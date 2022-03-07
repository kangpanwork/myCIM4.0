package com.fa.cim.simulator.controller;

import com.alibaba.fastjson.JSON;
import com.fa.cim.simulator.common.menu.TransactionIDEnum;
import com.fa.cim.simulator.dto.Params;
import com.fa.cim.simulator.dto.Response;
import com.fa.cim.simulator.dto.Results;
import com.fa.cim.simulator.dto.User;
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
public class StockerDetailInfoInqController {

    @RequestMapping(value = "/stocker_detail_info/inq", method = RequestMethod.POST)
    public Response stockerDetailInfoInq(@RequestBody Params.StockerDetailInfoInqParmas stockerDetailInfoInqParmas) {
        log.info("stockerDetailInfoInq Request Json" + JSON.toJSONString(stockerDetailInfoInqParmas));
        Results.StockerDetailInfoInqResult result = new Results.StockerDetailInfoInqResult();
        final String transactionID = TransactionIDEnum.OM10.getValue();
        ThreadContextHolder.setTransactionId(transactionID);
        User user = stockerDetailInfoInqParmas.getRequestUserID();
        result.setMachineID(stockerDetailInfoInqParmas.getMachineID());
        List<Infos.AmhsResourceInfo> amhsResourceInfos = new ArrayList<Infos.AmhsResourceInfo>();
        Infos.AmhsResourceInfo amhsResourceInfo = new Infos.AmhsResourceInfo();
        amhsResourceInfo.setResourceID("TestData");
        amhsResourceInfo.setResourceType("TestType");
        amhsResourceInfos.add(amhsResourceInfo);
        result.setResourceInfoData(amhsResourceInfos);
        List<Infos.AmhsZoneInfo> amhsZoneInfos = new ArrayList<Infos.AmhsZoneInfo>();
        Infos.AmhsZoneInfo amhsZoneInfo = new Infos.AmhsZoneInfo();
        amhsZoneInfo.setZoneID("ZoneIdTEST");
        amhsZoneInfo.setZoneDescription("testDate");
        amhsZoneInfo.setEmergencyCapacityOfZone(1);
        amhsZoneInfos.add(amhsZoneInfo);
        result.setZoneInfoData(amhsZoneInfos);
        ObjectIdentifier objectIdentifier = new ObjectIdentifier();
        objectIdentifier.setValue("WLOT");
        result.setE10Status(objectIdentifier);
        log.info("stockerDetailInfoInq Response json" + JSON.toJSONString(result));
        return Response.createSucc(transactionID, result);
    }
}