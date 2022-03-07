package com.fa.cim.tms.controller;

import com.alibaba.fastjson.JSON;
import com.fa.cim.tms.common.enums.TransactionIDEnum;
import com.fa.cim.tms.controller.interfaces.ITransportJobPauseReqController;
import com.fa.cim.tms.dto.Params;
import com.fa.cim.tms.dto.Response;
import com.fa.cim.tms.dto.Results;
import com.fa.cim.tms.dto.User;
import com.fa.cim.tms.method.IUtilsComp;
import com.fa.cim.tms.pojo.Infos;
import com.fa.cim.tms.service.ITransportJobPauseReqService;
import com.fa.cim.tms.utils.ArrayUtils;
import com.fa.cim.tms.utils.ThreadContextHolder;
import com.fa.cim.tms.utils.Validations;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
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
 * 2020/10/20                               Neyo                create file
 *
 * @author: Neyo
 * @date: 2020/10/20 15:46
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@RestController
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class TransportJobPauseReqController implements ITransportJobPauseReqController {
    @Autowired
    private IUtilsComp utilsComp;
    @Autowired
    private ITransportJobPauseReqService transportJobPauseReqService;

    @RequestMapping(value = "/tms/transport_job_pause/req", method = RequestMethod.POST)
    public Response tmsTransportJobPauseReq(@RequestBody Params.TransportJobPauseReqParams transportJobPauseReqParams) {
        log.info("tmsTransportJobPauseReq Request Json" + JSON.toJSONString(transportJobPauseReqParams));
        Results.TransportJobPauseReqResult result = new Results.TransportJobPauseReqResult();
        final String transactionID = TransactionIDEnum.OM03.getValue();
        ThreadContextHolder.setTransactionId(transactionID);
        User user = transportJobPauseReqParams.getRequestUserID();
        Validations.check(null == user, "the user info is null...");
        List<Infos.CarrierJob> carrierJobData = transportJobPauseReqParams.getCarrierJobData();
        Validations.check(ArrayUtils.isEmpty(carrierJobData), "the carrierJobData info is null...");
        Infos.ObjCommon objCommon = utilsComp.setObjCommon(TransactionIDEnum.OM03, user);

        log.info("【step1】 - transportJobPauseReqService.sxTransportJobPauseReq");
        result = transportJobPauseReqService.sxTransportJobPauseReq(objCommon, transportJobPauseReqParams);
        log.info("tmsTransportJobPauseReq Response json" + JSON.toJSONString(result));
        return Response.createSucc(result);
    }

    @RequestMapping(value = "/rtms/transport_job_pause/req", method = RequestMethod.POST)
    public Response rtmsTransportJobPauseReq(@RequestBody Params.TransportJobPauseReqParams transportJobPauseReqParams) {
        log.info("rtmsTransportJobPauseReq Request Json" + JSON.toJSONString(transportJobPauseReqParams));
        Results.TransportJobPauseReqResult result = new Results.TransportJobPauseReqResult();
        final String transactionID = TransactionIDEnum.ROM03.getValue();
        ThreadContextHolder.setTransactionId(transactionID);
        User user = transportJobPauseReqParams.getRequestUserID();
        Validations.check(null == user, "the user info is null...");
        List<Infos.CarrierJob> carrierJobData = transportJobPauseReqParams.getCarrierJobData();
        Validations.check(ArrayUtils.isEmpty(carrierJobData), "the carrierJobData info is null...");
        Infos.ObjCommon objCommon = utilsComp.setObjCommon(TransactionIDEnum.ROM03, user);

        log.info("【step1】 - transportJobPauseReqService.sxRtmsTransportJobPauseReq");
        result = transportJobPauseReqService.sxRtmsTransportJobPauseReq(objCommon, transportJobPauseReqParams);
        log.info("rtmsTransportJobPauseReq Response json" + JSON.toJSONString(result));
        return Response.createSucc(result);
    }
}
