package com.fa.cim.tms.controller;

import com.alibaba.fastjson.JSON;
import com.fa.cim.tms.common.enums.TransactionIDEnum;
import com.fa.cim.tms.controller.interfaces.ICarrierInfoChangeReqController;
import com.fa.cim.tms.dto.Params;
import com.fa.cim.tms.dto.Response;
import com.fa.cim.tms.dto.Results;
import com.fa.cim.tms.dto.User;
import com.fa.cim.tms.method.IUtilsComp;
import com.fa.cim.tms.pojo.Infos;
import com.fa.cim.tms.service.ICarrierInfoChangeReqService;
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
 * @date: 2020/10/22 13:49
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@RestController
@Slf4j
@RequestMapping("/tms")
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class CarrierInfoChangeReqController implements ICarrierInfoChangeReqController {

    @Autowired
    private IUtilsComp utilsComp;
    @Autowired
    private ICarrierInfoChangeReqService carrierInfoChangeReqService;

    @RequestMapping(value = "/carrier_info_change/Req", method = RequestMethod.POST)
    public Response tmsCarrierInfoChangeReq(@RequestBody Params.CarrierInfoChangeReqParam carrierInfoChangeReqParam) {
        log.info("tmsCarrierInfoChangeReq Request Json" + JSON.toJSONString(carrierInfoChangeReqParam));
        Results.CarrierInfoChangeReqResult result = new Results.CarrierInfoChangeReqResult();
        final String transcationID = TransactionIDEnum.OM15.getValue();
        ThreadContextHolder.setTransactionId(transcationID);
        User user = carrierInfoChangeReqParam.getRequestUserID();
        Validations.check(null == user, "the user info is null...");
        Infos.ObjCommon objCommon = utilsComp.setObjCommon(transcationID, user);

        log.info("【step1】 - carrierInfoChangeReqService.sxCarrierInfoChangeReq");
        result = carrierInfoChangeReqService.sxCarrierInfoChangeReq(objCommon, carrierInfoChangeReqParam);
        log.info("tmsCarrierInfoChangeReq Response json" + JSON.toJSONString(result));
        return Response.createSucc(result);
    }
    //RTMS Interface is same as TMS
}
