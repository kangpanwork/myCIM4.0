package com.fa.cim.tms.remote;

import com.fa.cim.middleware.standard.api.annotations.caller.Callback;
import com.fa.cim.middleware.standard.api.annotations.caller.CimRemoteManager;
import com.fa.cim.middleware.standard.api.annotations.caller.Dispatchable;
import com.fa.cim.middleware.standard.api.caller.RemoteManager;
import com.fa.cim.tms.dto.Params;
import com.fa.cim.tms.dto.Response;
import com.fa.cim.tms.remote.remoteBack.CallMcsDefaultCheckBack;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * description:
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/6/4                              Neyo                create file
 *
 * @author: Neyo
 * @date: 2020/6/4 15:51
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@CimRemoteManager("tms-mcs-service")
public interface IToMcsRemoteManager extends RemoteManager {

//    @Dispatchable("OM01")
    @Dispatchable("TransportJobCreateReq")
    @Callback(CallMcsDefaultCheckBack.class)
    Response sendTransportJobCreateReq(@RequestBody Params.TransportJobCreateReqParams param);

//    @Dispatchable("OM04")
    @Dispatchable("TransportJobCancelReq")
    @Callback(CallMcsDefaultCheckBack.class)
    Response sendTransportJobCancelReq(@RequestBody Params.TransportJobCancelReqParams param);

//    @Dispatchable("OM09")
    @Dispatchable("UploadInventoryReq")
    @Callback(CallMcsDefaultCheckBack.class)
    Response sendUploadInventoryReq(@RequestBody Params.UploadInventoryReqParmas param);

//    @Dispatchable("OM10")
    @Dispatchable("StockerDetailInfoInq")
    @Callback(CallMcsDefaultCheckBack.class)
    Response sendStockerDetailInfoInq(@RequestBody Params.StockerDetailInfoInqParmas param);

//    @Dispatchable("OM11")
    @Dispatchable("OnlineHostInq")
    @Callback(CallMcsDefaultCheckBack.class)
    Response sendOnlineHostInq(@RequestBody Params.OnlineHostInqParam param);

//    @Dispatchable("OM12")
    @Dispatchable("PriorityChangeReq")
    @Callback(CallMcsDefaultCheckBack.class)
    Response sendPriorityChangeReq(@RequestBody Params.PriorityChangeReqParam param);

//    @Dispatchable("OM14")
    @Dispatchable("TransportJobInq")
    @Callback(CallMcsDefaultCheckBack.class)
    Response sendTransportJobInq(@RequestBody Params.TransportJobInqParams param);
}
