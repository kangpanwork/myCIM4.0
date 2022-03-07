package com.fa.cim.controller.psm;

import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.support.Response;
import com.fa.cim.common.support.User;
import com.fa.cim.common.utils.ThreadContextHolder;
import com.fa.cim.controller.interfaces.plannedSplitMerge.IPlannedSplitMergeInqController;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;
import com.fa.cim.middleware.standard.api.annotations.listener.CimMapping;
import com.fa.cim.middleware.standard.api.annotations.listener.Listenable;
import com.fa.cim.service.access.IAccessInqService;
import com.fa.cim.service.psm.IPlannedSplitMergeInqService;
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
@RequestMapping("/psm")
@Listenable
public class PlannedSplitMergeInqController implements IPlannedSplitMergeInqController {

    @Autowired
    private IAccessInqService accessInqService;
    @Autowired
    private IPlannedSplitMergeInqService plannedSplitMergeInqService;

    @ResponseBody
    @RequestMapping(value = "/psm_lot_info/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.EXPERIMENTAL_LOT_INFO_INQ)
    public Response psmLotInfoInq(@RequestBody Params.PSMLotInfoInqParams params) {


        //nitialize Parameters;
        final TransactionIDEnum transactionID = TransactionIDEnum.EXPERIMENTAL_LOT_INFO_INQ;
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
        Results.PSMLotInfoInqResult result = plannedSplitMergeInqService.sxPSMLotInfoInq(objCommon, params.getLotFamilyID(), params.getSplitRouteID(), params.getSplitOperationNumber(), params.getOriginalRouteID(), params.getOriginalOperationNumber());
        return Response.createSucc(transactionID.getValue(), result);
    }

    @ResponseBody
    @RequestMapping(value = "/psm_lot_definition_list/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.EXPERIMENTAL_LOT_LIST_INQ)
    public Response psmLotDefinitionListInq(@RequestBody Params.PSMLotDefinitionListInqParams params) {

        //【Step-0】:Initialize Parameters;
        final TransactionIDEnum transactionID = TransactionIDEnum.EXPERIMENTAL_LOT_LIST_INQ;
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
        List<Infos.ExperimentalLotInfo> result = plannedSplitMergeInqService.sxPSMLotDefinitionListInq(objCommon, params.getLotFamilyID(), params.getDetailRequireFlag());
        return Response.createSucc(transactionID.getValue(), result);
    }

}