package com.fa.cim.remote;

import com.fa.cim.middleware.standard.api.annotations.caller.Callback;
import com.fa.cim.middleware.standard.api.annotations.caller.CimRemoteManager;
import com.fa.cim.middleware.standard.api.caller.RemoteManager;
import com.fa.cim.remote.remoteBack.CallTcsBackWithOutResponse;
import com.fa.cim.remote.remoteBack.CallTcsBackWithResponse;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * description:
 *
 * BUG-1065: oms-tcs-service change to eap-service
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/5/28                              Neyo                create file
 *
 * @author: Neyo
 * @date: 2020/5/28 16:06
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@CimRemoteManager("eap-service")
public interface ITCSRemoteManager extends RemoteManager {

    @Callback(CallTcsBackWithOutResponse.class)
    String sendEqpModeChangeReq(@RequestBody Map requestParam);

    @Callback(CallTcsBackWithOutResponse.class)
    String sendRecipeParamAdjustReq(@RequestBody Map requestParam);

    @Callback(CallTcsBackWithOutResponse.class)
    String sendControlJobActionReq(@RequestBody Map requestParam);

    @Callback(CallTcsBackWithOutResponse.class)
    String sendRecipeDownloadReq(@RequestBody Map requestParam);

    @Callback(CallTcsBackWithOutResponse.class)
    String sendRecipeCompareReq(@RequestBody Map requestParam);

    @Callback(CallTcsBackWithOutResponse.class)
    String sendRecipeDeleteInFileReq(@RequestBody Map requestParam);

    @Callback(CallTcsBackWithOutResponse.class)
    String sendNPWCarrierReserveReq(@RequestBody Map requestParam);

    @Callback(CallTcsBackWithOutResponse.class)
    String sendPJStatusChangeReq(@RequestBody Map requestParam);

    @Callback(CallTcsBackWithOutResponse.class)
    String sendNPWCarrierReserveForIBReq(@RequestBody Map requestParam);

    @Callback(CallTcsBackWithOutResponse.class)
    String sendRecipeUploadReq(@RequestBody Map requestParam);

    @Callback(CallTcsBackWithOutResponse.class)
    String sendRecipeDeleteReq(@RequestBody Map requestParam);

    @Callback(CallTcsBackWithOutResponse.class)
    String sendCarrierTransferJobEndRpt(@RequestBody Map requestParam);

    @Callback(CallTcsBackWithOutResponse.class)
    String sendArrivalCarrierNotificationCancelForInternalBufferReq(@RequestBody Map requestParam);

    @Callback(CallTcsBackWithOutResponse.class)
    String sendArrivalCarrierNotificationCancelReq(@RequestBody Map requestParam);

    @Callback(CallTcsBackWithOutResponse.class)
    String sendWaferSortOnEqpReq(@RequestBody Map requestParam);

    @Callback(CallTcsBackWithOutResponse.class)
    String sendWaferSortOnEqpCancelReq(@RequestBody Map requestParam);

    @Callback(CallTcsBackWithOutResponse.class)
    String sendSLMWaferRetrieveCassetteReserve(@RequestBody Map requestParam);

    @Callback(CallTcsBackWithOutResponse.class)
    String sendDurableControlJobActionReq(@RequestBody Map requestParam);

    @Callback(CallTcsBackWithOutResponse.class)
    String sendReserveCancelUnloadingLotsForIBReq(@RequestBody Map requestParam);

    @Callback(CallTcsBackWithOutResponse.class)
    String sendSLMCassetteUnclampReq(@RequestBody Map requestParam);

    /**************************************************************************/

    @Callback(CallTcsBackWithResponse.class)
    String sendMoveInReserveReq(@RequestBody Map requestParam);

    @Callback(CallTcsBackWithResponse.class)
    String sendCJPJOnlineInfoInq(@RequestBody Map requestParam);

    @Callback(CallTcsBackWithResponse.class)
    String sendMoveInReserveForIBReq(@RequestBody Map requestParam);

    @Callback(CallTcsBackWithResponse.class)
    String sendCJPJProgressInfoInq(@RequestBody Map requestParam);

    @Callback(CallTcsBackWithResponse.class)
    String sendEqpEAPInfoInq(@RequestBody Map requestParam);

    @Callback(CallTcsBackWithResponse.class)
    String sendEAPRecoveryReq(@RequestBody Map requestParam);

    @Callback(CallTcsBackWithResponse.class)
    String sendRecipeDirectoryInq(@RequestBody Map requestParam);

    @Callback(CallTcsBackWithResponse.class)
    String sendMoveInReserveCancelReq(@RequestBody Map requestParam);

    @Callback(CallTcsBackWithResponse.class)
    String sendMoveInReserveCancelForIBReq(@RequestBody Map requestParam);

    @Callback(CallTcsBackWithResponse.class)
    String sendRecipeParamAdjustOnActivePJReq(@RequestBody Map requestParam);

    @Callback(CallTcsBackWithResponse.class)
    String sendEDCDataItemWithTransitDataInq(@RequestBody Map requestParam);

    @Callback(CallTcsBackWithResponse.class)
    String sendMoveOutForIBReq(@RequestBody Map requestParam);

    @Callback(CallTcsBackWithResponse.class)
    String sendMoveOutReq(@RequestBody Map requestParam);

    @Callback(CallTcsBackWithResponse.class)
    String sendMoveInCancelForIBReq(@RequestBody Map requestParam);

    @Callback(CallTcsBackWithResponse.class)
    String sendMoveInCancelReq(@RequestBody Map requestParam);

    @Callback(CallTcsBackWithResponse.class)
    String sendMoveInForIBReq(@RequestBody Map requestParam);

    @Callback(CallTcsBackWithResponse.class)
    String sendMoveInReq(@RequestBody Map RequestParam);

    @Callback(CallTcsBackWithResponse.class)
    String sendPartialMoveOutReq(@RequestBody Map requestParam);

    @Callback(CallTcsBackWithResponse.class)
    String doSpcCheck(@RequestBody Map requestParam);

    @Callback(CallTcsBackWithResponse.class)
    String sendPartialMoveOutForIBReq(@RequestBody Map requestParam);

    @Callback(CallTcsBackWithResponse.class)
    String sortJobCreateReq(@RequestBody Map requestParam);

    @Callback(CallTcsBackWithResponse.class)
    String sortJobCancelReq(@RequestBody Map requestParam);

    @Callback(CallTcsBackWithResponse.class)
    String durableOperationStartReq(@RequestBody Map requestParam);

    @Callback(CallTcsBackWithOutResponse.class)
    String sendReticleRetrieveReq(@RequestBody Map requestParam);

    @Callback(CallTcsBackWithOutResponse.class)
    String sendReticleStoreCancelReq(@RequestBody Map requestParam);

    @Callback(CallTcsBackWithOutResponse.class)
    String sendReticleRetrieveCancelReq(@RequestBody Map requestParam);

    @Callback(CallTcsBackWithOutResponse.class)
    String sendReticlePodUnclampCancelReq(@RequestBody Map setRemoteManagerRequestParam);

    @Callback(CallTcsBackWithOutResponse.class)
    String sendReticleStoreReq(@RequestBody Map requestParam);

    @Callback(CallTcsBackWithOutResponse.class)
    String sendReticlePodUnclampReq(@RequestBody Map requestParam);

    @Callback(CallTcsBackWithOutResponse.class)
    String sendBareReticleStockerOnlineModeChangeReq(@RequestBody Map requestParam);

    @Callback(CallTcsBackWithOutResponse.class)
    String sendReticleInventoryReq(@RequestBody Map requestParam);
}
