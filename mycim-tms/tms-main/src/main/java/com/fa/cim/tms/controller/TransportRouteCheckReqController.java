package com.fa.cim.tms.controller;

import com.alibaba.fastjson.JSON;
import com.fa.cim.tms.common.enums.TransactionIDEnum;
import com.fa.cim.tms.controller.interfaces.ITransportRouteCheckReqController;
import com.fa.cim.tms.dto.Params;
import com.fa.cim.tms.dto.Response;
import com.fa.cim.tms.dto.Results;
import com.fa.cim.tms.dto.User;
import com.fa.cim.tms.method.IUtilsComp;
import com.fa.cim.tms.pojo.Infos;
import com.fa.cim.tms.service.ITransportRouteCheckReqService;
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
 * 2020/10/22                               Neyo                create file
 *
 * @author: Neyo
 * @date: 2020/10/22 13:04
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@RestController
@RequestMapping("/tms")
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class TransportRouteCheckReqController implements ITransportRouteCheckReqController {
    @Autowired
    private IUtilsComp utilsComp;
    @Autowired
    private ITransportRouteCheckReqService transportRouteCheckReqService;

    @RequestMapping(value = "/transport_route_check/req", method = RequestMethod.POST)
    public Response tmsTransportRouteCheckReq(@RequestBody Params.TransportRouteCheckReqParams transportRouteCheckReqParams) {
        log.info("tmsTransportRouteCheckReq Request Json" + JSON.toJSONString(transportRouteCheckReqParams));
        Results.TransportRouteCheckReqResult result = new Results.TransportRouteCheckReqResult();
        final String transactionID = TransactionIDEnum.OM08.getValue();
        ThreadContextHolder.setTransactionId(transactionID);
        User user = transportRouteCheckReqParams.getRequestUserID();
        Validations.check(null == user, "the user info is null...");
        List<Params.RouteCheck> routeChecks = transportRouteCheckReqParams.getRouteCheckData();
        Validations.check(null == routeChecks, "the routeChecks info is null...");
        Infos.ObjCommon objCommon = utilsComp.setObjCommon(TransactionIDEnum.OM08, user);

        log.info("【step1】 - transportRouteCheckReqService.sxTransportRouteCheckReq");
        result = transportRouteCheckReqService.sxTransportRouteCheckReq(objCommon, transportRouteCheckReqParams);
        log.info("tmsTransportRouteCheckReq Response json" + JSON.toJSONString(result));
        return Response.createSucc(result);
    }
    //RTMS Interface is same as TMS
}
