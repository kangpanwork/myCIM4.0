package com.fa.cim.service.post.Impl;

import com.fa.cim.annotaion.OmService;
import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.constant.CIMStateConst;
import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.OmCode;
import com.fa.cim.common.support.User;
import com.fa.cim.common.utils.*;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.config.RetCodeConfigEx;
import com.fa.cim.dto.*;
import com.fa.cim.method.*;
import com.fa.cim.newcore.bo.durable.CimCassette;
import com.fa.cim.newcore.bo.machine.CimMachine;
import com.fa.cim.newcore.impl.bo.env.StandardProperties;
import com.fa.cim.service.automonitor.IAutoMonitorService;
import com.fa.cim.service.bond.IBondService;
import com.fa.cim.service.dispatch.IDispatchService;
import com.fa.cim.service.doc.IDynamicOperationService;
import com.fa.cim.service.durable.IDurableService;
import com.fa.cim.service.edc.IEngineerDataCollectionService;
import com.fa.cim.service.fsm.IFutureSplitMergeService;
import com.fa.cim.service.lot.ILotService;
import com.fa.cim.service.pcs.IProcessControlScriptService;
import com.fa.cim.service.post.IPostService;
import com.fa.cim.service.processcontrol.IProcessControlService;
import com.fa.cim.service.processmonitor.IProcessMonitorService;
import com.fa.cim.service.psm.IPlannedSplitMergeService;
import com.fa.cim.service.sampling.ISamplingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Timestamp;
import java.util.*;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/9/8        ********            Nyx                create file
 *
 * @author: Nyx
 * @date: 2020/9/8 16:42
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@OmService
public class PostService implements IPostService {
    @Autowired
    private RetCodeConfig retCodeConfig;

    @Autowired
    private RetCodeConfigEx retCodeConfigEx;

    @Autowired
    private IPostProcessMethod postProcessQueueMethod;

    @Autowired
    private ILotMethod lotMethod;

    @Autowired
    private ICassetteMethod cassetteMethod;

    @Autowired
    private IPostProcessMethod postProcessMethod;

    @Autowired
    private IEquipmentMethod equipmentMethod;

    @Autowired
    private IEventMethod eventMethod;

    @Autowired
    private ILotService lotService;

    @Autowired
    private IDynamicOperationService dynamicOperationService;

    @Autowired
    private IDurableMethod durableMethod;

    @Autowired
    private IBondingGroupMethod bondingGroupMethod;

    @Autowired
    private IProcessMethod processMethod;

    @Autowired
    private IMessageMethod messageMethod;

    @Autowired
    private IControlJobMethod controlJobMethod;

    @Autowired
    private IAutoDispatchControlMethod autoDispatchControlMethod;

    @Autowired
    private IObjectLockMethod objectLockMethod;

    @Autowired
    private IObjectMethod objectMethod;

    @Autowired
    private IPlannedSplitMergeService plannedSplitMergeService;

    @Autowired
    private IProcessMonitorService processMonitorService;

    @Autowired
    private IProcessControlService processControlService;

    @Autowired
    private IEngineerDataCollectionService engineerDataCollectionService;

    @Autowired
    private IDispatchService dispatchService;

    @Autowired
    private IAutoMonitorService autoMonitorService;

    @Autowired
    private IPostService postService;

    @Autowired
    private IProcessControlScriptService processControlScriptService;

    @Autowired
    private IBondService bondService;

    @Autowired
    private IDurableService durableService;

    @Autowired
    private IRunCardMethod runCardMethod;

    @Autowired
    private IScheduleChangeReservationMethod scheduleChangeReservationMethod;

    @Autowired
    private ISamplingService samplingService;

    @Autowired
    private IFutureSplitMergeService futureSplitMergeService;

    @Override
    public Results.PostTaskExecuteReqResult sxPostTaskExecuteReq(Infos.ObjCommon objCommon, Params.PostTaskExecuteReqParams postTaskExecuteReqParams) {
        int rc = retCodeConfig.getSucc().getCode();
        int rc_extPostProc = retCodeConfig.getSucc().getCode();
        int rc_interFabXfer = retCodeConfig.getSucc().getCode();
        Results.PostTaskExecuteReqResult postTaskExecuteReqResult = new Results.PostTaskExecuteReqResult();

        String key = postTaskExecuteReqParams.getKey();
        int syncFlag = postTaskExecuteReqParams.getSyncFlag();
        long prevSeqNo = postTaskExecuteReqParams.getPreviousSequenceNumber();

        ObjectIdentifier objDummy = null;
        List<Long> nullSeqNoList = new ArrayList<>();
        String strPostProcessQueueState = syncFlag != -1 ? BizConstant.SP_POSTPROCESS_STATE_RESERVED : null;
        postTaskExecuteReqResult.setLastSequenceNumber(0); // Initialize.
        int ppChainMode = StandardProperties.OM_PP_CHAIN_FLAG.getIntValue();
        int rc_pp = retCodeConfig.getSucc().getCode();
        List<Infos.PostProcessActionInfo> postProcessActionInfos = null;
        try {
            postProcessActionInfos = postProcessQueueMethod.postProcessQueueGetDR(objCommon, key, prevSeqNo, nullSeqNoList, strPostProcessQueueState, syncFlag, null, objDummy);
        } catch (ServiceException ex) {
            if (Validations.isEquals(retCodeConfig.getNotFoundEntry(), ex.getCode())) {
                log.info("Not found the queue... ");
                try {
                    postProcessQueueMethod.postProcessAdditionalInfoDeleteDR(objCommon, key);
                } catch (ServiceException ex1) {
                    if (!Validations.isEquals(retCodeConfig.getPostprocQueueExist(), ex1.getCode())) {
                        throw ex1;
                    }
                }
            } else {
                throw ex;
            }
        }
        if (CimArrayUtils.isEmpty(postProcessActionInfos)) {
            return postTaskExecuteReqResult;
        }
        Infos.PostProcessActionInfo info = postProcessActionInfos.get(0);
        List<Infos.PostProcessActionInfo> postProcessActionInfoList = new ArrayList<>();
        postProcessActionInfoList.add(info);
        postTaskExecuteReqResult.setPostProcessActionInfoList(postProcessActionInfoList);
        postTaskExecuteReqResult.setLastSequenceNumber(info.getSequenceNumber());

        //----------------------------------------------------------------------------------------------------------
        // Check execCondition
        //
        // Example)
        //   sequenceNumber   execCondition  targetType   postProcID      LotID
        //  ------------------------------------------------------------------
        //     1                      LOT        Split           LOT-A
        //     2          1           LOT        PlannedSplit    LOT-A     <-- sequenceNumber 2 is dependent on sequenceNumber 1.
        //     3                      EQP        MessageQueuePut LOT-A
        // sequenceNumber 2 must be performed after sequenceNumber 1 is performed.
        //----------------------------------------------------------------------------------------------------------
        if (!CimObjectUtils.isEmpty(info.getExecConditionList())) {
            List<Infos.PostProcessActionInfo> postProcessActionInfos2 = null;
            try {
                postProcessActionInfos2 = postProcessMethod.postProcessQueueGetDR(objCommon, key, -1L, info.getExecConditionList(), null, -1, null, objDummy);
                boolean bExecutable = false;
                if (CimStringUtils.equals(info.getPostProcessID(), BizConstant.SP_POSTPROCESS_ACTIONID_PARALLELEXECFINALIZE)) {
                    // In case InterFabXfer is performed for partial lots in a cassette, PostProcExecFinalize action is executable
                    // if all of head actions of each lots are "InterFabXfer" and its status is "Executing"
                    //
                    // Lot_A and Lot_B is in the same cassette
                    //     Lot_A : InterFabXfer isn't defined
                    //     Lot_B : InterFabXfer is    defined
                    // => PostProcess should be completed for Lot_A, and PostProcess stops at "InterFabXfer" for Lot_B
                    //
                    // seqNo execCond PostProcID           TARGET_TYPE  LotID
                    // ----- -------- -------------------- ------------ -------
                    // 1              InterFabXfer         LOT-ABSOLUTE Lot_A
                    // 2              InterFabXfer         LOT-ABSOLUTE Lot_B
                    // 3     1        Script               LOT-ABSOLUTE Lot_A
                    // 4     2        Script               LOT-ABSOLUTE Lot_B
                    // 5     3+4      ParallelExecFinalize CAST
                    //
                    // Procedure
                    //     For Lot_A, PostProcess of seqNo 1 and 3 completes
                    //     For Lot_B, PostProcess stops at seqMp 2. (status "Executing")
                    //     => Exec condition of "ParallelExecFinalize" isn't met.
                    // However if following condition is met, "ParallelExecFinalize" is executable
                    //     - Head PostProcess actions of the lot which exec condition isn't met are "InteFabXfer"
                    //     - The status of PostProcess is "Executing
                    // (e.g.)
                    // seqNo execCond PostProcID           TARGET_TYPE  LotID   Status
                    // ----- -------- -------------------- ------------ ------- --------------
                    // 1              InterFabXfer         LOT-ABSOLUTE Lot_A   Completed(Deleted)
                    // 2              InterFabXfer         LOT-ABSOLUTE Lot_B   Executing
                    // 3     1        Script               LOT-ABSOLUTE Lot_A   Completed(Deleted)
                    // 4     2        Script               LOT-ABSOLUTE Lot_B   Reserved
                    // 5     3+4      ParallelExecFinalize CAST
                    //
                    // Exec condition of "ParallelExecFinalize" is "3+4" and isn't met for now.
                    // Head PostProcess for Lot_B(exec condition isn't met) is "InterFabXfer" and the status is "Executing"
                    // => "ParallelExecFinalize" is executable
                    //
                    bExecutable = true;
                    int nLen = CimArrayUtils.getSize(postProcessActionInfos2);
                    for (int i = 0; i < nLen; i++) {
                        Infos.PostProcessActionInfo postProcessActionInfo = postProcessActionInfos2.get(i);
                        if (CimStringUtils.equals(postProcessActionInfo.getTargetType(), BizConstant.SP_POSTPROCESS_TARGETTYPE_LOT)
                                || CimStringUtils.equals(postProcessActionInfo.getTargetType(), BizConstant.SP_POSTPROCESS_TARGETTYPE_LOT_ABSOLUTE)) {
                            Infos.PostProcessTargetObject postProcessTargetObject = new Infos.PostProcessTargetObject();
                            postProcessTargetObject.setLotID(postProcessActionInfo.getPostProcessTargetObject().getLotID());
                            Inputs.PostProcessQueueListDRIn postProcessQueueListDRIn = new Inputs.PostProcessQueueListDRIn();
                            postProcessQueueListDRIn.setKey(key);
                            postProcessQueueListDRIn.setSeqNo(-1L);
                            postProcessQueueListDRIn.setPostProcId(BizConstant.SP_POSTPROCESS_ACTIONID_INTERFABXFER);
                            postProcessQueueListDRIn.setSyncFlag(-1L);
                            postProcessQueueListDRIn.setStrPostProcessTargetObject(postProcessTargetObject);
                            postProcessQueueListDRIn.setStatus(BizConstant.SP_POSTPROCESS_STATE_EXECUTING);
                            postProcessQueueListDRIn.setPassedTime(-1L);
                            postProcessQueueListDRIn.setMaxCount(1L);
                            postProcessQueueListDRIn.setCommittedReadFlag(true);
                            Outputs.ObjPostProcessQueListDROut objPostProcessQueListDROut = postProcessMethod.postProcessQueueListDR(objCommon, postProcessQueueListDRIn);
                            List<Infos.PostProcessActionInfo> strActionInfoSeq = objPostProcessQueListDROut.getStrActionInfoSeq();
                            int procLen = CimArrayUtils.getSize(strActionInfoSeq);
                            if (procLen > 0) {
                                log.info("InterFabXfer action is being executed for the lot.");
                            } else {
                                log.info("Exec condition isn't met.");
                                bExecutable = false;
                                break;
                            }
                        } else {
                            log.info("Action isn't ParallelExecFinalize. Exec condition isn't met.");
                        }
                        if (!bExecutable) {
                            //-------------------------------------------------------------------------------------
                            // Check based on commitFlag whether performing with TX_COMMIT or without TX_COMMIT.
                            //-------------------------------------------------------------------------------------
                            if (info.getCommitFlag()) {
                                throw new ServiceException(retCodeConfig.getPostprocNextEntryWithCommit());
                            } else {
                                throw new ServiceException(retCodeConfig.getPostprocNextEntryWithoutCommit());
                            }
                        }
                    }
                }
            } catch (ServiceException e) {
                if (Validations.isEquals(retCodeConfig.getNotFoundEntry(), e.getCode())) {
                    log.info("Can execute!");
                } else {
                    throw e;
                }
            }
        }

        /*-------------------------------------------------------------------*/
        /*  Post Process Execution!!                                         */
        /*-------------------------------------------------------------------*/
        String postProcessID = info.getPostProcessID();
        log.info("Executed queue information. dkey:{}, sequenceNumber:{}, postProcID:{}, targetType:{}", info.getDKey(), info.getSequenceNumber(), postProcessID, info.getTargetType());

        //--------------------------------------------------------
        // Copy TXID and claimUserID
        //--------------------------------------------------------
        Infos.ObjCommon tmpStrObjCommonIn = new Infos.ObjCommon();
        tmpStrObjCommonIn.setTransactionID(info.getTransationID());
        User user = new User();
        user.setFunctionID(info.getTransationID());
        user.setUserID(new ObjectIdentifier(info.getClaimUserID()));
        tmpStrObjCommonIn.setUser(user);
        Infos.TimeStamp timeStamp = new Infos.TimeStamp();
        timeStamp.setReportTimeStamp(Timestamp.valueOf(info.getClaimTime()));
        timeStamp.setReportShopDate(info.getClaimShopDate());
        tmpStrObjCommonIn.setTimeStamp(timeStamp);

        //---------------------------------------------------------
        //  Prepare for changing Lot's Hold State. (Hold/Release)
        //---------------------------------------------------------

        ObjectIdentifier dummyLot = null;
        ObjectIdentifier holdReleaseCode = new ObjectIdentifier(BizConstant.SP_REASON_LOTLOCKRELEASE);
        ObjectIdentifier holdCode;
        List<Infos.LotHoldReq> strHoldListSeq = new ArrayList<>();
        Infos.LotHoldReq lotHoldReq = new Infos.LotHoldReq();
        strHoldListSeq.add(lotHoldReq);
        lotHoldReq.setHoldType(BizConstant.SP_HOLDTYPE_LOTHOLD);
        lotHoldReq.setHoldReasonCodeID(new ObjectIdentifier(BizConstant.SP_REASON_LOTLOCK));
        lotHoldReq.setHoldUserID(new ObjectIdentifier(BizConstant.SP_POSTPROC_PERSON));
        lotHoldReq.setResponsibleOperationMark(BizConstant.SP_RESPONSIBLEOPERATION_CURRENT);
        lotHoldReq.setRelatedLotID(dummyLot);
        lotHoldReq.setClaimMemo("");

        //---------------------------------------------------------
        //  Prepare for changing Durables' Hold State. (Hold/Release)
        //---------------------------------------------------------
        ObjectIdentifier dummyID = null;
        List<Infos.DurableHoldList> strDurableHoldList = new ArrayList<>();
        Infos.DurableHoldList durableHoldList = new Infos.DurableHoldList();
        durableHoldList.setHoldType(BizConstant.SP_HOLDTYPE_DURABLEHOLD);
        durableHoldList.setHoldReasonCodeID(new ObjectIdentifier(BizConstant.SP_REASON_DURABLELOCK));
        durableHoldList.setHoldUserID(new ObjectIdentifier(BizConstant.SP_POSTPROC_PERSON));
        durableHoldList.setResponsibleOperationMark(BizConstant.SP_RESPONSIBLEOPERATION_CURRENT);
        durableHoldList.setRouteID(dummyID);
        durableHoldList.setRelatedDurableID(dummyID);
        strDurableHoldList.add(durableHoldList);

        boolean bHoldRelease = true;
        Infos.PostProcessTargetObject postProcessTargetObject = info.getPostProcessTargetObject();
        ObjectIdentifier lotID = postProcessTargetObject.getLotID();
        ObjectIdentifier cassetteID = postProcessTargetObject.getCassetteID();
        ObjectIdentifier equipmentID = postProcessTargetObject.getEquipmentID();
        ObjectIdentifier controlJobID = postProcessTargetObject.getControlJobID();

        String durableCategory = BizConstant.SP_DURABLECAT_CASSETTE;
        if (!ObjectIdentifier.isEmpty(cassetteID)) {
            durableCategory = durableMethod.durableDurableCategoryGet(objCommon, cassetteID);
        }

        // Get environment variables
        int lockHoldUseFlag = StandardProperties.OM_LOCK_HOLD_MODE.getIntValue();
        int postProcFlagUseFlag = StandardProperties.OM_PP_FLAG_USE_FLAG.getIntValue();

        log.info("lockHoldUseFlag     {}", lockHoldUseFlag);
        log.info("postProcFlagUseFlag {}", postProcFlagUseFlag);

        boolean bFindLockHoldFlag = false;
        boolean bInPostProcessFlag = false;

        if (0 == lockHoldUseFlag || 1 == lockHoldUseFlag || 1 == postProcFlagUseFlag) {
            log.info("lockHoldUseFlag -> 0 or 1, postProcFlagUseFlag -> 1");
            if (!ObjectIdentifier.isEmptyWithValue(lotID)) {
                //---------------------------
                // Get the lot's hold list.
                //---------------------------
                List<ObjectIdentifier> lotIDs = new ArrayList<>();
                lotIDs.add(lotID);
                log.info(" {} Get the lot's hold list. ", lotID);
                int retCode = 0;
                List<Infos.LotHoldListAttributes> lotHoldListAttributesList = null;
                try {
                    lotHoldListAttributesList = lotMethod.lotFillInTxTRQ005DR(objCommon, lotID);
                } catch (ServiceException e) {
                    retCode = e.getCode();
                    if (!Validations.isEquals(retCodeConfig.getNotFoundEntry(), e.getCode())) {
                        log.error("lotFillInTxTRQ005DR() != ok");
                        throw e;
                    }
                }
                if (retCode == 0) {
                    //---------------------------
                    // Check LOCK Hold.
                    //---------------------------
                    int lotRsnCnt = CimArrayUtils.getSize(lotHoldListAttributesList);
                    if (!CimArrayUtils.isEmpty(lotHoldListAttributesList)) {
                        for (Infos.LotHoldListAttributes lotHoldListAttributes : lotHoldListAttributesList) {
                            if (ObjectIdentifier.equalsWithValue(lotHoldListAttributes.getReasonCodeID(), BizConstant.SP_REASON_LOTLOCK)) {
                                log.info("Find LOCK Hold.");
                                bFindLockHoldFlag = true;
                                break;
                            }
                        }
                    }
                    log.info("Get InPostProcessFlag of Lot.");
                    Outputs.ObjLotInPostProcessFlagOut objLotInPostProcessFlagOut = lotMethod.lotInPostProcessFlagGet(objCommon, lotID);
                    bInPostProcessFlag = objLotInPostProcessFlagOut.getInPostProcessFlagOfLot();
                    log.info(" {} bInPostProcessFlag ", bInPostProcessFlag);
                }
            } else if (!ObjectIdentifier.isEmpty(cassetteID)) {
                log.info("cassetteID is not blank");
                try {
                    durableMethod.durableOnRouteCheck(objCommon, durableCategory, cassetteID);
                } catch (ServiceException e) {
                    if (Validations.isEquals(retCodeConfig.getDurableOnroute(), e.getCode())) {
                        log.info("durable_OnRoute_Check() == RC_DURABLE_ONROUTE");
                        // durable_FillInODRBQ019
                        int retCode = 0;
                        List<Infos.DurableHoldListAttributes> durableHoldListAttributes = null;
                        try {
                            durableHoldListAttributes = durableMethod.durableFillInODRBQ019(objCommon, durableCategory, cassetteID);
                        } catch (ServiceException ex) {
                            retCode = ex.getCode();
                            if (!Validations.isEquals(retCodeConfig.getNotFoundEntry(), ex.getCode())) {
                                throw ex;
                            }
                        }
                        if (retCode == 0) {
                            //---------------------------
                            // Check LOCK Hold.
                            //---------------------------
                            int durHoldCnt = CimArrayUtils.getSize(durableHoldListAttributes);
                            for (int nDurHold = 0; nDurHold < durHoldCnt; nDurHold++) {
                                Infos.DurableHoldListAttributes durableHoldListAttributes2 = durableHoldListAttributes.get(nDurHold);
                                if (ObjectIdentifier.equalsWithValue(durableHoldListAttributes2.getReasonCodeID(), BizConstant.SP_REASON_DURABLELOCK)) {
                                    bFindLockHoldFlag = true;
                                    break;
                                }
                            }
                        }
                        log.info("Get InPostProcessFlag of Durable.");
                        // Get InPostProcessFlag of Durable
                        Boolean durableInPostProcessFlagGet = durableMethod.durableInPostProcessFlagGet(objCommon, durableCategory, cassetteID);
                        if (durableInPostProcessFlagGet) {
                            log.info("bInPostProcessFlag == TRUE");
                            bInPostProcessFlag = true;
                        }
                    }
                }
            }
        }


        //----------------------------------------
        // Call txPCSExecReq
        //----------------------------------------
        // Line 620
        if (CimStringUtils.equals(info.getPostProcessID(), BizConstant.SP_POSTPROCESS_ACTIONID_SCRIPT)) {
            log.info(">>>>>>>>> Execute Pre1 Script");
            String lotState = null;
            try {
                lotState = lotMethod.lotStateGet(objCommon, lotID);
            } catch (ServiceException e) {
                postTaskExecuteReqResult.setOmCode(new OmCode(e.getCode(), e.getMessage()));
                throw new ServiceException(retCodeConfig.getPostprocError(), postTaskExecuteReqResult);
            }

            if (CimStringUtils.equals(lotState, CIMStateConst.CIM_LOT_STATE_ACTIVE)) {
                log.info("lot_state == Active. - N/A -");
                if (1 == lockHoldUseFlag || 1 == postProcFlagUseFlag) {
                    log.info("Check LockHold & InPostProcessFlag.");
                    if (!bFindLockHoldFlag && !bInPostProcessFlag) {
                        log.info("Lot is not in post process.");
                        postTaskExecuteReqResult.setOmCode(new OmCode(retCodeConfigEx.getLotNotInpostprocess(), ObjectIdentifier.fetchValue(lotID)));
                        throw new ServiceException(new OmCode(retCodeConfig.getPostprocError(), postTaskExecuteReqResult));
                    }
                }

                // Set InPostProcessFlag of Lot to OFF
                lotMethod.lotInPostProcessFlagSet(objCommon, lotID, false);
                if (bFindLockHoldFlag && (1 == lockHoldUseFlag || 0 == lockHoldUseFlag)) {
                    // Release Lot Hold.
                    Params.HoldLotReleaseReqParams holdLotReleaseReqParams = new Params.HoldLotReleaseReqParams();
                    holdLotReleaseReqParams.setReleaseReasonCodeID(holdReleaseCode);
                    holdLotReleaseReqParams.setHoldReqList(strHoldListSeq);
                    holdLotReleaseReqParams.setLotID(lotID);
                    try {
                        log.info("Release Lot's Hold (LOCK)");
                        lotService.sxHoldLotReleaseReq(objCommon, holdLotReleaseReqParams);
                    } catch (ServiceException e) {
                        postTaskExecuteReqResult.setOmCode(new OmCode(e.getCode(), e.getMessage()));
                        throw new ServiceException(retCodeConfig.getPostprocError(), postTaskExecuteReqResult);
                    }
                }

                // Call sxProcessControlScriptRunReq
                Params.ProcessControlScriptRunReqParams scriptParams = new Params.ProcessControlScriptRunReqParams();
                scriptParams.setEquipmentId(equipmentID);
                scriptParams.setLotId(lotID);
                scriptParams.setPhase(BizConstant.SP_BRSCRIPT_PRE1);
                scriptParams.setUser(tmpStrObjCommonIn.getUser());
                try {
                    log.info(">>>>>> Execute Pre1 Script");
                    processControlScriptService.sxProcessControlScriptRunReq(tmpStrObjCommonIn, scriptParams);
                } catch (ServiceException e) {
                    if (e.getCode() != null && !Validations.isEquals(retCodeConfig.getNotFoundScript(), e.getCode())) {
                        postTaskExecuteReqResult.setOmCode(new OmCode(e.getCode(), e.getMessage()));
                        throw new ServiceException(retCodeConfig.getPostprocError(), postTaskExecuteReqResult);
                    } else if (Validations.isEquals(retCodeConfig.getNotFoundScript(), e.getCode())) {
                        // do nothing.
                        log.info("Not Found Script.");
                    } else {
                        throw e;
                    }
                }

                if (lockHoldUseFlag == 1) {
                    try {
                        log.info("Hold Lot. (LOCK)");
                        lotService.sxHoldLotReq(objCommon, lotID, strHoldListSeq);
                    } catch (ServiceException e) {
                        if (!Validations.isEquals(retCodeConfig.getInvalidLotStat(), e.getCode())) {
                            postTaskExecuteReqResult.setOmCode(new OmCode(e.getCode(), e.getMessage()));
                            throw new ServiceException(retCodeConfig.getPostprocError(), postTaskExecuteReqResult);
                        } else {
                            bHoldRelease = false;
                        }
                    }
                }

                if (postProcFlagUseFlag == 1) {
                    log.info("Post Proc Flag is usable.");

                    //Check lot state
                    try {
                        lotState = lotMethod.lotStateGet(objCommon, lotID);
                    } catch (ServiceException e) {
                        postTaskExecuteReqResult.setOmCode(new OmCode(e.getCode(), e.getMessage()));
                        throw new ServiceException(retCodeConfig.getPostprocError(), postTaskExecuteReqResult);
                    }

                    if (CimStringUtils.equals(lotState, CIMStateConst.CIM_LOT_STATE_ACTIVE)) {
                        log.info("lot_state = Active.");
                        lotMethod.lotInPostProcessFlagSet(objCommon, lotID, true);
                    }
                }
            } else {
                log.info("lot_state != Active. - N/A -");
                bHoldRelease = false;
            }
        }

        //----------------------------------------
        // Call txPSMLotActionReq
        //----------------------------------------
        // Line 776
        else if (CimStringUtils.equals(info.getPostProcessID(), BizConstant.SP_POSTPROCESS_ACTIONID_PSM)) {
            //Check lot state
            String lotState = null;
            try {
                lotState = lotMethod.lotStateGet(objCommon, lotID);
            } catch (ServiceException e) {
                postTaskExecuteReqResult.setOmCode(new OmCode(e.getCode(), e.getMessage()));
                throw new ServiceException(retCodeConfig.getPostprocError(), postTaskExecuteReqResult);
            }
            if (CimStringUtils.equals(lotState, CIMStateConst.CIM_LOT_STATE_ACTIVE)) {
                /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/
                /*                                                                       */
                /*   Object Lock Process                                                 */
                /*                                                                       */
                /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/
                //Get lot cassette
                int retCode = 0;
                ObjectIdentifier cassetteGetOut = null;
                try {
                    cassetteGetOut = lotMethod.lotCassetteGet(objCommon, lotID);
                } catch (ServiceException e) {
                    retCode = e.getCode();
                    if (Validations.isEquals(retCodeConfig.getNotFoundCst(), e.getCode())) {

                    } else {
                        throw e;
                    }
                }
                if (retCode == 0) {
                    Infos.LotLocationInfo cassetteLocationInfo = cassetteMethod.cassetteLocationInfoGetDR(cassetteGetOut);
                    // Transfer state is "EI"
                    if (CimStringUtils.equals(cassetteLocationInfo.getTransferStatus(), BizConstant.SP_TRANSSTATE_EQUIPMENTIN)) {
                        // object_lockMode_Get
                        Inputs.ObjLockModeIn objLockModeIn = new Inputs.ObjLockModeIn();
                        objLockModeIn.setObjectID(cassetteLocationInfo.getEquipmentID());
                        objLockModeIn.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
                        objLockModeIn.setFunctionCategory(BizConstant.SP_POSTPROCESS_ACTIONID_PSM);
                        objLockModeIn.setUserDataUpdateFlag(false);
                        Outputs.ObjLockModeOut objLockModeOut = objectMethod.objectLockModeGet(objCommon, objLockModeIn);
                        Long lockMode = objLockModeOut.getLockMode();
                        if (!lockMode.equals(BizConstant.SP_EQP_LOCK_MODE_WRITE)) {
                            // Lock Equipment LoadCassette Element (Write)
                            objectLockMethod.advancedObjectLock(objCommon, new Inputs.ObjAdvanceLockIn(cassetteLocationInfo.getEquipmentID(),
                                    BizConstant.SP_CLASSNAME_POSMACHINE,
                                    BizConstant.SP_OBJECTLOCK_OBJECTTYPE_LOADCASSETTE,
                                    (long) BizConstant.SP_OBJECTLOCK_LOCKTYPE_WRITE,
                                    Collections.singletonList(cassetteID.getValue())));
                        }
                    }
                }
                if (lockHoldUseFlag == 1 || postProcFlagUseFlag == 1) {
                    if (!bFindLockHoldFlag && !bInPostProcessFlag) {
                        postTaskExecuteReqResult.setOmCode(new OmCode(retCodeConfigEx.getLotNotInpostprocess()));
                        throw new ServiceException(retCodeConfig.getPostprocError(), postTaskExecuteReqResult);
                    }
                }
                // Set InPostProcessFlag of Lot to OFF
                lotMethod.lotInPostProcessFlagSet(objCommon, lotID, false);
                if (bFindLockHoldFlag && (lockHoldUseFlag == 1 || lockHoldUseFlag == 0)) {
                    // Release Lot Hold.
                    Params.HoldLotReleaseReqParams holdLotReleaseReqParams = new Params.HoldLotReleaseReqParams();
                    holdLotReleaseReqParams.setHoldReqList(strHoldListSeq);
                    holdLotReleaseReqParams.setReleaseReasonCodeID(holdReleaseCode);
                    holdLotReleaseReqParams.setLotID(lotID);
                    try {
                        lotService.sxHoldLotReleaseReq(objCommon, holdLotReleaseReqParams);
                    } catch (ServiceException e) {
                        postTaskExecuteReqResult.setOmCode(new OmCode(e.getCode(), e.getMessage()));
                        throw new ServiceException(retCodeConfig.getPostprocError(), postTaskExecuteReqResult);
                    }
                }
                // Store TriggerDKey as thread specific data
                if (ppChainMode == 1) {
                    ThreadContextHolder.setThreadSpecificDataString(BizConstant.SP_THREADSPECIFICDATA_KEY_TRIGGERDKEY, info.getDKey());
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
                // Call txPSMLotActionReq
                try {
                    plannedSplitMergeService.sxPSMLotActionReq(tmpStrObjCommonIn, lotID, "");
                } catch (ServiceException e) {
                    if (Validations.isEquals(retCodeConfig.getPostrpocDkeyRecreate(), e.getCode())) {
                        rc_pp = e.getCode();
                    } else {
                        postTaskExecuteReqResult.setOmCode(new OmCode(e.getCode(), e.getMessage()));
                        throw new ServiceException(retCodeConfig.getPostprocError(), postTaskExecuteReqResult);
                    }
                }
                //future split merge
                //-----------------------------------------------------------------
                // Call sxFSMLotActionReq
                //-----------------------------------------------------------------
                try {
                    futureSplitMergeService.sxFSMLotActionReq(tmpStrObjCommonIn, lotID, "");
                } catch (ServiceException e) {
                    log.error("future split merge Fail: {}", e.getMessage());
                    if (Validations.isEquals(retCodeConfig.getPostrpocDkeyRecreate(), e.getCode())) {
                        rc_pp = e.getCode();
                    } else {
                        postTaskExecuteReqResult.setOmCode(new OmCode(e.getCode(), e.getMessage()));
                        throw new ServiceException(retCodeConfig.getPostprocError(), postTaskExecuteReqResult);
                    }
                }
                // remove TriggerDKey from thread specific data (set blank)
                if (ppChainMode == 1) {
                    ThreadContextHolder.removeDataString(BizConstant.SP_THREADSPECIFICDATA_KEY_TRIGGERDKEY);
                }
                if (lockHoldUseFlag == 1) {
                    // Hold Lot "LOCK".
                    try {
                        lotService.sxHoldLotReq(objCommon, lotID, strHoldListSeq);
                    } catch (ServiceException e) {
                        if (!Validations.isEquals(retCodeConfig.getInvalidLotStat(), e.getCode())) {
                            postTaskExecuteReqResult.setOmCode(new OmCode(e.getCode(), e.getMessage()));
                            throw new ServiceException(retCodeConfig.getPostprocError(), postTaskExecuteReqResult);
                        } else {
                            bHoldRelease = false;
                        }
                    }
                }
                if (postProcFlagUseFlag == 1) {
                    // Set InPostProcessFlag of Lot to ON
                    lotMethod.lotInPostProcessFlagSet(objCommon, lotID, true);
                }
            } else {
                //Inactive
                log.info("lot_state != Active. - N/A -");
                bHoldRelease = false;
            }
        }
        //----------------------------------------
        // Call messageQueue_Put
        //----------------------------------------
        else if (CimStringUtils.equals(info.getPostProcessID(), BizConstant.SP_POSTPROCESS_ACTIONID_MESSAGEQUEUEPUT)
                || CimStringUtils.equals(info.getPostProcessID(), BizConstant.SP_POSTPROCESS_ACTIONID_MESSAGEQUEUEPUTFORLOTRECOVERY)) {
            //------------------------------------------------------------------------------------------------------------------------------------------------
            // If the queue condition is "postProcID == MessageQueuePutForLotRecovery" and "syncFlag == 1 "(This is inparameter and is not info.syncFlag.),
            // the queue is deleted in synchronous execution normally end.
            // But, when some error occurs, the queue remains. And PostProcessWatchdog performs about the remaining execution queue, the queue is deleted.
            //------------------------------------------------------------------------------------------------------------------------------------------------
            if (CimStringUtils.equals(info.getPostProcessID(), BizConstant.SP_POSTPROCESS_ACTIONID_MESSAGEQUEUEPUTFORLOTRECOVERY)
                    && (BizConstant.SP_POSTPROCESS_SYNCFLAG_SYNC_SEQUENTIAL == syncFlag || BizConstant.SP_POSTPROCESS_SYNCFLAG_SYNC_PARALLEL == syncFlag)) {
                log.info("{} : {}MessageQueuePut ", info.getPostProcessID(), syncFlag);
            } else {
                //--------------------------------------------------------------
                //   Put Message into Message Queue for Full-Auto
                //
                //     objMessageQueue_Put_out&  strMessageQueue_Put_out
                //     const pptObjCommonIn&     strObjCommonIn
                //     const objectIdentifier&   equipmentID
                //     const char *              equipmentMode
                //     const objectIdentifier&   equipmentStatusCode
                //     const objectIdentifier&   lotID
                //     const char *              lotProcessState
                //     const char *              lotHoldState
                //     const objectIdentifier&   cassetteID
                //     const char *              cassetteTransferState
                //     CORBA::Boolean            cassetteTransferReserveFlag
                //     CORBA::Boolean            cassetteDispatchReserveFlag
                //     const objectIdentifier&   durableID
                //     const char *              durableTransferState
                //--------------------------------------------------------------
                if (CimStringUtils.equals(info.getTargetType(), BizConstant.SP_POSTPROCESS_TARGETTYPE_EQP)) {
                    // Call messageQueue_Put
                    Inputs.MessageQueuePutIn messageQueuePutIn = new Inputs.MessageQueuePutIn();
                    messageQueuePutIn.setEquipmentID(equipmentID);
                    messageQueuePutIn.setCassetteTransferReserveFlag(false);
                    messageQueuePutIn.setCassetteDispatchReserveFlag(false);
                    try {
                        messageMethod.messageQueuePut(tmpStrObjCommonIn, messageQueuePutIn);
                    } catch (ServiceException e) {
                        postTaskExecuteReqResult.setOmCode(new OmCode(e.getCode(), e.getMessage()));
                        throw new ServiceException(retCodeConfig.getPostprocError(), postTaskExecuteReqResult);
                    }
                } else if (CimStringUtils.equals(info.getTargetType(), BizConstant.SP_POSTPROCESS_TARGETTYPE_LOT)) {
                    //Check lot state
                    String lotState = null;
                    try {
                        lotState = lotMethod.lotStateGet(objCommon, lotID);
                    } catch (ServiceException e) {
                        postTaskExecuteReqResult.setOmCode(new OmCode(e.getCode(), e.getMessage()));
                        throw new ServiceException(retCodeConfig.getPostprocError(), postTaskExecuteReqResult);
                    }
                    if (CimStringUtils.equals(lotState, CIMStateConst.CIM_LOT_STATE_ACTIVE)) {
                        if (lockHoldUseFlag == 1 || postProcFlagUseFlag == 1) {
                            if (!bFindLockHoldFlag && !bInPostProcessFlag) {
                                postTaskExecuteReqResult.setOmCode(new OmCode(retCodeConfigEx.getLotNotInpostprocess(), ObjectIdentifier.fetchValue(lotID)));
                                throw new ServiceException(retCodeConfig.getPostprocError(), postTaskExecuteReqResult);
                            }
                        }
                        // Set InPostProcessFlag of Lot to OFF
                        lotMethod.lotInPostProcessFlagSet(objCommon, lotID, false);
                        if (bFindLockHoldFlag && (lockHoldUseFlag == 1 || lockHoldUseFlag == 0)) {
                            // Release Lot Hold.
                            Params.HoldLotReleaseReqParams holdLotReleaseReqParams = new Params.HoldLotReleaseReqParams();
                            holdLotReleaseReqParams.setLotID(lotID);
                            holdLotReleaseReqParams.setReleaseReasonCodeID(holdReleaseCode);
                            holdLotReleaseReqParams.setHoldReqList(strHoldListSeq);
                            try {
                                lotService.sxHoldLotReleaseReq(objCommon, holdLotReleaseReqParams);
                            } catch (ServiceException e) {
                                postTaskExecuteReqResult.setOmCode(new OmCode(e.getCode(), e.getMessage()));
                                throw new ServiceException(retCodeConfig.getPostprocError(), postTaskExecuteReqResult);
                            }
                        }
                        //Check lot Hold state
                        String lotHoldState = null;
                        try {
                            lotHoldState = lotMethod.lotHoldStateGet(objCommon, lotID);
                        } catch (ServiceException e) {
                            postTaskExecuteReqResult.setOmCode(new OmCode(e.getCode(), e.getMessage()));
                            throw new ServiceException(retCodeConfig.getPostprocError(), postTaskExecuteReqResult);
                        }
                        // If Lot's Hold status is ONHOLD, messageQueuePut is not performed.
                        if (CimStringUtils.equals(lotHoldState, CIMStateConst.CIM_LOT_HOLD_STATE_NOTONHOLD)) {
                            // NotOnHold
                            // Call messageQueue_Put
                            Inputs.MessageQueuePutIn messageQueuePutIn = new Inputs.MessageQueuePutIn();
                            messageQueuePutIn.setLotID(lotID);
                            messageQueuePutIn.setLotHoldState(CIMStateConst.CIM_LOT_HOLD_STATE_NOTONHOLD);
                            messageQueuePutIn.setCassetteDispatchReserveFlag(false);
                            messageQueuePutIn.setCassetteTransferReserveFlag(false);
                            try {
                                messageMethod.messageQueuePut(tmpStrObjCommonIn, messageQueuePutIn);
                            } catch (ServiceException e) {
                                postTaskExecuteReqResult.setOmCode(new OmCode(e.getCode(), e.getMessage()));
                                throw new ServiceException(retCodeConfig.getPostprocError(), postTaskExecuteReqResult);
                            }
                        }
                        if (lockHoldUseFlag == 1) {
                            // Hold Lot "LOCK".
                            try {
                                lotService.sxHoldLotReq(objCommon, lotID, strHoldListSeq);
                            } catch (ServiceException e) {
                                postTaskExecuteReqResult.setOmCode(new OmCode(e.getCode(), e.getMessage()));
                                throw new ServiceException(retCodeConfig.getPostprocError(), postTaskExecuteReqResult);
                            }
                        }
                        if (postProcFlagUseFlag == 1) {
                            // Set InPostProcessFlag of Lot to ON
                            lotMethod.lotInPostProcessFlagSet(objCommon, lotID, true);
                        }
                    } else {
                        //Inactive
                        bHoldRelease = false;
                    }
                }
            }
        }

        //-------------------------------------------------------------------
        // Call txAPCLotActionReq and txAPCProdActionRpt   TODO Line :1245-1291
        //-------------------------------------------------------------------
        else if (CimStringUtils.equals(info.getPostProcessID(), BizConstant.SP_POSTPROCESS_ACTIONID_APCDISPOSITION)) {

        }
        //----------------------------------------
        // Call txFutureReworkActionDoReq
        //----------------------------------------
        else if (CimStringUtils.equals(info.getPostProcessID(), BizConstant.SP_POSTPROCESS_ACTIONID_FUTUREREWORK)) {
            // Check lot state
            String lotState = null;
            try {
                lotState = lotMethod.lotStateGet(objCommon, lotID);
            } catch (ServiceException e) {
                postTaskExecuteReqResult.setOmCode(new OmCode(e.getCode(), e.getMessage()));
                throw new ServiceException(retCodeConfig.getPostprocError(), postTaskExecuteReqResult);
            }
            if (CimStringUtils.equals(lotState, BizConstant.CIMFW_LOT_STATE_ACTIVE)) {
                // Active
                if (1 == lockHoldUseFlag || 1 == postProcFlagUseFlag) {
                    log.info("Check LockHold & InPostProcessFlag.");
                    if (!bFindLockHoldFlag && !bInPostProcessFlag) {
                        postTaskExecuteReqResult.setOmCode(new OmCode(retCodeConfigEx.getLotNotInpostprocess(), ObjectIdentifier.fetchValue(lotID)));
                        throw new ServiceException(retCodeConfig.getPostprocError(), postTaskExecuteReqResult);
                    }
                }

                // Set InPostProcessFlag of Lot to OFF
                lotMethod.lotInPostProcessFlagSet(objCommon, lotID, false);

                if (bFindLockHoldFlag && (1 == lockHoldUseFlag || 0 == lockHoldUseFlag)) {
                    // Release Lot's Hold "LOCK"
                    log.info("Release Lot's Hold (LOCK)");
                    Params.HoldLotReleaseReqParams holdLotReleaseReqParams = new Params.HoldLotReleaseReqParams();
                    holdLotReleaseReqParams.setLotID(lotID);
                    holdLotReleaseReqParams.setReleaseReasonCodeID(holdReleaseCode);
                    holdLotReleaseReqParams.setHoldReqList(strHoldListSeq);
                    try {
                        lotService.sxHoldLotReleaseReq(objCommon, holdLotReleaseReqParams);
                    } catch (ServiceException e) {
                        postTaskExecuteReqResult.setOmCode(new OmCode(e.getCode(), e.getMessage()));
                        throw new ServiceException(retCodeConfig.getPostprocError(), postTaskExecuteReqResult);
                    }

                }

                // Call txFutureReworkActionDoReq
                log.info("Call txFutureReworkActionDoReq (TargetType:FutureRework)");
                try {
                    processControlService.sxFutureReworkActionDoReq(tmpStrObjCommonIn, lotID, "");
                } catch (ServiceException e) {
                    if (!Validations.isEquals(retCodeConfigEx.getFtrwkNotFound(), e.getCode())) {
                        postTaskExecuteReqResult.setOmCode(new OmCode(e.getCode(), e.getMessage()));
                        throw new ServiceException(retCodeConfig.getPostprocError(), postTaskExecuteReqResult);
                    }
                }

                if (1 == lockHoldUseFlag) {
                    // Hold Lot "LOCK"
                    log.info("Hold Lot (LOCK)");
                    try {
                        lotService.sxHoldLotReq(objCommon, lotID, strHoldListSeq);
                    } catch (ServiceException e) {
                        if (!Validations.isEquals(retCodeConfig.getInvalidLotStat(), e.getCode())) {
                            postTaskExecuteReqResult.setOmCode(new OmCode(e.getCode(), e.getMessage()));
                            throw new ServiceException(retCodeConfig.getPostprocError(), postTaskExecuteReqResult);
                        } else {
                            bHoldRelease = false;
                        }
                    }
                }
                if (1 == postProcFlagUseFlag) {
                    log.info("Post Proc Flag is usable.");

                    // Set InPostProcessFlag of Lot to ON
                    lotMethod.lotInPostProcessFlagSet(objCommon, lotID, true);
                }
            } else {
                log.info("lot_state != Active. - N/A -");
                bHoldRelease = false;
            }
        }
        //----------------------
        // Call UTSQueuePut  TODO Line :1439-1454
        //----------------------
        else if (CimStringUtils.equals(info.getPostProcessID(), BizConstant.SP_POSTPROCESS_ACTIONID_UTSQUEUEPUT)) {

        }

        //---------------------
        // Call txDOCLotActionReq
        //---------------------
        else if (CimStringUtils.equals(info.getPostProcessID(), BizConstant.SP_POSTPROCESS_ACTIONID_FPC)) {
            String lotState = null;
            try {
                lotState = lotMethod.lotStateGet(objCommon, lotID);
            } catch (ServiceException e) {
                postTaskExecuteReqResult.setOmCode(new OmCode(e.getCode(), e.getMessage()));
                throw new ServiceException(retCodeConfig.getPostprocError(), postTaskExecuteReqResult);
            }
            if (CimStringUtils.equals(lotState, CIMStateConst.CIM_LOT_STATE_ACTIVE)) {
                /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/
                /*                                                                       */
                /*   Object Lock Process                                                 */
                /*                                                                       */
                /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/
                //Get lot cassette
                log.info("calling lot_cassette_Get() {}", lotID);
                int retCode = 0;
                ObjectIdentifier getLotCassette = null;
                try {
                    getLotCassette = lotMethod.lotCassetteGet(objCommon, lotID);
                } catch (ServiceException e) {
                    retCode = e.getCode();
                    if (Validations.isEquals(retCodeConfig.getNotFoundCst(), e.getCode())) {
                        //Do nothing..
                    } else {
                        throw e;
                    }
                }
                if (retCode == 0) {
                    Infos.LotLocationInfo cassetteLocationInfo = cassetteMethod.cassetteLocationInfoGetDR(getLotCassette);
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
                            objectLockMethod.advancedObjectLock(objCommon, new Inputs.ObjAdvanceLockIn(cassetteLocationInfo.getEquipmentID(),
                                    BizConstant.SP_CLASSNAME_POSMACHINE,
                                    BizConstant.SP_OBJECTLOCK_OBJECTTYPE_LOADCASSETTE,
                                    (long) BizConstant.SP_OBJECTLOCK_LOCKTYPE_WRITE,
                                    Collections.singletonList(cassetteID.getValue())));
                        }
                    }
                }
                if (1 == lockHoldUseFlag
                        || 1 == postProcFlagUseFlag) {
                    log.info("Check LockHold & InPostProcessFlag.");
                    if (!bFindLockHoldFlag && !bInPostProcessFlag) {
                        postTaskExecuteReqResult.setOmCode(new OmCode(retCodeConfigEx.getLotNotInpostprocess(), ObjectIdentifier.fetchValue(lotID)));
                        throw new ServiceException(retCodeConfig.getPostprocError(), postTaskExecuteReqResult);
                    }
                }
                Inputs.ObjLotInPostProcessFlagSetIn in = new Inputs.ObjLotInPostProcessFlagSetIn();
                in.setLotID(lotID);
                in.setInPostProcessFlag(false);
                lotMethod.lotInPostProcessFlagSet(objCommon, lotID, false);
                if (bFindLockHoldFlag && (1 == lockHoldUseFlag || 0 == lockHoldUseFlag)) {
                    // Release Lot Hold.
                    log.info(" Release Lot's Hold(LOCK) ");
                    Params.HoldLotReleaseReqParams holdLotReleaseReqParams = new Params.HoldLotReleaseReqParams();
                    holdLotReleaseReqParams.setLotID(lotID);
                    holdLotReleaseReqParams.setReleaseReasonCodeID(holdReleaseCode);
                    holdLotReleaseReqParams.setHoldReqList(strHoldListSeq);
                    try {
                        lotService.sxHoldLotReleaseReq(objCommon, holdLotReleaseReqParams);
                    } catch (ServiceException e) {
                        postTaskExecuteReqResult.setOmCode(new OmCode(e.getCode(), e.getMessage()));
                        throw new ServiceException(retCodeConfig.getPostprocError(), postTaskExecuteReqResult);
                    }
                }
                // Store TriggerDKey as thread specific data
                if (1 == ppChainMode) {
                    log.info("ppChainMode=1, Store TriggerDKey");
                    ThreadContextHolder.setThreadSpecificDataString(BizConstant.SP_THREADSPECIFICDATA_KEY_TRIGGERDKEY, info.getDKey());
                }
                // Call txDOCLotActionReq
                log.info("Call txDOCLotActionReq (TargetType:DOC)");
                try {
                    dynamicOperationService.sxDOCLotActionReq(tmpStrObjCommonIn, lotID);
                } catch (ServiceException e) {
                    if (Validations.isEquals(retCodeConfig.getPostrpocDkeyRecreate(), e.getCode())) {
                        rc_pp = e.getCode();
                    } else {
                        postTaskExecuteReqResult.setOmCode(new OmCode(e.getCode(), e.getMessage()));
                        throw new ServiceException(retCodeConfig.getPostprocError(), postTaskExecuteReqResult);
                    }
                }

                // remove TriggerDKey from thread specific data (set blank)
                if (1 == ppChainMode) {
                    log.info("ppChainMode=1, clear TriggerDKey");
                    ThreadContextHolder.removeDataString(BizConstant.SP_THREADSPECIFICDATA_KEY_TRIGGERDKEY);
                }
                if (1 == lockHoldUseFlag) {
                    // Hold Lot "LOCK"
                    log.info(" Hold Lot. (LOCK)");
                    try {
                        lotService.sxHoldLotReq(objCommon, lotID, strHoldListSeq);
                    } catch (ServiceException e) {
                        if (!Validations.isEquals(retCodeConfig.getInvalidLotStat(), e.getCode())) {
                            log.error("sxHoldLotReq() != ok");
                            postTaskExecuteReqResult.setOmCode(new OmCode(e.getCode(), e.getMessage()));
                            throw new ServiceException(retCodeConfig.getPostprocError(), postTaskExecuteReqResult);
                        } else {
                            bHoldRelease = false;
                        }
                    }
                }
                if (1 == postProcFlagUseFlag) {
                    log.info("Post Proc Flag is usable.");
                    //Check lot state
                    String lotState1 = null;
                    try {
                        lotState1 = lotMethod.lotStateGet(objCommon, lotID);
                    } catch (ServiceException e) {
                        postTaskExecuteReqResult.setOmCode(new OmCode(e.getCode(), e.getMessage()));
                        throw new ServiceException(retCodeConfig.getPostprocError(), postTaskExecuteReqResult);
                    }
                    if (CimStringUtils.equals(lotState1, CIMStateConst.CIM_LOT_STATE_ACTIVE)) {
                        // Active
                        log.info("lot_state = Active.");
                        // Set InPostProcessFlag of Lot to ON
                        lotMethod.lotInPostProcessFlagSet(objCommon, lotID, true);
                    }
                }
            } else {
                //N/A
                log.info("lot_state != Active. - N/A -");
                bHoldRelease = false;
            }
        }

        //------------------------------------------
        // Call txEDCWithSpecCheckActionByPostTaskReq
        //------------------------------------------
        else if (CimStringUtils.equals(info.getPostProcessID(), BizConstant.SP_POSTPROCESS_ACTIONID_COLLECTEDDATAACTION)) {
            //-------------------------------------------------
            //   Make Event for Collected Data History
            //-------------------------------------------------
            List<Infos.StartCassette> tmpStartCassette = null;
            Outputs.ObjLotPreviousOperationDataCollectionInformationGetOut lotPreviousOperationDataCollectionInformationGetOut = null;
            try {
                lotPreviousOperationDataCollectionInformationGetOut = lotMethod.lotPreviousOperationDataCollectionInformationGet(objCommon, equipmentID, Arrays.asList(lotID));
            } catch (ServiceException e) {
                postTaskExecuteReqResult.setOmCode(new OmCode(e.getCode(), e.getMessage()));
                throw new ServiceException(retCodeConfig.getPostprocError(), postTaskExecuteReqResult);
            }
            tmpStartCassette = null == lotPreviousOperationDataCollectionInformationGetOut ? null : lotPreviousOperationDataCollectionInformationGetOut.getStrStartCassette();
            Validations.check(CimArrayUtils.isEmpty(tmpStartCassette), retCodeConfig.getNotFoundCassette(), "*****");
            try {
                eventMethod.collectedDataEventForPreviousOperationMake(objCommon,
                        TransactionIDEnum.POST_PROCESS_EXEC_REQ.getValue(),
                        tmpStartCassette,
                        controlJobID,
                        equipmentID,
                        postTaskExecuteReqParams.getClaimMemo());
            } catch (ServiceException e) {
                postTaskExecuteReqResult.setOmCode(new OmCode(e.getCode(), e.getMessage()));
                throw new ServiceException(retCodeConfig.getPostprocError(), postTaskExecuteReqResult);
            }
            //Check lot state
            String lotState = null;
            try {
                lotState = lotMethod.lotStateGet(objCommon, lotID);
            } catch (ServiceException e) {
                postTaskExecuteReqResult.setOmCode(new OmCode(e.getCode(), e.getMessage()));
                throw new ServiceException(retCodeConfig.getPostprocError(), postTaskExecuteReqResult);
            }
            if (CimStringUtils.equals(lotState, CIMStateConst.CIM_LOT_STATE_ACTIVE)) {
                // Active
                if (lockHoldUseFlag == 1 || 1 == postProcFlagUseFlag) {
                    if (!bFindLockHoldFlag && !bInPostProcessFlag) {
                        postTaskExecuteReqResult.setOmCode(new OmCode(retCodeConfigEx.getLotNotInpostprocess(), ObjectIdentifier.fetchValue(lotID)));
                        throw new ServiceException(retCodeConfig.getPostprocError(), postTaskExecuteReqResult);
                    }
                }
                // Set InPostProcessFlag of Lot to OFF
                lotMethod.lotInPostProcessFlagSet(objCommon, lotID, false);
                if (bFindLockHoldFlag && (1 == lockHoldUseFlag || 0 == lockHoldUseFlag)) {
                    // Release Lot Hold.
                    Params.HoldLotReleaseReqParams holdLotReleaseReqParams = new Params.HoldLotReleaseReqParams();
                    holdLotReleaseReqParams.setLotID(lotID);
                    holdLotReleaseReqParams.setHoldReqList(strHoldListSeq);
                    holdLotReleaseReqParams.setReleaseReasonCodeID(holdReleaseCode);
                    try {
                        lotService.sxHoldLotReleaseReq(objCommon, holdLotReleaseReqParams);
                    } catch (ServiceException e) {
                        postTaskExecuteReqResult.setOmCode(new OmCode(e.getCode(), e.getMessage()));
                        throw new ServiceException(retCodeConfig.getPostprocError(), postTaskExecuteReqResult);
                    }
                }
                //-----------------------------------------------------------------
                // Call sxEDCWithSpecCheckActionByPostTaskReqParams
                //-----------------------------------------------------------------
                Params.EDCWithSpecCheckActionByPostTaskReqParams edcWithSpecCheckActionByPostTaskReqParams = new Params.EDCWithSpecCheckActionByPostTaskReqParams();
                edcWithSpecCheckActionByPostTaskReqParams.setEquipmentID(equipmentID);
                edcWithSpecCheckActionByPostTaskReqParams.setControlJobID(controlJobID);
                edcWithSpecCheckActionByPostTaskReqParams.setLotID(lotID);
                Results.CollectedDataActionByPostProcReqResult collectedDataActionByPostProcReqResult = null;
                try {
                    collectedDataActionByPostProcReqResult = engineerDataCollectionService.sxEDCWithSpecCheckActionByPostTaskReq(tmpStrObjCommonIn, edcWithSpecCheckActionByPostTaskReqParams);
                } catch (ServiceException e) {
                    postTaskExecuteReqResult.setOmCode(new OmCode(e.getCode(), e.getMessage()));
                    throw new ServiceException(retCodeConfig.getPostprocError(), postTaskExecuteReqResult);
                }
                if (null != collectedDataActionByPostProcReqResult) {
                    postTaskExecuteReqResult.setRelatedQueueKey(collectedDataActionByPostProcReqResult.getRelatedQueuekey());
                    if (null != collectedDataActionByPostProcReqResult.getMoveOutReqResult()) {
                        postTaskExecuteReqResult.setStrLotSpecSPCCheckResultSeq(collectedDataActionByPostProcReqResult.getMoveOutReqResult().getMoveOutLot());
                    }
                }
                if (1 == lockHoldUseFlag) {
                    //Hold Lot "LOCK"
                    log.info(" Hold Lot. (LOCK)");
                    try {
                        lotService.sxHoldLotReq(objCommon, lotID, strHoldListSeq);
                    } catch (ServiceException e) {
                        if (!Validations.isEquals(retCodeConfig.getInvalidLotStat(), e.getCode())) {
                            log.error("txHoldLotReq() != RC_OK returned error.");
                            postTaskExecuteReqResult.setOmCode(new OmCode(e.getCode(), e.getMessage()));
                            throw new ServiceException(retCodeConfig.getPostprocError(), postTaskExecuteReqResult);
                        } else {
                            log.info("rc == RC_INVALID_LOT_STAT.");
                            bHoldRelease = false;
                        }
                    }
                }
                if (1 == postProcFlagUseFlag) {
                    log.info("Post Proc Flag is usable.");
                    //Set InPostProcessFlag of Lot to ON
                    lotMethod.lotInPostProcessFlagSet(objCommon, lotID, true);
                }
            } else {
                bHoldRelease = false;
            }
        }

        //----------------------------------------
        // Call txQtimeManageActionByPostTaskReq
        //----------------------------------------
        else if (CimStringUtils.equals(info.getPostProcessID(), BizConstant.SP_POSTPROCESS_ACTIONID_QTIME)) {
            List<Infos.PostProcessAdditionalInfo> postProcessAdditionalInfos = postProcessMethod.postProcessAdditionalInfoGetDR(objCommon, info.getDKey(), info.getSequenceNumber());
            for (Infos.PostProcessAdditionalInfo postProcessAdditionalInfo : postProcessAdditionalInfos) {
                if (CimStringUtils.equals(postProcessAdditionalInfo.getName(), BizConstant.SP_THREADSPECIFICDATA_KEY_PREVIOUSBRANCHINFO)) {
                    // Store PreviousBranchInfo as thread specific data
                    ThreadContextHolder.setThreadSpecificDataString(BizConstant.SP_THREADSPECIFICDATA_KEY_PREVIOUSBRANCHINFO, postProcessAdditionalInfo.getValue());
                } else if (CimStringUtils.equals(postProcessAdditionalInfo.getName(), BizConstant.SP_THREADSPECIFICDATA_KEY_PREVIOUSRETURNINFO)) {
                    // Store PreviousReturnInfo as thread specific data
                    ThreadContextHolder.setThreadSpecificDataString(BizConstant.SP_THREADSPECIFICDATA_KEY_PREVIOUSRETURNINFO, postProcessAdditionalInfo.getValue());
                } else if (CimStringUtils.equals(postProcessAdditionalInfo.getName(), BizConstant.SP_THREADSPECIFICDATA_KEY_PREVIOUSREWORKOUTKEY)) {
                    // Store PreviousReworkOutKey as thread specific data
                    ThreadContextHolder.setThreadSpecificDataString(BizConstant.SP_THREADSPECIFICDATA_KEY_PREVIOUSREWORKOUTKEY, postProcessAdditionalInfo.getValue());
                }
            }
            //Check lot state
            String lotState = null;
            try {
                lotState = lotMethod.lotStateGet(objCommon, lotID);
            } catch (ServiceException e) {
                postTaskExecuteReqResult.setOmCode(new OmCode(e.getCode(), e.getMessage()));
                throw new ServiceException(retCodeConfig.getPostprocError(), postTaskExecuteReqResult);
            }
            if (CimStringUtils.equals(lotState, CIMStateConst.CIM_LOT_STATE_ACTIVE)) {
                // Active
                if (1 == lockHoldUseFlag || 1 == postProcFlagUseFlag) {
                    if (!bFindLockHoldFlag && !bInPostProcessFlag) {
                        postTaskExecuteReqResult.setOmCode(new OmCode(retCodeConfigEx.getLotNotInpostprocess(), ObjectIdentifier.fetchValue(lotID)));
                        throw new ServiceException(retCodeConfig.getPostprocError(), postTaskExecuteReqResult);
                    }
                }
                // Set InPostProcessFlag of Lot to OFF
                lotMethod.lotInPostProcessFlagSet(objCommon, lotID, false);
                if (bFindLockHoldFlag && (lockHoldUseFlag == 1 || lockHoldUseFlag == 0)) {
                    // Release Lot Hold.
                    Params.HoldLotReleaseReqParams holdLotReleaseReqParams = new Params.HoldLotReleaseReqParams();
                    holdLotReleaseReqParams.setLotID(lotID);
                    holdLotReleaseReqParams.setHoldReqList(strHoldListSeq);
                    holdLotReleaseReqParams.setReleaseReasonCodeID(holdReleaseCode);
                    try {
                        lotService.sxHoldLotReleaseReq(objCommon, holdLotReleaseReqParams);
                    } catch (ServiceException e) {
                        postTaskExecuteReqResult.setOmCode(new OmCode(e.getCode(), e.getMessage()));
                        throw new ServiceException(retCodeConfig.getPostprocError(), postTaskExecuteReqResult);
                    }
                }
                //-----------------------------------------------------------------
                // Call txQtimeManageActionByPostTaskReq
                //-----------------------------------------------------------------
                Params.QtimeManageActionByPostTaskReqInParm strQtimeManageActionByPostTaskReqInParm = new Params.QtimeManageActionByPostTaskReqInParm();
                strQtimeManageActionByPostTaskReqInParm.setLotID(lotID);
                try {
                    processControlService.sxQtimeManageActionByPostTaskReq(tmpStrObjCommonIn, strQtimeManageActionByPostTaskReqInParm, postTaskExecuteReqParams.getClaimMemo());
                } catch (ServiceException e) {
                    postTaskExecuteReqResult.setOmCode(new OmCode(e.getCode(), e.getMessage()));
                    throw new ServiceException(retCodeConfig.getPostprocError(), postTaskExecuteReqResult);
                }
                if (lockHoldUseFlag == 1) {
                    // Hold Lot "LOCK".
                    try {
                        lotService.sxHoldLotReq(objCommon, lotID, strHoldListSeq);
                    } catch (ServiceException e) {
                        if (!Validations.isEquals(retCodeConfig.getInvalidLotStat(), e.getCode())) {
                            postTaskExecuteReqResult.setOmCode(new OmCode(e.getCode(), e.getMessage()));
                            throw new ServiceException(retCodeConfig.getPostprocError(), postTaskExecuteReqResult);
                        } else {
                            bHoldRelease = false;
                        }
                    }
                }
                if (postProcFlagUseFlag == 1) {
                    // Set InPostProcessFlag of Lot to ON
                    lotMethod.lotInPostProcessFlagSet(objCommon, lotID, true);
                }
            } else {
                //Inactive
                bHoldRelease = false;
            }
        }
        //----------------------------------------
        // Call txLagTimeActionReq
        //----------------------------------------
        else if (CimStringUtils.equals(info.getPostProcessID(), BizConstant.SP_POSTPROCESS_ACTIONID_PROCESSLAGTIME)) {
            //Check lot state
            String lotState = null;
            try {
                lotState = lotMethod.lotStateGet(objCommon, lotID);
            } catch (ServiceException e) {
                postTaskExecuteReqResult.setOmCode(new OmCode(e.getCode(), e.getMessage()));
                throw new ServiceException(retCodeConfig.getPostprocError(), postTaskExecuteReqResult);
            }
            if (CimStringUtils.equals(lotState, CIMStateConst.CIM_LOT_STATE_ACTIVE)) {
                // Active
                if (lockHoldUseFlag == 1 || postProcFlagUseFlag == 1) {
                    if (!bFindLockHoldFlag && !bInPostProcessFlag) {
                        postTaskExecuteReqResult.setOmCode(new OmCode(retCodeConfigEx.getLotNotInpostprocess(), ObjectIdentifier.fetchValue(lotID)));
                        throw new ServiceException(new OmCode(retCodeConfig.getPostprocError(), lotID.getValue()), postTaskExecuteReqResult);
                    }
                }
                // Set InPostProcessFlag of Lot to OFF
                lotMethod.lotInPostProcessFlagSet(objCommon, lotID, false);
                if (bFindLockHoldFlag && (lockHoldUseFlag == 1 || lockHoldUseFlag == 0)) {
                    // Release Lot Hold.
                    Params.HoldLotReleaseReqParams holdLotReleaseReqParams = new Params.HoldLotReleaseReqParams();
                    holdLotReleaseReqParams.setLotID(lotID);
                    holdLotReleaseReqParams.setHoldReqList(strHoldListSeq);
                    holdLotReleaseReqParams.setReleaseReasonCodeID(holdReleaseCode);
                    try {
                        lotService.sxHoldLotReleaseReq(objCommon, holdLotReleaseReqParams);
                    } catch (ServiceException e) {
                        postTaskExecuteReqResult.setOmCode(new OmCode(e.getCode(), e.getMessage()));
                        throw new ServiceException(retCodeConfig.getPostprocError(), postTaskExecuteReqResult);
                    }
                }
                //-----------------------------------------------------------------
                // Call txLagTimeActionReq
                //-----------------------------------------------------------------
                Params.LagTimeActionReqParams lagTimeActionReqParams = new Params.LagTimeActionReqParams();
                lagTimeActionReqParams.setLotID(lotID);
                lagTimeActionReqParams.setAction(BizConstant.SP_PROCESSLAGTIME_ACTION_SET);
                lagTimeActionReqParams.setClaimMemo(postTaskExecuteReqParams.getClaimMemo());
                try {
                    processControlService.sxProcessLagTimeUpdate(objCommon, lagTimeActionReqParams);
                } catch (ServiceException e) {
                    postTaskExecuteReqResult.setOmCode(new OmCode(e.getCode(), e.getMessage()));
                    throw new ServiceException(retCodeConfig.getPostprocError(), postTaskExecuteReqResult);
                }
                if (lockHoldUseFlag == 1) {
                    // Hold Lot "LOCK".
                    try {
                        lotService.sxHoldLotReq(objCommon, lotID, strHoldListSeq);
                    } catch (ServiceException e) {
                        if (!Validations.isEquals(retCodeConfig.getInvalidLotStat(), e.getCode())) {
                            postTaskExecuteReqResult.setOmCode(new OmCode(e.getCode(), e.getMessage()));
                            throw new ServiceException(retCodeConfig.getPostprocError(), postTaskExecuteReqResult);
                        } else {
                            bHoldRelease = false;
                        }
                    }
                }
                if (postProcFlagUseFlag == 1) {
                    // Set InPostProcessFlag of Lot to ON
                    lotMethod.lotInPostProcessFlagSet(objCommon, lotID, true);
                }
            } else {
                bHoldRelease = false;
            }
        }
        //----------------------------------------
        // TODO :
        // Call txFutureHoldPostTypeByPostTaskReq,txLotPlanChangeReserveDoActionByPostTaskReq
        // Line 2237-2484
        //----------------------------------------
        else if (CimStringUtils.equals(info.getPostProcessID(), BizConstant.SP_POSTPROCESS_ACTIONID_FUTUREHOLDPOST)) {

        } else if (CimStringUtils.equals(info.getPostProcessID(), BizConstant.SP_POSTPROCESS_ACTIONID_SCHEXEC)) {

        }

        //----------------------------------------
        // Call txMonitorHoldDoActionByPostTaskReq
        //----------------------------------------
        else if (CimStringUtils.equals(BizConstant.SP_POSTPROCESS_ACTIONID_MONITOREDLOTHOLD, info.getPostProcessID())) {
            //Check lot state
            String lotState = null;
            try {
                lotState = lotMethod.lotStateGet(objCommon, lotID);
            } catch (ServiceException e) {
                postTaskExecuteReqResult.setOmCode(new OmCode(e.getCode(), e.getMessage()));
                throw new ServiceException(retCodeConfig.getPostprocError(), postTaskExecuteReqResult);
            }
            if (CimStringUtils.equals(CIMStateConst.CIM_LOT_STATE_ACTIVE, lotState)) {
                //Active
                if (lockHoldUseFlag == 1 || 1 == postProcFlagUseFlag) {
                    log.info("Check LockHold & InPostProcessFlag.");
                    if (!bFindLockHoldFlag && !bInPostProcessFlag) {
                        postTaskExecuteReqResult.setOmCode(new OmCode(retCodeConfigEx.getLotNotInpostprocess(), ObjectIdentifier.fetchValue(lotID)));
                        throw new ServiceException(retCodeConfig.getPostprocError(), postTaskExecuteReqResult);
                    }
                }
                //Set InPostProcessFlag of Lot to OFF
                lotMethod.lotInPostProcessFlagSet(objCommon, lotID, false);
                if (CimBooleanUtils.isTrue(bFindLockHoldFlag) && (1 == lockHoldUseFlag || 0 == lockHoldUseFlag)) {
                    //Release Lot Hold
                    log.info("Release Lot's Hold (LOCK)");
                    Params.HoldLotReleaseReqParams holdLotReleaseReqParams = new Params.HoldLotReleaseReqParams();
                    holdLotReleaseReqParams.setReleaseReasonCodeID(holdReleaseCode);
                    holdLotReleaseReqParams.setLotID(lotID);
                    holdLotReleaseReqParams.setHoldReqList(strHoldListSeq);
                    holdLotReleaseReqParams.setUser(objCommon.getUser());
                    try {
                        lotService.sxHoldLotReleaseReq(objCommon, holdLotReleaseReqParams);
                    } catch (ServiceException e) {
                        postTaskExecuteReqResult.setOmCode(new OmCode(e.getCode(), e.getMessage()));
                        throw new ServiceException(retCodeConfig.getPostprocError(), postTaskExecuteReqResult);
                    }
                }
                //Call txMonitorHoldDoActionByPostTaskReq
                log.info("Call txMonitorHoldDoActionByPostTaskReq (TargetType:MonitoredLotHold)");
                try {
                    processMonitorService.sxMonitorHoldDoActionByPostTaskReq(tmpStrObjCommonIn, lotID);
                } catch (ServiceException e) {
                    postTaskExecuteReqResult.setOmCode(new OmCode(e.getCode(), e.getMessage()));
                    throw new ServiceException(retCodeConfig.getPostprocError(), postTaskExecuteReqResult);
                }
                if (1 == lockHoldUseFlag) {
                    //Hold Lot "LOCK"
                    log.info(" Hold Lot. (LOCK)");
                    try {
                        lotService.sxHoldLotReq(objCommon, lotID, strHoldListSeq);
                    } catch (ServiceException e) {
                        if (!Validations.isEquals(retCodeConfig.getInvalidLotStat(), e.getCode())) {
                            log.error("txHoldLotReq() != RC_OK returned error.");
                            postTaskExecuteReqResult.setOmCode(new OmCode(e.getCode(), e.getMessage()));
                            throw new ServiceException(retCodeConfig.getPostprocError(), postTaskExecuteReqResult);
                        } else {
                            log.info("rc == RC_INVALID_LOT_STAT.");
                            bHoldRelease = false;
                        }
                    }
                }
                if (1 == postProcFlagUseFlag) {
                    log.info("Post Proc Flag is usable.");
                    //Set InPostProcessFlag of Lot to ON
                    lotMethod.lotInPostProcessFlagSet(objCommon, lotID, true);
                }
            } else {
                //Inactive
                //N/A
                log.info("lot_state != Active. - N/A -");
                bHoldRelease = false;
            }
        }
        //----------------------------------------
        // Call txFutureHoldPreTypeByPostActionReq TODO Line : 2615-2736
        //----------------------------------------
        else if (CimStringUtils.equals(info.getPostProcessID(), BizConstant.SP_POSTPROCESS_ACTIONID_FUTUREHOLDPRE)) {

        }

        //----------------------------------------
        // Call txProcessHoldDoActionReq
        //----------------------------------------
        else if (CimStringUtils.equals(info.getPostProcessID(), BizConstant.SP_POSTPROCESS_ACTIONID_PROCESSHOLD)) {
            //Check lot state
            String lotState = null;
            try {
                lotState = lotMethod.lotStateGet(objCommon, lotID);
            } catch (ServiceException e) {
                postTaskExecuteReqResult.setOmCode(new OmCode(e.getCode(), e.getMessage()));
                throw new ServiceException(retCodeConfig.getPostprocError(), postTaskExecuteReqResult);
            }
            if (CimStringUtils.equals(lotState, CIMStateConst.CIM_LOT_STATE_ACTIVE)) {
                // Active
                if (lockHoldUseFlag == 1 || 1 == postProcFlagUseFlag) {
                    if (!bFindLockHoldFlag && !bInPostProcessFlag) {
                        postTaskExecuteReqResult.setOmCode(new OmCode(retCodeConfigEx.getLotNotInpostprocess(), ObjectIdentifier.fetchValue(lotID)));
                        throw new ServiceException(retCodeConfig.getPostprocError(), postTaskExecuteReqResult);
                    }
                }
                // Set InPostProcessFlag of Lot to OFF
                lotMethod.lotInPostProcessFlagSet(objCommon, lotID, false);
                if (bFindLockHoldFlag && (1 == lockHoldUseFlag || 0 == lockHoldUseFlag)) {
                    // Release Lot Hold.
                    Params.HoldLotReleaseReqParams holdLotReleaseReqParams = new Params.HoldLotReleaseReqParams();
                    holdLotReleaseReqParams.setLotID(lotID);
                    holdLotReleaseReqParams.setHoldReqList(strHoldListSeq);
                    holdLotReleaseReqParams.setReleaseReasonCodeID(holdReleaseCode);
                    try {
                        lotService.sxHoldLotReleaseReq(objCommon, holdLotReleaseReqParams);
                    } catch (ServiceException e) {
                        postTaskExecuteReqResult.setOmCode(new OmCode(e.getCode(), e.getMessage()));
                        throw new ServiceException(retCodeConfig.getPostprocError(), postTaskExecuteReqResult);
                    }
                }
                //-----------------------------------------------------------------
                // Call txProcessHoldDoActionReq
                //-----------------------------------------------------------------
                try {
                    processControlService.sxProcessHoldDoActionReq(tmpStrObjCommonIn, lotID, postTaskExecuteReqParams.getClaimMemo());
                } catch (ServiceException e) {
                    postTaskExecuteReqResult.setOmCode(new OmCode(e.getCode(), e.getMessage()));
                    throw new ServiceException(retCodeConfig.getPostprocError(), postTaskExecuteReqResult);
                }
                if (1 == lockHoldUseFlag) {
                    //Hold Lot "LOCK"
                    log.info(" Hold Lot. (LOCK)");
                    try {
                        lotService.sxHoldLotReq(objCommon, lotID, strHoldListSeq);
                    } catch (ServiceException e) {
                        if (!Validations.isEquals(retCodeConfig.getInvalidLotStat(), e.getCode())) {
                            log.error("txHoldLotReq() != RC_OK returned error.");
                            postTaskExecuteReqResult.setOmCode(new OmCode(e.getCode(), e.getMessage()));
                            throw new ServiceException(retCodeConfig.getPostprocError(), postTaskExecuteReqResult);
                        } else {
                            log.info("rc == RC_INVALID_LOT_STAT.");
                            bHoldRelease = false;
                        }
                    }
                }
                if (1 == postProcFlagUseFlag) {
                    log.info("Post Proc Flag is usable.");
                    //Set InPostProcessFlag of Lot to ON
                    lotMethod.lotInPostProcessFlagSet(objCommon, lotID, true);
                }
            } else {
                bHoldRelease = false;
            }
        }
        //----------------------------------------
        // TODO : Line 2865- 3253
        // Call txBankInByPostTaskReq,txExternalPostTaskExecuteReq,txInterFabXferReserveReq
        //----------------------------------------
        else if (CimStringUtils.equals(info.getPostProcessID(), BizConstant.SP_POSTPROCESS_ACTIONID_AUTOBANKIN)) {

        } else if (CimStringUtils.equals(info.getPostProcessID(), BizConstant.SP_POSTPROCESS_ACTIONID_EXTERNALPOSTPROCESSEXECREQ)) {

        } else if (CimStringUtils.equals(info.getPostProcessID(), BizConstant.SP_POSTPROCESS_ACTIONID_INTERFABXFER)) {

        } else if (CimStringUtils.equals(info.getPostProcessID(), BizConstant.SP_POSTPROCESS_ACTIONID_WAFERSTACKING)) {
            //--------------------------------------------------------
            //   Call txWaferStackingReq for each Bonding Group
            //--------------------------------------------------------
            Outputs.ObjBondingGroupInfoByEqpGetDROut objBondingGroupInfoByEqpGetDROut = bondingGroupMethod.bondingGroupInfoByEqpGetDR(objCommon, equipmentID, controlJobID, false);
            List<Infos.BondingGroupInfo> bondingGroupInfoList = objBondingGroupInfoByEqpGetDROut.getBondingGroupInfoList();
            if (!CimArrayUtils.isEmpty(bondingGroupInfoList)) {
                for (Infos.BondingGroupInfo bondingGroupInfo : bondingGroupInfoList) {
                    Params.WaferStackingReqInParams waferStackingReqInParams = new Params.WaferStackingReqInParams();
                    waferStackingReqInParams.setBondingGroupID(bondingGroupInfo.getBondingGroupID());
                    try {
                        bondService.sxWaferStackingReq(objCommon, waferStackingReqInParams);
                    } catch (ServiceException e) {
                        postTaskExecuteReqResult.setOmCode(new OmCode(e.getCode(), e.getMessage()));
                        throw new ServiceException(retCodeConfig.getPostprocError(), postTaskExecuteReqResult);
                    }
                }
            }

        }
        else if (CimStringUtils.equals(info.getPostProcessID(), BizConstant.SP_POSTPROCESS_ACTIONID_PODELQUEUEPUT)) {
            int envEventCreateType = StandardProperties.OM_MAINT_PO_EVENT_CREATE_TYPE.getIntValue();
            if (envEventCreateType == BizConstant.SP_POMAINTEVENTCREATETYPE_ACTIVELOTENABLED
                    || envEventCreateType == BizConstant.SP_POMAINTEVENTCREATETYPE_ENABLED) {
                //--------------------------------------------------------
                //   Put Event Queue for PO Maintenance
                //--------------------------------------------------------
                try {
                    processMethod.poDelQueuePutDR(tmpStrObjCommonIn, lotID);
                } catch (ServiceException e) {
                    postTaskExecuteReqResult.setOmCode(new OmCode(e.getCode(), e.getMessage()));
                    throw new ServiceException(retCodeConfig.getPostprocError(), postTaskExecuteReqResult);
                }
            }
        }
        else if (CimStringUtils.equals(info.getPostProcessID(), BizConstant.SP_POSTPROCESS_ACTIONID_PARTIALCOMPLOTHOLD)) {
            //Check lot state
            String lotState = null;
            try {
                lotState = lotMethod.lotStateGet(objCommon, lotID);
            } catch (ServiceException e) {
                postTaskExecuteReqResult.setOmCode(new OmCode(e.getCode(), e.getMessage()));
                throw new ServiceException(retCodeConfig.getPostprocError(), postTaskExecuteReqResult);
            }
            if (CimStringUtils.equals(lotState, CIMStateConst.CIM_LOT_STATE_ACTIVE)) {
                // Active
                if (lockHoldUseFlag == 1 || 1 == postProcFlagUseFlag) {
                    if (!bFindLockHoldFlag && !bInPostProcessFlag) {
                        postTaskExecuteReqResult.setOmCode(new OmCode(retCodeConfigEx.getLotNotInpostprocess(), ObjectIdentifier.fetchValue(lotID)));
                        throw new ServiceException(retCodeConfig.getPostprocError(), postTaskExecuteReqResult);
                    }
                }
                // Set InPostProcessFlag of Lot to OFF
                lotMethod.lotInPostProcessFlagSet(objCommon, lotID, false);
                if (bFindLockHoldFlag && (1 == lockHoldUseFlag || 0 == lockHoldUseFlag)) {
                    // Release Lot Hold.
                    Params.HoldLotReleaseReqParams holdLotReleaseReqParams = new Params.HoldLotReleaseReqParams();
                    holdLotReleaseReqParams.setLotID(lotID);
                    holdLotReleaseReqParams.setHoldReqList(strHoldListSeq);
                    holdLotReleaseReqParams.setReleaseReasonCodeID(holdReleaseCode);
                    try {
                        lotService.sxHoldLotReleaseReq(objCommon, holdLotReleaseReqParams);
                    } catch (ServiceException e) {
                        postTaskExecuteReqResult.setOmCode(new OmCode(e.getCode(), e.getMessage()));
                        throw new ServiceException(retCodeConfig.getPostprocError(), postTaskExecuteReqResult);
                    }
                }
                //-----------------------------------------------------------------
                // Check if hold target lot is OpeComped lot or OpeStartCancelled lot
                //-----------------------------------------------------------------
                boolean previousPOFlag = false;
                ObjectIdentifier dummyLotID = null;
                Inputs.ObjControlJobProcessOperationListGetDRIn objControlJobProcessOperationListGetDRIn = new Inputs.ObjControlJobProcessOperationListGetDRIn();
                objControlJobProcessOperationListGetDRIn.setLotID(dummyLotID);
                objControlJobProcessOperationListGetDRIn.setControlJobID(controlJobID);
                List<Infos.ProcessOperationLot> processOperationLots = null;
                try {
                    processOperationLots = controlJobMethod.controlJobProcessOperationListGetDR(objCommon, objControlJobProcessOperationListGetDRIn);
                } catch (ServiceException e) {
                    if (Validations.isEquals(retCodeConfig.getNotFoundLotInControlJob(), e.getCode())) {
                        // There is no PO which have input control job -> lot is OpeStartCancelled
                        // Get PO from Current PO
                        previousPOFlag = false;
                    } else {
                        throw e;
                    }
                }
                int poLen = CimArrayUtils.getSize(processOperationLots);
                int poCnt = 0;
                for (poCnt = 0; poCnt < poLen; poCnt++) {
                    Infos.ProcessOperationLot processOperationLot = processOperationLots.get(poCnt);
                    if (ObjectIdentifier.equalsWithValue(lotID, processOperationLot.getLotID())) {
                        // PO which have input control job and lot ID is found
                        previousPOFlag = true;
                    }
                }
                //-----------------------------------------------------------------
                // Call txHoldLotReq
                //-----------------------------------------------------------------
                List<Infos.LotHoldReq> strHoldListSeq2 = new ArrayList<>();
                if (previousPOFlag) {
                    Outputs.ObjLotPreviousOperationInfoGetOut objLotPreviousOperationInfoGetOut = lotMethod.lotPreviousOperationInfoGet(objCommon, lotID);
                    Infos.LotHoldReq holdList = new Infos.LotHoldReq();
                    strHoldListSeq2.add(holdList);
                    holdList.setHoldType(BizConstant.SP_HOLDTYPE_LOTHOLD);
                    holdList.setHoldReasonCodeID(new ObjectIdentifier(BizConstant.SP_REASON_PARTIALOPECOMPHOLD));
                    holdList.setHoldUserID(objCommon.getUser().getUserID());
                    holdList.setResponsibleOperationMark(BizConstant.SP_RESPONSIBLEOPERATION_PREVIOUS);
                    holdList.setRouteID(objLotPreviousOperationInfoGetOut.getRouteID());
                    holdList.setOperationNumber(objLotPreviousOperationInfoGetOut.getOperationNumber());
                    holdList.setClaimMemo("");
                } else {
                    //-----------------------------------------------------------------
                    // Call txHoldLotReq
                    //-----------------------------------------------------------------
                    Outputs.ObjLotCurrentOperationInfoGetOut objLotCurrentOperationInfoGetOut = lotMethod.lotCurrentOperationInfoGet(objCommon, lotID);
                    Infos.LotHoldReq holdList = new Infos.LotHoldReq();
                    strHoldListSeq2.add(holdList);
                    holdList.setHoldType(BizConstant.SP_HOLDTYPE_LOTHOLD);
                    holdList.setHoldReasonCodeID(new ObjectIdentifier(BizConstant.SP_REASON_PARTIALOPECOMPHOLD));
                    holdList.setHoldUserID(objCommon.getUser().getUserID());
                    holdList.setResponsibleOperationMark(BizConstant.SP_RESPONSIBLEOPERATION_CURRENT);
                    holdList.setRouteID(objLotCurrentOperationInfoGetOut.getRouteID());
                    holdList.setOperationNumber(objLotCurrentOperationInfoGetOut.getOperationNumber());
                    holdList.setClaimMemo("");
                }
                try {
                    lotService.sxHoldLotReq(objCommon, lotID, strHoldListSeq2);
                } catch (ServiceException e) {
                    postTaskExecuteReqResult.setOmCode(new OmCode(e.getCode(), e.getMessage()));
                    throw new ServiceException(retCodeConfig.getPostprocError(), postTaskExecuteReqResult);
                }
                if (1 == lockHoldUseFlag) {
                    //Hold Lot "LOCK"
                    log.info(" Hold Lot. (LOCK)");
                    try {
                        lotService.sxHoldLotReq(objCommon, lotID, strHoldListSeq);
                    } catch (ServiceException e) {
                        if (!Validations.isEquals(retCodeConfig.getInvalidLotStat(), e.getCode())) {
                            log.error("txHoldLotReq() != RC_OK returned error.");
                            postTaskExecuteReqResult.setOmCode(new OmCode(e.getCode(), e.getMessage()));
                            throw new ServiceException(retCodeConfig.getPostprocError(), postTaskExecuteReqResult);
                        } else {
                            log.info("rc == RC_INVALID_LOT_STAT.");
                            bHoldRelease = false;
                        }
                    }
                }
                if (1 == postProcFlagUseFlag) {
                    log.info("Post Proc Flag is usable.");
                    //Set InPostProcessFlag of Lot to ON
                    lotMethod.lotInPostProcessFlagSet(objCommon, lotID, true);
                }
            } else {
                bHoldRelease = false;
            }
        }
        else if (CimStringUtils.equals(info.getPostProcessID(), BizConstant.SP_POSTPROCESS_ACTIONID_COLLECTEDDATAACTIONBYPJ)) {
            //Check lot state
            String lotState = null;
            try {
                lotState = lotMethod.lotStateGet(objCommon, lotID);
            } catch (ServiceException e) {
                postTaskExecuteReqResult.setOmCode(new OmCode(e.getCode(), e.getMessage()));
                throw new ServiceException(retCodeConfig.getPostprocError(), postTaskExecuteReqResult);
            }
            if (CimStringUtils.equals(lotState, CIMStateConst.CIM_LOT_STATE_ACTIVE)) {
                // Active
                if (lockHoldUseFlag == 1 || 1 == postProcFlagUseFlag) {
                    if (!bFindLockHoldFlag && !bInPostProcessFlag) {
                        postTaskExecuteReqResult.setOmCode(new OmCode(retCodeConfigEx.getLotNotInpostprocess(), ObjectIdentifier.fetchValue(lotID)));
                        throw new ServiceException(retCodeConfig.getPostprocError(), postTaskExecuteReqResult);
                    }
                }
                // Set InPostProcessFlag of Lot to OFF
                lotMethod.lotInPostProcessFlagSet(objCommon, lotID, false);
                if (bFindLockHoldFlag && (1 == lockHoldUseFlag || 0 == lockHoldUseFlag)) {
                    // Release Lot Hold.
                    Params.HoldLotReleaseReqParams holdLotReleaseReqParams = new Params.HoldLotReleaseReqParams();
                    holdLotReleaseReqParams.setLotID(lotID);
                    holdLotReleaseReqParams.setHoldReqList(strHoldListSeq);
                    holdLotReleaseReqParams.setReleaseReasonCodeID(holdReleaseCode);
                    try {
                        lotService.sxHoldLotReleaseReq(objCommon, holdLotReleaseReqParams);
                    } catch (ServiceException e) {
                        postTaskExecuteReqResult.setOmCode(new OmCode(e.getCode(), e.getMessage()));
                        throw new ServiceException(retCodeConfig.getPostprocError(), postTaskExecuteReqResult);
                    }
                }
                //-----------------------------------------------------------------
                // Call txEDCWithSpecCheckActionByPostTaskReq
                //-----------------------------------------------------------------
                Params.EDCWithSpecCheckActionByPJReqParams edcWithSpecCheckActionByPJReqParams = new Params.EDCWithSpecCheckActionByPJReqParams();
                edcWithSpecCheckActionByPJReqParams.setEquipmentID(equipmentID);
                edcWithSpecCheckActionByPJReqParams.setControlJobID(controlJobID);
                edcWithSpecCheckActionByPJReqParams.setLotID(lotID);
                edcWithSpecCheckActionByPJReqParams.setClaimMemo(postTaskExecuteReqParams.getClaimMemo());
                Results.EDCWithSpecCheckActionByPJReqResult edcWithSpecCheckActionByPJReqResult = null;
                try {
                    edcWithSpecCheckActionByPJReqResult = engineerDataCollectionService.sxEDCWithSpecCheckActionByPJReq(tmpStrObjCommonIn, edcWithSpecCheckActionByPJReqParams);
                } catch (ServiceException e) {
                    if (Validations.isEquals(retCodeConfig.getSpeccheckError(), e.getCode())) {
                        return postTaskExecuteReqResult;
                    } else {
                        postTaskExecuteReqResult.setOmCode(new OmCode(e.getCode(), e.getMessage()));
                        throw new ServiceException(retCodeConfig.getPostprocError(), postTaskExecuteReqResult);
                    }
                }
                if (1 == lockHoldUseFlag) {
                    //Hold Lot "LOCK"
                    log.info(" Hold Lot. (LOCK)");
                    try {
                        lotService.sxHoldLotReq(objCommon, lotID, strHoldListSeq);
                    } catch (ServiceException e) {
                        if (!Validations.isEquals(retCodeConfig.getInvalidLotStat(), e.getCode())) {
                            log.error("txHoldLotReq() != RC_OK returned error.");
                            postTaskExecuteReqResult.setOmCode(new OmCode(e.getCode(), e.getMessage()));
                            throw new ServiceException(retCodeConfig.getPostprocError(), postTaskExecuteReqResult);
                        } else {
                            log.info("rc == RC_INVALID_LOT_STAT.");
                            bHoldRelease = false;
                        }
                    }
                }
                if (1 == postProcFlagUseFlag) {
                    log.info("Post Proc Flag is usable.");
                    //Set InPostProcessFlag of Lot to ON
                    lotMethod.lotInPostProcessFlagSet(objCommon, lotID, true);
                }
            } else {
                bHoldRelease = false;
            }
        }
        else if (CimStringUtils.equals(info.getPostProcessID(), BizConstant.SP_POSTPROCESS_ACTIONID_AUTODISPATCHCONTROL)) {
            //Check lot state
            String lotState = null;
            try {
                lotState = lotMethod.lotStateGet(objCommon, lotID);
            } catch (ServiceException e) {
                postTaskExecuteReqResult.setOmCode(new OmCode(e.getCode(), e.getMessage()));
                throw new ServiceException(retCodeConfig.getPostprocError(), postTaskExecuteReqResult);
            }
            if (CimStringUtils.equals(lotState, CIMStateConst.CIM_LOT_STATE_ACTIVE)) {
                // Active
                if (lockHoldUseFlag == 1 || 1 == postProcFlagUseFlag) {
                    if (!bFindLockHoldFlag && !bInPostProcessFlag) {
                        postTaskExecuteReqResult.setOmCode(new OmCode(retCodeConfigEx.getLotNotInpostprocess(), ObjectIdentifier.fetchValue(lotID)));
                        throw new ServiceException(retCodeConfig.getPostprocError(), postTaskExecuteReqResult);
                    }
                }
                // Set InPostProcessFlag of Lot to OFF
                lotMethod.lotInPostProcessFlagSet(objCommon, lotID, false);
                if (bFindLockHoldFlag && (1 == lockHoldUseFlag || 0 == lockHoldUseFlag)) {
                    // Release Lot Hold.
                    Params.HoldLotReleaseReqParams holdLotReleaseReqParams = new Params.HoldLotReleaseReqParams();
                    holdLotReleaseReqParams.setLotID(lotID);
                    holdLotReleaseReqParams.setHoldReqList(strHoldListSeq);
                    holdLotReleaseReqParams.setReleaseReasonCodeID(holdReleaseCode);
                    try {
                        lotService.sxHoldLotReleaseReq(objCommon, holdLotReleaseReqParams);
                    } catch (ServiceException e) {
                        postTaskExecuteReqResult.setOmCode(new OmCode(e.getCode(), e.getMessage()));
                        throw new ServiceException(retCodeConfig.getPostprocError(), postTaskExecuteReqResult);
                    }
                }
                Outputs.ObjLotPreviousOperationInfoGetOut objLotPreviousOperationInfoGetOut = lotMethod.lotPreviousOperationInfoGet(objCommon, lotID);
                //--------------------------------------------
                //  Get Auto Dispatch Control Information
                //--------------------------------------------
                Inputs.ObjAutoDispatchControlInfoGetDRIn objAutoDispatchControlInfoGetDRIn = new Inputs.ObjAutoDispatchControlInfoGetDRIn();
                objAutoDispatchControlInfoGetDRIn.setLotID(lotID);
                objAutoDispatchControlInfoGetDRIn.setRouteID(objLotPreviousOperationInfoGetOut.getRouteID());
                objAutoDispatchControlInfoGetDRIn.setOperationNumber(objLotPreviousOperationInfoGetOut.getOperationNumber());
                List<Infos.LotAutoDispatchControlInfo> lotAutoDispatchControlInfos = autoDispatchControlMethod.autoDispatchControlInfoGetDR(objCommon, objAutoDispatchControlInfoGetDRIn);
                if (!CimArrayUtils.isEmpty(lotAutoDispatchControlInfos)) {
                    Infos.LotAutoDispatchControlInfo lotAutoDispatchControlInfo = lotAutoDispatchControlInfos.get(0);
                    if (ObjectIdentifier.equalsWithValue(lotAutoDispatchControlInfo.getRouteID(), objAutoDispatchControlInfoGetDRIn.getRouteID())
                            && CimStringUtils.equals(lotAutoDispatchControlInfo.getOperationNumber(), objAutoDispatchControlInfoGetDRIn.getOperationNumber())
                            && lotAutoDispatchControlInfo.isSingleTriggerFlag()) {
                        Params.AutoDispatchConfigModifyReqParams autoDispatchConfigModifyReqParams = new Params.AutoDispatchConfigModifyReqParams();
                        List<Infos.LotAutoDispatchControlUpdateInfo> lotAutoDispatchControlUpdateInfoList = new ArrayList<>();
                        autoDispatchConfigModifyReqParams.setLotAutoDispatchControlUpdateInfoList(lotAutoDispatchControlUpdateInfoList);
                        Infos.LotAutoDispatchControlUpdateInfo lotAutoDispatchControlUpdateInfo = new Infos.LotAutoDispatchControlUpdateInfo();
                        lotAutoDispatchControlUpdateInfoList.add(lotAutoDispatchControlUpdateInfo);
                        lotAutoDispatchControlUpdateInfo.setLotID(lotID);
                        List<Infos.AutoDispatchControlUpdateInfo> autoDispatchControlUpdateInfoList = new ArrayList<>();
                        lotAutoDispatchControlUpdateInfo.setAutoDispatchControlUpdateInfoList(autoDispatchControlUpdateInfoList);
                        Infos.AutoDispatchControlUpdateInfo autoDispatchControlUpdateInfo = new Infos.AutoDispatchControlUpdateInfo();
                        autoDispatchControlUpdateInfoList.add(autoDispatchControlUpdateInfo);
                        autoDispatchControlUpdateInfo.setUpdateMode(BizConstant.SP_AUTODISPATCHCONTROL_AUTODELETE);
                        autoDispatchControlUpdateInfo.setRouteID(lotAutoDispatchControlInfo.getRouteID());
                        autoDispatchControlUpdateInfo.setOperationNumber(lotAutoDispatchControlInfo.getOperationNumber());
                        autoDispatchControlUpdateInfo.setSingleTriggerFlag(lotAutoDispatchControlInfo.isSingleTriggerFlag());
                        autoDispatchControlUpdateInfo.setDescription(lotAutoDispatchControlInfo.getDescription());
                        autoDispatchConfigModifyReqParams.setClaimMemo(postTaskExecuteReqParams.getClaimMemo());
                        try {
                            dispatchService.sxAutoDispatchConfigModifyReq(tmpStrObjCommonIn, autoDispatchConfigModifyReqParams);
                        } catch (ServiceException e) {
                            postTaskExecuteReqResult.setOmCode(new OmCode(e.getCode(), e.getMessage()));
                            throw new ServiceException(retCodeConfig.getPostprocError(), postTaskExecuteReqResult);
                        }
                    }
                }
                if (1 == lockHoldUseFlag) {
                    //Hold Lot "LOCK"
                    log.info(" Hold Lot. (LOCK)");
                    try {
                        lotService.sxHoldLotReq(objCommon, lotID, strHoldListSeq);
                    } catch (ServiceException e) {
                        if (!Validations.isEquals(retCodeConfig.getInvalidLotStat(), e.getCode())) {
                            log.error("txHoldLotReq() != RC_OK returned error.");
                            postTaskExecuteReqResult.setOmCode(new OmCode(e.getCode(), e.getMessage()));
                            throw new ServiceException(retCodeConfig.getPostprocError(), postTaskExecuteReqResult);
                        } else {
                            log.info("rc == RC_INVALID_LOT_STAT.");
                            bHoldRelease = false;
                        }
                    }
                }
                if (1 == postProcFlagUseFlag) {
                    log.info("Post Proc Flag is usable.");
                    //Set InPostProcessFlag of Lot to ON
                    lotMethod.lotInPostProcessFlagSet(objCommon, lotID, true);
                }
            } else {
                bHoldRelease = false;
            }
        }
        else if (CimStringUtils.equals(info.getPostProcessID(), BizConstant.SP_POSTPROCESS_ACTIONID_RUNWAFERINFOUPDATE)) {
            // Get additional parameters
            List<Infos.PostProcessAdditionalInfo> postProcessAdditionalInfos = postProcessMethod.postProcessAdditionalInfoGetDR(objCommon, info.getDKey(), info.getSequenceNumber());
            int processWaferCnt = 0;
            int opeStartCnt = 0;
            for (Infos.PostProcessAdditionalInfo postProcessAdditionalInfo : postProcessAdditionalInfos) {
                if (CimStringUtils.equals(postProcessAdditionalInfo.getName(), BizConstant.SP_THREADSPECIFICDATA_KEY_RUNWAFERCNT)) {
                    processWaferCnt = Integer.parseInt(postProcessAdditionalInfo.getValue());
                } else if (CimStringUtils.equals(postProcessAdditionalInfo.getName(), BizConstant.SP_THREADSPECIFICDATA_KEY_OPESTARTCNT)) {
                    opeStartCnt = Integer.parseInt(postProcessAdditionalInfo.getValue());
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
                    objectLockMethod.advancedObjectLock(objCommon, new Inputs.ObjAdvanceLockIn(equipmentID,
                            BizConstant.SP_CLASSNAME_POSMACHINE,
                            BizConstant.SP_OBJECTLOCK_OBJECTTYPE_LOADCASSETTE,
                            objLockModeOut.getRequiredLockForMainObject(),
                            new ArrayList<>()));
                } else {
                    /*--------------------------------*/
                    /*   Lock Equipment object        */
                    /*--------------------------------*/
                    objectLockMethod.objectLock(objCommon, CimMachine.class, equipmentID);
                }
                Inputs.ObjEquipmentUsageCountUpdateForPostProcIn objEquipmentUsageCountUpdateForPostProcIn = new Inputs.ObjEquipmentUsageCountUpdateForPostProcIn();
                objEquipmentUsageCountUpdateForPostProcIn.setEquipmentID(equipmentID);
                objEquipmentUsageCountUpdateForPostProcIn.setWaferCnt(processWaferCnt);
                objEquipmentUsageCountUpdateForPostProcIn.setOpeStartCnt(opeStartCnt);
                // set new equipment PM related attributes
                if (CimStringUtils.equals(info.getTransationID(), "OEQPW005")
                        || CimStringUtils.equals(info.getTransationID(), "OEQPW004")
                        || CimStringUtils.equals(info.getTransationID(), "ODRBW046")
                        || CimStringUtils.equals(info.getTransationID(), "ODRBW029")) {
                    objEquipmentUsageCountUpdateForPostProcIn.setAction(BizConstant.SP_EQPATTR_UPDATE_ACTION_INCREASE);
                } else if (CimStringUtils.equals(info.getTransationID(), "OEQPW009")
                        || CimStringUtils.equals(info.getTransationID(), "OEQPW010")
                        || CimStringUtils.equals(info.getTransationID(), "OEQPW012")
                        || CimStringUtils.equals(info.getTransationID(), "OEQPW024")
                        || CimStringUtils.equals(info.getTransationID(), "ODRBW047")
                        || CimStringUtils.equals(info.getTransationID(), "ODRBW028")) {
                    objEquipmentUsageCountUpdateForPostProcIn.setAction(BizConstant.SP_EQPATTR_UPDATE_ACTION_DECREASE);
                }
                equipmentMethod.equipmentUsageCountUpdateForPostProc(objCommon, objEquipmentUsageCountUpdateForPostProcIn);
            }
        }
        else if (CimStringUtils.equals(info.getPostProcessID(), BizConstant.SP_POSTPROCESS_ACTIONID_PARALLELEXECFINALIZE)) {
            // object_lock
            try {
                objectLockMethod.objectLock(objCommon, CimCassette.class, cassetteID);
            } catch (ServiceException e) {
                postTaskExecuteReqResult.setOmCode(new OmCode(e.getCode(), e.getMessage()));
                throw new ServiceException(retCodeConfig.getPostprocError(), postTaskExecuteReqResult);
            }
            //-------------------------------------------
            // Finalize action for cassette
            //-------------------------------------------
            try {
                cassetteMethod.cassetteStatusFinalizeForPostProcess(objCommon, cassetteID);
            } catch (ServiceException e) {
                postTaskExecuteReqResult.setOmCode(new OmCode(e.getCode(), e.getMessage()));
                throw new ServiceException(retCodeConfig.getPostprocError(), postTaskExecuteReqResult);
            }
        }
        //----------------------------------------
        // Call txAMVerifyReq
        //----------------------------------------
        else if (CimStringUtils.equals(info.getPostProcessID(), BizConstant.SP_POSTPROCESS_ACTIONID_EQPMONITOREVAL)) {
            //Check lot state
            String lotState = null;
            try {
                lotState = lotMethod.lotStateGet(objCommon, lotID);
            } catch (ServiceException e) {
                postTaskExecuteReqResult.setOmCode(new OmCode(e.getCode(), e.getMessage()));
                throw new ServiceException(retCodeConfig.getPostprocError(), postTaskExecuteReqResult);
            }
            if (CimStringUtils.equals(lotState, CIMStateConst.CIM_LOT_STATE_ACTIVE)) {
                // Active
                String eqpmonitorSwitch = StandardProperties.OM_AUTOMON_FLAG.getValue();
                if (CimStringUtils.equals(eqpmonitorSwitch, "1")) {
                    //Check Lot type
                    String lotType = lotMethod.lotTypeGet(objCommon, lotID);
                    if (CimStringUtils.equals(lotType, BizConstant.SP_LOT_TYPE_EQUIPMENTMONITORLOT)
                            || CimStringUtils.equals(lotType, BizConstant.SP_LOT_TYPE_DUMMYLOT)) {
                        Infos.EqpMonitorJobLotInfo eqpMonitorJobLotInfo = null;
                        try {
                            eqpMonitorJobLotInfo = lotMethod.lotEqpMonitorJobGet(objCommon, lotID);
                        } catch (ServiceException e) {
                            postTaskExecuteReqResult.setOmCode(new OmCode(e.getCode(), e.getMessage()));
                            throw new ServiceException(retCodeConfig.getPostprocError(), postTaskExecuteReqResult);
                        }
                        if (ObjectIdentifier.isEmptyWithValue(eqpMonitorJobLotInfo.getEqpMonitorJobID())) {
                            // Post process action "AutoMonitorEval" can be skipped
                            //  - Lot isn't involved in EqpMonitor job
                            //  - No violation in Spec/SPC check
                            log.info("AutoMonitorEval can be skipped");
                        } else {
                            log.info("AutoMonitorEval can not be skipped");
                            if (lockHoldUseFlag == 1 || 1 == postProcFlagUseFlag) {
                                if (!bFindLockHoldFlag && !bInPostProcessFlag) {
                                    postTaskExecuteReqResult.setOmCode(new OmCode(retCodeConfigEx.getLotNotInpostprocess(), ObjectIdentifier.fetchValue(lotID)));
                                    throw new ServiceException(retCodeConfig.getPostprocError(), postTaskExecuteReqResult);
                                }
                            }
                            // Set InPostProcessFlag of Lot to OFF
                            try {
                                lotMethod.lotInPostProcessFlagSet(objCommon, lotID, false);
                            } catch (ServiceException e) {
                                postTaskExecuteReqResult.setOmCode(new OmCode(e.getCode(), e.getMessage()));
                                throw new ServiceException(retCodeConfig.getPostprocError(), postTaskExecuteReqResult);
                            }
                            if (bFindLockHoldFlag && (1 == lockHoldUseFlag || 0 == lockHoldUseFlag)) {
                                // Release Lot Hold.
                                Params.HoldLotReleaseReqParams holdLotReleaseReqParams = new Params.HoldLotReleaseReqParams();
                                holdLotReleaseReqParams.setLotID(lotID);
                                holdLotReleaseReqParams.setHoldReqList(strHoldListSeq);
                                holdLotReleaseReqParams.setReleaseReasonCodeID(holdReleaseCode);
                                try {
                                    lotService.sxHoldLotReleaseReq(objCommon, holdLotReleaseReqParams);
                                } catch (ServiceException e) {
                                    postTaskExecuteReqResult.setOmCode(new OmCode(e.getCode(), e.getMessage()));
                                    throw new ServiceException(retCodeConfig.getPostprocError(), postTaskExecuteReqResult);
                                }
                            }
                            //Call txAMVerifyReq to the object Lot.
                            Params.AMVerifyReqInParams amVerifyReqInParams = new Params.AMVerifyReqInParams();
                            amVerifyReqInParams.setEquipmentID(equipmentID);
                            amVerifyReqInParams.setControlJobID(controlJobID);
                            amVerifyReqInParams.setLotID(lotID);
                            Results.AMVerifyReqResult amVerifyReqResult = null;
                            try {
                                amVerifyReqResult = autoMonitorService.sxAMVerifyReq(tmpStrObjCommonIn, amVerifyReqInParams);
                            } catch (ServiceException e) {
                                postTaskExecuteReqResult.setOmCode(new OmCode(e.getCode(), e.getMessage()));
                                throw new ServiceException(retCodeConfig.getPostprocError(), postTaskExecuteReqResult);
                            }
                            if (1 == lockHoldUseFlag) {
                                //Hold Lot "LOCK"
                                log.info(" Hold Lot. (LOCK)");
                                try {
                                    lotService.sxHoldLotReq(objCommon, lotID, strHoldListSeq);
                                } catch (ServiceException e) {
                                    if (!Validations.isEquals(retCodeConfig.getInvalidLotStat(), e.getCode())) {
                                        log.error("txHoldLotReq() != RC_OK returned error.");
                                        postTaskExecuteReqResult.setOmCode(new OmCode(e.getCode(), e.getMessage()));
                                        throw new ServiceException(retCodeConfig.getPostprocError(), postTaskExecuteReqResult);
                                    } else {
                                        log.info("rc == RC_INVALID_LOT_STAT.");
                                        bHoldRelease = false;
                                    }
                                }
                            }
                            if (1 == postProcFlagUseFlag) {
                                log.info("Post Proc Flag is usable.");
                                //Set InPostProcessFlag of Lot to ON
                                try {
                                    lotMethod.lotInPostProcessFlagSet(objCommon, lotID, true);
                                } catch (ServiceException e) {
                                    postTaskExecuteReqResult.setOmCode(new OmCode(e.getCode(), e.getMessage()));
                                    throw new ServiceException(retCodeConfig.getPostprocError(), postTaskExecuteReqResult);
                                }
                            }
                        }
                    }
                }
            } else {
                bHoldRelease = false;
            }
        }
        else if (CimStringUtils.equals(info.getPostProcessID(), BizConstant.SP_POSTPROCESS_ACTIONID_EQPMONITORJOBLOTREMOVE)) {
            //Check lot state
            String lotState = null;
            try {
                lotState = lotMethod.lotStateGet(objCommon, lotID);
            } catch (ServiceException e) {
                postTaskExecuteReqResult.setOmCode(new OmCode(e.getCode(), e.getMessage()));
                throw new ServiceException(retCodeConfig.getPostprocError(), postTaskExecuteReqResult);
            }
            if (CimStringUtils.equals(lotState, CIMStateConst.CIM_LOT_STATE_ACTIVE)) {
                // Active
                String eqpmonitorSwitch = StandardProperties.OM_AUTOMON_FLAG.getValue();
                if (CimStringUtils.equals(eqpmonitorSwitch, "1")) {
                    //Check Lot type
                    String lotType = lotMethod.lotTypeGet(objCommon, lotID);
                    if (CimStringUtils.equals(lotType, BizConstant.SP_LOT_TYPE_EQUIPMENTMONITORLOT)
                            || CimStringUtils.equals(lotType, BizConstant.SP_LOT_TYPE_DUMMYLOT)) {
                        Infos.EqpMonitorJobLotInfo eqpMonitorJobLotInfo = null;
                        try {
                            eqpMonitorJobLotInfo = lotMethod.lotEqpMonitorJobGet(objCommon, lotID);
                        } catch (ServiceException e) {
                            postTaskExecuteReqResult.setOmCode(new OmCode(e.getCode(), e.getMessage()));
                            throw new ServiceException(retCodeConfig.getPostprocError(), postTaskExecuteReqResult);
                        }
                        if (ObjectIdentifier.isEmptyWithValue(eqpMonitorJobLotInfo.getEqpMonitorJobID())
                                || !eqpMonitorJobLotInfo.getExitFlag()) {
                            log.info("AutoMonitorJobLotRemove can be skipped");
                        } else {
                            log.info("AutoMonitorJobLotRemove can not be skipped");
                            if (lockHoldUseFlag == 1 || 1 == postProcFlagUseFlag) {
                                if (!bFindLockHoldFlag && !bInPostProcessFlag) {
                                    postTaskExecuteReqResult.setOmCode(new OmCode(retCodeConfigEx.getLotNotInpostprocess(), ObjectIdentifier.fetchValue(lotID)));
                                    throw new ServiceException(retCodeConfig.getPostprocError(), postTaskExecuteReqResult);
                                }
                            }
                            // Set InPostProcessFlag of Lot to OFF
                            lotMethod.lotInPostProcessFlagSet(objCommon, lotID, false);
                            if (bFindLockHoldFlag && (1 == lockHoldUseFlag || 0 == lockHoldUseFlag)) {
                                // Release Lot Hold.
                                Params.HoldLotReleaseReqParams holdLotReleaseReqParams = new Params.HoldLotReleaseReqParams();
                                holdLotReleaseReqParams.setLotID(lotID);
                                holdLotReleaseReqParams.setHoldReqList(strHoldListSeq);
                                holdLotReleaseReqParams.setReleaseReasonCodeID(holdReleaseCode);
                                try {
                                    lotService.sxHoldLotReleaseReq(objCommon, holdLotReleaseReqParams);
                                } catch (ServiceException e) {
                                    postTaskExecuteReqResult.setOmCode(new OmCode(e.getCode(), e.getMessage()));
                                    throw new ServiceException(retCodeConfig.getPostprocError(), postTaskExecuteReqResult);
                                }
                            }
                            //txAMJObLotDeleteReq to the object Lot.
                            Params.AMJObLotDeleteReqInParams amjObLotDeleteReqInParams = new Params.AMJObLotDeleteReqInParams();
                            amjObLotDeleteReqInParams.setEquipmentID(equipmentID);
                            amjObLotDeleteReqInParams.setControlJobID(controlJobID);
                            amjObLotDeleteReqInParams.setLotID(lotID);
                            amjObLotDeleteReqInParams.setClaimMemo(postTaskExecuteReqParams.getClaimMemo());
                            Results.AMJObLotDeleteReqResult amjObLotDeleteReqResult = null;
                            try {
                                amjObLotDeleteReqResult = autoMonitorService.sxAMJObLotDeleteReq(tmpStrObjCommonIn, amjObLotDeleteReqInParams);
                            } catch (ServiceException e) {
                                postTaskExecuteReqResult.setOmCode(new OmCode(e.getCode(), e.getMessage()));
                                throw new ServiceException(retCodeConfig.getPostprocError(), postTaskExecuteReqResult);
                            }
                            if (1 == lockHoldUseFlag) {
                                //Hold Lot "LOCK"
                                log.info(" Hold Lot. (LOCK)");
                                try {
                                    lotService.sxHoldLotReq(objCommon, lotID, strHoldListSeq);
                                } catch (ServiceException e) {
                                    if (!Validations.isEquals(retCodeConfig.getInvalidLotStat(), e.getCode())) {
                                        log.error("txHoldLotReq() != RC_OK returned error.");
                                        postTaskExecuteReqResult.setOmCode(new OmCode(e.getCode(), e.getMessage()));
                                        throw new ServiceException(retCodeConfig.getPostprocError(), postTaskExecuteReqResult);
                                    } else {
                                        log.info("rc == RC_INVALID_LOT_STAT.");
                                        bHoldRelease = false;
                                    }
                                }
                            }
                            if (1 == postProcFlagUseFlag) {
                                log.info("Post Proc Flag is usable.");
                                //Set InPostProcessFlag of Lot to ON
                                try {
                                    lotMethod.lotInPostProcessFlagSet(objCommon, lotID, true);
                                } catch (ServiceException e) {
                                    postTaskExecuteReqResult.setOmCode(new OmCode(e.getCode(), e.getMessage()));
                                    throw new ServiceException(retCodeConfig.getPostprocError(), postTaskExecuteReqResult);
                                }
                            }
                        }
                    }
                }
            } else {
                bHoldRelease = false;
            }
        }
        else if (CimStringUtils.equals(info.getPostProcessID(), BizConstant.SP_POSTPROCESS_ACTIONID_EQPMONITORUSEDCOUNTUP)) {
            //Check lot state
            String lotState = null;
            try {
                lotState = lotMethod.lotStateGet(objCommon, lotID);
            } catch (ServiceException e) {
                postTaskExecuteReqResult.setOmCode(new OmCode(e.getCode(), e.getMessage()));
                throw new ServiceException(retCodeConfig.getPostprocError(), postTaskExecuteReqResult);
            }
            if (CimStringUtils.equals(lotState, CIMStateConst.CIM_LOT_STATE_ACTIVE)) {
                // Active
                String eqpmonitorSwitch = StandardProperties.OM_AUTOMON_FLAG.getValue();
                if (CimStringUtils.equals(eqpmonitorSwitch, "1")) {
                    //Check Lot type
                    String lotType = lotMethod.lotTypeGet(objCommon, lotID);
                    if (CimStringUtils.equals(lotType, BizConstant.SP_LOT_TYPE_EQUIPMENTMONITORLOT)
                            || CimStringUtils.equals(lotType, BizConstant.SP_LOT_TYPE_DUMMYLOT)) {
                        List<Infos.EqpMonitorLabelInfo> eqpMonitorLabelInfos = lotMethod.lotEqpMonitorOperationLabelGet(tmpStrObjCommonIn, lotID);
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
                            log.info("AutoMonitorUsedCountUp can be skipped");
                        } else {
                            log.info("Update Equipmet Monitor count");
                            if (lockHoldUseFlag == 1 || 1 == postProcFlagUseFlag) {
                                if (!bFindLockHoldFlag && !bInPostProcessFlag) {
                                    postTaskExecuteReqResult.setOmCode(new OmCode(retCodeConfigEx.getLotNotInpostprocess(), ObjectIdentifier.fetchValue(lotID)));
                                    throw new ServiceException(retCodeConfig.getPostprocError(), postTaskExecuteReqResult);
                                }
                            }
                            // Set InPostProcessFlag of Lot to OFF
                            try {
                                lotMethod.lotInPostProcessFlagSet(objCommon, lotID, false);
                            } catch (ServiceException e) {
                                postTaskExecuteReqResult.setOmCode(new OmCode(e.getCode(), e.getMessage()));
                                throw new ServiceException(retCodeConfig.getPostprocError(), postTaskExecuteReqResult);
                            }
                            if (bFindLockHoldFlag && (1 == lockHoldUseFlag || 0 == lockHoldUseFlag)) {
                                // Release Lot Hold.
                                Params.HoldLotReleaseReqParams holdLotReleaseReqParams = new Params.HoldLotReleaseReqParams();
                                holdLotReleaseReqParams.setLotID(lotID);
                                holdLotReleaseReqParams.setHoldReqList(strHoldListSeq);
                                holdLotReleaseReqParams.setReleaseReasonCodeID(holdReleaseCode);
                                try {
                                    lotService.sxHoldLotReleaseReq(objCommon, holdLotReleaseReqParams);
                                } catch (ServiceException e) {
                                    postTaskExecuteReqResult.setOmCode(new OmCode(e.getCode(), e.getMessage()));
                                    throw new ServiceException(retCodeConfig.getPostprocError(), postTaskExecuteReqResult);
                                }
                            }
                            //Increment Equipment Monitor used count by 1 for all lot's wafers.
                            Inputs.ObjEqpMonitorWaferUsedCountUpdateIn objEqpMonitorWaferUsedCountUpdateIn = new Inputs.ObjEqpMonitorWaferUsedCountUpdateIn();
                            objEqpMonitorWaferUsedCountUpdateIn.setLotID(lotID);
                            objEqpMonitorWaferUsedCountUpdateIn.setAction(BizConstant.SP_EQPMONUSEDCNT_ACTION_INCREMENT);
                            List<Infos.EqpMonitorWaferUsedCount> eqpMonitorWaferUsedCounts = equipmentMethod.eqpMonitorWaferUsedCountUpdate(objCommon, objEqpMonitorWaferUsedCountUpdateIn);
                            // Create Operation History
                            Inputs.ObjEqpMonitorWaferUsedCountUpdateEventMakeParams objEqpMonitorWaferUsedCountUpdateEventMakeParams = new Inputs.ObjEqpMonitorWaferUsedCountUpdateEventMakeParams();
                            objEqpMonitorWaferUsedCountUpdateEventMakeParams.setTransactionID("OAMNW007");
                            objEqpMonitorWaferUsedCountUpdateEventMakeParams.setLotID(lotID);
                            objEqpMonitorWaferUsedCountUpdateEventMakeParams.setEquipmentID(equipmentID);
                            objEqpMonitorWaferUsedCountUpdateEventMakeParams.setControlJobID(controlJobID);
                            objEqpMonitorWaferUsedCountUpdateEventMakeParams.setStrEqpMonitorWaferUsedCountList(eqpMonitorWaferUsedCounts);
                            objEqpMonitorWaferUsedCountUpdateEventMakeParams.setClaimMemo(postTaskExecuteReqParams.getClaimMemo());
                            eventMethod.eqpMonitorWaferUsedCountUpdateEventMake(objCommon, objEqpMonitorWaferUsedCountUpdateEventMakeParams);
                            if (1 == lockHoldUseFlag) {
                                //Hold Lot "LOCK"
                                log.info(" Hold Lot. (LOCK)");
                                try {
                                    lotService.sxHoldLotReq(objCommon, lotID, strHoldListSeq);
                                } catch (ServiceException e) {
                                    if (!Validations.isEquals(retCodeConfig.getInvalidLotStat(), e.getCode())) {
                                        log.error("txHoldLotReq() != RC_OK returned error.");
                                        postTaskExecuteReqResult.setOmCode(new OmCode(e.getCode(), e.getMessage()));
                                        throw new ServiceException(retCodeConfig.getPostprocError(), postTaskExecuteReqResult);
                                    } else {
                                        log.info("rc == RC_INVALID_LOT_STAT.");
                                        bHoldRelease = false;
                                    }
                                }
                            }
                            if (1 == postProcFlagUseFlag) {
                                log.info("Post Proc Flag is usable.");
                                //Set InPostProcessFlag of Lot to ON
                                try {
                                    lotMethod.lotInPostProcessFlagSet(objCommon, lotID, true);
                                } catch (ServiceException e) {
                                    postTaskExecuteReqResult.setOmCode(new OmCode(e.getCode(), e.getMessage()));
                                    throw new ServiceException(retCodeConfig.getPostprocError(), postTaskExecuteReqResult);
                                }
                            }
                        }
                    }
                }
            } else {
                bHoldRelease = false;
            }
        }
        //-----------------------------------------------------------------
        // TODO Line 4562-5083
        // Post Actions for Durable.
        // Call txRunDurableBRScriptReq
        //-----------------------------------------------------------------
        else if (CimStringUtils.equals(info.getPostProcessID(), BizConstant.SP_POSTPROCESS_ACTIONID_DSCRIPT)) {

        }

        //-----------------------------------------------------------------
        // Post Actions for Durable.
        // Call txDurableBankInByPostTaskReq
        //-----------------------------------------------------------------
        else if (CimStringUtils.equals(info.getPostProcessID(), BizConstant.SP_POSTPROCESS_ACTIONID_DAUTOBANKIN)) {

        }
        //-----------------------------------------------------------------
        // Post Actions for Durable.
        // Call txDurableLagTimeActionReq
        //-----------------------------------------------------------------
        else if (CimStringUtils.equals(info.getPostProcessID(), BizConstant.SP_POSTPROCESS_ACTIONID_DPROCESSLAGTIME)) {
            log.info("{}", "postProcID is DProcessLagTime");
            Infos.DurableOnRouteStateGetIn strDurableOnRouteStateGetin = new Infos.DurableOnRouteStateGetIn();
            strDurableOnRouteStateGetin.setDurableCategory(durableCategory);
            strDurableOnRouteStateGetin.setDurableID(cassetteID);
            String strDurableOnRouteStateGetout;
            strDurableOnRouteStateGetout = durableMethod.durableOnRouteStateGet(objCommon, strDurableOnRouteStateGetin);
            if (CimStringUtils.equals(strDurableOnRouteStateGetout, BizConstant.SP_DURABLE_ONROUTESTATE_ACTIVE)) {
                if (lockHoldUseFlag == 1 || postProcFlagUseFlag == 1) {
                    log.info("{}", "Check LockHold & InPostProcessFlag.");
                    if (CimBooleanUtils.isFalse(bFindLockHoldFlag) && CimBooleanUtils.isFalse(bInPostProcessFlag)) {
                        log.info("{}", "Durable is not in post process.");
                        Validations.check(true,
                                retCodeConfig.getCassetteNotInPostProcess(),
                                ObjectIdentifier.fetchValue(cassetteID));
                    }
                }

                // Set InPostProcessFlag of Durable to OFF
                durableMethod.durableInPostProcessFlagSet(objCommon, durableCategory,
                        cassetteID,
                        false);

                if (CimBooleanUtils.isTrue(bFindLockHoldFlag) && (lockHoldUseFlag == 1 || lockHoldUseFlag == 0)) {
                    // Release Durable Hold.
                    log.info("{}", " Release Durables' Hold (LOCK) ");
                    Infos.HoldDurableReleaseReqInParam strHoldDurableReleaseReqInParam = new Infos.HoldDurableReleaseReqInParam();
                    strHoldDurableReleaseReqInParam.setDurableCategory(durableCategory);
                    strHoldDurableReleaseReqInParam.setDurableID(cassetteID);
                    strHoldDurableReleaseReqInParam.setReleaseReasonCodeID(ObjectIdentifier.buildWithValue(BizConstant.SP_REASON_DURABLELOCKRELEASE));
                    strHoldDurableReleaseReqInParam.setStrDurableHoldList(strDurableHoldList);
                    durableService.sxHoldDrbReleaseReq(objCommon, strHoldDurableReleaseReqInParam, postTaskExecuteReqParams.getClaimMemo());
                }

                // Call txDurableProcessLagTimeUpdateReq
                Results.DurableProcessLagTimeUpdateReqResult strDurableProcessLagTimeUpdateReqResult;
                Params.DurableProcessLagTimeUpdateReqInParm strDurableProcessLagTimeUpdateReqInParm = new Params.DurableProcessLagTimeUpdateReqInParm();
                strDurableProcessLagTimeUpdateReqInParm.setDurableCategory(durableCategory);
                strDurableProcessLagTimeUpdateReqInParm.setDurableID(cassetteID);
                strDurableProcessLagTimeUpdateReqInParm.setAction(BizConstant.SP_PROCESSLAGTIME_ACTION_SET);
                strDurableProcessLagTimeUpdateReqResult = durableService.sxDrbLagTimeActionReq(objCommon, postTaskExecuteReqParams.getUser(), strDurableProcessLagTimeUpdateReqInParm);

                //Hold durable "LOCK"
                if (lockHoldUseFlag == 1) {
                    Params.HoldDurableReqInParam strHoldDurableReqInParam = new Params.HoldDurableReqInParam();
                    strHoldDurableReqInParam.setDurableCategory(durableCategory);
                    strHoldDurableReqInParam.setDurableID(cassetteID);
                    strHoldDurableReqInParam.setDurableHoldLists(strDurableHoldList);
                    try {
                        durableService.sxHoldDrbReq(objCommon, strHoldDurableReqInParam, postTaskExecuteReqParams.getClaimMemo());
                    } catch (ServiceException ex) {
                        if (Validations.isEquals(ex.getCode(), retCodeConfigEx.getInvalidDurableStat())) {
                            log.info("{}", "txHoldDurableReq() != RC_INVALID_DURABLE_STAT returned error.");
                            Validations.check(true, retCodeConfig.getPostprocError());
                        } else {
                            log.info("{}", "rc == RC_INVALID_DURABLE_STAT.");
                            bHoldRelease = false;
                        }
                    }
                }
                if (postProcFlagUseFlag == 1) {
                    log.info("{}", "Post Proc Flag is usable.");
                    // Set InPostProcessFlag of Durable to OFF
                    durableMethod.durableInPostProcessFlagSet(objCommon, durableCategory,
                            cassetteID,
                            true);
                }
            } else {
                log.info("{}", "set bHoldRelease to FALSE");
                bHoldRelease = false;
            }
        }

        //-----------------------------------------------------------------
        // Post Actions for Sampling.
        // Call txLotSamplingCheck
        //-----------------------------------------------------------------
        else if (CimStringUtils.equals(info.getPostProcessID(), BizConstant.SP_LOT_SAMPLING_ACTION)) {
            log.info(">>>>>>>>> Execute Sampling");
            String lotState;
            try {
                lotState = lotMethod.lotStateGet(objCommon, lotID);
            } catch (ServiceException e) {
                postTaskExecuteReqResult.setOmCode(new OmCode(e.getCode(), e.getMessage()));
                throw new ServiceException(retCodeConfig.getPostprocError(), postTaskExecuteReqResult);
            }

            if (CimStringUtils.equals(lotState, CIMStateConst.CIM_LOT_STATE_ACTIVE)) {
                log.info("lot_state == Active. - N/A -");
                if (1 == lockHoldUseFlag || 1 == postProcFlagUseFlag) {
                    log.info("Check LockHold & InPostProcessFlag.");
                    if (!bFindLockHoldFlag && !bInPostProcessFlag) {
                        log.info("Lot is not in post process.");
                        postTaskExecuteReqResult.setOmCode(new OmCode(retCodeConfigEx.getLotNotInpostprocess(), ObjectIdentifier.fetchValue(lotID)));
                        throw new ServiceException(new OmCode(retCodeConfig.getPostprocError(), postTaskExecuteReqResult));
                    }
                }

                // Set InPostProcessFlag of Lot to OFF
                lotMethod.lotInPostProcessFlagSet(objCommon, lotID, false);
                if (bFindLockHoldFlag && (1 == lockHoldUseFlag || 0 == lockHoldUseFlag)) {
                    // Release Lot Hold.
                    Params.HoldLotReleaseReqParams holdLotReleaseReqParams = new Params.HoldLotReleaseReqParams();
                    holdLotReleaseReqParams.setReleaseReasonCodeID(holdReleaseCode);
                    holdLotReleaseReqParams.setHoldReqList(strHoldListSeq);
                    holdLotReleaseReqParams.setLotID(lotID);
                    try {
                        log.info("Release Lot's Hold (LOCK)");
                        lotService.sxHoldLotReleaseReq(objCommon, holdLotReleaseReqParams);
                    } catch (ServiceException e) {
                        postTaskExecuteReqResult.setOmCode(new OmCode(e.getCode(), e.getMessage()));
                        throw new ServiceException(retCodeConfig.getPostprocError(), postTaskExecuteReqResult);
                    }
                }

                try {
                    samplingService.sxLotSamplingCheckThenSkipReq(objCommon, lotID, info.getTransationID(), BizConstant.LS_BASIC_LOT_EXECUTE);
                } catch (ServiceException e) {
                    if (e.getCode() != null && !Validations.isEquals(retCodeConfig.getNotFoundScript(), e.getCode())) {
                        postTaskExecuteReqResult.setOmCode(new OmCode(e.getCode(), e.getMessage()));
                        throw new ServiceException(retCodeConfig.getPostprocError(), postTaskExecuteReqResult);
                    } else {
                        throw e;
                    }
                }

                if (lockHoldUseFlag == 1) {
                    try {
                        log.info("Hold Lot. (LOCK)");
                        lotService.sxHoldLotReq(objCommon, lotID, strHoldListSeq);
                    } catch (ServiceException e) {
                        if (!Validations.isEquals(retCodeConfig.getInvalidLotStat(), e.getCode())) {
                            postTaskExecuteReqResult.setOmCode(new OmCode(e.getCode(), e.getMessage()));
                            throw new ServiceException(retCodeConfig.getPostprocError(), postTaskExecuteReqResult);
                        } else {
                            bHoldRelease = false;
                        }
                    }
                }

                if (postProcFlagUseFlag == 1) {
                    log.info("Post Proc Flag is usable.");

                    //Check lot state
                    try {
                        lotState = lotMethod.lotStateGet(objCommon, lotID);
                    } catch (ServiceException e) {
                        postTaskExecuteReqResult.setOmCode(new OmCode(e.getCode(), e.getMessage()));
                        throw new ServiceException(retCodeConfig.getPostprocError(), postTaskExecuteReqResult);
                    }

                    if (CimStringUtils.equals(lotState, CIMStateConst.CIM_LOT_STATE_ACTIVE)) {
                        log.info("lot_state = Active.");
                        lotMethod.lotInPostProcessFlagSet(objCommon, lotID, true);
                    }
                }
            } else {
                log.info("lot_state != Active. - N/A -");
                bHoldRelease = false;
            }
        }
        //-----------------------------------------------------------------
        // Post Actions for npw lot final step skip.
        // Call txNpwCheck
        //-----------------------------------------------------------------
        else if (CimStringUtils.equals(info.getPostProcessID(), BizConstant.SP_NPW_LOT_AUTO_SKIP)) {
            log.info(">>>>>>>>> Execute NPWLotSkip");
            String lotState;
            try {
                lotState = lotMethod.lotStateGet(objCommon, lotID);
            } catch (ServiceException e) {
                postTaskExecuteReqResult.setOmCode(new OmCode(e.getCode(), e.getMessage()));
                throw new ServiceException(retCodeConfig.getPostprocError(), postTaskExecuteReqResult);
            }

            if (CimStringUtils.equals(lotState, CIMStateConst.CIM_LOT_STATE_ACTIVE)) {
                log.info("lot_state == Active. - N/A -");
                if (1 == lockHoldUseFlag || 1 == postProcFlagUseFlag) {
                    log.info("Check LockHold & InPostProcessFlag.");
                    if (!bFindLockHoldFlag && !bInPostProcessFlag) {
                        log.info("Lot is not in post process.");
                        postTaskExecuteReqResult.setOmCode(new OmCode(retCodeConfigEx.getLotNotInpostprocess(), ObjectIdentifier.fetchValue(lotID)));
                        throw new ServiceException(new OmCode(retCodeConfig.getPostprocError(), postTaskExecuteReqResult));
                    }
                }

                // Set InPostProcessFlag of Lot to OFF
                lotMethod.lotInPostProcessFlagSet(objCommon, lotID, false);
                if (bFindLockHoldFlag && (1 == lockHoldUseFlag || 0 == lockHoldUseFlag)) {
                    // Release Lot Hold.
                    Params.HoldLotReleaseReqParams holdLotReleaseReqParams = new Params.HoldLotReleaseReqParams();
                    holdLotReleaseReqParams.setReleaseReasonCodeID(holdReleaseCode);
                    holdLotReleaseReqParams.setHoldReqList(strHoldListSeq);
                    holdLotReleaseReqParams.setLotID(lotID);
                    try {
                        log.info("Release Lot's Hold (LOCK)");
                        lotService.sxHoldLotReleaseReq(objCommon, holdLotReleaseReqParams);
                    } catch (ServiceException e) {
                        postTaskExecuteReqResult.setOmCode(new OmCode(e.getCode(), e.getMessage()));
                        throw new ServiceException(retCodeConfig.getPostprocError(), postTaskExecuteReqResult);
                    }
                }

                lotService.sxNPWLotAutoSkipCheckAndExcute(objCommon, lotID);

                if (lockHoldUseFlag == 1) {
                    try {
                        log.info("Hold Lot. (LOCK)");
                        lotService.sxHoldLotReq(objCommon, lotID, strHoldListSeq);
                    } catch (ServiceException e) {
                        if (!Validations.isEquals(retCodeConfig.getInvalidLotStat(), e.getCode())) {
                            postTaskExecuteReqResult.setOmCode(new OmCode(e.getCode(), e.getMessage()));
                            throw new ServiceException(retCodeConfig.getPostprocError(), postTaskExecuteReqResult);
                        } else {
                            bHoldRelease = false;
                        }
                    }
                }

                if (postProcFlagUseFlag == 1) {
                    log.info("Post Proc Flag is usable.");

                    //Check lot state
                    try {
                        lotState = lotMethod.lotStateGet(objCommon, lotID);
                    } catch (ServiceException e) {
                        postTaskExecuteReqResult.setOmCode(new OmCode(e.getCode(), e.getMessage()));
                        throw new ServiceException(retCodeConfig.getPostprocError(), postTaskExecuteReqResult);
                    }

                    if (CimStringUtils.equals(lotState, CIMStateConst.CIM_LOT_STATE_ACTIVE)) {
                        log.info("lot_state = Active.");
                        lotMethod.lotInPostProcessFlagSet(objCommon, lotID, true);
                    }
                }
            } else {
                log.info("lot_state != Active. - N/A -");
                bHoldRelease = false;
            }
        } else {
            throw new ServiceException(new OmCode(retCodeConfigEx.getPostProcUnknownProcId(), info.getPostProcessID()));
        }
        //--------------------------
        // Check Error again.
        //--------------------------
        if (rc != 0) {
            throw new ServiceException(retCodeConfig.getPostprocError());
        }
        boolean interFabXferFlag = false;
        boolean bLastPostProc = false;
        //--------------------------------------
        // Post process for lot process
        //--------------------------------------
        if (CimStringUtils.equals(BizConstant.SP_POSTPROCESS_TARGETTYPE_LOT, info.getTargetType())) {
            //------------------------------------------------------------------------------------------------------------------------
            //  If postProcID == SP_PostProcess_ActionID_InterFabXfer and interFabXfer is done, return RC_INTERFAB_LOTXFER_EXECUTED.
            //------------------------------------------------------------------------------------------------------------------------
            if (CimStringUtils.equals(BizConstant.SP_POSTPROCESS_ACTIONID_INTERFABXFER, info.getPostProcessID()) && rc_interFabXfer == retCodeConfig.getInterfabLotxferExecuted().getCode()) {
                interFabXferFlag = true;
            } else if (rc_pp == retCodeConfig.getPostrpocDkeyRecreate().getCode()) {
                // Store TriggerDKey as thread specific data
                ThreadContextHolder.setThreadSpecificDataString(BizConstant.SP_THREADSPECIFICDATA_KEY_TRIGGERDKEY, info.getDKey());
                //delete all remaining post processes related to the parent lot
                //release LockHold and set PPFlag to false for parent lot
                List<Infos.PostProcessAdditionalInfo> dummyPostProcessAdditionalInfoSeq = new ArrayList<>();
                List<Infos.PostProcessActionInfo> updatePostProcessActionInfoList = postService.sxPostActionModifyReq(objCommon, BizConstant.SP_POSTPROCESSACTIONINFO_DELETEWITHLOT, postProcessActionInfos, dummyPostProcessAdditionalInfoSeq, "");
                //-------------------------------------------
                // Register post process for parent lot again
                //-------------------------------------------
                Params.PostTaskRegisterReqParams postTaskRegisterReqParams = new Params.PostTaskRegisterReqParams();
                Infos.PostProcessRegistrationParam postProcessRegistrationParm = new Infos.PostProcessRegistrationParam();
                postProcessRegistrationParm.setLotIDs(Arrays.asList(lotID));
                String strTxId = null;
                if (CimStringUtils.equals(info.getPostProcessID(), BizConstant.SP_POSTPROCESS_ACTIONID_PSM)) {
                    strTxId = BizConstant.TX_ID_P_BRANCH;
                } else {
                    strTxId = BizConstant.TX_ID_P_LOCATE;
                }
                postTaskRegisterReqParams.setPostProcessRegistrationParm(postProcessRegistrationParm);
                postTaskRegisterReqParams.setTransactionID(strTxId);
                postTaskRegisterReqParams.setSequenceNumber(-1);
                Results.PostTaskRegisterReqResult postTaskRegisterReqResult = postService.sxPostTaskRegisterReq(objCommon, postTaskRegisterReqParams);
                // remove TriggerDKey from thread specific data (set blank)
                ThreadContextHolder.removeDataString(BizConstant.SP_THREADSPECIFICDATA_KEY_TRIGGERDKEY);
                //set flag for FOUP PostProcessFlag control
                bLastPostProc = false;
            } else {
                //----------------------------
                // Check lot state
                //----------------------------
                String lotState = lotMethod.lotStateGet(objCommon, lotID);
                if (CimStringUtils.equals(CIMStateConst.CIM_LOT_STATE_ACTIVE, lotState)) {
                    log.info("{} The lot state is Active.", lotID.getValue());
                    //--------------------------
                    // Delete Queue
                    //--------------------------
                    List<Infos.PostProcessActionInfo> actionInfoSeq = new ArrayList<>();
                    Infos.PostProcessActionInfo postProcessActionInfo = new Infos.PostProcessActionInfo();
                    actionInfoSeq.add(postProcessActionInfo);
                    postProcessActionInfo.setDKey(info.getDKey());
                    postProcessActionInfo.setSequenceNumber(info.getSequenceNumber());
                    if (CimStringUtils.equals(info.getPostProcessID(), BizConstant.SP_POSTPROCESS_ACTIONID_EXTERNALPOSTPROCESSEXECREQ)
                            && rc_extPostProc == retCodeConfig.getExtpostprocExecuted().getCode()) {
                        throw new ServiceException(retCodeConfig.getExtpostprocExecuted());
                    } else {
                        List<Infos.PostProcessActionInfo> updatePostProcessActionInfoList = postProcessQueueMethod.postProcessQueueUpdateDR(objCommon, BizConstant.SP_POSTPROCESSACTIONINFO_DELETE, actionInfoSeq);
                    }

                    //------------------------------------------------------------------------------------------------------
                    // If targetType is "LOT" and there are not some queues about the "LOT", the Lot's Hold is released.
                    //------------------------------------------------------------------------------------------------------
                    if (bHoldRelease) {
                        //---------------------------------------------------
                        // Check whether Queue of the same Lot remains yet.
                        //---------------------------------------------------
                        List<Infos.PostProcessActionInfo> postProcessActionInfos3 = null;
                        try {
                            postProcessActionInfos3 = postProcessQueueMethod.postProcessQueueGetDR(objCommon, key, -1L, nullSeqNoList, null, -1, info.getTargetType(), lotID);
                        } catch (ServiceException ex) {
                            if (Validations.isEquals(retCodeConfig.getNotFoundEntry(), ex.getCode())) {
                                // Set InPostProcessFlag=OFF for the Lot.
                                // InPostProcessFlag of the cassette is also set to OFF when InPostProcessFlag of all the lot in the cassette are OFF.
                                lotMethod.lotInPostProcessFlagSet(objCommon, lotID, false);
                                if (lockHoldUseFlag == 1) {
                                    log.info("Release Lot's Hold (LOCK)");
                                    // Release Lot Hold.
                                    Params.HoldLotReleaseReqParams holdLotReleaseReqParams = new Params.HoldLotReleaseReqParams();
                                    holdLotReleaseReqParams.setUser(objCommon.getUser());
                                    holdLotReleaseReqParams.setLotID(lotID);
                                    holdLotReleaseReqParams.setReleaseReasonCodeID(holdReleaseCode);
                                    holdLotReleaseReqParams.setHoldReqList(strHoldListSeq);
                                    try {
                                        lotService.sxHoldLotReleaseReq(objCommon, holdLotReleaseReqParams);
                                    } catch (ServiceException e) {
                                        postTaskExecuteReqResult.setOmCode(new OmCode(e.getCode(), e.getMessage()));
                                        throw new ServiceException(retCodeConfig.getPostprocError(), postTaskExecuteReqResult);
                                    }
                                }
                                //set flag for FOUP PostProcessFlag control
                                bLastPostProc = true;
                            } else {
                                log.info("postProcessQueue_GetDR() != RC_OK");
                                throw ex;
                            }
                        }
                    }
                } else {
                    log.info("{} The lot state is not Active.", lotID.getValue());
                    List<Infos.PostProcessAdditionalInfo> dummyPostProcessAdditionalInfoSeq = new ArrayList<>();
                    postService.sxPostActionModifyReq(objCommon, BizConstant.SP_POSTPROCESSACTIONINFO_DELETEWITHLOT, postProcessActionInfos, dummyPostProcessAdditionalInfoSeq, "");
                    // Set InPostProcessFlag of Lot to OFF
                    lotMethod.lotInPostProcessFlagSet(objCommon, lotID, false);
                    //set flag for FOUP PostProcessFlag control
                    bLastPostProc = true;
                }
            }
            if ((interFabXferFlag || bLastPostProc) && (1 == postProcFlagUseFlag)) {
                //-------------------------------------------------------------------------------------
                //  Adjust cassette PostProcess Flag.
                //-------------------------------------------------------------------------------------
                //Get lot cassette
                ObjectIdentifier lotCassetteOutRetCode = null;
                try {
                    lotCassetteOutRetCode = lotMethod.lotCassetteGet(objCommon, lotID);
                } catch (ServiceException e) {
                    if (Validations.isEquals(retCodeConfig.getNotFoundCst(), e.getCode())) {
                        //Do nothing
                    } else {
                        throw e;
                    }
                }
                boolean lotPostProcessingFlag = postProcessMethod.postProcessLastFlagForCarrierGetDR(objCommon, lotCassetteOutRetCode);
                if (!lotPostProcessingFlag) {
                    Inputs.ObjCassetteInPostProcessFlagSetIn objCassetteInPostProcessFlagSetIn = new Inputs.ObjCassetteInPostProcessFlagSetIn();
                    objCassetteInPostProcessFlagSetIn.setCassetteID(lotCassetteOutRetCode);
                    objCassetteInPostProcessFlagSetIn.setInPostProcessFlag(false);
                    cassetteMethod.cassetteInPostProcessFlagSet(objCommon, objCassetteInPostProcessFlagSetIn);
                }
            }
            Validations.check(interFabXferFlag, retCodeConfig.getInterfabLotxferExecuted());

        }
        //--------------------------------------
        // Post process for durables process
        //--------------------------------------
        else if (CimStringUtils.equals(info.getTargetType(), BizConstant.SP_POSTPROCESS_TARGETTYPE_CAST) && ObjectIdentifier.isEmpty(lotID)) {
            log.info("targetType is CAST");

            OmCode routeCheckRC = retCodeConfig.getSucc();
            try {
                durableMethod.durableOnRouteCheck(objCommon, durableCategory, cassetteID);
            } catch (ServiceException e) {
                routeCheckRC = new OmCode(e.getCode(), e.getMessage());
            }
            if (Validations.isEquals(retCodeConfig.getDurableOnroute(), routeCheckRC)) {
                log.info("durable_OnRoute_Check() == RC_DURABLE_ONROUTE");

                //--------------------------
                // Delete Queue
                //--------------------------
                List<Infos.PostProcessActionInfo> actionInfoSeq = new ArrayList<>();
                Infos.PostProcessActionInfo postProcessActionInfo = new Infos.PostProcessActionInfo();
                postProcessActionInfo.setDKey(info.getDKey());
                postProcessActionInfo.setSequenceNumber(info.getSequenceNumber());
                actionInfoSeq.add(postProcessActionInfo);
                postProcessMethod.postProcessQueueUpdateDR(objCommon, BizConstant.SP_POSTPROCESSACTIONINFO_DELETE, actionInfoSeq);

                //---------------------------------------------------
                // Check whether Queue of the same Durable remains yet.
                //---------------------------------------------------
                try {
                    postProcessMethod.postProcessQueueGetDR(objCommon, key, -1L, nullSeqNoList, null, -1, info.getTargetType(), cassetteID);
                } catch (ServiceException e) {
                    if (Validations.isEquals(retCodeConfig.getNotFoundEntry(), e.getCode())) {
                        // Set InPostProcessFlag of Durable to OFF
                        durableMethod.durableInPostProcessFlagSet(objCommon, durableCategory, cassetteID, false);

                        if (bHoldRelease && bFindLockHoldFlag && lockHoldUseFlag == 1) {
                            // Release Durable Hold.
                            Infos.HoldDurableReleaseReqInParam holdDurableReleaseReqInParam = new Infos.HoldDurableReleaseReqInParam();
                            holdDurableReleaseReqInParam.setDurableCategory(durableCategory);
                            holdDurableReleaseReqInParam.setDurableID(cassetteID);
                            holdDurableReleaseReqInParam.setReleaseReasonCodeID(ObjectIdentifier.buildWithValue(BizConstant.SP_REASON_DURABLELOCKRELEASE));
                            holdDurableReleaseReqInParam.setStrDurableHoldList(strDurableHoldList);
                            try {
                                durableService.sxHoldDrbReleaseReq(objCommon, holdDurableReleaseReqInParam, postTaskExecuteReqParams.getClaimMemo());
                            } catch (ServiceException ex) {
                                if (!Validations.isEquals(ex.getCode(), retCodeConfig.getInvalidCassetteState())
                                        && !Validations.isEquals(ex.getCode(), retCodeConfig.getInvalidReticlepodStat())
                                        && !Validations.isEquals(ex.getCode(), retCodeConfig.getInvalidReticleStat())
                                        && !Validations.isEquals(ex.getCode(), retCodeConfig.getDurableNotAvailableStateForDrblProcess())) {
                                    throw e;
                                }
                            }
                        }
                    } else {
                        throw e;
                    }
                }
            } else {
                List<Infos.PostProcessActionInfo> actionInfoSeq = new ArrayList<>();
                Infos.PostProcessActionInfo postProcessActionInfo = new Infos.PostProcessActionInfo();
                postProcessActionInfo.setDKey(info.getDKey());
                postProcessActionInfo.setSequenceNumber(info.getSequenceNumber());
                actionInfoSeq.add(postProcessActionInfo);
                postProcessMethod.postProcessQueueUpdateDR(objCommon, BizConstant.SP_POSTPROCESSACTIONINFO_DELETE, actionInfoSeq);
            }
        } else {
            //--------------------------
            // Delete Queue
            //--------------------------
            List<Infos.PostProcessActionInfo> actionInfoSeq = new ArrayList<>();
            Infos.PostProcessActionInfo postProcessActionInfo = new Infos.PostProcessActionInfo();
            actionInfoSeq.add(postProcessActionInfo);
            postProcessActionInfo.setDKey(info.getDKey());
            postProcessActionInfo.setSequenceNumber(info.getSequenceNumber());
            postProcessQueueMethod.postProcessQueueUpdateDR(objCommon, BizConstant.SP_POSTPROCESSACTIONINFO_DELETE, actionInfoSeq);
        }
        //-------------------------------------------------------------------------------------
        // commitFlag is checked whether performing with TX_COMMIT or without TX_COMMIT.
        //-------------------------------------------------------------------------------------
        log.info("commitFlag is ... {}", info.getCommitFlag());
        if (info.getCommitFlag()) {
            Validations.check(true, retCodeConfig.getPostprocNextEntryWithCommit());
        } else {
            Validations.check(true, retCodeConfig.getPostprocNextEntryWithoutCommit());
        }
        return postTaskExecuteReqResult;
    }

    @Override
    public Results.PostTaskRegisterReqResult sxPostTaskRegisterReq(Infos.ObjCommon objCommon, Params.PostTaskRegisterReqParams params) {
        String txID = params.getTransactionID();
        String patternID = params.getPatternID();
        String key = params.getKey();
        Integer seqNo = params.getSequenceNumber();
        Infos.PostProcessRegistrationParam strPostProcessRegistrationParam = params.getPostProcessRegistrationParm();
        String claimMemo = params.getClaimMemo();

        Outputs.PostProcessQueueMakeOut postProcessQueueMakeOut = postProcessMethod.postProcessQueueMake(objCommon, txID,
                patternID, key, seqNo, strPostProcessRegistrationParam, claimMemo);

        /*------------------------------------------------------------------------*/
        /* Get LotIDs                                                             */
        /*------------------------------------------------------------------------*/
        log.debug("[step-1] Get LotIDs ");
        List<Infos.PostProcessActionInfo> strActionInfoSeq = postProcessQueueMakeOut.getStrActionInfoSeq();
        int len = CimArrayUtils.getSize(strActionInfoSeq);
        List<ObjectIdentifier> tmpLotIDs = new ArrayList<>();

        int cntLot = 0;
        for (int i = 0; i < len; i++) {
            Infos.PostProcessActionInfo postProcessActionInfo = strActionInfoSeq.get(i);
            int j = 0;
            ObjectIdentifier lotID = postProcessActionInfo.getPostProcessTargetObject().getLotID();
            log.trace("ObjectUtils.isEmpty(lotID) : {}", ObjectIdentifier.isEmpty(lotID));
            if (ObjectIdentifier.isEmpty(lotID)) {
                log.debug("lotID.stringifiedObjectReference is NULL or no Length. {}", lotID);
                //check id
                log.trace("ObjectUtils.isEmpty(lotID) : {}", ObjectIdentifier.isEmpty(lotID));
                if (ObjectIdentifier.isEmpty(lotID)) {
                    log.debug("lotID.stringifiedObjectReference and identifier is NULL or no Length.");
                    continue;
                } else {
                    //Check tmpLotIDs
                    log.debug("[step-1-1] Check tmpLotIDs");
                    for (j = 0; j < cntLot; j++) {
                        log.trace("ObjectUtils.equalsWithValue(tmpLotIDs.get(j), lotID) : {}", ObjectIdentifier.equalsWithValue(tmpLotIDs.get(j), lotID));
                        if (ObjectIdentifier.equalsWithValue(tmpLotIDs.get(j), lotID)) {
                            log.debug("Same Lot.");
                            break;
                        }
                    }
                }
            } else {
                log.debug("lotID.stringifiedObjectReference is {} ", lotID);
                //Check tmpLotIDs
                for (j = 0; j < cntLot; j++) {
                    log.trace("ObjectUtils.equalsWithValue(tmpLotIDs.get(j), lotID) : {}", ObjectIdentifier.equalsWithValue(tmpLotIDs.get(j), lotID));
                    if (ObjectIdentifier.equalsWithValue(tmpLotIDs.get(j), lotID)) {
                        log.debug("Same Lot.");
                        break;
                    }
                }
            }
            //LotID is not found in tmpLotIDs
            log.debug("LotID is not found in tmpLotIDs");
            log.trace("j == cnLot : {})", j == cntLot);
            if (j == cntLot) {
                log.debug("{}  lotID.identifier = ", lotID);
                tmpLotIDs.add(cntLot, lotID);
                cntLot++;
            }
        }
        // Get environment variables
        log.debug("[step-2] Get environment variables");
        int lockHoldUseFlag = StandardProperties.OM_LOCK_HOLD_MODE.getIntValue();
        log.trace("1 == lockHoldUseFlag : {}",1 == lockHoldUseFlag);
        if (1 == lockHoldUseFlag) {
            log.debug("[step-2-1] Lock Hold is usable.");
            /*------------------------------------------------------------------------*/
            /* Hold Lot                                                               */
            /*------------------------------------------------------------------------*/
            List<Infos.LotHoldReq> strHoldListSeq = new ArrayList<>();
            Infos.LotHoldReq lotHoldReq = new Infos.LotHoldReq();
            lotHoldReq.setHoldType(BizConstant.SP_HOLDTYPE_LOTHOLD);
            lotHoldReq.setHoldReasonCodeID(new ObjectIdentifier(BizConstant.SP_REASON_LOTLOCK));
            lotHoldReq.setHoldUserID(new ObjectIdentifier(BizConstant.SP_POST_PROC_PERSON));
            lotHoldReq.setResponsibleOperationMark(BizConstant.SP_RESPONSIBLEOPERATION_CURRENT);
            lotHoldReq.setClaimMemo("");
            strHoldListSeq.add(lotHoldReq);

//            objCommon.setTransactionID("OPOSW002");
            Infos.ObjCommon objCommon1 = new Infos.ObjCommon();
            objCommon1.setTransactionID("OPOSW002");
            objCommon1.setTimeStamp(objCommon.getTimeStamp());
            objCommon1.setUser(objCommon.getUser());
            objCommon1.setReserve(objCommon.getReserve());
            for (int i = 0; i < tmpLotIDs.size(); i++) {
                log.debug("Hold Lot for Post Process");
                try {
                    lotService.sxHoldLotReq(objCommon1, tmpLotIDs.get(i), strHoldListSeq);
                } catch (ServiceException e) {
                    log.trace("!Validations.isEquals(retCodeConfig.getInvalidLotStat(), e.getCode()) : {}",!Validations.isEquals(retCodeConfig.getInvalidLotStat(), e.getCode()));
                    if (!Validations.isEquals(retCodeConfig.getInvalidLotStat(), e.getCode())) {
                        throw e;
                    }
                }
            }
        }

        // Get environment variables
        int postProcFlagUseFlag = StandardProperties.SP_POST_PROC_FLAG_USE_FLAG.getIntValue();
        log.debug("{} postProcFlagUseFlag ", postProcFlagUseFlag);
        log.trace("1 == postProcFlagUseFlag : {}",1 == postProcFlagUseFlag);
        if (1 == postProcFlagUseFlag) {
            log.debug("Post Proc Flag is usable.");
            //-------------------------------------------
            // Under PostProcess parallel execution, cassette's PostProcessFlag
            // isn't set TRUE by lot_inPostProcessFlag_Set() if 1 is set for thread specific data
            // "SP_ThreadSpecificData_Key_PostProcParallelFlag".
            // Then Temporally make SP_ThreadSpecificData_Key_PostProcParallelFlag as "0".
            //-------------------------------------------
            String strParallelPostProcFlag = ThreadContextHolder.getThreadSpecificDataString(BizConstant.SP_THREADSPECIFICDATA_KEY_POSTPROCPARALLELFLAG);
            log.trace("StringUtils.equals(strParallelPostProcFlag, BizConstant.SP_POSTPROCESS_PARALLELEXECUTION_ON) : {}", CimStringUtils.equals(strParallelPostProcFlag, BizConstant.SP_POSTPROCESS_PARALLELEXECUTION_ON));
            if (CimStringUtils.equals(strParallelPostProcFlag, BizConstant.SP_POSTPROCESS_PARALLELEXECUTION_ON)) {
                log.debug("Set SP_PostProcess_ParallelExecution_OFF");
                ThreadContextHolder.setThreadSpecificDataString(BizConstant.SP_THREADSPECIFICDATA_KEY_POSTPROCPARALLELFLAG, BizConstant.SP_POSTPROCESS_PARALLELEXECUTION_OFF);
            }

            //-------------------------------------------------
            //  Set InPostProcessFlag of Lot/Cassette to ON
            //-------------------------------------------------
            log.debug("Set InPostProcessFlag of Lot/Cassette to ON");
            int nTmpLotLen = CimArrayUtils.getSize(tmpLotIDs);
            log.debug("{} tmpLotIDs.length()", nTmpLotLen);
            for (int i = 0; i < nTmpLotLen; i++) {
                lotMethod.lotInPostProcessFlagSet(objCommon, tmpLotIDs.get(i), true);
            }
            //-------------------------------------------
            // In case of Post Process parallel execution, reset SP_PostProcess_ParallelExecution_ON to
            //-------------------------------------------
            log.debug("In case of Post Process parallel execution, reset SP_PostProcess_ParallelExecution_ON to");
            log.trace("StringUtils.equals(strParallelPostProcFlag, BizConstant.SP_POSTPROCESS_PARALLELEXECUTION_ON) : {}", CimStringUtils.equals(strParallelPostProcFlag, BizConstant.SP_POSTPROCESS_PARALLELEXECUTION_ON));
            if (CimStringUtils.equals(strParallelPostProcFlag, BizConstant.SP_POSTPROCESS_PARALLELEXECUTION_ON)) {
                ThreadContextHolder.setThreadSpecificDataString(BizConstant.SP_THREADSPECIFICDATA_KEY_POSTPROCPARALLELFLAG, BizConstant.SP_POSTPROCESS_PARALLELEXECUTION_ON);
            }
        }
        /*------------------------------------------------------------------------*/
        /* Put Action informations to queue                                       */
        /*------------------------------------------------------------------------*/
        log.debug("[step-3] Add action information to Queue.");
        postProcessMethod.postProcessQueueUpdateDR(objCommon, BizConstant.SP_POSTPROCESSACTIONINFO_ADD, strActionInfoSeq);
        //Result
        Results.PostTaskRegisterReqResult postTaskRegisterReqResult = new Results.PostTaskRegisterReqResult();
        postTaskRegisterReqResult.setPostProcessActionInfoList(strActionInfoSeq);
        postTaskRegisterReqResult.setDKey(postProcessQueueMakeOut.getDKey());
        postTaskRegisterReqResult.setKeyTimeStamp(postProcessQueueMakeOut.getKeyTimeStamp());
        return postTaskRegisterReqResult;
    }

    @Override
    public List<Infos.PostProcessActionInfo> sxPostActionModifyReq(Infos.ObjCommon objCommon,
                                                                   String actionCode,
                                                                   List<Infos.PostProcessActionInfo> strPostProcessActionInfoSeq,
                                                                   List<Infos.PostProcessAdditionalInfo> strPostProcessAdditionalInfoSeq,
                                                                   String claimMemo) {
        //-----------------------------------------------------
        //  Check parameter
        //-----------------------------------------------------
        if (!CimStringUtils.equals(actionCode, BizConstant.SP_POSTPROCESSACTIONINFO_ADD)
                && !CimStringUtils.equals(actionCode, BizConstant.SP_POSTPROCESSACTIONINFO_UPDATE)
                && !CimStringUtils.equals(actionCode, BizConstant.SP_POSTPROCESSACTIONINFO_UPDATEFORSTATUS)
                && !CimStringUtils.equals(actionCode, BizConstant.SP_POSTPROCESSACTIONINFO_DELETEADDITIONALINFO)
                && !CimStringUtils.equals(actionCode, BizConstant.SP_POSTPROCESSACTIONINFO_ADDADDITIONALINFO)
                && !CimStringUtils.equals(actionCode, BizConstant.SP_POSTPROCESSACTIONINFO_DELETEWITHCAST)
                && !CimStringUtils.equals(actionCode, BizConstant.SP_POSTPROCESSACTIONINFO_ADDUPDATEADDITIONALINFO)
                && !CimStringUtils.equals(actionCode, BizConstant.SP_POSTPROCESSACTIONINFO_DELETE)
                && !CimStringUtils.equals(actionCode, BizConstant.SP_POSTPROCESSACTIONINFO_DELETEWITHLOT)) {
            throw new ServiceException(retCodeConfig.getInvalidParameter());
        }
        if (CimArrayUtils.isEmpty(strPostProcessActionInfoSeq)) {
            throw new ServiceException(retCodeConfig.getInvalidParameter());
        }
        //-----------------------------------------------------
        //  Get environment variables
        //-----------------------------------------------------
        int lockHoldUseFlag = StandardProperties.OM_LOCK_HOLD_MODE.getIntValue();
        //-------------------------------------------------------
        //  Call postProcessQueue_UpdateDR__100
        //-------------------------------------------------------
        List<Infos.PostProcessActionInfo> updateActionInfoSeq = null;
        if (CimStringUtils.equals(actionCode, BizConstant.SP_POSTPROCESSACTIONINFO_ADD)
                || CimStringUtils.equals(actionCode, BizConstant.SP_POSTPROCESSACTIONINFO_UPDATEFORSTATUS)
                || CimStringUtils.equals(actionCode, BizConstant.SP_POSTPROCESSACTIONINFO_UPDATE)) {
            updateActionInfoSeq = postProcessMethod.postProcessQueueUpdateDR(objCommon, actionCode, strPostProcessActionInfoSeq);
        } else if (CimStringUtils.equals(actionCode, BizConstant.SP_POSTPROCESSACTIONINFO_DELETE)) {
            Validations.check(CimArrayUtils.getSize(strPostProcessActionInfoSeq) != 1, retCodeConfig.getInvalidParameter());
            //----------------------------------------------------------------------------
            // == Delete one or all action records. ==
            // - Delete the action record with the smallest Sequence Number
            //   among all the action records holding the same D_key.
            //   The Sequence Number should be 1 or greater.
            //    (Example)
            //      d_key    seq_no   post_proc_id
            //    ---------------------------------
            //      abcdef    2       Script           <---- deleted
            //      abcdef    3       PlanndedSplit    <---- not be deleted.
            //      abcdef    4       MessageQueuePut  <---- not be deleted.
            //
            //   *** Notification ***                                               //DSN000050720
            //   Above restriction isn't applied to TxPostDoActionForSpecificTaskNoReq.  //DSN000050720
            //   If this tx is called from OPOSW008, seqNo which is specified by    //DSN000050720
            //   strPostProcessActionInfoSeq.seqNo is deleted.                      //DSN000050720
            //   (The check if the record is smallest or not is skipped.)           //DSN000050720
            //
            // - Delete all action records holding the same D_key.
            //   The Sequence Number should be -1 (default).
            //    (Example)
            //      d_key    seq_no   post_proc_id
            //    ---------------------------------
            //      abcdef    2       Script           <---- deleted
            //      abcdef    3       PlanndedSplit    <---- deleted
            //      abcdef    4       MessageQueuePut  <---- deleted
            //      xyz       1       Script           <---- not be deleted.
            //
            //----------------------------------------------------------------------------
            if (strPostProcessActionInfoSeq.get(0).getSequenceNumber() != -1) {
                List<Long> seqNoListDummy = new ArrayList<>();
                ObjectIdentifier objDummy = null;
                if (CimStringUtils.equals(objCommon.getTransactionID(), "OPOSW008")) {
                    seqNoListDummy.add(strPostProcessActionInfoSeq.get(0).getSequenceNumber().longValue());
                }
                List<Infos.PostProcessActionInfo> strActionInfoSeq = postProcessMethod.postProcessQueueGetDR(objCommon, strPostProcessActionInfoSeq.get(0).getDKey(), (long) -1, seqNoListDummy, null, -1, null, objDummy);
                //------------------------------------------------
                // The record of youngest sequence number ??
                // In case of OPOSW008, this youngest check works for different meaning. (Check for ritrieved data.)
                Validations.check(CimArrayUtils.isEmpty(strActionInfoSeq) ||
                                (!CimArrayUtils.isEmpty(strActionInfoSeq) && !Objects.equals(strPostProcessActionInfoSeq.get(0).getSequenceNumber(), strActionInfoSeq.get(0).getSequenceNumber())),
                        retCodeConfigEx.getPostProcDeleteError());
                //------------------------------------------------
                // Delete Schedule Change Reservation if reserved by Lot Schedule Change
                // Looped for sequence but the sequence length should be normally 1.
                //------------------------------------------------
                int postLen = CimArrayUtils.getSize(strActionInfoSeq);
                for (int k = 0; k < postLen; k++) {
                    Infos.PostProcessActionInfo postProcessActionInfo = strActionInfoSeq.get(k);
                    if (CimStringUtils.equals(postProcessActionInfo.getPostProcessID(), BizConstant.SP_POSTPROCESS_ACTIONID_SCHEXEC)) {
                        if (!ObjectIdentifier.isEmptyWithValue(postProcessActionInfo.getPostProcessTargetObject().getLotID())) {
                            //------------------------------------------------------------------------//
                            // Get Previous Operation Info by lotID                                   //
                            //------------------------------------------------------------------------//
                            Outputs.ObjLotPreviousOperationInfoGetOut objLotPreviousOperationInfoGetOut = lotMethod.lotPreviousOperationInfoGet(objCommon, postProcessActionInfo.getPostProcessTargetObject().getLotID());
                            Inputs.ObjSchdlChangeReservationCheckForActionDRIn objSchdlChangeReservationCheckForActionDRIn = new Inputs.ObjSchdlChangeReservationCheckForActionDRIn();
                            objSchdlChangeReservationCheckForActionDRIn.setLotID(postProcessActionInfo.getPostProcessTargetObject().getLotID());
                            objSchdlChangeReservationCheckForActionDRIn.setRouteID(objLotPreviousOperationInfoGetOut.getRouteID().getValue());
                            objSchdlChangeReservationCheckForActionDRIn.setOperationNumber(objLotPreviousOperationInfoGetOut.getOperationNumber());
                            Outputs.ObjSchdlChangeReservationCheckForActionDROut objSchdlChangeReservationCheckForActionDROut = scheduleChangeReservationMethod.schdlChangeReservationCheckForActionDR(objCommon, objSchdlChangeReservationCheckForActionDRIn);
                            /*-------------------------------------------------------------------------------------------*/
                            /*   Send e-mail and delete Schedule Change Reservation                                      */
                            /*   only when Schedule Change Reservation is created in Lot Information Change operation    */
                            /*-------------------------------------------------------------------------------------------*/
                            if (objSchdlChangeReservationCheckForActionDROut.isExistFlag() && objSchdlChangeReservationCheckForActionDROut.getStrSchdlChangeReservation().getLotInfoChangeFlag()) {
                                StringBuffer messageSb = new StringBuffer();
                                messageSb.append("This message was sent because reservation for \\\"Lot Information Change\\\" is canceled.\\n")
                                        .append("Schedule Change Reservation Info.\n")
                                        .append("LotID      : ").append(postProcessActionInfo.getPostProcessTargetObject().getLotID().getValue()).append("\n")
                                        .append("Product ID : ").append(objSchdlChangeReservationCheckForActionDROut.getStrSchdlChangeReservation().getProductID().getValue()).append("\n")
                                        .append("Route ID   : ").append(objSchdlChangeReservationCheckForActionDROut.getStrSchdlChangeReservation().getRouteID().getValue()).append("\n")
                                        .append("Ope.No     : ").append(objSchdlChangeReservationCheckForActionDROut.getStrSchdlChangeReservation().getOperationNumber()).append("\n")
                                        .append("SubLotType : ").append(objSchdlChangeReservationCheckForActionDROut.getStrSchdlChangeReservation().getSubLotType()).append("\n");
                                ObjectIdentifier messageID = new ObjectIdentifier(BizConstant.SP_SYSTEMMSGCODE_SCRNOTICE);
                                messageMethod.messageDistributionMgrPutMessage(objCommon, messageID, postProcessActionInfo.getPostProcessTargetObject().getLotID(), "", null,
                                        objSchdlChangeReservationCheckForActionDROut.getStrSchdlChangeReservation().getRouteID(), objSchdlChangeReservationCheckForActionDROut.getStrSchdlChangeReservation().getOperationNumber(), "", messageSb.toString());
                                scheduleChangeReservationMethod.schdlChangeReservationDeleteDR(objCommon, objSchdlChangeReservationCheckForActionDROut.getStrSchdlChangeReservation());
                            }
                        }
                        break;
                    }
                }
                //------------------------
                // Delete one record
                //------------------------
                updateActionInfoSeq = postProcessMethod.postProcessQueueUpdateDR(objCommon, actionCode, strActionInfoSeq);
                /* for APC I/F */
                if (CimStringUtils.equals(strActionInfoSeq.get(0).getPostProcessID(), BizConstant.SP_POSTPROCESS_ACTIONID_APCDISPOSITION)) {
                    ObjectIdentifier controlJobID = strActionInfoSeq.get(0).getPostProcessTargetObject().getControlJobID();
                    if (controlJobID != null
                            && (!CimStringUtils.isEmpty(controlJobID.getValue()) || !CimStringUtils.isEmpty(controlJobID.getReferenceKey()))) {
                        // TODO APCRuntimeCapability_DeleteDR
                    }
                } else if (CimStringUtils.equals(strActionInfoSeq.get(0).getPostProcessID(), BizConstant.SP_POSTPROCESS_ACTIONID_PARALLELEXECFINALIZE)) {
                    ObjectIdentifier cassetteID = strActionInfoSeq.get(0).getPostProcessTargetObject().getCassetteID();
                    if (cassetteID != null
                            && (!CimStringUtils.isEmpty(cassetteID.getValue()) || !CimStringUtils.isEmpty(cassetteID.getReferenceKey()))) {
                        cassetteMethod.cassetteStatusFinalizeForPostProcess(objCommon, strActionInfoSeq.get(0).getPostProcessTargetObject().getCassetteID());
                    }
                }
                //------------------------------------------------------------------------------------------------------------
                // If Post Processing Execution of same Lot doesn't exist (has finished) the Lot's Hold("LOCK") is released.
                // And it checks from the second of a list whether the lastest lot.
                // Because it is to delete delete the action record with the smallest Sequence Number
                // among all the action records holding the same D_key.
                //------------------------------------------------------------------------------------------------------------
                if (CimStringUtils.equals(strActionInfoSeq.get(0).getTargetType(), BizConstant.SP_POSTPROCESS_TARGETTYPE_LOT)
                        && !ObjectIdentifier.isEmptyWithValue(strActionInfoSeq.get(0).getPostProcessTargetObject().getLotID())) {
                    //---------------------------------------------------
                    // Check whether Queue of the same Lot remains yet.
                    //---------------------------------------------------
                    List<Long> nullSeqNoList = new ArrayList<>();
                    List<Infos.PostProcessActionInfo> strActionInfoSeq2 = null;
                    try {
                        strActionInfoSeq2 = postProcessMethod.postProcessQueueGetDR(objCommon, strActionInfoSeq.get(0).getDKey(), -1L, nullSeqNoList, null, -1,
                                strActionInfoSeq.get(0).getTargetType(), strActionInfoSeq.get(0).getPostProcessTargetObject().getLotID());
                    } catch (ServiceException e) {
                        if (Validations.isEquals(retCodeConfig.getNotFoundEntry(), e.getCode())) {
                            //--------------------------------------
                            //  Release the Lot's Hold("LOCK").
                            //--------------------------------------
                            ObjectIdentifier dummyLot = null;
                            ObjectIdentifier reasonCodeID = new ObjectIdentifier(BizConstant.SP_REASON_LOTLOCKRELEASE);
                            List<Infos.LotHoldReq> strHoldListSeq = new ArrayList<>();
                            Infos.LotHoldReq holdList = new Infos.LotHoldReq();
                            strHoldListSeq.add(holdList);
                            holdList.setHoldType(BizConstant.SP_HOLDTYPE_LOTHOLD);
                            holdList.setHoldReasonCodeID(new ObjectIdentifier(BizConstant.SP_REASON_LOTLOCK));
                            holdList.setHoldUserID(new ObjectIdentifier(BizConstant.SP_POSTPROC_PERSON));
                            holdList.setResponsibleOperationMark(BizConstant.SP_RESPONSIBLEOPERATION_CURRENT);
                            holdList.setRelatedLotID(dummyLot);
                            holdList.setClaimMemo("");
                            //Check lot state
                            String lotState = lotMethod.lotStateGet(objCommon, strActionInfoSeq.get(0).getPostProcessTargetObject().getLotID());
                            if (CimStringUtils.equals(lotState, CIMStateConst.CIM_LOT_STATE_ACTIVE)) {
                                //----------------------------------
                                //  Set InPostProcessFlag to OFF
                                //----------------------------------
                                lotMethod.lotInPostProcessFlagSet(objCommon, strActionInfoSeq.get(0).getPostProcessTargetObject().getLotID(), false);
                                if (lockHoldUseFlag == 1 || lockHoldUseFlag == 0) {
                                    //---------------------------
                                    // Get the lot's hold list.
                                    //---------------------------
                                    List<ObjectIdentifier> lotIDs = new ArrayList<>();
                                    lotIDs.add(strActionInfoSeq.get(0).getPostProcessTargetObject().getLotID());
                                    int retCode = 0;
                                    List<Infos.LotHoldListAttributes> lotHoldListAttributesList = null;
                                    try {
                                        lotHoldListAttributesList = lotMethod.lotFillInTxTRQ005DR(objCommon, lotIDs.get(0));
                                    } catch (ServiceException ex) {
                                        retCode = ex.getCode();
                                        if (!Validations.isEquals(retCodeConfig.getNotFoundEntry(), e.getCode())) {
                                            throw ex;
                                        }
                                    }
                                    boolean bFindLockHoldFlag = false;
                                    if (retCode == 0) {
                                        //---------------------------
                                        // Check LOCK Hold.
                                        //---------------------------
                                        int lotRsnCnt = CimArrayUtils.getSize(lotHoldListAttributesList);
                                        for (int i = 0; i < lotRsnCnt; i++) {
                                            if (ObjectIdentifier.equalsWithValue(lotHoldListAttributesList.get(i).getReasonCodeID(), BizConstant.SP_REASON_LOTLOCK)) {
                                                bFindLockHoldFlag = true;
                                                break;
                                            }
                                        }
                                    }
                                    if (bFindLockHoldFlag) {
                                        Params.HoldLotReleaseReqParams holdLotReleaseReqParams = new Params.HoldLotReleaseReqParams();
                                        holdLotReleaseReqParams.setHoldReqList(strHoldListSeq);
                                        holdLotReleaseReqParams.setReleaseReasonCodeID(reasonCodeID);
                                        holdLotReleaseReqParams.setLotID(strActionInfoSeq.get(0).getPostProcessTargetObject().getLotID());
                                        lotService.sxHoldLotReleaseReq(objCommon, holdLotReleaseReqParams);
                                    }
                                }
                            }
                        } else {
                            throw e;
                        }
                    }
                }
                //--------------------------------------------------------------------------------------------------------------------
                // If Post Processing Execution of same Durable doesn't exist (has finished) the Durable's Hold("LOCK") is released.
                // And it checks from the second of a list whether the lastest Durable.
                // Because it is to delete the action record with the smallest Sequence Number
                // among all the action records holding the same D_key.
                //--------------------------------------------------------------------------------------------------------------------
                if (CimStringUtils.equals(strActionInfoSeq.get(0).getTargetType(), BizConstant.SP_POSTPROCESS_TARGETTYPE_CAST)
                        && !ObjectIdentifier.isEmptyWithValue(strActionInfoSeq.get(0).getPostProcessTargetObject().getCassetteID())) {
                    //-------------------------------------------------------
                    // Check whether Queue of the same durable remains yet.
                    //-------------------------------------------------------
                    List<Long> nullSeqNoList = new ArrayList<>();
                    List<Infos.PostProcessActionInfo> strActionInfoSeq2 = null;
                    try {
                        strActionInfoSeq2 = postProcessMethod.postProcessQueueGetDR(objCommon, strActionInfoSeq.get(0).getDKey(), -1L, nullSeqNoList, null, -1,
                                strActionInfoSeq.get(0).getTargetType(), strActionInfoSeq.get(0).getPostProcessTargetObject().getCassetteID());
                    } catch (ServiceException e) {
                        if (Validations.isEquals(retCodeConfig.getNotFoundEntry(), e.getCode())) {
                            // TODO DURALE相关 Line: 620-724
                        } else {
                            throw e;
                        }
                    }
                }
            } else {
                //------------------------
                // Get all record
                //------------------------
                log.info("Get all record of the specified the dKey {}", strPostProcessActionInfoSeq.get(0).getDKey());
                Infos.PostProcessTargetObject dummyPostProcessTargetObject = new Infos.PostProcessTargetObject();
                Inputs.PostProcessQueueListDRIn postProcessQueueListDRIn = new Inputs.PostProcessQueueListDRIn();
                postProcessQueueListDRIn.setKey(strPostProcessActionInfoSeq.get(0).getDKey());
                postProcessQueueListDRIn.setSeqNo(-1L);
                postProcessQueueListDRIn.setSyncFlag(-1L);
                postProcessQueueListDRIn.setStrPostProcessTargetObject(dummyPostProcessTargetObject);
                postProcessQueueListDRIn.setPassedTime(-1L);
                postProcessQueueListDRIn.setMaxCount(-1L);
                postProcessQueueListDRIn.setCommittedReadFlag(true);
                Outputs.ObjPostProcessQueListDROut objPostProcessQueListDROut = postProcessMethod.postProcessQueueListDR(objCommon, postProcessQueueListDRIn);
                //------------------------------------------------
                // Delete Schedule Change Reservation if reserved by Lot Schedule Change
                //------------------------------------------------
                int postLen = CimArrayUtils.getSize(objPostProcessQueListDROut.getStrActionInfoSeq());
                for (int k = 0; k < postLen; k++) {
                    Infos.PostProcessActionInfo postProcessActionInfo = objPostProcessQueListDROut.getStrActionInfoSeq().get(k);
                    if (CimStringUtils.equals(postProcessActionInfo.getPostProcessID(), BizConstant.SP_POSTPROCESS_ACTIONID_SCHEXEC)) {
                        if (!ObjectIdentifier.isEmptyWithValue(postProcessActionInfo.getPostProcessTargetObject().getLotID())) {
                            //------------------------------------------------------------------------//
                            // Get Previous Operation Info by lotID                                   //
                            //------------------------------------------------------------------------//
                            Outputs.ObjLotPreviousOperationInfoGetOut objLotPreviousOperationInfoGetOut = lotMethod.lotPreviousOperationInfoGet(objCommon, postProcessActionInfo.getPostProcessTargetObject().getLotID());
                            Inputs.ObjSchdlChangeReservationCheckForActionDRIn objSchdlChangeReservationCheckForActionDRIn = new Inputs.ObjSchdlChangeReservationCheckForActionDRIn();
                            objSchdlChangeReservationCheckForActionDRIn.setLotID(postProcessActionInfo.getPostProcessTargetObject().getLotID());
                            objSchdlChangeReservationCheckForActionDRIn.setRouteID(objLotPreviousOperationInfoGetOut.getRouteID().getValue());
                            objSchdlChangeReservationCheckForActionDRIn.setOperationNumber(objLotPreviousOperationInfoGetOut.getOperationNumber());
                            Outputs.ObjSchdlChangeReservationCheckForActionDROut objSchdlChangeReservationCheckForActionDROut = scheduleChangeReservationMethod.schdlChangeReservationCheckForActionDR(objCommon, objSchdlChangeReservationCheckForActionDRIn);
                            /*-------------------------------------------------------------------------------------------*/
                            /*   Send e-mail and delete Schedule Change Reservation                                      */
                            /*   only when Schedule Change Reservation is created in Lot Information Change operation    */
                            /*-------------------------------------------------------------------------------------------*/
                            if (objSchdlChangeReservationCheckForActionDROut.isExistFlag() && objSchdlChangeReservationCheckForActionDROut.getStrSchdlChangeReservation().getLotInfoChangeFlag()) {
                                StringBuffer messageSb = new StringBuffer();
                                messageSb.append("This message was sent because reservation for \\\"Lot Information Change\\\" is canceled.\\n")
                                        .append("Schedule Change Reservation Info.\n")
                                        .append("LotID      : ").append(postProcessActionInfo.getPostProcessTargetObject().getLotID().getValue()).append("\n")
                                        .append("Product ID : ").append(objSchdlChangeReservationCheckForActionDROut.getStrSchdlChangeReservation().getProductID().getValue()).append("\n")
                                        .append("Route ID   : ").append(objSchdlChangeReservationCheckForActionDROut.getStrSchdlChangeReservation().getRouteID().getValue()).append("\n")
                                        .append("Ope.No     : ").append(objSchdlChangeReservationCheckForActionDROut.getStrSchdlChangeReservation().getOperationNumber()).append("\n")
                                        .append("SubLotType : ").append(objSchdlChangeReservationCheckForActionDROut.getStrSchdlChangeReservation().getSubLotType()).append("\n");
                                ObjectIdentifier messageID = new ObjectIdentifier(BizConstant.SP_SYSTEMMSGCODE_SCRNOTICE);
                                messageMethod.messageDistributionMgrPutMessage(objCommon, messageID, postProcessActionInfo.getPostProcessTargetObject().getLotID(), "", null,
                                        objLotPreviousOperationInfoGetOut.getRouteID(), objLotPreviousOperationInfoGetOut.getOperationNumber(), "", messageSb.toString());
                                scheduleChangeReservationMethod.schdlChangeReservationDeleteDR(objCommon, objSchdlChangeReservationCheckForActionDROut.getStrSchdlChangeReservation());
                            }
                        }
                    }
                }
                //------------------------
                // Delete all record
                //------------------------
                updateActionInfoSeq = postProcessMethod.postProcessQueueUpdateDR(objCommon, actionCode, strPostProcessActionInfoSeq);
                /* for APC I/F */
                if (!CimArrayUtils.isEmpty(objPostProcessQueListDROut.getStrActionInfoSeq())) {
                    for (int i = 0; i < objPostProcessQueListDROut.getStrActionInfoSeq().size(); i++) {
                        Infos.PostProcessActionInfo postProcessActionInfo = objPostProcessQueListDROut.getStrActionInfoSeq().get(i);
                        if (CimStringUtils.equals(postProcessActionInfo.getPostProcessID(), BizConstant.SP_POSTPROCESS_ACTIONID_APCDISPOSITION)) {
                            if (postProcessActionInfo.getPostProcessTargetObject().getControlJobID() != null
                                    && (!CimStringUtils.isEmpty(postProcessActionInfo.getPostProcessTargetObject().getControlJobID().getReferenceKey())
                                    || !CimStringUtils.isEmpty(postProcessActionInfo.getPostProcessTargetObject().getControlJobID().getValue()))) {
                                // TODO APCRuntimeCapability_DeleteDR
                            }
                        } else if (CimStringUtils.equals(postProcessActionInfo.getPostProcessID(), BizConstant.SP_POSTPROCESS_ACTIONID_PARALLELEXECFINALIZE)) {
                            ObjectIdentifier cassetteID = postProcessActionInfo.getPostProcessTargetObject().getCassetteID();
                            if (cassetteID != null
                                    && (!CimStringUtils.isEmpty(cassetteID.getValue()) || !CimStringUtils.isEmpty(cassetteID.getReferenceKey()))) {
                                cassetteMethod.cassetteStatusFinalizeForPostProcess(objCommon, postProcessActionInfo.getPostProcessTargetObject().getCassetteID());
                            }
                        }
                    }
                }
                //-------------------------------------------------------------------------
                //  If the lot is specified, its Hold("LOCK") is released.
                //-------------------------------------------------------------------------
                int cntLot = 0;
                List<ObjectIdentifier> tmpLotIDs = new ArrayList<>();
                for (int i = 0; i < objPostProcessQueListDROut.getStrActionInfoSeq().size(); i++) {
                    int j = 0;
                    ObjectIdentifier lotID = objPostProcessQueListDROut.getStrActionInfoSeq().get(i).getPostProcessTargetObject().getLotID();
                    if (lotID != null && CimStringUtils.isEmpty(lotID.getReferenceKey())) {
                        if (CimStringUtils.isEmpty(lotID.getValue())) {
                            continue;
                        } else {
                            for (j = 0; j < cntLot; j++) {
                                if (ObjectIdentifier.equalsWithValue(tmpLotIDs.get(j), lotID)) {
                                    log.info("same lot.");
                                    break;
                                }
                            }
                        }
                    } else {
                        //Check tmpLotIDs.
                        for (j = 0; j < cntLot; j++) {
                            if (CimStringUtils.equals(tmpLotIDs.get(j).getReferenceKey(), lotID.getReferenceKey())) {
                                log.info("Same lot.");
                                break;
                            }
                        }
                    }
                    //LotID is not found in tmpLotIDs.
                    if (j == cntLot) {
                        tmpLotIDs.add(lotID);
                        cntLot++;
                    }
                }
                if (cntLot > 0) {
                    //--------------------------------------
                    //  Release the Lot's Hold("LOCK").
                    //--------------------------------------
                    ObjectIdentifier dummyLot = null;
                    ObjectIdentifier reasonCodeID = new ObjectIdentifier(BizConstant.SP_REASON_LOTLOCKRELEASE);
                    List<Infos.LotHoldReq> strHoldListSeq = new ArrayList<>();
                    Infos.LotHoldReq holdList = new Infos.LotHoldReq();
                    strHoldListSeq.add(holdList);
                    holdList.setHoldType(BizConstant.SP_HOLDTYPE_LOTHOLD);
                    holdList.setHoldReasonCodeID(new ObjectIdentifier(BizConstant.SP_REASON_LOTLOCK));
                    holdList.setHoldUserID(new ObjectIdentifier(BizConstant.SP_POSTPROC_PERSON));
                    holdList.setResponsibleOperationMark(BizConstant.SP_RESPONSIBLEOPERATION_CURRENT);
                    holdList.setRelatedLotID(dummyLot);
                    holdList.setClaimMemo("");
                    for (int i = 0; i < tmpLotIDs.size(); i++) {
                        //Check lot state
                        String lotState = lotMethod.lotStateGet(objCommon, tmpLotIDs.get(i));
                        if (CimStringUtils.equals(lotState, CIMStateConst.CIM_LOT_STATE_ACTIVE)) {
                            //----------------------------------
                            //  Set InPostProcessFlag to OFF
                            //----------------------------------
                            lotMethod.lotInPostProcessFlagSet(objCommon, tmpLotIDs.get(i), false);
                            if (lockHoldUseFlag == 1 || lockHoldUseFlag == 0) {
                                //---------------------------
                                // Get the lot's hold list.
                                int retCode = 0;
                                List<Infos.LotHoldListAttributes> lotHoldListAttributesList = null;
                                try {
                                    lotHoldListAttributesList = lotMethod.lotFillInTxTRQ005DR(objCommon, tmpLotIDs.get(i));
                                } catch (ServiceException ex) {
                                    retCode = ex.getCode();
                                    if (!Validations.isEquals(retCodeConfig.getNotFoundEntry(), ex.getCode())) {
                                        throw ex;
                                    }
                                }
                                boolean bFindLockHoldFlag = false;
                                if (retCode == 0) {
                                    //---------------------------
                                    // Check LOCK Hold.
                                    //---------------------------
                                    int lotRsnCnt = CimArrayUtils.getSize(lotHoldListAttributesList);
                                    for (int j = 0; j < lotRsnCnt; j++) {
                                        if (ObjectIdentifier.equalsWithValue(lotHoldListAttributesList.get(j).getReasonCodeID(), BizConstant.SP_REASON_LOTLOCK)) {
                                            bFindLockHoldFlag = true;
                                            break;
                                        }
                                    }
                                }
                                if (bFindLockHoldFlag) {
                                    Params.HoldLotReleaseReqParams holdLotReleaseReqParams = new Params.HoldLotReleaseReqParams();
                                    holdLotReleaseReqParams.setHoldReqList(strHoldListSeq);
                                    holdLotReleaseReqParams.setReleaseReasonCodeID(reasonCodeID);
                                    holdLotReleaseReqParams.setLotID(tmpLotIDs.get(i));
                                    lotService.sxHoldLotReleaseReq(objCommon, holdLotReleaseReqParams);
                                }
                            }
                        }
                    }
                }
                //If Lot's post process action is nothing, check durable's post process action
                if (cntLot == 0) {
                    int cntDurable = 0;
                    List<ObjectIdentifier> tmpDurableIDs = new ArrayList<>();
                    for (int i = 0; i < objPostProcessQueListDROut.getStrActionInfoSeq().size(); i++) {
                        //-------------------------------------------------------------------------
                        //  If the durable is specified, create durableID list
                        //-------------------------------------------------------------------------
                        int tmpDurableIDindex = 0;
                        //check objref
                        ObjectIdentifier cassetteID = objPostProcessQueListDROut.getStrActionInfoSeq().get(i).getPostProcessTargetObject().getCassetteID();
                        if (cassetteID != null && CimStringUtils.isEmpty(cassetteID.getReferenceKey())) {
                            //check id
                            if (CimStringUtils.isEmpty(cassetteID.getValue())) {
                                continue;
                            } else {
                                //Check tmpDurableIDs.
                                for (tmpDurableIDindex = 0; tmpDurableIDindex < cntDurable; tmpDurableIDindex++) {
                                    if (ObjectIdentifier.equalsWithValue(tmpDurableIDs.get(tmpDurableIDindex), cassetteID)) {
                                        break;
                                    }
                                }
                            }
                        } else {
                            //Check tmpDurableIDs.
                            for (tmpDurableIDindex = 0; tmpDurableIDindex < cntDurable; tmpDurableIDindex++) {
                                if (CimStringUtils.equals(tmpDurableIDs.get(tmpDurableIDindex).getReferenceKey(), cassetteID.getReferenceKey())) {
                                    break;
                                }
                            }
                        }
                        //cassetteID is not found in tmpDurableIDs.
                        if (tmpDurableIDindex == cntDurable) {
                            tmpDurableIDs.add(cassetteID);
                            cntDurable++;
                        }
                    }
                    if (cntDurable > 0) {
                        // TODO durable相关 Line:1211-1317
                    }
                }
            }
        } else if (CimStringUtils.equals(actionCode, BizConstant.SP_POSTPROCESSACTIONINFO_DELETEWITHLOT)) {
            // Check lentgh.
            Validations.check(CimArrayUtils.getSize(strPostProcessActionInfoSeq) != 1, retCodeConfig.getInvalidParameter());
            // Check lotID, its objectidentifiedReference exist.
            Validations.check(CimStringUtils.isEmpty(strPostProcessActionInfoSeq.get(0).getPostProcessTargetObject().getLotID().getValue())
                    && CimStringUtils.isEmpty(strPostProcessActionInfoSeq.get(0).getPostProcessTargetObject().getLotID().getReferenceKey()), retCodeConfig.getInvalidParameter());

            //---------------------------------------------------------------------------------------------
            //  Delete by Lot.
            //  - Delete all action records holding the same LotID and D_key,
            //    and their Target Type being "LOT."
            //    (Example)
            //      d_key    seq_no   TargetType  LotID   post_proc_id     TargetType
            //    --------------------------------------------------------------------
            //      abcdef    1       EQP                 MessageQueuePut  <---- not be deleted.
            //      abcdef    2       LOT         LOT-A   APCDisposition   <---- deleted.
            //      abcdef    3       LOT         LOT-A   Script           <---- deleted.
            //      abcdef    4       LOT         LOT-A   PlanndedSplit    <---- deleted.
            //      abcdef    5       LOT         LOT-B   APCDisposition   <---- not be deleted.
            //      abcdef    6       LOT         LOT-B   Script           <---- not be deleted.
            //      abcdef    7       LOT         LOT-B   PlanndedSplit    <---- not be deleted.
            //
            // * LOT-A has the trigger of APCDispotion.
            //   So, The record related to lot is deleted in FSRUNCAPA table.
            // * When Delete operation is used, the system automatically checks the Lot Conditions,
            //   and takes some actions as necessary:
            //->1. IF there is no two Post Processing Actions holding the same LotID and D_key,
            //   THEN release the HoldLot having the reason code "LOCK."
            //->2. IF the lot requires APC Disposition transaction, THEN delete the information of the lot
            //   in FSRUNCAPAxx Table.
            //---------------------------------------------------------------------------------------------
            //-------------------------------------------------------------
            // Check if SCHExec queue remains.
            //-------------------------------------------------------------
            Infos.PostProcessTargetObject strPostProcessTargetObject = new Infos.PostProcessTargetObject();
            strPostProcessTargetObject.setLotID(strPostProcessActionInfoSeq.get(0).getPostProcessTargetObject().getLotID());
            Inputs.PostProcessQueueListDRIn postProcessQueueListDRIn = new Inputs.PostProcessQueueListDRIn();
            postProcessQueueListDRIn.setKey(strPostProcessActionInfoSeq.get(0).getDKey());
            postProcessQueueListDRIn.setSeqNo(-1L);
            postProcessQueueListDRIn.setPostProcId(BizConstant.SP_POSTPROCESS_ACTIONID_SCHEXEC);
            postProcessQueueListDRIn.setSyncFlag(-1L);
            postProcessQueueListDRIn.setTargetType(BizConstant.SP_POSTPROCESS_TARGETTYPE_LOT);
            postProcessQueueListDRIn.setStrPostProcessTargetObject(strPostProcessTargetObject);
            postProcessQueueListDRIn.setPassedTime(-1L);
            postProcessQueueListDRIn.setMaxCount(1L);
            postProcessQueueListDRIn.setCommittedReadFlag(true);
            Outputs.ObjPostProcessQueListDROut objPostProcessQueListDROut = postProcessMethod.postProcessQueueListDR(objCommon, postProcessQueueListDRIn);
            int postSCHExLen = CimArrayUtils.getSize(objPostProcessQueListDROut.getStrActionInfoSeq());
            if (postSCHExLen > 0) {
                for (int k = 0; k < postSCHExLen; k++) {
                    Infos.PostProcessActionInfo postProcessActionInfo = objPostProcessQueListDROut.getStrActionInfoSeq().get(k);
                    if (!ObjectIdentifier.isEmptyWithValue(postProcessActionInfo.getPostProcessTargetObject().getLotID())) {
                        //------------------------------------------------------------------------//
                        // Get Previous Operation Info by lotID                                   //
                        //------------------------------------------------------------------------//
                        Outputs.ObjLotPreviousOperationInfoGetOut objLotPreviousOperationInfoGetOut = lotMethod.lotPreviousOperationInfoGet(objCommon, postProcessActionInfo.getPostProcessTargetObject().getLotID());
                        Inputs.ObjSchdlChangeReservationCheckForActionDRIn objSchdlChangeReservationCheckForActionDRIn = new Inputs.ObjSchdlChangeReservationCheckForActionDRIn();
                        objSchdlChangeReservationCheckForActionDRIn.setLotID(postProcessActionInfo.getPostProcessTargetObject().getLotID());
                        objSchdlChangeReservationCheckForActionDRIn.setRouteID(objLotPreviousOperationInfoGetOut.getRouteID().getValue());
                        objSchdlChangeReservationCheckForActionDRIn.setOperationNumber(objLotPreviousOperationInfoGetOut.getOperationNumber());
                        Outputs.ObjSchdlChangeReservationCheckForActionDROut objSchdlChangeReservationCheckForActionDROut = scheduleChangeReservationMethod.schdlChangeReservationCheckForActionDR(objCommon, objSchdlChangeReservationCheckForActionDRIn);
                        /*-------------------------------------------------------------------------------------------*/
                        /*   Send e-mail and delete Schedule Change Reservation                                      */
                        /*   only when Schedule Change Reservation is created in Lot Information Change operation    */
                        /*-------------------------------------------------------------------------------------------*/
                        if (objSchdlChangeReservationCheckForActionDROut.isExistFlag() && objSchdlChangeReservationCheckForActionDROut.getStrSchdlChangeReservation().getLotInfoChangeFlag()) {
                            StringBuffer messageSb = new StringBuffer();
                            messageSb.append("This message was sent because reservation for \\\"Lot Information Change\\\" is canceled.\\n")
                                    .append("Schedule Change Reservation Info.\n")
                                    .append("LotID      : ").append(postProcessActionInfo.getPostProcessTargetObject().getLotID().getValue()).append("\n")
                                    .append("Product ID : ").append(objSchdlChangeReservationCheckForActionDROut.getStrSchdlChangeReservation().getProductID().getValue()).append("\n")
                                    .append("Route ID   : ").append(objSchdlChangeReservationCheckForActionDROut.getStrSchdlChangeReservation().getRouteID().getValue()).append("\n")
                                    .append("Ope.No     : ").append(objSchdlChangeReservationCheckForActionDROut.getStrSchdlChangeReservation().getOperationNumber()).append("\n")
                                    .append("SubLotType : ").append(objSchdlChangeReservationCheckForActionDROut.getStrSchdlChangeReservation().getSubLotType()).append("\n");
                            ObjectIdentifier messageID = new ObjectIdentifier(BizConstant.SP_SYSTEMMSGCODE_SCRNOTICE);
                            messageMethod.messageDistributionMgrPutMessage(objCommon, messageID, postProcessActionInfo.getPostProcessTargetObject().getLotID(), "", null,
                                    objLotPreviousOperationInfoGetOut.getRouteID(), objLotPreviousOperationInfoGetOut.getOperationNumber(), "", messageSb.toString());
                            scheduleChangeReservationMethod.schdlChangeReservationDeleteDR(objCommon, objSchdlChangeReservationCheckForActionDROut.getStrSchdlChangeReservation());
                        }
                        break;
                    }
                }
            }
            //-------------------------------------------------------------
            // Check the APCDisposition queue remains. Before deletion.
            //-------------------------------------------------------------
            Infos.PostProcessTargetObject dummyPostProcessTargetObject = new Infos.PostProcessTargetObject();
            Inputs.PostProcessQueueListDRIn postProcessQueueListDRInApc = new Inputs.PostProcessQueueListDRIn();
            postProcessQueueListDRInApc.setKey(strPostProcessActionInfoSeq.get(0).getDKey());
            postProcessQueueListDRInApc.setSeqNo(-1L);
            postProcessQueueListDRInApc.setPostProcId(BizConstant.SP_POSTPROCESS_ACTIONID_APCDISPOSITION);
            postProcessQueueListDRInApc.setSyncFlag(-1L);
            postProcessQueueListDRInApc.setStrPostProcessTargetObject(dummyPostProcessTargetObject);
            postProcessQueueListDRInApc.setPassedTime(-1L);
            postProcessQueueListDRInApc.setMaxCount(1L);
            postProcessQueueListDRInApc.setCommittedReadFlag(true);
            Outputs.ObjPostProcessQueListDROut objPostProcessQueListDROutBefore = postProcessMethod.postProcessQueueListDR(objCommon, postProcessQueueListDRInApc);
            //----------------------------
            // Delete record
            //----------------------------
            updateActionInfoSeq = postProcessMethod.postProcessQueueUpdateDR(objCommon, BizConstant.SP_POSTPROCESSACTIONINFO_DELETE, strPostProcessActionInfoSeq);
            //----------------------------
            // Delete record (with Lot)
            //----------------------------
            try {
                updateActionInfoSeq = postProcessMethod.postProcessQueueUpdateDR(objCommon, actionCode, strPostProcessActionInfoSeq);
            } catch (ServiceException e) {
                if (!Validations.isEquals(retCodeConfig.getNotFoundEntry(), e.getCode())) {
                    throw e;
                }
            }
            //------------------------------------------------------------
            // Check the APCDisposition queue remains.  After deletion.
            //------------------------------------------------------------
            Inputs.PostProcessQueueListDRIn postProcessQueueListDRInAfter = new Inputs.PostProcessQueueListDRIn();
            postProcessQueueListDRInAfter.setKey(strPostProcessActionInfoSeq.get(0).getDKey());
            postProcessQueueListDRInAfter.setSeqNo(-1L);
            postProcessQueueListDRInAfter.setPostProcId(BizConstant.SP_POSTPROCESS_ACTIONID_APCDISPOSITION);
            postProcessQueueListDRInAfter.setSyncFlag(-1L);
            postProcessQueueListDRInAfter.setStrPostProcessTargetObject(dummyPostProcessTargetObject);
            postProcessQueueListDRInAfter.setPassedTime(-1L);
            postProcessQueueListDRInAfter.setMaxCount(1L);
            postProcessQueueListDRInAfter.setCommittedReadFlag(true);
            Outputs.ObjPostProcessQueListDROut objPostProcessQueListDROutAfter = postProcessMethod.postProcessQueueListDR(objCommon, postProcessQueueListDRInAfter);
            // Delete Runcapa
            if (!CimArrayUtils.isEmpty(objPostProcessQueListDROutBefore.getStrActionInfoSeq())
                    && CimArrayUtils.isEmpty(objPostProcessQueListDROutAfter.getStrActionInfoSeq())) {
                ObjectIdentifier controlJobID = strPostProcessActionInfoSeq.get(0).getPostProcessTargetObject().getControlJobID();
                if (controlJobID != null
                        && (!CimStringUtils.isEmpty(strPostProcessActionInfoSeq.get(0).getPostProcessTargetObject().getControlJobID().getReferenceKey())
                        || !CimStringUtils.isEmpty(strPostProcessActionInfoSeq.get(0).getPostProcessTargetObject().getControlJobID().getValue()))) {
                    //---------------------------------------
                    // Delete the record in RUNCAPA table.
                    //---------------------------------------
                    // TODO APCRuntimeCapability_DeleteDR
                } else {
                    throw new ServiceException(retCodeConfig.getInvalidParameter());
                }
            }
            //--------------------------------------
            //  Release the Lot's Hold("LOCK").
            //--------------------------------------
            ObjectIdentifier dummyLot = null;
            ObjectIdentifier reasonCodeID = new ObjectIdentifier(BizConstant.SP_REASON_LOTLOCKRELEASE);
            List<Infos.LotHoldReq> strHoldListSeq = new ArrayList<>();
            Infos.LotHoldReq holdList = new Infos.LotHoldReq();
            strHoldListSeq.add(holdList);
            holdList.setHoldType(BizConstant.SP_HOLDTYPE_LOTHOLD);
            holdList.setHoldReasonCodeID(new ObjectIdentifier(BizConstant.SP_REASON_LOTLOCK));
            holdList.setHoldUserID(new ObjectIdentifier(BizConstant.SP_POSTPROC_PERSON));
            holdList.setResponsibleOperationMark(BizConstant.SP_RESPONSIBLEOPERATION_CURRENT);
            holdList.setRelatedLotID(dummyLot);
            holdList.setClaimMemo("");
            //Check lot state
            String lotState = lotMethod.lotStateGet(objCommon, strPostProcessActionInfoSeq.get(0).getPostProcessTargetObject().getLotID());
            if (CimStringUtils.equals(lotState, CIMStateConst.CIM_LOT_STATE_ACTIVE)) {
                //----------------------------------
                //  Set InPostProcessFlag to OFF
                //----------------------------------
                lotMethod.lotInPostProcessFlagSet(objCommon, strPostProcessActionInfoSeq.get(0).getPostProcessTargetObject().getLotID(), false);
                if (lockHoldUseFlag == 1 || lockHoldUseFlag == 0) {
                    //---------------------------
                    // Get the lot's hold list.
                    //---------------------------
                    List<ObjectIdentifier> lotIDs = new ArrayList<>();
                    lotIDs.add(strPostProcessActionInfoSeq.get(0).getPostProcessTargetObject().getLotID());
                    int retCode = 0;
                    List<Infos.LotHoldListAttributes> lotHoldListAttributesList = null;
                    try {
                        lotHoldListAttributesList = lotMethod.lotFillInTxTRQ005DR(objCommon, lotIDs.get(0));
                    } catch (ServiceException e) {
                        retCode = e.getCode();
                        if (!Validations.isEquals(retCodeConfig.getNotFoundEntry(), e.getCode())) {
                            throw e;
                        }
                    }
                    boolean bFindLockHoldFlag = false;
                    if (retCode == 0) {
                        //---------------------------
                        // Check LOCK Hold.
                        //---------------------------
                        int lotRsnCnt = CimArrayUtils.getSize(lotHoldListAttributesList);
                        for (int i = 0; i < lotRsnCnt; i++) {
                            Infos.LotHoldListAttributes lotHoldListAttributes = lotHoldListAttributesList.get(i);
                            if (ObjectIdentifier.equalsWithValue(lotHoldListAttributes.getReasonCodeID(), BizConstant.SP_REASON_LOTLOCK)) {
                                bFindLockHoldFlag = true;
                                break;
                            }
                        }
                    }
                    if (bFindLockHoldFlag) {
                        //----------------------
                        // Release LOCK Hold.
                        //----------------------
                        Params.HoldLotReleaseReqParams holdLotReleaseReqParams = new Params.HoldLotReleaseReqParams();
                        holdLotReleaseReqParams.setHoldReqList(strHoldListSeq);
                        holdLotReleaseReqParams.setReleaseReasonCodeID(reasonCodeID);
                        holdLotReleaseReqParams.setLotID(strPostProcessActionInfoSeq.get(0).getPostProcessTargetObject().getLotID());
                        lotService.sxHoldLotReleaseReq(objCommon, holdLotReleaseReqParams);
                    }
                }
            }
        } else if (CimStringUtils.equals(actionCode, BizConstant.SP_POSTPROCESSACTIONINFO_DELETEWITHCAST)) {
            Validations.check(CimArrayUtils.getSize(strPostProcessActionInfoSeq) != 1, retCodeConfig.getInvalidParameter());
            Validations.check(ObjectIdentifier.isEmptyWithValue(strPostProcessActionInfoSeq.get(0).getPostProcessTargetObject().getCassetteID()), retCodeConfig.getInvalidParameter());
            updateActionInfoSeq = postProcessMethod.postProcessQueueUpdateDR(objCommon, BizConstant.SP_POSTPROCESSACTIONINFO_DELETE, strPostProcessActionInfoSeq);
            try {
                updateActionInfoSeq = postProcessMethod.postProcessQueueUpdateDR(objCommon, actionCode, strPostProcessActionInfoSeq);
            } catch (ServiceException e) {
                if (!Validations.isEquals(retCodeConfig.getNotFoundEntry(), e.getCode())) {
                    throw e;
                }
            }
            // TODO durable相关 Line： 1833-1935
        } else if (CimStringUtils.equals(actionCode, BizConstant.SP_POSTPROCESSACTIONINFO_ADDADDITIONALINFO)) {
            // insert additional information
            postProcessMethod.postProcessAdditionalInfoInsertDR(objCommon, strPostProcessAdditionalInfoSeq);
        } else if (CimStringUtils.equals(actionCode, BizConstant.SP_POSTPROCESSACTIONINFO_DELETEADDITIONALINFO)) {
            try {
                postProcessMethod.postProcessAdditionalInfoDeleteDR(objCommon, strPostProcessActionInfoSeq.get(0).getDKey());
            } catch (ServiceException e) {
                if (Validations.isEquals(retCodeConfig.getPostprocQueueExist(), e.getCode())) {
                    return updateActionInfoSeq;
                } else {
                    throw e;
                }
            }
        } else if (CimStringUtils.equals(actionCode, BizConstant.SP_POSTPROCESSACTIONINFO_ADDUPDATEADDITIONALINFO)) {
            // insert update additional information
            postProcessMethod.postProcessAdditionalInfoInsertUpdateDR(objCommon, strPostProcessAdditionalInfoSeq);
        }
        return updateActionInfoSeq;
    }

    @Override
    public void sxPostFilterCreateForExtReq(Infos.ObjCommon objCommon, Infos.PostFilterCreateForExtReqInParm parm, String claimMemo) {

        String objectType = parm.getObjectType();
        ObjectIdentifier objectID = parm.getObjectID();

        log.info("in-parm objectType     {}", objectType);
        log.info("in-parm objectID       {}", objectID);

        //  --------------------------------------------------
        //  1. objectType check
        //      objectType should be one of followings
        //      (Lot/ProductSpec/ProductGroup/Technology)
        //  --------------------------------------------------

        Validations.check(!CimStringUtils.equals(objectType, BizConstant.SP_POSTPROCESS_OBJECTTYPE_PRODUCTSPEC) &&
                !CimStringUtils.equals(objectType, BizConstant.SP_POSTPROCESS_OBJECTTYPE_PRODUCTGROUP) &&
                !CimStringUtils.equals(objectType, BizConstant.SP_POSTPROCESS_OBJECTTYPE_TECHNOLOGY) &&
                !CimStringUtils.equals(objectType, BizConstant.SP_POSTPROCESS_OBJECTTYPE_LOT), retCodeConfigEx.getInvalidObjectType(), objectType);

        //  --------------------------------------------------
        //  2. objectID check
        //  --------------------------------------------------
        postProcessMethod.postProcessFilterRegistCheckDR(objCommon, objectType, objectID);

        if (CimStringUtils.equals(objectType, BizConstant.SP_POSTPROCESS_OBJECTTYPE_LOT)) {
            //  -----------------------------------------------------------
            //  Get lot interFabXferState
            //  -----------------------------------------------------------
            String interFabXferState = lotMethod.lotInterFabXferStateGet(objCommon, objectID);

            //  ------------------------------------------------------------------------
            //  Check lot InterFabXfer State.  If it "Transferring", reject a request.
            //  ------------------------------------------------------------------------
            Validations.check(CimStringUtils.equals(interFabXferState, BizConstant.SP_INTERFAB_XFERSTATE_TRANSFERRING), retCodeConfig.getInterfabInvalidLotXferstateForReq(), objectID, interFabXferState);
        }
        //----------------------------------------------------------------------
        //  Register filter
        //----------------------------------------------------------------------
        postProcessMethod.postProcessFilterInsertDR(objCommon, objectType, objectID);
    }

    @Override
    public void sxPostFilterRemoveForExtReq(Infos.ObjCommon objCommon, List<Infos.ExternalPostProcessFilterInfo> externalPostProcessFilterInfos, String claimMemo) {

        for (Infos.ExternalPostProcessFilterInfo externalPostProcessFilterInfo : externalPostProcessFilterInfos) {
            log.info("# in-parm objectType    {}", externalPostProcessFilterInfo.getObjectType());
            log.info("# in-parm objectID      {}", externalPostProcessFilterInfo.getObjectID());
            log.info("# in-parm claimUserID   {}", externalPostProcessFilterInfo.getClaimUserID());
        }

        //----------------------------------------------------------------
        //  Pre Process
        //----------------------------------------------------------------
        Validations.check(CimObjectUtils.isEmpty(externalPostProcessFilterInfos), retCodeConfig.getInvalidParameterWithMsg(), "strExternalPostProcessFilterInfoSeq should not be empty");

        //----------------------------------------------------------------------
        //  Delete filter
        //----------------------------------------------------------------------
        postProcessMethod.postProcessFilterDeleteDR(objCommon, externalPostProcessFilterInfos);
    }
}