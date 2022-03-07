package com.fa.cim.tms.status.recovery.remote;

import com.fa.cim.common.support.Response;
import com.fa.cim.middleware.standard.api.annotations.caller.Callback;
import com.fa.cim.middleware.standard.api.annotations.caller.CimRemoteManager;
import com.fa.cim.middleware.standard.api.annotations.caller.Dispatchable;
import com.fa.cim.middleware.standard.api.caller.RemoteManager;
import com.fa.cim.tms.status.recovery.dto.OMSParams;
import com.fa.cim.tms.status.recovery.remote.remoteBack.CallOmsDefaultCheckBack;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * description:
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/6/4                              Neyo                create file
 *
 * @author: Neyo
 * @date: 2020/6/4 15:52
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@CimRemoteManager("tms-staust-recovery-oms-service")
public interface IToOmsRemoteManager extends RemoteManager {


//    @Dispatchable("OTMSR005")
    @Dispatchable("StockerStatusChangeRpt")
    @Callback(CallOmsDefaultCheckBack.class)
    Response sendStockerStatusChangeRpt(@RequestBody OMSParams.StockerStatusChangeRptParams param);

}
