package com.fa.cim.service.reticle;

import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;

public interface IReticleService {

    Results.ReticleOperationExceptionResult sxReticleInspectionRequst(Infos.ObjCommon objCommon, Params.ReticleInspectionReuqstReqParams params);

    Results.ReticleOperationExceptionResult sxReticleInspectionIn(Infos.ObjCommon objCommon, Params.ReticleInspectionInReqParams params);

    Results.ReticleOperationExceptionResult sxReticleInspectionOut(Infos.ObjCommon objCommon, Params.ReticleInspectionOutReqParams params);

    Results.ReticleOperationExceptionResult sxConfirmMaskQuality(Infos.ObjCommon objCommon, Params.ReticleConfirmMaskQualityParams params);

    Results.ReticleOperationExceptionResult sxReticleHold(Infos.ObjCommon objCommon, Params.ReticleHoldReqParams params);

    Results.ReticleOperationExceptionResult sxReticleHoldRelease(Infos.ObjCommon objCommon, Params.ReticleHoldReleaseReqParams params);

    Results.ReticleOperationExceptionResult sxReticleRequestRepair(Infos.ObjCommon objCommon, Params.ReticleRequestRepairReqParams params);

    Results.ReticleOperationExceptionResult sxReticleRepairIn(Infos.ObjCommon objCommon, Params.ReticleRepairInReqParams params);

    Results.ReticleOperationExceptionResult sxReticleRepairOut(Infos.ObjCommon objCommon, Params.ReticleRepairOutReqParams params);

    Results.ReticleOperationExceptionResult sxReticleTerminate(Infos.ObjCommon objCommon, Params.ReticleTerminateReqParams params);

    Results.ReticleOperationExceptionResult sxReticleTerminateCancel(Infos.ObjCommon objCommon, Params.ReticleTerminateReqParams params);

    Results.ReticleOperationExceptionResult sxReticleScrap(Infos.ObjCommon objCommon, Params.ReticleScrapReqParams params);

    Results.ReticleOperationExceptionResult sxReticleScrapCancel(Infos.ObjCommon objCommon, Params.ReticleScrapReqParams params);

    /*   reticle scan request, avaliable on idle and hold status
    * description:
    * change history:
    * date             defect             person             comments
    * ---------------------------------------------------------------------------------------------------------------------
    * 2021/8/18 11:28                      Decade                Create
    *
    * @author Decade
    * @date 2021/8/18 11:28
    * @param null -
    * @return
    */
    void sxReticleScanRequest(Infos.ObjCommon objCommon, Params.ReticleScanRequestReqParams params);

    /*       reticle scan complete, avaliable on waitscan status
    * description:
    * change history:
    * date             defect             person             comments
    * ---------------------------------------------------------------------------------------------------------------------
    * 2021/8/18 11:29                      Decade                Create
    *
    * @author Decade
    * @date 2021/8/18 11:29
    * @param null -
    * @return
    */
    void sxReticleScanComplete(Infos.ObjCommon objCommon, Params.ReticleScanCompleteReqParams params);

    /*     reticle inspection type change, avaliable on waitinspection status
    * description:
    * change history:
    * date             defect             person             comments
    * ---------------------------------------------------------------------------------------------------------------------
    * 2021/8/18 11:29                      Decade                Create
    *
    * @author Decade
    * @date 2021/8/18 11:29
    * @param null -
    * @return
    */
    void sxReticleInspectionTypeChange(Infos.ObjCommon objCommon, Params.ReticleInspectionTypeChangeReqParams params);
}
