package com.fa.cim.controller.tcc.cancel;

import com.fa.cim.common.support.Response;
import com.fa.cim.controller.interfaces.arhs.IArhsController;
import com.fa.cim.dto.Params;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * description:
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/11/3                              Neyo                create file
 *
 * @author: Neyo
 * @date: 2020/11/3 16:19
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Service("ArhsCancel")
@Transactional(rollbackFor = Exception.class)
public class ArhsCancel implements IArhsController {

    @Override
    public Response reticleDispatchJobCreateReq(Params.AsyncReticleXferJobCreateReqParams params) {
        return null;
    }

    @Override
    public Response rpodXferStatusChangeRpt(Params.RSPXferStatusChangeRptParams params) {
        return null;
    }

    @Override
    public Response reticleComptJobCreateErrHdlrReq(Params.ReticleActionReleaseErrorReqParams params) {
        return null;
    }

    @Override
    public Response reticleComptJobCreateReq(Params.ReticleActionReleaseReqParams params) {
        return null;
    }

    @Override
    public Response reticleDispatchJobCancelReq(Params.ReticleDispatchJobCancelReqParams params) {
        return null;
    }

    @Override
    public Response reticleComptJobRetryReq(Params.ReticleComponentJobRetryReqParams params) {
        return null;
    }

    @Override
    public Response reticleComptJobSkipReq(Params.ReticleComponentJobSkipReqParams params) {
        return null;
    }

    @Override
    public Response reticleDispatchJobDeleteReq(Params.ReticleDispatchJobDeleteReqParams params) {
        return null;
    }

    @Override
    public Response reticleDispatchJobAddReq(Params.ReticleDispatchJobInsertReqParams params) {
        return null;
    }

    @Override
    public Response reticleRetrieveJobCreateReq(Params.ReticleRetrieveJobCreateReqParams params) {
        return null;
    }

    @Override
    public Response reticleStoreJobCreateReq(Params.ReticleStoreJobCreateReqParams params) {
        return null;
    }

    @Override
    public Response reticleXferJobCreateReq(Params.ReticleXferJobCreateReqParams params) {
        return null;
    }

    @Override
    public Response rpodXferReq(Params.ReticlePodXferReqParams params) {
        return null;
    }

    @Override
    public Response reticleDeliveryStatusChangeReq(Params.ReticleDispatchAndComponentJobStatusChangeReqParams params) {
        return null;
    }

    @Override
    public Response whatReticleActionListReq(Params.WhatReticleActionListReqParams params) {
        return null;
    }

    @Override
    public Response rpodXferJobCreateReq(Params.ReticlePodXferJobCreateReqParams params) {
        return null;
    }

    @Override
    public Response rpodXferJobCompRpt(Params.ReticlePodXferJobCompRptParams params) {
        return null;
    }

    @Override
    public Response rpodUnclampRpt(Params.ReticlePodUnclampRptParams params) {
        return null;
    }

    @Override
    public Response rpodUnclampReq(Params.ReticlePodUnclampReqParams params) {
        return null;
    }

    @Override
    public Response rpodUnclampAndXferJobCreateReq(Params.ReticlePodUnclampAndXferJobCreateReqParams params) {
        return null;
    }

    @Override
    public Response rpodUnclampJobCreateReq(Params.ReticlePodUnclampJobCreateReqParams params) {
        return null;
    }

    @Override
    public Response rpodXferJobDeleteReq(Params.ReticlePodXferJobDeleteReqParams params) {
        return null;
    }
}
