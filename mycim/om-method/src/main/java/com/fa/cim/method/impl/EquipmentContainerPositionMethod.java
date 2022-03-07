package com.fa.cim.method.impl;

import com.fa.cim.annotaion.OmMethod;
import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.OmCode;
import com.fa.cim.common.utils.*;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.config.RetCodeConfigEx;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Inputs;
import com.fa.cim.method.IEquipmentContainerMethod;
import com.fa.cim.method.IEquipmentContainerPositionMethod;
import com.fa.cim.newcore.bo.durable.CimCassette;
import com.fa.cim.newcore.bo.machine.CimMachine;
import com.fa.cim.newcore.bo.machine.CimMachineContainer;
import com.fa.cim.newcore.bo.machine.CimMachineContainerPosition;
import com.fa.cim.newcore.bo.product.CimControlJob;
import com.fa.cim.newcore.bo.product.CimLot;
import com.fa.cim.newcore.bo.product.CimWafer;
import com.fa.cim.newcore.dto.machine.MachineDTO;
import com.fa.cim.newcore.dto.product.ProductDTO;
import com.fa.cim.newcore.factory.BaseCoreFactory;
import com.fa.cim.newcore.standard.prdctmng.Lot;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * <p>EquipmentContainerPositionMethod .
 * change history:
 * date                      defect#             person            comments
 * -------------------------------------------------------------------------------------------------------------------
 * 2020/4/28 17:18         ********              ZQI             create file.
 *
 * @author ZQI
 * @version 1.0
 * @date 2020/4/28 17:18
 * @copyright 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 * @since JDK1.8
 */
@Slf4j
@OmMethod
public class EquipmentContainerPositionMethod implements IEquipmentContainerPositionMethod {

    @Autowired
    private RetCodeConfig retCodeConfig;

    @Autowired
    private RetCodeConfigEx retCodeConfigEx;

    @Autowired
    private BaseCoreFactory baseCoreFactory;

    @Autowired
    private IEquipmentContainerMethod equipmentContainerMethod;

    @Override
    public void equipmentContainerPositionReservationCreate(Infos.ObjCommon objCommon, Inputs.ObjEquipmentContainerPositionReservationCreateIn reservationCreateIn) {
        Validations.check(null == reservationCreateIn, retCodeConfig.getInvalidParameter());

        List<ObjectIdentifier> containerPosIDs = reservationCreateIn.getContainerPositionIDs();
        ObjectIdentifier equipmentID = reservationCreateIn.getEquipmentID();
        ObjectIdentifier controlJobID = reservationCreateIn.getControlJobID();
        List<Infos.StartCassette> strStartCassette = reservationCreateIn.getStrStartCassette();
        List<Infos.MtrlOutSpec> strMtrlOutSpecSeq = reservationCreateIn.getStrMtrlOutSpecSeq();

        log.info("InParam [equipmentID] :" + equipmentID.getValue());
        log.info("InParam [controlJobID]:" + controlJobID.getValue());

        CimMachine aMachine = baseCoreFactory.getBO(CimMachine.class, equipmentID);
        Validations.check(null == aMachine, retCodeConfig.getNotFoundEqp());

        String slmSwitch = aMachine.getSLMSwitch();
        log.info("SLMSwitch : " + slmSwitch);

        CimControlJob aControlJob = baseCoreFactory.getBO(CimControlJob.class, controlJobID);
        Validations.check(null == aControlJob, retCodeConfig.getNotFoundControlJob());

        //--------------------------------------------------------------------------------------
        // Count the destination wafers
        //--------------------------------------------------------------------------------------
        log.info("Count the destination wafers.");
        AtomicInteger waferCnt = new AtomicInteger(0);
        Optional.ofNullable(strStartCassette).ifPresent(startCassettes -> startCassettes.forEach(startCassette -> {
            Optional.ofNullable(startCassette.getLotInCassetteList()).ifPresent(lotInCassettes -> {
                for (Infos.LotInCassette lotInCassette : lotInCassettes) {
                    if (!lotInCassette.getMoveInFlag()) {
                        continue;
                    }
                    waferCnt.addAndGet(CimArrayUtils.getSize(lotInCassette.getLotWaferList()));
                }
            });
        }));

        log.info("waferCnt : " + waferCnt.get());
        Validations.check(waferCnt.get() > CimArrayUtils.getSize(containerPosIDs), new OmCode(retCodeConfigEx.getNotEnoughContainerPosSpace(),waferCnt.get(), CimArrayUtils.getSize(containerPosIDs)));

        //--------------------------------------------------------------------------------------
        // Write equipment container position information
        //--------------------------------------------------------------------------------------
        log.info("Write equipment container position info.");

        AtomicInteger contPosIdx = new AtomicInteger(0);
        Set<ObjectIdentifier> destCastIDs = new HashSet<>();

        Optional.ofNullable(strStartCassette).ifPresent(startCassettes -> {
            for (Infos.StartCassette startCassette : startCassettes) {
                log.info("cassetteID : " + startCassette.getCassetteID().getValue());
                // Make equipment container position information from StartCassette
                log.info("Make equipment container position Info from StartCassette");
                Optional.ofNullable(startCassette.getLotInCassetteList()).ifPresent(lotInCassettes -> {
                    for (Infos.LotInCassette lotInCassette : lotInCassettes) {
                        log.info("lotID : " + lotInCassette.getLotID().getValue());
                        if (!lotInCassette.getMoveInFlag()) {
                            continue;
                        }
                        Optional.ofNullable(lotInCassette.getLotWaferList()).ifPresent(lotWafers -> {
                            for (Infos.LotWafer lotWafer : lotWafers) {
                                log.info("waferID ： " + lotWafer.getWaferID().getValue());
                                // Look for destination map information
                                // The destination map information may not exist.
                                log.info("Look for destination map information");
                                ObjectIdentifier destCassetteID = null;
                                int destSlotNo = 0;

                                for (Infos.MtrlOutSpec mtrlOutSpec : strMtrlOutSpecSeq) {
                                    List<Infos.SlmSlotMap> strDestinationMapSeq = mtrlOutSpec.getDestinationMapList();
                                    for (Infos.SlmSlotMap slmSlotMap : strDestinationMapSeq) {
                                        if (ObjectIdentifier.equalsWithValue(slmSlotMap.getWaferID(), lotWafer.getWaferID())) {
                                            // Found destination information
                                            log.info("Found destination informaNtion");
                                            destCassetteID = slmSlotMap.getCassetteID();
                                            destSlotNo = slmSlotMap.getSlotNumber();
                                            break;
                                        }
                                    }
                                    if (ObjectIdentifier.isNotEmptyWithValue(destCassetteID)) {
                                        break;
                                    }
                                }

                                log.info("destCassetteID : " + ObjectIdentifier.fetchValue(destCassetteID));
                                log.info("destSlotNo : " + destSlotNo);
                                // Get equipment container position information
                                log.info("Get Machine Container Position Info");

                                CimMachineContainerPosition aEqpContPos = baseCoreFactory.getBO(CimMachineContainerPosition.class, containerPosIDs.get(contPosIdx.get()));
                                Validations.check(null == aEqpContPos, retCodeConfig.getNotFoundEquipmentContainerPosition());

                                MachineDTO.MachineContainerPositionInfo eqpContPosInfo = aEqpContPos.getMachineContainerPositionInfo();

                                // Set equipment container position information
                                log.info("Set equipment container position Info");

                                if (null == eqpContPosInfo) {
                                    eqpContPosInfo = new MachineDTO.MachineContainerPositionInfo();
                                }

                                // Set equipment container position information
                                log.info("Set equipment container position Info");

                                eqpContPosInfo.setProcessJob(null);
                                eqpContPosInfo.setWafer(lotWafer.getWaferID());
                                eqpContPosInfo.setSrcPort(startCassette.getLoadPortID());
                                eqpContPosInfo.setDestCassette(null);
                                eqpContPosInfo.setProcessStartTimeStamp(null);
                                eqpContPosInfo.setProcessCompTimeStamp(null);
                                eqpContPosInfo.setEstimatedEndTimeStamp(null);
                                eqpContPosInfo.setLastClaimedTimeStamp(objCommon.getTimeStamp().getReportTimeStamp().toString());
                                eqpContPosInfo.setLastClaimedUser(objCommon.getUser().getUserID());

                                eqpContPosInfo.setControlJob(ObjectIdentifier.build(aControlJob.getIdentifier(), aControlJob.getPrimaryKey()));
                                if (CimStringUtils.equals(slmSwitch, BizConstant.SP_SLM_SWITCH_ON)) {
                                    eqpContPosInfo.setSLMState(BizConstant.SP_SLMSTATE_RESERVED);
                                    eqpContPosInfo.setSrcCassette(startCassette.getCassetteID());
                                    eqpContPosInfo.setSrcSlotNo(lotWafer.getSlotNumber());
                                    eqpContPosInfo.setDestSlotNo(destSlotNo);

                                    if (ObjectIdentifier.isNotEmptyWithValue(destCassetteID)) {

                                        CimCassette aDestCassette = baseCoreFactory.getBO(CimCassette.class, destCassetteID);
                                        Validations.check(null == aDestCassette, retCodeConfig.getNotFoundCassette());
                                        eqpContPosInfo.setDestCassette(ObjectIdentifier.build(aDestCassette.getIdentifier(), aDestCassette.getPrimaryKey()));
                                    } else {
                                        eqpContPosInfo.setDestCassette(destCassetteID);
                                    }
                                } else {
                                    eqpContPosInfo.setSLMState(BizConstant.SP_SLMSTATE_NONSLMOPE);
                                    eqpContPosInfo.setSrcCassette(null);
                                    eqpContPosInfo.setSrcSlotNo(0);
                                    eqpContPosInfo.setDestCassette(null);
                                    eqpContPosInfo.setDestSlotNo(0);
                                }

                                log.info(" Set Machine Container Position Info : " + eqpContPosInfo);

                                aEqpContPos.setMachineContainerPositionInfo(eqpContPosInfo);
                                // Increment equipment container position index.
                                contPosIdx.incrementAndGet();

                                // Save destination cassettes
                                if (ObjectIdentifier.isNotEmptyWithValue(destCassetteID)) {
                                    destCastIDs.add(destCassetteID);
                                }
                            } // end of lotWafers
                        });
                    } // end of lotInCassettes
                });
            } // end of startCassettes
        });

        //--------------------------------------------------------------------------------------
        // Set SLM reservation of destination cassette
        //--------------------------------------------------------------------------------------
        log.info("Set SLM reservation of destination cassette");
        Optional.of(destCastIDs).ifPresent(sets -> sets.forEach(data -> {
            CimCassette aCassette = baseCoreFactory.getBO(CimCassette.class, data);
            Validations.check(null == aCassette, retCodeConfig.getNotFoundCassette());
            aCassette.setSLMReservedMachine(aMachine);
        }));

        //--------------------------------------------------------------------------------------
        // Set Lot info into ControlJob
        //--------------------------------------------------------------------------------------
        log.info("");
        log.info("Set Lot info into ControlJob");
        Optional.ofNullable(strStartCassette).ifPresent(startCassettes -> startCassettes.forEach(startCassette -> {
            Optional.ofNullable(startCassette.getLotInCassetteList()).ifPresent(lotInCassettes -> {
                for (Infos.LotInCassette lotInCassette : lotInCassettes) {
                    if (!lotInCassette.getMoveInFlag()) {
                        continue;
                    }
                    ProductDTO.LotInControlJobInfo lotInControlJobInfo = new ProductDTO.LotInControlJobInfo();
                    lotInControlJobInfo.setMonitorLotFlag(lotInCassette.getMonitorLotFlag());
                    lotInControlJobInfo.setLotID(lotInCassette.getLotID());
                    aControlJob.addControlJobLot(lotInControlJobInfo);
                }
            });
        }));
    }

    public List<Infos.EqpContainerPosition> equipmentContainerPositionInfoGetByDestCassette(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID,ObjectIdentifier cassetteID){
        List<Infos.EqpContainerPosition> eqpContainerPositionList = new ArrayList<>();
        Infos.EqpContainerInfo eqpContainerInfo = equipmentContainerMethod.equipmentContainerInfoGet(objCommon, equipmentID);
        List<Infos.EqpContainer> eqpContainerList = eqpContainerInfo.getEqpContainerList();
        if (CimObjectUtils.isEmpty(eqpContainerList)){
            throw new ServiceException(retCodeConfig.getNotFoundEquipmentContainer());
        }
        List<Infos.EqpContainerPosition> eqpContainerPositions = eqpContainerList.get(0).getEqpContainerPosition();
        for(Infos.EqpContainerPosition eqpContainerPosition : eqpContainerPositions){
            if (CimStringUtils.equals(eqpContainerPosition.getDestCassetteID().getValue(),cassetteID.getValue())){
                eqpContainerPositionList.add(eqpContainerPosition);
            }
        }
        return eqpContainerPositionList;
    }

    @Override
    public Infos.EqpContainerPositionInfo equipmentContainerPositionInfoGet(Infos.ObjCommon objCommon, Inputs.ObjEquipmentContainerPositionInfoGetIn positionInfoGetIn) {
        Validations.check(null == positionInfoGetIn, retCodeConfig.getInvalidInputParam());
        ObjectIdentifier equipmentID = positionInfoGetIn.getEquipmentID();
        String keyCategory = positionInfoGetIn.getKeyCategory();
        ObjectIdentifier key = positionInfoGetIn.getKey();
        Validations.check(ObjectIdentifier.isEmpty(equipmentID), retCodeConfig.getInvalidParameter());

        //For OutPutParameter
        Infos.EqpContainerPositionInfo retVal = new Infos.EqpContainerPositionInfo();
        retVal.setEquipmentID(equipmentID);

        List<Infos.EqpContainerPosition> eqpContainerPositionList = new ArrayList<>();
        retVal.setEqpContainerPositionList(eqpContainerPositionList);

        // Get Machine Object
        CimMachine aMachine = baseCoreFactory.getBO(CimMachine.class, equipmentID);
        Validations.check(null == aMachine,retCodeConfig.getNotFoundMachine());

        List<CimMachineContainerPosition> equipmentContainerPositions = new ArrayList<>();
        if (CimStringUtils.equals(keyCategory,BizConstant.SP_SLM_KEYCATEGORY_EQUIPMENT)
                || CimStringUtils.equals(keyCategory,BizConstant.SP_SLM_KEYCATEGORY_LOT)){
            log.info("KeyCategory_Equipment or KeyCategory_Lot");
            //-----------------------------------------
            //Get All Machine Container of Equipment
            //-----------------------------------------
            List<CimMachineContainer> equipmentContainer = aMachine.allMachineContainers();
            Optional.ofNullable(equipmentContainer)
                    .ifPresent(list -> list.forEach(data ->
                            equipmentContainerPositions.addAll(data.allMachineContainerPositions()))
                    );
        }else if (CimStringUtils.equals(BizConstant.SP_SLM_KEYCATEGORY_CONTROLJOB,keyCategory)){
            //--------------------------------------------------
            //Get Machine Container of Equipment by ControlJob
            //--------------------------------------------------
            Optional.ofNullable(aMachine.findMachineContainerPositionForControlJob(key.getValue()))
                    .filter(data -> data.size() > 0)
                    .ifPresent(equipmentContainerPositions::addAll);
        }else if (CimStringUtils.equals(BizConstant.SP_SLM_KEYCATEGORY_PROCESSJOB,keyCategory)){
            //--------------------------------------------------
            //Get Machine Container of Equipment by ProcessJob
            //--------------------------------------------------
            Optional.ofNullable(aMachine.findMachineContainerPositionForProcessJob(key.getValue()))
                    .filter(data -> data.size() > 0)
                    .ifPresent(equipmentContainerPositions::addAll);
        }else if (CimStringUtils.equals(BizConstant.SP_SLM_KEYCATEGORY_WAFER,keyCategory)){
            //--------------------------------------------------
            //Get Machine Container of Equipment by Wafer
            //--------------------------------------------------
            Optional.ofNullable(aMachine.findMachineContainerPositionForWafer(key.getValue()))
                    .ifPresent(equipmentContainerPositions::add);
        }else {
            log.error("!!!!! Error Because IN_keyCategory is invalid.");
            throw new ServiceException(new OmCode(retCodeConfig.getSlmInvalidKeyCategory(),keyCategory));
        }

        for (CimMachineContainerPosition equipmentContainerPosition : equipmentContainerPositions) {
            CimWafer wafer = equipmentContainerPosition.getWafer();
            if (null == wafer) {
                continue;
            }
            Lot lot = wafer.getLot();
            ObjectIdentifier lotID = new ObjectIdentifier();
            if (null != lot){

                lotID = new ObjectIdentifier(lot.getIdentifier(), lot.getPrimaryKey());
            }
            if (CimStringUtils.equals(keyCategory, BizConstant.SP_SLM_KEYCATEGORY_LOT)
                    && !CimStringUtils.equals(key.getValue(), lotID.getValue())) {
                continue;
            }

            Infos.EqpContainerPosition eqpContainerPosition = new Infos.EqpContainerPosition();
            MachineDTO.MachineContainerPositionInfo machineContainerPositionInfo =
                    equipmentContainerPosition.getMachineContainerPositionInfo();
            eqpContainerPosition.setContainerPositionID(machineContainerPositionInfo.getMachineContainerPosition());
            eqpContainerPosition.setControlJobID(machineContainerPositionInfo.getControlJob());
            eqpContainerPosition.setProcessJobID(machineContainerPositionInfo.getProcessJob());
            eqpContainerPosition.setLotID(lotID);
            eqpContainerPosition.setSrcCassetteID(machineContainerPositionInfo.getSrcCassette());
            eqpContainerPosition.setWaferID(machineContainerPositionInfo.getWafer());
            eqpContainerPosition.setSrcPortID(machineContainerPositionInfo.getSrcPort());
            eqpContainerPosition.setFmcState(machineContainerPositionInfo.getSLMState());
            eqpContainerPosition.setSrcSlotNo(CimNumberUtils.intValue(machineContainerPositionInfo.getSrcSlotNo()));
            eqpContainerPosition.setDestCassetteID(machineContainerPositionInfo.getDestCassette());
            eqpContainerPosition.setDestPortID(machineContainerPositionInfo.getDestPort());
            eqpContainerPosition.setEstimatedProcessEndTime(
                    CimDateUtils.convertTo(machineContainerPositionInfo.getEstimatedEndTimeStamp()));
            eqpContainerPosition.setDestSlotNo(CimNumberUtils.intValue(machineContainerPositionInfo.getDestSlotNo()));
            eqpContainerPosition.setProcessStartTime(
                    CimDateUtils.convertTo(machineContainerPositionInfo.getProcessStartTimeStamp()));
            eqpContainerPosition.setLastClaimedTimeStamp(
                    CimDateUtils.convertTo(machineContainerPositionInfo.getLastClaimedTimeStamp()));
            eqpContainerPosition.setLastClaimedUserID(machineContainerPositionInfo.getLastClaimedUser());
            eqpContainerPositionList.add(eqpContainerPosition);

            retVal.setEquipmentID(machineContainerPositionInfo.getMachine());
        }
        return retVal;
    }

    @Override
    public List<ObjectIdentifier> equipmentContainerPositionInfoClear(Infos.ObjCommon objCommon, Inputs.ObjEquipmentContainerPositionInfoClearIn in) {
        ObjectIdentifier equipmentID = in.getEquipmentID();
        ObjectIdentifier objKey = in.getKey();
        String keyCategory = in.getKeyCategory();

        /*--------------------------*/
        /*   Get Equipment Object   */
        /*--------------------------*/
        CimMachine cimMachine = baseCoreFactory.getBO(CimMachine.class, equipmentID);

        /*-------------------------------------*/
        /*   Get Equipment ContainerSequence   */
        /*-------------------------------------*/
        List<CimMachineContainer> cimMachineContainers = cimMachine.allMachineContainers();
        Validations.check(CimArrayUtils.isEmpty(cimMachineContainers), retCodeConfig.getNotFoundEquipmentContainer());
        List<CimMachineContainerPosition> positionsToClear = new LinkedList<>();
        switch (keyCategory) {
            case BizConstant.SP_SLM_KEYCATEGORY_WAFER:
                cimMachineContainers.forEach(cimMachineContainer -> {
                    CimMachineContainerPosition position = cimMachineContainer.findMachineContainerPositionForWafer(ObjectIdentifier.fetchValue(objKey));
                    positionsToClear.add(position);
                });
                break;
            case BizConstant.SP_SLM_KEYCATEGORY_CONTROLJOB:
                cimMachineContainers.forEach(cimMachineContainer -> {
                    positionsToClear.addAll(cimMachineContainer.findMachineContainerPositionForControlJob(ObjectIdentifier.fetchValue(objKey))
                            .stream().filter(data ->
                                    !CimStringUtils.equalsIn(objCommon.getTransactionID(),
                                            TransactionIDEnum.PARTIAL_OPERATION_COMP_FOR_INTERNAL_BUFFER_REQ.getValue(),
                                            TransactionIDEnum.PARTIAL_OPERATION_COMP_WITH_DATA_REQ.getValue()) &&
                                    data.getControlJob() != null).collect(Collectors.toList()));
                });
                break;
            case BizConstant.SP_SLM_KEYCATEGORY_LOT:
                CimLot cimLot = baseCoreFactory.getBO(CimLot.class, objKey);
                Validations.check(cimLot == null, retCodeConfig.getNotFoundLot());
                List<ProductDTO.WaferInfo> allWaferInfo = cimLot.getAllWaferInfo();
                Validations.check(CimArrayUtils.isEmpty(allWaferInfo), retCodeConfigEx.getNotFoundWaferInLot());
                allWaferInfo.stream().map(data -> ObjectIdentifier.fetchValue(data.getWaferID()))
                        .forEach(waferId -> cimMachineContainers
                                .forEach(container -> positionsToClear.add(container.findMachineContainerPositionForWafer(waferId))));
                break;
            default:
                throw new ServiceException(retCodeConfig.getInvalidInputParam());
        }
        List<ObjectIdentifier> retVal = new ArrayList<>(positionsToClear.size());
        positionsToClear.forEach(position -> {
            retVal.add(ObjectIdentifier.build(position.getIdentifier(), position.getPrimaryKey()));
            position.clearMachineContainerPositionInfo();
        });
        return CimObjectUtils.isEmpty(retVal) ? Collections.emptyList() : retVal;
    }
}
