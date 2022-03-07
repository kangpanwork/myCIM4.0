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
 * @description: N2PurgeReqController
 * @author: miner
 * @create: 2019-01-22 16:52
 */
@Slf4j
@RestController
@RequestMapping("/mcs")
public class TransportJobResumeReqController {

    @RequestMapping(value = "/transport_job_resume/req", method = RequestMethod.POST)
    public Response transportJobResumeReq(@RequestBody Params.TransportJobResumeReqParams transportJobResumeReqParams) {
        final String transactionID = TransactionIDEnum.OM05.getValue();
        // TODO: 2020/10/27 todo confirm function
        return null;
    }
}
