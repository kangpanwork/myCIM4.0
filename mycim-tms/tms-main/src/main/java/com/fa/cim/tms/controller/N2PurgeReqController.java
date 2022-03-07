package com.fa.cim.tms.controller;

import com.alibaba.fastjson.JSON;
import com.fa.cim.tms.common.enums.TransactionIDEnum;
import com.fa.cim.tms.controller.interfaces.IN2PurgeReqController;
import com.fa.cim.tms.dto.Params;
import com.fa.cim.tms.dto.Response;
import com.fa.cim.tms.dto.Results;
import com.fa.cim.tms.dto.User;
import com.fa.cim.tms.method.IUtilsComp;
import com.fa.cim.tms.pojo.Infos;
import com.fa.cim.tms.service.IN2PurgeReqService;
import com.fa.cim.tms.utils.ThreadContextHolder;
import com.fa.cim.tms.utils.Validations;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
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
 * @date: 2020/10/20 13:32
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@RestController
@RequestMapping("/tms")
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class N2PurgeReqController implements IN2PurgeReqController {
    @Autowired
    private IUtilsComp utilsComp;
    @Autowired
    private IN2PurgeReqService n2PurgeReqService;

    @RequestMapping(value = "/n2_purge/req", method = RequestMethod.POST)
    public Response tmsN2PurgeReq(@RequestBody Params.N2PurgeReqParams n2PurgeReqParams) {
        log.info("tmsN2PurgeReq Request Json" + JSON.toJSONString(n2PurgeReqParams));
        Results.N2PurgeReqResult result = new Results.N2PurgeReqResult();
        final String transactionID = TransactionIDEnum.OM13.getValue();
        ThreadContextHolder.setTransactionId(transactionID);
        User user = n2PurgeReqParams.getRequestUserID();
        Validations.check(null == user, "the user info is null...");
        Infos.ObjCommon objCommon = utilsComp.setObjCommon(transactionID, user);

        log.info("【step1】 - n2PurgeReqService.sxN2PurgeReq");
        result = n2PurgeReqService.sxN2PurgeReq(objCommon, n2PurgeReqParams);
        log.info("tmsN2PurgeReq Response Json" + JSON.toJSONString(result));
        return Response.createSucc(result);
    }
    //RTMS Interface is same as TMS
}
