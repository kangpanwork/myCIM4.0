package com.fa.cim.idp.tms.api;

import com.fa.cim.common.support.User;
import com.fa.cim.dto.*;

/**
 * description:
 * <p>TmsService .<br/></p>
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/4/12        ********             Yuri               create file
 *
 * @author: Yuri
 * @date: 2019/4/12 14:41
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface TmsService {

    Results.TransportJobCreateReqResult transportJobCreateReq (Infos.ObjCommon objCommon, User user, Params.TransportJobCreateReqParams param);

    Results.TransportJobCreateReqResult rtransportJobCreateReq (Infos.ObjCommon objCommon, User user, Params.TransportJobCreateReqParams param);

    Outputs.SendTransportJobInqOut transportJobInq(Inputs.SendTransportJobInqIn param);

    Outputs.SendTransportJobInqOut rtransportJobInq(Inputs.SendRTMSTransportJobInqIn param);

    Outputs.SendTransportJobCancelReqOut transportJobCancelReq(Inputs.SendTransportJobCancelReqIn param);

    Outputs.SendTransportJobCancelReqOut rtransportJobCancelReq(Inputs.SendTransportJobCancelReqIn param);

    Results.PriorityChangeReqResult priorityChangeReq(Infos.ObjCommon objCommon,User user,Infos.PriorityChangeReq param);

    Outputs.SendStockerDetailInfoInqOut stockerDetailInfoInq(Infos.ObjCommon objCommon, User user, Inputs.StockerDetailInfoInq param);

    Results.AmhsUploadInventoryReqResult uploadInventoryReq(Inputs.SendUploadInventoryReqIn param);
}
