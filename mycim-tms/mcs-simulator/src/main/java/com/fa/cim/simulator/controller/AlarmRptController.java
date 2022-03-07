package com.fa.cim.simulator.controller;

import com.alibaba.fastjson.JSONObject;
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
 * @date: 2020/10/27 11:15
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@RestController
@RequestMapping("/mcs")
public class AlarmRptController {
    @Autowired
    private IToTmsRemoteManager toTmsRemoteManager;

    @RequestMapping(value = "/alarm/rpt", method = RequestMethod.POST)
    public Response alarmRpt(@RequestBody Params.AlarmReportParam alarmReportParam) {
        final String transactionID = TransactionIDEnum.TM10.getValue();
        ThreadContextHolder.setTransactionId(transactionID);
        log.info("mq MCS ->TMS callTmsAlarmRpt request {}", JSONObject.toJSONString(alarmReportParam));
        Response response = null;
        try {
            response = toTmsRemoteManager.callTmsAlarmRpt(alarmReportParam);
        } catch(CimIntegrationException e) {
            Validations.check(true,new Code((int) e.getCode(),e.getMessage()));
        }
        log.info("mq MCS ->TMS  callTmsAlarmRpt response {}", JSONObject.toJSONString(response));
        return response;
    }

}
