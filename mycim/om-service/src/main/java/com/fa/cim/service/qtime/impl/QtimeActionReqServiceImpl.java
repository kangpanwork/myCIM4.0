package com.fa.cim.service.qtime.impl;

import com.fa.cim.annotaion.OmService;
import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.OmCode;
import com.fa.cim.common.support.RetCode;
import com.fa.cim.common.utils.*;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.config.RetCodeConfigEx;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Inputs;
import com.fa.cim.dto.Outputs;
import com.fa.cim.dto.Params;
import com.fa.cim.method.*;
import com.fa.cim.service.lot.ILotService;
import com.fa.cim.service.processcontrol.IProcessControlService;
import com.fa.cim.service.qtime.IFutureHoldReqService;
import com.fa.cim.service.qtime.IQtimeActionReqService;
import com.fa.cim.service.system.ISystemService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2018/11/7        ********             Jerry               create file
 *
 * @author: Jerry
 * @date: 2018/11/7 15:32
 * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */

@OmService
public class QtimeActionReqServiceImpl implements IQtimeActionReqService {

    @Autowired
    private IQTimeMethod qTimeMethod;

    @Autowired
    private RetCodeConfig retCodeConfig;

    @Autowired
    private RetCodeConfigEx retCodeConfigEx;

    @Autowired
    private ILotMethod lotMethod;

    @Autowired
    private IFutureHoldReqService futureHoldReqService;

    @Autowired
    private ILotService lotService;

    @Autowired
    private IProcessMethod processMethod;

    @Autowired
    private ISystemService systemService;

    @Autowired
    private IMessageMethod messageMethod;

    @Autowired
    private IPersonMethod personMethod;

    @Autowired
    private IProcessControlService processControlService;

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
            if (null != qtimeOriginalInformationOut
                    && !ObjectIdentifier.isEmptyWithValue(qtimeOriginalInformationOut.getTargetRouteID())) {
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
                        futureHoldReqService.sxFutureHoldReq(objCommon, futureHoldReqParams);
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
                        futureHoldReqService.sxFutureHoldReq(objCommon, futureHoldReqParams);
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
                List<Infos.OperationNameAttributes> strOperationAttributeList = processMethod.processOperationListForLot(objCommon, objProcessOperationListForLotIn);
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
                    String tmpFutureReworkDetailInfoTrigger = String.format("%s.%s.%s.%s.%s", ObjectIdentifier.fetchValue(lotID), ObjectIdentifier.fetchValue(originalRouteID),
                            originalTriggerOperationNumber, originalTargetOperationNumber, strQTimeAction.getQrestrictionTargetTimeStamp());

                    Infos.FutureReworkDetailInfo futureReworkDetailInfo = new Infos.FutureReworkDetailInfo();
                    futureReworkDetailInfo.setTrigger(tmpFutureReworkDetailInfoTrigger);
                    futureReworkDetailInfo.setReworkRouteID(strQTimeAction.getReworkRouteID());
                    futureReworkDetailInfo.setReturnOperationNumber("");
                    futureReworkDetailInfo.setReasonCodeID(strQTimeAction.getReasonCodeID());
                    Params.FutureReworkReqParams futureReworkReqParams = new Params.FutureReworkReqParams();
                    futureReworkReqParams.setUser(objCommon.getUser());
                    futureReworkReqParams.setLotID(lotID);
                    futureReworkReqParams.setRouteID(actionRouteID);
                    futureReworkReqParams.setOperationNumber(strQTimeAction.getActionOperationNumber());
                    futureReworkReqParams.setFutureReworkDetailInfo(futureReworkDetailInfo);
                    futureReworkReqParams.setClaimMemo(qtimeActionReqIn.getClaimMemo());
                    try {
                        processControlService.sxFutureReworkReq(objCommon, futureReworkReqParams);
                        bActionDoneFlag = true;
                    } catch (ServiceException e) {
                        if (Validations.isEquals(retCodeConfigEx.getFtrwkAlreadyExist(), e.getCode()) || Validations.isEquals(retCodeConfigEx.getFtrwkReadded(), e.getCode()) || Validations.isEquals(retCodeConfigEx.getFtrwkUpdate(), e.getCode())) {
                            bActionDoneFlag = true;
                        } else if (Validations.isEquals(retCodeConfigEx.getFtrwkDuplicate(), e.getCode())) {
                            bNeedAlertMessageRpt = true;
                            systemMsgCode = BizConstant.SP_SYSTEMMSGCODE_FUTUREREWORKDUP;
                            systemMessage += "Duplicated FutureRework actions were registered.\n";
                            systemMessage += "Error will occur when these FutureRework are executed.\n";
                            systemMessage += "LotID         : " + ObjectIdentifier.fetchValue(lotID) + "\n";
                            systemMessage += "RouteID       : " + ObjectIdentifier.fetchValue(routeID) + "\n";
                            systemMessage += "TriggerRouteID: " + ObjectIdentifier.fetchValue(actionRouteID) + '\n'; //DSN000085792
                            systemMessage += "TriggerOpe.No : " + strQTimeAction.getQrestrictionTriggerOperationNumber() + "\n";
                            systemMessage += "TargetOpe.No  : " + strQTimeAction.getQrestrictionTargetOperationNumber() + "\n";
                            systemMessage += "TatgetTime    : " + strQTimeAction.getQrestrictionTargetTimeStamp() + "\n";
                        } else {
                            throw e;
                        }
                    }
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
            bActionDoneFlag = true;
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

}