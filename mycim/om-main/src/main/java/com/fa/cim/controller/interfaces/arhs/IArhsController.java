package com.fa.cim.controller.interfaces.arhs;

import com.fa.cim.common.support.Response;
import com.fa.cim.dto.Params;

/**
 * description:
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/11/3                              Neyo                create file
 *
 * @author: Neyo
 * @date: 2020/11/3 16:17
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IArhsController {

    Response reticleDispatchJobCreateReq(Params.AsyncReticleXferJobCreateReqParams params);

    Response rpodXferStatusChangeRpt(Params.RSPXferStatusChangeRptParams params);

    Response reticleComptJobCreateErrHdlrReq(Params.ReticleActionReleaseErrorReqParams params);

    Response reticleComptJobCreateReq(Params.ReticleActionReleaseReqParams params);

    Response reticleDispatchJobCancelReq(Params.ReticleDispatchJobCancelReqParams params);

    Response reticleComptJobRetryReq(Params.ReticleComponentJobRetryReqParams params);

    Response reticleComptJobSkipReq(Params.ReticleComponentJobSkipReqParams params);

    Response reticleDispatchJobDeleteReq(Params.ReticleDispatchJobDeleteReqParams params);

    Response reticleDispatchJobAddReq(Params.ReticleDispatchJobInsertReqParams params);

    Response reticleRetrieveJobCreateReq(Params.ReticleRetrieveJobCreateReqParams params);

    Response reticleStoreJobCreateReq(Params.ReticleStoreJobCreateReqParams params);

    Response reticleXferJobCreateReq(Params.ReticleXferJobCreateReqParams params);

    Response rpodXferReq(Params.ReticlePodXferReqParams params);

    Response reticleDeliveryStatusChangeReq(Params.ReticleDispatchAndComponentJobStatusChangeReqParams params);

    Response whatReticleActionListReq(Params.WhatReticleActionListReqParams params);

    Response rpodXferJobCreateReq(Params.ReticlePodXferJobCreateReqParams params);

    Response rpodXferJobCompRpt(Params.ReticlePodXferJobCompRptParams params);

    Response rpodUnclampRpt(Params.ReticlePodUnclampRptParams params);

    Response rpodUnclampReq(Params.ReticlePodUnclampReqParams params);

    Response rpodUnclampAndXferJobCreateReq(Params.ReticlePodUnclampAndXferJobCreateReqParams params);

    Response rpodUnclampJobCreateReq(Params.ReticlePodUnclampJobCreateReqParams params);

    Response rpodXferJobDeleteReq(Params.ReticlePodXferJobDeleteReqParams params);

}
