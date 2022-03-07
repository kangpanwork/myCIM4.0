package com.fa.cim.method.impl;

import com.fa.cim.annotaion.OmMethod;
import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.OmCode;
import com.fa.cim.common.utils.*;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.config.RetCodeConfigEx;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Inputs;
import com.fa.cim.dto.Outputs;
import com.fa.cim.dto.Results;
import com.fa.cim.method.*;
import com.fa.cim.newcore.bo.durable.CimCassette;
import com.fa.cim.newcore.bo.machine.CimMachine;
import com.fa.cim.newcore.factory.BaseCoreFactory;
import com.fa.cim.newcore.impl.bo.env.StandardProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.Comparator.comparing;

/**
 * <p>SLMMethod .
 * change history:
 * date                      defect#             person            comments
 * -------------------------------------------------------------------------------------------------------------------
 * 2020/4/29 15:14         ********              ZQI             create file.
 *
 * @author ZQI
 * @version 1.0
 * @date 2020/4/29 15:14
 * @copyright 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 * @since JDK1.8
 */
@OmMethod
@Slf4j
public class SLMMethod implements ISLMMethod {

    @Autowired
    private RetCodeConfig retCodeConfig;

    @Autowired
    private RetCodeConfigEx retCodeConfigEx;

    @Autowired
    private IEquipmentMethod equipmentMethod;

    @Autowired
    private BaseCoreFactory baseCoreFactory;

    @Autowired
    private IWaferMethod waferMethod;

    @Autowired
    private ICassetteMethod cassetteMethod;

    @Autowired
    private IBondingGroupMethod bondingGroupMethod;

    @Autowired
    private ILotMethod lotMethod;

    @Autowired
    private IControlJobMethod controlJobMethod;

    @Autowired
    private IEquipmentContainerPositionMethod equipmentContainerPositionMethod;

    @Autowired
    private IEquipmentContainerMethod equipmentContainerMethod;


    @Override
    public void slmCheckConditionForOperation(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID,
                                              String portGroupID, ObjectIdentifier controlJobID,
                                              List<Infos.StartCassette> strStartCassette,
                                              List<Infos.MtrlOutSpec> strMtrlOutSpecSeq, String operation) {
        log.info("InParam [equipmentID] : " + ObjectIdentifier.fetchValue(equipmentID));
        log.info("InParam [portGroupID] : " + portGroupID);
        log.info("InParam [controlJobID]: " + ObjectIdentifier.fetchValue(controlJobID));
        log.info("InParam [operation]   : " + operation);

        Optional.ofNullable(strMtrlOutSpecSeq).ifPresent(mtrlOutSpecs -> mtrlOutSpecs.forEach(mtrl -> {
            Optional.ofNullable(mtrl.getSourceMapList()).ifPresent(list -> list.forEach(data -> {
                log.info(" Source  cassetteID  : " + ObjectIdentifier.fetchValue(data.getCassetteID()));
                log.info(" Source  waferID     : " + ObjectIdentifier.fetchValue(data.getWaferID()));
                log.info(" Source  slotNumber  : " + data.getSlotNumber());
            }));
            log.info("");
            Optional.ofNullable(mtrl.getDestinationMapList()).ifPresent(list -> list.forEach(data -> {
                log.info(" Destination  cassetteID  : " + ObjectIdentifier.fetchValue(data.getCassetteID()));
                log.info(" Destination  waferID     : " + ObjectIdentifier.fetchValue(data.getWaferID()));
                log.info(" Destination  slotNumber  : " + data.getSlotNumber());
                if (data.getSlotNumber() == 0) {
                    log.error("slotNumber == 0");
                    throw new ServiceException(retCodeConfigEx.getInvalidSlotNo());
                }
            }));
        }));

        //--------------------------------------------------
        // Get Equipment Base Information
        //--------------------------------------------------
        Infos.EqpBrInfo eqpBrInfo = equipmentMethod.equipmentBRInfoGetDR(objCommon, equipmentID);

        //--------------------------------------------------
        // Get Equipment Container Information
        //--------------------------------------------------
        Infos.EqpContainerInfo eqpContainerInfo = equipmentContainerMethod.equipmentContainerInfoGet(objCommon, equipmentID);

        //******************************************************************************************
        //
        // Loading
        //
        //******************************************************************************************
        if (CimStringUtils.equals(operation, BizConstant.SP_OPERATION_LOADINGLOT)) {
            log.info("operation is [Loading]");
            Optional.ofNullable(strStartCassette).ifPresent(startCassettes -> startCassettes.forEach(startCassette -> {
                log.info(" cassetteID      : " + ObjectIdentifier.fetchValue(startCassette.getCassetteID()));
                log.info(" loadPurposeType : " + startCassette.getLoadPurposeType());
                if (CimStringUtils.equals(startCassette.getLoadPurposeType(), BizConstant.SP_LOADPURPOSETYPE_SLMRETRIEVING)) {
                    log.info(" LoadPurposeType is 'SLMRetrieving' ");
                    // The cassette must have SLMReserved equipment if LoadPurposeType is 'SLMRetrieving'.
                    CimCassette aCassette = baseCoreFactory.getBO(CimCassette.class, startCassette.getCassetteID());
                    Validations.check(null == aCassette, retCodeConfig.getNotFoundCassette());

                    CimMachine aSLMReservedMachine = aCassette.getSLMReservedMachine();
                    Validations.check(null == aSLMReservedMachine, retCodeConfig.getNotReservedCastSLM(),ObjectIdentifier.fetchValue(startCassette.getCassetteID()));

                    Validations.check(CimObjectUtils.isEmpty(eqpContainerInfo.getEqpContainerList().get(0).getEqpContainerPosition()), retCodeConfig.getNotFoundEquipmentContainer());

                    // destination port should be the same as reserve requested port
                    List<Infos.EqpContainerPosition> eqpContainerPosition = eqpContainerInfo.getEqpContainerList().get(0).getEqpContainerPosition();
                    Optional.ofNullable(eqpContainerPosition).ifPresent(eqpContainerPositions -> {
                        for (Infos.EqpContainerPosition containerPosition : eqpContainerPositions) {
                            if (ObjectIdentifier.equalsWithValue(containerPosition.getDestCassetteID(), startCassette.getCassetteID())) {
                                log.info("Destination Port is reserved for cassette");
                                if (ObjectIdentifier.isNotEmptyWithValue(containerPosition.getDestPortID())
                                        && ObjectIdentifier.equalsWithValue(containerPosition.getDestPortID(), startCassette.getLoadPortID())) {
                                    throw new ServiceException(retCodeConfig.getInvalidLoadingPort());
                                }

                                break;
                            }
                        }
                    });
                }
            }));
        }


        //******************************************************************************************
        //
        // StartReservation
        // OpeStart
        //
        //******************************************************************************************
        if (CimStringUtils.equals(operation, BizConstant.SP_OPERATION_STARTRESERVATION)
                || CimStringUtils.equals(operation, BizConstant.SP_OPERATION_OPESTART)) {
            log.info("operation is [StartReservation] or [OpeStart]");
            // If SLM Capability = Yes. Cannot claim OpeStart directly without start lot reservation
            if (CimStringUtils.equals(operation, BizConstant.SP_OPERATION_OPESTART)) {
                log.info("operation = SP_Operation_OpeStart");
                if (eqpBrInfo.isFmcCapabilityFlag()) {
                    log.info("SLMCapabilityFlag = TRUE");
                    if (ObjectIdentifier.isEmptyWithValue(controlJobID)) {
                        throw new ServiceException(retCodeConfigEx.getSlmCtrlJobRequired());
                    }
                }
            }

            // Cassette must not be SLMReserved.
            Optional.ofNullable(strStartCassette).ifPresent(startCassettes -> startCassettes.forEach(startCassette -> {
                log.info(" # cassetteID : " + ObjectIdentifier.fetchValue(startCassette.getCassetteID()));
                CimCassette aCassette = baseCoreFactory.getBO(CimCassette.class, startCassette.getCassetteID());
                Validations.check(null == aCassette, retCodeConfig.getNotFoundCassette());

                CimMachine aSLMReservedMachine = aCassette.getSLMReservedMachine();
                if (null != aSLMReservedMachine) {
                    log.info("# aSLMReservedMachine is NOT NULL");
                    if (CimStringUtils.equals(operation, BizConstant.SP_OPERATION_STARTRESERVATION)) {
                        log.error("# operation = StartReservation");
                        Validations.check(retCodeConfig.getAlreadyReservedCassetteSlm(),aCassette.getIdentifier());
                    }

                    if (CimStringUtils.equals(operation, BizConstant.SP_OPERATION_OPESTART)) {
                        log.info("# operation = OpeStart");
                        if (ObjectIdentifier.isEmptyWithValue(controlJobID)) {
                            //----------------------------------------
                            // OpeStart without controlJobID
                            // SLMRsvEqp should be Nil
                            //----------------------------------------
                            Validations.check(retCodeConfig.getAlreadyReservedCassetteSlm(),aCassette.getIdentifier());
                        } else {
                            //----------------------------------------
                            // OpeStart with controlJobID
                            // cassette should be starting controlJob's destination cassette
                            //----------------------------------------
                            Optional.ofNullable(eqpContainerInfo.getEqpContainerList().get(0).getEqpContainerPosition()).ifPresent(eqpContainerPositions -> {
                                boolean SLMReservedByMyselfFlag = false;
                                for (Infos.EqpContainerPosition eqpContainerPosition : eqpContainerPositions) {
                                    if (!ObjectIdentifier.equalsWithValue(eqpContainerPosition.getControlJobID(), controlJobID)) {
                                        log.info("## different controlJobID");
                                        continue;
                                    }
                                    if (ObjectIdentifier.equalsWithValue(eqpContainerPosition.getDestCassetteID(), startCassette.getCassetteID())) {
                                        log.info("## SLMReservedByMyselfFlag = TRUE");
                                        SLMReservedByMyselfFlag = true;
                                        break;
                                    }
                                }
                                Validations.check(!SLMReservedByMyselfFlag, new OmCode(retCodeConfig.getAlreadyReservedCassetteSlm(),startCassette.getCassetteID().getValue()));
                            });
                        }
                    }
                }
            }));
        }


        //******************************************************************************************
        //
        // NPW Reserve
        //
        //******************************************************************************************
        if (CimStringUtils.equals(operation, BizConstant.SP_OPERATION_NPWCARRIERXFER)) {
            log.info("operation is [NPWCarrierXfer]");
            Optional.ofNullable(strStartCassette).ifPresent(startCassettes -> startCassettes.forEach(startCassette -> {
                // The cassette must have SLMReserved equipment if LoadPurposeType is 'SLMRetrieving'.
                CimCassette aCassette = baseCoreFactory.getBO(CimCassette.class, startCassette.getCassetteID());
                Validations.check(null == aCassette, retCodeConfig.getNotFoundCassette());

                CimMachine aSLMReservedMachine = aCassette.getSLMReservedMachine();

                if (CimStringUtils.equals(startCassette.getLoadPurposeType(), BizConstant.SP_LOADPURPOSETYPE_SLMRETRIEVING)) {
                    log.info(" LoadPurposeType = SLMRetrieving ");
                    if (null == aSLMReservedMachine) {
                        Validations.check(retCodeConfigEx.getUndefinedDestCastSlm());
                    } else {
                        String reservedEqpID = aSLMReservedMachine.getIdentifier();
                        Validations.check(!CimStringUtils.equals(reservedEqpID, equipmentID.getValue()), retCodeConfig.getNotReservedCastSLM());
                    }

                    Validations.check(CimArrayUtils.getSize(eqpContainerInfo.getEqpContainerList()) == 0, retCodeConfig.getNotFoundEquipmentContainer());
                    // destination port should be the same as reserve requested port
                    Optional.ofNullable(eqpContainerInfo.getEqpContainerList().get(0).getEqpContainerPosition()).ifPresent(eqpContainerPositions -> {
                        for (Infos.EqpContainerPosition eqpContainerPosition : eqpContainerPositions) {
                            if (ObjectIdentifier.equalsWithValue(eqpContainerPosition.getDestCassetteID(), startCassette.getCassetteID())) {
                                log.info("Destination Port is reserved for cassette");
                                if (ObjectIdentifier.isNotEmptyWithValue(eqpContainerPosition.getDestPortID())
                                        && ObjectIdentifier.equalsWithValue(eqpContainerPosition.getDestPortID(), startCassette.getLoadPortID())) {
                                    log.error("invlude loading port");
                                    Validations.check(retCodeConfig.getInvalidLoadingPort());
                                }
                            }
                            break;
                        }
                    });
                } else {
                    log.info("LoadPurposeType != SLMRetrieving");
                    if (null != aSLMReservedMachine) {
                        String reservedEqpID = aSLMReservedMachine.getIdentifier();
                        Validations.check(CimStringUtils.isNotEmpty(reservedEqpID), retCodeConfig.getInvalidPortLoadPurposeForNpwTransfer());
                    }
                }
            }));
        }

        //******************************************************************************************
        //
        // StartReservationForSLM
        //
        //******************************************************************************************
        if (CimStringUtils.equals(operation, BizConstant.SP_OPERATION_STARTRESERVATIONFORSLM)) {
            log.info("operation is [StartReservationForSLM]");
            //--------------------------------------------------------------------------------------
            // The SLMCapabilityFlag must be TRUE.
            //--------------------------------------------------------------------------------------
            Validations.check(!eqpBrInfo.isFmcCapabilityFlag(), retCodeConfigEx.getEqpSlmCapabilityOff());

            //--------------------------------------------------------------------------------------
            // Check source cassette condition
            //--------------------------------------------------------------------------------------
            Optional.ofNullable(strStartCassette).ifPresent(list -> list.forEach(startCassette -> {
                CimCassette aCassette = baseCoreFactory.getBO(CimCassette.class, startCassette.getCassetteID());
                Validations.check(null == aCassette, retCodeConfig.getNotFoundCassette());
                CimMachine slmReservedMachine = aCassette.getSLMReservedMachine();
                Validations.check(null != slmReservedMachine, retCodeConfig.getAlreadyReservedCassetteSlm(),aCassette.getIdentifier());
            }));

            //--------------------------------------------------------------------------------------
            // Don't specify MtrlOutSpec, when SLMSwitch of equipment is FALSE.
            //--------------------------------------------------------------------------------------
            Validations.check(CimStringUtils.equals(eqpBrInfo.getFmcSwitch(), BizConstant.SP_SLM_SWITCH_OFF)
                    && CimArrayUtils.getSize(strMtrlOutSpecSeq) > 0, retCodeConfigEx.getNotSpecifySlmInfo());

            if (CimArrayUtils.getSize(strMtrlOutSpecSeq) == 0) {
                return;
            }

            //--------------------------------------------------------------------------------------
            // Don't specify EmptyCassette, when SLMSwitch of equipment is ON.
            //--------------------------------------------------------------------------------------
            if (CimStringUtils.equals(eqpBrInfo.getFmcSwitch(), BizConstant.SP_SLM_SWITCH_ON)) {
                Optional.ofNullable(strStartCassette).ifPresent(list -> list.forEach(startCassette -> {
                    Validations.check(CimStringUtils.equals(startCassette.getLoadPurposeType(), BizConstant.SP_LOADPURPOSETYPE_EMPTYCASSETTE), retCodeConfigEx.getNotSpecifyEmptycastSlmrsv());
                }));
            }

            //--------------------------------------------------------------------------------------
            // The MtrlOut count must be the same as the start cassette count.
            //--------------------------------------------------------------------------------------
            Validations.check(CimArrayUtils.getSize(strStartCassette) != CimArrayUtils.getSize(strMtrlOutSpecSeq), retCodeConfig.getInvalidInputParam());

            //--------------------------------------------------------------------------------------
            // Count the started wafers.
            //--------------------------------------------------------------------------------------
            log.info("Count the started wafers.");
            AtomicInteger startWaferCnt = new AtomicInteger(0);
            Optional.ofNullable(strStartCassette).ifPresent(startCassettes -> startCassettes.forEach(startCassette -> {
                Optional.ofNullable(startCassette.getLotInCassetteList()).ifPresent(lotInCassettes -> lotInCassettes.forEach(lotInCassette -> {
                    if (lotInCassette.getMoveInFlag()) {
                        startWaferCnt.getAndAdd(CimArrayUtils.getSize(lotInCassette.getLotWaferList()));
                    }
                }));
            }));
            log.info("Total startWaferCnt : " + startWaferCnt.get());

            //--------------------------------------------------------------------------------------
            // Count the wafer that is Stored or Retrieved in equipment container position.
            //--------------------------------------------------------------------------------------
            log.info("");
            log.info("Count the wafer that is Stored or Retrieved in equipment container position.");
            Validations.check(CimArrayUtils.getSize(eqpContainerInfo.getEqpContainerList()) == 0, retCodeConfig.getNotFoundEquipmentContainer());

            AtomicInteger waferInEqpCnt = new AtomicInteger(0);
            AtomicInteger waferNotInEqpCnt = new AtomicInteger(0);
            Optional.ofNullable(eqpContainerInfo.getEqpContainerList().get(0).getEqpContainerPosition()).ifPresent(list -> list.forEach(eqpContainerPosition -> {
                if (ObjectIdentifier.isNotEmptyWithValue(eqpContainerPosition.getWaferID())) {
                    waferInEqpCnt.incrementAndGet();
                } else {
                    waferNotInEqpCnt.incrementAndGet();
                }
            }));

            log.info("Total waferInEqpCnt : " + waferInEqpCnt.get());
            log.info("Total waferNotInEqpCnt : " + waferNotInEqpCnt.get());
            Integer maxRsvCount = eqpContainerInfo.getEqpContainerList().get(0).getMaxRsvCount();
            Validations.check(CimObjectUtils.isEmpty(maxRsvCount),retCodeConfigEx.getInvalidMaxrsvcount());
            log.info("equipment's maxRsvCount : " + maxRsvCount);

            //--------------------------------------------------------------------------------------
            // Started wafers and wafers that is in equipment must not exceed maxRsvCount of equipment container.
            //--------------------------------------------------------------------------------------
            // The starting wafers count [%s] and wafers that is already in container count [%s] exceed max reserve count [%s].
            Validations.check(startWaferCnt.addAndGet(waferInEqpCnt.get()) > maxRsvCount,
                    retCodeConfigEx.getTotalWaferOverMaxrsvCount(),
                    startWaferCnt.get(),
                    waferInEqpCnt.get(),
                    maxRsvCount);

            //--------------------------------------------------------------------------------------
            // MtrlOut's cassetteID and waferID must be not empty.
            //--------------------------------------------------------------------------------------
            log.info("");
            log.info("MtrlOut's cassetteID and waferID must be not empty.");
            Optional.ofNullable(strMtrlOutSpecSeq).ifPresent(mtrlOutSpecs -> mtrlOutSpecs.forEach(mtrl -> {
                // SourceMap
                Optional.ofNullable(mtrl.getSourceMapList()).ifPresent(slmSlotMaps -> slmSlotMaps.forEach(slmSlotMap -> {
                    if (ObjectIdentifier.isEmptyWithValue(slmSlotMap.getCassetteID()) || ObjectIdentifier.isEmptyWithValue(slmSlotMap.getWaferID())) {
                        Validations.check(retCodeConfig.getInvalidInputParam());
                    }
                }));

                // DestinationMap
                Optional.ofNullable(mtrl.getDestinationMapList()).ifPresent(slmSlotMaps -> slmSlotMaps.forEach(slmSlotMap -> {
                    if (ObjectIdentifier.isEmptyWithValue(slmSlotMap.getCassetteID()) || ObjectIdentifier.isEmptyWithValue(slmSlotMap.getWaferID())) {
                        Validations.check(retCodeConfig.getInvalidInputParam());
                    }
                }));
            }));

            //--------------------------------------------------------------------------------------
            // Check SourceMap SlotNo
            //   - SlotNo must be the value of 1 to OM_CARRIER_STORE_SIZE.
            //   - SlotNo must not overlap in the same Cassette.
            //--------------------------------------------------------------------------------------
            log.info("");
            log.info("Check SourceMap SlotNo");
            String castCapaStr = StandardProperties.OM_CARRIER_STORE_SIZE.getValue();
            int castCapa = CimStringUtils.isEmpty(castCapaStr) ? 0 : Integer.parseInt(castCapaStr);
            log.info(" OM_CARRIER_STORE_SIZE = " + castCapa);
            Optional.ofNullable(strMtrlOutSpecSeq).ifPresent(mtrlOutSpecs -> mtrlOutSpecs.forEach(mtrl -> {
                Optional.ofNullable(mtrl.getSourceMapList()).ifPresent(slmSlotMaps -> {
                    int lenSrcMap = slmSlotMaps.size();
                    for (int nSrc = 0; nSrc < lenSrcMap; nSrc++) {
                        if (slmSlotMaps.get(nSrc).getSlotNumber() < 1 || slmSlotMaps.get(nSrc).getSlotNumber() > castCapa) {
                            Validations.check(retCodeConfigEx.getInvalidWaferPosition());
                        }
                        for (int nSrc2 = nSrc + 1; nSrc2 < lenSrcMap; nSrc2++) {
                            if (ObjectIdentifier.equalsWithValue(slmSlotMaps.get(nSrc).getCassetteID(), slmSlotMaps.get(nSrc2).getCassetteID())
                                    && slmSlotMaps.get(nSrc).getSlotNumber() == slmSlotMaps.get(nSrc2).getSlotNumber()) {
                                Validations.check(retCodeConfigEx.getSlmInvalidParameterForSlotDuplicate());
                            }
                        }

                    }
                });
            }));

            //--------------------------------------------------------------------------------------
            // Check DestinationMap SlotNo
            //   - SlotNo must be the value of 1 to OM_CARRIER_STORE_SIZE.
            //   - SlotNo must not overlap in the same Cassette.
            //--------------------------------------------------------------------------------------
            log.info("");
            log.info("Check DestinationMap SlotNo");
            Optional.ofNullable(strMtrlOutSpecSeq).ifPresent(mtrlOutSpecs -> mtrlOutSpecs.forEach(mtrl -> {
                Optional.ofNullable(mtrl.getDestinationMapList()).ifPresent(slmSlotMaps -> {
                    for (Infos.SlmSlotMap slmSlotMap : slmSlotMaps) {
                        if (slmSlotMap.getSlotNumber() < 1 || slmSlotMap.getSlotNumber() > castCapa) {
                            Validations.check(retCodeConfigEx.getInvalidWaferPosition());
                        }
                        AtomicInteger sameInfoCnt = new AtomicInteger(0);
                        // Two or more combination of Cassette-SlotNo must not exist in destination information.
                        Optional.of(strMtrlOutSpecSeq).ifPresent(mtrlOutSpecs2 -> mtrlOutSpecs2.forEach(mtrl2 -> {
                            Optional.ofNullable(mtrl2.getDestinationMapList()).ifPresent(slmSlotMaps2 -> {
                                for (Infos.SlmSlotMap slmSlotMap2 : slmSlotMaps2) {
                                    if (ObjectIdentifier.equalsWithValue(slmSlotMap.getCassetteID(), slmSlotMap2.getCassetteID())
                                            && slmSlotMap.getSlotNumber() == slmSlotMap2.getSlotNumber()) {
                                        sameInfoCnt.incrementAndGet();
                                    }
                                }
                            });
                        }));

                        if (sameInfoCnt.get() != 1) {
                            Validations.check(retCodeConfigEx.getSlmInvalidParameterForSlotDuplicate());
                        }
                    }
                });
            }));

            //--------------------------------------------------------------------------------------
            // Check SourceMap
            //   - All Wafers of Lot(OpeStartFlag is TRUE) of StartCassette must be in SourceMap.
            //--------------------------------------------------------------------------------------
            log.info("");
            log.info("Check SourceMap");
            Optional.ofNullable(strStartCassette).ifPresent(startCassettes -> {
                AtomicInteger index = new AtomicInteger(0);
                for (Infos.StartCassette startCassette : startCassettes) {
                    // The StartCassette index and MtrlOut index are linked.
                    Optional.ofNullable(startCassette.getLotInCassetteList()).ifPresent(lotInCassettes -> {
                        for (Infos.LotInCassette lotInCassette : lotInCassettes) {
                            if (!lotInCassette.getMoveInFlag()) {
                                continue;
                            }
                            log.info("lotID : " + lotInCassette.getLotID().getValue());
                            Optional.ofNullable(lotInCassette.getLotWaferList()).ifPresent(lotWafers -> lotWafers.forEach(lotWafer -> {
                                log.info("waferID : " + lotWafer.getWaferID().getValue());
                                boolean bFoundWafer = false;
                                if (CimArrayUtils.isNotEmpty(strMtrlOutSpecSeq)) {
                                    Infos.MtrlOutSpec mtrlOutSpec = strMtrlOutSpecSeq.get(index.get());
                                    for (Infos.SlmSlotMap slmSlotMap : mtrlOutSpec.getSourceMapList()) {
                                        if (!ObjectIdentifier.equalsWithValue(startCassette.getCassetteID(), slmSlotMap.getCassetteID())) {
                                            Validations.check(retCodeConfig.getInvalidInputParam());
                                        }
                                        if (ObjectIdentifier.equalsWithValue(lotWafer.getWaferID(), slmSlotMap.getWaferID())) {
                                            log.info("found waferID");
                                            bFoundWafer = true;
                                            break;
                                        }
                                    }
                                    Validations.check(!bFoundWafer, retCodeConfig.getInvalidInputParam());
                                }
                            }));
                        }
                    });
                    index.incrementAndGet();
                }
            });

            //--------------------------------------------------------------------------------------
            // Check DestinationMap
            //   - Wafer of Lot(OpeStartFlag is TRUE) of StartCassette must not straddle different cassette.
            //   - If destination cassette of Lot is not decided, it does not need to be set.
            //--------------------------------------------------------------------------------------
            log.info("");
            log.info("Check DestinationMap");
            Optional.ofNullable(strStartCassette).ifPresent(startCassettes -> {
                AtomicInteger index = new AtomicInteger(0);
                for (Infos.StartCassette startCassette : startCassettes) {
                    // The StartCassette index and MtrlOut index are linked.
                    Optional.ofNullable(startCassette.getLotInCassetteList()).ifPresent(lotInCassettes -> {
                        for (Infos.LotInCassette lotInCassette : lotInCassettes) {
                            if (!lotInCassette.getMoveInFlag()) {
                                continue;
                            }
                            log.info("lotID : " + lotInCassette.getLotID().getValue());
                            AtomicInteger foundWaferCnt = new AtomicInteger(0);
                            AtomicReference<ObjectIdentifier> checkCassetteID = new AtomicReference<>();

                            Optional.ofNullable(lotInCassette.getLotWaferList()).ifPresent(lotWafers -> lotWafers.forEach(lotWafer -> {
                                log.info("waferID : " + lotWafer.getWaferID().getValue());
                                if (CimArrayUtils.isNotEmpty(strMtrlOutSpecSeq)) {
                                    Infos.MtrlOutSpec mtrlOutSpec = strMtrlOutSpecSeq.get(index.get());
                                    for (Infos.SlmSlotMap slmSlotMap : mtrlOutSpec.getDestinationMapList()) {
                                        if (ObjectIdentifier.equalsWithValue(lotWafer.getWaferID(), slmSlotMap.getWaferID())) {
                                            log.info("found same wafer");
                                            foundWaferCnt.incrementAndGet();

                                            if (checkCassetteID.get() == null || CimStringUtils.isEmpty(checkCassetteID.get().getValue())) {
                                                // save first cassetteID
                                                checkCassetteID.set(slmSlotMap.getCassetteID());
                                            } else if (!ObjectIdentifier.equalsWithValue(checkCassetteID.get(), slmSlotMap.getCassetteID())) {
                                                Validations.check(retCodeConfig.getInvalidInputParam());
                                            }
                                            break;
                                        }
                                    }
                                }
                            }));

                            if (0 == foundWaferCnt.get()) {
                                // If destination cassette of Lot is not decided, it does not need to be set.
                                log.info("If destination cassette of Lot is not decided, it does not need to be set.");
                            } else if (foundWaferCnt.get()!= lotInCassette.getLotWaferList().size()){
                                Validations.check(retCodeConfig.getInvalidInputParam());
                            }
                        }
                    });
                    index.incrementAndGet();
                }
            });

            //--------------------------------------------------------------------------------------
            // Check destination cassette condition
            //--------------------------------------------------------------------------------------
            log.info("");
            log.info("Check distination cassette condition");

            // Make unique destination cassette list
            Set<ObjectIdentifier> destCastSeq = new HashSet<>();
            Optional.ofNullable(strMtrlOutSpecSeq).ifPresent(mtrlOutSpecs -> mtrlOutSpecs.forEach(mtrl -> {
                Optional.ofNullable(mtrl.getDestinationMapList()).ifPresent(slmSlotMaps -> slmSlotMaps.forEach(slmSlotMap -> {
                    destCastSeq.add(slmSlotMap.getCassetteID());
                }));
            }));

            Optional.of(destCastSeq).ifPresent(destCasts -> destCasts.forEach(cassetteID -> {
                // Look for Lot of destination Cassette
                log.info("Look for Lot of destination Cassette : " + cassetteID.getValue());
                AtomicReference<ObjectIdentifier> targetLotID = new AtomicReference<>();
                Optional.ofNullable(strMtrlOutSpecSeq).ifPresent(mtrlOutSpecs -> {
                    for (Infos.MtrlOutSpec mtrlOutSpec : mtrlOutSpecs) {
                        Optional.ofNullable(mtrlOutSpec.getDestinationMapList()).ifPresent(slmSlotMaps -> {
                            for (Infos.SlmSlotMap slmSlotMap : slmSlotMaps) {
                                if (ObjectIdentifier.equalsWithValue(slmSlotMap.getCassetteID(), cassetteID)) {
                                    log.info("call wafer_lot_Get()");
                                    ObjectIdentifier waferID = waferMethod.waferLotGet(objCommon, slmSlotMap.getWaferID());
                                    targetLotID.set(waferID);
                                    break;
                                }
                            }
                        });
                        if (ObjectIdentifier.isNotEmptyWithValue(targetLotID.get())) {
                            break;
                        }
                    }
                });

                log.info("targetLotID : " + ObjectIdentifier.fetchValue(targetLotID.get()));
                cassetteMethod.cassetteCheckConditionForSLMDestCassette(objCommon, equipmentID, controlJobID, cassetteID, targetLotID.get());
            }));
        }

        //******************************************************************************************
        //
        // StartReservationForSLM, SLMWaferRetrieve, SLMWaferRetrieveCassetteReserve
        //
        //******************************************************************************************
        if (CimStringUtils.equals(operation, BizConstant.SP_OPERATION_STARTRESERVATIONFORSLM)
                || CimStringUtils.equals(operation, BizConstant.SP_OPERATION_SLMWAFERRETRIEVE)
                || CimStringUtils.equals(operation, BizConstant.SP_OPERATION_SLMWAFERRETRIEVECASSETTERESERVE)) {
            log.info("operation is [StartReservationForSLM], [SLMWaferRetrieve], [SLMWaferRetrieveCassetteReserve]");
            //--------------------------------------------------------------------------------------
            // Check Slot of destination cassette
            //--------------------------------------------------------------------------------------
            log.info("");
            log.info("Check Slot of destination cassette");
            List<ObjectIdentifier> checkedCastSeq = new ArrayList<>();
            Optional.ofNullable(strMtrlOutSpecSeq).ifPresent(mtrlOutSpecs -> mtrlOutSpecs.forEach(mtrl -> {
                Optional.ofNullable(mtrl.getDestinationMapList()).ifPresent(slmSlotMaps -> {
                    for (Infos.SlmSlotMap destMap : slmSlotMaps) {
                        if (ObjectIdentifier.isNotEmptyWithValue(destMap.getCassetteID())) {
                            // exclude duplication check
                            boolean bAlreadyChecked = false;
                            for (ObjectIdentifier checkedCast : checkedCastSeq) {
                                if (ObjectIdentifier.equalsWithValue(checkedCast, destMap.getCassetteID())) {
                                    bAlreadyChecked = true;
                                    break;
                                }
                            }
                            if (bAlreadyChecked) continue;

                            ObjectIdentifier checkCassetteID = destMap.getCassetteID();
                            log.info("checkCassetteID : " + checkCassetteID.getValue());
                            // if source cassette and destination cassette are same, excepts check.
                            boolean bSameCassette = false;
                            List<Infos.SlmSlotMap> strSourceMapSeq = mtrl.getSourceMapList();
                            for (Infos.SlmSlotMap sourceMap : strSourceMapSeq) {
                                if (ObjectIdentifier.equalsWithValue(destMap.getWaferID(), sourceMap.getWaferID())
                                        && ObjectIdentifier.equalsWithValue(destMap.getCassetteID(), sourceMap.getCassetteID())) {
                                    bSameCassette = true;
                                    break;
                                }
                            }
                            if (bSameCassette) {
                                log.info("Source cassette and destination cassette are same.");
                                continue;
                            }
                            // save checked cassetteID
                            checkedCastSeq.add(checkCassetteID);

                            // Get SlotMap information
                            log.info("call cassette_GetWaferMapDR()");
                            List<Infos.WaferMapInCassetteInfo> waferMapInCassetteInfosOut = cassetteMethod.cassetteGetWaferMapDR(objCommon, checkCassetteID);

                            // Get slot map information of cassette
                            Optional.ofNullable(waferMapInCassetteInfosOut).ifPresent(waferMapInCassetteInfos -> {
                                for (Infos.WaferMapInCassetteInfo castInWaferMap : waferMapInCassetteInfos) {
                                    // Ignore empty slot
                                    if (ObjectIdentifier.isEmptyWithValue(castInWaferMap.getWaferID())) {
                                        continue;
                                    }
                                    // If wafer is in slot, and the slot is not specified by DestMap, ignore the slot.
                                    AtomicBoolean bSlotUseByDestMap = new AtomicBoolean(false);
                                    Optional.of(strMtrlOutSpecSeq).ifPresent(mtrlOutSpecs2 -> {
                                        for (Infos.MtrlOutSpec mtrl2 : mtrlOutSpecs2) {
                                            Optional.ofNullable(mtrl2.getDestinationMapList()).ifPresent(slmSlotMaps2 -> {
                                                for (Infos.SlmSlotMap destSlotMap : slmSlotMaps2) {
                                                    if (ObjectIdentifier.equalsWithValue(checkCassetteID, destSlotMap.getCassetteID())
                                                            && castInWaferMap.getSlotNumber() == destSlotMap.getSlotNumber()) {
                                                        bSlotUseByDestMap.set(true);
                                                        break;
                                                    }
                                                }
                                            });
                                        }
                                    });

                                    if (!bSlotUseByDestMap.get()) {
                                        log.info("slot is not specified by DestMap");
                                        continue;
                                    }

                                    // The slot is already wafer, and the slot specified by DestMap.
                                    // but if the wafer specified by DestMap, ignore the slot.
                                    AtomicBoolean bWaferInDestMap = new AtomicBoolean(false);
                                    Optional.of(strMtrlOutSpecSeq).ifPresent(mtrlOutSpecs2 -> {
                                        for (Infos.MtrlOutSpec mtrl2 : mtrlOutSpecs2) {
                                            Optional.ofNullable(mtrl2.getDestinationMapList()).ifPresent(slmSlotMaps2 -> {
                                                for (Infos.SlmSlotMap destSlotMap : slmSlotMaps2) {
                                                    if (ObjectIdentifier.equalsWithValue(destSlotMap.getWaferID(), castInWaferMap.getWaferID())) {
                                                        bWaferInDestMap.set(true);
                                                        break;
                                                    }
                                                }
                                            });
                                        }
                                    });
                                    if (!bWaferInDestMap.get()) {
                                        Validations.check(retCodeConfigEx.getNotUseSlot(),
                                                castInWaferMap.getWaferID().getValue(),
                                                castInWaferMap.getSlotNumber(),
                                                checkCassetteID.getValue());
                                    }
                                }
                            });
                        }
                    }
                });
            }));
        }

        //------------------------------//
        //   Check Equipment Category   //
        //------------------------------//
        log.info(" equipmentCategory : " + eqpBrInfo.getEquipmentCategory());

        if (CimStringUtils.equals(eqpBrInfo.getEquipmentCategory(), BizConstant.SP_MC_CATEGORY_WAFERBONDING)) {
            log.info("##### Equipment Category is  SP_Mc_Category_WaferBonding");
            //-----------------------------------------------------------//
            //  Wafer Stacking Operation                                 //
            //    If Equipment Category is SP_Mc_Category_WaferBonding,  //
            //    Top Wafer's Destination Map cannot be changed.         //
            //-----------------------------------------------------------//

            //******************************************************************************************
            //
            // SLMWaferRetrieve, SLMWaferRetrieveCassetteReserve
            //
            //******************************************************************************************
            AtomicReference<Outputs.ObjBondingGroupInfoByEqpGetDROut> bondingGroupInfoByEqpGetDROut = new AtomicReference<>();
            if (CimStringUtils.equals(operation, BizConstant.SP_OPERATION_SLMWAFERRETRIEVE) || CimStringUtils.equals(operation, BizConstant.SP_OPERATION_SLMWAFERRETRIEVECASSETTERESERVE)) {
                log.info("");
                log.info("operation is [SLMWaferRetrieve], [SLMWaferRetrieveCassetteReserve]");
                //------------------------------//
                //   Get Bonding Map Info       //
                //------------------------------//
                bondingGroupInfoByEqpGetDROut.set(bondingGroupMethod.bondingGroupInfoByEqpGetDR(objCommon, equipmentID, controlJobID, true));
            }
            //******************************************************************************************
            //
            // StartReservationForSLM
            //
            //******************************************************************************************
            if (CimStringUtils.equals(operation, BizConstant.SP_OPERATION_STARTRESERVATIONFORSLM)) {
                log.info("");
                log.info("operation is [StartReservationForSLM]");
                Outputs.ObjBondingGroupInfoByEqpGetDROut out = new Outputs.ObjBondingGroupInfoByEqpGetDROut();
                List<Infos.BondingGroupInfo> bondingGroupInfos = new ArrayList<>();
                out.setBondingGroupInfoList(bondingGroupInfos);
                bondingGroupInfoByEqpGetDROut.set(out);

                Optional.ofNullable(strStartCassette).ifPresent(startCassettes -> startCassettes.forEach(startCassette -> {
                    Optional.ofNullable(startCassette.getLotInCassetteList()).ifPresent(lotInCassettes -> {
                        for (Infos.LotInCassette lotInCassette : lotInCassettes) {
                            // skip not start lot
                            if (!lotInCassette.getMoveInFlag()) {
                                log.info("operationStartFlag == FALSE");
                                continue;
                            }

                            // skip lot which is reserved to Bonding Group
                            boolean bReserved = false;

                            for (Infos.BondingGroupInfo strBondingGroupInfo : bondingGroupInfos) {
                                List<Infos.BondingMapInfo> bondingMapInfoList = strBondingGroupInfo.getBondingMapInfoList();
                                for (Infos.BondingMapInfo mapInfo : bondingMapInfoList) {
                                    // check base lot
                                    if (ObjectIdentifier.equalsWithValue(lotInCassette.getLotID(), mapInfo.getBaseLotID())) {
                                        bReserved = true;
                                        break;
                                    }

                                    // check top lot
                                    if (ObjectIdentifier.equalsWithValue(lotInCassette.getLotID(), mapInfo.getPlanTopLotID())) {
                                        bReserved = true;
                                        break;
                                    }
                                }
                            }

                            if (bReserved) {
                                log.info("This Lot is reserved to Bonding Group.");
                                continue;
                            }

                            //------------------------------//
                            //   Get Bonding Map Info       //
                            //------------------------------//
                            String groupIDGetDR = lotMethod.lotBondingGroupIDGetDR(objCommon, lotInCassette.getLotID());

                            Validations.check(CimStringUtils.isEmpty(groupIDGetDR), retCodeConfig.getNotFoundBondingGroupForLot());

                            Outputs.ObjBondingGroupInfoGetDROut bondingGroupInfoGetDROut = bondingGroupMethod.bondingGroupInfoGetDR(objCommon, groupIDGetDR, true);
                            // Set Bonding Group Information to list
                            bondingGroupInfos.add(bondingGroupInfoGetDROut.getBondingGroupInfo());
                        }
                    });
                }));
            }
            Outputs.ObjBondingGroupInfoByEqpGetDROut out = bondingGroupInfoByEqpGetDROut.get();
            if (out != null && CimArrayUtils.getSize(out.getBondingGroupInfoList()) > 0) {
                //---------------------------------------------------------//
                //   Check whether Top Wafer's MtrlOutSpec will be changed //
                //---------------------------------------------------------//
                log.info("Bonding Group does exist.");
                Optional.ofNullable(strMtrlOutSpecSeq).ifPresent(mtrlOutSpecs -> mtrlOutSpecs.forEach(mtrl -> {
                    Optional.ofNullable(mtrl.getSourceMapList()).ifPresent(sourceMaps -> sourceMaps.forEach(sourceMap -> {
                        AtomicBoolean bFoundFlg = new AtomicBoolean(false);
                        //---------------------------------------------------//
                        //   Firstly check whether wafer is Top Wafer or not //
                        //---------------------------------------------------//
                        for (Infos.BondingGroupInfo strBondingGroupInfo : out.getBondingGroupInfoList()) {
                            Optional.ofNullable(strBondingGroupInfo.getBondingMapInfoList()).ifPresent(bondingMapInfos -> {
                                for (Infos.BondingMapInfo bondingMapInfo : bondingMapInfos) {
                                    if (ObjectIdentifier.equalsWithValue(bondingMapInfo.getPlanTopWaferID(), sourceMap.getWaferID())) {
                                        log.info("### Found! TopWaferID : " + sourceMap.getWaferID().getValue());
                                        bFoundFlg.set(true);
                                        break;
                                    }
                                }
                            });
                            if (bFoundFlg.get()) {
                                break;
                            }
                        }
                        //------------------------------------------------------------//
                        //   Secondly check whether Destination Map is changed or not //
                        //------------------------------------------------------------//
                        if (bFoundFlg.get()) {
                            Optional.ofNullable(mtrl.getDestinationMapList()).ifPresent(destMaps -> destMaps.forEach(destMap -> {
                                boolean bDstMapChanged = false;
                                if (ObjectIdentifier.equalsWithValue(destMap.getWaferID(), sourceMap.getWaferID())) {
                                    log.info(" #### destination cassette is changed.");
                                    // check whether destination cassette is changed
                                    if (!ObjectIdentifier.equalsWithValue(destMap.getCassetteID(), sourceMap.getCassetteID())) {
                                        log.info(" #### destination cassette is changed.\".");
                                        bDstMapChanged = true;
                                    }
                                    // check whether destination slot is changed
                                    if (destMap.getSlotNumber() != sourceMap.getSlotNumber()) {
                                        log.info(" #### destination slot is changed.");
                                        bDstMapChanged = true;
                                    }
                                    if (bDstMapChanged) {
                                        log.error(" #### destination edit for top wafer.");
                                        Validations.check(retCodeConfigEx.getSlmInvalidDstMapForTopLot());
                                    }
                                }
                            }));
                        }
                    }));
                }));

            } else {
                log.info("Bonding Group does not exist.");
            }
        }
    }


    @Override
    public Results.SLMCheckConditionForCassetteReserveResult slmCheckConditionForCassetteReserve(Infos.ObjCommon objCommon,
                                                                                          ObjectIdentifier IN_equipmentID,
                                                                                          ObjectIdentifier IN_lotID,
                                                                                          ObjectIdentifier IN_controlJobID,
                                                                                          List<Infos.SlmSlotMap> IN_dstMapSeq,
                                                                                          List<Infos.SlmSlotMap> IN_srcMapSeq){

        //---------------------------------
        // Put Input Parameter
        //---------------------------------
        List<Infos.EventParameter> strEventParameter = new ArrayList<>();
        strEventParameter.add(new Infos.EventParameter("EQP_ID",IN_equipmentID.getValue()));
        strEventParameter.add(new Infos.EventParameter("CONTROL_JOB_ID",IN_controlJobID.getValue()));
        strEventParameter.add(new Infos.EventParameter("LOT_ID",IN_lotID.getValue()));
        strEventParameter.forEach(eventParameter -> log.info("Name : " + eventParameter.getParameterName() + " Value : " + eventParameter.getParameterValue()));
        //-----------------------
        // OutPut Parameter
        //-----------------------
        Results.SLMCheckConditionForCassetteReserveResult reserveResult = new Results.SLMCheckConditionForCassetteReserveResult();
        List<Infos.EqpContainerPosition> OUT_EqpContainerPositionSeq = new ArrayList<>();
        List<Infos.EqpContainer> OUT_EqpContainerSeq = new ArrayList<>();

        //---------------------------------------------------------------------------------
        //     Attention!!!
        //
        // Check for destination cassette cancel exsistence.
        // Used slotMap(checkedMap) for getting wafer list and for MaterialOutSpec check.
        //     - destCast cancel exist -> IN_srcMapSeq
        //     - no destCast cancel    -> IN_dstMapSeq
        //---------------------------------------------------------------------------------
        List<Infos.SlmSlotMap> checkedMap = new ArrayList<>();
        boolean destCastCancelFlag =false;
        log.info(" # Check for destination cassette cancel ");
        if (IN_srcMapSeq.size() != IN_dstMapSeq.size()){
            log.info(" # Check for destination cassette cancel ");
            destCastCancelFlag = true;
            checkedMap = IN_srcMapSeq;
        }else {
            log.info(" # Destination cassette cancel doesn't exist. ");
            checkedMap = IN_dstMapSeq;
        }
        //-------------------------------
        //   Get Equipment Online Mode
        //-------------------------------
        String onlineMode = equipmentMethod.equipmentOnlineModeGet(objCommon, IN_equipmentID);
        //---------------------------------
        //   Check Equipment Online Mode
        //---------------------------------
        log.info(" # Equipment Online Mode is ", onlineMode);
        //if online mode is online, controlJob is specified?
        if (!ObjectIdentifier.isEmpty(IN_controlJobID) && !CimStringUtils.equals(onlineMode,BizConstant.SP_EQP_ONLINEMODE_OFFLINE)){
            log.info(" # !!!!! Error Because the equipment is Online but controlJob is blank.");
            throw new ServiceException(new OmCode(retCodeConfigEx.getSlmEqpOnlinemodeForCastrsv(),BizConstant.SP_SLM_ACTIONCODE_CASSETTERESERVE,IN_controlJobID.getValue(),onlineMode));
        }
        //--------------------
        //   lotID exists ?
        //--------------------
        //lot_waferIDList_GetDR
        Inputs.ObjLotWaferIDListGetDRIn objLotWaferIDListGetDRIn = null;
        List<ObjectIdentifier> waferIDList = null;
        ObjectIdentifier lotControlJobID = null;
        if (!ObjectIdentifier.isEmpty(IN_lotID)) {
            objLotWaferIDListGetDRIn = new Inputs.ObjLotWaferIDListGetDRIn();
            objLotWaferIDListGetDRIn.setLotID(IN_lotID);
            objLotWaferIDListGetDRIn.setScrapCheckFlag(false);
            waferIDList = lotMethod.lotWaferIDListGetDR(objCommon, objLotWaferIDListGetDRIn);
            int INLotWaferLen = waferIDList.size();
            lotControlJobID = lotMethod.lotControlJobIDGet(objCommon, IN_lotID);
        }
        //---------------------------
        //   controlJobID exists ?
        //---------------------------
        List<ObjectIdentifier> cjWafers = new ArrayList<>();
        if (!ObjectIdentifier.isEmpty(IN_controlJobID)){
            log.info(" # Call controlJob_lotList_Get");
            List<Infos.ControlJobLot> controlJobLots = controlJobMethod.controlJobLotListGet(objCommon, IN_controlJobID);
            //-----------------------
            //   Get CJ's WaferSeq
            //-----------------------
            for (Infos.ControlJobLot controlJobLot : controlJobLots){
                Inputs.ObjLotWaferIDListGetDRIn strLot_waferIDList_GetDR_in = new Inputs.ObjLotWaferIDListGetDRIn();
                strLot_waferIDList_GetDR_in.setLotID(controlJobLot.getLotID());
                strLot_waferIDList_GetDR_in.setScrapCheckFlag(false);
                List<ObjectIdentifier> strLot_waferIDList_GetDR_out = lotMethod.lotWaferIDListGetDR(objCommon, strLot_waferIDList_GetDR_in);
                //controlJob's wafers list
                cjWafers.addAll(strLot_waferIDList_GetDR_out);
            }
        }
        //--------------------------------------------
        //   the lot's controlJob vs the controlJob
        //--------------------------------------------
        log.info(" # Check : the IN_lot's controlJob VS the IN_controlJob  ");
        if (!ObjectIdentifier.isEmpty(IN_lotID) && !ObjectIdentifier.isEmpty(IN_controlJobID)){
            log.info(" # IN_lot is specified and IN_controlJob is specified.");
            log.info(" # Lot must have a controlJob which is same as the specified controlJob.");
            if (ObjectIdentifier.isEmpty(lotControlJobID)){
                log.info(" # !!!!! Error Because the controlJob is specified but the lot's controljob is not found.");
                throw new ServiceException(new OmCode(retCodeConfig.getSlmInvalidParameterForCj(),IN_lotID.getValue()," ",IN_controlJobID.getValue()));
            }
            if (!ObjectIdentifier.equalsWithValue(IN_controlJobID, lotControlJobID)){
                log.info(" # !!!!! Error Because the lot's controljob and the controlJob is different.");
                throw new ServiceException(new OmCode(retCodeConfig.getSlmInvalidParameterForCj(),IN_lotID.getValue(),lotControlJobID.getValue(),IN_controlJobID.getValue()));
            }
        }else if (!ObjectIdentifier.isEmpty(IN_lotID) && ObjectIdentifier.isEmpty(IN_controlJobID)){
            log.info(" # IN_lot is specified and IN_controlJob is NOT specified.  ");
            log.info(" # Lot must not have a controlJob.");
            if (ObjectIdentifier.isEmpty(lotControlJobID)){
                log.info(" # !!!!! Error Because the lot has a controlJob but controljob is not specified.");
                throw new ServiceException(new OmCode(retCodeConfig.getSlmInvalidParameterForCj(),IN_lotID.getValue()," "," "));
            }
        }

        //--------------------------------------------------------
        //   Check MtrlOutSpec. Checked when IN_lotID is blank.
        //       Check SlotMap(checkedMap)
        //       - Cassette cancel exist -> IN_srcMapSeq
        //       - No cassette cancel    -> IN_dstMapSeq
        //--------------------------------------------------------

        log.info(" # Check : MtrlOutSpec");
        if (!CimObjectUtils.isEmpty(checkedMap)){
            if (ObjectIdentifier.isEmpty(IN_lotID)){
                if (!ObjectIdentifier.isEmpty(IN_controlJobID)){
                    //-----------------------------------------------------
                    //controlJob's wafers are in checked MaterialOutSpec ?
                    //-----------------------------------------------------
                    for (ObjectIdentifier caWafer : cjWafers){
                        boolean wfFoundFlag = false;
                        for (Infos.SlmSlotMap slmSlotMap : checkedMap){
                            if (ObjectIdentifier.equalsWithValue(caWafer, slmSlotMap.getWaferID())){
                                log.info(" ## Wafer is found");
                                wfFoundFlag = true;
                                break;
                            }
                        }
                        if (!wfFoundFlag){
                            log.info(" # !!!!! Error Because the controlJob's wafers are not in the specified MtrlOutSpec." );
                            throw new ServiceException(new OmCode(retCodeConfigEx.getSlmInvalidDstmapForCastrsv(),IN_controlJobID.getValue(),"***"));
                        }
                    }
                    //----------------------------------------------------------------------
                    //   Wafers are in Source Map?
                    //   Checked only when dest cassette cancels don't exist.
                    //       <- checkedMapCnt = IN_srcMapSeq. The same check was done above.
                    //----------------------------------------------------------------------
                    if(!destCastCancelFlag){
                        log.info("FALSE == destCastCancelFlag ");
                        for (ObjectIdentifier cjWafer : cjWafers){
                            boolean wfFoundInMtrOutSpecFlag = false;
                            for (Infos.SlmSlotMap slmSlotMap : IN_srcMapSeq){
                                if (ObjectIdentifier.equalsWithValue(cjWafer, slmSlotMap.getWaferID())){
                                    log.info(" ## Wafer is found");
                                    wfFoundInMtrOutSpecFlag= true;
                                    break;
                                }
                            }
                            if (!wfFoundInMtrOutSpecFlag){
                                log.info(" # !!!!! Error Because the lot(wafer) and MtrlOutSpec source Map are different.");
                                throw new ServiceException(new OmCode(retCodeConfigEx.getSlmInvalidSrcmapForCastrsv(),IN_controlJobID.getValue(),IN_lotID.getValue()));
                            }
                        }
                    }
                    log.info("Check : controlJob's wafer count / checkedMapLen");
                    if (cjWafers.size() != checkedMap.size()){
                        log.info("Error Because the controlJob's wafers are not in the specified MtrlOutSpec.");
                        throw new ServiceException(new OmCode(retCodeConfigEx.getSlmInvalidDstmapForCastrsv(),IN_controlJobID.getValue(),IN_lotID.getValue()));
                    }
                }else {
                    log.info("controlJob and lot are not specified. Get lot list from chencked slotMap.");
                    //Get Lot list from IN_dstMap
                    List<ObjectIdentifier> lotIDs = new ArrayList<>();
                    int lotCnt = 0 ;
                    for (Infos.SlmSlotMap slmSlotMap : checkedMap){
                        ObjectIdentifier waferLotID = waferMethod.waferLotGet(objCommon, slmSlotMap.getWaferID());
                        boolean lotFoundFlag =false;
                        for (ObjectIdentifier lotID : lotIDs) {
                            if (ObjectIdentifier.equalsWithValue(waferLotID, lotID)) {
                                lotFoundFlag = true;
                                break;
                            }
                        }
                        if (CimBooleanUtils.isFalse(lotFoundFlag)) {
                            lotIDs.add(waferLotID);
                        }
                    }
                    //Resize
                    for (ObjectIdentifier lotID : lotIDs){
                        //Get Lot's wafers ...
                        Inputs.ObjLotWaferIDListGetDRIn strLot_waferIDList_GetDR_in = new Inputs.ObjLotWaferIDListGetDRIn();
                        strLot_waferIDList_GetDR_in.setLotID(lotID);
                        strLot_waferIDList_GetDR_in.setScrapCheckFlag(false);
                        List<ObjectIdentifier> waferIDsList = lotMethod.lotWaferIDListGetDR(objCommon, strLot_waferIDList_GetDR_in);
                        //--------------------------------------------
                        //  Lot's all wafers are in checked slotMap?
                        //--------------------------------------------
                        log.info(" # Check : (controljob)Lot's all wafers are in checked slotMap?       ");
                        for (ObjectIdentifier waferID : waferIDsList){
                            boolean wfFoundFlag = false;
                            for (Infos.SlmSlotMap slmSlotMap : checkedMap){
                                if (ObjectIdentifier.equalsWithValue(waferID, slmSlotMap.getWaferID())){
                                    log.info(" ### Wafer found");
                                    wfFoundFlag = true;
                                    break;
                                }
                            }
                            if (!wfFoundFlag){
                                log.info(" ## !!!!! Error Because the (controlJob)lot's wafers are not in the specified Destination Map." );
                                throw new ServiceException(new OmCode(retCodeConfigEx.getSlmInvalidParameterForWafermap(),lotID.getValue()));
                            }
                        }
                    }
                }
            }
        }
        //--------------------------------------------
        // Get Equipment Container Information.
        //--------------------------------------------
        log.info(" # Get Equipment Container Information. ");
        Infos.EqpContainerInfo eqpContainerInfo = equipmentContainerMethod.equipmentContainerInfoGet(objCommon, IN_equipmentID);
        List<Infos.EqpContainer> strEqpContainerSeq = eqpContainerInfo.getEqpContainerList();
        //---------------------------------------------------------------
        //   If any wafer in this lot has been retrieved,
        //   the destination map cannot be changed
        //---------------------------------------------------------------
        List<ObjectIdentifier> retrievedLots = new ArrayList<>();
        for (Infos.EqpContainer eqpContainer : strEqpContainerSeq){
            List<Infos.EqpContainerPosition> eqpContainerPositions = eqpContainer.getEqpContainerPosition();
            for (Infos.EqpContainerPosition tmpEqpContainerPosition : eqpContainerPositions){
                if (ObjectIdentifier.equalsWithValue(tmpEqpContainerPosition.getControlJobID(), IN_controlJobID)){
                    if (!ObjectIdentifier.isEmpty(tmpEqpContainerPosition.getControlJobID())){
                        //---------------------------------------------------------------
                        // If SLMState = SP_SLMState_NonSLMOpe,
                        // Cannot change destination
                        //---------------------------------------------------------------
                        if (CimStringUtils.equals(tmpEqpContainerPosition.getFmcState(),BizConstant.SP_SLMSTATE_NONSLMOPE)){
                            log.info("## SLMState = SP_SLMState_NonSLMOpe");
                            throw new ServiceException(new OmCode(retCodeConfigEx.getNoneSlmOpe()));
                        }
                    }else {
                        log.info("## CIMFWStrLen(controlJobID) = NULL");
                        // check if this lot is relevent
                        int tempCount = 0;
                        for (Infos.SlmSlotMap slmSlotMap : IN_srcMapSeq){
                            if (ObjectIdentifier.equalsWithValue(tmpEqpContainerPosition.getWaferID(), slmSlotMap.getWaferID())){
                                log.info("### Relevent WaferID ");
                                break;
                            }else {
                                log.info("### Different WaferID ");
                                tempCount++;
                            }
                        }
                        if (IN_srcMapSeq.size()==tempCount){
                            log.info("this lot is irrelevent");
                            continue;
                        }else {
                            log.info("this lot is not irrelevent");
                        }
                    }

                    if (CimStringUtils.equals(tmpEqpContainerPosition.getFmcState(),BizConstant.SP_SLMSTATE_RETRIEVED)){
                        int tempCount = 0;
                        for (ObjectIdentifier retrievedLot : retrievedLots){
                            if (ObjectIdentifier.equalsWithValue(tmpEqpContainerPosition.getLotID(), retrievedLot)){
                                log.info("### Already in list");
                                break;
                            }else {
                                log.info("### Different Lot ID");
                                tempCount++;
                            }
                        }
                        if (tempCount==retrievedLots.size()){
                            retrievedLots.add(tmpEqpContainerPosition.getLotID());
                        }else {
                            log.info("## nCnt3 != retrievedLotLen");
                        }
                    }else {
                        log.info("## Not Retrieved Wafer");
                    }
                }else {
                    log.info("## different controlJobID");
                }
            }
        }

        for (ObjectIdentifier retrievedLot : retrievedLots){
            for (Infos.EqpContainer eqpContainer : strEqpContainerSeq){
                List<Infos.EqpContainerPosition> eqpContainerPositions = eqpContainer.getEqpContainerPosition();
                for (Infos.EqpContainerPosition tmpEqpContainerPosition : eqpContainerPositions){
                    log.info("lotID:",tmpEqpContainerPosition.getLotID().getValue());
                    log.info("waferID:",tmpEqpContainerPosition.getWaferID().getValue());
                    log.info("destCassetteID:",tmpEqpContainerPosition.getDestCassetteID().getValue());
                    log.info("destSlotNo:64");
                    if (ObjectIdentifier.equalsWithValue(tmpEqpContainerPosition.getLotID(), retrievedLot)){
                        int tmpCount = 0;
                        for (Infos.SlmSlotMap slmSlotMap : IN_dstMapSeq){
                            if (ObjectIdentifier.equalsWithValue(slmSlotMap.getWaferID(), tmpEqpContainerPosition.getWaferID())){
                                log.info("#### Found wafer    ");
                                if (!ObjectIdentifier.equalsWithValue(slmSlotMap.getCassetteID(), tmpEqpContainerPosition.getWaferID())
                                ||   slmSlotMap.getSlotNumber() != tmpEqpContainerPosition.getDestSlotNo()){
                                    log.info(" #### destination edit for retrieved lot.");
                                    throw new ServiceException(retCodeConfigEx.getInvalidSlmStateRetrieved());
                                }else {
                                    log.info("#### No destination change for wafer");
                                }
                                break;
                            }else {
                                log.info("#### Different Wafer");
                            }
                            tmpCount++;
                        }
                        if (tmpCount == IN_dstMapSeq.size()){
                            // wafer is not found in destination map
                            // it means the caller is trying to cancel destination cassette for this retrieved lot
                            throw new ServiceException(retCodeConfigEx.getInvalidSlmStateRetrieved());
                        }else {
                            log.info("wafer Found");
                        }
                    }else {
                        log.info("### Different Lot");
                    }
                }
            }
        }

        //---------------------------------------------------------------
        //   Wafers are in Equipment Container Position information ?
        //       Check SlotMap(checkedMap)
        //       - Cassette cancel exist -> IN_srcMapSeq
        //       - No cassette cancel    -> IN_dstMapSeq
        //---------------------------------------------------------------
        log.info(" # Check : Wafers are in Equipment Container Position information ?");
        if (ObjectIdentifier.isEmpty(IN_lotID)){
            if (!CimObjectUtils.isEmpty(checkedMap)){
                for (Infos.SlmSlotMap slmSlotMap : checkedMap){
                    boolean wfFoundFlag = false;
                    for (Infos.EqpContainer eqpContainer : strEqpContainerSeq){
                        List<Infos.EqpContainerPosition> eqpContainerPositions = eqpContainer.getEqpContainerPosition();
                        for (Infos.EqpContainerPosition eqpContainerPosition : eqpContainerPositions){
                            if (!ObjectIdentifier.isEmpty(IN_controlJobID)){
                                //controlJob is specified.
                                if (ObjectIdentifier.equalsWithValue(slmSlotMap.getWaferID(), eqpContainerPosition.getWaferID())
                                && ObjectIdentifier.equalsWithValue(IN_controlJobID, eqpContainerPosition.getControlJobID())){
                                    log.info(" ### Found the wafer");
                                    wfFoundFlag = true;
                                }
                            }else {
                                //controlJob is not specified.
                                log.info(" ### ControlJob is not specified case..  ");
                                if (ObjectIdentifier.equalsWithValue(slmSlotMap.getWaferID(), eqpContainerPosition.getWaferID())){
                                    wfFoundFlag = true;
                                    //----------------------------------------------------------------------------
                                    //if controlJob is blank, SLMState must be "Stored".
                                    //----------------------------------------------------------------------------
                                    log.info(" ### Check : if controlJob is blank and lotID is specified, all wafer's SLMState is Stored ?");
                                    if (!CimStringUtils.equals(eqpContainerPosition.getFmcState(),BizConstant.SP_SLMSTATE_STORED)){
                                        log.info( " ### !!!!! Error Because if controlJob is blank, all wafer's SLMState is not Stored.");
                                        throw new ServiceException(retCodeConfigEx.getInvalidSlmStateStored());
                                    }
                                }
                            }
                        }
                    }
                    if (!wfFoundFlag){
                        log.info(" ## !!!!! Error Because all the specified wafers are not in Equipment Container Position.");
                        throw new ServiceException(retCodeConfigEx.getNotReservedSlmAllwafers());
                    }
                }
                //----------------------
                //Set OutPut Parameter
                //----------------------
                for (Infos.SlmSlotMap slmSlotMap : checkedMap){
                    for (Infos.EqpContainer eqpContainer : strEqpContainerSeq){
                        List<Infos.EqpContainerPosition> eqpContainerPositions = eqpContainer.getEqpContainerPosition();
                        for (Infos.EqpContainerPosition eqpContainerPosition : eqpContainerPositions){
                            if (ObjectIdentifier.equalsWithValue(slmSlotMap.getWaferID(), eqpContainerPosition.getWaferID())){
                                log.info(" ### Found Wafer");
                                OUT_EqpContainerPositionSeq.add(eqpContainerPosition);
                                boolean foundInDestMapFlag = false;
                                ObjectIdentifier dummy = new ObjectIdentifier();
                                for (Infos.SlmSlotMap dstSlotMap : IN_dstMapSeq){
                                    if (ObjectIdentifier.equalsWithValue(slmSlotMap.getWaferID(), dstSlotMap.getWaferID())){
                                        log.info("Wafer is found in DestMap.");
                                        foundInDestMapFlag = true;
                                        Infos.EqpContainerPosition containerPosition = OUT_EqpContainerPositionSeq.get(OUT_EqpContainerPositionSeq.size() - 1);
                                        containerPosition.setDestCassetteID(dstSlotMap.getCassetteID());
                                        break;
                                    }
                                }
                                if (!foundInDestMapFlag){
                                    log.info(" ### Wafer NOT found in DestMap. Set blank cassette.");
                                    Infos.EqpContainerPosition containerPosition = OUT_EqpContainerPositionSeq.get(OUT_EqpContainerPositionSeq.size() - 1);
                                    containerPosition.setDestCassetteID(dummy);
                                }
                            }
                        }
                    }
                }

                for (Infos.EqpContainer eqpContainer : strEqpContainerSeq){
                    List<Infos.EqpContainerPosition> eqpContainerPositions = eqpContainer.getEqpContainerPosition();
                    Infos.EqpContainer tmpEqpContainer = new Infos.EqpContainer();
                    List<Infos.EqpContainerPosition> tmpEqpContainerPosition = tmpEqpContainer.getEqpContainerPosition();
                    for (Infos.EqpContainerPosition eqpContainerPosition : eqpContainerPositions){
                        boolean dstMapFoundFlag = false;
                        for (Infos.SlmSlotMap slmSlotMap : checkedMap){
                            if (ObjectIdentifier.equalsWithValue(eqpContainerPosition.getWaferID(), slmSlotMap.getWaferID())){
                                log.info(" ### Wafer is found.");
                                dstMapFoundFlag = true;
                                break;
                            }
                        }
                        if (!dstMapFoundFlag){
                            log.info("### Wafer isn't found.");
                            tmpEqpContainerPosition.add(eqpContainerPosition);
                        }
                    }
                    //Resize
                    if (!CimObjectUtils.isEmpty(tmpEqpContainerPosition)){
                        // Set ContainerInformation
                        tmpEqpContainer.setEquipmentContainerID(eqpContainer.getEquipmentContainerID());
                        tmpEqpContainer.setChamberID(eqpContainer.getChamberID());
                        tmpEqpContainer.setMaxCapacity(eqpContainer.getMaxCapacity());
                        tmpEqpContainer.setMaxRsvCount(eqpContainer.getMaxRsvCount());
                        tmpEqpContainer.setCurrentCapacity(eqpContainer.getCurrentCapacity());
                        OUT_EqpContainerSeq.add(tmpEqpContainer);
                    }
                }
            }
        }else {
            if (!ObjectIdentifier.isEmpty(IN_controlJobID)){
                log.info("ControlJob and Lot are specified case..  ");
                log.info("Check : controlJob's wafers are in Equipment Container Position Infromation?  ");
                //-----------------------------------------------------------------
                //controlJob's wafers vs Equipment Container Position Infromation.
                //-----------------------------------------------------------------
                for (ObjectIdentifier cjWafer : cjWafers){
                    boolean wfFoundFlag = false;
                    for (Infos.EqpContainer eqpContainer : strEqpContainerSeq){
                        List<Infos.EqpContainerPosition> eqpContainerPositions = eqpContainer.getEqpContainerPosition();
                        for (Infos.EqpContainerPosition eqpContainerPosition : eqpContainerPositions){
                            if (ObjectIdentifier.equalsWithValue(cjWafer, eqpContainerPosition.getWaferID())){
                                //-----------------------------------
                                //ControlJob is valid?
                                //-----------------------------------
                                log.info(" ### EqpContainerPosition Condition Wafer_ID/ControlJob");
                                if (!ObjectIdentifier.equalsWithValue(eqpContainerPosition.getControlJobID(), IN_controlJobID)){
                                    log.info(" ### !!!!! Error Because controlJobID in EQPContianerPosition isn't match with input ControlJobID. WaferID/CJ_IDinEQPCTN/inputCJ");
                                    throw new ServiceException(new OmCode(retCodeConfig.getCtrljobEqpctnpstUnmatch(),eqpContainerPosition.getControlJobID().getValue(),cjWafer.getValue()));
                                }
                                /************************************/
                                /*   Check SLMState                 */
                                /************************************/
                                if (CimStringUtils.equals(eqpContainerPosition.getFmcState(),BizConstant.SP_SLMSTATE_NONSLMOPE)){
                                    log.info("### SLMState = SP_SLMState_NonSLMOpe");
                                    throw new ServiceException(retCodeConfigEx.getNoneSlmOpe());
                                }
                                wfFoundFlag = true;
                                break;
                            }
                        }
                    }
                    if (!wfFoundFlag){
                        log.info(" ## !!!!! Error Because all the specified wafers are not in Equipment Container Position.");
                        throw new ServiceException(retCodeConfigEx.getNotReservedSlmAllwafers());
                    }
                }
            }else if (ObjectIdentifier.isEmpty(IN_controlJobID)){
                log.info("ControlJob is blank. and Lot is specified case..  ");
                log.info("Check : Lot's wafers are in Equipment Container Position Infromation?  ");
                //-----------------------------------------------------------
                //Lot's wafers vs Equipment Container Position Infromation.
                //-----------------------------------------------------------
                for (ObjectIdentifier waferID : waferIDList){
                    boolean wfFoundFlag = false;
                    for (Infos.EqpContainer eqpContainer : strEqpContainerSeq){
                        List<Infos.EqpContainerPosition> eqpContainerPositions = eqpContainer.getEqpContainerPosition();
                        for (Infos.EqpContainerPosition eqpContainerPosition : eqpContainerPositions){
                            if (ObjectIdentifier.equalsWithValue(waferID, eqpContainerPosition.getWaferID())){
                                log.info(" ### Found the wafer");
                                /************************************/
                                /*   Check SLMState                 */
                                /************************************/
                                if (CimStringUtils.equals(eqpContainerPosition.getFmcState(),BizConstant.SP_SLMSTATE_NONSLMOPE)){
                                    log.info("### SLMState = SP_SLMState_NonSLMOpe");
                                    throw new ServiceException(retCodeConfigEx.getNoneSlmOpe());
                                }
                                //----------------------------------------------------------------------------
                                //if controlJob is blank and lotID is specified, SLMState must be "Stored".
                                //----------------------------------------------------------------------------
                                log.info(" ### Check : if controlJob is blank and lotID is specified, all wafer's SLMState is Stored ?");
                                if (!CimStringUtils.equals(eqpContainerPosition.getFmcState(),BizConstant.SP_SLMSTATE_STORED)){
                                    log.info( " ### !!!!! Error Because if controlJob is blank, all wafer's SLMState is not Stored.");
                                    throw new ServiceException(retCodeConfigEx.getInvalidSlmStateStored());
                                }
                                wfFoundFlag = true;
                                break;
                            }
                        }
                    }
                    if (!wfFoundFlag){
                        log.info(" # !!!!! Error Because all the specified wafers are not in Equipment Container Position.");
                        throw new ServiceException(retCodeConfigEx.getNotReservedSlmAllwafers());
                    }
                }
            }
            //----------------------
            //Set OutPut Parameter
            //----------------------
            for (Infos.EqpContainer eqpContainer : strEqpContainerSeq){
                List<Infos.EqpContainerPosition> eqpContainerPositions = eqpContainer.getEqpContainerPosition();
                Infos.EqpContainer tmpEqpContainer = new Infos.EqpContainer();
                List<Infos.EqpContainerPosition> tmpEqpContainerPosition = new ArrayList<>();
                tmpEqpContainer.setEqpContainerPosition(tmpEqpContainerPosition);
                for (Infos.EqpContainerPosition eqpContainerPosition : eqpContainerPositions){
                    boolean waferFoundFlag = false;
                    for (ObjectIdentifier waferID : waferIDList){
                        if (ObjectIdentifier.equalsWithValue(waferID, eqpContainerPosition.getWaferID())){
                            log.info(" ### Wafer found. Add to OUT_EqpContainerPositionSeq");
                            waferFoundFlag = true;
                            OUT_EqpContainerPositionSeq.add(eqpContainerPosition);
                            break;
                        }
                    }
                    if (!waferFoundFlag){
                        log.info(" ### Wafer not found. Add to OUT_EqpContainerSeq");
                        tmpEqpContainerPosition.add(eqpContainerPosition);
                    }
                }
                //Resize
                if (!CimObjectUtils.isEmpty(tmpEqpContainerPosition)){
                    // Set ContainerInformation
                    tmpEqpContainer.setEquipmentContainerID(eqpContainer.getEquipmentContainerID());
                    tmpEqpContainer.setChamberID(eqpContainer.getChamberID());
                    tmpEqpContainer.setMaxCapacity(eqpContainer.getMaxCapacity());
                    tmpEqpContainer.setMaxRsvCount(eqpContainer.getMaxRsvCount());
                    tmpEqpContainer.setCurrentCapacity(eqpContainer.getCurrentCapacity());
                    OUT_EqpContainerSeq.add(tmpEqpContainer);
                }
            }
        }
        //----------------------
        //Set OutPut Parameter
        //----------------------
        Infos.EqpContainerPositionInfo strEqpContainerPositionInfo = new Infos.EqpContainerPositionInfo();
        reserveResult.setStrEqpContainerPositionInfo(strEqpContainerPositionInfo);
        Infos.EqpContainerInfo strEqpContainerInfo = new Infos.EqpContainerInfo();
        reserveResult.setStrEqpContainerInfo(strEqpContainerInfo);
        strEqpContainerPositionInfo.setEquipmentID(IN_equipmentID);
        strEqpContainerPositionInfo.setEqpContainerPositionList(OUT_EqpContainerPositionSeq);
        strEqpContainerInfo.setEquipmentID(IN_equipmentID);
        strEqpContainerInfo.setEqpContainerList(OUT_EqpContainerSeq);

        return reserveResult;
    }

    @Override
    public Results.SLMCheckConditionForPortReserveResult slmCheckConditionForPortReserve(Infos.ObjCommon objCommon,
                                                                                         String IN_actionCode,
                                                                                         ObjectIdentifier IN_equipmentID,
                                                                                         ObjectIdentifier IN_cassetteID,
                                                                                         ObjectIdentifier IN_destPortID){
        //---------------------------------
        // Put Input Parameter
        //---------------------------------
        List<Infos.EventParameter> strEventParameter = new ArrayList<>();
        strEventParameter.add(new Infos.EventParameter("ACTION_CODE",IN_actionCode));
        strEventParameter.add(new Infos.EventParameter("EQUIPMENT_ID",IN_equipmentID.getValue()));
        strEventParameter.add(new Infos.EventParameter("CASSETTE_ID",IN_cassetteID.getValue()));
        strEventParameter.add(new Infos.EventParameter("DEST_PORT_ID",IN_destPortID.getValue()));
        strEventParameter.forEach(eventParameter -> log.info("Name : " + eventParameter.getParameterName() + " Value : " + eventParameter.getParameterValue()));
        //-----------------------
        // OutPut Parameter
        //-----------------------
        Results.SLMCheckConditionForPortReserveResult reserveResult = new Results.SLMCheckConditionForPortReserveResult();
        List<Infos.EqpContainerPosition> OUT_EqpContainerPositionSeq = new ArrayList<>();
        List<Infos.EqpContainer> OUT_EqpContainerSeq = new ArrayList<>();
        //------------------------------------
        // Existence Check
        //------------------------------------
        CimCassette aCassette = baseCoreFactory.getBO(CimCassette.class, IN_cassetteID);
        //Get equipment Port information.
        Infos.EqpPortInfo strEquipment_portInfo_Get_out = equipmentMethod.equipmentPortInfoGet(objCommon, IN_equipmentID);
        List<Infos.EqpPortStatus> eqpPortStatuses = strEquipment_portInfo_Get_out.getEqpPortStatuses();
        //------------------------------------
        // Specified Port Existence Check
        //------------------------------------
        if (CimStringUtils.equals(IN_actionCode,BizConstant.SP_SLM_ACTIONCODE_PORTRESERVE)){
            log.info("Check : Specified Port Existence");
            boolean portFoundFlag = false;
            for (Infos.EqpPortStatus eqpPortStatus : eqpPortStatuses){
                if (ObjectIdentifier.equalsWithValue(eqpPortStatus.getPortID(), IN_destPortID)){
                    log.info("Specified port exists on the equipment.");
                    portFoundFlag = true;
                    break;
                }
            }
            if (!portFoundFlag){
                log.info("!!!!! Error Because the specified IN_destPortID is not found in Equipment information.");
                throw new ServiceException(new OmCode(retCodeConfig.getNotFoundPortResource(),IN_destPortID.getValue()));
            }
        }
        // Get Equipment Container Information.
        log.info("Get Equipment Container Information.");
        Infos.EqpContainerInfo strEqpContainerInfo = equipmentContainerMethod.equipmentContainerInfoGet(objCommon, IN_equipmentID);
        List<Infos.EqpContainer> strEqpContainerSeq = strEqpContainerInfo.getEqpContainerList();
        boolean castFoundFlag = false;
        for (Infos.EqpContainer eqpContainer : strEqpContainerSeq){
            List<Infos.EqpContainerPosition> eqpContainerPositions = eqpContainer.getEqpContainerPosition();
            Infos.EqpContainer tempEqpContainer = new Infos.EqpContainer();
            List<Infos.EqpContainerPosition> tempEqpContainerPosition = new ArrayList<>();
            tempEqpContainer.setEqpContainerPosition(tempEqpContainerPosition);
            for (Infos.EqpContainerPosition eqpContainerPosition : eqpContainerPositions){
                //------------------------------------------------------------------
                // IN_cassetteID is in Equipment Container Position Information?
                //------------------------------------------------------------------
                if (ObjectIdentifier.equalsWithValue(IN_cassetteID, eqpContainerPosition.getDestCassetteID())){
                    //---------------------
                    // Set OutPut Parameter
                    //---------------------
                    OUT_EqpContainerPositionSeq.add(eqpContainerPosition);
                    castFoundFlag = true;
                }else {
                    if (CimStringUtils.equals(IN_actionCode,BizConstant.SP_SLM_ACTIONCODE_PORTRESERVE)){
                        //--------------------------------------------------
                        // Action Code = PortReserve
                        // IN_destPortID is not reserved by other SLMRsv ?
                        //--------------------------------------------------
                        if (ObjectIdentifier.equalsWithValue(IN_destPortID, eqpContainerPosition.getDestPortID())){
                            log.info("!!!!! Error Because the specified IN_destPortID was reserved by other SLM Reservation.");
                            throw new ServiceException(new OmCode(retCodeConfigEx.getAlreadyReservedPortSlm(),IN_destPortID.getValue()));
                        }
                    }
                    //---------------------
                    // Set OutPut Parameter
                    //---------------------
                    tempEqpContainerPosition.add(eqpContainerPosition);
                }
            }
            //Resize
            if (!CimObjectUtils.isEmpty(tempEqpContainerPosition)){
                tempEqpContainer.setChamberID(eqpContainer.getChamberID());
                tempEqpContainer.setEquipmentContainerID(eqpContainer.getEquipmentContainerID());
                tempEqpContainer.setMaxRsvCount(eqpContainer.getMaxRsvCount());
                tempEqpContainer.setMaxCapacity(eqpContainer.getMaxCapacity());
                tempEqpContainer.setCurrentCapacity(eqpContainer.getCurrentCapacity());
                OUT_EqpContainerSeq.add(tempEqpContainer);
            }
        }

        log.info("Check : IN_cassetteID is found in Equipment Container Position information ?");
        if (!castFoundFlag){
            log.info("!!!!! Error Because the specified IN_cassetteID is not found in Equipment Container Position information.");
            throw new ServiceException(new OmCode(retCodeConfigEx.getUndefinedDestCastSlm(),IN_cassetteID.getValue()));
        }
        //------------------------------------------------
        // Action Code : Port Reserve
        // Check CassetteCategory  (cassette vs port)
        //------------------------------------------------
        log.info("Check : Cassette Category (cassette vs port) ? (PortReserve)");
        if (CimStringUtils.equals(IN_actionCode,BizConstant.SP_SLM_ACTIONCODE_PORTRESERVE)){
            String destCastCategory = aCassette.getCassetteCategory();
            boolean cateFoundFlag = false;
            for (Infos.EqpPortStatus eqpPortStatus : eqpPortStatuses){
                if (ObjectIdentifier.equalsWithValue(IN_destPortID, eqpPortStatus.getPortID())){
                    //---------------------------------------------
                    // Equipment Port Mode is Auto ? (PortReserve)
                    //---------------------------------------------
                    log.info("Check : Equipment Port Mode is Auto ? (PortReserve)");
                    if (!CimStringUtils.equals(BizConstant.SP_EQP_ACCESSMODE_AUTO,eqpPortStatus.getAccessMode())){
                        log.info("!!!!! Error Because the specified Port's accessMode  is not Auto.");
                        throw new ServiceException(new OmCode(retCodeConfigEx.getSlmEqpPortAccessMode(),
                                IN_equipmentID.getValue(),IN_destPortID.getValue(),eqpPortStatus.getAccessMode()));
                    }
                    List<String> cassetteCategoryCapabilities = eqpPortStatus.getCassetteCategoryCapability();
                    if (cassetteCategoryCapabilities.size()==0){
                        //no required cassette category specified
                        cateFoundFlag = true;
                    }else {
                        for (String cassetteCategoryCapability : cassetteCategoryCapabilities){
                            if (CimStringUtils.equals(cassetteCategoryCapability,destCastCategory)){
                                log.info("Cassette Category is found in port's category capabilities.");
                                cateFoundFlag = true;
                                break;
                            }
                        }
                    }
                }
            }
            if (!cateFoundFlag){
                log.info("!!!!! Error Because the specified IN_cassetteID's category is not found in the port's cassette category capabilities.");
                throw new ServiceException(new OmCode(retCodeConfigEx.getCarrierPortCarrierCategoryUnmatch(),
                        IN_cassetteID.getValue(),IN_equipmentID.getValue(),IN_destPortID.getValue()));
            }
        }
        //---------------------------------------
        // Carrier is reserved for dispatching?
        //---------------------------------------
        boolean dispatchReservedFlag = aCassette.isDispatchReserved();
        if (CimBooleanUtils.isTrue(dispatchReservedFlag)){
            String NPWLoadPurposeType = aCassette.getNPWLoadPurposeType();
            if (CimStringUtils.equals(BizConstant.SP_LOADPURPOSETYPE_SLMRETRIEVING,NPWLoadPurposeType)){
                log.info("!!!!! Error Because cassette is reserved");
                throw new ServiceException(retCodeConfig.getAlreadyDispatchReservedCassette());
            }
        }
        //----------------------
        //Set OutPut Parameter
        //----------------------
        Infos.EqpContainerInfo reltEqpContainerInfo = reserveResult.getStrEqpContainerInfo();
        Infos.EqpContainerPositionInfo reltEqpContainerPositionInfo = reserveResult.getStrEqpContainerPositionInfo();
        reltEqpContainerInfo.setEquipmentID(IN_equipmentID);
        reltEqpContainerInfo.setEqpContainerList(OUT_EqpContainerSeq);
        reltEqpContainerPositionInfo.setEquipmentID(IN_equipmentID);
        reltEqpContainerPositionInfo.setEqpContainerPositionList(OUT_EqpContainerPositionSeq);
        return reserveResult;
    }

    @Override
    public void slmMaterialOutSpecCombinationCheck(Infos.ObjCommon objCommon, Infos.EqpContainerPositionInfo eqpContainerPositionInfo){
        //---------------------------------
        // Put Input Parameter
        //---------------------------------
        ObjectIdentifier IN_equipmentID = eqpContainerPositionInfo.getEquipmentID();
        List<Infos.EqpContainerPosition> IN_eqpContainerPositionSeq = eqpContainerPositionInfo.getEqpContainerPositionList();
        int IN_eqpCntPsnCnt =0;
        List<Infos.SlmSlotMap> strSrcMapSeq = new ArrayList<>();
        List<Infos.SlmSlotMap> strDstMapSeq = new ArrayList<>();
        for (Infos.EqpContainerPosition eqpContainerPosition : IN_eqpContainerPositionSeq){
            Infos.SlmSlotMap srcSlmSlotMap = new Infos.SlmSlotMap();
            srcSlmSlotMap.setCassetteID(eqpContainerPosition.getSrcCassetteID());
            srcSlmSlotMap.setSlotNumber(eqpContainerPosition.getSrcSlotNo());
            srcSlmSlotMap.setWaferID(eqpContainerPosition.getWaferID());
            strSrcMapSeq.add(srcSlmSlotMap);

            Infos.SlmSlotMap srcDstSlotMap = new Infos.SlmSlotMap();
            srcDstSlotMap.setCassetteID(eqpContainerPosition.getDestCassetteID());
            srcDstSlotMap.setSlotNumber(eqpContainerPosition.getDestSlotNo());
            srcDstSlotMap.setWaferID(eqpContainerPosition.getWaferID());
            strDstMapSeq.add(srcDstSlotMap);
        }
        //-----------------------------
        // Sort DestMap by Cassette
        //-----------------------------
        Infos.SlmSlotMap tmpMap = new Infos.SlmSlotMap();
        Collections.sort(strDstMapSeq, comparing(map -> map.getCassetteID().getValue()));
        //-----------------------------
        // Duplicate Check
        //-----------------------------
        int  strDstMapSeqLen = strDstMapSeq.size();
        for (int i = 0 ; i < strDstMapSeqLen ; i++){
            if (!CimObjectUtils.isEmpty(strDstMapSeq.get(i))){
                for (int j = i+1 ; j < strDstMapSeqLen ; j++){
                    if (CimObjectUtils.isEmpty(strDstMapSeq.get(j))){
                        continue;
                    }else if (ObjectIdentifier.equalsWithValue(strDstMapSeq.get(i).getCassetteID(), strDstMapSeq.get(j).getCassetteID())
                           && strDstMapSeq.get(i).getSlotNumber() == strDstMapSeq.get(j).getSlotNumber()){
                        log.info(" !!!!! Error Because SlotNumber is duplicate. ");
                        throw new ServiceException(retCodeConfigEx.getSlmInvalidParameterForSlotDuplicate());
                    }
                }
            }
        }
        //-------------------------------------------
        // The wafer in the slot is reserved by SLM?
        //-------------------------------------------
        ObjectIdentifier tmpCast = new ObjectIdentifier();
        for (Infos.SlmSlotMap slmSlotMap : strDstMapSeq){
            List<Infos.WaferMapInCassetteInfo> strCassette_GetWaferMapDR_out = new ArrayList<>();
            if (!ObjectIdentifier.isEmpty(slmSlotMap.getCassetteID())){
                if (ObjectIdentifier.isEmpty(tmpCast)){
                    tmpCast = slmSlotMap.getCassetteID();
                }
                if (!ObjectIdentifier.equalsWithValue(tmpCast, slmSlotMap.getCassetteID())){
                    tmpCast = slmSlotMap.getCassetteID();
                    //------------------------------
                    // Get Wafer Map in Cassette .
                    //------------------------------
                    strCassette_GetWaferMapDR_out = cassetteMethod.cassetteGetWaferMapDR(objCommon, tmpCast);
                }else {
                    ObjectIdentifier tmpWafer = new ObjectIdentifier();
                    for (Infos.WaferMapInCassetteInfo waferMapInCassette : strCassette_GetWaferMapDR_out){
                        //The specified Slot is empty?
                        if (waferMapInCassette.getSlotNumber() == slmSlotMap.getSlotNumber()){
                            if (!ObjectIdentifier.isEmpty(waferMapInCassette.getWaferID())){
                                tmpWafer = waferMapInCassette.getWaferID();
                                break;
                            }
                        }
                    }
                    //If the specified slot is not empty, the wafer in the slot is reserved by SLM.
                    if (!ObjectIdentifier.isEmpty(tmpWafer)){
                        log.info(" ##### A wafer is in the slot .         ");
                        Inputs.ObjEquipmentContainerPositionInfoGetIn positionInfoGetIn = new Inputs.ObjEquipmentContainerPositionInfoGetIn();
                        positionInfoGetIn.setEquipmentID(IN_equipmentID);
                        positionInfoGetIn.setKey(tmpWafer);
                        positionInfoGetIn.setKeyCategory(BizConstant.SP_SLM_KEYCATEGORY_WAFER);
                        Infos.EqpContainerPositionInfo strEquipmentContainerPosition_info_Get_out = equipmentContainerPositionMethod.equipmentContainerPositionInfoGet(objCommon, positionInfoGetIn);
                        if (CimObjectUtils.isEmpty(strEquipmentContainerPosition_info_Get_out)){
                            log.info(" !!!!! Error Because SlotNumber is duplicate. ");
                            throw new ServiceException(retCodeConfigEx.getSlmInvalidParameterForSlotDuplicate());
                        }
                    }
                }
            }
        }
    }

    @Override
    public List<Infos.MtrlOutSpec> slmStartReserveInfoForDeliveryMake(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, String portGroupID, List<Infos.StartCassette> startCassetteList) {

        List<Infos.MtrlOutSpec> out = new ArrayList<>();
        log.info("InParam [equipmentID] : {}", ObjectIdentifier.fetchValue(equipmentID));
        log.info("InParam [portGroupID] : {}",portGroupID);

        int lenCast = CimArrayUtils.getSize(startCassetteList);
        log.info("lenCast: {}",lenCast);

        int mtrlOutIdx = 0;
        if (lenCast > 0){
            for (Infos.StartCassette startCassette : startCassetteList) {
                out.add(mtrlOutIdx,new Infos.MtrlOutSpec());
                out.get(mtrlOutIdx).setDestinationMapList(new ArrayList<>());
                out.get(mtrlOutIdx).setSourceMapList(new ArrayList<>());

                Boolean bOpeStartLot = false;
                int lenLot = CimArrayUtils.getSize(startCassette.getLotInCassetteList());
                log.info("lenLot: {}",lenLot);
                if (lenLot > 0){
                    for (Infos.LotInCassette lotInCassette : startCassette.getLotInCassetteList()) {
                        log.info("lotID: {}", ObjectIdentifier.fetchValue(lotInCassette.getLotID()));
                        if (CimBooleanUtils.isFalse(lotInCassette.getMoveInFlag())){
                            continue;
                        }
                        bOpeStartLot = true;
                        int lenWafer = CimArrayUtils.getSize(lotInCassette.getLotWaferList());
                        log.info("lenWafer: {}",lenWafer);

                        if (lenWafer > 0){
                            for (Infos.LotWafer lotWafer : lotInCassette.getLotWaferList()) {
                                Infos.SlmSlotMap slmSlotMap = new Infos.SlmSlotMap();
                                slmSlotMap.setCassetteID(startCassette.getCassetteID());
                                slmSlotMap.setSlotNumber(lotWafer.getSlotNumber().intValue());
                                slmSlotMap.setWaferID(lotWafer.getWaferID());
                                out.get(mtrlOutIdx).getSourceMapList().add(slmSlotMap);
                                out.get(mtrlOutIdx).getDestinationMapList().add(slmSlotMap);
                            }
                        }
                    }
                }
                if (CimBooleanUtils.isTrue(bOpeStartLot)){
                    mtrlOutIdx++;
                }
            }
        }
        return out;
    }

}
