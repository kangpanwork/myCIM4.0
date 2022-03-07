package com.fa.cim.service.tms.Impl;

import com.fa.cim.annotaion.OmService;
import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.constant.TCSReqEnum;
import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.OmCode;
import com.fa.cim.common.support.RetCode;
import com.fa.cim.common.utils.*;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.config.RetCodeConfigEx;
import com.fa.cim.dto.*;
import com.fa.cim.idp.tms.api.TmsService;
import com.fa.cim.method.*;
import com.fa.cim.method.impl.TCSMethod;
import com.fa.cim.newcore.bo.durable.CimCassette;
import com.fa.cim.newcore.bo.durable.CimProcessDurable;
import com.fa.cim.newcore.bo.durable.CimReticlePod;
import com.fa.cim.newcore.bo.machine.CimMachine;
import com.fa.cim.newcore.bo.machine.CimStorageMachine;
import com.fa.cim.newcore.bo.product.CimLot;
import com.fa.cim.newcore.impl.bo.env.StandardProperties;
import com.fa.cim.newcore.standard.mchnmngm.Machine;
import com.fa.cim.remote.IEAPRemoteManager;
import com.fa.cim.service.dispatch.IDispatchInqService;
import com.fa.cim.service.dispatch.IDispatchService;
import com.fa.cim.service.durable.IDurableInqService;
import com.fa.cim.service.durable.IDurableService;
import com.fa.cim.service.equipment.IEquipmentInqService;
import com.fa.cim.service.fmc.IFMCService;
import com.fa.cim.service.lot.ILotService;
import com.fa.cim.service.tms.ITransferManagementSystemInqService;
import com.fa.cim.service.tms.ITransferManagementSystemService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * description:
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/9/8                              Neyo                create file
 *
 * @author: Neyo
 * @date: 2020/9/8 17:01
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@OmService
@Slf4j
public class TransferManagementSystemServiceImpl implements ITransferManagementSystemService {
    @Autowired
    private ICassetteMethod cassetteMethod;

    @Autowired
    private IObjectLockMethod objectLockMethod;

    @Autowired
    private RetCodeConfig retCodeConfig;

    @Autowired
    private IEquipmentMethod equipmentMethod;

    @Autowired
    private IStockerMethod stockerComp;

    @Autowired
    private ILotMethod lotMethod;


    @Autowired
    private IEventMethod eventMethod;

    @Autowired
    private TmsService tmsService;

    @Autowired
    private TCSMethod tcsMethod;

    @Autowired
    private IEAPMethod eapMethod;

    @Autowired
    private RetCodeConfigEx retCodeConfigEx;

    @Autowired
    private IWhereNextMethod whereNextTransferEqp;

    @Autowired
    private IFPCMethod fpcMethod;

    @Autowired
    private IStartCassetteMethod startCassetteMethod;

    @Autowired
    private ILotService lotService;

    @Autowired
    private IDispatchService dispatchService;

    @Autowired
    private ITransferManagementSystemInqService transferManagementSystemInqService;

    @Autowired
    private IDispatchInqService dispatchInqService;

    @Autowired
    private IWhatNextMethod whatNextMethod;

    @Autowired
    private IObjectLockMethod lockMethod;

    @Autowired
    private IEquipmentInqService equipmentInqService;

    @Autowired
    private IWhereNextMethod whereNextMethod;

    @Autowired
    private ISLMMethod slmMethod;

    @Autowired
    private IFMCService fmcService;

    @Autowired
    private IControlJobMethod controlJobMethod;

    @Autowired
    private IPortMethod portMethod;

    @Autowired
    private IStockerMethod stockerMethod;

    @Autowired
    private IObjectMethod objectMethod;

    @Autowired
    private IEquipmentForDurableMethod equipmentForDurableMethod;

    @Autowired
    private IDurableService durableService;

    @Autowired
    private IDurableInqService durableInqService;

    @Autowired
    private IDurableMethod durableMethod;


    @Override
    public Results.CarrierReserveCancelReqResult sxCarrierReserveCancelReq(Infos.ObjCommon objCommon, List<Infos.ReserveCancelLotCarrier> reserveCancelLotCarriers, String claimMemo) {
        Results.CarrierReserveCancelReqResult carrierReserveCancelReqResult = new Results.CarrierReserveCancelReqResult();
        int nLen = CimArrayUtils.getSize(reserveCancelLotCarriers);
        // Step1 - object_Lock
        for (int i = 0; i < nLen; i++){
            objectLockMethod.objectLock(objCommon, CimCassette.class, reserveCancelLotCarriers.get(i).getCarrierID());
        }
        /*-----------------------------------------------------------*/
        /* Cancel reservation                                        */
        /*-----------------------------------------------------------*/
        List<Infos.ReserveCancelLot> reserveCancelLots = new ArrayList<>();
        for (int i=0; i<reserveCancelLotCarriers.size(); i++) {
            Infos.ReserveCancelLotCarrier reserveCancelLotCarrier = reserveCancelLotCarriers.get(i);
            Infos.ReserveCancelLot reserveCancelLot = new Infos.ReserveCancelLot(reserveCancelLotCarrier.getLotID(), reserveCancelLotCarrier.getCarrierID(), reserveCancelLotCarrier.getForEquipmentID());
            reserveCancelLots.add(reserveCancelLot);
            //Step2 - cassette_interFabXferState_Get
            String interFabXferState = cassetteMethod.cassetteInterFabXferStateGet(objCommon, reserveCancelLotCarrier.getCarrierID());
            Validations.check(CimStringUtils.equals(BizConstant.SP_INTERFAB_XFERSTATE_TRANSFERRING, interFabXferState),
                    new OmCode(retCodeConfig.getInterfabInvalidCassetteTransferStateForRequest(), reserveCancelLotCarrier.getCarrierID().getValue(), interFabXferState));
        }
        //Step3 - cassette_lot_ReserveCancel
        Outputs.ObjCassetteLotReserveCancelOut objCassetteLotReserveCancelOut = cassetteMethod.cassetteLotReserveCancel(objCommon, reserveCancelLots, claimMemo);
        carrierReserveCancelReqResult.setClaimMemo(objCassetteLotReserveCancelOut.getClaimMemo());
        carrierReserveCancelReqResult.setStrReserveCancelLot(objCassetteLotReserveCancelOut.getStrReserveCancelLot());
        return carrierReserveCancelReqResult;
    }

    @Override
    public Results.CarrierReserveReqResult sxCarrierReserveReq (Infos.ObjCommon objCommonIn, Params.CarrierReserveReqParam params) {
        List<Infos.RsvLotCarrier> rsvLotCarriers =  params.getStrRsvLotCarrier();
        int nLen = CimArrayUtils.getSize(rsvLotCarriers);
        // Step 1 object_lock
        for (int i = 0; i < nLen; i++){
            objectLockMethod.objectLock(objCommonIn, CimCassette.class, rsvLotCarriers.get(i).getCarrierID());
        }
        // Step 2 cassette_interFabXferState_Get
        List<Infos.ReserveLot> reserveLotList = new ArrayList<>();
        for (Infos.RsvLotCarrier rsvLotCarrier : rsvLotCarriers){
            Infos.ReserveLot reserveLot = new Infos.ReserveLot();
            reserveLot.setLotID(rsvLotCarrier.getLotID());
            reserveLot.setCassetteID(rsvLotCarrier.getCarrierID());
            reserveLot.setForEquipmentID(rsvLotCarrier.getForEquipmentID());
            reserveLotList.add(reserveLot);
            String cassetteInterFabTransferState = cassetteMethod.cassetteInterFabXferStateGet(objCommonIn, rsvLotCarrier.getCarrierID());
            Validations.check(CimStringUtils.equals(BizConstant.SP_INTERFAB_XFERSTATE_TRANSFERRING, cassetteInterFabTransferState),
                    new OmCode(retCodeConfig.getInterfabInvalidCassetteTransferStateForRequest(), rsvLotCarrier.getCarrierID().getValue(), cassetteInterFabTransferState));
        }

        // Step 3: cassette_lot_reserve
        Outputs.CassetteLotReserveOut cassetteLotReserveOutRetCode = cassetteMethod.cassetteLotReserve(objCommonIn, reserveLotList, params.getClaimMemo());

        Results.CarrierReserveReqResult carrierReserveReqResult = new Results.CarrierReserveReqResult();
        carrierReserveReqResult.setClaimMemo(cassetteLotReserveOutRetCode.getClaimMemo());
        carrierReserveReqResult.setStrRsvLotCarrier(cassetteLotReserveOutRetCode.getReserveLot());
        return carrierReserveReqResult;
    }

    @Override
    public void sxCarrierTransferJobEndRpt(Infos.ObjCommon objCommon, Params.CarrierTransferJobEndRptParams carrierTransferJobEndRptParams){
        log.debug("sxCarrierTransferJobEndRpt(): enter sxCarrierTransferJobEndRpt");
        // Check length of In-Parameter

        if (carrierTransferJobEndRptParams.getStrXferJob().size()<=0){
            throw new ServiceException(retCodeConfig.getInvalidParameter());
        }
        /*---------------------------*/
        /*   Check Input Parameter   */
        /*---------------------------*/
        int nLen = carrierTransferJobEndRptParams.getStrXferJob().size();
        for (int i=0;i<nLen;i++){
            if (ObjectIdentifier.isEmpty(carrierTransferJobEndRptParams.getStrXferJob().get(i).getToMachineID())){
                throw new ServiceException(retCodeConfig.getInvalidInputParam());
            }
            if (i==0){
                continue;
            }else {
                if (!CimStringUtils.equals(carrierTransferJobEndRptParams.getStrXferJob().get(i).getToMachineID().getValue(),carrierTransferJobEndRptParams.getStrXferJob().get(i-1).getToMachineID().getValue())){
                    throw new ServiceException(retCodeConfig.getInvalidInputParam());
                }

            }

        }
        /*-------------------------*/
        /*   Check EQP MODE        */
        /*-------------------------*/
        Infos.EqpPortInfo eqpPortInfo = equipmentMethod.equipmentPortInfoGet(objCommon,carrierTransferJobEndRptParams.getStrXferJob().get(0).getToMachineID());
        // Check length of Equipment Port
        if (CimArrayUtils.getSize(eqpPortInfo.getEqpPortStatuses())<=0){
            throw new ServiceException(retCodeConfig.getNotFoundPort());
        }
        if (CimStringUtils.equals(BizConstant.SP_EQP_ONLINEMODE_OFFLINE, eqpPortInfo.getEqpPortStatuses().get(0).getOnlineMode())){
            //ok
            return;
        }
        /*-------------------------*/
        /*   Send to EAP Manager   */
        /*-------------------------*/
        Inputs.SendCarrierTransferJobEndRptIn sendCarrierTransferJobEndRpt = new Inputs.SendCarrierTransferJobEndRptIn();
        sendCarrierTransferJobEndRpt.setObjCommonIn(objCommon);
        sendCarrierTransferJobEndRpt.setClaimMemo(carrierTransferJobEndRptParams.getClaimMemo());
        sendCarrierTransferJobEndRpt.setRequestUserID(objCommon.getUser());
        sendCarrierTransferJobEndRpt.setStrXferJob(carrierTransferJobEndRptParams.getStrXferJob());
        sendCarrierTransferJobEndRpt.setEquipmentID(carrierTransferJobEndRptParams.getStrXferJob().get(0).getToMachineID());
        tcsMethod.sendTCSReq(TCSReqEnum.sendCarrierTransferJobEndRpt,sendCarrierTransferJobEndRpt);

    }

    /**
     * txCarrierTransferStatusChangeRpt__160
     * Description:
     * //<Method Summary>
     * // This function changes Carrier Transfer Status.
     * //
     * // You can change Carrier Transfer Status to the followings.
     * //-SI: Station-In       --- Carrier is taken into stocker by AGV.
     * //-SO: Station-Out      --- Carrier is taken out of stocker by AGV.
     * //-BI: Bay-In           --- Carrier is taken into stocker by InterBay.
     * //-BO: Bay-Out          --- Carrier is taken out of stocker by InterBay.
     * //-MI: Manual-In        --- Carrier is taken into stocker manually.
     * //-MO: Manual-Out       --- Carrier is taken out of stocker manually.
     * //-EI: eqp-In     --- Carrier is in eqp.
     * //-EO: eqp-Out    --- Carrier is out of eqp.
     * //-HI: Shelf-In         --- Carrier is in Shelf that belongs to Internal Buffer eqp.
     * //-HO: Shelf-Out        --- Carrier is out of Shelf that belongs to Internal Buffer eqp.
     * //-II: Intermediate-In  --- Carrier is in intermediate stocker.
     * //-IO: Intermediate-Out --- Carrier is out of intermediate stocker.
     * //-AI: Abnormal-In      --- Carrier is in Stoker or eqp without intervention of SiviewSystem.
     * //-AO: Abnormal-Out     --- Carrier is out of stocker or eqp without intervention of SiviewSystem.
     * //
     * //If the Carrier has transfer reservation when an operator takes stocker into Carrier manually, the reservation will be cancelled.
     * //And it depends on Carrier XferStatus whether you can change specified Transfer Status or not.
     * //</Method Summary>
     * //
     * //<MRM>
     * // LGS1-4. Transfer Request
     * //</MRM>
     *
     * @param objCommonIn
     * @param carrierTransferStatusChangeRptParams
     * @return
     */
    @Override
    public Results.CarrierTransferStatusChangeRptResult sxCarrierTransferStatusChangeRpt(Infos.ObjCommon objCommonIn, Params.CarrierTransferStatusChangeRptParams carrierTransferStatusChangeRptParams) {
        log.debug("sxCarrierTransferStatusChangeRpt(): enter sxCarrierTransferStatusChangeRpt");
        Results.CarrierTransferStatusChangeRptResult carrierTransferStatusChangeRptResult = new Results.CarrierTransferStatusChangeRptResult();
        List<Results.XferLot> xferLots = new ArrayList<>();
        carrierTransferStatusChangeRptResult.setStrXferLot(xferLots);

        boolean equipmentFlag = false;
        Outputs.ObjStockerTypeGetDROut stockerTypeGetDROut = null;
        if (ObjectIdentifier.isNotEmptyWithValue(carrierTransferStatusChangeRptParams.getMachineID())) {
            //Get Machine Type;
            try {
                stockerTypeGetDROut = stockerComp.stockerTypeGet(objCommonIn, carrierTransferStatusChangeRptParams.getMachineID());
            }catch (ServiceException ex){
                // equipment_state_GetDR
                ObjectIdentifier equipmentStateDR = null;
                try {
                    equipmentStateDR = equipmentMethod.equipmentStateGetDR(objCommonIn, carrierTransferStatusChangeRptParams.getMachineID());
                    equipmentFlag = true;
                } catch (ServiceException e){
                    returnResult(xferLots, BizConstant.SP_TRANSJOBSTATE_ERRORREPORTED);
                    throw new ServiceException(new OmCode(e.getCode(), e.getMessage()), carrierTransferStatusChangeRptResult);
                }
            }

        } else {
            returnResult(xferLots, BizConstant.SP_TRANSJOBSTATE_ERRORREPORTED);
            throw new ServiceException(retCodeConfig.getInvalidMachineId(), carrierTransferStatusChangeRptResult);
        }

        //Lock objects to be updated;
        String aTransferState = null;
        if (equipmentFlag) {
            aTransferState = carrierTransferStatusChangeRptParams.getXferStatus();
        } else {
            if (CimBooleanUtils.isTrue(carrierTransferStatusChangeRptParams.isManualInFlag())) {
                try{
                    cassetteMethod.cassetteCheckEmpty(carrierTransferStatusChangeRptParams.getCarrierID());
                }catch (ServiceException e) {
                    if (Validations.isEquals(retCodeConfig.getCastNotEmpty(), e.getCode())) {
                        // cassette_GetLotList
                        Infos.LotListInCassetteInfo cassetteLotListGetResult = cassetteMethod.cassetteGetLotList(objCommonIn, carrierTransferStatusChangeRptParams.getCarrierID());
                        List<ObjectIdentifier> lotIDList = cassetteLotListGetResult.getLotIDList();
                        if (CimArrayUtils.getSize(lotIDList) > 0) {
                            for (ObjectIdentifier lotID : lotIDList) {
                                if (lotID == null) {
                                    continue;
                                }
                                // lot_processState_Get
                                String lotProcessState = null;
                                try {
                                    lotProcessState = lotMethod.lotProcessStateGet(objCommonIn, lotID);
                                } catch (ServiceException se){
                                    returnResult(xferLots, BizConstant.SP_TRANSJOBSTATE_ERRORREPORTED);
                                    throw new ServiceException(new OmCode(se.getCode(), se.getMessage()), carrierTransferStatusChangeRptResult);
                                }
                                if (CimStringUtils.equals(BizConstant.SP_LOT_PROCSTATE_PROCESSING, lotProcessState)) {
                                    returnResult(xferLots, BizConstant.SP_TRANSJOBSTATE_ERRORREPORTED);
                                    throw new ServiceException(new OmCode(retCodeConfig.getInvalidLotProcstat(), ObjectIdentifier.fetchValue(lotID), BizConstant.SP_LOT_PROCSTATE_PROCESSING), carrierTransferStatusChangeRptResult);
                                }
                            }
                        }
                    }
                }
            }
        }

        //Lock objects to be updated;
        try {
            objectLockMethod.objectLock(objCommonIn, CimCassette.class, carrierTransferStatusChangeRptParams.getCarrierID());
        } catch (ServiceException e) {
            returnResult(xferLots, BizConstant.SP_TRANSJOBSTATE_ERRORREPORTED);
            throw new ServiceException(new OmCode(e.getCode(), e.getMessage()), carrierTransferStatusChangeRptResult);
        }
        //bug-2901 优化：不依赖于前端参数，直接获取当前时间
        Timestamp recordTimeStamp = CimDateUtils.getCurrentTimeStamp();
        if (!equipmentFlag) {
            if (carrierTransferStatusChangeRptParams.isManualInFlag()) {
                aTransferState = BizConstant.SP_TRANSSTATE_MANUALIN;
            } else {
                //Get Previous transferStatus (cassette_transferState_GetDR);
                String cassetteTransferState = null;
                try {
                    cassetteTransferState = cassetteMethod.cassetteTransferStateGet(objCommonIn, carrierTransferStatusChangeRptParams.getCarrierID());
                } catch (ServiceException e) {
                    returnResult(xferLots, BizConstant.SP_TRANSJOBSTATE_ERRORREPORTED);
                    throw new ServiceException(new OmCode(e.getCode(), e.getMessage()), carrierTransferStatusChangeRptResult);
                }
                String stockerType = stockerTypeGetDROut.getStockerType();
                if(CimStringUtils.equals(stockerType, BizConstant.SP_STOCKER_TYPE_AUTO)){
                    if (CimArrayUtils.binarySearch(new String[]{
                            BizConstant.SP_TRANSSTATE_STATIONIN,
                            BizConstant.SP_TRANSSTATE_STATIONOUT,
                            BizConstant.SP_TRANSSTATE_EQUIPMENTIN,
                            BizConstant.SP_TRANSSTATE_EQUIPMENTOUT,
                            BizConstant.SP_TRANSSTATE_INTERMEDIATEIN,
                            BizConstant.SP_TRANSSTATE_INTERMEDIATEOUT
                    }, cassetteTransferState)) {
                        aTransferState =  BizConstant.SP_TRANSSTATE_STATIONIN;
                    } else if (CimArrayUtils.binarySearch(new String[]{
                            BizConstant.SP_TRANSSTATE_BAYIN,
                            BizConstant.SP_TRANSSTATE_BAYOUT
                    }, cassetteTransferState)) {
                        aTransferState =  BizConstant.SP_TRANSSTATE_BAYIN;
                    } else if (CimArrayUtils.binarySearch(new String[]{
                            BizConstant.SP_TRANSSTATE_MANUALIN,
                            BizConstant.SP_TRANSSTATE_MANUALOUT,
                            BizConstant.SP_TRANSSTATE_SHELFIN,
                            BizConstant.SP_TRANSSTATE_SHELFOUT,
                            BizConstant.SP_TRANSSTATE_ABNORMALOUT,
                            "", null,
                            //CIMStateEnum.SP_TRANS_STATE_UNKNOWN.getValue()
                    }, cassetteTransferState)) {
                        aTransferState =  BizConstant.SP_TRANSSTATE_MANUALIN;
                    } else if (CimStringUtils.equals(cassetteTransferState, BizConstant.SP_TRANSSTATE_ABNORMALIN)) {
                        aTransferState =  BizConstant.SP_TRANSSTATE_ABNORMALIN;
                    }else if (CimStringUtils.equals(cassetteTransferState,BizConstant.SP_UNDEFINED_STATE)){
                        aTransferState = BizConstant.SP_TRANSSTATE_MANUALIN;
                    }
                }else if(CimStringUtils.equals(stockerType, BizConstant.SP_STOCKER_TYPE_SHELF)){
                    aTransferState =  carrierTransferStatusChangeRptParams.getXferStatus();
                }else if(CimStringUtils.equals(stockerType, BizConstant.SP_STOCKER_TYPE_ERACK)){
                    aTransferState =  carrierTransferStatusChangeRptParams.getXferStatus();
                }else if(CimStringUtils.equals(stockerType, BizConstant.SP_STOCKER_TYPE_INTERM)){
                    aTransferState =  BizConstant.SP_TRANSSTATE_INTERMEDIATEIN;
                }else if(CimStringUtils.equals(stockerType, BizConstant.SP_STOCKER_TYPE_RETICLE)){
                    aTransferState =  BizConstant.SP_TRANSSTATE_STATIONIN;
                }else if(CimStringUtils.equals(stockerType, BizConstant.SP_STOCKER_TYPE_FIXTURE)){
                    aTransferState =  BizConstant.SP_TRANSSTATE_STATIONIN;
                }else if(CimStringUtils.equals(stockerType, BizConstant.SP_STOCKER_TYPE_INTERBAY)){
                    aTransferState =  BizConstant.SP_TRANSSTATE_BAYOUT;
                }else if(CimStringUtils.equals(stockerType, BizConstant.SP_STOCKER_TYPE_INTRABAY)){
                    aTransferState = CimStringUtils.equals(carrierTransferStatusChangeRptParams.getXferStatus(), BizConstant.SP_TRANSSTATE_MANUALOUT)
                            ? BizConstant.SP_TRANSSTATE_MANUALOUT : BizConstant.SP_TRANSSTATE_STATIONOUT;
                }else{
                    aTransferState =  BizConstant.SP_TRANSSTATE_MANUALOUT;
                }

                if (CimStringUtils.equals(carrierTransferStatusChangeRptParams.getXferStatus(), BizConstant.SP_TRANSSTATE_MANUALOUT)) {
                    aTransferState = BizConstant.SP_TRANSSTATE_MANUALOUT;
                }
                if (CimStringUtils.equals(carrierTransferStatusChangeRptParams.getXferStatus(), BizConstant.SP_TRANSSTATE_ABNORMALOUT)) {
                    aTransferState = BizConstant.SP_TRANSSTATE_ABNORMALOUT;
                }
            }
        }

        //Transfer State Update Process ;
        Infos.XferCassette xferCassette = new Infos.XferCassette();
        xferCassette.setCassetteID(carrierTransferStatusChangeRptParams.getCarrierID());
        xferCassette.setPortID(carrierTransferStatusChangeRptParams.getPortID());
        xferCassette.setTransferStatus(aTransferState);
        ObjectIdentifier aStockerID, aEquipmentID;
        if (equipmentFlag) {
            aStockerID = null;
            aEquipmentID = carrierTransferStatusChangeRptParams.getMachineID();
        } else {
            aStockerID = carrierTransferStatusChangeRptParams.getMachineID();
            aEquipmentID = null;
        }

        // cassette_transferState_Change
        try {
            cassetteMethod.cassetteTransferStateChange(objCommonIn, aStockerID, aEquipmentID, carrierTransferStatusChangeRptParams.getCarrierID(), xferCassette, recordTimeStamp, carrierTransferStatusChangeRptParams.getShelfPosition());
        } catch (ServiceException e) {
            returnResult(xferLots, aTransferState);
            throw new ServiceException(e, carrierTransferStatusChangeRptResult);
        }
        if (CimStringUtils.equals(BizConstant.SP_TRANSSTATE_MANUALIN, aTransferState)) {
            // cassette_reservedState_Get
            Outputs.ObjCassetteReservedStateGetOut cassetteReservedStateRetCode = null;
            try {
                cassetteReservedStateRetCode = cassetteMethod.cassetteReservedStateGet(objCommonIn, carrierTransferStatusChangeRptParams.getCarrierID());
            } catch (ServiceException e){
                returnResult(xferLots, aTransferState);
                throw new ServiceException(new OmCode(e.getCode(), e.getMessage()), carrierTransferStatusChangeRptResult);
            }
            if (CimBooleanUtils.isTrue(cassetteReservedStateRetCode.isTransferReserved())) {
                List<Infos.ReserveCancelLotCarrier> strRsvCanLotCarrier = new ArrayList<>();
                Infos.ReserveCancelLotCarrier rsvCanLotCarrier = new Infos.ReserveCancelLotCarrier();
                rsvCanLotCarrier.setCarrierID(carrierTransferStatusChangeRptParams.getCarrierID());
                strRsvCanLotCarrier.add(rsvCanLotCarrier);
                if (CimBooleanUtils.isTrue(equipmentFlag)) {
                    rsvCanLotCarrier.setForEquipmentID(carrierTransferStatusChangeRptParams.getMachineID());
                }
                // step9 - call sxCarrierReserveCancelReq
                try {
                    this.sxCarrierReserveCancelReq(objCommonIn, strRsvCanLotCarrier, BizConstant.EMPTY);
                } catch (ServiceException e) {
                    returnResult(xferLots, aTransferState);
                    throw new ServiceException(new OmCode(e.getCode(), e.getMessage()), carrierTransferStatusChangeRptResult);
                }
            }

            // cassette_dispatchState_Get
            Boolean cassetteDispatchState = null;
            try {
                cassetteDispatchState = cassetteMethod.cassetteDispatchStateGet(objCommonIn, carrierTransferStatusChangeRptParams.getCarrierID());
            } catch (ServiceException e){
                returnResult(xferLots, aTransferState);
                throw new ServiceException(new OmCode(e.getCode(), e.getMessage()), carrierTransferStatusChangeRptResult);
            }
            if (CimBooleanUtils.isTrue(cassetteDispatchState)) {
                returnResult(xferLots, aTransferState);
                throw new ServiceException(retCodeConfig.getAlreadyDispatchReservedCassette(), carrierTransferStatusChangeRptResult);
            }
        }

        //durableXferStatusChangeEvent_Make
        Inputs.DurableXferStatusChangeEventMakeParams durableXferStatusChangeEventMakeParams = new Inputs.DurableXferStatusChangeEventMakeParams();
        durableXferStatusChangeEventMakeParams.setTransactionID(TransactionIDEnum.EQP_LOT_CASSETTE_XFER_STATUS_CHANGE_RPT.getValue());
        durableXferStatusChangeEventMakeParams.setDurableID(carrierTransferStatusChangeRptParams.getCarrierID());
        durableXferStatusChangeEventMakeParams.setDurableType(BizConstant.SP_DURABLECAT_CASSETTE);
        durableXferStatusChangeEventMakeParams.setActionCode(BizConstant.SP_DURABLEEVENT_ACTION_XFERSTATECHANGE);
        durableXferStatusChangeEventMakeParams.setDurableStatus("");
        durableXferStatusChangeEventMakeParams.setXferStatus(aTransferState);
        durableXferStatusChangeEventMakeParams.setTransferStatusChangeTimeStamp(CimDateUtils.convertToSpecString(recordTimeStamp));
        durableXferStatusChangeEventMakeParams.setLocation(ObjectIdentifier.fetchValue(carrierTransferStatusChangeRptParams.getMachineID()));
        durableXferStatusChangeEventMakeParams.setClaimMemo(carrierTransferStatusChangeRptParams.getClaimMemo());
        try {
            eventMethod.durableXferStatusChangeEventMake(objCommonIn, durableXferStatusChangeEventMakeParams);
        } catch (ServiceException e) {
            returnResult(xferLots, aTransferState);
            throw new ServiceException(retCodeConfig.getFailMakeHistory(), carrierTransferStatusChangeRptResult);
        }

        Results.XferLot xferLot = new Results.XferLot();
        carrierTransferStatusChangeRptResult.setStockerID(aStockerID);
        carrierTransferStatusChangeRptResult.setEquipmentID(aEquipmentID);
        xferLot.setCassetteID(xferCassette.getCassetteID());
        xferLot.setXPosition(xferCassette.getXPosition());
        xferLot.setYPosition(xferCassette.getYPosition());
        xferLot.setZPosition(xferCassette.getZPosition());
        xferLot.setPortID(xferCassette.getPortID());
        xferLot.setTransferStatus(xferCassette.getTransferStatus());

        xferLots.add(xferLot);

        return carrierTransferStatusChangeRptResult;
    }

    private void returnResult(List<Results.XferLot> xferLots, String xferStatus) {
        Results.XferLot xferLot = new Results.XferLot();
        xferLot.setTransferStatus(xferStatus);
        xferLots.add(xferLot);
    }

    @Override
    public void sxLotCassetteXferDeleteReq(Infos.ObjCommon objCommon, Params.CarrierTransferJobDeleteReqParam params) {

        //init
        List<Infos.DelCarrierJob> strDelCarrierJob = params.getStrDelCarrierJob();
        String jobId = params.getJobId();
        String claimMemo = params.getClaimMemo();

        int castLen = CimArrayUtils.getSize(strDelCarrierJob);
        for (int k = 0; k < castLen; k++) {
            //【step-1】: cassette_transferState_Get
            String cassetteTransferStateGetOutRetCode = cassetteMethod.cassetteTransferStateGet(objCommon, strDelCarrierJob.get(k).getCarrierID());
            if (CimStringUtils.equals(BizConstant.SP_TRANSSTATE_EQUIPMENTIN, cassetteTransferStateGetOutRetCode)) {
                //【step-2】: cassette_equipmentID_Get
                Outputs.ObjCassetteEquipmentIDGetOut cassetteEquipmentIdGetRetCode = cassetteMethod.cassetteEquipmentIDGet(objCommon, strDelCarrierJob.get(k).getCarrierID());

                log.info("equipmentID: {}", cassetteEquipmentIdGetRetCode.getEquipmentID().getValue());
                /**********************************************************/
                /*  Lock All Port Object for internal Buffer Equipment.   */
                /**********************************************************/
                //【step-3】: equipment_portInfo_GetDR
                Infos.EqpPortInfo eqpPortInfo = equipmentMethod.equipmentPortInfoGet(objCommon, cassetteEquipmentIdGetRetCode.getEquipmentID());
                int lenPortInfo = CimArrayUtils.getSize(eqpPortInfo.getEqpPortStatuses());
                for (int j = 0; j < lenPortInfo; j++) {
                    objectLockMethod.objectLockForEquipmentResource(objCommon, cassetteEquipmentIdGetRetCode.getEquipmentID(), eqpPortInfo.getEqpPortStatuses().get(j).getPortID(), BizConstant.SP_CLASSNAME_POSPORTRESOURCE);
                }
            }
        }
        /*----------------------------------------------------*/
        /*   Request to TMS to Request Transfer Job Deletion   */
        /*----------------------------------------------------*/
        int nLen = CimArrayUtils.getSize(strDelCarrierJob);
        Infos.TranJobCancelReq tranJobCancelReq = new Infos.TranJobCancelReq();
        List<Infos.CarrierJob> carrierJobData = new ArrayList<>();
        tranJobCancelReq.setCarrierJobData(carrierJobData);
        tranJobCancelReq.setJobID(jobId);

        Inputs.SendTransportJobCancelReqIn sendTransportJobCancelReqIn = new Inputs.SendTransportJobCancelReqIn();
        for (int i = 0; i < nLen; i++) {
            Infos.CarrierJob carrierJob = new Infos.CarrierJob();
            carrierJobData.add(i, carrierJob);
            carrierJob.setCarrierJobID(strDelCarrierJob.get(i).getCarrierJobID());
            carrierJob.setCarrierID(strDelCarrierJob.get(i).getCarrierID());
            //【step-5】TMSMgr_SendTransportJobCancelReq
            sendTransportJobCancelReqIn.setStrObjCommonIn(objCommon);
            sendTransportJobCancelReqIn.setTranJobCancelReq(tranJobCancelReq);
            sendTransportJobCancelReqIn.setUser(objCommon.getUser());
        }
        Outputs.SendTransportJobCancelReqOut sendTransportJobCancelReqOutRetCode = tmsService.transportJobCancelReq(sendTransportJobCancelReqIn);
        /*---------------------------------*/
        /*   Get Cassette Reserve Status   */
        /*---------------------------------*/
        Outputs.ObjCassetteReservedStateGetOut cassetteReservedStateRetCode = new Outputs.ObjCassetteReservedStateGetOut();
        cassetteReservedStateRetCode.setTransferReserved(false);
        for (int i = 0; i < nLen; i++) {
            //【step-6】cassette_reservedState_Get
            cassetteReservedStateRetCode = cassetteMethod.cassetteReservedStateGet(objCommon, strDelCarrierJob.get(i).getCarrierID());
        }
        if (CimBooleanUtils.isTrue(cassetteReservedStateRetCode.isTransferReserved())) {
            log.info("strCassette_reservedState_Get_out.transferReserved == TRUE");
            /*------------------------------------------*/
            /*   Call txCarrierReserveCancelReq()   */
            /*------------------------------------------*/
            //【step-7】 txCarrierReserveCancelReq
            List<Infos.ReserveCancelLotCarrier> strRsvCanLotCarrier = new ArrayList<>();
            for (int i = 0; i < nLen; i++) {
                Infos.ReserveCancelLotCarrier reserveCancelLotCarrier = new Infos.ReserveCancelLotCarrier();
                strRsvCanLotCarrier.add(i, reserveCancelLotCarrier);
                reserveCancelLotCarrier.setCarrierID(strDelCarrierJob.get(i).getCarrierID());
            }
            this.sxCarrierReserveCancelReq(objCommon, strRsvCanLotCarrier, claimMemo);
        }
        /*---------------------------------*/
        /*   Get Cassette Reserve Status   */
        /*---------------------------------*/
        Boolean cassetteDispatchStateRetCode = false;
        for (int i = 0; i < nLen; i++) {
            //【step-8】cassette_dispatchState_Get
            cassetteDispatchStateRetCode = cassetteMethod.cassetteDispatchStateGet(objCommon, strDelCarrierJob.get(i).getCarrierID());
        }
        if (CimBooleanUtils.isTrue(cassetteDispatchStateRetCode)) {
            /*-------------------------------------------------------*/
            /*  MoveInReserveCancelReq for next equipment     */
            /*-------------------------------------------------------*/
            log.info("strCassette_dispatchState_Get_out.dispatchReservedFlag == TRUE");
            for (int i = 0; i < nLen; i++) {
                /*-----------------------------------*/
                /*  Check cassette transfer status   */
                /*-----------------------------------*/
                log.info("Check cassette transfer status");
                //【step-9】cassette_transferState_Get
                String cassetteTransferStateGetOutRetCode = cassetteMethod.cassetteTransferStateGet(objCommon, strDelCarrierJob.get(i).getCarrierID());
                if (CimStringUtils.equals(BizConstant.SP_TRANSSTATE_EQUIPMENTIN, cassetteTransferStateGetOutRetCode)) {
                    log.info("transferState == EI");
                    /*------------------------------------*/
                    /*   Get Cassette Info in Equipment   */
                    /*------------------------------------*/
                    log.info("Get Cassette Info in Equipment");
                    //【step-10】cassette_equipmentID_Get
                    Outputs.ObjCassetteEquipmentIDGetOut cassetteEquipmentIDGetRetCode = cassetteMethod.cassetteEquipmentIDGet(objCommon, strDelCarrierJob.get(i).getCarrierID());
                    /*-------------------------*/
                    /*   Get Equipment Object  */
                    /*-------------------------*/
                    String equipmentCategory = "";
                    //【step-11】 equipment_brInfo_GetDR__120
                    Infos.EqpBrInfo eqpBrInfo = equipmentMethod.equipmentBRInfoGetDR(objCommon, cassetteEquipmentIDGetRetCode.getEquipmentID());
                    equipmentCategory = eqpBrInfo.getEquipmentCategory();
                    log.info("EQP Category is {}", equipmentCategory);
                    /*-----------------------*/
                    /*   Get Equipment Port  */
                    /*-----------------------*/
                    Infos.EqpPortInfo eqpPortInfo = new Infos.EqpPortInfo();
                    if (CimStringUtils.equals(BizConstant.SP_MC_CATEGORY_INTERNALBUFFER, equipmentCategory)) {
                        //【step-12】 equipment_portInfoForInternalBuffer_GetDR
                        eqpPortInfo = equipmentMethod.equipmentPortInfoForInternalBufferGetDR(objCommon, cassetteEquipmentIDGetRetCode.getEquipmentID());
                    } else {
                        //【step-13】equipment_portInfo_Get
                        eqpPortInfo = equipmentMethod.equipmentPortInfoGet(objCommon, cassetteEquipmentIDGetRetCode.getEquipmentID());
                    }
                    int lenEqpPort = CimArrayUtils.getSize(eqpPortInfo.getEqpPortStatuses());
                    for (int j = 0; j < lenEqpPort; j++) {
                        if (CimStringUtils.equals(strDelCarrierJob.get(i).getCarrierID().getValue(), eqpPortInfo.getEqpPortStatuses().get(j).getLoadedCassetteID().getValue())) {
                            log.info("cassetteID == loadedCassetteID");
                            log.info("strEqpPortStatus[j].portState = {}", eqpPortInfo.getEqpPortStatuses().get(j).getPortState());
                            log.info("strEqpPortStatus[j].dispatchState = {}", eqpPortInfo.getEqpPortStatuses().get(j).getDispatchState());
                            if ((CimStringUtils.equals(BizConstant.SP_PORTRSC_PORTSTATE_UNLOADREQ, eqpPortInfo.getEqpPortStatuses().get(j).getPortState()))
                                    && (CimStringUtils.equals(BizConstant.SP_PORTRSC_DISPATCHSTATE_DISPATCHED, eqpPortInfo.getEqpPortStatuses().get(j).getDispatchState()))) {
                                log.info("(portState == UnloadReq) && (dispatchState == Dispatched)");
                                /*------------------------*/
                                /*   change to Required   */
                                /*------------------------*/
                                //【step-14】equipment_dispatchState_Change
                                equipmentMethod.equipmentDispatchStateChange(objCommon,
                                        cassetteEquipmentIDGetRetCode.getEquipmentID(),
                                        eqpPortInfo.getEqpPortStatuses().get(j).getPortID(),
                                        BizConstant.SP_PORTRSC_DISPATCHSTATE_REQUIRED,
                                        null,
                                        null,
                                        null,
                                        null);
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public Results.CarrierTransferReqResult sxCarrierTransferForIBReq(Infos.ObjCommon objCommonIn, ObjectIdentifier equipmentID) {
        /*----------------*/
        /*   Initialize   */
        /*----------------*/
        Results.CarrierTransferReqResult out = new Results.CarrierTransferReqResult();
        out.setSysMstStockInfoList(new ArrayList<>());

        int i, j;
        int ii, jj;
        log.info("Check Transaction ID and equipment Category combination.");
        //【step1】 - equipment_categoryVsTxID_CheckCombination
        equipmentMethod.equipmentCategoryVsTxIDCheckCombination(objCommonIn, equipmentID);
        /******************************************************************************************/
        /*                                                                                        */
        /*     Pre-Check Process                                                                  */
        /*                                                                                        */
        /******************************************************************************************/

        /*--------------------------------*/
        /*   Get Equipment Infomation     */
        /*--------------------------------*/
        log.info("Get Equipment Infomation");
        //【step2】 - txEqpInfoForIBInq__120
        Params.EqpInfoForIBInqParams param = new Params.EqpInfoForIBInqParams();
        param.setEquipmentID(equipmentID);
        param.setRequestFlagForBasicInfo(true);
        param.setRequestFlagForStatusInfo(true);
        param.setRequestFlagForPMInfo(false);
        param.setRequestFlagForPortInfo(true);
        param.setRequestFlagForChamberInfo(false);
        param.setRequestFlagForInternalBufferInfo(false);
        param.setRequestFlagForStockerInfo(false);
        param.setRequestFlagForInprocessingLotInfo(true);
        param.setRequestFlagForReservedControlJobInfo(false);
        param.setRequestFlagForRSPPortInfo(false);
        Results.EqpInfoForIBInqResult eqpInfoForIBInqResultRetCode = equipmentInqService.sxEqpInfoForIBInq(objCommonIn, param);

        /*-----------------------*/
        /*   Check Online Mode   */
        /*-----------------------*/
        log.info("Check Online Mode");
        int nILen = CimArrayUtils.getSize(eqpInfoForIBInqResultRetCode.getEquipmentPortInfo().getEqpPortStatuses());
        for (i = 0; i < nILen; i++) {
            List<Infos.EqpPortStatus> content = eqpInfoForIBInqResultRetCode.getEquipmentPortInfo().getEqpPortStatuses();
            if (CimStringUtils.equals(BizConstant.SP_EQP_ONLINEMODE_OFFLINE, content.get(i).getOnlineMode())) {
                log.info("strEqpInfoForIBInqResult.equipmentPortInfo.strEqpPortStatus[i].onlineMode == SP_Eqp_OnlineMode_Offline");
                Validations.check(retCodeConfig.getInvalidEquipmentMode(), ObjectIdentifier.fetchValue(equipmentID),
                        content.get(i).getOnlineMode());
            }
        }

        String takeOutXferInNotAvailableState = StandardProperties.OM_XFER_TAKEOUT_IN_NOT_AVAIL_STATE.getValue();
        /*--------------------------------*/
        /*   Check EQP's Available Flag   */
        /*--------------------------------*/
        log.info("Check EQP's Available Flag");
        if (CimBooleanUtils.isFalse(eqpInfoForIBInqResultRetCode.getEquipmentStatusInfo().getEquipmentAvailableFlag())
                && !CimStringUtils.equals(takeOutXferInNotAvailableState, "1")) {
            log.info("TRUE != strEqpInfoForIBInqResult.equipmentStatusInfo.equipmentAvailableFlag");
            throw new ServiceException(new OmCode(retCodeConfig.getInvalidEquipmentStatus(), equipmentID.getValue(), "NotAvailable"));
        }
        /*----------------------------------*/
        /*   Get Eqp Internal Buffer Info   */
        /*----------------------------------*/
        log.info("Get Eqp Internal Buffer Info ");
        //【step3】 - equipment_internalBufferInfo_Get
        List<Infos.EqpInternalBufferInfo> equipmentInternalBufferInfoGetRetCode = equipmentMethod.equipmentInternalBufferInfoGet(objCommonIn, equipmentID);
        /*------------------------*/
        /*   Pick Up Target Port  */
        /*------------------------*/
        log.info("Pick Up Target Port");
        //【step4】 - equipment_targetPort_PickupForInternalBuffer
        Outputs.EquipmentTargetPortPickupOut equipmentTargetPortPickupForInternalBufferRetCode = equipmentMethod.equipmentTargetPortPickupForInternalBuffer(objCommonIn,
                equipmentID,
                equipmentInternalBufferInfoGetRetCode,
                eqpInfoForIBInqResultRetCode.getEquipmentPortInfo().getEqpPortStatuses());
        ObjectIdentifier dummy = new ObjectIdentifier();
        String tmpAPCIFControlStatus = null;
        /******************************************************************************************/
        /*                                                                                        */
        /*     UnloadReq Process                                                                  */
        /*                                                                                        */
        /******************************************************************************************/
        if (CimStringUtils.equals(BizConstant.SP_PORTRSC_PORTSTATE_UNLOADREQ, equipmentTargetPortPickupForInternalBufferRetCode.getTargetPortType())) {
            log.info("targetPortType == SP_PortRsc_PortState_UnloadReq");
            if (CimBooleanUtils.isTrue(eqpInfoForIBInqResultRetCode.getEqpBasicInfoForInternalBuffer().isEqpToEqpTransferFlag())) {
                log.info("UnLoadReq Process RUN!!  EQP -> EQP");
                /*------------------------------*/
                /*   get whereNextTransferEqp   */
                /*------------------------------*/
                log.info("get whereNextTransferEqp");
                Boolean whereNextOKFlag = true;
                Outputs.WhereNextTransferEqpOut whereNextTransferEqpOutRetCode = null;
                try {
                    whereNextTransferEqpOutRetCode = whereNextTransferEqp.whereNextTransferEqp(objCommonIn,
                            equipmentID,
                            equipmentTargetPortPickupForInternalBufferRetCode);
                } catch (ServiceException e) {
                    whereNextOKFlag = false;
                    whereNextTransferEqpOutRetCode = (Outputs.WhereNextTransferEqpOut) e.getData();
                    log.info("EQP to EQP Xfer is interrupted, and change for the Stocker Xfer");
                }

                if (CimBooleanUtils.isTrue(whereNextOKFlag)){
                    Long tmpFPCAdoptFlag = StandardProperties.OM_DOC_ENABLE_FLAG.getLongValue();
                    if (1 == tmpFPCAdoptFlag) {
                        log.info("DOC adopt Flag is ON. New apply FPCInfo.");
                        String exchangeType = BizConstant.SP_FPC_EXCHANGETYPE_STARTRESERVEINFO;
                        List<Infos.StartCassette> exchangeFPCStartCassetteInfoRetCode = fpcMethod.fpcStartCassetteInfoExchange(objCommonIn, exchangeType, equipmentID, whereNextTransferEqpOutRetCode.getStartCassetteList());

                        whereNextTransferEqpOutRetCode.setStartCassetteList(exchangeFPCStartCassetteInfoRetCode);
                    } else {
                        log.info("DOC adopt Flag is OFF.");
                    }
                    log.info("Set processJobExecFlag based on wafer sampling setting. ");
                    Boolean notSendIgnoreMail = false;
                    OmCode startCassetteProcessJobExecFlagSetRc = retCodeConfig.getSucc();
                    Outputs.ObjStartCassetteProcessJobExecFlagSetOut startCassetteProcessJobExecFlagSetOutRetCode = null;
                    try {
                        startCassetteProcessJobExecFlagSetOutRetCode = startCassetteMethod.startCassetteProcessJobExecFlagSet(objCommonIn,
                                whereNextTransferEqpOutRetCode.getStartCassetteList(),
                                equipmentID);
                    } catch (ServiceException ex) {
                        startCassetteProcessJobExecFlagSetOutRetCode = ex.getData(Outputs.ObjStartCassetteProcessJobExecFlagSetOut.class);
                        if (!Validations.isEquals(retCodeConfig.getNoSmplSetting(), ex.getCode())) {
                            // When errors have occurred, hold add the error lot and send e-mail to notify that.
                            notSendIgnoreMail = true;
                            startCassetteProcessJobExecFlagSetRc = retCodeConfig.getInvalidSmplSetting();
                        }
                    }
                    int mailLen = CimArrayUtils.getSize(startCassetteProcessJobExecFlagSetOutRetCode.getSamplingMessage());
                    //hold lots and send E-mails if error
                    for (int mailCnt = 0; mailCnt < mailLen; mailCnt++) {
                        if (BizConstant.SP_SAMPLING_ERROR_MAIL == startCassetteProcessJobExecFlagSetOutRetCode.getSamplingMessage().get(mailCnt).getMessageType()
                                || BizConstant.SP_SAMPLING_WARN_MAIL == startCassetteProcessJobExecFlagSetOutRetCode.getSamplingMessage().get(mailCnt).getMessageType()) {
                            log.info("messageType == SP_Sampling_error_mail || SP_Sampling_warn_mail ");
                            // Lot Hold
                            //【step8】 - lot_currentOperationInfo_Get
                            Outputs.ObjLotCurrentOperationInfoGetOut lotCurrentOperationInfoRetCode = lotMethod.lotCurrentOperationInfoGet(objCommonIn, startCassetteProcessJobExecFlagSetOutRetCode.getSamplingMessage().get(mailCnt).getLotID());

                            //【step9】 - txHoldLotReq
                            Infos.LotHoldReq lotHoldReq = new Infos.LotHoldReq();
                            lotHoldReq.setHoldType(BizConstant.SP_HOLDTYPE_LOTHOLD);
                            lotHoldReq.setHoldReasonCodeID(new ObjectIdentifier(BizConstant.SP_REASON_WAFERSAMPLINGHOLD));
                            lotHoldReq.setHoldUserID(objCommonIn.getUser().getUserID());
                            lotHoldReq.setResponsibleOperationMark(BizConstant.SP_RESPONSIBLEOPERATION_CURRENT);
                            lotHoldReq.setRouteID(lotCurrentOperationInfoRetCode.getRouteID());
                            lotHoldReq.setOperationNumber(lotCurrentOperationInfoRetCode.getOperationNumber());
                            lotHoldReq.setClaimMemo("");
                            List<Infos.LotHoldReq> holdReqList = new ArrayList<Infos.LotHoldReq>();
                            holdReqList.add(lotHoldReq);
                            lotService.sxHoldLotReq(objCommonIn, startCassetteProcessJobExecFlagSetOutRetCode.getSamplingMessage().get(mailCnt).getLotID(), holdReqList);
                        }
                        //error Mail Set
                        if (CimBooleanUtils.isFalse(notSendIgnoreMail) || BizConstant.SP_SAMPLING_IGNORED_MAIL != startCassetteProcessJobExecFlagSetOutRetCode.getSamplingMessage().get(mailCnt).getMessageType()) {
                            log.info("notSendIgnoreMail == FALSE ||  strStartCassette_processJobExecFlag_Set_out__090.strSamplingMessage[mailCnt].messageType != SP_Sampling_ignored_mail");
                            //When notSendIgnoreMail == TRUE, just the Lot Hold mail is to be sent.
                            //【step10】 - lot_samplingMessage_Create
                            String messageText = "";
                            if (BizConstant.SP_SAMPLING_WARN_MAIL == startCassetteProcessJobExecFlagSetOutRetCode.getSamplingMessage().get(mailCnt).getMessageType()) {
                                //-------------------------------------------------------------------
                                // create mail text if message type == SP_Sampling_warn_mail;
                                // startCassette_processJobExecFlag_Set__090 returns just error text. Caller should switch text handling.
                                //-------------------------------------------------------------------
                                messageText = lotMethod.lotSamplingMessageCreate(objCommonIn,
                                        startCassetteProcessJobExecFlagSetOutRetCode.getSamplingMessage().get(mailCnt).getLotID(),
                                        BizConstant.SP_SAMPLING_ERROR_MAIL,
                                        startCassetteProcessJobExecFlagSetOutRetCode.getSamplingMessage().get(mailCnt).getMessageText());
                            } else {
                                messageText = startCassetteProcessJobExecFlagSetOutRetCode.getSamplingMessage().get(mailCnt).getMessageText();
                            }
                            //Set System Message
                            Infos.SysMsgStockInfo sysMsg = new Infos.SysMsgStockInfo();
                            sysMsg.setSubSystemID(BizConstant.SP_SUBSYSTEMID_MM);
                            sysMsg.setSystemMessageCode(BizConstant.SP_SYSTEMMSGCODE_SMPLERR);
                            sysMsg.setSystemMessageText(messageText);
                            sysMsg.setNotifyFlag(true);
                            sysMsg.setEquipmentID(whereNextTransferEqpOutRetCode.getEquipmentID());
                            sysMsg.setEquipmentStatus("");
                            sysMsg.setStockerID(dummy);
                            sysMsg.setStockerStatus("");
                            sysMsg.setAGVID(dummy);
                            sysMsg.setAGVStatus("");
                            sysMsg.setLotID(startCassetteProcessJobExecFlagSetOutRetCode.getSamplingMessage().get(mailCnt).getLotID());
                            sysMsg.setLotStatus("");
                            sysMsg.setRouteID(dummy);
                            sysMsg.setOperationID(dummy);
                            sysMsg.setOperationNumber("");
                            sysMsg.setSystemMessageTimeStamp(CimDateUtils.convertToSpecString(objCommonIn.getTimeStamp().getReportTimeStamp()));
                            List<Infos.SysMsgStockInfo> sysMstStockInfoList = new ArrayList<>();
                            sysMstStockInfoList.add(sysMsg);
                            out.setSysMstStockInfoList(sysMstStockInfoList);
                        }
                    }
                    if (CimBooleanUtils.isTrue(notSendIgnoreMail)) {
                        log.info("notSendIgnoreMail == TRUE  return this TX.");
                        if (Validations.isEquals(retCodeConfig.getSucc(),startCassetteProcessJobExecFlagSetRc)){
                            return out;
                        }else {
                            throw new ServiceException(startCassetteProcessJobExecFlagSetRc,out);
                        }
                    }
                    //valid check of processJobExecFlag for each lot.
                    //Check every lot has at least one wafer with processJobExecFlag == TRUE
                    //【step11】- lot_processJobExecFlag_ValidCheck
                    Outputs.ObjLotProcessJobExecFlagValidCheckOut lotProcessJobExecFlagValidCheckOut = null;
                    try {
                        lotProcessJobExecFlagValidCheckOut = lotMethod.lotProcessJobExecFlagValidCheck(objCommonIn, startCassetteProcessJobExecFlagSetOutRetCode.getStartCassettes());
                    } catch (ServiceException e) {
                        if (!Validations.isEquals(retCodeConfig.getNoProcessJobExecFlag(), e.getCode())) {
                            log.info("lot_processJobExecFlag_ValidCheck() != RC_OK && != RC_NO_PROCESSJOBEXECFLAG");
                            e.setData(out);
                            throw e;
                        }
                        lotProcessJobExecFlagValidCheckOut = e.getData(Outputs.ObjLotProcessJobExecFlagValidCheckOut.class);
                        if (Validations.isEquals(retCodeConfig.getNoProcessJobExecFlag(), e.getCode())) {
                            log.info("objLot_processJobExecFlag_ValidCheck() rc == RC_NO_PROCESSJOBEXECFLAG");
                            // Lot Hold + error mail
                            // create hold mail message
                            //【step12】 - lot_samplingMessage_Create
                            String lotSamplingMessageCreateRetCode = null;
                            try {
                                lotSamplingMessageCreateRetCode = lotMethod.lotSamplingMessageCreate(objCommonIn,
                                        lotProcessJobExecFlagValidCheckOut.getLotID(),
                                        BizConstant.SP_SAMPLING_ERROR_MAIL,
                                        e.getMessage());
                            } catch (ServiceException ex) {
                                ex.setData(out);
                                throw ex;
                            }

                            //【step13】 - lot_currentOperationInfo_Get
                            Outputs.ObjLotCurrentOperationInfoGetOut lotCurrentOperationInfo = null;
                            try {
                                lotCurrentOperationInfo = lotMethod.lotCurrentOperationInfoGet(objCommonIn, lotProcessJobExecFlagValidCheckOut.getLotID());
                            } catch (ServiceException ex) {
                                ex.setData(out);
                                throw ex;
                            }

                            Infos.LotHoldReq lotHoldReq = new Infos.LotHoldReq();
                            lotHoldReq.setHoldType(BizConstant.SP_HOLDTYPE_LOTHOLD);
                            lotHoldReq.setHoldReasonCodeID(new ObjectIdentifier(BizConstant.SP_REASON_WAFERSAMPLINGHOLD));
                            lotHoldReq.setHoldUserID(objCommonIn.getUser().getUserID());
                            lotHoldReq.setResponsibleOperationMark(BizConstant.SP_RESPONSIBLEOPERATION_CURRENT);
                            lotHoldReq.setRouteID(lotCurrentOperationInfo.getRouteID());
                            lotHoldReq.setOperationNumber(lotCurrentOperationInfo.getOperationNumber());
                            lotHoldReq.setClaimMemo("");
                            List<Infos.LotHoldReq> holdReqList = new ArrayList<Infos.LotHoldReq>();
                            holdReqList.add(lotHoldReq);
                            //【step14】 - txHoldLotReq
                            try {
                                lotService.sxHoldLotReq(objCommonIn, lotProcessJobExecFlagValidCheckOut.getLotID(), holdReqList);
                            } catch (ServiceException ex) {
                                ex.setData(out);
                                throw ex;
                            }
                            //error Mail Set
                            Infos.SysMsgStockInfo sysMsg = new Infos.SysMsgStockInfo();
                            sysMsg.setSubSystemID(BizConstant.SP_SUBSYSTEMID_MM);
                            sysMsg.setSystemMessageCode(BizConstant.SP_SYSTEMMSGCODE_SMPLERR);
                            sysMsg.setSystemMessageText(lotSamplingMessageCreateRetCode);
                            sysMsg.setNotifyFlag(true);
                            sysMsg.setEquipmentID(whereNextTransferEqpOutRetCode.getEquipmentID());
                            sysMsg.setEquipmentStatus("");
                            sysMsg.setStockerID(dummy);
                            sysMsg.setStockerStatus("");
                            sysMsg.setAGVID(dummy);
                            sysMsg.setAGVStatus("");
                            sysMsg.setLotID(lotProcessJobExecFlagValidCheckOut.getLotID());
                            sysMsg.setLotStatus("");
                            sysMsg.setRouteID(dummy);
                            sysMsg.setOperationID(dummy);
                            sysMsg.setOperationNumber("");
                            sysMsg.setSystemMessageTimeStamp(CimDateUtils.convertToSpecString(objCommonIn.getTimeStamp().getReportTimeStamp()));
                            List<Infos.SysMsgStockInfo> sysMstStockInfoList = CimArrayUtils.getSize(out.getSysMstStockInfoList()) == 0 ? new ArrayList<>() : out.getSysMstStockInfoList();
                            sysMstStockInfoList.add(sysMsg);
                            out.setSysMstStockInfoList(sysMstStockInfoList);
                            throw new ServiceException(retCodeConfig.getNoProcessJobExecFlag(), out);
                        }
                    }
                    whereNextTransferEqpOutRetCode.setStartCassetteList(lotProcessJobExecFlagValidCheckOut.getStartCassetteList());
                    log.info("Set processJobExecFlag based on wafer sampling setting end. ");
                    /*-----------------------------------------------------*/
                    /*   Request to Start Lot Reservation                  */
                    /*                                                     */
                    /*   Not Support Xfer EQP -> EQP of InternalBuffer!!   */
                    /*   Therefore, call Normal Reservation.               */
                    /*-----------------------------------------------------*/
                    //【step15】 - txMoveInReserveReq
                    Infos.ObjCommon strTmpObjCommon = objCommonIn;
                    strTmpObjCommon.setTransactionID("ODISW001");
                    Params.MoveInReserveReqParams params = new Params.MoveInReserveReqParams();
                    params.setEquipmentID(whereNextTransferEqpOutRetCode.getEquipmentID());
                    params.setPortGroupID(whereNextTransferEqpOutRetCode.getPortGroup());
                    params.setControlJobID(dummy);
                    params.setStartCassetteList(whereNextTransferEqpOutRetCode.getStartCassetteList());
                    params.setApcIFControlStatus(tmpAPCIFControlStatus);
                    Results.MoveInReserveReqResult moveInReserveReqResultRetCode = null;
                    try {
                        moveInReserveReqResultRetCode = (Results.MoveInReserveReqResult) dispatchService.sxMoveInReserveReq(strTmpObjCommon, params);
                    } catch (ServiceException ex) {
                        log.info("txMoveInReserveReq() rc != RC_OK");
                        //---------------------------------------------------------------------------------------------
                        //  Prepare message text string for case of failure of start lot reservation.
                        //  If start lot reservation for equipment is fail, this messageText is filled.
                        //---------------------------------------------------------------------------------------------
                        //Prepare e-mail Message Text
                        StringBuilder msg = new StringBuilder("<<< MoveInReservation Error!  (EQP -> EQP) >>>\n");
                        msg.append("\n")
                                .append("Equipment ID         : ").append(ObjectIdentifier.fetchValue(whereNextTransferEqpOutRetCode.getEquipmentID())).append("\n")
                                .append("Lot IDs              : ");
                        List<Infos.StartCassette> startCassetteList = whereNextTransferEqpOutRetCode.getStartCassetteList();
                        int nCastCnt = CimArrayUtils.getSize(startCassetteList);
                        boolean bLotSet = false;
                        for (i = 0; i < nCastCnt; i++) {
                            List<Infos.LotInCassette> lotInCassetteList = startCassetteList.get(i).getLotInCassetteList();
                            int nLotCnt = CimArrayUtils.getSize(lotInCassetteList);
                            for (j = 0; j < nLotCnt; j++) {
                                if (!ObjectIdentifier.isEmptyWithValue(lotInCassetteList.get(j).getLotID())){
                                    if (CimBooleanUtils.isTrue(bLotSet)){
                                        msg.append(", ");
                                    }
                                    msg.append(ObjectIdentifier.fetchValue(lotInCassetteList.get(j).getLotID()));
                                    bLotSet = true;
                                }
                            }
                        }

                        msg.append("\n")
                                .append("Transaction ID       : ").append(ex.getTransactionID()).append("\n")
                                .append("Return Code          : ").append(ex.getCode()).append("\n")
                                .append("Message Text         : ").append(ex.getMessage()).append("\n")
                                .append("Reason Text          : ").append(ex.getReasonText()).append("\n");
                        Infos.SysMsgStockInfo sysMsg = new Infos.SysMsgStockInfo();
                        sysMsg.setSubSystemID(BizConstant.SP_SUBSYSTEMID_MM);
                        sysMsg.setSystemMessageCode(BizConstant.SP_SYSTEMMSGCODE_DELIVERYERROR);
                        sysMsg.setSystemMessageText(msg.toString());
                        sysMsg.setNotifyFlag(true);
                        sysMsg.setEquipmentID(equipmentID);
                        sysMsg.setEquipmentStatus("");
                        sysMsg.setStockerID(dummy);
                        sysMsg.setStockerStatus("");
                        sysMsg.setAGVID(dummy);
                        sysMsg.setAGVStatus("");
                        sysMsg.setLotID(dummy);
                        sysMsg.setLotStatus("");
                        sysMsg.setRouteID(dummy);
                        sysMsg.setOperationID(dummy);
                        sysMsg.setOperationNumber("");
                        sysMsg.setSystemMessageTimeStamp(CimDateUtils.convertToSpecString(objCommonIn.getTimeStamp().getReportTimeStamp()));
                        List<Infos.SysMsgStockInfo> sysMstStockInfoList = CimArrayUtils.getSize(out.getSysMstStockInfoList()) == 0 ? new ArrayList<>() : out.getSysMstStockInfoList();
                        sysMstStockInfoList.add(sysMsg);
                        out.setSysMstStockInfoList(sysMstStockInfoList);
                        ex.setData(out);
                        throw ex;
                    }
                    log.info("txMoveInReserveReq() == RC_OK");
                    //Make txMultipleCarrierTransferReq parameter
                    //【step16】 - multiCarrierXferFillInOTMSW005InParm
                    Outputs.ObjMultiCarrierXferFillInOTMSW005InParmOut multiCarrierXferFillInOTMSW005InParmRetCode = null;
                    try {
                        multiCarrierXferFillInOTMSW005InParmRetCode = cassetteMethod.multiCarrierXferFillInOTMSW005InParm(objCommonIn,
                                whereNextTransferEqpOutRetCode.getEquipmentID(), whereNextTransferEqpOutRetCode.getStartCassetteList());
                    } catch (ServiceException e) {
                        log.info("multiCarrierXferFillInOTMSW005InParm() rc != RC_OK");
                        //Request to Start Lot Reservation Cancel
                        // If process EqpID is passed here, it becomes an error by CategoryCheck.
                        // Because, this method is InternalBuffer function.
                        // So, transactionID change to StartReserveCancel of FixedBufferEqp.
                        // Now, EQPID of InternalBuffer does not come here.
                        // Because, CassetteDelivery cannot deliver to InternalBufferEqp in EqpToEqp.
                        //【step17】 - txMoveInReserveCancelReq
                        Infos.ObjCommon strTmpCommonIn = new Infos.ObjCommon();
                        strTmpCommonIn = objCommonIn;
                        strTmpCommonIn.setTransactionID("ODISW002");
                        Params.MoveInReserveCancelReqParams in = new Params.MoveInReserveCancelReqParams();
                        in.setEquipmentID(whereNextTransferEqpOutRetCode.getEquipmentID());
                        in.setControlJobID(moveInReserveReqResultRetCode.getControlJobID());
                        try {
                            Results.MoveInReserveCancelReqResult moveInReserveCancelReqResultRetCode = dispatchService.sxMoveInReserveCancelReqService(strTmpCommonIn, in);
                        } catch (ServiceException ex) {
                            log.info("txMoveInReserveCancelReq != RC_OK");
                        }
                        e.setData(out);
                        throw e;
                    }

                    // Debug Trace of TransferInfo
                    /*-----------------------------------------------*/
                    /*   Send Transfer Request to XM. (EQP -> EQP)   */
                    /*-----------------------------------------------*/
                    log.info("Send Transfer Request to TMS. (EQP -> EQP)");
                    //【step18】 - txMultipleCarrierTransferReq
                    try {
                        this.sxMultipleCarrierTransferReq(objCommonIn,
                                false, "S", multiCarrierXferFillInOTMSW005InParmRetCode.getStrCarrierXferReq());
                    } catch (ServiceException e) {
                        log.info("txMultipleCarrierTransferReq() rc != RC_OK");
                        //Prepare e-mail Message Text
                        StringBuilder msg = new StringBuilder("<<< Transfer Error!  (EQP -> EQP) >>>\n");
                        msg.append("\n")
                                .append("Equipment ID         : ").append(ObjectIdentifier.fetchValue(equipmentID)).append("\n");
                        List<Infos.CarrierXferReq> strCarrierXferReq = multiCarrierXferFillInOTMSW005InParmRetCode.getStrCarrierXferReq();
                        int lenCarrierXfer = CimArrayUtils.getSize(strCarrierXferReq);
                        for (ii = 0; ii < lenCarrierXfer; ii++) {
                            Infos.CarrierXferReq carrierXferReq = strCarrierXferReq.get(ii);
                            msg.append("-----------------------------------------\n")
                                    .append("carrier ID           : ").append(ObjectIdentifier.fetchValue(carrierXferReq.getCarrierID())).append("\n")
                                    .append("Lot ID               : ").append(ObjectIdentifier.fetchValue(carrierXferReq.getFromMachineID())).append("\n")
                                    .append("From Port ID         : ").append(ObjectIdentifier.fetchValue(carrierXferReq.getFromPortID())).append("\n")
                                    .append("To Stocker Group     : ").append(carrierXferReq.getToStockerGroup()).append("\n");
                            int toMachineLen = CimArrayUtils.getSize(carrierXferReq.getStrToMachine());
                            if (toMachineLen == 0){
                                msg.append("To Machine ID        : Nothing\n");
                            }
                            for (jj = 0; jj < toMachineLen; jj++) {
                                Infos.ToMachine toMachine = carrierXferReq.getStrToMachine().get(jj);
                                msg.append("To Machine ID        : ").append(ObjectIdentifier.fetchValue(toMachine.getToMachineID())).append("\n")
                                        .append("To Port ID           : ").append(ObjectIdentifier.fetchValue(toMachine.getToPortID())).append("\n");
                            }
                        }
                        msg.append("-----------------------------------------\n")
                                .append("Transaction ID       : ").append(e.getTransactionID()).append("\n")
                                .append("Return Code          : ").append(e.getCode()).append("\n")
                                .append("Message Text         : ").append(e.getMessage()).append("\n")
                                .append("Reason Text          : ").append(e.getReasonText()).append("\n");
                        //Set System Message
                        Infos.SysMsgStockInfo sysMsg = new Infos.SysMsgStockInfo();
                        sysMsg.setSubSystemID(BizConstant.SP_SUBSYSTEMID_MM);
                        sysMsg.setSystemMessageCode(BizConstant.SP_SYSTEMMSGCODE_DELIVERYERROR);
                        sysMsg.setSystemMessageText(msg.toString());
                        sysMsg.setNotifyFlag(true);
                        sysMsg.setEquipmentID(equipmentID);
                        sysMsg.setEquipmentStatus("");
                        sysMsg.setStockerID(dummy);
                        sysMsg.setStockerStatus("");
                        sysMsg.setAGVID(dummy);
                        sysMsg.setAGVStatus("");
                        sysMsg.setLotID(dummy);
                        sysMsg.setLotStatus("");
                        sysMsg.setRouteID(dummy);
                        sysMsg.setOperationID(dummy);
                        sysMsg.setOperationNumber("");
                        sysMsg.setSystemMessageTimeStamp(CimDateUtils.convertToSpecString(objCommonIn.getTimeStamp().getReportTimeStamp()));
                        List<Infos.SysMsgStockInfo> sysMstStockInfoList = CimArrayUtils.getSize(out.getSysMstStockInfoList()) == 0 ? new ArrayList<>() : out.getSysMstStockInfoList();
                        sysMstStockInfoList.add(sysMsg);
                        out.setSysMstStockInfoList(sysMstStockInfoList);
                        /*---------------------------------------------*/
                        /*   Request to Start Lot Reservation Cancel   */
                        /*---------------------------------------------*/
                        //【step19】 - txMoveInReserveCancelReq
                        // If process EqpID is passed here, it becomes an error by CategoryCheck.
                        // Because, this method is InternalBuffer function.
                        // So, transactionID change to StartReserveCancel of FixedBufferEqp.
                        // Now, EQPID of InternalBuffer does not come here.
                        // Because, CassetteDelivery cannot deliver to InternalBufferEqp in EqpToEqp.
                        Infos.ObjCommon strTmpCommonIn = new Infos.ObjCommon();
                        strTmpCommonIn = objCommonIn;
                        strTmpCommonIn.setTransactionID("ODISW002");
                        Params.MoveInReserveCancelReqParams moveInReserveCancelReqParams = new Params.MoveInReserveCancelReqParams();
                        moveInReserveCancelReqParams.setEquipmentID(whereNextTransferEqpOutRetCode.getEquipmentID());
                        moveInReserveCancelReqParams.setControlJobID(moveInReserveReqResultRetCode.getControlJobID());
                        try {
                            Results.MoveInReserveCancelReqResult moveInReserveCancelReqResultRetCode = dispatchService.sxMoveInReserveCancelReqService(strTmpCommonIn, moveInReserveCancelReqParams);
                        } catch (ServiceException ex) {
                            log.info("txMoveInReserveCancelReq != RC_OK");
                        }
                        e.setData(out);
                        throw e;
                    }

                    /*--------------------*/
                    /*   Return to Main   */
                    /*--------------------*/
                    log.info("OK! UnloadReq [EQP to EQP]  Return to Main");
                    return out;
                }
            }
            log.info("UnLoadReq Process RUN!!");
            List<Infos.PortID> strPortIDs = equipmentTargetPortPickupForInternalBufferRetCode.getEqpTargetPortInfo().getPortGroups().get(0).getStrPortID();
            int nPortLen = CimArrayUtils.getSize(strPortIDs);
            for (i = 0; i < nPortLen; i++) {
                /*----------------------------*/
                /*   Get Where Next Stocker   */
                /*----------------------------*/
                Results.WhereNextStockerInqResult strWhereNextStockerInqResult = null;
                /*------------------------*/
                /*   Call RTD Interface   */
                /*------------------------*/
                log.info("Call RTD Interface");
                //todo【step20】 - cassetteDelivery_RTDInterfaceReq
                Outputs.ObjCassetteDeliveryRTDInterfaceReqOut cassetteDeliveryRTDInterfaceReqOutRetCode = null;
                try {
                    cassetteDeliveryRTDInterfaceReqOutRetCode = cassetteMethod.cassetteDeliveryRTDInterfaceReq(objCommonIn,
                            BizConstant.SP_RTD_FUNCTION_CODE_WHERENEXT, strPortIDs.get(i).getCassetteID());
                    strWhereNextStockerInqResult = cassetteDeliveryRTDInterfaceReqOutRetCode.getStrWhereNextStockerInqResult();
                } catch (ServiceException e) {
                    log.info("Faild RTD Interface -----> Call Normal Function (txWhereNextStockerInq)");
                    if (!Validations.isEquals(retCodeConfigEx.getRtdInterfaceSwitchOff(), e.getCode())
                            && !Validations.isEquals(retCodeConfigEx.getNotFoundRTD(), e.getCode())) {
                        StringBuilder msg = new StringBuilder("<<< RTD Interface Error!  (WhereNext of CassetteDelivery) >>>\n");
                        msg.append("\n")
                                .append("Equipment ID         : ").append(ObjectIdentifier.fetchValue(equipmentID)).append("\n")
                                .append("-----------------------------------------\n")
                                .append("Transaction ID       : ").append(e.getTransactionID()).append("\n")
                                .append("Return Code          : ").append(e.getCode()).append("\n")
                                .append("Message Text         : ").append(e.getMessage()).append("\n")
                                .append("Reason Text          : ").append(e.getReasonText()).append("\n");
                        //Set System Message
                        Infos.SysMsgStockInfo sysMsg = new Infos.SysMsgStockInfo();
                        sysMsg.setSubSystemID(BizConstant.SP_SUBSYSTEMID_MM);
                        sysMsg.setSystemMessageCode(BizConstant.SP_SYSTEMMSGCODE_RTDERROR);
                        sysMsg.setSystemMessageText(msg.toString());
                        sysMsg.setNotifyFlag(true);
                        sysMsg.setEquipmentID(equipmentID);
                        sysMsg.setEquipmentStatus("");
                        sysMsg.setStockerID(dummy);
                        sysMsg.setStockerStatus("");
                        sysMsg.setAGVID(dummy);
                        sysMsg.setAGVStatus("");
                        sysMsg.setLotID(dummy);
                        sysMsg.setLotStatus("");
                        sysMsg.setRouteID(dummy);
                        sysMsg.setOperationID(dummy);
                        sysMsg.setOperationNumber("");
                        sysMsg.setSystemMessageTimeStamp(CimDateUtils.convertToSpecString(objCommonIn.getTimeStamp().getReportTimeStamp()));
                        List<Infos.SysMsgStockInfo> sysMstStockInfoList = CimArrayUtils.getSize(out.getSysMstStockInfoList()) == 0 ? new ArrayList<>() : out.getSysMstStockInfoList();
                        sysMstStockInfoList.add(sysMsg);
                        out.setSysMstStockInfoList(sysMstStockInfoList);
                    }
                    /*---------------------------------------------------*/
                    /*   Call Normal Function (txWhereNextStockerInq)   */
                    /*---------------------------------------------------*/
                    log.info("Call Normal Function (txWhereNextStockerInq)");
                    //【step21】 - txWhereNextStockerInq
                    try {
                        strWhereNextStockerInqResult = transferManagementSystemInqService.sxWhereNextStockerInq(objCommonIn,
                                dummy,
                                strPortIDs.get(i).getCassetteID());
                    } catch (ServiceException ex) {
                        StringBuilder msg = new StringBuilder("<<< WhereNext Error!  (EQP -> Stocker) >>>\n");
                        msg.append("\n")
                                .append("Equipment ID         : ").append(ObjectIdentifier.fetchValue(equipmentID)).append("\n")
                                .append("-----------------------------------------\n")
                                .append("Transaction ID       : ").append(ex.getTransactionID()).append("\n")
                                .append("Return Code          : ").append(ex.getCode()).append("\n")
                                .append("Message Text         : ").append(ex.getMessage()).append("\n")
                                .append("Reason Text          : ").append(ex.getReasonText()).append("\n");
                        //Set System Message
                        Infos.SysMsgStockInfo sysMsg = new Infos.SysMsgStockInfo();
                        sysMsg.setSubSystemID(BizConstant.SP_SUBSYSTEMID_MM);
                        sysMsg.setSystemMessageCode(BizConstant.SP_SYSTEMMSGCODE_DELIVERYERROR);
                        sysMsg.setSystemMessageText(msg.toString());
                        sysMsg.setNotifyFlag(true);
                        sysMsg.setEquipmentID(equipmentID);
                        sysMsg.setEquipmentStatus("");
                        sysMsg.setStockerID(dummy);
                        sysMsg.setStockerStatus("");
                        sysMsg.setAGVID(dummy);
                        sysMsg.setAGVStatus("");
                        sysMsg.setLotID(dummy);
                        sysMsg.setLotStatus("");
                        sysMsg.setRouteID(dummy);
                        sysMsg.setOperationID(dummy);
                        sysMsg.setOperationNumber("");
                        sysMsg.setSystemMessageTimeStamp(CimDateUtils.convertToSpecString(objCommonIn.getTimeStamp().getReportTimeStamp()));
                        List<Infos.SysMsgStockInfo> sysMstStockInfoList = CimArrayUtils.getSize(out.getSysMstStockInfoList()) == 0 ? new ArrayList<>() : out.getSysMstStockInfoList();
                        sysMstStockInfoList.add(sysMsg);
                        out.setSysMstStockInfoList(sysMstStockInfoList);
                        ex.setData(out);
                        throw ex;
                    }
                }
                /*-------------------------------------------*/
                /*   Make txSingleCarrierTransferReq parameter   */
                /*-------------------------------------------*/
                log.info("Make txSingleCarrierTransferReq parameter");
                //【step22】 - singleCarrierXferFillInOTMSW006InParm
                Outputs.ObjSingleCarrierXferFillInOTMSW006InParmOut singleCarrierXferFillInOTMSW006InParmRetCode = null;
                try {
                    singleCarrierXferFillInOTMSW006InParmRetCode = cassetteMethod.singleCarrierXferFillInOTMSW006InParm(objCommonIn,
                            equipmentID, equipmentTargetPortPickupForInternalBufferRetCode.getEqpTargetPortInfo().getPortGroups().get(0).getStrPortID().get(i).getPortID(),
                            strPortIDs.get(i).getCassetteID(), strWhereNextStockerInqResult);
                } catch (ServiceException e) {
                    if (Validations.isEquals(retCodeConfigEx.getNoXferNeeded(), e.getCode())) {
                        log.info("No Transfer Job is requested by environment variable. Proceed to the Next Port...");
                        continue;
                    } else if (Validations.isEquals(retCodeConfigEx.getNoStockerForCurrentEqp(), e.getCode())) {
                        singleCarrierXferFillInOTMSW006InParmRetCode = e.getData(Outputs.ObjSingleCarrierXferFillInOTMSW006InParmOut.class);
                        Infos.CarrierXferReq carrierXferReq = singleCarrierXferFillInOTMSW006InParmRetCode.getStrCarrierXferReq();
                        log.info("An Error is Detected. Proceed to the Next Port...");
                        //Prepare e-mail Message Text(Skip)
                        StringBuilder msg = new StringBuilder("<<< Transfer Error!  (EQP -> Stocker) >>>\n");
                        msg.append("\n")
                                .append("Equipment ID         : ").append(ObjectIdentifier.fetchValue(equipmentID)).append("\n")
                                .append("-----------------------------------------\n")
                                .append("carrier ID           : ").append(ObjectIdentifier.fetchValue(carrierXferReq.getCarrierID())).append("\n")
                                .append("Lot ID               : ").append(ObjectIdentifier.fetchValue(carrierXferReq.getLotID())).append("\n")
                                .append("From Machine ID      : ").append(ObjectIdentifier.fetchValue(carrierXferReq.getFromMachineID())).append("\n")
                                .append("From Port ID         : ").append(ObjectIdentifier.fetchValue(carrierXferReq.getFromPortID())).append("\n")
                                .append("To Stocker Group     : ").append(carrierXferReq.getToStockerGroup()).append("\n");
                        int toMachineLen = CimArrayUtils.getSize(carrierXferReq.getStrToMachine());
                        if (toMachineLen == 0){
                            msg.append("To Machine ID        : Nothing\n");
                        }
                        for (jj = 0; jj < toMachineLen; jj++) {
                            Infos.ToMachine toMachine = carrierXferReq.getStrToMachine().get(jj);
                            msg.append("To Machine ID        : ").append(ObjectIdentifier.fetchValue(toMachine.getToMachineID())).append("\n")
                                    .append("To Port ID           : ").append(ObjectIdentifier.fetchValue(toMachine.getToPortID())).append("\n");
                        }

                        msg.append("-----------------------------------------\n")
                                .append("Transaction ID       : ").append(e.getTransactionID()).append("\n")
                                .append("Return Code          : ").append(e.getCode()).append("\n")
                                .append("Message Text         : ").append(e.getMessage()).append("\n")
                                .append("Reason Text          : ").append(e.getReasonText()).append("\n");
                        //Set System Message
                        Infos.SysMsgStockInfo sysMsg = new Infos.SysMsgStockInfo();
                        sysMsg.setSubSystemID(BizConstant.SP_SUBSYSTEMID_MM);
                        sysMsg.setSystemMessageCode(BizConstant.SP_SYSTEMMSGCODE_DELIVERYERROR);
                        sysMsg.setSystemMessageText(msg.toString());
                        sysMsg.setNotifyFlag(true);
                        sysMsg.setEquipmentID(equipmentID);
                        sysMsg.setEquipmentStatus("");
                        sysMsg.setStockerID(dummy);
                        sysMsg.setStockerStatus("");
                        sysMsg.setAGVID(dummy);
                        sysMsg.setAGVStatus("");
                        sysMsg.setLotID(dummy);
                        sysMsg.setLotStatus("");
                        sysMsg.setRouteID(dummy);
                        sysMsg.setOperationID(dummy);
                        sysMsg.setOperationNumber("");
                        sysMsg.setSystemMessageTimeStamp(CimDateUtils.convertToSpecString(objCommonIn.getTimeStamp().getReportTimeStamp()));
                        List<Infos.SysMsgStockInfo> sysMstStockInfoList = CimArrayUtils.getSize(out.getSysMstStockInfoList()) == 0 ? new ArrayList<>() : out.getSysMstStockInfoList();
                        sysMstStockInfoList.add(sysMsg);
                        out.setSysMstStockInfoList(sysMstStockInfoList);
                        continue;
                    } else {
                        log.info("singleCarrierXferFillInOTMSW006InParm() rc != RC_OK");
                        e.setData(out);
                        throw e;
                    }
                }
                // Debug Trace of TransferInfo
                /*------------------------------------------------------*/
                /*  Check whether specified machine is eqp or stocker   */
                /*------------------------------------------------------*/
                Boolean isStorageFlag = false;
                //【step23】 - equipment_statusInfo_Get__090
                Infos.EqpStatusInfo eqpStatusInfoRetCode = null;
                try {
                    eqpStatusInfoRetCode = equipmentMethod.equipmentStatusInfoGet(objCommonIn, singleCarrierXferFillInOTMSW006InParmRetCode.getStrCarrierXferReq().getFromMachineID());
                } catch (ServiceException e) {
                    if (Validations.isEquals(retCodeConfig.getNotFoundEqp(), e.getCode())) {
                        isStorageFlag = true;
                    } else {
                        log.info("equipment_statusInfo_Get__090() rc != RC_OK");
                        e.setData(out);
                        throw e;
                    }
                }
                if (CimBooleanUtils.isFalse(isStorageFlag)) {
                    /*--------------------------------------------*/
                    /* Lock Port object of machine                */
                    /*--------------------------------------------*/
                    // step24 - object_LockForEquipmentResource
                    try {
                        lockMethod.objectLockForEquipmentResource(objCommonIn,
                                singleCarrierXferFillInOTMSW006InParmRetCode.getStrCarrierXferReq().getFromMachineID(),
                                singleCarrierXferFillInOTMSW006InParmRetCode.getStrCarrierXferReq().getFromPortID(),
                                BizConstant.SP_CLASSNAME_POSPORTRESOURCE
                        );
                    } catch (ServiceException e) {
                        e.setData(out);
                        throw e;
                    }
                    /*-----------------------------------------------------------------------------*/
                    /*                                                                             */
                    /*   Check Equipment Port for Sending Request to TMS. (EQP -> Stocker)          */
                    /*                                                                             */
                    /*-----------------------------------------------------------------------------*/
                    //【step25】 - equipment_portState_CheckForCassetteDelivery
                    Inputs.ObjEquipmentPortStateCheckForCassetteDeliveryIn deliveryIn = new Inputs.ObjEquipmentPortStateCheckForCassetteDeliveryIn();
                    deliveryIn.setEquipmentID(singleCarrierXferFillInOTMSW006InParmRetCode.getStrCarrierXferReq().getFromMachineID());
                    deliveryIn.setPortID(singleCarrierXferFillInOTMSW006InParmRetCode.getStrCarrierXferReq().getFromPortID());
                    try {
                        equipmentMethod.equipmentPortStateCheckForCassetteDelivery(objCommonIn, deliveryIn);
                    } catch (ServiceException e) {
                        e.setData(out);
                        throw e;
                    }
                }
                /*------------------------------------------*/
                /*   Send Request to TMS. (EQP -> Stocker)   */
                /*------------------------------------------*/
                Params.SingleCarrierTransferReqParam singleCarrierTransferReqParam = new Params.SingleCarrierTransferReqParam();
                singleCarrierTransferReqParam.setRerouteFlag(singleCarrierXferFillInOTMSW006InParmRetCode.getRerouteFlag());
                Infos.CarrierXferReq strCarrierXferReq = singleCarrierXferFillInOTMSW006InParmRetCode.getStrCarrierXferReq();
                singleCarrierTransferReqParam.setCarrierID(strCarrierXferReq.getCarrierID());
                singleCarrierTransferReqParam.setLotID(strCarrierXferReq.getLotID());
                singleCarrierTransferReqParam.setZoneType(strCarrierXferReq.getZoneType());
                singleCarrierTransferReqParam.setN2PurgeFlag(strCarrierXferReq.getN2PurgeFlag());
                singleCarrierTransferReqParam.setFromMachineID(strCarrierXferReq.getFromMachineID());
                singleCarrierTransferReqParam.setFromPortID(strCarrierXferReq.getFromPortID());
                singleCarrierTransferReqParam.setToStockerGroup(strCarrierXferReq.getToStockerGroup());
                singleCarrierTransferReqParam.setToMachine(strCarrierXferReq.getStrToMachine());
                singleCarrierTransferReqParam.setExpectedStartTime(strCarrierXferReq.getExpectedStartTime());
                singleCarrierTransferReqParam.setExpectedEndTime(strCarrierXferReq.getExpectedEndTime());
                singleCarrierTransferReqParam.setMandatoryFlag(strCarrierXferReq.getMandatoryFlag());
                singleCarrierTransferReqParam.setPriority(strCarrierXferReq.getPriority());
                //【step26】 - txSingleCarrierTransferReq
                Results.SingleCarrierTransferReqResult singleCarrierTransferReqResultRetCode = null;
                try {
                    singleCarrierTransferReqResultRetCode = this.sxSingleCarrierTransferReq(objCommonIn, singleCarrierTransferReqParam);
                } catch (ServiceException e) {
                    log.info("txSingleCarrierTransferReq() rc != RC_OK");
                    //Prepare e-mail Message Text (skip)
                    Infos.CarrierXferReq carrierXferReq = singleCarrierXferFillInOTMSW006InParmRetCode.getStrCarrierXferReq();
                    StringBuilder msg = new StringBuilder("<<< Transfer Error!  (EQP -> Stocker) >>>\n");
                    msg.append("\n")
                            .append("Equipment ID         : ").append(ObjectIdentifier.fetchValue(equipmentID)).append("\n")
                            .append("-----------------------------------------\n")
                            .append("carrier ID           : ").append(ObjectIdentifier.fetchValue(carrierXferReq.getCarrierID())).append("\n")
                            .append("Lot ID               : ").append(ObjectIdentifier.fetchValue(carrierXferReq.getLotID())).append("\n")
                            .append("From Machine ID      : ").append(ObjectIdentifier.fetchValue(carrierXferReq.getFromMachineID())).append("\n")
                            .append("From Port ID         : ").append(ObjectIdentifier.fetchValue(carrierXferReq.getFromPortID())).append("\n")
                            .append("To Stocker Group     : ").append(carrierXferReq.getToStockerGroup()).append("\n");
                    int toMachineLen = CimArrayUtils.getSize(carrierXferReq.getStrToMachine());
                    if (toMachineLen == 0){
                        msg.append("To Machine ID        : Nothing\n");
                    }
                    for (jj = 0; jj < toMachineLen; jj++) {
                        Infos.ToMachine toMachine = carrierXferReq.getStrToMachine().get(jj);
                        msg.append("To Machine ID        : ").append(ObjectIdentifier.fetchValue(toMachine.getToMachineID())).append("\n")
                                .append("To Port ID           : ").append(ObjectIdentifier.fetchValue(toMachine.getToPortID())).append("\n");
                    }

                    msg.append("-----------------------------------------\n")
                            .append("Transaction ID       : ").append(e.getTransactionID()).append("\n")
                            .append("Return Code          : ").append(e.getCode()).append("\n")
                            .append("Message Text         : ").append(e.getMessage()).append("\n")
                            .append("Reason Text          : ").append(e.getReasonText()).append("\n");
                    //Set System Message
                    Infos.SysMsgStockInfo sysMsg = new Infos.SysMsgStockInfo();
                    sysMsg.setSubSystemID(BizConstant.SP_SUBSYSTEMID_MM);
                    sysMsg.setSystemMessageCode(BizConstant.SP_SYSTEMMSGCODE_DELIVERYERROR);
                    sysMsg.setSystemMessageText(msg.toString());
                    sysMsg.setNotifyFlag(true);
                    sysMsg.setEquipmentID(equipmentID);
                    sysMsg.setEquipmentStatus("");
                    sysMsg.setStockerID(dummy);
                    sysMsg.setStockerStatus("");
                    sysMsg.setAGVID(dummy);
                    sysMsg.setAGVStatus("");
                    sysMsg.setLotID(dummy);
                    sysMsg.setLotStatus("");
                    sysMsg.setRouteID(dummy);
                    sysMsg.setOperationID(dummy);
                    sysMsg.setOperationNumber("");
                    sysMsg.setSystemMessageTimeStamp(CimDateUtils.convertToSpecString(objCommonIn.getTimeStamp().getReportTimeStamp()));
                    List<Infos.SysMsgStockInfo> sysMstStockInfoList = CimArrayUtils.getSize(out.getSysMstStockInfoList()) == 0 ? new ArrayList<>() : out.getSysMstStockInfoList();
                    sysMstStockInfoList.add(sysMsg);
                    out.setSysMstStockInfoList(sysMstStockInfoList);
                    e.setData(out);
                    throw e;
                }
            }
            /*--------------------*/
            /*   Return to Main   */
            /*--------------------*/
            log.info("OK! UnloadReq [EQP to Stocker]  Return to Main");
            return out;
        }
        /******************************************************************************************/
        /*                                                                                        */
        /*     LoadReq Process                                                                    */
        /*                                                                                        */
        /******************************************************************************************/
        if (CimStringUtils.equals(BizConstant.SP_PORTRSC_PORTSTATE_LOADREQ, equipmentTargetPortPickupForInternalBufferRetCode.getTargetPortType())) {
            /*-----------------*/
            /*   Load Request  */
            /*-----------------*/
            log.info("LoadReq Process RUN!!");
            if (CimBooleanUtils.isFalse(eqpInfoForIBInqResultRetCode.getEquipmentStatusInfo().getEquipmentAvailableFlag())) {
                log.info("TRUE != strEqpInfoInqResult.equipmentStatusInfo.equipmentAvailableFlag");
                throw new ServiceException(new OmCode(retCodeConfig.getInvalidEquipmentStatus(), equipmentID.getValue(), "NotAvailable"));
            }
            /*------------------*/
            /*   Get WIP Lots   */
            /*------------------*/
            log.info("Get WIP Lots");
            Results.WhatNextLotListResult strWhatNextInqResult = null;
            //【step27】 - cassetteDelivery_RTDInterfaceReq
            Outputs.ObjCassetteDeliveryRTDInterfaceReqOut cassetteDeliveryRTDInterfaceReqOutRetCode = null;
            try {
                cassetteDeliveryRTDInterfaceReqOutRetCode = cassetteMethod.cassetteDeliveryRTDInterfaceReq(objCommonIn, BizConstant.SP_RTD_FUNCTION_CODE_WHATNEXT, equipmentID);
                log.info("Normal End RTD Interface of txWhatNextInq");
                strWhatNextInqResult = null == cassetteDeliveryRTDInterfaceReqOutRetCode ? null : cassetteDeliveryRTDInterfaceReqOutRetCode.getStrWhatNextInqResult();
            } catch (ServiceException e) {
                log.info("Faild RTD Interface Call Normal Function (txWhatNextInq)");
                if (!Validations.isEquals(retCodeConfigEx.getRtdInterfaceSwitchOff(), e.getCode())
                        && !Validations.isEquals(retCodeConfigEx.getNotFoundRTD(), e.getCode())) {
                    //Set System Message (skip)
                    StringBuilder msg = new StringBuilder("<<< RTD Interface Error!  (WhatNext of CassetteDelivery) >>>\n");
                    msg.append("\n")
                            .append("Equipment ID         : ").append(ObjectIdentifier.fetchValue(equipmentID)).append("\n")
                            .append("-----------------------------------------\n")
                            .append("Transaction ID       : ").append(e.getTransactionID()).append("\n")
                            .append("Return Code          : ").append(e.getCode()).append("\n")
                            .append("Message Text         : ").append(e.getMessage()).append("\n")
                            .append("Reason Text          : ").append(e.getReasonText()).append("\n");

                    Infos.SysMsgStockInfo sysMsg = new Infos.SysMsgStockInfo();
                    sysMsg.setSubSystemID(BizConstant.SP_SUBSYSTEMID_MM);
                    sysMsg.setSystemMessageCode(BizConstant.SP_SYSTEMMSGCODE_RTDERROR);
                    sysMsg.setSystemMessageText(msg.toString());
                    sysMsg.setNotifyFlag(true);
                    sysMsg.setEquipmentID(equipmentID);
                    sysMsg.setEquipmentStatus("");
                    sysMsg.setStockerID(dummy);
                    sysMsg.setStockerStatus("");
                    sysMsg.setAGVID(dummy);
                    sysMsg.setAGVStatus("");
                    sysMsg.setLotID(dummy);
                    sysMsg.setLotStatus("");
                    sysMsg.setRouteID(dummy);
                    sysMsg.setOperationID(dummy);
                    sysMsg.setOperationNumber("");
                    sysMsg.setSystemMessageTimeStamp(CimDateUtils.convertToSpecString(objCommonIn.getTimeStamp().getReportTimeStamp()));
                    List<Infos.SysMsgStockInfo> sysMstStockInfoList = CimArrayUtils.getSize(out.getSysMstStockInfoList()) == 0 ? new ArrayList<>() : out.getSysMstStockInfoList();
                    sysMstStockInfoList.add(sysMsg);
                    out.setSysMstStockInfoList(sysMstStockInfoList);
                }
                //Call Normal Function (txWhatNextInq)
                Params.WhatNextLotListParams whatNextLotListParams = new Params.WhatNextLotListParams();
                whatNextLotListParams.setEquipmentID(equipmentID);
                whatNextLotListParams.setSelectCriteria(ObjectIdentifier.buildWithValue(BizConstant.SP_DP_SELECTCRITERIA_AUTO3));
                //【step32】 - txWhatNextInq__140
                try {
                    strWhatNextInqResult = dispatchInqService.sxWhatNextLotListInfo(objCommonIn, whatNextLotListParams);
                } catch (ServiceException ex) {
                    if (!Validations.isEquals(retCodeConfigEx.getNoWipLot(), ex.getCode())) {
                        //Set System Message
                        StringBuilder msg = new StringBuilder("<<< WhatNext Error!  (Stocker -> EQP) >>>\n");
                        msg.append("\n")
                                .append("Equipment ID         : ").append(ObjectIdentifier.fetchValue(equipmentID)).append("\n")
                                .append("-----------------------------------------\n")
                                .append("Transaction ID       : ").append(ex.getTransactionID()).append("\n")
                                .append("Return Code          : ").append(ex.getCode()).append("\n")
                                .append("Message Text         : ").append(ex.getMessage()).append("\n")
                                .append("Reason Text          : ").append(ex.getReasonText()).append("\n");

                        Infos.SysMsgStockInfo sysMsg = new Infos.SysMsgStockInfo();
                        sysMsg.setSubSystemID(BizConstant.SP_SUBSYSTEMID_MM);
                        sysMsg.setSystemMessageCode(BizConstant.SP_SYSTEMMSGCODE_DELIVERYERROR);
                        sysMsg.setSystemMessageText(msg.toString());
                        sysMsg.setNotifyFlag(true);
                        sysMsg.setEquipmentID(equipmentID);
                        sysMsg.setEquipmentStatus("");
                        sysMsg.setStockerID(dummy);
                        sysMsg.setStockerStatus("");
                        sysMsg.setAGVID(dummy);
                        sysMsg.setAGVStatus("");
                        sysMsg.setLotID(dummy);
                        sysMsg.setLotStatus("");
                        sysMsg.setRouteID(dummy);
                        sysMsg.setOperationID(dummy);
                        sysMsg.setOperationNumber("");
                        sysMsg.setSystemMessageTimeStamp(CimDateUtils.convertToSpecString(objCommonIn.getTimeStamp().getReportTimeStamp()));
                        List<Infos.SysMsgStockInfo> sysMstStockInfoList = CimArrayUtils.getSize(out.getSysMstStockInfoList()) == 0 ? new ArrayList<>() : out.getSysMstStockInfoList();
                        sysMstStockInfoList.add(sysMsg);
                        out.setSysMstStockInfoList(sysMstStockInfoList);
                    }
                    ex.setData(out);
                    throw ex;
                }
            }
            /*--------------------------*/
            /*   Select Multiple Lots   */
            /*--------------------------*/
            log.info("Select Multiple Lots ");
            Results.WhatNextLotListResult convWhatNextInqResult = strWhatNextInqResult;
            //【step29】 - whatNextLotList_to_StartCassetteForDeliveryForInternalBufferReq
            List<Infos.StartCassette> whatNextLotListToStartCassetteForDeliveryForInternalBufferReqRetCode = null;
            try {
                whatNextLotListToStartCassetteForDeliveryForInternalBufferReqRetCode = whatNextMethod.whatNextLotListToStartCassetteForDeliveryForInternalBufferReq(objCommonIn,
                        equipmentID,
                        equipmentTargetPortPickupForInternalBufferRetCode,
                        convWhatNextInqResult,
                        true,
                        equipmentInternalBufferInfoGetRetCode);
            } catch (ServiceException e) {
                log.info("whatNextLotListToStartCassetteForDeliveryForInternalBufferReq != RC_OK");
                e.setData(out);
                throw e;
            }
            int tmpFPCAdoptFlag = StandardProperties.OM_DOC_ENABLE_FLAG.getIntValue();
            if (tmpFPCAdoptFlag == 1) {
                log.info("DOC Adopt Flag is ON. Now apply FPCInfo.");
                //【step30】 - FPCStartCassetteInfo_Exchange
                String exchangeType = BizConstant.SP_FPC_EXCHANGETYPE_STARTRESERVEINFO;
                try {
                    whatNextLotListToStartCassetteForDeliveryForInternalBufferReqRetCode = fpcMethod.fpcStartCassetteInfoExchange(objCommonIn, exchangeType, equipmentID, whatNextLotListToStartCassetteForDeliveryForInternalBufferReqRetCode);
                } catch (ServiceException e) {
                    e.setData(out);
                    throw e;
                }
            } else {
                log.info("DOC Adopt Flag is OFF");
            }
            log.info("Set processJobExecFlag based on wafer sampling setting. ");
            //【step31】 - startCassette_processJobExecFlag_Set__090
            Boolean notSendIgnoreMail = false;
            OmCode startCassette_processJobExecFlag_SetRc = retCodeConfig.getSucc();
            Outputs.ObjStartCassetteProcessJobExecFlagSetOut startCassetteProcessJobExecFlagSetOutRetCode = null;
            try {
                startCassetteProcessJobExecFlagSetOutRetCode = startCassetteMethod.startCassetteProcessJobExecFlagSet(objCommonIn,
                        whatNextLotListToStartCassetteForDeliveryForInternalBufferReqRetCode, equipmentID);
            } catch (ServiceException ex) {
                startCassetteProcessJobExecFlagSetOutRetCode = ex.getData(Outputs.ObjStartCassetteProcessJobExecFlagSetOut.class);
                if (!Validations.isEquals(retCodeConfig.getNoSmplSetting(), ex.getCode())) {
                    // When errors have occurred, hold add the error lot and send e-mail to notify that.
                    notSendIgnoreMail = true;
                    startCassette_processJobExecFlag_SetRc = retCodeConfig.getInvalidSmplSetting();
                }
            }
            //hold lots and send E-Mails if error
            int mailLen = CimArrayUtils.getSize(startCassetteProcessJobExecFlagSetOutRetCode.getSamplingMessage());
            for (int mailCnt = 0; mailCnt < mailLen; mailCnt++) {
                if (BizConstant.SP_SAMPLING_ERROR_MAIL == startCassetteProcessJobExecFlagSetOutRetCode.getSamplingMessage().get(mailCnt).getMessageType()
                        || BizConstant.SP_SAMPLING_WARN_MAIL ==  startCassetteProcessJobExecFlagSetOutRetCode.getSamplingMessage().get(mailCnt).getMessageType()) {
                    log.info("messageType == SP_Sampling_error_mail || SP_Sampling_warn_mail ");
                    // Lot Hold
                    //【step32】 - lot_currentOperationInfo_Get
                    Outputs.ObjLotCurrentOperationInfoGetOut lotCurrentOperationInfoGetRetCode = null;
                    try {
                        lotCurrentOperationInfoGetRetCode = lotMethod.lotCurrentOperationInfoGet(objCommonIn, startCassetteProcessJobExecFlagSetOutRetCode.getSamplingMessage().get(mailCnt).getLotID());
                    } catch (ServiceException e) {
                        e.setData(out);
                        throw e;
                    }

                    Infos.LotHoldReq lotHoldReq = new Infos.LotHoldReq();
                    lotHoldReq.setHoldType(BizConstant.SP_HOLDTYPE_LOTHOLD);
                    lotHoldReq.setHoldReasonCodeID(new ObjectIdentifier(BizConstant.SP_REASON_WAFERSAMPLINGHOLD));
                    lotHoldReq.setHoldUserID(objCommonIn.getUser().getUserID());
                    lotHoldReq.setResponsibleOperationMark(BizConstant.SP_RESPONSIBLEOPERATION_CURRENT);
                    lotHoldReq.setRouteID(lotCurrentOperationInfoGetRetCode.getRouteID());
                    lotHoldReq.setOperationNumber(lotCurrentOperationInfoGetRetCode.getOperationNumber());
                    lotHoldReq.setClaimMemo("");
                    List<Infos.LotHoldReq> holdReqList = new ArrayList<Infos.LotHoldReq>();
                    holdReqList.add(lotHoldReq);
                    //【step33】 - txHoldLotReq
                    try {
                        lotService.sxHoldLotReq(objCommonIn, startCassetteProcessJobExecFlagSetOutRetCode.getSamplingMessage().get(mailCnt).getLotID(), holdReqList);
                    } catch (ServiceException e) {
                        e.setData(out);
                        throw e;
                    }
                }
                //error Mail Set
                if (CimBooleanUtils.isFalse(notSendIgnoreMail) || BizConstant.SP_SAMPLING_WARN_MAIL != startCassetteProcessJobExecFlagSetOutRetCode.getSamplingMessage().get(mailCnt).getMessageType()) {
                    //When notSendIgnoreMail == TRUE, just the Lot Hold mail is to be sent.
                    String messageText = "";
                    if (BizConstant.SP_SAMPLING_WARN_MAIL == startCassetteProcessJobExecFlagSetOutRetCode.getSamplingMessage().get(mailCnt).getMessageType()) {
                        //----------------------------------------------------------------
                        // create mail text if message type == SP_Sampling_warn_mail;
                        // startCassette_processJobExecFlag_Set__090 returns just error text. Caller should switch text handling.
                        //----------------------------------------------------------------
                        //【step34】 - lot_samplingMessage_Create
                        try {
                            messageText = lotMethod.lotSamplingMessageCreate(objCommonIn,
                                    startCassetteProcessJobExecFlagSetOutRetCode.getSamplingMessage().get(mailCnt).getLotID(),
                                    BizConstant.SP_SAMPLING_ERROR_MAIL,
                                    startCassetteProcessJobExecFlagSetOutRetCode.getSamplingMessage().get(mailCnt).getMessageText());
                        } catch (ServiceException e) {
                            e.setData(out);
                            throw e;
                        }
                    } else {
                        messageText = startCassetteProcessJobExecFlagSetOutRetCode.getSamplingMessage().get(mailCnt).getMessageText();
                    }
                    //Set System Message
                    Infos.SysMsgStockInfo sysMsg = new Infos.SysMsgStockInfo();
                    sysMsg.setSubSystemID(BizConstant.SP_SUBSYSTEMID_MM);
                    sysMsg.setSystemMessageCode(BizConstant.SP_SYSTEMMSGCODE_SMPLERR);
                    sysMsg.setSystemMessageText(messageText);
                    sysMsg.setNotifyFlag(true);
                    sysMsg.setEquipmentID(equipmentID);
                    sysMsg.setEquipmentStatus("");
                    sysMsg.setStockerID(dummy);
                    sysMsg.setStockerStatus("");
                    sysMsg.setAGVID(dummy);
                    sysMsg.setAGVStatus("");
                    sysMsg.setLotID(startCassetteProcessJobExecFlagSetOutRetCode.getSamplingMessage().get(mailCnt).getLotID());
                    sysMsg.setLotStatus("");
                    sysMsg.setRouteID(dummy);
                    sysMsg.setOperationID(dummy);
                    sysMsg.setOperationNumber("");
                    sysMsg.setSystemMessageTimeStamp(CimDateUtils.convertToSpecString(objCommonIn.getTimeStamp().getReportTimeStamp()));
                    List<Infos.SysMsgStockInfo> sysMstStockInfoList = CimArrayUtils.getSize(out.getSysMstStockInfoList()) == 0 ? new ArrayList<>() : out.getSysMstStockInfoList();
                    sysMstStockInfoList.add(sysMsg);
                    out.setSysMstStockInfoList(sysMstStockInfoList);
                }
            }
            if (CimBooleanUtils.isTrue(notSendIgnoreMail)) {
                log.info("notSendIgnoreMail == TRUE  return this TX.");
                if (Validations.isEquals(retCodeConfig.getSucc(),startCassette_processJobExecFlag_SetRc)){
                    return out;
                }else {
                    throw new ServiceException(startCassette_processJobExecFlag_SetRc,out);
                }
            }
            //valid check of processJobExecFlag for each lot.
            //Check every lot has at least one wafer with processJobExecFlag == TRUE
            //【step35】 - lot_processJobExecFlag_ValidCheck
            Outputs.ObjLotProcessJobExecFlagValidCheckOut lotProcessJobExecFlagValidCheckOut = null;
            try {
                lotProcessJobExecFlagValidCheckOut = lotMethod.lotProcessJobExecFlagValidCheck(objCommonIn, startCassetteProcessJobExecFlagSetOutRetCode.getStartCassettes());
            } catch (ServiceException e) {
                if (!Validations.isEquals(retCodeConfig.getNoProcessJobExecFlag(), e.getCode())) {
                    log.info("lot_processJobExecFlag_ValidCheck() != RC_OK && != RC_NO_PROCESSJOBEXECFLAG");
                    e.setData(out);
                    throw e;
                }
                if (Validations.isEquals(retCodeConfig.getNoProcessJobExecFlag(), e.getCode())) {
                    lotProcessJobExecFlagValidCheckOut = e.getData(Outputs.ObjLotProcessJobExecFlagValidCheckOut.class);
                    log.info("objLot_processJobExecFlag_ValidCheck() rc == RC_NO_PROCESSJOBEXECFLAG");
                    // Lot Hold + error mail
                    // create hold mail message
                    //【step36】 - lot_samplingMessage_Create
                    String lotSamplingMessageCreateRetCode = null;
                    try {
                        lotSamplingMessageCreateRetCode = lotMethod.lotSamplingMessageCreate(objCommonIn,
                                lotProcessJobExecFlagValidCheckOut.getLotID(),
                                BizConstant.SP_SAMPLING_ERROR_MAIL,
                                e.getMessage());
                    } catch (ServiceException ex) {
                        ex.setData(out);
                        throw ex;
                    }

                    //【step37】 - lot_currentOperationInfo_Get
                    Outputs.ObjLotCurrentOperationInfoGetOut lotCurrentOperationInfo = null;
                    try {
                        lotCurrentOperationInfo = lotMethod.lotCurrentOperationInfoGet(objCommonIn, lotProcessJobExecFlagValidCheckOut.getLotID());
                    } catch (ServiceException ex) {
                        ex.setData(out);
                        throw ex;
                    }

                    Infos.LotHoldReq lotHoldReq = new Infos.LotHoldReq();
                    lotHoldReq.setHoldType(BizConstant.SP_HOLDTYPE_LOTHOLD);
                    lotHoldReq.setHoldReasonCodeID(new ObjectIdentifier(BizConstant.SP_REASON_WAFERSAMPLINGHOLD));
                    lotHoldReq.setHoldUserID(objCommonIn.getUser().getUserID());
                    lotHoldReq.setResponsibleOperationMark(BizConstant.SP_RESPONSIBLEOPERATION_CURRENT);
                    lotHoldReq.setRouteID(lotCurrentOperationInfo.getRouteID());
                    lotHoldReq.setOperationNumber(lotCurrentOperationInfo.getOperationNumber());
                    lotHoldReq.setClaimMemo("");
                    List<Infos.LotHoldReq> holdReqList = new ArrayList<Infos.LotHoldReq>();
                    holdReqList.add(lotHoldReq);
                    //【step38】 - txHoldLotReq
                    try {
                        lotService.sxHoldLotReq(objCommonIn, lotProcessJobExecFlagValidCheckOut.getLotID(), holdReqList);
                    } catch (ServiceException ex) {
                        ex.setData(out);
                        throw ex;
                    }
                    //error Mail Set
                    Infos.SysMsgStockInfo sysMsg = new Infos.SysMsgStockInfo();
                    sysMsg.setSubSystemID(BizConstant.SP_SUBSYSTEMID_MM);
                    sysMsg.setSystemMessageCode(BizConstant.SP_SYSTEMMSGCODE_SMPLERR);
                    sysMsg.setSystemMessageText(lotSamplingMessageCreateRetCode);
                    sysMsg.setNotifyFlag(true);
                    sysMsg.setEquipmentID(equipmentID);
                    sysMsg.setEquipmentStatus("");
                    sysMsg.setStockerID(dummy);
                    sysMsg.setStockerStatus("");
                    sysMsg.setAGVID(dummy);
                    sysMsg.setAGVStatus("");
                    sysMsg.setLotID(lotProcessJobExecFlagValidCheckOut.getLotID());
                    sysMsg.setLotStatus("");
                    sysMsg.setRouteID(dummy);
                    sysMsg.setOperationID(dummy);
                    sysMsg.setOperationNumber("");
                    sysMsg.setSystemMessageTimeStamp(CimDateUtils.convertToSpecString(objCommonIn.getTimeStamp().getReportTimeStamp()));
                    List<Infos.SysMsgStockInfo> sysMstStockInfoList = CimArrayUtils.getSize(out.getSysMstStockInfoList()) == 0 ? new ArrayList<>() : out.getSysMstStockInfoList();
                    sysMstStockInfoList.add(sysMsg);
                    out.setSysMstStockInfoList(sysMstStockInfoList);
                    throw new ServiceException(retCodeConfig.getNoProcessJobExecFlag(), out);
                }
            }

            whatNextLotListToStartCassetteForDeliveryForInternalBufferReqRetCode = startCassetteProcessJobExecFlagSetOutRetCode.getStartCassettes();
            log.info("Set processJobExecFlag based on wafer sampling setting end. ");
            //Request to Start Lot Reservation
            Infos.ObjCommon strTmpObjCommonIn = objCommonIn;
            strTmpObjCommonIn.setTransactionID("ODISW003");
            Params.MoveInReserveForIBReqParams params = new Params.MoveInReserveForIBReqParams();
            params.setEquipmentID(equipmentID);
            params.setControlJobID(dummy);
            params.setStartCassetteList(whatNextLotListToStartCassetteForDeliveryForInternalBufferReqRetCode);
            params.setOpeMemo("");
            //【step39】 - txMoveInReserveForIBReq
            ObjectIdentifier moveInReserveForIBReqRetCode = null;
            try {
                moveInReserveForIBReqRetCode = dispatchService.sxMoveInReserveForIBReq(strTmpObjCommonIn, params, tmpAPCIFControlStatus);
            } catch (ServiceException ex) {
                /*------------------------*/
                /*   Set System Message   */
                /*------------------------*/
                log.info("Set System Message.   txMoveInReserveReq() != RC_OK");
                //---------------------------------------------------------------------------------------------
                //  Prepare message text string for case of failure of start lot reservation
                //  Only if start lot resercatioin for current equipment is fail, this messageText is filled
                //---------------------------------------------------------------------------------------------
                //-------------------------------------------------------
                // Prepare e-mail Message Text
                //-------------------------------------------------------
                StringBuilder msg = new StringBuilder("<<< MoveInReservation Error!  (Stocker -> EQP) >>>\n");
                msg.append("\n")
                        .append("Equipment ID         : ").append(ObjectIdentifier.fetchValue(equipmentID)).append("\n")
                        .append("Lot IDs              : ");
                boolean bLotSet = false;
                for (i = 0; i < CimArrayUtils.getSize(whatNextLotListToStartCassetteForDeliveryForInternalBufferReqRetCode); i++) {
                    for (j = 0; j < CimArrayUtils.getSize(whatNextLotListToStartCassetteForDeliveryForInternalBufferReqRetCode.get(i).getLotInCassetteList()); j++) {
                        if (!ObjectIdentifier.isEmpty(whatNextLotListToStartCassetteForDeliveryForInternalBufferReqRetCode.get(i).getLotInCassetteList().get(j).getLotID())){
                            if (CimBooleanUtils.isTrue(bLotSet)){
                                msg.append(", ");
                            }
                            msg.append(ObjectIdentifier.fetchValue(whatNextLotListToStartCassetteForDeliveryForInternalBufferReqRetCode.get(i).getLotInCassetteList().get(j).getLotID()));
                            bLotSet = true;
                        }
                    }
                }
                msg.append("\n")
                        .append("Transaction ID       : ").append(ex.getTransactionID()).append("\n")
                        .append("Return Code          : ").append(ex.getCode()).append("\n")
                        .append("Message Text         : ").append(ex.getMessage()).append("\n")
                        .append("Reason Text          : ").append(ex.getReasonText()).append("\n");
                Infos.SysMsgStockInfo sysMsg = new Infos.SysMsgStockInfo();
                sysMsg.setSubSystemID(BizConstant.SP_SUBSYSTEMID_MM);
                sysMsg.setSystemMessageCode(BizConstant.SP_SYSTEMMSGCODE_DELIVERYERROR);
                sysMsg.setSystemMessageText(msg.toString());
                sysMsg.setNotifyFlag(true);
                sysMsg.setEquipmentID(equipmentID);
                sysMsg.setEquipmentStatus("");
                sysMsg.setStockerID(dummy);
                sysMsg.setStockerStatus("");
                sysMsg.setAGVID(dummy);
                sysMsg.setAGVStatus("");
                sysMsg.setLotID(dummy);
                sysMsg.setLotStatus("");
                sysMsg.setRouteID(dummy);
                sysMsg.setOperationID(dummy);
                sysMsg.setOperationNumber("");
                sysMsg.setSystemMessageTimeStamp(CimDateUtils.convertToSpecString(objCommonIn.getTimeStamp().getReportTimeStamp()));
                List<Infos.SysMsgStockInfo> sysMstStockInfoList = CimArrayUtils.getSize(out.getSysMstStockInfoList()) == 0 ? new ArrayList<>() : out.getSysMstStockInfoList();
                sysMstStockInfoList.add(sysMsg);
                out.setSysMstStockInfoList(sysMstStockInfoList);
                ex.setData(out);
                throw ex;
            }
            /*------------------------------------------*/
            /*   Make txMultipleCarrierTransferReq parameter   */
            /*------------------------------------------*/
            //【step40】 - multiCarrierXferFillInOTMSW005InParm
            Outputs.ObjMultiCarrierXferFillInOTMSW005InParmOut multiCarrierXferFillInOTMSW005InParmRetCode = null;
            try {
                multiCarrierXferFillInOTMSW005InParmRetCode = cassetteMethod.multiCarrierXferFillInOTMSW005InParm(objCommonIn,
                        equipmentID, whatNextLotListToStartCassetteForDeliveryForInternalBufferReqRetCode);
            } catch (ServiceException e) {
                log.info("multiCarrierXferFillInOTMSW005InParm() rc != RC_OK");
                /*---------------------------------------------*/
                /*   Request to Start Lot Reservation Cancel   */
                /*---------------------------------------------*/
                log.info("Request to Start Lot Reservation Cancel");
                //【step41】 - txMoveInReserveCancelForIBReq
                Params.MoveInReserveCancelForIBReqParams input = new Params.MoveInReserveCancelForIBReqParams();
                input.setEquipmentID(equipmentID);
                input.setControlJobID(moveInReserveForIBReqRetCode);
                input.setOpeMemo("");
                input.setAPCIFControlStatus(new String());
                try {
                    dispatchService.sxMoveInReserveCancelForIBReqService(objCommonIn, input);
                } catch (ServiceException ex) {
                    log.info("txMoveInReserveCancelForIBReq != RC_OK");
                }
                throw new ServiceException(retCodeConfigEx.getStartLotReservationFail(), out);
            }
            // Debug Trace of TransferInfo
            /*---------------------------------------------------*/
            /*   Send Transfer Request to TMS. (Stocker -> EQP)   */
            /*---------------------------------------------------*/
            log.info("Send Transfer Request to TMS. (Stocker -> EQP) ");
            Boolean bReRouteFlag = false;
            String reRouteXferFlag = StandardProperties.OM_XFER_REROUTE_FLAG.getValue();
            if (CimStringUtils.equals("1", reRouteXferFlag)) {
                log.info("reRouteXferFlag is 1");
                bReRouteFlag = true;
            }
            //Send Transfer Request to TMS. (Stocker -> EQP
            //【step42】 - txMultipleCarrierTransferReq
            try {
                this.sxMultipleCarrierTransferReq(objCommonIn, bReRouteFlag, "S", multiCarrierXferFillInOTMSW005InParmRetCode.getStrCarrierXferReq());
            } catch (ServiceException e) {
                log.info("txMultipleCarrierTransferReq() rc != RC_OK");
                //Prepare e-mail Message Text
                StringBuilder msg = new StringBuilder("<<< Transfer Error!  (Stocker -> EQP) >>>\n");
                msg.append("\n")
                        .append("Equipment ID         : ").append(ObjectIdentifier.fetchValue(equipmentID)).append("\n");
                for (ii = 0; ii < CimArrayUtils.getSize(multiCarrierXferFillInOTMSW005InParmRetCode.getStrCarrierXferReq()); ii++) {
                    Infos.CarrierXferReq carrierXferReq = multiCarrierXferFillInOTMSW005InParmRetCode.getStrCarrierXferReq().get(ii);
                    msg.append("-----------------------------------------\n")
                            .append("carrier ID           : ").append(ObjectIdentifier.fetchValue(carrierXferReq.getCarrierID())).append("\n")
                            .append("Lot ID               : ").append(ObjectIdentifier.fetchValue(carrierXferReq.getLotID())).append("\n")
                            .append("From Machine ID      : ").append(ObjectIdentifier.fetchValue(carrierXferReq.getFromMachineID())).append("\n")
                            .append("From Port ID         : ").append(ObjectIdentifier.fetchValue(carrierXferReq.getFromPortID())).append("\n")
                            .append("To Stocker Group     : ").append(carrierXferReq.getToStockerGroup()).append("\n");
                    int toMachineLen = CimArrayUtils.getSize(carrierXferReq.getStrToMachine());
                    if (toMachineLen == 0){
                        msg.append("To Machine ID        : Nothing\n");
                    }
                    for (jj = 0; jj < toMachineLen; jj++) {
                        Infos.ToMachine toMachine = carrierXferReq.getStrToMachine().get(jj);
                        msg.append("To Machine ID        : ").append(ObjectIdentifier.fetchValue(toMachine.getToMachineID())).append("\n")
                                .append("To Port ID           : ").append(ObjectIdentifier.fetchValue(toMachine.getToPortID())).append("\n");
                    }
                }
                msg.append("-----------------------------------------\n")
                        .append("Transaction ID       : ").append(e.getTransactionID()).append("\n")
                        .append("Return Code          : ").append(e.getCode()).append("\n")
                        .append("Message Text         : ").append(e.getMessage()).append("\n")
                        .append("Reason Text          : ").append(e.getReasonText()).append("\n");
                Infos.SysMsgStockInfo sysMsg = new Infos.SysMsgStockInfo();
                sysMsg.setSubSystemID(BizConstant.SP_SUBSYSTEMID_MM);
                sysMsg.setSystemMessageCode(BizConstant.SP_SYSTEMMSGCODE_DELIVERYERROR);
                sysMsg.setSystemMessageText(msg.toString());
                sysMsg.setNotifyFlag(true);
                sysMsg.setEquipmentID(equipmentID);
                sysMsg.setEquipmentStatus("");
                sysMsg.setStockerID(dummy);
                sysMsg.setStockerStatus("");
                sysMsg.setAGVID(dummy);
                sysMsg.setAGVStatus("");
                sysMsg.setLotID(dummy);
                sysMsg.setLotStatus("");
                sysMsg.setRouteID(dummy);
                sysMsg.setOperationID(dummy);
                sysMsg.setOperationNumber("");
                sysMsg.setSystemMessageTimeStamp(CimDateUtils.convertToSpecString(objCommonIn.getTimeStamp().getReportTimeStamp()));
                List<Infos.SysMsgStockInfo> sysMstStockInfoList = CimArrayUtils.getSize(out.getSysMstStockInfoList()) == 0 ? new ArrayList<>() : out.getSysMstStockInfoList();
                sysMstStockInfoList.add(sysMsg);
                out.setSysMstStockInfoList(sysMstStockInfoList);
                //Request to Start Lot Reservation Cancel
                //【step43】 - txMoveInReserveCancelForIBReq
                Params.MoveInReserveCancelForIBReqParams input = new Params.MoveInReserveCancelForIBReqParams();
                input.setEquipmentID(equipmentID);
                input.setControlJobID(moveInReserveForIBReqRetCode);
                input.setOpeMemo("");
                input.setAPCIFControlStatus(new String());
                try {
                    dispatchService.sxMoveInReserveCancelForIBReqService(objCommonIn, input);
                } catch (ServiceException ex) {
                    log.info("##### txMoveInReserveCancelForIBReq != RC_OK");
                }
                throw new ServiceException(retCodeConfigEx.getStartLotReservationFail(), out);
            }
            log.info("OK! LoadReq  Return to Main");
            /*--------------------*/
            /*   End of Process   */
            /*--------------------*/
        }
        /*--------------------*/
        /*                    */
        /*   Return to Main   */
        /*                    */
        /*--------------------*/
        return out;
    }

    @Override
    public Results.CarrierTransferReqResult sxCarrierTransferReq(Infos.ObjCommon objCommonIn, ObjectIdentifier equipmentID) {
        //Initialize
        Results.CarrierTransferReqResult out = new Results.CarrierTransferReqResult();
        out.setSysMstStockInfoList(new ArrayList<>());

        //Check Process
        // 【Step-1】: equipment_categoryVsTxID_CheckCombination
        equipmentMethod.equipmentCategoryVsTxIDCheckCombination(objCommonIn, equipmentID);

        //Get eqp Infomation
        // 【Step-2】: txEqpInfoInq;
        Results.EqpInfoInqResult eqpInfoInqRes = new Results.EqpInfoInqResult();
        Infos.EqpStatusInfo equipmentStatusInfo = new Infos.EqpStatusInfo();
        Infos.EqpBrInfo equipmentBRInfo = new Infos.EqpBrInfo();
        eqpInfoInqRes.setEquipmentBasicInfo(equipmentBRInfo);
        eqpInfoInqRes.setEquipmentStatusInfo(equipmentStatusInfo);
        equipmentStatusInfo.setEquipmentAvailableFlag(false);
        equipmentBRInfo.setReticleUseFlag(false);
        equipmentBRInfo.setFixtureUseFlag(false);
        equipmentBRInfo.setCassetteChangeFlag(false);
        equipmentBRInfo.setStartLotsNotifyRequiredFlag(false);
        equipmentBRInfo.setMonitorCreationFlag(false);
        equipmentBRInfo.setEqpToEqpTransferFlag(false);
        equipmentBRInfo.setTakeInOutTransferFlag(false);
        equipmentBRInfo.setEmptyCassetteRequireFlag(false);
        equipmentBRInfo.setFmcCapabilityFlag(false);
        equipmentBRInfo.setFmcSwitch("OFF");
        Params.EqpInfoInqParams eqpInfoInqParams = new Params.EqpInfoInqParams();
        eqpInfoInqParams.setEquipmentID(equipmentID);
        eqpInfoInqParams.setRequestFlagForBasicInfo(true);
        eqpInfoInqParams.setRequestFlagForStatusInfo(true);
        eqpInfoInqParams.setRequestFlagForPMInfo(false);
        eqpInfoInqParams.setRequestFlagForPortInfo(true);
        eqpInfoInqParams.setRequestFlagForChamberInfo(false);
        eqpInfoInqParams.setRequestFlagForStockerInfo(false);
        eqpInfoInqParams.setRequestFlagForInprocessingLotInfo(false);
        eqpInfoInqParams.setRequestFlagForReservedControlJobInfo(false);
        eqpInfoInqParams.setRequestFlagForRSPPortInfo(false);
        eqpInfoInqParams.setRequestFlagForEqpContainerInfo(false);
        Results.EqpInfoInqResult eqpInfoInqResult = equipmentInqService.sxEqpInfoInq(objCommonIn, eqpInfoInqParams);

        //Check Online Mode and Available Flag
        List<Infos.EqpPortStatus> eqpPortStatusList = eqpInfoInqResult.getEquipmentPortInfo().getEqpPortStatuses();
        for (Infos.EqpPortStatus portStatus : eqpPortStatusList) {
            if (CimStringUtils.equals(BizConstant.SP_EQP_ONLINEMODE_OFFLINE, portStatus.getOnlineMode())) {
                log.info("onlineMode == [Offline]");
                Validations.check(retCodeConfig.getInvalidEquipmentMode(), ObjectIdentifier.fetchValue(equipmentID),
                        portStatus.getOnlineMode());
            } else {
                log.info("onlineMode is OK");
            }
        }
        String takeOutXferInNotAvailableState = StandardProperties.OM_XFER_TAKEOUT_IN_NOT_AVAIL_STATE.getValue();
        if (CimBooleanUtils.isFalse(eqpInfoInqResult.getEquipmentStatusInfo().getEquipmentAvailableFlag())
                && !CimStringUtils.equals("1", takeOutXferInNotAvailableState)) {
            throw new ServiceException(new OmCode(retCodeConfig.getInvalidEquipmentStatus(), equipmentID.getValue(), "NotAvailable"));
        }

        // 【Step-3】: equipment_portInfo_SortByGroup
        Infos.EqpPortInfoOrderByGroup eqpPortInfoOrderByGroup = equipmentMethod.equipmentPortInfoSortByGroup(objCommonIn, equipmentID, eqpPortStatusList);

        // 【Step-4】: equipment_targetPort_Pickup
        //converter.Convert120_to_080( strEqpInfoInqResult.equipmentBRInfo, strEqpBrInfo );
        Infos.EqpBrInfo eqpBrInfo = eqpInfoInqResult.getEquipmentBasicInfo();
        Outputs.EquipmentTargetPortPickupOut equipmentTargetPortPickupOut = equipmentMethod.equipmentTargetPortPickup(objCommonIn, eqpPortInfoOrderByGroup, eqpBrInfo, eqpInfoInqResult.getEquipmentPortInfo());

        List<Infos.PortGroup> targetPortGroupList = equipmentTargetPortPickupOut.getEqpTargetPortInfo().getPortGroups();
        int lenPortGroup = CimArrayUtils.getSize(targetPortGroupList);

        if (lenPortGroup == 0) {//0 == lenPortGroup
            log.info(" 0 == lenPortGroup");
            throw new ServiceException(retCodeConfig.getNotFoundTargetPort());
        }

        ObjectIdentifier dummy = new ObjectIdentifier();
        int nPortLen;
        String tmpAPCIFControlStatus = null;
        String APCIFControlStatus;

        Boolean bLoadReqFlag = true;

        // Unload Request
        if (CimStringUtils.equals(equipmentTargetPortPickupOut.getTargetPortType(), BizConstant.SP_PORTRSC_PORTSTATE_UNLOADREQ)) {
            bLoadReqFlag = false;
            //Check Lot Process State
            nPortLen = CimArrayUtils.getSize(eqpPortStatusList);
            Infos.PortGroup zeroTargetGroup = targetPortGroupList.get(0);
            for (int i = 0; i < nPortLen; i++) {
                Infos.EqpPortStatus iEqpPortStatus = eqpPortStatusList.get(i);
                if (CimStringUtils.equals(iEqpPortStatus.getPortGroup(), zeroTargetGroup.getPortGroup())) {
                    int nLotOnPortLen = CimArrayUtils.getSize(iEqpPortStatus.getLotOnPorts());
                    for (int j = 0; j < nLotOnPortLen; j++) {
                        //【Step-5】: lot_processState_Get
                        String theLotProcessState = lotMethod.lotProcessStateGet(objCommonIn,
                                iEqpPortStatus.getLotOnPorts().get(j).getLotID());
                        if (CimStringUtils.equals(theLotProcessState, BizConstant.SP_LOT_PROCSTATE_PROCESSING)) {
                            log.info("strLot_processState_Get_out.theLotProcessState == SP_Lot_ProcState_Processing");
                            bLoadReqFlag = true;
                            break;
                        }
                    }
                } else {
                    log.debug("NoCheck Ignored");
                }
            }

            // Check eqp availability of TakeOutIn Transfer;
            // 【Step-6】: equipment_TakeOutInMode_Check;
            Long eqpTakeOutInSupport = equipmentMethod.equipmentTakeOutInModeCheck(objCommonIn, equipmentID);

            if (CimBooleanUtils.isFalse(bLoadReqFlag)) {
                if (CimBooleanUtils.isTrue(eqpInfoInqResult.getEquipmentBasicInfo().isEqpToEqpTransferFlag())) {
                    //get whereNextTransferEqp
                    // 【Step-7】: whereNextTransferEqp
                    Boolean whereNextOKFlag = true;
                    Outputs.WhereNextTransferEqpOut whereNextTransferEqpOut = null;
                    try {
                        whereNextTransferEqpOut = whereNextMethod.whereNextTransferEqp(objCommonIn, equipmentID, equipmentTargetPortPickupOut);
                    } catch (ServiceException e) {
                        whereNextOKFlag = false;
                        whereNextTransferEqpOut = (Outputs.WhereNextTransferEqpOut) e.getData();
                        log.info("EQP to EQP Xfer is interrupted, and change for the Stocker Xfer");
                    }

                    if (CimBooleanUtils.isTrue(whereNextOKFlag)){
                        int tmpFPCAdoptFlag = StandardProperties.OM_DOC_ENABLE_FLAG.getIntValue();

                        if (1 == tmpFPCAdoptFlag) {
                            log.debug("DOC adopt Flag is ON");
                            // 【Step-8】: FPCStartCassetteInfo_Exchange
                            String exchangeType = BizConstant.SP_FPC_EXCHANGETYPE_STARTRESERVEINFO;
                            List<Infos.StartCassette> strStartCassette = fpcMethod.fpcStartCassetteInfoExchange(objCommonIn, exchangeType, equipmentID, whereNextTransferEqpOut.getStartCassetteList());
                            whereNextTransferEqpOut.setStartCassetteList(strStartCassette);
                        } else {
                            log.debug("DOC adopt Flag is OFF");
                        }
                        log.info("Set processJobExecFlag based on wafer sampling setting. ");
                        Boolean notSendIgnoreMail = false;
                        OmCode startCassette_processJobExecFlag_SetRc = retCodeConfig.getSucc();
                        //【step9】 - startCassette_processJobExecFlag_Set__090
                        Outputs.ObjStartCassetteProcessJobExecFlagSetOut startCassetteProcessJobExecFlagSetOutRetCode = null;
                        try {
                            startCassetteProcessJobExecFlagSetOutRetCode = startCassetteMethod.startCassetteProcessJobExecFlagSet(objCommonIn, whereNextTransferEqpOut.getStartCassetteList(), whereNextTransferEqpOut.getEquipmentID());
                        } catch (ServiceException ex) {
                            startCassetteProcessJobExecFlagSetOutRetCode = ex.getData(Outputs.ObjStartCassetteProcessJobExecFlagSetOut.class);
                            if (!Validations.isEquals(retCodeConfig.getNoSmplSetting(), ex.getCode())) {
                                // When errors have occurred, hold all the error lot and send e-mail to notify that.
                                notSendIgnoreMail = true;
                                startCassette_processJobExecFlag_SetRc = retCodeConfig.getInvalidSmplSetting();
                            }
                        }
                        int mailLen = CimArrayUtils.getSize(startCassetteProcessJobExecFlagSetOutRetCode.getSamplingMessage());
                        //hold lots and send E-mails if error
                        for (int mailCnt = 0; mailCnt < mailLen; mailCnt++) {
                            if (startCassetteProcessJobExecFlagSetOutRetCode.getSamplingMessage().get(mailCnt).getMessageType() == BizConstant.SP_SAMPLING_ERROR_MAIL
                                    || startCassetteProcessJobExecFlagSetOutRetCode.getSamplingMessage().get(mailCnt).getMessageType() == BizConstant.SP_SAMPLING_WARN_MAIL) {
                                log.info("messageType == SP_Sampling_error_mail || SP_Sampling_warn_mail ");
                                // Lot Hold
                                //【step10】 lot_currentOperationInfo_Get
                                Outputs.ObjLotCurrentOperationInfoGetOut lotCurrentOperationInfoRetCode = lotMethod.lotCurrentOperationInfoGet(objCommonIn, startCassetteProcessJobExecFlagSetOutRetCode.getSamplingMessage().get(mailCnt).getLotID());

                                //【step11】 txHoldLotReq
                                Infos.LotHoldReq lotHoldReq = new Infos.LotHoldReq();
                                lotHoldReq.setHoldType(BizConstant.SP_HOLDTYPE_LOTHOLD);
                                lotHoldReq.setHoldReasonCodeID(new ObjectIdentifier(BizConstant.SP_REASON_WAFERSAMPLINGHOLD));
                                lotHoldReq.setHoldUserID(objCommonIn.getUser().getUserID());
                                lotHoldReq.setResponsibleOperationMark(BizConstant.SP_RESPONSIBLEOPERATION_CURRENT);
                                lotHoldReq.setRouteID(lotCurrentOperationInfoRetCode.getRouteID());
                                lotHoldReq.setOperationNumber(lotCurrentOperationInfoRetCode.getOperationNumber());
                                lotHoldReq.setClaimMemo("");
                                List<Infos.LotHoldReq> holdReqList = new ArrayList<Infos.LotHoldReq>();
                                holdReqList.add(lotHoldReq);
                                lotService.sxHoldLotReq(objCommonIn, startCassetteProcessJobExecFlagSetOutRetCode.getSamplingMessage().get(mailCnt).getLotID(), holdReqList);
                            }
                            //error Mail Set
                            if (CimBooleanUtils.isFalse(notSendIgnoreMail) || BizConstant.SP_SAMPLING_IGNORED_MAIL != startCassetteProcessJobExecFlagSetOutRetCode.getSamplingMessage().get(mailCnt).getMessageType()) {
                                log.info("notSendIgnoreMail == FALSE ||  strStartCassette_processJobExecFlag_Set_out__090.strSamplingMessage[mailCnt].messageType != SP_Sampling_ignored_mail");
                                //When notSendIgnoreMail == TRUE, just the Lot Hold mail is to be sent.
                                //【step12】 - lot_samplingMessage_Create
                                String messageText = "";
                                if (BizConstant.SP_SAMPLING_WARN_MAIL == startCassetteProcessJobExecFlagSetOutRetCode.getSamplingMessage().get(mailCnt).getMessageType()) {
                                    //-------------------------------------------------------------------
                                    // create mail text if message type == SP_Sampling_warn_mail;
                                    // startCassette_processJobExecFlag_Set__090 returns just error text. Caller should switch text handling.
                                    //-------------------------------------------------------------------
                                    messageText = lotMethod.lotSamplingMessageCreate(objCommonIn,
                                            startCassetteProcessJobExecFlagSetOutRetCode.getSamplingMessage().get(mailCnt).getLotID(),
                                            BizConstant.SP_SAMPLING_ERROR_MAIL,
                                            startCassetteProcessJobExecFlagSetOutRetCode.getSamplingMessage().get(mailCnt).getMessageText());
                                } else {
                                    messageText = startCassetteProcessJobExecFlagSetOutRetCode.getSamplingMessage().get(mailCnt).getMessageText();
                                }
                                //Set System Message
                                Infos.SysMsgStockInfo sysMsg = new Infos.SysMsgStockInfo();
                                sysMsg.setSubSystemID(BizConstant.SP_SUBSYSTEMID_MM);
                                sysMsg.setSystemMessageCode(BizConstant.SP_SYSTEMMSGCODE_SMPLERR);
                                sysMsg.setSystemMessageText(messageText);
                                sysMsg.setNotifyFlag(true);
                                sysMsg.setEquipmentID(whereNextTransferEqpOut.getEquipmentID());
                                sysMsg.setEquipmentStatus("");
                                sysMsg.setStockerID(dummy);
                                sysMsg.setStockerStatus("");
                                sysMsg.setAGVID(dummy);
                                sysMsg.setAGVStatus("");
                                sysMsg.setLotID(startCassetteProcessJobExecFlagSetOutRetCode.getSamplingMessage().get(mailCnt).getLotID());
                                sysMsg.setLotStatus("");
                                sysMsg.setRouteID(dummy);
                                sysMsg.setOperationID(dummy);
                                sysMsg.setOperationNumber("");
                                sysMsg.setSystemMessageTimeStamp(CimDateUtils.convertToSpecString(objCommonIn.getTimeStamp().getReportTimeStamp()));
                                List<Infos.SysMsgStockInfo> sysMstStockInfoList = new ArrayList<>();
                                sysMstStockInfoList.add(sysMsg);
                                out.setSysMstStockInfoList(sysMstStockInfoList);
                            }
                        }
                        if (CimBooleanUtils.isTrue(notSendIgnoreMail)) {
                            log.info("notSendIgnoreMail == TRUE  return this TX.");
                            if (Validations.isEquals(startCassette_processJobExecFlag_SetRc, retCodeConfig.getSucc())){
                                return out;
                            } else {
                                throw new ServiceException(startCassette_processJobExecFlag_SetRc,out);
                            }
                        }
                        //valid check of processJobExecFlag for each lot.
                        //Check every lot has at least one wafer with processJobExecFlag == TRUE
                        //【step13】 - lot_processJobExecFlag_ValidCheck
                        Outputs.ObjLotProcessJobExecFlagValidCheckOut lotProcessJobExecFlagValidCheckOutRetCode = null;
                        try {
                            lotProcessJobExecFlagValidCheckOutRetCode = lotMethod.lotProcessJobExecFlagValidCheck(objCommonIn, startCassetteProcessJobExecFlagSetOutRetCode.getStartCassettes());
                        } catch (ServiceException e) {
                            lotProcessJobExecFlagValidCheckOutRetCode = e.getData(Outputs.ObjLotProcessJobExecFlagValidCheckOut.class);
                            if (!Validations.isEquals(retCodeConfig.getNoProcessJobExecFlag(), e.getCode())) {
                                log.info("lot_processJobExecFlag_ValidCheck() != RC_OK && != RC_NO_PROCESSJOBEXECFLAG");
                                e.setData(out);
                                throw e;
                            }
                            if (Validations.isEquals(retCodeConfig.getNoProcessJobExecFlag(), e.getCode())) {
                                log.info("objLot_processJobExecFlag_ValidCheck() rc == RC_NO_PROCESSJOBEXECFLAG");
                                // Lot Hold + error mail
                                // create hold mail message
                                //【step14】 - lot_samplingMessage_Create
                                String message = null;
                                try {
                                    message = lotMethod.lotSamplingMessageCreate(objCommonIn,
                                            lotProcessJobExecFlagValidCheckOutRetCode.getLotID(),
                                            BizConstant.SP_SAMPLING_ERROR_MAIL,
                                            e.getMessage());
                                } catch (ServiceException ex) {
                                    ex.setData(out);
                                    throw ex;
                                }

                                //【step15】 - lot_currentOperationInfo_Get
                                Outputs.ObjLotCurrentOperationInfoGetOut lotCurrentOperationInfoGetOutRetCode = null;
                                try {
                                    lotCurrentOperationInfoGetOutRetCode = lotMethod.lotCurrentOperationInfoGet(objCommonIn, lotProcessJobExecFlagValidCheckOutRetCode.getLotID());
                                } catch (ServiceException ex) {
                                    ex.setData(out);
                                    throw ex;
                                }

                                Infos.LotHoldReq lotHoldReq = new Infos.LotHoldReq();
                                lotHoldReq.setHoldType(BizConstant.SP_HOLDTYPE_LOTHOLD);
                                lotHoldReq.setHoldReasonCodeID(new ObjectIdentifier(BizConstant.SP_REASON_WAFERSAMPLINGHOLD));
                                lotHoldReq.setHoldUserID(objCommonIn.getUser().getUserID());
                                lotHoldReq.setResponsibleOperationMark(BizConstant.SP_RESPONSIBLEOPERATION_CURRENT);
                                lotHoldReq.setRouteID(lotCurrentOperationInfoGetOutRetCode.getRouteID());
                                lotHoldReq.setOperationNumber(lotCurrentOperationInfoGetOutRetCode.getOperationNumber());
                                lotHoldReq.setClaimMemo("");
                                List<Infos.LotHoldReq> holdReqList = new ArrayList<Infos.LotHoldReq>();
                                holdReqList.add(lotHoldReq);
                                //【step16】 - txHoldLotReq
                                try {
                                    lotService.sxHoldLotReq(objCommonIn, lotProcessJobExecFlagValidCheckOutRetCode.getLotID(), holdReqList);
                                } catch (ServiceException ex) {
                                    ex.setData(out);
                                    throw ex;
                                }
                                //error Mail Set
                                //Set System Message
                                Infos.SysMsgStockInfo sysMsg = new Infos.SysMsgStockInfo();
                                sysMsg.setSubSystemID(BizConstant.SP_SUBSYSTEMID_MM);
                                sysMsg.setSystemMessageCode(BizConstant.SP_SYSTEMMSGCODE_SMPLERR);
                                sysMsg.setSystemMessageText(message);
                                sysMsg.setNotifyFlag(true);
                                sysMsg.setEquipmentID(whereNextTransferEqpOut.getEquipmentID());
                                sysMsg.setEquipmentStatus("");
                                sysMsg.setStockerID(dummy);
                                sysMsg.setStockerStatus("");
                                sysMsg.setAGVID(dummy);
                                sysMsg.setAGVStatus("");
                                sysMsg.setLotID(lotProcessJobExecFlagValidCheckOutRetCode.getLotID());
                                sysMsg.setLotStatus("");
                                sysMsg.setRouteID(dummy);
                                sysMsg.setOperationID(dummy);
                                sysMsg.setOperationNumber("");
                                sysMsg.setSystemMessageTimeStamp(CimDateUtils.convertToSpecString(objCommonIn.getTimeStamp().getReportTimeStamp()));
                                //List<Infos.SysMsgStockInfo> sysMstStockInfoList = new ArrayList<>();
                                List<Infos.SysMsgStockInfo> sysMstStockInfoList = CimArrayUtils.getSize(out.getSysMstStockInfoList()) == 0 ? new ArrayList<>() : out.getSysMstStockInfoList();
                                sysMstStockInfoList.add(sysMsg);
                                out.setSysMstStockInfoList(sysMstStockInfoList);
                                throw new ServiceException(retCodeConfig.getNoProcessJobExecFlag(), out);
                            }
                        }

                        whereNextTransferEqpOut.setStartCassetteList(startCassetteProcessJobExecFlagSetOutRetCode.getStartCassettes());
                        log.info("Set processJobExecFlag based on wafer sampling setting end. ");

                        //Request to Start Lot Reservation
                        //【step17】 - txMoveInReserveReq
                        Infos.ObjCommon strTmpObjCommon = objCommonIn;
                        strTmpObjCommon.setTransactionID("ODISW001");
                        Params.MoveInReserveReqParams params = new Params.MoveInReserveReqParams();
                        params.setEquipmentID(whereNextTransferEqpOut.getEquipmentID());
                        params.setPortGroupID(whereNextTransferEqpOut.getPortGroup());
                        params.setControlJobID(new ObjectIdentifier());
                        params.setStartCassetteList(whereNextTransferEqpOut.getStartCassetteList());
                        params.setApcIFControlStatus(tmpAPCIFControlStatus);
                        Results.MoveInReserveReqResult moveInReserveReqResultRetCode = null;
                        try {
                            moveInReserveReqResultRetCode = (Results.MoveInReserveReqResult) dispatchService.sxMoveInReserveReq(strTmpObjCommon, params);
                        } catch (ServiceException ex) {
                            log.info("txMoveInReserveReq() rc != RC_OK");
                            //---------------------------------------------------------------------------------------------
                            //  Prepare message text string for case of failure of start lot reservation.
                            //  If start lot reservation for equipment is fail, this messageText is filled.
                            //---------------------------------------------------------------------------------------------
                            //Prepare e-mail Message Text
                            StringBuilder msg = new StringBuilder("<<< MoveInReservation Error!  (EQP -> EQP) >>>\n");
                            msg.append("\n")
                                    .append("Equipment ID         : ").append(ObjectIdentifier.fetchValue(whereNextTransferEqpOut.getEquipmentID())).append("\n")
                                    .append("Lot IDs              : ");
                            boolean bLotSet = false;
                            for (int i = 0; i < CimArrayUtils.getSize(whereNextTransferEqpOut.getStartCassetteList()); i++) {
                                for (int j = 0; j < CimArrayUtils.getSize(whereNextTransferEqpOut.getStartCassetteList().get(i).getLotInCassetteList()); j++) {
                                    if (!ObjectIdentifier.isEmptyWithValue(whereNextTransferEqpOut.getStartCassetteList().get(i).getLotInCassetteList().get(j).getLotID())){
                                        if (CimBooleanUtils.isTrue(bLotSet)){
                                            msg.append(", ");
                                        }
                                        msg.append(ObjectIdentifier.fetchValue(whereNextTransferEqpOut.getStartCassetteList().get(i).getLotInCassetteList().get(j).getLotID()));
                                        bLotSet = true;
                                    }
                                }
                            }
                            msg.append("\n")
                                    .append("Transaction ID       : ").append(ex.getTransactionID()).append("\n")
                                    .append("Return Code          : ").append(ex.getCode()).append("\n")
                                    .append("Message Text         : ").append(ex.getMessage()).append("\n")
                                    .append("Reason Text          : ").append(ex.getReasonText()).append("\n");
                            Infos.SysMsgStockInfo sysMsg = new Infos.SysMsgStockInfo();
                            sysMsg.setSubSystemID(BizConstant.SP_SUBSYSTEMID_MM);
                            sysMsg.setSystemMessageCode(BizConstant.SP_SYSTEMMSGCODE_DELIVERYERROR);
                            sysMsg.setSystemMessageText(msg.toString());
                            sysMsg.setNotifyFlag(true);
                            sysMsg.setEquipmentID(equipmentID);
                            sysMsg.setEquipmentStatus("");
                            sysMsg.setStockerID(dummy);
                            sysMsg.setStockerStatus("");
                            sysMsg.setAGVID(dummy);
                            sysMsg.setAGVStatus("");
                            sysMsg.setLotID(dummy);
                            sysMsg.setLotStatus("");
                            sysMsg.setRouteID(dummy);
                            sysMsg.setOperationID(dummy);
                            sysMsg.setOperationNumber("");
                            sysMsg.setSystemMessageTimeStamp(CimDateUtils.convertToSpecString(objCommonIn.getTimeStamp().getReportTimeStamp()));
                            List<Infos.SysMsgStockInfo> sysMstStockInfoList = CimArrayUtils.getSize(out.getSysMstStockInfoList()) == 0 ? new ArrayList<>() : out.getSysMstStockInfoList();
                            sysMstStockInfoList.add(sysMsg);
                            out.setSysMstStockInfoList(sysMstStockInfoList);
                            ex.setData(out);
                            throw ex;
                        }
                        log.info("txMoveInReserveReq() == RC_OK");
                        //Make txMultipleCarrierTransferReq parameter
                        //【step18】 - multiCarrierXferFillInOTMSW005InParm
                        Outputs.ObjMultiCarrierXferFillInOTMSW005InParmOut multiCarrierXferFillInOTMSW005InParmRetCode = null;
                        try {
                            multiCarrierXferFillInOTMSW005InParmRetCode = cassetteMethod.multiCarrierXferFillInOTMSW005InParm(objCommonIn,
                                    whereNextTransferEqpOut.getEquipmentID(), whereNextTransferEqpOut.getStartCassetteList());
                        } catch (ServiceException e) {
                            log.info("multiCarrierXferFillInOTMSW005InParm() rc != RC_OK");
                            //Request to Start Lot Reservation Cancel
                            //【step19】 - txMoveInReserveCancelReq
                            Params.MoveInReserveCancelReqParams in = new Params.MoveInReserveCancelReqParams();
                            in.setEquipmentID(whereNextTransferEqpOut.getEquipmentID());
                            in.setControlJobID(moveInReserveReqResultRetCode.getControlJobID());
                            try {
                                Results.MoveInReserveCancelReqResult moveInReserveCancelReqResultRetCode = dispatchService.sxMoveInReserveCancelReqService(objCommonIn, in);
                            } catch (ServiceException ex) {
                                log.info("txMoveInReserveCancelReq != RC_OK");
                            }
                            e.setData(out);
                            throw e;
                        }
                        // TakeOutIn transfer
                        //Check takeOutIn transfer function enable. (EQP -> EQP)
                        if (eqpTakeOutInSupport == 1) {
                            log.info("TakeOutIn enable for this equipment. Call txLotCarrierTOTIReq() ");
                            //【step20】 - txLotCarrierTOTIReq
                            Params.LotCarrierTOTIReqParam lotCarrierTOTIReqParam = new Params.LotCarrierTOTIReqParam();
                            lotCarrierTOTIReqParam.setEqpToEqpFlag(true);
                            lotCarrierTOTIReqParam.setEquipmentID(equipmentID);
                            lotCarrierTOTIReqParam.setControlJobID(moveInReserveReqResultRetCode.getControlJobID());
                            lotCarrierTOTIReqParam.setStrEqpTargetPortInfo(equipmentTargetPortPickupOut.getEqpTargetPortInfo());
                            lotCarrierTOTIReqParam.setStrCarrierXferReqSeq(multiCarrierXferFillInOTMSW005InParmRetCode.getStrCarrierXferReq());
                            Results.LotCarrierTOTIReqResult lotCarrierTOTIReqResultRetCode = null;
                            try {
                                lotCarrierTOTIReqResultRetCode = this.sxLotCarrierTOTIReq(objCommonIn, lotCarrierTOTIReqParam, "");
                                // Set mail information even if the transaction returns error.
                                int sysMsgLen = CimArrayUtils.getSize(lotCarrierTOTIReqResultRetCode.getStrSysMsgStockInfoSeq());
                                for (int i = 0; i < sysMsgLen; i++) {
                                    List<Infos.SysMsgStockInfo> sysMstStockInfoList = CimArrayUtils.getSize(out.getSysMstStockInfoList()) == 0 ? new ArrayList<>() : out.getSysMstStockInfoList();
                                    sysMstStockInfoList.add(lotCarrierTOTIReqResultRetCode.getStrSysMsgStockInfoSeq().get(i));
                                    out.setSysMstStockInfoList(sysMstStockInfoList);
                                }
                            } catch (ServiceException e) {
                                lotCarrierTOTIReqResultRetCode = e.getData(Results.LotCarrierTOTIReqResult.class);
                                if (!CimObjectUtils.isEmpty(lotCarrierTOTIReqResultRetCode)){
                                    int sysMsgLen = CimArrayUtils.getSize(lotCarrierTOTIReqResultRetCode.getStrSysMsgStockInfoSeq());
                                    for (int i = 0; i < sysMsgLen; i++) {
                                        List<Infos.SysMsgStockInfo> sysMstStockInfoList = CimArrayUtils.getSize(out.getSysMstStockInfoList()) == 0 ? new ArrayList<>() : out.getSysMstStockInfoList();
                                        sysMstStockInfoList.add(lotCarrierTOTIReqResultRetCode.getStrSysMsgStockInfoSeq().get(i));
                                        out.setSysMstStockInfoList(sysMstStockInfoList);
                                    }
                                }
                                e.setData(out);
                                throw e;
                            }
                            //Return to Main
                            return out;
                        } else {
                            //Send Transfer Request to XM. (EQP -> EQP)
                            //【step21】 - txMultipleCarrierTransferReq
                            try {
                                this.sxMultipleCarrierTransferReq(objCommonIn,
                                        false, "S", multiCarrierXferFillInOTMSW005InParmRetCode.getStrCarrierXferReq());
                            } catch (ServiceException e) {
                                log.info("txMultipleCarrierTransferReq() rc != RC_OK");
                                //Prepare e-mail Message Text
                                StringBuilder msg = new StringBuilder("<<< Transfer Error!  (EQP -> EQP) >>>\n");
                                msg.append("\n")
                                        .append("Equipment ID         : ").append(ObjectIdentifier.fetchValue(equipmentID)).append("\n");
                                for (int i = 0; i < CimArrayUtils.getSize(multiCarrierXferFillInOTMSW005InParmRetCode.getStrCarrierXferReq()); i++) {
                                    Infos.CarrierXferReq carrierXferReq = multiCarrierXferFillInOTMSW005InParmRetCode.getStrCarrierXferReq().get(i);
                                    msg.append("-----------------------------------------\n")
                                            .append("carrier ID           : ").append(ObjectIdentifier.fetchValue(carrierXferReq.getCarrierID())).append("\n")
                                            .append("Lot ID               : ").append(ObjectIdentifier.fetchValue(carrierXferReq.getLotID())).append("\n")
                                            .append("From Machine ID      : ").append(ObjectIdentifier.fetchValue(carrierXferReq.getFromMachineID())).append("\n")
                                            .append("From Port ID         : ").append(ObjectIdentifier.fetchValue(carrierXferReq.getFromPortID())).append("\n")
                                            .append("To Stocker Group     : ").append(carrierXferReq.getToStockerGroup()).append("\n");
                                    int toMachineLen = CimArrayUtils.getSize(carrierXferReq.getStrToMachine());
                                    if (toMachineLen == 0){
                                        msg.append("To Machine ID        : Nothing\n");
                                    }
                                    for (int j = 0; j < toMachineLen; j++) {
                                        msg.append("To Machine ID        : ").append(ObjectIdentifier.fetchValue(carrierXferReq.getStrToMachine().get(j).getToMachineID())).append("\n")
                                                .append("To Port ID           : ").append(ObjectIdentifier.fetchValue(carrierXferReq.getStrToMachine().get(j).getToPortID())).append("\n");
                                    }
                                }
                                msg.append("-----------------------------------------\n")
                                        .append("Transaction ID       : ").append(e.getTransactionID()).append("\n")
                                        .append("Return Code          : ").append(e.getCode()).append("\n")
                                        .append("Message Text         : ").append(e.getMessage()).append("\n")
                                        .append("Reason Text          : ").append(e.getReasonText()).append("\n");
                                //Set System Message
                                Infos.SysMsgStockInfo sysMsg = new Infos.SysMsgStockInfo();
                                sysMsg.setSubSystemID(BizConstant.SP_SUBSYSTEMID_MM);
                                sysMsg.setSystemMessageCode(BizConstant.SP_SYSTEMMSGCODE_DELIVERYERROR);
                                sysMsg.setSystemMessageText(msg.toString());// up on the msg but not add
                                sysMsg.setNotifyFlag(true);
                                sysMsg.setEquipmentID(equipmentID);
                                sysMsg.setEquipmentStatus("");
                                sysMsg.setStockerID(dummy);
                                sysMsg.setStockerStatus("");
                                sysMsg.setAGVID(dummy);
                                sysMsg.setAGVStatus("");
                                sysMsg.setLotID(dummy);
                                sysMsg.setLotStatus("");
                                sysMsg.setRouteID(dummy);
                                sysMsg.setOperationID(dummy);
                                sysMsg.setOperationNumber("");
                                sysMsg.setSystemMessageTimeStamp(CimDateUtils.convertToSpecString(objCommonIn.getTimeStamp().getReportTimeStamp()));
                                List<Infos.SysMsgStockInfo> sysMstStockInfoList = CimArrayUtils.getSize(out.getSysMstStockInfoList()) == 0 ? new ArrayList<>() : out.getSysMstStockInfoList();
                                sysMstStockInfoList.add(sysMsg);
                                out.setSysMstStockInfoList(sysMstStockInfoList);
                                //RetCode<Results.CarrierTransferReqResult> saveRC = result;
                                //Request to Start Lot Reservation Cancel
                                //【step22】 - txMoveInReserveCancelReq
                                Params.MoveInReserveCancelReqParams moveInReserveCancelReqParams = new Params.MoveInReserveCancelReqParams();
                                moveInReserveCancelReqParams.setEquipmentID(whereNextTransferEqpOut.getEquipmentID());
                                moveInReserveCancelReqParams.setControlJobID(moveInReserveReqResultRetCode.getControlJobID());
                                try {
                                    Results.MoveInReserveCancelReqResult moveInReserveCancelReqResultRetCode = dispatchService.sxMoveInReserveCancelReqService(objCommonIn, moveInReserveCancelReqParams);
                                } catch (ServiceException ex) {
                                    log.info("txMoveInReserveCancelReq != RC_OK");
                                }
                                e.setData(out);
                                throw e;
                            }
                            /*--------------------*/
                            /*   Return to Main   */
                            /*--------------------*/
                            return out;
                        }
                    }
                }
            }
            if (CimBooleanUtils.isFalse(bLoadReqFlag)) {
                List<Infos.CarrierXferReq> tmpCarrierXferReqSeq = new ArrayList<>();
                int nILen = CimArrayUtils.getSize(equipmentTargetPortPickupOut.getEqpTargetPortInfo().getPortGroups().get(0).getStrPortID());
                for (int i = 0; i < nILen; i++) {
                    //Get Where Next Stocker
                    //Call RTD Interface
                    //【step23】 - cassetteDelivery_RTDInterfaceReq
                    Results.WhereNextStockerInqResult strWhereNextStockerInqResult = null;
                    Outputs.ObjCassetteDeliveryRTDInterfaceReqOut cassetteDeliveryRTDInterfaceReqOutRetCode = null;
                    try {
                        cassetteDeliveryRTDInterfaceReqOutRetCode = cassetteMethod.cassetteDeliveryRTDInterfaceReq(objCommonIn,
                                BizConstant.SP_RTD_FUNCTION_CODE_WHERENEXT, equipmentTargetPortPickupOut.getEqpTargetPortInfo().getPortGroups().get(0).getStrPortID().get(i).getCassetteID());
                        strWhereNextStockerInqResult = cassetteDeliveryRTDInterfaceReqOutRetCode == null ? null : cassetteDeliveryRTDInterfaceReqOutRetCode.getStrWhereNextStockerInqResult();
                    } catch (ServiceException e) {
                        if (!Validations.isEquals(retCodeConfigEx.getRtdInterfaceSwitchOff(), e.getCode())
                                && !Validations.isEquals(retCodeConfigEx.getNotFoundRTD(), e.getCode())) {
                            StringBuilder msg = new StringBuilder("<<< RTD Interface Error!  (WhereNext of CassetteDelivery) >>>\n");
                            msg.append("\n")
                                    .append("Equipment ID         : ").append(ObjectIdentifier.fetchValue(equipmentID)).append("\n")
                                    .append("-----------------------------------------\n")
                                    .append("Transaction ID       : ").append(e.getTransactionID()).append("\n")
                                    .append("Return Code          : ").append(e.getCode()).append("\n").append(e.getMessage()).append("\n")
                                    .append("Reason Text          : ").append(e.getReasonText()).append("\n");
                            //Set System Message
                            Infos.SysMsgStockInfo sysMsg = new Infos.SysMsgStockInfo();
                            sysMsg.setSubSystemID(BizConstant.SP_SUBSYSTEMID_MM);
                            sysMsg.setSystemMessageCode(BizConstant.SP_SYSTEMMSGCODE_RTDERROR);
                            sysMsg.setSystemMessageText(msg.toString());// up on the msg but not add
                            sysMsg.setNotifyFlag(true);
                            sysMsg.setEquipmentID(equipmentID);
                            sysMsg.setEquipmentStatus("");
                            sysMsg.setStockerID(dummy);
                            sysMsg.setStockerStatus("");
                            sysMsg.setAGVID(dummy);
                            sysMsg.setAGVStatus("");
                            sysMsg.setLotID(dummy);
                            sysMsg.setLotStatus("");
                            sysMsg.setRouteID(dummy);
                            sysMsg.setOperationID(dummy);
                            sysMsg.setOperationNumber("");
                            sysMsg.setSystemMessageTimeStamp(CimDateUtils.convertToSpecString(objCommonIn.getTimeStamp().getReportTimeStamp()));
                            List<Infos.SysMsgStockInfo> sysMstStockInfoList = CimArrayUtils.getSize(out.getSysMstStockInfoList()) == 0 ? new ArrayList<>() : out.getSysMstStockInfoList();
                            sysMstStockInfoList.add(sysMsg);
                            out.setSysMstStockInfoList(sysMstStockInfoList);
                        }
                        //Call Normal Function (txWhereNextStockerInq)
                        //【step24】 - txWhereNextStockerInq
                        try {
                            strWhereNextStockerInqResult = transferManagementSystemInqService.sxWhereNextStockerInq(objCommonIn,
                                    dummy,
                                    equipmentTargetPortPickupOut.getEqpTargetPortInfo().getPortGroups().get(0).getStrPortID().get(i).getCassetteID());
                        } catch (ServiceException ex) {
                            if (!Validations.isSuccess(ex.getCode())) {
                                StringBuilder msg = new StringBuilder("<<< WhereNext Error!  (EQP -> Stocker) >>>\n");
                                msg.append("\n")
                                        .append("Equipment ID         : ").append(ObjectIdentifier.fetchValue(equipmentID)).append("\n")
                                        .append("-----------------------------------------\n")
                                        .append("Transaction ID       : ").append(ex.getTransactionID()).append("\n")
                                        .append("Return Code          : ").append(ex.getCode()).append("\n")
                                        .append("Message Text         : ").append(ex.getMessage()).append("\n")
                                        .append("Reason Text          : ").append(ex.getReasonText()).append("\n");
                                //Set System Message
                                Infos.SysMsgStockInfo sysMsg = new Infos.SysMsgStockInfo();
                                sysMsg.setSubSystemID(BizConstant.SP_SUBSYSTEMID_MM);
                                sysMsg.setSystemMessageCode(BizConstant.SP_SYSTEMMSGCODE_DELIVERYERROR);
                                sysMsg.setSystemMessageText(msg.toString());// up on the msg but not add
                                sysMsg.setNotifyFlag(true);
                                sysMsg.setEquipmentID(equipmentID);
                                sysMsg.setEquipmentStatus("");
                                sysMsg.setStockerID(dummy);
                                sysMsg.setStockerStatus("");
                                sysMsg.setAGVID(dummy);
                                sysMsg.setAGVStatus("");
                                sysMsg.setLotID(dummy);
                                sysMsg.setLotStatus("");
                                sysMsg.setRouteID(dummy);
                                sysMsg.setOperationID(dummy);
                                sysMsg.setOperationNumber("");
                                sysMsg.setSystemMessageTimeStamp(CimDateUtils.convertToSpecString(objCommonIn.getTimeStamp().getReportTimeStamp()));
                                List<Infos.SysMsgStockInfo> sysMstStockInfoList = CimArrayUtils.getSize(out.getSysMstStockInfoList()) == 0 ? new ArrayList<>() : out.getSysMstStockInfoList();
                                sysMstStockInfoList.add(sysMsg);
                                out.setSysMstStockInfoList(sysMstStockInfoList);
                                ex.setData(out);
                                throw ex;
                            }
                        }
                    }
                    //Make txSingleCarrierTransferReq parameter
                    //【step25】 - singleCarrierXferFillInOTMSW006InParm
                    Outputs.ObjSingleCarrierXferFillInOTMSW006InParmOut singleCarrierXferFillInOTMSW006InParmRetCode = null;
                    try {
                        singleCarrierXferFillInOTMSW006InParmRetCode = cassetteMethod.singleCarrierXferFillInOTMSW006InParm(objCommonIn,
                                equipmentID, equipmentTargetPortPickupOut.getEqpTargetPortInfo().getPortGroups().get(0).getStrPortID().get(i).getPortID(),
                                equipmentTargetPortPickupOut.getEqpTargetPortInfo().getPortGroups().get(0).getStrPortID().get(i).getCassetteID(),
                                strWhereNextStockerInqResult);
                    } catch (ServiceException e) {
                        if (Validations.isEquals(retCodeConfigEx.getNoXferNeeded(), e.getCode())) {
                            log.info("No Transfer Job is requested by environment variable. Proceed to the Next Port...");
                            continue;
                        } else if (Validations.isEquals(retCodeConfigEx.getNoStockerForCurrentEqp(), e.getCode())) {
                            singleCarrierXferFillInOTMSW006InParmRetCode = e.getData(Outputs.ObjSingleCarrierXferFillInOTMSW006InParmOut.class);
                            Infos.CarrierXferReq carrierXferReq = singleCarrierXferFillInOTMSW006InParmRetCode.getStrCarrierXferReq();
                            log.info("An Error is Detected. Proceed to the Next Port...");
                            //Prepare e-mail Message Text(Skip)
                            StringBuilder msg = new StringBuilder("<<< Transfer Error!  (EQP -> Stocker) >>>\n");
                            msg.append("\n")
                                    .append("Equipment ID         : ").append(ObjectIdentifier.fetchValue(equipmentID)).append("\n")
                                    .append("-----------------------------------------\n")
                                    .append("carrier ID           : ").append(ObjectIdentifier.fetchValue(carrierXferReq.getCarrierID())).append("\n")
                                    .append("Lot ID               : ").append(ObjectIdentifier.fetchValue(carrierXferReq.getLotID())).append("\n")
                                    .append("From Machine ID      : ").append(ObjectIdentifier.fetchValue(carrierXferReq.getFromMachineID())).append("\n")
                                    .append("From Port ID         : ").append(ObjectIdentifier.fetchValue(carrierXferReq.getFromPortID())).append("\n")
                                    .append("To Stocker Group     : ").append(carrierXferReq.getToStockerGroup()).append("\n");
                            int toMachineLen = CimArrayUtils.getSize(carrierXferReq.getStrToMachine());
                            if (toMachineLen == 0){
                                msg.append("To Machine ID        : Nothing\n");
                            }
                            for (int j = 0; j < toMachineLen; j++) {
                                msg.append("To Machine ID        : ").append(ObjectIdentifier.fetchValue(carrierXferReq.getStrToMachine().get(j).getToMachineID())).append("\n")
                                        .append("To Port ID           : ").append(ObjectIdentifier.fetchValue(carrierXferReq.getStrToMachine().get(j).getToPortID())).append("\n");
                            }

                            msg.append("-----------------------------------------\n")
                                    .append("Transaction ID       : ").append(e.getTransactionID()).append("\n")
                                    .append("Return Code          : ").append(e.getCode()).append("\n")
                                    .append("Message Text         : ").append(e.getMessage()).append("\n")
                                    .append("Reason Text          : ").append(e.getReasonText()).append("\n");
                            //Set System Message
                            Infos.SysMsgStockInfo sysMsg = new Infos.SysMsgStockInfo();
                            sysMsg.setSubSystemID(BizConstant.SP_SUBSYSTEMID_MM);
                            sysMsg.setSystemMessageCode(BizConstant.SP_SYSTEMMSGCODE_DELIVERYERROR);
                            sysMsg.setSystemMessageText(msg.toString());// up on the msg but not add
                            sysMsg.setNotifyFlag(true);
                            sysMsg.setEquipmentID(equipmentID);
                            sysMsg.setEquipmentStatus("");
                            sysMsg.setStockerID(dummy);
                            sysMsg.setStockerStatus("");
                            sysMsg.setAGVID(dummy);
                            sysMsg.setAGVStatus("");
                            sysMsg.setLotID(dummy);
                            sysMsg.setLotStatus("");
                            sysMsg.setRouteID(dummy);
                            sysMsg.setOperationID(dummy);
                            sysMsg.setOperationNumber("");
                            sysMsg.setSystemMessageTimeStamp(CimDateUtils.convertToSpecString(objCommonIn.getTimeStamp().getReportTimeStamp()));
                            List<Infos.SysMsgStockInfo> sysMstStockInfoList = CimArrayUtils.getSize(out.getSysMstStockInfoList()) == 0 ? new ArrayList<>() : out.getSysMstStockInfoList();
                            sysMstStockInfoList.add(sysMsg);
                            out.setSysMstStockInfoList(sysMstStockInfoList);
                            continue;
                        } else {
                            e.setData(out);
                            throw e;
                        }
                    }

                    //TakeOutIn transfer
                    //Check takeOutIn transfer function enable. (EQP -> STK)
                    if (eqpTakeOutInSupport == 1) {
                        log.info("TakeOutIn enable for this equipment. Stock Carrier Xfer information() ");
                        Infos.CarrierXferReq strCarrierXferReq = singleCarrierXferFillInOTMSW006InParmRetCode.getStrCarrierXferReq();
                        Infos.CarrierXferReq carrierXferReq = new Infos.CarrierXferReq();
                        carrierXferReq.setCarrierID(strCarrierXferReq.getCarrierID());
                        carrierXferReq.setLotID(strCarrierXferReq.getLotID());
                        carrierXferReq.setZoneType(strCarrierXferReq.getZoneType());
                        carrierXferReq.setN2PurgeFlag(strCarrierXferReq.getN2PurgeFlag());
                        carrierXferReq.setFromMachineID(strCarrierXferReq.getFromMachineID());
                        carrierXferReq.setFromPortID(strCarrierXferReq.getFromPortID());
                        carrierXferReq.setToStockerGroup(strCarrierXferReq.getToStockerGroup());
                        carrierXferReq.setStrToMachine(strCarrierXferReq.getStrToMachine());
                        carrierXferReq.setExpectedStartTime(strCarrierXferReq.getExpectedStartTime());
                        carrierXferReq.setExpectedEndTime(strCarrierXferReq.getExpectedEndTime());
                        carrierXferReq.setMandatoryFlag(strCarrierXferReq.getMandatoryFlag());
                        carrierXferReq.setPriority(strCarrierXferReq.getPriority());
                        tmpCarrierXferReqSeq.add(carrierXferReq);
                    } else {
                        //Check whether specified machine is eqp or stocker
                        Boolean isStorageFlag = false;
                        //【step26】 - equipment_statusInfo_Get__090
                        Infos.EqpStatusInfo eqpStatusInfoRetCode = null;
                        try {
                            eqpStatusInfoRetCode = equipmentMethod.equipmentStatusInfoGet(objCommonIn, singleCarrierXferFillInOTMSW006InParmRetCode.getStrCarrierXferReq().getFromMachineID());
                        } catch (ServiceException e) {
                            if (Validations.isEquals(retCodeConfig.getNotFoundEqp(), e.getCode())) {
                                isStorageFlag = true;
                            } else {
                                e.setData(out);
                                throw e;
                            }
                        }
                        log.info("Equipment is Storage or not == {},{}", singleCarrierXferFillInOTMSW006InParmRetCode.getStrCarrierXferReq().getFromMachineID().getValue(), isStorageFlag);
                        if (CimBooleanUtils.isFalse(isStorageFlag)) {
                            // Lock Port Object Of machine
                            // step27 - object_LockForEquipmentResource
                            try {
                                lockMethod.objectLockForEquipmentResource(objCommonIn,singleCarrierXferFillInOTMSW006InParmRetCode.getStrCarrierXferReq().getFromMachineID(),
                                        singleCarrierXferFillInOTMSW006InParmRetCode.getStrCarrierXferReq().getFromPortID(),
                                        BizConstant.SP_CLASSNAME_POSPORTRESOURCE);
                            } catch (ServiceException e) {
                                e.setData(out);
                                throw e;
                            }
                            //Check Equipment Port for Sending Request to XM. (EQP -> Stocker)
                            //【step28】 - equipment_portState_CheckForCassetteDelivery
                            Inputs.ObjEquipmentPortStateCheckForCassetteDeliveryIn deliveryIn = new Inputs.ObjEquipmentPortStateCheckForCassetteDeliveryIn();
                            deliveryIn.setEquipmentID(singleCarrierXferFillInOTMSW006InParmRetCode.getStrCarrierXferReq().getFromMachineID());
                            deliveryIn.setPortID(singleCarrierXferFillInOTMSW006InParmRetCode.getStrCarrierXferReq().getFromPortID());
                            try {
                                equipmentMethod.equipmentPortStateCheckForCassetteDelivery(objCommonIn, deliveryIn);
                            } catch (ServiceException e) {
                                e.setData(out);
                                throw e;
                            }
                        }
                        //Send Request to XM. (EQP -> Stocker)
                        Params.SingleCarrierTransferReqParam singleCarrierTransferReqParam = new Params.SingleCarrierTransferReqParam();
                        singleCarrierTransferReqParam.setRerouteFlag(singleCarrierXferFillInOTMSW006InParmRetCode.getRerouteFlag());
                        Infos.CarrierXferReq strCarrierXferReq = singleCarrierXferFillInOTMSW006InParmRetCode.getStrCarrierXferReq();
                        singleCarrierTransferReqParam.setCarrierID(strCarrierXferReq.getCarrierID());
                        singleCarrierTransferReqParam.setLotID(strCarrierXferReq.getLotID());
                        singleCarrierTransferReqParam.setZoneType(strCarrierXferReq.getZoneType());
                        singleCarrierTransferReqParam.setN2PurgeFlag(strCarrierXferReq.getN2PurgeFlag());
                        singleCarrierTransferReqParam.setFromMachineID(strCarrierXferReq.getFromMachineID());
                        singleCarrierTransferReqParam.setFromPortID(strCarrierXferReq.getFromPortID());
                        singleCarrierTransferReqParam.setToStockerGroup(strCarrierXferReq.getToStockerGroup());
                        singleCarrierTransferReqParam.setToMachine(strCarrierXferReq.getStrToMachine());
                        singleCarrierTransferReqParam.setExpectedStartTime(strCarrierXferReq.getExpectedStartTime());
                        singleCarrierTransferReqParam.setExpectedEndTime(strCarrierXferReq.getExpectedEndTime());
                        singleCarrierTransferReqParam.setMandatoryFlag(strCarrierXferReq.getMandatoryFlag());
                        singleCarrierTransferReqParam.setPriority(strCarrierXferReq.getPriority());
                        //【step29】 - txSingleCarrierTransferReq
                        Results.SingleCarrierTransferReqResult singleCarrierTransferReqResultRetCode = null;
                        try {
                            singleCarrierTransferReqResultRetCode = this.sxSingleCarrierTransferReq(objCommonIn, singleCarrierTransferReqParam);
                        } catch (ServiceException e) {
                            log.info("txSingleCarrierTransferReq() rc != RC_OK");
                            //Prepare e-mail Message Text (skip)
                            StringBuilder msg = new StringBuilder("<<< Transfer Error!  (EQP -> Stocker) >>>\n");
                            msg.append("\n")
                                    .append("Equipment ID         : ").append(ObjectIdentifier.fetchValue(equipmentID)).append("\n")
                                    .append("-----------------------------------------\n")
                                    .append("carrier ID           : ").append(ObjectIdentifier.fetchValue(strCarrierXferReq.getCarrierID())).append("\n")
                                    .append("Lot ID               : ").append(ObjectIdentifier.fetchValue(strCarrierXferReq.getLotID())).append("\n")
                                    .append("From Machine ID      : ")
                                    .append(ObjectIdentifier.fetchValue(strCarrierXferReq.getFromMachineID()))
                                    .append("From Port ID         : ").append(ObjectIdentifier.fetchValue(strCarrierXferReq.getFromPortID())).append("\n")
                                    .append("To Stocker Group     : ").append(strCarrierXferReq.getToStockerGroup()).append("\n");
                            int toMachineLen = CimArrayUtils.getSize(strCarrierXferReq.getStrToMachine());
                            if (toMachineLen == 0){
                                msg.append("To Machine ID        : Nothing\n");
                            }
                            for (int j = 0; j < toMachineLen; j++) {
                                msg.append("To Machine ID        : ").append(ObjectIdentifier.fetchValue(strCarrierXferReq.getStrToMachine().get(j).getToMachineID())).append("\n")
                                        .append("To Port ID           : ").append(ObjectIdentifier.fetchValue(strCarrierXferReq.getStrToMachine().get(j).getToPortID())).append("\n");
                            }
                            msg.append("-----------------------------------------\n")
                                    .append("Transaction ID       : ").append(e.getTransactionID()).append("\n")
                                    .append("Return Code          : ").append(e.getCode()).append("\n")
                                    .append("Message Text         : ").append(e.getMessage()).append("\n")
                                    .append("Reason Text          : ").append(e.getReasonText()).append("\n");
                            //Set System Message
                            Infos.SysMsgStockInfo sysMsg = new Infos.SysMsgStockInfo();
                            sysMsg.setSubSystemID(BizConstant.SP_SUBSYSTEMID_MM);
                            sysMsg.setSystemMessageCode(BizConstant.SP_SYSTEMMSGCODE_DELIVERYERROR);
                            sysMsg.setSystemMessageText(msg.toString());
                            sysMsg.setNotifyFlag(true);
                            sysMsg.setEquipmentID(equipmentID);
                            sysMsg.setEquipmentStatus("");
                            sysMsg.setStockerID(dummy);
                            sysMsg.setStockerStatus("");
                            sysMsg.setAGVID(dummy);
                            sysMsg.setAGVStatus("");
                            sysMsg.setLotID(dummy);
                            sysMsg.setLotStatus("");
                            sysMsg.setRouteID(dummy);
                            sysMsg.setOperationID(dummy);
                            sysMsg.setOperationNumber("");
                            sysMsg.setSystemMessageTimeStamp(CimDateUtils.convertToSpecString(objCommonIn.getTimeStamp().getReportTimeStamp()));
                            List<Infos.SysMsgStockInfo> sysMstStockInfoList = CimArrayUtils.getSize(out.getSysMstStockInfoList()) == 0 ? new ArrayList<>() : out.getSysMstStockInfoList();
                            sysMstStockInfoList.add(sysMsg);
                            out.setSysMstStockInfoList(sysMstStockInfoList);
                            e.setData(out);
                            throw e;
                        }

                    }
                }
                //TakeOutIn transfer
                //Check takeOutIn transfer function enable. (EQP -> STK)
                if (eqpTakeOutInSupport == 1) {
                    Params.LotCarrierTOTIReqParam lotCarrierTOTIReqParam = new Params.LotCarrierTOTIReqParam();
                    lotCarrierTOTIReqParam.setEqpToEqpFlag(false);
                    lotCarrierTOTIReqParam.setEquipmentID(equipmentID);
                    lotCarrierTOTIReqParam.setControlJobID(ObjectIdentifier.buildWithValue(""));
                    lotCarrierTOTIReqParam.setStrEqpTargetPortInfo(equipmentTargetPortPickupOut.getEqpTargetPortInfo());
                    lotCarrierTOTIReqParam.setStrCarrierXferReqSeq(tmpCarrierXferReqSeq);
                    //【step30】  - txLotCarrierTOTIReq
                    Results.LotCarrierTOTIReqResult lotCarrierTOTIReqResultRetCode = null;
                    try {
                        lotCarrierTOTIReqResultRetCode = this.sxLotCarrierTOTIReq(objCommonIn, lotCarrierTOTIReqParam, "");
                        // Set mail information even if the transaction returns error.
                        int sysMsgLen = CimArrayUtils.getSize(lotCarrierTOTIReqResultRetCode.getStrSysMsgStockInfoSeq());
                        for (int i = 0; i < sysMsgLen; i++) {
                            List<Infos.SysMsgStockInfo> sysMstStockInfoList = CimArrayUtils.getSize(out.getSysMstStockInfoList()) == 0 ? new ArrayList<>() : out.getSysMstStockInfoList();
                            sysMstStockInfoList.add(lotCarrierTOTIReqResultRetCode.getStrSysMsgStockInfoSeq().get(i));
                            out.setSysMstStockInfoList(sysMstStockInfoList);
                        }
                    } catch (ServiceException e) {
                        lotCarrierTOTIReqResultRetCode = e.getData(Results.LotCarrierTOTIReqResult.class);
                        if (!CimObjectUtils.isEmpty(lotCarrierTOTIReqResultRetCode)){
                            int sysMsgLen = CimArrayUtils.getSize(lotCarrierTOTIReqResultRetCode.getStrSysMsgStockInfoSeq());
                            for (int i = 0; i < sysMsgLen; i++) {
                                List<Infos.SysMsgStockInfo> sysMstStockInfoList = CimArrayUtils.getSize(out.getSysMstStockInfoList()) == 0 ? new ArrayList<>() : out.getSysMstStockInfoList();
                                sysMstStockInfoList.add(lotCarrierTOTIReqResultRetCode.getStrSysMsgStockInfoSeq().get(i));
                                out.setSysMstStockInfoList(sysMstStockInfoList);
                            }
                        }
                        e.setData(out);
                        throw e;
                    }
                    /*--------------------*/
                    /*   Return to Main   */
                    /*--------------------*/
                    return out;
                }
                /*--------------------*/
                /*   End of Process   */
                /*--------------------*/
                bLoadReqFlag = false;
            }
        }
        if (CimBooleanUtils.isTrue(bLoadReqFlag)) {
            //Load Request
            if (CimStringUtils.equals(BizConstant.SP_PORTRSC_PORTSTATE_LOADREQ, equipmentTargetPortPickupOut.getTargetPortType())) {
                if (CimBooleanUtils.isFalse(eqpInfoInqResult.getEquipmentStatusInfo().getEquipmentAvailableFlag())) {
                    throw new ServiceException(new OmCode(retCodeConfig.getInvalidEquipmentStatus(), equipmentID.getValue(), "NotAvailable"));
                }
                //Get WIP Lots
                Results.WhatNextLotListResult strWhatNextInqResult = null;
                //Call RTD Interface
                //【step31】 - cassetteDelivery_RTDInterfaceReq
                Outputs.ObjCassetteDeliveryRTDInterfaceReqOut cassetteDeliveryRTDInterfaceReqOutRetCode = null;
                try {
                    cassetteDeliveryRTDInterfaceReqOutRetCode = cassetteMethod.cassetteDeliveryRTDInterfaceReq(objCommonIn, BizConstant.SP_RTD_FUNCTION_CODE_WHATNEXT, equipmentID);
                    log.info("Normal End RTD Interface of txWhatNextInq");
                    strWhatNextInqResult = null == cassetteDeliveryRTDInterfaceReqOutRetCode ? null : cassetteDeliveryRTDInterfaceReqOutRetCode.getStrWhatNextInqResult();
                } catch (ServiceException e) {
                    log.info("Faild RTD Interface Call Normal Function (txWhatNextInq)");
                    if (!Validations.isEquals(retCodeConfigEx.getRtdInterfaceSwitchOff(), e.getCode())
                            && !Validations.isEquals(retCodeConfigEx.getNotFoundRTD(), e.getCode())) {
                        //Set System Message (skip)
                        StringBuilder msg = new StringBuilder("<<< RTD Interface Error!  (WhatNext of CassetteDelivery) >>>\n");
                        msg.append("\n")
                                .append("Equipment ID         : ").append(ObjectIdentifier.fetchValue(equipmentID)).append("\n")
                                .append("-----------------------------------------\n")
                                .append("Transaction ID       : ").append(e.getTransactionID()).append("\n")
                                .append("Return Code          : ").append(e.getCode()).append("\n")
                                .append("Message Text         : ").append(e.getMessage()).append("\n")
                                .append("Reason Text          : ").append(e.getReasonText()).append("\n");
                        Infos.SysMsgStockInfo sysMsg = new Infos.SysMsgStockInfo();
                        sysMsg.setSubSystemID(BizConstant.SP_SUBSYSTEMID_MM);
                        sysMsg.setSystemMessageCode(BizConstant.SP_SYSTEMMSGCODE_RTDERROR);
                        sysMsg.setSystemMessageText(msg.toString());// up on the msg but not add
                        sysMsg.setNotifyFlag(true);
                        sysMsg.setEquipmentID(equipmentID);
                        sysMsg.setEquipmentStatus("");
                        sysMsg.setStockerID(dummy);
                        sysMsg.setStockerStatus("");
                        sysMsg.setAGVID(dummy);
                        sysMsg.setAGVStatus("");
                        sysMsg.setLotID(dummy);
                        sysMsg.setLotStatus("");
                        sysMsg.setRouteID(dummy);
                        sysMsg.setOperationID(dummy);
                        sysMsg.setOperationNumber("");
                        sysMsg.setSystemMessageTimeStamp(CimDateUtils.convertToSpecString(objCommonIn.getTimeStamp().getReportTimeStamp()));
                        List<Infos.SysMsgStockInfo> sysMstStockInfoList = CimArrayUtils.getSize(out.getSysMstStockInfoList()) == 0 ? new ArrayList<>() : out.getSysMstStockInfoList();
                        sysMstStockInfoList.add(sysMsg);
                        out.setSysMstStockInfoList(sysMstStockInfoList);
                    }
                    //Call Normal Function (txWhatNextInq)
                    Params.WhatNextLotListParams whatNextLotListParams = new Params.WhatNextLotListParams();
                    whatNextLotListParams.setEquipmentID(equipmentID);
                    whatNextLotListParams.setSelectCriteria(ObjectIdentifier.buildWithValue(BizConstant.SP_DP_SELECTCRITERIA_AUTO3));
                    //【step32】 - txWhatNextInq__140
                    try {
                        strWhatNextInqResult = dispatchInqService.sxWhatNextLotListInfo(objCommonIn, whatNextLotListParams);
                    } catch (ServiceException ex) {
                        if (!Validations.isEquals(retCodeConfigEx.getNoWipLot(), ex.getCode())) {
                            //Set System Message
                            StringBuilder msg = new StringBuilder("<<< WhatNext Error!  (Stocker -> EQP) >>>\n");
                            msg.append("\n")
                                    .append("Equipment ID         : ").append(ObjectIdentifier.fetchValue(equipmentID)).append("\n")
                                    .append("-----------------------------------------\n")
                                    .append("Transaction ID       : ").append(ex.getTransactionID()).append("\n")
                                    .append("Return Code          : ").append(ex.getCode()).append("\n")
                                    .append("Message Text         : ").append(ex.getMessage()).append("\n")
                                    .append("Reason Text          : ").append(ex.getReasonText()).append("\n");
                            Infos.SysMsgStockInfo sysMsg = new Infos.SysMsgStockInfo();
                            sysMsg.setSubSystemID(BizConstant.SP_SUBSYSTEMID_MM);
                            sysMsg.setSystemMessageCode(BizConstant.SP_SYSTEMMSGCODE_DELIVERYERROR);
                            sysMsg.setSystemMessageText(msg.toString());// up on the msg but not add
                            sysMsg.setNotifyFlag(true);
                            sysMsg.setEquipmentID(equipmentID);
                            sysMsg.setEquipmentStatus("");
                            sysMsg.setStockerID(dummy);
                            sysMsg.setStockerStatus("");
                            sysMsg.setAGVID(dummy);
                            sysMsg.setAGVStatus("");
                            sysMsg.setLotID(dummy);
                            sysMsg.setLotStatus("");
                            sysMsg.setRouteID(dummy);
                            sysMsg.setOperationID(dummy);
                            sysMsg.setOperationNumber("");
                            sysMsg.setSystemMessageTimeStamp(CimDateUtils.convertToSpecString(objCommonIn.getTimeStamp().getReportTimeStamp()));
                            List<Infos.SysMsgStockInfo> sysMstStockInfoList = CimArrayUtils.getSize(out.getSysMstStockInfoList()) == 0 ? new ArrayList<>() : out.getSysMstStockInfoList();
                            sysMstStockInfoList.add(sysMsg);
                            out.setSysMstStockInfoList(sysMstStockInfoList);
                        }
                        ex.setData(out);
                        throw ex;
                    }
                }
                for (int i = 0; i < lenPortGroup; i++) {
                    List<Infos.PortGroup> strPortGroupSeq = new ArrayList<>();
                    Infos.PortGroup portGroup = equipmentTargetPortPickupOut.getEqpTargetPortInfo().getPortGroups().get(i);
                    strPortGroupSeq.add(portGroup);
                    // Select Multiple Lots
                    Results.WhatNextLotListResult convWhatNextInqResult = strWhatNextInqResult;
                    //【step33】 - whatNextLotList_to_StartCassetteForDeliveryReq
                    List<Infos.StartCassette> strStartCassette = null;
                    try {
                        strStartCassette = whatNextMethod.whatNextLotListToStartCassetteForDeliveryReq(objCommonIn, equipmentID, strPortGroupSeq, convWhatNextInqResult);
                    } catch (ServiceException ex) {
                        log.info("whatNextLotList_to_StartCassetteForDeliveryReq() rc != RC_OK");
                        if (lenPortGroup - 1 == i) {
                            log.info("i is last loop. so return error.");
                            ex.setData(out);
                            throw ex;
                        } else {
                            log.info("continue next PortGroup!!");
                            continue;
                        }
                    }
                    long tmpFPCAdoptFlag = StandardProperties.OM_DOC_ENABLE_FLAG.getLongValue();
                    if (CimNumberUtils.longValue(tmpFPCAdoptFlag) == 1) {
                        log.info("DOC Adopt Flag is ON. Now apply FPCInfo.");
                        //【step34】 - FPCStartCassetteInfo_Exchange
                        String exchangeType = BizConstant.SP_FPC_EXCHANGETYPE_STARTRESERVEINFO;
                        try {
                            strStartCassette = fpcMethod.fpcStartCassetteInfoExchange(objCommonIn, exchangeType, equipmentID, strStartCassette);
                        } catch (ServiceException e) {
                            e.setData(out);
                            throw e;
                        }
                    } else {
                        log.info("DOC Adopt Flag is OFF");
                    }
                    //【step35】 - startCassette_processJobExecFlag_Set__090
                    Boolean notSendIgnoreMail = false;
                    OmCode startCassetteProcessJobExecFlagSetRc = retCodeConfig.getSucc();
                    Outputs.ObjStartCassetteProcessJobExecFlagSetOut startCassetteProcessJobExecFlagSetOutRetCode = null;
                    try {
                        startCassetteProcessJobExecFlagSetOutRetCode = startCassetteMethod.startCassetteProcessJobExecFlagSet(objCommonIn,
                                strStartCassette,
                                equipmentID);
                    } catch (ServiceException ex) {
                        startCassetteProcessJobExecFlagSetOutRetCode = ex.getData(Outputs.ObjStartCassetteProcessJobExecFlagSetOut.class);
                        if (!Validations.isEquals(retCodeConfig.getNoSmplSetting(), ex.getCode())) {
                            // When errors have occurred, hold add the error lot and send e-mail to notify that.
                            notSendIgnoreMail = true;
                            startCassetteProcessJobExecFlagSetRc = retCodeConfig.getInvalidSmplSetting();
                        }
                    }
                    //hold lots and send E-Mails if error
                    int mailLen = CimArrayUtils.getSize(startCassetteProcessJobExecFlagSetOutRetCode.getSamplingMessage());
                    for (int mailCnt = 0; mailCnt < mailLen; mailCnt++) {
                        if (BizConstant.SP_SAMPLING_ERROR_MAIL == startCassetteProcessJobExecFlagSetOutRetCode.getSamplingMessage().get(mailCnt).getMessageType()
                                || BizConstant.SP_SAMPLING_WARN_MAIL == startCassetteProcessJobExecFlagSetOutRetCode.getSamplingMessage().get(mailCnt).getMessageType()) {
                            log.info("messageType == SP_Sampling_error_mail || SP_Sampling_warn_mail ");
                            // Lot Hold
                            //【step36】 - lot_currentOperationInfo_Get
                            Outputs.ObjLotCurrentOperationInfoGetOut lotCurrentOperationInfoGetRetCode = null;
                            try {
                                lotCurrentOperationInfoGetRetCode = lotMethod.lotCurrentOperationInfoGet(objCommonIn, startCassetteProcessJobExecFlagSetOutRetCode.getSamplingMessage().get(mailCnt).getLotID());
                            } catch (ServiceException e) {
                                e.setData(out);
                                throw e;
                            }

                            Infos.LotHoldReq lotHoldReq = new Infos.LotHoldReq();
                            lotHoldReq.setHoldType(BizConstant.SP_HOLDTYPE_LOTHOLD);
                            lotHoldReq.setHoldReasonCodeID(new ObjectIdentifier(BizConstant.SP_REASON_WAFERSAMPLINGHOLD));
                            lotHoldReq.setHoldUserID(objCommonIn.getUser().getUserID());
                            lotHoldReq.setResponsibleOperationMark(BizConstant.SP_RESPONSIBLEOPERATION_CURRENT);
                            lotHoldReq.setRouteID(lotCurrentOperationInfoGetRetCode.getRouteID());
                            lotHoldReq.setOperationNumber(lotCurrentOperationInfoGetRetCode.getOperationNumber());
                            lotHoldReq.setClaimMemo("");
                            List<Infos.LotHoldReq> holdReqList = new ArrayList<Infos.LotHoldReq>();
                            holdReqList.add(lotHoldReq);
                            //【step37】 - txHoldLotReq
                            try {
                                lotService.sxHoldLotReq(objCommonIn, startCassetteProcessJobExecFlagSetOutRetCode.getSamplingMessage().get(mailCnt).getLotID(), holdReqList);
                            } catch (ServiceException e) {
                                e.setData(out);
                                throw e;
                            }
                        }
                        //error Mail Set
                        if (CimBooleanUtils.isFalse(notSendIgnoreMail) || BizConstant.SP_SAMPLING_IGNORED_MAIL  != startCassetteProcessJobExecFlagSetOutRetCode.getSamplingMessage().get(mailCnt).getMessageType()) {
                            //When notSendIgnoreMail == TRUE, just the Lot Hold mail is to be sent.
                            String messageText = "";
                            if (BizConstant.SP_SAMPLING_WARN_MAIL == startCassetteProcessJobExecFlagSetOutRetCode.getSamplingMessage().get(mailCnt).getMessageType()) {
                                //----------------------------------------------------------------
                                // create mail text if message type == SP_Sampling_warn_mail;
                                // startCassette_processJobExecFlag_Set__090 returns just error text. Caller should switch text handling.
                                //----------------------------------------------------------------
                                //【step38】 - lot_samplingMessage_Create
                                try {
                                    messageText = lotMethod.lotSamplingMessageCreate(objCommonIn,
                                            startCassetteProcessJobExecFlagSetOutRetCode.getSamplingMessage().get(mailCnt).getLotID(),
                                            Integer.valueOf(BizConstant.SP_SAMPLING_ERROR_MAIL),
                                            startCassetteProcessJobExecFlagSetOutRetCode.getSamplingMessage().get(mailCnt).getMessageText());
                                } catch (ServiceException e) {
                                    e.setData(out);
                                    throw e;
                                }
                            } else {
                                messageText = startCassetteProcessJobExecFlagSetOutRetCode.getSamplingMessage().get(mailCnt).getMessageText();
                            }
                            //Set System Message
                            Infos.SysMsgStockInfo sysMsg = new Infos.SysMsgStockInfo();
                            sysMsg.setSubSystemID(BizConstant.SP_SUBSYSTEMID_MM);
                            sysMsg.setSystemMessageCode(BizConstant.SP_SYSTEMMSGCODE_SMPLERR);
                            sysMsg.setSystemMessageText(messageText);
                            sysMsg.setNotifyFlag(true);
                            sysMsg.setEquipmentID(equipmentID);
                            sysMsg.setEquipmentStatus("");
                            sysMsg.setStockerID(dummy);
                            sysMsg.setStockerStatus("");
                            sysMsg.setAGVID(dummy);
                            sysMsg.setAGVStatus("");
                            sysMsg.setLotID(startCassetteProcessJobExecFlagSetOutRetCode.getSamplingMessage().get(mailCnt).getLotID());
                            sysMsg.setLotStatus("");
                            sysMsg.setRouteID(dummy);
                            sysMsg.setOperationID(dummy);
                            sysMsg.setOperationNumber("");
                            sysMsg.setSystemMessageTimeStamp(CimDateUtils.convertToSpecString(objCommonIn.getTimeStamp().getReportTimeStamp()));
                            List<Infos.SysMsgStockInfo> sysMstStockInfoList = CimArrayUtils.getSize(out.getSysMstStockInfoList()) == 0 ? new ArrayList<>() : out.getSysMstStockInfoList();
                            sysMstStockInfoList.add(sysMsg);
                            out.setSysMstStockInfoList(sysMstStockInfoList);
                        }
                    }
                    if (CimBooleanUtils.isTrue(notSendIgnoreMail)) {
                        log.info("notSendIgnoreMail == TRUE  return this TX.");
                        if (Validations.isEquals(retCodeConfig.getSucc(),startCassetteProcessJobExecFlagSetRc)){
                            return out;
                        }else {
                            throw new ServiceException(startCassetteProcessJobExecFlagSetRc,out);
                        }
                    }
                    //valid check of processJobExecFlag for each lot.
                    //Check every lot has at least one wafer with processJobExecFlag == TRUE
                    //【step39】 - lot_processJobExecFlag_ValidCheck
                    Outputs.ObjLotProcessJobExecFlagValidCheckOut lotProcessJobExecFlagValidCheckOut = null;
                    try {
                        lotProcessJobExecFlagValidCheckOut = lotMethod.lotProcessJobExecFlagValidCheck(objCommonIn, startCassetteProcessJobExecFlagSetOutRetCode.getStartCassettes());
                    } catch (ServiceException e) {
                        if (!Validations.isEquals(retCodeConfig.getNoProcessJobExecFlag(), e.getCode())) {
                            log.info("lot_processJobExecFlag_ValidCheck() != RC_OK && != RC_NO_PROCESSJOBEXECFLAG");
                            e.setData(out);
                            throw e;
                        }
                        if (Validations.isEquals(retCodeConfig.getNoProcessJobExecFlag(), e.getCode())) {
                            lotProcessJobExecFlagValidCheckOut = e.getData(Outputs.ObjLotProcessJobExecFlagValidCheckOut.class);
                            log.info("objLot_processJobExecFlag_ValidCheck() rc == RC_NO_PROCESSJOBEXECFLAG");
                            // Lot Hold + error mail
                            // create hold mail message
                            //【step40】 - lot_samplingMessage_Create
                            String lotSamplingMessageCreateRetCode = null;
                            try {
                                lotSamplingMessageCreateRetCode = lotMethod.lotSamplingMessageCreate(objCommonIn,
                                        lotProcessJobExecFlagValidCheckOut.getLotID(),
                                        BizConstant.SP_SAMPLING_ERROR_MAIL,
                                        e.getMessage());
                            } catch (ServiceException ex) {
                                ex.setData(out);
                                throw ex;
                            }

                            //【step41】 - lot_currentOperationInfo_Get
                            Outputs.ObjLotCurrentOperationInfoGetOut lotCurrentOperationInfo = null;
                            try {
                                lotCurrentOperationInfo = lotMethod.lotCurrentOperationInfoGet(objCommonIn, lotProcessJobExecFlagValidCheckOut.getLotID());
                            } catch (ServiceException ex) {
                                ex.setData(out);
                                throw ex;
                            }

                            Infos.LotHoldReq lotHoldReq = new Infos.LotHoldReq();
                            lotHoldReq.setHoldType(BizConstant.SP_HOLDTYPE_LOTHOLD);
                            lotHoldReq.setHoldReasonCodeID(new ObjectIdentifier(BizConstant.SP_REASON_WAFERSAMPLINGHOLD));
                            lotHoldReq.setHoldUserID(objCommonIn.getUser().getUserID());
                            lotHoldReq.setResponsibleOperationMark(BizConstant.SP_RESPONSIBLEOPERATION_CURRENT);
                            lotHoldReq.setRouteID(lotCurrentOperationInfo.getRouteID());
                            lotHoldReq.setOperationNumber(lotCurrentOperationInfo.getOperationNumber());
                            lotHoldReq.setClaimMemo("");
                            List<Infos.LotHoldReq> holdReqList = new ArrayList<Infos.LotHoldReq>();
                            holdReqList.add(lotHoldReq);
                            try {
                                lotService.sxHoldLotReq(objCommonIn, lotProcessJobExecFlagValidCheckOut.getLotID(), holdReqList);
                            } catch (ServiceException ex) {
                                ex.setData(out);
                                throw ex;
                            }
                            //error Mail Set
                            Infos.SysMsgStockInfo sysMsg = new Infos.SysMsgStockInfo();
                            sysMsg.setSubSystemID(BizConstant.SP_SUBSYSTEMID_MM);
                            sysMsg.setSystemMessageCode(BizConstant.SP_SYSTEMMSGCODE_SMPLERR);
                            sysMsg.setSystemMessageText(lotSamplingMessageCreateRetCode);// up on the msg but not add
                            sysMsg.setNotifyFlag(true);
                            sysMsg.setEquipmentID(equipmentID);
                            sysMsg.setEquipmentStatus("");
                            sysMsg.setStockerID(dummy);
                            sysMsg.setStockerStatus("");
                            sysMsg.setAGVID(dummy);
                            sysMsg.setAGVStatus("");
                            sysMsg.setLotID(lotProcessJobExecFlagValidCheckOut.getLotID());
                            sysMsg.setLotStatus("");
                            sysMsg.setRouteID(dummy);
                            sysMsg.setOperationID(dummy);
                            sysMsg.setOperationNumber("");
                            sysMsg.setSystemMessageTimeStamp(CimDateUtils.convertToSpecString(objCommonIn.getTimeStamp().getReportTimeStamp()));
                            List<Infos.SysMsgStockInfo> sysMstStockInfoList = CimArrayUtils.getSize(out.getSysMstStockInfoList()) == 0 ? new ArrayList<>() : out.getSysMstStockInfoList();
                            sysMstStockInfoList.add(sysMsg);
                            out.setSysMstStockInfoList(sysMstStockInfoList);
                            throw new ServiceException(retCodeConfig.getNoProcessJobExecFlag(), out);
                        }
                    }
                    strStartCassette = startCassetteProcessJobExecFlagSetOutRetCode.getStartCassettes();
                    log.info("Set processJobExecFlag based on wafer sampling setting end. ");
                    //Request to Start Lot Reservation
                    Infos.ObjCommon strTmpObjCommon = objCommonIn;
                    strTmpObjCommon.setTransactionID("ODISW001");
                    Params.MoveInReserveReqParams params = new Params.MoveInReserveReqParams();
                    params.setEquipmentID(equipmentID);
                    params.setPortGroupID(strPortGroupSeq.get(0).getPortGroup());
                    params.setControlJobID(new ObjectIdentifier());
                    params.setStartCassetteList(strStartCassette);
                    //【step43】 - txMoveInReserveReq
                    Results.MoveInReserveReqResult moveInReserveReqResultRetCode = null;
                    try {
                        moveInReserveReqResultRetCode = (Results.MoveInReserveReqResult) dispatchService.sxMoveInReserveReq(strTmpObjCommon, params);
                    } catch (ServiceException ex) {
                        log.info("txMoveInReserveReq() rc != RC_OK");
                        //Prepare e-mail Message Text
                        StringBuilder msg = new StringBuilder("<<< MoveInReservation Error!  (Stocker -> EQP) >>>\n");
                        msg.append("\n")
                                .append("Equipment ID         : ").append(ObjectIdentifier.fetchValue(equipmentID)).append("\n")
                                .append("Lot IDs              : ");
                        boolean bFirstSet = false;
                        for (i = 0; i < CimArrayUtils.getSize(strStartCassette); i++) {
                            for (int j = 0; j < CimArrayUtils.getSize(strStartCassette.get(i).getLotInCassetteList()); j++) {
                                if (bFirstSet){
                                    msg.append(", ");
                                }
                                msg.append(ObjectIdentifier.fetchValue(strStartCassette.get(i).getLotInCassetteList().get(j).getLotID()));
                                bFirstSet = true;
                            }
                        }
                        msg.append("\n")
                                .append("Transaction ID       : ").append(ex.getTransactionID()).append("\n")
                                .append("Return Code          : ").append(ex.getCode()).append("\n")
                                .append("Message Text         : ").append(ex.getMessage()).append("\n")
                                .append("Reason Text          : ").append(ex.getReasonText()).append("\n");
                        Infos.SysMsgStockInfo sysMsg = new Infos.SysMsgStockInfo();
                        sysMsg.setSubSystemID(BizConstant.SP_SUBSYSTEMID_MM);
                        sysMsg.setSystemMessageCode(BizConstant.SP_SYSTEMMSGCODE_DELIVERYERROR);
                        sysMsg.setSystemMessageText(msg.toString());
                        sysMsg.setNotifyFlag(true);
                        sysMsg.setEquipmentID(equipmentID);
                        sysMsg.setEquipmentStatus("");
                        sysMsg.setStockerID(dummy);
                        sysMsg.setStockerStatus("");
                        sysMsg.setAGVID(dummy);
                        sysMsg.setAGVStatus("");
                        sysMsg.setLotID(dummy);
                        sysMsg.setLotStatus("");
                        sysMsg.setRouteID(dummy);
                        sysMsg.setOperationID(dummy);
                        sysMsg.setOperationNumber("");
                        sysMsg.setSystemMessageTimeStamp(CimDateUtils.convertToSpecString(objCommonIn.getTimeStamp().getReportTimeStamp()));
                        List<Infos.SysMsgStockInfo> sysMstStockInfoList = CimArrayUtils.getSize(out.getSysMstStockInfoList()) == 0 ? new ArrayList<>() : out.getSysMstStockInfoList();
                        sysMstStockInfoList.add(sysMsg);
                        out.setSysMstStockInfoList(sysMstStockInfoList);
                        ex.setReasonText(msg.toString());
                        ex.setData(out);
                        throw ex;
                    }
                    //Make txMultipleCarrierTransferReq parameter
                    //【step44】 - multiCarrierXferFillInOTMSW005InParm
                    Outputs.ObjMultiCarrierXferFillInOTMSW005InParmOut multiCarrierXferFillInOTMSW005InParmRetCode = null;
                    try {
                        multiCarrierXferFillInOTMSW005InParmRetCode = cassetteMethod.multiCarrierXferFillInOTMSW005InParm(objCommonIn,
                                equipmentID, strStartCassette);
                    } catch (ServiceException e) {
                        log.info("multiCarrierXferFillInOTMSW005InParm() rc != RC_OK");
                        log.info("Request to Start Lot Reservation Cancel");
                        //【step45】 - txMoveInReserveCancelReq
                        Params.MoveInReserveCancelReqParams moveInReserveCancelReqParams = new Params.MoveInReserveCancelReqParams();
                        moveInReserveCancelReqParams.setEquipmentID(equipmentID);
                        moveInReserveCancelReqParams.setControlJobID(moveInReserveReqResultRetCode.getControlJobID());
                        try {
                            Results.MoveInReserveCancelReqResult moveInReserveCancelReqResultRetCode = dispatchService.sxMoveInReserveCancelReqService(objCommonIn, moveInReserveCancelReqParams);
                        } catch (ServiceException ex) {
                            log.info("txMoveInReserveCancelReq != RC_OK");
                        }
                        e.setData(out);
                        throw e;
                    }
                    Boolean bReRouteFlag = false;
                    String reRouteXferFlag = StandardProperties.OM_XFER_REROUTE_FLAG.getValue();
                    if (CimStringUtils.equals("1", reRouteXferFlag)) {
                        log.info("reRouteXferFlag is 1");
                        bReRouteFlag = true;
                    }
                    //Send Transfer Request to TMS. (Stocker -> EQP
                    //【step46】 - txMultipleCarrierTransferReq
                    List<Infos.CarrierXferReq> strCarrierXferReq = multiCarrierXferFillInOTMSW005InParmRetCode.getStrCarrierXferReq();
                    try {
                        this.sxMultipleCarrierTransferReq(objCommonIn, bReRouteFlag, "S", strCarrierXferReq);
                    } catch (ServiceException e) {
                        log.info("txMultipleCarrierTransferReq() rc != RC_OK");
                        //Prepare e-mail Message Text
                        StringBuilder msg = new StringBuilder("<<< Transfer Error!  (Stocker -> EQP) >>>\n");
                        msg.append("\n")
                                .append("Equipment ID         : ").append(equipmentID.getValue()).append("\n");
                        int lenCarrierXfer = CimArrayUtils.getSize(strCarrierXferReq);
                        for (int ii = 0; ii < lenCarrierXfer; ii++){
                            Infos.CarrierXferReq carrierXferReq = strCarrierXferReq.get(ii);
                            msg.append("-----------------------------------------\n")
                                    .append("carrier ID           : ").append(ObjectIdentifier.fetchValue(carrierXferReq.getCarrierID())).append("\n")
                                    .append("Lot ID               : ").append(ObjectIdentifier.fetchValue(carrierXferReq.getLotID())).append("\n")
                                    .append("From Machine ID      : ").append(ObjectIdentifier.fetchValue(carrierXferReq.getFromMachineID())).append("\n")
                                    .append("From Port ID         : ").append(ObjectIdentifier.fetchValue(carrierXferReq.getFromPortID())).append("\n")
                                    .append("To Stocker Group     : ").append(carrierXferReq.getToStockerGroup()).append("\n");
                            List<Infos.ToMachine> strToMachine = carrierXferReq.getStrToMachine();
                            int lenToEqp = CimArrayUtils.getSize(strToMachine);
                            if (lenToEqp == 0){
                                msg.append("To Machine ID        : Nothing\n");
                            }
                            for (int jj = 0; jj < lenToEqp; jj++){
                                Infos.ToMachine toMachine = strToMachine.get(jj);
                                msg.append("To Machine ID        : ").append(ObjectIdentifier.fetchValue(toMachine.getToMachineID())).append("\n")
                                        .append("To Port ID           : ").append(ObjectIdentifier.fetchValue(toMachine.getToPortID())).append("\n");
                            }
                        }
                        msg.append("-----------------------------------------\n")
                                .append("Transaction ID       : ").append(e.getTransactionID()).append("\n")
                                .append("Return Code          : ").append(e.getCode()).append("\n")
                                .append("Message Text         : ").append(e.getMessage()).append("\n")
                                .append("Reason Text          : ").append(e.getReasonText()).append("\n");
                        Infos.SysMsgStockInfo sysMsg = new Infos.SysMsgStockInfo();
                        sysMsg.setSubSystemID(BizConstant.SP_SUBSYSTEMID_MM);
                        sysMsg.setSystemMessageCode(BizConstant.SP_SYSTEMMSGCODE_DELIVERYERROR);
                        sysMsg.setSystemMessageText(msg.toString());
                        sysMsg.setNotifyFlag(true);
                        sysMsg.setEquipmentID(equipmentID);
                        sysMsg.setEquipmentStatus("");
                        sysMsg.setStockerID(dummy);
                        sysMsg.setStockerStatus("");
                        sysMsg.setAGVID(dummy);
                        sysMsg.setAGVStatus("");
                        sysMsg.setLotID(dummy);
                        sysMsg.setLotStatus("");
                        sysMsg.setRouteID(dummy);
                        sysMsg.setOperationID(dummy);
                        sysMsg.setOperationNumber("");
                        sysMsg.setSystemMessageTimeStamp(CimDateUtils.convertToSpecString(objCommonIn.getTimeStamp().getReportTimeStamp()));
                        List<Infos.SysMsgStockInfo> sysMstStockInfoList = CimArrayUtils.getSize(out.getSysMstStockInfoList()) == 0 ? new ArrayList<>() : out.getSysMstStockInfoList();
                        sysMstStockInfoList.add(sysMsg);
                        out.setSysMstStockInfoList(sysMstStockInfoList);
                        //Request to Start Lot Reservation Cancel
                        //【step47】 - txMoveInReserveCancelReq
                        Params.MoveInReserveCancelReqParams moveInReserveCancelReqParams = new Params.MoveInReserveCancelReqParams();
                        moveInReserveCancelReqParams.setEquipmentID(equipmentID);
                        moveInReserveCancelReqParams.setControlJobID(moveInReserveReqResultRetCode.getControlJobID());
                        try {
                            Results.MoveInReserveCancelReqResult moveInReserveCancelReqResultRetCode = dispatchService.sxMoveInReserveCancelReqService(objCommonIn, moveInReserveCancelReqParams);
                        } catch (ServiceException ex) {
                            log.info("txMoveInReserveCancelReq != RC_OK");
                        }
                        e.setData(out);
                        throw e;
                    }
                    //End of Process
                    log.info("OK! OK! OK! [Stocker to EQP]  Return to Main");
                    break;
                }
            }
        }
        // Return to Main
        return out;
    }
    @Override
    public Results.CarrierTransferReqResult sxFMCCarrierTransferReq(Infos.ObjCommon objCommonIn, ObjectIdentifier equipmentID) {
        /*----------------*/
        /*   Initialize   */
        /*----------------*/
        Results.CarrierTransferReqResult out = new Results.CarrierTransferReqResult();
        out.setSysMstStockInfoList(new ArrayList<>());

        //=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
        //   Check Process
        //=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
        log.info("Check Transaction ID and equipment Category combination.");
        equipmentMethod.equipmentCategoryVsTxIDCheckCombination(objCommonIn, equipmentID);

        /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/
        /*                                                                       */
        /*   Check Process                                                       */
        /*                                                                       */
        /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/
        log.info("Get Equipment Information");
        /*--------------------------------*/
        /*   Get Equipment Information     */
        /*--------------------------------*/
        Params.EqpInfoInqParams eqpInfoInqParams = new Params.EqpInfoInqParams();
        eqpInfoInqParams.setEquipmentID(equipmentID);
        eqpInfoInqParams.setRequestFlagForBasicInfo(true);
        eqpInfoInqParams.setRequestFlagForStatusInfo(true);
        eqpInfoInqParams.setRequestFlagForPMInfo(false);
        eqpInfoInqParams.setRequestFlagForPortInfo(true);
        eqpInfoInqParams.setRequestFlagForChamberInfo(false);
        eqpInfoInqParams.setRequestFlagForStockerInfo(false);
        eqpInfoInqParams.setRequestFlagForInprocessingLotInfo(false);
        eqpInfoInqParams.setRequestFlagForReservedControlJobInfo(false);
        eqpInfoInqParams.setRequestFlagForRSPPortInfo(false);
        eqpInfoInqParams.setRequestFlagForEqpContainerInfo(true);
        Results.EqpInfoInqResult eqpInfoInqResult = equipmentInqService.sxEqpInfoInq(objCommonIn, eqpInfoInqParams);

        //--------------------------------------------------------------------------------------
        // The SLMCapabilityFlag must be TRUE.
        //--------------------------------------------------------------------------------------
        Validations.check(CimBooleanUtils.isFalse(eqpInfoInqResult.getEquipmentBasicInfo().isFmcCapabilityFlag()), retCodeConfigEx.getEqpSlmCapabilityOff(), ObjectIdentifier.fetchValue(equipmentID));

        /*------------------------------------------*/
        /*   Check Online Mode and Available Flag   */
        /*------------------------------------------*/
        log.info("Check Online Mode and Available Flag");
        if (CimArrayUtils.isNotEmpty(eqpInfoInqResult.getEquipmentPortInfo().getEqpPortStatuses())) {
            for (Infos.EqpPortStatus eqpPortStatus : eqpInfoInqResult.getEquipmentPortInfo().getEqpPortStatuses()) {
                if (CimStringUtils.equals(BizConstant.SP_EQP_ONLINEMODE_OFFLINE, eqpPortStatus.getOnlineMode())) {
                    log.info("onlineMode == [Offline]");
                    Validations.check(retCodeConfig.getInvalidEquipmentMode(), ObjectIdentifier.fetchValue(equipmentID),
                            eqpPortStatus.getOnlineMode());
                } else {
                    log.info("onlineMode is OK: {},{}", ObjectIdentifier.fetchValue(equipmentID), eqpPortStatus.getOnlineMode());
                }
            }
        }

        String takeOutXferInNotAvailableState = StandardProperties.OM_XFER_TAKEOUT_IN_NOT_AVAIL_STATE.getValue();
        if (CimBooleanUtils.isFalse(eqpInfoInqResult.getEquipmentStatusInfo().getEquipmentAvailableFlag())
                && !CimStringUtils.equals("1", takeOutXferInNotAvailableState)) {
            Validations.check(true, retCodeConfig.getInvalidEquipmentStatus(), ObjectIdentifier.fetchValue(equipmentID), "NotAvailable");
        }

        log.info("Sorting Port Group Info by Port Group");
        /*-------------------------------------------*/
        /*   Sorting Port Group Info by Port Group   */
        /*-------------------------------------------*/
        Infos.EqpPortInfoOrderByGroup eqpPortInfoOrderByGroup = equipmentMethod.equipmentPortInfoSortByGroup(objCommonIn, equipmentID, eqpInfoInqResult.getEquipmentPortInfo().getEqpPortStatuses());

        log.info("Pick Up Target Port");
        Infos.EqpBrInfo eqpBrInfo = eqpInfoInqResult.getEquipmentBasicInfo();
        Outputs.EquipmentTargetPortPickupOut equipmentTargetPortPickupOut = equipmentMethod.equipmentTargetPortPickup(objCommonIn, eqpPortInfoOrderByGroup, eqpBrInfo, eqpInfoInqResult.getEquipmentPortInfo());

        List<Infos.PortGroup> targetPortGroupList = equipmentTargetPortPickupOut.getEqpTargetPortInfo().getPortGroups();
        int lenPortGroup = CimArrayUtils.getSize(targetPortGroupList);
        Validations.check(0 == lenPortGroup, retCodeConfig.getNotFoundTargetPort());

        /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/
        /*                                                                       */
        /*   Main Process                                                        */
        /*                                                                       */
        /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/
        ObjectIdentifier dummy = new ObjectIdentifier();
        Boolean bLoadReqFlag = true;

        AtomicReference<String> tmpAPCIFControlStatus = new AtomicReference<>();
        String APCIFControlStatus = null;
        log.info("targetPortType: {}", equipmentTargetPortPickupOut.getTargetPortType());
        /*--------------------*/
        /*   Unload Request   */
        /*--------------------*/
        if (CimStringUtils.equals(equipmentTargetPortPickupOut.getTargetPortType(), BizConstant.SP_PORTRSC_PORTSTATE_UNLOADREQ)) {
            log.info("Unload Request");
            bLoadReqFlag = false;
            log.info("Check Lot Process State");
            /*-----------------------------*/
            /*   Check Lot Process State   */
            /*-----------------------------*/
            log.info("TergetPort [PortGroupID] : {}", targetPortGroupList.get(0).getPortGroup());
            int nPortLen = CimArrayUtils.getSize(eqpInfoInqResult.getEquipmentPortInfo().getEqpPortStatuses());
            log.info("port Len == {}", nPortLen);
            if (nPortLen > 0) {
                for (Infos.EqpPortStatus eqpPortStatus : eqpInfoInqResult.getEquipmentPortInfo().getEqpPortStatuses()) {
                    log.info("EqpInfo [PortGroupID] :{}", eqpPortStatus.getPortGroup());
                    // check is targetPortInfo only
                    if (CimStringUtils.equals(targetPortGroupList.get(0).getPortGroup(), eqpPortStatus.getPortGroup())) {
                        log.info("TergetPortGropuID == EqpInfoPortGroupID");
                        int nLotOnPortLen = CimArrayUtils.getSize(eqpPortStatus.getLotOnPorts());
                        if (nLotOnPortLen > 0) {
                            for (Infos.LotOnPort lotOnPort : eqpPortStatus.getLotOnPorts()) {
                                log.info("EqpInfo [lotID] : [}", ObjectIdentifier.fetchValue(lotOnPort.getLotID()));

                                String theLotProcessState = lotMethod.lotProcessStateGet(objCommonIn, lotOnPort.getLotID());
                                log.info("theLotProcessState : {}", theLotProcessState);

                                if (CimStringUtils.equals(BizConstant.SP_LOT_PROCSTATE_PROCESSING, theLotProcessState)) {
                                    log.info("theLotProcessState == {}", "Processing");
                                    log.info("Cancel UnloadReq");
                                    bLoadReqFlag = true;
                                    break;
                                }
                            }
                        }
                    } else {
                        log.info("NoCheck ignore [PortGropuID]: {}", eqpPortStatus.getPortGroup());
                    }
                }
            }
            if (CimBooleanUtils.isFalse(bLoadReqFlag)) {
                log.info("bLoadReqFlag == FALSE");
                log.info("UnLoadReq Process RUN!!  EQP -> EQP");
                /*------------------------------*/
                /*   get whereNextTransferEqp   */
                /*------------------------------*/
                Boolean whereNextOKFlag = true;
                Outputs.WhereNextTransferEqpOut whereNextTransferEqpOut = null;
                try {
                    whereNextTransferEqpOut = whereNextMethod.whereNextTransferEqp(objCommonIn, equipmentID, equipmentTargetPortPickupOut);
                } catch (ServiceException e) {
                    whereNextOKFlag = false;
                    whereNextTransferEqpOut = (Outputs.WhereNextTransferEqpOut) e.getData();
                    log.info("EQP to EQP Xfer is interrupted, and change for the Stocker Xfer");
                }
                if (CimBooleanUtils.isTrue(whereNextOKFlag)) {
                    log.info("whereNextTransferEqp() is OK");
                    log.info("Reservation [equipmentID] ---> {}", ObjectIdentifier.fetchValue(whereNextTransferEqpOut.getEquipmentID()));
                    log.info("Reservation [portGroup] -----> {}", whereNextTransferEqpOut.getPortGroup());

                    int tmpFPCAdoptFlag = StandardProperties.OM_DOC_ENABLE_FLAG.getIntValue();
                    if (1 == tmpFPCAdoptFlag) {
                        log.debug("DOC adopt Flag is ON");
                        List<Infos.StartCassette> strStartCassette = fpcMethod.fpcStartCassetteInfoExchange(objCommonIn, BizConstant.SP_FPC_EXCHANGETYPE_STARTRESERVEINFO, equipmentID, whereNextTransferEqpOut.getStartCassetteList());
                        whereNextTransferEqpOut.setStartCassetteList(strStartCassette);
                    } else {
                        log.debug("DOC adopt Flag is OFF");
                    }

                    log.info("Set processJobExecFlag based on wafer sampling setting. ");
                    Boolean notSendIgnoreMail = false;
                    OmCode startCassette_processJobExecFlag_SetRc = retCodeConfig.getSucc();
                    Outputs.ObjStartCassetteProcessJobExecFlagSetOut startCassetteProcessJobExecFlagSetOutRetCode = null;
                    try {
                        startCassetteProcessJobExecFlagSetOutRetCode = startCassetteMethod.startCassetteProcessJobExecFlagSet(objCommonIn, whereNextTransferEqpOut.getStartCassetteList(), whereNextTransferEqpOut.getEquipmentID());
                    } catch (ServiceException ex) {
                        startCassetteProcessJobExecFlagSetOutRetCode = ex.getData(Outputs.ObjStartCassetteProcessJobExecFlagSetOut.class);
                        if (!Validations.isEquals(retCodeConfig.getNoSmplSetting(), ex.getCode())) {
                            // When errors have occurred, hold all the error lot and send e-mail to notify that.
                            notSendIgnoreMail = true;
                            startCassette_processJobExecFlag_SetRc = retCodeConfig.getInvalidSmplSetting();
                        }
                    }
                    int mailLen = CimArrayUtils.getSize(startCassetteProcessJobExecFlagSetOutRetCode.getSamplingMessage());
                    //hold lots and send E-mails if error
                    if (CimArrayUtils.getSize(startCassetteProcessJobExecFlagSetOutRetCode.getSamplingMessage()) > 0) {
                        for (Infos.ObjSamplingMessageAttribute samplingMessageAttribute : startCassetteProcessJobExecFlagSetOutRetCode.getSamplingMessage()) {
                            if (samplingMessageAttribute.getMessageType() == BizConstant.SP_SAMPLING_ERROR_MAIL
                                    || samplingMessageAttribute.getMessageType() == BizConstant.SP_SAMPLING_WARN_MAIL) {
                                log.info("messageType == SP_Sampling_error_mail || SP_Sampling_warn_mail ");
                                // Lot Hold
                                Outputs.ObjLotCurrentOperationInfoGetOut lotCurrentOperationInfoRetCode = lotMethod.lotCurrentOperationInfoGet(objCommonIn, samplingMessageAttribute.getLotID());

                                Infos.LotHoldReq lotHoldReq = new Infos.LotHoldReq();
                                lotHoldReq.setHoldType(BizConstant.SP_HOLDTYPE_LOTHOLD);
                                lotHoldReq.setHoldReasonCodeID(new ObjectIdentifier(BizConstant.SP_REASON_WAFERSAMPLINGHOLD));
                                lotHoldReq.setHoldUserID(objCommonIn.getUser().getUserID());
                                lotHoldReq.setResponsibleOperationMark(BizConstant.SP_RESPONSIBLEOPERATION_CURRENT);
                                lotHoldReq.setRouteID(lotCurrentOperationInfoRetCode.getRouteID());
                                lotHoldReq.setOperationNumber(lotCurrentOperationInfoRetCode.getOperationNumber());
                                lotHoldReq.setClaimMemo("");
                                List<Infos.LotHoldReq> holdReqList = new ArrayList<Infos.LotHoldReq>();
                                holdReqList.add(lotHoldReq);
                                lotService.sxHoldLotReq(objCommonIn, samplingMessageAttribute.getLotID(), holdReqList);
                            }
                            //error Mail Set
                            if (CimBooleanUtils.isFalse(notSendIgnoreMail) || BizConstant.SP_SAMPLING_IGNORED_MAIL != samplingMessageAttribute.getMessageType()) {
                                log.info("notSendIgnoreMail == FALSE ||  strStartCassette_processJobExecFlag_Set_out__090.strSamplingMessage[mailCnt].messageType != SP_Sampling_ignored_mail");
                                //When notSendIgnoreMail == TRUE, just the Lot Hold mail is to be sent.
                                String messageText = "";
                                if (BizConstant.SP_SAMPLING_WARN_MAIL == samplingMessageAttribute.getMessageType()) {
                                    //-------------------------------------------------------------------
                                    // create mail text if message type == SP_Sampling_warn_mail;
                                    // startCassette_processJobExecFlag_Set__090 returns just error text. Caller should switch text handling.
                                    //-------------------------------------------------------------------
                                    messageText = lotMethod.lotSamplingMessageCreate(objCommonIn,
                                            samplingMessageAttribute.getLotID(),
                                            BizConstant.SP_SAMPLING_ERROR_MAIL,
                                            samplingMessageAttribute.getMessageText());
                                } else {
                                    messageText = samplingMessageAttribute.getMessageText();
                                }
                                //Set System Message
                                Infos.SysMsgStockInfo sysMsg = new Infos.SysMsgStockInfo();
                                sysMsg.setSubSystemID(BizConstant.SP_SUBSYSTEMID_MM);
                                sysMsg.setSystemMessageCode(BizConstant.SP_SYSTEMMSGCODE_SMPLERR);
                                sysMsg.setSystemMessageText(messageText);
                                sysMsg.setNotifyFlag(true);
                                sysMsg.setEquipmentID(whereNextTransferEqpOut.getEquipmentID());
                                sysMsg.setEquipmentStatus("");
                                sysMsg.setStockerID(dummy);
                                sysMsg.setStockerStatus("");
                                sysMsg.setAGVID(dummy);
                                sysMsg.setAGVStatus("");
                                sysMsg.setLotID(samplingMessageAttribute.getLotID());
                                sysMsg.setLotStatus("");
                                sysMsg.setRouteID(dummy);
                                sysMsg.setOperationID(dummy);
                                sysMsg.setOperationNumber("");
                                sysMsg.setSystemMessageTimeStamp(CimDateUtils.convertToSpecString(objCommonIn.getTimeStamp().getReportTimeStamp()));
                                List<Infos.SysMsgStockInfo> sysMstStockInfoList = new ArrayList<>();
                                sysMstStockInfoList.add(sysMsg);
                                out.setSysMstStockInfoList(sysMstStockInfoList);
                            }
                        }
                    }
                    if (CimBooleanUtils.isTrue(notSendIgnoreMail)) {
                        log.info("notSendIgnoreMail == TRUE  return this TX.");
                        if (Validations.isEquals(startCassette_processJobExecFlag_SetRc, retCodeConfig.getSucc())) {
                            return out;
                        } else {
                            throw new ServiceException(startCassette_processJobExecFlag_SetRc, out);
                        }
                    }
                    //valid check of processJobExecFlag for each lot.
                    //Check every lot has at least one wafer with processJobExecFlag == TRUE
                    Outputs.ObjLotProcessJobExecFlagValidCheckOut lotProcessJobExecFlagValidCheckOutRetCode = null;
                    try {
                        lotProcessJobExecFlagValidCheckOutRetCode = lotMethod.lotProcessJobExecFlagValidCheck(objCommonIn, startCassetteProcessJobExecFlagSetOutRetCode.getStartCassettes());
                    } catch (ServiceException e) {
                        lotProcessJobExecFlagValidCheckOutRetCode = e.getData(Outputs.ObjLotProcessJobExecFlagValidCheckOut.class);
                        if (!Validations.isEquals(retCodeConfig.getNoProcessJobExecFlag(), e.getCode())) {
                            log.info("lotProcessJobExecFlagValidCheck is not OK && != RC_NO_PROCESSJOBEXECFLAG");
                            e.setData(out);
                            throw e;
                        }
                        if (Validations.isEquals(retCodeConfig.getNoProcessJobExecFlag(), e.getCode())) {
                            log.info("lotProcessJobExecFlagValidCheck() rc == RC_NO_PROCESSJOBEXECFLAG");
                            // Lot Hold + error mail
                            // create hold mail message
                            String message = null;
                            try {
                                message = lotMethod.lotSamplingMessageCreate(objCommonIn,
                                        lotProcessJobExecFlagValidCheckOutRetCode.getLotID(),
                                        BizConstant.SP_SAMPLING_ERROR_MAIL,
                                        e.getMessage());
                            } catch (ServiceException ex) {
                                ex.setData(out);
                                throw ex;
                            }

                            Outputs.ObjLotCurrentOperationInfoGetOut lotCurrentOperationInfoGetOutRetCode = null;
                            try {
                                lotCurrentOperationInfoGetOutRetCode = lotMethod.lotCurrentOperationInfoGet(objCommonIn, lotProcessJobExecFlagValidCheckOutRetCode.getLotID());
                            } catch (ServiceException ex) {
                                ex.setData(out);
                                throw ex;
                            }

                            Infos.LotHoldReq lotHoldReq = new Infos.LotHoldReq();
                            lotHoldReq.setHoldType(BizConstant.SP_HOLDTYPE_LOTHOLD);
                            lotHoldReq.setHoldReasonCodeID(new ObjectIdentifier(BizConstant.SP_REASON_WAFERSAMPLINGHOLD));
                            lotHoldReq.setHoldUserID(objCommonIn.getUser().getUserID());
                            lotHoldReq.setResponsibleOperationMark(BizConstant.SP_RESPONSIBLEOPERATION_CURRENT);
                            lotHoldReq.setRouteID(lotCurrentOperationInfoGetOutRetCode.getRouteID());
                            lotHoldReq.setOperationNumber(lotCurrentOperationInfoGetOutRetCode.getOperationNumber());
                            lotHoldReq.setClaimMemo("");
                            List<Infos.LotHoldReq> holdReqList = new ArrayList<Infos.LotHoldReq>();
                            holdReqList.add(lotHoldReq);

                            try {
                                lotService.sxHoldLotReq(objCommonIn, lotProcessJobExecFlagValidCheckOutRetCode.getLotID(), holdReqList);
                            } catch (ServiceException ex) {
                                ex.setData(out);
                                throw ex;
                            }
                            //error Mail Set
                            //Set System Message
                            Infos.SysMsgStockInfo sysMsg = new Infos.SysMsgStockInfo();
                            sysMsg.setSubSystemID(BizConstant.SP_SUBSYSTEMID_MM);
                            sysMsg.setSystemMessageCode(BizConstant.SP_SYSTEMMSGCODE_SMPLERR);
                            sysMsg.setSystemMessageText(message);
                            sysMsg.setNotifyFlag(true);
                            sysMsg.setEquipmentID(whereNextTransferEqpOut.getEquipmentID());
                            sysMsg.setEquipmentStatus("");
                            sysMsg.setStockerID(dummy);
                            sysMsg.setStockerStatus("");
                            sysMsg.setAGVID(dummy);
                            sysMsg.setAGVStatus("");
                            sysMsg.setLotID(lotProcessJobExecFlagValidCheckOutRetCode.getLotID());
                            sysMsg.setLotStatus("");
                            sysMsg.setRouteID(dummy);
                            sysMsg.setOperationID(dummy);
                            sysMsg.setOperationNumber("");
                            sysMsg.setSystemMessageTimeStamp(CimDateUtils.convertToSpecString(objCommonIn.getTimeStamp().getReportTimeStamp()));
                            //List<Infos.SysMsgStockInfo> sysMstStockInfoList = new ArrayList<>();
                            List<Infos.SysMsgStockInfo> sysMstStockInfoList = CimArrayUtils.getSize(out.getSysMstStockInfoList()) == 0 ? new ArrayList<>() : out.getSysMstStockInfoList();
                            sysMstStockInfoList.add(sysMsg);
                            out.setSysMstStockInfoList(sysMstStockInfoList);
                            throw new ServiceException(retCodeConfig.getNoProcessJobExecFlag(), out);
                        }
                    }
                    log.info("Set processJobExecFlag based on wafer sampling setting end. ");
                    /*--------------------------------------*/
                    /*   Request to Start Lot Reservation   */
                    /*--------------------------------------*/
                    Results.MoveInReserveReqResult moveInReserveReqResultRetCode = null;
                    ObjectIdentifier slmStartLotsReservationReqOut = null;
                    //----------------------------------------------------------------
                    //  Get SLM Capability of Equipment BR info
                    //----------------------------------------------------------------
                    Infos.EqpBrInfo eqpBrInfoWhereNextEqp = null;
                    try {
                        eqpBrInfoWhereNextEqp = equipmentMethod.equipmentBRInfoGetDR(objCommonIn, whereNextTransferEqpOut.getEquipmentID());
                    } catch (ServiceException e) {
                        e.setData(out);
                        throw e;
                    }
                    log.info("SLMCapabilityFlag : {}", CimBooleanUtils.convertBooleanToLong(eqpBrInfoWhereNextEqp.isFmcCapabilityFlag()));

                    Boolean moveInReserveOK = true;
                    RetCode moveInReserveFailCode = new RetCode();
                    if (CimBooleanUtils.isFalse(eqpBrInfoWhereNextEqp.isFmcCapabilityFlag())) {
                        log.info("SLM Capability = OFF");
                        // Change TransactionID
                        Infos.ObjCommon strTmpObjCommonIn = new Infos.ObjCommon();
                        strTmpObjCommonIn = objCommonIn;
                        strTmpObjCommonIn.setTransactionID("ODISW001");// TransactionID of MoveInReservationReq

                        Params.MoveInReserveReqParams params = new Params.MoveInReserveReqParams();
                        params.setEquipmentID(whereNextTransferEqpOut.getEquipmentID());
                        params.setPortGroupID(whereNextTransferEqpOut.getPortGroup());
                        params.setControlJobID(new ObjectIdentifier());
                        params.setStartCassetteList(whereNextTransferEqpOut.getStartCassetteList());
                        params.setApcIFControlStatus(tmpAPCIFControlStatus.get());
                        try {
                            moveInReserveReqResultRetCode = (Results.MoveInReserveReqResult) dispatchService.sxMoveInReserveReq(strTmpObjCommonIn, params);
                        } catch (ServiceException e) {
                            moveInReserveOK = false;
                            moveInReserveFailCode.setReturnCode(new OmCode(e.getCode(), e.getMessage()));
                            moveInReserveFailCode.setTransactionID(e.getTransactionID());
                            moveInReserveFailCode.setMessageText(e.getMessage());
                            moveInReserveFailCode.setReasonText(e.getReasonText());
                            moveInReserveFailCode.setObject(e.getData());
                        }
                    } else {
                        List<Infos.MtrlOutSpec> slmStartReserveInfoForDeliveryMakeOut = null;
                        log.info("SLM Capability = ON");
                        if (CimStringUtils.equals(BizConstant.SP_SLM_SWITCH_ON, eqpInfoInqResult.getEquipmentBasicInfo().getFmcSwitch())) {
                            try {
                                slmStartReserveInfoForDeliveryMakeOut = slmMethod.slmStartReserveInfoForDeliveryMake(objCommonIn, whereNextTransferEqpOut.getEquipmentID(), whereNextTransferEqpOut.getPortGroup(), whereNextTransferEqpOut.getStartCassetteList());
                            } catch (ServiceException e) {
                                e.setData(out);
                                throw e;
                            }
                        }
                        log.info("call FMC move in reserve");
                        // Change TransactionID
                        Infos.ObjCommon strTmpObjCommonIn = new Infos.ObjCommon();
                        strTmpObjCommonIn = objCommonIn;
                        strTmpObjCommonIn.setTransactionID("OFMCW003");// SLMStartLotsReservationReq

                        Params.SLMStartLotsReservationReqInParams params = new Params.SLMStartLotsReservationReqInParams();
                        params.setEquipmentID(whereNextTransferEqpOut.getEquipmentID());
                        params.setPortGroupID(whereNextTransferEqpOut.getPortGroup());
                        params.setStartCassetteList(whereNextTransferEqpOut.getStartCassetteList());
                        params.setMtrlOutSpecList(slmStartReserveInfoForDeliveryMakeOut);
                        params.setOpeMemo("");

                        try {
                            slmStartLotsReservationReqOut = fmcService.sxSLMStartLotsReservationReq(strTmpObjCommonIn, params, tmpAPCIFControlStatus);
                        } catch (ServiceException e) {
                            moveInReserveOK = false;
                            moveInReserveFailCode.setReturnCode(new OmCode(e.getCode(), e.getMessage()));
                            moveInReserveFailCode.setTransactionID(e.getTransactionID());
                            moveInReserveFailCode.setMessageText(e.getMessage());
                            moveInReserveFailCode.setReasonText(e.getReasonText());
                            moveInReserveFailCode.setObject(e.getData());
                        }
                    }
                    if (CimBooleanUtils.isTrue(moveInReserveOK)) {
                        log.info("moveInReserve is OK");
                        ObjectIdentifier controlJobID = null;
                        if (CimBooleanUtils.isFalse(eqpBrInfoWhereNextEqp.isFmcCapabilityFlag())) {
                            log.info("SLMCapabilityFlag == FALSE");
                            controlJobID = moveInReserveReqResultRetCode.getControlJobID();
                        } else {
                            log.info("SLMCapabilityFlag == TRUE");
                            controlJobID = slmStartLotsReservationReqOut;
                        }
                        /*------------------------------------------*/
                        /*   Make txMultiCarrierXferReq parameter   */
                        /*------------------------------------------*/
                        Outputs.ObjMultiCarrierXferFillInOTMSW005InParmOut multiCarrierXferFillInOTMSW005InParmRetCode = null;
                        try {
                            multiCarrierXferFillInOTMSW005InParmRetCode = cassetteMethod.multiCarrierXferFillInOTMSW005InParm(objCommonIn,
                                    whereNextTransferEqpOut.getEquipmentID(), whereNextTransferEqpOut.getStartCassetteList());
                        } catch (ServiceException e) {
                            log.info("multiCarrierXferFillInOTMSW005InParm is not OK");
                            log.info("Request to Move In Reservation Cancel");

                            Params.MoveInReserveCancelReqParams in = new Params.MoveInReserveCancelReqParams();
                            in.setEquipmentID(whereNextTransferEqpOut.getEquipmentID());
                            in.setControlJobID(controlJobID);
                            try {
                                Results.MoveInReserveCancelReqResult moveInReserveCancelReqResultRetCode = dispatchService.sxMoveInReserveCancelReqService(objCommonIn, in);
                            } catch (ServiceException ex) {
                                log.info("dispatchService is not OK");
                            }
                            e.setData(out);
                            throw e;
                        }
                        log.info("Send Transfer Request to TMS. (EQP -> EQP) ");
                        /*-----------------------------------------------*/
                        /*   Send Transfer Request to TMS. (EQP -> EQP)   */
                        /*-----------------------------------------------*/
                        try {
                            this.sxMultipleCarrierTransferReq(objCommonIn,
                                    false, "S", multiCarrierXferFillInOTMSW005InParmRetCode.getStrCarrierXferReq());
                        } catch (ServiceException e) {
                            log.info("sxMultipleCarrierTransferReq() is Not OK");
                            //Prepare e-mail Message Text
                            StringBuilder msg = new StringBuilder("<<< Transfer Error!  (EQP -> EQP) >>>\n");
                            msg.append("\n")
                                    .append("Equipment ID         : ").append(ObjectIdentifier.fetchValue(equipmentID)).append("\n");
                            if (CimArrayUtils.getSize(multiCarrierXferFillInOTMSW005InParmRetCode.getStrCarrierXferReq()) > 0) {
                                for (Infos.CarrierXferReq carrierXferReq : multiCarrierXferFillInOTMSW005InParmRetCode.getStrCarrierXferReq()) {
                                    msg.append("-----------------------------------------\n")
                                            .append("carrier ID           : ").append(ObjectIdentifier.fetchValue(carrierXferReq.getCarrierID())).append("\n")
                                            .append("Lot ID               : ").append(ObjectIdentifier.fetchValue(carrierXferReq.getLotID())).append("\n")
                                            .append("From Machine ID      : ").append(ObjectIdentifier.fetchValue(carrierXferReq.getFromMachineID())).append("\n")
                                            .append("From Port ID         : ").append(ObjectIdentifier.fetchValue(carrierXferReq.getFromPortID())).append("\n")
                                            .append("To Stocker Group     : ").append(carrierXferReq.getToStockerGroup()).append("\n");
                                    int toMachineLen = CimArrayUtils.getSize(carrierXferReq.getStrToMachine());
                                    if (toMachineLen == 0) {
                                        msg.append("To Machine ID        : Nothing\n");
                                    } else {
                                        for (Infos.ToMachine toMachine : carrierXferReq.getStrToMachine()) {
                                            msg.append("To Machine ID        : ").append(ObjectIdentifier.fetchValue(toMachine.getToMachineID())).append("\n")
                                                    .append("To Port ID           : ").append(ObjectIdentifier.fetchValue(toMachine.getToPortID())).append("\n");
                                        }
                                    }
                                }
                            }
                            msg.append("-----------------------------------------\n")
                                    .append("Transaction ID       : ").append(e.getTransactionID()).append("\n")
                                    .append("Return Code          : ").append(e.getCode()).append("\n")
                                    .append("Message Text         : ").append(e.getMessage()).append("\n")
                                    .append("Reason Text          : ").append(e.getReasonText()).append("\n");
                            //Set System Message
                            Infos.SysMsgStockInfo sysMsg = new Infos.SysMsgStockInfo();
                            sysMsg.setSubSystemID(BizConstant.SP_SUBSYSTEMID_MM);
                            sysMsg.setSystemMessageCode(BizConstant.SP_SYSTEMMSGCODE_DELIVERYERROR);
                            sysMsg.setSystemMessageText(msg.toString());
                            sysMsg.setNotifyFlag(true);
                            sysMsg.setEquipmentID(equipmentID);
                            sysMsg.setEquipmentStatus("");
                            sysMsg.setStockerID(dummy);
                            sysMsg.setStockerStatus("");
                            sysMsg.setAGVID(dummy);
                            sysMsg.setAGVStatus("");
                            sysMsg.setLotID(dummy);
                            sysMsg.setLotStatus("");
                            sysMsg.setRouteID(dummy);
                            sysMsg.setOperationID(dummy);
                            sysMsg.setOperationNumber("");
                            sysMsg.setSystemMessageTimeStamp(CimDateUtils.convertToSpecString(objCommonIn.getTimeStamp().getReportTimeStamp()));
                            List<Infos.SysMsgStockInfo> sysMstStockInfoList = CimArrayUtils.getSize(out.getSysMstStockInfoList()) == 0 ? new ArrayList<>() : out.getSysMstStockInfoList();
                            sysMstStockInfoList.add(sysMsg);
                            out.setSysMstStockInfoList(sysMstStockInfoList);
                            /*---------------------------------------------*/
                            /*   Request to Start Lot Reservation Cancel   */
                            /*---------------------------------------------*/
                            Params.MoveInReserveCancelReqParams moveInReserveCancelReqParams = new Params.MoveInReserveCancelReqParams();
                            moveInReserveCancelReqParams.setEquipmentID(whereNextTransferEqpOut.getEquipmentID());
                            moveInReserveCancelReqParams.setControlJobID(controlJobID);
                            moveInReserveCancelReqParams.setOpeMemo("");
                            try {
                                Results.MoveInReserveCancelReqResult moveInReserveCancelReqResultRetCode = dispatchService.sxMoveInReserveCancelReqService(objCommonIn, moveInReserveCancelReqParams);
                            } catch (ServiceException ex) {
                                log.info("dispatchService is not OK");
                            }
                            e.setData(out);
                            throw e;
                        }
                        log.info("OK! OK! OK! [EQP to EQP]  Return to Main");
                        /*--------------------*/
                        /*   Return to Main   */
                        /*--------------------*/
                        return out;
                    } else {
                        log.info("moveInReserve is Error");
                        if (CimBooleanUtils.isFalse(eqpBrInfoWhereNextEqp.isFmcCapabilityFlag())) {
                            log.info("moveInReserve is not OK");
                        } else {
                            log.info("FMCMoveInReserve is not OK");
                        }
                        //---------------------------------------------------------------------------------------------
                        //  Prepare message text string for case of failure of start lot reservation.
                        //  If start lot reservation for equipment is fail, this messageText is filled.
                        //---------------------------------------------------------------------------------------------
                        //Prepare e-mail Message Text
                        StringBuilder msg = new StringBuilder("<<< MoveInReservation Error!  (EQP -> EQP) >>>\n");
                        msg.append("\n")
                                .append("Equipment ID         : ").append(ObjectIdentifier.fetchValue(whereNextTransferEqpOut.getEquipmentID())).append("\n")
                                .append("Lot IDs              : ");
                        boolean bLotSet = false;
                        if (CimArrayUtils.getSize(whereNextTransferEqpOut.getStartCassetteList()) > 0) {
                            for (Infos.StartCassette startCassette : whereNextTransferEqpOut.getStartCassetteList()) {
                                if (CimArrayUtils.getSize(startCassette.getLotInCassetteList()) > 0) {
                                    for (Infos.LotInCassette lotInCassette : startCassette.getLotInCassetteList()) {
                                        if (!ObjectIdentifier.isEmptyWithValue(lotInCassette.getLotID())) {
                                            if (CimBooleanUtils.isTrue(bLotSet)) {
                                                msg.append(", ");
                                            }
                                            msg.append(ObjectIdentifier.fetchValue(lotInCassette.getLotID()));
                                            bLotSet = true;
                                        }
                                    }
                                }
                            }
                        }
                        msg.append("\n")
                                .append("Transaction ID       : ").append(moveInReserveFailCode.getTransactionID()).append("\n")
                                .append("Return Code          : ").append(moveInReserveFailCode.getReturnCode().getCode()).append("\n")
                                .append("Message Text         : ").append(moveInReserveFailCode.getReturnCode().getMessage()).append("\n")
                                .append("Reason Text          : ").append(moveInReserveFailCode.getReasonText()).append("\n");
                        Infos.SysMsgStockInfo sysMsg = new Infos.SysMsgStockInfo();
                        sysMsg.setSubSystemID(BizConstant.SP_SUBSYSTEMID_MM);
                        sysMsg.setSystemMessageCode(BizConstant.SP_SYSTEMMSGCODE_DELIVERYERROR);
                        sysMsg.setSystemMessageText(msg.toString());
                        sysMsg.setNotifyFlag(true);
                        sysMsg.setEquipmentID(equipmentID);
                        sysMsg.setEquipmentStatus("");
                        sysMsg.setStockerID(dummy);
                        sysMsg.setStockerStatus("");
                        sysMsg.setAGVID(dummy);
                        sysMsg.setAGVStatus("");
                        sysMsg.setLotID(dummy);
                        sysMsg.setLotStatus("");
                        sysMsg.setRouteID(dummy);
                        sysMsg.setOperationID(dummy);
                        sysMsg.setOperationNumber("");
                        sysMsg.setSystemMessageTimeStamp(CimDateUtils.convertToSpecString(objCommonIn.getTimeStamp().getReportTimeStamp()));
                        List<Infos.SysMsgStockInfo> sysMstStockInfoList = CimArrayUtils.getSize(out.getSysMstStockInfoList()) == 0 ? new ArrayList<>() : out.getSysMstStockInfoList();
                        sysMstStockInfoList.add(sysMsg);
                        out.setSysMstStockInfoList(sysMstStockInfoList);
                        Validations.check(true, moveInReserveFailCode.getReturnCode());
                    }
                }
            }
            if (CimBooleanUtils.isFalse(bLoadReqFlag)) {
                log.info("UnLoadReq Process RUN!!");
                List<Infos.CarrierXferReq> tmpCarrierXferReqList = new ArrayList<>();
                int nILen = CimArrayUtils.getSize(equipmentTargetPortPickupOut.getEqpTargetPortInfo().getPortGroups().get(0).getStrPortID());
                if (nILen > 0) {
                    for (Infos.PortID portID : equipmentTargetPortPickupOut.getEqpTargetPortInfo().getPortGroups().get(0).getStrPortID()) {
                        log.info("Get Where Next Stocker");
                        log.info("cassetteID: {}", ObjectIdentifier.fetchValue(portID.getCassetteID()));
                        /*----------------------------*/
                        /*   Get Where Next Stocker   */
                        /*----------------------------*/
                        Results.WhereNextStockerInqResult strWhereNextStockerInqResult = null;
                        /*------------------------*/
                        /*   Call RTD Interface   */
                        /*------------------------*/
                        log.info("Call RTD Interface ");
                        Outputs.ObjCassetteDeliveryRTDInterfaceReqOut cassetteDeliveryRTDInterfaceReqOutRetCode = null;
                        try {
                            cassetteDeliveryRTDInterfaceReqOutRetCode = cassetteMethod.cassetteDeliveryRTDInterfaceReq(objCommonIn,
                                    BizConstant.SP_RTD_FUNCTION_CODE_WHERENEXT, portID.getCassetteID());
                            log.info("Normal End      RTD Interface of WhereNextInterBayInqResult");
                            strWhereNextStockerInqResult = cassetteDeliveryRTDInterfaceReqOutRetCode == null ? null : cassetteDeliveryRTDInterfaceReqOutRetCode.getStrWhereNextStockerInqResult();
                        } catch (ServiceException e) {
                            log.info("Faild RTD Interface -----> Call Normal Function (WhereNextStockerInq)");
                            if (!Validations.isEquals(retCodeConfigEx.getRtdInterfaceSwitchOff(), e.getCode())
                                    && !Validations.isEquals(retCodeConfigEx.getNotFoundRTD(), e.getCode())) {
                                StringBuilder msg = new StringBuilder("<<< RTD Interface Error!  (WhereNext of CassetteDelivery) >>>\n");
                                msg.append("\n")
                                        .append("Equipment ID         : ").append(ObjectIdentifier.fetchValue(equipmentID)).append("\n")
                                        .append("-----------------------------------------\n")
                                        .append("Transaction ID       : ").append(e.getTransactionID()).append("\n")
                                        .append("Return Code          : ").append(e.getCode()).append("\n").append(e.getMessage()).append("\n")
                                        .append("Reason Text          : ").append(e.getReasonText()).append("\n");
                                //Set System Message
                                Infos.SysMsgStockInfo sysMsg = new Infos.SysMsgStockInfo();
                                sysMsg.setSubSystemID(BizConstant.SP_SUBSYSTEMID_MM);
                                sysMsg.setSystemMessageCode(BizConstant.SP_SYSTEMMSGCODE_RTDERROR);
                                sysMsg.setSystemMessageText(msg.toString());// up on the msg but not add
                                sysMsg.setNotifyFlag(true);
                                sysMsg.setEquipmentID(equipmentID);
                                sysMsg.setEquipmentStatus("");
                                sysMsg.setStockerID(dummy);
                                sysMsg.setStockerStatus("");
                                sysMsg.setAGVID(dummy);
                                sysMsg.setAGVStatus("");
                                sysMsg.setLotID(dummy);
                                sysMsg.setLotStatus("");
                                sysMsg.setRouteID(dummy);
                                sysMsg.setOperationID(dummy);
                                sysMsg.setOperationNumber("");
                                sysMsg.setSystemMessageTimeStamp(CimDateUtils.convertToSpecString(objCommonIn.getTimeStamp().getReportTimeStamp()));
                                List<Infos.SysMsgStockInfo> sysMstStockInfoList = CimArrayUtils.getSize(out.getSysMstStockInfoList()) == 0 ? new ArrayList<>() : out.getSysMstStockInfoList();
                                sysMstStockInfoList.add(sysMsg);
                                out.setSysMstStockInfoList(sysMstStockInfoList);
                            }
                            /*---------------------------------------------------*/
                            /*   Call Normal Function (WhereNextStockerInq)   */
                            /*---------------------------------------------------*/
                            try {
                                strWhereNextStockerInqResult = transferManagementSystemInqService.sxWhereNextStockerInq(objCommonIn,
                                        dummy,
                                        portID.getCassetteID());
                            } catch (ServiceException ex) {
                                if (!Validations.isSuccess(ex.getCode())) {
                                    StringBuilder msg = new StringBuilder("<<< WhereNext Error!  (EQP -> Stocker) >>>\n");
                                    msg.append("\n")
                                            .append("Equipment ID         : ").append(ObjectIdentifier.fetchValue(equipmentID)).append("\n")
                                            .append("-----------------------------------------\n")
                                            .append("Transaction ID       : ").append(ex.getTransactionID()).append("\n")
                                            .append("Return Code          : ").append(ex.getCode()).append("\n")
                                            .append("Message Text         : ").append(ex.getMessage()).append("\n")
                                            .append("Reason Text          : ").append(ex.getReasonText()).append("\n");
                                    //Set System Message
                                    Infos.SysMsgStockInfo sysMsg = new Infos.SysMsgStockInfo();
                                    sysMsg.setSubSystemID(BizConstant.SP_SUBSYSTEMID_MM);
                                    sysMsg.setSystemMessageCode(BizConstant.SP_SYSTEMMSGCODE_DELIVERYERROR);
                                    sysMsg.setSystemMessageText(msg.toString());// up on the msg but not add
                                    sysMsg.setNotifyFlag(true);
                                    sysMsg.setEquipmentID(equipmentID);
                                    sysMsg.setEquipmentStatus("");
                                    sysMsg.setStockerID(dummy);
                                    sysMsg.setStockerStatus("");
                                    sysMsg.setAGVID(dummy);
                                    sysMsg.setAGVStatus("");
                                    sysMsg.setLotID(dummy);
                                    sysMsg.setLotStatus("");
                                    sysMsg.setRouteID(dummy);
                                    sysMsg.setOperationID(dummy);
                                    sysMsg.setOperationNumber("");
                                    sysMsg.setSystemMessageTimeStamp(CimDateUtils.convertToSpecString(objCommonIn.getTimeStamp().getReportTimeStamp()));
                                    List<Infos.SysMsgStockInfo> sysMstStockInfoList = CimArrayUtils.getSize(out.getSysMstStockInfoList()) == 0 ? new ArrayList<>() : out.getSysMstStockInfoList();
                                    sysMstStockInfoList.add(sysMsg);
                                    out.setSysMstStockInfoList(sysMstStockInfoList);
                                    ex.setData(out);
                                    throw ex;
                                }
                            }
                        }
                        /*-------------------------------------------*/
                        /*   Make SingleCarrierTransferReq parameter   */
                        /*-------------------------------------------*/
                        Outputs.ObjSingleCarrierXferFillInOTMSW006InParmOut singleCarrierXferFillInOTMSW006InParmRetCode = null;
                        try {
                            singleCarrierXferFillInOTMSW006InParmRetCode = cassetteMethod.singleCarrierXferFillInOTMSW006InParm(objCommonIn,
                                    equipmentID, portID.getPortID(),
                                    portID.getCassetteID(),
                                    strWhereNextStockerInqResult);
                        } catch (ServiceException e) {
                            if (Validations.isEquals(retCodeConfigEx.getNoXferNeeded(), e.getCode())) {
                                log.info("No Transfer Job is requested by environment variable. Proceed to the Next Port...");
                                continue;
                            } else if (Validations.isEquals(retCodeConfigEx.getNoStockerForCurrentEqp(), e.getCode())) {
                                singleCarrierXferFillInOTMSW006InParmRetCode = e.getData(Outputs.ObjSingleCarrierXferFillInOTMSW006InParmOut.class);
                                Infos.CarrierXferReq carrierXferReq = singleCarrierXferFillInOTMSW006InParmRetCode.getStrCarrierXferReq();
                                log.info("An Error is Detected. Proceed to the Next Port...");
                                //Prepare e-mail Message Text(Skip)
                                StringBuilder msg = new StringBuilder("<<< Transfer Error!  (EQP -> Stocker) >>>\n");
                                msg.append("\n")
                                        .append("Equipment ID         : ").append(ObjectIdentifier.fetchValue(equipmentID)).append("\n")
                                        .append("-----------------------------------------\n")
                                        .append("carrier ID           : ").append(ObjectIdentifier.fetchValue(carrierXferReq.getCarrierID())).append("\n")
                                        .append("Lot ID               : ").append(ObjectIdentifier.fetchValue(carrierXferReq.getLotID())).append("\n")
                                        .append("From Machine ID      : ").append(ObjectIdentifier.fetchValue(carrierXferReq.getFromMachineID())).append("\n")
                                        .append("From Port ID         : ").append(ObjectIdentifier.fetchValue(carrierXferReq.getFromPortID())).append("\n")
                                        .append("To Stocker Group     : ").append(carrierXferReq.getToStockerGroup()).append("\n");
                                int toMachineLen = CimArrayUtils.getSize(carrierXferReq.getStrToMachine());
                                if (toMachineLen == 0) {
                                    msg.append("To Machine ID        : Nothing\n");
                                }
                                for (int j = 0; j < toMachineLen; j++) {
                                    msg.append("To Machine ID        : ").append(ObjectIdentifier.fetchValue(carrierXferReq.getStrToMachine().get(j).getToMachineID())).append("\n")
                                            .append("To Port ID           : ").append(ObjectIdentifier.fetchValue(carrierXferReq.getStrToMachine().get(j).getToPortID())).append("\n");
                                }

                                msg.append("-----------------------------------------\n")
                                        .append("Transaction ID       : ").append(e.getTransactionID()).append("\n")
                                        .append("Return Code          : ").append(e.getCode()).append("\n")
                                        .append("Message Text         : ").append(e.getMessage()).append("\n")
                                        .append("Reason Text          : ").append(e.getReasonText()).append("\n");
                                //Set System Message
                                Infos.SysMsgStockInfo sysMsg = new Infos.SysMsgStockInfo();
                                sysMsg.setSubSystemID(BizConstant.SP_SUBSYSTEMID_MM);
                                sysMsg.setSystemMessageCode(BizConstant.SP_SYSTEMMSGCODE_DELIVERYERROR);
                                sysMsg.setSystemMessageText(msg.toString());// up on the msg but not add
                                sysMsg.setNotifyFlag(true);
                                sysMsg.setEquipmentID(equipmentID);
                                sysMsg.setEquipmentStatus("");
                                sysMsg.setStockerID(dummy);
                                sysMsg.setStockerStatus("");
                                sysMsg.setAGVID(dummy);
                                sysMsg.setAGVStatus("");
                                sysMsg.setLotID(dummy);
                                sysMsg.setLotStatus("");
                                sysMsg.setRouteID(dummy);
                                sysMsg.setOperationID(dummy);
                                sysMsg.setOperationNumber("");
                                sysMsg.setSystemMessageTimeStamp(CimDateUtils.convertToSpecString(objCommonIn.getTimeStamp().getReportTimeStamp()));
                                List<Infos.SysMsgStockInfo> sysMstStockInfoList = CimArrayUtils.getSize(out.getSysMstStockInfoList()) == 0 ? new ArrayList<>() : out.getSysMstStockInfoList();
                                sysMstStockInfoList.add(sysMsg);
                                out.setSysMstStockInfoList(sysMstStockInfoList);
                                continue;
                            } else {
                                e.setData(out);
                                throw e;
                            }
                        }
                        /*------------------------------------------------------*/
                        /*  Check whether specified machine is eqp or stocker   */
                        /*------------------------------------------------------*/
                        Boolean isStorageFlag = false;
                        Infos.EqpStatusInfo eqpStatusInfo = null;
                        try {
                            eqpStatusInfo = equipmentMethod.equipmentStatusInfoGet(objCommonIn, equipmentID);
                        } catch (ServiceException e) {
                            if (Validations.isEquals(retCodeConfig.getNotFoundEqp(), e.getCode())) {
                                isStorageFlag = true;
                            } else {
                                e.setData(out);
                                throw e;
                            }
                        }
                        log.info("Equipment {} is Storage or not == {}", ObjectIdentifier.fetchValue(singleCarrierXferFillInOTMSW006InParmRetCode.getStrCarrierXferReq().getFromMachineID()), CimBooleanUtils.convertBooleanToLong(isStorageFlag));
                        if (CimBooleanUtils.isFalse(isStorageFlag)) {
                            /*--------------------------------------------*/
                            /* Lock Port object of machine                */
                            /*--------------------------------------------*/
                            try {
                                lockMethod.objectLockForEquipmentResource(objCommonIn,
                                        singleCarrierXferFillInOTMSW006InParmRetCode.getStrCarrierXferReq().getFromMachineID(),
                                        singleCarrierXferFillInOTMSW006InParmRetCode.getStrCarrierXferReq().getFromPortID(),
                                        BizConstant.SP_CLASSNAME_POSPORTRESOURCE
                                );
                            } catch (ServiceException e) {
                                e.setData(out);
                                throw e;
                            }
                            log.info("Locked port object  : {}", ObjectIdentifier.fetchValue(singleCarrierXferFillInOTMSW006InParmRetCode.getStrCarrierXferReq().getFromPortID()));
                            /*-----------------------------------------------------------------------------*/
                            /*                                                                             */
                            /*   Check Equipment Port for Sending Request to XM. (EQP -> Stocker)          */
                            /*                                                                             */
                            /*-----------------------------------------------------------------------------*/
                            Inputs.ObjEquipmentPortStateCheckForCassetteDeliveryIn deliveryIn = new Inputs.ObjEquipmentPortStateCheckForCassetteDeliveryIn();
                            deliveryIn.setEquipmentID(singleCarrierXferFillInOTMSW006InParmRetCode.getStrCarrierXferReq().getFromMachineID());
                            deliveryIn.setPortID(singleCarrierXferFillInOTMSW006InParmRetCode.getStrCarrierXferReq().getFromPortID());
                            try {
                                equipmentMethod.equipmentPortStateCheckForCassetteDelivery(objCommonIn, deliveryIn);
                            } catch (ServiceException e) {
                                e.setData(out);
                                throw e;
                            }
                        }
                        log.info("Send Request to TMS. (EQP -> Stocker)");
                        /*------------------------------------------*/
                        /*   Send Request to TMS. (EQP -> Stocker)   */
                        /*------------------------------------------*/
                        Params.SingleCarrierTransferReqParam singleCarrierTransferReqParam = new Params.SingleCarrierTransferReqParam();
                        singleCarrierTransferReqParam.setRerouteFlag(singleCarrierXferFillInOTMSW006InParmRetCode.getRerouteFlag());
                        Infos.CarrierXferReq strCarrierXferReq = singleCarrierXferFillInOTMSW006InParmRetCode.getStrCarrierXferReq();
                        singleCarrierTransferReqParam.setCarrierID(strCarrierXferReq.getCarrierID());
                        singleCarrierTransferReqParam.setLotID(strCarrierXferReq.getLotID());
                        singleCarrierTransferReqParam.setZoneType(strCarrierXferReq.getZoneType());
                        singleCarrierTransferReqParam.setN2PurgeFlag(strCarrierXferReq.getN2PurgeFlag());
                        singleCarrierTransferReqParam.setFromMachineID(strCarrierXferReq.getFromMachineID());
                        singleCarrierTransferReqParam.setFromPortID(strCarrierXferReq.getFromPortID());
                        singleCarrierTransferReqParam.setToStockerGroup(strCarrierXferReq.getToStockerGroup());
                        singleCarrierTransferReqParam.setToMachine(strCarrierXferReq.getStrToMachine());
                        singleCarrierTransferReqParam.setExpectedStartTime(strCarrierXferReq.getExpectedStartTime());
                        singleCarrierTransferReqParam.setExpectedEndTime(strCarrierXferReq.getExpectedEndTime());
                        singleCarrierTransferReqParam.setMandatoryFlag(strCarrierXferReq.getMandatoryFlag());
                        singleCarrierTransferReqParam.setPriority(strCarrierXferReq.getPriority());

                        Results.SingleCarrierTransferReqResult singleCarrierTransferReqResultRetCode = null;
                        try {
                            singleCarrierTransferReqResultRetCode = this.sxSingleCarrierTransferReq(objCommonIn, singleCarrierTransferReqParam);
                        } catch (ServiceException e) {
                            log.info("SingleCarrierTransferReq is not OK");
                            //Prepare e-mail Message Text (skip)
                            StringBuilder msg = new StringBuilder("<<< Transfer Error!  (EQP -> Stocker) >>>\n");
                            msg.append("\n")
                                    .append("Equipment ID         : ").append(ObjectIdentifier.fetchValue(equipmentID)).append("\n")
                                    .append("-----------------------------------------\n")
                                    .append("carrier ID           : ").append(ObjectIdentifier.fetchValue(strCarrierXferReq.getCarrierID())).append("\n")
                                    .append("Lot ID               : ").append(ObjectIdentifier.fetchValue(strCarrierXferReq.getLotID())).append("\n")
                                    .append("From Machine ID      : ")
                                    .append(ObjectIdentifier.fetchValue(strCarrierXferReq.getFromMachineID()))
                                    .append("From Port ID         : ").append(ObjectIdentifier.fetchValue(strCarrierXferReq.getFromPortID())).append("\n")
                                    .append("To Stocker Group     : ").append(strCarrierXferReq.getToStockerGroup()).append("\n");
                            int toMachineLen = CimArrayUtils.getSize(strCarrierXferReq.getStrToMachine());
                            if (toMachineLen == 0) {
                                msg.append("To Machine ID        : Nothing\n");
                            }
                            for (int j = 0; j < toMachineLen; j++) {
                                msg.append("To Machine ID        : ").append(ObjectIdentifier.fetchValue(strCarrierXferReq.getStrToMachine().get(j).getToMachineID())).append("\n")
                                        .append("To Port ID           : ").append(ObjectIdentifier.fetchValue(strCarrierXferReq.getStrToMachine().get(j).getToPortID())).append("\n");
                            }
                            msg.append("-----------------------------------------\n")
                                    .append("Transaction ID       : ").append(e.getTransactionID()).append("\n")
                                    .append("Return Code          : ").append(e.getCode()).append("\n")
                                    .append("Message Text         : ").append(e.getMessage()).append("\n")
                                    .append("Reason Text          : ").append(e.getReasonText()).append("\n");
                            //Set System Message
                            Infos.SysMsgStockInfo sysMsg = new Infos.SysMsgStockInfo();
                            sysMsg.setSubSystemID(BizConstant.SP_SUBSYSTEMID_MM);
                            sysMsg.setSystemMessageCode(BizConstant.SP_SYSTEMMSGCODE_DELIVERYERROR);
                            sysMsg.setSystemMessageText(msg.toString());
                            sysMsg.setNotifyFlag(true);
                            sysMsg.setEquipmentID(equipmentID);
                            sysMsg.setEquipmentStatus("");
                            sysMsg.setStockerID(dummy);
                            sysMsg.setStockerStatus("");
                            sysMsg.setAGVID(dummy);
                            sysMsg.setAGVStatus("");
                            sysMsg.setLotID(dummy);
                            sysMsg.setLotStatus("");
                            sysMsg.setRouteID(dummy);
                            sysMsg.setOperationID(dummy);
                            sysMsg.setOperationNumber("");
                            sysMsg.setSystemMessageTimeStamp(CimDateUtils.convertToSpecString(objCommonIn.getTimeStamp().getReportTimeStamp()));
                            List<Infos.SysMsgStockInfo> sysMstStockInfoList = CimArrayUtils.getSize(out.getSysMstStockInfoList()) == 0 ? new ArrayList<>() : out.getSysMstStockInfoList();
                            sysMstStockInfoList.add(sysMsg);
                            out.setSysMstStockInfoList(sysMstStockInfoList);
                            e.setData(out);
                            throw e;
                        }
                    }
                }
                bLoadReqFlag = false;
                log.info("OK! OK! OK! [EQP to Stocker]  Return to Main");
            }
        }

        /*-----------------*/
        /*   Load Request  */
        /*-----------------*/
        if (CimBooleanUtils.isTrue(bLoadReqFlag)) {
            log.info("bLoadReqFlag == TRUE");
            if (CimStringUtils.equals(BizConstant.SP_PORTRSC_PORTSTATE_LOADREQ, equipmentTargetPortPickupOut.getTargetPortType())) {
                log.info("Load Request");
                Validations.check(CimBooleanUtils.isFalse(eqpInfoInqResult.getEquipmentStatusInfo().getEquipmentAvailableFlag()), retCodeConfig.getInvalidEquipmentStatus(), ObjectIdentifier.fetchValue(equipmentID), "NotAvailable");
                log.info("Get WIP Lots <<< WhatNextLotListInfo >>>");
                /*------------------*/
                /*   Get WIP Lots   */
                /*------------------*/
                /*------------------------*/
                /*   Call RTD Interface   */
                /*------------------------*/
                Results.WhatNextLotListResult strWhatNextInqResult = null;
                Outputs.ObjCassetteDeliveryRTDInterfaceReqOut cassetteDeliveryRTDInterfaceReqOutRetCode = null;
                try {
                    cassetteDeliveryRTDInterfaceReqOutRetCode = cassetteMethod.cassetteDeliveryRTDInterfaceReq(objCommonIn, BizConstant.SP_RTD_FUNCTION_CODE_WHATNEXT, equipmentID);
                    log.info("Normal End RTD Interface of txWhatNextInq");
                    strWhatNextInqResult = null == cassetteDeliveryRTDInterfaceReqOutRetCode ? null : cassetteDeliveryRTDInterfaceReqOutRetCode.getStrWhatNextInqResult();
                } catch (ServiceException e) {
                    log.info("Faild RTD Interface Call Normal Function (WhatNextLotListInfo)");
                    if (!Validations.isEquals(retCodeConfigEx.getRtdInterfaceSwitchOff(), e.getCode())
                            && !Validations.isEquals(retCodeConfigEx.getNotFoundRTD(), e.getCode())) {
                        //Set System Message (skip)
                        StringBuilder msg = new StringBuilder("<<< RTD Interface Error!  (WhatNext of CassetteDelivery) >>>\n");
                        msg.append("\n")
                                .append("Equipment ID         : ").append(ObjectIdentifier.fetchValue(equipmentID)).append("\n")
                                .append("-----------------------------------------\n")
                                .append("Transaction ID       : ").append(e.getTransactionID()).append("\n")
                                .append("Return Code          : ").append(e.getCode()).append("\n")
                                .append("Message Text         : ").append(e.getMessage()).append("\n")
                                .append("Reason Text          : ").append(e.getReasonText()).append("\n");
                        Infos.SysMsgStockInfo sysMsg = new Infos.SysMsgStockInfo();
                        sysMsg.setSubSystemID(BizConstant.SP_SUBSYSTEMID_MM);
                        sysMsg.setSystemMessageCode(BizConstant.SP_SYSTEMMSGCODE_RTDERROR);
                        sysMsg.setSystemMessageText(msg.toString());
                        sysMsg.setNotifyFlag(true);
                        sysMsg.setEquipmentID(equipmentID);
                        sysMsg.setEquipmentStatus("");
                        sysMsg.setStockerID(dummy);
                        sysMsg.setStockerStatus("");
                        sysMsg.setAGVID(dummy);
                        sysMsg.setAGVStatus("");
                        sysMsg.setLotID(dummy);
                        sysMsg.setLotStatus("");
                        sysMsg.setRouteID(dummy);
                        sysMsg.setOperationID(dummy);
                        sysMsg.setOperationNumber("");
                        sysMsg.setSystemMessageTimeStamp(CimDateUtils.convertToSpecString(objCommonIn.getTimeStamp().getReportTimeStamp()));
                        List<Infos.SysMsgStockInfo> sysMstStockInfoList = CimArrayUtils.getSize(out.getSysMstStockInfoList()) == 0 ? new ArrayList<>() : out.getSysMstStockInfoList();
                        sysMstStockInfoList.add(sysMsg);
                        out.setSysMstStockInfoList(sysMstStockInfoList);
                    }
                    /*-------------------------------------------------*/
                    /*   Call Normal Function (WhatNextLotListInfo)   */
                    /*-------------------------------------------------*/
                    Params.WhatNextLotListParams whatNextLotListParams = new Params.WhatNextLotListParams();
                    whatNextLotListParams.setEquipmentID(equipmentID);
                    whatNextLotListParams.setSelectCriteria(ObjectIdentifier.buildWithValue(BizConstant.SP_DP_SELECTCRITERIA_AUTO3));
                    try {
                        strWhatNextInqResult = dispatchInqService.sxWhatNextLotListInfo(objCommonIn, whatNextLotListParams);
                    } catch (ServiceException ex) {
                        if (!Validations.isEquals(retCodeConfigEx.getNoWipLot(), ex.getCode())) {
                            //Set System Message
                            StringBuilder msg = new StringBuilder("<<< WhatNext Error!  (Stocker -> EQP) >>>\n");
                            msg.append("\n")
                                    .append("Equipment ID         : ").append(ObjectIdentifier.fetchValue(equipmentID)).append("\n")
                                    .append("-----------------------------------------\n")
                                    .append("Transaction ID       : ").append(ex.getTransactionID()).append("\n")
                                    .append("Return Code          : ").append(ex.getCode()).append("\n")
                                    .append("Message Text         : ").append(ex.getMessage()).append("\n")
                                    .append("Reason Text          : ").append(ex.getReasonText()).append("\n");
                            Infos.SysMsgStockInfo sysMsg = new Infos.SysMsgStockInfo();
                            sysMsg.setSubSystemID(BizConstant.SP_SUBSYSTEMID_MM);
                            sysMsg.setSystemMessageCode(BizConstant.SP_SYSTEMMSGCODE_DELIVERYERROR);
                            sysMsg.setSystemMessageText(msg.toString());
                            sysMsg.setNotifyFlag(true);
                            sysMsg.setEquipmentID(equipmentID);
                            sysMsg.setEquipmentStatus("");
                            sysMsg.setStockerID(dummy);
                            sysMsg.setStockerStatus("");
                            sysMsg.setAGVID(dummy);
                            sysMsg.setAGVStatus("");
                            sysMsg.setLotID(dummy);
                            sysMsg.setLotStatus("");
                            sysMsg.setRouteID(dummy);
                            sysMsg.setOperationID(dummy);
                            sysMsg.setOperationNumber("");
                            sysMsg.setSystemMessageTimeStamp(CimDateUtils.convertToSpecString(objCommonIn.getTimeStamp().getReportTimeStamp()));
                            List<Infos.SysMsgStockInfo> sysMstStockInfoList = CimArrayUtils.getSize(out.getSysMstStockInfoList()) == 0 ? new ArrayList<>() : out.getSysMstStockInfoList();
                            sysMstStockInfoList.add(sysMsg);
                            out.setSysMstStockInfoList(sysMstStockInfoList);
                        }
                        ex.setData(out);
                        throw ex;
                    }
                }
                //---------------------------------------------------------------------------------------------
                // Priority is given to the SLM destination cassette.
                //---------------------------------------------------------------------------------------------
                log.info("SLM retrieving cassette process (LoadReq)");
                // Get Equipment Container Information
                Infos.EqpContainerInfo eqpContainerInfo = null;
                try {
                    eqpContainerInfo = equipmentMethod.equipmentContainerInfoGetDR(objCommonIn, equipmentID);
                } catch (ServiceException e) {
                    e.setData(out);
                    throw e;
                }
                int lenEqpCont = CimArrayUtils.getSize(eqpContainerInfo.getEqpContainerList());
                log.info("lenEqpCont: {}", lenEqpCont);
                Validations.check(0 == lenEqpCont, retCodeConfig.getNotFoundEquipmentContainer());
                List<Infos.EqpContainerPosition> strEqpContPosList = eqpContainerInfo.getEqpContainerList().get(0).getEqpContainerPosition();
                int lenEqpContPos = CimArrayUtils.getSize(strEqpContPosList);
                log.info("lenEqpContPos: {}", lenEqpContPos);
                if (lenEqpContPos > 0) {
                    for (Infos.EqpContainerPosition eqpContainerPosition : strEqpContPosList) {
                        log.info("containerPositionID: {}", ObjectIdentifier.fetchValue(eqpContainerPosition.getContainerPositionID()));
                        log.info("destCassetteID: {}", ObjectIdentifier.fetchValue(eqpContainerPosition.getDestCassetteID()));
                        log.info("destPortID: {}", ObjectIdentifier.fetchValue(eqpContainerPosition.getDestPortID()));
                        if (ObjectIdentifier.isEmptyWithValue(eqpContainerPosition.getDestCassetteID())) {
                            log.info("destCassetteID is null");
                            continue;
                        }
                        if (ObjectIdentifier.isEmptyWithValue(eqpContainerPosition.getDestPortID())) {
                            log.info("destPostID is null");
                            continue;
                        }
                        String targetPortGroup = null;
                        log.info("lenPortGroup: {}", lenPortGroup);
                        for (Infos.PortGroup portGroup : targetPortGroupList) {
                            int lenPort = CimArrayUtils.getSize(portGroup.getStrPortID());
                            if (lenPort > 0) {
                                for (Infos.PortID portID : portGroup.getStrPortID()) {
                                    if (ObjectIdentifier.equalsWithValue(portID.getPortID(), eqpContainerPosition.getDestCassetteID())) {
                                        targetPortGroup = portGroup.getPortGroup();
                                        break;
                                    }
                                }
                                if (CimStringUtils.isNotEmpty(targetPortGroup)) {
                                    break;
                                }
                            }
                        }
                        if (CimStringUtils.isEmpty(targetPortGroup)) {
                            log.info("targetPortGroup is null");
                            continue;
                        }
                        // Get AssociatedPort
                        log.info("call equipmentPortInfoGet()");
                        Infos.EqpPortInfo eqpPortInfo = null;
                        try {
                            eqpPortInfo = equipmentMethod.equipmentPortInfoGet(objCommonIn, equipmentID);
                        } catch (ServiceException e) {
                            e.setData(out);
                            throw e;
                        }
                        ObjectIdentifier unloadPortID = null;
                        int lenPort = CimArrayUtils.getSize(eqpPortInfo.getEqpPortStatuses());
                        if (lenPort > 0) {
                            for (Infos.EqpPortStatus eqpPortStatus : eqpPortInfo.getEqpPortStatuses()) {
                                if (ObjectIdentifier.equalsWithValue(eqpPortStatus.getPortID(), eqpContainerPosition.getDestPortID())) {
                                    unloadPortID = eqpPortStatus.getAssociatedPortID();
                                    log.info("unloadPortID: {}", ObjectIdentifier.fetchValue(unloadPortID));
                                    break;
                                }
                            }
                        }
                        List<Infos.StartCassette> strStartCassetteForNPW = new ArrayList<>();
                        Infos.StartCassette startCassette = new Infos.StartCassette();
                        startCassette.setLoadSequenceNumber(1L);
                        startCassette.setCassetteID(eqpContainerPosition.getDestCassetteID());
                        startCassette.setLoadPurposeType(BizConstant.SP_LOADPURPOSETYPE_SLMRETRIEVING);
                        startCassette.setLoadPortID(eqpContainerPosition.getDestPortID());
                        startCassette.setUnloadPortID(unloadPortID);
                        strStartCassetteForNPW.add(startCassette);

                        Params.NPWCarrierReserveReqParams npwCarrierReserveReqParams = new Params.NPWCarrierReserveReqParams();
                        npwCarrierReserveReqParams.setEquipmentID(equipmentID);
                        npwCarrierReserveReqParams.setPortGroupID(targetPortGroup);
                        npwCarrierReserveReqParams.setStartCassetteList(strStartCassetteForNPW);
                        npwCarrierReserveReqParams.setOpeMemo("");
                        try {
                            this.sxNPWCarrierReserveReq(objCommonIn, npwCarrierReserveReqParams);
                        } catch (ServiceException e) {
                            if (Validations.isEquals(retCodeConfig.getAlreadyDispatchReservedCassette(), e.getCode())) {
                                log.info("Cassette is already reserved");
                                continue;
                            } else {
                                e.setData(out);
                                throw e;
                            }
                        }
                        log.info("call multiCarrierXferFillInOTMSW005InParm()");
                        Outputs.ObjMultiCarrierXferFillInOTMSW005InParmOut multiCarrierXferFillInOTMSW005InParmRetCode = null;
                        try {
                            multiCarrierXferFillInOTMSW005InParmRetCode = cassetteMethod.multiCarrierXferFillInOTMSW005InParm(objCommonIn,
                                    equipmentID, strStartCassetteForNPW);
                        } catch (ServiceException e) {
                            log.info("multiCarrierXferFillInOTMSW005InParm() is not OK");

                            List<Infos.NPWXferCassette> strNPWXferCassette = new ArrayList<>();
                            Infos.NPWXferCassette npwXferCassette = new Infos.NPWXferCassette();
                            npwXferCassette.setLoadSequenceNumber(1L);
                            npwXferCassette.setCassetteID(strStartCassetteForNPW.get(0).getCassetteID());
                            npwXferCassette.setLoadPurposeType(strStartCassetteForNPW.get(0).getLoadPurposeType());
                            npwXferCassette.setLoadPortID(strStartCassetteForNPW.get(0).getLoadPortID());
                            npwXferCassette.setUnloadPortID(strStartCassetteForNPW.get(0).getUnloadPortID());
                            strNPWXferCassette.add(npwXferCassette);
                            //【noted】: the code need notify to EAP or not ? now we set true
                            try {
                                this.sxNPWCarrierReserveCancelReq(objCommonIn, equipmentID, targetPortGroup, strNPWXferCassette, true, "");
                            } catch (ServiceException ex) {
                                log.info("NPWCarrierReserveCancelReq is not OK");
                            }
                            e.setData(out);
                            throw e;
                        }

                        Boolean bReRouteFlag = false;
                        String reRouteXferFlag = StandardProperties.OM_XFER_REROUTE_FLAG.getValue();
                        log.info("OM_XFER_REROUTE_FLAG: {}", reRouteXferFlag);
                        if (CimStringUtils.equals(BizConstant.VALUE_ONE, reRouteXferFlag)) {
                            log.info("reRouteXferFlag is 1");
                            bReRouteFlag = true;
                        }
                        /*---------------------------------------------------*/
                        /*   Send Transfer Request to TMS. (Stocker -> EQP)   */
                        /*---------------------------------------------------*/
                        List<Infos.CarrierXferReq> strCarrierXferReq = multiCarrierXferFillInOTMSW005InParmRetCode.getStrCarrierXferReq();
                        try {
                            this.sxMultipleCarrierTransferReq(objCommonIn, bReRouteFlag, "S", strCarrierXferReq);
                        } catch (ServiceException e) {
                            log.info("MultipleCarrierTransferReq() is not OK");
                            //Prepare e-mail Message Text
                            StringBuilder msg = new StringBuilder("<<< Transfer Error!  (Stocker -> EQP) >>>\n");
                            msg.append("\n")
                                    .append("Equipment ID         : ").append(equipmentID.getValue()).append("\n");
                            if (CimArrayUtils.getSize(strCarrierXferReq) > 0) {
                                for (Infos.CarrierXferReq carrierXferReq : strCarrierXferReq) {
                                    msg.append("-----------------------------------------\n")
                                            .append("carrier ID           : ").append(ObjectIdentifier.fetchValue(carrierXferReq.getCarrierID())).append("\n")
                                            .append("Lot ID               : ").append(ObjectIdentifier.fetchValue(carrierXferReq.getLotID())).append("\n")
                                            .append("From Machine ID      : ").append(ObjectIdentifier.fetchValue(carrierXferReq.getFromMachineID())).append("\n")
                                            .append("From Port ID         : ").append(ObjectIdentifier.fetchValue(carrierXferReq.getFromPortID())).append("\n")
                                            .append("To Stocker Group     : ").append(carrierXferReq.getToStockerGroup()).append("\n");
                                    List<Infos.ToMachine> strToMachine = carrierXferReq.getStrToMachine();
                                    int lenToEqp = CimArrayUtils.getSize(strToMachine);
                                    if (lenToEqp == 0) {
                                        msg.append("To Machine ID        : Nothing\n");
                                    } else {
                                        for (Infos.ToMachine toMachine : strToMachine) {
                                            msg.append("To Machine ID        : ").append(ObjectIdentifier.fetchValue(toMachine.getToMachineID())).append("\n")
                                                    .append("To Port ID           : ").append(ObjectIdentifier.fetchValue(toMachine.getToPortID())).append("\n");
                                        }
                                    }
                                }
                            }
                            msg.append("-----------------------------------------\n")
                                    .append("Transaction ID       : ").append(e.getTransactionID()).append("\n")
                                    .append("Return Code          : ").append(e.getCode()).append("\n")
                                    .append("Message Text         : ").append(e.getMessage()).append("\n")
                                    .append("Reason Text          : ").append(e.getReasonText()).append("\n");
                            Infos.SysMsgStockInfo sysMsg = new Infos.SysMsgStockInfo();
                            sysMsg.setSubSystemID(BizConstant.SP_SUBSYSTEMID_MM);
                            sysMsg.setSystemMessageCode(BizConstant.SP_SYSTEMMSGCODE_DELIVERYERROR);
                            sysMsg.setSystemMessageText(msg.toString());
                            sysMsg.setNotifyFlag(true);
                            sysMsg.setEquipmentID(equipmentID);
                            sysMsg.setEquipmentStatus("");
                            sysMsg.setStockerID(dummy);
                            sysMsg.setStockerStatus("");
                            sysMsg.setAGVID(dummy);
                            sysMsg.setAGVStatus("");
                            sysMsg.setLotID(dummy);
                            sysMsg.setLotStatus("");
                            sysMsg.setRouteID(dummy);
                            sysMsg.setOperationID(dummy);
                            sysMsg.setOperationNumber("");
                            sysMsg.setSystemMessageTimeStamp(CimDateUtils.convertToSpecString(objCommonIn.getTimeStamp().getReportTimeStamp()));
                            List<Infos.SysMsgStockInfo> sysMstStockInfoList = CimArrayUtils.getSize(out.getSysMstStockInfoList()) == 0 ? new ArrayList<>() : out.getSysMstStockInfoList();
                            sysMstStockInfoList.add(sysMsg);
                            out.setSysMstStockInfoList(sysMstStockInfoList);

                            log.info("Request to NPWCarrierReserveCancelReq");
                            List<Infos.NPWXferCassette> strNPWXferCassette = new ArrayList<>();
                            Infos.NPWXferCassette npwXferCassette = new Infos.NPWXferCassette();
                            npwXferCassette.setLoadSequenceNumber(1L);
                            npwXferCassette.setCassetteID(strStartCassetteForNPW.get(0).getCassetteID());
                            npwXferCassette.setLoadPurposeType(strStartCassetteForNPW.get(0).getLoadPurposeType());
                            npwXferCassette.setLoadPortID(strStartCassetteForNPW.get(0).getLoadPortID());
                            npwXferCassette.setUnloadPortID(strStartCassetteForNPW.get(0).getUnloadPortID());
                            strNPWXferCassette.add(npwXferCassette);
                            //【noted】: the code need notify to EAP or not ? now we set true
                            try {
                                this.sxNPWCarrierReserveCancelReq(objCommonIn, equipmentID, targetPortGroup, strNPWXferCassette, true, "");
                            } catch (ServiceException ex) {
                                log.info("NPWCarrierReserveCancelReq is not OK");
                            }
                            e.setData(out);
                            throw e;
                        }
                        return out;
                    }
                }
                log.info("Normal LoadReq process");
                log.info("lenPortGroup: {}", lenPortGroup);
                for (Infos.PortGroup portGroup : targetPortGroupList) {
                    List<Infos.PortGroup> strPortGroupList = new ArrayList<>();
                    strPortGroupList.add(portGroup);
                    log.info("Select Multiple Lots <<< whatNextLotListToStartCassetteForSLMDeliveryReq >>>");
                    /*--------------------------*/
                    /*   Select Multiple Lots   */
                    /*--------------------------*/
                    log.info("Select Multiple Lots ");
                    Results.WhatNextLotListResult convWhatNextInqResult = strWhatNextInqResult;
                    List<Infos.StartCassette> whatNextLotListToStartCassetteForSLMDeliveryOut = null;
                    try {
                        whatNextLotListToStartCassetteForSLMDeliveryOut = whatNextMethod.whatNextLotListToStartCassetteForSLMDeliveryReq(objCommonIn,
                                equipmentID,
                                strPortGroupList,
                                convWhatNextInqResult);
                    } catch (ServiceException e) {
                        if (portGroup.equals(targetPortGroupList.get(targetPortGroupList.size() - 1))) {
                            log.info("is last loop. so return error");
                            e.setData(out);
                            throw e;
                        } else {
                            log.info("continue next PortGroup!!");
                            continue;
                        }
                    }
                    int tmpFPCAdoptFlag = StandardProperties.OM_DOC_ENABLE_FLAG.getIntValue();
                    if (tmpFPCAdoptFlag == 1) {
                        log.info("DOC Adopt Flag is ON. Now apply FPCInfo.");

                        String exchangeType = BizConstant.SP_FPC_EXCHANGETYPE_STARTRESERVEINFO;
                        try {
                            whatNextLotListToStartCassetteForSLMDeliveryOut = fpcMethod.fpcStartCassetteInfoExchange(objCommonIn, exchangeType, equipmentID, whatNextLotListToStartCassetteForSLMDeliveryOut);
                        } catch (ServiceException e) {
                            e.setData(out);
                            throw e;
                        }
                    } else {
                        log.info("DOC Adopt Flag is OFF");
                    }
                    log.info("Set processJobExecFlag based on wafer sampling setting. ");

                    Boolean notSendIgnoreMail = false;
                    OmCode startCassette_processJobExecFlag_SetRc = retCodeConfig.getSucc();
                    Outputs.ObjStartCassetteProcessJobExecFlagSetOut startCassetteProcessJobExecFlagSetOutRetCode = null;
                    try {
                        startCassetteProcessJobExecFlagSetOutRetCode = startCassetteMethod.startCassetteProcessJobExecFlagSet(objCommonIn,
                                whatNextLotListToStartCassetteForSLMDeliveryOut, equipmentID);
                    } catch (ServiceException ex) {
                        startCassetteProcessJobExecFlagSetOutRetCode = ex.getData(Outputs.ObjStartCassetteProcessJobExecFlagSetOut.class);
                        if (!Validations.isEquals(retCodeConfig.getNoSmplSetting(), ex.getCode())) {
                            // When errors have occurred, hold add the error lot and send e-mail to notify that.
                            notSendIgnoreMail = true;
                            startCassette_processJobExecFlag_SetRc = retCodeConfig.getInvalidSmplSetting();
                        }
                    }
                    //hold lots and send E-Mails if error
                    int mailLen = CimArrayUtils.getSize(startCassetteProcessJobExecFlagSetOutRetCode.getSamplingMessage());
                    if (CimArrayUtils.getSize(startCassetteProcessJobExecFlagSetOutRetCode.getSamplingMessage()) > 0) {
                        for (Infos.ObjSamplingMessageAttribute samplingMessageAttribute : startCassetteProcessJobExecFlagSetOutRetCode.getSamplingMessage()) {
                            if (BizConstant.SP_SAMPLING_ERROR_MAIL == samplingMessageAttribute.getMessageType()
                                    || BizConstant.SP_SAMPLING_WARN_MAIL == samplingMessageAttribute.getMessageType()) {
                                log.info("messageType == SP_Sampling_error_mail || SP_Sampling_warn_mail ");
                                // Lot Hold
                                Outputs.ObjLotCurrentOperationInfoGetOut lotCurrentOperationInfoGetRetCode = null;
                                try {
                                    lotCurrentOperationInfoGetRetCode = lotMethod.lotCurrentOperationInfoGet(objCommonIn, samplingMessageAttribute.getLotID());
                                } catch (ServiceException e) {
                                    e.setData(out);
                                    throw e;
                                }

                                Infos.LotHoldReq lotHoldReq = new Infos.LotHoldReq();
                                lotHoldReq.setHoldType(BizConstant.SP_HOLDTYPE_LOTHOLD);
                                lotHoldReq.setHoldReasonCodeID(new ObjectIdentifier(BizConstant.SP_REASON_WAFERSAMPLINGHOLD));
                                lotHoldReq.setHoldUserID(objCommonIn.getUser().getUserID());
                                lotHoldReq.setResponsibleOperationMark(BizConstant.SP_RESPONSIBLEOPERATION_CURRENT);
                                lotHoldReq.setRouteID(lotCurrentOperationInfoGetRetCode.getRouteID());
                                lotHoldReq.setOperationNumber(lotCurrentOperationInfoGetRetCode.getOperationNumber());
                                lotHoldReq.setClaimMemo("");
                                List<Infos.LotHoldReq> holdReqList = new ArrayList<Infos.LotHoldReq>();
                                holdReqList.add(lotHoldReq);
                                try {
                                    lotService.sxHoldLotReq(objCommonIn, samplingMessageAttribute.getLotID(), holdReqList);
                                } catch (ServiceException e) {
                                    e.setData(out);
                                    throw e;
                                }
                            }
                            //error Mail Set
                            if (CimBooleanUtils.isFalse(notSendIgnoreMail) || BizConstant.SP_SAMPLING_WARN_MAIL != samplingMessageAttribute.getMessageType()) {
                                //When notSendIgnoreMail == TRUE, just the Lot Hold mail is to be sent.
                                String messageText = "";
                                if (BizConstant.SP_SAMPLING_WARN_MAIL == samplingMessageAttribute.getMessageType()) {
                                    //----------------------------------------------------------------
                                    // create mail text if message type == SP_Sampling_warn_mail;
                                    // startCassette_processJobExecFlag_Set__090 returns just error text. Caller should switch text handling.
                                    //----------------------------------------------------------------
                                    try {
                                        messageText = lotMethod.lotSamplingMessageCreate(objCommonIn,
                                                samplingMessageAttribute.getLotID(),
                                                BizConstant.SP_SAMPLING_ERROR_MAIL,
                                                samplingMessageAttribute.getMessageText());
                                    } catch (ServiceException e) {
                                        e.setData(out);
                                        throw e;
                                    }
                                } else {
                                    messageText = samplingMessageAttribute.getMessageText();
                                }
                                //Set System Message
                                Infos.SysMsgStockInfo sysMsg = new Infos.SysMsgStockInfo();
                                sysMsg.setSubSystemID(BizConstant.SP_SUBSYSTEMID_MM);
                                sysMsg.setSystemMessageCode(BizConstant.SP_SYSTEMMSGCODE_SMPLERR);
                                sysMsg.setSystemMessageText(messageText);
                                sysMsg.setNotifyFlag(true);
                                sysMsg.setEquipmentID(equipmentID);
                                sysMsg.setEquipmentStatus("");
                                sysMsg.setStockerID(dummy);
                                sysMsg.setStockerStatus("");
                                sysMsg.setAGVID(dummy);
                                sysMsg.setAGVStatus("");
                                sysMsg.setLotID(samplingMessageAttribute.getLotID());
                                sysMsg.setLotStatus("");
                                sysMsg.setRouteID(dummy);
                                sysMsg.setOperationID(dummy);
                                sysMsg.setOperationNumber("");
                                sysMsg.setSystemMessageTimeStamp(CimDateUtils.convertToSpecString(objCommonIn.getTimeStamp().getReportTimeStamp()));
                                List<Infos.SysMsgStockInfo> sysMstStockInfoList = CimArrayUtils.getSize(out.getSysMstStockInfoList()) == 0 ? new ArrayList<>() : out.getSysMstStockInfoList();
                                sysMstStockInfoList.add(sysMsg);
                                out.setSysMstStockInfoList(sysMstStockInfoList);
                            }
                        }
                    }
                    if (CimBooleanUtils.isTrue(notSendIgnoreMail)) {
                        log.info("notSendIgnoreMail == TRUE  return this TX.");
                        if (Validations.isEquals(retCodeConfig.getSucc(), startCassette_processJobExecFlag_SetRc)) {
                            return out;
                        } else {
                            throw new ServiceException(startCassette_processJobExecFlag_SetRc, out);
                        }
                    }
                    //valid check of processJobExecFlag for each lot.
                    //Check every lot has at least one wafer with processJobExecFlag == TRUE
                    Outputs.ObjLotProcessJobExecFlagValidCheckOut lotProcessJobExecFlagValidCheckOut = null;
                    try {
                        lotProcessJobExecFlagValidCheckOut = lotMethod.lotProcessJobExecFlagValidCheck(objCommonIn, startCassetteProcessJobExecFlagSetOutRetCode.getStartCassettes());
                    } catch (ServiceException e) {
                        if (!Validations.isEquals(retCodeConfig.getNoProcessJobExecFlag(), e.getCode())) {
                            log.info("lotProcessJobExecFlagValidCheck() is not OK && != RC_NO_PROCESSJOBEXECFLAG");
                            e.setData(out);
                            throw e;
                        }
                        if (Validations.isEquals(retCodeConfig.getNoProcessJobExecFlag(), e.getCode())) {
                            lotProcessJobExecFlagValidCheckOut = e.getData(Outputs.ObjLotProcessJobExecFlagValidCheckOut.class);
                            log.info("lotProcessJobExecFlagValidCheck() rc == RC_NO_PROCESSJOBEXECFLAG");
                            // Lot Hold + error mail
                            // create hold mail message
                            String lotSamplingMessageCreateRetCode = null;
                            try {
                                lotSamplingMessageCreateRetCode = lotMethod.lotSamplingMessageCreate(objCommonIn,
                                        lotProcessJobExecFlagValidCheckOut.getLotID(),
                                        BizConstant.SP_SAMPLING_ERROR_MAIL,
                                        e.getMessage());
                            } catch (ServiceException ex) {
                                ex.setData(out);
                                throw ex;
                            }

                            Outputs.ObjLotCurrentOperationInfoGetOut lotCurrentOperationInfo = null;
                            try {
                                lotCurrentOperationInfo = lotMethod.lotCurrentOperationInfoGet(objCommonIn, lotProcessJobExecFlagValidCheckOut.getLotID());
                            } catch (ServiceException ex) {
                                ex.setData(out);
                                throw ex;
                            }

                            Infos.LotHoldReq lotHoldReq = new Infos.LotHoldReq();
                            lotHoldReq.setHoldType(BizConstant.SP_HOLDTYPE_LOTHOLD);
                            lotHoldReq.setHoldReasonCodeID(new ObjectIdentifier(BizConstant.SP_REASON_WAFERSAMPLINGHOLD));
                            lotHoldReq.setHoldUserID(objCommonIn.getUser().getUserID());
                            lotHoldReq.setResponsibleOperationMark(BizConstant.SP_RESPONSIBLEOPERATION_CURRENT);
                            lotHoldReq.setRouteID(lotCurrentOperationInfo.getRouteID());
                            lotHoldReq.setOperationNumber(lotCurrentOperationInfo.getOperationNumber());
                            lotHoldReq.setClaimMemo("");
                            List<Infos.LotHoldReq> holdReqList = new ArrayList<Infos.LotHoldReq>();
                            holdReqList.add(lotHoldReq);
                            try {
                                lotService.sxHoldLotReq(objCommonIn, lotProcessJobExecFlagValidCheckOut.getLotID(), holdReqList);
                            } catch (ServiceException ex) {
                                ex.setData(out);
                                throw ex;
                            }
                            //error Mail Set
                            Infos.SysMsgStockInfo sysMsg = new Infos.SysMsgStockInfo();
                            sysMsg.setSubSystemID(BizConstant.SP_SUBSYSTEMID_MM);
                            sysMsg.setSystemMessageCode(BizConstant.SP_SYSTEMMSGCODE_SMPLERR);
                            sysMsg.setSystemMessageText(lotSamplingMessageCreateRetCode);
                            sysMsg.setNotifyFlag(true);
                            sysMsg.setEquipmentID(equipmentID);
                            sysMsg.setEquipmentStatus("");
                            sysMsg.setStockerID(dummy);
                            sysMsg.setStockerStatus("");
                            sysMsg.setAGVID(dummy);
                            sysMsg.setAGVStatus("");
                            sysMsg.setLotID(lotProcessJobExecFlagValidCheckOut.getLotID());
                            sysMsg.setLotStatus("");
                            sysMsg.setRouteID(dummy);
                            sysMsg.setOperationID(dummy);
                            sysMsg.setOperationNumber("");
                            sysMsg.setSystemMessageTimeStamp(CimDateUtils.convertToSpecString(objCommonIn.getTimeStamp().getReportTimeStamp()));
                            List<Infos.SysMsgStockInfo> sysMstStockInfoList = CimArrayUtils.getSize(out.getSysMstStockInfoList()) == 0 ? new ArrayList<>() : out.getSysMstStockInfoList();
                            sysMstStockInfoList.add(sysMsg);
                            out.setSysMstStockInfoList(sysMstStockInfoList);
                            throw new ServiceException(retCodeConfig.getNoProcessJobExecFlag(), out);
                        }
                    }

                    whatNextLotListToStartCassetteForSLMDeliveryOut = startCassetteProcessJobExecFlagSetOutRetCode.getStartCassettes();
                    log.info("Set processJobExecFlag based on wafer sampling setting end. ");
                    log.info("Request to Move In Reservation");
                    /*--------------------------------------*/
                    /*   Request to Start Lot Reservation   */
                    /*--------------------------------------*/
                    List<Infos.MtrlOutSpec> slmStartReserveInfoForDeliveryMakeOut = new ArrayList<>();
                    if (CimStringUtils.equals(BizConstant.SP_SLM_SWITCH_ON, eqpInfoInqResult.getEquipmentBasicInfo().getFmcSwitch())) {
                        try {
                            slmStartReserveInfoForDeliveryMakeOut = slmMethod.slmStartReserveInfoForDeliveryMake(objCommonIn, equipmentID,strPortGroupList.get(0).getPortGroup() ,whatNextLotListToStartCassetteForSLMDeliveryOut);
                        } catch (ServiceException e) {
                            e.setData(out);
                            throw e;
                        }
                    }
                    log.info("call FMC move in reserve");
                    // Change TransactionID
                    Infos.ObjCommon strTmpObjCommonIn = new Infos.ObjCommon();
                    strTmpObjCommonIn = objCommonIn;
                    strTmpObjCommonIn.setTransactionID("OFMCW003");// SLMStartLotsReservationReq

                    Params.SLMStartLotsReservationReqInParams params = new Params.SLMStartLotsReservationReqInParams();
                    params.setEquipmentID(equipmentID);
                    params.setPortGroupID(strPortGroupList.get(0).getPortGroup());
                    params.setStartCassetteList(whatNextLotListToStartCassetteForSLMDeliveryOut);
                    params.setMtrlOutSpecList(slmStartReserveInfoForDeliveryMakeOut);
                    params.setOpeMemo("");

                    ObjectIdentifier slmStartLotsReservationReqOut = null;
                    try {
                        slmStartLotsReservationReqOut = fmcService.sxSLMStartLotsReservationReq(strTmpObjCommonIn, params, tmpAPCIFControlStatus);
                    } catch (ServiceException ex) {
                        /*------------------------*/
                        /*   Set System Message   */
                        /*------------------------*/
                        log.info("Set System Message.   SLMStartLotsReservationReq is not OK");
                        //---------------------------------------------------------------------------------------------
                        //  Prepare message text string for case of failure of start lot reservation
                        //  Only if start lot resercatioin for current equipment is fail, this messageText is filled
                        //---------------------------------------------------------------------------------------------
                        //-------------------------------------------------------
                        // Prepare e-mail Message Text
                        //-------------------------------------------------------
                        StringBuilder msg = new StringBuilder("<<< FMC MoveInReservation Error!  (Stocker -> EQP) >>>\n");
                        msg.append("\n")
                                .append("Equipment ID         : ").append(ObjectIdentifier.fetchValue(equipmentID)).append("\n")
                                .append("Lot IDs              : ");
                        boolean bLotSet = false;
                        if (CimArrayUtils.getSize(whatNextLotListToStartCassetteForSLMDeliveryOut) > 0){
                            for (Infos.StartCassette startCassette : whatNextLotListToStartCassetteForSLMDeliveryOut) {
                                if (CimArrayUtils.getSize(startCassette.getLotInCassetteList()) > 0){
                                    for (Infos.LotInCassette lotInCassette : startCassette.getLotInCassetteList()) {
                                        if (!ObjectIdentifier.isEmpty(lotInCassette.getLotID())){
                                            if (CimBooleanUtils.isTrue(bLotSet)){
                                                msg.append(", ");
                                            }
                                            msg.append(ObjectIdentifier.fetchValue(lotInCassette.getLotID()));
                                            bLotSet = true;
                                        }
                                    }
                                }
                            }
                        }
                        msg.append("\n")
                                .append("Transaction ID       : ").append(ex.getTransactionID()).append("\n")
                                .append("Return Code          : ").append(ex.getCode()).append("\n")
                                .append("Message Text         : ").append(ex.getMessage()).append("\n")
                                .append("Reason Text          : ").append(ex.getReasonText()).append("\n");
                        Infos.SysMsgStockInfo sysMsg = new Infos.SysMsgStockInfo();
                        sysMsg.setSubSystemID(BizConstant.SP_SUBSYSTEMID_MM);
                        sysMsg.setSystemMessageCode(BizConstant.SP_SYSTEMMSGCODE_DELIVERYERROR);
                        sysMsg.setSystemMessageText(msg.toString());
                        sysMsg.setNotifyFlag(true);
                        sysMsg.setEquipmentID(equipmentID);
                        sysMsg.setEquipmentStatus("");
                        sysMsg.setStockerID(dummy);
                        sysMsg.setStockerStatus("");
                        sysMsg.setAGVID(dummy);
                        sysMsg.setAGVStatus("");
                        sysMsg.setLotID(dummy);
                        sysMsg.setLotStatus("");
                        sysMsg.setRouteID(dummy);
                        sysMsg.setOperationID(dummy);
                        sysMsg.setOperationNumber("");
                        sysMsg.setSystemMessageTimeStamp(CimDateUtils.convertToSpecString(objCommonIn.getTimeStamp().getReportTimeStamp()));
                        List<Infos.SysMsgStockInfo> sysMstStockInfoList = CimArrayUtils.getSize(out.getSysMstStockInfoList()) == 0 ? new ArrayList<>() : out.getSysMstStockInfoList();
                        sysMstStockInfoList.add(sysMsg);
                        out.setSysMstStockInfoList(sysMstStockInfoList);
                        ex.setData(out);
                        throw ex;
                    }
                    log.info("Make MultipleCarrierTransferReq parameter");
                    /*------------------------------------------*/
                    /*   Make txMultiCarrierXferReq parameter   */
                    /*------------------------------------------*/
                    Outputs.ObjMultiCarrierXferFillInOTMSW005InParmOut multiCarrierXferFillInOTMSW005InParmRetCode = null;
                    try {
                        multiCarrierXferFillInOTMSW005InParmRetCode = cassetteMethod.multiCarrierXferFillInOTMSW005InParm(objCommonIn,
                                equipmentID, whatNextLotListToStartCassetteForSLMDeliveryOut);
                    } catch (ServiceException e) {
                        log.info("multiCarrierXferFillInOTMSW005InParm is not OK");
                        /*---------------------------------------------*/
                        /*   Request to Start Lot Reservation Cancel   */
                        /*---------------------------------------------*/
                        log.info("Request to Start Lot Reservation Cancel");

                        Params.MoveInReserveCancelReqParams input = new Params.MoveInReserveCancelReqParams();
                        input.setEquipmentID(equipmentID);
                        input.setControlJobID(slmStartLotsReservationReqOut);
                        input.setOpeMemo("");
                        try {
                            dispatchService.sxMoveInReserveCancelReqService(objCommonIn, input);
                        } catch (ServiceException ex) {
                            log.info("dispatchService is not OK");
                        }
                        throw new ServiceException(retCodeConfigEx.getStartLotReservationFail(), out);
                    }

                    Boolean bReRouteFlag = false;
                    String reRouteXferFlag = StandardProperties.OM_XFER_REROUTE_FLAG.getValue();
                    if (CimStringUtils.equals(BizConstant.VALUE_ONE, reRouteXferFlag)) {
                        log.info("reRouteXferFlag is 1");
                        bReRouteFlag = true;
                    }

                    log.info("Send Transfer Request to TMS. (Stocker -> EQP) ");
                    /*---------------------------------------------------*/
                    /*   Send Transfer Request to TMS. (Stocker -> EQP)   */
                    /*---------------------------------------------------*/
                    try {
                        this.sxMultipleCarrierTransferReq(objCommonIn, bReRouteFlag, "S", multiCarrierXferFillInOTMSW005InParmRetCode.getStrCarrierXferReq());
                    } catch (ServiceException e) {
                        log.info("MultipleCarrierTransferReq is not OK");
                        //Prepare e-mail Message Text
                        StringBuilder msg = new StringBuilder("<<< Transfer Error!  (Stocker -> EQP) >>>\n");
                        msg.append("\n")
                                .append("Equipment ID         : ").append(ObjectIdentifier.fetchValue(equipmentID)).append("\n");
                        if (CimArrayUtils.getSize(multiCarrierXferFillInOTMSW005InParmRetCode.getStrCarrierXferReq()) > 0){
                            for (Infos.CarrierXferReq carrierXferReq : multiCarrierXferFillInOTMSW005InParmRetCode.getStrCarrierXferReq()) {
                                msg.append("-----------------------------------------\n")
                                        .append("carrier ID           : ").append(ObjectIdentifier.fetchValue(carrierXferReq.getCarrierID())).append("\n")
                                        .append("Lot ID               : ").append(ObjectIdentifier.fetchValue(carrierXferReq.getLotID())).append("\n")
                                        .append("From Machine ID      : ").append(ObjectIdentifier.fetchValue(carrierXferReq.getFromMachineID())).append("\n")
                                        .append("From Port ID         : ").append(ObjectIdentifier.fetchValue(carrierXferReq.getFromPortID())).append("\n")
                                        .append("To Stocker Group     : ").append(carrierXferReq.getToStockerGroup()).append("\n");
                                int toMachineLen = CimArrayUtils.getSize(carrierXferReq.getStrToMachine());
                                if (toMachineLen == 0){
                                    msg.append("To Machine ID        : Nothing\n");
                                }else {
                                    for (Infos.ToMachine toMachine : carrierXferReq.getStrToMachine()) {
                                        msg.append("To Machine ID        : ").append(ObjectIdentifier.fetchValue(toMachine.getToMachineID())).append("\n")
                                                .append("To Port ID           : ").append(ObjectIdentifier.fetchValue(toMachine.getToPortID())).append("\n");
                                    }
                                }
                            }
                        }
                        msg.append("-----------------------------------------\n")
                                .append("Transaction ID       : ").append(e.getTransactionID()).append("\n")
                                .append("Return Code          : ").append(e.getCode()).append("\n")
                                .append("Message Text         : ").append(e.getMessage()).append("\n")
                                .append("Reason Text          : ").append(e.getReasonText()).append("\n");
                        Infos.SysMsgStockInfo sysMsg = new Infos.SysMsgStockInfo();
                        sysMsg.setSubSystemID(BizConstant.SP_SUBSYSTEMID_MM);
                        sysMsg.setSystemMessageCode(BizConstant.SP_SYSTEMMSGCODE_DELIVERYERROR);
                        sysMsg.setSystemMessageText(msg.toString());
                        sysMsg.setNotifyFlag(true);
                        sysMsg.setEquipmentID(equipmentID);
                        sysMsg.setEquipmentStatus("");
                        sysMsg.setStockerID(dummy);
                        sysMsg.setStockerStatus("");
                        sysMsg.setAGVID(dummy);
                        sysMsg.setAGVStatus("");
                        sysMsg.setLotID(dummy);
                        sysMsg.setLotStatus("");
                        sysMsg.setRouteID(dummy);
                        sysMsg.setOperationID(dummy);
                        sysMsg.setOperationNumber("");
                        sysMsg.setSystemMessageTimeStamp(CimDateUtils.convertToSpecString(objCommonIn.getTimeStamp().getReportTimeStamp()));
                        List<Infos.SysMsgStockInfo> sysMstStockInfoList = CimArrayUtils.getSize(out.getSysMstStockInfoList()) == 0 ? new ArrayList<>() : out.getSysMstStockInfoList();
                        sysMstStockInfoList.add(sysMsg);
                        out.setSysMstStockInfoList(sysMstStockInfoList);
                        //Request to Start Lot Reservation Cancel
                        Params.MoveInReserveCancelReqParams input = new Params.MoveInReserveCancelReqParams();
                        input.setEquipmentID(equipmentID);
                        input.setControlJobID(slmStartLotsReservationReqOut);
                        input.setOpeMemo("");
                        try {
                            dispatchService.sxMoveInReserveCancelReqService(objCommonIn, input);
                        } catch (ServiceException ex) {
                            log.info("dispatchService is not OK");
                        }
                        throw new ServiceException(retCodeConfigEx.getStartLotReservationFail(), out);
                    }
                    log.info("OK! OK! OK! [Stocker to EQP]  Return to Main");
                    /*--------------------*/
                    /*   End of Process   */
                    /*--------------------*/
                    break;
                }
            }
        }

        /*--------------------*/
        /*                    */
        /*   Return to Main   */
        /*                    */
        /*--------------------*/
        return out;
    }

    /**
     * description:
     * <p><br/></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommonIn Infos.ObjCommon
     * @param params Params.SingleCarrierTransferReqParam
     * @return IDPRetCode<Results.SingleCarrierTransferReqResult>
     * @author Yuri
     * @date 2018/10/30 17:22:05
     */
    @Override
    public Results.SingleCarrierTransferReqResult sxSingleCarrierTransferReq(Infos.ObjCommon objCommonIn, Params.SingleCarrierTransferReqParam params) {

        Results.SingleCarrierTransferReqResult singleCarrierTransferReqResult=new Results.SingleCarrierTransferReqResult();

        ObjectIdentifier fromMachineID = params.getFromMachineID();

        Boolean isStorageFlag = false;
        // Step 1: equipment_statusInfo_Get
        Infos.EqpStatusInfo eqpStatusInfoRetCode = null;
        try {
            eqpStatusInfoRetCode = equipmentMethod.equipmentStatusInfoGet(objCommonIn, fromMachineID);
        } catch (ServiceException e) {
            if (Validations.isEquals(retCodeConfig.getNotFoundEqp(), e.getCode())){
                isStorageFlag = true;
            } else {
                throw e;
            }
        }

        if (CimBooleanUtils.isFalse(isStorageFlag)) {
            // Step 2: object_LockForEquipmentResource
            lockMethod.objectLockForEquipmentResource(objCommonIn,params.getFromMachineID(),params.getFromPortID(),
                    BizConstant.SP_CLASSNAME_POSPORTRESOURCE);
        }
        // Step 3: object_Lock

        lockMethod.objectLock( objCommonIn,
                CimCassette.class,
                params.getCarrierID());


        // Step 4: equipment_machineType_CheckDR
        List<Infos.ToMachine> toMachines = params.getToMachine();
        if (!CimArrayUtils.isEmpty(toMachines)) {
            Boolean equipmentFlag = equipmentMethod.equipmentMachineTypeCheckDR(objCommonIn,
                    toMachines.get(0).getToMachineID());
            if (CimBooleanUtils.isFalse(equipmentFlag)) {
                // toMachine is a stocker. Next, check if the Xfer request is stocker out;
                if (CimStringUtils.isNotEmpty(ObjectIdentifier.fetchValue(toMachines.get(0).getToMachineID()))
                        && CimStringUtils.isNotEmpty(ObjectIdentifier.fetchValue(toMachines.get(0).getToPortID()))) {
                    // If this is a Manual out request, then cassette reservation is required
                    List<Infos.RsvLotCarrier> rsvLotCarriers = new ArrayList<>(1);
                    Infos.RsvLotCarrier rsvLotCarrier = new Infos.RsvLotCarrier();
                    rsvLotCarrier.setLotID(params.getLotID());
                    rsvLotCarrier.setCarrierID(params.getCarrierID());
                    rsvLotCarriers.add(rsvLotCarrier);
                    Params.CarrierReserveReqParam carrierReserveReqParam = new Params.CarrierReserveReqParam();
                    carrierReserveReqParam.setStrRsvLotCarrier(rsvLotCarriers);
                    carrierReserveReqParam.setClaimMemo("");

                    // Step 5: txCarrierReserveReq
                    Results.CarrierReserveReqResult carrierReserveReqResultRetCode = this.sxCarrierReserveReq(objCommonIn, carrierReserveReqParam);
                } else {
                    // If Control Job exists, then this request should be rejected
                    // Check cassette reservation Status

                    // Step 6: cassette_reservationInfo_GetDR
                    Outputs.CassetteReservationInfoGetDROut cassetteReservationInfoGetDROut = cassetteMethod.cassetteReservationInfoGetDR(objCommonIn, params.getCarrierID());
                    if (CimStringUtils.isNotEmpty(ObjectIdentifier.fetchValue(cassetteReservationInfoGetDROut.getControlJobID()))
                            || CimStringUtils.isNotEmpty(cassetteReservationInfoGetDROut.getNPWLoadPurposeType())) {
                        throw new ServiceException(retCodeConfig.getAlreadyDispatchReservedCassette());
                    }
                }
            }

        }

        // Set data for Xfer Request
        Params.TransportJobCreateReqParams transportJobCreateReqParam = new Params.TransportJobCreateReqParams();
        List<Infos.JobCreateArray> jobCreateArrays = new ArrayList<>(1);
        Infos.JobCreateArray jobCreateData = new Infos.JobCreateArray();
        jobCreateArrays.add(jobCreateData);
        transportJobCreateReqParam.setJobCreateData(jobCreateArrays);

        Boolean isStorageBool = false;

        // Step 7: equipment_statusInfo_Get
        try {
            eqpStatusInfoRetCode = equipmentMethod.equipmentStatusInfoGet(objCommonIn, fromMachineID);
        } catch (ServiceException e) {
            if (Validations.isEquals(retCodeConfig.getNotFoundEqp(), e.getCode())){
                isStorageBool = true;
            } else {
                log.error("equipment_statusInfo_Get() rc != RC_OK");
                throw e;
            }
        }

        log.debug(String.format("isStorageBool == %b, fromMachineID [ %s ]", isStorageBool, fromMachineID));

        Boolean dispatchStateMaintainRequiredFlag = false;

        if (isStorageBool) {
            jobCreateData.setFromMachineID(null);
            jobCreateData.setFromPortID(null);
        } else {
            jobCreateData.setFromMachineID(params.getFromMachineID());
            jobCreateData.setFromPortID(params.getFromPortID());
            dispatchStateMaintainRequiredFlag = true;
        }
        List<Infos.ToMachine> toMachineList = params.getToMachine();
        if (!CimArrayUtils.isEmpty(toMachineList)) {
            jobCreateData.setToMachine(new ArrayList<>(toMachineList.size()));
            for (Infos.ToMachine toMachine: toMachineList) {
                Infos.ToDestination toDestination = new Infos.ToDestination();
                toDestination.setToMachineID(toMachine.getToMachineID());
                toDestination.setToPortID(toMachine.getToPortID());
                jobCreateData.getToMachine().add(toDestination);
            }
        }
        transportJobCreateReqParam.setTransportType("S");
        transportJobCreateReqParam.setRerouteFlag(params.getRerouteFlag());
        jobCreateData.setCarrierID(params.getCarrierID());
        jobCreateData.setZoneType(params.getZoneType());
        jobCreateData.setN2PurgeFlag(params.getN2PurgeFlag());
        jobCreateData.setExpectedStartTime(params.getExpectedStartTime());
        jobCreateData.setExpectedEndTime(params.getExpectedEndTime());
        jobCreateData.setMandatoryFlag(params.getMandatoryFlag());
        jobCreateData.setPriority(params.getPriority());

        // Step 8: TMSMgr_SendTransportJobCreateReq
        Results.TransportJobCreateReqResult transportJobCreateReqResultRetCode = tmsService.transportJobCreateReq(objCommonIn,objCommonIn.getUser(),transportJobCreateReqParam);

        // Maintain Eqp-port's DispatchState
        if (CimBooleanUtils.isTrue(dispatchStateMaintainRequiredFlag)) {
            // Step 9: equipment_dispatchState_Change
            equipmentMethod.equipmentDispatchStateChange(objCommonIn, fromMachineID, params.getFromPortID(),
                    BizConstant.SP_PORTRSC_DISPATCHSTATE_DISPATCHED, null, null, null,
                    params.getCarrierID());
        }
        return singleCarrierTransferReqResult;
    }

    @Override
    public void sxMultipleCarrierTransferReq(Infos.ObjCommon objCommon, Boolean rerouteFlag,String transportType,List<Infos.CarrierXferReq> strCarrierXferReq) {
        /*-----------------------------------------------------------*/
        /* Set data for Xfer Request                                 */
        /*-----------------------------------------------------------*/
        //set data for xfer request.
        //if the destinatin is stocker, set null for 'from' field.
        Params.TransportJobCreateReqParams tranJobCreateReq = new Params.TransportJobCreateReqParams();
        int nLen = CimArrayUtils.getSize(strCarrierXferReq);
        /*-----------------------------------------------------------*/
        /* Input data                                                */
        /*-----------------------------------------------------------*/
        /*--------------------------------------------*/
        /*                                            */
        /*        Object Lock Process                 */
        /*                                            */
        /*--------------------------------------------*/
        for (int ii = 0; ii < nLen; ii++) {
            /*--------------------------------------------*/
            /*                                            */
            /* Lock Port object (To)                      */
            /*                                            */
            /*--------------------------------------------*/
            if (0 > CimArrayUtils.getSize(strCarrierXferReq.get(ii).getStrToMachine())){
                throw new ServiceException(retCodeConfig.getNotFoundMachine());
            }
            if (!ObjectIdentifier.isEmptyWithValue(strCarrierXferReq.get(ii).getStrToMachine().get(0).getToMachineID())
                    && !ObjectIdentifier.isEmptyWithValue(strCarrierXferReq.get(ii).getStrToMachine().get(0).getToPortID())){
                /*---------------------------------------------------------*/
                /*   Get All Ports being in the same Port Group as ToPort  */
                /*---------------------------------------------------------*/
                //【step1】 - portResource_allPortsInSameGroup_Get
                Infos.EqpPortInfo allPortsInSameGroupRetCode = portMethod.portResourceAllPortsInSameGroupGet(objCommon,
                        strCarrierXferReq.get(ii).getStrToMachine().get(0).getToMachineID(),
                        strCarrierXferReq.get(ii).getStrToMachine().get(0).getToPortID());

                int lenPort = CimArrayUtils.getSize(allPortsInSameGroupRetCode.getEqpPortStatuses());
                for (int jj = 0; jj < lenPort; jj++) {
                    objectLockMethod.objectLockForEquipmentResource(objCommon, strCarrierXferReq.get(ii).getStrToMachine().get(0).getToMachineID(), allPortsInSameGroupRetCode.getEqpPortStatuses().get(jj).getPortID(), BizConstant.SP_CLASSNAME_POSPORTRESOURCE);
                }
            }
            /*--------------------------------------------*/
            /*                                            */
            /* Lock Port object (From)                    */
            /*                                            */
            /*--------------------------------------------*/
            if (ObjectIdentifier.isEmptyWithValue(strCarrierXferReq.get(ii).getFromMachineID())){
                continue;
            }
            /*------------------------------------------------------*/
            /*  Check whether specified machine is eqp or stocker   */
            /*------------------------------------------------------*/
            Boolean isStorageFlag = false;
            Infos.EqpStatusInfo eqpStatusInfoRetCode = null;
            try {
                eqpStatusInfoRetCode = equipmentMethod.equipmentStatusInfoGet(objCommon, strCarrierXferReq.get(ii).getFromMachineID());
            } catch (ServiceException e) {
                if (Validations.isEquals(retCodeConfig.getNotFoundEqp(), e.getCode())){
                    isStorageFlag = true;
                } else {
                    log.info("equipment_statusInfo_Get__090() rc != RC_OK");
                    continue;
                }
            }
            log.info("From-equiment is Storage or not == {}",strCarrierXferReq.get(ii).getFromMachineID().getValue());
            /*--------------------------------------------*/
            /* Lock Port object of machine                */
            /*--------------------------------------------*/
            if (CimBooleanUtils.isFalse(isStorageFlag)){
                objectLockMethod.objectLockForEquipmentResource(objCommon, strCarrierXferReq.get(ii).getFromMachineID(), strCarrierXferReq.get(ii).getFromPortID(), BizConstant.SP_CLASSNAME_POSPORTRESOURCE);
            }
        }
        //---------------------------------------------
        // Check Port condition
        //---------------------------------------------
        log.info("Check Port condition");
        for (int nCastXfer = 0; nCastXfer < nLen; nCastXfer++) {
            int nToEqp = CimArrayUtils.getSize(strCarrierXferReq.get(nCastXfer).getStrToMachine());
            if (0 == nToEqp){
                log.info("nToEqp is 0");
                continue;
            }
            if (ObjectIdentifier.isEmptyWithValue(strCarrierXferReq.get(nCastXfer).getStrToMachine().get(0).getToPortID())){
                log.info("ToMachine is stocker. Omit check for port.");
                continue;
            }
            //【step5】 - equipment_portInfo_Get
            Infos.EqpPortInfo eqpPortInfo = equipmentMethod.equipmentPortInfoGet(objCommon, strCarrierXferReq.get(nCastXfer).getStrToMachine().get(0).getToMachineID());
            int nPortLen = CimArrayUtils.getSize(eqpPortInfo.getEqpPortStatuses());
            for (int nPort = 0; nPort < nPortLen; nPort++) {
                if (ObjectIdentifier.equalsWithValue(eqpPortInfo.getEqpPortStatuses().get(nPort).getPortID(),
                        strCarrierXferReq.get(nCastXfer).getStrToMachine().get(0).getToPortID())){
                    if (!CimStringUtils.equals(BizConstant.SP_EQP_DISPATCHMODE_AUTO, eqpPortInfo.getEqpPortStatuses().get(nPort).getAccessMode())){
                        log.error("RC_INVALID_PORT_ACCESSMODE");
                        throw new ServiceException(new OmCode(retCodeConfigEx.getInvalidPortAccessMode(),
                                eqpPortInfo.getEqpPortStatuses().get(nPort).getPortID().getValue(),
                                eqpPortInfo.getEqpPortStatuses().get(nPort).getAccessMode()));
                    }
                    if (!CimStringUtils.equals(BizConstant.SP_EQP_ONLINEMODE_OFFLINE, eqpPortInfo.getEqpPortStatuses().get(nPort).getOnlineMode())){
                        log.info("onlineMode is not Offline");
                        if (CimStringUtils.equals(BizConstant.SP_LOADPURPOSETYPE_PROCESSLOT, eqpPortInfo.getEqpPortStatuses().get(nPort).getLoadPurposeType())
                                || CimStringUtils.equals(BizConstant.SP_LOADPURPOSETYPE_EMPTYCASSETTE, eqpPortInfo.getEqpPortStatuses().get(nPort).getLoadPurposeType())
                                || CimStringUtils.equals(BizConstant.SP_LOADPURPOSETYPE_ANY, eqpPortInfo.getEqpPortStatuses().get(nPort).getLoadPurposeType())){
                            log.info("loadPurposeType is ProcessLot or EmptyCassette or Any. ");
                            if (!CimStringUtils.equals(BizConstant.SP_PORTRSC_DISPATCHSTATE_REQUIRED, eqpPortInfo.getEqpPortStatuses().get(nPort).getDispatchState())
                                    && !CimStringUtils.equals(BizConstant.SP_TRANSPORT_TYPE_TAKE_OUT_IN, transportType)){
                                if (ObjectIdentifier.equalsWithValue(eqpPortInfo.getEqpPortStatuses().get(nPort).getDispatchLoadCassetteID(),
                                        strCarrierXferReq.get(nCastXfer).getCarrierID())){
                                    //OK
                                    log.info("CarrierID of Input param and dispatchLoadCassetteID are same.");
                                }else {
                                    log.error("RC_INVALID_PORT_DISPSTAT");
                                    throw new ServiceException(new OmCode(retCodeConfigEx.getInvalidPortDispatchStatus(),
                                            eqpPortInfo.getEqpPortStatuses().get(nPort).getPortID().getValue(),
                                            eqpPortInfo.getEqpPortStatuses().get(nPort).getDispatchState()));
                                }
                            }
                        }
                        //------------------------------------------------------
                        //  Check Port Status for Fixed buffer equipment only
                        //------------------------------------------------------
                        //【step6】 - equipment_brInfoForInternalBuffer_GetDR__120
                        Infos.EqpBrInfoForInternalBuffer equipmentBrInfoForInternalBufferDrRetCode = equipmentMethod.equipmentBrInfoForInternalBufferGetDR(objCommon,
                                strCarrierXferReq.get(nCastXfer).getStrToMachine().get(0).getToMachineID());
                        if (!CimStringUtils.equals(BizConstant.SP_MC_CATEGORY_INTERNALBUFFER, equipmentBrInfoForInternalBufferDrRetCode.getEquipmentCategory())){
                            log.info("Equipment Category is fixed Buffer ");
                            if (!CimStringUtils.equals(BizConstant.SP_TRANSPORT_TYPE_TAKE_OUT_IN, transportType)
                                    && (CimStringUtils.equals(BizConstant.SP_PORTRSC_PORTSTATE_UNLOADAVAIL,eqpPortInfo.getEqpPortStatuses().get(nPort).getPortState())
                                    || CimStringUtils.equals(BizConstant.SP_PORTRSC_PORTSTATE_UNLOADREQ,eqpPortInfo.getEqpPortStatuses().get(nPort).getPortState()))){
                                log.error("RC_INVALID_PORT_STATE");
                                throw new ServiceException(new OmCode(retCodeConfig.getInvalidPortState(),
                                        ObjectIdentifier.fetchValue(eqpPortInfo.getEqpPortStatuses().get(nPort).getPortID()), eqpPortInfo.getEqpPortStatuses().get(nPort).getPortState()));
                            }
                            if (!CimStringUtils.equals(BizConstant.SP_PORTRSC_PORTSTATE_LOADREQ,eqpPortInfo.getEqpPortStatuses().get(nPort).getPortState())
                                    && !CimStringUtils.equals(BizConstant.SP_PORTRSC_PORTSTATE_LOADAVAIL,eqpPortInfo.getEqpPortStatuses().get(nPort).getPortState())
                                    && !CimStringUtils.equals(BizConstant.SP_PORTRSC_PORTSTATE_UNLOADAVAIL,eqpPortInfo.getEqpPortStatuses().get(nPort).getPortState())
                                    && !CimStringUtils.equals(BizConstant.SP_PORTRSC_PORTSTATE_UNLOADREQ,eqpPortInfo.getEqpPortStatuses().get(nPort).getPortState())){
                                //Indent adjust start
                                log.error("RC_INVALID_PORT_STATE");
                                throw new ServiceException(new OmCode(retCodeConfig.getInvalidPortState(),
                                        ObjectIdentifier.fetchValue(eqpPortInfo.getEqpPortStatuses().get(nPort).getPortID()), eqpPortInfo.getEqpPortStatuses().get(nPort).getPortState()));
                            }
                        }else {
                            // Equipment category is Internal Bufrer
                            log.info("Equipment category is Internal Bufrer");
                            if (!CimStringUtils.equals(BizConstant.SP_PORTRSC_PORTSTATE_LOADREQ,eqpPortInfo.getEqpPortStatuses().get(nPort).getPortState())
                                    && !CimStringUtils.equals(BizConstant.SP_PORTRSC_PORTSTATE_LOADCOMP,eqpPortInfo.getEqpPortStatuses().get(nPort).getPortState())
                                    && !CimStringUtils.equals(BizConstant.SP_PORTRSC_PORTSTATE_UNLOADAVAIL,eqpPortInfo.getEqpPortStatuses().get(nPort).getPortState())){
                                log.error("RC_INVALID_PORT_STATE");
                                throw new ServiceException(new OmCode(retCodeConfig.getInvalidPortState(),
                                        ObjectIdentifier.fetchValue(eqpPortInfo.getEqpPortStatuses().get(nPort).getPortID()), eqpPortInfo.getEqpPortStatuses().get(nPort).getPortState()));
                            }
                        }
                        log.info("Port is good condition");
                    }
                }
            }
        }
        List<Infos.JobCreateArray> jobCreateDataList  = new ArrayList<>();
        for (int i = 0; i < nLen; i++) {
            Machine aBaseMachine = null;
            Boolean isStorageBool = false;
            ObjectIdentifier dummy = new ObjectIdentifier();
            tranJobCreateReq.setJobCreateData(jobCreateDataList);
            if (ObjectIdentifier.isEmptyWithValue(strCarrierXferReq.get(i).getFromMachineID())){
                log.info("PPT_CONVERT_EQPID_TO_BASEMACHINE_OR() isStorageBool==TRUE");
                Infos.JobCreateArray jobCreateData = new Infos.JobCreateArray();
                jobCreateDataList.add(jobCreateData);
                jobCreateData.setFromMachineID(dummy);
                jobCreateData.setFromPortID(dummy);
            }else {
                log.info("PPT_CONVERT_EQPID_TO_BASEMACHINE_OR() isStorageBool==FALSE");
                Infos.JobCreateArray jobCreateData = new Infos.JobCreateArray();
                jobCreateDataList.add(jobCreateData);
                jobCreateData.setFromMachineID(strCarrierXferReq.get(i).getFromMachineID());
                jobCreateData.setFromPortID(strCarrierXferReq.get(i).getFromPortID());
            }
            int nnLen = CimArrayUtils.getSize(strCarrierXferReq.get(i).getStrToMachine());
            List<Infos.ToDestination> toMachine = new ArrayList<>();
            jobCreateDataList.get(i).setToMachine(toMachine);
            for (int j = 0; j < nnLen; j++) {
                Infos.ToDestination toDestination = new Infos.ToDestination();
                toMachine.add(toDestination);
                toDestination.setToMachineID(strCarrierXferReq.get(i).getStrToMachine().get(j).getToMachineID());
                toDestination.setToPortID(strCarrierXferReq.get(i).getStrToMachine().get(j).getToPortID());
            }
            tranJobCreateReq.setTransportType(transportType);
            tranJobCreateReq.setRerouteFlag(rerouteFlag);
            tranJobCreateReq.getJobCreateData().get(i).setCarrierID(strCarrierXferReq.get(i).getCarrierID());
            tranJobCreateReq.getJobCreateData().get(i).setZoneType(strCarrierXferReq.get(i).getZoneType());
            tranJobCreateReq.getJobCreateData().get(i).setN2PurgeFlag(strCarrierXferReq.get(i).getN2PurgeFlag());
            tranJobCreateReq.getJobCreateData().get(i).setExpectedStartTime(strCarrierXferReq.get(i).getExpectedStartTime());
            tranJobCreateReq.getJobCreateData().get(i).setExpectedEndTime(strCarrierXferReq.get(i).getExpectedEndTime());
            tranJobCreateReq.getJobCreateData().get(i).setMandatoryFlag(strCarrierXferReq.get(i).getMandatoryFlag());
            tranJobCreateReq.getJobCreateData().get(i).setPriority(strCarrierXferReq.get(i).getPriority());
        }
        /*-----------------------------------------------------------*/
        /* Transfer Request to TMSMgr                                */
        /*-----------------------------------------------------------*/
        //【step7】 - TMSMgr_SendTransportJobCreateReq
        Results.TransportJobCreateReqResult transportJobCreateReqResultRetCode = tmsService.transportJobCreateReq(objCommon,objCommon.getUser(),tranJobCreateReq);

        /*-----------------------------------------------------------*/
        /* Maintain Eqp-Port's DispatchState ( -> Dispatched )       */
        /*-----------------------------------------------------------*/
        for (int j = 0; j < nLen; j++) {
            //check length of To-machine
            if (CimArrayUtils.getSize(strCarrierXferReq.get(j).getStrToMachine()) <= 0){
                throw new ServiceException(retCodeConfig.getNotFoundMachine());
            }
            if (!ObjectIdentifier.isEmptyWithValue(strCarrierXferReq.get(j).getStrToMachine().get(0).getToMachineID())
                    && !ObjectIdentifier.isEmptyWithValue(strCarrierXferReq.get(j).getStrToMachine().get(0).getToPortID())){
                /*----------------------------------------------------------------*/
                /* Change Specified Target Port's DispatchState to "Dispatched"   */
                /*----------------------------------------------------------------*/
                log.info("Change Specified Target Port's DispatchState to 'Dispatched'...");
                //【step8】 - equipment_dispatchState_Change
                ObjectIdentifier dmyObjID = new ObjectIdentifier();
                equipmentMethod.equipmentDispatchStateChange(objCommon,
                        strCarrierXferReq.get(j).getStrToMachine().get(0).getToMachineID(),
                        strCarrierXferReq.get(j).getStrToMachine().get(0).getToPortID(),
                        BizConstant.SP_PORTRSC_DISPATCHSTATE_DISPATCHED,
                        dmyObjID,
                        strCarrierXferReq.get(j).getCarrierID(),
                        dmyObjID,
                        dmyObjID);
            }
        }
        /*-----------------------------------------------------------*/
        /* Maintain Eqp-Port's DispatchState ( -> NotDispatched )    */
        /*-----------------------------------------------------------*/
        log.info("Maintain Eqp-Port's DispatchState ( -> NotDispatched )...");
        for (int j = 0; j < nLen; j++) {
            // Check length of In-Parameter
            if (CimArrayUtils.getSize(strCarrierXferReq.get(j).getStrToMachine()) <= 0){
                throw new ServiceException(retCodeConfig.getNotFoundMachine());
            }
            if (!ObjectIdentifier.isEmptyWithValue(strCarrierXferReq.get(j).getStrToMachine().get(0).getToMachineID())
                    && !ObjectIdentifier.isEmptyWithValue(strCarrierXferReq.get(j).getStrToMachine().get(0).getToPortID())){
                /*-----------------------------------------------*/
                /* Get All Ports registered as Same Port Group   */
                /*-----------------------------------------------*/
                //【step9】 - equipment_portInfo_Get
                Infos.EqpPortInfo eqpPortInfo = equipmentMethod.equipmentPortInfoGet(objCommon, strCarrierXferReq.get(j).getStrToMachine().get(0).getToMachineID());
                /*-----------------------------------*/
                /* Get Specified Port's Port Group   */
                /*-----------------------------------*/
                log.info("Get Specified Port's Port Group...");
                int basePGNo = 0;
                int pLen = CimArrayUtils.getSize(eqpPortInfo.getEqpPortStatuses());
                for (int k = 0; k < pLen; k++) {
                    if (ObjectIdentifier.equalsWithValue(strCarrierXferReq.get(j).getStrToMachine().get(0).getToPortID(),
                            eqpPortInfo.getEqpPortStatuses().get(k).getPortID())){
                        basePGNo = k;
                        break;
                    }
                }
                /*------------------------------*/
                /* Find Port ID to be Updated   */
                /*------------------------------*/
                for (int k = 0; k < pLen; k++) {
                    /*===== Omit Base Port (In-Parm's Port) =====*/
                    if (k == basePGNo){
                        log.info("k == basePGNo,continue...");
                    }
                    /*===== Omit Different Group's Port =====*/
                    if (!CimStringUtils.equals(eqpPortInfo.getEqpPortStatuses().get(k).getPortGroup(),
                            eqpPortInfo.getEqpPortStatuses().get(basePGNo).getPortGroup())){
                        log.info("Not same portGroup, continue...");
                        continue;
                    }
                    /*===== Check Port's Status =====*/
                    Infos.EqpPortStatus eqpPortStatus = eqpPortInfo.getEqpPortStatuses().get(k);
                    if (CimStringUtils.equals(BizConstant.SP_PORTRSC_DISPATCHSTATE_REQUIRED,eqpPortStatus.getDispatchState())
                            && (CimStringUtils.equals(BizConstant.SP_PORTRSC_PORTSTATE_LOADAVAIL,eqpPortStatus.getPortState())
                            || CimStringUtils.equals(BizConstant.SP_PORTRSC_PORTSTATE_LOADREQ,eqpPortStatus.getPortState()))){
                        /*-----------------------------------------------------------------------------*/
                        /* Change Same Group's Not-Specified Port's DispatchState to "NotDispatched"   */
                        /*-----------------------------------------------------------------------------*/
                        log.info("Change Same Group's Not-Specified Port's DispatchState to 'NotDispatched'...");
                        ObjectIdentifier dmyOjbID = new ObjectIdentifier();
                        //【step10】 - equipment_dispatchState_Change
                        equipmentMethod.equipmentDispatchStateChange(objCommon,
                                strCarrierXferReq.get(j).getStrToMachine().get(0).getToMachineID(),
                                eqpPortStatus.getPortID(),
                                BizConstant.SP_PORTRSC_DISPATCHSTATE_NOTDISPATCHED ,
                                dmyOjbID,
                                dmyOjbID,
                                dmyOjbID,
                                dmyOjbID);
                    }
                }
            }
        }
        /*------------------------------*/
        /* change fromEQP dispatchState */
        /*------------------------------*/
        log.info("change fromEQP dispatchState");
        for (int j = 0; j < nLen; j++) {
            if (ObjectIdentifier.isEmptyWithValue(strCarrierXferReq.get(j).getFromMachineID())){
                log.info("0 == CIMFWStrLen(strCarrierXferReq[j].fromMachineID.identifier)");
                continue;
            }
            //【step11】- equipment_statusInfo_Get__090
            Boolean isStorageMachine = false;
            Infos.EqpStatusInfo eqpStatusInfoResultRetCode = null;
            try {
                eqpStatusInfoResultRetCode = equipmentMethod.equipmentStatusInfoGet(objCommon, strCarrierXferReq.get(j).getFromMachineID());
            } catch (ServiceException e) {
                if (Validations.isEquals(retCodeConfig.getNotFoundEqp(), e.getCode())){
                    isStorageMachine = true;
                } else {
                    log.info("equipment_statusInfo_Get__090() rc != RC_OK");
                    continue;
                }
            }

            if (CimBooleanUtils.isFalse(isStorageMachine)){
                log.info("FALSE == isStorageMachine");
                log.info("call equipment_dispatchState_Change()");
                //【step12】 - equipment_dispatchState_Change
                ObjectIdentifier dmyOjbID = new ObjectIdentifier();
                //【step10】 - equipment_dispatchState_Change
                equipmentMethod.equipmentDispatchStateChange(objCommon,
                        strCarrierXferReq.get(j).getFromMachineID(),
                        strCarrierXferReq.get(j).getFromPortID(),
                        BizConstant.SP_PORTRSC_DISPATCHSTATE_DISPATCHED ,
                        dmyOjbID,
                        dmyOjbID,
                        dmyOjbID,
                        strCarrierXferReq.get(j).getCarrierID());
            }else {
                log.info("TRUE == isStorageMachine");
            }
        }
    }

    @Override
    public Results.StockerInventoryUploadReqResult sxStockerInventoryUploadReq(Infos.ObjCommon objCommon, ObjectIdentifier stockerID, Infos.ShelfPosition shelfPosition, String claimMemo) {
        Results.StockerInventoryUploadReqResult result = new Results.StockerInventoryUploadReqResult();
        StringBuilder reasonText = new StringBuilder();

        if(!ObjectIdentifier.isEmpty(stockerID)) {
            // Step1 - Lock objects to be updated  - object_Lock
            objectLockMethod.objectLock(objCommon, CimStorageMachine.class, stockerID);

            //Step2 - stocker_type_Get
            Outputs.ObjStockerTypeGetDROut stockerTypeResult = stockerMethod.stockerTypeGet(objCommon, stockerID);

            String stockerType = stockerTypeResult.getStockerType();
            if (!CimStringUtils.equals(BizConstant.SP_STOCKER_TYPE_AUTO,stockerType) && !CimStringUtils.equals(BizConstant.SP_STOCKER_TYPE_INTERM,stockerType)) {
                Validations.check(retCodeConfig.getNotAutoStocker(), ObjectIdentifier.fetchValue(stockerID));
            }

            //Step3 - Check stocker Type - stocker_inventoryState_Get
            Boolean inventoryStateResult = stockerMethod.stockerInventoryStateGet(objCommon, stockerID);
            if (CimBooleanUtils.isTrue(inventoryStateResult)) {
                Validations.check(retCodeConfig.getStockerInventoryInProcess(), ObjectIdentifier.fetchValue(stockerID));
            }

            // Step4 - Send Inventory Request to TMS - TMSMgr_SendUploadInventoryReq

            Inputs.SendUploadInventoryReqIn sendUploadInventoryReqIn = new Inputs.SendUploadInventoryReqIn();
            sendUploadInventoryReqIn.setObjCommon(objCommon);
            sendUploadInventoryReqIn.setUser(objCommon.getUser());
            Infos.UploadInventoryReq uploadInventoryReq = new Infos.UploadInventoryReq();
            uploadInventoryReq.setMachineID(stockerID);
            uploadInventoryReq.setUploadLevel("");
            sendUploadInventoryReqIn.setUploadInventoryReq(uploadInventoryReq);
            // xmsMethod.sendUploadInventoryReq(sendUploadInventoryReqIn);
            Results.AmhsUploadInventoryReqResult xmsCarrers=tmsService.uploadInventoryReq(sendUploadInventoryReqIn);

            /*---------------------------------*/
            /*  Send Inventory Request to TMS  */
            /*---------------------------------*/

            /*---------------------------------*/
            /*   Update cassette's position    */
            /*   by returned data of           */
            /*   TMSMgr_SendUploadInventoryReq */
            /*---------------------------------*/

            List<Infos.InventoryLotInfo> inventoryLotInfos = new ArrayList<>();

            Optional.ofNullable(xmsCarrers.getUploadInventoryReqResults()).orElse(new ArrayList<>()).forEach(uploadInventoryReqResult -> {
                Infos.InventoryLotInfo inventoryLotInfo=new Infos.InventoryLotInfo();
                inventoryLotInfo.setCassetteID(uploadInventoryReqResult.getCarrierID());
                inventoryLotInfos.add(inventoryLotInfo);
            });

            /*--------------------------------------------*/
            /* 1. Omit FOSB from input parameter.         */
            /* 2. Lock eqp object of 'EI' cassette. */
            /*--------------------------------------------*/
            //Step5 - cassette_LocationInfo_GetDR
            boolean notFoundFoupFlag = false;
            List<Infos.InventoryLotInfo> carrierInfos = new ArrayList<>();
            List<Infos.InventoryLotInfo> fosbInfos = new ArrayList<>();

            List<ObjectIdentifier> equipments = new ArrayList<>();
            List<ObjectIdentifier> carrierLoadEqpIDs = new ArrayList<>();

            for (int i = 0; i< CimArrayUtils.getSize(inventoryLotInfos); i++) {
                Infos.InventoryLotInfo inventoryLotInfo = inventoryLotInfos.get(i);

                //Step3 - Check carrier existence and get location information - cassette_LocationInfo_GetDR
                Infos.LotLocationInfo cassetteLocationInfo = null;
                try {
                    cassetteLocationInfo =  cassetteMethod.cassetteLocationInfoGetDR(inventoryLotInfo.getCassetteID());
                } catch (ServiceException e){
                    if (Validations.isEquals(retCodeConfig.getNotFoundCassette(), e.getCode())){
                        fosbInfos.add(inventoryLotInfo);
                        notFoundFoupFlag = true;
                        continue;
                    } else {
                        throw e;
                    }
                }
                carrierInfos.add(inventoryLotInfo);
                carrierLoadEqpIDs.add(new ObjectIdentifier(""));

                /*---------------------------------------------------------------*/
                /*  If carrier location is 'EI', get equipmentID for object lock */
                /*---------------------------------------------------------------*/
                if (CimStringUtils.equals(BizConstant.SP_TRANSSTATE_EQUIPMENTIN, cassetteLocationInfo.getTransferStatus())) {
                    /*---------------------------------------------------------------*/
                    /*  Check control job existence. If exist, no need to list up    */
                    /*---------------------------------------------------------------*/
                    //Step6 cassette_controlJobID_Get
                    ObjectIdentifier cassetteControlJobID = cassetteMethod.cassetteControlJobIDGet(objCommon, inventoryLotInfo.getCassetteID());
                    if (!ObjectIdentifier.isEmpty(cassetteControlJobID)) {
                        continue;
                    }

                    carrierLoadEqpIDs.set((CimArrayUtils.getSize(carrierLoadEqpIDs) - 1),cassetteLocationInfo.getEquipmentID());

                    /*-----------------------------------------------------*/
                    /*  Check whether eqp already collected or not   */
                    /*-----------------------------------------------------*/
                    boolean alreadyCollectedFlag = false;

                    for (int j=0; j<equipments.size(); j++) {
                        if (cassetteLocationInfo.getEquipmentID().equals(equipments.get(j))) {
                            alreadyCollectedFlag = true;
                            break;
                        }
                    }

                    if (!alreadyCollectedFlag) {
                        equipments.add(cassetteLocationInfo.getEquipmentID());
                    }
                }
            }

            /*---------------------------------------------------------------*/
            /*  Set reason text if FOSB is included in reported carrieres    */
            /*---------------------------------------------------------------*/
            if (!CimArrayUtils.isEmpty(fosbInfos)) {
                reasonText.append("[Information : FOSB reported : Following reported carriers are FOSB. Its location was not changed.] =>").append("\n");
                fosbInfos.forEach(inventoryLotInfo -> {
                    reasonText.append(ObjectIdentifier.fetchValue(inventoryLotInfo.getCassetteID()));
                    if (!inventoryLotInfo.equals(fosbInfos.get(fosbInfos.size() - 1))){
                        reasonText.append(",");
                    }
                });
            }
            /*------------------------------------------------------*/
            /*   Lock equipment/FOUP object for check (and update)  */
            /*------------------------------------------------------*/
            int eqpCnt = CimArrayUtils.getSize(equipments);
            int fpCnt = CimArrayUtils.getSize(carrierLoadEqpIDs);
            for (int lockCnt = 0; lockCnt < eqpCnt; lockCnt++){
                // Step7 object_lockMode_Get
                Inputs.ObjLockModeIn objLockModeIn = new Inputs.ObjLockModeIn();
                objLockModeIn.setObjectID(equipments.get(lockCnt));
                objLockModeIn.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
                objLockModeIn.setFunctionCategory(TransactionIDEnum.STOCKER_INVENTORY_REQ.getValue());
                objLockModeIn.setUserDataUpdateFlag(false);
                Outputs.ObjLockModeOut objLockModeOut = objectMethod.objectLockModeGet(objCommon, objLockModeIn);
                Long lockMode = objLockModeOut.getLockMode();
                if (!lockMode.equals(BizConstant.SP_EQP_LOCK_MODE_WRITE)){
                    // Advanced Mode
                    // Lock Equipment Main Object
                    // Step8 advanced_object_Lock
                    objectLockMethod.advancedObjectLock(objCommon, new Inputs.ObjAdvanceLockIn(equipments.get(lockCnt),
                            BizConstant.SP_CLASSNAME_POSMACHINE,
                            BizConstant.SP_OBJECTLOCK_OBJECTTYPE_MAINOBJECT,
                            objLockModeOut.getRequiredLockForMainObject(), new ArrayList<>()));
                    List<String> loadCastSeq = new ArrayList<>();
                    for (int castCnt = 0; castCnt < fpCnt; castCnt++){
                        if (ObjectIdentifier.equalsWithValue(equipments.get(lockCnt), carrierLoadEqpIDs.get(castCnt))){
                            loadCastSeq.add(carrierInfos.get(castCnt).getCassetteID().getValue());
                        }
                    }
                    // Step9 advanced_object_Lock
                    if (!CimArrayUtils.isEmpty(loadCastSeq)){
                        objectLockMethod.advancedObjectLock(objCommon, new Inputs.ObjAdvanceLockIn(equipments.get(lockCnt),
                                BizConstant.SP_CLASSNAME_POSMACHINE,
                                BizConstant.SP_OBJECTLOCK_OBJECTTYPE_LOADCASSETTE,
                                (long)BizConstant.SP_OBJECTLOCK_LOCKTYPE_WRITE, loadCastSeq));
                    }
                } else {
                    // Step10 object_Lock
                    objectLockMethod.objectLock(objCommon, CimMachine.class, equipments.get(lockCnt));
                }
            }
            for (int lockCnt = 0; lockCnt < fpCnt; lockCnt++){
                // Step11 object_Lock
                objectLockMethod.objectLock(objCommon, CimCassette.class, carrierInfos.get(lockCnt).getCassetteID());
            }

            /*--------------------------------*/
            /*   Update cassette's position   */
            /*--------------------------------*/
            //Step12 cassette_position_UpdateByStockerInventory
            List<Infos.InventoriedLotInfo> cassettePositionUpdateRetCode = null;
            try{
                cassetteMethod.cassettePositionUpdateByStockerInventory(objCommon, stockerID, shelfPosition, carrierInfos);
            }catch (ServiceException e) {
                if (!Validations.isEquals(retCodeConfig.getSomeCassetteInventoryDataError(), e.getCode())) {
                    throw e;
                }
                //get final reason test
                if (CimStringUtils.isNotEmpty(e.getReasonText())){
                    reasonText.append("\n").append(e.getReasonText());
                }
            }

            /*-----------------------*/
            /*   Set reason text     */
            /*-----------------------*/

            //Step13 - stocker_inventoryState_Change
            stockerMethod.stockerInventoryStateChange(objCommon, stockerID, false);
        } else {
            Validations.check(retCodeConfig.getInvalidInputParam());
        }
        result.setStockerID(stockerID);
        result.setReasonText(reasonText.toString());
        return result;
    }

    @Override
    public Results.StockerInventoryRptResult sxStockerInventoryRpt(Infos.ObjCommon objCommon, ObjectIdentifier stockerID, Infos.ShelfPosition shelfPosition, List<Infos.InventoryLotInfo> inventoryLotInfos, String claimMemo) {
        Results.StockerInventoryRptResult stockerInventoryRptResult = new Results.StockerInventoryRptResult();

        if(!ObjectIdentifier.isEmpty(stockerID)) {
            stockerInventoryRptResult.setStockerID(stockerID);

            // Step1 - Lock objects to be updated - object_Lock
            objectLockMethod.objectLock(objCommon, CimStorageMachine.class, stockerID);

            //Step2 - Check stocker Type - stocker_type_Get
            Outputs.ObjStockerTypeGetDROut stockerTypeResult = stockerMethod.stockerTypeGet(objCommon, stockerID);

            String stockerType = stockerTypeResult.getStockerType();
            if (!CimStringUtils.equals(BizConstant.SP_STOCKER_TYPE_AUTO, stockerType)
                    && !CimStringUtils.equals(BizConstant.SP_STOCKER_TYPE_INTERM, stockerType)) {
                throw new ServiceException(retCodeConfig.getNotAutoStocker());
            }

            /*--------------------------------------------*/
            /* 1. Omit FOSB from input parameter.         */
            /* 2. Lock eqp object of 'EI' cassette. */
            /*--------------------------------------------*/
            boolean notFoundFoupFlag = false;
            List<Infos.InventoryLotInfo> carrierInfos = new ArrayList<>();
            List<Infos.InventoryLotInfo> fosbInfos = new ArrayList<>();

            List<ObjectIdentifier> equipments = new ArrayList<>();
            List<ObjectIdentifier> carrierLoadEqpIDs = new ArrayList<>();

            int nLen = CimArrayUtils.getSize(inventoryLotInfos);
            for (int i=0; i < nLen; i++) {
                Infos.InventoryLotInfo inventoryLotInfo = inventoryLotInfos.get(i);

                //Step3 - Check carrier existence and get location information - cassette_LocationInfo_GetDR
                Infos.LotLocationInfo cassetteLocationInfo = null;
                try {
                    cassetteLocationInfo = cassetteMethod.cassetteLocationInfoGetDR(inventoryLotInfo.getCassetteID());
                } catch (ServiceException e){
                    if (Validations.isEquals(retCodeConfig.getNotFoundCassette(), e.getCode())){
                        fosbInfos.add(inventoryLotInfo);
                        notFoundFoupFlag = true;
                        continue;
                    } else {
                        throw e;
                    }
                }
                carrierInfos.add(inventoryLotInfo);
                carrierLoadEqpIDs.add(new ObjectIdentifier(""));

                /*---------------------------------------------------------------*/
                /*  If carrier location is 'EI', get equipmentID for object lock */
                /*---------------------------------------------------------------*/
                if (CimStringUtils.equals(BizConstant.SP_TRANSSTATE_EQUIPMENTIN, cassetteLocationInfo.getTransferStatus())) {
                    /*---------------------------------------------------------------*/
                    /*  Check control job existence. If exist, no need to list up    */
                    /*---------------------------------------------------------------*/
                    //Step4 cassette_controlJobID_Get
                    ObjectIdentifier cassetteControlJobID = cassetteMethod.cassetteControlJobIDGet(objCommon, inventoryLotInfo.getCassetteID());
                    if (!ObjectIdentifier.isEmpty(cassetteControlJobID)) {
                        continue;
                    }

                    carrierLoadEqpIDs.add(cassetteLocationInfo.getEquipmentID());

                    /*-----------------------------------------------------*/
                    /*  Check whether eqp already collected or not   */
                    /*-----------------------------------------------------*/
                    boolean alreadyCollectedFlag = false;
                    int collectedEqpLen = CimArrayUtils.getSize(equipments);
                    for (int j=0; j < collectedEqpLen; j++) {
                        if (ObjectIdentifier.equals(cassetteLocationInfo.getEquipmentID(), equipments.get(j))) {
                            alreadyCollectedFlag = true;
                            break;
                        }
                    }

                    if (CimBooleanUtils.isFalse(alreadyCollectedFlag)) {
                        equipments.add(cassetteLocationInfo.getEquipmentID());
                    }
                }
            }

            /*---------------------------------------------------------------*/
            /*  Set reason text if FOSB is included in reported carrieres    */
            /*---------------------------------------------------------------*/
            String reasonText = "";
            if (!CimArrayUtils.isEmpty(fosbInfos)) {
                reasonText = fosbInfos.toString();
            }
            /*------------------------------------------------------*/
            /*   Lock equipment/FOUP object for check (and update)  */
            /*------------------------------------------------------*/
            int eqpCnt = CimArrayUtils.getSize(equipments);
            int fpCnt = CimArrayUtils.getSize(carrierLoadEqpIDs);
            for (int lockCnt = 0; lockCnt < eqpCnt; lockCnt++){
                // Step5 object_lockMode_Get
                Inputs.ObjLockModeIn objLockModeIn = new Inputs.ObjLockModeIn();
                objLockModeIn.setObjectID(equipments.get(lockCnt));
                objLockModeIn.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
                objLockModeIn.setFunctionCategory(TransactionIDEnum.STOCKER_INVENTORY_RPT.getValue());
                objLockModeIn.setUserDataUpdateFlag(false);
                Outputs.ObjLockModeOut objLockModeOut = objectMethod.objectLockModeGet(objCommon, objLockModeIn);
                Long lockMode = objLockModeOut.getLockMode();
                if (!lockMode.equals(BizConstant.SP_EQP_LOCK_MODE_WRITE)){
                    // Advanced Mode
                    // Lock Equipment Main Object
                    // Step6 advanced_object_Lock
                    objectLockMethod.advancedObjectLock(objCommon, new Inputs.ObjAdvanceLockIn(equipments.get(lockCnt),
                            BizConstant.SP_CLASSNAME_POSMACHINE,
                            BizConstant.SP_OBJECTLOCK_OBJECTTYPE_MAINOBJECT,
                            objLockModeOut.getRequiredLockForMainObject(), new ArrayList<>()));
                    List<String> loadCastSeq = new ArrayList<>();
                    for (int castCnt = 0; castCnt < fpCnt; castCnt++){
                        if (ObjectIdentifier.equalsWithValue(equipments.get(lockCnt), carrierLoadEqpIDs.get(castCnt))){
                            loadCastSeq.add(carrierInfos.get(castCnt).getCassetteID().getValue());
                        }
                    }
                    // Step7 advanced_object_Lock
                    if (!CimArrayUtils.isEmpty(loadCastSeq)){
                        objectLockMethod.advancedObjectLock(objCommon, new Inputs.ObjAdvanceLockIn(equipments.get(lockCnt),
                                BizConstant.SP_CLASSNAME_POSMACHINE,
                                BizConstant.SP_OBJECTLOCK_OBJECTTYPE_LOADCASSETTE,
                                (long)BizConstant.SP_OBJECTLOCK_LOCKTYPE_WRITE, loadCastSeq));
                    }
                } else {
                    // Step8 object_Lock
                    objectLockMethod.objectLock(objCommon, CimMachine.class, equipments.get(lockCnt));
                }
            }
            for (int lockCnt = 0; lockCnt < fpCnt; lockCnt++){
                // Step9 object_Lock
                objectLockMethod.objectLock(objCommon, CimCassette.class, carrierInfos.get(lockCnt).getCassetteID());
            }

            /*--------------------------------*/
            /*   Update cassette's position   */
            /*--------------------------------*/
            //Step10 cassette_position_UpdateByStockerInventory
            List<Infos.InventoriedLotInfo> cassettePositionUpdateRetCode = null;
            try{
                cassettePositionUpdateRetCode = cassetteMethod.cassettePositionUpdateByStockerInventory(objCommon, stockerID, shelfPosition, carrierInfos);
            }catch (ServiceException e) {
                if (Validations.isEquals(retCodeConfig.getSomeCassetteInventoryDataError(), e.getCode())) {

                } else {
                    throw e;
                }
            }
            /*-----------------------*/
            /*   Set reason text     */
            /*-----------------------*/

            //Step11 stocker_inventoryState_Change
            stockerMethod.stockerInventoryStateChange(objCommon, stockerID, false);
        } else {
            throw new ServiceException(retCodeConfig.getInvalidInputParam());
        }

        return stockerInventoryRptResult;
    }

    @Override
    public ObjectIdentifier sxStockerStatusChangeRpt(Infos.ObjCommon objCommon, ObjectIdentifier stockerID, ObjectIdentifier stockerStatusCode, String claimMemo) {

        // Step1 - object_Lock
        objectLockMethod.objectLock(objCommon, CimStorageMachine.class, stockerID);
        //Step2 - rawEquipmentState_Translate
        Outputs.ObjRawEquipmentStateTranslateOut stateTranslateOutRetCode = equipmentMethod.rawEquipmentStateTranslate(
                objCommon, stockerID, stockerStatusCode);

        //Step3 - Convert eqp Status - equipmentState_Convert
        Outputs.ObjEquipmentStateConvertOut stateConvertOutRetCode = equipmentMethod.equipmentStateConvertV2(
                stockerID, stateTranslateOutRetCode.getEquipmentStatusCode());

        //Step4 - Check input-eqp-status vs. allowed-new-staus by Current mode - equipment_currentState_CheckTransition
        boolean updateCurrentStateFlag = false;
        try {
            equipmentMethod.equipmentCurrentStateCheckTransitionV2(objCommon, stockerID,
                    stateConvertOutRetCode.getConvertedStatusCode(), true);
            updateCurrentStateFlag = true;
        } catch (ServiceException e) {
            if (Validations.isEquals(retCodeConfig.getCurrentStateSame(), e.getCode())
                    || Validations.isEquals(retCodeConfig.getInvalidStateTransition(), e.getCode())){
            } else {
                throw e;
            }
        }

        //Step5 - equipment_currentState_ChangeByAuto
        Outputs.ObjEquipmentCurrentStateChangeByAutoOut  stateChangeOutRetCode = equipmentMethod
                .equipmentCurrentStateChangeByAuto(objCommon, stockerID,
                        stateTranslateOutRetCode.getEquipmentStatusCode(),
                        stateConvertOutRetCode.getConvertedStatusCode(), updateCurrentStateFlag);

        /*------------------------------------------*/
        /*   Create Equipment Status Change Event   */
        /*------------------------------------------*/
        // Step6 - equipmentStatusChangeEvent_Make

        if (updateCurrentStateFlag){
            Inputs.EquipmentStatusChangeEventMakeParams equipmentStatusChangeEventMakeParams1 =
                    new Inputs.EquipmentStatusChangeEventMakeParams();
            equipmentStatusChangeEventMakeParams1.setTransactionID(
                    TransactionIDEnum.STOCKER_STATUS_CHANGE_RPT.getValue());
            equipmentStatusChangeEventMakeParams1.setEquipmentID(new ObjectIdentifier());
            equipmentStatusChangeEventMakeParams1.setStockerID(stockerID);
            equipmentStatusChangeEventMakeParams1.setNewEquipmentStatus(
                    stateConvertOutRetCode.getConvertedStatusCode());
            equipmentStatusChangeEventMakeParams1.setNewE10Status(stateChangeOutRetCode.getE10Status());
            equipmentStatusChangeEventMakeParams1.setNewActualStatus(stateChangeOutRetCode.getActualStatus());
            equipmentStatusChangeEventMakeParams1.setNewActualE10Status(stateChangeOutRetCode.getActualE10Status());
            equipmentStatusChangeEventMakeParams1.setNewOperationMode("");
            equipmentStatusChangeEventMakeParams1.setPreviousStatus(stateChangeOutRetCode.getPreviousStatus());
            equipmentStatusChangeEventMakeParams1.setPreviousE10Status(stateChangeOutRetCode.getPreviousE10Status());
            equipmentStatusChangeEventMakeParams1.setPreviousActualStatus(
                    stateChangeOutRetCode.getPreviousActualStatus());
            equipmentStatusChangeEventMakeParams1.setPreviousActualE10Status(
                    stateChangeOutRetCode.getPreviousActualE10Status());
            equipmentStatusChangeEventMakeParams1.setPreviousOpeMode("");
            equipmentStatusChangeEventMakeParams1.setPrevStateStartTime(stateChangeOutRetCode.getPrevStateStartTime());
            equipmentStatusChangeEventMakeParams1.setClaimMemo(claimMemo);
            eventMethod.equipmentStatusChangeEventMake(objCommon, equipmentStatusChangeEventMakeParams1);
        } else {
            // Step7 - equipmentStatusChangeEvent_Make
            Inputs.EquipmentStatusChangeEventMakeParams equipmentStatusChangeEventMakeParams2 =
                    new Inputs.EquipmentStatusChangeEventMakeParams();
            equipmentStatusChangeEventMakeParams2.setTransactionID(
                    TransactionIDEnum.STOCKER_STATUS_CHANGE_RPT.getValue());
            equipmentStatusChangeEventMakeParams2.setEquipmentID(new ObjectIdentifier());
            equipmentStatusChangeEventMakeParams2.setStockerID(stockerID);
            equipmentStatusChangeEventMakeParams2.setNewEquipmentStatus(stateChangeOutRetCode.getPreviousStatus());
            equipmentStatusChangeEventMakeParams2.setNewE10Status(stateChangeOutRetCode.getPreviousE10Status());
            equipmentStatusChangeEventMakeParams2.setNewActualStatus(stateChangeOutRetCode.getActualStatus());
            equipmentStatusChangeEventMakeParams2.setNewActualE10Status(stateChangeOutRetCode.getActualE10Status());
            equipmentStatusChangeEventMakeParams2.setNewOperationMode("");
            equipmentStatusChangeEventMakeParams2.setPreviousStatus(stateChangeOutRetCode.getPreviousStatus());
            equipmentStatusChangeEventMakeParams2.setPreviousE10Status(stateChangeOutRetCode.getPreviousE10Status());
            equipmentStatusChangeEventMakeParams2.setPreviousActualStatus(
                    stateChangeOutRetCode.getPreviousActualStatus());
            equipmentStatusChangeEventMakeParams2.setPreviousActualE10Status(
                    stateChangeOutRetCode.getPreviousActualE10Status());
            equipmentStatusChangeEventMakeParams2.setPreviousOpeMode("");
            equipmentStatusChangeEventMakeParams2.setPrevStateStartTime(stateChangeOutRetCode.getPrevStateStartTime());
            equipmentStatusChangeEventMakeParams2.setClaimMemo(claimMemo);
            eventMethod.equipmentStatusChangeEventMake(objCommon, equipmentStatusChangeEventMakeParams2);
        }

        return stockerID;
    }

    @Override
    public void sxNPWCarrierReserveForIBReq(Infos.ObjCommon objCommon, Params.NPWCarrierReserveForIBReqParams params) {
        ObjectIdentifier equipmentID = params.getEquipmentID();
        ObjectIdentifier controlJobID = params.getControlJobID();
        String portGroupID = params.getPortGroupID();
        List<Infos.StartCassette> strStartCassette = params.getStartCassetteList();
        String claimMemo = params.getOpeMemo();

        /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/
        /*   Check TransactionID                                                 */
        /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/
        log.info("【step1】: Check Transaction ID and equipment Category combination.");
        equipmentMethod.equipmentCategoryVsTxIDCheckCombination(objCommon, equipmentID);

        //----------------------------------------------------------------------------
        //  Check Scrap Wafer Exsit In Carrier (Except loadPurposeType=Other carrier)
        //----------------------------------------------------------------------------
        log.info("【step2】: Check Scrap Wafer Exsit In Carrier (Except loadPurposeType=Other carrier)");
        List<ObjectIdentifier> cassetteIDs = strStartCassette.stream().filter(x -> !CimStringUtils.equals(x.getLoadPurposeType(), BizConstant.SP_LOADPURPOSETYPE_OTHER)).map(Infos.StartCassette::getCassetteID).collect(Collectors.toList());
        if (!CimObjectUtils.isEmpty(cassetteIDs)) {
            List<Infos.LotWaferMap> lotWaferMaps = cassetteMethod.cassetteScrapWaferSelectDR(objCommon, cassetteIDs);
            Validations.check(!CimObjectUtils.isEmpty(lotWaferMaps), retCodeConfig.getFoundScrap());
        }

        /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/
        /*  Object Lock Process    */
        /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/
        log.info("【step3】: Object Lock Process");
        Inputs.ObjLockModeIn objLockModeIn = new Inputs.ObjLockModeIn();
        objLockModeIn.setObjectID(equipmentID);
        objLockModeIn.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
        objLockModeIn.setFunctionCategory(TransactionIDEnum.ARRIVAL_CARRIER_NOTIFICATION_FOR_INTERNAL_BUFFER_REQ.getValue());
        objLockModeIn.setUserDataUpdateFlag(false);
        Outputs.ObjLockModeOut objLockModeOut = objectMethod.objectLockModeGet(objCommon, objLockModeIn);
        Long lockMode = objLockModeOut.getLockMode();
        Inputs.ObjAdvanceLockIn strAdvancedobjecLockin = new Inputs.ObjAdvanceLockIn();
        if (!lockMode.equals(BizConstant.SP_EQP_LOCK_MODE_WRITE)){
            List<String> dummySeq;
            dummySeq = new ArrayList<>(0);
            strAdvancedobjecLockin.setObjectID(params.getEquipmentID());
            strAdvancedobjecLockin.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
            strAdvancedobjecLockin.setObjectType(BizConstant.SP_OBJECTLOCK_OBJECTTYPE_MAINOBJECT);
            strAdvancedobjecLockin.setLockType(objLockModeOut.getRequiredLockForMainObject());
            strAdvancedobjecLockin.setKeyList(dummySeq);
            objectLockMethod.advancedObjectLock(objCommon, strAdvancedobjecLockin);
        } else {
            /*--------------------------------*/
            /*   Lock objects to be updated   */
            /*--------------------------------*/
            objectLockMethod.objectLock(objCommon, CimMachine.class, equipmentID);
        }
        if (!lockMode.equals(BizConstant.SP_EQP_LOCK_MODE_WRITE)){
            int lenCast = CimArrayUtils.getSize(strStartCassette);
            for (int ll = 0; ll < lenCast; ll++){
                objectLockMethod.objectLockForEquipmentResource(objCommon, equipmentID, strStartCassette.get(ll).getLoadPortID(), BizConstant.SP_CLASSNAME_POSPORTRESOURCE);
            }
        } else {
            /**********************************************************/
            /*  Lock All Port Object for internal Buffer Equipment.   */
            /**********************************************************/
            Infos.EqpPortInfo eqpPortInfo = equipmentMethod.equipmentPortInfoForInternalBufferGetDR(objCommon, equipmentID);
            List<Infos.EqpPortStatus> eqpPortStatuses = eqpPortInfo.getEqpPortStatuses();
            int lenPortInfo = CimArrayUtils.getSize(eqpPortStatuses);
            for (int i = 0; i < lenPortInfo; i++){
                objectLockMethod.objectLockForEquipmentResource(objCommon, equipmentID, eqpPortStatuses.get(i).getPortID(), BizConstant.SP_CLASSNAME_POSPORTRESOURCE);
            }
        }
        if (!lockMode.equals(BizConstant.SP_EQP_LOCK_MODE_WRITE)){
            /*--------------------------------------------*/
            /*                                            */
            /*   Material Location Object Lock Process    */
            /*                                            */
            /*--------------------------------------------*/
            List<ObjectIdentifier> loadPurposeType = new ArrayList<>();
            int nCassetteLen = CimArrayUtils.getSize(strStartCassette);
            for (int i = 0; i < nCassetteLen; i++){
                Inputs.ObjAdvancedObjectLockForEquipmentResourceIn objAdvancedObjectLockForEquipmentResourceIn = new Inputs.ObjAdvancedObjectLockForEquipmentResourceIn();
                objAdvancedObjectLockForEquipmentResourceIn.setEquipmentID(equipmentID);
                objAdvancedObjectLockForEquipmentResourceIn.setClassName(BizConstant.SP_CLASSNAME_POSMATERIALLOCATION_EMPTYML);
                objAdvancedObjectLockForEquipmentResourceIn.setObjectID(strStartCassette.get(i).getCassetteID());
                objAdvancedObjectLockForEquipmentResourceIn.setObjectLockType(BizConstant.SP_OBJECTLOCK_LOCKTYPE_WRITE);
                objAdvancedObjectLockForEquipmentResourceIn.setBufferResourceName(strStartCassette.get(i).getLoadPurposeType());
                objAdvancedObjectLockForEquipmentResourceIn.setBufferResourceLockType(BizConstant.SP_OBJECTLOCK_LOCKTYPE_READ);
                objectLockMethod.advancedObjectLockForEquipmentResource(objCommon, objAdvancedObjectLockForEquipmentResourceIn);
            }
        } else {
            /*--------------------------------------------*/
            /*                                            */
            /*   Material Location Object Lock Process    */
            /*                                            */
            /*--------------------------------------------*/
            ObjectIdentifier loadPurposeType = new ObjectIdentifier();
            int nCassetteLen = CimArrayUtils.getSize(strStartCassette);
            for (int i = 0; i < nCassetteLen; i++){
                loadPurposeType.setValue(strStartCassette.get(i).getLoadPurposeType());
                objectLockMethod.objectLockForEquipmentResource(objCommon, equipmentID, loadPurposeType, BizConstant.SP_CLASSNAME_POSMATERIALLOCATION);
            }
        }
        int nILen = CimArrayUtils.getSize(strStartCassette);
        List<ObjectIdentifier> lotIDs = new ArrayList<>();
        for (int i = 0; i < nILen; i++){
            cassetteIDs.add(strStartCassette.get(i).getCassetteID());
            List<Infos.LotInCassette> lotInCassetteList = strStartCassette.get(i).getLotInCassetteList();
            int nJLen = CimArrayUtils.getSize(lotInCassetteList);
            for (int j = 0; j < nJLen; j++){
                lotIDs.add(lotInCassetteList.get(j).getLotID());
            }
        }
        /*------------------------------*/
        /*   Lock Cassette/Lot Object   */
        /*-------------------------------*/
        objectLockMethod.objectSequenceLock(objCommon, CimCassette.class, cassetteIDs);
        objectLockMethod.objectSequenceLock(objCommon, CimLot.class, lotIDs);
        /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/
        /*   Check Process                                                       */
        /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/
        log.info("【step4】: Check Process");
        /*-----------------------------------------------------------------------*/
        /*   Check Process for Cassette                                          */
        /*                                                                       */
        /*   The following conditions are checked by this object                 */
        /*   * Set SP_Operation_NPWCarrierXfer in-param's operation.             */
        /*   - Check Start Cassette's Loading Order                              */
        /*   - controlJobID should not be assigned. :NULL                        */
        /*   - transferState                        :SI/MI/BI                    */
        /*   - dispatchState should be FALSE        :FALSE                       */
        /*   - condition of Cassette                :_Available/_InUse           */
        /*   - Check unload port reserved                                        */
        /*   - Count of Cassette LoadPurposeType                                 */
        /*   - Check Internal Buffer Free Space                                  */
        /*                      VS StartCassette LoadPurposeType Total           */
        /*-----------------------------------------------------------------------*/
        log.info("【step4-1】: Check Process for Cassette");
        cassetteMethod.cassetteCheckConditionForOperationForInternalBuffer(objCommon, equipmentID, strStartCassette, BizConstant.SP_OPERATION_NPWCARRIERXFER);

        /*-----------------------------------------------------------------------*/
        /*   Check Process for Lot                                               */
        /*                                                                       */
        /*   The following conditions are checked by this object                 */
        /*   * Set SP_Operation_NPWCarrierXfer in-param's operation.             */
        /*   For all of lots in Carrier specified in in-param's,                 */
        /*   - holdState      : NotOnHold                                        */
        /*   - inventoryState : InBank                                           */
        /*   - finishedState  : Completed                                        */
        /*   - lot's BankID   : same as eqp-defined CtrlBank                     */
        /*   - controlJobID should not be assigned. :NULL                        */
        /*   - procState      : SP_Lot_ProcState_Waiting                         */
        /*   - invState                             :OnFloor                     */
        /*   - Equipment Availability for Lot                                    */
        /*   - Minimun Wafer Count                                               */
        /*                                                                       */
        /*-----------------------------------------------------------------------*/
        log.info("【step4-2】: Check Process for Lot");
        lotMethod.lotCheckConditionForOperationForInternalBuffer(objCommon, equipmentID, strStartCassette, BizConstant.SP_OPERATION_NPWCARRIERXFER);


        //---------------------------------------------------------------------------------------------//
        //   Check Equipment Port Condition for NPWCarrierXfer                                         //
        //   Check logic itself is same as StartReservation perfectly, so to verify                    //
        //   equipment's condition, equipment_portState_CheckForStartReservationFor internalBuffer()   //
        //   is called.                                                                                //
        //                                                                                             //
        //                                                                                             //
        //   - controlJobID should not be assigned. :NULL                                              //
        //   - port's loadMode                      : CIMFW_PortRsc_Input                              //
        //                                                 / _InputOutput                              //
        //   - portGroup                            : _LoadAvail / _LoadReq                            //
        //          when equipment is Online.                                                          //
        //   - portGroup                            : no loadedCassetteID                              //
        //   - strStartCassette[].loadPortID's                                                         //
        //              portGroupID                 : in-parm's portGroupID                            //
        //   - strStartCassette[].loadPurposeType   : eqp-port's loadPurposeType                       //
        //   - strStartCassette[].loadSequenceNumber: port's loadSequenceNumber                        //
        //                                                                                             //
        //---------------------------------------------------------------------------------------------//
        log.info("【step4-3】: Check Equipment Port Condition for NPWCarrierXfer");
        equipmentMethod.equipmentPortStateCheckForStartReservationForInternalBuffer(objCommon, equipmentID, strStartCassette);

        /*---------------------------------------------------------------------------*/
        /*   Check Category for Copper/Non Copper                                    */
        /*                                                                           */
        /*   It is checked in the following method whether it is the condition       */
        /*   that Lot of the object is made of OpeStart.                             */
        /*                                                                           */
        /*    - It is checked whether CassetteCategoryCapability of CassetteCategory */
        /*      of PosCassette and PosPortResource is the same.                      */
        /*---------------------------------------------------------------------------*/
        log.info("【step4-4】: Check Category for Copper/Non Copper ");
        strStartCassette.forEach(x -> {
            Params.CarrierMoveFromIBRptParams carrierMoveFromIBRptParams = new Params.CarrierMoveFromIBRptParams();
            carrierMoveFromIBRptParams.setCarrierID(x.getCassetteID());
            carrierMoveFromIBRptParams.setEquipmentID(equipmentID);
            carrierMoveFromIBRptParams.setDestinationPortID(x.getLoadPortID());
            cassetteMethod.cassetteCategoryPortCapabilityCheckForContaminationControl(objCommon, carrierMoveFromIBRptParams);
        });

        /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/
        /*   Main Process                                                        */
        /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/
        log.info("【step5】: Main Process");
        /*----------------------------------------------*/
        /*   Change Cassette's Dispatch State to TRUE   */
        /*   Set Cassette's NPWLoadPurposeType.         */
        /*----------------------------------------------*/
        log.info("【step5-1】: Change Cassette's Dispatch State to TRUE Set Cassette's NPWLoadPurposeType.");
        List<Infos.RsvLotCarrier> rsvLotCarriers = new ArrayList<>();
        for (Infos.StartCassette startCassette : strStartCassette) {
            log.info("【step5-1-1】: change cassette dispatch state");
            cassetteMethod.cassetteDispatchStateChange(objCommon, startCassette.getCassetteID(), true);

            log.info("【step5-1-2】: set cassette NPW loadPurposeType");
            cassetteMethod.cassetteSetNPWLoadPurposeType(objCommon, startCassette.getCassetteID(), startCassette.getLoadPurposeType());

            /*-----------------------------------------*/
            /* Copy strRsvLotCarrier to StartCassette  */
            /*-----------------------------------------*/
            List<Infos.LotInCassette> lotInCassetteList = startCassette.getLotInCassetteList();
            if (!CimObjectUtils.isEmpty(lotInCassetteList)) {
                Infos.RsvLotCarrier rsvLotCarrier = new Infos.RsvLotCarrier();
                rsvLotCarrier.setLotID(lotInCassetteList.get(0).getLotID());
                rsvLotCarrier.setCarrierID(startCassette.getCassetteID());
                rsvLotCarriers.add(rsvLotCarrier);
            }
        }

        /*------------------------------------------------------------------------*/
        /*   Set information on strStartCassette on EQP-Shelf(MaterialLocation)   */
        /*------------------------------------------------------------------------*/
        log.info("【step5-2】: Set information on strStartCassette on EQP-Shelf(MaterialLocation)");
        for (Infos.StartCassette startCassette : strStartCassette) {
            equipmentMethod.equipmentAllocatedMaterialAdd(objCommon, equipmentID, startCassette.getCassetteID(), startCassette.getLoadPortID(), startCassette.getLoadPurposeType(), controlJobID);
        }

        /*------------------------------------------------------------------*/
        /*   Send TxNPWCarrierReserveForIBReq to EAP   */
        /*------------------------------------------------------------------*/
        log.info("【step5-3】: Send TxNPWCarrierReserveForIBReq to EAP");
        Inputs.SendNPWCarrierReserveForIBReqIn in = new Inputs.SendNPWCarrierReserveForIBReqIn();
        in.setObjCommonIn(objCommon);
        in.setUser(params.getUser());
        in.setEquipmentID(equipmentID);
        in.setControlJobID(controlJobID);
        in.setPortGroupID(portGroupID);
        in.setStrStartCassette(strStartCassette);
        in.setClaimMemo(claimMemo);

        // tcsMethod.sendTCSReq(TCSReqEnum.sendNPWCarrierReserveReq, in);
        // 更改为EAPMethod调用
        String tmpSleepTimeValue  = StandardProperties.OM_EAP_CONNECT_SLEEP_TIME.getValue();
        String tmpRetryCountValue = StandardProperties.OM_EAP_CONNECT_RETRY_COUNT.getValue();
        long sleepTimeValue;
        long retryCountValue;

        if (CimStringUtils.length(tmpSleepTimeValue) == 0) {
            sleepTimeValue = BizConstant.SP_DEFAULT_SLEEP_TIME_TCS;
        } else {
            sleepTimeValue = CimNumberUtils.intValue(tmpSleepTimeValue);
        }

        if (CimStringUtils.length(tmpRetryCountValue) == 0) {
            retryCountValue = BizConstant.SP_DEFAULT_RETRY_COUNT_TCS;
        } else {
            retryCountValue = CimNumberUtils.intValue(tmpRetryCountValue);
        }
        // 暂时无eap支持
//        tcsMethod.sendTCSReq(TCSReqEnum.sendNPWCarrierReserveForIBReq, in);
        IEAPRemoteManager eapRemoteManager = eapMethod.eapRemoteManager(objCommon, objCommon.getUser(), params.getEquipmentID(), null, true);
        if (eapRemoteManager!=null){
            for(int i = 0 ; i < (retryCountValue + 1) ; i++) {
                try {
                    eapRemoteManager.sendNPWCarrierReserveForIBReq(params);
                } catch (ServiceException ex){
                    if (Validations.isEquals(ex.getCode(),retCodeConfig.getExtServiceBindFail())||
                            Validations.isEquals(ex.getCode(),retCodeConfig.getExtServiceNilObj())||
                            Validations.isEquals(ex.getCode(),retCodeConfig.getTcsNoResponse())){
                        if (i<retryCountValue){
                            try {
                                Thread.sleep(sleepTimeValue);
                                continue;
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                                ex.addSuppressed(e);
                                throw ex;
                            }
                        }
                    }
                    throw ex;
                } catch (Exception ex){
                    if (i<retryCountValue){
                        try {
                            Thread.sleep(sleepTimeValue);
                            continue;
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            ex.addSuppressed(e);
                            throw ex;
                        }
                    }
                    throw ex;
                }
                break;
            }
        }
        /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/
        /*   Check ACCESS Mode                                                   */    //P4100326
        /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/
        log.info("【step6】: Check ACSESS Mode");
        log.info("【step6-1】: get equipment port info");
        Infos.EqpPortInfo eqpPortInfo = equipmentMethod.equipmentPortInfoGet(objCommon, equipmentID);
        List<Infos.EqpPortStatus> eqpPortStatuses = eqpPortInfo.getEqpPortStatuses();
        if (!CimObjectUtils.isEmpty(eqpPortStatuses)) {
            log.info("【step6-2】: call sxCarrierReserveReq");
            if (CimStringUtils.equals(eqpPortStatuses.get(0).getAccessMode(), BizConstant.SP_EQP_ACCESSMODE_AUTO)) {
                Params.CarrierReserveReqParam carrierReserveReqParam = new Params.CarrierReserveReqParam();
                carrierReserveReqParam.setStrRsvLotCarrier(rsvLotCarriers);
                carrierReserveReqParam.setClaimMemo(claimMemo);
                Results.CarrierReserveReqResult sxCarrierReserveReq = this.sxCarrierReserveReq(objCommon, carrierReserveReqParam);
            }
        }
    }

    @Override
    public void sxNPWCarrierReserveReq(Infos.ObjCommon objCommon, Params.NPWCarrierReserveReqParams params) {
        ObjectIdentifier equipmentID = params.getEquipmentID();
        ObjectIdentifier controlJobID = params.getControlJobID();
        String portGroupID = params.getPortGroupID();
        List<Infos.StartCassette> strStartCassette = params.getStartCassetteList();
        /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/
        /*   Check TransactionID                                                 */
        /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/
        log.info("【step1】: Check TransactionID");
        equipmentMethod.equipmentCategoryVsTxIDCheckCombination(objCommon, equipmentID);

        /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/
        /*   Object Lock Process                                    */
        /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/
        log.info("【step2】: Object Lock Process");
        Inputs.ObjLockModeIn objLockModeIn = new Inputs.ObjLockModeIn();
        objLockModeIn.setObjectID(equipmentID);
        objLockModeIn.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
        objLockModeIn.setFunctionCategory(TransactionIDEnum.ARRIVAL_CARRIER_NOTIFICATION_REQ.getValue());
        objLockModeIn.setUserDataUpdateFlag(false);
        Outputs.ObjLockModeOut objLockModeOut = objectMethod.objectLockModeGet(objCommon, objLockModeIn);
        Long lockMode = objLockModeOut.getLockMode();
        Inputs.ObjAdvanceLockIn strAdvancedobjecLockin = new Inputs.ObjAdvanceLockIn();
        if (!lockMode.equals(BizConstant.SP_EQP_LOCK_MODE_WRITE)){
            // Lock Equipment Main Object
            List<String> dummySeq;
            dummySeq = new ArrayList<>(0);
            strAdvancedobjecLockin.setObjectID(params.getEquipmentID());
            strAdvancedobjecLockin.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
            strAdvancedobjecLockin.setObjectType(BizConstant.SP_OBJECTLOCK_OBJECTTYPE_MAINOBJECT);
            strAdvancedobjecLockin.setLockType(objLockModeOut.getRequiredLockForMainObject());
            strAdvancedobjecLockin.setKeyList(dummySeq);

            objectLockMethod.advancedObjectLock(objCommon, strAdvancedobjecLockin);
        } else {
            /*--------------------------------*/
            /*   Lock objects to be updated   */
            /*--------------------------------*/
            objectLockMethod.objectLock(objCommon, CimMachine.class, equipmentID);
        }
        int nStrCassetteLen = CimArrayUtils.getSize(strStartCassette);
        /*--------------------------*/
        /*   Lock Port object       */
        /*--------------------------*/
        for (int i = 0; i < nStrCassetteLen; i++){
            objectLockMethod.objectLockForEquipmentResource(objCommon, equipmentID, strStartCassette.get(i).getLoadPortID(), BizConstant.SP_CLASSNAME_POSPORTRESOURCE);
        }
        List<ObjectIdentifier> lotIDs = new ArrayList<>();
        List<ObjectIdentifier> cassetteIDs = new ArrayList<>();
        for (int i = 0; i < nStrCassetteLen; i++){
            cassetteIDs.add(strStartCassette.get(i).getCassetteID());
            List<Infos.LotInCassette> lotInCassetteList = strStartCassette.get(i).getLotInCassetteList();
            int nStrLotInCassetteLen = CimArrayUtils.getSize(lotInCassetteList);
            for (int j = 0; j < nStrLotInCassetteLen; j++){
                lotIDs.add(lotInCassetteList.get(j).getLotID());
            }
        }
        /*------------------------------*/
        /*   Lock Cassette/Lot Object   */
        objectLockMethod.objectSequenceLock(objCommon, CimCassette.class, cassetteIDs);
        objectLockMethod.objectSequenceLock(objCommon, CimLot.class, lotIDs);
        /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/
        /*   Check Process                                                       */
        /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/
        log.info("【step3】: Check Process");

        /*-----------------------------------------------------------------------*/
        /*   Check Process for Cassette                                          */
        /*                                                                       */
        /*   The following conditions are checked by this object                 */
        /*   * Set SP_Operation_NPWCarrierXfer in-param's operation.             */
        /*   - controlJobID should not be assigned. :NULL                        */
        /*   - MultiLot Type                        :na                          */
        /*   - transferState                        :SI/MI/BI                    */
        /*   - transferReserved                     :FALSE                       */
        /*   - dispatchState should be FALSE        :FALSE                       */
        /*   - condition of Cassette                :_Available/_InUse           */
        /*   - maxBatchSize                                                      */
        /*      LoadPurposeType:ProcessLot          :< total count of cassette   */
        /*                      ProcessMonitorLot   :< total count of cassette   */
        /*                      Any                 :< total count of cassette   */
        /*   - minBatchSize                                                      */
        /*      LoadPurposeType:ProcessLot          :> total count of cassette   */
        /*                      ProcessMonitorLot   :> total count of cassette   */
        /*   - emptyCassetteCount                                                */
        /*                                                                       */
        /*   - loadingSequenceNumber                                             */
        /*      strStartCassette[].loadSequenceNumber  :na                       */
        /*   - eqp's multiRecipeCapability and recipeParameter :na               */
        /*-----------------------------------------------------------------------*/
        log.info("【step3-1】: Check Process for Cassette");
        cassetteMethod.cassetteCheckConditionForOperation(objCommon, equipmentID, portGroupID, strStartCassette, BizConstant.SP_OPERATION_NPWCARRIERXFER);
        /*-----------------------------------------------------------------------*/
        /*   Check Process for Lot                                               */
        /*                                                                       */
        /*   The following conditions are checked by this object                 */
        /*   * Set SP_Operation_NPWCarrierXfer in-param's operation.             */
        /*   For all of lots in Carrier specified in in-param's,                 */
        /*   - controlJobID should not be assigned. :NULL                        */
        /*   - eqpID                                :na                          */
        /*   - holdState                            :na                          */
        /*   - procState                                                         */
        /*        opeStartFlag=TRUE lots !=                                      */
        /*                             SP_Lot_ProcState_ProcessingIn-Processing  */
        /*   - invState                             :OnFloor                     */
        /*   - entityIH                             :na                          */
        /*   - minWfrCnt                            :na                          */
        /*   - eqpAvail                             :Avail                       */
        /*-----------------------------------------------------------------------*/
        log.info("【step3-2】: Check Process for Lot");
        lotMethod.lotCheckConditionForOperation(objCommon, equipmentID, portGroupID, strStartCassette, BizConstant.SP_OPERATION_NPWCARRIERXFER);


        // ----------------------------------------------------------//
        // SLM Check
        // If loadPurpose is "SLMRetrieving",
        // cassette should be registered as destination cassette
        // ----------------------------------------------------------//
        log.info("【step3-3】: Check SLM related conditions");
        // SLM_CheckConditionForOperation
        slmMethod.slmCheckConditionForOperation(objCommon, equipmentID, portGroupID, controlJobID, strStartCassette, null, BizConstant.SP_OPERATION_NPWCARRIERXFER);
        /*-----------------------------------------------------------------------*/
        /*   Check Lock Hold for Lot                                             */
        /*   The following conditions are checked by this object                 */
        /*   - Lot Hold States should be not LOCK HOLD.                          */
        /*-----------------------------------------------------------------------*/
        log.info("【step3-4】: Check LOCK Hold. ");
        for (int i = 0; i < CimArrayUtils.getSize(strStartCassette); i++) {
            for (int j = 0; j < CimArrayUtils.getSize(strStartCassette.get(i).getLotInCassetteList()); j++) {
                lotMethod.lotCheckLockHoldConditionForOperation(objCommon, Arrays.asList(strStartCassette.get(i).getLotInCassetteList().get(j).getLotID()));
            }
        }

        //---------------------------------------------------------------------------//
        //   Check Equipment Port Condition for NPWCarrierXfer                       //
        //                                                                           //
        //   Check logic itself is same as StartReservation perfectly, so to verify  //
        //   equipment's condition, equipment_portState_CheckForStartReservation()   //
        //   is called.                                                              //
        //---------------------------------------------------------------------------//
        log.info("【step3-5】: Check Equipment Port for Non-WIP Carrier Transfer ");
        equipmentMethod.equipmentPortStateCheckForStartReservation(objCommon, equipmentID, portGroupID, strStartCassette,false);

        /*---------------------------------------------------------------------------*/
        /*   Check Category for Copper/Non Copper                                    */
        /*                                                                           */
        /*   It is checked in the following method whether it is the condition       */
        /*   that Lot of the object is made of OpeStart.                             */
        /*                                                                           */
        /*    - It is checked whether CassetteCategoryCapability of CassetteCategory */
        /*      of PosCassette and PosPortResource is the same.                      */
        /*---------------------------------------------------------------------------*/
        log.info("【step3-6】: Check Category for Copper/Non Copper ");
        strStartCassette.forEach(x -> {
            Params.CarrierMoveFromIBRptParams carrierMoveFromIBRptParams = new Params.CarrierMoveFromIBRptParams();
            carrierMoveFromIBRptParams.setCarrierID(x.getCassetteID());
            carrierMoveFromIBRptParams.setEquipmentID(equipmentID);
            carrierMoveFromIBRptParams.setDestinationPortID(x.getLoadPortID());
            cassetteMethod.cassetteCategoryPortCapabilityCheckForContaminationControl(objCommon, carrierMoveFromIBRptParams);
        });

        //----------------------------------------------------------------------------
        //  Check Scrap Wafer Exsit In Carrier (Except loadPurposeType=Other carrier)
        //----------------------------------------------------------------------------
        log.info("【step3-7】: Check Scrap Wafer Exsit In Carrier (Except loadPurposeType=Other carrier)");
        cassetteIDs = strStartCassette.stream().filter(x -> !CimStringUtils.equals(x.getLoadPurposeType(), BizConstant.SP_LOADPURPOSETYPE_OTHER)).map(Infos.StartCassette::getCassetteID).collect(Collectors.toList());
        if (!CimObjectUtils.isEmpty(cassetteIDs)) {
            List<Infos.LotWaferMap> lotWaferMaps = cassetteMethod.cassetteScrapWaferSelectDR(objCommon, cassetteIDs);
            Validations.check(!CimObjectUtils.isEmpty(lotWaferMaps), retCodeConfig.getFoundScrap());
        }

        /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/
        /*   Main Process                                                        */
        /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/
        log.info("【step4】: Main Process");

        /*----------------------------------------------*/
        /*   Change Cassette's Dispatch State to TRUE   */
        /*   Set Cassette's NPWLoadPurposeType.         */
        /*----------------------------------------------*/
        log.info("【step4-1】: Change Cassette's Dispatch State to TRUE Set Cassette's NPWLoadPurposeType.");
        for (Infos.StartCassette startCassette : strStartCassette) {

            cassetteMethod.cassetteDispatchStateChange(objCommon, startCassette.getCassetteID(), true);
            cassetteMethod.cassetteSetNPWLoadPurposeType(objCommon, startCassette.getCassetteID(), startCassette.getLoadPurposeType());
            //--------------------------------------------------------------------//
            // Set Arrival CassetteID to specified Port                           //
            //--------------------------------------------------------------------//
            equipmentMethod.equipmentDispatchStateChange(objCommon, equipmentID, startCassette.getLoadPortID(),
                    BizConstant.SP_PORTRSC_DISPATCHSTATE_DISPATCHED, null, startCassette.getCassetteID(), null, null);
        }

        /*-------------------------------------------------*/
        /*   Send TxNPWCarrierReserveReq to EAP   */
        /*-------------------------------------------------*/
        log.info("【step4-2】: Send TxNPWCarrierReserveReq to EAP");
        Inputs.SendNPWCarrierReserveReqIn in = new Inputs.SendNPWCarrierReserveReqIn();
        in.setControlJobID(params.getControlJobID());
        in.setEquipmentID(params.getEquipmentID());
        in.setPortGropuID(params.getPortGroupID());
        in.setRequestUserID(params.getUser());
        in.setClaimMemo(params.getOpeMemo());
        in.setObjCommonIn(objCommon);
        in.setStrStartCassette(params.getStartCassetteList());
//        tcsMethod.sendTCSReq(TCSReqEnum.sendNPWCarrierReserveReq, in);
        // 更改为EAPMethod调用
        String tmpSleepTimeValue  = StandardProperties.OM_EAP_CONNECT_SLEEP_TIME.getValue();
        String tmpRetryCountValue = StandardProperties.OM_EAP_CONNECT_RETRY_COUNT.getValue();
        long sleepTimeValue;
        long retryCountValue;

        if (CimStringUtils.length(tmpSleepTimeValue) == 0) {
            sleepTimeValue = BizConstant.SP_DEFAULT_SLEEP_TIME_TCS;
        } else {
            sleepTimeValue = CimNumberUtils.intValue(tmpSleepTimeValue);
        }

        if (CimStringUtils.length(tmpRetryCountValue) == 0) {
            retryCountValue = BizConstant.SP_DEFAULT_RETRY_COUNT_TCS;
        } else {
            retryCountValue = CimNumberUtils.intValue(tmpRetryCountValue);
        }

        log.debug("{}{}","env value of OM_EAP_CONNECT_SLEEP_TIME  = ",sleepTimeValue);
        log.debug("{}{}","env value of OM_EAP_CONNECT_RETRY_COUNT = ",retryCountValue);


//        暂时无eap支持
        IEAPRemoteManager eapRemoteManager = eapMethod.eapRemoteManager(objCommon, objCommon.getUser(), params.getEquipmentID(), null, true);
        if (eapRemoteManager!=null){
            for(int i = 0 ; i < (retryCountValue + 1) ; i++) {
                try {
                    eapRemoteManager.sendNPWCarrierReserveReq(params);
                } catch (ServiceException ex){
                    if (Validations.isEquals(ex.getCode(),retCodeConfig.getExtServiceBindFail())||
                            Validations.isEquals(ex.getCode(),retCodeConfig.getExtServiceNilObj())||
                                    Validations.isEquals(ex.getCode(),retCodeConfig.getTcsNoResponse())){
                        if (i<retryCountValue){
                            try {
                                Thread.sleep(sleepTimeValue);
                                continue;
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                                ex.addSuppressed(e);
                                throw ex;
                            }
                        }
                    }
                    throw ex;
                } catch (Exception ex){
                    if (i<retryCountValue){
                        try {
                            Thread.sleep(sleepTimeValue);
                            continue;
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            ex.addSuppressed(e);
                            throw ex;
                        }
                    }
                    throw ex;
                }
                break;
            }
        }

        /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/
        /*   Check ACSESS Mode                                                   */
        /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/
        log.info("【step5】: Check ACSESS Mode");
        Infos.EqpPortInfo eqpPortInfo = equipmentMethod.equipmentPortInfoGet(objCommon, equipmentID);
        List<Infos.EqpPortStatus> eqpPortStatuses = eqpPortInfo.getEqpPortStatuses();
        strStartCassette.forEach(x -> eqpPortStatuses.forEach(y -> {
            if (ObjectIdentifier.equalsWithValue(x.getLoadPortID(), y.getPortID()) && CimStringUtils.equals(y.getAccessMode(), BizConstant.SP_EQP_ACCESSMODE_AUTO)) {
                List<Infos.LotInCassette> lotInCassetteList = x.getLotInCassetteList();
                if (!CimObjectUtils.isEmpty(lotInCassetteList)) {
                    Params.CarrierReserveReqParam carrierReserveReqParam = new Params.CarrierReserveReqParam();
                    List<Infos.RsvLotCarrier> reserveLots = new ArrayList<>();
                    Infos.RsvLotCarrier reserveLot = new Infos.RsvLotCarrier();
                    reserveLot.setLotID(lotInCassetteList.get(0).getLotID());
                    reserveLot.setCarrierID(x.getCassetteID());
                    reserveLots.add(reserveLot);
                    carrierReserveReqParam.setStrRsvLotCarrier(reserveLots);
                    Results.CarrierReserveReqResult sxCarrierReserveReq = this.sxCarrierReserveReq(objCommon, carrierReserveReqParam);
                    return;
                }
            }
        }));
    }

    @Override
    public void sxNPWCarrierReserveCancelForIBReq(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, String portGroup, List<Infos.NPWXferCassette> strNPWXferCassette, Boolean notifyToTCSFlag, String claimMemo) {
        ObjectIdentifier controlJobID = null;
        /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-
        =-=-=-=-=-=-=-=-=-=*/
        /*                                                                       */
        /*   Check Transaction ID                                                */
        /*                                                                       */
        /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/
        log.debug("【step1】 - equipment_categoryVsTxID_CheckCombination");
        equipmentMethod.equipmentCategoryVsTxIDCheckCombination(objCommon, equipmentID);

        int nStrNPWXferCassetteLen = CimArrayUtils.getSize(strNPWXferCassette);
        Validations.check(nStrNPWXferCassetteLen == 0, retCodeConfig.getInvalidInputParam());

        /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/
        /*                                                                       */
        /*   Object Lock Process                                                 */
        /*                                                                       */
        /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/
        log.info("sxNPWCarrierReserveCancelForIBReq: Object Lock Process");
        // step2 - object_lockMode_Get
        Inputs.ObjLockModeIn objLockModeIn = new Inputs.ObjLockModeIn();
        objLockModeIn.setObjectID(equipmentID);
        objLockModeIn.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
        objLockModeIn.setFunctionCategory(TransactionIDEnum.ARRIVAL_CARRIER_CANCEL_FOR_INTERNAL_BUFFER_REQ.getValue());
        objLockModeIn.setUserDataUpdateFlag(false);
        Outputs.ObjLockModeOut objLockModeOut = objectMethod.objectLockModeGet(objCommon, objLockModeIn);
        Long lockMode = objLockModeOut.getLockMode();
        Inputs.ObjAdvanceLockIn strAdvancedobjecLockin = new Inputs.ObjAdvanceLockIn();
        if (!lockMode.equals(BizConstant.SP_EQP_LOCK_MODE_WRITE)) {
            // step3 - advanced_object_Lock
            List<String> dummySeq;
            dummySeq = new ArrayList<>(0);
            strAdvancedobjecLockin.setObjectID(equipmentID);
            strAdvancedobjecLockin.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
            strAdvancedobjecLockin.setObjectType(BizConstant.SP_OBJECTLOCK_OBJECTTYPE_MAINOBJECT);
            strAdvancedobjecLockin.setLockType(objLockModeOut.getRequiredLockForMainObject());
            strAdvancedobjecLockin.setKeyList(dummySeq);

            objectLockMethod.advancedObjectLock(objCommon, strAdvancedobjecLockin);
        } else {
            // step4 - object_Lock
            objectLockMethod.objectLock(objCommon, CimMachine.class, equipmentID);
        }
        /*--------------------------------------------*/
        /*                                            */
        /*        Port Object Lock Process            */
        /*                                            */
        /*--------------------------------------------*/
        /*--------------------------------*/
        /*   Lock Port object (To)        */
        /*--------------------------------*/
        if (!lockMode.equals(BizConstant.SP_EQP_LOCK_MODE_WRITE)) {
            // step5 - object_LockForEquipmentResource
            int lenCast = CimArrayUtils.getSize(strNPWXferCassette);
            for (int ll = 0; ll < lenCast; ll++) {
                objectLockMethod.objectLockForEquipmentResource(objCommon, equipmentID, strNPWXferCassette.get(ll).getLoadPortID(), BizConstant.SP_CLASSNAME_POSPORTRESOURCE);
            }
        } else {
            // step6 - equipment_portInfoForInternalBuffer_GetDR
            Infos.EqpPortInfo eqpPortInfo = equipmentMethod.equipmentPortInfoForInternalBufferGetDR(objCommon, equipmentID);
            List<Infos.EqpPortStatus> eqpPortStatuses = eqpPortInfo.getEqpPortStatuses();
            int lenPortInfo = CimArrayUtils.getSize(eqpPortStatuses);
            for (int jj = 0; jj < lenPortInfo; jj++) {
                // step7 - object_LockForEquipmentResource
                objectLockMethod.objectLockForEquipmentResource(objCommon, equipmentID, eqpPortStatuses.get(jj).getPortID(), BizConstant.SP_CLASSNAME_POSPORTRESOURCE);
            }
        }
        List<Infos.StartCassette> strStartCassetteSequence = new ArrayList<>();
        /*--------------------------------*/
        /*   Lock Port object (From)      */
        /*--------------------------------*/
        for (int i = 0; i < nStrNPWXferCassetteLen; i++) {
            /*-----------------------------------*/
            /*  Check cassette transfer status   */
            /*-----------------------------------*/
            log.info("sxNPWCarrierReserveCancelForIBReq: Check cassette transfer status");
            log.debug("【step8】 - cassette_transferState_Get");
            String cassetteTransferStateGetOut = cassetteMethod.cassetteTransferStateGet(objCommon, strNPWXferCassette.get(i).getCassetteID());
            if (CimStringUtils.equals(cassetteTransferStateGetOut, BizConstant.SP_TRANSSTATE_EQUIPMENTIN)) {
                log.info("sxNPWCarrierReserveCancelForIBReq: Cassette's transferState == EI ");
                /*------------------------------------*/
                /*   Get Cassette Info in Equipment   */
                /*------------------------------------*/
                log.info("sxNPWCarrierReserveCancelForIBReq: Get Cassette Info in Equipment");
                log.debug("【step9】 - cassette_equipmentID_Get");
                Outputs.ObjCassetteEquipmentIDGetOut cassetteEquipmentIDGetOut = cassetteMethod.cassetteEquipmentIDGet(objCommon, strNPWXferCassette.get(i).getCassetteID());
                log.info("sxNPWCarrierReserveCancelForIBReq: equipmentID:{}", cassetteEquipmentIDGetOut.getEquipmentID().getValue());

                log.debug("【step10】 - equipment_portInfoForInternalBuffer_GetDR");
                Infos.EqpPortInfo eqpPortInfo = equipmentMethod.equipmentPortInfoForInternalBufferGetDR(objCommon, cassetteEquipmentIDGetOut.getEquipmentID());
                /*----------------------------------------------------------*/
                /*  Lock port object which has the specified cassette.      */
                /*----------------------------------------------------------*/
                List<Infos.EqpPortStatus> eqpPortStatuses = eqpPortInfo.getEqpPortStatuses();
                int lenFromPort = CimArrayUtils.getSize(eqpPortStatuses);
                for (int j = 0; j < lenFromPort; j++) {
                    // step11- object_LockForEquipmentResource
                    if (!lockMode.equals(BizConstant.SP_EQP_LOCK_MODE_WRITE)) {
                        if (!ObjectIdentifier.equalsWithValue(strNPWXferCassette.get(i).getCassetteID(), eqpPortStatuses.get(j).getLoadedCassetteID())) {
                            continue;
                        }
                    }
                    objectLockMethod.objectLockForEquipmentResource(objCommon, cassetteEquipmentIDGetOut.getEquipmentID(), eqpPortStatuses.get(j).getPortID(), BizConstant.SP_CLASSNAME_POSPORTRESOURCE);
                }
            }
        }
        List<ObjectIdentifier> cassetteIDs = new ArrayList<>();
        for (int i = 0; i < nStrNPWXferCassetteLen; i++) {
            log.info("sxNPWCarrierReserveCancelForIBReq: strNPWXferCassette :{}", i);
            cassetteIDs.add(strNPWXferCassette.get(i).getCassetteID());
            if (!lockMode.equals(BizConstant.SP_EQP_LOCK_MODE_WRITE)) {
                // step12 - advanced_object_LockForEquipmentResource
                Inputs.ObjAdvancedObjectLockForEquipmentResourceIn objAdvancedObjectLockForEquipmentResourceIn = new Inputs.ObjAdvancedObjectLockForEquipmentResourceIn();
                objAdvancedObjectLockForEquipmentResourceIn.setEquipmentID(equipmentID);
                objAdvancedObjectLockForEquipmentResourceIn.setClassName(BizConstant.SP_CLASSNAME_POSMATERIALLOCATION_BYCASTID);
                objAdvancedObjectLockForEquipmentResourceIn.setObjectID(strNPWXferCassette.get(i).getCassetteID());
                objAdvancedObjectLockForEquipmentResourceIn.setObjectLockType(BizConstant.SP_OBJECTLOCK_LOCKTYPE_WRITE);
                objAdvancedObjectLockForEquipmentResourceIn.setBufferResourceLockType(BizConstant.SP_OBJECTLOCK_LOCKTYPE_READ);
                objAdvancedObjectLockForEquipmentResourceIn.setBufferResourceName(strNPWXferCassette.get(i).getLoadPurposeType());
                objectLockMethod.advancedObjectLockForEquipmentResource(objCommon, objAdvancedObjectLockForEquipmentResourceIn);
            }
            Infos.StartCassette startCassette = new Infos.StartCassette();
            strStartCassetteSequence.add(i, startCassette);
            startCassette.setLoadSequenceNumber(strNPWXferCassette.get(i).getLoadSequenceNumber());
            startCassette.setCassetteID(strNPWXferCassette.get(i).getCassetteID());
            startCassette.setLoadPurposeType(strNPWXferCassette.get(i).getLoadPurposeType());
            startCassette.setLoadPortID(strNPWXferCassette.get(i).getLoadPortID());
            startCassette.setUnloadPortID(strNPWXferCassette.get(i).getUnloadPortID());
        }
        /*-------------------*/
        /*   Lock Cassette   */
        /*-------------------*/
        // step12 step13 - objectSequence_Lock
        objectLockMethod.objectSequenceLock(objCommon, CimCassette.class, cassetteIDs);
        /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/
        /*                                                                       */
        /*   Port Status Change Process    (To Equipment)                        */
        /*                                                                       */
        /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/
        log.info("sxNPWCarrierReserveCancelForIBReq:Port Status Change Process    (To Equipment)");
        for (int i = 0; i < nStrNPWXferCassetteLen; i++) {
            log.info("sxNPWCarrierReserveCancelForIBReq: NPWCassette Len :{}", i);
            log.debug("【step14】 - equipment_portInfoForInternalBuffer_GetDR");
            Infos.EqpPortInfo eqpPortInfo = equipmentMethod.equipmentPortInfoForInternalBufferGetDR(objCommon, equipmentID);
            int lenPortInfo = CimArrayUtils.getSize(eqpPortInfo.getEqpPortStatuses());
            for (int j = 0; j < lenPortInfo; j++) {
                if (!ObjectIdentifier.equalsWithValue(strNPWXferCassette.get(i).getLoadPortID(), eqpPortInfo.getEqpPortStatuses().get(j).getPortID())) {
                    log.info("sxNPWCarrierReserveCancelForIBReq: strNPWXferCassette.get(i).getLoadPortID() != eqpPortInfo.getEqpPortStatuses().get(j).getPortID()");
                    continue;
                }
                if (CimStringUtils.equals(BizConstant.SP_PORTRSC_DISPATCHSTATE_DISPATCHED, eqpPortInfo.getEqpPortStatuses().get(j).getDispatchState())) {
                    log.info("sxNPWCarrierReserveCancelForIBReq: dispatchState is [Dispatched]");
                    /*------------------------*/
                    /*   change to Required   */
                    /*------------------------*/
                    log.debug("【step15】 - equipment_dispatchState_Change");
                    equipmentMethod.equipmentDispatchStateChange(objCommon,
                            equipmentID,
                            eqpPortInfo.getEqpPortStatuses().get(j).getPortID(),
                            BizConstant.SP_PORTRSC_DISPATCHSTATE_REQUIRED,
                            null,
                            null,
                            null,
                            null);
                    break;
                }
            }
        }
        /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-*/
        /*   Port Status Change Process    (From Equipment)                                                           */
        /*                                                                                                            */
        /*   Normally, equipment_dispatchState_Change() of this logic is not performed.                               */
        /*   Because condition of XferState:[EI] , PortState:[UnLoad] , DispatchState:[Dispatched] does not happen.   */
        /*   But this logic remains in order to delete garbage.                                                       */
        /*                                                                                                            */
        /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-*/
        log.info("sxNPWCarrierReserveCancelForIBReq:Port Status Change Process");
        for (int i = 0; i < nStrNPWXferCassetteLen; i++) {
            log.info("sxNPWCarrierReserveCancelForIBReq: NPWCassette Len :{}", i);
            /*-----------------------------------*/
            /*  Check cassette transfer status   */
            /*-----------------------------------*/
            log.debug("【step16】 - cassette_transferState_Get");
            String cassetteTransferStateGetOut = cassetteMethod.cassetteTransferStateGet(objCommon, strNPWXferCassette.get(i).getCassetteID());
            if (CimStringUtils.equals(BizConstant.SP_TRANSSTATE_EQUIPMENTIN, cassetteTransferStateGetOut)) {
                log.info("sxNPWCarrierReserveCancelForIBReq: transferState = [EI]");
                /*------------------------------------*/
                /*   Get Cassette Info in Equipment   */
                /*------------------------------------*/
                log.debug("【step17】 - cassette_equipmentID_Get");
                Outputs.ObjCassetteEquipmentIDGetOut cassetteEquipmentIDGetOut = cassetteMethod.cassetteEquipmentIDGet(objCommon, strNPWXferCassette.get(i).getCassetteID());

                log.debug("【step18】 - equipment_portInfoForInternalBuffer_GetDR");
                Infos.EqpPortInfo eqpPortInfo = equipmentMethod.equipmentPortInfoForInternalBufferGetDR(objCommon, cassetteEquipmentIDGetOut.getEquipmentID());
                int lenEqpPort = CimArrayUtils.getSize(eqpPortInfo.getEqpPortStatuses());
                log.info("sxNPWCarrierReserveCancelForIBReq: lenEqpPort = {}", lenEqpPort);
                for (int j = 0; j < lenEqpPort; j++) {
                    log.info("sxNPWCarrierReserveCancelForIBReq: eqpPortStatus = {}", j);
                    if (ObjectIdentifier.equalsWithValue(strNPWXferCassette.get(i).getCassetteID(),
                            eqpPortInfo.getEqpPortStatuses().get(j).getLoadedCassetteID())) {
                        log.info("sxNPWCarrierReserveCancelForIBReq: cassetteID = loadedCassetteID");
                        if (CimStringUtils.equals(BizConstant.SP_PORTRSC_PORTSTATE_UNLOADREQ, eqpPortInfo.getEqpPortStatuses().get(j).getPortState())
                                && CimStringUtils.equals(BizConstant.SP_PORTRSC_DISPATCHSTATE_DISPATCHED, eqpPortInfo.getEqpPortStatuses().get(j).getDispatchState())) {
                            log.info("sxNPWCarrierReserveCancelForIBReq: portState = [UnloadReq] && dispatchState = [Dispatched]");
                            /*------------------------*/
                            /*   change to Required   */
                            /*------------------------*/
                            log.debug("【step19】 - equipment_dispatchState_Change");
                            equipmentMethod.equipmentDispatchStateChange(objCommon,
                                    cassetteEquipmentIDGetOut.getEquipmentID(),
                                    eqpPortInfo.getEqpPortStatuses().get(j).getPortID(),
                                    BizConstant.SP_PORTRSC_DISPATCHSTATE_REQUIRED,
                                    null,
                                    null,
                                    null,
                                    null);
                        }
                    }
                }
            }
        }
        /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/
        /*                                                                       */
        /*   Check Process                                                       */
        /*                                                                       */
        /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/
        log.info("sxNPWCarrierReserveCancelForIBReq: Check Process");
        /*-----------------------------------------------------------------------*/
        /*                                                                       */
        /*   Check Process for Cassette                                          */
        /*                                                                       */
        /*   The following conditions are checked by this object                 */
        /*                                                                       */
        /*   - dispatchState should be TRUE.                                     */
        /*   - controlJobID should be blank'.                                    */
        /*                                                                       */
        /*-----------------------------------------------------------------------*/
        log.info("sxNPWCarrierReserveCancelForIBReq: Check Process for Cassette");
        log.debug("【step20】 - cassette_CheckConditionForArrivalCarrierCancel");
        cassetteMethod.cassetteCheckConditionForArrivalCarrierCancel(objCommon, strNPWXferCassette);
        /*-----------------------------------------------------------------------*/
        /*                                                                       */
        /*   Check Process for Lot                                               */
        /*                                                                       */
        /*   The following conditions are checked by this object                 */
        /*   - Omit Empty cassette.                                              */
        /*   - lotProcessState should not be InProcessing.                       */
        /*   - controlJobID should be blank.                                     */
        /*                                                                       */
        /*-----------------------------------------------------------------------*/
        log.info("sxNPWCarrierReserveCancelForIBReq: Check Process for Lot");
        log.debug("【step21】 - lot_CheckConditionForArrivalCarrierCancel");
        lotMethod.lotCheckConditionForArrivalCarrierCancel(objCommon, controlJobID, strStartCassetteSequence);
        /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/
        /*                                                                       */
        /*   Main Process                                                        */
        /*                                                                       */
        /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/
        log.info("sxNPWCarrierReserveCancelForIBReq: Main Process");
        /*---------------------------------------------------*/
        /*                                                   */
        /*   Cassette Related Information Update Procedure   */
        /*                                                   */
        /*---------------------------------------------------*/
        log.info("sxNPWCarrierReserveCancelForIBReq: Cassette Related Information Update Procedure");
        for (int i = 0; i < nStrNPWXferCassetteLen; i++) {
            /*-----------------------------------------------*/
            /*   Change Cassette's Dispatch State to FALSE   */
            /*-----------------------------------------------*/
            log.info("sxNPWCarrierReserveCancelForIBReq: Change Cassette's Dispatch State to FALSE");
            log.debug("【step22】 - cassette_dispatchState_Change");
            cassetteMethod.cassetteDispatchStateChange(objCommon, strNPWXferCassette.get(i).getCassetteID(), false);
            /*--------------------------------------------------*/
            /*   Change Cassette's NPWLoadPurposeType to NULL   */
            /*--------------------------------------------------*/
            log.info("sxNPWCarrierReserveCancelForIBReq: Change Cassette's NPWLoadPurposeType to NULL");
            log.debug("【step23】 - cassette_SetNPWLoadPurposeType");
            cassetteMethod.cassetteSetNPWLoadPurposeType(objCommon, strNPWXferCassette.get(i).getCassetteID(), null);
        }
        /*-------------------------------------------------*/
        /*                                                 */
        /*   Clear MaterialLocations(shelf of equipment)   */
        /*                                                 */
        /*-------------------------------------------------*/
        log.info("sxNPWCarrierReserveCancelForIBReq: Clear MaterialLocations(shelf of equipment)");
        log.debug("【step24】 - equipment_ArrivalCarrierCancelForInternalBuffer");
        equipmentMethod.equipmentArrivalCarrierCancelForInternalBuffer(objCommon,
                equipmentID,
                controlJobID,
                strNPWXferCassette);

        // tcsMethod.sendTCSReq(TCSReqEnum.sendNPWCarrierReserveReq, in);
        // 更改为EAPMethod调用
        String tmpSleepTimeValue  = StandardProperties.OM_EAP_CONNECT_SLEEP_TIME.getValue();
        String tmpRetryCountValue = StandardProperties.OM_EAP_CONNECT_RETRY_COUNT.getValue();
        long sleepTimeValue;
        long retryCountValue;

        if (CimStringUtils.length(tmpSleepTimeValue) == 0) {
            sleepTimeValue = BizConstant.SP_DEFAULT_SLEEP_TIME_TCS;
        } else {
            sleepTimeValue = CimNumberUtils.intValue(tmpSleepTimeValue);
        }

        if (CimStringUtils.length(tmpRetryCountValue) == 0) {
            retryCountValue = BizConstant.SP_DEFAULT_RETRY_COUNT_TCS;
        } else {
            retryCountValue = CimNumberUtils.intValue(tmpRetryCountValue);
        }
        /*------------------------------------------------------------------------*/
        /*                                                                        */
        /*   Send NPWCarrierReserveCancelForIBReq() to TCS Procedure     */
        /*                                                                        */
        /*------------------------------------------------------------------------*/
//        if (CimBooleanUtils.isTrue(notifyToTCSFlag)) {
//            log.info("sxNPWCarrierReserveCancelForIBReq: Send NPWCarrierReserveCancelForIBReq() to TCS Procedure");
//            Inputs.SendArrivalCarrierNotificationCancelForInternalBufferReqIn sendArrivalCarrierNotificationCancelForInternalBufferReqIn = new Inputs.SendArrivalCarrierNotificationCancelForInternalBufferReqIn();
//            sendArrivalCarrierNotificationCancelForInternalBufferReqIn.setObjCommonIn(objCommon);
//            sendArrivalCarrierNotificationCancelForInternalBufferReqIn.setRequestUserID(objCommon.getUser());
//            sendArrivalCarrierNotificationCancelForInternalBufferReqIn.setEquipmentID(equipmentID);
//            sendArrivalCarrierNotificationCancelForInternalBufferReqIn.setPortGroupID(portGroup);
//            sendArrivalCarrierNotificationCancelForInternalBufferReqIn.setStrNPWXferCassette(strNPWXferCassette);
//            sendArrivalCarrierNotificationCancelForInternalBufferReqIn.setClaimMemo(claimMemo);
////            暂时无eap支持
////            tcsMethod.sendTCSReq(TCSReqEnum.sendArrivalCarrierNotificationCancelForInternalBufferReq, sendArrivalCarrierNotificationCancelForInternalBufferReqIn);
//        }
        Params.NPWCarrierReserveCancelForIBReqParams sendNPWCarrierReserveCancelForIBReqParams = new Params.NPWCarrierReserveCancelForIBReqParams();
//        sendArrivalCarrierNotificationCancelForInternalBufferReqIn.setObjCommonIn(objCommon);
        sendNPWCarrierReserveCancelForIBReqParams.setUser(objCommon.getUser());
        sendNPWCarrierReserveCancelForIBReqParams.setEquipmentID(equipmentID);
        sendNPWCarrierReserveCancelForIBReqParams.setPortGroupID(portGroup);
        sendNPWCarrierReserveCancelForIBReqParams.setNpwTransferCassetteList(strNPWXferCassette);
        sendNPWCarrierReserveCancelForIBReqParams.setOpeMemo(claimMemo);
        sendNPWCarrierReserveCancelForIBReqParams.setNotifyToTCSFlag(notifyToTCSFlag);
        IEAPRemoteManager eapRemoteManager = eapMethod.eapRemoteManager(objCommon, objCommon.getUser(), equipmentID, null, true);
        if (eapRemoteManager!=null){
            for(int i = 0 ; i < (retryCountValue + 1) ; i++) {
                try {
                    eapRemoteManager.sendNPWCarrierReserveCancelForIBReq(sendNPWCarrierReserveCancelForIBReqParams);
                } catch (ServiceException ex){
                    if (Validations.isEquals(ex.getCode(),retCodeConfig.getExtServiceBindFail())||
                            Validations.isEquals(ex.getCode(),retCodeConfig.getExtServiceNilObj())||
                            Validations.isEquals(ex.getCode(),retCodeConfig.getTcsNoResponse())){
                        if (i<retryCountValue){
                            try {
                                Thread.sleep(sleepTimeValue);
                                continue;
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                                ex.addSuppressed(e);
                                throw ex;
                            }
                        }
                    }
                    throw ex;
                } catch (Exception ex){
                    if (i<retryCountValue){
                        try {
                            Thread.sleep(sleepTimeValue);
                            continue;
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            ex.addSuppressed(e);
                            throw ex;
                        }
                    }
                    throw ex;
                }
                break;
            }
        }

    }

    @Override
    public void sxNPWCarrierReserveCancelReq(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, String portGroupID, List<Infos.NPWXferCassette> strNPWXferCassette, Boolean notifyToTCSFlag, String claimMemo) {


        int nStrNPWCassetteLen = CimArrayUtils.getSize(strNPWXferCassette);
        Validations.check( nStrNPWCassetteLen == 0 , retCodeConfig.getInvalidParameter());


        //D4000028(1) Add Start
        if (ObjectIdentifier.isEmpty(equipmentID)){
            log.info("equipmentID is null");
            /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/
            /*                                                                       */
            /*   Object Lock Process                                                 */
            /*                                                                       */
            /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/
            /*--------------------------------*/
            /*   Lock objects to be updated   */
            /*--------------------------------*/
            //  object_Lock
            objectLockMethod.objectLock(objCommon, CimMachine.class, equipmentID);

            for (int i=0 ; i< nStrNPWCassetteLen ; i++ ) {
                objectLockMethod.objectLock(objCommon, CimCassette.class, strNPWXferCassette.get(i).getCassetteID());
            }

            /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/
            /*                                                                       */
            /*   Check Process                                                       */
            /*                                                                       */
            /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/
            log.info("Check Process");
            /*-----------------------------------------------------------------------*/
            /*                                                                       */
            /*   Check Process for Cassette                                          */
            /*                                                                       */
            /*   The following conditions are checked by this object                 */
            /*                                                                       */
            /*   - dispatchState should be TRUE.                                     */
            /*   - controlJobID should be blank'.                                    */
            /*                                                                       */
            /*-----------------------------------------------------------------------*/
            log.info("Check Process for Cassette");
            cassetteMethod.cassetteCheckConditionForArrivalCarrierCancel(objCommon, strNPWXferCassette );

            /*-----------------------------------------------------------------------*/
            /*                                                                       */
            /*   Check Process for Lot                                               */
            /*                                                                       */
            /*   The following conditions are checked by this object                 */
            /*   - Omit Empty cassette.                                              */
            /*   - lotProcessState should not be InProcessing.                       */
            /*   - controlJobID should be blank.                                     */
            /*                                                                       */
            /*-----------------------------------------------------------------------*/
            ObjectIdentifier controlJobID = null;

            /*-----------------------------------------------------------*/
            /*   Make strStartCassetteSequence from strNPWXferCassette   */
            /*-----------------------------------------------------------*/
            List<Infos.StartCassette> strStartCassetteSequence = new ArrayList<>();

            for(int i = 0 ; i < nStrNPWCassetteLen ; i++ ) {
                Infos.StartCassette startCassette = new Infos.StartCassette();
                startCassette.setLoadSequenceNumber(strNPWXferCassette.get(i).getLoadSequenceNumber());
                startCassette.setCassetteID(strNPWXferCassette.get(i).getCassetteID());
                startCassette.setLoadPurposeType(strNPWXferCassette.get(i).getLoadPurposeType());
                startCassette.setLoadPortID(strNPWXferCassette.get(i).getLoadPortID() );
                startCassette.setUnloadPortID(strNPWXferCassette.get(i).getUnloadPortID());
                strStartCassetteSequence.add(startCassette);
            }
            log.info("Check Process for Lot");
            lotMethod.lotCheckConditionForArrivalCarrierCancel(objCommon, null, strStartCassetteSequence );


            /*=-=-=-=-=-=-=-=-=-=-=-=*/
            /*   Main Process        */
            /*=-=-=-=-=-=-=-=-=-=-=-=*/

            /*---------------------------------------------------*/
            /*   Cassette Related Information Update Procedure   */
            /*---------------------------------------------------*/
            log.info("Cassette Related Information Update Procedure");
            for (int i=0 ; i< nStrNPWCassetteLen ; i++ ) {
                /*-----------------------------------------------*/
                /*   Change Cassette's Dispatch State to FALSE   */
                /*-----------------------------------------------*/
                log.info("Change Cassette's Dispatch State to FALSE");
                cassetteMethod.cassetteDispatchStateChange(objCommon ,strNPWXferCassette.get(i).getCassetteID() , false );
                /*--------------------------------------------------*/
                /*   Change Cassette's NPWLoadPurposeType to NULL   */
                /*--------------------------------------------------*/
                log.info("Change Cassette's NPWLoadPurposeType to NULL");

                cassetteMethod.cassetteSetNPWLoadPurposeType(objCommon, strNPWXferCassette.get(i).getCassetteID(), null );
            }
        } else {

            /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/
            /*   Check TransactionID         */
            /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/
            log.info("Check TransactionID");
            equipmentMethod.equipmentCategoryVsTxIDCheckCombination(objCommon, equipmentID );
            /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/
            /*                                                                       */
            /*   Object Lock Process                                                 */
            /*                                                                       */
            /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/
            log.info("Object Lock Process");
            // Get required equipment lock mode
            Inputs.ObjLockModeIn objLockModeIn = new Inputs.ObjLockModeIn();
            objLockModeIn.setObjectID(equipmentID);
            objLockModeIn.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
            objLockModeIn.setFunctionCategory(TransactionIDEnum.ARRIVAL_CARRIER_CANCEL_REQ.getValue());
            objLockModeIn.setUserDataUpdateFlag(false);
            Outputs.ObjLockModeOut objLockModeOut = objectMethod.objectLockModeGet(objCommon, objLockModeIn);
            Long lockMode = objLockModeOut.getLockMode();
            Inputs.ObjAdvanceLockIn strAdvancedobjecLockin = new Inputs.ObjAdvanceLockIn();
            if (!lockMode.equals(BizConstant.SP_EQP_LOCK_MODE_WRITE)) {
                log.info("", "lockMode != SP_EQP_LOCK_MODE_WRITE");
                List<String> dummySeq;
                dummySeq = new ArrayList<>(0);
                strAdvancedobjecLockin.setObjectID(equipmentID);
                strAdvancedobjecLockin.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
                strAdvancedobjecLockin.setObjectType(BizConstant.SP_OBJECTLOCK_OBJECTTYPE_MAINOBJECT);
                strAdvancedobjecLockin.setLockType(objLockModeOut.getRequiredLockForMainObject());
                strAdvancedobjecLockin.setKeyList(dummySeq);

                objectLockMethod.advancedObjectLock(objCommon, strAdvancedobjecLockin);
            } else {
                log.info("", "lockMode = SP_EQP_LOCK_MODE_WRITE");
                objectLockMethod.objectLock(objCommon, CimMachine.class, equipmentID);
            }
            int i = 0;
            int j = 0;
            nStrNPWCassetteLen = CimArrayUtils.getSize(strNPWXferCassette) ;

            /*--------------------------------------------*/
            /*                                            */
            /*        Port Object Lock Process            */
            /*                                            */
            /*--------------------------------------------*/
            /*--------------------------*/
            /*   Lock Port object (To)  */
            /*--------------------------*/
            /*--------------------------------------------------------*/
            /*   Get All Ports being in the same Port Group as ToPort */
            /*--------------------------------------------------------*/
            Infos.EqpPortInfo eqpPortInfo0 = portMethod.portResourceAllPortsInSameGroupGet(objCommon, equipmentID, strNPWXferCassette.get(0).getLoadPortID());

            /*---------------------------------------------------------*/
            /* Lock All Ports being in the same Port Group as ToPort   */
            /*---------------------------------------------------------*/
            // object_LockForEquipmentResource
            List<Infos.EqpPortStatus> eqpPortStatuses0 = eqpPortInfo0.getEqpPortStatuses();
            int lenToPort = CimArrayUtils.getSize(eqpPortStatuses0);
            for ( int ii=0 ; ii < lenToPort ; ii++ ) {
                objectLockMethod.objectLockForEquipmentResource(objCommon, equipmentID, eqpPortStatuses0.get(ii).getPortID(), BizConstant.SP_CLASSNAME_POSPORTRESOURCE);
            }
            /*--------------------------------*/
            /*   Lock Port object (From)      */
            /*--------------------------------*/
            for (int ii=0 ; ii<nStrNPWCassetteLen ; ii++ ) {
                log.info("Check cassette transfer status");
                String transferState = cassetteMethod.cassetteTransferStateGet(objCommon, strNPWXferCassette.get(ii).getCassetteID());

                if (CimStringUtils.equals(transferState, BizConstant.SP_TRANSSTATE_EQUIPMENTIN)) {
                    /*------------------------------------*/
                    /*   Get Cassette Info in Equipment   */
                    /*------------------------------------*/
                    log.info("Get Cassette Info in Equipment");
                    Outputs.ObjCassetteEquipmentIDGetOut strCassette_equipmentID_Get_out = cassetteMethod.cassetteEquipmentIDGet(objCommon, strNPWXferCassette.get(ii).getCassetteID());

                    log.info("equipmentID == {}", strCassette_equipmentID_Get_out.getEquipmentID().getValue());
                    Infos.EqpPortInfo eqpPortInfo = equipmentMethod.equipmentPortInfoGet(objCommon, strCassette_equipmentID_Get_out.getEquipmentID());
                    List<Infos.EqpPortStatus> eqpPortStatuses = eqpPortInfo.getEqpPortStatuses();
                    int lenFromPort = CimArrayUtils.getSize(eqpPortStatuses);
                    for (int jj = 0; jj < lenFromPort; jj++) {
                        if (ObjectIdentifier.equalsWithValue(strNPWXferCassette.get(ii).getCassetteID(), eqpPortStatuses.get(jj).getLoadedCassetteID())) {
                            //  object_LockForEquipmentResource
                            objectLockMethod.objectLockForEquipmentResource(objCommon, strCassette_equipmentID_Get_out.getEquipmentID(), eqpPortStatuses.get(jj).getPortID(), BizConstant.SP_CLASSNAME_POSPORTRESOURCE);
                        }
                    }
                }
            }
            for ( i=0 ; i< nStrNPWCassetteLen ; i++ ) {
                objectLockMethod.objectLock(objCommon, CimCassette.class, strNPWXferCassette.get(i).getCassetteID());
            }

            /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/
            /*                                                                       */
            /*   Port Status Change Process    (To Equipment)                        */
            /*                                                                       */
            /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/
            log.info("Port Status Change Process    (To Equipment)");
            for ( i=0 ; i< nStrNPWCassetteLen ; i++ ) {

                int basePGNo = -1; //P7000350

                Infos.EqpPortInfo eqpPortInfo = equipmentMethod.equipmentPortInfoGet(objCommon, equipmentID );
                int lenPortInfo = CimArrayUtils.getSize(eqpPortInfo.getEqpPortStatuses());

                for (j=0 ; j<lenPortInfo ; j++ ) {
                    if (!ObjectIdentifier.equalsWithValue(strNPWXferCassette.get(i).getLoadPortID(),
                            eqpPortInfo.getEqpPortStatuses().get(j).getPortID())) {
                        continue;
                    }

                    if (CimStringUtils.equals(eqpPortInfo.getEqpPortStatuses().get(j).getDispatchState(), BizConstant.SP_PORTRSC_DISPATCHSTATE_DISPATCHED)) {

                        /*------------------------*/
                        /*   change to Required   */
                        /*------------------------*/
                        log.info("change to Required");
                        equipmentMethod.equipmentDispatchStateChange(objCommon, equipmentID, eqpPortInfo.getEqpPortStatuses().get(j).getPortID(),
                                BizConstant.SP_PORTRSC_DISPATCHSTATE_REQUIRED, null, null, null, null );
                        break;
                    }
                }

                /*--------------------------*/
                /*   find Same Port Group   */
                /*--------------------------*/
                if ( 0 <= basePGNo ) {
                    log.info("0 <= basePGNo:{}",basePGNo);
                    for ( j=0 ; j<lenPortInfo ; j++ ) {
                        /*===== Omit Base Port =====*/
                        if ( j == basePGNo ) {
                            log.info("Omit Base Port {}",j);
                            continue;
                        }
                        /*===== Omit Different Group's Port =====*/
                        if ( !CimStringUtils.equals(eqpPortInfo.getEqpPortStatuses().get(j).getPortGroup(), eqpPortInfo.getEqpPortStatuses().get(basePGNo).getPortGroup())) {
                            log.info("Omit Different Group's Port Not Equal");
                            continue;
                        }

                        /*===== Check portState =====*/
                        if (!CimStringUtils.equals(eqpPortInfo.getEqpPortStatuses().get(j).getPortState(), BizConstant.SP_PORTRSC_PORTSTATE_LOADREQ) &&
                                !CimStringUtils.equals(eqpPortInfo.getEqpPortStatuses().get(j).getPortState(), BizConstant.SP_PORTRSC_PORTSTATE_UNLOADREQ) &&
                                !CimStringUtils.equals(eqpPortInfo.getEqpPortStatuses().get(j).getPortState(), BizConstant.SP_PORTRSC_PORTSTATE_UNKNOWN)) {
                            log.info("Omit Different Group's Port Not Equal");
                            continue;
                        }

                        /*===== Check dispatchState =====*/
                        if ( CimStringUtils.equals(eqpPortInfo.getEqpPortStatuses().get(j).getDispatchState(), BizConstant.SP_PORTRSC_DISPATCHSTATE_DISPATCHED)
                                || CimStringUtils.equals(eqpPortInfo.getEqpPortStatuses().get(j).getDispatchState(), BizConstant.SP_PORTRSC_DISPATCHSTATE_NOTDISPATCHED)) {
                            log.info("Check dispatchState");

                            /*------------------------*/
                            /*   change to Required   */
                            /*------------------------*/
                            log.info("change to Required");
                            equipmentMethod.equipmentDispatchStateChange(objCommon, equipmentID, eqpPortInfo.getEqpPortStatuses().get(j).getPortID(),
                                    BizConstant.SP_PORTRSC_DISPATCHSTATE_REQUIRED, null, null, null, null );
                        }
                    }
                }
            }


            /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/
            /*                                                                       */
            /*   Port Status Change Process    (From Equipment)                      */
            /*                                                                       */
            /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/
            log.info("Port Status Change Process");

            for ( i=0 ; i< nStrNPWCassetteLen ; i++ ) {
                log.info("PPTNpwCassetteLen :{}",i);
                /*-----------------------------------*/
                /*  Check cassette transfer status   */
                /*-----------------------------------*/
                log.info("Check cassette transfer status");
                String strCassette_transferState_Get_out =  cassetteMethod.cassetteTransferStateGet(objCommon, strNPWXferCassette.get(i).getCassetteID() );

                if (CimStringUtils.equals(strCassette_transferState_Get_out, BizConstant.SP_TRANSSTATE_EQUIPMENTIN)) {
                    log.info("transferState = SP_TransState_EquipmentIn");
                    /*------------------------------------*/
                    /*   Get Cassette Info in Equipment   */
                    /*------------------------------------*/
                    log.info("Get Cassette Info in Equipment");
                    Outputs.ObjCassetteEquipmentIDGetOut strCassette_equipmentID_Get_out = cassetteMethod.cassetteEquipmentIDGet(objCommon, strNPWXferCassette.get(i).getCassetteID() );

                    Infos.EqpPortInfo eqpPortInfo = equipmentMethod.equipmentPortInfoGet(objCommon,strCassette_equipmentID_Get_out.getEquipmentID());
                    int lenEqpPort = CimArrayUtils.getSize(eqpPortInfo.getEqpPortStatuses());
                    for ( j=0; j < lenEqpPort; j++ ) {
                        if (ObjectIdentifier.equalsWithValue(strNPWXferCassette.get(i).getCassetteID(), eqpPortInfo.getEqpPortStatuses().get(j).getLoadedCassetteID())) {
                            if ( CimStringUtils.equals( eqpPortInfo.getEqpPortStatuses().get(j).getPortState(), BizConstant.SP_PORTRSC_PORTSTATE_UNLOADREQ)
                                    && CimStringUtils.equals( eqpPortInfo.getEqpPortStatuses().get(j).getDispatchState(), BizConstant.SP_PORTRSC_DISPATCHSTATE_DISPATCHED) ) {

                                /*------------------------*/
                                /*   change to Required   */
                                /*------------------------*/
                                log.info("change to Required");
                                equipmentMethod.equipmentDispatchStateChange(objCommon, strCassette_equipmentID_Get_out.getEquipmentID(), eqpPortInfo.getEqpPortStatuses().get(j).getPortID(),
                                        BizConstant.SP_PORTRSC_DISPATCHSTATE_REQUIRED, null, null, null, null );

                            }
                        }
                    }
                }
            }

            /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/
            /*                                                                       */
            /*   Check Process                                                       */
            /*                                                                       */
            /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/
            log.info("Check Process");
            /*-----------------------------------------------------------------------*/
            /*                                                                       */
            /*   Check Process for Cassette                                          */
            /*                                                                       */
            /*   The following conditions are checked by this object                 */
            /*                                                                       */
            /*   - dispatchState should be TRUE.                                     */
            /*   - controlJobID should be blank'.                                    */
            /*                                                                       */
            /*-----------------------------------------------------------------------*/
            log.info("Check Process for Cassette");
            cassetteMethod.cassetteCheckConditionForArrivalCarrierCancel(objCommon, strNPWXferCassette );

            /*-----------------------------------------------------------------------*/
            /*                                                                       */
            /*   Check Process for Lot                                               */
            /*                                                                       */
            /*   The following conditions are checked by this object                 */
            /*   - Omit Empty cassette.                                              */
            /*   - lotProcessState should not be InProcessing.                       */
            /*   - controlJobID should be blank.                                     */
            /*                                                                       */
            /*-----------------------------------------------------------------------*/
            ObjectIdentifier controlJobID = null;

            /*-----------------------------------------------------------*/
            /*   Make strStartCassetteSequence from strNPWXferCassette   */
            /*-----------------------------------------------------------*/
            List<Infos.StartCassette> strStartCassetteSequence = new ArrayList<>();

            for( i = 0 ; i < nStrNPWCassetteLen ; i++ ) {
                Infos.StartCassette startCassette = new Infos.StartCassette();
                startCassette.setLoadSequenceNumber(strNPWXferCassette.get(i).getLoadSequenceNumber());
                startCassette.setCassetteID(strNPWXferCassette.get(i).getCassetteID());
                startCassette.setLoadPurposeType(strNPWXferCassette.get(i).getLoadPurposeType());
                startCassette.setLoadPortID(strNPWXferCassette.get(i).getLoadPortID());
                startCassette.setUnloadPortID(strNPWXferCassette.get(i).getUnloadPortID());
                strStartCassetteSequence.add(startCassette);

            }

            log.info("NPWCarrierReserveCancelReq", "Check Process for Lot" );
            lotMethod.lotCheckConditionForArrivalCarrierCancel(objCommon, controlJobID, strStartCassetteSequence );


            /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/
            /*                                                                       */
            /*   Main Process                                                        */
            /*                                                                       */
            /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/

            /*---------------------------------------------------*/
            /*                                                   */
            /*   Cassette Related Information Update Procedure   */
            /*                                                   */
            /*---------------------------------------------------*/
            log.info("Cassette Related Information Update Procedure");
            for ( i=0 ; i< nStrNPWCassetteLen ; i++ ) {
                log.info("PPTStrNPWCassetteLen : {}",i);
                /*-----------------------------------------------*/
                /*   Change Cassette's Dispatch State to FALSE   */
                /*-----------------------------------------------*/
                log.info("NPWCarrierReserveCancelReq__090", "Change Cassette's Dispatch State to FALSE");
                cassetteMethod.cassetteDispatchStateChange(objCommon ,strNPWXferCassette.get(i).getCassetteID() , false );


                /*--------------------------------------------------*/
                /*   Change Cassette's NPWLoadPurposeType to NULL   */
                /*--------------------------------------------------*/
                log.info("NPWCarrierReserveCancelReq__090, Change Cassette's NPWLoadPurposeType to NULL" );
                cassetteMethod.cassetteSetNPWLoadPurposeType(objCommon, strNPWXferCassette.get(i).getCassetteID(), null );
            }


            /*------------------------------------------------------------------------*/
            /*                                                                        */
            /*   Send NPWCarrierReserveCancelReq() to EAP Procedure                      */
            /*                                                                        */
            /*------------------------------------------------------------------------*/
            if(CimBooleanUtils.isTrue(notifyToTCSFlag) ) {
                log.info("PPTManager_i::txNPWCarrierReserveCancelReq__090", "Send NPWCarrierReserveCancelReq() to EAP Procedure" );
                //'retryCountValue + 1' means first try plus retry count
                //TCSMgr_SendNPWCarrierReserveCancelReq
                Inputs.SendArrivalCarrierNotificationCancelReqIn sendArrivalCarrierNotificationCancelReqIn = new Inputs.SendArrivalCarrierNotificationCancelReqIn();
//                sendArrivalCarrierNotificationCancelReqIn.setObjCommonIn(objCommon);
                sendArrivalCarrierNotificationCancelReqIn.setUser(objCommon.getUser());
                sendArrivalCarrierNotificationCancelReqIn.setEquipmentID(equipmentID);
                sendArrivalCarrierNotificationCancelReqIn.setPortGroupID(portGroupID);
                sendArrivalCarrierNotificationCancelReqIn.setStrNPWXferCassette(strNPWXferCassette);
                sendArrivalCarrierNotificationCancelReqIn.setClaimMemo(claimMemo);

                String tmpSleepTimeValue  = StandardProperties.OM_EAP_CONNECT_SLEEP_TIME.getValue();
                String tmpRetryCountValue = StandardProperties.OM_EAP_CONNECT_RETRY_COUNT.getValue();
                long sleepTimeValue;
                long retryCountValue;

                if (CimStringUtils.length(tmpSleepTimeValue) == 0) {
                    sleepTimeValue = BizConstant.SP_DEFAULT_SLEEP_TIME_TCS;
                } else {
                    sleepTimeValue = CimNumberUtils.intValue(tmpSleepTimeValue);
                }

                if (CimStringUtils.length(tmpRetryCountValue) == 0) {
                    retryCountValue = BizConstant.SP_DEFAULT_RETRY_COUNT_TCS;
                } else {
                    retryCountValue = CimNumberUtils.intValue(tmpRetryCountValue);
                }

                log.debug("{}{}","env value of OM_EAP_CONNECT_SLEEP_TIME  = ",sleepTimeValue);
                log.debug("{}{}","env value of OM_EAP_CONNECT_RETRY_COUNT = ",retryCountValue);

                IEAPRemoteManager eapRemoteManager = eapMethod.eapRemoteManager(objCommon, objCommon.getUser(), equipmentID, null, true);
                if (eapRemoteManager!=null){
                    for(i = 0 ; i < (retryCountValue + 1) ; i++) {
                        try {
                            log.debug("call EAP NPWCarrierReserveCancel");
                            // TODO: 2021/7/14 需要放开
                            //eapRemoteManager.sendNPWCarrierReserveCancelReq(sendArrivalCarrierNotificationCancelReqIn);
                        } catch (ServiceException ex){
                            if (Validations.isEquals(ex.getCode(),retCodeConfig.getExtServiceBindFail())||
                                    Validations.isEquals(ex.getCode(),retCodeConfig.getExtServiceNilObj())||
                                    Validations.isEquals(ex.getCode(),retCodeConfig.getTcsNoResponse())){
                                if (i<retryCountValue){
                                    try {
                                        Thread.sleep(sleepTimeValue);
                                        continue;
                                    } catch (InterruptedException e) {
                                        Thread.currentThread().interrupt();
                                        ex.addSuppressed(e);
                                        throw ex;
                                    }
                                }
                            }
                            throw ex;
                        } catch (Exception ex){
                            if (i<retryCountValue){
                                try {
                                    Thread.sleep(sleepTimeValue);
                                    continue;
                                } catch (InterruptedException e) {
                                    Thread.currentThread().interrupt();
                                    ex.addSuppressed(e);
                                    throw ex;
                                }
                            }
                            throw ex;
                        }
                        break;
                    }
                }
            }
        }
    }

    @Override
    public Results.LotCarrierTOTIReqResult sxLotCarrierTOTIReq(Infos.ObjCommon objCommon, Params.LotCarrierTOTIReqParam params,String claimMemo) {

        //Initialize
        Results.LotCarrierTOTIReqResult out = new Results.LotCarrierTOTIReqResult();
        ObjectIdentifier equipmentID = params.getEquipmentID();
        out.setStrSysMsgStockInfoSeq(new ArrayList<>());

        //check Process
        log.info("Check Transaction ID and equipment Category combination.");
        Boolean doTakeOutInXferFlag = true;
        //【step1】 - equipment_categoryVsTxID_CheckCombination
        equipmentMethod.equipmentCategoryVsTxIDCheckCombination(objCommon, equipmentID);
        //check input parameters
        if (CimBooleanUtils.isTrue(params.getEqpToEqpFlag())){
            if (ObjectIdentifier.isEmptyWithValue(params.getControlJobID())){
                log.error("EqpToEqpFlag == TRUE, but input controlJobID is null... return error.");
                throw new ServiceException(retCodeConfig.getInvalidInputParam());
            }
        }
        if (CimArrayUtils.getSize(params.getStrCarrierXferReqSeq()) == 0
                || CimArrayUtils.getSize(params.getStrEqpTargetPortInfo().getPortGroups()) == 0
                || ObjectIdentifier.isEmptyWithValue(params.getEquipmentID())){
            log.info("input equipmentID or strCarrierXferReqSeq or strEqpTargetPortInfo is null... return errro.");
            throw new ServiceException(retCodeConfig.getInvalidInputParam());
        }
        //check equipment TakeOutIn mode
        log.info("Check Equipment TakeOutIn mode.");
        //【step2】 - equipment_TakeOutInMode_Check
        Long checkEquipmentTakeOutInModeRetCode = equipmentMethod.equipmentTakeOutInModeCheck(objCommon, equipmentID);

        if (CimNumberUtils.longValue(checkEquipmentTakeOutInModeRetCode) == 0){
            log.info("strEquipment_TakeOutInMode_Check_out.eqpTakeOutInSupport == 0");
            throw new ServiceException(new OmCode(retCodeConfigEx.getEqpNotSupportTakeOutInFunc(), equipmentID.getValue()));
        }
        //Pick Up Target Port for TakeOutIn transfer
        //Pick Up Target Port
        //【step3】 - equipment_targetPort_PickupForTakeOutIn
        Infos.EqpTargetPortInfo  equipmentTargetPortPickupForTakeOutInRetCode = equipmentMethod.equipmentTargetPortPickupForTakeOutIn(objCommon, params.getEquipmentID(), params.getStrEqpTargetPortInfo());

        log.info("<<<<< End Block >>>>>");
        //Main Process Start
        // Check Target Port Count
        int lenPortGroup = CimArrayUtils.getSize(equipmentTargetPortPickupForTakeOutInRetCode.getPortGroups());
        if (0 == lenPortGroup){
            doTakeOutInXferFlag = false;
        }
        switch (doTakeOutInXferFlag ? 1:0){
            case 1:{
                ObjectIdentifier dummy = new ObjectIdentifier();
                String tmpAPCIFControlStatus = null;
                //What's Next Section start
                //Get WIP Lots
                Results.WhatNextLotListResult strWhatNextInqResult = new Results.WhatNextLotListResult();
                //Call RTD Interface
                String value = BizConstant.SP_RTD_FUNCTION_CODE_WHATNEXT;
                //【step4】 - cassetteDelivery_RTDInterfaceReq
                Outputs.ObjCassetteDeliveryRTDInterfaceReqOut cassetteDeliveryRTDInterfaceReqOutRetCode = null;
                try{
                    cassetteDeliveryRTDInterfaceReqOutRetCode = cassetteMethod.cassetteDeliveryRTDInterfaceReq(objCommon, value, equipmentID);
                    log.info("Normal End      RTD Interface of txWhatNextInq");
                    strWhatNextInqResult = cassetteDeliveryRTDInterfaceReqOutRetCode.getStrWhatNextInqResult();
                }catch (ServiceException e) {
                    log.info("Faild RTD Interface -----> Call Normal Function (txWhatNextInq)");
                    if (!Validations.isEquals(retCodeConfigEx.getRtdInterfaceSwitchOff(), e.getCode())
                            && !Validations.isEquals(retCodeConfigEx.getNotFoundRTD(), e.getCode())) {
                        //Set System Message
                        StringBuilder msg = new StringBuilder("<<< RTD Interface Error!  (WhatNext of CassetteDelivery) >>>\n");
                        msg.append("\n")
                                .append("Eqpipment ID         : ").append(ObjectIdentifier.fetchValue(equipmentID)).append("\n")
                                .append("-----------------------------------------\n")
                                .append("Transaction ID       : ").append(e.getTransactionID()).append("\n")
                                .append("Return Code          : ").append(e.getCode()).append("\n")
                                .append("Message Text         : ").append(e.getMessage()).append("\n")
                                .append("Reason Text          : ").append(e.getReasonText()).append("\n");
                        Infos.SysMsgStockInfo sysMsg = new Infos.SysMsgStockInfo();
                        sysMsg.setSubSystemID(BizConstant.SP_SUBSYSTEMID_MM);
                        sysMsg.setSystemMessageCode(BizConstant.SP_SYSTEMMSGCODE_RTDERROR);
                        sysMsg.setSystemMessageText(msg.toString());
                        sysMsg.setNotifyFlag(true);
                        sysMsg.setEquipmentID(equipmentID);
                        sysMsg.setEquipmentStatus("");
                        sysMsg.setStockerID(dummy);
                        sysMsg.setStockerStatus("");
                        sysMsg.setAGVID(dummy);
                        sysMsg.setAGVStatus("");
                        sysMsg.setLotID(dummy);
                        sysMsg.setLotStatus("");
                        sysMsg.setRouteID(dummy);
                        sysMsg.setOperationID(dummy);
                        sysMsg.setOperationNumber("");
                        sysMsg.setSystemMessageTimeStamp(CimDateUtils.convertToSpecString(objCommon.getTimeStamp().getReportTimeStamp()));
                        List<Infos.SysMsgStockInfo> sysMstStockInfoList = CimArrayUtils.getSize(out.getStrSysMsgStockInfoSeq()) == 0 ? new ArrayList<>() : out.getStrSysMsgStockInfoSeq();
                        sysMstStockInfoList.add(sysMsg);
                        out.setStrSysMsgStockInfoSeq(sysMstStockInfoList);
                    }
                    //Call Normal Function (txWhatNextInq)
                    log.info(" Call Normal Function (txWhatNextInq)");
                    Params.WhatNextLotListParams whatNextLotListParams = new Params.WhatNextLotListParams();
                    whatNextLotListParams.setEquipmentID(equipmentID);
                    whatNextLotListParams.setSelectCriteria(ObjectIdentifier.buildWithValue(BizConstant.SP_DP_SELECTCRITERIA_AUTO3));
                    //【step5】 - txWhatNextInq__140
                    try {
                        strWhatNextInqResult = dispatchInqService.sxWhatNextLotListInfo(objCommon, whatNextLotListParams);
                    }catch (ServiceException ex){
                        if (!Validations.isEquals(retCodeConfigEx.getNoWipLot(), ex.getCode())){
                            //Set System Message
                            StringBuilder msg = new StringBuilder("<<< WhatNext Error!  (Stocker -> EQP) >>>\n");
                            msg.append("\n")
                                    .append("Eqpipment ID         : ").append(ObjectIdentifier.fetchValue(equipmentID)).append("\n")
                                    .append("-----------------------------------------\n")
                                    .append("Transaction ID       : ").append(ex.getTransactionID()).append("\n")
                                    .append("Return Code          : ").append(ex.getCode()).append("\n")
                                    .append("Message Text         : ").append(ex.getMessage()).append("\n")
                                    .append("Reason Text          : ").append(ex.getReasonText()).append("\n");
                            Infos.SysMsgStockInfo sysMsg = new Infos.SysMsgStockInfo();
                            sysMsg.setSubSystemID(BizConstant.SP_SUBSYSTEMID_MM);
                            sysMsg.setSystemMessageCode(BizConstant.SP_SYSTEMMSGCODE_DELIVERYERROR);
                            sysMsg.setSystemMessageText(msg.toString());
                            sysMsg.setNotifyFlag(true);
                            sysMsg.setEquipmentID(equipmentID);
                            sysMsg.setEquipmentStatus("");
                            sysMsg.setStockerID(dummy);
                            sysMsg.setStockerStatus("");
                            sysMsg.setAGVID(dummy);
                            sysMsg.setAGVStatus("");
                            sysMsg.setLotID(dummy);
                            sysMsg.setLotStatus("");
                            sysMsg.setRouteID(dummy);
                            sysMsg.setOperationID(dummy);
                            sysMsg.setOperationNumber("");
                            sysMsg.setSystemMessageTimeStamp(CimDateUtils.convertToSpecString(objCommon.getTimeStamp().getReportTimeStamp()));
                            List<Infos.SysMsgStockInfo> sysMstStockInfoList = CimArrayUtils.getSize(out.getStrSysMsgStockInfoSeq()) == 0 ? new ArrayList<>() : out.getStrSysMsgStockInfoSeq();
                            sysMstStockInfoList.add(sysMsg);
                            out.setStrSysMsgStockInfoSeq(sysMstStockInfoList);
                        }
                        log.info("txWhatNextInq() != RC_OK");
                        doTakeOutInXferFlag = false;
                        break;
                    }
                }
                List<Infos.PortGroup> strPortGroupSeq = new ArrayList<>();
                strPortGroupSeq.add(equipmentTargetPortPickupForTakeOutInRetCode.getPortGroups().get(0));
                log.info("Select Multiple Lots <<< whatNextLotList_to_StartCassetteForDeliveryReq >>>");
                //Select Multiple Lots
                Results.WhatNextLotListResult convWhatNextInqResult = strWhatNextInqResult;
                //【step6】 - whatNextLotList_to_StartCassetteForTakeOutInDeliveryReq
                Inputs.ObjWhatNextLotListToStartCassetteForTakeOutInDeliveryReqIn input = new Inputs.ObjWhatNextLotListToStartCassetteForTakeOutInDeliveryReqIn();
                input.setEquipmentID(equipmentID);
                input.setStrPortGroup(strPortGroupSeq);
                input.setStrWhatNextInqResult(convWhatNextInqResult);
                List<Infos.StartCassette> strStartCassette = null;
                try {
                    strStartCassette = whatNextMethod.whatNextLotListToStartCassetteForTakeOutInDeliveryReq(objCommon,input);
                }catch (ServiceException ex){
                    log.info("whatNextLotList_to_StartCassetteForDeliveryReq() rc != RC_OK");
                    doTakeOutInXferFlag = false;
                    break;
                }
                /******************************************************************************/
                /********************* What's Next Section end   ******************************/
                /******************************************************************************/

                /******************************************************************************/
                /********************* DOC process Section start ******************************/
                /******************************************************************************/
                long tmpFPCAdoptFlag = StandardProperties.OM_DOC_ENABLE_FLAG.getLongValue();
                String exchangeType = BizConstant.SP_FPC_EXCHANGETYPE_STARTRESERVEINFO;
                if (1 == tmpFPCAdoptFlag){
                    log.info("DOC Adopt Flag is ON. Now apply FPCInfo.");
                    List<Infos.StartCassette> fpcStartCassetteInfoRetCode = null;
                    try {
                        fpcStartCassetteInfoRetCode = fpcMethod.fpcStartCassetteInfoExchange(objCommon,
                                exchangeType,
                                equipmentID,
                                strStartCassette);
                        log.info("FPCStartCassetteInfo_Exchange() == RC_OK");
                        strStartCassette = fpcStartCassetteInfoRetCode;
                    } catch (ServiceException e) {
                        log.info("FPCStartCassetteInfo_Exchange() != RC_OK");
                        doTakeOutInXferFlag = false;
                        break;
                    }
                }else {
                    log.info("DOC Adopt Flag is OFF.");
                }
                /******************************************************************************/
                /********************* DOC process Section end   ******************************/
                /******************************************************************************/

                /******************************************************************************/
                /********************* Sampling process Section start *************************/
                /******************************************************************************/
                log.info("Set processJobExecFlag based on wafer sampling setting. ");
                Boolean notSendIgnoreMail = false;
                OmCode startCassetteProcessJobExecFlagSetRc = retCodeConfig.getSucc();
                //【step8】 - startCassette_processJobExecFlag_Set__090
                Outputs.ObjStartCassetteProcessJobExecFlagSetOut startCassetteProcessJobExecFlagSetOutRetCode = null;
                try {
                    startCassetteProcessJobExecFlagSetOutRetCode = startCassetteMethod.startCassetteProcessJobExecFlagSet(objCommon,
                            strStartCassette,
                            equipmentID);
                }catch (ServiceException ex){
                    startCassetteProcessJobExecFlagSetOutRetCode = ex.getData(Outputs.ObjStartCassetteProcessJobExecFlagSetOut.class);
                    if (!Validations.isEquals(retCodeConfig.getNoSmplSetting(), ex.getCode())){
                        // When errors have occurred, hold all the error lot and send e-mail to notify that.
                        notSendIgnoreMail = true;
                        startCassetteProcessJobExecFlagSetRc = retCodeConfig.getInvalidSmplSetting();
                    }
                }
                int mailLen = CimArrayUtils.getSize(startCassetteProcessJobExecFlagSetOutRetCode.getSamplingMessage());
                //hold lots and send E-Mails if error
                for (int mailCnt = 0; mailCnt < mailLen; mailCnt++) {
                    if (BizConstant.SP_SAMPLING_ERROR_MAIL == startCassetteProcessJobExecFlagSetOutRetCode.getSamplingMessage().get(mailCnt).getMessageType()
                            || BizConstant.SP_SAMPLING_WARN_MAIL == startCassetteProcessJobExecFlagSetOutRetCode.getSamplingMessage().get(mailCnt).getMessageType()){
                        log.info("messageType == SP_Sampling_error_mail || SP_Sampling_warn_mail ");
                        // Lot Hold
                        //【step- 9】 lot_currentOperationInfo_Get
                        Outputs.ObjLotCurrentOperationInfoGetOut lotCurrentOperationInfoRetCode = null;
                        try {
                            lotCurrentOperationInfoRetCode = lotMethod.lotCurrentOperationInfoGet(objCommon, startCassetteProcessJobExecFlagSetOutRetCode.getSamplingMessage().get(mailCnt).getLotID());
                        }catch (ServiceException e){
                            log.info("lot_currentOperationInfo_Get() != RC_OK");
                            doTakeOutInXferFlag = false;
                            break;
                        }
                        //【step10】 txHoldLotReq
                        Infos.LotHoldReq lotHoldReq = new Infos.LotHoldReq();
                        lotHoldReq.setHoldType(BizConstant.SP_HOLDTYPE_LOTHOLD);
                        lotHoldReq.setHoldReasonCodeID(new ObjectIdentifier(BizConstant.SP_REASON_WAFERSAMPLINGHOLD));
                        lotHoldReq.setHoldUserID(objCommon.getUser().getUserID());
                        lotHoldReq.setResponsibleOperationMark(BizConstant.SP_RESPONSIBLEOPERATION_CURRENT);
                        lotHoldReq.setRouteID(lotCurrentOperationInfoRetCode.getRouteID());
                        lotHoldReq.setOperationNumber(lotCurrentOperationInfoRetCode.getOperationNumber());
                        lotHoldReq.setClaimMemo("");
                        List<Infos.LotHoldReq> holdReqList = new ArrayList<Infos.LotHoldReq>();
                        holdReqList.add(lotHoldReq);
                        try {
                            lotService.sxHoldLotReq(objCommon, startCassetteProcessJobExecFlagSetOutRetCode.getSamplingMessage().get(mailCnt).getLotID(), holdReqList);
                        } catch (ServiceException e) {
                            doTakeOutInXferFlag = false;
                            break;
                        }
                    }
                    //error Mail Set
                    if (CimBooleanUtils.isFalse(notSendIgnoreMail) || !(BizConstant.SP_SAMPLING_IGNORED_MAIL == startCassetteProcessJobExecFlagSetOutRetCode.getSamplingMessage().get(mailCnt).getMessageType())){
                        log.info("notSendIgnoreMail == FALSE ||  strStartCassette_processJobExecFlag_Set_out__090.strSamplingMessage[mailCnt].messageType != SP_Sampling_ignored_mail");
                        //When notSendIgnoreMail == TRUE, just the Lot Hold mail is to be sent.
                        //【step11】 - lot_samplingMessage_Create
                        String messageText = "";
                        if (BizConstant.SP_SAMPLING_WARN_MAIL == startCassetteProcessJobExecFlagSetOutRetCode.getSamplingMessage().get(mailCnt).getMessageType()){
                            //-------------------------------------------------------------------
                            // create mail text if message type == SP_Sampling_warn_mail;
                            // startCassette_processJobExecFlag_Set__090 returns just error text. Caller should switch text handling.
                            //-------------------------------------------------------------------
                            try {
                                messageText = lotMethod.lotSamplingMessageCreate(objCommon,
                                        startCassetteProcessJobExecFlagSetOutRetCode.getSamplingMessage().get(mailCnt).getLotID(),
                                        BizConstant.SP_SAMPLING_ERROR_MAIL,
                                        startCassetteProcessJobExecFlagSetOutRetCode.getSamplingMessage().get(mailCnt).getMessageText());
                            }catch(ServiceException ex){
                                log.info("lot_samplingMessage_Create() != RC_OK");
                                doTakeOutInXferFlag = false;
                                break;
                            }
                        }else {
                            messageText = startCassetteProcessJobExecFlagSetOutRetCode.getSamplingMessage().get(mailCnt).getMessageText();
                        }
                        //Set System Message
                        Infos.SysMsgStockInfo sysMsg = new Infos.SysMsgStockInfo();
                        sysMsg.setSubSystemID(BizConstant.SP_SUBSYSTEMID_MM);
                        sysMsg.setSystemMessageCode(BizConstant.SP_SYSTEMMSGCODE_SMPLERR);
                        sysMsg.setSystemMessageText(messageText);
                        sysMsg.setNotifyFlag(true);
                        sysMsg.setEquipmentID(equipmentID);
                        sysMsg.setEquipmentStatus("");
                        sysMsg.setStockerID(dummy);
                        sysMsg.setStockerStatus("");
                        sysMsg.setAGVID(dummy);
                        sysMsg.setAGVStatus("");
                        sysMsg.setLotID(startCassetteProcessJobExecFlagSetOutRetCode.getSamplingMessage().get(mailCnt).getLotID());
                        sysMsg.setLotStatus("");
                        sysMsg.setRouteID(dummy);
                        sysMsg.setOperationID(dummy);
                        sysMsg.setOperationNumber("");
                        sysMsg.setSystemMessageTimeStamp(CimDateUtils.convertToSpecString(objCommon.getTimeStamp().getReportTimeStamp()));
                        List<Infos.SysMsgStockInfo> sysMstStockInfoList = CimArrayUtils.getSize(out.getStrSysMsgStockInfoSeq()) == 0 ? new ArrayList<>() : out.getStrSysMsgStockInfoSeq();
                        sysMstStockInfoList.add(sysMsg);
                        out.setStrSysMsgStockInfoSeq(sysMstStockInfoList);
                    }
                }
                if (CimBooleanUtils.isFalse(doTakeOutInXferFlag)){
                    break;
                }
                if (CimBooleanUtils.isTrue(notSendIgnoreMail)){
                    log.info("notSendIgnoreMail == TRUE  return this TX.{}",startCassetteProcessJobExecFlagSetRc);
                    doTakeOutInXferFlag = false;
                    break;
                }
                //valid check of processJobExecFlag for each lot.
                //Check every lot has at least one wafer with processJobExecFlag == TRUE
                //【step12】 - lot_processJobExecFlag_ValidCheck
                Outputs.ObjLotProcessJobExecFlagValidCheckOut lotProcessJobExecFlagValidCheckOut = null;
                try {
                    lotProcessJobExecFlagValidCheckOut = lotMethod.lotProcessJobExecFlagValidCheck(objCommon, startCassetteProcessJobExecFlagSetOutRetCode.getStartCassettes());
                } catch (ServiceException e) {
                    lotProcessJobExecFlagValidCheckOut = e.getData(Outputs.ObjLotProcessJobExecFlagValidCheckOut.class);
                    if (!Validations.isEquals(retCodeConfig.getNoProcessJobExecFlag(),e.getCode())) {
                        log.info("lot_processJobExecFlag_ValidCheck() != RC_OK && != RC_NO_PROCESSJOBEXECFLAG");
                        doTakeOutInXferFlag = false;
                        break;
                    }
                    if (Validations.isEquals(retCodeConfig.getNoProcessJobExecFlag(),e.getCode())) {
                        log.info("objLot_processJobExecFlag_ValidCheck() rc == RC_NO_PROCESSJOBEXECFLAG");
                        // Lot Hold + error mail
                        // create hold mail message
                        //【step13】 - lot_samplingMessage_Create
                        String message = null;
                        try {
                            message = lotMethod.lotSamplingMessageCreate(objCommon,
                                    lotProcessJobExecFlagValidCheckOut.getLotID(),
                                    BizConstant.SP_SAMPLING_ERROR_MAIL,
                                    e.getMessage());
                        } catch (ServiceException ex) {
                            log.info("lot_samplingMessage_Create() != RC_OK");
                            doTakeOutInXferFlag = false;
                            break;
                        }

                        //【step14】 - lot_currentOperationInfo_Get
                        Outputs.ObjLotCurrentOperationInfoGetOut lotCurrentOperationInfoRetCode = null;
                        try {
                            lotCurrentOperationInfoRetCode = lotMethod.lotCurrentOperationInfoGet(objCommon, lotProcessJobExecFlagValidCheckOut.getLotID());
                        } catch (ServiceException ex) {
                            log.info("lot_currentOperationInfo_Get() != RC_OK");
                            doTakeOutInXferFlag = false;
                            break;
                        }
                        Infos.LotHoldReq lotHoldReq = new Infos.LotHoldReq();
                        lotHoldReq.setHoldType(BizConstant.SP_HOLDTYPE_LOTHOLD);
                        lotHoldReq.setHoldReasonCodeID(new ObjectIdentifier(BizConstant.SP_REASON_WAFERSAMPLINGHOLD));
                        lotHoldReq.setHoldUserID(objCommon.getUser().getUserID());
                        lotHoldReq.setResponsibleOperationMark(BizConstant.SP_RESPONSIBLEOPERATION_CURRENT);
                        lotHoldReq.setRouteID(lotCurrentOperationInfoRetCode.getRouteID());
                        lotHoldReq.setOperationNumber(lotCurrentOperationInfoRetCode.getOperationNumber());
                        lotHoldReq.setClaimMemo("");
                        List<Infos.LotHoldReq> holdReqList = new ArrayList<Infos.LotHoldReq>();
                        holdReqList.add(lotHoldReq);
                        //【step15】 - txHoldLotReq
                        try {
                            lotService.sxHoldLotReq(objCommon, lotProcessJobExecFlagValidCheckOut.getLotID(), holdReqList);
                        } catch (ServiceException ex) {
                            log.info("txHoldLotReq() != RC_OK");
                            doTakeOutInXferFlag = false;
                            break;
                        }
                        Infos.SysMsgStockInfo sysMsg = new Infos.SysMsgStockInfo();
                        sysMsg.setSubSystemID(BizConstant.SP_SUBSYSTEMID_MM);
                        sysMsg.setSystemMessageCode(BizConstant.SP_SYSTEMMSGCODE_SMPLERR);
                        sysMsg.setSystemMessageText(message);
                        sysMsg.setNotifyFlag(true);
                        sysMsg.setEquipmentID(equipmentID);
                        sysMsg.setEquipmentStatus("");
                        sysMsg.setStockerID(dummy);
                        sysMsg.setStockerStatus("");
                        sysMsg.setAGVID(dummy);
                        sysMsg.setAGVStatus("");
                        sysMsg.setLotID(lotProcessJobExecFlagValidCheckOut.getLotID());
                        sysMsg.setLotStatus("");
                        sysMsg.setRouteID(dummy);
                        sysMsg.setOperationID(dummy);
                        sysMsg.setOperationNumber("");
                        sysMsg.setSystemMessageTimeStamp(CimDateUtils.convertToSpecString(objCommon.getTimeStamp().getReportTimeStamp()));
                        List<Infos.SysMsgStockInfo> sysMstStockInfoList = CimArrayUtils.getSize(out.getStrSysMsgStockInfoSeq()) == 0 ? new ArrayList<>() : out.getStrSysMsgStockInfoSeq();
                        sysMstStockInfoList.add(sysMsg);
                        out.setStrSysMsgStockInfoSeq(sysMstStockInfoList);
                        doTakeOutInXferFlag = false;
                        break;
                    }
                }

                strStartCassette = startCassetteProcessJobExecFlagSetOutRetCode.getStartCassettes();
                log.info("Set processJobExecFlag based on wafer sampling setting end. ");
                /******************************************************************************/
                /********************* Sampling process Section end ***************************/
                /******************************************************************************/
                /******************************************************************************/
                /********************* StartReservation process Section start *****************/
                /******************************************************************************/
                /*--------------------------------------*/
                /*   Request to Start Lot Reservation   */
                /*--------------------------------------*/
                ObjectIdentifier dummyCJID = new ObjectIdentifier();
                //【step16】 - txMoveInReserveForTOTIReq
                Params.MoveInReserveForTOTIReqInParam in = new Params.MoveInReserveForTOTIReqInParam();
                in.setEquipmentID(equipmentID);
                in.setPortGroupID(strPortGroupSeq.get(0).getPortGroup());
                in.setControlJobID(dummyCJID);
                in.setStrStartCassetteSequence(strStartCassette);
                Infos.ObjCommon strTmpObjCommonin = objCommon;
                strTmpObjCommonin.setTransactionID(TransactionIDEnum.START_LOTS_RESERVATION_FOR_TAKE_OUT_IN_REQ.getValue());
                Results.MoveInReserveForTOTIReqResult moveInReserveForTOTIReqResultRetCode = null;
                try {
                    moveInReserveForTOTIReqResultRetCode = dispatchService.sxMoveInReserveForTOTIReq(strTmpObjCommonin, in, "");
                } catch (ServiceException e) {
                    log.info("txMoveInReserveReq() rc != RC_OK");
                    //---------------------------------------------------------------------------------------------
                    //  Prepare message text string for case of failure of start lot reservation
                    //  Only if start lot resercatioin for current equipment is fail, this messageText is filled
                    //-----r----------------------------------------------------------------------------------------
                    //-------------------------------------------------------
                    // Prepare e-mail Message Text
                    //-------------------------------------------------------
                    StringBuilder msg = new StringBuilder("<<< MoveInReservation Error!  (Stocker -> EQP) >>>\n");
                    msg.append("\n").append("Eqpipment ID         : ").append(ObjectIdentifier.fetchValue(equipmentID)).append("\n")
                            .append("Lot IDs              :");
                    Boolean bFirstSet = false;
                    int nCasLen = CimArrayUtils.getSize(strStartCassette);
                    for (int i = 0; i < nCasLen; i++){
                        List<Infos.LotInCassette> lotInCassetteList = strStartCassette.get(i).getLotInCassetteList();
                        int nLotLen = CimArrayUtils.getSize(lotInCassetteList);
                        for (int j = 0; j < nLotLen; j++){
                            Infos.LotInCassette lotInCassette = lotInCassetteList.get(j);
                            if (!ObjectIdentifier.isEmptyWithValue(lotInCassette.getLotID())){
                                if (bFirstSet){
                                    msg.append(", ");
                                }
                                msg.append(lotInCassette.getLotID().getValue());
                                bFirstSet = true;
                            }
                        }
                    }
                    msg.append("\n")
                            .append("Transaction ID       : ").append(strTmpObjCommonin.getTransactionID()).append("\n")
                            .append("Return Code          : ").append(e.getCode()).append("\n")
                            .append("Message ID           : ")
                            .append("\n")
                            .append("Message Text         : ").append(e.getMessage()).append("\n")
                            .append("Reason Text          : ").append(e.getReasonText()).append("\n");

                    Infos.SysMsgStockInfo sysMsg = new Infos.SysMsgStockInfo();
                    sysMsg.setSubSystemID(BizConstant.SP_SUBSYSTEMID_MM);
                    sysMsg.setSystemMessageCode(BizConstant.SP_SYSTEMMSGCODE_DELIVERYERROR);
                    sysMsg.setSystemMessageText(msg.toString());
                    sysMsg.setNotifyFlag(true);
                    sysMsg.setEquipmentID(equipmentID);
                    sysMsg.setEquipmentStatus("");
                    sysMsg.setStockerID(dummy);
                    sysMsg.setStockerStatus("");
                    sysMsg.setAGVID(dummy);
                    sysMsg.setAGVStatus("");
                    sysMsg.setLotID(dummy);
                    sysMsg.setLotStatus("");
                    sysMsg.setRouteID(dummy);
                    sysMsg.setOperationID(dummy);
                    sysMsg.setOperationNumber("");
                    sysMsg.setSystemMessageTimeStamp(CimDateUtils.convertToSpecString(objCommon.getTimeStamp().getReportTimeStamp()));
                    List<Infos.SysMsgStockInfo> sysMstStockInfoList = CimArrayUtils.getSize(out.getStrSysMsgStockInfoSeq()) == 0 ? new ArrayList<>() : out.getStrSysMsgStockInfoSeq();
                    sysMstStockInfoList.add(sysMsg);
                    out.setStrSysMsgStockInfoSeq(sysMstStockInfoList);
                    doTakeOutInXferFlag = false;
                    break;
                }
                /******************************************************************************/
                /********************* StartReservation process Section end *******************/
                /******************************************************************************/


                /******************************************************************************/
                /********************* Carrier Xfer info generate Section start ***************/
                /******************************************************************************/
                /*------------------------------------------*/
                /*   Make txMultipleCarrierTransferReq parameter   */
                /*------------------------------------------*/
                //【step17】 - multiCarrierXferFillInOTMSW005InParm
                Outputs.ObjMultiCarrierXferFillInOTMSW005InParmOut multiCarrierXferFillInOTMSW005InParmOutRetCode = null;
                try{
                    multiCarrierXferFillInOTMSW005InParmOutRetCode = cassetteMethod.multiCarrierXferFillInOTMSW005InParm(objCommon,
                            equipmentID, strStartCassette);
                } catch (ServiceException e) {
                    /*---------------------------------------------*/
                    /*   Request to Start Lot Reservation Cancel   */
                    /*---------------------------------------------*/
                    Params.MoveInReserveCancelReqParams parm = new Params.MoveInReserveCancelReqParams();
                    parm.setEquipmentID(equipmentID);
                    parm.setControlJobID(moveInReserveForTOTIReqResultRetCode.getControlJobID());
                    //【step18】 - txMoveInReserveCancelReq
                    try {
                        Results.MoveInReserveCancelReqResult moveInReserveCancelReqResultRetCode = dispatchService.sxMoveInReserveCancelReqService(objCommon, parm);
                    } catch (ServiceException ex) {
                        log.info("txMoveInReserveCancelReq != RC_OK");
                    }
                    doTakeOutInXferFlag = false;
                    break;
                }
                /******************************************************************************/
                /********************* Carrier Xfer info generate Section end *****************/
                /******************************************************************************/


                /******************************************************************************/
                /********************* Carrier Xfer request Section start *********************/
                /******************************************************************************/
                int takeInXferLen = CimArrayUtils.getSize(multiCarrierXferFillInOTMSW005InParmOutRetCode.getStrCarrierXferReq());
                int takeOutXferLen = CimArrayUtils.getSize(params.getStrCarrierXferReqSeq());
                List<Infos.CarrierXferReq> mixedCarrierXferSeq = new ArrayList<>(params.getStrCarrierXferReqSeq());
                int mixedCnt= takeOutXferLen;
                for (int takeInCnt = 0; takeInCnt < takeInXferLen; takeInCnt++) {
                    mixedCarrierXferSeq.add(mixedCnt,multiCarrierXferFillInOTMSW005InParmOutRetCode.getStrCarrierXferReq().get(takeInCnt));
                    mixedCnt++;
                }
                Boolean bReRouteFlag = false;
                String reRouteXferFlag = StandardProperties.OM_XFER_REROUTE_FLAG.getValue();
                if (CimStringUtils.equals(reRouteXferFlag,"1")){
                    log.info("reRouteXferFlag is 1");
                    bReRouteFlag = true;
                }
                /*---------------------------------------------------*/
                /*   Send Transfer Request to XM. (Stocker -> EQP)   */
                /*---------------------------------------------------*/
                //【step19】 - txMultipleCarrierTransferReq
                try {
                    this.sxMultipleCarrierTransferReq(objCommon,
                            bReRouteFlag,
                            "L",
                            mixedCarrierXferSeq);
                } catch (ServiceException e) {
                    log.info("txMultipleCarrierTransferReq() rc != RC_OK");
                    //Prepare e-mail Message Text ignore
                    StringBuilder msg = new StringBuilder("<<< Transfer Error!  (Stocker -> EQP) >>>\n");
                    msg.append("\n")
                            .append("Eqpipment ID         : ").append(ObjectIdentifier.fetchValue(equipmentID)).append("\n");
                    for (int i = 0; i < CimArrayUtils.getSize(multiCarrierXferFillInOTMSW005InParmOutRetCode.getStrCarrierXferReq()); i++) {
                        Infos.CarrierXferReq carrierXferReq = multiCarrierXferFillInOTMSW005InParmOutRetCode.getStrCarrierXferReq().get(i);
                        msg.append("-----------------------------------------\n")
                                .append("carrier ID           : ").append(ObjectIdentifier.fetchValue(carrierXferReq.getCarrierID())).append("\n")
                                .append("Lot ID               : ").append(ObjectIdentifier.fetchValue(carrierXferReq.getLotID())).append("\n")
                                .append("From Machine ID      : ").append(ObjectIdentifier.fetchValue(carrierXferReq.getFromMachineID())).append("\n")
                                .append("From Port ID         : ").append(ObjectIdentifier.fetchValue(carrierXferReq.getFromPortID())).append("\n")
                                .append("To Stocker Group     : ").append(carrierXferReq.getToStockerGroup()).append("\n");
                        int toMachineLen = CimArrayUtils.getSize(carrierXferReq.getStrToMachine());
                        if (toMachineLen == 0){
                            msg.append("To Machine ID        : Nothing\n");
                        }
                        for (int j = 0; j < toMachineLen; j++) {
                            msg.append("To Machine ID        : ").append(ObjectIdentifier.fetchValue(carrierXferReq.getStrToMachine().get(j).getToMachineID())).append("\n")
                                    .append("To Port ID           : ").append(ObjectIdentifier.fetchValue(carrierXferReq.getStrToMachine().get(j).getToPortID())).append("\n");
                        }
                    }
                    msg.append("-----------------------------------------\n")
                            .append("Transaction ID       : ").append(objCommon.getTransactionID()).append("\n")
                            .append("Return Code          : ").append(e.getCode()).append("\n")
                            .append("Message Text         : ").append(e.getMessage()).append("\n")
                            .append("Reason Text          : ").append(e.getReasonText()).append("\n");
                    Infos.SysMsgStockInfo sysMsg = new Infos.SysMsgStockInfo();
                    sysMsg.setSubSystemID(BizConstant.SP_SUBSYSTEMID_MM);
                    sysMsg.setSystemMessageCode(BizConstant.SP_SYSTEMMSGCODE_DELIVERYERROR);
                    sysMsg.setSystemMessageText(msg.toString());
                    sysMsg.setNotifyFlag(true);
                    sysMsg.setEquipmentID(equipmentID);
                    sysMsg.setEquipmentStatus("");
                    sysMsg.setStockerID(dummy);
                    sysMsg.setStockerStatus("");
                    sysMsg.setAGVID(dummy);
                    sysMsg.setAGVStatus("");
                    sysMsg.setLotID(dummy);
                    sysMsg.setLotStatus("");
                    sysMsg.setRouteID(dummy);
                    sysMsg.setOperationID(dummy);
                    sysMsg.setOperationNumber("");
                    sysMsg.setSystemMessageTimeStamp(CimDateUtils.convertToSpecString(objCommon.getTimeStamp().getReportTimeStamp()));
                    List<Infos.SysMsgStockInfo> sysMstStockInfoList = CimArrayUtils.getSize(out.getStrSysMsgStockInfoSeq()) == 0 ? new ArrayList<>() : out.getStrSysMsgStockInfoSeq();
                    sysMstStockInfoList.add(sysMsg);
                    out.setStrSysMsgStockInfoSeq(sysMstStockInfoList);
                    /*---------------------------------------------*/
                    /*   Request to Start Lot Reservation Cancel   */
                    /*---------------------------------------------*/
                    //【step20】 - txMoveInReserveCancelReq
                    Params.MoveInReserveCancelReqParams lotsReservationCancelReqParams = new Params.MoveInReserveCancelReqParams();
                    lotsReservationCancelReqParams.setEquipmentID(equipmentID);
                    lotsReservationCancelReqParams.setControlJobID(moveInReserveForTOTIReqResultRetCode.getControlJobID());
                    try {
                        Results.MoveInReserveCancelReqResult moveInReserveCancelReqResultRetCode = dispatchService.sxMoveInReserveCancelReqService(objCommon, lotsReservationCancelReqParams);
                    } catch (ServiceException ex) {
                        log.info("txMoveInReserveCancelReq != RC_OK");
                    }
                    doTakeOutInXferFlag = false;
                    break;
                }
                /******************************************************************************/
                /********************* Carrier Xfer request Section end ***********************/
                /******************************************************************************/

                /*--------------------*/
                /*   End of Process   */
                /*--------------------*/
                break;
            }
            case 0:{
                //do nothing...
                log.info("doTakeOutInXferFlag == FALSE.");
                break;
            }
            default:
                break;
        }
        /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/
        /*                                                                       */
        /*   Main Process end                                                    */
        /*                                                                       */
        /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/
        /*------------------------------------------------------------------------------------------*/
        /*  If TakeOutIn xfer can not be done by some reasons, do Carrier Xfer only input XferReq   */
        /*------------------------------------------------------------------------------------------*/
        if (CimBooleanUtils.isFalse(doTakeOutInXferFlag)){
            Boolean okFlag = true;
            RetCode retCode = new RetCode(objCommon.getTransactionID(), retCodeConfig.getSucc(), null);
            /*-----------------------------------------------------------------*/
            /*   Send Transfer Request to XM. (EQP -> Stocker or EQP -> EQP)   */
            /*-----------------------------------------------------------------*/
            boolean bReRouteFlag= false;
            String reRouteXferFlag = StandardProperties.OM_XFER_REROUTE_FLAG.getValue();
            if (CimStringUtils.equals(reRouteXferFlag,"1")){
                log.info("reRouteXferFlag is 1");
                bReRouteFlag = true;
            }
            int xferSeqLen = CimArrayUtils.getSize(params.getStrCarrierXferReqSeq());
            if (xferSeqLen == 0){
                //Do nothing...
                return out;
            }else if (CimBooleanUtils.isTrue(params.getEqpToEqpFlag())){
                log.info("EqpToEqp transfer. Call txMultipleCarrierTransferReq()");
                String transportType = "";
                if (xferSeqLen > 1 ){
                    transportType = "B";
                }else {
                    transportType = "S";
                }
                //【step21】 - txMultipleCarrierTransferReq
                try {
                    this.sxMultipleCarrierTransferReq(objCommon,
                            bReRouteFlag,
                            transportType,
                            params.getStrCarrierXferReqSeq());
                } catch (ServiceException e) {
                    okFlag = false;
                    retCode = new RetCode(e.getTransactionID(), new OmCode(e.getCode(), e.getMessage()), e.getReasonText());
                    log.info("txMultipleCarrierTransferReq() rc != RC_OK");
                }
            }else {
                log.info("transfer to Stocker. Call txSingleCarrierTransferReq()");
                for (int i = 0; i < xferSeqLen; i++) {
                    /*------------------------------------------------------*/
                    /*  Check whether specified machine is eqp or stocker   */
                    /*------------------------------------------------------*/
                    Boolean isStorageFlag = false;
                    //【step22】 - equipment_statusInfo_Get__090
                    Infos.EqpStatusInfo eqpStatusInfoRetCode = null;
                    try {
                        eqpStatusInfoRetCode = equipmentMethod.equipmentStatusInfoGet(objCommon, params.getStrCarrierXferReqSeq().get(i).getFromMachineID());
                    } catch (ServiceException e) {
                        if (Validations.isEquals(retCodeConfig.getNotFoundEqp(), e.getCode())){
                            isStorageFlag = true;
                        } else {
                            log.error("equipment_statusInfo_Get__090() rc != RC_OK");
                            e.setData(out);
                            throw e;
                        }
                    }
                    if (CimBooleanUtils.isFalse(isStorageFlag)){
                        /*--------------------------------------------*/
                        /* Lock Port object of machine                */
                        /*--------------------------------------------*/
                        //step23 - object_LockForEquipmentResource
                        objectLockMethod.objectLockForEquipmentResource(objCommon, params.getStrCarrierXferReqSeq().get(i).getFromMachineID(), params.getStrCarrierXferReqSeq().get(i).getFromPortID(), BizConstant.SP_CLASSNAME_POSPORTRESOURCE);
                        /*-----------------------------------------------------------------------------*/
                        /*                                                                             */
                        /*   Check Equipment Port for Sending Request to XM. (EQP -> Stocker)          */
                        /*                                                                             */
                        /*-----------------------------------------------------------------------------*/
                        //【step24】 - equipment_portState_CheckForCassetteDelivery
                        Inputs.ObjEquipmentPortStateCheckForCassetteDeliveryIn deliveryIn = new Inputs.ObjEquipmentPortStateCheckForCassetteDeliveryIn();
                        deliveryIn.setEquipmentID(params.getEquipmentID());
                        deliveryIn.setPortID(params.getStrCarrierXferReqSeq().get(i).getFromPortID());
                        equipmentMethod.equipmentPortStateCheckForCassetteDelivery(objCommon,deliveryIn);
                    }
                    //【step25】 - txSingleCarrierTransferReq
                    Params.SingleCarrierTransferReqParam singleCarrierTransferReqParam = new Params.SingleCarrierTransferReqParam();
                    singleCarrierTransferReqParam.setRerouteFlag(bReRouteFlag);
                    Infos.CarrierXferReq strCarrierXferReq = params.getStrCarrierXferReqSeq().get(i);
                    singleCarrierTransferReqParam.setCarrierID(strCarrierXferReq.getCarrierID());
                    singleCarrierTransferReqParam.setLotID(strCarrierXferReq.getLotID());
                    singleCarrierTransferReqParam.setZoneType(strCarrierXferReq.getZoneType());
                    singleCarrierTransferReqParam.setN2PurgeFlag(strCarrierXferReq.getN2PurgeFlag());
                    singleCarrierTransferReqParam.setFromMachineID(strCarrierXferReq.getFromMachineID());
                    singleCarrierTransferReqParam.setFromPortID(strCarrierXferReq.getFromPortID());
                    singleCarrierTransferReqParam.setToStockerGroup(strCarrierXferReq.getToStockerGroup());
                    singleCarrierTransferReqParam.setToMachine(strCarrierXferReq.getStrToMachine());
                    singleCarrierTransferReqParam.setExpectedStartTime(strCarrierXferReq.getExpectedStartTime());
                    singleCarrierTransferReqParam.setExpectedEndTime(strCarrierXferReq.getExpectedEndTime());
                    singleCarrierTransferReqParam.setMandatoryFlag(strCarrierXferReq.getMandatoryFlag());
                    singleCarrierTransferReqParam.setPriority(strCarrierXferReq.getPriority());
                    try {
                        Results.SingleCarrierTransferReqResult singleCarrierTransferReqResultRetCode = this.sxSingleCarrierTransferReq(objCommon, singleCarrierTransferReqParam);
                    } catch (ServiceException e) {
                        okFlag = false;
                        retCode = new RetCode(e.getTransactionID(), new OmCode(e.getCode(), e.getMessage()), e.getReasonText());
                        break;
                    }
                }
            }
            if (CimBooleanUtils.isFalse(okFlag)){
                //Prepare e-mail Message Text ingonore
                StringBuilder msg = new StringBuilder();
                ObjectIdentifier machineID = new ObjectIdentifier();
                if (CimBooleanUtils.isTrue(params.getEqpToEqpFlag())){
                    msg.append("<<< Transfer Error!  (EQP -> EQP) >>>\n");
                    //--------------------------------------
                    // Get controlJob related equipmentID
                    //--------------------------------------
                    //【step26】 - controlJob_startReserveInformation_Ge
                    Outputs.ObjControlJobStartReserveInformationOut controlJobStartReserveInformation =
                            controlJobMethod.controlJobStartReserveInformationGet(objCommon,
                                    params.getControlJobID(),
                                    false);
                    machineID = controlJobStartReserveInformation.getEquipmentID();
                    log.info("ToEquipmentID = {}",machineID.getValue());
                }else {
                    msg.append("<<< Transfer Error!  (EQP -> Stocker) >>>\n");
                }
                int lenCarrierXfer = CimArrayUtils.getSize(params.getStrCarrierXferReqSeq());
                //prepare msg
                msg.append("\n")
                        .append("Eqpipment ID         : ").append(ObjectIdentifier.fetchValue(equipmentID)).append("\n");
                for (int i = 0; i < CimArrayUtils.getSize(params.getStrCarrierXferReqSeq()); i++) {
                    Infos.CarrierXferReq carrierXferReq = params.getStrCarrierXferReqSeq().get(i);
                    msg.append("-----------------------------------------\n")
                            .append("carrier ID           : ").append(ObjectIdentifier.fetchValue(carrierXferReq.getCarrierID())).append("\n")
                            .append("Lot ID               : ").append(ObjectIdentifier.fetchValue(carrierXferReq.getLotID())).append("\n")
                            .append("From Machine ID      : ").append(ObjectIdentifier.fetchValue(carrierXferReq.getFromMachineID())).append("\n")
                            .append("From Port ID         : ").append(ObjectIdentifier.fetchValue(carrierXferReq.getFromPortID())).append("\n")
                            .append("To Stocker Group     : ").append(carrierXferReq.getToStockerGroup()).append("\n");
                    int toMachineLen = CimArrayUtils.getSize(carrierXferReq.getStrToMachine());
                    if (toMachineLen == 0){
                        msg.append("To Machine ID        : Nothing\n");
                    }
                    for (int j = 0; j < toMachineLen; j++) {
                        msg.append("To Machine ID        : ").append(ObjectIdentifier.fetchValue(carrierXferReq.getStrToMachine().get(j).getToMachineID())).append("\n")
                                .append("To Port ID           : ").append(ObjectIdentifier.fetchValue(carrierXferReq.getStrToMachine().get(j).getToPortID())).append("\n");
                    }
                }
                msg.append("-----------------------------------------\n")
                        .append("Transaction ID       : ").append(objCommon.getTransactionID()).append("\n")
                        .append("Return Code          : ").append(retCode.getReturnCode().getCode()).append("\n")
                        .append("Message Text         : ").append(retCode.getReturnCode().getMessage()).append("\n")
                        .append("Reason Text          : ").append(retCode.getReasonText()).append("\n");
                /*------------------------*/
                /*   Set System Message   */
                /*------------------------*/
                ObjectIdentifier dummy = new ObjectIdentifier();
                Infos.SysMsgStockInfo sysMsg = new Infos.SysMsgStockInfo();
                sysMsg.setSubSystemID(BizConstant.SP_SUBSYSTEMID_MM);
                sysMsg.setSystemMessageCode(BizConstant.SP_SYSTEMMSGCODE_DELIVERYERROR);
                sysMsg.setSystemMessageText(msg.toString());
                sysMsg.setNotifyFlag(true);
                sysMsg.setEquipmentID(equipmentID);
                sysMsg.setEquipmentStatus("");
                sysMsg.setStockerID(dummy);
                sysMsg.setStockerStatus("");
                sysMsg.setAGVID(dummy);
                sysMsg.setAGVStatus("");
                sysMsg.setLotID(dummy);
                sysMsg.setLotStatus("");
                sysMsg.setRouteID(dummy);
                sysMsg.setOperationID(dummy);
                sysMsg.setOperationNumber("");
                sysMsg.setSystemMessageTimeStamp(CimDateUtils.convertToSpecString(objCommon.getTimeStamp().getReportTimeStamp()));
                List<Infos.SysMsgStockInfo> sysMstStockInfoList = CimArrayUtils.getSize(out.getStrSysMsgStockInfoSeq()) == 0 ? new ArrayList<>() : out.getStrSysMsgStockInfoSeq();
                sysMstStockInfoList.add(sysMsg);
                out.setStrSysMsgStockInfoSeq(sysMstStockInfoList);
                /*---------------------------------------------------------------------------*/
                /*   If this delivery request is for eqpToEqp, cancel Start reservation.     */
                /*---------------------------------------------------------------------------*/
                if (CimBooleanUtils.isTrue(params.getEqpToEqpFlag())){
                    //【step27】 - txMoveInReserveCancelReq
                    Params.MoveInReserveCancelReqParams in = new Params.MoveInReserveCancelReqParams();
                    in.setEquipmentID(machineID);
                    in.setControlJobID(params.getControlJobID());
                    try {
                        Results.MoveInReserveCancelReqResult moveInReserveCancelReqResultRetCode = dispatchService.sxMoveInReserveCancelReqService(objCommon, in);
                    } catch (ServiceException e) {
                        log.info("txMoveInReserveCancelReq != RC_OK");
                    }
                }
                throw new ServiceException(new OmCode(retCode.getReturnCode(),retCode.getMessageText()),out);
            }
        }
        /*--------------------*/
        /*                    */
        /*   Return to Main   */
        /*                    */
        /*--------------------*/
        return out;
    }

    @Override
    public Results.CarrierTransferReqResult sxDmsTransferReq(Infos.ObjCommon objCommonIn, ObjectIdentifier equipmentID)  {
        //Initialize
        Results.CarrierTransferReqResult out = new Results.CarrierTransferReqResult();
        out.setSysMstStockInfoList(new ArrayList<>());

        //Check Process
        //step1 - equipmentSpecialControlVsTxIDCheckCombination
        log.info("Step1 - equipmentSpecialControlVsTxIDCheckCombination");
        equipmentForDurableMethod.equipmentSpecialControlVsTxIDCheckCombination(objCommonIn, equipmentID);

        //Get eqp Infomation
        //step2 - sxEqpInfoInq
        log.info("step2 - sxEqpInfoInq");
        Results.EqpInfoInqResult eqpInfoInqRes = new Results.EqpInfoInqResult();
        Infos.EqpStatusInfo equipmentStatusInfo = new Infos.EqpStatusInfo();
        Infos.EqpBrInfo equipmentBRInfo = new Infos.EqpBrInfo();
        eqpInfoInqRes.setEquipmentBasicInfo(equipmentBRInfo);
        eqpInfoInqRes.setEquipmentStatusInfo(equipmentStatusInfo);
        equipmentStatusInfo.setEquipmentAvailableFlag(false);
        equipmentBRInfo.setReticleUseFlag(false);
        equipmentBRInfo.setFixtureUseFlag(false);
        equipmentBRInfo.setCassetteChangeFlag(false);
        equipmentBRInfo.setStartLotsNotifyRequiredFlag(false);
        equipmentBRInfo.setMonitorCreationFlag(false);
        equipmentBRInfo.setEqpToEqpTransferFlag(false);
        equipmentBRInfo.setTakeInOutTransferFlag(false);
        equipmentBRInfo.setEmptyCassetteRequireFlag(false);
        equipmentBRInfo.setFmcCapabilityFlag(false);
        equipmentBRInfo.setFmcSwitch("OFF");
        Params.EqpInfoInqParams eqpInfoInqParams = new Params.EqpInfoInqParams();
        eqpInfoInqParams.setEquipmentID(equipmentID);
        eqpInfoInqParams.setRequestFlagForBasicInfo(true);
        eqpInfoInqParams.setRequestFlagForStatusInfo(true);
        eqpInfoInqParams.setRequestFlagForPMInfo(false);
        eqpInfoInqParams.setRequestFlagForPortInfo(true);
        eqpInfoInqParams.setRequestFlagForChamberInfo(false);
        eqpInfoInqParams.setRequestFlagForStockerInfo(false);
        eqpInfoInqParams.setRequestFlagForInprocessingLotInfo(false);
        eqpInfoInqParams.setRequestFlagForReservedControlJobInfo(false);
        eqpInfoInqParams.setRequestFlagForRSPPortInfo(false);
        eqpInfoInqParams.setRequestFlagForEqpContainerInfo(false);
        Results.EqpInfoInqResult eqpInfoInqResult = equipmentInqService.sxEqpInfoInq(objCommonIn, eqpInfoInqParams);

        //Check Online Mode and Available Flag
        List<Infos.EqpPortStatus> eqpPortStatusList = eqpInfoInqResult.getEquipmentPortInfo().getEqpPortStatuses();
        for (Infos.EqpPortStatus portStatus : eqpPortStatusList) {
            if (CimStringUtils.equals(BizConstant.SP_EQP_ONLINEMODE_OFFLINE, portStatus.getOnlineMode())) {
                log.info("onlineMode == [Offline]");
                Validations.check(retCodeConfig.getInvalidEquipmentMode(), ObjectIdentifier.fetchValue(equipmentID),
                        portStatus.getOnlineMode());
            } else {
                log.info("onlineMode is OK");
            }
        }

        //step3 - equipmentPortInfoSortByGroup
        log.info("step3 - equipmentPortInfoSortByGroup");
        Infos.EqpPortInfoOrderByGroup eqpPortInfoOrderByGroup = equipmentMethod.equipmentPortInfoSortByGroup(objCommonIn, equipmentID, eqpPortStatusList);

        //step4 - durableEquipmentTargetPortPickup
        log.info("step4 - durableEquipmentTargetPortPickup");
        Infos.EqpBrInfo eqpBrInfo = eqpInfoInqResult.getEquipmentBasicInfo();
        Outputs.EquipmentTargetPortPickupOut equipmentTargetPortPickupOut = equipmentForDurableMethod.durableEquipmentTargetPortPickup(objCommonIn, eqpPortInfoOrderByGroup, eqpBrInfo, eqpInfoInqResult.getEquipmentPortInfo());

        List<Infos.PortGroup> targetPortGroupList = equipmentTargetPortPickupOut.getEqpTargetPortInfo().getPortGroups();
        int lenPortGroup = CimArrayUtils.getSize(targetPortGroupList);

        if (lenPortGroup == 0) {
            log.info(" 0 == lenPortGroup");
            throw new ServiceException(retCodeConfig.getNotFoundTargetPort());
        }

        ObjectIdentifier dummy = new ObjectIdentifier();
        int nPortLen;
        String tmpAPCIFControlStatus = null;
        Boolean bLoadReqFlag = true;

        // Unload Request
        if (CimStringUtils.equals(equipmentTargetPortPickupOut.getTargetPortType(), BizConstant.SP_PORTRSC_PORTSTATE_UNLOADREQ)) {
            bLoadReqFlag = false;

            // Check eqp availability;
            if (CimBooleanUtils.isFalse(bLoadReqFlag)) {
                if (CimBooleanUtils.isTrue(eqpInfoInqResult.getEquipmentBasicInfo().isEqpToEqpTransferFlag())) {
                    //get whereNextTransferEqp
                    //step5 - durableWhereNextTransferEqp
                    log.info("step5 - durableWhereNextTransferEqp");
                    Boolean whereNextOKFlag = true;
                    Outputs.DurableWhereNextTransferEqpOut durableWhereNextTransferEqpOut = null;
                    try {
                        durableWhereNextTransferEqpOut = whereNextMethod.durableWhereNextTransferEqp(objCommonIn, equipmentID, equipmentTargetPortPickupOut);
                    } catch (ServiceException e) {
                        whereNextOKFlag = false;
                        durableWhereNextTransferEqpOut = (Outputs.DurableWhereNextTransferEqpOut) e.getData();
                        log.info("EQP to EQP Xfer is interrupted, and change for the Stocker Xfer");
                    }

                    if (CimBooleanUtils.isTrue(whereNextOKFlag)){
                        //Request to durable move in reserve
                        //step5-1 - sxDrbInfoForMoveInReserveInq
                        log.info("step5-1 - sxDrbInfoForMoveInReserveInq");
                        Params.DurablesInfoForStartReservationInqInParam durablesInfoForStartReservationInqInParam = new Params.DurablesInfoForStartReservationInqInParam();
                        durablesInfoForStartReservationInqInParam.setDurableCategory(durableWhereNextTransferEqpOut.getDurableCategory());
                        durablesInfoForStartReservationInqInParam.setEquipmentID(durableWhereNextTransferEqpOut.getEquipmentID());
                        List<ObjectIdentifier> durableIDs = durableWhereNextTransferEqpOut.getStrStartDurables().stream().map(Infos.StartDurable::getDurableId).collect(Collectors.toList());
                        durablesInfoForStartReservationInqInParam.setDurableIDs(durableIDs);
                        Results.DurablesInfoForStartReservationInqResult durablesInfoForStartReservationInqResult = null;
                        try {
                            durablesInfoForStartReservationInqResult = durableInqService.sxDrbInfoForMoveInReserveInq(objCommonIn, durablesInfoForStartReservationInqInParam);
                        } catch (ServiceException e) {
                            log.info("sxDrbInfoForMoveInReserveInq Not OK");
                            e.setData(out);
                            throw e;
                        }
                        //step5-2 - sxDrbMoveInReq
                        log.info("step5-2 - sxDrbMoveInReq");
                        Infos.ObjCommon strTmpObjCommon = objCommonIn;
                        strTmpObjCommon.setTransactionID(TransactionIDEnum.START_DURABLES_RESERVATION_REQ.getValue());
                        Params.StartDurablesReservationReqInParam params = new Params.StartDurablesReservationReqInParam();
                        params.setEquipmentID(durablesInfoForStartReservationInqResult.getEquipmentID());
                        params.setDurableCategory(durablesInfoForStartReservationInqResult.getDurableCategory());
                        params.setStrDurableStartRecipe(durablesInfoForStartReservationInqResult.getStrDurableStartRecipe());
                        params.setStrStartDurables(durableWhereNextTransferEqpOut.getStrStartDurables());
                        params.setClaimMemo(null);
                        ObjectIdentifier durableControlJobID = null;
                        try {
                            durableControlJobID = durableService.sxDrbMoveInReserveReq(strTmpObjCommon, params,null);
                        } catch (ServiceException ex) {
                            log.info("sxDrbMoveInReq() Not OK");
                            //---------------------------------------------------------------------------------------------
                            //  Prepare message text string for case of failure of start lot reservation.
                            //  If start lot reservation for equipment is fail, this messageText is filled.
                            //---------------------------------------------------------------------------------------------
                            //Prepare e-mail Message Text
                            StringBuilder msg = new StringBuilder("<<< DrbMoveInReservation Error!  (EQP -> EQP) >>>\n");
                            msg.append("\n")
                                    .append("Equipment ID         : ").append(ObjectIdentifier.fetchValue(durableWhereNextTransferEqpOut.getEquipmentID())).append("\n")
                                    .append("Durable IDs              : ");
                            boolean bLotSet = false;
                            if (CimArrayUtils.isNotEmpty(durableWhereNextTransferEqpOut.getStrStartDurables())){
                                for (Infos.StartDurable strStartDurable : durableWhereNextTransferEqpOut.getStrStartDurables()) {
                                    if (!ObjectIdentifier.isEmptyWithValue(strStartDurable.getDurableId())){
                                        if (CimBooleanUtils.isTrue(bLotSet)){
                                            msg.append(", ");
                                        }
                                        msg.append(ObjectIdentifier.fetchValue(strStartDurable.getDurableId()));
                                        bLotSet = true;
                                    }
                                }
                            }
                            msg.append("\n")
                                    .append("Transaction ID       : ").append(ex.getTransactionID()).append("\n")
                                    .append("Return Code          : ").append(ex.getCode()).append("\n")
                                    .append("Message Text         : ").append(ex.getMessage()).append("\n")
                                    .append("Reason Text          : ").append(ex.getReasonText()).append("\n");
                            Infos.SysMsgStockInfo sysMsg = new Infos.SysMsgStockInfo();
                            sysMsg.setSubSystemID(BizConstant.SP_SUBSYSTEMID_MM);
                            sysMsg.setSystemMessageCode(BizConstant.SP_SYSTEMMSGCODE_DELIVERYERROR);
                            sysMsg.setSystemMessageText(msg.toString());
                            sysMsg.setNotifyFlag(true);
                            sysMsg.setEquipmentID(equipmentID);
                            sysMsg.setEquipmentStatus("");
                            sysMsg.setStockerID(dummy);
                            sysMsg.setStockerStatus("");
                            sysMsg.setAGVID(dummy);
                            sysMsg.setAGVStatus("");
                            sysMsg.setLotID(dummy);
                            sysMsg.setLotStatus("");
                            sysMsg.setRouteID(dummy);
                            sysMsg.setOperationID(dummy);
                            sysMsg.setOperationNumber("");
                            sysMsg.setSystemMessageTimeStamp(CimDateUtils.convertToSpecString(objCommonIn.getTimeStamp().getReportTimeStamp()));
                            List<Infos.SysMsgStockInfo> sysMstStockInfoList = CimArrayUtils.getSize(out.getSysMstStockInfoList()) == 0 ? new ArrayList<>() : out.getSysMstStockInfoList();
                            sysMstStockInfoList.add(sysMsg);
                            out.setSysMstStockInfoList(sysMstStockInfoList);
                            ex.setData(out);
                            throw ex;
                        }
                        log.info("sxDrbMoveInReq() Is OK");
                        //Make sxMultipleCarrierTransferReq parameter
                        //step5-3 - multiDurableXferFillInOTMSW005InParm
                        log.info("step5-3 - multiDurableXferFillInOTMSW005InParm");
                        Outputs.ObjMultiDurableXferFillInOTMSW005InParmOut multiDurableXferFillInOTMSW005InParmResult = null;
                        try {
                            multiDurableXferFillInOTMSW005InParmResult = durableMethod.multiDurableXferFillInOTMSW005InParm(objCommonIn,
                                    durableWhereNextTransferEqpOut.getEquipmentID(), durableWhereNextTransferEqpOut.getStrStartDurables(),durablesInfoForStartReservationInqResult.getDurableCategory());
                        } catch (ServiceException e) {
                            log.info("multiDurableXferFillInOTMSW005InParm() Not OK");
                            //Request to Move In Reserve Cancel
                            //step5-4 - sxDrbMoveInCancelReq
                            log.info("step5-4 - sxDrbMoveInCancelReq");
                            Params.StartDurablesReservationCancelReqInParam in = new Params.StartDurablesReservationCancelReqInParam();
                            in.setEquipmentID(durableWhereNextTransferEqpOut.getEquipmentID());
                            in.setDurableControlJobID(durableControlJobID);
                            try {
                                Results.StartDurablesReservationCancelReqResult moveInReserveCancelReqResult = durableService.sxDrbMoveInReserveCancelReq(objCommonIn, in,null);
                            } catch (ServiceException ex) {
                                log.info("sxDrbMoveInCancelReq Not OK");
                            }
                            e.setData(out);
                            throw e;
                        }

                        //Send Transfer Request to TMS. (EQP -> EQP)
                        //step5-5 - sxMultipleDurableTransferReq
                        log.info("step5-5 - sxMultipleDurableTransferReq");
                        try {
                            this.sxMultipleDurableTransferReq(objCommonIn,
                                    false, "S", multiDurableXferFillInOTMSW005InParmResult.getStrDurableXferReq(),durablesInfoForStartReservationInqResult.getDurableCategory());
                        } catch (ServiceException e) {
                            log.info("sxMultipleDurableTransferReq() Not OK");
                            //Prepare e-mail Message Text
                            StringBuilder msg = new StringBuilder("<<< Transfer Error!  (EQP -> EQP) >>>\n");
                            msg.append("\n")
                                    .append("Equipment ID         : ").append(ObjectIdentifier.fetchValue(equipmentID)).append("\n");
                            if (CimArrayUtils.isNotEmpty(multiDurableXferFillInOTMSW005InParmResult.getStrDurableXferReq())){
                                for (Infos.DurableXferReq durableXferReq : multiDurableXferFillInOTMSW005InParmResult.getStrDurableXferReq()) {
                                    msg.append("-----------------------------------------\n")
                                            .append("Durable ID           : ").append(ObjectIdentifier.fetchValue(durableXferReq.getDurableID())).append("\n")
                                            .append("From Machine ID      : ").append(ObjectIdentifier.fetchValue(durableXferReq.getFromMachineID())).append("\n")
                                            .append("From Port ID         : ").append(ObjectIdentifier.fetchValue(durableXferReq.getFromPortID())).append("\n")
                                            .append("To Stocker Group     : ").append(durableXferReq.getToStockerGroup()).append("\n");
                                    int toMachineLen = CimArrayUtils.getSize(durableXferReq.getStrToMachine());
                                    if (toMachineLen == 0){
                                        msg.append("To Machine ID        : Nothing\n");
                                    }
                                    for (Infos.ToMachine toMachine : durableXferReq.getStrToMachine()) {
                                        msg.append("To Machine ID        : ").append(ObjectIdentifier.fetchValue(toMachine.getToMachineID())).append("\n")
                                                .append("To Port ID           : ").append(ObjectIdentifier.fetchValue(toMachine.getToPortID())).append("\n");
                                    }
                                }
                            }
                            msg.append("-----------------------------------------\n")
                                    .append("Transaction ID       : ").append(e.getTransactionID()).append("\n")
                                    .append("Return Code          : ").append(e.getCode()).append("\n")
                                    .append("Message Text         : ").append(e.getMessage()).append("\n")
                                    .append("Reason Text          : ").append(e.getReasonText()).append("\n");
                            //Set System Message
                            Infos.SysMsgStockInfo sysMsg = new Infos.SysMsgStockInfo();
                            sysMsg.setSubSystemID(BizConstant.SP_SUBSYSTEMID_MM);
                            sysMsg.setSystemMessageCode(BizConstant.SP_SYSTEMMSGCODE_DELIVERYERROR);
                            sysMsg.setSystemMessageText(msg.toString());
                            sysMsg.setNotifyFlag(true);
                            sysMsg.setEquipmentID(equipmentID);
                            sysMsg.setEquipmentStatus("");
                            sysMsg.setStockerID(dummy);
                            sysMsg.setStockerStatus("");
                            sysMsg.setAGVID(dummy);
                            sysMsg.setAGVStatus("");
                            sysMsg.setLotID(dummy);
                            sysMsg.setLotStatus("");
                            sysMsg.setRouteID(dummy);
                            sysMsg.setOperationID(dummy);
                            sysMsg.setOperationNumber("");
                            sysMsg.setSystemMessageTimeStamp(CimDateUtils.convertToSpecString(objCommonIn.getTimeStamp().getReportTimeStamp()));
                            List<Infos.SysMsgStockInfo> sysMstStockInfoList = CimArrayUtils.getSize(out.getSysMstStockInfoList()) == 0 ? new ArrayList<>() : out.getSysMstStockInfoList();
                            sysMstStockInfoList.add(sysMsg);
                            out.setSysMstStockInfoList(sysMstStockInfoList);
                            //Request to MoveInReserve Cancel
                            //step5-6 - sxDrbMoveInCancelReq
                            log.info("step5-6 - sxDrbMoveInCancelReq");
                            Params.StartDurablesReservationCancelReqInParam moveInReserveCancelReqParams = new Params.StartDurablesReservationCancelReqInParam();
                            moveInReserveCancelReqParams.setEquipmentID(durableWhereNextTransferEqpOut.getEquipmentID());
                            moveInReserveCancelReqParams.setDurableControlJobID(durableControlJobID);
                            try {
                                Results.StartDurablesReservationCancelReqResult moveInReserveCancelReqResultRetCode = durableService.sxDrbMoveInReserveCancelReq(objCommonIn, moveInReserveCancelReqParams,null);
                            } catch (ServiceException ex) {
                                log.info("sxDrbMoveInCancelReq Not OK");
                            }
                            e.setData(out);
                            throw e;
                        }
                        /*--------------------*/
                        /*   Return to Main   */
                        /*--------------------*/
                        log.info("OK! OK! OK! [EQP to EQP]  Return to Main");
                        return out;
                    }
                }
            }
            if (CimBooleanUtils.isFalse(bLoadReqFlag)) {
                List<Infos.CarrierXferReq> tmpCarrierXferReqSeq = new ArrayList<>();
                int nILen = CimArrayUtils.getSize(equipmentTargetPortPickupOut.getEqpTargetPortInfo().getPortGroups().get(0).getStrPortID());
                for (int i = 0; i < nILen; i++) {
                    //Get Where Next Stocker
                    //Call RTD Interface
                    //step6 - cassetteDelivery_RTDInterfaceReq
                    log.info("step6 - cassetteDelivery_RTDInterfaceReq");
                    Results.DurableWhereNextStockerInqResult durableWhereNextStockerInqResult = null;
                    Outputs.ObjDurableDeliveryRTDInterfaceReqOut durableDeliveryRTDInterfaceReqResult = null;
                    try {
                        durableDeliveryRTDInterfaceReqResult = durableMethod.durableDeliveryRTDInterfaceReq(objCommonIn,
                                BizConstant.SP_RTD_FUNCTION_CODE_WHERENEXT, equipmentTargetPortPickupOut.getEqpTargetPortInfo().getPortGroups().get(0).getStrPortID().get(i).getCassetteID());
                        durableWhereNextStockerInqResult = durableDeliveryRTDInterfaceReqResult == null ? null : durableDeliveryRTDInterfaceReqResult.getStrWhereNextStockerInqResult();
                    } catch (ServiceException e) {
                        if (!Validations.isEquals(retCodeConfigEx.getRtdInterfaceSwitchOff(), e.getCode())
                                && !Validations.isEquals(retCodeConfigEx.getNotFoundRTD(), e.getCode())) {
                            StringBuilder msg = new StringBuilder("<<< RTD Interface Error!  (WhereNext of DurableDelivery) >>>\n");
                            msg.append("\n")
                                    .append("Equipment ID         : ").append(ObjectIdentifier.fetchValue(equipmentID)).append("\n")
                                    .append("-----------------------------------------\n")
                                    .append("Transaction ID       : ").append(e.getTransactionID()).append("\n")
                                    .append("Return Code          : ").append(e.getCode()).append("\n")
                                    .append(e.getMessage()).append("\n")
                                    .append("Reason Text          : ").append(e.getReasonText()).append("\n");
                            //Set System Message
                            Infos.SysMsgStockInfo sysMsg = new Infos.SysMsgStockInfo();
                            sysMsg.setSubSystemID(BizConstant.SP_SUBSYSTEMID_MM);
                            sysMsg.setSystemMessageCode(BizConstant.SP_SYSTEMMSGCODE_RTDERROR);
                            sysMsg.setSystemMessageText(msg.toString());
                            sysMsg.setNotifyFlag(true);
                            sysMsg.setEquipmentID(equipmentID);
                            sysMsg.setEquipmentStatus("");
                            sysMsg.setStockerID(dummy);
                            sysMsg.setStockerStatus("");
                            sysMsg.setAGVID(dummy);
                            sysMsg.setAGVStatus("");
                            sysMsg.setLotID(dummy);
                            sysMsg.setLotStatus("");
                            sysMsg.setRouteID(dummy);
                            sysMsg.setOperationID(dummy);
                            sysMsg.setOperationNumber("");
                            sysMsg.setSystemMessageTimeStamp(CimDateUtils.convertToSpecString(objCommonIn.getTimeStamp().getReportTimeStamp()));
                            List<Infos.SysMsgStockInfo> sysMstStockInfoList = CimArrayUtils.getSize(out.getSysMstStockInfoList()) == 0 ? new ArrayList<>() : out.getSysMstStockInfoList();
                            sysMstStockInfoList.add(sysMsg);
                            out.setSysMstStockInfoList(sysMstStockInfoList);
                        }
                        //Call Normal Function (sxDurableWhereNextStockerInq)
                        //step6-1 - sxDurableWhereNextStockerInq
                        log.info("step6-1 - sxDurableWhereNextStockerInq");
                        try {
                            durableWhereNextStockerInqResult = transferManagementSystemInqService.sxDurableWhereNextStockerInq(objCommonIn,
                                    equipmentTargetPortPickupOut.getEqpTargetPortInfo().getPortGroups().get(0).getStrPortID().get(i).getCassetteID());
                        } catch (ServiceException ex) {
                            if (!Validations.isSuccess(ex.getCode())) {
                                StringBuilder msg = new StringBuilder("<<< WhereNext Error!  (EQP -> Stocker) >>>\n");
                                msg.append("\n")
                                        .append("Equipment ID         : ").append(ObjectIdentifier.fetchValue(equipmentID)).append("\n")
                                        .append("-----------------------------------------\n")
                                        .append("Transaction ID       : ").append(ex.getTransactionID()).append("\n")
                                        .append("Return Code          : ").append(ex.getCode()).append("\n")
                                        .append("Message Text         : ").append(ex.getMessage()).append("\n")
                                        .append("Reason Text          : ").append(ex.getReasonText()).append("\n");
                                //Set System Message
                                Infos.SysMsgStockInfo sysMsg = new Infos.SysMsgStockInfo();
                                sysMsg.setSubSystemID(BizConstant.SP_SUBSYSTEMID_MM);
                                sysMsg.setSystemMessageCode(BizConstant.SP_SYSTEMMSGCODE_DELIVERYERROR);
                                sysMsg.setSystemMessageText(msg.toString());
                                sysMsg.setNotifyFlag(true);
                                sysMsg.setEquipmentID(equipmentID);
                                sysMsg.setEquipmentStatus("");
                                sysMsg.setStockerID(dummy);
                                sysMsg.setStockerStatus("");
                                sysMsg.setAGVID(dummy);
                                sysMsg.setAGVStatus("");
                                sysMsg.setLotID(dummy);
                                sysMsg.setLotStatus("");
                                sysMsg.setRouteID(dummy);
                                sysMsg.setOperationID(dummy);
                                sysMsg.setOperationNumber("");
                                sysMsg.setSystemMessageTimeStamp(CimDateUtils.convertToSpecString(objCommonIn.getTimeStamp().getReportTimeStamp()));
                                List<Infos.SysMsgStockInfo> sysMstStockInfoList = CimArrayUtils.getSize(out.getSysMstStockInfoList()) == 0 ? new ArrayList<>() : out.getSysMstStockInfoList();
                                sysMstStockInfoList.add(sysMsg);
                                out.setSysMstStockInfoList(sysMstStockInfoList);
                                ex.setData(out);
                                throw ex;
                            }
                        }
                    }
                    //Make txSingleCarrierTransferReq parameter
                    //step6-2 - singleDurableXferFillInOTMSW006InParm
                    log.info("step6-2 - singleDurableXferFillInOTMSW006InParm");
                    Outputs.ObjSingleDurableXferFillInOTMSW006InParmOut singleDurableXferFillInOTMSW006InParmResult = null;
                    try {
                        if (durableWhereNextStockerInqResult != null) {
                            singleDurableXferFillInOTMSW006InParmResult = durableMethod.singleDurableXferFillInOTMSW006InParm(objCommonIn,
                                    equipmentID, equipmentTargetPortPickupOut.getEqpTargetPortInfo().getPortGroups().get(0).getStrPortID().get(i).getPortID(),
                                    equipmentTargetPortPickupOut.getEqpTargetPortInfo().getPortGroups().get(0).getStrPortID().get(i).getCassetteID(),
                                    durableWhereNextStockerInqResult,durableWhereNextStockerInqResult.getDurableCategory());
                        }
                    } catch (ServiceException e) {
                        if (Validations.isEquals(retCodeConfigEx.getNoXferNeeded(), e.getCode())) {
                            log.info("No Transfer Job is requested by environment variable. Proceed to the Next Port...");
                            continue;
                        } else if (Validations.isEquals(retCodeConfigEx.getNoStockerForCurrentEqp(), e.getCode())) {
                            singleDurableXferFillInOTMSW006InParmResult = e.getData(Outputs.ObjSingleDurableXferFillInOTMSW006InParmOut.class);
                            Infos.DurableXferReq durableXferReq = singleDurableXferFillInOTMSW006InParmResult.getStrDurableXferReq();
                            log.info("An Error is Detected. Proceed to the Next Port...");
                            //Prepare e-mail Message Text
                            StringBuilder msg = new StringBuilder("<<< Transfer Error!  (EQP -> Stocker) >>>\n");
                            msg.append("\n")
                                    .append("Equipment ID         : ").append(ObjectIdentifier.fetchValue(equipmentID)).append("\n")
                                    .append("-----------------------------------------\n")
                                    .append("durable ID           : ").append(ObjectIdentifier.fetchValue(durableXferReq.getDurableID())).append("\n")
                                    .append("From Machine ID      : ").append(ObjectIdentifier.fetchValue(durableXferReq.getFromMachineID())).append("\n")
                                    .append("From Port ID         : ").append(ObjectIdentifier.fetchValue(durableXferReq.getFromPortID())).append("\n")
                                    .append("To Stocker Group     : ").append(durableXferReq.getToStockerGroup()).append("\n");
                            int toMachineLen = CimArrayUtils.getSize(durableXferReq.getStrToMachine());
                            if (toMachineLen == 0){
                                msg.append("To Machine ID        : Nothing\n");
                            }
                            for (Infos.ToMachine toMachine : durableXferReq.getStrToMachine()) {
                                msg.append("To Machine ID        : ").append(ObjectIdentifier.fetchValue(toMachine.getToMachineID())).append("\n")
                                        .append("To Port ID           : ").append(ObjectIdentifier.fetchValue(toMachine.getToPortID())).append("\n");
                            }
                            msg.append("-----------------------------------------\n")
                                    .append("Transaction ID       : ").append(e.getTransactionID()).append("\n")
                                    .append("Return Code          : ").append(e.getCode()).append("\n")
                                    .append("Message Text         : ").append(e.getMessage()).append("\n")
                                    .append("Reason Text          : ").append(e.getReasonText()).append("\n");
                            //Set System Message
                            Infos.SysMsgStockInfo sysMsg = new Infos.SysMsgStockInfo();
                            sysMsg.setSubSystemID(BizConstant.SP_SUBSYSTEMID_MM);
                            sysMsg.setSystemMessageCode(BizConstant.SP_SYSTEMMSGCODE_DELIVERYERROR);
                            sysMsg.setSystemMessageText(msg.toString());// up on the msg but not add
                            sysMsg.setNotifyFlag(true);
                            sysMsg.setEquipmentID(equipmentID);
                            sysMsg.setEquipmentStatus("");
                            sysMsg.setStockerID(dummy);
                            sysMsg.setStockerStatus("");
                            sysMsg.setAGVID(dummy);
                            sysMsg.setAGVStatus("");
                            sysMsg.setLotID(dummy);
                            sysMsg.setLotStatus("");
                            sysMsg.setRouteID(dummy);
                            sysMsg.setOperationID(dummy);
                            sysMsg.setOperationNumber("");
                            sysMsg.setSystemMessageTimeStamp(CimDateUtils.convertToSpecString(objCommonIn.getTimeStamp().getReportTimeStamp()));
                            List<Infos.SysMsgStockInfo> sysMstStockInfoList = CimArrayUtils.getSize(out.getSysMstStockInfoList()) == 0 ? new ArrayList<>() : out.getSysMstStockInfoList();
                            sysMstStockInfoList.add(sysMsg);
                            out.setSysMstStockInfoList(sysMstStockInfoList);
                            continue;
                        } else {
                            e.setData(out);
                            throw e;
                        }
                    }

                    //Check whether specified machine is eqp or stocker
                    Boolean isStorageFlag = false;
                    //step6-3 - equipmentStatusInfoGet
                    log.info("step6-3 - equipmentStatusInfoGet");
                    Infos.EqpStatusInfo eqpStatusInfoRetCode = null;
                    try {
                        if (null != singleDurableXferFillInOTMSW006InParmResult) {
                            eqpStatusInfoRetCode = equipmentMethod.equipmentStatusInfoGet(objCommonIn, singleDurableXferFillInOTMSW006InParmResult.getStrDurableXferReq().getFromMachineID());
                        }
                    } catch (ServiceException e) {
                        if (Validations.isEquals(retCodeConfig.getNotFoundEqp(), e.getCode())) {
                            isStorageFlag = true;
                        } else {
                            e.setData(out);
                            throw e;
                        }
                    }
                    if (null != singleDurableXferFillInOTMSW006InParmResult) {
                        log.info("Equipment is Storage or not == {},{}", ObjectIdentifier.fetchValue(singleDurableXferFillInOTMSW006InParmResult.getStrDurableXferReq().getFromMachineID()), isStorageFlag);
                    }
                    if (CimBooleanUtils.isFalse(isStorageFlag)) {
                        // Lock Port Object Of machine
                        //step6-4 - objectLockForEquipmentResource
                        log.info("step6-4 - objectLockForEquipmentResource");
                        try {
                            if (null != singleDurableXferFillInOTMSW006InParmResult) {
                                lockMethod.objectLockForEquipmentResource(objCommonIn,singleDurableXferFillInOTMSW006InParmResult.getStrDurableXferReq().getFromMachineID(),
                                        singleDurableXferFillInOTMSW006InParmResult.getStrDurableXferReq().getFromPortID(),
                                        BizConstant.SP_CLASSNAME_POSPORTRESOURCE);
                            }
                        } catch (ServiceException e) {
                            e.setData(out);
                            throw e;
                        }
                        //Check Equipment Port for Sending Request to TMS. (EQP -> Stocker)
                        //step6-5 - equipmentPortStateCheckForCassetteDelivery
                        log.info("step6-5 - equipmentPortStateCheckForCassetteDelivery");
                        Inputs.ObjEquipmentPortStateCheckForCassetteDeliveryIn deliveryIn = new Inputs.ObjEquipmentPortStateCheckForCassetteDeliveryIn();
                        if (null != singleDurableXferFillInOTMSW006InParmResult) {
                            deliveryIn.setEquipmentID(singleDurableXferFillInOTMSW006InParmResult.getStrDurableXferReq().getFromMachineID());
                            deliveryIn.setPortID(singleDurableXferFillInOTMSW006InParmResult.getStrDurableXferReq().getFromPortID());
                        }
                        try {
                            equipmentMethod.equipmentPortStateCheckForCassetteDelivery(objCommonIn, deliveryIn);
                        } catch (ServiceException e) {
                            e.setData(out);
                            throw e;
                        }
                    }
                    //Send Request to TMS. (EQP -> Stocker)
                    Params.SingleDurableTransferReqParam singleDurableTransferReqParam = new Params.SingleDurableTransferReqParam();
                    Infos.DurableXferReq strDurableXferReq = null;
                    if (null != singleDurableXferFillInOTMSW006InParmResult) {
                        singleDurableTransferReqParam.setRerouteFlag(singleDurableXferFillInOTMSW006InParmResult.getRerouteFlag());
                        strDurableXferReq = singleDurableXferFillInOTMSW006InParmResult.getStrDurableXferReq();
                    }
                    if (null != strDurableXferReq) {
                        singleDurableTransferReqParam.setDurableID(strDurableXferReq.getDurableID());
                        singleDurableTransferReqParam.setZoneType(strDurableXferReq.getZoneType());
                        singleDurableTransferReqParam.setN2PurgeFlag(strDurableXferReq.getN2PurgeFlag());
                        singleDurableTransferReqParam.setFromMachineID(strDurableXferReq.getFromMachineID());
                        singleDurableTransferReqParam.setFromPortID(strDurableXferReq.getFromPortID());
                        singleDurableTransferReqParam.setToStockerGroup(strDurableXferReq.getToStockerGroup());
                        singleDurableTransferReqParam.setToMachine(strDurableXferReq.getStrToMachine());
                        singleDurableTransferReqParam.setExpectedStartTime(strDurableXferReq.getExpectedStartTime());
                        singleDurableTransferReqParam.setExpectedEndTime(strDurableXferReq.getExpectedEndTime());
                        singleDurableTransferReqParam.setMandatoryFlag(strDurableXferReq.getMandatoryFlag());
                        singleDurableTransferReqParam.setPriority(strDurableXferReq.getPriority());
                    }
                    //step6-6 - sxSingleDurableTransferReq
                    log.info("step6-6 - sxSingleDurableTransferReq");
                    try {
                        if (null != durableWhereNextStockerInqResult) {
                            this.sxSingleDurableTransferReq(objCommonIn, singleDurableTransferReqParam,durableWhereNextStockerInqResult.getDurableCategory());
                        }
                    } catch (ServiceException e) {
                        log.info("sxSingleDurableTransferReq() Not OK");
                        //Prepare e-mail Message Text
                        StringBuilder msg = new StringBuilder("<<< Transfer Error!  (EQP -> Stocker) >>>\n");
                        msg.append("\n")
                                .append("Equipment ID         : ").append(ObjectIdentifier.fetchValue(equipmentID)).append("\n")
                                .append("-----------------------------------------\n")
                                .append("Durable ID           : ").append(ObjectIdentifier.fetchValue(strDurableXferReq.getDurableID())).append("\n")
                                .append("From Machine ID      : ")
                                .append(ObjectIdentifier.fetchValue(strDurableXferReq.getFromMachineID()))
                                .append("From Port ID         : ").append(ObjectIdentifier.fetchValue(strDurableXferReq.getFromPortID())).append("\n")
                                .append("To Stocker Group     : ").append(strDurableXferReq.getToStockerGroup()).append("\n");
                        int toMachineLen = CimArrayUtils.getSize(strDurableXferReq.getStrToMachine());
                        if (toMachineLen == 0){
                            msg.append("To Machine ID        : Nothing\n");
                        }
                        for (Infos.ToMachine toMachine : strDurableXferReq.getStrToMachine()) {
                            msg.append("To Machine ID        : ").append(ObjectIdentifier.fetchValue(toMachine.getToMachineID())).append("\n")
                                    .append("To Port ID           : ").append(ObjectIdentifier.fetchValue(toMachine.getToPortID())).append("\n");
                        }
                        msg.append("-----------------------------------------\n")
                                .append("Transaction ID       : ").append(e.getTransactionID()).append("\n")
                                .append("Return Code          : ").append(e.getCode()).append("\n")
                                .append("Message Text         : ").append(e.getMessage()).append("\n")
                                .append("Reason Text          : ").append(e.getReasonText()).append("\n");
                        //Set System Message
                        Infos.SysMsgStockInfo sysMsg = new Infos.SysMsgStockInfo();
                        sysMsg.setSubSystemID(BizConstant.SP_SUBSYSTEMID_MM);
                        sysMsg.setSystemMessageCode(BizConstant.SP_SYSTEMMSGCODE_DELIVERYERROR);
                        sysMsg.setSystemMessageText(msg.toString());
                        sysMsg.setNotifyFlag(true);
                        sysMsg.setEquipmentID(equipmentID);
                        sysMsg.setEquipmentStatus("");
                        sysMsg.setStockerID(dummy);
                        sysMsg.setStockerStatus("");
                        sysMsg.setAGVID(dummy);
                        sysMsg.setAGVStatus("");
                        sysMsg.setLotID(dummy);
                        sysMsg.setLotStatus("");
                        sysMsg.setRouteID(dummy);
                        sysMsg.setOperationID(dummy);
                        sysMsg.setOperationNumber("");
                        sysMsg.setSystemMessageTimeStamp(CimDateUtils.convertToSpecString(objCommonIn.getTimeStamp().getReportTimeStamp()));
                        List<Infos.SysMsgStockInfo> sysMstStockInfoList = CimArrayUtils.getSize(out.getSysMstStockInfoList()) == 0 ? new ArrayList<>() : out.getSysMstStockInfoList();
                        sysMstStockInfoList.add(sysMsg);
                        out.setSysMstStockInfoList(sysMstStockInfoList);
                        e.setData(out);
                        throw e;
                    }
                }
                /*--------------------*/
                /*   End of Process   */
                /*--------------------*/
                log.info("OK! OK! OK! [EQP to EQP fail and to STK]  Return to Main");
                bLoadReqFlag = false;
            }
        }
        if (CimBooleanUtils.isTrue(bLoadReqFlag)) {
            //Load Request
            if (CimStringUtils.equals(BizConstant.SP_PORTRSC_PORTSTATE_LOADREQ, equipmentTargetPortPickupOut.getTargetPortType())) {
                if (CimBooleanUtils.isFalse(eqpInfoInqResult.getEquipmentStatusInfo().getEquipmentAvailableFlag())) {
                    throw new ServiceException(new OmCode(retCodeConfig.getInvalidEquipmentStatus(), equipmentID.getValue(), "NotAvailable"));
                }
                //Get WIP Lots
                Results.WhatNextDurableListInqResult durableWhatNextInqResult = null;
                //Call RTD Interface
                //step7 - durableDeliveryRTDInterfaceReq
                log.info("step7 - durableDeliveryRTDInterfaceReq");
                Outputs.ObjDurableDeliveryRTDInterfaceReqOut durableDeliveryRTDInterfaceReqResult = null;
                try {
                    durableDeliveryRTDInterfaceReqResult = durableMethod.durableDeliveryRTDInterfaceReq(objCommonIn, BizConstant.SP_RTD_FUNCTION_CODE_WHATNEXT, equipmentID);
                    log.info("Normal End RTD Interface of sxDrbWhatNextListInq");
                    durableWhatNextInqResult = null == durableDeliveryRTDInterfaceReqResult ? null : durableDeliveryRTDInterfaceReqResult.getStrWhatNextInqResult();
                } catch (ServiceException e) {
                    log.info("Faild RTD Interface Call Normal Function (sxDrbWhatNextListInq)");
                    if (!Validations.isEquals(retCodeConfigEx.getRtdInterfaceSwitchOff(), e.getCode())
                            && !Validations.isEquals(retCodeConfigEx.getNotFoundRTD(), e.getCode())) {
                        //Set System Message
                        StringBuilder msg = new StringBuilder("<<< RTD Interface Error!  (WhatNext of DurableDelivery) >>>\n");
                        msg.append("\n")
                                .append("Equipment ID         : ").append(ObjectIdentifier.fetchValue(equipmentID)).append("\n")
                                .append("-----------------------------------------\n")
                                .append("Transaction ID       : ").append(e.getTransactionID()).append("\n")
                                .append("Return Code          : ").append(e.getCode()).append("\n")
                                .append("Message Text         : ").append(e.getMessage()).append("\n")
                                .append("Reason Text          : ").append(e.getReasonText()).append("\n");
                        Infos.SysMsgStockInfo sysMsg = new Infos.SysMsgStockInfo();
                        sysMsg.setSubSystemID(BizConstant.SP_SUBSYSTEMID_MM);
                        sysMsg.setSystemMessageCode(BizConstant.SP_SYSTEMMSGCODE_RTDERROR);
                        sysMsg.setSystemMessageText(msg.toString());
                        sysMsg.setNotifyFlag(true);
                        sysMsg.setEquipmentID(equipmentID);
                        sysMsg.setEquipmentStatus("");
                        sysMsg.setStockerID(dummy);
                        sysMsg.setStockerStatus("");
                        sysMsg.setAGVID(dummy);
                        sysMsg.setAGVStatus("");
                        sysMsg.setLotID(dummy);
                        sysMsg.setLotStatus("");
                        sysMsg.setRouteID(dummy);
                        sysMsg.setOperationID(dummy);
                        sysMsg.setOperationNumber("");
                        sysMsg.setSystemMessageTimeStamp(CimDateUtils.convertToSpecString(objCommonIn.getTimeStamp().getReportTimeStamp()));
                        List<Infos.SysMsgStockInfo> sysMstStockInfoList = CimArrayUtils.getSize(out.getSysMstStockInfoList()) == 0 ? new ArrayList<>() : out.getSysMstStockInfoList();
                        sysMstStockInfoList.add(sysMsg);
                        out.setSysMstStockInfoList(sysMstStockInfoList);
                    }
                    //Call Normal Function (sxDrbWhatNextListInq)
                    Params.WhatNextDurableListInqInParam whatNextDurableListInqInParam = new Params.WhatNextDurableListInqInParam();
                    whatNextDurableListInqInParam.setEquipmentID(equipmentID);
                    whatNextDurableListInqInParam.setSelectCriteria(BizConstant.SP_DP_SELECTCRITERIA_AUTO3);
                    //step7-1 - sxDrbWhatNextListInq
                    log.info("step7-1 - sxDrbWhatNextListInq");
                    try {
                        durableWhatNextInqResult = durableInqService.sxDrbWhatNextListInq(objCommonIn,whatNextDurableListInqInParam);
                    } catch (ServiceException ex) {
                        if (!Validations.isEquals(retCodeConfigEx.getNoWipLot(), ex.getCode())) {
                            //Set System Message
                            StringBuilder msg = new StringBuilder("<<< WhatNext Error!  (Stocker -> EQP) >>>\n");
                            msg.append("\n")
                                    .append("Equipment ID         : ").append(ObjectIdentifier.fetchValue(equipmentID)).append("\n")
                                    .append("-----------------------------------------\n")
                                    .append("Transaction ID       : ").append(ex.getTransactionID()).append("\n")
                                    .append("Return Code          : ").append(ex.getCode()).append("\n")
                                    .append("Message Text         : ").append(ex.getMessage()).append("\n")
                                    .append("Reason Text          : ").append(ex.getReasonText()).append("\n");
                            Infos.SysMsgStockInfo sysMsg = new Infos.SysMsgStockInfo();
                            sysMsg.setSubSystemID(BizConstant.SP_SUBSYSTEMID_MM);
                            sysMsg.setSystemMessageCode(BizConstant.SP_SYSTEMMSGCODE_DELIVERYERROR);
                            sysMsg.setSystemMessageText(msg.toString());
                            sysMsg.setNotifyFlag(true);
                            sysMsg.setEquipmentID(equipmentID);
                            sysMsg.setEquipmentStatus("");
                            sysMsg.setStockerID(dummy);
                            sysMsg.setStockerStatus("");
                            sysMsg.setAGVID(dummy);
                            sysMsg.setAGVStatus("");
                            sysMsg.setLotID(dummy);
                            sysMsg.setLotStatus("");
                            sysMsg.setRouteID(dummy);
                            sysMsg.setOperationID(dummy);
                            sysMsg.setOperationNumber("");
                            sysMsg.setSystemMessageTimeStamp(CimDateUtils.convertToSpecString(objCommonIn.getTimeStamp().getReportTimeStamp()));
                            List<Infos.SysMsgStockInfo> sysMstStockInfoList = CimArrayUtils.getSize(out.getSysMstStockInfoList()) == 0 ? new ArrayList<>() : out.getSysMstStockInfoList();
                            sysMstStockInfoList.add(sysMsg);
                            out.setSysMstStockInfoList(sysMstStockInfoList);
                        }
                        ex.setData(out);
                        throw ex;
                    }
                }
                for (int i = 0; i < lenPortGroup; i++) {
                    List<Infos.PortGroup> strPortGroupSeq = new ArrayList<>();
                    Infos.PortGroup portGroup = equipmentTargetPortPickupOut.getEqpTargetPortInfo().getPortGroups().get(i);
                    strPortGroupSeq.add(portGroup);
                    // Select Multiple Durables
                    //step7-2 - whatNextDurableListToStartDurableForDeliveryReq
                    log.info("step7-2 - whatNextDurableListToStartDurableForDeliveryReq");
                    List<Infos.StartDurable> strStartDurable = null;
                    try {
                        strStartDurable = whatNextMethod.whatNextDurableListToStartDurableForDeliveryReq(objCommonIn, equipmentID, strPortGroupSeq, durableWhatNextInqResult);
                    } catch (ServiceException ex) {
                        log.info("whatNextDurableListToStartDurableForDeliveryReq() Not OK");
                        if (lenPortGroup - 1 == i) {
                            log.info("i is last loop. so return error.");
                            ex.setData(out);
                            throw ex;
                        } else {
                            log.info("continue next PortGroup!!");
                            continue;
                        }
                    }
                    //step7-3 - sxDrbInfoForMoveInReserveInq
                    log.info("step7-3 - sxDrbInfoForMoveInReserveInq");
                    Params.DurablesInfoForStartReservationInqInParam durablesInfoForStartReservationInqInParam = new Params.DurablesInfoForStartReservationInqInParam();
                    durablesInfoForStartReservationInqInParam.setDurableCategory(BizConstant.SP_DURABLECAT_CASSETTE);
                    durablesInfoForStartReservationInqInParam.setEquipmentID(equipmentID);
                    List<ObjectIdentifier> durableIDs = strStartDurable.stream().map(Infos.StartDurable::getDurableId).collect(Collectors.toList());
                    durablesInfoForStartReservationInqInParam.setDurableIDs(durableIDs);
                    Results.DurablesInfoForStartReservationInqResult durablesInfoForStartReservationInqResult = null;
                    try {
                        durablesInfoForStartReservationInqResult = durableInqService.sxDrbInfoForMoveInReserveInq(objCommonIn, durablesInfoForStartReservationInqInParam);
                    } catch (ServiceException e) {
                        log.info("sxDrbInfoForMoveInReserveInq Not OK");
                        e.setData(out);
                        throw e;
                    }
                    //Request to Start Lot Reservation
                    Infos.ObjCommon strTmpObjCommon = objCommonIn;
                    strTmpObjCommon.setTransactionID(TransactionIDEnum.START_DURABLES_RESERVATION_REQ.getValue());
                    Params.StartDurablesReservationReqInParam params = new Params.StartDurablesReservationReqInParam();
                    params.setEquipmentID(durablesInfoForStartReservationInqResult.getEquipmentID());
                    params.setDurableCategory(durablesInfoForStartReservationInqResult.getDurableCategory());
                    params.setStrDurableStartRecipe(durablesInfoForStartReservationInqResult.getStrDurableStartRecipe());
                    params.setStrStartDurables(strStartDurable);
                    params.setClaimMemo(null);

                    //step7-4 - sxDrbMoveInReserveReq
                    log.info("step7-4 - sxDrbMoveInReserveReq");
                    ObjectIdentifier durableControlJobID = null;
                    try {
                        durableControlJobID = durableService.sxDrbMoveInReserveReq(strTmpObjCommon, params,null);
                    } catch (ServiceException ex) {
                        log.info("sxDrbMoveInReserveReq() Not OK");
                        //Prepare e-mail Message Text
                        StringBuilder msg = new StringBuilder("<<< DrbMoveInReservation Error!  (Stocker -> EQP) >>>\n");
                        msg.append("\n")
                                .append("Equipment ID         : ").append(ObjectIdentifier.fetchValue(equipmentID)).append("\n")
                                .append("Durable IDs              : ");
                        Boolean bFirstSet = false;
                        if (CimArrayUtils.isNotEmpty(strStartDurable)){
                            for (Infos.StartDurable startDurable : strStartDurable) {
                                if (bFirstSet){
                                    msg.append(", ");
                                }
                                msg.append(ObjectIdentifier.fetchValue(startDurable.getDurableId()));
                                bFirstSet = true;
                            }
                        }
                        msg.append("\n")
                                .append("Transaction ID       : ").append(ex.getTransactionID()).append("\n")
                                .append("Return Code          : ").append(ex.getCode()).append("\n")
                                .append("Message Text         : ").append(ex.getMessage()).append("\n")
                                .append("Reason Text          : ").append(ex.getReasonText()).append("\n");
                        Infos.SysMsgStockInfo sysMsg = new Infos.SysMsgStockInfo();
                        sysMsg.setSubSystemID(BizConstant.SP_SUBSYSTEMID_MM);
                        sysMsg.setSystemMessageCode(BizConstant.SP_SYSTEMMSGCODE_DELIVERYERROR);
                        sysMsg.setSystemMessageText(msg.toString());
                        sysMsg.setNotifyFlag(true);
                        sysMsg.setEquipmentID(equipmentID);
                        sysMsg.setEquipmentStatus("");
                        sysMsg.setStockerID(dummy);
                        sysMsg.setStockerStatus("");
                        sysMsg.setAGVID(dummy);
                        sysMsg.setAGVStatus("");
                        sysMsg.setLotID(dummy);
                        sysMsg.setLotStatus("");
                        sysMsg.setRouteID(dummy);
                        sysMsg.setOperationID(dummy);
                        sysMsg.setOperationNumber("");
                        sysMsg.setSystemMessageTimeStamp(CimDateUtils.convertToSpecString(objCommonIn.getTimeStamp().getReportTimeStamp()));
                        List<Infos.SysMsgStockInfo> sysMstStockInfoList = CimArrayUtils.getSize(out.getSysMstStockInfoList()) == 0 ? new ArrayList<>() : out.getSysMstStockInfoList();
                        sysMstStockInfoList.add(sysMsg);
                        out.setSysMstStockInfoList(sysMstStockInfoList);
                        ex.setReasonText(msg.toString());
                        ex.setData(out);
                        throw ex;
                    }
                    //Make sxMultipleDurableTransferReq parameter
                    //step7-5 - multiDurableXferFillInOTMSW005InParm
                    log.info("step7-5 - multiDurableXferFillInOTMSW005InParm");
                    Outputs.ObjMultiDurableXferFillInOTMSW005InParmOut multiDurableXferFillInOTMSW005InParmRetCode = null;
                    try {
                        multiDurableXferFillInOTMSW005InParmRetCode = durableMethod.multiDurableXferFillInOTMSW005InParm(objCommonIn,
                                equipmentID, strStartDurable,durablesInfoForStartReservationInqResult.getDurableCategory());
                    } catch (ServiceException e) {
                        log.info("multiDurableXferFillInOTMSW005InParm() Not OK");
                        log.info("Request to Move In Reserve Cancel");
                        //step7-6 - sxDrbMoveInReserveCancelReq
                        log.info("step7-6 - sxDrbMoveInReserveCancelReq");
                        Params.StartDurablesReservationCancelReqInParam moveInReserveCancelReqParams = new Params.StartDurablesReservationCancelReqInParam();
                        moveInReserveCancelReqParams.setEquipmentID(equipmentID);
                        moveInReserveCancelReqParams.setDurableControlJobID(durableControlJobID);
                        try {
                            Results.StartDurablesReservationCancelReqResult startDurablesReservationCancelReqResult = durableService.sxDrbMoveInReserveCancelReq(objCommonIn, moveInReserveCancelReqParams, null);
                        } catch (ServiceException ex) {
                            log.info("sxDrbMoveInReserveCancelReq Not OK");
                        }
                        e.setData(out);
                        throw e;
                    }
                    Boolean bReRouteFlag = false;
                    String reRouteXferFlag = StandardProperties.OM_XFER_REROUTE_FLAG.getValue();
                    if (CimStringUtils.equals("1", reRouteXferFlag)) {
                        log.info("reRouteXferFlag is 1");
                        bReRouteFlag = true;
                    }
                    //Send Transfer Request to TMS. (Stocker -> EQP
                    //step7-7 - sxMultipleDurableTransferReq
                    log.info("step7-7 - sxMultipleDurableTransferReq");
                    List<Infos.DurableXferReq> strDurableXferReq = multiDurableXferFillInOTMSW005InParmRetCode.getStrDurableXferReq();
                    try {
                        this.sxMultipleDurableTransferReq(objCommonIn, bReRouteFlag, "S", strDurableXferReq,durablesInfoForStartReservationInqResult.getDurableCategory());
                    } catch (ServiceException e) {
                        log.info("sxMultipleDurableTransferReq() Not OK");
                        //Prepare e-mail Message Text
                        StringBuilder msg = new StringBuilder("<<< Transfer Error!  (Stocker -> EQP) >>>\n");
                        msg.append("\n")
                                .append("Equipment ID         : ").append(equipmentID.getValue()).append("\n");
                        if (CimArrayUtils.isNotEmpty(strDurableXferReq)){
                            for (Infos.DurableXferReq durableXferReq : strDurableXferReq) {
                                msg.append("-----------------------------------------\n")
                                        .append("Durable ID           : ").append(ObjectIdentifier.fetchValue(durableXferReq.getDurableID())).append("\n")
                                        .append("From Machine ID      : ").append(ObjectIdentifier.fetchValue(durableXferReq.getFromMachineID())).append("\n")
                                        .append("From Port ID         : ").append(ObjectIdentifier.fetchValue(durableXferReq.getFromPortID())).append("\n")
                                        .append("To Stocker Group     : ").append(durableXferReq.getToStockerGroup()).append("\n");
                                List<Infos.ToMachine> strToMachine = durableXferReq.getStrToMachine();
                                int lenToEqp = CimArrayUtils.getSize(strToMachine);
                                if (lenToEqp == 0){
                                    msg.append("To Machine ID        : Nothing\n");
                                }
                                for (Infos.ToMachine toMachine : strToMachine) {
                                    msg.append("To Machine ID        : ").append(ObjectIdentifier.fetchValue(toMachine.getToMachineID())).append("\n")
                                            .append("To Port ID           : ").append(ObjectIdentifier.fetchValue(toMachine.getToPortID())).append("\n");
                                }
                            }
                        }
                        msg.append("-----------------------------------------\n")
                                .append("Transaction ID       : ").append(e.getTransactionID()).append("\n")
                                .append("Return Code          : ").append(e.getCode()).append("\n")
                                .append("Message Text         : ").append(e.getMessage()).append("\n")
                                .append("Reason Text          : ").append(e.getReasonText()).append("\n");
                        Infos.SysMsgStockInfo sysMsg = new Infos.SysMsgStockInfo();
                        sysMsg.setSubSystemID(BizConstant.SP_SUBSYSTEMID_MM);
                        sysMsg.setSystemMessageCode(BizConstant.SP_SYSTEMMSGCODE_DELIVERYERROR);
                        sysMsg.setSystemMessageText(msg.toString());
                        sysMsg.setNotifyFlag(true);
                        sysMsg.setEquipmentID(equipmentID);
                        sysMsg.setEquipmentStatus("");
                        sysMsg.setStockerID(dummy);
                        sysMsg.setStockerStatus("");
                        sysMsg.setAGVID(dummy);
                        sysMsg.setAGVStatus("");
                        sysMsg.setLotID(dummy);
                        sysMsg.setLotStatus("");
                        sysMsg.setRouteID(dummy);
                        sysMsg.setOperationID(dummy);
                        sysMsg.setOperationNumber("");
                        sysMsg.setSystemMessageTimeStamp(CimDateUtils.convertToSpecString(objCommonIn.getTimeStamp().getReportTimeStamp()));
                        List<Infos.SysMsgStockInfo> sysMstStockInfoList = CimArrayUtils.getSize(out.getSysMstStockInfoList()) == 0 ? new ArrayList<>() : out.getSysMstStockInfoList();
                        sysMstStockInfoList.add(sysMsg);
                        out.setSysMstStockInfoList(sysMstStockInfoList);
                        //Request to MoveInReserve Cancel
                        //【step7-8】 - sxDrbMoveInReserveCancelReq
                        Params.StartDurablesReservationCancelReqInParam moveInReserveCancelReqParams = new Params.StartDurablesReservationCancelReqInParam();
                        moveInReserveCancelReqParams.setEquipmentID(equipmentID);
                        moveInReserveCancelReqParams.setDurableControlJobID(durableControlJobID);
                        try {
                            Results.StartDurablesReservationCancelReqResult startDurablesReservationCancelReqResult = durableService.sxDrbMoveInReserveCancelReq(objCommonIn, moveInReserveCancelReqParams, null);
                        } catch (ServiceException ex) {
                            log.info("sxDrbMoveInReserveCancelReq Not OK");
                        }
                        e.setData(out);
                        throw e;
                    }
                    //End of Process
                    log.info("OK! OK! OK! [Stocker to EQP]  Return to Main");
                    break;
                }
            }
        }
        // Return to Main
        return out;
    }

    @Override
    public Results.CarrierTransferReqResult sxDmsTransferForIBReq(Infos.ObjCommon objCommonIn, ObjectIdentifier equipmentID) {
        /*----------------*/
        /*   Initialize   */
        /*----------------*/
        Results.CarrierTransferReqResult out = new Results.CarrierTransferReqResult();
        out.setSysMstStockInfoList(new ArrayList<>());

        log.info("step1 - equipmentCategoryVsTxIDCheckCombination");
        //step1 - equipmentCategoryVsTxIDCheckCombination
        equipmentMethod.equipmentCategoryVsTxIDCheckCombination(objCommonIn, equipmentID);
        /******************************************************************************************/
        /*                                                                                        */
        /*     Pre-Check Process                                                                  */
        /*                                                                                        */
        /******************************************************************************************/

        /*--------------------------------*/
        /*   Get Equipment Infomation     */
        /*--------------------------------*/
        log.info("Get Equipment Infomation");
        log.info("step2 - sxEqpInfoForIBInq");
        //step2 - sxEqpInfoForIBInq
        Params.EqpInfoForIBInqParams param = new Params.EqpInfoForIBInqParams();
        param.setEquipmentID(equipmentID);
        param.setRequestFlagForBasicInfo(true);
        param.setRequestFlagForStatusInfo(true);
        param.setRequestFlagForPMInfo(false);
        param.setRequestFlagForPortInfo(true);
        param.setRequestFlagForChamberInfo(false);
        param.setRequestFlagForInternalBufferInfo(false);
        param.setRequestFlagForStockerInfo(false);
        param.setRequestFlagForInprocessingLotInfo(true);
        param.setRequestFlagForReservedControlJobInfo(false);
        param.setRequestFlagForRSPPortInfo(false);
        Results.EqpInfoForIBInqResult eqpInfoForIBInqResult = equipmentInqService.sxEqpInfoForIBInq(objCommonIn, param);

        /*-----------------------*/
        /*   Check Online Mode   */
        /*-----------------------*/
        log.info("Check Online Mode");
        int nILen = CimArrayUtils.getSize(eqpInfoForIBInqResult.getEquipmentPortInfo().getEqpPortStatuses());
        for (int i = 0; i < nILen; i++) {
            List<Infos.EqpPortStatus> content = eqpInfoForIBInqResult.getEquipmentPortInfo().getEqpPortStatuses();
            if (CimStringUtils.equals(BizConstant.SP_EQP_ONLINEMODE_OFFLINE, content.get(i).getOnlineMode())) {
                log.info("eqpInfoForIBInqResultRetCode.equipmentPortInfo.strEqpPortStatus[i].onlineMode = Off-Line");
                Validations.check(retCodeConfig.getInvalidEquipmentMode(), ObjectIdentifier.fetchValue(equipmentID),
                        content.get(i).getOnlineMode());
            }
        }
        /*--------------------------------*/
        /*   Check EQP's Available Flag   */
        /*--------------------------------*/
        log.info("Check EQP's Available Flag");
        if (CimBooleanUtils.isFalse(eqpInfoForIBInqResult.getEquipmentStatusInfo().getEquipmentAvailableFlag())) {
            log.info("equipmentAvailableFlag is not TRUE");
            throw new ServiceException(new OmCode(retCodeConfig.getInvalidEquipmentStatus(), equipmentID.getValue(), "NotAvailable"));
        }
        /*----------------------------------*/
        /*   Get Eqp Internal Buffer Info   */
        /*----------------------------------*/
        log.info("Get Eqp Internal Buffer Info ");
        log.info("step3 - equipmentInternalBufferInfoGet");
        //step3 - equipmentInternalBufferInfoGet
        List<Infos.EqpInternalBufferInfo> equipmentInternalBufferInfoGetResult = equipmentMethod.equipmentInternalBufferInfoGet(objCommonIn, equipmentID);
        /*------------------------*/
        /*   Pick Up Target Port  */
        /*------------------------*/
        log.info("Pick Up Target Port");
        log.info("step4 - equipmentTargetPortPickupForInternalBuffer");
        //step4 - equipmentTargetPortPickupForInternalBuffer
        Outputs.EquipmentTargetPortPickupOut equipmentTargetPortPickupForInternalBufferResult = equipmentMethod.equipmentTargetPortPickupForInternalBuffer(objCommonIn,
                equipmentID,
                equipmentInternalBufferInfoGetResult,
                eqpInfoForIBInqResult.getEquipmentPortInfo().getEqpPortStatuses());
        ObjectIdentifier dummy = new ObjectIdentifier();
        String tmpAPCIFControlStatus = null;
        /******************************************************************************************/
        /*                                                                                        */
        /*     UnloadReq Process                                                                  */
        /*                                                                                        */
        /******************************************************************************************/
        if (CimStringUtils.equals(BizConstant.SP_PORTRSC_PORTSTATE_UNLOADREQ, equipmentTargetPortPickupForInternalBufferResult.getTargetPortType())) {
            log.info("targetPortType == UnloadReq");
            if (CimBooleanUtils.isTrue(eqpInfoForIBInqResult.getEqpBasicInfoForInternalBuffer().isEqpToEqpTransferFlag())) {
                log.info("UnLoadReq Process RUN!!  EQP -> EQP");
                /*------------------------------*/
                /*   get whereNextTransferEqp   */
                /*------------------------------*/
                log.info("get whereNextTransferEqp");
                //step5 -durableWhereNextTransferEqp
                log.info("step5 -durableWhereNextTransferEqp");
                Boolean whereNextOKFlag = true;
                Outputs.DurableWhereNextTransferEqpOut durableWhereNextTransferEqpOut = null;
                try {
                    durableWhereNextTransferEqpOut = whereNextTransferEqp.durableWhereNextTransferEqp(objCommonIn,
                            equipmentID,
                            equipmentTargetPortPickupForInternalBufferResult);
                } catch (ServiceException e) {
                    whereNextOKFlag = false;
                    durableWhereNextTransferEqpOut = (Outputs.DurableWhereNextTransferEqpOut) e.getData();
                    log.info("EQP to EQP Xfer is interrupted, and change for the Stocker Xfer");
                }

                if (CimBooleanUtils.isTrue(whereNextOKFlag)){
                    //Request to durable move in reserve
                    //step5-1 - sxDrbInfoForMoveInReserveInq
                    log.info("step5-1 - sxDrbInfoForMoveInReserveInq");
                    Params.DurablesInfoForStartReservationInqInParam durablesInfoForStartReservationInqInParam = new Params.DurablesInfoForStartReservationInqInParam();
                    durablesInfoForStartReservationInqInParam.setDurableCategory(durableWhereNextTransferEqpOut.getDurableCategory());
                    durablesInfoForStartReservationInqInParam.setEquipmentID(durableWhereNextTransferEqpOut.getEquipmentID());
                    List<ObjectIdentifier> durableIDs = durableWhereNextTransferEqpOut.getStrStartDurables().stream().map(Infos.StartDurable::getDurableId).collect(Collectors.toList());
                    durablesInfoForStartReservationInqInParam.setDurableIDs(durableIDs);
                    Results.DurablesInfoForStartReservationInqResult durablesInfoForStartReservationInqResult = null;
                    try {
                        durablesInfoForStartReservationInqResult = durableInqService.sxDrbInfoForMoveInReserveInq(objCommonIn, durablesInfoForStartReservationInqInParam);
                    } catch (ServiceException e) {
                        log.info("sxDrbInfoForMoveInReserveInq Not OK");
                        e.setData(out);
                        throw e;
                    }
                    /*-----------------------------------------------------*/
                    /*   Request to DrbMoveInReserve                       */
                    /*                                                     */
                    /*   Not Support Xfer EQP -> EQP of InternalBuffer!!   */
                    /*   Therefore, call Normal Reservation.               */
                    /*-----------------------------------------------------*/
                    //step5-2 - sxDrbMoveInReq
                    log.info("step5-2 - sxDrbMoveInReq");
                    Infos.ObjCommon strTmpObjCommon = objCommonIn;
                    strTmpObjCommon.setTransactionID(TransactionIDEnum.START_DURABLES_RESERVATION_REQ.getValue());
                    Params.StartDurablesReservationReqInParam params = new Params.StartDurablesReservationReqInParam();
                    params.setEquipmentID(durablesInfoForStartReservationInqResult.getEquipmentID());
                    params.setDurableCategory(durablesInfoForStartReservationInqResult.getDurableCategory());
                    params.setStrDurableStartRecipe(durablesInfoForStartReservationInqResult.getStrDurableStartRecipe());
                    params.setStrStartDurables(durableWhereNextTransferEqpOut.getStrStartDurables());
                    params.setClaimMemo(null);
                    ObjectIdentifier durableControlJobID = null;
                    try {
                        durableControlJobID = durableService.sxDrbMoveInReserveReq(strTmpObjCommon, params,null);
                    } catch (ServiceException ex) {
                        log.info("sxDrbMoveInReserveReq() is Not OK");
                        //---------------------------------------------------------------------------------------------
                        //  Prepare message text string for case of failure of start lot reservation.
                        //  If start lot reservation for equipment is fail, this messageText is filled.
                        //---------------------------------------------------------------------------------------------
                        //Prepare e-mail Message Text
                        StringBuilder msg = new StringBuilder("<<< DrbMoveInReservation Error!  (EQP -> EQP) >>>\n");
                        msg.append("\n")
                                .append("Equipment ID         : ").append(ObjectIdentifier.fetchValue(durableWhereNextTransferEqpOut.getEquipmentID())).append("\n")
                                .append("Durable IDs              : ");
                        boolean bLotSet = false;
                        if (CimArrayUtils.isNotEmpty(durableWhereNextTransferEqpOut.getStrStartDurables())){
                            for (Infos.StartDurable strStartDurable : durableWhereNextTransferEqpOut.getStrStartDurables()) {
                                if (!ObjectIdentifier.isEmptyWithValue(strStartDurable.getDurableId())){
                                    if (CimBooleanUtils.isTrue(bLotSet)){
                                        msg.append(", ");
                                    }
                                    msg.append(ObjectIdentifier.fetchValue(strStartDurable.getDurableId()));
                                    bLotSet = true;
                                }
                            }
                        }
                        msg.append("\n")
                                .append("Transaction ID       : ").append(ex.getTransactionID()).append("\n")
                                .append("Return Code          : ").append(ex.getCode()).append("\n")
                                .append("Message Text         : ").append(ex.getMessage()).append("\n")
                                .append("Reason Text          : ").append(ex.getReasonText()).append("\n");
                        Infos.SysMsgStockInfo sysMsg = new Infos.SysMsgStockInfo();
                        sysMsg.setSubSystemID(BizConstant.SP_SUBSYSTEMID_MM);
                        sysMsg.setSystemMessageCode(BizConstant.SP_SYSTEMMSGCODE_DELIVERYERROR);
                        sysMsg.setSystemMessageText(msg.toString());
                        sysMsg.setNotifyFlag(true);
                        sysMsg.setEquipmentID(equipmentID);
                        sysMsg.setEquipmentStatus("");
                        sysMsg.setStockerID(dummy);
                        sysMsg.setStockerStatus("");
                        sysMsg.setAGVID(dummy);
                        sysMsg.setAGVStatus("");
                        sysMsg.setLotID(dummy);
                        sysMsg.setLotStatus("");
                        sysMsg.setRouteID(dummy);
                        sysMsg.setOperationID(dummy);
                        sysMsg.setOperationNumber("");
                        sysMsg.setSystemMessageTimeStamp(CimDateUtils.convertToSpecString(objCommonIn.getTimeStamp().getReportTimeStamp()));
                        List<Infos.SysMsgStockInfo> sysMstStockInfoList = CimArrayUtils.getSize(out.getSysMstStockInfoList()) == 0 ? new ArrayList<>() : out.getSysMstStockInfoList();
                        sysMstStockInfoList.add(sysMsg);
                        out.setSysMstStockInfoList(sysMstStockInfoList);
                        ex.setData(out);
                        throw ex;
                    }
                    log.info("sxDrbMoveInReq() Is OK");
                    //Make sxMultipleCarrierTransferReq parameter
                    //step5-3 - multiDurableXferFillInOTMSW005InParm
                    log.info("step5-3 - multiDurableXferFillInOTMSW005InParm");
                    Outputs.ObjMultiDurableXferFillInOTMSW005InParmOut multiDurableXferFillInOTMSW005InParmResult = null;
                    try {
                        multiDurableXferFillInOTMSW005InParmResult = durableMethod.multiDurableXferFillInOTMSW005InParm(objCommonIn,
                                durableWhereNextTransferEqpOut.getEquipmentID(), durableWhereNextTransferEqpOut.getStrStartDurables(),durablesInfoForStartReservationInqResult.getDurableCategory());
                    } catch (ServiceException e) {
                        log.info("multiDurableXferFillInOTMSW005InParm() Not OK");
                        //Request to Move In Reserve Cancel
                        //step5-4 - sxDrbMoveInCancelReq
                        log.info("step5-4 - sxDrbMoveInCancelReq");
                        Params.StartDurablesReservationCancelReqInParam in = new Params.StartDurablesReservationCancelReqInParam();
                        in.setEquipmentID(durableWhereNextTransferEqpOut.getEquipmentID());
                        in.setDurableControlJobID(durableControlJobID);
                        try {
                            Results.StartDurablesReservationCancelReqResult moveInReserveCancelReqResult = durableService.sxDrbMoveInReserveCancelReq(objCommonIn, in,null);
                        } catch (ServiceException ex) {
                            log.info("sxDrbMoveInCancelReq Not OK");
                        }
                        e.setData(out);
                        throw e;
                    }

                    //Send Transfer Request to TMS. (EQP -> EQP)
                    //step5-5 - sxMultipleDurableTransferReq
                    log.info("step5-5 - sxMultipleDurableTransferReq");
                    try {
                        this.sxMultipleDurableTransferReq(objCommonIn,
                                false, "S", multiDurableXferFillInOTMSW005InParmResult.getStrDurableXferReq(),durablesInfoForStartReservationInqResult.getDurableCategory());
                    } catch (ServiceException e) {
                        log.info("sxMultipleDurableTransferReq() Not OK");
                        //Prepare e-mail Message Text
                        StringBuilder msg = new StringBuilder("<<< Transfer Error!  (EQP -> EQP) >>>\n");
                        msg.append("\n")
                                .append("Equipment ID         : ").append(ObjectIdentifier.fetchValue(equipmentID)).append("\n");
                        if (CimArrayUtils.isNotEmpty(multiDurableXferFillInOTMSW005InParmResult.getStrDurableXferReq())){
                            for (Infos.DurableXferReq durableXferReq : multiDurableXferFillInOTMSW005InParmResult.getStrDurableXferReq()) {
                                msg.append("-----------------------------------------\n")
                                        .append("Durable ID           : ").append(ObjectIdentifier.fetchValue(durableXferReq.getDurableID())).append("\n")
                                        .append("From Machine ID      : ").append(ObjectIdentifier.fetchValue(durableXferReq.getFromMachineID())).append("\n")
                                        .append("From Port ID         : ").append(ObjectIdentifier.fetchValue(durableXferReq.getFromPortID())).append("\n")
                                        .append("To Stocker Group     : ").append(durableXferReq.getToStockerGroup()).append("\n");
                                int toMachineLen = CimArrayUtils.getSize(durableXferReq.getStrToMachine());
                                if (toMachineLen == 0){
                                    msg.append("To Machine ID        : Nothing\n");
                                }
                                for (Infos.ToMachine toMachine : durableXferReq.getStrToMachine()) {
                                    msg.append("To Machine ID        : ").append(ObjectIdentifier.fetchValue(toMachine.getToMachineID())).append("\n")
                                            .append("To Port ID           : ").append(ObjectIdentifier.fetchValue(toMachine.getToPortID())).append("\n");
                                }
                            }
                        }
                        msg.append("-----------------------------------------\n")
                                .append("Transaction ID       : ").append(e.getTransactionID()).append("\n")
                                .append("Return Code          : ").append(e.getCode()).append("\n")
                                .append("Message Text         : ").append(e.getMessage()).append("\n")
                                .append("Reason Text          : ").append(e.getReasonText()).append("\n");
                        //Set System Message
                        Infos.SysMsgStockInfo sysMsg = new Infos.SysMsgStockInfo();
                        sysMsg.setSubSystemID(BizConstant.SP_SUBSYSTEMID_MM);
                        sysMsg.setSystemMessageCode(BizConstant.SP_SYSTEMMSGCODE_DELIVERYERROR);
                        sysMsg.setSystemMessageText(msg.toString());
                        sysMsg.setNotifyFlag(true);
                        sysMsg.setEquipmentID(equipmentID);
                        sysMsg.setEquipmentStatus("");
                        sysMsg.setStockerID(dummy);
                        sysMsg.setStockerStatus("");
                        sysMsg.setAGVID(dummy);
                        sysMsg.setAGVStatus("");
                        sysMsg.setLotID(dummy);
                        sysMsg.setLotStatus("");
                        sysMsg.setRouteID(dummy);
                        sysMsg.setOperationID(dummy);
                        sysMsg.setOperationNumber("");
                        sysMsg.setSystemMessageTimeStamp(CimDateUtils.convertToSpecString(objCommonIn.getTimeStamp().getReportTimeStamp()));
                        List<Infos.SysMsgStockInfo> sysMstStockInfoList = CimArrayUtils.getSize(out.getSysMstStockInfoList()) == 0 ? new ArrayList<>() : out.getSysMstStockInfoList();
                        sysMstStockInfoList.add(sysMsg);
                        out.setSysMstStockInfoList(sysMstStockInfoList);
                        //Request to MoveInReserve Cancel
                        //step5-6 - sxDrbMoveInCancelReq
                        log.info("step5-6 - sxDrbMoveInCancelReq");
                        Params.StartDurablesReservationCancelReqInParam moveInReserveCancelReqParams = new Params.StartDurablesReservationCancelReqInParam();
                        moveInReserveCancelReqParams.setEquipmentID(durableWhereNextTransferEqpOut.getEquipmentID());
                        moveInReserveCancelReqParams.setDurableControlJobID(durableControlJobID);
                        try {
                            Results.StartDurablesReservationCancelReqResult moveInReserveCancelReqResultRetCode = durableService.sxDrbMoveInReserveCancelReq(objCommonIn, moveInReserveCancelReqParams,null);
                        } catch (ServiceException ex) {
                            log.info("sxDrbMoveInCancelReq Not OK");
                        }
                        e.setData(out);
                        throw e;
                    }
                    /*--------------------*/
                    /*   Return to Main   */
                    /*--------------------*/
                    log.info("OK! OK! OK! [EQP to EQP]  Return to Main");
                    return out;
                }
            }
            log.info("UnLoadReq Process RUN!!");
            List<Infos.PortID> strPortIDs = equipmentTargetPortPickupForInternalBufferResult.getEqpTargetPortInfo().getPortGroups().get(0).getStrPortID();
            int nPortLen = CimArrayUtils.getSize(strPortIDs);
            for (int i = 0; i < nPortLen; i++) {
                //Get Where Next Stocker
                //Call RTD Interface
                //step6 - cassetteDelivery_RTDInterfaceReq
                log.info("step6 - cassetteDelivery_RTDInterfaceReq");
                Results.DurableWhereNextStockerInqResult durableWhereNextStockerInqResult = null;
                Outputs.ObjDurableDeliveryRTDInterfaceReqOut durableDeliveryRTDInterfaceReqResult = null;
                try {
                    durableDeliveryRTDInterfaceReqResult = durableMethod.durableDeliveryRTDInterfaceReq(objCommonIn,
                            BizConstant.SP_RTD_FUNCTION_CODE_WHERENEXT, strPortIDs.get(i).getCassetteID());
                    durableWhereNextStockerInqResult = durableDeliveryRTDInterfaceReqResult == null ? null : durableDeliveryRTDInterfaceReqResult.getStrWhereNextStockerInqResult();
                } catch (ServiceException e) {
                    if (!Validations.isEquals(retCodeConfigEx.getRtdInterfaceSwitchOff(), e.getCode())
                            && !Validations.isEquals(retCodeConfigEx.getNotFoundRTD(), e.getCode())) {
                        StringBuilder msg = new StringBuilder("<<< RTD Interface Error!  (WhereNext of DurableDelivery) >>>\n");
                        msg.append("\n")
                                .append("Equipment ID         : ").append(ObjectIdentifier.fetchValue(equipmentID)).append("\n")
                                .append("-----------------------------------------\n")
                                .append("Transaction ID       : ").append(e.getTransactionID()).append("\n")
                                .append("Return Code          : ").append(e.getCode()).append("\n")
                                .append(e.getMessage()).append("\n")
                                .append("Reason Text          : ").append(e.getReasonText()).append("\n");
                        //Set System Message
                        Infos.SysMsgStockInfo sysMsg = new Infos.SysMsgStockInfo();
                        sysMsg.setSubSystemID(BizConstant.SP_SUBSYSTEMID_MM);
                        sysMsg.setSystemMessageCode(BizConstant.SP_SYSTEMMSGCODE_RTDERROR);
                        sysMsg.setSystemMessageText(msg.toString());// up on the msg but not add
                        sysMsg.setNotifyFlag(true);
                        sysMsg.setEquipmentID(equipmentID);
                        sysMsg.setEquipmentStatus("");
                        sysMsg.setStockerID(dummy);
                        sysMsg.setStockerStatus("");
                        sysMsg.setAGVID(dummy);
                        sysMsg.setAGVStatus("");
                        sysMsg.setLotID(dummy);
                        sysMsg.setLotStatus("");
                        sysMsg.setRouteID(dummy);
                        sysMsg.setOperationID(dummy);
                        sysMsg.setOperationNumber("");
                        sysMsg.setSystemMessageTimeStamp(CimDateUtils.convertToSpecString(objCommonIn.getTimeStamp().getReportTimeStamp()));
                        List<Infos.SysMsgStockInfo> sysMstStockInfoList = CimArrayUtils.getSize(out.getSysMstStockInfoList()) == 0 ? new ArrayList<>() : out.getSysMstStockInfoList();
                        sysMstStockInfoList.add(sysMsg);
                        out.setSysMstStockInfoList(sysMstStockInfoList);
                    }
                    //Call Normal Function (sxDurableWhereNextStockerInq)
                    //step6-1 - sxDurableWhereNextStockerInq
                    log.info("step6-1 - sxDurableWhereNextStockerInq");
                    try {
                        durableWhereNextStockerInqResult = transferManagementSystemInqService.sxDurableWhereNextStockerInq(objCommonIn,
                                strPortIDs.get(i).getCassetteID());
                    } catch (ServiceException ex) {
                        if (!Validations.isSuccess(ex.getCode())) {
                            StringBuilder msg = new StringBuilder("<<< WhereNext Error!  (EQP -> Stocker) >>>\n");
                            msg.append("\n")
                                    .append("Equipment ID         : ").append(ObjectIdentifier.fetchValue(equipmentID)).append("\n")
                                    .append("-----------------------------------------\n")
                                    .append("Transaction ID       : ").append(ex.getTransactionID()).append("\n")
                                    .append("Return Code          : ").append(ex.getCode()).append("\n")
                                    .append("Message Text         : ").append(ex.getMessage()).append("\n")
                                    .append("Reason Text          : ").append(ex.getReasonText()).append("\n");
                            //Set System Message
                            Infos.SysMsgStockInfo sysMsg = new Infos.SysMsgStockInfo();
                            sysMsg.setSubSystemID(BizConstant.SP_SUBSYSTEMID_MM);
                            sysMsg.setSystemMessageCode(BizConstant.SP_SYSTEMMSGCODE_DELIVERYERROR);
                            sysMsg.setSystemMessageText(msg.toString());
                            sysMsg.setNotifyFlag(true);
                            sysMsg.setEquipmentID(equipmentID);
                            sysMsg.setEquipmentStatus("");
                            sysMsg.setStockerID(dummy);
                            sysMsg.setStockerStatus("");
                            sysMsg.setAGVID(dummy);
                            sysMsg.setAGVStatus("");
                            sysMsg.setLotID(dummy);
                            sysMsg.setLotStatus("");
                            sysMsg.setRouteID(dummy);
                            sysMsg.setOperationID(dummy);
                            sysMsg.setOperationNumber("");
                            sysMsg.setSystemMessageTimeStamp(CimDateUtils.convertToSpecString(objCommonIn.getTimeStamp().getReportTimeStamp()));
                            List<Infos.SysMsgStockInfo> sysMstStockInfoList = CimArrayUtils.getSize(out.getSysMstStockInfoList()) == 0 ? new ArrayList<>() : out.getSysMstStockInfoList();
                            sysMstStockInfoList.add(sysMsg);
                            out.setSysMstStockInfoList(sysMstStockInfoList);
                            ex.setData(out);
                            throw ex;
                        }
                    }
                }
                //Make txSingleCarrierTransferReq parameter
                //step6-2 - singleDurableXferFillInOTMSW006InParm
                log.info("step6-2 - singleDurableXferFillInOTMSW006InParm");
                Outputs.ObjSingleDurableXferFillInOTMSW006InParmOut singleDurableXferFillInOTMSW006InParmResult = null;
                try {
                    if (durableWhereNextStockerInqResult != null) {
                        singleDurableXferFillInOTMSW006InParmResult = durableMethod.singleDurableXferFillInOTMSW006InParm(objCommonIn,
                                equipmentID, strPortIDs.get(i).getPortID(),
                                strPortIDs.get(i).getCassetteID(),
                                durableWhereNextStockerInqResult,durableWhereNextStockerInqResult.getDurableCategory());
                    }
                } catch (ServiceException e) {
                    if (Validations.isEquals(retCodeConfigEx.getNoXferNeeded(), e.getCode())) {
                        log.info("No Transfer Job is requested by environment variable. Proceed to the Next Port...");
                        continue;
                    } else if (Validations.isEquals(retCodeConfigEx.getNoStockerForCurrentEqp(), e.getCode())) {
                        singleDurableXferFillInOTMSW006InParmResult = e.getData(Outputs.ObjSingleDurableXferFillInOTMSW006InParmOut.class);
                        Infos.DurableXferReq durableXferReq = singleDurableXferFillInOTMSW006InParmResult.getStrDurableXferReq();
                        log.info("An Error is Detected. Proceed to the Next Port...");
                        //Prepare e-mail Message Text
                        StringBuilder msg = new StringBuilder("<<< Transfer Error!  (EQP -> Stocker) >>>\n");
                        msg.append("\n")
                                .append("Equipment ID         : ").append(ObjectIdentifier.fetchValue(equipmentID)).append("\n")
                                .append("-----------------------------------------\n")
                                .append("durable ID           : ").append(ObjectIdentifier.fetchValue(durableXferReq.getDurableID())).append("\n")
                                .append("From Machine ID      : ").append(ObjectIdentifier.fetchValue(durableXferReq.getFromMachineID())).append("\n")
                                .append("From Port ID         : ").append(ObjectIdentifier.fetchValue(durableXferReq.getFromPortID())).append("\n")
                                .append("To Stocker Group     : ").append(durableXferReq.getToStockerGroup()).append("\n");
                        int toMachineLen = CimArrayUtils.getSize(durableXferReq.getStrToMachine());
                        if (toMachineLen == 0){
                            msg.append("To Machine ID        : Nothing\n");
                        }
                        for (Infos.ToMachine toMachine : durableXferReq.getStrToMachine()) {
                            msg.append("To Machine ID        : ").append(ObjectIdentifier.fetchValue(toMachine.getToMachineID())).append("\n")
                                    .append("To Port ID           : ").append(ObjectIdentifier.fetchValue(toMachine.getToPortID())).append("\n");
                        }

                        msg.append("-----------------------------------------\n")
                                .append("Transaction ID       : ").append(e.getTransactionID()).append("\n")
                                .append("Return Code          : ").append(e.getCode()).append("\n")
                                .append("Message Text         : ").append(e.getMessage()).append("\n")
                                .append("Reason Text          : ").append(e.getReasonText()).append("\n");
                        //Set System Message
                        Infos.SysMsgStockInfo sysMsg = new Infos.SysMsgStockInfo();
                        sysMsg.setSubSystemID(BizConstant.SP_SUBSYSTEMID_MM);
                        sysMsg.setSystemMessageCode(BizConstant.SP_SYSTEMMSGCODE_DELIVERYERROR);
                        sysMsg.setSystemMessageText(msg.toString());
                        sysMsg.setNotifyFlag(true);
                        sysMsg.setEquipmentID(equipmentID);
                        sysMsg.setEquipmentStatus("");
                        sysMsg.setStockerID(dummy);
                        sysMsg.setStockerStatus("");
                        sysMsg.setAGVID(dummy);
                        sysMsg.setAGVStatus("");
                        sysMsg.setLotID(dummy);
                        sysMsg.setLotStatus("");
                        sysMsg.setRouteID(dummy);
                        sysMsg.setOperationID(dummy);
                        sysMsg.setOperationNumber("");
                        sysMsg.setSystemMessageTimeStamp(CimDateUtils.convertToSpecString(objCommonIn.getTimeStamp().getReportTimeStamp()));
                        List<Infos.SysMsgStockInfo> sysMstStockInfoList = CimArrayUtils.getSize(out.getSysMstStockInfoList()) == 0 ? new ArrayList<>() : out.getSysMstStockInfoList();
                        sysMstStockInfoList.add(sysMsg);
                        out.setSysMstStockInfoList(sysMstStockInfoList);
                        continue;
                    } else {
                        e.setData(out);
                        throw e;
                    }
                }
                // Debug Trace of TransferInfo
                //Check whether specified machine is eqp or stocker
                Boolean isStorageFlag = false;
                //step6-3 - equipmentStatusInfoGet
                log.info("step6-3 - equipmentStatusInfoGet");
                Infos.EqpStatusInfo eqpStatusInfoRetCode = null;
                try {
                    if (null != singleDurableXferFillInOTMSW006InParmResult) {
                        eqpStatusInfoRetCode = equipmentMethod.equipmentStatusInfoGet(objCommonIn, singleDurableXferFillInOTMSW006InParmResult.getStrDurableXferReq().getFromMachineID());
                    }
                } catch (ServiceException e) {
                    if (Validations.isEquals(retCodeConfig.getNotFoundEqp(), e.getCode())) {
                        isStorageFlag = true;
                    } else {
                        e.setData(out);
                        throw e;
                    }
                }
                if (null != singleDurableXferFillInOTMSW006InParmResult) {
                    log.info("Equipment is Storage or not == {},{}", ObjectIdentifier.fetchValue(singleDurableXferFillInOTMSW006InParmResult.getStrDurableXferReq().getFromMachineID()), isStorageFlag);
                }
                if (CimBooleanUtils.isFalse(isStorageFlag)) {
                    // Lock Port Object Of machine
                    //step6-4 - objectLockForEquipmentResource
                    log.info("step6-4 - objectLockForEquipmentResource");
                    try {
                        if (null != singleDurableXferFillInOTMSW006InParmResult) {
                            lockMethod.objectLockForEquipmentResource(objCommonIn,singleDurableXferFillInOTMSW006InParmResult.getStrDurableXferReq().getFromMachineID(),
                                    singleDurableXferFillInOTMSW006InParmResult.getStrDurableXferReq().getFromPortID(),
                                    BizConstant.SP_CLASSNAME_POSPORTRESOURCE);
                        }
                    } catch (ServiceException e) {
                        e.setData(out);
                        throw e;
                    }
                    //Check Equipment Port for Sending Request to TMS. (EQP -> Stocker)
                    //step6-5 - equipmentPortStateCheckForCassetteDelivery
                    log.info("step6-5 - equipmentPortStateCheckForCassetteDelivery");
                    Inputs.ObjEquipmentPortStateCheckForCassetteDeliveryIn deliveryIn = new Inputs.ObjEquipmentPortStateCheckForCassetteDeliveryIn();
                    if (null != singleDurableXferFillInOTMSW006InParmResult) {
                        deliveryIn.setEquipmentID(singleDurableXferFillInOTMSW006InParmResult.getStrDurableXferReq().getFromMachineID());
                        deliveryIn.setPortID(singleDurableXferFillInOTMSW006InParmResult.getStrDurableXferReq().getFromPortID());
                    }
                    try {
                        equipmentMethod.equipmentPortStateCheckForCassetteDelivery(objCommonIn, deliveryIn);
                    } catch (ServiceException e) {
                        e.setData(out);
                        throw e;
                    }
                }
                //Send Request to TMS. (EQP -> Stocker)
                Params.SingleDurableTransferReqParam singleDurableTransferReqParam = new Params.SingleDurableTransferReqParam();
                Infos.DurableXferReq strDurableXferReq = null;
                if (null != singleDurableXferFillInOTMSW006InParmResult) {
                    singleDurableTransferReqParam.setRerouteFlag(singleDurableXferFillInOTMSW006InParmResult.getRerouteFlag());
                    strDurableXferReq = singleDurableXferFillInOTMSW006InParmResult.getStrDurableXferReq();
                }
                if (null != strDurableXferReq) {
                    singleDurableTransferReqParam.setDurableID(strDurableXferReq.getDurableID());
                    singleDurableTransferReqParam.setZoneType(strDurableXferReq.getZoneType());
                    singleDurableTransferReqParam.setN2PurgeFlag(strDurableXferReq.getN2PurgeFlag());
                    singleDurableTransferReqParam.setFromMachineID(strDurableXferReq.getFromMachineID());
                    singleDurableTransferReqParam.setFromPortID(strDurableXferReq.getFromPortID());
                    singleDurableTransferReqParam.setToStockerGroup(strDurableXferReq.getToStockerGroup());
                    singleDurableTransferReqParam.setToMachine(strDurableXferReq.getStrToMachine());
                    singleDurableTransferReqParam.setExpectedStartTime(strDurableXferReq.getExpectedStartTime());
                    singleDurableTransferReqParam.setExpectedEndTime(strDurableXferReq.getExpectedEndTime());
                    singleDurableTransferReqParam.setMandatoryFlag(strDurableXferReq.getMandatoryFlag());
                    singleDurableTransferReqParam.setPriority(strDurableXferReq.getPriority());
                }
                //step6-6 - sxSingleDurableTransferReq
                log.info("step6-6 - sxSingleDurableTransferReq");
                try {
                    if (null != durableWhereNextStockerInqResult) {
                        this.sxSingleDurableTransferReq(objCommonIn, singleDurableTransferReqParam,durableWhereNextStockerInqResult.getDurableCategory());
                    }
                } catch (ServiceException e) {
                    log.info("sxSingleDurableTransferReq() Not OK");
                    //Prepare e-mail Message Text
                    StringBuilder msg = new StringBuilder("<<< Transfer Error!  (EQP -> Stocker) >>>\n");
                    msg.append("\n")
                            .append("Equipment ID         : ").append(ObjectIdentifier.fetchValue(equipmentID)).append("\n")
                            .append("-----------------------------------------\n")
                            .append("Durable ID           : ").append(ObjectIdentifier.fetchValue(strDurableXferReq.getDurableID())).append("\n")
                            .append("From Machine ID      : ").append(ObjectIdentifier.fetchValue(strDurableXferReq.getFromMachineID())).append("\n")
                            .append("From Port ID         : ").append(ObjectIdentifier.fetchValue(strDurableXferReq.getFromPortID())).append("\n")
                            .append("To Stocker Group     : ").append(strDurableXferReq.getToStockerGroup()).append("\n");
                    int toMachineLen = CimArrayUtils.getSize(strDurableXferReq.getStrToMachine());
                    if (toMachineLen == 0){
                        msg.append("To Machine ID        : Nothing\n");
                    }
                    for (Infos.ToMachine toMachine : strDurableXferReq.getStrToMachine()) {
                        msg.append("To Machine ID        : ").append(ObjectIdentifier.fetchValue(toMachine.getToMachineID())).append("\n")
                                .append("To Port ID           : ").append(ObjectIdentifier.fetchValue(toMachine.getToPortID())).append("\n");
                    }
                    msg.append("-----------------------------------------\n")
                            .append("Transaction ID       : ").append(e.getTransactionID()).append("\n")
                            .append("Return Code          : ").append(e.getCode()).append("\n")
                            .append("Message Text         : ").append(e.getMessage()).append("\n")
                            .append("Reason Text          : ").append(e.getReasonText()).append("\n");
                    //Set System Message
                    Infos.SysMsgStockInfo sysMsg = new Infos.SysMsgStockInfo();
                    sysMsg.setSubSystemID(BizConstant.SP_SUBSYSTEMID_MM);
                    sysMsg.setSystemMessageCode(BizConstant.SP_SYSTEMMSGCODE_DELIVERYERROR);
                    sysMsg.setSystemMessageText(msg.toString());
                    sysMsg.setNotifyFlag(true);
                    sysMsg.setEquipmentID(equipmentID);
                    sysMsg.setEquipmentStatus("");
                    sysMsg.setStockerID(dummy);
                    sysMsg.setStockerStatus("");
                    sysMsg.setAGVID(dummy);
                    sysMsg.setAGVStatus("");
                    sysMsg.setLotID(dummy);
                    sysMsg.setLotStatus("");
                    sysMsg.setRouteID(dummy);
                    sysMsg.setOperationID(dummy);
                    sysMsg.setOperationNumber("");
                    sysMsg.setSystemMessageTimeStamp(CimDateUtils.convertToSpecString(objCommonIn.getTimeStamp().getReportTimeStamp()));
                    List<Infos.SysMsgStockInfo> sysMstStockInfoList = CimArrayUtils.getSize(out.getSysMstStockInfoList()) == 0 ? new ArrayList<>() : out.getSysMstStockInfoList();
                    sysMstStockInfoList.add(sysMsg);
                    out.setSysMstStockInfoList(sysMstStockInfoList);
                    e.setData(out);
                    throw e;
                }
            }
            /*--------------------*/
            /*   Return to Main   */
            /*--------------------*/
            log.info("OK! OK! OK! UnloadReq [EQP to Stocker]  Return to Main");
            return out;
        }
        /******************************************************************************************/
        /*                                                                                        */
        /*     LoadReq Process                                                                    */
        /*                                                                                        */
        /******************************************************************************************/
        if (CimStringUtils.equals(BizConstant.SP_PORTRSC_PORTSTATE_LOADREQ, equipmentTargetPortPickupForInternalBufferResult.getTargetPortType())) {
            /*-----------------*/
            /*   Load Request  */
            /*-----------------*/
            log.info("LoadReq Process RUN!!");
            if (CimBooleanUtils.isFalse(eqpInfoForIBInqResult.getEquipmentStatusInfo().getEquipmentAvailableFlag())) {
                log.info("equipmentAvailableFlag is Not TRUE");
                throw new ServiceException(new OmCode(retCodeConfig.getInvalidEquipmentStatus(), equipmentID.getValue(), "NotAvailable"));
            }
            /*------------------*/
            /*   Get WIP Lots   */
            /*------------------*/
            log.info("Get WIP Lots");
            Results.WhatNextDurableListInqResult durableWhatNextInqResult = null;
            //Call RTD Interface
            //step7 - durableDeliveryRTDInterfaceReq
            log.info("step7 - durableDeliveryRTDInterfaceReq");
            Outputs.ObjDurableDeliveryRTDInterfaceReqOut durableDeliveryRTDInterfaceReqResult = null;
            try {
                durableDeliveryRTDInterfaceReqResult = durableMethod.durableDeliveryRTDInterfaceReq(objCommonIn, BizConstant.SP_RTD_FUNCTION_CODE_WHATNEXT, equipmentID);
                log.info("Normal End RTD Interface of sxDrbWhatNextListInq");
                durableWhatNextInqResult = null == durableDeliveryRTDInterfaceReqResult ? null : durableDeliveryRTDInterfaceReqResult.getStrWhatNextInqResult();
            } catch (ServiceException e) {
                log.info("Faild RTD Interface Call Normal Function (sxDrbWhatNextListInq)");
                if (!Validations.isEquals(retCodeConfigEx.getRtdInterfaceSwitchOff(), e.getCode())
                        && !Validations.isEquals(retCodeConfigEx.getNotFoundRTD(), e.getCode())) {
                    //Set System Message
                    StringBuilder msg = new StringBuilder("<<< RTD Interface Error!  (WhatNext of DurableDelivery) >>>\n");
                    msg.append("\n")
                            .append("Equipment ID         : ").append(ObjectIdentifier.fetchValue(equipmentID)).append("\n")
                            .append("-----------------------------------------\n")
                            .append("Transaction ID       : ").append(e.getTransactionID()).append("\n")
                            .append("Return Code          : ").append(e.getCode()).append("\n")
                            .append("Message Text         : ").append(e.getMessage()).append("\n")
                            .append("Reason Text          : ").append(e.getReasonText()).append("\n");
                    Infos.SysMsgStockInfo sysMsg = new Infos.SysMsgStockInfo();
                    sysMsg.setSubSystemID(BizConstant.SP_SUBSYSTEMID_MM);
                    sysMsg.setSystemMessageCode(BizConstant.SP_SYSTEMMSGCODE_RTDERROR);
                    sysMsg.setSystemMessageText(msg.toString());
                    sysMsg.setNotifyFlag(true);
                    sysMsg.setEquipmentID(equipmentID);
                    sysMsg.setEquipmentStatus("");
                    sysMsg.setStockerID(dummy);
                    sysMsg.setStockerStatus("");
                    sysMsg.setAGVID(dummy);
                    sysMsg.setAGVStatus("");
                    sysMsg.setLotID(dummy);
                    sysMsg.setLotStatus("");
                    sysMsg.setRouteID(dummy);
                    sysMsg.setOperationID(dummy);
                    sysMsg.setOperationNumber("");
                    sysMsg.setSystemMessageTimeStamp(CimDateUtils.convertToSpecString(objCommonIn.getTimeStamp().getReportTimeStamp()));
                    List<Infos.SysMsgStockInfo> sysMstStockInfoList = CimArrayUtils.getSize(out.getSysMstStockInfoList()) == 0 ? new ArrayList<>() : out.getSysMstStockInfoList();
                    sysMstStockInfoList.add(sysMsg);
                    out.setSysMstStockInfoList(sysMstStockInfoList);
                }
                //Call Normal Function (sxDrbWhatNextListInq)
                Params.WhatNextDurableListInqInParam whatNextDurableListInqInParam = new Params.WhatNextDurableListInqInParam();
                whatNextDurableListInqInParam.setEquipmentID(equipmentID);
                whatNextDurableListInqInParam.setSelectCriteria(BizConstant.SP_DP_SELECTCRITERIA_AUTO3);
                //step7-1 - sxDrbWhatNextListInq
                log.info("step7-1 - sxDrbWhatNextListInq");
                try {
                    durableWhatNextInqResult = durableInqService.sxDrbWhatNextListInq(objCommonIn,whatNextDurableListInqInParam);
                } catch (ServiceException ex) {
                    if (!Validations.isEquals(retCodeConfigEx.getNoWipLot(), ex.getCode())) {
                        //Set System Message
                        StringBuilder msg = new StringBuilder("<<< WhatNext Error!  (Stocker -> EQP) >>>\n");
                        msg.append("\n")
                                .append("Equipment ID         : ").append(ObjectIdentifier.fetchValue(equipmentID)).append("\n")
                                .append("-----------------------------------------\n")
                                .append("Transaction ID       : ").append(ex.getTransactionID()).append("\n")
                                .append("Return Code          : ").append(ex.getCode()).append("\n")
                                .append("Message Text         : ").append(ex.getMessage()).append("\n")
                                .append("Reason Text          : ").append(ex.getReasonText()).append("\n");
                        Infos.SysMsgStockInfo sysMsg = new Infos.SysMsgStockInfo();
                        sysMsg.setSubSystemID(BizConstant.SP_SUBSYSTEMID_MM);
                        sysMsg.setSystemMessageCode(BizConstant.SP_SYSTEMMSGCODE_DELIVERYERROR);
                        sysMsg.setSystemMessageText(msg.toString());
                        sysMsg.setNotifyFlag(true);
                        sysMsg.setEquipmentID(equipmentID);
                        sysMsg.setEquipmentStatus("");
                        sysMsg.setStockerID(dummy);
                        sysMsg.setStockerStatus("");
                        sysMsg.setAGVID(dummy);
                        sysMsg.setAGVStatus("");
                        sysMsg.setLotID(dummy);
                        sysMsg.setLotStatus("");
                        sysMsg.setRouteID(dummy);
                        sysMsg.setOperationID(dummy);
                        sysMsg.setOperationNumber("");
                        sysMsg.setSystemMessageTimeStamp(CimDateUtils.convertToSpecString(objCommonIn.getTimeStamp().getReportTimeStamp()));
                        List<Infos.SysMsgStockInfo> sysMstStockInfoList = CimArrayUtils.getSize(out.getSysMstStockInfoList()) == 0 ? new ArrayList<>() : out.getSysMstStockInfoList();
                        sysMstStockInfoList.add(sysMsg);
                        out.setSysMstStockInfoList(sysMstStockInfoList);
                    }
                    ex.setData(out);
                    throw ex;
                }
            }
            /*--------------------------*/
            /*   Select Multiple Lots   */
            /*--------------------------*/
            log.info("Select Multiple Lots ");
            log.info("step7-2 - whatNextDurableListToStartDurableForDeliverForInternalBufferReq");
            Results.WhatNextDurableListInqResult convWhatNextInqResult = durableWhatNextInqResult;
            List<Infos.StartDurable> strStartDurable = null;
            try {
                strStartDurable = whatNextMethod.whatNextDurableListToStartDurableForDeliveryForInternalBufferReq(objCommonIn,
                        equipmentID,
                        equipmentTargetPortPickupForInternalBufferResult,
                        convWhatNextInqResult,
                        true,
                        equipmentInternalBufferInfoGetResult);
            } catch (ServiceException e) {
                log.info("whatNextDurableListToStartDurableForDeliveryForInternalBufferReq is Not OK");
                e.setData(out);
                throw e;
            }
            //step7-3 - sxDrbInfoForMoveInReserveInq
            log.info("step7-3 - sxDrbInfoForMoveInReserveInq");
            Params.DurablesInfoForStartReservationInqInParam durablesInfoForStartReservationInqInParam = new Params.DurablesInfoForStartReservationInqInParam();
            durablesInfoForStartReservationInqInParam.setDurableCategory(BizConstant.SP_DURABLECAT_CASSETTE);
            durablesInfoForStartReservationInqInParam.setEquipmentID(equipmentID);
            List<ObjectIdentifier> durableIDs = strStartDurable.stream().map(Infos.StartDurable::getDurableId).collect(Collectors.toList());
            durablesInfoForStartReservationInqInParam.setDurableIDs(durableIDs);
            Results.DurablesInfoForStartReservationInqResult durablesInfoForStartReservationInqResult = null;
            try {
                durablesInfoForStartReservationInqResult = durableInqService.sxDrbInfoForMoveInReserveInq(objCommonIn, durablesInfoForStartReservationInqInParam);
            } catch (ServiceException e) {
                log.info("sxDrbInfoForMoveInReserveInq Not OK");
                e.setData(out);
                throw e;
            }
            //Request to Start Lot Reservation
            Infos.ObjCommon strTmpObjCommon = objCommonIn;
            strTmpObjCommon.setTransactionID(TransactionIDEnum.START_DURABLES_RESERVATION_REQ.getValue());
            Params.StartDurablesReservationReqInParam params = new Params.StartDurablesReservationReqInParam();
            params.setEquipmentID(durablesInfoForStartReservationInqResult.getEquipmentID());
            params.setDurableCategory(durablesInfoForStartReservationInqResult.getDurableCategory());
            params.setStrDurableStartRecipe(durablesInfoForStartReservationInqResult.getStrDurableStartRecipe());
            params.setStrStartDurables(strStartDurable);
            params.setClaimMemo(null);

            //step7-4 - sxDrbMoveInReserveForIBReq
            log.info("step7-4 - sxDrbMoveInReserveForIBReq");
            ObjectIdentifier durableControlJobID = null;
            try {
                durableControlJobID = durableService.sxDrbMoveInReserveForIBReq(strTmpObjCommon, params,null);
            } catch (ServiceException ex) {
                log.info("sxDrbMoveInReserveReq() Not OK");
                //Prepare e-mail Message Text
                StringBuilder msg = new StringBuilder("<<< DrbMoveInReservation Error!  (Stocker -> EQP) >>>\n");
                msg.append("\n")
                        .append("Equipment ID         : ").append(ObjectIdentifier.fetchValue(equipmentID)).append("\n")
                        .append("Durable IDs              : ");
                Boolean bFirstSet = false;
                if (CimArrayUtils.isNotEmpty(strStartDurable)){
                    for (Infos.StartDurable startDurable : strStartDurable) {
                        if (bFirstSet){
                            msg.append(", ");
                        }
                        msg.append(ObjectIdentifier.fetchValue(startDurable.getDurableId()));
                        bFirstSet = true;
                    }
                }
                msg.append("\n")
                        .append("Transaction ID       : ").append(ex.getTransactionID()).append("\n")
                        .append("Return Code          : ").append(ex.getCode()).append("\n")
                        .append("Message Text         : ").append(ex.getMessage()).append("\n")
                        .append("Reason Text          : ").append(ex.getReasonText()).append("\n");
                Infos.SysMsgStockInfo sysMsg = new Infos.SysMsgStockInfo();
                sysMsg.setSubSystemID(BizConstant.SP_SUBSYSTEMID_MM);
                sysMsg.setSystemMessageCode(BizConstant.SP_SYSTEMMSGCODE_DELIVERYERROR);
                sysMsg.setSystemMessageText(msg.toString());
                sysMsg.setNotifyFlag(true);
                sysMsg.setEquipmentID(equipmentID);
                sysMsg.setEquipmentStatus("");
                sysMsg.setStockerID(dummy);
                sysMsg.setStockerStatus("");
                sysMsg.setAGVID(dummy);
                sysMsg.setAGVStatus("");
                sysMsg.setLotID(dummy);
                sysMsg.setLotStatus("");
                sysMsg.setRouteID(dummy);
                sysMsg.setOperationID(dummy);
                sysMsg.setOperationNumber("");
                sysMsg.setSystemMessageTimeStamp(CimDateUtils.convertToSpecString(objCommonIn.getTimeStamp().getReportTimeStamp()));
                List<Infos.SysMsgStockInfo> sysMstStockInfoList = CimArrayUtils.getSize(out.getSysMstStockInfoList()) == 0 ? new ArrayList<>() : out.getSysMstStockInfoList();
                sysMstStockInfoList.add(sysMsg);
                out.setSysMstStockInfoList(sysMstStockInfoList);
                ex.setReasonText(msg.toString());
                ex.setData(out);
                throw ex;
            }
            /*------------------------------------------*/
            /*   Make txMultipleCarrierTransferReq parameter   */
            /*------------------------------------------*/
            //step7-5 - multiDurableXferFillInOTMSW005InParm
            log.info("step7-5 - multiDurableXferFillInOTMSW005InParm");
            Outputs.ObjMultiDurableXferFillInOTMSW005InParmOut multiDurableXferFillInOTMSW005InParmOut = null;
            try {
                multiDurableXferFillInOTMSW005InParmOut = durableMethod.multiDurableXferFillInOTMSW005InParm(objCommonIn,
                        equipmentID, strStartDurable,durablesInfoForStartReservationInqResult.getDurableCategory());
            } catch (ServiceException e) {
                log.info("multiDurableXferFillInOTMSW005InParm() is Not OK");
                /*----------------------------------------------------*/
                /*   Request to DurableMoveInReserveForIbReq Cancel   */
                /*----------------------------------------------------*/
                log.info("Request to DurableMoveInReserveForIbReq Cancel");
                //step7-6 - sxDrbMoveInReserveCancelForIBReq
                log.info("step7-6 - sxDrbMoveInReserveCancelForIBReq");
                Params.StartDurablesReservationCancelReqInParam input = new Params.StartDurablesReservationCancelReqInParam();
                input.setEquipmentID(equipmentID);
                input.setDurableControlJobID(durableControlJobID);
                input.setClaimMemo("");
                try {
                    durableService.sxDrbMoveInReserveCancelForIBReq(objCommonIn, input,null);
                } catch (ServiceException ex) {
                    log.info("sxDrbMoveInReserveCancelForIBReq != RC_OK");
                }
                throw new ServiceException(retCodeConfigEx.getStartLotReservationFail(), out);
            }
            // Debug Trace of TransferInfo
            log.info("Send Transfer Request to TMS. (Stocker -> EQP) ");
            Boolean bReRouteFlag = false;
            String reRouteXferFlag = StandardProperties.OM_XFER_REROUTE_FLAG.getValue();
            if (CimStringUtils.equals("1", reRouteXferFlag)) {
                log.info("reRouteXferFlag is 1");
                bReRouteFlag = true;
            }
            //Send Transfer Request to TMS. (Stocker -> EQP)
            //step7-7 - sxMultipleDurableTransferReq
            log.info("step7-7 - sxMultipleDurableTransferReq");
            List<Infos.DurableXferReq> strDurableXferReq = multiDurableXferFillInOTMSW005InParmOut.getStrDurableXferReq();
            try {
                this.sxMultipleDurableTransferReq(objCommonIn, bReRouteFlag, "S", strDurableXferReq,durablesInfoForStartReservationInqResult.getDurableCategory());
            } catch (ServiceException e) {
                log.info("sxMultipleDurableTransferReq() Not OK");
                //Prepare e-mail Message Text
                StringBuilder msg = new StringBuilder("<<< Transfer Error!  (Stocker -> EQP) >>>\n");
                msg.append("\n")
                        .append("Equipment ID         : ").append(equipmentID.getValue()).append("\n");
                if (CimArrayUtils.isNotEmpty(strDurableXferReq)){
                    for (Infos.DurableXferReq durableXferReq : strDurableXferReq) {
                        msg.append("-----------------------------------------\n")
                                .append("Durable ID           : ").append(ObjectIdentifier.fetchValue(durableXferReq.getDurableID())).append("\n")
                                .append("From Machine ID      : ").append(ObjectIdentifier.fetchValue(durableXferReq.getFromMachineID())).append("\n")
                                .append("From Port ID         : ").append(ObjectIdentifier.fetchValue(durableXferReq.getFromPortID())).append("\n")
                                .append("To Stocker Group     : ").append(durableXferReq.getToStockerGroup()).append("\n");
                        List<Infos.ToMachine> strToMachine = durableXferReq.getStrToMachine();
                        int lenToEqp = CimArrayUtils.getSize(strToMachine);
                        if (lenToEqp == 0){
                            msg.append("To Machine ID        : Nothing\n");
                        }
                        for (Infos.ToMachine toMachine : strToMachine) {
                            msg.append("To Machine ID        : ").append(ObjectIdentifier.fetchValue(toMachine.getToMachineID())).append("\n")
                                    .append("To Port ID           : ").append(ObjectIdentifier.fetchValue(toMachine.getToPortID())).append("\n");
                        }
                    }
                }
                msg.append("-----------------------------------------\n")
                        .append("Transaction ID       : ").append(e.getTransactionID()).append("\n")
                        .append("Return Code          : ").append(e.getCode()).append("\n")
                        .append("Message Text         : ").append(e.getMessage()).append("\n")
                        .append("Reason Text          : ").append(e.getReasonText()).append("\n");
                Infos.SysMsgStockInfo sysMsg = new Infos.SysMsgStockInfo();
                sysMsg.setSubSystemID(BizConstant.SP_SUBSYSTEMID_MM);
                sysMsg.setSystemMessageCode(BizConstant.SP_SYSTEMMSGCODE_DELIVERYERROR);
                sysMsg.setSystemMessageText(msg.toString());
                sysMsg.setNotifyFlag(true);
                sysMsg.setEquipmentID(equipmentID);
                sysMsg.setEquipmentStatus("");
                sysMsg.setStockerID(dummy);
                sysMsg.setStockerStatus("");
                sysMsg.setAGVID(dummy);
                sysMsg.setAGVStatus("");
                sysMsg.setLotID(dummy);
                sysMsg.setLotStatus("");
                sysMsg.setRouteID(dummy);
                sysMsg.setOperationID(dummy);
                sysMsg.setOperationNumber("");
                sysMsg.setSystemMessageTimeStamp(CimDateUtils.convertToSpecString(objCommonIn.getTimeStamp().getReportTimeStamp()));
                List<Infos.SysMsgStockInfo> sysMstStockInfoList = CimArrayUtils.getSize(out.getSysMstStockInfoList()) == 0 ? new ArrayList<>() : out.getSysMstStockInfoList();
                sysMstStockInfoList.add(sysMsg);
                out.setSysMstStockInfoList(sysMstStockInfoList);
                //step7-8 - sxDrbMoveInReserveCancelForIBReq
                log.info("step7-8 - sxDrbMoveInReserveCancelForIBReq");
                Params.StartDurablesReservationCancelReqInParam input = new Params.StartDurablesReservationCancelReqInParam();
                input.setEquipmentID(equipmentID);
                input.setDurableControlJobID(durableControlJobID);
                input.setClaimMemo("");
                try {
                    durableService.sxDrbMoveInReserveCancelForIBReq(objCommonIn, input,durablesInfoForStartReservationInqResult.getDurableCategory());
                } catch (ServiceException ex) {
                    log.info("##### txMoveInReserveCancelForIBReq != RC_OK");
                }
                throw new ServiceException(retCodeConfigEx.getStartLotReservationFail(), out);
            }
            log.info("OK! OK! OK! LoadReq [Stocker to EQP]  Return to Main");
            /*--------------------*/
            /*   End of Process   */
            /*--------------------*/
        }
        /*--------------------*/
        /*                    */
        /*   Return to Main   */
        /*                    */
        /*--------------------*/
        return out;
    }

    @Override
    public void sxMultipleDurableTransferReq(Infos.ObjCommon objCommon, Boolean rerouteFlag, String transportType, List<Infos.DurableXferReq> strDurableXferReq,String durableCategory) {
        /*-----------------------------------------------------------*/
        /* Set data for Xfer Request                                 */
        /*-----------------------------------------------------------*/
        //set data for xfer request.
        //if the destinatin is stocker, set null for 'from' field.
        Params.TransportJobCreateReqParams tranJobCreateReq = new Params.TransportJobCreateReqParams();
        int nLen = CimArrayUtils.getSize(strDurableXferReq);
        /*-----------------------------------------------------------*/
        /* Input data                                                */
        /*-----------------------------------------------------------*/
        /*--------------------------------------------*/
        /*                                            */
        /*        Object Lock Process                 */
        /*                                            */
        /*--------------------------------------------*/
        for (int ii = 0; ii < nLen; ii++) {
            /*--------------------------------------------*/
            /*                                            */
            /* Lock Port object (To)                      */
            /*                                            */
            /*--------------------------------------------*/
            if (0 > CimArrayUtils.getSize(strDurableXferReq.get(ii).getStrToMachine())){
                throw new ServiceException(retCodeConfig.getNotFoundMachine());
            }
            if (!ObjectIdentifier.isEmptyWithValue(strDurableXferReq.get(ii).getStrToMachine().get(0).getToMachineID())
                    && !ObjectIdentifier.isEmptyWithValue(strDurableXferReq.get(ii).getStrToMachine().get(0).getToPortID())){
                /*---------------------------------------------------------*/
                /*   Get All Ports being in the same Port Group as ToPort  */
                /*---------------------------------------------------------*/
                //【step1】 - portResource_allPortsInSameGroup_Get
                Infos.EqpPortInfo allPortsInSameGroupRetCode = portMethod.portResourceAllPortsInSameGroupGet(objCommon,
                        strDurableXferReq.get(ii).getStrToMachine().get(0).getToMachineID(),
                        strDurableXferReq.get(ii).getStrToMachine().get(0).getToPortID());

                int lenPort = CimArrayUtils.getSize(allPortsInSameGroupRetCode.getEqpPortStatuses());
                for (int jj = 0; jj < lenPort; jj++) {
                    objectLockMethod.objectLockForEquipmentResource(objCommon, strDurableXferReq.get(ii).getStrToMachine().get(0).getToMachineID(), allPortsInSameGroupRetCode.getEqpPortStatuses().get(jj).getPortID(), BizConstant.SP_CLASSNAME_POSPORTRESOURCE);
                }
            }
            /*--------------------------------------------*/
            /*                                            */
            /* Lock Port object (From)                    */
            /*                                            */
            /*--------------------------------------------*/
            if (ObjectIdentifier.isEmptyWithValue(strDurableXferReq.get(ii).getFromMachineID())){
                continue;
            }
            /*------------------------------------------------------*/
            /*  Check whether specified machine is eqp or stocker   */
            /*------------------------------------------------------*/
            Boolean isStorageFlag = false;
            Infos.EqpStatusInfo eqpStatusInfoRetCode = null;
            try {
                eqpStatusInfoRetCode = equipmentMethod.equipmentStatusInfoGet(objCommon, strDurableXferReq.get(ii).getFromMachineID());
            } catch (ServiceException e) {
                if (Validations.isEquals(retCodeConfig.getNotFoundEqp(), e.getCode())){
                    isStorageFlag = true;
                } else {
                    log.info("equipmentStatusInfoGet() is Not OK");
                    continue;
                }
            }
            log.info("From-equiment is Storage or not == {}",ObjectIdentifier.fetchValue(strDurableXferReq.get(ii).getFromMachineID()));
            /*--------------------------------------------*/
            /* Lock Port object of machine                */
            /*--------------------------------------------*/
            if (CimBooleanUtils.isFalse(isStorageFlag)){
                objectLockMethod.objectLockForEquipmentResource(objCommon, strDurableXferReq.get(ii).getFromMachineID(), strDurableXferReq.get(ii).getFromPortID(), BizConstant.SP_CLASSNAME_POSPORTRESOURCE);
            }
        }
        //---------------------------------------------
        // Check Port condition
        //---------------------------------------------
        log.info("Check Port condition");
        for (int nCastXfer = 0; nCastXfer < nLen; nCastXfer++) {
            int nToEqp = CimArrayUtils.getSize(strDurableXferReq.get(nCastXfer).getStrToMachine());
            if (0 == nToEqp){
                log.info("nToEqp is 0");
                continue;
            }
            if (ObjectIdentifier.isEmptyWithValue(strDurableXferReq.get(nCastXfer).getStrToMachine().get(0).getToPortID())){
                log.info("ToMachine is stocker. Omit check for port.");
                continue;
            }
            //【step5】 - equipment_portInfo_Get
            Infos.EqpPortInfo eqpPortInfo = equipmentMethod.equipmentPortInfoGet(objCommon, strDurableXferReq.get(nCastXfer).getStrToMachine().get(0).getToMachineID());
            int nPortLen = CimArrayUtils.getSize(eqpPortInfo.getEqpPortStatuses());
            for (int nPort = 0; nPort < nPortLen; nPort++) {
                if (ObjectIdentifier.equalsWithValue(eqpPortInfo.getEqpPortStatuses().get(nPort).getPortID(),
                        strDurableXferReq.get(nCastXfer).getStrToMachine().get(0).getToPortID())){
                    if (!CimStringUtils.equals(BizConstant.SP_EQP_DISPATCHMODE_AUTO, eqpPortInfo.getEqpPortStatuses().get(nPort).getAccessMode())){
                        log.error("Return Invalid Port Accessmode");
                        throw new ServiceException(new OmCode(retCodeConfigEx.getInvalidPortAccessMode(),
                                eqpPortInfo.getEqpPortStatuses().get(nPort).getPortID().getValue(),
                                eqpPortInfo.getEqpPortStatuses().get(nPort).getAccessMode()));
                    }
                    if (!CimStringUtils.equals(BizConstant.SP_EQP_ONLINEMODE_OFFLINE, eqpPortInfo.getEqpPortStatuses().get(nPort).getOnlineMode())){
                        log.info("onlineMode is not Offline");
                        //【neyo】change loadPurposeType is Other Type Only
                        if (CimStringUtils.equals(BizConstant.SP_LOADPURPOSETYPE_OTHER, eqpPortInfo.getEqpPortStatuses().get(nPort).getLoadPurposeType())){
                            log.info("loadPurposeType is Other Only ");
                            if (!CimStringUtils.equals(BizConstant.SP_PORTRSC_DISPATCHSTATE_REQUIRED, eqpPortInfo.getEqpPortStatuses().get(nPort).getDispatchState())
                                    && !CimStringUtils.equals(BizConstant.SP_TRANSPORT_TYPE_TAKE_OUT_IN, transportType)){
                                if (ObjectIdentifier.equalsWithValue(eqpPortInfo.getEqpPortStatuses().get(nPort).getDispatchLoadCassetteID(),
                                        strDurableXferReq.get(nCastXfer).getDurableID())){
                                    //OK
                                    log.info("DurableID of Input param and dispatchLoadCassetteID are same.");
                                }else {
                                    log.error("Return Invalid Port Dispstat");
                                    throw new ServiceException(new OmCode(retCodeConfigEx.getInvalidPortDispatchStatus(),
                                            ObjectIdentifier.fetchValue(eqpPortInfo.getEqpPortStatuses().get(nPort).getPortID()),
                                            eqpPortInfo.getEqpPortStatuses().get(nPort).getDispatchState()));
                                }
                            }
                        }
                        //------------------------------------------------------
                        //  Check Port Status for Fixed buffer equipment only
                        //------------------------------------------------------
                        //【step6】 - equipment_brInfoForInternalBuffer_GetDR__120
                        Infos.EqpBrInfoForInternalBuffer equipmentBrInfoForInternalBufferDrRetCode = equipmentMethod.equipmentBrInfoForInternalBufferGetDR(objCommon,
                                strDurableXferReq.get(nCastXfer).getStrToMachine().get(0).getToMachineID());
                        if (!CimStringUtils.equals(BizConstant.SP_MC_CATEGORY_INTERNALBUFFER, equipmentBrInfoForInternalBufferDrRetCode.getEquipmentCategory())){
                            log.info("Equipment Category is fixed Buffer ");
                            if (!CimStringUtils.equals(BizConstant.SP_TRANSPORT_TYPE_TAKE_OUT_IN, transportType)
                                    && (CimStringUtils.equals(BizConstant.SP_PORTRSC_PORTSTATE_UNLOADAVAIL,eqpPortInfo.getEqpPortStatuses().get(nPort).getPortState())
                                    || CimStringUtils.equals(BizConstant.SP_PORTRSC_PORTSTATE_UNLOADREQ,eqpPortInfo.getEqpPortStatuses().get(nPort).getPortState()))){
                                log.error("Return Invalid Port State");
                                throw new ServiceException(new OmCode(retCodeConfig.getInvalidPortState(),
                                        eqpPortInfo.getEqpPortStatuses().get(nPort).getPortID().getValue(), eqpPortInfo.getEqpPortStatuses().get(nPort).getPortState()));
                            }
                            if (!CimStringUtils.equals(BizConstant.SP_PORTRSC_PORTSTATE_LOADREQ,eqpPortInfo.getEqpPortStatuses().get(nPort).getPortState())
                                    && !CimStringUtils.equals(BizConstant.SP_PORTRSC_PORTSTATE_LOADAVAIL,eqpPortInfo.getEqpPortStatuses().get(nPort).getPortState())
                                    && !CimStringUtils.equals(BizConstant.SP_PORTRSC_PORTSTATE_UNLOADAVAIL,eqpPortInfo.getEqpPortStatuses().get(nPort).getPortState())
                                    && !CimStringUtils.equals(BizConstant.SP_PORTRSC_PORTSTATE_UNLOADREQ,eqpPortInfo.getEqpPortStatuses().get(nPort).getPortState())){
                                //Indent adjust start
                                log.error("Return Invalid Port State");
                                throw new ServiceException(new OmCode(retCodeConfig.getInvalidPortState(),
                                        ObjectIdentifier.fetchValue(eqpPortInfo.getEqpPortStatuses().get(nPort).getPortID()), eqpPortInfo.getEqpPortStatuses().get(nPort).getPortState()));
                            }
                        }else {
                            // Equipment category is Internal Bufrer
                            log.info("Equipment category is Internal Bufrer");
                            if (!CimStringUtils.equals(BizConstant.SP_PORTRSC_PORTSTATE_LOADREQ,eqpPortInfo.getEqpPortStatuses().get(nPort).getPortState())
                                    && !CimStringUtils.equals(BizConstant.SP_PORTRSC_PORTSTATE_LOADCOMP,eqpPortInfo.getEqpPortStatuses().get(nPort).getPortState())
                                    && !CimStringUtils.equals(BizConstant.SP_PORTRSC_PORTSTATE_UNLOADAVAIL,eqpPortInfo.getEqpPortStatuses().get(nPort).getPortState())){
                                log.error("Return Invalid Port State");
                                throw new ServiceException(new OmCode(retCodeConfig.getInvalidPortState(),
                                        ObjectIdentifier.fetchValue(eqpPortInfo.getEqpPortStatuses().get(nPort).getPortID()), eqpPortInfo.getEqpPortStatuses().get(nPort).getPortState()));
                            }
                        }
                        log.info("Port is good condition");
                    }
                }
            }
        }
        List<Infos.JobCreateArray> jobCreateDataList  = new ArrayList<>();
        for (int i = 0; i < nLen; i++) {
            Machine aBaseMachine = null;
            Boolean isStorageBool = false;
            ObjectIdentifier dummy = new ObjectIdentifier();
            tranJobCreateReq.setJobCreateData(jobCreateDataList);
            if (ObjectIdentifier.isEmptyWithValue(strDurableXferReq.get(i).getFromMachineID())){
                log.info("isStorageBool==TRUE");
                Infos.JobCreateArray jobCreateData = new Infos.JobCreateArray();
                jobCreateDataList.add(jobCreateData);
                jobCreateData.setFromMachineID(dummy);
                jobCreateData.setFromPortID(dummy);
            }else {
                log.info("isStorageBool==FALSE");
                Infos.JobCreateArray jobCreateData = new Infos.JobCreateArray();
                jobCreateDataList.add(jobCreateData);
                jobCreateData.setFromMachineID(strDurableXferReq.get(i).getFromMachineID());
                jobCreateData.setFromPortID(strDurableXferReq.get(i).getFromPortID());
            }
            int nnLen = CimArrayUtils.getSize(strDurableXferReq.get(i).getStrToMachine());
            List<Infos.ToDestination> toMachine = new ArrayList<>();
            jobCreateDataList.get(i).setToMachine(toMachine);
            for (int j = 0; j < nnLen; j++) {
                Infos.ToDestination toDestination = new Infos.ToDestination();
                toMachine.add(toDestination);
                toDestination.setToMachineID(strDurableXferReq.get(i).getStrToMachine().get(j).getToMachineID());
                toDestination.setToPortID(strDurableXferReq.get(i).getStrToMachine().get(j).getToPortID());
            }
            tranJobCreateReq.setTransportType(transportType);
            tranJobCreateReq.setRerouteFlag(rerouteFlag);
            tranJobCreateReq.getJobCreateData().get(i).setCarrierID(strDurableXferReq.get(i).getDurableID());
            tranJobCreateReq.getJobCreateData().get(i).setZoneType(strDurableXferReq.get(i).getZoneType());
            tranJobCreateReq.getJobCreateData().get(i).setN2PurgeFlag(strDurableXferReq.get(i).getN2PurgeFlag());
            tranJobCreateReq.getJobCreateData().get(i).setExpectedStartTime(strDurableXferReq.get(i).getExpectedStartTime());
            tranJobCreateReq.getJobCreateData().get(i).setExpectedEndTime(strDurableXferReq.get(i).getExpectedEndTime());
            tranJobCreateReq.getJobCreateData().get(i).setMandatoryFlag(strDurableXferReq.get(i).getMandatoryFlag());
            tranJobCreateReq.getJobCreateData().get(i).setPriority(strDurableXferReq.get(i).getPriority());
        }
        /*-----------------------------------------------------------*/
        /* Transfer Request to TMSMgr                                */
        /*-----------------------------------------------------------*/
        //【step7】 - TMSMgr_SendTransportJobCreateReq
        Results.TransportJobCreateReqResult transportJobCreateReqResultRetCode = tmsService.transportJobCreateReq(objCommon,objCommon.getUser(),tranJobCreateReq);

        /*-----------------------------------------------------------*/
        /* Maintain Eqp-Port's DispatchState ( -> Dispatched )       */
        /*-----------------------------------------------------------*/
        for (int j = 0; j < nLen; j++) {
            //check length of To-machine
            if (CimArrayUtils.isEmpty(strDurableXferReq.get(j).getStrToMachine())){
                throw new ServiceException(retCodeConfig.getNotFoundMachine());
            }
            if (!ObjectIdentifier.isEmptyWithValue(strDurableXferReq.get(j).getStrToMachine().get(0).getToMachineID())
                    && !ObjectIdentifier.isEmptyWithValue(strDurableXferReq.get(j).getStrToMachine().get(0).getToPortID())){
                /*----------------------------------------------------------------*/
                /* Change Specified Target Port's DispatchState to "Dispatched"   */
                /*----------------------------------------------------------------*/
                log.info("Change Specified Target Port's DispatchState to 'Dispatched'...");
                //【step8】 - equipment_dispatchState_Change
                ObjectIdentifier dmyObjID = new ObjectIdentifier();
                equipmentMethod.equipmentDispatchStateChange(objCommon,
                        strDurableXferReq.get(j).getStrToMachine().get(0).getToMachineID(),
                        strDurableXferReq.get(j).getStrToMachine().get(0).getToPortID(),
                        BizConstant.SP_PORTRSC_DISPATCHSTATE_DISPATCHED,
                        dmyObjID,
                        strDurableXferReq.get(j).getDurableID(),
                        dmyObjID,
                        dmyObjID);
            }
        }
        /*-----------------------------------------------------------*/
        /* Maintain Eqp-Port's DispatchState ( -> NotDispatched )    */
        /*-----------------------------------------------------------*/
        log.info("Maintain Eqp-Port's DispatchState ( -> NotDispatched )...");
        for (int j = 0; j < nLen; j++) {
            // Check length of In-Parameter
            if (CimArrayUtils.isEmpty(strDurableXferReq.get(j).getStrToMachine())){
                throw new ServiceException(retCodeConfig.getNotFoundMachine());
            }
            if (!ObjectIdentifier.isEmptyWithValue(strDurableXferReq.get(j).getStrToMachine().get(0).getToMachineID())
                    && !ObjectIdentifier.isEmptyWithValue(strDurableXferReq.get(j).getStrToMachine().get(0).getToPortID())){
                /*-----------------------------------------------*/
                /* Get All Ports registered as Same Port Group   */
                /*-----------------------------------------------*/
                //【step9】 - equipment_portInfo_Get
                Infos.EqpPortInfo eqpPortInfo = equipmentMethod.equipmentPortInfoGet(objCommon, strDurableXferReq.get(j).getStrToMachine().get(0).getToMachineID());
                /*-----------------------------------*/
                /* Get Specified Port's Port Group   */
                /*-----------------------------------*/
                log.info("Get Specified Port's Port Group...");
                int basePGNo = 0;
                int pLen = CimArrayUtils.getSize(eqpPortInfo.getEqpPortStatuses());
                for (int k = 0; k < pLen; k++) {
                    if (ObjectIdentifier.equalsWithValue(strDurableXferReq.get(j).getStrToMachine().get(0).getToPortID(),
                            eqpPortInfo.getEqpPortStatuses().get(k).getPortID())){
                        basePGNo = k;
                        break;
                    }
                }
                /*------------------------------*/
                /* Find Port ID to be Updated   */
                /*------------------------------*/
                for (int k = 0; k < pLen; k++) {
                    /*===== Omit Base Port (In-Parm's Port) =====*/
                    if (k == basePGNo){
                        log.info("k == basePGNo,continue...");
                    }
                    /*===== Omit Different Group's Port =====*/
                    if (!CimStringUtils.equals(eqpPortInfo.getEqpPortStatuses().get(k).getPortGroup(),
                            eqpPortInfo.getEqpPortStatuses().get(basePGNo).getPortGroup())){
                        log.info("Not same portGroup, continue...");
                        continue;
                    }
                    /*===== Check Port's Status =====*/
                    Infos.EqpPortStatus eqpPortStatus = eqpPortInfo.getEqpPortStatuses().get(k);
                    if (CimStringUtils.equals(BizConstant.SP_PORTRSC_DISPATCHSTATE_REQUIRED,eqpPortStatus.getDispatchState())
                            && (CimStringUtils.equals(BizConstant.SP_PORTRSC_PORTSTATE_LOADAVAIL,eqpPortStatus.getPortState())
                            || CimStringUtils.equals(BizConstant.SP_PORTRSC_PORTSTATE_LOADREQ,eqpPortStatus.getPortState()))){
                        /*-----------------------------------------------------------------------------*/
                        /* Change Same Group's Not-Specified Port's DispatchState to "NotDispatched"   */
                        /*-----------------------------------------------------------------------------*/
                        log.info("Change Same Group's Not-Specified Port's DispatchState to 'NotDispatched'...");
                        ObjectIdentifier dmyOjbID = new ObjectIdentifier();
                        //【step10】 - equipment_dispatchState_Change
                        equipmentMethod.equipmentDispatchStateChange(objCommon,
                                strDurableXferReq.get(j).getStrToMachine().get(0).getToMachineID(),
                                eqpPortStatus.getPortID(),
                                BizConstant.SP_PORTRSC_DISPATCHSTATE_NOTDISPATCHED ,
                                dmyOjbID,
                                dmyOjbID,
                                dmyOjbID,
                                dmyOjbID);
                    }
                }
            }
        }
        /*------------------------------*/
        /* change fromEQP dispatchState */
        /*------------------------------*/
        log.info("change fromEQP dispatchState");
        for (int j = 0; j < nLen; j++) {
            if (ObjectIdentifier.isEmptyWithValue(strDurableXferReq.get(j).getFromMachineID())){
                log.info("0 == CIMFWStrLen(strCarrierXferReq[j].fromMachineID.identifier)");
                continue;
            }
            //【step11】- equipment_statusInfo_Get__090
            Boolean isStorageMachine = false;
            Infos.EqpStatusInfo eqpStatusInfoResultRetCode = null;
            try {
                eqpStatusInfoResultRetCode = equipmentMethod.equipmentStatusInfoGet(objCommon, strDurableXferReq.get(j).getFromMachineID());
            } catch (ServiceException e) {
                if (Validations.isEquals(retCodeConfig.getNotFoundEqp(), e.getCode())){
                    isStorageMachine = true;
                } else {
                    log.info("equipment_statusInfo_Get__090() rc != RC_OK");
                    continue;
                }
            }

            if (CimBooleanUtils.isFalse(isStorageMachine)){
                log.info("FALSE == isStorageMachine");
                log.info("call equipment_dispatchState_Change()");
                //【step12】 - equipment_dispatchState_Change
                ObjectIdentifier dmyOjbID = new ObjectIdentifier();
                //【step10】 - equipment_dispatchState_Change
                equipmentMethod.equipmentDispatchStateChange(objCommon,
                        strDurableXferReq.get(j).getFromMachineID(),
                        strDurableXferReq.get(j).getFromPortID(),
                        BizConstant.SP_PORTRSC_DISPATCHSTATE_DISPATCHED ,
                        dmyOjbID,
                        dmyOjbID,
                        dmyOjbID,
                        strDurableXferReq.get(j).getDurableID());
            }else {
                log.info("TRUE == isStorageMachine");
            }
        }
    }

    @Override
    public void sxSingleDurableTransferReq(Infos.ObjCommon objCommonIn, Params.SingleDurableTransferReqParam params,String durableCategory) {

        ObjectIdentifier fromMachineID = params.getFromMachineID();

        Boolean isStorageFlag = false;
        //【Step1】: equipmentStatusInfoGet
        Infos.EqpStatusInfo eqpStatusInfoRetCode = null;
        try {
            eqpStatusInfoRetCode = equipmentMethod.equipmentStatusInfoGet(objCommonIn, fromMachineID);
        } catch (ServiceException e) {
            if (Validations.isEquals(retCodeConfig.getNotFoundEqp(), e.getCode())){
                isStorageFlag = true;
            } else {
                throw e;
            }
        }

        if (CimBooleanUtils.isFalse(isStorageFlag)) {
            //【Step2】: objectLockForEquipmentResource
            lockMethod.objectLockForEquipmentResource(objCommonIn,params.getFromMachineID(),params.getFromPortID(),
                    BizConstant.SP_CLASSNAME_POSPORTRESOURCE);
        }
        //【Step3】: objectLock
        if (CimStringUtils.equals(BizConstant.SP_DURABLECAT_CASSETTE,durableCategory)){
            lockMethod.objectLock( objCommonIn,
                    CimCassette.class,
                    params.getDurableID());
        }else if (CimStringUtils.equals(BizConstant.SP_DURABLECAT_RETICLEPOD,durableCategory)) {
            lockMethod.objectLock( objCommonIn,
                    CimReticlePod.class,
                    params.getDurableID());
        } else if (CimStringUtils.equals(BizConstant.SP_DURABLECAT_RETICLE,durableCategory)) {
            lockMethod.objectLock( objCommonIn,
                    CimProcessDurable.class,
                    params.getDurableID());
        }

        //【Step4】: equipmentMachineTypeCheckDR
        List<Infos.ToMachine> toMachines = params.getToMachine();
        if (!CimArrayUtils.isEmpty(toMachines)) {
            Boolean equipmentFlag = equipmentMethod.equipmentMachineTypeCheckDR(objCommonIn,
                    toMachines.get(0).getToMachineID());
            if (CimBooleanUtils.isFalse(equipmentFlag)) {
                // toMachine is a stocker. Next, check if the Xfer request is stocker out;
                if (CimStringUtils.equals(BizConstant.SP_DURABLECAT_CASSETTE,durableCategory)){
                    if (CimStringUtils.isNotEmpty(ObjectIdentifier.fetchValue(toMachines.get(0).getToMachineID()))
                            && CimStringUtils.isNotEmpty(ObjectIdentifier.fetchValue(toMachines.get(0).getToPortID()))) {
                        // If this is a Manual out request, then cassette reservation is required
                        List<Infos.RsvLotCarrier> rsvLotCarriers = new ArrayList<>(1);
                        Infos.RsvLotCarrier rsvLotCarrier = new Infos.RsvLotCarrier();
                        rsvLotCarrier.setCarrierID(params.getDurableID());
                        rsvLotCarriers.add(rsvLotCarrier);
                        Params.CarrierReserveReqParam carrierReserveReqParam = new Params.CarrierReserveReqParam();
                        carrierReserveReqParam.setStrRsvLotCarrier(rsvLotCarriers);
                        carrierReserveReqParam.setClaimMemo("");

                        //【Step5】: sxCarrierReserveReq
                        Results.CarrierReserveReqResult carrierReserveReqResultRetCode = this.sxCarrierReserveReq(objCommonIn, carrierReserveReqParam);
                    } else {
                        // If Control Job exists, then this request should be rejected
                        // Check cassette reservation Status
                        //【Step6】: cassetteReservationInfoGetDR
                        Outputs.CassetteReservationInfoGetDROut cassetteReservationInfoGetDROut = cassetteMethod.cassetteReservationInfoGetDR(objCommonIn, params.getDurableID());
                        if (CimStringUtils.isNotEmpty(ObjectIdentifier.fetchValue(cassetteReservationInfoGetDROut.getControlJobID()))
                                || CimStringUtils.isNotEmpty(cassetteReservationInfoGetDROut.getNPWLoadPurposeType())) {
                            throw new ServiceException(retCodeConfig.getAlreadyDispatchReservedCassette());
                        }
                    }
                }
                // TODO: 2020/9/16 Durable/Reticle
            }
        }

        // Set data for Xfer Request
        Params.TransportJobCreateReqParams transportJobCreateReqParam = new Params.TransportJobCreateReqParams();
        List<Infos.JobCreateArray> jobCreateArrays = new ArrayList<>(1);
        Infos.JobCreateArray jobCreateData = new Infos.JobCreateArray();
        jobCreateArrays.add(jobCreateData);
        transportJobCreateReqParam.setJobCreateData(jobCreateArrays);

        Boolean isStorageBool = false;

        //【Step7】: equipmentStatusInfoGet
        try {
            eqpStatusInfoRetCode = equipmentMethod.equipmentStatusInfoGet(objCommonIn, fromMachineID);
        } catch (ServiceException e) {
            if (Validations.isEquals(retCodeConfig.getNotFoundEqp(), e.getCode())){
                isStorageBool = true;
            } else {
                log.error("equipment_statusInfo_Get() rc != RC_OK");
                throw e;
            }
        }

        log.debug(String.format("isStorageBool == %b, fromMachineID [ %s ]", isStorageBool, fromMachineID));

        Boolean dispatchStateMaintainRequiredFlag = false;

        if (isStorageBool) {
            jobCreateData.setFromMachineID(null);
            jobCreateData.setFromPortID(null);
        } else {
            jobCreateData.setFromMachineID(params.getFromMachineID());
            jobCreateData.setFromPortID(params.getFromPortID());
            dispatchStateMaintainRequiredFlag = true;
        }
        List<Infos.ToMachine> toMachineList = params.getToMachine();
        if (!CimArrayUtils.isEmpty(toMachineList)) {
            jobCreateData.setToMachine(new ArrayList<>(toMachineList.size()));
            for (Infos.ToMachine toMachine: toMachineList) {
                Infos.ToDestination toDestination = new Infos.ToDestination();
                toDestination.setToMachineID(toMachine.getToMachineID());
                toDestination.setToPortID(toMachine.getToPortID());
                jobCreateData.getToMachine().add(toDestination);
            }
        }
        transportJobCreateReqParam.setTransportType("S");
        transportJobCreateReqParam.setRerouteFlag(params.getRerouteFlag());
        jobCreateData.setCarrierID(params.getDurableID());
        jobCreateData.setZoneType(params.getZoneType());
        jobCreateData.setN2PurgeFlag(params.getN2PurgeFlag());
        jobCreateData.setExpectedStartTime(params.getExpectedStartTime());
        jobCreateData.setExpectedEndTime(params.getExpectedEndTime());
        jobCreateData.setMandatoryFlag(params.getMandatoryFlag());
        jobCreateData.setPriority(params.getPriority());

        //【Step8】: transportJobCreateReq
        Results.TransportJobCreateReqResult transportJobCreateReqResultRetCode = tmsService.transportJobCreateReq(objCommonIn,objCommonIn.getUser(),transportJobCreateReqParam);

        // Maintain Eqp-port's DispatchState
        if (CimBooleanUtils.isTrue(dispatchStateMaintainRequiredFlag)) {
            //【Step9】: equipmentDispatchStateChange
            equipmentMethod.equipmentDispatchStateChange(objCommonIn, fromMachineID, params.getFromPortID(),
                    BizConstant.SP_PORTRSC_DISPATCHSTATE_DISPATCHED, null, null, null,
                    params.getDurableID());
        }
    }
}
