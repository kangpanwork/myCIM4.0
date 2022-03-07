package com.fa.cim.controller.tcc.cancel;


import com.fa.cim.common.support.Response;
import com.fa.cim.controller.interfaces.reticle.IReticleController;
import com.fa.cim.dto.Params;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("ReticleCancel")
@Transactional(rollbackFor = Exception.class)
public class ReticleCancel implements IReticleController {
    @Override
    public Response reticleInspectionRequestReq(Params.ReticleInspectionReuqstReqParams params) {
        return null;
    }

    @Override
    public Response reticleInspectionInReq(Params.ReticleInspectionInReqParams params) {
        return null;
    }

    @Override
    public Response reticleInspectionOutReq(Params.ReticleInspectionOutReqParams params) {
        return null;
    }

    @Override
    public Response reticleRequestRepairReq(Params.ReticleRequestRepairReqParams params) {
        return null;
    }

    @Override
    public Response reticleRepairInReq(Params.ReticleRepairInReqParams params) {
        return null;
    }

    @Override
    public Response reticleRepairOutReq(Params.ReticleRepairOutReqParams params) {
        return null;
    }

    @Override
    public Response reticleConfirmMaskQualityReq(Params.ReticleConfirmMaskQualityParams params) {
        return null;
    }

    @Override
    public Response reticleHoldReq(Params.ReticleHoldReqParams params) {
        return null;
    }

    @Override
    public Response reticleTerminateReq(Params.ReticleTerminateReqParams params) {
        return null;
    }

    @Override
    public Response reticleTerminateCancelReq(Params.ReticleTerminateReqParams params) {
        return null;
    }

    @Override
    public Response reticleHoldReleaseReq(Params.ReticleHoldReleaseReqParams params) {
        return null;
    }

    @Override
    public Response reticleScrapReq(Params.ReticleScrapReqParams params) {
        return null;
    }

    @Override
    public Response reticleScrapCancelReq(Params.ReticleScrapReqParams params) {
        return null;
    }

    @Override
    public Response reticleScanRequestReq(Params.ReticleScanRequestReqParams params) {
        return null;
    }

    @Override
    public Response reticleScanCompleteReq(Params.ReticleScanCompleteReqParams params) {
        return null;
    }

    @Override
    public Response reticleInspectionTypeChangeReq(Params.ReticleInspectionTypeChangeReqParams params) {
        return null;
    }
}
