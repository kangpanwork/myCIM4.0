package com.fa.cim.tms.controller;

import com.alibaba.fastjson.JSON;
import com.fa.cim.tms.common.enums.TransactionIDEnum;
import com.fa.cim.tms.controller.interfaces.IAccessControlCheckInqController;
import com.fa.cim.tms.dto.Params;
import com.fa.cim.tms.dto.Response;
import com.fa.cim.tms.dto.Results;
import com.fa.cim.tms.dto.User;
import com.fa.cim.tms.method.IUtilsComp;
import com.fa.cim.tms.pojo.Infos;
import com.fa.cim.tms.service.IAccessControlCheckInqService;
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
@RequestMapping("/tms")
public class AccessControlCheckInqController implements IAccessControlCheckInqController {
    @Autowired
    private IUtilsComp utilsComp;
    @Autowired
    private IAccessControlCheckInqService accessControlCheckInqService;

    @RequestMapping(value = "/access_controller_check/inq", method = RequestMethod.POST)
    public Response tmsAccessControlCheckInq(@RequestBody Params.AccessControlCheckInqParam accessControlCheckInqParam) {
        log.info("tmsAccessControlCheckInq Request Json" + JSON.toJSONString(accessControlCheckInqParam));
        Results.AccessControlCheckInqResult result = new Results.AccessControlCheckInqResult();
        final String transactionID = TransactionIDEnum.TM08.getValue();
        ThreadContextHolder.setTransactionId(transactionID);
        User user = accessControlCheckInqParam.getRequestUserID();
        Validations.check(null == user, "the user info is null...");
        Infos.ObjCommon objCommon = utilsComp.setObjCommon(transactionID, user);

        log.info("【step1】 - accessControlCheckInqService.sxAccessControlCheckInq");
        result = accessControlCheckInqService.sxAccessControlCheckInq(objCommon, accessControlCheckInqParam);
        log.info("tmsAccessControlCheckInq Response json" + JSON.toJSONString(result));
        return Response.createSucc(result);
    }
    //RTMS Interface is same as TMS
}
