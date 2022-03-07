package com.fa.cim.simulator.controller;

import com.alibaba.fastjson.JSON;
import com.fa.cim.middleware.standard.core.exception.base.CimIntegrationException;
import com.fa.cim.simulator.common.menu.TransactionIDEnum;
import com.fa.cim.simulator.dto.CimRequest;
import com.fa.cim.simulator.dto.Code;
import com.fa.cim.simulator.dto.Params;
import com.fa.cim.simulator.dto.Response;
import com.fa.cim.simulator.remote.IToTmsRemoteManager;
import com.fa.cim.simulator.utils.ThreadContextHolder;
import com.fa.cim.simulator.utils.Validations;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * description:
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/10/27                               Neyo                create file
 *
 * @author: Neyo
 * @date: 2020/10/27 11:15
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@RestController
@RequestMapping("/mcs")
public class AllCarrierIDInqController {
    @Autowired
    private IToTmsRemoteManager toTmsRemoteManager;

    @RequestMapping(value = "/all_carrier_id/inq", method = RequestMethod.POST)
    public Response allCarrierIDInq(@RequestBody Params.AllCarrierIDInquiryParam allCarrierIDInquiryParam) {
        final String transactionID = TransactionIDEnum.TM14.getValue();
        ThreadContextHolder.setTransactionId(transactionID);
        log.info("mq MCS ->TMS callTmsAllCarrierIDInq request {}", JSON.toJSONString(allCarrierIDInquiryParam));
        Response response = null;
        try {
            response = toTmsRemoteManager.callTmsAllCarrierIDInq(allCarrierIDInquiryParam);
        } catch(CimIntegrationException e) {
            Validations.check(true,new Code((int) e.getCode(),e.getMessage()));
        }
        log.info("mq MCS ->TMS callTmsAllCarrierIDInq response {}" + JSON.toJSONString(response));
        return response;
    }

    @RequestMapping(value = "/rtms/all_carrier_id/inq", method = RequestMethod.POST)
    public Response rtmsAllCarrierIDInq(@RequestBody Params.AllCarrierIDInquiryParam allCarrierIDInquiryParam) {
        final String transcationID = TransactionIDEnum.RTM14.getValue();
        CimRequest cimRequest = CimRequest.create(allCarrierIDInquiryParam.getRequestUserID(), transcationID, allCarrierIDInquiryParam);
        log.info("mq MCS ->RTMS callRTmsAllCarrierIDInq request {}", JSON.toJSONString(cimRequest));
        Response response = null;
        try {
            response = toTmsRemoteManager.callRTmsAllCarrierIDInq(cimRequest);
        } catch(CimIntegrationException e) {
            Validations.check(true,new Code((int) e.getCode(),e.getMessage()));
        }
        log.info("mq MCS ->RTMS callRTmsAllCarrierIDInq response {}" + JSON.toJSONString(response));
        return response;
    }
}
