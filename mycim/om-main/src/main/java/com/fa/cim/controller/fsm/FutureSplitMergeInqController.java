package com.fa.cim.controller.fsm;

import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.support.Response;
import com.fa.cim.common.support.User;
import com.fa.cim.common.utils.ThreadContextHolder;
import com.fa.cim.controller.interfaces.fsm.IFutureSplitMergeInqController;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.middleware.standard.api.annotations.listener.CimMapping;
import com.fa.cim.middleware.standard.api.annotations.listener.Listenable;
import com.fa.cim.service.access.IAccessInqService;
import com.fa.cim.service.fsm.IFutureSplitMergeInqService;
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
 * 2019/7/31          ********            Nyx                create file
 *
 * @author: Nyx
 * @date: 2019/7/31 16:05
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@RestController
@RequestMapping("/fsm")
@Listenable
public class FutureSplitMergeInqController implements IFutureSplitMergeInqController {

    @Autowired
    private IAccessInqService accessInqService;
    @Autowired
    private IFutureSplitMergeInqService futureSplitMergeInqService;

    @ResponseBody
    @RequestMapping(value = "/fsm_lot_info/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.FSM_LOT_INFO_INQ)
    public Response fsmLotInfoInq(@RequestBody com.fa.cim.fsm.Params.FSMLotInfoInqParams params) {


        //nitialize Parameters;
        final TransactionIDEnum transactionID = TransactionIDEnum.FSM_LOT_INFO_INQ;
        ThreadContextHolder.setTransactionId(transactionID.getValue());

        //check input params
        User user = params.getUser();
        //Incoming Log Put;
        log.info(" Incoming = {}", transactionID.getValue());

        //【Step-1】:txCalendar_GetCurrentTimeDR;
        log.debug("【Step-1】txCalendar_GetCurrentTimeDR(...)");

        //【Step-2】:txAccessControlCheckInq;
        log.debug("【Step-2】txAccessControlCheckInq(...)");
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID.getValue(), params.getUser(), accessControlCheckInqParams);


        //【step-3】:txPSMLotInfoInq;
        log.debug("【Step-3】txPSMLotInfoInq(...)");
        com.fa.cim.fsm.Results.FSMLotInfoInqResult result = futureSplitMergeInqService.sxFSMLotInfoInq(objCommon, params.getLotFamilyID(), params.getSplitRouteID(), params.getSplitOperationNumber(), params.getOriginalRouteID(), params.getOriginalOperationNumber());
        return Response.createSucc(transactionID.getValue(), result);
    }

    @ResponseBody
    @RequestMapping(value = "/fsm_lot_definition_list/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.FSM_LOT_DEFINITION_LIST_INQ)
    public Response fsmLotDefinitionListInq(@RequestBody com.fa.cim.fsm.Params.FSMLotDefinitionListInqParams params) {

        //【Step-0】:Initialize Parameters;
        final TransactionIDEnum transactionID = TransactionIDEnum.FSM_LOT_DEFINITION_LIST_INQ;
        ThreadContextHolder.setTransactionId(transactionID.getValue());

        //check input params

        //【Step-0】:Incoming Log Put;
        log.info(" Incoming = {}", transactionID.getValue());

        //【Step-1】:txCalendar_GetCurrentTimeDR;
        log.debug("【Step-1】txCalendar_GetCurrentTimeDR(...)");

        //【Step-2】:txAccessControlCheckInq;
        log.debug("【Step-2】txAccessControlCheckInq(...)");
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID.getValue(), params.getUser(), accessControlCheckInqParams);

        //【step-3】:txPSMLotDefinitionListInq__160;
        log.debug("【Step-3】txPSMLotDefinitionListInq__160;(...)");
        List<com.fa.cim.fsm.Infos.ExperimentalFutureLotInfo> result = futureSplitMergeInqService.sxFSMLotDefinitionListInq(objCommon, params.getLotFamilyID(), params.getDetailRequireFlag());
        return Response.createSucc(transactionID.getValue(), result);
    }

}