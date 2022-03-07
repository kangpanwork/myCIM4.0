package com.fa.cim.controller.tcc.cancel;

import com.fa.cim.common.support.Response;
import com.fa.cim.controller.interfaces.durable.IDurableController;
import com.fa.cim.dto.Params;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/7/26          ********            Nyx                create file
 *
 * @author: Nyx
 * @date: 2019/7/26 16:26
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Service("DurableCancel")
@Transactional(rollbackFor = Exception.class)
public class DurableCancel implements IDurableController {
    @Override
    public Response durableSetReq(Params.DurableSetReqParams params) {
        return null;
    }

    @Override
    public Response multiDurableStatusChangeReq(Params.MultiDurableStatusChangeReqParams params) {
        return null;
    }

    @Override
    public Response reticleAllInOutRpt(Params.ReticleAllInOutRptParams params) {
        return null;
    }

    @Override
    public Response multipleReticlePodStatusChangeRpt(Params.MultipleReticlePodStatusChangeRptParam params) {
        return null;
    }

    @Override
    public Response reticlePodMaintInfoUpdateReq(Params.ReticlePodMaintInfoUpdateReqParam params) {
        return null;
    }

    @Override
    public Response reticlePodTransferStatusChangeRpt(Params.ReticlePodTransferStatusChangeRptParams params) {
        return null;
    }

    @Override
    public Response reticleSortRpt(Params.ReticleSortRptParam param) {
        return null;
    }

    @Override
    public Response carrierStatusChangeRpt(Params.CarrierStatusChangeRptInParams carrierStatusChangeRptInParams) {
        return null;
    }

    @Override
    public Response multipleCarrierStatusChangeRpt(Params.MultipleCarrierStatusChangeRptParms multipleCarrierStatusChangeRptParms) {
        return null;
    }

    @Override
    public Response carrierUsageCountResetReq(Params.CarrierUsageCountResetReqParms carrierUsageCountResetReqParms) {
        return null;
    }

    @Override
    public Response reticleStatusChangeRpt(Params.ReticleStatusChangeRptParams reticleStatusChangeRptParams) {
        return null;
    }

    @Override
    public Response multipleReticleStatusChangeRpt(Params.MultipleReticleStatusChangeRptParams multipleReticleStatusChangeRptParams) {
        return null;
    }

    @Override
    public Response reticleUsageCountResetReq(Params.ReticleUsageCountResetReqParams reticleUsageCountResetReqParams) {
        return null;
    }

    @Override
    public Response reticleTransferStatusChangeRpt(Params.ReticleTransferStatusChangeRptParams reticleTransferStatusChangeRptParams) {
        return null;
    }

    @Override
    public Response reticlePodInvUpdateRpt(Params.ReticlePodInvUpdateRptParam params) {
        return null;
    }

    @Override
    public Response drbBankInReq(Params.DurableBankInReqInParam durableBankInReqInParam) {
        return null;
    }

    @Override
    public Response drbBankInCancelReq(Params.DurableBankInCancelReqInParam durableBankInCancelReqInParam) {
        return null;
    }

    @Override
    public Response drbBankMoveReq(Params.DurableBankMoveReqParam durableBankMoveReqParam) {
        return null;
    }

    @Override
    public Response drbDeleteReq(Params.DurableDeleteParam durableDeleteParam) {
        return null;
    }


    @Override
    public Response drbMoveInReq(Params.DurableOperationStartReqInParam durableOperationStartReqInParam) {
        return null;
    }

    @Override
    public Response drbMoveInForIBReq(Params.DurableOperationStartReqInParam durableOperationStartReqInParam) {
        return null;
    }

    @Override
    public Response drbLagTimeActionReq(Params.DurableProcessLagTimeUpdateReqInParm durableProcessLagTimeUpdateReqInParm) {
        return null;
    }

    @Override
    public Response drbPostTaskRegistReq(Params.DurablePostProcessActionRegistReqInParam durablePostProcessActionRegistReqInParam) {
        return null;
    }

    @Override
    public Response drbPrfCxDeleteReq(Params.DurablePFXDeleteReqInParam durablePFXDeleteReqInParam) {
        return null;
    }

    @Override
    public Response drbPrfCxCreateReq(Params.DurablePFXCreateReqInParam durablePFXCreateReqInParam) {
        return null;
    }

    @Override
    public Response drbMoveInCancelReq(Params.DurableOperationStartCancelReqInParam durableOperationStartCancelReqInParam) {
        return null;
    }

    @Override
    public Response drbMoveInCancelForIBReq(Params.DurableOperationStartCancelReqInParam durableOperationStartCancelReqInParam) {
        return null;
    }

    @Override
    public Response drbBankInByPostTaskReq(Params.DurableBankInByPostProcReqInParam durableBankInByPostProcReqInParam) {
        return null;
    }

    @Override
    public Response drbCJStatusChangeReq(Params.DurableControlJobManageReqInParam durableControlJobManageReqInParam) {
        return null;
    }

    @Override
    public Response drbPassThruReq(Params.DurableGatePassReqInParam durableGatePassReqInParam) {
        return null;
    }

    @Override
    public Response drbMoveOutReq(Params.DurableOpeCompReqInParam durableOpeCompReqInParam) {
        return null;
    }

    @Override
    public Response drbSkipReq(Params.DurableOpeLocateReqInParam durableOpeLocateReqInParam) {
        return null;
    }

    @Override
    public Response holdDrbReleaseReq(Params.HoldDurableReleaseReqParams params) {
        return null;
    }

    @Override
    public Response drbReworkCancelReq(Params.ReworkDurableCancelReqParams params) {
        return null;
    }

    @Override
    public Response drbReworkReq(Params.ReworkDurableReqInParam strReworkDurableReqInParam) {
        return null;
    }

    @Override
    public Response holdDrbReq(Params.HoldDurableReqParams holdDurableReqParams) {
        return null;
    }

    @Override
    public Response drbForceSkipReq(Params.DurableForceOpeLocateReqInParam strDurableForceOpeLocateReqInParam) {
        return null;
    }

    @Override
    public Response drbMoveInReserveCancelReq(Params.StartDurablesReservationCancelReqInParam strStartDurablesReservationCancelReqInParam) {
        return null;
    }

    @Override
    public Response drbMoveInReserveCancelForIBReq(Params.StartDurablesReservationCancelReqInParam strStartDurablesReservationCancelReqInParam) {
        return null;
    }

    @Override
    public Response drbMoveInReserveReq(Params.StartDurablesReservationReqInParam strStartDurablesReservationReqInParam) {
        return null;
    }

    @Override
    public Response drbMoveInReserveForIBReq(Params.StartDurablesReservationReqInParam strStartDurablesReservationReqInParam) {
        return null;
    }

    @Override
    public Response drbMoveOutForIBReq(Params.DurableOpeCompReqInParam durableOpeCompReqInParam) {
        return null;
    }

    @Override
    public Response durableJobStatusChangeReq(Params.DurableJobStatusChangeReqParams params) {
        return null;
    }

    @Override
    public Response durableJobStatusChangeRpt(Params.DurableJobStatusChangeRptParams params) {
        return null;
    }

    @Override
    public Response reworkDrbWithHoldReleaseReq(Params.ReworkDurableWithHoldReleaseReqInParam strReworkDurableWithHoldReleaseReqInParam) {
        return null;
    }

    @Override
    public Response reticlePodPortAccessModeChgReq(Params.EqpRSPPortAccessModeChangeReqParams params) {
        return null;
    }

    @Override
    public Response reticleRetrieveByOffline(Params.ReticleOfflineRetrieveReqParams params) {
        return null;
    }

    @Override
    public Response reticlePodPortStatusChgRpt(Params.EqpRSPPortStatusChangeRpt params) {
        return null;
    }

    @Override
    public Response reticleRetrieveReq(Params.ReticleRetrieveReqParams params) {
        return null;
    }

    @Override
    public Response reticleStoreByOffline(Params.ReticleOfflineStoreReqParams params) {
        return null;
    }

    @Override
    public Response reticleRetrieveRpt(Params.ReticleRetrieveRptParams params) {
        return null;
    }

    @Override
    public Response reticleStoreReq(Params.ReticleStoreReqParams params) {
        return null;
    }

    @Override
    public Response reticleStoreRpt(Params.ReticleStoreRptParams params) {
        return null;
    }

    @Override
    public Response reticlePodUnloadRpt(Params.ReticlePodUnloadingRptParams params) {
        return null;
    }

    @Override
    public Response reticlePodLoadRpt(Params.ReticlePodLoadingRptParams params) {
        return null;
    }

    @Override
    public Response bRSOnlineModeChgReq(Params.BareReticleStockerOnlineModeChangeReqParams params) {
        return null;
    }

    @Override
    public Response reticlePodInvUpdateReq(Params.ReticlePodInventoryReqParams params) {
        return null;
    }

    @Override
    public Response reticlePodLoadByOfflineReq(Params.ReticlePodOfflineLoadingReqParams params) {
        return null;
    }

    @Override
    public Response reticlePodUnloadByOfflineReq(Params.ReticlePodOfflineUnloadingReqParams params) {
        return null;
    }

    @Override
    public Response reticleInvUpdateReq(Params.ReticleInventoryReqParams params) {
        return null;
    }
}