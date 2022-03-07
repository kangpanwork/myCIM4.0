package com.fa.cim.frameworks.pprocess.executor.impl;

import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.utils.CimArrayUtils;
import com.fa.cim.common.utils.CimStringUtils;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Inputs;
import com.fa.cim.dto.Outputs;
import com.fa.cim.frameworks.dto.pp.PostProcessTask;
import com.fa.cim.frameworks.dto.pp.mode.AvailablePhase;
import com.fa.cim.frameworks.pprocess.api.annotations.PostProcessTaskHandler;
import com.fa.cim.frameworks.pprocess.api.definition.PostProcessExecutor;
import com.fa.cim.method.IEquipmentMethod;
import com.fa.cim.method.IObjectLockMethod;
import com.fa.cim.method.IObjectMethod;
import com.fa.cim.newcore.bo.machine.CimMachine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

/**
 * <p>RunWaferInfoUpdateExecutor .
 * change history:
 * date               defect            person            comments
 * -------------------------------------------------------------------------------------------------------------------
 * 2021/6/10 14:41    ********          ZQI               create file.
 *
 * @author ZQI
 * @version 1.0
 * @date 2021/6/10 14:41
 * @copyright 2021, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 * @since JDK1.8
 */
@Slf4j
@PostProcessTaskHandler
public class RunWaferInfoUpdateExecutor implements PostProcessExecutor {

    private final IObjectLockMethod objectLockMethod;
    private final IObjectMethod objectMethod;
    private final IEquipmentMethod equipmentMethod;

    @Autowired
    public RunWaferInfoUpdateExecutor(IObjectLockMethod objectLockMethod, IObjectMethod objectMethod,
                                      IEquipmentMethod equipmentMethod) {
        this.objectLockMethod = objectLockMethod;
        this.objectMethod = objectMethod;
        this.equipmentMethod = equipmentMethod;
    }

    /**
     * Update run wafer count for processed equipment after operation start or operation start cancel
     *
     * @param param the necessary params for task handling
     * @return post process result
     */
    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public PostProcessTask.Result doExecute(PostProcessTask.Param param) {
        if (null == param) return PostProcessTask.error(this);
        final Infos.ObjCommon objCommon = param.getObjCommon();
        final ObjectIdentifier equipmentID = param.getEquipmentID();
        final List<PostProcessTask.Detail> details = param.getDetails();
        final String transactionId = objCommon.getTransactionID();

        int processWaferCnt = 0;
        int opeStartCnt = 0;
        if (CimArrayUtils.isNotEmpty(details)) {
            for (PostProcessTask.Detail detail : details) {
                if (CimStringUtils.equals(detail.getName(), BizConstant.SP_THREADSPECIFICDATA_KEY_RUNWAFERCNT)) {
                    processWaferCnt = Integer.parseInt(detail.getValue());
                }
                if (CimStringUtils.equals(detail.getName(), BizConstant.SP_THREADSPECIFICDATA_KEY_OPESTARTCNT)) {
                    opeStartCnt = Integer.parseInt(detail.getValue());
                }
            }
        }

        if (processWaferCnt > 0 || opeStartCnt > 0) {
            // object_lockMode_Get
            Inputs.ObjLockModeIn objLockModeIn = new Inputs.ObjLockModeIn();
            objLockModeIn.setObjectID(equipmentID);
            objLockModeIn.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
            objLockModeIn.setFunctionCategory(BizConstant.SP_POSTPROCESS_ACTIONID_RUNWAFERINFOUPDATE);
            objLockModeIn.setUserDataUpdateFlag(false);
            Outputs.ObjLockModeOut objLockModeOut = objectMethod.objectLockModeGet(objCommon, objLockModeIn);
            Long lockMode = objLockModeOut.getLockMode();
            if (!lockMode.equals(BizConstant.SP_EQP_LOCK_MODE_WRITE)) {
                // Lock Equipment LoadCassette Element (Write)
                final Inputs.ObjAdvanceLockIn objAdvanceLockIn = new Inputs.ObjAdvanceLockIn(
                        equipmentID,
                        objLockModeOut.getRequiredLockForMainObject(),
                        Collections.emptyList());
                objectLockMethod.advancedObjectLock(objCommon, objAdvanceLockIn);
            } else {
                /*--------------------------------*/
                /*   Lock Equipment object        */
                /*--------------------------------*/
                objectLockMethod.objectLock(objCommon, CimMachine.class, equipmentID);
            }
            Inputs.ObjEquipmentUsageCountUpdateForPostProcIn objEquipmentUsageCountUpdateForPostProcIn =
                    new Inputs.ObjEquipmentUsageCountUpdateForPostProcIn();
            objEquipmentUsageCountUpdateForPostProcIn.setEquipmentID(equipmentID);
            objEquipmentUsageCountUpdateForPostProcIn.setWaferCnt(processWaferCnt);
            objEquipmentUsageCountUpdateForPostProcIn.setOpeStartCnt(opeStartCnt);
            // set new equipment PM related attributes

            if (TransactionIDEnum.OPERATION_START_REQ.equals(transactionId)
                    || TransactionIDEnum.OPERATION_START_FOR_INTERNAL_BUFFER_REQ.equals(transactionId)
                    || TransactionIDEnum.DURABLE_OPERATION_START_FOR_IB_REQ.equals(transactionId)
                    || TransactionIDEnum.DURABLE_OPERATION_START_REQ.equals(transactionId)) {
                objEquipmentUsageCountUpdateForPostProcIn.setAction(BizConstant.SP_EQPATTR_UPDATE_ACTION_INCREASE);
            } else if (TransactionIDEnum.OPERATION_START_CANCEL_REQ.equals(transactionId)
                    || TransactionIDEnum.OPERATION_START_CANCEL_FOR_INTERNAL_BUFFER_REQ.equals(transactionId)
                    || TransactionIDEnum.PARTIAL_OPERATION_COMP_WITH_DATA_REQ.equals(transactionId)
                    || TransactionIDEnum.PARTIAL_OPERATION_COMP_FOR_INTERNAL_BUFFER_REQ.equals(transactionId)
                    || TransactionIDEnum.DURABLE_OPERATION_START_CANCEL_FOR_IB_REQ.equals(transactionId)
                    || TransactionIDEnum.DURABLE_OPERATION_START_CANCEL_REQ.equals(transactionId)) {
                objEquipmentUsageCountUpdateForPostProcIn.setAction(BizConstant.SP_EQPATTR_UPDATE_ACTION_DECREASE);
            }
            //------------------------------------------------
            // call equipmentUsageCountUpdateForPostProc
            //------------------------------------------------
            log.debug("call equipmentUsageCountUpdateForPostProc()...");
            equipmentMethod.equipmentUsageCountUpdateForPostProc(objCommon, objEquipmentUsageCountUpdateForPostProcIn);
        }

        return PostProcessTask.success();
    }
}
