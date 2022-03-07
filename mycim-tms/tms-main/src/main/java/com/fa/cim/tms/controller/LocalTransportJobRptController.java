package com.fa.cim.tms.controller;

import com.alibaba.fastjson.JSON;
import com.fa.cim.tms.common.enums.TransactionIDEnum;
import com.fa.cim.tms.controller.interfaces.ILocalTransportJobRptController;
import com.fa.cim.tms.dto.Params;
import com.fa.cim.tms.dto.Response;
import com.fa.cim.tms.dto.User;
import com.fa.cim.tms.method.IUtilsComp;
import com.fa.cim.tms.pojo.Infos;
import com.fa.cim.tms.service.IAccessControlCheckInqService;
import com.fa.cim.tms.service.ILocalTransportJobRptService;
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
public class LocalTransportJobRptController implements ILocalTransportJobRptController {

    @Autowired
    private IUtilsComp utilsComp;
    @Autowired
    private ILocalTransportJobRptService localTransportJobRptService;
    @Autowired
    private IAccessControlCheckInqService accessControlCheckInqService;

    @RequestMapping(value = "/local_transport_job/rpt", method = RequestMethod.POST)
    public Response tmsLocalTransportJobRpt(@RequestBody Params.LocalTransportJobReqParams localTransportJobReqParams) {
        log.info("tmsLocalTransportJobRpt Request Json" + JSON.toJSONString(localTransportJobReqParams));
        final String transactionID = TransactionIDEnum.TM15.getValue();
        ThreadContextHolder.setTransactionId(transactionID);
        User user = localTransportJobReqParams.getRequestUserID();
        Validations.check(null == user, "the user info is null...");
        Validations.check(ArrayUtils.isEmpty(localTransportJobReqParams.getLocalTransportJobData()), "the list info is null");
        Infos.ObjCommon objCommon = utilsComp.setObjCommon(transactionID, user);
        //check access privilege
        Params.AccessControlCheckInqParam accessControlCheckInqParam = new Params.AccessControlCheckInqParam();
        log.info("【step1】 - accessControlCheckInqService.sxAccessControlCheckInq");
        accessControlCheckInqService.sxAccessControlCheckInq(objCommon,accessControlCheckInqParam);

        log.info("【step2】 - localTransportJobRptService.sxLocalTransportJobRpt");
        localTransportJobRptService.sxLocalTransportJobRpt(objCommon, localTransportJobReqParams);
        log.info("tmsLocalTransportJobRpt Response json" + null);
        return Response.createSucc(transactionID, null);
    }
    //RTMS Interface is same as TMS
}
