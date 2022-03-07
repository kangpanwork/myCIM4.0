package com.fa.cim.tms.controller;

import com.alibaba.fastjson.JSON;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.tms.common.enums.TransactionIDEnum;
import com.fa.cim.tms.config.MsgRetCodeConfig;
import com.fa.cim.tms.controller.interfaces.ITransportJobCancelReqController;
import com.fa.cim.tms.dto.Params;
import com.fa.cim.tms.dto.Response;
import com.fa.cim.tms.dto.Results;
import com.fa.cim.tms.dto.User;
import com.fa.cim.tms.method.IUtilsComp;
import com.fa.cim.tms.pojo.Infos;
import com.fa.cim.tms.service.ITransportJobCancelReqService;
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
 * 2020/10/26                               Neyo                create file
 *
 * @author: Neyo
 * @date: 2020/10/26 13:43
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@RestController
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class TransportJobCancelReqController implements ITransportJobCancelReqController {
    @Autowired
    private IUtilsComp utilsComp;
    @Autowired
    private ITransportJobCancelReqService transportJobCancelReqService;
    @Autowired
    private MsgRetCodeConfig msgRetCodeConfig;

    @RequestMapping(value = "/tms/transport_job_cancel/req", method = RequestMethod.POST)
    public Response tmsTransportJobCancelReq(@RequestBody Params.TransportJobCancelReqParams transportJobCancelReqParams) {
        log.info("tmsTransportJobCancelReq Request Json" + JSON.toJSONString(transportJobCancelReqParams));
        Results.TransportJobCancelReqResult result = new Results.TransportJobCancelReqResult();
        final String transactionID = TransactionIDEnum.OM04.getValue();
        ThreadContextHolder.setTransactionId(transactionID);
        User user = transportJobCancelReqParams.getRequestUserID();
        Validations.check(null == user, "the user info is null...");
        List<Infos.AmhsCarrierJob> amhsCarrierJobArry = transportJobCancelReqParams.getCarrierJobData();
        Validations.check(ArrayUtils.isEmpty(amhsCarrierJobArry), "the amhsCarrierJob info is null...");
        Infos.ObjCommon objCommon = utilsComp.setObjCommon(transactionID, user);

        log.info("【step1】 - transportJobCancelReqService.sxTransportJobCancelReq");
        try {
            result = transportJobCancelReqService.sxTransportJobCancelReq(objCommon, transportJobCancelReqParams);
        } catch (ServiceException e) {
            result = e.getData(Results.TransportJobCancelReqResult.class);
            if (Validations.isEquals(msgRetCodeConfig.getMsgOmsCastTxNoSend(),e.getCode())){
                return Response.createSuccWithCode(transactionID,msgRetCodeConfig.getMsgOmsCastTxNoSend(),result);
            }
            throw e;
        }
        log.info("tmsTransportJobCancelReq Response json /n" + JSON.toJSONString(result));
        return Response.createSucc(result);
    }

    @RequestMapping(value = "/rtms/transport_job_cancel/req", method = RequestMethod.POST)
    public Response rtmsTransportJobCancelReq(@RequestBody Params.TransportJobCancelReqParams transportJobCancelReqParams) {
        log.info("rtmsTransportJobCancelReq Request Json" + JSON.toJSONString(transportJobCancelReqParams));
        Results.TransportJobCancelReqResult result = new Results.TransportJobCancelReqResult();
        final String transactionID = TransactionIDEnum.ROM04.getValue();
        ThreadContextHolder.setTransactionId(transactionID);
        User user = transportJobCancelReqParams.getRequestUserID();
        Validations.check(null == user, "the user info is null...");
        List<Infos.AmhsCarrierJob> amhsCarrierJobArry = transportJobCancelReqParams.getCarrierJobData();
        Validations.check(ArrayUtils.isEmpty(amhsCarrierJobArry), "the amhsCarrierJob info is null...");
        Infos.ObjCommon objCommon = utilsComp.setObjCommon(transactionID, user);

        log.info("【step1】 - transportJobCancelReqService.sxRtmsTransportJobCancelReq");
        result = transportJobCancelReqService.sxRtmsTransportJobCancelReq(objCommon, transportJobCancelReqParams);
        log.info("rtmsTransportJobCancelReq Response json /n" + JSON.toJSONString(result));
        return Response.createSucc(result);
    }
}