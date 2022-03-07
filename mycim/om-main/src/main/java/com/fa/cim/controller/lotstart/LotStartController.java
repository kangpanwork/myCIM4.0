package com.fa.cim.controller.lotstart;

import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.Response;
import com.fa.cim.common.support.User;
import com.fa.cim.common.utils.CimArrayUtils;
import com.fa.cim.common.utils.CimStringUtils;
import com.fa.cim.common.utils.ThreadContextHolder;
import com.fa.cim.common.utils.Validations;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.controller.interfaces.lotStart.ILotStartController;
import com.fa.cim.controller.post.PostController;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;
import com.fa.cim.frameworks.pprocess.api.annotations.EnablePostProcess;
import com.fa.cim.method.IUtilsComp;
import com.fa.cim.middleware.standard.api.annotations.listener.CimMapping;
import com.fa.cim.middleware.standard.api.annotations.listener.Listenable;
import com.fa.cim.service.access.IAccessInqService;
import com.fa.cim.service.lot.ILotInqService;
import com.fa.cim.service.lotstart.ILotStartInqService;
import com.fa.cim.service.lotstart.ILotStartService;
import com.fa.cim.service.post.IPostService;
import lombok.extern.slf4j.Slf4j;
import org.bytesoft.compensable.Compensable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/7/30          ********            Nyx                create file
 *
 * @author: Nyx
 * @date: 2019/7/30 13:50
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@RestController
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
@Compensable(interfaceClass = ILotStartController.class, confirmableKey = "LotStartConfirm", cancellableKey = "LotStartCancel")
@RequestMapping("/lotstart")
@Listenable
public class LotStartController implements ILotStartController {
    @Autowired
    private IUtilsComp utilsComp;
    @Autowired
    private RetCodeConfig retCodeConfig;
    @Autowired
    private IAccessInqService accessInqService;
    @Autowired
    private ILotStartInqService lotStartInqService;
    @Autowired
    private IPostService postService;
    @Autowired
    private PostController postController;
    @Autowired
    private ILotInqService lotInqService;
    @Autowired
    private ILotStartService lotStartService;


    @ResponseBody
    @RequestMapping(value = "/old/npw_lot_start/req", method = RequestMethod.POST)
    //@CimMapping(TransactionIDEnum.CTRL_LOT_STB_REQ)
    public Response npwLotStartReqOld(@RequestBody Params.NPWLotStartReqParams params) {
        //【step0】init params
        final String transactionID = TransactionIDEnum.CTRL_LOT_STB_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //【step1】call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams privilegeCheckParams = new Params.AccessControlCheckInqParams(true);
        privilegeCheckParams.setProductIDList(CimArrayUtils.generateList(params.getProductID()));
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), privilegeCheckParams);

        //【step2】call sxNPWLotStartReq(...)
        ObjectIdentifier result = lotStartService.sxNPWLotStartReq(objCommon, params);

        //【step3】call txPostTaskRegisterReq__100
        List<ObjectIdentifier> lotIDs = CimArrayUtils.generateList(result);
        Params.PostTaskRegisterReqParams postTaskRegisterReqParams = new Params.PostTaskRegisterReqParams();
        Infos.PostProcessRegistrationParam postProcessRegistrationParam = new Infos.PostProcessRegistrationParam();
        postProcessRegistrationParam.setLotIDs(lotIDs);
        postTaskRegisterReqParams.setTransactionID(objCommon.getTransactionID());
        postTaskRegisterReqParams.setSequenceNumber(-1);
        postTaskRegisterReqParams.setPostProcessRegistrationParm(postProcessRegistrationParam);
        postTaskRegisterReqParams.setClaimMemo(params.getClaimMemo());
        postTaskRegisterReqParams.setUser(params.getUser());
        Results.PostTaskRegisterReqResult postTaskRegisterReqResult = postService.sxPostTaskRegisterReq(objCommon, postTaskRegisterReqParams);
        //【step4】call TxPostTaskExecuteReq__100
        Params.PostTaskExecuteReqParams postTaskExecuteReqParams = new Params.PostTaskExecuteReqParams();
        postTaskExecuteReqParams.setUser(params.getUser());
        postTaskExecuteReqParams.setKey(postTaskRegisterReqResult == null ? null : postTaskRegisterReqResult.getDKey());
        postTaskExecuteReqParams.setSyncFlag(1);
        postTaskExecuteReqParams.setPreviousSequenceNumber(0);
        postController.postTaskExecuteReq(postTaskExecuteReqParams);
        return Response.createSucc(transactionID, result);
    }

    @Override
    @ResponseBody
    @RequestMapping(value = "/npw_lot_start/req", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.CTRL_LOT_STB_REQ)
    @EnablePostProcess
    public Response npwLotStartReq(@RequestBody Params.NPWLotStartReqParams params) {
        //【step0】init params
        final String transactionID = TransactionIDEnum.CTRL_LOT_STB_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //【step1】call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams privilegeCheckParams = new Params.AccessControlCheckInqParams(true);
        privilegeCheckParams.setProductIDList(CimArrayUtils.generateList(params.getProductID()));
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID,
                params.getUser(), privilegeCheckParams);

        //【step2】call sxNPWLotStartReq(...)
        Results.NPWLotStratReq retVal = new Results.NPWLotStratReq();
        ObjectIdentifier result = lotStartService.sxNPWLotStartReq(objCommon, params);
        retVal.setControlLotID(result);

        return Response.createSucc(transactionID, retVal);
        //-----------------------------
        // PostProcess
        //-----------------------------
    }

    @ResponseBody
    @RequestMapping(value = "/wafer_alias_set/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.ALIAS_WAFER_NAME_SET_REQ)
    public Response waferAliasSetReq(@RequestBody Params.WaferAliasSetReqParams params) {
        //【step0】init params
        final String transactionID = TransactionIDEnum.ALIAS_WAFER_NAME_SET_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //【step0】check input params
        ObjectIdentifier lotID = params.getLotID();
        List<Infos.AliasWaferName> aliasWaferNames = params.getAliasWaferNames();
        Validations.check(ObjectIdentifier.isEmpty(lotID) || ObjectUtils.isEmpty(aliasWaferNames), "the params is null");

        //【step1】get schedule from calendar

        //【step2】call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setLotIDLists(Arrays.asList(lotID));
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), accessControlCheckInqParams);

        //【step3】call sxWaferAliasSetReq(...)
        lotStartService.sxWaferAliasSetReq(objCommon, params);

        //【step4】judge whether the return code is success, if no, then TCC will rollback
        return Response.createSucc(transactionID, null);
    }

    @ResponseBody
    @RequestMapping(value = "/wafer_lot_start_cancel/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.STB_CANCEL_REQ)
    public Response waferLotStartCancelReq(@RequestBody Params.WaferLotStartCancelReqParams waferLotStartCancelReqParams) {
        //【step0】init params
        final String transactionID = TransactionIDEnum.STB_CANCEL_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //【step0】check input params

        //step1 - done: get schedule from calendar

        //【step2】call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        List<ObjectIdentifier> lotIDLists = new ArrayList<>();
        lotIDLists.add(waferLotStartCancelReqParams.getStbCancelledLotID());
        accessControlCheckInqParams.setLotIDLists(lotIDLists);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, waferLotStartCancelReqParams.getUser(), accessControlCheckInqParams);


        //step3 - call txWaferLotStartCancelReq(...)
        Results.WaferLotStartCancelReqResult result = lotStartService.sxWaferLotStartCancelReq(objCommon, waferLotStartCancelReqParams);

        return Response.createSucc(transactionID, result);
    }

    @ResponseBody
    @RequestMapping(value = "/stb_cancel_ex/req", method = RequestMethod.POST)
    @CimMapping(names = "OLSTW002_EX") //This interface does not have a TXID. Using STB_CANCEL_REQ TXID results in a duplicate TX, so it is changed to a unique TXID
    public Response waferLotStartCancelReqEx(@RequestBody Params.STBCancelExReqParams params) {
        //【step0】init params
        final String transactionID = TransactionIDEnum.STB_CANCEL_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);
        User user = params.getUser();

        //【step0】check input params

        //【step1】done: get schedule from calendar
        Infos.ObjCommon objCommon = utilsComp.setObjCommon(TransactionIDEnum.STB_CANCEL_REQ, user);

        //【step2】call WaferLotStartCancelInfoInq to get NewPreparedLotInfo list
        Params.WaferLotStartCancelInfoInqParams waferLotStartCancelInfoInqParams = new Params.WaferLotStartCancelInfoInqParams();
        waferLotStartCancelInfoInqParams.setUser(params.getUser());
        waferLotStartCancelInfoInqParams.setStbCancelledLotID(params.getStbCancelledLotID());
        Results.WaferLotStartCancelInfoInqResult waferLotStartCancelInfoInqResultRetCode = lotStartInqService.sxWaferLotStartCancelInfoInq(objCommon, waferLotStartCancelInfoInqParams);

        //【step3】call sxLotListByCarrierInq to get NewLotAttributes info
        ObjectIdentifier cassetteID = waferLotStartCancelInfoInqResultRetCode.getStbCancelledLotInfo().getCassetteID();
        Results.LotListByCarrierInqResult lotListByCarrierInqResultRetCode = lotInqService.sxLotListByCarrierInq(objCommon, cassetteID);

        Infos.NewLotAttributes newLotAttributes = new Infos.NewLotAttributes();
        List<Infos.NewWaferAttributes> newWaferAttributesList = new ArrayList<>();
        int waferSize = CimArrayUtils.getSize(lotListByCarrierInqResultRetCode.getWaferMapInCassetteInfoList());
        for (int i = 0; i < waferSize; i++) {
            Infos.WaferMapInCassetteInfo waferMapInCassetteInfo = lotListByCarrierInqResultRetCode.getWaferMapInCassetteInfoList().get(i);
            Infos.NewWaferAttributes newWaferAttributes = new Infos.NewWaferAttributes();
            newWaferAttributes.setSourceLotID(waferMapInCassetteInfo.getLotID());
            newWaferAttributes.setSourceWaferID(waferMapInCassetteInfo.getWaferID());
            newWaferAttributes.setNewSlotNumber(waferMapInCassetteInfo.getSlotNumber());
            newWaferAttributesList.add(newWaferAttributes);
        }
        newLotAttributes.setCassetteID(cassetteID);
        newLotAttributes.setNewWaferAttributesList(newWaferAttributesList);

        //【step4】call waferLotStartCancelReq()
        Params.WaferLotStartCancelReqParams waferLotStartCancelReqParams = new Params.WaferLotStartCancelReqParams();
        waferLotStartCancelReqParams.setUser(params.getUser());
        waferLotStartCancelReqParams.setStbCancelledLotID(params.getStbCancelledLotID());
        waferLotStartCancelReqParams.setNewPreparedLotInfoList(waferLotStartCancelInfoInqResultRetCode.getNewPreparedLotInfoList());
        waferLotStartCancelReqParams.setNewLotAttributes(newLotAttributes);
        Response response = this.waferLotStartCancelReq(waferLotStartCancelReqParams);
        response.setTransactionID(transactionID);

        //【step5】judge whether the return code is success, if no, then TCC will rollback
        Validations.isSuccessWithException(response);
        return response;
    }

    @ResponseBody
    @RequestMapping(value = "/old/wafer_lot_start/req", method = RequestMethod.POST)
    //@CimMapping(TransactionIDEnum.STB_RELEASED_LOT_REQ)
    public Response waferLotStartReqOld(@RequestBody Params.WaferLotStartReqParams waferLotStartReqParams) {
        if (CimStringUtils.isEmpty(waferLotStartReqParams.getProductRequestID().getValue()) || ObjectUtils.isEmpty(waferLotStartReqParams.getNewLotAttributes().getCassetteID())) {
            return Response.createError(retCodeConfig.getInvalidParameter(), TransactionIDEnum.WHATNEXT_LOT_LIST_INQ.getValue());
        }

        //【step0】init params
        final String transactionID = TransactionIDEnum.STB_RELEASED_LOT_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);
        User user = waferLotStartReqParams.getUser();

        //【step1】 call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        List<ObjectIdentifier> lotIDs = new ArrayList<>();
        for (Infos.NewWaferAttributes newWaferAttributes : waferLotStartReqParams.getNewLotAttributes().getNewWaferAttributesList()) {
            if (ObjectIdentifier.isEmpty(newWaferAttributes.getSourceLotID())) {
                return Response.createError(retCodeConfig.getInvalidParameter(), TransactionIDEnum.WHATNEXT_LOT_LIST_INQ.getValue());
            }
            lotIDs.add(newWaferAttributes.getSourceLotID());
        }
        accessControlCheckInqParams.setLotIDLists(lotIDs);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(TransactionIDEnum.STB_RELEASED_LOT_REQ.getValue(), user, accessControlCheckInqParams);

        //step2 - call sxWaferLotStartReq(...)
        ObjectIdentifier productRequestID = waferLotStartReqParams.getProductRequestID();
        Infos.NewLotAttributes newLotAttributes = waferLotStartReqParams.getNewLotAttributes();
        Results.WaferLotStartReqResult result = lotStartService.sxWaferLotStartReq(objCommon, productRequestID, newLotAttributes, waferLotStartReqParams.getClaimMemo());
        ObjectIdentifier lotID = result.getLotID();
        Response response = Response.createSucc(transactionID, result);


        //-----------------------------
        // Call postProcess
        //-----------------------------
        log.info("Call postProcess");
        Params.PostTaskRegisterReqParams postTaskRegisterReqParams = new Params.PostTaskRegisterReqParams(objCommon.getTransactionID(), null, null, -1,
                new Infos.PostProcessRegistrationParam(null, null, Arrays.asList(result.getLotID()), null), "");
        postController.execPostProcess(objCommon, postTaskRegisterReqParams);
        return response;
    }

    @Override
    @ResponseBody
    @RequestMapping(value = "/wafer_lot_start/req", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.STB_RELEASED_LOT_REQ)
    @EnablePostProcess
    public Response waferLotStartReq(@RequestBody Params.WaferLotStartReqParams waferLotStartReqParams) {
        Infos.NewLotAttributes newLotAttributes = waferLotStartReqParams.getNewLotAttributes();
        if (ObjectIdentifier.isEmpty(waferLotStartReqParams.getProductRequestID())
                || ObjectIdentifier.isEmpty(newLotAttributes.getCassetteID())) {
            Validations.check(retCodeConfig.getInvalidParameter());
        }

        //【step0】init params
        final String transactionID = TransactionIDEnum.STB_RELEASED_LOT_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);
        User user = waferLotStartReqParams.getUser();

        //【step1】 call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        List<ObjectIdentifier> lotIDs = new ArrayList<>();
        List<Infos.NewWaferAttributes> newWaferAttributesList = newLotAttributes.getNewWaferAttributesList();
        for (Infos.NewWaferAttributes newWaferAttributes :
                newWaferAttributesList) {
            if (ObjectIdentifier.isEmpty(newWaferAttributes.getSourceLotID())) {
                Validations.check(retCodeConfig.getInvalidParameter());
            }
            lotIDs.add(newWaferAttributes.getSourceLotID());
        }
        accessControlCheckInqParams.setLotIDLists(lotIDs);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID,
                user,
                accessControlCheckInqParams);

        //step2 - call sxWaferLotStartReq(...)
        Results.WaferLotStartReqResult result = lotStartService.sxWaferLotStartReq(objCommon,
                waferLotStartReqParams.getProductRequestID(),
                newLotAttributes,
                waferLotStartReqParams.getClaimMemo());

        return Response.createSucc(transactionID, result.getLotID());
    }
}
