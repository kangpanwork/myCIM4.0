package com.fa.cim.controller.lot;

import com.alibaba.fastjson.JSONObject;
import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.Response;
import com.fa.cim.common.support.User;
import com.fa.cim.common.utils.*;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.config.RetCodeConfigEx;
import com.fa.cim.controller.interfaces.lot.ILotController;
import com.fa.cim.controller.post.PostController;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;
import com.fa.cim.dto.lot.TerminateReq;
import com.fa.cim.frameworks.pprocess.api.annotations.EnablePostProcess;
import com.fa.cim.frameworks.pprocess.proxy.impl.*;
import com.fa.cim.lot.LotNpwUsageRecycleCountUpdateParams;
import com.fa.cim.lot.LotNpwUsageRecycleLimitUpdateParams;
import com.fa.cim.middleware.standard.api.annotations.listener.CimMapping;
import com.fa.cim.middleware.standard.api.annotations.listener.Listenable;
import com.fa.cim.service.access.IAccessInqService;
import com.fa.cim.service.lot.ILotService;
import com.fa.cim.service.post.IPostService;
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

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/7/26          ********            Nyx                create file
 *
 * @author: Nyx
 * @date: 2019/7/26 15:21
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@RestController
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
@Compensable(interfaceClass = ILotController.class, confirmableKey = "LotConfirm", cancellableKey = "LotCancel")
@RequestMapping("/lot")
@Listenable
public class LotController implements ILotController {
    @Autowired
    private IAccessInqService accessInqService;
    @Autowired
    private IPostService postService;
    @Autowired
    private RetCodeConfig retCodeConfig;
    @Autowired
    private RetCodeConfigEx retCodeConfigEx;
    @Autowired
    private PostController postController;

    @Autowired
    private ILotService lotService;

    @ResponseBody
    @RequestMapping(value = "/old/hold_lot_release/req", method = RequestMethod.POST)
    //@CimMapping(TransactionIDEnum.HOLD_LOT_RELEASE_REQ)
    public Response holdLotReleaseReqOld(@RequestBody Params.HoldLotReleaseReqParams holdLotReleaseReqParams) {
        String transactionID = TransactionIDEnum.HOLD_LOT_RELEASE_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);
        //step2 - call txAccessControlCheckInq(...)
        log.debug("[step-2] call txAccessControlCheckInq(...)");
        User user = holdLotReleaseReqParams.getUser();
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        List<ObjectIdentifier> lotIDLists = new ArrayList<>();
        lotIDLists.add(holdLotReleaseReqParams.getLotID());
        accessControlCheckInqParams.setLotIDLists(lotIDLists);
        accessControlCheckInqParams.conversionDepartmentCheckCode(holdLotReleaseReqParams.getHoldReqList());
        accessControlCheckInqParams.conversionDepartmentCheckCode(holdLotReleaseReqParams.getDepartment(), holdLotReleaseReqParams.getSection(), holdLotReleaseReqParams.getReleaseReasonCodeID());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, user, accessControlCheckInqParams);

        //step 3
        boolean reHold = false;
        try {
            lotService.sxHoldLotReleaseReq(objCommon, holdLotReleaseReqParams);
        } catch (ServiceException e) {
            if (e.getCode() == 3281) {
                reHold = true;
            } else {
                throw e;
            }
        }

        //step 4  txPostTaskRegisterReq__100
        //-------------------------------------
        // Post-Processing Registration Section
        //-------------------------------------
        Results.PostTaskRegisterReqResult postTaskRegisterReqResultOut = null;

        log.debug("txHoldLotReleaseReq :rc == RC_OK");
        Params.PostTaskRegisterReqParams strPostProcessRegistrationParm = new Params.PostTaskRegisterReqParams();
        strPostProcessRegistrationParm.setTransactionID(objCommon.getTransactionID());//OLOTW003  TXID
        strPostProcessRegistrationParm.setSequenceNumber(-1);//sequenceNumber
        strPostProcessRegistrationParm.setClaimMemo("");//claimMemo
        strPostProcessRegistrationParm.setPatternID(null);//patternID
        strPostProcessRegistrationParm.setKey(null);//dkey
        Infos.PostProcessRegistrationParam postProcessRegistrationParam = new Infos.PostProcessRegistrationParam();
        List<ObjectIdentifier> lotIdList = new ArrayList<>();
        lotIdList.add(holdLotReleaseReqParams.getLotID());
        postProcessRegistrationParam.setLotIDs(lotIdList);
        strPostProcessRegistrationParm.setPostProcessRegistrationParm(postProcessRegistrationParam);
        postTaskRegisterReqResultOut = postService.sxPostTaskRegisterReq(objCommon, strPostProcessRegistrationParm);

        //【step5】call TxPostTaskExecuteReq__100
        //----------------------------------
        // Post-Processing Execution Section
        //----------------------------------
        log.debug("txSkipReq and txPostTaskRegisterReq__100 :rc == RC_OK");
        Params.PostTaskExecuteReqParams parms = new Params.PostTaskExecuteReqParams();
        parms.setUser(user);
        parms.setKey(postTaskRegisterReqResultOut.getDKey());
        parms.setSyncFlag(1);
        parms.setPreviousSequenceNumber(0);
        parms.setKeyTimeStamp(null);
        Response postTaskExecuteReqRsponse = postController.postTaskExecuteReq(parms);


        //【step4】judge whether the return code is success, if no, then TCC will rollback
        if (reHold) {
            return Response.createSuccWithOmCode(transactionID, retCodeConfigEx.getContaminationLevelMatchState(), null);
        }
        return Response.createSucc(transactionID, null);
    }

    @Override
    @ResponseBody
    @RequestMapping(value = "/hold_lot_release/req", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.HOLD_LOT_RELEASE_REQ)
    @EnablePostProcess(proxy = HoldLotReleaseRequestProxy.class)
    public Response holdLotReleaseReq(@RequestBody Params.HoldLotReleaseReqParams holdLotReleaseReqParams) {
        String transactionID = TransactionIDEnum.HOLD_LOT_RELEASE_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);
        //step2 - call txAccessControlCheckInq(...)
        log.debug("[step-2] call txAccessControlCheckInq(...)");
        User user = holdLotReleaseReqParams.getUser();
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        List<ObjectIdentifier> lotIDLists = new ArrayList<>();
        lotIDLists.add(holdLotReleaseReqParams.getLotID());
        accessControlCheckInqParams.setLotIDLists(lotIDLists);
        accessControlCheckInqParams.conversionDepartmentCheckCode(holdLotReleaseReqParams.getHoldReqList());
        accessControlCheckInqParams.conversionDepartmentCheckCode(holdLotReleaseReqParams.getDepartment(),
                holdLotReleaseReqParams.getSection(),
                holdLotReleaseReqParams.getReleaseReasonCodeID());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, user,
                accessControlCheckInqParams);

        //step 3
        boolean reHold = false;
        try {
            lotService.sxHoldLotReleaseReq(objCommon, holdLotReleaseReqParams);
        } catch (ServiceException e) {
            if (e.getCode() == 3281) {
                reHold = true;
            } else {
                throw e;
            }
        }
        //【step4】judge whether the return code is success, if no, then TCC will rollback
        if (reHold) {
            return Response.createSuccWithOmCode(transactionID, retCodeConfigEx.getContaminationLevelMatchState()
                    , null);
        }
        return Response.createSucc(transactionID);
        //-----------------------------
        // PostProcess
        //-----------------------------
    }

    @ResponseBody
    @RequestMapping(value = "/old/hold_lot/req", method = RequestMethod.POST)
    //@CimMapping(TransactionIDEnum.HOLD_LOT_REQ)
    public Response holdLotReqOld(@RequestBody Params.HoldLotReqParams holdLotReqParams) {
        String transactionID = TransactionIDEnum.HOLD_LOT_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //step2 call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        List<ObjectIdentifier> lotIDLists = new ArrayList<>();
        lotIDLists.add(holdLotReqParams.getLotID());
        accessControlCheckInqParams.setLotIDLists(lotIDLists);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, holdLotReqParams.getUser(), accessControlCheckInqParams);

        //step 3
        lotService.sxHoldLotReq(objCommon, holdLotReqParams.getLotID(), holdLotReqParams.getHoldReqList());

        Params.PostTaskRegisterReqParams params = new Params.PostTaskRegisterReqParams();
        params.setSequenceNumber(-1);
        Infos.PostProcessRegistrationParam postProcessRegistrationParam = new Infos.PostProcessRegistrationParam();
        postProcessRegistrationParam.setLotIDs(Arrays.asList(holdLotReqParams.getLotID()));
        params.setPostProcessRegistrationParm(postProcessRegistrationParam);
        //【2019/10/12】add by bear
        params.setTransactionID(objCommon.getTransactionID());
        params.setUser(objCommon.getUser());

        Results.PostTaskRegisterReqResult postTaskRegisterReqResult = postService.sxPostTaskRegisterReq(objCommon, params);

        Params.PostTaskExecuteReqParams postTaskExecuteReqParams = new Params.PostTaskExecuteReqParams();
        postTaskExecuteReqParams.setKey(postTaskRegisterReqResult.getDKey());
        postTaskExecuteReqParams.setSyncFlag(1);
        postTaskExecuteReqParams.setPreviousSequenceNumber(0);
        postTaskExecuteReqParams.setUser(holdLotReqParams.getUser());
        postController.postTaskExecuteReq(postTaskExecuteReqParams);

        return Response.createSucc(transactionID, null);

    }

    @Override
    @ResponseBody
    @RequestMapping(value = "/hold_lot/req", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.HOLD_LOT_REQ)
    @EnablePostProcess
    public Response holdLotReq(@RequestBody Params.HoldLotReqParams holdLotReqParams) {
        String transactionID = TransactionIDEnum.HOLD_LOT_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //step2 call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        List<ObjectIdentifier> lotIDLists = new ArrayList<>();
        lotIDLists.add(holdLotReqParams.getLotID());
        accessControlCheckInqParams.setLotIDLists(lotIDLists);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID,
                holdLotReqParams.getUser(), accessControlCheckInqParams);

        //step 3
        lotService.sxHoldLotReq(objCommon, holdLotReqParams.getLotID(), holdLotReqParams.getHoldReqList());

        return Response.createSucc(transactionID);
        //-----------------------------
        // PostProcess
        //-----------------------------
    }

    @ResponseBody
    @RequestMapping(value = "/merge_lot_not_on_pf/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.MERGE_WAFER_LOT_NOT_ON_ROUTE_REQ)
    public Response mergeLotNotOnPfReq(@RequestBody Params.MergeLotNotOnPfReqParams mergeLotNotOnPfReqParams) {
        String txId = TransactionIDEnum.MERGE_WAFER_LOT_NOT_ON_ROUTE_REQ.getValue();
        ThreadContextHolder.setTransactionId(txId);
        //set current tx id
        User user = mergeLotNotOnPfReqParams.getUser();

        //step2 - call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        List<ObjectIdentifier> lotIDLists = new ArrayList<>();
        lotIDLists.add(mergeLotNotOnPfReqParams.getChildLotID());
        lotIDLists.add(mergeLotNotOnPfReqParams.getParentLotID());
        accessControlCheckInqParams.setLotIDLists(lotIDLists);

        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, user, accessControlCheckInqParams);
        /*-----------------------------------------------------------------------*/
        /*   Main Process                                                        */
        /*-----------------------------------------------------------------------*/
        lotService.sxMergeLotNotOnPfReq(objCommon, mergeLotNotOnPfReqParams);

        return Response.createSucc(txId, null);
    }

    @ResponseBody
    @RequestMapping(value = "/old/merge_lot/req", method = RequestMethod.POST)
    //@CimMapping(TransactionIDEnum.MERGE_WAFER_LOT_REQ)
    public Response mergeLotReqOld(@RequestBody Params.MergeLotReqParams mergeLotReqParams) {
        String transactionID = TransactionIDEnum.MERGE_WAFER_LOT_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        List<ObjectIdentifier> lotIDLists = new ArrayList<>();
        lotIDLists.add(mergeLotReqParams.getChildLotID());
        lotIDLists.add(mergeLotReqParams.getParentLotID());
        accessControlCheckInqParams.setLotIDLists(lotIDLists);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, mergeLotReqParams.getUser(), accessControlCheckInqParams);

        /*-----------------------------------------------------------------------*/
        /*   Main Process                                                        */
        /*-----------------------------------------------------------------------*/
        lotService.sxMergeLotReq(objCommon, mergeLotReqParams);
        //-----------------------------
        // Call postProcess
        //-----------------------------
        log.info("Call postProcess");
        Params.PostTaskRegisterReqParams postTaskRegisterReqParams = new Params.PostTaskRegisterReqParams(transactionID, null, null, -1,
                new Infos.PostProcessRegistrationParam(null, null, Collections.singletonList(mergeLotReqParams.getParentLotID()), null), "");
        postController.execPostProcess(objCommon, postTaskRegisterReqParams);
        return Response.createSucc(transactionID);
    }

    @Override
    @ResponseBody
    @RequestMapping(value = "/merge_lot/req", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.MERGE_WAFER_LOT_REQ)
    @EnablePostProcess
    public Response mergeLotReq(@RequestBody Params.MergeLotReqParams mergeLotReqParams) {
        String transactionID = TransactionIDEnum.MERGE_WAFER_LOT_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        List<ObjectIdentifier> lotIDLists = new ArrayList<>();
        lotIDLists.add(mergeLotReqParams.getChildLotID());
        lotIDLists.add(mergeLotReqParams.getParentLotID());
        accessControlCheckInqParams.setLotIDLists(lotIDLists);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID,
                mergeLotReqParams.getUser(), accessControlCheckInqParams);

        /*-----------------------------------------------------------------------*/
        /*   Main Process                                                        */
        /*-----------------------------------------------------------------------*/
        lotService.sxMergeLotReq(objCommon, mergeLotReqParams);

        return Response.createSucc(transactionID);
        //-----------------------------
        // PostProcess
        //-----------------------------
    }

    @ResponseBody
    @RequestMapping(value = "/old/skip/req", method = RequestMethod.POST)
    //@CimMapping(TransactionIDEnum.OPE_LOCATE_REQ)
    public Response skipReqOld(@RequestBody Params.SkipReqParams params) {
        log.info("SkipReqController::skipReq()");
        //init params
        final String transactionID = TransactionIDEnum.OPE_LOCATE_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        log.debug("【step0】check input params");
        ObjectIdentifier lotID = params.getLotID();
        Validations.check(ObjectIdentifier.isEmpty(lotID), "the lotID is null");

        log.debug("【step2】call txAccessControlCheckInq(...)");
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setLotIDLists(Arrays.asList(lotID));

        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), accessControlCheckInqParams);

        log.debug("【step3】call sxSkipReq(...)");
        lotService.sxSkipReq(objCommon, params);

        //-----------------------------
        // Call postProcess
        //-----------------------------
        log.info("Call postProcess");
        Params.PostTaskRegisterReqParams postTaskRegisterReqParams = new Params.PostTaskRegisterReqParams(objCommon.getTransactionID(), null, null, -1,
                new Infos.PostProcessRegistrationParam(null, null, Arrays.asList(lotID), null), "");
        postController.execPostProcess(objCommon, postTaskRegisterReqParams);
        return Response.createSucc(transactionID);
    }

    @Override
    @ResponseBody
    @RequestMapping(value = "/skip/req", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.OPE_LOCATE_REQ)
    @EnablePostProcess
    public Response skipReq(@RequestBody Params.SkipReqParams params) {
        //init params
        if (log.isDebugEnabled()) log.debug("step1 : Initialising transactionID");
        final String transactionID = TransactionIDEnum.OPE_LOCATE_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        ObjectIdentifier lotID = params.getLotID();
        Validations.check(ObjectIdentifier.isEmpty(lotID), retCodeConfig.getInvalidParameter());

        if (log.isDebugEnabled()) log.debug("step2 : Privilege Check");
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setLotIDLists(Collections.singletonList(lotID));
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID,
                params.getUser(),
                accessControlCheckInqParams);

        //---------------------------
        // Main Process
        //---------------------------
        if (log.isDebugEnabled()) log.debug(" step3: Main Process. call sxSkipReq(...)");
        lotService.sxSkipReq(objCommon, params);

        return Response.createSucc(transactionID);
        //-----------------------------
        // Call postProcess
        //-----------------------------
    }

    @ResponseBody
    @RequestMapping(value = "/old/partial_rework/req", method = RequestMethod.POST)
    /*@CimMapping({TransactionIDEnum.PARTIAL_REWORK_FORCE_AND_DYNAMIC_ROUTE, TransactionIDEnum.PARTIAL_REWORK_FORCE,
            TransactionIDEnum.POST_PARTIAL_REWORK_FORCE_AND_DYNAMIC_ROUTE, TransactionIDEnum.POST_PARTIAL_REWORK_FORCE,
            TransactionIDEnum.PARTIAL_REWORK_DYNAMIC_ROUTE, TransactionIDEnum.PARTIAL_REWORK,
            TransactionIDEnum.POST_PARTIAL_REWORK_DYNAMIC_ROUTE, TransactionIDEnum.POST_PARTIAL_REWORK})*/
    public Response partialReworkReqOld(@RequestBody Params.PartialReworkReqParams params) {
        //【step0】init params: Decide TX_ID;
        TransactionIDEnum partialReworkTransactionID;
        TransactionIDEnum postPartialReworkTransactionID;
        if (CimBooleanUtils.isTrue(params.getPartialReworkReqInformation().getBForceRework())) {
            partialReworkTransactionID = CimBooleanUtils.isTrue(params.getPartialReworkReqInformation().getBDynamicRoute())
                    ? TransactionIDEnum.PARTIAL_REWORK_FORCE_AND_DYNAMIC_ROUTE : TransactionIDEnum.PARTIAL_REWORK_FORCE;

            postPartialReworkTransactionID = CimBooleanUtils.isTrue(params.getPartialReworkReqInformation().getBDynamicRoute())
                    ? TransactionIDEnum.POST_PARTIAL_REWORK_FORCE_AND_DYNAMIC_ROUTE : TransactionIDEnum.POST_PARTIAL_REWORK_FORCE;
        } else {
            partialReworkTransactionID = CimBooleanUtils.isTrue(params.getPartialReworkReqInformation().getBDynamicRoute())
                    ? TransactionIDEnum.PARTIAL_REWORK_DYNAMIC_ROUTE : TransactionIDEnum.PARTIAL_REWORK;

            postPartialReworkTransactionID = CimBooleanUtils.isTrue(params.getPartialReworkReqInformation().getBDynamicRoute())
                    ? TransactionIDEnum.POST_PARTIAL_REWORK_DYNAMIC_ROUTE : TransactionIDEnum.POST_PARTIAL_REWORK;
        }
        String transactionID = partialReworkTransactionID.getValue();
        String postProcTxID = postPartialReworkTransactionID.getValue();

        //【step2】call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        List<ObjectIdentifier> lotIDLists = new ArrayList<>();
        lotIDLists.add(params.getPartialReworkReqInformation().getParentLotID());
        accessControlCheckInqParams.setLotIDLists(lotIDLists);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), accessControlCheckInqParams);

        //【step3】main process
        Results.PartialReworkReqResult reworkResult = lotService.sxPartialReworkReq(objCommon, params);
        //TxPostTaskRegisterReq__100
        //TxPostTaskExecuteReq__100
        Params.PostTaskRegisterReqParams postTaskRegisterReqParams = new Params.PostTaskRegisterReqParams(transactionID, null, null, -1,
                new Infos.PostProcessRegistrationParam(null, null, lotIDLists, null), params.getClaimMemo());
        postController.execPostProcess(objCommon, postTaskRegisterReqParams);

        List<ObjectIdentifier> childLotIDs = new ArrayList<>();
        childLotIDs.add(reworkResult.getCreatedLotID());
        Params.PostTaskRegisterReqParams postTaskRegisterReqParamsForChild = new Params.PostTaskRegisterReqParams(postProcTxID, null, null, -1,
                new Infos.PostProcessRegistrationParam(null, null, childLotIDs, null), params.getClaimMemo());
        postController.execPostProcess(objCommon, postTaskRegisterReqParamsForChild);

        return Response.createSucc(transactionID, reworkResult.getCreatedLotID());
    }

    @Override
    @ResponseBody
    @PostMapping(value = "/partial_rework/req")
    @CimMapping({TransactionIDEnum.PARTIAL_REWORK_FORCE_AND_DYNAMIC_ROUTE,
            TransactionIDEnum.PARTIAL_REWORK_FORCE,
            TransactionIDEnum.POST_PARTIAL_REWORK_FORCE_AND_DYNAMIC_ROUTE,
            TransactionIDEnum.POST_PARTIAL_REWORK_FORCE,
            TransactionIDEnum.PARTIAL_REWORK_DYNAMIC_ROUTE,
            TransactionIDEnum.PARTIAL_REWORK,
            TransactionIDEnum.POST_PARTIAL_REWORK_DYNAMIC_ROUTE,
            TransactionIDEnum.POST_PARTIAL_REWORK})
    @EnablePostProcess(proxy = PartialReworkPostProcessProxy.class)
    public Response partialReworkReq(@RequestBody Params.PartialReworkReqParams params) {
        //【step1】init params
        TransactionIDEnum partialReworkTransactionID;
        boolean dynamicRouteFlag = params.getPartialReworkReqInformation().getBDynamicRoute();
        if (CimBooleanUtils.isTrue(params.getPartialReworkReqInformation().getBForceRework())) {
            partialReworkTransactionID = CimBooleanUtils.isTrue(dynamicRouteFlag) ?
                    TransactionIDEnum.PARTIAL_REWORK_FORCE_AND_DYNAMIC_ROUTE :
                    TransactionIDEnum.PARTIAL_REWORK_FORCE;
        } else {
            partialReworkTransactionID = CimBooleanUtils.isTrue(dynamicRouteFlag) ?
                    TransactionIDEnum.PARTIAL_REWORK_DYNAMIC_ROUTE :
                    TransactionIDEnum.PARTIAL_REWORK;
        }
        String transactionID = partialReworkTransactionID.getValue();

        //------------------------------------------
        //【step2】call txAccessControlCheckInq(...)
        //------------------------------------------
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        List<ObjectIdentifier> lotIDLists = new ArrayList<>();
        lotIDLists.add(params.getPartialReworkReqInformation().getParentLotID());
        accessControlCheckInqParams.setLotIDLists(lotIDLists);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID,
                params.getUser(),
                accessControlCheckInqParams);

        //----------------------------
        //【step3】main process
        //----------------------------
        Results.PartialReworkReqResult reworkResult = lotService.sxPartialReworkReq(objCommon, params);

        return Response.createSucc(transactionID, reworkResult);
        //----------------------------
        // PostProcess
        //----------------------------
    }

    @ResponseBody
    @RequestMapping(value = "/old/partial_rework_with_hold_release/req", method = RequestMethod.POST)
    /*@CimMapping({TransactionIDEnum.PARTIAL_REWORK_WITH_HOLD_RELEASE_REQ_ONE, TransactionIDEnum.PARTIAL_REWORK_WITH_HOLD_RELEASE_REQ_TWO,
            TransactionIDEnum.PARTIAL_REWORK_WITH_HOLD_RELEASE_REQ_POST_PROC_ONE, TransactionIDEnum.PARTIAL_REWORK_WITH_HOLD_RELEASE_REQ_POST_PROC_TWO,
            TransactionIDEnum.PARTIAL_REWORK_WITH_HOLD_RELEASE_REQ_THREE, TransactionIDEnum.PARTIAL_REWORK_WITH_HOLD_RELEASE_REQ_FOUR,
            TransactionIDEnum.PARTIAL_REWORK_WITH_HOLD_RELEASE_REQ_POST_PROC_THREE, TransactionIDEnum.PARTIAL_REWORK_WITH_HOLD_RELEASE_REQ_POST_PROC_FOUR})*/
    public Response partialReworkWithHoldReleaseReqOld(@RequestBody Params.PartialReworkWithHoldReleaseReqParams partialReworkWithHoldReleaseReqParams) {

        TransactionIDEnum PARTIAL_REWORK_WITH_HOLD_RELEASE_REQ = null;
        TransactionIDEnum PARTIAL_REWORK_WITH_HOLD_RELEASE_REQ_POST_PROC = null;
        if (CimBooleanUtils.isTrue(partialReworkWithHoldReleaseReqParams.getPartialReworkReq().getBForceRework())) {
            PARTIAL_REWORK_WITH_HOLD_RELEASE_REQ = CimBooleanUtils.isTrue(partialReworkWithHoldReleaseReqParams.getPartialReworkReq().getBDynamicRoute())
                    ? TransactionIDEnum.PARTIAL_REWORK_WITH_HOLD_RELEASE_REQ_ONE : TransactionIDEnum.PARTIAL_REWORK_WITH_HOLD_RELEASE_REQ_TWO;

            PARTIAL_REWORK_WITH_HOLD_RELEASE_REQ_POST_PROC = CimBooleanUtils.isTrue(partialReworkWithHoldReleaseReqParams.getPartialReworkReq().getBDynamicRoute())
                    ? TransactionIDEnum.PARTIAL_REWORK_WITH_HOLD_RELEASE_REQ_POST_PROC_ONE : TransactionIDEnum.PARTIAL_REWORK_WITH_HOLD_RELEASE_REQ_POST_PROC_TWO;
        } else {
            PARTIAL_REWORK_WITH_HOLD_RELEASE_REQ = CimBooleanUtils.isTrue(partialReworkWithHoldReleaseReqParams.getPartialReworkReq().getBDynamicRoute())
                    ? TransactionIDEnum.PARTIAL_REWORK_WITH_HOLD_RELEASE_REQ_THREE : TransactionIDEnum.PARTIAL_REWORK_WITH_HOLD_RELEASE_REQ_FOUR;

            PARTIAL_REWORK_WITH_HOLD_RELEASE_REQ_POST_PROC = CimBooleanUtils.isTrue(partialReworkWithHoldReleaseReqParams.getPartialReworkReq().getBDynamicRoute())
                    ? TransactionIDEnum.PARTIAL_REWORK_WITH_HOLD_RELEASE_REQ_POST_PROC_THREE : TransactionIDEnum.PARTIAL_REWORK_WITH_HOLD_RELEASE_REQ_POST_PROC_FOUR;
        }

        String txId = PARTIAL_REWORK_WITH_HOLD_RELEASE_REQ.getValue();
        String postProcTxID = PARTIAL_REWORK_WITH_HOLD_RELEASE_REQ_POST_PROC.getValue();
        //set current tx id
        ThreadContextHolder.setTransactionId(txId);
        //step2 -  call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        List<ObjectIdentifier> lotIDLists = new ArrayList<>();
        lotIDLists.add(partialReworkWithHoldReleaseReqParams.getPartialReworkReq().getParentLotID());
        accessControlCheckInqParams.setLotIDLists(lotIDLists);
        accessControlCheckInqParams.conversionDepartmentCheckCode(partialReworkWithHoldReleaseReqParams.getHoldReqList());
        accessControlCheckInqParams.conversionDepartmentCheckCode(
                partialReworkWithHoldReleaseReqParams.getDepartment(), partialReworkWithHoldReleaseReqParams.getSection(), partialReworkWithHoldReleaseReqParams.getReleaseReasonCodeID());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, partialReworkWithHoldReleaseReqParams.getUser(), accessControlCheckInqParams);
        /*-----------------------------------------------------------------------*/
        /*   Main Process                                                        */
        /*-----------------------------------------------------------------------*/
        Results.PartialReworkReqResult reworkResult = lotService.sxPartialReworkWithHoldReleaseReq(objCommon, partialReworkWithHoldReleaseReqParams);
        //TxPostTaskRegisterReq__100
        //TxPostTaskExecuteReq__100
        Params.PostTaskRegisterReqParams postTaskRegisterReqParams = new Params.PostTaskRegisterReqParams(txId, null, null, -1,
                new Infos.PostProcessRegistrationParam(null, null, lotIDLists, null), partialReworkWithHoldReleaseReqParams.getClaimMemo());
        postController.execPostProcess(objCommon, postTaskRegisterReqParams);

        List<ObjectIdentifier> childLotIDs = new ArrayList<>();
        childLotIDs.add(reworkResult.getCreatedLotID());
        Params.PostTaskRegisterReqParams postTaskRegisterReqParamsForChild = new Params.PostTaskRegisterReqParams(postProcTxID, null, null, -1,
                new Infos.PostProcessRegistrationParam(null, null, childLotIDs, null), partialReworkWithHoldReleaseReqParams.getClaimMemo());
        postController.execPostProcess(objCommon, postTaskRegisterReqParamsForChild);

        return Response.createSucc(txId, reworkResult.getCreatedLotID());
    }

    @Override
    @ResponseBody
    @PostMapping(value = "/partial_rework_with_hold_release/req")
    @CimMapping({TransactionIDEnum.PARTIAL_REWORK_WITH_HOLD_RELEASE_REQ_ONE,
            TransactionIDEnum.PARTIAL_REWORK_WITH_HOLD_RELEASE_REQ_TWO,
            TransactionIDEnum.PARTIAL_REWORK_WITH_HOLD_RELEASE_REQ_POST_PROC_ONE,
            TransactionIDEnum.PARTIAL_REWORK_WITH_HOLD_RELEASE_REQ_POST_PROC_TWO,
            TransactionIDEnum.PARTIAL_REWORK_WITH_HOLD_RELEASE_REQ_THREE,
            TransactionIDEnum.PARTIAL_REWORK_WITH_HOLD_RELEASE_REQ_FOUR,
            TransactionIDEnum.PARTIAL_REWORK_WITH_HOLD_RELEASE_REQ_POST_PROC_THREE,
            TransactionIDEnum.PARTIAL_REWORK_WITH_HOLD_RELEASE_REQ_POST_PROC_FOUR})
    @EnablePostProcess(proxy = PartialReworkWithHoldReleasePostProcessProxy.class)
    public Response partialReworkWithHoldReleaseReq(@RequestBody Params.PartialReworkWithHoldReleaseReqParams params) {

        boolean dynamicRouteFlag = params.getPartialReworkReq().getBDynamicRoute();
        boolean forceReworkFlag = params.getPartialReworkReq().getBForceRework();
        TransactionIDEnum partialReworkWithHoldReleaseReq;
        if (CimBooleanUtils.isTrue(forceReworkFlag)) {
            partialReworkWithHoldReleaseReq = CimBooleanUtils.isTrue(dynamicRouteFlag) ?
                    TransactionIDEnum.PARTIAL_REWORK_WITH_HOLD_RELEASE_REQ_ONE :
                    TransactionIDEnum.PARTIAL_REWORK_WITH_HOLD_RELEASE_REQ_TWO;

        } else {
            partialReworkWithHoldReleaseReq = CimBooleanUtils.isTrue(dynamicRouteFlag) ?
                    TransactionIDEnum.PARTIAL_REWORK_WITH_HOLD_RELEASE_REQ_THREE :
                    TransactionIDEnum.PARTIAL_REWORK_WITH_HOLD_RELEASE_REQ_FOUR;
        }

        String txId = partialReworkWithHoldReleaseReq.getValue();
        //set current tx id
        ThreadContextHolder.setTransactionId(txId);

        //step2 -  call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        List<ObjectIdentifier> lotIDLists = new ArrayList<>();
        lotIDLists.add(params.getPartialReworkReq().getParentLotID());
        accessControlCheckInqParams.setLotIDLists(lotIDLists);
        accessControlCheckInqParams.conversionDepartmentCheckCode(params.getHoldReqList());
        accessControlCheckInqParams.conversionDepartmentCheckCode(
                params.getDepartment(),
                params.getSection(),
                params.getReleaseReasonCodeID());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId,
                params.getUser(),
                accessControlCheckInqParams);
        /*-----------------------------------------------------------------------*/
        /*   Main Process                                                        */
        /*-----------------------------------------------------------------------*/
        Results.PartialReworkReqResult reworkResult = lotService.sxPartialReworkWithHoldReleaseReq(objCommon,
                params);

        return Response.createSucc(txId, reworkResult);
        //------------------------------
        // Post Process
        //------------------------------
    }

    @ResponseBody
    @RequestMapping(value = "/old/partial_rework_without_hold_release/req", method = RequestMethod.POST)
    /*@CimMapping({TransactionIDEnum.PARTIAL_REWORK_WITHOUT_HOLD_RELEASE_REQ_SPECIAL_ENGINEERING, TransactionIDEnum.PARTIAL_REWORK_WITHOUT_HOLD_RELEASE_REQ_SPECIAL,
            TransactionIDEnum.PARTIAL_REWORK_WITHOUT_HOLD_RELEASE_REQ_SPECIAL_ENGINEERING_POST_PROC, TransactionIDEnum.PARTIAL_REWORK_WITHOUT_HOLD_RELEASE_REQ_SPECIAL_POST_PROC,
            TransactionIDEnum.PARTIAL_REWORK_WITHOUT_HOLD_RELEASE_REQ_ENGINEERING, TransactionIDEnum.PARTIAL_REWORK_WITHOUT_HOLD_RELEASE_REQ,
            TransactionIDEnum.PARTIAL_REWORK_WITHOUT_HOLD_RELEASE_REQ_ENGINEERING_POST_PROC, TransactionIDEnum.PARTIAL_REWORK_WITHOUT_HOLD_RELEASE_REQ_POST_PROC})*/
    public Response partialReworkWithoutHoldReleaseReqOld(@RequestBody Params.PartialReworkWithoutHoldReleaseReqParams params) {
        //【step0】check input params
        User user = params.getUser();
        Validations.check(null == user, "the user info is null");
        Infos.PartialReworkReq partialReworkReq = params.getPartialReworkReq();
        List<Infos.LotHoldReq> lotHoldReqs = params.getHoldReqList();
        Validations.check(null == partialReworkReq, "the parameter is null");

        TransactionIDEnum transactionID;
        TransactionIDEnum postProcTxID;
        if (CimBooleanUtils.isTrue(partialReworkReq.getBForceRework())) {
            transactionID = CimBooleanUtils.isTrue(partialReworkReq.getBDynamicRoute())
                    ? TransactionIDEnum.PARTIAL_REWORK_WITHOUT_HOLD_RELEASE_REQ_SPECIAL_ENGINEERING : TransactionIDEnum.PARTIAL_REWORK_WITHOUT_HOLD_RELEASE_REQ_SPECIAL;

            postProcTxID = CimBooleanUtils.isTrue(partialReworkReq.getBDynamicRoute())
                    ? TransactionIDEnum.PARTIAL_REWORK_WITHOUT_HOLD_RELEASE_REQ_SPECIAL_ENGINEERING_POST_PROC : TransactionIDEnum.PARTIAL_REWORK_WITHOUT_HOLD_RELEASE_REQ_SPECIAL_POST_PROC;
        } else {
            transactionID = CimBooleanUtils.isTrue(partialReworkReq.getBDynamicRoute())
                    ? TransactionIDEnum.PARTIAL_REWORK_WITHOUT_HOLD_RELEASE_REQ_ENGINEERING : TransactionIDEnum.PARTIAL_REWORK_WITHOUT_HOLD_RELEASE_REQ;

            postProcTxID = CimBooleanUtils.isTrue(partialReworkReq.getBDynamicRoute())
                    ? TransactionIDEnum.PARTIAL_REWORK_WITHOUT_HOLD_RELEASE_REQ_ENGINEERING_POST_PROC : TransactionIDEnum.PARTIAL_REWORK_WITHOUT_HOLD_RELEASE_REQ_POST_PROC;
        }
        ThreadContextHolder.setTransactionId(transactionID.getValue());

        //【step2】call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        List<ObjectIdentifier> lotIDLists = new ArrayList<>();
        lotIDLists.add(params.getPartialReworkReq().getParentLotID());
        accessControlCheckInqParams.setLotIDLists(lotIDLists);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID.getValue(), params.getUser(), accessControlCheckInqParams);

        //【step3】call sxPartialReworkWithoutHoldReleaseReq(...)
        Results.PartialReworkReqResult reworkResult = lotService.sxPartialReworkWithoutHoldReleaseReq(objCommon, partialReworkReq, lotHoldReqs, params.getClaimMemo());
        //step4】txPostTaskRegisterReq__100
        //【step5】TxPostTaskExecuteReq__100
        List<ObjectIdentifier> childLotIDs = new ArrayList<>();
        childLotIDs.add(reworkResult.getCreatedLotID());
        Params.PostTaskRegisterReqParams postTaskRegisterReqParamsForChild = new Params.PostTaskRegisterReqParams(postProcTxID.getValue(), null, null, -1,
                new Infos.PostProcessRegistrationParam(null, null, childLotIDs, null), params.getClaimMemo());
        postController.execPostProcess(objCommon, postTaskRegisterReqParamsForChild);
        return Response.createSucc(transactionID.getValue(), reworkResult.getCreatedLotID());
    }


    @Override
    @ResponseBody
    @RequestMapping(value = "/partial_rework_without_hold_release/req", method = RequestMethod.POST)
    @CimMapping({TransactionIDEnum.PARTIAL_REWORK_WITHOUT_HOLD_RELEASE_REQ_SPECIAL_ENGINEERING,
            TransactionIDEnum.PARTIAL_REWORK_WITHOUT_HOLD_RELEASE_REQ_SPECIAL,
            TransactionIDEnum.PARTIAL_REWORK_WITHOUT_HOLD_RELEASE_REQ_SPECIAL_ENGINEERING_POST_PROC,
            TransactionIDEnum.PARTIAL_REWORK_WITHOUT_HOLD_RELEASE_REQ_SPECIAL_POST_PROC,
            TransactionIDEnum.PARTIAL_REWORK_WITHOUT_HOLD_RELEASE_REQ_ENGINEERING,
            TransactionIDEnum.PARTIAL_REWORK_WITHOUT_HOLD_RELEASE_REQ,
            TransactionIDEnum.PARTIAL_REWORK_WITHOUT_HOLD_RELEASE_REQ_ENGINEERING_POST_PROC,
            TransactionIDEnum.PARTIAL_REWORK_WITHOUT_HOLD_RELEASE_REQ_POST_PROC})
    @EnablePostProcess(proxy = PartialReworkWithoutHoldReleasePostProcessProxy.class)
    public Response partialReworkWithoutHoldReleaseReq(@RequestBody Params.PartialReworkWithoutHoldReleaseReqParams params) {
        //【step0】check input params
        User user = params.getUser();
        Infos.PartialReworkReq partialReworkReq = params.getPartialReworkReq();
        List<Infos.LotHoldReq> lotHoldReqs = params.getHoldReqList();
        Validations.check(null == partialReworkReq, retCodeConfig.getInvalidParameter());

        TransactionIDEnum transactionID;
        boolean dynamicRouteFlag = partialReworkReq.getBDynamicRoute();
        if (CimBooleanUtils.isTrue(partialReworkReq.getBForceRework())) {
            transactionID = dynamicRouteFlag ?
                    TransactionIDEnum.PARTIAL_REWORK_WITHOUT_HOLD_RELEASE_REQ_SPECIAL_ENGINEERING :
                    TransactionIDEnum.PARTIAL_REWORK_WITHOUT_HOLD_RELEASE_REQ_SPECIAL;
        } else {
            transactionID = dynamicRouteFlag ?
                    TransactionIDEnum.PARTIAL_REWORK_WITHOUT_HOLD_RELEASE_REQ_ENGINEERING :
                    TransactionIDEnum.PARTIAL_REWORK_WITHOUT_HOLD_RELEASE_REQ;
        }
        ThreadContextHolder.setTransactionId(transactionID.getValue());

        //【step2】call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        List<ObjectIdentifier> lotIDLists = new ArrayList<>();
        lotIDLists.add(params.getPartialReworkReq().getParentLotID());
        accessControlCheckInqParams.setLotIDLists(lotIDLists);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID.getValue(),
                params.getUser(),
                accessControlCheckInqParams);

        //【step3】call sxPartialReworkWithoutHoldReleaseReq(...)
        Results.PartialReworkReqResult reworkResult = lotService.sxPartialReworkWithoutHoldReleaseReq(objCommon,
                partialReworkReq,
                lotHoldReqs,
                params.getClaimMemo());

        return Response.createSucc(transactionID.getValue(), reworkResult);
    }

    @ResponseBody
    @RequestMapping(value = "/old/partial_rework_cancel/req", method = RequestMethod.POST)
    //@CimMapping(TransactionIDEnum.REWORK_PARTIAL_WAFER_LOT_CANCEL_REQ)
    public Response partialReworkCancelReqOld(@RequestBody Params.PartialReworkCancelReqParams params) {
        //【step0】init params;
        String transactionID = TransactionIDEnum.REWORK_PARTIAL_WAFER_LOT_CANCEL_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //【step2】call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        List<ObjectIdentifier> lotIDLists = new ArrayList<>();
        lotIDLists.add(params.getParentLotID());
        accessControlCheckInqParams.setLotIDLists(lotIDLists);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), accessControlCheckInqParams);

        //【step3】main process
        lotService.sxPartialReworkCancelReq(objCommon, params);
        //【step4】txPostTaskRegisterReq__100
        //【step5】TxPostTaskExecuteReq__100
        Params.PostTaskRegisterReqParams postTaskRegisterReqParams = new Params.PostTaskRegisterReqParams(transactionID, null, null, -1,
                new Infos.PostProcessRegistrationParam(null, null, lotIDLists, null), "");
        postController.execPostProcess(objCommon, postTaskRegisterReqParams);

        return Response.createSucc(transactionID, null);
    }

    @Override
    @ResponseBody
    @RequestMapping(value = "/partial_rework_cancel/req", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.REWORK_PARTIAL_WAFER_LOT_CANCEL_REQ)
    @EnablePostProcess
    public Response partialReworkCancelReq(@RequestBody Params.PartialReworkCancelReqParams params) {
        Validations.check(null == params, retCodeConfig.getInvalidParameter());
        //【step0】init params;
        String transactionID = TransactionIDEnum.REWORK_PARTIAL_WAFER_LOT_CANCEL_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //【step2】call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        List<ObjectIdentifier> lotIDLists = new ArrayList<>();
        lotIDLists.add(params.getParentLotID());
        accessControlCheckInqParams.setLotIDLists(lotIDLists);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID,
                params.getUser(),
                accessControlCheckInqParams);

        //【step3】main process
        lotService.sxPartialReworkCancelReq(objCommon, params);

        return Response.createSucc(transactionID);
        //--------------------------
        // Post Process
        //--------------------------
    }

    @ResponseBody
    @RequestMapping(value = "/old/rework/req", method = RequestMethod.POST)
    /*@CimMapping({TransactionIDEnum.FORCE_DYNAMIC_REWORK_REQ, TransactionIDEnum.FORCE_REWORK_REQ,
            TransactionIDEnum.DYNAMIC_REWORK_REQ, TransactionIDEnum.REWORK_REQ})*/
    public Response reworkReqOld(@RequestBody Params.ReworkReqParams params) {
        //【step0】init params
        TransactionIDEnum REWORK_REQ = null;
        if (CimBooleanUtils.isTrue(params.getReworkReq().getForceReworkFlag())) {
            REWORK_REQ = CimBooleanUtils.isTrue(params.getReworkReq().getDynamicRouteFlag())
                    ? TransactionIDEnum.FORCE_DYNAMIC_REWORK_REQ : TransactionIDEnum.FORCE_REWORK_REQ;
        } else {
            REWORK_REQ = CimBooleanUtils.isTrue(params.getReworkReq().getDynamicRouteFlag())
                    ? TransactionIDEnum.DYNAMIC_REWORK_REQ : TransactionIDEnum.REWORK_REQ;
        }

        final String transactionID = REWORK_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //【step2】call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        List<ObjectIdentifier> lotIDLists = new ArrayList<>();
        lotIDLists.add(params.getReworkReq().getLotID());
        accessControlCheckInqParams.setLotIDLists(lotIDLists);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), accessControlCheckInqParams);

        //【step3】main process
        lotService.sxReworkReq(objCommon, params.getReworkReq(), params.getClaimMemo());

        Params.PostTaskRegisterReqParams postTaskRegisterReqParams = new Params.PostTaskRegisterReqParams(transactionID, null, null, -1,
                new Infos.PostProcessRegistrationParam(null, null, lotIDLists, null), params.getClaimMemo());
        postController.execPostProcess(objCommon, postTaskRegisterReqParams);

        return Response.createSucc(transactionID, null);
    }

    @Override
    @ResponseBody
    @RequestMapping(value = "/rework/req", method = RequestMethod.POST)
    @CimMapping({TransactionIDEnum.FORCE_DYNAMIC_REWORK_REQ,
            TransactionIDEnum.FORCE_REWORK_REQ,
            TransactionIDEnum.DYNAMIC_REWORK_REQ,
            TransactionIDEnum.REWORK_REQ})
    @EnablePostProcess
    public Response reworkReq(@RequestBody Params.ReworkReqParams params) {
        Validations.check(null == params, retCodeConfig.getInvalidParameter());
        //【step0】init params
        TransactionIDEnum reworkReq;
        if (CimBooleanUtils.isTrue(params.getReworkReq().getForceReworkFlag())) {
            reworkReq = CimBooleanUtils.isTrue(params.getReworkReq().getDynamicRouteFlag())
                    ? TransactionIDEnum.FORCE_DYNAMIC_REWORK_REQ : TransactionIDEnum.FORCE_REWORK_REQ;
        } else {
            reworkReq = CimBooleanUtils.isTrue(params.getReworkReq().getDynamicRouteFlag())
                    ? TransactionIDEnum.DYNAMIC_REWORK_REQ : TransactionIDEnum.REWORK_REQ;
        }

        final String transactionID = reworkReq.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //【step2】call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        List<ObjectIdentifier> lotIDLists = new ArrayList<>();
        lotIDLists.add(params.getReworkReq().getLotID());
        accessControlCheckInqParams.setLotIDLists(lotIDLists);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID,
                params.getUser(),
                accessControlCheckInqParams);

        //【step3】main process
        lotService.sxReworkReq(objCommon, params.getReworkReq(), params.getClaimMemo());
        return Response.createSucc(transactionID, null);
    }

    @ResponseBody
    @RequestMapping(value = "/old/rework_cancel/req", method = RequestMethod.POST)
    //@CimMapping(TransactionIDEnum.REWORK_WHOLE_LOT_CANCEL_REQ)
    public Response reworkCancelReqOld(@RequestBody Params.ReworkCancelReqParams params) {
        String transactionID = TransactionIDEnum.REWORK_WHOLE_LOT_CANCEL_REQ.getValue();

        //【step0】init params
        ThreadContextHolder.setTransactionId(TransactionIDEnum.REWORK_WHOLE_LOT_CANCEL_REQ);

        //【step2】call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        List<ObjectIdentifier> lotIDLists = new ArrayList<>();
        lotIDLists.add(params.getLotID());
        accessControlCheckInqParams.setLotIDLists(lotIDLists);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), accessControlCheckInqParams);

        //【step3】call sxReworkWholeLotCancel
        lotService.sxReworkCancelReq(objCommon, params);
        //【step4】txPostTaskRegisterReq__100
        //step5】TxPostTaskExecuteReq__100
        Params.PostTaskRegisterReqParams postTaskRegisterReqParams = new Params.PostTaskRegisterReqParams(transactionID, null, null, -1,
                new Infos.PostProcessRegistrationParam(null, null, lotIDLists, null), "");
        postController.execPostProcess(objCommon, postTaskRegisterReqParams);

        return Response.createSucc(transactionID, null);
    }

    @Override
    @ResponseBody
    @RequestMapping(value = "/rework_cancel/req", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.REWORK_WHOLE_LOT_CANCEL_REQ)
    @EnablePostProcess
    public Response reworkCancelReq(@RequestBody Params.ReworkCancelReqParams params) {
        Validations.check(null == params, retCodeConfig.getInvalidParameter());

        //【step0】init params
        String transactionID = TransactionIDEnum.REWORK_WHOLE_LOT_CANCEL_REQ.getValue();
        ThreadContextHolder.setTransactionId(TransactionIDEnum.REWORK_WHOLE_LOT_CANCEL_REQ);

        //【step2】call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        List<ObjectIdentifier> lotIDLists = new ArrayList<>();
        lotIDLists.add(params.getLotID());
        accessControlCheckInqParams.setLotIDLists(lotIDLists);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID,
                params.getUser(),
                accessControlCheckInqParams);

        //【step3】call sxReworkWholeLotCancel
        lotService.sxReworkCancelReq(objCommon, params);
        return Response.createSucc(transactionID, null);

        //--------------------
        // Post Process
        //--------------------
    }

    @ResponseBody
    @RequestMapping(value = "/old/rework_with_hold_release/req", method = RequestMethod.POST)
    /*@CimMapping({TransactionIDEnum.REWORK_WITH_HOLD_RELEASE_REQ_ONE, TransactionIDEnum.REWORK_WITH_HOLD_RELEASE_REQ_TWO,
            TransactionIDEnum.REWORK_WITH_HOLD_RELEASE_REQ_THREE, TransactionIDEnum.REWORK_WITH_HOLD_RELEASE_REQ_FOUR})*/
    public Response reworkWithHoldReleaseReqOld(@RequestBody Params.ReworkWithHoldReleaseReqParams reworkWithHoldReleaseReqParams) {

        TransactionIDEnum REWORK_WITH_HOLD_RELEASE_REQ = null;
        if (CimBooleanUtils.isTrue(reworkWithHoldReleaseReqParams.getStrReworkReq().getForceReworkFlag())) {
            REWORK_WITH_HOLD_RELEASE_REQ = CimBooleanUtils.isTrue(reworkWithHoldReleaseReqParams.getStrReworkReq().getDynamicRouteFlag())
                    ? TransactionIDEnum.REWORK_WITH_HOLD_RELEASE_REQ_ONE : TransactionIDEnum.REWORK_WITH_HOLD_RELEASE_REQ_TWO;
        } else {
            REWORK_WITH_HOLD_RELEASE_REQ = CimBooleanUtils.isTrue(reworkWithHoldReleaseReqParams.getStrReworkReq().getDynamicRouteFlag())
                    ? TransactionIDEnum.REWORK_WITH_HOLD_RELEASE_REQ_THREE : TransactionIDEnum.REWORK_WITH_HOLD_RELEASE_REQ_FOUR;
        }

        String txId = REWORK_WITH_HOLD_RELEASE_REQ.getValue();
        //set current tx id
        ThreadContextHolder.setTransactionId(txId);
        //step2 -  call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        List<ObjectIdentifier> lotIDLists = new ArrayList<>();
        lotIDLists.add(reworkWithHoldReleaseReqParams.getStrReworkReq().getLotID());
        accessControlCheckInqParams.setLotIDLists(lotIDLists);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, reworkWithHoldReleaseReqParams.getUser(), accessControlCheckInqParams);

        /*-----------------------------------------------------------------------*/
        /*   Main Process                                                        */
        /*-----------------------------------------------------------------------*/

        lotService.sxReworkWithHoldReleaseReq(objCommon, reworkWithHoldReleaseReqParams);
        //txPostTaskRegisterReq__100
        //TxPostTaskExecuteReq__100
        Params.PostTaskRegisterReqParams postTaskRegisterReqParams = new Params.PostTaskRegisterReqParams(txId, null, null, -1,
                new Infos.PostProcessRegistrationParam(null, null, lotIDLists, null), reworkWithHoldReleaseReqParams.getClaimMemo());
        postController.execPostProcess(objCommon, postTaskRegisterReqParams);

        return Response.createSucc(txId, null);
    }

    @Override
    @ResponseBody
    @RequestMapping(value = "/rework_with_hold_release/req", method = RequestMethod.POST)
    @CimMapping({TransactionIDEnum.REWORK_WITH_HOLD_RELEASE_REQ_ONE,
            TransactionIDEnum.REWORK_WITH_HOLD_RELEASE_REQ_TWO,
            TransactionIDEnum.REWORK_WITH_HOLD_RELEASE_REQ_THREE,
            TransactionIDEnum.REWORK_WITH_HOLD_RELEASE_REQ_FOUR})
    @EnablePostProcess
    public Response reworkWithHoldReleaseReq(@RequestBody Params.ReworkWithHoldReleaseReqParams reworkWithHoldReleaseReqParams) {
        Validations.check(null == reworkWithHoldReleaseReqParams, retCodeConfig.getInvalidParameter());

        //step1 - init transactionID
        TransactionIDEnum transactionIDEnum;
        if (CimBooleanUtils.isTrue(reworkWithHoldReleaseReqParams.getStrReworkReq().getForceReworkFlag())) {
            transactionIDEnum = CimBooleanUtils.isTrue(reworkWithHoldReleaseReqParams.getStrReworkReq().getDynamicRouteFlag())
                    ? TransactionIDEnum.REWORK_WITH_HOLD_RELEASE_REQ_ONE : TransactionIDEnum.REWORK_WITH_HOLD_RELEASE_REQ_TWO;
        } else {
            transactionIDEnum = CimBooleanUtils.isTrue(reworkWithHoldReleaseReqParams.getStrReworkReq().getDynamicRouteFlag())
                    ? TransactionIDEnum.REWORK_WITH_HOLD_RELEASE_REQ_THREE : TransactionIDEnum.REWORK_WITH_HOLD_RELEASE_REQ_FOUR;
        }
        String txId = transactionIDEnum.getValue();
        ThreadContextHolder.setTransactionId(txId);

        //step2 -  call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        List<ObjectIdentifier> lotIDLists = new ArrayList<>();
        lotIDLists.add(reworkWithHoldReleaseReqParams.getStrReworkReq().getLotID());
        accessControlCheckInqParams.setLotIDLists(lotIDLists);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId,
                reworkWithHoldReleaseReqParams.getUser(),
                accessControlCheckInqParams);

        /*-----------------------------------------------------------------------*/
        /*   Main Process                                                        */
        /*-----------------------------------------------------------------------*/
        lotService.sxReworkWithHoldReleaseReq(objCommon, reworkWithHoldReleaseReqParams);

        return Response.createSucc(txId);
        //----------------------------
        // Post Process
        //----------------------------
    }

    @ResponseBody
    @RequestMapping(value = "/split_lot_not_on_pf/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.SPLIT_WAFER_LOT_NOT_ON_ROUTE_REQ)
    public Response splitLotNotOnPfReq(@RequestBody Params.SplitLotNotOnPfReqParams splitLotNotOnPfReqParams) {
        String transactionID = TransactionIDEnum.SPLIT_WAFER_LOT_NOT_ON_ROUTE_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);
        //step2 -  call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        List<ObjectIdentifier> lotIDLists = new ArrayList<>();
        lotIDLists.add(splitLotNotOnPfReqParams.getParentLotID());
        accessControlCheckInqParams.setLotIDLists(lotIDLists);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, splitLotNotOnPfReqParams.getUser(), accessControlCheckInqParams);
        /*-----------------------------------------------------------------------*/
        /*   Main Process                                                        */
        /*-----------------------------------------------------------------------*/
        return Response.createSucc(transactionID, lotService.sxSplitLotNotOnPfReq(objCommon, splitLotNotOnPfReqParams));
    }

    @ResponseBody
    @RequestMapping(value = "/old/split_lot/req", method = RequestMethod.POST)
    //@CimMapping(TransactionIDEnum.SPLIT_WAFER_LOT_REQ)
    public Response splitLotReqOld(@RequestBody Params.SplitLotReqParams splitLotReqParams) {
        String transactionID = TransactionIDEnum.SPLIT_WAFER_LOT_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);
        System.out.println("=================== split wafer lot req params =========================");
        System.out.println(JSONObject.toJSONString(splitLotReqParams));
        //step2 -  call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        List<ObjectIdentifier> lotIDLists = new ArrayList<>();
        lotIDLists.add(splitLotReqParams.getParentLotID());
        accessControlCheckInqParams.setLotIDLists(lotIDLists);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, splitLotReqParams.getUser(), accessControlCheckInqParams);

        /*-----------------------------------------------------------------------*/
        /*   Main Process                                                        */
        /*-----------------------------------------------------------------------*/
        Results.SplitLotReqResult splitResult = lotService.sxSplitLotReq(objCommon, splitLotReqParams);
        log.info("Call postProcess for Parent and Child Lot");
        Params.PostTaskRegisterReqParams postTaskRegisterReqParams = new Params.PostTaskRegisterReqParams(objCommon.getTransactionID(), null, null, -1,
                new Infos.PostProcessRegistrationParam(null, null, lotIDLists, null), "");
        postController.execPostProcess(objCommon, postTaskRegisterReqParams);

        List<ObjectIdentifier> childLotIDs = new ArrayList<>();
        childLotIDs.add(splitResult.getChildLotID());
        Params.PostTaskRegisterReqParams postTaskRegisterReqParamsForChild = new Params.PostTaskRegisterReqParams("TcTRC019", null, null, -1,
                new Infos.PostProcessRegistrationParam(null, null, childLotIDs, null), "");
        postController.execPostProcess(objCommon, postTaskRegisterReqParamsForChild);

        return Response.createSucc(transactionID, splitResult.getChildLotID());
    }

    @Override
    @ResponseBody
    @RequestMapping(value = "/split_lot/req", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.SPLIT_WAFER_LOT_REQ)
    @EnablePostProcess(proxy = SplitLotPostProcessProxy.class)
    public Response splitLotReq(@RequestBody Params.SplitLotReqParams splitLotReqParams) {
        String transactionID = TransactionIDEnum.SPLIT_WAFER_LOT_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);
        //step2 -  call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        List<ObjectIdentifier> lotIDLists = new ArrayList<>();
        lotIDLists.add(splitLotReqParams.getParentLotID());
        accessControlCheckInqParams.setLotIDLists(lotIDLists);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID,
                splitLotReqParams.getUser(),
                accessControlCheckInqParams);

        /*-----------------------------------------------------------------------*/
        /*   Main Process                                                        */
        /*-----------------------------------------------------------------------*/
        Results.SplitLotReqResult splitResult = lotService.sxSplitLotReq(objCommon, splitLotReqParams);

        return Response.createSucc(transactionID, splitResult);
        //--------------------------------
        // PostProcess
        //--------------------------------
    }

    @ResponseBody
    @RequestMapping(value = "/old/split_lot_with_hold_release/req", method = RequestMethod.POST)
    //@CimMapping(TransactionIDEnum.SPLIT_WAFER_LOT_WITH_HOLD_RELEASE_REQ)
    public Response splitLotWithHoldReleaseReqOld(@RequestBody Params.SplitLotWithHoldReleaseReqParams splitLotWithHoldReleaseReqParams) {
        String txId = TransactionIDEnum.SPLIT_WAFER_LOT_WITH_HOLD_RELEASE_REQ.getValue();
        //set current tx id
        ThreadContextHolder.setTransactionId(txId);

        //step2 -  call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        List<ObjectIdentifier> lotIDLists = new ArrayList<>();
        lotIDLists.add(splitLotWithHoldReleaseReqParams.getParentLotID());
        accessControlCheckInqParams.setLotIDLists(lotIDLists);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, splitLotWithHoldReleaseReqParams.getUser(), accessControlCheckInqParams);
        /*-----------------------------------------------------------------------*/
        /*   Main Process                                                        */
        /*-----------------------------------------------------------------------*/
        Results.SplitLotReqResult splitResult = lotService.sxSplitLotWithHoldReleaseReq(objCommon, splitLotWithHoldReleaseReqParams);
        /*-----------------------------------------------------------------------*/
        /*   Main Process                                                        */
        /*-----------------------------------------------------------------------*/
        log.debug("Call postProcess for Parent and Child Lot");
        Params.PostTaskRegisterReqParams postTaskRegisterReqParams = new Params.PostTaskRegisterReqParams(objCommon.getTransactionID(), null, null, -1,
                new Infos.PostProcessRegistrationParam(null, null, lotIDLists, null), "");
        postController.execPostProcess(objCommon, postTaskRegisterReqParams);

        List<ObjectIdentifier> childLotIDs = new ArrayList<>();
        childLotIDs.add(splitResult.getChildLotID());
        Params.PostTaskRegisterReqParams postTaskRegisterReqParamsForChild = new Params.PostTaskRegisterReqParams("TcTRC070", null, null, -1,
                new Infos.PostProcessRegistrationParam(null, null, childLotIDs, null), "");
        postController.execPostProcess(objCommon, postTaskRegisterReqParamsForChild);

        return Response.createSucc(txId, splitResult.getChildLotID());
    }

    @Override
    @ResponseBody
    @RequestMapping(value = "/split_lot_with_hold_release/req", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.SPLIT_WAFER_LOT_WITH_HOLD_RELEASE_REQ)
    @EnablePostProcess(proxy = SplitLotWithHoldReleasePostProcessProxy.class)
    public Response splitLotWithHoldReleaseReq(@RequestBody Params.SplitLotWithHoldReleaseReqParams params) {
        //set current tx id
        String txId = TransactionIDEnum.SPLIT_WAFER_LOT_WITH_HOLD_RELEASE_REQ.getValue();
        ThreadContextHolder.setTransactionId(txId);

        //step2 -  call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        List<ObjectIdentifier> lotIDLists = new ArrayList<>();
        lotIDLists.add(params.getParentLotID());
        accessControlCheckInqParams.setLotIDLists(lotIDLists);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId,
                params.getUser(),
                accessControlCheckInqParams);

        /*-----------------------------------------------------------------------*/
        /*   Main Process                                                        */
        /*-----------------------------------------------------------------------*/
        Results.SplitLotReqResult splitResult = lotService.sxSplitLotWithHoldReleaseReq(objCommon, params);

        return Response.createSucc(txId, splitResult);
        //------------------------------
        // Post Process
        //------------------------------
    }

    @ResponseBody
    @RequestMapping(value = "/old/split_lot_without_hold_release/req", method = RequestMethod.POST)
    //@CimMapping(TransactionIDEnum.SPLIT_WAFER_LOT_WITH_OUT_HOLD_RELEASE_REQ)
    public Response splitLotWithoutHoldReleaseReqOld(@RequestBody Params.SplitLotWithoutHoldReleaseReqParams splitLotWithoutHoldReleaseReqParams) {
        String transactionID = TransactionIDEnum.SPLIT_WAFER_LOT_WITH_OUT_HOLD_RELEASE_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //step2 - call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        List<ObjectIdentifier> lotIDLists = new ArrayList<>();
        lotIDLists.add(splitLotWithoutHoldReleaseReqParams.getParentLotID());
        accessControlCheckInqParams.setLotIDLists(lotIDLists);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, splitLotWithoutHoldReleaseReqParams.getUser(), accessControlCheckInqParams);

        /*-----------------------------------------------------------------------*/
        /*   Main Process                                                        */
        /*-----------------------------------------------------------------------*/
        Results.SplitLotReqResult splitResult = lotService.sxSplitLotWithoutHoldReleaseReq(objCommon, splitLotWithoutHoldReleaseReqParams);
        List<ObjectIdentifier> childLotIDs = new ArrayList<>();
        childLotIDs.add(splitResult.getChildLotID());
        Params.PostTaskRegisterReqParams postTaskRegisterReqParamsForChild = new Params.PostTaskRegisterReqParams("TcTRC072", null, null, -1,
                new Infos.PostProcessRegistrationParam(null, null, childLotIDs, null), "");
        postController.execPostProcess(objCommon, postTaskRegisterReqParamsForChild);

        return Response.createSucc(transactionID, splitResult.getChildLotID());
    }

    @Override
    @ResponseBody
    @RequestMapping(value = "/split_lot_without_hold_release/req", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.SPLIT_WAFER_LOT_WITH_OUT_HOLD_RELEASE_REQ)
    @EnablePostProcess(proxy = SplitLotWithoutHoldReleasePostProcessProxy.class)
    public Response splitLotWithoutHoldReleaseReq(@RequestBody Params.SplitLotWithoutHoldReleaseReqParams params) {
        String transactionID = TransactionIDEnum.SPLIT_WAFER_LOT_WITH_OUT_HOLD_RELEASE_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //step2 - call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        List<ObjectIdentifier> lotIDLists = new ArrayList<>();
        lotIDLists.add(params.getParentLotID());
        accessControlCheckInqParams.setLotIDLists(lotIDLists);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID,
                params.getUser(),
                accessControlCheckInqParams);

        /*-----------------------------------------------------------------------*/
        /*   Main Process                                                        */
        /*-----------------------------------------------------------------------*/
        Results.SplitLotReqResult splitResult = lotService.sxSplitLotWithoutHoldReleaseReq(objCommon, params);
        return Response.createSucc(transactionID, splitResult);
        //----------------------------------
        // PostProcess
        //----------------------------------
    }

    @ResponseBody
    @RequestMapping(value = "/old/branch_cancel/req", method = RequestMethod.POST)
    //@CimMapping(TransactionIDEnum.SUBROUT_BRABCH_CANCEL_REQ)
    public Response branchCancelReqOld(@RequestBody Params.BranchCancelReqParams branchCancelReqParams) {
        String txId = TransactionIDEnum.SUBROUT_BRABCH_CANCEL_REQ.getValue();
        //set current tx id
        ThreadContextHolder.setTransactionId(txId);
        //step2 -  call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        List<ObjectIdentifier> lotIDLists = new ArrayList<>();
        lotIDLists.add(branchCancelReqParams.getLotID());
        accessControlCheckInqParams.setLotIDLists(lotIDLists);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, branchCancelReqParams.getUser(), accessControlCheckInqParams);
        /*-----------------------------------------------------------------------*/
        /*   Main Process                                                        */
        /*-----------------------------------------------------------------------*/
        lotService.sxBranchCancelReq(objCommon, branchCancelReqParams);
        //【Step5】Post-Process task Registration;
        Infos.PostProcessRegistrationParam postProcessRegistrationParm = new Infos.PostProcessRegistrationParam();
        postProcessRegistrationParm.setLotIDs(lotIDLists);
        Params.PostTaskRegisterReqParams processActionRegistReqParams = new Params.PostTaskRegisterReqParams();
        processActionRegistReqParams.setTransactionID(objCommon.getTransactionID());
        processActionRegistReqParams.setSequenceNumber(-1);
        processActionRegistReqParams.setPostProcessRegistrationParm(postProcessRegistrationParm);
        processActionRegistReqParams.setClaimMemo(branchCancelReqParams.getClaimMemo());
        Results.PostTaskRegisterReqResult postTaskRegisterReqResult = postService.sxPostTaskRegisterReq(objCommon, processActionRegistReqParams);
        //【Step6】Post-Process task Execution Section;
        //-----------------------------
        // Call postProcess
        //-----------------------------
        log.info("Call postProcess");
        Params.PostTaskExecuteReqParams postTaskExecuteReqParams = new Params.PostTaskExecuteReqParams();
        postTaskExecuteReqParams.setUser(objCommon.getUser());
        postTaskExecuteReqParams.setKey(postTaskRegisterReqResult == null ? null : postTaskRegisterReqResult.getDKey());
        postTaskExecuteReqParams.setSyncFlag(1);
        postTaskExecuteReqParams.setPreviousSequenceNumber(0);
        postController.postTaskExecuteReq(postTaskExecuteReqParams);
        return Response.createSucc(txId);
    }

    @Override
    @ResponseBody
    @RequestMapping(value = "/branch_cancel/req", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.SUBROUT_BRABCH_CANCEL_REQ)
    @EnablePostProcess
    public Response branchCancelReq(@RequestBody Params.BranchCancelReqParams branchCancelReqParams) {
        String txId = TransactionIDEnum.SUBROUT_BRABCH_CANCEL_REQ.getValue();
        //set current tx id
        ThreadContextHolder.setTransactionId(txId);
        //step2 -  call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        List<ObjectIdentifier> lotIDLists = new ArrayList<>();
        lotIDLists.add(branchCancelReqParams.getLotID());
        accessControlCheckInqParams.setLotIDLists(lotIDLists);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId,
                branchCancelReqParams.getUser(),
                accessControlCheckInqParams);
        /*-----------------------------------------------------------------------*/
        /*   Main Process                                                        */
        /*-----------------------------------------------------------------------*/
        lotService.sxBranchCancelReq(objCommon, branchCancelReqParams);

        return Response.createSucc(txId);
        //-----------------------------
        // PostProcess
        //-----------------------------
    }

    @ResponseBody
    @RequestMapping(value = "/sub_route_branch/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.SUB_ROUTE_BRANCH_REQ)
    public Response subRouteBranchReq(@RequestBody Params.SubRouteBranchReqParams params) {
        TransactionIDEnum txId = TransactionIDEnum.SUB_ROUTE_BRANCH_REQ;
        ThreadContextHolder.setTransactionId(txId);

        //step2 -  call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        List<ObjectIdentifier> lotIDLists = new ArrayList<>();
        lotIDLists.add(params.getLotID());
        accessControlCheckInqParams.setLotIDLists(lotIDLists);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId.getValue(), params.getUser(), accessControlCheckInqParams);

        //Main Process ;
        lotService.sxSubRouteBranchReqService(objCommon, params);

        //【Step5】Post-Process task Registration;
        Infos.PostProcessRegistrationParam postProcessRegistrationParm = new Infos.PostProcessRegistrationParam();
        postProcessRegistrationParm.setLotIDs(lotIDLists);
        Params.PostTaskRegisterReqParams processActionRegistReqParams = new Params.PostTaskRegisterReqParams();
        processActionRegistReqParams.setTransactionID(objCommon.getTransactionID());
        processActionRegistReqParams.setSequenceNumber(-1);
        processActionRegistReqParams.setPostProcessRegistrationParm(postProcessRegistrationParm);
        processActionRegistReqParams.setClaimMemo(params.getClaimMemo());
        Results.PostTaskRegisterReqResult postTaskRegisterReqResult = postService.sxPostTaskRegisterReq(objCommon, processActionRegistReqParams);
        //【Step6】Post-Process task Execution Section;
        //-----------------------------
        // Call postProcess
        //-----------------------------
        log.info("Call postProcess");
        Params.PostTaskExecuteReqParams postTaskExecuteReqParams = new Params.PostTaskExecuteReqParams();
        postTaskExecuteReqParams.setUser(objCommon.getUser());
        postTaskExecuteReqParams.setKey(postTaskRegisterReqResult == null ? null : postTaskRegisterReqResult.getDKey());
        postTaskExecuteReqParams.setSyncFlag(1);
        postTaskExecuteReqParams.setPreviousSequenceNumber(0);
        postController.postTaskExecuteReq(postTaskExecuteReqParams);
        return Response.createSucc(txId.getValue());
    }

    @ResponseBody
    @RequestMapping(value = "/force_skip/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.FORCE_OPE_LOCATE_REQ)
    @EnablePostProcess
    public Response forceSkipReq(@RequestBody Params.SkipReqParams params) {
        /*-----------------------------------------------------------------------*/
        /*   Pre Process                                                         */
        /*-----------------------------------------------------------------------*/
        String txId = TransactionIDEnum.FORCE_OPE_LOCATE_REQ.getValue();
        ThreadContextHolder.setTransactionId(txId);

        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, params.getUser(), new Params.AccessControlCheckInqParams(true));

        //step3 - txForceSkipReq
        lotService.sxForceSkipReq(objCommon, params);

        return Response.createSucc(txId, null);
    }

    @Override
    @ResponseBody
    @RequestMapping(value = "/rework_whole_lot/req", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.REWORK_WHOLE_LOT_REQ)
    public Response reworkWholeLotReq(@RequestBody Params.ReworkWholeLotReqParams params) {
        //【step0】init params;
        TransactionIDEnum transactionID = TransactionIDEnum.REWORK_REQ;
        ThreadContextHolder.setTransactionId(transactionID.getValue());

        //【step2】call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        List<ObjectIdentifier> lotIDLists = new ArrayList<>();
        lotIDLists.add(params.getLotID());
        accessControlCheckInqParams.setLotIDLists(lotIDLists);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID.getValue(), params.getUser(), accessControlCheckInqParams);

        //【step3】main process
        lotService.sxReworkWholeLotReq(objCommon, params);
        //【step4】txPostTaskRegisterReq__100
        //【step5】TxPostTaskExecuteReq__100
        Params.PostTaskRegisterReqParams postTaskRegisterReqParams = new Params.PostTaskRegisterReqParams(transactionID.getValue(), null, null, -1,
                new Infos.PostProcessRegistrationParam(null, null, lotIDLists, null), "");
        postController.execPostProcess(objCommon, postTaskRegisterReqParams);

        return Response.createSucc(transactionID.getValue(), null);
    }

    @ResponseBody
    @RequestMapping(value = "/old/pass_thru/req", method = RequestMethod.POST)
    //@CimMapping(TransactionIDEnum.GATE_PASS_REQ)
    public Response passThruReqOld(@RequestBody Params.PassThruReqParams params) {
        String txId = TransactionIDEnum.GATE_PASS_REQ.getValue();
        ThreadContextHolder.setTransactionId(txId);

        User user = params.getUser();
        long lotLen = params.getGatePassLotInfos().size();
        List<ObjectIdentifier> lotIDs = new ArrayList<>();
        for (int i = 0; i < lotLen; i++) {
            lotIDs.add(params.getGatePassLotInfos().get(i).getLotID());
        }
        //Step2 - txAccessControlCheckInq
        Params.AccessControlCheckInqParams privilegeCheckParams = new Params.AccessControlCheckInqParams(true);
        privilegeCheckParams.setLotIDLists(lotIDs);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, params.getUser(), privilegeCheckParams);

        Validations.check(params.getGatePassLotInfos() == null, retCodeConfig.getError().getCode(), "Gate Pass Lot Info Is Null !");

        Results.PassThruReqResult retVal =  new Results.PassThruReqResult();
        retVal.setHoldReleasedLotIDs(new ArrayList<>());
        retVal.setStrGatePassLotsResult(new ArrayList<>());
        int gatePassLotInfos = CimArrayUtils.getSize(params.getGatePassLotInfos());

        for (int i = 0; i < gatePassLotInfos; i++) {

            //Step3 - txPassThruReq
            final Results.PassThruReqResult passThruReqResult = lotService.sxPassThruReq(objCommon,
                    params.getGatePassLotInfos().get(i),
                    params.getClaimMemo());
            retVal.getStrGatePassLotsResult().addAll(passThruReqResult.getStrGatePassLotsResult());
            retVal.getHoldReleasedLotIDs().addAll(passThruReqResult.getHoldReleasedLotIDs());
            //-------------------------------------
            // Post-Processing Registration Section
            //-------------------------------------
            Results.PostTaskRegisterReqResult postTaskRegisterReqResult, postTaskRegisterReqResult2 = null;
            log.info("Registration of Post-Processng for OpeCompedLot");
            Infos.PostProcessRegistrationParam postProcessRegistrationParam = new Infos.PostProcessRegistrationParam(null, null, Arrays.asList(params.getGatePassLotInfos().get(i).getLotID()), null);
            Results.PostTaskRegisterReqResult sxPostTaskRegisterReq = postService.sxPostTaskRegisterReq(objCommon,
                    new Params.PostTaskRegisterReqParams(objCommon.getTransactionID(), null, null, -1, postProcessRegistrationParam, ""));
            postTaskRegisterReqResult = sxPostTaskRegisterReq;

            if (!CimObjectUtils.isEmpty(passThruReqResult.getHoldReleasedLotIDs())) {
                log.info("Registration of Post-Processng for OpeCompedLot");
                postProcessRegistrationParam.setLotIDs(passThruReqResult.getHoldReleasedLotIDs());
                postTaskRegisterReqResult2 = postService.sxPostTaskRegisterReq(objCommon,
                        new Params.PostTaskRegisterReqParams("ThTRC004", null, null, -1, postProcessRegistrationParam, ""));
                postTaskRegisterReqResult2 = sxPostTaskRegisterReq;
            }

            //-------------------------------------
            // Post-Processing Execution Section
            //-------------------------------------
            postController.postTaskExecuteReq(new Params.PostTaskExecuteReqParams(user, postTaskRegisterReqResult.getDKey(), 1, 0, null, ""));
            if (postTaskRegisterReqResult2 != null) {
                postController.postTaskExecuteReq(new Params.PostTaskExecuteReqParams(user, postTaskRegisterReqResult2.getDKey(), 1, 0, null, ""));
            }
        }

        return Response.createSucc(txId, retVal.getStrGatePassLotsResult());
    }

    @Override
    @ResponseBody
    @RequestMapping(value = "/pass_thru/req", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.GATE_PASS_REQ)
    @EnablePostProcess(proxy = PassThruPostProcessProxy.class)
    public Response passThruReq(@RequestBody Params.PassThruReqParams params) {
        String txId = TransactionIDEnum.GATE_PASS_REQ.getValue();
        ThreadContextHolder.setTransactionId(txId);
        Validations.check(params.getGatePassLotInfos() == null, retCodeConfig.getInvalidParameter());

        List<ObjectIdentifier> lotIDs = new ArrayList<>();
        params.getGatePassLotInfos().forEach(gatePassLotInfo -> lotIDs.add(gatePassLotInfo.getLotID()));

        //Step2 - txAccessControlCheckInq
        Params.AccessControlCheckInqParams privilegeCheckParams = new Params.AccessControlCheckInqParams(true);
        privilegeCheckParams.setLotIDLists(lotIDs);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId,
                params.getUser(),
                privilegeCheckParams);

        Results.PassThruReqResult retVal = new Results.PassThruReqResult();
        retVal.setStrGatePassLotsResult(new ArrayList<>());
        retVal.setHoldReleasedLotIDs(new ArrayList<>());

        //-------------------------------
        // Main Process
        //-------------------------------
        params.getGatePassLotInfos().forEach(gatePassLotInfo -> {
            //Step3 - txPassThruReq
            final Results.PassThruReqResult passThruReqResult = lotService.sxPassThruReq(objCommon,
                    gatePassLotInfo,
                    params.getClaimMemo());
            retVal.getStrGatePassLotsResult().addAll(passThruReqResult.getStrGatePassLotsResult());
            retVal.getHoldReleasedLotIDs().addAll(passThruReqResult.getHoldReleasedLotIDs());
        });

        return Response.createSucc(txId, retVal);
    }

    @ResponseBody
    @RequestMapping(value = "/old/branch/req", method = RequestMethod.POST)
    //@CimMapping({TransactionIDEnum.BRANCH_REQ, TransactionIDEnum.SUB_ROUTE_BRANCH_REQ})
    public Response branchReqOld(@RequestBody Params.BranchReqParams params) {
        // init param
        Infos.BranchReq branchReq = params.getBranchReq();
        Validations.check(null == branchReq, "the branchReq is null");
        final String txId = CimBooleanUtils.isTrue(branchReq.getBDynamicRoute()) ? TransactionIDEnum.BRANCH_REQ.getValue() : TransactionIDEnum.SUB_ROUTE_BRANCH_REQ.getValue();
        ThreadContextHolder.setTransactionId(txId);

        //【step2】call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setLotIDLists(Arrays.asList(branchReq.getLotID()));
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, params.getUser(), accessControlCheckInqParams);

        //【step3】call sxBranchReq(...)
        lotService.sxBranchReq(objCommon, branchReq, params.getClaimMemo());

        //-----------------------------
        // Call postProcess
        //-----------------------------
        log.info("Call postProcess");
        Params.PostTaskRegisterReqParams postTaskRegisterReqParams = new Params.PostTaskRegisterReqParams(objCommon.getTransactionID(), null, null, -1,
                new Infos.PostProcessRegistrationParam(null, null, Arrays.asList(branchReq.getLotID()), null), "");
        postController.execPostProcess(objCommon, postTaskRegisterReqParams);

        return Response.createSucc(txId);
    }

    @Override
    @ResponseBody
    @RequestMapping(value = "/branch/req", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.BRANCH_REQ)
    @EnablePostProcess
    public Response branchReq(@RequestBody Params.BranchReqParams params) {
        // init param
        Infos.BranchReq branchReq = params.getBranchReq();
        Validations.check(null == branchReq, "the branchReq is null");
        final String txId = CimBooleanUtils.isTrue(branchReq.getBDynamicRoute()) ?
                TransactionIDEnum.BRANCH_REQ.getValue() :
                TransactionIDEnum.SUB_ROUTE_BRANCH_REQ.getValue();
        ThreadContextHolder.setTransactionId(txId);

        //【step2】call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setLotIDLists(Collections.singletonList(branchReq.getLotID()));
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId,
                params.getUser(),
                accessControlCheckInqParams);

        //【step3】call sxBranchReq(...)
        lotService.sxBranchReq(objCommon, branchReq, params.getClaimMemo());

        return Response.createSucc(txId);
        //-----------------------------
        // PostProcess
        //-----------------------------
    }

    @ResponseBody
    @RequestMapping(value = "/old/branch_with_hold_release/req", method = RequestMethod.POST)
    //@CimMapping(TransactionIDEnum.BRANCH_WITH_HOLD_RELEASE_REQ)
    public Response branchWithHoldReleaseReqOld(@RequestBody Params.BranchWithHoldReleaseReqParams params) {
        TransactionIDEnum transactionID = TransactionIDEnum.BRANCH_WITH_HOLD_RELEASE_REQ;
        ThreadContextHolder.setTransactionId(transactionID.getValue());

        //【step0】check input params
        User user = params.getUser();

        Infos.BranchReq branchReq = params.getBranchReq();
        ObjectIdentifier releaseReasonCodeID = params.getReleaseReasonCodeID();
        List<Infos.LotHoldReq> strLotHoldReleaseReqList = params.getStrLotHoldReleaseReqList();
        Validations.check(null == branchReq || ObjectIdentifier.isEmpty(releaseReasonCodeID) || CimObjectUtils.isEmpty(strLotHoldReleaseReqList), "the parameter is null");

        //【step1】call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        List<ObjectIdentifier> lotIDLists = new ArrayList<>();
        lotIDLists.add(branchReq.getLotID());
        accessControlCheckInqParams.setLotIDLists(lotIDLists);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID.getValue(), user, accessControlCheckInqParams);

        //【step2】call sxBranchReq(...)
        lotService.sxBranchWithHoldReleaseReq(objCommon, branchReq, releaseReasonCodeID, strLotHoldReleaseReqList, params.getClaimMemo());

        //【Step3】Post-Process task Registration;
        Infos.PostProcessRegistrationParam postProcessRegistrationParm = new Infos.PostProcessRegistrationParam();
        postProcessRegistrationParm.setLotIDs(lotIDLists);
        Params.PostTaskRegisterReqParams processActionRegistReqParams = new Params.PostTaskRegisterReqParams();
        processActionRegistReqParams.setTransactionID(objCommon.getTransactionID());
        processActionRegistReqParams.setSequenceNumber(-1);
        processActionRegistReqParams.setPostProcessRegistrationParm(postProcessRegistrationParm);
        processActionRegistReqParams.setClaimMemo(params.getClaimMemo());
        Results.PostTaskRegisterReqResult postTaskRegisterReqResult = postService.sxPostTaskRegisterReq(objCommon, processActionRegistReqParams);
        //【Step4】Post-Process task Execution Section;
        log.info("Call postProcess");
        Params.PostTaskExecuteReqParams postTaskExecuteReqParams = new Params.PostTaskExecuteReqParams();
        postTaskExecuteReqParams.setUser(objCommon.getUser());
        postTaskExecuteReqParams.setKey(postTaskRegisterReqResult == null ? null : postTaskRegisterReqResult.getDKey());
        postTaskExecuteReqParams.setSyncFlag(1);
        postTaskExecuteReqParams.setPreviousSequenceNumber(0);
        postController.postTaskExecuteReq(postTaskExecuteReqParams);
        return Response.createSucc(transactionID.getValue());
    }

    @Override
    @ResponseBody
    @RequestMapping(value = "/branch_with_hold_release/req", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.BRANCH_WITH_HOLD_RELEASE_REQ)
    @EnablePostProcess
    public Response branchWithHoldReleaseReq(@RequestBody Params.BranchWithHoldReleaseReqParams params) {
        TransactionIDEnum transactionID = TransactionIDEnum.BRANCH_WITH_HOLD_RELEASE_REQ;
        ThreadContextHolder.setTransactionId(transactionID.getValue());

        //【step0】check input params
        User user = params.getUser();

        Infos.BranchReq branchReq = params.getBranchReq();
        ObjectIdentifier releaseReasonCodeID = params.getReleaseReasonCodeID();
        List<Infos.LotHoldReq> strLotHoldReleaseReqList = params.getStrLotHoldReleaseReqList();
        Validations.check(null == branchReq || ObjectIdentifier.isEmpty(releaseReasonCodeID) ||
                CimObjectUtils.isEmpty(strLotHoldReleaseReqList), "the parameter is null");

        //【step1】call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        List<ObjectIdentifier> lotIDLists = new ArrayList<>();
        lotIDLists.add(branchReq.getLotID());
        accessControlCheckInqParams.setLotIDLists(lotIDLists);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID.getValue(),
                user,
                accessControlCheckInqParams);

        //【step2】call sxBranchReq(...)
        lotService.sxBranchWithHoldReleaseReq(objCommon, branchReq, releaseReasonCodeID,
                strLotHoldReleaseReqList,
                params.getClaimMemo());

        return Response.createSucc(transactionID.getValue());
        //-----------------------------
        // PostProcess
        //-----------------------------
    }

    @ResponseBody
    @RequestMapping(value = "/old/unscrap_wafer/req", method = RequestMethod.POST)
    //@CimMapping(TransactionIDEnum.SCRAP_WAFER_CANCEL_REQ)
    public Response unscrapWaferReqOld(@RequestBody Params.UnscrapWaferReqParams params) {
        //【step0】init params
        final String transactionID = TransactionIDEnum.SCRAP_WAFER_CANCEL_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //【step2】call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setLotIDLists(Arrays.asList(params.getLotID()));
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), accessControlCheckInqParams);


        //【step3】call sxUnscrapWaferReq(...)
        lotService.sxUnscrapWaferReq(objCommon, params);

        return Response.createSucc(transactionID, null);
    }

    @Override
    @ResponseBody
    @RequestMapping(value = "/unscrap_wafer/req", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.SCRAP_WAFER_CANCEL_REQ)
    @EnablePostProcess
    public Response unscrapWaferReq(@RequestBody Params.UnscrapWaferReqParams params) {
        //【step0】init params
        final String transactionID = TransactionIDEnum.SCRAP_WAFER_CANCEL_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //【step2】call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setLotIDLists(Collections.singletonList(params.getLotID()));
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID,
                params.getUser(),
                accessControlCheckInqParams);


        //【step3】call sxUnscrapWaferReq(...)
        lotService.sxUnscrapWaferReq(objCommon, params);

        return Response.createSucc(transactionID);
    }

    @ResponseBody
    @RequestMapping(value = "/unscrap_wafer_not_on_pf/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.SCRAP_WAFER_NOT_ON_ROUTE_CANCEL_REQ)
    public Response unscrapWaferNotOnPfReq(@RequestBody Params.ScrapWaferNotOnPfReqParams params) {
        //【step0】init params
        final String transactionID = TransactionIDEnum.SCRAP_WAFER_NOT_ON_ROUTE_CANCEL_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //【step2】call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setLotIDLists(Arrays.asList(params.getLotID()));
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), accessControlCheckInqParams);

        //【step3】call sxScrapWaferNotOnPfReq(...)
        lotService.sxUnscrapWaferNotOnPfReq(objCommon, params);

        return Response.createSucc(transactionID, null);
    }

    @ResponseBody
    @RequestMapping(value = "/scrap_wafer_not_on_pf/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.SCRAP_WAFER_NOT_ON_ROUTE_REQ)
    public Response scrapWaferNotOnPfReq(@RequestBody Params.ScrapWaferNotOnPfReqParams params) {
        //【step0】init params
        final String transactionID = TransactionIDEnum.SCRAP_WAFER_NOT_ON_ROUTE_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //【step2】call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setLotIDLists(Arrays.asList(params.getLotID()));
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), accessControlCheckInqParams);

        //【step3】call sxScrapWaferNotOnPfReq(...)
        lotService.sxScrapWaferNotOnPfReq(objCommon, params);
        return Response.createSucc(transactionID, null);
    }

    @ResponseBody
    @RequestMapping(value = "/old/scrap_wafer/req", method = RequestMethod.POST)
    //@CimMapping(TransactionIDEnum.SCRAP_WAFER_REQ)
    public Response scrapWaferReqOld(@RequestBody Params.ScrapWaferReqParams params) {
        System.out.println("==================================");
        System.out.println(JSONObject.toJSONString(params));
        //【step0】init params
        final String transactionID = TransactionIDEnum.SCRAP_WAFER_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);
        ObjectIdentifier lotID = params.getLotID();

        //【step2】call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setLotIDLists(Arrays.asList(lotID));
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), accessControlCheckInqParams);

        //【step3】call sxScrapWaferReq(...)
        lotService.sxScrapWaferReq(objCommon, params);
        return Response.createSucc(transactionID, null);
    }

    @Override
    @ResponseBody
    @RequestMapping(value = "/scrap_wafer/req", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.SCRAP_WAFER_REQ)
    @EnablePostProcess
    public Response scrapWaferReq(@RequestBody Params.ScrapWaferReqParams params) {
        //【step0】init params
        final String transactionID = TransactionIDEnum.SCRAP_WAFER_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //【step2】call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setLotIDLists(Collections.singletonList(params.getLotID()));
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID,
                params.getUser(),
                accessControlCheckInqParams);

        //【step3】call sxScrapWaferReq(...)
        lotService.sxScrapWaferReq(objCommon, params);
        return Response.createSucc(transactionID);
    }

    @ResponseBody
    @RequestMapping(value = "/lot_contamination_update/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.LOT_CONTAMINATION_UPDATE)
    public Response lotContaminationUpdateReq(@RequestBody Params.LotContaminationParams params) {
        String txId = TransactionIDEnum.LOT_CONTAMINATION_UPDATE.getValue();
        ThreadContextHolder.setTransactionId(txId);

        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, params.getUser(), new Params.AccessControlCheckInqParams(true));

        lotService.sxLotContaminationUpdateReq(params, objCommon);

        return Response.createSucc(txId, null);
    }

    @ResponseBody
    @RequestMapping(value = "/hold_department_change/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.DEPARTMENT_AUTHORITY_HOLD_DEPARTMENT_CHANGE_REQ)
    public Response holdDepartmentChangeReq(@RequestBody Params.HoldLotReqParams holdLotReqParams) {
        // step1 tx
        String transactionID = TransactionIDEnum.DEPARTMENT_AUTHORITY_HOLD_DEPARTMENT_CHANGE_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //step2 call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        List<ObjectIdentifier> lotIDLists = new ArrayList<>();
        lotIDLists.add(holdLotReqParams.getLotID());
        accessControlCheckInqParams.setLotIDLists(lotIDLists);
        accessControlCheckInqParams.conversionDepartmentEditCheckCode(holdLotReqParams.getHoldReqList());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, holdLotReqParams.getUser(), accessControlCheckInqParams);

        //step3 hold department change req
        lotService.sxHoldDepartmentChangeReq(objCommon, holdLotReqParams.getLotID(), holdLotReqParams.getHoldReqList());
        return Response.createSucc(transactionID, null);
    }

    @ResponseBody
    @RequestMapping(value = "/lot_npw_usage_recycle_count_update/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.LOT_NPW_USAGE_RECYCLE_COUNT_UPDATE_REQ)
    public Response lotNpwUsageRecycleCountUpdateReq(@RequestBody LotNpwUsageRecycleCountUpdateParams params) {
        // step1 tx
        String txId = TransactionIDEnum.LOT_NPW_USAGE_RECYCLE_COUNT_UPDATE_REQ.getValue();
        ThreadContextHolder.setTransactionId(txId);
        //step2 call txAccessControlCheckInq(...)
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, params.getUser(), new Params.AccessControlCheckInqParams(true));

        lotService.sxLotNpwUsageRecycleCountUpdateReq(objCommon, params);

        return Response.createSucc(txId, null);
    }

    @ResponseBody
    @RequestMapping(value = "/lot_npw_usage_recycle_limit_update/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.LOT_NPW_USAGE_RECYCLE_LIMIT_UPDATE_REQ)
    public Response lotNpwUsageRecycleLimitUpdateReq(@RequestBody LotNpwUsageRecycleLimitUpdateParams params) {
        // step1 tx
        String txId = TransactionIDEnum.LOT_NPW_USAGE_RECYCLE_LIMIT_UPDATE_REQ.getValue();
        ThreadContextHolder.setTransactionId(txId);
        //step2 call txAccessControlCheckInq(...)
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, params.getUser(), new Params.AccessControlCheckInqParams(true));

        lotService.sxLotNpwUsageRecycleLimitUpdateReq(objCommon, params);

        return Response.createSucc(txId, null);
    }

    @Override
    @RequestMapping(value = "/terminate/req", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.LOT_TERMINATE_REQ)
    public @ResponseBody Response lotTerminateReq(@RequestBody TerminateReq.TerminateReqParams params) {
        final String transactionID = TransactionIDEnum.LOT_TERMINATE_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setLotIDLists(Collections.singletonList(params.getLotID()));
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID,
                params.getUser(), accessControlCheckInqParams);

        lotService.sxTerminateReq(objCommon, params);

        return Response.createSucc(transactionID);
    }

    @Override
    @RequestMapping(value = "/terminate_cancel/req", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.LOT_TERMINATE_CANCEL_REQ)
    public @ResponseBody Response lotTerminateCancelReq(@RequestBody TerminateReq.TerminateCancelReqParams params) {
        final String transactionID = TransactionIDEnum.LOT_TERMINATE_CANCEL_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setLotIDLists(Collections.singletonList(params.getLotID()));
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID,
                params.getUser(), accessControlCheckInqParams);

        lotService.sxTerminateCancelReq(objCommon, params);

        return Response.createSucc(transactionID);
    }

}
