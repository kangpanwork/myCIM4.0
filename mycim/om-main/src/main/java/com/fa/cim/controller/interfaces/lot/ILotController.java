package com.fa.cim.controller.interfaces.lot;

import com.fa.cim.common.support.Response;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.lot.TerminateReq;
import com.fa.cim.lot.LotNpwUsageRecycleCountUpdateParams;
import com.fa.cim.lot.LotNpwUsageRecycleLimitUpdateParams;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/7/26          ********            Nyx                create file
 *
 * @author: Nyx
 * @date: 2019/7/26 14:59
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface ILotController {
    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/6/26        ********             Jerry               create file
     *
     * @author: Jerry
     * @date: 2018/6/26 15:37
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response holdLotReleaseReq(Params.HoldLotReleaseReqParams holdLotReleaseReqParams);

    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/6/26        ********             Jerry               create file
     *
     * @author: Jerry
     * @date: 2018/6/26 15:37
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response holdLotReq(Params.HoldLotReqParams holdLotReqParams);

    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/10/22        ********             Jerry               create file
     *
     * @author: Jerry
     * @date: 2018/10/22 9:26
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response mergeLotNotOnPfReq(Params.MergeLotNotOnPfReqParams mergeLotNotOnPfReqParams);

    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/10/16        ********             Jerry               create file
     *
     * @author: Jerry
     * @date: 2018/10/16 14:21
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response mergeLotReq(Params.MergeLotReqParams mergeLotReqParams);

    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/7/13        ********            Jerry              create file
     * 2018/12/28                           Nyx                modify context
     *
     * @author: Jerry
     * @date: 2018/7/13 13:20
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response skipReq(Params.SkipReqParams params);

    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 10/30/2018        ********             Sun               create file
     *
     * @author: Sun
     * @date: 10/30/2018 9:55 AM
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response partialReworkReq(Params.PartialReworkReqParams params);

    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/10/30        ********             Jerry               create file
     *
     * @author: Jerry
     * @date: 2018/10/30 16:14
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response partialReworkWithHoldReleaseReq(Params.PartialReworkWithHoldReleaseReqParams partialReworkWithHoldReleaseReqParams);

    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/10/29       ********              Nyx             create file
     *
     * @author: Nyx
     * @date: 2018/10/29 13:35
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response partialReworkWithoutHoldReleaseReq(Params.PartialReworkWithoutHoldReleaseReqParams params);

    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 11/2/2018        ********             Sun               create file
     *
     * @author: Sun
     * @date: 11/2/2018 2:58 PM
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response partialReworkCancelReq(Params.PartialReworkCancelReqParams params);

    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/10/22        ********             Bear               create file
     *
     * @author: Bear
     * @date: 2018/10/22 16:22
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response reworkReq(Params.ReworkReqParams params);

    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/11/5        ********             Bear               create file
     *
     * @author: Bear
     * @date: 2018/11/5 11:14
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response reworkCancelReq(Params.ReworkCancelReqParams params);

    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/10/30        ********             Jerry               create file
     *
     * @author: Jerry
     * @date: 2018/10/30 16:56
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response reworkWithHoldReleaseReq(Params.ReworkWithHoldReleaseReqParams reworkWithHoldReleaseReqParams);

    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/10/15        ********             Jerry               create file
     *
     * @author: Jerry
     * @date: 2018/10/15 13:44
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response splitLotNotOnPfReq(Params.SplitLotNotOnPfReqParams splitLotNotOnPfReqParams);

    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/9/17        ********             Jerry               create file
     *
     * @author: Jerry
     * @date: 2018/9/17 14:18
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response splitLotReq(Params.SplitLotReqParams splitLotReqParams);

    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/10/23        ********             Jerry               create file
     *
     * @author: Jerry
     * @date: 2018/10/23 14:12
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response splitLotWithHoldReleaseReq(Params.SplitLotWithHoldReleaseReqParams splitLotWithHoldReleaseReqParams);

    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/10/23        ********             Jerry               create file
     *
     * @author: Jerry
     * @date: 2018/10/23 16:14
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response splitLotWithoutHoldReleaseReq(Params.SplitLotWithoutHoldReleaseReqParams splitLotWithoutHoldReleaseReqParams);

    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/10/25        ********             Jerry               create file
     *
     * @author: Jerry
     * @date: 2018/10/25 11:04
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response branchCancelReq(Params.BranchCancelReqParams branchCancelReqParams);

    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 4/16/2019        ********             Sun               create file
     *
     * @author: Sun
     * @date: 4/16/2019 4:13 PM
     * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response subRouteBranchReq(Params.SubRouteBranchReqParams params);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/11/23                            Wind                create file
     *
     * @author: Wind
     * @date: 2018/11/23 10:05
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response forceSkipReq(Params.SkipReqParams params);

    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 12/10/2018        ********             Sun               create file
     *
     * @author: Sun
     * @date: 12/10/2018 11:31 AM
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response reworkWholeLotReq(Params.ReworkWholeLotReqParams params);

    /**
     * description:
     * <p>singleCarrierTransferReqController .<br/></p>
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/11/1        ********             Yuri               create file
     *
     * @author: Yuri
     * @date: 2018/11/1 11:29
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response passThruReq(Params.PassThruReqParams params);

    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/11/5       ********              Nyx             create file
     *
     * @author: Nyx
     * @date: 2018/11/5 10:50
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response branchReq(Params.BranchReqParams params);

    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/11/8       ********              Nyx             create file
     *
     * @author: Nyx
     * @date: 2018/11/8 14:01
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response branchWithHoldReleaseReq(Params.BranchWithHoldReleaseReqParams params);

    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/9/17         ********              Nyx             create file
     *
     * @author: Nyx
     * @date: 2018/9/27 14:21
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response unscrapWaferReq(Params.UnscrapWaferReqParams params);

    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/9/17         ********              Nyx             create file
     *
     * @author: Nyx
     * @date: 2018/10/9 10:11
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response unscrapWaferNotOnPfReq(Params.ScrapWaferNotOnPfReqParams params);

    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/9/17         ********              Nyx             create file
     *
     * @author: Nyx
     * @date: 2018/10/8 14:00
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response scrapWaferNotOnPfReq(Params.ScrapWaferNotOnPfReqParams params);

    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/9/17         ********              Nyx             create file
     *
     * @author: Nyx
     * @date: 2018/9/17 14:21
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response scrapWaferReq(Params.ScrapWaferReqParams params);

    /**
     * description: update lot the contamination level and pr flag
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param params - params
     * @return response
     * @author YJ
     * @date 2020/11/24 0024 9:45
     */
    Response lotContaminationUpdateReq(Params.LotContaminationParams params);

    /**
     * description: update lot hold
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param holdLotReqParams - holdLotReqParams
     * @return response
     * @author YJ
     * @date 2020/1/22 0024 9:45
     */
    Response holdDepartmentChangeReq(Params.HoldLotReqParams holdLotReqParams);

    /*
    * description:
    * change history:
    * date             defect             person             comments
    * ---------------------------------------------------------------------------------------------------------------------
    * 2021/2/22 16:46                      Decade                Create
    *
    * @author Decade
    * @date 2021/2/22 16:46
    * @param null -
    * @return
    */
    Response lotNpwUsageRecycleCountUpdateReq(LotNpwUsageRecycleCountUpdateParams params);

    /*
    * description:
    * change history:
    * date             defect             person             comments
    * ---------------------------------------------------------------------------------------------------------------------
    * 2021/2/24 14:40                      Decade                Create
    *
    * @author Decade
    * @date 2021/2/24 14:40
    * @param null -
    * @return
    */
    Response lotNpwUsageRecycleLimitUpdateReq(LotNpwUsageRecycleLimitUpdateParams params);

    /**
     * Lot Terminate
     * @version 0.1
     * @author Grant
     * @date 2021/7/7
     */
    Response lotTerminateReq(TerminateReq.TerminateReqParams params);

    /**
     * Lot Terminate Cancel
     * @version 0.1
     * @author Grant
     * @date 2021/7/7
     */
    Response lotTerminateCancelReq(TerminateReq.TerminateCancelReqParams params);

}
