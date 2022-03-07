package com.fa.cim.tms.controller;

import com.alibaba.fastjson.JSON;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.tms.common.enums.TransactionIDEnum;
import com.fa.cim.tms.config.MsgRetCodeConfig;
import com.fa.cim.tms.controller.interfaces.ITransportJobRemoveReqController;
import com.fa.cim.tms.dto.Params;
import com.fa.cim.tms.dto.Response;
import com.fa.cim.tms.dto.Results;
import com.fa.cim.tms.dto.User;
import com.fa.cim.tms.method.IUtilsComp;
import com.fa.cim.tms.pojo.Infos;
import com.fa.cim.tms.service.ITransportJobRemoveReqService;
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
 * @date: 2020/10/21 13:12
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@RestController
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class TransportJobRemoveReqController implements ITransportJobRemoveReqController {
    @Autowired
    private IUtilsComp utilsComp;
    @Autowired
    private ITransportJobRemoveReqService transportJobRemoveReqService;
    @Autowired
    private MsgRetCodeConfig msgRetCodeConfig;

    @RequestMapping(value = "/tms/transport_job_remove/req", method = RequestMethod.POST)
    public Response tmsTransportJobRemoveReq(@RequestBody Params.TransportJobRemoveReqParams transportJobRemoveReqParams) {
        log.info("tmsTransportJobRemoveReq Request Json" + JSON.toJSONString(transportJobRemoveReqParams));
        Results.TransportJobRemoveReqResult result = new Results.TransportJobRemoveReqResult();
        final String transactionID = TransactionIDEnum.OM07.getValue();
        ThreadContextHolder.setTransactionId(transactionID);
        User user = transportJobRemoveReqParams.getRequestUserID();
        Validations.check(null == user, "the user info is null...");
        List<Infos.CarrierJob> carrierJobData = transportJobRemoveReqParams.getCarrierJobData();
        Validations.check(ArrayUtils.isEmpty(carrierJobData), "the carrierJobData info is null...");
        Infos.ObjCommon objCommon = utilsComp.setObjCommon(transactionID, user);

        log.info("【step1】 - transportJobRemoveReqService.sxTransportJobRemoveReq");
        try {
            result = transportJobRemoveReqService.sxTransportJobRemoveReq(objCommon, transportJobRemoveReqParams);
        } catch (ServiceException e) {
            if (Validations.isEquals(msgRetCodeConfig.getMsgOmsCastTxNoSend(),e.getCode())){
                return Response.createSuccWithCode(transactionID,msgRetCodeConfig.getMsgOmsCastTxNoSend(),result);
            }
            throw e;
        }
        log.info("tmsTransportJobRemoveReq Response json" + JSON.toJSONString(result));
        return Response.createSucc(result);
    }

    @RequestMapping(value = "/rtms/transport_job_remove/req", method = RequestMethod.POST)
    public Response rtmsTransportJobRemoveReq(@RequestBody Params.TransportJobRemoveReqParams transportJobRemoveReqParams) {
        log.info("rtmsTransportJobRemoveReq Request Json" + JSON.toJSONString(transportJobRemoveReqParams));
        Results.TransportJobRemoveReqResult result = new Results.TransportJobRemoveReqResult();
        final String transactionID = TransactionIDEnum.ROM07.getValue();
        ThreadContextHolder.setTransactionId(transactionID);
        User user = transportJobRemoveReqParams.getRequestUserID();
        Validations.check(null == user, "the user info is null...");
        List<Infos.CarrierJob> carrierJobData = transportJobRemoveReqParams.getCarrierJobData();
        Validations.check(ArrayUtils.isEmpty(carrierJobData), "the carrierJobData info is null...");
        Infos.ObjCommon objCommon = utilsComp.setObjCommon(transactionID, user);

        log.info("【step1】 - transportJobRemoveReqService.sxRtmsTransportJobRemoveReq");
        result = transportJobRemoveReqService.sxRtmsTransportJobRemoveReq(objCommon, transportJobRemoveReqParams);
        log.info("rtmsTransportJobRemoveReq Response json" + JSON.toJSONString(result));
        return Response.createSucc(result);
    }
}
