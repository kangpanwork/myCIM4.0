package com.fa.cim.controller.interfaces.sort;

import com.fa.cim.common.support.Response;
import com.fa.cim.dto.Params;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/8/2          ********            Nyx                create file
 *
 * @author: Nyx
 * @date: 2019/8/2 11:25
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface ISortInqController {
    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/8/1                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/8/1 14:33
     * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response onlineSorterScrapWaferInq(Params.OnlineSorterScrapWaferInqParams params);

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/8/1                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/8/1 15:02
     * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */

    Response onlineSorterActionSelectionInq(Params.OnlineSorterActionSelectionInqParams params);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Jerry
     * @date 2019/8/2 14:15
     * @param params
     * @return com.fa.cim.common.support.Response
     */
    Response OnlineSorterActionStatusInq(Params.OnlineSorterActionStatusInqParm params);


    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/1/17 10:03                       Jerry               Create
     *
     * @author Jerry
     * @date 2020/1/17 10:03
     * @param params -
     * @return com.fa.cim.common.support.Response
     */
    Response SJListInq(Params.SJListInqParams params);


    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/1/17 10:16                       Jerry               Create
     *
     * @author Jerry
     * @date 2020/1/17 10:16
     * @param params -
     * @return com.fa.cim.common.support.Response
     */

    Response SJStatusInq(Params.SJStatusInqParams params);

    /**
     * description:
     * 2020/07/23   Auto lot start 需要call 的接口 临时自定义
     *
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/7/23 13:57                       Jerry               Create
     *
     * @author Jerry
     * @date 2020/7/23 13:57
     * @param  -
     * @return com.fa.cim.common.support.Response
     */

    Response sjInfoForAutoStartInq(com.fa.cim.fam.Params.SjInfoForAutoStartInqParams params);


    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2021/1/29 10:04                      decade              Create
     *
     * @author decade
     * @date 2021/1/29 10:04
     * @param params -
     * @return com.fa.cim.common.support.Response
     */

    Response reqCategoryGetByLot(Params.ReqCategoryGetByLotParams params);

}