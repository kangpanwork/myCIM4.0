package com.fa.cim.tms.controller;

import com.alibaba.fastjson.JSON;
import com.fa.cim.tms.common.enums.TransactionIDEnum;
import com.fa.cim.tms.controller.interfaces.IOnlineMcsInqController;
import com.fa.cim.tms.dto.Params;
import com.fa.cim.tms.dto.Response;
import com.fa.cim.tms.dto.User;
import com.fa.cim.tms.method.IUtilsComp;
import com.fa.cim.tms.pojo.Infos;
import com.fa.cim.tms.service.IAccessControlCheckInqService;
import com.fa.cim.tms.service.IOnlineMcsInqService;
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
 * @date: 2020/10/20 13:54
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@RestController
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class OnlineMcsInqController implements IOnlineMcsInqController {
    @Autowired
    private IUtilsComp utilsComp;
    @Autowired
    private IOnlineMcsInqService onlineMcsInqService;
    @Autowired
    private IAccessControlCheckInqService accessControlCheckInqService;

    @RequestMapping(value = "/tms/online_mcs/inq", method = RequestMethod.POST)
    public Response tmsOnlineMcsInq(@RequestBody Params.OnlineAmhsInqParams onlineAmhsInqParams) {
        log.info("tmsOnlineMcsInq Request Json" + JSON.toJSONString(onlineAmhsInqParams));
        final String transactionID = TransactionIDEnum.TM09.getValue();
        ThreadContextHolder.setTransactionId(transactionID);
        User user = onlineAmhsInqParams.getRequestUserID();
        Validations.check(null == user, "the user info is null...");
        Infos.ObjCommon objCommon = utilsComp.setObjCommon(transactionID, user);

        //check access privilege
        Params.AccessControlCheckInqParam accessControlCheckInqParam = new Params.AccessControlCheckInqParam();
        log.info("【step1】 - accessControlCheckInqService.sxAccessControlCheckInq");
        accessControlCheckInqService.sxAccessControlCheckInq(objCommon,accessControlCheckInqParam);

        log.info("【step2】 - onlineMcsInqService.sxOnlineMcsInq");
        onlineMcsInqService.sxOnlineMcsInq(objCommon);
        log.info("tmsOnlineMcsInq Response json" + null);
        return Response.createSucc(transactionID, null);
    }

    @RequestMapping(value = "/rtms/online_mcs/inq", method = RequestMethod.POST)
    public Response rtmsOnlineMcsInq(@RequestBody Params.OnlineAmhsInqParams onlineAmhsInqParams) {
        log.info("rtmsOnlineMcsInq Request Json" + JSON.toJSONString(onlineAmhsInqParams));
        final String transactionID = TransactionIDEnum.RTM09.getValue();
        ThreadContextHolder.setTransactionId(transactionID);
        User user = onlineAmhsInqParams.getRequestUserID();
        Validations.check(null == user, "the user info is null...");
        Infos.ObjCommon objCommon = utilsComp.setObjCommon(transactionID, user);

        //check access privilege
        Params.AccessControlCheckInqParam accessControlCheckInqParam = new Params.AccessControlCheckInqParam();
        log.info("【step1】 - accessControlCheckInqService.sxAccessControlCheckInq");
        accessControlCheckInqService.sxAccessControlCheckInq(objCommon,accessControlCheckInqParam);

        log.info("【step2】 - onlineMcsInqService.sxRtmsOnlineMcsInq");
        onlineMcsInqService.sxRtmsOnlineMcsInq(objCommon);
        log.info("rtmsOnlineMcsInq Response json" + null);
        return Response.createSucc(transactionID, null);
    }
}
