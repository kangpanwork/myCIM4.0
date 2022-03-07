package com.fa.cim.service.arhs.impl;

import com.fa.cim.annotaion.OmService;
import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.utils.*;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.config.RetCodeConfigEx;
import com.fa.cim.dto.*;
import com.fa.cim.idp.tms.api.TmsService;
import com.fa.cim.method.*;
import com.fa.cim.newcore.bo.durable.CimDurableControlJob;
import com.fa.cim.newcore.bo.durable.CimReticlePod;
import com.fa.cim.newcore.impl.bo.env.StandardProperties;
import com.fa.cim.service.arhs.IArhsService;
import com.fa.cim.service.durable.IDurableInqService;
import com.fa.cim.service.durable.IDurableService;
import com.fa.cim.service.system.ISystemService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * description:
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/11/3                              Neyo                create file
 *
 * @author: Neyo
 * @date: 2020/11/3 16:15
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@OmService
@Slf4j
public class ArhsServiceImpl implements IArhsService {

    @Autowired
    private RetCodeConfig retCodeConfig;
    @Autowired
    private RetCodeConfigEx retCodeConfigEx;
    @Autowired
    private IEquipmentMethod equipmentMethod;
    @Autowired
    private IReticleMethod reticleMethod;
    @Autowired
    private IDurableInqService durableInqService;
    @Autowired
    private ITCSMethod tcsMethod;
    @Autowired
    private IObjectLockMethod objectLockMethod;
    @Autowired
    private IStockerMethod stockerMethod;
    @Autowired
    private IObjectLockMethod lockMethod;
    @Autowired
    private IDurableMethod durableMethod;
    @Autowired
    private TmsService tmsService;
    @Autowired
    private IEquipmentForDurableMethod equipmentForDurableMethod;
    @Autowired
    private IDurableService durableService;
    @Autowired
    private ISystemService systemService;


    @Override
    public void sxAsyncReticleXferJobCreateReq(Infos.ObjCommon objCommon, Params.AsyncReticleXferJobCreateReqParams params) {
        Results.WhereNextForReticlePodInqResult result = new Results.WhereNextForReticlePodInqResult();
        String tmpArhs = StandardProperties.OM_ARHS_FLAG.getValue();
        if (log.isDebugEnabled())
            log.debug("ARHS switch on / off  tmpArhs = {}", tmpArhs);
        Validations.check(!CimStringUtils.equals(BizConstant.SP_ARMS_SWITCH_ON, tmpArhs), retCodeConfig.getFunctionNotAvailable());

        //-----------------------------------------
        //  check input param
        //-----------------------------------------
        log.debug("check input param");
        Validations.check(ObjectIdentifier.isEmptyWithValue(params.getToMachineID())
                || (ObjectIdentifier.isEmptyWithValue(params.getReticleID())
                && ObjectIdentifier.isEmptyWithValue(params.getReticlePodID())), retCodeConfig.getInvalidParameter());

        //-----------------------------------------
        //  create RDJ information
        //-----------------------------------------
        log.debug("create RDJ ID");
        String reticleDispatchJobID =reticleMethod.timeStampGetDR();

        log.debug("create RDJ data");
        log.debug("step1 - reticleMethod.reticleDispatchJobCreate");
        Outputs.ReticleDispatchJobCreateOut reticleDispatchJobCreateOut = null;
        try {
            reticleDispatchJobCreateOut = reticleMethod.reticleDispatchJobCreate(objCommon,
                    reticleDispatchJobID,
                    objCommon.getUser().getUserID(),
                    1L,
                    params.getReticleID(),
                    params.getReticlePodID(),
                    params.getToMachineID());
        } catch (ServiceException e) {
            if (Validations.isEquals(retCodeConfigEx.getInvalidRdjRecord(), e.getCode())) {
                Validations.check(true, retCodeConfig.getInvalidParameter());
            } else {
                throw e;
            }
        }
        // check Priority 1-3
        if (log.isDebugEnabled())
            log.debug("Check Priority, priority = {}", null != reticleDispatchJobCreateOut.getStrReticleDispatchJob() ? reticleDispatchJobCreateOut.getStrReticleDispatchJob().getPriority() : null);
        if (null != reticleDispatchJobCreateOut.getStrReticleDispatchJob()) {
            Infos.ReticleDispatchJob strReticleDispatchJob = reticleDispatchJobCreateOut.getStrReticleDispatchJob();
            if (strReticleDispatchJob.getPriority() < 1 || strReticleDispatchJob.getPriority() > 3) {
                log.error("priority < 1 || priority > 3, return!");
                Validations.check(true, retCodeConfigEx.getInvalidPriority());
            }
            // check existence of ReticlePod
            if (log.isDebugEnabled())
                log.debug("Check existance of reticlePodID = {}", ObjectIdentifier.fetchValue(strReticleDispatchJob.getReticlePodID()));
            if (ObjectIdentifier.isNotEmptyWithValue(strReticleDispatchJob.getReticlePodID())) {
                log.debug("call sxReticlePodDetailInfoInq() to check reticlePodID.");
                log.debug("step2 - durableInqService.sxReticlePodDetailInfoInq");
                Params.ReticlePodDetailInfoInqParams reticlePodDetailInfoInqParams = new Params.ReticlePodDetailInfoInqParams();
                reticlePodDetailInfoInqParams.setDurableOperationInfoFlag(false);
                reticlePodDetailInfoInqParams.setDurableWipOperationInfoFlag(false);
                reticlePodDetailInfoInqParams.setReticlePodID(strReticleDispatchJob.getReticlePodID());
                Results.ReticlePodDetailInfoInqResult reticlePodDetailInfoInqResult = durableInqService.sxReticlePodDetailInfoInq(objCommon, reticlePodDetailInfoInqParams);
            } else {
                log.debug("ReticlePodID is blank. no need to check.");
            }
            if (log.isDebugEnabled())
                log.debug("Check existance of reticleID = {}", ObjectIdentifier.fetchValue(strReticleDispatchJob.getReticleID()));
            log.debug("step3 - durableInqService.sxReticleDetailInfoInq");
            Results.ReticleDetailInfoInqResult reticleDetailInfoInqResult = durableInqService.sxReticleDetailInfoInq(objCommon, strReticleDispatchJob.getReticleID(), false, false);

            // check existence of toEQP
            if (log.isDebugEnabled())
                log.debug("Check existance of toEquipmentID = {}", ObjectIdentifier.fetchValue(strReticleDispatchJob.getToEquipmentID()));
            log.debug("step4 - equipmentMethod.equipmentMachineTypeCheckDR");
            Boolean equipmentFlag = equipmentMethod.equipmentMachineTypeCheckDR(objCommon, strReticleDispatchJob.getToEquipmentID());
            if (CimBooleanUtils.isTrue(equipmentFlag)) {
                log.debug("toEquipment Type == 'Equipment'");
            } else {
                log.debug("toEquipment Type == 'Stocker'");
            }

            //-----------------------------------------
            //  Check RDJ about Reticle
            //-----------------------------------------
            log.debug("Check RDJ about Reticle.");
            ObjectIdentifier dummyID = ObjectIdentifier.buildWithValue("");
            log.debug("step5 - reticleMethod.reticleDispatchJobCheckExistenceDR");
            List<Infos.ReticleDispatchJob> reticleDispatchJobs = null;
            try {
                reticleDispatchJobs = reticleMethod.reticleDispatchJobCheckExistenceDR(objCommon,
                        strReticleDispatchJob.getReticleID(),
                        dummyID,
                        dummyID);
            } catch (ServiceException e) {
                if (Validations.isEquals(retCodeConfig.getFoundInRdj(), e.getCode())) {
                    if (CimArrayUtils.isNotEmpty(reticleDispatchJobs)) {
                        for (Infos.ReticleDispatchJob reticleDispatchJob : reticleDispatchJobs) {
                            //Check RDJ DispatchStation
                            if (log.isDebugEnabled())
                                log.debug("dispatchStationID = {}", reticleDispatchJob.getDispatchStationID());
                            if (CimStringUtils.equals(reticleDispatchJob.getDispatchStationID(), strReticleDispatchJob.getDispatchStationID())) {
                                log.error("Reticle already has the same DispatchStation in other RDJ, return!");
                                Validations.check(true, retCodeConfigEx.getRtclAlreadyHasSameDispStation());
                            }
                            //Check RDJ User
                            if (log.isDebugEnabled())
                                log.debug("requestUserID = {}", ObjectIdentifier.fetchValue(reticleDispatchJob.getRequestUserID()));
                            if (ObjectIdentifier.equalsWithValue(reticleDispatchJob.getRequestUserID(), strReticleDispatchJob.getRequestUserID())) {
                                log.error("Reticle already has the same User in other RDJ, return!");
                                Validations.check(true, retCodeConfigEx.getRtclAlreadyHasSameUser());
                            }
                            //Check RDJ Status
                            if (log.isDebugEnabled())
                                log.debug("jobStatus = {}", reticleDispatchJob.getJobStatus());
                            if (!CimStringUtils.equals(reticleDispatchJob.getJobStatus(), BizConstant.SP_RDJ_STATUS_WAITTOEXECUTE)
                                    && !CimStringUtils.equals(reticleDispatchJob.getJobStatus(), BizConstant.SP_RDJ_STATUS_EXECUTING)
                                    && !CimStringUtils.equals(reticleDispatchJob.getJobStatus(), BizConstant.SP_RDJ_STATUS_COMPLETED)
                                    && !CimStringUtils.equals(reticleDispatchJob.getJobStatus(), BizConstant.SP_RDJ_STATUS_ERROR)) {
                                log.error("Reticle RDJ Status != 'WaitToExecute', 'Executing', 'Completed' and 'Error', return!");
                                Validations.check(true, retCodeConfigEx.getInvalidRtclRdjStatus());
                            }
                        }
                    }
                } else {
                    throw e;
                }
            }
            //-----------------------------------------
            //  Check RDJ about ReticlePod
            //-----------------------------------------
            if (ObjectIdentifier.isNotEmptyWithValue(strReticleDispatchJob.getReticlePodID())) {
                log.debug("call reticle_dispatchJob_CheckExistenceDR() to check reticlePodID.");
                log.debug("step6 - reticleMethod.reticleDispatchJobCheckExistenceDR");
                List<Infos.ReticleDispatchJob> reticleDispatchJobList = null;
                try {
                    reticleDispatchJobList = reticleMethod.reticleDispatchJobCheckExistenceDR(objCommon,
                            dummyID,
                            strReticleDispatchJob.getReticlePodID(),
                            dummyID);
                } catch (ServiceException e) {
                    if (Validations.isEquals(retCodeConfig.getFoundInRdj(), e.getCode())) {
                        if (CimArrayUtils.isNotEmpty(reticleDispatchJobs)) {
                            for (Infos.ReticleDispatchJob reticleDispatchJob : reticleDispatchJobList) {
                                //Check RDJ DispatchStation
                                if (log.isDebugEnabled())
                                    log.debug("dispatchStationID = {}", reticleDispatchJob.getDispatchStationID());
                                if (CimStringUtils.equals(reticleDispatchJob.getDispatchStationID(), strReticleDispatchJob.getDispatchStationID())) {
                                    log.debug("ReticlePod already has the same DispatchStation in other RDJ, return!");
                                    Validations.check(true, retCodeConfigEx.getRtclAlreadyHasSameDispStation());
                                }
                                //Check RDJ User
                                if (log.isDebugEnabled())
                                    log.debug("requestUserID = {}", ObjectIdentifier.fetchValue(reticleDispatchJob.getRequestUserID()));
                                if (ObjectIdentifier.equalsWithValue(reticleDispatchJob.getRequestUserID(), strReticleDispatchJob.getRequestUserID())) {
                                    log.error("ReticlePod already has the same User in other RDJ, return!");
                                    Validations.check(true, retCodeConfigEx.getRtclAlreadyHasSameUser());
                                }
                                //Check RDJ Status
                                if (log.isDebugEnabled())
                                    log.debug("jobStatus = {}", reticleDispatchJob.getJobStatus());
                                if (!CimStringUtils.equals(reticleDispatchJob.getJobStatus(), BizConstant.SP_RDJ_STATUS_WAITTOEXECUTE)
                                        && !CimStringUtils.equals(reticleDispatchJob.getJobStatus(), BizConstant.SP_RDJ_STATUS_EXECUTING)
                                        && !CimStringUtils.equals(reticleDispatchJob.getJobStatus(), BizConstant.SP_RDJ_STATUS_COMPLETED)
                                        && !CimStringUtils.equals(reticleDispatchJob.getJobStatus(), BizConstant.SP_RDJ_STATUS_ERROR)) {
                                    log.error("ReticlePod RDJ Status != 'WaitToExecute', 'Executing', 'Completed' and 'Error', return!");
                                    Validations.check(true, retCodeConfigEx.getInvalidRtclRdjStatus());
                                }
                            }
                        }
                    } else {
                        throw e;
                    }
                }
            } else {
                log.debug("ReticlePodID is blank. no need to check.");
            }
            //-----------------------------------------
            //  Insert RDJ information for FSRAL
            //-----------------------------------------
            log.debug("step7 - reticleMethod.reticleDispatchJobInsertDR");
            reticleMethod.reticleDispatchJobInsertDR(objCommon, Collections.singletonList(strReticleDispatchJob));

            //-----------------------------------------
            //  Reserve Reticle and ReticlePod
            //-----------------------------------------
            if (ObjectIdentifier.isNotEmptyWithValue(strReticleDispatchJob.getReticlePodID())) {
                log.debug("call reticleReticlePodReserve() to reticle reserve reticlePodID.");
                log.debug("step8 - reticleMethod.reticleReticlePodReserve");
                reticleMethod.reticleReticlePodReserve(objCommon,
                        strReticleDispatchJob.getReticleID(),
                        strReticleDispatchJob.getReticlePodID(),
                        strReticleDispatchJob.getToEquipmentID());
            }
            //-----------------------------------------
            //  Create RDJ Event Make
            //-----------------------------------------
            log.debug("step9 - reticleMethod.reticleEventQueuePutDR");
            reticleMethod.reticleEventQueuePutDR(objCommon, reticleDispatchJobCreateOut.getStrReticleEventRecord());
        }
    }


    @Override
    public Results.RSPXferStatusChangeRptResult sxRSPXferStatusChangeRpt(Infos.ObjCommon objCommon, Params.RSPXferStatusChangeRptParams params) {
        ObjectIdentifier reticlePodID = params.getReticlePodID();
        ObjectIdentifier machineID = params.getMachineID();
        Boolean manualInFlag = params.getManualInFlag();
        String xferStatus = params.getXferStatus();
        ObjectIdentifier portID = params.getPortID();

        String tmpARMS = StandardProperties.OM_ARHS_FLAG.getValue();
        if (log.isDebugEnabled())
            log.debug("ARMS switch on / off  tmpARMS = {}", tmpARMS);
        Validations.check(!CimStringUtils.equals(tmpARMS, BizConstant.SP_ARMS_SWITCH_ON), retCodeConfig.getFunctionNotAvailable());

        //=========================================================================
        // Get input machine type
        //=========================================================================
        Outputs.ObjMachineTypeGetOut objMachineTypeGetOut = equipmentMethod.machineTypeGet(objCommon, machineID);

        //=========================================================================
        // check valid type stocker or valid type equipment or not
        //=========================================================================
        String stockerType = objMachineTypeGetOut.getStockerType();
        if (CimBooleanUtils.isTrue(objMachineTypeGetOut.isBStorageMachineFlag())) {
            Validations.check(!CimStringUtils.equals(stockerType, BizConstant.SP_STOCKER_TYPE_RETICLE) &&
                    !CimStringUtils.equals(stockerType, BizConstant.SP_STOCKER_TYPE_INTERBAY) &&
                    !CimStringUtils.equals(stockerType, BizConstant.SP_STOCKER_TYPE_INTRABAY) &&
                    !CimStringUtils.equals(stockerType, BizConstant.SP_STOCKER_TYPE_RETICLESHELF) &&
                    !CimStringUtils.equals(stockerType, BizConstant.SP_STOCKER_TYPE_BARERETICLE) &&
                    !CimStringUtils.equals(stockerType, BizConstant.SP_STOCKER_TYPE_RETICLEPOD), retCodeConfigEx.getStkTypeDifferent(), stockerType);
        } else {
            log.error("called with EQP ID - invalid. {}", ObjectIdentifier.fetchValue(machineID));
            Validations.check(retCodeConfig.getInvalidParameter());
        }

        //=========================================================================
        // Get reticle pod current transfer status
        //=========================================================================
        String currentXferStatus = reticleMethod.reticlePodTransferStateGetDR(objCommon, reticlePodID);
        if (log.isDebugEnabled())
            log.debug("currentXferStatus {}", currentXferStatus);

        //=========================================================================
        // Deside carrier transfer status by using current status and input parameter
        //=========================================================================
        ObjectIdentifier stockerID;
        ObjectIdentifier equipmentID = null;
        String newTransferState = null;

        stockerID = machineID;
        // Manual In case
        if (manualInFlag) {
            newTransferState = BizConstant.SP_TRANSSTATE_MANUALIN;
        }
        // Manual Out case
        else if (CimStringUtils.equals(xferStatus, BizConstant.SP_TRANSSTATE_MANUALOUT)) {
            newTransferState = xferStatus; // trust input parameter
        } else {
            if (CimStringUtils.equals(stockerType, BizConstant.SP_STOCKER_TYPE_RETICLE)) {
                newTransferState = xferStatus; // trust input parameter
            } else if (CimStringUtils.equals(stockerType, BizConstant.SP_STOCKER_TYPE_INTERBAY)) {
                newTransferState = BizConstant.SP_TRANSSTATE_BAYOUT;
            } else if (CimStringUtils.equals(stockerType, BizConstant.SP_STOCKER_TYPE_INTRABAY)) {
                newTransferState = BizConstant.SP_TRANSSTATE_STATIONOUT;
            } else if (CimStringUtils.equals(stockerType, BizConstant.SP_STOCKER_TYPE_RETICLESHELF)) {
                newTransferState = xferStatus; // trust input parameter
            } else if (CimStringUtils.equals(stockerType, BizConstant.SP_STOCKER_TYPE_RETICLEPOD)) {
                if (CimStringUtils.equals(currentXferStatus, BizConstant.SP_TRANSSTATE_MANUALIN)) {
                    newTransferState = BizConstant.SP_TRANSSTATE_MANUALIN;
                } else if (CimStringUtils.equals(currentXferStatus, BizConstant.SP_TRANSSTATE_BAYIN) ||
                        CimStringUtils.equals(currentXferStatus, BizConstant.SP_TRANSSTATE_BAYOUT)) {
                    newTransferState = BizConstant.SP_TRANSSTATE_BAYIN;
                } else {
                    newTransferState = BizConstant.SP_TRANSSTATE_STATIONIN;
                }
            }else if (CimStringUtils.equals(stockerType, BizConstant.SP_STOCKER_TYPE_BARERETICLE)){
                newTransferState = xferStatus; // trust input parameter
            }
        }

        if (log.isDebugEnabled()){
            log.debug("xferStatus       {}", xferStatus);
            log.debug("newTransferState {}", newTransferState);
        }

        /**************************************************************/
        /*  Check whether the input reticle Pod is on RSPPort or not  */
        /**************************************************************/
        Infos.ReticlePodCurrentMachineGetOut reticlePodCurrentMachineGetOut = reticleMethod.reticlePodCurrentMachineGet(objCommon, reticlePodID);
        ObjectIdentifier currentReticlePodPortID = reticlePodCurrentMachineGetOut.getCurrentReticlePodPortID();

        //----------------------------------------------------------------------
        //  Here means the reticlePod is on the RSP port.
        //  Those ReticlePod should be unloaded before xfer status change.
        //  Error return !!
        //----------------------------------------------------------------------
        Validations.check(!ObjectIdentifier.isEmpty(currentReticlePodPortID), retCodeConfig.getInvalidReticlepodXferStat(), reticlePodID, "EI or SI");

        //=========================================================================
        // Update reticle pod transfer status by using input parameter + 'newTransferStatus'
        //=========================================================================
        // Parameter "" of TimeStamp set objCommon.strTimeStamp.reportTimeStamp.
        reticleMethod.reticlePodTransferStateChange(objCommon, stockerID, equipmentID, reticlePodID, newTransferState, "", params.getClaimMemo(), null);
        Results.RSPXferStatusChangeRptResult result = new Results.RSPXferStatusChangeRptResult();
        result.setStockerID(stockerID);
        result.setEquipmentID(equipmentID);

        return result;
    }

    @Override
    public void sxReticleActionReleaseErrorReq(Infos.ObjCommon objCommon, Params.ReticleActionReleaseErrorReqParams params) {
        String tmpArhs = StandardProperties.OM_ARHS_FLAG.getValue();
        log.info("ARHS switch on / off  tmpArhs = {}", tmpArhs);
        Validations.check(!CimStringUtils.equals(BizConstant.SP_ARMS_SWITCH_ON, tmpArhs), retCodeConfig.getFunctionNotAvailable());

        String reticleDispatchJobID = params.getReticleDispatchJobID();
        ObjectIdentifier machineID = params.getMachineID();
        log.info("in-para reticleDispatchJobID: {}", reticleDispatchJobID);
        log.info("in-para machineID: {}", ObjectIdentifier.fetchValue(machineID));

        log.info("step1 - reticleMethod.reticleDispatchJobListGetDR");
        List<Infos.ReticleDispatchJob> reticleDispatchJobList = reticleMethod.reticleDispatchJobListGetDR(objCommon, reticleDispatchJobID);
        if (CimArrayUtils.isEmpty(reticleDispatchJobList)) {
            log.info("RDJ not found...RDJ ID {}", reticleDispatchJobID);
            Validations.check(true, retCodeConfigEx.getRdjNotFound());
        } else if (CimArrayUtils.getSize(reticleDispatchJobList) != 1) {
            log.info("Duplicate RDJ found.");
            Validations.check(true, retCodeConfigEx.getRdjDuplicate());
        }
        Infos.ReticleDispatchJob tmpReticleDispatchJob = reticleDispatchJobList.get(0);

        Outputs.ObjMachineTypeGetOut machineTypeGetOut = null;
        log.info("step2 - equipmentMethod.machineTypeGet");
        try {
            machineTypeGetOut = equipmentMethod.machineTypeGet(objCommon, machineID);
        } catch (ServiceException e) {
            if (Validations.isEquals(retCodeConfig.getNotFoundEqp(), e.getCode())) {
                log.info("machineID {} is not valid.", ObjectIdentifier.fetchValue(machineID));
            } else {
                throw e;
            }
        }
        String machineType = null;
        if (machineTypeGetOut != null && CimBooleanUtils.isFalse(machineTypeGetOut.isBStorageMachineFlag())) {
            log.info("bStorageMachineFlag == FALSE");
            machineType = BizConstant.SP_MACHINE_TYPE_EQP;
        } else {
            log.info("bStorageMachineFlag == TRUE");
            if (machineTypeGetOut != null) {
                machineType = machineTypeGetOut.getStockerType();
            }
        }
        log.info("This machine's type is {}", machineType);

        tmpReticleDispatchJob.setFromEquipmentID(machineID);
        tmpReticleDispatchJob.setFromEquipmentCategory(machineType);
        tmpReticleDispatchJob.setJobStatus(BizConstant.SP_RDJ_STATUS_WAITTORELEASE);
        tmpReticleDispatchJob.setJobStatusChangeTimestamp(CimDateUtils.convertToSpecString(objCommon.getTimeStamp().getReportTimeStamp()));

        log.info("step2 - reticleMethod.reticleDispatchJobUpdateDR");
        reticleMethod.reticleDispatchJobUpdateDR(objCommon, tmpReticleDispatchJob);
    }

    @Override
    public Infos.ReticleXferJob sxReticleActionReleaseReq(Infos.ObjCommon objCommon, Params.ReticleActionReleaseReqParams params) {
        Infos.ReticleXferJob result = new Infos.ReticleXferJob();
        String tmpArhs = StandardProperties.OM_ARHS_FLAG.getValue();
        if (log.isDebugEnabled()){
            log.debug("ARHS switch on / off  tmpArhs = {}", tmpArhs);
        }
        Validations.check(!CimStringUtils.equals(BizConstant.SP_ARMS_SWITCH_ON, tmpArhs), retCodeConfig.getFunctionNotAvailable());

        String reticleDispatchJobID = params.getReticleDispatchJobID();
        if (log.isDebugEnabled()){
            log.debug("in-para reticleDispatchJobID {}", reticleDispatchJobID);
        }

        //Get RDJ record from RDJ ID
        if (log.isDebugEnabled()){
            log.debug("step1 - reticleMethod.reticleDispatchJobListGetForUpdateDR");
        }
        List<Infos.ReticleDispatchJob> reticleDispatchJobList = reticleMethod.reticleDispatchJobListGetForUpdateDR(objCommon, reticleDispatchJobID);
        if (CimArrayUtils.isEmpty(reticleDispatchJobList)) {
            log.error("{} Not found specified RDJ.", reticleDispatchJobID);
            Validations.check(true, retCodeConfigEx.getRdjNotFound());
        }
        if (CimArrayUtils.getSize(reticleDispatchJobList) > 1) {
            log.error("{} RDJ ID duplicate!", reticleDispatchJobID);
            Validations.check(true, retCodeConfigEx.getRdjDuplicate());
        }

        Infos.ReticleDispatchJob strTargetReticleDispatchJob = reticleDispatchJobList.get(0);
        //Check RDJ Record
        if (!CimStringUtils.equals(strTargetReticleDispatchJob.getJobStatus(), BizConstant.SP_RDJ_STATUS_CREATED)
                && !CimStringUtils.equals(strTargetReticleDispatchJob.getJobStatus(), BizConstant.SP_RDJ_STATUS_WAITTORELEASE)) {
            log.error("{} RDJ already released.", strTargetReticleDispatchJob.getJobStatus());
            Validations.check(true, retCodeConfigEx.getRdjStatusError(), strTargetReticleDispatchJob.getJobStatus());
        }

        if (ObjectIdentifier.isEmptyWithValue(strTargetReticleDispatchJob.getToEquipmentID())
                || (ObjectIdentifier.isEmptyWithValue(strTargetReticleDispatchJob.getReticleID())
                && ObjectIdentifier.isEmptyWithValue(strTargetReticleDispatchJob.getReticlePodID()))) {
            log.error("RDJ Record is incomplete");
            Validations.check(true, retCodeConfigEx.getRdjIncomplete());
        }

        // Get ReticlePod for Xfer
        ObjectIdentifier xferReticlePodID = ObjectIdentifier.buildWithValue("");
        ObjectIdentifier drbControlJob = null;
        if (ObjectIdentifier.isEmptyWithValue(strTargetReticleDispatchJob.getReticlePodID())) {
            if (log.isDebugEnabled()){
                log.debug("ReticlePodID not specified. now search reticlePod.");
                log.debug("step2 - reticleMethod.reticleReticlePodGetForXfer");
            }
            Outputs.ReticleReticlePodGetForXferOut reticleReticlePodGetForXferOut = reticleMethod.reticleReticlePodGetForXfer(objCommon, strTargetReticleDispatchJob.getReticleID());
            if (null != reticleReticlePodGetForXferOut) {
                if (CimArrayUtils.isEmpty(reticleReticlePodGetForXferOut.getStrCandidateReticlePods())) {
                    log.error("reticleReticlePodGetForXfer cannot find suitable RSP.");
                    Validations.check(true, retCodeConfigEx.getNotFoundSuitableRsp());
                }
                xferReticlePodID = reticleReticlePodGetForXferOut.getStrCandidateReticlePods().get(0).getReticlePodID();
                if (log.isDebugEnabled()){
                    log.debug("step3 - durableMethod.durableDurableControlJobIDGet");
                }
                drbControlJob = durableMethod.durableDurableControlJobIDGet(objCommon, strTargetReticleDispatchJob.getReticleID(), BizConstant.SP_DURABLECAT_RETICLE);
            }
        } else {
            if (log.isDebugEnabled()){
                log.debug("ReticlePodID {} specified.", ObjectIdentifier.fetchValue(strTargetReticleDispatchJob.getReticlePodID()));
            }
            xferReticlePodID = strTargetReticleDispatchJob.getReticlePodID();
            if (log.isDebugEnabled()){
                log.debug("step4 - durableMethod.durableDurableControlJobIDGet");
            }
            drbControlJob = durableMethod.durableDurableControlJobIDGet(objCommon, xferReticlePodID, BizConstant.SP_DURABLECAT_RETICLEPOD);
        }

        if (ObjectIdentifier.isNotEmptyWithValue(drbControlJob)) {
            if (log.isDebugEnabled()){
                log.debug("durableControlJobID {} is not blank", ObjectIdentifier.fetchValue(drbControlJob));
                log.debug("step5 - objectLockMethod.objectLock");
            }
            objectLockMethod.objectLock(objCommon, CimDurableControlJob.class, drbControlJob);

            if (log.isDebugEnabled()){
                log.debug("step6 - durableMethod.durableControlJobStatusGet");
            }
            Infos.DurableControlJobStatusGet durableControlJobStatusGetOut = durableMethod.durableControlJobStatusGet(objCommon, drbControlJob);
            if (!CimStringUtils.equals(durableControlJobStatusGetOut.getDurableControlJobStatus(), BizConstant.SP_DURABLECONTROLJOBSTATUS_CREATED)
                    && !CimStringUtils.equals(durableControlJobStatusGetOut.getDurableControlJobStatus(), BizConstant.SP_DURABLECONTROLJOBSTATUS_DELETE)) {
                Validations.check(true, retCodeConfig.getInvalidDcjstatus(), durableControlJobStatusGetOut.getDurableControlJobStatus());
            }
        }
        // Lock ReticlePod to check
        if (log.isDebugEnabled()){
            log.debug("step7 - objectLockMethod.objectLock");
        }
        objectLockMethod.objectLock(objCommon, CimReticlePod.class, xferReticlePodID);

        //decomposite RDJ
        if (log.isDebugEnabled()){
            log.debug("step8 - reticleMethod.reticleComponentJobCreate");
        }
        Outputs.ReticleComponentJobCreateOut reticleComponentJobCreateOut = reticleMethod.reticleComponentJobCreate(objCommon,
                reticleDispatchJobID,
                strTargetReticleDispatchJob.getRequestUserID(),
                strTargetReticleDispatchJob.getPriority(),
                strTargetReticleDispatchJob.getReticleID(),
                xferReticlePodID,
                strTargetReticleDispatchJob.getToEquipmentID());

        Infos.ReticleDispatchJob reticleDispatchJob = strTargetReticleDispatchJob;
        reticleDispatchJob.setReticlePodID(reticleComponentJobCreateOut.getStrReticleDispatchJob().getReticlePodID());
        reticleDispatchJob.setFromEquipmentID(reticleComponentJobCreateOut.getStrReticleDispatchJob().getFromEquipmentID());
        reticleDispatchJob.setFromEquipmentCategory(reticleComponentJobCreateOut.getStrReticleDispatchJob().getFromEquipmentCategory());
        reticleDispatchJob.setJobStatus(reticleComponentJobCreateOut.getStrReticleDispatchJob().getJobStatus());
        reticleDispatchJob.setJobStatusChangeTimestamp(reticleComponentJobCreateOut.getStrReticleDispatchJob().getJobStatusChangeTimestamp());

        if (log.isDebugEnabled()){
            log.debug("step9 - reticleMethod.reticleDispatchJobUpdateDR");
        }
        reticleMethod.reticleDispatchJobUpdateDR(objCommon, reticleDispatchJob);

        if (log.isDebugEnabled()){
            log.debug("step10 - reticleMethod.reticleComponentJobInsertDR");
        }
        reticleMethod.reticleComponentJobInsertDR(objCommon, reticleComponentJobCreateOut.getStrReticleComponentJobList());

        if (ObjectIdentifier.isNotEmptyWithValue(reticleComponentJobCreateOut.getStrReticleDispatchJob().getReticleID())) {
            if (log.isDebugEnabled()){
                log.debug("step11 - reticleMethod.reticleReticlePodReserve");
            }
            reticleMethod.reticleReticlePodReserve(objCommon,
                    reticleComponentJobCreateOut.getStrReticleDispatchJob().getReticleID(),
                    reticleComponentJobCreateOut.getStrReticleDispatchJob().getReticlePodID(),
                    reticleComponentJobCreateOut.getStrReticleDispatchJob().getToEquipmentID());
        }

        if (log.isDebugEnabled()){
            log.debug("step12 - reticleMethod.reticleEventQueuePutDR");
        }
        reticleMethod.reticleEventQueuePutDR(objCommon, reticleComponentJobCreateOut.getStrReticleEventRecord());
        return result;
    }

    @Override
    public void sxReticleDispatchJobCancelReq(Infos.ObjCommon objCommon, Params.ReticleDispatchJobCancelReqParams params) {
        String tmpArhs = StandardProperties.OM_ARHS_FLAG.getValue();
        if (log.isDebugEnabled())
            log.debug("ARHS switch on / off  tmpArhs = {}", tmpArhs);
        Validations.check(!CimStringUtils.equals(BizConstant.SP_ARMS_SWITCH_ON, tmpArhs), retCodeConfig.getFunctionNotAvailable());

        String dispatchJobID = params.getDispatchJobID();
        Validations.check(CimStringUtils.isEmpty(dispatchJobID), retCodeConfig.getInvalidParameter());

        /*-----------------------------------*/
        /*   Get Reticle Dispatch Job list   */
        /*-----------------------------------*/
        log.debug("step1 - reticleMethod.reticleDispatchJobListGetForUpdateDR");
        List<Infos.ReticleDispatchJob> reticleDispatchJobList = reticleMethod.reticleDispatchJobListGetForUpdateDR(objCommon, dispatchJobID);
        if (CimArrayUtils.isEmpty(reticleDispatchJobList)) {
            log.error("Not RDJ is found. | in-para dispatchJobID {}", dispatchJobID);
            Validations.check(true, retCodeConfigEx.getRdjNotFound());
        } else if (CimArrayUtils.getSize(reticleDispatchJobList) > 1) {
            log.error("Duplicate RDJ. | dispatchJobID {}", dispatchJobID);
            Validations.check(true, retCodeConfigEx.getRdjDuplicate());
        }

        /*------------------------------------*/
        /*   Get Reticle Component Job list   */
        /*------------------------------------*/
        log.debug("step2 - reticleMethod.reticleComponentJobListGetDR");
        List<Infos.ReticleComponentJob> reticleComponentJobList = reticleMethod.reticleComponentJobListGetDR(objCommon, dispatchJobID);

        /*--------------------*/
        /*   Delete RDJ/RCJ   */
        /*--------------------*/
        if (CimArrayUtils.isEmpty(reticleComponentJobList)) {
            if (CimStringUtils.equals(reticleDispatchJobList.get(0).getJobStatus(), BizConstant.SP_RDJ_STATUS_CREATED)
                    || CimStringUtils.equals(reticleDispatchJobList.get(0).getJobStatus(), BizConstant.SP_RDJ_STATUS_WAITTORELEASE)) {
                log.debug("jobStatus = RDJ_STATUS_CREATED || jobStatus = RDJ_STATUS_WAITTORELEASE (RCJ is not exist).");
                //Delete RDJ
                log.debug("step3 - reticleMethod.reticleDispatchJobDeleteDR");
                reticleMethod.reticleDispatchJobDeleteDR(objCommon,
                        dispatchJobID);
            } else {
                log.error("Status error. Does not match RDJ/RCJ Status.");
                Validations.check(true, retCodeConfigEx.getMismatchRdjrcjStatus());
            }
        } else {
            log.debug("Found RCJ list");
            if (!CimStringUtils.equals(reticleDispatchJobList.get(0).getJobStatus(), BizConstant.SP_RDJ_STATUS_EXECUTING)) {
                if (log.isDebugEnabled())
                    log.debug("JobStatus is not Executing. Delete RDJ/RCJ List. | jobStatus {}", reticleDispatchJobList.get(0).getJobStatus());
                //Delete RDJ
                log.debug("step4 - reticleMethod.reticleDispatchJobDeleteDR");
                reticleMethod.reticleDispatchJobDeleteDR(objCommon, dispatchJobID);

                //Delete RCJ
                log.debug("step5 - reticleMethod.reticleComponentJobDeleteDR");
                reticleMethod.reticleComponentJobDeleteDR(objCommon, dispatchJobID);

                if (ObjectIdentifier.isNotEmptyWithValue(reticleDispatchJobList.get(0).getReticleID())) {
                    if (log.isDebugEnabled())
                        log.debug("reticleID: {}", ObjectIdentifier.fetchValue(reticleDispatchJobList.get(0).getReticleID()));
                    log.debug("step6 - reticleMethod.reticleReticlePodReserveCancel");
                    reticleMethod.reticleReticlePodReserveCancel(objCommon,
                            reticleDispatchJobList.get(0).getReticleID(),
                            false);
                }
            } else {
                if (log.isDebugEnabled())
                    log.debug("JobStatus is Executing. Change RDJ/RCJ status {} to 'Error'.", reticleDispatchJobList.get(0).getJobStatus());
                /**************************************/
                /*  EAP environment variable setting  */
                /**************************************/
                String tmpSleepTimeValue = StandardProperties.OM_EAP_CONNECT_SLEEP_TIME.getValue();
                String tmpRetryCountValue = StandardProperties.OM_EAP_CONNECT_RETRY_COUNT.getValue();

                Long sleepTimeValue = CimObjectUtils.isEmpty(tmpSleepTimeValue) ? CimNumberUtils.longValue(BizConstant.SP_DEFAULT_SLEEP_TIME_TCS) : Integer.valueOf(tmpSleepTimeValue);
                Long retryCountValue = CimObjectUtils.isEmpty(tmpRetryCountValue) ? CimNumberUtils.longValue(BizConstant.SP_DEFAULT_RETRY_COUNT_TCS) : Integer.valueOf(tmpRetryCountValue);

                if (log.isDebugEnabled()){
                    log.debug("env value of OM_EAP_CONNECT_SLEEP_TIME  = {}", retryCountValue);
                    log.debug("env value of OM_EAP_CONNECT_RETRY_COUNT  = {}", sleepTimeValue);
                }
                Boolean critialErrorFlag = false;

                //find executing job in RCJ list
                Infos.ReticleComponentJob strReticleComponentExJob = new Infos.ReticleComponentJob();
                if (CimArrayUtils.isNotEmpty(reticleComponentJobList)) {
                    for (Infos.ReticleComponentJob reticleComponentJob : reticleComponentJobList) {
                        if (CimStringUtils.equals(reticleComponentJob.getJobStatus(), BizConstant.SP_RCJ_STATUS_EXECUTING)) {
                            log.debug("Executing RCJ found!");
                            strReticleComponentExJob = reticleComponentJob;
                            break;
                        }
                    }
                }
                Boolean bDeleteFlag = true;


                Infos.ReticleComponentJob strReticleComponentJobUpdate = new Infos.ReticleComponentJob();
                //case Store
                if (CimStringUtils.equals(strReticleComponentExJob.getJobName(), BizConstant.SP_RCJ_JOBNAME_STORE)) {
                    if (log.isDebugEnabled())
                        log.debug("Running job {} is Store job. Store cancel request to EAP", strReticleComponentExJob.getJobName());
                    //Store Cancel request to EAP
                    Infos.MoveReticles moveReticle = new Infos.MoveReticles();
                    moveReticle.setReticleID(strReticleComponentExJob.getReticleID());
                    moveReticle.setSlotNumber(strReticleComponentExJob.getSlotNo().intValue());

                    Boolean rcOK = true;
                    for (long i = 0; i < (retryCountValue + 1); i++) {
                        rcOK = true;
                        Inputs.SendReticleStoreCancelReqIn sendReticleStoreCancelReqIn = new Inputs.SendReticleStoreCancelReqIn();
                        sendReticleStoreCancelReqIn.setEquipmentID(strReticleComponentExJob.getToEquipmentID());
                        sendReticleStoreCancelReqIn.setPortID(strReticleComponentExJob.getToReticlePodPortID());
                        sendReticleStoreCancelReqIn.setReticlePodID(strReticleComponentExJob.getReticlePodID());
                        sendReticleStoreCancelReqIn.setStrMoveReticles(Collections.singletonList(moveReticle));
                        log.debug("step7 - tcsMethod.sendReticleStoreCancelReq");
                        try {
//                            tcsMethod.sendReticleStoreCancelReq(TCSReqEnum.sendReticleStoreCancelReq, sendReticleStoreCancelReqIn);
                            log.debug("Now EAP subSystem is alive!! Go ahead");
                            break;
                        } catch (ServiceException e) {
                            if (Validations.isEquals(retCodeConfig.getExtServiceBindFail(), e.getCode())
                                    || Validations.isEquals(retCodeConfig.getExtServiceNilObj(), e.getCode())
                                    || Validations.isEquals(retCodeConfig.getTcsNoResponse(), e.getCode())) {
                                log.debug("EAP subsystem has return NO_RESPONSE!! just retry now!!  now count...", i);
                                log.debug("now sleeping... ", sleepTimeValue);
                                try {
                                    Thread.sleep(CimNumberUtils.longValue(sleepTimeValue));
                                } catch (InterruptedException ex) {
                                    Thread.currentThread().interrupt();
                                    log.error(ex.getMessage(), ex);
                                }
                                rcOK = false;
                                continue;
                            } else {
                                critialErrorFlag = true;
                                break;
                            }
                        }
                    }
                    if (CimBooleanUtils.isFalse(rcOK) || CimBooleanUtils.isTrue(critialErrorFlag)) {
                        bDeleteFlag = false;
                        Infos.ReticleDispatchJob reticleDispatchJob = new Infos.ReticleDispatchJob();
                        reticleDispatchJob.setJobStatus(BizConstant.SP_RDJ_STATUS_ERROR);
                        reticleDispatchJob.setJobStatusChangeTimestamp(CimDateUtils.convertToSpecString(objCommon.getTimeStamp().getReportTimeStamp()));
                        log.debug("step8 - reticleMethod.reticleDispatchJobUpdateDR");
                        reticleMethod.reticleDispatchJobUpdateDR(objCommon, reticleDispatchJob);

                        //RCJ status change error
                        strReticleComponentJobUpdate = strReticleComponentExJob;
                        strReticleComponentJobUpdate.setJobStatus(BizConstant.SP_RCJ_STATUS_ERROR);
                        strReticleComponentJobUpdate.setJobStatusChangeTimestamp(CimDateUtils.convertToSpecString(objCommon.getTimeStamp().getReportTimeStamp()));

                        log.debug("step9 - reticleMethod.reticleComponentJobUpdateDR");
                        reticleMethod.reticleComponentJobUpdateDR(objCommon, strReticleComponentJobUpdate);
                    }
                }
                //case Retrieve、
                else if (CimStringUtils.equals(strReticleComponentExJob.getJobName(), BizConstant.SP_RCJ_JOBNAME_RETRIEVE)) {
                    if (log.isDebugEnabled())
                        log.debug("Running job {} is Retrieve job. Retrieve cancel request to EAP", strReticleComponentExJob.getJobName());
                    //Retrieve Cancel request to EAP
                    Infos.MoveReticles moveReticle = new Infos.MoveReticles();
                    moveReticle.setReticleID(strReticleComponentExJob.getReticleID());
                    moveReticle.setSlotNumber(strReticleComponentExJob.getSlotNo().intValue());

                    Boolean rcOK = true;
                    for (long i = 0; i < (retryCountValue + 1); i++) {
                        rcOK = true;
                        Inputs.SendReticleRetrieveCancelReqIn sendReticleRetrieveCancelReqIn = new Inputs.SendReticleRetrieveCancelReqIn();
                        sendReticleRetrieveCancelReqIn.setEquipmentID(strReticleComponentExJob.getToEquipmentID());
                        sendReticleRetrieveCancelReqIn.setPortID(strReticleComponentExJob.getToReticlePodPortID());
                        sendReticleRetrieveCancelReqIn.setReticlePodID(strReticleComponentExJob.getReticlePodID());
                        sendReticleRetrieveCancelReqIn.setStrMoveReticles(Collections.singletonList(moveReticle));
                        log.debug("step10 - tcsMethod.sendReticleRetrieveCancelReq");
                        try {
//                            tcsMethod.sendReticleRetrieveCancelReq(TCSReqEnum.sendReticleRetrieveCancelReq, sendReticleRetrieveCancelReqIn);
                            log.debug("Now EAP subSystem is alive!! Go ahead");
                            break;
                        } catch (ServiceException e) {
                            if (Validations.isEquals(retCodeConfig.getExtServiceBindFail(), e.getCode())
                                    || Validations.isEquals(retCodeConfig.getExtServiceNilObj(), e.getCode())
                                    || Validations.isEquals(retCodeConfig.getTcsNoResponse(), e.getCode())) {
                                log.debug("EAP subsystem has return NO_RESPONSE!! just retry now!!  now count...", i);
                                log.debug("now sleeping... ", sleepTimeValue);
                                try {
                                    Thread.sleep(CimNumberUtils.longValue(sleepTimeValue));
                                } catch (InterruptedException ex) {
                                    Thread.currentThread().interrupt();
                                    log.error(ex.getMessage(), ex);
                                }
                                rcOK = false;
                                continue;
                            } else {
                                critialErrorFlag = true;
                                break;
                            }
                        }
                    }
                    if (CimBooleanUtils.isFalse(rcOK) || CimBooleanUtils.isTrue(critialErrorFlag)) {
                        bDeleteFlag = false;
                        Infos.ReticleDispatchJob reticleDispatchJob = new Infos.ReticleDispatchJob();
                        reticleDispatchJob.setJobStatus(BizConstant.SP_RDJ_STATUS_ERROR);
                        reticleDispatchJob.setJobStatusChangeTimestamp(CimDateUtils.convertToSpecString(objCommon.getTimeStamp().getReportTimeStamp()));
                        log.debug("step11 - reticleMethod.reticleDispatchJobUpdateDR");
                        reticleMethod.reticleDispatchJobUpdateDR(objCommon, reticleDispatchJob);

                        //RCJ status change error
                        strReticleComponentJobUpdate = strReticleComponentExJob;
                        strReticleComponentJobUpdate.setJobStatus(BizConstant.SP_RCJ_STATUS_ERROR);
                        strReticleComponentJobUpdate.setJobStatusChangeTimestamp(CimDateUtils.convertToSpecString(objCommon.getTimeStamp().getReportTimeStamp()));

                        log.debug("step12 - reticleMethod.reticleComponentJobUpdateDR");
                        reticleMethod.reticleComponentJobUpdateDR(objCommon, strReticleComponentJobUpdate);
                    }
                }
                //case Unclamp
                else if (CimStringUtils.equals(strReticleComponentExJob.getJobName(), BizConstant.SP_RCJ_JOBNAME_UNCLAMP)) {
                    if (log.isDebugEnabled())
                        log.debug("Running job {} is Retrieve job. Retrieve cancel request to EAP", strReticleComponentExJob.getJobName());
                    //Retrieve Cancel request to EAP

                    Boolean rcOK = true;
                    for (long i = 0; i < (retryCountValue + 1); i++) {
                        rcOK = true;
                        Inputs.SendReticlePodUnclampCancelReqIn sendReticlePodUnclampCancelReqIn = new Inputs.SendReticlePodUnclampCancelReqIn();
                        sendReticlePodUnclampCancelReqIn.setEquipmentID(strReticleComponentExJob.getToEquipmentID());
                        sendReticlePodUnclampCancelReqIn.setPortID(strReticleComponentExJob.getToReticlePodPortID());
                        sendReticlePodUnclampCancelReqIn.setReticlePodID(strReticleComponentExJob.getReticlePodID());
                        log.debug("step13 - tcsMethod.sendReticlePodUnclampCancelReq");
                        try {
//                            tcsMethod.sendReticlePodUnclampCancelReq(TCSReqEnum.sendReticlePodUnclampCancelReq, sendReticlePodUnclampCancelReqIn);
                            log.debug("Now EAP subSystem is alive!! Go ahead");
                            break;
                        } catch (ServiceException e) {
                            if (Validations.isEquals(retCodeConfig.getExtServiceBindFail(), e.getCode())
                                    || Validations.isEquals(retCodeConfig.getExtServiceNilObj(), e.getCode())
                                    || Validations.isEquals(retCodeConfig.getTcsNoResponse(), e.getCode())) {
                                log.debug("EAP subsystem has return NO_RESPONSE!! just retry now!!  now count...", i);
                                log.debug("now sleeping... ", sleepTimeValue);
                                try {
                                    Thread.sleep(CimNumberUtils.longValue(sleepTimeValue));
                                } catch (InterruptedException ex) {
                                    Thread.currentThread().interrupt();
                                    log.error(ex.getMessage(), ex);
                                }
                                rcOK = false;
                                continue;
                            } else {
                                critialErrorFlag = true;
                                break;
                            }
                        }
                    }
                    if (CimBooleanUtils.isFalse(rcOK) || CimBooleanUtils.isTrue(critialErrorFlag)) {
                        bDeleteFlag = false;
                        Infos.ReticleDispatchJob reticleDispatchJob = new Infos.ReticleDispatchJob();
                        reticleDispatchJob.setJobStatus(BizConstant.SP_RDJ_STATUS_ERROR);
                        reticleDispatchJob.setJobStatusChangeTimestamp(CimDateUtils.convertToSpecString(objCommon.getTimeStamp().getReportTimeStamp()));
                        log.debug("step14 - reticleMethod.reticleDispatchJobUpdateDR");
                        reticleMethod.reticleDispatchJobUpdateDR(objCommon, reticleDispatchJob);

                        //RCJ status change error
                        strReticleComponentJobUpdate = strReticleComponentExJob;
                        strReticleComponentJobUpdate.setJobStatus(BizConstant.SP_RCJ_STATUS_ERROR);
                        strReticleComponentJobUpdate.setJobStatusChangeTimestamp(CimDateUtils.convertToSpecString(objCommon.getTimeStamp().getReportTimeStamp()));

                        log.debug("step15 - reticleMethod.reticleComponentJobUpdateDR");
                        reticleMethod.reticleComponentJobUpdateDR(objCommon, strReticleComponentJobUpdate);
                    }
                }
                //case Xfer
                else if (CimStringUtils.equals(strReticleComponentExJob.getJobName(), BizConstant.SP_RCJ_JOBNAME_XFER)) {
                    log.debug("jobName = RCJ_JOBNAME_XFER");
                    List<Infos.ReticlePodPortInfo> strReticlePodPortInfo = new ArrayList<>();
                    Long fromMachineType = 0L;

                    log.debug("step16 - equipmentMethod.machineTypeGet");
                    Outputs.ObjMachineTypeGetOut machineTypeGetOut = equipmentMethod.machineTypeGet(objCommon, strReticleComponentExJob.getFromEquipmentID());
                    if (CimBooleanUtils.isFalse(machineTypeGetOut.isBStorageMachineFlag())) {
                        if (log.isDebugEnabled())
                            log.debug("fromMachine type is EQP {}", ObjectIdentifier.fetchValue(strReticleComponentExJob.getFromEquipmentID()));
                        fromMachineType = 1L;

                        log.debug("step17 - equipmentMethod.equipmentReticlePodPortInfoGetDR");
                        Outputs.ObjEquipmentReticlePodPortInfoGetDROut equipmentReticlePodPortInfoGetDROut = equipmentMethod.equipmentReticlePodPortInfoGetDR(objCommon, strReticleComponentExJob.getFromEquipmentID());
                        strReticlePodPortInfo = equipmentReticlePodPortInfoGetDROut.getReticlePodPortInfoList();
                    } else if (CimStringUtils.equals(machineTypeGetOut.getStockerType(), BizConstant.SP_STOCKER_TYPE_BARERETICLE)) {
                        if (log.isDebugEnabled())
                            log.debug("fromMachine type is BRSTK {}", ObjectIdentifier.fetchValue(strReticleComponentExJob.getFromEquipmentID()));
                        fromMachineType = 2L;

                        log.debug("step18 - stockerMethod.stockerReticlePodPortInfoGetDR");
                        List<Infos.ReticlePodPortInfo> reticlePodPortInfoList = stockerMethod.stockerReticlePodPortInfoGetDR(objCommon, strReticleComponentExJob.getFromEquipmentID());
                        strReticlePodPortInfo = reticlePodPortInfoList;
                    } else if (CimStringUtils.equals(machineTypeGetOut.getStockerType(), BizConstant.SP_STOCKER_TYPE_RETICLEPOD)) {
                        if (log.isDebugEnabled())
                            log.debug("fromMachine {} type is RSPSTK.", ObjectIdentifier.fetchValue(strReticleComponentExJob.getFromEquipmentID()));
                        fromMachineType = 3L;
                    } else {
                        log.error("{} machine {} type {} is invalid.", ObjectIdentifier.fetchValue(strReticleComponentExJob.getFromEquipmentID()), machineTypeGetOut.getStockerType());
                        Validations.check(true, retCodeConfig.getInvalidStockerType(), ObjectIdentifier.fetchValue(strReticleComponentExJob.getFromEquipmentID()));
                    }
                    Long toMachineType = 0L;
                    log.debug("step19 - equipmentMethod.machineTypeGet");
                    Outputs.ObjMachineTypeGetOut toMachineTypeGetOut = equipmentMethod.machineTypeGet(objCommon, strReticleComponentExJob.getToEquipmentID());
                    if (CimBooleanUtils.isFalse(toMachineTypeGetOut.isBStorageMachineFlag())) {
                        if (log.isDebugEnabled())
                            log.debug("toMachine {} type is EQP", ObjectIdentifier.fetchValue(strReticleComponentExJob.getToEquipmentID()));
                        toMachineType = 1L;
                    } else if (CimStringUtils.equals(toMachineTypeGetOut.getStockerType(), BizConstant.SP_STOCKER_TYPE_BARERETICLE)) {
                        if (log.isDebugEnabled())
                            log.debug("toMachine {} type is BRSTK", ObjectIdentifier.fetchValue(strReticleComponentExJob.getToEquipmentID()));
                        toMachineType = 2L;
                    } else if (CimStringUtils.equals(toMachineTypeGetOut.getStockerType(), BizConstant.SP_STOCKER_TYPE_RETICLEPOD)) {
                        if (log.isDebugEnabled())
                            log.debug("toMachine {} type is RSPSTK.", ObjectIdentifier.fetchValue(strReticleComponentExJob.getToEquipmentID()));
                        toMachineType = 3L;
                    } else {
                        log.error("{} machine {} type {} is invalid.", ObjectIdentifier.fetchValue(strReticleComponentExJob.getToEquipmentID()), machineTypeGetOut.getStockerType());
                        Validations.check(true, retCodeConfig.getInvalidStockerType(), ObjectIdentifier.fetchValue(strReticleComponentExJob.getToEquipmentID()));
                    }

                    log.debug("Running job is Xfer job. Xfer cancel request to RTMS");
                    Inputs.SendTransportJobCancelReqIn sendTransportJobCancelReqIn = new Inputs.SendTransportJobCancelReqIn();
                    Infos.TranJobCancelReq tranJobCancelReq = new Infos.TranJobCancelReq();
                    Infos.CarrierJob carrierJob = new Infos.CarrierJob();
                    carrierJob.setCarrierID(strReticleComponentExJob.getReticlePodID());
                    tranJobCancelReq.setCarrierJobData(Collections.singletonList(carrierJob));

                    //【step-5】TMSMgr_SendTransportJobCancelReq
                    sendTransportJobCancelReqIn.setStrObjCommonIn(objCommon);
                    sendTransportJobCancelReqIn.setTranJobCancelReq(tranJobCancelReq);
                    sendTransportJobCancelReqIn.setUser(objCommon.getUser());
                    log.debug("step20 - tmsService.rtransportJobCancelReq");
                    try {
                        Outputs.SendTransportJobCancelReqOut sendTransportJobCancelReqOut = tmsService.rtransportJobCancelReq(sendTransportJobCancelReqIn);
                    } catch (ServiceException e) {
                        bDeleteFlag = false;
                        //RDJ status change error
                        Infos.ReticleDispatchJob reticleDispatchJob = new Infos.ReticleDispatchJob();
                        reticleDispatchJob.setJobStatus(BizConstant.SP_RCJ_STATUS_ERROR);
                        reticleDispatchJob.setJobStatusChangeTimestamp(CimDateUtils.convertToSpecString(objCommon.getTimeStamp().getReportTimeStamp()));
                        log.debug("step21 - reticleMethod.reticleDispatchJobUpdateDR");
                        reticleMethod.reticleDispatchJobUpdateDR(objCommon, reticleDispatchJob);

                        //RCJ status change error
                        strReticleComponentJobUpdate = strReticleComponentExJob;
                        strReticleComponentJobUpdate.setJobStatus(BizConstant.SP_RCJ_STATUS_ERROR);
                        strReticleComponentJobUpdate.setJobStatusChangeTimestamp(CimDateUtils.convertToSpecString(objCommon.getTimeStamp().getReportTimeStamp()));

                        log.debug("step22 - reticleMethod.reticleComponentJobUpdateDR");
                        reticleMethod.reticleComponentJobUpdateDR(objCommon, strReticleComponentJobUpdate);
                    }
                    if (fromMachineType == 1L || fromMachineType == 2L) {
                        log.debug("fromMachineType == 1 || fromMachineType == 2");
                        Boolean bNotReleaseFlag = false;
                        if (CimArrayUtils.isNotEmpty(strReticlePodPortInfo)) {
                            for (Infos.ReticlePodPortInfo reticlePodPortInfo : strReticlePodPortInfo) {
                                if (ObjectIdentifier.equalsWithValue(reticlePodPortInfo.getReticlePodPortID(), strReticleComponentExJob.getFromReticlePodPortID())
                                        && !ObjectIdentifier.equalsWithValue(reticlePodPortInfo.getReservedReticlePodID(), strReticleComponentExJob.getReticlePodID())) {
                                    if (log.isDebugEnabled())
                                        log.debug("From Port is already reserved for other reticlePod {}.", ObjectIdentifier.fetchValue(reticlePodPortInfo.getReservedReticlePodID()));
                                    bNotReleaseFlag = true;
                                    break;
                                }
                            }
                        }
                        if (CimBooleanUtils.isFalse(bNotReleaseFlag)) {
                            //Release from Port reserve
                            log.debug("bNotReleaseFlag == FALSE");
                            log.debug("step23 - equipmentForDurableMethod.machineReticlePodPortReserveCancel");
                            equipmentForDurableMethod.machineReticlePodPortReserveCancel(objCommon, strReticleComponentExJob.getFromEquipmentID(), strReticleComponentExJob.getFromReticlePodPortID());
                        }
                    }
                    if (toMachineType == 1L || toMachineType == 2L) {
                        log.debug("toMachineType == 1 || toMachineType == 2");
                        //Release to Port reserve
                        log.debug("step24 - equipmentForDurableMethod.machineReticlePodPortReserveCancel");
                        equipmentForDurableMethod.machineReticlePodPortReserveCancel(objCommon, strReticleComponentExJob.getToEquipmentID(), strReticleComponentExJob.getToReticlePodPortID());
                    }
                    //Release reticlePod transfer reserve
                    log.debug("step25 - reticleMethod.reticlePodTransferReserveCancel");
                    reticleMethod.reticlePodTransferReserveCancel(objCommon, strReticleComponentExJob.getReticlePodID());
                }
                if (CimBooleanUtils.isTrue(bDeleteFlag)) {
                    //Delete RDJ/RCJ
                    log.debug("BDeleteFlag = TRUE");
                    log.debug("step26 - reticleMethod.reticleDispatchJobDeleteDR");
                    reticleMethod.reticleDispatchJobDeleteDR(objCommon, dispatchJobID);

                    log.debug("step27 - reticleMethod.reticleComponentJobDeleteDR");
                    reticleMethod.reticleComponentJobDeleteDR(objCommon, dispatchJobID);

                    if (ObjectIdentifier.isNotEmptyWithValue(reticleDispatchJobList.get(0).getReticleID())) {

                        log.debug("step28 - reticleMethod.reticleReticlePodReserveCancel");
                        reticleMethod.reticleReticlePodReserveCancel(objCommon, reticleDispatchJobList.get(0).getReticleID(), false);
                    }
                }
            }
        }
    }

    @Override
    public void sxReticleComponentJobRetryReq(Infos.ObjCommon objCommon, Params.ReticleComponentJobRetryReqParams params) {
        String tmpArhs = StandardProperties.OM_ARHS_FLAG.getValue();
        if (log.isDebugEnabled())
            log.debug("ARHS switch on / off  tmpArhs = {}", tmpArhs);
        Validations.check(!CimStringUtils.equals(BizConstant.SP_ARMS_SWITCH_ON, tmpArhs), retCodeConfig.getFunctionNotAvailable());

        String reticleDispatchJobID = params.getReticleDispatchJobID();
        String reticleComponentJobID = params.getReticleComponentJobID();
        Validations.check(CimStringUtils.isEmpty(reticleDispatchJobID) || CimStringUtils.isEmpty(reticleComponentJobID), retCodeConfig.getInvalidParameter());

        /*-----------------------------------*/
        /*   Get Reticle Dispatch Job list   */
        /*-----------------------------------*/
        log.debug("step1 - reticleMethod.reticleDispatchJobListGetDR");
        List<Infos.ReticleDispatchJob> reticleDispatchJobList = reticleMethod.reticleDispatchJobListGetDR(objCommon, reticleDispatchJobID);
        if (CimArrayUtils.isEmpty(reticleDispatchJobList)) {
            log.error("RDJ not found | reticleDispatchJobID {}", reticleDispatchJobID);
            Validations.check(true, retCodeConfigEx.getRdjNotFound());
        } else if (CimArrayUtils.getSize(reticleDispatchJobList) > 1) {
            log.error("Duplicate RDJ | reticleDispatchJobID {}", reticleDispatchJobID);
            Validations.check(true, retCodeConfigEx.getRdjDuplicate());
        }

        if (!CimStringUtils.equals(reticleDispatchJobList.get(0).getJobStatus(), BizConstant.SP_RDJ_STATUS_EXECUTING)
                && !CimStringUtils.equals(reticleDispatchJobList.get(0).getJobStatus(), BizConstant.SP_RDJ_STATUS_ERROR)) {
            log.error("Invalid job status {} for retry.", reticleDispatchJobList.get(0).getJobStatus());
            Validations.check(true, retCodeConfigEx.getRdjStatusError(), reticleDispatchJobList.get(0).getJobStatus());
        }

        /*------------------------------------*/
        /*   Get Reticle Component Job List   */
        /*------------------------------------*/
        log.debug("step2 - reticleMethod.reticleComponentJobListGetDR");
        List<Infos.ReticleComponentJob> reticleComponentJobList = reticleMethod.reticleComponentJobListGetDR(objCommon, reticleDispatchJobID);
        if (CimArrayUtils.isEmpty(reticleComponentJobList)) {
            log.error("Not found RCJ jobs.");
            Validations.check(true, retCodeConfigEx.getRcjNotFound());
        }
        Infos.ReticleComponentJob strReticleComponentUpdateJob = new Infos.ReticleComponentJob();
        Boolean foundFlag = false;
        for (Infos.ReticleComponentJob reticleComponentJob : reticleComponentJobList) {
            if (CimStringUtils.equals(reticleComponentJob.getReticleComponentJobID(), reticleComponentJobID)
                    && !CimStringUtils.equals(reticleComponentJob.getJobStatus(), reticleDispatchJobList.get(0).getJobStatus())) {
                log.error("Mis-match RDJ/RCJ jobStatus. RDJ {} : RCJ {}",
                        reticleDispatchJobList.get(0).getJobStatus(),
                        reticleComponentJob.getJobStatus());
                Validations.check(true, retCodeConfigEx.getMismatchRdjrcjStatus());
            } else if (CimStringUtils.equals(reticleComponentJob.getReticleComponentJobID(), reticleComponentJobID)
                    && CimStringUtils.equals(reticleComponentJob.getJobStatus(), reticleDispatchJobList.get(0).getJobStatus())) {
                log.debug("Found RCJ for update");
                strReticleComponentUpdateJob = reticleComponentJob;
                foundFlag = true;
                break;
            } else {
                log.debug("Do nothing");
            }
        }
        Validations.check(CimBooleanUtils.isFalse(foundFlag), retCodeConfigEx.getRcjNotFound());

        /*--------------------*/
        /*   Update RDJ/RCJ   */
        /*--------------------*/
        Infos.ReticleDispatchJob strReticleDispatchJob = reticleDispatchJobList.get(0);
        strReticleDispatchJob.setJobStatus(BizConstant.SP_RDJ_STATUS_WAITTOEXECUTE);
        strReticleDispatchJob.setJobStatusChangeTimestamp(CimDateUtils.convertToSpecString(objCommon.getTimeStamp().getReportTimeStamp()));
        log.debug("step3 - reticleMethod.reticleDispatchJobUpdateDR");
        reticleMethod.reticleDispatchJobUpdateDR(objCommon, strReticleDispatchJob);

        Infos.ReticleComponentJob strReticleComponentJob = strReticleComponentUpdateJob;
        strReticleComponentJob.setJobStatus(BizConstant.SP_RCJ_STATUS_WAITTOEXECUTE);
        strReticleComponentJob.setJobStatusChangeTimestamp(CimDateUtils.convertToSpecString(objCommon.getTimeStamp().getReportTimeStamp()));
        log.debug("step4 - reticleMethod.reticleComponentJobUpdateDR");
        reticleMethod.reticleComponentJobUpdateDR(objCommon, strReticleComponentJob);

        /*---------------------------------------------------------*/
        /*   If retry job is 'Xfer', Cancel transfer reservation   */
        /*---------------------------------------------------------*/
        if (CimStringUtils.equals(strReticleComponentJob.getJobName(), BizConstant.SP_RCJ_JOBNAME_XFER)) {
            log.debug("Retry job is Xfer. Cancel transfer reserve.");
            List<Infos.ReticlePodPortInfo> strReticlePodPortInfo = new ArrayList<>();
            Long fromMachineType = 0L;
            log.debug("step5 - equipmentMethod.machineTypeGet");
            Outputs.ObjMachineTypeGetOut machineTypeGetOut = equipmentMethod.machineTypeGet(objCommon, strReticleComponentJob.getFromEquipmentID());
            if (CimBooleanUtils.isFalse(machineTypeGetOut.isBStorageMachineFlag())) {
                if (log.isDebugEnabled())
                    log.debug("{} fromMachine type is EQP", ObjectIdentifier.fetchValue(strReticleComponentJob.getFromEquipmentID()));
                fromMachineType = 1L;
                log.debug("step6 - equipmentMethod.equipmentReticlePodPortInfoGetDR");
                Outputs.ObjEquipmentReticlePodPortInfoGetDROut equipmentReticlePodPortInfoGetDROut = equipmentMethod.equipmentReticlePodPortInfoGetDR(objCommon, strReticleComponentJob.getFromEquipmentID());
                strReticlePodPortInfo = equipmentReticlePodPortInfoGetDROut.getReticlePodPortInfoList();
            } else if (CimStringUtils.equals(machineTypeGetOut.getStockerType(), BizConstant.SP_STOCKER_TYPE_BARERETICLE)) {
                if (log.isDebugEnabled())
                    log.debug("{} fromMachine type is BRSTK", ObjectIdentifier.fetchValue(strReticleComponentJob.getFromEquipmentID()));
                fromMachineType = 2L;
                log.debug("step7 - stockerMethod.stockerReticlePodPortInfoGetDR");
                strReticlePodPortInfo = stockerMethod.stockerReticlePodPortInfoGetDR(objCommon, strReticleComponentJob.getFromEquipmentID());
            } else if (CimStringUtils.equals(machineTypeGetOut.getStockerType(), BizConstant.SP_STOCKER_TYPE_RETICLEPOD)) {
                if (log.isDebugEnabled())
                    log.debug("{} fromMachine type is RSPSTK.", ObjectIdentifier.fetchValue(strReticleComponentJob.getFromEquipmentID()));
                fromMachineType = 3L;
            } else {
                log.error("{} machine type {} is invalid.", ObjectIdentifier.fetchValue(strReticleComponentJob.getFromEquipmentID()), machineTypeGetOut.getStockerType());
                Validations.check(true, retCodeConfig.getInvalidStockerType(), ObjectIdentifier.fetchValue(strReticleComponentJob.getFromEquipmentID()));
            }

            Long toMachineType = 0L;
            log.debug("step8 - equipmentMethod.machineTypeGet");
            Outputs.ObjMachineTypeGetOut toMachineTypeGetOut = equipmentMethod.machineTypeGet(objCommon, strReticleComponentJob.getToEquipmentID());
            if (CimBooleanUtils.isFalse(toMachineTypeGetOut.isBStorageMachineFlag())) {
                if (log.isDebugEnabled())
                    log.debug("toMachine {} type is EQP", ObjectIdentifier.fetchValue(strReticleComponentJob.getToEquipmentID()));
                toMachineType = 1L;
            } else if (CimStringUtils.equals(toMachineTypeGetOut.getStockerType(), BizConstant.SP_STOCKER_TYPE_BARERETICLE)) {
                if (log.isDebugEnabled())
                    log.debug("toMachine {} type is BRSTK", ObjectIdentifier.fetchValue(strReticleComponentJob.getToEquipmentID()));
                toMachineType = 2L;
            } else if (CimStringUtils.equals(toMachineTypeGetOut.getStockerType(), BizConstant.SP_STOCKER_TYPE_RETICLEPOD)) {
                if (log.isDebugEnabled())
                    log.debug("toMachine {} type is RSPSTK.", ObjectIdentifier.fetchValue(strReticleComponentJob.getToEquipmentID()));
                toMachineType = 3L;
            } else {
                log.error("{} machine {} type {} is invalid.", ObjectIdentifier.fetchValue(strReticleComponentJob.getToEquipmentID()), machineTypeGetOut.getStockerType());
                Validations.check(true, retCodeConfig.getInvalidStockerType(), ObjectIdentifier.fetchValue(strReticleComponentJob.getToEquipmentID()));
            }

            if (fromMachineType == 1L || fromMachineType == 2L) {
                if (log.isDebugEnabled())
                    log.debug("fromMachineType {}", fromMachineType);
                Boolean bNotReleaseFlag = false;
                if (CimArrayUtils.isNotEmpty(strReticlePodPortInfo)) {
                    for (Infos.ReticlePodPortInfo reticlePodPortInfo : strReticlePodPortInfo) {
                        if (ObjectIdentifier.equalsWithValue(reticlePodPortInfo.getReticlePodPortID(), strReticleComponentJob.getFromReticlePodPortID())
                                && !ObjectIdentifier.equalsWithValue(reticlePodPortInfo.getReservedReticlePodID(), strReticleComponentJob.getReticlePodID())) {
                            if (log.isDebugEnabled())
                                log.debug("From Port is already reserved for other reticlePod {}.", ObjectIdentifier.fetchValue(reticlePodPortInfo.getReservedReticlePodID()));
                            bNotReleaseFlag = true;
                            break;
                        }
                    }
                }
                if (CimBooleanUtils.isFalse(bNotReleaseFlag)) {
                    log.debug("Release fromPort reserve");
                    log.debug("step9 - equipmentForDurableMethod.machineReticlePodPortReserveCancel");
                    equipmentForDurableMethod.machineReticlePodPortReserveCancel(objCommon,
                            strReticleComponentJob.getFromEquipmentID(),
                            strReticleComponentJob.getFromReticlePodPortID());
                }
            }
            if (toMachineType == 1L || toMachineType == 2L) {
                if (log.isDebugEnabled())
                    log.debug("toMachineType {}", toMachineType);
                log.debug("step10 - equipmentForDurableMethod.machineReticlePodPortReserveCancel");
                equipmentForDurableMethod.machineReticlePodPortReserveCancel(objCommon,
                        strReticleComponentJob.getToEquipmentID(),
                        strReticleComponentJob.getToReticlePodPortID());
            }
            log.debug("Release reticlePod transfer reserve.");
            log.debug("step11 - reticleMethod.reticlePodTransferReserveCancel");
            reticleMethod.reticlePodTransferReserveCancel(objCommon, strReticleComponentJob.getReticlePodID());
        }

        /*-------------------------*/
        /*   Insert event record   */
        /*-------------------------*/
        Infos.ReticleEventRecord strReticleEventRecord = new Infos.ReticleEventRecord();
        strReticleEventRecord.setEventTime(CimDateUtils.convertToSpecString(objCommon.getTimeStamp().getReportTimeStamp()));
        strReticleEventRecord.setReticleDispatchJobID(reticleDispatchJobID);
        strReticleEventRecord.setReticleComponentJobID(reticleComponentJobID);

        log.debug("step12 - reticleMethod.reticleEventQueuePutDR");
        reticleMethod.reticleEventQueuePutDR(objCommon, strReticleEventRecord);
    }

    @Override
    public void sxReticleComponentJobSkipReq(Infos.ObjCommon objCommon, Params.ReticleComponentJobSkipReqParams params) {
        String tmpArhs = StandardProperties.OM_ARHS_FLAG.getValue();
        if (log.isDebugEnabled())
            log.debug("ARHS switch on / off  tmpArhs = {}", tmpArhs);
        Validations.check(!CimStringUtils.equals(BizConstant.SP_ARMS_SWITCH_ON, tmpArhs), retCodeConfig.getFunctionNotAvailable());

        String reticleDispatchJobID = params.getReticleDispatchJobID();
        String reticleComponentJobID = params.getReticleComponentJobID();
        Validations.check(CimStringUtils.isEmpty(reticleDispatchJobID) && CimStringUtils.isEmpty(reticleComponentJobID), retCodeConfig.getInvalidParameter());

        /*-----------------------------------*/
        /*   Get Reticle Dispatch Job list   */
        /*-----------------------------------*/
        log.debug("step1 - reticleMethod.reticleDispatchJobListGetDR");
        List<Infos.ReticleDispatchJob> reticleDispatchJobList = reticleMethod.reticleDispatchJobListGetDR(objCommon, reticleDispatchJobID);
        if (CimArrayUtils.isEmpty(reticleDispatchJobList)) {
            log.error("RDJ not found | reticleDispatchJobID {}", reticleDispatchJobID);
            Validations.check(true, retCodeConfigEx.getRdjNotFound());
        } else if (CimArrayUtils.getSize(reticleDispatchJobList) > 1) {
            log.error("Duplicate RDJ | reticleDispatchJobID {}", reticleDispatchJobID);
            Validations.check(true, retCodeConfigEx.getRdjDuplicate());
        }

        if (!CimStringUtils.equals(reticleDispatchJobList.get(0).getJobStatus(), BizConstant.SP_RDJ_STATUS_EXECUTING)
                && !CimStringUtils.equals(reticleDispatchJobList.get(0).getJobStatus(), BizConstant.SP_RDJ_STATUS_ERROR)) {
            log.error("Invalid job status {} for skip.", reticleDispatchJobList.get(0).getJobStatus());
            Validations.check(true, retCodeConfigEx.getRdjStatusError(), reticleDispatchJobList.get(0).getJobStatus());
        }

        /*------------------------------------*/
        /*   Get Reticle Component Job List   */
        /*------------------------------------*/
        log.debug("step2 - reticleMethod.reticleComponentJobListGetDR");
        List<Infos.ReticleComponentJob> reticleComponentJobList = reticleMethod.reticleComponentJobListGetDR(objCommon, reticleDispatchJobID);
        if (CimArrayUtils.isEmpty(reticleComponentJobList)) {
            log.error("Not found RCJ jobs");
            Validations.check(true, retCodeConfigEx.getRcjNotFound());
        }

        Infos.ReticleComponentJob strReticleComponentUpdateJob = new Infos.ReticleComponentJob();
        Boolean foundFlag = false;
        for (Infos.ReticleComponentJob reticleComponentJob : reticleComponentJobList) {
            if (CimStringUtils.equals(reticleComponentJob.getReticleComponentJobID(), reticleComponentJobID)
                    && !CimStringUtils.equals(reticleComponentJob.getJobStatus(), reticleDispatchJobList.get(0).getJobStatus())) {
                log.error("Mis-match RDJ/RCJ jobStatus. RDJ {}:RCJ {}",
                        reticleDispatchJobList.get(0).getJobStatus(),
                        reticleComponentJob.getJobStatus());
                Validations.check(true, retCodeConfigEx.getMismatchRdjrcjStatus());
            } else if (CimStringUtils.equals(reticleComponentJob.getReticleComponentJobID(), reticleComponentJobID)
                    && CimStringUtils.equals(reticleComponentJob.getJobStatus(), reticleDispatchJobList.get(0).getJobStatus())) {
                log.debug("Found RCJ for update");
                strReticleComponentUpdateJob = reticleComponentJob;
                foundFlag = true;
                break;
            }
        }
        Validations.check(CimBooleanUtils.isFalse(foundFlag), retCodeConfigEx.getRcjNotFound());
        ObjectIdentifier dummyID = ObjectIdentifier.buildWithValue("");

        if (CimStringUtils.equals(strReticleComponentUpdateJob.getJobName(), BizConstant.SP_RCJ_JOBNAME_STORE)) {
            /*---------------------*/
            /*   Status recovery   */
            /*---------------------*/
            log.debug("Skip job is Store. call sxReticleStoreRpt");

            Infos.ReticleStoreResult reticleStoreResult = new Infos.ReticleStoreResult();
            reticleStoreResult.setReticleID(strReticleComponentUpdateJob.getReticleID());
            reticleStoreResult.setSlotNo(strReticleComponentUpdateJob.getSlotNo().intValue());
            reticleStoreResult.setReticleStoredFlag(true);
            List<Infos.ReticleStoreResult> reticleStoreResults = Collections.singletonList(reticleStoreResult);
            if (CimStringUtils.equals(strReticleComponentUpdateJob.getToEquipmentCategory(), BizConstant.SP_MACHINE_TYPE_EQP)) {
                log.debug("toEquipmentCategory = EQP");
                log.debug("step3 - durableService.sxReticleStoreRpt");
                Params.ReticleStoreRptParams reticleStoreRptParams = new Params.ReticleStoreRptParams();
                reticleStoreRptParams.setEquipmentID(strReticleComponentUpdateJob.getToEquipmentID());
                reticleStoreRptParams.setReticlePodPortID(strReticleComponentUpdateJob.getToReticlePodPortID());
                reticleStoreRptParams.setReticlePodID(strReticleComponentUpdateJob.getReticlePodID());
                reticleStoreRptParams.setReticleStoreResultList(reticleStoreResults);
                reticleStoreRptParams.setOpeMemo(params.getOpeMemo());
                durableService.sxReticleStoreRpt(objCommon, reticleStoreRptParams);
            } else if (CimStringUtils.equals(strReticleComponentUpdateJob.getToEquipmentCategory(), BizConstant.SP_STOCKER_TYPE_BARERETICLE)) {
                log.debug("toEquipmentCategory = BRS");
                log.debug("step4 - durableService.sxReticleStoreRpt");
                Params.ReticleStoreRptParams reticleStoreRptParams = new Params.ReticleStoreRptParams();
                reticleStoreRptParams.setStockerID(strReticleComponentUpdateJob.getToEquipmentID());
                reticleStoreRptParams.setResourceID(strReticleComponentUpdateJob.getToReticlePodPortID());
                reticleStoreRptParams.setReticlePodID(strReticleComponentUpdateJob.getReticlePodID());
                reticleStoreRptParams.setReticleStoreResultList(reticleStoreResults);
                reticleStoreRptParams.setOpeMemo(params.getOpeMemo());
                durableService.sxReticleStoreRpt(objCommon, reticleStoreRptParams);
            } else {
                log.error("Invalid toEquipmentCategory | jobName {}: eqpCategory {}", strReticleComponentUpdateJob.getJobName(), strReticleComponentUpdateJob.getToEquipmentCategory());
                Validations.check(true, retCodeConfigEx.getRcjIncomplete());
            }
        } else if (CimStringUtils.equals(strReticleComponentUpdateJob.getJobName(), BizConstant.SP_RCJ_JOBNAME_RETRIEVE)) {
            /*---------------------*/
            /*   Status recovery   */
            /*---------------------*/
            Infos.ReticleRetrieveResult reticleRetrieveResult = new Infos.ReticleRetrieveResult();
            reticleRetrieveResult.setReticleID(strReticleComponentUpdateJob.getReticleID());
            reticleRetrieveResult.setSlotNo(strReticleComponentUpdateJob.getSlotNo().intValue());
            reticleRetrieveResult.setReticleRetrievedFlag(true);
            log.debug("Skip job is Retrieve. call sxReticleRetrieveRpt");
            if (CimStringUtils.equals(strReticleComponentUpdateJob.getToEquipmentCategory(), BizConstant.SP_MACHINE_TYPE_EQP)) {
                log.debug("toEquipmentCategory = EQP");
                log.debug("step5 - durableService.sxReticleRetrieveRpt");
                Params.ReticleRetrieveRptParams reticleRetrieveRptParams = new Params.ReticleRetrieveRptParams();
                reticleRetrieveRptParams.setEquipmentID(strReticleComponentUpdateJob.getToEquipmentID());
                reticleRetrieveRptParams.setReticlePodPortID(strReticleComponentUpdateJob.getToReticlePodPortID());
                reticleRetrieveRptParams.setReticlePodID(strReticleComponentUpdateJob.getReticlePodID());
                reticleRetrieveRptParams.setReticleRetrieveResultList(Collections.singletonList(reticleRetrieveResult));
                reticleRetrieveRptParams.setOpeMemo(params.getOpeMemo());
                durableService.sxReticleRetrieveRpt(objCommon, reticleRetrieveRptParams);
            } else if (CimStringUtils.equals(strReticleComponentUpdateJob.getToEquipmentCategory(), BizConstant.SP_STOCKER_TYPE_BARERETICLE)) {
                log.debug("toEquipmentCategory = BRS");
                log.debug("step6 - durableService.sxReticleRetrieveRpt");
                Params.ReticleRetrieveRptParams reticleRetrieveRptParams = new Params.ReticleRetrieveRptParams();
                reticleRetrieveRptParams.setStockerID(strReticleComponentUpdateJob.getToEquipmentID());
                reticleRetrieveRptParams.setResourceID(strReticleComponentUpdateJob.getToReticlePodPortID());
                reticleRetrieveRptParams.setReticlePodID(strReticleComponentUpdateJob.getReticlePodID());
                reticleRetrieveRptParams.setReticleRetrieveResultList(Collections.singletonList(reticleRetrieveResult));
                reticleRetrieveRptParams.setOpeMemo(params.getOpeMemo());
                durableService.sxReticleRetrieveRpt(objCommon, reticleRetrieveRptParams);
            } else {
                log.error("Invalid toEquipmentCategory | jobName {} : eqpCategory {}",
                        strReticleComponentUpdateJob.getJobName(),
                        strReticleComponentUpdateJob.getToEquipmentCategory());
                Validations.check(true, retCodeConfigEx.getRcjIncomplete());
            }
        } else if (CimStringUtils.equals(strReticleComponentUpdateJob.getJobName(), BizConstant.SP_RCJ_JOBNAME_UNCLAMP)) {
            /*---------------------*/
            /*   Status recovery   */
            /*---------------------*/
            log.debug("step7 - arshService.sxReticlePodUnclampRpt");
            log.debug("Skip job is Unclamp. call sxReticlePodUnclampRpt and sxEqpRSPPortStatusChangeRpt");
            Params.ReticlePodUnclampRptParams reticlePodUnclampRptParams = new Params.ReticlePodUnclampRptParams();
            reticlePodUnclampRptParams.setMachineID(strReticleComponentUpdateJob.getToEquipmentID());
            reticlePodUnclampRptParams.setPortID(strReticleComponentUpdateJob.getToReticlePodPortID());
            reticlePodUnclampRptParams.setReticlePodID(strReticleComponentUpdateJob.getReticlePodID());
            reticlePodUnclampRptParams.setBSuccessFlag(true);
            reticlePodUnclampRptParams.setOpeMemo(params.getOpeMemo());
            this.sxReticlePodUnclampRpt(objCommon, reticlePodUnclampRptParams);

            Params.EqpRSPPortStatusChangeRpt eqpRSPPortStatusChangeRpt = new Params.EqpRSPPortStatusChangeRpt();
            eqpRSPPortStatusChangeRpt.setEquipmentID(strReticleComponentUpdateJob.getToEquipmentID());
            Infos.EqpRSPPortEventOnEAP eqpRSPPortEventOnEAP = new Infos.EqpRSPPortEventOnEAP();
            eqpRSPPortStatusChangeRpt.setStrEqpRSPPortEventOnEAP(Collections.singletonList(eqpRSPPortEventOnEAP));
            eqpRSPPortEventOnEAP.setPortID(strReticleComponentUpdateJob.getToReticlePodPortID());
            eqpRSPPortEventOnEAP.setPortStatus(BizConstant.SP_PORTRSC_PORTSTATE_UNLOADREQ);
            eqpRSPPortEventOnEAP.setReticlePodID(strReticleComponentUpdateJob.getReticlePodID());
            log.debug("step8 - durableService.sxEqpRSPPortStatusChangeRpt");
            durableService.sxEqpRSPPortStatusChangeRpt(objCommon, eqpRSPPortStatusChangeRpt);
        } else if (CimStringUtils.equals(strReticleComponentUpdateJob.getJobName(), BizConstant.SP_RCJ_JOBNAME_XFER)) {
            /*---------------------*/
            /*   Status recovery   */
            /*---------------------*/
            log.debug("Skip job is Xfer.");
            log.debug("Check from machine condition. reticlePod loaded on or not.");
            if (CimStringUtils.equals(strReticleComponentUpdateJob.getFromEquipmentCategory(), BizConstant.SP_MACHINE_TYPE_EQP)) {
                log.debug("fromEquipmentCategory = EQP, Check fromEquipment reticlePodPort.");
                log.debug("step9 - equipmentMethod.equipmentReticlePodPortInfoGetDR");
                Outputs.ObjEquipmentReticlePodPortInfoGetDROut equipmentReticlePodPortInfoGetDROut = equipmentMethod.equipmentReticlePodPortInfoGetDR(objCommon, strReticleComponentUpdateJob.getFromEquipmentID());
                Boolean bFoundFlag = false;
                if (null != equipmentReticlePodPortInfoGetDROut && CimArrayUtils.isNotEmpty(equipmentReticlePodPortInfoGetDROut.getReticlePodPortInfoList())) {
                    for (Infos.ReticlePodPortInfo reticlePodPortInfo : equipmentReticlePodPortInfoGetDROut.getReticlePodPortInfoList()) {
                        if (ObjectIdentifier.equalsWithValue(reticlePodPortInfo.getReticlePodPortID(), strReticleComponentUpdateJob.getFromReticlePodPortID())
                                && ObjectIdentifier.equalsWithValue(reticlePodPortInfo.getLoadedReticlePodID(), strReticleComponentUpdateJob.getReticlePodID())) {
                            log.debug("ReticlePod is still loaded on equipment reticlePodPort.");
                            bFoundFlag = true;
                            break;
                        }
                    }
                }
                if (CimBooleanUtils.isTrue(bFoundFlag)) {
                    log.debug("bFoundFlag = TRUE");
                    log.debug("step10 - durableService.sxReticlePodUnloadingRpt");
                    Params.ReticlePodUnloadingRptParams reticlePodUnloadingRptParams = new Params.ReticlePodUnloadingRptParams();
                    reticlePodUnloadingRptParams.setEquipmentID(strReticleComponentUpdateJob.getFromEquipmentID());
                    reticlePodUnloadingRptParams.setReticlePodPortID(strReticleComponentUpdateJob.getFromReticlePodPortID());
                    reticlePodUnloadingRptParams.setReticlePodID(strReticleComponentUpdateJob.getReticlePodID());
                    reticlePodUnloadingRptParams.setOpeMemo(params.getOpeMemo());
                    durableService.sxReticlePodUnloadingRpt(objCommon, reticlePodUnloadingRptParams);

                    Params.EqpRSPPortStatusChangeRpt eqpRSPPortStatusChangeRpt = new Params.EqpRSPPortStatusChangeRpt();
                    eqpRSPPortStatusChangeRpt.setEquipmentID(strReticleComponentUpdateJob.getFromEquipmentID());
                    Infos.EqpRSPPortEventOnEAP eqpRSPPortEventOnEAP = new Infos.EqpRSPPortEventOnEAP();
                    eqpRSPPortStatusChangeRpt.setStrEqpRSPPortEventOnEAP(Collections.singletonList(eqpRSPPortEventOnEAP));
                    eqpRSPPortEventOnEAP.setPortID(strReticleComponentUpdateJob.getFromReticlePodPortID());
                    eqpRSPPortEventOnEAP.setPortStatus(BizConstant.SP_PORTRSC_PORTSTATE_LOADREQ);
                    eqpRSPPortEventOnEAP.setReticlePodID(dummyID);
                    log.debug("step11 - durableService.sxEqpRSPPortStatusChangeRpt");
                    durableService.sxEqpRSPPortStatusChangeRpt(objCommon, eqpRSPPortStatusChangeRpt);
                } else {
                    log.debug("ReticlePod is already unloaded from equipment reticlePodPort");
                }
            } else if (CimStringUtils.equals(strReticleComponentUpdateJob.getFromEquipmentCategory(), BizConstant.SP_STOCKER_TYPE_BARERETICLE)) {
                log.debug("fromEquipmentCategory = BRS, Check fromBareReticleStocker port.");
                log.debug("step12 - stockerMethod.stockerReticlePodPortInfoGetDR");
                List<Infos.ReticlePodPortInfo> stockerReticlePodPortInfoList = stockerMethod.stockerReticlePodPortInfoGetDR(objCommon, strReticleComponentUpdateJob.getFromEquipmentID());
                Boolean bFoundFlag = false;
                if (CimArrayUtils.isNotEmpty(stockerReticlePodPortInfoList)) {
                    for (Infos.ReticlePodPortInfo stockerReticlePodPortInfo : stockerReticlePodPortInfoList) {
                        if (ObjectIdentifier.equalsWithValue(stockerReticlePodPortInfo.getReticlePodPortID(), strReticleComponentUpdateJob.getFromReticlePodPortID())
                                && ObjectIdentifier.equalsWithValue(stockerReticlePodPortInfo.getLoadedReticlePodID(), strReticleComponentUpdateJob.getReticlePodID())) {
                            log.info("ReticlePod is still loaded on bareReticleStocker reticlePodPort.");
                            bFoundFlag = true;
                            break;
                        }
                    }
                }
                if (CimBooleanUtils.isTrue(bFoundFlag)) {
                    log.debug("bFoundFlag = TRUE");
                    log.debug("step13 - durableService.sxReticlePodUnloadingRpt");
                    Params.ReticlePodUnloadingRptParams reticlePodUnloadingRptParams = new Params.ReticlePodUnloadingRptParams();
                    reticlePodUnloadingRptParams.setBareReticleStockerID(strReticleComponentUpdateJob.getFromEquipmentID());
                    reticlePodUnloadingRptParams.setResourceID(strReticleComponentUpdateJob.getFromReticlePodPortID());
                    reticlePodUnloadingRptParams.setReticlePodID(strReticleComponentUpdateJob.getReticlePodID());
                    reticlePodUnloadingRptParams.setOpeMemo(params.getOpeMemo());
                    durableService.sxReticlePodUnloadingRpt(objCommon, reticlePodUnloadingRptParams);

                    Params.EqpRSPPortStatusChangeRpt eqpRSPPortStatusChangeRpt = new Params.EqpRSPPortStatusChangeRpt();
                    eqpRSPPortStatusChangeRpt.setEquipmentID(strReticleComponentUpdateJob.getFromEquipmentID());
                    Infos.EqpRSPPortEventOnEAP eqpRSPPortEventOnEAP = new Infos.EqpRSPPortEventOnEAP();
                    eqpRSPPortStatusChangeRpt.setStrEqpRSPPortEventOnEAP(Collections.singletonList(eqpRSPPortEventOnEAP));
                    eqpRSPPortEventOnEAP.setPortID(strReticleComponentUpdateJob.getFromReticlePodPortID());
                    eqpRSPPortEventOnEAP.setPortStatus(BizConstant.SP_PORTRSC_PORTSTATE_LOADREQ);
                    eqpRSPPortEventOnEAP.setReticlePodID(dummyID);
                    log.debug("step14 - durableService.sxEqpRSPPortStatusChangeRpt");
                    durableService.sxEqpRSPPortStatusChangeRpt(objCommon, eqpRSPPortStatusChangeRpt);
                } else {
                    log.debug("ReticlePod is already unloaded from bareReticleStocker reticlePodPort");
                }
            } else if (CimStringUtils.equals(strReticleComponentUpdateJob.getFromEquipmentCategory(), BizConstant.SP_STOCKER_TYPE_RETICLEPOD)) {
                log.debug("fromEquipmentCategory = RPS, Check ReticlePodStocker.");
                Params.ReticlePodStockerInfoInqParams reticlePodStockerInfoInqParams = new Params.ReticlePodStockerInfoInqParams();
                reticlePodStockerInfoInqParams.setStockerID(strReticleComponentUpdateJob.getFromEquipmentID());
                log.debug("step15 - durableInqService.sxReticlePodStockerInfoInq");
                Results.ReticlePodStockerInfoInqResult reticlePodStockerInfoInqResult = durableInqService.sxReticlePodStockerInfoInq(objCommon, reticlePodStockerInfoInqParams);

                Boolean bFoundFlag = false;
                if (null != reticlePodStockerInfoInqResult && CimArrayUtils.isNotEmpty(reticlePodStockerInfoInqResult.getReticlePodInfoInStockerList())) {
                    for (Infos.ReticlePodInfoInStocker reticlePodInfoInStocker : reticlePodStockerInfoInqResult.getReticlePodInfoInStockerList()) {
                        if (ObjectIdentifier.equalsWithValue(reticlePodInfoInStocker.getReticlePodID(), strReticleComponentUpdateJob.getReticlePodID())) {
                            log.debug("ReticlePod is still exist reticlePodStocker");
                            bFoundFlag = true;
                            break;
                        }
                    }
                }
                if (CimBooleanUtils.isTrue(bFoundFlag)) {
                    log.debug("bFoundFlag = TRUE, Unload reticlePodStocker(transfer status change to MO)");
                    Params.RSPXferStatusChangeRptParams rspXferStatusChangeRptParams = new Params.RSPXferStatusChangeRptParams();
                    rspXferStatusChangeRptParams.setReticlePodID(strReticleComponentUpdateJob.getReticlePodID());
                    rspXferStatusChangeRptParams.setXferStatus(BizConstant.SP_TRANSSTATE_MANUALOUT);
                    rspXferStatusChangeRptParams.setManualInFlag(false);
                    rspXferStatusChangeRptParams.setMachineID(strReticleComponentUpdateJob.getFromEquipmentID());
                    rspXferStatusChangeRptParams.setPortID(dummyID);
                    rspXferStatusChangeRptParams.setClaimMemo(params.getOpeMemo());
                    log.debug("step16 - arshService.sxRSPXferStatusChangeRpt");
                    Results.RSPXferStatusChangeRptResult rspXferStatusChangeRptResult = this.sxRSPXferStatusChangeRpt(objCommon, rspXferStatusChangeRptParams);
                }
            }
        }
        log.debug("step17 - arshService.sxReticlePodXferJobCompRpt");
        Params.ReticlePodXferJobCompRptParams reticlePodXferJobCompRptParams = new Params.ReticlePodXferJobCompRptParams();
        Infos.ReticlePodXferJobCompInfo reticlePodXferJobCompInfo = new Infos.ReticlePodXferJobCompInfo();
        reticlePodXferJobCompInfo.setReticlePodID(strReticleComponentUpdateJob.getReticlePodID());
        reticlePodXferJobCompInfo.setToMachineID(strReticleComponentUpdateJob.getToEquipmentID());
        reticlePodXferJobCompInfo.setToPortID(strReticleComponentUpdateJob.getToReticlePodPortID());
        reticlePodXferJobCompInfo.setTransferJobStatus(BizConstant.SP_TRANSFERJOBSTATUS_COMP);
        reticlePodXferJobCompRptParams.setStrReticlePodXferJobCompInfo(Collections.singletonList(reticlePodXferJobCompInfo));
        reticlePodXferJobCompRptParams.setClaimMemo(params.getOpeMemo());
        this.sxReticlePodXferJobCompRpt(objCommon, reticlePodXferJobCompRptParams);

        log.debug("Check toMachine condition. reticlePod loaded on or not.");
        if (CimStringUtils.equals(strReticleComponentUpdateJob.getToEquipmentCategory(), BizConstant.SP_MACHINE_TYPE_EQP)) {
            log.debug("toEquipmentCategory = EQP, Check toEquipment reticlePodPort.");
            log.debug("step18 - equipmentMethod.equipmentReticlePodPortInfoGetDR");
            Outputs.ObjEquipmentReticlePodPortInfoGetDROut equipmentReticlePodPortInfoGetDROut = equipmentMethod.equipmentReticlePodPortInfoGetDR(objCommon, strReticleComponentUpdateJob.getToEquipmentID());
            Boolean bFoundFlag = false;
            if (null != equipmentReticlePodPortInfoGetDROut && CimArrayUtils.isNotEmpty(equipmentReticlePodPortInfoGetDROut.getReticlePodPortInfoList())) {
                for (Infos.ReticlePodPortInfo reticlePodPortInfo : equipmentReticlePodPortInfoGetDROut.getReticlePodPortInfoList()) {
                    if (ObjectIdentifier.equalsWithValue(reticlePodPortInfo.getReticlePodPortID(), strReticleComponentUpdateJob.getToReticlePodPortID())
                            && ObjectIdentifier.equalsWithValue(reticlePodPortInfo.getLoadedReticlePodID(), strReticleComponentUpdateJob.getReticlePodID())) {
                        log.info("ReticlePod has already been loaded to target equipment reticlePodPort.");
                        bFoundFlag = true;
                        break;
                    }
                }
            }
            if (CimBooleanUtils.isFalse(bFoundFlag)) {
                log.debug("bFoundFlag = FALSE");
                log.debug("step19 - durableService.sxReticlePodLoadingRpt");
                Params.ReticlePodLoadingRptParams reticlePodLoadingRptParams = new Params.ReticlePodLoadingRptParams();
                reticlePodLoadingRptParams.setEquipmentID(strReticleComponentUpdateJob.getToEquipmentID());
                reticlePodLoadingRptParams.setReticlePodPortID(strReticleComponentUpdateJob.getToReticlePodPortID());
                reticlePodLoadingRptParams.setReticlePodID(strReticleComponentUpdateJob.getReticlePodID());
                reticlePodLoadingRptParams.setOpeMemo(params.getOpeMemo());
                durableService.sxReticlePodLoadingRpt(objCommon, reticlePodLoadingRptParams);

                Params.EqpRSPPortStatusChangeRpt eqpRSPPortStatusChangeRpt = new Params.EqpRSPPortStatusChangeRpt();
                eqpRSPPortStatusChangeRpt.setEquipmentID(strReticleComponentUpdateJob.getToEquipmentID());
                Infos.EqpRSPPortEventOnEAP eqpRSPPortEventOnEAP = new Infos.EqpRSPPortEventOnEAP();
                eqpRSPPortStatusChangeRpt.setStrEqpRSPPortEventOnEAP(Collections.singletonList(eqpRSPPortEventOnEAP));
                eqpRSPPortEventOnEAP.setPortID(strReticleComponentUpdateJob.getToReticlePodPortID());
                eqpRSPPortEventOnEAP.setPortStatus(BizConstant.SP_PORTRSC_PORTSTATE_LOADCOMP);
                eqpRSPPortEventOnEAP.setReticlePodID(dummyID);
                log.debug("step20 - durableService.sxEqpRSPPortStatusChangeRpt");
                durableService.sxEqpRSPPortStatusChangeRpt(objCommon, eqpRSPPortStatusChangeRpt);
            } else {
                log.debug("bFoundFlag = TRUE, Do nothing;");
            }
        } else if (CimStringUtils.equals(strReticleComponentUpdateJob.getToEquipmentCategory(), BizConstant.SP_STOCKER_TYPE_BARERETICLE)) {
            log.debug("toEquipmentCategory = BRS, Check toBareReticleStocker port.");
            log.debug("step21 - stockerMethod.stockerReticlePodPortInfoGetDR");
            List<Infos.ReticlePodPortInfo> stockerReticlePodPortInfoGetDROut = stockerMethod.stockerReticlePodPortInfoGetDR(objCommon, strReticleComponentUpdateJob.getToEquipmentID());
            Boolean bFoundFlag = false;
            if (CimArrayUtils.isNotEmpty(stockerReticlePodPortInfoGetDROut)) {
                for (Infos.ReticlePodPortInfo stockerReticlePodPortInfo : stockerReticlePodPortInfoGetDROut) {
                    if (ObjectIdentifier.equalsWithValue(stockerReticlePodPortInfo.getReticlePodPortID(), strReticleComponentUpdateJob.getToReticlePodPortID())
                            && ObjectIdentifier.equalsWithValue(stockerReticlePodPortInfo.getLoadedReticlePodID(), strReticleComponentUpdateJob.getReticlePodID())) {
                        log.debug("ReticlePod has already been loaded to target bare reticle stocker reticlePodPort.");
                        bFoundFlag = true;
                        break;
                    }
                }
            }
            if (CimBooleanUtils.isFalse(bFoundFlag)) {
                log.debug("bFoundFlag = FALSE");
                log.debug("step22 - durableService.sxReticlePodLoadingRpt");
                Params.ReticlePodLoadingRptParams reticlePodLoadingRptParams = new Params.ReticlePodLoadingRptParams();
                reticlePodLoadingRptParams.setBareReticleStockerID(strReticleComponentUpdateJob.getToEquipmentID());
                reticlePodLoadingRptParams.setResourceID(strReticleComponentUpdateJob.getToReticlePodPortID());
                reticlePodLoadingRptParams.setReticlePodID(strReticleComponentUpdateJob.getReticlePodID());
                reticlePodLoadingRptParams.setOpeMemo(params.getOpeMemo());
                durableService.sxReticlePodLoadingRpt(objCommon, reticlePodLoadingRptParams);

                Params.EqpRSPPortStatusChangeRpt eqpRSPPortStatusChangeRpt = new Params.EqpRSPPortStatusChangeRpt();
                eqpRSPPortStatusChangeRpt.setEquipmentID(strReticleComponentUpdateJob.getToEquipmentID());
                Infos.EqpRSPPortEventOnEAP eqpRSPPortEventOnEAP = new Infos.EqpRSPPortEventOnEAP();
                eqpRSPPortStatusChangeRpt.setStrEqpRSPPortEventOnEAP(Collections.singletonList(eqpRSPPortEventOnEAP));
                eqpRSPPortEventOnEAP.setPortID(strReticleComponentUpdateJob.getToReticlePodPortID());
                eqpRSPPortEventOnEAP.setPortStatus(BizConstant.SP_PORTRSC_PORTSTATE_LOADCOMP);
                eqpRSPPortEventOnEAP.setReticlePodID(dummyID);
                log.debug("step23 - durableService.sxEqpRSPPortStatusChangeRpt");
                durableService.sxEqpRSPPortStatusChangeRpt(objCommon, eqpRSPPortStatusChangeRpt);
            } else {
                log.debug("bFoundFlag = TRUE");
            }
        } else if (CimStringUtils.equals(strReticleComponentUpdateJob.getToEquipmentCategory(), BizConstant.SP_STOCKER_TYPE_RETICLEPOD)) {
            log.debug("toEquipmentCategory = RPS, Check ReticlePodStocker");
            Params.ReticlePodStockerInfoInqParams reticlePodStockerInfoInqParams = new Params.ReticlePodStockerInfoInqParams();
            reticlePodStockerInfoInqParams.setStockerID(strReticleComponentUpdateJob.getToEquipmentID());
            log.debug("step24 - durableInqService.sxReticlePodStockerInfoInq");
            Results.ReticlePodStockerInfoInqResult reticlePodStockerInfoInqResult = durableInqService.sxReticlePodStockerInfoInq(objCommon, reticlePodStockerInfoInqParams);
            Boolean bFoundFlag = false;
            if (null != reticlePodStockerInfoInqResult && CimArrayUtils.isNotEmpty(reticlePodStockerInfoInqResult.getReticlePodInfoInStockerList())) {
                for (Infos.ReticlePodInfoInStocker reticlePodInfoInStocker : reticlePodStockerInfoInqResult.getReticlePodInfoInStockerList()) {
                    if (ObjectIdentifier.equalsWithValue(reticlePodInfoInStocker.getReticlePodID(), strReticleComponentUpdateJob.getReticlePodID())) {
                        log.debug("ReticlePod is already exist toReticlePodStocker");
                        bFoundFlag = true;
                        break;
                    }
                }
            }
            // TODO-Confirm: 2021/6/1 the source code is if bFoundFlag is True.
            // then change the RSP xferStatus, but the logical is mixed.
            // now change it the !bFoundFlag.
            if (CimBooleanUtils.isFalse(bFoundFlag)) {
                log.debug("bFoundFlag = False, Load reticlePodStocker(transfer status change to MI)");
                Params.RSPXferStatusChangeRptParams rspXferStatusChangeRptParams = new Params.RSPXferStatusChangeRptParams();
                rspXferStatusChangeRptParams.setReticlePodID(strReticleComponentUpdateJob.getReticlePodID());
                rspXferStatusChangeRptParams.setXferStatus(BizConstant.SP_TRANSSTATE_MANUALIN);
                rspXferStatusChangeRptParams.setManualInFlag(true);
                rspXferStatusChangeRptParams.setMachineID(strReticleComponentUpdateJob.getToEquipmentID());
                rspXferStatusChangeRptParams.setPortID(dummyID);
                rspXferStatusChangeRptParams.setClaimMemo(params.getOpeMemo());
                log.debug("step25 - arhsService.sxRSPXferStatusChangeRpt");
                Results.RSPXferStatusChangeRptResult rspXferStatusChangeRptResult = this.sxRSPXferStatusChangeRpt(objCommon, rspXferStatusChangeRptParams);
            }else {
                log.debug("bFoundFlag = TRUE");
            }
        }
    }

    @Override
    public void sxReticleDispatchJobDeleteReq(Infos.ObjCommon objCommon, Params.ReticleDispatchJobDeleteReqParams params) {
        String reticleDispatchJobID = params.getReticleDispatchJobID();
        log.info("In parameter reticleDispatchJobID {}", reticleDispatchJobID);
        /*-----------------------*/
        /*   Delete from FSRAL   */
        /*-----------------------*/
        log.info("Delete from OSRTCLACTLIST");
        log.info("step1 - reticleMethod.reticleDispatchJobDeleteByStatusDR");
        reticleMethod.reticleDispatchJobDeleteByStatusDR(objCommon, reticleDispatchJobID);
    }

    @Override
    public void sxReticleDispatchJobInsertReq(Infos.ObjCommon objCommon, Params.ReticleDispatchJobInsertReqParams params) {
        String reticleDispatchJobID = params.getStrReticleDispatchJob().getReticleDispatchJobID();
        log.info("In parameter reticleDispatchJobID {}", reticleDispatchJobID);
        /*-----------------------*/
        /*   Insert to FSRAL   */
        /*-----------------------*/
        log.info("Insert to OSRTCLACTLIST");
        log.info("step1 - reticleMethod.reticleDispatchJobInsertDR");
        reticleMethod.reticleDispatchJobInsertDR(objCommon, Collections.singletonList(params.getStrReticleDispatchJob()));
    }


    @Override
    public void sxReticleRetrieveJobCreateReq(Infos.ObjCommon objCommon, Params.ReticleRetrieveJobCreateReqParams params) {
        /**********************/
        /* Check ARMS switch  */
        /**********************/
        String tmpArhs = StandardProperties.OM_ARHS_FLAG.getValue();
        if (log.isDebugEnabled())
            log.debug("ARHS switch on / off  tmpArhs = {}", tmpArhs);
        Validations.check(!CimStringUtils.equals(BizConstant.SP_ARMS_SWITCH_ON, tmpArhs), retCodeConfig.getFunctionNotAvailable());

        /************************************/
        /*  Check input parameter validity  */
        /************************************/
        ObjectIdentifier machineID = params.getMachineID();
        ObjectIdentifier portID = params.getPortID();
        ObjectIdentifier reticlePodID = params.getReticlePodID();
        List<Infos.MoveReticles> strMoveReticlesList = params.getMoveReticlesList();

        if (CimArrayUtils.getSize(strMoveReticlesList) > 1) {
            log.error("{} Invalid parameter. too many reticle is specified to move.", CimArrayUtils.getSize(strMoveReticlesList));
            Validations.check(true, retCodeConfig.getInvalidParameter());
        }

        /**************************/
        /*   Check machine type   */
        /**************************/
        log.debug("step1 - equipmentMethod.machineTypeGet");
        Outputs.ObjMachineTypeGetOut machineTypeGetOut = equipmentMethod.machineTypeGet(objCommon, machineID);

        String machineType = null;
        Boolean isEquipment = false;

        if (CimBooleanUtils.isFalse(machineTypeGetOut.isBStorageMachineFlag())) {
            if (log.isDebugEnabled())
                log.debug("This machine {} is Equipment.", ObjectIdentifier.fetchValue(machineID));
            isEquipment = true;
            machineType = BizConstant.SP_MACHINE_TYPE_EQP;
        } else if (CimStringUtils.equals(machineTypeGetOut.getStockerType(), BizConstant.SP_STOCKER_TYPE_BARERETICLE)) {
            if (log.isDebugEnabled())
                log.debug("This machine {} is BR Stocker.", ObjectIdentifier.fetchValue(machineID));
            isEquipment = false;
            machineType = BizConstant.SP_STOCKER_TYPE_BARERETICLE;
        } else {
            log.error("This machine {} {} is invalid for request.", ObjectIdentifier.fetchValue(machineID), machineTypeGetOut.getStockerType());
            Validations.check(true, retCodeConfigEx.getStkTypeDifferent(), machineTypeGetOut.getStockerType());
        }

        /************************************************/
        /*  Check online mode (must be online-remote)   */
        /************************************************/
        if (CimBooleanUtils.isTrue(isEquipment)) {
            log.debug("step2 - equipmentMethod.equipmentOnlineModeGet");
            String onlineModeGetOut = equipmentMethod.equipmentOnlineModeGet(objCommon, machineID);
            if (log.isDebugEnabled())
                log.debug("Equipment's online mode {}", onlineModeGetOut);
            if (!CimStringUtils.equals(onlineModeGetOut, BizConstant.SP_EQP_ONLINEMODE_ONLINEREMOTE)) {
                log.error("OnlineMode is not online-remote.");
                Validations.check(true, retCodeConfig.getEqpOnlineMode(), ObjectIdentifier.fetchValue(machineID), onlineModeGetOut);
            }
        } else {
            log.debug("step3 - stockerMethod.stockerOnlineModeGet");
            String stockerOnlineModeGetOut = stockerMethod.stockerOnlineModeGet(objCommon, machineID);
            if (!CimStringUtils.equals(stockerOnlineModeGetOut, BizConstant.SP_EQP_ONLINEMODE_ONLINEREMOTE)) {
                log.error("OnlineMode is not online-remote.");
                Validations.check(true, retCodeConfig.getEqpOnlineMode(), ObjectIdentifier.fetchValue(machineID), stockerOnlineModeGetOut);
            }
        }
        /************************************/
        /*  Check reticlePod availability   */
        /************************************/
        log.debug("step4 - reticleMethod.reticlePodAvailabilityCheck");
        reticleMethod.reticlePodAvailabilityCheck(objCommon, reticlePodID);

        /********************************************************************/
        /*  Check equipment/reticlePod/reticle combination.                 */
        /********************************************************************/
        if (CimBooleanUtils.isTrue(isEquipment)) {
            log.debug("step5 - equipmentMethod.equipmentConditionCheckForReticleRetrieve");
            equipmentMethod.equipmentConditionCheckForReticleRetrieve(objCommon,
                    machineID,
                    portID,
                    reticlePodID,
                    strMoveReticlesList);
        } else {
            log.debug("step6 - stockerMethod.stockerStatusCheckForReticleRetrieve");
            stockerMethod.stockerStatusCheckForReticleRetrieve(objCommon,
                    machineID,
                    portID,
                    reticlePodID,
                    strMoveReticlesList);
        }

        if (CimArrayUtils.getSize(strMoveReticlesList) > 0) {
            log.debug("This reticlePod have reticle(s).");
            for (Infos.MoveReticles moveReticles : strMoveReticlesList) {
                if (log.isDebugEnabled())
                    log.debug("durableID {}", ObjectIdentifier.fetchValue(moveReticles.getReticleID()));
                log.debug("step7 - durableMethod.durableDurableControlJobIDGet");
                ObjectIdentifier durableControlJobIDGetOut = durableMethod.durableDurableControlJobIDGet(objCommon,
                        moveReticles.getReticleID(),
                        BizConstant.SP_DURABLECAT_RETICLE);

                if (ObjectIdentifier.isNotEmptyWithValue(durableControlJobIDGetOut)) {
                    if (log.isDebugEnabled())
                        log.debug("durableControlJobID {} is not blank", ObjectIdentifier.fetchValue(durableControlJobIDGetOut));
                    log.debug("step8 - durableMethod.durableControlJobStatusGet");
                    objectLockMethod.objectLock(objCommon, CimDurableControlJob.class, durableControlJobIDGetOut);
                    log.debug("step9 - durableMethod.durableControlJobStatusGet");
                    Infos.DurableControlJobStatusGet durableControlJobStatusGetOut = durableMethod.durableControlJobStatusGet(objCommon, durableControlJobIDGetOut);
                    if (!CimStringUtils.equals(durableControlJobStatusGetOut.getDurableControlJobStatus(), BizConstant.SP_DURABLECONTROLJOBSTATUS_CREATED)
                            && !CimStringUtils.equals(durableControlJobStatusGetOut.getDurableControlJobStatus(), BizConstant.SP_DURABLECONTROLJOBSTATUS_DELETE)) {
                        Validations.check(true, retCodeConfig.getInvalidDcjstatus(), durableControlJobStatusGetOut.getDurableControlJobStatus());
                    }
                }
            }
        } else {
            log.debug("bReticleExistFlag = FALSE");
            log.debug("step10 - durableMethod.durableDurableControlJobIDGet");
            ObjectIdentifier durableDurableControlJobIDGetOut = durableMethod.durableDurableControlJobIDGet(objCommon, reticlePodID, BizConstant.SP_DURABLECAT_RETICLEPOD);
            if (ObjectIdentifier.isNotEmptyWithValue(durableDurableControlJobIDGetOut)) {
                if (log.isDebugEnabled())
                    log.debug("durableControlJobID {} is not blank", ObjectIdentifier.fetchValue(durableDurableControlJobIDGetOut));
                log.debug("step11 - objectLockMethod.objectLock");
                objectLockMethod.objectLock(objCommon, CimDurableControlJob.class, durableDurableControlJobIDGetOut);

                log.debug("step12 - durableMethod.durableControlJobStatusGet");
                Infos.DurableControlJobStatusGet durableControlJobStatusGetOut = durableMethod.durableControlJobStatusGet(objCommon, durableDurableControlJobIDGetOut);
                if (!CimStringUtils.equals(durableControlJobStatusGetOut.getDurableControlJobStatus(), BizConstant.SP_DURABLECONTROLJOBSTATUS_CREATED)
                        && !CimStringUtils.equals(durableControlJobStatusGetOut.getDurableControlJobStatus(), BizConstant.SP_DURABLECONTROLJOBSTATUS_DELETE)) {
                    Validations.check(true, retCodeConfig.getInvalidDcjstatus());
                }
            }
        }
        /*****************************************************************************/
        /*  Check and get reticle's following status. (move success reticles only)   */
        /*   1) if reticle is reserved by user, then return error.                   */
        /*   2) if reticle is reserved by control job, then return error.            */
        /*****************************************************************************/
        if (CimArrayUtils.isNotEmpty(strMoveReticlesList)) {
            for (Infos.MoveReticles moveReticles : strMoveReticlesList) {
                log.debug("step13 - reticleMethod.reticleReservationCheck");
                reticleMethod.reticleReservationCheck(objCommon, moveReticles.getReticleID());
            }
        }

        /**************************************************/
        /*  Check empty slots of destination reticlePod.  */
        /**************************************************/
        log.debug("step14 - reticleMethod.reticlePodVacantSlotPositionCheck");
        reticleMethod.reticlePodVacantSlotPositionCheck(objCommon, reticlePodID, strMoveReticlesList);

        /************************************/
        /*                                  */
        /*          Main Routine            */
        /*                                  */
        /************************************/
        /***********************************************************/
        /*  Create retrieve job.                                   */
        /*  Set transfer reservation and make event for watchdog   */
        /***********************************************************/
        if (CimArrayUtils.isNotEmpty(strMoveReticlesList)) {
            for (Infos.MoveReticles moveReticles : strMoveReticlesList) {
                /************************/
                /*  Create retrive job  */
                /************************/
                log.debug("step15 - reticleMethod.reticleArhsJobCreate");
                Outputs.ReticleArhsJobCreateOut reticleArhsJobCreateOut = reticleMethod.reticleArhsJobCreate(objCommon,
                        moveReticles.getReticleID(),
                        moveReticles.getSlotNumber(),
                        reticlePodID,
                        machineID,
                        portID,
                        machineID,
                        portID,
                        BizConstant.SP_RETICLEJOB_RETRIEVE);

                /******************************/
                /*  set transfer reservation  */
                /******************************/
                ObjectIdentifier dummyID = ObjectIdentifier.buildWithValue("");
                log.debug("step16 - reticleMethod.reticleReticlePodReserve");
                reticleMethod.reticleReticlePodReserve(objCommon,
                        moveReticles.getReticleID(),
                        reticlePodID,
                        dummyID);

                /******************************/
                /*  make event for watchdog   */
                /******************************/
                Infos.ReticleEventRecord reticleEventRecord = new Infos.ReticleEventRecord();
                reticleEventRecord.setEventTime(CimDateUtils.convertToSpecString(objCommon.getTimeStamp().getReportTimeStamp()));
                reticleEventRecord.setReticleDispatchJobID(null == reticleArhsJobCreateOut ? null : reticleArhsJobCreateOut.getReticleDispatchJobID());
                reticleEventRecord.setReticleComponentJobID(null == reticleArhsJobCreateOut ? null : reticleArhsJobCreateOut.getReticleComponentJobID());

                log.debug("step17 - reticleMethod.reticleEventQueuePutDR");
                reticleMethod.reticleEventQueuePutDR(objCommon, reticleEventRecord);
            }
        }
    }

    @Override
    public void sxReticleStoreJobCreateReq(Infos.ObjCommon objCommon, Params.ReticleStoreJobCreateReqParams params) {
        ObjectIdentifier machineID = params.getMachineID();
        ObjectIdentifier portID = params.getPortID();
        ObjectIdentifier reticlePodID = params.getReticlePodID();
        List<Infos.MoveReticles> strMoveReticlesSeq = params.getMoveReticlesList();

        String tmpArhs = StandardProperties.OM_ARHS_FLAG.getValue();
        if (log.isDebugEnabled())
            log.debug("ARHS switch on / off  tmpArhs = {}", tmpArhs);
        Validations.check(!CimStringUtils.equals(BizConstant.SP_ARMS_SWITCH_ON, tmpArhs), retCodeConfig.getFunctionNotAvailable());

        boolean bFoundFlag = false;
        int moveReticlesSize = CimArrayUtils.getSize(strMoveReticlesSeq);
        Validations.check(moveReticlesSize > 1 || 0 == moveReticlesSize, retCodeConfig.getInvalidParameter());

        /*-------------------------------*/
        /*   Check equipment condition   */
        /*-------------------------------*/
        Outputs.ObjMachineTypeGetOut objMachineTypeGetOut = equipmentMethod.machineTypeGet(objCommon, machineID);

        if (CimBooleanUtils.isFalse(objMachineTypeGetOut.isBStorageMachineFlag())) {
            log.debug("bStorageMachineFlag = FALSE");
            String onlineMode = equipmentMethod.equipmentOnlineModeGet(objCommon, machineID);
            Validations.check(!CimStringUtils.equals(onlineMode, BizConstant.SP_EQP_ONLINEMODE_ONLINEREMOTE), retCodeConfig.getEqpOnlineMode(), machineID, onlineMode);

            Outputs.ObjEquipmentReticlePodPortInfoGetDROut objEquipmentReticlePodPortInfoGetDROut = equipmentMethod.equipmentReticlePodPortInfoGetDR(objCommon, machineID);

            List<Infos.ReticlePodPortInfo> reticlePodPortInfoList = objEquipmentReticlePodPortInfoGetDROut.getReticlePodPortInfoList();
            for (Infos.ReticlePodPortInfo reticlePodPortInfo : reticlePodPortInfoList) {
                if (ObjectIdentifier.equalsWithValue(reticlePodPortInfo.getReticlePodPortID(), portID) &&
                        ObjectIdentifier.equalsWithValue(reticlePodPortInfo.getLoadedReticlePodID(), reticlePodID)) {
                    if (log.isDebugEnabled())
                        log.debug("Found reticlePodPort and loadedReticlePod. {} {}", reticlePodPortInfo.getReticlePodPortID(), reticlePodPortInfo.getLoadedReticlePodID());
                    bFoundFlag = true;
                    break;
                }
            }

            Validations.check(!bFoundFlag, retCodeConfigEx.getRtclpodNotLoaded(), reticlePodID, machineID, portID);

            //Capacity check
            equipmentMethod.machineCapacityCheckForReticleStore(objCommon, machineID);
        } else if (CimStringUtils.equals(objMachineTypeGetOut.getStockerType(), BizConstant.SP_STOCKER_TYPE_BARERETICLE)) {
            log.debug("stockerType = SP_Stocker_Type_BareReticle");
            Outputs.ObjStockerTypeGetDROut objStockerTypeGetDROut = stockerMethod.stockerTypeGetDR(objCommon, machineID);
            Validations.check(!CimStringUtils.equals(objStockerTypeGetDROut.getStockerType(), BizConstant.SP_STOCKER_TYPE_BARERETICLE), retCodeConfig.getInvalidParameter());

            String onlineMode = stockerMethod.stockerOnlineModeGet(objCommon, machineID);
            Validations.check(!CimStringUtils.equals(onlineMode, BizConstant.SP_EQP_ONLINEMODE_ONLINEREMOTE), retCodeConfig.getEqpOnlineMode());

            List<Infos.ReticlePodPortInfo> reticlePodPortInfos = stockerMethod.stockerReticlePodPortInfoGetDR(objCommon, machineID);

            for (Infos.ReticlePodPortInfo reticlePodPortInfo : reticlePodPortInfos) {
                if (ObjectIdentifier.equalsWithValue(reticlePodPortInfo.getReticlePodPortID(), portID) &&
                        ObjectIdentifier.equalsWithValue(reticlePodPortInfo.getLoadedReticlePodID(), reticlePodID)) {
                    if (log.isDebugEnabled())
                        log.debug("Found reticlePodPort and loadedReticlePod. {} {}", reticlePodPortInfo.getReticlePodPortID(), reticlePodPortInfo.getLoadedReticlePodID());
                    bFoundFlag = true;
                    break;
                }
            }
            Validations.check(!bFoundFlag, retCodeConfigEx.getRtclpodNotLoaded(), reticlePodID, machineID, portID);

            //Check capacity
            equipmentMethod.machineCapacityCheckForReticleStore(objCommon, machineID);
        } else {
            log.error("Invalid input parameter");
            Validations.check(retCodeConfig.getInvalidInputParam());
        }

        /*-------------------------------------------------------------------*/
        /* If input reticle is reserved for control job,                     */
        /* that reticle has to be in the same equipment of input equipment   */
        /*-------------------------------------------------------------------*/
        Outputs.ReticleReservationDetailInfoGetOut reticleReservationDetailInfoGetOut = reticleMethod.reticleReservationDetailInfoGet(objCommon, strMoveReticlesSeq.get(0).getReticleID());
        Validations.check(reticleReservationDetailInfoGetOut.isReservedFlag(), retCodeConfigEx.getAlreadyReservedReticle());

        /*--------------------------------------------------*/
        /*   Check reticle/slot combination in reticlePod   */
        /*--------------------------------------------------*/
        Params.ReticlePodDetailInfoInqParams reticlePodDetailInfoInqParams = new Params.ReticlePodDetailInfoInqParams();
        reticlePodDetailInfoInqParams.setReticlePodID(reticlePodID);
        reticlePodDetailInfoInqParams.setDurableOperationInfoFlag(false);
        reticlePodDetailInfoInqParams.setDurableWipOperationInfoFlag(false);
        Outputs.ObjReticlePodFillInTxPDQ013DROut objReticlePodFillInTxPDQ013DROut = reticleMethod.reticlePodFillInTxPDQ013DR(objCommon, reticlePodDetailInfoInqParams);

        bFoundFlag = false;
        Infos.ReticlePodStatusInfo reticlePodStatusInfo = objReticlePodFillInTxPDQ013DROut.getReticlePodStatusInfo();
        if (!CimObjectUtils.isEmpty(reticlePodStatusInfo) && CimArrayUtils.isNotEmpty(reticlePodStatusInfo.getStrContainedReticleInfo())) {
            for (Infos.ContainedReticleInfo containedReticleInfo : reticlePodStatusInfo.getStrContainedReticleInfo()) {

                if (ObjectIdentifier.equalsWithValue(containedReticleInfo.getReticleID(), strMoveReticlesSeq.get(0).getReticleID()) &&
                        containedReticleInfo.getSlotNo() == strMoveReticlesSeq.get(0).getSlotNumber()) {
                    if (log.isDebugEnabled())
                        log.debug("Stored reticle/slot is {} {}", containedReticleInfo.getReticleID(), containedReticleInfo.getSlotNo());
                    bFoundFlag = true;
                    break;
                }
            }
        }

        if (log.isDebugEnabled())
            log.debug("bFoundFlag {}", bFoundFlag);
        Validations.check(!bFoundFlag, retCodeConfigEx.getRtclNotInReclPod(), strMoveReticlesSeq.get(0).getReticleID(), reticlePodID, strMoveReticlesSeq.get(0).getSlotNumber());

        ObjectIdentifier durableControlJobID = durableMethod.durableDurableControlJobIDGet(objCommon, strMoveReticlesSeq.get(0).getReticleID(), BizConstant.SP_DURABLECAT_RETICLE);

        if (!ObjectIdentifier.isEmpty(durableControlJobID)) {
            if (log.isDebugEnabled())
                log.debug("durableControlJobID is not blank {}", durableControlJobID);
            objectLockMethod.objectLock(objCommon, CimDurableControlJob.class, durableControlJobID);

            Infos.DurableControlJobStatusGet durableControlJobStatusGet = durableMethod.durableControlJobStatusGet(objCommon, durableControlJobID);
            String durableControlJobStatus = durableControlJobStatusGet.getDurableControlJobStatus();
            Validations.check(!CimStringUtils.equals(durableControlJobStatus, BizConstant.SP_DURABLECONTROLJOBSTATUS_CREATED) &&
                    !CimStringUtils.equals(durableControlJobStatus, BizConstant.SP_DURABLECONTROLJOBSTATUS_DELETE), retCodeConfig.getInvalidDcjstatus(), durableControlJobStatus);
        }

        /*---------------------------------------------------------------------------------*/
        /*   if requested reticle/reticlePod is not in RDJ/RCJ List, Create RDJ/RCJ List   */
        /*---------------------------------------------------------------------------------*/
        Outputs.ReticleArhsJobCreateOut reticleArhsJobCreateOut = reticleMethod.reticleArhsJobCreate(objCommon, strMoveReticlesSeq.get(0).getReticleID(),
                strMoveReticlesSeq.get(0).getSlotNumber(), reticlePodID, machineID, portID, machineID, portID, BizConstant.SP_RETICLEJOB_STORE);

        /*----------------*/
        /*   Event Make   */
        /*----------------*/
        Infos.ReticleEventRecord reticleEventRecord = new Infos.ReticleEventRecord();
        reticleEventRecord.setEventTime(objCommon.getTimeStamp().getReportTimeStamp().toString());
        reticleEventRecord.setReticleDispatchJobID(reticleArhsJobCreateOut.getReticleDispatchJobID());
        reticleEventRecord.setReticleComponentJobID(reticleArhsJobCreateOut.getReticleComponentJobID());
        reticleMethod.reticleEventQueuePutDR(objCommon, reticleEventRecord);
    }

    @Override
    public Results.ReticlePodXferReqResult sxReticlePodXferReq(Infos.ObjCommon objCommon, Params.ReticlePodXferReqParams params) {
        Results.ReticlePodXferReqResult reuslt = new Results.ReticlePodXferReqResult();
        String reticleDispatchJobID = params.getReticleDispatchJobID();
        String reticleComponentJobID = params.getReticleComponentJobID();
        ObjectIdentifier reticlePodID = params.getReticlePodID();
        ObjectIdentifier fromMachineID = params.getFromMachineID();
        ObjectIdentifier fromPortID = params.getFromPortID();
        ObjectIdentifier toMachineID = params.getToMachineID();
        ObjectIdentifier toPortID = params.getToPortID();

        String tmpArhs = StandardProperties.OM_ARHS_FLAG.getValue();
        if (log.isDebugEnabled())
            log.debug("ARHS switch on / off  tmpArhs = {}", tmpArhs);
        Validations.check(!CimStringUtils.equals(BizConstant.SP_ARMS_SWITCH_ON, tmpArhs), retCodeConfig.getFunctionNotAvailable());

        /*-------------------*/
        /*   Check RDJ/RCJ   */
        /*-------------------*/
        log.debug("step1 - reticleMethod.reticleDispatchJobListGetDR");
        List<Infos.ReticleDispatchJob> reticleDispatchJobList = reticleMethod.reticleDispatchJobListGetDR(objCommon, reticleDispatchJobID);
        if (CimArrayUtils.isEmpty(reticleDispatchJobList)) {
            log.error("Not Found RDJ");
            Validations.check(true, retCodeConfigEx.getRdjNotFound());
        } else if (CimArrayUtils.getSize(reticleDispatchJobList) > 1) {
            log.error("This reticleDispatchJob is duplicate job");
            Validations.check(true, retCodeConfigEx.getRdjDuplicate());
        }

        if (!ObjectIdentifier.equalsWithValue(reticleDispatchJobList.get(0).getReticlePodID(), reticlePodID)) {
            log.error("This request Mismatch RDJ {} information. pod/toEqp", ObjectIdentifier.fetchValue(reticleDispatchJobList.get(0).getReticlePodID()));
            Validations.check(true, retCodeConfigEx.getInvalidRequestToRdj());
        }

        log.debug("step2 - reticleMethod.reticleComponentJobListGetDR");
        List<Infos.ReticleComponentJob> reticleComponentJobList = reticleMethod.reticleComponentJobListGetDR(objCommon, reticleDispatchJobID);
        if (CimArrayUtils.isEmpty(reticleComponentJobList)) {
            log.error("Not found RCJ.");
            Validations.check(true, retCodeConfigEx.getRcjNotFound());
        }

        Boolean bFoundFlag = false;
        for (Infos.ReticleComponentJob reticleComponentJob : reticleComponentJobList) {
            if (CimStringUtils.equals(reticleComponentJob.getReticleComponentJobID(), reticleComponentJobID)
                    && ObjectIdentifier.equalsWithValue(reticleComponentJob.getReticlePodID(), reticlePodID)
                    && ObjectIdentifier.equalsWithValue(reticleComponentJob.getFromEquipmentID(), fromMachineID)
                    && ObjectIdentifier.equalsWithValue(reticleComponentJob.getFromReticlePodPortID(), fromPortID)
                    && ObjectIdentifier.equalsWithValue(reticleComponentJob.getToEquipmentID(), toMachineID)
                    && ObjectIdentifier.equalsWithValue(reticleComponentJob.getToReticlePodPortID(), toPortID)
                    && CimStringUtils.equals(reticleComponentJob.getJobName(), BizConstant.SP_RCJ_JOBNAME_XFER)) {
                if (log.isDebugEnabled()){
                    log.debug("Found RCJ information.");
                    log.debug("reticleComponentJobID: {}", reticleComponentJob.getReticleComponentJobID());
                    log.debug("fromEquipmentID: {}", ObjectIdentifier.fetchValue(reticleComponentJob.getFromEquipmentID()));
                    log.debug("fromReticlePodPortID: {}", ObjectIdentifier.fetchValue(reticleComponentJob.getFromReticlePodPortID()));
                    log.debug("toEquipmentID: {}", ObjectIdentifier.fetchValue(reticleComponentJob.getToEquipmentID()));
                    log.debug("toReticlePodPortID: {}", ObjectIdentifier.fetchValue(reticleComponentJob.getToReticlePodPortID()));
                    log.debug("jobName: {}", reticleComponentJob.getReticleComponentJobID());
                }
                bFoundFlag = true;
                break;
            }
        }
        if (CimBooleanUtils.isFalse(bFoundFlag)) {
            log.error("This request mismatch RCJ information.");
            Validations.check(true, retCodeConfigEx.getInvalidRequestToRdj());
        }

        /*----------------------------------------------*/
        /*   Get ReticldPod Information                 */
        /*----------------------------------------------*/
        Params.ReticlePodDetailInfoInqParams reticlePodDetailInfoInqParams = new Params.ReticlePodDetailInfoInqParams();
        reticlePodDetailInfoInqParams.setDurableOperationInfoFlag(false);
        reticlePodDetailInfoInqParams.setDurableWipOperationInfoFlag(false);
        reticlePodDetailInfoInqParams.setReticlePodID(reticlePodID);
        log.debug("step3 - reticleMethod.reticlePodFillInTxPDQ013DR");
        Outputs.ObjReticlePodFillInTxPDQ013DROut reticlePodFillInTxPDQ013DROut = reticleMethod.reticlePodFillInTxPDQ013DR(objCommon, reticlePodDetailInfoInqParams);

        /*----------------------------------*/
        /*   Check from Machine condition   */
        /*----------------------------------*/
        Long fromMachienType = 0L;
        log.debug("step4 - equipmentMethod.machineTypeGet");
        Outputs.ObjMachineTypeGetOut fromMachineTypeGetOut = equipmentMethod.machineTypeGet(objCommon, fromMachineID);

        if (CimBooleanUtils.isFalse(fromMachineTypeGetOut.isBStorageMachineFlag())) {
            log.debug("fromMachine type is Equipment");
            fromMachienType = 1L;
        } else {
            if (CimStringUtils.equals(fromMachineTypeGetOut.getStockerType(), BizConstant.SP_STOCKER_TYPE_BARERETICLE)) {
                log.debug("fromMachine type is BareReticleStocker");
                fromMachienType = 2L;
            } else if (CimStringUtils.equals(fromMachineTypeGetOut.getStockerType(), BizConstant.SP_STOCKER_TYPE_RETICLEPOD)) {
                log.debug("fromMachine type is ReticlePodStocker");
                fromMachienType = 3L;
            } else {
                log.error("machine type is invalid.");
                Validations.check(true, retCodeConfigEx.getRtclPodDestInvalid());
            }
        }

        Infos.ReticlePodPortInfo fromReticlePodPortInfo = null;
        String fromMachineOnlineMode = BizConstant.SP_EQP_ONLINEMODE_ONLINEREMOTE;
        if (fromMachienType == 1L) {
            log.debug("step5 - equipmentMethod.equipmentReticlePodPortInfoGetDR");
            Outputs.ObjEquipmentReticlePodPortInfoGetDROut equipmentReticlePodPortInfoGetDROut = equipmentMethod.equipmentReticlePodPortInfoGetDR(objCommon, fromMachineID);
            Boolean bPodOnPortFlag = false;
            if (null != equipmentReticlePodPortInfoGetDROut && CimArrayUtils.isNotEmpty(equipmentReticlePodPortInfoGetDROut.getReticlePodPortInfoList())) {
                for (Infos.ReticlePodPortInfo reticlePodPortInfo : equipmentReticlePodPortInfoGetDROut.getReticlePodPortInfoList()) {
                    if (ObjectIdentifier.equalsWithValue(reticlePodPortInfo.getReticlePodPortID(), fromPortID)
                            && ObjectIdentifier.equalsWithValue(reticlePodPortInfo.getLoadedReticlePodID(), reticlePodID)) {
                        log.debug("Found requested reticlePod on Port.");
                        fromReticlePodPortInfo = reticlePodPortInfo;
                        bPodOnPortFlag = true;
                        break;
                    }
                }
            }
            if (CimBooleanUtils.isFalse(bPodOnPortFlag)) {
                log.error("Not found reticlePod on fromPort");
                Validations.check(true, retCodeConfigEx.getRtclpodNotLoaded(),
                        ObjectIdentifier.fetchValue(reticlePodID),
                        ObjectIdentifier.fetchValue(fromMachineID),
                        ObjectIdentifier.fetchValue(fromPortID));
            }

            log.debug("step6 - equipmentMethod.equipmentOnlineModeGet");
            String onlineModeGetOut = equipmentMethod.equipmentOnlineModeGet(objCommon, fromMachineID);
            fromMachineOnlineMode = onlineModeGetOut;
            if (log.isDebugEnabled()){
                log.debug("From Equipment's online-mode is: {}", fromMachineOnlineMode);
            }
        }
        if (fromMachienType == 2L) {
            log.debug("step7 - stockerMethod.stockerReticlePodPortInfoGetDR");
            List<Infos.ReticlePodPortInfo> reticlePodPortInfoList = stockerMethod.stockerReticlePodPortInfoGetDR(objCommon, fromMachineID);
            Boolean bPodOnPortFlag = false;
            if (CimArrayUtils.isNotEmpty(reticlePodPortInfoList)) {
                for (Infos.ReticlePodPortInfo reticlePodPortInfo : reticlePodPortInfoList) {
                    if (ObjectIdentifier.equalsWithValue(reticlePodPortInfo.getReticlePodPortID(), fromPortID)
                            && ObjectIdentifier.equalsWithValue(reticlePodPortInfo.getLoadedReticlePodID(), reticlePodID)) {
                        log.debug("Found requested reticlePod on Port.");
                        fromReticlePodPortInfo = reticlePodPortInfo;
                        bPodOnPortFlag = true;
                        break;
                    }
                }
            }
            if (CimBooleanUtils.isFalse(bPodOnPortFlag)) {
                log.error("Not found reticlePod on fromPort");
                Validations.check(true, retCodeConfigEx.getRtclpodNotLoaded(),
                        ObjectIdentifier.fetchValue(reticlePodID),
                        ObjectIdentifier.fetchValue(fromMachineID),
                        ObjectIdentifier.fetchValue(fromPortID));
            }

            log.debug("step8 - stockerMethod.stockerOnlineModeGet");
            String stockerOnlineModeGetOut = stockerMethod.stockerOnlineModeGet(objCommon, fromMachineID);
            fromMachineOnlineMode = stockerOnlineModeGetOut;
        }
        if (fromMachienType == 3L) {
            if (!ObjectIdentifier.equalsWithValue(reticlePodFillInTxPDQ013DROut.getReticlePodStatusInfo().getStockerID(), fromMachineID)
                    || (!CimStringUtils.equals(reticlePodFillInTxPDQ013DROut.getReticlePodStatusInfo().getTransferStatus(), BizConstant.SP_TRANSSTATE_STATIONIN)
                    && !CimStringUtils.equals(reticlePodFillInTxPDQ013DROut.getReticlePodStatusInfo().getTransferStatus(), BizConstant.SP_TRANSSTATE_MANUALIN)
                    && !CimStringUtils.equals(reticlePodFillInTxPDQ013DROut.getReticlePodStatusInfo().getTransferStatus(), BizConstant.SP_TRANSSTATE_BAYIN)
                    && !CimStringUtils.equals(reticlePodFillInTxPDQ013DROut.getReticlePodStatusInfo().getTransferStatus(), BizConstant.SP_TRANSSTATE_INTERMEDIATEIN)
                    && !CimStringUtils.equals(reticlePodFillInTxPDQ013DROut.getReticlePodStatusInfo().getTransferStatus(), BizConstant.SP_TRANSSTATE_ABNORMALIN))) {
                log.error("Not found reticlePod in RSP Stocker");
                Validations.check(true, retCodeConfigEx.getRtclpodNotLoaded(),
                        ObjectIdentifier.fetchValue(reticlePodID),
                        ObjectIdentifier.fetchValue(fromMachineID),
                        ObjectIdentifier.fetchValue(fromPortID));
            }
        }
        if (fromMachienType == 1L || fromMachienType == 2L) {
            if (!CimStringUtils.equals(fromReticlePodPortInfo.getAccessMode(), BizConstant.SP_EQP_ACCESSMODE_AUTO)) {
                log.error("fromPort accessMode {} is not Auto.", fromReticlePodPortInfo.getAccessMode());
                Validations.check(true, retCodeConfigEx.getEqpPortAccessMode(),
                        ObjectIdentifier.fetchValue(fromMachineID),
                        ObjectIdentifier.fetchValue(fromPortID),
                        fromReticlePodPortInfo.getAccessMode());
            }
            if (!CimStringUtils.equals(fromMachineOnlineMode, BizConstant.SP_EQP_ONLINEMODE_OFFLINE)
                    && !CimStringUtils.equals(fromReticlePodPortInfo.getPortStatus(), BizConstant.SP_PORTRSC_PORTSTATE_UNLOADREQ)) {
                log.error("fromPort is not UnloadReq. portState: {}", fromReticlePodPortInfo.getPortStatus());
                Validations.check(true, retCodeConfigEx.getInvalidRspPortStatus(),
                        ObjectIdentifier.fetchValue(fromMachineID),
                        ObjectIdentifier.fetchValue(fromPortID));
            }
            if (!CimStringUtils.equals(fromReticlePodPortInfo.getDispatchStatus(), BizConstant.SP_PORTRSC_DISPATCHSTATE_REQUIRED)) {
                log.error("fromPort dispatchState is {}", fromReticlePodPortInfo.getDispatchStatus());
                Validations.check(true, retCodeConfigEx.getInvalidMachinePortDispatchState(),
                        ObjectIdentifier.fetchValue(fromMachineID),
                        ObjectIdentifier.fetchValue(fromPortID),
                        fromReticlePodPortInfo.getDispatchStatus());
            }
            if (!CimStringUtils.equals(fromReticlePodPortInfo.getTransferReserveStatus(), BizConstant.SP_RETICLEPODPORT_NOTRESERVED)) {
                log.error("fromPort is reserved");
                Validations.check(true, retCodeConfigEx.getInvalidMachinePortTransRsrvState(),
                        ObjectIdentifier.fetchValue(fromMachineID),
                        ObjectIdentifier.fetchValue(fromPortID),
                        fromReticlePodPortInfo.getTransferReserveStatus());
            }
        }
        if (fromMachienType == 2L || fromMachienType == 3L) {
            log.debug("fromMachineType is 2 or 3.");
            log.debug("step9 - equipmentMethod.equipmentCheckAvail");
            equipmentMethod.equipmentCheckAvail(objCommon, fromMachineID);

        }
        /*--------------------------------*/
        /*   Check to Machine condition   */
        /*--------------------------------*/
        Long toMachineType = 0L;
        log.debug("step10 - equipmentMethod.machineTypeGet");
        Outputs.ObjMachineTypeGetOut toMachineTypeGetOut = equipmentMethod.machineTypeGet(objCommon, toMachineID);
        if (CimBooleanUtils.isFalse(toMachineTypeGetOut.isBStorageMachineFlag())) {
            log.debug("toMachine type is Equipment");
            toMachineType = 1L;
        } else {
            if (CimStringUtils.equals(toMachineTypeGetOut.getStockerType(), BizConstant.SP_STOCKER_TYPE_BARERETICLE)) {
                log.debug("toMachine type is BareReticleStocker");
                toMachineType = 2L;
            } else if (CimStringUtils.equals(toMachineTypeGetOut.getStockerType(), BizConstant.SP_STOCKER_TYPE_RETICLEPOD)) {
                log.debug("toMachine type is ReticlePodStocker");
                toMachineType = 3L;
            } else {
                log.error("machine type is invalid.");
                Validations.check(true, retCodeConfigEx.getRtclPodDestInvalid(),
                        ObjectIdentifier.fetchValue(reticlePodID),
                        ObjectIdentifier.fetchValue(toMachineID));
            }
        }
        Infos.ReticlePodPortInfo toReticlePodPortInfo = null;
        String toMachineOnlineMode = BizConstant.SP_EQP_ONLINEMODE_ONLINEREMOTE;
        if (toMachineType == 1L) {
            log.debug("step11 - equipmentMethod.equipmentReticlePodPortInfoGetDR");
            Outputs.ObjEquipmentReticlePodPortInfoGetDROut equipmentReticlePodPortInfoGetDROut = equipmentMethod.equipmentReticlePodPortInfoGetDR(objCommon, toMachineID);
            Boolean bFoundPortFlag = false;
            if (null != equipmentReticlePodPortInfoGetDROut && CimArrayUtils.isNotEmpty(equipmentReticlePodPortInfoGetDROut.getReticlePodPortInfoList())) {
                for (Infos.ReticlePodPortInfo reticlePodPortInfo : equipmentReticlePodPortInfoGetDROut.getReticlePodPortInfoList()) {
                    if (ObjectIdentifier.equalsWithValue(reticlePodPortInfo.getReticlePodPortID(), toPortID)) {
                        log.debug("Found requested reticlePod on Port.");
                        toReticlePodPortInfo = reticlePodPortInfo;
                        bFoundPortFlag = true;
                        break;
                    }
                }
            }
            if (CimBooleanUtils.isFalse(bFoundPortFlag)) {
                log.error("Not found toReticlePodPort");
                Validations.check(true, retCodeConfig.getRspportNotFound(),
                        ObjectIdentifier.fetchValue(toMachineID),
                        ObjectIdentifier.fetchValue(toPortID));
            }

            log.debug("step12 - equipmentMethod.equipmentOnlineModeGet");
            String equipmentOnlineModeGetOut = equipmentMethod.equipmentOnlineModeGet(objCommon, toMachineID);
            toMachineOnlineMode = equipmentOnlineModeGetOut;
            if (log.isDebugEnabled())
                log.debug("To Equipment's online-mode is: {}", toMachineOnlineMode);
        }
        if (toMachineType == 2L) {
            log.debug("step13 - stockerMethod.stockerReticlePodPortInfoGetDR");
            List<Infos.ReticlePodPortInfo> reticlePodPortInfoList = stockerMethod.stockerReticlePodPortInfoGetDR(objCommon, toMachineID);
            Boolean bFoundPortFlag = false;
            if (CimArrayUtils.isNotEmpty(reticlePodPortInfoList)) {
                for (Infos.ReticlePodPortInfo reticlePodPortInfo : reticlePodPortInfoList) {
                    if (ObjectIdentifier.equalsWithValue(reticlePodPortInfo.getReticlePodPortID(), toPortID)) {
                        log.debug("Found requested reticlePodPort.");
                        toReticlePodPortInfo = reticlePodPortInfo;
                        bFoundPortFlag = true;
                        break;
                    }
                }
            }
            if (CimBooleanUtils.isFalse(bFoundPortFlag)) {
                log.error("Not found reticlePodPort");
                Validations.check(true, retCodeConfig.getRspportNotFound(),
                        ObjectIdentifier.fetchValue(toMachineID),
                        ObjectIdentifier.fetchValue(toPortID));
            }

            log.debug("step14 - stockerMethod.stockerOnlineModeGet");
            String stockerOnlineModeGetOut = stockerMethod.stockerOnlineModeGet(objCommon, toMachineID);
            toMachineOnlineMode = stockerOnlineModeGetOut;
            if (log.isDebugEnabled())
                log.debug("to BRStocker's online-mode is: {}", toMachineOnlineMode);
        }

        if (toMachineType == 1L || toMachineType == 2L) {
            if (!CimStringUtils.equals(toReticlePodPortInfo.getAccessMode(), BizConstant.SP_EQP_ACCESSMODE_AUTO)) {
                log.error("toPort accessMode {} is not Auto.", toReticlePodPortInfo.getAccessMode());
                Validations.check(true, retCodeConfigEx.getEqpPortAccessMode(),
                        ObjectIdentifier.fetchValue(toMachineID),
                        ObjectIdentifier.fetchValue(toPortID),
                        toReticlePodPortInfo.getAccessMode());
            }
            if (!CimStringUtils.equals(toMachineOnlineMode, BizConstant.SP_EQP_ONLINEMODE_OFFLINE)
                    && !CimStringUtils.equals(toReticlePodPortInfo.getPortStatus(), BizConstant.SP_PORTRSC_PORTSTATE_LOADREQ)) {
                log.error("toPort is not UnloadReq. portState: {}", toReticlePodPortInfo.getPortStatus());
                Validations.check(true, retCodeConfigEx.getInvalidRspPortStatus(),
                        ObjectIdentifier.fetchValue(toMachineID),
                        ObjectIdentifier.fetchValue(toPortID));
            }
            if (!CimStringUtils.equals(toReticlePodPortInfo.getDispatchStatus(), BizConstant.SP_PORTRSC_DISPATCHSTATE_REQUIRED)) {
                log.error("toPort dispatchState is {}", toReticlePodPortInfo.getDispatchStatus());
                Validations.check(true, retCodeConfigEx.getInvalidMachinePortDispatchState(),
                        ObjectIdentifier.fetchValue(toMachineID),
                        ObjectIdentifier.fetchValue(toPortID),
                        toReticlePodPortInfo.getDispatchStatus());
            }
            if (!CimStringUtils.equals(toReticlePodPortInfo.getTransferReserveStatus(), BizConstant.SP_RETICLEPODPORT_NOTRESERVED)) {
                log.error("toPort is reserved.");
                Validations.check(true, retCodeConfigEx.getInvalidMachinePortTransRsrvState(),
                        ObjectIdentifier.fetchValue(toMachineID),
                        ObjectIdentifier.fetchValue(toPortID),
                        toReticlePodPortInfo.getTransferReserveStatus());
            }
        }
        if (toMachineType == 2L || toMachineType == 3L) {
            log.debug("toMachineType is 2 or 3.");
            log.debug("step15 - equipmentMethod.equipmentCheckAvail");
            equipmentMethod.equipmentCheckAvail(objCommon, toMachineID);
        }

        /*----------------------------------------------*/
        /*   Confirm whether reticle is in reticlePod   */
        /*----------------------------------------------*/
        Boolean bReticleExistFlag = false;
        if (CimArrayUtils.isNotEmpty(reticlePodFillInTxPDQ013DROut.getReticlePodStatusInfo().getStrContainedReticleInfo())) {
            log.debug("This reticlePod have reticle(s).");
            bReticleExistFlag = true;
        }
        if (CimBooleanUtils.isTrue(bReticleExistFlag)) {
            for (Infos.ContainedReticleInfo containedReticleInfo : reticlePodFillInTxPDQ013DROut.getReticlePodStatusInfo().getStrContainedReticleInfo()) {
                ObjectIdentifier reticleID = containedReticleInfo.getReticleID();

                /*-------------------------------*/
                /*   Check reticles controlJob   */
                /*-------------------------------*/
                log.debug("step16 - reticleMethod.reticlecontrolJobInfoGet");
                Outputs.ReticleControlJobInfoGetOut reticleControlJobInfoGetOut = reticleMethod.reticlecontrolJobInfoGet(objCommon, reticleID);
                if (null != reticleControlJobInfoGetOut && CimArrayUtils.isNotEmpty(reticleControlJobInfoGetOut.getStrControlJobAttributeInfoSeq())) {
                    //--------------------------------------------------
                    //   Get OM_RETICLE_RETRIEVE_WHEN_LOT_PROCESS
                    //--------------------------------------------------
                    Long retrieveReticleDuringLotProcFlag = StandardProperties.OM_RETICLE_RETRIEVE_WHEN_LOT_PROCESS.getLongValue();
                    if (log.isDebugEnabled())
                        log.debug("Env OM_RETICLE_RETRIEVE_WHEN_LOT_PROCESS {}", retrieveReticleDuringLotProcFlag);
                    if (1L == retrieveReticleDuringLotProcFlag) {
                        log.debug("Check if reticle has reserve control job");
                        for (Outputs.ControlJobAttributeInfo controlJobAttributeInfo : reticleControlJobInfoGetOut.getStrControlJobAttributeInfoSeq()) {
                            if (CimStringUtils.equals(BizConstant.SP_CONTROLJOBSTATUS_CREATED, controlJobAttributeInfo.getControlJobStatus())) {
                                log.error("controlJob status is Created");
                                Validations.check(true, retCodeConfigEx.getRtclHasCtrljob(), ObjectIdentifier.fetchValue(reticleID));
                            }
                        }
                    } else {
                        log.error("This reticle {} have controlJob.", ObjectIdentifier.fetchValue(reticleID));
                        Validations.check(true, retCodeConfigEx.getRtclHasCtrljob(), ObjectIdentifier.fetchValue(reticleID));
                    }
                }
            }
        }
        /*---------------------------------------------------------------*/
        /*   Reserve from port when that is EQP or BareReticle Stocker   */
        /*---------------------------------------------------------------*/
        if (fromMachienType == 1L || fromMachienType == 2L) {
            log.debug("fromMachineType is 1 or 2.");
            log.debug("step17 - equipmentMethod.machineReticlePodPortReserve");
            equipmentMethod.machineReticlePodPortReserve(objCommon,
                    fromMachineID,
                    fromPortID,
                    reticlePodID);
        }

        /*-------------------------------------------------------------*/
        /*   Reserve to port when that is EQP or BareReticle Stocker   */
        /*-------------------------------------------------------------*/
        if (toMachineType == 1L || toMachineType == 2L) {
            log.debug("toMachineType is 1 or 2.");
            log.debug("step18 - equipmentMethod.machineReticlePodPortReserve");
            equipmentMethod.machineReticlePodPortReserve(objCommon,
                    toMachineID,
                    toPortID,
                    reticlePodID);
        }

        /*---------------------------------*/
        /*   ReticlePod transfer reserve   */
        /*---------------------------------*/
        log.debug("step19 - reticleMethod.reticlePodTransferReserve");
        reticleMethod.reticlePodTransferReserve(objCommon,
                toMachineID,
                toPortID,
                reticlePodID);

        /*--------------------------------------------------*/
        /*   Send reticle pod transfer information to RTMS  */
        /*--------------------------------------------------*/
        Params.TransportJobCreateReqParams transportJobCreateReqParam = new Params.TransportJobCreateReqParams();
        Infos.JobCreateArray jobCreateData = new Infos.JobCreateArray();
        jobCreateData.setCarrierID(reticlePodID);
        jobCreateData.setFromMachineID(fromMachineID);
        jobCreateData.setFromPortID(fromPortID);
        Infos.ToDestination toDestination = new Infos.ToDestination();
        toDestination.setToMachineID(toMachineID);
        toDestination.setToPortID(toPortID);
        jobCreateData.setToMachine(Collections.singletonList(toDestination));
        transportJobCreateReqParam.setJobCreateData(Collections.singletonList(jobCreateData));
        log.debug("step20 - tmsService.rtransportJobCreateReq");
        tmsService.rtransportJobCreateReq(objCommon, objCommon.getUser(), transportJobCreateReqParam);

        /*---------------------------*/
        /*   Update RDJ/RCJ status   */
        /*---------------------------*/
        log.debug("step21 - reticleMethod.reticleJobStatusUpdateByRequestDR");
        reticleMethod.reticleJobStatusUpdateByRequestDR(objCommon,
                reticleDispatchJobID,
                reticleComponentJobID,
                true);

        return reuslt;
    }

    @Override
    public void sxReticleDispatchAndComponentJobStatusChangeReq(Infos.ObjCommon objCommon, Params.ReticleDispatchAndComponentJobStatusChangeReqParams params) {
        String tmpArhs = StandardProperties.OM_ARHS_FLAG.getValue();
        if (log.isDebugEnabled())
            log.debug("ARHS switch on / off  tmpArhs = {}", tmpArhs);
        Validations.check(!CimStringUtils.equals(BizConstant.SP_ARMS_SWITCH_ON, tmpArhs), retCodeConfig.getFunctionNotAvailable());

        String rdjID = params.getRdjID();
        String rcjID = params.getRcjID();
        String jobName = params.getJobName();
        ObjectIdentifier reticleID = params.getReticleID();
        ObjectIdentifier reticlePodID = params.getReticlePodID();
        ObjectIdentifier fromMachineID = params.getFromMachineID();
        ObjectIdentifier fromPortID = params.getFromPortID();
        ObjectIdentifier toMachineID = params.getToMachineID();
        ObjectIdentifier toPortID = params.getToPortID();

        StringBuilder msgTest = new StringBuilder("");
        Boolean bMessageRequiredFlag = false;
        if (CimBooleanUtils.isTrue(params.getIsRequest())) {
            log.debug("isRequest = TRUE");
            if (CimStringUtils.isEmpty(rdjID) || CimStringUtils.isEmpty(rcjID)) {
                log.error("in-pata RDJ_ID/RCJ_ID is null.");
                Validations.check(true, retCodeConfig.getInvalidParameter());
            }
            /*-------------------*/
            /*   Check RDJ/RCJ   */
            /*-------------------*/
            log.debug("step1 - reticleMethod.reticleDispatchJobListGetDR");
            List<Infos.ReticleDispatchJob> reticleDispatchJobList = reticleMethod.reticleDispatchJobListGetDR(objCommon, rdjID);
            if (CimArrayUtils.getSize(reticleDispatchJobList) > 1) {
                log.error("This RDJ_ID is already defined.");
                Validations.check(true, retCodeConfigEx.getRdjDuplicate());
            }

            log.debug("step2 - reticleMethod.reticleComponentJobListGetDR");
            List<Infos.ReticleComponentJob> reticleComponentJobList = reticleMethod.reticleComponentJobListGetDR(objCommon, rdjID);
            Validations.check(CimArrayUtils.isEmpty(reticleDispatchJobList) && CimStringUtils.isNotEmpty(rdjID), retCodeConfigEx.getRdjNotFound());
            Validations.check(CimArrayUtils.isEmpty(reticleComponentJobList) && CimStringUtils.isNotEmpty(rcjID), retCodeConfigEx.getRcjNotFound());
            if (CimArrayUtils.isEmpty(reticleDispatchJobList) && CimArrayUtils.isEmpty(reticleComponentJobList)) {
                log.debug("Not found RDJ/RCJ. not update.");
                return;
            }
            if (CimArrayUtils.isEmpty(reticleDispatchJobList) && CimArrayUtils.isNotEmpty(reticleComponentJobList)) {
                log.error("RDJ/RCJ mismatch.");
                Validations.check(true, retCodeConfigEx.getMismatchRdjrcjStatus());
            }

            /*-------------------------*/
            /*   Update RDJ/RCJ data   */
            /*-------------------------*/
            log.debug("step3 - reticleMethod.reticleJobStatusUpdateByRequestDR");
            reticleMethod.reticleJobStatusUpdateByRequestDR(objCommon,
                    rdjID,
                    rcjID,
                    false);

            msgTest.append("Error Type          : ");
            if (CimStringUtils.equals(jobName, BizConstant.SP_RCJ_JOBNAME_STORE)) {
                msgTest.append(BizConstant.SP_RCJ_ERROR_TYPE_STOREREQ).append("\n");
            }
            if (CimStringUtils.equals(jobName, BizConstant.SP_RCJ_JOBNAME_RETRIEVE)) {
                msgTest.append(BizConstant.SP_RCJ_ERROR_TYPE_RETRIEVEREQ).append("\n");
            }
            if (CimStringUtils.equals(jobName, BizConstant.SP_RCJ_JOBNAME_UNCLAMP)) {
                msgTest.append(BizConstant.SP_RCJ_ERROR_TYPE_UNCLAMPREQ).append("\n");
            }
            if (CimStringUtils.equals(jobName, BizConstant.SP_RCJ_JOBNAME_XFER)) {
                msgTest.append(BizConstant.SP_RCJ_ERROR_TYPE_XFERREQ).append("\n");
            }
            msgTest.append("From Machine ID     : ").append(ObjectIdentifier.fetchValue(fromMachineID)).append("\n");
            msgTest.append("From Machine Port   : ").append(ObjectIdentifier.fetchValue(toMachineID)).append("\n");
            msgTest.append("To Machine ID       : ").append(ObjectIdentifier.fetchValue(toMachineID)).append("\n");
            msgTest.append("To Machine Port     : ").append(ObjectIdentifier.fetchValue(toPortID)).append("\n");
            msgTest.append("Reticle Pod ID      : ").append(ObjectIdentifier.fetchValue(reticlePodID)).append("\n");
            msgTest.append("Reticle ID          : ").append(ObjectIdentifier.fetchValue(reticleID)).append("\n");
            msgTest.append("------------------------------------------------------------").append("\n");
            msgTest.append("Transaction ID      : ").append(objCommon.getTransactionID()).append("\n");
            msgTest.append("Return Code         : ").append(params.getStxResult().getMessageID()).append("\n");
            msgTest.append("Message ID          : ").append(params.getStxResult().getMessageID()).append("\n");
            msgTest.append("Message Text        : ").append(params.getStxResult().getMessageText()).append("\n");
            msgTest.append("Reason Text         : ").append(params.getStxResult().getReasonText()).append("\n");

            bMessageRequiredFlag = true;
        } else {
            log.debug("isRequest = FALSE");
            if (CimStringUtils.isEmpty(jobName)
                    || (ObjectIdentifier.isEmptyWithValue(reticleID) && ObjectIdentifier.isEmptyWithValue(reticlePodID))) {
                log.error("In-para is no valid. jobName or reticleID or reticlePodID is null.");
                Validations.check(true, retCodeConfig.getInvalidParameter());
            }
            log.debug("step4 - reticleMethod.reticleComponentJobGetByJobNameDR");
            Outputs.ReticleComponentJobGetByJobNameDROut reticleComponentJobGetByJobNameDROut = reticleMethod.reticleComponentJobGetByJobNameDR(objCommon,
                    jobName,
                    reticleID,
                    reticlePodID);

            if (null != reticleComponentJobGetByJobNameDROut && CimStringUtils.isNotEmpty(reticleComponentJobGetByJobNameDROut.getStrReticleComponentJob().getReticleComponentJobID())) {
                String reticleComponentJobID = reticleComponentJobGetByJobNameDROut.getStrReticleComponentJob().getReticleComponentJobID();
                if (log.isDebugEnabled())
                    log.debug("reticleComponentJobID {}", reticleComponentJobID);
                log.debug("step5 - reticleMethod.reticleJobStatusUpdateByReportDR");
                reticleMethod.reticleJobStatusUpdateByReportDR(objCommon, reticleComponentJobID, false);

                msgTest.append("Error Type          : ");
                if (CimStringUtils.equals(jobName, BizConstant.SP_RCJ_JOBNAME_STORE)) {
                    msgTest.append(BizConstant.SP_RCJ_ERROR_TYPE_STORERPT).append("\n");
                }
                if (CimStringUtils.equals(jobName, BizConstant.SP_RCJ_JOBNAME_RETRIEVE)) {
                    msgTest.append(BizConstant.SP_RCJ_ERROR_TYPE_RETRIEVERPT).append("\n");
                }
                if (CimStringUtils.equals(jobName, BizConstant.SP_RCJ_JOBNAME_UNCLAMP)) {
                    msgTest.append(BizConstant.SP_RCJ_ERROR_TYPE_UNCLAMPRPT).append("\n");
                }
                if (CimStringUtils.equals(jobName, BizConstant.SP_RCJ_JOBNAME_XFER)) {
                    msgTest.append(BizConstant.SP_RCJ_ERROR_TYPE_XFERRPT).append("\n");
                }
                msgTest.append("From Machine ID     : ").append(ObjectIdentifier.fetchValue(fromMachineID)).append("\n");
                msgTest.append("From Machine Port   : ").append(ObjectIdentifier.fetchValue(toMachineID)).append("\n");
                msgTest.append("To Machine ID       : ").append(ObjectIdentifier.fetchValue(toMachineID)).append("\n");
                msgTest.append("To Machine Port     : ").append(ObjectIdentifier.fetchValue(toPortID)).append("\n");
                msgTest.append("Reticle Pod ID      : ").append(ObjectIdentifier.fetchValue(reticlePodID)).append("\n");
                msgTest.append("Reticle ID          : ").append(ObjectIdentifier.fetchValue(reticleID)).append("\n");
                msgTest.append("------------------------------------------------------------").append("\n");
                msgTest.append("Transaction ID      : ").append(objCommon.getTransactionID()).append("\n");
                msgTest.append("Return Code         : ").append(params.getStxResult().getMessageID()).append("\n");
                msgTest.append("Message ID          : ").append(params.getStxResult().getMessageID()).append("\n");
                msgTest.append("Message Text        : ").append(params.getStxResult().getMessageText()).append("\n");
                msgTest.append("Reason Text         : ").append(params.getStxResult().getReasonText()).append("\n");

                bMessageRequiredFlag = true;
            }
        }
        if (CimBooleanUtils.isTrue(bMessageRequiredFlag)) {
            log.debug("bMessageRequiredFlag = TRUE");
            Params.AlertMessageRptParams alertMessageRptParams = new Params.AlertMessageRptParams();
            alertMessageRptParams.setSubSystemID(BizConstant.SP_SUBSYSTEMID_MM);
            alertMessageRptParams.setSystemMessageCode(BizConstant.SP_SYSTEMMSGCODE_ARMSERROR);
            alertMessageRptParams.setSystemMessageText(msgTest.toString());
            alertMessageRptParams.setNotifyFlag(true);
            alertMessageRptParams.setEquipmentID(null);
            alertMessageRptParams.setEquipmentStatus("");
            alertMessageRptParams.setStockerID(null);
            alertMessageRptParams.setStockerStatus("");
            alertMessageRptParams.setAGVID(null);
            alertMessageRptParams.setAGVStatus("");
            alertMessageRptParams.setLotID(null);
            alertMessageRptParams.setLotStatus("");
            alertMessageRptParams.setRouteID(null);
            alertMessageRptParams.setOperationID(null);
            alertMessageRptParams.setOperationNumber("");
            alertMessageRptParams.setSystemMessageTimeStamp(CimDateUtils.convertToSpecString(objCommon.getTimeStamp().getReportTimeStamp()));
            log.debug("step6 - systemService.sxAlertMessageRpt");
            systemService.sxAlertMessageRpt(objCommon, alertMessageRptParams);
        }
    }

    @Override
    public void sxReticleXferJobCreateReq(Infos.ObjCommon objCommon, Params.ReticleXferJobCreateReqParams params) {
        ObjectIdentifier reticleID = params.getReticleID();
        ObjectIdentifier reticlePodID = params.getReticlePodID();
        ObjectIdentifier toMachineID = params.getToMachineID();

        String tmpArhs = StandardProperties.OM_ARHS_FLAG.getValue();
        log.info("ARHS switch on / off  tmpArhs = {}", tmpArhs);
        Validations.check(!CimStringUtils.equals(BizConstant.SP_ARMS_SWITCH_ON, tmpArhs), retCodeConfig.getFunctionNotAvailable());

        log.info("in-para  reticleID: {}", ObjectIdentifier.fetchValue(reticleID));
        log.info("in-para  reticlePodID: {}", ObjectIdentifier.fetchValue(reticlePodID));
        log.info("in-para  toMachineID: {}", ObjectIdentifier.fetchValue(toMachineID));

        //Check input parameter
        Validations.check(ObjectIdentifier.isEmpty(toMachineID) ||
                (ObjectIdentifier.isEmpty(reticleID) && ObjectIdentifier.isEmpty(reticlePodID)), retCodeConfig.getInvalidParameter());

        //Check RDJ
        reticleMethod.reticleDispatchJobCheckExistenceDR(objCommon, reticleID, reticlePodID, null);

        //Create RDJ ID
        String reticleDispatchJobID = CimDateUtils.getCurrentTimeStamp().toString();
        ObjectIdentifier xferReticlePodID;

        ObjectIdentifier durableControlJobID = null;

        if (ObjectIdentifier.isEmpty(reticlePodID)) {
            log.info("ReticlePodID not specified. now search reticlePod.");
            Outputs.ReticleReticlePodGetForXferOut reticleReticlePodGetForXferOut = reticleMethod.reticleReticlePodGetForXfer(objCommon, reticleID);
            Validations.check(CimArrayUtils.getSize(reticleReticlePodGetForXferOut.getStrCandidateReticlePods()) == 0, retCodeConfigEx.getNotFoundSuitableRsp());
            xferReticlePodID = reticleReticlePodGetForXferOut.getStrCandidateReticlePods().get(0).getReticlePodID();

            durableControlJobID = durableMethod.durableDurableControlJobIDGet(objCommon, reticleID, BizConstant.SP_DURABLECAT_RETICLE);
        } else {
            log.info("ReticlePodID specified. {}", reticlePodID);
            xferReticlePodID = reticlePodID;
            durableControlJobID = durableMethod.durableDurableControlJobIDGet(objCommon, xferReticlePodID, BizConstant.SP_DURABLECAT_RETICLEPOD);
        }

        if (!ObjectIdentifier.isEmpty(durableControlJobID)) {
            log.info("durableControlJobID {} is not blank", ObjectIdentifier.fetchValue(durableControlJobID));
            lockMethod.objectLock(objCommon, CimDurableControlJob.class, durableControlJobID);

            Infos.DurableControlJobStatusGet durableControlJobStatusGet = durableMethod.durableControlJobStatusGet(objCommon, durableControlJobID);

            Validations.check(!CimStringUtils.equals(durableControlJobStatusGet.getDurableControlJobStatus(), BizConstant.SP_DURABLECONTROLJOBSTATUS_CREATED) &&
                    !CimStringUtils.equals(durableControlJobStatusGet.getDurableControlJobStatus(), BizConstant.SP_DURABLECONTROLJOBSTATUS_DELETE), retCodeConfig.getInvalidDcjstatus(), durableControlJobStatusGet.getDurableControlJobStatus());
        }
        lockMethod.objectLock(objCommon, CimReticlePod.class, xferReticlePodID);

        //Create RCJ
        Outputs.ReticleComponentJobCreateOut reticleComponentJobCreateOut = null;
        try {
            reticleComponentJobCreateOut = reticleMethod.reticleComponentJobCreate(objCommon,
                    reticleDispatchJobID,
                    objCommon.getUser().getUserID(),
                    1L,
                    reticleID,
                    xferReticlePodID,
                    toMachineID);
        } catch (ServiceException ex) {
            if (Validations.isEquals(retCodeConfigEx.getInvalidRdjRecord(), ex.getCode())) {
                log.info("reticle_componentJob_Create() == RC_INVALID_RDJ_RECORD. now convert.");
                Validations.check(retCodeConfig.getInvalidParameter());
            } else {
                throw ex;
            }
        }

        if (reticleComponentJobCreateOut != null) {
            List<Infos.ReticleDispatchJob> strInsertReticleDispatchJobList = new ArrayList<>();
            strInsertReticleDispatchJobList.add(reticleComponentJobCreateOut.getStrReticleDispatchJob());
            reticleMethod.reticleDispatchJobInsertDR(objCommon, strInsertReticleDispatchJobList);

            reticleMethod.reticleComponentJobInsertDR(objCommon, reticleComponentJobCreateOut.getStrReticleComponentJobList());

            reticleMethod.reticleReticlePodReserve(objCommon,
                    reticleComponentJobCreateOut.getStrReticleDispatchJob().getReticleID(),
                    reticleComponentJobCreateOut.getStrReticleDispatchJob().getReticlePodID(),
                    reticleComponentJobCreateOut.getStrReticleDispatchJob().getToEquipmentID());

            reticleMethod.reticleEventQueuePutDR(objCommon, reticleComponentJobCreateOut.getStrReticleEventRecord());
        }
    }

    @Override
    public void sxReticlePodXferJobCreateReq(Infos.ObjCommon objCommon, Params.ReticlePodXferJobCreateReqParams params) {
        ObjectIdentifier reticlePodID = params.getReticlePodID();
        ObjectIdentifier fromMachineID = params.getFromMachineID();
        ObjectIdentifier fromPortID = params.getFromPortID();
        ObjectIdentifier toMachineID = params.getToMachineID();
        ObjectIdentifier toPortID = params.getToPortID();

        String tmpArhs = StandardProperties.OM_ARHS_FLAG.getValue();
        if (log.isDebugEnabled()){
            log.debug("ARHS switch on / off  tmpArhs = {}", tmpArhs);
        }
        Validations.check(!CimStringUtils.equals(BizConstant.SP_ARMS_SWITCH_ON, tmpArhs), retCodeConfig.getFunctionNotAvailable());

        /*----------------------------------------------*/
        /*   Confirm whether reticle is in reticlePod   */
        /*----------------------------------------------*/
        Params.ReticlePodDetailInfoInqParams reticlePodDetailInfoInqParams = new Params.ReticlePodDetailInfoInqParams();
        reticlePodDetailInfoInqParams.setReticlePodID(reticlePodID);
        reticlePodDetailInfoInqParams.setDurableWipOperationInfoFlag(false);
        reticlePodDetailInfoInqParams.setDurableOperationInfoFlag(false);
        log.debug("step1 - reticleMethod.reticlePodFillInTxPDQ013DR");
        Outputs.ObjReticlePodFillInTxPDQ013DROut reticlePodFillInTxPDQ013DROut = reticleMethod.reticlePodFillInTxPDQ013DR(objCommon, reticlePodDetailInfoInqParams);

        Boolean bReticleExistFlag = false;
        if (null != reticlePodFillInTxPDQ013DROut && CimArrayUtils.isNotEmpty(reticlePodFillInTxPDQ013DROut.getReticlePodStatusInfo().getStrContainedReticleInfo())) {
            if (CimArrayUtils.isNotEmpty(reticlePodFillInTxPDQ013DROut.getReticlePodStatusInfo().getStrContainedReticleInfo())) {
                log.debug("This reticlePod have reticle(s).");
                bReticleExistFlag = true;
            }
        }
        if (CimBooleanUtils.isTrue(bReticleExistFlag)) {
            log.debug("bReticleExistFlag = TRUE");
            for (Infos.ContainedReticleInfo containedReticleInfo : reticlePodFillInTxPDQ013DROut.getReticlePodStatusInfo().getStrContainedReticleInfo()) {
                if (log.isDebugEnabled()){
                    log.debug("durableID {}", ObjectIdentifier.fetchValue(containedReticleInfo.getReticleID()));
                }
                log.debug("step2 - durableMethod.durableDurableControlJobIDGet");
                ObjectIdentifier durableControlJobID = durableMethod.durableDurableControlJobIDGet(objCommon,
                        containedReticleInfo.getReticleID(),
                        BizConstant.SP_DURABLECAT_RETICLE);
                if (ObjectIdentifier.isNotEmptyWithValue(durableControlJobID)) {
                    if (log.isDebugEnabled()){
                        log.debug("durableControlJobID {} is not blank", ObjectIdentifier.fetchValue(durableControlJobID));
                    }
                    log.debug("step3 - objectLockMethod.objectLock");
                    objectLockMethod.objectLock(objCommon, CimDurableControlJob.class, durableControlJobID);

                    log.debug("step4 - durableMethod.durableControlJobStatusGet");
                    Infos.DurableControlJobStatusGet durableControlJobStatus = durableMethod.durableControlJobStatusGet(objCommon, durableControlJobID);
                    if (!CimStringUtils.equals(durableControlJobStatus.getDurableControlJobStatus(), BizConstant.SP_DURABLECONTROLJOBSTATUS_CREATED)
                            && !CimStringUtils.equals(durableControlJobStatus.getDurableControlJobStatus(), BizConstant.SP_DURABLECONTROLJOBSTATUS_DELETE)) {
                        Validations.check(true, retCodeConfig.getInvalidDcjstatus(),
                                durableControlJobStatus.getDurableControlJobStatus());
                    }
                }
            }
        } else {
            log.debug("bReticleExistFlag = FALSE");
            log.debug("step5 - durableMethod.durableDurableControlJobIDGet");
            ObjectIdentifier durableControlJobID = durableMethod.durableDurableControlJobIDGet(objCommon,
                    reticlePodID,
                    BizConstant.SP_DURABLECAT_RETICLEPOD);

            if (ObjectIdentifier.isNotEmptyWithValue(durableControlJobID)) {
                if (log.isDebugEnabled()){
                    log.debug("durableControlJobID {} is not blank", ObjectIdentifier.fetchValue(durableControlJobID));
                }
                log.debug("step6 - objectLockMethod.objectLock");
                objectLockMethod.objectLock(objCommon, CimDurableControlJob.class, durableControlJobID);

                log.debug("step7 - durableMethod.durableControlJobStatusGet");
                Infos.DurableControlJobStatusGet durableControlJobStatus = durableMethod.durableControlJobStatusGet(objCommon, durableControlJobID);
                if (!CimStringUtils.equals(durableControlJobStatus.getDurableControlJobStatus(), BizConstant.SP_DURABLECONTROLJOBSTATUS_CREATED)
                        && !CimStringUtils.equals(durableControlJobStatus.getDurableControlJobStatus(), BizConstant.SP_DURABLECONTROLJOBSTATUS_DELETE)) {
                    Validations.check(true, retCodeConfig.getInvalidDcjstatus(),
                            durableControlJobStatus.getDurableControlJobStatus());
                }
            }
        }
        List<Infos.MoveReticles> strMoveReticles = new ArrayList<>();
        if (CimBooleanUtils.isTrue(bReticleExistFlag)) {
            log.debug("TRUE == bReticleExistFlag");
            for (Infos.ContainedReticleInfo containedReticleInfo : reticlePodFillInTxPDQ013DROut.getReticlePodStatusInfo().getStrContainedReticleInfo()) {
                Infos.MoveReticles moveReticles = new Infos.MoveReticles();
                strMoveReticles.add(moveReticles);
                moveReticles.setReticleID(containedReticleInfo.getReticleID());
                moveReticles.setSlotNumber((int) containedReticleInfo.getSlotNo());
                /*-------------------------------*/
                /*   Check reticles controlJob   */
                /*-------------------------------*/
                log.debug("step8 - reticleMethod.reticlecontrolJobInfoGet");
                Outputs.ReticleControlJobInfoGetOut reticleControlJobInfoGetOut = reticleMethod.reticlecontrolJobInfoGet(objCommon, moveReticles.getReticleID());
                if (null != reticleControlJobInfoGetOut && CimArrayUtils.isNotEmpty(reticleControlJobInfoGetOut.getStrControlJobAttributeInfoSeq())) {
                    //--------------------------------------------------
                    //   Get OM_RETICLE_RETRIEVE_WHEN_LOT_PROCESS
                    //--------------------------------------------------
                    Long retrieveReticleDuringLotProcFlag = StandardProperties.OM_RETICLE_RETRIEVE_WHEN_LOT_PROCESS.getLongValue();
                    if (log.isDebugEnabled()){
                        log.debug("Env OM_RETICLE_RETRIEVE_WHEN_LOT_PROCESS {}", retrieveReticleDuringLotProcFlag);
                    }
                    if (1L == retrieveReticleDuringLotProcFlag) {
                        log.debug("Check if reticle has reserve control job");
                        for (Outputs.ControlJobAttributeInfo controlJobAttributeInfo : reticleControlJobInfoGetOut.getStrControlJobAttributeInfoSeq()) {
                            if (CimStringUtils.equals(BizConstant.SP_CONTROLJOBSTATUS_CREATED, controlJobAttributeInfo.getControlJobStatus())) {
                                log.debug("controlJob status is Created");
                                Validations.check(true, retCodeConfigEx.getRtclHasCtrljob(),
                                        ObjectIdentifier.fetchValue(moveReticles.getReticleID()));
                            }
                        }
                    } else {
                        if (log.isDebugEnabled()){
                            log.debug("This reticle {} have controlJob.", ObjectIdentifier.fetchValue(moveReticles.getReticleID()));
                        }
                        Validations.check(true, retCodeConfigEx.getRtclHasCtrljob(),
                                ObjectIdentifier.fetchValue(moveReticles.getReticleID()));
                    }
                }
            }
            for (int i = 0; i < CimArrayUtils.getSize(reticlePodFillInTxPDQ013DROut.getReticlePodStatusInfo().getStrContainedReticleInfo()); i++) {
                /*-------------------*/
                /*   Check RDJ/RCJ   */
                /*-------------------*/
                log.debug("step9 - reticleMethod.reticleDispatchJobCheckExistenceDR");
                List<Infos.ReticleDispatchJob> reticleDispatchJobList = reticleMethod.reticleDispatchJobCheckExistenceDR(objCommon,
                        strMoveReticles.get(i).getReticleID(),
                        reticlePodID,
                        null);

                List<Infos.ReticleComponentJob> reticleComponentJobList = null;
                log.debug("step10 - reticleMethod.reticleComponentJobCheckExistenceDR");
                try {
                    reticleComponentJobList = reticleMethod.reticleComponentJobCheckExistenceDR(objCommon,
                            strMoveReticles.get(i).getReticleID(),
                            reticlePodID,
                            fromMachineID,
                            fromPortID,
                            toMachineID,
                            toPortID);
                } catch (ServiceException e) {
                    if (Validations.isEquals(retCodeConfigEx.getFoundInRcj(), e.getCode())) {
                        reticleComponentJobList = e.getData(List.class);
                        if (CimArrayUtils.isNotEmpty(reticleComponentJobList)) {
                            for (Infos.ReticleComponentJob reticleComponentJob : reticleComponentJobList) {
                                if (!CimStringUtils.equals(reticleComponentJob.getJobName(), BizConstant.SP_RCJ_JOBNAME_XFER)
                                        || CimStringUtils.equals(reticleComponentJob.getJobStatus(), BizConstant.SP_RCJ_STATUS_WAITTOEXECUTE)
                                        || ((!ObjectIdentifier.equalsWithValue(reticleComponentJob.getFromEquipmentID(), toMachineID)
                                        || !ObjectIdentifier.equalsWithValue(reticleComponentJob.getFromReticlePodPortID(), toPortID)
                                ) && (!ObjectIdentifier.equalsWithValue(reticleComponentJob.getFromEquipmentID(), fromMachineID)
                                        || !ObjectIdentifier.equalsWithValue(reticleComponentJob.getFromReticlePodPortID(), fromPortID)))) {
                                    throw e;
                                }
                            }
                        }
                    } else {
                        throw e;
                    }
                }
            }
        } else {
            log.debug("TRUE != bReticleExistFlag");
            Infos.MoveReticles moveReticles = new Infos.MoveReticles();
            moveReticles.setSlotNumber(0);
            strMoveReticles.add(moveReticles);
            /*-------------------*/
            /*   Check RDJ/RCJ   */
            /*-------------------*/
            log.debug("step11 - reticleMethod.reticleDispatchJobCheckExistenceDR");
            List<Infos.ReticleDispatchJob> reticleDispatchJobList = reticleMethod.reticleDispatchJobCheckExistenceDR(objCommon,
                    null,
                    reticlePodID,
                    null);

            List<Infos.ReticleComponentJob> reticleComponentJobList = null;
            log.debug("step12 - reticleMethod.reticleComponentJobCheckExistenceDR");
            try {
                reticleComponentJobList = reticleMethod.reticleComponentJobCheckExistenceDR(objCommon,
                        null,
                        reticlePodID,
                        fromMachineID,
                        fromPortID,
                        toMachineID,
                        toPortID);
            } catch (ServiceException e) {
                if (Validations.isEquals(retCodeConfigEx.getFoundInRcj(), e.getCode())) {
                    reticleComponentJobList = e.getData(List.class);
                    if (CimArrayUtils.isNotEmpty(reticleComponentJobList)) {
                        for (Infos.ReticleComponentJob reticleComponentJob : reticleComponentJobList) {
                            if (!CimStringUtils.equals(reticleComponentJob.getJobName(), BizConstant.SP_RCJ_JOBNAME_XFER)
                                    || CimStringUtils.equals(reticleComponentJob.getJobStatus(), BizConstant.SP_RCJ_STATUS_WAITTOEXECUTE)
                                    || ((!ObjectIdentifier.equalsWithValue(reticleComponentJob.getFromEquipmentID(), toMachineID)
                                    || !ObjectIdentifier.equalsWithValue(reticleComponentJob.getFromReticlePodPortID(), toPortID)
                            ) && (!ObjectIdentifier.equalsWithValue(reticleComponentJob.getFromEquipmentID(), fromMachineID)
                                    || !ObjectIdentifier.equalsWithValue(reticleComponentJob.getFromReticlePodPortID(), fromPortID)))) {
                                throw e;
                            }
                        }
                    }
                } else {
                    throw e;
                }
            }
        }
        /*----------------------------------*/
        /*   Check from Machine condition   */
        /*----------------------------------*/
        String fromMachineCategory = null;
        Long fromMachineType = 0L;
        log.debug("step13 - equipmentMethod.machineTypeGet");
        Outputs.ObjMachineTypeGetOut fromMachienTypeGetOut = equipmentMethod.machineTypeGet(objCommon, fromMachineID);
        if (CimBooleanUtils.isFalse(fromMachienTypeGetOut.isBStorageMachineFlag())) {
            log.debug("fromMachine type is Equipment");
            fromMachineType = 1L;
            fromMachineCategory = BizConstant.SP_MACHINE_TYPE_EQP;
        } else {
            if (CimStringUtils.equals(fromMachienTypeGetOut.getStockerType(), BizConstant.SP_STOCKER_TYPE_BARERETICLE)) {
                log.debug("fromMachine type is BareReticleStocker");
                fromMachineType = 2L;
                fromMachineCategory = BizConstant.SP_STOCKER_TYPE_BARERETICLE;
            } else if (CimStringUtils.equals(fromMachienTypeGetOut.getStockerType(), BizConstant.SP_STOCKER_TYPE_RETICLEPOD)) {
                log.debug("fromMachine type is ReticlePodStocker");
                fromMachineType = 3L;
                fromMachineCategory = BizConstant.SP_STOCKER_TYPE_RETICLEPOD;
            } else {
                log.debug("machine type is invalid.");
                Validations.check(true, retCodeConfigEx.getRtclPodDestInvalid(),
                        ObjectIdentifier.fetchValue(reticlePodID),
                        ObjectIdentifier.fetchValue(fromMachineID));
            }
        }
        Infos.ReticlePodPortInfo fromReticlePodPortInfo = null;
        String fromMachineOnlineMode = BizConstant.SP_EQP_ONLINEMODE_ONLINEREMOTE;
        if (fromMachineType == 1L) {
            log.debug("step14 - equipmentMethod.equipmentReticlePodPortInfoGetDR");
            Outputs.ObjEquipmentReticlePodPortInfoGetDROut equipmentReticlePodPortInfoGetDROut = equipmentMethod.equipmentReticlePodPortInfoGetDR(objCommon, fromMachineID);
            Boolean bPodOnPortFlag = false;
            if (null != equipmentReticlePodPortInfoGetDROut && CimArrayUtils.isNotEmpty(equipmentReticlePodPortInfoGetDROut.getReticlePodPortInfoList())) {
                for (Infos.ReticlePodPortInfo reticlePodPortInfo : equipmentReticlePodPortInfoGetDROut.getReticlePodPortInfoList()) {
                    if (ObjectIdentifier.equalsWithValue(reticlePodPortInfo.getReticlePodPortID(), fromPortID)
                            && ObjectIdentifier.equalsWithValue(reticlePodPortInfo.getLoadedReticlePodID(), reticlePodID)) {
                        log.debug("Found requested reticlePod on Port.");
                        fromReticlePodPortInfo = reticlePodPortInfo;
                        bPodOnPortFlag = true;
                        break;
                    }
                }
            }
            if (CimBooleanUtils.isFalse(bPodOnPortFlag)) {
                log.debug("Not found reticlePod on fromPort");
                Validations.check(true, retCodeConfigEx.getRtclpodNotLoaded(),
                        ObjectIdentifier.fetchValue(reticlePodID),
                        ObjectIdentifier.fetchValue(fromMachineID),
                        ObjectIdentifier.fetchValue(fromPortID));
            }
            log.debug("step15 - equipmentMethod.equipmentOnlineModeGet");
            String equipmentOnlineModeGet = equipmentMethod.equipmentOnlineModeGet(objCommon, fromMachineID);
            fromMachineOnlineMode = equipmentOnlineModeGet;
            if (log.isDebugEnabled()){
                log.debug("From Equipment's online-mode is: {}", fromMachineOnlineMode);
            }
        }
        if (fromMachineType == 2L) {
            log.debug("step16 - stockerMethod.stockerReticlePodPortInfoGetDR");
            List<Infos.ReticlePodPortInfo> reticlePodPortInfoList = stockerMethod.stockerReticlePodPortInfoGetDR(objCommon, fromMachineID);
            Boolean bPodOnPortFlag = false;
            if (CimArrayUtils.isNotEmpty(reticlePodPortInfoList)) {
                for (Infos.ReticlePodPortInfo reticlePodPortInfo : reticlePodPortInfoList) {
                    if (ObjectIdentifier.equalsWithValue(reticlePodPortInfo.getReticlePodPortID(), fromPortID)
                            && ObjectIdentifier.equalsWithValue(reticlePodPortInfo.getLoadedReticlePodID(), reticlePodID)) {
                        log.debug("Found requested reticlePod on Port.");
                        fromReticlePodPortInfo = reticlePodPortInfo;
                        bPodOnPortFlag = true;
                        break;
                    }
                }
            }
            if (CimBooleanUtils.isFalse(bPodOnPortFlag)) {
                log.debug("Not found reticlePod on fromPort");
                Validations.check(true, retCodeConfigEx.getRtclpodNotLoaded(),
                        ObjectIdentifier.fetchValue(reticlePodID),
                        ObjectIdentifier.fetchValue(fromMachineID),
                        ObjectIdentifier.fetchValue(fromPortID));
            }
            log.debug("step17 - stockerMethod.stockerOnlineModeGet");
            String stockerOnlineModeGet = stockerMethod.stockerOnlineModeGet(objCommon, fromMachineID);
            fromMachineOnlineMode = stockerOnlineModeGet;
            if (log.isDebugEnabled()){
                log.debug("From BRStocker's online-mode is: {}", fromMachineOnlineMode);
            }
        }
        if (fromMachineType == 3L) {
            if (!ObjectIdentifier.equalsWithValue(reticlePodFillInTxPDQ013DROut.getReticlePodStatusInfo().getStockerID(), fromMachineID) ||
                    !CimStringUtils.equals(reticlePodFillInTxPDQ013DROut.getReticlePodStatusInfo().getTransferStatus(), BizConstant.SP_TRANSSTATE_STATIONIN) &&
                            !CimStringUtils.equals(reticlePodFillInTxPDQ013DROut.getReticlePodStatusInfo().getTransferStatus(), BizConstant.SP_TRANSSTATE_MANUALIN) &&
                            !CimStringUtils.equals(reticlePodFillInTxPDQ013DROut.getReticlePodStatusInfo().getTransferStatus(), BizConstant.SP_TRANSSTATE_BAYIN) &&
                            !CimStringUtils.equals(reticlePodFillInTxPDQ013DROut.getReticlePodStatusInfo().getTransferStatus(), BizConstant.SP_TRANSSTATE_INTERMEDIATEIN) &&
                            !CimStringUtils.equals(reticlePodFillInTxPDQ013DROut.getReticlePodStatusInfo().getTransferStatus(), BizConstant.SP_TRANSSTATE_ABNORMALIN)) {
                log.debug("Not found reticlePod in RSP Stocker");
                Validations.check(true, retCodeConfigEx.getRtclpodNotLoaded(),
                        ObjectIdentifier.fetchValue(reticlePodID),
                        ObjectIdentifier.fetchValue(fromMachineID),
                        ObjectIdentifier.fetchValue(fromPortID));
            }
        }
        if (fromMachineType == 1L || fromMachineType == 2L) {
            log.debug("fromMachineType is 1 or 2.");
            if (!CimStringUtils.equals(fromReticlePodPortInfo.getAccessMode(), BizConstant.SP_EQP_ACCESSMODE_AUTO)) {
                log.error("fromPort accessMode {} is not Auto.", fromReticlePodPortInfo.getAccessMode());
                Validations.check(retCodeConfigEx.getEqpPortAccessMode(),
                        ObjectIdentifier.fetchValue(fromMachineID),
                        ObjectIdentifier.fetchValue(fromPortID),
                        fromReticlePodPortInfo.getAccessMode());
            }
            if (!CimStringUtils.equals(fromMachineOnlineMode, BizConstant.SP_EQP_ONLINEMODE_OFFLINE)
                    && !CimStringUtils.equals(fromReticlePodPortInfo.getPortStatus(), BizConstant.SP_PORTRSC_PORTSTATE_UNLOADREQ)) {
                log.error("fromPort is not UnloadReq. portState: {}", fromReticlePodPortInfo.getPortStatus());
                Validations.check(true, retCodeConfigEx.getInvalidRspPortStatus(),
                        ObjectIdentifier.fetchValue(fromMachineID),
                        ObjectIdentifier.fetchValue(fromPortID));
            }
            if (!CimStringUtils.equals(fromReticlePodPortInfo.getDispatchStatus(), BizConstant.SP_PORTRSC_DISPATCHSTATE_REQUIRED)) {
                log.error("fromPort dispatchState is {}", fromReticlePodPortInfo.getDispatchStatus());
                Validations.check(true, retCodeConfigEx.getInvalidMachinePortDispatchState(),
                        ObjectIdentifier.fetchValue(fromMachineID),
                        ObjectIdentifier.fetchValue(fromPortID),
                        fromReticlePodPortInfo.getDispatchStatus());
            }
            if (!CimStringUtils.equals(fromReticlePodPortInfo.getTransferReserveStatus(), BizConstant.SP_RETICLEPODPORT_NOTRESERVED)) {
                log.error("fromPort is reserved.");
                Validations.check(true, retCodeConfigEx.getInvalidMachinePortTransRsrvState(),
                        ObjectIdentifier.fetchValue(fromMachineID),
                        ObjectIdentifier.fetchValue(fromPortID),
                        fromReticlePodPortInfo.getTransferReserveStatus());
            }
        }
        if (fromMachineType == 2L || fromMachineType == 3L) {
            log.debug("fromMachineType is 2 or 3.");
            log.debug("step18 - equipmentMethod.equipmentCheckAvail");
            equipmentMethod.equipmentCheckAvail(objCommon, fromMachineID);
        }

        /*--------------------------------*/
        /*   Check to Machine condition   */
        /*--------------------------------*/
        String toMachineCategory = null;
        Long toMachineType = 0L;
        log.debug("step19 - equipmentMethod.machineTypeGet");
        Outputs.ObjMachineTypeGetOut toMachineTypeGetOut = equipmentMethod.machineTypeGet(objCommon, toMachineID);
        if (CimBooleanUtils.isFalse(toMachineTypeGetOut.isBStorageMachineFlag())) {
            log.debug("toMachine type is Equipment");
            toMachineType = 1L;
            toMachineCategory = BizConstant.SP_MACHINE_TYPE_EQP;
        } else {
            if (CimStringUtils.equals(toMachineTypeGetOut.getStockerType(), BizConstant.SP_STOCKER_TYPE_BARERETICLE)) {
                log.debug("toMachine type is BareReticleStocker");
                toMachineType = 2L;
                toMachineCategory = BizConstant.SP_STOCKER_TYPE_BARERETICLE;
            } else if (CimStringUtils.equals(toMachineTypeGetOut.getStockerType(), BizConstant.SP_STOCKER_TYPE_RETICLEPOD)) {
                log.debug("toMachine type is ReticlePodStocker");
                toMachineType = 3L;
                toMachineCategory = BizConstant.SP_STOCKER_TYPE_RETICLEPOD;
            } else {
                log.error("machine type is invalid.");
                Validations.check(true, retCodeConfigEx.getRtclPodDestInvalid(),
                        ObjectIdentifier.fetchValue(reticlePodID),
                        ObjectIdentifier.fetchValue(toMachineID));
            }
        }
        Infos.ReticlePodPortInfo toReticlePodPortInfo = null;
        String toMachineOnlineMode = BizConstant.SP_EQP_ONLINEMODE_ONLINEREMOTE;
        if (toMachineType == 1L) {
            log.debug("step20 - equipmentMethod.equipmentReticlePodPortInfoGetDR");
            Outputs.ObjEquipmentReticlePodPortInfoGetDROut equipmentReticlePodPortInfoGetDROut = equipmentMethod.equipmentReticlePodPortInfoGetDR(objCommon, toMachineID);
            Boolean bFoundPortFlag = false;
            if (null != equipmentReticlePodPortInfoGetDROut && CimArrayUtils.isNotEmpty(equipmentReticlePodPortInfoGetDROut.getReticlePodPortInfoList())) {
                for (Infos.ReticlePodPortInfo reticlePodPortInfo : equipmentReticlePodPortInfoGetDROut.getReticlePodPortInfoList()) {
                    if (ObjectIdentifier.equalsWithValue(reticlePodPortInfo.getReticlePodPortID(), toPortID)) {
                        log.debug("Found requested reticlePodPort.");
                        toReticlePodPortInfo = reticlePodPortInfo;
                        bFoundPortFlag = true;
                        break;
                    }
                }
            }
            if (CimBooleanUtils.isFalse(bFoundPortFlag)) {
                log.error("Not found reticlePodPort");
                Validations.check(true, retCodeConfig.getRspportNotFound(),
                        ObjectIdentifier.fetchValue(toMachineID),
                        ObjectIdentifier.fetchValue(toPortID));
            }
            log.debug("step21 - equipmentMethod.equipmentOnlineModeGet");
            String equipmentOnlineModeGetOut = equipmentMethod.equipmentOnlineModeGet(objCommon, toMachineID);
            toMachineOnlineMode = equipmentOnlineModeGetOut;
            if (log.isDebugEnabled()){
                log.debug("To Equipment's online-mode is: {}", toMachineOnlineMode);
            }
        }
        if (toMachineType == 2L) {
            log.debug("step22 - stockerMethod.stockerReticlePodPortInfoGetDR");
            List<Infos.ReticlePodPortInfo> reticlePodPortInfoList = stockerMethod.stockerReticlePodPortInfoGetDR(objCommon, toMachineID);
            Boolean bFoundPortFlag = false;
            if (CimArrayUtils.isNotEmpty(reticlePodPortInfoList)) {
                for (Infos.ReticlePodPortInfo reticlePodPortInfo : reticlePodPortInfoList) {
                    if (ObjectIdentifier.equalsWithValue(reticlePodPortInfo.getReticlePodPortID(), toPortID)) {
                        log.debug("Found requested reticlePodPort.");
                        toReticlePodPortInfo = reticlePodPortInfo;
                        bFoundPortFlag = true;
                        break;
                    }
                }
            }
            if (CimBooleanUtils.isFalse(bFoundPortFlag)) {
                log.error("Not found reticlePodPort");
                Validations.check(retCodeConfig.getRspportNotFound(),
                        ObjectIdentifier.fetchValue(toMachineID),
                        ObjectIdentifier.fetchValue(toPortID));
            }
            log.debug("step23 - stockerMethod.stockerOnlineModeGet");
            String stockerOnlineModeGet = stockerMethod.stockerOnlineModeGet(objCommon, toMachineID);
            toMachineOnlineMode = stockerOnlineModeGet;
            if (log.isDebugEnabled()){
                log.debug("to BRStocker's online-mode is: {}", toMachineOnlineMode);
            }
        }
        if (toMachineType == 1L || toMachineType == 2L) {
            log.debug("toMachineType is 1 or 2.");
            if (!CimStringUtils.equals(toReticlePodPortInfo.getAccessMode(), BizConstant.SP_EQP_ACCESSMODE_AUTO)) {
                log.error("toPort accessMode is {} not Auto", toReticlePodPortInfo.getAccessMode());
                Validations.check(true, retCodeConfigEx.getEqpPortAccessMode(),
                        ObjectIdentifier.fetchValue(toMachineID),
                        ObjectIdentifier.fetchValue(toPortID),
                        toReticlePodPortInfo.getAccessMode());
            }
            if (!CimStringUtils.equals(toMachineOnlineMode, BizConstant.SP_EQP_ONLINEMODE_OFFLINE)
                    && !CimStringUtils.equals(toReticlePodPortInfo.getPortStatus(), BizConstant.SP_PORTRSC_PORTSTATE_LOADREQ)) {
                log.error("toPort is not UnloadReq. portState: {}", toReticlePodPortInfo.getPortStatus());
                Validations.check(true, retCodeConfigEx.getInvalidRspPortStatus(),
                        ObjectIdentifier.fetchValue(toMachineID),
                        ObjectIdentifier.fetchValue(toPortID));
            }
            if (!CimStringUtils.equals(toReticlePodPortInfo.getDispatchStatus(), BizConstant.SP_PORTRSC_DISPATCHSTATE_REQUIRED)) {
                log.error("toPort dispatchState is {}", toReticlePodPortInfo.getDispatchStatus());
                Validations.check(true, retCodeConfigEx.getInvalidMachinePortDispatchState(),
                        ObjectIdentifier.fetchValue(toMachineID),
                        ObjectIdentifier.fetchValue(toPortID),
                        toReticlePodPortInfo.getDispatchStatus());
            }
            if (!CimStringUtils.equals(toReticlePodPortInfo.getTransferReserveStatus(), BizConstant.SP_RETICLEPODPORT_NOTRESERVED)) {
                log.error("toPort is reserved.");
                Validations.check(true, retCodeConfigEx.getInvalidMachinePortTransRsrvState(),
                        ObjectIdentifier.fetchValue(toMachineID),
                        ObjectIdentifier.fetchValue(toPortID),
                        toReticlePodPortInfo.getTransferReserveStatus());
            }
        }
        if (toMachineType == 2L || toMachineType == 3L) {
            log.debug("toMachineType is 2 or 3.");
            log.debug("step24 - equipmentMethod.equipmentCheckAvail");
            equipmentMethod.equipmentCheckAvail(objCommon, toMachineID);
        }
        /*---------------------*/
        /*   Insert to FSRAL   */
        /*---------------------*/
        Infos.ReticleDispatchJob reticleDispatchJob = new Infos.ReticleDispatchJob();
        reticleDispatchJob.setRequestedTimestamp(CimDateUtils.convertToSpecString(objCommon.getTimeStamp().getReportTimeStamp()));

        if (CimBooleanUtils.isTrue(bReticleExistFlag)) {
            log.debug("TRUE == bReticleExistFlag");
            reticleDispatchJob.setReticleID(strMoveReticles.get(0).getReticleID());
        } else {
            log.debug("TRUE != bReticleExistFlag");
            reticleDispatchJob.setReticleID(ObjectIdentifier.buildWithValue(""));
        }
        reticleDispatchJob.setReticlePodID(reticlePodID);

        reticleDispatchJob.setReticleDispatchJobID(reticleMethod.timeStampGetDR());
        reticleDispatchJob.setFromEquipmentID(fromMachineID);
        reticleDispatchJob.setFromEquipmentCategory(fromMachineCategory);
        reticleDispatchJob.setToEquipmentID(toMachineID);
        reticleDispatchJob.setToEquipmentCategory(toMachineCategory);
        reticleDispatchJob.setPriority(1L);
        reticleDispatchJob.setRequestUserID(objCommon.getUser().getUserID());
        reticleDispatchJob.setJobStatus(BizConstant.SP_RDJ_STATUS_WAITTOEXECUTE);
        reticleDispatchJob.setJobStatusChangeTimestamp(CimDateUtils.convertToSpecString(objCommon.getTimeStamp().getReportTimeStamp()));
        log.debug("step25 - reticleMethod.reticleDispatchJobInsertDR");
        reticleMethod.reticleDispatchJobInsertDR(objCommon, Collections.singletonList(reticleDispatchJob));

        /*---------------------*/
        /*   Insert to FSREL   */
        /*---------------------*/
        Infos.ReticleComponentJob reticleComponentJob = new Infos.ReticleComponentJob();
        reticleComponentJob.setRequestedTimestamp(CimDateUtils.convertToSpecString(objCommon.getTimeStamp().getReportTimeStamp()));
        reticleComponentJob.setPriority(1L);
        reticleComponentJob.setReticleDispatchJobID(reticleDispatchJob.getReticleDispatchJobID());

        reticleComponentJob.setReticleComponentJobID(reticleMethod.timeStampGetDR());
        reticleComponentJob.setReticleDispatchJobRequestUserID(reticleDispatchJob.getRequestUserID());
        reticleComponentJob.setJobSeq(1L);
        reticleComponentJob.setReticlePodID(reticlePodID);

        if (CimBooleanUtils.isTrue(bReticleExistFlag)) {
            log.debug("TRUE == bReticleExistFlag");
            reticleComponentJob.setSlotNo(CimNumberUtils.longValue(strMoveReticles.get(0).getSlotNumber()));
            reticleComponentJob.setReticleID(strMoveReticles.get(0).getReticleID());
        } else {
            log.debug("TRUE != bReticleExistFlag");
            reticleComponentJob.setSlotNo(0L);
            reticleComponentJob.setReticleID(ObjectIdentifier.buildWithValue(""));
        }

        reticleComponentJob.setJobName(BizConstant.SP_RCJ_JOBNAME_XFER);
        reticleComponentJob.setToEquipmentID(toMachineID);
        reticleComponentJob.setToReticlePodPortID(toPortID);
        reticleComponentJob.setToEquipmentCategory(toMachineCategory);
        reticleComponentJob.setFromEquipmentID(fromMachineID);
        reticleComponentJob.setFromReticlePodPortID(fromPortID);
        reticleComponentJob.setFromEquipmentCategory(fromMachineCategory);
        reticleComponentJob.setJobStatus(BizConstant.SP_RCJ_STATUS_WAITTOEXECUTE);
        reticleComponentJob.setJobStatusChangeTimestamp(CimDateUtils.convertToSpecString(objCommon.getTimeStamp().getReportTimeStamp()));

        log.debug("step26 - reticleMethod.reticleComponentJobInsertDR");
        reticleMethod.reticleComponentJobInsertDR(objCommon, Collections.singletonList(reticleComponentJob));

        /*----------------*/
        /*   Event Make   */
        /*----------------*/
        Infos.ReticleEventRecord reticleEventRecord = new Infos.ReticleEventRecord();
        reticleEventRecord.setEventTime(CimDateUtils.convertToSpecString(objCommon.getTimeStamp().getReportTimeStamp()));
        reticleEventRecord.setReticleDispatchJobID(reticleDispatchJob.getReticleDispatchJobID());
        reticleEventRecord.setReticleComponentJobID(reticleComponentJob.getReticleComponentJobID());
        log.debug("step27 - reticleMethod.reticleEventQueuePutDR");
        reticleMethod.reticleEventQueuePutDR(objCommon, reticleEventRecord);
    }

    @Override
    public List<Infos.ReticlePodXferJobCompInfo> sxReticlePodXferJobCompRpt(Infos.ObjCommon objCommon, Params.ReticlePodXferJobCompRptParams params) {
        List<Infos.ReticlePodXferJobCompInfo> result = new ArrayList<>();
        String tmpArhs = StandardProperties.OM_ARHS_FLAG.getValue();
        if (log.isDebugEnabled())
            log.debug("ARHS switch on / off  tmpArhs = {}", tmpArhs);
        Validations.check(!CimStringUtils.equals(BizConstant.SP_ARMS_SWITCH_ON, tmpArhs), retCodeConfig.getFunctionNotAvailable());

        //=========================================================================
        // Check input parameter
        //=========================================================================
        Boolean firstRecord = true;
        if (CimArrayUtils.isNotEmpty(params.getStrReticlePodXferJobCompInfo())) {
            for (Infos.ReticlePodXferJobCompInfo reticlePodXferJobCompInfo : params.getStrReticlePodXferJobCompInfo()) {
                if (ObjectIdentifier.isEmptyWithValue(reticlePodXferJobCompInfo.getToMachineID())) {
                    log.error("toMachineID is null");
                    Validations.check(true, retCodeConfig.getInvalidInputParam());
                }
                if (log.isDebugEnabled())
                    log.debug("firstRecord: {}", firstRecord);
                if (CimBooleanUtils.isTrue(firstRecord)) {
                    firstRecord = false;
                    continue;
                } else {
                    if (!ObjectIdentifier.equalsWithValue(reticlePodXferJobCompInfo.getToMachineID(), params.getStrReticlePodXferJobCompInfo().get(0).getToMachineID())) {
                        Validations.check(true, retCodeConfig.getInvalidInputParam());
                    }
                }
            }
        }

        //=========================================================================
        // Check report is for equipment or stocker. (use 0th toMachineID)
        //=========================================================================
        log.debug("step1 - equipmentMethod.machineTypeGet");
        Outputs.ObjMachineTypeGetOut machineTypeGetOut = equipmentMethod.machineTypeGet(objCommon, params.getStrReticlePodXferJobCompInfo().get(0).getToMachineID());

        //=========================================================================
        // If it is equipment or bare reticle stocker, check port exists or not.
        //=========================================================================
        List<Infos.ReticlePodPortInfo> tmpReticlePodPortInfo = new ArrayList<>();
        Boolean bPortRequired = true;
        if (log.isDebugEnabled()){
            log.debug("stockerType         {}", machineTypeGetOut.getStockerType());
            log.debug("bStorageMachineFlag {}", machineTypeGetOut.isBStorageMachineFlag());
        }

        if (CimBooleanUtils.isFalse(machineTypeGetOut.isBStorageMachineFlag())) {
            log.debug("step2 - equipmentMethod.equipmentReticlePodPortInfoGetDR");
            Outputs.ObjEquipmentReticlePodPortInfoGetDROut equipmentReticlePodPortInfoGetDROut = equipmentMethod.equipmentReticlePodPortInfoGetDR(objCommon, machineTypeGetOut.getEquipmentID());
            if (null != equipmentReticlePodPortInfoGetDROut) {
                tmpReticlePodPortInfo = equipmentReticlePodPortInfoGetDROut.getReticlePodPortInfoList();
            }
        } else if (CimStringUtils.equals(machineTypeGetOut.getStockerType(), BizConstant.SP_STOCKER_TYPE_BARERETICLE)) {
            log.debug("step3 - stockerMethod.stockerReticlePodPortInfoGetDR");
            List<Infos.ReticlePodPortInfo> reticlePodPortInfoList = stockerMethod.stockerReticlePodPortInfoGetDR(objCommon, machineTypeGetOut.getStockerID());
            tmpReticlePodPortInfo = reticlePodPortInfoList;
        } else if (CimStringUtils.equals(machineTypeGetOut.getStockerType(), BizConstant.SP_STOCKER_TYPE_RETICLEPOD)) {
            bPortRequired = false;
        }
        if (log.isDebugEnabled())
            log.debug("bPortRequired: {}", bPortRequired);
        if (CimBooleanUtils.isTrue(bPortRequired)) {
            if (CimArrayUtils.isNotEmpty(params.getStrReticlePodXferJobCompInfo())) {
                for (Infos.ReticlePodXferJobCompInfo reticlePodXferJobCompInfo : params.getStrReticlePodXferJobCompInfo()) {
                    Boolean bPortFound = false;
                    if (CimArrayUtils.isNotEmpty(tmpReticlePodPortInfo)) {
                        for (Infos.ReticlePodPortInfo tmpReticlePodPort : tmpReticlePodPortInfo) {
                            if (ObjectIdentifier.equalsWithValue(reticlePodXferJobCompInfo.getToPortID(), tmpReticlePodPort.getReticlePodPortID())) {
                                bPortFound = true;
                                break;
                            }
                        }
                    }
                    if (log.isDebugEnabled())
                        log.debug("bPortFound: {}", bPortFound);
                    if (CimBooleanUtils.isFalse(bPortFound)) {
                        log.error("bPortFound == false");
                        Validations.check(true, retCodeConfig.getRspportNotFound(),
                                ObjectIdentifier.fetchValue(reticlePodXferJobCompInfo.getToMachineID()),
                                ObjectIdentifier.fetchValue(reticlePodXferJobCompInfo.getToPortID()));
                    }
                }
            }
        }
        //=========================================================================
        // Transfer Reservation Release for each reticle pod
        //=========================================================================
        List<ObjectIdentifier> firstReticleForPod = new ArrayList<>();
        if (CimArrayUtils.isNotEmpty(params.getStrReticlePodXferJobCompInfo())) {
            for (Infos.ReticlePodXferJobCompInfo reticlePodXferJobCompInfo : params.getStrReticlePodXferJobCompInfo()) {
                Long currentSlotNo = 0L;
                log.debug("step4 - reticleMethod.reticlePodTransferReserveCancel");
                reticleMethod.reticlePodTransferReserveCancel(objCommon, reticlePodXferJobCompInfo.getReticlePodID());
                Params.ReticlePodDetailInfoInqParams reticlePodDetailInfoInqParams = new Params.ReticlePodDetailInfoInqParams();
                reticlePodDetailInfoInqParams.setReticlePodID(reticlePodXferJobCompInfo.getReticlePodID());
                reticlePodDetailInfoInqParams.setDurableWipOperationInfoFlag(false);
                reticlePodDetailInfoInqParams.setDurableOperationInfoFlag(false);
                log.debug("step5 - reticleMethod.reticlePodFillInTxPDQ013DR");
                Outputs.ObjReticlePodFillInTxPDQ013DROut reticlePodFillInTxPDQ013DROut = reticleMethod.reticlePodFillInTxPDQ013DR(objCommon, reticlePodDetailInfoInqParams);
                ObjectIdentifier reticleForPodData = new ObjectIdentifier();
                if (null != reticlePodFillInTxPDQ013DROut && null != reticlePodFillInTxPDQ013DROut.getReticlePodStatusInfo() && CimArrayUtils.isNotEmpty(reticlePodFillInTxPDQ013DROut.getReticlePodStatusInfo().getStrContainedReticleInfo())) {
                    for (Infos.ContainedReticleInfo containedReticleInfo : reticlePodFillInTxPDQ013DROut.getReticlePodStatusInfo().getStrContainedReticleInfo()) {
                        if (currentSlotNo < 1 || currentSlotNo > containedReticleInfo.getSlotNo()) {
                            if (log.isDebugEnabled())
                                log.debug("currentSlotNo {}", currentSlotNo);
                            currentSlotNo = containedReticleInfo.getSlotNo();
                            reticleForPodData = containedReticleInfo.getReticleID();
                            firstReticleForPod.add(reticleForPodData);
                        }
                    }
                }
                if (CimStringUtils.equals(machineTypeGetOut.getStockerType(), BizConstant.SP_STOCKER_TYPE_RETICLEPOD)) {
                    log.debug("Reticle's reserve cancel when toMachine is RSPStocker.");
                    if (null != reticlePodFillInTxPDQ013DROut && null != reticlePodFillInTxPDQ013DROut.getReticlePodStatusInfo() && CimArrayUtils.isNotEmpty(reticlePodFillInTxPDQ013DROut.getReticlePodStatusInfo().getStrContainedReticleInfo())) {
                        for (Infos.ContainedReticleInfo containedReticleInfo : reticlePodFillInTxPDQ013DROut.getReticlePodStatusInfo().getStrContainedReticleInfo()) {
                            log.debug("step6 - reticleMethod.reticleReticlePodReserveCancel");
                            reticleMethod.reticleReticlePodReserveCancel(objCommon,
                                    containedReticleInfo.getReticleID(),
                                    false);
                        }
                    }
                }
            }
        }
        /*-------------------------------*/
        /*   Check RDJ/RCJ information   */
        /*-------------------------------*/
        log.debug("step7 - reticleMethod.reticleDispatchJobListGetDR");
        List<Infos.ReticleDispatchJob> reticleDispatchJobList = reticleMethod.reticleDispatchJobListGetDR(objCommon, "");
        Infos.ReticleDispatchJob strReticleDispatchJob = null;
        if (CimArrayUtils.isNotEmpty(params.getStrReticlePodXferJobCompInfo())) {
            for (int i = 0; i < CimArrayUtils.getSize(params.getStrReticlePodXferJobCompInfo()); i++) {
                Infos.ReticlePodXferJobCompInfo reticlePodXferJobCompInfo = params.getStrReticlePodXferJobCompInfo().get(i);
                Boolean bFoundFlag = false;
                if (CimArrayUtils.isNotEmpty(reticleDispatchJobList)) {
                    for (Infos.ReticleDispatchJob reticleDispatchJob : reticleDispatchJobList) {
                        if (ObjectIdentifier.equalsWithValue(reticlePodXferJobCompInfo.getReticlePodID(), reticleDispatchJob.getReticlePodID())) {
                            if (log.isDebugEnabled())
                                log.debug("Found ReticlePod! {}", ObjectIdentifier.fetchValue(reticlePodXferJobCompInfo.getReticlePodID()));
                            strReticleDispatchJob = reticleDispatchJob;
                            bFoundFlag = true;
                            break;
                        }
                    }
                }
                Boolean jobSuccessFlag = false;
                if (CimStringUtils.equals(reticlePodXferJobCompInfo.getTransferJobStatus(), BizConstant.SP_TRANSFERJOBSTATUS_COMP)) {
                    log.debug("TransferJobStatus is Comp.");
                    jobSuccessFlag = true;
                }
                Boolean bRCJFoundFlag = false;
                if (CimBooleanUtils.isTrue(bFoundFlag)) {
                    log.debug("TRUE == bFoundFlag");
                    log.debug("step8 - reticleMethod.reticleComponentJobGetByJobNameDR");
                    Outputs.ReticleComponentJobGetByJobNameDROut reticleComponentJobGetByJobNameDROut = reticleMethod.reticleComponentJobGetByJobNameDR(objCommon,
                            BizConstant.SP_RCJ_JOBNAME_XFER,
                            CimArrayUtils.isEmpty(firstReticleForPod) ? null : firstReticleForPod.get(i),
                            reticlePodXferJobCompInfo.getReticlePodID());
                    if (null != reticleComponentJobGetByJobNameDROut && null != reticleComponentJobGetByJobNameDROut.getStrReticleComponentJob()) {
                        Infos.ReticleComponentJob strReticleComponentJob = reticleComponentJobGetByJobNameDROut.getStrReticleComponentJob();
                        if (CimStringUtils.isEmpty(strReticleComponentJob.getReticleComponentJobID())) {
                            log.error("Reported data exist in RDJ list. but not found RCJ list.");
                            Validations.check(true, retCodeConfigEx.getRcjIncomplete());
                        } else if (!ObjectIdentifier.equalsWithValue(strReticleComponentJob.getToEquipmentID(), reticlePodXferJobCompInfo.getToMachineID())
                                && !ObjectIdentifier.equalsWithValue(strReticleComponentJob.getToReticlePodPortID(), reticlePodXferJobCompInfo.getToPortID())) {
                            log.error("This report is not same RCJ information. ");
                            Validations.check(true, retCodeConfigEx.getRcjNotMatchRequest());
                        } else {
                            log.debug("RDJ/RCJ data update");
                            bRCJFoundFlag = true;
                            /*-------------------------*/
                            /*   RDJ/RCJ data update   */
                            /*-------------------------*/
                            log.debug("step9 - reticleMethod.reticleJobStatusUpdateByReportDR");
                            reticleMethod.reticleJobStatusUpdateByReportDR(objCommon,
                                    strReticleComponentJob.getReticleComponentJobID(),
                                    jobSuccessFlag);
                        }
                    }
                }
                if (CimBooleanUtils.isTrue(bRCJFoundFlag) && CimBooleanUtils.isFalse(jobSuccessFlag)) {
                    /*---------------*/
                    /*   Send Mail   */
                    /*---------------*/
                    log.debug("Reported data exist in RDJ/RCJ. but it was error report.");
                    StringBuffer msgText = new StringBuffer("");
                    msgText.append("Error Type          : XferRpt").append("\n");
                    msgText.append("From Machine ID     : ").append("\n");
                    msgText.append("From Machine Port   : ").append("\n");
                    msgText.append("To Machine ID       : ").append(ObjectIdentifier.fetchValue(reticlePodXferJobCompInfo.getToMachineID())).append("\n");
                    msgText.append("To Machine Port     : ").append(ObjectIdentifier.fetchValue(reticlePodXferJobCompInfo.getToPortID())).append("\n");
                    msgText.append("Reticle Pod ID      : ").append(ObjectIdentifier.fetchValue(reticlePodXferJobCompInfo.getReticlePodID())).append("\n");
                    msgText.append("Reticle ID          : ").append("\n");
                    msgText.append("------------------------------------------------------------").append("\n");
                    msgText.append("Transaction ID      : ").append(objCommon.getTransactionID()).append("\n");
                    msgText.append("Return Code         : ").append("\n");
                    msgText.append("Message ID          : ").append(retCodeConfigEx.getArmsTcsReportedError().getCode()).append("\n");
                    msgText.append("Message Text        : ").append(retCodeConfigEx.getArmsTcsReportedError().getMessage()).append("\n");
                    msgText.append("Reason Text         : Request System Engineer to do problem determination and analysis of EAP trace log.");

                    Params.AlertMessageRptParams alertMessageRptParams = new Params.AlertMessageRptParams();
                    alertMessageRptParams.setSubSystemID(BizConstant.SP_SUBSYSTEMID_MM);
                    alertMessageRptParams.setSystemMessageCode(BizConstant.SP_SYSTEMMSGCODE_ARMSERROR);
                    alertMessageRptParams.setSystemMessageText(msgText.toString());
                    alertMessageRptParams.setNotifyFlag(true);
                    alertMessageRptParams.setSystemMessageTimeStamp(CimDateUtils.convertToSpecString(objCommon.getTimeStamp().getReportTimeStamp()));
                    log.debug("step10 - systemService.sxAlertMessageRpt");
                    systemService.sxAlertMessageRpt(objCommon, alertMessageRptParams);
                }
                /*----------------*/
                /*   Event Make   */
                /*----------------*/
                Infos.ReticleEventRecord reticleEventRecord = new Infos.ReticleEventRecord();
                reticleEventRecord.setEventTime(CimDateUtils.convertToSpecString(objCommon.getTimeStamp().getReportTimeStamp()));
                reticleEventRecord.setReticlePodID(reticlePodXferJobCompInfo.getReticlePodID());
                reticleEventRecord.setRSPPortEvent(BizConstant.SP_RCJ_JOBNAME_XFER);
                log.debug("step11 - reticleMethod.reticleEventQueuePutDR");
                reticleMethod.reticleEventQueuePutDR(objCommon, reticleEventRecord);
            }
        }
        return result;
    }

    @Override
    public void sxReticlePodUnclampRpt(Infos.ObjCommon objCommon, Params.ReticlePodUnclampRptParams params) {
        ObjectIdentifier machineID = params.getMachineID();
        ObjectIdentifier portID = params.getPortID();
        ObjectIdentifier reticlePodID = params.getReticlePodID();
        Boolean bSuccessFlag = params.getBSuccessFlag();

        String tmpArhs = StandardProperties.OM_ARHS_FLAG.getValue();
        if (log.isDebugEnabled())
            log.debug("ARHS switch on / off  tmpArhs = {}", tmpArhs);
        Validations.check(!CimStringUtils.equals(BizConstant.SP_ARMS_SWITCH_ON, tmpArhs), retCodeConfig.getFunctionNotAvailable());

        /*-----------------------------*/
        /*   Check machine condition   */
        /*-----------------------------*/
        log.debug("step1 - equipmentMethod.machineTypeGet");
        Outputs.ObjMachineTypeGetOut machineTypeGetOut = equipmentMethod.machineTypeGet(objCommon, machineID);
        Long machineType = 0L;
        if (CimBooleanUtils.isFalse(machineTypeGetOut.isBStorageMachineFlag())) {
            log.debug("in-para machineID is Equipment.");
            log.debug("step2 - equipmentMethod.equipmentOnlineModeGet");
            String equipmentOnlineModeGet = equipmentMethod.equipmentOnlineModeGet(objCommon, machineID);
            if (CimStringUtils.equals(equipmentOnlineModeGet, BizConstant.SP_EQP_ONLINEMODE_OFFLINE)) {
                log.error("This machine is Offline Mode {}.", equipmentOnlineModeGet);
                Validations.check(true, retCodeConfig.getEqpOnlineMode(),
                        ObjectIdentifier.fetchValue(machineID),
                        equipmentOnlineModeGet);
            }
            machineType = 1L;
        } else if (CimStringUtils.equals(machineTypeGetOut.getStockerType(), BizConstant.SP_STOCKER_TYPE_BARERETICLE)) {
            log.debug("in-para machineID is bareReticleStocker.");
            log.debug("step3 - stockerMethod.stockerOnlineModeGet");
            String stockerOnlineModeGet = stockerMethod.stockerOnlineModeGet(objCommon, machineID);
            if (CimStringUtils.equals(stockerOnlineModeGet, BizConstant.SP_EQP_ONLINEMODE_OFFLINE)) {
                log.error("stockerOnlineMode is Offline Mode {}.", stockerOnlineModeGet);
                Validations.check(true, retCodeConfig.getEqpOnlineMode(),
                        ObjectIdentifier.fetchValue(machineID),
                        stockerOnlineModeGet);
            }
            machineType = 2L;
        } else {
            log.error("Reported machine type is invalid.");
            Validations.check(true, retCodeConfig.getInvalidParameter());
        }
        /*----------------------------------------------*/
        /*   Confirm whether reticle is in reticlePod   */
        /*----------------------------------------------*/
        Params.ReticlePodDetailInfoInqParams reticlePodDetailInfoInqParams = new Params.ReticlePodDetailInfoInqParams();
        reticlePodDetailInfoInqParams.setReticlePodID(reticlePodID);
        reticlePodDetailInfoInqParams.setDurableWipOperationInfoFlag(false);
        reticlePodDetailInfoInqParams.setDurableOperationInfoFlag(false);
        log.debug("step4 - reticleMethod.reticlePodFillInTxPDQ013DR");
        Outputs.ObjReticlePodFillInTxPDQ013DROut reticlePodFillInTxPDQ013DROut = reticleMethod.reticlePodFillInTxPDQ013DR(objCommon, reticlePodDetailInfoInqParams);
        Boolean bReticleExistFlag = false;
        if (null != reticlePodFillInTxPDQ013DROut && null != reticlePodFillInTxPDQ013DROut.getReticlePodStatusInfo() && CimArrayUtils.isNotEmpty(reticlePodFillInTxPDQ013DROut.getReticlePodStatusInfo().getStrContainedReticleInfo())) {
            log.debug("This reticlePod have reticle(s).");
            bReticleExistFlag = true;
        }
        ObjectIdentifier firstReticleID = null;
        List<Infos.MoveReticles> strMoveReticles = new ArrayList<>();
        if (CimBooleanUtils.isTrue(bReticleExistFlag)) {
            log.debug("TRUE == bReticleExistFlag");
            for (Infos.ContainedReticleInfo containedReticleInfo : reticlePodFillInTxPDQ013DROut.getReticlePodStatusInfo().getStrContainedReticleInfo()) {
                Infos.MoveReticles moveReticles = new Infos.MoveReticles();
                strMoveReticles.add(moveReticles);
                moveReticles.setReticleID(containedReticleInfo.getReticleID());
                moveReticles.setSlotNumber((int) containedReticleInfo.getSlotNo());
                /*-------------------------------*/
                /*   Check reticles controlJob   */
                /*-------------------------------*/
                log.debug("step5 - reticleMethod.reticlecontrolJobInfoGet");
                Outputs.ReticleControlJobInfoGetOut reticleControlJobInfoGetOut = reticleMethod.reticlecontrolJobInfoGet(objCommon, moveReticles.getReticleID());
                if (null != reticleControlJobInfoGetOut && CimArrayUtils.isNotEmpty(reticleControlJobInfoGetOut.getStrControlJobAttributeInfoSeq())) {
                    //--------------------------------------------------
                    //   Get OM_RETICLE_RETRIEVE_WHEN_LOT_PROCESS
                    //--------------------------------------------------
                    Long retrieveReticleDuringLotProcFlag = StandardProperties.OM_RETICLE_RETRIEVE_WHEN_LOT_PROCESS.getLongValue();
                    if (log.isDebugEnabled())
                        log.debug("Env OM_RETICLE_RETRIEVE_WHEN_LOT_PROCESS {}", retrieveReticleDuringLotProcFlag);
                    if (1L == retrieveReticleDuringLotProcFlag) {
                        log.debug("Check if reticle has reserve control job");
                        for (Outputs.ControlJobAttributeInfo controlJobAttributeInfo : reticleControlJobInfoGetOut.getStrControlJobAttributeInfoSeq()) {
                            if (CimStringUtils.equals(BizConstant.SP_CONTROLJOBSTATUS_CREATED, controlJobAttributeInfo.getControlJobStatus())) {
                                log.error("controlJob status is Created");
                                Validations.check(true, retCodeConfigEx.getRtclHasCtrljob(),
                                        ObjectIdentifier.fetchValue(moveReticles.getReticleID()));
                            }
                        }
                    } else {
                        log.error("This reticle {} have controlJob.", ObjectIdentifier.fetchValue(moveReticles.getReticleID()));
                        Validations.check(true, retCodeConfigEx.getRtclHasCtrljob(),
                                ObjectIdentifier.fetchValue(moveReticles.getReticleID()));
                    }
                }
            }
            Long currentSlotNo = 0L;
            for (int i = 0; i < reticlePodFillInTxPDQ013DROut.getReticlePodStatusInfo().getStrContainedReticleInfo().size(); i++) {
                if (currentSlotNo < 1 || currentSlotNo > strMoveReticles.get(i).getSlotNumber()) {
                    firstReticleID = strMoveReticles.get(i).getReticleID();
                }
            }
        }
        /*-------------------*/
        /*   Check RDJ/RCJ   */
        /*-------------------*/
        //Check not conpleted store/retrieve job
        log.error("step6 - reticleMethod.reticleComponentJobListGetDR");
        List<Infos.ReticleComponentJob> reticleComponentJobList = reticleMethod.reticleComponentJobListGetDR(objCommon, "");
        if (CimArrayUtils.isNotEmpty(reticleComponentJobList)) {
            for (Infos.ReticleComponentJob reticleComponentJob : reticleComponentJobList) {
                String rcjJobName = reticleComponentJob.getJobName();
                String rcjStatus = reticleComponentJob.getJobStatus();
                // skip 'Completed' (say another words, check RCJ which status is 'WaitToExecute' or 'Executing' or 'Error' )
                if (CimStringUtils.equals(rcjStatus, BizConstant.SP_RCJ_STATUS_COMPLETED)) {
                    log.error("RCJ Status is Completed.");
                    continue;
                }
                // skip RCJ which is not 'Store' or 'Retrieve'
                if (!CimStringUtils.equals(rcjJobName, BizConstant.SP_RCJ_JOBNAME_STORE)
                        && !CimStringUtils.equals(rcjJobName, BizConstant.SP_RCJ_JOBNAME_RETRIEVE)) {
                    log.error("RCJ JobName is not Store, Retrieve.");
                    continue;
                }
                if (ObjectIdentifier.equalsWithValue(reticleComponentJob.getToEquipmentID(), machineID)
                        && ObjectIdentifier.equalsWithValue(reticleComponentJob.getToReticlePodPortID(), portID)
                        && ObjectIdentifier.equalsWithValue(reticleComponentJob.getReticlePodID(), reticlePodID)) {
                    log.error("This pod has store/retrieve job on this eqp/port that it is not completed.");
                    Validations.check(true, retCodeConfigEx.getRtclPodHasReticleJob(),
                            ObjectIdentifier.fetchValue(reticlePodID));
                }
            }
        }
        // Find RCJ/RDJ from report parameter. This method should not require existence of RDJ/RCJ.
        // If found RCJ/RDJ then update it, and if not, accept report without RDJ/RCJ process.
        log.debug("step7 - reticleMethod.reticleComponentJobGetByJobNameDR");
        Outputs.ReticleComponentJobGetByJobNameDROut reticleComponentJobGetByJobNameDROut = reticleMethod.reticleComponentJobGetByJobNameDR(objCommon,
                BizConstant.SP_RCJ_JOBNAME_UNCLAMP,
                firstReticleID,
                reticlePodID);
        String assoRCJID = null == reticleComponentJobGetByJobNameDROut || null == reticleComponentJobGetByJobNameDROut.getStrReticleComponentJob() ?
                null : reticleComponentJobGetByJobNameDROut.getStrReticleComponentJob().getReticleComponentJobID();
        Boolean bRCJFoundFlag = false;
        if (CimStringUtils.isNotEmpty(assoRCJID)) {
            if (log.isDebugEnabled())
                log.debug("Associated (related) RCJ {} found.", assoRCJID);
            bRCJFoundFlag = true;
            log.debug("step8 - reticleMethod.reticleJobStatusUpdateByReportDR");
            reticleMethod.reticleJobStatusUpdateByReportDR(objCommon,
                    assoRCJID,
                    bSuccessFlag);
        }
        if (CimBooleanUtils.isTrue(bRCJFoundFlag) && CimBooleanUtils.isFalse(bSuccessFlag)) {
            /*---------------*/
            /*   Send Mail   */
            /*---------------*/
            log.debug("Reported data exist in RDJ/RCJ. but it was error report.");
            StringBuffer msgText = new StringBuffer("");
            msgText.append("Error Type          : UnclampRpt").append("\n");
            msgText.append("From Machine ID     : ").append("\n");
            msgText.append("From Machine Port   : ").append("\n");
            msgText.append("To Machine ID       : ").append(ObjectIdentifier.fetchValue(machineID)).append("\n");
            msgText.append("To Machine Port     : ").append(ObjectIdentifier.fetchValue(portID)).append("\n");
            msgText.append("Reticle Pod ID      : ").append(ObjectIdentifier.fetchValue(reticlePodID)).append("\n");
            msgText.append("Reticle ID          : ").append("\n");
            msgText.append("------------------------------------------------------------").append("\n");
            msgText.append("Transaction ID      : ").append(objCommon.getTransactionID()).append("\n");
            msgText.append("Return Code         : ").append("\n");
            msgText.append("Message ID          : ").append(retCodeConfigEx.getArmsTcsReportedError().getCode()).append("\n");
            msgText.append("Message Text        : ").append(retCodeConfigEx.getArmsTcsReportedError().getMessage()).append("\n");
            msgText.append("Reason Text         : Request System Engineer to do problem determination and analysis of EAP trace log.");

            Params.AlertMessageRptParams alertMessageRptParams = new Params.AlertMessageRptParams();
            alertMessageRptParams.setSubSystemID(BizConstant.SP_SUBSYSTEMID_MM);
            alertMessageRptParams.setSystemMessageCode(BizConstant.SP_SYSTEMMSGCODE_ARMSERROR);
            alertMessageRptParams.setSystemMessageText(msgText.toString());
            alertMessageRptParams.setNotifyFlag(true);
            alertMessageRptParams.setSystemMessageTimeStamp(CimDateUtils.convertToSpecString(objCommon.getTimeStamp().getReportTimeStamp()));
            log.debug("step9 - systemService.sxAlertMessageRpt");
            systemService.sxAlertMessageRpt(objCommon, alertMessageRptParams);
        }
        /*----------------*/
        /*   Event Make   */
        /*----------------*/
        Infos.ReticleEventRecord reticleEventRecord = new Infos.ReticleEventRecord();
        reticleEventRecord.setEventTime(CimDateUtils.convertToSpecString(objCommon.getTimeStamp().getReportTimeStamp()));
        if (machineType == 1L) {
            log.debug("machineType == 1");
            reticleEventRecord.setEquipmentID(machineID);
            reticleEventRecord.setRSPPortID(portID);
        }
        if (machineType == 2L) {
            log.debug("machineType == 2");
            reticleEventRecord.setBareReticleStockerID(machineID);
            reticleEventRecord.setResourceID(portID);
        }
        reticleEventRecord.setReticlePodID(reticlePodID);
        reticleEventRecord.setRSPPortEvent(BizConstant.SP_RCJ_JOBNAME_UNCLAMP);
        log.debug("step10 - reticleMethod.reticleEventQueuePutDR");
        reticleMethod.reticleEventQueuePutDR(objCommon, reticleEventRecord);
    }

    @Override
    public void sxReticlePodUnclampReq(Infos.ObjCommon objCommon, Params.ReticlePodUnclampReqParams params) {
        String reticleDispatchJobID = params.getReticleDispatchJobID();
        String reticleComponentJobID = params.getReticleComponentJobID();
        ObjectIdentifier machineID = params.getMachineID();
        ObjectIdentifier portID = params.getPortID();
        ObjectIdentifier reticlePodID = params.getReticlePodID();

        String tmpArhs = StandardProperties.OM_ARHS_FLAG.getValue();
        if (log.isDebugEnabled()){
            log.debug("ARHS switch on / off  tmpArhs = {}", tmpArhs);
        }
        Validations.check(!CimStringUtils.equals(BizConstant.SP_ARMS_SWITCH_ON, tmpArhs), retCodeConfig.getFunctionNotAvailable());

        /*-------------------*/
        /*   Check RDJ/RCJ   */
        /*-------------------*/
        if (log.isDebugEnabled()){
            log.debug("step1 - reticleMethod.reticleDispatchJobListGetDR");
        }
        List<Infos.ReticleDispatchJob> reticleDispatchJobList = reticleMethod.reticleDispatchJobListGetDR(objCommon, reticleDispatchJobID);
        int rdjLen = CimArrayUtils.getSize(reticleDispatchJobList);
        if (rdjLen == 0) {
            log.error("This RDJ_ID not found.");
            Validations.check(true, retCodeConfigEx.getRdjNotFound());
        } else if (rdjLen > 1) {
            log.error("this RDJ_ID is duplicate.");
            Validations.check(true, retCodeConfigEx.getRdjDuplicate());
        }
        Infos.ReticleDispatchJob reticleDispatchJob = reticleDispatchJobList.get(0);
        if (!CimStringUtils.equals(reticleDispatchJob.getReticleDispatchJobID(), reticleDispatchJobID)
                || !ObjectIdentifier.equalsWithValue(reticleDispatchJob.getReticlePodID(), reticlePodID)) {
            log.error("This request Mismatch RDJ information. pod {}", ObjectIdentifier.fetchValue(reticleDispatchJob.getReticlePodID()));
            Validations.check(true, retCodeConfigEx.getInvalidRequestToRdj());
        }

        if (log.isDebugEnabled()){
            log.debug("step2 - reticleMethod.reticleComponentJobListGetDR");
        }
        List<Infos.ReticleComponentJob> reticleComponentJobList = reticleMethod.reticleComponentJobListGetDR(objCommon, reticleDispatchJobID);
        int rcjLen = CimArrayUtils.getSize(reticleComponentJobList);
        if (rcjLen == 0) {
            log.error("this request is not found RCJ list.");
            Validations.check(true, retCodeConfigEx.getRcjNotFound());
        }
        /*-------------------------------------*/
        /*   Not In Check Store/Retrieve job   */
        /*-------------------------------------*/
        for (Infos.ReticleComponentJob rcj : reticleComponentJobList) {
            if (CimStringUtils.equals(rcj.getReticleDispatchJobID(), reticleDispatchJobID)
                    && ObjectIdentifier.equalsWithValue(rcj.getReticlePodID(), reticlePodID)
                    && ObjectIdentifier.equalsWithValue(rcj.getToEquipmentID(), machineID)
                    && ObjectIdentifier.equalsWithValue(rcj.getToReticlePodPortID(), portID)
                    && (CimStringUtils.equals(rcj.getJobName(), BizConstant.SP_RCJ_JOBNAME_STORE)
                    || CimStringUtils.equals(rcj.getJobName(), BizConstant.SP_RCJ_JOBNAME_RETRIEVE))
                    && !CimStringUtils.equals(rcj.getJobStatus(), BizConstant.SP_RCJ_STATUS_COMPLETED)) {
                log.error("This RDJ have Store/Retrieve job which is not yet execute.");
                Validations.check(true, retCodeConfigEx.getRdjHaveNotExcuteJob(),
                        reticleDispatchJobID,
                        rcj.getJobName());
            }
        }
        Boolean bFoundFlag = false;
        for (Infos.ReticleComponentJob rcj : reticleComponentJobList) {
            if (CimStringUtils.equals(rcj.getReticleComponentJobID(), reticleComponentJobID)
                    && ObjectIdentifier.equalsWithValue(rcj.getReticlePodID(), reticlePodID)
                    && ObjectIdentifier.equalsWithValue(rcj.getToEquipmentID(), machineID)
                    && ObjectIdentifier.equalsWithValue(rcj.getToReticlePodPortID(), portID)
                    && CimStringUtils.equals(rcj.getJobName(), BizConstant.SP_RCJ_JOBNAME_UNCLAMP)) {
                if (log.isDebugEnabled()){
                    log.debug("Found RCJ information");
                }
                bFoundFlag = true;
                break;
            }
        }
        if (CimBooleanUtils.isFalse(bFoundFlag)) {
            log.error("This request mismatch RCJ information.");
            Validations.check(true, retCodeConfigEx.getInvalidRequestToRcj());
        }
        /*-----------------------------*/
        /*   Check machine condition   */
        /*-----------------------------*/
        if (log.isDebugEnabled()){
            log.debug("step3 - equipmentMethod.machineTypeGet");
        }
        Outputs.ObjMachineTypeGetOut machineTypeGetOut = equipmentMethod.machineTypeGet(objCommon, machineID);
        List<Infos.ReticlePodPortInfo> strReticlePodPortInfoList = new ArrayList<>();
        if (CimBooleanUtils.isFalse(machineTypeGetOut.isBStorageMachineFlag())) {
            if (log.isDebugEnabled()){
                log.debug("TRUE != bStorageMachineFlag");
                log.debug("step4 - equipmentMethod.equipmentOnlineModeGet");
            }
            String equipmentOnlineModeGet = equipmentMethod.equipmentOnlineModeGet(objCommon, machineID);
            if (!CimStringUtils.equals(equipmentOnlineModeGet, BizConstant.SP_EQP_ONLINEMODE_ONLINEREMOTE)) {
                log.error("onlineMode: {}", equipmentOnlineModeGet);
                Validations.check(true, retCodeConfig.getEqpOnlineMode(),
                        ObjectIdentifier.fetchValue(machineID),
                        equipmentOnlineModeGet);
            }
            if (log.isDebugEnabled()){
                log.debug("step5 - equipmentMethod.equipmentReticlePodPortInfoGetDR");
            }
            Outputs.ObjEquipmentReticlePodPortInfoGetDROut equipmentReticlePodPortInfoGetDROut = equipmentMethod.equipmentReticlePodPortInfoGetDR(objCommon, machineID);
            if (null != equipmentReticlePodPortInfoGetDROut) {
                strReticlePodPortInfoList = equipmentReticlePodPortInfoGetDROut.getReticlePodPortInfoList();
            }
        } else if (CimStringUtils.equals(machineTypeGetOut.getStockerType(), BizConstant.SP_STOCKER_TYPE_BARERETICLE)) {
            if (log.isDebugEnabled()){
                log.debug("StockerType is BareReticle");
                log.debug("step6 - stockerMethod.stockerOnlineModeGet");
            }
            String stockerOnlineModeGet = stockerMethod.stockerOnlineModeGet(objCommon, machineID);
            if (!CimStringUtils.equals(stockerOnlineModeGet, BizConstant.SP_EQP_ONLINEMODE_ONLINEREMOTE)) {
                log.error("stockerOnlineMode {} is Online Mode", stockerOnlineModeGet);
                Validations.check(true, retCodeConfig.getEqpOnlineMode(),
                        ObjectIdentifier.fetchValue(machineID),
                        stockerOnlineModeGet);
            }
            if (log.isDebugEnabled()){
                log.debug("step7 - stockerMethod.stockerReticlePodPortInfoGetDR");
            }
            List<Infos.ReticlePodPortInfo> reticlePodPortInfos = stockerMethod.stockerReticlePodPortInfoGetDR(objCommon, machineID);
            strReticlePodPortInfoList = reticlePodPortInfos;
        } else {
            log.error("in-para machineID is not equipment or bareReticleStocker");
            Validations.check(true, retCodeConfig.getInvalidParameter());
        }
        // Check if reticlePod is loaded on specified port of specified machine.
        Boolean checkFlag = false;
        int i;
        for (i = 0; i < CimArrayUtils.getSize(strReticlePodPortInfoList); i++) {
            if (ObjectIdentifier.equalsWithValue(strReticlePodPortInfoList.get(i).getReticlePodPortID(), portID)) {
                checkFlag = true;
                if (log.isDebugEnabled()){
                    log.debug("Port {} found on machine {} .", ObjectIdentifier.fetchValue(portID), ObjectIdentifier.fetchValue(machineID));
                }
                break;
            }
        }
        if (i == CimArrayUtils.getSize(strReticlePodPortInfoList)) {
            log.error("Port {} not found on this machine {}.", ObjectIdentifier.fetchValue(portID), ObjectIdentifier.fetchValue(machineID));
            Validations.check(true, retCodeConfig.getRspportNotFound(),
                    ObjectIdentifier.fetchValue(machineID),
                    ObjectIdentifier.fetchValue(portID));
        } else if (!ObjectIdentifier.equalsWithValue(strReticlePodPortInfoList.get(i).getLoadedReticlePodID(), reticlePodID)) {
            log.error("Specified reticlePod {} is not loaded on this port {} {}.", ObjectIdentifier.fetchValue(portID),
                    ObjectIdentifier.fetchValue(machineID),
                    ObjectIdentifier.fetchValue(reticlePodID));
            Validations.check(true, retCodeConfigEx.getRtclpodNotLoaded(),
                    ObjectIdentifier.fetchValue(reticlePodID),
                    ObjectIdentifier.fetchValue(machineID),
                    ObjectIdentifier.fetchValue(portID));
        }
        /*----------------------------------------------*/
        /*   Confirm whether reticle is in reticlePod   */
        /*----------------------------------------------*/
        Params.ReticlePodDetailInfoInqParams reticlePodDetailInfoInqParams = new Params.ReticlePodDetailInfoInqParams();
        reticlePodDetailInfoInqParams.setReticlePodID(reticlePodID);
        reticlePodDetailInfoInqParams.setDurableWipOperationInfoFlag(false);
        reticlePodDetailInfoInqParams.setDurableOperationInfoFlag(false);
        if (log.isDebugEnabled()){
            log.debug("step8 - reticleMethod.reticlePodFillInTxPDQ013DR");
        }
        Outputs.ObjReticlePodFillInTxPDQ013DROut reticlePodFillInTxPDQ013DROut = reticleMethod.reticlePodFillInTxPDQ013DR(objCommon, reticlePodDetailInfoInqParams);

        Boolean bReticleExistFlag = false;
        if (null != reticlePodFillInTxPDQ013DROut && null != reticlePodFillInTxPDQ013DROut.getReticlePodStatusInfo() && CimArrayUtils.isNotEmpty(reticlePodFillInTxPDQ013DROut.getReticlePodStatusInfo().getStrContainedReticleInfo())) {
            if (log.isDebugEnabled()){
                log.debug("This reticlePod have reticle(s).");
            }
            bReticleExistFlag = true;
        }
        List<ObjectIdentifier> reticleIDs = new ArrayList<>();
        if (CimBooleanUtils.isTrue(bReticleExistFlag)) {
            if (log.isDebugEnabled()){
                log.debug("TRUE == bReticleExistFlag");
            }
            for (Infos.ContainedReticleInfo containedReticleInfo : reticlePodFillInTxPDQ013DROut.getReticlePodStatusInfo().getStrContainedReticleInfo()) {
                reticleIDs.add(containedReticleInfo.getReticleID());
                /*-------------------------------*/
                /*   Check reticles controlJob   */
                /*-------------------------------*/
                if (log.isDebugEnabled()){
                    log.debug("step9 - reticleMethod.reticlecontrolJobInfoGet");
                }
                Outputs.ReticleControlJobInfoGetOut reticleControlJobInfoGetOut = reticleMethod.reticlecontrolJobInfoGet(objCommon, containedReticleInfo.getReticleID());
                if (null != reticleControlJobInfoGetOut && CimArrayUtils.isNotEmpty(reticleControlJobInfoGetOut.getStrControlJobAttributeInfoSeq())) {
                    //--------------------------------------------------
                    //   Get OM_RETICLE_RETRIEVE_WHEN_LOT_PROCESS
                    //--------------------------------------------------
                    Long retrieveReticleDuringLotProcFlag = StandardProperties.OM_RETICLE_RETRIEVE_WHEN_LOT_PROCESS.getLongValue();
                    if (log.isDebugEnabled()){
                        log.debug("Env OM_RETICLE_RETRIEVE_WHEN_LOT_PROCESS {}", retrieveReticleDuringLotProcFlag);
                    }
                    if (1L == retrieveReticleDuringLotProcFlag) {
                        if (log.isDebugEnabled()){
                            log.debug("Check if reticle has reserve control job");
                        }
                        for (Outputs.ControlJobAttributeInfo controlJobAttributeInfo : reticleControlJobInfoGetOut.getStrControlJobAttributeInfoSeq()) {
                            if (CimStringUtils.equals(BizConstant.SP_CONTROLJOBSTATUS_CREATED, controlJobAttributeInfo.getControlJobStatus())) {
                                log.error("controlJob status is Created");
                                Validations.check(true, retCodeConfigEx.getRtclHasCtrljob(), ObjectIdentifier.fetchValue(containedReticleInfo.getReticleID()));
                            }
                        }
                    } else {
                        log.error("This reticle {} have controlJob.", ObjectIdentifier.fetchValue(containedReticleInfo.getReticleID()));
                        Validations.check(true, retCodeConfigEx.getRtclHasCtrljob(), ObjectIdentifier.fetchValue(containedReticleInfo.getReticleID()));
                    }
                }
            }
        }
        /*---------------------------*/
        /*   Update RDJ/RCJ Status   */
        /*---------------------------*/
        if (log.isDebugEnabled()){
            log.debug("step10 - reticleMethod.reticleJobStatusUpdateByRequestDR");
        }
        reticleMethod.reticleJobStatusUpdateByRequestDR(objCommon,
                reticleDispatchJobID,
                reticleComponentJobID,
                true);

        Inputs.SendReticlePodUnclampReqIn in = new Inputs.SendReticlePodUnclampReqIn();
        in.setObjCommonIn(objCommon);
        in.setEquipmentID(machineID);
        in.setPortID(portID);
        in.setReticlePodID(reticlePodID);
        if (log.isDebugEnabled()){
            log.debug("step11 - tcsMethod.sendTCSReq");
        }
        // TODO: 2021/6/25 EAP todo add
//        tcsMethod.sendTCSReq(TCSReqEnum.sendReticlePodUnclampReq, in);
    }

    @Override
    public void sxReticlePodUnclampAndXferJobCreateReq(Infos.ObjCommon objCommon, Params.ReticlePodUnclampAndXferJobCreateReqParams params) {
        ObjectIdentifier fromMachineID = params.getFromMachineID();
        ObjectIdentifier fromPortID = params.getFromPortID();
        ObjectIdentifier reticlePodID = params.getReticlePodID();
        ObjectIdentifier toMachineID = params.getToMachineID();
        ObjectIdentifier toPortID = params.getToPortID();

        String tmpArhs = StandardProperties.OM_ARHS_FLAG.getValue();
        if (log.isDebugEnabled()){
            log.debug("ARHS switch on / off  tmpArhs = {}", tmpArhs);
        }
        Validations.check(!CimStringUtils.equals(BizConstant.SP_ARMS_SWITCH_ON, tmpArhs), retCodeConfig.getFunctionNotAvailable());

        /*----------------------------------------------*/
        /*   Confirm whether reticle is in reticlePod   */
        /*----------------------------------------------*/

        log.debug("Confirm whether reticle is in reticlePod");
        Params.ReticlePodDetailInfoInqParams reticlePodDetailInfoInqParams = new Params.ReticlePodDetailInfoInqParams();
        reticlePodDetailInfoInqParams.setReticlePodID(reticlePodID);
        reticlePodDetailInfoInqParams.setDurableOperationInfoFlag(false);
        reticlePodDetailInfoInqParams.setDurableWipOperationInfoFlag(false);
        Outputs.ObjReticlePodFillInTxPDQ013DROut objReticlePodFillInTxPDQ013DROut = reticleMethod.reticlePodFillInTxPDQ013DR(objCommon, reticlePodDetailInfoInqParams);

        List<Infos.ContainedReticleInfo> strContainedReticleInfo = objReticlePodFillInTxPDQ013DROut.getReticlePodStatusInfo().getStrContainedReticleInfo();
        int rtclLen = CimArrayUtils.getSize(strContainedReticleInfo);
        boolean bReticleExistFlag = false;
        if (rtclLen != 0) {
            log.debug("This reticlePod have reticle(s).");
            bReticleExistFlag = true;
        }

        if (bReticleExistFlag) {
            log.debug("bReticleExistFlag = TRUE");
            for (Infos.ContainedReticleInfo containedReticleInfo : strContainedReticleInfo) {
                if (log.isDebugEnabled()){
                    log.debug("durableID {}", ObjectIdentifier.fetchValue(containedReticleInfo.getReticleID()));
                }

                ObjectIdentifier durableControlJobID = durableMethod.durableDurableControlJobIDGet(objCommon, containedReticleInfo.getReticleID(), BizConstant.SP_DURABLECAT_RETICLE);
                Validations.check(!ObjectIdentifier.isEmpty(durableControlJobID), retCodeConfig.getDurableControlJobFilled());

                if (!ObjectIdentifier.isEmpty(durableControlJobID)) {
                    if (log.isDebugEnabled()){
                        log.debug("durableControlJobID {} is not blank", durableControlJobID);
                    }
                    objectLockMethod.objectLock(objCommon, CimDurableControlJob.class, durableControlJobID);
                    Infos.DurableControlJobStatusGet durableControlJobStatusGet = durableMethod.durableControlJobStatusGet(objCommon, durableControlJobID);

                    String durableControlJobStatus = durableControlJobStatusGet.getDurableControlJobStatus();
                    Validations.check(!CimStringUtils.equals(durableControlJobStatus, BizConstant.SP_DURABLECONTROLJOBSTATUS_CREATED) &&
                                    !CimStringUtils.equals(durableControlJobStatus, BizConstant.SP_DURABLECONTROLJOBSTATUS_DELETE),
                            retCodeConfig.getInvalidDcjstatus(), durableControlJobStatus);
                }
            }
        } else {
            log.debug("bReticleExistFlag = FALSE");
            ObjectIdentifier durableControlJobID = durableMethod.durableDurableControlJobIDGet(objCommon, reticlePodID, BizConstant.SP_DURABLECAT_RETICLEPOD);

            if (!ObjectIdentifier.isEmpty(durableControlJobID)) {
                if (log.isDebugEnabled()){
                    log.debug("durableControlJobID {} is not blank", durableControlJobID);
                }
                objectLockMethod.objectLock(objCommon, CimDurableControlJob.class, durableControlJobID);

                Infos.DurableControlJobStatusGet durableControlJobStatusGet = durableMethod.durableControlJobStatusGet(objCommon, durableControlJobID);
                String durableControlJobStatus = durableControlJobStatusGet.getDurableControlJobStatus();
                Validations.check(!CimStringUtils.equals(durableControlJobStatus, BizConstant.SP_DURABLECONTROLJOBSTATUS_CREATED) &&
                                !CimStringUtils.equals(durableControlJobStatus, BizConstant.SP_DURABLECONTROLJOBSTATUS_DELETE),
                        retCodeConfig.getInvalidDcjstatus(), durableControlJobStatus);
            }
        }

        /*-----------------------------*/
        /*  Lock Reticle Pod to check  */
        /*-----------------------------*/
        log.debug("Lock Reticle Pod to check");
        objectLockMethod.objectLock(objCommon, CimReticlePod.class, reticlePodID);

        List<Infos.MoveReticles> strMoveReticles = new ArrayList<>();
        if (bReticleExistFlag) {
            for (Infos.ContainedReticleInfo containedReticleInfo : strContainedReticleInfo) {
                strMoveReticles.add(new Infos.MoveReticles(containedReticleInfo.getReticleID(), (int)containedReticleInfo.getSlotNo()));

                /*-------------------------------*/
                /*   Check reticles controlJob   */
                /*-------------------------------*/
                log.debug("Check reticles controlJob");
                Outputs.ReticleControlJobInfoGetOut reticleControlJobInfoGetOut = reticleMethod.reticlecontrolJobInfoGet(objCommon, containedReticleInfo.getReticleID());
                List<Outputs.ControlJobAttributeInfo> strControlJobAttributeInfoSeq = reticleControlJobInfoGetOut.getStrControlJobAttributeInfoSeq();
                if (!CimObjectUtils.isEmpty(strControlJobAttributeInfoSeq)) {
                    //--------------------------------------------------
                    //   Get OM_RETICLE_RETRIEVE_WHEN_LOT_PROCESS
                    //--------------------------------------------------
                    int retrieveReticleDuringLotProcFlag = StandardProperties.OM_RETICLE_RETRIEVE_WHEN_LOT_PROCESS.getIntValue();
                    if (log.isDebugEnabled())
                        log.debug("Env OM_RETICLE_RETRIEVE_WHEN_LOT_PROCESS {}", retrieveReticleDuringLotProcFlag);
                    if (1 == retrieveReticleDuringLotProcFlag) {
                        log.debug("Check if reticle has reserve control job");
                        for (Outputs.ControlJobAttributeInfo controlJobAttributeInfo : strControlJobAttributeInfoSeq) {
                            Validations.check(CimStringUtils.equals(BizConstant.SP_CONTROLJOBSTATUS_CREATED, controlJobAttributeInfo.getControlJobStatus()), retCodeConfigEx.getRtclHasCtrljob(), containedReticleInfo.getReticleID());
                        }
                    } else {
                        log.error("This reticle {} have controlJob.", ObjectIdentifier.fetchValue(containedReticleInfo.getReticleID()));
                        Validations.check(retCodeConfigEx.getRtclHasCtrljob(), containedReticleInfo.getReticleID());
                    }
                }
            }

            for (int i = 0; i < strContainedReticleInfo.size(); i++) {
                /*-------------------*/
                /*   Check RDJ/RCJ   */
                /*-------------------*/
                log.debug("Check RDJ/RCJ");
                reticleMethod.reticleDispatchJobCheckExistenceDR(objCommon, strMoveReticles.get(i).getReticleID(), reticlePodID, null);

                List<Infos.ReticleComponentJob> reticleComponentJobs = new ArrayList<>();
                try {
                    reticleComponentJobs = reticleMethod.reticleComponentJobCheckExistenceDR(objCommon,
                            strMoveReticles.get(i).getReticleID(),
                            reticlePodID,
                            fromMachineID,
                            fromPortID,
                            toMachineID,
                            toPortID);
                } catch (ServiceException e) {
                    if (Validations.isEquals(retCodeConfigEx.getFoundInRcj(), e.getCode())) {
                        reticleComponentJobs = e.getData(List.class);
                        if (CimArrayUtils.isNotEmpty(reticleComponentJobs)) {
                            for (Infos.ReticleComponentJob reticleComponentJob : reticleComponentJobs) {
                                if (!CimStringUtils.equals(reticleComponentJob.getJobName(), BizConstant.SP_RCJ_JOBNAME_XFER)
                                        || CimStringUtils.equals(reticleComponentJob.getJobStatus(), BizConstant.SP_RCJ_STATUS_WAITTOEXECUTE)
                                        || ((!ObjectIdentifier.equalsWithValue(reticleComponentJob.getFromEquipmentID(), toMachineID)
                                        || !ObjectIdentifier.equalsWithValue(reticleComponentJob.getFromReticlePodPortID(), toPortID)
                                ) && (!ObjectIdentifier.equalsWithValue(reticleComponentJob.getFromEquipmentID(), fromMachineID)
                                        || !ObjectIdentifier.equalsWithValue(reticleComponentJob.getFromReticlePodPortID(), fromPortID)))) {
                                    throw e;
                                }
                            }
                        }
                    } else {
                        throw e;
                    }
                }
            }
        } else {
            /*-------------------*/
            /*   Check RDJ/RCJ   */
            /*-------------------*/
            log.debug("Check RDJ/RCJ");
            reticleMethod.reticleDispatchJobCheckExistenceDR(objCommon, null, reticlePodID, null);

            List<Infos.ReticleComponentJob> reticleComponentJobs;
            try {
                reticleComponentJobs = reticleMethod.reticleComponentJobCheckExistenceDR(objCommon,
                        null,
                        reticlePodID,
                        fromMachineID,
                        fromPortID,
                        toMachineID,
                        toPortID);
            } catch (ServiceException e) {
                if (Validations.isEquals(retCodeConfigEx.getFoundInRcj(), e.getCode())) {
                    reticleComponentJobs = e.getData(List.class);
                    if (CimArrayUtils.isNotEmpty(reticleComponentJobs)) {
                        for (Infos.ReticleComponentJob reticleComponentJob : reticleComponentJobs) {
                            if (!CimStringUtils.equals(reticleComponentJob.getJobName(), BizConstant.SP_RCJ_JOBNAME_XFER)
                                    || CimStringUtils.equals(reticleComponentJob.getJobStatus(), BizConstant.SP_RCJ_STATUS_WAITTOEXECUTE)
                                    || ((!ObjectIdentifier.equalsWithValue(reticleComponentJob.getFromEquipmentID(), toMachineID)
                                    || !ObjectIdentifier.equalsWithValue(reticleComponentJob.getFromReticlePodPortID(), toPortID)
                            ) && (!ObjectIdentifier.equalsWithValue(reticleComponentJob.getFromEquipmentID(), fromMachineID)
                                    || !ObjectIdentifier.equalsWithValue(reticleComponentJob.getFromReticlePodPortID(), fromPortID)))) {
                                throw e;
                            }
                        }
                    }
                } else {
                    throw e;
                }
            }
        }

        /*----------------------------------*/
        /*   Check from Machine condition   */
        /*----------------------------------*/
        log.debug("Check from Machine condition");
        String fromMachineCategory = null;
        int fromMachineType = 0;
        Outputs.ObjMachineTypeGetOut objMachineTypeGetOut = equipmentMethod.machineTypeGet(objCommon, fromMachineID);

        if (!objMachineTypeGetOut.isBStorageMachineFlag()) {
            log.debug("fromMachine type is Equipment");
            fromMachineType = 1;
            fromMachineCategory = BizConstant.SP_MACHINE_TYPE_EQP;
        } else {
            if (CimStringUtils.equals(objMachineTypeGetOut.getStockerType(), BizConstant.SP_STOCKER_TYPE_BARERETICLE)) {
                log.debug("fromMachine type is BareReticleStocker");
                fromMachineType = 2;
                fromMachineCategory = BizConstant.SP_STOCKER_TYPE_BARERETICLE;
            } else {
                log.debug("machine type is invalid.");
                Validations.check(retCodeConfigEx.getRtclPodDestInvalid(), reticlePodID, fromMachineID);
            }
        }

        Infos.ReticlePodPortInfo fromReticlePodPortInfo = null;
        if (fromMachineType == 1) {
            String onlineMode = equipmentMethod.equipmentOnlineModeGet(objCommon, fromMachineID);
            Validations.check(!CimStringUtils.equals(onlineMode, BizConstant.SP_EQP_ONLINEMODE_ONLINEREMOTE), retCodeConfig.getEqpOnlineMode(), fromMachineID, onlineMode);

            Outputs.ObjEquipmentReticlePodPortInfoGetDROut objEquipmentReticlePodPortInfoGetDROut = equipmentMethod.equipmentReticlePodPortInfoGetDR(objCommon, fromMachineID);
            List<Infos.ReticlePodPortInfo> reticlePodPortInfoList = objEquipmentReticlePodPortInfoGetDROut.getReticlePodPortInfoList();

            boolean bPodOnPortFlag = false;
            for (Infos.ReticlePodPortInfo reticlePodPortInfo : reticlePodPortInfoList) {
                if (ObjectIdentifier.equalsWithValue(reticlePodPortInfo.getReticlePodPortID(), fromPortID)
                        && ObjectIdentifier.equalsWithValue(reticlePodPortInfo.getLoadedReticlePodID(), reticlePodID)) {
                    log.debug("Found requested reticlePod on Port.");
                    fromReticlePodPortInfo = reticlePodPortInfo;
                    bPodOnPortFlag = true;
                    break;
                }
            }
            Validations.check(!bPodOnPortFlag, retCodeConfigEx.getRtclpodNotLoaded(), reticlePodID, fromMachineID, fromPortID);
        }

        if (fromMachineType == 2) {
            String onlineMode = stockerMethod.stockerOnlineModeGet(objCommon, fromMachineID);
            Validations.check(!CimStringUtils.equals(onlineMode, BizConstant.SP_EQP_ONLINEMODE_ONLINEREMOTE), retCodeConfig.getEqpOnlineMode(), fromMachineID, onlineMode);

            List<Infos.ReticlePodPortInfo> reticlePodPortInfos = stockerMethod.stockerReticlePodPortInfoGetDR(objCommon, fromMachineID);

            boolean bPodOnPortFlag = false;
            for (Infos.ReticlePodPortInfo reticlePodPortInfo : reticlePodPortInfos) {
                if (ObjectIdentifier.equalsWithValue(reticlePodPortInfo.getReticlePodPortID(), fromPortID) &&
                        ObjectIdentifier.equalsWithValue(reticlePodPortInfo.getLoadedReticlePodID(), reticlePodID)) {
                    log.debug("Found requested reticlePod on Port.");
                    fromReticlePodPortInfo = reticlePodPortInfo;
                    bPodOnPortFlag = true;
                    break;
                }
            }
            Validations.check(!bPodOnPortFlag, retCodeConfigEx.getRtclpodNotLoaded(), reticlePodID, fromMachineID, fromPortID);
        }

        if (fromMachineType == 1 || fromMachineType == 2) {
            Validations.check(!CimStringUtils.equals(fromReticlePodPortInfo.getAccessMode(), BizConstant.SP_EQP_ACCESSMODE_AUTO), retCodeConfigEx.getEqpPortAccessMode(), fromMachineID, fromPortID, fromReticlePodPortInfo.getAccessMode());
            Validations.check(!CimStringUtils.equals(fromReticlePodPortInfo.getPortStatus(), BizConstant.SP_PORTRSC_PORTSTATE_LOADCOMP), retCodeConfig.getInvalidPortState(), fromMachineID, fromReticlePodPortInfo.getPortStatus());
            Validations.check(!CimStringUtils.equals(fromReticlePodPortInfo.getDispatchStatus(), BizConstant.SP_PORTRSC_DISPATCHSTATE_REQUIRED), retCodeConfigEx.getInvalidMachinePortDispatchState(), fromMachineID, fromPortID, fromReticlePodPortInfo.getDispatchStatus());
            Validations.check(!CimStringUtils.equals(fromReticlePodPortInfo.getTransferReserveStatus(), BizConstant.SP_RETICLEPODPORT_NOTRESERVED), retCodeConfigEx.getInvalidMachinePortTransRsrvState(), fromMachineID, fromPortID, fromReticlePodPortInfo.getTransferReserveStatus());
        }

        if (fromMachineType == 2) {
            equipmentMethod.equipmentCheckAvail(objCommon, fromMachineID);
        }

        /*--------------------------------*/
        /*   Check to Machine condition   */
        /*--------------------------------*/
        String toMachineCategory = null;
        int toMachineType = 0;
        Outputs.ObjMachineTypeGetOut toMachineTypeGetOut = equipmentMethod.machineTypeGet(objCommon, toMachineID);

        if (!toMachineTypeGetOut.isBStorageMachineFlag()) {
            log.debug("toMachine type is Equipment");
            toMachineType = 1;
            toMachineCategory = BizConstant.SP_MACHINE_TYPE_EQP;
        } else {
            if (CimStringUtils.equals(toMachineTypeGetOut.getStockerType(), BizConstant.SP_STOCKER_TYPE_BARERETICLE)) {
                log.debug("toMachine type is BareReticleStocker");
                toMachineType = 2;
                toMachineCategory = BizConstant.SP_STOCKER_TYPE_BARERETICLE;
            } else if (CimStringUtils.equals(toMachineTypeGetOut.getStockerType(), BizConstant.SP_STOCKER_TYPE_RETICLEPOD)) {
                log.debug("toMachine type is ReticlePodStocker");
                toMachineType = 3;
                toMachineCategory = BizConstant.SP_STOCKER_TYPE_RETICLEPOD;
            } else {
                log.debug("machine type is invalid.");
                Validations.check(retCodeConfigEx.getRtclPodDestInvalid(), reticlePodID, toMachineID);
            }
        }

        Infos.ReticlePodPortInfo toReticlePodPortInfo = null;
        if (toMachineType == 1) {
            Outputs.ObjEquipmentReticlePodPortInfoGetDROut objEquipmentReticlePodPortInfoGetDROut = equipmentMethod.equipmentReticlePodPortInfoGetDR(objCommon, toMachineID);
            List<Infos.ReticlePodPortInfo> reticlePodPortInfoList = objEquipmentReticlePodPortInfoGetDROut.getReticlePodPortInfoList();
            boolean bFoundPortFlag = false;
            for (Infos.ReticlePodPortInfo reticlePodPortInfo : reticlePodPortInfoList) {
                if (ObjectIdentifier.equalsWithValue(reticlePodPortInfo.getReticlePodPortID(), toPortID)) {
                    log.debug("Found requested reticlePodPort.");
                    toReticlePodPortInfo = reticlePodPortInfo;
                    bFoundPortFlag = true;
                    break;
                }
            }
            Validations.check(!bFoundPortFlag, retCodeConfig.getRspportNotFound(), toMachineID, toPortID);
        }

        if (toMachineType == 2) {
            List<Infos.ReticlePodPortInfo> reticlePodPortInfos = stockerMethod.stockerReticlePodPortInfoGetDR(objCommon, toMachineID);
            boolean bFoundPortFlag = false;
            for (Infos.ReticlePodPortInfo reticlePodPortInfo : reticlePodPortInfos) {
                if (ObjectIdentifier.equalsWithValue(reticlePodPortInfo.getReticlePodPortID(), toPortID)) {
                    log.debug("Found requested reticlePodPort.");
                    toReticlePodPortInfo = reticlePodPortInfo;
                    bFoundPortFlag = true;
                    break;
                }
            }
            Validations.check(!bFoundPortFlag, retCodeConfig.getRspportNotFound(), toMachineID, toPortID);
        }

        if (toMachineType == 1 || toMachineType == 2) {
            Validations.check(!CimStringUtils.equals(toReticlePodPortInfo.getAccessMode(), BizConstant.SP_EQP_ACCESSMODE_AUTO), retCodeConfigEx.getEqpPortAccessMode(), toMachineID, toPortID, toReticlePodPortInfo.getAccessMode());
            Validations.check(!CimStringUtils.equals(toReticlePodPortInfo.getPortStatus(), BizConstant.SP_PORTRSC_PORTSTATE_LOADREQ), retCodeConfigEx.getInvalidRspPortStatus(), toMachineID, toPortID);
            Validations.check(!ObjectIdentifier.isEmpty(toReticlePodPortInfo.getLoadedReticlePodID()), retCodeConfigEx.getDiffRtclpodLoaded(), toReticlePodPortInfo.getLoadedReticlePodID(), toMachineID, toPortID);
            Validations.check(!CimStringUtils.equals(toReticlePodPortInfo.getDispatchStatus(), BizConstant.SP_PORTRSC_DISPATCHSTATE_REQUIRED), retCodeConfigEx.getInvalidMachinePortDispatchState(), toMachineID, toPortID, toReticlePodPortInfo.getDispatchStatus());
            Validations.check(!CimStringUtils.equals(toReticlePodPortInfo.getTransferReserveStatus(), BizConstant.SP_RETICLEPODPORT_NOTRESERVED), retCodeConfigEx.getInvalidMachinePortTransRsrvState(), toMachineID, toPortID, toReticlePodPortInfo.getTransferReserveStatus());
        }

        if (toMachineType == 2 || toMachineType == 3) {
            equipmentMethod.equipmentCheckAvail(objCommon, toMachineID);
        }

        /*---------------------*/
        /*   Insert to FSRAL   */
        /*---------------------*/
        log.debug("Insert to OSRTCLACTLIST ");
        List<Infos.ReticleDispatchJob> strReticleDispatchJobList = new ArrayList<>();
        Infos.ReticleDispatchJob reticleDispatchJob = new Infos.ReticleDispatchJob();
        strReticleDispatchJobList.add(reticleDispatchJob);
        reticleDispatchJob.setRequestedTimestamp(objCommon.getTimeStamp().getReportTimeStamp().toString());
        if (bReticleExistFlag) {
            reticleDispatchJob.setReticleID(strMoveReticles.get(0).getReticleID());
        } else {
            reticleDispatchJob.setReticleID(new ObjectIdentifier(""));
        }
        reticleDispatchJob.setReticlePodID(reticlePodID);
        reticleDispatchJob.setReticleDispatchJobID(reticleMethod.timeStampGetDR());
        reticleDispatchJob.setFromEquipmentID(fromMachineID);
        reticleDispatchJob.setFromEquipmentCategory(fromMachineCategory);
        reticleDispatchJob.setToEquipmentID(toMachineID);
        reticleDispatchJob.setToEquipmentCategory(toMachineCategory);
        reticleDispatchJob.setPriority(1L);
        reticleDispatchJob.setRequestUserID(objCommon.getUser().getUserID());
        reticleDispatchJob.setJobStatus(BizConstant.SP_RDJ_STATUS_WAITTOEXECUTE);
        reticleDispatchJob.setJobStatusChangeTimestamp(objCommon.getTimeStamp().getReportTimeStamp().toString());
        reticleMethod.reticleDispatchJobInsertDR(objCommon, strReticleDispatchJobList);

        /*---------------------*/
        /*   Insert to FSREL   */
        /*---------------------*/
        log.debug("Insert to OSRTCLEXELIST");
        List<Infos.ReticleComponentJob> strReticleComponentJobList = new ArrayList<>();
        Infos.ReticleComponentJob reticleComponentJob1 = new Infos.ReticleComponentJob();
        strReticleComponentJobList.add(reticleComponentJob1);
        reticleComponentJob1.setRequestedTimestamp(objCommon.getTimeStamp().getReportTimeStamp().toString());
        reticleComponentJob1.setPriority(1L);
        reticleComponentJob1.setReticleDispatchJobID(strReticleDispatchJobList.get(0).getReticleDispatchJobID());
        reticleComponentJob1.setReticleComponentJobID(reticleMethod.timeStampGetDR());
        reticleComponentJob1.setReticleDispatchJobRequestUserID(strReticleDispatchJobList.get(0).getRequestUserID());
        reticleComponentJob1.setJobSeq(1L);
        reticleComponentJob1.setReticlePodID(reticlePodID);

        if (bReticleExistFlag) {
            reticleComponentJob1.setSlotNo(strMoveReticles.get(0).getSlotNumber().longValue());
            reticleComponentJob1.setReticleID(strMoveReticles.get(0).getReticleID());
        } else {
            reticleComponentJob1.setSlotNo(0L);
            reticleComponentJob1.setReticleID(new ObjectIdentifier(""));
        }

        reticleComponentJob1.setJobName(BizConstant.SP_RCJ_JOBNAME_UNCLAMP);
        reticleComponentJob1.setToEquipmentID(fromMachineID);
        reticleComponentJob1.setToReticlePodPortID(fromPortID);
        reticleComponentJob1.setToEquipmentCategory(fromMachineCategory);
        reticleComponentJob1.setFromEquipmentID(fromMachineID);
        reticleComponentJob1.setFromReticlePodPortID(fromPortID);
        reticleComponentJob1.setFromEquipmentCategory(fromMachineCategory);
        reticleComponentJob1.setJobStatus(BizConstant.SP_RCJ_STATUS_WAITTOEXECUTE);
        reticleComponentJob1.setJobStatusChangeTimestamp(objCommon.getTimeStamp().getReportTimeStamp().toString());

        Infos.ReticleComponentJob reticleComponentJob2 = new Infos.ReticleComponentJob();
        strReticleComponentJobList.add(reticleComponentJob2);
        reticleComponentJob2.setRequestedTimestamp(objCommon.getTimeStamp().getReportTimeStamp().toString());
        reticleComponentJob2.setPriority(1L);
        reticleComponentJob2.setReticleDispatchJobID(strReticleDispatchJobList.get(0).getReticleDispatchJobID());
        reticleComponentJob2.setReticleComponentJobID(reticleMethod.timeStampGetDR());
        reticleComponentJob2.setReticleDispatchJobRequestUserID(strReticleDispatchJobList.get(0).getRequestUserID());
        reticleComponentJob2.setJobSeq(2L);
        reticleComponentJob2.setReticlePodID(reticlePodID);

        if (bReticleExistFlag) {
            reticleComponentJob2.setSlotNo(strMoveReticles.get(0).getSlotNumber().longValue());
            reticleComponentJob2.setReticleID(strMoveReticles.get(0).getReticleID());
        } else {
            reticleComponentJob2.setSlotNo(0L);
            reticleComponentJob2.setReticleID(new ObjectIdentifier(""));
        }

        reticleComponentJob2.setJobName(BizConstant.SP_RCJ_JOBNAME_XFER);
        reticleComponentJob2.setToEquipmentID(toMachineID);
        reticleComponentJob2.setToReticlePodPortID(toPortID);
        reticleComponentJob2.setToEquipmentCategory(toMachineCategory);
        reticleComponentJob2.setFromEquipmentID(fromMachineID);
        reticleComponentJob2.setFromReticlePodPortID(fromPortID);
        reticleComponentJob2.setFromEquipmentCategory(fromMachineCategory);
        reticleComponentJob2.setJobStatus(BizConstant.SP_RCJ_STATUS_WAITTOEXECUTE);
        reticleComponentJob2.setJobStatusChangeTimestamp(objCommon.getTimeStamp().getReportTimeStamp().toString());
        reticleMethod.reticleComponentJobInsertDR(objCommon, strReticleComponentJobList);
        /*----------------*/
        /*   Event Make   */
        /*----------------*/
        log.debug("Event Make");
        Infos.ReticleEventRecord strReticleEventRecord = new Infos.ReticleEventRecord();
        strReticleEventRecord.setEventTime(objCommon.getTimeStamp().getReportTimeStamp().toString());
        strReticleEventRecord.setReticleDispatchJobID(strReticleDispatchJobList.get(0).getReticleDispatchJobID());
        strReticleEventRecord.setReticleComponentJobID(strReticleComponentJobList.get(0).getReticleComponentJobID());
        reticleMethod.reticleEventQueuePutDR(objCommon, strReticleEventRecord);
    }


    @Override
    public void sxReticlePodUnclampJobCreateReq(Infos.ObjCommon objCommon, Params.ReticlePodUnclampJobCreateReqParams params) {
        ObjectIdentifier machineID = params.getMachineID();
        ObjectIdentifier portID = params.getPortID();
        ObjectIdentifier reticlePodID = params.getReticlePodID();

        String tmpArhs = StandardProperties.OM_ARHS_FLAG.getValue();
        if (log.isDebugEnabled()){
            log.debug("ARHS switch on / off  tmpArhs = {}", tmpArhs);
        }
        Validations.check(!CimStringUtils.equals(BizConstant.SP_ARMS_SWITCH_ON, tmpArhs), retCodeConfig.getFunctionNotAvailable());

        /*-------------------------------*/
        /*   Check equipment condition   */
        /*-------------------------------*/
        log.debug("step1 - equipmentMethod.machineTypeGet");
        Outputs.ObjMachineTypeGetOut machineTypeGetOut = equipmentMethod.machineTypeGet(objCommon, machineID);

        List<Infos.ReticlePodPortInfo> strReticlePodPortInfoList = new ArrayList<>();
        if (CimBooleanUtils.isFalse(machineTypeGetOut.isBStorageMachineFlag())) {
            log.debug("TRUE != bStorageMachineFlag");
            log.debug("step2 - equipmentMethod.equipmentOnlineModeGet");
            String equipmentOnlineModeGet = equipmentMethod.equipmentOnlineModeGet(objCommon, machineID);

            if (!CimStringUtils.equals(equipmentOnlineModeGet, BizConstant.SP_EQP_ONLINEMODE_ONLINEREMOTE)) {
                log.error("onlineMode:{}", equipmentOnlineModeGet);
                Validations.check(true, retCodeConfig.getEqpOnlineMode(),
                        ObjectIdentifier.fetchValue(machineID),
                        equipmentOnlineModeGet);
            }
            log.debug("step3 - equipmentMethod.equipmentReticlePodPortInfoGetDR");
            Outputs.ObjEquipmentReticlePodPortInfoGetDROut equipmentReticlePodPortInfoGetDROut = equipmentMethod.equipmentReticlePodPortInfoGetDR(objCommon, machineID);
            if (null != equipmentReticlePodPortInfoGetDROut) {
                strReticlePodPortInfoList = equipmentReticlePodPortInfoGetDROut.getReticlePodPortInfoList();
            }
        } else if (CimStringUtils.equals(machineTypeGetOut.getStockerType(), BizConstant.SP_STOCKER_TYPE_BARERETICLE)) {
            log.debug("StockerType is BareReticle.");
            log.debug("step4 - stockerMethod.stockerOnlineModeGet");
            String stockerOnlineModeGet = stockerMethod.stockerOnlineModeGet(objCommon, machineID);

            if (!CimStringUtils.equals(stockerOnlineModeGet, BizConstant.SP_EQP_ONLINEMODE_ONLINEREMOTE)) {
                log.error("stockerOnlineMode is Online Mode {}", stockerOnlineModeGet);
                Validations.check(true, retCodeConfig.getEqpOnlineMode(),
                        ObjectIdentifier.fetchValue(machineID),
                        stockerOnlineModeGet);
            }

            log.debug("step5 - stockerMethod.stockerReticlePodPortInfoGetDR");
            List<Infos.ReticlePodPortInfo> reticlePodPortInfos = stockerMethod.stockerReticlePodPortInfoGetDR(objCommon, machineID);
            strReticlePodPortInfoList = reticlePodPortInfos;
        } else {
            log.error("in-para machineID is not equipment or bareReticleStocker");
            Validations.check(true, retCodeConfig.getInvalidParameter());
        }

        //Check if reticlePod is loaded on specified port of specified machine.
        int portCount = CimArrayUtils.getSize(strReticlePodPortInfoList);
        int i = 0;
        for (i = 0; i < portCount; i++) {
            if (ObjectIdentifier.equalsWithValue(strReticlePodPortInfoList.get(i).getReticlePodPortID(), portID)) {
                if (log.isDebugEnabled()){
                    log.debug("Port {} found on machine {} .", ObjectIdentifier.fetchValue(portID), ObjectIdentifier.fetchValue(machineID));
                }
                break;
            }
        }
        if (i == portCount) {
            log.error("Port {} not fo|und on this machine {}.", ObjectIdentifier.fetchValue(portID), ObjectIdentifier.fetchValue(machineID));
            Validations.check(true, retCodeConfig.getRspportNotFound(),
                    ObjectIdentifier.fetchValue(machineID),
                    ObjectIdentifier.fetchValue(portID));
        } else if (!ObjectIdentifier.equalsWithValue(strReticlePodPortInfoList.get(i).getLoadedReticlePodID(), reticlePodID)) {
            log.error("Specified reticlePod is not loaded on this port.",
                    ObjectIdentifier.fetchValue(portID),
                    ObjectIdentifier.fetchValue(machineID),
                    ObjectIdentifier.fetchValue(reticlePodID));
            Validations.check(true, retCodeConfigEx.getRtclpodNotLoaded(),
                    ObjectIdentifier.fetchValue(reticlePodID),
                    ObjectIdentifier.fetchValue(machineID),
                    ObjectIdentifier.fetchValue(portID));
        }

        /*----------------------------------------------*/
        /*   Confirm whether reticle is in reticlePod   */
        /*----------------------------------------------*/
        Params.ReticlePodDetailInfoInqParams reticlePodDetailInfoInqParams = new Params.ReticlePodDetailInfoInqParams();
        reticlePodDetailInfoInqParams.setReticlePodID(reticlePodID);
        reticlePodDetailInfoInqParams.setDurableWipOperationInfoFlag(false);
        reticlePodDetailInfoInqParams.setDurableOperationInfoFlag(false);
        log.debug("step6 - reticleMethod.reticlePodFillInTxPDQ013DR");
        Outputs.ObjReticlePodFillInTxPDQ013DROut reticlePodFillInTxPDQ013DROut = reticleMethod.reticlePodFillInTxPDQ013DR(objCommon, reticlePodDetailInfoInqParams);

        Boolean bReticleExistFlag = false;
        if (null != reticlePodFillInTxPDQ013DROut && null != reticlePodFillInTxPDQ013DROut.getReticlePodStatusInfo() && CimArrayUtils.isNotEmpty(reticlePodFillInTxPDQ013DROut.getReticlePodStatusInfo().getStrContainedReticleInfo())) {
            log.debug("This reticlePod have reticle(s).");
            bReticleExistFlag = true;
        }

        if (CimBooleanUtils.isTrue(bReticleExistFlag)) {
            log.debug("bReticleExistFlag = TRUE");
            for (Infos.ContainedReticleInfo containedReticleInfo : reticlePodFillInTxPDQ013DROut.getReticlePodStatusInfo().getStrContainedReticleInfo()) {
                if (log.isDebugEnabled()){
                    log.debug("durableID: {}", ObjectIdentifier.fetchValue(containedReticleInfo.getReticleID()));
                }
                log.debug("step7 - durableMethod.durableDurableControlJobIDGet");
                ObjectIdentifier durableConrolJobID = durableMethod.durableDurableControlJobIDGet(objCommon, containedReticleInfo.getReticleID(), BizConstant.SP_DURABLECAT_RETICLE);

                if (ObjectIdentifier.isNotEmptyWithValue(durableConrolJobID)) {
                    if (log.isDebugEnabled()){
                        log.debug("durableControlJobID {} is not blank", ObjectIdentifier.fetchValue(durableConrolJobID));
                    }
                    objectLockMethod.objectLock(objCommon, CimDurableControlJob.class, durableConrolJobID);

                    log.debug("step8 - durableMethod.durableControlJobStatusGet");
                    Infos.DurableControlJobStatusGet durableControlJobStatusGet = durableMethod.durableControlJobStatusGet(objCommon, durableConrolJobID);
                    if (null != durableControlJobStatusGet) {
                        if (!CimStringUtils.equals(durableControlJobStatusGet.getDurableControlJobStatus(), BizConstant.SP_DURABLECONTROLJOBSTATUS_CREATED) &&
                                !CimStringUtils.equals(durableControlJobStatusGet.getDurableControlJobStatus(), BizConstant.SP_DURABLECONTROLJOBSTATUS_DELETE)) {
                            Validations.check(true, retCodeConfig.getInvalidDcjstatus(), durableControlJobStatusGet.getDurableControlJobStatus());
                        }
                    }
                }
            }
        } else {
            log.debug("bReticleExistFlag = FALSE");
            log.debug("step9 - durableMethod.durableDurableControlJobIDGet");
            ObjectIdentifier durableControlJobID = durableMethod.durableDurableControlJobIDGet(objCommon, reticlePodID, BizConstant.SP_DURABLECAT_RETICLEPOD);


            if (ObjectIdentifier.isNotEmptyWithValue(durableControlJobID)) {
                if (log.isDebugEnabled()){
                    log.debug("durableControlJobID {} is not blank", ObjectIdentifier.fetchValue(durableControlJobID));
                }

                log.debug("step10 - objectLockMethod.objectLock");
                objectLockMethod.objectLock(objCommon, CimDurableControlJob.class, durableControlJobID);

                log.debug("step11 - durableMethod.durableControlJobStatusGet");
                Infos.DurableControlJobStatusGet durableControlJobStatusGet = durableMethod.durableControlJobStatusGet(objCommon, durableControlJobID);

                if (null != durableControlJobStatusGet) {
                    if (!CimStringUtils.equals(durableControlJobStatusGet.getDurableControlJobStatus(), BizConstant.SP_DURABLECONTROLJOBSTATUS_CREATED) &&
                            !CimStringUtils.equals(durableControlJobStatusGet.getDurableControlJobStatus(), BizConstant.SP_DURABLECONTROLJOBSTATUS_DELETE)) {
                        Validations.check(true, retCodeConfig.getInvalidDcjstatus(), durableControlJobStatusGet.getDurableControlJobStatus());
                    }
                }
            }
        }

        List<Infos.MoveReticles> strMoveReticles = new ArrayList<>();
        if (CimBooleanUtils.isTrue(bReticleExistFlag)) {
            for (Infos.ContainedReticleInfo containedReticleInfo : reticlePodFillInTxPDQ013DROut.getReticlePodStatusInfo().getStrContainedReticleInfo()) {
                Infos.MoveReticles moveReticles = new Infos.MoveReticles();
                strMoveReticles.add(moveReticles);
                moveReticles.setReticleID(containedReticleInfo.getReticleID());
                moveReticles.setSlotNumber((int) containedReticleInfo.getSlotNo());

                /*-------------------------------*/
                /*   Check reticles controlJob   */
                /*-------------------------------*/
                log.debug("step12 - reticleMethod.reticlecontrolJobInfoGet");
                Outputs.ReticleControlJobInfoGetOut reticleControlJobInfoGetOut = reticleMethod.reticlecontrolJobInfoGet(objCommon, containedReticleInfo.getReticleID());

                int rtclCtrlJobLen = null == reticleControlJobInfoGetOut ? 0 : CimArrayUtils.getSize(reticleControlJobInfoGetOut.getStrControlJobAttributeInfoSeq());
                if (log.isDebugEnabled()){
                    log.debug("reticle's controljob count {}", rtclCtrlJobLen);
                }

                if (rtclCtrlJobLen > 0) {
                    //--------------------------------------------------
                    //   Get OM_RETICLE_RETRIEVE_WHEN_LOT_PROCESS
                    //--------------------------------------------------
                    Long retrieveReticleDuringLotProcFlag = StandardProperties.OM_RETICLE_RETRIEVE_WHEN_LOT_PROCESS.getLongValue();
                    log.debug("Env OM_RETICLE_RETRIEVE_WHEN_LOT_PROCESS ", retrieveReticleDuringLotProcFlag);
                    if (1 == retrieveReticleDuringLotProcFlag) {
                        log.debug("Check if reticle has reserve control job");
                        for (Outputs.ControlJobAttributeInfo controlJobAttributeInfo : reticleControlJobInfoGetOut.getStrControlJobAttributeInfoSeq()) {
                            if (CimStringUtils.equals(BizConstant.SP_CONTROLJOBSTATUS_CREATED, controlJobAttributeInfo.getControlJobStatus())) {
                                log.error("controlJob status is Created");
                                Validations.check(true, retCodeConfigEx.getRtclHasCtrljob(),
                                        ObjectIdentifier.fetchValue(containedReticleInfo.getReticleID()));
                            }
                        }
                    } else {
                        log.error("This reticle {}  have controlJob.", ObjectIdentifier.fetchValue(containedReticleInfo.getReticleID()));
                        Validations.check(true, retCodeConfigEx.getRtclHasCtrljob(),
                                ObjectIdentifier.fetchValue(containedReticleInfo.getReticleID()));
                    }
                }
            }
        }
        ObjectIdentifier reticleID = null;
        int slotNumber = 0;
        if (CimBooleanUtils.isTrue(bReticleExistFlag)) {
            reticleID = strMoveReticles.get(0).getReticleID();
            slotNumber = strMoveReticles.get(0).getSlotNumber();
        }
        /*---------------------------------------------------------------------------------*/
        /*   if requested reticle/reticlePod is not in RDJ/RCJ List, Create RDJ/RCJ List   */
        /*---------------------------------------------------------------------------------*/
        log.error("step13 - reticleMethod.reticleArhsJobCreate");
        Outputs.ReticleArhsJobCreateOut reticleArhsJobCreateOut = reticleMethod.reticleArhsJobCreate(objCommon,
                reticleID,
                slotNumber,
                reticlePodID,
                machineID,                   //from machine
                portID,                      //from machine
                machineID,
                portID,
                BizConstant.SP_RCJ_JOBNAME_UNCLAMP);


        /*----------------*/
        /*   Event Make   */
        /*----------------*/
        Infos.ReticleEventRecord strReticleEventRecord = new Infos.ReticleEventRecord();
        strReticleEventRecord.setEventTime(CimDateUtils.convertToSpecString(objCommon.getTimeStamp().getReportTimeStamp()));
        strReticleEventRecord.setReticleDispatchJobID(reticleArhsJobCreateOut.getReticleDispatchJobID());
        strReticleEventRecord.setReticleComponentJobID(reticleArhsJobCreateOut.getReticleComponentJobID());

        log.debug("step14 - reticleMethod.reticleEventQueuePutDR");
        reticleMethod.reticleEventQueuePutDR(objCommon, strReticleEventRecord);
    }

    @Override
    public void sxReticlePodXferJobDeleteReq(Infos.ObjCommon objCommon, Params.ReticlePodXferJobDeleteReqParams params) {
        String jobID = params.getJobID();
        List<Infos.ReticlePodJob> strReticlePodJob = params.getReticlePodJobList();

        String tmpArhs = StandardProperties.OM_ARHS_FLAG.getValue();
        if (log.isDebugEnabled())
            log.debug("ARHS switch on / off  tmpArhs = {}", tmpArhs);
        Validations.check(!CimStringUtils.equals(BizConstant.SP_ARMS_SWITCH_ON, tmpArhs), retCodeConfig.getFunctionNotAvailable());

        //=========================================================================
        // Cancel transfer job => TMS(R)
        //=========================================================================
        Inputs.SendTransportJobCancelReqIn sendTransportJobCancelReqIn = new Inputs.SendTransportJobCancelReqIn();
        Infos.TranJobCancelReq tranJobCancelReq = new Infos.TranJobCancelReq();

        tranJobCancelReq.setJobID(jobID);
        List<Infos.CarrierJob> carrierJobData = new ArrayList<>();
        tranJobCancelReq.setCarrierJobData(carrierJobData);

        if (CimArrayUtils.isNotEmpty(strReticlePodJob)) {
            for (Infos.ReticlePodJob reticlePodJob : strReticlePodJob) {
                Infos.CarrierJob carrierJob = new Infos.CarrierJob();
                carrierJobData.add(carrierJob);
                carrierJob.setCarrierJobID(reticlePodJob.getReticlePodJobID());
                carrierJob.setCarrierID(reticlePodJob.getReticlePodID());
            }
        }
        sendTransportJobCancelReqIn.setStrObjCommonIn(objCommon);
        sendTransportJobCancelReqIn.setTranJobCancelReq(tranJobCancelReq);
        sendTransportJobCancelReqIn.setUser(objCommon.getUser());
        log.debug("step1 - tmsService.rtransportJobCancelReq");
        Outputs.SendTransportJobCancelReqOut sendTransportJobCancelReqOut = tmsService.rtransportJobCancelReq(sendTransportJobCancelReqIn);

        if (CimArrayUtils.isNotEmpty(strReticlePodJob)) {
            for (Infos.ReticlePodJob reticlePodJob : strReticlePodJob) {
                //=========================================================================
                // Get current reticle pod status
                //=========================================================================
                Params.ReticlePodDetailInfoInqParams reticlePodDetailInfoInqParams = new Params.ReticlePodDetailInfoInqParams();
                reticlePodDetailInfoInqParams.setReticlePodID(reticlePodJob.getReticlePodID());
                reticlePodDetailInfoInqParams.setDurableWipOperationInfoFlag(false);
                reticlePodDetailInfoInqParams.setDurableOperationInfoFlag(false);
                log.debug("step2 - durableInqService.sxReticlePodDetailInfoInq");
                Results.ReticlePodDetailInfoInqResult reticlePodDetailInfoInqResult = durableInqService.sxReticlePodDetailInfoInq(objCommon, reticlePodDetailInfoInqParams);

                //================================================
                // Get machineType for BareReticleStocker
                //================================================
                log.debug("step3 - equipmentMethod.machineTypeGet");
                Outputs.ObjMachineTypeGetOut machineTypeGetOut = equipmentMethod.machineTypeGet(objCommon, reticlePodJob.getFromMachineID());

                List<Infos.ReticlePodPortInfo> strReticlePodPortInfo = null;
                if (CimBooleanUtils.isFalse(machineTypeGetOut.isBStorageMachineFlag())) {
                    if (log.isDebugEnabled())
                        log.debug("This machine {} is EQP", ObjectIdentifier.fetchValue(reticlePodJob.getFromMachineID()));
                    log.debug("step4 - equipmentMethod.equipmentReticlePodPortInfoGetDR");
                    Outputs.ObjEquipmentReticlePodPortInfoGetDROut equipmentReticlePodPortInfoGetDROut = equipmentMethod.equipmentReticlePodPortInfoGetDR(objCommon, reticlePodJob.getFromMachineID());
                    if (null != equipmentReticlePodPortInfoGetDROut) {
                        strReticlePodPortInfo = equipmentReticlePodPortInfoGetDROut.getReticlePodPortInfoList();
                    }
                } else if (CimStringUtils.equals(machineTypeGetOut.getStockerType(), BizConstant.SP_STOCKER_TYPE_BARERETICLE)) {
                    if (log.isDebugEnabled())
                        log.debug("This machine {} is BRS", ObjectIdentifier.fetchValue(reticlePodJob.getFromMachineID()));
                    log.debug("step5 - stockerMethod.stockerReticlePodPortInfoGetDR");
                    List<Infos.ReticlePodPortInfo> reticlePodPortInfos = stockerMethod.stockerReticlePodPortInfoGetDR(objCommon, reticlePodJob.getFromMachineID());
                    strReticlePodPortInfo = reticlePodPortInfos;
                } else {
                    if (log.isDebugEnabled())
                        log.debug("This machine {} is other stocker", ObjectIdentifier.fetchValue(reticlePodJob.getFromMachineID()));
                    strReticlePodPortInfo = new ArrayList<>();
                }

                //=========================================================================
                // If reticle pod is on equipment port (of from equipment)
                //=========================================================================
                if (log.isDebugEnabled()){
                    log.debug("transferState {}", reticlePodDetailInfoInqResult.getReticlePodStatusInfo().getTransferStatus());
                    log.debug("(if from machine is stocker...) stockerType {}",
                            machineTypeGetOut.getStockerType());
                }
                if (CimStringUtils.equals(reticlePodDetailInfoInqResult.getReticlePodStatusInfo().getTransferStatus(), BizConstant.SP_TRANSSTATE_EQUIPMENTIN) ||
                        (CimStringUtils.equals(machineTypeGetOut.getStockerType(), BizConstant.SP_STOCKER_TYPE_BARERETICLE) &&
                                CimStringUtils.equals(reticlePodDetailInfoInqResult.getReticlePodStatusInfo().getTransferStatus(), BizConstant.SP_TRANSSTATE_STATIONIN))) {
                    log.debug("TransferState is EI or SI(BRS).");

                    Boolean bNotReleaseFlag = false;
                    if (CimArrayUtils.isNotEmpty(strReticlePodPortInfo)) {
                        for (Infos.ReticlePodPortInfo reticlePodPortInfo : strReticlePodPortInfo) {
                            if (ObjectIdentifier.equalsWithValue(reticlePodJob.getFromPortID(), reticlePodPortInfo.getReticlePodPortID())
                                    && !ObjectIdentifier.equalsWithValue(reticlePodJob.getReticlePodID(), reticlePodPortInfo.getReservedReticlePodID())) {
                                if (log.isDebugEnabled())
                                    log.debug("From Port {} is already reserved for other reticlePod.", ObjectIdentifier.fetchValue(reticlePodPortInfo.getReservedReticlePodID()));
                                bNotReleaseFlag = true;
                                break;
                            }
                        }
                    }

                    if (CimBooleanUtils.isFalse(bNotReleaseFlag)) {
                        log.debug("TRUE != bNotReleaseFlag");
                        log.debug("step6 - equipmentForDurableMethod.machineReticlePodPortReserveCancel");
                        equipmentForDurableMethod.machineReticlePodPortReserveCancel(objCommon, reticlePodJob.getFromMachineID(), reticlePodJob.getFromPortID());
                    }
                }

                //=========================================================================
                // Cancel destination equipment reservation ( for strReticlePodJob[].toMachine )
                //=========================================================================
                log.debug("step7 - equipmentMethod.machineTypeGet");
                Outputs.ObjMachineTypeGetOut toMachineTypeGetOut = equipmentMethod.machineTypeGet(objCommon, reticlePodJob.getToMachineID());

                if (!CimStringUtils.equals(toMachineTypeGetOut.getStockerType(), BizConstant.SP_STOCKER_TYPE_RETICLEPOD)) {
                    log.debug("To port reserve cancel for Eqp and BareReticleStocker");
                    log.debug("step8 - equipmentForDurableMethod.machineReticlePodPortReserveCancel");
                    equipmentForDurableMethod.machineReticlePodPortReserveCancel(objCommon, reticlePodJob.getToMachineID(), reticlePodJob.getToPortID());
                }

                //=========================================================================
                // Cancel reticle pod reservation
                //=========================================================================
                log.debug("step9 - reticleMethod.reticlePodTransferReserveCancel");
                reticleMethod.reticlePodTransferReserveCancel(objCommon, reticlePodJob.getReticlePodID());
            }
        }
    }
}
