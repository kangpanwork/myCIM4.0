package com.fa.cim.controller.interfaces.lotStart;

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
 * @date: 2019/7/30 13:45
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface ILotStartController {
    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/10/30       ********              Nyx             create file
     *
     * @author: Nyx
     * @date: 2018/10/30 10:24
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response npwLotStartReq(Params.NPWLotStartReqParams params);

    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/10/19         ********              Nyx             create file
     *
     * @author: Nyx
     * @date: 2018/10/19 10:58
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response waferAliasSetReq(Params.WaferAliasSetReqParams params);

    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/6/8        ********             Bear               create file
     *
     * @author: Bear
     * @date: 2018/6/8 14:15
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response waferLotStartCancelReq(Params.WaferLotStartCancelReqParams waferLotStartCancelReqParams);

    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/5/7        ********             Bear               create file
     *
     * @author: Bear
     * @date: 2018/5/7 9:45
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response waferLotStartReq(Params.WaferLotStartReqParams waferLotStartReqParams);
}