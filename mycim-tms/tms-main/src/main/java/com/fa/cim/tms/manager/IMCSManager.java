package com.fa.cim.tms.manager;

import com.fa.cim.tms.dto.Params;
import com.fa.cim.tms.dto.Results;
import com.fa.cim.tms.pojo.Infos;

/**
 * description: To Mcs Manager Interface
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/10/12                               Neyo                create file
 *
 * @author: Neyo
 * @date: 2020/10/12 13:20
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IMCSManager {
    Results.TransportJobCreateReqResult sendTransportJobCreateReq(Infos.ObjCommon objCommon, Params.TransportJobCreateReqParams tempTranJobCreateReq);

    Results.TransportJobInqResult sendTransportJobInq(Infos.ObjCommon objCommon, Params.TransportJobInqParams transportJobInq);

    Results.UploadInventoryReqResult sendUploadInventoryReq(Infos.ObjCommon objCommon, Params.UploadInventoryReqParmas uploadInventoryReqParmas);

    Results.TransportJobCancelReqResult sendTransportJobCancelReq(Infos.ObjCommon objCommon, Params.TransportJobCancelReqParams tempTranJobCancelReq);

    Results.StockerDetailInfoInqResult sendStockerDetailInfoInq(Infos.ObjCommon objCommon, Params.StockerDetailInfoInqParmas stockerDetailInfoInqParmas);

    Results.CarrierInfoChangeReqResult sendCarrierInfoChangeReq(Infos.ObjCommon objCommon, Params.CarrierInfoChangeReqParam param);

    Results.TransportJobAbortReqResult sendTransportJobAbortReq(Infos.ObjCommon objCommon, Params.TransportJobAbortReqParams transportJobAbortReqParams);

    Results.TransportJobPauseReqResult sendTransportJobPauseReq(Infos.ObjCommon objCommon, Params.TransportJobPauseReqParams transportJobPauseReqParams);

    Results.TransportJobRemoveReqResult sendTransportJobRemoveReq(Infos.ObjCommon objCommon, Params.TransportJobRemoveReqParams transportJobRemoveReqParams);

    Results.TransportJobStopReqResult sendTransportJobStopReq(Infos.ObjCommon objCommon, Params.TransportJobStopReqParams transportJobStopReqParams);

    Results.TransportRouteCheckReqResult sendTransportJRouteCheckReq(Infos.ObjCommon objCommon, Params.TransportRouteCheckReqParams transportRouteCheckReqParams);

    Results.EstimatedTransportTimeInqResult sendEstimatedTarnsportTimeInq(Infos.ObjCommon objCommon, Infos.EstimatedTarnsportTimeInq estimatedTransportTimeInq);

    Results.PriorityChangeReqResult sendPriorityChangeReq(Infos.ObjCommon objCommon, Params.PriorityChangeReqParam priorityChangeReqParam);

    void sendOnlineHostInq(Infos.ObjCommon objCommon, Params.OnlineHostInqParam onlineHostInqParam);

    Results.N2PurgeReqResult sendN2PurgeReq(Infos.ObjCommon objCommon, Params.N2PurgeReqParams n2PurgeReqParams);

    Results.TransportJobResumeReqResult sendTransportJobResumeReq(Infos.ObjCommon objCommon, Params.TransportJobResumeReqParams transportJobResumeReqParams);


}
