package com.fa.cim.controller.interfaces.dispatch;

import com.fa.cim.common.support.Response;
import com.fa.cim.dto.Params;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/7/29          ********            Nyx                create file
 *
 * @author: Nyx
 * @date: 2019/7/29 11:33
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IDispatchController {
    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/9/25        ********             Bear               create file
     *
     * @author: Bear
     * @date: 2018/9/25 15:57
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response moveInReserveCancelReq(Params.MoveInReserveCancelReqParams params);

    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/7/4        ********             Bear               create file
     *
     * @author: Bear
     * @date: 2018/7/4 11:06
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response moveInReserveReq(Params.MoveInReserveReqParams moveInReserveReqParams);

    /**
     * description:
     * <p>MoveInReserveCancelForIBReqController .<br/></p>
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/1/2        ********             Scott               create file
     *
     * @author: Scott
     * @date: 2019/1/2 10:08
     * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response moveInReserveCancelForIBReq(Params.MoveInReserveCancelForIBReqParams params);

    /**
     * description:
     * <p>This function supports to reserve in advance the Lot for processing to Equipment that Category ID defined in SM is Internal Buffer.</p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/1/2                            Wind                create file
     *
     * @author: Wind
     * @date: 2019/1/2 09:56
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response moveInReserveForIBReq(Params.MoveInReserveForIBReqParams params);

    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/7/24       ********              lightyh             create file
     *
     * @author: lightyh
     * @date: 2019/7/24 17:54
     * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response autoDispatchConfigModifyReq(Params.AutoDispatchConfigModifyReqParams autoDispatchConfigModifyReqParams);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/2/4 15:39                       Jerry               Create
     *
     * @author Jerry
     * @date 2020/2/4 15:39
     * @param carrierDispatchAttrChgReqParm -
     * @return com.fa.cim.common.support.Response
     */

    Response carrierDispatchAttrChgReq(Params.CarrierDispatchAttrChgReqParm carrierDispatchAttrChgReqParm);

    Response eqpFullAutoConfigChgReq(@RequestBody Params.EqpFullAutoConfigChgReqInParm params);

    /**
     * description: Qiandao project
     *              EAP-OMS eqpAutoMoveInReserveReq in fixedBuffer case to reserve a single FOUP automation
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2021/2/26                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2021/2/26 11:20
     * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response eqpAutoMoveInReserveReq(Params.EqpAutoMoveInReserveReqParams params);
}