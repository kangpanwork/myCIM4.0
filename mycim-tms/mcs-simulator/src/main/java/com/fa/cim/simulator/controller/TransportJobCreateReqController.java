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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * description:
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/10/27                               Neyo                create file
 *
 * @author: Neyo
 * @date: 2020/10/27 13:01
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@RestController
@RequestMapping("/mcs")
public class TransportJobCreateReqController {

    @RequestMapping(value = "/transport_job_create/req", method = RequestMethod.POST)
    public Response transportJobCreateReq(@RequestBody Params.TransportJobCreateReqParams transportJobCreateReqParams) {
        log.info("Request Json" + JSON.toJSONString(transportJobCreateReqParams));
        final String transactionID = TransactionIDEnum.OM01.getValue();
        ThreadContextHolder.setTransactionId(transactionID);
        Results.TransportJobCreateReqResult result = new Results.TransportJobCreateReqResult();
        List<Infos.AmhsJobCreateArray> amhsJobCreateArrays = transportJobCreateReqParams.getJobCreateData();
        ArrayList<Infos.AmhsJobCreateResult> amhsJobCreateResults = new ArrayList<Infos.AmhsJobCreateResult>();
        for (Infos.AmhsJobCreateArray amhsJobCreateArray : amhsJobCreateArrays) {
            Infos.AmhsJobCreateResult amhsJobCreateResult = new Infos.AmhsJobCreateResult();
            List<Infos.AmhsToDestination> amhsToDestinations = amhsJobCreateArray.getToMachine();
            amhsJobCreateResult.setCarrierID(amhsJobCreateArray.getCarrierID());
            amhsJobCreateResult.setCarrierJobID(amhsJobCreateArray.getCarrierID().getValue() + getOnlyDateNum());
            amhsJobCreateResult.setToMachineID(amhsToDestinations.get(0).getToMachineID());
            amhsJobCreateResults.add(amhsJobCreateResult);
        }
        result.setJobCreateResultSequenceData(amhsJobCreateResults);
        result.setJobID(getOnlyDateNum());

        log.info("Response json /n" + JSON.toJSONString(result));
        return Response.createSucc(transactionID,result);
    }


    public static String getOnlyDateNum() {
        SimpleDateFormat simpleDateFormat;
        simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        String date = simpleDateFormat.format(new Date());
        Random random = new Random();
        int rannum = (int) (random.nextDouble() * (99999 - 10000 + 1)) + 10000;
        String rand = date + rannum;
        return rand;
    }
}
