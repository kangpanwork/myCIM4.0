package com.fa.cim.remote;

import com.fa.cim.middleware.standard.api.annotations.caller.Callback;
import com.fa.cim.middleware.standard.api.annotations.caller.CimRemoteManager;
import com.fa.cim.middleware.standard.api.annotations.caller.Dispatchable;
import com.fa.cim.middleware.standard.api.caller.RemoteManager;
import com.fa.cim.remote.remoteBack.CallRTMSBackWithResponse;
import com.fa.cim.rtms.*;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2021/7/22        ********              Decade               create file
 * * @author: Decade
 *
 * @date: 2021/7/22 14:20
 * @copyright: 2021, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@CimRemoteManager(value = "oms-rtms-service")
public interface IRTMSRemoteManager extends RemoteManager {

    //common interface
    @Callback(CallRTMSBackWithResponse.class)
    @Dispatchable("ReticleEquipmentInReq")
    Object reticleRTMSEqpInReq(@RequestBody ReticleRTMSEqpInReqParams params);

    @Callback(CallRTMSBackWithResponse.class)
    @Dispatchable("ReticleEquipmentOutReq")
    Object reticleRTMSEqpOutReq(@RequestBody ReticleRTMSEqpOutReqParams params);

    @Callback(CallRTMSBackWithResponse.class)
    @Dispatchable("ReticleStockerInReq")
    Object reticleRTMSStockerInReq(@RequestBody ReticleRTMSStockerInReqParams params);

    @Callback(CallRTMSBackWithResponse.class)
    @Dispatchable("ReticleStockerOutReq")
    Object requestRTMSStockerOut(@RequestBody ReticleRTMSStockerOutReqParams params);

    @Callback(CallRTMSBackWithResponse.class)
    @Dispatchable("ReticleJustInOutReq")
    Object requestRTMSJustInOut(@RequestBody ReticleRTMSJustInOutReqParams params);

    @Callback(CallRTMSBackWithResponse.class)
    @Dispatchable("ReticleInspectionRequestReq")
    Object requestRTMSInspectionRequest(@RequestBody ReticleRTMSInspectionRequestReqParams params);

    @Callback(CallRTMSBackWithResponse.class)
    @Dispatchable("ReticleInspectionInReq")
    Object requestRTMSInspectionIn(@RequestBody ReticleRTMSInspectionInReqParams params);

    @Callback(CallRTMSBackWithResponse.class)
    @Dispatchable("ReticleInspectionOutReq")
    Object requestRTMSInspectionOut(@RequestBody ReticleRTMSInspectionOutReqParams params);

    @Callback(CallRTMSBackWithResponse.class)
    @Dispatchable("MoveInAndReserveReticleCheckReq")
    Object requestRTMSMoveInAndReserveReq(@RequestBody ReticleRTMSMoveInAndReserveReqParams params);

    @Callback(CallRTMSBackWithResponse.class)
    @Dispatchable("MoveOutAndCancelReticleUpdateReq")
    Object requestRTMSMoveOutAndCancelReq(@RequestBody ReticleRTMSMoveOutAndCancelReqParams params);

}