package com.fa.cim.controller.tcc.cancel;

import com.fa.cim.common.support.Response;
import com.fa.cim.controller.interfaces.plan.IPlanController;
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
 * @date: 2019/7/30 16:31
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Service("PlanCancel")
@Transactional(rollbackFor = Exception.class)
public class PlanCancel implements IPlanController {
    @Override
    public Response prodOrderChangeReq(Params.ProdOrderChangeReqParams prodOrderChangeReqParams) {
        return null;
    }

    @Override
    public Response lotExtPriorityModifyReq(Params.LotExtPriorityModifyReqParams params) {
        return null;
    }

    @Override
    public Response lotCurrentQueueReactivateReq(Params.LotCurrentQueueReactivateReqParams params) {
        return null;
    }

    @Override
    public Response lotPlanChangeReq(Params.LotScheduleChangeReqParams params) {
        return null;
    }

    @Override
    public Response newProdOrderCancelReq(Params.NewProdOrderCancelReqParams newProdOrderCancelReqParams) {
        return null;
    }

    @Override
    public Response newProdOrderCreateReq(Params.NewProdOrderCreateReqParams newProdOrderCreateReqParams) {
        return null;
    }

    @Override
    public Response newProdOrderModifyReq(Params.NewProdOrderModifyReqParams params) {
        return null;
    }

    @Override
    public Response lotPlanChangeReserveCreateReq(Params.LotPlanChangeReserveCreateReqParams params) {
        return null;
    }

    @Override
    public Response lotPlanChangeReserveModifyReq(Params.LotPlanChangeReserveModifyReqParams params) {
        return null;
    }

    @Override
    public Response lotPlanChangeReserveCancelReq(Params.LotPlanChangeReserveCancelReqParams params) {
        return null;
    }

    @Override
    public Response stepContentResetByLotReq(Params.StepContentResetByLotReqParams params) {
        return null;
    }
}