package com.fa.cim.tms.event.recovery.remote;

import com.fa.cim.common.support.Response;
import com.fa.cim.middleware.standard.api.annotations.caller.Callback;
import com.fa.cim.middleware.standard.api.annotations.caller.CimRemoteManager;
import com.fa.cim.middleware.standard.api.annotations.caller.Dispatchable;
import com.fa.cim.middleware.standard.api.caller.RemoteManager;
import com.fa.cim.tms.event.recovery.dto.Params;
import com.fa.cim.tms.event.recovery.remote.remoteBack.CallTmsDefaultCheckBack;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * description:
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/6/5                              Miner                create file
 *
 * @author: Miner
 * @date: 2020/6/5 14:53
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@CimRemoteManager("tms-event-recovery-tms-service")
public interface IToTmsRemoteManager extends RemoteManager {


//    @Dispatchable("TM16")
    @Dispatchable("CarrierIDReadReportRetry")
    @Callback(CallTmsDefaultCheckBack.class)
    Response callTmsCarrierIDReadRptRetry(@RequestBody Params.CarrierIDReadReportParmas param);
}

