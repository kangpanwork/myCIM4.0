package com.fa.cim.tms.controller;

import com.alibaba.fastjson.JSON;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.tms.common.enums.TransactionIDEnum;
import com.fa.cim.tms.config.MsgRetCodeConfig;
import com.fa.cim.tms.controller.interfaces.ICarrierStatusRptController;
import com.fa.cim.tms.dto.Params;
import com.fa.cim.tms.dto.Response;
import com.fa.cim.tms.dto.Results;
import com.fa.cim.tms.dto.User;
import com.fa.cim.tms.method.IUtilsComp;
import com.fa.cim.tms.pojo.Infos;
import com.fa.cim.tms.service.IAccessControlCheckInqService;
import com.fa.cim.tms.service.ICarrierStatusRptService;
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
@Slf4j
@RestController
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class CarrierStatusRptController implements ICarrierStatusRptController {
    @Autowired
    private IUtilsComp utilsComp;
    @Autowired
    private ICarrierStatusRptService carrierStatusRptService;
    @Autowired
    private IAccessControlCheckInqService accessControlCheckInqService;
    @Autowired
    private MsgRetCodeConfig msgRetCodeConfig;

    @RequestMapping(value = "/tms/carrier_status/rpt", method = RequestMethod.POST)
    public Response tmsCarrierStatusRpt(@RequestBody Params.CarrierStatusReportParam carrierStatusReportParam) {
        log.info("tmsCarrierStatusRpt Request Json" + JSON.toJSONString(carrierStatusReportParam));
        Results.CarrierStatusReportResult result = new Results.CarrierStatusReportResult();
        final String transactionID = TransactionIDEnum.TM02.getValue();
        ThreadContextHolder.setTransactionId(transactionID);
        User user = carrierStatusReportParam.getRequestUserID();
        Infos.ObjCommon objCommon = utilsComp.setObjCommon(transactionID, user);
        //check access privilege
        Params.AccessControlCheckInqParam accessControlCheckInqParam = new Params.AccessControlCheckInqParam();
        log.info("【step1】 - accessControlCheckInqService.sxAccessControlCheckInq");
        accessControlCheckInqService.sxAccessControlCheckInq(objCommon,accessControlCheckInqParam);

        log.info("【step2】 - carrierStatusRptService.sxCarrierStatusReport");
        try {
            result = carrierStatusRptService.sxCarrierStatusReport(objCommon, carrierStatusReportParam);
        } catch (ServiceException e) {
            result = e.getData(Results.CarrierStatusReportResult.class);
            if (Validations.isEquals(msgRetCodeConfig.getMsgOmsCastTxNoSend(),e.getCode())){
                return Response.createSuccWithCode(transactionID,msgRetCodeConfig.getMsgOmsCastTxNoSend(),result);
            }
            throw e;
        }
        log.info("tmsCarrierStatusRpt Response json" + JSON.toJSONString(result));
        return Response.createSucc(result);
    }

    @RequestMapping(value = "/rtms/carrier_status/rpt", method = RequestMethod.POST)
    public Response rtmsCarrierStatusRpt(@RequestBody Params.CarrierStatusReportParam carrierStatusReportParam) {
        log.info("rtmsCarrierStatusRpt Request Json" + JSON.toJSONString(carrierStatusReportParam));
        Results.CarrierStatusReportResult result = new Results.CarrierStatusReportResult();
        final String transactionID = TransactionIDEnum.RTM02.getValue();
        ThreadContextHolder.setTransactionId(transactionID);
        User user = carrierStatusReportParam.getRequestUserID();
        Infos.ObjCommon objCommon = utilsComp.setObjCommon(transactionID, user);
        //check access privilege
        Params.AccessControlCheckInqParam accessControlCheckInqParam = new Params.AccessControlCheckInqParam();
        log.info("【step1】 - accessControlCheckInqService.sxAccessControlCheckInq");
        accessControlCheckInqService.sxAccessControlCheckInq(objCommon,accessControlCheckInqParam);

        log.info("【step2】 - carrierStatusRptService.sxRtmsCarrierStatusReport");
        result = carrierStatusRptService.sxRtmsCarrierStatusReport(objCommon, carrierStatusReportParam);
        log.info("rtmsCarrierStatusRpt Response json" + JSON.toJSONString(result));
        return Response.createSucc(result);
    }
}
