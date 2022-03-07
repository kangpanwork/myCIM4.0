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
 * 2019/7/30          ********            Nyx                create file
 *
 * @author: Nyx
 * @date: 2019/7/30 10:45
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IDispatchInqController {
    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/8/15        ********             Jerry               create file
     *
     * @author: Jerry
     * @date: 2018/8/15 15:41
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response lotsMoveInReserveInfoInq(Params.LotsMoveInReserveInfoInqParams lotsMoveInReserveInfoInqParams);

    /**
     * description: WhatNextNPWStandbyLotInqController
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 10/29/2018        ********             Sun               create file
     *
     * @author: Sun
     * @date: 10/29/2018 10:08 AM
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response whatNextNPWStandbyLotInq(Params.WhatNextNPWStandbyLotInqParams params);

    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/7/11        ********             panda               create file
     *
     * @author: panda
     * @date: 2018/7/11 14:15
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response whatNextInq(Params.WhatNextLotListParams whatNextLotListParams);

    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/12/17       ********              Nyx             create file
     *
     * @author: Nyx
     * @date: 2018/12/17 10:38
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response lotsMoveInReserveInfoForIBInq(Params.LotsMoveInReserveInfoForIBInqParams params);

    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/7/24       ********              lightyh             create file
     *
     * @author: lightyh
     * @date: 2019/7/24 16:28
     * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response AutoDispatchConfigInq(Params.AutoDispatchConfigInqParams autoDispatchConfigInqParams);

    /**
     * description:
     * <p>This function returns Information of Virtual Operation Lots.
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/3/8        ********             Bear               create file
     *
     * @author: Bear
     * @date: 2019/3/8 10:46
     * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response virtualOperationWipListInq(Params.VirtualOperationWipListInqParams params);

    /**
     * description:
     * <p>eqpFullAutoConfigListInq</p>
     * change history:
     * date   defect   person   comments
     * ------------------------------------------------------------------------------------------------------------------
     *
     * @return Response
     * @author Decade
     * @date 2019/11/25/025 14:57
     */
    Response eqpFullAutoConfigListInq(@RequestBody Params.EqpFullAutoConfigListInqInParm eqpFullAutoConfigListInqInParm);
}