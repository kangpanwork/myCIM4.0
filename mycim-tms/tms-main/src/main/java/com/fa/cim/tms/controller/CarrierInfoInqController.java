package com.fa.cim.tms.controller;

import com.alibaba.fastjson.JSON;
import com.fa.cim.tms.common.enums.TransactionIDEnum;
import com.fa.cim.tms.controller.interfaces.ICarrierInfoInqController;
import com.fa.cim.tms.dto.Params;
import com.fa.cim.tms.dto.Response;
import com.fa.cim.tms.dto.Results;
import com.fa.cim.tms.dto.User;
import com.fa.cim.tms.method.IUtilsComp;
import com.fa.cim.tms.pojo.Infos;
import com.fa.cim.tms.service.IAccessControlCheckInqService;
import com.fa.cim.tms.service.ICarrierInfoInqService;
import com.fa.cim.tms.utils.ThreadContextHolder;
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
public class CarrierInfoInqController implements ICarrierInfoInqController {
    @Autowired
    private IUtilsComp utilsComp;
    @Autowired
    private ICarrierInfoInqService carrierInfoInqService;
    @Autowired
    private IAccessControlCheckInqService accessControlCheckInqService;

    @RequestMapping(value = "/tms/carrier_info/inq", method = RequestMethod.POST)
    public Response tmsCarrierInfoInq(@RequestBody Params.CarrierInfoInqParam carrierInfoInqParam) {
        log.info("tmsCarrierInfoInq Request Json" + JSON.toJSONString(carrierInfoInqParam));
        Results.CarrierInfoInqResult result = new Results.CarrierInfoInqResult();
        final String transactionID = TransactionIDEnum.TM07.getValue();
        ThreadContextHolder.setTransactionId(transactionID);
        User user = carrierInfoInqParam.getRequestUserID();
        Infos.ObjCommon objCommon = utilsComp.setObjCommon(transactionID, user);

        //check access privilege
        Params.AccessControlCheckInqParam accessControlCheckInqParam = new Params.AccessControlCheckInqParam();
        log.info("【step1】 - accessControlCheckInqService.sxAccessControlCheckInq");
        accessControlCheckInqService.sxAccessControlCheckInq(objCommon,accessControlCheckInqParam);

        log.info("【step2】 - carrierInfoInqService.sxCarrierInfoInq");
        result = carrierInfoInqService.sxCarrierInfoInq(objCommon, carrierInfoInqParam);
        log.info("tmsCarrierInfoInq Response json" + JSON.toJSONString(result));
        return Response.createSucc(result);
    }

    @RequestMapping(value = "/rtms/carrier_info/inq", method = RequestMethod.POST)
    public Response rtmsCarrierInfoInq(@RequestBody Params.CarrierInfoInqParam carrierInfoInqParam) {
        log.info("rtmsCarrierInfoInq Request Json" + JSON.toJSONString(carrierInfoInqParam));
        Results.CarrierInfoInqResult result = new Results.CarrierInfoInqResult();
        final String transactionID = TransactionIDEnum.RTM07.getValue();
        ThreadContextHolder.setTransactionId(transactionID);
        User user = carrierInfoInqParam.getRequestUserID();
        Infos.ObjCommon objCommon = utilsComp.setObjCommon(transactionID, user);

        //check access privilege
        Params.AccessControlCheckInqParam accessControlCheckInqParam = new Params.AccessControlCheckInqParam();
        log.info("【step1】 - accessControlCheckInqService.sxAccessControlCheckInq");
        accessControlCheckInqService.sxAccessControlCheckInq(objCommon,accessControlCheckInqParam);

        log.info("【step2】 - carrierInfoInqService.sxRtmsCarrierInfoInq");
        result = carrierInfoInqService.sxRtmsCarrierInfoInq(objCommon, carrierInfoInqParam);
        log.info("rtmsCarrierInfoInq Response json" + JSON.toJSONString(result));
        return Response.createSucc(result);
    }
}
