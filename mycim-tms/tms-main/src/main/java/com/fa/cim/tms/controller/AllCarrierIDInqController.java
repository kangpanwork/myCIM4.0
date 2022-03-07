package com.fa.cim.tms.controller;

import com.alibaba.fastjson.JSON;
import com.fa.cim.tms.common.enums.TransactionIDEnum;
import com.fa.cim.tms.controller.interfaces.IAllCarrierIDInqController;
import com.fa.cim.tms.dto.Params;
import com.fa.cim.tms.dto.Response;
import com.fa.cim.tms.dto.Results;
import com.fa.cim.tms.dto.User;
import com.fa.cim.tms.method.IUtilsComp;
import com.fa.cim.tms.pojo.Infos;
import com.fa.cim.tms.service.IAccessControlCheckInqService;
import com.fa.cim.tms.service.IAllCarrierIDInqService;
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
 * 2020/10/22                               Neyo                create file
 *
 * @author: Neyo
 * @date: 2020/10/22 13:49
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@RestController
public class AllCarrierIDInqController implements IAllCarrierIDInqController {
    @Autowired
    private IUtilsComp utilsComp;
    @Autowired
    private IAllCarrierIDInqService allCarrierIDInqService;
    @Autowired
    private IAccessControlCheckInqService accessControlCheckInqService;

    @RequestMapping(value = "/tms/all_carrier_id/inq", method = RequestMethod.POST)
    public Response tmsAllCarrierIDInq(@RequestBody Params.AllCarrierIDInquiryParam allCarrierIDInquiryParam) {
        Results.AllCarrierIDInquiryResult result = new Results.AllCarrierIDInquiryResult();
        log.info("tmsAllCarrierIDInq Request Josn" + JSON.toJSONString(allCarrierIDInquiryParam));
        final String transcationID = TransactionIDEnum.TM14.getValue();
        ThreadContextHolder.setTransactionId(transcationID);
        User user = allCarrierIDInquiryParam.getRequestUserID();
        Validations.check(null == user, "the user info is null...");
        Infos.ObjCommon objCommon = utilsComp.setObjCommon(transcationID, user);

        //check access privilege
        Params.AccessControlCheckInqParam accessControlCheckInqParam = new Params.AccessControlCheckInqParam();
        log.info("【step1】 - accessControlCheckInqService.sxAccessControlCheckInq");
        accessControlCheckInqService.sxAccessControlCheckInq(objCommon,accessControlCheckInqParam);

        log.info("【step2】 - allCarrierIDInqService.sxAllCarrierIDInq");
        result = allCarrierIDInqService.sxAllCarrierIDInq(objCommon, allCarrierIDInquiryParam);
        log.info("tmsAllCarrierIDInq Response json /n" + JSON.toJSONString(result));
        return Response.createSucc(result);
    }

    @RequestMapping(value = "/rtms/all_carrier_id/inq", method = RequestMethod.POST)
    public Response rtmsAllCarrierIDInq(@RequestBody Params.AllCarrierIDInquiryParam allCarrierIDInquiryParam) {
        Results.AllCarrierIDInquiryResult result = new Results.AllCarrierIDInquiryResult();
        log.info("rtmsAllCarrierIDInq Request Josn" + JSON.toJSONString(allCarrierIDInquiryParam));
        final String transcationID = TransactionIDEnum.RTM14.getValue();
        ThreadContextHolder.setTransactionId(transcationID);
        User user = allCarrierIDInquiryParam.getRequestUserID();
        Validations.check(null == user, "the user info is null...");
        Infos.ObjCommon objCommon = utilsComp.setObjCommon(transcationID, user);

        //check access privilege
        Params.AccessControlCheckInqParam accessControlCheckInqParam = new Params.AccessControlCheckInqParam();
        log.info("【step1】 - accessControlCheckInqService.sxAccessControlCheckInq");
        accessControlCheckInqService.sxAccessControlCheckInq(objCommon,accessControlCheckInqParam);

        log.info("【step2】 - allCarrierIDInqService.sxRtmsAllCarrierIDInq");
        result = allCarrierIDInqService.sxRtmsAllCarrierIDInq(objCommon, allCarrierIDInquiryParam);
        log.info("rtmsAllCarrierIDInq Response json /n" + JSON.toJSONString(result));
        return Response.createSucc(result);
    }
}
