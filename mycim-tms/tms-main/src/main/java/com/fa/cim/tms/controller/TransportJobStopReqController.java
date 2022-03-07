package com.fa.cim.tms.controller;

import com.alibaba.fastjson.JSON;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.tms.common.enums.TransactionIDEnum;
import com.fa.cim.tms.config.MsgRetCodeConfig;
import com.fa.cim.tms.controller.interfaces.ITransportJobStopReqController;
import com.fa.cim.tms.dto.Params;
import com.fa.cim.tms.dto.Response;
import com.fa.cim.tms.dto.Results;
import com.fa.cim.tms.dto.User;
import com.fa.cim.tms.method.IUtilsComp;
import com.fa.cim.tms.pojo.Infos;
import com.fa.cim.tms.service.ITransportJobStopReqService;
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
 * 2020/10/22                               Neyo                create file
 *
 * @author: Neyo
 * @date: 2020/10/22 12:53
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@RestController
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class TransportJobStopReqController implements ITransportJobStopReqController {
    @Autowired
    private IUtilsComp utilsComp;
    @Autowired
    private ITransportJobStopReqService transportJobStopReqService;
    @Autowired
    private MsgRetCodeConfig msgRetCodeConfig;

    @RequestMapping(value = "/tms/transport_job_stop/req", method = RequestMethod.POST)
    public Response tmTransportJobStopReq(@RequestBody Params.TransportJobStopReqParams transportJobStopReqParams) {
        log.info("tmTransportJobStopReq Request Json" + JSON.toJSONString(transportJobStopReqParams));
        Results.TransportJobStopReqResult result = new Results.TransportJobStopReqResult();
        final String transactionID = TransactionIDEnum.OM02.getValue();
        ThreadContextHolder.setTransactionId(transactionID);
        User user = transportJobStopReqParams.getRequestUserID();
        Validations.check(null == user, "the user info is null...");
        List<Infos.CarrierJob> carrierJobData = transportJobStopReqParams.getCarrierJobData();
        Validations.check(null == carrierJobData, "the carrierJobData info is null...");
        Infos.ObjCommon objCommon = utilsComp.setObjCommon(TransactionIDEnum.OM02, user);

        log.info("【step1】 - transportJobStopReqService.sxTransportJobStopReq");
        try {
            result = transportJobStopReqService.sxTransportJobStopReq(objCommon, transportJobStopReqParams);
        } catch (ServiceException e) {
            if (Validations.isEquals(msgRetCodeConfig.getMsgOmsCastTxNoSend(),e.getCode())){
                return Response.createSuccWithCode(transactionID,msgRetCodeConfig.getMsgOmsCastTxNoSend(),result);
            }
            throw e;
        }
        log.info("tmTransportJobStopReq Response json" + JSON.toJSONString(result));
        return Response.createSucc(result);
    }

    @RequestMapping(value = "/rtms/transport_job_stop/req", method = RequestMethod.POST)
    public Response rtmTransportJobStopReq(@RequestBody Params.TransportJobStopReqParams transportJobStopReqParams) {
        log.info("rtmTransportJobStopReq Request Json" + JSON.toJSONString(transportJobStopReqParams));
        Results.TransportJobStopReqResult result = new Results.TransportJobStopReqResult();
        final String transactionID = TransactionIDEnum.ROM02.getValue();
        ThreadContextHolder.setTransactionId(transactionID);
        User user = transportJobStopReqParams.getRequestUserID();
        Validations.check(null == user, "the user info is null...");
        List<Infos.CarrierJob> carrierJobData = transportJobStopReqParams.getCarrierJobData();
        Validations.check(null == carrierJobData, "the carrierJobData info is null...");
        Infos.ObjCommon objCommon = utilsComp.setObjCommon(transactionID, user);

        log.info("【step1】 - transportJobStopReqService.sxRtmsTransportJobStopReq");
        result = transportJobStopReqService.sxRtmsTransportJobStopReq(objCommon, transportJobStopReqParams);
        log.info("rtmTransportJobStopReq Response json" + JSON.toJSONString(result));
        return Response.createSucc(result);
    }
}
