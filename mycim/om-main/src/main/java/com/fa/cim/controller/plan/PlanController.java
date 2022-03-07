package com.fa.cim.controller.plan;

import com.alibaba.fastjson.JSONObject;
import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.OmCode;
import com.fa.cim.common.support.Response;
import com.fa.cim.common.support.RetCode;
import com.fa.cim.common.utils.*;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.controller.interfaces.plan.IPlanController;
import com.fa.cim.controller.post.PostController;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Inputs;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;
import com.fa.cim.frameworks.pprocess.api.annotations.EnablePostProcess;
import com.fa.cim.method.IMessageMethod;
import com.fa.cim.middleware.standard.api.annotations.listener.CimMapping;
import com.fa.cim.middleware.standard.api.annotations.listener.Listenable;
import com.fa.cim.service.access.IAccessInqService;
import com.fa.cim.service.plan.IPlanService;
import lombok.extern.slf4j.Slf4j;
import org.bytesoft.compensable.Compensable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/7/30          ********            Nyx                create file
 *
 * @author: Nyx
 * @date: 2019/7/30 16:33
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@RestController
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
@Compensable(interfaceClass = IPlanController.class, confirmableKey = "PlanConfirm", cancellableKey = "PlanCancel")
@RequestMapping("/plan")
@Listenable
public class PlanController implements IPlanController {
    @Autowired
    private RetCodeConfig retCodeConfig;
    @Autowired
    private IPlanService planService;
    @Autowired
    private IAccessInqService accessInqService;
    @Autowired
    private PostController postController;
    @Autowired
    private IMessageMethod messageMethod;

    @ResponseBody
    @RequestMapping(value = "/prod_order_change/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.LOT_MFG_ORDER_CHANGE_REQ)
    public Response prodOrderChangeReq(@RequestBody Params.ProdOrderChangeReqParams Params) {
        final String transactionID = TransactionIDEnum.LOT_MFG_ORDER_CHANGE_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        List<Infos.ChangedLotAttributes> strChangedLotAttributes = Params.getStrChangedLotAttributes();

        //List<ObjectIdentifier> dummyIDs=new ArrayList<>();
        List<ObjectIdentifier> lotIDs = new ArrayList<>();
        ;

        for (int j = 0; j < CimArrayUtils.getSize(strChangedLotAttributes); j++) {
            ObjectIdentifier lotID = strChangedLotAttributes.get(j).getLotID();
            lotIDs.add(lotID);
        }

        // step2 - txAccessControlCheckInq
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setLotIDLists(lotIDs);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, Params.getUser(), accessControlCheckInqParams);

        List<Infos.ChangeLotReturn> strProdOrderChangeReqResult = new ArrayList<>();
        for (int i = 0; i < CimArrayUtils.getSize(strChangedLotAttributes); i++) {
            // step3 - txProdOrderChangeReq
            strProdOrderChangeReqResult = planService.sxProdOrderChangeReq(strProdOrderChangeReqResult, objCommon, i, strChangedLotAttributes, Params.getClaimMemo());
            // step4 - messageQueue_Put
            /*--------------------------------------------------------------*/
            /*   Put Message into Message Queue for Full-Auto               */
            /*                                                              */
            /*     objMessageQueue_Put_out&  strMessageQueue_Put_out        */
            /*     const pptObjCommonIn&     strObjCommonIn                 */
            /*     const objectIdentifier&   equipmentID                    */
            /*     const char *              equipmentMode                  */
            /*     const objectIdentifier&   equipmentStatusCode            */
            /*     const objectIdentifier&   lotID                          */
            /*     const char *              lotProcessState                */
            /*     const char *              lotHoldState                   */
            /*     const objectIdentifier&   cassetteID                     */
            /*     const char *              cassetteTransferState          */
            /*     CORBA::Boolean            cassetteTransferReserveFlag    */
            /*     CORBA::Boolean            cassetteDispatchReserveFlag    */
            /*     const objectIdentifier&   durableID                      */
            /*     const char *              durableTransferState           */
            /*--------------------------------------------------------------*/
            Inputs.MessageQueuePutIn messageQueuePutIn = new Inputs.MessageQueuePutIn();
            messageQueuePutIn.setLotID(strChangedLotAttributes.get(i).getLotID());
            messageQueuePutIn.setCassetteDispatchReserveFlag(false);
            messageQueuePutIn.setCassetteTransferReserveFlag(false);
            messageMethod.messageQueuePut(objCommon, messageQueuePutIn);
        }
        return Response.createSucc(transactionID, strProdOrderChangeReqResult);
    }

    @ResponseBody
    @RequestMapping(value = "/lot_ext_priority_modify/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.LOT_EXTERNAL_PRIORITY_CHANGE_REQ)
    public Response lotExtPriorityModifyReq(@RequestBody Params.LotExtPriorityModifyReqParams params) {
        //======Pre Process======
        final String transactionID = TransactionIDEnum.LOT_EXTERNAL_PRIORITY_CHANGE_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //Step2 - txAccessControlCheckInq
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), accessControlCheckInqParams);

        //=======Main Process=======
        List<Results.LotExternalPriorityResult> resultList = new ArrayList<>();

        List<Infos.LotExternalPriority> lotExternalPriorityList = params.getLotExternalPriorityList();
        //if support commit by every lot
        AtomicInteger errorLotCount = new AtomicInteger(0);
        if (CimBooleanUtils.isTrue(params.getCommitByLotFlag())) {
            //Step3 - lotExternalPriorityChangeByLot
            planService.lotExternalPriorityChangeByLot(objCommon, lotExternalPriorityList, errorLotCount, resultList);
            if (errorLotCount.get() == lotExternalPriorityList.size()) {
                throw new ServiceException(retCodeConfig.getAllRequestFail());

            } else if (errorLotCount.get() != 0 && errorLotCount.get() != lotExternalPriorityList.size()) {
                throw new ServiceException(retCodeConfig.getSomeLotFailed());
            }
        } else {
            //Step4 - lotExternalPriorityChangeByAll
            planService.lotExternalPriorityChangeByAll(objCommon, lotExternalPriorityList, errorLotCount, resultList);
            if (errorLotCount.get() != 0) {
                throw new ServiceException(retCodeConfig.getAllRequestFail());
            }
        }
        return Response.createSucc(transactionID, resultList);
    }

    @ResponseBody
    @RequestMapping(value = "/old/lot_current_queue_reactivate/req", method = RequestMethod.POST)
    //@CimMapping(TransactionIDEnum.LOT_REQUEUE_REQ)
    public Response lotCurrentQueueReactivateReqOld(@RequestBody Params.LotCurrentQueueReactivateReqParams params) {

        //init params
        final String transactionID = TransactionIDEnum.LOT_REQUEUE_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //【step2】call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        List<ObjectIdentifier> lotIDLists = new ArrayList<>();
        int size = CimArrayUtils.getSize(params.getLotReQueueAttributesList());
        for (int i = 0; i < size; i++) {
            Infos.LotReQueueAttributes lotReQueueAttributes = params.getLotReQueueAttributesList().get(i);
            lotIDLists.add(lotReQueueAttributes.getLotID());
        }
        accessControlCheckInqParams.setLotIDLists(lotIDLists);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), accessControlCheckInqParams);
        //【step3】call sxLotCurrentQueueReactivateReq(...)
        log.debug("【step3】call sxLotCurrentQueueReactivateReq(...)");
        Results.LotCurrentQueueReactivateReqResult lotCurrentQueueReactivateReqResult = planService.sxLotRequeueReq(objCommon, params.getLotReQueueAttributesList());
        //【step4】judge whether the return code is success, if no, then TCC will rollback
        log.debug("【step4】judge whether the return code is success, if no, then TCC will rollback");
        return Response.createSucc(transactionID, lotCurrentQueueReactivateReqResult);
    }

    @Override
    @ResponseBody
    @RequestMapping(value = "/lot_current_queue_reactivate/req", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.LOT_REQUEUE_REQ)
    @EnablePostProcess
    public Response lotCurrentQueueReactivateReq(@RequestBody Params.LotCurrentQueueReactivateReqParams params) {

        //init params
        final String transactionID = TransactionIDEnum.LOT_REQUEUE_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //【step2】call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        List<ObjectIdentifier> lotIDLists = new ArrayList<>();
        int size = CimArrayUtils.getSize(params.getLotReQueueAttributesList());
        for (int i = 0; i < size; i++) {
            Infos.LotReQueueAttributes lotReQueueAttributes = params.getLotReQueueAttributesList().get(i);
            lotIDLists.add(lotReQueueAttributes.getLotID());
        }
        accessControlCheckInqParams.setLotIDLists(lotIDLists);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID,
                params.getUser(), accessControlCheckInqParams);
        //【step3】call sxLotCurrentQueueReactivateReq(...)
        log.debug("【step3】call sxLotCurrentQueueReactivateReq(...)");
        Results.LotCurrentQueueReactivateReqResult result = planService.sxLotRequeueReq(objCommon,
                params.getLotReQueueAttributesList());
        //【step4】judge whether the return code is success, if no, then TCC will rollback
        log.debug("【step4】judge whether the return code is success, if no, then TCC will rollback");
        return Response.createSucc(transactionID, result);
        //-----------------------------
        // PostProcess
        //-----------------------------
    }

    @ResponseBody
    @RequestMapping(value = "/lot_plan_change/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.LOT_SCHEDULE_CHANGE_REQ)
    public Response lotPlanChangeReq(@RequestBody Params.LotScheduleChangeReqParams params) {
        //init params
        final String transactionID = TransactionIDEnum.LOT_SCHEDULE_CHANGE_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        List<Infos.ReScheduledLotAttributes> reScheduledLotAttributesList = params.getReScheduledLotAttributesList();
        Validations.check(CimObjectUtils.isEmpty(reScheduledLotAttributesList), retCodeConfig.getInvalidInputParam(), transactionID);

        //【step1】get schedule from calendar
        log.debug("【step1】get schedule from calendar");

        //【step2】call txAccessControlCheckInq(...)
        List<Infos.ChangeLotScheduleReturn> changeLotScheduleReturns = new ArrayList<>();
        List<ObjectIdentifier> lotIDs = new ArrayList<>();
        List<ObjectIdentifier> routeIDs = new ArrayList<>();
        List<ObjectIdentifier> productIDs = new ArrayList<>();
        reScheduledLotAttributesList.forEach(x -> {
            lotIDs.add(x.getLotID());
            changeLotScheduleReturns.add(new Infos.ChangeLotScheduleReturn(x.getLotID(), String.valueOf(retCodeConfig.getSucc().getCode())));
            ObjectIdentifier routeID = x.getRouteID();
            ObjectIdentifier productID = x.getProductID();
            if (!ObjectIdentifier.isEmpty(routeID)) {
                routeIDs.add(routeID);
            }
            if (!ObjectIdentifier.isEmpty(productID)) {
                productIDs.add(productID);
            }
        });
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setLotIDLists(lotIDs);
        accessControlCheckInqParams.setRouteIDList(routeIDs);
        accessControlCheckInqParams.setProductIDList(productIDs);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), accessControlCheckInqParams);

        /*-----------------------------------------------------------------------*/
        /*   Main Process                                                        */
        /*-----------------------------------------------------------------------*/
        boolean msgFlg = false;
        for (int i = 0, j = reScheduledLotAttributesList.size(); i < j; i++) {
            log.debug("【step3】call sxLotScheduleChangeReq(...)");
            try {
                List<Infos.ChangeLotScheduleReturn> sxLotScheduleChangeReqResult = planService.sxLotScheduleChangeReq(objCommon, params);
                //-------------------------------------------------------
                // Post-Processing Registration Section and Execution
                //-------------------------------------------------------
                //Temporarily not needed Post Process
                Params.PostTaskRegisterReqParams postTaskRegisterReqParams = new Params.PostTaskRegisterReqParams(objCommon.getTransactionID(), null, null, -1,
                        new Infos.PostProcessRegistrationParam(null, null, Arrays.asList(reScheduledLotAttributesList.get(i).getLotID()), null), "");
                postController.execPostProcess(objCommon, postTaskRegisterReqParams);

            } catch (ServiceException ex) {
                if (Validations.isEquals(ex.getCode(), retCodeConfig.getSomeLotidDataError())) {
                    msgFlg = true;
                } else if (Validations.isEquals(ex.getCode(), retCodeConfig.getInprocessLotinfoUpdate())) {
                } else {
                    break;
                }
            }
        }
        OmCode succ = retCodeConfig.getSucc();
        if (msgFlg) {
            succ.setMessage("Since Schedule Change of Lot[********] was Processing and it has not been performed, it was registered into SchduleChangeReservation. ");
        }
        return Response.createSuccWithOmCode(transactionID, succ, null);
    }

    @ResponseBody
    @RequestMapping(value = "/new_prod_order_cancel/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.NEW_LOT_RELEASE_CANCEL_REQ)
    public Response newProdOrderCancelReq(@RequestBody Params.NewProdOrderCancelReqParams params) {
        log.debug("params:%s", JSONObject.toJSONString(params));

        //Step-0:Initialize Parameters;
        final String transactionID = TransactionIDEnum.NEW_LOT_RELEASE_CANCEL_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //Step-3:txAccessControlCheckInq;
        log.debug("txAccessControlCheckInq(...)");
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), accessControlCheckInqParams);

        //Step-4:txNewProdOrderCancelReq;
        log.debug("【Step-4】call-txNewProdOrderCancelReq(...)");
        List<Infos.ReleaseCancelLotReturn> result = planService.sxNewProdOrderCancelReq(params, objCommon);
        return Response.createSucc(transactionID, result);
    }

    @ResponseBody
    @RequestMapping(value = "/new_prod_order_create/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.NEW_LOT_RELEASE_REQ)
    public Response newProdOrderCreateReq(@RequestBody Params.NewProdOrderCreateReqParams params) {
        // print the in-param
        log.debug("params:%s", JSONObject.toJSONString(params));
        //【step0】init params
        final String transactionID = TransactionIDEnum.NEW_LOT_RELEASE_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);
        List<Infos.ReleaseLotAttributes> releaseLotAttributesList = params.getReleaseLotAttributesList();

        //【step1】get schedule from calendar

        //【step2】call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        List<ObjectIdentifier> productIDList = new ArrayList<>();
        for (int i = 0; i < CimArrayUtils.getSize(params.getReleaseLotAttributesList()); i++) {
            Infos.ReleaseLotAttributes releaseLotAttributes = params.getReleaseLotAttributesList().get(i);
            productIDList.add(releaseLotAttributes.getProductID());
        }
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), accessControlCheckInqParams);

        //【step3】main process
        Results.NewProdOrderCreateReqResult result = new Results.NewProdOrderCreateReqResult();

        List<Infos.ReleasedLotReturn> releasedLotReturnList = new ArrayList<>();
        for (Infos.ReleaseLotAttributes pptReleaseLotAttributes : releaseLotAttributesList) {
            result = planService.sxNewProdOrderCreateReq(objCommon, pptReleaseLotAttributes);
            releasedLotReturnList = CimArrayUtils.join(releasedLotReturnList, result.getReleasedLotReturnList());
        }
        result.setReleasedLotReturnList(releasedLotReturnList);

        return Response.createSucc(transactionID, result);
    }

    @ResponseBody
    @RequestMapping(value = "/new_prod_order_modify/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.NEW_LOT_UPDATE_REQ)
    public Response newProdOrderModifyReq(@RequestBody Params.NewProdOrderModifyReqParams params) {
        // print the in-param
        log.debug("newLotUpdatedReqParams:%s", JSONObject.toJSONString(params));

        //init params
        final String transactionID = TransactionIDEnum.NEW_LOT_UPDATE_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //【step2】call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        List<ObjectIdentifier> productIDLists = new ArrayList<>();
        int size = CimArrayUtils.getSize(params.getUpdateLotAttributesList());
        for (int i = 0; i < size; i++) {
            Infos.UpdateLotAttributes updateLotAttributes = params.getUpdateLotAttributesList().get(i);
            productIDLists.add(updateLotAttributes.getProductID());
        }
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), accessControlCheckInqParams);
        //【step3】call sxNewProdOrderModifyReq(...)
        log.debug("【step3】call sxNewProdOrderModifyReq(...)");
        List<Infos.ReleasedLotReturn> out = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            Infos.UpdateLotAttributes updateLotAttributes = params.getUpdateLotAttributesList().get(i);
            Infos.ReleasedLotReturn releasedLotReturn = planService.sxNewProdOrderModifyReq(objCommon, updateLotAttributes);
            out.add(releasedLotReturn);
        }

        //【step4】judge whether the return code is success, if no, then TCC will rollback
        log.debug("【step4】judge whether the return code is success, if no, then TCC will rollback");
        return Response.createSucc(transactionID, out);
    }

    @ResponseBody
    @RequestMapping(value = "/lot_plan_change_reserve_create/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.SCHEDULE_CHANGERESERVATION_CREATE_REQ)
    public Response lotPlanChangeReserveCreateReq(@RequestBody Params.LotPlanChangeReserveCreateReqParams params) {
        // print the in-param
        log.debug("lotPlanChangeReserveCreateReq:%s", JSONObject.toJSONString(params));

        //init params
        final String transactionID = TransactionIDEnum.SCHEDULE_CHANGERESERVATION_CREATE_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //【step2】call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        List<ObjectIdentifier> lotIDs = new ArrayList<>();
        List<ObjectIdentifier> productIDs = new ArrayList<>();
        List<ObjectIdentifier> routeIDs = new ArrayList<>();
        Infos.SchdlChangeReservation schdlChangeReservation = params.getSchdlChangeReservation();
        if (CimStringUtils.equals(schdlChangeReservation.getObjectType(), BizConstant.SP_SCHDL_CHG_OBJTYPE_LOT)
                && !ObjectIdentifier.isEmptyWithValue(schdlChangeReservation.getObjectID())) {
            lotIDs.add(schdlChangeReservation.getObjectID());
            accessControlCheckInqParams.setLotIDLists(lotIDs);
        }
        if (!ObjectIdentifier.isEmptyWithValue(schdlChangeReservation.getProductID())) {
            productIDs.add(schdlChangeReservation.getProductID());
            accessControlCheckInqParams.setProductIDList(productIDs);
        }
        if (!ObjectIdentifier.isEmptyWithValue(schdlChangeReservation.getRouteID())) {
            routeIDs.add(schdlChangeReservation.getRouteID());
            accessControlCheckInqParams.setRouteIDList(routeIDs);
        }
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), accessControlCheckInqParams);
        //【step3】call txLotPlanChangeReserveCreateReq__110(...)
        log.debug("【step3】call txLotPlanChangeReserveCreateReq__110(...)");
        planService.sxLotPlanChangeReserveCreateReq(objCommon, schdlChangeReservation, params.getClaimMemo());
        Response response = Response.createSucc(transactionID);
        log.debug("lotPlanChangeReserveCreateReqs - Response:%s", JSONObject.toJSONString(response));
        return response;
    }

    @ResponseBody
    @RequestMapping(value = "/lot_plan_change_reserve_modify/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.SCHEDULE_CHANGERESERVATION_CHANGE_REQ)
    public Response lotPlanChangeReserveModifyReq(@RequestBody Params.LotPlanChangeReserveModifyReqParams params) {
        // print the in-param
        log.debug("lotPlanChangeReserveModifyReq:%s", JSONObject.toJSONString(params));

        //init params
        final String transactionID = TransactionIDEnum.SCHEDULE_CHANGERESERVATION_CHANGE_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //【step2】call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        List<ObjectIdentifier> lotIDs = new ArrayList<>();
        List<ObjectIdentifier> productIDs = new ArrayList<>();
        List<ObjectIdentifier> routeIDs = new ArrayList<>();
        Infos.SchdlChangeReservation strNewSchdlChangeReservation = params.getStrNewSchdlChangeReservation();
        if (CimStringUtils.equals(strNewSchdlChangeReservation.getObjectType(), BizConstant.SP_SCHDL_CHG_OBJTYPE_LOT)
                && !ObjectIdentifier.isEmptyWithValue(strNewSchdlChangeReservation.getObjectID())) {
            lotIDs.add(strNewSchdlChangeReservation.getObjectID());
        }
        if (!ObjectIdentifier.isEmptyWithValue(strNewSchdlChangeReservation.getProductID())) {
            productIDs.add(strNewSchdlChangeReservation.getProductID());
        }
        if (!ObjectIdentifier.isEmptyWithValue(strNewSchdlChangeReservation.getRouteID())) {
            routeIDs.add(strNewSchdlChangeReservation.getRouteID());
        }
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), accessControlCheckInqParams);
        //【step3】call txLotPlanChangeReserveModifyReq__110(...)
        log.debug("【step3】call txLotPlanChangeReserveModifyReq__110(...)");
        planService.sxLotPlanChangeReserveModifyReq(objCommon, params.getStrCurrentSchdlChangeReservation(), params.getStrNewSchdlChangeReservation(), params.getClaimMemo());
        return Response.createSucc(transactionID);
    }

    @ResponseBody
    @RequestMapping(value = "/lot_plan_change_reserve_cancel/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.SCHEDULE_CHANGERESERVATION_CANCEL_REQ)
    public Response lotPlanChangeReserveCancelReq(@RequestBody Params.LotPlanChangeReserveCancelReqParams params) {
        // print the in-param
        log.debug("lotPlanChangeReserveCancelReq:%s", JSONObject.toJSONString(params));

        //init params
        final String transactionID = TransactionIDEnum.SCHEDULE_CHANGERESERVATION_CANCEL_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //【step2】call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        List<ObjectIdentifier> lotIDs = new ArrayList<>();
        List<ObjectIdentifier> productIDs = new ArrayList<>();
        List<ObjectIdentifier> routeIDs = new ArrayList<>();
        List<Infos.SchdlChangeReservation> strSchdlChangeReservations = params.getStrSchdlChangeReservations();
        int schdlReserveLen = CimArrayUtils.getSize(strSchdlChangeReservations);
        for (int i = 0; i < schdlReserveLen; i++) {
            Infos.SchdlChangeReservation schdlChangeReservation = strSchdlChangeReservations.get(i);
            if (CimStringUtils.equals(schdlChangeReservation.getObjectType(), BizConstant.SP_SCHDL_CHG_OBJTYPE_LOT)
                    && !ObjectIdentifier.isEmptyWithValue(schdlChangeReservation.getObjectID())) {
                lotIDs.add(schdlChangeReservation.getObjectID());
            }
            if (!ObjectIdentifier.isEmptyWithValue(schdlChangeReservation.getProductID())) {
                productIDs.add(schdlChangeReservation.getProductID());
            }
            if (!ObjectIdentifier.isEmptyWithValue(schdlChangeReservation.getRouteID())) {
                routeIDs.add(schdlChangeReservation.getRouteID());
            }
        }
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), accessControlCheckInqParams);
        //【step3】call txLotPlanChangeReserveCancelReq__110(...)
        log.debug("【step3】call txLotPlanChangeReserveCancelReq__110(...)");
        List<Infos.SchdlChangeReservation> schdlChangeReservationList = planService.sxLotPlanChangeReserveCancelReq(objCommon, strSchdlChangeReservations, params.getClaimMemo());
        return Response.createSucc(transactionID, schdlChangeReservationList);
    }

    @ResponseBody
    @RequestMapping(value = "/step_content_reset_by_lot/req", method = RequestMethod.POST)
    @Override
    public Response stepContentResetByLotReq(@RequestBody Params.StepContentResetByLotReqParams params) {
        //Step0 - init params
        final String transactionID = TransactionIDEnum.WIPLOT_RESET_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //Step1 - txAccessControlCheckInq
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        Infos.StepContentResetByLotReqInParm stepContentResetByLotReqInParm = params.getStepContentResetByLotReqInParm();
        List<ObjectIdentifier> lotIDs = stepContentResetByLotReqInParm.getStrWIPLotResetAttributes().stream().map(Infos.WIPLotResetAttribute::getLotID).collect(Collectors.toList());
        accessControlCheckInqParams.setLotIDLists(lotIDs);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), accessControlCheckInqParams);

        int failCnt = 0;
        List<Infos.WIPLotResetResult> results = new ArrayList<>();
        for (ObjectIdentifier lotID : lotIDs) {

            //Step3 - TxStepContentResetByLotReq
            List<Infos.WIPLotResetResult> wipLotResetResults = new ArrayList<>();
            try {
                wipLotResetResults = planService.sxStepContentResetByLotReq(objCommon, stepContentResetByLotReqInParm, params.getClaimMemo());
                //-----------------------------
                // Call postProcess
                //-----------------------------
                log.info("Call postProcess");
                Params.PostTaskRegisterReqParams postTaskRegisterReqParams = new Params.PostTaskRegisterReqParams(objCommon.getTransactionID(), null, null, -1,
                        new Infos.PostProcessRegistrationParam(null, null, Arrays.asList(lotID), null), "");
                postController.execPostProcess(objCommon, postTaskRegisterReqParams);

            } catch (ServiceException ex) {
                wipLotResetResults.add(new Infos.WIPLotResetResult(new RetCode(ex.getTransactionID(), new OmCode(ex.getCode(), ex.getMessage()), null)));
                failCnt++;
            }
            Infos.WIPLotResetResult wipLotResetResult = new Infos.WIPLotResetResult();
            wipLotResetResult.setLotID(lotID);
            wipLotResetResult.setStrResult(wipLotResetResults.get(0).getStrResult());
            results.add(wipLotResetResult);
        }
        Validations.check(failCnt > 0, results, retCodeConfig.getSomeRequestsFailed());
        return Response.createSucc(transactionID, results);
    }
}
