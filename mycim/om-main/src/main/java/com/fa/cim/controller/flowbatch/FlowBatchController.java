package com.fa.cim.controller.flowbatch;

import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.Response;
import com.fa.cim.common.utils.CimArrayUtils;
import com.fa.cim.common.utils.CimObjectUtils;
import com.fa.cim.common.utils.ThreadContextHolder;
import com.fa.cim.common.utils.Validations;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.controller.interfaces.flowBatch.IFlowBatchController;
import com.fa.cim.controller.post.PostController;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;
import com.fa.cim.frameworks.pprocess.api.annotations.EnablePostProcess;
import com.fa.cim.middleware.standard.api.annotations.listener.CimMapping;
import com.fa.cim.middleware.standard.api.annotations.listener.Listenable;
import com.fa.cim.service.access.IAccessInqService;
import com.fa.cim.service.flowbatch.IFlowBatchService;
import com.fa.cim.service.post.IPostService;
import lombok.extern.slf4j.Slf4j;
import org.bytesoft.compensable.Compensable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/7/29          ********            Nyx                create file
 *
 * @author: Nyx
 * @date: 2019/7/29 14:28
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@RestController
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
@Compensable(interfaceClass = IFlowBatchController.class, confirmableKey = "FlowBatchConfirm", cancellableKey = "FlowBatchCancel")
@RequestMapping("/flowb")
@Listenable
public class FlowBatchController implements IFlowBatchController {

    @Autowired
    private IFlowBatchService flowBatchService;
    @Autowired
    private IAccessInqService accessInqService;
    @Autowired
    private RetCodeConfig retCodeConfig;
    @Autowired
    private IPostService postService;
    @Autowired
    private PostController postController;

    @ResponseBody
    @RequestMapping(value = "/old/flow_batch_by_manual_action/req", method = RequestMethod.POST)
    //@CimMapping(TransactionIDEnum.FLOW_BATCHING_REQ)
    public Response flowBatchByManualActionReqOld(@RequestBody Params.FlowBatchByManualActionReqParam param) {

        final String transactionID = TransactionIDEnum.FLOW_BATCHING_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        List<ObjectIdentifier> lotIDs = new ArrayList<>();
        for (Infos.FlowBatchByManualActionReqCassette flowBatchByManualActionReqCassette : param.getStrFlowBatchByManualActionReqCassette()) {
            lotIDs.addAll(flowBatchByManualActionReqCassette.getLotID());
        }
        //Step1 - txAccessControlCheckInq
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        accessControlCheckInqParams.setLotIDLists(lotIDs);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, param.getUser(), accessControlCheckInqParams);

        //-----------------------------------------------------------------------
        //   Main Process
        //-----------------------------------------------------------------------
        //step2 - sxFlowBatchByManualActionReq
        Results.FlowBatchByManualActionReqResult flowBatchByManualActionReqResult = flowBatchService.sxFlowBatchByManualActionReq(objCommon, param);
        //-------------------------------------
        // Post-Processing Registration Section
        //-------------------------------------
        // step3 - txPostTaskRegisterReq__100
        Results.PostTaskRegisterReqResult registReqResultRetCode = null;
        log.info("txFlowBatchByManualActionReq :rc == RC_OK");
        long seqLen = flowBatchByManualActionReqResult.getStrFlowBatchedLot().size();
        List<ObjectIdentifier> tempLotIDs = new ArrayList<ObjectIdentifier>();
        for (int i = 0; i < seqLen; i++) {
            tempLotIDs.add(flowBatchByManualActionReqResult.getStrFlowBatchedLot().get(i).getLotID());
            log.info("Collecting LotID,{}", tempLotIDs.get(i));
        }
        if (seqLen > 0) {
            Params.PostTaskRegisterReqParams postTaskRegisterReqParams = new Params.PostTaskRegisterReqParams();
            Infos.PostProcessRegistrationParam postProcessRegistrationParam = new Infos.PostProcessRegistrationParam();
            postProcessRegistrationParam.setLotIDs(tempLotIDs);
            postTaskRegisterReqParams.setTransactionID(objCommon.getTransactionID());
            postTaskRegisterReqParams.setPatternID(null);
            postTaskRegisterReqParams.setKey(null);
            postTaskRegisterReqParams.setSequenceNumber(-1);
            postTaskRegisterReqParams.setClaimMemo(param.getClaimMemo());
            postTaskRegisterReqParams.setPostProcessRegistrationParm(postProcessRegistrationParam);
            try {
                registReqResultRetCode = postService.sxPostTaskRegisterReq(objCommon, postTaskRegisterReqParams);
            } catch (ServiceException e) {
                return Response.createError(retCodeConfig.getError(), TransactionIDEnum.FLOW_BATCHING_REQ.getValue(), e.toString());
            }
        }

        // step4 - TxPostTaskExecuteReq__100
        // Post Process Execution for lot
        if (registReqResultRetCode != null) {
            log.info("txFlowBatchByManualActionReq and txPostTaskRegisterReq__100 :rc == RC_OK");
            Params.PostTaskExecuteReqParams postTaskExecuteReqParams = new Params.PostTaskExecuteReqParams();
            postTaskExecuteReqParams.setUser(param.getUser());
            postTaskExecuteReqParams.setKey(registReqResultRetCode.getDKey());
            postTaskExecuteReqParams.setSyncFlag(1);
            postTaskExecuteReqParams.setPreviousSequenceNumber(0);
            postTaskExecuteReqParams.setKeyTimeStamp(null);
            postController.postTaskExecuteReq(postTaskExecuteReqParams);
        }
        //----------------------------------------------------------------------
        //   Post Process
        //----------------------------------------------------------------------
        return Response.createSucc(transactionID, flowBatchByManualActionReqResult);
    }

    @ResponseBody
    @RequestMapping(value = "/flow_batch_by_manual_action/req", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.FLOW_BATCHING_REQ)
    @EnablePostProcess
    @Override
    public Response flowBatchByManualActionReq(@RequestBody Params.FlowBatchByManualActionReqParam param) {

        final String transactionID = TransactionIDEnum.FLOW_BATCHING_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        List<ObjectIdentifier> lotIDs = new ArrayList<>();
        for (Infos.FlowBatchByManualActionReqCassette cassette : param.getStrFlowBatchByManualActionReqCassette()) {
            lotIDs.addAll(cassette.getLotID());
        }
        //Step1 - txAccessControlCheckInq
        Params.AccessControlCheckInqParams params = new Params.AccessControlCheckInqParams(true);
        params.setLotIDLists(lotIDs);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, param.getUser(), params);

        //-----------------------------------------------------------------------
        //   Main Process
        //-----------------------------------------------------------------------
        //step2 - sxFlowBatchByManualActionReq
        Results.FlowBatchByManualActionReqResult result = flowBatchService.sxFlowBatchByManualActionReq(objCommon, param);

        return Response.createSucc(transactionID, result);
        //----------------------------------------------------------------------
        //   Post Process
        //----------------------------------------------------------------------
    }

    @ResponseBody
    @RequestMapping(value = "/flow_batch_lot_remove/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.LOT_REMOVE_FROM_FLOW_BATCH_REQ)
    public Response flowBatchLotRemoveReq(@RequestBody Params.FlowBatchLotRemoveReq params) {
        //【step0】init params
        final String transactionID = TransactionIDEnum.LOT_REMOVE_FROM_FLOW_BATCH_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //【step2】call txAccessControlCheckInq(...)
        List<ObjectIdentifier> lotIDLists = new ArrayList<>();
        for (Infos.RemoveCassette removeCassette : params.getStrRemoveCassette()) {
            List<ObjectIdentifier> lotIDs = removeCassette.getLotID();
            lotIDLists.addAll(lotIDs);
        }
        //Step1 - txAccessControlCheckInq
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        accessControlCheckInqParams.setLotIDLists(lotIDLists);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), accessControlCheckInqParams);

        //【step3】call sxReworkWholeLotCancel
        Results.FlowBatchLotRemoveReqResult result = flowBatchService.sxFlowBatchLotRemoveReq(objCommon, params);

        Response response = Response.createSucc(transactionID, result);
        return response;
    }

    @ResponseBody
    @RequestMapping(value = "/old/flow_batch_by_auto_action/req", method = RequestMethod.POST)
    //@CimMapping(TransactionIDEnum.AUTO_FLOW_BATCHING_REQ)
    public Response autoFlowBatchByManualActionReqOld(@RequestBody Params.FlowBatchByAutoActionReqParams params) {
        //Step0 - init params
        final String transactionID = TransactionIDEnum.AUTO_FLOW_BATCHING_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //Step1 - txAccessControlCheckInq
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), accessControlCheckInqParams);

        //step3 - txFlowBatchByAutoActionReq
        Results.FlowBatchByAutoActionReqResult result = flowBatchService.sxFlowBatchByAutoActionReq(objCommon, params);



        List<Infos.FlowBatchedCassette> strBatchedCassette = result.getStrBatchedCassette();
        List<ObjectIdentifier> tempLotIDs = new ArrayList<>();
        List<ObjectIdentifier> tempCastIDs = new ArrayList<>();

        int lotCnt = 0;
        if (CimArrayUtils.isNotEmpty(strBatchedCassette)) {
            for (Infos.FlowBatchedCassette flowBatchedCassette : strBatchedCassette) {
                tempCastIDs.add(flowBatchedCassette.getCassetteID());
                List<ObjectIdentifier> lotID = flowBatchedCassette.getLotID();
                if (CimArrayUtils.isNotEmpty(lotID)) {
                    for (ObjectIdentifier lotId : lotID) {
                        tempLotIDs.add(lotId);
                        lotCnt++;
                    }
                }
            }
        }

        Results.PostTaskRegisterReqResult postTaskRegisterReqResult = null;
        if (lotCnt > 0) {
            Params.PostTaskRegisterReqParams postTaskRegisterReqParams = new Params.PostTaskRegisterReqParams();
            Infos.PostProcessRegistrationParam postProcessRegistrationParam = new Infos.PostProcessRegistrationParam();

            postProcessRegistrationParam.setCassetteIDs(tempCastIDs);
            postProcessRegistrationParam.setLotIDs(tempLotIDs);

            postTaskRegisterReqParams.setPostProcessRegistrationParm(postProcessRegistrationParam);
            postTaskRegisterReqParams.setSequenceNumber(-1);
            postTaskRegisterReqParams.setTransactionID(objCommon.getTransactionID());
            postTaskRegisterReqParams.setUser(params.getUser());

            //step4 - txPostTaskRegisterReq__100
            postTaskRegisterReqResult = postService.sxPostTaskRegisterReq(objCommon, postTaskRegisterReqParams);
        }
        Params.PostTaskExecuteReqParams postTaskExecuteReqParams = new Params.PostTaskExecuteReqParams();
        postTaskExecuteReqParams.setUser(params.getUser());
        postTaskExecuteReqParams.setKey(postTaskRegisterReqResult == null ? null : postTaskRegisterReqResult.getDKey());
        postTaskExecuteReqParams.setSyncFlag(1);
        postTaskExecuteReqParams.setPreviousSequenceNumber(0);

        //step5 - TxPostTaskExecuteReq__100
        postController.postTaskExecuteReq(postTaskExecuteReqParams);
        return Response.createSucc(transactionID, result);
    }

    @Override
    @ResponseBody
    @RequestMapping(value = "/flow_batch_by_auto_action/req", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.AUTO_FLOW_BATCHING_REQ)
    @EnablePostProcess
    public Response autoFlowBatchByManualActionReq(@RequestBody Params.FlowBatchByAutoActionReqParams params) {
        //Step0 - init params
        final String transactionID = TransactionIDEnum.AUTO_FLOW_BATCHING_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //Step1 - txAccessControlCheckInq
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID,
                params.getUser(),
                accessControlCheckInqParams);

        //step3 - txFlowBatchByAutoActionReq
        Results.FlowBatchByAutoActionReqResult result = flowBatchService.sxFlowBatchByAutoActionReq(objCommon, params);

        return Response.createSucc(transactionID, result);
        //-----------------------------
        // PostProcess
        //-----------------------------
    }


    @ResponseBody
    @RequestMapping(value = "/eqp_max_flowb_count_modify/req")
    @Override
    @CimMapping(TransactionIDEnum.EQP_FLOW_BATCH_MAX_COUNT_CHANGE_REQ)
    public Response eqpMaxFlowbCountModifyReq(@RequestBody Params.EqpMaxFlowbCountModifyReqParams params) {
        log.info("input eqpMaxFlowbCountModifyReq...  params: " + params);

        //Step0 - init params
        final String transactionID = TransactionIDEnum.EQP_FLOW_BATCH_MAX_COUNT_CHANGE_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        /*-----------------------------------------------------------------------*/
        /*   Pre Process                                                         */
        /*-----------------------------------------------------------------------*/
        List<Infos.EventParameter> eventParameterList = new ArrayList<>();

        // set current tx id.
        ThreadContextHolder.setTransactionId(transactionID);
        ObjectIdentifier equipmentID = params.getEquipmentID();
        Validations.check(ObjectIdentifier.isEmpty(equipmentID), "the equipmentID is null.");
        int flowBatchMaxCount = params.getFlowBatchMaxCount();
        Validations.check(CimObjectUtils.isEmpty(flowBatchMaxCount), "the flowBatchMaxCount is null.");
        String claimMemo = params.getClaimMemo();

        //Step1 - txAccessControlCheckInq
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), accessControlCheckInqParams);

        /*-----------------------------------------------------------------------*/
        /*   Main Process                                                        */
        /*-----------------------------------------------------------------------*/
        // 【Step3】 txEqpMaxFlowbCountModifyReq(...)
        flowBatchService.sxEqpMaxFlowbCountModifyReq(objCommon, equipmentID, flowBatchMaxCount, claimMemo);

        /*-----------------------------------------------------------------------*/
        /*   Post Process                                                        */
        /*-----------------------------------------------------------------------*/
        log.info("exit eqpMaxFlowbCountModifyReq...");
        return Response.createSucc(transactionID);
    }

    @ResponseBody
    @RequestMapping(value = "/eqp_reserve_cancel_for_flow_batch/req")
    @Override
    @CimMapping(TransactionIDEnum.EQP_RESERVE_CANCEL_REQ)
    public Response eqpReserveCancelForflowBatchReq(@RequestBody Params.EqpReserveCancelForflowBatchReqParams params) {
        log.info("eqpReserveCancelForflowBatchReq():come in eqpReserveCancelForflowBatchReq... Params: " + params);
        /*-----------------------------------------------------------------------*/
        /*   Pre Process                                                         */
        /*-----------------------------------------------------------------------*/
        final String transactionID = TransactionIDEnum.EQP_RESERVE_CANCEL_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        ObjectIdentifier equipmentID = params.getEquipmentID();
        Validations.check(equipmentID == null, "the equipmentID info is null.");
        ObjectIdentifier flowBatchID = params.getFlowBatchID();
        Validations.check(flowBatchID == null, "the flowBatchID info is null.");
        String claimMemo = params.getClaimMemo();

        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), accessControlCheckInqParams);

        /*-----------------------------------------------------------------------*/
        /*   Main Process                                                        */
        /*-----------------------------------------------------------------------*/
        // Step3 - call txEqpReserveCancelForflowBatchReq(...)
        flowBatchService.sxEqpReserveCancelForflowBatchReq(objCommon, equipmentID, flowBatchID, claimMemo);

        /*-----------------------------------------------------------------------*/
        /*   Post Process                                                        */
        /*-----------------------------------------------------------------------*/
        log.info("eqpReserveCancelForflowBatchReq():exit eqpReserveCancelForflowBatchReq...");
        return Response.createSucc(transactionID);
    }

    @ResponseBody
    @RequestMapping(value = "/old/eqp_reserve_for_flow_batch/req")
//    @CimMapping(TransactionIDEnum.EQP_RESERVE_REQ)
    public Response eqpReserveForFlowBatchReqOld(@RequestBody Params.EqpReserveForFlowBatchReqParam params) {
        log.info("eqpReserveForFlowBatchReq(): come in eqpReserveForFlowBatchReq method... Params: " + params);
        /*-----------------------------------------------------------------------*/
        /*   Pre Process                                                         */
        /*-----------------------------------------------------------------------*/
        final String transactionID = TransactionIDEnum.EQP_RESERVE_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        // check input params.
        ObjectIdentifier equipmentID = params.getEquipmentID();
        Validations.check(equipmentID == null, "the equipmentID info is null.");
        ObjectIdentifier flowBatchID = params.getFlowBatchID();
        Validations.check(flowBatchID == null, "the flowBatchID info is null.");
        String claimMemo = params.getClaimMemo();

        //Step1 - txAccessControlCheckInq
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), accessControlCheckInqParams);

        /*-----------------------------------------------------------------------*/
        /*   Main Process                                                        */
        /*-----------------------------------------------------------------------*/
        // Step2 - txEqpReserveForFlowBatchReq
        List<Infos.FlowBatchedLot> result = flowBatchService.sxEqpReserveForFlowBatchReq(objCommon, equipmentID, flowBatchID, claimMemo);
        log.info("eqpReserveForFlowBatchReq(): sxEqpReserveForFlowBatchReq method was success.");
        //-------------------------------------
        // Post-Processing Registration Section
        //-------------------------------------
        // step3 - call txPostTaskRegisterReq__100
        int seqLen = CimArrayUtils.getSize(result);
        List<ObjectIdentifier> tempLotIDs = new ArrayList<>();
        ObjectIdentifier dummeyID = new ObjectIdentifier();
        for (int i = 0; i < seqLen; i++) {
            tempLotIDs.add(result.get(i).getLotID());
        }
        Results.PostTaskRegisterReqResult postTaskRegisterReqResult = null;
        if (seqLen > 0) {
            Infos.PostProcessRegistrationParam strPostProcessRegistrationParm = new Infos.PostProcessRegistrationParam();
            strPostProcessRegistrationParm.setLotIDs(tempLotIDs);
            log.info("eqpReserveForFlowBatchReq(): Registration of Post-Processng");
            Params.PostTaskRegisterReqParams postTaskRegisterReqParams = new Params.PostTaskRegisterReqParams();
            postTaskRegisterReqParams.setUser(objCommon.getUser());
            postTaskRegisterReqParams.setTransactionID(transactionID);
            postTaskRegisterReqParams.setSequenceNumber(-1);
            postTaskRegisterReqParams.setClaimMemo(claimMemo);
            postTaskRegisterReqParams.setPostProcessRegistrationParm(strPostProcessRegistrationParm);

            postTaskRegisterReqResult = postService.sxPostTaskRegisterReq(objCommon, postTaskRegisterReqParams);
        }
        //----------------------------------
        // Post-Processing Execution Section
        //----------------------------------
        // step4 - call TxPostTaskExecuteReq__100
        log.info("eqpReserveForFlowBatchReq(): txEqpReserveForFlowBatchReq and txPostTaskRegisterReq__100 :rc == RC_OK");
        Params.PostTaskExecuteReqParams postTaskExecuteReqParams = new Params.PostTaskExecuteReqParams();
        postTaskExecuteReqParams.setUser(params.getUser());
        postTaskExecuteReqParams.setKey(postTaskRegisterReqResult == null ? null : postTaskRegisterReqResult.getDKey());
        postTaskExecuteReqParams.setSyncFlag(1);
        postTaskExecuteReqParams.setPreviousSequenceNumber(0);    // previous sequence number
        postController.postTaskExecuteReq(postTaskExecuteReqParams);
        /*-----------------------------------------------------------------------*/
        /*   Post Process                                                        */
        /*-----------------------------------------------------------------------*/
        log.info("eqpReserveForFlowBatchReq(): exit eqpReserveForFlowBatchReq method...");
        return Response.createSucc(transactionID, result);
    }

    @ResponseBody
    @RequestMapping(value = "/eqp_reserve_for_flow_batch/req")
    @CimMapping(TransactionIDEnum.EQP_RESERVE_REQ)
    @EnablePostProcess
    @Override
    public Response eqpReserveForFlowBatchReq(@RequestBody Params.EqpReserveForFlowBatchReqParam params) {
        log.info("eqpReserveForFlowBatchReq(): come in eqpReserveForFlowBatchReq method... Params: " + params);
        /*-----------------------------------------------------------------------*/
        /*   Pre Process                                                         */
        /*-----------------------------------------------------------------------*/
        final String transactionID = TransactionIDEnum.EQP_RESERVE_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        // check input params.
        ObjectIdentifier equipmentID = params.getEquipmentID();
        Validations.check(equipmentID == null, "the equipmentID info is null.");
        ObjectIdentifier flowBatchID = params.getFlowBatchID();
        Validations.check(flowBatchID == null, "the flowBatchID info is null.");
        String claimMemo = params.getClaimMemo();

        //Step1 - txAccessControlCheckInq
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(),
                accessControlCheckInqParams);

        /*-----------------------------------------------------------------------*/
        /*   Main Process                                                        */
        /*-----------------------------------------------------------------------*/
        // Step2 - txEqpReserveForFlowBatchReq
        Results.EqpReserveForFlowBatchReqResult retVal = new Results.EqpReserveForFlowBatchReqResult();
        List<Infos.FlowBatchedLot> result = flowBatchService.sxEqpReserveForFlowBatchReq(objCommon, equipmentID,
                flowBatchID, claimMemo);
        retVal.setFlowBatchedLots(result);
        log.info("eqpReserveForFlowBatchReq(): sxEqpReserveForFlowBatchReq method was success.");

        log.info("eqpReserveForFlowBatchReq(): exit eqpReserveForFlowBatchReq method...");
        return Response.createSucc(transactionID, retVal);
        //-----------------------------
        // PostProcess
        //-----------------------------
    }

    @ResponseBody
    @RequestMapping(value = "/flow_batch_check_for_lot_skip/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.FLOW_BATCH_OPE_LOCATE_CHECK_REQ)
    public Response flowBatchCheckForLotSkipReq(@RequestBody Params.FlowBatchCheckForLotSkipReqParams params) {
        final String transactionID = TransactionIDEnum.FLOW_BATCH_OPE_LOCATE_CHECK_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);
        //step2 - txAccessControlCheckInq
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), accessControlCheckInqParams);

        //step3 - txFlowBatchCheckForLotSkipReq
        flowBatchService.sxFlowBatchCheckForLotSkipReq(objCommon, params);
        return Response.createSucc(transactionID);
    }

    @ResponseBody
    @RequestMapping(value = "/old/flow_batch_by_floating_batch_lots/req", method = RequestMethod.POST)
    //@CimMapping(TransactionIDEnum.REFLOW_BATCHING_REQ)
    public Response reFlowBatchByManualActionReqOld(@RequestBody Params.ReFlowBatchByManualActionReqParam param) {

        String transactionID = TransactionIDEnum.REFLOW_BATCHING_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);
        //step1 - checkPrivilegeAndGetObjCommon
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, param.getUser(), accessControlCheckInqParams);

        //step2 - txReFlowBatchByManualActionReq
        Results.ReFlowBatchByManualActionReqResult reFlowBatchByManualActionReqResult = flowBatchService.sxReFlowBatchByManualActionReq(objCommon, param);
        //-------------------------------------
        // Post-Processing Registration Section
        //-------------------------------------
        List<ObjectIdentifier> tempLotIDs = new ArrayList<>();
        List<ObjectIdentifier> tempCastIDs = new ArrayList<>();
        List<Infos.FlowBatchedLot> strFlowBatchedLot = reFlowBatchByManualActionReqResult.getStrFlowBatchedLot();
        Results.PostTaskRegisterReqResult postTaskRegisterReqResult = null;
        if (!CimArrayUtils.isEmpty(strFlowBatchedLot)) {
            for (Infos.FlowBatchedLot flowBatchedLotInfo : strFlowBatchedLot) {
                tempLotIDs.add(flowBatchedLotInfo.getLotID());
                tempCastIDs.add(flowBatchedLotInfo.getCassetteID());

            }
            Params.PostTaskRegisterReqParams postTaskRegisterReqParams = new Params.PostTaskRegisterReqParams();
            Infos.PostProcessRegistrationParam strPostProcessRegistrationParm = new Infos.PostProcessRegistrationParam();
            postTaskRegisterReqParams.setPostProcessRegistrationParm(strPostProcessRegistrationParm);
            postTaskRegisterReqParams.setUser(param.getUser());
            postTaskRegisterReqParams.setTransactionID(objCommon.getTransactionID());
            postTaskRegisterReqParams.setSequenceNumber(-1);
            postTaskRegisterReqParams.setClaimMemo(param.getClaimMemo());
            strPostProcessRegistrationParm.setLotIDs(tempLotIDs);
            strPostProcessRegistrationParm.setCassetteIDs(tempCastIDs);

            //step3 - txPostTaskRegisterReq__100
            postTaskRegisterReqResult = postService.sxPostTaskRegisterReq(objCommon, postTaskRegisterReqParams);
        }
        //-------------------------------------
        // Post-Processing Execution Section
        //-------------------------------------
        Params.PostTaskExecuteReqParams postTaskExecuteReqParam = new Params.PostTaskExecuteReqParams();
        postTaskExecuteReqParam.setUser(param.getUser());
        postTaskExecuteReqParam.setKey(postTaskRegisterReqResult == null ? null : postTaskRegisterReqResult.getDKey());
        postTaskExecuteReqParam.setSyncFlag(1);
        postTaskExecuteReqParam.setPreviousSequenceNumber(0);
        postTaskExecuteReqParam.setKeyTimeStamp("");
        //step4 - TxPostTaskExecuteReq__100
        postController.postTaskExecuteReq(postTaskExecuteReqParam);

        return Response.createSucc(transactionID, reFlowBatchByManualActionReqResult);
    }

    @Override
    @ResponseBody
    @RequestMapping(value = "/flow_batch_by_floating_batch_lots/req", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.REFLOW_BATCHING_REQ)
    @EnablePostProcess
    public Response reFlowBatchByManualActionReq(@RequestBody Params.ReFlowBatchByManualActionReqParam param) {

        String transactionID = TransactionIDEnum.REFLOW_BATCHING_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);
        //step1 - checkPrivilegeAndGetObjCommon
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID,
                param.getUser(),
                accessControlCheckInqParams);

        //step2 - txReFlowBatchByManualActionReq
        Results.ReFlowBatchByManualActionReqResult result = flowBatchService.sxReFlowBatchByManualActionReq(objCommon, param);

        return Response.createSucc(transactionID, result);
    }

}
