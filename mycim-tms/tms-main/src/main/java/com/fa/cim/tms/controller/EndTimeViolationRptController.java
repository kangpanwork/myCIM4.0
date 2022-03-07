package com.fa.cim.tms.controller;

import com.alibaba.fastjson.JSON;
import com.fa.cim.tms.common.enums.TransactionIDEnum;
import com.fa.cim.tms.controller.interfaces.IEndTimeViolationRptController;
import com.fa.cim.tms.dto.Params;
import com.fa.cim.tms.dto.Response;
import com.fa.cim.tms.dto.Results;
import com.fa.cim.tms.dto.User;
import com.fa.cim.tms.method.IUtilsComp;
import com.fa.cim.tms.pojo.Infos;
import com.fa.cim.tms.service.IAccessControlCheckInqService;
import com.fa.cim.tms.service.IEndTimeViolationRptService;
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
 * @date: 2020/10/22 14:23
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@RestController
@Slf4j
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class EndTimeViolationRptController implements IEndTimeViolationRptController {
    @Autowired
    private IUtilsComp utilsComp;
    @Autowired
    private IEndTimeViolationRptService endTimeViolationRptService;
    @Autowired
    private IAccessControlCheckInqService accessControlCheckInqService;

    @RequestMapping(value = "/tms/endtime_violation/rpt", method = RequestMethod.POST)
    public Response tmsEndTimeViolationRpt(@RequestBody Params.EndTimeViolationReportParam endTimeViolationReportParam) {
        log.info("tmsEndTimeViolationRpt Request Json" + JSON.toJSONString(endTimeViolationReportParam));
        Results.EndTimeViolationReportResult result = new Results.EndTimeViolationReportResult();
        final String transactionID = TransactionIDEnum.TM04.getValue();
        ThreadContextHolder.setTransactionId(transactionID);
        User user = endTimeViolationReportParam.getRequestUserID();
        Validations.check(null == user, "the user info is null...");
        Infos.ObjCommon objCommon = utilsComp.setObjCommon(transactionID, user);

        //check access privilege
        Params.AccessControlCheckInqParam accessControlCheckInqParam = new Params.AccessControlCheckInqParam();
        log.info("【step1】 - accessControlCheckInqService.sxAccessControlCheckInq");
        accessControlCheckInqService.sxAccessControlCheckInq(objCommon,accessControlCheckInqParam);

        log.info("【step2】 - endTimeViolationRptService.sxEndTimeViolationReport");
        result = endTimeViolationRptService.sxEndTimeViolationReport(objCommon, endTimeViolationReportParam);
        log.info("tmsEndTimeViolationRpt Response Json" + JSON.toJSONString(result));
        return Response.createSucc(result);
    }

    @RequestMapping(value = "/rtms/endtime_violation/rpt", method = RequestMethod.POST)
    public Response rtmsEndTimeViolationRpt(@RequestBody Params.EndTimeViolationReportParam endTimeViolationReportParam) {
        log.info("rtmsEndTimeViolationRpt Request Json" + JSON.toJSONString(endTimeViolationReportParam));
        Results.EndTimeViolationReportResult result = new Results.EndTimeViolationReportResult();
        final String transactionID = TransactionIDEnum.RTM04.getValue();
        ThreadContextHolder.setTransactionId(transactionID);
        User user = endTimeViolationReportParam.getRequestUserID();
        Validations.check(null == user, "the user info is null...");
        Infos.ObjCommon objCommon = utilsComp.setObjCommon(transactionID, user);

        //check access privilege
        Params.AccessControlCheckInqParam accessControlCheckInqParam = new Params.AccessControlCheckInqParam();
        log.info("【step1】 - accessControlCheckInqService.sxAccessControlCheckInq");
        accessControlCheckInqService.sxAccessControlCheckInq(objCommon,accessControlCheckInqParam);

        log.info("【step2】 - endTimeViolationRptService.sxRtmsEndTimeViolationReport");
        result = endTimeViolationRptService.sxRtmsEndTimeViolationReport(objCommon, endTimeViolationReportParam);
        log.info("rtmsEndTimeViolationRpt Response Json" + JSON.toJSONString(result));
        return Response.createSucc(result);
    }
}
