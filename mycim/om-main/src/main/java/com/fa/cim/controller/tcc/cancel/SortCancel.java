package com.fa.cim.controller.tcc.cancel;

import com.fa.cim.common.support.Response;
import com.fa.cim.controller.interfaces.sort.ISortController;
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
 * @date: 2019/7/30 15:32
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Service("SortCancel")
@Transactional(rollbackFor = Exception.class)
public class SortCancel implements ISortController {
    @Override
    public Response waferSlotmapChangeReq(Params.WaferSlotmapChangeReqParams param) {
        return null;
    }

    @Override
    public Response onlineSorterActionExecuteReq(Params.OnlineSorterActionExecuteReqParams params) {
        return null;
    }

    @Override
    public Response onlineSorterRpt(Params.OnlineSorterRptParams params) {
        return null;
    }

    @Override
    public Response onlineSorterSlotmapCompareReq(Params.OnlineSorterSlotmapCompareReqParams onlineSorterSlotmapCompareReqParams) {
        return null;
    }

    @Override
    public Response waferSorterActionRegisterReq(Params.WaferSorterActionRegisterReqParams params) {
        return null;
    }

    @Override
    public Response onlineSorterSlotmapAdjustReq(Params.OnlineSorterSlotmapAdjustReqParam params) {
        return null;
    }

    @Override
    public Response onlineSorterActionCancelReq(Params.OnlineSorterActionCancelReqParm params) {
        return null;
    }

    @Override
    public Response carrierExchangeReq(Params.CarrierExchangeReqParams params) {
        return null;
    }

    @Override
    public Response sjCreateReq(Params.SJCreateReqParams params) {
        return null;
    }

    @Override
    public Response sjStartReq(Params.SJStartReqParams params) {
        return null;
    }

    @Override
    public Response sjConditionCheckReq(Params.SortJobCheckConditionReqInParam params) {
        return null;
    }

    @Override
    public Response sjCancelReq(Params.SJCancelReqParm params) {
        return null;
    }

    @Override
    public Response sjPriorityChangeReq(Params.SortJobPriorityChangeReqParam params) {
        return null;
    }

    @Override
    public Response sjStatusChgRpt(Params.SJStatusChgRptParams params) {
        return null;
    }
}