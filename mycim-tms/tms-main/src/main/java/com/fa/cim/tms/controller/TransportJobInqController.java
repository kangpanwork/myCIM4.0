package com.fa.cim.tms.controller;

import com.alibaba.fastjson.JSON;
import com.fa.cim.tms.common.enums.TransactionIDEnum;
import com.fa.cim.tms.controller.interfaces.ITransportJobInqController;
import com.fa.cim.tms.dto.Params;
import com.fa.cim.tms.dto.Response;
import com.fa.cim.tms.dto.Results;
import com.fa.cim.tms.dto.User;
import com.fa.cim.tms.method.IUtilsComp;
import com.fa.cim.tms.pojo.Infos;
import com.fa.cim.tms.service.ITransportJobInqService;
import com.fa.cim.tms.utils.ThreadContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * description:
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/10/21                               Neyo                create file
 *
 * @author: Neyo
 * @date: 2020/10/21 11:18
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@RestController
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class TransportJobInqController implements ITransportJobInqController {
    @Autowired
    private IUtilsComp utilsComp;
    @Autowired
    private ITransportJobInqService transportJobInqService;

    @RequestMapping(value = "/tms/transport_job/inq", method = RequestMethod.POST)
    public Response tmsTransportJobInq(@RequestBody Params.TransportJobInqParams transportJobInqParams) {
        Results.TransportJobInqResult result = new Results.TransportJobInqResult();
        log.info("tmsTransportJobInq Request Json " + JSON.toJSONString(transportJobInqParams));
        final String transcationID = TransactionIDEnum.OM14.getValue();
        ThreadContextHolder.setTransactionId(transcationID);
        User user = transportJobInqParams.getRequestUserID();
        Infos.ObjCommon objCommon = utilsComp.setObjCommon(transcationID, user);

        log.info("【step1】 - transportJobInqService.sxTransportJobInq");
        result = transportJobInqService.sxTransportJobInq(objCommon, transportJobInqParams);
        log.info("tmsTransportJobInq Response json /n" + JSON.toJSONString(result));
        return Response.createSucc(result);

    }

    @RequestMapping(value = "/rtms/transport_job/inq", method = RequestMethod.POST)
    public Response rtmsTransportJobInq(@RequestBody Params.TransportJobInqParams transportJobInqParams) {
        Results.TransportJobInqResult result = new Results.TransportJobInqResult();
        log.info("rtmsTransportJobInq Request Json " + JSON.toJSONString(transportJobInqParams));
        final String transcationID = TransactionIDEnum.ROM14.getValue();
        ThreadContextHolder.setTransactionId(transcationID);
        User user = transportJobInqParams.getRequestUserID();
        Infos.ObjCommon objCommon = utilsComp.setObjCommon(transcationID, user);

        log.info("【step1】 - transportJobInqService.sxRtmsTransportJobInq");
        result = transportJobInqService.sxRtmsTransportJobInq(objCommon, transportJobInqParams);
        log.info("rtmsTransportJobInq Response json /n" + JSON.toJSONString(result));
        return Response.createSucc(result);
    }
}
