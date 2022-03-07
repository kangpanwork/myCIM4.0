package com.fa.cim.frameworks.pprocess.executor.impl;

import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.constant.CIMStateConst;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.utils.CimStringUtils;
import com.fa.cim.common.utils.Validations;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Inputs;
import com.fa.cim.dto.Outputs;
import com.fa.cim.frameworks.dto.pp.PostProcessTask;
import com.fa.cim.frameworks.pprocess.api.annotations.PostProcessTaskHandler;
import com.fa.cim.frameworks.pprocess.api.definition.PostProcessExecutor;
import com.fa.cim.method.ICassetteMethod;
import com.fa.cim.method.ILotMethod;
import com.fa.cim.method.IObjectLockMethod;
import com.fa.cim.method.IObjectMethod;
import com.fa.cim.service.doc.IDynamicOperationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

/**
 * <p>DOCActionExecutor .
 * change history:
 * date               defect            person            comments
 * -------------------------------------------------------------------------------------------------------------------
 * 2021/6/10 11:28    ********          ZQI               create file.
 *
 * @author ZQI
 * @version 1.0
 * @date 2021/6/10 11:28
 * @copyright 2021, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 * @since JDK1.8
 */
@Slf4j
@PostProcessTaskHandler(isNextOperationRequired = true)
public class DOCActionExecutor implements PostProcessExecutor {

    private final ILotMethod lotMethod;
    private final ICassetteMethod cassetteMethod;
    private final IObjectLockMethod objectLockMethod;
    private final IObjectMethod objectMethod;
    private final IDynamicOperationService dynamicOperationService;
    private final RetCodeConfig retCodeConfig;

    @Autowired
    public DOCActionExecutor(ILotMethod lotMethod, ICassetteMethod cassetteMethod,
                             IObjectLockMethod objectLockMethod, IObjectMethod objectMethod,
                             IDynamicOperationService dynamicOperationService, RetCodeConfig retCodeConfig) {
        this.lotMethod = lotMethod;
        this.cassetteMethod = cassetteMethod;
        this.objectLockMethod = objectLockMethod;
        this.objectMethod = objectMethod;
        this.dynamicOperationService = dynamicOperationService;
        this.retCodeConfig = retCodeConfig;
    }


    /**
     * DOC (Dynamic Process Operation Change)
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

        String lotState = lotMethod.lotStateGet(objCommon, lotID);
        if (CimStringUtils.equals(lotState, CIMStateConst.CIM_LOT_STATE_ACTIVE)) {
            if (log.isDebugEnabled()) log.debug("lot_state = Active. - N/A -");
            /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/
            /*                                                                       */
            /*   Object Lock Process                                                 */
            /*                                                                       */
            /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/
            //Get lot cassette
            final ObjectIdentifier lotCassetteGet = lotMethod.lotCassetteGet(objCommon, lotID);
            if (ObjectIdentifier.isNotEmpty(lotCassetteGet)) {
                Infos.LotLocationInfo cassetteLocationInfo = cassetteMethod.cassetteLocationInfoGetDR(lotCassetteGet);
                // Transfer state is "EI"
                if (CimStringUtils.equals(cassetteLocationInfo.getTransferStatus(), BizConstant.SP_TRANSSTATE_EQUIPMENTIN)) {
                    // object_lockMode_Get
                    Inputs.ObjLockModeIn objLockModeIn = new Inputs.ObjLockModeIn();
                    objLockModeIn.setObjectID(cassetteLocationInfo.getEquipmentID());
                    objLockModeIn.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
                    objLockModeIn.setFunctionCategory(BizConstant.SP_POSTPROCESS_ACTIONID_FPC);
                    objLockModeIn.setUserDataUpdateFlag(false);
                    Outputs.ObjLockModeOut objLockModeOut = objectMethod.objectLockModeGet(objCommon, objLockModeIn);
                    Long lockMode = objLockModeOut.getLockMode();
                    if (!lockMode.equals(BizConstant.SP_EQP_LOCK_MODE_WRITE)) {
                        // Lock Equipment LoadCassette Element (Write)
                        final Inputs.ObjAdvanceLockIn objAdvanceLockIn = new Inputs.ObjAdvanceLockIn(cassetteLocationInfo.getEquipmentID(),
                                (long) BizConstant.SP_OBJECTLOCK_LOCKTYPE_WRITE,
                                Collections.singletonList(lotCassetteGet.getValue()));
                        objectLockMethod.advancedObjectLock(objCommon, objAdvanceLockIn);
                    }
                }
            }

            //-----------------------------
            // Call sxDOCLotActionReq
            //-----------------------------
            if (log.isDebugEnabled()) log.debug("call sxDOCLotActionReq()...");
            try {
                dynamicOperationService.sxDOCLotActionReq(objCommon, lotID);
            } catch (ServiceException e) {
                if (Validations.isEquals(retCodeConfig.getPostrpocDkeyRecreate(), e.getCode())) {
                    if (log.isDebugEnabled()) log.debug("Post Process DKey Recreate...");
                } else {
                    throw e;
                }
            }
            return PostProcessTask.success();
        } else {
            if (log.isDebugEnabled()) log.debug("lot_state != Active. - N/A -");
            return PostProcessTask.success();
        }
    }
}
