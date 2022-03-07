package com.fa.cim.controller.interfaces.processMonitor;

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
 * @date: 2019/7/30 16:41
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IProcessMonitorController {
    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/10/30        ********             Jerry               create file
     *
     * @author: Jerry
     * @date: 2018/10/30 15:53
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response monitorBatchDeleteReq(Params.MonitorBatchDeleteReqParams monitorBatchDeleteReqParams);

    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/10/30        ********             Jerry               create file
     *
     * @author: Jerry
     * @date: 2018/10/30 10:12
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response monitorBatchCreateReq(Params.MonitorBatchCreateReqParams monitorBatchCreateReqParams);

    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/10/26        ********             Jerry               create file
     *
     * @author: Jerry
     * @date: 2018/10/26 9:31
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    Response autoCreateMonitorForInProcessLotReq(Params.AutoCreateMonitorForInProcessLotReqParams autoCreateMonitorForInProcessLotReqParams);
}