package com.fa.cim.tms.controller;

import com.alibaba.fastjson.JSON;
import com.fa.cim.tms.common.enums.TransactionIDEnum;
import com.fa.cim.tms.controller.interfaces.IStockerDetailInfoInqController;
import com.fa.cim.tms.dto.Params;
import com.fa.cim.tms.dto.Response;
import com.fa.cim.tms.dto.Results;
import com.fa.cim.tms.dto.User;
import com.fa.cim.tms.method.IUtilsComp;
import com.fa.cim.tms.pojo.Infos;
import com.fa.cim.tms.service.IStockerDetailInfoInqService;
import com.fa.cim.tms.utils.ThreadContextHolder;
import com.fa.cim.tms.utils.Validations;
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
 * 2020/10/20                               Neyo                create file
 *
 * @author: Neyo
 * @date: 2020/10/20 15:46
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@RestController
@RequestMapping("/tms")
public class StockerDetailInfoInqController implements IStockerDetailInfoInqController {
    @Autowired
    private IUtilsComp utilsComp;
    @Autowired
    private IStockerDetailInfoInqService stockerDetailInfoInqService;

    @RequestMapping(value = "/stocker_detail_info/inq", method = RequestMethod.POST)
    public Response tmsStockerDetailInfoInq(@RequestBody Params.StockerDetailInfoInqParmas stockerDetailInfoInqParmas) {
        log.info("tmsStockerDetailInfoInq Request Json" + JSON.toJSONString(stockerDetailInfoInqParmas));
        Results.StockerDetailInfoInqResult result = new Results.StockerDetailInfoInqResult();
        final String transactionID = TransactionIDEnum.OM10.getValue();
        ThreadContextHolder.setTransactionId(transactionID);
        User user = stockerDetailInfoInqParmas.getRequestUserID();
        Validations.check(null == user, "the user info is null...");
        Infos.ObjCommon objCommon = utilsComp.setObjCommon(transactionID, user);

        log.info("【step1】 - stockerDetailInfoInqService.sxStockerDetailInfoInq");
        result = stockerDetailInfoInqService.sxStockerDetailInfoInq(objCommon, stockerDetailInfoInqParmas);
        log.info("tmsStockerDetailInfoInq Response json /n" + JSON.toJSONString(result));
        return Response.createSucc(result);
    }
    //RTMS Interface is same as TMS
}