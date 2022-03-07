package com.fa.cim.simulator.controller;

import com.fa.cim.simulator.common.menu.TransactionIDEnum;
import com.fa.cim.simulator.dto.Params;
import com.fa.cim.simulator.dto.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @program: mycim_tms
 * @description: TransportJobPauseReqController
 * @author: miner
 * @create: 2019-01-16 15:06
 */
@Slf4j
@RestController
@RequestMapping("/mcs")
public class TransportJobStopReqController {

    @RequestMapping(value = "/transport_job_stop/req", method = RequestMethod.POST)
    public Response transportJobStopReq(@RequestBody Params.TransportJobStopReqParams transportJobStopReqParams) {
        final String transactionID = TransactionIDEnum.OM02.getValue();
        // TODO: 2020/10/27 todo confirm the function
        return null;
    }
}
