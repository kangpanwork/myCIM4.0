package com.fa.cim.service.newSorter;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.dto.Infos;
import com.fa.cim.sorter.Info;
import com.fa.cim.sorter.Params;

import java.util.List;

/**
 * description:
 *
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2021/06/22       开发NewSorter        Bear               create file
 *
 * @author Jerry
 * @since 2021/06/22 04:22
 * Copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface ISortNewService {

    /**
     * description:
     *
     * 重构Sorter Job create 接口
     *
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2021/6/30 11:28 上午                 Jerry              Create
     * @author Jerry
     * @date 2021/6/30 11:28 上午
     * @param objCommon
     * @param params
     * @return java.lang.String
     */
    String sxSJCreateReq(Infos.ObjCommon objCommon, Params.SJCreateReqParams params) ;

    /**
     * description: 对设备的sort action 进行增删改
     * change history:
     * date defect person comments
     * ‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐ ‐‐‐‐
     * 2021/6/22 8:13 下午 ZH Create
     *
     * @author ZH
     * @date 2021/6/22 8:13 下午
     * @param  ‐
     * @return void
     */
    void sxWaferSorterActionRegisterReq(Infos.ObjCommon objCommon, Params.WaferSorterActionRegisterReqParams params);

    /**
     * description:  修改sortJob/componentJob的优先级
     * change history:
     * date defect person comments
     * ‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐ ‐‐‐‐
     * 2021/6/22 9:20 下午 ZH Create
     *
     * @author ZH
     * @date 2021/6/22 9:20 下午
     * @param  ‐
     * @return void
     */
    void sxSortJobPriorityChangeReq(Infos.ObjCommon objCommon, Params.SortJobPriorityChangeReqParam param);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2021/6/30 12:45 下午                 Jerry              Create
     * @author Jerry
     * @date 2021/6/30 12:45 下午
     * @param objCommon
     * @param params
     * @return void
     */
    void sxSorterActionReq(Infos.ObjCommon objCommon, Info.SortJobInfo params) ;

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2021/6/30 12:46 下午                 Jerry              Create
     * @author Jerry
     * @date 2021/6/30 12:46 下午
     * @param objCommon
     * @param params
     * @return void
     */
    List<ObjectIdentifier> sxSorterActionRpt(Infos.ObjCommon objCommon, Info.SortJobInfo params);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2021/6/30 12:46 下午                 Jerry              Create
     * @author Jerry
     * @date 2021/6/30 12:46 下午
     * @param objCommon
     * @param params
     * @return void
     */
    void sxOnlineSorterSlotmapAdjustReq(Infos.ObjCommon objCommon, Params.OnlineSorterSlotmapAdjustReqParam params);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2021/6/30 12:46 下午                       Jerry              Create
     * @author Jerry
     * @date 2021/6/30 12:46 下午
     * @param objCommon
     * @param params
     * @return void
     */
    void sxSJConditionCheckReq(Infos.ObjCommon objCommon, Params.SortJobCheckConditionReqInParam params);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2021/6/30 12:47 下午                 Jerry              Create
     * @author   Jerry
     * @date 2021/6/30 12:47 下午
     * @param objCommon
     * @param params
     * @return void
     */
    void sxWaferSlotmapChangeReq(Infos.ObjCommon objCommon, Info.SortJobInfo params, boolean notifyToTCS);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2021/6/30 12:47 下午                 Jerry              Create
     * @author Jerry
     * @date 2021/6/30 12:47 下午
     * @param objCommon
     * @return void
     */
    void sxCarrierExchangeReq(Infos.ObjCommon objCommon, Info.SortJobInfo sortJobInfo);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2021/6/30 12:47 下午                 Jerry              Create
     * @author Jerry
     * @date 2021/6/30 12:47 下午
     * @param objCommon
     * @param params
     * @return void
     */
    void sxWaferSlotmapChangeRpt(Infos.ObjCommon objCommon, Info.SortJobInfo params);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2021/6/30 1:41 下午                 Jerry              Create
     * @author Jerry
     * @date 2021/6/30 1:41 下午
     * @param objCommon
     * @param sortJobInfo
     * @return java.util.List<com.fa.cim.sorter.Info.WaferSorterCompareCassette>
     */
    Info.WaferSorterCompareCassette sxOnlineSorterSlotmapCompareReq(Infos.ObjCommon objCommon, Params.SJListInqParams sortJobInfo) ;


    /**
     * description: 通知EAP可以做Start
     * change history:
     *  date      time           person          comments
     * ‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐
     * 2021/6/7    19:32          Ly              Create
     *
     * @author Ly
     * @date 2021/6/7 19:32
     * @param
     * @return
     */
    void sortActionStart(Infos.ObjCommon objCommon, Params.SJCreateReqParams inqParams);



    /**
     * description: 取消sortJob
     * change history:
     * date defect person comments
     * ‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐ ‐‐‐‐
     * 2021/6/22 9:34 下午 ZH Create
     *
     * @author ZH
     * @date 2021/6/22 9:34 下午
     * @param  ‐
     * @return void
     */
//    void sxSJCancelReq(Infos.ObjCommon objCommon, Params.SJCancelReqParm params);

    /**
     * description: 修改sortJob的状态
     * change history:
     * date defect person comments
     * ‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐ ‐‐‐‐
     * 2021/6/29 10:26 上午 ZH Create
     *
     * @author ZH
     * @date 2021/6/29 10:26 上午
     * @param  ‐
     * @return void
     */
    void sxSJStatusChgRpt(Infos.ObjCommon objCommon, Params.SJStatusChgRptParams params, boolean resEAPFlag);

    /**
     * description: 提供EAP调用，可以cancel sortJob 和 componentJob
     * change history:
     * date defect person comments
     * ‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐ ‐‐‐‐
     * 2021/6/15 11:28 上午 ZH Create
     *
     * @author ZH
     * @date 2021/6/15 11:28 上午
     * @param  ‐
     * @return void
     */
//    void sxSJCancelRpt(Infos.ObjCommon objCommon, Params.SJCancelRptParm params);

    /**
    * description: MoveInReserve的时候创建SorterJob
    * change history:
    * date defect person comments
    * ‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐ ‐‐‐‐
    * 2021/7/13 3:57 下午 ZH Create
    *
    * @author ZH
    * @date 2021/7/13 3:57 下午
    * @param  ‐
    * @return java.lang.String
    */
    String sxSJCreateByMoveInReserve(Infos.ObjCommon objCommon, com.fa.cim.dto.Params.MoveInReserveReqParams moveInReserveReqParams, ObjectIdentifier controlJobID);



    /**
    * description: EAP报告完成后做后置处理
    * change history:
    * date defect person comments
    * ‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐ ‐‐‐‐
    * 2021/7/15 19:48 Ly Create
    *
    * @author Ly
    * @date 2021/7/15 19:48
    * @param  ‐
    * @return
    */
    void sxPostAct(Infos.ObjCommon objCommon, Info.PostActDRIn postActDRIn);

    /**
     * description: 取消sortJob
     * change history:
     * date defect person comments
     * ‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐ ‐‐‐‐
     * 2021/6/22 9:34 下午 ZH Create
     *
     * @author ZH
     * @date 2021/6/22 9:34 下午
     * @param  ‐
     * @return void
     */
    void sxSJCancelReq(Infos.ObjCommon objCommon, com.fa.cim.sorter.Params.SJCancelReqParm params, boolean isDelete);
}
