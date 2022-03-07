package com.fa.cim.tms.controller;

import com.alibaba.fastjson.JSON;
import com.fa.cim.tms.common.enums.TransactionIDEnum;
import com.fa.cim.tms.controller.interfaces.ITransportJobResumeReqController;
import com.fa.cim.tms.dto.Params;
import com.fa.cim.tms.dto.Response;
import com.fa.cim.tms.dto.Results;
import com.fa.cim.tms.dto.User;
import com.fa.cim.tms.method.IUtilsComp;
import com.fa.cim.tms.pojo.Infos;
import com.fa.cim.tms.service.ITransportJobResumeReqService;
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
 * 2020/10/21                               Neyo                create file
 *
 * @author: Neyo
 * @date: 2020/10/21 13:37
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@RestController
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class TransportJobResumeReqController implements ITransportJobResumeReqController {
    @Autowired
    private IUtilsComp utilsComp;
    @Autowired
    private ITransportJobResumeReqService transportJobResumeReqService;

    @RequestMapping(value = "/tms/transport_job_resume/req", method = RequestMethod.POST)
    public Response tmsTransportJobResumeReq(@RequestBody Params.TransportJobResumeReqParams transportJobResumeReqParams) {
        log.info("tmsTransportJobResumeReq Request Json" + JSON.toJSONString(transportJobResumeReqParams));
        Results.TransportJobResumeReqResult result = new Results.TransportJobResumeReqResult();
        final String transactionID = TransactionIDEnum.OM05.getValue();
        ThreadContextHolder.setTransactionId(transactionID);
        User user = transportJobResumeReqParams.getRequestUserID();
        Validations.check(null == user, "the user info is null...");
        List<Infos.CarrierJob> amhsJobCreateArray = transportJobResumeReqParams.getCarrierJobData();
        Validations.check(ArrayUtils.isEmpty(amhsJobCreateArray), "the amhsJobCreateArray info is null...");
        Infos.ObjCommon objCommon = utilsComp.setObjCommon(transactionID, user);

        log.info("【step1】 - transportJobResumeReqService.sxTransportJobResumeReq");
        result = transportJobResumeReqService.sxTransportJobResumeReq(objCommon, transportJobResumeReqParams);
        log.info("tmsTransportJobResumeReq Response Json" + JSON.toJSONString(result));
        return Response.createSucc(result);
    }

    @RequestMapping(value = "/rtms/transport_job_resume/req", method = RequestMethod.POST)
    public Response rtmsTransportJobResumeReq(@RequestBody Params.TransportJobResumeReqParams transportJobResumeReqParams) {
        log.info("rtmsTransportJobResumeReq Request Json" + JSON.toJSONString(transportJobResumeReqParams));
        Results.TransportJobResumeReqResult result = new Results.TransportJobResumeReqResult();
        final String transactionID = TransactionIDEnum.ROM05.getValue();
        ThreadContextHolder.setTransactionId(transactionID);
        User user = transportJobResumeReqParams.getRequestUserID();
        Validations.check(null == user, "the user info is null...");
        List<Infos.CarrierJob> amhsJobCreateArray = transportJobResumeReqParams.getCarrierJobData();
        Validations.check(ArrayUtils.isEmpty(amhsJobCreateArray), "the amhsJobCreateArray info is null...");
        Infos.ObjCommon objCommon = utilsComp.setObjCommon(transactionID, user);

        log.info("【step1】 - transportJobResumeReqService.sxRtmsTransportJobResumeReq");
        result = transportJobResumeReqService.sxRtmsTransportJobResumeReq(objCommon, transportJobResumeReqParams);
        log.info("rtmsTransportJobResumeReq Response Json" + JSON.toJSONString(result));
        return Response.createSucc(result);
    }
}
