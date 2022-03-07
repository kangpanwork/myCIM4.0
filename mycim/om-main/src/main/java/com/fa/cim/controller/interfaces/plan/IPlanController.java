package com.fa.cim.controller.interfaces.plan;

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
 * @date: 2019/7/30 16:25
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IPlanController {
    /**
     * description: TxProdOrderChangeReq
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @author Ho
     * @date 2018/11/23 10:06:31
     */
    Response prodOrderChangeReq(Params.ProdOrderChangeReqParams prodOrderChangeReqParams);

    /**
     * description:
     * <p>LotExtPriorityModifyReqController ，confirmed this controller only will be invoked by UI, so do not need to support TCC <br/></p>
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/10/25        ********             PlayBoy               create file
     *
     * @author: PlayBoy
     * @date: 2018/10/25 13:14
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response lotExtPriorityModifyReq(Params.LotExtPriorityModifyReqParams params);

    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/11/28        ********             Bear               create file
     *
     * @author: Bear
     * @date: 2018/11/28 13:27
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response lotCurrentQueueReactivateReq(Params.LotCurrentQueueReactivateReqParams params);

    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/11/22        ********             Bear               create file
     *
     * @author: Bear
     * @date: 2018/11/22 14:20
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response lotPlanChangeReq(Params.LotScheduleChangeReqParams params);

    /**
     * description:
     * The method use to define the NewProdOrderCancelReqController.
     * transaction ID: OPLNW002
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/10/17        ********            Sun         create file
     *
     * @author: Sun
     * @date: 2018/10/17 13:42
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response newProdOrderCancelReq(Params.NewProdOrderCancelReqParams newProdOrderCancelReqParams);

    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/4/8        ********            Bear         create file
     *
     * @author: Bear
     * @date: 2018/4/8 15:51
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response newProdOrderCreateReq(Params.NewProdOrderCreateReqParams newProdOrderCreateReqParams);

    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/11/22        ********             Bear               create file
     *
     * @author: Bear
     * @date: 2018/11/22 15:54
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response newProdOrderModifyReq(Params.NewProdOrderModifyReqParams params);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2019/11/18 17:25
     * @param  -
     * @return com.fa.cim.common.support.Response
     */
    Response lotPlanChangeReserveCreateReq(Params.LotPlanChangeReserveCreateReqParams params);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2019/11/18 18:33
     * @param params -
     * @return com.fa.cim.common.support.Response
     */
    Response lotPlanChangeReserveModifyReq(Params.LotPlanChangeReserveModifyReqParams params);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author light
     * @date 2019/11/19 10:10
     * @param params -
     * @return com.fa.cim.common.support.Response
     */
    Response lotPlanChangeReserveCancelReq(Params.LotPlanChangeReserveCancelReqParams params);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Nyx
     * @date 2020/1/16 12:51
     * @param params -
     * @return com.fa.cim.common.support.Response
     */
    Response stepContentResetByLotReq(Params.StepContentResetByLotReqParams params);
}