package com.fa.cim.service.processcontrol.impl;

import com.fa.cim.annotaion.OmService;
import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.constant.CIMStateConst;
import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.OmCode;
import com.fa.cim.common.support.RetCode;
import com.fa.cim.common.support.User;
import com.fa.cim.common.utils.*;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.config.RetCodeConfigEx;
import com.fa.cim.dto.*;
import com.fa.cim.method.*;
import com.fa.cim.newcore.bo.pd.CimProcessDefinition;
import com.fa.cim.newcore.bo.product.CimLot;
import com.fa.cim.service.lot.ILotService;
import com.fa.cim.service.processcontrol.IProcessControlService;
import com.fa.cim.service.system.ISystemService;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.fa.cim.common.constant.BizConstant.SP_ENTRYTYPE_CANCEL;
import static com.fa.cim.common.constant.BizConstant.SP_REASON_QTIMECLEAR;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/9/8        ********            Nyx                create file
 *
 * @author: Nyx
 * @date: 2020/9/8 16:55
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@OmService
public class ProcessControlService implements IProcessControlService {
    @Autowired
    private RetCodeConfig retCodeConfig;

    @Autowired
    private ICodeMethod codeComp;

    @Autowired
    private ILotMethod lotMethod;

    @Autowired
    private IEventMethod eventMethod;

    @Autowired
    private IObjectLockMethod objectLockMethod;

    @Autowired
    private IProcessMethod processMethod;

    @Autowired
    private ILotService lotService;

    @Autowired
    private IQTimeMethod qTimeMethod;

    @Autowired
    private IProcessControlService processControlService;

    @Autowired
    private ISystemService systemService;

    @Autowired
    private IMessageMethod messageMethod;

    @Autowired
    private IPersonMethod personMethod;

    @Autowired
    private RetCodeConfigEx retCodeConfigEx;

    @Autowired
    private IObjectLockMethod lockMethod;

    @Autowired
    private IScheduleChangeReservationMethod scheduleChangeReservationMethod;
    @Autowired
    private ICodeMethod codeMethod;

    @Override
    public void sxFutureHoldReq(Infos.ObjCommon objCommon, Params.FutureHoldReqParams params) {

        ObjectIdentifier lotID = params.getLotID();
        ObjectIdentifier codeDataID = params.getReasonCodeID();

        // [step1] -  Lock objects to be updated [object_Lock]
        objectLockMethod.objectLock(objCommon, CimLot.class, lotID);
        // [step2] -Check lot interFabXferState [lot_interFabXferState_Get]
        log.info("[FutureHoldReq - step2] : Get and Check lotState (must not be Finished)");
        String interFabXferState = lotMethod.lotStateGet(objCommon, lotID);
        Validations.check(CimStringUtils.equals(BizConstant.CIMFW_LOT_STATE_FINISHED, interFabXferState), retCodeConfig.getInvalidLotStat());

        /*-----------------------------------------------------------*/
        /*  [step3] Check PosCode                                           */
        /*-----------------------------------------------------------*/
        log.info("[FutureHoldReq - step3] : Check PosCode");
        List<ObjectIdentifier> codeDataIDs = new ArrayList<>();
        codeDataIDs.add(codeDataID);
        try{
            codeComp.codeCheckExistanceDR(objCommon, CIMStateConst.CIM_LOT_HOLD_TYPE_FUTUREHOLD, codeDataIDs);
        }catch (ServiceException e) {
            String transactionID = objCommon.getTransactionID();
            if(transactionID.equals("OPRCW001") || transactionID.equals("TXPCC029")){
                throw e;
            }else {
                codeComp.codeCheckExistanceDR(objCommon, BizConstant.SP_REASONCAT_LOTHOLD, codeDataIDs);
            }
        }
        // [step4] - TODO-Corecode： is not implemented completely yet: Check lot interFabXferState [lot_interFabXferState_Get]
        //-----------------------------------------------------------
        // Check lot interFabXferState
        //-----------------------------------------------------------
        String strLotInterFabXferState = lotMethod.lotInterFabXferStateGet(objCommon, lotID);
        //-----------------------------------------------------------
        // "Transferring"
        //-----------------------------------------------------------
        Validations.check(CimStringUtils.equals(strLotInterFabXferState, BizConstant.SP_INTERFAB_XFERSTATE_TRANSFERRING), retCodeConfig.getInterfabInvalidLotXferstateForReq());

        // [step5] - Create a list into PosProcessOperation of a lot
        log.info("[FutureHoldReq - step5] : Create a list into PosProcessOperation of a lot");
        Infos.FutureHoldHistory entryOut = lotMethod.lotFutureHoldRequestsMakeEntry(objCommon, params);


        // [step6] Call lotFutureHoldEvent_Make [lotFutureHoldEvent_Make]
        log.info("Call lotFutureHoldEvent_Make");
        Inputs.LotFutureHoldEventMakeParams lotFutureHoldEventMakeParams = new Inputs.LotFutureHoldEventMakeParams();
        lotFutureHoldEventMakeParams.setTransactionID(TransactionIDEnum.ENHANCED_FUTURE_HOLD_REQ.getValue());
        lotFutureHoldEventMakeParams.setLotID(params.getLotID());
        lotFutureHoldEventMakeParams.setEntryType(BizConstant.SP_ENTRYTYPE_ENTRY);
        lotFutureHoldEventMakeParams.setFutureHoldHistory(entryOut);
        lotFutureHoldEventMakeParams.setClaimMemo(params.getClaimMemo());
        lotFutureHoldEventMakeParams.setReleaseReasonCode(new ObjectIdentifier());
        eventMethod.lotFutureHoldEventMake(objCommon, lotFutureHoldEventMakeParams);
    }

    @Override
    public void sxFutureHoldCancelReq(Infos.ObjCommon objCommon, Params.FutureHoldCancelReqParams params) {

        // [step1] - Lock objects to be updated [object_Lock]
        objectLockMethod.objectLock(objCommon, CimLot.class, params.getLotID());

        // [step2] - Check lot interFabXferState [lot_interFabXferState_Get]
        log.info("[FutureHoldCancelReq - step2] :Check lot interFabXferState ");
        String interFabXferState = lotMethod.lotStateGet(objCommon, params.getLotID());
        Validations.check(CimStringUtils.equals(BizConstant.SP_INTERFAB_XFERSTATE_TRANSFERRING, interFabXferState), retCodeConfig.getInterfabInvalidLotXferstateForReq(), params.getLotID());

        // [step3] - Delete a list in PosProcessOperation
        log.info("[FutureHoldCancelReq - step3] : Delete a list in PosProcessOperation ");
        List<Infos.FutureHoldHistory> futureHoldHistoryList = lotMethod.lotFutureHoldRequestsDeleteEntry(objCommon, params);


        // [step4] - Call lotFutureHoldEvent_Make [lotFutureHoldEvent_Make]
        if (!CimObjectUtils.isEmpty(futureHoldHistoryList)){
            Inputs.LotFutureHoldEventMakeParams lotFutureHoldEventMakeParams = new Inputs.LotFutureHoldEventMakeParams();
            lotFutureHoldEventMakeParams.setLotID(params.getLotID());
            lotFutureHoldEventMakeParams.setTransactionID(TransactionIDEnum.FUTURE_HOLD_CANCEL_REQ.getValue());
            lotFutureHoldEventMakeParams.setEntryType(params.getEntryType());
            lotFutureHoldEventMakeParams.setReleaseReasonCode(params.getReleaseReasonCodeID());
            lotFutureHoldEventMakeParams.setClaimMemo("");
            for (Infos.FutureHoldHistory futureHoldHistory : futureHoldHistoryList){
                lotFutureHoldEventMakeParams.setFutureHoldHistory(futureHoldHistory);
                eventMethod.lotFutureHoldEventMake(objCommon, lotFutureHoldEventMakeParams);
            }
        }
    }

    @Override
    public void sxFutureHoldCancelReq(Infos.ObjCommon objCommon, ObjectIdentifier lotID, ObjectIdentifier releaseReasonCodeID, String entityType, List<Infos.LotHoldReq> futureHoldCancelReqList) {
        Params.FutureHoldCancelReqParams params = new Params.FutureHoldCancelReqParams();
        params.setLotID(lotID);
        params.setReleaseReasonCodeID(releaseReasonCodeID);
        params.setEntryType(entityType);
        params.setLotHoldList(futureHoldCancelReqList);

        sxFutureHoldCancelReq(objCommon, params);
    }

    @Override
    public Results.NPWUsageStateModifyReqResult sxNPWUsageStateModifyReq(Infos.ObjCommon objCommon, ObjectIdentifier lotID, String controlUseState, int usageCount) {
        //-----------------------------------------------------------
        // Check lot interFabXferState
        //-----------------------------------------------------------
        String interFabXferState = lotMethod.lotInterFabXferStateGet(objCommon, lotID);
        //-----------------------------------------------------------
        // "Transferring"
        //-----------------------------------------------------------
        Validations.check(CimStringUtils.equals(interFabXferState, BizConstant.SP_INTERFAB_XFERSTATE_TRANSFERRING),
                new OmCode(retCodeConfig.getInterfabInvalidLotXferstateForReq(), lotID.getValue(), interFabXferState));
        /*---------------------------------------------------------*/
        /*   Change lotControlUseState and usedCount information   */
        /*---------------------------------------------------------*/
        Outputs.ObjLotControlUseInfoChangeOut objLotControlUseInfoChangeOut = lotMethod.lotControlUseInfoChange(objCommon, lotID, controlUseState, usageCount);

        /*-----------------------*/
        /*   Set out structure   */
        /*-----------------------*/
        Results.NPWUsageStateModifyReqResult npwUsageStateModifyReqResult = new Results.NPWUsageStateModifyReqResult();
        npwUsageStateModifyReqResult.setControlUseState(objLotControlUseInfoChangeOut.getControlUseState());
        npwUsageStateModifyReqResult.setUsageCount(objLotControlUseInfoChangeOut.getUsageCount());
        return npwUsageStateModifyReqResult;
    }

    @Override
    public void sxProcessLagTimeUpdate(Infos.ObjCommon objCommon, Params.LagTimeActionReqParams params) {
        ObjectIdentifier lotID = params.getLotID();
        //【step1】check lot interFab transfer state
        log.debug("【step1】check lot interFab transfer state");
        String interFabXferState = lotMethod.lotInterFabXferStateGet(objCommon, lotID);
        //-----------------------------------------------------------
        // "Transferring"
        //-----------------------------------------------------------
        if (CimStringUtils.equals(interFabXferState, BizConstant.SP_INTERFAB_XFERSTATE_TRANSFERRING)){
            throw new ServiceException(new OmCode(retCodeConfig.getInterfabInvalidCassetteTransferStateForRequest(), lotID.getValue(), interFabXferState));
        }

        //【step2】action:SP_ProcessLagTime_Action_Set
        log.debug("【step2】action:SP_ProcessLagTime_Action_Set");
        if (CimStringUtils.equals(BizConstant.SP_PROCESSLAGTIME_ACTION_SET, params.getAction())) {
            log.debug("【in-param】action = SP_PROCESSLAGTIME_ACTION_SET");
            /**********************************************************************************************************/
            /*【step2-1】set process lag time info                                                                    */
            /*   - get process lag time info                                                                          */
            /*   - set process lag time info                                                                          */
            /**********************************************************************************************************/
            log.debug("【step2-1】set process lag time info");
            //【step2-1-1】get process lag time info
            Outputs.ObjProcessLagTimeGetOut lagTimeGetOutRetCode = processMethod.processProcessLagTimeGet(objCommon, lotID);

            //【step2-2】set process lag time information to lot
            log.debug("【step2-2】set process lag time information to lot");
            lotMethod.lotProcessLagTimeSet(objCommon, lotID, lagTimeGetOutRetCode.getProcessLagTimeStamp().toString());

            Double expriedTimeDuration = lagTimeGetOutRetCode.getExpriedTimeDuration();
            if (null != expriedTimeDuration && expriedTimeDuration > 0D) {
                log.debug("expired time duration > 0...");
                //prepare for txHoldLotReq's Input paramter.
                User user = new User();
                ObjectIdentifier holdUserID = new ObjectIdentifier(BizConstant.SP_PPTSVCMGR_PERSON);
                user.setUserID(holdUserID);
                objCommon.setUser(user);

                ObjectIdentifier holdReasonCodeID = new ObjectIdentifier(BizConstant.SP_REASON_PROCESSLAGTIMEHOLD);
                List<Infos.LotHoldReq> holdReqList = new ArrayList<>();
                Infos.LotHoldReq lotHoldReq = new Infos.LotHoldReq();
                lotHoldReq.setHoldType(BizConstant.SP_HOLDTYPE_LOTHOLD);
                lotHoldReq.setHoldReasonCodeID(holdReasonCodeID);
                lotHoldReq.setHoldUserID(holdUserID);
                lotHoldReq.setResponsibleOperationMark(BizConstant.SP_RESPONSIBLEOPERATION_CURRENT);
                lotHoldReq.setClaimMemo(params.getClaimMemo());
                holdReqList.add(lotHoldReq);
                //【step2-2-1】call sxHoldLotReq
                try {
                    lotService.sxHoldLotReq(objCommon, lotID, holdReqList);
                } catch (ServiceException e) {
                    if (!Validations.isEquals(retCodeConfig.getExistSameHold(), e.getCode())){
                        throw e;
                    }
                }
            }
        } else {
            //【step3】action:SP_ProcessLagTime_Action_Clear
            log.debug("【step3】action:SP_ProcessLagTime_Action_Clear");
            log.debug("【in-param】action = SP_PROCESSLAGTIME_ACTION_CLEAR");
            //【step3-1】clear lot's process lag time information
            log.debug("【step3-1】clear lot's process lag time information");
            lotMethod.lotProcessLagTimeSet(objCommon, lotID, BizConstant.SP_TIMESTAMP_NIL_OBJECT_STRING);
            //-----------------------------------------------------------
            //   Prepare for txHoldLotReleaseReq's Input Parameter
            //-----------------------------------------------------------
            ObjectIdentifier holdReasonCodeID = new ObjectIdentifier(BizConstant.SP_REASON_PROCESSLAGTIMEHOLD);
            ObjectIdentifier releaseReasonCodeID = new ObjectIdentifier(BizConstant.SP_REASON_PROCESSLAGTIMEHOLDRELEASE);
            ObjectIdentifier holdUserID = new ObjectIdentifier(BizConstant.SP_PPTSVCMGR_PERSON);
            List<Infos.LotHoldReq> holdReqList = new ArrayList<>();
            Infos.LotHoldReq lotHoldReq = new Infos.LotHoldReq();
            lotHoldReq.setHoldType(BizConstant.SP_HOLDTYPE_LOTHOLD);
            lotHoldReq.setHoldReasonCodeID(holdReasonCodeID);
            lotHoldReq.setHoldUserID(holdUserID);
            lotHoldReq.setResponsibleOperationMark(BizConstant.SP_RESPONSIBLEOPERATION_CURRENT);
            holdReqList.add(lotHoldReq);
            //【step3-2】release "PLTH" hold record by txHoldLotReleaseReq
            log.debug("【step3-2】release \"PLTH\" hold record by txHoldLotReleaseReq");
            Params.HoldLotReleaseReqParams holdLotReleaseReqParams = new Params.HoldLotReleaseReqParams();
            holdLotReleaseReqParams.setReleaseReasonCodeID(releaseReasonCodeID);
            holdLotReleaseReqParams.setLotID(lotID);
            holdLotReleaseReqParams.setHoldReqList(holdReqList);
            try{
                lotService.sxHoldLotReleaseReq(objCommon, holdLotReleaseReqParams);
            }catch (ServiceException e) {
                if (!Validations.isEquals(retCodeConfig.getLotNotHeld(), e.getCode())
                        && !Validations.isEquals(retCodeConfig.getNotExistHold(), e.getCode()) ) {
                    throw e;
                }
            }
        }
    }

    @Override
    public RetCode<String> sxQtimeActionReq(Infos.ObjCommon objCommon, Inputs.QtimeActionReqIn qtimeActionReqIn) {
        RetCode<String> result = new RetCode<>();
        Boolean bActionDoneFlag = false;
        Boolean bNeedToCallPostProcess = false;
        Boolean bNeedAlertMessageRpt = false;
        String systemMessage = null;
        String systemMsgCode = null;
        ObjectIdentifier lotID = qtimeActionReqIn.getLotID();
        String lotStatus = qtimeActionReqIn.getLotStatus();
        ObjectIdentifier routeID = qtimeActionReqIn.getRouteID();
        String operationNumber = qtimeActionReqIn.getOperationNumber();
        ObjectIdentifier equipmentID = qtimeActionReqIn.getEquipmentID();
        ObjectIdentifier actionRouteID = qtimeActionReqIn.getStrQrestTimeAction().getQrestrictionTriggerRouteID();
        ObjectIdentifier originalRouteID = qtimeActionReqIn.getStrQrestTimeAction().getQrestrictionTriggerRouteID();  //DSN000100682
        String originalTriggerOperationNumber = qtimeActionReqIn.getStrQrestTimeAction().getQrestrictionTriggerOperationNumber();
        String originalTargetOperationNumber = qtimeActionReqIn.getStrQrestTimeAction().getQrestrictionTargetOperationNumber();
        if (!CimStringUtils.isEmpty(qtimeActionReqIn.getStrQrestTimeAction().getOriginalQTime())) {
            //----------------------------------
            //  Get original Q-Time information
            //----------------------------------
            Outputs.ObjQtimeOriginalInformationGetOut qtimeOriginalInformationOut = qTimeMethod.qtimeOriginalInformationGet(objCommon, qtimeActionReqIn.getStrQrestTimeAction().getOriginalQTime());
            //----------------------------------
            //  Set action route ID
            //----------------------------------
            if (!ObjectIdentifier.isEmptyWithValue(qtimeOriginalInformationOut.getTargetRouteID())) {
                actionRouteID = qtimeOriginalInformationOut.getTriggerRouteID();
                originalRouteID = qtimeOriginalInformationOut.getTriggerRouteID();  //DSN000100682
                originalTriggerOperationNumber = qtimeOriginalInformationOut.getTriggerOperationNumber();
                originalTargetOperationNumber = qtimeOriginalInformationOut.getTriggerOperationNumber();
            }
        }
        if (!ObjectIdentifier.isEmptyWithValue(qtimeActionReqIn.getStrQrestTimeAction().getActionRouteID())) {
            //--------------------------------------------
            //  Set action route ID (replace sub route ID)
            //--------------------------------------------
            actionRouteID = qtimeActionReqIn.getStrQrestTimeAction().getActionRouteID();
        }
        //----------------------------------
        //  Get InPostProcessFlag of lot
        //----------------------------------
        //lot_inPostProcessFlag_Get
        Outputs.ObjLotInPostProcessFlagOut lotInPostProcessFlagOut = lotMethod.lotInPostProcessFlagGet(objCommon, lotID);

        //-----------------------------------------------------------
        // Check lot interFabXferState
        //-----------------------------------------------------------
        //lot_interFabXferState_Get
        String interFabXferState = lotMethod.lotInterFabXferStateGet(objCommon, lotID);
        Validations.check(CimStringUtils.equals(interFabXferState, BizConstant.SP_INTERFAB_XFERSTATE_TRANSFERRING)
                , new OmCode(retCodeConfig.getInterfabInvalidLotXferstateForReq(), ObjectIdentifier.fetchValue(lotID), interFabXferState));

        if (CimBooleanUtils.isTrue(lotInPostProcessFlagOut.getInPostProcessFlagOfLot())) {
            if (!CimStringUtils.equals(interFabXferState, BizConstant.SP_INTERFAB_XFERSTATE_REQUIRED)) {
                List<ObjectIdentifier> userGroupList = personMethod.personUserGroupListGetDR(objCommon, objCommon.getUser().getUserID());
                int nCnt = 0;
                for (nCnt = 0; nCnt < CimArrayUtils.getSize(userGroupList); nCnt++) {
                }
                Validations.check(nCnt == CimArrayUtils.getSize(userGroupList), new OmCode(retCodeConfig.getLotInPostProcess(), ObjectIdentifier.fetchValue(lotID)));
            }
        }

        //----------------------------------------------
        //  If lot is in post process, returns error
        //----------------------------------------------
        //【TODO】【TODO - NOTIMPL】- person_userGroupList_GetDR
        Outputs.QrestTimeAction strQTimeAction = qtimeActionReqIn.getStrQrestTimeAction();
        if (CimBooleanUtils.isTrue(qtimeActionReqIn.getStrQrestTimeAction().getActionDoneOnlyFlag())) {
            bActionDoneFlag = true;
        } else {
            /*--------------------------------------------------------------------------*/
            /* Action execution section for SP_QTimeRestriction_Action_ImmediateHold    */
            /*--------------------------------------------------------------------------*/
            if (CimStringUtils.equals(qtimeActionReqIn.getStrQrestTimeAction().getQrestrictionAction(), BizConstant.SP_QTIMERESTRICTION_ACTION_IMMEDIATEHOLD)) {
                /*-----------------------------------------------------------*/
                /*   Check ProcessState of the lot                           */
                /*-----------------------------------------------------------*/
                String lotProcessStateRetCode = lotMethod.lotProcessStateGet(objCommon, qtimeActionReqIn.getLotID());
                if (CimStringUtils.equals(lotProcessStateRetCode, BizConstant.SP_LOT_PROCSTATE_PROCESSING)) {
                    /*-----------------------------------------------------------*/
                    /*   FutureHold(Post) registration instead of lot Hold       */
                    /*-----------------------------------------------------------*/
                    //txFutureHoldReq
                    Params.FutureHoldReqParams futureHoldReqParams = new Params.FutureHoldReqParams();
                    futureHoldReqParams.setHoldType(BizConstant.SP_HOLDTYPE_QTIMEOVERHOLD);
                    futureHoldReqParams.setLotID(qtimeActionReqIn.getLotID());
                    futureHoldReqParams.setRouteID(qtimeActionReqIn.getRouteID());
                    futureHoldReqParams.setOperationNumber(qtimeActionReqIn.getOperationNumber());
                    futureHoldReqParams.setReasonCodeID(qtimeActionReqIn.getStrQrestTimeAction().getReasonCodeID());
                    futureHoldReqParams.setPostFlag(true);
                    futureHoldReqParams.setSingleTriggerFlag(true);
                    futureHoldReqParams.setClaimMemo(qtimeActionReqIn.getClaimMemo());
                    try {
                        processControlService.sxFutureHoldReq(objCommon, futureHoldReqParams);
                    } catch (ServiceException e) {
                        if (!Validations.isEquals(retCodeConfig.getDuplicateFtholdEntry(), e.getCode())) {
                            throw e;
                        }
                    }
                    strQTimeAction.setActionOperationNumber(qtimeActionReqIn.getOperationNumber());
                    bActionDoneFlag = true;
                } else {
                    /*-----------------------------------------------------------*/
                    /*   Hold the lot                                            */
                    /*-----------------------------------------------------------*/
                    Infos.LotHoldReq lotHoldReq = new Infos.LotHoldReq();
                    lotHoldReq.setHoldType(BizConstant.SP_HOLDTYPE_QTIMEOVERHOLD);
                    lotHoldReq.setHoldReasonCodeID(qtimeActionReqIn.getStrQrestTimeAction().getReasonCodeID());
                    lotHoldReq.setHoldUserID(objCommon.getUser().getUserID());
                    lotHoldReq.setResponsibleOperationMark(BizConstant.SP_RESPONSIBLEOPERATION_CURRENT);
                    lotHoldReq.setRouteID(qtimeActionReqIn.getRouteID());
                    lotHoldReq.setOperationNumber(qtimeActionReqIn.getOperationNumber());
                    lotHoldReq.setClaimMemo(qtimeActionReqIn.getClaimMemo());
                    List<Infos.LotHoldReq> lotHoldReqList = new ArrayList<>();
                    lotHoldReqList.add(lotHoldReq);
                    try {
                        lotService.sxHoldLotReq(objCommon, qtimeActionReqIn.getLotID(), lotHoldReqList);
                    }catch (ServiceException ex){
                        if (!Validations.isEquals(retCodeConfig.getExistSameHold(), ex.getCode())) {
                            throw new ServiceException(new OmCode(ex.getCode(), ex.getMessage()));
                        }
                    }
                    bActionDoneFlag = true;
                    bNeedToCallPostProcess = true;
                }
                /*--------------------------------------------------------------------------*/
                /* Action execution section for SP_QTimeRestriction_Action_FutureHold       */
                /*--------------------------------------------------------------------------*/
            } else if (CimStringUtils.equals(qtimeActionReqIn.getStrQrestTimeAction().getQrestrictionAction(), BizConstant.SP_QTIMERESTRICTION_ACTION_FUTUREHOLD)) {
                Boolean bNeedToCallFutureHold = false;
                /*-----------------------------------------------------------*/
                /*   Compare lot's current Ope. with the specified Ope.      */
                /*-----------------------------------------------------------*/
                Inputs.ObjProcessOperationListForLotIn objProcessOperationListForLotIn = new Inputs.ObjProcessOperationListForLotIn();
                objProcessOperationListForLotIn.setSearchDirectionFlag(true);
                objProcessOperationListForLotIn.setPosSearchFlag(false);
                objProcessOperationListForLotIn.setSearchCount(Integer.valueOf(BizConstant.SP_SEARCH_COUNT_MAX));
                objProcessOperationListForLotIn.setSearchRouteID(actionRouteID);
                objProcessOperationListForLotIn.setSearchOperationNumber(qtimeActionReqIn.getStrQrestTimeAction().getActionOperationNumber());
                objProcessOperationListForLotIn.setCurrentFlag(true);
                objProcessOperationListForLotIn.setLotID(qtimeActionReqIn.getLotID());
                List<Infos.OperationNameAttributes> strOperationAttributeList = processMethod.processOperationListForLot(objCommon, objProcessOperationListForLotIn);
                Integer opeLen = strOperationAttributeList.size();
                Validations.check(opeLen <=0, new OmCode(retCodeConfig.getNotFoundCorrpo(), qtimeActionReqIn.getStrQrestTimeAction().getActionOperationNumber()));
                if (ObjectIdentifier.equalsWithValue(actionRouteID, strOperationAttributeList.get(opeLen - 1).getRouteID())
                        && CimStringUtils.equals(qtimeActionReqIn.getStrQrestTimeAction().getActionOperationNumber()
                        ,strOperationAttributeList.get(opeLen - 1).getOperationNumber())) {
                    if (1 < opeLen) {
                        result.setReturnCode(retCodeConfig.getCurrentToperationEarly());
                    } else {
                        result.setReturnCode(retCodeConfig.getCurrentToperationSame());
                    }
                } else {
                    result.setReturnCode(retCodeConfig.getCurrentToperationLate());
                }
                if (retCodeConfig.getCurrentToperationLate() == result.getReturnCode()) {
                    bNeedAlertMessageRpt = true;
                    systemMsgCode = BizConstant.SP_SYSTEMMSGCODE_QRESTRICTIONSYSMSG;
                    StringBuffer systemMessageSb = new StringBuffer();
                    systemMessageSb.append("The Q-Time Over Lot has passed the operation of FutureHold action.\n")
                            .append("LotID         : ").append(qtimeActionReqIn.getLotID().getValue()).append("\n")
                            .append("RouteID       : ").append(qtimeActionReqIn.getRouteID().getValue()).append("\n")
                            .append("TriggerRouteID: ").append(actionRouteID.getValue()).append("\n")
                            .append("TriggerOpe.No : ").append(strQTimeAction.getQrestrictionTriggerOperationNumber()).append("\n")
                            .append("TargetOpe.No  : ").append(strQTimeAction.getQrestrictionTargetOperationNumber()).append("\n")
                            .append("TatgetTime    : ").append(strQTimeAction.getQrestrictionTargetTimeStamp()).append("\n");
                    systemMessage = systemMessageSb.toString();
                } else if (retCodeConfig.getCurrentToperationSame() == result.getReturnCode()) {
                    if (CimStringUtils.equals(BizConstant.SP_FUTUREHOLD_PRE, qtimeActionReqIn.getStrQrestTimeAction().getFutureHoldTiming())) {
                        /*-----------------------------------------------------------*/
                        /*   Get ProcessState of the lot for the following judgement */
                        /*-----------------------------------------------------------*/
                        String lotProcessStateRetCode = lotMethod.lotProcessStateGet(objCommon, qtimeActionReqIn.getLotID());
                        /*-----------------------------------------------------------*/
                        /*   Judge whether to call txHoldLotReq                      */
                        /*-----------------------------------------------------------*/
                        if (BizConstant.SP_LOT_PROCSTATE_PROCESSING.equals(lotProcessStateRetCode)) {
                            bNeedAlertMessageRpt = true;
                            systemMsgCode = BizConstant.SP_SYSTEMMSGCODE_QRESTRICTIONSYSMSG;
                            StringBuffer systemMessageSb = new StringBuffer();
                            systemMessageSb.append("The Q-Time Over Lot has passed the operation of FutureHold action.\n")
                                    .append("LotID         : ").append(qtimeActionReqIn.getLotID().getValue()).append("\n")
                                    .append("RouteID       : ").append(qtimeActionReqIn.getRouteID().getValue()).append("\n")
                                    .append("TriggerRouteID: ").append(actionRouteID.getValue()).append("\n")
                                    .append("TriggerOpe.No : ").append(strQTimeAction.getQrestrictionTriggerOperationNumber()).append("\n")
                                    .append("TargetOpe.No  : ").append(strQTimeAction.getQrestrictionTargetOperationNumber()).append("\n")
                                    .append("TatgetTime    : ").append(strQTimeAction.getQrestrictionTargetTimeStamp()).append("\n");
                            systemMessage = systemMessageSb.toString();
                        } else {
                            /*-----------------------------------------------------------*/
                            /*   Hold the lot                                            */
                            /*-----------------------------------------------------------*/
                            Infos.LotHoldReq lotHoldReq = new Infos.LotHoldReq();
                            lotHoldReq.setHoldType(BizConstant.SP_HOLDTYPE_QTIMEOVERHOLD);
                            lotHoldReq.setHoldReasonCodeID(qtimeActionReqIn.getStrQrestTimeAction().getReasonCodeID());
                            lotHoldReq.setHoldUserID(objCommon.getUser().getUserID());
                            lotHoldReq.setResponsibleOperationMark(BizConstant.SP_RESPONSIBLEOPERATION_CURRENT);
                            lotHoldReq.setRouteID(new ObjectIdentifier(qtimeActionReqIn.getRouteID().getValue()));
                            lotHoldReq.setOperationNumber(qtimeActionReqIn.getOperationNumber());
                            lotHoldReq.setClaimMemo(qtimeActionReqIn.getClaimMemo());
                            List<Infos.LotHoldReq> lotHoldReqList = new ArrayList<>();
                            lotHoldReqList.add(lotHoldReq);
                            try {
                                lotService.sxHoldLotReq(objCommon, qtimeActionReqIn.getLotID(), lotHoldReqList);
                            }catch (ServiceException ex){
                                Validations.check(!Validations.isEquals(retCodeConfig.getExistSameHold(), ex.getCode())
                                        , new OmCode(ex.getCode(), ex.getMessage()));
                            }
                            bActionDoneFlag = true;
                            bNeedToCallPostProcess = true;
                        }
                    } else {
                        bNeedToCallFutureHold = true;
                    }
                } else {
                    bNeedToCallFutureHold = true;
                }
                /*--------------------------*/
                /* FutureHold Action        */
                /*--------------------------*/
                if (bNeedToCallFutureHold) {
                    Boolean postFlag = false;
                    if (!CimStringUtils.equals(BizConstant.SP_FUTUREHOLD_PRE, qtimeActionReqIn.getStrQrestTimeAction().getFutureHoldTiming())) {
                        postFlag = true;
                    }
                    /*-----------------------------------------*/
                    /* FutureHold Registration                 */
                    /*-----------------------------------------*/
                    Params.FutureHoldReqParams futureHoldReqParams = new Params.FutureHoldReqParams();
                    futureHoldReqParams.setHoldType(BizConstant.SP_HOLDTYPE_QTIMEOVERHOLD);
                    futureHoldReqParams.setLotID(qtimeActionReqIn.getLotID());
                    futureHoldReqParams.setRouteID(actionRouteID);
                    futureHoldReqParams.setOperationNumber(qtimeActionReqIn.getStrQrestTimeAction().getActionOperationNumber());
                    futureHoldReqParams.setReasonCodeID(qtimeActionReqIn.getStrQrestTimeAction().getReasonCodeID());
                    futureHoldReqParams.setPostFlag(postFlag);
                    futureHoldReqParams.setSingleTriggerFlag(true);
                    futureHoldReqParams.setClaimMemo(qtimeActionReqIn.getClaimMemo());
                    try {
                        processControlService.sxFutureHoldReq(objCommon, futureHoldReqParams);
                    } catch (ServiceException e) {
                        if (!Validations.isEquals(retCodeConfig.getDuplicateFtholdEntry(), e.getCode())) {
                            throw e;
                        }
                    }

                    bActionDoneFlag = true;
                }
                /*--------------------------------------------------------------------------*/
                /* Action execution section for SP_QTimeRestriction_Action_FutureRework     */
                /*--------------------------------------------------------------------------*/
            } else if (CimStringUtils.equals(BizConstant.SP_QTIMERESTRICTION_ACTION_FUTUREREWORK, qtimeActionReqIn.getStrQrestTimeAction().getQrestrictionAction())) {
                Boolean bNeedToCallFutureRework = false;
                /*-----------------------------------------------------------*/
                /*   Compare lot's current Ope. with the specified Ope.      */
                /*-----------------------------------------------------------*/
                Inputs.ObjProcessOperationListForLotIn objProcessOperationListForLotIn = new Inputs.ObjProcessOperationListForLotIn();
                objProcessOperationListForLotIn.setSearchDirectionFlag(true);
                objProcessOperationListForLotIn.setPosSearchFlag(false);
                objProcessOperationListForLotIn.setSearchCount(Integer.valueOf(BizConstant.SP_SEARCH_COUNT_MAX));
                objProcessOperationListForLotIn.setSearchRouteID(actionRouteID);
                objProcessOperationListForLotIn.setSearchOperationNumber(qtimeActionReqIn.getStrQrestTimeAction().getActionOperationNumber());
                objProcessOperationListForLotIn.setCurrentFlag(true);
                objProcessOperationListForLotIn.setLotID(qtimeActionReqIn.getLotID());
                // TODO:  processOperationListForLot
                List<Infos.OperationNameAttributes> objProcessOperationListForLotOutRetCode = processMethod.processOperationListForLot(objCommon, objProcessOperationListForLotIn);
                List<Infos.OperationNameAttributes> strOperationAttributeList = objProcessOperationListForLotOutRetCode;
                Integer opeLen = strOperationAttributeList.size();
                Validations.check(opeLen <= 0, new OmCode(retCodeConfig.getNotFoundCorrpo(), qtimeActionReqIn.getStrQrestTimeAction().getActionOperationNumber()));
                if (ObjectIdentifier.equalsWithValue(actionRouteID, strOperationAttributeList.get(opeLen - 1).getRouteID())
                        && CimStringUtils.equals(qtimeActionReqIn.getStrQrestTimeAction().getActionOperationNumber()
                        , strOperationAttributeList.get(opeLen - 1).getOperationNumber())) {
                    if (1 < opeLen) {
                        result.setReturnCode(retCodeConfig.getCurrentToperationEarly());
                    } else {
                        result.setReturnCode(retCodeConfig.getCurrentToperationSame());
                    }
                } else {
                    result.setReturnCode(retCodeConfig.getCurrentToperationLate());
                }
                if (retCodeConfig.getCurrentToperationLate() == result.getReturnCode()) {
                    bNeedAlertMessageRpt = true;
                    systemMsgCode = BizConstant.SP_SYSTEMMSGCODE_QRESTRICTIONSYSMSG;
                    StringBuffer systemMessageSb = new StringBuffer();
                    systemMessageSb.append("The Q-Time Over Lot has passed the operation of FutureHold action.\n")
                            .append("LotID         : ").append(qtimeActionReqIn.getLotID().getValue()).append("\n")
                            .append("RouteID       : ").append(qtimeActionReqIn.getRouteID().getValue()).append("\n")
                            .append("TriggerRouteID: ").append(actionRouteID.getValue()).append("\n")
                            .append("TriggerOpe.No : ").append(strQTimeAction.getQrestrictionTriggerOperationNumber()).append("\n")
                            .append("TargetOpe.No  : ").append(strQTimeAction.getQrestrictionTargetOperationNumber()).append("\n")
                            .append("TatgetTime    : ").append(strQTimeAction.getQrestrictionTargetTimeStamp()).append("\n");
                    systemMessage = systemMessageSb.toString();
                } else if (retCodeConfig.getCurrentToperationSame() == result.getReturnCode()) {
                    /*-----------------------------------------------------------*/
                    /*   Get ProcessState of the lot for the following judgement */
                    /*-----------------------------------------------------------*/
                    String lotProcessStateRetCode = lotMethod.lotProcessStateGet(objCommon, qtimeActionReqIn.getLotID());
                    /*-----------------------------------------------------------*/
                    /*   Judge whether to call txFutureReworkReq                 */
                    /*-----------------------------------------------------------*/
                    if (CimStringUtils.equals(BizConstant.SP_LOT_PROCSTATE_PROCESSING, lotProcessStateRetCode)) {
                        bNeedAlertMessageRpt = true;
                        systemMsgCode = BizConstant.SP_SYSTEMMSGCODE_QRESTRICTIONSYSMSG;
                        StringBuffer systemMessageSb = new StringBuffer();
                        systemMessageSb.append("The Q-Time Over Lot has passed the operation of FutureHold action.\n")
                                .append("LotID         : ").append(qtimeActionReqIn.getLotID().getValue()).append("\n")
                                .append("RouteID       : ").append(qtimeActionReqIn.getRouteID().getValue()).append("\n")
                                .append("TriggerRouteID: ").append(actionRouteID.getValue()).append("\n")
                                .append("TriggerOpe.No : ").append(strQTimeAction.getQrestrictionTriggerOperationNumber()).append("\n")
                                .append("TargetOpe.No  : ").append(strQTimeAction.getQrestrictionTargetOperationNumber()).append("\n")
                                .append("TatgetTime    : ").append(strQTimeAction.getQrestrictionTargetTimeStamp()).append("\n");
                        systemMessage = systemMessageSb.toString();
                    } else {
                        /*-----------------------------------------------------------*/
                        /* lot is not passed the Operation of futurerework action.   */
                        /*-----------------------------------------------------------*/
                        bNeedToCallFutureRework = true;
                        bNeedToCallPostProcess = true;
                    }
                } else {
                    bNeedToCallFutureRework = true;
                }
                if (bNeedToCallFutureRework) {
                    //【TODO】【TODO - NOTIMPL】- txFutureReworkReq
                }
                /*--------------------------------------------------------------------------*/
                /* Action execution section for SP_QTimeRestriction_Action_Mail             */
                /*--------------------------------------------------------------------------*/
            } else if (CimStringUtils.equals(BizConstant.SP_QTIMERESTRICTION_ACTION_MAIL, qtimeActionReqIn.getStrQrestTimeAction().getQrestrictionAction())) {
                /*----------------------------------------------*/
                /* Create a message for Mail Action.            */
                /*----------------------------------------------*/
                StringBuffer messageSb = new StringBuffer();
                messageSb.append("This message was sent by Lot Q-Time over.\n")
                        .append("LotID         : ").append(qtimeActionReqIn.getLotID().getValue()).append("\n")
                        .append("TriggerOpe.No : ").append(strQTimeAction.getQrestrictionTriggerOperationNumber()).append("\n")
                        .append("TargetOpe.No  : ").append(strQTimeAction.getQrestrictionTargetOperationNumber()).append("\n")
                        .append("TatgetTime    : ").append(strQTimeAction.getQrestrictionTargetTimeStamp()).append("\n");
                /*----------------------------------------------*/
                /* Send the message                             */
                /*----------------------------------------------*/
                // MessageDistributionMgr_PutMessage
                messageMethod.messageDistributionMgrPutMessage(objCommon, strQTimeAction.getMessageID(), qtimeActionReqIn.getLotID(), "",
                        qtimeActionReqIn.getEquipmentID(), qtimeActionReqIn.getRouteID(), qtimeActionReqIn.getOperationNumber(),
                        strQTimeAction.getReasonCodeID().getValue(), messageSb.toString());
                bActionDoneFlag = true;
                /*--------------------------------------------------------------------------*/
                /* "else" section for not defined actions                                   */
                /*--------------------------------------------------------------------------*/
            } else {
                throw new ServiceException(retCodeConfig.getInvalidActionCode());
            }
        }
        /*------------------------------------------------*/
        /* Send System Message                            */
        /*------------------------------------------------*/
        if (bNeedAlertMessageRpt) {
            // txAlertMessageRpt
            Params.AlertMessageRptParams alertMessageRptParams = new Params.AlertMessageRptParams();
            alertMessageRptParams.setSubSystemID(BizConstant.SP_SUBSYSTEMID_MM);
            alertMessageRptParams.setSystemMessageCode(systemMsgCode);
            alertMessageRptParams.setSystemMessageText(systemMessage);
            alertMessageRptParams.setNotifyFlag(true);
            alertMessageRptParams.setEquipmentID(qtimeActionReqIn.getEquipmentID());
            alertMessageRptParams.setLotID(qtimeActionReqIn.getLotID());
            alertMessageRptParams.setLotStatus(qtimeActionReqIn.getLotStatus());
            alertMessageRptParams.setRouteID(qtimeActionReqIn.getRouteID());
            alertMessageRptParams.setOperationNumber(qtimeActionReqIn.getOperationNumber());
            alertMessageRptParams.setSystemMessageTimeStamp(CimDateUtils.convertToSpecString(objCommon.getTimeStamp().getReportTimeStamp()));
            systemService.sxAlertMessageRpt(objCommon, alertMessageRptParams);
        }
        /*------------------------------------------------*/
        /* Update the ActionDoneFlag of the Q-Time Action */
        /*------------------------------------------------*/
        if (CimBooleanUtils.isTrue(bActionDoneFlag)) {
            qTimeMethod.qtimeQrestTimeFlagMaint(objCommon, qtimeActionReqIn.getLotID(), qtimeActionReqIn.getRouteID(), qtimeActionReqIn.getOperationNumber(), strQTimeAction);
        }
        /*------------------------------------------------*/
        /* Set "1" to strPostProcessRequired variable     */
        /*------------------------------------------------*/
        if (CimBooleanUtils.isTrue(bNeedToCallPostProcess)) {
            result.setObject("1");
        } else {
            result.setObject("0");
        }
        result.setReturnCode(retCodeConfig.getSucc());
        return result;
    }

    @Override
    public void sxQtimerReq(Infos.ObjCommon objCommon, Params.QtimerReqParams params) {
        log.info("【Method Entry】sxQtimerReq()");

        //【Step0-1】Trace InParameters
        log.info("in-parm actionType = {}", params.getActionType());
        log.info("in-parm lotID = {}", params.getLotID().getValue());

        // 【Step0-2】In parameters check : Blank input check for in-parameter "lotID";
        Validations.check(ObjectIdentifier.isEmptyWithValue(params.getLotID()), retCodeConfigEx.getBlankInputParameter());

        //【Step1】Create, Update, or Delete must be specified as actionType
        if (!CimStringUtils.equals(params.getActionType(), BizConstant.SP_QRESTTIME_OPECATEGORY_CREATE)
                && !CimStringUtils.equals(params.getActionType(), BizConstant.SP_QRESTTIME_OPECATEGORY_UPDATE)
                && !CimStringUtils.equals(params.getActionType(), BizConstant.SP_QRESTTIME_OPECATEGORY_DELETE)) {
            log.info("Input parameter actionType is invalid");
            Validations.check(true, retCodeConfig.getInvalidInputParam());
        }

        int nQtimeListLen = CimArrayUtils.getSize(params.getQtimeInfoList());
        //【Step2】update and delete are limited for wafer level qtime;
        if (CimStringUtils.equals(params.getActionType(), BizConstant.SP_QRESTTIME_OPECATEGORY_UPDATE)
                || CimStringUtils.equals(params.getActionType(), BizConstant.SP_QRESTTIME_OPECATEGORY_DELETE)) {
            log.info("Action is Delete or Update");
            for (int qtimeIndex = 0; qtimeIndex < nQtimeListLen; qtimeIndex++) {
                log.info("Loop {} to nQtimeListLen", qtimeIndex);
                Infos.QrestTimeInfo strQtimeInfo = params.getQtimeInfoList().get(qtimeIndex);
                Validations.check(!ObjectIdentifier.isEmptyWithValue(strQtimeInfo.getWaferID()), retCodeConfig.getInvalidInputParam());
            }
        }

        //【Step3】wafer Level Qtime should be created with empty waferid.
        if (CimStringUtils.equals(BizConstant.SP_QRESTTIME_OPECATEGORY_CREATE, params.getActionType())) {
            log.info("Action is Create.");
            for (int qtimeIndex = 0; qtimeIndex < nQtimeListLen; qtimeIndex++) {
                log.info("Loop {} to nQtimeListLen", qtimeIndex);
                Infos.QrestTimeInfo strQtimeInfo = params.getQtimeInfoList().get(qtimeIndex);
                strQtimeInfo.setQTimeType(BizConstant.SP_QTIMETYPE_BYLOT);
                if (!ObjectIdentifier.isEmptyWithValue(strQtimeInfo.getWaferID())) {
                    log.info("Clear wafer ID");
                    strQtimeInfo.setWaferID(new ObjectIdentifier("", ""));
                }
            }
        }

        //【Step4】Lock objects to be updated;
        if (CimStringUtils.equals(params.getActionType(), BizConstant.SP_QRESTTIME_OPECATEGORY_UPDATE)
                || CimStringUtils.equals(params.getActionType(), BizConstant.SP_QRESTTIME_OPECATEGORY_DELETE)) {
            log.info("actionType is Update or Delete");
//            cimComp.objectLock(objCommon, params.getLotID(), BizConstant.SP_CLASSNAME_POSLOT);
            lockMethod.objectLock(objCommon, CimLot.class,params.getLotID());
        }

        // 【Step5】Check lot condition:Check lotState;
        String lotStateRetCode = lotMethod.lotStateGet(objCommon, params.getLotID());
        Validations.check(!CimStringUtils.equals(CIMStateConst.CIM_LOT_STATE_ACTIVE, lotStateRetCode), retCodeConfig.getInvalidLotStat());

        //【Step6】Perform qTime update；
        qTimeMethod.qTimeInfoUpdate(objCommon, params);
        log.info("【Method Exit】sxQtimerReq()");
    }

    @Override
    public void sxQtimeManageActionByPostTaskReq(Infos.ObjCommon strObjCommonIn, Params.QtimeManageActionByPostTaskReqInParm strQtimeManageActionByPostTaskReqInParm, String claimMemo){

        ObjectIdentifier lotID  = strQtimeManageActionByPostTaskReqInParm.getLotID();
        Validations.check(ObjectIdentifier.isEmptyWithValue(lotID), retCodeConfig.getInvalidInputParam());
        log.info(""+ "The input parameter lotID : "+ lotID.getValue());

        Infos.QtimeLotSetClearByOperationCompOut strQtimeLotSetClearByOperationCompOut;
        Infos.QtimeLotSetClearByOperationCompIn strQtimeLotSetClearByOperationCompIn=new Infos.QtimeLotSetClearByOperationCompIn();
        strQtimeLotSetClearByOperationCompIn.setLotID( lotID);

        // 当EDC check 失败后，会被Hold在当前站点，不会做 process move 操作，因此需要添加这个 Flag 的逻辑判断
        final boolean checkConditionForPO = lotMethod.lotCheckConditionForPO(strObjCommonIn, lotID);
        if (checkConditionForPO) {
            // 取当前PO
            strQtimeLotSetClearByOperationCompIn.setPreviousOperationFlag(false);
        } else {
            // 取前一个PO
            strQtimeLotSetClearByOperationCompIn.setPreviousOperationFlag(true);
        }

        strQtimeLotSetClearByOperationCompOut = qTimeMethod.qtimeLotSetClearByOperationComp(strObjCommonIn, strQtimeLotSetClearByOperationCompIn );

        ObjectIdentifier  resetReasonCodeID=new ObjectIdentifier();
        resetReasonCodeID.setValue (  SP_REASON_QTIMECLEAR );

        if( CimArrayUtils.getSize(strQtimeLotSetClearByOperationCompOut.getStrLotHoldReleaseList()) > 0 ){
            log.info(""+ "The lot hold actions to reset was found.");
            Params.HoldLotReleaseReqParams holdLotReleaseReqParams = new Params.HoldLotReleaseReqParams();
            holdLotReleaseReqParams.setLotID(lotID);
            holdLotReleaseReqParams.setReleaseReasonCodeID(resetReasonCodeID);
            holdLotReleaseReqParams.setHoldReqList(strQtimeLotSetClearByOperationCompOut.getStrLotHoldReleaseList());

            // Call txHoldLotReleaseReq
            lotService.sxHoldLotReleaseReq(strObjCommonIn, holdLotReleaseReqParams);
        }

        if( CimArrayUtils.getSize(strQtimeLotSetClearByOperationCompOut.getStrFutureHoldCancelList()) > 0 ){
            log.info(""+ "The future hold actions to cancel was found.");

            //call - txFutureHoldCancelReq
            processControlService.sxFutureHoldCancelReq(strObjCommonIn, lotID, resetReasonCodeID,
                    SP_ENTRYTYPE_CANCEL, strQtimeLotSetClearByOperationCompOut.getStrFutureHoldCancelList());
        }

        int  cancelLen = CimArrayUtils.getSize(strQtimeLotSetClearByOperationCompOut.getStrFutureReworkCancelList());
        if( cancelLen > 0 ){
            log.info(""+ "The future rework actions to cancel was found."+ cancelLen);

            for( int  cancelCnt = 0; cancelCnt < cancelLen; cancelCnt++ ){
                Infos.FutureReworkInfo strFutureRework = strQtimeLotSetClearByOperationCompOut.getStrFutureReworkCancelList().get(cancelCnt);

                // call txFutureReworkCancelReq
                processControlService.sxFutureReworkCancelReq( strObjCommonIn, strFutureRework.getLotID(), strFutureRework.getRouteID(),
                        strFutureRework.getOperationNumber(), strFutureRework.getFutureReworkDetailInfoList(), "" );
            }
        }

        log.info("txQtimeManageActionByPostTaskReq");
    }

    @Override
    public void sxFutureReworkCancelReq(
            Infos.ObjCommon strObjCommonIn,
            ObjectIdentifier lotID,
            ObjectIdentifier routeID,
            String operationNumber,
            List<Infos.FutureReworkDetailInfo> strFutureReworkDetailInfoSeq,
            String claimMemo) {
        log.info("txFutureReworkCancelReq");

        int paramCnt = 0;
        if (CimStringUtils.length(lotID.getValue()) > 0) {
            log.info("" + "The input parameter lotID          " + lotID.getValue());
            paramCnt++;
        }
        if (CimStringUtils.length(routeID.getValue()) > 0) {
            log.info("" + "The input parameter routeID        " + routeID.getValue());
            paramCnt++;
        }
        if (CimStringUtils.length(operationNumber) > 0) {
            log.info("" + "The input parameter operationNumber" + operationNumber);
            paramCnt++;
        }
        Validations.check (paramCnt < 3, retCodeConfig.getInvalidInputParam());

        Outputs.lotFutureReworkListGetDROut strLotFutureReworkListGetDROut;
        Params.FutureActionDetailInfoInqParams futureActionDetailInfoInqParams=new Params.FutureActionDetailInfoInqParams();
        futureActionDetailInfoInqParams.setLotID(lotID);
        Infos.OperationFutureActionAttributes operationFutureActionAttributes=new Infos.OperationFutureActionAttributes();
        operationFutureActionAttributes.setRouteID(routeID);
        operationFutureActionAttributes.setOperationNumber(operationNumber);
        futureActionDetailInfoInqParams.setOperationFutureActionAttributes(operationFutureActionAttributes);

        // call lotFutureReworkListGetDR
        strLotFutureReworkListGetDROut = lotMethod.lotFutureReworkListGetDR(strObjCommonIn, futureActionDetailInfoInqParams);
        if (CimArrayUtils.getSize(strLotFutureReworkListGetDROut.getFutureReworkDetailInfoList()) < 1) {
            log.info("" + "No future rework request is found.");
            Validations.check(retCodeConfigEx.getFtrwkNotFound());
        } else if (CimArrayUtils.getSize(strLotFutureReworkListGetDROut.getFutureReworkDetailInfoList()) > 1) {
            log.info("" + "More than two future rework requests are found. getThe() combination of key items is invalid.");
            Validations.check(retCodeConfig.getInvalidInputParam());
        } else {
            log.info("" + "One future rework request is found.");
        }

        String strLotInterFabXferStateGetOut;
        // call lotInterFabXferStateGet
        strLotInterFabXferStateGetOut = lotMethod.lotInterFabXferStateGet(
                strObjCommonIn,
                lotID);


        if (CimStringUtils.equals(strLotInterFabXferStateGetOut, BizConstant.SP_INTERFAB_XFERSTATE_TRANSFERRING)) {
            log.info("" + "interFabXferState == Transferring");
            throw new ServiceException(retCodeConfig.getInterfabInvalidLotXferstateForReq());
        }

        List<Infos.FutureReworkDetailInfo> strDefinedDetails = strLotFutureReworkListGetDROut.getFutureReworkDetailInfoList().get(0).getFutureReworkDetailInfoList();

        int definedDetailLen = CimArrayUtils.getSize(strDefinedDetails);
        int removeDetailLen = CimArrayUtils.getSize(strFutureReworkDetailInfoSeq);

        List<Infos.FutureReworkDetailInfo> strFutureReworkDetailInfoList = null;

        if (removeDetailLen < 1) {
            definedDetailLen = removeDetailLen;
        } else {
            if (definedDetailLen < removeDetailLen) {
                log.info("" + "The detail information more than it is defined is specified." + definedDetailLen + removeDetailLen);
                Validations.check(retCodeConfig.getInvalidInputParam());
            }

            strFutureReworkDetailInfoList=new ArrayList<>();

            int accordCnt = 0;
            for (int i = 0; i < removeDetailLen; i++) {
                for (int j = 0; j < definedDetailLen; j++) {
                    if (CimStringUtils.equals(strFutureReworkDetailInfoSeq.get(i).getTrigger(), strDefinedDetails.get(j).getTrigger())) {
                        log.info("" + "The trigger is found." + strFutureReworkDetailInfoSeq.get(i).getTrigger());

                        if (CimStringUtils.equals(strFutureReworkDetailInfoSeq.get(i).getReworkRouteID().getValue(), strDefinedDetails.get(j).getReworkRouteID().getValue()) &&
                                CimStringUtils.equals(strFutureReworkDetailInfoSeq.get(i).getReasonCodeID().getValue(), strDefinedDetails.get(j).getReasonCodeID().getValue())) {
                            log.info("" + "The rework route and the reason code are same with the defined one." +strDefinedDetails.get(j).getReworkRouteID().getValue() + strDefinedDetails.get(j).getReasonCodeID().getValue());

                            if (!CimStringUtils.equals(strFutureReworkDetailInfoSeq.get(i).getReturnOperationNumber(), strDefinedDetails.get(j).getReturnOperationNumber())) {
                                log.info("" + "The return operation number is not same with the defined one." +strFutureReworkDetailInfoSeq.get(i).getReturnOperationNumber() + strDefinedDetails.get(j).getReturnOperationNumber());

                                Validations.check(retCodeConfig.getFtRwkDataInvalid());
                            } else {
                                accordCnt++;
                                strDefinedDetails.get(j).setTrigger("");

                                break;
                            }
                        } else {
                            log.info("" + "The rework route and the reason code" + strFutureReworkDetailInfoSeq.get(i).getReworkRouteID().getValue() +strFutureReworkDetailInfoSeq.get(i).getReasonCodeID().getValue());
                            log.info("" + "A rework route and a reason code which are defined" + strDefinedDetails.get(j).getReworkRouteID().getValue() +strDefinedDetails.get(j).getReasonCodeID().getValue());
                        }
                    }
                }
            }

            Validations.check (accordCnt != removeDetailLen, retCodeConfig.getInvalidInputParam());

            int remainCnt = 0;
            for (int k = 0; k < definedDetailLen; k++) {
                if (CimStringUtils.length(strDefinedDetails.get(k).getTrigger()) > 0) {
                    log.info("" + "The detail information to be left is found." + remainCnt + strDefinedDetails.get(k).getTrigger() +strDefinedDetails.get(k).getReworkRouteID().getValue() + strDefinedDetails.get(k).getReasonCodeID().getValue());
                    strFutureReworkDetailInfoList.add(remainCnt++,strDefinedDetails.get(k));
                }
            }
        }

        if (definedDetailLen == removeDetailLen) {
            // call lotFutureReworkRequestDelete
            lotMethod.lotFutureReworkRequestDelete(strObjCommonIn, lotID, routeID, operationNumber);


            strFutureReworkDetailInfoList = strLotFutureReworkListGetDROut.getFutureReworkDetailInfoList().get(0).getFutureReworkDetailInfoList();
        } else {


            //call lotFutureReworkRequestRegist
            lotMethod.lotFutureReworkRequestRegist( strObjCommonIn, true,
                    lotID, routeID, operationNumber, strFutureReworkDetailInfoList);


            strFutureReworkDetailInfoList = strFutureReworkDetailInfoSeq;
        }

        RetCode<Object> strLotFutureReworkEventMakeOut;

        // call lotFutureReworkEventMake
        eventMethod.lotFutureReworkEventMake(strObjCommonIn, "OPRCW010", BizConstant.SP_FUTUREREWORK_ACTION_CANCEL,
                lotID, routeID, operationNumber, strFutureReworkDetailInfoList, claimMemo);

        log.info("txFutureReworkCancelReq");
    }

    @Override
    public List<Infos.ProcessHoldLot> sxProcessHoldCancelReq(Infos.ObjCommon objCommon, Params.ProcessHoldCancelReq param){
        List<Infos.ProcessHoldLot> processHoldLots = new ArrayList<>();
        //--- Check In-Parameter ---//
        Validations.check(CimStringUtils.isEmpty(param.getHoldType()), retCodeConfig.getInvalidInputParam());

        //step1 - processHoldRequests_DeleteEntry
        log.info("step1 - processHoldRequests_DeleteEntry");
        processMethod.processHoldRequestsDeleteEntry(objCommon, param);

        //step2 - processHoldEvent_Make
        //-----------------------------------------------------------//
        //   Create a ProcessHold registration history as cancel     //
        //-----------------------------------------------------------//
        log.info("step2 - processHoldEvent_Make");
        eventMethod.processHoldEventMake(objCommon, TransactionIDEnum.PROCESS_HOLD_CANCEL_REQ.getValue(), param.getRouteID(),
                param.getOperationNumber(), param.getProductID(), param.getWithExecHoldReleaseFlag(), param.getHoldType(), param.getReleaseReasonCodeID(),
                BizConstant.SP_ENTRYTYPE_CANCEL, param.getClaimMemo());

        //-------------------------------------------------------------------------------------------//
        //  If withExecHoldReleaseFlag is TRUE, call txHoldLotCancel                                 //
        //  This object method return released LotIDs to txBRRunScript().                            //
        //-------------------------------------------------------------------------------------------//
        if(CimBooleanUtils.isTrue(param.getWithExecHoldReleaseFlag())){
            //step3 - processHold_GetLotListForHoldReleaseDR
            log.info("step3 - processHold_GetLotListForHoldReleaseDR");
            List<ObjectIdentifier> lotIDs = processMethod.processHoldGetLotListForHoldReleaseDR(objCommon, param);
            if(CimArrayUtils.isNotEmpty(lotIDs)){
                for (ObjectIdentifier lotID : lotIDs) {
                    Infos.ProcessHoldLot processHoldLot = new Infos.ProcessHoldLot();
                    processHoldLot.setStrResult(new RetCode<>(null, retCodeConfig.getSucc(), null));
                    processHoldLot.setLotID(lotID);
                    processHoldLots.add(processHoldLot);
                }
            }
        }
        return processHoldLots;
    }

    @Override
    public List<Infos.ProcessHoldLot> sxProcessHoldReq(Infos.ObjCommon objCommon, Params.ProcessHoldReq param){
        // Check In-Parameter
        String holdType = param.getHoldType();
        Validations.check(CimStringUtils.isEmpty(holdType), retCodeConfig.getInvalidInputParam());
        List<Infos.ProcessHoldLot> lotIDList = new ArrayList<>();

        //-------------------------------------------------------
        //    Check RouteID and OperationNumber
        //-------------------------------------------------------
        Inputs.ProcessOperationListForRoute processOperationListForRoute = new Inputs.ProcessOperationListForRoute();
        processOperationListForRoute.setRouteID(param.getRouteID());
        processOperationListForRoute.setOperationID(new ObjectIdentifier(""));
        processOperationListForRoute.setOperationNumber(param.getOperationNumber());
        processOperationListForRoute.setPdType("");
        processOperationListForRoute.setSearchCount(1);
        //step1 - process_operationListForRoute__160
        log.info("step1 - process_operationListForRoute__160");
        try {
            List<Infos.OperationNameAttributes> processOperationListForRoutes = processMethod.processOperationListForRoute(objCommon, processOperationListForRoute);
        } catch (ServiceException e) {
            throw new ServiceException(retCodeConfig.getInvalidInputParam());
        }
        //Check registered Process Hold indication
        Infos.ProcessHoldSearchKey processHoldSearchKey = new Infos.ProcessHoldSearchKey();
        processHoldSearchKey.setRouteID(param.getRouteID());
        processHoldSearchKey.setOperationNumber(param.getOperationNumber());
        processHoldSearchKey.setHoldType(param.getHoldType());
        long count = 0;
        boolean countLimitter = false;
        //step2 - processHold_holdList_GetDR
        log.info("step2 - processHold_holdList_GetDR");
        List<Infos.ProcHoldListAttributes> procHoldListAttributes = null;
        try{
            procHoldListAttributes = processMethod.processHoldHoldListGetDR(objCommon, processHoldSearchKey, param.getReasonCodeID(), count, countLimitter, false);
        }catch (ServiceException ex){
            if (!Validations.isEquals(retCodeConfig.getNotFoundEntryW(), ex.getCode()) && !Validations.isEquals(retCodeConfig.getSomeRouteDataError(), ex.getCode())){
                throw ex;
            }
        }
        int holdListLen = CimArrayUtils.getSize(procHoldListAttributes);
        if(holdListLen > 0){
            //----------------------------------------------------//
            //    Check Dupulicate for Process Hold Entry         //
            //----------------------------------------------------//
            for (Infos.ProcHoldListAttributes procHoldListAttribute : procHoldListAttributes) {
                Validations.check(ObjectIdentifier.equalsWithValue(procHoldListAttribute.getProductID(), param.getProductID()), retCodeConfig.getDuplicateProcessHoldEntry());
            }
            //-----------------------------------------------------------------------//
            // < Check Case >                                                        //
            //                                                                       //
            // [CASE-A] productID == ""                                               //
            //  (1) Already registered&specified productID's ProcHold is canceled.   //
            //        ==>Call txProcessHoldCancelReq()                               //
            //           "withExecHoldReleaseFlag" is OFF                            //
            //  (2) Register new ProcessHoldEntry by "*".                            //
            //                                                                       //
            // [CASE-B] (productID == specified ) && Registered ProcessHold == "*"   //
            //  (1)  retrun RC_DUPLICATE_PRHOLD_ENTRY.                               //
            //-----------------------------------------------------------------------//
            if(ObjectIdentifier.isEmptyWithValue(param.getProductID())){
                for (Infos.ProcHoldListAttributes procHoldListAttribute : procHoldListAttributes) {
                    ObjectIdentifier releaseReasonCodeID = new ObjectIdentifier(BizConstant.SP_REASON_PROCESSHOLDCANCEL);
                    boolean withExecHoldReleaseFlag = false;
                    Params.ProcessHoldCancelReq processHoldCancelReq = new Params.ProcessHoldCancelReq();
                    processHoldCancelReq.setRouteID(procHoldListAttribute.getRouteID());
                    processHoldCancelReq.setOperationNumber(procHoldListAttribute.getOperationNumber());
                    processHoldCancelReq.setProductID(procHoldListAttribute.getProductID());
                    processHoldCancelReq.setHoldReasonCodeID(procHoldListAttribute.getReasonCodeID());
                    processHoldCancelReq.setReleaseReasonCodeID(releaseReasonCodeID);
                    processHoldCancelReq.setWithExecHoldReleaseFlag(withExecHoldReleaseFlag);
                    processHoldCancelReq.setHoldType(param.getHoldType());
                    processHoldCancelReq.setClaimMemo(param.getClaimMemo());
                    //step3 - txProcessHoldCancelReq
                    log.info("step3 - txProcessHoldCancelReq");
                    processControlService.sxProcessHoldCancelReq(objCommon, processHoldCancelReq);
                }
            } else {
                for (Infos.ProcHoldListAttributes procHoldListAttribute : procHoldListAttributes) {
                    Validations.check(ObjectIdentifier.isEmptyWithValue(procHoldListAttribute.getProductID()), retCodeConfig.getDuplicateProcessHoldEntry());
                }
            }
        }
        //step4 - processHoldRequests_MakeEntry
        log.info("step4 - processHoldRequests_MakeEntry");
        processMethod.processHoldRequestsMakeEntry(objCommon, param);

        //step5 - processHoldEvent_Make
        /*-----------------------------------------------------------*/
        /*   Create a ProcessHold registration history               */
        /*-----------------------------------------------------------*/
        log.info("step5 - processHoldEvent_Make");
        eventMethod.processHoldEventMake(objCommon, TransactionIDEnum.PROCESS_HOLD_REQ.getValue(), param.getRouteID(), param.getOperationNumber(),
                param.getProductID(), param.getWithExecHoldFlag(), param.getHoldType(), param.getReasonCodeID(), BizConstant.SP_ENTRYTYPE_ENTRY, param.getClaimMemo());

        //------------------------------------------------------------------------------//
        // If withExecHoldFlag is TRUE, execute ProcessHold for target LotIDs           //
        //------------------------------------------------------------------------------//
        if(CimBooleanUtils.isTrue(param.getWithExecHoldFlag())){
            //step6 - processHold_GetLotListForHoldDR
            log.info("step6 - processHold_GetLotListForHoldDR");
            List<ObjectIdentifier> lotIDs = processMethod.processHoldGetLotListForHoldDR(objCommon, param);
            if(CimArrayUtils.isNotEmpty(lotIDs)){
                for (ObjectIdentifier lotID : lotIDs) {
                    Infos.ProcessHoldLot processHoldLot = new Infos.ProcessHoldLot();
                    processHoldLot.setStrResult(new RetCode(null, retCodeConfig.getSucc(), null));
                    processHoldLot.setLotID(lotID);
                    lotIDList.add(processHoldLot);
                }
            }
        }
        return lotIDList;
    }

    @Override
    public void sxFutureReworkReq(Infos.ObjCommon objCommon, Params.FutureReworkReqParams params) {
        //------------------------------------------------------------
        // Trace and check input parameters
        //------------------------------------------------------------
        ObjectIdentifier lotID = params.getLotID();
        ObjectIdentifier routeID = params.getRouteID();
        String operationNumber = params.getOperationNumber();
        Infos.FutureReworkDetailInfo futureReworkDetailInfo = params.getFutureReworkDetailInfo();
        int paramCnt = 0;
        if (!ObjectIdentifier.isEmpty(lotID)) {
            log.info("The input parameter lotID {}", lotID);
            paramCnt++;
        }
        if (!ObjectIdentifier.isEmpty(routeID)) {
            log.info("The input parameter routeID {}", routeID);
            paramCnt++;
        }
        if (!CimObjectUtils.isEmpty(operationNumber)) {
            log.info("The input parameter operationNumber     {} ", operationNumber);
            paramCnt++;
        }
        if (!CimObjectUtils.isEmpty(futureReworkDetailInfo) && !CimObjectUtils.isEmpty(futureReworkDetailInfo.getTrigger())) {
            log.info("The input parameter trigger {} ", futureReworkDetailInfo.getTrigger());
            paramCnt++;
        }
        if (!CimObjectUtils.isEmpty(futureReworkDetailInfo) && !ObjectIdentifier.isEmpty(futureReworkDetailInfo.getReworkRouteID())) {
            log.info("The input parameter reworkRouteID       {} ", futureReworkDetailInfo.getReworkRouteID());
            paramCnt++;
        }
        if (!CimObjectUtils.isEmpty(futureReworkDetailInfo) && !CimObjectUtils.isEmpty(futureReworkDetailInfo.getReturnOperationNumber())) {
            log.info("The input parameter returnOperationNumber {}", futureReworkDetailInfo.getReturnOperationNumber());
        }
        if (!CimObjectUtils.isEmpty(futureReworkDetailInfo) && !ObjectIdentifier.isEmpty(futureReworkDetailInfo.getReasonCodeID())) {
            log.info("The input parameter reasonCodeID        {} ", futureReworkDetailInfo.getReasonCodeID());
            paramCnt++;
        }
        Validations.check(paramCnt < 6, retCodeConfig.getInvalidInputParam());


        //------------------------------------------------------------
        // Lock lot object
        //------------------------------------------------------------
        objectLockMethod.objectLock(objCommon, CimLot.class, lotID);

        //------------------------------------------------------------
        // Check for "routeID"
        //------------------------------------------------------------
        //===== Get Process Type of the target route =======//
        String processDefinitionType = processMethod.processExistenceCheck(objCommon, routeID, BizConstant.SP_PD_FLOWLEVEL_MAIN);
        //===== Verify that the type is not "Rework" =======//
        Validations.check(CimStringUtils.equals(processDefinitionType, BizConstant.SP_MAINPDTYPE_REWORK), retCodeConfig.getInvalidPDType(), processDefinitionType, routeID);


        Inputs.ObjProcessOperationProcessRefListForLotIn refListForLotIn = new Inputs.ObjProcessOperationProcessRefListForLotIn();
        refListForLotIn.setSearchDirection(true);
        refListForLotIn.setPosSearchFlag(false);
        refListForLotIn.setSearchCount(9999);
        refListForLotIn.setSearchRouteID(routeID);
        refListForLotIn.setSearchOperationNumber(operationNumber);
        refListForLotIn.setCurrentFlag(true);
        refListForLotIn.setLotID(lotID);
        List<Infos.OperationProcessRefListAttributes> strOperationAttributeList = processMethod.processOperationProcessRefListForLot(objCommon, refListForLotIn);
        int opeLen = CimArrayUtils.getSize(strOperationAttributeList);

        //----- The sequence number at the list of operation attribute, for the target operation and the rework operation -------//
        // The rework operation is the operation from which the target rework route is due to diverge.
        // And, the previous operation of the target operation is the return operation.
        int tgtOpe = 0;    // The target operation's sequence number at the list of operation attribute
        int rwkOpe = 1;    // The rework operation's sequence number at the list of operation attribute
        Infos.OperationProcessRefListAttributes strOperationNameAttributesForRwkOpe = new Infos.OperationProcessRefListAttributes();
        if (ObjectIdentifier.equalsWithValue(routeID, strOperationAttributeList.get(opeLen - 1).getRouteID()) &&
                CimStringUtils.equals(operationNumber, strOperationAttributeList.get(opeLen - 1).getOperationNumber())) {
            if (opeLen > 1) {
                log.info("The target operation is more forward to the target lot's current operation.");

                //===== Verify that the target route is same as the target lot's current route =======//
                if (!ObjectIdentifier.equalsWithValue(routeID, strOperationAttributeList.get(0).getRouteID())) {
                    log.info("The target route is not same as the target lot's current route.");
                    //===== Get previous process reference =======//
                    Outputs.objProcessPreviousProcessReferenceOut objProcessPreviousProcessReferenceOut = processMethod.processPreviousProcessReferenceGet(objCommon, strOperationAttributeList.get(opeLen - 1).getProcessRef());

                    strOperationNameAttributesForRwkOpe.setOperationNumber(objProcessPreviousProcessReferenceOut.getPreviousOperationNumber());
                    strOperationNameAttributesForRwkOpe.setProcessRef(objProcessPreviousProcessReferenceOut.getPreviousProcessRef());

                } else {
                    log.info("The target route is same as the target lot's current route.");
                    strOperationNameAttributesForRwkOpe = strOperationAttributeList.get(opeLen - 2);
                }


                //===== Reset the sequence number about target operation and the rework operation =======//
                tgtOpe = opeLen - 1;
                rwkOpe = opeLen - 2;
            } else {
                log.info("The target operation is same as the target lot's current operation.");

                //------------------------------------------------------------
                // Check for "lotID"
                //------------------------------------------------------------
                //===== Get the target lot's all state =======//
                Outputs.ObjLotAllStateGetOut objLotAllStateGetOut = lotMethod.lotAllStateGet(objCommon, lotID);

                //===== Verify that the target lot is active =======//
                Validations.check(!CimStringUtils.equals(objLotAllStateGetOut.getLotState(), CIMStateConst.CIM_LOT_STATE_ACTIVE), retCodeConfig.getInvalidLotStat(), lotID, objLotAllStateGetOut.getLotState());
                //===== Don't verify that the target lot is not being held =======//
                // If the target lot is being held, to execute the future rework request concerned fails.
                // But the registered requests have to be valid as post process task of releasing the target lot's hold record.
                // So, even if the target lot is held, to register a future rework request is allowed.
                //===== Verify that the target lot is not being processed =======//
                Validations.check(CimStringUtils.equals(objLotAllStateGetOut.getProcessState(), BizConstant.SP_LOT_PROCSTATE_PROCESSING), retCodeConfig.getInvalidLotProcstat(), lotID, objLotAllStateGetOut.getProcessState());
                //===== Verify that the target lot is at the floor or a non-pro bank =======//
                String inventoryState = objLotAllStateGetOut.getInventoryState();
                Validations.check(!CimStringUtils.equals(inventoryState, BizConstant.SP_LOT_INVENTORYSTATE_ONFLOOR) &&
                        !CimStringUtils.equals(inventoryState, BizConstant.SP_LOT_INVENTORYSTATE_NONPROBANK), retCodeConfig.getInvalidLotInventoryStat(), lotID, inventoryState);


                //===== Get the taret lot's control job =======//
                ObjectIdentifier controlJobID = lotMethod.lotControlJobIDGet(objCommon, lotID);

                //===== Verify that the target lot has no control job =======//
                Validations.check(!ObjectIdentifier.isEmpty(controlJobID), retCodeConfig.getLotControlJobidFilled(), lotID, controlJobID);

                //===== Search the rework operation and reset the list of operation attribute =======//
                refListForLotIn = new Inputs.ObjProcessOperationProcessRefListForLotIn();
                refListForLotIn.setSearchDirection(false);
                refListForLotIn.setPosSearchFlag(true);
                refListForLotIn.setSearchCount(2);
                refListForLotIn.setCurrentFlag(true);
                refListForLotIn.setLotID(lotID);
                strOperationAttributeList = processMethod.processOperationProcessRefListForLot(objCommon, refListForLotIn);
                strOperationNameAttributesForRwkOpe = strOperationAttributeList.get(rwkOpe);

            }
        } else {
            log.info("The target operation may be more backward to the target lot's current operation.");
            Validations.check(retCodeConfig.getCurrentToperationEarly());
        }
        log.info("The rework operation number {}", strOperationNameAttributesForRwkOpe.getOperationNumber());

        //------------------------------------------------------------
        // Check for "operationNumber"
        //------------------------------------------------------------
        //===== Get the target operation's flow batch setting =======//
        String tgtOpeModulePOS = strOperationAttributeList.get(tgtOpe).getProcessRef().getModulePOS();
        Outputs.ObjProcessFlowBatchDefinitionGetDROut objProcessFlowBatchDefinitionGetDROut = processMethod.processFlowBatchDefinitionGetDR(objCommon, tgtOpeModulePOS);

        //===== Verify that the target operation is not included by a flow batch section =======//
        Validations.check(!CimObjectUtils.isEmpty(objProcessFlowBatchDefinitionGetDROut.getFlowBatchControl().getName()) &&
                        CimBooleanUtils.isFalse(objProcessFlowBatchDefinitionGetDROut.getFlowBatchSection().getEntryOperationFlag()), retCodeConfig.getProcessInBatchSection(),
                strOperationAttributeList.get(tgtOpe).getRouteID(), strOperationAttributeList.get(tgtOpe).getOperationNumber());

        //===== Get the target operation's bonding flow section setting =======//
        Outputs.ObjProcessBondingFlowDefinitionGetDROut objProcessBondingFlowDefinitionGetDROut = processMethod.processBondingFlowDefinitionGetDR(objCommon, tgtOpeModulePOS);

        //===== Verify that the target operation is not included by a bonding flow section =======//
        Validations.check(!CimObjectUtils.isEmpty(objProcessBondingFlowDefinitionGetDROut.getFlowSectionControl().getFlowSectionID()) &&
                        CimBooleanUtils.isFalse(objProcessBondingFlowDefinitionGetDROut.getFlowSectionControl().getFlowSectionEntryFlag()), retCodeConfig.getProcessInBatchSection(),
                strOperationAttributeList.get(tgtOpe).getRouteID(), strOperationAttributeList.get(tgtOpe).getOperationNumber());

        //------------------------------------------------------------
        // Check for "trigger"
        //------------------------------------------------------------
        //===== Verify that the number of the target trigger's characters is 27 or more and is 256 or less =======//
        // The number of trigger's characters has to be 27 or more, to assure strncpy's result at lot_futureReworkInfo_Check().
        // And, 256 is the length of the column FRFTWRK_RWKRT.TRIGGER to which the trigger is set.
        int searchTriggerLen = futureReworkDetailInfo.getTrigger().length();
        Validations.check(searchTriggerLen < 27 || searchTriggerLen > 256, retCodeConfig.getFtRwkDataInvalid(), BizConstant.SP_FUTUREREWORK_ITEM_TRIGGER, futureReworkDetailInfo.getTrigger());

        //------------------------------------------------------------
        // Check for "reworkRouteID"
        //------------------------------------------------------------
        //P7000438 Add Start
        //===== Get the target rework route's active id =======//
        ObjectIdentifier reworkRouteID = processMethod.processActiveIDGet(objCommon, futureReworkDetailInfo.getReworkRouteID());
        //===== Get the target lot's original routes =======//
        Outputs.ObjLotOriginalRouteListGetOut objLotOriginalRouteListGetOut = lotMethod.lotOriginalRouteListGet(objCommon, lotID);

        //===== Verify that the target rework route is not same as the original routes =======//
        List<ObjectIdentifier> originalRouteIDs = objLotOriginalRouteListGetOut.getOriginalRouteID();
        for (ObjectIdentifier originalRouteID : originalRouteIDs) {
            Validations.check(ObjectIdentifier.equalsWithValue(reworkRouteID, originalRouteID), retCodeConfig.getFtRwkDataInvalid(), BizConstant.SP_FUTUREREWORK_ITEM_REWORKROUTEID, futureReworkDetailInfo.getReworkRouteID());
        }

        //------------------------------------------------------------
        // Check for "reworkRouteID" and "returnOperationNumber"
        //------------------------------------------------------------
        //===== Get the list of the rework routes which are connected to the rework operation =======//
        String mainPF = strOperationNameAttributesForRwkOpe.getProcessRef().getProcessFlow();
        String modulePOS = strOperationNameAttributesForRwkOpe.getProcessRef().getModulePOS();

        List<Infos.ConnectedRoute> connectedRoutes = processMethod.processConnectedRouteGetDR(objCommon, mainPF, strOperationNameAttributesForRwkOpe.getOperationNumber(), modulePOS, false, true);
        int routeLen = CimArrayUtils.getSize(connectedRoutes);
        //===== Verify that the target rework route diverge from the rework operation =======//
        int routeCnt;
        for (routeCnt = 0; routeCnt < routeLen; routeCnt++) {
            if (ObjectIdentifier.equalsWithValue(reworkRouteID, connectedRoutes.get(routeCnt).getRouteID())) {
                log.info("The target rework route diverge from the rework operation. {} {}", routeCnt, routeLen);
                break;
            }
        }

        if (routeCnt == routeLen) {
            log.info("The target rework route don't diverge from the rework operation. {}", routeLen);
            Validations.check(true, retCodeConfig.getFtRwkDataInvalid(), BizConstant.SP_FUTUREREWORK_ITEM_REWORKROUTEID, futureReworkDetailInfo.getReworkRouteID());
        } else {
            //===== Verify that the target return operation is same as the target rework route's =======//
            String returnOperationNumber = futureReworkDetailInfo.getReturnOperationNumber();
            Validations.check(!CimObjectUtils.isEmpty(returnOperationNumber) &&
                    !CimStringUtils.equals(returnOperationNumber,
                            connectedRoutes.get(routeCnt).getReturnOperationNumber()), retCodeConfig.getFtRwkDataInvalid(), BizConstant.SP_FUTUREREWORK_ITEM_RETURNOPENO, returnOperationNumber);
        }

        //===== Set the target rework route's active one, and its return operation =======//
        //===== Set the target rework route's return operation =======//
        futureReworkDetailInfo.setReturnOperationNumber(connectedRoutes.get(routeCnt).getReturnOperationNumber());
        log.info("The target future rework request is registered using the return operation of the active rework route. {} {}",
                connectedRoutes.get(routeCnt).getRouteID(), connectedRoutes.get(routeCnt).getReturnOperationNumber());


        //------------------------------------------------------------
        // Check for "returnOperationNumber"
        //------------------------------------------------------------
        //===== Verify that the target return operation doesn't relate to any future hold requests =======//
        Infos.ProcessRef operationReference = strOperationAttributeList.get(tgtOpe).getProcessRef();
        processMethod.processFutureHoldRequestsCheck(objCommon, lotID, routeID, operationNumber, operationReference, futureReworkDetailInfo.getReturnOperationNumber());

        //===== Verify that the target return operation doesn't relate to any schedule change reservations =======//
        // This condition means that the target operation is same as the target lot's current operation.
        if (opeLen == 1) {
            scheduleChangeReservationMethod.schdlChangeReservationCheckForFutureOperation(objCommon, lotID, ObjectIdentifier.fetchValue(routeID), futureReworkDetailInfo.getReturnOperationNumber());
        }

        //------------------------------------------------------------
        // Check for "reasonCodeID"
        //------------------------------------------------------------
        //===== Verify that the target reason code exists =======//
        List<ObjectIdentifier> codeDataIDs = new ArrayList<>();
        codeDataIDs.add(futureReworkDetailInfo.getReasonCodeID());
        codeMethod.codeCheckExistanceDR(objCommon, BizConstant.SP_REASONCAT_REWORK, codeDataIDs);

        //------------------------------------------------------------
        // Check for the rework operation
        //------------------------------------------------------------
        //===== Verify that the rework count and the process count for the rework operation doesn't exceed those limit =======//
        processMethod.processMaxCountCheck(objCommon, lotID, routeID, strOperationNameAttributesForRwkOpe.getOperationNumber(), operationReference);

        //------------------------------------------------------------
        // Check for the target future rework request's consistency
        //------------------------------------------------------------
        Outputs.ObjLotFutureReworkInfoCheckOut objLotFutureReworkInfoCheckOut = null;
        OmCode checkedResult = retCodeConfig.getSucc();
        try {
            objLotFutureReworkInfoCheckOut = lotMethod.lotFutureReworkInfoCheck(objCommon, lotID, routeID, operationNumber, futureReworkDetailInfo);
        } catch (ServiceException e) {
            checkedResult = new OmCode(e.getCode(), e.getMessage());
            objLotFutureReworkInfoCheckOut = (Outputs.ObjLotFutureReworkInfoCheckOut) e.getData();
            if (!Validations.isEquals(retCodeConfigEx.getFtrwkDuplicate(), e.getCode()) && !Validations.isEquals(retCodeConfigEx.getFtrwkReadded(), e.getCode())
                    && !Validations.isEquals(retCodeConfigEx.getFtrwkUpdate(), e.getCode())) {
                if (Validations.isEquals(retCodeConfigEx.getFtrwkAlreadyExist(), e.getCode())) {
                    log.info("The future rework request already exists.");
                } else {
                    log.info("lot_futureReworkInfo_Check() != RC_OK");
                }
                throw e;
            }
        }
        assert objLotFutureReworkInfoCheckOut != null;
        List<Infos.FutureReworkDetailInfo> strFutureReworkDetailList = objLotFutureReworkInfoCheckOut.getFutureReworkDetailInfos();

        //-------------------------------
        // Check Lot interFabXferState
        //-------------------------------
        String interFabXferState = lotMethod.lotInterFabXferStateGet(objCommon, lotID);
        Validations.check(CimStringUtils.equals(interFabXferState, BizConstant.SP_INTERFAB_XFERSTATE_TRANSFERRING)
                || CimStringUtils.equals(interFabXferState, BizConstant.SP_INTERFAB_XFERSTATE_REQUIRED), retCodeConfig.getInterfabInvalidLotXferstateForReq(), lotID, interFabXferState);

        //------------------------------------
        // Check interFabXferPlan existence
        //------------------------------------
        Inputs.ObjProcessCheckInterFabXferPlanSkipIn skipIn = new Inputs.ObjProcessCheckInterFabXferPlanSkipIn();
        skipIn.setLotID(lotID);
        skipIn.setCurrentRouteID(routeID);
        skipIn.setCurrentOpeNo(operationNumber);
        skipIn.setJumpingRouteID(routeID);
        skipIn.setJumpingOpeNo(futureReworkDetailInfo.getReturnOperationNumber());
        try {
            processMethod.processCheckInterFabXferPlanSkip(objCommon, skipIn);
        } catch (ServiceException e) {
            // If interFabXfer plan exist between current operation and return point by reworking, Mail send to the lot owner.
            if (Validations.isEquals(retCodeConfig.getInterfabProcessSkipError(), e.getCode())) {
                messageMethod.messageDistributionMgrPutMessage(objCommon, new ObjectIdentifier(BizConstant.SP_SYSTEMMSGCODE_FUTUREREWORKREGISTERROR),
                        lotID, "", null, routeID, operationNumber, "****", "");
            } else {
                throw e;
            }
        }

        //------------------------------------------------------------
        // Add or update future rework request
        //------------------------------------------------------------
        lotMethod.lotFutureReworkRequestRegist(objCommon, objLotFutureReworkInfoCheckOut.isUpdateFlag(), lotID, routeID, operationNumber, strFutureReworkDetailList);

        //------------------------------------------------------------
        // Make event for future rework request
        //------------------------------------------------------------
        String action = Validations.isEquals(retCodeConfigEx.getFtrwkUpdate(), checkedResult) ? BizConstant.SP_FUTUREREWORK_ACTION_UPDATE : BizConstant.SP_FUTUREREWORK_ACTION_ENTRY;

        strFutureReworkDetailList.clear();
        strFutureReworkDetailList.add(futureReworkDetailInfo);
        eventMethod.lotFutureReworkEventMake(objCommon, TransactionIDEnum.FUTURE_REWORK_REQ.getValue(), action, lotID, routeID, operationNumber, strFutureReworkDetailList, params.getClaimMemo());

        Validations.check(!Validations.isEquals(retCodeConfig.getSucc(), checkedResult), checkedResult);
    }

    @Override
    public void sxFutureReworkActionDoReq(Infos.ObjCommon objCommon, ObjectIdentifier lotID, String claimMemo) {
        //--------------------------------------------------
        // Trace and check input parameter
        //--------------------------------------------------
        if (!ObjectIdentifier.isEmpty(lotID)) {
            log.info("The input parameter lotID {}", lotID);
        } else {
            log.info("The input parameter lotID is not specified.");
            Validations.check(retCodeConfig.getInvalidParameter());
        }


        //-----------------------------------
        // Get lot's all state
        //-----------------------------------
        Outputs.ObjLotAllStateGetOut objLotAllStateGetOut = lotMethod.lotAllStateGet(objCommon, lotID);

        //-------------------------------------------------------
        // Check lot's condition
        // If the conditions are as follows, RC_OK is returned.
        //   holdState       is     OnHold
        //   lotState        is not Active
        //   processState    is     Processing
        //   productionState is     InRework
        //   inventoryState  is not OnFloor
        //-------------------------------------------------------
        if (CimStringUtils.equals(objLotAllStateGetOut.getHoldState(), BizConstant.CIMFW_LOT_HOLDSTATE_ONHOLD)) {
            log.info("lot_allState_Get() objLotAllStateGetOut.holdState == CIMFW_Lot_HoldState_OnHold");
            return;
        }
        if (!CimStringUtils.equals(objLotAllStateGetOut.getLotState(), BizConstant.CIMFW_LOT_STATE_ACTIVE)) {
            log.info("lot_allState_Get() objLotAllStateGetOut.lotState != CIMFW_Lot_State_Active");
            return;
        }
        if (CimStringUtils.equals(objLotAllStateGetOut.getProcessState(), BizConstant.SP_LOT_PROCSTATE_PROCESSING)) {
            log.info("lot_allState_Get() objLotAllStateGetOut.processState == SP_Lot_ProcState_Processing");
            return;
        }
        if (CimStringUtils.equals(objLotAllStateGetOut.getProductionState(), BizConstant.SP_LOT_PRODCTN_STATE_INREWORK)) {
            log.info("lot_allState_Get() objLotAllStateGetOut.productionState == CIMFW_Lot_ProductionState_InRework");
            return;
        }
        if (!CimStringUtils.equals(objLotAllStateGetOut.getInventoryState(), BizConstant.SP_LOT_INVENTORYSTATE_ONFLOOR)) {
            log.info("lot_allState_Get() objLotAllStateGetOut.inventoryState != SP_Lot_InventoryState_OnFloor");
            return;
        }

        //--------------------------------------------------
        // Get lot's route and operation number
        //--------------------------------------------------
        Outputs.ObjLotCurrentOperationInfoGetOut objLotCurrentOperationInfoGetOut = lotMethod.lotCurrentOperationInfoGet(objCommon, lotID);
        ObjectIdentifier routeID = objLotCurrentOperationInfoGetOut.getRouteID();
        String operationNumber = objLotCurrentOperationInfoGetOut.getOperationNumber();
        log.info("Lot's currentRouteID         : {}", routeID);
        log.info("Lot's currentOperationNumber : {}", operationNumber);

        //-----------------------------------------------------------------
        // Get and Check lot's future rework request at current operation
        //-----------------------------------------------------------------
        List<Infos.FutureReworkInfo> futureReworkInfos = lotMethod.lotFutureReworkListGetDR(objCommon, lotID, routeID, operationNumber);
        if (CimArrayUtils.getSize(futureReworkInfos) < 1) {
            log.info("No future rework request is found.");
            Validations.check(retCodeConfigEx.getFtrwkNotFound());
        } else if (CimArrayUtils.getSize(futureReworkInfos) > 1) {
            log.info("More than two future rework requests are found. The combination of key items is invalid.");
            Validations.check(retCodeConfig.getInvalidInputParam());
        } else {
            log.info("One future rework request is found.");
            List<Infos.FutureReworkDetailInfo> futureReworkDetailInfoList = futureReworkInfos.get(0).getFutureReworkDetailInfoList();
            if (CimArrayUtils.getSize(futureReworkDetailInfoList) < 1) {
                log.info("No future rework detail information is found.");
                Validations.check(retCodeConfigEx.getFtrwkNotFound());
            } else if (CimArrayUtils.getSize(futureReworkDetailInfoList) > 1) {
                Infos.FutureReworkDetailInfo futureReworkDetailInfo0 = futureReworkDetailInfoList.get(0);
                log.info("More than two future rework detail information are found.");
                for (Infos.FutureReworkDetailInfo futureReworkDetailInfo : futureReworkDetailInfoList) {
                    if ((!ObjectIdentifier.equalsWithValue(futureReworkDetailInfo0.getReworkRouteID(), futureReworkDetailInfo.getReworkRouteID())) ||
                            (!CimStringUtils.equals(futureReworkDetailInfo0.getReturnOperationNumber(), futureReworkDetailInfo.getReturnOperationNumber())) ||
                            (!ObjectIdentifier.equalsWithValue(futureReworkDetailInfo0.getReasonCodeID(), futureReworkDetailInfo.getReasonCodeID()))) {
                        log.info("The future rework detail information are duplicated.");
                        Validations.check(retCodeConfigEx.getFtrwkDuplicate());
                    }
                }

                log.info("The future rework detail information are not duplicated.");
            } else {
                log.info("One future rework detail information is found.");
            }

            //--------------------------------------------------
            // Execute lot's future rework request
            //--------------------------------------------------
            Infos.ReworkReq reworkReq = new Infos.ReworkReq();
            reworkReq.setLotID(lotID);
            reworkReq.setCurrentRouteID(routeID);
            reworkReq.setCurrentOperationNumber(operationNumber);
            reworkReq.setSubRouteID(futureReworkDetailInfoList.get(0).getReworkRouteID());
            reworkReq.setReturnOperationNumber(futureReworkDetailInfoList.get(0).getReturnOperationNumber());
            reworkReq.setReasonCodeID(futureReworkDetailInfoList.get(0).getReasonCodeID());
            reworkReq.setForceReworkFlag(false);
            reworkReq.setDynamicRouteFlag(false);
            lotService.sxReworkReq(objCommon, reworkReq, claimMemo);

            //--------------------------------------------------
            // Make event for future rework request
            //--------------------------------------------------
            eventMethod.lotFutureReworkEventMake(objCommon, TransactionIDEnum.FUTURE_REWORK_CANCEL_REQ.getValue(), BizConstant.SP_FUTUREREWORK_ACTION_CANCEL,
                    lotID, routeID, operationNumber, futureReworkDetailInfoList, claimMemo);

            //--------------------------------------------------
            // Delete lot's future rework request
            //--------------------------------------------------
            lotMethod.lotFutureReworkRequestDelete(objCommon, lotID, routeID, operationNumber);
        }
    }

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon   -
     * @param createLotID -
     * @author Jerry / NYX
     * @since 2018/5/7
     */
    public void sxProcessHoldDoActionReq(Infos.ObjCommon objCommon, ObjectIdentifier createLotID, String claimMemo) {

        String processStateGet = lotMethod.lotProcessStateGet(objCommon, createLotID);
        if (CimStringUtils.equals(BizConstant.SP_LOT_PROCSTATE_PROCESSING, processStateGet)) {
            log.info("Skip registering process hold when lot status is Processing");
        }

        //step 1 - If the lot has ProcessHold indication on operationNumber/RouteID/ProductID, call txLotHoldReq() by registered ProcessHold indication
        Outputs.ObjLotCurrentOperationInfoGetOut lotCurrentOperationInfoGetOut = lotMethod.lotCurrentOperationInfoGet(objCommon, createLotID);

        Outputs.ObjLotProductIDGetOut lotProductIDGetOut = lotMethod.lotProductIDGet(objCommon, createLotID);

        //step 2 - Get UserID & ReasonCode for ProcessHold
        Infos.ProcessHoldSearchKey processHoldSearchKey = new Infos.ProcessHoldSearchKey();
        ObjectIdentifier reasonCodeID = null;
        processHoldSearchKey.setOperationNumber(lotCurrentOperationInfoGetOut.getOperationNumber());
        //processHoldSearchKey.setProductID(productID);
        processHoldSearchKey.setRouteID(lotCurrentOperationInfoGetOut.getRouteID());
        List<Infos.ProcHoldListAttributes> procHoldListAttributes;
        try {
            procHoldListAttributes = processMethod.processHoldHoldListGetDR(objCommon, processHoldSearchKey, reasonCodeID, 0L, false, false);
        } catch (ServiceException e) {
            if (Validations.isEquals(retCodeConfig.getNotFoundEntryW(), e.getCode())) {
                log.info("no Process Hold to execute");
                return;
            } else {
                throw e;
            }
        }

        //step3 - Call txHoldLotReq() : execute ProcessHold
        if (CimArrayUtils.isNotEmpty(procHoldListAttributes)) {
            for (int i = 0; i < CimArrayUtils.getSize(procHoldListAttributes); i++) {
                if (0 == CimStringUtils.length(procHoldListAttributes.get(i).getProductID().getValue())
                        || ObjectIdentifier.equalsWithValue(lotProductIDGetOut.getProductID(), procHoldListAttributes.get(i).getProductID())) {
                    List<Infos.LotHoldReq> lotHoldReqList = new ArrayList<>();
                    Infos.LotHoldReq lotHoldReq = new Infos.LotHoldReq();
                    lotHoldReq.setHoldType(BizConstant.SP_HOLDTYPE_PROCESSHOLD);
                    lotHoldReq.setHoldReasonCodeID(procHoldListAttributes.get(i).getReasonCodeID());
                    lotHoldReq.setHoldUserID(procHoldListAttributes.get(i).getUserID());
                    lotHoldReq.setResponsibleOperationMark(BizConstant.SP_RESPONSIBLEOPERATION_CURRENT);
                    lotHoldReq.setRouteID(lotCurrentOperationInfoGetOut.getRouteID());
                    lotHoldReq.setOperationNumber(lotCurrentOperationInfoGetOut.getOperationNumber());
                    lotHoldReq.setClaimMemo(procHoldListAttributes.get(i).getClaimMemo());
                    lotHoldReq.setDepartment(procHoldListAttributes.get(i).getDepartment());
                    lotHoldReq.setSection(procHoldListAttributes.get(i).getSection());
                    lotHoldReqList.add(lotHoldReq);

                    lotService.sxHoldLotReq(objCommon, createLotID, lotHoldReqList);
                }
            }
        }
    }

    @Override
    public void sxFutureHoldDepartmentChangeReq(Infos.ObjCommon objCommon, Params.FutureHoldReqParams params) {
        // 【step1】 check params
        ObjectIdentifier lotID = params.getLotID();
        String holdType = params.getHoldType();
        ObjectIdentifier reasonCodeID = params.getReasonCodeID();
        ObjectIdentifier routeID = params.getRouteID();
        String operationNumber = params.getOperationNumber();
        Validations.check(ObjectIdentifier.isEmpty(lotID) || CimObjectUtils.isEmpty(holdType) || ObjectIdentifier.isEmpty(reasonCodeID)
                || ObjectIdentifier.isEmpty(routeID) || CimStringUtils.isEmpty(operationNumber), retCodeConfig.getInvalidParameter());

        // 【step2】 -  Lock objects to be updated [object_Lock]
        objectLockMethod.objectLock(objCommon, CimLot.class, lotID);

        // 【step3】 -Check lot interFabXferState [lot_interFabXferState_Get]
        log.info("[FutureHoldReq - step2] : Get and Check lotState (must not be Finished)");
        String interFabXferState = lotMethod.lotStateGet(objCommon, lotID);
        Validations.check(CimStringUtils.equals(BizConstant.CIMFW_LOT_STATE_FINISHED, interFabXferState), retCodeConfig.getInvalidLotStat());

        /*-----------------------------------------------------------*/
        /*  【step4】 Check PosCode                                  */
        /*-----------------------------------------------------------*/
        log.info("[FutureHoldReq - step3] : Check PosCode");
        List<ObjectIdentifier> codeDataIDs = new ArrayList<>();
        codeDataIDs.add(reasonCodeID);
        try{
            codeComp.codeCheckExistanceDR(objCommon, CIMStateConst.CIM_LOT_HOLD_TYPE_FUTUREHOLD, codeDataIDs);
        }catch (ServiceException e) {
            String transactionID = objCommon.getTransactionID();
            if(transactionID.equals("OPRCW001") || transactionID.equals("TXPCC029")){
                throw e;
            }else {
                codeComp.codeCheckExistanceDR(objCommon, BizConstant.SP_REASONCAT_LOTHOLD, codeDataIDs);
            }
        }
        //-----------------------------------------------------------
        // Check lot interFabXferState
        //-----------------------------------------------------------
        String strLotInterFabXferState = lotMethod.lotInterFabXferStateGet(objCommon, lotID);
        //-----------------------------------------------------------
        // "Transferring"
        //-----------------------------------------------------------
        Validations.check(CimStringUtils.equals(strLotInterFabXferState, BizConstant.SP_INTERFAB_XFERSTATE_TRANSFERRING), retCodeConfig.getInterfabInvalidLotXferstateForReq());

        // 【step5】 - Create a list into PosProcessOperation of a lot
        log.info("[FutureHoldReq - step5] :future hold change");
        Infos.FutureHoldHistory entryOut = lotMethod.lotFutureHoldChangeMakeEntry(objCommon, params);


        // 【step6】 Call lotFutureHoldEvent_Make [lotFutureHoldEvent_Make]
        log.info("Call lotFutureHoldEvent_Make");
        Inputs.LotFutureHoldEventMakeParams lotFutureHoldEventMakeParams = new Inputs.LotFutureHoldEventMakeParams();
        lotFutureHoldEventMakeParams.setTransactionID(TransactionIDEnum.ENHANCED_FUTURE_HOLD_REQ.getValue());
        lotFutureHoldEventMakeParams.setLotID(params.getLotID());
        lotFutureHoldEventMakeParams.setEntryType(BizConstant.SP_ENTRYTYPE_ENTRY);
        lotFutureHoldEventMakeParams.setFutureHoldHistory(entryOut);
        lotFutureHoldEventMakeParams.setClaimMemo(params.getClaimMemo());
        lotFutureHoldEventMakeParams.setReleaseReasonCode(new ObjectIdentifier());
        eventMethod.lotFutureHoldEventMake(objCommon, lotFutureHoldEventMakeParams);

    }

    @Override
    public void sxProcessHoldDepartmentChangeReq(Infos.ObjCommon objCommon, Params.ProcessHoldReq params) {
        // Check In-Parameter
        String holdType = params.getHoldType();
        Validations.check(CimStringUtils.isEmpty(holdType), retCodeConfig.getInvalidInputParam());

        //-------------------------------------------------------
        //    Check RouteID and OperationNumber
        //-------------------------------------------------------
        Inputs.ProcessOperationListForRoute processOperationListForRoute = new Inputs.ProcessOperationListForRoute();
        processOperationListForRoute.setRouteID(params.getRouteID());
        processOperationListForRoute.setOperationID(new ObjectIdentifier(""));
        processOperationListForRoute.setOperationNumber(params.getOperationNumber());
        processOperationListForRoute.setPdType("");
        processOperationListForRoute.setSearchCount(1);
        //step1 - process_operationListForRoute__160
        log.info("step1 - process_operationListForRoute__160");
        try {
            List<Infos.OperationNameAttributes> processOperationListForRoutes = processMethod.processOperationListForRoute(objCommon, processOperationListForRoute);
        } catch (ServiceException e) {
            throw new ServiceException(retCodeConfig.getInvalidInputParam());
        }
        //Check registered Process Hold indication
        Infos.ProcessHoldSearchKey processHoldSearchKey = new Infos.ProcessHoldSearchKey();
        processHoldSearchKey.setRouteID(params.getRouteID());
        processHoldSearchKey.setOperationNumber(params.getOperationNumber());
        processHoldSearchKey.setHoldType(params.getHoldType());
        long count = 0;
        boolean countLimitter = false;
        //step2 - processHold_holdList_GetDR
        log.info("step2 - processHold_holdList_GetDR");
        List<Infos.ProcHoldListAttributes> procHoldListAttributes = Lists.newArrayList();
        try {
            procHoldListAttributes = processMethod.processHoldHoldListGetDR(objCommon, processHoldSearchKey, params.getReasonCodeID(), count, countLimitter, false);
        } catch (ServiceException ex) {
            if (!Validations.isEquals(retCodeConfig.getNotFoundEntryW(), ex.getCode()) && !Validations.isEquals(retCodeConfig.getSomeRouteDataError(), ex.getCode())) {
                throw ex;
            }
        }
        objectLockMethod.objectLock(objCommon, CimProcessDefinition.class, params.getRouteID());
        int holdListLen = CimArrayUtils.getSize(procHoldListAttributes);
        if (holdListLen > 0) {
            //-----------------------------------------------------------------------//
            // < Check Case >                                                        //
            //                                                                       //
            // [CASE-A] productID == ""                                               //
            //  (1) Already registered&specified productID's ProcHold is canceled.   //
            //        ==>Call txProcessHoldCancelReq()                               //
            //           "withExecHoldReleaseFlag" is OFF                            //
            //  (2) Register new ProcessHoldEntry by "*".                            //
            //                                                                       //
            // [CASE-B] (productID == specified ) && Registered ProcessHold == "*"   //
            //  (1)  retrun RC_DUPLICATE_PRHOLD_ENTRY.                               //
            //-----------------------------------------------------------------------//
            if (ObjectIdentifier.isEmptyWithValue(params.getProductID())) {
                for (Infos.ProcHoldListAttributes procHoldListAttribute : procHoldListAttributes) {
                    ObjectIdentifier releaseReasonCodeID = new ObjectIdentifier(BizConstant.SP_REASON_PROCESSHOLDCANCEL);
                    Params.ProcessHoldCancelReq processHoldCancelReq = new Params.ProcessHoldCancelReq();
                    processHoldCancelReq.setRouteID(procHoldListAttribute.getRouteID());
                    processHoldCancelReq.setOperationNumber(procHoldListAttribute.getOperationNumber());
                    processHoldCancelReq.setProductID(procHoldListAttribute.getProductID());
                    processHoldCancelReq.setHoldReasonCodeID(procHoldListAttribute.getReasonCodeID());
                    processHoldCancelReq.setReleaseReasonCodeID(releaseReasonCodeID);
                    processHoldCancelReq.setWithExecHoldReleaseFlag(false);
                    processHoldCancelReq.setHoldType(params.getHoldType());
                    processHoldCancelReq.setClaimMemo(params.getClaimMemo());
                    //step3 - txProcessHoldCancelReq
                    log.info("step3 - txProcessHoldCancelReq");
                    processControlService.sxProcessHoldCancelReq(objCommon, processHoldCancelReq);
                }
            } else {
                for (Infos.ProcHoldListAttributes procHoldListAttribute : procHoldListAttributes) {
                    Validations.check(ObjectIdentifier.isEmptyWithValue(procHoldListAttribute.getProductID()), retCodeConfig.getDuplicateProcessHoldEntry());
                }
            }
        }

        //step4 - processHoldRequests_edit Entry
        log.info("step4 - processHoldRequests_MakeEntry");
        processMethod.processHoldRequestsChangeEntry(objCommon, params);

        //step5 - update lot reason code and department and section
        log.info("step5 - process hold processHoldGetLotListForHoldDR ");
        List<ObjectIdentifier> lotIDs = processMethod.processHoldGetLotListForHoldDR(objCommon, params);
        lotIDs.forEach(lotId -> objectLockMethod.objectLock(objCommon, CimLot.class, lotId));

        lotIDs.parallelStream().forEach(lotId -> {
            List<Infos.LotHoldListAttributes> lotHoldListAttributesList = null;
            try {
                lotHoldListAttributesList = lotMethod.lotFillInTxTRQ005DR(objCommon, lotId);
            } catch (ServiceException e) {
                log.info("process hold change , hold error: {}", e.getMessage());
            }
            if (!CollectionUtils.isEmpty(lotHoldListAttributesList)) {
                List<Infos.LotHoldReq> holdReqList = lotHoldListAttributesList.parallelStream()
                        .filter(lotHoldListAttributes ->
                                CimStringUtils.equals(BizConstant.SP_HOLDTYPE_PROCESSHOLD, lotHoldListAttributes.getHoldType())
                                        && ObjectIdentifier.equalsWithValue(params.getReasonCodeID(), lotHoldListAttributes.getReasonCodeID())
                                        && ObjectIdentifier.equalsWithValue(params.getRouteID(), lotHoldListAttributes.getResponsibleRouteID())
                                        && CimStringUtils.equals(params.getOperationNumber(), lotHoldListAttributes.getResponsibleOperationNumber()))
                        .map(lotHoldListAttributes -> {
                            Infos.LotHoldReq lotHoldReq = new Infos.LotHoldReq();
                            BeanUtils.copyProperties(lotHoldListAttributes, lotHoldReq);

                            lotHoldReq.setRouteID(lotHoldListAttributes.getResponsibleRouteID());
                            lotHoldReq.setOperationNumber(lotHoldListAttributes.getResponsibleOperationNumber());
                            lotHoldReq.setHoldUserID(lotHoldListAttributes.getUserID());

                            // new
                            lotHoldReq.setHoldReasonCodeID(params.getReasonCodeID());
                            lotHoldReq.setDepartment(params.getDepartment());
                            lotHoldReq.setSection(params.getSection());
                            // old
                            lotHoldReq.setOldHoldReasonCodeID(lotHoldListAttributes.getReasonCodeID());
                            lotHoldReq.setOldHoldUserID(lotHoldListAttributes.getUserID());
                            lotHoldReq.setOldRelatedLotID(lotHoldListAttributes.getRelatedLotID());
                            return lotHoldReq;
                        })
                        .collect(Collectors.toList());
                if (!CollectionUtils.isEmpty(holdReqList)) {
                    lotService.sxHoldDepartmentChangeReq(objCommon, lotId, holdReqList);
                }
            }
        });


    }

    @Override
    public void sxFutureHoldPreByPostProcessReq(Infos.ObjCommon objCommon, ObjectIdentifier lotID, String claimMemo) {
        //--------------------------------------------------
        // Trace and check input parameter
        //--------------------------------------------------
        Validations.check(ObjectIdentifier.isEmpty(lotID), retCodeConfig.getInvalidInputParam());

        /*------------------------------------------*/
        /*   Effect Future Hold Direction if Exist  */
        /*------------------------------------------*/
        ObjectIdentifier releaseReasonCodeID = ObjectIdentifier.buildWithValue(BizConstant.SP_REASON_GATEPASS);

        /*------------------------------------------------------------------*/
        /*   Get Effected Future Hold PRE of Current after Operation Move   */
        /*------------------------------------------------------------------*/
       Optional.ofNullable(lotMethod.lotFutureHoldRequestsEffectByCondition(objCommon, lotID,
               new Infos.EffectCondition(BizConstant.SP_FUTUREHOLD_PRE, BizConstant.SP_FUTUREHOLD_ALL)))
                .map(Outputs.ObjLotFutureHoldRequestsEffectByConditionOut::getStrLotHoldReqList)
                .filter(CimArrayUtils::isNotEmpty)
                .ifPresent(lotHoldReqs -> lotService.sxHoldLotReq(objCommon, lotID, lotHoldReqs));

        /*------------------------------------------------------------------------------------*/
        /*   Get Effected Future Hold Cancel PRE and SINGLE of Current after Operation Move   */
        /*------------------------------------------------------------------------------------*/
        Optional.ofNullable(lotMethod.lotFutureHoldRequestsDeleteEffectedByCondition(objCommon, lotID,
                new Infos.EffectCondition(BizConstant.SP_FUTUREHOLD_PRE, BizConstant.SP_FUTUREHOLD_SINGLE)))
                .map(Outputs.ObjLotFutureHoldRequestsDeleteEffectedByConditionOut::getStrFutureHoldReleaseReqList)
                .filter(CimArrayUtils::isNotEmpty)
                .ifPresent(lotHoldReleaseReqs -> sxFutureHoldCancelReq(objCommon, lotID, releaseReasonCodeID,
                        BizConstant.SP_ENTRYTYPE_REMOVE, lotHoldReleaseReqs));
    }

    @Autowired
    private IFutureHoldMethod futureHoldMethod;

    @Override
    public void sxFutureHoldPostByPostProcessReq(Infos.ObjCommon objCommon, ObjectIdentifier lotID, String claimMemo) {
        //--------------------------------------------------
        // Trace and check input parameter
        //--------------------------------------------------
        Validations.check(ObjectIdentifier.isEmpty(lotID), retCodeConfig.getInvalidInputParam());

        /*=========================================================================*/
        /* Previous PO Section                                                     */
        /*=========================================================================*/
        /*-------------------------------*/
        /*   Get Effected Future Hold    */
        /*-------------------------------*/
        List<Infos.LotHoldReq> preHoldReqs =
                Optional.ofNullable(futureHoldMethod.getForResponsibleOperation(objCommon, lotID,
                        new Infos.EffectCondition(BizConstant.SP_FUTUREHOLD_POST, BizConstant.SP_FUTUREHOLD_ALL)))
                        .map(Outputs.FutureHoldRequestsForPreviousPO::getStrLotHoldReqList)
                        .filter(CimArrayUtils::isNotEmpty)
                        .orElseGet(Collections::emptyList);

        /*-------------------------------------------*/
        /*   Delete Effected Future Hold Direction   */
        /*-------------------------------------------*/
        ObjectIdentifier releaseReasonCodeID = ObjectIdentifier.buildWithValue(BizConstant.SP_REASON_GATEPASS);
        Optional.ofNullable(futureHoldMethod.deleteForResponsibleOperation(objCommon, lotID,
                new Infos.EffectCondition(BizConstant.SP_FUTUREHOLD_POST, BizConstant.SP_FUTUREHOLD_SINGLE)))
                .map(Outputs.FutureHoldRequestsDeleteForPreviousPO::getStrFutureHoldReleaseReqList)
                .filter(CimArrayUtils::isNotEmpty)
                .ifPresent(records -> sxFutureHoldCancelReq(objCommon, lotID, releaseReasonCodeID,
                        BizConstant.SP_ENTRYTYPE_REMOVE, records));

        /*=========================================================================*/
        /* Current PO Section                                                      */
        /*=========================================================================*/

        /*----------------------------------------------------------------------------*/
        /*   Lot FutureHold Post By Previous Operation Effect after Operation Move    */
        /*----------------------------------------------------------------------------*/
        Optional.ofNullable(lotMethod.lotFutureHoldEffectedProcessConversion(objCommon, lotID, preHoldReqs))
                .map(Outputs.ObjLotFutureHoldEffectedProcessConversionOut::getLotHoldReqList)
                .filter(CimArrayUtils::isNotEmpty)
                .ifPresent(records -> lotService.sxHoldLotReq(objCommon, lotID, records));
    }
}
