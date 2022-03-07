package com.fa.cim.tms.controller;

import com.alibaba.fastjson.JSON;
import com.fa.cim.tms.common.enums.TransactionIDEnum;
import com.fa.cim.tms.controller.interfaces.IEstimatedTransportTimeInqController;
import com.fa.cim.tms.dto.Params;
import com.fa.cim.tms.dto.Response;
import com.fa.cim.tms.dto.Results;
import com.fa.cim.tms.dto.User;
import com.fa.cim.tms.method.IUtilsComp;
import com.fa.cim.tms.pojo.Infos;
import com.fa.cim.tms.service.IAccessControlCheckInqService;
import com.fa.cim.tms.service.IEstimatedTransportTimeInqService;
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
 * @date: 2020/10/22 14:36
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@RestController
@RequestMapping("/tms")
public class EstimatedTransportTimeInqController implements IEstimatedTransportTimeInqController {
    @Autowired
    private IUtilsComp utilsComp;
    @Autowired
    private IEstimatedTransportTimeInqService estimatedTransportTimeInqService;
    @Autowired
    private IAccessControlCheckInqService accessControlCheckInqService;

    @RequestMapping(value = "/estimated_transport_time/inq", method = RequestMethod.POST)
    public Response tmsEstimatedTransportTimeInq(@RequestBody Params.EstimatedTransportTimeInqParams estimatedTransportTimeInqParams) {
        log.info("tmsEstimatedTransportTimeInq Request Json" + JSON.toJSONString(estimatedTransportTimeInqParams));
        Results.EstimatedTransportTimeInqResult result = new Results.EstimatedTransportTimeInqResult();
        final String transactionID = TransactionIDEnum.OM16.getValue();
        ThreadContextHolder.setTransactionId(transactionID);
        User user = estimatedTransportTimeInqParams.getRequestUserID();
        Validations.check(null == user, "the user info is null...");
        Infos.ObjCommon objCommon = utilsComp.setObjCommon(transactionID, user);
        //check access privilege
        Params.AccessControlCheckInqParam accessControlCheckInqParam = new Params.AccessControlCheckInqParam();
        log.info("【step1】 - accessControlCheckInqService.sxAccessControlCheckInq");
        accessControlCheckInqService.sxAccessControlCheckInq(objCommon,accessControlCheckInqParam);

        log.info("【step2】 - estimatedTransportTimeInqService.sxEstimatedTransportTimeInq");
        result = estimatedTransportTimeInqService.sxEstimatedTransportTimeInq(objCommon, estimatedTransportTimeInqParams);
        log.info("tmsEstimatedTransportTimeInq Response json" + JSON.toJSONString(result));
        return Response.createSucc(result);
    }
    //RTMS Interface is same as TMS
}
