package com.fa.cim.tms.event.recovery.remote;

import com.fa.cim.common.support.Response;
import com.fa.cim.middleware.standard.api.annotations.caller.Callback;
import com.fa.cim.middleware.standard.api.annotations.caller.CimRemoteManager;
import com.fa.cim.middleware.standard.api.annotations.caller.Dispatchable;
import com.fa.cim.middleware.standard.api.caller.RemoteManager;
import com.fa.cim.tms.event.recovery.dto.OMSParams;
import com.fa.cim.tms.event.recovery.remote.remoteBack.CallOmsDefaultCheckBack;
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
@CimRemoteManager("tms-event-recovery-oms-service")
public interface IToOmsRemoteManager extends RemoteManager {

//    @Dispatchable("OTMSR001")
    @Dispatchable("CarrierTransferStatusChangeRpt")
    @Callback(CallOmsDefaultCheckBack.class)
    Response sendCarrierTransferStatusChangeRpt(@RequestBody OMSParams.CarrierTransferStatusChangeRptParams param);

//    @Dispatchable("OTMSR003")
    @Dispatchable("DurableXferStatusChangeRpt")
    @Callback(CallOmsDefaultCheckBack.class)
    Response sendDurableXferStatusChangeRpt(@RequestBody OMSParams.DurableTransferJobStatusRptParams param);

//    @Dispatchable("OTMSR002")
    @Dispatchable("LotCassetteXferJobCompRpt")
    @Callback(CallOmsDefaultCheckBack.class)
    Response sendLotCassetteXferJobCompRpt(@RequestBody OMSParams.CarrierTransferJobEndRptParams param);

//    @Dispatchable("OTMSW004")
    @Dispatchable("ReserveCancelReq")
    @Callback(CallOmsDefaultCheckBack.class)
    Response sendReserveCancelReq(@RequestBody OMSParams.LotCassetteReserveCancelParams param);

//    @Dispatchable("TXPDR023")//OARHR003
    @Dispatchable("RSPXferStatusChangeRpt")
    @Callback(CallOmsDefaultCheckBack.class)
    Response sendRSPXferStatusChangeRpt(@RequestBody OMSParams.RSPXferStatusChangeRptParams param);

//    @Dispatchable("TXPDR022")//OARHR002
    @Dispatchable("ReticlePodXferJobCompRpt")
    @Callback(CallOmsDefaultCheckBack.class)
    Response sendReticlePodXferJobCompRpt(OMSParams.ReticlePodXferJobCompRptParams param);
}
