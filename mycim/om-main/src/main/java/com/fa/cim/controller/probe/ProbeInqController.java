package com.fa.cim.controller.probe;

import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.support.Response;
import com.fa.cim.common.support.User;
import com.fa.cim.common.utils.ThreadContextHolder;
import com.fa.cim.controller.interfaces.probe.IProbeInqController;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;
import com.fa.cim.middleware.standard.api.annotations.listener.CimMapping;
import com.fa.cim.middleware.standard.api.annotations.listener.Listenable;
import com.fa.cim.service.access.IAccessInqService;
import com.fa.cim.service.probe.IProbeInqService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/11/5          ********            Jerry_Huang                create file
 *
 * @author Jerry_Huang
 * @since 2020/11/5 10:17
 * Copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@RestController
@RequestMapping("/probe")
@Listenable
public class ProbeInqController implements IProbeInqController {

    @Autowired
    private IAccessInqService accessInqService;

    @Autowired
    private IProbeInqService probeInqService;

    @ResponseBody
    @RequestMapping(value = "/probe_list/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.PROBE_LIST_INQ)
    public Response probeListInq(@RequestBody Params.ProbeListInqParams probeListInqParams) {
        //init params
        final String transactionID = TransactionIDEnum.PROBE_LIST_INQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);
        User user = probeListInqParams.getUser();

        // check privilege
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon  objCommon  = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, user, accessControlCheckInqParams);

        //【step0】
        log.debug("call probeListInq service");
        Results.FixtureListInqResult strResult =  probeInqService.sxProbeListInq(objCommon, probeListInqParams);
        return Response.createSucc(transactionID, strResult);
    }

    @ResponseBody
    @RequestMapping(value = "/probeID_list/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.PROBE_ID_LIST_INQ)
    public Response probeIDListInq(@RequestBody Params.ProbeIDListInqParams probeIDListInqParams) {
        //init params
        final String transactionID = TransactionIDEnum.PROBE_ID_LIST_INQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);
        User user = probeIDListInqParams.getUser();

        // check privilege
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon  objCommon  = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, user, accessControlCheckInqParams);

        //【step0】
        log.debug("call probeIDListInq service");
        Results.FixtureIDListInqResult strResult =  probeInqService.sxProbeIDListInq(objCommon, probeIDListInqParams);
        return Response.createSucc(transactionID, strResult);
    }

    @ResponseBody
    @RequestMapping(value = "/probe_status/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.PROBE_STATUS_INQ)
    public Response probeStatusInq(@RequestBody Params.ProbeStatusParams probeStatusParams) {
        //init params
        final String transactionID = TransactionIDEnum.PROBE_STATUS_INQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);
        User user = probeStatusParams.getUser();

        // check privilege
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon  objCommon  = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, user, accessControlCheckInqParams);

        //【step0】
        log.debug("call probeStatusInq service");
        Results.FixtureStatusInqResult strResult = probeInqService.sxProbeStatusInq(objCommon, probeStatusParams.getFixtureID());
        return Response.createSucc(transactionID, strResult);
    }

    @ResponseBody
    @RequestMapping(value = "/probe_stocker_info_inq/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.PROBE_STOCKER_INFO_INQ)
    public Response probeStockerInfoInq(@RequestBody Params.ProbeStockerInfoInq probeStockerInfoInq) {
        //init params
        final String transactionID = TransactionIDEnum.PROBE_STOCKER_INFO_INQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);
        User user = probeStockerInfoInq.getUser();

        // check privilege
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon  objCommon  = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, user, accessControlCheckInqParams);

        //【step0】
        log.debug("call probeStockerInfoInq service");
        Results.FixtureStockerInfoInqResult strResult = probeInqService.sxProbeStockerInfoInq(objCommon, probeStockerInfoInq.getStockerID());

        return Response.createSucc(transactionID, strResult);
    }

    @ResponseBody
    @RequestMapping(value = "/probe_group_id_list/inq", method = RequestMethod.POST)
    @Override
    @CimMapping(TransactionIDEnum.PROBE_GROUP_ID_LIST_INQ)
    public Response probeGroupIDListInq(@RequestBody Params.probeGroupIdListParams probeGroupIdListParams) {
        //init params
        final String transactionID = TransactionIDEnum.PROBE_GROUP_ID_LIST_INQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);
        User user = probeGroupIdListParams.getUser();

        // check privilege
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon  objCommon  = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, user, accessControlCheckInqParams);

        //【step0】
        log.debug("call probeGroupIDListInq service");
        Results.FixtureGroupIDListInqResult strResult = probeInqService.sxProbeGroupIDListInq(objCommon);
        return Response.createSucc(transactionID, strResult);
    }

}
