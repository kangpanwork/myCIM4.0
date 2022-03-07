package com.fa.cim.simulator.controller;

import com.alibaba.fastjson.JSON;
import com.fa.cim.simulator.common.menu.TransactionIDEnum;
import com.fa.cim.simulator.dto.Params;
import com.fa.cim.simulator.dto.Response;
import com.fa.cim.simulator.dto.Results;
import com.fa.cim.simulator.pojo.Infos;
import com.fa.cim.simulator.pojo.ObjectIdentifier;
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
 * @date: 2020/10/27 12:50
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@RestController
@RequestMapping("/mcs")
public class PriorityChangeReqController {

    @RequestMapping(value = "/priority_change/req", method = RequestMethod.POST)
    public Response priorityChangeReq(@RequestBody Params.PriorityChangeReqParam priorityChangeReqParam) {
        Results.PriorityChangeReqResult result = new Results.PriorityChangeReqResult();
        final String transactionID = TransactionIDEnum.OM12.getValue();
        ThreadContextHolder.setTransactionId(transactionID);
        List<Infos.PriorityInfo> priorityInfoData = priorityChangeReqParam.getPriorityInfoData();
        List<Results.PiorityInfoResult> priorityInfoResultData = new ArrayList<Results.PiorityInfoResult>();
        for (Infos.PriorityInfo priorityInfo : priorityInfoData) {
            Results.PiorityInfoResult piorityInfoResult = new Results.PiorityInfoResult();
            piorityInfoResult.setCarrierID(priorityInfo.getCarrierID());
            piorityInfoResult.setCarrierJobID(priorityInfo.getCarrierJobID());
            piorityInfoResult.setCarrierReturnCode("aaa");
            ObjectIdentifier objectIdentifier = new ObjectIdentifier();
            objectIdentifier.setValue("STK0103");
            piorityInfoResult.setToMachineID(objectIdentifier);
            priorityInfoResultData.add(piorityInfoResult);
        }
        result.setPriorityInfoResultData(priorityInfoResultData);
        result.setJobID(priorityChangeReqParam.getJobID());
        log.info("PriorityChangeReq Response json" + JSON.toJSONString(result));
        return Response.createSucc(transactionID, result);
    }
}
