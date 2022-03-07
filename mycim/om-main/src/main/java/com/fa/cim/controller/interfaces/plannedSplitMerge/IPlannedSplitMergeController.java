package com.fa.cim.controller.interfaces.plannedSplitMerge;

import com.fa.cim.common.support.Response;
import com.fa.cim.dto.Params;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/7/29          ********            Nyx                create file
 *
 * @author: Nyx
 * @date: 2019/7/29 17:15
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IPlannedSplitMergeController {
    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/3/21                              Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/3/21 下午 4:11
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response psmLotRemoveReq(Params.PSMLotRemoveReqParams params);

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/4/30                              Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/4/30 11:21
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response psmLotActionReq(Params.PSMLotActionReqParams params);

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/3/25                              Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/3/25 13:52
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response psmLotInfoSetReq(Params.PSMLotInfoSetReqParams params);
}