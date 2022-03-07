package com.fa.cim.tms.controller;

import com.alibaba.fastjson.JSON;
import com.fa.cim.tms.common.enums.TransactionIDEnum;
import com.fa.cim.tms.controller.interfaces.IPriorityChangeReqController;
import com.fa.cim.tms.dto.Params;
import com.fa.cim.tms.dto.Response;
import com.fa.cim.tms.dto.Results;
import com.fa.cim.tms.dto.User;
import com.fa.cim.tms.method.IUtilsComp;
import com.fa.cim.tms.pojo.Infos;
import com.fa.cim.tms.service.IPriorityChangeReqService;
import com.fa.cim.tms.utils.ArrayUtils;
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

import java.util.List;

/**
 * description:
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/10/20                               Neyo                create file
 *
 * @author: Neyo
 * @date: 2020/10/20 15:34
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@RestController
@RequestMapping("/tms")
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class PriorityChangeReqController implements IPriorityChangeReqController {
    @Autowired
    private IUtilsComp utilsComp;
    @Autowired
    private IPriorityChangeReqService priorityChangeReqService;

    @RequestMapping(value = "/priority_change/req", method = RequestMethod.POST)
    public Response tmsPriorityChangeReq(@RequestBody Params.PriorityChangeReqParam priorityChangeReqParam) {
        log.info("tmsPriorityChangeReq Request Json" + JSON.toJSONString(priorityChangeReqParam));
        Results.PriorityChangeReqResult result = new Results.PriorityChangeReqResult();
        final String transactionID = TransactionIDEnum.OM12.getValue();
        ThreadContextHolder.setTransactionId(transactionID);
        User user = priorityChangeReqParam.getRequestUserID();
        Validations.check(null == user, "the user info is null...");
        List<Infos.PriorityInfo> priorityInfoData = priorityChangeReqParam.getPriorityInfoData();
        Validations.check(ArrayUtils.isEmpty(priorityInfoData), "the priorityInfoData info is null...");
        Infos.ObjCommon objCommon = utilsComp.setObjCommon(transactionID, user);

        log.info("【step1】 - priorityChangeReqService.sxPriorityChangeReq");
        result = priorityChangeReqService.sxPriorityChangeReq(objCommon, priorityChangeReqParam);
        log.info("tmsPriorityChangeReq Response json" + JSON.toJSONString(result));
        return Response.createSucc(result);
    }
    //RTMS Interface is same as TMS
}
