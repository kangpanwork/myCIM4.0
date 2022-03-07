package com.fa.cim.controller.processcontrol;

import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.*;
import com.fa.cim.common.utils.*;
import com.fa.cim.controller.interfaces.processControl.IProcessController;
import com.fa.cim.controller.post.PostController;
import com.fa.cim.dto.*;
import com.fa.cim.frameworks.pprocess.api.annotations.EnablePostProcess;
import com.fa.cim.method.IQTimeMethod;
import com.fa.cim.middleware.standard.api.annotations.listener.CimMapping;
import com.fa.cim.middleware.standard.api.annotations.listener.Listenable;
import com.fa.cim.service.access.IAccessInqService;
import com.fa.cim.service.lot.ILotInqService;
import com.fa.cim.service.lot.ILotService;
import com.fa.cim.service.post.IPostService;
import com.fa.cim.service.processcontrol.IProcessControlInqService;
import com.fa.cim.service.processcontrol.IProcessControlService;
import com.google.common.collect.ImmutableList;
import lombok.extern.slf4j.Slf4j;
import org.bytesoft.compensable.Compensable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.fa.cim.common.constant.TransactionIDEnum.FUTURE_REWORK_CANCEL_REQ;
import static com.fa.cim.common.constant.TransactionIDEnum.QTIME_ACTION_RESET_BY_POST_PROC_REQ;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/7/30          ********            Nyx                create file
 *
 * @author Nyx
 * @since 2019/7/30 15:46
 * Copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@RestController
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
@Compensable(interfaceClass = IProcessController.class, confirmableKey = "ProcessControlConfirm", cancellableKey = "ProcessControlCancel")
@RequestMapping("/pctrl")
@Listenable
public class ProcessController implements IProcessController {
    @Autowired
    private IAccessInqService accessInqService;
    @Autowired
    private IProcessControlService processControlService;
    @Autowired
    private IProcessControlInqService processControlInqService;
    @Autowired
    private IQTimeMethod qTimeMethod;
    @Autowired
    private IPostService postService;
    @Autowired
    private PostController postController;
    @Autowired
    private ILotInqService lotInqService;
    @Autowired
    private ILotService lotService;

    @ResponseBody
    @RequestMapping(value = "/future_hold/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.ENHANCED_FUTURE_HOLD_REQ)
    public Response futureHoldReq(@RequestBody Params.FutureHoldReqParams params) {
        //【step0】init params
        final String transactionID = TransactionIDEnum.ENHANCED_FUTURE_HOLD_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        ObjectIdentifier lotID = params.getLotID();
        String holdType = params.getHoldType();
        ObjectIdentifier reasonCodeID = params.getReasonCodeID();
        ObjectIdentifier routeID = params.getRouteID();
        String operationNumber = params.getOperationNumber();
        Validations.check(ObjectIdentifier.isEmpty(lotID) || CimObjectUtils.isEmpty(holdType) || ObjectIdentifier.isEmpty(reasonCodeID)
                || ObjectIdentifier.isEmpty(routeID) || CimStringUtils.isEmpty(operationNumber), "the parameter is null");

        //【step1】get schedule from calendar

        //step2 - call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        accessControlCheckInqParams.setLotIDLists(Arrays.asList(lotID));
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), accessControlCheckInqParams);

        //【step3】call sxFutureHoldReq(...)
        processControlService.sxFutureHoldReq(objCommon, params);

        //【step4】judge whether the return code is success, if no, then TCC will rollback
        return Response.createSucc(transactionID, null);
    }

    @ResponseBody
    @RequestMapping(value = "/future_hold_cancel/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.FUTURE_HOLD_CANCEL_REQ)
    public Response futureHoldCancelReq(@RequestBody Params.FutureHoldCancelReqParams params) {

        //【step1-a】init params
        final String transactionID = TransactionIDEnum.FUTURE_HOLD_CANCEL_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //【step1-b】check input params
        ObjectIdentifier lotID = params.getLotID();
        List<Infos.LotHoldReq> lotHoldList = params.getLotHoldList();
        Validations.check(ObjectIdentifier.isEmpty(lotID) || CimArrayUtils.isEmpty(lotHoldList), "the parameter is null");

        //【step2】check user's privilege of this function for the lot
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setLotIDLists(Arrays.asList(lotID));
        accessControlCheckInqParams.conversionDepartmentEditCheckCode(params.getLotHoldList());
        accessControlCheckInqParams.conversionDepartmentCheckCode(params.getDepartment(),
                params.getSection(), params.getReleaseReasonCodeID());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), accessControlCheckInqParams);

        //【step3】call sxFutureHoldCancelReq(...)
        processControlService.sxFutureHoldCancelReq(objCommon, params);

        //【step4】judge whether the return code is success, if no, then TCC will rollback
        return Response.createSucc(transactionID, null);
    }

    @ResponseBody
    @RequestMapping(value = "/npw_usage_state_modify/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.LOT_CTRL_STATUS_CHANGE_REQ)
    public Response npwUsageStateModifyReq(@RequestBody Params.NPWUsageStateModifyReqParams params) {
        //【step0】check input params
        ObjectIdentifier lotID = params.getLotID();
        String controlUseState = params.getControlUseState();
        Validations.check(ObjectIdentifier.isEmpty(lotID) || CimObjectUtils.isEmpty(controlUseState), "the parameter is null");

        final TransactionIDEnum transactionID = TransactionIDEnum.LOT_CTRL_STATUS_CHANGE_REQ;
        ThreadContextHolder.setTransactionId(transactionID.getValue());

        //【step1】get schedule from calendar
        //【step2】call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setLotIDLists(Arrays.asList(lotID));
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID.getValue(), params.getUser(), accessControlCheckInqParams);

        //【step3】call sxBranchReq(...)
        Results.NPWUsageStateModifyReqResult result = processControlService.sxNPWUsageStateModifyReq(objCommon, lotID, controlUseState, params.getUsageCount());

        //【step4】judge whether the return code is success, if no, then TCC will rollback
        return Response.createSucc(transactionID.getValue(), result);
    }

    @ResponseBody
    @RequestMapping(value = "/old/lag_time_action/req", method = RequestMethod.POST)
    //@CimMapping(TransactionIDEnum.PROCESS_LAG_TIME_UPDATE_REQ)
    public Response lagTimeActionReqOld(@RequestBody Params.LagTimeActionReqParams params) {
        //【step0】init params
        final String transactionID = TransactionIDEnum.PROCESS_LAG_TIME_UPDATE_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);
        //【step2】call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        List<ObjectIdentifier> lotIDLists = new ArrayList<>();
        lotIDLists.add(params.getLotID());
        accessControlCheckInqParams.setLotIDLists(lotIDLists);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), accessControlCheckInqParams);
        //【step3】main process
        processControlService.sxProcessLagTimeUpdate(objCommon, params);
        //【step4】txPostTaskRegisterReq__100
        Params.PostTaskRegisterReqParams postTaskRegisterReqParams = new Params.PostTaskRegisterReqParams();
        Infos.PostProcessRegistrationParam postProcessRegistrationParam = new Infos.PostProcessRegistrationParam();
        postProcessRegistrationParam.setLotIDs(lotIDLists);
        postTaskRegisterReqParams.setTransactionID(objCommon.getTransactionID());
        postTaskRegisterReqParams.setSequenceNumber(-1);
        postTaskRegisterReqParams.setClaimMemo(params.getClaimMemo());
        postTaskRegisterReqParams.setPostProcessRegistrationParm(postProcessRegistrationParam);
        Results.PostTaskRegisterReqResult postTaskRegisterReqResult = postService.sxPostTaskRegisterReq(objCommon, postTaskRegisterReqParams);
        //【step5】TxPostTaskExecuteReq__100
        Params.PostTaskExecuteReqParams postTaskExecuteReqParams = new Params.PostTaskExecuteReqParams();
        postTaskExecuteReqParams.setUser(params.getUser());
        postTaskExecuteReqParams.setKey(postTaskRegisterReqResult == null ? null : postTaskRegisterReqResult.getDKey());
        postTaskExecuteReqParams.setSyncFlag(1);
        postTaskExecuteReqParams.setPreviousSequenceNumber(0);
        postController.postTaskExecuteReq(postTaskExecuteReqParams);

        return Response.createSucc(transactionID);
    }

    @Override
    @ResponseBody
    @RequestMapping(value = "/lag_time_action/req", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.PROCESS_LAG_TIME_UPDATE_REQ)
    @EnablePostProcess
    public Response lagTimeActionReq(@RequestBody Params.LagTimeActionReqParams params) {
        //【step0】init params
        final String transactionID = TransactionIDEnum.PROCESS_LAG_TIME_UPDATE_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);
        //【step2】call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        List<ObjectIdentifier> lotIDLists = new ArrayList<>();
        lotIDLists.add(params.getLotID());
        accessControlCheckInqParams.setLotIDLists(lotIDLists);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(),
                accessControlCheckInqParams);
        //【step3】main process
        processControlService.sxProcessLagTimeUpdate(objCommon, params);

        return Response.createSucc(transactionID);
        //-----------------------------
        // PostProcess
        //-----------------------------
    }

    @ResponseBody
    @RequestMapping(value = "/old/qtime_do_action/req", method = RequestMethod.POST)
    //@CimMapping(TransactionIDEnum.QREST_TIME_ACTION_EXEC_REQ)
    public Response qtimeActionReqOld(@RequestBody Params.QtimeActionReqParam qtimeActionReqParam) {
        String txId = TransactionIDEnum.QREST_TIME_ACTION_EXEC_REQ.getValue();
        //set current tx id
        ThreadContextHolder.setTransactionId(txId);

        //step2 -  call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        List<ObjectIdentifier> lotIDLists = new ArrayList<>();
        lotIDLists.add(qtimeActionReqParam.getLotID());
        accessControlCheckInqParams.setLotIDLists(lotIDLists);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, qtimeActionReqParam.getUser(), accessControlCheckInqParams);

        /*-----------------------------------------------------------------------*/
        /*   Main Process                                                        */
        /*-----------------------------------------------------------------------*/
        Infos.QtimeListInqInfo qtimeListInqInfo = new Infos.QtimeListInqInfo();
        qtimeListInqInfo.setLotID(qtimeActionReqParam.getLotID());
        qtimeListInqInfo.setActiveQTime(true);
        List<Outputs.QrestLotInfo> tmpResult = processControlInqService.sxQtimeListInq(objCommon, qtimeListInqInfo);

        List<Outputs.QrestTimeAction> lotQtimeInfoSortByActionResult = qTimeMethod.lotQtimeInfoSortByAction(objCommon, qtimeActionReqParam.getLotID(), tmpResult.get(0).getStrQtimeInfo());
        Integer actionLen = lotQtimeInfoSortByActionResult.size();
        Boolean bNeedToCallPostProcess = false;
        List<String> postProcessRegistKey = new ArrayList<>();
        Integer postProcessRegistNum = 0;
        //逻辑需要检查
        for (int i = 0; i < actionLen; i++) {
            Inputs.QtimeActionReqIn in = new Inputs.QtimeActionReqIn();
            in.setLotID(qtimeActionReqParam.getLotID());
            in.setLotStatus(tmpResult.get(0).getLotStatus());
            in.setRouteID(tmpResult.get(0).getRouteID());
            in.setOperationNumber(tmpResult.get(0).getOperationNumber());
            in.setStrQrestTimeAction(lotQtimeInfoSortByActionResult.get(i));
            RetCode<String> qtimeActionReq = processControlService.sxQtimeActionReq(objCommon, in);
            //【step4】txPostTaskRegisterReq__100
            if (CimStringUtils.equals(qtimeActionReq.getObject(), "1")) {
                Params.PostTaskRegisterReqParams postTaskRegisterReqParams = new Params.PostTaskRegisterReqParams();
                postTaskRegisterReqParams.setTransactionID(objCommon.getTransactionID());
                Infos.PostProcessRegistrationParam postProcessRegistrationParm = new Infos.PostProcessRegistrationParam();
                postProcessRegistrationParm.setLotIDs(lotIDLists);
                postTaskRegisterReqParams.setPostProcessRegistrationParm(postProcessRegistrationParm);
                postTaskRegisterReqParams.setUser(qtimeActionReqParam.getUser());
                postTaskRegisterReqParams.setPatternID(null);
                postTaskRegisterReqParams.setKey(null);
                postTaskRegisterReqParams.setSequenceNumber(-1);
                postTaskRegisterReqParams.setClaimMemo("");
                Results.PostTaskRegisterReqResult postTaskRegisterReqResult = postService.sxPostTaskRegisterReq(objCommon, postTaskRegisterReqParams);
                if (CimStringUtils.isNotEmpty(postTaskRegisterReqResult.getDKey())) {
                    postProcessRegistKey.add(postTaskRegisterReqResult.getDKey());
                }
            }
        }
        //----------------------------------
        // Post-Processing Execution Section
        //----------------------------------
        //TxPostTaskExecuteReq__100
        for (int i = 0; i < CimArrayUtils.getSize(postProcessRegistKey); i++) {
            Params.PostTaskExecuteReqParams postTaskExecuteReqParams = new Params.PostTaskExecuteReqParams();
            postTaskExecuteReqParams.setUser(qtimeActionReqParam.getUser());
            postTaskExecuteReqParams.setKey(postProcessRegistKey.get(i));
            postTaskExecuteReqParams.setSyncFlag(1);
            postTaskExecuteReqParams.setPreviousSequenceNumber(0);
            postTaskExecuteReqParams.setKeyTimeStamp(null);
            postTaskExecuteReqParams.setClaimMemo("");
            postController.postTaskExecuteReq(postTaskExecuteReqParams);
        }
        return Response.createSucc(txId, null);
    }

    @Override
    @ResponseBody
    @RequestMapping(value = "/qtime_do_action/req", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.QREST_TIME_ACTION_EXEC_REQ)
    @EnablePostProcess
    public Response qtimeActionReq(@RequestBody Params.QtimeActionReqParam qtimeActionReqParam) {
        String txId = TransactionIDEnum.QREST_TIME_ACTION_EXEC_REQ.getValue();
        //set current tx id
        ThreadContextHolder.setTransactionId(txId);

        //step2 -  call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        List<ObjectIdentifier> lotIDLists = new ArrayList<>();
        lotIDLists.add(qtimeActionReqParam.getLotID());
        accessControlCheckInqParams.setLotIDLists(lotIDLists);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, qtimeActionReqParam.getUser(),
                accessControlCheckInqParams);

        /*-----------------------------------------------------------------------*/
        /*   Main Process                                                        */
        /*-----------------------------------------------------------------------*/
        Infos.QtimeListInqInfo qtimeListInqInfo = new Infos.QtimeListInqInfo();
        qtimeListInqInfo.setLotID(qtimeActionReqParam.getLotID());
        qtimeListInqInfo.setActiveQTime(true);
        processControlInqService.sxQtimeListInq(objCommon, qtimeListInqInfo);
        List<Outputs.QrestLotInfo> tmpResult = processControlInqService.sxQtimeListInq(objCommon, qtimeListInqInfo);

        List<Outputs.QrestTimeAction> lotQtimeInfoSortByActionResult = qTimeMethod.lotQtimeInfoSortByAction(objCommon,
                qtimeActionReqParam.getLotID(), tmpResult.get(0).getStrQtimeInfo());
        Integer actionLen = lotQtimeInfoSortByActionResult.size();
        //逻辑需要检查
        for (int i = 0; i < actionLen; i++) {
            Inputs.QtimeActionReqIn in = new Inputs.QtimeActionReqIn();
            in.setLotID(qtimeActionReqParam.getLotID());
            in.setLotStatus(tmpResult.get(0).getLotStatus());
            in.setRouteID(tmpResult.get(0).getRouteID());
            in.setOperationNumber(tmpResult.get(0).getOperationNumber());
            in.setStrQrestTimeAction(lotQtimeInfoSortByActionResult.get(i));
            processControlService.sxQtimeActionReq(objCommon, in);
        }

        return Response.createSucc(txId);
        //-----------------------------
        // PostProcess
        //-----------------------------
    }

    @ResponseBody
    @RequestMapping(value = "/qtimer/req", method = RequestMethod.POST)
    @Override
    @CimMapping({TransactionIDEnum.QREST_TIME_UPDATE_REQ_ACTION_CREATE,TransactionIDEnum.QREST_TIME_UPDATE_REQ_ACTION_UPDATE,TransactionIDEnum.QREST_TIME_UPDATE_REQ_ACTION_DELETE})
    public Response qtimerReq(@RequestBody Params.QtimerReqParams params) {
        //Step-0:Initialize Parameters;
        String transactionID = null;
        if (CimStringUtils.equals(BizConstant.SP_QRESTTIME_OPECATEGORY_CREATE, params.getActionType())) {
            transactionID = TransactionIDEnum.QREST_TIME_UPDATE_REQ_ACTION_CREATE.getValue();
        } else if (CimStringUtils.equals(BizConstant.SP_QRESTTIME_OPECATEGORY_UPDATE, params.getActionType())) {
            transactionID = TransactionIDEnum.QREST_TIME_UPDATE_REQ_ACTION_UPDATE.getValue();
        } else if (CimStringUtils.equals(BizConstant.SP_QRESTTIME_OPECATEGORY_DELETE, params.getActionType())) {
            transactionID = TransactionIDEnum.QREST_TIME_UPDATE_REQ_ACTION_DELETE.getValue();
        } else {
            transactionID = TransactionIDEnum.QREST_TIME_UPDATE_REQ_ACTION_CREATE.getValue();
        }
        ThreadContextHolder.setTransactionId(transactionID);
        //check input params
        //Step-1:Incoming Log Put;
        log.info(" Incoming = {}", transactionID);

        //Step-2:txCalendar_GetCurrentTimeDR;
        log.debug("【Step-2】txCalendar_GetCurrentTimeDR(...)");

        //Step-3:txAccessControlCheckInq;
        log.debug("【Step-3】txAccessControlCheckInq(...)");
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        List<ObjectIdentifier> lotIDLists = new ArrayList<>();
        lotIDLists.add(params.getLotID());
        accessControlCheckInqParams.setLotIDLists(lotIDLists);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), accessControlCheckInqParams);

        //Step-4:txqtimerReq;
        log.debug("【Step-4】call-txQtimerReq(...)");
        processControlService.sxQtimerReq(objCommon, params);

        // Step-5:Post Process;

        return Response.createSucc(transactionID, null);
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param strQtimeManageActionByPostTaskReqInParm
     * @return com.fa.cim.common.support.Response
     * @throws
     * @author Ho
     * @date 2019/8/27 14:38
     */
    @ResponseBody
    @RequestMapping(value = "/qtime_manage_action_by_post_task/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.QTIME_ACTION_RESET_BY_POST_PROC_REQ)
    public Response qtimeManageActionByPostTaskReq(@RequestBody Params.QtimeManageActionByPostTaskReqInParm strQtimeManageActionByPostTaskReqInParm) {

        String txID = QTIME_ACTION_RESET_BY_POST_PROC_REQ.getValue();
        ThreadContextHolder.setTransactionId(txID);

        log.info("TxQtimeManageActionByPostTaskReq");
        // 【step1】get schedule from calendar
        ObjectIdentifier dummy = new ObjectIdentifier();
        List<ObjectIdentifier> dummyIDs = new ArrayList<>();
        List<ObjectIdentifier> lotIDs = new ArrayList<>();
        lotIDs.add(strQtimeManageActionByPostTaskReqInParm.getLotID());

        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();

        accessControlCheckInqParams.setUser(accessControlCheckInqParams.getUser());
        accessControlCheckInqParams.setEquipmentID(dummy);
        accessControlCheckInqParams.setStockerID(dummy);
        accessControlCheckInqParams.setProductIDList(dummyIDs);
        accessControlCheckInqParams.setRouteIDList(dummyIDs);
        accessControlCheckInqParams.setLotIDLists(lotIDs);
        accessControlCheckInqParams.setMachineRecipeIDList(dummyIDs);

        // step2 - txAccessControlCheckInq
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txID, accessControlCheckInqParams.getUser(), accessControlCheckInqParams);

        // step3 - txQtimeManageActionByPostTaskReq
        processControlService.sxQtimeManageActionByPostTaskReq(objCommon, strQtimeManageActionByPostTaskReqInParm
                , strQtimeManageActionByPostTaskReqInParm.getClaimMemo());


        return Response.createSucc(txID, null);
    }

    @ResponseBody
    @RequestMapping(value = "/future_rework_cancel/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.FUTURE_REWORK_CANCEL_REQ)
    public Response futureReworkCancelReq(@RequestBody Params.FutureReworkCancelReqParams params) {
        //【step0】init params
        final String transactionID = FUTURE_REWORK_CANCEL_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //step2 - call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        ObjectIdentifier lotID = params.getLotID();
        ObjectIdentifier routeID = params.getRouteID();
        accessControlCheckInqParams.setLotIDLists(ImmutableList.of(lotID));
        accessControlCheckInqParams.setRouteIDList(ImmutableList.of(routeID));
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), accessControlCheckInqParams);

        //【step3】call sxFutureReworkCancelReq(...)
        processControlService.sxFutureReworkCancelReq(objCommon, lotID, routeID, params.getOperationNumber(), params.getStrFutureReworkDetailInfoSeq(), params.getClaimMemo());
        return Response.createSucc(transactionID);
    }

    @ResponseBody
    @RequestMapping(value = "/old/process_hold_cancel/req", method = RequestMethod.POST)
    //@CimMapping(TransactionIDEnum.PROCESS_HOLD_CANCEL_REQ)
    public Response processHoldCancelReqOld(@RequestBody Params.ProcessHoldCancelReq param) {
        final String txId = TransactionIDEnum.PROCESS_HOLD_CANCEL_REQ.getValue();
        ThreadContextHolder.setTransactionId(txId);

        User user = param.getUser();
        //step1 - txAccessControlCheckInq
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        List<ObjectIdentifier> checkProdIDs = new ArrayList<>();
        checkProdIDs.add(param.getProductID());
        accessControlCheckInqParams.setProductIDList(checkProdIDs);
        List<ObjectIdentifier> checkRouteIDs = new ArrayList<>();
        checkRouteIDs.add(param.getRouteID());
        accessControlCheckInqParams.setRouteIDList(checkRouteIDs);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, user, accessControlCheckInqParams);

        //step3 - txProcessHoldReq
        List<Infos.ProcessHoldLot> targetLotIDList = processControlService.sxProcessHoldCancelReq(objCommon, param);
        if (CimBooleanUtils.isTrue(param.getWithExecHoldReleaseFlag())) {
            if (CimArrayUtils.isNotEmpty(targetLotIDList)) {
                Infos.ObjCommon tmpObjCommonIn = new Infos.ObjCommon();
                tmpObjCommonIn.setTransactionID(objCommon.getTransactionID());
                tmpObjCommonIn.setTimeStamp(objCommon.getTimeStamp());
                tmpObjCommonIn.setUser(objCommon.getUser());
                tmpObjCommonIn.setReserve(objCommon.getReserve());

                List<Infos.LotHoldReq> strLotHoldReqList = new ArrayList<>();
                Infos.LotHoldReq lotHoldReq = new Infos.LotHoldReq();
                lotHoldReq.setHoldType(BizConstant.SP_HOLDTYPE_PROCESSHOLD);
                lotHoldReq.setHoldReasonCodeID(param.getHoldReasonCodeID());
                lotHoldReq.setResponsibleOperationMark(BizConstant.SP_RESPONSIBLEOPERATION_CURRENT);
                lotHoldReq.setRouteID(param.getRouteID());
                lotHoldReq.setOperationNumber(param.getOperationNumber());
                lotHoldReq.setClaimMemo(param.getClaimMemo());
                strLotHoldReqList.add(lotHoldReq);
                //-----------------------------------------------------------//
                //   Call txHoldLotReleaseReq()                              //
                //-----------------------------------------------------------//
                boolean existHoldFlag = false;
                List<String> tempKeys = new ArrayList<>();
                for (Infos.ProcessHoldLot processHoldLot : targetLotIDList) {
                    //------------------------------//
                    //        Get Hold UserID       //
                    //------------------------------//
                    List<Infos.LotHoldListAttributes> lotHoldListAttributesList = null;
                    try {
                        lotHoldListAttributesList = lotInqService.sxHoldLotListInq(objCommon, processHoldLot.getLotID());
                    } catch (ServiceException ex) {
                        continue;
                    }
                    int holdListLen = CimArrayUtils.getSize(lotHoldListAttributesList);
                    existHoldFlag = false;
                    if (holdListLen > 0) {
                        for (Infos.LotHoldListAttributes lotHoldListAttributes : lotHoldListAttributesList) {
                            if (CimStringUtils.equals(BizConstant.SP_HOLDTYPE_PROCESSHOLD, lotHoldListAttributes.getHoldType())
                                    && ObjectIdentifier.equalsWithValue(param.getHoldReasonCodeID(), lotHoldListAttributes.getReasonCodeID())
                                    && ObjectIdentifier.equalsWithValue(param.getRouteID(), lotHoldListAttributes.getResponsibleRouteID())
                                    && CimStringUtils.equals(param.getOperationNumber(), lotHoldListAttributes.getResponsibleOperationNumber())) {
                                existHoldFlag = true;
                                lotHoldReq.setHoldUserID(lotHoldListAttributes.getUserID());
                                break;
                            }
                        }
                    }
                    //-------------------------------------------------------------
                    //  Check whether the specified Lot has ProcessHold is or not.
                    //-------------------------------------------------------------
                    if (!existHoldFlag) {
                        // TX_COMMIT(txHoldLotListInq)
                        continue;
                    }

                    try {
                        Params.HoldLotReleaseReqParams holdLotReleaseReqParams = new Params.HoldLotReleaseReqParams();
                        holdLotReleaseReqParams.setUser(objCommon.getUser());
                        holdLotReleaseReqParams.setLotID(processHoldLot.getLotID());
                        holdLotReleaseReqParams.setReleaseReasonCodeID(param.getReleaseReasonCodeID());
                        holdLotReleaseReqParams.setHoldReqList(strLotHoldReqList);
                        lotService.sxHoldLotReleaseReq(tmpObjCommonIn, holdLotReleaseReqParams);

                        //-------------------------------------
                        // Post-Processing Registration Section
                        //-------------------------------------
                        Params.PostTaskRegisterReqParams postTaskRegisterReqParams = new Params.PostTaskRegisterReqParams();
                        List<ObjectIdentifier> lotIDList = new ArrayList<>();
                        lotIDList.add(processHoldLot.getLotID());
                        postTaskRegisterReqParams.setTransactionID(objCommon.getTransactionID());
                        postTaskRegisterReqParams.setSequenceNumber(-1);
                        postTaskRegisterReqParams.setClaimMemo(param.getClaimMemo());
                        Infos.PostProcessRegistrationParam postProcessRegistrationParam = new Infos.PostProcessRegistrationParam();
                        postTaskRegisterReqParams.setPostProcessRegistrationParm(postProcessRegistrationParam);
                        postProcessRegistrationParam.setLotIDs(lotIDList);

                        //Step4 - txPostTaskRegisterReq__100
                        //Post-Processing Registration for Lot
                        Results.PostTaskRegisterReqResult postTaskRegisterReqResult = postService.sxPostTaskRegisterReq(objCommon, postTaskRegisterReqParams);
                        if (postTaskRegisterReqResult != null) {
                            tempKeys.add(postTaskRegisterReqResult.getDKey());
                        }

                    }catch (ServiceException ex){
                        throw ex;
                    }
                }
                for (int i = 0; i < targetLotIDList.size(); i++) {
                    if (CimArrayUtils.isNotEmpty(tempKeys)) {
                        try {
                            Params.PostTaskExecuteReqParams postTaskExecuteReqParams = new Params.PostTaskExecuteReqParams();
                            postTaskExecuteReqParams.setUser(param.getUser());
                            postTaskExecuteReqParams.setKey(tempKeys.get(i));
                            postTaskExecuteReqParams.setSyncFlag(1);
                            postTaskExecuteReqParams.setPreviousSequenceNumber(0);
                            postTaskExecuteReqParams.setKeyTimeStamp(null);
                            //Step6 - TxPostTaskExecuteReq__100
                            postController.postTaskExecuteReq(postTaskExecuteReqParams);

                        }catch (ServiceException ex){
                            targetLotIDList.get(i).setStrResult(new RetCode(ex.getTransactionID(), new OmCode(ex.getCode(), ex.getMessage()), null));
                        }
                    }
                }
            }
        }
        return Response.createSucc(txId, targetLotIDList);
    }

    @Override
    @ResponseBody
    @RequestMapping(value = "/process_hold_cancel/req", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.PROCESS_HOLD_CANCEL_REQ)
    public Response processHoldCancelReq(@RequestBody Params.ProcessHoldCancelReq param) {
        final String txId = TransactionIDEnum.PROCESS_HOLD_CANCEL_REQ.getValue();
        ThreadContextHolder.setTransactionId(txId);

        User user = param.getUser();
        //step2 - AccessControlCheck
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        List<ObjectIdentifier> checkProdIDs = new ArrayList<>();
        checkProdIDs.add(param.getProductID());
        accessControlCheckInqParams.setProductIDList(checkProdIDs);
        List<ObjectIdentifier> checkRouteIDs = new ArrayList<>();
        checkRouteIDs.add(param.getRouteID());
        accessControlCheckInqParams.setRouteIDList(checkRouteIDs);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, user, accessControlCheckInqParams);

        Results.ProcessHoldCancelReqResult retVal = new Results.ProcessHoldCancelReqResult();
        //step3 - sxProcessHoldCancelReq
        List<Infos.ProcessHoldLot> targetLotIDList = processControlService.sxProcessHoldCancelReq(objCommon, param);
        retVal.setReleasedLotIDs(targetLotIDList);

        //--------------------------------------------------------------------------//
        // If withExecHoldReleaseFlag is TRUE, call sxHoldLotListInq.             //
        //--------------------------------------------------------------------------//
        if (CimBooleanUtils.isTrue(param.getWithExecHoldReleaseFlag())) {
            if (CimArrayUtils.isNotEmpty(targetLotIDList)) {
                Infos.ObjCommon tmpObjCommonIn = new Infos.ObjCommon();
                tmpObjCommonIn.setTransactionID(objCommon.getTransactionID());
                tmpObjCommonIn.setTimeStamp(objCommon.getTimeStamp());
                tmpObjCommonIn.setUser(objCommon.getUser());
                tmpObjCommonIn.setReserve(objCommon.getReserve());

                List<Infos.LotHoldReq> strLotHoldReqList = new ArrayList<>();
                Infos.LotHoldReq lotHoldReq = new Infos.LotHoldReq();
                lotHoldReq.setHoldType(BizConstant.SP_HOLDTYPE_PROCESSHOLD);
                lotHoldReq.setHoldReasonCodeID(param.getHoldReasonCodeID());
                lotHoldReq.setResponsibleOperationMark(BizConstant.SP_RESPONSIBLEOPERATION_CURRENT);
                lotHoldReq.setRouteID(param.getRouteID());
                lotHoldReq.setOperationNumber(param.getOperationNumber());
                lotHoldReq.setClaimMemo(param.getClaimMemo());
                strLotHoldReqList.add(lotHoldReq);
                //-----------------------------------------------------------//
                //   Call sxHoldLotListInq()                              //
                //-----------------------------------------------------------//
                boolean existHoldFlag;
                for (Infos.ProcessHoldLot processHoldLot : targetLotIDList) {
                    //------------------------------//
                    //        Get Hold UserID       //
                    //------------------------------//
                    List<Infos.LotHoldListAttributes> lotHoldListAttributesList;
                    ObjectIdentifier lotID = processHoldLot.getLotID();
                    try {
                        lotHoldListAttributesList = lotInqService.sxHoldLotListInq(objCommon, lotID);
                    } catch (ServiceException ex) {
                        final OmCode omCode = new OmCode(ex.getCode(), ex.getMessage());
                        final RetCode<?> retCode = new RetCode<>(ex.getTransactionID(), omCode, null);
                        processHoldLot.setStrResult(retCode);
                        continue;
                    }
                    existHoldFlag = false;
                    if (CimArrayUtils.isNotEmpty(lotHoldListAttributesList)) {
                        for (Infos.LotHoldListAttributes lotHold : lotHoldListAttributesList) {
                            if (CimStringUtils.equals(BizConstant.SP_HOLDTYPE_PROCESSHOLD, lotHold.getHoldType())
                                    && ObjectIdentifier.equalsWithValue(param.getHoldReasonCodeID(), lotHold.getReasonCodeID())
                                    && ObjectIdentifier.equalsWithValue(param.getRouteID(), lotHold.getResponsibleRouteID())
                                    && CimStringUtils.equals(param.getOperationNumber(), lotHold.getResponsibleOperationNumber())) {
                                existHoldFlag = true;
                                lotHoldReq.setHoldUserID(lotHold.getUserID());
                                break;
                            }
                        }
                    }
                    //-------------------------------------------------------------
                    //  Check whether the specified Lot has ProcessHold is or not.
                    //-------------------------------------------------------------
                    if (!existHoldFlag) {
                        if (log.isDebugEnabled()) {
                            log.debug("Listed Lot[{}] doesn't have the specified ProcessHold.", lotID.getValue());
                        }
                        continue;
                    }
                    Params.HoldLotReleaseReqParams holdLotReleaseReqParams = new Params.HoldLotReleaseReqParams();
                    holdLotReleaseReqParams.setUser(objCommon.getUser());
                    holdLotReleaseReqParams.setLotID(processHoldLot.getLotID());
                    holdLotReleaseReqParams.setReleaseReasonCodeID(param.getReleaseReasonCodeID());
                    holdLotReleaseReqParams.setHoldReqList(strLotHoldReqList);
                    lotService.sxHoldLotReleaseReq(tmpObjCommonIn, holdLotReleaseReqParams);

                    // 设置该Lot需要执行PostProcess逻辑
                    processHoldLot.setExecPostProcessFlag(true);
                }
            }
        }
        return Response.createSucc(txId, retVal);

        //-----------------------------
        // Exec PostProcess
        //-----------------------------
    }

    @ResponseBody
    @RequestMapping(value = "/old/process_hold/req", method = RequestMethod.POST)
    //@CimMapping(TransactionIDEnum.PROCESS_HOLD_REQ)
    public Response processHoldReqOld(@RequestBody Params.ProcessHoldReq param) {
        //step1 - init
        final String txId = TransactionIDEnum.PROCESS_HOLD_REQ.getValue();
        ThreadContextHolder.setTransactionId(txId);
        //step2 - txAccessControlCheckInq
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        List<ObjectIdentifier> checkProdIDs = new ArrayList<>();
        checkProdIDs.add(param.getProductID());
        List<ObjectIdentifier> checkRouteIDs = new ArrayList<>();
        checkRouteIDs.add(param.getRouteID());
        accessControlCheckInqParams.setProductIDList(checkProdIDs);
        accessControlCheckInqParams.setRouteIDList(checkRouteIDs);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, param.getUser(), accessControlCheckInqParams);
        //step3 - txProcessHoldReq
        List<Infos.ProcessHoldLot> heldLotIDs = processControlService.sxProcessHoldReq(objCommon, param);
        if (CimBooleanUtils.isTrue(param.getWithExecHoldFlag())) {
            if (CimArrayUtils.isNotEmpty(heldLotIDs)) {
                List<Infos.LotHoldReq> holdReqList = new ArrayList<>();
                Infos.LotHoldReq lotHoldReq = new Infos.LotHoldReq();
                holdReqList.add(lotHoldReq);
                lotHoldReq.setHoldType(BizConstant.SP_HOLDTYPE_PROCESSHOLD);
                lotHoldReq.setHoldReasonCodeID(param.getReasonCodeID());
                lotHoldReq.setHoldUserID(param.getUser().getUserID());
                lotHoldReq.setResponsibleOperationMark(BizConstant.SP_RESPONSIBLEOPERATION_CURRENT);
                lotHoldReq.setRouteID(param.getRouteID());
                lotHoldReq.setOperationNumber(param.getOperationNumber());
                lotHoldReq.setClaimMemo(param.getClaimMemo());
                lotHoldReq.setDepartment(param.getDepartment());
                lotHoldReq.setSection(param.getSection());
                for (Infos.ProcessHoldLot heldLotID : heldLotIDs) {
                    Results.PostTaskRegisterReqResult postTaskRegisterReqResult = null;
                    try {

                        //step4 - txHoldLotReq
                        lotService.sxHoldLotReq(objCommon, heldLotID.getLotID(), holdReqList);
                        // Post-Processing Registration Section
                        //【Step5】Post-Process task Registration;
                        Infos.PostProcessRegistrationParam postProcessRegistrationParm = new Infos.PostProcessRegistrationParam();
                        List<ObjectIdentifier> lotIDLists = new ArrayList<>();
                        lotIDLists.add(heldLotID.getLotID());
                        postProcessRegistrationParm.setLotIDs(lotIDLists);
                        Params.PostTaskRegisterReqParams processActionRegistReqParams = new Params.PostTaskRegisterReqParams();
                        processActionRegistReqParams.setTransactionID(objCommon.getTransactionID());
                        processActionRegistReqParams.setSequenceNumber(-1);
                        processActionRegistReqParams.setPostProcessRegistrationParm(postProcessRegistrationParm);
                        postTaskRegisterReqResult = postService.sxPostTaskRegisterReq(objCommon, processActionRegistReqParams);

                    } catch (ServiceException ex){
                        heldLotID.setStrResult(new RetCode(ex.getTransactionID(), new OmCode(ex.getCode(), ex.getMessage()), null));
                        continue;
                    }

                    //-----------------------------
                    // Call postProcess
                    //-----------------------------
                    //【Step6】Post-Process task Execution Section;
                    log.info("Call postProcess");
                    Params.PostTaskExecuteReqParams postTaskExecuteReqParams = new Params.PostTaskExecuteReqParams();
                    postTaskExecuteReqParams.setUser(objCommon.getUser());
                    postTaskExecuteReqParams.setKey(postTaskRegisterReqResult == null ? null : postTaskRegisterReqResult.getDKey());
                    postTaskExecuteReqParams.setSyncFlag(1);
                    postTaskExecuteReqParams.setPreviousSequenceNumber(0);
                    postController.postTaskExecuteReq(postTaskExecuteReqParams);
                }
            }
        }
        return Response.createSucc(txId, heldLotIDs);
    }

    @Override
    @ResponseBody
    @RequestMapping(value = "/process_hold/req", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.PROCESS_HOLD_REQ)
    @EnablePostProcess
    public Response processHoldReq(@RequestBody Params.ProcessHoldReq param) {
        //step1 - init
        final String txId = TransactionIDEnum.PROCESS_HOLD_REQ.getValue();
        ThreadContextHolder.setTransactionId(txId);

        //step2 - checkPrivilegeAndGetObjCommon
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        List<ObjectIdentifier> checkProdIDs = new ArrayList<>();
        checkProdIDs.add(param.getProductID());
        List<ObjectIdentifier> checkRouteIDs = new ArrayList<>();
        checkRouteIDs.add(param.getRouteID());
        accessControlCheckInqParams.setProductIDList(checkProdIDs);
        accessControlCheckInqParams.setRouteIDList(checkRouteIDs);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId,
                param.getUser(),
                accessControlCheckInqParams);


        Results.ProcessHoldReqResult retVal = new Results.ProcessHoldReqResult();
        //step3 - sxProcessHoldReq
        List<Infos.ProcessHoldLot> heldLotIDs = processControlService.sxProcessHoldReq(objCommon, param);
        retVal.setHeldLotIDs(heldLotIDs);

        //--------------------------------------------------------------------------//
        // If withExecHoldFlag is TRUE,                                             //
        //  call sxHoldLotReq for target LotIDs after (sxProcessHoldReq).           //
        //--------------------------------------------------------------------------//
        if (CimBooleanUtils.isTrue(param.getWithExecHoldFlag())) {
            if (CimArrayUtils.isNotEmpty(heldLotIDs)) {
                List<Infos.LotHoldReq> holdReqList = new ArrayList<>();
                Infos.LotHoldReq lotHoldReq = new Infos.LotHoldReq();
                holdReqList.add(lotHoldReq);
                lotHoldReq.setHoldType(BizConstant.SP_HOLDTYPE_PROCESSHOLD);
                lotHoldReq.setHoldUserID(param.getUser().getUserID());
                lotHoldReq.setHoldReasonCodeID(param.getReasonCodeID());
                lotHoldReq.setResponsibleOperationMark(BizConstant.SP_RESPONSIBLEOPERATION_CURRENT);
                lotHoldReq.setRouteID(param.getRouteID());
                lotHoldReq.setOperationNumber(param.getOperationNumber());
                lotHoldReq.setDepartment(param.getDepartment());
                lotHoldReq.setSection(param.getSection());
                lotHoldReq.setClaimMemo(param.getClaimMemo());
                for (Infos.ProcessHoldLot heldLotID : heldLotIDs) {
                    try {
                        //step4 - sxHoldLotReq
                        lotService.sxHoldLotReq(objCommon, heldLotID.getLotID(), holdReqList);
                        // 设置该Lot需要执行PostProcess逻辑
                        heldLotID.setExecPostProcessFlag(true);
                    } catch (ServiceException ex) {
                        final OmCode omCode = new OmCode(ex.getCode(), ex.getMessage());
                        final RetCode<?> retCode = new RetCode<>(ex.getTransactionID(), omCode, null);
                        heldLotID.setStrResult(retCode);
                    }
                }
            }
        }

        return Response.createSucc(txId, retVal);
        //----------------------------
        // Exec PostProcess
        //----------------------------
    }

    @ResponseBody
    @RequestMapping(value = "/future_rework/req", method = RequestMethod.POST)
    @Override
    public Response futureReworkReq(@RequestBody Params.FutureReworkReqParams params) {
        //【step0】init params
        final String transactionID = TransactionIDEnum.FUTURE_REWORK_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //step2 - call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        accessControlCheckInqParams.setLotIDLists(ImmutableList.of(params.getLotID()));
        accessControlCheckInqParams.setRouteIDList(ImmutableList.of(params.getRouteID()));
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), accessControlCheckInqParams);

        //【step3】call sxFutureReworkReq(...)
        processControlService.sxFutureReworkReq(objCommon, params);
        return Response.createSucc(transactionID, null);
    }

    @ResponseBody
    @RequestMapping(value = "/future_rework_action_do/req", method = RequestMethod.POST)
    @Override
    public Response futureReworkActionDoReq(@RequestBody Params.FutureReworkActionDoReqParams params) {
        //【step0】init params
        final String transactionID = TransactionIDEnum.FUTURE_REWORK_EXEC_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //step2 - call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        ObjectIdentifier lotID = params.getLotID();
        accessControlCheckInqParams.setLotIDLists(ImmutableList.of(lotID));
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), accessControlCheckInqParams);

        //【step3】call sxFutureReworkReq(...)
        processControlService.sxFutureReworkActionDoReq(objCommon, lotID, params.getClaimMemo());
        return Response.createSucc(transactionID, null);
    }

    @ResponseBody
    @Override
    @RequestMapping(value = "/future_hold_department_change/req", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.DEPARTMENT_AUTHORITY_FUTURE_HOLD_DEPARTMENT_CHANGE_REQ)
    public Response futureHoldDepartmentChangeReq(@RequestBody Params.FutureHoldReqParams params) {
        //【step1】init params
        final String transactionID = TransactionIDEnum.DEPARTMENT_AUTHORITY_FUTURE_HOLD_DEPARTMENT_CHANGE_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //【step2】 - call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        accessControlCheckInqParams.setLotIDLists(Collections.singletonList(params.getLotID()));
        accessControlCheckInqParams.conversionDepartmentCheckCode(params.getOldDepartment(), params.getOldSection(), params.getOldReasonCodeID());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), accessControlCheckInqParams);

        //【step3】call sxFutureHoldEditReq(...)
        processControlService.sxFutureHoldDepartmentChangeReq(objCommon, params);

        //【step4】judge whether the return code is success, if no, then TCC will rollback
        return Response.createSucc(transactionID, null);
    }

    @ResponseBody
    @Override
    @RequestMapping(value = "/process_hold_department_change/req", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.DEPARTMENT_AUTHORITY_PROCESS_HOLD_DEPARTMENT_CHANGE_REQ)
    public Response processHoldDepartmentChangeReq(@RequestBody Params.ProcessHoldReq params) {
        //step1 - init
        final String txId = TransactionIDEnum.DEPARTMENT_AUTHORITY_PROCESS_HOLD_DEPARTMENT_CHANGE_REQ.getValue();
        ThreadContextHolder.setTransactionId(txId);

        //step2 - txAccessControlCheckInq
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        List<ObjectIdentifier> checkProdIDs = new ArrayList<>();
        checkProdIDs.add(params.getProductID());
        List<ObjectIdentifier> checkRouteIDs = new ArrayList<>();
        checkRouteIDs.add(params.getRouteID());
        accessControlCheckInqParams.setProductIDList(checkProdIDs);
        accessControlCheckInqParams.setRouteIDList(checkRouteIDs);
        accessControlCheckInqParams.conversionDepartmentCheckCode(params.getOldDepartment(), params.getOldSection(), params.getOldReasonCodeID());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, params.getUser(), accessControlCheckInqParams);

        //step3 - txProcessHoldEditReq
        processControlService.sxProcessHoldDepartmentChangeReq(objCommon, params);
        return Response.createSucc(txId, null);
    }

}
