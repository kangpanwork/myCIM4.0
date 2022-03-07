package com.fa.cim.controller.interfaces.newSorter;

import com.fa.cim.common.support.Response;
import com.fa.cim.sorter.Info;
import com.fa.cim.sorter.Params;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/7/30          ********            Nyx                create file
 *
 * @author: Jerry
 * @date: 2021/6/22 15:34
 * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface ISortNewController {

    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/7/29       ********              Jerry             create file
     *
     * @author: Jerry
     * @date: 2019/7/29 10:18
     * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response sorterActionReq(Info.SortJobInfo params);


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
    Response sortActionStart(Params.SJCreateReqParams inqParams);



    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2021/6/30 12:46 下午                 Jerry              Create
     * @author Jerry
     * @date 2021/6/30 12:46 下午
     * @param params
     * @return void
     */
    Response sorterActionRpt(Info.SortJobInfo params);


    /**
     * description: 对设备的action code增删改
     * change history:
     * date defect person comments
     * ‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐ ‐‐‐‐
     * 2021/6/22 7:48 下午 ZH Create
     *
     * @author ZH
     * @date 2021/6/22 7:48 下午
     * @param  ‐
     * @return com.fa.cim.common.support.Response
     */
    Response waferSorterActionRegisterReq(Params.WaferSorterActionRegisterReqParams params);

    /**
     * description: 修改sortJob/componentJob的优先级
     * change history:
     * date defect person comments
     * ‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐ ‐‐‐‐
     * 2021/6/22 9:19 下午 ZH Create
     *
     * @author ZH
     * @date 2021/6/22 9:19 下午
     * @param  ‐
     * @return com.fa.cim.common.support.Response
     */
    Response sjPriorityChangeReq(Params.SortJobPriorityChangeReqParam params);




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
     * @return com.fa.cim.common.support.Response
     */
    Response sjCancelReq(Params.SJCancelReqParm params);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/6/30 10:04                       Jerry               Create
     *
     * @author Jerry
     * @date 2020/6/30 10:04
     * @param params -
     * @return com.fa.cim.common.support.Response
     */
    Response sjStatusChgRpt(Params.SJStatusChgRptParams params);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/2/3 16:20                       Jerry               Create
     *
     * @author Jerry
     * @date 2020/2/3 16:20
     * @param params -
     * @return com.fa.cim.common.support.Response
     */
    Response sjCreateReq(Params.SJCreateReqParams params);

    /**
    * description: 进行slotMap比较
    * change history:
    * date defect person comments
    * ‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐ ‐‐‐‐
    * 2021/7/6 8:30 下午 ZH Create
    *
    * @author ZH
    * @date 2021/7/6 8:30 下午
    * @param  ‐
    * @return com.fa.cim.common.support.Response
    */
    Response onlineSorterSlotmapCompareReq(Params.SJListInqParams params);

    /**
    * description: 调整sorter slotMap
    * change history:
    * date defect person comments
    * ‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐ ‐‐‐‐
    * 2021/7/7 2:55 下午 ZH Create
    *
    * @author ZH
    * @date 2021/7/7 2:55 下午
    * @param  ‐
    * @return com.fa.cim.common.support.Response
    */
    Response onlineSorterSlotmapAdjustReq(Params.OnlineSorterSlotmapAdjustReqParam params);

    /**
    * description: 创建sortJob钱进行检查
    * change history:
    * date defect person comments
    * ‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐ ‐‐‐‐
    * 2021/7/14 2:37 下午 ZH Create
    *
    * @author ZH
    * @date 2021/7/14 2:37 下午
    * @param  ‐
    * @return com.fa.cim.common.support.Response
    */
    Response sjConditionCheckReq(Params.SortJobCheckConditionReqInParam params);


    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/3/15        ********             Jerry               create file
     *
     * @author: Jerry
     * @date: 2019/3/15 11:00
     * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response waferSlotmapChangeReq(Params.WaferSlotMapChange params);

    /**
    * description: 进行MoveOut的时候，将wafer放入到其他carrier中去
    * change history:
    * date defect person comments
    * ‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐ ‐‐‐‐
    * 2021/7/22 1:50 下午 ZH Create
    *
    * @author ZH
    * @date 2021/7/22 1:50 下午
    * @param  ‐
    * @return com.fa.cim.common.support.Response
    */
    Response carrierExchangeReq(Params.CarrierExchangeParams params);
}