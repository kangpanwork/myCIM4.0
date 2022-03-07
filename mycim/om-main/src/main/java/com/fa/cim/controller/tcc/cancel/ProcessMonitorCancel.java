package com.fa.cim.controller.tcc.cancel;

import com.fa.cim.common.support.Response;
import com.fa.cim.controller.interfaces.processMonitor.IProcessMonitorController;
import com.fa.cim.dto.Params;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/7/30          ********            Nyx                create file
 *
 * @author: Nyx
 * @date: 2019/7/30 16:44
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Service("ProcessMonitorCancel")
@Transactional(rollbackFor = Exception.class)
public class ProcessMonitorCancel implements IProcessMonitorController {
    @Override
    public Response monitorBatchDeleteReq(Params.MonitorBatchDeleteReqParams monitorBatchDeleteReqParams) {
        return null;
    }

    @Override
    public Response monitorBatchCreateReq(Params.MonitorBatchCreateReqParams monitorBatchCreateReqParams) {
        return null;
    }

    @Override
    public Response autoCreateMonitorForInProcessLotReq(Params.AutoCreateMonitorForInProcessLotReqParams autoCreateMonitorForInProcessLotReqParams) {
        return null;
    }
}