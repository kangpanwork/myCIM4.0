package com.fa.cim.tms.service;

import com.fa.cim.tms.dto.Params;
import com.fa.cim.tms.dto.Results;
import com.fa.cim.tms.pojo.Infos;

/**
 * description:
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/10/12                              Neyo                create file
 *
 * @author: Neyo
 * @date: 2020/10/12 15:15
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface ITransportJobCancelReqService {
    Results.TransportJobCancelReqResult sxTransportJobCancelReq(Infos.ObjCommon strObjCommonIn, Params.TransportJobCancelReqParams transportJobCancelReqParams);

    Results.TransportJobCancelReqResult sxRtmsTransportJobCancelReq(Infos.ObjCommon objCommon, Params.TransportJobCancelReqParams transportJobCancelReqParams);
}
