package com.fa.cim.method.impl;

import com.fa.cim.annotaion.OmMethod;
import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.OmCode;
import com.fa.cim.common.utils.*;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Outputs;
import com.fa.cim.dto.Results;
import com.fa.cim.method.*;
import com.fa.cim.newcore.bo.dispatch.CimDispatcher;
import com.fa.cim.newcore.bo.durable.CimCassette;
import com.fa.cim.newcore.bo.durable.CimProcessDurable;
import com.fa.cim.newcore.bo.machine.CimMachine;
import com.fa.cim.newcore.bo.machine.CimPortResource;
import com.fa.cim.newcore.bo.product.CimLot;
import com.fa.cim.newcore.bo.recipe.CimLogicalRecipe;
import com.fa.cim.newcore.bo.recipe.CimMachineRecipe;
import com.fa.cim.newcore.dto.dispatch.DispatcherDTO;
import com.fa.cim.newcore.factory.BaseCoreFactory;
import com.fa.cim.newcore.impl.bo.env.StandardProperties;
import com.fa.cim.newcore.standard.prdctmng.Lot;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

/**
 * description:
 * <p>WhereNextTransferEqp .<br/></p>
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2018/11/8        ********             Yuri               create file
 * 2019/9/23        ######              Neko                Refactor: change retCode to exception
 *
 * @author Yuri
 * @since 2018/11/8 13:31
 * Copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@OmMethod
@Slf4j
public class WhereNextMethod implements IWhereNextMethod {

    @Autowired
    private RetCodeConfig retCodeConfig;

    @Autowired
    private ICassetteMethod cassetteMethod;

    @Autowired
    private IEquipmentMethod equipmentMethod;

    @Autowired
    private ILotMethod lotMethod;

    @Autowired
    private IWhatNextMethod whatNextMethod;

    @Autowired
    private BaseCoreFactory baseCoreFactory;

    @Autowired
    private IDurableMethod durableMethod;

    @Autowired
    private IEquipmentForDurableMethod equipmentForDurableMethod;

    @Override
    public Outputs.WhereNextTransferEqpOut whereNextTransferEqp(Infos.ObjCommon objCommonIn,
                                                                ObjectIdentifier equipmentID,
                                                                Outputs.EquipmentTargetPortPickupOut equipmentTargetPortPickupOut) {
        Outputs.WhereNextTransferEqpOut out = new Outputs.WhereNextTransferEqpOut();
        out.setStartCassetteList(new ArrayList<>());
        Long searchCondition = 0L;
        String searchConditionVar = StandardProperties.OM_CONSTRAINT_CHK_WITH_SEARCH.getValue();
        if (CimStringUtils.length(searchConditionVar) > 0){
            searchCondition = Long.valueOf(searchConditionVar);
        }

        List<Infos.PortGroup> targetPortGroup = equipmentTargetPortPickupOut.getEqpTargetPortInfo().getPortGroups();

        int lenPortGroup = CimArrayUtils.getSize(targetPortGroup);

        // check if the port group list is empty
        Validations.check(0 == lenPortGroup,retCodeConfig.getNotFoundTargetPort() );

        List<Infos.PortID> targetPortID = targetPortGroup.get(0).getStrPortID();
        int lenPortID = CimArrayUtils.getSize(targetPortID);
        for (int i = 0; i < lenPortGroup; i++) {
            Infos.PortID iPortID = targetPortID.get(i);
            CimCassette cassette = baseCoreFactory.getBO(CimCassette.class, iPortID.getCassetteID());
            Validations.check(null == cassette, new OmCode(retCodeConfig.getNotFoundCassette(), ObjectIdentifier.fetchValue(iPortID.getCassetteID())));

            //SLMReserveMachine;
            CimMachine aSLMReservedMachine = cassette.getSLMReservedMachine();
            Validations.check(!CimObjectUtils.isEmpty(aSLMReservedMachine),
                    new OmCode(retCodeConfig.getAlreadyReservedCassetteSlm(), ObjectIdentifier.fetchValue(iPortID.getCassetteID())));

            //【step-1】cassette_multiLotType_Get
            String multiLotType = null;
            try {
                multiLotType = cassetteMethod.cassetteMultiLotTypeGet(objCommonIn, iPortID.getCassetteID());
            }catch (ServiceException e) {
                if (!Validations.isEquals(retCodeConfig.getNotFoundCassette(), e.getCode())) {
                    throw e;
                }
            }

            Validations.check(!CimStringUtils.equals(multiLotType, BizConstant.SP_CAS_MULTILOTTYPE_SINGLELOTSINGLERECIPE) , retCodeConfig.getCassetteEquipmentConditionError());

            Infos.CassetteDBINfoGetDRInfo cassetteDBINfoGetDRInfo = new Infos.CassetteDBINfoGetDRInfo();
            cassetteDBINfoGetDRInfo.setCassetteID(iPortID.getCassetteID());
            cassetteDBINfoGetDRInfo.setDurableOperationInfoFlag(false);
            cassetteDBINfoGetDRInfo.setDurableWipOperationInfoFlag(false);

            //【step2】 - cassette_DBInfo_GetDR__170
            Outputs.CassetteDBInfoGetDROut cassetteDBInfoOut = cassetteMethod.cassetteDBInfoGetDR(objCommonIn, cassetteDBINfoGetDRInfo);
            List<Infos.ContainedLotInfo> strContainedLotInfo = cassetteDBInfoOut.getCarrierDetailInfoInqResult().getCassetteStatusInfo().getStrContainedLotInfo();
            for (Infos.ContainedLotInfo lotInfo : strContainedLotInfo) {
                Validations.check(CimBooleanUtils.isTrue(lotInfo.isAutoDispatchDisableFlag()), retCodeConfig.getInvalidCastDispatchStat());
            }
        }
        //*************************************************************************************
        // get next EQP list
        //
        //  EQP-1
        // +--------------------+
        // |  P1        P2      |  Cas1-NextEqpList ---> [EQP-2], [EQP-3], [EQP-4]
        // | +------+  +------+ |  Cas2-NextEqpList ---> [EQP-3], [EQP-4], [EQP-5]
        // | | Cas1 |  | Cas2 | |
        // | +------+  +------+ |
        // +--------------------+
        //
        //*************************************************************************************
        lenPortGroup = CimArrayUtils.getSize(equipmentTargetPortPickupOut.getEqpTargetPortInfo().getPortGroups().get(0).getStrPortID());
        List<Results.WhereNextStockerInqResult> strWhereNextStockerInqResults = new ArrayList<>();
        for (int i = 0; i < lenPortGroup; i++) {
            //Get Where Next EQP
            // 【step3】 - cassette_destinationInfo_Get
            ObjectIdentifier dummyLotId = new ObjectIdentifier();
            Outputs.CassetteDestinationInfoGetOut cassetteDestinationInfoGetOutRetCode = cassetteMethod.cassetteDestinationInfoGet(objCommonIn,
                    equipmentTargetPortPickupOut.getEqpTargetPortInfo().getPortGroups().get(0).getStrPortID().get(i).getCassetteID(),
                    dummyLotId);

            strWhereNextStockerInqResults.add(cassetteDestinationInfoGetOutRetCode.getDestinationOrder());
        }
        //*************************************************************************************
        // AND list EQP
        //
        // Cas1-NextEqpList ---> [EQP-2], [EQP-3], [EQP-4]
        // Cas2-NextEqpList ---> [EQP-3], [EQP-4], [EQP-5]
        //    |
        //    V
        // eqpIDs -------------> [EQP-3], [EQP-4]
        //
        //*************************************************************************************
        log.info(" AND list EQP");
        List<ObjectIdentifier> eqpIDs = new ArrayList<>();
        List<ObjectIdentifier> tmpEqpIDs = new ArrayList<>();
        for (int i = 0; i < lenPortGroup; i++) {
            int eqpIDCnt = 0;
            int lenNextEqp = CimArrayUtils.getSize(strWhereNextStockerInqResults.get(i).getWhereNextEqpStatus());
            for (int j = 0; j < lenNextEqp; j++) {
                if (i == 0){
                    tmpEqpIDs.add(strWhereNextStockerInqResults.get(i).getWhereNextEqpStatus().get(j).getEquipmentID());
                    eqpIDCnt++;
                }else {
                    int lenEqpID = CimArrayUtils.getSize(eqpIDs);
                    for (int k = 0; k < lenEqpID; k++) {
                        if (ObjectIdentifier.equalsWithValue(eqpIDs.get(k), strWhereNextStockerInqResults.get(i).getWhereNextEqpStatus().get(j).getEquipmentID())){
                            tmpEqpIDs.add(eqpIDCnt,strWhereNextStockerInqResults.get(i).getWhereNextEqpStatus().get(j).getEquipmentID());
                            eqpIDCnt++;
                            break;
                        }
                    }
                }
            }
            eqpIDs = tmpEqpIDs;
            tmpEqpIDs = new ArrayList<>();
        }
        //Omit InternalBufferEQP from eqpIDs
        tmpEqpIDs = eqpIDs;
        int nSetCnt = 0;
        int lenEqpIDs = CimArrayUtils.getSize(tmpEqpIDs);
        eqpIDs = new ArrayList<>();
        for (int i = 0; i < lenEqpIDs; i++) {
            CimMachine aPosMachine = baseCoreFactory.getBO(CimMachine.class, tmpEqpIDs.get(i));
            Validations.check(aPosMachine == null, new OmCode(retCodeConfig.getNotFoundEqp(), tmpEqpIDs.get(i).getValue()));
            String strMachineCategory = aPosMachine.getCategory();
            if (!CimStringUtils.equals(BizConstant.SP_MC_CATEGORY_INTERNALBUFFER,strMachineCategory)){
                eqpIDs.add(nSetCnt,tmpEqpIDs.get(i));
                nSetCnt++;
            }else {
                log.info("Omit!! InternalBufferEQP!!");
            }
        }
        //Get Target EQP Unload Port Count
        log.info("Get Target EQP Unload Port Count");
        int tgtEqpUnloadPortCnt = 0;
        lenPortID = CimArrayUtils.getSize(equipmentTargetPortPickupOut.getEqpTargetPortInfo().getPortGroups().get(0).getStrPortID());
        for (int i = 0; i < lenPortID; i++) {
            if (CimStringUtils.equals(BizConstant.SP_PORTRSC_PORTSTATE_UNLOADREQ, equipmentTargetPortPickupOut.getEqpTargetPortInfo().getPortGroups().get(0).getStrPortID().get(i).getPortState())){
                tgtEqpUnloadPortCnt ++;
            }
        }
        //Check ToEquipment. and Pickup PortGroup Information
        log.info("Check ToEquipment. and Pickup PortGroup Information");
        nSetCnt = 0;
        List<Infos.EqpOnePortGroupInfo> eqpOnePortGroupInfoList = new ArrayList<>();
        tmpEqpIDs = eqpIDs;
        int lenToEqp = CimArrayUtils.getSize(tmpEqpIDs);
        for (int i = 0; i < lenToEqp; i++) {
            // Check To EQP Process Run Max & Min
            log.info("Check To EQP Process Run Max & Min");
            CimMachine aToMachine = baseCoreFactory.getBO(CimMachine.class, tmpEqpIDs.get(i));
            Validations.check(null == aToMachine, new OmCode(retCodeConfig.getNotFoundEqp(), ObjectIdentifier.fetchValue(tmpEqpIDs.get(i))));
            int runSizeMax = aToMachine.getProcessRunSizeMaximum();
            int runSizeMin = aToMachine.getProcessRunSizeMinimum();
            if (tgtEqpUnloadPortCnt > runSizeMax || tgtEqpUnloadPortCnt < runSizeMin){
                log.info("continue!!  tgtEqpUnloadPortCnt > runSizeMax || tgtEqpUnloadPortCnt < runSizeMin");
                continue;
            }
            //Get Equipment Information
            log.info("call equipment_brInfo_GetDR__120()");
            //【step4】 - equipment_brInfo_GetDR__120
            Infos.EqpBrInfo eqpBrInfo = equipmentMethod.equipmentBRInfoGetDR(objCommonIn, tmpEqpIDs.get(i));
            log.info("call equipment_statusInfo_GetDR__090()");
            //【step5】 - equipment_statusInfo_GetDR__090
            Infos.EqpStatusInfo eqpStatusInfo = equipmentMethod.equipmentStatusInfoGetDR(objCommonIn, tmpEqpIDs.get(i));
            log.info("call equipment_portInfo_GetDR()");
            //【step6】 - equipment_portInfo_GetDR
            Infos.EqpPortInfo eqpPortInfo = equipmentMethod.equipmentPortInfoGet(objCommonIn, tmpEqpIDs.get(i));
            if (CimBooleanUtils.isFalse(eqpStatusInfo.getEquipmentAvailableFlag())){
                log.info("TRUE != EqpInfo.equipmentAvailableFlag  ...<continue>");
                continue;
            }
            //Sorting Port Group Info by Port Group
            //【step7】 - equipment_portInfo_SortByGroup
            log.info("call equipment_portInfo_SortByGroup()");
            Infos.EqpPortInfoOrderByGroup eqpPortInfoOrderByGroupRetCode = equipmentMethod.equipmentPortInfoSortByGroup(objCommonIn,
                    tmpEqpIDs.get(i),
                    eqpPortInfo.getEqpPortStatuses());
            /*------------------------------------------------------------------------------*/
            /*   Pickup Target Port of ToEquipment                                          */
            /*                                                                              */
            /*   equipment_targetPort_Pickup is carried out for every PortGroup.            */
            /*   Because, equipment_targetPort_Pickup returns only PortGroup found first.   */
            /*------------------------------------------------------------------------------*/
            log.info("call equipment_targetPort_Pickup()");
            int lenPortGroupNew = CimArrayUtils.getSize(eqpPortInfoOrderByGroupRetCode.getStrPortGroup());
            for (int j = 0; j < lenPortGroupNew; j++) {
                Infos.EqpPortInfoOrderByGroup strTmpPortSortByGroup = new Infos.EqpPortInfoOrderByGroup();
                strTmpPortSortByGroup.setEquipmentID(tmpEqpIDs.get(i));
                Infos.PortGroup portGroup = eqpPortInfoOrderByGroupRetCode.getStrPortGroup().get(j);
                List<Infos.PortGroup> portGroups = new ArrayList<>();
                portGroups.add(portGroup);
                strTmpPortSortByGroup.setStrPortGroup(portGroups);
                //【step8】 - equipment_targetPort_Pickup
                Outputs.EquipmentTargetPortPickupOut object = new Outputs.EquipmentTargetPortPickupOut();
                Infos.EqpBrInfo strEqpBrInfo = eqpBrInfo;
                object.setWhatsNextRequireFlag(false);
                //【step8】 - equipment_targetPort_Pickup
                try {
                    object = equipmentMethod.equipmentTargetPortPickup(objCommonIn, strTmpPortSortByGroup, strEqpBrInfo, eqpPortInfo);
                } catch (ServiceException e) {
                    continue;
                }
                if (!CimStringUtils.equals(BizConstant.SP_PORTRSC_PORTSTATE_LOADREQ, object.getTargetPortType())){
                    log.info("TargetPortType is not [LoadReq]  ...<continue>");
                    continue;
                }
                int lenTargetPortGroup = CimArrayUtils.getSize(strTmpPortSortByGroup.getStrPortGroup());
                if (lenTargetPortGroup == 0){
                    continue;
                }
                Infos.EqpOnePortGroupInfo eqpOnePortGroupInfo = new Infos.EqpOnePortGroupInfo();
                eqpOnePortGroupInfo.setEquipmentID(tmpEqpIDs.get(i));
                eqpOnePortGroupInfo.setStrPortGroup(strTmpPortSortByGroup.getStrPortGroup().get(0));
                eqpOnePortGroupInfoList.add(nSetCnt,eqpOnePortGroupInfo);
                nSetCnt ++;
            }
        }
        log.info("Pickup PortGroup Count,{}",nSetCnt);
        //*************************************************************************************
        // sort old order of portGroup timeStamp
        //
        // EQP-3 --- PG-A
        //       P1 : dispatchState_TimeStamp: 2000/05/03 18:23:08  <--- This!
        //       P2 : dispatchState_TimeStamp: 2000/05/01 12:14:28
        //
        // EQP-3 --- PG-B
        //       P1 : dispatchState_TimeStamp: 2000/05/02 11:47:13  <--- This!
        //       P2 : dispatchState_TimeStamp: 2000/05/01 02:54:31
        //
        // EQP-4 --- PG-A
        //       P1 : dispatchState_TimeStamp: 2000/05/02 20:21:49
        //       P2 : dispatchState_TimeStamp: 2000/05/03 14:58:26  <--- This!
        //
        // pptEqpOnePortGroupInfoSeq ---> [EQP3 - PG-A], [EQP3 - PG-B], [EQP4 - PG-A]
        //          |
        //          V
        // pptEqpOnePortGroupInfoSeq ---> [EQP3 - PG-B], [EQP4 - PG-A], [EQP3 - PG-A]
        //
        //*************************************************************************************
        log.info("pickup most last timeStamp PortGroup");
        Infos.EqpOnePortGroupInfo tmpEqpPortGroupInfoSeq = new Infos.EqpOnePortGroupInfo();
        String tmpTime = null;
        List<Integer> portNumSeq = new ArrayList<>();
        Integer lenEqpPortGroupInfoSeq = 0;
        lenEqpPortGroupInfoSeq = CimArrayUtils.getSize(eqpOnePortGroupInfoList);
        for (int i = 0; i < lenEqpPortGroupInfoSeq; i++) {
            //Get High Priority Port
            //KeyPort
            portNumSeq.add(0);
            lenPortID = CimArrayUtils.getSize(eqpOnePortGroupInfoList.get(i).getStrPortGroup().getStrPortID());
            tmpTime = eqpOnePortGroupInfoList.get(i).getStrPortGroup().getStrPortID().get(0).getDispatchStateTimeStamp();
            for (int j = 0; j < lenPortID; j++) {
                if ( CimDateUtils.convertToOrInitialTime(eqpOnePortGroupInfoList.get(i).getStrPortGroup().getStrPortID().get(j).getDispatchStateTimeStamp()).compareTo( CimDateUtils.convertToOrInitialTime(tmpTime)) > 0){
                    tmpTime = eqpOnePortGroupInfoList.get(i).getStrPortGroup().getStrPortID().get(j).getDispatchStateTimeStamp();
                    portNumSeq.set(i, j);
                }
            }
        }
        for (int i = 0; i < lenEqpPortGroupInfoSeq; i++) {
            //sort
            for (int j = 0; j < i; j++) {
                if (CimDateUtils.convertToOrInitialTime(eqpOnePortGroupInfoList.get(i).getStrPortGroup().getStrPortID().get(portNumSeq.get(i)).getDispatchStateTimeStamp()).compareTo(
                        CimDateUtils.convertToOrInitialTime(eqpOnePortGroupInfoList.get(j).getStrPortGroup().getStrPortID().get(portNumSeq.get(j)).getDispatchStateTimeStamp())) < 0){
                    tmpEqpPortGroupInfoSeq = eqpOnePortGroupInfoList.get(j);
                    eqpOnePortGroupInfoList.set(j, eqpOnePortGroupInfoList.get(i));
                    eqpOnePortGroupInfoList.set(i, tmpEqpPortGroupInfoSeq);
                }
            }
        }
        //run check
        log.info("run check");
        Results.WhatNextLotListResult strWhatNextInqResult = new Results.WhatNextLotListResult();
        int nowMaxLen = 0;
        int lenPortIDs = CimArrayUtils.getSize(equipmentTargetPortPickupOut.getEqpTargetPortInfo().getPortGroups().get(0).getStrPortID());
        List<Infos.WhatNextAttributes> whatNextAttributes = new ArrayList<>();
        for (int i = 0; i < lenPortIDs; i++) {
            ObjectIdentifier cassetteId = equipmentTargetPortPickupOut.getEqpTargetPortInfo().getPortGroups().get(0).getStrPortID().get(i).getCassetteID();
            CimCassette aCassette = baseCoreFactory.getBO(CimCassette.class, cassetteId);
            Validations.check(aCassette == null, new OmCode(retCodeConfig.getNotFoundCassette(), ObjectIdentifier.fetchValue(cassetteId)));
            List<Lot> aLotSequence = aCassette.allLots();
            nowMaxLen = CimArrayUtils.getSize(strWhatNextInqResult.getStrWhatNextAttributes());
            int lenLotID = CimArrayUtils.getSize(aLotSequence);
            for (int j = 0; j < lenLotID; j++) {
                //Get Lot Info
                CimLot aLot = (CimLot) aLotSequence.get(j);
                String lotId = aLot.getIdentifier();
                ObjectIdentifier lotIDObj = ObjectIdentifier.buildWithValue(lotId);

                Infos.LotInfoInqFlag lotInfoInqFlag = new Infos.LotInfoInqFlag();
                lotInfoInqFlag.setLotBasicInfoFlag(true);
                lotInfoInqFlag.setLotControlUseInfoFlag(false);
                lotInfoInqFlag.setLotFlowBatchInfoFlag(false);
                lotInfoInqFlag.setLotNoteFlagInfoFlag(false);
                lotInfoInqFlag.setLotOperationInfoFlag(false);
                lotInfoInqFlag.setLotOrderInfoFlag(false);
                lotInfoInqFlag.setLotControlJobInfoFlag(false);
                lotInfoInqFlag.setLotProductInfoFlag(false);
                lotInfoInqFlag.setLotRecipeInfoFlag(true);
                lotInfoInqFlag.setLotLocationInfoFlag(true);
                lotInfoInqFlag.setLotWipOperationInfoFlag(false);
                lotInfoInqFlag.setLotWaferAttributesFlag(false);
                lotInfoInqFlag.setLotBackupInfoFlag(false);

                //【step9】 - lot_detailInfo_GetDR__160
                Infos.LotInfo lotDetailInfoRetCode = lotMethod.lotDetailInfoGetDR(objCommonIn, lotInfoInqFlag, lotIDObj);

                strWhatNextInqResult.setStrWhatNextAttributes(whatNextAttributes);
                Infos.WhatNextAttributes whatNextAttribute = new Infos.WhatNextAttributes();
                whatNextAttributes.add(nowMaxLen + j,whatNextAttribute);
                whatNextAttribute.setLotID(lotDetailInfoRetCode.getLotBasicInfo().getLotID());
                whatNextAttribute.setCassetteID(equipmentTargetPortPickupOut.getEqpTargetPortInfo().getPortGroups().get(0).getStrPortID().get(i).getCassetteID());
                whatNextAttribute.setLogicalRecipeID(lotDetailInfoRetCode.getLotRecipeInfo().getLogicalRecipeID());
                whatNextAttribute.setTransferStatus(lotDetailInfoRetCode.getLotLocationInfo().getTransferStatus());
                whatNextAttribute.setEquipmentID(equipmentID);
            }
            nowMaxLen += lenLotID;
        }
        //End Make pptWhatNextInqResult Struct Info
        List<Infos.StartCassette> whatNextLotListToStartCarrierTransferReqOut = null;
        List<Infos.PortGroup> portGroups = new ArrayList<>();
        int lenEqpPortGroupInfo = CimArrayUtils.getSize(eqpOnePortGroupInfoList);
        Boolean bOk = false;
        for (int i = 0; i < lenEqpPortGroupInfo; i++) {
            CimMachine aMachine = baseCoreFactory.getBO(CimMachine.class, eqpOnePortGroupInfoList.get(i).getEquipmentID());
            Validations.check(null == aMachine, new OmCode(retCodeConfig.getNotFoundEqp(), ObjectIdentifier.fetchValue(eqpOnePortGroupInfoList.get(i).getEquipmentID())));
            ObjectIdentifier empty = new ObjectIdentifier();
            int lenWipLots = CimArrayUtils.getSize(strWhatNextInqResult.getStrWhatNextAttributes());
            for (int j = 0; j < lenWipLots; j++) {
                // reset information
                List<Infos.WhatNextAttributes> strWhatNextAttributes = strWhatNextInqResult.getStrWhatNextAttributes();
                strWhatNextAttributes.get(j).setMachineRecipeID(empty);
                strWhatNextAttributes.get(j).setPhysicalRecipeID("");
                CimLot aLot = baseCoreFactory.getBO(CimLot.class, strWhatNextInqResult.getStrWhatNextAttributes().get(j).getLotID());
                Validations.check(null == aLot, new OmCode(retCodeConfig.getNotFoundLot(), ObjectIdentifier.fetchValue(strWhatNextInqResult.getStrWhatNextAttributes().get(j).getLotID())));
                CimLogicalRecipe aLogicalRecipe = null;
                if (!ObjectIdentifier.isEmptyWithValue(strWhatNextInqResult.getStrWhatNextAttributes().get(j).getLogicalRecipeID())){
                    aLogicalRecipe = baseCoreFactory.getBO(CimLogicalRecipe.class, strWhatNextInqResult.getStrWhatNextAttributes().get(j).getLogicalRecipeID());
                    Validations.check(aLogicalRecipe == null, retCodeConfig.getNotFoundLogicalRecipe());
                }
                CimMachineRecipe aMachineRecipe=null;
                // Check the DOC information
                //【step10】 - lot_effectiveFPCInfo_Get
                String exchangeType = BizConstant.SP_FPC_EXCHANGETYPE_STARTRESERVEINFO;
                Outputs.ObjLotEffectiveFPCInfoGetOut lotEffectiveFPCInfoGetOutRetCode = lotMethod.lotEffectiveFPCInfoGet(objCommonIn,
                        exchangeType, eqpOnePortGroupInfoList.get(i).getEquipmentID(), strWhatNextInqResult.getStrWhatNextAttributes().get(j).getLotID());

                if (CimBooleanUtils.isTrue(lotEffectiveFPCInfoGetOutRetCode.isMachineRecipeActionRequiredFlag())){
                    log.info("machineRecipeID should be changed.");
                    strWhatNextInqResult.getStrWhatNextAttributes().get(j).setMachineRecipeID(lotEffectiveFPCInfoGetOutRetCode.getFpcInfo().getMachineRecipeID());
                    //convert machineRecipe
                    aMachineRecipe = baseCoreFactory.getBO(CimMachineRecipe.class, strWhatNextInqResult.getStrWhatNextAttributes().get(j).getMachineRecipeID());
                    Validations.check(aMachineRecipe == null, retCodeConfig.getNotFoundMachineRecipe());
                }else {
                    //Get Machine Recipe ID
                    String subLotType = aLot.getSubLotType();
                    if (aLogicalRecipe != null){
                        log.info("aLogicalRecipe is not nil");
                        if (searchCondition  == 1){
                            aMachineRecipe = aLogicalRecipe.findMachineRecipeFor(aLot, aMachine);
                        }else {
                            aMachineRecipe = aLogicalRecipe.findMachineRecipeForSubLotType(aMachine, subLotType);
                        }
                    }
                    if (!CimObjectUtils.isEmpty(aMachineRecipe)){
                        strWhatNextInqResult.getStrWhatNextAttributes().get(j).setMachineRecipeID(new ObjectIdentifier(aMachineRecipe.getIdentifier(), aMachineRecipe.getPrimaryKey()));
                    }else {
                        strWhatNextInqResult.getStrWhatNextAttributes().get(j).setMachineRecipeID(null);
                    }
                }
                // Get Physical Recipe ID
                if (!CimObjectUtils.isEmpty(aMachineRecipe)){
                    log.info("aMachineRecipe is not nil");
                    strWhatNextInqResult.getStrWhatNextAttributes().get(j).setPhysicalRecipeID(aMachineRecipe.getPhysicalRecipeId());
                }
            }
            Integer processRunSizeMax = aMachine.getProcessRunSizeMaximum();
            strWhatNextInqResult.setProcessRunSizeMaximum(processRunSizeMax);
            portGroups.add(eqpOnePortGroupInfoList.get(i).getStrPortGroup());
            log.info("call whatNextLotList_to_StartCassetteForDeliveryReq");
            //【step11】 - whatNextLotList_to_StartCassetteForDeliveryReq
            whatNextLotListToStartCarrierTransferReqOut = whatNextMethod.whatNextLotListToStartCassetteForDeliveryReq(objCommonIn,
                    eqpOnePortGroupInfoList.get(i).getEquipmentID(),
                    portGroups,
                    strWhatNextInqResult);

            log.info("FOUND TRANSFER EQP !!");
            log.info("find set unload port");
            //find set unload port
            int lenPortId = CimArrayUtils.getSize(equipmentTargetPortPickupOut.getEqpTargetPortInfo().getPortGroups().get(0).getStrPortID());
            int lenStartCassette = CimArrayUtils.getSize(whatNextLotListToStartCarrierTransferReqOut);
            for (int j = 0; j < lenStartCassette; j++) {
                CimPortResource aPs = baseCoreFactory.getBO(CimPortResource.class, whatNextLotListToStartCarrierTransferReqOut.get(j).getLoadPortID());
                Validations.check(aPs == null, new OmCode(retCodeConfig.getNotFoundPort(), ObjectIdentifier.fetchValue(whatNextLotListToStartCarrierTransferReqOut.get(j).getLoadPortID())));
                CimPortResource aPosAssociatedPort = aPs.getAssociatedPort();
                if (!CimObjectUtils.isEmpty(aPosAssociatedPort)) {
                    whatNextLotListToStartCarrierTransferReqOut.get(j).setUnloadPortID(new ObjectIdentifier(aPosAssociatedPort.getIdentifier(), aPosAssociatedPort.getPrimaryKey()));
                } else {
                    whatNextLotListToStartCarrierTransferReqOut.get(j).setUnloadPortID(new ObjectIdentifier("", ""));
                }
            }
            //set output parameter
            out.setEquipmentID(eqpOnePortGroupInfoList.get(i).getEquipmentID());
            out.setPortGroup(eqpOnePortGroupInfoList.get(i).getStrPortGroup().getPortGroup());
            out.setStartCassetteList(whatNextLotListToStartCarrierTransferReqOut);
            bOk = true;
            break;
        }
        if (CimBooleanUtils.isFalse(bOk)){
            log.info("not found eqp");
            throw new ServiceException(retCodeConfig.getNotFoundEqp(),out);
        }
        return out;
    }

    @Override
    public Outputs.DurableWhereNextTransferEqpOut durableWhereNextTransferEqp(Infos.ObjCommon objCommonIn, ObjectIdentifier equipmentID, Outputs.EquipmentTargetPortPickupOut equipmentTargetPortPickupOut) {
        //init
        Outputs.DurableWhereNextTransferEqpOut out = new Outputs.DurableWhereNextTransferEqpOut();
        out.setStrStartDurables(new ArrayList<>());

        List<Infos.PortGroup> targetPortGroup = equipmentTargetPortPickupOut.getEqpTargetPortInfo().getPortGroups();
        int lenPortGroup = CimArrayUtils.getSize(targetPortGroup);

        //check if the port group list is empty
        Validations.check(0 == lenPortGroup,retCodeConfig.getNotFoundTargetPort() );

        List<Infos.PortID> targetPortID = targetPortGroup.get(0).getStrPortID();
        int lenPortID = CimArrayUtils.getSize(targetPortID);

        //get durableCategory
        List<DispatcherDTO.DispatchDecision> aDispatchDecisionSequence = null;
        int nCandidateDurableCount = 0;
        String durableCategory = null;
        //get machine and dispatch
        CimMachine aMachine = baseCoreFactory.getBO(CimMachine.class, equipmentID);
        Validations.check(null == aMachine,retCodeConfig.getNotFoundMachine());
        CimDispatcher aDispatcher = aMachine.getDispatcher();
        Validations.check(null == aDispatcher,retCodeConfig.getNotFoundEqpDispatcher());

        if(CimStringUtils.isEmpty(durableCategory) && 0 == nCandidateDurableCount ) {
            aDispatchDecisionSequence = aDispatcher.whatNextDurableForMachine(aMachine, BizConstant.SP_DURABLECAT_CASSETTE);
            nCandidateDurableCount = CimArrayUtils.getSize(aDispatchDecisionSequence);
            if(nCandidateDurableCount > 0 && CimStringUtils.isEmpty(durableCategory)) {
                durableCategory = BizConstant.SP_DURABLECAT_CASSETTE;
            }
        }

        if(CimStringUtils.isEmpty(durableCategory) && 0 == nCandidateDurableCount ) {
            aDispatchDecisionSequence = aDispatcher.whatNextDurableForMachine(aMachine, BizConstant.SP_DURABLECAT_RETICLEPOD);
            nCandidateDurableCount = CimArrayUtils.getSize(aDispatchDecisionSequence);
            if(nCandidateDurableCount > 0 && CimStringUtils.isEmpty(durableCategory)) {
                durableCategory = BizConstant.SP_DURABLECAT_RETICLEPOD;
            }
        }

        if(CimStringUtils.isEmpty(durableCategory) && 0 == nCandidateDurableCount) {
            aDispatchDecisionSequence = aDispatcher.whatNextDurableForMachine(aMachine, BizConstant.SP_DURABLECAT_RETICLE);
            nCandidateDurableCount = CimArrayUtils.getSize(aDispatchDecisionSequence);
            if(nCandidateDurableCount > 0 && CimStringUtils.isEmpty(durableCategory)) {
                durableCategory = BizConstant.SP_DURABLECAT_RETICLE;
            }
        }
        //main logical
        for (int i = 0; i < lenPortGroup; i++) {
            Infos.PortID iPortID = targetPortID.get(i);
            if (CimStringUtils.equals(BizConstant.SP_DURABLECAT_CASSETTE,durableCategory)){
                CimCassette cassette = baseCoreFactory.getBO(CimCassette.class, iPortID.getCassetteID());
                Validations.check(null == cassette, new OmCode(retCodeConfig.getNotFoundCassette(), ObjectIdentifier.fetchValue(iPortID.getCassetteID())));
            }else if (CimStringUtils.equals(BizConstant.SP_DURABLECAT_RETICLE,durableCategory)){
                CimProcessDurable durable = baseCoreFactory.getBO(CimProcessDurable.class, iPortID.getCassetteID());
                Validations.check(null == durable,retCodeConfig.getNotFoundDurable());
            }
        }

        if (CimStringUtils.equals(BizConstant.SP_DURABLECAT_CASSETTE,durableCategory)){
            //*************************************************************************************
            // get next EQP list
            //
            //  EQP-1
            // +--------------------+
            // |  P1        P2      |  Cas1-NextEqpList ---> [EQP-2], [EQP-3], [EQP-4]
            // | +------+  +------+ |  Cas2-NextEqpList ---> [EQP-3], [EQP-4], [EQP-5]
            // | | Cas1 |  | Cas2 | |
            // | +------+  +------+ |
            // +--------------------+
            //
            //*************************************************************************************
            lenPortGroup = CimArrayUtils.getSize(equipmentTargetPortPickupOut.getEqpTargetPortInfo().getPortGroups().get(0).getStrPortID());
            List<Results.DurableWhereNextStockerInqResult> strWhereNextStockerInqResults = new ArrayList<>();
            for (int i = 0; i < lenPortGroup; i++) {
                //Get Where Next EQP
                // step1- durableDestinationInfoGet
                log.info("step-1 durableDestinationInfoGet");
                ObjectIdentifier dummyLotId = new ObjectIdentifier();
                Results.DurableWhereNextStockerInqResult durableWhereNextStockerInqResult = durableMethod.durableDestinationInfoGet(objCommonIn, equipmentTargetPortPickupOut.getEqpTargetPortInfo().getPortGroups().get(0).getStrPortID().get(i).getCassetteID());
                strWhereNextStockerInqResults.add(durableWhereNextStockerInqResult);
            }
            //*************************************************************************************
            // AND list EQP
            //
            // Cas1-NextEqpList ---> [EQP-2], [EQP-3], [EQP-4]
            // Cas2-NextEqpList ---> [EQP-3], [EQP-4], [EQP-5]
            //    |
            //    V
            // eqpIDs -------------> [EQP-3], [EQP-4]
            //
            //*************************************************************************************
            log.info(" AND list EQP");
            List<ObjectIdentifier> eqpIDs = new ArrayList<>();
            List<ObjectIdentifier> tmpEqpIDs = new ArrayList<>();
            for (int i = 0; i < lenPortGroup; i++) {
                int eqpIDCnt = 0;
                int lenNextEqp = CimArrayUtils.getSize(strWhereNextStockerInqResults.get(i).getWhereNextEqpStatus());
                for (int j = 0; j < lenNextEqp; j++) {
                    if (i == 0){
                        tmpEqpIDs.add(strWhereNextStockerInqResults.get(i).getWhereNextEqpStatus().get(j).getEquipmentID());
                        eqpIDCnt++;
                    }else {
                        int lenEqpID = CimArrayUtils.getSize(eqpIDs);
                        for (int k = 0; k < lenEqpID; k++) {
                            if (ObjectIdentifier.equalsWithValue(eqpIDs.get(k), strWhereNextStockerInqResults.get(i).getWhereNextEqpStatus().get(j).getEquipmentID())){
                                tmpEqpIDs.add(eqpIDCnt,strWhereNextStockerInqResults.get(i).getWhereNextEqpStatus().get(j).getEquipmentID());
                                eqpIDCnt++;
                                break;
                            }
                        }
                    }
                }
                eqpIDs = tmpEqpIDs;
                tmpEqpIDs = new ArrayList<>();
            }
            //Omit InternalBufferEQP from eqpIDs
            tmpEqpIDs = eqpIDs;
            int nSetCnt = 0;
            int lenEqpIDs = CimArrayUtils.getSize(tmpEqpIDs);
            eqpIDs = new ArrayList<>();
            for (int i = 0; i < lenEqpIDs; i++) {
                CimMachine aPosMachine = baseCoreFactory.getBO(CimMachine.class, tmpEqpIDs.get(i));
                Validations.check(aPosMachine == null, new OmCode(retCodeConfig.getNotFoundEqp(), tmpEqpIDs.get(i).getValue()));
                String strMachineCategory = aPosMachine.getCategory();
                if (!CimStringUtils.equals(BizConstant.SP_MC_CATEGORY_INTERNALBUFFER,strMachineCategory)){
                    eqpIDs.add(nSetCnt,tmpEqpIDs.get(i));
                    nSetCnt++;
                }else {
                    log.info("Omit!! InternalBufferEQP!!");
                }
            }
            //Get Target EQP Unload Port Count
            log.info("Get Target EQP Unload Port Count");
            int tgtEqpUnloadPortCnt = 0;
            lenPortID = CimArrayUtils.getSize(equipmentTargetPortPickupOut.getEqpTargetPortInfo().getPortGroups().get(0).getStrPortID());
            for (int i = 0; i < lenPortID; i++) {
                if (CimStringUtils.equals(BizConstant.SP_PORTRSC_PORTSTATE_UNLOADREQ, equipmentTargetPortPickupOut.getEqpTargetPortInfo().getPortGroups().get(0).getStrPortID().get(i).getPortState())){
                    tgtEqpUnloadPortCnt ++;
                }
            }
            //Check ToEquipment. and Pickup PortGroup Information
            log.info("Check ToEquipment. and Pickup PortGroup Information");
            nSetCnt = 0;
            List<Infos.EqpOnePortGroupInfo> eqpOnePortGroupInfoList = new ArrayList<>();
            tmpEqpIDs = eqpIDs;
            int lenToEqp = CimArrayUtils.getSize(tmpEqpIDs);
            for (int i = 0; i < lenToEqp; i++) {
                // Check To EQP Process Run Max & Min
                log.info("Check To EQP Process Run Max & Min");
                CimMachine aToMachine = baseCoreFactory.getBO(CimMachine.class, tmpEqpIDs.get(i));
                Validations.check(null == aToMachine, new OmCode(retCodeConfig.getNotFoundEqp(), ObjectIdentifier.fetchValue(tmpEqpIDs.get(i))));
                int runSizeMax = aToMachine.getProcessRunSizeMaximum();
                int runSizeMin = aToMachine.getProcessRunSizeMinimum();
                if (tgtEqpUnloadPortCnt > runSizeMax || tgtEqpUnloadPortCnt < runSizeMin){
                    log.info("continue!!  tgtEqpUnloadPortCnt > runSizeMax || tgtEqpUnloadPortCnt < runSizeMin");
                    continue;
                }
                //Get Equipment Information
                log.info("step2 - equipmentBRInfoGetDR");
                //step2 - equipmentBRInfoGetDR
                Infos.EqpBrInfo eqpBrInfo = equipmentMethod.equipmentBRInfoGetDR(objCommonIn, tmpEqpIDs.get(i));
                log.info("step3 - equipmentStatusInfoGetDR");
                //step3 - equipmentStatusInfoGetDR
                Infos.EqpStatusInfo eqpStatusInfo = equipmentMethod.equipmentStatusInfoGetDR(objCommonIn, tmpEqpIDs.get(i));
                log.info("step4 - equipmentPortInfoGet");
                //step4 - equipmentPortInfoGet
                Infos.EqpPortInfo eqpPortInfo = equipmentMethod.equipmentPortInfoGet(objCommonIn, tmpEqpIDs.get(i));
                if (CimBooleanUtils.isFalse(eqpStatusInfo.getEquipmentAvailableFlag())){
                    log.info("TRUE != EqpInfo.equipmentAvailableFlag  ...<continue>");
                    continue;
                }
                //Sorting Port Group Info by Port Group
                //step5 - equipmentPortInfoSortByGroup
                log.info("step5 - equipmentPortInfoSortByGroup");
                Infos.EqpPortInfoOrderByGroup eqpPortInfoOrderByGroupRetCode = equipmentMethod.equipmentPortInfoSortByGroup(objCommonIn,
                        tmpEqpIDs.get(i),
                        eqpPortInfo.getEqpPortStatuses());
                /*------------------------------------------------------------------------------*/
                /*   Pickup Target Port of ToEquipment                                          */
                /*                                                                              */
                /*   equipment_targetPort_Pickup is carried out for every PortGroup.            */
                /*   Because, equipment_targetPort_Pickup returns only PortGroup found first.   */
                /*------------------------------------------------------------------------------*/
                log.info("step6 - durableEquipmentTargetPortPickup");
                int lenPortGroupNew = CimArrayUtils.getSize(eqpPortInfoOrderByGroupRetCode.getStrPortGroup());
                for (int j = 0; j < lenPortGroupNew; j++) {
                    Infos.EqpPortInfoOrderByGroup strTmpPortSortByGroup = new Infos.EqpPortInfoOrderByGroup();
                    strTmpPortSortByGroup.setEquipmentID(tmpEqpIDs.get(i));
                    Infos.PortGroup portGroup = eqpPortInfoOrderByGroupRetCode.getStrPortGroup().get(j);
                    List<Infos.PortGroup> portGroups = new ArrayList<>();
                    portGroups.add(portGroup);
                    strTmpPortSortByGroup.setStrPortGroup(portGroups);
                    //step6 - durableEquipmentTargetPortPickup
                    Outputs.EquipmentTargetPortPickupOut object = new Outputs.EquipmentTargetPortPickupOut();
                    Infos.EqpBrInfo strEqpBrInfo = eqpBrInfo;
                    object.setWhatsNextRequireFlag(false);
                    try {
                        object = equipmentForDurableMethod.durableEquipmentTargetPortPickup(objCommonIn, strTmpPortSortByGroup, strEqpBrInfo, eqpPortInfo);
                    } catch (ServiceException e) {
                        log.info("durableEquipmentTargetPortPickup is not OK  ...<continue>");
                        continue;
                    }
                    if (!CimStringUtils.equals(BizConstant.SP_PORTRSC_PORTSTATE_LOADREQ, object.getTargetPortType())){
                        log.info("TargetPortType is not [LoadReq]  ...<continue>");
                        continue;
                    }
                    int lenTargetPortGroup = CimArrayUtils.getSize(strTmpPortSortByGroup.getStrPortGroup());
                    if (lenTargetPortGroup == 0){
                        log.info("TargetPortGroup lentgh is 0  ...<continue>");
                        continue;
                    }
                    Infos.EqpOnePortGroupInfo eqpOnePortGroupInfo = new Infos.EqpOnePortGroupInfo();
                    eqpOnePortGroupInfo.setEquipmentID(tmpEqpIDs.get(i));
                    eqpOnePortGroupInfo.setStrPortGroup(strTmpPortSortByGroup.getStrPortGroup().get(0));
                    eqpOnePortGroupInfoList.add(nSetCnt,eqpOnePortGroupInfo);
                    nSetCnt ++;
                }
            }
            log.info("Pickup PortGroup Count,{}",nSetCnt);
            //*************************************************************************************
            // sort old order of portGroup timeStamp
            //
            // EQP-3 --- PG-A
            //       P1 : dispatchState_TimeStamp: 2000/05/03 18:23:08  <--- This!
            //       P2 : dispatchState_TimeStamp: 2000/05/01 12:14:28
            //
            // EQP-3 --- PG-B
            //       P1 : dispatchState_TimeStamp: 2000/05/02 11:47:13  <--- This!
            //       P2 : dispatchState_TimeStamp: 2000/05/01 02:54:31
            //
            // EQP-4 --- PG-A
            //       P1 : dispatchState_TimeStamp: 2000/05/02 20:21:49
            //       P2 : dispatchState_TimeStamp: 2000/05/03 14:58:26  <--- This!
            //
            // pptEqpOnePortGroupInfoSeq ---> [EQP3 - PG-A], [EQP3 - PG-B], [EQP4 - PG-A]
            //          |
            //          V
            // pptEqpOnePortGroupInfoSeq ---> [EQP3 - PG-B], [EQP4 - PG-A], [EQP3 - PG-A]
            //
            //*************************************************************************************
            log.info("pickup most last timeStamp PortGroup");
            Infos.EqpOnePortGroupInfo tmpEqpPortGroupInfoSeq = new Infos.EqpOnePortGroupInfo();
            String tmpTime = null;
            List<Integer> portNumSeq = new ArrayList<>();
            Integer lenEqpPortGroupInfoSeq = 0;
            lenEqpPortGroupInfoSeq = CimArrayUtils.getSize(eqpOnePortGroupInfoList);
            for (int i = 0; i < lenEqpPortGroupInfoSeq; i++) {
                //Get High Priority Port
                //KeyPort
                portNumSeq.add(0);
                lenPortID = CimArrayUtils.getSize(eqpOnePortGroupInfoList.get(i).getStrPortGroup().getStrPortID());
                tmpTime = eqpOnePortGroupInfoList.get(i).getStrPortGroup().getStrPortID().get(0).getDispatchStateTimeStamp();
                for (int j = 0; j < lenPortID; j++) {
                    if ( CimDateUtils.convertToOrInitialTime(eqpOnePortGroupInfoList.get(i).getStrPortGroup().getStrPortID().get(j).getDispatchStateTimeStamp()).compareTo( CimDateUtils.convertToOrInitialTime(tmpTime)) > 0){
                        tmpTime = eqpOnePortGroupInfoList.get(i).getStrPortGroup().getStrPortID().get(j).getDispatchStateTimeStamp();
                        portNumSeq.set(i, j);
                    }
                }
            }
            for (int i = 0; i < lenEqpPortGroupInfoSeq; i++) {
                //sort
                for (int j = 0; j < i; j++) {
                    if (CimDateUtils.convertToOrInitialTime(eqpOnePortGroupInfoList.get(i).getStrPortGroup().getStrPortID().get(portNumSeq.get(i)).getDispatchStateTimeStamp()).compareTo(
                            CimDateUtils.convertToOrInitialTime(eqpOnePortGroupInfoList.get(j).getStrPortGroup().getStrPortID().get(portNumSeq.get(j)).getDispatchStateTimeStamp())) < 0){
                        tmpEqpPortGroupInfoSeq = eqpOnePortGroupInfoList.get(j);
                        eqpOnePortGroupInfoList.set(j, eqpOnePortGroupInfoList.get(i));
                        eqpOnePortGroupInfoList.set(i, tmpEqpPortGroupInfoSeq);
                    }
                }
            }
            //run check
            log.info("run check");
            Results.WhatNextDurableListInqResult strDurableWhatNextInqResult = new Results.WhatNextDurableListInqResult();
            int lenPortIDs = CimArrayUtils.getSize(equipmentTargetPortPickupOut.getEqpTargetPortInfo().getPortGroups().get(0).getStrPortID());
            List<Infos.WhatNextDurableAttributes> durableWhatNextAttributes = new ArrayList<>();
            for (int i = 0; i < lenPortIDs; i++) {
                ObjectIdentifier cassetteID = equipmentTargetPortPickupOut.getEqpTargetPortInfo().getPortGroups().get(0).getStrPortID().get(i).getCassetteID();
                CimCassette aCassette = baseCoreFactory.getBO(CimCassette.class, cassetteID);
                Validations.check(aCassette == null, new OmCode(retCodeConfig.getNotFoundCassette(), ObjectIdentifier.fetchValue(cassetteID)));
                //step7 - cassetteDBInfoGetDR
                log.info("step7 - cassetteDBInfoGetDR");
                Infos.CassetteDBINfoGetDRInfo cassetteDBINfoGetDRInfo = new Infos.CassetteDBINfoGetDRInfo();
                cassetteDBINfoGetDRInfo.setCassetteID(cassetteID);
                cassetteDBINfoGetDRInfo.setDurableOperationInfoFlag(true);
                cassetteDBINfoGetDRInfo.setDurableWipOperationInfoFlag(true);
                Outputs.CassetteDBInfoGetDROut cassetteDBInfoGetDROut = cassetteMethod.cassetteDBInfoGetDR(objCommonIn, cassetteDBINfoGetDRInfo);

                strDurableWhatNextInqResult.setStrWhatNextDurableAttributes(durableWhatNextAttributes);
                Infos.WhatNextDurableAttributes whatNextDurableAttributes = new Infos.WhatNextDurableAttributes();
                durableWhatNextAttributes.add(whatNextDurableAttributes);
                whatNextDurableAttributes.setEquipmentID(equipmentID);
                whatNextDurableAttributes.setDurableCategory(durableCategory);
                whatNextDurableAttributes.setDurableID(cassetteID);
                whatNextDurableAttributes.setLogicalRecipeID(cassetteDBInfoGetDROut.getCarrierDetailInfoInqResult().getStrDurableOperationInfo().getLogicalRecipeID());
                whatNextDurableAttributes.setTransferStatus(cassetteDBInfoGetDROut.getCarrierDetailInfoInqResult().getCassetteStatusInfo().getTransferStatus());
            }
            //End Make pptWhatNextInqResult Struct Info
            List<Infos.StartDurable> whatNextDurableListToStartDurableTransferReqOut = null;
            List<Infos.PortGroup> portGroups = new ArrayList<>();
            int lenEqpPortGroupInfo = CimArrayUtils.getSize(eqpOnePortGroupInfoList);
            Boolean bOk = false;
            for (int i = 0; i < lenEqpPortGroupInfo; i++) {
                CimMachine machine = baseCoreFactory.getBO(CimMachine.class, eqpOnePortGroupInfoList.get(i).getEquipmentID());
                Validations.check(null == machine, new OmCode(retCodeConfig.getNotFoundEqp(), ObjectIdentifier.fetchValue(eqpOnePortGroupInfoList.get(i).getEquipmentID())));
                int lenWipDurables = CimArrayUtils.getSize(strDurableWhatNextInqResult.getStrWhatNextDurableAttributes());
                for (int j = 0; j < lenWipDurables; j++) {
                    // reset information
                    CimLogicalRecipe aLogicalRecipe = baseCoreFactory.getBO(CimLogicalRecipe.class, strDurableWhatNextInqResult.getStrWhatNextDurableAttributes().get(j).getLogicalRecipeID());
                    Validations.check(aLogicalRecipe == null, retCodeConfig.getNotFoundLogicalRecipe());

                    //get MachineRecipe object and set objectIdentifier
                    CimMachineRecipe aMachineRecipe = aLogicalRecipe.findMachineRecipeForSubLotType(machine, null);
                    Validations.check(null == aMachineRecipe, retCodeConfig.getNotFoundMachineRecipe());
                    strDurableWhatNextInqResult.getStrWhatNextDurableAttributes().get(j).setMachineRecipeID(ObjectIdentifier.build(aMachineRecipe.getIdentifier(), aMachineRecipe.getPrimaryKey()));

                    // Get Physical Recipe ID
                    if (!CimObjectUtils.isEmpty(aMachineRecipe)){
                        log.info("aMachineRecipe is not nil");
                        strDurableWhatNextInqResult.getStrWhatNextDurableAttributes().get(j).setPhysicalRecipeID(aMachineRecipe.getPhysicalRecipeId());
                    }
                }
                Integer processRunSizeMax = machine.getProcessRunSizeMaximum();
                strDurableWhatNextInqResult.setProcessRunSizeMaximum(CimNumberUtils.longValue(processRunSizeMax));
                portGroups.add(eqpOnePortGroupInfoList.get(i).getStrPortGroup());
                log.info("step8 - whatNextDurableListToStartDurableForDeliveryReq");
                //step8 - whatNextDurableListToStartDurableForDeliveryReq
                whatNextDurableListToStartDurableTransferReqOut = whatNextMethod.whatNextDurableListToStartDurableForDeliveryReq(objCommonIn,
                        eqpOnePortGroupInfoList.get(i).getEquipmentID(),
                        portGroups,
                        strDurableWhatNextInqResult);

                log.info("FOUND TRANSFER EQP !!");
                log.info("find set unload port");
                //find set unload port
                int lenStartCassette = CimArrayUtils.getSize(whatNextDurableListToStartDurableTransferReqOut);
                for (int j = 0; j < lenStartCassette; j++) {
                    CimPortResource aPs = baseCoreFactory.getBO(CimPortResource.class, whatNextDurableListToStartDurableTransferReqOut.get(j).getStartDurablePort().getLoadPortID());
                    Validations.check(aPs == null, new OmCode(retCodeConfig.getNotFoundPort(), ObjectIdentifier.fetchValue(whatNextDurableListToStartDurableTransferReqOut.get(j).getStartDurablePort().getLoadPortID())));
                    CimPortResource aPosAssociatedPort = aPs.getAssociatedPort();
                    if (!CimObjectUtils.isEmpty(aPosAssociatedPort)) {
                        whatNextDurableListToStartDurableTransferReqOut.get(j).getStartDurablePort().setUnloadPortID(new ObjectIdentifier(aPosAssociatedPort.getIdentifier(), aPosAssociatedPort.getPrimaryKey()));
                    } else {
                        whatNextDurableListToStartDurableTransferReqOut.get(j).getStartDurablePort().setUnloadPortID(new ObjectIdentifier("", ""));
                    }
                }
                //set output parameter
                out.setEquipmentID(eqpOnePortGroupInfoList.get(i).getEquipmentID());
                out.setPortGroup(eqpOnePortGroupInfoList.get(i).getStrPortGroup().getPortGroup());
                out.setStrStartDurables(whatNextDurableListToStartDurableTransferReqOut);
                out.setDurableCategory(durableCategory);
                bOk = true;
                break;
            }
            if (CimBooleanUtils.isFalse(bOk)){
                log.info("not found eqp");
                throw new ServiceException(retCodeConfig.getNotFoundEqp(),out);
            }
            return out;
        }
        // TODO: 2020/9/22 Durable/Reticle
        return null;
    }
}
