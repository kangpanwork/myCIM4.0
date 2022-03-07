package com.fa.cim.frameworks.pprocess.executor.impl;

import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.constant.CIMStateConst;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.utils.CimArrayUtils;
import com.fa.cim.common.utils.CimStringUtils;
import com.fa.cim.common.utils.Validations;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Inputs;
import com.fa.cim.dto.Outputs;
import com.fa.cim.frameworks.dto.pp.PostProcessTask;
import com.fa.cim.frameworks.pprocess.api.annotations.PostProcessTaskHandler;
import com.fa.cim.frameworks.pprocess.api.definition.PostProcessExecutor;
import com.fa.cim.method.*;
import com.fa.cim.service.fsm.IFutureSplitMergeService;
import com.fa.cim.service.psm.IPlannedSplitMergeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>PSMExecutor .
 * change history:
 * date               defect            person            comments
 * -------------------------------------------------------------------------------------------------------------------
 * 2021/6/10 09:26    ********          ZQI               create file.
 *
 * @author ZQI
 * @version 1.0
 * @date 2021/6/10 09:26
 * @copyright 2021, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 * @since JDK1.8
 */
@Slf4j
@PostProcessTaskHandler(isNextOperationRequired = true)
public class PSMActionExecutor implements PostProcessExecutor {

    private final ILotMethod lotMethod;
    private final ICassetteMethod cassetteMethod;
    private final IObjectLockMethod objectLockMethod;
    private final IObjectMethod objectMethod;
    private final IRunCardMethod runCardMethod;
    private final IPlannedSplitMergeService plannedSplitMergeService;
    private final IFutureSplitMergeService futureSplitMergeService;
    private final RetCodeConfig retCodeConfig;

    @Autowired
    public PSMActionExecutor(ILotMethod lotMethod, ICassetteMethod cassetteMethod,
                             IObjectLockMethod objectLockMethod, IObjectMethod objectMethod,
                             IRunCardMethod runCardMethod, IPlannedSplitMergeService plannedSplitMergeService,
                             IFutureSplitMergeService futureSplitMergeService, RetCodeConfig retCodeConfig) {
        this.lotMethod = lotMethod;
        this.cassetteMethod = cassetteMethod;
        this.objectLockMethod = objectLockMethod;
        this.objectMethod = objectMethod;
        this.runCardMethod = runCardMethod;
        this.plannedSplitMergeService = plannedSplitMergeService;
        this.futureSplitMergeService = futureSplitMergeService;
        this.retCodeConfig = retCodeConfig;
    }

    /**
     * Plan split and merge
     *
     * @param param the necessary params for task handling
     * @return the executing result
     */
    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public PostProcessTask.Result doExecute(PostProcessTask.Param param) {
        if (null == param) return PostProcessTask.error(this);
        final ObjectIdentifier lotID = param.getEntityID();
        final Infos.ObjCommon objCommon = param.getObjCommon();

        final String lotState = lotMethod.lotStateGet(objCommon, lotID);
        if (CimStringUtils.equals(lotState, CIMStateConst.CIM_LOT_STATE_ACTIVE)) {
            if (log.isDebugEnabled()) log.debug("lot_state == Active. - N/A -");
            /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/
            /*                                                                       */
            /*   Object Lock Process                                                 */
            /*                                                                       */
            /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/
            //Get lot cassette
            final ObjectIdentifier cassetteID = lotMethod.lotCassetteGet(objCommon, lotID);
            if (ObjectIdentifier.isNotEmpty(cassetteID)) {
                final Infos.LotLocationInfo cassetteLocationInfo = cassetteMethod.cassetteLocationInfoGetDR(cassetteID);
                if (null != cassetteLocationInfo
                        && CimStringUtils.equals(cassetteLocationInfo.getTransferStatus(), BizConstant.SP_TRANSSTATE_EQUIPMENTIN)) {
                    Inputs.ObjLockModeIn objLockModeIn = new Inputs.ObjLockModeIn();
                    objLockModeIn.setObjectID(cassetteLocationInfo.getEquipmentID());
                    objLockModeIn.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
                    objLockModeIn.setFunctionCategory(BizConstant.SP_POSTPROCESS_ACTIONID_PSM);
                    objLockModeIn.setUserDataUpdateFlag(false);
                    // object_lockMode_Get
                    Outputs.ObjLockModeOut objLockModeOut = objectMethod.objectLockModeGet(objCommon, objLockModeIn);
                    Long lockMode = objLockModeOut.getLockMode();
                    if (!lockMode.equals(BizConstant.SP_EQP_LOCK_MODE_WRITE)) {
                        // Lock Equipment LoadCassette Element (Write)
                        final Inputs.ObjAdvanceLockIn objAdvanceLockIn = new Inputs.ObjAdvanceLockIn(cassetteLocationInfo.getEquipmentID(),
                                (long) BizConstant.SP_OBJECTLOCK_LOCKTYPE_WRITE,
                                Collections.singletonList(cassetteID.getValue()));
                        // lock
                        objectLockMethod.advancedObjectLock(objCommon, objAdvanceLockIn);
                    }
                }
            }

            //add run card auto complete start
            //-----------------------------------------------------------------
            // Call runCardAutoCompleteAction
            //-----------------------------------------------------------------
            try {
                runCardMethod.runCardAutoCompleteAction(objCommon, lotID);
            } catch (Exception e) {
                //do nothing
                log.error("RunCard Auto Complete Fail: {}", e.getMessage());
            }
            //add run card auto complete end

            //-----------------------------------------------------------------
            // Call sxPSMLotActionReq
            //-----------------------------------------------------------------
            List<ObjectIdentifier> newLotIDs = new ArrayList<>();
            try {
                newLotIDs.addAll(plannedSplitMergeService.sxPSMLotActionReq(objCommon, lotID, "PSMExecutor"));
            } catch (ServiceException e) {
                if (Validations.isEquals(retCodeConfig.getPostrpocDkeyRecreate(), e.getCode())) {
                    if (log.isDebugEnabled()) log.debug("Post Process DKey Recreate...");
                } else {
                    throw e;
                }
            }

            //future split merge
            //-----------------------------------------------------------------
            // Call sxFSMLotActionReq
            //-----------------------------------------------------------------
            try {
                newLotIDs.addAll(futureSplitMergeService.sxFSMLotActionReq(objCommon, lotID, "FSMExecutor"));
            } catch (ServiceException e) {
                if (Validations.isEquals(retCodeConfig.getPostrpocDkeyRecreate(), e.getCode())) {
                    if (log.isDebugEnabled()) log.debug("Post Process DKey Recreate...");
                } else {
                    throw e;
                }
            }

            if (CimArrayUtils.isNotEmpty(newLotIDs)) {
                List<PostProcessTask.ExtraTask> extraTasks = newLotIDs.stream()
                        .distinct()
                        .map(newLotID -> PostProcessTask.extraLotTask("P_BRANCH", newLotID))
                        .collect(Collectors.toList());
                return PostProcessTask.success(extraTasks);
            } else {
                return PostProcessTask.success();
            }
        } else {
            if (log.isDebugEnabled()) log.debug("lot_state != Active. - N/A -");
            return PostProcessTask.success();
        }
    }
}
