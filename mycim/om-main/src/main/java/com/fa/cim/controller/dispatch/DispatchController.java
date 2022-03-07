package com.fa.cim.controller.dispatch;

import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.*;
import com.fa.cim.common.utils.*;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.controller.interfaces.dispatch.IDispatchController;
import com.fa.cim.controller.post.PostController;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;
import com.fa.cim.frameworks.pprocess.api.annotations.EnablePostProcess;
import com.fa.cim.method.IControlJobMethod;
import com.fa.cim.method.IEquipmentMethod;
import com.fa.cim.method.IQTimeMethod;
import com.fa.cim.method.IUtilsComp;
import com.fa.cim.middleware.standard.api.annotations.listener.CimMapping;
import com.fa.cim.middleware.standard.api.annotations.listener.Listenable;
import com.fa.cim.newcore.impl.bo.env.StandardProperties;
import com.fa.cim.service.access.IAccessInqService;
import com.fa.cim.service.constraint.IConstraintService;
import com.fa.cim.service.dispatch.IDispatchService;
import com.fa.cim.service.post.IPostService;
import lombok.extern.slf4j.Slf4j;
import org.bytesoft.compensable.Compensable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/7/29          ********            Nyx                create file
 *
 * @author: Nyx
 * @date: 2019/7/29 13:39
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@RestController
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
@Compensable(interfaceClass = IDispatchController.class, confirmableKey = "DispatchConfirm", cancellableKey = "DispatchCancel")
@RequestMapping("/dispatch")
@Listenable
public class DispatchController implements IDispatchController {

    @Autowired
    private IUtilsComp utilsComp;

    @Autowired
    private IEquipmentMethod equipmentMethod;

    @Autowired
    private RetCodeConfig retCodeConfig;

    @Autowired
    private IAccessInqService accessInqService;

    @Autowired
    private IConstraintService constraintService;

    @Autowired
    private IQTimeMethod qTimeMethod;

    @Autowired
    private IControlJobMethod controlJobMethod;

    @Autowired
    private IDispatchService dispatchService;

    @Autowired
    private IPostService postService;

    @Autowired
    private PostController postController;

    @ResponseBody
    @RequestMapping(value = "/old/move_in_reserve_cancel/req", method = RequestMethod.POST)
    //@CimMapping(TransactionIDEnum.START_LOTS_RESERVATION_CANCEL_REQ)
    public Response moveInReserveCancelReqOld(@RequestBody Params.MoveInReserveCancelReqParams params) {
        //【step0】init params
        final String transactionID = TransactionIDEnum.START_LOTS_RESERVATION_CANCEL_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);
        User user = params.getUser();

        //【step1】get schedule from calendar
        Infos.ObjCommon objCommon = utilsComp.setObjCommon(TransactionIDEnum.START_LOTS_RESERVATION_CANCEL_REQ, user);

        //【step2】controlJob_lotIDList_GetDR
        List<ObjectIdentifier> lotIDList = new ArrayList<>();
        String tmpPrivilegeCheckCJValue = StandardProperties.OM_ACCESS_CHECK_FOR_CJ.getValue();
        if (!CimStringUtils.isEmpty(tmpPrivilegeCheckCJValue) && CimStringUtils.equals("1", tmpPrivilegeCheckCJValue)) {
            lotIDList = controlJobMethod.controlJobLotIDListGetDR(objCommon, params.getControlJobID());
        }
        //【step3】call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setEquipmentID(params.getEquipmentID());
        accessControlCheckInqParams.setLotIDLists(lotIDList);
        accessInqService.sxAccessControlCheckInq(objCommon, accessControlCheckInqParams);

        //【step4】call txMoveInReserveCancelReq
        Results.MoveInReserveCancelReqResult result = dispatchService.sxMoveInReserveCancelReqService(objCommon, params);
        //【step5】call txPostTaskRegisterReq__100
        int castLen = CimArrayUtils.getSize(result.getStartCassetteList());
        List<ObjectIdentifier> tempLotIDs = new ArrayList<>();
        int lotCnt = 0;
        ;
        for (int i = 0; i < castLen; i++) {
            /*===== Omit Empty Cassette =====*/
            Infos.StartCassette startCassette = result.getStartCassetteList().get(i);
            if (CimStringUtils.equals(startCassette.getLoadPurposeType(), BizConstant.SP_LOADPURPOSETYPE_EMPTYCASSETTE)) {
                continue;
            }
            List<Infos.LotInCassette> lotInCassetteList = startCassette.getLotInCassetteList();
            int lotLen = CimArrayUtils.getSize(lotInCassetteList);
            for (int j = 0; j < lotLen; j++) {
                /*===== Omit Not-Cancel Lot =====*/
                Infos.LotInCassette lotInCassette = lotInCassetteList.get(j);
                if (!lotInCassette.getMoveInFlag()) {
                    continue;
                }
                tempLotIDs.add(lotInCassette.getLotID());
                lotCnt++;
            }
        }
        Results.PostTaskRegisterReqResult postTaskRegisterReqResult = null;
        if (lotCnt > 0) {
            Params.PostTaskRegisterReqParams postTaskRegisterReqParams = new Params.PostTaskRegisterReqParams();
            postTaskRegisterReqParams.setTransactionID(objCommon.getTransactionID());
            postTaskRegisterReqParams.setSequenceNumber(-1);
            Infos.PostProcessRegistrationParam postProcessRegistrationParm = new Infos.PostProcessRegistrationParam();
            postTaskRegisterReqParams.setPostProcessRegistrationParm(postProcessRegistrationParm);
            postProcessRegistrationParm.setLotIDs(tempLotIDs);
            postProcessRegistrationParm.setEquipmentID(params.getEquipmentID());
            postTaskRegisterReqParams.setClaimMemo(params.getOpeMemo());
            postTaskRegisterReqResult = postService.sxPostTaskRegisterReq(objCommon, postTaskRegisterReqParams);
        }
        Params.PostTaskExecuteReqParams postTaskExecuteReqParams = new Params.PostTaskExecuteReqParams();
        postTaskExecuteReqParams.setUser(params.getUser());
        postTaskExecuteReqParams.setKey(postTaskRegisterReqResult == null ? null : postTaskRegisterReqResult.getDKey());
        postTaskExecuteReqParams.setSyncFlag(1);
        postTaskExecuteReqParams.setPreviousSequenceNumber(0);
        postController.postTaskExecuteReq(postTaskExecuteReqParams);

        // TODO txAPCCJRpt
        //【step8】judge whether the return code is success, if no, then TCC will rollback
        return Response.createSucc(transactionID, result);
    }

    @Override
    @ResponseBody
    @RequestMapping(value = "/move_in_reserve_cancel/req", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.START_LOTS_RESERVATION_CANCEL_REQ)
    @EnablePostProcess
    public Response moveInReserveCancelReq(@RequestBody Params.MoveInReserveCancelReqParams params) {
        //【step0】init params
        final String transactionID = TransactionIDEnum.START_LOTS_RESERVATION_CANCEL_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        User user = params.getUser();
        Validations.check(null == user, retCodeConfig.getInvalidParameter());

        //【step1】get schedule from calendar
        Infos.ObjCommon objCommon = utilsComp.setObjCommon(TransactionIDEnum.START_LOTS_RESERVATION_CANCEL_REQ, user);

        //【step2】controlJob_lotIDList_GetDR
        List<ObjectIdentifier> lotIDList = new ArrayList<>();
        int accessCheckForCjIntValue = StandardProperties.OM_ACCESS_CHECK_FOR_CJ.getIntValue();
        if (accessCheckForCjIntValue == 1) {
            lotIDList = controlJobMethod.controlJobLotIDListGetDR(objCommon, params.getControlJobID());
        }

        //【step3】call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setEquipmentID(params.getEquipmentID());
        accessControlCheckInqParams.setLotIDLists(lotIDList);
        accessInqService.sxAccessControlCheckInq(objCommon, accessControlCheckInqParams);

        //【step4】call txMoveInReserveCancelReq
        Results.MoveInReserveCancelReqResult result = dispatchService.sxMoveInReserveCancelReqService(objCommon, params);

        // TODO txAPCCJRpt
        //【step8】judge whether the return code is success, if no, then TCC will rollback
        return Response.createSucc(transactionID, result);

        //----------------------------
        // Post Process
        //----------------------------
    }

    @ResponseBody
    @RequestMapping(value = "/move_in_reserve/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.START_LOTS_RESERVATION_REQ)
    @EnablePostProcess
    public Response moveInReserveReq(@RequestBody Params.MoveInReserveReqParams moveInReserveReqParams) {
        //【step0】init params
        final String transactionID = TransactionIDEnum.START_LOTS_RESERVATION_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);
        User user = moveInReserveReqParams.getUser();

        //【step2】call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        List<ObjectIdentifier> lotIDLists = new ArrayList<>();
        int startCassetteSize = CimArrayUtils.getSize(moveInReserveReqParams.getStartCassetteList());
        for (int i = 0; i < startCassetteSize; i++) {
            Infos.StartCassette startCassette = moveInReserveReqParams.getStartCassetteList().get(i);
            int lotInCassetteSize = CimArrayUtils.getSize(startCassette.getLotInCassetteList());
            for (int j = 0; j < lotInCassetteSize; j++) {
                Infos.LotInCassette lotInCassette = startCassette.getLotInCassetteList().get(j);
                if (CimBooleanUtils.isTrue(lotInCassette.getMoveInFlag()) && !lotIDLists.contains(lotInCassette.getLotID())) {
                    qTimeMethod.checkMMQTime(lotInCassette.getLotID(), false);
                    lotIDLists.add(lotInCassette.getLotID());
                }
            }
        }
        accessControlCheckInqParams.setLotIDLists(lotIDLists);
        accessControlCheckInqParams.setEquipmentID(moveInReserveReqParams.getEquipmentID());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, user, accessControlCheckInqParams);

        //【step3】call txMoveInReserveReq
        Results.MoveInReserveReqResult result;
        //--------------------------------
        // Entity Inhibit
        //--------------------------------
        //【step5】call txMfgRestrictReq_101

        int envInhibitWhenAPC = StandardProperties.OM_CONSTRAINT_APC_RPARM_CHG_ERROR.getIntValue();
        try {
            result = (Results.MoveInReserveReqResult) dispatchService.sxMoveInReserveReq(objCommon, moveInReserveReqParams);
        } catch (ServiceException ex) {
//            if (envInhibitWhenAPC == 1) {
//                if (Validations.isEquals( retCodeConfig.getNoResponseApc(),ex.getCode())
//                        || Validations.isEquals( retCodeConfig.getApcServerBindFail(),ex.getCode())
//                        || Validations.isEquals( retCodeConfig.getApcRuntimecapabilityError(),ex.getCode())
//                        || Validations.isEquals( retCodeConfig.getApcRecipeparameterreqError(),ex.getCode())
//                        || Validations.isEquals( retCodeConfig.getApcReturnDuplicateParametername(),ex.getCode())) {
//                    Infos.EntityInhibitDetailAttributes entityInhibitAttributes = new Infos.EntityInhibitDetailAttributes();
//                    List<Infos.EntityIdentifier> entities = new ArrayList<>();
//                    entityInhibitAttributes.setEntities(entities);
//                    Infos.EntityIdentifier entityIdentifier = new Infos.EntityIdentifier();
//                    entities.add(entityIdentifier);
//                    entityIdentifier.setClassName(BizConstant.SP_INHIBITCLASSID_EQUIPMENT);
//                    entityIdentifier.setObjectID(moveInReserveReqParams.getEquipmentID());
//                    entityIdentifier.setAttribution("");
//                    entityInhibitAttributes.setStartTimeStamp(DateUtils.parseTimestampToSpecFormat(objCommon.getTimeStamp().getReportTimeStamp()));
//                    if (Validations.isEquals( retCodeConfig.getNoResponseApc(),ex.getCode())
//                            || Validations.isEquals( retCodeConfig.getApcServerBindFail(),ex.getCode())) {
//                        entityInhibitAttributes.setReasonCode(BizConstant.SP_REASON_ENTITYINHIBIT_FOR_APCNORESPONSE);
//                    } else if (Validations.isEquals( retCodeConfig.getApcRuntimecapabilityError(),ex.getCode())
//                            || Validations.isEquals( retCodeConfig.getApcRecipeparameterreqError(),ex.getCode())) {
//                        entityInhibitAttributes.setReasonCode(BizConstant.SP_REASON_ENTITYINHIBIT_FOR_APCRETURNCODENG);
//                    } else if (Validations.isEquals( retCodeConfig.getApcReturnDuplicateParametername(),ex.getCode())) {
//                        entityInhibitAttributes.setReasonCode(BizConstant.SP_REASON_ENTITYINHIBIT_FOR_APCRETURNVALUEERROR);
//                    }
//                    entityInhibitAttributes.setOwnerID(objCommon.getUser().getUserID());
//                    entityInhibitAttributes.setClaimedTimeStamp("");
//                    Params.MfgRestrictReqParams mfgRestrictReqParams = new Params.MfgRestrictReqParams();
//                    mfgRestrictReqParams.setEntityInhibitDetailAttributes(entityInhibitAttributes);
//                    constraintService.sxMfgRestrictReq(mfgRestrictReqParams, objCommon);
//                }else {
            throw ex;
//                }
//            }
        }
        return Response.createSucc(transactionID, result);
    }


    @ResponseBody
    @RequestMapping(value = "/old/move_in_reserve_cancel_for_ib/req", method = RequestMethod.POST)
    //@CimMapping(TransactionIDEnum.START_LOTS_RESERVATION_CANCEL_FOR_INTERNAL_BUFFER_REQ)
    public Response moveInReserveCancelForIBReqOld(@RequestBody Params.MoveInReserveCancelForIBReqParams params) {
        List<Infos.EventParameter> strEventParameter = new ArrayList<>();
        //【step0】Initialising strObjCommonIn's first two parameters
        final String transactionID = TransactionIDEnum.START_LOTS_RESERVATION_CANCEL_FOR_INTERNAL_BUFFER_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);
        User user = params.getUser();

        //【step0】check input params
        Validations.check(null == user, "the user info is null...");

        //【step1】get schedule from calendar
        Infos.ObjCommon objCommon = utilsComp.setObjCommon(transactionID, user);

        //【step2】controlJob_lotIDList_GetDR
        List<ObjectIdentifier> lotIDList = new ArrayList<>();
        String tmpPrivilegeCheckCJValue = StandardProperties.OM_ACCESS_CHECK_FOR_CJ.getValue();
        if (CimStringUtils.equals("1", tmpPrivilegeCheckCJValue)) {
            lotIDList = controlJobMethod.controlJobLotIDListGetDR(objCommon, params.getControlJobID());
        }

        //【step3】call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setEquipmentID(params.getEquipmentID());
        accessControlCheckInqParams.setLotIDLists(lotIDList);
        accessInqService.sxAccessControlCheckInq(objCommon, accessControlCheckInqParams);

        //【step4】call sxMoveInReserveCancelForIBReqService
        //-----------------------------------------------------------------------
        //   Main Process
        //-----------------------------------------------------------------------
        Results.MoveInReserveCancelForIBReqResult result = dispatchService.sxMoveInReserveCancelForIBReqService(objCommon, params);


        List<Infos.StartCassette> startCassetteList = result.getStartCassetteList();
        List<ObjectIdentifier> tempLotIDList = new ArrayList<>();
        for (int i = 0; i < CimArrayUtils.getSize(startCassetteList); i++) {
            Infos.StartCassette startCassette = startCassetteList.get(i);
            if (CimStringUtils.equals(startCassette.getLoadPurposeType(), BizConstant.SP_LOADPURPOSETYPE_EMPTYCASSETTE)) {
                continue;
            }

            for (int j = 0; j < CimArrayUtils.getSize(startCassette.getLotInCassetteList()); j++) {
                Infos.LotInCassette lotInCassette = startCassette.getLotInCassetteList().get(j);
                if (CimBooleanUtils.isFalse(lotInCassette.getMoveInFlag())) {
                    continue;
                }
                tempLotIDList.add(lotInCassette.getLotID());
            }
        }

        Results.PostTaskRegisterReqResult postTaskRegisterReqResult = new Results.PostTaskRegisterReqResult();
        if (CimArrayUtils.isNotEmpty(tempLotIDList)) {
            Params.PostTaskRegisterReqParams postTaskRegisterReqParams = new Params.PostTaskRegisterReqParams();
            Infos.PostProcessRegistrationParam postProcessRegistrationParam = new Infos.PostProcessRegistrationParam();
            postProcessRegistrationParam.setLotIDs(tempLotIDList);
            postProcessRegistrationParam.setEquipmentID(params.getEquipmentID());
            postTaskRegisterReqParams.setPostProcessRegistrationParm(postProcessRegistrationParam);
            postTaskRegisterReqParams.setSequenceNumber(-1);
            postTaskRegisterReqParams.setTransactionID(objCommon.getTransactionID());
            postTaskRegisterReqParams.setClaimMemo(params.getOpeMemo());

            //step5 - txPostTaskRegisterReq__100
            postTaskRegisterReqResult = postService.sxPostTaskRegisterReq(objCommon, postTaskRegisterReqParams);
            log.debug("txPostTaskRegisterReq__100 :rc == RC_OK");
        }

        Params.PostTaskExecuteReqParams postTaskExecuteReqParams = new Params.PostTaskExecuteReqParams();
        postTaskExecuteReqParams.setUser(params.getUser());
        postTaskExecuteReqParams.setKey(postTaskRegisterReqResult != null ? postTaskRegisterReqResult.getDKey() : "");
        postTaskExecuteReqParams.setSyncFlag(1);
        postTaskExecuteReqParams.setPreviousSequenceNumber(0);
        postTaskExecuteReqParams.setKeyTimeStamp(null);
        //Step6 - TxPostTaskExecuteReq__100
        Response postTaskExecuteReq = postController.postTaskExecuteReq(postTaskExecuteReqParams);

        /*

        int castLen = result.getObject().getStartCassetteList().size();
        if(castLen>0){
            int count = 0;
            List<Infos.StartCassette> startCassetteList = result.getObject().getStartCassetteList();
            StringBuffer strCastID = new StringBuffer("\0");
            StringBuffer strLPortID = new StringBuffer("\0");
            StringBuffer strUPortID = new StringBuffer("\0");
            for (count = 0; count < castLen; count++ ){
                if(count==castLen-1){
                    strCastID.append(startCassetteList.get(count).getCassetteID().getValue()).append("\0");
                    strLPortID.append(startCassetteList.get(count).getLoadPortID().getValue()).append("\0");
                    strUPortID.append(startCassetteList.get(count).getUnloadPortID().getValue()).append("\0");
                }else {
                  strCastID.append(startCassetteList.get(count).getCassetteID().getValue()).append(",");
                  strLPortID.append(startCassetteList.get(count).getLoadPortID().getValue()).append(",");
                  strUPortID.append(startCassetteList.get(count).getUnloadPortID().getValue()).append(",");
                }
            }
            Infos.EventParameter eventParameter = new Infos.EventParameter();
            eventParameter.setParameterName("LOAD_PORT_ID");
            eventParameter.setParameterValue(strLPortID.toString());
            strEventParameter.add(eventParameter);
            eventParameter.setParameterName("UNLOAD_PORT_ID");
            eventParameter.setParameterValue(strUPortID.toString());
            strEventParameter.add(eventParameter);
            eventParameter.setParameterName("CAST_ID");
            eventParameter.setParameterValue(strCastID.toString());
            strEventParameter.add(eventParameter);
        }
        */
        //【step9】judge whether the return code is success, if no, then TCC will rollback
        return Response.createSucc(transactionID, result);
    }

    @Override
    @ResponseBody
    @RequestMapping(value = "/move_in_reserve_cancel_for_ib/req", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.START_LOTS_RESERVATION_CANCEL_FOR_INTERNAL_BUFFER_REQ)
    @EnablePostProcess
    public Response moveInReserveCancelForIBReq(@RequestBody Params.MoveInReserveCancelForIBReqParams params) {
        //【step0】Initialising strObjCommonIn's first two parameters
        final String transactionID = TransactionIDEnum.START_LOTS_RESERVATION_CANCEL_FOR_INTERNAL_BUFFER_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        User user = params.getUser();
        //【step0】check input params
        Validations.check(null == user, "the user info is null...");

        //【step1】get schedule from calendar
        Infos.ObjCommon objCommon = utilsComp.setObjCommon(transactionID, user);

        //【step2】controlJob_lotIDList_GetDR
        List<ObjectIdentifier> lotIDList = new ArrayList<>();
        int accessCheckForCjIntValue = StandardProperties.OM_ACCESS_CHECK_FOR_CJ.getIntValue();
        if (accessCheckForCjIntValue == 1) {
            lotIDList = controlJobMethod.controlJobLotIDListGetDR(objCommon, params.getControlJobID());
        }

        //【step3】call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setEquipmentID(params.getEquipmentID());
        accessControlCheckInqParams.setLotIDLists(lotIDList);
        accessInqService.sxAccessControlCheckInq(objCommon, accessControlCheckInqParams);

        //【step4】call sxMoveInReserveCancelForIBReqService
        //-----------------------------------------------------------------------
        //   Main Process
        //-----------------------------------------------------------------------
        Results.MoveInReserveCancelForIBReqResult result = dispatchService.sxMoveInReserveCancelForIBReqService(
                objCommon,
                params);

        //【step9】judge whether the return code is success, if no, then TCC will rollback
        return Response.createSucc(transactionID, result);

        //-----------------------------
        // Post Process
        //-----------------------------
    }

    @ResponseBody
    @PostMapping("/move_in_reserve_for_ib/req")
    @Override
    @CimMapping(TransactionIDEnum.START_LOTS_RESERVATION_FOR_INTERNAL_BUFFER_REQ)
    @EnablePostProcess
    public Response moveInReserveForIBReq(@RequestBody Params.MoveInReserveForIBReqParams params) {
        String txId = TransactionIDEnum.START_LOTS_RESERVATION_FOR_INTERNAL_BUFFER_REQ.getValue();
        ThreadContextHolder.setTransactionId(txId);

        //Step1 calendar_GetCurrentTimeDR get schedule from calendar
        //Step2 - txAccessControlCheckInq
        Params.AccessControlCheckInqParams privilegeCheckParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, params.getUser(), privilegeCheckParams);

        String apcifControlStatus = new String();
        //Step3 - txMoveInReserveForIBReq
        ObjectIdentifier result;
        try {
            result = dispatchService.sxMoveInReserveForIBReq(objCommon, params, apcifControlStatus);
        } catch (ServiceException ex) {
            String rollBackNotifyToApc = StandardProperties.OM_APC_NOTIFY_RESEND.getValue();
            if (apcifControlStatus != null && CimStringUtils.equals(rollBackNotifyToApc, "1")) {

                //TODO-NOTIMPL:Step4 - txAPCCJRpt

            }
            String envInhibitWhenAPC = StandardProperties.OM_CONSTRAINT_APC_RPARM_CHG_ERROR.getValue();
            if (CimStringUtils.equals(envInhibitWhenAPC, "1")) {
                if (Validations.isEquals(retCodeConfig.getNoResponseApc(), ex.getCode())
                        || Validations.isEquals(retCodeConfig.getApcServerBindFail(), ex.getCode())
                        || Validations.isEquals(retCodeConfig.getApcRuntimecapabilityError(), ex.getCode())
                        || Validations.isEquals(retCodeConfig.getApcRecipeparameterreqError(), ex.getCode())
                        || Validations.isEquals(retCodeConfig.getApcReturnDuplicateParametername(), ex.getCode())) {

                    Infos.EntityInhibitDetailAttributes strEntityInhibitions = new Infos.EntityInhibitDetailAttributes();
                    List<Infos.EntityIdentifier> entities = new ArrayList<>();
                    Infos.EntityIdentifier entityIdentifier = new Infos.EntityIdentifier();
                    strEntityInhibitions.setEntities(entities);
                    entities.add(entityIdentifier);
                    entityIdentifier.setClassName(BizConstant.SP_INHIBITCLASSID_EQUIPMENT);
                    entityIdentifier.setObjectID(params.getEquipmentID());
                    entityIdentifier.setAttribution("");
                    strEntityInhibitions.setStartTimeStamp(String.valueOf(objCommon.getTimeStamp().getReportTimeStamp()));

                    if (Validations.isEquals(retCodeConfig.getNoResponseApc(), ex.getCode())
                            || Validations.isEquals(retCodeConfig.getApcServerBindFail(), ex.getCode())) {
                        strEntityInhibitions.setReasonCode(BizConstant.SP_REASON_ENTITYINHIBIT_FOR_APCNORESPONSE);
                    } else if (Validations.isEquals(retCodeConfig.getApcRuntimecapabilityError(), ex.getCode())
                            || Validations.isEquals(retCodeConfig.getApcRecipeparameterreqError(), ex.getCode())) {
                        strEntityInhibitions.setReasonCode(BizConstant.SP_REASON_ENTITYINHIBIT_FOR_APCRETURNCODENG);
                    } else if (Validations.isEquals(retCodeConfig.getApcReturnDuplicateParametername(), ex.getCode())) {
                        strEntityInhibitions.setReasonCode(BizConstant.SP_REASON_ENTITYINHIBIT_FOR_APCRETURNVALUEERROR);
                    }
                    strEntityInhibitions.setOwnerID(objCommon.getUser().getUserID());
                    strEntityInhibitions.setClaimedTimeStamp("");

//                    Params.MfgRestrictReqParams mfgRestrictReqParams = new Params.MfgRestrictReqParams();
//                    mfgRestrictReqParams.setUser(objCommon.getUser());
//                    mfgRestrictReqParams.setEntityInhibitDetailAttributes(strEntityInhibitions);
//                    mfgRestrictReqParams.setClaimMemo(params.getClaimMemo());
//
//                    //Step5 - txMfgRestrictReq__101
//                    constraintService.sxMfgRestrictReq(mfgRestrictReqParams, objCommon);

                    //Step5 - txMfgRestrictReq__110
                    Params.MfgRestrictReq_110Params mfgRestrictReq_110Params = new Params.MfgRestrictReq_110Params();
                    List<Infos.EntityInhibitDetailAttributes> entityInhibitDetailAttributeList = new ArrayList<>();
                    entityInhibitDetailAttributeList.add(strEntityInhibitions);
                    mfgRestrictReq_110Params.setClaimMemo(params.getOpeMemo());
                    mfgRestrictReq_110Params.setUser(objCommon.getUser());
                    mfgRestrictReq_110Params.setEntityInhibitDetailAttributeList(entityInhibitDetailAttributeList);
                    constraintService.sxMfgRestrictReq_110(mfgRestrictReq_110Params,objCommon);
                }
            }
            throw ex;
        }

        //-------------------------------------------------------------------
        // save ControlJob Information into InputParameters for PostProcess
        //-------------------------------------------------------------------
        params.setControlJobID(result);

        return Response.createSucc(txId, result);
    }

    @ResponseBody
    @RequestMapping(value = "/auto_dispatch_config_modify/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.AUTO_DISPATCH_CONTROL_UPDATE_REQ)
    public Response autoDispatchConfigModifyReq(@RequestBody Params.AutoDispatchConfigModifyReqParams autoDispatchConfigModifyReqParams) {
        //【step0】init params
        final String transactionID = TransactionIDEnum.AUTO_DISPATCH_CONTROL_UPDATE_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //【step0】check input params
        User user = autoDispatchConfigModifyReqParams.getUser();
        List<Infos.LotAutoDispatchControlUpdateInfo> lotAutoDispatchControlUpdateInfoList = autoDispatchConfigModifyReqParams.getLotAutoDispatchControlUpdateInfoList();
        Validations.check(CimObjectUtils.isEmpty(lotAutoDispatchControlUpdateInfoList), "lotIDs is null");
        //【step1】 call txAccessControlCheckInq(...)
        List<ObjectIdentifier> lotIDs = new ArrayList<>();
        for (Infos.LotAutoDispatchControlUpdateInfo lotAutoDispatchControlUpdateInfo : lotAutoDispatchControlUpdateInfoList) {
            lotIDs.add(lotAutoDispatchControlUpdateInfo.getLotID());
        }
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setLotIDLists(lotIDs);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, user, accessControlCheckInqParams);
        //【step2】call sxAutoDispatchConfigModifyReq(...)
        Results.AutoDispatchConfigModifyReqResult result = dispatchService.sxAutoDispatchConfigModifyReq(objCommon, autoDispatchConfigModifyReqParams);
        return Response.createSucc(transactionID, result);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @RequestMapping(value = "/carrier_dispatch_attr_chg/req", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.CASSETTE_DISPATCH_ATTRIBUTE_UPDATE_REQ)
    public Response carrierDispatchAttrChgReq(@RequestBody Params.CarrierDispatchAttrChgReqParm carrierDispatchAttrChgReqParm) {
        final String transactionID = TransactionIDEnum.CASSETTE_DISPATCH_ATTRIBUTE_UPDATE_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        User user = carrierDispatchAttrChgReqParm.getUser();
        if (user == null) {
            return Response.createError(new ErrorCode("user can not be null"), transactionID);
        }
        //step1 - call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, user, accessControlCheckInqParams);


        //step2 - txReticleUsageCountResetReq
        dispatchService.sxCarrierDispatchAttrChgReq(objCommon, carrierDispatchAttrChgReqParm);

        return Response.createSucc(objCommon.getTransactionID(), null);
    }

    @ResponseBody
    @RequestMapping(value = "/eqp_full_auto_config_chg/req", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.EQP_AUTO_3SETTING_UPDATE_REQ)
    public Response eqpFullAutoConfigChgReq(@RequestBody Params.EqpFullAutoConfigChgReqInParm params) {
        //step1 init params
        final String txId = TransactionIDEnum.EQP_AUTO_3SETTING_UPDATE_REQ.getValue();
        User user = params.getUser();

        //step2 -  call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, user, accessControlCheckInqParams);

        Results.EqpFullAutoConfigChgReqResult eqpFullAutoConfigChgReqResult = new Results.EqpFullAutoConfigChgReqResult();
        List<Results.EqpAuto3SettingUpdateResult> eqpAuto3SettingUpdateResults = new ArrayList<>();

        //===============
        // Main Process
        //===============
        Params.EqpFullAutoConfigChgReqInParm eqpFullAutoConfigChgReqInParm = new Params.EqpFullAutoConfigChgReqInParm();
        eqpFullAutoConfigChgReqInParm.setUpdateMode(params.getUpdateMode());
        eqpFullAutoConfigChgReqInParm.setUser(params.getUser());
        String reason = " LoadReq and UnloadReq event watchdog name should be the same.";
        Long failCnt = 0L;
        List<Infos.EqpAuto3SettingInfo> eqpAuto3SettingInfos = params.getEqpAuto3SettingInfo();
        List<Infos.EqpAuto3SettingInfo> newEqpAuto3SettingInfos = new ArrayList<>();
        for (Infos.EqpAuto3SettingInfo eqpAuto3SettingInfo : eqpAuto3SettingInfos) {
            Results.EqpAuto3SettingUpdateResult eqpAuto3SettingUpdateResult = new Results.EqpAuto3SettingUpdateResult();
            String loadReqWd = null;
            String unloadReqWd = null;
            for (Infos.EqpAuto3SettingInfo compareEqpAuto3SettingInfo : eqpAuto3SettingInfos) {
                if (ObjectIdentifier.equalsWithValue(eqpAuto3SettingInfo.getEqpID(), compareEqpAuto3SettingInfo.getEqpID())) {
                    if (CimStringUtils.equals(compareEqpAuto3SettingInfo.getCarrierTransferRequestEvent(), BizConstant.SP_EQPAUTO3SETTING_EVENTTYPE_LOADREQ)) {
                        loadReqWd = compareEqpAuto3SettingInfo.getWatchdogName();
                    } else if (CimStringUtils.equals(compareEqpAuto3SettingInfo.getCarrierTransferRequestEvent(), BizConstant.SP_EQPAUTO3SETTING_EVENTTYPE_UNLOADREQ)) {
                        unloadReqWd = compareEqpAuto3SettingInfo.getWatchdogName();
                    }
                }
            }
            if (CimStringUtils.equals(params.getUpdateMode(), BizConstant.SP_EQPAUTO3SETTING_UPDATEMODE_UPDATE)) {
                //===========================================================================
                // Check watchdog name
                //  If event name is UnloadReq or LoadReq, they should share the same watchdog
                //===========================================================================
                // If inPara event is not UnloadReq or LoadReq, no need check loadReqWd = unloadReqWd = blank
                // If inPara event watchdog name is the same, they can be updated
                if (CimStringUtils.equals(loadReqWd, unloadReqWd)) {
                    log.info("loadReqWd = unloadReqWd");
                    newEqpAuto3SettingInfos.add(eqpAuto3SettingInfo);
                } else if (CimStringUtils.isNotEmpty(loadReqWd) && CimStringUtils.isNotEmpty(unloadReqWd) && !CimStringUtils.equals(loadReqWd, unloadReqWd)) {
                    log.info("loadReqWd != unloadReqWd");
                    eqpAuto3SettingUpdateResult.setEqpID(eqpAuto3SettingInfo.getEqpID());
                    eqpAuto3SettingUpdateResult.setStrResult(new RetCode(TransactionIDEnum.EQP_AUTO_3SETTING_UPDATE_REQ.getValue(), new OmCode(retCodeConfig.getInvalidParameterWithMsg(), reason), null));
                    eqpAuto3SettingUpdateResults.add(eqpAuto3SettingUpdateResult);
                    failCnt++;
                } else {
                    //if inPara event contains only LoadReq or UnloadReq, need to compare with the one in db
                    // get from db
                    Boolean dbCheckedFlag = false;
                    Boolean existingFlag = false;
                    List<ObjectIdentifier> eqpIDs = new ArrayList<>();
                    eqpIDs.add(eqpAuto3SettingInfo.getEqpID());
                    List<Infos.EqpAuto3SettingInfo> dbEqpAuto3SettingInfos = equipmentMethod.equipmentAuto3DispatchSettingListGetDR(objCommon, eqpIDs);
                    if (CimObjectUtils.isEmpty(dbEqpAuto3SettingInfos)) {
                        // no existing non-updated event
                        dbCheckedFlag = true;
                    } else {
                        //compare the db data with the inParam data
                        for (Infos.EqpAuto3SettingInfo dbEqpAuto3SettingInfo : dbEqpAuto3SettingInfos) {
                            if (CimStringUtils.isEmpty(unloadReqWd) && CimStringUtils.equals(dbEqpAuto3SettingInfo.getCarrierTransferRequestEvent(), BizConstant.SP_EQPAUTO3SETTING_EVENTTYPE_UNLOADREQ)) {
                                existingFlag = true;
                                unloadReqWd = dbEqpAuto3SettingInfo.getWatchdogName();
                            } else if (CimStringUtils.isEmpty(loadReqWd) && CimStringUtils.equals(dbEqpAuto3SettingInfo.getCarrierTransferRequestEvent(), BizConstant.SP_EQPAUTO3SETTING_EVENTTYPE_LOADREQ)) {
                                existingFlag = true;
                                loadReqWd = dbEqpAuto3SettingInfo.getWatchdogName();
                            }
                        }
                        if (!existingFlag) {
                            // no existing non-updated event
                            dbCheckedFlag = true;
                        } else if (CimStringUtils.equals(unloadReqWd, loadReqWd)) {
                            log.info("Check OK");
                            dbCheckedFlag = true;
                        } else {
                            log.info("Check Failure, not updated");
                        }
                    }
                    if (dbCheckedFlag) {
                        //check pass, save the params
                        newEqpAuto3SettingInfos.add(eqpAuto3SettingInfo);
                    } else {
                        //check failed
                        eqpAuto3SettingUpdateResult.setEqpID(eqpAuto3SettingInfo.getEqpID());
                        eqpAuto3SettingUpdateResult.setStrResult(new RetCode(TransactionIDEnum.EQP_AUTO_3SETTING_UPDATE_REQ.getValue(), new OmCode(retCodeConfig.getInvalidParameterWithMsg(), reason), null));
                        eqpAuto3SettingUpdateResults.add(eqpAuto3SettingUpdateResult);
                        failCnt++;
                    }
                }
            }
        }

        if (CimStringUtils.equals(params.getUpdateMode(), BizConstant.SP_EQPAUTO3SETTING_UPDATEMODE_UPDATE)) {
            eqpFullAutoConfigChgReqInParm.setEqpAuto3SettingInfo(newEqpAuto3SettingInfos);
        } else {
            //not update,no need to check the LoadReq or UnloadReq,just use the params
            eqpFullAutoConfigChgReqInParm.setEqpAuto3SettingInfo(eqpAuto3SettingInfos);
        }
        Results.EqpFullAutoConfigChgReqResult runEqpFullAutoConfigChgReqResult = dispatchService.sxEqpFullAutoConfigChgReq(objCommon, eqpFullAutoConfigChgReqInParm);
        List<Results.EqpAuto3SettingUpdateResult> runEqpAuto3SettingUpdateResults = runEqpFullAutoConfigChgReqResult.getEqpAuto3SettingUpdateResults();
        for (Results.EqpAuto3SettingUpdateResult runEqpAuto3SettingUpdateResult : runEqpAuto3SettingUpdateResults) {
            if (runEqpAuto3SettingUpdateResult.getStrResult().getReturnCode().getCode() != 0) {
                failCnt++;
            }
        }
        eqpAuto3SettingUpdateResults.addAll(runEqpAuto3SettingUpdateResults);
        List<Results.EqpAuto3SettingUpdateResult> finalEqpAuto3SettingUpdateResult = eqpAuto3SettingUpdateResults.stream().distinct().collect(Collectors.toList());
        eqpFullAutoConfigChgReqResult.setEqpAuto3SettingUpdateResults(finalEqpAuto3SettingUpdateResult);
        Response response;
        if (failCnt > 0) {
            response = Response.createSuccWithOmCode(txId, retCodeConfig.getSomeRequestsFailed(), eqpFullAutoConfigChgReqResult);
        } else {
            response = Response.createSucc(txId, eqpFullAutoConfigChgReqResult);
        }
        return response;
    }

    @ResponseBody
    @RequestMapping(value = "/eqp_auto_move_in_reserve/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.EQP_AUTO_MOVE_IN_RESERVE_REQ)
    @EnablePostProcess
    public Response eqpAutoMoveInReserveReq(@RequestBody Params.EqpAutoMoveInReserveReqParams params) {
        //【step0】init params
        final String transactionID = TransactionIDEnum.EQP_AUTO_MOVE_IN_RESERVE_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //【step1】 call checkPrivilegeAndGetObjCommon
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setEquipmentID(params.getEquipmentID());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), accessControlCheckInqParams);
        //【step2】call sxEqpAutoMoveInReserveReq
        final Results.EqpAutoMoveInReserveReqResult result = dispatchService.sxEqpAutoMoveInReserveReq(objCommon, params);
        return Response.createSucc(transactionID, result);
    }
}