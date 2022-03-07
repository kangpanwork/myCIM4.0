package com.fa.cim.controller.doc;

import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.Response;
import com.fa.cim.common.utils.ThreadContextHolder;
import com.fa.cim.controller.interfaces.dynamicOperationControl.IDynamicOperationInqController;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;
import com.fa.cim.middleware.standard.api.annotations.listener.CimMapping;
import com.fa.cim.middleware.standard.api.annotations.listener.Listenable;
import com.fa.cim.service.access.IAccessInqService;
import com.fa.cim.service.doc.IDynamicOperationInqService;
import com.fa.cim.service.recipe.IRecipeInqService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Collections;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/7/30          ********            Nyx                create file
 *
 * @author: Nyx
 * @date: 2019/7/30 15:37
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@RestController
@RequestMapping("/doc")
@Listenable
public class DynamicOperationInqController implements IDynamicOperationInqController {

    @Autowired
    private IAccessInqService accessInqService;

    @Autowired
    private IRecipeInqService recipeInqService;

    @Autowired
    private IDynamicOperationInqService dynamicOperationInqService;


    @ResponseBody
    @RequestMapping(value = "/doc_lot_info/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.FPCDETAIL_INFO_INQ)
    public Response DOCLotInfoInq(@RequestBody Params.DOCLotInfoInqParams params) {
        //【step0】init params
        final String transactionID = TransactionIDEnum.FPCDETAIL_INFO_INQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        ObjectIdentifier orgMainPDID = params.getOrgMainPDID();
        accessControlCheckInqParams.getLotIDLists().add(params.getLotID());
        accessControlCheckInqParams.setRouteIDList(Collections.singletonList(ObjectIdentifier.isEmpty(orgMainPDID) ? params.getMainPDID() : orgMainPDID));
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), accessControlCheckInqParams);

        //【step3】call sxBankInCancelReq(...)
        Results.DOCLotInfoInqResult result = dynamicOperationInqService.sxDOCLotInfoInq(objCommon, params);
        return Response.createSucc(transactionID, result);
    }

    @ResponseBody
    @RequestMapping(value = "/doc_step_list_in_process_flow/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.FPCPROCESS_LIST_IN_ROUTE_INQ)
    public Response DOCStepListInProcessFlowInq(@RequestBody Params.DOCStepListInProcessFlowInqParams params) {
        //【step0】init params
        final String transactionID = TransactionIDEnum.FPCPROCESS_LIST_IN_ROUTE_INQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //【step2】call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        accessControlCheckInqParams.getLotIDLists().add(params.getLotID());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), accessControlCheckInqParams);

        //【step3】call sxBankInCancelReq(...)
        Results.DOCStepListInProcessFlowInqResult result = dynamicOperationInqService.sxDOCStepListInProcessFlowInq(objCommon, params.getLotID());
        return Response.createSucc(transactionID, result);
    }

    @ResponseBody
    @RequestMapping(value = "/recipe_id_list_for_doc/inq")
    @Override
    @CimMapping(TransactionIDEnum.MACHINE_RECIPE_LIST_FOR_FPC_INQ)
    public Response recipeIdListForDOCInq(@RequestBody Params.RecipeIdListForDOCInqParams params) {
        /*-----------------------------------------------------------------------*/
        /*   Pre Process                                                         */
        /*-----------------------------------------------------------------------*/
        final String transactionID = TransactionIDEnum.MACHINE_RECIPE_LIST_FOR_FPC_INQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        // 【Step2】  txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        accessControlCheckInqParams.getLotIDLists().add(params.getStrRecipeIdListForDOCInqInParm().getLotID());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), accessControlCheckInqParams);

        /*-----------------------------------------------------------------------*/
        /*   Main Process                                                        */
        /*-----------------------------------------------------------------------*/
        // 【Step3】  txRecipeIdListForDOCInq(...)
        Results.RecipeIdListForDOCInqResult result = recipeInqService.sxRecipeIdListForDOCInq(objCommon, params);
        return Response.createSucc(transactionID, result);

    }

    @ResponseBody
    @RequestMapping(value = "/process_flow_ope_list_with_nest/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.ROUTE_OPERATION_NEST_LIST_INQ)
    public Response processFlowOpeListWithNestInq(@RequestBody Params.ProcessFlowOpeListWithNestInqParam param) {
        final String txId = TransactionIDEnum.ROUTE_OPERATION_NEST_LIST_INQ.getValue();
        ThreadContextHolder.setTransactionId(txId);


        //step2 - txAccessControlCheckInq
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, param.getUser(), accessControlCheckInqParams);

        //step3 - txProcessFlowOpeListWithNestInq
        Infos.RouteInfo result = dynamicOperationInqService.sxProcessFlowOpeListWithNestInq(objCommon, param);

        return Response.createSucc(txId, result);
    }

    @ResponseBody
    @RequestMapping(value = "/copy_from/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.RC_COPY_FROM_INQ)
    public Response copyFromInq(@RequestBody Params.CopyFromInqParams params) {
        final String txId = TransactionIDEnum.RC_COPY_FROM_INQ.getValue();
        ThreadContextHolder.setTransactionId(txId);

        //step2 - txAccessControlCheckInq
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, params.getUser(), new Params.AccessControlCheckInqParams(true));

        //step3 - txProcessFlowOpeListWithNestInq
        return Response.createSucc(txId, dynamicOperationInqService.sxCopyFromInq(objCommon, params.getFpcInfo(), params.getProductID(), params.getLotID()));
    }
}