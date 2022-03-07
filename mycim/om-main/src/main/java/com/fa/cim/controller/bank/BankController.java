package com.fa.cim.controller.bank;

import com.alibaba.fastjson.JSONObject;
import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.Response;
import com.fa.cim.common.support.User;
import com.fa.cim.common.utils.CimArrayUtils;
import com.fa.cim.common.utils.CimObjectUtils;
import com.fa.cim.common.utils.ThreadContextHolder;
import com.fa.cim.common.utils.Validations;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.controller.interfaces.bank.IBankController;
import com.fa.cim.controller.post.PostController;
import com.fa.cim.dto.*;
import com.fa.cim.frameworks.pprocess.api.annotations.EnablePostProcess;
import com.fa.cim.method.ILotMethod;
import com.fa.cim.method.IUtilsComp;
import com.fa.cim.method.impl.MessageMethod;
import com.fa.cim.middleware.standard.api.annotations.listener.CimMapping;
import com.fa.cim.middleware.standard.api.annotations.listener.Listenable;
import com.fa.cim.service.access.IAccessInqService;
import com.fa.cim.service.bank.IBankService;
import com.fa.cim.service.post.IPostService;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.util.Asserts;
import org.bytesoft.compensable.Compensable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
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
 * @author Nyx
 * @since 2019/7/26 14:44
 * Copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@RestController
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
@Compensable(interfaceClass = IBankController.class, confirmableKey = "BankConfirm", cancellableKey = "BankCancel")
@RequestMapping("/bank")
@Listenable
public class BankController implements IBankController {

    @Autowired
    private RetCodeConfig retCodeConfig;
    @Autowired
    private IUtilsComp utilsComp;
    @Autowired
    private IAccessInqService accessInqService;
    @Autowired
    private IBankService bankService;
    @Autowired
    private ILotMethod lotMethod;

    @Autowired
    private PostController postController;
    @Autowired
    private IPostService postService;
    @Autowired
    private MessageMethod messageMethod;

    @ResponseBody
    @RequestMapping(value = "/bank_move/req", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.BANK_MOVE_REQ)
    @Override
    public Response bankMoveReq(@RequestBody Params.BankMoveReqParams bankMoveReqParams) {
        // init params
        final String transactionID = TransactionIDEnum.BANK_MOVE_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        List<ObjectIdentifier> lotIDs = bankMoveReqParams.getLotIDs();
        ObjectIdentifier bankID = bankMoveReqParams.getBankID();
        Validations.check(CimObjectUtils.isEmpty(lotIDs) || ObjectIdentifier.isEmpty(bankID), "the parameter is null...");

        //Step1 - txAccessControlCheckInq
        Params.AccessControlCheckInqParams privilegeCheckParams = new Params.AccessControlCheckInqParams(true);
        privilegeCheckParams.setLotIDLists(lotIDs);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, bankMoveReqParams.getUser(), privilegeCheckParams);

        log.info("【step3】call sxBankMoveReq");
        List<Infos.ReturnCodeInfo> result = bankService.sxBankMoveReq(objCommon, bankMoveReqParams);

        return Response.createSucc(transactionID, result);
    }

    @ResponseBody
    @RequestMapping(value = "/hold_lot_in_bank/req", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.HOLD_BANK_LOT_REQ)
    @Override
    public Response holdLotInBankReq(@RequestBody Params.HoldLotInBankReqParams holdLotInBankReqParams) {
        final String transactionID = TransactionIDEnum.HOLD_BANK_LOT_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //step 1 - Pre Process
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        accessControlCheckInqParams.setLotIDLists(holdLotInBankReqParams.getLotIDs());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, holdLotInBankReqParams.getUser(), accessControlCheckInqParams);

        //step 2 - Main Process
        Results.HoldLotInBankReqResult holdLotInBankReqResult = null;
        for (int i = 0; i < holdLotInBankReqParams.getLotIDs().size(); i++) {
            holdLotInBankReqResult = bankService.sxHoldLotInBankReqReq(objCommon, i, holdLotInBankReqParams);
        }
        Response response = Response.createSucc(transactionID, holdLotInBankReqResult);
        //【step4】judge whether the return code is success, if no, then TCC will rollback
        Validations.isSuccessWithException(response);
        return response;
    }

    @ResponseBody
    @RequestMapping(value = "/hold_lot_release_in_bank/req", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.HOLD_RELEASE_BANK_LOT_REQ)
    @Override
    public Response holdLotReleaseInBankReq(@RequestBody Params.HoldLotReleaseInBankReqParams holdLotReleaseInBankReqParams) {
        final String transactionID = TransactionIDEnum.HOLD_RELEASE_BANK_LOT_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        accessControlCheckInqParams.setLotIDLists(holdLotReleaseInBankReqParams.getLotIDs());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, holdLotReleaseInBankReqParams.getUser(), accessControlCheckInqParams);

        Response response = null;
        //step 2 - Main Process
        for (int i = 0; i < holdLotReleaseInBankReqParams.getLotIDs().size(); i++) {
            List<Infos.HoldHistory> result1 = bankService.sxHoldLotReleaseInBankReq(objCommon, i, holdLotReleaseInBankReqParams);
            // step3 - messageQueue_Put
            Inputs.MessageQueuePutIn msgQueuePut = new Inputs.MessageQueuePutIn();
            msgQueuePut.setLotID(holdLotReleaseInBankReqParams.getLotIDs().get(i));
            msgQueuePut.setCassetteDispatchReserveFlag(false);
            msgQueuePut.setCassetteTransferReserveFlag(false);
            messageMethod.messageQueuePut(objCommon, msgQueuePut);
            response = Response.createSucc(transactionID, result1);
        }
        //【step4】judge whether the return code is success, if no, then TCC will rollback
        Validations.isSuccessWithException(response);
        return response;
    }

    @ResponseBody
    @RequestMapping(value = "/material_prepare_cancel/req", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.LOT_PREPARATION_CANCEL_REQ)
    @Override
    public Response materialPrepareCancelReq(@RequestBody Params.MaterialPrepareCancelReqParams materialPrepareCancelReqParams) {
        //【step0】init params
        final String transactionID = TransactionIDEnum.LOT_PREPARATION_CANCEL_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //【step2】privilege check
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        accessControlCheckInqParams.getLotIDLists().add(materialPrepareCancelReqParams.getPreparationCancelledLotID());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, materialPrepareCancelReqParams.getUser(), accessControlCheckInqParams);

        //【step3】call service MaterialPrepareCancelReq()
        Results.MaterialPrepareCancelReqResult result = bankService.sxMaterialPrepareCancelReq(objCommon, materialPrepareCancelReqParams);
        Response response = Response.createSucc(transactionID, result);

        //【step4】judge whether the return code is success,
        Validations.isSuccessWithException(response);
        return response;
    }

    @ResponseBody
    @RequestMapping(value = "/material_prepare_cancel_ex/req", method = RequestMethod.POST)
    @CimMapping(names = "OBNKW005_EX") //This interface does not have a TXID. Using LOT_PREPARATION_CANCEL_REQ TXID results in a duplicate TX, so it is changed to a unique TXID
    @Override
    public Response lotPreparationCancelExReq(@RequestBody Params.LotPreparationCancelExReqParams params) {
        //【step0】init params
        final String transactionID = TransactionIDEnum.LOT_PREPARATION_CANCEL_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //【step1】get schedule from calendar
        Infos.ObjCommon objCommon = utilsComp.setObjCommon(TransactionIDEnum.LOT_PREPARATION_CANCEL_REQ, params.getUser());

        //【step2】get newVendorLotInfoList which will be canceled.
        Outputs.ObjLotPreparationCancelInfoGetDROut lotPreparationCancelInfoOut = lotMethod.lotPreparationCancelInfoGetDR(objCommon, params.getPreparationCancelledLotID());

        //【step3】call TxMaterialPrepareCancelReq()
        Params.MaterialPrepareCancelReqParams materialPrepareCancelReqParams = new Params.MaterialPrepareCancelReqParams();
        materialPrepareCancelReqParams.setUser(params.getUser());
        materialPrepareCancelReqParams.setPreparationCancelledLotID(params.getPreparationCancelledLotID());
        materialPrepareCancelReqParams.setNewVendorLotInfoList(lotPreparationCancelInfoOut.getNewVendorLotInfoList());
        Response response = this.materialPrepareCancelReq(materialPrepareCancelReqParams);

        //【step4】judge whether the return code is success, if no, then TCC will rollback
        Validations.isSuccessWithException(response);
        return response;
    }

    @ResponseBody
    @RequestMapping(value = "/old/non_prod_bank_store/req", method = RequestMethod.POST)
    //@CimMapping(TransactionIDEnum.NON_PRO_BANK_IN_REQ)
    public Response nonProdBankStoreReqOld(@RequestBody Params.NonProdBankStoreReqParams params) {
        log.debug("【step0】init params");
        final String transactionID = TransactionIDEnum.NON_PRO_BANK_IN_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        log.debug("【step1】call txAccessControlCheckInq(...)");
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        accessControlCheckInqParams.getLotIDLists().add(params.getLotID());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), accessControlCheckInqParams);

        log.debug("【step2】call sxNonProdBankStoreReq(...)");
        bankService.sxNonProdBankStoreReq(objCommon, params);

        Params.PostTaskRegisterReqParams postTaskRegisterReqParams = new Params.PostTaskRegisterReqParams();
        postTaskRegisterReqParams.setTransactionID(objCommon.getTransactionID());
        postTaskRegisterReqParams.setSequenceNumber(-1);
        Infos.PostProcessRegistrationParam postProcessRegistrationParm = new Infos.PostProcessRegistrationParam();
        postTaskRegisterReqParams.setPostProcessRegistrationParm(postProcessRegistrationParm);
        postProcessRegistrationParm.setLotIDs(Collections.singletonList(params.getLotID()));
        postController.execPostProcess(objCommon, postTaskRegisterReqParams);
        return Response.createSucc(transactionID);
    }

    @Override
    @ResponseBody
    @RequestMapping(value = "/non_prod_bank_store/req", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.NON_PRO_BANK_IN_REQ)
    @EnablePostProcess
    public Response nonProdBankStoreReq(@RequestBody Params.NonProdBankStoreReqParams params) {
        log.debug("【step0】init params");
        final String transactionID = TransactionIDEnum.NON_PRO_BANK_IN_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        log.debug("【step1】call txAccessControlCheckInq(...)");
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        accessControlCheckInqParams.getLotIDLists().add(params.getLotID());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(),
                accessControlCheckInqParams);

        log.debug("【step2】call sxNonProdBankStoreReq(...)");
        bankService.sxNonProdBankStoreReq(objCommon, params);

        return Response.createSucc(transactionID);
        //-----------------------------
        // PostProcess
        //-----------------------------
    }

    @ResponseBody
    @RequestMapping(value = "/old/non_prod_bank_release/req", method = RequestMethod.POST)
    //@CimMapping(TransactionIDEnum.NON_PRO_BANK_OUT_REQ)
    public Response nonProdBankReleaseReqOld(@RequestBody Params.NonProdBankReleaseReqParams params) {
        log.debug("【step0】init params");
        final String transactionID = TransactionIDEnum.NON_PRO_BANK_OUT_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        log.debug("【step1】call txAccessControlCheckInq(...)");
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        accessControlCheckInqParams.getLotIDLists().add(params.getLotID());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), accessControlCheckInqParams);

        log.debug("【step2】call sxNonProdBankReleaseReq(...)");
        bankService.sxNonProdBankReleaseReq(objCommon, params.getLotID(), false);

        log.debug("【step3】Resister post action.");
        Params.PostTaskRegisterReqParams postTaskRegisterReqParams = new Params.PostTaskRegisterReqParams(transactionID, null, null, -1,
                new Infos.PostProcessRegistrationParam(null, null, Collections.singletonList(params.getLotID()), null), "");
        postController.execPostProcess(objCommon, postTaskRegisterReqParams);
        return Response.createSucc(transactionID, null);
    }

    @Override
    @ResponseBody
    @RequestMapping(value = "/non_prod_bank_release/req", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.NON_PRO_BANK_OUT_REQ)
    @EnablePostProcess
    public Response nonProdBankReleaseReq(@RequestBody Params.NonProdBankReleaseReqParams params) {
        log.debug("【step0】init params");
        final String transactionID = TransactionIDEnum.NON_PRO_BANK_OUT_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        log.debug("【step1】call txAccessControlCheckInq(...)");
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        accessControlCheckInqParams.getLotIDLists().add(params.getLotID());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(),
                accessControlCheckInqParams);

        log.debug("【step2】call sxNonProdBankReleaseReq(...)");
        bankService.sxNonProdBankReleaseReq(objCommon, params.getLotID(), false);

        return Response.createSucc(transactionID);
        //-----------------------------
        // PostProcess
        //-----------------------------
    }


    @ResponseBody
    @RequestMapping(value = "/unship/req", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.SHIP_CANCEL_REQ)
    @Override
    public Response unshipReq(@RequestBody Params.BankMoveReqParams unshipReqParams) {
        //【step0】init params
        final String transactionID = TransactionIDEnum.SHIP_CANCEL_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        Validations.check(CimObjectUtils.isEmpty(unshipReqParams.getLotIDs()) || ObjectIdentifier.isEmpty(unshipReqParams.getBankID())
                , "the parameter is null...");
        //step1 - done: get schedule from calendar
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        accessControlCheckInqParams.setLotIDLists(unshipReqParams.getLotIDs());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, unshipReqParams.getUser(), accessControlCheckInqParams);

        //step3 - done: call sxUnshipReq(...)
        List<Infos.ReturnCodeInfo> result = bankService.sxUnshipReq(objCommon, unshipReqParams);

        return Response.createSucc(transactionID, result);
    }

    @ResponseBody
    @RequestMapping(value = "/ship/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.SHIP_REQ)
    public Response shipReq(@RequestBody Params.BankMoveReqParams shipReqParams) {

        //【step0】init params
        final String transactionID = TransactionIDEnum.SHIP_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        accessControlCheckInqParams.setLotIDLists(shipReqParams.getLotIDs());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, shipReqParams.getUser(), accessControlCheckInqParams);

        log.info("【step3】call sxShipReq(...)");
        List<Infos.ReturnCodeInfo> result = bankService.sxShipReq(objCommon, shipReqParams);

        //【step4】judge whether the return code is success, if no, then TCC will rollback
        return Response.createSucc(transactionID, result);
    }

    @ResponseBody
    @RequestMapping(value = "/material_prepare/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.VENDOR_LOT_PREPARATION_REQ)
    public Response materialPrepareReq(@RequestBody Params.MaterialPrepareReqParams materialPrepareReqParams) {
        //init params
        final String transactionID = TransactionIDEnum.VENDOR_LOT_PREPARATION_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);
        User user = materialPrepareReqParams.getUser();

        //【step1】call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        List<ObjectIdentifier> lotIDLists = new ArrayList<>();
        int size = 0;
        if (null != materialPrepareReqParams.getNewLotAttributes()) {
            size = CimArrayUtils.getSize(materialPrepareReqParams.getNewLotAttributes().getNewWaferAttributesList());
        }
        for (int i = 0; i < size; i++) {
            Infos.NewWaferAttributes newWaferAttributes = materialPrepareReqParams.getNewLotAttributes().getNewWaferAttributesList().get(i);
            if (!lotIDLists.contains(newWaferAttributes.getSourceLotID())) {
                lotIDLists.add(newWaferAttributes.getSourceLotID());
            }
        }
        accessControlCheckInqParams.setLotIDLists(lotIDLists);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, user, accessControlCheckInqParams);
        //【step2】call sxMaterialPrepareReq(...)
        log.debug("【step3】call sxMaterialPrepareReq(...)");
        Results.MaterialPrepareReqResult result = bankService.sxMaterialPrepareReq(objCommon, materialPrepareReqParams);
        return Response.createSucc(transactionID, result);
    }

    @ResponseBody
    @RequestMapping(value = "/material_receive_and_prepare/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.VENDOR_LOT_RECEIVE_AND_PREPARE_REQ)
    public Response materialReceiveAndPrepareReq(@RequestBody Params.MaterialReceiveAndPrepareReqParams materialReceiveAndPrepareReqParams) {
        // print the in-param
        log.debug("materialReceiveAndPrepareReqParams: {}", JSONObject.toJSONString(materialReceiveAndPrepareReqParams));

        //Step-0:Initialize Parameters;
        final String transactionID = TransactionIDEnum.VENDOR_LOT_RECEIVE_AND_PREPARE_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //Step-3:txAccessControlCheckInq;
        log.debug("【Step-3】-txAccessControlCheckInq(...)");
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        accessControlCheckInqParams.getLotIDLists().add(materialReceiveAndPrepareReqParams.getVendorLotID());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, materialReceiveAndPrepareReqParams.getUser(), accessControlCheckInqParams);


        //Step-4:txMaterialReceiveAndPrepareReq;
        log.debug("【Step-4】call-txMaterialReceiveAndPrepareReq(...)");

        ObjectIdentifier lotID = bankService.sxMaterialReceiveAndPrepareReq(materialReceiveAndPrepareReqParams, objCommon);
        // Step-5:Post Process(Generate Output Results/event log put/Set Transaction ID);
        Response response = Response.createSucc(transactionID, lotID);
        //whether roll back;
        Validations.isSuccessWithException(response);
        return response;
    }

    @ResponseBody
    @RequestMapping(value = "/old/bank_in_cancel/req", method = RequestMethod.POST)
    //@CimMapping(TransactionIDEnum.BANK_IN_CANCEL_REQ)
    public Response bankInCancelReqOld(@RequestBody Params.BankInCancelReqParams params) {
        log.debug("【step0】init params");
        final String transactionID = TransactionIDEnum.BANK_IN_CANCEL_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        log.debug("【step1】txAccessControlCheckInq");
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        accessControlCheckInqParams.getLotIDLists().add(params.getLotID());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), accessControlCheckInqParams);

        log.debug("【step2】call sxBankInCancelReq(...)");
        bankService.sxBankInCancelReq(objCommon, params.getLotID(), params.getClaimMemo());

        log.debug("【step3】call post process");
        List<ObjectIdentifier> lotIds = new ArrayList<>();
        lotIds.add(params.getLotID());
        Params.PostTaskRegisterReqParams postTaskRegisterReqParamsForChild = new Params.PostTaskRegisterReqParams(transactionID, null, null, -1,
                new Infos.PostProcessRegistrationParam(null, null, lotIds, null), "");
        postController.execPostProcess(objCommon, postTaskRegisterReqParamsForChild);
        Response response = Response.createSucc(transactionID, null);

        log.debug("【step4】judge whether the return code is success, if no, then TCC will rollback");
        Validations.isSuccessWithException(response);
        return response;
    }

    @Override
    @ResponseBody
    @RequestMapping(value = "/bank_in_cancel/req", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.BANK_IN_CANCEL_REQ)
    @EnablePostProcess
    public Response bankInCancelReq(@RequestBody Params.BankInCancelReqParams params) {
        if (log.isDebugEnabled()) log.debug("step1 : Initialising transactionID");
        final String transactionID = TransactionIDEnum.BANK_IN_CANCEL_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        if (log.isDebugEnabled()) log.debug("step2 : Privilege Check");
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        accessControlCheckInqParams.getLotIDLists().add(params.getLotID());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID,
                params.getUser(), accessControlCheckInqParams);

        if (log.isDebugEnabled()) log.debug("step3 : Main Process. call sxBankInCancelReq(...)");
        bankService.sxBankInCancelReq(objCommon, params.getLotID(), params.getClaimMemo());

        return Response.createSucc(transactionID);
        //-----------------------------
        // PostProcess
        //-----------------------------
    }

    @ResponseBody
    @RequestMapping(value = "/old/bank_in/req", method = RequestMethod.POST)
    //@CimMapping(TransactionIDEnum.BANK_IN_REQ)
    public Response bankInReqOld(@RequestBody Params.BankInReqParams params) {
        log.debug("【step0】init params");
        final String transactionID = TransactionIDEnum.BANK_IN_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        log.debug("【step1】 txAccessControlCheckInq");
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        List<ObjectIdentifier> lotIDs = params.getLotIDs();
        accessControlCheckInqParams.setLotIDLists(lotIDs);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), accessControlCheckInqParams);

        log.debug("【step2】call sxBankInReq(...)");
        int failCnt = 0;
        StringBuilder failInfo = new StringBuilder();
        for (int i = 0; i < lotIDs.size(); i++) {
            List<Infos.BankInLotResult> result = bankService.sxBankInReq(objCommon, i, lotIDs, params.getClaimMemo());
            if (CimArrayUtils.getSize(result) == 0) {
                log.info("Resister post action.");
                Params.PostTaskRegisterReqParams postTaskRegisterReqParams = new Params.PostTaskRegisterReqParams(objCommon.getTransactionID(), null, null, -1,
                        new Infos.PostProcessRegistrationParam(null, null, Collections.singletonList(lotIDs.get(i)), null), "");
                Results.PostTaskRegisterReqResult sxPostTaskRegisterReq = postService.sxPostTaskRegisterReq(objCommon, postTaskRegisterReqParams);
                log.info("Post-Processing Execution Section");
                postController.postTaskExecuteReq(new Params.PostTaskExecuteReqParams(objCommon.getUser(), sxPostTaskRegisterReq.getDKey()
                        , 1, 0, null, ""));
            } else {
                if (failCnt != 0) failInfo.append('\n');
                failInfo.append(result.get(i).getReturnCode().getMessage());
                failCnt++;
            }
        }
        Validations.check(failCnt > 0, retCodeConfig.getSomeRequestsFailed().getCode(), failInfo.toString());
        return Response.createSucc(transactionID, null);
    }

    @Override
    @ResponseBody
    @RequestMapping(value = "/bank_in/req", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.BANK_IN_REQ)
    @EnablePostProcess
    public Response bankInReq(@RequestBody Params.BankInReqParams params) {
        if (log.isInfoEnabled()) log.debug("step1 : Initialising transactionID");
        final String transactionID = TransactionIDEnum.BANK_IN_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        if (log.isInfoEnabled()) log.debug("step2 : Privilege Check");
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        List<ObjectIdentifier> lotIDs = params.getLotIDs();
        accessControlCheckInqParams.setLotIDLists(lotIDs);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID,
                params.getUser(), accessControlCheckInqParams);

        if (log.isInfoEnabled()) log.debug("step3 : Main Process. call sxBankInReq(...)");
        int failCnt = 0;
        StringBuilder failInfo = new StringBuilder();
        for (int i = 0; i < lotIDs.size(); i++) {
            final List<Infos.BankInLotResult> bankInLotResults = bankService.sxBankInReq(objCommon,
                    i,
                    lotIDs,
                    params.getClaimMemo());
            if (CimArrayUtils.isNotEmpty(bankInLotResults)) {
                if (failCnt != 0) failInfo.append('\n');
                failInfo.append(bankInLotResults.get(i).getReturnCode().getMessage());
                failCnt++;
            }
        }
        Validations.check(failCnt > 0, retCodeConfig.getSomeRequestsFailed().getCode(), failInfo.toString());
        return Response.createSucc(transactionID);
        //-----------------------------
        // Post Process
        //-----------------------------
    }

    @ResponseBody
    @RequestMapping(value = "/material_receive/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.VEND_LOT_RECEIVE_REQ)
    public Response vendorLotReceiveReq(@RequestBody Params.VendorLotReceiveParams vendorLotReceiveParams) {
        final String transactionID = TransactionIDEnum.VEND_LOT_RECEIVE_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        User user = vendorLotReceiveParams.getUser();
        // step0 - check input params
        log.info("vendorLotReceiveParams : {}", vendorLotReceiveParams);
        Asserts.check(null != vendorLotReceiveParams.getProductID(), "the ProductID is null!");
        Asserts.check(null != vendorLotReceiveParams.getSubLotType(), "the LotTypeSubLotType is null!");
        Asserts.check(null != vendorLotReceiveParams.getProductWaferCount(), "the ProductWaferCount is null!");

        //step1 - done:  Pre Process
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        accessControlCheckInqParams.getProductIDList().add(vendorLotReceiveParams.getProductID());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, user, accessControlCheckInqParams);

        //step2 - Main Process
        Results.VendorLotReceiveReqResult result = bankService.sxVendorLotReceiveReq(objCommon, vendorLotReceiveParams);
        Response response = Response.createSucc(transactionID, result);
        // step 3 - :  Post Process
        //【step4】judge whether the return code is success, if no, then TCC will rollback
        Validations.isSuccessWithException(response);
        return response;
    }

    @ResponseBody
    @RequestMapping(value = "/product_id/inq", method = RequestMethod.POST)
    public Response getProduct(@RequestBody Params.productInqParams productInqParams) {
        Results.ProductIDResult productIDObj = new Results.ProductIDResult();
        String type = productInqParams.getAll();
        String key = productInqParams.getRaw();
        if (StringUtils.isEmpty(type)) {
            productIDObj.setProductSpecificationList(bankService.getRaw(key));
        } else {
            productIDObj.setProductSpecificationList(bankService.getProductID(key));
        }
        return Response.createSucc(null, productIDObj);
    }

    @ResponseBody
    @RequestMapping(value = "/material_return/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.VEND_LOT_RETURN_REQ)
    public Response vendorLotReturnReq(@RequestBody Params.VendorLotReturnParams vendorLotReturnParams) {
        Object args = JSONObject.toJSON(vendorLotReturnParams);
        log.debug("vendorLotReturnParams: {}", args);
        //Step0 - init params
        final String transactionID = TransactionIDEnum.VEND_LOT_RETURN_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //Step1 - txAccessControlCheckInq
        Params.AccessControlCheckInqParams privilegeCheckParams = new Params.AccessControlCheckInqParams(true);
        privilegeCheckParams.getLotIDLists().add(vendorLotReturnParams.getLotID());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, vendorLotReturnParams.getUser(), privilegeCheckParams);

        //step 2 - Main Process
        Results.VendorLotReturnReqResult result = bankService.sxVendorLotReturnReq(objCommon, vendorLotReturnParams);
        Response response = Response.createSucc(transactionID, result);

        // step 3 - :  Post Process
        //【step4】judge whether the return code is success, if no, then TCC will rollback
        Validations.isSuccessWithException(response);
        return response;

    }
}
