package com.fa.cim.tms.controller;

import com.alibaba.fastjson.JSON;
import com.fa.cim.tms.common.enums.TransactionIDEnum;
import com.fa.cim.tms.controller.interfaces.IOnlineHostInqController;
import com.fa.cim.tms.dto.Params;
import com.fa.cim.tms.dto.Response;
import com.fa.cim.tms.dto.User;
import com.fa.cim.tms.method.IUtilsComp;
import com.fa.cim.tms.pojo.Infos;
import com.fa.cim.tms.service.IOnlineHostInqService;
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
 * @date: 2020/10/20 14:51
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@RestController
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class OnlineHostInqController implements IOnlineHostInqController {
    @Autowired
    private IUtilsComp utilsComp;
    @Autowired
    private IOnlineHostInqService onlineHostInqService;

    @RequestMapping(value = "/tms/online_host/inq", method = RequestMethod.POST)
    public Response tmsOnlineHostInq(@RequestBody Params.OnlineHostInqParam onlineHostInqParam) {
        log.info("tmsOnlineHostInq Request Json" + JSON.toJSONString(onlineHostInqParam));
        final String transactionID = TransactionIDEnum.OM11.getValue();
        ThreadContextHolder.setTransactionId(transactionID);
        User user = onlineHostInqParam.getRequestUserID();
        Validations.check(null == user, "the user info is null...");
        Infos.ObjCommon objCommon = utilsComp.setObjCommon(transactionID, user);

        log.info("【step1】 - onlineHostInqService.sxOnlineHostInq");
        onlineHostInqService.sxOnlineHostInq(objCommon, onlineHostInqParam);
        log.info("tmsOnlineHostInq Response json" + null);
        return Response.createSucc(transactionID, null);

    }

    @RequestMapping(value = "/rtms/online_host/inq", method = RequestMethod.POST)
    public Response rtmsOnlineHostInq(@RequestBody Params.OnlineHostInqParam onlineHostInqParam) {
        log.info("rtmsOnlineHostInq Request Json" + JSON.toJSONString(onlineHostInqParam));
        final String transactionID = TransactionIDEnum.ROM11.getValue();
        ThreadContextHolder.setTransactionId(transactionID);
        User user = onlineHostInqParam.getRequestUserID();
        Validations.check(null == user, "the user info is null...");
        Infos.ObjCommon objCommon = utilsComp.setObjCommon(transactionID, user);

        log.info("【step1】 - onlineHostInqService.sxRtmsOnlineHostInq");
        onlineHostInqService.sxRtmsOnlineHostInq(objCommon, onlineHostInqParam);
        log.info("rtmsOnlineHostInq Response json" + null);
        return Response.createSucc(transactionID, null);

    }
}
