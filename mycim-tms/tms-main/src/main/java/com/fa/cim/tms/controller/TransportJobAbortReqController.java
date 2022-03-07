package com.fa.cim.tms.controller;

import com.alibaba.fastjson.JSON;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.tms.common.enums.TransactionIDEnum;
import com.fa.cim.tms.config.MsgRetCodeConfig;
import com.fa.cim.tms.controller.interfaces.ITransportJobAbortReqController;
import com.fa.cim.tms.dto.Params;
import com.fa.cim.tms.dto.Response;
import com.fa.cim.tms.dto.Results;
import com.fa.cim.tms.dto.User;
import com.fa.cim.tms.method.IUtilsComp;
import com.fa.cim.tms.pojo.Infos;
import com.fa.cim.tms.service.ITransportJobAbortReqService;
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
 * @date: 2020/10/26 13:50
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@RestController
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class TransportJobAbortReqController implements ITransportJobAbortReqController {
    @Autowired
    private IUtilsComp utilsComp;
    @Autowired
    private ITransportJobAbortReqService transportJobAbortReqService;
    @Autowired
    private MsgRetCodeConfig msgRetCodeConfig;

    @RequestMapping(value = "/tms/transport_job_abort/req", method = RequestMethod.POST)
    public Response tmsTransportJobAbortReq(@RequestBody Params.TransportJobAbortReqParams transportJobAbortReqParams) {
        log.info("tmsTransportJobAbortReq Request Json" + JSON.toJSONString(transportJobAbortReqParams));
        Results.TransportJobAbortReqResult result = new Results.TransportJobAbortReqResult();
        final String transactionID = TransactionIDEnum.OM06.getValue();
        ThreadContextHolder.setTransactionId(transactionID);
        User user = transportJobAbortReqParams.getRequestUserID();
        Validations.check(null == user, "the user info is null...");
        List<Infos.CarrierJob> carrierJobData = transportJobAbortReqParams.getCarrierJobData();
        Validations.check(ArrayUtils.isEmpty(carrierJobData), "the carrierJobData info is null...");
        Infos.ObjCommon objCommon = utilsComp.setObjCommon(transactionID, user);

        log.info("【step1】 - transportJobAbortReqService.sxTransportJobAbortReq");
        try {
            result = transportJobAbortReqService.sxTransportJobAbortReq(objCommon, transportJobAbortReqParams);
        } catch (ServiceException e) {
            result = e.getData(Results.TransportJobAbortReqResult.class);
            if (Validations.isEquals(msgRetCodeConfig.getMsgOmsCastTxNoSend(),e.getCode())){
                return Response.createSuccWithCode(transactionID,msgRetCodeConfig.getMsgOmsCastTxNoSend(),result);
            }
            throw e;
        }
        log.info("tmsTransportJobAbortReq Response json" + JSON.toJSONString(result));
        return Response.createSucc(result);
    }

    @RequestMapping(value = "/rtms/transport_job_abort/req", method = RequestMethod.POST)
    public Response rtmsTransportJobAbortReq(@RequestBody Params.TransportJobAbortReqParams transportJobAbortReqParams) {
        log.info("rtmsTransportJobAbortReq Request Json" + JSON.toJSONString(transportJobAbortReqParams));
        Results.TransportJobAbortReqResult result = new Results.TransportJobAbortReqResult();
        final String transactionID = TransactionIDEnum.ROM06.getValue();
        ThreadContextHolder.setTransactionId(transactionID);
        User user = transportJobAbortReqParams.getRequestUserID();
        Validations.check(null == user, "the user info is null...");
        List<Infos.CarrierJob> carrierJobData = transportJobAbortReqParams.getCarrierJobData();
        Validations.check(ArrayUtils.isEmpty(carrierJobData), "the carrierJobData info is null...");
        Infos.ObjCommon objCommon = utilsComp.setObjCommon(transactionID, user);

        log.info("【step1】 - transportJobAbortReqService.sxRtmsTransportJobAbortReq");
        result = transportJobAbortReqService.sxRtmsTransportJobAbortReq(objCommon, transportJobAbortReqParams);
        log.info("rtmsTransportJobAbortReq Response json" + JSON.toJSONString(result));
        return Response.createSucc(result);
    }
}