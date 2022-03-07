package com.fa.cim.idp.tms.remote;

import com.fa.cim.common.support.Response;
import com.fa.cim.idp.tms.adaptor.param.*;
import com.fa.cim.idp.tms.remote.remoteBack.CallTmsBackWithResponse;
import com.fa.cim.middleware.standard.api.annotations.caller.Callback;
import com.fa.cim.middleware.standard.api.annotations.caller.CimRemoteManager;
import com.fa.cim.middleware.standard.api.annotations.caller.Dispatchable;
import com.fa.cim.middleware.standard.api.caller.RemoteManager;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * description:
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/6/3                              Neyo                create file
 *
 * @author: Neyo
 * @date: 2020/6/3 15:34
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@CimRemoteManager("oms-tms-service")
public interface IXferRemoteManager extends RemoteManager {

//    @Dispatchable("OM01")
    @Dispatchable("TransportJobCreateReq")
    @Callback(CallTmsBackWithResponse.class)
    Response transportJobCreateReq(@RequestBody TmsTransportJobCreateReqParam requestParam);

//    @Dispatchable("OM14")
    @Dispatchable("TransportJobInq")
    @Callback(CallTmsBackWithResponse.class)
    Response transportJobInq(@RequestBody TmsTransportJobInqParam requestParam);

//    @Dispatchable("OM04")
    @Dispatchable("TransportJobCancelReq")
    @Callback(CallTmsBackWithResponse.class)
    Response transportJobCancelReq(@RequestBody TmsTransportJobCancelReqParam requestParam);

//    @Dispatchable("OM12")
    @Dispatchable("PriorityChangeReq")
    @Callback(CallTmsBackWithResponse.class)
    Response priorityChangeReq(@RequestBody TmsPriorityChangeReqParam requestParam);

//    @Dispatchable("OM10")
    @Dispatchable("StockerDetailInfoInq")
    @Callback(CallTmsBackWithResponse.class)
    Response stockerDetailInfoInq(@RequestBody TmsStockerDetailInfoInqParam requestParam);

//    @Dispatchable("OM09")
    @Dispatchable("UploadInventoryReq")
    @Callback(CallTmsBackWithResponse.class)
    Response uploadInventoryReq(@RequestBody TmsUploadInventoryReqParam requestParam);

//    @Dispatchable("ROM01")
    @Dispatchable("RtransportJobCreateReq")
    @Callback(CallTmsBackWithResponse.class)
    Response rtransportJobCreateReq(@RequestBody TmsTransportJobCreateReqParam tmsRequest);

//    @Dispatchable("ROM04")
    @Dispatchable("RtransportJobCancelReq")
    @Callback(CallTmsBackWithResponse.class)
    Response rtransportJobCancelReq(@RequestBody TmsTransportJobCancelReqParam requestParam);

//    @Dispatchable("ROM14")
    @Dispatchable("RtransportJobInq")
    @Callback(CallTmsBackWithResponse.class)
    Response rtransportJobInq(@RequestBody TmsTransportJobInqParam requestParam);
}
