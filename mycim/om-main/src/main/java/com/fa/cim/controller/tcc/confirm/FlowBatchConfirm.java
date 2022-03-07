package com.fa.cim.controller.tcc.confirm;

import com.fa.cim.common.support.Response;
import com.fa.cim.controller.interfaces.flowBatch.IFlowBatchController;
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
 * @date: 2019/7/29 14:16
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Service("FlowBatchConfirm")
@Transactional(rollbackFor = Exception.class)
public class FlowBatchConfirm implements IFlowBatchController {
    @Override
    public Response flowBatchByManualActionReq(Params.FlowBatchByManualActionReqParam param) {
        return null;
    }

    @Override
    public Response flowBatchLotRemoveReq(Params.FlowBatchLotRemoveReq params) {
        return null;
    }

    @Override
    public Response autoFlowBatchByManualActionReq(Params.FlowBatchByAutoActionReqParams params) {
        return null;
    }

    @Override
    public Response eqpMaxFlowbCountModifyReq(Params.EqpMaxFlowbCountModifyReqParams params) {
        return null;
    }

    @Override
    public Response eqpReserveCancelForflowBatchReq(Params.EqpReserveCancelForflowBatchReqParams params) {
        return null;
    }

    @Override
    public Response eqpReserveForFlowBatchReq(Params.EqpReserveForFlowBatchReqParam params) {
        return null;
    }

    @Override
    public Response flowBatchCheckForLotSkipReq(Params.FlowBatchCheckForLotSkipReqParams params) {
        return null;
    }

    @Override
    public Response reFlowBatchByManualActionReq(Params.ReFlowBatchByManualActionReqParam param) {
        return null;
    }
}