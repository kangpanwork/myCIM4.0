package com.fa.cim.service.bond.impl;

import com.fa.cim.annotaion.OmService;
import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.OmCode;
import com.fa.cim.common.utils.*;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.config.RetCodeConfigEx;
import com.fa.cim.dto.*;
import com.fa.cim.method.*;
import com.fa.cim.newcore.bo.dispatch.DispatchingManager;
import com.fa.cim.newcore.bo.durable.CimCassette;
import com.fa.cim.newcore.bo.machine.CimMachine;
import com.fa.cim.newcore.bo.pd.CimProcessOperation;
import com.fa.cim.newcore.bo.person.CimPerson;
import com.fa.cim.newcore.bo.product.CimLot;
import com.fa.cim.newcore.bo.product.CimWafer;
import com.fa.cim.newcore.dto.machine.MachineDTO;
import com.fa.cim.newcore.exceptions.NotFoundRecordException;
import com.fa.cim.newcore.factory.BaseCoreFactory;
import com.fa.cim.newcore.impl.bo.env.StandardProperties;
import com.fa.cim.newcore.standard.drblmngm.MaterialContainer;
import com.fa.cim.newcore.standard.mchnmngm.Machine;
import com.fa.cim.newcore.standard.prdctmng.Product;
import com.fa.cim.service.bond.IBondService;
import com.fa.cim.service.lot.ILotInqService;
import com.fa.cim.service.lot.ILotService;
import com.fa.cim.service.processcontrol.IProcessControlService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/9/8        ********             Bear               create file
 *
 * @author: LiaoYunChuan
 * @date: 2020/9/8 17:24
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@OmService
@Slf4j
public class BondServiceImpl implements IBondService {
    @Autowired
    private IBondingGroupMethod bondingGroupMethod;

    @Autowired
    private IObjectLockMethod objectLockMethod;

    @Autowired
    private ILotMethod lotMethod;

    @Autowired
    private IContaminationMethod contaminationMethod;

    @Autowired
    private ICassetteMethod cassetteMethod;

    @Autowired
    private IBondingMapMethod bondingMapMethod;

    @Autowired
    private IEventMethod eventMethod;

    @Autowired
    private RetCodeConfig retCodeConfig;

    @Autowired
    private ILotInqService lotInqService;

    @Autowired
    private IProcessControlService processControlService;

    @Autowired
    private ILotService lotService;

    @Autowired
    private IEquipmentMethod equipmentMethod;

    @Autowired
    private RetCodeConfigEx retCodeConfigEx;


    @Autowired
    private IObjectLockMethod lockMethod;


    @Autowired
    private IObjectMethod objectMethod;


    @Autowired
    private IPostProcessMethod postProcessMethod;


    @Autowired
    private IQTimeMethod qTimeMethod;

    @Autowired
    private BaseCoreFactory baseCoreFactory;

    @Autowired
    private DispatchingManager dispatchingManager;

    @Autowired
    private IProcessMethod processMethod;


    public void sxWaferStackingReq(Infos.ObjCommon objCommon, Params.WaferStackingReqInParams waferStackingReqInParams) {
        Validations.check(null == waferStackingReqInParams, retCodeConfig.getInvalidParameter());
        String bondingGroupID = waferStackingReqInParams.getBondingGroupID();
        //------------------------------
        // bondingGroup_info_GetDR
        //------------------------------
        Outputs.ObjBondingGroupInfoGetDROut bondingGroupInfoGetDROut = bondingGroupMethod.bondingGroupInfoGetDR(objCommon,
                bondingGroupID, true);

        Infos.BondingGroupInfo bondingGroupInfo = bondingGroupInfoGetDROut.getBondingGroupInfo();
        List<ObjectIdentifier> bondingLotIDList = bondingGroupInfoGetDROut.getBondingLotIDList();
        List<Infos.BondingMapInfo> bondingMapInfoSeq = bondingGroupInfo.getBondingMapInfoList();

        ObjectIdentifier targetEquipmentID = bondingGroupInfo.getTargetEquipmentID();
        // Get required equipment lock mode
        Inputs.ObjLockModeIn objLockModeIn = new Inputs.ObjLockModeIn();
        objLockModeIn.setObjectID(targetEquipmentID);
        objLockModeIn.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
        objLockModeIn.setFunctionCategory(objCommon.getTransactionID());
        objLockModeIn.setUserDataUpdateFlag(false);
        Outputs.ObjLockModeOut objLockModeOut = objectMethod.objectLockModeGet(objCommon, objLockModeIn);

        long lockMode = objLockModeOut.getLockMode();
        if (lockMode != BizConstant.SP_EQP_LOCK_MODE_WRITE) {
            log.info("lockMode != SP_EQP_LOCK_MODE_WRITE");
            // Lock Equipment Main Object
            Inputs.ObjAdvanceLockIn objAdvanceLockIn = new Inputs.ObjAdvanceLockIn();
            objAdvanceLockIn.setObjectID(targetEquipmentID);
            objAdvanceLockIn.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
            objAdvanceLockIn.setObjectType(BizConstant.SP_OBJECTLOCK_OBJECTTYPE_MAINOBJECT);
            objAdvanceLockIn.setLockType(objLockModeOut.getRequiredLockForMainObject());
            lockMethod.advancedObjectLock(objCommon, objAdvanceLockIn);
        } else {
            log.info("lockMode = SP_EQP_LOCK_MODE_WRITE");
            //--------------------------------------------------------
            //  Object Lock for Lot
            //--------------------------------------------------------
            lockMethod.objectSequenceLock(objCommon, CimLot.class, bondingLotIDList);
        }

        //----------------------------------------------------------------
        // Check Bonding State
        //----------------------------------------------------------------
        if (!CimStringUtils.equals(bondingGroupInfo.getBondingGroupState(), BizConstant.SP_BONDINGGROUPSTATE_PROCESSED)
                && !CimStringUtils.equals(bondingGroupInfo.getBondingGroupState(), BizConstant.SP_BONDINGGROUPSTATE_ERROR)) {
            log.error("Bonding Group State should be Processed Or Error.");
            throw new ServiceException(retCodeConfigEx.getBondgrpStateInvalid());
        }

        Optional.ofNullable(bondingMapInfoSeq).ifPresent(list -> list.forEach(data -> {
            if (!CimStringUtils.equals(data.getBondingProcessState(), BizConstant.SP_BONDINGPROCESSSTATE_COMPLETED)) {
                log.error("Bonding Map State should be Completed.");
                throw new ServiceException(new OmCode(retCodeConfigEx.getBondmapStateInvalid(),
                        CimStringUtils.getValueOrEmptyString(data.getBondingProcessState()), data.getBaseWaferID().getValue()));
            }
        }));

        //----------------------------------------------------------------
        // Fill in Actual Top Carrier and SlotNo in Bonding Map
        //----------------------------------------------------------------
        List<ObjectIdentifier> topLotIDSeq = new ArrayList<>();
        for (ObjectIdentifier bondLot : bondingLotIDList) {
            AtomicBoolean isTopLot = new AtomicBoolean(false);
            Optional.ofNullable(bondingMapInfoSeq).ifPresent(list -> {
                for (Infos.BondingMapInfo bondMap : list) {
                    if (ObjectIdentifier.equalsWithValue(bondLot, bondMap.getPlanTopLotID())) {
                        isTopLot.set(true);
                        break;
                    }
                    if (ObjectIdentifier.equalsWithValue(bondLot, bondMap.getBaseLotID())) {
                        break;
                    }
                }
                if (isTopLot.get()) topLotIDSeq.add(bondLot);
            });
        }

        for (ObjectIdentifier topLot : topLotIDSeq) {
            log.info("Top Lot: " + topLot.getValue());
            //----------------------------------------------------------------
            //  FillIn Top Wafer Position
            //----------------------------------------------------------------
            List<Infos.LotWaferMap> lotWaferMaps = lotMethod.lotWaferMapGet(objCommon, topLot);
            Optional.ofNullable(lotWaferMaps).ifPresent(waferMaps -> {
                for (Infos.LotWaferMap waferMap : waferMaps) {
                    for (Infos.BondingMapInfo info : bondingMapInfoSeq) {
                        if (ObjectIdentifier.equalsWithValue(waferMap.getWaferID(), info.getActualTopWaferID())) {
                            info.setActualTopLotID(topLot);
                            info.setActualTopWaferID(waferMap.getWaferID());
                            info.setActualTopCarrierID(waferMap.getCassetteID());
                            info.setActualTopSlotNo(waferMap.getSlotNumber());
                            break;
                        }
                    }
                }
            });
        }

        //--------------------------------------------------------
        //  Object Lock for Cassette
        //--------------------------------------------------------
        Set<ObjectIdentifier> castIDSeq = new HashSet<>();
        Optional.ofNullable(bondingMapInfoSeq).ifPresent(list -> list.forEach(data -> {
            if (!ObjectIdentifier.isEmpty(data.getActualTopCarrierID())) {
                castIDSeq.add(data.getActualTopCarrierID());
            }
        }));

        if (lockMode != BizConstant.SP_EQP_LOCK_MODE_WRITE) {
            log.info("lockMode != SP_EQP_LOCK_MODE_WRITE");
            if (CimArrayUtils.isNotEmpty(bondingLotIDList)) {
                //--------------------------------------------------------
                //  Get Lot's ControlJobID
                //--------------------------------------------------------
                ObjectIdentifier controlJobIDGet = lotMethod.lotControlJobIDGet(objCommon, bondingLotIDList.get(0));
                if (!ObjectIdentifier.isEmptyWithValue(controlJobIDGet)) {
                    // Lock Equipment ProcLot Element (Write)
                    Inputs.ObjAdvanceLockIn objAdvanceLockIn = new Inputs.ObjAdvanceLockIn();
                    objAdvanceLockIn.setObjectID(targetEquipmentID);
                    objAdvanceLockIn.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
                    objAdvanceLockIn.setObjectType(BizConstant.SP_OBJECTLOCK_OBJECTTYPE_INPROCESSINGLOT);
                    objAdvanceLockIn.setLockType(CimNumberUtils.longValue(BizConstant.SP_OBJECTLOCK_LOCKTYPE_WRITE));
                    List<String> keys = new ArrayList<>();
                    bondingLotIDList.forEach(data -> keys.add(data.getValue()));
                    objAdvanceLockIn.setKeyList(keys);
                    lockMethod.advancedObjectLock(objCommon, objAdvanceLockIn);
                }
            }

            Inputs.ObjAdvanceLockIn objAdvanceLockInForCassette = new Inputs.ObjAdvanceLockIn();
            objAdvanceLockInForCassette.setObjectID(targetEquipmentID);
            objAdvanceLockInForCassette.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
            objAdvanceLockInForCassette.setObjectType(BizConstant.SP_OBJECTLOCK_OBJECTTYPE_LOADCASSETTE);
            objAdvanceLockInForCassette.setLockType(CimNumberUtils.longValue(BizConstant.SP_OBJECTLOCK_LOCKTYPE_WRITE));
            List<String> cassetteKeys = new ArrayList<>();
            castIDSeq.forEach(data -> cassetteKeys.add(data.getValue()));
            objAdvanceLockInForCassette.setKeyList(cassetteKeys);
            try {
                lockMethod.advancedObjectLock(objCommon, objAdvanceLockInForCassette);
            } catch (NotFoundRecordException e) {
                //cassette EO,service not called by move out but controller, called by bonding result report
                log.info("Cassete is EO,Please make sure");
            }

            /*------------------------------*/
            /*   Lock Cassette/Lot Object   */
            /*-------------------------------*/
            lockMethod.objectSequenceLock(objCommon, CimCassette.class, new ArrayList<>(castIDSeq));

            lockMethod.objectSequenceLock(objCommon, CimLot.class, bondingLotIDList);
        } else {
            log.info("lockMode = SP_EQP_LOCK_MODE_WRITE");
            lockMethod.objectSequenceLock(objCommon, CimCassette.class, new ArrayList<>(castIDSeq));
        }

        //--------------------------------------------------------------------------
        // Check:
        //  The first postProcess action in the queue should be "WaferStacking"
        //--------------------------------------------------------------------------
        Inputs.PostProcessQueueListDRIn postProcessQueueListDRIn = new Inputs.PostProcessQueueListDRIn();
        Infos.PostProcessTargetObject targetObject = new Infos.PostProcessTargetObject();
        targetObject.setEquipmentID(targetEquipmentID);
        targetObject.setControlJobID(bondingGroupInfo.getControlJobID());
        postProcessQueueListDRIn.setStrPostProcessTargetObject(targetObject);
        postProcessQueueListDRIn.setSeqNo(-1L);
        postProcessQueueListDRIn.setSyncFlag(-1L);
        postProcessQueueListDRIn.setPassedTime(-1L);
        postProcessQueueListDRIn.setMaxCount(1L);
        postProcessQueueListDRIn.setCommittedReadFlag(true);
        Outputs.ObjPostProcessQueListDROut postProcessQueListDROut = postProcessMethod.postProcessQueueListDR(objCommon,
                postProcessQueueListDRIn);

        if (null != postProcessQueListDROut && CimArrayUtils.getSize(postProcessQueListDROut.getStrActionInfoSeq()) > 0
                && !CimStringUtils.equals(postProcessQueListDROut.getStrActionInfoSeq().get(0).getPostProcessID(),
                BizConstant.SP_POSTPROCESS_ACTIONID_WAFERSTACKING)) {
            log.error("The first PostProcess information is not WaferStacking");
            throw new ServiceException(retCodeConfigEx.getEqpNotInWaferStacking());
        }

        //----------------------------------------------------------------
        // Check Lot Condition
        //----------------------------------------------------------------
        lotMethod.lotCheckConditionForWaferStacking(objCommon, bondingLotIDList);

        for (ObjectIdentifier topLot : topLotIDSeq) {
            //----------------------------------------------------------------
            // Post Process Flag Off
            //----------------------------------------------------------------
            lotMethod.lotInPostProcessFlagSet(objCommon, topLot, false);

            //----------------------------------------------------------------
            // Release ForceOpeCompHold / LockHold
            //----------------------------------------------------------------
            List<Infos.LotHoldListAttributes> lotHoldListInqResult = null;
            try {
                lotHoldListInqResult = lotInqService.sxHoldLotListInq(objCommon, topLot);
            } catch (ServiceException e) {
                if (!Validations.isEquals(e.getCode(), retCodeConfig.getNotFoundEntry())) {
                    throw e;
                }
            }

            //--------------------------
            // Hold Lot Release
            //--------------------------
            Optional.ofNullable(lotHoldListInqResult).ifPresent(list -> list.forEach(holdRecord -> {
                if (CimStringUtils.equals(holdRecord.getReasonCodeID().getValue(), BizConstant.SP_REASON_LOTLOCK)
                        || CimStringUtils.equals(holdRecord.getReasonCodeID().getValue(), BizConstant.SP_REASON_FORCECOMPHOLD)) {
                    log.info("Find Lock Hold or Force OpeComp Hold Record!!");
                    Params.HoldLotReleaseReqParams holdLotReleaseReqParams = new Params.HoldLotReleaseReqParams();
                    holdLotReleaseReqParams.setLotID(topLot);

                    Infos.LotHoldReq holdReq = new Infos.LotHoldReq();
                    holdReq.setHoldType(holdRecord.getHoldType());
                    holdReq.setHoldReasonCodeID(holdRecord.getReasonCodeID());
                    holdReq.setHoldUserID(holdRecord.getUserID());
                    holdReq.setResponsibleOperationMark(holdRecord.getResponsibleOperationMark());
                    holdReq.setRouteID(holdRecord.getResponsibleRouteID());
                    holdReq.setOperationNumber(holdRecord.getResponsibleOperationNumber());
                    holdReq.setRelatedLotID(holdRecord.getRelatedLotID());

                    holdLotReleaseReqParams.setHoldReqList(Collections.singletonList(holdReq));

                    ObjectIdentifier aReasonCodeID;
                    if (CimStringUtils.equals(holdReq.getHoldReasonCodeID().getValue(), BizConstant.SP_REASON_LOTLOCK)) {
                        aReasonCodeID = ObjectIdentifier.buildWithValue(BizConstant.SP_REASON_LOTLOCKRELEASE);
                    } else {
                        aReasonCodeID = ObjectIdentifier.buildWithValue(BizConstant.SP_REASON_WAFERSTACKINGHOLDRELEASE);
                    }
                    holdLotReleaseReqParams.setUser(objCommon.getUser());
                    holdLotReleaseReqParams.setReleaseReasonCodeID(aReasonCodeID);
                    lotService.sxHoldLotReleaseReq(objCommon, holdLotReleaseReqParams);
                }
            }));
        }

        //----------------------------------------------------------------
        // Make Wafer Stacked
        //----------------------------------------------------------------
        bondingMapMethod.bondingMapWaferStackMake(objCommon, bondingMapInfoSeq);

        //----------------------------------------------------------------
        // Make Lot Stacked
        // lot_waferStack_Make
        //----------------------------------------------------------------
        this.lotWaferStackMake(objCommon, topLotIDSeq);

        //----------------------------------------------------------------
        // Update Cassette's MultiLotType
        //----------------------------------------------------------------
        Optional.of(castIDSeq).ifPresent(list -> list.forEach(data -> cassetteMethod.cassetteMultiLotTypeUpdate(objCommon, data)));

        //----------------------------------------------------------------
        // Make Wafer Stack Event
        //----------------------------------------------------------------
        Optional.of(bondingLotIDList).ifPresent(list -> list.forEach(data -> lotMethod.lotWaferLotHistoryPointerUpdate(objCommon, data)));

        eventMethod.waferStackingEventMake(objCommon, bondingGroupInfo);

        //===================================================================================
        // **** Delete Bonding Group information.
        //===================================================================================
        bondingGroupInfo.setClaimMemo(waferStackingReqInParams.getClaimMemo());
        bondingGroupMethod.bondingGroupInfoUpdateDR(objCommon, BizConstant.SP_BONDINGGROUPACTION_DELETE, bondingGroupInfo);

        //===================================================================================
        // **** Make event for Bonding Group information update.
        //===================================================================================
        eventMethod.bondingGroupEventMake(objCommon, BizConstant.SP_BONDINGGROUPACTION_DELETE, bondingGroupInfo, null);

    }


    public void lotWaferStackMake(Infos.ObjCommon objCommon, List<ObjectIdentifier> topLotIDSeq) {
        CimPerson aPerson = baseCoreFactory.getBO(CimPerson.class, objCommon.getUser().getUserID());
        Validations.check(null == aPerson, retCodeConfig.getNotFoundPerson());
        Optional.ofNullable(topLotIDSeq).ifPresent(topLotIDs -> {
            for (ObjectIdentifier topLotID : topLotIDs) {
                Outputs.ObjQtimeAllClearByRouteChangeOut qtimeAllClearByRouteChangeOut = qTimeMethod.qtimeAllClearByRouteChange(objCommon, topLotID);
                //--------------------------------------------------
                // Reset Q-Time actions
                //--------------------------------------------------
                ObjectIdentifier resetReasonCodeID = ObjectIdentifier.buildWithValue(BizConstant.SP_REASON_QTIMECLEAR);

                //----- Lot Hold Actions -------//
                if (CimArrayUtils.getSize(qtimeAllClearByRouteChangeOut.getStrLotHoldReleaseList()) > 0) {
                    Params.HoldLotReleaseReqParams holdLotReleaseReqParams = new Params.HoldLotReleaseReqParams();
                    holdLotReleaseReqParams.setUser(objCommon.getUser());
                    holdLotReleaseReqParams.setReleaseReasonCodeID(resetReasonCodeID);
                    holdLotReleaseReqParams.setLotID(topLotID);
                    holdLotReleaseReqParams.setHoldReqList(qtimeAllClearByRouteChangeOut.getStrLotHoldReleaseList());
                    lotService.sxHoldLotReleaseReq(objCommon, holdLotReleaseReqParams);
                }

                //----- Future Hold Actions -------//
                processControlService.sxFutureHoldCancelReq(objCommon, topLotID, resetReasonCodeID,
                        BizConstant.SP_ENTRYTYPE_CANCEL, qtimeAllClearByRouteChangeOut.getStrFutureHoldCancelList());

                //----- Future Rework Actions -------//
                Optional.ofNullable(qtimeAllClearByRouteChangeOut.getStrFutureReworkCancelList()).ifPresent(list -> list.forEach(data -> {
                    processControlService.sxFutureReworkCancelReq(objCommon, data.getLotID(), data.getRouteID(),
                            data.getOperationNumber(), data.getFutureReworkDetailInfoList(), null);
                }));

                //--------------------------------------------------
                // Remove Lot from Equipment and Cassette
                //--------------------------------------------------
                CimLot aTopLot = baseCoreFactory.getBO(CimLot.class, topLotID);
                Validations.check(null == aTopLot, retCodeConfig.getNotFoundLot());
                List<MaterialContainer> aMaterialContainerSequence = aTopLot.materialContainers();

                Optional.ofNullable(aMaterialContainerSequence).ifPresent(materialContainers -> {
                    for (MaterialContainer materialContainer : materialContainers) {
                        if (!(materialContainer instanceof CimCassette)) {
                            continue;
                        }
                        CimCassette aCassette = (CimCassette) materialContainer;
                        aCassette.unloadLot(aTopLot);

                        String castID = aCassette.getIdentifier();
                        Machine tmpMachine = aCassette.currentAssignedMachine();
                        if (null == tmpMachine) {
                            log.info("Equipment is Not found.");
                            continue;
                        }

                        CimMachine aMachine = (CimMachine) tmpMachine;

                        // get FREQP_CAST information
                        MachineDTO.MachineCassette updatedCassetteInfo = aMachine.findCassetteNamed(castID);
                        if (null == updatedCassetteInfo ||
                                !CimStringUtils.equals(ObjectIdentifier.fetchValue(updatedCassetteInfo.getCassetteID()), castID)) {
                            log.info("Cassette is not Found in Equipment.");
                            continue;
                        }

                        // update FREQP_CAST_LOT
                        List<MachineDTO.MachineCassetteLot> machineCassetteLots = updatedCassetteInfo.getMachineCassetteLots();
                        List<MachineDTO.MachineCassetteLot> tmpLotList = new ArrayList<>();
                        for (MachineDTO.MachineCassetteLot machineCassetteLot : machineCassetteLots) {
                            if (ObjectIdentifier.equalsWithValue(machineCassetteLot.getLotID(), topLotID)) {
                                log.info("Remove this lot from OMEQP_CARRIER_LOT.");
                            } else {
                                log.info("Stay lot in OMEQP_CARRIER_LOT.");
                                tmpLotList.add(machineCassetteLot);
                            }
                        }
                        updatedCassetteInfo.setMachineCassetteLots(tmpLotList);
                        aMachine.updateCassette(updatedCassetteInfo);
                    }
                });

                //--------------------------------------------------
                // Dequeue Lot from Dispatcher
                //--------------------------------------------------
                String lotHoldState = aTopLot.getLotHoldState();
                if (CimStringUtils.equals(lotHoldState, BizConstant.CIMFW_LOT_HOLDSTATE_ONHOLD)) {
                    dispatchingManager.removeFromHoldQueue(aTopLot);
                } else {
                    dispatchingManager.removeFromQueue(aTopLot);
                }

                //--------------------------------------------------
                // Make Lot Stacked
                //--------------------------------------------------
                List<Product> prodSeq = aTopLot.allProducts();
                Optional.ofNullable(prodSeq).ifPresent(list -> list.forEach(data -> {
                    CimWafer wafer = (CimWafer) data;
                    if (!wafer.isStacked()) {
                        log.error(String.format("Wafer[%s] State Invalid.", wafer.getIdentifier()));
                        throw new ServiceException(retCodeConfig.getInvalidWaferState());
                    }
                }));

                aTopLot.makeStacked();

                String envStr = StandardProperties.OM_MAINT_PO_EVENT_CREATE_TYPE.getValue();
                int env = 0;
                if (null != envStr) {
                    env = Integer.parseInt(envStr);
                }
                if (env == BizConstant.SP_POMAINTEVENTCREATETYPE_INACTIVELOTENABLED
                        || env == BizConstant.SP_POMAINTEVENTCREATETYPE_ENABLED) {
                    CimProcessOperation aCurrentPO = aTopLot.getProcessOperation();
                    if (null != aCurrentPO) {
                        processMethod.poDelQueuePutDR(objCommon, topLotID);
                    }
                }

                //--------------------------------------------------
                // Set State Changed Record
                //--------------------------------------------------
                aTopLot.setStateChangedTimeStamp(objCommon.getTimeStamp().getReportTimeStamp());
                aTopLot.setStateChangedPerson(aPerson);
            }
        });
    }

    public void sxWaferStackingCancelReq(Infos.ObjCommon objCommon, Params.WaferStackingCancelReqInParams waferStackingCancelReqInParams) {
        Validations.check(null == waferStackingCancelReqInParams, retCodeConfig.getInvalidParameter());
        List<ObjectIdentifier> topLotIDSeq = waferStackingCancelReqInParams.getTopLotIDSeq();

        //------------------------------------------------------
        // Get stacked wafer information
        //------------------------------------------------------
        Inputs.ObjLotStackedWaferInfoGetDRIn lotStackedWaferInfoGetDRIn = new Inputs.ObjLotStackedWaferInfoGetDRIn();
        lotStackedWaferInfoGetDRIn.setTopLotIDSeq(topLotIDSeq);
        Outputs.ObjLotStackedWaferInfoGetDROut lotStackedWaferInfoGetDROut = lotMethod.lotStackedWaferInfoGetDR(objCommon,
                lotStackedWaferInfoGetDRIn);
        Validations.check(null == lotStackedWaferInfoGetDROut, retCodeConfigEx.getNotFoundStackedWafer());
        List<ObjectIdentifier> baseLotIDSeq = lotStackedWaferInfoGetDROut.getBaseLotIDSeq();

        if (CimArrayUtils.getSize(baseLotIDSeq) == 0) {
            log.error("Stacked Wafer Not Found.");
            throw new ServiceException(retCodeConfigEx.getNotFoundStackedWafer());
        }

        //--------------------------------------------------------
        //  Object Lock for Lot
        //--------------------------------------------------------
        lockMethod.objectLockWithoutDoubleLock(objCommon, topLotIDSeq, BizConstant.SP_CLASSNAME_POSLOT);

        lockMethod.objectLockWithoutDoubleLock(objCommon, baseLotIDSeq, BizConstant.SP_CLASSNAME_POSLOT);

        //----------------------------------------------------------------
        // Check Lot Condition
        //----------------------------------------------------------------
        lotMethod.lotCheckConditionForWaferStackingCancel(objCommon, baseLotIDSeq, topLotIDSeq);

        //----------------------------------------------------------------
        // Request Wafer Stacking Cancel
        //----------------------------------------------------------------
        log.info("Request Wafer Stacking Cancel.");
        lotMethod.lotWaferStackCancel(objCommon, baseLotIDSeq, topLotIDSeq, lotStackedWaferInfoGetDROut.getStrStackedWaferInfoSeq());

        //----------------------------------------------------------------
        // Make event for Cancel Wafer Stacking.
        //----------------------------------------------------------------
        Optional.ofNullable(baseLotIDSeq).ifPresent(list -> list.forEach(data -> lotMethod.lotWaferLotHistoryPointerUpdate(objCommon, data)));

        Optional.ofNullable(topLotIDSeq).ifPresent(list -> list.forEach(data -> lotMethod.lotWaferLotHistoryPointerUpdate(objCommon, data)));

        log.info("## Make event for Cancel Wafer Stacking.");
        eventMethod.waferStackingCancelEventMake(objCommon, lotStackedWaferInfoGetDROut.getStrStackedWaferInfoSeq(),
                waferStackingCancelReqInParams.getClaimMemo());
    }


    public void sxBondingMapResultRpt(Infos.ObjCommon objCommon, Params.BondingMapResultRptInParams bondingMapResultRptInParams) {
        Validations.check(null == bondingMapResultRptInParams, retCodeConfig.getInvalidParameter());
        //----------------------------------------------------------------
        // Get Lot List
        //----------------------------------------------------------------
        List<Infos.BondingMapInfo> bondingMapInfoList = bondingMapResultRptInParams.getBondingMapInfoList();
        Outputs.ObjBondingMapFillInTxPCR003DROut objBondingMapFillInTxPCR003DROut = bondingMapMethod.bondingMapFillInTxPCR003DR(objCommon,
                bondingMapInfoList);

        //--------------------------------------------------------
        //  Object Lock for Lot
        //--------------------------------------------------------
        objectLockMethod.objectLockWithoutDoubleLock(objCommon, objBondingMapFillInTxPCR003DROut.getBondingLotIDSeq(),
                BizConstant.SP_CLASSNAME_POSLOT);

        //----------------------------------------------------------------
        // Bonding Map Info Get
        //----------------------------------------------------------------
        Outputs.ObjBondingGroupInfoByEqpGetDROut objBondingGroupInfoByEqpGetDROut = bondingGroupMethod.bondingGroupInfoByEqpGetDR(objCommon,
                bondingMapResultRptInParams.getEquipmentID(),
                bondingMapResultRptInParams.getControlJobID(),
                true);

        assert null != objBondingGroupInfoByEqpGetDROut;
        Optional.of(objBondingGroupInfoByEqpGetDROut).ifPresent(out -> out.getBondingGroupInfoList().forEach(data -> {
            Validations.check(!CimStringUtils.equals(data.getBondingGroupState(), BizConstant.SP_BONDINGGROUPSTATE_PROCESSED)
                    && !CimStringUtils.equals(data.getBondingGroupState(), BizConstant.SP_BONDINGGROUPSTATE_ERROR),
                    retCodeConfigEx.getBondgrpStateInvalid());
        }));

        //----------------------------------------------------------------
        // Bonding Map Consistency Check
        //----------------------------------------------------------------
        log.info("Check Consistency of Bonding Map.");
        List<Infos.BondingMapInfo> bondingMapInfos = bondingMapMethod.bondingMapResultMerge(objCommon,
                objBondingMapFillInTxPCR003DROut.getStrBondingMapInfoSeq(),
                objBondingGroupInfoByEqpGetDROut.getBondingGroupInfoList());

        bondingMapMethod.bondingMapInfoConsistencyCheck(objCommon, bondingMapInfos, bondingMapResultRptInParams.getEquipmentID(), true);

        //----------------------------------------------------------------
        // Update Bonding Map
        //----------------------------------------------------------------
        bondingMapMethod.bondingMapResultUpdateDR(objCommon, bondingMapInfos);
    }


    /**
     * This function creates or updates Bonding Group Information.
     *
     * @version 1.0
     * @author ZQI
     * @date 2020/4/16 17:57
     */
    public Results.BondingGroupUpdateReqResult sxBondingGroupUpdateReq(Infos.ObjCommon objCommon,
                                                                       Params.BondingGroupUpdateReqInParams params) {
        //===================================================================================
        //
        // **** Check input parameter
        //
        //===================================================================================
        //-------------------------------------------------------------
        // *** Check length of Bonding Group Info structure
        //-------------------------------------------------------------
        Validations.check(null == params, retCodeConfig.getInvalidParameter());
        List<Infos.BondingGroupInfo> strBondingGroupInfoSeq = params.getStrBondingGroupInfoSeq();
        String updateMode = params.getUpdateMode();
        int size = CimArrayUtils.getSize(strBondingGroupInfoSeq);
        log.info("BondingGroupLen: " + size);
        Validations.check(size == 0, retCodeConfig.getInvalidParameter());
        assert null != strBondingGroupInfoSeq;

        //-------------------------------------------------------------
        // *** Check Update Mode type
        //     Create/Update/Delete
        //-------------------------------------------------------------
        log.info("## UpdateMode is : " + updateMode);
        boolean isCreate = CimStringUtils.equals(updateMode, BizConstant.SP_BONDINGGROUP_UPDATEMODE_CREATE);
        boolean isUpdate = CimStringUtils.equals(updateMode, BizConstant.SP_BONDINGGROUP_UPDATEMODE_UPDATE);
        boolean isDelete = CimStringUtils.equals(updateMode, BizConstant.SP_BONDINGGROUP_UPDATEMODE_DELETE);
        Validations.check(!isCreate && !isUpdate && !isDelete, retCodeConfig.getInvalidParameter());
        Optional.of(strBondingGroupInfoSeq).ifPresent(list -> list.forEach(data -> {
            if (isCreate) {
                Validations.check(CimArrayUtils.getSize(data.getBondingMapInfoList()) == 0, retCodeConfig.getInvalidParameter());
            }
            if (isDelete || isUpdate) {
                Validations.check(CimStringUtils.isEmpty(data.getBondingGroupID()), retCodeConfig.getInvalidParameter());
            }
        }));


        Results.BondingGroupUpdateReqResult strBondingGroupUpdateReqResult = new Results.BondingGroupUpdateReqResult();
        List<Infos.BondingGroupUpdateResult> bondingGroupUpdateResults = new ArrayList<>();
        strBondingGroupUpdateReqResult.setStrBondingGroupUpdateResultSeq(bondingGroupUpdateResults);

        AtomicInteger successCount = new AtomicInteger(0);

        for (Infos.BondingGroupInfo bondingGroupInfo : strBondingGroupInfoSeq) {
            Infos.retMessage retMessage = new Infos.retMessage();
            Infos.BondingGroupUpdateResult updateResult = new Infos.BondingGroupUpdateResult();
            updateResult.setBondingGroupID(bondingGroupInfo.getBondingGroupID());
            updateResult.setRetMessage(retMessage);
            bondingGroupUpdateResults.add(updateResult);
            //-------------------------------------------------------------
            // *** Get Bonding Group structure
            //-------------------------------------------------------------
            Outputs.ObjBondingGroupInfoGetDROut strBondingGroup_info_GetDR_out = null;
            if (isCreate) {
                log.info("SP_BondingGroup_UpdateMode_Create");
                strBondingGroup_info_GetDR_out = new Outputs.ObjBondingGroupInfoGetDROut();
                strBondingGroup_info_GetDR_out.setBondingGroupInfo(bondingGroupInfo);
                Set<ObjectIdentifier> bondingLotIDSeq = new HashSet<>();
                Optional.ofNullable(bondingGroupInfo.getBondingMapInfoList()).ifPresent(list -> list.forEach(data -> {
                    bondingLotIDSeq.add(data.getBaseLotID());
                    bondingLotIDSeq.add(data.getPlanTopLotID());
                }));
                strBondingGroup_info_GetDR_out.setBondingLotIDList(new ArrayList<>(bondingLotIDSeq));
                log.info("Bonding Lot ID length = " + CimArrayUtils.getSize(strBondingGroup_info_GetDR_out.getBondingLotIDList()));
            }

            if (isUpdate) {
                log.info("SP_BondingGroup_UpdateMode_Update");
                //------------------------------
                // bondingGroup_info_GetDR
                //------------------------------
                try {
                    strBondingGroup_info_GetDR_out = bondingGroupMethod.bondingGroupInfoGetDR(objCommon,
                            bondingGroupInfo.getBondingGroupID(), true);
                } catch (ServiceException e) {
                    retMessage.setReturnCode(e.getCode());
                    retMessage.setMessageText(e.getMessage());
                    continue;
                }
                if (null != strBondingGroup_info_GetDR_out) {
                    log.info("Bonding Lot ID length = " + CimArrayUtils.getSize(strBondingGroup_info_GetDR_out.getBondingLotIDList()));
                    if (!CimStringUtils.equals(strBondingGroup_info_GetDR_out.getBondingGroupInfo().getBondingGroupState(),
                            BizConstant.SP_BONDINGGROUPSTATE_CREATED)) {
                        continue;
                    }
                    strBondingGroup_info_GetDR_out.getBondingGroupInfo().setTargetEquipmentID(bondingGroupInfo.getTargetEquipmentID());
                }
            }

            if (isDelete) {
                log.info("SP_BondingGroup_UpdateMode_Delete");
                //------------------------------
                // bondingGroup_info_GetDR
                //------------------------------
                try {
                    strBondingGroup_info_GetDR_out = bondingGroupMethod.bondingGroupInfoGetDR(objCommon,
                            bondingGroupInfo.getBondingGroupID(), true);
                } catch (ServiceException e) {
                    retMessage.setReturnCode(e.getCode());
                    retMessage.setMessageText(e.getMessage());
                    continue;
                }
                if (null != strBondingGroup_info_GetDR_out) {
                    String state = strBondingGroup_info_GetDR_out.getBondingGroupInfo().getBondingGroupState();
                    if (!CimStringUtils.equals(state, BizConstant.SP_BONDINGGROUPSTATE_CREATED)
                            && !CimStringUtils.equals(state, BizConstant.SP_BONDINGGROUPSTATE_ERROR)) {
                        log.info("Bonding Group State should be SP_BondingGroupState_Created or SP_BondingGroupState_Error.");
                    }
                }
            }

            //--------------------------------------------------------
            //  Object Lock for Lot
            //--------------------------------------------------------
            assert strBondingGroup_info_GetDR_out != null;
            try {
                objectLockMethod.objectLockWithoutDoubleLock(objCommon, strBondingGroup_info_GetDR_out.getBondingLotIDList(),
                        BizConstant.SP_CLASSNAME_POSLOT);
            } catch (ServiceException e) {
                retMessage.setReturnCode(e.getCode());
                retMessage.setMessageText(e.getMessage());
                continue;
            }

            //-------------------------------------------------------------
            //
            // *** Check Conditions
            //
            //-------------------------------------------------------------
            //-------------------------------------------------------------
            // *** Check Equipment Information
            //-------------------------------------------------------------
            if (isCreate || isUpdate) {
                ObjectIdentifier targetEquipmentID = ObjectIdentifier.emptyIdentifier();
                if (!CimObjectUtils.isEmpty(strBondingGroup_info_GetDR_out.getBondingGroupInfo())) {
                    targetEquipmentID = strBondingGroup_info_GetDR_out.getBondingGroupInfo().getTargetEquipmentID();
                }
                if (CimStringUtils.isNotEmpty(ObjectIdentifier.fetchValue(targetEquipmentID))) {
                    log.info("Check Transaction ID and equipment Category combination.");
                    try {
                        equipmentMethod.equipmentCategoryVsTxIDCheckCombination(objCommon, targetEquipmentID);
                    } catch (ServiceException e) {
                        retMessage.setReturnCode(e.getCode());
                        retMessage.setMessageText(e.getMessage());
                        continue;
                    }

                    Outputs.ObjEquipmentProcessBatchConditionGetOut objEquipmentProcessBatchConditionGetOut;
                    log.info("Check Minimum Process Wafer Count.");
                    try {
                        objEquipmentProcessBatchConditionGetOut = equipmentMethod.equipmentProcessBatchConditionGet(objCommon, targetEquipmentID);
                    } catch (ServiceException e) {
                        retMessage.setReturnCode(e.getCode());
                        retMessage.setMessageText(e.getMessage());
                        continue;
                    }

                    int bondingMapInfoSize = CimArrayUtils.getSize(strBondingGroup_info_GetDR_out.getBondingGroupInfo()
                            .getBondingMapInfoList());
                    if (null != objEquipmentProcessBatchConditionGetOut
                            && objEquipmentProcessBatchConditionGetOut.getMinWaferSize() > bondingMapInfoSize * 2) {
                        log.info("Minimum Process Wafer Count is larger than wafer count.");
                        continue;
                    }
                }

                //-------------------------------------------------------------
                // *** Check Consistency of Bonding Map
                //-------------------------------------------------------------
                log.info("Check Consistency of Bonding Map.");
                try {
                    Infos.BondingGroupInfo tmpBondingGroupInfo = strBondingGroup_info_GetDR_out.getBondingGroupInfo();
                    bondingMapMethod.bondingMapInfoConsistencyCheck(objCommon,
                            CimObjectUtils.isEmpty(tmpBondingGroupInfo) ? new ArrayList<>() : tmpBondingGroupInfo.getBondingMapInfoList(),
                            targetEquipmentID, false);
                } catch (ServiceException e) {
                    retMessage.setReturnCode(e.getCode());
                    retMessage.setMessageText(e.getMessage());
                    continue;
                }
            }


            //-------------------------------------------------------------
            // *** Check Consistency of Bonding Group structure
            //-------------------------------------------------------------
            if (isCreate) {
                log.info("Check Condition of Related Lots.");
                try {
                    lotMethod.lotCheckConditionForBondingGroup(objCommon, BizConstant.SP_BONDINGGROUPACTION_CREATE,
                            strBondingGroup_info_GetDR_out.getBondingLotIDList());
                } catch (ServiceException e) {
                    retMessage.setReturnCode(e.getCode());
                    retMessage.setMessageText(e.getMessage());
                    continue;
                }
            }

            //===================================================================================
            //
            // **** Create/Update/Delete Bonding Group information.
            //
            //===================================================================================
            log.info("## Create/Update/Delete Bonding Group information.");
            String action = null;
            Infos.BondingGroupInfo bondingGroupInfoIn = strBondingGroup_info_GetDR_out.getBondingGroupInfo();
            if (isCreate) {
                action = BizConstant.SP_BONDINGGROUPACTION_CREATE;
                String bondingGroupID = String.format("%s%s%s",
                        ObjectIdentifier.fetchValue(bondingGroupInfoIn.getBondingMapInfoList().get(0).getBaseLotID()),
                        "+",
                        objCommon.getTimeStamp().getReportTimeStamp().toString());
                bondingGroupInfoIn.setBondingGroupID(bondingGroupID);
                List<Infos.BondingGroupUpdateResult> strBondingGroupUpdateResultSeq = strBondingGroupUpdateReqResult.getStrBondingGroupUpdateResultSeq();
                strBondingGroupUpdateResultSeq.get(0).setBondingGroupID(bondingGroupID);
            }

            if (isUpdate) {
                action = BizConstant.SP_BONDINGGROUPACTION_UPDATE;
            }

            if (isDelete) {
                action = BizConstant.SP_BONDINGGROUPACTION_DELETE;
            }

            bondingGroupInfoIn.setClaimMemo(params.getClaimMemo());
            try {
                bondingGroupMethod.bondingGroupInfoUpdateDR(objCommon, action, bondingGroupInfoIn);
            } catch (ServiceException e) {
                retMessage.setReturnCode(e.getCode());
                retMessage.setMessageText(e.getMessage());
                continue;
            }

            //===================================================================================
            //
            // **** Make event for Bonding Group information update.
            //
            //===================================================================================
            log.info("## Make event for Bonding Group information registration. ");
            try {
                eventMethod.bondingGroupEventMake(objCommon, action, bondingGroupInfoIn, null);
            } catch (ServiceException e) {
                retMessage.setReturnCode(e.getCode());
                retMessage.setMessageText(e.getMessage());
                continue;
            }

            //===================================================================================
            // **** Set out structure
            //===================================================================================
            log.info("## Set out structure.");
            successCount.incrementAndGet();
            retMessage.setMessageText("Sucess");
            retMessage.setReturnCode(0);
        }

        Validations.check(strBondingGroupInfoSeq.size() != successCount.get(), strBondingGroupUpdateReqResult,
                retCodeConfig.getSomeRequestsFailed());
        //--------------------
        //   Return to Main
        //--------------------
        return strBondingGroupUpdateReqResult;
    }

    public List<Infos.BondingGroupReleasedLot> sxBondingGroupPartialReleaseReq(Infos.ObjCommon objCommon,
                                                                               Params.BondingGroupPartialReleaseReqInParam param) {
        log.info("PPTManager_i::BondingGroupPartialRemoveReq");
        String claimMemo = param.getClaimMemo();
        List<Infos.BondingGroupReleaseLotWafer> strBondingGroupReleaseLotWaferSeq = param.getStrBondingGroupReleaseLotWaferSeq();
        List<ObjectIdentifier> lotIDs = new ArrayList<>(strBondingGroupReleaseLotWaferSeq.size());
        List<Infos.BondingGroupInfo> strBondingGroupInfoSeq = new ArrayList<>();
        if (CimArrayUtils.isNotEmpty(strBondingGroupReleaseLotWaferSeq)) {
            List<String> bondingGroupIDList = strBondingGroupReleaseLotWaferSeq.parallelStream().
                    map(Infos.BondingGroupReleaseLotWafer::getBondingGroupID).distinct().collect(Collectors.toList());
            for (String bondingGroupID : bondingGroupIDList){
                log.debug("bondingGroupID -> " + bondingGroupID);
                Outputs.ObjBondingGroupInfoGetDROut bondingGroupInfoGetDROut = bondingGroupMethod
                        .bondingGroupInfoGetDR(objCommon, bondingGroupID, true);
                log.info("calling object_LockWithoutDoubleLock()");
                objectLockMethod.objectLockWithoutDoubleLock(objCommon, bondingGroupInfoGetDROut.getBondingLotIDList(),
                        BizConstant.SP_CLASSNAME_POSLOT);
                strBondingGroupInfoSeq.add(bondingGroupInfoGetDROut.getBondingGroupInfo());
            }
            strBondingGroupReleaseLotWaferSeq.forEach(bondingGroupReleaseLotWafer -> {
                ObjectIdentifier parentLotID = bondingGroupReleaseLotWafer.getParentLotID();
                log.debug("lotID -> " + ObjectIdentifier.fetchValue(parentLotID));
                lotIDs.add(parentLotID);
            });
        }

        //--------------------------------------------------------
        //  Object Lock for Cassette
        //--------------------------------------------------------
        List<ObjectIdentifier> cassetteIDs = new ArrayList<>();
        lotIDs.forEach(lotId -> {
            List<ObjectIdentifier> cassetteId = Collections.emptyList();
            try {
                log.debug("Getting Cassette Information of Lot -> " + ObjectIdentifier.fetchValue(lotId));
                cassetteId = lotMethod.lotCassetteListGet(objCommon, lotId);
            } catch (ServiceException se) {
                if (retCodeConfig.getNotFoundCassette().getCode() != se.getCode()) {
                    throw se;
                }
            }
            cassetteId.stream().filter(id -> cassetteIDs.stream().noneMatch(id::equals)).forEach(cassetteIDs::add);
        });
        log.info("calling object_LockWithoutDoubleLock()");
        objectLockMethod.objectLockWithoutDoubleLock(objCommon, cassetteIDs, BizConstant.SP_CLASSNAME_POSCASSETTE);

        //--------------------------------------------------------
        //  Check Lot Condition
        //--------------------------------------------------------
        log.info("Check Condition of Related Lots");
        lotMethod.lotCheckConditionForBondingGroup(objCommon, BizConstant.SP_BONDINGGROUPACTION_PARTIALRELEASE, lotIDs);

        //--------------------------------------------------------
        //  Check Cassette Condition
        //--------------------------------------------------------
        cassetteMethod.cassetteCheckConditionForBondingGroup(objCommon, cassetteIDs);
        for (Infos.BondingGroupInfo bondingGroupInfo : strBondingGroupInfoSeq) {
            List<Infos.BondingMapInfo> orgnialBondingMapInfoList = bondingGroupInfo.getBondingMapInfoList();
            int bondingWaferCount = orgnialBondingMapInfoList.size();
            //--------------------------------------------------------
            //  Get Bonding Group Information
            //--------------------------------------------------------
            log.info("Get Bonding Group Information");
            List<Infos.BondingGroupReleaseLotWafer> tmpGrpInfo = new ArrayList<>(lotIDs.size());
            strBondingGroupReleaseLotWaferSeq.stream()
                    .filter(info -> CimStringUtils.equals(info.getBondingGroupID(), bondingGroupInfo.getBondingGroupID()))
                    .forEach(tmpGrpInfo::add);
            Outputs.BondingMapInfoConsistencyCheckForPartialReleaseOut checkForPartialReleaseOut = bondingMapMethod
                    .bondingMapInfoConsistencyCheckForPartialRelease(objCommon, bondingGroupInfo.getBondingMapInfoList(), tmpGrpInfo);

            //--------------------------------------------------------
            //  Update Bonding Group Information
            //--------------------------------------------------------
            log.info("Calling bondingGroup_info_UpdateDR()");
            bondingGroupInfo.setClaimMemo(claimMemo);
            bondingGroupInfo.setBondingMapInfoList(checkForPartialReleaseOut.getStrPartialReleaseDestinationMapSeq());
            bondingGroupMethod.bondingGroupInfoUpdateDR(objCommon, BizConstant.SP_BONDINGGROUPACTION_PARTIALRELEASE, bondingGroupInfo);

            //--------------------------------------------------------
            //  Make Event for Bonding Group Information Update
            //--------------------------------------------------------
            eventMethod.bondingGroupEventMake(objCommon, BizConstant.SP_BONDINGGROUPACTION_PARTIALRELEASE,
                    bondingGroupInfo, checkForPartialReleaseOut.getStrPartialReleaseSourceMapSeq());

            //--------------------------------------------------------
            //  Split Lot
            //--------------------------------------------------------
            log.info("Lot Splitting()");
            List<Infos.BondingGroupReleaseLotWafer> bondingGroupReleaseLotWaferSeq = checkForPartialReleaseOut.getStrBondingGroupReleaseLotWaferSeq();
            Infos.BondingGroupReleaseLotWafer bondingGroupReleaseLotWafer1 = bondingGroupReleaseLotWaferSeq.get(0);
            List<ObjectIdentifier> childWaferIDSeq = bondingGroupReleaseLotWafer1.getChildWaferIDSeq();
            boolean allRelease = false;
            if (childWaferIDSeq.size() == bondingWaferCount){
                allRelease = true;
            }
            if (!allRelease && CimArrayUtils.isNotEmpty(bondingGroupReleaseLotWaferSeq)) {
                for (Infos.BondingGroupReleaseLotWafer bondingGroupReleaseLotWafer : bondingGroupReleaseLotWaferSeq) {
                    ObjectIdentifier parentLotID = bondingGroupReleaseLotWafer.getParentLotID();
                    log.debug("Get Lot Hold State -> " + ObjectIdentifier.fetchValue(parentLotID));
                    String lotHoldState = lotMethod.lotHoldStateGet(objCommon, parentLotID);
                    log.debug("Lot State -> " + lotHoldState);
                    List<Infos.LotHoldReq> strHoldListSeq = Collections.emptyList();
                    if (CimStringUtils.equals(lotHoldState, BizConstant.CIMFW_LOT_HOLDSTATE_ONHOLD)) {
                        log.info(ObjectIdentifier.fetchValue(parentLotID) + " is held");
                        List<Infos.LotHoldListAttributes> lotHoldListAttributes = lotInqService.sxHoldLotListInq(objCommon, parentLotID);
                        List<Infos.LotHoldReq> holdListSeq = new ArrayList<>(lotHoldListAttributes.size());
                        if (CimArrayUtils.isNotEmpty(lotHoldListAttributes)) {
                            lotHoldListAttributes.stream().filter(att -> {
                                String reasonCodeId = ObjectIdentifier.fetchValue(att.getReasonCodeID());
                                boolean flag = CimStringUtils.equals(reasonCodeId, BizConstant.SP_REASON_LOTLOCK);
                                if (flag) {
                                    log.info("Lock Hold cannot be inherited");
                                }
                                return !flag;
                            }).forEach(att -> {
                                Infos.LotHoldReq lotHoldReq = new Infos.LotHoldReq();
                                lotHoldReq.setHoldType(att.getHoldType());
                                lotHoldReq.setHoldReasonCodeID(att.getReasonCodeID());
                                lotHoldReq.setHoldUserID(att.getUserID());
                                lotHoldReq.setResponsibleOperationMark(att.getResponsibleOperationMark());
                                lotHoldReq.setRouteID(att.getResponsibleRouteID());
                                lotHoldReq.setOperationNumber(att.getResponsibleOperationNumber());
                                lotHoldReq.setRelatedLotID(att.getRelatedLotID());
                                lotHoldReq.setClaimMemo(att.getClaimMemo());
                                holdListSeq.add(lotHoldReq);
                            });
                        }
                        strHoldListSeq = holdListSeq;
                    }

                    /*------------------------------------------------------------------------*/
                    /*   Change State                                                         */
                    /*------------------------------------------------------------------------*/
                    log.info("Calling lot_SplitWaferLot()");
                    ObjectIdentifier childLotID = lotMethod.lotSplitWaferLot(objCommon, parentLotID,
                            bondingGroupReleaseLotWafer.getChildWaferIDSeq());

                    //inherit the contamination flag from parent lot
                    contaminationMethod.inheritContaminationFlagFromParentLot(parentLotID,childLotID);
                    contaminationMethod.lotCheckContaminationLevelStepOut(objCommon,childLotID);

                    //--------------------------------------------------------------------------------------------------
                    // UpDate RequiredCassetteCategory
                    //--------------------------------------------------------------------------------------------------
                    log.info("Calling lot_CassetteCategory_UpdateForContaminationControl()");
                    lotMethod.lotCassetteCategoryUpdateForContaminationControl(objCommon, childLotID);

                    //------------------------------------//
                    //     Process Hold for Child Lot     //
                    //------------------------------------//
                    log.info("Calling txProcessHoldExecReq()");
                    processControlService.sxProcessHoldDoActionReq(objCommon, childLotID, claimMemo);

                    /*------------------------------------------------------------------------*/
                    /*   Make History                                                         */
                    /*------------------------------------------------------------------------*/
                    log.info("Calling lot_waferLotHistoryPointer_Update()");
                    lotMethod.lotWaferLotHistoryPointerUpdate(objCommon, parentLotID);
                    lotMethod.lotWaferLotHistoryPointerUpdate(objCommon, childLotID);

                    //-----------------------------------------------------
                    // Prepare input parameter of lotWaferMoveEvent_Make()
                    //-----------------------------------------------------
                    List<Infos.LotWaferMap> lotWaferMaps = lotMethod.lotWaferMapGet(objCommon, childLotID);
                    Infos.NewLotAttributes newLotAttributes = new Infos.NewLotAttributes();
                    newLotAttributes.setCassetteID(lotWaferMaps.get(0).getCassetteID());
                    List<Infos.NewWaferAttributes> newWaferAttributes = new ArrayList<>(lotWaferMaps.size());
                    newLotAttributes.setNewWaferAttributesList(newWaferAttributes);
                    lotWaferMaps.forEach(wafer -> {
                        Infos.NewWaferAttributes newWaferAttribute = new Infos.NewWaferAttributes();
                        newWaferAttribute.setNewLotID(childLotID);
                        newWaferAttribute.setNewWaferID(wafer.getWaferID());
                        newWaferAttribute.setNewSlotNumber(CimNumberUtils.intValue(wafer.getSlotNumber()));
                        newWaferAttribute.setSourceLotID(parentLotID);
                        newWaferAttribute.setSourceWaferID(wafer.getWaferID());
                        newWaferAttributes.add(newWaferAttribute);
                    });

                    //-----------------------------------------------------------
                    // Inherits Hold Records to Child Lot
                    //-----------------------------------------------------------
                    if (CimArrayUtils.isNotEmpty(strHoldListSeq)) {
                        log.info("Lot's inventory state is not 'InBank'. So, txHoldLotReq() is called");
                        lotService.sxHoldLotReq(objCommon, childLotID, strHoldListSeq);
                    }
                    eventMethod.lotWaferMoveEventMake(objCommon, newLotAttributes,
                            TransactionIDEnum.BONDING_GROUP_PARTIAL_REMOVE_REQ.getValue(), claimMemo);
                }
            }
        }
        cassetteIDs.forEach(cassetteID -> cassetteMethod.cassetteMultiLotTypeUpdate(objCommon, cassetteID));

        // No return value populated?
        return Collections.emptyList();
    }
}
