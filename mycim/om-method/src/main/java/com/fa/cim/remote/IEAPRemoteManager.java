package com.fa.cim.remote;

import com.fa.cim.dto.Infos;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Inputs;
import com.fa.cim.dto.Params;
import com.fa.cim.eqp.carrierout.CarrierOutReqParams;
import com.fa.cim.middleware.standard.api.annotations.caller.Callback;
import com.fa.cim.middleware.standard.api.annotations.caller.CimRemoteManager;
import com.fa.cim.middleware.standard.api.annotations.caller.Dispatchable;
import com.fa.cim.middleware.standard.api.caller.RemoteManager;
import com.fa.cim.remote.remoteBack.CallEAPBackWithResponse;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * description:
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/12/18                              Neyo                create file
 *
 * @author: Neyo
 * @date: 2020/12/18 19:15
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@CimRemoteManager(value = "oms-eap-service", singleton = false)
public interface IEAPRemoteManager extends RemoteManager {

    //common interface
    @Callback(CallEAPBackWithResponse.class)
    @Dispatchable("EqpModeChangeReq")
    Object sendEqpModeChangeReq(@RequestBody Params.EqpModeChangeReqPrams params);

    @Callback(CallEAPBackWithResponse.class)
    @Dispatchable("CJPJOnlineInfoInq")
    Object sendCJPJOnlineInfoInq(@RequestBody Params.CJPJOnlineInfoInqInParams params);

    @Callback(CallEAPBackWithResponse.class)
    @Dispatchable("PJStatusChangeReq")
    Object sendPJStatusChangeReq(@RequestBody Params.PJStatusChangeReqParams params);

    @Callback(CallEAPBackWithResponse.class)
    @Dispatchable("EqpEAPInfoInq")
    Object sendEqpEAPInfoInq(@RequestBody Inputs.SendEqpEAPInfoInqInput params);

    @Callback(CallEAPBackWithResponse.class)
    @Dispatchable("EAPRecoveryReq")
    Object sendEAPRecoveryReq(@RequestBody Inputs.SendEAPRecoveryReqInput params);

    @Callback(CallEAPBackWithResponse.class)
    @Dispatchable("CJStatusChangeReq")
    Object sendControlJobActionReq(@RequestBody Inputs.SendControlJobActionReqInput params);

    //fixed buffer interface
    @Callback(CallEAPBackWithResponse.class)
    @Dispatchable("MoveInReserveReq")
    Object sendMoveInReserveReq(@RequestBody Params.MoveInReserveReqParams params);

    @Callback(CallEAPBackWithResponse.class)
    @Dispatchable("MoveInReq")
    Object sendMoveInReq(@RequestBody Params.MoveInReqParams params);

    @Callback(CallEAPBackWithResponse.class)
    @Dispatchable("MoveOutReq")
    Object sendMoveOutReq(@RequestBody Params.OpeComWithDataReqParams params);

    @Callback(CallEAPBackWithResponse.class)
    @Dispatchable("MoveInReserveCancelReq")
    Object sendMoveInReserveCancelReq(@RequestBody Params.MoveInReserveCancelReqParams params);

    @Callback(CallEAPBackWithResponse.class)
    @Dispatchable("MoveInCancelReq")
    Object sendMoveInCancelReq(@RequestBody Params.MoveInCancelReqParams params);

    //internalBuffer interface
    @Callback(CallEAPBackWithResponse.class)
    @Dispatchable("MoveInReserveForIBReq")
    Object sendMoveInReserveForIBReq(@RequestBody Params.MoveInReserveForIBReqParams params);

    @Callback(CallEAPBackWithResponse.class)
    @Dispatchable("MoveInForIBReq")
    Object sendMoveInForIBReq(@RequestBody Params.MoveInForIBReqParams params);

    @Callback(CallEAPBackWithResponse.class)
    @Dispatchable("MoveOutForIBReq")
    Object sendMoveOutForIBReq(@RequestBody Params.MoveOutForIBReqParams params);

    @Callback(CallEAPBackWithResponse.class)
    @Dispatchable("MoveInReserveCancelForIBReq")
    Object sendMoveInReserveCancelForIBReq(@RequestBody Params.MoveInReserveCancelForIBReqParams params);

    @Callback(CallEAPBackWithResponse.class)
    @Dispatchable("MoveInCancelForIBReq")
    Object sendMoveInCancelForIBReq(@RequestBody Params.MoveInCancelForIBReqParams params);

    @Callback(CallEAPBackWithResponse.class)
    @Dispatchable("SortJobCancelNotificationReq")
    Object sendSortJobCancelNotificationReq(@RequestBody Inputs.SendSortJobCancelNotificationReqIn sendSortJobCancelNotificationReqIn);

    @Callback(CallEAPBackWithResponse.class)
    @Dispatchable("SortJobNotificationReq")
    Object sendSortJobNotificationReq(@RequestBody Inputs.SendSortJobNotificationReqIn sendSortJobNotificationReqIn);

    @Callback(CallEAPBackWithResponse.class)
    @Dispatchable("NPWCarrierReserveReq")
    Object sendNPWCarrierReserveReq(@RequestBody Params.NPWCarrierReserveReqParams npwCarrierReserveReqParams);

    @Callback(CallEAPBackWithResponse.class)
    @Dispatchable("WaferSortOnEqpCancelReq")
    Object sendWaferSortOnEqpCancelReq(@RequestBody Inputs.SendWaferSortOnEqpCancelReqIn sendWaferSortOnEqpCancelReqIn);

    @Callback(CallEAPBackWithResponse.class)
    @Dispatchable("NPWCarrierReserveCancelReq")
    Object sendNPWCarrierReserveCancelReq(@RequestBody Inputs.SendArrivalCarrierNotificationCancelReqIn sendArrivalCarrierNotificationCancelReqIn);

    @Callback(CallEAPBackWithResponse.class)
    @Dispatchable("SortActionStart")
    Object sendSortActionStartReq(@RequestBody com.fa.cim.sorter.Info.SortJobInfo jobInfoResult);

    @Callback(CallEAPBackWithResponse.class)
    @Dispatchable("SorterActionReserveReq")
    Object sendSorterActionReserveReq(@RequestBody com.fa.cim.sorter.Info.SortJobInfo jobInfoResult);

    @Callback(CallEAPBackWithResponse.class)
    @Dispatchable("SortActionCancelReq")
    Object sendSortActionCancelReq(@RequestBody com.fa.cim.sorter.Params.SJCancelRptParam sendSortActionCancelReqIn);

    @Callback(CallEAPBackWithResponse.class)
//    @Dispatchable("ReserveCancelUnloadingLotsForIBReq") rename to CancelCarrierOutPortReq to EAP
    @Dispatchable("CancelCarrierOutPortReq")
    Object sendReserveCancelUnloadingLotsForIBReq(@RequestBody Params.ReserveCancelUnloadingLotsForIBReqParams params);

    @Callback(CallEAPBackWithResponse.class)
    @Dispatchable("CarrierOutReq")
    Object sendCarrierOutReq(@RequestBody CarrierOutReqParams sendEapParam);

    @Callback(CallEAPBackWithResponse.class)
    @Dispatchable("NPWCarrierReserveCancelForIBReq")
    Object sendNPWCarrierReserveCancelForIBReq(@RequestBody Params.NPWCarrierReserveCancelForIBReqParams params);

    @Callback(CallEAPBackWithResponse.class)
    @Dispatchable("NPWCarrierReserveForIBReq")
    Object sendNPWCarrierReserveForIBReq(@RequestBody Params.NPWCarrierReserveForIBReqParams params);

    @Callback(CallEAPBackWithResponse.class)
    @Dispatchable("EDCDataItemWithTransitDataUIInq")
    Object sendEDCDataItemWithTransitDataInq(@RequestBody Infos.SendEDCDataItemWithTransitDataInqInParm sendEAPParam);
}
