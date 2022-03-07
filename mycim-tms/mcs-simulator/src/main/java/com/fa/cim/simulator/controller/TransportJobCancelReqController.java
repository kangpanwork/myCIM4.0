package com.fa.cim.simulator.controller;

import com.alibaba.fastjson.JSON;
import com.fa.cim.simulator.common.menu.TransactionIDEnum;
import com.fa.cim.simulator.dto.Params;
import com.fa.cim.simulator.dto.Response;
import com.fa.cim.simulator.dto.Results;
import com.fa.cim.simulator.pojo.Infos;
import com.fa.cim.simulator.utils.ThreadContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * description:
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/10/27                               Neyo                create file
 *
 * @author: Neyo
 * @date: 2020/10/27 13:04
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@RestController
@RequestMapping("/mcs")
public class TransportJobCancelReqController {

    @RequestMapping(value = "/transport_job_cancel/req", method = RequestMethod.POST)
    public Response transportJobCancelReq(@RequestBody Params.TransportJobCancelReqParams transportJobCancelReqParams) {
        log.info("Request Json" + JSON.toJSONString(transportJobCancelReqParams));
        Results.TransportJobCancelReqResult result = new Results.TransportJobCancelReqResult();
        final String transactionID = TransactionIDEnum.OM04.getValue();
        ThreadContextHolder.setTransactionId(transactionID);
        List<Infos.AmhsCarrierJob> amhsCarrierJobs = transportJobCancelReqParams.getCarrierJobData();
        List<Infos.AmhsCarrierJobRc> amhsCarrierJobRcs = new ArrayList<Infos.AmhsCarrierJobRc>();
        for (Infos.AmhsCarrierJob amhsCarrierJob : amhsCarrierJobs) {
            Infos.AmhsCarrierJobRc amhsCarrierJobRc = new Infos.AmhsCarrierJobRc();
            amhsCarrierJobRc.setCarrierID(amhsCarrierJob.getCarrierID());
            amhsCarrierJobRc.setCarrierJobID(amhsCarrierJob.getCarrierJobID());
            amhsCarrierJobRc.setCarrierReturnCode("Success");
            amhsCarrierJobRcs.add(amhsCarrierJobRc);
        }
        result.setCarrierJobRcData(amhsCarrierJobRcs);
        log.info("Response json" + JSON.toJSONString(result));
        return Response.createSucc(transactionID, result);
    }
}