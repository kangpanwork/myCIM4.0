package com.fa.cim.controller.arhs;

import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.support.Response;
import com.fa.cim.common.support.User;
import com.fa.cim.common.utils.ThreadContextHolder;
import com.fa.cim.controller.interfaces.arhs.IArhsInqController;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;
import com.fa.cim.middleware.standard.api.annotations.listener.CimMapping;
import com.fa.cim.middleware.standard.api.annotations.listener.Listenable;
import com.fa.cim.service.access.IAccessInqService;
import com.fa.cim.service.arhs.IArhsInqService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * description:
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/11/3                              Neyo                create file
 *
 * @author: Neyo
 * @date: 2020/11/3 16:18
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@RestController
@RequestMapping("/arhs")
@Listenable
public class ArhsInqController implements IArhsInqController {

    @Autowired
    private IAccessInqService accessInqService;
    @Autowired
    private IArhsInqService arhsInqService;

    @RequestMapping(value = "/what_reticle_action_list/inq", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.WHAT_RETICLE_ACTION_LIST_INQ)
    @Override
    public Response whatReticleActionListInq(@RequestBody Params.WhatReticleActionListInqParams params) {
        String txId = TransactionIDEnum.WHAT_RETICLE_ACTION_LIST_INQ.getValue();

        ThreadContextHolder.setTransactionId(txId);

        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, params.getUser(), accessControlCheckInqParams);

        Results.WhatReticleActionListInqResult result = arhsInqService.sxWhatReticleActionListInq(objCommon, params);
        return Response.createSucc(txId, result);
    }

    @RequestMapping(value = "/what_reticle_retrieve/inq", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.WHAT_RETICLE_RETRIEVE_INQ)
    @Override
    public Response whatReticleRetrieveInq(@RequestBody Params.WhatReticleRetrieveInqParams params) {
        String txId = TransactionIDEnum.WHAT_RETICLE_RETRIEVE_INQ.getValue();

        ThreadContextHolder.setTransactionId(txId);

        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        accessControlCheckInqParams.setEquipmentID(params.getEquipmentID());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, params.getUser(), accessControlCheckInqParams);

        List<Infos.StoredReticle> result = arhsInqService.sxWhatReticleRetrieveInq(objCommon, params);
        return Response.createSucc(txId, result);
    }


    @RequestMapping(value = "/where_next_for_rpod/inq", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.WHERE_NEXT_FOR_RETICLE_POD_INQ)
    @Override
    public Response whereNextForRpodInq(@RequestBody Params.WhereNextForReticlePodInqParams params) {
        String txId = TransactionIDEnum.WHERE_NEXT_FOR_RETICLE_POD_INQ.getValue();
        ThreadContextHolder.setTransactionId(txId);

        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, params.getUser(), accessControlCheckInqParams);

        Results.WhereNextForReticlePodInqResult result = arhsInqService.sxWhereNextForReticlePodInq(objCommon, params);
        return Response.createSucc(txId, result);
    }

    @RequestMapping(value = "/reticle_compt_job_list/inq", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.RETICLE_COMPONENT_JOB_LIST_INQ)
    @Override
    public Response reticleComptJobListInq(@RequestBody Params.ReticleComponentJobListInqParams params) {
        String txId = TransactionIDEnum.RETICLE_COMPONENT_JOB_LIST_INQ.getValue();
        ThreadContextHolder.setTransactionId(txId);

        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, params.getUser(), accessControlCheckInqParams);

        List<Infos.ReticleComponentJob> result = arhsInqService.sxReticleComponentJobListInq(objCommon, params);
        return Response.createSucc(txId, result);
    }

    @RequestMapping(value = "/reticle_dispatch_job_list/inq", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.RETICLE_DISPATCH_JOB_LIST_INQ)
    @Override
    public Response reticleDispatchJobListInq(@RequestBody User params) {
        String txId = TransactionIDEnum.RETICLE_DISPATCH_JOB_LIST_INQ.getValue();
        ThreadContextHolder.setTransactionId(txId);

        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, params, accessControlCheckInqParams);

        List<Infos.ReticleDispatchJob> result = arhsInqService.sxReticleDispatchJobListInq(objCommon);
        return Response.createSucc(txId, result);
    }

    @RequestMapping(value = "/rpod_xfer_job_list/inq", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.RETICLE_POD_XFER_JOB_LIST_INQ)
    @Override
    public Response rpodXferJobListInq(@RequestBody Params.ReticlePodXferJobListInqParams params) {
        String txId = TransactionIDEnum.RETICLE_POD_XFER_JOB_LIST_INQ.getValue();
        ThreadContextHolder.setTransactionId(txId);

        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        accessControlCheckInqParams.setEquipmentID(params.getFromMachineID());
        accessControlCheckInqParams.setStockerID(params.getFromStockerID());
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, params.getUser(), accessControlCheckInqParams);

        Results.ReticlePodXferJobListInqResult result = arhsInqService.sxReticlePodXferJobListInq(objCommon, params);
        return Response.createSucc(txId, result);
    }

    @RequestMapping(value = "/what_rpod_for_reticle_xfer/inq", method = RequestMethod.POST)
    @CimMapping(TransactionIDEnum.WHAT_RETICLE_POD_FOR_RETICLE_XFER_INQ)
    @Override
    public Response whatRpodForReticleXferInq(@RequestBody Params.WhatReticlePodForReticleXferInqParams params) {
        String txId = TransactionIDEnum.WHAT_RETICLE_POD_FOR_RETICLE_XFER_INQ.getValue();
        ThreadContextHolder.setTransactionId(txId);

        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(txId, params.getUser(), new Params.AccessControlCheckInqParams(true));
        return Response.createSucc(txId, arhsInqService.sxWhatReticlePodForReticleXferInq(objCommon, params.getReticleID()));
    }
}
