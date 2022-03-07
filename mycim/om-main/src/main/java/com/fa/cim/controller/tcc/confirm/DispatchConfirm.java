package com.fa.cim.controller.tcc.confirm;

import com.fa.cim.common.support.Response;
import com.fa.cim.controller.interfaces.dispatch.IDispatchController;
import com.fa.cim.dto.Params;
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
 * @date: 2019/7/29 13:39
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Service("DispatchConfirm")
@Transactional(rollbackFor = Exception.class)
public class DispatchConfirm implements IDispatchController {
    @Override
    public Response moveInReserveCancelReq(Params.MoveInReserveCancelReqParams params) {
        return null;
    }

    @Override
    public Response moveInReserveReq(Params.MoveInReserveReqParams moveInReserveReqParams) {
        return null;
    }

    @Override
    public Response moveInReserveCancelForIBReq(Params.MoveInReserveCancelForIBReqParams params) {
        return null;
    }

    @Override
    public Response moveInReserveForIBReq(Params.MoveInReserveForIBReqParams params) {
        return null;
    }

    @Override
    public Response autoDispatchConfigModifyReq(Params.AutoDispatchConfigModifyReqParams autoDispatchConfigModifyReqParams) {
        return null;
    }

    @Override
    public Response carrierDispatchAttrChgReq(Params.CarrierDispatchAttrChgReqParm carrierDispatchAttrChgReqParm) {
        return null;
    }

    @Override
    public Response eqpFullAutoConfigChgReq(Params.EqpFullAutoConfigChgReqInParm params) {
        return null;
    }

    @Override
    public Response eqpAutoMoveInReserveReq(Params.EqpAutoMoveInReserveReqParams params) {
        return null;
    }
}