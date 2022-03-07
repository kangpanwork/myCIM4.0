package com.fa.cim.controller.tcc.cancel;

import com.fa.cim.common.support.Response;
import com.fa.cim.controller.interfaces.lot.ILotController;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.lot.TerminateReq;
import com.fa.cim.lot.LotNpwUsageRecycleCountUpdateParams;
import com.fa.cim.lot.LotNpwUsageRecycleLimitUpdateParams;
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
 * @date: 2019/7/26 15:20
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Service("LotCancel")
@Transactional(rollbackFor = Exception.class)
public class LotCancel implements ILotController {

    @Override
    public Response holdLotReleaseReq(Params.HoldLotReleaseReqParams holdLotReleaseReqParams) {
        return null;
    }

    @Override
    public Response holdLotReq(Params.HoldLotReqParams holdLotReqParams) {
        return null;
    }

    @Override
    public Response mergeLotNotOnPfReq(Params.MergeLotNotOnPfReqParams mergeLotNotOnPfReqParams) {
        return null;
    }

    @Override
    public Response mergeLotReq(Params.MergeLotReqParams mergeLotReqParams) {
        return null;
    }

    @Override
    public Response skipReq(Params.SkipReqParams params) {
        return null;
    }

    @Override
    public Response partialReworkReq(Params.PartialReworkReqParams params) {
        return null;
    }

    @Override
    public Response partialReworkWithHoldReleaseReq(Params.PartialReworkWithHoldReleaseReqParams partialReworkWithHoldReleaseReqParams) {
        return null;
    }

    @Override
    public Response partialReworkWithoutHoldReleaseReq(Params.PartialReworkWithoutHoldReleaseReqParams params) {
        return null;
    }

    @Override
    public Response partialReworkCancelReq(Params.PartialReworkCancelReqParams params) {
        return null;
    }

    @Override
    public Response reworkReq(Params.ReworkReqParams params) {
        return null;
    }

    @Override
    public Response reworkCancelReq(Params.ReworkCancelReqParams params) {
        return null;
    }

    @Override
    public Response reworkWithHoldReleaseReq(Params.ReworkWithHoldReleaseReqParams reworkWithHoldReleaseReqParams) {
        return null;
    }

    @Override
    public Response splitLotNotOnPfReq(Params.SplitLotNotOnPfReqParams splitLotNotOnPfReqParams) {
        return null;
    }

    @Override
    public Response splitLotReq(Params.SplitLotReqParams splitLotReqParams) {
        return null;
    }

    @Override
    public Response splitLotWithHoldReleaseReq(Params.SplitLotWithHoldReleaseReqParams splitLotWithHoldReleaseReqParams) {
        return null;
    }

    @Override
    public Response splitLotWithoutHoldReleaseReq(Params.SplitLotWithoutHoldReleaseReqParams splitLotWithoutHoldReleaseReqParams) {
        return null;
    }

    @Override
    public Response branchCancelReq(Params.BranchCancelReqParams branchCancelReqParams) {
        return null;
    }

    @Override
    public Response subRouteBranchReq(Params.SubRouteBranchReqParams params) {
        return null;
    }

    @Override
    public Response forceSkipReq(Params.SkipReqParams params) {
        return null;
    }

    @Override
    public Response reworkWholeLotReq(Params.ReworkWholeLotReqParams params) {
        return null;
    }

    @Override
    public Response passThruReq(Params.PassThruReqParams params) {
        return null;
    }

    @Override
    public Response branchReq(Params.BranchReqParams params) {
        return null;
    }

    @Override
    public Response branchWithHoldReleaseReq(Params.BranchWithHoldReleaseReqParams params) {
        return null;
    }

    @Override
    public Response unscrapWaferReq(Params.UnscrapWaferReqParams params) {
        return null;
    }

    @Override
    public Response unscrapWaferNotOnPfReq(Params.ScrapWaferNotOnPfReqParams params) {
        return null;
    }

    @Override
    public Response scrapWaferNotOnPfReq(Params.ScrapWaferNotOnPfReqParams params) {
        return null;
    }

    @Override
    public Response scrapWaferReq(Params.ScrapWaferReqParams params) {
        return null;
    }

    @Override
    public Response lotContaminationUpdateReq(Params.LotContaminationParams params) {
        return null;
    }

    @Override
    public Response holdDepartmentChangeReq(Params.HoldLotReqParams holdLotReqParams) {
        return null;
    }

    @Override
    public Response lotNpwUsageRecycleCountUpdateReq(LotNpwUsageRecycleCountUpdateParams params) {
        return null;
    }

    @Override
    public Response lotNpwUsageRecycleLimitUpdateReq(LotNpwUsageRecycleLimitUpdateParams params) {
        return null;
    }

    @Override
    public Response lotTerminateReq(TerminateReq.TerminateReqParams params) {
        return null;
    }

    @Override
    public Response lotTerminateCancelReq(TerminateReq.TerminateCancelReqParams params) {
        return null;
    }

}
