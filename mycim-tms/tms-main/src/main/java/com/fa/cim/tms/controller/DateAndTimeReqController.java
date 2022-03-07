package com.fa.cim.tms.controller;

import com.alibaba.fastjson.JSON;
import com.fa.cim.tms.common.enums.TransactionIDEnum;
import com.fa.cim.tms.controller.interfaces.IDateAndTimeReqController;
import com.fa.cim.tms.dto.Params;
import com.fa.cim.tms.dto.Response;
import com.fa.cim.tms.dto.Results;
import com.fa.cim.tms.dto.User;
import com.fa.cim.tms.method.IUtilsComp;
import com.fa.cim.tms.pojo.Infos;
import com.fa.cim.tms.service.IAccessControlCheckInqService;
import com.fa.cim.tms.service.IDateAndTimeReqService;
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
 * 2020/10/22                               Neyo                create file
 *
 * @author: Neyo
 * @date: 2020/10/22 14:15
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@RestController
@RequestMapping("/tms")
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class DateAndTimeReqController implements IDateAndTimeReqController {
    @Autowired
    private IUtilsComp utilsComp;
    @Autowired
    private IDateAndTimeReqService dateAndTimeReqService;
    @Autowired
    private IAccessControlCheckInqService accessControlCheckInqService;

    @RequestMapping(value = "/date_and_time/req", method = RequestMethod.POST)
    public Response tmsDateAndTimeReq(@RequestBody Params.DateAndTimeReqRestParmas dateAndTimeReqRestParmas) {
        log.info("tmsDateAndTimeReq Request Json" + JSON.toJSONString(dateAndTimeReqRestParmas));
        Results.DateAndTimeReqResult result = new Results.DateAndTimeReqResult();
        final String transactionID = TransactionIDEnum.TM11.getValue();
        ThreadContextHolder.setTransactionId(transactionID);
        User user = dateAndTimeReqRestParmas.getRequestUserID();
        Validations.check(null == user, "the user info is null...");
        Infos.ObjCommon objCommon = utilsComp.setObjCommon(transactionID, user);

        //check access privilege
        Params.AccessControlCheckInqParam accessControlCheckInqParam = new Params.AccessControlCheckInqParam();
        log.info("【step1】 - accessControlCheckInqService.sxAccessControlCheckInq");
        accessControlCheckInqService.sxAccessControlCheckInq(objCommon,accessControlCheckInqParam);

        log.info("【step2】 - dateAndTimeReqService.sxDateAndTimeReq");
        result = dateAndTimeReqService.sxDateAndTimeReq(objCommon);
        log.info("tmsDateAndTimeReq Response json" + JSON.toJSONString(result));
        return Response.createSucc(result);
    }
    //RTMS Interface is same as TMS
}
