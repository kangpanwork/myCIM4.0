package com.fa.cim.controller.interfaces.controlJobProcessJob;

import com.fa.cim.common.support.Response;
import com.fa.cim.dto.Params;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/7/31          ********            Nyx                create file
 *
 * @author: Nyx
 * @date: 2019/7/31 10:37
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IControlJobProcessJobInqController {
    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/3/26        ********             Jerry               create file
     *
     * @author: Jerry
     * @date: 2019/3/26 9:51
     * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response CJPJOnlineInfoInq(Params.CJPJOnlineInfoInqInParams params);

    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/3/27        ********             Jerry               create file
     *
     * @author: Jerry
     * @date: 2019/3/27 13:49
     * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response CJPJProgressInfoInq(Params.CJPJProgressInfoInqParams params);
}