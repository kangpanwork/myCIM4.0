package com.fa.cim.controller.tcc.cancel;

import com.fa.cim.common.support.Response;
import com.fa.cim.controller.interfaces.equipment.IEquipmentController;
import com.fa.cim.dto.Params;
import com.fa.cim.eqp.carrierout.CarrierOutPortReqParams;
import com.fa.cim.eqp.carrierout.CarrierOutReqParams;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/7/29          ********            Nyx                create file
 *
 * @author: Nyx
 * @date: 2019/7/29 10:47
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Service("EquipmentCancel")
@Transactional(rollbackFor = Exception.class)
public class EquipmentCancel implements IEquipmentController {
    @Override
    public Response chamberStatusChangeReq(Params.ChamberStatusChangeReqPrams params) {
        return null;
    }

    @Override
    public Response chamberStatusChangeRpt(Params.ChamberStatusChangeRptPrams params) {
        return null;
    }

    @Override
    public Response EqpModeChangeReq(Params.EqpModeChangeReqPrams params) {
        return null;
    }

    @Override
    public Response eqpMemoAddReq(Params.EqpMemoAddReqParams eqpMemoAddReqParams) {
        return null;
    }

    @Override
    public Response portStatusChangeRpt(Params.PortStatusChangeRptParam params) {
        return null;
    }

    @Override
    public Response EqpEAPStatusSyncReq(Params.EqpEAPStatusSyncReqPrams params) {
        return null;
    }

    @Override
    public Response eqpStatusChangeReq(Params.EqpStatusChangeReqParams eqpStatusChangeReqParams) {
        return null;
    }

    @Override
    public Response eqpStatusChangeRpt(Params.EqpStatusChangeRptParams eqpStatusChangeRptParams) {
        return null;
    }

    @Override
    public Response eqpStatusResetReq(Params.EqpStatusResetReqParams params) {
        return null;
    }

    @Override
    public Response eqpUsageCountResetReq(Params.EqpUsageCountResetReqParam params) {
        return null;
    }

    @Override
    public Response carrierLoadingRpt(Params.loadOrUnloadLotRptParams param) {
        return null;
    }

    @Override
    public Response carrierLoadingVerifyReq(Params.CarrierLoadingVerifyReqParams carrierLoadingVerifyReqParams) {
        return null;
    }

    @Override
    public Response moveOutReq(Params.OpeComWithDataReqParams opeComWithDataReqParams) {
        return null;
    }

    @Override
    public Response moveInCancelForIBReq(Params.MoveInCancelForIBReqParams params) {
        return null;
    }

    @Override
    public Response moveInCancelReq(Params.MoveInCancelReqParams params) {
        return null;
    }

    @Override
    public Response moveInForIBReq(Params.MoveInForIBReqParams moveInForIBReqParams) {
        return null;
    }

    @Override
    public Response moveInReq(Params.MoveInReqParams params) {
        return null;
    }

    @Override
    public Response uncarrierLoadingRpt(Params.loadOrUnloadLotRptParams param) {
        return null;
    }

    @Override
    public Response eapRecoveryReq(Params.EAPRecoveryReqParam params) {
        return null;
    }

    @Override
    public Response forceMoveOutReq(Params.ForceMoveOutReqParams params) {
        return null;
    }

    @Override
    public Response carrierOutFromIBReq(Params.CarrierOutFromIBReqParam param){return null;};

    @Override
    public Response carrierMoveFromIBRpt(Params.CarrierMoveFromIBRptParams params) {
        return null;
    }

    @Override
    public Response runningHoldReq(Params.RunningHoldReqParams params) {
        return null;
    }

    @Override
    public Response carrierLoadingForIBRpt(Params.CarrierLoadingForIBRptParams carrierLoadingForIBRptParams) {
        return null;
    }

    @Override
    public Response carrierMoveToIBRpt(Params.CarrierMoveToIBRptParams carrierMoveToIBRptParams) {
        return null;
    }

    @Override
    public Response uncarrierLoadingForIBRpt(Params.CarrierUnloadingForIBRptParams uncarrierLoadingForIBRptParams) {
        return null;
    }

    @Override
    public Response carrierLoadingVerifyForIBReq(Params.CarrierLoadingVerifyForIBReqParams params) {
        return null;
    }

    @Override
    public Response moveOutForIBReq(Params.MoveOutForIBReqParams params) {
        return null;
    }

    @Override
    public Response partialMoveOutReq(Params.PartialMoveOutReqParams params) {
        return null;
    }

    @Override
    public Response moveOutWithRunningSplitForIBReq(Params.PartialMoveOutReqParams params) {
        return null;
    }

    @Override
    public Response chamberWithProcessWaferRpt(Params.ChamberWithProcessWaferRptInParams params) {
        return null;
    }

    @Override
    public Response waferPositionWithProcessResourceRpt(Params.WaferPositionWithProcessResourceRptParam param) {
        return null;
    }

    @Override
    public Response eqpBufferTypeModifyReq(Params.EqpBufferTypeModifyReqInParm params) {
        return null;
    }

    @Override
    public Response reserveUnloadingLotsForIBRpt(Params.ReserveUnloadingLotsForIBRptParams params) {
        return null;
    }

    @Override
    public Response ReserveCancelUnloadingLotsForIBReq(Params.ReserveCancelUnloadingLotsForIBReqParams params) {
        return null;
    }

    @Override
    public Response ForceMoveOutForIBReq(Params.ForceMoveOutForIBReqParams params) {
        return null;
    }

    @Override
    public Response cxEqpAlarmRpt(Params.EqpAlarmRptParams eqpAlarmRptParams) {
        return null;
    }

    @Override
    public Response processStatusRpt(Params.ProcessStatusRptParam param) {
        return null;
    }

    @Override
    public Response carrierLoadingForSRTRpt(Params.CarrierLoadingForSORRptParams param) {
        return null;
    }

    @Override
    public Response carrierUnloadingForSRTRpt(Params.CarrierUnloadingForSORRptParams param) {
        return null;
    }

    @Override
    public Response carrierOutPortReq(CarrierOutPortReqParams params) {
        return null;
    }

    @Override
    public Response carrierOutReq(CarrierOutReqParams params) {
        return null;
    }

}