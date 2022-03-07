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
 * @date: 2020/10/27 12:50
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@RestController
@RequestMapping("/mcs")
public class EndTimeViolationRptController {

    @Autowired
    private IToTmsRemoteManager toTmsRemoteManager;

    @RequestMapping(value = "/end_time_violation/rpt", method = RequestMethod.POST)
    public Response endTimeViolationRpt(@RequestBody Params.EndTimeViolationReportParam endTimeViolationReportParam) {
        final String transactionID = TransactionIDEnum.TM04.getValue();
        ThreadContextHolder.setTransactionId(transactionID);
        log.info("mq MCS ->TMS callTmsEndTimeViolationRpt request {}", JSON.toJSONString(endTimeViolationReportParam));
        Response response = null;
        try {
            response = toTmsRemoteManager.callTmsEndTimeViolationRpt(endTimeViolationReportParam);
        } catch(CimIntegrationException e) {
            Validations.check(true,new Code((int) e.getCode(),e.getMessage()));
        }
        log.info("mq MCS ->TMS callTmsEndTimeViolationRpt response {}" + JSON.toJSONString(response));
        return response;
    }

    @RequestMapping(value = "/rtms/end_time_violation/rpt", method = RequestMethod.POST)
    public Response rtmsEndTimeViolationRpt(@RequestBody Params.EndTimeViolationReportParam endTimeViolationReportParam) {
        final String transactionID = TransactionIDEnum.RTM04.getValue();
        ThreadContextHolder.setTransactionId(transactionID);
        log.info("mq MCS ->RTMS callRTmsEndTimeViolationRpt request {}", JSON.toJSONString(endTimeViolationReportParam));
        Response response = null;
        try {
            response = toTmsRemoteManager.callRTmsEndTimeViolationRpt(endTimeViolationReportParam);
        } catch(CimIntegrationException e) {
            Validations.check(true,new Code((int) e.getCode(),e.getMessage()));
        }
        log.info("mq MCS ->RTMS callRTmsEndTimeViolationRpt response {}" + JSON.toJSONString(response));
        return response;
    }
}
