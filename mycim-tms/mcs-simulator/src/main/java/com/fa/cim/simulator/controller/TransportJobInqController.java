package com.fa.cim.simulator.controller;

import com.alibaba.fastjson.JSON;
import com.fa.cim.simulator.common.menu.TransactionIDEnum;
import com.fa.cim.simulator.dto.Params;
import com.fa.cim.simulator.dto.Response;
import com.fa.cim.simulator.dto.Results;
import com.fa.cim.simulator.pojo.Infos;
import com.fa.cim.simulator.utils.ThreadContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
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
 * @date: 2020/10/27 13:09
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@RestController
@RequestMapping("/mcs")
public class TransportJobInqController {

    @ResponseBody
    @RequestMapping(value = "/transport_job/inq", method = RequestMethod.POST)
    public Response transportJobInq(@RequestBody Params.TransportJobInqParams transportJobInqParams) {
        Results.TransportJobInqResult result = new Results.TransportJobInqResult();
        log.info("Request Json  transportJobInq " + JSON.toJSONString(transportJobInqParams));
        final String transactionID = TransactionIDEnum.OM14.getValue();
        ThreadContextHolder.setTransactionId(transactionID);
        List<Infos.AmhsTransportJobInqData> jobInqData = new ArrayList<Infos.AmhsTransportJobInqData>();
        Infos.AmhsTransportJobInqData amhsTransportJobInqData = new Infos.AmhsTransportJobInqData();
        amhsTransportJobInqData.setJobID(transportJobInqParams.getCarrierID().getValue() + "MCSJOB" + new Timestamp(System.currentTimeMillis()));
        amhsTransportJobInqData.setJobStatus("XCMP");
        amhsTransportJobInqData.setTransportType("S");
        List<Infos.TransferJobInfo> entities = new ArrayList<Infos.TransferJobInfo>();
        Infos.TransferJobInfo fxtrnreqEntity = new Infos.TransferJobInfo();
        entities.add(fxtrnreqEntity);
        fxtrnreqEntity.setCarrierID(transportJobInqParams.getCarrierID());
        fxtrnreqEntity.setCarrierJobID(amhsTransportJobInqData.getJobID());
        fxtrnreqEntity.setCarrierJobStatus(amhsTransportJobInqData.getJobStatus());
        fxtrnreqEntity.setFromMachineID(transportJobInqParams.getFromMachineID());
        fxtrnreqEntity.setToMachineID(transportJobInqParams.getToMachineID());
        amhsTransportJobInqData.setCarrierJobInqInfo(entities);
        jobInqData.add(amhsTransportJobInqData);
        result.setInquiryType(transportJobInqParams.getInquiryType());
        result.setJobInqData(jobInqData);
        log.info(" Response json transportJobInq" + JSON.toJSONString(result));
        return Response.createSucc(transactionID, result);
    }
}
