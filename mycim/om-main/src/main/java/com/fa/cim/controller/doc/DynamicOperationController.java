package com.fa.cim.controller.doc;

import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.Response;
import com.fa.cim.common.utils.CimObjectUtils;
import com.fa.cim.common.utils.CimStringUtils;
import com.fa.cim.common.utils.ThreadContextHolder;
import com.fa.cim.common.utils.Validations;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.controller.interfaces.dynamicOperationControl.IDynamicOperationController;
import com.fa.cim.controller.post.PostController;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;
import com.fa.cim.method.ILotFamilyMethod;
import com.fa.cim.method.ILotMethod;
import com.fa.cim.method.IUtilsComp;
import com.fa.cim.middleware.standard.api.annotations.listener.CimMapping;
import com.fa.cim.middleware.standard.api.annotations.listener.Listenable;
import com.fa.cim.newcore.impl.bo.env.StandardProperties;
import com.fa.cim.service.access.IAccessInqService;
import com.fa.cim.service.doc.IDynamicOperationService;
import com.fa.cim.service.post.IPostService;
import lombok.extern.slf4j.Slf4j;
import org.bytesoft.compensable.Compensable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

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
 * @date: 2019/7/29 16:41
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@RestController
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
@Compensable(interfaceClass = IDynamicOperationController.class, confirmableKey = "DynamicOperationControlConfirm", cancellableKey = "DynamicOperationControlCancel")
@RequestMapping("/doc")
@Listenable
public class DynamicOperationController implements IDynamicOperationController {

    @Autowired
    private RetCodeConfig retCodeConfig;

    @Autowired
    private IUtilsComp utilsComp;

    @Autowired
    private IAccessInqService accessInqService;

    @Autowired
    private ILotMethod lotMethod;

    @Autowired
    private ILotFamilyMethod lotFamilyMethod;

    @Autowired
    private IPostService postService;

    @Autowired
    private PostController postController;

    @Autowired
    private IDynamicOperationService dynamicOperationService;

    @ResponseBody
    @RequestMapping(value = "/doc_lot_remove/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.FPCDELETE_REQ)
    public Response docLotRemoveReq(@RequestBody Params.DOCLotRemoveReqParams params) {
        //【step0】init params
        final String transactionID = TransactionIDEnum.FPCDELETE_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //【step1】calendar_GetCurrentTimeDR
        log.info("【step1】calendar_GetCurrentTimeDR");
        Infos.ObjCommon objCommon = utilsComp.setObjCommon(transactionID, params.getUser());

        //【step2】Get lotIDs from lotFamily
        log.info("【step2】Get lotIDs from lotFamily");
        List<ObjectIdentifier> lotIDs = lotFamilyMethod.lotFamilyAllLotsGetDR(objCommon, params.getLotFamilyID());
        if (!CimObjectUtils.isEmpty(lotIDs)) {
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
            }
        }

        //【step3】txAccessControlCheckInq
        log.info("【step3】 txAccessControlCheckInq");
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        accessInqService.sxAccessControlCheckInq(objCommon, accessControlCheckInqParams);

        //【step4】txDOCLotRemoveReq
        log.info("【step4】 txDOCLotRemoveReq");
        Results.DOCLotRemoveReqResult result = dynamicOperationService.sxDOCLotRemoveReq(objCommon, params);
        return Response.createSucc(transactionID, result);
    }

    @ResponseBody
    @RequestMapping(value = "/doc_lot_action/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.FPC_EXEC_REQ)
    public Response docLotActionReq(@RequestBody Params.DOCLotActionReqParams params) {
        // init params
        final String transactionID = TransactionIDEnum.FPC_EXEC_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //Step1 - txAccessControlCheckInq
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        accessControlCheckInqParams.getLotIDLists().add(params.getLotID());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), accessControlCheckInqParams);

        log.info("【step3】call sxDOCLotActionReq");
        dynamicOperationService.sxDOCLotActionReq(objCommon, params.getLotID());
        return Response.createSucc(transactionID, null);
    }

    @ResponseBody
    @RequestMapping(value = "/doc_lot_info_set/req", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.FPC_UPDATE_REQ)
    public Response docLotInfoSetReq(@RequestBody Params.DOCLotInfoSetReqParams params) {
        final String transactionID = TransactionIDEnum.FPC_UPDATE_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        List<Infos.FPCInfoAction> strFPCInfoActionList = params.getStrFPCInfoActionList();
        Validations.check(CimObjectUtils.isEmpty(strFPCInfoActionList), "FPCInfoActionList is null...");

        log.info("【step1】get schedule from calendar");

        log.info("【step2】call txAccessControlCheckInq(...)");
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), accessControlCheckInqParams);

        log.info("【step3】call sxDOCLotActionReq");
        Results.DOCLotInfoSetReqResult object = dynamicOperationService.sxDOCLotInfoSetReq(objCommon, strFPCInfoActionList, params.getClaimMemo(), params.getRunCardID());
        Response response = Response.createSucc(transactionID, object);

        List<String> fpc_iDs = object.getFPC_IDs();
        List<ObjectIdentifier> lotIDs = object.getLotIDs();
        //------------------------------
        // TODO: Reset out event parameter
        //------------------------------

        //-----------------------------
        // Resister post action.
        //-----------------------------
        log.info("Resister post action.");
        Results.PostTaskRegisterReqResult postTaskRegisterReqResult = null;
        if (!CimObjectUtils.isEmpty(lotIDs)) {
            int FPCAdaptationFlag = StandardProperties.OM_DOC_ENABLE_FLAG.getIntValue();
            if (1 == FPCAdaptationFlag) {
                Infos.PostProcessRegistrationParam postProcessRegistrationParam = new Infos.PostProcessRegistrationParam(null, null, lotIDs, null);
                postTaskRegisterReqResult = postService.sxPostTaskRegisterReq(objCommon,
                        new Params.PostTaskRegisterReqParams(objCommon.getTransactionID(), null, null, -1, postProcessRegistrationParam, ""));
            }
        }
        //-------------------------------------
        // Post-Processing Execution Section
        //-------------------------------------
        log.info("Post-Processing Execution Section");
        if (postTaskRegisterReqResult != null) {
            postController.postTaskExecuteReq(
                    new Params.PostTaskExecuteReqParams(params.getUser(), postTaskRegisterReqResult.getDKey(), 1, 0, null, ""));
        }

        return response;
    }

    @ResponseBody
    @RequestMapping(value = "/rc_doc_lot_info_set/req", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.RC_DOC_ADD_REQ)
    public Response runCardDOCLotInfoSetReq(@RequestBody Params.DOCLotInfoSetReqParams params) {
        return docLotInfoSetReq(params);
    }

    @ResponseBody
    @RequestMapping(value = "/rc_doc_lot_remove/req", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.RC_DOC_DELETE_REQ)
    public Response runCardDOCLotRemoveReq(@RequestBody Params.DOCLotRemoveReqParams params) {
        return docLotRemoveReq(params);
    }
}