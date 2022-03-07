package com.fa.cim.tms.controller;

import com.alibaba.fastjson.JSON;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.tms.common.enums.TransactionIDEnum;
import com.fa.cim.tms.config.MsgRetCodeConfig;
import com.fa.cim.tms.controller.interfaces.ICarrierLocationRptController;
import com.fa.cim.tms.dto.Params;
import com.fa.cim.tms.dto.Response;
import com.fa.cim.tms.dto.Results;
import com.fa.cim.tms.dto.User;
import com.fa.cim.tms.method.IUtilsComp;
import com.fa.cim.tms.pojo.Infos;
import com.fa.cim.tms.service.IAccessControlCheckInqService;
import com.fa.cim.tms.service.ICarrierLocationRptService;
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
public class CarrierLocationRptController implements ICarrierLocationRptController {
    @Autowired
    private IUtilsComp utilsComp;
    @Autowired
    private ICarrierLocationRptService carrierLocationRptService;
    @Autowired
    private IAccessControlCheckInqService accessControlCheckInqService;
    @Autowired
    private MsgRetCodeConfig msgRetCodeConfig;

    @RequestMapping(value = "/tms/carrier_location/rpt", method = RequestMethod.POST)
    public Response tmsCarrierLocationRpt(@RequestBody Params.CarrierLocationReportParmas carrierLocationReportParmas) {
        log.info("tmsCarrierLocationRpt Request Json" + JSON.toJSONString(carrierLocationReportParmas));
        Results.CarrierLocationReportResult result = new Results.CarrierLocationReportResult();
        final String transactionID = TransactionIDEnum.TM01.getValue();
        ThreadContextHolder.setTransactionId(transactionID);
        User user = carrierLocationReportParmas.getRequestUserID();
        Validations.check(null == user, "the user info is null...");
        Infos.ObjCommon objCommon = utilsComp.setObjCommon(transactionID, user);

        //check access privilege
        Params.AccessControlCheckInqParam accessControlCheckInqParam = new Params.AccessControlCheckInqParam();
        log.info("【step1】 - accessControlCheckInqService.sxAccessControlCheckInq");
        accessControlCheckInqService.sxAccessControlCheckInq(objCommon,accessControlCheckInqParam);

        log.info("【step2】 - carrierLocationRptService.sxCarrierLocationRpt");
        try {
            result = carrierLocationRptService.sxCarrierLocationRpt(objCommon, carrierLocationReportParmas);
        } catch (ServiceException e) {
            result = e.getData(Results.CarrierLocationReportResult.class);
            if (Validations.isEquals(msgRetCodeConfig.getMsgOmsCastTxNoSend(),e.getCode())){
                return Response.createSuccWithCode(transactionID,msgRetCodeConfig.getMsgOmsCastTxNoSend(),result);
            }
            throw e;
        }
        log.info("tmsCarrierLocationRpt Response json" + JSON.toJSONString(result));
        return Response.createSucc(result);
    }

    @RequestMapping(value = "/rtms/carrier_location/rpt", method = RequestMethod.POST)
    public Response rtmsCarrierLocationRpt(@RequestBody Params.CarrierLocationReportParmas carrierLocationReportParmas) {
        log.info("rtmsCarrierLocationRpt Request Json" + JSON.toJSONString(carrierLocationReportParmas));
        Results.CarrierLocationReportResult result = new Results.CarrierLocationReportResult();
        final String transactionID = TransactionIDEnum.RTM01.getValue();
        ThreadContextHolder.setTransactionId(transactionID);
        User user = carrierLocationReportParmas.getRequestUserID();
        Validations.check(null == user, "the user info is null...");
        Infos.ObjCommon objCommon = utilsComp.setObjCommon(transactionID, user);
        //check access privilege
        Params.AccessControlCheckInqParam accessControlCheckInqParam = new Params.AccessControlCheckInqParam();
        log.info("【step1】 - accessControlCheckInqService.sxAccessControlCheckInq");
        accessControlCheckInqService.sxAccessControlCheckInq(objCommon,accessControlCheckInqParam);

        log.info("【step2】 - carrierLocationRptService.sxRtmsCarrierLocationRpt");
        result = carrierLocationRptService.sxRtmsCarrierLocationRpt(objCommon, carrierLocationReportParmas);
        log.info("rtmsCarrierLocationRpt Response json" + JSON.toJSONString(result));
        return Response.createSucc(result);
    }
}