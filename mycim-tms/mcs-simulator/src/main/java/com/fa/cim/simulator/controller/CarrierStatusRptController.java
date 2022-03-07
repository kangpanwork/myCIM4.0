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
 * <p>CarrierStatusReportController .<br/></p>
 * * change history:
 * date             defect#             person             comments
 * -------------------------------------------------------------------------------------------------------------------
 * 2019/1/17        ********             miner               create file
 *
 * @author: miner
 * @date: 2019/1/17 17:28
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@RestController
@RequestMapping("/mcs")
public class CarrierStatusRptController {

    @Autowired
    private IToTmsRemoteManager toTmsRemoteManager;

    @RequestMapping(value = "/carrier_status/rpt", method = RequestMethod.POST)
    public Response carrierStatusRpt(@RequestBody Params.CarrierStatusReportParam carrierStatusReportParam) {
        log.info("Request Json" + JSON.toJSONString(carrierStatusReportParam));
        final String transactionID = TransactionIDEnum.TM02.getValue();
        ThreadContextHolder.setTransactionId(transactionID);
        log.info("mq MCS ->TMS callTmsCarrierIDReadReportRetry request {}", JSON.toJSONString(carrierStatusReportParam));
        Response response = null;
        try {
            response = toTmsRemoteManager.callTmsCarrierStatusRpt(carrierStatusReportParam);
        } catch(CimIntegrationException e) {
            Validations.check(true,new Code((int) e.getCode(),e.getMessage()));
        }
        log.info("mq MCS ->TMS callTmsCarrierIDReadReportRetry response {}" + JSON.toJSONString(response));
        return response;
    }

    @RequestMapping(value = "/rtms/carrier_status/rpt", method = RequestMethod.POST)
    public Response rtmsCarrierStatusRpt(@RequestBody Params.CarrierStatusReportParam carrierStatusReportParam) {
        log.info("Request Json" + JSON.toJSONString(carrierStatusReportParam));
        final String transactionID = TransactionIDEnum.RTM02.getValue();
        ThreadContextHolder.setTransactionId(transactionID);
        log.info("mq MCS ->RTMS callRTmsCarrierStatusRpt request {}", JSON.toJSONString(carrierStatusReportParam));
        Response response = toTmsRemoteManager.callRTmsCarrierStatusRpt(carrierStatusReportParam);
        log.info("mq MCS ->RTMS callRTmsCarrierStatusRpt response {}" + JSON.toJSONString(response));
        return response;
    }
}
