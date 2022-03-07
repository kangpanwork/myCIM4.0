package com.fa.cim.tms.controller;

import com.alibaba.fastjson.JSON;
import com.fa.cim.tms.common.enums.TransactionIDEnum;
import com.fa.cim.tms.controller.interfaces.ISubComponentStatusRptController;
import com.fa.cim.tms.dto.Params;
import com.fa.cim.tms.dto.Response;
import com.fa.cim.tms.dto.Results;
import com.fa.cim.tms.dto.User;
import com.fa.cim.tms.method.IUtilsComp;
import com.fa.cim.tms.pojo.Infos;
import com.fa.cim.tms.service.IAccessControlCheckInqService;
import com.fa.cim.tms.service.ISubComponentStatusRptService;
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
 * @date: 2020/10/20 15:46
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@RestController
@RequestMapping("/tms")
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class SubComponentStatusRptController implements ISubComponentStatusRptController {
    @Autowired
    private IUtilsComp utilsComp;
    @Autowired
    private ISubComponentStatusRptService subComponentStatusRptService;
    @Autowired
    private IAccessControlCheckInqService accessControlCheckInqService;

    @RequestMapping(value = "/sub_component_status/rpt", method = RequestMethod.POST)
    public Response tmsSubComponentStatusRpt(@RequestBody Params.SubComponentStatusReportParam subComponentStatusReportParam) {
        log.info("tmsSubComponentStatusRpt Request Json" + JSON.toJSONString(subComponentStatusReportParam));
        Results.SubComponentStatusReportResult result = new Results.SubComponentStatusReportResult();
        final String transactionID = TransactionIDEnum.TM12.getValue();
        ThreadContextHolder.setTransactionId(transactionID);
        User user = subComponentStatusReportParam.getRequestUserID();
        Validations.check(null == user, "the user info is null...");
        Infos.ObjCommon objCommon = utilsComp.setObjCommon(transactionID, user);

        //check access privilege
        Params.AccessControlCheckInqParam accessControlCheckInqParam = new Params.AccessControlCheckInqParam();
        log.info("【step1】 - accessControlCheckInqService.sxAccessControlCheckInq");
        accessControlCheckInqService.sxAccessControlCheckInq(objCommon,accessControlCheckInqParam);

        log.info("【step2】 - subComponentStatusRptService.sxSubComponentStatusReport");
        result = subComponentStatusRptService.sxSubComponentStatusReport(objCommon, subComponentStatusReportParam);
        log.info("tmsSubComponentStatusRpt Response json" + JSON.toJSONString(result));
        return Response.createSucc(result);
    }
    //RTMS Interface is same as TMS
}
