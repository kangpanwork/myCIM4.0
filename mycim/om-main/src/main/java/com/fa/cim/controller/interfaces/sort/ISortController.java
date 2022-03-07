package com.fa.cim.controller.interfaces.sort;

import com.fa.cim.common.support.Response;
import com.fa.cim.dto.Params;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/7/30          ********            Nyx                create file
 *
 * @author: Nyx
 * @date: 2019/7/30 15:31
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface ISortController {
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
    Response waferSlotmapChangeReq(Params.WaferSlotmapChangeReqParams param);

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

    Response onlineSorterActionExecuteReq(Params.OnlineSorterActionExecuteReqParams params);

    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/7/31       ********              Jerry             create file
     *
     * @author: Jerry
     * @date: 2019/7/31 16:28
     * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response onlineSorterRpt(Params.OnlineSorterRptParams params);


    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/8/1                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/8/1 9:41
     * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response onlineSorterSlotmapCompareReq(Params.OnlineSorterSlotmapCompareReqParams params);

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/8/1                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/8/1 16:43
     * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response waferSorterActionRegisterReq(Params.WaferSorterActionRegisterReqParams params);

    /**
     * description: This function adjusts inconsistency of SlotMap in Carrier between MMServer and Wafer Sorter Equipment.
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/8/2                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/8/2 15:55
     * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response onlineSorterSlotmapAdjustReq(Params.OnlineSorterSlotmapAdjustReqParam params);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Jerry
     * @date 2019/8/5 10:12
     * @param params
     * @return com.fa.cim.common.support.Response
     */
    Response onlineSorterActionCancelReq(Params.OnlineSorterActionCancelReqParm params);

    /**
     * description:
     * <p>CarrierExchangeReqController .<br/></p>
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/9/27        ********             PlayBoy               create file
     *
     * @author: PlayBoy
     * @date: 2018/9/27 11:18
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response carrierExchangeReq(Params.CarrierExchangeReqParams params);


    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/1/13 13:31                       Jerry               Create
     *
     * @author Jerry
     * @date 2020/1/13 13:31
     * @param params -
     * @return com.fa.cim.common.support.Response
     */

    Response sjCreateReq(Params.SJCreateReqParams params);

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

    Response sjStartReq(Params.SJStartReqParams params);


    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/6/5 10:44                       Jerry               Create
     *
     * @author Jerry
     * @date 2020/6/5 10:44
     * @param params -
     * @return com.fa.cim.common.support.Response
     */

    Response sjConditionCheckReq(Params.SortJobCheckConditionReqInParam params);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/6/12 14:39                       Jerry               Create
     *
     * @author Jerry
     * @date 2020/6/12 14:39
     * @param params -
     * @return com.fa.cim.common.support.Response
     */

    Response sjCancelReq(Params.SJCancelReqParm params);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/6/23 15:54                       Jerry               Create
     *
     * @author Jerry
     * @date 2020/6/23 15:54
     * @param params -
     * @return com.fa.cim.common.support.Response
     */

    Response sjPriorityChangeReq(Params.SortJobPriorityChangeReqParam params);

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

}