package com.fa.cim.service.doc.impl;

import com.fa.cim.annotaion.OmService;
import com.fa.cim.common.constant.BizConstant;
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
import com.fa.cim.service.doc.IDynamicOperationService;
import com.fa.cim.service.lot.ILotService;
import com.fa.cim.service.post.IPostService;
import com.fa.cim.service.system.ISystemService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * description:
 * <p>
 * change history:
 * date             defect             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/9/8        ********             Jack Luo            create file
 *
 * @author: Jack Luo
 * @date: 2020/9/8 20:23
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@OmService
public class DynamicOperationServiceImpl implements IDynamicOperationService {

    @Autowired
    private IProcessMethod processMethod;

    @Autowired
    private RetCodeConfig retCodeConfig;

    @Autowired
    private ILotMethod lotMethod;

    @Autowired
    private IEquipmentMethod equipmentMethod;

    @Autowired
    private ICassetteMethod cassetteMethod;

    @Autowired
    private IFPCMethod fpcMethod;

    @Autowired
    private IObjectLockMethod objectLockMethod;

    @Autowired
    private ILotService lotService;

    @Autowired
    private ISystemService systemService;

    @Autowired
    private IPostService postService;

    @Autowired
    private IRunCardMethod runCardMethod;

    @Autowired
    private RetCodeConfigEx retCodeConfigEx;

    @Autowired
    private ILotFamilyMethod lotFamilyMethod;

    @Autowired
    private IEventMethod eventMethod;

    @Autowired
    private IInterFabMethod interFabMethod;


    @Override
    public void sxDOCLotActionReq(Infos.ObjCommon objCommon, ObjectIdentifier lotID) {
        log.info("DOCLotActionReq lotID = {}", lotID);
        //=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
        // Check Process
        //=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
        /*--------------------------------*/
        /*   Lock objects to be updated   */
        /*--------------------------------*/
        // object_Lock
        objectLockMethod.objectLock(objCommon, CimLot.class, lotID);

        //---------------------------------------
        // Environment Variable Check
        //---------------------------------------
        log.info("Environment Variable Check");
        String tmpFPCAdaptFlag = StandardProperties.OM_DOC_ENABLE_FLAG.getValue();
        if (!BizConstant.ENV_ENABLE.equals(tmpFPCAdaptFlag)) {
            log.info("SP_FPC_ADAPTATION_FLAG is FALSE. now return");
            return;
        }

        //----------------------------------------------------------------
        //  Check effective DOC existence to lot.
        //----------------------------------------------------------------
        log.info("Check effective DOC existence to lot.");
        ObjectIdentifier dummyEqpID = null;
        ObjectIdentifier dummyoperationID = null;
        Boolean waferIDInfoGetFlag = true;      //All DOC data can be obtained by this Flag TRUE.
        Boolean recipeParmInfoGetFlag = false;
        Boolean reticleInfoGetFlag = false;
        Boolean dcSpecItemInfoGetFlag = false;

        List<Infos.FPCInfo> currentFPCInfoRet = lotMethod.lotCurrentFPCInfoGet(objCommon, lotID, dummyEqpID, waferIDInfoGetFlag, recipeParmInfoGetFlag, reticleInfoGetFlag, dcSpecItemInfoGetFlag);

        if (CimObjectUtils.isEmpty(currentFPCInfoRet)) {
            log.info("DOC setting is not found. normal end.");
            return;
        }

        //add runCard logic start
        //check run card exsit
        for (Infos.FPCInfo fpcInfo : currentFPCInfoRet) {
            List<ObjectIdentifier> wafers = fpcInfo.getLotWaferInfoList().stream().map(Infos.LotWaferInfo::getWaferID).collect(Collectors.toList());
            Infos.RunCardInfo splitRunCardInfo = runCardMethod.getRunCardInfoByDoc(objCommon,fpcInfo);
            if (null != splitRunCardInfo) {
                log.info("exsit runCard");
                if (!CimStringUtils.equals(BizConstant.RUNCARD_RUNNING, splitRunCardInfo.getRunCardState())) {
                    log.info("runCard state not RUNNING return");
                    //not trigger doc and return
                    return;
                }
                log.info("continue normal runCard doc logic");
            } else {
                //do nothing
                log.info("continue normal doc logic");
            }
        }
        //add runCard logic end

        //----------------------------------------------------------------
        // Lot's Control Job ID Check
        // If Lot has ControlJob, it means that the Lot has been reserved.
        // So return to caller.
        //----------------------------------------------------------------
        log.info("Lot's Control Job ID Check");
        ObjectIdentifier controlJobID = lotMethod.lotControlJobIDGet(objCommon, lotID);
        if (!ObjectIdentifier.isEmptyWithValue(controlJobID)) {
            throw new ServiceException(new OmCode(retCodeConfig.getLotControlJobidFilled(), ObjectIdentifier.fetchValue(lotID), ObjectIdentifier.fetchValue(controlJobID)));
        }

        //---------------------------------------
        // Does Lot have Transfer Reservation ?
        // Unnecessary
        //---------------------------------------

        //---------------------------------------
        // Is EI ?   Unnecessary
        //---------------------------------------

        //------------------------------------------------
        // Lot's Hold Status Check
        // If Lot is held, this function returns RC_OK,
        // because DOC will be adapted after Hold Release.
        //------------------------------------------------
        log.info("Lot's Hold Status Check");
        String objLotHoldStateGetOut = lotMethod.lotHoldStateGet(objCommon, lotID);
        if (BizConstant.CIMFW_LOT_HOLDSTATE_ONHOLD.equals(objLotHoldStateGetOut)) {
            log.info("Lot's holdState is OnHold. now return OK.");
            return;
        }

        int ppChainMode = StandardProperties.OM_PP_CHAIN_FLAG.getIntValue();
        String strTriggerDKey = null;
        if (1 == ppChainMode) {
            strTriggerDKey = ThreadContextHolder.getThreadSpecificDataString(BizConstant.SP_THREADSPECIFICDATA_KEY_TRIGGERDKEY);
            if (CimObjectUtils.isEmpty(strTriggerDKey)) {
                log.info("strTriggerDKey is blank, set ppChainMode = 0");
                ppChainMode = 0;
            }
        }

        if (1 == ppChainMode) {
            log.info("ppChainMode=1, strTriggerDKey={}", strTriggerDKey);
            List<Infos.PostProcessAdditionalInfo> postProcessAdditionalInfos = processMethod.postProcessAdditionalInfoGetDR(objCommon, strTriggerDKey, 0);
            int nExecutionCnt = 0;
            for (Infos.PostProcessAdditionalInfo postProcessAdditionalInfo : postProcessAdditionalInfos) {
                if (BizConstant.SP_THREADSPECIFICDATA_KEY_CHAINEXECCNT.equals(postProcessAdditionalInfo.getName())) {
                    nExecutionCnt = Integer.parseInt(postProcessAdditionalInfo.getValue());
                    break;
                }
            }
            nExecutionCnt++;
            log.info("nExecutionCnt {}", nExecutionCnt);

            int nMaxChainExecCnt = StandardProperties.OM_PP_MAX_CHAIN_FLAG.getIntValue();
            log.info("OM_PP_CHAIN_FLAG {}", nMaxChainExecCnt);

            if (nExecutionCnt > nMaxChainExecCnt) {
                log.info("nExecutionCnt > nMaxChainExecCnt, skip chained pp registration");
                ppChainMode = 0;
            } else {
                ThreadContextHolder.setThreadSpecificDataString(BizConstant.SP_THREADSPECIFICDATA_KEY_CHAINEXECCNT, String.valueOf(nExecutionCnt));
            }
        }

        log.info("OM_PP_CHAIN_FLAG = {}", ppChainMode);

        int skipLimit = StandardProperties.OM_DOC_SKIP_OPE_CONTINUOUS_LIMIT.getIntValue();
        log.info("ContinuousSkipLimit {}", skipLimit);
        if (skipLimit < 1) {
            log.info("ContinuousSkipLimit now reset to 1");
            skipLimit = 1;
        }

        Integer passCount = 0;
        Integer remaining = 0;
        Boolean done = false;
        List<Infos.OperationProcessRefListAttributes> operationProcessRefListAttributes = null;
        do {
            log.info("passCount... {}", passCount);
            done = true;

            if (0 == passCount) {
                // CAUTION : process_OperationListForLot is DR obj.

                Inputs.ObjProcessOperationProcessRefListForLotIn refListForLotIn = new Inputs.ObjProcessOperationProcessRefListForLotIn();
                refListForLotIn.setSearchDirection(true);
                refListForLotIn.setPosSearchFlag(false);
                refListForLotIn.setSearchCount(skipLimit + 1);
                refListForLotIn.setCurrentFlag(false);
                refListForLotIn.setLotID(lotID);
                operationProcessRefListAttributes = processMethod.processOperationProcessRefListForLot(objCommon, refListForLotIn);
                remaining = CimArrayUtils.getSize(operationProcessRefListAttributes);
                log.info("retrieved route length {}", remaining);
            }

            List<Infos.FPCDispatchEqpInfo> fpcDispatchEqpInfos = fpcMethod.fpcLotDispatchEquipmentsInfoCreate(objCommon, lotID);
            if (CimObjectUtils.isEmpty(fpcDispatchEqpInfos)) {
                log.info("There is no FPCInfo for this Lot {}", lotID);
                break;
            }

            Integer eqpMonitorSwitch = StandardProperties.OM_AUTOMON_FLAG.getIntValue();
            if (1 == eqpMonitorSwitch) {
                log.info("1 == SP_EQPMONITOR_SWITCH");
                //Check lot type
                String lotType = lotMethod.lotTypeGet(objCommon, lotID);

                if (BizConstant.SP_LOT_TYPE_EQUIPMENTMONITORLOT.equals(lotType)
                        || BizConstant.SP_LOT_TYPE_DUMMYLOT.equals(lotType)) {
                    log.info("strLot_lotType_Get_out.lotType is Auto Monitor or Dummy");
                    Boolean bEqpRestrictCheck = false;
                    for (Infos.FPCDispatchEqpInfo fpcDispatchEqpInfo : fpcDispatchEqpInfos) {
                        if (fpcDispatchEqpInfo.getRestrictEqpFlag()) {
                            log.info("TRUE == strFPCLotDispatchEquipmentsInfo_Create_out.strFPCDispatchEqpInfoList[iCnt].restrictEqpFlag");
                            bEqpRestrictCheck = true;
                            break;
                        }
                    }
                    if (bEqpRestrictCheck) {
                        log.info("TRUE == bEqpRestrictCheck");
                        Outputs.ObjEquipmentMonitorSectionInfoGetForJobOut equipmentMonitorSectionInfo = lotMethod.lotEqpMonitorSectionInfoGetForJob(objCommon, lotID);

                        if (BizConstant.SP_EQPMONITOR_OPELABEL_MONITOR.equals(equipmentMonitorSectionInfo.getOperationLabel())) {
                            log.info("strLot_eqpMonitorSectionInfo_GetForJob_out.operationLabel is Monitor");
                            //Get EqpMonitorJob Information
                            List<Infos.EqpMonitorJobInfo> strEqpMonitorJobInfoGetOut = equipmentMethod.eqpMonitorJobInfoGet(objCommon, equipmentMonitorSectionInfo.getEquipmentMonitorID(),
                                    equipmentMonitorSectionInfo.getEquipmentMonitorJobID());

                            //If the DOC definition includes equipment restriction for except EqpMonitor target equipment, an error is returned.
                            for (Infos.FPCDispatchEqpInfo fpcDispatchEqpInfo : fpcDispatchEqpInfos) {
                                if (fpcDispatchEqpInfo.getRestrictEqpFlag()) {
                                    log.info("TRUE == strFPCLotDispatchEquipmentsInfo_Create_out.strFPCDispatchEqpInfoList[iCnt].restrictEqpFlag");
                                    List<ObjectIdentifier> dispatchEqpIDs = fpcDispatchEqpInfo.getDispatchEqpIDs();
                                    if (!CimObjectUtils.isEmpty(dispatchEqpIDs) && 1 != dispatchEqpIDs.size()) {
                                        log.error("restrictEqpFlag is TRUE, but EQP count is not 1");
                                        throw new ServiceException(retCodeConfig.getInvalidParameter());
                                    } else {
                                        log.info("restrictEqpFlag is TRUE, and EQP count is 1");
                                        if (ObjectIdentifier.equalsWithValue(dispatchEqpIDs.get(0), strEqpMonitorJobInfoGetOut.get(0).getEquipmentID())) {
                                            log.error("Target lot is reserved to Auto Monitor job");
                                            throw new ServiceException(retCodeConfig.getLotResEqpmonjob(), lotID.getValue());
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            //Note : FPCGroupCount >= 1.
            Boolean sendEmailFlag = fpcDispatchEqpInfos.get(0).getSendEmailFlag();
            Boolean holdLotFlag = fpcDispatchEqpInfos.get(0).getHoldLotFlag();
            if (holdLotFlag) {
                log.info("DOC HOLD action required for this lot. {}", lotID);
                ObjectIdentifier dummyID = null;
                List<Infos.LotHoldReq> holdReqList = new ArrayList<>();
                Infos.LotHoldReq lotHoldReq = new Infos.LotHoldReq();
                holdReqList.add(lotHoldReq);
                lotHoldReq.setHoldType(BizConstant.SP_HOLDTYPE_LOTHOLD);
                lotHoldReq.setHoldReasonCodeID(new ObjectIdentifier(BizConstant.SP_REASON_FPCHOLD));
                lotHoldReq.setHoldUserID(objCommon.getUser().getUserID());
                lotHoldReq.setResponsibleOperationMark(BizConstant.SP_RESPONSIBLEOPERATION_CURRENT);
                lotHoldReq.setRelatedLotID(dummyID);
                lotService.sxHoldLotReq(objCommon, lotID, holdReqList);
            }
            if (sendEmailFlag || holdLotFlag) {
                log.info("DOC MailSend action required for this lot. {}", lotID);
                Outputs.ObjLotCurrentOperationInfoGetOut strLotCurrentOperationInfoGetOut = lotMethod.lotCurrentOperationInfoGet(objCommon, lotID);

                String mailMsg = null;
                String systemMsgCode = null;
                StringBuffer mailMsgSb = new StringBuffer();
                if (holdLotFlag) {
                    systemMsgCode = BizConstant.SP_SYSTEMMSGCODE_FPCHOLD;
                    mailMsgSb.append("DOC is applied and HOLD for the lot with following condition.\n");
                } else {
                    systemMsgCode = BizConstant.SP_SYSTEMMSGCODE_FPCEXEC;
                    mailMsgSb.append("DOC is applied for the lot with following condition.\n");
                }
                mailMsgSb.append("  Lot ID     : ").append(lotID.getValue()).append("\n")
                        .append("  Route ID   : ").append(ObjectIdentifier.fetchValue(strLotCurrentOperationInfoGetOut.getRouteID())).append("\n")
                        .append("  Process ID : ").append(ObjectIdentifier.fetchValue(strLotCurrentOperationInfoGetOut.getOperationID())).append("\n")
                        .append("  Ope No.    : ").append(strLotCurrentOperationInfoGetOut.getOperationNumber()).append("\n");
                mailMsg = mailMsgSb.toString();
                // txAlertMessageRpt
                Params.AlertMessageRptParams alertMessageRptParams = new Params.AlertMessageRptParams();
                alertMessageRptParams.setSubSystemID(BizConstant.SP_SUBSYSTEMID_MM);
                alertMessageRptParams.setSystemMessageCode(systemMsgCode);
                alertMessageRptParams.setSystemMessageText(mailMsg);
                alertMessageRptParams.setNotifyFlag(true);
                alertMessageRptParams.setLotID(lotID);
                alertMessageRptParams.setRouteID(strLotCurrentOperationInfoGetOut.getRouteID());
                alertMessageRptParams.setOperationID(strLotCurrentOperationInfoGetOut.getOperationID());
                alertMessageRptParams.setOperationNumber(strLotCurrentOperationInfoGetOut.getOperationNumber());
                alertMessageRptParams.setSystemMessageTimeStamp(CimDateUtils.convertToSpecString(objCommon.getTimeStamp().getReportTimeStamp()));
                systemService.sxAlertMessageRpt(objCommon, alertMessageRptParams);
            }

            if (holdLotFlag) {
                log.info("DOC targetLot is HOLD. Now break subsequent action. {}", lotID);
                break;
            }

            Boolean baseHasSplit = false;
            for (Infos.FPCDispatchEqpInfo fpcDispatchEqpInfo : fpcDispatchEqpInfos) {
                Integer groupNo = fpcDispatchEqpInfo.getFPCGroupNo();
                log.info("DOC Action for GroupNo", groupNo);
                ObjectIdentifier targetLotID;
                targetLotID = lotID;
                if (fpcDispatchEqpInfo.getSplitFlag()) {
                    log.info("  DOC AutoSplit action required for this group.");

                    ObjectIdentifier nextRouteID;
                    String nextOperationNo;
                    if (passCount < remaining) {
                        log.info("Get nextOperation for AutoSplit FH.");
                        nextRouteID = operationProcessRefListAttributes.get(passCount).getRouteID();
                        nextOperationNo = operationProcessRefListAttributes.get(passCount).getOperationNumber();

                        log.info("FutureHold operation for AutoSplit. {} {}", nextRouteID, nextOperationNo);
                    } else {
                        // This operation is the last operation. DOC AutoSplit with FutureHold cannot be taken place.
                        log.info("This operation is the last operation. DOC AutoSplit with FutureHold cannot be taken place.");
                        lotMethod.lotCurrentOperationInfoGet(objCommon, lotID);
                        log.info("Lot's next operation (Requested by DOC AutoSplit)");
                        throw new ServiceException(retCodeConfig.getFtholdNotFound());
                    }

                    baseHasSplit = true;
                    ObjectIdentifier dummyID = null;
                    Params.SplitLotReqParams splitLotReqParams = new Params.SplitLotReqParams();
                    splitLotReqParams.setParentLotID(lotID);
                    splitLotReqParams.setChildWaferIDs(fpcDispatchEqpInfo.getWaferIDs());
                    splitLotReqParams.setFutureMergeFlag(true);
                    splitLotReqParams.setMergedRouteID(nextRouteID);
                    splitLotReqParams.setMergedOperationNumber(nextOperationNo);
                    splitLotReqParams.setBranchingRouteSpecifyFlag(false);
                    splitLotReqParams.setSubRouteID(dummyID);
                    Results.SplitLotReqResult splitLotReqResultRet = lotService.sxSplitLotReq(objCommon, splitLotReqParams);

                    // change target Lot as split child.
                    log.info("DOC target Lot is child lot {}", targetLotID);
                    targetLotID = splitLotReqResultRet.getChildLotID();
                }

                if (fpcDispatchEqpInfo.getSkipFlag()) {
                    log.info("  DOC Skip action required for this group.");

                    if (!baseHasSplit) {
                        log.info("    Base Lot has not been DOC AutoSplit.");
                        done = false;
                    }

                    if (passCount < remaining) {
                        Outputs.ObjLotCurrentOperationInfoGetOut currentOperationInfoGetOutObj = lotMethod.lotCurrentOperationInfoGet(objCommon, lotID);
                        Params.SkipReqParams skipReqParams = new Params.SkipReqParams();
                        skipReqParams.setLocateDirection(true);
                        skipReqParams.setLotID(targetLotID);
                        skipReqParams.setCurrentRouteID(currentOperationInfoGetOutObj.getRouteID());
                        skipReqParams.setCurrentOperationNumber(currentOperationInfoGetOutObj.getOperationNumber());
                        skipReqParams.setRouteID(operationProcessRefListAttributes.get(passCount).getRouteID());
                        skipReqParams.setOperationID(dummyoperationID);
                        skipReqParams.setOperationNumber(operationProcessRefListAttributes.get(passCount).getOperationNumber());
                        skipReqParams.setProcessRef(operationProcessRefListAttributes.get(passCount).getProcessRef());
                        skipReqParams.setSeqno(operationProcessRefListAttributes.get(passCount).getSeqno());
                        lotService.sxSkipReq(objCommon, skipReqParams);


                    } else {
                        log.info("passCount >= remaining... something wrong. lot can be HOLD. {}", targetLotID);
                    }
                    //Register post process for child lot
                    log.info("OM_PP_CHAIN_FLAG", ppChainMode);

                    if (1 == ppChainMode) {
                        log.info("ppChainMode=1, call txPostTaskRegisterReq");
                        if (ObjectIdentifier.equalsWithValue(targetLotID, lotID)) {
                            log.info("parent lot skipped {}", targetLotID);
                            throw new ServiceException(new OmCode(retCodeConfig.getPostrpocDkeyRecreate(), lotID.getValue()));
                        } else {
                            log.info("child lot skipped", targetLotID);
                            Infos.PostProcessRegistrationParam postProcessRegistrationParam = new Infos.PostProcessRegistrationParam();
                            postProcessRegistrationParam.setLotIDs(Arrays.asList(targetLotID));
                            postService.sxPostTaskRegisterReq(objCommon, new Params.PostTaskRegisterReqParams(BizConstant.TX_ID_P_LOCATE, null, null, -1, postProcessRegistrationParam, ""));
                        }
                    } else {
                        log.info("ppChainMode==0, no need to recreate post process");
                    }
                }

                if (done) {
                    log.info("  DOC Now dispatch the lot of this group to the equipment.");
                    fpcMethod.fpcLotDispatchEquipmentsSet(objCommon, fpcDispatchEqpInfo.getRestrictEqpFlag(), targetLotID, fpcDispatchEqpInfo.getDispatchEqpIDs());
                }
            }
            passCount++;
        } while (!done && passCount < skipLimit);

        if (!done)  // skip limit exceeded.
        {
            //use baseLotID. because continuous skip occurs on non-split lot only.
            log.info("DOC Continuous Skip Limit Exceeded! Now HOLD the Lot. {}", lotID);
            ObjectIdentifier dummyID = null;
            List<Infos.LotHoldReq> holdReqList = new ArrayList<>();
            Infos.LotHoldReq lotHoldReq = new Infos.LotHoldReq();
            holdReqList.add(lotHoldReq);
            lotHoldReq.setHoldType(BizConstant.CIMFW_LOT_HOLDSTATE_ONHOLD);
            lotHoldReq.setHoldReasonCodeID(new ObjectIdentifier(BizConstant.SP_REASON_FPCHOLD));
            lotHoldReq.setHoldUserID(objCommon.getUser().getUserID());
            lotHoldReq.setResponsibleOperationMark(BizConstant.SP_RESPONSIBLEOPERATION_CURRENT);
            lotHoldReq.setRelatedLotID(dummyID);
            lotService.sxHoldLotReq(objCommon, lotID, holdReqList);
        }

        //-----------------------
        // Update multiLotType
        //-----------------------
        ObjectIdentifier lotCassetteOut = null;
        int retCode = 0;
        try {
            lotCassetteOut = lotMethod.lotCassetteGet(objCommon, lotID);
        } catch (ServiceException e) {
            retCode = e.getCode();
            if (!Validations.isEquals(retCodeConfig.getNotFoundCassette(), e.getCode())) {
                throw e;
            }
        }
        if (retCode == 0) {
            cassetteMethod.cassetteMultiLotTypeUpdate(objCommon, lotCassetteOut);
        }
    }

    @Override
    public Results.DOCLotInfoSetReqResult sxDOCLotInfoSetReq(Infos.ObjCommon objCommon, List<Infos.FPCInfoAction> strFPCInfoActionList, String claimMemo, String runCardID) {

        //===================================================================================
        // **** Check input parameter
        //===================================================================================
        //-------------------------------------------------------------
        //
        // *** Check length of DOC Info structure
        //
        //-------------------------------------------------------------
        Validations.check(CimObjectUtils.isEmpty(strFPCInfoActionList), retCodeConfig.getInvalidParameter());
        //-------------------------------------------------------------
        //
        // *** Check Action type
        //     Create/Update/NoChange
        //-------------------------------------------------------------
        for (Infos.FPCInfoAction fpcInfoAction : strFPCInfoActionList) {
            String actionType = fpcInfoAction.getActionType();
            Infos.FPCInfo strFPCInfo = fpcInfoAction.getStrFPCInfo();
            log.info("{} ## Action Type is ", actionType);
            log.info("{} ## DOC ID      is ", strFPCInfo.getFpcID());
            if (CimObjectUtils.isEmpty(actionType)
                    || (!CimStringUtils.equals(actionType, BizConstant.SP_FPCINFO_CREATE)
                    && !CimStringUtils.equals(actionType, BizConstant.SP_FPCINFO_UPDATE)
                    && !CimStringUtils.equals(actionType, BizConstant.SP_FPCINFO_NOCHANGE))) {
                log.info("{} Action Type is not normal.", actionType);
                throw new ServiceException(retCodeConfig.getInvalidParameter());
            }
            if (CimStringUtils.equals(actionType, BizConstant.SP_FPCINFO_CREATE)) {
                if (!CimObjectUtils.isEmpty(strFPCInfo.getFpcID()) ||
                        !CimObjectUtils.isEmpty(strFPCInfo.getCreateTime()) ||
                        !CimObjectUtils.isEmpty(strFPCInfo.getUpdateTime())) {
                    log.info("{} FPCID and CreateTime and UpdateTime should be blank with Action Create.", actionType);
                    throw new ServiceException(retCodeConfig.getInvalidParameter());
                }
            }
            if (CimStringUtils.equals(actionType, BizConstant.SP_FPCINFO_UPDATE) ||
                    CimStringUtils.equals(actionType, BizConstant.SP_FPCINFO_NOCHANGE)) {
                if (CimObjectUtils.isEmpty(strFPCInfo.getFpcID()) ||
                        CimObjectUtils.isEmpty(strFPCInfo.getCreateTime()) ||
                        CimObjectUtils.isEmpty(strFPCInfo.getUpdateTime())) {
                    log.info("{} FPCID should be set with Action Update or NoChange.", actionType);
                    throw new ServiceException(retCodeConfig.getInvalidParameter());
                }
            }
        }

        //-------------------------------------------------------------
        //
        // *** Check LotFamily, Route/Opearation
        //
        //-------------------------------------------------------------
        //MandatoryItems: LotFamilyID, mainPDID, operationNumber, pdID, FPCType, Wafers
        log.info("## Check Mandatory Items : LotFamilyID, mainPDID, operationNumber, pdID, FPCType, Wafers");
        Validations.check(CimArrayUtils.isEmpty(
                strFPCInfoActionList.get(0).getStrFPCInfo().getLotWaferInfoList()),retCodeConfig.getNotFoundWafer());
        Infos.FPCInfo strFPCInfo0 = strFPCInfoActionList.get(0).getStrFPCInfo();
        if (ObjectIdentifier.isEmpty(strFPCInfo0.getLotFamilyID()) ||
                ObjectIdentifier.isEmpty(strFPCInfo0.getMainProcessDefinitionID()) ||
                CimObjectUtils.isEmpty(strFPCInfo0.getOperationNumber()) ||
                ObjectIdentifier.isEmpty(strFPCInfo0.getProcessDefinitionID()) ||
                CimObjectUtils.isEmpty(strFPCInfo0.getFpcType())) {
            log.info("Some necessary items(LotFamilyID,mainPDID,OperNo,PDID,FPCType) are missing for registration DOC .");
            log.info("{} LotFamilyID       ", strFPCInfo0.getLotFamilyID());
            log.info("{} mainPDID          ", strFPCInfo0.getMainProcessDefinitionID());
            log.info("{} operationNumber   ", strFPCInfo0.getOperationNumber());
            log.info("{} PDID              ", strFPCInfo0.getProcessDefinitionID());
            log.info("{} FPCType           ", strFPCInfo0.getFpcType());
            log.info("{} Corresponding Operation ", strFPCInfo0.getCorrespondOperationNumber());//PSN000103458
            throw new ServiceException(retCodeConfig.getInvalidParameter());
        }

        for (Infos.FPCInfoAction fpcInfoAction : strFPCInfoActionList) {
            Infos.FPCInfo strFPCInfo = fpcInfoAction.getStrFPCInfo();
            ObjectIdentifier lotFamilyID = strFPCInfo.getLotFamilyID();
            log.info("## Action Type             {} ", fpcInfoAction.getActionType());
            log.info("## LotFamilyID             {} ", lotFamilyID);
            log.info("## mainPDID                {} ", strFPCInfo.getMainProcessDefinitionID());
            log.info("## operationNumber         {} ", strFPCInfo.getOperationNumber());
            log.info("## originalMainPDID        {} ", strFPCInfo.getOriginalMainProcessDefinitionID());
            log.info("## originalOperationNumber {} ", strFPCInfo.getOriginalOperationNumber());
            log.info("## subMainPDID             {} ", strFPCInfo.getSubMainProcessDefinitionID());
            log.info("## subOperationNumber      {} ", strFPCInfo.getSubOperationNumber());
            log.info("## PDID                    {} ", strFPCInfo.getProcessDefinitionID());
            log.info("## PDType                  {} ", strFPCInfo.getProcessDefinitionType());
            log.info("## Corresponding Operation {} ", strFPCInfo.getCorrespondOperationNumber());//PSN000103458

            //The lot of runCard cannot be used in DOC, check if the lot of runCard and DOC conflict
            List<Infos.LotWaferInfo> waferInfoList = strFPCInfo.getLotWaferInfoList();
            if (CimArrayUtils.isEmpty(waferInfoList)) { //If runCardID is empty, it means DOC add
                Validations.check(retCodeConfig.getInvalidInputParam());
            }
            Infos.RunCardInfo runCardFromLotID = runCardMethod.getRunCardInfoByDoc(objCommon,strFPCInfo);
            if (CimStringUtils.isEmpty(runCardID)) { //If runCardID is empty, it means DOC add
                Validations.check(runCardFromLotID != null, retCodeConfigEx.getAlreadyExsitRunCard(), lotFamilyID);
            } else { //If runCardID is not empty, it means runCard add
                Validations.check(runCardFromLotID == null, retCodeConfigEx.getAlreadyExsitDoc(), lotFamilyID);
            }

            if (ObjectIdentifier.equalsWithValue(lotFamilyID, strFPCInfo0.getLotFamilyID()) &&
                    ObjectIdentifier.equalsWithValue(strFPCInfo.getMainProcessDefinitionID(), strFPCInfo0.getMainProcessDefinitionID()) &&
                    CimStringUtils.equals(strFPCInfo.getOperationNumber(), strFPCInfo0.getOperationNumber()) &&
                    ObjectIdentifier.equalsWithValue(strFPCInfo.getOriginalMainProcessDefinitionID(), strFPCInfo0.getOriginalMainProcessDefinitionID()) &&
                    CimStringUtils.equals(strFPCInfo.getOriginalOperationNumber(), strFPCInfo0.getOriginalOperationNumber()) &&
                    ObjectIdentifier.equalsWithValue(strFPCInfo.getSubMainProcessDefinitionID(), strFPCInfo0.getSubMainProcessDefinitionID()) &&
                    CimStringUtils.equals(strFPCInfo.getSubOperationNumber(), strFPCInfo0.getSubOperationNumber())) {
                //OK
                List<Infos.LotWaferInfo> lotWaferInfoList = waferInfoList;
                if (CimObjectUtils.isEmpty(lotWaferInfoList)) {
                    log.info("Any wafers does not exist.");
                    throw new ServiceException(retCodeConfig.getInvalidParameter());
                }
            } else {
                log.info("Some necessary items are missing for registration DOC .");
                throw new ServiceException(retCodeConfig.getInvalidParameter());
            }
            if (!CimStringUtils.equals(strFPCInfo.getProcessDefinitionType(), BizConstant.SP_DCDEF_COLLECTION_MEASUREMENT)
                    && !CimObjectUtils.isEmpty(strFPCInfo.getCorrespondingOperationInfoList())
                    && 1 < CimArrayUtils.getSize(strFPCInfo.getCorrespondingOperationInfoList())) {
                log.info("Can not set multiple Corresponding Operation if PDType is Process.");
                Validations.check(retCodeConfig.getInvalidPDType(), strFPCInfo.getProcessDefinitionType(), strFPCInfo.getMainProcessDefinitionID());
            }

            log.info("call lotFamily_allLots_GetDR()");
            List<ObjectIdentifier> lotIDs = lotFamilyMethod.lotFamilyAllLotsGetDR(objCommon, lotFamilyID);
            for (ObjectIdentifier lotID : lotIDs) {
                //-----------------------------------------------------------
                // Check lot interFabXferState
                //-----------------------------------------------------------
                String interFabXferState = lotMethod.lotInterFabXferStateGet(objCommon, lotID);

                //-----------------------------------------------------------
                // "Transferring"
                //-----------------------------------------------------------
                Validations.check(CimStringUtils.equals(interFabXferState, BizConstant.SP_INTERFAB_XFERSTATE_TRANSFERRING),
                        retCodeConfig.getInterfabInvalidLotXferstateForReq(), lotID, interFabXferState);

                if (strFPCInfo.isSkipFalg()) {
                    //---------------------------------------
                    //   Check interFabXferPlan existence
                    //---------------------------------------
                    Outputs.ObjInterFabXferPlanListGetDROut objInterFabXferPlanListGetDROut = null;
                    try {
                        Inputs.ObjInterFabXferPlanListGetDRIn objInterFabXferPlanListGetDRIn = new Inputs.ObjInterFabXferPlanListGetDRIn();
                        Infos.InterFabLotXferPlanInfo interFabLotXferPlanInfo = new Infos.InterFabLotXferPlanInfo();
                        interFabLotXferPlanInfo.setLotID(lotID);
                        interFabLotXferPlanInfo.setSeqNo(0);
                        interFabLotXferPlanInfo.setOriginalRouteID(strFPCInfo.getMainProcessDefinitionID());
                        interFabLotXferPlanInfo.setOriginalOpeNumber(strFPCInfo.getOperationNumber());
                        objInterFabXferPlanListGetDRIn.setStrInterFabLotXferPlanInfo(interFabLotXferPlanInfo);
                        objInterFabXferPlanListGetDROut = interFabMethod.interFabXferPlanListGetDR(objCommon, objInterFabXferPlanListGetDRIn);
                    } catch (ServiceException e) {
                        if (!Validations.isEquals(retCodeConfig.getInterfabNotFoundXferPlan(), e.getCode())) {
                            throw e;
                        }
                    }
                    if (objInterFabXferPlanListGetDROut != null) {
                        List<Infos.InterFabLotXferPlanInfo> strInterFabLotXferPlanInfoSeq = objInterFabXferPlanListGetDROut.getStrInterFabLotXferPlanInfoSeq();
                        for (Infos.InterFabLotXferPlanInfo interFabLotXferPlanInfo : strInterFabLotXferPlanInfoSeq) {
                            Validations.check(!CimStringUtils.equals(interFabLotXferPlanInfo.getState(), BizConstant.SP_INTERFAB_XFERPLANSTATE_COMPLETED),
                                    retCodeConfigEx.getInterfabXferplanSpecified(), strFPCInfo.getMainProcessDefinitionID(), strFPCInfo.getOperationNumber());
                        }
                    }
                }
            }
        }

        //===================================================================================
        // **** Compare the defined DOC info with input DOC info and merge them.
        //===================================================================================
        log.info("## Compare the defined DOC info with input DOC info and merge them.");
        List<Infos.FPCInfoAction> strFPCInfoActionListAll = fpcMethod.fpcInfoMerge(objCommon, strFPCInfoActionList);

        //===================================================================================
        // **** Check consistency between DOC information.
        // **** Check consistency between DOC definition and SM definition.
        //===================================================================================
        strFPCInfoActionListAll = fpcMethod.fpcInfoConsistencyCheckForUpdate(objCommon, strFPCInfoActionListAll);

        //===================================================================================
        // **** Check whether DOC information can be updated or not.
        //
        // Check:FPCAvailableFlag/HoldState
        //
        // Check about whether the target Lot is on the specified DOC info point or not.
        // If some lots have holdRecord which is responsibleOperation Previous,
        // the DOC info can NOT be registered.
        // However, if the DOC info is the initial registration, it is allowed.
        // Ofcource, all lot of the lotFamily should have authority for the registration.
        // ( DOC Available Flag = TRUE )
        //===================================================================================
        log.info("## Check whether DOC information can be updated or not.");
        Infos.FPCInfo strFPCInfo0ByFPCInfo = strFPCInfoActionListAll.get(0).getStrFPCInfo();
        Inputs.FPCCheckConditionForUpdateIn in = new Inputs.FPCCheckConditionForUpdateIn();
        in.setLotFamilyID(strFPCInfo0ByFPCInfo.getLotFamilyID());
        in.setActionType(BizConstant.SP_FPCINFO_UPDATE);
        in.setMainPDID(strFPCInfo0ByFPCInfo.getMainProcessDefinitionID());
        in.setMainOpeNo(strFPCInfo0ByFPCInfo.getOperationNumber());
        in.setOrgMainPDID(strFPCInfo0ByFPCInfo.getOriginalMainProcessDefinitionID());
        in.setOrgOpeNo(strFPCInfo0ByFPCInfo.getOriginalOperationNumber());
        in.setSubMainPDID(strFPCInfo0ByFPCInfo.getSubMainProcessDefinitionID());
        in.setSubOpeNo(strFPCInfo0ByFPCInfo.getSubOperationNumber());

        Outputs.ObjFPCCheckConditionForUpdateOut fpcCheckConditionForUpdateOut = fpcMethod.fpcCheckConditionForUpdate(objCommon, in);
        List<ObjectIdentifier> strLotFamilyCurrentStatusList = fpcCheckConditionForUpdateOut.getStrLotFamilyCurrentStatusList();
        if (!CimArrayUtils.isEmpty(strLotFamilyCurrentStatusList)) {
            log.info("{} ## Current Lot is OnHold ?", (fpcCheckConditionForUpdateOut.getHoldFlag() ? "TRUE" : "FALSE"));
            if (!fpcCheckConditionForUpdateOut.getHoldFlag()) {
                //Initial registration?
                if (1 < CimArrayUtils.getSize(strFPCInfoActionListAll)) {
                    log.info("This registration is NOT allowd because the DOC info is NOT the initial registration.");
                    Validations.check(true, retCodeConfig.getFpcUpdateError(), "Because some lots are on the specified operation and the DOC info is NOT the initial registration.");
                }

                if (!CimStringUtils.equals(strFPCInfoActionListAll.get(0).getActionType(), BizConstant.SP_FPCINFO_CREATE)) {
                    log.info("This registration is NOT allowd because the DOC info is NOT the initial registration.");
                    Validations.check(true, retCodeConfig.getFpcUpdateError(), "Because the DOC info is NOT the initial registration.");
                }
            }

            int eqpMonitorSwitch = StandardProperties.OM_AUTOMON_FLAG.getIntValue();
            if (1 == eqpMonitorSwitch) {
                log.info("1 == SP_EQPMONITOR_SWITCH");
                for (Infos.FPCInfoAction fpcInfoAction : strFPCInfoActionListAll) {
                    if (fpcInfoAction.getStrFPCInfo().isRestrictEquipmentFlag()) {
                        log.info("TRUE == strFPCInfoActionListAll[iCnt1].strFPCInfo0.restrictEqpFlag");
                        if (CimStringUtils.equals(fpcInfoAction.getActionType(), BizConstant.SP_FPCINFO_CREATE)) {
                            log.info("strFPCInfoActionListAll[iCnt1].actionType is Create");
                            for (ObjectIdentifier lotID : strLotFamilyCurrentStatusList) {
                                String lotType = lotMethod.lotTypeGet(objCommon, lotID);

                                if (CimStringUtils.equals(lotType, BizConstant.SP_LOT_TYPE_EQUIPMENTMONITORLOT)
                                        || CimStringUtils.equals(lotType, BizConstant.SP_LOT_TYPE_DUMMYLOT)) {
                                    log.info("strLot_lotType_Get_out.lotType is Equipment Monitor or Dummy");

                                    Inputs.ObjLotWaferIDListGetDRIn lotWaferIDListParams = new Inputs.ObjLotWaferIDListGetDRIn();
                                    lotWaferIDListParams.setLotID(lotID);
                                    lotWaferIDListParams.setScrapCheckFlag(true);
                                    List<ObjectIdentifier> waferIDs = lotMethod.lotWaferIDListGetDR(objCommon, lotWaferIDListParams);

                                    boolean bWaferFound = false;
                                    List<Infos.LotWaferInfo> lotWaferInfoList = fpcInfoAction.getStrFPCInfo().getLotWaferInfoList();
                                    for (Infos.LotWaferInfo lotWaferInfo : lotWaferInfoList) {
                                        for (ObjectIdentifier waferID : waferIDs) {
                                            if (ObjectIdentifier.equalsWithValue(waferID,
                                                    lotWaferInfo.getWaferID())) {
                                                log.info("bWaferFound = TRUE");
                                                bWaferFound = true;
                                                break;
                                            }
                                        }

                                        if (bWaferFound) {
                                            log.info("TRUE == bWaferFound");
                                            break;
                                        }
                                    }

                                    if (bWaferFound) {
                                        log.info("TRUE == bWaferFound");
                                        Outputs.ObjEquipmentMonitorSectionInfoGetForJobOut equipmentMonitorSectionInfo = lotMethod.lotEqpMonitorSectionInfoGetForJob(objCommon, lotID);

                                        if (CimStringUtils.equals(equipmentMonitorSectionInfo.getOperationLabel(), BizConstant.SP_EQPMONITOR_OPELABEL_MONITOR)) {
                                            log.info("strLot_eqpMonitorSectionInfo_GetForJob_out.operationLabel is Monitor");
                                            //Get EqpMonitorJob Information
                                            List<Infos.EqpMonitorJobInfo> strEqpMonitorJobInfoGetOut = equipmentMethod.eqpMonitorJobInfoGet(objCommon, equipmentMonitorSectionInfo.getEquipmentMonitorID(), equipmentMonitorSectionInfo.getEquipmentMonitorJobID());


                                            //If the DOC definition includes equipment restriction for except EqpMonitor target equipment, an error is returned.
                                            if (!ObjectIdentifier.equalsWithValue(fpcInfoAction.getStrFPCInfo().getEquipmentID(),
                                                    strEqpMonitorJobInfoGetOut.get(0).getEquipmentID())
                                                    && fpcInfoAction.getStrFPCInfo().isRestrictEquipmentFlag()) {
                                                log.info("Target lot is reserved to Equipment Monitor job");
                                                throw new ServiceException(new OmCode(retCodeConfig.getLotResEqpmonjob(), ObjectIdentifier.fetchValue(lotID)));
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            log.info("strFPCInfoActionListAll[iCnt1].actionType is not Create");
                            List<ObjectIdentifier> heldLotIDs = fpcCheckConditionForUpdateOut.getHeldLotIDs();
                            for (ObjectIdentifier heldLotID : heldLotIDs) {
                                String lotType = lotMethod.lotTypeGet(objCommon, heldLotID);

                                if (CimStringUtils.equals(lotType, BizConstant.SP_LOT_TYPE_EQUIPMENTMONITORLOT)
                                        || CimStringUtils.equals(lotType, BizConstant.SP_LOT_TYPE_DUMMYLOT)) {
                                    log.info("strLot_lotType_Get_out.lotType is Equipment Monitor or Dummy");
                                    Inputs.ObjLotWaferIDListGetDRIn lotWaferIDListGetDRIn = new Inputs.ObjLotWaferIDListGetDRIn();
                                    lotWaferIDListGetDRIn.setLotID(heldLotID);
                                    lotWaferIDListGetDRIn.setScrapCheckFlag(true);
                                    List<ObjectIdentifier> waferIDs = lotMethod.lotWaferIDListGetDR(objCommon, lotWaferIDListGetDRIn);

                                    boolean bWaferFound = false;
                                    List<Infos.LotWaferInfo> lotWaferInfoList = fpcInfoAction.getStrFPCInfo().getLotWaferInfoList();
                                    for (Infos.LotWaferInfo lotWaferInfo : lotWaferInfoList) {
                                        for (ObjectIdentifier waferID : waferIDs) {
                                            if (ObjectIdentifier.equalsWithValue(waferID, lotWaferInfo.getWaferID())) {
                                                log.info("bWaferFound = TRUE");
                                                bWaferFound = true;
                                                break;
                                            }
                                        }

                                        if (bWaferFound) {
                                            log.info("TRUE == bWaferFound");
                                            break;
                                        }
                                    }

                                    if (bWaferFound) {
                                        log.info("TRUE == bWaferFound");
                                        Outputs.ObjEquipmentMonitorSectionInfoGetForJobOut equipmentMonitorSectionInfo = lotMethod.lotEqpMonitorSectionInfoGetForJob(objCommon, heldLotID);
                                        if (CimStringUtils.equals(equipmentMonitorSectionInfo.getOperationLabel(), BizConstant.SP_EQPMONITOR_OPELABEL_MONITOR)) {
                                            log.info("strLot_eqpMonitorSectionInfo_GetForJob_out.operationLabel is Monitor");
                                            //Get EqpMonitorJob Information
                                            List<Infos.EqpMonitorJobInfo> strEqpMonitorJobInfoGetOut = equipmentMethod.eqpMonitorJobInfoGet(objCommon, equipmentMonitorSectionInfo.getEquipmentMonitorID(), equipmentMonitorSectionInfo.getEquipmentMonitorJobID());
                                            //If the DOC definition includes equipment restriction for except EqpMonitor target equipment, an error is returned.
                                            if (!ObjectIdentifier.equalsWithValue(fpcInfoAction.getStrFPCInfo().getEquipmentID(), strEqpMonitorJobInfoGetOut.get(0).getEquipmentID())
                                                    && fpcInfoAction.getStrFPCInfo().isRestrictEquipmentFlag()) {
                                                log.info("Target lot is reserved to Equipment Monitor job");
                                                throw new ServiceException(new OmCode(retCodeConfig.getLotResEqpmonjob(), ObjectIdentifier.fetchValue(heldLotID)));
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        //===================================================================================
        // **** Create/Update DOC information.
        //===================================================================================
        log.info("## Create/Update DOC information.");
        fpcMethod.fpcInfoUpdateDR(objCommon, strFPCInfoActionListAll);

        //===================================================================================
        // **** Make event for DOC information registration.
        //===================================================================================
        log.info("## Make event for DOC information registration. ");
        //FPC_infoRegist_Event_Make__101
        eventMethod.fpcInfoRegistEventMake(objCommon, objCommon.getTransactionID(), strFPCInfoActionList, claimMemo, runCardID);
        //===================================================================================
        // **** Set out structure
        //===================================================================================
        log.info("## Set out structure.");
        Results.DOCLotInfoSetReqResult docLotInfoSetReqResult = new Results.DOCLotInfoSetReqResult();
        docLotInfoSetReqResult.setLotIDs(strLotFamilyCurrentStatusList);
        docLotInfoSetReqResult.setLotFamilyID(strFPCInfoActionListAll.get(0).getStrFPCInfo().getLotFamilyID());
        List<String> FPCIDs = new ArrayList<>();
        docLotInfoSetReqResult.setFPC_IDs(FPCIDs);
        for (Infos.FPCInfoAction fpcInfoAction : strFPCInfoActionListAll) {
            if (CimStringUtils.equals(fpcInfoAction.getActionType(), BizConstant.SP_FPCINFO_CREATE) ||
                    CimStringUtils.equals(fpcInfoAction.getActionType(), BizConstant.SP_FPCINFO_UPDATE)) {
                FPCIDs.add(fpcInfoAction.getStrFPCInfo().getFpcID());
            }
        }

        //--------------------
        //   Return to Main
        //--------------------
        return docLotInfoSetReqResult;
    }

    @Override
    public Results.DOCLotRemoveReqResult sxDOCLotRemoveReq(Infos.ObjCommon objCommon, Params.DOCLotRemoveReqParams strDOCLotRemoveReqInParm) {

        //init
        log.info("PPTManager_i::txDOCLotRemoveReq");
        Results.DOCLotRemoveReqResult out = new Results.DOCLotRemoveReqResult();

        //check param
        List<String> fpcIDs = strDOCLotRemoveReqInParm.getFpcIDs();
        ObjectIdentifier lotFamilyID = strDOCLotRemoveReqInParm.getLotFamilyID();
        int fpcLen = CimArrayUtils.getSize(fpcIDs);
        log.info("FPC_IDs.length() {}", fpcLen);
        Validations.check(0 == fpcLen, retCodeConfig.getInvalidInputParam());
        log.info("Specified lotFamilyID {}", lotFamilyID.getValue());
        Validations.check(ObjectIdentifier.isEmptyWithValue(lotFamilyID), retCodeConfig.getInvalidInputParam());

        //Get DOC Info
        //【step1】 FPC_info_GetDR__101
        Inputs.ObjFPCInfoGetDRIn objFPCInfoGetDRIn = new Inputs.ObjFPCInfoGetDRIn();
        objFPCInfoGetDRIn.setFPCIDs(fpcIDs);
        objFPCInfoGetDRIn.setLotID(null);
        objFPCInfoGetDRIn.setLotFamilyID(null);
        objFPCInfoGetDRIn.setMainPDID(null);
        objFPCInfoGetDRIn.setMainOperNo("");
        objFPCInfoGetDRIn.setOrgMainPDID(null);
        objFPCInfoGetDRIn.setOrgOperNo("");
        objFPCInfoGetDRIn.setSubMainPDID(null);
        objFPCInfoGetDRIn.setSubOperNo("");
        objFPCInfoGetDRIn.setEquipmentID(null);
        objFPCInfoGetDRIn.setWaferIDInfoGetFlag(true);
        objFPCInfoGetDRIn.setRecipeParmInfoGetFlag(true);
        objFPCInfoGetDRIn.setReticleInfoGetFlag(true);
        objFPCInfoGetDRIn.setDcSpecItemInfoGetFlag(true);
        List<Infos.FPCInfo> fpcInfoGetDROut = fpcMethod.fpcInfoGetDR(objCommon, objFPCInfoGetDRIn);

        int regFpcLen = CimArrayUtils.getSize(fpcInfoGetDROut);
        Validations.check(0 == regFpcLen, retCodeConfig.getInvalidInputParam());
        for (int fpcCnt = 0; fpcCnt < regFpcLen; fpcCnt++) {
            // Check matching with lotFamily
            Validations.check(!ObjectIdentifier.equalsWithValue(fpcInfoGetDROut.get(fpcCnt).getLotFamilyID(), lotFamilyID), retCodeConfig.getFpcDeleteError());
            //Check condition
            //【step2】FPC_CheckConditionForUpdate__140
            Inputs.FPCCheckConditionForUpdateIn in = new Inputs.FPCCheckConditionForUpdateIn();
            in.setLotFamilyID(fpcInfoGetDROut.get(fpcCnt).getLotFamilyID());
            in.setActionType(BizConstant.SP_FPCINFO_DELETE);
            in.setMainPDID(fpcInfoGetDROut.get(fpcCnt).getMainProcessDefinitionID());
            in.setMainOpeNo(fpcInfoGetDROut.get(fpcCnt).getOperationNumber());
            in.setOrgMainPDID(fpcInfoGetDROut.get(fpcCnt).getOriginalMainProcessDefinitionID());
            in.setOrgOpeNo(fpcInfoGetDROut.get(fpcCnt).getOriginalOperationNumber());
            in.setSubMainPDID(fpcInfoGetDROut.get(fpcCnt).getSubMainProcessDefinitionID());
            in.setSubOpeNo(fpcInfoGetDROut.get(fpcCnt).getSubOperationNumber());
            fpcMethod.fpcCheckConditionForUpdate(objCommon, in);

        }
        log.info("call lotFamily_allLots_GetDR()");
        //【step3】lotFamily_allLots_GetDR
        List<ObjectIdentifier> lotFamilyAllLotsGetDROut = lotFamilyMethod.lotFamilyAllLotsGetDR(objCommon, lotFamilyID);

        int i = 0;
        int lotIDLen = CimArrayUtils.getSize(lotFamilyAllLotsGetDROut);
        log.info("lotIDLen {}", lotIDLen);
        for (i = 0; i < lotIDLen; i++) {
            log.info("call lot_interFabXferState_Get() {}", lotFamilyAllLotsGetDROut.get(i).getValue());
            //Check lot interFabXferState
            //【step4】lot_interFabXferState_Get
            String lotInterFabXferStateResultOut = lotMethod.lotInterFabXferStateGet(objCommon, lotFamilyAllLotsGetDROut.get(i));

            //"Transferring"
            Validations.check(CimStringUtils.equals(BizConstant.SP_INTERFAB_XFERSTATE_TRANSFERRING, lotInterFabXferStateResultOut)
                    , retCodeConfig.getInterfabInvalidLotXferstateForReq());
        }
        //Delete DOC definitions
        //【step5】 FPC_info_DeleteDR
        fpcMethod.fpcInfoDeleteDR(objCommon, fpcIDs);

        //Create event
        List<Infos.FPCInfoAction> strFPCInfoActionList = new ArrayList<>();
        for (int fpcCnt = 0; fpcCnt < regFpcLen; fpcCnt++) {
            Infos.FPCInfoAction fpcInfoAction = new Infos.FPCInfoAction();
            fpcInfoAction.setActionType(BizConstant.SP_FPCINFO_DELETE);
            fpcInfoAction.setStrFPCInfo(fpcInfoGetDROut.get(fpcCnt));
            strFPCInfoActionList.add(fpcInfoAction);
        }
        //【step6】 FPC_infoRegist_Event_Make__101
        eventMethod.fpcInfoRegistEventMake(objCommon, objCommon.getTransactionID(), strFPCInfoActionList, strDOCLotRemoveReqInParm.getClaimMemo(), strDOCLotRemoveReqInParm.getRunCardID());
        out.setFpcIDs(fpcIDs);
        out.setLotFamilyID(lotFamilyID);
        return out;
    }
}
