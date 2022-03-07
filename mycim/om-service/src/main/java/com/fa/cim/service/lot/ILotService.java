package com.fa.cim.service.lot;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;
import com.fa.cim.dto.lot.TerminateReq;
import com.fa.cim.lot.LotNpwUsageRecycleCountUpdateParams;
import com.fa.cim.lot.LotNpwUsageRecycleLimitUpdateParams;

import java.util.List;

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
public interface ILotService {

    /**
     * description: releases the specified LotHold record
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
      * @param objCommon
     * @param holdLotReleaseReqParams
     * @return void
     * @exception
     * @author ho
     * @date 2020/9/8 16:28
     */
    void sxHoldLotReleaseReq(Infos.ObjCommon objCommon, Params.HoldLotReleaseReqParams holdLotReleaseReqParams);

    /**
    * description: holds a lot at the current operation immediately
    * <p></p>
    * change history:
    * date             defect             person             comments
    * ---------------------------------------------------------------------------------------------------------------------
    * @return void
    * @exception
    * @author ho
    * @date 2020/9/8 16:29
    */
    void sxHoldLotReq(Infos.ObjCommon objCommon, ObjectIdentifier lotID, List<Infos.LotHoldReq> holdReqList);

    /**
     * description: merges not on process flow logically split Child Lot to Parent Lot.
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
      * @param objCommon
     * @param mergeLotNotOnPfReqParams
     * @return void
     * @exception
     * @author ho
     * @date 2020/9/8 16:30
     */
    void sxMergeLotNotOnPfReq(Infos.ObjCommon objCommon, Params.MergeLotNotOnPfReqParams mergeLotNotOnPfReqParams) ;

    /**
     * description: merges logically split ChildLot to Lot in other Lot Family.
     * To merge ChildLot, it is necessary that both ChildLot and Lots in Lot Family be on the same Route/Operation and be in the same Carrier.
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
    void sxMergeLotReq(Infos.ObjCommon objCommon, Params.MergeLotReqParams mergeLotReqParams) ;

    /**
     * description: Jump directly from one operator step in the process to another
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
    void sxSkipReq(Infos.ObjCommon objCommon, Params.SkipReqParams skipReqParams) ;

    /**
     * description: split a part of the specified Lot and moves it onto a Rework Route
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
    Results.PartialReworkReqResult sxPartialReworkReq(Infos.ObjCommon objCommon, Params.PartialReworkReqParams params) ;

    /**
     * description: performs Partial Rework to a Lot whose Hold State is "ONHOLD"
     * To inherit Hold Records to Child Lot from Parent Lot is possible.
     * However Process Hold, Merge Hold, Rework Hold and so on is not so.
     * The other processes are same as sxPartialReworkReq().
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
    Results.PartialReworkReqResult sxPartialReworkWithHoldReleaseReq(Infos.ObjCommon objCommon, Params.PartialReworkWithHoldReleaseReqParams partialReworkWithHoldReleaseReqParams);

    /**
     * description: performs Partial Rework to a Lot whose Hold State is "ONHOLD"
     * To inherit Hold Records to Child Lot from Parent Lot is possible.
     * However Process Hold, Merge Hold, Rework Hold and so on is not so.
     * The other processes are same as sxPartialReworkReq().
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
    Results.PartialReworkReqResult sxPartialReworkWithoutHoldReleaseReq(Infos.ObjCommon objCommon, Infos.PartialReworkReq partialReworkReq, List<Infos.LotHoldReq> lotHoldReqs,String claimMemo);

    /**
     * description: cancels Partial Rework operation and merges Child Lot to Parent Lot.
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
    void sxPartialReworkCancelReq(Infos.ObjCommon objCommon, Params.PartialReworkCancelReqParams params) ;

    /**
     * description: moves the specified Lot to Rework Route.
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
    void sxReworkReq(Infos.ObjCommon objCommon, Infos.ReworkReq reworkReq, String claimMemo);

    /**
     * description: cancels a future rework request for the target lot's operation.
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
    void sxReworkCancelReq(Infos.ObjCommon objCommon, Params.ReworkCancelReqParams params);

    /**
     * description: performs HoldRelease and WholeRework sequentially on Lot of which Lot Status is OnHold.
     * All Hold Records of Lot will be deleted by this function.
     * There is no need to perform HoldRelease in advance, it is possible to rework without transfer process on automation process.
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
    void sxReworkWithHoldReleaseReq(Infos.ObjCommon objCommon, Params.ReworkWithHoldReleaseReqParams reworkWithHoldReleaseReqParams);

    /**
     * description: splits Lot logically and generates Child Lot
     * applied for Lot that is not related to Route
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
    ObjectIdentifier sxSplitLotNotOnPfReq(Infos.ObjCommon objCommon, Params.SplitLotNotOnPfReqParams splitLotNotOnPfReqParams);

    /**
     * description: splits Lot logically and generates Child Lot
     * The original Lot is Parent Lot, Child Lot is created by specified wafers and Lot ID is newly assigned to Child Lot.
     * You can specify subRouteID for Branch Route to Child Lot.
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
    Results.SplitLotReqResult sxSplitLotReq(Infos.ObjCommon objCommon,
                                            Params.SplitLotReqParams splitLotReqParams);

    /**
     * description: performs HoldRelease and LotSplit sequentially on Lot of which Lot Status is OnHold
     * All Hold Records of Lot will be deleted by this
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
    Results.SplitLotReqResult sxSplitLotWithHoldReleaseReq(Infos.ObjCommon objCommon,
                                                           Params.SplitLotWithHoldReleaseReqParams splitLotWithHoldReleaseReqParams);

    /**
     * description: This function performs Split to a Lot whose Hold State is "ONHOLD".
     * To inherit Hold Records to Child Lot from Parent Lot is possible.
     * However Process Hold, Merge Hold, Rework Hold and so on is not
     * soThe other processes are same as txSplitWaferLotReq().
     * If the lot is held with "LOCK", it's not performed
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
    Results.SplitLotReqResult sxSplitLotWithoutHoldReleaseReq(Infos.ObjCommon objCommon,
                                                     Params.SplitLotWithoutHoldReleaseReqParams splitLotWithoutHoldReleaseReqParams);

    /**
     * description: cancels Branch to Sub Route.
     * moves Lot on Branch Route back to the previous Route/Operation performing Branch
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
    void sxBranchCancelReq(Infos.ObjCommon objCommon, Params.BranchCancelReqParams branchCancelReqParams);

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
    void sxSubRouteBranchReqService(Infos.ObjCommon objCommon, Params.SubRouteBranchReqParams params);

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
    void sxForceSkipReq(Infos.ObjCommon objCommon, Params.SkipReqParams params);

    /**
     * description: The whole LOT will be reworked
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
    void sxReworkWholeLotReq(Infos.ObjCommon objCommon, Params.ReworkWholeLotReqParams params);

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
    Results.PassThruReqResult sxPassThruReq(Infos.ObjCommon objCommon,
                                            Infos.GatePassLotInfo strGatePassLotInfo,
                                            String claimMemo);

    /**
     * description: moves Lot to Branch Route.
     * The purpose of Branch is to make similar process operations in advance and let the operation for Lot branched, when you want to create a product that has slight differences.
     * //
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
    void sxBranchReq(Infos.ObjCommon objCommon, Infos.BranchReq branchReq, String claimMemo);

    /**
     * description: performs HoldRelease and Branch sequentially.
     * All hold Records of Lot will be released by this
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
    void sxBranchWithHoldReleaseReq(Infos.ObjCommon objCommon, Infos.BranchReq branchReq, ObjectIdentifier releaseReasonCodeID, List<Infos.LotHoldReq> strLotHoldReleaseReqList, String claimMemo);

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
    void sxUnscrapWaferReq(Infos.ObjCommon objCommon, Params.UnscrapWaferReqParams params);

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
    void sxUnscrapWaferNotOnPfReq(Infos.ObjCommon objCommon, Params.ScrapWaferNotOnPfReqParams params);

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
    void sxScrapWaferNotOnPfReq(Infos.ObjCommon objCommon, Params.ScrapWaferNotOnPfReqParams params);

    /**
     * description: scraps a specified Wafer in Lot.
     * If Wafer which can't keep quality as product by spec over or miss operation, then the Wafer have to be scrapped.
     * This time, the Scrap State of the scrapped Wafer is changed to "Scrap".
     * Also, when all the Wafers in a Lot is specified, the Lot is considered that all Lots have been scrapped (Whole Lot Scrap) and the Scrap State of the Lot is changed to "Scrap".
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
    void sxScrapWaferReq(Infos.ObjCommon objCommon, Params.ScrapWaferReqParams params);

    /**     
     * description: update contamination update
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author YJ
     * @date 2020/11/24 0024 10:22
     * @param params - params
     * @param objCommon - obj
     */
    void sxLotContaminationUpdateReq(Params.LotContaminationParams params, Infos.ObjCommon objCommon);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author salt
     * @date 2021/1/27 16:18
     * @param objCommon - common info
     * @param lotIDLists - lot id list
     * @param action - action
     * @return
     */
    void checkLotSplitOrMerge(Infos.ObjCommon objCommon, List<ObjectIdentifier> lotIDLists,String action);

    /**
     * description: update lot hold record
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon   - common
     * @param lotID       - lot id
     * @param holdReqList - hold list
     * @author ho
     * @date 2020/9/8 16:29
     */
    void sxHoldDepartmentChangeReq(Infos.ObjCommon objCommon, ObjectIdentifier lotID, List<Infos.LotHoldReq> holdReqList);

    /*
    * description:
    * change history:
    * date             defect             person             comments
    * ---------------------------------------------------------------------------------------------------------------------
    * 2021/2/22 16:59                      Decade                Create
    *
    * @author Nyx
    * @date 2021/2/22 16:59
    * @param null -
    * @return
    */
    void sxLotNpwUsageRecycleLimitUpdateReq(Infos.ObjCommon objCommon, LotNpwUsageRecycleLimitUpdateParams params);

    /*
    * description:
    * change history:
    * date             defect             person             comments
    * ---------------------------------------------------------------------------------------------------------------------
    * 2021/2/24 15:52                      Decade                Create
    *
    * @author Decade
    * @date 2021/2/24 15:52
    * @param null -
    * @return
    */
    void sxLotNpwUsageRecycleCountUpdateReq(Infos.ObjCommon objCommon, LotNpwUsageRecycleCountUpdateParams params);

    /*
    * description:
    * change history:
    * date             defect             person             comments
    * ---------------------------------------------------------------------------------------------------------------------
    * 2021/3/1 16:49                      Decade                Create
    *
    * @author Decade
    * @date 2021/3/1 16:49
    * @param null -
    * @return
    */
    void sxNPWLotAutoSkipCheckAndExcute(Infos.ObjCommon objCommon, ObjectIdentifier lotID);

    /**
     * 执行Lot Terminate逻辑
     * @version 0.1
     * @author Grant
     * @date 2021/7/7
     */
    void sxTerminateReq(Infos.ObjCommon objCommon, TerminateReq.TerminateReqParams params);

    /**
     * 执行Lot Terminate Cancel逻辑
     * @version 0.1
     * @author Grant
     * @date 2021/7/7
     */
    void sxTerminateCancelReq(Infos.ObjCommon objCommon, TerminateReq.TerminateCancelReqParams params);

    /**
     * description: lotMergeCheck 合并请求之前检查
     * change history:
     * date defect person comments
     * ‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐ ‐‐‐‐
     * 2021/7/13 10:22 Ly Create
     *
     * @author Ly
     * @date 2021/7/13 10:22
     * @param  ‐
     * @return
     */
    Boolean lotMergeCheck(Infos.ObjCommon objCommon,Params.MergeLotReqParams mergeLotReqParams,Boolean updateControlJobFlag);

    /**
     * description:lot merge
     * change history:
     * date defect person comments
     * ‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐ ‐‐‐‐
     * 2021/7/13 10:51 Ly Create
     *
     * @author Ly
     * @date 2021/7/13 10:51
     * @param  ‐
     * @return
     */
    void lotMerge(Infos.ObjCommon objCommon, Params.MergeLotReqParams mergeLotReqParams,ObjectIdentifier originalCarrierID);

    /**
    * description: 分批前进行检查
    * change history:
    * date defect person comments
    * ‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐ ‐‐‐‐
    * 2021/7/16 1:45 下午 ZH Create
    *
    * @author ZH
    * @date 2021/7/16 1:45 下午
    * @param  ‐
    * @return void
    */
    void lotSplitCheck(Infos.ObjCommon objCommon, Params.SplitLotReqParams splitLotReqParams);
}