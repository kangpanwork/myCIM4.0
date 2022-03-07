package com.fa.cim.tms.remote;

import com.fa.cim.middleware.standard.api.annotations.caller.Callback;
import com.fa.cim.middleware.standard.api.annotations.caller.CimRemoteManager;
import com.fa.cim.middleware.standard.api.annotations.caller.Dispatchable;
import com.fa.cim.middleware.standard.api.caller.RemoteManager;
import com.fa.cim.tms.dto.OMSParams;
import com.fa.cim.tms.dto.Response;
import com.fa.cim.tms.remote.remoteBack.CallOmsDefaultCheckBack;
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
@CimRemoteManager("tms-oms-service")
public interface IToOmsRemoteManager extends RemoteManager {

//    @Dispatchable("OTMSR001")
    @Dispatchable("CarrierTransferStatusChangeRpt")
    @Callback(CallOmsDefaultCheckBack.class)
    Response sendCarrierTransferStatusChangeRpt(@RequestBody OMSParams.CarrierTransferStatusChangeRptParams param);

//    @Dispatchable("OTMSR003")
    @Dispatchable("DurableXferStatusChangeRpt")
    @Callback(CallOmsDefaultCheckBack.class)
    Response sendDurableXferStatusChangeRpt(@RequestBody OMSParams.DurableTransferJobStatusRptParams param);

//    @Dispatchable("ODRBR008")
    @Dispatchable("ReticlePodTransferStatusChangeRpt")
    @Callback(CallOmsDefaultCheckBack.class)
    Response sendReticlePodTransferStatusChangeRpt(@RequestBody OMSParams.ReticlePodTransferStatusChangeRptParams param);

//    @Dispatchable("OTMSR005")
    @Dispatchable("StockerStatusChangeRpt")
    @Callback(CallOmsDefaultCheckBack.class)
    Response sendStockerStatusChangeRpt(@RequestBody OMSParams.StockerStatusChangeRptParams param);

//    @Dispatchable("OTMSQ005")
    @Dispatchable("WhereNextInterBay")
    @Callback(CallOmsDefaultCheckBack.class)
    Response sendWhereNextInterBay(@RequestBody OMSParams.WhereNextStockerInqParams param);

//    @Dispatchable("ODRBQ001")
    @Dispatchable("CassetteStatusInq")
    @Callback(CallOmsDefaultCheckBack.class)
    Response sendCassetteStatusInq(@RequestBody OMSParams.CarrierDetailInfoInqParams param);

//    @Dispatchable("OTMSQ001")
    @Dispatchable("StockerInfoInq")
    @Callback(CallOmsDefaultCheckBack.class)
    Response sendStockerInfoInq(@RequestBody OMSParams.StockerInfoInqInParams param);

//    @Dispatchable("OSYSR001")
    @Dispatchable("SystemMsgRpt")
    @Callback(CallOmsDefaultCheckBack.class)
    Response sendSystemMsgRpt(@RequestBody OMSParams.AlertMessageRptParams param);

//    @Dispatchable("OTMSQ002")
    @Dispatchable("StockerListInq")
    @Callback(CallOmsDefaultCheckBack.class)
    Response sendStockerListInq(@RequestBody OMSParams.StockerListInqInParams param);

//    @Dispatchable("OTMSQ006")
    @Dispatchable("StockerForAutoTransferInq")
    @Callback(CallOmsDefaultCheckBack.class)
    Response sendStockerForAutoTransferInq(@RequestBody OMSParams.StockerForAutoTransferInqParams param);

//    @Dispatchable("OTMSR004")
    @Dispatchable("StockerInventoryRpt")
    @Callback(CallOmsDefaultCheckBack.class)
    Response sendStockerInventoryRpt(@RequestBody OMSParams.StockerInventoryRptParam param);

//    @Dispatchable("OTMSR002")
    @Dispatchable("LotCassetteXferJobCompRpt")
    @Callback(CallOmsDefaultCheckBack.class)
    Response sendLotCassetteXferJobCompRpt(@RequestBody OMSParams.CarrierTransferJobEndRptParams param);

    @Callback(CallOmsDefaultCheckBack.class)
//    @Dispatchable("OACCQ001")
    @Dispatchable("LoginCheckInq")
    Response sendLoginCheckInq(@RequestBody OMSParams.LoginCheckInqParams params);

//    @Dispatchable("OEQPR014")
    @Dispatchable("EqpAlarmRpt")
    @Callback(CallOmsDefaultCheckBack.class)
    Response sendEqpAlarmRpt(@RequestBody OMSParams.EqpAlarmRptParams param);

//    @Dispatchable("OTMSW004")
    @Dispatchable("ReserveCancelReq")
    @Callback(CallOmsDefaultCheckBack.class)
    Response sendReserveCancelReq(@RequestBody OMSParams.LotCassetteReserveCancelParams param);

    //RTMS Interface
//    @Dispatchable("OARHR002")
    @Dispatchable("ReticlePodXferJobCompRpt")
    @Callback(CallOmsDefaultCheckBack.class)
    Response sendReticlePodXferJobCompRpt(@RequestBody OMSParams.ReticlePodXferJobCompRptParams params);

//    @Dispatchable("TXPDR023")
    @Dispatchable("RSPXferStatusChangeRpt")
    @Callback(CallOmsDefaultCheckBack.class)
    Response sendRSPXferStatusChangeRpt(@RequestBody OMSParams.RSPXferStatusChangeRptParams rspXferStatusChangeRptParams);

//    @Dispatchable("ODRBQ016")
    @Dispatchable("ReticlePodStockerInfoInq")
    @Callback(CallOmsDefaultCheckBack.class)
    Response sendReticlePodStockerInfoInq(@RequestBody OMSParams.ReticlePodStockerInfoInqParams reticlePodStockerInfoInqParams);

//    @Dispatchable("ODRBQ008")
    @Dispatchable("ReticlePodStatusInq")
    @Callback(CallOmsDefaultCheckBack.class)
    Response sendReticlePodStatusInq(@RequestBody OMSParams.ReticlePodStatusInqParams reticlePodStatusInqParams);

//    @Dispatchable("ODRBR016")
    @Dispatchable("ReticlePodInventoryRpt")
    @Callback(CallOmsDefaultCheckBack.class)
    Response sendReticlePodInventoryRpt(@RequestBody OMSParams.ReticlePodInventoryRptParams reticlePodInventoryRptParams);
}
