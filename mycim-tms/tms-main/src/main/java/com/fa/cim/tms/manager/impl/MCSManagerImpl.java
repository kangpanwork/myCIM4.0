package com.fa.cim.tms.manager.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fa.cim.middleware.standard.core.exception.base.CimIntegrationException;
import com.fa.cim.tms.dto.Code;
import com.fa.cim.tms.dto.Params;
import com.fa.cim.tms.dto.Response;
import com.fa.cim.tms.dto.Results;
import com.fa.cim.tms.manager.IMCSManager;
import com.fa.cim.tms.pojo.Infos;
import com.fa.cim.tms.pojo.ObjectIdentifier;
import com.fa.cim.tms.remote.IToMcsRemoteManager;
import com.fa.cim.tms.utils.Validations;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;

/**
 * @program: mycim_tms
 * @description: MTSCManagerImpl
 * @author: miner
 * @create: 2018-09-21 18:38
 */
@Service
@Slf4j
public class MCSManagerImpl implements IMCSManager {

    @Autowired
    private IToMcsRemoteManager toMcsRemoteManager;

    @Override
    public Results.TransportJobCreateReqResult sendTransportJobCreateReq(Infos.ObjCommon objCommon, Params.TransportJobCreateReqParams tempTranJobCreateReq) {
        Results.TransportJobCreateReqResult transportJobCreateReqResult = new Results.TransportJobCreateReqResult();
        tempTranJobCreateReq.setRequestUserID(objCommon.getUser());
        log.info("TMS->MCS mq  sendTransportJobCreateReq request {}", JSONObject.toJSONString(tempTranJobCreateReq));
        Response response = null;
        try {
            response = toMcsRemoteManager.sendTransportJobCreateReq(tempTranJobCreateReq);
        } catch(CimIntegrationException e) {
            Validations.check(true,new Code((int) e.getCode(),e.getMessage()));
        }
        log.info("TMS->MCS mq  sendTransportJobCreateReq response {}", response);
        if (null != response && null != response.getBody()) {
            transportJobCreateReqResult = JSON.parseObject(response.getBody().toString(), Results.TransportJobCreateReqResult.class);
        }
        return transportJobCreateReqResult;

    }

    @Override
    public Results.CarrierInfoChangeReqResult sendCarrierInfoChangeReq(Infos.ObjCommon objCommon, Params.CarrierInfoChangeReqParam param) {
        Results.CarrierInfoChangeReqResult result = new Results.CarrierInfoChangeReqResult();
        return result;
    }

    @Override
    public Results.TransportJobAbortReqResult sendTransportJobAbortReq(Infos.ObjCommon objCommon, Params.TransportJobAbortReqParams transportJobAbortReqParams) {
        Results.TransportJobAbortReqResult result = new Results.TransportJobAbortReqResult();
        // TODO: 2020/10/20 TestData
        result.setJobID("mtsc0116");
        Results.CarrierJobRc carrierJobRc = new Results.CarrierJobRc();
        carrierJobRc.setCarrierJobID("111");
        ObjectIdentifier objectIdentifier = new ObjectIdentifier();
        objectIdentifier.setValue("111");
        objectIdentifier.setReferenceKey("111");
        carrierJobRc.setCarrierID(objectIdentifier);
        carrierJobRc.setCarrierReturnCode("OK");
        result.setCarrierJobRcData(Arrays.asList(carrierJobRc));
        log.info("sendTransportJobAbortReq Response Json" + JSON.toJSONString(result));
        return result;
    }

    @Override
    public Results.TransportJobPauseReqResult sendTransportJobPauseReq(Infos.ObjCommon objCommon, Params.TransportJobPauseReqParams transportJobPauseReqParams) {
        Results.TransportJobPauseReqResult result = new Results.TransportJobPauseReqResult();
        // TODO: 2020/10/20 TestData
        result.setJobID("mtsc0116");
        Results.CarrierJobRc carrierJobRc = new Results.CarrierJobRc();
        carrierJobRc.setCarrierJobID("111");
        ObjectIdentifier objectIdentifier = new ObjectIdentifier();
        objectIdentifier.setValue("111");
        objectIdentifier.setReferenceKey("111");
        carrierJobRc.setCarrierID(objectIdentifier);
        carrierJobRc.setCarrierReturnCode("OK");
        result.setCarrierJobRcData(Arrays.asList(carrierJobRc));
        log.info("sendTransportJobPauseReq Response Json" + JSON.toJSONString(result));
        return result;
    }

    @Override
    public Results.TransportJobRemoveReqResult sendTransportJobRemoveReq(Infos.ObjCommon objCommon, Params.TransportJobRemoveReqParams transportJobRemoveReqParams) {
        Results.TransportJobRemoveReqResult result = new Results.TransportJobRemoveReqResult();
        return result;
    }

    @Override
    public Results.TransportJobStopReqResult sendTransportJobStopReq(Infos.ObjCommon objCommon, Params.TransportJobStopReqParams transportJobStopReqParams) {
        Results.TransportJobStopReqResult result = new Results.TransportJobStopReqResult();
        return result;
    }

    @Override
    public Results.TransportRouteCheckReqResult sendTransportJRouteCheckReq(Infos.ObjCommon objCommon, Params.TransportRouteCheckReqParams transportRouteCheckReqParams) {
        Results.TransportRouteCheckReqResult result = new Results.TransportRouteCheckReqResult();
        return result;
    }

    @Override
    public Results.EstimatedTransportTimeInqResult sendEstimatedTarnsportTimeInq(Infos.ObjCommon objCommon, Infos.EstimatedTarnsportTimeInq estimatedTransportTimeInqParams) {
        Results.EstimatedTransportTimeInqResult result = new Results.EstimatedTransportTimeInqResult();
        return result;
    }

    @Override
    public Results.PriorityChangeReqResult sendPriorityChangeReq(Infos.ObjCommon objCommon, Params.PriorityChangeReqParam priorityChangeReqParam) {
        Results.PriorityChangeReqResult priorityChangeReqResult = new Results.PriorityChangeReqResult();
        priorityChangeReqParam.setRequestUserID(objCommon.getUser());
        log.info("TMS->MCS mq  sendPriorityChangeReq request {}", JSONObject.toJSONString(priorityChangeReqParam));
        Response response = null;
        try {
            response = toMcsRemoteManager.sendPriorityChangeReq(priorityChangeReqParam);
        } catch(CimIntegrationException e) {
            Validations.check(true,new Code((int) e.getCode(),e.getMessage()));
        }
        log.info("TMS->MCS mq  sendPriorityChangeReq response {}", response);
        if (null != response && null != response.getBody()) {
            priorityChangeReqResult = JSON.parseObject(response.getBody().toString(), Results.PriorityChangeReqResult.class);
        }
        return priorityChangeReqResult;
    }

    @Override
    public void sendOnlineHostInq(Infos.ObjCommon objCommon, Params.OnlineHostInqParam onlineHostInqParam) {
        onlineHostInqParam.setRequestUserID(objCommon.getUser());
        String request = JSONObject.toJSONString(onlineHostInqParam);
        log.info("TMS->MCS mq  sendOnlineHostInq request {}", request);
        Response response = null;
        try {
            response = toMcsRemoteManager.sendOnlineHostInq(onlineHostInqParam);
        } catch(CimIntegrationException e) {
            Validations.check(true,new Code((int) e.getCode(),e.getMessage()));
        }
        log.info("TMS->MCS mq  sendOnlineHostInq response {}", response);
    }

    @Override
    public Results.N2PurgeReqResult sendN2PurgeReq(Infos.ObjCommon objCommon, Params.N2PurgeReqParams n2PurgeReqParams) {
        Results.N2PurgeReqResult result = new Results.N2PurgeReqResult();
        return result;
    }

    @Override
    public Results.TransportJobResumeReqResult sendTransportJobResumeReq(Infos.ObjCommon objCommon, Params.TransportJobResumeReqParams transportJobResumeReqParams) {
        Results.TransportJobResumeReqResult result = new Results.TransportJobResumeReqResult();
        Results.CarrierJobRc carrierJobRc = new Results.CarrierJobRc();
        return result;
    }


    @Override
    public Results.TransportJobInqResult sendTransportJobInq(Infos.ObjCommon objCommon, Params.TransportJobInqParams transportJobInq) {
        Results.TransportJobInqResult transportJobInqResult = new Results.TransportJobInqResult();
        transportJobInq.setRequestUserID(objCommon.getUser());
        String request = JSONObject.toJSONString(transportJobInq);
        log.info("TMS->MCS  mq sendTransportJobInq request {}", request);
        Response response = null;
        try {
            response = toMcsRemoteManager.sendTransportJobInq(transportJobInq);
        } catch(CimIntegrationException e) {
            Validations.check(true,new Code((int) e.getCode(),e.getMessage()));
        }
        log.info("TMS->MCS  mq sendTransportJobInq response {}", response);
        transportJobInqResult = JSON.parseObject(response.getBody().toString(), Results.TransportJobInqResult.class);
        return transportJobInqResult;
    }


    @Override
    public Results.UploadInventoryReqResult sendUploadInventoryReq(Infos.ObjCommon objCommon, Params.UploadInventoryReqParmas uploadInventoryReqParmas) {
        Results.UploadInventoryReqResult amhsUploadInventoryReqResult = new Results.UploadInventoryReqResult();
        uploadInventoryReqParmas.setRequestUserID(objCommon.getUser());
        String request = JSONObject.toJSONString(uploadInventoryReqParmas);
        log.info("TMS->MCS mq  sendUploadInventoryReq request {}", request);
        Response response = null;
        try {
            response = toMcsRemoteManager.sendUploadInventoryReq(uploadInventoryReqParmas);
        } catch(CimIntegrationException e) {
            Validations.check(true,new Code((int) e.getCode(),e.getMessage()));
        }
        log.info("TMS->MCS mq  sendUploadInventoryReq response {}", response);
        if (null != response && null != response.getBody()) {
            amhsUploadInventoryReqResult = JSON.parseObject(response.getBody().toString(), Results.UploadInventoryReqResult.class);
        }
        return amhsUploadInventoryReqResult;
    }


    @Override
    public Results.TransportJobCancelReqResult sendTransportJobCancelReq(Infos.ObjCommon objCommon, Params.TransportJobCancelReqParams tempTranJobCancelReq) {
        Results.TransportJobCancelReqResult transportJobCancelReqResult = new Results.TransportJobCancelReqResult();
        tempTranJobCancelReq.setRequestUserID(objCommon.getUser());
        String request = JSONObject.toJSONString(tempTranJobCancelReq);
        log.info("TMS->MCS mq  sendTransportJobCancelReq request {}", request);
        Response response = null;//simulatorRabbitMQ.callMCS(request);
        try {
            response = toMcsRemoteManager.sendTransportJobCancelReq(tempTranJobCancelReq);
        } catch(CimIntegrationException e) {
            Validations.check(true,new Code((int) e.getCode(),e.getMessage()));
        }
        log.info("TMS->MCS mq  sendTransportJobCancelReq response {}", response);
        if (null != response && null != response.getBody()) {
            transportJobCancelReqResult = JSON.parseObject(response.getBody().toString(), Results.TransportJobCancelReqResult.class);
        }
        return transportJobCancelReqResult;
    }

    @Override
    public Results.StockerDetailInfoInqResult sendStockerDetailInfoInq(Infos.ObjCommon objCommon, Params.StockerDetailInfoInqParmas stockerDetailInfoInqParmas) {
        stockerDetailInfoInqParmas.setRequestUserID(objCommon.getUser());
        Results.StockerDetailInfoInqResult stockerDetailInfoInqResult = new Results.StockerDetailInfoInqResult();
        log.info("TMS->MCS mq sendStockerDetailInfoInq request {}", JSONObject.toJSONString(stockerDetailInfoInqResult));
        Response response = null;
        try {
            response = toMcsRemoteManager.sendStockerDetailInfoInq(stockerDetailInfoInqParmas);
        } catch(CimIntegrationException e) {
            Validations.check(true,new Code((int) e.getCode(),e.getMessage()));
        }
        log.info("TMS->MCS mq sendStockerDetailInfoInq response {}", response);
        if (null != response && null != response.getBody()) {
            stockerDetailInfoInqResult = JSON.parseObject(response.getBody().toString(), Results.StockerDetailInfoInqResult.class);
        }
        return stockerDetailInfoInqResult;
    }
}
