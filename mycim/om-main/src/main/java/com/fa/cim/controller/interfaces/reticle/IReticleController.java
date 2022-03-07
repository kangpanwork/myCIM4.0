package com.fa.cim.controller.interfaces.reticle;

import com.fa.cim.common.support.Response;
import com.fa.cim.dto.Params;
import org.springframework.web.bind.annotation.RequestBody;

public interface IReticleController {

    Response reticleInspectionRequestReq(@RequestBody Params.ReticleInspectionReuqstReqParams params);

    Response reticleInspectionInReq(@RequestBody Params.ReticleInspectionInReqParams params);

    Response reticleInspectionOutReq(@RequestBody Params.ReticleInspectionOutReqParams params);

    Response reticleRequestRepairReq(@RequestBody Params.ReticleRequestRepairReqParams params);

    Response reticleRepairInReq(@RequestBody Params.ReticleRepairInReqParams params);

    Response reticleRepairOutReq(@RequestBody Params.ReticleRepairOutReqParams params);

    Response reticleConfirmMaskQualityReq(@RequestBody Params.ReticleConfirmMaskQualityParams params);

    Response reticleHoldReq(@RequestBody Params.ReticleHoldReqParams params);

    Response reticleTerminateReq(@RequestBody Params.ReticleTerminateReqParams params);

    Response reticleTerminateCancelReq(@RequestBody Params.ReticleTerminateReqParams params);

    Response reticleHoldReleaseReq(@RequestBody Params.ReticleHoldReleaseReqParams params);

    Response reticleScrapReq(@RequestBody Params.ReticleScrapReqParams params);

    Response reticleScrapCancelReq(@RequestBody Params.ReticleScrapReqParams params);

    Response reticleScanRequestReq(@RequestBody Params.ReticleScanRequestReqParams params);

    Response reticleScanCompleteReq(@RequestBody Params.ReticleScanCompleteReqParams params);

    Response reticleInspectionTypeChangeReq(@RequestBody Params.ReticleInspectionTypeChangeReqParams params);
}
