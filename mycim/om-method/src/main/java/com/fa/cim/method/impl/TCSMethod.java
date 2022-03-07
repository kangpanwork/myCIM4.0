package com.fa.cim.method.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fa.cim.annotaion.OmMethod;
import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.constant.TCSReqEnum;
import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.OmCode;
import com.fa.cim.common.support.User;
import com.fa.cim.common.utils.CimBooleanUtils;
import com.fa.cim.common.utils.CimDateUtils;
import com.fa.cim.common.utils.CimStringUtils;
import com.fa.cim.common.utils.Validations;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.config.RetCodeConfigEx;
import com.fa.cim.dto.*;
import com.fa.cim.method.IEquipmentMethod;
import com.fa.cim.method.ITCSMethod;
import com.fa.cim.newcore.bo.machine.CimMachine;
import com.fa.cim.newcore.bo.machine.CimStorageMachine;
import com.fa.cim.newcore.bo.machine.MachineManager;
import com.fa.cim.newcore.factory.BaseCoreFactory;
import com.fa.cim.newcore.impl.bo.env.StandardProperties;
import com.fa.cim.newcore.standard.mchnmngm.Machine;
import com.fa.cim.remote.ITCSRemoteManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/4/10        ********             Bear               create file
 *
 * @author: Bear
 * @date: 2019/4/10 18:01
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@OmMethod
public class TCSMethod implements ITCSMethod {

    @Autowired
    private RetCodeConfig retCodeConfig;

    @Autowired
    private BaseCoreFactory baseCoreFactory;

    @Autowired
    private IEquipmentMethod equipmentMethod;

    @Autowired
    private RetCodeConfigEx retCodeConfigEx;

    @Autowired
    private ITCSRemoteManager tcsRemoteManager;

    @Autowired
    private MachineManager machineManager;



    private Boolean checkConditon(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID){

        /*------------------------------*/
        /*   Get Eqp Port Information   */
        /*------------------------------*/
        Infos.EqpPortInfo eqpPortInfo = equipmentMethod.equipmentPortInfoGet(objCommon, equipmentID);
        if (CimStringUtils.equals(eqpPortInfo.getEqpPortStatuses().get(0).getOnlineMode(), BizConstant.SP_EQP_ONLINEMODE_OFFLINE)){
            String sendToTCSFlg = StandardProperties.OM_EAP_SEND_IN_OFFLINE.getValue();
            if (!CimStringUtils.equals(sendToTCSFlg, "1")){
                return false;
            }
        }
        return true;
    }

    private String checkCellControllerForEqp(Inputs.TCSIn tcsIn){
        ObjectIdentifier equipmentID = null;
        if (null != tcsIn) {
            equipmentID = tcsIn.getEquipmentID();
        }
        CimMachine aMachine = baseCoreFactory.getBO(CimMachine.class, equipmentID);
        Validations.check(null == aMachine,new OmCode(retCodeConfig.getNotFoundEqp()), ObjectIdentifier.fetchValue(equipmentID));
        String hostName = aMachine.getCellController();
        return hostName;
    }

    private String checkCellControllerForConfirmNotStk(Inputs.TCSIn tcsIn, Boolean isStorageBool){
        Machine aMachine = null;
        CimMachine anEquipment = null;
        ObjectIdentifier equipmentID = null;
        if (null != tcsIn) {
            equipmentID = tcsIn.getEquipmentID();
        }
        aMachine = baseCoreFactory.getBO(CimMachine.class,equipmentID);
        if (null == aMachine){
            aMachine = baseCoreFactory.getBO(CimStorageMachine.class,equipmentID);
            Validations.check(null == aMachine,retCodeConfig.getNotFoundEqp(),equipmentID);
        }
        Boolean isStorageBoolFlag = aMachine.isStorageMachine();
        Validations.check(CimBooleanUtils.isTrue(isStorageBoolFlag),retCodeConfig.getNotFoundEqp(),equipmentID);
        anEquipment = (CimMachine) aMachine;
        return anEquipment.getCellController();
    }

    private Boolean checkCellControllerForStkOrEqp(Inputs.TCSIn tcsIn, Boolean isStorageBool){
        String hostName = null;
        Machine aMachine = null;
        CimMachine anEquipment = null;
        ObjectIdentifier equipmentID = null;
        if (null != tcsIn) {
            equipmentID = tcsIn.getEquipmentID();
        }
//        ObjectIdentifier stockerID = ((Inputs.SendBareReticleStockerOnlineModeChangeReqIn) tcsIn).getBareReticleStockerID();
        aMachine = baseCoreFactory.getBO(CimMachine.class,equipmentID);
        if (null == aMachine){
            aMachine = baseCoreFactory.getBO(CimStorageMachine.class,equipmentID);
            Validations.check(null == aMachine,retCodeConfig.getNotFoundEqp(),equipmentID);
        }
        Boolean isStorageBoolFlag = aMachine.isStorageMachine();
        if (CimBooleanUtils.isTrue(isStorageBoolFlag)){
            CimStorageMachine aStocker = (CimStorageMachine) aMachine;
            Validations.check(null == aStocker,retCodeConfigEx.getUnexpectedNilObject(),equipmentID);
            String stockerType = aStocker.getStockerType();
            Validations.check(!CimStringUtils.equals(BizConstant.SP_STOCKER_TYPE_BARERETICLE,stockerType),new OmCode(retCodeConfigEx.getStkTypeDifferent(),stockerType));
            hostName = aStocker.getCellController();
            if (CimStringUtils.isEmpty(hostName)){
                return false;
            }
        }else {
            anEquipment = (CimMachine)aMachine;
            Validations.check(null == anEquipment,retCodeConfigEx.getUnexpectedNilObject());
            hostName = anEquipment.getCellController();
            if (CimStringUtils.isEmpty(hostName)){
                return false;
            }
        }
        return true;
    }


    @Override
    public Outputs.TCSOut sendTCSReq(TCSReqEnum tcsReqEnum, Inputs.TCSIn tcsIn) {
        Validations.check(null == tcsIn, retCodeConfig.getInvalidParameter());
        //add tcs input log
        log.info("send to EAP Request Name：{}",null == tcsReqEnum ? null :tcsReqEnum.getValue());
        log.info("send to EAP Request Parameter：{}",null == tcsIn ? null : tcsIn.toString());
        Outputs.TCSOut tcsOut = null;
        String result = null;
        //check user first
        //-----------------------------------------
        // [UserName is "EAP"] ---> return RC_OK
        //-----------------------------------------
        if (tcsIn != null && ObjectIdentifier.equalsWithValue(tcsIn.getObjCommonIn().getUser().getUserID(), BizConstant.SP_TCS_PERSON)){
            return tcsOut;
        }
        String hostName = null;

        //check call CellController for stk
        Boolean isStorageBool = false;
        if (tcsReqEnum != null) {
            if (CimStringUtils.equals(TCSReqEnum.sendCJPJOnlineInfoInq.getValue(),tcsReqEnum.getValue())
                    || CimStringUtils.equals(TCSReqEnum.sendEDCDataItemWithTransitDataInq.getValue(),tcsReqEnum.getValue())
                    || CimStringUtils.equals(TCSReqEnum.sendPartialMoveOutReq.getValue(),tcsReqEnum.getValue())
                    || CimStringUtils.equals(TCSReqEnum.sendPJStatusChangeReq.getValue(),tcsReqEnum.getValue())
                    || CimStringUtils.equals(TCSReqEnum.sendSLMWaferRetrieveCassetteReserveReq.getValue(),tcsReqEnum.getValue())
                    || CimStringUtils.equals(TCSReqEnum.sendDurableControlJobActionReq.getValue(),tcsReqEnum.getValue())
                    || CimStringUtils.equals(TCSReqEnum.sendRecipeParamAdjustOnActivePJReq.getValue(),tcsReqEnum.getValue())
                    || CimStringUtils.equals(TCSReqEnum.sendSLMCassetteUnclampReq.getValue(),tcsReqEnum.getValue())
                    || CimStringUtils.equals(TCSReqEnum.sendMoveInReserveReq.getValue(), tcsReqEnum.getValue())
//                    || StringUtils.equals(TCSReqEnum.sendReticleStoreReq.getValue(), tcsReqEnum.getValue())
                    || CimStringUtils.equals(TCSReqEnum.sendBareReticleStockerOnlineModeChangeReq.getValue(),tcsReqEnum.getValue())
                    || CimStringUtils.equals(TCSReqEnum.sendReticleStoreReq.getValue(), tcsReqEnum.getValue())
                    || CimStringUtils.equals(TCSReqEnum.sendReticleInventoryReq.getValue(), tcsReqEnum.getValue())
                    || CimStringUtils.equals(TCSReqEnum.sendReticlePodUnclampReq.getValue(), tcsReqEnum.getValue())){
                // TODO: 2020/4/7 TCSMgr_SendReticleInventoryReq
                Boolean checkFlag = checkCellControllerForStkOrEqp(tcsIn,isStorageBool);
                if (CimBooleanUtils.isFalse(checkFlag)){
                    return tcsOut;
                }
            }
        }
        if (tcsReqEnum != null && CimStringUtils.equals(TCSReqEnum.sendBareReticleStockerOnlineModeChangeReq.getValue(),tcsReqEnum.getValue())){
            Inputs.SendBareReticleStockerOnlineModeChangeReqIn sendBareReticleStockerOnlineModeChangeReqIn = (Inputs.SendBareReticleStockerOnlineModeChangeReqIn) tcsIn;
            ObjectIdentifier stockerID = null;
            if (null != sendBareReticleStockerOnlineModeChangeReqIn) {
                stockerID = sendBareReticleStockerOnlineModeChangeReqIn.getBareReticleStockerID();
            }
            CimStorageMachine cimStorageMachine = null;
            if (ObjectIdentifier.isEmptyWithRefKey(stockerID)){
                Validations.check(ObjectIdentifier.isEmptyWithValue(stockerID),retCodeConfig.getNotFoundStocker(),"******");
                cimStorageMachine = machineManager.findStorageMachineNamed(stockerID.getValue());
            }else {
                cimStorageMachine = baseCoreFactory.getBO(CimStorageMachine.class,stockerID.getReferenceKey());
            }
            Validations.check(null == cimStorageMachine,retCodeConfig.getNotFoundStocker(),ObjectIdentifier.fetchValue(stockerID));
            if (CimStringUtils.isEmpty(cimStorageMachine.getCellController())){
                return tcsOut;
            }
        }

        //check call CellController for eqp
        if (CimStringUtils.equals(TCSReqEnum.sendWaferSortOnEqpReq.getValue(),tcsReqEnum.getValue())
            || CimStringUtils.equals(TCSReqEnum.sendWaferSortOnEqpCancelReq.getValue(),tcsReqEnum.getValue())){
            hostName = checkCellControllerForEqp(tcsIn);
            Validations.check(CimStringUtils.isEmpty(hostName),retCodeConfig.getTcsNoResponse());
        }else {
            hostName = checkCellControllerForConfirmNotStk(tcsIn,isStorageBool);
            if (CimStringUtils.isEmpty(hostName)){
                return tcsOut;
            }
        }

        //check eap info second
        if (CimStringUtils.equals(TCSReqEnum.sendNPWCarrierReserveReq.getValue(), tcsReqEnum.getValue())
                || CimStringUtils.equals(TCSReqEnum.sendNPWCarrierReserveForIBReq.getValue(), tcsReqEnum.getValue())
                || CimStringUtils.equals(TCSReqEnum.sendMoveInReserveReq.getValue(), tcsReqEnum.getValue())
                || CimStringUtils.equals(TCSReqEnum.sendMoveInReserveForIBReq.getValue(), tcsReqEnum.getValue())
                || CimStringUtils.equals(TCSReqEnum.sendRecipeParamAdjustReq.getValue(), tcsReqEnum.getValue())
                || CimStringUtils.equals(TCSReqEnum.sendControlJobActionReq.getValue(), tcsReqEnum.getValue())
                || CimStringUtils.equals(TCSReqEnum.sendMoveInReserveCancelReq.getValue(), tcsReqEnum.getValue())
                || CimStringUtils.equals(TCSReqEnum.sendMoveInReserveCancelForIBReq.getValue(), tcsReqEnum.getValue())
                || CimStringUtils.equals(TCSReqEnum.sendArrivalCarrierNotificationCancelForInternalBufferReq.getValue(), tcsReqEnum.getValue())
                || CimStringUtils.equals(TCSReqEnum.sendArrivalCarrierNotificationCancel.getValue(), tcsReqEnum.getValue())
                || CimStringUtils.equals(TCSReqEnum.sendCJPJProgressInfoInq.getValue(), tcsReqEnum.getValue())
                || CimStringUtils.equals(TCSReqEnum.sendMoveOutForIBReq.getValue(), tcsReqEnum.getValue())
                || CimStringUtils.equals(TCSReqEnum.sendMoveOutReq.getValue(), tcsReqEnum.getValue())
                || CimStringUtils.equals(TCSReqEnum.sendMoveInCancelForIBReq.getValue(), tcsReqEnum.getValue())
                || CimStringUtils.equals(TCSReqEnum.sendMoveInCancelReq.getValue(), tcsReqEnum.getValue())
                || CimStringUtils.equals(TCSReqEnum.sendMoveInForIBReq.getValue(), tcsReqEnum.getValue())
                || CimStringUtils.equals(TCSReqEnum.sendMoveInReq.getValue(), tcsReqEnum.getValue())
                || CimStringUtils.equals(TCSReqEnum.sendSLMWaferRetrieveCassetteReserveReq.getValue(), tcsReqEnum.getValue())
                || CimStringUtils.equals(TCSReqEnum.sendDurableControlJobActionReq.getValue(), tcsReqEnum.getValue())
                || CimStringUtils.equals(TCSReqEnum.sendReserveCancelUnloadingLotsForIBReq.getValue(), tcsReqEnum.getValue())) {
            Boolean flag = checkConditon(tcsIn.getObjCommonIn(), tcsIn.getEquipmentID());
            if (CimBooleanUtils.isFalse(flag)) {
                return tcsOut;
            }
        }

        /*-------------------------------------------------*/
        /*   Send Tx to EAP   */
        /*-------------------------------------------------*/
        log.info("Send Tx to EAP");

        long sleepTimeValue = StandardProperties.OM_EAP_CONNECT_SLEEP_TIME.getLongValue();
        long retryCountValue = StandardProperties.OM_EAP_CONNECT_RETRY_COUNT.getLongValue();


        log.info("env value of OM_EAP_CONNECT_SLEEP_TIME  = {}", sleepTimeValue);
        log.info("env value of OM_EAP_CONNECT_RETRY_COUNT = {}", retryCountValue);

        for (long i = 0, j = retryCountValue + 1; i < j; i++) {
            /*--------------------------*/
            /*    Send Request to EAP   */
            /*--------------------------*/
            try {
                switch (tcsReqEnum) {
                    case sendRecipeDownloadReq:
                        Inputs.SendRecipeDownloadReqIn sendRecipeDownloadReqIn = (Inputs.SendRecipeDownloadReqIn) tcsIn;
                        sendRecipeDownloadReqIn.setSendName(TCSReqEnum.sendRecipeDownloadReq.getValue());
                        sendRecipeDownloadReqIn.setRealTransaction(TransactionIDEnum.RECIPE_DOWNLOAD_REQ.getValue());
                        tcsRemoteManager.sendRecipeDownloadReq(this.setRemoteManagerRequestParam(sendRecipeDownloadReqIn));
                        break;
                    case sendRecipeCompareReq:
                        Inputs.SendRecipeCompareReqIn sendRecipeCompareReqIn = (Inputs.SendRecipeCompareReqIn) tcsIn;
                        sendRecipeCompareReqIn.setSendName(TCSReqEnum.sendRecipeCompareReq.getValue());
                        sendRecipeCompareReqIn.setRealTransaction(TransactionIDEnum.RECIPE_CONFIRMATION_REQ.getValue());
                        tcsRemoteManager.sendRecipeCompareReq(this.setRemoteManagerRequestParam(sendRecipeCompareReqIn));
                        break;
                    case sendRecipeDeleteInFileReq:
                        Inputs.SendRecipeDeleteInFileReqIn sendRecipeDeleteInFileReqIn = (Inputs.SendRecipeDeleteInFileReqIn) tcsIn;
                        sendRecipeDeleteInFileReqIn.setSendName(TCSReqEnum.sendRecipeDeleteInFileReq.getValue());
                        sendRecipeDeleteInFileReqIn.setRealTransaction(TransactionIDEnum.RECIPE_FILE_DELETION_REQ.getValue());
                        tcsRemoteManager.sendRecipeDeleteInFileReq(this.setRemoteManagerRequestParam(sendRecipeDeleteInFileReqIn));
                        break;
                    case sendNPWCarrierReserveReq:
                        Inputs.SendNPWCarrierReserveReqIn sendNPWCarrierReserveReqIn = (Inputs.SendNPWCarrierReserveReqIn) tcsIn;
                        sendNPWCarrierReserveReqIn.setSendName(TCSReqEnum.sendNPWCarrierReserveReq.getValue());
                        sendNPWCarrierReserveReqIn.setRealTransaction(TransactionIDEnum.ARRIVAL_CARRIER_NOTIFICATION_REQ.getValue());
                        tcsRemoteManager.sendNPWCarrierReserveReq(this.setRemoteManagerRequestParam(sendNPWCarrierReserveReqIn));
                        break;
                    case sendPJStatusChangeReq:
                        Inputs.SendPJStatusChangeReqIn sendPJStatusChangeReqIn = (Inputs.SendPJStatusChangeReqIn) tcsIn;
                        sendPJStatusChangeReqIn.setSendName(TCSReqEnum.sendPJStatusChangeReq.getValue());
                        sendPJStatusChangeReqIn.setRealTransaction(TransactionIDEnum.PROCESS_JOB_MANAGE_REQ.getValue());
                        tcsRemoteManager.sendPJStatusChangeReq(this.setRemoteManagerRequestParam(sendPJStatusChangeReqIn));
                        break;
                    case sendNPWCarrierReserveForIBReq:
                        Inputs.SendNPWCarrierReserveForIBReqIn sendNPWCarrierReserveForIBReqIn = (Inputs.SendNPWCarrierReserveForIBReqIn) tcsIn;
                        sendNPWCarrierReserveForIBReqIn.setSendName(TCSReqEnum.sendNPWCarrierReserveForIBReq.getValue());
                        sendNPWCarrierReserveForIBReqIn.setRealTransaction(TransactionIDEnum.ARRIVAL_CARRIER_NOTIFICATION_FOR_INTERNAL_BUFFER_REQ.getValue());
                        tcsRemoteManager.sendNPWCarrierReserveForIBReq(this.setRemoteManagerRequestParam(sendNPWCarrierReserveForIBReqIn));
                        break;
                    case sendRecipeDirectoryInq:
                        Inputs.SendRecipeDirectoryInqIn sendRecipeDirectoryInqIn = (Inputs.SendRecipeDirectoryInqIn) tcsIn;
                        sendRecipeDirectoryInqIn.setSendName(TCSReqEnum.sendRecipeDirectoryInq.getValue());
                        sendRecipeDirectoryInqIn.setRealTransaction(TransactionIDEnum.RECIPE_DIRECTORY_INQ.getValue());
                        result = tcsRemoteManager.sendRecipeDirectoryInq(this.setRemoteManagerRequestParam(sendRecipeDirectoryInqIn));
                        tcsOut = JSONObject.parseObject(result, Outputs.SendRecipeDirectoryInqOut.class);
                        break;
                    case sendRecipeUploadReq:
                        Inputs.SendRecipeUploadReqIn sendRecipeUploadReqIn = (Inputs.SendRecipeUploadReqIn) tcsIn;
                        sendRecipeUploadReqIn.setSendName(TCSReqEnum.sendRecipeUploadReq.getValue());
                        sendRecipeUploadReqIn.setRealTransaction(TransactionIDEnum.RECIPE_UPLOAD_REQ.getValue());
                        tcsRemoteManager.sendRecipeUploadReq(this.setRemoteManagerRequestParam(sendRecipeUploadReqIn));
                        break;
                    case sendRecipeDeleteReq:
                        Inputs.SendRecipeDeleteReqIn sendRecipeDeleteReqIn = (Inputs.SendRecipeDeleteReqIn) tcsIn;
                        sendRecipeDeleteReqIn.setSendName(TCSReqEnum.sendRecipeDeleteReq.getValue());
                        sendRecipeDeleteReqIn.setRealTransaction(TransactionIDEnum.RECIPE_DELETION_REQ.getValue());
                        tcsRemoteManager.sendRecipeDeleteReq(this.setRemoteManagerRequestParam(sendRecipeDeleteReqIn));
                        break;
                    case sendEqpEAPInfoInq:
                        Inputs.SendEqpEAPInfoInqIn tcsEqpEAPInfoInq = (Inputs.SendEqpEAPInfoInqIn) tcsIn;
                        tcsEqpEAPInfoInq.setSendName(TCSReqEnum.sendEqpEAPInfoInq.getValue());
                        tcsEqpEAPInfoInq.setRealTransaction(TransactionIDEnum.EQP_DETAIL_INFO_INQ.getValue());
                        result = tcsRemoteManager.sendEqpEAPInfoInq(this.setRemoteManagerRequestParam(tcsEqpEAPInfoInq));
                        tcsOut = JSONObject.parseObject(result, Outputs.SendEqpEAPInfoInqOut.class);
                        break;
                    case sendMoveInReserveReq:
                        Inputs.SendMoveInReserveReqIn tcsMoveInReserveReq = (Inputs.SendMoveInReserveReqIn) tcsIn;
                        tcsMoveInReserveReq.setSendName(TCSReqEnum.sendMoveInReserveReq.getValue());
                        tcsMoveInReserveReq.setRealTransaction(TransactionIDEnum.START_LOTS_RESERVATION_REQ.getValue());
                        result = tcsRemoteManager.sendMoveInReserveReq(this.setRemoteManagerRequestParam(tcsMoveInReserveReq));
                        tcsOut = JSON.parseObject(result, Outputs.SendMoveInReserveReqOut.class);
                        break;
                    case sendMoveInReserveForIBReq:
                        Inputs.SendMoveInReserveForIBReqIn moveInReserveForIBReqIn = (Inputs.SendMoveInReserveForIBReqIn) tcsIn;
                        moveInReserveForIBReqIn.setSendName(TCSReqEnum.sendMoveInReserveForIBReq.getValue());
                        moveInReserveForIBReqIn.setRealTransaction(TransactionIDEnum.START_LOTS_RESERVATION_FOR_INTERNAL_BUFFER_REQ.getValue());
                        result = tcsRemoteManager.sendMoveInReserveForIBReq(this.setRemoteManagerRequestParam(moveInReserveForIBReqIn));
                        tcsOut = JSONObject.parseObject(result, Outputs.SendMoveInReserveForIBReqOut.class);
                        break;
                    case sendEqpModeChangeReq:
                        Inputs.SendEqpModeChangeReqIn sendEqpModeChangeReqIn = (Inputs.SendEqpModeChangeReqIn) tcsIn;
                        sendEqpModeChangeReqIn.setSendName(TCSReqEnum.sendEqpModeChangeReq.getValue());
                        sendEqpModeChangeReqIn.setRealTransaction(TransactionIDEnum.EQP_MODE_CHANGE_REQ.getValue());
                        tcsRemoteManager.sendEqpModeChangeReq(this.setRemoteManagerRequestParam(sendEqpModeChangeReqIn));
                        break;
                    case sendCarrierTransferJobEndRpt:
                        Inputs.SendCarrierTransferJobEndRptIn sendCarrierTransferJobEndRptIn = (Inputs.SendCarrierTransferJobEndRptIn) tcsIn;
                        sendCarrierTransferJobEndRptIn.setSendName(TCSReqEnum.sendCarrierTransferJobEndRpt.getValue());
                        sendCarrierTransferJobEndRptIn.setRealTransaction(TransactionIDEnum.LOT_ASSETTE_XFER_JOB_COMP_RPT.getValue());
                        tcsRemoteManager.sendCarrierTransferJobEndRpt(this.setRemoteManagerRequestParam(sendCarrierTransferJobEndRptIn));
                        break;
                    case sendTcsRecoveryReq:
                        Inputs.SendEAPRecoveryReqIn sendEAPRecoveryReqIn = (Inputs.SendEAPRecoveryReqIn) tcsIn;
                        sendEAPRecoveryReqIn.setSendName(TCSReqEnum.sendTcsRecoveryReq.getValue());
                        sendEAPRecoveryReqIn.setRealTransaction(TransactionIDEnum.TCS_RECOVERY_REQ.getValue());
                        result = tcsRemoteManager.sendEAPRecoveryReq(this.setRemoteManagerRequestParam(sendEAPRecoveryReqIn));
                        tcsOut = JSONObject.parseObject(result, Outputs.SendEAPRecoveryReqOut.class);
                        break;
                    case sendRecipeParamAdjustReq:
                        Inputs.SendRecipeParamAdjustReqIn sendRecipeParamAdjustReqIn = (Inputs.SendRecipeParamAdjustReqIn) tcsIn;
                        sendRecipeParamAdjustReqIn.setSendName(TCSReqEnum.sendRecipeParamAdjustReq.getValue());
                        sendRecipeParamAdjustReqIn.setRealTransaction(TransactionIDEnum.RECIPE_PARAMETER_ADJUST_REQ.getValue());
                        tcsRemoteManager.sendRecipeParamAdjustReq(this.setRemoteManagerRequestParam(sendRecipeParamAdjustReqIn));
                        break;
                    case sendControlJobActionReq:
                        Inputs.SendControlJobActionReqIn sendControlJobActionReqIn = (Inputs.SendControlJobActionReqIn) tcsIn;
                        sendControlJobActionReqIn.setSendName(TCSReqEnum.sendControlJobActionReq.getValue());
                        sendControlJobActionReqIn.setRealTransaction(TransactionIDEnum.CONTROL_JOB_MANAGE_REQ.getValue());
                        tcsRemoteManager.sendControlJobActionReq(this.setRemoteManagerRequestParam(sendControlJobActionReqIn));
                        break;
                    case sendMoveInReserveCancelReq:
                        Inputs.SendMoveInReserveCancelReqIn sendMoveInReserveCancelReqIn = (Inputs.SendMoveInReserveCancelReqIn) tcsIn;
                        sendMoveInReserveCancelReqIn.setSendName(TCSReqEnum.sendMoveInReserveCancelReq.getValue());
                        sendMoveInReserveCancelReqIn.setRealTransaction(TransactionIDEnum.START_LOTS_RESERVATION_CANCEL_REQ.getValue());
                        result = tcsRemoteManager.sendMoveInReserveCancelReq(this.setRemoteManagerRequestParam(sendMoveInReserveCancelReqIn));
                        tcsOut = JSONObject.parseObject(result, Outputs.SendMoveInReserveCancelReqOut.class);
                        break;
                    case sendMoveInReserveCancelForIBReq:
                        Inputs.SendMoveInReserveCancelForIBReqIn sendMoveInReserveCancelForIBReqIn = (Inputs.SendMoveInReserveCancelForIBReqIn) tcsIn;
                        sendMoveInReserveCancelForIBReqIn.setSendName(TCSReqEnum.sendMoveInReserveCancelForIBReq.getValue());
                        sendMoveInReserveCancelForIBReqIn.setRealTransaction(TransactionIDEnum.START_LOTS_RESERVATION_CANCEL_FOR_INTERNAL_BUFFER_REQ.getValue());
                        result = tcsRemoteManager.sendMoveInReserveCancelForIBReq(this.setRemoteManagerRequestParam(sendMoveInReserveCancelForIBReqIn));
                        tcsOut = JSONObject.parseObject(result, Outputs.SendMoveInReserveCancelForIBReqOut.class);
                        break;
                    case sendArrivalCarrierNotificationCancelForInternalBufferReq:
                        Inputs.SendArrivalCarrierNotificationCancelForInternalBufferReqIn sendArrivalCarrierNotificationCancelForInternalBufferReqIn = (Inputs.SendArrivalCarrierNotificationCancelForInternalBufferReqIn) tcsIn;
                        sendArrivalCarrierNotificationCancelForInternalBufferReqIn.setSendName(TCSReqEnum.sendArrivalCarrierNotificationCancelForInternalBufferReq.getValue());
                        sendArrivalCarrierNotificationCancelForInternalBufferReqIn.setRealTransaction(TransactionIDEnum.ARRIVAL_CARRIER_CANCEL_FOR_INTERNAL_BUFFER_REQ.getValue());
                        tcsRemoteManager.sendArrivalCarrierNotificationCancelForInternalBufferReq(this.setRemoteManagerRequestParam(sendArrivalCarrierNotificationCancelForInternalBufferReqIn));
                        break;
                    case sendArrivalCarrierNotificationCancel:
                        Inputs.SendArrivalCarrierNotificationCancelReqIn sendArrivalCarrierNotificationCancelReqIn = (Inputs.SendArrivalCarrierNotificationCancelReqIn) tcsIn;
                        sendArrivalCarrierNotificationCancelReqIn.setSendName(TCSReqEnum.sendArrivalCarrierNotificationCancel.getValue());
                        sendArrivalCarrierNotificationCancelReqIn.setRealTransaction(TransactionIDEnum.ARRIVAL_CARRIER_CANCEL_REQ.getValue());
                        tcsRemoteManager.sendArrivalCarrierNotificationCancelReq(this.setRemoteManagerRequestParam(sendArrivalCarrierNotificationCancelReqIn));
                        break;
                    case sendCJPJProgressInfoInq:
                        Inputs.SendCJPJProgressIn sendCJPJProgressIn = (Inputs.SendCJPJProgressIn) tcsIn;
                        sendCJPJProgressIn.setSendName(TCSReqEnum.sendCJPJProgressInfoInq.getValue());
                        sendCJPJProgressIn.setRealTransaction(TransactionIDEnum.CIPJ_PROGRESS_INQ.getValue());
                        result = tcsRemoteManager.sendCJPJProgressInfoInq(this.setRemoteManagerRequestParam(sendCJPJProgressIn));
                        tcsOut = JSONObject.parseObject(result, Outputs.SendCJPJProgressOut.class);
                        break;
                    case sendCJPJOnlineInfoInq:
                        Inputs.SendCJPJInfoIn sendCJPJInfoIn = (Inputs.SendCJPJInfoIn) tcsIn;
                        sendCJPJInfoIn.setSendName(TCSReqEnum.sendCJPJOnlineInfoInq.getValue());
                        sendCJPJInfoIn.setRealTransaction(TransactionIDEnum.CIPJ_INFO_INQ.getValue());
                        result = tcsRemoteManager.sendCJPJOnlineInfoInq(this.setRemoteManagerRequestParam(sendCJPJInfoIn));
                        tcsOut = JSONObject.parseObject(result, Outputs.SendCJPJOnlineInfoInqOut.class);
                        break;
                    case sendRecipeParamAdjustOnActivePJReq:
                        Inputs.SendRecipeParamAdjustOnActivePJReqIn sendRecipeParamAdjustOnActivePJReqIn = (Inputs.SendRecipeParamAdjustOnActivePJReqIn) tcsIn;
                        sendRecipeParamAdjustOnActivePJReqIn.setSendName(TCSReqEnum.sendRecipeParamAdjustOnActivePJReq.getValue());
                        sendRecipeParamAdjustOnActivePJReqIn.setRealTransaction(TransactionIDEnum.RECIPE_PARAMETER_ADJUST_IN_PROCESSING_REQ.getValue());
                        result = tcsRemoteManager.sendRecipeParamAdjustOnActivePJReq(this.setRemoteManagerRequestParam(sendRecipeParamAdjustOnActivePJReqIn));
                        tcsOut = JSONObject.parseObject(result, Outputs.SendRecipeParamAdjustOnActivePJReqOut.class);
                        break;
                    case sendEDCDataItemWithTransitDataInq:
                        Inputs.SendEDCDataItemWithTransitDataInqIn sendEDCDataItemWithTransitDataInqIn = (Inputs.SendEDCDataItemWithTransitDataInqIn) tcsIn;
                        sendEDCDataItemWithTransitDataInqIn.setSendName(TCSReqEnum.sendEDCDataItemWithTransitDataInq.getValue());
                        sendEDCDataItemWithTransitDataInqIn.setRealTransaction(TransactionIDEnum.DATA_ITEM_WITH_TEMP_DATA_INQ.getValue());
                        result = tcsRemoteManager.sendEDCDataItemWithTransitDataInq(this.setRemoteManagerRequestParam(sendEDCDataItemWithTransitDataInqIn));
                        tcsOut = JSONObject.parseObject(result, Outputs.SendEDCDataItemWithTransitDataInqOut.class);
                        break;
                    case sendMoveOutForIBReq:
                        Inputs.SendMoveOutForIBReqIn sendMoveOutForIBReqIn = (Inputs.SendMoveOutForIBReqIn) tcsIn;
                        sendMoveOutForIBReqIn.setSendName(TCSReqEnum.sendMoveOutForIBReq.getValue());
                        sendMoveOutForIBReqIn.setRealTransaction(TransactionIDEnum.OPERATION_COMP_FOR_INTERNAL_BUFFER_REQ.getValue());
                        result = tcsRemoteManager.sendMoveOutForIBReq(this.setRemoteManagerRequestParam(sendMoveOutForIBReqIn));
                        tcsOut = JSONObject.parseObject(result, Outputs.SendMoveOutForIBReqOut.class);
                        break;
                    case sendMoveOutReq:
                        Inputs.SendMoveOutReqIn sendMoveOutReqIn = (Inputs.SendMoveOutReqIn) tcsIn;
                        sendMoveOutReqIn.setSendName(TCSReqEnum.sendMoveOutReq.getValue());
                        sendMoveOutReqIn.setRealTransaction(TransactionIDEnum.OPERATION_COMP_WITH_DATA_REQ.getValue());
                        result = tcsRemoteManager.sendMoveOutReq(this.setRemoteManagerRequestParam(sendMoveOutReqIn));
                        tcsOut = JSONObject.parseObject(result, Outputs.SendMoveOutReqOut.class);
                        break;
                    case sendMoveInCancelForIBReq:
                        Inputs.SendMoveInCancelForIBReqIn sendMoveInCancelForIBReqIn = (Inputs.SendMoveInCancelForIBReqIn) tcsIn;
                        sendMoveInCancelForIBReqIn.setSendName(TCSReqEnum.sendMoveInCancelForIBReq.getValue());
                        sendMoveInCancelForIBReqIn.setRealTransaction(TransactionIDEnum.OPERATION_START_CANCEL_FOR_INTERNAL_BUFFER_REQ.getValue());
                        result = tcsRemoteManager.sendMoveInCancelForIBReq(this.setRemoteManagerRequestParam(sendMoveInCancelForIBReqIn));
                        tcsOut = JSONObject.parseObject(result, Outputs.SendMoveInCancelForIBReqOut.class);
                        break;
                    case sendMoveInCancelReq:
                        Inputs.SendMoveInCancelReqIn sendMoveInCancelReqIn = (Inputs.SendMoveInCancelReqIn) tcsIn;
                        sendMoveInCancelReqIn.setSendName(TCSReqEnum.sendMoveInCancelReq.getValue());
                        sendMoveInCancelReqIn.setRealTransaction(TransactionIDEnum.OPERATION_START_CANCEL_REQ.getValue());
                        result = tcsRemoteManager.sendMoveInCancelReq(this.setRemoteManagerRequestParam(sendMoveInCancelReqIn));
                        tcsOut = JSONObject.parseObject(result, Outputs.SendMoveInCancelReqOut.class);
                        break;
                    case sendMoveInForIBReq:
                        Inputs.SendMoveInForIBReqIn sendMoveInForIBReqIn = (Inputs.SendMoveInForIBReqIn) tcsIn;
                        sendMoveInForIBReqIn.setSendName(TCSReqEnum.sendMoveInForIBReq.getValue());
                        sendMoveInForIBReqIn.setRealTransaction(TransactionIDEnum.OPERATION_START_FOR_INTERNAL_BUFFER_REQ.getValue());
                        result = tcsRemoteManager.sendMoveInForIBReq(this.setRemoteManagerRequestParam(sendMoveInForIBReqIn));
                        tcsOut = JSONObject.parseObject(result, Outputs.SendMoveInForIBReqOut.class);
                        break;
                    case sendMoveInReq:
                        Inputs.SendMoveInReqIn sendMoveInReqIn = (Inputs.SendMoveInReqIn) tcsIn;
                        sendMoveInReqIn.setSendName(TCSReqEnum.sendMoveInReq.getValue());
                        sendMoveInReqIn.setRealTransaction(TransactionIDEnum.OPERATION_START_REQ.getValue());
                        result = tcsRemoteManager.sendMoveInReq(this.setRemoteManagerRequestParam(sendMoveInReqIn));
                        tcsOut = JSONObject.parseObject(result, Outputs.SendMoveInReqOut.class);
                        break;
                    case sendWaferSortOnEqpReq:
                        Inputs.SendWaferSortOnEqpReqIn sendWaferSortOnEqpReqIn = (Inputs.SendWaferSortOnEqpReqIn) tcsIn;
                        sendWaferSortOnEqpReqIn.setSendName(TCSReqEnum.sendWaferSortOnEqpReq.getValue());
                        sendWaferSortOnEqpReqIn.setRealTransaction(TransactionIDEnum.WAFER_SORTER_ON_EQP_REQ.getValue());
                        tcsRemoteManager.sendWaferSortOnEqpReq(this.setRemoteManagerRequestParam(sendWaferSortOnEqpReqIn));
                        break;
                    case sendWaferSortOnEqpCancelReq:
                        Inputs.SendWaferSortOnEqpCancelReqIn sendWaferSortOnEqpCancelReqIn = (Inputs.SendWaferSortOnEqpCancelReqIn) tcsIn;
                        sendWaferSortOnEqpCancelReqIn.setSendName(TCSReqEnum.sendWaferSortOnEqpCancelReq.getValue());
                        sendWaferSortOnEqpCancelReqIn.setRealTransaction(TransactionIDEnum.WAFER_SORTER_ON_EQP_CANCEL_REQ.getValue());
                        tcsRemoteManager.sendWaferSortOnEqpCancelReq(this.setRemoteManagerRequestParam(sendWaferSortOnEqpCancelReqIn));
                        break;
                    case sendPartialMoveOutReq:
                        Inputs.SendPartialMoveOutReqIn sendPartialMoveOutReqIn = (Inputs.SendPartialMoveOutReqIn) tcsIn;
                        sendPartialMoveOutReqIn.setSendName(TCSReqEnum.sendPartialMoveOutReq.getValue());
                        sendPartialMoveOutReqIn.setRealTransaction(TransactionIDEnum.PARTIAL_OPERATION_COMP_WITH_DATA_REQ.getValue());
                        result = tcsRemoteManager.sendPartialMoveOutReq(this.setRemoteManagerRequestParam(sendPartialMoveOutReqIn));
                        tcsOut = JSONObject.parseObject(result, Outputs.SendPartialMoveOutReqOut.class);
                        break;
                    case sendReserveCancelUnloadingLotsForIBReq:
                        Inputs.SendReserveCancelUnloadingLotsForIBReqIn sendReserveCancelUnloadingLotsForIBReqIn = (Inputs.SendReserveCancelUnloadingLotsForIBReqIn) tcsIn;
                        sendReserveCancelUnloadingLotsForIBReqIn.setSendName(TCSReqEnum.sendReserveCancelUnloadingLotsForIBReq.getValue());
                        sendReserveCancelUnloadingLotsForIBReqIn.setRealTransaction(TransactionIDEnum.UN_LOADING_LOTS_RESERVATION_CANCEL_FOR_INTERNAL_BUFFER_REQ.getValue());
                        tcsRemoteManager.sendReserveCancelUnloadingLotsForIBReq(this.setRemoteManagerRequestParam(sendReserveCancelUnloadingLotsForIBReqIn));
                        break;
                    case sendDoSpcCheck:
//                        Inputs.SpcInput spcInput = (Inputs.SpcInput) tcsIn;
//                        spcInput.setSendName(TCSReqEnum.sendDoSpcCheck.getValue());
//                        spcInput.setRealTransaction(TransactionIDEnum.SPC_CHECK_REQ.getValue());
//                        result = tcsRemoteManager.doSpcCheck(this.setRemoteManagerRequestParam(spcInput));
//                        tcsOut = JSONObject.parseObject(result, Outputs.SpcOutput.class);
//                        break;
                    case sendSLMCassetteUnclampReq:
                        Inputs.SendSLMCassetteUnclampReqIn slmUnclampIn = (Inputs.SendSLMCassetteUnclampReqIn) tcsIn;
                        slmUnclampIn.setSendName(TCSReqEnum.sendSLMCassetteUnclampReq.getValue());
                        slmUnclampIn.setRealTransaction(TransactionIDEnum.SLM_CASSETTE_UNCLAMP_REQ.getValue());
                        tcsRemoteManager.sendSLMCassetteUnclampReq(this.setRemoteManagerRequestParam(slmUnclampIn));
                        break;
                    case sendSLMWaferRetrieveCassetteReserveReq:
                        Inputs.SLMWaferRetrieveCassetteReserveReqIn slmWaferRetrieveCassetteReserveReqIn = (Inputs.SLMWaferRetrieveCassetteReserveReqIn) tcsIn;
                        slmWaferRetrieveCassetteReserveReqIn.setSendName(TCSReqEnum.sendSLMWaferRetrieveCassetteReserveReq.getValue());
                        slmWaferRetrieveCassetteReserveReqIn.setRealTransaction(TransactionIDEnum.FMC_WAFER_RETRIEVE_CARRIER_RESERVE_REQ.getValue());
                        tcsRemoteManager.sendSLMWaferRetrieveCassetteReserve(this.setRemoteManagerRequestParam(slmWaferRetrieveCassetteReserveReqIn));
                        break;
                    case sendDurableControlJobActionReq:
                        Inputs.SendDurableControlJobActionReqIn sendDurableControlJobActionReqIn = (Inputs.SendDurableControlJobActionReqIn) tcsIn;
                        sendDurableControlJobActionReqIn.setSendName(TCSReqEnum.sendDurableControlJobActionReq.getValue());
                        sendDurableControlJobActionReqIn.setRealTransaction(TransactionIDEnum.DURABLE_CONTROL_JOB_MANAGE_REQ.getValue());
                        tcsRemoteManager.sendDurableControlJobActionReq(this.setRemoteManagerRequestParam(sendDurableControlJobActionReqIn));
                        break;
                    case sendPartialMoveOutForInternalBufferReq:
                        Inputs.SendPartialMoveOutReqIn sendPartialMoveOutReqInForIB = (Inputs.SendPartialMoveOutReqIn) tcsIn;
                        sendPartialMoveOutReqInForIB.setSendName(TCSReqEnum.sendPartialMoveOutForInternalBufferReq.getValue());
                        sendPartialMoveOutReqInForIB.setRealTransaction(TransactionIDEnum.PARTIAL_OPERATION_COMP_FOR_INTERNAL_BUFFER_REQ.getValue());
                        result = tcsRemoteManager.sendPartialMoveOutForIBReq(this.setRemoteManagerRequestParam(sendPartialMoveOutReqInForIB));
                        tcsOut = JSONObject.parseObject(result, Outputs.SendPartialMoveOutReqOut.class);
                        break;
                    case SORT_JOB_CREATE_REQ:
                        Inputs.SendSortJobNotificationReqIn sendSortJobNotificationReqIn = (Inputs.SendSortJobNotificationReqIn) tcsIn;
                        sendSortJobNotificationReqIn.setSendName(TCSReqEnum.SORT_JOB_CREATE_REQ.getValue());
                        sendSortJobNotificationReqIn.setRealTransaction(TransactionIDEnum.SORT_JOB_CREATE_REQ.getValue());
                        result = tcsRemoteManager.sortJobCreateReq(this.setRemoteManagerRequestParam(sendSortJobNotificationReqIn));
                        tcsOut = JSONObject.parseObject(result, Outputs.SendPartialMoveOutReqOut.class);
                        break;
                    case SORT_JOB_CANCEL_REQ:
                        Inputs.SendSortJobCancelNotificationReqIn sendSortJobCancelNotificationReqIn = (Inputs.SendSortJobCancelNotificationReqIn) tcsIn;
                        sendSortJobCancelNotificationReqIn.setSendName(TCSReqEnum.SORT_JOB_CANCEL_REQ.getValue());
                        sendSortJobCancelNotificationReqIn.setRealTransaction(TransactionIDEnum.SORT_JOB_CANCEL_REQ.getValue());
                        result = tcsRemoteManager.sortJobCancelReq(this.setRemoteManagerRequestParam(sendSortJobCancelNotificationReqIn));
                        tcsOut = JSONObject.parseObject(result, Outputs.SendPartialMoveOutReqOut.class);
                        break;
                    case sendDurableMoveInReq:
                        Inputs.SendDurableOpeStartReqIn sendDurableOpeStartReqIn = (Inputs.SendDurableOpeStartReqIn) tcsIn;
                        sendDurableOpeStartReqIn.setSendName(TCSReqEnum.sendDurableMoveInReq.getValue());
                        sendDurableOpeStartReqIn.setRealTransaction(TransactionIDEnum.DURABLE_OPERATION_START_REQ.getValue());
                        result = tcsRemoteManager.durableOperationStartReq(this.setRemoteManagerRequestParam(sendDurableOpeStartReqIn));
                        tcsOut = JSONObject.parseObject(result, Outputs.SendPartialMoveOutReqOut.class);
                        break;
                    case sendReticleRetrieveReq:
                        Inputs.SendReticleRetrieveReqIn sendReticleRetrieveReqIn = (Inputs.SendReticleRetrieveReqIn) tcsIn;
                        sendReticleRetrieveReqIn.setSendName(TCSReqEnum.sendReticleRetrieveReq.getValue());
                        sendReticleRetrieveReqIn.setRealTransaction(TransactionIDEnum.RETICLE_RETRIEVE_REQ.getValue());
                        tcsRemoteManager.sendReticleRetrieveReq(this.setRemoteManagerRequestParam(sendReticleRetrieveReqIn));
                        break;
                    case sendReticleStoreReq:
                        Inputs.SendReticleStoreReqIn sendReticleStoreReqIn = (Inputs.SendReticleStoreReqIn) tcsIn;
                        sendReticleStoreReqIn.setSendName(TCSReqEnum.sendReticleStoreReq.getValue());
                        sendReticleStoreReqIn.setRealTransaction(TransactionIDEnum.RETICLE_STORE_REQ.getValue());
                        tcsRemoteManager.sendReticleStoreReq(this.setRemoteManagerRequestParam(sendReticleStoreReqIn));
                        break;
                    case sendReticlePodUnclampReq:
                        Inputs.SendReticlePodUnclampReqIn sendReticlePodUnclampReqIn = (Inputs.SendReticlePodUnclampReqIn) tcsIn;
                        sendReticlePodUnclampReqIn.setSendName(TCSReqEnum.sendReticlePodUnclampReq.getValue());
                        sendReticlePodUnclampReqIn.setRealTransaction(TransactionIDEnum.RETICLE_POD_UNCLAMP_REQ.getValue());
                        tcsRemoteManager.sendReticlePodUnclampReq(this.setRemoteManagerRequestParam(sendReticlePodUnclampReqIn));
                        break;
                    case sendBareReticleStockerOnlineModeChangeReq:
                        Inputs.SendBareReticleStockerOnlineModeChangeReqIn sendBareReticleStockerOnlineModeChangeReqIn = (Inputs.SendBareReticleStockerOnlineModeChangeReqIn) tcsIn;
                        sendBareReticleStockerOnlineModeChangeReqIn.setSendName(TCSReqEnum.sendBareReticleStockerOnlineModeChangeReq.getValue());
                        sendBareReticleStockerOnlineModeChangeReqIn.setRealTransaction(TransactionIDEnum.BARE_RETICLE_STOCKER_ONLINE_MODE_CHANGE_REQ.getValue());
                        tcsRemoteManager.sendBareReticleStockerOnlineModeChangeReq(this.setRemoteManagerRequestParam(sendBareReticleStockerOnlineModeChangeReqIn));
                        break;
                    case sendReticleInventoryReq:
                        Inputs.SendReticleInventoryReqIn sendReticleInventoryReqIn = (Inputs.SendReticleInventoryReqIn) tcsIn;
                        sendReticleInventoryReqIn.setSendName(TCSReqEnum.sendReticleInventoryReq.getValue());
                        sendReticleInventoryReqIn.setRealTransaction(TransactionIDEnum.RETICLE_INVENTORY_REQ.getValue());
                        tcsRemoteManager.sendReticleInventoryReq(this.setRemoteManagerRequestParam(sendReticleInventoryReqIn));
                        break;
                }

            } catch (ServiceException ex) {
                Integer retCode = ex.getCode();
                if (Validations.isEquals(retCodeConfig.getExtServiceBindFail(), retCode)
                        || Validations.isEquals(retCodeConfig.getExtServiceNilObj(), retCode)
                        || Validations.isEquals(retCodeConfig.getTcsNoResponse(), retCode)) {
                    //If the last retry, still fails, throw an exception
                    if (i == j - 1) {
                        throw ex;
                    }

                    //If you fail, try again
                    try {
                        Thread.sleep(sleepTimeValue);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        log.error(e.getMessage(),e);
                    }
                    continue;
                } else {
                    throw ex;
                }
            } catch (Exception e) {
                throw e;
            }
            //Exit if successful
            break;
        }
        return tcsOut;
    }


    private void setTransactionRoute(Inputs.TCSIn tcsIn) {
        if (tcsIn.getObjCommonIn().getTransactionID().equals(tcsIn.getRealTransaction())){
            tcsIn.setTransactionRoute(tcsIn.getRealTransaction());
        } else {
            tcsIn.setTransactionRoute(tcsIn.getObjCommonIn().getTransactionID() + ";" + tcsIn.getRealTransaction());
        }
    }

    private Map setRemoteManagerRequestParam(Inputs.TCSIn tcsIn){
        this.setTransactionRoute(tcsIn);
        Map param = new HashMap();
        param.put("functionId", tcsIn.getObjCommonIn().getTransactionID());
        param.put("messageBody",JSON.toJSONString(tcsIn));
        param.put("messageId", UUID.randomUUID().toString());
        param.put("sendTime", CimDateUtils.getCurrentDateTimeWithDefault());
        param.put("sendName", tcsIn.getSendName());
        param.put("transactionRoute", tcsIn.getTransactionRoute());
        return param;
    }

    @Override
    public void sendCarrierOutFromIBReq(Infos.ObjCommon strObjCommonIn, User requestUserID, ObjectIdentifier equipmentID, ObjectIdentifier cassetteID, String claimMemo){

    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
      * @param objCommon
     * @param sendDurableOpeStartCancelReqIn
     * @return com.fa.cim.dto.Results.DurableOperationStartCancelReqResult
     * @exception
     * @author ho
     * @date 2020/6/24 15:28
     */
    public Results.DurableOperationStartCancelReqResult sendDurableOpeStartCancelReq(Infos.ObjCommon objCommon, Infos.SendDurableOpeStartCancelReqIn sendDurableOpeStartCancelReqIn){
        // TODO:
        return null;
    }

    @Override
    public void sendDurableControlJobActionReq(Infos.ObjCommon objCommon, Infos.SendDurableControlJobActionReqIn sendDurableControlJobActionReqIn) {

    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
      * @param strObjCommonIn
     * @param strTCSMgr_SendStartDurablesReservationReq_in
     * @return com.fa.cim.common.support.ObjectIdentifier
     * @exception
     * @author ho
     * @date 2020/7/3 14:24
     */
    public ObjectIdentifier sendStartDurablesReservationReq(
            Infos.ObjCommon                                    strObjCommonIn,
            Infos.SendStartDurablesReservationReqIn strTCSMgr_SendStartDurablesReservationReq_in ) {
        //TODO:
        return null;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
      * @param strObjCommonIn
     * @param strTCSMgrSendStartDurablesReservationCancelReqin
     * @return com.fa.cim.dto.Results.StartDurablesReservationCancelReqResult
     * @exception
     * @author ho
     * @date 2020/7/7 13:07
     */
    @Override
    public Results.StartDurablesReservationCancelReqResult sendStartDurablesReservationCancelReq(Infos.ObjCommon strObjCommonIn, Params.StartDurablesReservationCancelReqInParam strTCSMgrSendStartDurablesReservationCancelReqin) {
        //TODO:
        return null;
    }

    @Override
    public void sendReticleStoreCancelReq(TCSReqEnum tcsReqEnum, Inputs.SendReticleStoreCancelReqIn tcsIn) {
        log.info("send to EAP Request Name：{}", null == tcsReqEnum ? null : tcsReqEnum.getValue());
        log.info("send to EAP Request Parameter：{}", null == tcsIn ? null : tcsIn.toString());
        Outputs.TCSOut tcsOut = null;
        //check user first
        //-----------------------------------------
        // [UserName is "EAP"] ---> return RC_OK
        //-----------------------------------------
        if (tcsIn != null && ObjectIdentifier.equalsWithValue(tcsIn.getObjCommonIn().getUser().getUserID(), BizConstant.SP_TCS_PERSON)) {
            return;
        }
        String hostName = null;

        //check call CellController for stk
        Boolean isStorageBool = false;
        Boolean checkFlag = checkCellControllerForStkOrEqp(tcsIn, isStorageBool);
        if (CimBooleanUtils.isFalse(checkFlag)) {
            return;
        }
        Inputs.SendReticleStoreCancelReqIn sendReticleStoreCancelReqIn = (Inputs.SendReticleStoreCancelReqIn) tcsIn;
        if (null != sendReticleStoreCancelReqIn) {
            sendReticleStoreCancelReqIn.setSendName(TCSReqEnum.sendReticleStoreCancelReq.getValue());
            sendReticleStoreCancelReqIn.setRealTransaction(TransactionIDEnum.RETICLE_DISPATCH_JOB_CANCEL_REQ.getValue());
        }
        tcsRemoteManager.sendReticleStoreCancelReq(this.setRemoteManagerRequestParam(sendReticleStoreCancelReqIn));
    }

    @Override
    public void sendReticleRetrieveCancelReq(TCSReqEnum tcsReqEnum, Inputs.SendReticleRetrieveCancelReqIn tcsIn) {
        log.info("send to EAP Request Name：{}", null == tcsReqEnum ? null : tcsReqEnum.getValue());
        log.info("send to EAP Request Parameter：{}", null == tcsIn ? null : tcsIn.toString());
        Outputs.TCSOut tcsOut = null;
        //check user first
        //-----------------------------------------
        // [UserName is "EAP"] ---> return RC_OK
        //-----------------------------------------
        if (tcsIn != null && ObjectIdentifier.equalsWithValue(tcsIn.getObjCommonIn().getUser().getUserID(), BizConstant.SP_TCS_PERSON)) {
            return;
        }
        String hostName = null;

        //check call CellController for stk
        Boolean isStorageBool = false;
        Boolean checkFlag = checkCellControllerForStkOrEqp(tcsIn, isStorageBool);
        if (CimBooleanUtils.isFalse(checkFlag)) {
            return;
        }
        Inputs.SendReticleRetrieveCancelReqIn sendReticleRetrieveCancelReqIn = (Inputs.SendReticleRetrieveCancelReqIn) tcsIn;
        if (null != sendReticleRetrieveCancelReqIn) {
            sendReticleRetrieveCancelReqIn.setSendName(TCSReqEnum.sendReticleRetrieveCancelReq.getValue());
            sendReticleRetrieveCancelReqIn.setRealTransaction(TransactionIDEnum.RETICLE_DISPATCH_JOB_CANCEL_REQ.getValue());
        }
        tcsRemoteManager.sendReticleRetrieveCancelReq(this.setRemoteManagerRequestParam(sendReticleRetrieveCancelReqIn));
    }

    @Override
    public void sendReticlePodUnclampCancelReq(TCSReqEnum tcsReqEnum, Inputs.SendReticlePodUnclampCancelReqIn tcsIn) {
        log.info("send to EAP Request Name：{}", null == tcsReqEnum ? null : tcsReqEnum.getValue());
        log.info("send to EAP Request Parameter：{}", null == tcsIn ? null : tcsIn.toString());
        Outputs.TCSOut tcsOut = null;
        //check user first
        //-----------------------------------------
        // [UserName is "EAP"] ---> return RC_OK
        //-----------------------------------------
        if (tcsIn != null && ObjectIdentifier.equalsWithValue(tcsIn.getObjCommonIn().getUser().getUserID(), BizConstant.SP_TCS_PERSON)) {
            return;
        }
        String hostName = null;

        //check call CellController for stk
        Boolean isStorageBool = false;
        Boolean checkFlag = checkCellControllerForStkOrEqp(tcsIn, isStorageBool);
        if (CimBooleanUtils.isFalse(checkFlag)) {
            return;
        }
        Inputs.SendReticlePodUnclampCancelReqIn sendReticlePodUnclampCancelReqIn = (Inputs.SendReticlePodUnclampCancelReqIn) tcsIn;
        if (null != sendReticlePodUnclampCancelReqIn) {
            sendReticlePodUnclampCancelReqIn.setSendName(TCSReqEnum.sendReticlePodUnclampCancelReq.getValue());
            sendReticlePodUnclampCancelReqIn.setRealTransaction(TransactionIDEnum.RETICLE_DISPATCH_JOB_CANCEL_REQ.getValue());
        }
        tcsRemoteManager.sendReticlePodUnclampCancelReq(this.setRemoteManagerRequestParam(sendReticlePodUnclampCancelReqIn));
    }
}
