package com.fa.cim.tms.controller;

import com.alibaba.fastjson.JSON;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.tms.common.enums.TransactionIDEnum;
import com.fa.cim.tms.config.MsgRetCodeConfig;
import com.fa.cim.tms.controller.interfaces.ITransportJobStatusRptController;
import com.fa.cim.tms.dto.Params;
import com.fa.cim.tms.dto.Response;
import com.fa.cim.tms.dto.Results;
import com.fa.cim.tms.dto.User;
import com.fa.cim.tms.method.IUtilsComp;
import com.fa.cim.tms.pojo.Infos;
import com.fa.cim.tms.service.IAccessControlCheckInqService;
import com.fa.cim.tms.service.ITransportJobStatusRptService;
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

import java.util.List;

/**
 * description:
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/10/21                               Neyo                create file
 *
 * @author: Neyo
 * @date: 2020/10/21 13:44
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@RestController
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class TransportJobStatusRptController implements ITransportJobStatusRptController {
    @Autowired
    private IUtilsComp utilsComp;
    @Autowired
    private ITransportJobStatusRptService transportJobStatusRptService;
    @Autowired
    private IAccessControlCheckInqService accessControlCheckInqService;
    @Autowired
    private MsgRetCodeConfig msgRetCodeConfig;

    @RequestMapping(value = "/tms/transport_job_status/rpt", method = RequestMethod.POST)
    public Response tmsTransportJobStatusRpt(@RequestBody Params.TransportJobStatusReportParams transportJobStatusReportParams) {
        log.info("tmsTransportJobStatusRpt Request Json " + JSON.toJSONString(transportJobStatusReportParams));
        Results.TransportJobStatusReportResult result = new Results.TransportJobStatusReportResult();
        final String transactionID = TransactionIDEnum.TM03.getValue();
        ThreadContextHolder.setTransactionId(transactionID);
        User user = transportJobStatusReportParams.getRequestUserID();
        Validations.check(null == user, "the user info is null...");
        List<Infos.JobStatusReportArray> jobStatusReportData = transportJobStatusReportParams.getJobStatusReportData();
        Validations.check(ArrayUtils.isEmpty(jobStatusReportData), "the jobStatusReportData info is null...");
        Infos.ObjCommon objCommon = utilsComp.setObjCommon(transactionID, user);

        //check access privilege
        Params.AccessControlCheckInqParam accessControlCheckInqParam = new Params.AccessControlCheckInqParam();
        log.info("【step1】 - accessControlCheckInqService.sxAccessControlCheckInq");
        accessControlCheckInqService.sxAccessControlCheckInq(objCommon,accessControlCheckInqParam);

        log.info("【step2】 - transportJobStatusRptService.sxTransportJobStatusReport");
        try {
            result = transportJobStatusRptService.sxTransportJobStatusReport(objCommon, transportJobStatusReportParams);
        } catch (ServiceException e) {
            result = e.getData(Results.TransportJobStatusReportResult.class);
            if (Validations.isEquals(msgRetCodeConfig.getMsgReportFail(),e.getCode())){
                return Response.createSuccWithCode(transactionID,msgRetCodeConfig.getMsgReportFail(),result);
            }else if (Validations.isEquals(msgRetCodeConfig.getMsgOmsCastTxNoSend(),e.getCode())){
                return Response.createSuccWithCode(transactionID,msgRetCodeConfig.getMsgOmsCastTxNoSend(),result);
            }
            throw e;
        }
        log.info("tmsTransportJobStatusRpt Response json /n" + JSON.toJSONString(result));
        return Response.createSucc(result);
    }

    @RequestMapping(value = "/rtms/transport_job_status/rpt", method = RequestMethod.POST)
    public Response rtmsTransportJobStatusRpt(@RequestBody Params.TransportJobStatusReportParams transportJobStatusReportParams) {
        log.info("rtmsTransportJobStatusRpt Request Json " + JSON.toJSONString(transportJobStatusReportParams));
        Results.TransportJobStatusReportResult result = new Results.TransportJobStatusReportResult();
        final String transactionID = TransactionIDEnum.RTM03.getValue();
        ThreadContextHolder.setTransactionId(transactionID);
        User user = transportJobStatusReportParams.getRequestUserID();
        Validations.check(null == user, "the user info is null...");
        List<Infos.JobStatusReportArray> jobStatusReportData = transportJobStatusReportParams.getJobStatusReportData();
        Validations.check(ArrayUtils.isEmpty(jobStatusReportData), "the jobStatusReportData info is null...");
        Infos.ObjCommon objCommon = utilsComp.setObjCommon(transactionID, user);

        //check access privilege
        Params.AccessControlCheckInqParam accessControlCheckInqParam = new Params.AccessControlCheckInqParam();
        log.info("【step1】 - accessControlCheckInqService.sxAccessControlCheckInq");
        accessControlCheckInqService.sxAccessControlCheckInq(objCommon,accessControlCheckInqParam);

        log.info("【step2】 - transportJobStatusRptService.sxRtmsTransportJobStatusReport");
        result = transportJobStatusRptService.sxRtmsTransportJobStatusReport(objCommon, transportJobStatusReportParams);
        log.info("rtmsTransportJobStatusRpt Response json /n" + JSON.toJSONString(result));
        return Response.createSucc(result);
    }
}
