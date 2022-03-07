package com.fa.cim.controller.apc;

import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.support.Response;
import com.fa.cim.common.utils.ThreadContextHolder;
import com.fa.cim.controller.interfaces.apc.IAPCInqController;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;
import com.fa.cim.middleware.standard.api.annotations.listener.CimMapping;
import com.fa.cim.middleware.standard.api.annotations.listener.Listenable;
import com.fa.cim.service.access.IAccessInqService;
import com.fa.cim.service.apc.IAPCInqService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/4/27          ********            lightyh                create file
 *
 * @author: light
 * @date: 2020/4/27 12:26
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@RestController
@RequestMapping("/apc")
@Listenable
public class APCInqController implements IAPCInqController {

    @Autowired
    private IAccessInqService accessInqService;

    @Autowired
    private IAPCInqService apcInqService;


    @ResponseBody
    @RequestMapping(value = "/runtime_capability/inq", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.APC_CAPABILITY_INQ)
    @Override
    public Response APCRunTimeCapabilityInq(@RequestBody Params.APCRunTimeCapabilityInqParams apcRunTimeCapabilityInqParams) {
        //init params
        final String transactionID = TransactionIDEnum.APC_CAPABILITY_INQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);
        //【step2】call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, apcRunTimeCapabilityInqParams.getUser(), accessControlCheckInqParams);
        //【step3】call txAPCCapabilityInq(...)
        List<Infos.APCRunTimeCapabilityResponse> apcRunTimeCapabilityResponses = apcInqService.sxAPCRunTimeCapabilityInq(objCommon, apcRunTimeCapabilityInqParams.getEquipmentID(), apcRunTimeCapabilityInqParams.getControlJobID(), apcRunTimeCapabilityInqParams.getStrStartCassette(), apcRunTimeCapabilityInqParams.isSendTxFlag());
        return Response.createSucc(transactionID, apcRunTimeCapabilityResponses);
    }


    @ResponseBody
    @RequestMapping(value = "/recipe_parameter_adjust/inq", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.APC_RECIPE_PARAMETER_ADJUST_INQ)
    @Override
    public Response APCRecipeParameterAdjustInq(@RequestBody Params.APCRecipeParameterAdjustInqParams apcRecipeParameterAdjustInqParams) {
        //init params
        final String transactionID = TransactionIDEnum.APC_RECIPE_PARAMETER_ADJUST_INQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);
        //【step2】call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, apcRecipeParameterAdjustInqParams.getUser(), accessControlCheckInqParams);
        //【step3】call txAPCRecipeParameterAdjustInq(...)
        List<Infos.StartCassette> startCassettes = apcInqService.sxAPCRecipeParameterAdjustInq(objCommon, apcRecipeParameterAdjustInqParams.getEquipmentID(), apcRecipeParameterAdjustInqParams.getStrStartCassette(), apcRecipeParameterAdjustInqParams.getStrAPCRunTimeCapabilityResponse(), apcRecipeParameterAdjustInqParams.isFinalBoolean());
        return Response.createSucc(transactionID, startCassettes);
    }

    @ResponseBody
    @RequestMapping(value = "/apc_interface_list/inq", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.APC_IF_LIST_INQ)
    @Override
    public Response APCInterfaceListInq(@RequestBody Params.APCIFListInqParams apcifListInqParams) {
        //init params
        final String transactionID = TransactionIDEnum.APC_IF_LIST_INQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);
        //【step2】call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, apcifListInqParams.getUser(), accessControlCheckInqParams);
        //【step3】call txAPCIFListInq(...)
        List<Infos.APCIf> apcIfList = apcInqService.sxAPCInterfaceListInq(objCommon, apcifListInqParams.getEquipmentID());
        return Response.createSucc(transactionID, apcIfList);
    }

    @ResponseBody
    @RequestMapping(value = "/apc_entity_list/inq", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.ENTITY_LIST_INQ)
    @Override
    public Response entityListInq(@RequestBody Params.EntityListInqParams entityListInqParams) {
        //init params
        final String transactionID = TransactionIDEnum.ENTITY_LIST_INQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);
        //【step2】call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, entityListInqParams.getUser(), accessControlCheckInqParams);
        //【step3】call txEntityListInq(...)
        Results.EntityListInqResult entityListInqResult = apcInqService.sxEntityListInq(objCommon, entityListInqParams.getEntityClass(), entityListInqParams.getSearchKeyName(), entityListInqParams.getSearchKeyValue(), entityListInqParams.getOption());
        return Response.createSucc(transactionID, entityListInqResult);
    }
}