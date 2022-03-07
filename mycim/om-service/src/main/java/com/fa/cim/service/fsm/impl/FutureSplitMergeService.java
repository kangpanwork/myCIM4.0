package com.fa.cim.service.fsm.impl;

import com.fa.cim.annotaion.OmService;
import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.OmCode;
import com.fa.cim.common.support.RetCode;
import com.fa.cim.common.utils.*;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.config.RetCodeConfigEx;
import com.fa.cim.dto.*;
import com.fa.cim.method.*;
import com.fa.cim.newcore.bo.product.CimLot;
import com.fa.cim.newcore.bo.product.CimLotFamily;
import com.fa.cim.newcore.impl.bo.env.StandardProperties;
import com.fa.cim.service.fsm.IFutureSplitMergeService;
import com.fa.cim.service.lot.ILotService;
import com.fa.cim.service.post.IPostService;
import com.fa.cim.service.system.ISystemService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/9/8        ********            Nyx                create file
 *
 * @author: Nyx
 * @date: 2020/9/8 16:29
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@OmService
public class FutureSplitMergeService implements IFutureSplitMergeService {
    @Autowired
    private RetCodeConfig retCodeConfig;

    @Autowired
    private ILotMethod lotMethod;

    @Autowired
    private ILotFamilyMethod lotFamilyMethod;

    @Autowired
    private IExperimentalForFutureMethod experimentalForFutureMethod;

    @Autowired
    private IEventMethod eventMethod;

    @Autowired
    private IWaferMethod waferMethod;

    @Autowired
    private IProcessMethod processMethod;

    @Autowired
    private ICassetteMethod cassetteMethod;

    @Autowired
    private IScheduleChangeReservationMethod scheduleChangeReservationMethod;

    @Autowired
    private ILotService lotService;

    @Autowired
    private IPostService postService;

    @Autowired
    private IFutureSplitMergeService futureSplitMergeService;

    @Autowired
    private ISystemService systemService;

    @Autowired
    private IObjectLockMethod lockMethod;

    @Autowired
    private RetCodeConfigEx retCodeConfigEx;

    @Autowired
    private  IFutureEventMethod futureEventMethod;

    @Override
    public void sxFSMLotRemoveReq(Infos.ObjCommon objCommon, com.fa.cim.fsm.Params.FSMLotRemoveReqParams params) {

        //【step1】Check input-parameters
        ObjectIdentifier lotFamilyID = params.getLotFamilyID();
        String originalOperationNumber = params.getOriginalOperationNumber();
        ObjectIdentifier originalRouteID = params.getOriginalRouteID();
        String splitOperationNumber = params.getSplitOperationNumber();
        ObjectIdentifier splitRouteID = params.getSplitRouteID();
        Validations.check(ObjectIdentifier.isEmpty(lotFamilyID) || ObjectIdentifier.isEmpty(splitRouteID) || CimObjectUtils.isEmpty(splitOperationNumber) || ObjectIdentifier.isEmpty(originalRouteID) || CimObjectUtils.isEmpty(originalOperationNumber), retCodeConfig.getInvalidParameter());


        //【step2】lotFamily_allLots_GetDR
        List<ObjectIdentifier> lotFamilyAllLotsGetDROut = lotFamilyMethod.lotFamilyAllLotsGetDR(objCommon, lotFamilyID);


        int lotIDlen = CimArrayUtils.getSize(lotFamilyAllLotsGetDROut);
        for (int i = 0; i < lotIDlen; i++) {
            //【step3】Check lot interFabXferState
            Inputs.ObjLotInterFabTransferStateGetIn objLotInterFabXferStateGetIn = new Inputs.ObjLotInterFabTransferStateGetIn();
            objLotInterFabXferStateGetIn.setLotID(lotFamilyAllLotsGetDROut.get(i));
            String lotInterFabXferStateGetOut = lotMethod.lotInterFabXferStateGet(objCommon, objLotInterFabXferStateGetIn.getLotID());

            //【step4】"Transferring"
            Validations.check(CimStringUtils.equals(lotInterFabXferStateGetOut, BizConstant.SP_INTERFAB_XFERSTATE_TRANSFERRING),
                    retCodeConfig.getInterfabInvalidLotXferstateForReq(),lotFamilyAllLotsGetDROut.get(i),lotInterFabXferStateGetOut);
        }
        //【step5】Get FSM Info for History
        List<com.fa.cim.fsm.Infos.ExperimentalFutureLotInfo> experimentalFutureLotListGetDR = experimentalForFutureMethod.experimentalFutureLotListGetDR(objCommon, lotFamilyID.getValue(), splitRouteID.getValue(), splitOperationNumber, originalRouteID.getValue(), originalOperationNumber, false, true);

        if (CimArrayUtils.getSize(experimentalFutureLotListGetDR) > 1) {
            throw new ServiceException(retCodeConfig.getInvalidInputParam());
        } else if (CimArrayUtils.getSize(experimentalFutureLotListGetDR) < 1) {
            throw new ServiceException(retCodeConfig.getNotFoundExperimentalLotData());
        }
        com.fa.cim.fsm.Infos.ExperimentalFutureLotInfo experimentalFutureLotInfo = experimentalFutureLotListGetDR.get(0);

        //【step6】Delete the indications for the lot.
        String fsmJobID = experimentalForFutureMethod.experimentalFutureLotInfoDelete(objCommon, lotFamilyID, splitRouteID, splitOperationNumber, originalRouteID, originalOperationNumber);//已实现

        //【step7】Make History
        com.fa.cim.fsm.Infos.ExperimentalFutureLotRegistInfo experimentalFutureLotRegistInfo = new com.fa.cim.fsm.Infos.ExperimentalFutureLotRegistInfo();
        experimentalFutureLotRegistInfo.setUserID(objCommon.getUser().getUserID().getValue());
        experimentalFutureLotRegistInfo.setFsmJobID(fsmJobID);
        experimentalFutureLotRegistInfo.setAction(BizConstant.SP_EWR_ACTION_DELETE);
        experimentalFutureLotRegistInfo.setLotFamilyID(lotFamilyID);
        experimentalFutureLotRegistInfo.setSplitRouteID(splitRouteID);
        experimentalFutureLotRegistInfo.setSplitOperationNumber(splitOperationNumber);
        experimentalFutureLotRegistInfo.setOriginalRouteID(originalRouteID);
        experimentalFutureLotRegistInfo.setOriginalOperationNumber(originalOperationNumber);
        experimentalFutureLotRegistInfo.setActionEMail(experimentalFutureLotInfo.getActionEMail());
        experimentalFutureLotRegistInfo.setActionHold(experimentalFutureLotInfo.getActionHold());
        experimentalFutureLotRegistInfo.setTestMemo(experimentalFutureLotInfo.getTestMemo());

        int lenDetailInfoSeq = CimArrayUtils.getSize(experimentalFutureLotInfo.getStrExperimentalFutureLotDetailInfoSeq());
        int size = CimArrayUtils.getSize(experimentalFutureLotRegistInfo.getStrExperimentalFutureLotRegistSeq());
        size = lenDetailInfoSeq;
        List<com.fa.cim.fsm.Infos.ExperimentalFutureLotRegist> strExperimentalFutureLotRegistSeq = new ArrayList<>();
        List<com.fa.cim.fsm.Infos.ExperimentalFutureLotDetailInfo> strExperimentalFutureLotDetailInfoSeq = new ArrayList<>();
        for (int i = 0; i < lenDetailInfoSeq; i++) {
           com.fa.cim.fsm.Infos.ExperimentalFutureLotRegist r1 = new com.fa.cim.fsm.Infos.ExperimentalFutureLotRegist();
            com.fa.cim.fsm.Infos.ExperimentalFutureLotDetailInfo r2 = experimentalFutureLotInfo.getStrExperimentalFutureLotDetailInfoSeq().get(i);
            r1.setRouteID(r2.getRouteID());
            r1.setReturnOperationNumber(r2.getReturnOperationNumber());
            r1.setMergeOperationNumber(r2.getMergeOperationNumber());
            r1.setMemo(r2.getMemo());
            //List<ObjectIdentifier> identifiers = r2.getWaferIDs().stream().map(m -> m.getWaferID()).collect(Collectors.toList());
            r1.setWaferIDs(r2.getWaferIDs());
            strExperimentalFutureLotRegistSeq.add(r1);
        }
        experimentalFutureLotRegistInfo.setStrExperimentalFutureLotRegistSeq(strExperimentalFutureLotRegistSeq);
        String tx = TransactionIDEnum.EXPERIMENTAL_LOT_DELETE_REQ.getValue();
        String testMemo = experimentalFutureLotRegistInfo.getTestMemo();

        // task-3988 this function not has been definde ,for now just comment it out
        // eventMethod.experimentalLotRegistEventMake(objCommon, tx, testMemo, experimentalFutureLotRegistInfo);
        futureEventMethod.experimentalLotRegistEventMake(objCommon, tx, testMemo, experimentalFutureLotRegistInfo);
    }

    @Override
    public List<ObjectIdentifier> sxFSMLotActionReq(Infos.ObjCommon objCommon, ObjectIdentifier lotId, String claimMemo) {
        //init
        List<ObjectIdentifier> createLotIds = new ArrayList<>();
        String dummy = "";
        ObjectIdentifier dummyId = null;

        //【step1】 Lock Lot object
        // step1 - object_Lock
        lockMethod.objectLock(objCommon, CimLot.class, lotId);

        //Check Lot condition
        //【step2】experimental_lotState_Check
        try {
            experimentalForFutureMethod.experimentalFutureLotStateCheck(objCommon, lotId);
        } catch (ServiceException e) {
            //ok
            return createLotIds;
        }

        // Get PSM definition
        //【step3】experimental_lotInfo_Get
        com.fa.cim.fsm.Outputs.ObjExperimentalFutureLotInfoGetOut experimentalFutureLotInfoGetOut = null;
        try {
            experimentalFutureLotInfoGetOut = experimentalForFutureMethod.experimentalFutureLotInfoGet(objCommon, lotId);
        } catch (ServiceException e) {
            //ok
            return createLotIds;
        }
        if (CimBooleanUtils.isTrue(experimentalFutureLotInfoGetOut.getStrExperimentalFutureLotInfo().getExecFlag())) {
            log.info("The PSM definition already worked.");
            return createLotIds;
        }
        com.fa.cim.fsm.Infos.ExperimentalFutureLotInfo strExperimentalFutureLotInfo = experimentalFutureLotInfoGetOut.getStrExperimentalFutureLotInfo();

        // Lock LotFamily to keep data inconsistency
        // step4 - object_Lock
        lockMethod.objectLock(objCommon, CimLotFamily.class, strExperimentalFutureLotInfo.getLotFamilyID());

        //Check SorterJob existence
        Infos.EquipmentLoadPortAttribute dummyEquipmentLoadPortAttribute = new Infos.EquipmentLoadPortAttribute();
        List<ObjectIdentifier> dummyCastIds = new ArrayList<>();
        List<ObjectIdentifier> lotIds = new ArrayList<>();
        lotIds.add(lotId);
        Inputs.ObjWaferSorterJobCheckForOperation objWaferSorterJobCheckForOperation = new Inputs.ObjWaferSorterJobCheckForOperation();
        objWaferSorterJobCheckForOperation.setEquipmentLoadPortAttribute(dummyEquipmentLoadPortAttribute);
        objWaferSorterJobCheckForOperation.setCassetteIDList(dummyCastIds);
        objWaferSorterJobCheckForOperation.setLotIDList(lotIds);
        objWaferSorterJobCheckForOperation.setOperation(BizConstant.SP_OPERATION_FOR_LOT);
        //【step5】waferSorter_sorterJob_CheckForOperation
        waferMethod.waferSorterSorterJobCheckForOperation(objCommon, objWaferSorterJobCheckForOperation);
        long ppChainMode = 0;
        RetCode rc_pp = new RetCode(); //rc for PostProcess registration
        rc_pp.setReturnCode(retCodeConfig.getSucc());
        ppChainMode = StandardProperties.OM_PP_CHAIN_FLAG.getLongValue();
        log.info("OM_PP_CHAIN_FLAG:{}", ppChainMode);
        String strTriggerDKey = null;
        if (ppChainMode == 1) {
            log.info("ppChainMode=1, get SP_ThreadSpecificData_Key_TriggerDKey");
            strTriggerDKey = ThreadContextHolder.getThreadSpecificDataString(BizConstant.SP_THREADSPECIFICDATA_KEY_TRIGGERDKEY);
            log.info("strTriggerDKey:{}", strTriggerDKey);
            if (CimStringUtils.length(strTriggerDKey) == 0) {
                log.info("strTriggerDKey is blank, set ppChainMode = 0");
                ppChainMode = 0;
            }
        }
        if (ppChainMode == 1) {
            log.info("ppChainMode=1, strTriggerDKey= {}", strTriggerDKey);
            //【step6】 postProcessAdditionalInfoGetDR
            List<Infos.PostProcessAdditionalInfo> postProcessAdditionalInfoGetDROut = processMethod.postProcessAdditionalInfoGetDR(objCommon, strTriggerDKey, 0);
            log.info("strPostProcessAdditionalInfoSeq.length = {}", postProcessAdditionalInfoGetDROut.size());
            long nExecutionCnt = 0;
            for (int j = 0; j < CimArrayUtils.getSize(postProcessAdditionalInfoGetDROut); j++) {
                log.info("strPostProcessAdditionalInfoSeq[j].name:{}", postProcessAdditionalInfoGetDROut.get(j).getName());
                if (CimStringUtils.equals(BizConstant.SP_THREADSPECIFICDATA_KEY_CHAINEXECCNT, postProcessAdditionalInfoGetDROut.get(j).getName())) {
                    // get ExecutionCnt
                    log.info("found ExecutionCnt");
                    nExecutionCnt = Long.valueOf(postProcessAdditionalInfoGetDROut.get(j).getValue());
                    break;
                }
            }
            nExecutionCnt++;
            log.info("nExecutionCnt:{}", nExecutionCnt);
            long nMaxChainExecCnt = StandardProperties.OM_PP_MAX_CHAIN_FLAG.getIntValue();
            log.info("OM_PP_CHAIN_FLAG,{}", nMaxChainExecCnt);
            if (nExecutionCnt > nMaxChainExecCnt) {
                log.info("nExecutionCnt > nMaxChainExecCnt, skip chained pp registration");
                ppChainMode = 0;
            } else {
                //Store ExecutionCnt as thread specific data
                ThreadContextHolder.setThreadSpecificDataString(BizConstant.SP_THREADSPECIFICDATA_KEY_CHAINEXECCNT, String.valueOf(nExecutionCnt));
            }
        }
        log.info("OM_PP_CHAIN_FLAG,{}", ppChainMode);
        //Create actual Split/Branch data
        //【step7】 experimental_lotActualInfo_Create
        com.fa.cim.fsm.Outputs.ObjExperimentalFutureLotActualInfoCreateOut experimentalFutureLotActualInfoCreateOut = null;
        try {
            experimentalFutureLotActualInfoCreateOut = experimentalForFutureMethod.experimentalFutureLotActualInfoCreate(objCommon, lotId, strExperimentalFutureLotInfo);
        } catch (ServiceException e) {
            boolean checkFlag = Validations.isEquals(e.getCode(), retCodeConfigEx.getFsmExecutionFail());
            if (checkFlag) {
                List<Infos.LotHoldReq> strHoldListSeq = new ArrayList<>();
                Infos.LotHoldReq lotHoldReq = new Infos.LotHoldReq();
                strHoldListSeq.add(lotHoldReq);
                lotHoldReq.setHoldType(BizConstant.SP_HOLDTYPE_LOTHOLD);
                lotHoldReq.setHoldReasonCodeID(ObjectIdentifier
                        .buildWithValue(BizConstant.SP_REASON_FSMEXECUTIONFAILHOLD));
                lotHoldReq.setHoldUserID(ObjectIdentifier
                        .buildWithValue(BizConstant.SP_POSTPROC_PERSON));
                lotHoldReq
                        .setResponsibleOperationMark(BizConstant
                                .SP_RESPONSIBLEOPERATION_CURRENT);
                lotHoldReq.setClaimMemo("");
                lotService.sxHoldLotReq(objCommon, lotId, strHoldListSeq);
                throw new ServiceException(retCodeConfigEx.getFsmExecutionFail());
            }
            throw e;
        }
        int actualDataLen = CimArrayUtils.getSize(experimentalFutureLotActualInfoCreateOut.getStrExperimentalFutureLotDetailInfoSeq());
        //----- Actual Data -------//
        List<com.fa.cim.fsm.Infos.ExperimentalFutureLotDetailInfo> strActualData = new ArrayList<>();
        //----- History Data -------//
        com.fa.cim.fsm.Infos.ExperimentalFutureLotDetailResultInfo strHistoryData = new com.fa.cim.fsm.Infos.ExperimentalFutureLotDetailResultInfo();

        if (actualDataLen < 1) {
            log.info("The PSM definition to work is not found.");
            //ok
            return createLotIds;
        } else if (actualDataLen > 0) {
            //----- Actual Data -------//
            strActualData = experimentalFutureLotActualInfoCreateOut.getStrExperimentalFutureLotDetailInfoSeq();
            //----- History Data -------//
            strHistoryData = experimentalFutureLotActualInfoCreateOut.getStrExperimentalLotDetailResultInfo();
        }
        //Check carrier reserved flag
        //【step8】lot_cassette_Get
        ObjectIdentifier lotCassetteOut = null;
        try {
            lotCassetteOut = lotMethod.lotCassetteGet(objCommon, lotId);
            log.info("cassette_lot_Get() rc == RC_OK");
            //【step9】 - cassette_reservedState_Get
            Outputs.ObjCassetteReservedStateGetOut cassetteReservedStateGetOut = new Outputs.ObjCassetteReservedStateGetOut();
            cassetteReservedStateGetOut.setTransferReserved(false);
            cassetteReservedStateGetOut = cassetteMethod.cassetteReservedStateGet(objCommon, lotCassetteOut);
            if (CimBooleanUtils.isTrue(cassetteReservedStateGetOut.isTransferReserved())) {
                log.info("cassette_reservedState_Get() transferReserved == TRUE");
                throw new ServiceException(retCodeConfig.getAlreadyReservedCst());
            } else {
                log.info("cassette_reservedState_Get() transferReserved == FALSE");
            }
        } catch (ServiceException e) {
            if (Validations.isEquals(retCodeConfig.getNotFoundCst(), e.getCode())) {
                // Lot is not in Carrier
                log.info("cassette_lot_Get() rc == RC_NOT_FOUND_CST");
            } else {
                log.info("cassette_lot_Get() rc != RC_OK");
                throw e;
            }
        }

        //Created Lot
        for (int i = 0; i < actualDataLen; i++) {

            //Check interFabXferPlan existence
            //【step10】 - process_CheckInterFabXferPlanSkip
            //TODO: Not Implement process_CheckInterFabXferPlanSkip
            Inputs.ObjProcessCheckInterFabXferPlanSkipIn objProcessCheckInterFabXferPlanSkipIn = new Inputs.ObjProcessCheckInterFabXferPlanSkipIn();
            objProcessCheckInterFabXferPlanSkipIn.setLotID(lotId);
            objProcessCheckInterFabXferPlanSkipIn.setCurrentRouteID(strExperimentalFutureLotInfo.getOriginalRouteID());
            objProcessCheckInterFabXferPlanSkipIn.setCurrentOpeNo(strExperimentalFutureLotInfo.getOriginalOperationNumber());
            objProcessCheckInterFabXferPlanSkipIn.setJumpingRouteID(strExperimentalFutureLotInfo.getOriginalRouteID());
            objProcessCheckInterFabXferPlanSkipIn.setJumpingOpeNo(strActualData.get(i).getReturnOperationNumber());
            processMethod.processCheckInterFabXferPlanSkip(objCommon, objProcessCheckInterFabXferPlanSkipIn);//未完成，差表数据
            //Judge action to perform
            //【step11】- lot_waferMap_Get
            List<Infos.LotWaferMap> lotWaferMapGetOut = lotMethod.lotWaferMapGet(objCommon, lotId);

            //Check Route is Dynamic or Not
            if (CimBooleanUtils.isTrue(strActualData.get(i).getDynamicFlag())) {
                //【step12】- process_checkForDynamicRoute
                Outputs.ObjProcessCheckForDynamicRouteOut processCheckForDynamicRouteOut = processMethod.processCheckForDynamicRoute(objCommon, strActualData.get(i).getRouteID());
                if (CimBooleanUtils.isFalse(processCheckForDynamicRouteOut.getDynamicRouteFlag())) {
                    Validations.check(true, retCodeConfig.getNotDynamicRoute());
                }
            }
            int lotWaferLen = CimArrayUtils.getSize(lotWaferMapGetOut);
            ObjectIdentifier subRouteID = strActualData.get(i).getRouteID();
            String returnOperationNumber = strActualData.get(i).getReturnOperationNumber();
            ObjectIdentifier childLotID = null;
            Map<String, List<com.fa.cim.fsm.Infos.Wafer>> lotGroup = strActualData.get(i).getWaferIDs().stream().collect(Collectors.groupingBy(com.fa.cim.fsm.Infos.Wafer::getGroupNo));
            for (List<com.fa.cim.fsm.Infos.Wafer> groupWafers : lotGroup.values()) {
                int actualWaferLen = CimArrayUtils.getSize(groupWafers);
                if (lotWaferLen != actualWaferLen) {
                    log.info("Split is performed.{},{}", i, strActualData.get(i).getRouteID().getValue());
                    //Perform Split
                    //【step13】 - txSplitLotReq 调用接口
                    Params.SplitLotReqParams futrueSplitLotReqParams = new Params.SplitLotReqParams();
                    futrueSplitLotReqParams.setParentLotID(lotId);
                    List<ObjectIdentifier> waferList = groupWafers.stream().map(com.fa.cim.fsm.Infos.Wafer::getWaferID).collect(Collectors.toList());
                    futrueSplitLotReqParams.setChildWaferIDs(waferList);
                    if (CimStringUtils.isNotEmpty(strActualData.get(i).getMergeOperationNumber())){
                        futrueSplitLotReqParams.setFutureMergeFlag(true);
                    } else {
                        futrueSplitLotReqParams.setFutureMergeFlag(false);
                    }
                    futrueSplitLotReqParams.setCombineHold(experimentalFutureLotActualInfoCreateOut.getActionCombineHold());
                    futrueSplitLotReqParams.setMergedRouteID(experimentalFutureLotActualInfoCreateOut.getSplitRouteId());
                    futrueSplitLotReqParams.setMergedOperationNumber(strActualData.get(i).getMergeOperationNumber());

                    if (ObjectIdentifier.isEmpty(subRouteID) && CimStringUtils.isEmpty(returnOperationNumber)) {
                        futrueSplitLotReqParams.setBranchingRouteSpecifyFlag(false);
                    } else {
                        futrueSplitLotReqParams.setBranchingRouteSpecifyFlag(true);
                    }
                    futrueSplitLotReqParams.setSubRouteID(strActualData.get(i).getRouteID());
                    futrueSplitLotReqParams.setReturnOperationNumber(strActualData.get(i).getReturnOperationNumber());

                    futrueSplitLotReqParams.setClaimMemo(strActualData.get(i).getMemo());
                    childLotID = lotService.sxSplitLotReq(objCommon, futrueSplitLotReqParams).getChildLotID();

                    /*-----------------------------------------------------------------------------------------------------------------------------------------------------*/
                    /*   Check Future Action Procedure                                                                                                                     */
                    /*   ** This objmethod gets schedule change information by dirty read.                                                                                 */
                    /*      So if the schedule info is checked by child Lot, the defined schecule info cannot be found in Database because the record is not uncommitted.  */
                    /*      At here, check parent Lot schedule info. If exist, return error.                                                                               */
                    /*-----------------------------------------------------------------------------------------------------------------------------------------------------*/
                    log.info("schdlChangeReservation_CheckForFutureOperation");
                    log.info("strExperimental_lotActualInfo_Create_out.splitRouteID :{}", experimentalFutureLotActualInfoCreateOut.getSplitRouteId().getValue());
                    //【step14】 - schdlChangeReservation_CheckForFutureOperation
                    if (CimStringUtils.isEmpty(strActualData.get(i).getReturnOperationNumber())){
                        strActualData.get(i).setReturnOperationNumber(strActualData.get(i).getMergeOperationNumber());
                    }
                    scheduleChangeReservationMethod.schdlChangeReservationCheckForFutureOperation(objCommon, lotId, experimentalFutureLotActualInfoCreateOut.getSplitRouteId().getValue(), strActualData.get(i).getReturnOperationNumber());
                    //Register post process for child lot
                    if (1 == ppChainMode) {
                        log.info("ppChainMode=1, call txPostTaskRegisterReq for child lot");
                        //【step15】 - txPostTaskRegisterReq__100 调用接口
                        Params.PostTaskRegisterReqParams strPostProcessRegistrationParm = new Params.PostTaskRegisterReqParams();
                        strPostProcessRegistrationParm.setTransactionID(BizConstant.TX_ID_P_BRANCH);
                        strPostProcessRegistrationParm.setSequenceNumber(-1);
                        strPostProcessRegistrationParm.setClaimMemo("");
                        strPostProcessRegistrationParm.setPatternID(null);
                        strPostProcessRegistrationParm.setKey(null);

                        Infos.PostProcessRegistrationParam postProcessRegistrationParam = new Infos.PostProcessRegistrationParam();
                        List<ObjectIdentifier> lotIdList = new ArrayList<>();
                        lotIdList.add(childLotID);
                        postProcessRegistrationParam.setLotIDs(lotIdList);
                        strPostProcessRegistrationParm.setPostProcessRegistrationParm(postProcessRegistrationParam);


                        // step15 - txPostTaskRegisterReq__100
                        Results.PostTaskRegisterReqResult postTaskRegisterReqResultOut = postService.sxPostTaskRegisterReq(objCommon, strPostProcessRegistrationParm);

                        log.info("New dKey", postTaskRegisterReqResultOut.getDKey());
                    } else {
                        log.info("ppChainMode==0, no need to recreate post process");
                    }
                    //----- Created Lot -----//
                    createLotIds.add(childLotID);
                    //----- History Data : Action -------//
                    strHistoryData.setAction(BizConstant.SP_EWR_ACTION_SPLIT);
                } else {
                    log.info("Branch is performed.{},{}", i, subRouteID.getValue());
                    //If subRouteID or returnNumer is not empty, it means that this is the main process in batches and does not need to enter the sub-process
                    if (ObjectIdentifier.isEmpty(subRouteID) || CimStringUtils.isEmpty(returnOperationNumber)) {
                        continue;
                    }

                    //Perform Branch
                    //【step16】 - txBranchReq 调用接口
                    Infos.BranchReq branchReq = new Infos.BranchReq();
                    branchReq.setLotID(lotId);
                    branchReq.setCurrentRouteID(dummyId);//null
                    branchReq.setCurrentOperationNumber(dummy);//""
                    branchReq.setSubRouteID(strActualData.get(i).getRouteID());
                    branchReq.setReturnOperationNumber(strActualData.get(i).getReturnOperationNumber());
                    branchReq.setEventTxId(objCommon.getUser().getFunctionID());
                    branchReq.setBDynamicRoute(strActualData.get(i).getDynamicFlag());
                    lotService.sxBranchReq(objCommon, branchReq, "");
                    //----- History Data : Action -------//
                    strHistoryData.setAction(BizConstant.SP_EWR_ACTION_BRANCH);

                    //Recreate post process for parent lot
//                Validations.check(1 == ppChainMode, retCodeConfig.getPostrpocDkeyRecreate());
                    if (1 == ppChainMode) {
                        rc_pp.setReturnCode(retCodeConfig.getPostrpocDkeyRecreate());
                    }
                }
                //Prepare to update PSM definition etc.
                for (int j = 0; j < actualWaferLen; j++) {
                    int m = 0;
                    int hisDataLen = CimArrayUtils.getSize(strHistoryData.getStrExperimentalFutureLotDetailSeq());
                    for (m = 0; m < hisDataLen; m++) {
                        int n = 0;
                        int hisWafLen = CimArrayUtils.getSize(strHistoryData.getStrExperimentalFutureLotDetailSeq().get(m).getStrExperimentalLotWaferSeq());
                        for (n = 0; n < hisWafLen; n++) {
                            if (CimStringUtils.equals(groupWafers.get(j).getWaferID().getValue(), strHistoryData.getStrExperimentalFutureLotDetailSeq().get(m).getStrExperimentalLotWaferSeq().get(n).getWaferId().getWaferID().getValue())) {
                                //===== The defined data is updated if it works =======//
                                // The length and index are same between "strHistoryData" and "strExperimentalLotInfo".
                                strExperimentalFutureLotInfo.getStrExperimentalFutureLotDetailInfoSeq().get(m).setExecFlag(true);
                                strExperimentalFutureLotInfo.getStrExperimentalFutureLotDetailInfoSeq().get(m).setActionTimeStamp(CimDateUtils.convertToSpecString(objCommon.getTimeStamp().getReportTimeStamp()));
                                //----- History Data : Parent Lot -------//
                                strHistoryData.getStrExperimentalFutureLotDetailSeq().get(m).setParentLotID(lotId);
                                if (CimStringUtils.equals(BizConstant.SP_EWR_ACTION_SPLIT, strHistoryData.getAction())) {
                                    //----- History Data : Child Lot -------//
                                    strHistoryData.getStrExperimentalFutureLotDetailSeq().get(m).setChildLotID(childLotID);
                                }
                                break;
                            }
                        }
                        if (n != hisWafLen) {
                            break;
                        }
                    }
                    if (m != hisDataLen) {
                        break;
                    }
                }
            }
            //Send E-Mail
            // step17 - txAlertMessageRpt
            if (CimBooleanUtils.isTrue(strExperimentalFutureLotInfo.getActionEMail())) {
                log.info("The PSM's mail action flag is on, so mail is sent.");
                Params.AlertMessageRptParams alertMessageRptParams = new Params.AlertMessageRptParams();
                alertMessageRptParams.setSubSystemID(BizConstant.SP_SUBSYSTEMID_MM);
                alertMessageRptParams.setSystemMessageCode(BizConstant.SP_SYSTEMMSGCODE_PSMEXEC);
                alertMessageRptParams.setNotifyFlag(true);
                alertMessageRptParams.setLotID(lotId);
                alertMessageRptParams.setRouteID(experimentalFutureLotActualInfoCreateOut.getSplitRouteId());
                alertMessageRptParams.setSystemMessageTimeStamp(CimDateUtils.convertToSpecString(objCommon.getTimeStamp().getReportTimeStamp()));
                alertMessageRptParams.setClaimMemo(claimMemo);
                systemService.sxAlertMessageRpt(objCommon, alertMessageRptParams);
            }
        }
        //Perfrom Lot Hold
        if (CimBooleanUtils.isTrue(experimentalFutureLotActualInfoCreateOut.isActionHold())) {
            List<Infos.LotHoldReq> strLotHoldReqList = new ArrayList<>();
            Infos.LotHoldReq lotHoldReq = new Infos.LotHoldReq();
            strLotHoldReqList.add(lotHoldReq);
            lotHoldReq.setHoldType(BizConstant.SP_HOLDTYPE_LOTHOLD);
            lotHoldReq.setHoldUserID(objCommon.getUser().getUserID());
            lotHoldReq.setResponsibleOperationMark(BizConstant.SP_RESPONSIBLEOPERATION_CURRENT);
            lotHoldReq.setRouteID(dummyId);//null
            lotHoldReq.setOperationNumber(dummy);//""
            lotHoldReq.setClaimMemo(claimMemo);
            //---- PSM Hold -------//
            log.info("The PSM's hold action flag is on, so the lot and created lot are held.");
            lotHoldReq.setHoldReasonCodeID(ObjectIdentifier.buildWithValue(BizConstant.SP_REASON_PSM_HOLD));
            if (CimBooleanUtils.isTrue(experimentalFutureLotActualInfoCreateOut.getActionSeparateHold())) {
                lotHoldReq.setHoldReasonCodeID(ObjectIdentifier.buildWithValue(BizConstant.SP_REASON_SPR_HOLD));
            }
            //【step18】 - txHoldLotReq  调用接口
            lotService.sxHoldLotReq(objCommon, lotId, strLotHoldReqList);
            for (int childLotCnt = 0; childLotCnt < createLotIds.size(); childLotCnt++) {
                //【step19】 -  txHoldLotReq 调用接口
                lotService.sxHoldLotReq(objCommon, createLotIds.get(childLotCnt), strLotHoldReqList);
            }
        }
        //Make History
        String txId = TransactionIDEnum.EXPERIMENTAL_LOT_EXEC_REQ.getValue();
        //TODO-FSM EVENT
//        eventMethod.experimentalLotExecEventMake(objCommon, txId, "", strHistoryData);
        futureEventMethod.experimentalLotExecEventMake(objCommon, txId, "", strHistoryData);
        //Update execFlag of PSM definition
        Boolean execFlag = true;
        String execTime = CimDateUtils.convertToSpecString(objCommon.getTimeStamp().getReportTimeStamp());
        int defDataLen = CimArrayUtils.getSize(strExperimentalFutureLotInfo.getStrExperimentalFutureLotDetailInfoSeq());
        for (int defDataCnt = 0; defDataCnt < defDataLen; defDataCnt++) {
            if (CimBooleanUtils.isTrue(strExperimentalFutureLotInfo.getStrExperimentalFutureLotDetailInfoSeq().get(defDataCnt).getExecFlag())) {
                continue;
            }
            log.info("The PSM definition has some sub routes which don't work yet.");
            execFlag = false;
            execTime = strExperimentalFutureLotInfo.getActionTimeStamp();
            break;
        }
        //【step21】 - txPSMLotInfoSetReq 调用接口F
        com.fa.cim.fsm.Params.FSMLotInfoSetReqParams fsmLotInfoSetReqParams = new com.fa.cim.fsm.Params.FSMLotInfoSetReqParams();
        fsmLotInfoSetReqParams.setLotFamilyID(strExperimentalFutureLotInfo.getLotFamilyID());
        fsmLotInfoSetReqParams.setSplitRouteID(strExperimentalFutureLotInfo.getSplitRouteID());
        fsmLotInfoSetReqParams.setSplitOperationNumber(strExperimentalFutureLotInfo.getSplitOperationNumber());
        fsmLotInfoSetReqParams.setOriginalRouteID(strExperimentalFutureLotInfo.getOriginalRouteID());
        fsmLotInfoSetReqParams.setOriginalOperationNumber(strExperimentalFutureLotInfo.getOriginalOperationNumber());
        fsmLotInfoSetReqParams.setActionEMail(strExperimentalFutureLotInfo.getActionEMail());
        fsmLotInfoSetReqParams.setActionHold(strExperimentalFutureLotInfo.getActionHold());
        fsmLotInfoSetReqParams.setActionSeparateHold(strExperimentalFutureLotInfo.getActionSeparateHold());
        fsmLotInfoSetReqParams.setActionCombineHold(strExperimentalFutureLotInfo.getActionCombineHold());
        fsmLotInfoSetReqParams.setTestMemo(strExperimentalFutureLotInfo.getTestMemo());
        fsmLotInfoSetReqParams.setExecFlag(execFlag);
        fsmLotInfoSetReqParams.setActionTimeStamp(execTime);
        fsmLotInfoSetReqParams.setModifyUserID(objCommon.getUser().getUserID());
        fsmLotInfoSetReqParams.setStrExperimentalFutureLotDetailInfoSeq(strExperimentalFutureLotInfo.getStrExperimentalFutureLotDetailInfoSeq());
        fsmLotInfoSetReqParams.setClaimMemo(claimMemo);
        futureSplitMergeService.sxFSMLotInfoSetReq(objCommon, fsmLotInfoSetReqParams);

        // Update cassette multi lot type
        if (CimStringUtils.length(lotCassetteOut.getValue()) > 0) {
            log.info("Update cassette multi lot type");
            //【step22】 - cassette_multiLotType_Update
            cassetteMethod.cassetteMultiLotTypeUpdate(objCommon, lotCassetteOut);
        }

        //check rc_pp if is error
        if (Validations.isNotSuccess(rc_pp)) {
            Validations.check(true, rc_pp.getReturnCode(), createLotIds);
        }

        //return ok
        return createLotIds;
    }

    @Override
    public void sxFSMLotInfoSetReq(Infos.ObjCommon objCommon, com.fa.cim.fsm.Params.FSMLotInfoSetReqParams params) {

        //【step0】Check input-parameters
        log.info("PSMLotInfoSetReq    !!! (Trace ON)");
        if (CimBooleanUtils.isTrue(params.getActionEMail())) {
            log.debug("actionEMail is TRUE");
        } else {
            log.debug("actionEMail is FALSE");
        }

        if (CimBooleanUtils.isTrue(params.getActionHold())) {
            log.debug("actionHold is TRUE");
        } else {
            log.debug("actionHold is FALSE");
        }

        if (CimBooleanUtils.isTrue(params.getExecFlag())) {
            log.debug("execFlag is TRUE");
        } else {
            log.debug("execFlag is FALSE");
        }
        ObjectIdentifier modifierID = new ObjectIdentifier();
        if (!CimObjectUtils.isEmpty(params.getModifyUserID().getValue())) {
            modifierID = params.getModifyUserID();
        } else {
            modifierID = objCommon.getUser().getUserID();
        }
        log.info("modifierID:{}",modifierID);
        // len is the list length of wafer
        int len = CimArrayUtils.getSize(params.getStrExperimentalFutureLotDetailInfoSeq());
        Validations.check(len == 0, retCodeConfig.getInvalidDataContents());

        //task-3988 to validation wafer`s length,in time it`s dosen`t work
        for (int i = 0; i < len; i++) {
            int len2 = CimArrayUtils.getSize(params.getStrExperimentalFutureLotDetailInfoSeq().get(i).getWaferIDs());
            Validations.check(len2 < 1, retCodeConfig.getInvalidInputParam());

            for (int j = 0; j < len2; j++) {
                log.info("waferIDs,{}",params.getStrExperimentalFutureLotDetailInfoSeq().get(i).getWaferIDs());
            }
        }
        log.info("PSMLotInfoSetReq E n d!!! (Trace End)");

        //【step1】lotFamily_allLots_GetDR
        if (CimStringUtils.equals(objCommon.getTransactionID(), TransactionIDEnum.FSM_LOT_INFO_SET_REQ.getValue())) {
            log.debug("call lotFamilyAllLotsGetDR()");
            List<ObjectIdentifier> strLotFamilyAllLotsGetDROut = lotFamilyMethod.lotFamilyAllLotsGetDR(objCommon, params.getLotFamilyID());

            int lotIDlen = CimArrayUtils.getSize(strLotFamilyAllLotsGetDROut);
            for (int i = 0; i < lotIDlen; i++) {
                //【step2】Check lot interFabXferState
                log.debug("call lotInterFabXferStateGet");
                Inputs.ObjLotInterFabTransferStateGetIn objLotInterFabTransferStateGetIn = new Inputs.ObjLotInterFabTransferStateGetIn();
                objLotInterFabTransferStateGetIn.setLotID(strLotFamilyAllLotsGetDROut.get(i));
                String lotInterFabXferStateResultRetCode = lotMethod.lotInterFabXferStateGet(objCommon, objLotInterFabTransferStateGetIn.getLotID());//Ho以实现待测
                log.info("interFabXferState {}",lotInterFabXferStateResultRetCode);

                //【step2-1】"Transferring"
                if (CimStringUtils.equals(BizConstant.SP_INTERFAB_XFERSTATE_TRANSFERRING, lotInterFabXferStateResultRetCode)) {
                    log.info("##### interFabXferState == Transferring");
                    throw new ServiceException(new OmCode(retCodeConfig.getInterfabInvalidLotXferstateForReq(),
                            strLotFamilyAllLotsGetDROut.get(i).getValue(),lotInterFabXferStateResultRetCode));
                }

                //-----------------------------------------------------------
                // Get next operation of originalRouteID/originalOperationNumber.
                // Because, XferPlan can be defined on PSM definition operation.
                // If orgOpeNo is passed to process_CheckInterFabXferPlanSkip(), it is treated as an error.
                //-----------------------------------------------------------
                //【step3】process_OperationNumberListForLot
                log.debug("call processOperationNumberListForLot");
                log.info("originalRouteID:{}",params.getOriginalRouteID().getValue());
                log.info("originalOperationNumber:{}",params.getOriginalOperationNumber());
                Inputs.ObjProcessOperationProcessRefListForLotIn objProcessOperationNumberListForLotIn = new Inputs.ObjProcessOperationProcessRefListForLotIn();
                objProcessOperationNumberListForLotIn.setSearchDirection(true);
                objProcessOperationNumberListForLotIn.setPosSearchFlag(false);
                objProcessOperationNumberListForLotIn.setSearchCount(9999);
                objProcessOperationNumberListForLotIn.setSearchRouteID(params.getOriginalRouteID());
                objProcessOperationNumberListForLotIn.setSearchOperationNumber("");
                objProcessOperationNumberListForLotIn.setCurrentFlag(false);
                objProcessOperationNumberListForLotIn.setLotID(strLotFamilyAllLotsGetDROut.get(i));

                List<Infos.OperationNumberListAttributes> processOperationNumberListForLotResult = processMethod.processOperationNumberListForLot(objCommon, objProcessOperationNumberListForLotIn);
                int lenOpe = CimArrayUtils.getSize(processOperationNumberListForLotResult);
                int nOrgOpeNoIdx = -1;
                for (int nOpe = 0; nOpe < lenOpe; nOpe++) {
                    if (CimStringUtils.equals(params.getOriginalOperationNumber(), processOperationNumberListForLotResult.get(i).getOperationNumber())) {
                        nOrgOpeNoIdx = nOpe;
                        break;
                    }
                }
                if (0 > nOrgOpeNoIdx) {
                    log.debug("next operation was not found");
                    continue;
                }
                if (nOrgOpeNoIdx >= lenOpe - 1) {
                    log.debug("OrgOpeNo is last operation");
                    continue;
                }
                nOrgOpeNoIdx++;
                String nextOpeNo = processOperationNumberListForLotResult.get(nOrgOpeNoIdx).getOperationNumber();
                for (int j = 0; j < len; j++) {
                    if (CimStringUtils.isNotEmpty(params.getStrExperimentalFutureLotDetailInfoSeq().get(j).getReturnOperationNumber())) {
                        //【step4】process_CheckInterFabXferPlanSkip
                        //【joseph】we don't do,so we skip this source code. return OK
                        log.debug("call process_CheckInterFabXferPlanSkip()");
                        Inputs.ObjProcessCheckInterFabXferPlanSkipIn objProcessCheckInterFabXferPlanSkipIn = new Inputs.ObjProcessCheckInterFabXferPlanSkipIn();
                        objProcessCheckInterFabXferPlanSkipIn.setLotID(strLotFamilyAllLotsGetDROut.get(i));
                        objProcessCheckInterFabXferPlanSkipIn.setCurrentRouteID(params.getOriginalRouteID());
                        objProcessCheckInterFabXferPlanSkipIn.setCurrentOpeNo(nextOpeNo);
                        objProcessCheckInterFabXferPlanSkipIn.setJumpingRouteID(params.getOriginalRouteID());
                        objProcessCheckInterFabXferPlanSkipIn.setJumpingOpeNo(params.getStrExperimentalFutureLotDetailInfoSeq().get(j).getReturnOperationNumber());
                        RetCode<Object> processCheckInterFabXferPlanSkipRetCode = null;
                        //todo processCheckInterFabXferPlanSkip
                        processMethod.processCheckInterFabXferPlanSkip(objCommon, objProcessCheckInterFabXferPlanSkipIn);
                    }
                }
            }
        }
        //Get ELC informations.
        int searchCnt = 0;
        int foundCnt = 0;
        int actLen = 0;
        int defLen = 0;
        String latestActionTimeStamp = "";
        Boolean workedFlag = false;
        com.fa.cim.fsm.Infos.ExperimentalFutureLotInfo strDefData = new com.fa.cim.fsm.Infos.ExperimentalFutureLotInfo();
        //【step5】 experimental_lotList_GetDR
        List<com.fa.cim.fsm.Infos.ExperimentalFutureLotInfo> experimentalFutureLotListGetDROut = experimentalForFutureMethod.experimentalFutureLotListGetDR(objCommon, params.getLotFamilyID().getValue(),
                params.getSplitRouteID().getValue(),
                params.getSplitOperationNumber(),
                params.getOriginalRouteID().getValue(),
                params.getOriginalOperationNumber(),
                false,
                true);
        if (CimArrayUtils.getSize(experimentalFutureLotListGetDROut) > 1) {
            log.info("The key items of target FSM are invalid.");
            throw new ServiceException(retCodeConfig.getInvalidInputParam());
        } else if (CimArrayUtils.getSize(experimentalFutureLotListGetDROut) > 0) {
            strDefData = experimentalFutureLotListGetDROut.get(0);
            // bug-1542
            //Validations.check(BooleanUtils.isTrue(strDefData.getExecFlag()), retCodeConfig.getExplotAlreadyDone());


            //Check execFlag of each sub route
            actLen = CimArrayUtils.getSize(params.getStrExperimentalFutureLotDetailInfoSeq());
            defLen = CimArrayUtils.getSize(strDefData.getStrExperimentalFutureLotDetailInfoSeq());
            for (int defCnt = 0; defCnt < defLen; defCnt++) {
                if (CimBooleanUtils.isTrue(strDefData.getStrExperimentalFutureLotDetailInfoSeq().get(defCnt).getExecFlag())) {
                    searchCnt++;
                }
                //Loop for actual PSM's Branch Route
                for (int actCnt = 0; actCnt < actLen; actCnt++) {
                    // task-3988 this is a main flow if SubRouteID and ReturnOperationNumber is blank
                    if (ObjectIdentifier.isNotEmpty(params.getStrExperimentalFutureLotDetailInfoSeq().get(actCnt).getRouteID()) && CimStringUtils.isNotEmpty(params.getStrExperimentalFutureLotDetailInfoSeq().get(actCnt).getReturnOperationNumber())) {
                        if (!CimStringUtils.equals(params.getStrExperimentalFutureLotDetailInfoSeq().get(actCnt).getRouteID().getValue(),
                                strDefData.getStrExperimentalFutureLotDetailInfoSeq().get(defCnt).getRouteID().getValue())) {
                            continue;
                        }
                    }
                    int accordWafCnt = 0;
                    int actWafLen = CimArrayUtils.getSize(params.getStrExperimentalFutureLotDetailInfoSeq().get(actCnt).getWaferIDs());
                    int defWafLen = CimArrayUtils.getSize(strDefData.getStrExperimentalFutureLotDetailInfoSeq().get(defCnt).getWaferIDs());
                    // Loop for defined PSM's Wafer
                    for (int defWafCnt = 0; defWafCnt < defWafLen; defWafCnt++) {
                        //Loop for actual PSM's Wafer
                        for (int actWafCnt = 0; actWafCnt < actWafLen; actWafCnt++) {
                            if (CimStringUtils.equals(params.getStrExperimentalFutureLotDetailInfoSeq().get(actCnt).getWaferIDs().get(actWafCnt).getWaferID().getValue(),
                                    strDefData.getStrExperimentalFutureLotDetailInfoSeq().get(defCnt).getWaferIDs().get(defWafCnt).getWaferID().getValue())) {
                                accordWafCnt++;
                                break;
                            }
                        }
                    }
                    if (CimStringUtils.equals(params.getStrExperimentalFutureLotDetailInfoSeq().get(actCnt).getReturnOperationNumber(),
                            strDefData.getStrExperimentalFutureLotDetailInfoSeq().get(defCnt).getReturnOperationNumber())
                            && CimStringUtils.equals(params.getStrExperimentalFutureLotDetailInfoSeq().get(actCnt).getMergeOperationNumber(),
                            strDefData.getStrExperimentalFutureLotDetailInfoSeq().get(defCnt).getReturnOperationNumber())
                            && CimStringUtils.equals(params.getStrExperimentalFutureLotDetailInfoSeq().get(actCnt).getMemo(),
                            strDefData.getStrExperimentalFutureLotDetailInfoSeq().get(defCnt).getMemo())
                            && (params.getStrExperimentalFutureLotDetailInfoSeq().get(actCnt).getDynamicFlag() ==
                            strDefData.getStrExperimentalFutureLotDetailInfoSeq().get(defCnt).getDynamicFlag())) {
                        if (CimBooleanUtils.isTrue(strDefData.getStrExperimentalFutureLotDetailInfoSeq().get(defCnt).getExecFlag())) {
                            log.info("The data of details already done is found.", strDefData.getStrExperimentalFutureLotDetailInfoSeq().get(defCnt).getRouteID().getValue());
                            foundCnt++;
                            Timestamp actionTimeStamp = CimDateUtils.convertToOrInitialTime(params.getStrExperimentalFutureLotDetailInfoSeq().get(actCnt).getActionTimeStamp());
                            if (foundCnt == 1) {
                                latestActionTimeStamp = CimDateUtils.convertToSpecString(actionTimeStamp);
                            } else {
                                if (actionTimeStamp.after(CimDateUtils.convertToOrInitialTime(latestActionTimeStamp))) {
                                    latestActionTimeStamp = CimDateUtils.convertToSpecString(actionTimeStamp);//?
                                }
                            }
                        } else if (CimBooleanUtils.isTrue(params.getStrExperimentalFutureLotDetailInfoSeq().get(actCnt).getExecFlag())) {
                            log.debug("Target PSM was worked about Branch Route, so execFlag will be updated.");
                            workedFlag = true;
                            strDefData.getStrExperimentalFutureLotDetailInfoSeq().get(defCnt).setExecFlag(params.getStrExperimentalFutureLotDetailInfoSeq().get(actCnt).getExecFlag());
                            strDefData.getStrExperimentalFutureLotDetailInfoSeq().get(defCnt).setActionTimeStamp(params.getStrExperimentalFutureLotDetailInfoSeq().get(actCnt).getActionTimeStamp());
                        }
                        break;
                    }
                }
            }
            // bug-1542
            // Validations.check(searchCnt != foundCnt, retCodeConfig.getExplotAlreadyDone());

            log.debug("Target PSM is updated.");
        } else {
            log.debug("Target PSM is added.");
        }
        ObjectIdentifier lotFamilyIDTemp;
        ObjectIdentifier splitRouteIDTemp;
        String splitOperationNumberTemp;
        ObjectIdentifier originalRouteIDTemp;
        String originalOperationNumberTemp;
        Boolean actionEMailTemp;
        Boolean actionHoldTemp;
        String testMemoTemp;
        Boolean execFlagTemp;
        String actionTimeStampTemp;
        String modifyTimeStampTemp;
        String modifyUserIDTemp;
        List<com.fa.cim.fsm.Infos.ExperimentalFutureLotDetailInfo> strExperimentalFutureLotDetailInfoSeqTemp;

        if (CimBooleanUtils.isFalse(workedFlag)) {
            //Check the requested information.
            //【step6】experimental_lotInfo_Check
            experimentalForFutureMethod.experimentalFutureLotInfoCheck(objCommon, params.getLotFamilyID(), params.getSplitRouteID(), params.getSplitOperationNumber(), params.getOriginalRouteID(), params.getOriginalOperationNumber(), params.getStrExperimentalFutureLotDetailInfoSeq());//已实现

            //The check on flow batch sections
            //【step7】process_operationListForRoute__160
            Inputs.ProcessOperationListForRoute processOperationListForRoute = new Inputs.ProcessOperationListForRoute();
            processOperationListForRoute.setRouteID(params.getSplitRouteID());
            processOperationListForRoute.setOperationID(ObjectIdentifier.buildWithValue(""));
            processOperationListForRoute.setOperationNumber(params.getSplitOperationNumber());
            processOperationListForRoute.setPdType("");
            processOperationListForRoute.setSearchCount(1);
            List<Infos.OperationNameAttributes> processOperationListForRouteOut = processMethod.processOperationListForRoute(objCommon, processOperationListForRoute);
            Validations.check(CimArrayUtils.getSize(processOperationListForRouteOut) < 1,
                    retCodeConfig.getNotFoundRouteOpe(),params.getSplitRouteID(),params.getSplitOperationNumber());

            //The relation with flow batch sections is checked
            //【step8】process_flowBatchDefinition_GetDR
            String modulePOS = processOperationListForRouteOut.get(0).getProcessRef().getModulePOS();
            Outputs.ObjProcessFlowBatchDefinitionGetDROut processFlowBatchDefinitionGetDROut = processMethod.processFlowBatchDefinitionGetDR(objCommon, modulePOS);
            if (!CimObjectUtils.isEmpty(processFlowBatchDefinitionGetDROut.getFlowBatchControl().getName())
                    && CimBooleanUtils.isFalse(processFlowBatchDefinitionGetDROut.getFlowBatchSection().getEntryOperationFlag())) {
                log.info("The split operation is included by a flow batch section.");
                Validations.check(true, retCodeConfig.getProcessInBatchSection(),params.getSplitRouteID(),params.getSplitOperationNumber());
            }
            //The relation with bonding flow section is checked
            //【step9】process_bondingFlowDefinition_GetDR
            Outputs.ObjProcessBondingFlowDefinitionGetDROut processBondingFlowDefinitionGetDROut = processMethod.processBondingFlowDefinitionGetDR(objCommon, modulePOS);
            if (!CimObjectUtils.isEmpty(processBondingFlowDefinitionGetDROut.getFlowSectionControl().getFlowSectionID())
                    && CimBooleanUtils.isFalse(processBondingFlowDefinitionGetDROut.getFlowSectionControl().getFlowSectionEntryFlag())) {
                log.info("The split operation is included by a bonding flow section.");
                Validations.check(true, retCodeConfig.getProcessInBondingFlowSection(),params.getSplitRouteID(),params.getSplitOperationNumber());
            }
            //The merge operations
            int mergeLen = CimArrayUtils.getSize(params.getStrExperimentalFutureLotDetailInfoSeq());
            for (int mergeCnt = 0; mergeCnt < mergeLen; mergeCnt++) {
                //【step10】 process_operationListForRoute__160
                Inputs.ProcessOperationListForRoute processOperationListForRouteIn = new Inputs.ProcessOperationListForRoute();
                processOperationListForRouteIn.setRouteID(params.getSplitRouteID());
                processOperationListForRouteIn.setOperationID(ObjectIdentifier.buildWithValue(""));
                processOperationListForRouteIn.setOperationNumber(params.getStrExperimentalFutureLotDetailInfoSeq().get(mergeCnt).getMergeOperationNumber());
                processOperationListForRouteIn.setPdType("");
                processOperationListForRouteIn.setSearchCount(1);
                List<Infos.OperationNameAttributes> processOperationListForRouteInOut = processMethod.processOperationListForRoute(objCommon, processOperationListForRouteIn);
                if (CimArrayUtils.getSize(processOperationListForRouteInOut) < 1) {
                    log.info("The merge operation is not found.", params.getStrExperimentalFutureLotDetailInfoSeq().get(mergeCnt).getMergeOperationNumber());
                    Validations.check(true, retCodeConfig.getNotFoundRouteOpe(),params.getSplitRouteID(),params.getStrExperimentalFutureLotDetailInfoSeq().get(mergeCnt).getMergeOperationNumber());
                }
                //The relation with flow batch sections is checked
                //【step11】process_flowBatchDefinition_GetDR
                Outputs.ObjProcessFlowBatchDefinitionGetDROut objProcessFlowBatchDefinitionGetDROut = processMethod.processFlowBatchDefinitionGetDR(objCommon, processOperationListForRouteInOut.get(0).getProcessRef().getModulePOS());
                if (!CimObjectUtils.isEmpty(objProcessFlowBatchDefinitionGetDROut.getFlowBatchControl().getName())
                        && CimBooleanUtils.isFalse(objProcessFlowBatchDefinitionGetDROut.getFlowBatchSection().getEntryOperationFlag())) {
                    log.info("The merge operation is included by a flow batch section.", params.getStrExperimentalFutureLotDetailInfoSeq().get(mergeCnt).getMergeOperationNumber());
                    Validations.check(true, retCodeConfig.getProcessInBatchSection(),params.getSplitRouteID(),params.getStrExperimentalFutureLotDetailInfoSeq().get(mergeCnt).getMergeOperationNumber());
                }
                //The relation with bonding flow section is checked
                //【step12】process_bondingFlowDefinition_GetDR
                Outputs.ObjProcessBondingFlowDefinitionGetDROut objProcessBondingFlowDefinitionGetDROut = processMethod.processBondingFlowDefinitionGetDR(objCommon, processOperationListForRouteInOut.get(0).getProcessRef().getModulePOS());
                if (!CimObjectUtils.isEmpty(objProcessBondingFlowDefinitionGetDROut.getFlowSectionControl().getFlowSectionID())
                        && CimBooleanUtils.isFalse(objProcessBondingFlowDefinitionGetDROut.getFlowSectionControl().getFlowSectionEntryFlag())) {
                    log.info("The merge operation is included by a bonding flow section.", params.getStrExperimentalFutureLotDetailInfoSeq().get(mergeCnt).getMergeOperationNumber());
                    Validations.check(true, retCodeConfig.getProcessInBondingFlowSection(),params.getSplitRouteID(),params.getStrExperimentalFutureLotDetailInfoSeq().get(mergeCnt).getMergeOperationNumber());
                }
            }
            //Set In Param's
            lotFamilyIDTemp = params.getLotFamilyID();
            splitRouteIDTemp = params.getSplitRouteID();
            splitOperationNumberTemp = params.getSplitOperationNumber();
            originalRouteIDTemp = params.getOriginalRouteID();
            originalOperationNumberTemp = params.getOriginalOperationNumber();
            actionEMailTemp = params.getActionEMail();
            actionHoldTemp = params.getActionHold();
            testMemoTemp = params.getTestMemo();
            execFlagTemp = params.getExecFlag();
            actionTimeStampTemp = params.getActionTimeStamp();
            modifyTimeStampTemp = CimDateUtils.convertToSpecString(objCommon.getTimeStamp().getReportTimeStamp());
            modifyUserIDTemp = params.getModifyUserID().getValue();
            strExperimentalFutureLotDetailInfoSeqTemp = params.getStrExperimentalFutureLotDetailInfoSeq();
        } else {
            //When update execFlag and actionTimeStamp, another data should be copied from DB.
            modifyUserIDTemp = params.getModifyUserID().getValue();
            lotFamilyIDTemp = strDefData.getLotFamilyID();
            splitRouteIDTemp = strDefData.getSplitRouteID();
            splitOperationNumberTemp = strDefData.getSplitOperationNumber();
            originalRouteIDTemp = strDefData.getOriginalRouteID();
            originalOperationNumberTemp = strDefData.getOriginalOperationNumber();
            actionEMailTemp = strDefData.getActionEMail();
            actionHoldTemp = strDefData.getActionHold();
            testMemoTemp = strDefData.getTestMemo();
            execFlagTemp = params.getExecFlag();
            actionTimeStampTemp = params.getActionTimeStamp();
            modifyTimeStampTemp = strDefData.getModifyTimeStamp();
            modifyUserIDTemp = strDefData.getModifyUserID().getValue();
            strExperimentalFutureLotDetailInfoSeqTemp = strDefData.getStrExperimentalFutureLotDetailInfoSeq();
        }
        if (actLen > 0 && actLen == foundCnt && CimBooleanUtils.isFalse(execFlagTemp)) {
            log.info("So all details already done. execFlag set to TRUE");
            execFlagTemp = true;
            actionTimeStampTemp = latestActionTimeStamp;
        }
        //Update associated information.
        //【step13】experimental_lotInfo_Update
//        //add runcard logic start
//        String modifyPsmKey = params.getStrExperimentalFutureLotDetailInfoSeq().stream().anyMatch(experimentalLotDetailInfo -> BooleanUtils.isTrue(experimentalLotDetailInfo.getModifyFlag())) ? params.getStrExperimentalFutureLotDetailInfoSeq().stream().filter(experimentalLotDetailInfo -> BooleanUtils.isTrue(experimentalLotDetailInfo.getModifyFlag())).findFirst().get().getPsmKey() : null;
//        List<String> inputPsmKeysList = params.getStrExperimentalFutureLotDetailInfoSeq().stream().map(com.fa.cim.fsm.Infos.ExperimentalFutureLotDetailInfo::getPsmKey).collect(Collectors.toList());
//        Set<String> oldSet = new HashSet<>(inputPsmKeysList);
//        Set<String> newSet = new HashSet<>();
//        if (ArrayUtils.isNotEmpty(params.getOriginalPsmKeys())){
//            newSet = new HashSet<>(params.getOriginalPsmKeys());
//            newSet.removeAll(oldSet);
//        }
//        String removePsmKey = null;
//        if (!newSet.isEmpty()){
//            removePsmKey = newSet.toArray()[0].toString();
//        }
//        //add runcard logic end
        String futrueSplitJobID = experimentalForFutureMethod.experimentalFutureLotInfoUpdate(objCommon, lotFamilyIDTemp, splitRouteIDTemp, splitOperationNumberTemp, originalRouteIDTemp, originalOperationNumberTemp, actionEMailTemp, actionHoldTemp,
                testMemoTemp, execFlagTemp, actionTimeStampTemp, modifyTimeStampTemp, modifyUserIDTemp, strExperimentalFutureLotDetailInfoSeqTemp,params.getActionSeparateHold(), params.getActionCombineHold());

        //Make History
        com.fa.cim.fsm.Infos.ExperimentalFutureLotRegistInfo strExperimentalFutureLotRegistInfo = new com.fa.cim.fsm.Infos.ExperimentalFutureLotRegistInfo();
        List<com.fa.cim.fsm.Infos.ExperimentalFutureLotRegist> tmpStrExperimentalFutureLotRegistSeq = new ArrayList<>();
        //add psmJobID for history
        strExperimentalFutureLotRegistInfo.setUserID(modifyUserIDTemp);
        strExperimentalFutureLotRegistInfo.setFsmJobID(futrueSplitJobID);
        strExperimentalFutureLotRegistInfo.setAction(BizConstant.SP_EWR_ACTION_UPDATE);
        strExperimentalFutureLotRegistInfo.setClaimedTimeStamp(actionTimeStampTemp);
        strExperimentalFutureLotRegistInfo.setLotFamilyID(lotFamilyIDTemp);
        strExperimentalFutureLotRegistInfo.setSplitRouteID(splitRouteIDTemp);
        strExperimentalFutureLotRegistInfo.setSplitOperationNumber(splitOperationNumberTemp);
        strExperimentalFutureLotRegistInfo.setOriginalRouteID(originalRouteIDTemp);
        strExperimentalFutureLotRegistInfo.setOriginalOperationNumber(originalOperationNumberTemp);
        strExperimentalFutureLotRegistInfo.setActionEMail(actionEMailTemp);
        strExperimentalFutureLotRegistInfo.setTestMemo(testMemoTemp);
        int lenDetailInfoSeq = CimArrayUtils.getSize(strExperimentalFutureLotDetailInfoSeqTemp);
        for (int i = 0; i < lenDetailInfoSeq; i++) {
            com.fa.cim.fsm.Infos.ExperimentalFutureLotRegist experimentalLotRegist = new com.fa.cim.fsm.Infos.ExperimentalFutureLotRegist();
            experimentalLotRegist.setRouteID(strExperimentalFutureLotDetailInfoSeqTemp.get(i).getRouteID());
            experimentalLotRegist.setReturnOperationNumber(strExperimentalFutureLotDetailInfoSeqTemp.get(i).getReturnOperationNumber());
            experimentalLotRegist.setMergeOperationNumber(strExperimentalFutureLotDetailInfoSeqTemp.get(i).getMergeOperationNumber());
            experimentalLotRegist.setMemo(strExperimentalFutureLotDetailInfoSeqTemp.get(i).getMemo());
            //List<ObjectIdentifier> identifier = strExperimentalFutureLotDetailInfoSeqTemp.get(i).getWaferIDs().stream().map(m -> m.getWaferID()).collect(Collectors.toList());
            experimentalLotRegist.setWaferIDs(strExperimentalFutureLotDetailInfoSeqTemp.get(i).getWaferIDs());
        tmpStrExperimentalFutureLotRegistSeq.add(experimentalLotRegist);
        }
        strExperimentalFutureLotRegistInfo.setStrExperimentalFutureLotRegistSeq(tmpStrExperimentalFutureLotRegistSeq);
        //【step14】 experimental_lotRegistEvent_Make
        String tx = TransactionIDEnum.EXPERIMENTAL_LOT_UPDATE_REQ.getValue();
        if (CimBooleanUtils.isFalse(params.getExecFlag())) {
            // task-3988 this function not has been difinde,for now just comment it out
            // eventMethod.experimentalLotRegistEventMake(objCommon, tx, params.getClaimMemo(), strExperimentalFutureLotRegistInfo);
            futureEventMethod.experimentalLotRegistEventMake(objCommon, tx, params.getClaimMemo(), strExperimentalFutureLotRegistInfo);
        } else {
            log.info("#### PSM Execution Update. So does not Make Update History ### execFlag == TRUE");
        }
        //Check if lot's branch information matches with PSM definition (Checked only when lot is on branch route) When they aren't match, PSM might not triggered and warning message is returned.
        String warningOnPSMRegistration = StandardProperties.OM_PSM_BRANCH_WARNING_ON_REGISTRATION.getValue();
        log.info("warningOnPSMRegistration", warningOnPSMRegistration);
        if (CimStringUtils.equals("1", warningOnPSMRegistration)) {
            Boolean warningFalg = false;
            if (CimStringUtils.equals(params.getSplitRouteID().getValue(), params.getOriginalRouteID().getValue())) {
                log.info("Split is on main route, no check", params.getSplitRouteID().getValue());
            } else {
                log.info("Split is on sub route, check", params.getSplitRouteID().getValue());
                for (int i = 0; i < len; i++) {
                    int len2 = CimArrayUtils.getSize(params.getStrExperimentalFutureLotDetailInfoSeq().get(i).getWaferIDs());
                    log.info("strExperimentalLotDetailInfoSeq[i].subRouteID", params.getStrExperimentalFutureLotDetailInfoSeq().get(i).getRouteID().getValue());
                    log.info("strExperimentalLotDetailInfoSeq[i].waferIDs.length", len2);
                    int lotIDCnt = 0;
                    List<ObjectIdentifier> lotIDs = new ArrayList<>();
                    for (int j = 0; j < len2; j++) {
                        log.info("check wafer", j, params.getStrExperimentalFutureLotDetailInfoSeq().get(i).getWaferIDs().get(j).getWaferID().getValue());
                        //Get Lot from Wafer
                        //【step15】wafer_lot_Get

                        // task-3988 the function about wafer,but for now it`s not work
                        ObjectIdentifier getWaferLotOut = waferMethod.waferLotGet(objCommon, params.getStrExperimentalFutureLotDetailInfoSeq().get(i).getWaferIDs().get(j).getWaferID());
                        Boolean lotFoundFlag = false;
                        for (int k = 0; k < lotIDCnt; k++) {
                            log.info("Loop lotID", k, lotIDs.get(k).getValue());
                            if (CimStringUtils.equals(lotIDs.get(k).getValue(), getWaferLotOut.getValue())) {
                                log.info("Found lotID", lotIDs.get(k).getValue());
                                lotFoundFlag = true;
                                break;
                            }
                        }
                        if (CimBooleanUtils.isFalse(lotFoundFlag)) {
                            log.info("Add lotID to lotIDs", getWaferLotOut.getValue());
                            lotIDs.add(lotIDCnt, getWaferLotOut);
                            lotIDCnt++;
                        } else {
                            log.info("LotID was already checked, continue", getWaferLotOut.getValue());
                            continue;
                        }
                        log.info("Check LotID", getWaferLotOut.getValue());
                        //Get Lot's original/sub routeID and OpeNo for PSM
                        //【step16】process_originalOperation_Get
                        Infos.ProcessOriginalOperationGetOut processOriginalOperationGetOut = processMethod.processOriginalOperationGet(objCommon, getWaferLotOut);
                        log.info("strProcess_originalOperation_Get_out.originalMainPDID", processOriginalOperationGetOut.getOriginalMainPDID());
                        log.info("strProcess_originalOperation_Get_out.originalOpeNo ", processOriginalOperationGetOut.getOriginalOpeNo());
                        log.info("strProcess_originalOperation_Get_out.subOrigMainPDID", processOriginalOperationGetOut.getSubOrigMainPDID());
                        log.info("strProcess_originalOperation_Get_out.subOrigOpeNo", processOriginalOperationGetOut.getSubOrigOpeNo());
                        if (0 == processOriginalOperationGetOut.getBranchNestLevel()) {
                            log.info("Lot is on Main Route.");
                        } else if (1 == processOriginalOperationGetOut.getBranchNestLevel()) {
                            log.info("Lot is on Sub Route.");
                            //Get currentRouteID.
                            //【step17】lot_currentRouteID_Get
                            ObjectIdentifier currentRouteID = new ObjectIdentifier();
                            ObjectIdentifier getLotCurrentRouteIDOut = lotMethod.lotCurrentRouteIDGet(objCommon, getWaferLotOut);

                            currentRouteID = getLotCurrentRouteIDOut;
                            log.info("Lot current is", currentRouteID.getValue());
                            if (ObjectIdentifier.equalsWithValue(currentRouteID, params.getSplitRouteID())
                                    && ObjectIdentifier.equalsWithValue(processOriginalOperationGetOut.getOriginalMainPDID(), params.getOriginalRouteID())) {
                                if (!CimStringUtils.equals(processOriginalOperationGetOut.getOriginalOpeNo(), params.getOriginalOperationNumber())) {
                                    log.info("OriginalOpeNo is ", processOriginalOperationGetOut.getOriginalOpeNo());
                                    warningFalg = true;
                                    break;
                                }
                            }
                        } else {
                            log.info("Lot is on Sub-Sub Route.");
                            if (ObjectIdentifier.equalsWithValue(processOriginalOperationGetOut.getSubOrigMainPDID(), params.getSplitRouteID())
                                    && ObjectIdentifier.equalsWithValue(processOriginalOperationGetOut.getOriginalMainPDID(), params.getOriginalRouteID())) {
                                log.info("Lot is on branch Route which is used in PSM definition");
                                if (!CimStringUtils.equals(processOriginalOperationGetOut.getOriginalOpeNo(), params.getOriginalOperationNumber())) {
                                    log.info("OriginalOpeNo is ", processOriginalOperationGetOut.getOriginalOpeNo());
                                    warningFalg = true;
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            Validations.check(warningFalg, retCodeConfigEx.getFsmExplotAlreadyChanged());
        }
    }
}