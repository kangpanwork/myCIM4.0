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
import org.springframework.web.bind.annotation.*;

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
public class E10StatusRptController {

    @Autowired
    private IToTmsRemoteManager toTmsRemoteManager;

    @ResponseBody
    @RequestMapping(value = "/e10_Status/rpt", method = RequestMethod.POST)
    public Response e10StatusRpt(@RequestBody Params.E10StatusReportParmas e10StatusReportParmas) {
        final String transactionID = TransactionIDEnum.TM05.getValue();
        ThreadContextHolder.setTransactionId(transactionID);
        log.info("mq MCS ->TMS callTmsE10StatusRpt request {}", JSON.toJSONString(e10StatusReportParmas));
        Response response = null;
        try {
            response = toTmsRemoteManager.callTmsE10StatusRpt(e10StatusReportParmas);
        } catch(CimIntegrationException e) {
            Validations.check(true,new Code((int) e.getCode(),e.getMessage()));
        }
        log.info("mq MCS ->TMS callTmsE10StatusRpt response {}" + JSON.toJSONString(response));
        return response;
    }
}
