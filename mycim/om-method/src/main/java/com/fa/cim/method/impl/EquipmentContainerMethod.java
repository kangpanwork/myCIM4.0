package com.fa.cim.method.impl;

import com.fa.cim.annotaion.OmMethod;
import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.OmCode;
import com.fa.cim.common.support.User;
import com.fa.cim.common.utils.*;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.config.RetCodeConfigEx;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Inputs;
import com.fa.cim.method.*;
import com.fa.cim.newcore.bo.durable.CimCassette;
import com.fa.cim.newcore.bo.machine.CimMachine;
import com.fa.cim.newcore.bo.machine.CimMachineContainer;
import com.fa.cim.newcore.bo.machine.CimMachineContainerPosition;
import com.fa.cim.newcore.bo.machine.CimPortResource;
import com.fa.cim.newcore.bo.person.CimPerson;
import com.fa.cim.newcore.bo.product.CimControlJob;
import com.fa.cim.newcore.bo.product.CimLot;
import com.fa.cim.newcore.bo.product.CimWafer;
import com.fa.cim.newcore.dto.machine.MachineDTO;
import com.fa.cim.newcore.dto.product.ProductDTO;
import com.fa.cim.newcore.factory.BaseCoreFactory;
import com.fa.cim.newcore.standard.mchnmngm.PortResource;
import com.fa.cim.newcore.standard.prdctmng.Lot;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * <p>EquipmentContainerMethod .
 * change history:
 * date                      defect#             person            comments
 * -------------------------------------------------------------------------------------------------------------------
 * 2020/5/8 15:53         ********              ZQI             create file.
 *
 * @author ZQI
 * @version 1.0
 * @date 2020/5/8 15:53
 * @copyright 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 * @since JDK1.8
 */
@Slf4j
@OmMethod
public class EquipmentContainerMethod implements IEquipmentContainerMethod {

    @Autowired
    private RetCodeConfig retCodeConfig;

    @Autowired
    private BaseCoreFactory baseCoreFactory;

    @Autowired
    private IWaferMethod waferMethod;

    @Autowired
    private ILotMethod lotMethod;

    @Autowired
    private IControlJobMethod controlJobMethod;

    @Autowired
    private RetCodeConfigEx retCodeConfigEx;

    @Autowired
    private IEquipmentContainerPositionMethod equipmentContainerPositionMethod;

    @Autowired
    private IEquipmentMethod equipmentMethod;

    @Autowired
    private ICassetteMethod cassetteMethod;

    @Autowired
    private IEquipmentContainerMethod equipmentContainerMethod;

    @Autowired
    private IEventMethod eventMethod;

    @Override
    public void equipmentContainerWaferStore(Infos.ObjCommon objCommon, Inputs.ObjEquipmentContainerWaferStoreIn equipmentContainerWaferStoreIn) {
        Validations.check(null == objCommon || null == equipmentContainerWaferStoreIn, retCodeConfig.getInvalidInputParam());
        ObjectIdentifier equipmentID = equipmentContainerWaferStoreIn.getEquipmentID();
        ObjectIdentifier controlJobID = equipmentContainerWaferStoreIn.getControlJobID();
        String processJobID = equipmentContainerWaferStoreIn.getProcessJobID();
        List<Infos.SlmSlotMap> strSLMSlotMapSeq = equipmentContainerWaferStoreIn.getStrSLMSlotMapSeq();

        log.info("in-parm equipmentID : " + ObjectIdentifier.fetchValue(equipmentID));
        log.info("in-parm controlJobID: " + ObjectIdentifier.fetchValue(controlJobID));
        log.info("in-parm processJobID: " + processJobID);

        CimMachine aMachine = baseCoreFactory.getBO(CimMachine.class, equipmentID);
        Validations.check(null == aMachine, retCodeConfig.getNotFoundEqp());

        //----------------------------------------------------------------
        // Change SLMState of equipment container positions
        //----------------------------------------------------------------
        CimPerson aPosPerson = baseCoreFactory.getBO(CimPerson.class, objCommon.getUser().getUserID());
        Validations.check(null == aPosPerson, retCodeConfig.getNotFoundPerson());

        Optional.ofNullable(strSLMSlotMapSeq).ifPresent(slmSlotMaps -> slmSlotMaps.forEach(slmSlotMap -> {
            CimMachineContainerPosition aEqpContPos = aMachine.findMachineContainerPositionForWafer(slmSlotMap.getWaferID().getValue());
            Validations.check(null == aEqpContPos, retCodeConfig.getNotFoundEquipmentContainer());
            aEqpContPos.setSLMState(BizConstant.SP_SLMSTATE_STORED);
            aEqpContPos.setLastClaimedTimeStamp(objCommon.getTimeStamp().getReportTimeStamp());
            aEqpContPos.setLastClaimedUser(aPosPerson);
        }));

        //----------------------------------------------------------------
        // Get lot list without duplication
        //----------------------------------------------------------------
        log.info("Get lot list without duplication");
        List<ObjectIdentifier> storeCastIDs = new ArrayList<>();
        List<ObjectIdentifier> storeLotIDs = new ArrayList<>();
        Optional.ofNullable(strSLMSlotMapSeq).ifPresent(slmSlotMaps -> slmSlotMaps.forEach(slmSlotMap -> {
            //  Get Lot from Wafer
            ObjectIdentifier lotGet = waferMethod.waferLotGet(objCommon, slmSlotMap.getWaferID());
            boolean bFoundLot = false;
            for (ObjectIdentifier storeLotID : storeLotIDs) {
                if (ObjectIdentifier.equalsWithValue(lotGet, storeLotID)) {
                    bFoundLot = true;
                    break;
                }
            }
            if (!bFoundLot) {
                storeLotIDs.add(lotGet);
                storeCastIDs.add(slmSlotMap.getCassetteID());
            }
        }));

        //----------------------------------------------------------------
        // Get Equipment Container Information
        //----------------------------------------------------------------
        Infos.EqpContainerInfo eqpContainerInfo = this.equipmentContainerInfoGet(objCommon, equipmentID);
        if (null == eqpContainerInfo || CimArrayUtils.getSize(eqpContainerInfo.getEqpContainerList()) == 0) {
            Validations.check(retCodeConfig.getNotFoundEquipmentContainerPosition());
        }
        assert eqpContainerInfo != null;
        List<Infos.EqpContainer> strEqpContSeq = eqpContainerInfo.getEqpContainerList();
        List<Infos.EqpContainerPosition> eqpContainerPositions = strEqpContSeq.get(0).getEqpContainerPosition();

        //----------------------------------------------------------------
        // If all Wafers of Lot go into equipment,
        //     cut the relation between Cassette and Lot.
        //----------------------------------------------------------------
        log.info("cut the relation between Cassette and Lot");
        List<ObjectIdentifier> removeLotIDs = new ArrayList<>();

        String transactionID = objCommon.getTransactionID();
        int size = CimArrayUtils.getSize(storeLotIDs);
        for (int nCstLot = 0; nCstLot < size; nCstLot++) {
            ObjectIdentifier storeLotID = storeLotIDs.get(nCstLot);
            ObjectIdentifier storeCastID = storeCastIDs.get(nCstLot);

            boolean bWaferInContPos = false;
            if (CimStringUtils.equals(transactionID, TransactionIDEnum.PARTIAL_OPERATION_COMP_WITH_DATA_REQ.getValue())
                    || CimStringUtils.equals(transactionID, TransactionIDEnum.PARTIAL_OPERATION_COMP_FOR_INTERNAL_BUFFER_REQ.getValue())) {
                CimLot aPosLot = baseCoreFactory.getBO(CimLot.class, storeLotID);
                Validations.check(null == aPosLot, retCodeConfig.getNotFoundLot());

                List<ProductDTO.WaferInfo> allWaferInfo = aPosLot.getAllWaferInfo();
                for (ProductDTO.WaferInfo waferInfo : allWaferInfo) {
                    bWaferInContPos = false;
                    for (Infos.EqpContainerPosition strCtnPstInfo : eqpContainerPositions) {
                        if (ObjectIdentifier.equalsWithValue(strCtnPstInfo.getWaferID(), waferInfo.getWaferID())
                                && (CimStringUtils.equals(strCtnPstInfo.getFmcState(), BizConstant.SP_SLMSTATE_STORED)
                                || CimStringUtils.equals(strCtnPstInfo.getFmcState(), BizConstant.SP_SLMSTATE_RETRIEVED))) {
                            log.info("bWaferInContPos = TRUE");
                            bWaferInContPos = true;
                            break;
                        }
                    }
                    if (!bWaferInContPos) {
                        break;
                    }
                }
            } else {
                Inputs.ObjLotWaferIDListGetDRIn objLotWaferIDListGetDRIn = new Inputs.ObjLotWaferIDListGetDRIn();
                objLotWaferIDListGetDRIn.setLotID(storeLotID);
                objLotWaferIDListGetDRIn.setScrapCheckFlag(true);
                List<ObjectIdentifier> waferIDListGetDR = lotMethod.lotWaferIDListGetDR(objCommon, objLotWaferIDListGetDRIn);
                for (ObjectIdentifier waferIDGet : waferIDListGetDR) {
                    bWaferInContPos = false;
                    for (Infos.EqpContainerPosition strCtnPstInfo : eqpContainerPositions) {
                        if (ObjectIdentifier.equalsWithValue(strCtnPstInfo.getWaferID(), waferIDGet)
                                && (CimStringUtils.equals(strCtnPstInfo.getFmcState(), BizConstant.SP_SLMSTATE_STORED)
                                || CimStringUtils.equals(strCtnPstInfo.getFmcState(), BizConstant.SP_SLMSTATE_RETRIEVED))) {
                            bWaferInContPos = true;
                            break;
                        }
                    }
                    if (!bWaferInContPos) {
                        break;
                    }
                }
            }

            if (!bWaferInContPos) {
                // All Wafers of this Lot are NOT contained in EQPConPos yet.
                log.info("All Wafers of this Lot are NOT contained in EQPConPos yet.");
                continue;
            }

            log.info("cut the relation between Cassette and Lot.");
            removeLotIDs.add(storeLotID);

            CimLot aLot = baseCoreFactory.getBO(CimLot.class, storeLotID);
            Validations.check(null == aLot, retCodeConfig.getNotFoundLot());

            CimCassette aCassette = baseCoreFactory.getBO(CimCassette.class, storeCastID);
            Validations.check(null == aCassette, retCodeConfig.getNotFoundCassette());

            // OMCARRIER_LOT
            aCassette.unloadLot(aLot);

            // Get FREQP_CAST information
            MachineDTO.MachineCassette updatedCassetteInfo = aMachine.findCassetteNamed(storeCastID.getValue());
            List<MachineDTO.MachineCassetteLot> tmpLotList = new ArrayList<>();
            if (null != updatedCassetteInfo) {
                Optional.ofNullable(updatedCassetteInfo.getMachineCassetteLots()).ifPresent(machineCassetteLots -> machineCassetteLots.forEach(machineCassetteLot -> {
                    if (ObjectIdentifier.equalsWithValue(machineCassetteLot.getLotID(), storeLotID)) {
                        log.info(String.format("Remove this lot[%s] from OMEQP_CARRIER_LOT.", storeCastID.getValue()));
                    } else {
                        tmpLotList.addAll(updatedCassetteInfo.getMachineCassetteLots());
                    }
                }));
                updatedCassetteInfo.setMachineCassetteLots(tmpLotList);
                aMachine.updateCassette(updatedCassetteInfo);
            }

            // Update OMEQP_LOT info (clear UnloadCassette, UnloadPort)
            MachineDTO.MachineLot processingLot = aMachine.findProcessingLotNamed(storeLotID.getValue());
            Validations.check(null == processingLot, retCodeConfig.getNotFoundLotInProcessLot());

            aMachine.removeProcessingLot(aLot);

            processingLot.setUnloadCassetteID(null);
            processingLot.setUnloadPortID(null);
            aMachine.addProcessingLot(processingLot);
        }

        //----------------------------------------------------------------
        // Update FRCTRLJOB_CAST_LOT
        //----------------------------------------------------------------
        log.info("Update OMCJ_CARRIERL_LOT");
        CimControlJob aControlJob = baseCoreFactory.getBO(CimControlJob.class, controlJobID);
        Validations.check(null == aControlJob, retCodeConfig.getNotFoundControlJob());

        List<ProductDTO.PosStartCassetteInfo> updateStartCastSeq = aControlJob.getStartCassetteInfo();
        for (ProductDTO.PosStartCassetteInfo startCassetteInfo : updateStartCastSeq) {
            List<ProductDTO.PosLotInCassetteInfo> tmpLotSeq = new ArrayList<>();
            List<ProductDTO.PosLotInCassetteInfo> lotInCassetteInfos = startCassetteInfo.getLotInCassetteInfo();

            for (ProductDTO.PosLotInCassetteInfo lotInCassetteInfo : lotInCassetteInfos) {
                boolean bRemoveLot = false;
                for (ObjectIdentifier removeLotID : removeLotIDs) {
                    if (ObjectIdentifier.equalsWithValue(removeLotID, lotInCassetteInfo.getLotID())) {
                        log.info(String.format("Remove lot[%s] from OMCJ_CARRIERL_LOT", lotInCassetteInfo.getLotID().getValue()));
                        bRemoveLot = true;
                        break;
                    }
                }
                if (!bRemoveLot) {
                    tmpLotSeq.add(lotInCassetteInfo);
                    break;
                }
            }
            startCassetteInfo.setLotInCassetteInfo(tmpLotSeq);
        }
        aControlJob.setStartCassetteInfo(updateStartCastSeq);
    }

    @Override
    public Infos.EqpContainerInfo equipmentContainerInfoGet(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID) {
        log.info("");
        log.info("+++++ in para equipmentID: " + ObjectIdentifier.fetchValue(equipmentID));
        Infos.EqpContainerInfo retVal = new Infos.EqpContainerInfo();
        retVal.setEquipmentID(equipmentID);

        // Get Machine Object
        CimMachine aMachine = baseCoreFactory.getBO(CimMachine.class, equipmentID);
        Validations.check(null == aMachine, retCodeConfig.getNotFoundMachine());

        List<CimMachineContainer> aEqpContSeq = aMachine.allMachineContainers();
        if (CimArrayUtils.getSize(aEqpContSeq) == 0) {
            return retVal;
        }

        //Equipment Container
        List<Infos.EqpContainer> eqpContainersList = new ArrayList<>();
        for (CimMachineContainer machineContainer : aEqpContSeq) {
            Infos.EqpContainer eqpContainer = new Infos.EqpContainer();
            //Equipment Container Position
            List<CimMachineContainerPosition> aEqpContPosSeq = machineContainer.allMachineContainerPositions();
            if (CimArrayUtils.getSize(aEqpContPosSeq) == 0) {
                continue;
            }
            List<Infos.EqpContainerPosition> eqpContainerPositionsList = new ArrayList<>();
            for (CimMachineContainerPosition eqpctnpst : aEqpContPosSeq) {
                //---------------------
                // Set Posision Info
                //---------------------
                CimWafer wafer = eqpctnpst.getWafer();
                if (null == wafer) {
                    continue;
                }

                Lot lot = wafer.getLot();
                ObjectIdentifier tmpLotID = new ObjectIdentifier();
                if (null != lot) {
                    tmpLotID = new ObjectIdentifier(lot.getIdentifier(), lot.getPrimaryKey());
                }
                MachineDTO.MachineContainerPositionInfo machineContainerPositionInfo = eqpctnpst.getMachineContainerPositionInfo();

                Infos.EqpContainerPosition eqpContainerPosition = new Infos.EqpContainerPosition();
                eqpContainerPosition.setContainerPositionID(machineContainerPositionInfo.getMachineContainerPosition());
                eqpContainerPosition.setControlJobID(machineContainerPositionInfo.getControlJob());
                eqpContainerPosition.setProcessJobID(machineContainerPositionInfo.getProcessJob());
                eqpContainerPosition.setLotID(tmpLotID);
                eqpContainerPosition.setWaferID(machineContainerPositionInfo.getWafer());
                eqpContainerPosition.setSrcCassetteID(machineContainerPositionInfo.getSrcCassette());
                eqpContainerPosition.setSrcPortID(machineContainerPositionInfo.getSrcPort());
                eqpContainerPosition.setSrcSlotNo(CimNumberUtils.intValue(machineContainerPositionInfo.getSrcSlotNo()));
                eqpContainerPosition.setFmcState(machineContainerPositionInfo.getSLMState());
                eqpContainerPosition.setDestCassetteID(machineContainerPositionInfo.getDestCassette());
                eqpContainerPosition.setDestPortID(machineContainerPositionInfo.getDestPort());
                eqpContainerPosition.setDestSlotNo(CimNumberUtils.intValue(machineContainerPositionInfo.getDestSlotNo()));
                eqpContainerPosition.setEstimatedProcessEndTime(CimDateUtils.convertTo(machineContainerPositionInfo.getEstimatedEndTimeStamp()));
                eqpContainerPosition.setProcessStartTime(CimDateUtils.convertTo(machineContainerPositionInfo.getProcessStartTimeStamp()));
                eqpContainerPosition.setLastClaimedTimeStamp(CimDateUtils.convertTo(machineContainerPositionInfo.getLastClaimedTimeStamp()));
                eqpContainerPosition.setLastClaimedUserID(machineContainerPositionInfo.getLastClaimedUser());
                eqpContainerPositionsList.add(eqpContainerPosition);
            }

            //-----------------------------
            // Set Container Info
            //-----------------------------
            MachineDTO.MachineContainerInfo machineContainerInfo = machineContainer.getMachineContainerInfo();
            eqpContainer.setEqpContainerPosition(eqpContainerPositionsList);
            eqpContainer.setEquipmentContainerID(machineContainerInfo.getMachineContainer());
            eqpContainer.setChamberID(machineContainerInfo.getChamber());
            eqpContainer.setMaxCapacity(machineContainerInfo.getMaxCapacity());
            eqpContainer.setMaxRsvCount(machineContainerInfo.getMaxReserveCount());
            eqpContainer.setCurrentCapacity((long) aEqpContPosSeq.size());
            eqpContainersList.add(eqpContainer);
        }
        retVal.setEqpContainerList(eqpContainersList);
        return retVal;
    }

    @Override
    public void equipmentContainerWaferRetrieve(Infos.ObjCommon objCommon, Inputs.ObjEquipmentContainerWaferRetrieveIn in) {
        User user = objCommon.getUser();
        CimPerson cimPerson = baseCoreFactory.getBO(CimPerson.class, user.getUserID());
        Validations.check(cimPerson == null, retCodeConfig.getNotFoundPerson());
        ObjectIdentifier equipmentID = in.getEquipmentID();
        ObjectIdentifier controlJobID = in.getControlJobID();
        String processJobID = in.getProcessJobID();
        List<Infos.SlmSlotMap> strSLMSlotMapSeq = in.getStrSLMSlotMapSeq();

        /*---------------------------*/
        /*   Check input parameter   */
        /*---------------------------*/
        Validations.check(CimArrayUtils.isEmpty(strSLMSlotMapSeq), retCodeConfig.getNoSlotMapInFormAtion());

        String controlJobId = ObjectIdentifier.fetchValue(controlJobID);
        /*--------------------------------------*/
        /*   Get Lot Information in ControlJob  */
        /*--------------------------------------*/
        boolean hasControlJob = CimStringUtils.isNotEmpty(controlJobId);
        List<Infos.ControlJobLot> controlJobLots = hasControlJob ?
                controlJobMethod.controlJobLotListGet(objCommon, controlJobID) :
                Collections.emptyList();
        Validations.check(hasControlJob && CimArrayUtils.isEmpty(controlJobLots), retCodeConfig.getNotFoundLotInControlJob());

        /*-----------------------------------*/
        /*   Get Lot Sequence from SlotMap   */
        /*-----------------------------------*/
        Set<ObjectIdentifier> retrieveLotIDs = new HashSet<>(strSLMSlotMapSeq.size());
        strSLMSlotMapSeq.forEach(map -> {
            ObjectIdentifier waferID = map.getWaferID();
            ObjectIdentifier retrieveLotID = waferMethod.waferLotGet(objCommon, waferID);
            retrieveLotIDs.add(retrieveLotID);
        });

        /*-------------------------------------*/
        /*   Update SLM State to "Retrieved"   */
        /*-------------------------------------*/
        CimMachine cimMachine = baseCoreFactory.getBO(CimMachine.class, equipmentID);
        strSLMSlotMapSeq.stream().map(map -> ObjectIdentifier.fetchValue(map.getWaferID())).forEach(waferId -> {
            CimMachineContainerPosition aEqpContPos = cimMachine.findMachineContainerPositionForWafer(waferId);
            aEqpContPos.setSLMState(BizConstant.SP_SLMSTATE_RETRIEVED);
        });

        /*------------------------------------------------------------------------------------*/
        /*   Checks are done for every lot. If the result is OK, the cassette are attached.   */
        /*------------------------------------------------------------------------------------*/
        for (ObjectIdentifier retrieveLotID : retrieveLotIDs) {
            /*-----------------------------------------------------------------------------*/
            /*   Check Conditions for attaching cassette                                   */
            /*       Attached Conditions                                                   */
            /*           - all wafers' SLM state in lot are "Retrieved".                   */
            /*           - all wafers' destination cassette are the same.                  */
            /*           - all wafers' destination port are the same (if they were set).   */
            /*-----------------------------------------------------------------------------*/
            /*------------------------------*/
            /*   Get Wafer Map from LotID   */
            /*------------------------------*/
            String transactionId = objCommon.getTransactionID();
            List<ObjectIdentifier> waferIDs;
            if (CimStringUtils.equalsIn(transactionId,
                    TransactionIDEnum.PARTIAL_OPERATION_COMP_WITH_DATA_REQ.getValue(),
                    TransactionIDEnum.PARTIAL_OPERATION_COMP_FOR_INTERNAL_BUFFER_REQ.getValue())) {

                CimLot cimLot = baseCoreFactory.getBO(CimLot.class, retrieveLotID);
                List<ProductDTO.WaferInfo> allWaferInfo = cimLot.getAllWaferInfo();
                waferIDs = new ArrayList<>(allWaferInfo.size());
                allWaferInfo.stream().map(ProductDTO.WaferInfo::getWaferID).forEach(waferIDs::add);
            } else {
                waferIDs = lotMethod.lotWaferIDListGetDR(objCommon, new Inputs.ObjLotWaferIDListGetDRIn(retrieveLotID, false));
            }

            boolean allWaferRetrieveFlag = true;
            ObjectIdentifier destCassetteID = ObjectIdentifier.emptyIdentifier();
            ObjectIdentifier destPortID = ObjectIdentifier.emptyIdentifier();
            for (ObjectIdentifier waferID : waferIDs) {
                String waferId = ObjectIdentifier.fetchValue(waferID);
                Infos.EqpContainerPositionInfo eqpContainerPositionInfo = equipmentContainerPositionMethod
                        .equipmentContainerPositionInfoGet(objCommon, new Inputs.ObjEquipmentContainerPositionInfoGetIn(
                                equipmentID, waferID, BizConstant.SP_SLM_KEYCATEGORY_WAFER));
                List<Infos.EqpContainerPosition> eqpContainerPositionList = eqpContainerPositionInfo.getEqpContainerPositionList();
                Validations.check(CimArrayUtils.isEmpty(eqpContainerPositionList), retCodeConfigEx.getSlmWaferNotFoundInPosition(), waferId);
                Infos.EqpContainerPosition eqpContainerPosition = eqpContainerPositionList.get(0);
                String slmState = eqpContainerPosition.getFmcState();
                if (!CimStringUtils.equals(slmState, BizConstant.SP_SLMSTATE_RETRIEVED)) {
                    allWaferRetrieveFlag = false;
                    break;
                }

                if (ObjectIdentifier.isEmpty(destCassetteID)) {
                    destCassetteID.set(eqpContainerPosition.getDestCassetteID());
                } else {
                    Validations.check(!destCassetteID.equals(eqpContainerPosition.getDestCassetteID()), retCodeConfig.getAllWaferDestCastNotSame());
                }

                if (ObjectIdentifier.isEmpty(destPortID)) {
                    destPortID.set(eqpContainerPosition.getDestPortID());
                } else {
                    Validations.check(!destPortID.equals(eqpContainerPosition.getDestPortID()), retCodeConfig.getAllWaferDestPortNotSame());
                }

            }

            if (!allWaferRetrieveFlag) {
                continue;
            }

            /*--------------------------------*/
            /*   Attache the cassette starts  */
            /*--------------------------------*/
            String destCassettId = ObjectIdentifier.fetchValue(destCassetteID);
            log.debug("Attach the cassette starts. CassetteID = " + destCassettId);

            /*-------------------------------------------------------*/
            /*   Get the port where destination cassette is loaded   */
            /*-------------------------------------------------------*/
            Infos.EqpPortInfo eqpPortInfo = equipmentMethod.equipmentPortInfoGet(objCommon, equipmentID);
            List<Infos.EqpPortStatus> eqpPortStatuses = eqpPortInfo.getEqpPortStatuses();
            Optional<Infos.EqpPortStatus> eqpPortStatus = eqpPortStatuses.stream()
                    .filter(info -> destCassetteID.equals(info.getLoadedCassetteID())).findFirst();
            Validations.check(!eqpPortStatus.isPresent(), retCodeConfig.getCastNotLoadedDestPort(),
                    destCassettId,
                    ObjectIdentifier.fetchValue(destPortID));
            Infos.EqpPortStatus retPort = eqpPortStatus.orElse(new Infos.EqpPortStatus());
            boolean opeStartFlag = false;
            boolean monitorLotFlag = false;
            String retrieveLotId = ObjectIdentifier.fetchValue(retrieveLotID);
            if (!ObjectIdentifier.isEmpty(controlJobID)) {
                /*--------------------------------------*/
                /*   Update information on ControlJob   */
                /*--------------------------------------*/
                log.debug("controlJobID = " + controlJobId);
                /*-----------------------------------------*/
                /*   Check if lot is "opestarted" or not   */
                /*-----------------------------------------*/
                opeStartFlag = true;

                CimControlJob cimControlJob = baseCoreFactory.getBO(CimControlJob.class, controlJobID);
                ProductDTO.LotInControlJobInfo controlJobLotNamed = cimControlJob.findControlJobLotNamed(retrieveLotId);
                boolean _monitorLotFlag = CimBooleanUtils.isTrue(controlJobLotNamed.getMonitorLotFlag());
                monitorLotFlag = _monitorLotFlag;
                List<ProductDTO.PosStartCassetteInfo> startCassetteInfo = cimControlJob.getStartCassetteInfo();
                startCassetteInfo.stream().filter(info -> {
                    boolean startCastFoundFlag = destCassetteID.equals(info.getCassetteID());
                    if (startCastFoundFlag) {
                        log.debug("## Start cassette information already exist in control job.");
                        info.getLotInCassetteInfo().add(new ProductDTO.PosLotInCassetteInfo(true, _monitorLotFlag, retrieveLotID));
                    }
                    return startCastFoundFlag;
                }).findFirst().orElseGet(() -> {
                    ProductDTO.PosStartCassetteInfo tmpInfo = new ProductDTO.PosStartCassetteInfo();
                    startCassetteInfo.add(tmpInfo);
                    tmpInfo.setLoadSequenceNumber(retPort.getLoadSequenceNumber());
                    tmpInfo.setCassetteID(destCassetteID);
                    tmpInfo.setLoadPurposeType(retPort.getLoadPurposeType());
                    tmpInfo.setLoadPortID(retPort.getPortID());
                    tmpInfo.setUnloadPortID(retPort.getPortID());
                    List<ProductDTO.PosLotInCassetteInfo> posLotInCassetteInfos = new ArrayList<>();
                    tmpInfo.setLotInCassetteInfo(posLotInCassetteInfos);
                    Infos.LotListInCassetteInfo lotListInCassetteInfo = cassetteMethod.cassetteLotListGetDR(objCommon, destCassetteID);
                    List<ObjectIdentifier> lotIDList = lotListInCassetteInfo.getLotIDList();
                    Optional.ofNullable(lotIDList).ifPresent(tmepLotIdList -> tmepLotIdList.stream().filter(lotID -> controlJobLots.stream().anyMatch(ctrlJInfo -> !lotID.equals(ctrlJInfo.getLotID())))
                            .forEach(lotID -> posLotInCassetteInfos.add(new ProductDTO.PosLotInCassetteInfo(false, false, lotID))));
                    posLotInCassetteInfos.add(new ProductDTO.PosLotInCassetteInfo(true, _monitorLotFlag, retrieveLotID));
                    /*-------------------------------*/
                    /*   Update OMCARRIER information   */
                    /*-------------------------------*/
                    CimCassette cimCassette = baseCoreFactory.getBO(CimCassette.class, destCassetteID);
                    cimCassette.setControlJob(cimControlJob);
                    return tmpInfo;
                });

                /*---------------------------------------*/
                /*   Set Start Cassette to Control Job   */
                /*---------------------------------------*/
                cimControlJob.setStartCassetteInfo(startCassetteInfo);

                /*-----------------------------------*/
                /*   Update FRCTROLJOB information   */
                /*-----------------------------------*/
                cimControlJob.setPortGroup(retPort.getPortGroup());

                /*----------------------------------*/
                /*   Update OMEQP_LOT information   */
                /*----------------------------------*/
                MachineDTO.MachineLot processingLotNamed = cimMachine.findProcessingLotNamed(retrieveLotId);

                CimLot cimLot = baseCoreFactory.getBO(CimLot.class, retrieveLotID);
                cimMachine.removeProcessingLot(cimLot);

                processingLotNamed.setUnloadCassetteID(destCassetteID);
                processingLotNamed.setUnloadPortID(retPort.getPortID());

                cimMachine.addProcessingLot(processingLotNamed);
            }

            /*---------------------------------------*/
            /*   Update FREQP_CAST_LOT information   */
            /*---------------------------------------*/
            MachineDTO.MachineCassette cassetteNamed = cimMachine.findCassetteNamed(destCassettId);
            List<MachineDTO.MachineCassetteLot> machineCassetteLots = cassetteNamed.getMachineCassetteLots();
            machineCassetteLots.add(new MachineDTO.MachineCassetteLot(retrieveLotID, opeStartFlag, monitorLotFlag));

            cimMachine.updateCassette(cassetteNamed);

            /*----------------------------------*/
            /*   Update Cassette_multiLotType   */
            /*----------------------------------*/
            cassetteMethod.cassetteMultiLotTypeUpdate(objCommon, destCassetteID);

            /*-----------------------------------------*/
            /*  Set wafer into destination cassette    */
            /*-----------------------------------------*/
            waferIDs.forEach(waferID -> {
                Infos.EqpContainerPositionInfo eqpContainerPositionInfo = equipmentContainerPositionMethod.equipmentContainerPositionInfoGet(objCommon,
                        new Inputs.ObjEquipmentContainerPositionInfoGetIn(equipmentID, waferID, BizConstant.SP_SLM_KEYCATEGORY_WAFER));
                Infos.EqpContainerPosition eqpContainerPosition = eqpContainerPositionInfo.getEqpContainerPositionList().get(0);
                waferMethod.waferMaterialContainerChange(objCommon, destCassetteID,
                        new Infos.Wafer(eqpContainerPosition.getWaferID(), eqpContainerPosition.getDestSlotNo()));

            });
            /*--------------------------------*/
            /*   Attaching the Cassette Ends  */
            /*--------------------------------*/
            log.debug(String.format("Attaching the cassette ends. CassetteID[%s]/LotID[%s]", destCassettId, retrieveLotId));

            /*------------------------------------------------------------------------*/
            /*   Clear machine container information if input controlJob ID is blank  */
            /*------------------------------------------------------------------------*/
            if (!hasControlJob) {
                log.debug("Input control job is blank. Clears machine contaier position info for this lot. LotID = " + retrieveLotId);
                equipmentContainerPositionMethod.equipmentContainerPositionInfoClear(objCommon,
                        new Inputs.ObjEquipmentContainerPositionInfoClearIn(equipmentID, retrieveLotID, BizConstant.SP_SLM_KEYCATEGORY_LOT));
            }

            //------------------//
            //    Make Event    //
            //------------------//
            Infos.EqpContainerInfo eqpContainerInfo = equipmentContainerMethod.equipmentContainerInfoGet(objCommon, equipmentID);
            List<String> waferIds = waferIDs.stream().map(ObjectIdentifier::getValue).collect(Collectors.toList());
            List<Infos.EqpContainer> oldEqpContSeq = eqpContainerInfo.getEqpContainerList();
            List<Infos.WaferTransfer> strWaferXferSeq = new LinkedList<>();
            AtomicBoolean hisEventMakeFlag = new AtomicBoolean(false);
            oldEqpContSeq.forEach(oldEqpCont -> oldEqpCont.getEqpContainerPosition().stream()
                    .filter(data -> CimStringUtils.equalsIn(ObjectIdentifier.fetchValue(data.getWaferID()), waferIds)).forEach(data -> {
                        hisEventMakeFlag.set(!data.getSrcCassetteID().equals(data.getDestCassetteID()) ||
                                !data.getSrcSlotNo().equals(data.getDestSlotNo()));
                        Infos.WaferTransfer waferTransfer = new Infos.WaferTransfer();
                        strWaferXferSeq.add(waferTransfer);
                        waferTransfer.setWaferID(data.getWaferID());
                        waferTransfer.setDestinationCassetteID(data.getDestCassetteID());
                        waferTransfer.setBDestinationCassetteManagedByOM(true);
                        waferTransfer.setDestinationSlotNumber(data.getDestSlotNo());
                        waferTransfer.setOriginalCassetteID(data.getSrcCassetteID());
                        waferTransfer.setBOriginalCassetteManagedByOM(true);
                        waferTransfer.setOriginalSlotNumber(data.getSrcSlotNo());
                    }));

            if (hisEventMakeFlag.get() && CimArrayUtils.isNotEmpty(strWaferXferSeq)) {
                eventMethod.lotWaferSortEventMake(objCommon, TransactionIDEnum.FMC_WAFER_RETRIEVE_RPT.getValue(),
                        strWaferXferSeq, CimStringUtils.EMPTY);
            }

            /*---------------------------------------------------------------------*/
            /*   Check if the machine container position information is cleared.   */
            /*   This check is done only when input control job isn't blank.       */
            /*       Clear Condition                                               */
            /*           - All wafers' SLM State in ControlJob is "Retrieved".     */
            /*---------------------------------------------------------------------*/
            if (hasControlJob) {
                List<ObjectIdentifier> eqpCtnPsnList = new ArrayList<>();
                Infos.EqpContainerPositionInfo eqpContainerPositionInfo = equipmentContainerPositionMethod
                        .equipmentContainerPositionInfoGet(objCommon, new Inputs.ObjEquipmentContainerPositionInfoGetIn(
                                equipmentID, controlJobID, BizConstant.SP_SLM_KEYCATEGORY_CONTROLJOB));
                List<Infos.EqpContainerPosition> eqpContainerPositionList = eqpContainerPositionInfo.getEqpContainerPositionList();
                eqpContainerPositionList.forEach(eqpContainerPosition -> eqpCtnPsnList.add(eqpContainerPosition.getContainerPositionID()));
                if (CimStringUtils.equalsIn(transactionId,
                        TransactionIDEnum.PARTIAL_OPERATION_COMP_WITH_DATA_REQ.getValue(),
                        TransactionIDEnum.PARTIAL_OPERATION_COMP_FOR_INTERNAL_BUFFER_REQ.getValue())) {
                    List<Infos.EqpContainerPosition> eqpContainerPositions = eqpContainerPositionList
                            .stream().filter(data -> !ObjectIdentifier.isEmpty(data.getControlJobID()))
                            .collect(Collectors.toList());
                    eqpContainerPositionInfo.setEqpContainerPositionList(eqpContainerPositions);
                }

                boolean clearReqFlag = eqpContainerPositionList.stream()
                        .allMatch(data -> CimStringUtils.equals(data.getFmcState(), BizConstant.SP_SLMSTATE_RETRIEVED));
                if (clearReqFlag) {
                    equipmentContainerPositionMethod.equipmentContainerPositionInfoClear(objCommon,
                            new Inputs.ObjEquipmentContainerPositionInfoClearIn(equipmentID, controlJobID, BizConstant.SP_SLM_KEYCATEGORY_CONTROLJOB));
                    for (ObjectIdentifier eqpCtnPsn : eqpCtnPsnList){
                        CimMachineContainerPosition machineContainerPosition = baseCoreFactory.getBO(CimMachineContainerPosition.class ,eqpCtnPsn);
                        Validations.check(machineContainerPosition == null, retCodeConfig.getNotFoundEquipmentContainerPosition());
                        machineContainerPosition.setLastClaimedTimeStamp(objCommon.getTimeStamp().getReportTimeStamp());
                        machineContainerPosition.setLastClaimedUser(cimPerson);
                    }
                }
            }
        }
        /*----------------------------------*/
        /*   Update LastClaimed User/Time   */
        /*----------------------------------*/
        strSLMSlotMapSeq.stream().map(data -> ObjectIdentifier.fetchValue(data.getWaferID())).forEach(waferId -> {
            CimMachineContainerPosition position = cimMachine.findMachineContainerPositionForWafer(waferId);
            if (!CimObjectUtils.isEmpty(position)) {
                position.setLastClaimedUser(cimPerson);
                position.setLastClaimedTimeStamp(objCommon.getTimeStamp().getReportTimeStamp());
            }
        });
    }

    @Override
    public void equipmentContainerReservationUpdate(Infos.ObjCommon objCommon, Inputs.ObjEquipmentContainerReservationUpdateIn objEquipmentContainerReservationUpdateIn) {
        String IN_actionCode = objEquipmentContainerReservationUpdateIn.getActionCode();
        ObjectIdentifier IN_cassetteID = objEquipmentContainerReservationUpdateIn.getCassetteID();
        ObjectIdentifier IN_destPortID = objEquipmentContainerReservationUpdateIn.getDesPortID();
        ObjectIdentifier IN_equipmentID = objEquipmentContainerReservationUpdateIn.getEquipmentID();
        List<ObjectIdentifier> IN_preCassetteIDs = objEquipmentContainerReservationUpdateIn.getPreCassetteIDs();
        List<Infos.EqpContainerPosition> IN_eqpContainerPositionSeq = objEquipmentContainerReservationUpdateIn.getStrEqpContainerPositionSeq();

        // Get Machine Object
        CimMachine aMachine = baseCoreFactory.getBO(CimMachine.class, IN_equipmentID);
        if (CimStringUtils.equals(IN_actionCode, BizConstant.SP_SLM_ACTIONCODE_CASSETTERESERVE)) {
            for (Infos.EqpContainerPosition IN_eqpContainerPosition : IN_eqpContainerPositionSeq) {
                boolean updateFlag = false;
                CimMachineContainerPosition aPosMachineContainerPosition = aMachine.findMachineContainerPositionForWafer(IN_eqpContainerPosition.getWaferID().getValue());
                if (CimObjectUtils.isEmpty(aPosMachineContainerPosition)) {
                    throw new ServiceException(retCodeConfig.getNotFoundEquipmentContainerPosition());
                }
                CimCassette aPosCassette = null;
                if (!ObjectIdentifier.isEmpty(IN_eqpContainerPosition.getDestCassetteID())) {
                    aPosCassette = baseCoreFactory.getBO(CimCassette.class, IN_eqpContainerPosition.getDestCassetteID());
                }
                //-----------------------------
                //Set Destination Cassette
                //-----------------------------
                CimCassette aDestCast = aPosMachineContainerPosition.getDestCassette();
                if (!CimObjectUtils.isEmpty(aDestCast)) {
                    String destCast = aDestCast.getIdentifier();
                    if (CimStringUtils.equals(destCast, IN_eqpContainerPosition.getDestCassetteID().getValue())) {
                        aPosMachineContainerPosition.setDestCassette(aPosCassette);
                        updateFlag = true;
                    }
                } else {
                    if (!CimObjectUtils.isEmpty(aPosCassette)) {
                        aPosMachineContainerPosition.setDestCassette(aPosCassette);
                        updateFlag = true;
                    }
                }
                //----------------------
                // Port Update
                //----------------------
                CimPortResource aPosPortResource = null;
                if (!ObjectIdentifier.isEmpty(IN_eqpContainerPosition.getDestPortID())) {
                    PortResource portResourceNamed = aMachine.findPortResourceNamed(IN_eqpContainerPosition.getDestPortID().getValue());
                    aPosPortResource = (CimPortResource) portResourceNamed;
                }
                CimPortResource aDestPort = aPosMachineContainerPosition.getDestPort();
                if (!CimObjectUtils.isEmpty(aDestPort)) {
                    String destPort = aDestPort.getIdentifier();
                    if (!CimStringUtils.equals(destPort, IN_eqpContainerPosition.getDestPortID().getValue())) {
                        aPosMachineContainerPosition.setDestPort(aPosPortResource);
                        updateFlag = true;
                    }
                } else {
                    if (!CimObjectUtils.isEmpty(aPosPortResource)) {
                        aPosMachineContainerPosition.setDestPort(aPosPortResource);
                        updateFlag = true;
                    }
                }
                updateFlag = true;
                //-----------------------------
                //Set Destination Slot Number
                //-----------------------------
                int tmpSlot = aPosMachineContainerPosition.getDestSlotNo();
                if (tmpSlot != IN_eqpContainerPosition.getDestSlotNo()) {
                    aPosMachineContainerPosition.setDestSlotNo(IN_eqpContainerPosition.getDestSlotNo());
                    updateFlag = true;
                }
                //---------------------------------
                // Update Reserved Machine
                //---------------------------------
                if (!CimObjectUtils.isEmpty(aPosCassette)) {
                    aPosCassette.setSLMReservedMachine(aMachine);
                }
                //-------------------------
                // Update Last Claimed
                //-------------------------
                if (updateFlag) {
                    aPosMachineContainerPosition.setLastClaimedTimeStamp(CimDateUtils.getCurrentTimeStamp());
                    CimPerson aPerson = baseCoreFactory.getBO(CimPerson.class, objCommon.getUser().getUserID());
                    aPosMachineContainerPosition.setLastClaimedUser(aPerson);
                }
            }
            //----------------------------------------
            // Reset Previous Destination Cassette .
            //----------------------------------------
            CimMachine aEmptyMachine = null;
            for (ObjectIdentifier IN_preCassetteID : IN_preCassetteIDs) {
                CimCassette aPosCassette = baseCoreFactory.getBO(CimCassette.class, IN_preCassetteID);
                aPosCassette.setSLMReservedMachine(aEmptyMachine);
            }
        } else if (CimStringUtils.equals(IN_actionCode, BizConstant.SP_SLM_ACTIONCODE_PORTRESERVE)
                || CimStringUtils.equals(IN_actionCode, BizConstant.SP_SLM_ACTIONCODE_PORTRESERVECANCEL)) {
            List<Infos.EqpContainerPosition> eqpContainerPositionSeq = equipmentContainerPositionMethod.equipmentContainerPositionInfoGetByDestCassette(objCommon, IN_equipmentID, IN_cassetteID);
            for (Infos.EqpContainerPosition eqpContainerPosition : eqpContainerPositionSeq) {
                boolean updateFlag = false;
                CimMachineContainerPosition aPosMachineContainerPosition = aMachine.findMachineContainerPositionForWafer(eqpContainerPosition.getWaferID().getValue());
                if (CimObjectUtils.isEmpty(aPosMachineContainerPosition)) {
                    throw new ServiceException(retCodeConfig.getNotFoundEquipmentContainerPosition());
                }
                if (CimStringUtils.equals(IN_actionCode, BizConstant.SP_SLM_ACTIONCODE_PORTRESERVE)) {
                    //----------------------
                    // Port Reserve
                    //----------------------
                    PortResource aTmpPortResource = aMachine.findPortResourceNamed(IN_destPortID.getValue());
                    CimPortResource aPosPortResource = (CimPortResource) aTmpPortResource;
                    aPosMachineContainerPosition.setDestPort(aPosPortResource);
                    updateFlag = true;
                } else if (CimStringUtils.equals(IN_actionCode, BizConstant.SP_SLM_ACTIONCODE_PORTRESERVECANCEL)) {
                    //----------------------
                    // Port Reserve Cancel
                    //----------------------
                    CimPortResource aTmpPortResource = null;
                    aPosMachineContainerPosition.setDestPort(aTmpPortResource);
                    updateFlag = true;
                }
                //-------------------------
                // Update Last Claimed
                //-------------------------
                if (updateFlag) {
                    aPosMachineContainerPosition.setLastClaimedTimeStamp(CimDateUtils.getCurrentTimeStamp());
                    CimPerson aPerson = baseCoreFactory.getBO(CimPerson.class, objCommon.getUser().getUserID());
                    aPosMachineContainerPosition.setLastClaimedUser(aPerson);
                }
            }
        }
    }

    public void equipmentContainerMaxRsvCountUpdate(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, ObjectIdentifier eqpCtnID, Integer inputMaxRsvCount) {
        //--------------------------------//
        //  Check Requested max capacity  //
        //--------------------------------//
        if (inputMaxRsvCount < 0 || inputMaxRsvCount > 999) {
            throw new ServiceException(retCodeConfigEx.getInvalidMaxrsvcount());
        }
        //--------------------------------------------------------//
        //  Gets the max capacity of the input machine container  //
        //--------------------------------------------------------//
        CimMachine aMachine = baseCoreFactory.getBO(CimMachine.class, equipmentID);
        CimMachineContainer aMachineContainer = aMachine.findMachineContainerNamed(eqpCtnID.getValue());
        if (CimObjectUtils.isEmpty(aMachineContainer)) {
            throw new ServiceException(retCodeConfig.getNotFoundEquipmentContainer());
        }
        int maxCapacity = aMachineContainer.getMaxCapacity();
        log.info("Current Max Capacity -> ", maxCapacity);
        //-----------------------------------------------------------------------------//
        //  Check if input max reserve count is smaller than equipment's max capacity  //
        //-----------------------------------------------------------------------------//
        if (inputMaxRsvCount > maxCapacity) {
            throw new ServiceException(new OmCode(retCodeConfigEx.getMaxrsvcountOverCapacity(), inputMaxRsvCount, maxCapacity));
        }
        //--------------------------//
        //  Change MaxReserveCount  //
        //--------------------------//
        aMachineContainer.setMaxReserveCount(inputMaxRsvCount);

    }
}
