package com.fa.cim.simulator.controller;

import com.alibaba.fastjson.JSON;
import com.fa.cim.middleware.standard.core.exception.base.CimIntegrationException;
import com.fa.cim.simulator.common.menu.TransactionIDEnum;
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
 * @date: 2020/10/27 13:12
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@RestController
@RequestMapping("/mcs")
public class TransportJobStatusRptController {

    @Autowired
    private IToTmsRemoteManager toTmsRemoteManager;

    @RequestMapping(value = "/transport_job_status/rpt", method = RequestMethod.POST)
    public Response transportJobStatusRpt(@RequestBody Params.TransportJobStatusReportParams transportJobStatusReportParams) {
        final String transactionID = TransactionIDEnum.TM03.getValue();
        ThreadContextHolder.setTransactionId(transactionID);
        log.info("mq MCS ->TMS callTmsTransportJobStatusRpt request {}", JSON.toJSONString(transportJobStatusReportParams));
        Response response = null;
        try {
            response = toTmsRemoteManager.callTmsTransportJobStatusRpt(transportJobStatusReportParams);
        } catch(CimIntegrationException e) {
            Validations.check(true,new Code((int) e.getCode(),e.getMessage()));
        }
        log.info("mq MCS ->TMS callTmsTransportJobStatusRpt response {}", JSON.toJSONString(response));
        return response;
    }

    @RequestMapping(value = "/rtms/transport_job_status/rpt", method = RequestMethod.POST)
    public Response rtmsTransportJobStatusRpt(@RequestBody Params.TransportJobStatusReportParams transportJobStatusReportParams) {
        final String transactionID = TransactionIDEnum.RTM03.getValue();
        ThreadContextHolder.setTransactionId(transactionID);
        log.info("mq MCS ->RTMS callRTmsTransportJobStatusRpt request {}", JSON.toJSONString(transportJobStatusReportParams));
        Response response = null;
        try {
            response = toTmsRemoteManager.callRTmsTransportJobStatusRpt(transportJobStatusReportParams);
        } catch(CimIntegrationException e) {
            Validations.check(true,new Code((int) e.getCode(),e.getMessage()));
        }
        log.info("mq MCS ->RTMS callRTmsTransportJobStatusRpt response {}", JSON.toJSONString(response));
        return response;
    }
}
