package com.fa.cim.controller.tcc.confirm;

import com.fa.cim.common.support.Response;
import com.fa.cim.controller.interfaces.transferManagementSystem.ITransferManagementSystemController;
import com.fa.cim.dto.Params;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/7/30          ********            Nyx                create file
 *
 * @author: Nyx
 * @date: 2019/7/30 15:03
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Service("TransferManagementSystemConfirm")
@Transactional(rollbackFor = Exception.class)
public class TransferManagementSystemConfirm implements ITransferManagementSystemController {
    @Override
    public Response carrierReserveCancelReq(Params.LotCassetteReserveCancelParams params) {
        return null;
    }

    @Override
    public Response carrierReserveReq(Params.CarrierReserveReqParam params) {
        return null;
    }

    @Override
    public Response carrierTransferJobEndRpt(Params.CarrierTransferJobEndRptParams carrierTransferJobEndRptParams) {
        return null;
    }

    @Override
    public Response carrierTransferStatusChangeRpt(Params.CarrierTransferStatusChangeRptParams carrierTransferStatusChangeRptParams) {
        return null;
    }

    @Override
    public Response carrierTransferJobDeleteReq(Params.CarrierTransferJobDeleteReqParam params) {
        return null;
    }

    @Override
    public Response carrierTransferForIBReq(Params.CastDeliveryReqParam castDeliveryReqParam) {
        return null;
    }

    @Override
    public Response carrierTransferReq(Params.CastDeliveryReqParam castDeliveryReqParam) {
        return null;
    }

    @Override
    public Response fmcCarrierTransferReq(Params.CastDeliveryReqParam castDeliveryReqParam) {
        return null;
    }

    @Override
    public Response singleCarrierTransferReq(Params.SingleCarrierTransferReqParam params) {
        return null;
    }

    @Override
    public Response multipleCarrierTransferReq(Params.MultipleCarrierTransferReqParam params) {
        return null;
    }

    @Override
    public Response durableTransferJobStatusRpt(Params.DurableTransferJobStatusRptParams params) {
        return null;
    }

    @Override
    public Response stockerInventoryUploadReq(Params.StockerInventoryUploadReqParam params) {
        return null;
    }

    @Override
    public Response stockerInventoryRpt(Params.StockerInventoryRptParam params) {
        return null;
    }

    @Override
    public Response stockerStatusChangeRpt(Params.StockerStatusChangeRptParams params) {
        return null;
    }

    @Override
    public Response npwCarrierReserveForIBReq(Params.NPWCarrierReserveForIBReqParams params) {
        return null;
    }

    @Override
    public Response npwCarrierReserveReq(Params.NPWCarrierReserveReqParams params) {
        return null;
    }

    @Override
    public Response npwCarrierReserveCancelForIBReq(Params.NPWCarrierReserveCancelReqParm params) {
        return null;
    }

    @Override
    public Response npwCarrierReserveCancelReq(Params.NPWCarrierReserveCancelReqParm params) {
        return null;
    }

    @Override
    public Response dmsTransferReq(Params.CastDeliveryReqParam params) {
        return null;
    }

    @Override
    public Response dmsTransferForIBReq(Params.CastDeliveryReqParam params) {
        return null;
    }
}