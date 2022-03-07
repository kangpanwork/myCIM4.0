package com.fa.cim.tms.controller;

import com.alibaba.fastjson.JSON;
import com.fa.cim.tms.common.enums.TransactionIDEnum;
import com.fa.cim.tms.controller.interfaces.IAlarmReportController;
import com.fa.cim.tms.dto.Params;
import com.fa.cim.tms.dto.Response;
import com.fa.cim.tms.dto.Results;
import com.fa.cim.tms.dto.User;
import com.fa.cim.tms.method.IUtilsComp;
import com.fa.cim.tms.pojo.Infos;
import com.fa.cim.tms.service.IAccessControlCheckInqService;
import com.fa.cim.tms.service.IAlarmRptService;
import com.fa.cim.tms.utils.ThreadContextHolder;
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
@Slf4j
@RestController
@RequestMapping("/tms")
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class AlarmReportController implements IAlarmReportController {
    @Autowired
    private IUtilsComp utilsComp;
    @Autowired
    private IAlarmRptService alarmRptService;
    @Autowired
    private IAccessControlCheckInqService accessControlCheckInqService;

    @RequestMapping(value = "/alarm/rpt", method = RequestMethod.POST)
    public Response tmsAlarmRpt(@RequestBody Params.AlarmReportParam alarmReportParam) {
        log.info("tmsAlarmRpt Request Json" + JSON.toJSONString(alarmReportParam));
        Results.AlarmReportResult result = new Results.AlarmReportResult();
        final String transcationID = TransactionIDEnum.TM10.getValue();
        ThreadContextHolder.setTransactionId(transcationID);
        User user = alarmReportParam.getRequestUserID();
        Infos.ObjCommon objCommon = utilsComp.setObjCommon(transcationID, user);

        //check access privilege
        Params.AccessControlCheckInqParam accessControlCheckInqParam = new Params.AccessControlCheckInqParam();
        log.info("【step1】 - accessControlCheckInqService.sxAccessControlCheckInq");
        accessControlCheckInqService.sxAccessControlCheckInq(objCommon,accessControlCheckInqParam);

        log.info("【step2】 - alarmRptService.sxAlarmReport");
        result = alarmRptService.sxAlarmReport(objCommon, alarmReportParam);
        log.info("AlarmReportController Response json /n" + JSON.toJSONString(result));
        return Response.createSucc(result);
    }
    //RTMS Interface is same as TMS
}
