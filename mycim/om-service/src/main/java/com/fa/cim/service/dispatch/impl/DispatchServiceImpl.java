package com.fa.cim.service.dispatch.impl;

import com.fa.cim.annotaion.OmService;
import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.constant.CIMStateConst;
import com.fa.cim.common.constant.TCSReqEnum;
import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.OmCode;
import com.fa.cim.common.support.RetCode;
import com.fa.cim.common.utils.*;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.config.RetCodeConfigEx;
import com.fa.cim.crcp.ChamberLevelRecipeReserveParam;
import com.fa.cim.dto.*;
import com.fa.cim.layoutrecipe.LayoutRecipeParams;
import com.fa.cim.method.*;
import com.fa.cim.newcore.bo.dispatch.CimDispatcher;
import com.fa.cim.newcore.bo.durable.CimCassette;
import com.fa.cim.newcore.bo.machine.CimMachine;
import com.fa.cim.newcore.bo.product.CimControlJob;
import com.fa.cim.newcore.bo.product.CimLot;
import com.fa.cim.newcore.dto.pr.PilotRunInfo;
import com.fa.cim.newcore.impl.bo.env.StandardProperties;
import com.fa.cim.remote.IEAPRemoteManager;
import com.fa.cim.service.apc.IAPCInqService;
import com.fa.cim.service.cjpj.IControlJobProcessJobService;
import com.fa.cim.service.dispatch.IDispatchService;
import com.fa.cim.service.lot.ILotInqService;
import com.fa.cim.service.newSorter.ISortNewService;
import com.fa.cim.service.pr.IPilotRunService;
import com.fa.cim.service.recipe.IRecipeService;
import com.fa.cim.service.season.ISeasoningService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.stream.Collectors;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

/**
 * description:
 * <p>
 * change history:
 * date             defect             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/9/8        ********             Jack Luo            create file
 *
 * @author: Jack Luo
 * @date: 2020/9/8 18:37
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@OmService
public class DispatchServiceImpl implements IDispatchService {

    @Autowired
    private RetCodeConfig retCodeConfig;

    @Autowired
    private ILotMethod lotMethod;

    @Autowired
    private IPersonMethod personMethod;

    @Autowired
    private IAutoDispatchControlMethod autoDispatchControlMethod;

    @Autowired
    private IEventMethod eventMethod;

    @Autowired
    private IObjectLockMethod objectLockMethod;

    @Autowired
    private ICassetteMethod cassetteMethod;

    @Autowired
    private ISeasoningService seasoningService;

    @Autowired
    private IEquipmentMethod equipmentMethod;

    @Autowired
    private IObjectMethod objectMethod;

    @Autowired
    private IControlJobMethod controlJobMethod;

    @Autowired
    private IProcessMethod processMethod;

    @Autowired
    private IControlJobProcessJobService controlJobProcessJobService;

    @Autowired
    private IScheduleChangeReservationMethod scheduleChangeReservationMethod;

    @Autowired
    private ITCSMethod tcsMethod;

    @Autowired
    private IMessageMethod messageMethod;

    @Autowired
    private IAPCMethod apcMethod;

    @Autowired
    private RetCodeConfigEx retCodeConfigEx;

    @Autowired
    private IPortMethod portMethod;

    @Autowired
    private IMachineRecipeMethod machineRecipeMethod;

    @Autowired
    private IFPCMethod fpcMethod;

    @Autowired
    private IRecipeService recipeService;

    @Autowired
    private ISeasonMethod seasonMethod;

    @Autowired
    private IAPCInqService apcInqService;

    @Autowired
    private IPilotRunService pilotRunService;

    @Autowired
    private ISLMMethod slmMethod;

    @Autowired
    private IContaminationMethod contaminationMethod;

    @Autowired
    private IEAPMethod eapMethod;

    @Autowired
    private IMinQTimeMethod minQTimeMethod;

    @Autowired
    private IPilotRunMethod pilotRunMethod;

    @Autowired
    private IOperationMethod operationMethod;

    @Autowired
    private IBondingGroupMethod bondingGroupMethod;

    @Autowired
    private ILotInqService lotInqService;

    @Autowired
    private ILayoutRecipeMethod layoutRecipeMethod;

    @Autowired
    private IOcapMethod ocapMethod;

    @Autowired
    private ISortNewService sortNewService;

    @Autowired
    private ISorterNewMethod sorterNewMethod;

    @Autowired
    private IReticleMethod reticleMethod;

    @Autowired
    private IChamberLevelRecipeMethod chamberLevelRecipeMethod;

    @Override
    public Results.AutoDispatchConfigModifyReqResult sxAutoDispatchConfigModifyReq(Infos.ObjCommon objCommon, Params.AutoDispatchConfigModifyReqParams autoDispatchConfigModifyReqParams) {
        Results.AutoDispatchConfigModifyReqResult autoDispatchConfigModifyReqResult = new Results.AutoDispatchConfigModifyReqResult();
        List<Infos.LotAutoDispatchControlUpdateInfo> lotAutoDispatchControlUpdateInfoList = autoDispatchConfigModifyReqParams.getLotAutoDispatchControlUpdateInfoList();
        //------------------------------------------------------------------------
        //   Check Condition
        //------------------------------------------------------------------------
        for (Infos.LotAutoDispatchControlUpdateInfo lotAutoDispatchControlUpdateInfo : lotAutoDispatchControlUpdateInfoList) {
            // Step1-lot_state_Get
            log.info("Step1 - lot_state_Get");
            String lotState = lotMethod.lotStateGet(objCommon, lotAutoDispatchControlUpdateInfo.getLotID());
            Validations.check(!CimStringUtils.equals(lotState, CIMStateConst.CIM_LOT_STATE_ACTIVE), new OmCode(retCodeConfig.getInvalidLotStat(), lotState));
        }
        for (Infos.LotAutoDispatchControlUpdateInfo lotAutoDispatchControlUpdateInfo : lotAutoDispatchControlUpdateInfoList) {
            //----------------------------------
            //  Get InPostProcessFlag of Lot
            //----------------------------------
            log.info("Step2 - lot_inPostProcessFlag_Get");
            Outputs.ObjLotInPostProcessFlagOut lotInPostProcessFlag = lotMethod.lotInPostProcessFlagGet(objCommon, lotAutoDispatchControlUpdateInfo.getLotID());
            //----------------------------------------------
            //  If Lot is in post process, returns error
            //----------------------------------------------
            if (lotInPostProcessFlag.getInPostProcessFlagOfLot()) {
                /*---------------------------*/
                /* Get UserGroupID By UserID */
                /*---------------------------*/
                log.info("Step3 - person_userGroupList_GetDR");
                List<ObjectIdentifier> userGroupIDs = personMethod.personUserGroupListGetDR(objCommon, objCommon.getUser().getUserID());
                int userGroupIDsLen = CimArrayUtils.getSize(userGroupIDs);
                int i = 0;
                for (i = 0; i < userGroupIDsLen; i++) {

                }
                Validations.check(i == userGroupIDsLen, new OmCode(retCodeConfig.getLotInPostProcess(), lotAutoDispatchControlUpdateInfo.getLotID().getValue()));
            }
        }

        for (Infos.LotAutoDispatchControlUpdateInfo lotAutoDispatchControlUpdateInfo : lotAutoDispatchControlUpdateInfoList) {
            log.info("Step4 - autoDispatchControl_info_Check");
            try {
                autoDispatchControlMethod.autoDispatchControlInfoCheck(objCommon, lotAutoDispatchControlUpdateInfo);
            } catch (ServiceException e) {
                e.setData(lotAutoDispatchControlUpdateInfo);
                throw e;
            }
        }
        for (Infos.LotAutoDispatchControlUpdateInfo lotAutoDispatchControlUpdateInfo : lotAutoDispatchControlUpdateInfoList) {
            /*--------------------------------*/
            /*   Lock objects to be updated   */
            /*--------------------------------*/
            // Step5 - object_Lock
            objectLockMethod.objectLock(objCommon, CimLot.class, lotAutoDispatchControlUpdateInfo.getLotID());
        }
        for (Infos.LotAutoDispatchControlUpdateInfo lotAutoDispatchControlUpdateInfo : lotAutoDispatchControlUpdateInfoList) {
            List<Infos.AutoDispatchControlUpdateInfo> autoDispatchControlUpdateInfoList = lotAutoDispatchControlUpdateInfo.getAutoDispatchControlUpdateInfoList();
            if (!CimArrayUtils.isEmpty(autoDispatchControlUpdateInfoList)) {
                for (Infos.AutoDispatchControlUpdateInfo autoDispatchControlUpdateInfo : autoDispatchControlUpdateInfoList) {
                    Inputs.AutoDispatchControlInfoUpdateIn autoDispatchControlInfoUpdateIn = new Inputs.AutoDispatchControlInfoUpdateIn();
                    autoDispatchControlInfoUpdateIn.setLotID(lotAutoDispatchControlUpdateInfo.getLotID());
                    autoDispatchControlInfoUpdateIn.setAutoDispatchControlUpdateInfo(autoDispatchControlUpdateInfo);
                    log.info("Step6 - autoDispatchControl_info_Update");
                    try {
                        autoDispatchControlMethod.autoDispatchControlInfoUpdate(objCommon, autoDispatchControlInfoUpdateIn);
                    } catch (ServiceException e) {
                        e.setData(lotAutoDispatchControlUpdateInfo);
                        throw e;
                    }
                    //===================================================================================
                    // Make event for Auto Dispatch Control Information Update.
                    //===================================================================================
                    Inputs.AutoDispatchControlEventMakeIn autoDispatchControlEventMakeIn = new Inputs.AutoDispatchControlEventMakeIn();
                    autoDispatchControlEventMakeIn.setLotID(lotAutoDispatchControlUpdateInfo.getLotID());
                    autoDispatchControlEventMakeIn.setAutoDispatchControlUpdateInfo(autoDispatchControlUpdateInfo);
                    autoDispatchControlEventMakeIn.setClaimMemo(autoDispatchConfigModifyReqParams.getClaimMemo());
                    log.info("Step7 - autoDispatchControlEvent_Make");
                    try {
                        eventMethod.autoDispatchControlEventMake(objCommon, autoDispatchControlEventMakeIn);
                    } catch (ServiceException e) {
                        e.setData(lotAutoDispatchControlUpdateInfo);
                        throw e;
                    }
                }
            }
        }
        return autoDispatchConfigModifyReqResult;
    }

    @Override
    public void sxCarrierDispatchAttrChgReq(Infos.ObjCommon objCommon, Params.CarrierDispatchAttrChgReqParm carrierTransferJobEndRptParams) {
        //-----------------------------------------------------------
        //    Object Lock for Cassette
        //-----------------------------------------------------------
        objectLockMethod.objectLock(objCommon, CimCassette.class, carrierTransferJobEndRptParams.getCassetteID());

        //-----------------------------------------------------------
        //    Update cassette's dispatching attribute
        //-----------------------------------------------------------

        cassetteMethod.cassetteDispatchAttributeUpdate(objCommon, carrierTransferJobEndRptParams.getCassetteID(), carrierTransferJobEndRptParams.getSetFlag(), carrierTransferJobEndRptParams.getActionCode());
        //-----------------------------------------------------------
        //    Return to Caller
        //-----------------------------------------------------------
    }

    @Override
    public Results.MoveInReserveCancelForIBReqResult sxMoveInReserveCancelForIBReqService(Infos.ObjCommon objCommon, Params.MoveInReserveCancelForIBReqParams params) {
        Results.MoveInReserveCancelForIBReqResult out = new Results.MoveInReserveCancelForIBReqResult();

        //Step1 equipment_categoryVsTxID_CheckCombination
        //=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
        //   Check Process
        //=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
        equipmentMethod.equipmentCategoryVsTxIDCheckCombination(objCommon, params.getEquipmentID());
        //=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
        //   Object Lock Process
        //=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
        // Get required equipment lock mode
        // step2 - object_lockMode_Get
        Inputs.ObjLockModeIn objLockModeIn = new Inputs.ObjLockModeIn();
        objLockModeIn.setObjectID(params.getEquipmentID());
        objLockModeIn.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
        objLockModeIn.setFunctionCategory(TransactionIDEnum.START_LOTS_RESERVATION_CANCEL_FOR_INTERNAL_BUFFER_REQ.getValue());
        objLockModeIn.setUserDataUpdateFlag(false);
        Outputs.ObjLockModeOut objLockModeOut = objectMethod.objectLockModeGet(objCommon, objLockModeIn);
        Long lockMode = objLockModeOut.getLockMode();
        // step3 - advanced_object_Lock
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
            /*-------------------------*/
            /*   Lock Machine Object   */
            /*-------------------------*/
            // step4 - object_Lock
            objectLockMethod.objectLock(objCommon, CimMachine.class, params.getEquipmentID());
        }
        //-------------------------
        //   Get ControlJob Info
        //-------------------------
        //Step5 - controlJob_startReserveInformation_Get
        Outputs.ObjControlJobStartReserveInformationOut strControlJobStartReserveInformationGetOut = controlJobMethod.controlJobStartReserveInformationGet(objCommon, params.getControlJobID(), false);
        //---------------------------------------------------
        //   Prepare strStartCassette for Working-Valiable
        //---------------------------------------------------
        List<Infos.StartCassette> strStartCassette = strControlJobStartReserveInformationGetOut.getStartCassetteList();
        int scLen = CimArrayUtils.getSize(strStartCassette);
        /*--------------------------------------------*/
        /*                                            */
        /*        Port Object Lock Process            */
        /*                                            */
        /*--------------------------------------------*/
        //--------------------------------
        //   Lock Port object (To)
        //--------------------------------
        // step6 object_LockForEquipmentResource
        if (!lockMode.equals(BizConstant.SP_EQP_LOCK_MODE_WRITE)){
            for (int i = 0; i < scLen; i++){
                objectLockMethod.objectLockForEquipmentResource(objCommon, params.getEquipmentID(), strStartCassette.get(i).getLoadPortID(), BizConstant.SP_CLASSNAME_POSPORTRESOURCE);
            }
        } else {
            // step7 equipment_portInfoForInternalBuffer_GetDR
            Infos.EqpPortInfo eqpPortInfo = equipmentMethod.equipmentPortInfoForInternalBufferGetDR(objCommon, params.getEquipmentID());
            List<Infos.EqpPortStatus> eqpPortStatuses = eqpPortInfo.getEqpPortStatuses();
            int lenPortInfo = CimArrayUtils.getSize(eqpPortStatuses);
            // step8 object_LockForEquipmentResource
            for (int i = 0; i < lenPortInfo; i++){
                objectLockMethod.objectLockForEquipmentResource(objCommon, params.getEquipmentID(), eqpPortStatuses.get(i).getPortID(), BizConstant.SP_CLASSNAME_POSPORTRESOURCE);
            }
        }
        if (!lockMode.equals(BizConstant.SP_EQP_LOCK_MODE_WRITE)){
            // step9 advanced_object_LockForEquipmentResource
            int nCassetteLen = CimArrayUtils.getSize(strStartCassette);
            for (int i = 0; i < nCassetteLen; i++){
                Inputs.ObjAdvancedObjectLockForEquipmentResourceIn objAdvancedObjectLockForEquipmentResourceIn = new Inputs.ObjAdvancedObjectLockForEquipmentResourceIn();
                objAdvancedObjectLockForEquipmentResourceIn.setEquipmentID(params.getEquipmentID());
                objAdvancedObjectLockForEquipmentResourceIn.setClassName(BizConstant.SP_CLASSNAME_POSMATERIALLOCATION_BYCASTID);
                objAdvancedObjectLockForEquipmentResourceIn.setObjectID(strStartCassette.get(i).getCassetteID());
                objAdvancedObjectLockForEquipmentResourceIn.setObjectLockType(BizConstant.SP_OBJECTLOCK_LOCKTYPE_WRITE);
                objAdvancedObjectLockForEquipmentResourceIn.setBufferResourceName(strStartCassette.get(i).getLoadPurposeType());
                objAdvancedObjectLockForEquipmentResourceIn.setBufferResourceLockType(BizConstant.SP_OBJECTLOCK_LOCKTYPE_READ);
                objectLockMethod.advancedObjectLockForEquipmentResource(objCommon, objAdvancedObjectLockForEquipmentResourceIn);
            }
            // step10 object_Lock
            objectLockMethod.objectLock(objCommon, CimControlJob.class, params.getControlJobID());
        }


        //-------------------------------
        //  Lock Cassette / Lot Object
        //-------------------------------
        List<ObjectIdentifier> lotIDs = new ArrayList<>();
        List<ObjectIdentifier> cassetteIDs = new ArrayList<>();
        for (Infos.StartCassette startCassette : strStartCassette) {
            cassetteIDs.add(startCassette.getCassetteID());
            List<Infos.LotInCassette> lotInCassetteList = startCassette.getLotInCassetteList();
            for (Infos.LotInCassette lotInCassette : lotInCassetteList) {
                //---------------------------
                //   Omit Not-OpeStart Lot
                //---------------------------
                if(CimBooleanUtils.isFalse(lotInCassette.getMoveInFlag())){
                    continue;
                }
                lotIDs.add(lotInCassette.getLotID());
            }
        }
        //-----------------------------
        //   Lock Cassette/Lot Object
        //-----------------------------
        // step11 objectSequence_Lock
        objectLockMethod.objectSequenceLock(objCommon, CimCassette.class, cassetteIDs);
        // step12 objectSequence_Lock
        objectLockMethod.objectSequenceLock(objCommon, CimLot.class, lotIDs);

        // add season by ho
        Params.MoveInReserveCancelReqParams seasonMoveInReserveCancelParam=new Params.MoveInReserveCancelReqParams();
        seasonMoveInReserveCancelParam.setUser(params.getUser());
        seasonMoveInReserveCancelParam.setEquipmentID(params.getEquipmentID());
        seasonMoveInReserveCancelParam.setControlJobID(params.getControlJobID());
        seasonMoveInReserveCancelParam.setOpeMemo(params.getOpeMemo());
        seasoningService.sxSeasonForMoveInReserveCancel(objCommon,seasonMoveInReserveCancelParam);

        //=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
        //   Port Status Change Process    (To Equipment)
        //=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
        for (Infos.StartCassette startCassette : strStartCassette) {
            //Step13 equipment_portInfo_Get
            Infos.EqpPortInfo eqpPortInfo = equipmentMethod.equipmentPortInfoGet(objCommon, params.getEquipmentID());
            List<Infos.EqpPortStatus> eqpPortStatuses = eqpPortInfo.getEqpPortStatuses();
            for (Infos.EqpPortStatus eqpPortStatus : eqpPortStatuses) {
                if(!ObjectIdentifier.equalsWithValue(startCassette.getLoadPortID(), eqpPortStatus.getPortID())){
                    continue;
                }
                if(CimStringUtils.equals(eqpPortStatus.getDispatchState(), BizConstant.SP_PORTRSC_DISPATCHSTATE_DISPATCHED)){
                    log.info("dispatchState == Dispatched");
                    if (CimStringUtils.equals(eqpPortStatus.getPortState(), BizConstant.SP_PORTRSC_PORTSTATE_LOADREQ)
                            || CimStringUtils.equals(eqpPortStatus.getPortState(), BizConstant.SP_PORTRSC_PORTSTATE_UNKNOWN)
                            || CimStringUtils.equals(eqpPortStatus.getPortState(), BizConstant.SP_PORTRSC_PORTSTATE_UNLOADREQ)) {
                        log.info("portState == LoadReq or UnloadReq or UnKnown");
                        // change to Required
                        // step14 - equipment_dispatchState_Change
                        equipmentMethod.equipmentDispatchStateChange(objCommon, params.getEquipmentID(), eqpPortStatus.getPortID()
                                , BizConstant.SP_PORTRSC_DISPATCHSTATE_REQUIRED, null, null, null, null);
                    }
                    break;
                }
            }
        }
        //-------------------------------------
        //   call cassette_APCInformation_GetDR
        //-------------------------------------
        // step15 - cassette_APCInformation_GetDR
        List<Infos.ApcBaseCassette> apcBaseCassettes = null;
        try {
            apcBaseCassettes = cassetteMethod.cassetteAPCInformationGetDR(objCommon, params.getEquipmentID(), strStartCassette);
        } catch (ServiceException e) {
            if (Validations.isEquals(retCodeConfig.getSystemError(), e.getCode())){
                throw e;
            }
        }

        /*=-=-=-=-=-=-=-=-=-=*/
        /*   Check Process   */
        /*=-=-=-=-=-=-=-=-=-=*/
        //-----------------------------------------------------------------------
        //
        //   Check Process for Cassette
        //
        //   The following conditions are checked by this object
        //
        //   - dispatchState
        //   - controlJobID
        //-----------------------------------------------------------------------

        //【step16】cassette_CheckConditionForStartReserveCancel
        cassetteMethod.cassetteCheckConditionForStartReserveCancel(objCommon, params.getControlJobID(), strStartCassette);
        /*-----------------------------------------------------------------------*/
        /*                                                                       */
        /*   Check Process for Lot                                               */
        /*                                                                       */
        /*   The following conditions are checked by this object                 */
        /*                                                                       */
        /*   - lotProcessState                                                   */
        /*   - controlJobID                                                      */
        /*                                                                       */
        /*-----------------------------------------------------------------------*/
        //【step17】lot_CheckConditionForStartReserveCancel
        lotMethod.lotCheckConditionForStartReserveCancel(objCommon, params.getControlJobID(), strStartCassette);

        /*-----------------------------------------------------------------------*/
        /*                                                                       */
        /*   Check Process for Equipment                                         */
        /*                                                                       */
        /*   The following conditions are checked by this object                 */
        /*                                                                       */
        /*   - reservedControlJobID                                              */
        /*                                                                       */
        /*-----------------------------------------------------------------------*/
        //【step18】equipment_CheckConditionForStartReserveCancel
        equipmentMethod.equipmentCheckConditionForStartReserveCancel(objCommon, params.getEquipmentID(), params.getControlJobID());

        // step19 equipment_StartReserveCancelForInternalBuffer
        equipmentMethod.equipmentStartReserveCancelForInternalBuffer(objCommon, params.getEquipmentID(),params.getControlJobID());

        //Step20 - process_startReserveInformation_Clear
        processMethod.processStartReserveInformationClear(objCommon,params.getControlJobID(), strStartCassette);

        //Step21 - equipment_reservedControlJobID_Clear
        equipmentMethod.equipmentReservedControlJobIDClear(objCommon, params.getEquipmentID(), params.getControlJobID());
        for (Infos.StartCassette startCassette : strStartCassette) {
            List<Infos.LotInCassette> lotInCassetteList = startCassette.getLotInCassetteList();
            for (Infos.LotInCassette lotInCassette : lotInCassetteList) {
                // Omit Not-OpeStart Lot
                if(CimBooleanUtils.isFalse(lotInCassette.getMoveInFlag())){
                    continue;
                }
                //---------------------------------------------------------------------------------------
                //  Check if schedule change by lot information change is reserved for this operation
                //  If schedule change is reserved, delete the reservation and send e-mail to lot owner
                //---------------------------------------------------------------------------------------
                // step22 - lot_currentOperationInfo_Get
                Outputs.ObjLotCurrentOperationInfoGetOut strLotCurrentOperationInfoGetOut = lotMethod.lotCurrentOperationInfoGet(objCommon, lotInCassette.getLotID());

                String targetRouteID = strLotCurrentOperationInfoGetOut.getRouteID().getValue();
                String targetOperationNumber = strLotCurrentOperationInfoGetOut.getOperationNumber();
                //Step23 - schdlChangeReservation_GetListDR__110
                Inputs.ObjScheduleChangeReservationGetListIn scheduleChangeReservationParams = new Inputs.ObjScheduleChangeReservationGetListIn();
                scheduleChangeReservationParams.setObjectID(lotInCassette.getLotID().getValue());
                scheduleChangeReservationParams.setObjectID(BizConstant.SP_SCHDL_CHG_OBJTYPE_LOT);
                scheduleChangeReservationParams.setTargetRouteID(targetRouteID);
                scheduleChangeReservationParams.setTargetOperationNumber(targetOperationNumber);
                // LOTINFO_CHANGE_FLAG is 1. this flag value to be optimized as a constant
                scheduleChangeReservationParams.setLotInfoChangeFlag(2L);
                List<Infos.SchdlChangeReservation> scheduleChangeReservationResult = scheduleChangeReservationMethod.schdlChangeReservationGetListDR(objCommon, scheduleChangeReservationParams);
                long rsvLength = scheduleChangeReservationResult.size();
                if(rsvLength>0){
                    List<Infos.SchdlChangeReservation> scheduleChangeReservationList = scheduleChangeReservationResult;
                    for (Infos.SchdlChangeReservation scheduleChangeReservation : scheduleChangeReservationList) {
                        //Step24 - schdlChangeReservation_DeleteDR__110
                        scheduleChangeReservationMethod.schdlChangeReservationDeleteDR(objCommon, scheduleChangeReservation);
                        //Step25 -Send a message as the result of SPC Check
                        StringBuffer messageSb = new StringBuffer();
                        messageSb.append("This message was sent because reservation for \"Lot Information Change\" is canceled.\n")
                                .append("Schedule Change Reservation Info.\n")
                                .append("LotID      : ").append(lotInCassette.getLotID().getValue()).append("\n")
                                .append("Product ID : ").append(scheduleChangeReservation.getProductID().getValue()).append("\n")
                                .append("Route ID   : ").append(scheduleChangeReservation.getRouteID().getValue()).append("\n")
                                .append("Ope.No     : ").append(scheduleChangeReservation.getOperationNumber()).append("\n")
                                .append("SubLotType : ").append(scheduleChangeReservation.getSubLotType()).append("\n");
                        messageMethod.messageDistributionMgrPutMessage(objCommon, new ObjectIdentifier(BizConstant.SP_SYSTEMMSGCODE_SCRNOTICE), lotInCassette.getLotID(),
                                "", params.getEquipmentID(), strLotCurrentOperationInfoGetOut.getRouteID(), strLotCurrentOperationInfoGetOut.getOperationNumber(),
                                "", messageSb.toString());
                    }
                }
            }
        }
        //------------------------
        //   Delete Control Job
        //------------------------
        //Step26 - txCJStatusChangeReq
        Params.CJStatusChangeReqParams cjStatusChangeReqParams = new Params.CJStatusChangeReqParams();
        cjStatusChangeReqParams.setControlJobAction(BizConstant.SP_CONTROLJOBACTION_TYPE_DELETE);
        cjStatusChangeReqParams.setControlJobID(ObjectIdentifier.build(params.getControlJobID().getValue(), null));
        cjStatusChangeReqParams.setControlJobCreateRequest(new Infos.ControlJobCreateRequest());
        Results.CJStatusChangeReqResult cjStatusChangeReqOut = controlJobProcessJobService.sxCJStatusChangeReqService(objCommon, cjStatusChangeReqParams);

        //---------------------------------------------------
        //   Cassette Related Information Update Procedure
        //---------------------------------------------------
        for (Infos.StartCassette startCassette : strStartCassette) {
            // step27 cassette_dispatchState_Change
            // Change Cassette's Dispatch State to FALSE
            cassetteMethod.cassetteDispatchStateChange(objCommon, startCassette.getCassetteID(), false);
        }
        //------------------------------------------------------------------------
        //   Send MoveInReserveCancelReq() to EAP Procedure
        //------------------------------------------------------------------------
        // step28 TCSMgr_SendMoveInReserveCancelForIBReq
//        Inputs.SendMoveInReserveCancelForIBReqIn sendMoveInReserveCancelForIBReqIn = new Inputs.SendMoveInReserveCancelForIBReqIn();
//        sendMoveInReserveCancelForIBReqIn.setObjCommonIn(objCommon);
//        sendMoveInReserveCancelForIBReqIn.setEquipmentID(params.getEquipmentID());
//        sendMoveInReserveCancelForIBReqIn.setControlJobID(params.getControlJobID());
//        Outputs.SendMoveInReserveCancelForIBReqOut sendMoveInReserveCancelReqOut = null;
//        sendMoveInReserveCancelReqOut = (Outputs.SendMoveInReserveCancelForIBReqOut) tcsMethod.sendTCSReq(TCSReqEnum.sendMoveInReserveCancelForIBReq,sendMoveInReserveCancelForIBReqIn);
        String tmpSleepTimeValue = StandardProperties.OM_EAP_CONNECT_SLEEP_TIME.getValue();
        String tmpRetryCountValue = StandardProperties.OM_EAP_CONNECT_RETRY_COUNT.getValue();
        Long sleepTimeValue = 0L;
        Long retryCountValue = 0L;

        if (0 == CimStringUtils.length(tmpSleepTimeValue)) {
            sleepTimeValue = BizConstant.SP_DEFAULT_SLEEP_TIME_TCS;
        } else {
            sleepTimeValue = CimNumberUtils.longValue(tmpSleepTimeValue);
        }

        if (0 == CimStringUtils.length(tmpRetryCountValue)) {
            retryCountValue = BizConstant.SP_DEFAULT_RETRY_COUNT_TCS;
        } else {
            retryCountValue = CimNumberUtils.longValue(tmpRetryCountValue);
        }

        for (int retryNum = 0; retryNum < (retryCountValue + 1); retryNum++) {
            log.info("{} {}", "loop to retryCountValue + 1", retryNum);
            /*--------------------------*/
            /*    Send Request to EAP   */
            /*--------------------------*/
            IEAPRemoteManager eapRemoteManager = eapMethod.eapRemoteManager(objCommon,params.getUser(),params.getEquipmentID(),null,true);
            if (null == eapRemoteManager) {
                log.info("MES not configure EAP host");
                break;
            }
            try {
                Object moveInReserveCancelForIBReqEapOut = eapRemoteManager.sendMoveInReserveCancelForIBReq(params);
                log.info("Now EAP subSystem is alive!! Go ahead");
                break;
            } catch (ServiceException ex) {
                if (Validations.isEquals(ex.getCode(), retCodeConfig.getTcsNoResponse())) {
                    log.info("{} {}", "EAP subsystem has return NO_RESPONSE!! just retry now!!  now count...", retryNum);
                    log.info("{} {}", "now sleeping... ", sleepTimeValue);
                    if (retryNum != retryCountValue){
                        try {
                            Thread.sleep(sleepTimeValue);
                            continue;
                        } catch (InterruptedException e) {
                            ex.addSuppressed(e);
                            Thread.currentThread().interrupt();
                            throw ex;
                        }
                    }else {
                        Validations.check(true,retCodeConfig.getTcsNoResponse());
                    }
                } else {
                    Validations.check(true,new OmCode(ex.getCode(),ex.getMessage()));
                }
            }
        }

        //-----------------------------------------------
        //   call APCRuntimeCapability_DeleteDR
        //-----------------------------------------------
        // 【Step29】Call APCRuntimeCapability_DeleteDR
        apcMethod.apcRuntimeCapabilityDeleteDR(objCommon, params.getControlJobID());
        //--------------------------------------------------
        //   call APCMgr_SendControlJobInformationDR
        //--------------------------------------------------
        // 【Step30】Call APCMgr_SendControlJobInformationDR
        //   Wafer information in APCBaseCassette is not necessary for reserve cancel,
        //   but it is required by wrapper for process xml which satisfy dtd.
        //   So, now we add dummy wafer data into strCassette_APCInformation_GetDR_out.strAPCBaseCassetteList.
        int bcLen = CimArrayUtils.getSize(apcBaseCassettes);
        for (int bcIdx = 0; bcIdx < bcLen; bcIdx++){
            Infos.ApcBaseCassette apcBaseCassette = apcBaseCassettes.get(bcIdx);
            List<Infos.ApcBaseLot> apcBaseLotList = apcBaseCassette.getApcBaseLotList();
            int blLen = CimArrayUtils.getSize(apcBaseLotList);
            for (int blIdx = 0; blIdx < blLen; blIdx++){
                Infos.ApcBaseLot apcBaseLot = apcBaseLotList.get(blIdx);
                Infos.ApcBaseWafer apcBaseWafer = new Infos.ApcBaseWafer();
                apcBaseWafer.setWaferID("");
                apcBaseWafer.setSlotNumber(0L);
                apcBaseWafer.setControlWaferFlag(false);
                apcBaseWafer.setSendAheadWaferFlag(false);
                apcBaseWafer.setProcessFlag(false);
                apcBaseWafer.setExperimentSplitWafer(false);
                apcBaseLot.setApcBaseWaferList(Collections.singletonList(apcBaseWafer));
            }
        }
        int retCode = 0;
        try {
            apcMethod.APCMgrSendControlJobInformationDR(objCommon, params.getEquipmentID(), cjStatusChangeReqOut.getControlJobID(), BizConstant.SP_APC_CONTROLJOBSTATUS_CANCELED, apcBaseCassettes);
        } catch (ServiceException e) {
            retCode = e.getCode();
            if (!Validations.isEquals(retCodeConfigEx.getOkNoIF(), e.getCode())){
                throw e;
            }
        }

        if (retCode == 0){
            String tmpString = params.getAPCIFControlStatus();
            params.setAPCIFControlStatus(BizConstant.SP_APC_CONTROLJOBSTATUS_CREATED);
        }
        out.setStartCassetteList(strStartCassette);
        return out;
    }

    @Override
    public Results.MoveInReserveCancelReqResult sxMoveInReserveCancelReqService(Infos.ObjCommon objCommon, Params.MoveInReserveCancelReqParams params) {

        ObjectIdentifier equipmentID = params.getEquipmentID();
        ObjectIdentifier controlJobID = params.getControlJobID();

        //【step1】object lock process
        log.debug("【step1】object lock process");
        Inputs.ObjLockModeIn objLockModeIn = new Inputs.ObjLockModeIn();
        objLockModeIn.setObjectID(equipmentID);
        objLockModeIn.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
        objLockModeIn.setFunctionCategory(TransactionIDEnum.START_LOTS_RESERVATION_CANCEL_REQ.getValue());
        objLockModeIn.setUserDataUpdateFlag(false);
        Outputs.ObjLockModeOut objLockModeOut = objectMethod.objectLockModeGet(objCommon, objLockModeIn);
        Long lockMode = objLockModeOut.getLockMode();
        Inputs.ObjAdvanceLockIn strAdvancedobjecLockin = new Inputs.ObjAdvanceLockIn();
        if (!lockMode.equals(BizConstant.SP_EQP_LOCK_MODE_WRITE)){
            // Lock Equipment Main Object
            List<String> dummySeq;
            dummySeq = new ArrayList<>(0);
            strAdvancedobjecLockin.setObjectID(equipmentID);
            strAdvancedobjecLockin.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
            strAdvancedobjecLockin.setObjectType(BizConstant.SP_OBJECTLOCK_OBJECTTYPE_MAINOBJECT);
            strAdvancedobjecLockin.setLockType(objLockModeOut.getRequiredLockForMainObject());
            strAdvancedobjecLockin.setKeyList(dummySeq);

            objectLockMethod.advancedObjectLock(objCommon, strAdvancedobjecLockin);
        } else {
            /*-------------------------*/
            /*   Lock Machine Object   */
            /*-------------------------*/
            objectLockMethod.objectLock(objCommon, CimMachine.class, equipmentID);
        }
        //【step2】Transaction ID and eqp Category consisytency Check
        log.info("【step2】Transaction ID and eqp Category consisytency Check");
        equipmentMethod.equipmentCategoryVsTxIDCheckCombination(objCommon, equipmentID);

        //【step3】get control job startReserve information
        log.info("【step3】get control job startReserve information");
        Outputs.ObjControlJobStartReserveInformationOut informationOutRetCode
                = controlJobMethod.controlJobStartReserveInformationGet(objCommon, controlJobID, false);

        List<Infos.StartCassette> startCassetteList = informationOutRetCode.getStartCassetteList();
        int startCassetteSize = CimArrayUtils.getSize(startCassetteList);
        /**************************************************************************************************************/
        /*【step4】port object lock process                                                         */
        /**************************************************************************************************************/

        log.debug("【step4】todo - port object lock process");
        // lock port object(to)
        //【step4-1】get All Ports being in the same port Group as ToPort
        log.debug("【step4-1】get All Ports being in the same port Group as ToPort");
        Infos.EqpPortInfo portInfoGetDROut = portMethod.portResourceAllPortsInSameGroupGet(objCommon,equipmentID,  startCassetteList.get(0).getLoadPortID());

        //【step4-2】lock all ports being in the same port group as ToPort.
        log.debug("【step4-2】lock all ports being in the same port group as ToPort.");

        int lenToPort = CimArrayUtils.getSize(portInfoGetDROut.getEqpPortStatuses());
        for (int i = 0; i < lenToPort; i++) {
            ObjectIdentifier portID = portInfoGetDROut.getEqpPortStatuses().get(i).getPortID();
            objectLockMethod.objectLockForEquipmentResource(objCommon, equipmentID, portID, BizConstant.SP_CLASSNAME_POSPORTRESOURCE);
        }
        //【step4-3】lock port object(form)
        log.debug("【step4-3】todo - lock port object(form)");
        for (int i = 0; i < startCassetteSize; i++){
            /*-----------------------------------*/
            /*  Check cassette transfer status   */
            /*-----------------------------------*/
            String transferState = cassetteMethod.cassetteTransferStateGet(objCommon, startCassetteList.get(i).getCassetteID());
            if (CimStringUtils.equals(transferState, BizConstant.SP_TRANSSTATE_EQUIPMENTIN)){
                /*------------------------------------*/
                /*   Get Cassette Info in Equipment   */
                /*------------------------------------*/
                Outputs.ObjCassetteEquipmentIDGetOut objCassetteEquipmentIDGetOut = cassetteMethod.cassetteEquipmentIDGet(objCommon, startCassetteList.get(i).getCassetteID());
                Infos.EqpPortInfo eqpPortInfo = equipmentMethod.equipmentPortInfoGet(objCommon, objCassetteEquipmentIDGetOut.getEquipmentID());
                /*----------------------------------------------------------*/
                /*  Lock port object which has the specified cassette.      */
                /*----------------------------------------------------------*/
                List<Infos.EqpPortStatus> eqpPortStatuses = eqpPortInfo.getEqpPortStatuses();
                int lenFromPort = CimArrayUtils.getSize(eqpPortStatuses);
                for (int j = 0; j < lenFromPort; j++){
                    if (ObjectIdentifier.equalsWithValue(startCassetteList.get(i).getCassetteID(), eqpPortStatuses.get(j).getLoadedCassetteID())){
                        objectLockMethod.objectLockForEquipmentResource(objCommon, objCassetteEquipmentIDGetOut.getEquipmentID(), eqpPortStatuses.get(j).getPortID(), BizConstant.SP_CLASSNAME_POSPORTRESOURCE);
                    }
                }
            }
        }
        if (!lockMode.equals(BizConstant.SP_EQP_LOCK_MODE_WRITE)){
            /*------------------------------*/
            /*   Lock ControlJob Object     */
            /*------------------------------*/
            objectLockMethod.objectLock(objCommon, CimControlJob.class, controlJobID);
        }
        //【step4-4】lock cassette / lot object
        log.debug("【step4-4】 lock cassette / lot object");
        List<ObjectIdentifier> lotIDs = new ArrayList<>();
        List<ObjectIdentifier> cassetteIDs = new ArrayList<>();
        for (int i = 0; i < startCassetteSize; i++){
            Infos.StartCassette startCassette = startCassetteList.get(i);
            cassetteIDs.add(startCassette.getCassetteID());
            List<Infos.LotInCassette> lotInCassetteList = startCassette.getLotInCassetteList();
            int lcLen = CimArrayUtils.getSize(lotInCassetteList);
            for (int j = 0; j < lcLen; j++){
                /*---------------------------*/
                /*   Omit Not-OpeStart Lot   */
                /*---------------------------*/
                if (CimBooleanUtils.isFalse(lotInCassetteList.get(j).getMoveInFlag())){
                    continue;
                }
                lotIDs.add(lotInCassetteList.get(j).getLotID());
            }
        }
        //--------------------------------------------------
        //   Lock Destination Cassette by SLM Reserved
        //--------------------------------------------------
        Infos.EqpContainerInfo eqpContainerInfo = equipmentMethod.equipmentContainerInfoGetDR(objCommon, equipmentID);
        List<Infos.EqpContainer> eqpContainerList = eqpContainerInfo.getEqpContainerList();
        int lenEqpCont = CimArrayUtils.getSize(eqpContainerList);
        for (int nCst = 0; nCst < startCassetteSize; nCst++){
            for (int nECT = 0; nECT < lenEqpCont; nECT++){
                List<Infos.EqpContainerPosition> eqpContainerPositionList = eqpContainerList.get(nECT).getEqpContainerPosition();
                int lenEqpContPos = CimArrayUtils.getSize(eqpContainerPositionList);
                for (int nECP = 0; nECP < lenEqpContPos; nECP++){
                    Infos.EqpContainerPosition strCtnPstInfo = eqpContainerPositionList.get(nECP);
                    if (ObjectIdentifier.equalsWithValue(startCassetteList.get(nCst).getCassetteID(), strCtnPstInfo.getSrcCassetteID())
                            && (!ObjectIdentifier.isEmptyWithValue(strCtnPstInfo.getDestCassetteID())
                            && !ObjectIdentifier.equalsWithValue(strCtnPstInfo.getSrcCassetteID(), strCtnPstInfo.getDestCassetteID()))){
                        objectLockMethod.objectLock(objCommon, CimCassette.class, strCtnPstInfo.getDestCassetteID());
                    }
                }
            }
        }
        /*------------------------------*/
        /*   Lock Cassette/Lot Object   */
        /*-------------------------------*/
        objectLockMethod.objectSequenceLock(objCommon, CimCassette.class, cassetteIDs);
        objectLockMethod.objectSequenceLock(objCommon, CimLot.class, lotIDs);

        // add season by ho
        seasoningService.sxSeasonForMoveInReserveCancel(objCommon,params);
        //-----------------------------------------------
        //   Lock Equipment Container Position Objects
        //-----------------------------------------------
        Inputs.ObjObjectLockForEquipmentContainerPositionIn objObjectLockForEquipmentContainerPositionIn = new Inputs.ObjObjectLockForEquipmentContainerPositionIn();
        objObjectLockForEquipmentContainerPositionIn.setEquipmentID(equipmentID);
        objObjectLockForEquipmentContainerPositionIn.setControlJobID(controlJobID);
        objectLockMethod.objectLockForEquipmentContainerPosition(objCommon, objObjectLockForEquipmentContainerPositionIn);
        //【step5】check eqp availability of TakeOutIn transfer
        log.info("【step5】check eqp availability of TakeOutIn transfer");
        Long eqpTaskOutInSupport = equipmentMethod.equipmentTakeOutInModeCheck(objCommon, equipmentID);

        /**************************************************************************************************************/
        /*【step6】port status change process（to eqp）                                                         */
        /*   - 【step6-1】equipment_portInfo_Get                                                                      */
        /*   - 【step6-2】equipment_dispatchState_Change                                                              */
        /**************************************************************************************************************/
        log.info("【step6】port status change process（to eqp）");
        for (int i = 0; i < startCassetteSize; i++) {
            Infos.StartCassette startCassetteObj = startCassetteList.get(i);
            int basePgNo = 0;
            ObjectIdentifier loadPortID = startCassetteObj.getLoadPortID();
            log.debug(String.format("the loadPortID:%s", loadPortID));
            //【step6-1】get eqp port info
            log.debug("【step6-1】get eqp port info");
            Infos.EqpPortInfo eqpPortInfo = equipmentMethod.equipmentPortInfoGet(objCommon, equipmentID);
            int lenPortInfo = CimArrayUtils.getSize(eqpPortInfo.getEqpPortStatuses());
            for (int j = 0; j < lenPortInfo; j++) {
                Infos.EqpPortStatus eqpPortStatusObj = eqpPortInfo.getEqpPortStatuses().get(j);
                ObjectIdentifier portID = eqpPortStatusObj.getPortID();
                log.debug("the portID:%s", portID);
                String dispatchState = eqpPortStatusObj.getDispatchState();
                log.debug("the dispatchState:%s", dispatchState);
                if (!ObjectIdentifier.equalsWithValue(loadPortID, portID)) {
                    log.debug("not same the  portID, continue...");
                    continue;
                }
                String portState = eqpPortStatusObj.getPortState();
                if (CimStringUtils.equals(BizConstant.SP_PORTRSC_DISPATCHSTATE_DISPATCHED, dispatchState)) {
                    log.debug("the dispatchState = 'Dispatched'");
                    if (CimStringUtils.equals(BizConstant.SP_PORTRSC_PORTSTATE_LOADREQ, portState)
                            || CimStringUtils.equals(BizConstant.SP_PORTRSC_PORTSTATE_UNLOADREQ, portState)
                            || CimStringUtils.equals(BizConstant.SP_PORTRSC_PORTSTATE_UNKNOWN, portState)) {
                        log.debug("the portState = LoadReq or UnLoadReq or '-'");
                        //【step6-2】change eqp dispatch state
                        log.debug("【step6-2】change eqp dispatch state");
                        ObjectIdentifier dummyOI = null;
                        equipmentMethod.equipmentDispatchStateChange(objCommon, equipmentID, portID,
                                BizConstant.SP_PORTRSC_DISPATCHSTATE_REQUIRED, dummyOI, dummyOI, dummyOI, dummyOI);
                        ObjectIdentifier dispatchUnLoadCassetteID = eqpPortStatusObj.getDispatchUnloadCassetteID();
                        if (1 == eqpTaskOutInSupport && !ObjectIdentifier.isEmptyWithValue(dispatchUnLoadCassetteID)) {
                            log.debug("this reservation is TakeOutIn mode!!!");
                            /******************************************************************************************/
                            /* This port has TakeOutIn reservation. dispatch status should be re-changed              */
                            /* for take out carrier                                                                   */
                            /*    1. dispatch State ==> Dispatched.                                                   */
                            /*    2. dispatch Unload Carrier ==> dispUnLoadCastID.                                    */
                            /******************************************************************************************/
                            equipmentMethod.equipmentDispatchStateChange(objCommon,equipmentID, portID,
                                    BizConstant.SP_PORTRSC_DISPATCHSTATE_DISPATCHED, dummyOI, dummyOI, dummyOI,
                                    dispatchUnLoadCassetteID);
                        }
                    }
                    basePgNo = j;
                    break;
                }
            }
            // find same port group
            log.debug("find same port group");
            for (int j = 0; j < lenPortInfo; j++) {
                // omit base port
                if (j == basePgNo) {
                    continue;
                }
                // omit different group's port
                Infos.EqpPortStatus eqpPortStatusObj = eqpPortInfo.getEqpPortStatuses().get(j);
                String portGroup = eqpPortStatusObj.getPortGroup();
                String basePGNoPortGroup = eqpPortInfo.getEqpPortStatuses().get(basePgNo).getPortGroup();
                if (!CimStringUtils.equals(portGroup, basePGNoPortGroup)) {
                    log.debug("not same portGroup, continue...");
                    continue;
                }
                String portState = eqpPortStatusObj.getPortState();
                // check portState
                log.debug("check portState, the portState:%s", portState);
                if (!CimStringUtils.equals(BizConstant.SP_PORTRSC_PORTSTATE_LOADREQ, portState)
                        && !CimStringUtils.equals(BizConstant.SP_PORTRSC_PORTSTATE_UNLOADREQ, portState)
                        && !CimStringUtils.equals(BizConstant.SP_PORTRSC_PORTSTATE_UNKNOWN, portState)) {
                    log.debug("portState != LoadReq or UnloadReq or '-', continue...");
                    continue;
                }
                // check dispatch state
                log.debug("check dispatch state");
                String dispatchState = eqpPortStatusObj.getDispatchState();
                String portID = eqpPortStatusObj.getPortID().getValue();
                if (CimStringUtils.equals(BizConstant.SP_PORTRSC_DISPATCHSTATE_DISPATCHED, dispatchState)
                        || CimStringUtils.equals(BizConstant.SP_PORTRSC_DISPATCHSTATE_NOTDISPATCHED, dispatchState)) {
                    log.debug("same portGroup = Dispatched or NotDispatched");
                    ObjectIdentifier dummyOI = null;
                    equipmentMethod.equipmentDispatchStateChange(objCommon, equipmentID, new ObjectIdentifier(portID),
                            BizConstant.SP_PORTRSC_DISPATCHSTATE_REQUIRED, dummyOI, dummyOI, dummyOI, dummyOI);
                    ObjectIdentifier dispatchUnLoadCassetteID = eqpPortStatusObj.getDispatchUnloadCassetteID();
                    if (1 == eqpTaskOutInSupport && !ObjectIdentifier.isEmptyWithValue(dispatchUnLoadCassetteID)) {
                        /******************************************************************************************/
                        /* This port has TakeOutIn reservation. dispatch status should be re-changed              */
                        /* for take out carrier                                                                   */
                        /*    1. dispatch State ==> Dispatched.                                                   */
                        /*    2. dispatch Unload Carrier ==> dispUnLoadCastID.                                    */
                        /******************************************************************************************/
                        equipmentMethod.equipmentDispatchStateChange(objCommon, equipmentID, new ObjectIdentifier(portID),
                                BizConstant.SP_PORTRSC_DISPATCHSTATE_DISPATCHED, dummyOI, dummyOI, dummyOI,
                                dispatchUnLoadCassetteID);
                    }
                }
            }
        }

        /**************************************************************************************************************/
        /*【step7】port status change process（from eqp）                                                       */
        /*   - 【step7-1】cassette_transferState_Get                                                                  */
        /*   - 【step7-2】cassette_equipmentID_Get                                                                    */
        /*   - 【step7-3】equipment_TakeOutInMode_Check                                                               */
        /*   - 【step7-4】equipment_portInfo_Get                                                                      */
        /*   - 【step7-5】equipment_dispatchState_Change                                                              */
        /**************************************************************************************************************/
        log.info("【step7】port status change process（from eqp）");
        for (int i = 0; i < startCassetteSize; i++) {
            Infos.StartCassette startCassetteObj = startCassetteList.get(i);
            ObjectIdentifier cassetteID = startCassetteObj.getCassetteID();
            log.debug("the cassetteID:%s", cassetteID);
            //【step7-1】get cassette transfer state
            log.debug("【step7-1】get cassette transfer state");
            String transferStateGetOutRetCode = cassetteMethod.cassetteTransferStateGet(objCommon, cassetteID);
            String transferState = transferStateGetOutRetCode;
            if (CimStringUtils.equals(BizConstant.SP_TRANSSTATE_EQUIPMENTIN, transferState)) {
                log.debug("the transferState = EI");
                //【step7-2】get cassette info in eqp
                log.debug("【step7-2】get cassette info in eqp");
                Outputs.ObjCassetteEquipmentIDGetOut stringRetCode = cassetteMethod.cassetteEquipmentIDGet(objCommon, cassetteID);
                ObjectIdentifier eqpID = stringRetCode.getEquipmentID();
                log.debug("the equipmentID:%s", stringRetCode);

                //【step7-3】check eqp availability of TakeOutIn transfer
                log.debug("【step7-3】check eqp availability of TakeOutIn transfer");
                Long fromEqpTakeOutInSupport = equipmentMethod.equipmentTakeOutInModeCheck(objCommon, eqpID);
                log.debug("the fromEqpTakeOutInSupport = %d", fromEqpTakeOutInSupport);
                //【step7-4】get eqp port info
                log.debug("【step7-4】get eqp port info");
                Infos.EqpPortInfo eqpPortInfo = equipmentMethod.equipmentPortInfoGet(objCommon, eqpID);
                //【step7-5】change eqp dispatch state
                log.debug("【step7-5】change eqp dispatch state");
                int lenEqpPort = CimArrayUtils.getSize(eqpPortInfo.getEqpPortStatuses());
                for (int j = 0; j < lenEqpPort; j++) {
                    Infos.EqpPortStatus eqpPortStatusObj = eqpPortInfo.getEqpPortStatuses().get(j);
                    ObjectIdentifier loadedCassetteID = eqpPortStatusObj.getLoadedCassetteID();
                    log.debug("the loadedCassetteID:%s", loadedCassetteID);
                    if (ObjectIdentifier.equalsWithValue(cassetteID, loadedCassetteID)) {
                        log.debug("the cassetteID = loadedCassetteID");
                        String portState = eqpPortStatusObj.getPortState();
                        String dispatchState = eqpPortStatusObj.getDispatchState();
                        if (CimStringUtils.equals(BizConstant.SP_PORTRSC_PORTSTATE_UNLOADREQ, portState)
                                && CimStringUtils.equals(BizConstant.SP_PORTRSC_DISPATCHSTATE_DISPATCHED, dispatchState)) {
                            log.debug("portState = UnloadReq && dispatchState = Dispatched");
                            ObjectIdentifier dummy = null;
                            ObjectIdentifier portID = eqpPortStatusObj.getPortID();
                            equipmentMethod.equipmentDispatchStateChange(objCommon, eqpID, portID, BizConstant.SP_PORTRSC_DISPATCHSTATE_REQUIRED,
                                    dummy, dummy, dummy, dummy);
                            ObjectIdentifier dispatchLoadCassetteID = eqpPortStatusObj.getDispatchLoadCassetteID();
                            if (1 == fromEqpTakeOutInSupport && !ObjectIdentifier.isEmptyWithValue(dispatchLoadCassetteID)) {
                                /**************************************************************************************/
                                /* This port has TakeOutIn reservation. dispatch status should be re-changed          */
                                /* for take out carrier                                                               */
                                /*    1. dispatch State ==> Dispatched.                                               */
                                /*    2. dispatch Unload Carrier ==> dispUnLoadCastID.                                */
                                /**************************************************************************************/
                                log.debug("this port has TakeOutIn reservation. dispatch status should be re-changed for take out carrier");
                                equipmentMethod.equipmentDispatchStateChange(objCommon, equipmentID, portID, BizConstant.SP_PORTRSC_DISPATCHSTATE_DISPATCHED, dummy, dispatchLoadCassetteID, dummy, dummy);
                            }
                        }
                    }
                }
            }

        }

        //【step8】call cassette_APCInformation_GetDR() (txMoveInReserveCancelReq.cpp:996 - 1013)
        log.info("【step8】todo - get cassette APC information");
        List<Infos.ApcBaseCassette> apcBaseCassettes = null;
        try {
            apcBaseCassettes = cassetteMethod.cassetteAPCInformationGetDR(objCommon, equipmentID, startCassetteList);
        } catch (ServiceException e) {
            if (Validations.isEquals(retCodeConfig.getSystemError(), e.getCode())){
                throw e;
            }
        }

        /**************************************************************************************************************/
        /*【step9】check process                                                                                      */
        /*   - 【step9-1】cassette_CheckConditionForStartReserveCancel                                                */
        /*   - 【step9-2】lot_CheckConditionForStartReserveCancel                                                     */
        /*   - 【step9-3】equipment_CheckConditionForStartReserveCancel                                               */
        /*   - 【step9-4】portResource_currentOperationMode_Get                                                       */
        /**************************************************************************************************************/

        //-------------------------------------------------
        //  Check pilotrun in eqp
        //-------------------------------------------------
        Infos.CheckPilotRunForEquipmentInfo checkPilotRunForEquipmentInfo = pilotRunMethod.checkPilotRunForEquipment(objCommon, params.getEquipmentID(), startCassetteList);

        //update pirun job status to Created
        if (ObjectIdentifier.isNotEmpty(checkPilotRunForEquipmentInfo.getPilotRunJobID())) {
            Params.PilotRunStatusChangeParams pilotRunStatusChangeParams = new Params.PilotRunStatusChangeParams();
            pilotRunStatusChangeParams.setEntity(checkPilotRunForEquipmentInfo.getPilotRunJobID());
            pilotRunStatusChangeParams.setEntityType(PilotRunInfo.EntityType.PiLotRunJob.getValue());
            pilotRunStatusChangeParams.setStatus(PilotRunInfo.Status.Created.getValue());
            pilotRunService.sxPilotRunStatusChange(objCommon, pilotRunStatusChangeParams);
        }

        /***********************************************************************************/
        /* check process for cassette                                                      */
        /* the following conditions are checked by this object                             */
        /*   -  dispatchState                                                              */
        /*   -  controlJobID                                                               */
        /***********************************************************************************/
        //【step9-1】check process for cassette
        log.info("【step9-1】check process for cassette");
        cassetteMethod.cassetteCheckConditionForStartReserveCancel(objCommon, controlJobID, startCassetteList);

        /***********************************************************************************/
        /* check process for lot                                                           */
        /* the following conditions are checked by this object                             */
        /*   -  lotProcessState                                                            */
        /*   -  controlJobID                                                               */
        /***********************************************************************************/
        //【step9-2】check process for lot
        log.info("【step9-2】check process for lot");
        lotMethod.lotCheckConditionForStartReserveCancel(objCommon, controlJobID, startCassetteList);

        /***********************************************************************************/
        /* check process for eqp                                                     */
        /* the following conditions are checked by this object                             */
        /*   -  reservedControlJobID                                                       */
        /***********************************************************************************/
        //【step9-3】check process for eqp
        log.info("【step9-3】check process for eqp");
        equipmentMethod.equipmentCheckConditionForStartReserveCancel(objCommon, equipmentID, controlJobID);

        //【step9-4】get eqp's online mode
        log.info("【step9-4】get eqp's online mode");
        ObjectIdentifier unLoadPortID = startCassetteList.get(0).getUnloadPortID();
        Outputs.ObjPortResourceCurrentOperationModeGetOut operationModeRetCode = portMethod.portResourceCurrentOperationModeGet(objCommon, equipmentID, unLoadPortID);

        /**************************************************************************************************************/
        /*【step10】Main Process (Reverse Order of StartReserve Procedure)                                            */
        /*   Clear ActualStart Info in Each Lots' PO                                                                  */
        /*   -  Clear controlJobID of each cassette                                                                   */
        /*   -  Clear controlJobID of each lot.                                                                       */
        /*   -  Clear control job info of each lot's cunrrent PO                                                      */
        /**************************************************************************************************************/
        //【step10-1】clear process start reserve information.
        log.info("【step10-1】clear process start reserve information.");
        processMethod.processStartReserveInformationClear(objCommon, controlJobID, startCassetteList);

        if (StandardProperties.OM_RTMS_CHECK_ACTIVE.isTrue()){
            //report RTMS
            for (Infos.StartCassette startCassette : startCassetteList){
                List<Infos.LotInCassette> lotInCassetteList = startCassette.getLotInCassetteList();
                for (Infos.LotInCassette lotInCassette : lotInCassetteList){
                    if (lotInCassette.getMoveInFlag()){
                        List<String> reticleList = new ArrayList<>();
                        Infos.StartRecipe startRecipe = lotInCassette.getStartRecipe();
                        List<Infos.StartReticleInfo> startReticleList = startRecipe.getStartReticleList();
                        if (CimArrayUtils.isNotEmpty(startReticleList)) {
                            for (Infos.StartReticleInfo startReticleInfo : startReticleList){
                                reticleList.add(startReticleInfo.getReticleID().getValue());
                            }
                        }
                        if (CimArrayUtils.isNotEmpty(reticleList)) {
                            reticleMethod.reticleUsageCheckByRTMS(objCommon,BizConstant.RTMS_RETICLE_CHECK_ACTION_MOVE_IN_RESERVE_CANCEL,
                                    reticleList,lotInCassette.getLotID(),equipmentID);
                        }
                    }
                }
            }
        }


        //【step10-2】clear eqp reserved controlJobID
        log.info("【step10-2】clear eqp reserved controlJobID");
        equipmentMethod.equipmentReservedControlJobIDClear(objCommon, equipmentID, controlJobID);

        //【step10-3】clear reserved SLM information
        log.debug("【step10-3】clear reserved SLM information");
        equipmentMethod.equipmentReservedControlJobClearForSLM(objCommon, equipmentID, controlJobID);

        //【step10-4】 wafer Stacking Operation
        log.info("【step10-4】wafer Stacking Operation");
        lotMethod.lotBondingGroupUpdateByOperation(objCommon, equipmentID, controlJobID, startCassetteList, BizConstant.SP_OPERATION_STARTRESERVATIONCANCEL);

        for (int i = 0; i < startCassetteSize; i++) {
            Infos.StartCassette startCassetteObj = startCassetteList.get(i);
            int lotInCassetteListSize = CimArrayUtils.getSize(startCassetteObj.getLotInCassetteList());
            for (int j = 0; j < lotInCassetteListSize; j++) {
                Infos.LotInCassette lotInCassetteObj = startCassetteObj.getLotInCassetteList().get(j);
                ObjectIdentifier lotID = lotInCassetteObj.getLotID();
                log.debug("the lotID:%s", lotID);
                if (CimBooleanUtils.isFalse(lotInCassetteObj.getMoveInFlag())) {
                    continue;
                }

                /***************************************************************************************/
                /* check if schedule change by lot information change is reserved for this operation   */
                /* If schedule change is reserved, delete the reservation and call e-mail to lot owner */
                /***************************************************************************************/
                Outputs.ObjLotCurrentOperationInfoGetOut lotCurrentOperationInfoGetOut = lotMethod.lotCurrentOperationInfoGet(objCommon, lotID);
                ObjectIdentifier targetRouteID = lotCurrentOperationInfoGetOut.getRouteID();
                String targetOperationNumber = lotCurrentOperationInfoGetOut.getOperationNumber();
                log.debug("the targetRouteID:%s", targetRouteID);
                log.debug("the targetOperationNumber:%s", targetOperationNumber);
                log.debug("【step10-6】get schedule change reservation list");
                Inputs.ObjScheduleChangeReservationGetListIn inParams = new Inputs.ObjScheduleChangeReservationGetListIn();
                inParams.setObjectID(lotID.getValue());
                inParams.setObjectType(BizConstant.SP_SCHDL_CHG_OBJTYPE_LOT);
                inParams.setTargetRouteID(targetRouteID.getValue());
                inParams.setTargetOperationNumber(targetOperationNumber);
                inParams.setLotInfoChangeFlag(2L);   //2: LOTINFO_CHANGE_FLAG is 1
                List<Infos.SchdlChangeReservation> schdlChangeReservationList = scheduleChangeReservationMethod.schdlChangeReservationGetListDR(objCommon, inParams);
                //【step10-7】delete schedule chaange reservation
                log.debug("【step10-7】 delete schedule chaange reservation");
                int rsvLength = CimArrayUtils.getSize(schdlChangeReservationList);
                if (rsvLength > 0){
                    for (Infos.SchdlChangeReservation schdlChangeReservation : schdlChangeReservationList){
                        scheduleChangeReservationMethod.schdlChangeReservationDeleteDR(objCommon, schdlChangeReservation);
                        //【step10-8】call e-mail
                        log.debug("【step10-8】todo - call e-mail");
                        StringBuffer messageSb = new StringBuffer();
                        messageSb.append("This message was sent because reservation for \"Lot Information Change\" is canceled.\n")
                                .append("Schedule Change Reservation Info.\n")
                                .append( "LotID      : " ).append(lotInCassetteObj.getLotID().getValue()).append("\n")
                                .append("Product ID : ").append(schdlChangeReservation.getProductID().getValue()).append("\n")
                                .append("Route ID   : ").append(schdlChangeReservation.getRouteID().getValue()).append("\n")
                                .append("Ope.No     : ").append(schdlChangeReservation.getOperationNumber()).append("\n")
                                .append("SubLotType : ").append(schdlChangeReservation.getSubLotType()).append("\n");
                        ObjectIdentifier messageID = new ObjectIdentifier(BizConstant.SP_SYSTEMMSGCODE_SCRNOTICE);
                        messageMethod.messageDistributionMgrPutMessage(objCommon, messageID, lotInCassetteObj.getLotID(),
                                "", equipmentID, lotCurrentOperationInfoGetOut.getRouteID(), lotCurrentOperationInfoGetOut.getOperationNumber(),
                                "", messageSb.toString());
                    }
                }
            }
        }
        //【step11】delete controlJob
        log.info("【step11】delete controlJob");
        Params.CJStatusChangeReqParams cjStatusChangeReqParams = new Params.CJStatusChangeReqParams();
        Infos.ControlJobCreateRequest dummyControlJobCreateRequest = null;
        cjStatusChangeReqParams.setControlJobID(controlJobID);
        cjStatusChangeReqParams.setControlJobAction(BizConstant.SP_CONTROLJOBACTION_TYPE_DELETE);
        cjStatusChangeReqParams.setControlJobCreateRequest(dummyControlJobCreateRequest);
        cjStatusChangeReqParams.setClaimMemo(params.getOpeMemo());
        controlJobProcessJobService.sxCJStatusChangeReqService(objCommon, cjStatusChangeReqParams);

        /***************************************************************************************/
        /*【step12】update related information procedure                                       */
        /*   - change cassette's dispatch state to FALSE                                       */
        /***************************************************************************************/
        log.info("【step12】update related information procedure");
        for (int i = 0; i < startCassetteSize; i++) {
            Infos.StartCassette startCassetteObj = startCassetteList.get(i);
            // change cassette's dispatch state to FALSE
            log.debug("change cassette's dispatch state to FALSE");
            cassetteMethod.cassetteDispatchStateChange(objCommon, startCassetteObj.getCassetteID(), false);
        }

        //【step13】 call MoveInReserveCancelReq() to EAP Procedure
        log.debug("【step13】call MoveInReserveCancelReq() to EAP Procedure");
        /*------------------------------------------------------------------------*/
        /*                                                                        */
        /*   Send MoveInReserveCancelReq() to EAP Procedure                */
        /*                                                                        */
        /*------------------------------------------------------------------------*/
//        Inputs.SendMoveInReserveCancelReqIn sendMoveInReserveCancelReqIn = new Inputs.SendMoveInReserveCancelReqIn();
//        sendMoveInReserveCancelReqIn.setObjCommonIn(objCommon);
//        sendMoveInReserveCancelReqIn.setEquipmentID(equipmentID);
//        sendMoveInReserveCancelReqIn.setControlJobID(controlJobID);
//        Outputs.SendMoveInReserveCancelReqOut sendMoveInReserveCancelReqOut = null;
//        sendMoveInReserveCancelReqOut = (Outputs.SendMoveInReserveCancelReqOut) tcsMethod.sendTCSReq(TCSReqEnum.sendMoveInReserveCancelReq,sendMoveInReserveCancelReqIn);

        String tmpSleepTimeValue = StandardProperties.OM_EAP_CONNECT_SLEEP_TIME.getValue();
        String tmpRetryCountValue = StandardProperties.OM_EAP_CONNECT_RETRY_COUNT.getValue();
        Long sleepTimeValue = 0L;
        Long retryCountValue = 0L;

        if (0 == CimStringUtils.length(tmpSleepTimeValue)) {
            sleepTimeValue = BizConstant.SP_DEFAULT_SLEEP_TIME_TCS;
        } else {
            sleepTimeValue = CimNumberUtils.longValue(tmpSleepTimeValue);
        }

        if (0 == CimStringUtils.length(tmpRetryCountValue)) {
            retryCountValue = BizConstant.SP_DEFAULT_RETRY_COUNT_TCS;
        } else {
            retryCountValue = CimNumberUtils.longValue(tmpRetryCountValue);
        }

        if (!params.isAlreadySendMsgFlag()) {
            for (int retryNum = 0; retryNum < (retryCountValue + 1); retryNum++) {
                log.info("{} {}", "loop to retryCountValue + 1", retryNum);
                /*--------------------------*/
                /*    Send Request to EAP   */
                /*--------------------------*/
                IEAPRemoteManager eapRemoteManager = eapMethod.eapRemoteManager(objCommon,params.getUser(),params.getEquipmentID(),null,true);
                if (null == eapRemoteManager) {
                    log.info("MES not configure EAP host");
                    break;
                }
                try {
                    Object moveInReserveCancelReqEapOut = eapRemoteManager.sendMoveInReserveCancelReq(params);
                    log.info("Now EAP subSystem is alive!! Go ahead");
                    break;
                } catch (ServiceException ex) {
                    if (Validations.isEquals(ex.getCode(), retCodeConfig.getTcsNoResponse())) {
                        log.info("{} {}", "EAP subsystem has return NO_RESPONSE!! just retry now!!  now count...", retryNum);
                        log.info("{} {}", "now sleeping... ", sleepTimeValue);
                        if (retryNum != retryCountValue){
                            try {
                                Thread.sleep(sleepTimeValue);
                                continue;
                            } catch (InterruptedException e) {
                                ex.addSuppressed(e);
                                Thread.currentThread().interrupt();
                                throw ex;
                            }
                        }else {
                            Validations.check(true,retCodeConfig.getTcsNoResponse());
                        }
                    } else {
                        Validations.check(true,new OmCode(ex.getCode(),ex.getMessage()));
                    }
                }
            }
        }


        //【step14】delete APC runtime capability (line:1461 - 1471)
        log.debug("【step14】 delete APC runtime capability");
        apcMethod.apcRuntimeCapabilityDeleteDR(objCommon, controlJobID);

        //【step15】call controlJob information to APC manager (line:1499 - 1524)
        log.debug("【step15】call controlJob information to APC manager");
        //   Wafer information in APCBaseCassette is not necessary for reserve cancel,
        //   but it is required by wrapper for process xml which satisfy dtd.
        //   So, now we add dummy wafer data into strCassette_APCInformation_GetDR_out.strAPCBaseCassetteList.
        int bcLen = CimArrayUtils.getSize(apcBaseCassettes);
        for (int bcIdx = 0; bcIdx < bcLen; bcIdx++){
            Infos.ApcBaseCassette apcBaseCassette = apcBaseCassettes.get(bcIdx);
            List<Infos.ApcBaseLot> apcBaseLotList = apcBaseCassette.getApcBaseLotList();
            int blLen = CimArrayUtils.getSize(apcBaseLotList);
            for (int blIdx = 0; blIdx < blLen; blIdx++){
                Infos.ApcBaseLot apcBaseLot = apcBaseLotList.get(blIdx);
                Infos.ApcBaseWafer apcBaseWafer = new Infos.ApcBaseWafer();
                apcBaseWafer.setWaferID("");
                apcBaseWafer.setSlotNumber(0L);
                apcBaseWafer.setControlWaferFlag(false);
                apcBaseWafer.setSendAheadWaferFlag(false);
                apcBaseWafer.setProcessFlag(false);
                apcBaseWafer.setExperimentSplitWafer(false);
                apcBaseLot.setApcBaseWaferList(Collections.singletonList(apcBaseWafer));
            }
        }
        int retCode = 0;
        try {
            apcMethod.APCMgrSendControlJobInformationDR(objCommon, params.getEquipmentID(), params.getControlJobID(), BizConstant.SP_APC_CONTROLJOBSTATUS_CANCELED, apcBaseCassettes);
        } catch (ServiceException e) {
            retCode = e.getCode();
            if (!Validations.isEquals(retCodeConfigEx.getOkNoIF(), e.getCode())){
                throw e;
            }
        }
        if (retCode == 0){
            String tmpString = params.getAPCIFControlStatus();
            params.setAPCIFControlStatus(BizConstant.SP_APC_CONTROLJOBSTATUS_CREATED);
        }
        Results.MoveInReserveCancelReqResult moveInReserveCancelReqResult = new Results.MoveInReserveCancelReqResult();
        moveInReserveCancelReqResult.setStartCassetteList(startCassetteList);
        return moveInReserveCancelReqResult;
    }

    @Override
    public ObjectIdentifier sxMoveInReserveForIBReq(Infos.ObjCommon objCommon, Params.MoveInReserveForIBReqParams params, String apcifControlStatus) {
        List<Infos.StartCassette> strStartCassette = params.getStartCassetteList();
        Validations.check(CimArrayUtils.isEmpty(strStartCassette), retCodeConfig.getInvalidInputParam());
        for (Infos.StartCassette startCassette : strStartCassette) {
            List<Infos.LotInCassette> lotInCassetteList = startCassette.getLotInCassetteList();
            if (CimObjectUtils.isEmpty(lotInCassetteList)) {
                startCassette.setLotInCassetteList(new ArrayList<>());
            }
        }

        //step1 - equipment_categoryVsTxID_CheckCombination
        equipmentMethod.equipmentCategoryVsTxIDCheckCombination(objCommon, params.getEquipmentID());

        //step2 - lot_processJobExecFlag_ValidCheck
        Outputs.ObjLotProcessJobExecFlagValidCheckOut lotProcessJobExecFlagValidCheck = lotMethod.lotProcessJobExecFlagValidCheck(objCommon, strStartCassette);

        //Check Process
        List<ObjectIdentifier> cassetteIDs = new ArrayList<>();
        for (Infos.StartCassette startCassette : strStartCassette) {
            cassetteIDs.add(startCassette.getCassetteID());
        }


        //step3 - cassette_scrapWafer_SelectDR
        List<Infos.LotWaferMap> lotWaferMaps = cassetteMethod.cassetteScrapWaferSelectDR(objCommon, cassetteIDs);
        Validations.check(CimArrayUtils.isNotEmpty(lotWaferMaps), retCodeConfig.getFoundScrap());
        /*-----------------------------------------*/
        /*   call txAPCRunTimeCapabilityInq        */
        /*-----------------------------------------*/
        // step4 - txAPCRTSystemInfoInq
        List<Infos.APCRunTimeCapabilityResponse> apcRunTimeCapabilityResponseList = apcInqService.sxAPCRunTimeCapabilityInq(objCommon, params.getEquipmentID(), params.getControlJobID(), strStartCassette, true);
        int runCapaRespCount = CimArrayUtils.getSize(apcRunTimeCapabilityResponseList);
        if (runCapaRespCount > 0){
            String tmpString = apcifControlStatus;
            apcifControlStatus = BizConstant.SP_APC_CONTROLJOBSTATUS_CANCELED;
        }
        /*------------------------------------------------*/
        /*   call txAPCRecipeParameterAdjustInq           */
        /*------------------------------------------------*/
        // step5 - txAPCRcpParmChgInq
        int retCode = 0;
        try {
            strStartCassette = apcInqService.sxAPCRecipeParameterAdjustInq(objCommon, params.getEquipmentID(), params.getStartCassetteList(), apcRunTimeCapabilityResponseList, true);
        } catch (ServiceException e) {
            retCode = e.getCode();
            if (!Validations.isEquals(retCodeConfigEx.getOkNoIF(), e.getCode())){
                throw e;
            }
        }
        if (retCode == 0){
            String tmpString = apcifControlStatus;
            apcifControlStatus = BizConstant.SP_APC_CONTROLJOBSTATUS_CANCELED;
        }

        //Check every lot has at least one wafer with processJobExecFlag == TRUE

        //step6 - lot_processJobExecFlag_ValidCheck
        lotProcessJobExecFlagValidCheck = lotMethod.lotProcessJobExecFlagValidCheck(objCommon, strStartCassette);


        /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/
        /*                                                                       */
        /*   Object Lock Process                                                 */
        /*                                                                       */
        /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/
        // step7 - object_lockMode_Get
        Inputs.ObjLockModeIn objLockModeIn = new Inputs.ObjLockModeIn();
        objLockModeIn.setObjectID(params.getEquipmentID());
        objLockModeIn.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
        objLockModeIn.setFunctionCategory(TransactionIDEnum.START_LOTS_RESERVATION_FOR_INTERNAL_BUFFER_REQ.getValue());
        objLockModeIn.setUserDataUpdateFlag(false);
        Outputs.ObjLockModeOut objLockModeOut = objectMethod.objectLockModeGet(objCommon, objLockModeIn);
        Long lockMode = objLockModeOut.getLockMode();
        Inputs.ObjAdvanceLockIn strAdvancedobjecLockin = new Inputs.ObjAdvanceLockIn();
        if (!lockMode.equals(BizConstant.SP_EQP_LOCK_MODE_WRITE)){
            // step8 - advanced_object_Lock
            List<String> dummySeq;
            dummySeq = new ArrayList<>(0);
            strAdvancedobjecLockin.setObjectID(params.getEquipmentID());
            strAdvancedobjecLockin.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
            strAdvancedobjecLockin.setObjectType(BizConstant.SP_OBJECTLOCK_OBJECTTYPE_MAINOBJECT);
            strAdvancedobjecLockin.setLockType(objLockModeOut.getRequiredLockForMainObject());
            strAdvancedobjecLockin.setKeyList(dummySeq);
            objectLockMethod.advancedObjectLock(objCommon, strAdvancedobjecLockin);
            // tep9 - object_Lock
            /*------------------------------*/
            /*   Lock Dispatcher Object     */
            /*------------------------------*/
            objectLockMethod.objectLock(objCommon, CimMachine.class, params.getEquipmentID());
        } else {
            /*--------------------------------------------*/
            /*                                            */
            /*      Machine Object Lock Process           */
            /*                                            */
            /*--------------------------------------------*/
            // step10 - object_Lock
            objectLockMethod.objectLock(objCommon, CimMachine.class, params.getEquipmentID());
        }

        /*--------------------------------------------*/
        /*                                            */
        /*        Port Object Lock Process            */
        /*                                            */
        /*--------------------------------------------*/
        // step11 - object_LockForEquipmentResource
        if (!lockMode.equals(BizConstant.SP_EQP_LOCK_MODE_WRITE)){
            int lenCast = CimArrayUtils.getSize(strStartCassette);
            for (int i = 0; i < lenCast; i++){
                objectLockMethod.objectLockForEquipmentResource(objCommon, params.getEquipmentID(), strStartCassette.get(i).getLoadPortID(), BizConstant.SP_CLASSNAME_POSPORTRESOURCE);
            }
        } else {
            //step12 - equipment_portInfoForInternalBuffer_GetDR
            Infos.EqpPortInfo eqpPortInfo = equipmentMethod.equipmentPortInfoForInternalBufferGetDR(objCommon, params.getEquipmentID());
            List<Infos.EqpPortStatus> eqpPortStatuses = eqpPortInfo.getEqpPortStatuses();
            int lenPortInfo = CimArrayUtils.getSize(eqpPortStatuses);
            for (int i = 0; i < lenPortInfo; i++){
                // step13 - object_LockForEquipmentResource
                objectLockMethod.objectLockForEquipmentResource(objCommon, params.getEquipmentID(), eqpPortStatuses.get(i).getPortID(), BizConstant.SP_CLASSNAME_POSPORTRESOURCE);
            }
        }
        /*-------------------------------------------------------------------------*/
        /*                                                                         */
        /*   Material Location Object (and Buffer resource object )Lock Process    */
        /*    ==> This lock process is for Material Location resynchronization.    */
        /*                                                                         */
        /*-------------------------------------------------------------------------*/
        if (!lockMode.equals(BizConstant.SP_EQP_LOCK_MODE_WRITE)){
            // step14 - advanced_object_LockForEquipmentResource
            int nCassetteLen = CimArrayUtils.getSize(strStartCassette);
            for (int i = 0; i < nCassetteLen; i++){
                Inputs.ObjAdvancedObjectLockForEquipmentResourceIn objAdvancedObjectLockForEquipmentResourceIn = new Inputs.ObjAdvancedObjectLockForEquipmentResourceIn();
                objAdvancedObjectLockForEquipmentResourceIn.setEquipmentID(params.getEquipmentID());
                objAdvancedObjectLockForEquipmentResourceIn.setClassName(BizConstant.SP_CLASSNAME_POSMATERIALLOCATION_EMPTYML);
                objAdvancedObjectLockForEquipmentResourceIn.setObjectID(strStartCassette.get(i).getCassetteID());
                objAdvancedObjectLockForEquipmentResourceIn.setObjectLockType(BizConstant.SP_OBJECTLOCK_LOCKTYPE_WRITE);
                objAdvancedObjectLockForEquipmentResourceIn.setBufferResourceName(strStartCassette.get(i).getLoadPurposeType());
                objAdvancedObjectLockForEquipmentResourceIn.setBufferResourceLockType(BizConstant.SP_OBJECTLOCK_LOCKTYPE_READ);
                objectLockMethod.advancedObjectLockForEquipmentResource(objCommon, objAdvancedObjectLockForEquipmentResourceIn);
            }
        } else {
            // step15 - object_LockForEquipmentResource
            int nCassetteLen = CimArrayUtils.getSize(strStartCassette);
            for (int i = 0; i < nCassetteLen; i++){
                ObjectIdentifier loadPurposeType = new ObjectIdentifier(strStartCassette.get(i).getLoadPurposeType());
                objectLockMethod.objectLockForEquipmentResource(objCommon, params.getEquipmentID(), loadPurposeType, BizConstant.SP_CLASSNAME_POSMATERIALLOCATION);
            }
        }

        //check contamination through recipe
        for (Infos.StartCassette startCassette : params.getStartCassetteList()) {
            List<Infos.LotInCassette> lotInCassetteList = startCassette.getLotInCassetteList();
            for (Infos.LotInCassette lotInCassette : lotInCassetteList){
                ObjectIdentifier lotID = lotInCassette.getLotID();
                Infos.StartRecipe startRecipe = lotInCassette.getStartRecipe();
                ObjectIdentifier logicalRecipeID = startRecipe.getLogicalRecipeID();
                ObjectIdentifier machineRecipeID = startRecipe.getMachineRecipeID();
                contaminationMethod.recipeContaminationCheck(logicalRecipeID,machineRecipeID,lotID,params.getEquipmentID());
            }
        }

        /*--------------------------------------------*/
        /*                                            */
        /*        Cassette Object Lock Process        */
        /*                                            */
        /*--------------------------------------------*/
        List<ObjectIdentifier> lotIDs = new ArrayList<>();
        cassetteIDs = new ArrayList<>();
        for (Infos.StartCassette startCassette : strStartCassette) {
            cassetteIDs.add(startCassette.getCassetteID());
            List<Infos.LotInCassette> lotInCassetteList = startCassette.getLotInCassetteList();
            if (CimArrayUtils.isNotEmpty(lotInCassetteList)) {
                for (Infos.LotInCassette lotInCassette : lotInCassetteList) {
                    lotIDs.add(lotInCassette.getLotID());
                }
            }
        }
        /*------------------------------*/
        /*   Lock Cassette/Lot Object   */
        /*-------------------------------*/
        // step16 - objectSequence_Lock
        objectLockMethod.objectSequenceLock(objCommon, CimCassette.class, cassetteIDs);

        // step17 - objectSequence_Lock
        objectLockMethod.objectSequenceLock(objCommon, CimLot.class, lotIDs);

        // seasong 相关代码, by ho
        seasonMethod.checkSeasonForMoveInReserve(objCommon, params.getStartCassetteList(), params.getEquipmentID());

        //check Ocap Info
        strStartCassette = ocapMethod.ocapCheckEquipmentAndSamplingAndRecipeExchange(params.getEquipmentID(),
                strStartCassette);

        log.debug("check PM PilotRun");
        pilotRunMethod.checkPMRun(params.getEquipmentID(), strStartCassette);

        /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/
        /*                                                                       */
        /*   Check Process                                                       */
        /*                                                                       */
        /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/
        int size = strStartCassette.size();
        for (int i = 0; i < size; i++) {
            cassetteIDs.set(i, strStartCassette.get(i).getCassetteID());
        }

        //step18 - cassette_scrapWafer_SelectDR
        lotWaferMaps = cassetteMethod.cassetteScrapWaferSelectDR(objCommon, cassetteIDs);
        Validations.check(CimArrayUtils.isNotEmpty(lotWaferMaps),retCodeConfig.getFoundScrap());

        //-------------------------------------------------
        //  Check Contamination (pr flag and contamination level)
        //-------------------------------------------------
        List<ObjectIdentifier> moveInLotIds = strStartCassette.parallelStream()
                .flatMap(startCassette -> startCassette.getLotInCassetteList().parallelStream()
                        .filter(Infos.LotInCassette::getMoveInFlag).map(Infos.LotInCassette::getLotID))
                .collect(Collectors.toList());
        contaminationMethod.lotCheckContaminationLevelAndPrFlagStepIn(moveInLotIds, params.getEquipmentID(),"");
        List<Params.ContaminationAllLotCheckParams> allLots = new ArrayList<>();
        for (Infos.StartCassette tempStartCassettes : strStartCassette){
            List<Infos.LotInCassette> lotInCassetteList = tempStartCassettes.getLotInCassetteList();
            for (Infos.LotInCassette lotInCassette : lotInCassetteList){
                Params.ContaminationAllLotCheckParams checkParams = new Params.ContaminationAllLotCheckParams();
                allLots.add(checkParams);
                checkParams.setLotID(lotInCassette.getLotID());
                checkParams.setMoveInFlag(lotInCassette.getMoveInFlag());
            }
        }
        contaminationMethod.contaminationLvlCheckAmongLots(allLots);

        //check capability
        equipmentMethod.capabilityCheck(objCommon,moveInLotIds,params.getEquipmentID());

        //check pilot run
        Infos.CheckPilotRunForEquipmentInfo checkPilotRunForEquipmentInfo = pilotRunMethod.checkPilotRunForEquipment(objCommon, params.getEquipmentID(), strStartCassette);

        //update pirun job status to ongoing
        if (ObjectIdentifier.isNotEmpty(checkPilotRunForEquipmentInfo.getPilotRunJobID())) {
            Params.PilotRunStatusChangeParams pilotRunStatusChangeParams = new Params.PilotRunStatusChangeParams();
            pilotRunStatusChangeParams.setEntity(checkPilotRunForEquipmentInfo.getPilotRunJobID());
            pilotRunStatusChangeParams.setEntityType(PilotRunInfo.EntityType.PiLotRunJob.getValue());
            pilotRunStatusChangeParams.setStatus(PilotRunInfo.Status.Ongoing.getValue());
            pilotRunService.sxPilotRunStatusChange(objCommon, pilotRunStatusChangeParams);
        }

        // Check Min Q-Time restrictions
        minQTimeMethod.checkIsRejectByRestriction(objCommon, strStartCassette);

        //step19 - cassette_CheckConditionForOperationForInternalBuffer
        cassetteMethod.cassetteCheckConditionForOperationForInternalBuffer(objCommon, params.getEquipmentID(), strStartCassette, BizConstant.SP_OPERATION_STARTRESERVATION);

        //step20 - lot_CheckConditionForOperationForInternalBuffer
        lotMethod.lotCheckConditionForOperationForInternalBuffer(objCommon, params.getEquipmentID(), strStartCassette, BizConstant.SP_OPERATION_STARTRESERVATION);

        //step21 - equipment_portState_CheckForStartReservationForInternalBuffer
        equipmentMethod.equipmentPortStateCheckForStartReservationForInternalBuffer(objCommon, params.getEquipmentID(), strStartCassette);

        Inputs.ObjEquipmentLotCheckFlowBatchConditionForOperationStartIn operationStartIn = new Inputs.ObjEquipmentLotCheckFlowBatchConditionForOperationStartIn();
        operationStartIn.setEquipmentID(params.getEquipmentID());
        operationStartIn.setPortGroupID("");
        operationStartIn.setStartCassetteList(strStartCassette);

        //step22 - equipment_lot_CheckFlowBatchConditionForOpeStart__090
        ObjectIdentifier conditionForOperationStart = equipmentMethod.equipmentLotCheckFlowBatchConditionForOpeStart(objCommon, operationStartIn);
        //step23 - equipment_processDurableRequiredFlag_Get
        try {
            equipmentMethod.equipmentProcessDurableRequiredFlagGet(objCommon, params.getEquipmentID());
        } catch (ServiceException e){
            if (Validations.isEquals(retCodeConfig.getEquipmentProcessDurableReticleRequired(), e.getCode())
                    || Validations.isEquals(retCodeConfig.getEquipmentProcessDurableFixtRequired(), e.getCode())) {
                for (Infos.StartCassette startCassette : strStartCassette) {
                    if (CimStringUtils.equals(BizConstant.SP_LOADPURPOSETYPE_EMPTYCASSETTE, startCassette.getLoadPurposeType())) {
                        continue;
                    }
                    int nJMax = startCassette.getLotInCassetteList().size();
                    for (int j = 0; j < nJMax; j++) {
                        if (CimBooleanUtils.isFalse(startCassette.getLotInCassetteList().get(j).getMoveInFlag())) {
                            continue;
                        }

                        //step24 - processDurable_CheckConditionForOpeStart
                        Outputs.ObjProcessDurableCheckConditionForOperationStartOut checkConditionForOpeStartOutRetCode = processMethod.processDurableCheckConditionForOpeStart(objCommon, params.getEquipmentID(),
                                startCassette.getLotInCassetteList().get(j).getStartRecipe().getLogicalRecipeID(),
                                startCassette.getLotInCassetteList().get(j).getStartRecipe().getMachineRecipeID(),
                                startCassette.getLotInCassetteList().get(j).getLotID());
                        startCassette.getLotInCassetteList().get(j).getStartRecipe().setStartReticleList(checkConditionForOpeStartOutRetCode.getStartReticleList());
                        startCassette.getLotInCassetteList().get(j).getStartRecipe().setStartFixtureList(checkConditionForOpeStartOutRetCode.getStartFixtureList());
                    }
                }
            } else if (Validations.isEquals(retCodeConfig.getEquipmentProcessDurableNotRequired(), e.getCode())) {
            } else {
                throw e;
            }
        }

        /*-----------------------------------------------------------*/
        /*   machine recipe convert to layout recipe                 */
        /*-----------------------------------------------------------*/
        LayoutRecipeParams.ConvertEquipmentFurnaceRecipeParams convertEquipmentFurnaceRecipeParams = new LayoutRecipeParams.ConvertEquipmentFurnaceRecipeParams();
        convertEquipmentFurnaceRecipeParams.setEquipmentId(params.getEquipmentID());
        convertEquipmentFurnaceRecipeParams.setStartCassettes(strStartCassette);
        strStartCassette = layoutRecipeMethod.equipmentFurnaceRecipeConvert(objCommon, convertEquipmentFurnaceRecipeParams);
        params.setStartCassetteList(strStartCassette);

        /*------------------------------------------------------------------*/
        /*   machine recipe convert to chamber level recipe                 */
        /*------------------------------------------------------------------*/
        ChamberLevelRecipeReserveParam chamberLevelRecipeReserveParam = new ChamberLevelRecipeReserveParam();
        chamberLevelRecipeReserveParam.setEquipmentId(params.getEquipmentID());
        chamberLevelRecipeReserveParam.setStartCassettes(strStartCassette);
        strStartCassette = chamberLevelRecipeMethod.chamberLevelRecipeMoveQueryRpt(objCommon, chamberLevelRecipeReserveParam);
        params.setStartCassetteList(strStartCassette);

        //step25 - machineRecipe_GetListForRecipeBodyManagement
        List<Infos.RecipeBodyManagement> recipeBodyManagements = machineRecipeMethod.machineRecipeGetListForRecipeBodyManagement(objCommon, params.getEquipmentID(), strStartCassette);
        if (CimArrayUtils.isNotEmpty(recipeBodyManagements)) {
            for (Infos.RecipeBodyManagement recipeBodyManagement : recipeBodyManagements) {
                Boolean downLoadFlag = false;
                if (CimBooleanUtils.isTrue(recipeBodyManagement.getForceDownLoadFlag())) {
                    downLoadFlag = true;
                } else {
                    if (CimBooleanUtils.isTrue(recipeBodyManagement.getRecipeBodyConfirmFlag())) {
                        Params.RecipeCompareReqParams recipeCompareReqParams = new Params.RecipeCompareReqParams();
                        recipeCompareReqParams.setEquipmentID(params.getEquipmentID());
                        recipeCompareReqParams.setMachineRecipeID(recipeBodyManagement.getMachineRecipeId());
                        recipeCompareReqParams.setPhysicalRecipeID(recipeBodyManagement.getPhysicalRecipeId());
                        recipeCompareReqParams.setFileLocation(recipeBodyManagement.getFileLocation());
                        recipeCompareReqParams.setFileName(recipeBodyManagement.getFileName());
                        recipeCompareReqParams.setFormatFlag(recipeBodyManagement.getFormatFlag());
                        try {
                            recipeService.sxRecipeCompareReq(objCommon, recipeCompareReqParams);
                        } catch (ServiceException e) {
                            if (!Validations.isEquals(retCodeConfigEx.getTcsMMTapPPConfirmError(), e.getCode())){
                                throw e;
                            } else {
                                if (recipeBodyManagement.getConditionalDownLoadFlag()){
                                    //--------------------------
                                    // Conditional Down Load
                                    //--------------------------
                                    downLoadFlag = true;
                                } else {
                                    throw new ServiceException(new OmCode(retCodeConfigEx.getRecipeConfirmError(), recipeBodyManagement.getMachineRecipeId().getValue()));
                                }
                            }
                        }
                    }
                }
                if (CimBooleanUtils.isTrue(downLoadFlag)) {

                    //step27 - txRecipeDownloadReq
                    //---------------------
                    // Recipe Down Load
                    //---------------------
                    Params.RecipeDownloadReqParams recipeDownloadReqParams = new Params.RecipeDownloadReqParams();
                    recipeDownloadReqParams.setEquipmentID(params.getEquipmentID());
                    recipeDownloadReqParams.setMachineRecipeID(recipeBodyManagement.getMachineRecipeId());
                    recipeDownloadReqParams.setPhysicalRecipeID(recipeBodyManagement.getPhysicalRecipeId());
                    recipeDownloadReqParams.setFileLocation(recipeBodyManagement.getFileLocation());
                    recipeDownloadReqParams.setFileName(recipeBodyManagement.getFileName());
                    recipeDownloadReqParams.setFormatFlag(recipeBodyManagement.getFormatFlag());
                    recipeService.sxRecipeDownloadReq(objCommon, recipeDownloadReqParams);
                }
            }
        }
        ObjectIdentifier dummyLotID = new ObjectIdentifier();
        for (Infos.StartCassette startCassette : strStartCassette) {
            if (CimStringUtils.equals(BizConstant.SP_LOADPURPOSETYPE_EMPTYCASSETTE, startCassette.getLoadPurposeType())) {

                //step28 - lot_CassetteCategory_CheckForContaminationControl
                lotMethod.lotCassetteCategoryCheckForContaminationControl(objCommon, dummyLotID, startCassette.getCassetteID(), params.getEquipmentID(), startCassette.getLoadPortID());

            } else {
                List<Infos.LotInCassette> lotInCassetteList = startCassette.getLotInCassetteList();
                if (CimArrayUtils.isNotEmpty(lotInCassetteList)) {
                    for (Infos.LotInCassette lotInCassette : lotInCassetteList) {
                        if (CimBooleanUtils.isFalse(lotInCassette.getMoveInFlag())) {
                            continue;
                        }

                        //step29 - lot_CassetteCategory_CheckForContaminationControl
                        lotMethod.lotCassetteCategoryCheckForContaminationControl(objCommon, lotInCassette.getLotID(), startCassette.getCassetteID(), params.getEquipmentID(), startCassette.getLoadPortID());

                    }
                }
            }
        }

        //step30 - emptyCassette_CheckCategoryForOperation
        cassetteMethod.emptyCassetteCheckCategoryForOperation(objCommon, strStartCassette);
        for (Infos.StartCassette startCassette : strStartCassette) {
            //step31 - cassette_dispatchState_Change
            cassetteMethod.cassetteDispatchStateChange(objCommon, startCassette.getCassetteID(), true);
        }
        ObjectIdentifier dummyControlJobID = new ObjectIdentifier();
        Params.CJStatusChangeReqParams cjStatusChangeReqParams = new Params.CJStatusChangeReqParams();
        cjStatusChangeReqParams.setControlJobID(dummyControlJobID);
        cjStatusChangeReqParams.setControlJobAction(BizConstant.SP_CONTROLJOBACTION_TYPE_CREATE);
        Infos.ControlJobCreateRequest controlJobCreateRequest = new Infos.ControlJobCreateRequest();
        controlJobCreateRequest.setEquipmentID(params.getEquipmentID());
        controlJobCreateRequest.setPortGroup("");
        controlJobCreateRequest.setStartCassetteList(strStartCassette);
        cjStatusChangeReqParams.setControlJobCreateRequest(controlJobCreateRequest);

        //step32 - txCJStatusChangeReq
        Results.CJStatusChangeReqResult cjStatusChangeReqResultRetCode = controlJobProcessJobService.sxCJStatusChangeReqService(objCommon, cjStatusChangeReqParams);


        //step33 - equipment_reservedControlJobID_Set
        equipmentMethod.equipmentReservedControlJobIDSet(objCommon, params.getEquipmentID(), cjStatusChangeReqResultRetCode.getControlJobID());
        //step34 - process_startReserveInformation_Get
        Outputs.ObjProcessStartReserveInformationGetOut processStartReserveInformation = processMethod.processStartReserveInformationGet(objCommon, strStartCassette, params.getEquipmentID(), false);

        log.debug("【step4-5】apply DOC information for StartCassette.");
        String strTempFPCAdoptFlag = StandardProperties.OM_DOC_ENABLE_FLAG.getValue();
        int tempFPCAdoptFlag = CimStringUtils.isEmpty(strTempFPCAdoptFlag) ? 0 : Integer.parseInt(strTempFPCAdoptFlag);
        if (1 == tempFPCAdoptFlag) {
            log.debug("DOC Adopt Flag is ON. Now apply FPCInfo.");
            List<Infos.StartCassette> exchangeOut = fpcMethod.fpcStartCassetteInfoExchange(objCommon,
                    BizConstant.SP_FPC_EXCHANGETYPE_STARTRESERVEINFO, params.getEquipmentID(), processStartReserveInformation.getStartCassetteList());

            processStartReserveInformation.setStartCassetteList(exchangeOut);
        } else {
            log.debug("DOC Adopt Flag is OFF. Now only check whiteFlag.");
            //【step4-6】check DOC process condition of StartCassette.
            log.debug("【step4-6】check DOC process condition of StartCassette.");
            List<Infos.FPCProcessCondition> checkOut = fpcMethod.fpcStartCassetteProcessConditionCheck(
                    objCommon, params.getEquipmentID(), processStartReserveInformation.getStartCassetteList(), false, true);

        }

        //step37 - process_startReserveInformation_Set__090
        processMethod.processStartReserveInformationSet(objCommon, params.getEquipmentID(), "",
                cjStatusChangeReqResultRetCode.getControlJobID(), processStartReserveInformation.getStartCassetteList(), false);

        for (Infos.StartCassette startCassette : strStartCassette) {

            //step38 - equipment_allocatedMaterial_Add
            equipmentMethod.equipmentAllocatedMaterialAdd(objCommon, params.getEquipmentID(), startCassette.getCassetteID(), startCassette.getLoadPortID(), startCassette.getLoadPurposeType(), cjStatusChangeReqResultRetCode.getControlJobID());
        }
//        Inputs.SendMoveInReserveForIBReqIn sendMoveInReserveForIBReqIn = new Inputs.SendMoveInReserveForIBReqIn();
//        sendMoveInReserveForIBReqIn.setObjCommonIn(objCommon);
//        sendMoveInReserveForIBReqIn.setEquipmentID(params.getEquipmentID());
//        sendMoveInReserveForIBReqIn.setControlJobID(cjStatusChangeReqResultRetCode.getControlJobID());
//        sendMoveInReserveForIBReqIn.setStrStartCassette(processStartReserveInformation.getStartCassetteList());
//        Outputs.SendMoveInReserveForIBReqOut sendMoveInReserveForIBReqOut = (Outputs.SendMoveInReserveForIBReqOut)tcsMethod.sendTCSReq(TCSReqEnum.sendMoveInReserveForIBReq,sendMoveInReserveForIBReqIn);
        String tmpSleepTimeValue = StandardProperties.OM_EAP_CONNECT_SLEEP_TIME.getValue();
        String tmpRetryCountValue = StandardProperties.OM_EAP_CONNECT_RETRY_COUNT.getValue();
        Long sleepTimeValue = 0L;
        Long retryCountValue = 0L;

        if (0 == CimStringUtils.length(tmpSleepTimeValue)) {
            sleepTimeValue = BizConstant.SP_DEFAULT_SLEEP_TIME_TCS;
        } else {
            sleepTimeValue = CimNumberUtils.longValue(tmpSleepTimeValue);
        }

        if (0 == CimStringUtils.length(tmpRetryCountValue)) {
            retryCountValue = BizConstant.SP_DEFAULT_RETRY_COUNT_TCS;
        } else {
            retryCountValue = CimNumberUtils.longValue(tmpRetryCountValue);
        }

        //QianDao add MES-EAP Integration cassetteChangeFlag
        for (int retryNum = 0; retryNum < (retryCountValue + 1); retryNum++) {
            log.info("{} {}", "loop to retryCountValue + 1", retryNum);
            /*--------------------------*/
            /*    Send Request to EAP   */
            /*--------------------------*/
            IEAPRemoteManager eapRemoteManager = eapMethod.eapRemoteManager(objCommon,params.getUser(),params.getEquipmentID(),null,true);
            if (null == eapRemoteManager) {
                log.info("MES not configure EAP host");
                break;
            }
            Params.MoveInReserveForIBReqParams sendToEapParams = new Params.MoveInReserveForIBReqParams();
            sendToEapParams.setUser(params.getUser());
            sendToEapParams.setEquipmentID(params.getEquipmentID());
            sendToEapParams.setControlJobID(cjStatusChangeReqResultRetCode.getControlJobID());
            // chamber level recipe 转换 [ESEC Service]
            List<Infos.StartCassette> startCassetteList = processStartReserveInformation.getStartCassetteList();
            startCassetteList.parallelStream().forEach(startCassette -> {
                startCassette.getLotInCassetteList().forEach(lotInCassette -> {
                    Infos.StartRecipe startRecipe = lotInCassette.getStartRecipe();
                    if (Objects.nonNull(startRecipe) &&
                            ObjectIdentifier.isNotEmpty(startRecipe.getChamberLevelRecipeID())) {
                        startRecipe.setPhysicalRecipeID(
                                ObjectIdentifier.fetchValue(startRecipe.getChamberLevelRecipeID()));
                    }
                });
            });
            sendToEapParams.setStartCassetteList(startCassetteList);
            //QianDao add MES-EAP Integration cassetteChangeFlag
            //sendToEapParams.setCassetteChangeFlag(equipmentMethod.equipmentBRInfoGetDR(objCommon, equipmentID).isCassetteChangeFlag());
            try {
                Object moveInReserveForIBReqEqpOut = eapRemoteManager.sendMoveInReserveForIBReq(sendToEapParams);
                log.info("Now EAP subSystem is alive!! Go ahead");
                break;
            } catch (ServiceException ex) {
                if (Validations.isEquals(ex.getCode(), retCodeConfig.getTcsNoResponse())) {
                    log.info("{} {}", "EAP subsystem has return NO_RESPONSE!! just retry now!!  now count...", retryNum);
                    log.info("{} {}", "now sleeping... ", sleepTimeValue);
                    if (retryNum != retryCountValue){
                        try {
                            Thread.sleep(sleepTimeValue);
                            continue;
                        } catch (InterruptedException e) {
                            ex.addSuppressed(e);
                            Thread.currentThread().interrupt();
                            throw ex;
                        }
                    }else {
                        Validations.check(true,retCodeConfig.getTcsNoResponse());
                    }
                } else {
                    Validations.check(true,new OmCode(ex.getCode(),ex.getMessage()));
                }
            }
        }

        // step40 - APCRuntimeCapability_RegistDR
        apcMethod.apcRuntimeCapabilityRegistDR(objCommon, cjStatusChangeReqResultRetCode.getControlJobID(), apcRunTimeCapabilityResponseList);

        // step41 - cassette_APCInformation_GetDR
        List<Infos.ApcBaseCassette> apcBaseCassetteList = null;
        try {
            apcBaseCassetteList = cassetteMethod.cassetteAPCInformationGetDR(objCommon, params.getEquipmentID(), strStartCassette);
        } catch (ServiceException e) {
            if (Validations.isEquals(retCodeConfig.getSystemError(), e.getCode())){
                throw e;
            }
        }

        // step42 - APCMgr_SendControlJobInformationDR
        try {
            apcMethod.APCMgrSendControlJobInformationDR(objCommon, params.getEquipmentID(), cjStatusChangeReqResultRetCode.getControlJobID(), BizConstant.SP_APC_CONTROLJOBSTATUS_CREATED, apcBaseCassetteList);
        } catch (ServiceException e) {
            retCode = e.getCode();
            if (!Validations.isEquals(retCodeConfigEx.getOkNoIF(), e.getCode())){
                throw e;
            }
        }
        if (retCode == 0){
            String tmpString = apcifControlStatus;
            apcifControlStatus = BizConstant.SP_APC_CONTROLJOBSTATUS_CANCELED;
        }
        return cjStatusChangeReqResultRetCode.getControlJobID();
    }

    @Override
    public Results.MoveInReserveForTOTIReqResult sxMoveInReserveForTOTIReq(Infos.ObjCommon objCommon, Params.MoveInReserveForTOTIReqInParam params, String claimMemo) {
        /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/
        /*   Initialize                                                          */
        /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/
        Results.MoveInReserveForTOTIReqResult out = new Results.MoveInReserveForTOTIReqResult();
        ObjectIdentifier equipmentID = params.getEquipmentID();
        ObjectIdentifier controlJobID = params.getControlJobID();
        String portGroupID = params.getPortGroupID();
        List<Infos.StartCassette> tmpStartCassette = params.getStrStartCassetteSequence();

        /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/
        /*                                                                       */
        /*   Check Process                                                       */
        /*                                                                       */
        /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/
        int lenCassette = CimArrayUtils.getSize(tmpStartCassette);
        if (0 >= lenCassette){
            log.info("0>= CIMFWStrLen(tmpStartCassette.length())");
            throw new ServiceException(retCodeConfig.getInvalidInputParam());
        }
        log.info("Check Transaction ID and equipment Category combination.");
        //【step1】 - equipment_categoryVsTxID_CheckCombination
        equipmentMethod.equipmentCategoryVsTxIDCheckCombination(objCommon, equipmentID);
        //Input parameter check.
        //Check every lot has at least one wafer with processJobExecFlag == TRUE
        //【step2】 - lot_processJobExecFlag_ValidCheck
        Outputs.ObjLotProcessJobExecFlagValidCheckOut lotProcessJobExecFlagValidCheckOut = lotMethod.lotProcessJobExecFlagValidCheck(objCommon, tmpStartCassette);

        //-------------------------------------------------
        //  Check Scrap Wafer Exsit In Carrier
        //-------------------------------------------------
        List<ObjectIdentifier> cassetteIDs = new ArrayList<>();
        int castLen = CimArrayUtils.getSize(tmpStartCassette);
        for (int i = 0; i < castLen; i++) {
            cassetteIDs.add(tmpStartCassette.get(i).getCassetteID());
        }
        //【step3】 - cassette_scrapWafer_SelectDR
        List<Infos.LotWaferMap> lotWaferMaps = cassetteMethod.cassetteScrapWaferSelectDR(objCommon, cassetteIDs);
        int scrapCount = CimArrayUtils.getSize(lotWaferMaps);
        if (scrapCount > 0){
            log.error("ScrapWafer Found,{}",scrapCount);
            throw new ServiceException(retCodeConfig.getFoundScrap());
        }
        /*-----------------------------------------*/
        /*   call txAPCRTSystemInfoInq        */
        /*-----------------------------------------*/
        log.info("call txAPCRTSystemInfoInq");
        //TODO-NOTIMPL:step4 - txAPCRTSystemInfoInq

        //TODO-NOTIMPL: step5 - txAPCRcpParmChgInq
        // APC result check.
        // Check every lot has at least one wafer with processJobExecFlag == TRUE:step5 - txAPCRcpParmChgInq
        log.info("Check every lot of APC result has at least one wafer with processJobExecFlag == TRUE.");
        Outputs.ObjLotProcessJobExecFlagValidCheckOut processJobExecFlagValidCheckOut = lotMethod.lotProcessJobExecFlagValidCheck(objCommon, tmpStartCassette);

        // step7 - object_lockMode_Get
        Inputs.ObjLockModeIn objLockModeIn = new Inputs.ObjLockModeIn();
        objLockModeIn.setObjectID(equipmentID);
        objLockModeIn.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
        objLockModeIn.setFunctionCategory(TransactionIDEnum.START_LOTS_RESERVATION_FOR_TAKE_OUT_IN_REQ.getValue());
        objLockModeIn.setUserDataUpdateFlag(false);
        Outputs.ObjLockModeOut objLockModeOut = objectMethod.objectLockModeGet(objCommon, objLockModeIn);
        Long lockMode = objLockModeOut.getLockMode();
        Inputs.ObjAdvanceLockIn strAdvancedobjecLockin = new Inputs.ObjAdvanceLockIn();
        if (!lockMode.equals(BizConstant.SP_EQP_LOCK_MODE_WRITE)){
            // step8 - advanced_object_Lock
            List<String> dummySeq;
            dummySeq = new ArrayList<>(0);
            strAdvancedobjecLockin.setObjectID(equipmentID);
            strAdvancedobjecLockin.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
            strAdvancedobjecLockin.setObjectType(BizConstant.SP_OBJECTLOCK_OBJECTTYPE_MAINOBJECT);
            strAdvancedobjecLockin.setLockType(objLockModeOut.getRequiredLockForMainObject());
            strAdvancedobjecLockin.setKeyList(dummySeq);
            objectLockMethod.advancedObjectLock(objCommon, strAdvancedobjecLockin);
            // step9 - object_Lock
            objectLockMethod.objectLock(objCommon, CimMachine.class, equipmentID);
        } else {
            /*--------------------------------------------*/
            /*                                            */
            /*      Machine Object Lock Process           */
            /*                                            */
            /*--------------------------------------------*/
            // step10 - object_Lock
            objectLockMethod.objectLock(objCommon, CimMachine.class, equipmentID);
        }
        /*--------------------------------------------------------*/
        /*   Get All Ports being in the same Port Group as ToPort */
        /*--------------------------------------------------------*/
        Infos.EqpPortInfo eqpPortInfo = portMethod.portResourceAllPortsInSameGroupGet(objCommon, equipmentID, tmpStartCassette.get(0).getLoadPortID());

        /*---------------------------------------------------------*/
        /* Lock All Ports being in the same Port Group as ToPort   */
        /*---------------------------------------------------------*/
        // step12 - object_LockForEquipmentResource
        List<Infos.EqpPortStatus> eqpPortStatuses = eqpPortInfo.getEqpPortStatuses();
        int lenToPort = CimArrayUtils.getSize(eqpPortStatuses);
        for (int i = 0; i < lenToPort; i++){
            objectLockMethod.objectLockForEquipmentResource(objCommon, equipmentID, eqpPortStatuses.get(i).getPortID(), BizConstant.SP_CLASSNAME_POSPORTRESOURCE);
        }
        /*--------------------------------------------*/
        /*                                            */
        /*       Cassette Object Lock Process         */
        /*                                            */
        /*--------------------------------------------*/
        List<ObjectIdentifier> lotIDs = new ArrayList<>();
        cassetteIDs = new ArrayList<>();
        int nILen = CimArrayUtils.getSize(tmpStartCassette);
        for (int i = 0; i < nILen; i++){
            cassetteIDs.add(tmpStartCassette.get(i).getCassetteID());
            List<Infos.LotInCassette> lotInCassetteList = tmpStartCassette.get(i).getLotInCassetteList();
            int nJLen = CimArrayUtils.getSize(lotInCassetteList);
            for (int j = 0; j < nJLen; j++){
                lotIDs.add(lotInCassetteList.get(j).getLotID());
            }
        }
        // step13- objectSequence_Lock
        objectLockMethod.objectSequenceLock(objCommon, CimCassette.class, cassetteIDs);
        // step14 - objectSequence_Lock
        objectLockMethod.objectSequenceLock(objCommon, CimLot.class, lotIDs);
        /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/
        /*                                                                       */
        /*   Check Process                                                       */
        /*                                                                       */
        /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/
        //-------------------------------------------------
        //  Check scrap wafer existence in carrier
        //-------------------------------------------------
        cassetteIDs = new ArrayList<>();
        for (int cnt = 0; cnt < castLen; cnt++){
            cassetteIDs.add(tmpStartCassette.get(cnt).getCassetteID());
        }
        //【step15】 - cassette_scrapWafer_SelectDR
        lotWaferMaps = cassetteMethod.cassetteScrapWaferSelectDR(objCommon, cassetteIDs);
        scrapCount = CimArrayUtils.getSize(lotWaferMaps);
        if (scrapCount > 0){
            log.error("ScrapWafer Found {}",scrapCount);
            throw new ServiceException(retCodeConfig.getFoundScrap());
        }
        /*-----------------------------------------------------------------------*/
        /*                                                                       */
        /*   Check Process for Cassette                                          */
        /*                                                                       */
        /*   The following conditions are checked by this object                 */
        /*                                                                       */
        /*   - controlJobID                                                      */
        /*   - multiLotType                                                      */
        /*   - transferState                                                     */
        /*   - transferReserved                                                  */
        /*   - dispatchState                                                     */
        /*   - maxBatchSize                                                      */
        /*   - minBatchSize                                                      */
        /*   - emptyCassetteCount                                                */
        /*   - cassette'sloadingSequenceNomber                                   */
        /*   - eqp's multiRecipeCapability and recipeParameter                   */
        /*   - Upper/Lower Limit for RecipeParameterChange                       */
        /*   - MonitorLotCount or OperationStartLotCount                         */
        /*                                                                       */
        /*-----------------------------------------------------------------------*/
        //【step16】 - cassette_CheckConditionForOperation
        String operation = BizConstant.SP_OPERATION_STARTRESERVATION;
        cassetteMethod.cassetteCheckConditionForOperation(objCommon, equipmentID, portGroupID, tmpStartCassette, operation);
        /*-----------------------------------------------------------------------*/
        /*                                                                       */
        /*   Check Process for Lot                                               */
        /*                                                                       */
        /*   The following conditions are checked by this object                 */
        /*                                                                       */
        /*   - controlJobID                                                      */
        /*   - lot's equipmentID                                                 */
        /*   - lotHoldState                                                      */
        /*   - lotProcessState                                                   */
        /*   - lotInventoryState                                                 */
        /*   - entityInhibition                                                  */
        /*   - minWaferCount                                                     */
        /*   - equipment's availability for specified lot                        */
        /*                                                                       */
        /*-----------------------------------------------------------------------*/
        //【step17】 - lot_CheckConditionForOperation
        lotMethod.lotCheckConditionForOperation(objCommon, equipmentID, portGroupID, tmpStartCassette, operation);

        /*-----------------------------------------------------------------------------*/
        /*                                                                             */
        /*   Check Equipment Port for Start Reservation                                */
        /*                                                                             */
        /*   The following conditions are checked by this object                       */
        /*                                                                             */
        /*   1. In-parm's portGroupID must not have controlJobID.                      */
        /*   2. All of ports' loadMode must be CIMFW_PortRsc_Input or _InputOutput.    */
        /*   3. All of port, which is registered as in-parm's portGroup, must be       */
        /*      _LoadAvail or _LoadReq or _UnloadReq when equipment is online.         */
        /*   4. strStartCassette[].loadPortID's portGroupID must be same               */
        /*      as in-parm's portGroupID.                                              */
        /*   5. strStartCassette[].loadPurposeType must be match as specified port's   */
        /*      loadPutposeType.                                                       */
        /*   6. strStartCassette[].loadSequenceNumber must be same as specified port's */
        /*      loadSequenceNumber.                                                    */
        /*                                                                             */
        /*-----------------------------------------------------------------------------*/
        log.info("===== equipment_portState_CheckForStartReservation() ==========");
        //【step18】 -  equipment_portState_CheckForTakeOutIn
        equipmentMethod.equipmentPortStateCheckForTakeOutIn(objCommon, equipmentID, portGroupID, tmpStartCassette);
        /*-----------------------------------------------------------------------*/
        /*                                                                       */
        /*   Check Process for FlowBatch                                         */
        /*                                                                       */
        /*   The following conditions are checked by this object                 */
        /*                                                                       */
        /*   1. whether in-parm's equipment has reserved flowBatchID or not      */
        /*      fill  -> all of flowBatch member and in-parm's lot must be       */
        /*               same perfectly.                                         */
        /*      blank -> no check                                                */
        /*                                                                       */
        /*   2. whether lot is in flowBatch section or not                       */
        /*      in    -> lot must have flowBatchID, and flowBatch must have      */
        /*               reserved equipmentID.                                   */
        /*               if lot is on target operation, flowBatch's reserved     */
        /*               equipmentID and in-parm's equipmentID must be same.     */
        /*      out   -> no check                                                */
        /*                                                                       */
        /*-----------------------------------------------------------------------*/
        //【step19】 - equipment_lot_CheckFlowBatchConditionForOpeStart__090
        Inputs.ObjEquipmentLotCheckFlowBatchConditionForOperationStartIn operationStartIn=new Inputs.ObjEquipmentLotCheckFlowBatchConditionForOperationStartIn();
        operationStartIn.setEquipmentID(equipmentID);
        operationStartIn.setPortGroupID(portGroupID);
        operationStartIn.setStartCassetteList(tmpStartCassette);
        ObjectIdentifier checkFlowBatchConditionForOperationStartOutRetCode = equipmentMethod.equipmentLotCheckFlowBatchConditionForOpeStart(objCommon, operationStartIn);
        /*-----------------------------------------------------------------------*/
        /*                                                                       */
        /*   Check Process for Process Durable                                   */
        /*                                                                       */
        /*   The following conditions are checked by this object                 */
        /*                                                                       */
        /*   1. Whether equipment requires process durable or not                */
        /*      If no-need, return OK;                                           */
        /*                                                                       */
        /*   2. At least one of reticle / fixture for each reticleGroup /        */
        /*      fixtureGroup is in the equipment or not.                         */
        /*      Even if required reticle is in the equipment, its status must    */
        /*      be _Available or _InUse.                                         */
        /*                                                                       */
        /*-----------------------------------------------------------------------*/

        /*-----------------------------------------*/
        /*   Check Process Durable Required Flag   */
        /*-----------------------------------------*/
        //【step20】 - equipment_processDurableRequiredFlag_Get
        try {
            equipmentMethod.equipmentProcessDurableRequiredFlagGet(objCommon, equipmentID);
        } catch (ServiceException e){
            if (Validations.isEquals(retCodeConfig.getEquipmentProcessDurableReticleRequired(), e.getCode())
                    || Validations.isEquals(retCodeConfig.getEquipmentProcessDurableFixtRequired(), e.getCode())){
                log.info("rc == RC_EQP_PROCDRBL_RTCL_REQD || rc == RC_EQP_PROCDRBL_FIXT_REQD");
                int nIMax = CimArrayUtils.getSize(tmpStartCassette);
                for (int i = 0; i < nIMax; i++) {
                    if (CimStringUtils.equals(BizConstant.SP_LOADPURPOSETYPE_EMPTYCASSETTE, tmpStartCassette.get(i).getLoadPurposeType())){
                        continue;
                    }
                    int nJMax = CimArrayUtils.getSize(tmpStartCassette.get(i).getLotInCassetteList());
                    for (int j = 0; j < nJMax; j++) {
                        if (CimBooleanUtils.isFalse(tmpStartCassette.get(i).getLotInCassetteList().get(j).getMoveInFlag())){
                            continue;
                        }
                        /*--------------------------------------------------*/
                        /*   Check Process Durable Condition for OpeStart   */
                        /*--------------------------------------------------*/
                        //【step21】 - processDurable_CheckConditionForOpeStart
                        Outputs.ObjProcessDurableCheckConditionForOperationStartOut durableCheckConditionForOperationStartOutRetCode = processMethod.processDurableCheckConditionForOpeStart(objCommon,
                                equipmentID,
                                tmpStartCassette.get(0).getLotInCassetteList().get(j).getStartRecipe().getLogicalRecipeID(),
                                tmpStartCassette.get(0).getLotInCassetteList().get(j).getStartRecipe().getMachineRecipeID(),
                                tmpStartCassette.get(0).getLotInCassetteList().get(j).getLotID());
                        /*---------------------------------------*/
                        /*   Set Available Reticles / Fixtures   */
                        /*---------------------------------------*/
                        tmpStartCassette.get(i).getLotInCassetteList().get(j).getStartRecipe().setStartReticleList(durableCheckConditionForOperationStartOutRetCode.getStartReticleList());
                        tmpStartCassette.get(i).getLotInCassetteList().get(j).getStartRecipe().setStartFixtureList(durableCheckConditionForOperationStartOutRetCode.getStartFixtureList());
                    }
                }
                //result.setReturnCode(retCodeConfig.getSucc());
            }else if (Validations.isEquals(retCodeConfig.getEquipmentProcessDurableNotRequired(), e.getCode())){
                //result.setReturnCode(retCodeConfig.getSucc());
            }else {
                log.info("!RC_EQP_PROCDRBL_RTCL_REQD && !RC_EQP_PROCDRBL_FIXT_REQD && !RC_EQP_PROCDRBL_NOT_REQD");
                throw e;
            }
        }

        /*----------------------------------------------------------------------------------------------------------------------*/
        /*                                                                                                                      */
        /*   Confirmation for Uploaded Recipe Body and Eqp's Recipe Body                                                        */
        /*                                                                                                                      */
        /*   When the following conditions are all matched, recipe body                                                         */
        /*   confirmation request is sent to TCS.                                                                               */
        /*                                                                                                                      */
        /*   1. Equipment's onlineMode is Online                                                                                */
        /*   2. Equipment's recipe body manage flag is TRUE.                                                                    */
        /*   3. Machine recipe's recipe body confirm flag is TRUE.                                                              */
        //                                                                                                                      */
        //   Force Down Load Flag  Recipe Body Confirm Flag  Conditional Down Load Flag                                         */
        //           Yes                      No                       No               -> Download it without confirmation.    */
        //           No                       Yes                      No               -> Confirm only.                        */
        //           No                       Yes                      Yes              -> If confirmation is NG, download it.  */
        //           No                       No                       No               -> No action.                           */
        /*                                                                                                                      */
        /*----------------------------------------------------------------------------------------------------------------------*/
        int nIMax = 0;
        //---------------------------------------------------
        // Get Machine Recipe List for Recipe Body Mangement
        //---------------------------------------------------
        log.info("Get Machine Recipe List for Recipe Body Mangement.");
        //【step22】 - machineRecipe_GetListForRecipeBodyManagement
        List<Infos.RecipeBodyManagement>  machineRecipeGetListForRecipeBodyManagementOut = machineRecipeMethod.machineRecipeGetListForRecipeBodyManagement(objCommon, equipmentID, tmpStartCassette);

        int targetRecipeLen = CimArrayUtils.getSize(machineRecipeGetListForRecipeBodyManagementOut);
        if (targetRecipeLen == 0){
            log.info("Recipe for Recipe Body Management does not exist.");
        }

        for(int targetRecipeCnt=0; targetRecipeCnt<targetRecipeLen; targetRecipeCnt++) {
            //-------------------
            // Force Down Load
            //-------------------
            boolean downLoadFlag = FALSE;
            if(TRUE.equals(machineRecipeGetListForRecipeBodyManagementOut.get(targetRecipeCnt).getForceDownLoadFlag())) {
                log.info("downLoadFlag turns to True.");
                downLoadFlag = TRUE;
            } else {
                if(TRUE.equals(machineRecipeGetListForRecipeBodyManagementOut.get(targetRecipeCnt).getRecipeBodyConfirmFlag())) {
                    //---------------------
                    // Recipe Confirmation
                    //---------------------
                    log.info("Call txRecipeCompareReq");
                    Params.RecipeCompareReqParams recipeCompareReqParams=new Params.RecipeCompareReqParams();
                    recipeCompareReqParams.setEquipmentID(equipmentID);
                    recipeCompareReqParams.setMachineRecipeID(machineRecipeGetListForRecipeBodyManagementOut.get(targetRecipeCnt).getMachineRecipeId());
                    recipeCompareReqParams.setPhysicalRecipeID(machineRecipeGetListForRecipeBodyManagementOut.get(targetRecipeCnt).getPhysicalRecipeId());
                    recipeCompareReqParams.setFileLocation(machineRecipeGetListForRecipeBodyManagementOut.get(targetRecipeCnt).getFileLocation());
                    recipeCompareReqParams.setFileName(machineRecipeGetListForRecipeBodyManagementOut.get(targetRecipeCnt).getFileName());
                    recipeCompareReqParams.setFormatFlag(machineRecipeGetListForRecipeBodyManagementOut.get(targetRecipeCnt).getFormatFlag());
                    recipeCompareReqParams.setClaimMemo("");

                    try {
                        //step23 - txRecipeCompareReq
                        recipeService.sxRecipeCompareReq(  objCommon, recipeCompareReqParams);
                    } catch (ServiceException ex){
                        if (!Validations.isEquals(ex.getCode(),retCodeConfigEx.getTcsMMTapPPConfirmError())){
                            throw ex;
                        }

                        if(TRUE.equals(machineRecipeGetListForRecipeBodyManagementOut.get(targetRecipeCnt).getConditionalDownLoadFlag())) {
                            //--------------------------
                            // Conditional Down Load
                            //--------------------------
                            log.info("downLoadFlag turns to True.");
                            downLoadFlag = TRUE;
                        } else {
                            //Recipe Body Confirmation error. the Recipe Body differs between Uploaded it to system and the owned it by equipment.
                            log.info("Recipe Body Confirmation error. the Recipe Body differs between Uploaded it to system and the owned it by equipment.\n{}",machineRecipeGetListForRecipeBodyManagementOut.get(targetRecipeCnt).getMachineRecipeId().getValue());
                            Validations.check(true,retCodeConfigEx.getRecipeConfirmError(),
                                    machineRecipeGetListForRecipeBodyManagementOut.get(targetRecipeCnt).getMachineRecipeId().getValue());
                        }
                    }
                } else {
                    log.info("Recipe Body management .. no action.");
                }
            }

            log.info("Recipe Down Load {}", (downLoadFlag?"True":"False"));
            if( downLoadFlag ) {
                //---------------------
                // Recipe Down Load
                //---------------------
                log.info("Call txRecipeDownloadReq");
                Params.RecipeDownloadReqParams recipeDownloadReqParams=new Params.RecipeDownloadReqParams();
                recipeDownloadReqParams.setEquipmentID(equipmentID);
                recipeDownloadReqParams.setMachineRecipeID(machineRecipeGetListForRecipeBodyManagementOut.get(targetRecipeCnt).getMachineRecipeId());
                recipeDownloadReqParams.setPhysicalRecipeID(machineRecipeGetListForRecipeBodyManagementOut.get(targetRecipeCnt).getPhysicalRecipeId());
                recipeDownloadReqParams.setFileLocation(machineRecipeGetListForRecipeBodyManagementOut.get(targetRecipeCnt).getFileLocation());
                recipeDownloadReqParams.setFileName(machineRecipeGetListForRecipeBodyManagementOut.get(targetRecipeCnt).getFileName());
                recipeDownloadReqParams.setFormatFlag(machineRecipeGetListForRecipeBodyManagementOut.get(targetRecipeCnt).getFormatFlag());
                recipeDownloadReqParams.setClaimMemo("");
                // step24 - txRecipeDownloadReq
                recipeService.sxRecipeDownloadReq( objCommon, recipeDownloadReqParams);

            }
        }

        /*---------------------------------------------------------------------------*/
        /*                                                                           */
        /*   Check Category for Copper/Non Copper                                    */
        /*                                                                           */
        /*   It is checked in the following method whether it is the condition       */
        /*   that Lot of the object is made of OpeStart.                             */
        /*                                                                           */
        /*   1. It is checked whether CassetteCategory of RequiredCassetteCategory   */
        /*      of PosLot and PosCassette is the same.                               */
        /*                                                                           */
        /*   2. It is checked whether CassetteCategoryCapability of CassetteCategory */
        /*      of PosCassette and PosPortResource is the same.                      */
        /*                                                                           */
        /*   3. It is proper condition if CassetteCategoryCapability is the same     */
        /*      as RequiredCassetteCategory and CassetteCategory.                    */
        /*                                                                           */
        /*---------------------------------------------------------------------------*/
        int nCastLen = CimArrayUtils.getSize(tmpStartCassette);
        int nLotInCastLen = 0;
        ObjectIdentifier dummyLotID = new ObjectIdentifier();
        for (int ii = 0; ii < nCastLen; ii++) {
            if (CimObjectUtils.equals(BizConstant.SP_LOADPURPOSETYPE_EMPTYCASSETTE,tmpStartCassette.get(ii).getLoadPurposeType())){
                //【step25】 - lot_CassetteCategory_CheckForContaminationControl
                lotMethod.lotCassetteCategoryCheckForContaminationControl(objCommon,
                        dummyLotID,
                        tmpStartCassette.get(ii).getCassetteID(),
                        equipmentID,
                        tmpStartCassette.get(ii).getLoadPortID());

            }else {
                nLotInCastLen = CimArrayUtils.getSize(tmpStartCassette.get(ii).getLotInCassetteList());
                for (int jj = 0; jj < nLotInCastLen; jj++) {
                    if (CimBooleanUtils.isFalse(tmpStartCassette.get(ii).getLotInCassetteList().get(jj).getMoveInFlag())){
                        continue;
                    }
                    //【step26】 - lot_CassetteCategory_CheckForContaminationControl
                    lotMethod.lotCassetteCategoryCheckForContaminationControl(objCommon,
                            tmpStartCassette.get(ii).getLotInCassetteList().get(jj).getLotID(),
                            tmpStartCassette.get(ii).getCassetteID(),
                            equipmentID,
                            tmpStartCassette.get(ii).getLoadPortID());

                }
            }
        }
        /*---------------------------------------------------------------------------*/
        /*                                                                           */
        /*   Check Carrier Type for next operation of Empty carrier.             */
        /*                                                                           */
        /*---------------------------------------------------------------------------*/
        log.info("call emptyCassette_CheckCategoryForOperation()");
        //【step27】 - emptyCassette_CheckCategoryForOperation
        cassetteMethod.emptyCassetteCheckCategoryForOperation(objCommon, tmpStartCassette);
        /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/
        /*                                                                       */
        /*   Main Process                                                        */
        /*                                                                       */
        /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/

        /*----------------------------------------------*/
        /*                                              */
        /*   Change Cassette's Dispatch State to TRUE   */
        /*                                              */
        /*----------------------------------------------*/
        nIMax = CimArrayUtils.getSize(tmpStartCassette);
        for (int i = 0; i < nIMax; i++) {
            //【step28】 - cassette_dispatchState_Change
            cassetteMethod.cassetteDispatchStateChange(objCommon, tmpStartCassette.get(i).getCassetteID(), true);
        }
        /*-------------------------------------------------------------*/
        /*                                                             */
        /*   Create Control Job and Assign to Each Cassettes / Lots    */
        /*                                                             */
        /*   - Create new controlJob                                   */
        /*   - Set created controlJobID to each cassettes / lots       */
        /*   - Set created controlJobID to equipment                   */
        /*                                                             */
        /*-------------------------------------------------------------*/
        Results.CJStatusChangeReqResult strCJStatusChangeReqResult = new Results.CJStatusChangeReqResult();
        Infos.ControlJobCreateRequest strControlJobCreateRequest = new Infos.ControlJobCreateRequest();
        ObjectIdentifier dummyControlJobID = new ObjectIdentifier();
        strControlJobCreateRequest.setEquipmentID(equipmentID);
        strControlJobCreateRequest.setPortGroup(portGroupID);
        //【step29】 -txCJStatusChangeReq
        Params.CJStatusChangeReqParams cjStatusChangeReqParams = new Params.CJStatusChangeReqParams();
        cjStatusChangeReqParams.setControlJobAction(BizConstant.SP_CONTROLJOBACTION_TYPE_CREATE);
        Infos.ControlJobCreateRequest controlJobCreateRequest = new Infos.ControlJobCreateRequest();
        cjStatusChangeReqParams.setControlJobCreateRequest(controlJobCreateRequest);
        cjStatusChangeReqParams.setControlJobID(dummyControlJobID);
        controlJobCreateRequest.setEquipmentID(equipmentID);
        controlJobCreateRequest.setPortGroup(portGroupID);
        controlJobCreateRequest.setStartCassetteList(tmpStartCassette);
        Results.CJStatusChangeReqResult cjStatusChangeReqOut = controlJobProcessJobService.sxCJStatusChangeReqService(objCommon, cjStatusChangeReqParams);

        /*----------------------------------------------------*/
        /*                                                    */
        /*   Set Start Reserved Control Job into Equipment    */
        /*                                                    */
        /*----------------------------------------------------*/
        //【step30】 - equipment_reservedControlJobID_Set
        equipmentMethod.equipmentReservedControlJobIDSet(objCommon, params.getEquipmentID(), cjStatusChangeReqOut.getControlJobID());
        /*----------------------------------------------------*/
        /*                                                    */
        /*   Get Start Information for each Cassette / Lot    */
        /*                                                    */
        /*   - strStartCassette information is not filled     */
        /*     perfectlly. By this object function, it will   */
        /*     be filled.                                     */
        /*                                                    */
        /*----------------------------------------------------*/
        //【step31】 -process_startReserveInformation_Get
        Outputs.ObjProcessStartReserveInformationGetOut processStartReserveInformation = processMethod.processStartReserveInformationGet(objCommon, tmpStartCassette, equipmentID, false);
        // Apply FPCInformation for StartCassette.
        Long tmpFPCAdoptFlag = StandardProperties.OM_DOC_ENABLE_FLAG.getLongValue();
        if (1 == tmpFPCAdoptFlag){
            log.info("DOC Adopt Flag is ON. Now apply FPCInfo.");
            //【step32】 - FPCStartCassetteInfo_Exchange
            String exchangeType = BizConstant.SP_FPC_EXCHANGETYPE_STARTRESERVEREQ;
            List<Infos.StartCassette> exchangeFPCStartCassetteInfoRetCode = fpcMethod.fpcStartCassetteInfoExchange(objCommon, exchangeType, equipmentID, processStartReserveInformation.getStartCassetteList());

            processStartReserveInformation.setStartCassetteList(exchangeFPCStartCassetteInfoRetCode);
        }else {
            log.info("DOC Adopt Flag is OFF. Now only check whiteFlag.");
            //【step33】 - FPC_startCassette_processCondition_Check
            List<Infos.FPCProcessCondition> fpcStartCassetteProcessConditionCheckRetCode = fpcMethod.fpcStartCassetteProcessConditionCheck(objCommon, equipmentID, processStartReserveInformation.getStartCassetteList(), false, true);

        }
        /*----------------------------------------------------*/
        /*                                                    */
        /*   Set Start Reservation Info to Each Lots' PO      */
        /*                                                    */
        /*   - Set created controlJobID into each cassette.   */
        /*   - Set created controlJobID into each lot.        */
        /*   - Set control job info (StartRecipe, DCDefs,     */
        /*     DCSpecs, Parameters, ...) into each lot's      */
        /*     cunrrent PO.                                   */
        /*                                                    */
        /*----------------------------------------------------*/
        //【step34】 - process_startReserveInformation_Set__090
        RetCode<Object> setProcessStartReserveInformation = null;
        processMethod.processStartReserveInformationSet(objCommon, equipmentID, portGroupID,
                cjStatusChangeReqOut.getControlJobID(), processStartReserveInformation.getStartCassetteList(), false);
        /*-------------------------------------------*/
        /*                                           */
        /*   Send TxMoveInReserveForTOTIReq to TCS   */
        /*                                           */
        /*-------------------------------------------*/
        Inputs.SendMoveInReserveReqIn sendMoveInReserveReqIn = new Inputs.SendMoveInReserveReqIn();
        sendMoveInReserveReqIn.setEquipmentID(equipmentID);
        sendMoveInReserveReqIn.setControlJobID(cjStatusChangeReqOut.getControlJobID());
        sendMoveInReserveReqIn.setObjCommonIn(objCommon);
        sendMoveInReserveReqIn.setPortGroupID(portGroupID);
        sendMoveInReserveReqIn.setStrStartCassette(processStartReserveInformation.getStartCassetteList());

        Outputs.SendMoveInReserveReqOut sendMoveInReserveReq = (Outputs.SendMoveInReserveReqOut)tcsMethod.sendTCSReq(TCSReqEnum.sendMoveInReserveReq,sendMoveInReserveReqIn);


        /*-------------------------------------------------*/
        /*   call APCRuntimeCapability_RegistDR            */
        /*-------------------------------------------------*/
        log.info("call APCRuntimeCapability_RegistDR");
        //TODO-IMPL step36 - APCRuntimeCapability_RegistDR
        //TODO-IMPL step37 - cassette_APCInformation_GetDR
        //TODO-IMPL step38 - APCMgr_SendControlJobInformationDR
        /*--------------------*/
        /*                    */
        /*   Return to Main   */
        /*                    */
        /*--------------------*/
        out.setControlJobID(cjStatusChangeReqOut.getControlJobID());
        return out;
    }

    @Override
    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param moveInReserveReqParams -
     * @return object
     * @author Bear
     * @date 2018/7/4 13:22
     */
    public Object sxMoveInReserveReq(Infos.ObjCommon objCommon,
                                                             Params.MoveInReserveReqParams moveInReserveReqParams) {
        Results.MoveInReserveReqResult moveInReserveReqResult = new Results.MoveInReserveReqResult();
        //qiandao autoMoveInReserve Requirement: add autoMoveInReserveReqResult for autoMoveInReserve Requirement
        Results.EqpAutoMoveInReserveReqResult autoMoveInReserveReqResult = new Results.EqpAutoMoveInReserveReqResult();
        //【init params】
        ObjectIdentifier equipmentID = moveInReserveReqParams.getEquipmentID();
        String portGroupID = moveInReserveReqParams.getPortGroupID();
        ObjectIdentifier controlJobID = moveInReserveReqParams.getControlJobID();
        List<Infos.StartCassette> startCassetteList = moveInReserveReqParams.getStartCassetteList();

        Validations.check(CimArrayUtils.isEmpty(startCassetteList), retCodeConfig.getInvalidInputParam());
        String APCIFControlStatus = moveInReserveReqParams.getApcIFControlStatus();

        /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/
        /*                                                                       */
        /*   Check Process                                                       */
        /*                                                                       */
        /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/
        //【step1-1】check transaction ID and eqp category combination.
        log.info("【step1-1】check transaction ID and eqp category combination.");
        equipmentMethod.equipmentCategoryVsTxIDCheckCombination(objCommon, equipmentID);

        //【step1-2】check every lot has at least one wafer with processJobExecFlag == TRUE
        log.info("【step1-2】check every lot has at least one wafer with processJobExecFlag == TRUE");
        lotMethod.lotProcessJobExecFlagValidCheck(objCommon, startCassetteList);

        //check contamination through recipe
        for (Infos.StartCassette startCassette : startCassetteList) {
            List<Infos.LotInCassette> lotInCassetteList = startCassette.getLotInCassetteList();
            for (Infos.LotInCassette lotInCassette : lotInCassetteList){
                ObjectIdentifier lotID = lotInCassette.getLotID();
                Infos.StartRecipe startRecipe = lotInCassette.getStartRecipe();
                ObjectIdentifier logicalRecipeID = startRecipe.getLogicalRecipeID();
                ObjectIdentifier machineRecipeID = startRecipe.getMachineRecipeID();
                contaminationMethod.recipeContaminationCheck(logicalRecipeID,machineRecipeID,lotID,equipmentID);
            }
        }

        //【step1-3】check scrap wafer exist in cassette
        log.info("【step1-3】check scrap wafer exist in cassette");
        List<ObjectIdentifier> cassetteIDList = new ArrayList<>();
        for (Infos.StartCassette startCassette : startCassetteList) {
            cassetteIDList.add(startCassette.getCassetteID());
        }
        List<Infos.LotWaferMap> lotWaferMaps = cassetteMethod.cassetteScrapWaferSelectDR(objCommon, cassetteIDList);
        Validations.check(!CimArrayUtils.isEmpty(lotWaferMaps), retCodeConfig.getFoundScrap());

        //【step1-4】call txAPCRTSystemInfoInq (line: 308 - 332)
        log.info("【step1-4】call txAPCRTSystemInfoInq");
        List<Infos.APCRunTimeCapabilityResponse> apcRunTimeCapabilityResponseList = apcInqService.sxAPCRunTimeCapabilityInq(objCommon, equipmentID, controlJobID, startCassetteList, true);
        int runCapaRespCount = CimArrayUtils.getSize(apcRunTimeCapabilityResponseList);
        if (runCapaRespCount > 0){
            String tmpString = APCIFControlStatus;
            APCIFControlStatus = BizConstant.SP_APC_CONTROLJOBSTATUS_CANCELED;
        }

        //【step1-5】call txAPCRcpParmChgInq (line: 338 - 361)
        log.info("【step1-5】call txAPCRcpParmChgInq");
        int retCode = 0;
        try {
            startCassetteList = apcInqService.sxAPCRecipeParameterAdjustInq(objCommon, equipmentID, startCassetteList, apcRunTimeCapabilityResponseList, true);
        } catch (ServiceException e) {
            retCode = e.getCode();
            if (!Validations.isEquals(retCodeConfigEx.getOkNoIF(), e.getCode())){
                throw e;
            }
        }
        if (retCode == 0){
            String tmpString  = APCIFControlStatus;
            APCIFControlStatus = BizConstant.SP_APC_CONTROLJOBSTATUS_CANCELED;
        }
        //【step1-6】check every lot has at least one wafer with processJobExecFlag == TRUE
        log.info("【step1-6】check every lot has at least one wafer with processJobExecFlag == TRUE");
        lotMethod.lotProcessJobExecFlagValidCheck(objCommon, startCassetteList);

        /**********************************************************************************************/
        /*                     【step2】Object Lock Process (line: 382 - 606)                  */
        /**********************************************************************************************/
        log.info("【step2】Object Lock Process");
        Inputs.ObjLockModeIn objLockModeIn = new Inputs.ObjLockModeIn();
        objLockModeIn.setObjectID(equipmentID);
        objLockModeIn.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
        objLockModeIn.setFunctionCategory(TransactionIDEnum.START_LOTS_RESERVATION_REQ.getValue());
        objLockModeIn.setUserDataUpdateFlag(false);
        Outputs.ObjLockModeOut objLockModeOut = objectMethod.objectLockModeGet(objCommon, objLockModeIn);
        Long lockMode = objLockModeOut.getLockMode();
        Inputs.ObjAdvanceLockIn strAdvancedobjecLockin = new Inputs.ObjAdvanceLockIn();
        if (!lockMode.equals(BizConstant.SP_EQP_LOCK_MODE_WRITE)){
            // Lock Equipment Main Object
            List<String> dummySeq;
            dummySeq = new ArrayList<>(0);
            strAdvancedobjecLockin.setObjectID(equipmentID);
            strAdvancedobjecLockin.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
            strAdvancedobjecLockin.setObjectType(BizConstant.SP_OBJECTLOCK_OBJECTTYPE_MAINOBJECT);
            strAdvancedobjecLockin.setLockType(objLockModeOut.getRequiredLockForMainObject());
            strAdvancedobjecLockin.setKeyList(dummySeq);

            objectLockMethod.advancedObjectLock(objCommon, strAdvancedobjecLockin);
            /*------------------------------*/
            /*   Lock Dispatcher Object     */
            /*------------------------------*/
            objectLockMethod.objectLock(objCommon, CimDispatcher.class, equipmentID);
        } else {
            /*--------------------------------------------*/
            /*                                            */
            /*      Machine Object Lock Process           */
            /*                                            */
            /*--------------------------------------------*/
            objectLockMethod.objectLock(objCommon, CimMachine.class, equipmentID);
        }
        /*--------------------------------------------*/
        /*                                            */
        /*        Port Object Lock Process            */
        /*                                            */
        /*--------------------------------------------*/
        Infos.EqpPortInfo eqpPortInfo = portMethod.portResourceAllPortsInSameGroupGet(objCommon, equipmentID, startCassetteList.get(0).getLoadPortID());
        /*---------------------------------------------------------*/
        /* Lock All Ports being in the same Port Group as ToPort   */
        /*---------------------------------------------------------*/
        List<Infos.EqpPortStatus> eqpPortStatuses = eqpPortInfo.getEqpPortStatuses();
        int lenToPort = CimArrayUtils.getSize(eqpPortStatuses);
        for (int i = 0; i < lenToPort; i++){
            objectLockMethod.objectLockForEquipmentResource(objCommon, equipmentID, eqpPortStatuses.get(i).getPortID(), BizConstant.SP_CLASSNAME_POSPORTRESOURCE);
        }
        /*--------------------------------------------*/
        /*                                            */
        /*       Cassette Object Lock Process         */
        /*                                            */
        /*--------------------------------------------*/
        List<ObjectIdentifier> lotIDs = new ArrayList<>();
        cassetteIDList = new ArrayList<>();
        int nILen = CimArrayUtils.getSize(startCassetteList);
        for (int i = 0; i < nILen; i++){
            cassetteIDList.add(startCassetteList.get(i).getCassetteID());
            List<Infos.LotInCassette> lotInCassetteList = startCassetteList.get(i).getLotInCassetteList();
            for (int j = 0; j < CimArrayUtils.getSize(lotInCassetteList); j++){
                lotIDs.add(lotInCassetteList.get(j).getLotID());
            }
        }
        /*------------------------------*/
        /*   Lock Cassette/Lot Object   */
        /*-------------------------------*/
        objectLockMethod.objectSequenceLock(objCommon, CimCassette.class, cassetteIDList);
        objectLockMethod.objectSequenceLock(objCommon, CimLot.class, lotIDs);

        // season 相关代码, by ho
        seasonMethod.checkSeasonForMoveInReserve(objCommon, startCassetteList, equipmentID);

        //check Ocap Info
        startCassetteList = ocapMethod.ocapCheckEquipmentAndSamplingAndRecipeExchange(equipmentID,startCassetteList);

        /**********************************************************************************************/
        /*                     【step3】Check Process (line: 610 - 606)                               */
        /**********************************************************************************************/
        log.info("【step3】Check Process");
        cassetteIDList.clear();  //clear the List to add new data

        /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/
        /*                                                                       */
        /*   Check Process                                                       */
        /*                                                                       */
        /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/
        //-------------------------------------------------
        //  Check scrap wafer existence in carrier
        //-------------------------------------------------
        List<Infos.StartCassette> tmpStartCassetteList = startCassetteList;
        for (Infos.StartCassette startCassette : tmpStartCassetteList) {
            cassetteIDList.add(startCassette.getCassetteID());
        }
        lotWaferMaps = cassetteMethod.cassetteScrapWaferSelectDR(objCommon, cassetteIDList);
        Validations.check(!CimArrayUtils.isEmpty(lotWaferMaps), retCodeConfig.getFoundScrap());

        moveInReserveReqParams.getStartCassetteList()
                .parallelStream()
                .forEach(startCassette ->
                        bondingGroupMethod.portWaferBondingCheck(objCommon, startCassette.getLoadPortID(), moveInReserveReqParams.getEquipmentID(), startCassette.getCassetteID())
                );

        //-------------------------------------------------
        //  Check pilotrun in eqp
        //-------------------------------------------------
        Infos.CheckPilotRunForEquipmentInfo checkPilotRunForEquipmentInfo = pilotRunMethod.checkPilotRunForEquipment(objCommon, moveInReserveReqParams.getEquipmentID(), startCassetteList);

        log.debug("check PM PilotRun");
        pilotRunMethod.checkPMRun(equipmentID, startCassetteList);

        //update pirun job status to ongoing
        if (ObjectIdentifier.isNotEmpty(checkPilotRunForEquipmentInfo.getPilotRunJobID())) {
            Params.PilotRunStatusChangeParams pilotRunStatusChangeParams = new Params.PilotRunStatusChangeParams();
            pilotRunStatusChangeParams.setEntity(checkPilotRunForEquipmentInfo.getPilotRunJobID());
            pilotRunStatusChangeParams.setEntityType(PilotRunInfo.EntityType.PiLotRunJob.getValue());
            pilotRunStatusChangeParams.setStatus(PilotRunInfo.Status.Ongoing.getValue());
            pilotRunService.sxPilotRunStatusChange(objCommon, pilotRunStatusChangeParams);
        }

        // Check Min Q-Time restrictions
        minQTimeMethod.checkIsRejectByRestriction(objCommon, startCassetteList);

        /**********************************************************************************************/
        /*【step3-2】Check Process for cassette                                                       */
        /*  The following conditions are checked by this object                                       */
        /*  - controlJobID、multiLotType、transferState、transferReserved、maxBatchSize               */
        /*  - minBatchSize、emptyCassetteCount、cassette'sloadingSequenceNomber                       */
        /*  - eqp's multiRecipeCapability and recipeParameter                                   */
        /*  - upper/Lower Limit for RecipeParameterChange                                             */
        /*  - monitorLotCount or OperationStartLotCount                                               */
        /**********************************************************************************************/
        log.info("【step3-2】Check Process for cassette");
        String operation = BizConstant.SP_OPERATION_STARTRESERVATION;
        //QianDao add MES-EAP Integration cassetteChangeFlag change cassetteCheckConditionForOperation to cassetteCheckConditionForOperationForBackSideClean
        Boolean reExchangeFlag = cassetteMethod.cassetteCheckConditionForOperationForBackSideClean(objCommon, equipmentID, portGroupID, tmpStartCassetteList, operation);
        /**********************************************************************************************/
        /* 【step3-3】Check Process for lot                                                           */
        /*  The following conditions are checked by this object                                       */
        /*     - controlJobID                                                                         */
        /*     - lot's equipmentID                                                                    */
        /*     - lotHoldStae                                                                          */
        /*     - lotProcessState                                                                      */
        /*     - lotInventoryState                                                                    */
        /*     - entityInhibition                                                                     */
        /*     - minWaferCount                                                                        */
        /*     - eqp's availability for specified lot                                           */
        /**********************************************************************************************/
        log.info("【step3-3】Check Process for lot");
        lotMethod.lotCheckConditionForOperation(objCommon, equipmentID, portGroupID, tmpStartCassetteList, operation);

        /**********************************************************************************************/
        /* 【step3-4】Check eqp port for Start Reservation                                      */
        /*  The following conditions are checked by this object                                       */
        /*   1.In-parm's portGroupID must not have controlJobID.                                      */
        /*   2. All of ports' loadMode must be CIMFW_PortRsc_Input or _InputOutput.                   */
        /*   3. All of port, which is registered as in-parm's portGroup, must be                      */
        /*        _LoadAvail or _LoadReq when eqp is online.                                    */
        /*      As exceptional case, if eqp's takeOutInTransferFlag is True,                    */
        /*      _UnloadReq is also OK for start reservation when eqp is Online.                 */
        /*   4. All of port, which is registered as in-parm's portGroup,                              */
        /*      must not have loadedCassetteID.                                                       */
        /*   5. strStartCassette[].loadPortID's portGroupID must be same                              */
        /*      as in-parm's portGroupID.                                                             */
        /*   6. strStartCassette[].loadPurposeType must be match as specified port's                  */
        /*      loadPutposeType.                                                                      */
        /*   7. strStartCassette[].loadSequenceNumber must be same as specified port's                */
        /*      loadSequenceNumber.                                                                   */
        /**********************************************************************************************/
        log.info("【step3-4】Check eqp port for Start Reservatio");
        equipmentMethod.equipmentPortStateCheckForStartReservation(objCommon, equipmentID, portGroupID, startCassetteList,moveInReserveReqParams.getAutoMoveInReserveFlag());
        /**********************************************************************************************/
        /* 【step3-5】check process for flowBatch                                                     */
        /*  The following conditions are checked by this object                                       */
        /*   1. whether in-parm's eqp has reserved flowBatchID or not                           */
        /*      fill  -> all of flowBatch member and in-parm's lot must be                            */
        /*               same perfectly.                                                              */
        /*      blank -> no check                                                                     */
        /*                                                                                            */
        /*   2. whether lot is in flowBatch section or not                                            */
        /*      in    -> lot must have flowBatchID, and flowBatch must have                           */
        /*               reserved equipmentID.                                                        */
        /*               if lot is on target operation, flowBatch's reserved                          */
        /*               equipmentID and in-parm's equipmentID must be same.                          */
        /*      out   -> no check                                                                     */
        /**********************************************************************************************/
        log.info("【step3-5】check process for flowbatch");
        Inputs.ObjEquipmentLotCheckFlowBatchConditionForOperationStartIn operationStartIn = new Inputs
                .ObjEquipmentLotCheckFlowBatchConditionForOperationStartIn();
        operationStartIn.setEquipmentID(equipmentID);
        operationStartIn.setPortGroupID(portGroupID);
        operationStartIn.setStartCassetteList(startCassetteList);
        // equipment_lot_CheckFlowBatchConditionForOpeStart__090
        equipmentMethod.equipmentLotCheckFlowBatchConditionForOpeStart(objCommon, operationStartIn);

        /**********************************************************************************************/
        /* 【step3-6】check process for process durable                                               */
        /*   The following conditions are checked by this object                                      */
        /*                                                                                            */
        /*   1. Whether eqp requires process durable or not                                     */
        /*      If no-need, return OK;                                                                */
        /*                                                                                            */
        /*   2. At least one of reticle / fixture for each reticleGroup /                             */
        /*      fixtureGroup is in the eqp or not.                                              */
        /*      Even if required reticle is in the eqp, its status must                         */
        /*      be _Available or _InUse.                                                              */
        /**********************************************************************************************/
        log.info("【step3-6】check process for process durable ");
        try {
            equipmentMethod.equipmentProcessDurableRequiredFlagGet(objCommon, equipmentID);
        } catch (ServiceException e) {
            if (Validations.isEquals(retCodeConfig.getEquipmentProcessDurableReticleRequired(), e.getCode())
                    || Validations.isEquals(retCodeConfig.getEquipmentProcessDurableFixtRequired(), e.getCode())) {
                int nIMax = CimArrayUtils.getSize(startCassetteList);
                for (int i = 0; i < nIMax; i++) {
                    Infos.StartCassette startCassette = startCassetteList.get(i);
                    if (CimStringUtils.equals(BizConstant.SP_LOADPURPOSETYPE_EMPTYCASSETTE, startCassette.getLoadPurposeType())) {
                        continue;
                    }
                    List<Infos.LotInCassette> lotInCassetteList = startCassette.getLotInCassetteList();
                    int nJMax = CimArrayUtils.getSize(lotInCassetteList);
                    for (int j = 0; j < nJMax; j++) {
                        Infos.LotInCassette lotInCassetteObj = lotInCassetteList.get(j);
                        if (!lotInCassetteObj.getMoveInFlag()) {
                            continue;
                        }
                        //【step3-6-2】check processDurableConditionForOpeStart
                        log.info("【step3-6-2】check processDurableConditionForOpeStart");
                        Infos.StartRecipe startRecipe = lotInCassetteObj.getStartRecipe();
                        Outputs.ObjProcessDurableCheckConditionForOperationStartOut objProcessDurableCheckConditionForOperationStartOut = processMethod.processDurableCheckConditionForOpeStart(objCommon, equipmentID, startRecipe.getLogicalRecipeID(), startRecipe.getMachineRecipeID(), lotInCassetteObj.getLotID());
                        //check reticle usage by RTMS
                        List<String> reticleList = new ArrayList<>();
                        // fix,a null pointexcception ,if reticle is null
                        Optional.ofNullable(objProcessDurableCheckConditionForOperationStartOut.getStartReticleList())
                                .ifPresent(reticles->reticles.stream().forEach(reticle->{
                                    reticleList.add(reticle.getReticleID().getValue());
                                }));
                        // if reticle is null , do not call RTMS
                        if (StandardProperties.OM_RTMS_CHECK_ACTIVE.isTrue()){
                            if (CimArrayUtils.isNotEmpty(reticleList)) {
                                reticleMethod.reticleUsageCheckByRTMS(objCommon,BizConstant.RTMS_RETICLE_CHECK_ACTION_MOVE_IN_RESERVE,
                                        reticleList,lotInCassetteObj.getLotID(),equipmentID);
                            }
                        }
                        //【step3-6-3】set available reticles / fixtures
                        log.info("【step3-6-3】set available reticles / fixtures");
                        startRecipe.setStartReticleList(objProcessDurableCheckConditionForOperationStartOut.getStartReticleList());
                        startRecipe.setStartFixtureList(objProcessDurableCheckConditionForOperationStartOut.getStartFixtureList());
                    }
                }
            } else if (Validations.isEquals(retCodeConfig.getEquipmentProcessDurableNotRequired(), e.getCode())) {

            } else {
                throw e;
            }
        }

        /*------------------------------------------------------------------*/
        /*   machine recipe convert to chamber level recipe                 */
        /*------------------------------------------------------------------*/
        ChamberLevelRecipeReserveParam chamberLevelRecipeReserveParam = new ChamberLevelRecipeReserveParam();
        chamberLevelRecipeReserveParam.setEquipmentId(moveInReserveReqParams.getEquipmentID());
        chamberLevelRecipeReserveParam.setStartCassettes(moveInReserveReqParams.getStartCassetteList());
        startCassetteList = chamberLevelRecipeMethod.chamberLevelRecipeMoveQueryRpt(objCommon,
                chamberLevelRecipeReserveParam);
        moveInReserveReqParams.setStartCassetteList(startCassetteList);


        /**********************************************************************************************/
        /* 【step3-7】Confirmation for Uploaded Recipe Body and Eqp's Recipe Body                     */
        /*   When the following conditions are all matched, recipe body                               */
        /*   confirmation request is sent to TCS.                                                     */
        /*                                                                                            */
        /*   1. eqp's onlineMode is Online                                                      */
        /*   2. eqp's recipe body manage flag is TRUE.                                          */
        /*   3. Machine recipe's recipe body confirm flag is TRUE.                                    */
        /*
        /*   Force Down Load Flag  Recipe Body Confirm Flag  Conditional Down Load Flag
        /*           Yes                      No                       No                      -> Download it without confirmation.
        /*           No                       Yes                      No                      -> Confirm only.
        /*           No                       Yes                      Yes                     -> If confirmation is NG, download it.
        /*           No                       No                       No                      -> No action.
        /**********************************************************************************************/
        log.info("【step3-7】Confirmation for Uploaded Recipe Body and Eqp's Recipe Body");
        //【step3-7-1】get Machine Recipe List for Recipe Body Management
        log.info("【step3-7-1】get Machine Recipe List for Recipe Body Management");
        Inputs.ObjEquipmentRecipeGetListForRecipeBodyManagementIn in = new Inputs.ObjEquipmentRecipeGetListForRecipeBodyManagementIn();
        in.setEquipmentID(equipmentID);
        in.setStartCassetteList(startCassetteList);
        List<Infos.RecipeBodyManagement> recipeBodyManagementList = machineRecipeMethod.machineRecipeGetListForRecipeBodyManagement(objCommon, in);
        int targetRecipeSize = CimArrayUtils.getSize(recipeBodyManagementList);
        if (0 == targetRecipeSize) {
            log.debug("Recipe for Recipe Body Management does not exist.");
        }
        for (int i = 0; i < targetRecipeSize; i++) {
            Infos.RecipeBodyManagement recipeBodyManagement = recipeBodyManagementList.get(i);
            // force down load
            boolean downLoadFlag = false;
            if (recipeBodyManagement.getForceDownLoadFlag()){
                downLoadFlag = true;
            } else {
                if (recipeBodyManagement.getRecipeBodyConfirmFlag()){
                    //---------------------
                    // Recipe Confirmation
                    //---------------------
                    Params.RecipeCompareReqParams recipeCompareReqParams = new Params.RecipeCompareReqParams();
                    recipeCompareReqParams.setEquipmentID(equipmentID);
                    recipeCompareReqParams.setMachineRecipeID(recipeBodyManagement.getMachineRecipeId());
                    recipeCompareReqParams.setPhysicalRecipeID(recipeBodyManagement.getPhysicalRecipeId());
                    recipeCompareReqParams.setFileLocation(recipeBodyManagement.getFileLocation());
                    recipeCompareReqParams.setFileName(recipeBodyManagement.getFileName());
                    recipeCompareReqParams.setFormatFlag(recipeBodyManagement.getFormatFlag());
                    try {
                        recipeService.sxRecipeCompareReq(objCommon, recipeCompareReqParams);
                    } catch (ServiceException e) {
                        if (!Validations.isEquals(retCodeConfigEx.getTcsMMTapPPConfirmError(), e.getCode())){
                            throw e;
                        } else {
                            if (recipeBodyManagement.getConditionalDownLoadFlag()){
                                //--------------------------
                                // Conditional Down Load
                                //--------------------------
                                downLoadFlag = true;
                            } else {
                                throw new ServiceException(new OmCode(retCodeConfigEx.getRecipeConfirmError(), recipeBodyManagement.getMachineRecipeId().getValue()));
                            }
                        }
                    }
                }
            }
            if (downLoadFlag) {
                //---------------------
                // Recipe Down Load
                //---------------------
                Params.RecipeDownloadReqParams recipeDownloadReqParams = new Params.RecipeDownloadReqParams();
                recipeDownloadReqParams.setEquipmentID(equipmentID);
                recipeDownloadReqParams.setMachineRecipeID(recipeBodyManagement.getMachineRecipeId());
                recipeDownloadReqParams.setPhysicalRecipeID(recipeBodyManagement.getPhysicalRecipeId());
                recipeDownloadReqParams.setFileLocation(recipeBodyManagement.getFileLocation());
                recipeDownloadReqParams.setFileName(recipeBodyManagement.getFileName());
                recipeDownloadReqParams.setFormatFlag(recipeBodyManagement.getFormatFlag());
                recipeService.sxRecipeDownloadReq(objCommon, recipeDownloadReqParams);
            }
        }

        /**********************************************************************************************/
        /* 【step3-8】Check Category for Copper/Non Copper                                            */
        /*   It is checked in the following method whether it is the condition                        */
        /*   that lot of the object is made of OpeStart.                                              */
        /*                                                                                            */
        /*   1. It is checked whether CassetteCategory of RequiredCassetteCategory                    */
        /*      of PosLot and PosCassette is the same.                                                */
        /*                                                                                            */
        /*   2. It is checked whether CassetteCategoryCapability of CassetteCategory                  */
        /*      of PosCassette and PosPortResource is the same.                                       */
        /*                                                                                            */
        /*   3. It is proper condition if CassetteCategoryCapability is the same                      */
        /*      as RequiredCassetteCategory and CassetteCategory.                                     */
        /**********************************************************************************************/
        log.info("【step3-8】Check Category for Copper/Non Copper");
        int startCassetteSize = CimArrayUtils.getSize(startCassetteList);
        ObjectIdentifier dummyLotID = new ObjectIdentifier();
        for (int i = 0; i < startCassetteSize; i++) {
            Infos.StartCassette startCassetteObj = startCassetteList.get(i);
            if (CimStringUtils.equals(BizConstant.SP_LOADPURPOSETYPE_EMPTYCASSETTE, startCassetteObj.getLoadPurposeType())) {
                lotMethod.lotCassetteCategoryCheckForContaminationControl(objCommon, dummyLotID, startCassetteObj.getCassetteID(), equipmentID, startCassetteObj.getLoadPortID());
            } else {
                int lotInCassetteSize = CimArrayUtils.getSize(startCassetteObj.getLotInCassetteList());
                for (int j = 0; j < lotInCassetteSize; j++) {
                    Infos.LotInCassette lotInCassetteObj = startCassetteObj.getLotInCassetteList().get(j);
                    if (!lotInCassetteObj.getMoveInFlag()) {
                        continue;
                    }
                    lotMethod.lotCassetteCategoryCheckForContaminationControl(objCommon, lotInCassetteObj.getLotID(),
                            startCassetteObj.getCassetteID(), equipmentID, startCassetteObj.getLoadPortID());

                }
            }
        }

        //【step3-9】check cassette category for next operation of empty cassette.
        log.info("【step3-9】check cassette category for next operation of empty cassette.");
        cassetteMethod.emptyCassetteCheckCategoryForOperation(objCommon, startCassetteList);
        //check contamination level and pr flag
        List<ObjectIdentifier> moveInLotIds = startCassetteList.parallelStream()
                .flatMap(startCassette -> startCassette.getLotInCassetteList().parallelStream()
                        .filter(Infos.LotInCassette::getMoveInFlag).map(Infos.LotInCassette::getLotID))
                .collect(Collectors.toList());
        contaminationMethod.lotCheckContaminationLevelAndPrFlagStepIn(moveInLotIds, moveInReserveReqParams.getEquipmentID(),"");
        List<Params.ContaminationAllLotCheckParams> allLots = new ArrayList<>();
        for (Infos.StartCassette tempStartCassettes : startCassetteList){
            List<Infos.LotInCassette> lotInCassetteList = tempStartCassettes.getLotInCassetteList();
            for (Infos.LotInCassette lotInCassette : lotInCassetteList){
                Params.ContaminationAllLotCheckParams checkParams = new Params.ContaminationAllLotCheckParams();
                allLots.add(checkParams);
                checkParams.setLotID(lotInCassette.getLotID());
                checkParams.setMoveInFlag(lotInCassette.getMoveInFlag());
            }
        }
        contaminationMethod.contaminationLvlCheckAmongLots(allLots);
        //check wafer bonding usage type
        contaminationMethod.normalMoveInWaferBondingUsageCheck(objCommon, moveInLotIds);

        //【step3-10】check condition for slm
        log.info("【step3-10】check condition for slm");
        Inputs.ObjSlmCheckConditionForOperationIn operationIn = new Inputs.ObjSlmCheckConditionForOperationIn();
        operationIn.setEquipmentID(equipmentID);
        operationIn.setPortGroupID(portGroupID);
        operationIn.setControlJobID(controlJobID);
        operationIn.setStartCassetteList(startCassetteList);
        operationIn.setOperation(BizConstant.SP_OPERATION_STARTRESERVATION);
        slmMethod.slmCheckConditionForOperation(objCommon, equipmentID, portGroupID, controlJobID, startCassetteList,null, BizConstant.SP_OPERATION_STARTRESERVATION);
        /**********************************************************************************************/
        /* 【step4】Main Process                                                                      */
        /**********************************************************************************************/
        log.info("【step4】Main Process");
        //【step4-1】Change cassette's dispatch State to TRUE
        log.info("【step4-1】Change cassette's dispatch State to TRUE");
        for (int i = 0; i < startCassetteSize; i++) {
            Infos.StartCassette startCassetteObj = startCassetteList.get(i);
            cassetteMethod.cassetteDispatchStateChange(objCommon, startCassetteObj.getCassetteID(), true);
        }
        /**********************************************************************************************/
        /* 【step4-2】create control job and assign to each cassette /Lots            */
        /*   - Create new controlJob                                                                  */
        /*   - Set created controlJobID to each cassettes / lots                                      */
        /*   - Set created controlJobID to eqp                                                  */
        /**********************************************************************************************/
        //【bear】call txCJStatusChangeReq - (line:1217 - 1234)
        log.info("【bear】call txCJStatusChangeReq - (line:1217 - 1234)");
        String dummyControlJobID = "";
        Infos.ControlJobCreateRequest controlJobCreateRequest = new Infos.ControlJobCreateRequest();
        controlJobCreateRequest.setEquipmentID(equipmentID);
        controlJobCreateRequest.setPortGroup(portGroupID);
        controlJobCreateRequest.setStartCassetteList(startCassetteList);
        Params.CJStatusChangeReqParams cjStatusChangeReqParams = new Params.CJStatusChangeReqParams();
        cjStatusChangeReqParams.setControlJobCreateRequest(controlJobCreateRequest);
        cjStatusChangeReqParams.setControlJobID(ObjectIdentifier.build(dummyControlJobID, null));
        cjStatusChangeReqParams.setControlJobAction(BizConstant.SP_CONTROLJOBACTION_TYPE_CREATE);
        Results.CJStatusChangeReqResult cjStatusChangeReqResult = controlJobProcessJobService.sxCJStatusChangeReqService(objCommon, cjStatusChangeReqParams);

        if (log.isDebugEnabled()) {
            log.debug("create sort job");
        }
        String eqpCategory = equipmentMethod.equipmentCategoryGet(objCommon, equipmentID);
        String actionCode = "";
        if (CimStringUtils.equals(BizConstant.SP_MC_CATEGORY_WAFERSORTER, eqpCategory)) {
            actionCode = sortNewService.sxSJCreateByMoveInReserve(objCommon, moveInReserveReqParams, cjStatusChangeReqResult.getControlJobID());
        }
        if (log.isInfoEnabled()) {
            log.debug("actionCode : "+ actionCode);
        }
        //check capability
        equipmentMethod.capabilityCheck(objCommon,moveInLotIds,moveInReserveReqParams.getEquipmentID());
        //【step4-3】set start reserved control job into eqp

        log.info("【step4-3】set start reserved control job into eqp");
        equipmentMethod.equipmentReservedControlJobIDSet(objCommon, equipmentID, cjStatusChangeReqResult.getControlJobID());
        /**********************************************************************************************/
        /* 【step4-4】get Start Information for each cassette / lot                                   */
        /*   - strStartCassette information is not filled perfectly. By this object function,         */
        /*     it will be filled.                                                                     */
        /**********************************************************************************************/
        Outputs.ObjProcessStartReserveInformationGetOut informationGetOut = processMethod.processStartReserveInformationGet(objCommon, startCassetteList, equipmentID, false);


        //【step4-5】apply DOC information for StartCassette.
        log.info("【step4-5】apply DOC information for StartCassette.");
        int tempFPCAdoptFlag = StandardProperties.OM_DOC_ENABLE_FLAG.getIntValue();
        if (1 == tempFPCAdoptFlag) {
            log.debug("DOC Adopt Flag is ON. Now apply FPCInfo.");
            List<Infos.StartCassette> fpcStartCassetteInfoExchangeOut = fpcMethod.fpcStartCassetteInfoExchange(objCommon,
                    BizConstant.SP_FPC_EXCHANGETYPE_STARTRESERVEREQ, equipmentID, informationGetOut.getStartCassetteList());
            informationGetOut.setStartCassetteList(fpcStartCassetteInfoExchangeOut);
        } else {
            log.debug("DOC Adopt Flag is OFF. Now only check whiteFlag.");
            //【step4-6】check DOC process condition of StartCassette.
            log.info("【step4-6】check DOC process condition of StartCassette.");
            fpcMethod.fpcStartCassetteProcessConditionCheck(objCommon, equipmentID, informationGetOut.getStartCassetteList(), false, true);
        }
        /**********************************************************************************************/
        /* 【step4-7】Set Start Reservation Info to Each Lots' PO                                     */
        /*   - Set created controlJobID into each cassette.                                           */
        /*   - Set created controlJobID into each lot.                                                */
        /*   - Set control job info (StartRecipe, DCDefs,DCSpecs, Parameters, ...)                    */
        /*     into each lot's current PO.                                                            */
        /**********************************************************************************************/
        log.info("【step4-7】Set Start Reservation Info to Each Lots' PO ");
        processMethod.processStartReserveInformationSet(objCommon, equipmentID, portGroupID,
                cjStatusChangeReqResult.getControlJobID(),   //【bear】in fact,controlJobID = strCJStatusChangeReqResult.controlJobID; not null
                informationGetOut.getStartCassetteList(), false);

        //-----------------------------------------------------------//
        //  Wafer Stacking Operation                                 //
        //    If Equipment Category is SP_Mc_Category_WaferBonding,  //
        //    update Bonding Group Information                       //
        //-----------------------------------------------------------//
        //【step4-8】lot_bondingGroup_UpdateByOperation
        log.info("【step4-8】lot_bondingGroup_UpdateByOperation");
        lotMethod.lotBondingGroupUpdateByOperation(objCommon, equipmentID, cjStatusChangeReqResult.getControlJobID(), informationGetOut.getStartCassetteList(), BizConstant.SP_OPERATION_STARTRESERVATION);

        /*------------------------------------------------------*/
        /* 【step4-9】  Send TxMoveInReserveReq to TCS   */
        /*------------------------------------------------------*/

//        Inputs.SendMoveInReserveReqIn sendMoveInReserveReqIn = new Inputs.SendMoveInReserveReqIn();
//        sendMoveInReserveReqIn.setObjCommonIn(objCommon);
//        sendMoveInReserveReqIn.setEquipmentID(equipmentID);
//        sendMoveInReserveReqIn.setPortGroupID(portGroupID);
//        sendMoveInReserveReqIn.setControlJobID(cjStatusChangeReqResult.getControlJobID());
//        sendMoveInReserveReqIn.setStrStartCassette(informationGetOut.getStartCassetteList());
//        Outputs.SendMoveInReserveReqOut sendMoveInReserveReq = (Outputs.SendMoveInReserveReqOut)tcsMethod.sendTCSReq(TCSReqEnum.sendMoveInReserveReq,sendMoveInReserveReqIn);
        //qiandao autoMoveInReserve Requirement: add autoMoveInReserveReqFlag
        log.debug("moveInReserve autoMoveInReserveFlag: {}",moveInReserveReqParams.getAutoMoveInReserveFlag());
        if (CimBooleanUtils.isFalse(moveInReserveReqParams.getAutoMoveInReserveFlag())){
            if (!CimStringUtils.equals(BizConstant.SP_MC_CATEGORY_WAFERSORTER, eqpCategory)) {
                String tmpSleepTimeValue = StandardProperties.OM_EAP_CONNECT_SLEEP_TIME.getValue();
                String tmpRetryCountValue = StandardProperties.OM_EAP_CONNECT_RETRY_COUNT.getValue();
                Long sleepTimeValue = 0L;
                Long retryCountValue = 0L;

                if (0 == CimStringUtils.length(tmpSleepTimeValue)) {
                    sleepTimeValue = BizConstant.SP_DEFAULT_SLEEP_TIME_TCS;
                } else {
                    sleepTimeValue = CimNumberUtils.longValue(tmpSleepTimeValue);
                }

                if (0 == CimStringUtils.length(tmpRetryCountValue)) {
                    retryCountValue = BizConstant.SP_DEFAULT_RETRY_COUNT_TCS;
                } else {
                    retryCountValue = CimNumberUtils.longValue(tmpRetryCountValue);
                }

                //QianDao add MES-EAP Integration cassetteChangeFlag
                for (int retryNum = 0; retryNum < (retryCountValue + 1); retryNum++) {
                    log.info("{} {}", "loop to retryCountValue + 1", retryNum);
                    /*--------------------------*/
                    /*    Send Request to EAP   */
                    /*--------------------------*/
                    IEAPRemoteManager eapRemoteManager = eapMethod.eapRemoteManager(objCommon,moveInReserveReqParams.getUser(),moveInReserveReqParams.getEquipmentID(),null,true);
                    if (null == eapRemoteManager) {
                        log.info("MES not configure EAP host");
                        break;
                    }
                    Params.MoveInReserveReqParams sendToEapParams = new Params.MoveInReserveReqParams();
                    sendToEapParams.setUser(moveInReserveReqParams.getUser());
                    sendToEapParams.setEquipmentID(equipmentID);
                    sendToEapParams.setControlJobID(cjStatusChangeReqResult.getControlJobID());
                    sendToEapParams.setPortGroupID(moveInReserveReqParams.getPortGroupID());
                    // 此处处理如果拥有chamber level recipe ， 需要发送给eap [ESEC Service]
                    List<Infos.StartCassette> startCassetteListSend = informationGetOut.getStartCassetteList();
                    startCassetteListSend.parallelStream().forEach(startCassette -> {
                        startCassette.getLotInCassetteList().forEach(lotInCassette -> {
                            Infos.StartRecipe startRecipe = lotInCassette.getStartRecipe();
                            if (Objects.nonNull(startRecipe) &&
                                    ObjectIdentifier.isNotEmpty(startRecipe.getChamberLevelRecipeID())) {
                                startRecipe.setPhysicalRecipeID(
                                        ObjectIdentifier.fetchValue(startRecipe.getChamberLevelRecipeID()));
                            }
                        });
                    });
                    sendToEapParams.setStartCassetteList(startCassetteListSend);
                    //QianDao add MES-EAP Integration cassetteChangeFlag
                    sendToEapParams.setCassetteChangeFlag(reExchangeFlag);
                    try {
                        Object moveInReserveReqEqpOut = eapRemoteManager.sendMoveInReserveReq(sendToEapParams);
                        log.info("Now EAP subSystem is alive!! Go ahead");
                        break;
                    } catch (ServiceException ex) {
                        if (Validations.isEquals(ex.getCode(), retCodeConfig.getTcsNoResponse())) {
                            log.info("{} {}", "EAP subsystem has return NO_RESPONSE!! just retry now!!  now count...", retryNum);
                            log.info("{} {}", "now sleeping... ", sleepTimeValue);
                            if (retryNum != retryCountValue){
                                try {
                                    Thread.sleep(sleepTimeValue);
                                    continue;
                                } catch (InterruptedException e) {
                                    ex.addSuppressed(e);
                                    Thread.currentThread().interrupt();
                                    throw ex;
                                }
                            }else {
                                Validations.check(true,retCodeConfig.getTcsNoResponse());
                            }
                        } else {
                            Validations.check(true,new OmCode(ex.getCode(),ex.getMessage()));
                        }
                    }
                }
            }
        }else {
            //qiandao autoMoveInReserve Requirement: when autoMoveInReserveReq return autoMoveInReserveReqResult
            autoMoveInReserveReqResult.setUser(moveInReserveReqParams.getUser());
            autoMoveInReserveReqResult.setEquipmentID(equipmentID);
            autoMoveInReserveReqResult.setControlJobID(cjStatusChangeReqResult.getControlJobID());
            autoMoveInReserveReqResult.setPortGroupID(moveInReserveReqParams.getPortGroupID());
            autoMoveInReserveReqResult.setStartCassetteList(informationGetOut.getStartCassetteList());
            autoMoveInReserveReqResult.setCassetteChangeFlag(reExchangeFlag);
        }

        /*-------------------------------------------------*/
        /*   call APCRuntimeCapability_RegistDR            */
        /*-------------------------------------------------*/
        apcMethod.apcRuntimeCapabilityRegistDR(objCommon, cjStatusChangeReqResult.getControlJobID(), apcRunTimeCapabilityResponseList);

        /*-------------------------------------------------*/
        /*   call cassette_APCInformation_GetDR            */
        /*-------------------------------------------------*/
        List<Infos.ApcBaseCassette> apcBaseCassettes = null;
        try {
            apcBaseCassettes = cassetteMethod.cassetteAPCInformationGetDR(objCommon, equipmentID, startCassetteList);
        } catch (ServiceException e) {
            if (Validations.isEquals(retCodeConfig.getSystemError(), e.getCode())){
                throw e;
            }
        }
        /*-------------------------------------------------*/
        /*   call APCMgr_SendControlJobInformationDR       */
        /*-------------------------------------------------*/
        try {
            apcMethod.APCMgrSendControlJobInformationDR(objCommon, equipmentID, cjStatusChangeReqResult.getControlJobID(), BizConstant.SP_APC_CONTROLJOBSTATUS_CREATED, apcBaseCassettes);
        } catch (ServiceException e){
            retCode = e.getCode();
            if (!Validations.isEquals(retCodeConfigEx.getOkNoIF(), e.getCode())){
                throw e;
            }
        }
        if (retCode == 0){
            String tmpString = APCIFControlStatus;
            APCIFControlStatus = BizConstant.SP_APC_CONTROLJOBSTATUS_CANCELED;
        }
        moveInReserveReqResult.setControlJobID(cjStatusChangeReqResult.getControlJobID());

        //qiandao autoMoveInReserve Requirement: when autoMoveInReserveFlag is false return moveInReserveReqResult else retrun autoMoveInReserveReqResults
        if (CimBooleanUtils.isFalse(moveInReserveReqParams.getAutoMoveInReserveFlag())){
            return moveInReserveReqResult;
        }else {
            return autoMoveInReserveReqResult;
        }
    }

    @Override
    public Results.EqpFullAutoConfigChgReqResult sxEqpFullAutoConfigChgReq(Infos.ObjCommon objCommon, Params.EqpFullAutoConfigChgReqInParm eqpFullAutoConfigChgReqInParm) {
        Results.EqpFullAutoConfigChgReqResult eqpFullAutoConfigChgReqResult = new Results.EqpFullAutoConfigChgReqResult();
        List<Results.EqpAuto3SettingUpdateResult> eqpAuto3SettingUpdateResults = new ArrayList<>();
        List<Infos.EqpAuto3SettingInfo> eqpAuto3SettingInfos = eqpFullAutoConfigChgReqInParm.getEqpAuto3SettingInfo();
        for (Infos.EqpAuto3SettingInfo eqpAuto3SettingInfo:eqpAuto3SettingInfos){
            Results.EqpAuto3SettingUpdateResult eqpAuto3SettingUpdateResult = new Results.EqpAuto3SettingUpdateResult();
            ObjectIdentifier equipmentID = eqpAuto3SettingInfo.getEqpID();
            String updateMode = eqpFullAutoConfigChgReqInParm.getUpdateMode();
            String carrierTransferRequestEvent = eqpAuto3SettingInfo.getCarrierTransferRequestEvent();
            String watchdogName = eqpAuto3SettingInfo.getWatchdogName();
            try {
                equipmentMethod.equipmentGetTypeDR(objCommon, equipmentID);
            }catch (ServiceException e){
                eqpAuto3SettingUpdateResult.setEqpID(equipmentID);
                eqpAuto3SettingUpdateResult.setStrResult(new RetCode(e.getTransactionID(), new OmCode(e.getCode(), e.getMessage()), null));
                eqpAuto3SettingUpdateResults.add(eqpAuto3SettingUpdateResult);
                break ;
            }
            //----------------------------------------------------------------
            //  Pre Process
            //----------------------------------------------------------------
            //----------------------------------------------------------------
            //  equipment_auto3DispatchSetting_GetDR
            //----------------------------------------------------------------
            List<ObjectIdentifier> equipmentIDs = new ArrayList<>();
            equipmentIDs.add(equipmentID);
            List<Infos.EqpAuto3SettingInfo> auto3DispatchSettingListGetDR = equipmentMethod.equipmentAuto3DispatchSettingListGetDR(objCommon, equipmentIDs);
            if (CimStringUtils.equals(updateMode, BizConstant.SP_EQPAUTO3SETTING_UPDATEMODE_UPDATE)){
                Boolean existingEventFlag = false;
                for (Infos.EqpAuto3SettingInfo auto3SettingInfo : auto3DispatchSettingListGetDR){
                    if (CimStringUtils.equals(auto3SettingInfo.getCarrierTransferRequestEvent(),carrierTransferRequestEvent)){
                        log.info("Existing auto3Setting");
                        existingEventFlag=true;
                        break;
                    }else {
                        log.info("Non-Update auto3Setting");
                    }
                }
                if (!existingEventFlag){
                    log.info("existingEventFlag = FALSE");
                    updateMode = BizConstant.SP_EQPAUTO3SETTING_UPDATEMODE_INSERT;
                }else {
                    log.info("existingEventFlag = TRUE");
                }
            }else if (CimStringUtils.equals(updateMode,BizConstant.SP_EQPAUTO3SETTING_UPDATEMODE_DELETE)){

            }else {
                log.info("updateMode is invalid."+updateMode);
                eqpAuto3SettingUpdateResult.setEqpID(equipmentID);
                eqpAuto3SettingUpdateResult.setStrResult(new RetCode(TransactionIDEnum.EQP_AUTO_3SETTING_UPDATE_REQ.getValue(), retCodeConfig.getInvalidInputParam(), null));
                eqpAuto3SettingUpdateResults.add(eqpAuto3SettingUpdateResult);
            }
            //----------------------------------------------------------------------
            //  Main Process
            //----------------------------------------------------------------------
            //----------------------------------------------------------------------
            //  equipment_auto3DispatchSetting_UpdateDR
            //----------------------------------------------------------------------
            log.info("call equipment_auto3DispatchSetting_UpdateDR()");
            try {
                equipmentMethod.equipmentAuto3DispatchSettingUpdateDR(objCommon,eqpAuto3SettingInfo,updateMode);
                eqpAuto3SettingUpdateResult.setEqpID(equipmentID);
                eqpAuto3SettingUpdateResult.setStrResult(new RetCode(TransactionIDEnum.EQP_AUTO_3SETTING_UPDATE_REQ.getValue(), retCodeConfig.getSucc(), null));
                eqpAuto3SettingUpdateResults.add(eqpAuto3SettingUpdateResult);
            }catch (ServiceException e){
                eqpAuto3SettingUpdateResult.setEqpID(equipmentID);
                eqpAuto3SettingUpdateResult.setStrResult(new RetCode(e.getTransactionID(), new OmCode(e.getCode(), e.getMessage()), null));
                eqpAuto3SettingUpdateResults.add(eqpAuto3SettingUpdateResult);
            }
        }
        eqpFullAutoConfigChgReqResult.setEqpAuto3SettingUpdateResults(eqpAuto3SettingUpdateResults);
        return eqpFullAutoConfigChgReqResult;
    }

    @Override
    public Results.EqpAutoMoveInReserveReqResult sxEqpAutoMoveInReserveReq(Infos.ObjCommon objCommon, Params.EqpAutoMoveInReserveReqParams params) {
        ObjectIdentifier carrierID = params.getCarrierID();
        ObjectIdentifier equipmentID = params.getEquipmentID();
        ObjectIdentifier portID = params.getPortID();

        //step1 - equipmentAutoMoveInReserveConditionCheck
        log.debug("step1 - equipmentAutoMoveInReserveConditionCheck");
        Infos.EqpPortStatus eqpPortStatus = equipmentMethod.equipmentAutoMoveInReserveConditionCheck(objCommon, carrierID, equipmentID, portID);

        //step2- LotListByCarrierInq
        log.debug("step2- sxLotListByCarrierInq");
        Results.LotListByCarrierInqResult lotListByCarrierInqResult = lotInqService.sxLotListByCarrierInq(objCommon, carrierID);

        //step3 - check if none lot in carrier and then return error
        log.debug("step3 - check if none lot in carrier and then return error");
        Infos.LotListInCassetteInfo lotListInCassetteInfo = lotListByCarrierInqResult.getLotListInCassetteInfo();
        String multiLotType = lotListInCassetteInfo.getMultiLotType();
        log.debug("carrier multiLotType: {}",multiLotType);
        List<ObjectIdentifier> lotIDList = lotListInCassetteInfo.getLotIDList();
        List<Infos.WaferMapInCassetteInfo> waferMapInCassetteInfoList = lotListByCarrierInqResult.getWaferMapInCassetteInfoList();
        Validations.check(CimArrayUtils.isEmpty(lotIDList)
                || CimArrayUtils.isEmpty(waferMapInCassetteInfoList),
                retCodeConfig.getCastIsEmpty(),ObjectIdentifier.fetchValue(carrierID));

        //step4 - LotInfoInq
        log.debug("step4 - LotInfoInq");
        Params.LotInfoInqParams lotInfoInqParams = new Params.LotInfoInqParams();
        Infos.LotInfoInqFlag lotInfoInqFlag = new Infos.LotInfoInqFlag();
        lotInfoInqFlag.setLotBasicInfoFlag(true);
        lotInfoInqFlag.setLotOperationInfoFlag(true);
        lotInfoInqFlag.setLotProductInfoFlag(true);
        lotInfoInqFlag.setLotListInCassetteInfoFlag(true);
        lotInfoInqFlag.setLotWaferMapInCassetteInfoFlag(true);
        lotInfoInqFlag.setLotWaferAttributesFlag(true);
        lotInfoInqFlag.setLotControlUseInfoFlag(false);
        lotInfoInqFlag.setLotFlowBatchInfoFlag(false);
        lotInfoInqFlag.setLotNoteFlagInfoFlag(false);
        lotInfoInqFlag.setLotOrderInfoFlag(false);
        lotInfoInqFlag.setLotControlJobInfoFlag(false);
        lotInfoInqFlag.setLotRecipeInfoFlag(false);
        lotInfoInqFlag.setLotLocationInfoFlag(false);
        lotInfoInqFlag.setLotWipOperationInfoFlag(false);
        lotInfoInqFlag.setLotBackupInfoFlag(false);
        lotInfoInqParams.setLotInfoInqFlag(lotInfoInqFlag);
        lotInfoInqParams.setLotIDs(lotIDList);
        Results.LotInfoInqResult lotInfoInqResult = lotInqService.sxLotInfoInq(objCommon, lotInfoInqParams);

        //step5 - equipmentAutoMoveInReserveInfoGet
        log.debug("step5 - equipmentAutoMoveInReserveInfoGet");
        Results.LotsMoveInReserveInfoInqResult equipmentAutoMoveInReserveInfoGetResult = equipmentMethod.equipmentAutoMoveInReserveInfoGet(objCommon,lotInfoInqResult,equipmentID);

        //step6 - MoveInReserveInfoInq
        log.debug("step6 - MoveInReserveInfoInq");
        Params.LotsMoveInReserveInfoInqParams lotsMoveInReserveInfoInqParams = new Params.LotsMoveInReserveInfoInqParams();
        lotsMoveInReserveInfoInqParams.setEquipmentID(equipmentID);
        lotsMoveInReserveInfoInqParams.setUser(objCommon.getUser());
        lotsMoveInReserveInfoInqParams.setStartCassettes(equipmentAutoMoveInReserveInfoGetResult.getStrStartCassette());
        Results.LotsMoveInReserveInfoInqResult lotsMoveInReserveInfoInqResult = lotInqService.sxLotsMoveInReserveInfoInq(objCommon, lotsMoveInReserveInfoInqParams);

        //add autoMoveInReserve single FOUP loadSequenceNumber loadPurposeType loadPortID unloadPortID
        lotsMoveInReserveInfoInqResult.getStrStartCassette().get(0).setLoadSequenceNumber(eqpPortStatus.getLoadSequenceNumber());
        lotsMoveInReserveInfoInqResult.getStrStartCassette().get(0).setLoadPurposeType(BizConstant.SP_LOADPURPOSETYPE_PROCESSLOT);
        lotsMoveInReserveInfoInqResult.getStrStartCassette().get(0).setLoadPortID(portID);
        lotsMoveInReserveInfoInqResult.getStrStartCassette().get(0).setUnloadPortID(portID);

        //step7 - AutoMoveInReserveReq
        log.debug("step7 - AutoMoveInReserveReq");
        Params.MoveInReserveReqParams moveInReserveReqParams = new Params.MoveInReserveReqParams();
        moveInReserveReqParams.setUser(objCommon.getUser());
        moveInReserveReqParams.setEquipmentID(equipmentID);
        moveInReserveReqParams.setPortGroupID(eqpPortStatus.getPortGroup());
        moveInReserveReqParams.setControlJobID(new ObjectIdentifier());
        moveInReserveReqParams.setStartCassetteList(lotsMoveInReserveInfoInqResult.getStrStartCassette());
        //only autoMoveInReserveReq can set autoMoveInReserveFlag = true,others is false
        moveInReserveReqParams.setAutoMoveInReserveFlag(true);
        return (Results.EqpAutoMoveInReserveReqResult) this.sxMoveInReserveReq(objCommon, moveInReserveReqParams);
    }
}
