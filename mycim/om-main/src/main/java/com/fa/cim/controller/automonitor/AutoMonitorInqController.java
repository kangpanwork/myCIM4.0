package com.fa.cim.controller.automonitor;

import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.Response;
import com.fa.cim.common.utils.ThreadContextHolder;
import com.fa.cim.controller.interfaces.autoMonitor.IAutoMonitorInqController;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;
import com.fa.cim.dto.am.AllRecipeByProductSpecificationInqParam;
import com.fa.cim.middleware.standard.api.annotations.listener.CimMapping;
import com.fa.cim.middleware.standard.api.annotations.listener.Listenable;
import com.fa.cim.service.access.IAccessInqService;
import com.fa.cim.service.automonitor.IAutoMonitorInqService;
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
 * 2019/11/13      ********             Neko                create file
 *
 * @author Neko
 * @since 2019/11/13 18:00
 * Copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@RestController
@RequestMapping("/amon")
@Listenable
public class AutoMonitorInqController implements IAutoMonitorInqController {
    @Autowired
    private IAccessInqService accessInqService;

    @Autowired
    private IAutoMonitorInqService autoMonitorInqService;

    @ResponseBody
    @RequestMapping(value = "/am_job_list/inq", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.EQP_MONITOR_JOB_LIST_INQ)
    @Override
    public Response amJobListInq(@RequestBody Params.AMJobListInqParams params) {
        String txId = TransactionIDEnum.EQP_MONITOR_JOB_LIST_INQ.getValue();
        //set current tx id
        ThreadContextHolder.setTransactionId(txId);

        //step2 -  call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setEquipmentID(params.getEquipmentID());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, params.getUser(), accessControlCheckInqParams);

        /*-------------------------------*/
        /*   Main Process                */
        /*-------------------------------*/
        Results.AMJobListInqResult tmpResult = autoMonitorInqService.sxAMJobListInq(objCommon, params);

        log.info("AMJobListInq is success");
        return Response.createSucc(txId, tmpResult);
    }

    @ResponseBody
    @RequestMapping(value = "/am_list/inq", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.EQP_MONITOR_LIST_INQ)
    @Override
    public Response amListInq(@RequestBody Params.AMListInqParams monitorListInqParams) {
        String txId = TransactionIDEnum.EQP_MONITOR_LIST_INQ.getValue();
        //set current tx id
        ThreadContextHolder.setTransactionId(txId);

        //step2 -  call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setEquipmentID(monitorListInqParams.getEquipmentID());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, monitorListInqParams.getUser(), accessControlCheckInqParams);
        /*-------------------------------*/
        /*   Main Process                */
        /*-------------------------------*/
        List<Infos.EqpMonitorDetailInfo> tmpResult = autoMonitorInqService.sxAMListInq(objCommon, monitorListInqParams);

        log.info("AMListInq is success");
        return Response.createSucc(txId, tmpResult);
    }

    @ResponseBody
    @RequestMapping(value = "/what_next_am_lot/inq", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.WHAT_NEXT_EQP_MONITOR_LOT_LIST_INQ)
    @Override
    public Response whatNextAMLotInq(@RequestBody Params.WhatNextAMLotInqInParm parm) {
        String txId = TransactionIDEnum.WHAT_NEXT_EQP_MONITOR_LOT_LIST_INQ.getValue();
        //set current tx id
        ThreadContextHolder.setTransactionId(txId);

        //step2 - call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        accessControlCheckInqParams.setEquipmentID(parm.getEquipmentID());
        accessControlCheckInqParams.setStockerID(new ObjectIdentifier(""));
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, parm.getUser(), accessControlCheckInqParams);

        //step3 - txWhatNextAMLotInq
        Results.WhatNextAMLotInqResult result = autoMonitorInqService.sxWhatNextAMLotInq(objCommon, parm);
        return Response.createSucc(objCommon.getTransactionID(), result);
    }

    @ResponseBody
    @RequestMapping(value = "/all_recipe_by_productspecification/inq", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.ALL_RECIPE_BY_PRODUCTSPECIFICATION_INQ)
    @Override
    public Response allRecipeByProductSpecificationInq(@RequestBody AllRecipeByProductSpecificationInqParam params) {
        //init params
        final String transactionID = TransactionIDEnum.ALL_RECIPE_BY_PRODUCTSPECIFICATION_INQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //check input params
        //【step1】get schedule from calendar
        //【step2】call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, params.getUser(), accessControlCheckInqParams);

        //【step3】call sxWaferScrappedHistoryInq(...)
        List<Infos.DefaultRecipeSetting> result = autoMonitorInqService.sxAllRecipeByProductSpecificationInq(objCommon,params);
        return Response.createSucc(transactionID, result);
    }

}
