package com.fa.cim.simulator.listener;

import com.fa.cim.middleware.standard.api.annotations.listener.CimMapping;
import com.fa.cim.middleware.standard.api.annotations.listener.Listenable;
import com.fa.cim.simulator.common.menu.TransactionIDEnum;
import com.fa.cim.simulator.controller.*;
import com.fa.cim.simulator.dto.Params;
import com.fa.cim.simulator.dto.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * description:
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/10/27                               Neyo                create file
 *
 * @author: Neyo
 * @date: 2020/10/27 13:17
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Service
@Slf4j
@Listenable
public class McsControllerListenerManager {
    @Autowired
    private TransportJobCreateReqController transportJobCreateReqController;
    @Autowired
    private UploadInventoryReqController uploadInventoryReqController;
    @Autowired
    private TransportJobInqController transportJobInqController;
    @Autowired
    private OnlineHostInqController onlineHostInqController;
    @Autowired
    private TransportJobCancelReqController transportJobCancelReqController;
    @Autowired
    private PriorityChangeReqController priorityChangeReqController;
    @Autowired
    private StockerDetailInfoInqController stockerDetailInfoInqController;


    @CimMapping(names = "OM01")
    public Response transportJobCreateReq(@RequestBody Params.TransportJobCreateReqParams cimRequest) {
        log.info("MCS transportJobCreateReq Request Json From MQ >>>:" + cimRequest.toString());
//        Object messageBody = cimRequest.getMessageBody();
        if (cimRequest == null) {
            return Response.createError(TransactionIDEnum.OM01.getValue(), "TMS messageBody is null");
        }
//        Params.TransportJobCreateReqParams transportJobCreateReqParams = JSONObject.parseObject(messageBody.toString(), Params.TransportJobCreateReqParams.class);
        Response response = transportJobCreateReqController.transportJobCreateReq(cimRequest);
        log.info("MCS transportJobCreateReq Response Json To MQ >>>:" + response);
        return response;
    }

    @CimMapping(names = "OM04")
    public Response transportJobCancelReq(@RequestBody Params.TransportJobCancelReqParams cimRequest) {
        log.info("MCS transportJobCancelReq Request Json From MQ >>>:" + cimRequest.toString());
//        Object messageBody = cimRequest.getMessageBody();
        if (cimRequest == null) {
            return Response.createError(TransactionIDEnum.OM04.getValue(), "TMS messageBody is null");
        }
//        Params.TransportJobCancelReqParams transportJobCancelReqParams = JSONObject.parseObject(messageBody.toString(), Params.TransportJobCancelReqParams.class);
        Response response = transportJobCancelReqController.transportJobCancelReq(cimRequest);
        log.info("MCS transportJobCancelReq Response Json To MQ >>>:" + response.toString());
        return response;
    }


    @CimMapping(names = "OM09")
    public Response uploadInventoryReq(@RequestBody Params.AmhsUploadInventoryReqParmas cimRequest) {
        log.info("MCS uploadInventoryReq Request Json From MQ >>>:" + cimRequest.toString());
//        Object messageBody = cimRequest.getMessageBody();
        if (cimRequest == null) {
            return Response.createError(TransactionIDEnum.OM09.getValue(), "TMS messageBody is null");
        }
//        Params.AmhsUploadInventoryReqParmas amhsUploadInventoryReqParmas = JSONObject.parseObject(messageBody.toString(), Params.AmhsUploadInventoryReqParmas.class);
        Response response = uploadInventoryReqController.uploadInventoryReq(cimRequest);
        log.info("MCS uploadInventoryReq Response Json To MQ >>>:" + response.toString());
        return response;
    }

    @CimMapping(names = "OM10")
    public Response stockerDetailInfoInq(@RequestBody Params.StockerDetailInfoInqParmas cimRequest) {
        log.info("MCS txXmStockerDetailInfoInq Request Json From MQ >>>:" + cimRequest.toString());
//        Object messageBody = cimRequest.getMessageBody();
        if (cimRequest == null) {
            return Response.createError(TransactionIDEnum.OM10.getValue(), "TMS messageBody is null");
        }
//        Params.StockerDetailInfoInqParmas stockerDetailInfoInqParmas = JSONObject.parseObject(messageBody.toString(), Params.StockerDetailInfoInqParmas.class);
        Response response = stockerDetailInfoInqController.stockerDetailInfoInq(cimRequest);
        log.info("MCS txXmStockerDetailInfoInq Response Json To MQ >>>:" + response.toString());
        return response;
    }

    @CimMapping(names = "OM11")
    public Response onlineHostInq(@RequestBody Params.OnlineHostInqParam cimRequest) {
        log.info("MCS txXmStockerDetailInfoInq Request Json From MQ >>>:" + cimRequest.toString());
//        Object messageBody = cimRequest.getMessageBody();
        if (cimRequest == null) {
            return Response.createError(TransactionIDEnum.OM11.getValue(), "TMS messageBody is null");
        }
//        Params.OnlineHostInqParam onlineHostInqParam = JSONObject.parseObject(messageBody.toString(), Params.OnlineHostInqParam.class);
        Response response = onlineHostInqController.onlineHostInq(cimRequest);
        log.info("MCS txXmStockerDetailInfoInq Response Json To MQ >>>:" + response.toString());
        return response;
    }

    @CimMapping(names = "OM12")
    public Response priorityChangeReq(@RequestBody Params.PriorityChangeReqParam cimRequest) {
        log.info("MCS priorityChangeReq Request Json From MQ >>>:" + cimRequest.toString());
//        Object messageBody = cimRequest.getMessageBody();
        if (cimRequest == null) {
            return Response.createError(TransactionIDEnum.OM12.getValue(), "TMS messageBody is null");
        }
//        Params.PriorityChangeReqParam priorityChangeReqParam = JSONObject.parseObject(messageBody.toString(), Params.PriorityChangeReqParam.class);
        Response response = priorityChangeReqController.priorityChangeReq(cimRequest);
        log.info("MCS priorityChangeReq Response Json To MQ >>>:" + response.toString());
        return response;
    }

    @CimMapping(names = "OM14")
    public Response transportJobInq(@RequestBody Params.TransportJobInqParams cimRequest) {
        log.info("MCS transportJobInq Request Json From MQ >>>:" + cimRequest.toString());
//        Object messageBody = cimRequest.getMessageBody();
        if (cimRequest == null) {
            return Response.createError(TransactionIDEnum.OM14.getValue(), "TMS messageBody is null");
        }
//        Params.TransportJobInqParams transportJobInqParams = JSONObject.parseObject(messageBody.toString(), Params.TransportJobInqParams.class);
        Response response = transportJobInqController.transportJobInq(cimRequest);
        log.info("MCS transportJobInq Response Json To MQ >>>:" + response.toString());
        return response;
    }
}
