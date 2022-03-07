package com.fa.cim.service.cjpj.impl;

import com.fa.cim.annotaion.OmService;
import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.OmCode;
import com.fa.cim.common.utils.*;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.config.RetCodeConfigEx;
import com.fa.cim.dto.*;
import com.fa.cim.method.*;
import com.fa.cim.newcore.bo.product.CimLot;
import com.fa.cim.newcore.impl.bo.env.StandardProperties;
import com.fa.cim.remote.IEAPRemoteManager;
import com.fa.cim.service.cjpj.IControlJobProcessJobService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.stream.Collectors;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/9/8        ********             Bear               create file
 *
 * @author: LiaoYunChuan
 * @date: 2020/9/8 17:38
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@OmService
public class ControlJobProcessJobServiceImpl implements IControlJobProcessJobService {
    @Autowired
    private IEquipmentMethod equipmentMethod;

    @Autowired
    private RetCodeConfig retCodeConfig;

    @Autowired
    private RetCodeConfigEx retCodeConfigEx;

    @Autowired
    private IObjectLockMethod objectLockMethod;

    @Autowired
    private IControlJobMethod controlJobMethod;

    @Autowired
    private IBondingGroupMethod bondingGroupMethod;

    @Autowired
    private IEventMethod eventMethod;

    @Autowired
    private IPortMethod portMethod;

    @Autowired
    private ITCSMethod tcsMethod;

    @Autowired
    private IProcessMethod processMethod;

    @Autowired
    private IQTimeMethod qTimeMethod;

    @Autowired
    private IMessageMethod messageMethod;

    @Autowired
    private IEquipmentContainerPositionMethod equipmentContainerPositionMethod;

    @Autowired
    private IEAPMethod eapMethod;

    @Override
    public void sxProcessJobMapInfoRpt(Infos.ObjCommon objCommon, Infos.ProcessJobMapInfoRptInParm processJobMapInfoRptInParm) {
        ObjectIdentifier equipmentID = processJobMapInfoRptInParm.getEquipmentID();
        ObjectIdentifier controlJobID = processJobMapInfoRptInParm.getControlJobID();
        List<Infos.ProcessJobMapInfo> strProcessJobMapInfoSeq = processJobMapInfoRptInParm.getProcessJobMapInfoList();
        //----------------------------------------------------------------
        //
        //  Pre Process
        //
        //----------------------------------------------------------------

        //----------------------------------------------------------------
        //  object_Lock for Equipment Container Position by ControlJob
        //----------------------------------------------------------------
        log.info("【step1】object_LockForEquipmentContainerPosition");
        Inputs.ObjObjectLockForEquipmentContainerPositionIn objObjectLockForEquipmentContainerPositionIn = new Inputs.ObjObjectLockForEquipmentContainerPositionIn();
        objObjectLockForEquipmentContainerPositionIn.setEquipmentID(processJobMapInfoRptInParm.getEquipmentID());
        objObjectLockForEquipmentContainerPositionIn.setControlJobID(processJobMapInfoRptInParm.getControlJobID());
        objectLockMethod.objectLockForEquipmentContainerPosition(objCommon, objObjectLockForEquipmentContainerPositionIn);

        //----------------------------------------------------------------
        //  Get SLM Capability of Equipment BR info
        //----------------------------------------------------------------
        log.info("【step2】Get SLM Capability of Equipment BR info");
        Infos.EqpBrInfo equipmentBRInfo = equipmentMethod.equipmentBRInfoGetDR(objCommon, equipmentID);
        // Check SLM Capability
        Validations.check(!equipmentBRInfo.isFmcCapabilityFlag(), new OmCode(retCodeConfigEx.getEqpSlmCapabilityOff(), equipmentID.getValue()));
        //----------------------------------------------------------------
        //  Get ControlJob of Equipment
        //----------------------------------------------------------------
        log.info("【step3】Get ControlJob of Equipment");
        Infos.EqpInprocessingControlJobInfo eqpInprocessingControlJobInfo = equipmentMethod.equipmentInprocessingControlJobInfoGet(objCommon, equipmentID);
        // Check combination of control job / equipment
        boolean bFoundCJ = false;
        List<Infos.EqpInprocessingControlJob> strEqpInprocessingControlJob = eqpInprocessingControlJobInfo.getStrEqpInprocessingControlJob();
        int nCJLen = CimArrayUtils.getSize(strEqpInprocessingControlJob);
        for (int i = 0; i < nCJLen; i++) {
            Infos.EqpInprocessingControlJob eqpInprocessingControlJob = strEqpInprocessingControlJob.get(i);
            if (ObjectIdentifier.equalsWithValue(controlJobID, eqpInprocessingControlJob.getControlJobID())) {
                bFoundCJ = true;
                break;
            }
        }
        Validations.check(!bFoundCJ, new OmCode(retCodeConfig.getControlJobEqpUnmatch(), controlJobID.getValue(), equipmentID.getValue()));
        //----------------------------------------------------------------
        //  Get Equipment Container Position info by ControlJob
        //----------------------------------------------------------------
        log.info("【step4】Get Equipment Container Position info by ControlJob");
        Infos.EqpContainerPositionInfo eqpContainerPositionInfo = equipmentMethod.equipmentContainerPositionInfoGet(objCommon, equipmentID, controlJobID, BizConstant.SP_SLM_KEYCATEGORY_CONTROLJOB);
        // Check combination of control job / wafer in equipment container
        List<Infos.EqpContainerPosition> eqpContainerPositionList = eqpContainerPositionInfo.getEqpContainerPositionList();
        int nCtnPstLen = CimArrayUtils.getSize(eqpContainerPositionList);
        Validations.check(nCtnPstLen == 0, retCodeConfig.getNotFoundEquipmentContainerPosition());
        for (Infos.EqpContainerPosition eqpContainerPosition : eqpContainerPositionList) {
            boolean bFoundWafer = false;
            if (!CimArrayUtils.isEmpty(strProcessJobMapInfoSeq)) {
                for (Infos.ProcessJobMapInfo processJobMapInfo : strProcessJobMapInfoSeq) {
                    List<ObjectIdentifier> waferList = processJobMapInfo.getWaferSeq();
                    if (!CimArrayUtils.isEmpty(waferList)) {
                        for (ObjectIdentifier waferID : waferList) {
                            if (ObjectIdentifier.equalsWithValue(eqpContainerPosition.getWaferID(), waferID)) {
                                bFoundWafer = true;
                                break;
                            }
                        }
                    }
                    if (bFoundWafer) {
                        break;
                    }
                }
            }
            Validations.check(!bFoundWafer, new OmCode(retCodeConfig.getCtrljobEqpctnpstUnmatch(), controlJobID.getValue(), eqpContainerPosition.getWaferID().getValue()));
        }
        //----------------------------------------------------------------
        //
        //  Main Process
        //
        //----------------------------------------------------------------

        //----------------------------------------------------------------
        //  Set ProcessJob to each Equipment Container Position
        //----------------------------------------------------------------
        log.info("【step5】Set ProcessJob to each Equipment Container Position");
        equipmentMethod.equipmentContainerPositionProcessJobSet(objCommon, equipmentID, strProcessJobMapInfoSeq);
    }

    @Override
    public void sxPJInfoRpt(Infos.ObjCommon objCommon, Params.PJInfoRptParams params) {
        ObjectIdentifier equipmentID = params.getEquipmentID();
        ObjectIdentifier controlJobID = params.getControlJobID();
        List<Infos.ProcessJob> strProcessJobList = params.getProcessJobList();
        //------------------------------------------------------
        // Check Control Job
        //------------------------------------------------------
        log.info("【step1】: Check Control Job");
        // The specified control job should be existing
        Outputs.ObjControlJobStatusGetOut controlJobStatusGet = controlJobMethod.controlJobStatusGet(objCommon, controlJobID);

        //------------------------------------------------------
        // Check Equipment
        //------------------------------------------------------
        log.info("【step2】: Check Equipment");
        Infos.EqpBrInfo equipmentBRInfo = equipmentMethod.equipmentBRInfoGetDR(objCommon, equipmentID);

        //distinct
        List<ObjectIdentifier> reportedLotIDs = strProcessJobList.stream().map(Infos.ProcessJob::getProcessWaferList).flatMap(Collection::stream).map(Infos.ProcessWafer::getLotID).distinct().collect(Collectors.toList());

        log.info("【step3】: object_Lock");
        for (int nLocks = 0; nLocks < CimArrayUtils.getSize(reportedLotIDs); nLocks++) {
            log.info("Lock the Lot : {}", reportedLotIDs.get(nLocks));
            objectLockMethod.objectLock(objCommon, CimLot.class, reportedLotIDs.get(nLocks));
        }

        log.info("【step4】: Get lotIDs from controlJobID.");
        List<Infos.ControlJobCassette> controlJobCassettes = controlJobMethod.controlJobContainedLotGet(objCommon, controlJobID);

        //----------------------------------------------------------------
        // Bonding Map Info Get
        //----------------------------------------------------------------
        log.info("【step5】: Bonding Map Info Get.");
        Outputs.ObjBondingGroupInfoByEqpGetDROut bondingGroupInfoByEqpGetDR = bondingGroupMethod.bondingGroupInfoByEqpGetDR(objCommon, equipmentID, controlJobID, true);

        List<ObjectIdentifier> topLotIDSeq = bondingGroupInfoByEqpGetDR.getTopLotIDSeq();
        //Check reported lotID should be the same as control job contained start lot
        for (Infos.ControlJobCassette controlJobCassette : controlJobCassettes) {
            List<Infos.ControlJobLot> strControlJobLotSeq = controlJobCassette.getControlJobLotList();
            for (Infos.ControlJobLot controlJobLot : strControlJobLotSeq) {
                if (CimBooleanUtils.isFalse(controlJobLot.getOperationStartFlag())) {
                    continue;
                }
                if (topLotIDSeq.stream().anyMatch(topLotID -> ObjectIdentifier.equalsWithValue(controlJobLot.getLotID(), topLotID))) {
                    continue;
                }
                Validations.check(reportedLotIDs.stream().noneMatch(reportedLotID -> ObjectIdentifier.equalsWithValue(controlJobLot.getLotID(), reportedLotID)),
                        new OmCode(retCodeConfig.getInvalidParameterWithMsg(), "Incompleted lot information is reported"));
            }
        }
        //Save the reported waferIDs to the respective lot PO.
        //If the reported wafers are sampled wafers, the sample wafer flag should stay true.
        log.info("【step6】: set controlJob process wafers");
        controlJobMethod.controlJobProcessWafersSet(objCommon, controlJobID, strProcessJobList);

        // For Process Job Level Control Equipment
        if (CimBooleanUtils.isTrue(equipmentBRInfo.isProcessJobLevelCtrl())) {
            log.info("TRUE == strEquipment_brInfo_GetDR_out.equipmentBRInfo.processJobLevelCtrl");
            // processJobChangeEvent_Make
            // Create PosProcessJobChangeEvent Record for each process job.
            //     a.  Operation Category is "ProcessJobCreate"
            //     b.  waferIDs is set as reported
            //     c.  processStartFlag should be set as reported
            for (Infos.ProcessJob processJob : strProcessJobList) {
                log.info("【step7】: create event");
                // Prepare event data
                Inputs.ProcessJobChangeEventMakeParams processJobChangeEventMakeParams = new Inputs.ProcessJobChangeEventMakeParams();
                processJobChangeEventMakeParams.setControlJobID(controlJobID);
                processJobChangeEventMakeParams.setProcessJobID(processJob.getProcessJobID());
                processJobChangeEventMakeParams.setOpeCategory(BizConstant.SP_PROCESSJOBOPECATEGORY_CREATED);
                processJobChangeEventMakeParams.setProcessStart(processJob.getProcessStartFlag() ? BizConstant.SP_PROCESSJOBSTART_YES : BizConstant.SP_PROCESSJOBSTART_NO);
                processJobChangeEventMakeParams.setCurrentState(BizConstant.SP_PROCESSJOBSTATUS_QUEUED);
                processJobChangeEventMakeParams.setClaimMemo(params.getOpeMemo());
                processJobChangeEventMakeParams.setProcessWaferList(processJob.getProcessWaferList());
                processJobChangeEventMakeParams.setProcessJobChangeRecipeParameterList(new ArrayList<>());
                eventMethod.processJobChangeEventMake(objCommon, processJobChangeEventMakeParams);
            }
        }

        // For SLM Capability Equipment
        if (CimBooleanUtils.isTrue(equipmentBRInfo.isFmcCapabilityFlag())) {
            log.info("TRUE == strEquipment_brInfo_GetDR_out.equipmentBRInfo.SLMCapabilityFlag");
            //----------------------------------------------------------------
            //  object_Lock for Equipment Container Position by ControlJob
            //----------------------------------------------------------------
            log.info("【step9】: object_Lock for Equipment Container Position by ControlJob");
            Inputs.ObjObjectLockForEquipmentContainerPositionIn objObjectLockForEquipmentContainerPositionIn = new Inputs.ObjObjectLockForEquipmentContainerPositionIn();
            objObjectLockForEquipmentContainerPositionIn.setEquipmentID(equipmentID);
            objObjectLockForEquipmentContainerPositionIn.setControlJobID(controlJobID);
            objectLockMethod.objectLockForEquipmentContainerPosition(objCommon, objObjectLockForEquipmentContainerPositionIn);
            //----------------------------------------------------------------
            //  Get ControlJob of Equipment
            //----------------------------------------------------------------
            log.info("【step10】: Get ControlJob of Equipment");
            List<Infos.EqpInprocessingControlJob> eqpInprocessingControlJobs = equipmentMethod.equipmentInprocessingControlJobInfoGetDR(objCommon, equipmentID);
            // Check combination of control job / equipment
            Validations.check(eqpInprocessingControlJobs.stream().noneMatch(x -> ObjectIdentifier.equalsWithValue(controlJobID, x.getControlJobID())),
                    new OmCode(retCodeConfig.getControlJobEqpUnmatch(), ObjectIdentifier.fetchValue(controlJobID), ObjectIdentifier.fetchValue(equipmentID)));

            //----------------------------------------------------------------
            //  Get Equipment Container Position info by ControlJob
            //----------------------------------------------------------------
            log.info("【step11】: Get Equipment Container Position info by ControlJob");
            Infos.EqpContainerPositionInfo eqpContainerPositionInfo = equipmentMethod.equipmentContainerPositionInfoGet(objCommon, equipmentID, controlJobID, BizConstant.SP_SLM_KEYCATEGORY_CONTROLJOB);

            // Check combination of control job / wafer in equipment container
            List<Infos.EqpContainerPosition> eqpContainerPositionList = eqpContainerPositionInfo.getEqpContainerPositionList();
            Validations.check(CimObjectUtils.isEmpty(eqpContainerPositionList), retCodeConfig.getNotFoundEquipmentContainerPosition());

            boolean bFoundWafer = false;
            for (Infos.EqpContainerPosition eqpContainerPosition : eqpContainerPositionList) {
                for (Infos.ProcessJob processJob : strProcessJobList) {
                    List<Infos.ProcessWafer> processWaferList = processJob.getProcessWaferList();
                    bFoundWafer = processWaferList.stream().anyMatch(x -> ObjectIdentifier.equalsWithValue(eqpContainerPosition.getWaferID(), x.getWaferID()));
                    if (CimBooleanUtils.isTrue(bFoundWafer)) {
                        break;
                    }
                }
                Validations.check(CimBooleanUtils.isFalse(bFoundWafer), new OmCode(retCodeConfig.getCtrljobEqpctnpstUnmatch(), ObjectIdentifier.fetchValue(controlJobID), ObjectIdentifier.fetchValue(eqpContainerPosition.getWaferID())));
            }

            //----------------------------------------------------------------
            //  Set ProcessJob to each Equipment Container Position
            //----------------------------------------------------------------
            log.info("【step12】: 【Main Process】Set ProcessJob to each Equipment Container Position");
            List<Infos.ProcessJobMapInfo> strProcessJobMapInfoSeq = new ArrayList<>();
            for (Infos.ProcessJob processJob : strProcessJobList) {
                Infos.ProcessJobMapInfo processJobMapInfo = new Infos.ProcessJobMapInfo();
                processJobMapInfo.setProcessJobID(processJob.getProcessJobID());
                processJobMapInfo.setWaferSeq(processJob.getProcessWaferList().stream().map(Infos.ProcessWafer::getWaferID).collect(Collectors.toList()));
                strProcessJobMapInfoSeq.add(processJobMapInfo);
            }
            equipmentMethod.equipmentContainerPositionProcessJobSet(objCommon, equipmentID, strProcessJobMapInfoSeq);
        }


        /*-------------------------------------------------*/
        /*   call APCMgr_SendProcessJobInformationDR__120  */
        /*-------------------------------------------------*/
        log.info("【step13】: call APCMgr_SendProcessJobInformationDR__120");
        //TODO-NOTIMPL: APCMgr_SendProcessJobInformationDR__120
    }

    @Override
    public void sxPJStatusChangeReq(Infos.ObjCommon objCommon, Params.PJStatusChangeReqParams params) {
        Infos.PJStatusChangeReqInParm inParm = params.getStrPJStatusChangeReqInParm();
        ObjectIdentifier equipmentID = inParm.getEquipmentID();
        ObjectIdentifier controlJobID = inParm.getControlJobID();
        String actionCode = inParm.getActionCode();
        //------------------------------------------------------
        // Check Function Availability
        //------------------------------------------------------
        if (log.isDebugEnabled()) {
            log.debug("【step1】: Check Function Availability");
        }
        String PJCtrlFunc = StandardProperties.OM_PJ_CONTROL_ENABLE_FLAG.getValue();
        Validations.check(!CimStringUtils.equals(PJCtrlFunc, BizConstant.SP_FUNCTION_AVAILABLE_TRUE),
                retCodeConfig.getFunctionNotAvailable());

        //------------------------------------------------------
        // Check Equipment
        //------------------------------------------------------
        if (log.isDebugEnabled()) {
            log.debug("【step2】: Check Equipment");
        }
        equipmentMethod.equipmentProcessJobLevelControlCheck(objCommon,
                equipmentID,
                true,
                true,
                true,
                false);

        //------------------------------------------------------
        // Check Control Job
        //------------------------------------------------------
        if (log.isDebugEnabled()) {
            log.debug("【step3】: Check Control Job");
        }
        Outputs.ControlJobAttributeInfo controlJobAttributeInfo =
                controlJobMethod.controlJobAttributeInfoGet(objCommon, controlJobID);

        //------------------------------------------------------
        // Check the actionCode
        //------------------------------------------------------
        if (log.isDebugEnabled()) {
            log.debug("【step4】: Check the actionCode");
        }
        // The actionCode should be one of the following
        // a.   ProcessJobAbort
        // b.   ProcessJobPause
        // c.   ProcessJobResume
        // d.   ProcessJobStart
        // e.   ProcessJobStop
        Validations.check(CimStringUtils.unEqualIn(actionCode,
                BizConstant.SP_PROCESSJOBACTION_ABORT,
                BizConstant.SP_PROCESSJOBACTION_PAUSE,
                BizConstant.SP_PROCESSJOBACTION_RESUME,
                BizConstant.SP_PROCESSJOBACTION_START,
                BizConstant.SP_PROCESSJOBACTION_STOP),
                retCodeConfig.getInvalidActionCode(), actionCode);

        //------------------------------------------------------
        // Check operation mode
        //------------------------------------------------------
        if (log.isDebugEnabled()) {
            log.debug("【step5】: Check operation mode");
        }
        //If actionCode is ProcessJobStart, equipment start mode should be Manual
        if (CimStringUtils.equals(actionCode, BizConstant.SP_PROCESSJOBACTION_START)) {
            ObjectIdentifier loadPortID = controlJobAttributeInfo.getStrControlJobCassetteSeq().get(0).getLoadPortID();

            if (ObjectIdentifier.isEmpty(loadPortID)) {
                boolean slmCapabilityFlag = false;
                /*--------------------------------------*/
                /*   Get SLM Switch for the equipment   */
                /*--------------------------------------*/
                if (log.isDebugEnabled()) {
                    log.debug("【step5-1】: Get SLM Switch for the equipment");
                }
                Infos.EqpBrInfo eqpBrInfo = equipmentMethod.equipmentBRInfoGetDR(objCommon, equipmentID);

                // Check SLM Capability
                if (CimBooleanUtils.isTrue(eqpBrInfo.isFmcCapabilityFlag())) {
                    if (log.isDebugEnabled()) {
                        log.debug("FMC Capability is TRUE. ");
                    }
                    slmCapabilityFlag = true;
                }

                if (slmCapabilityFlag) {
                    if (log.isDebugEnabled()) {
                        log.debug("FMCCapabilityFlag == TRUE");
                    }

                    //----------------------------------------------------------------
                    // Get Equipment Container Information
                    //----------------------------------------------------------------
                    if (log.isDebugEnabled()) {
                        log.debug("【step5-2】: Get Equipment Container Information");
                    }
                    Infos.EqpContainerPositionInfo getEquipmentContainerPositionInfo =
                            equipmentContainerPositionMethod.equipmentContainerPositionInfoGet(objCommon,
                                    new Inputs.ObjEquipmentContainerPositionInfoGetIn(equipmentID,
                                            controlJobID, BizConstant.SP_SLM_KEYCATEGORY_PROCESSJOB));
                    List<Infos.EqpContainerPosition> strCtnPosInfoSeq = getEquipmentContainerPositionInfo.
                            getEqpContainerPositionList();
                    Validations.check(CimArrayUtils.isEmpty(strCtnPosInfoSeq),
                            retCodeConfig.getNotFoundEquipmentContainerPosition());
                    loadPortID = strCtnPosInfoSeq.get(0).getSrcPortID();
                }
            }

            //Get port access mode
            if (log.isDebugEnabled()) {
                log.debug("【step5-3】: Get port access mode");
            }
            Outputs.ObjPortResourceCurrentOperationModeGetOut getPortResourceCurrentOperationMode =
                    portMethod.portResourceCurrentOperationModeGet(objCommon, equipmentID, loadPortID);

            String operationStartMode = getPortResourceCurrentOperationMode.getOperationMode().getMoveInMode();
            Validations.check(!CimStringUtils.equals(operationStartMode, BizConstant.SP_EQP_STARTMODE_MANUAL),
                    retCodeConfigEx.getInvalidPortStartmode(),
                    ObjectIdentifier.fetchValue(loadPortID),
                    operationStartMode);
        }

        //------------------------------------------------------
        // Main Process : Call EAP to get information
        //------------------------------------------------------
        if (log.isDebugEnabled()) {
            log.debug("【step6】: Main Process : Call EAP to get information");
        }

        long sleepTimeValue = StandardProperties.OM_EAP_CONNECT_SLEEP_TIME.getLongValue() == 0L ?
                BizConstant.SP_DEFAULT_SLEEP_TIME_TCS : StandardProperties.OM_EAP_CONNECT_SLEEP_TIME.getLongValue();

        long retryCountValue = StandardProperties.OM_EAP_CONNECT_RETRY_COUNT.getLongValue() == 0L ?
                BizConstant.SP_DEFAULT_RETRY_COUNT_TCS : StandardProperties.OM_EAP_CONNECT_SLEEP_TIME.getLongValue();
        for (int retryNum = 0; retryNum < (retryCountValue + 1); retryNum++) {
            if (log.isDebugEnabled()) {
                log.debug("{} loop to retryCountValue + 1", retryNum);
            }
            /*--------------------------*/
            /*    Send Request to EAP   */
            /*--------------------------*/
            IEAPRemoteManager eapRemoteManager = eapMethod.eapRemoteManager(objCommon, params.getUser(),
                    params.getStrPJStatusChangeReqInParm().getEquipmentID(), null, false);
            if (null == eapRemoteManager) {
                if (log.isDebugEnabled()) {
                    log.debug("MES not configure EAP host");
                }
                break;
            }
            params.setActionRequestTimeStamp(objCommon.getTimeStamp().getReportTimeStamp().toString());
            try {
                eapRemoteManager.sendPJStatusChangeReq(params);
                log.debug("Now EAP subSystem is alive!! Go ahead");
                break;
            } catch (ServiceException ex) {
                if (Validations.isEquals(ex.getCode(), retCodeConfig.getTcsNoResponse())) {
                    if (log.isDebugEnabled()) {
                        log.debug("EAP subsystem has return NO_RESPONSE!just retry now!! now count...{}", retryNum);
                        log.debug("now sleeping... {}", sleepTimeValue);
                    }
                    if (retryNum != retryCountValue) {
                        try {
                            Thread.sleep(sleepTimeValue);
                            continue;
                        } catch (InterruptedException e) {
                            ex.addSuppressed(e);
                            Thread.currentThread().interrupt();
                            throw ex;
                        }
                    } else {
                        Validations.check(true, retCodeConfig.getTcsNoResponse());
                    }
                } else {
                    Validations.check(true, new OmCode(ex.getCode(), ex.getMessage()));
                }
            }
        }

        //------------------------------------------------------
        // Create posProcessJobChangeEventRecord
        //------------------------------------------------------
        if (log.isDebugEnabled()) {
            log.debug("【step7】: Create posProcessJobChangeEventRecord");
        }
        //Prepare event data
        Inputs.ProcessJobChangeEventMakeParams eventMakeParams = new Inputs.ProcessJobChangeEventMakeParams();
        String opeCategory = null;
        switch (actionCode) {
            case BizConstant.SP_PROCESSJOBACTION_ABORT: {
                opeCategory = BizConstant.SP_PROCESSJOBOPECATEGORY_ABORTREQ;
                break;
            }
            case BizConstant.SP_PROCESSJOBACTION_PAUSE: {
                opeCategory = BizConstant.SP_PROCESSJOBOPECATEGORY_PAUSEREQ;
                break;
            }
            case BizConstant.SP_PROCESSJOBACTION_RESUME: {
                opeCategory = BizConstant.SP_PROCESSJOBOPECATEGORY_RESUMEREQ;
                break;
            }
            case BizConstant.SP_PROCESSJOBACTION_START: {
                opeCategory = BizConstant.SP_PROCESSJOBOPECATEGORY_STARTREQ;
                break;
            }
            case BizConstant.SP_PROCESSJOBACTION_STOP: {
                opeCategory = BizConstant.SP_PROCESSJOBOPECATEGORY_STOPREQ;
                break;
            }
        }
        eventMakeParams.setOpeCategory(opeCategory);
        eventMakeParams.setControlJobID(params.getStrPJStatusChangeReqInParm().getControlJobID());
        eventMakeParams.setProcessJobID(params.getStrPJStatusChangeReqInParm().getProcessJobID());
        eventMakeParams.setProcessStart(BizConstant.SP_PROCESSJOBSTART_DEFAULT);
        eventMakeParams.setCurrentState(params.getStrPJStatusChangeReqInParm().getCurrentState());
        eventMakeParams.setClaimMemo(params.getClaimMemo());
        eventMakeParams.setProcessJobChangeRecipeParameterList(Collections.EMPTY_LIST);
        eventMakeParams.setProcessWaferList(Collections.EMPTY_LIST);
        eventMethod.processJobChangeEventMake(objCommon, eventMakeParams);
    }

    @Override
    public void sxPJStatusChangeRpt(Infos.ObjCommon objCommon, Params.PJStatusChangeRptInParm params) {
        //----------------------------------------------------------------
        //  Pre Process
        //----------------------------------------------------------------

        // Initialize

        //Trace InParameters
        log.info("InParam equipmentID   : {}", params.getEquipmentID());
        log.info("InParam controlJobID  : {}", params.getControlJobID());
        log.info("InParam processJobID  : {}", params.getProcessJobID());
        log.info("InParam currentState  : {}", params.getCurrentState());
        log.info("InParam actionCode  : {}", params.getActionCode());

        //------------------------------------------------------
        // Check Function Availability
        //------------------------------------------------------
        String PJCtrlFunc = StandardProperties.OM_PJ_CONTROL_ENABLE_FLAG.getValue();

        Validations.check(!CimStringUtils.equals(PJCtrlFunc, BizConstant.SP_FUNCTION_AVAILABLE_TRUE), retCodeConfig.getFunctionNotAvailable());

        //----------------------------------------------------------------
        //  In parameters check
        //----------------------------------------------------------------

        //------------------------------------------------------
        // Check Equipment
        //------------------------------------------------------
        Infos.EqpBrInfo eqpBrInfo = equipmentMethod.equipmentBRInfoGetDR(objCommon, params.getEquipmentID());

        //----------------------------------------------------------------------
        //
        //  Main Process
        //
        //----------------------------------------------------------------------

        //List of LotID & WaferID
        Map<ObjectIdentifier, Infos.CommonObjectIdentifierStruct> lotWaferList = new HashMap<>();
        Infos.CommonObjectIdentifierStruct strCommonObjectIdentifierStruct = new Infos.CommonObjectIdentifierStruct();

        List<ObjectIdentifier> waferIDSeq = new ArrayList<>();
        ObjectIdentifier lotID = null;

        //  Pre Process for Wafer level QTime
        if (CimStringUtils.equals(params.getActionCode(), BizConstant.SP_PROCESSJOBACTIONCOMPLETED)
                || CimStringUtils.equals(params.getActionCode(), BizConstant.SP_SLM_ACTIONCODE_PROCESSINGCOMP)) {
            log.info("call processOperation_processWafers_Get()");
            List<Infos.ProcessJob> strProcessOperationProcessWafersGetOut = processMethod.processOperationProcessWafersGet(objCommon, params.getControlJobID());

            int pjLen = CimArrayUtils.getSize(strProcessOperationProcessWafersGetOut);
            for (int i = 0; i < pjLen; i++) {
                log.info("strProcessOperationProcessWafersGetOut.strProcessJobSeq[i].processJobID : {}", strProcessOperationProcessWafersGetOut.get(i).getProcessJobID());
                if (CimStringUtils.equals(strProcessOperationProcessWafersGetOut.get(i).getProcessJobID(), params.getProcessJobID())) {
                    log.info("ProcessJob is found");
                    int WaferListLen = CimArrayUtils.getSize(strProcessOperationProcessWafersGetOut.get(i).getProcessWaferList());
                    for (int j = 0; j < WaferListLen; j++) {
                        if (!lotWaferList.containsKey(strProcessOperationProcessWafersGetOut.get(i).getProcessWaferList().get(j).getLotID())) {
                            log.info("The lot does not exist in lotWaferList : {}", strProcessOperationProcessWafersGetOut.get(i).getProcessWaferList().get(j).getLotID());

                            lotID = strProcessOperationProcessWafersGetOut.get(i).getProcessWaferList().get(j).getLotID();
                            for (int k = 0; k < WaferListLen; k++) {
                                if (ObjectIdentifier.equalsWithValue(strProcessOperationProcessWafersGetOut.get(i).getProcessWaferList().get(k).getLotID(), lotID)) {
                                    log.info("Add a Wafer to waferIDSeq :{}", strProcessOperationProcessWafersGetOut.get(i).getProcessWaferList().get(k).getWaferID());
                                    waferIDSeq.add(strProcessOperationProcessWafersGetOut.get(i).getProcessWaferList().get(k).getWaferID());
                                }
                            }
                            log.info("Add a lotWaferList to list");
                            strCommonObjectIdentifierStruct.setObjectIdentifierSeq(waferIDSeq);
                            lotWaferList.put(lotID, strCommonObjectIdentifierStruct);

                            /*---------------------*/
                            /*   Lock Lot Object   */
                            /*---------------------*/
                            objectLockMethod.objectLock(objCommon, CimLot.class, lotID);
                        }
                    }
                    break;
                }
            }


        }

        // For Process Job Level Control Equipment
        if (CimBooleanUtils.isTrue(eqpBrInfo.isProcessJobLevelCtrl())) {
            log.info("processJobLevelCtrl = TRUE");
            //------------------------------------------------------
            // Check the actionCode
            //------------------------------------------------------
            // The actionCode should be one of the following
            // a.  "ProcessJobAborted"
            // b.  "ProcessJobPaused"
            // c.  "ProcessJobResumed"
            // d.  "ProcessJobStarted"
            // e.  "ProcessJobStopped"
            // f.  "ProcessJobCompleted"
            // g.  SP_SLM_ActionCode_ProcessStart
            // f.  SP_SLM_ActionCode_ProcessingComp
            if (CimStringUtils.equals(params.getActionCode(), BizConstant.SP_PROCESSJOBACTIONABORTED)
                    || CimStringUtils.equals(params.getActionCode(), BizConstant.SP_PROCESSJOBACTIONPAUSED)
                    || CimStringUtils.equals(params.getActionCode(), BizConstant.SP_PROCESSJOBACTIONRESUMED)
                    || CimStringUtils.equals(params.getActionCode(), BizConstant.SP_PROCESSJOBACTIONSTARTED)
                    || CimStringUtils.equals(params.getActionCode(), BizConstant.SP_PROCESSJOBACTIONSTOPPED)
                    || CimStringUtils.equals(params.getActionCode(), BizConstant.SP_PROCESSJOBACTIONCOMPLETED)
                    || CimStringUtils.equals(params.getActionCode(), BizConstant.SP_SLM_ACTIONCODE_PROCESSSTART)
                    || CimStringUtils.equals(params.getActionCode(), BizConstant.SP_SLM_ACTIONCODE_PROCESSINGCOMP)) {
                //------------------------------------------------------
                // Create posProcessJobChangeEventRecord
                //------------------------------------------------------

                // Prepare event data
                String opeCategory = null;
                if (CimStringUtils.equals(params.getActionCode(), BizConstant.SP_PROCESSJOBACTIONABORTED)) {
                    opeCategory = BizConstant.SP_PROCESSJOBOPECATEGORY_ABORTED;
                } else if (CimStringUtils.equals(params.getActionCode(), BizConstant.SP_PROCESSJOBACTIONPAUSED)) {
                    opeCategory = BizConstant.SP_PROCESSJOBOPECATEGORY_PAUSED;
                } else if (CimStringUtils.equals(params.getActionCode(), BizConstant.SP_PROCESSJOBACTIONRESUMED)) {
                    opeCategory = BizConstant.SP_PROCESSJOBOPECATEGORY_RESUMED;
                } else if (CimStringUtils.equals(params.getActionCode(), BizConstant.SP_PROCESSJOBACTIONSTARTED)
                        || CimStringUtils.equals(params.getActionCode(), BizConstant.SP_SLM_ACTIONCODE_PROCESSSTART)) {
                    opeCategory = BizConstant.SP_PROCESSJOBOPECATEGORY_STARTED;
                } else if (CimStringUtils.equals(params.getActionCode(), BizConstant.SP_PROCESSJOBACTIONSTOPPED)) {
                    opeCategory = BizConstant.SP_PROCESSJOBOPECATEGORY_STOPPED;
                } else if (CimStringUtils.equals(params.getActionCode(), BizConstant.SP_PROCESSJOBACTIONCOMPLETED)
                        || CimStringUtils.equals(params.getActionCode(), BizConstant.SP_SLM_ACTIONCODE_PROCESSINGCOMP)) {
                    opeCategory = BizConstant.SP_PROCESSJOBOPECATEGORY_COMPLETED;
                }
                Inputs.ProcessJobChangeEventMakeParams processJobChangeEventMakeParams = new Inputs.ProcessJobChangeEventMakeParams();
                processJobChangeEventMakeParams.setControlJobID(params.getControlJobID());
                processJobChangeEventMakeParams.setProcessJobID(params.getProcessJobID());
                processJobChangeEventMakeParams.setOpeCategory(opeCategory);
                processJobChangeEventMakeParams.setProcessStart(BizConstant.SP_PROCESSJOBSTART_DEFAULT);
                processJobChangeEventMakeParams.setCurrentState(params.getCurrentState());
                processJobChangeEventMakeParams.setProcessWaferList(new ArrayList<>());
                processJobChangeEventMakeParams.setProcessJobChangeRecipeParameterList(new ArrayList<>());
                eventMethod.processJobChangeEventMake(objCommon, processJobChangeEventMakeParams);
            }
        }
        // For SLM Capability Equipment
        if (CimBooleanUtils.isTrue(eqpBrInfo.isFmcCapabilityFlag())) {
            log.info("SLMCapabilityFlag = TRUE");

            //------------------------------------------------------
            // Check the actionCode
            //------------------------------------------------------
            if (CimStringUtils.equals(params.getActionCode(), BizConstant.SP_PROCESSJOBACTIONSTARTED)
                    || CimStringUtils.equals(params.getActionCode(), BizConstant.SP_PROCESSJOBACTIONCOMPLETED)
                    || CimStringUtils.equals(params.getActionCode(), BizConstant.SP_SLM_ACTIONCODE_PROCESSSTART)
                    || CimStringUtils.equals(params.getActionCode(), BizConstant.SP_SLM_ACTIONCODE_PROCESSINGCOMP)) {
                log.info("actionCode : {}", params.getActionCode());
                //----------------------------------------------------------------
                //  object_Lock for Equipment Container Position by ControlJob
                //----------------------------------------------------------------
                Inputs.ObjObjectLockForEquipmentContainerPositionIn objObjectLockForEquipmentContainerPositionIn = new Inputs.ObjObjectLockForEquipmentContainerPositionIn();
                objObjectLockForEquipmentContainerPositionIn.setEquipmentID(params.getEquipmentID());
                objObjectLockForEquipmentContainerPositionIn.setControlJobID(params.getControlJobID());
                objectLockMethod.objectLockForEquipmentContainerPosition(objCommon, objObjectLockForEquipmentContainerPositionIn);

                //----------------------------------------------------------------
                //  Get Equipment Container Position info by Wafer
                //----------------------------------------------------------------
                int nWaferLen = CimArrayUtils.getSize(params.getWaferList());
                Boolean nonSLMOpeFlag = false;

                List<Infos.EqpContainerPosition> tmpStrEqpContainerPositionSeq = new ArrayList<>();
                log.info("nWaferLen : {}", nWaferLen);
                for (int nCnt1 = 0; nCnt1 < nWaferLen; nCnt1++) {
                    log.info("nCnt1 : {}", nCnt1);
                    log.info("call equipmentContainerPosition_info_Get()");
                    Infos.EqpContainerPositionInfo containerPositionInfo = equipmentMethod.equipmentContainerPositionInfoGet(objCommon, params.getEquipmentID(),
                            waferIDSeq.get(nCnt1), BizConstant.SP_SLM_KEYCATEGORY_WAFER);

                    int lenEqpContPos = CimArrayUtils.getSize(containerPositionInfo.getEqpContainerPositionList());
                    log.info("lenEqpContPos : {}", lenEqpContPos);

                    // Check combination of control job & process job / wafer in equipment container
                    Validations.check(lenEqpContPos < 1, retCodeConfig.getSlmWaferNotFoundInPosition());


                    log.info("SLMState", containerPositionInfo.getEqpContainerPositionList().get(0).getFmcState());
                    if (CimStringUtils.equals(containerPositionInfo.getEqpContainerPositionList().get(0).getFmcState(), BizConstant.SP_SLMSTATE_NONSLMOPE)) {
                        log.info("SLMState = SP_SLMState_NonSLMOpe");
                        nonSLMOpeFlag = true;
                    }

                    // check controlJobID
                    log.info("controlJobID : {}", containerPositionInfo.getEqpContainerPositionList().get(0).getControlJobID());
                    Validations.check(!ObjectIdentifier.equalsWithValue(containerPositionInfo.getEqpContainerPositionList().get(0).getControlJobID(), params.getControlJobID()), retCodeConfig.getCtrljobEqpctnpstUnmatch());


                    //check processJobID
                    log.info("processJobID : {}", containerPositionInfo.getEqpContainerPositionList().get(0).getProcessJobID());
                    Validations.check(!CimStringUtils.equals(containerPositionInfo.getEqpContainerPositionList().get(0).getProcessJobID(),
                            params.getProcessJobID()), retCodeConfig.getCtrljobEqpctnpstUnmatch());

                    tmpStrEqpContainerPositionSeq.add(containerPositionInfo.getEqpContainerPositionList().get(0));
                }

                //----------------------------------------------------------------------
                //  Set process job status information to each Equipment Container Position
                //----------------------------------------------------------------------
                Inputs.EquipmentContainerPositionProcessJobStatusSetIn strEquipmentContainerPositionProcessJobStatusSetIn = new Inputs.EquipmentContainerPositionProcessJobStatusSetIn();
                strEquipmentContainerPositionProcessJobStatusSetIn.setEquipmentID(params.getEquipmentID());
                strEquipmentContainerPositionProcessJobStatusSetIn.setWaferSeq(params.getWaferList());
                strEquipmentContainerPositionProcessJobStatusSetIn.setActionCode(params.getActionCode());

                log.info("call equipmentContainerPosition_processJobStatus_Set()");
                equipmentMethod.equipmentContainerPositionProcessJobStatusSet(objCommon, strEquipmentContainerPositionProcessJobStatusSetIn);

                if (CimBooleanUtils.isFalse(nonSLMOpeFlag)
                        && (CimStringUtils.equals(params.getActionCode(), BizConstant.SP_SLM_ACTIONCODE_PROCESSINGCOMP)
                        || CimStringUtils.equals(params.getActionCode(), BizConstant.SP_PROCESSJOBACTIONCOMPLETED))) {
                    log.info("actionCode = Processing Comp. ");

                    ObjectIdentifier tmpLotID = null;
                    ObjectIdentifier tmpDestCassetteID = null;

                    Infos.EqpPortInfo eqpPortInfo = equipmentMethod.equipmentPortInfoGet(objCommon, params.getEquipmentID());
                    //message queue put only if equipment is online
                    log.info("strEquipmentPortInfoGetDROut.strEqpPortInfo.strEqpPortStatus.length() : {}", CimArrayUtils.getSize(eqpPortInfo.getEqpPortStatuses()));
                    if (0 != CimArrayUtils.getSize(eqpPortInfo.getEqpPortStatuses())) {
                        log.info("strEqpPortStatus.length != 0");
                        log.info("strEquipmentPortInfoGetDROut.strEqpPortInfo.strEqpPortStatus[0].onlineMode : {}", eqpPortInfo.getEqpPortStatuses().get(0).getOnlineMode());
                        if (!CimStringUtils.equals(eqpPortInfo.getEqpPortStatuses().get(0).getOnlineMode(), BizConstant.SP_EQP_ONLINEMODE_OFFLINE)) {
                            log.info("equipment is online");

                            // sort tmpStrEqpContainerPositionSeq
                            Infos.EqpContainerPosition tmpStrEqpContainerPosition;
                            tmpStrEqpContainerPosition = tmpStrEqpContainerPositionSeq.get(0);
                            for (int nCnt1 = 0; nCnt1 < nWaferLen; nCnt1++) {
                                log.info("# Loop[nCnt1], lotID = : {}", nCnt1, tmpStrEqpContainerPositionSeq.get(nCnt1).getLotID());
                                if (0 == nCnt1) {
                                    log.info("# nCnt1 == 0");
                                    continue;
                                }
                                log.info("tmpStrEqpContainerPositionSeq[nCnt1-1].lotID : {}", tmpStrEqpContainerPositionSeq.get(nCnt1 - 1).getLotID());
                                if (!ObjectIdentifier.equalsWithValue(tmpStrEqpContainerPositionSeq.get(nCnt1).getLotID(),
                                        tmpStrEqpContainerPositionSeq.get(nCnt1 - 1).getLotID())) {
                                    log.info("# Different lot, Go Swap Loop");
                                    for (int nCnt2 = nCnt1 + 1; nCnt2 < nWaferLen; nCnt2++) {
                                        if (ObjectIdentifier.equalsWithValue(tmpStrEqpContainerPositionSeq.get(nCnt2).getLotID(),
                                                tmpStrEqpContainerPositionSeq.get(nCnt1 - 1).getLotID())) {
                                            log.info("## Same Lot, Swap position");
                                            tmpStrEqpContainerPosition = tmpStrEqpContainerPositionSeq.get(nCnt1);
                                            tmpStrEqpContainerPositionSeq.add(nCnt1, tmpStrEqpContainerPositionSeq.get(nCnt2));
                                            tmpStrEqpContainerPositionSeq.add(nCnt2, tmpStrEqpContainerPosition);
                                        }
                                    }
                                }
                            }

                            // create message queue by lot
                            for (int nCnt2 = 0; nCnt2 < nWaferLen; nCnt2++) {
                                log.info("nCnt2 : {}", nCnt2);
                                if (!ObjectIdentifier.equalsWithValue(tmpStrEqpContainerPositionSeq.get(nCnt2).getLotID(), tmpLotID)) {
                                    tmpLotID = tmpStrEqpContainerPositionSeq.get(nCnt2).getLotID();
                                    tmpDestCassetteID = tmpStrEqpContainerPositionSeq.get(nCnt2).getDestCassetteID();

                                    Boolean messagePutFlag = true;
                                    String tmpMessageID = null;

                                    if (null == tmpDestCassetteID) {
                                        log.info("destCassette is NOT assigned for lot : {}", tmpLotID);
                                        tmpMessageID = BizConstant.SP_SLM_MSG_DESTUNKNOWN;
                                    } else {
                                        log.info("destCassette is assigned for lot : {}", tmpLotID);
                                        tmpMessageID = BizConstant.SP_SLM_MSG_DESTNOTACCESSIBLE;
                                        // check if destination cassette is loaded on port or reserved for port
                                        int nPortLen = CimArrayUtils.getSize(eqpPortInfo.getEqpPortStatuses());
                                        log.info("nPortLen : {}", nPortLen);
                                        for (int nCnt3 = 0; nCnt3 < nPortLen; nCnt3++) {
                                            log.info("Loop[nCnt3]", nCnt3);
                                            log.info("strEquipmentPortInfoGetDROut.strEqpPortInfo.strEqpPortStatus[nCnt3].loadedCassetteID : {}", eqpPortInfo.getEqpPortStatuses().get(nCnt3).getLoadedCassetteID());
                                            log.info("strEquipmentPortInfoGetDROut.strEqpPortInfo.strEqpPortStatus[nCnt3].dispatchLoadCassetteID : {}", eqpPortInfo.getEqpPortStatuses().get(nCnt3).getDispatchLoadCassetteID());
                                            if (ObjectIdentifier.equalsWithValue(eqpPortInfo.getEqpPortStatuses().get(nCnt3).getLoadedCassetteID(),
                                                    tmpDestCassetteID)) {
                                                // already loaded, no need message queue put
                                                log.info("destCassette is loaded on port : {}", eqpPortInfo.getEqpPortStatuses().get(nCnt3).getPortID());
                                                messagePutFlag = false;
                                                break;

                                            } else if (ObjectIdentifier.equalsWithValue(eqpPortInfo.getEqpPortStatuses().get(nCnt3).getDispatchLoadCassetteID(),
                                                    tmpDestCassetteID)) {
                                                // already NPW reserved, no need message queue put
                                                log.info("destCassette is reserved to port : {}", eqpPortInfo.getEqpPortStatuses().get(nCnt3).getPortID());
                                                messagePutFlag = false;
                                                break;
                                            }
                                        }
                                    }

                                    if (CimBooleanUtils.isTrue(messagePutFlag)) {
                                        log.info("SLM message queue  put");
                                        Infos.StrSLMMsgQueueRecord strSLMMsgQueueRecord = new Infos.StrSLMMsgQueueRecord();
                                        strSLMMsgQueueRecord.setEventName(BizConstant.SP_SLM_EVENTNAME_PROCESSINGCOMP);
                                        strSLMMsgQueueRecord.setEquipmentID(params.getEquipmentID());
                                        strSLMMsgQueueRecord.setPortID(new ObjectIdentifier());
                                        strSLMMsgQueueRecord.setCassetteID(tmpDestCassetteID);
                                        strSLMMsgQueueRecord.setControlJobID(params.getControlJobID());
                                        strSLMMsgQueueRecord.setLotID(tmpLotID);
                                        strSLMMsgQueueRecord.setMessageID(tmpMessageID);
                                        messageMethod.slmMessageQueuePutDR(objCommon, strSLMMsgQueueRecord);
                                    }
                                }// nextLot
                            }//waferSeqLen
                        }// equipment is online
                    }
                }
            }
        }

        //  Wafer level QTime trigger
        if (CimStringUtils.equals(params.getActionCode(), BizConstant.SP_PROCESSJOBACTIONCOMPLETED)
                || CimStringUtils.equals(params.getActionCode(), BizConstant.SP_SLM_ACTIONCODE_PROCESSINGCOMP)) {
            log.info("Wafer level QTime Management");
            /*---------------------------*/
            /*   Set Q-Time Management   */
            /*---------------------------*/

            String getKey = null;
            ObjectIdentifier alotID;
            for (Map.Entry<ObjectIdentifier, Infos.CommonObjectIdentifierStruct> next : lotWaferList.entrySet()) {
                alotID = next.getKey();
                log.info("Qtime Set By PJComp , lotID = :{}", getKey);

                Inputs.QtimeSetByPJCompIn strQtimeSetByPJCompIn = new Inputs.QtimeSetByPJCompIn();
                strQtimeSetByPJCompIn.setLotID(alotID);
                strQtimeSetByPJCompIn.setWaferIDSeq(next.getValue().getObjectIdentifierSeq());
                qTimeMethod.qtimeSetByPJComp(objCommon, strQtimeSetByPJCompIn);
            }
        }
        //------------------------------------------------------
        // Set output result
        //------------------------------------------------------

        // Return to caller

    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param cjStatusChangeReqParams -
     * @return com.fa.cim.dto.RetCode<Results.CJStatusChangeReqResult>
     * @author Bear
     * @date 2018/9/5 16:07
     */
    @Override
    public Results.CJStatusChangeReqResult sxCJStatusChangeReqService(Infos.ObjCommon objCommon,
                                                                      Params.CJStatusChangeReqParams
                                                                              cjStatusChangeReqParams) {

        Results.CJStatusChangeReqResult cjStatusChangeReqResult = new Results.CJStatusChangeReqResult();

        ObjectIdentifier controlJobID = cjStatusChangeReqParams.getControlJobID();
        String controlJobAction = cjStatusChangeReqParams.getControlJobAction();

        //【step1】get environment variables for event_make
        if (log.isDebugEnabled()) {
            log.debug("【step1】get environment variables for event_make");
        }
        String tmpControlJobStatusChangeHistoryMask = StandardProperties.OM_CJ_STATUS_HISTORY_SET.getValue();
        int counter = 0;
        if (CimStringUtils.isEmpty(tmpControlJobStatusChangeHistoryMask)
                || (tmpControlJobStatusChangeHistoryMask.length() != 5)) {
            if (log.isDebugEnabled()) {
                log.debug("Environment variable was set to the default value of 00000 because of the invalid input.");
            }
            tmpControlJobStatusChangeHistoryMask = "00000";
        }
        for (counter = 0; counter < 5; counter++) {
            if (tmpControlJobStatusChangeHistoryMask.charAt(counter) == '0'
                    || tmpControlJobStatusChangeHistoryMask.charAt(counter) == '1') {
                continue;
            }
            break;
        }
        if (counter != 5) {
            if (log.isDebugEnabled()) {
                log.debug("Environment variable was set to the default value of 00000 because of the invalid input.");
            }
            tmpControlJobStatusChangeHistoryMask = "00000";
        } else {
            if (log.isDebugEnabled()) {
                log.debug("normal case. continue to the following procedure.");
            }
        }

        //【step2】check input parameter
        if (log.isDebugEnabled()) {
            log.debug("【step2】check input parameter");
        }
        String controlJobStatus = null;
        int needToCheckControlJobStatus = 0;
        boolean needToMakeEvent = false;
        if (log.isDebugEnabled()) {
            log.debug("CJStatusChangeReq was claimed with :{} {}", controlJobID, controlJobAction);
        }
        ObjectIdentifier saveControlJobID = null;
        if (CimStringUtils.equals(BizConstant.SP_CONTROLJOBACTION_TYPE_CREATE, controlJobAction)) {
            if (log.isDebugEnabled()) {
                log.debug("ControlJobAction type : create");
            }
            List<Infos.StartCassette> startCassetteList =
                    cjStatusChangeReqParams.getControlJobCreateRequest().getStartCassetteList();
            ObjectIdentifier equipmentID = cjStatusChangeReqParams.getControlJobCreateRequest().getEquipmentID();
            String portGroupID = cjStatusChangeReqParams.getControlJobCreateRequest().getPortGroup();
            String transactionID = objCommon.getTransactionID();
            if (CimStringUtils.equals(transactionID, TransactionIDEnum.CONTROL_JOB_MANAGE_REQ.getValue())) {
                log.error("Request of ControlJobAction type : create is not supported to the specified transaction :{}",
                        transactionID);
                Validations.check(retCodeConfig.getCalledFromInvalidTransaction(), objCommon.getTransactionID());
            }
            //【step2-1】create control job
            if (log.isDebugEnabled()) {
                log.debug("【step2-1】create control job");
            }
            ObjectIdentifier controlJobCreateOut = controlJobMethod.controlJobCreate(objCommon,
                    equipmentID, portGroupID, startCassetteList);

            needToMakeEvent = (tmpControlJobStatusChangeHistoryMask.charAt(0) == '1');
            controlJobStatus = BizConstant.SP_CONTROLJOBSTATUS_CREATED;
            cjStatusChangeReqResult.setControlJobID(controlJobCreateOut);
            saveControlJobID = controlJobCreateOut;
        } else if (CimStringUtils.equals(BizConstant.SP_CONTROLJOBACTION_TYPE_QUEUE, controlJobAction)) {
            needToMakeEvent = (tmpControlJobStatusChangeHistoryMask.charAt(1) == '1');
            controlJobStatus = BizConstant.SP_CONTROLJOBSTATUS_QUEUED;
            saveControlJobID = controlJobID;
        } else if (CimStringUtils.equals(BizConstant.SP_CONTROLJOBACTION_TYPE_EXECUTE, controlJobAction)) {
            needToMakeEvent = (tmpControlJobStatusChangeHistoryMask.charAt(2) == '1');
            controlJobStatus = BizConstant.SP_CONTROLJOBSTATUS_EXECUTING;
            saveControlJobID = controlJobID;
        } else if (CimStringUtils.equals(BizConstant.SP_CONTROLJOBACTION_TYPE_COMPLETE, controlJobAction)) {
            needToMakeEvent = (tmpControlJobStatusChangeHistoryMask.charAt(3) == '1');
            controlJobStatus = BizConstant.SP_CONTROLJOBSTATUS_COMPLETED;
            saveControlJobID = controlJobID;
        } else if (CimStringUtils.equalsIn(controlJobAction, BizConstant.SP_CONTROLJOBACTION_TYPE_DELETE,
                BizConstant.SP_CONTROLJOBACTION_TYPE_DELETE_FROM_EQP,
                BizConstant.SP_CONTROLJOBACTION_TYPE_DELETE_FROM_LOTANDCASSETTE)) {
            if (CimStringUtils.equals(objCommon.getTransactionID(),
                    TransactionIDEnum.CONTROL_JOB_MANAGE_REQ.getValue())) {
                log.error("Request of ControlJobAction type : delete is not supported to the specified transaction :{}",
                        objCommon.getTransactionID());
                Validations.check(retCodeConfig.getCalledFromInvalidTransaction(), objCommon.getTransactionID());
            }
            needToMakeEvent = (tmpControlJobStatusChangeHistoryMask.charAt(4) == '1')
                    && !CimStringUtils.equals(BizConstant.SP_CONTROLJOBACTION_TYPE_DELETE_FROM_LOTANDCASSETTE,
                    controlJobAction);
            controlJobStatus = BizConstant.SP_CONTROLJOBSTATUS_DELETED;
            saveControlJobID = controlJobID;
        } else if (CimStringUtils.equals(BizConstant.SP_CONTROLJOBACTION_TYPE_PRIORITY, controlJobAction)) {
            if (log.isDebugEnabled()) {
                log.debug("ControlJobAction type : priority");
            }
            needToMakeEvent = true;
            needToCheckControlJobStatus = 1;
            // This line can be commented out since CJStatus will not be changed by this action
            controlJobStatus = BizConstant.SP_CONTROLJOBSTATUS_PRIORITY;
            saveControlJobID = controlJobID;
        } else if (CimStringUtils.equals(BizConstant.SP_CONTROLJOBACTION_TYPE_STOP, controlJobAction)) {
            if (log.isDebugEnabled()) {
                log.debug("ControlJobAction type : stop");
            }
            needToMakeEvent = true;
            needToCheckControlJobStatus = 2;
            controlJobStatus = BizConstant.SP_CONTROLJOBSTATUS_STOPPED;
            saveControlJobID = controlJobID;
        } else if (CimStringUtils.equals(BizConstant.SP_CONTROLJOBACTION_TYPE_ABORT, controlJobAction)) {
            if (log.isDebugEnabled()) {
                log.debug("ControlJobAction type : stop");
            }
            needToMakeEvent = true;
            needToCheckControlJobStatus = 2;
            controlJobStatus = BizConstant.SP_CONTROLJOBSTATUS_ABORTED;
            saveControlJobID = controlJobID;
        } else {
            Validations.check(retCodeConfig.getInvalidControlJobActionType(), controlJobAction);
        }

        //【step3】 call EAP method for 'Abort','Stop';'Priority'. Event_Make is at the same time ( if needed ).
        if (log.isDebugEnabled()) {
            log.debug("【step3】 call EAP for 'Abort','Stop','Priority'.Event_Make is at the same time ( if needed ).");
        }
        if (needToCheckControlJobStatus > 0 || needToMakeEvent) {
            //【step3-1】Get common information between calling EAP and Event_Make
            if (log.isDebugEnabled()) {
                log.debug("【step3-1】Get common information between calling EAP and Event_Make");
            }
            Outputs.ObjControlJobStartReserveInformationOut controlJobStartReserveInformationGetOut
                    = controlJobMethod.controlJobStartReserveInformationGet(objCommon,
                    saveControlJobID, false);
            //【step3-2】Check current CJ status to before calling EAP
            //【bear】the step we can skip at current phase-1.
            if (log.isDebugEnabled()) {
                log.debug("【step3-2】 Check current CJ status to before calling EAP");
            }
            if (0 < needToCheckControlJobStatus) {
                //-------------------------------------------------------
                // 3-2-1. Get current status of the specified CJ
                //-------------------------------------------------------
                if (log.isDebugEnabled()) {
                    log.debug("3-2-1. Get current status of the specified CJ");
                }
                Outputs.ObjControlJobStatusGetOut strControlJobStatusGetOut =
                        controlJobMethod.controlJobStatusGet(objCommon, saveControlJobID);
                //-------------------------------------------------------
                // 3-2-2. current status vs. required CJ Action
                //-------------------------------------------------------
                if (log.isDebugEnabled()) {
                    log.debug("3-2-2. current status vs. required CJ Action");
                }
                if (1 == needToCheckControlJobStatus) {
                    if (!CimStringUtils.equals(strControlJobStatusGetOut.getControlJobStatus(),
                            BizConstant.SP_CONTROLJOBSTATUS_QUEUED)) {
                        Validations.check(retCodeConfig.getInvalidControlJobActionForCJStatus(),
                                strControlJobStatusGetOut.getControlJobStatus(),
                                ObjectIdentifier.fetchValue(saveControlJobID));
                    }
                } else if (2 == needToCheckControlJobStatus) {
                    if (CimStringUtils.unEqualIn(strControlJobStatusGetOut.getControlJobStatus(),
                            BizConstant.SP_CONTROLJOBSTATUS_QUEUED, BizConstant.SP_CONTROLJOBSTATUS_EXECUTING)) {
                        Validations.check(retCodeConfig.getInvalidControlJobActionForCJStatus(),
                                strControlJobStatusGetOut.getControlJobStatus(),
                                ObjectIdentifier.fetchValue(saveControlJobID));
                    }
                }
                //-----------------------------------------------------------
                // 3-2-3. Check On-line mode of EQP to call EAP
                //-----------------------------------------------------------
                if (log.isDebugEnabled()) {
                    log.debug("3-2-3. Check On-line mode of EQP to call EAP");
                }
                Infos.EqpPortInfo eqpPortInfo = equipmentMethod.equipmentPortInfoGet(objCommon,
                        controlJobStartReserveInformationGetOut.getEquipmentID());
                Validations.check(CimStringUtils.equals(eqpPortInfo.getEqpPortStatuses().get(0).getOnlineMode(),
                        BizConstant.SP_EQP_ONLINEMODE_OFFLINE), retCodeConfig.getInvalidFromEqpMode(),
                        ObjectIdentifier.fetchValue(controlJobStartReserveInformationGetOut.getEquipmentID()),
                        eqpPortInfo.getEqpPortStatuses().get(0).getOnlineMode());
                //-------------------------------------------------------
                // 3-2-4. Call TxControlJobActionReq of EAP
                //-------------------------------------------------------
                Inputs.SendControlJobActionReqInput eapParam = new Inputs.SendControlJobActionReqInput();
                eapParam.setActionCode(controlJobAction);
                eapParam.setControlJobID(saveControlJobID);
                eapParam.setEquipmentID(controlJobStartReserveInformationGetOut.getEquipmentID());
                eapParam.setClaimMemo(cjStatusChangeReqParams.getClaimMemo());

                long sleepTimeValue = StandardProperties.OM_EAP_CONNECT_SLEEP_TIME.getLongValue() == 0L ?
                        BizConstant.SP_DEFAULT_SLEEP_TIME_TCS :
                        StandardProperties.OM_EAP_CONNECT_SLEEP_TIME.getLongValue();

                long retryCountValue = StandardProperties.OM_EAP_CONNECT_RETRY_COUNT.getLongValue() == 0L ?
                        BizConstant.SP_DEFAULT_RETRY_COUNT_TCS :
                        StandardProperties.OM_EAP_CONNECT_SLEEP_TIME.getLongValue();
                for (int retryNum = 0; retryNum < (retryCountValue + 1); retryNum++) {
                    if (log.isDebugEnabled()) {
                        log.debug("{} loop to retryCountValue + 1", retryNum);
                    }
                    /*--------------------------*/
                    /*    Send Request to EAP   */
                    /*--------------------------*/
                    IEAPRemoteManager eapRemoteManager = eapMethod.eapRemoteManager(objCommon,
                            cjStatusChangeReqParams.getUser(),
                            controlJobStartReserveInformationGetOut.getEquipmentID(),
                            null, false);
                    if (null == eapRemoteManager) {
                        if (log.isDebugEnabled()) {
                            log.debug("MES not configure EAP host");
                        }
                        break;
                    }
                    try {
                        eapRemoteManager.sendControlJobActionReq(eapParam);
                        log.debug("Now EAP subSystem is alive!! Go ahead");
                        break;
                    } catch (ServiceException ex) {
                        if (Validations.isEquals(ex.getCode(), retCodeConfig.getTcsNoResponse())) {
                            if (log.isDebugEnabled()) {
                                log.debug("EAP subsystem has return NO_RESPONSE!just retry now!! now count...{}",
                                        retryNum);
                                log.debug("now sleeping... {}", sleepTimeValue);
                            }
                            if (retryNum != retryCountValue) {
                                try {
                                    Thread.sleep(sleepTimeValue);
                                    continue;
                                } catch (InterruptedException e) {
                                    ex.addSuppressed(e);
                                    Thread.currentThread().interrupt();
                                    throw ex;
                                }
                            } else {
                                Validations.check(true, retCodeConfig.getTcsNoResponse());
                            }
                        } else {
                            Validations.check(true, new OmCode(ex.getCode(), ex.getMessage()));
                        }
                    }
                }

            }
            //【step3-3】Event Make
            if (log.isDebugEnabled()) {
                log.debug("【step3-3】Event Make");
            }
            if (needToMakeEvent) {
                eventMethod.controlJobStatusChangeEventMake(objCommon,
                        objCommon.getTransactionID(),
                        saveControlJobID,
                        controlJobStatus,
                        controlJobStartReserveInformationGetOut.getStartCassetteList(),
                        cjStatusChangeReqParams.getClaimMemo());
            }
        }

        //【step4】change control job status
        if (log.isDebugEnabled()) {
            log.debug("【step4】change control job status");
        }
        if (CimStringUtils.unEqualIn(controlJobAction, BizConstant.SP_CONTROLJOBACTION_TYPE_PRIORITY,
                BizConstant.SP_CONTROLJOBACTION_TYPE_DELETE_FROM_LOTANDCASSETTE)) {
            controlJobMethod.controlJobStatusChange(objCommon, saveControlJobID, controlJobStatus);
        }

        /**************************************************************************************************************/
        //【step5】Delete controljob when deletion of CJ is claimed method call for deleting controlJob should be
        //at the end. controljob needs to be alive during above procedure.
        /**************************************************************************************************************/
        if (log.isDebugEnabled()) {
            log.debug("【step5】Delete controljob when deletion of CJ is claimed method " +
                    "call for deleting controlJob should be at the end.");
        }
        if (CimStringUtils.equalsIn(controlJobAction, BizConstant.SP_CONTROLJOBACTION_TYPE_DELETE,
                BizConstant.SP_CONTROLJOBACTION_TYPE_DELETE_FROM_LOTANDCASSETTE,
                BizConstant.SP_CONTROLJOBACTION_TYPE_DELETE_FROM_EQP)) {
            controlJobMethod.controlJobDelete(objCommon, saveControlJobID, controlJobAction);
        }
        return cjStatusChangeReqResult;
    }

}
