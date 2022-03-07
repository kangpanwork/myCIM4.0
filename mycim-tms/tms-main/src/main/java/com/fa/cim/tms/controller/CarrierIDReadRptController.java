package com.fa.cim.tms.controller;

import com.alibaba.fastjson.JSON;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.tms.common.enums.TransactionIDEnum;
import com.fa.cim.tms.config.MsgRetCodeConfig;
import com.fa.cim.tms.controller.interfaces.ICarrierIDReadRptController;
import com.fa.cim.tms.dto.*;
import com.fa.cim.tms.method.IUtilsComp;
import com.fa.cim.tms.pojo.Infos;
import com.fa.cim.tms.service.IAccessControlCheckInqService;
import com.fa.cim.tms.service.ICarrierIDReadRptService;
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
public class CarrierIDReadRptController implements ICarrierIDReadRptController {
    @Autowired
    private IUtilsComp utilsComp;
    @Autowired
    private ICarrierIDReadRptService carrierIDReadRptService;
    @Autowired
    private MsgRetCodeConfig msgRetCodeConfig;
    @Autowired
    private IAccessControlCheckInqService accessControlCheckInqService;

    @RequestMapping(value = "/tms/carrier_id_read/rpt", method = RequestMethod.POST)
    public Response tmsCarrierIDReadRpt(@RequestBody Params.CarrierIDReadReportParmas carrierIDReadReportParmas) {
        log.info("tmsCarrierIDReadRpt Request Json" + JSON.toJSONString(carrierIDReadReportParmas));
        Results.CarrierIDReadReportResult result = new Results.CarrierIDReadReportResult();
        final String transactionID = TransactionIDEnum.TM06.getValue();
        ThreadContextHolder.setTransactionId(transactionID);
        User user = carrierIDReadReportParmas.getRequestUserID();
        Validations.check(null == user, "the user info is null...");
        Infos.ObjCommon objCommon = utilsComp.setObjCommon(transactionID, user);
        //check access privilege
        Params.AccessControlCheckInqParam accessControlCheckInqParam = new Params.AccessControlCheckInqParam();
        log.info("【step1】 - accessControlCheckInqService.sxAccessControlCheckInq");
        accessControlCheckInqService.sxAccessControlCheckInq(objCommon,accessControlCheckInqParam);

        log.info("【step2】 - carrierIDReadRptService.sxCarrierIDReadReport");
        try {
            result = carrierIDReadRptService.sxCarrierIDReadReport(objCommon, carrierIDReadReportParmas);
        } catch (ServiceException e) {
            if (Validations.isEquals(msgRetCodeConfig.getMsgAbnormalStock(), e.getCode())) {
                return Response.createSuccWithCode(transactionID, new Code(e.getCode(), e.getMessage()), null);
            } else if (Validations.isEquals(msgRetCodeConfig.getMsgOmsCastTxNoSend(), e.getCode())) {
                return Response.createSuccWithCode(transactionID, new Code(e.getCode(), e.getMessage()), null);
            }
            throw e;
        }
        log.info("tmsCarrierIDReadRpt Response json" + JSON.toJSONString(result));
        return Response.createSucc(result);
    }

    @RequestMapping(value = "/rtms/carrier_id_read/rpt", method = RequestMethod.POST)
    public Response rtmsCarrierIDReadRpt(@RequestBody Params.CarrierIDReadReportParmas carrierIDReadReportParmas) {
        log.info("rtmsCarrierIDReadRpt Request Json" + JSON.toJSONString(carrierIDReadReportParmas));
        Results.CarrierIDReadReportResult result = new Results.CarrierIDReadReportResult();
        final String transactionID = TransactionIDEnum.RTM17.getValue();
        ThreadContextHolder.setTransactionId(transactionID);
        User user = carrierIDReadReportParmas.getRequestUserID();
        Validations.check(null == user, "the user info is null...");
        Infos.ObjCommon objCommon = utilsComp.setObjCommon(transactionID, user);

        //check access privilege
        Params.AccessControlCheckInqParam accessControlCheckInqParam = new Params.AccessControlCheckInqParam();
        log.info("【step1】 - accessControlCheckInqService.sxAccessControlCheckInq");
        accessControlCheckInqService.sxAccessControlCheckInq(objCommon,accessControlCheckInqParam);

        log.info("【step2】 - accessControlCheckInqService.sxRtmsCarrierIDReadReport");
        try {
            result = carrierIDReadRptService.sxRtmsCarrierIDReadReport(objCommon, carrierIDReadReportParmas);
        } catch (ServiceException e) {
            if (Validations.isEquals(msgRetCodeConfig.getMsgAbnormalStock(), e.getCode())) {
                return Response.createSuccWithCode(transactionID, new Code(e.getCode(),e.getMessage()), null);
            }
            throw e;
        }
        log.info("rtmsCarrierIDReadRpt Response json" + JSON.toJSONString(result));
        return Response.createSucc(result);
    }
}
