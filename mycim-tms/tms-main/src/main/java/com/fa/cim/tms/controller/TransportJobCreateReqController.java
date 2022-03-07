package com.fa.cim.tms.controller;

import com.alibaba.fastjson.JSON;
import com.fa.cim.tms.common.enums.TransactionIDEnum;
import com.fa.cim.tms.controller.interfaces.ITransportJobCreateReqController;
import com.fa.cim.tms.dto.Params;
import com.fa.cim.tms.dto.Response;
import com.fa.cim.tms.dto.Results;
import com.fa.cim.tms.dto.User;
import com.fa.cim.tms.method.IUtilsComp;
import com.fa.cim.tms.pojo.Infos;
import com.fa.cim.tms.service.ITransportJobCreateReqService;
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
 * @date: 2020/10/20 17:23
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@RestController
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class TransportJobCreateReqController implements ITransportJobCreateReqController {
    @Autowired
    private IUtilsComp utilsComp;
    @Autowired
    private ITransportJobCreateReqService transportJobCreateService;

    @RequestMapping(value = "/tms/transport_job_create/req", method = RequestMethod.POST)
    public Response tmsTransportJobCreateReq(@RequestBody Params.TransportJobCreateReqParams transportJobCreateReqParams) {
        log.info("tmsTransportJobCreateReq Request Json  /n:" + JSON.toJSONString(transportJobCreateReqParams));
        Results.TransportJobCreateReqResult result = new Results.TransportJobCreateReqResult();
        final String txId = TransactionIDEnum.OM01.getValue();
        ThreadContextHolder.setTransactionId(txId);
        User user = transportJobCreateReqParams.getRequestUserID();

        Validations.check(null == user, "the user info is null...");
        List<Infos.AmhsJobCreateArray> amhsJobCreateArray = transportJobCreateReqParams.getJobCreateData();
        Validations.check(ArrayUtils.isEmpty(amhsJobCreateArray), "the amhsJobCreateArray info is null...");
        Infos.ObjCommon objCommon = utilsComp.setObjCommon(txId, user);

        log.info("【step1】 - transportJobCreateService.sxTransportJobCreateReq");
        result = transportJobCreateService.sxTransportJobCreateReq(objCommon, transportJobCreateReqParams);
        log.info("tmsTransportJobCreateReq Response json /n" + JSON.toJSONString(result));
        return Response.createSucc(result);
    }

    @RequestMapping(value = "/rtms/transport_job_create/req", method = RequestMethod.POST)
    public Response rtmsTransportJobCreateReq(@RequestBody Params.TransportJobCreateReqParams transportJobCreateReqParams) {
        log.info("rtmsTransportJobCreateReq Request Json  /n:" + JSON.toJSONString(transportJobCreateReqParams));
        Results.TransportJobCreateReqResult result = new Results.TransportJobCreateReqResult();
        final String txId = TransactionIDEnum.ROM01.getValue();
        ThreadContextHolder.setTransactionId(txId);
        User user = transportJobCreateReqParams.getRequestUserID();

        Validations.check(null == user, "the user info is null...");
        List<Infos.AmhsJobCreateArray> amhsJobCreateArray = transportJobCreateReqParams.getJobCreateData();
        Validations.check(ArrayUtils.isEmpty(amhsJobCreateArray), "the amhsJobCreateArray info is null...");
        Infos.ObjCommon objCommon = utilsComp.setObjCommon(txId, user);

        log.info("【step1】 - transportJobCreateService.sxRtmsTransportJobCreateReq");
        result = transportJobCreateService.sxRtmsTransportJobCreateReq(objCommon, transportJobCreateReqParams);
        log.info("rtmsTransportJobCreateReq Response json /n" + JSON.toJSONString(result));
        return Response.createSucc(result);
    }
}
