package com.fa.cim.simulator.controller;

import com.alibaba.fastjson.JSON;
import com.fa.cim.simulator.common.menu.TransactionIDEnum;
import com.fa.cim.simulator.dto.Params;
import com.fa.cim.simulator.dto.Response;
import com.fa.cim.simulator.dto.Results;
import com.fa.cim.simulator.dto.User;
import com.fa.cim.simulator.pojo.ObjectIdentifier;
import com.fa.cim.simulator.utils.ThreadContextHolder;
import com.fa.cim.simulator.utils.Validations;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2018/12/13        ********             Miner              create file
 *
 * @author: Miner
 * @date: 2018/12/13 17:46
 * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@RestController
@RequestMapping("/mcs")
public class UploadInventoryReqController {


    @RequestMapping(value = "/upload_inventory/rep", method = RequestMethod.POST)
    public Response uploadInventoryReq(@RequestBody Params.AmhsUploadInventoryReqParmas amhsUploadInventoryReqParmas) {
        log.info("uploadInventoryReq Request Json" + JSON.toJSONString(amhsUploadInventoryReqParmas));
        Results.AmhsUploadInventoryReqResult result = new Results.AmhsUploadInventoryReqResult();
        final String transactionID = TransactionIDEnum.OM09.getValue();
        ThreadContextHolder.setTransactionId(transactionID);
        User user = amhsUploadInventoryReqParmas.getRequestUserID();
        Validations.check(null == user, "the user info is null...");
        List<Results.UploadInventoryReqResult> uploadInventoryReqResults = new ArrayList<>();
        //CRUP0001~CRUP0100
        int i = 0;
        for (i = 1; i <= 100; i++) {
            Results.UploadInventoryReqResult uploadInventoryReqResult = new Results.UploadInventoryReqResult();
            ObjectIdentifier objectIdentifier = new ObjectIdentifier();
            String name = "";
            if (i < 10) {
                name = "CRUP000" + i;
            } else if (i == 100) {
                name = "CRUP0" + i;
            } else {
                name = "CRUP00" + i;
            }
            objectIdentifier.setValue(name);
            uploadInventoryReqResult.setCarrierID(objectIdentifier);
            uploadInventoryReqResult.setShelfType("");
            uploadInventoryReqResult.setAlternateStockerFlag(false);
            uploadInventoryReqResult.setStockInTime("20200327");
            uploadInventoryReqResult.setZoneID("");
            uploadInventoryReqResults.add(uploadInventoryReqResult);
        }

        result.setUploadInventoryReqResults(uploadInventoryReqResults);
        log.info("uploadInventoryReq Response json" + JSON.toJSONString(result));
        return Response.createSucc(transactionID, result);
    }

}
