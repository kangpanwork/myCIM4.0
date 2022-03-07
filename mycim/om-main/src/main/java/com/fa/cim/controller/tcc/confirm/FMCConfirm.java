package com.fa.cim.controller.tcc.confirm;

import com.fa.cim.common.support.Response;
import com.fa.cim.controller.interfaces.fmc.IFMCController;
import com.fa.cim.dto.Params;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>FMCConfirm .
 * change history:
 * date                      defect#             person            comments
 * -------------------------------------------------------------------------------------------------------------------
 * 2020/4/21 11:08         ********              ZQI             create file.
 *
 * @author ZQI
 * @version 1.0
 * @date 2020/4/21 11:08
 * @copyright 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 * @since JDK1.8
 */
@Service("FMCConfirm")
@Transactional(rollbackFor = Exception.class)
public class FMCConfirm implements IFMCController {
    @Override
    public Response fmcMoveInReserveReq(Params.SLMStartLotsReservationReqInParams slmStartLotsReservationReqInParams) {
        return null;
    }

    @Override
    public Response fmcWaferRetrieveCarrierReserveReq(Params.SLMWaferRetrieveCassetteReserveReqInParams slmWaferRetrieveCassetteReserveReqInParams) {
        return null;
    }

    @Override
    public Response fmcProcessJobStatusRpt(Params.SLMProcessJobStatusRptInParams slmProcessJobStatusRptInParams) {
        return null;
    }

    @Override
    public Response fmcWaferStoreRpt(Params.SLMWaferStoreRptInParams slmWaferStoreRptInParams) {
        return null;
    }

    @Override
    public Response fmcWaferRetrieveRpt(Params.SLMWaferRetrieveRptInParams slmWaferRetrieveRptInParams) {
        return null;
    }

    @Override
    public Response fmcCarrierRemoveFromCJReq(Params.SLMCassetteDetachFromCJReqInParams slmCassetteDetachFromCJReqInParams) {
        return null;
    }

    @Override
    public Response fmcCarrierUnclampReq(Params.SLMCassetteUnclampReqInParams slmCassetteUnclampReqInParams) {
        return null;
    }

    @Override
    public Response fmcModeChangeReq(Params.SLMSwitchUpdateReqInParams slmSwitchUpdateReqInParams) {
        return null;
    }

    @Override
    public Response fmcRsvMaxCountUpdateReq(Params.FmcRsvMaxCountUpdateReqInParams fmcRsvMaxCountUpdateReqInParams) {
        return null;
    }
}
