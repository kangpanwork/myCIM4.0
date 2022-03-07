package com.fa.cim.frameworks.pprocess.executor.impl;

import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.constant.CIMStateConst;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.utils.CimArrayUtils;
import com.fa.cim.common.utils.CimStringUtils;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Inputs;
import com.fa.cim.frameworks.dto.pp.PostProcessTask;
import com.fa.cim.frameworks.dto.pp.mode.AvailablePhase;
import com.fa.cim.frameworks.pprocess.api.annotations.PostProcessTaskHandler;
import com.fa.cim.frameworks.pprocess.api.definition.PostProcessExecutor;
import com.fa.cim.method.IEquipmentMethod;
import com.fa.cim.method.IEventMethod;
import com.fa.cim.method.ILotMethod;
import com.fa.cim.newcore.impl.bo.env.StandardProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * <p>AutoMonitorUsedCountUpExecutor .
 * change history:
 * date               defect            person            comments
 * -------------------------------------------------------------------------------------------------------------------
 * 2021/6/10 15:30    ********          ZQI               create file.
 *
 * @author ZQI
 * @version 1.0
 * @date 2021/6/10 15:30
 * @copyright 2021, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 * @since JDK1.8
 */
@Slf4j
@PostProcessTaskHandler
public class AutoMonitorUsedCountUpExecutor implements PostProcessExecutor {

    private final ILotMethod lotMethod;
    private final IEventMethod eventMethod;
    private final IEquipmentMethod equipmentMethod;

    @Autowired
    public AutoMonitorUsedCountUpExecutor(ILotMethod lotMethod, IEventMethod eventMethod,
                                          IEquipmentMethod equipmentMethod) {
        this.lotMethod = lotMethod;
        this.eventMethod = eventMethod;
        this.equipmentMethod = equipmentMethod;
    }

    /**
     * Auto monitor used count up
     *
     * @param param the necessary params for task handling
     * @return post process result
     */
    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public PostProcessTask.Result doExecute(PostProcessTask.Param param) {
        if (null == param) return PostProcessTask.error(this);
        final Infos.ObjCommon objCommon = param.getObjCommon();
        final ObjectIdentifier lotID = param.getEntityID();
        final ObjectIdentifier equipmentID = param.getEquipmentID();
        final ObjectIdentifier controlJobID = param.getControlJobID();

        String lotState = lotMethod.lotStateGet(objCommon, lotID);
        if (CimStringUtils.equals(lotState, CIMStateConst.CIM_LOT_STATE_ACTIVE)) {
            if (log.isDebugEnabled()) log.debug("lot_state = Active. - N/A -");
            int eqpmonitorSwitch = StandardProperties.OM_AUTOMON_FLAG.getIntValue();
            if (eqpmonitorSwitch == 1) {
                //Check Lot type
                String lotType = lotMethod.lotTypeGet(objCommon, lotID);
                if (CimStringUtils.equals(lotType, BizConstant.SP_LOT_TYPE_EQUIPMENTMONITORLOT)
                        || CimStringUtils.equals(lotType, BizConstant.SP_LOT_TYPE_DUMMYLOT)) {
                    //--------------------------------------
                    // call lotEqpMonitorOperationLabelGet
                    //--------------------------------------
                    List<Infos.EqpMonitorLabelInfo> eqpMonitorLabelInfos = lotMethod.lotEqpMonitorOperationLabelGet(objCommon, lotID);
                    boolean bMonitorLabel = false;
                    if (CimArrayUtils.isNotEmpty(eqpMonitorLabelInfos)) {
                        for (Infos.EqpMonitorLabelInfo eqpMonitorLabelInfo : eqpMonitorLabelInfos) {
                            if (CimStringUtils.equals(eqpMonitorLabelInfo.getOperationLabel(), BizConstant.SP_EQPMONITOR_OPELABEL_MONITOR)) {
                                bMonitorLabel = true;
                                break;
                            }
                        }
                    }
                    if (!bMonitorLabel) {
                        if (log.isDebugEnabled()) log.debug("AutoMonitorUsedCountUp can be skipped");
                    } else {
                        if (log.isDebugEnabled()) log.debug("Update Equipmet Monitor count");
                        //Increment Equipment Monitor used count by 1 for all lot's wafers.
                        Inputs.ObjEqpMonitorWaferUsedCountUpdateIn objEqpMonitorWaferUsedCountUpdateIn =
                                new Inputs.ObjEqpMonitorWaferUsedCountUpdateIn();
                        objEqpMonitorWaferUsedCountUpdateIn.setLotID(lotID);
                        objEqpMonitorWaferUsedCountUpdateIn.setAction(BizConstant.SP_EQPMONUSEDCNT_ACTION_INCREMENT);
                        //--------------------------------------
                        // call eqpMonitorWaferUsedCountUpdate
                        //--------------------------------------
                        List<Infos.EqpMonitorWaferUsedCount> eqpMonitorWaferUsedCounts = equipmentMethod
                                .eqpMonitorWaferUsedCountUpdate(objCommon, objEqpMonitorWaferUsedCountUpdateIn);

                        // Create Operation History
                        Inputs.ObjEqpMonitorWaferUsedCountUpdateEventMakeParams eventMakeParams =
                                new Inputs.ObjEqpMonitorWaferUsedCountUpdateEventMakeParams();
                        eventMakeParams.setTransactionID("OAMNW007");
                        eventMakeParams.setLotID(lotID);
                        eventMakeParams.setEquipmentID(equipmentID);
                        eventMakeParams.setControlJobID(controlJobID);
                        eventMakeParams.setStrEqpMonitorWaferUsedCountList(eqpMonitorWaferUsedCounts);
                        eventMakeParams.setClaimMemo("AutoMonitorUsedCountUpExecutor");
                        eventMethod.eqpMonitorWaferUsedCountUpdateEventMake(objCommon, eventMakeParams);
                    }
                }
            }
            return PostProcessTask.success();
        } else {
            if (log.isDebugEnabled()) log.debug("lot_state != Active. - N/A -");
            return PostProcessTask.success();
        }
    }
}
