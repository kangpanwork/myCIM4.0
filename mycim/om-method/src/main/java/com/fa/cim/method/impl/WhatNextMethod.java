package com.fa.cim.method.impl;

import com.fa.cim.annotaion.OmMethod;
import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.constant.CIMStateConst;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.OmCode;
import com.fa.cim.common.utils.*;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.config.RetCodeConfigEx;
import com.fa.cim.dto.*;
import com.fa.cim.jpa.SearchCondition;
import com.fa.cim.method.*;
import com.fa.cim.newcore.bo.durable.CimCassette;
import com.fa.cim.newcore.bo.machine.CimMachine;
import com.fa.cim.newcore.bo.pd.CimProcessFlowContext;
import com.fa.cim.newcore.bo.pd.CimProcessOperation;
import com.fa.cim.newcore.bo.prodspec.CimProductSpecification;
import com.fa.cim.newcore.bo.product.CimLot;
import com.fa.cim.newcore.bo.recipe.CimLogicalRecipe;
import com.fa.cim.newcore.bo.recipe.CimMachineRecipe;
import com.fa.cim.newcore.dto.recipe.RecipeDTO;
import com.fa.cim.newcore.factory.BaseCoreFactory;
import com.fa.cim.newcore.impl.bo.env.StandardProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@OmMethod
@Slf4j
public class WhatNextMethod implements IWhatNextMethod {

    @Autowired
    private RetCodeConfig retCodeConfig;

    @Autowired
    private RetCodeConfigEx retCodeConfigEx;

    @Autowired
    private BaseCoreFactory baseCoreFactory;

    @Autowired
    private IEquipmentMethod equipmentMethod;

    @Autowired
    private ILotMethod lotMethod;

    @Autowired
    private ICassetteMethod cassetteMethod;

    @Autowired
    private IProcessMethod processMethod;

    @Autowired
    private IFlowBatchMethod flowBatchMethod;

    @Autowired
    private IFPCMethod fpcMethod;

    @Autowired
    private IDurableMethod durableMethod;

    @Autowired
    private IProcessForDurableMethod processForDurableMethod;

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/7/5                               Neyo                create file
     *
     * @author Neyo
     * @since 2019/7/5 15:03
     * Copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Override
    public List<Infos.StartCassette> whatNextLotListToStartCassetteForDeliveryReq(Infos.ObjCommon objCommonIn, ObjectIdentifier equipmentID, List<Infos.PortGroup> strPortGroupSeq, Results.WhatNextLotListResult strWhatNextInqResult) {
        //Initialize
        String portGroupID = null;
        String multiLotType = null;
        int searchCondition = 0;
        String searchCondition_var = StandardProperties.OM_CONSTRAINT_CHK_WITH_SEARCH.getValue();
        if (CimStringUtils.length(searchCondition_var) > 0){
            searchCondition = Integer.parseInt(searchCondition_var);
        }
        //Check Condition and Get Information
        //Get Equipment Object
        CimMachine aMachine = baseCoreFactory.getBO(CimMachine.class, equipmentID);
        Validations.check(null == aMachine,retCodeConfig.getNotFoundEqp(), ObjectIdentifier.fetchValue(equipmentID));

        //Get Equipment's MultiRecipeCapability
        String multiRecipeCapability = aMachine.getMultipleRecipeCapability();

        //Get PortID List
        log.info("Get PortID List");
        List<ObjectIdentifier> portID = new ArrayList<>();
        List<Long> loadSequenceNumber = new ArrayList<>();
        List<String> loadPurposeType = new ArrayList<>();
        int portCount = 0;
        int nPortGroupLen = CimArrayUtils.getSize(strPortGroupSeq);
        for (int i = 0; i < nPortGroupLen; i++) {
            int nPortLen = CimArrayUtils.getSize(strPortGroupSeq.get(i).getStrPortID());
            for (int j = 0; j < nPortLen; j++) {
                if (CimStringUtils.equals(BizConstant.SP_LOADPURPOSETYPE_EMPTYCASSETTE,strPortGroupSeq.get(i).getStrPortID().get(j).getLoadPurposeType())){
                    log.info("port.loadPurposeType == EmptyCassette   ...<<continue>>");
                    continue;
                }
                portID.add(portCount,strPortGroupSeq.get(i).getStrPortID().get(j).getPortID());
                loadSequenceNumber.add(portCount,strPortGroupSeq.get(i).getStrPortID().get(j).getLoadSequenceNoInPortGroup());
                loadPurposeType.add(portCount,strPortGroupSeq.get(i).getStrPortID().get(j).getLoadPurposeType());
                portCount++;
            }
        }
        // Check PortCount and processRunSizeMaximum
        log.info("Check PortCount and processRunSizeMaximum");
        int processRunCount = 0;
        int processRunSizeMaximum = strWhatNextInqResult.getProcessRunSizeMaximum();
        Validations.check (portCount == 0 || portCount < processRunSizeMaximum, retCodeConfigEx.getNotFoundFilledCast());
        //Get Equipment's Process Batch Condition
        log.info("equipment_processBatchCondition_Get()");
        //【step1】 - equipment_processBatchCondition_Get
        Outputs.ObjEquipmentProcessBatchConditionGetOut equipmentProcessBatchConditionRetCode = equipmentMethod.equipmentProcessBatchConditionGet(objCommonIn, equipmentID);
        //Check WIP Lot Count
        log.info("Check WIP Lot Count");
        int attrLen = CimArrayUtils.getSize(strWhatNextInqResult.getStrWhatNextAttributes());
        Validations.check (attrLen == 0, retCodeConfigEx.getNotFoundFilledCast());

        //Get Empty Cassette
        log.info("Get Empty Cassette");
        Outputs.ObjCassetteListEmptyAvailablePickUpOut cassetteListEmptyAvailablePickupOutRetCode = null;
        if (CimBooleanUtils.isTrue(equipmentProcessBatchConditionRetCode.isCassetteExchangeFlag()
                || CimBooleanUtils.isTrue(equipmentProcessBatchConditionRetCode.isMonitorCreationFlag()))){
            log.info("EmptyCassette is necessary");
            //Get Empty Cassette
            Inputs.ObjCassetteListGetDRIn listGetDRIn = new Inputs.ObjCassetteListGetDRIn(new Params.CarrierListInqParams());
            listGetDRIn.setEmptyFlag(true);
            listGetDRIn.setCassetteStatus(CIMStateConst.CIM_DURABLE_AVAILABLE);
            listGetDRIn.setMaxRetrieveCount(-1);
            listGetDRIn.setSorterJobCreationCheckFlag(false);
            ObjectIdentifier dummy = ObjectIdentifier.buildWithValue("");
            listGetDRIn.setBankID(dummy);
            listGetDRIn.setDurablesSubStatus(dummy);
            listGetDRIn.setFlowStatus("");
            //【note】: add searchCondition default by neyo
            listGetDRIn.setSearchCondition(new SearchCondition());
            //【step2】 - cassette_ListGetDR__170
            Page<Infos.FoundCassette> carrierListInq170ResultRetCode = cassetteMethod.cassetteListGetDR170(objCommonIn, listGetDRIn);
            List<Infos.FoundCassette> strFoundCassette = carrierListInq170ResultRetCode.getContent();
            //Pick Up Target Empty Cassette
            log.info("cassetteList_emptyAvailable_Pickup()");
            //【step3】 - cassetteList_emptyAvailable_Pickup
            cassetteListEmptyAvailablePickupOutRetCode = cassetteMethod.cassetteListEmptyAvailablePickup(objCommonIn,strFoundCassette);
        }else {
            log.info("EmptyCassette is unnecessary");
        }

        //Get and Check FlowBatch Control
        log.info("Get and Check FlowBatch Info");
        //【step4】 - flowBatch_CheckConditionForCassetteDelivery
        Outputs.ObjFlowBatchCheckConditionForCassetteDeliveryOut flowBatchCheckConditionForCassetteDeliveryRetCode = flowBatchMethod.flowBatchCheckConditionForCassetteDelivery(objCommonIn,equipmentID,strWhatNextInqResult);

        //Make strStartCassette Process
        log.info("Make strStartCassette Process");
        List<Infos.StartCassette> strStartCassette = new ArrayList<>();
        boolean bCheckMinBatchSize = false;
        String checkMinBatchSize = StandardProperties.OM_DELIVERY_CHK_MINIMUM_BATCH_SIZE_ENABLE.getValue();
        if (CimStringUtils.equals(checkMinBatchSize,"1")
                && equipmentProcessBatchConditionRetCode.getMinBatchSize() > 1){
            log.info("bCheckMinBatchSize is 1");
            bCheckMinBatchSize = true;
        }
        int nSetStartCassetteCnt = 0;
        List<ObjectIdentifier> omitCassetteSeq = new ArrayList<>();
        while(true){
            //Reset variable
            processRunCount = 0;
            int nPortLen;
            int startNo = 0;
            boolean bWhileExitFlag = false;
            ObjectIdentifier baseLogicalRecipeID = new ObjectIdentifier();
            ObjectIdentifier baseMachineRecipeID = new ObjectIdentifier();
            ObjectIdentifier tmpBaseLogicalRecipeID = new ObjectIdentifier();
            ObjectIdentifier tmpBaseMachineRecipeID = new ObjectIdentifier();

            Boolean bAlreadyCheckMonitorCreationFlag = false;
            nSetStartCassetteCnt = 0;
            int nAssignEmptyCassetteCnt = 0;
            List<ObjectIdentifier> useEmptyCassetteIDSeq = new ArrayList<>();
            List<ObjectIdentifier> useAssignEmptyCassettePortSeq = new ArrayList<>();

            int nWIPLotLoopEndCnt= 0;
            int nSaveProcessRunCount = -1;
            while (processRunCount < processRunSizeMaximum){
                log.info("while ( processRunCount < processRunSizeMaximum ) ");
                if (CimBooleanUtils.isTrue(bWhileExitFlag)){
                    log.info("bWhileExitFlag == TRUE    break!!");
                    break;
                }
                Boolean bTmpBaseRecipeFlag = false;
                List<Infos.StartCassette> tmpStartCassette = new ArrayList<>();

                //Set Cassette Info
                log.info("Set Cassette Info");
                if (nSaveProcessRunCount != processRunCount){
                    log.info("Change processRunCount!!  WIPLot start index <--- 0");
                    startNo = 0;
                    nWIPLotLoopEndCnt = 0;
                }
                nSaveProcessRunCount = processRunCount;
                int i = startNo;
                for (i = startNo; i < attrLen; i++) {
                    // Omit CassetteID is NULL
                    if (ObjectIdentifier.isEmptyWithValue(strWhatNextInqResult.getStrWhatNextAttributes().get(i).getCassetteID())){
                        log.info("CassetteID is NULL   ..<<continue>>");
                        continue;
                    }
                    Boolean bOmitCassette = false;
                    int lenOmitCst = CimArrayUtils.getSize(omitCassetteSeq);
                    for (int m = 0; m < lenOmitCst; m++) {
                        if (ObjectIdentifier.equalsWithValue(omitCassetteSeq.get(m),
                                strWhatNextInqResult.getStrWhatNextAttributes().get(i).getCassetteID())){
                            bOmitCassette = true;
                            break;
                        }
                    }
                    if (CimBooleanUtils.isTrue(bOmitCassette)){
                        log.info("Omit CassetteID   ..<<continue>>");
                        continue;
                    }

                    //Omit already saved strStartCassette
                    Boolean bFoundFlag = false;
                    int lenStartCassette = CimArrayUtils.getSize(strStartCassette);
                    for (int j = 0; j < lenStartCassette; j++) {
                        if (ObjectIdentifier.equalsWithValue(strWhatNextInqResult.getStrWhatNextAttributes().get(i).getCassetteID(),
                                strStartCassette.get(j).getCassetteID())){
                            bFoundFlag = true;
                            break;
                        }
                    }
                    if (CimBooleanUtils.isTrue(bFoundFlag)){
                        log.info("CassetteID is Exist   ..<<continue>>");
                        continue;
                    }

                    //Omit Lot which is not FlowBatchingLots in the case of FlowBatch
                    if (!ObjectIdentifier.isEmptyWithValue(flowBatchCheckConditionForCassetteDeliveryRetCode.getFlowBatchID())){
                        log.info("Omit Lot which is not FlowBatchingLots in the case of FlowBatch");
                        Boolean bFound = false;
                        int lenFlowBatchLots = CimArrayUtils.getSize(flowBatchCheckConditionForCassetteDeliveryRetCode.getStrContainedLotsInFlowBatch());
                        for (int j = 0; j < lenFlowBatchLots; j++) {
                            if (ObjectIdentifier.equalsWithValue(strWhatNextInqResult.getStrWhatNextAttributes().get(i).getLotID(),
                                    flowBatchCheckConditionForCassetteDeliveryRetCode.getStrContainedLotsInFlowBatch().get(j).getLotID())){
                                bFound = true;
                                break;
                            }
                        }
                        if (CimBooleanUtils.isFalse(bFound)){
                            log.info("LotID is not FlowBatchingLots   ..<<continue>>");
                            continue;
                        }
                    }
                    //Omit lot if "Monitor" label is put on lot's process and the lot doesn't have EqpMonitor job
                    if (CimStringUtils.equals(StandardProperties.OM_AUTOMON_FLAG.getValue(),"1")){
                        log.info("OM_AUTOMON_FLAG is 1");
                        //check Lot type
                        //【step5】 -lot_lotType_Get
                        String lotType = lotMethod.lotTypeGet(objCommonIn, strWhatNextInqResult.getStrWhatNextAttributes().get(i).getLotID());
                        if (CimStringUtils.equals(BizConstant.SP_LOT_TYPE_EQUIPMENTMONITORLOT, lotType)
                                || CimStringUtils.equals(BizConstant.SP_LOT_TYPE_DUMMYLOT, lotType)){
                            log.info("lotType is Equipment Monitor or Dummy.");
                            //【step6】 - lot_eqpMonitorOperationLabel_Get
                            List<Infos.EqpMonitorLabelInfo> equipmentMonitorOperationLabelGetOut = lotMethod.lotEqpMonitorOperationLabelGet(objCommonIn, strWhatNextInqResult.getStrWhatNextAttributes().get(i).getLotID());
                            boolean bMonitorLabel = false;
                            int size = CimArrayUtils.getSize(equipmentMonitorOperationLabelGetOut);
                            for (int x = 0; x < size; x++) {
                                log.info("Loop through strEqpMonitorLabelInfoSeq");
                                if (CimStringUtils.equals(BizConstant.SP_EQPMONITOR_OPELABEL_MONITOR,equipmentMonitorOperationLabelGetOut.get(x).getOperationLabel())){
                                    log.info("Found Monitor label");
                                    bMonitorLabel = true;
                                    break;
                                }
                            }
                            if (CimBooleanUtils.isTrue(bMonitorLabel)){
                                log.info("bMonitorLabel is TRUE.");
                                //【step7】 - lot_eqpMonitorJob_Get
                                Infos.EqpMonitorJobLotInfo eqpMonitorJobLotInfoRetCode = lotMethod.lotEqpMonitorJobGet(objCommonIn, strWhatNextInqResult.getStrWhatNextAttributes().get(i).getLotID());

                                if (ObjectIdentifier.isEmptyWithValue(eqpMonitorJobLotInfoRetCode.getEqpMonitorJobID())){
                                    log.info("eqpMonitorJobID is not attached to lot");
                                    continue;
                                }
                                CimLot aPosLot = baseCoreFactory.getBO(CimLot.class, strWhatNextInqResult.getStrWhatNextAttributes().get(i).getLotID());
                                Validations.check(aPosLot == null, new OmCode(retCodeConfig.getNotFoundLot(), ObjectIdentifier.fetchValue(strWhatNextInqResult.getStrWhatNextAttributes().get(i).getLotID())));
                                CimProcessOperation aPosPO = aPosLot.getProcessOperation();
                                Validations.check (CimObjectUtils.isEmpty(aPosPO), new OmCode(retCodeConfig.getNotFoundProcessOperation(), ObjectIdentifier.fetchValue(strWhatNextInqResult.getStrWhatNextAttributes().get(i).getLotID())));
                                String strOPNumber = aPosPO.getOperationNumber();
                                CimProcessFlowContext aPFX = aPosLot.getProcessFlowContext();
                                Validations.check (CimObjectUtils.isEmpty(aPFX), retCodeConfig.getNotFoundPfx());
                                String strEqpMonitorKey = aPFX.getEqpMonOperationKey(strOPNumber);
                                if (!CimStringUtils.equals(strEqpMonitorKey, eqpMonitorJobLotInfoRetCode.getMonitorOpeKey())){
                                    //The EqpMonitor job is for another Monitor process
                                    log.info("EqpMonitorKey is not same.");
                                    continue;
                                }else {
                                    //check OK
                                    log.info("EqpMonitorKey is same.");
                                }
                            }
                        }
                    }
                    // Search Port for ProcessLot
                    log.info("Search Port for ProcessLot");
                    int nAssignPortIdx = -1;
                    int lenAssignPort = CimArrayUtils.getSize(useAssignEmptyCassettePortSeq);
                    int lenPort = CimArrayUtils.getSize(portID);
                    for (int j = 0; j < lenPort; j++) {
                        boolean bFoundPort = false;
                        for (int k = 0; k < lenAssignPort; k++) {
                            if (ObjectIdentifier.isEmptyWithValue(useAssignEmptyCassettePortSeq.get(k))){
                                continue;
                            }
                            if (ObjectIdentifier.equalsWithValue(useAssignEmptyCassettePortSeq.get(k), portID.get(j))){
                                bFoundPort = true;
                                break;
                            }
                        }
                        if (CimBooleanUtils.isFalse(bFoundPort)){
                            nAssignPortIdx = j;
                            break;
                        }
                    }
                    if (nAssignPortIdx < 0){
                        log.info("0 > nAssignPortIdx");
                        break;
                    }
                    //Check Category for Copper/Non Copper
                    log.info("Check Category for Copper/Non Copper");
                    //【step8】 - lot_CassetteCategory_CheckForContaminationControl
                    try {
                        lotMethod.lotCassetteCategoryCheckForContaminationControl(objCommonIn,
                                strWhatNextInqResult.getStrWhatNextAttributes().get(i).getLotID(),
                                strWhatNextInqResult.getStrWhatNextAttributes().get(i).getCassetteID(),
                                equipmentID,
                                portID.get(nAssignPortIdx));
                    } catch (ServiceException e) {
                        log.error("UnMatch CarrierCategory (Copper/NonCopper)   ..<<continue>>");
                        continue;
                    }
                    //Check Stocker which Lot belongs to, Available?
                    ObjectIdentifier checkID = new ObjectIdentifier();
                    if (CimStringUtils.equals(BizConstant.SP_TRANSSTATE_EQUIPMENTIN, strWhatNextInqResult.getStrWhatNextAttributes().get(i).getTransferStatus())
                            && !ObjectIdentifier.equalsWithValue(strWhatNextInqResult.getStrWhatNextAttributes().get(i).getEquipmentID(), equipmentID)){
                        checkID = equipmentID;
                        log.info("Delivery Process is [EQP to EQP]");
                    }else {
                        checkID = strWhatNextInqResult.getStrWhatNextAttributes().get(i).getStockerID();
                        log.info("Delivery Process is [EQP to Stocker]");
                    }
                    if (CimStringUtils.equals(BizConstant.SP_TRANSSTATE_EQUIPMENTOUT,strWhatNextInqResult.getStrWhatNextAttributes().get(i).getTransferStatus())
                            || CimStringUtils.equals(BizConstant.SP_TRANSSTATE_STATIONOUT,strWhatNextInqResult.getStrWhatNextAttributes().get(i).getTransferStatus())){
                        log.info("Cassette is EO or SO. No need to check machine availability.");
                    }else {
                        log.info("Call equipment_CheckAvail()   check EqpID or StockerID --->{}", ObjectIdentifier.fetchValue(checkID));
                        //【step9】 - equipment_CheckAvail
                        try {
                            equipmentMethod.equipmentCheckAvail(objCommonIn, checkID);
                        } catch (ServiceException e){
                            log.error("RC_OK != equipment_CheckAvail()   ..<<continue>>");
                            continue;
                        }
                    }
                    // Check Scrap Wafer
                    List<ObjectIdentifier> cassetteIDs = new ArrayList<>();
                    cassetteIDs.add(strWhatNextInqResult.getStrWhatNextAttributes().get(i).getCassetteID());
                    log.info("call cassette_scrapWafer_SelectDR()");
                    //【step10】 - cassette_scrapWafer_SelectDR
                    List<Infos.LotWaferMap> lotWaferMaps = cassetteMethod.cassetteScrapWaferSelectDR(objCommonIn, cassetteIDs);
                    int nScrapCnt = CimArrayUtils.getSize(lotWaferMaps);
                    if (nScrapCnt > 0){
                        log.info("Cassette has ScrapWafer ..<<continue>>");
                        continue;
                    }
                    //【step11】 - cassette_DBInfo_GetDR__170
                    Infos.CassetteDBINfoGetDRInfo cassetteDBINfoGetDRInfoParam = new Infos.CassetteDBINfoGetDRInfo();
                    cassetteDBINfoGetDRInfoParam.setCassetteID(strWhatNextInqResult.getStrWhatNextAttributes().get(i).getCassetteID());
                    cassetteDBINfoGetDRInfoParam.setDurableOperationInfoFlag(false);
                    cassetteDBINfoGetDRInfoParam.setDurableWipOperationInfoFlag(false);
                    Outputs.CassetteDBInfoGetDROut cassetteDBInfoGetDROutRetCode = cassetteMethod.cassetteDBInfoGetDR(objCommonIn, cassetteDBINfoGetDRInfoParam);
                    int lotLen = 0;
                    int lotCnt = 0;
                    Boolean bCastDispatchDisableFlag = false;
                    lotLen = CimArrayUtils.getSize(cassetteDBInfoGetDROutRetCode.getCarrierDetailInfoInqResult().getCassetteStatusInfo().getStrContainedLotInfo());
                    for (lotCnt = 0;  lotCnt < lotLen; lotCnt++) {
                        if (CimBooleanUtils.isTrue(cassetteDBInfoGetDROutRetCode.getCarrierDetailInfoInqResult().getCassetteStatusInfo().getStrContainedLotInfo().get(lotCnt).isAutoDispatchDisableFlag())){
                            bCastDispatchDisableFlag = true;
                            break;
                        }
                    }
                    if (CimBooleanUtils.isTrue(bCastDispatchDisableFlag)){
                        log.info("Cassette Auto Dispatch Disable Flag == TRUE ..<<continue>>");
                        continue;
                    }
                    Infos.StartCassette startCassette = new Infos.StartCassette();
                    tmpStartCassette.add(startCassette);
                    startCassette.setCassetteID(strWhatNextInqResult.getStrWhatNextAttributes().get(i).getCassetteID());
                    //Set Temporary Base Recipe
                    log.info("Set Temporary Base Recipe");
                    if (processRunCount == 0){
                        log.info("processRunCount == 0");
                        if (CimBooleanUtils.isFalse(bTmpBaseRecipeFlag)){
                            //|Temporary Base Recipe
                            log.info("bTmpBaseRecipeFlag == FALSE");
                            tmpBaseLogicalRecipeID = strWhatNextInqResult.getStrWhatNextAttributes().get(i).getLogicalRecipeID();
                            tmpBaseMachineRecipeID = strWhatNextInqResult.getStrWhatNextAttributes().get(i).getMachineRecipeID();
                            bTmpBaseRecipeFlag = true;
                        }
                    }
                    //Set Port Info
                    log.info("Set Pot Info");
                    startCassette.setLoadSequenceNumber(loadSequenceNumber.get(nAssignPortIdx));
                    if (CimStringUtils.equals(BizConstant.SP_LOADPURPOSETYPE_PROCESSMONITORLOT,loadPurposeType.get(nAssignPortIdx))){
                        if (CimStringUtils.equals(BizConstant.SP_LOT_TYPE_PRODUCTIONMONITORLOT,strWhatNextInqResult.getStrWhatNextAttributes().get(i).getLotType())){
                            log.info("loadPurposeType == ProcessMonitorLot and LotType == ProcessMonitorLot");
                            startCassette.setLoadPurposeType(BizConstant.SP_LOADPURPOSETYPE_PROCESSMONITORLOT);
                        }else {
                            log.info("UnMatch LoadPurposeType  ..<<continue>>");
                            continue;
                        }
                    }else {
                        log.info("Set LoadPurposeType [ProcessLot]");
                        startCassette.setLoadPurposeType(BizConstant.SP_LOADPURPOSETYPE_PROCESSLOT);
                    }
                    startCassette.setLoadPortID(portID.get(nAssignPortIdx));
                    //Get PortGroupID
                    log.info("Get PortGroupID");
                    bFoundFlag = false;
                    for (int j = 0; j < nPortGroupLen; j++) {
                        nPortLen = CimArrayUtils.getSize(strPortGroupSeq.get(j).getStrPortID());
                        for (int k = 0; k < nPortLen; k++) {
                            if (ObjectIdentifier.equalsWithValue(tmpStartCassette.get(0).getLoadPortID(),
                                    strPortGroupSeq.get(j).getStrPortID().get(k).getPortID())){
                                portGroupID = strPortGroupSeq.get(j).getPortGroup();
                                bFoundFlag = true;
                                break;
                            }
                        }
                        if (CimBooleanUtils.isTrue(bFoundFlag)){
                            break;
                        }
                    }
                    //Get Contained Lot in Cassette
                    log.info("===== cassette_GetLotList() ==================");
                    //【step12】 - cassette_GetLotList
                    Infos.LotListInCassetteInfo cassetteLotListGetDROutRetCode = null;
                    try {
                        cassetteLotListGetDROutRetCode = cassetteMethod.cassetteGetLotList(objCommonIn, tmpStartCassette.get(0).getCassetteID());
                    } catch (ServiceException e) {
                        log.info("cassetteGetLotList() is not OK   ...<<continue>>");
                        continue;
                    }
                    int nLotLen = CimArrayUtils.getSize(cassetteLotListGetDROutRetCode.getLotIDList());
                    List<Infos.LotInCassette> lotInCassetteList = new ArrayList<>();
                    startCassette.setLotInCassetteList(lotInCassetteList);
                    for (int j = 0; j < nLotLen; j++) {
                        Infos.LotInCassette lotInCassette = new Infos.LotInCassette();
                        lotInCassetteList.add(j,lotInCassette);
                        lotInCassette.setRecipeParameterChangeType(BizConstant.SP_RPARM_CHANGETYPE_BYLOT);
                        lotInCassette.setMoveInFlag(true);
                        lotInCassette.setLotID(cassetteLotListGetDROutRetCode.getLotIDList().get(j));

                        CimLot aLot = baseCoreFactory.getBO(CimLot.class, tmpStartCassette.get(0).getLotInCassetteList().get(j).getLotID());
                        Validations.check(aLot == null, new OmCode(retCodeConfig.getNotFoundLot(), ObjectIdentifier.fetchValue(tmpStartCassette.get(0).getLotInCassetteList().get(j).getLotID())));
                        lotInCassette.setLotType(aLot.getLotType());
                        lotInCassette.setSubLotType(aLot.getSubLotType());
                        //【step13】- lot_productID_Get
                        Outputs.ObjLotProductIDGetOut lotProductIDGetRetCode = lotMethod.lotProductIDGet(objCommonIn,tmpStartCassette.get(0).getLotInCassetteList().get(j).getLotID());

                        lotInCassette.setProductID(lotProductIDGetRetCode.getProductID());

                        //Set Operation Info
                        log.info("Set Opertiion Info");
                        for (int m = 0; m < attrLen; m++) {
                            if (ObjectIdentifier.equalsWithValue(tmpStartCassette.get(0).getLotInCassetteList().get(j).getLotID(),
                                    strWhatNextInqResult.getStrWhatNextAttributes().get(m).getLotID())){
                                Infos.StartOperationInfo startOperationInfo = new Infos.StartOperationInfo();
                                lotInCassette.setStartOperationInfo(startOperationInfo);
                                startOperationInfo.setProcessFlowID(strWhatNextInqResult.getStrWhatNextAttributes().get(m).getRouteID());
                                startOperationInfo.setOperationID(strWhatNextInqResult.getStrWhatNextAttributes().get(m).getOperationID());
                                startOperationInfo.setOperationNumber(strWhatNextInqResult.getStrWhatNextAttributes().get(m).getOperationNumber());
                                break;
                            }
                        }
                        if (CimStringUtils.equals(BizConstant.SP_LOADPURPOSETYPE_PROCESSMONITORLOT,tmpStartCassette.get(0).getLoadPurposeType())){
                            //Get Lot Type
                            log.info("===== lot_type_Get() ==================");
                            //【step14】 - lot_type_Get
                            String lotTypeRetCode = null;
                            try {
                                lotTypeRetCode = lotMethod.lotTypeGet(objCommonIn, tmpStartCassette.get(0).getLotInCassetteList().get(j).getLotID());
                            } catch (ServiceException e) {
                                continue;
                            }
                            if (CimStringUtils.equals(BizConstant.SP_LOT_TYPE_PRODUCTIONMONITORLOT, lotTypeRetCode)){
                                log.info("LotType == [MonitorLot]");
                                lotInCassette.setMonitorLotFlag(true);
                            }else {
                                log.info("LotType != [MonitorLot]");
                                lotInCassette.setMonitorLotFlag(false);
                            }
                        } else {
                            lotInCassette.setMonitorLotFlag(false);
                        }
                        //Get Contained Wafer in Lot
                        log.info("===== lot_waferMap_Get() ==================");
                        //【step15】 - lot_waferMap_Get
                        List<Infos.LotWaferMap> lotWaferMapGetRetCode = null;
                        try {
                            lotWaferMapGetRetCode = lotMethod.lotWaferMapGet(objCommonIn, tmpStartCassette.get(0).getLotInCassetteList().get(j).getLotID());
                        } catch (ServiceException e) {
                            log.info("lot_waferMap_Get() != RC_OK   ..<<continue>>");
                            continue;
                        }
                        int nWafLen = CimArrayUtils.getSize(lotWaferMapGetRetCode);
                        List<Infos.LotWafer> lotWaferList = new ArrayList<>();
                        lotInCassette.setLotWaferList(lotWaferList);
                        for (int k = 0; k < nWafLen; k++) {
                            List<Infos.StartRecipeParameter> startRecipeParameterList = new ArrayList<>();
                            Infos.LotWafer lotWafer = new Infos.LotWafer();
                            lotWaferList.add(lotWafer);
                            lotWafer.setWaferID(lotWaferMapGetRetCode.get(k).getWaferID());
                            lotWafer.setSlotNumber(lotWaferMapGetRetCode.get(k).getSlotNumber());
                            lotWafer.setControlWaferFlag(lotWaferMapGetRetCode.get(k).isControlWaferFlag());
                            lotWafer.setStartRecipeParameterList(startRecipeParameterList);
                        }
                    }
                    log.info("Finished tmpStartCassette Info  ..<<break!! WhatNextInfo Loop>>");
                    break;
                }
                //It is finished if Loop of WhatNextInfo finishes turning twice.
                if (i >= attrLen -1 ){
                    nWIPLotLoopEndCnt++;
                    log.info("Turned WhatNextInfo Loop!!  Therefore Count It");
                    if (1 < nWIPLotLoopEndCnt){
                        log.info("Turned <<Twice>> WhatNextInfo Loop!!   Therefore ..<<break!! WhatNextInfo Loop>>");
                        break;
                    }
                }
                //Set Next Index of WIPLot loop
                startNo = i + 1;
                log.info("make strStartCassette Process  done.");
                if (CimArrayUtils.getSize(tmpStartCassette) == 0 || ObjectIdentifier.isEmptyWithValue(tmpStartCassette.get(0).getCassetteID())){
                    log.info("tmpStartCassette[0].cassetteID.identifier is null  ..<<continue>>");
                    continue;
                }
                //Set logicalRecipeID, machineRecipeID and Set not use operationStartFlag
                log.info("Set logicalRecipeID, machineRecipeID and Set not use operationStartFlag ");
                int nLotLen = CimArrayUtils.getSize(tmpStartCassette.get(0).getLotInCassetteList());
                for (int i1 = 0; i1 < nLotLen; i1++) {
                    Boolean bLotFindFlag = false;
                    for (int j = 0; j < attrLen; j++) {
                        if (ObjectIdentifier.equalsWithValue(strWhatNextInqResult.getStrWhatNextAttributes().get(j).getLotID(),
                                tmpStartCassette.get(0).getLotInCassetteList().get(i1).getLotID())){
                            //Set logicalRecipeID, machineRecipeID for strWhatNextInqResult
                            Infos.LotInCassette lotInCassette = tmpStartCassette.get(0).getLotInCassetteList().get(i1);
                            Infos.StartRecipe startRecipe = new Infos.StartRecipe();
                            lotInCassette.setStartRecipe(startRecipe);
                            startRecipe.setLogicalRecipeID(strWhatNextInqResult.getStrWhatNextAttributes().get(j).getLogicalRecipeID());
                            startRecipe.setMachineRecipeID(strWhatNextInqResult.getStrWhatNextAttributes().get(j).getMachineRecipeID());
                            startRecipe.setPhysicalRecipeID(strWhatNextInqResult.getStrWhatNextAttributes().get(j).getPhysicalRecipeID());
                            bLotFindFlag = true;
                        }
                    }
                    if (CimBooleanUtils.isFalse(bLotFindFlag)){
                        // Set operationStartFlag for strWhatNextInqResult
                        log.info("operationStartFlag <--- FALSE");
                        tmpStartCassette.get(0).getLotInCassetteList().get(i1).setMoveInFlag(false);
                    }
                }
                // Set operationStartFlag for Recipe
                log.info(" Set operationStartFlag for Recipe ");
                CimCassette aCassette = baseCoreFactory.getBO(CimCassette.class, tmpStartCassette.get(0).getCassetteID());
                Validations.check(aCassette == null, new OmCode(retCodeConfig.getNotFoundCassette(), ObjectIdentifier.fetchValue(tmpStartCassette.get(0).getCassetteID())));
                multiLotType = aCassette.getMultiLotType();
                if (CimStringUtils.equals(BizConstant.SP_EQP_MULTIRECIPECAPABILITY_SINGLERECIPE, multiRecipeCapability)
                        && CimStringUtils.equals(BizConstant.SP_CAS_MULTILOTTYPE_MULTILOTMULTIRECIPE, multiLotType)){
                    log.info("multiRecipeCapability is [SingleRecipe] and multiLotType is [ML-MR]");
                    int nLotLen1 = CimArrayUtils.getSize(tmpStartCassette.get(0).getLotInCassetteList());
                    for (int j = 0; j < nLotLen1; j++) {
                        //Set OperationStartFlag for Recipe
                        //Set Temporary Base Recipe
                        if (processRunCount == 0){
                            // Temporary Base Recipe
                            log.info("Temporary Base Recipe");
                        }else {
                            // Final Base Recipe
                            log.info("Final Base Recipe");
                            tmpBaseLogicalRecipeID = baseLogicalRecipeID;
                            tmpBaseMachineRecipeID = baseMachineRecipeID;
                        }
                        ObjectIdentifier logicalRecipeID = tmpStartCassette.get(0).getLotInCassetteList().get(j).getStartRecipe() == null ? null : tmpStartCassette.get(0).getLotInCassetteList().get(j).getStartRecipe().getLogicalRecipeID();
                        ObjectIdentifier machineRecipeID = tmpStartCassette.get(0).getLotInCassetteList().get(j).getStartRecipe() == null ? null : tmpStartCassette.get(0).getLotInCassetteList().get(j).getStartRecipe().getMachineRecipeID();
                        if (ObjectIdentifier.equalsWithValue(tmpBaseLogicalRecipeID, logicalRecipeID)
                                && ObjectIdentifier.equalsWithValue(tmpBaseMachineRecipeID, machineRecipeID)){
                            log.info("tmpBaseLogicalRecipeID == logicalRecipeID && tmpBaseMachineRecipeID == machineRecipeID");
                        }else {
                            tmpStartCassette.get(0).getLotInCassetteList().get(j).setMoveInFlag(false);
                        }
                    }
                }
                //Set StartRecipeParameter
                log.info("Set StartRecipeParameter");
                for (int j = 0; j < nLotLen; j++) {
                    if (CimBooleanUtils.isFalse(tmpStartCassette.get(0).getLotInCassetteList().get(j).getMoveInFlag())) {
                        continue;
                    }
                    CimLogicalRecipe aLogicalRecipe = baseCoreFactory.getBO(CimLogicalRecipe.class, tmpStartCassette.get(0).getLotInCassetteList().get(j).getStartRecipe().getLogicalRecipeID());
                    Validations.check(aLogicalRecipe == null, retCodeConfig.getNotFoundLogicalRecipe());
                    CimMachineRecipe aMachineRecipe = null;
                    //Get subLotType
                    CimLot aLot = null;
                    String subLotType = null;
                    if (CimStringUtils.isEmpty(tmpStartCassette.get(0).getLotInCassetteList().get(j).getSubLotType())){
                        aLot = baseCoreFactory.getBO(CimLot.class, tmpStartCassette.get(0).getLotInCassetteList().get(j).getLotID());
                        Validations.check(aLot == null, new OmCode(retCodeConfig.getNotFoundLot(), ObjectIdentifier.fetchValue(tmpStartCassette.get(0).getLotInCassetteList().get(j).getLotID())));
                        subLotType = aLot.getSubLotType();
                    }else {
                        subLotType = tmpStartCassette.get(0).getLotInCassetteList().get(j).getSubLotType();
                    }
                    Boolean skipFlag = false;
                    String exchangeType = BizConstant.SP_FPC_EXCHANGETYPE_STARTRESERVEINFO;
                    Outputs.ObjLotEffectiveFPCInfoGetOut lotEffectiveFPCInfoGetOutRetCode = lotMethod.lotEffectiveFPCInfoGet(objCommonIn, exchangeType, equipmentID, tmpStartCassette.get(0).getLotInCassetteList().get(j).getLotID());

                    if (CimBooleanUtils.isTrue(lotEffectiveFPCInfoGetOutRetCode.isMachineRecipeActionRequiredFlag())){
                        log.info("MachineRecipe is overwritten by FPC");
                    }else {
                        if (searchCondition == 1){
                            if (CimObjectUtils.isEmpty(aLot)){
                                aLot = baseCoreFactory.getBO(CimLot.class, tmpStartCassette.get(0).getLotInCassetteList().get(j).getLotID());
                                Validations.check(aLot == null, new OmCode(retCodeConfig.getNotFoundLot(), ObjectIdentifier.fetchValue(tmpStartCassette.get(0).getLotInCassetteList().get(j).getLotID())));
                            }
                            aMachineRecipe = aLogicalRecipe.findMachineRecipeFor(aLot, aMachine);
                        }else {
                            aMachineRecipe = aLogicalRecipe.findMachineRecipeForSubLotType(aMachine, subLotType);
                        }
                        List<RecipeDTO.RecipeParameter> recipeParameterSeq = aLogicalRecipe.findRecipeParametersForSubLotType(aMachine, aMachineRecipe, subLotType);

                        int rpmCnt = CimArrayUtils.getSize(recipeParameterSeq);
                        int nWafLen = CimArrayUtils.getSize(tmpStartCassette.get(0).getLotInCassetteList().get(j).getLotWaferList());
                        for (int k = 0; k < nWafLen; k++) {
                            if (CimObjectUtils.isEmpty(recipeParameterSeq)){
                                tmpStartCassette.get(0).getLotInCassetteList().get(j).getLotWaferList().get(k).setStartRecipeParameterList(new ArrayList<>());
                            }else {
                                for (int l = 0; l < rpmCnt; l++) {
                                    List<Infos.StartRecipeParameter> startRecipeParameterList = tmpStartCassette.get(0).getLotInCassetteList().get(j).getLotWaferList().get(k).getStartRecipeParameterList();
                                    Infos.StartRecipeParameter startRecipeParameter = new Infos.StartRecipeParameter();
                                    startRecipeParameterList.add(startRecipeParameter);

                                    startRecipeParameter.setParameterName(recipeParameterSeq.get(l).getParameterName());//parameterName
                                    if (CimBooleanUtils.isTrue(recipeParameterSeq.get(l).getUseCurrentValueFlag())){ //useCurrentValueFlag
                                        startRecipeParameter.setParameterValue("");
                                    } else {
                                        startRecipeParameter.setParameterValue(recipeParameterSeq.get(l).getDefaultValue());//defaultValue
                                    }
                                    startRecipeParameter.setTargetValue(recipeParameterSeq.get(l).getDefaultValue());
                                    startRecipeParameter.setUseCurrentSettingValueFlag(recipeParameterSeq.get(l).getUseCurrentValueFlag());
                                }
                            }
                        }
                    }
                }
                //check strStartCassette Process
                log.info("check strStartCassette Process");
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
                /*                                                                       */
                /*-----------------------------------------------------------------------*/
                log.info("===== Check Process for Cassette ==========");
                long saveLoadSequenceNumber = CimNumberUtils.longValue(tmpStartCassette.get(0).getLoadSequenceNumber());
                tmpStartCassette.get(0).setLoadSequenceNumber(1L);

                //*===== for emptyCassetteCount =====*/
                if (CimBooleanUtils.isTrue(equipmentProcessBatchConditionRetCode.isCassetteExchangeFlag())
                        || CimBooleanUtils.isTrue(equipmentProcessBatchConditionRetCode.isMonitorCreationFlag())
                        || equipmentProcessBatchConditionRetCode.getMinBatchSize() > 1){
                    log.info("cassetteExchangeFlag == TRUE or monitorCreationFlag  == TRUE or minBatchSize > 1");
                    log.info("call cassette_CheckConditionForDelivery()");
                    String operation = BizConstant.SP_OPERATION_STARTRESERVATION;
                    //step17 - cassette_CheckConditionForDelivery
                    try{
                        cassetteMethod.cassetteCheckConditionForDelivery(objCommonIn, equipmentID, portGroupID, tmpStartCassette, operation);
                    }catch (ServiceException e) {
                        log.error("cassette_CheckConditionForDelivery() != RC_OK   ..<<continue>>");
                        continue;
                    }
                } else {
                    log.info("!cassetteExchangeFlag == TRUE or monitorCreationFlag  == TRUE or minBatchSize > 1");
                    log.info("call cassette_CheckConditionForOperation()");
                    //【step18】 - cassette_CheckConditionForOperation
                    String operation = BizConstant.SP_OPERATION_STARTRESERVATION;
                    try {
                        cassetteMethod.cassetteCheckConditionForOperation(objCommonIn,equipmentID, portGroupID, tmpStartCassette, operation);
                    }catch (ServiceException e) {
                        log.error("cassette_CheckConditionForOperation() != RC_OK   ..<<continue>>");
                        continue;
                    }
                }
                tmpStartCassette.get(0).setLoadSequenceNumber(saveLoadSequenceNumber);
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
                log.info("===== lot_CheckConditionForOperation() ==========");
                String operation =BizConstant.SP_OPERATION_CASSETTEDELIVERY ;
                //【step19】 - lot_CheckConditionForOperation
                try {
                    lotMethod.lotCheckConditionForOperation(objCommonIn,
                            equipmentID,
                            portGroupID,
                            tmpStartCassette,
                            operation);
                } catch (ServiceException e) {
                    log.info("lot_CheckConditionForOperation() != RC_OK   ..<<continue>>");
                    continue;
                }


                /*-----------------------------------------------------------------------------*/
                /*                                                                             */
                /*   Check Equipment Port for Start Reservation                                */
                /*                                                                             */
                /*   The following conditions are checked by this object                       */
                /*                                                                             */
                /*   1. In-parm's portGroupID must not have controlJobID.                      */
                /*   2. All of ports' loadMode must be CIMFW_PortRsc_Input or _InputOutput.    */
                /*   3. All of port, which is registered as in-parm's portGroup, must be       */
                /*      _LoadAvail or _LoadReq when equipment is online.                       */
                /*      As exceptional case, if equipment's takeOutInTransferFlag is True,     */
                /*      _UnloadReq is also OK for start reservation when equipment is Online.  */
                /*   4. All of port, which is registered as in-parm's portGroup,               */
                /*      must not have loadedCassetteID.                                        */
                /*   5. strStartCassette[].loadPortID's portGroupID must be same               */
                /*      as in-parm's portGroupID.                                              */
                /*   6. strStartCassette[].loadPurposeType must be match as specified port's   */
                /*      loadPutposeType.                                                       */
                /*   7. strStartCassette[].loadSequenceNumber must be same as specified port's */
                /*      loadSequenceNumber.                                                    */
                /*                                                                             */
                /*-----------------------------------------------------------------------------*/
                log.info("===== equipment_portState_CheckForStartReservation() ==========");
                //【step20】 - equipment_portState_CheckForStartReservation
                try {
                    equipmentMethod.equipmentPortStateCheckForStartReservation(objCommonIn,
                            equipmentID,
                            portGroupID,
                            tmpStartCassette,
                            false);
                } catch (ServiceException e) {
                    continue;
                }
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
                log.info("===== Check Process for Process Durable ==========");
                Boolean  durableRequiredFlag = false;
                log.info("call equipment_processDurableRequiredFlag_Get()");
                //【step21】 - equipment_processDurableRequiredFlag_Get
                try {
                    equipmentMethod.equipmentProcessDurableRequiredFlagGet(objCommonIn, equipmentID);
                } catch (ServiceException e){
                    if (Validations.isEquals(retCodeConfig.getEquipmentProcessDurableReticleRequired(), e.getCode())
                            || Validations.isEquals(retCodeConfig.getEquipmentProcessDurableFixtRequired(), e.getCode())){
                        log.info("rc == RC_EQP_PROCDRBL_RTCL_REQD || rc == RC_EQP_PROCDRBL_FIXT_REQD");
                        if (!CimStringUtils.equals(BizConstant.SP_LOADPURPOSETYPE_EMPTYCASSETTE,tmpStartCassette.get(0).getLoadPurposeType())){
                            log.info("tmpStartCassette[0].loadPurposeType != SP_LoadPurposeType_EmptyCassette");
                            int nLotLen2 = CimArrayUtils.getSize(tmpStartCassette.get(0).getLotInCassetteList());
                            for (int j = 0; j < nLotLen2; j++) {
                                if (CimBooleanUtils.isFalse(tmpStartCassette.get(0).getLotInCassetteList().get(j).getMoveInFlag())){
                                    log.info("tmpStartCassette[0].strLotInCassette[j].operationStartFlag == FALSE");
                                    continue;
                                }
                                // Check Process Durable Condition for OpeStart
                                log.info("call processDurable_CheckConditionForOpeStart()");
                                //【step22】 - processDurable_CheckConditionForOpeStart
                                Outputs.ObjProcessDurableCheckConditionForOperationStartOut durableCheckConditionForOperationStartOutRetCode = null;
                                try{
                                    durableCheckConditionForOperationStartOutRetCode = processMethod.processDurableCheckConditionForOpeStart(objCommonIn,
                                            equipmentID,
                                            tmpStartCassette.get(0).getLotInCassetteList().get(j).getStartRecipe().getLogicalRecipeID(),
                                            tmpStartCassette.get(0).getLotInCassetteList().get(j).getStartRecipe().getMachineRecipeID(),
                                            tmpStartCassette.get(0).getLotInCassetteList().get(j).getLotID());
                                }catch(ServiceException ex){
                                    log.error("processDurable_CheckConditionForOpeStart() != RC_OK");
                                    durableRequiredFlag = true;
                                    break;
                                }

                                //Set Available Reticles / Fixtures
                                tmpStartCassette.get(0).getLotInCassetteList().get(j).getStartRecipe().setStartReticleList(durableCheckConditionForOperationStartOutRetCode.getStartReticleList());
                                tmpStartCassette.get(0).getLotInCassetteList().get(j).getStartRecipe().setStartFixtureList(durableCheckConditionForOperationStartOutRetCode.getStartFixtureList());
                            }
                            if (CimBooleanUtils.isTrue(durableRequiredFlag)){
                                continue;
                            }
                        }
                    } else if (!Validations.isEquals(retCodeConfig.getEquipmentProcessDurableNotRequired(), e.getCode())){
                        log.info("rc != RC_EQP_PROCDRBL_NOT_REQD   ..<<continue>>");
                        continue;
                    }
                }

                /* *********************************************************************************/
                /*                                                                                */
                /*   Check multiRecipeCapability and multiLotType                                 */
                /*   and Decide finally to put it in StartCassette                                */
                /*                                                                                */
                /* *********************************************************************************/
                log.info("Check multiRecipeCapability and multiLotType");
                //----------------------------------------------------------------------
                //      Equipment's         Cassette's
                // multiRecipeCapability    multiLotType    Same Recipe Check
                // =====================================================================
                // M-Recipe                 SL-SR           FALSE
                //                          ML-SR           FALSE
                //                          ML-MR           FALSE
                // -----------------------  --------------------------------------------
                // S-Recipe                 SL-SR           TRUE
                //                          ML-SR           TRUE
                //                          ML-MR           TRUE
                // -----------------------  --------------------------------------------
                // Batch                    SL-SR           TRUE
                //                          ML-SR           TRUE
                //                          ML-MR           Error
                // -----------------------  --------------------------------------------
                Boolean bAddStartCassette = false;
                if (CimStringUtils.equals(BizConstant.SP_EQP_MULTIRECIPECAPABILITY_MULTIPLERECIPE,multiRecipeCapability)){
                    // M-Recipe: SL-SR, ML-SR, ML-MR
                    // Same Recipe Check : FALSE
                    log.info("M-Recipe: SL-SR, ML-SR, ML-MR");
                    log.info("Same Recipe Check : FALSE");
                    bAddStartCassette = true;
                }else if (CimStringUtils.equals(BizConstant.SP_EQP_MULTIRECIPECAPABILITY_BATCH,multiRecipeCapability)
                        && CimStringUtils.equals(BizConstant.SP_CAS_MULTILOTTYPE_MULTILOTMULTIRECIPE,multiLotType)){
                    // Batch and ML-MR
                    // Error
                    log.error("Batch and ML-MR: continue");
                    continue;
                }else {
                    // S-Recipe: SL-SR, ML-SR, ML-MR
                    // Batch   : SL-SR, ML-SR
                    // Same Recipe Check : TRUE
                    log.info("S-Recipe: SL-SR, ML-SR, ML-MR");
                    log.info("Batch   : SL-SR, ML-SR");
                    log.info("Same Recipe Check : TRUE");
                    if (processRunCount == 0){
                        log.info("processRunCount == 0");
                        bAddStartCassette = true;
                        /*------------------------------------------------------------*/
                        /*   Set Base Recipe (First operationStartFlag=TRUE Recipe)   */
                        /*------------------------------------------------------------*/
                        for (i = 0; i < tmpStartCassette.get(0).getLotInCassetteList().size(); i++) {
                            if (CimBooleanUtils.isTrue(tmpStartCassette.get(0).getLotInCassetteList().get(i).getMoveInFlag())){
                                baseLogicalRecipeID = tmpStartCassette.get(0).getLotInCassetteList().get(i).getStartRecipe().getLogicalRecipeID();
                                baseMachineRecipeID = tmpStartCassette.get(0).getLotInCassetteList().get(i).getStartRecipe().getMachineRecipeID();
                                break;
                            }
                        }
                        if (ObjectIdentifier.isEmptyWithValue(baseLogicalRecipeID)
                                || ObjectIdentifier.isEmptyWithValue(baseMachineRecipeID)){
                            log.info(" Can not Found Base Recipe!!");
                        }
                    }else {
                        ObjectIdentifier logicalRecipeID = new ObjectIdentifier();
                        ObjectIdentifier machineRecipeID = new ObjectIdentifier();
                        //Find Recipe (First operationStartFlag=TRUE Recipe)
                        for (i = 0; i < tmpStartCassette.get(0).getLotInCassetteList().size(); i++) {
                            if (CimBooleanUtils.isTrue(tmpStartCassette.get(0).getLotInCassetteList().get(i).getMoveInFlag())){
                                logicalRecipeID = tmpStartCassette.get(0).getLotInCassetteList().get(i).getStartRecipe().getLogicalRecipeID();
                                machineRecipeID = tmpStartCassette.get(0).getLotInCassetteList().get(i).getStartRecipe().getMachineRecipeID();
                                break;
                            }
                        }
                        if (ObjectIdentifier.isEmptyWithValue(logicalRecipeID)
                                || ObjectIdentifier.isEmptyWithValue(machineRecipeID)){
                            log.info(" Can not Found Base Recipe!!");
                        }
                        if (ObjectIdentifier.equalsWithValue(baseLogicalRecipeID, logicalRecipeID)
                                && ObjectIdentifier.equalsWithValue(baseMachineRecipeID, machineRecipeID)){
                            bAddStartCassette = true;
                        }else {
                            log.info("Deferent Recipe.  ...<<<continue>>>");
                            continue;
                        }
                    }
                }
                /* *********************************************************************************/
                /*                                                                                */
                /*   Add tmpStartCassette to StartCassette                                        */
                /*                                                                                */
                /* *********************************************************************************/
                log.info("Add tmpStartCassette to StartCassette");
                if (CimBooleanUtils.isTrue(bAddStartCassette)){
                    /* ******************************************************************************/
                    /*   Check MonitorCreationFlag                                                 */
                    /*   Only one time of the beginnings                                           */
                    /* ******************************************************************************/
                    if (CimBooleanUtils.isFalse(bAlreadyCheckMonitorCreationFlag)
                            && CimBooleanUtils.isTrue(equipmentProcessBatchConditionRetCode.isMonitorCreationFlag())){
                        log.info("===== Check MonitorCreationFlag ==========");
                        CimLogicalRecipe aLogicalRecipe = baseCoreFactory.getBO(CimLogicalRecipe.class, tmpBaseLogicalRecipeID);
                        Validations.check(aLogicalRecipe == null, new OmCode(retCodeConfig.getNotFoundLogicalRecipe()));
                        CimProductSpecification aMonitorProduct = aLogicalRecipe.getMonitorProduct();
                        if (!CimObjectUtils.isEmpty(aMonitorProduct)){
                            //EmptyCassette is necessary!
                            log.info("===== Set EmptyCassette for MonitorCreation ==========");
                            /*------------------------------------------------*/
                            /*   Look for Port to assign, and EmptyCassette   */
                            /*------------------------------------------------*/
                            log.info("Look for Port to assign, and EmptyCassette");
                            //【step23】 - cassetteDelivery_SearchEmptyCassetteAssignPort
                            ObjectIdentifier dummyLotID = new ObjectIdentifier();
                            Outputs.ObjCassetteDeliverySearchEmptyCassetteAssignPortOut cassetteDeliverySearchEmptyCassetteAssignPortRetCode = null;
                            try{
                                cassetteDeliverySearchEmptyCassetteAssignPortRetCode = cassetteMethod.cassetteDeliverySearchEmptyCassetteAssignPort(objCommonIn,
                                        dummyLotID, strPortGroupSeq.get(0).getStrPortID(), cassetteListEmptyAvailablePickupOutRetCode.getStrFoundCassette(),
                                        useEmptyCassetteIDSeq, useAssignEmptyCassettePortSeq);
                            }catch (ServiceException e) {
                                log.error("NotFound!! Assign EmptyCassette or Assign Port!   set bWhileExitFlag = TRUE  ...<<<continue>>>");
                                bWhileExitFlag = true;
                                continue;
                            }
                            //Hold EmptyCasstte and Port to prevent duplication
                            log.info("Hold EmptyCasstte and Port to prevent duplication");
                            useAssignEmptyCassettePortSeq.add(nAssignEmptyCassetteCnt,cassetteDeliverySearchEmptyCassetteAssignPortRetCode.getStrFoundPort().getPortID());
                            useEmptyCassetteIDSeq.add(nAssignEmptyCassetteCnt,cassetteDeliverySearchEmptyCassetteAssignPortRetCode.getFoundEmptyCassetteID());
                            nAssignEmptyCassetteCnt++;
                            /*-------------------------------------------------------------------------*/
                            /*   Found!! Assign EmptyCassette. ---> Add information on startCassette   */
                            /*   Put it at the head of StartCassette surely!!                          */
                            /*-------------------------------------------------------------------------*/
                            log.info("Found!! Assign EmptyCassette. ---> Add information on startCassette");
                            strStartCassette.add(new Infos.StartCassette());
                            strStartCassette.get(0).setLoadSequenceNumber(cassetteDeliverySearchEmptyCassetteAssignPortRetCode.getStrFoundPort().getLoadSequenceNoInPortGroup());
                            strStartCassette.get(0).setCassetteID(cassetteDeliverySearchEmptyCassetteAssignPortRetCode.getFoundEmptyCassetteID());
                            strStartCassette.get(0).setLoadPurposeType(BizConstant.SP_LOADPURPOSETYPE_EMPTYCASSETTE);
                            strStartCassette.get(0).setLoadPortID(cassetteDeliverySearchEmptyCassetteAssignPortRetCode.getStrFoundPort().getPortID());
                            nSetStartCassetteCnt ++;
                            if (Objects.equals(strStartCassette.get(0).getLoadSequenceNumber(), tmpStartCassette.get(0).getLoadSequenceNumber())){
                                int lenPortIDs = CimArrayUtils.getSize(portID);
                                if (1 < lenPortIDs){
                                    //Set next port info
                                    tmpStartCassette.get(0).setLoadSequenceNumber(loadSequenceNumber.get(1));
                                    tmpStartCassette.get(0).setLoadPortID(portID.get(1));
                                }
                            }
                        }
                        bAlreadyCheckMonitorCreationFlag = true;
                    }
                    /*------------------------------------------------------------------------------------------------------*/
                    /*   Selected PortID and CassetteID is stocked.                                                         */
                    /*   Originally this Sequence(useAssignEmptyCassettePortSeq, useEmptyCassetteIDSeq) exists for Empty.   */
                    /*   But, by D4200302, Cassette must assign also to AnyPort.                                            */
                    /*   Here, if it does not set to these Sequences, following problem arises                              */
                    /*                                               by cassetteDelivery_SearchEmptyCassetteAssignPort().   */
                    /*     Equipment is CassetteExchange Type.                                                              */
                    /*                                                                                                      */
                    /*     PortID  PortGrp  Purpose                                                                         */
                    /*     --------------------------                                                                       */
                    /*       P1       A       Any                                                                           */
                    /*       P2       A       Any                                                                           */
                    /*                                                                                                      */
                    /*    First, P1 is assigned with ProcessLot.                                                            */
                    /*    Next, Search EmpPort by cassetteDelivery_SearchEmptyCassetteAssignPort().                         */
                    /*    But Function chooses P1.                                                                          */
                    /*    Because P1 is not set to useEmptyCassetteIDSeq.                                                   */
                    /*                                                                                                      */
                    /*    It is not necessary to put CassetteID into useAssignEmptyCassettePortSeq.                         */
                    /*    But sequence counter(nAssignEmptyCassetteCnt) becomes mismatching.                                */
                    /*    So, CassetteID is set also to useEmptyCassetteIDSeq.                                              */
                    /*------------------------------------------------------------------------------------------------------*/
                    useAssignEmptyCassettePortSeq.add(nAssignEmptyCassetteCnt,tmpStartCassette.get(0).getLoadPortID());
                    useEmptyCassetteIDSeq.add(nAssignEmptyCassetteCnt,tmpStartCassette.get(0).getCassetteID());
                    nAssignEmptyCassetteCnt++;
                    /* ******************************************************************************/
                    /*   Set EmptyCassette if it is necessary                                      */
                    /* ******************************************************************************/
                    if (CimBooleanUtils.isTrue(equipmentProcessBatchConditionRetCode.isCassetteExchangeFlag())){
                        log.info("===== Set EmptyCassette for CassetteExchange ==========");
                        /*------------------------------------------------------*/
                        /*   Look for the first Lot when OpeStartFlag is TRUE   */
                        /*------------------------------------------------------*/
                        log.info("Look for the first Lot when OpeStartFlag is TRUE");
                        ObjectIdentifier targetLotID = new ObjectIdentifier();
                        for (i = 0; i < tmpStartCassette.get(0).getLotInCassetteList().size(); i++) {
                            if (CimBooleanUtils.isTrue(tmpStartCassette.get(0).getLotInCassetteList().get(i).getMoveInFlag())){
                                targetLotID = tmpStartCassette.get(0).getLotInCassetteList().get(i).getLotID();
                                break;
                            }
                        }
                        if (ObjectIdentifier.isEmptyWithValue(targetLotID)){
                            continue;
                        }
                        /*------------------------------------------------*/
                        /*   Look for Port to assign, and EmptyCassette   */
                        /*------------------------------------------------*/
                        log.info("Look for Port to assign, and EmptyCassette ");
                        //【step24】 - cassetteDelivery_SearchEmptyCassetteAssignPort
                        Outputs.ObjCassetteDeliverySearchEmptyCassetteAssignPortOut cassetteDeliverySearchEmptyCassetteAssignPortOutRetCode = null;
                        try{
                            cassetteDeliverySearchEmptyCassetteAssignPortOutRetCode = cassetteMethod.cassetteDeliverySearchEmptyCassetteAssignPort(objCommonIn,
                                    targetLotID, strPortGroupSeq.get(0).getStrPortID(), cassetteListEmptyAvailablePickupOutRetCode.getStrFoundCassette(),
                                    useEmptyCassetteIDSeq, useAssignEmptyCassettePortSeq);
                        }catch (ServiceException e) {
                            log.error("NotFound!! Assign EmptyCassette or Assign Port!   set bWhileExitFlag = TRUE  ...<<<continue>>>");
                            bWhileExitFlag = true;
                            continue;
                        }
                        // Hold EmptyCasstte and Port to prevent duplication
                        log.info("Hold EmptyCasstte and Port to prevent duplication");
                        useAssignEmptyCassettePortSeq.add(nAssignEmptyCassetteCnt,cassetteDeliverySearchEmptyCassetteAssignPortOutRetCode.getStrFoundPort().getPortID());
                        useEmptyCassetteIDSeq.add(nAssignEmptyCassetteCnt,cassetteDeliverySearchEmptyCassetteAssignPortOutRetCode.getFoundEmptyCassetteID());
                        nAssignEmptyCassetteCnt++;
                        /*-------------------------------------------------------------------------*/
                        /*   Found!! Assign EmptyCassette. -----> Add information on TmpCassette   */
                        /*-------------------------------------------------------------------------*/
                        log.info("Found!! Assign EmptyCassette. ---> Add information on startCassette");
                        int lenTmp = CimArrayUtils.getSize(tmpStartCassette);
                        Infos.StartCassette startCassette = new Infos.StartCassette();
                        tmpStartCassette.add(startCassette);
                        startCassette.setLoadSequenceNumber(cassetteDeliverySearchEmptyCassetteAssignPortOutRetCode.getStrFoundPort().getLoadSequenceNoInPortGroup());
                        startCassette.setCassetteID(cassetteDeliverySearchEmptyCassetteAssignPortOutRetCode.getFoundEmptyCassetteID());
                        startCassette.setLoadPurposeType(BizConstant.SP_LOADPURPOSETYPE_EMPTYCASSETTE);
                        startCassette.setLoadPortID(cassetteDeliverySearchEmptyCassetteAssignPortOutRetCode.getStrFoundPort().getPortID());
                    }
                    //Add tmpStartCassette to StartCassette
                    log.info("===== Add tmpStartCassette to StartCassette ==========");
                    for (int j = 0; j < CimArrayUtils.getSize(tmpStartCassette); j++) {
                        strStartCassette.add(nSetStartCassetteCnt, tmpStartCassette.get(j));
                        nSetStartCassetteCnt++;
                    }
                    processRunCount++;
                }
            }
            //If selected Carrier does not fulfill MinBatchSize, they are omitted from candidate.
            if (CimBooleanUtils.isTrue(bCheckMinBatchSize) && processRunCount > 0 && processRunCount < equipmentProcessBatchConditionRetCode.getMinBatchSize()){
                for (int m = 0; m < nSetStartCassetteCnt; m++) {
                    omitCassetteSeq.add(strStartCassette.get(m).getCassetteID());
                }
            }else {
                break;
            }
        }
        // Final Check
        //Check processRunCount
        Validations.check (processRunCount == 0, retCodeConfigEx.getNotFoundFilledCast());
        Validations.check(processRunCount < equipmentProcessBatchConditionRetCode.getMinBatchSize(), retCodeConfigEx.getNotFoundFilledCast());
        //Check Mininum Wafer Count
        int nTotalWaferCount = 0;
        for (int i = 0; i < nSetStartCassetteCnt; i++) {
            int lenLot = CimArrayUtils.getSize(strStartCassette.get(i).getLotInCassetteList());
            for (int j = 0; j < lenLot; j++) {
                if (CimBooleanUtils.isTrue(strStartCassette.get(i).getLotInCassetteList().get(j).getMoveInFlag())){
                    nTotalWaferCount += CimArrayUtils.getSize(strStartCassette.get(i).getLotInCassetteList().get(j).getLotWaferList());
                }
            }
        }
        Validations.check (equipmentProcessBatchConditionRetCode.getMinWaferSize() > nTotalWaferCount, retCodeConfig.getInvalidInputWaferCount());
        if (!ObjectIdentifier.isEmptyWithValue(flowBatchCheckConditionForCassetteDeliveryRetCode.getFlowBatchID())){
            //processRunCount must be the same as FlowBatching Lots Count
            int lenFlowBatchLots = CimArrayUtils.getSize(flowBatchCheckConditionForCassetteDeliveryRetCode.getStrContainedLotsInFlowBatch());
            List<ObjectIdentifier> cassetteIDSeq = new ArrayList<>();
            int nFlowBatchCasIdx = 0;
            for (int i = 0; i < lenFlowBatchLots; i++) {
                Boolean bFound = false;
                int lenCas = CimArrayUtils.getSize(cassetteIDSeq);
                for (int j = 0; j < lenCas; j++) {
                    if (ObjectIdentifier.equalsWithValue(cassetteIDSeq.get(j), flowBatchCheckConditionForCassetteDeliveryRetCode.getStrContainedLotsInFlowBatch().get(i).getCassetteID())){
                        bFound = true;
                        break;
                    }
                }
                if (CimBooleanUtils.isFalse(bFound)){
                    cassetteIDSeq.add(nFlowBatchCasIdx,flowBatchCheckConditionForCassetteDeliveryRetCode.getStrContainedLotsInFlowBatch().get(i).getCassetteID());
                    nFlowBatchCasIdx++;
                }
            }
            Validations.check (processRunCount != nFlowBatchCasIdx, retCodeConfigEx.getNotSelectAllFlowBatchLots());
        }
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
        //【step25】 - equipment_lot_CheckFlowBatchConditionForOpeStart__090
        Inputs.ObjEquipmentLotCheckFlowBatchConditionForOperationStartIn operationStartIn=new Inputs.ObjEquipmentLotCheckFlowBatchConditionForOperationStartIn();
        operationStartIn.setEquipmentID(equipmentID);
        operationStartIn.setPortGroupID(portGroupID);
        operationStartIn.setStartCassetteList(strStartCassette);
        equipmentMethod.equipmentLotCheckFlowBatchConditionForOpeStart(objCommonIn, operationStartIn);
        long tmpFPCAdoptFlag = StandardProperties.OM_DOC_ENABLE_FLAG.getLongValue();
        if (1 == tmpFPCAdoptFlag){
            log.info("FPC Adopt Flag is ON. Now apply FPCInfo.");
            //【step26】 - FPCStartCassetteInfo_Exchange
            List<Infos.StartCassette> exchangeFPCStartCassetteInfo = fpcMethod.fpcStartCassetteInfoExchange(objCommonIn,
                    BizConstant.SP_FPC_EXCHANGETYPE_STARTRESERVEINFO,
                    equipmentID,
                    strStartCassette);

            strStartCassette = exchangeFPCStartCassetteInfo;
        }else {
            log.info("FPC Adopt Flag is OFF.");
        }
        //Set Return Struct
        return strStartCassette;
    }

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/7/17                               Neyo                create file
     *
     * @author Neyo
     * @since 2019/7/17 16:41
     * Copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Override
    public List<Infos.StartCassette> whatNextLotListToStartCassetteForTakeOutInDeliveryReq(Infos.ObjCommon objCommon, Inputs.ObjWhatNextLotListToStartCassetteForTakeOutInDeliveryReqIn input) {

        /*---------------------------------*/
        /*   Debug Trace Input Parameter   */
        /*---------------------------------*/
        ObjectIdentifier equipmentID = input.getEquipmentID();
        List<Infos.PortGroup> strPortGroupSeq = input.getStrPortGroup();
        Results.WhatNextLotListResult strWhatNextInqResult = input.getStrWhatNextInqResult();

        //Initialize
        String portGroupID = null;
        String multiLotType = null;
        int searchCondition = 0;
        String searchCondition_var = StandardProperties.OM_CONSTRAINT_CHK_WITH_SEARCH.getValue();
        if (CimStringUtils.length(searchCondition_var) > 0){
            searchCondition = Integer.parseInt(searchCondition_var);
        }
        //Check Condition and Get Information
        //Get Equipment Object
        CimMachine aMachine = baseCoreFactory.getBO(CimMachine.class, equipmentID);
        Validations.check(null == aMachine,retCodeConfig.getNotFoundEqp(), ObjectIdentifier.fetchValue(equipmentID));

        //Get Equipment's MultiRecipeCapability
        String multiRecipeCapability;
        multiRecipeCapability = aMachine.getMultipleRecipeCapability();

        //Get PortID List
        log.info("Get PortID List");
        List<ObjectIdentifier> portID = new ArrayList<>();
        List<Long> loadSequenceNumber = new ArrayList<>();
        List<String> loadPurposeType = new ArrayList<>();
        int portCount = 0;
        int nPortGroupLen = CimArrayUtils.getSize(strPortGroupSeq);
        for (int i = 0; i < nPortGroupLen; i++) {
            int nPortLen = CimArrayUtils.getSize(strPortGroupSeq.get(i).getStrPortID());
            for (int j = 0; j < nPortLen; j++) {
                if (CimStringUtils.equals(BizConstant.SP_LOADPURPOSETYPE_EMPTYCASSETTE,strPortGroupSeq.get(i).getStrPortID().get(j).getLoadPurposeType())){
                    log.info("port.loadPurposeType == EmptyCassette   ...<<continue>>");
                    continue;
                }
                portID.add(portCount,strPortGroupSeq.get(i).getStrPortID().get(j).getPortID());
                loadSequenceNumber.add(portCount,strPortGroupSeq.get(i).getStrPortID().get(j).getLoadSequenceNoInPortGroup());
                loadPurposeType.add(portCount,strPortGroupSeq.get(i).getStrPortID().get(j).getLoadPurposeType());
                portCount++;
            }
        }
        // Check PortCount and processRunSizeMaximum
        log.info("Check PortCount and processRunSizeMaximum");
        int processRunCount = 0;
        int processRunSizeMaximum = strWhatNextInqResult.getProcessRunSizeMaximum();
        Validations.check (portCount == 0 || portCount < processRunSizeMaximum, retCodeConfigEx.getNotFoundFilledCast());
        //Get Equipment's Process Batch Condition
        log.info("equipment_processBatchCondition_Get()");
        //【step1】 - equipment_processBatchCondition_Get
        Outputs.ObjEquipmentProcessBatchConditionGetOut equipmentProcessBatchConditionRetCode = equipmentMethod.equipmentProcessBatchConditionGet(objCommon, equipmentID);
        //Check WIP Lot Count
        log.info("Check WIP Lot Count");
        int attrLen = CimArrayUtils.getSize(strWhatNextInqResult.getStrWhatNextAttributes());
        Validations.check (attrLen == 0, retCodeConfigEx.getNotFoundFilledCast());

        //Get Empty Cassette
        log.info("Get Empty Cassette");
        Outputs.ObjCassetteListEmptyAvailablePickUpOut cassetteListEmptyAvailablePickupOutRetCode = null;
        if (CimBooleanUtils.isTrue(equipmentProcessBatchConditionRetCode.isCassetteExchangeFlag()
                || CimBooleanUtils.isTrue(equipmentProcessBatchConditionRetCode.isMonitorCreationFlag()))){
            log.info("EmptyCassette is necessary");
            //Get Empty Cassette
            Inputs.ObjCassetteListGetDRIn listGetDRIn = new Inputs.ObjCassetteListGetDRIn(new Params.CarrierListInqParams());
            listGetDRIn.setEmptyFlag(true);
            listGetDRIn.setCassetteStatus(CIMStateConst.CIM_DURABLE_AVAILABLE);
            listGetDRIn.setMaxRetrieveCount(-1);
            listGetDRIn.setSorterJobCreationCheckFlag(false);
            ObjectIdentifier dummy = ObjectIdentifier.buildWithValue("");
            listGetDRIn.setBankID(dummy);
            listGetDRIn.setDurablesSubStatus(dummy);
            listGetDRIn.setFlowStatus("");
            //【note】: add searchCondition default by neyo
            listGetDRIn.setSearchCondition(new SearchCondition());
            //【step2】 - cassette_ListGetDR__170
            Page<Infos.FoundCassette> carrierListInq170ResultRetCode = cassetteMethod.cassetteListGetDR170(objCommon, listGetDRIn);
            List<Infos.FoundCassette> strFoundCassette = new ArrayList<>();
            strFoundCassette = carrierListInq170ResultRetCode.getContent();
            //Pick Up Target Empty Cassette
            log.info("cassetteList_emptyAvailable_Pickup()");
            //【step3】 - cassetteList_emptyAvailable_Pickup
            try{
                cassetteListEmptyAvailablePickupOutRetCode = cassetteMethod.cassetteListEmptyAvailablePickup(objCommon,strFoundCassette);
            } catch (ServiceException e) {
                log.error("RC_OK != cassetteList_emptyAvailable_Pickup()");
            }
        }else {
            log.info("EmptyCassette is unnecessary");
        }

        //Get and Check FlowBatch Control
        log.info("Get and Check FlowBatch Info");
        //【step4】 - flowBatch_CheckConditionForCassetteDelivery
        Outputs.ObjFlowBatchCheckConditionForCassetteDeliveryOut flowBatchCheckConditionForCassetteDeliveryRetCode = flowBatchMethod.flowBatchCheckConditionForCassetteDelivery(objCommon,equipmentID,strWhatNextInqResult);

        //Make strStartCassette Process
        log.info("Make strStartCassette Process");
        List<Infos.StartCassette> strStartCassette = new ArrayList<>();
        boolean bCheckMinBatchSize = false;
        String checkMinBatchSize = StandardProperties.OM_DELIVERY_CHK_MINIMUM_BATCH_SIZE_ENABLE.getValue();
        if (CimStringUtils.equals(checkMinBatchSize,"1")
                && equipmentProcessBatchConditionRetCode.getMinBatchSize() > 1){
            log.info("bCheckMinBatchSize is 1");
            bCheckMinBatchSize = true;
        }
        int nSetStartCassetteCnt = 0;
        List<ObjectIdentifier> omitCassetteSeq = new ArrayList<>();
        while(true){
            //Reset variable
            processRunCount = 0;
            int nPortLen = CimArrayUtils.getSize(strPortGroupSeq.get(0).getStrPortID());
            int startNo = 0;
            boolean bWhileExitFlag = false;
            ObjectIdentifier baseLogicalRecipeID = new ObjectIdentifier();
            ObjectIdentifier baseMachineRecipeID = new ObjectIdentifier();
            ObjectIdentifier tmpBaseLogicalRecipeID = new ObjectIdentifier();
            ObjectIdentifier tmpBaseMachineRecipeID = new ObjectIdentifier();

            Boolean bAlreadyCheckMonitorCreationFlag = false;
            nSetStartCassetteCnt = 0;
            int nAssignEmptyCassetteCnt = 0;
            List<ObjectIdentifier> useEmptyCassetteIDSeq = new ArrayList<>();
            List<ObjectIdentifier> useAssignEmptyCassettePortSeq = new ArrayList<>();

            int nWIPLotLoopEndCnt= 0;
            int nSaveProcessRunCount = -1;
            while (processRunCount < processRunSizeMaximum){
                log.info("while ( processRunCount < processRunSizeMaximum ) ");
                if (CimBooleanUtils.isTrue(bWhileExitFlag)){
                    log.info("bWhileExitFlag == TRUE    break!!");
                    break;
                }
                Boolean bTmpBaseRecipeFlag = false;
                List<Infos.StartCassette> tmpStartCassette = new ArrayList<>();

                //Set Cassette Info
                log.info("Set Cassette Info");
                if (nSaveProcessRunCount != processRunCount){
                    log.info("Change processRunCount!!  WIPLot start index <--- 0");
                    startNo = 0;
                    nWIPLotLoopEndCnt = 0;
                }
                nSaveProcessRunCount = processRunCount;
                int i = startNo;
                for (i = startNo; i < attrLen; i++) {
                    // Omit CassetteID is NULL
                    if (ObjectIdentifier.isEmptyWithValue(strWhatNextInqResult.getStrWhatNextAttributes().get(i).getCassetteID())){
                        log.info("CassetteID is NULL   ..<<continue>>");
                        continue;
                    }
                    Boolean bOmitCassette = false;
                    int lenOmitCst = CimArrayUtils.getSize(omitCassetteSeq);
                    for (int m = 0; m < lenOmitCst; m++) {
                        if (ObjectIdentifier.equalsWithValue(omitCassetteSeq.get(m),
                                strWhatNextInqResult.getStrWhatNextAttributes().get(i).getCassetteID())){
                            bOmitCassette = true;
                            break;
                        }
                    }
                    if (CimBooleanUtils.isTrue(bOmitCassette)){
                        log.info("Omit CassetteID   ..<<continue>>");
                        continue;
                    }

                    //Omit already saved strStartCassette
                    Boolean bFoundFlag = false;
                    int lenStartCassette = CimArrayUtils.getSize(strStartCassette);
                    for (int j = 0; j < lenStartCassette; j++) {
                        if (ObjectIdentifier.equalsWithValue(strWhatNextInqResult.getStrWhatNextAttributes().get(i).getCassetteID(),
                                strStartCassette.get(j).getCassetteID())){
                            bFoundFlag = true;
                            break;
                        }
                    }
                    if (CimBooleanUtils.isTrue(bFoundFlag)){
                        log.info("CassetteID is Exist   ..<<continue>>");
                        continue;
                    }

                    //Omit Lot which is not FlowBatchingLots in the case of FlowBatch
                    if (!ObjectIdentifier.isEmptyWithValue(flowBatchCheckConditionForCassetteDeliveryRetCode.getFlowBatchID())){
                        log.info("Omit Lot which is not FlowBatchingLots in the case of FlowBatch");
                        Boolean bFound = false;
                        int lenFlowBatchLots = CimArrayUtils.getSize(flowBatchCheckConditionForCassetteDeliveryRetCode.getStrContainedLotsInFlowBatch());
                        for (int j = 0; j < lenFlowBatchLots; j++) {
                            if (ObjectIdentifier.equalsWithValue(strWhatNextInqResult.getStrWhatNextAttributes().get(i).getLotID(),
                                    flowBatchCheckConditionForCassetteDeliveryRetCode.getStrContainedLotsInFlowBatch().get(i).getLotID())){
                                bFound = true;
                                break;
                            }
                        }
                        if (CimBooleanUtils.isFalse(bFound)){
                            log.info("LotID is not FlowBatchingLots   ..<<continue>>");
                            continue;
                        }
                    }
                    //Omit lot if "Monitor" label is put on lot's process and the lot doesn't have EqpMonitor job
                    if (CimStringUtils.equals(StandardProperties.OM_AUTOMON_FLAG.getValue(),"1")){
                        log.info("OM_AUTOMON_FLAG is 1");
                        //check Lot type
                        //【step5】 -lot_lotType_Get
                        String lotTypeGetRetCode = lotMethod.lotTypeGet(objCommon, strWhatNextInqResult.getStrWhatNextAttributes().get(i).getLotID());
                        if (CimStringUtils.equals(BizConstant.SP_LOT_TYPE_EQUIPMENTMONITORLOT,lotTypeGetRetCode)
                                || CimStringUtils.equals(BizConstant.SP_LOT_TYPE_DUMMYLOT,lotTypeGetRetCode)){
                            log.info("lotType is Equipment Monitor or Dummy.");
                            //【step6】 - lot_eqpMonitorOperationLabel_Get
                            List<Infos.EqpMonitorLabelInfo> equipmentMonitorOperationLabelGetOut = lotMethod.lotEqpMonitorOperationLabelGet(objCommon, strWhatNextInqResult.getStrWhatNextAttributes().get(i).getLotID());
                            boolean bMonitorLabel = false;
                            int size = CimArrayUtils.getSize(equipmentMonitorOperationLabelGetOut);
                            for (int x = 0; x < size; x++) {
                                log.info("Loop through strEqpMonitorLabelInfoSeq");
                                if (CimStringUtils.equals(BizConstant.SP_EQPMONITOR_OPELABEL_MONITOR,equipmentMonitorOperationLabelGetOut.get(x).getOperationLabel())){
                                    log.info("Found Monitor label");
                                    bMonitorLabel = true;
                                    break;
                                }
                            }
                            if (CimBooleanUtils.isTrue(bMonitorLabel)){
                                log.info("bMonitorLabel is TRUE.");
                                //【step7】 - lot_eqpMonitorJob_Get
                                Infos.EqpMonitorJobLotInfo eqpMonitorJobLotInfoRetCode = lotMethod.lotEqpMonitorJobGet(objCommon, strWhatNextInqResult.getStrWhatNextAttributes().get(i).getLotID());
                                if (ObjectIdentifier.isEmptyWithValue(eqpMonitorJobLotInfoRetCode.getEqpMonitorJobID())){
                                    log.info("eqpMonitorJobID is not attached to lot");
                                    continue;
                                }
                                CimLot aPosLot = baseCoreFactory.getBO(CimLot.class, strWhatNextInqResult.getStrWhatNextAttributes().get(i).getLotID());
                                CimProcessOperation aPosPO = aPosLot.getProcessOperation();
                                Validations.check (CimObjectUtils.isEmpty(aPosPO), new OmCode(retCodeConfig.getNotFoundProcessOperation(), ObjectIdentifier.fetchValue(strWhatNextInqResult.getStrWhatNextAttributes().get(i).getLotID())));
                                String strOPNumber = aPosPO.getOperationNumber();
                                CimProcessFlowContext aPFX = aPosLot.getProcessFlowContext();
                                Validations.check (CimObjectUtils.isEmpty(aPFX), retCodeConfig.getNotFoundPfx());
                                String strEqpMonitorKey = aPFX.getEqpMonOperationKey(strOPNumber);
                                if (!CimStringUtils.equals(strEqpMonitorKey,
                                        eqpMonitorJobLotInfoRetCode.getMonitorOpeKey())){
                                    //The EqpMonitor job is for another Monitor process
                                    log.info("EqpMonitorKey is not same.");
                                    continue;
                                }else {
                                    //check OK
                                    log.info("EqpMonitorKey is same.");
                                }
                            }
                        }
                    }
                    // Search Port for ProcessLot
                    log.info("Search Port for ProcessLot");
                    int nAssignPortIdx = -1;
                    int lenAssignPort = CimArrayUtils.getSize(useAssignEmptyCassettePortSeq);
                    int lenPort = CimArrayUtils.getSize(portID);
                    for (int j = 0; j < lenPort; j++) {
                        Boolean bFoundPort = false;
                        for (int k = 0; k < lenAssignPort; k++) {
                            if (ObjectIdentifier.isEmptyWithValue(useAssignEmptyCassettePortSeq.get(k))){
                                continue;
                            }
                            if (ObjectIdentifier.equalsWithValue(useAssignEmptyCassettePortSeq.get(k), portID.get(j))){
                                bFoundPort = true;
                                break;
                            }
                        }
                        if (CimBooleanUtils.isFalse(bFoundPort)){
                            nAssignPortIdx = j;
                            break;
                        }
                    }
                    if (nAssignPortIdx < 0){
                        log.info("0 > nAssignPortIdx");
                        break;
                    }
                    //Check Category for Copper/Non Copper
                    log.info("Check Category for Copper/Non Copper");
                    //【step8】 - lot_CassetteCategory_CheckForContaminationControl
                    try {
                        lotMethod.lotCassetteCategoryCheckForContaminationControl(objCommon,
                                strWhatNextInqResult.getStrWhatNextAttributes().get(i).getLotID(),
                                strWhatNextInqResult.getStrWhatNextAttributes().get(i).getCassetteID(),
                                equipmentID,
                                portID.get(nAssignPortIdx));
                    } catch (ServiceException e) {
                        log.info("UnMatch CarrierCategory (Copper/NonCopper)   ..<<continue>>");
                        continue;
                    }

                    //Check Stocker which Lot belongs to, Available?
                    ObjectIdentifier checkID = new ObjectIdentifier();
                    if (CimStringUtils.equals(BizConstant.SP_TRANSSTATE_EQUIPMENTIN,strWhatNextInqResult.getStrWhatNextAttributes().get(i).getTransferStatus())
                            && !ObjectIdentifier.equalsWithValue(strWhatNextInqResult.getStrWhatNextAttributes().get(i).getEquipmentID(), equipmentID)){
                        checkID = equipmentID;
                        log.info("Delivery Process is [EQP to EQP]");
                    }else {
                        checkID = strWhatNextInqResult.getStrWhatNextAttributes().get(i).getStockerID();
                        log.info("Delivery Process is [EQP to Stocker]");
                    }
                    if (CimStringUtils.equals(BizConstant.SP_TRANSSTATE_EQUIPMENTOUT,strWhatNextInqResult.getStrWhatNextAttributes().get(i).getTransferStatus())
                            || CimStringUtils.equals(BizConstant.SP_TRANSSTATE_STATIONOUT,strWhatNextInqResult.getStrWhatNextAttributes().get(i).getTransferStatus())){
                        log.info("Cassette is EO or SO. No need to check machine availability.");
                    }else {
                        log.info("Call equipment_CheckAvail()   check EqpID or StockerID --->{}", ObjectIdentifier.fetchValue(checkID));
                        //【step9】 - equipment_CheckAvail
                        try {
                            equipmentMethod.equipmentCheckAvail(objCommon, checkID);
                        } catch (Exception e) {
                            log.error("RC_OK != equipment_CheckAvail()   ..<<continue>>");
                            continue;
                        }
                    }
                    // Check Scrap Wafer
                    List<ObjectIdentifier> cassetteIDs = new ArrayList<>();
                    cassetteIDs.add(strWhatNextInqResult.getStrWhatNextAttributes().get(i).getCassetteID());
                    log.info("call cassette_scrapWafer_SelectDR()");
                    //【step10】 - cassette_scrapWafer_SelectDR
                    List<Infos.LotWaferMap> lotWaferMaps = cassetteMethod.cassetteScrapWaferSelectDR(objCommon, cassetteIDs);
                    int nScrapCnt = CimArrayUtils.getSize(lotWaferMaps);
                    if (nScrapCnt > 0){
                        log.info("Cassette has ScrapWafer ..<<continue>>");
                        continue;
                    }
                    //【step11】 - cassette_DBInfo_GetDR__170
                    Infos.CassetteDBINfoGetDRInfo cassetteDBINfoGetDRInfoParam = new Infos.CassetteDBINfoGetDRInfo();
                    cassetteDBINfoGetDRInfoParam.setCassetteID(strWhatNextInqResult.getStrWhatNextAttributes().get(i).getCassetteID());
                    cassetteDBINfoGetDRInfoParam.setDurableOperationInfoFlag(false);
                    cassetteDBINfoGetDRInfoParam.setDurableWipOperationInfoFlag(false);
                    Outputs.CassetteDBInfoGetDROut cassetteDBInfoGetDROutRetCode = cassetteMethod.cassetteDBInfoGetDR(objCommon, cassetteDBINfoGetDRInfoParam);
                    int lotLen = 0;
                    int lotCnt = 0;
                    Boolean bCastDispatchDisableFlag = false;
                    lotLen = CimArrayUtils.getSize(cassetteDBInfoGetDROutRetCode.getCarrierDetailInfoInqResult().getCassetteStatusInfo().getStrContainedLotInfo());
                    for (lotCnt = 0;  lotCnt < lotLen; lotCnt++) {
                        if (CimBooleanUtils.isTrue(cassetteDBInfoGetDROutRetCode.getCarrierDetailInfoInqResult().getCassetteStatusInfo().getStrContainedLotInfo().get(lotCnt).isAutoDispatchDisableFlag())){
                            bCastDispatchDisableFlag = true;
                            break;
                        }
                    }
                    if (CimBooleanUtils.isTrue(bCastDispatchDisableFlag)){
                        log.info("Cassette Auto Dispatch Disable Flag == TRUE ..<<continue>>");
                        continue;
                    }
                    Infos.StartCassette startCassette = new Infos.StartCassette();
                    tmpStartCassette.add(startCassette);
                    startCassette.setCassetteID(strWhatNextInqResult.getStrWhatNextAttributes().get(i).getCassetteID());
                    //Set Temporary Base Recipe
                    log.info("Set Temporary Base Recipe");
                    if (processRunCount == 0){
                        log.info("processRunCount == 0");
                        if (CimBooleanUtils.isFalse(bTmpBaseRecipeFlag)){
                            //|Temporary Base Recipe
                            log.info("bTmpBaseRecipeFlag == FALSE");
                            tmpBaseLogicalRecipeID = strWhatNextInqResult.getStrWhatNextAttributes().get(i).getLogicalRecipeID();
                            tmpBaseMachineRecipeID = strWhatNextInqResult.getStrWhatNextAttributes().get(i).getMachineRecipeID();
                            bTmpBaseRecipeFlag = true;
                        }
                    }
                    //Set Port Info
                    log.info("Set Pot Info");
                    startCassette.setLoadSequenceNumber(loadSequenceNumber.get(nAssignPortIdx));
                    if (CimStringUtils.equals(BizConstant.SP_LOADPURPOSETYPE_PROCESSMONITORLOT,loadPurposeType.get(nAssignPortIdx))){
                        if (CimStringUtils.equals(BizConstant.SP_LOT_TYPE_PRODUCTIONMONITORLOT,strWhatNextInqResult.getStrWhatNextAttributes().get(i).getLotType())){
                            log.info("loadPurposeType == ProcessMonitorLot and LotType == ProcessMonitorLot");
                            startCassette.setLoadPurposeType(BizConstant.SP_LOADPURPOSETYPE_PROCESSMONITORLOT);
                        }else {
                            log.info("UnMatch LoadPurposeType  ..<<continue>>");
                            continue;
                        }
                    }else {
                        log.info("Set LoadPurposeType [ProcessLot]");
                        startCassette.setLoadPurposeType(BizConstant.SP_LOADPURPOSETYPE_PROCESSLOT);
                    }
                    startCassette.setLoadPortID(portID.get(nAssignPortIdx));
                    //Get PortGroupID
                    log.info("Get PortGroupID");
                    bFoundFlag = false;
                    for (int j = 0; j < nPortGroupLen; j++) {
                        nPortLen = CimArrayUtils.getSize(strPortGroupSeq.get(j).getStrPortID());
                        for (int k = 0; k < nPortLen; k++) {
                            if (ObjectIdentifier.equalsWithValue(tmpStartCassette.get(0).getLoadPortID(),
                                    strPortGroupSeq.get(j).getStrPortID().get(k).getPortID())){
                                portGroupID = strPortGroupSeq.get(j).getPortGroup();
                                bFoundFlag = true;
                                break;
                            }
                        }
                        if (CimBooleanUtils.isTrue(bFoundFlag)){
                            break;
                        }
                    }
                    //Get Contained Lot in Cassette
                    log.info("===== cassette_GetLotList() ==================");
                    //【step12】 - cassette_GetLotList
                    Infos.LotListInCassetteInfo cassetteLotListGetDROutRetCode = null;
                    try {
                        cassetteLotListGetDROutRetCode = cassetteMethod.cassetteGetLotList(objCommon, tmpStartCassette.get(0).getCassetteID());
                    }catch (ServiceException e) {
                        log.error("cassette_GetLotList() != RC_OK   ...<<continue>>");
                        continue;
                    }
                    int nLotLen = CimArrayUtils.getSize(cassetteLotListGetDROutRetCode.getLotIDList());
                    List<Infos.LotInCassette> lotInCassetteList = new ArrayList<>();
                    startCassette.setLotInCassetteList(lotInCassetteList);
                    for (int j = 0; j < nLotLen; j++) {
                        Infos.LotInCassette lotInCassette = new Infos.LotInCassette();
                        lotInCassetteList.add(j,lotInCassette);
                        lotInCassette.setRecipeParameterChangeType(BizConstant.SP_RPARM_CHANGETYPE_BYLOT);
                        lotInCassette.setMoveInFlag(true);
                        lotInCassette.setLotID(cassetteLotListGetDROutRetCode.getLotIDList().get(j));

                        CimLot aLot = baseCoreFactory.getBO(CimLot.class, tmpStartCassette.get(0).getLotInCassetteList().get(j).getLotID());
                        if(aLot != null) {
                            lotInCassette.setLotType(aLot.getLotType());
                            lotInCassette.setSubLotType(aLot.getSubLotType());
                        }

                        //【step13】- lot_productID_Get
                        Outputs.ObjLotProductIDGetOut lotProductIDGetRetCode = lotMethod.lotProductIDGet(objCommon,tmpStartCassette.get(0).getLotInCassetteList().get(j).getLotID());

                        lotInCassette.setProductID(lotProductIDGetRetCode.getProductID());

                        //Set Operation Info
                        log.info("Set Opertiion Info");
                        for (int m = 0; m < attrLen; m++) {
                            if (ObjectIdentifier.equalsWithValue(tmpStartCassette.get(0).getLotInCassetteList().get(j).getLotID(),
                                    strWhatNextInqResult.getStrWhatNextAttributes().get(m).getLotID())){
                                Infos.StartOperationInfo startOperationInfo = new Infos.StartOperationInfo();
                                lotInCassette.setStartOperationInfo(startOperationInfo);
                                startOperationInfo.setProcessFlowID(strWhatNextInqResult.getStrWhatNextAttributes().get(m).getRouteID());
                                startOperationInfo.setOperationID(strWhatNextInqResult.getStrWhatNextAttributes().get(m).getOperationID());
                                startOperationInfo.setOperationNumber(strWhatNextInqResult.getStrWhatNextAttributes().get(m).getOperationNumber());
                                break;
                            }
                        }
                        if (CimStringUtils.equals(BizConstant.SP_LOADPURPOSETYPE_PROCESSMONITORLOT,tmpStartCassette.get(0).getLoadPurposeType())){
                            //Get Lot Type
                            log.info("===== lot_type_Get() ==================");
                            //【step14】 - lot_type_Get
                            String lotTypeRetCode = null;
                            try {
                                lotTypeRetCode = lotMethod.lotTypeGet(objCommon, tmpStartCassette.get(0).getLotInCassetteList().get(j).getLotID());
                            } catch (ServiceException e) {
                                continue;
                            }

                            if (CimStringUtils.equals(BizConstant.SP_LOT_TYPE_PRODUCTIONMONITORLOT,lotTypeRetCode)){
                                log.info("LotType == [MonitorLot]");
                                lotInCassette.setMonitorLotFlag(true);
                            }else {
                                log.info("LotType != [MonitorLot]");
                                lotInCassette.setMonitorLotFlag(false);
                            }
                        }else {
                            lotInCassette.setMonitorLotFlag(false);
                        }
                        //Get Contained Wafer in Lot
                        log.info("===== lot_waferMap_Get() ==================");
                        //【step15】 - lot_waferMap_Get
                        List<Infos.LotWaferMap> lotWaferMapGetRetCode = null;
                        try {
                            lotWaferMapGetRetCode = lotMethod.lotWaferMapGet(objCommon, tmpStartCassette.get(0).getLotInCassetteList().get(j).getLotID());
                        } catch (ServiceException e) {
                            log.info("lot_waferMap_Get() != RC_OK   ..<<continue>>");
                            continue;
                        }

                        int nWafLen = CimArrayUtils.getSize(lotWaferMapGetRetCode);
                        List<Infos.LotWafer> lotWaferList = new ArrayList<>();
                        lotInCassette.setLotWaferList(lotWaferList);
                        for (int k = 0; k < nWafLen; k++) {
                            List<Infos.StartRecipeParameter> startRecipeParameterList = new ArrayList<>();
                            Infos.LotWafer lotWafer = new Infos.LotWafer();
                            lotWaferList.add(k,lotWafer);
                            lotWafer.setWaferID(lotWaferMapGetRetCode.get(k).getWaferID());
                            lotWafer.setSlotNumber(lotWaferMapGetRetCode.get(k).getSlotNumber());
                            lotWafer.setControlWaferFlag(lotWaferMapGetRetCode.get(k).isControlWaferFlag());
                            lotWafer.setStartRecipeParameterList(startRecipeParameterList);
                        }
                    }
                    log.info("Finished tmpStartCassette Info  ..<<break!! WhatNextInfo Loop>>");
                    break;
                }
                //It is finished if Loop of WhatNextInfo finishes turning twice.
                if (i >= attrLen -1 ){
                    nWIPLotLoopEndCnt++;
                    log.info("Turned WhatNextInfo Loop!!  Therefore Count It");
                    if (1 < nWIPLotLoopEndCnt){
                        log.info("Turned <<Twice>> WhatNextInfo Loop!!   Therefore ..<<break!! WhatNextInfo Loop>>");
                        break;
                    }
                }
                //Set Next Index of WIPLot loop
                startNo = i + 1;
                log.info("make strStartCassette Process  done.");
                if (CimArrayUtils.getSize(tmpStartCassette) == 0 || ObjectIdentifier.isEmptyWithValue(tmpStartCassette.get(0).getCassetteID())){
                    log.info("tmpStartCassette[0].cassetteID.identifier is null  ..<<continue>>");
                    continue;
                }
                //Set logicalRecipeID, machineRecipeID and Set not use operationStartFlag
                log.info("Set logicalRecipeID, machineRecipeID and Set not use operationStartFlag ");
                int nLotLen = CimArrayUtils.getSize(tmpStartCassette.get(0).getLotInCassetteList());
                for (int i1 = 0; i1 < nLotLen; i1++) {
                    Boolean bLotFindFlag = false;
                    for (int j = 0; j < attrLen; j++) {
                        if (ObjectIdentifier.equalsWithValue(strWhatNextInqResult.getStrWhatNextAttributes().get(j).getLotID(),
                                tmpStartCassette.get(0).getLotInCassetteList().get(i1).getLotID())){
                            //Set logicalRecipeID, machineRecipeID for strWhatNextInqResult
                            Infos.LotInCassette lotInCassette = tmpStartCassette.get(0).getLotInCassetteList().get(i1);
                            Infos.StartRecipe startRecipe = new Infos.StartRecipe();
                            lotInCassette.setStartRecipe(startRecipe);
                            startRecipe.setLogicalRecipeID(strWhatNextInqResult.getStrWhatNextAttributes().get(j).getLogicalRecipeID());
                            startRecipe.setMachineRecipeID(strWhatNextInqResult.getStrWhatNextAttributes().get(j).getMachineRecipeID());
                            startRecipe.setPhysicalRecipeID(strWhatNextInqResult.getStrWhatNextAttributes().get(j).getPhysicalRecipeID());
                            bLotFindFlag = true;
                        }
                    }
                    if (CimBooleanUtils.isFalse(bLotFindFlag)){
                        // Set operationStartFlag for strWhatNextInqResult
                        log.info("operationStartFlag <--- FALSE");
                        tmpStartCassette.get(0).getLotInCassetteList().get(i1).setMoveInFlag(false);
                    }
                }
                // Set operationStartFlag for Recipe
                log.info(" Set operationStartFlag for Recipe ");
                CimCassette aCassette = baseCoreFactory.getBO(CimCassette.class, tmpStartCassette.get(0).getCassetteID());
                multiLotType = aCassette.getMultiLotType();
                if (CimStringUtils.equals(BizConstant.SP_EQP_MULTIRECIPECAPABILITY_SINGLERECIPE,multiRecipeCapability)
                        && CimStringUtils.equals(BizConstant.SP_CAS_MULTILOTTYPE_MULTILOTMULTIRECIPE,multiLotType)){
                    log.info("multiRecipeCapability is [SingleRecipe] and multiLotType is [ML-MR]");
                    int nLotLen1 = CimArrayUtils.getSize(tmpStartCassette.get(0).getLotInCassetteList());
                    for (int j = 0; j < nLotLen1; j++) {
                        //Set OperationStartFlag for Recipe
                        //Set Temporary Base Recipe
                        if (processRunCount == 0){
                            // Temporary Base Recipe
                            log.info("Temporary Base Recipe");
                        }else {
                            // Final Base Recipe
                            log.info("Final Base Recipe");
                            tmpBaseLogicalRecipeID = baseLogicalRecipeID;
                            tmpBaseMachineRecipeID = baseMachineRecipeID;
                        }
                        ObjectIdentifier logicalRecipeID = tmpStartCassette.get(0).getLotInCassetteList().get(j).getStartRecipe() == null ? null : tmpStartCassette.get(0).getLotInCassetteList().get(j).getStartRecipe().getLogicalRecipeID();
                        ObjectIdentifier machineRecipeID = tmpStartCassette.get(0).getLotInCassetteList().get(j).getStartRecipe() == null ? null : tmpStartCassette.get(0).getLotInCassetteList().get(j).getStartRecipe().getMachineRecipeID();
                        if (ObjectIdentifier.equalsWithValue(tmpBaseLogicalRecipeID, logicalRecipeID)
                                && ObjectIdentifier.equalsWithValue(tmpBaseMachineRecipeID, machineRecipeID)){
                            log.info("tmpBaseLogicalRecipeID == logicalRecipeID && tmpBaseMachineRecipeID == machineRecipeID");
                        }else {
                            tmpStartCassette.get(0).getLotInCassetteList().get(j).setMoveInFlag(false);
                        }
                    }
                }
                //Set StartRecipeParameter
                log.info("Set StartRecipeParameter");
                for (int j = 0; j < nLotLen; j++) {
                    if (CimBooleanUtils.isFalse(tmpStartCassette.get(0).getLotInCassetteList().get(j).getMoveInFlag())) {
                        continue;
                    }
                    CimLogicalRecipe aLogicalRecipe = baseCoreFactory.getBO(CimLogicalRecipe.class, tmpStartCassette.get(0).getLotInCassetteList().get(j).getStartRecipe().getLogicalRecipeID());
                    CimMachineRecipe aMachineRecipe = null;
                    //Get subLotType
                    CimLot aLot = null;
                    String subLotType = "";
                    if (CimObjectUtils.isEmpty(tmpStartCassette.get(0).getLotInCassetteList().get(j).getSubLotType())){
                        aLot = baseCoreFactory.getBO(CimLot.class, tmpStartCassette.get(0).getLotInCassetteList().get(j).getLotID());
                        subLotType = aLot.getSubLotType();
                    }else {
                        subLotType = tmpStartCassette.get(0).getLotInCassetteList().get(j).getSubLotType();
                    }
                    Boolean skipFlag = false;
                    String exchangeType = BizConstant.SP_FPC_EXCHANGETYPE_STARTRESERVEINFO;
                    Outputs.ObjLotEffectiveFPCInfoGetOut lotEffectiveFPCInfoGetOutRetCode = lotMethod.lotEffectiveFPCInfoGet(objCommon, exchangeType, equipmentID, tmpStartCassette.get(0).getLotInCassetteList().get(j).getLotID());

                    if (CimBooleanUtils.isTrue(lotEffectiveFPCInfoGetOutRetCode.isMachineRecipeActionRequiredFlag())){
                        log.info("MachineRecipe is overwritten by FPC");
                    }else {
                        if (searchCondition == 1){
                            if (CimObjectUtils.isEmpty(aLot)){
                                aLot = baseCoreFactory.getBO(CimLot.class, tmpStartCassette.get(0).getLotInCassetteList().get(j).getLotID());
                            }
                            aMachineRecipe = aLogicalRecipe.findMachineRecipeFor(aLot, aMachine);
                        }else {
                            aMachineRecipe = aLogicalRecipe.findMachineRecipeForSubLotType(aMachine, subLotType);
                        }
                        List<RecipeDTO.RecipeParameter> recipeParameterSeq = aLogicalRecipe.findRecipeParametersForSubLotType(aMachine, aMachineRecipe, subLotType);
                        int rpmCnt = CimArrayUtils.getSize(recipeParameterSeq);
                        int nWafLen = CimArrayUtils.getSize(tmpStartCassette.get(0).getLotInCassetteList().get(j).getLotWaferList());
                        for (int k = 0; k < nWafLen; k++) {
                            if (CimObjectUtils.isEmpty(recipeParameterSeq)){
                                tmpStartCassette.get(0).getLotInCassetteList().get(j).getLotWaferList().get(k).setStartRecipeParameterList(new ArrayList<Infos.StartRecipeParameter>());
                            }else {
                                for (int l = 0; l < rpmCnt; l++) {
                                    List<Infos.StartRecipeParameter> startRecipeParameterList = tmpStartCassette.get(0).getLotInCassetteList().get(j).getLotWaferList().get(k).getStartRecipeParameterList();
                                    Infos.StartRecipeParameter startRecipeParameter = new Infos.StartRecipeParameter();
                                    startRecipeParameterList.add(l,startRecipeParameter);
                                    startRecipeParameter.setParameterName(recipeParameterSeq.get(l).getParameterName());//parameterName
                                    if (CimBooleanUtils.isTrue(recipeParameterSeq.get(l).getUseCurrentValueFlag())){ //useCurrentValueFlag
                                        startRecipeParameter.setParameterValue("");
                                    }else {
                                        startRecipeParameter.setParameterValue(recipeParameterSeq.get(l).getDefaultValue());//defaultValue
                                    }
                                    startRecipeParameter.setTargetValue(recipeParameterSeq.get(l).getDefaultValue());
                                    startRecipeParameter.setUseCurrentSettingValueFlag(recipeParameterSeq.get(l).getUseCurrentValueFlag());
                                }
                            }
                        }
                    }
                }
                //check strStartCassette Process
                log.info("check strStartCassette Process");
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
                /*                                                                       */
                /*-----------------------------------------------------------------------*/
                log.info("===== Check Process for Cassette ==========");
                long saveLoadSequenceNumber = CimNumberUtils.longValue(tmpStartCassette.get(0).getLoadSequenceNumber());
                tmpStartCassette.get(0).setLoadSequenceNumber(1L);

                //*===== for emptyCassetteCount =====*/
                if (CimBooleanUtils.isTrue(equipmentProcessBatchConditionRetCode.isCassetteExchangeFlag())
                        || CimBooleanUtils.isTrue(equipmentProcessBatchConditionRetCode.isMonitorCreationFlag())
                        || equipmentProcessBatchConditionRetCode.getMinBatchSize() > 1){
                    log.info("cassetteExchangeFlag == TRUE or monitorCreationFlag  == TRUE or minBatchSize > 1");
                    log.info("call cassette_CheckConditionForDelivery()");
                    String operation = BizConstant.SP_OPERATION_STARTRESERVATION;
                    //step17 - cassette_CheckConditionForDelivery
                    try{
                        cassetteMethod.cassetteCheckConditionForDelivery(objCommon, equipmentID, portGroupID, tmpStartCassette, operation);
                    }catch (ServiceException e) {
                        log.error("cassette_CheckConditionForDelivery() != RC_OK   ..<<continue>>");
                        continue;
                    }
                }else {
                    log.info("!cassetteExchangeFlag == TRUE or monitorCreationFlag  == TRUE or minBatchSize > 1");
                    log.info("call cassette_CheckConditionForOperation()");
                    //【step18】 - cassette_CheckConditionForOperation
                    String operation = BizConstant.SP_OPERATION_STARTRESERVATION;
                    try {
                        cassetteMethod.cassetteCheckConditionForOperation(objCommon, equipmentID, portGroupID, tmpStartCassette, operation);
                    }catch (ServiceException e) {
                        log.error("cassette_CheckConditionForOperation() != RC_OK   ..<<continue>>");
                        continue;
                    }
                }
                tmpStartCassette.get(0).setLoadSequenceNumber(saveLoadSequenceNumber);
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
                log.info("===== lot_CheckConditionForOperation() ==========");
                String operation = BizConstant.SP_OPERATION_CASSETTEDELIVERY;
                //【step19】 - lot_CheckConditionForOperation
                try {
                    lotMethod.lotCheckConditionForOperation(objCommon,
                            equipmentID,
                            portGroupID,
                            tmpStartCassette,
                            operation);
                } catch (ServiceException e) {
                    log.info("lot_CheckConditionForOperation() != RC_OK   ..<<continue>>");
                    continue;
                }

                /*-----------------------------------------------------------------------------*/
                /*                                                                             */
                /*   Check Equipment Port for Start Reservation                                */
                /*                                                                             */
                /*   The following conditions are checked by this object                       */
                /*                                                                             */
                /*   1. In-parm's portGroupID must not have controlJobID.                      */
                /*   2. All of ports' loadMode must be CIMFW_PortRsc_Input or _InputOutput.    */
                /*   3. All of port, which is registered as in-parm's portGroup, must be       */
                /*      _LoadAvail or _LoadReq when equipment is online.                       */
                /*      As exceptional case, if equipment's takeOutInTransferFlag is True,     */
                /*      _UnloadReq is also OK for start reservation when equipment is Online.  */
                /*   4. All of port, which is registered as in-parm's portGroup,               */
                /*      must not have loadedCassetteID.                                        */
                /*   5. strStartCassette[].loadPortID's portGroupID must be same               */
                /*      as in-parm's portGroupID.                                              */
                /*   6. strStartCassette[].loadPurposeType must be match as specified port's   */
                /*      loadPutposeType.                                                       */
                /*   7. strStartCassette[].loadSequenceNumber must be same as specified port's */
                /*      loadSequenceNumber.                                                    */
                /*                                                                             */
                /*-----------------------------------------------------------------------------*/
                log.info("===== equipment_portState_CheckForStartReservation() ==========");
                //【step20】 - equipment_portState_CheckForTakeOutIn
                try {
                    equipmentMethod.equipmentPortStateCheckForTakeOutIn(objCommon,
                            equipmentID,
                            portGroupID,
                            tmpStartCassette);
                } catch (ServiceException e) {
                    log.info("equipment_portState_CheckForTakeOutIn() != RC_OK   ..<<continue>>");
                    continue;
                }
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
                log.info("===== Check Process for Process Durable ==========");
                Boolean  durableRequiredFlag = false;
                log.info("call equipment_processDurableRequiredFlag_Get()");
                //【step21】 - equipment_processDurableRequiredFlag_Get
                try {
                    equipmentMethod.equipmentProcessDurableRequiredFlagGet(objCommon, equipmentID);
                } catch (ServiceException e){
                    if (Validations.isEquals(retCodeConfig.getEquipmentProcessDurableReticleRequired(), e.getCode())
                            || Validations.isEquals(retCodeConfig.getEquipmentProcessDurableFixtRequired(), e.getCode())){
                        log.info("rc == RC_EQP_PROCDRBL_RTCL_REQD || rc == RC_EQP_PROCDRBL_FIXT_REQD");
                        if (!CimStringUtils.equals(BizConstant.SP_LOADPURPOSETYPE_EMPTYCASSETTE,tmpStartCassette.get(0).getLoadPurposeType())){
                            log.info("tmpStartCassette[0].loadPurposeType != SP_LoadPurposeType_EmptyCassette");
                            int nLotLen2 = CimArrayUtils.getSize(tmpStartCassette.get(0).getLotInCassetteList());
                            for (int j = 0; j < nLotLen2; j++) {
                                if (CimBooleanUtils.isFalse(tmpStartCassette.get(0).getLotInCassetteList().get(j).getMoveInFlag())){
                                    log.info("tmpStartCassette[0].strLotInCassette[j].operationStartFlag == FALSE");
                                    continue;
                                }

                                // Check Process Durable Condition for OpeStart
                                log.info("call processDurable_CheckConditionForOpeStart()");
                                //【step22】 - processDurable_CheckConditionForOpeStart
                                Outputs.ObjProcessDurableCheckConditionForOperationStartOut durableCheckConditionForOperationStartOutRetCode =null;
                                try{
                                    durableCheckConditionForOperationStartOutRetCode = processMethod.processDurableCheckConditionForOpeStart(objCommon,
                                            equipmentID,
                                            tmpStartCassette.get(0).getLotInCassetteList().get(j).getStartRecipe().getLogicalRecipeID(),
                                            tmpStartCassette.get(0).getLotInCassetteList().get(j).getStartRecipe().getMachineRecipeID(),
                                            tmpStartCassette.get(0).getLotInCassetteList().get(j).getLotID());
                                }catch (ServiceException ex){
                                    log.error("processDurable_CheckConditionForOpeStart() != RC_OK");
                                    durableRequiredFlag = true;
                                    break;
                                }

                                //Set Available Reticles / Fixtures
                                tmpStartCassette.get(0).getLotInCassetteList().get(j).getStartRecipe().setStartReticleList(durableCheckConditionForOperationStartOutRetCode.getStartReticleList());
                                tmpStartCassette.get(0).getLotInCassetteList().get(j).getStartRecipe().setStartFixtureList(durableCheckConditionForOperationStartOutRetCode.getStartFixtureList());
                            }
                            if (CimBooleanUtils.isTrue(durableRequiredFlag)){
                                continue;
                            }
                        }
                    } else if (!Validations.isEquals(retCodeConfig.getEquipmentProcessDurableNotRequired(), e.getCode())){
                        log.info("rc != RC_EQP_PROCDRBL_NOT_REQD   ..<<continue>>");
                        continue;
                    }
                }
                /* *********************************************************************************/
                /*                                                                                */
                /*   Check multiRecipeCapability and multiLotType                                 */
                /*   and Decide finally to put it in StartCassette                                */
                /*                                                                                */
                /* *********************************************************************************/
                log.info("Check multiRecipeCapability and multiLotType");
                //----------------------------------------------------------------------
                //      Equipment's         Cassette's
                // multiRecipeCapability    multiLotType    Same Recipe Check
                // =====================================================================
                // M-Recipe                 SL-SR           FALSE
                //                          ML-SR           FALSE
                //                          ML-MR           FALSE
                // -----------------------  --------------------------------------------
                // S-Recipe                 SL-SR           TRUE
                //                          ML-SR           TRUE
                //                          ML-MR           TRUE
                // -----------------------  --------------------------------------------
                // Batch                    SL-SR           TRUE
                //                          ML-SR           TRUE
                //                          ML-MR           Error
                // -----------------------  --------------------------------------------
                Boolean bAddStartCassette = false;
                if (CimStringUtils.equals(BizConstant.SP_EQP_MULTIRECIPECAPABILITY_MULTIPLERECIPE,multiRecipeCapability)){
                    // M-Recipe: SL-SR, ML-SR, ML-MR
                    // Same Recipe Check : FALSE
                    log.info("M-Recipe: SL-SR, ML-SR, ML-MR");
                    log.info("Same Recipe Check : FALSE");
                    bAddStartCassette = true;
                }else if (CimStringUtils.equals(BizConstant.SP_EQP_MULTIRECIPECAPABILITY_BATCH,multiRecipeCapability)
                        && CimStringUtils.equals(BizConstant.SP_CAS_MULTILOTTYPE_MULTILOTMULTIRECIPE,multiLotType)){
                    // Batch and ML-MR
                    // Error
                    log.error("Batch and ML-MR: continue");
                    continue;
                }else {
                    // S-Recipe: SL-SR, ML-SR, ML-MR
                    // Batch   : SL-SR, ML-SR
                    // Same Recipe Check : TRUE
                    log.info("S-Recipe: SL-SR, ML-SR, ML-MR");
                    log.info("Batch   : SL-SR, ML-SR");
                    log.info("Same Recipe Check : TRUE");
                    if (processRunCount == 0){
                        log.info("processRunCount == 0");
                        bAddStartCassette = true;
                        /*------------------------------------------------------------*/
                        /*   Set Base Recipe (First operationStartFlag=TRUE Recipe)   */
                        /*------------------------------------------------------------*/
                        for (i = 0; i < tmpStartCassette.get(0).getLotInCassetteList().size(); i++) {
                            if (CimBooleanUtils.isTrue(tmpStartCassette.get(0).getLotInCassetteList().get(i).getMoveInFlag())){
                                baseLogicalRecipeID = tmpStartCassette.get(0).getLotInCassetteList().get(i).getStartRecipe().getLogicalRecipeID();
                                baseMachineRecipeID = tmpStartCassette.get(0).getLotInCassetteList().get(i).getStartRecipe().getMachineRecipeID();
                                break;
                            }
                        }
                        if (ObjectIdentifier.isEmptyWithValue(baseLogicalRecipeID)
                                || ObjectIdentifier.isEmptyWithValue(baseMachineRecipeID)){
                            log.info(" Can not Found Base Recipe!!");
                        }
                    }else {
                        ObjectIdentifier logicalRecipeID = new ObjectIdentifier();
                        ObjectIdentifier machineRecipeID = new ObjectIdentifier();
                        //Find Recipe (First operationStartFlag=TRUE Recipe)
                        for (i = 0; i < tmpStartCassette.get(0).getLotInCassetteList().size(); i++) {
                            if (CimBooleanUtils.isTrue(tmpStartCassette.get(0).getLotInCassetteList().get(i).getMoveInFlag())){
                                logicalRecipeID = tmpStartCassette.get(0).getLotInCassetteList().get(i).getStartRecipe().getLogicalRecipeID();
                                machineRecipeID = tmpStartCassette.get(0).getLotInCassetteList().get(i).getStartRecipe().getMachineRecipeID();
                                break;
                            }
                        }
                        if (ObjectIdentifier.isEmptyWithValue(logicalRecipeID)
                                || ObjectIdentifier.isEmptyWithValue(machineRecipeID)){
                            log.info(" Can not Found Base Recipe!!");
                        }
                        if (ObjectIdentifier.equalsWithValue(baseLogicalRecipeID, logicalRecipeID)
                                && ObjectIdentifier.equalsWithValue(baseMachineRecipeID, machineRecipeID)){
                            bAddStartCassette = true;
                        }else {
                            log.info("Deferent Recipe.  ...<<<continue>>>");
                            continue;
                        }
                    }
                }
                /* *********************************************************************************/
                /*                                                                                */
                /*   Add tmpStartCassette to StartCassette                                        */
                /*                                                                                */
                /* *********************************************************************************/
                log.info("Add tmpStartCassette to StartCassette");
                if (CimBooleanUtils.isTrue(bAddStartCassette)){
                    /* ******************************************************************************/
                    /*   Check MonitorCreationFlag                                                 */
                    /*   Only one time of the beginnings                                           */
                    /* ******************************************************************************/
                    if (CimBooleanUtils.isFalse(bAlreadyCheckMonitorCreationFlag)
                            && CimBooleanUtils.isTrue(equipmentProcessBatchConditionRetCode.isMonitorCreationFlag())){
                        log.info("===== Check MonitorCreationFlag ==========");
                        CimLogicalRecipe aLogicalRecipe = baseCoreFactory.getBO(CimLogicalRecipe.class, tmpBaseLogicalRecipeID);
                        CimProductSpecification aMonitorProduct = aLogicalRecipe.getMonitorProduct();
                        if (!CimObjectUtils.isEmpty(aMonitorProduct)){
                            //EmptyCassette is necessary!
                            log.info("===== Set EmptyCassette for MonitorCreation ==========");
                            /*------------------------------------------------*/
                            /*   Look for Port to assign, and EmptyCassette   */
                            /*------------------------------------------------*/
                            log.info("Look for Port to assign, and EmptyCassette");
                            //【step23】 - cassetteDelivery_SearchEmptyCassetteAssignPort
                            ObjectIdentifier dummyLotID = new ObjectIdentifier();
                            Outputs.ObjCassetteDeliverySearchEmptyCassetteAssignPortOut cassetteDeliverySearchEmptyCassetteAssignPortRetCode = null;
                            try{
                                cassetteDeliverySearchEmptyCassetteAssignPortRetCode = cassetteMethod.cassetteDeliverySearchEmptyCassetteAssignPort(objCommon,
                                        dummyLotID, strPortGroupSeq.get(0).getStrPortID(), cassetteListEmptyAvailablePickupOutRetCode.getStrFoundCassette(),
                                        useEmptyCassetteIDSeq, useAssignEmptyCassettePortSeq);
                            }catch (ServiceException e) {
                                log.error("NotFound!! Assign EmptyCassette or Assign Port!   set bWhileExitFlag = TRUE  ...<<<continue>>>");
                                bWhileExitFlag = true;
                                continue;
                            }
                            //Hold EmptyCasstte and Port to prevent duplication
                            log.info("Hold EmptyCasstte and Port to prevent duplication");
                            useAssignEmptyCassettePortSeq.add(nAssignEmptyCassetteCnt,cassetteDeliverySearchEmptyCassetteAssignPortRetCode.getStrFoundPort().getPortID());
                            useEmptyCassetteIDSeq.add(nAssignEmptyCassetteCnt,cassetteDeliverySearchEmptyCassetteAssignPortRetCode.getFoundEmptyCassetteID());
                            nAssignEmptyCassetteCnt++;
                            /*-------------------------------------------------------------------------*/
                            /*   Found!! Assign EmptyCassette. ---> Add information on startCassette   */
                            /*   Put it at the head of StartCassette surely!!                          */
                            /*-------------------------------------------------------------------------*/
                            log.info("Found!! Assign EmptyCassette. ---> Add information on startCassette");
                            strStartCassette.get(0).setLoadSequenceNumber(cassetteDeliverySearchEmptyCassetteAssignPortRetCode.getStrFoundPort().getLoadSequenceNoInPortGroup());
                            strStartCassette.get(0).setCassetteID(cassetteDeliverySearchEmptyCassetteAssignPortRetCode.getStrFoundPort().getCassetteID());
                            strStartCassette.get(0).setLoadPurposeType(BizConstant.SP_LOADPURPOSETYPE_EMPTYCASSETTE);
                            strStartCassette.get(0).setLoadPortID(cassetteDeliverySearchEmptyCassetteAssignPortRetCode.getStrFoundPort().getPortID());
                            nSetStartCassetteCnt ++;
                            if (Objects.equals(strStartCassette.get(0).getLoadSequenceNumber(), tmpStartCassette.get(0).getLoadSequenceNumber())){
                                int lenPortIDs = CimArrayUtils.getSize(portID);
                                if (1 < lenPortIDs){
                                    //Set next port info
                                    tmpStartCassette.get(0).setLoadSequenceNumber(loadSequenceNumber.get(1));
                                    tmpStartCassette.get(0).setLoadPortID(portID.get(1));
                                }
                            }
                        }
                        bAlreadyCheckMonitorCreationFlag = true;
                    }
                    /*------------------------------------------------------------------------------------------------------*/
                    /*   Selected PortID and CassetteID is stocked.                                                         */
                    /*   Originally this Sequence(useAssignEmptyCassettePortSeq, useEmptyCassetteIDSeq) exists for Empty.   */
                    /*   But, by D4200302, Cassette must assign also to AnyPort.                                            */
                    /*   Here, if it does not set to these Sequences, following problem arises                              */
                    /*                                               by cassetteDelivery_SearchEmptyCassetteAssignPort().   */
                    /*     Equipment is CassetteExchange Type.                                                              */
                    /*                                                                                                      */
                    /*     PortID  PortGrp  Purpose                                                                         */
                    /*     --------------------------                                                                       */
                    /*       P1       A       Any                                                                           */
                    /*       P2       A       Any                                                                           */
                    /*                                                                                                      */
                    /*    First, P1 is assigned with ProcessLot.                                                            */
                    /*    Next, Search EmpPort by cassetteDelivery_SearchEmptyCassetteAssignPort().                         */
                    /*    But Function chooses P1.                                                                          */
                    /*    Because P1 is not set to useEmptyCassetteIDSeq.                                                   */
                    /*                                                                                                      */
                    /*    It is not necessary to put CassetteID into useAssignEmptyCassettePortSeq.                         */
                    /*    But sequence counter(nAssignEmptyCassetteCnt) becomes mismatching.                                */
                    /*    So, CassetteID is set also to useEmptyCassetteIDSeq.                                              */
                    /*------------------------------------------------------------------------------------------------------*/
                    useAssignEmptyCassettePortSeq.add(nAssignEmptyCassetteCnt,tmpStartCassette.get(0).getLoadPortID());
                    useEmptyCassetteIDSeq.add(nAssignEmptyCassetteCnt,tmpStartCassette.get(0).getCassetteID());
                    nAssignEmptyCassetteCnt++;
                    /* ******************************************************************************/
                    /*   Set EmptyCassette if it is necessary                                      */
                    /* ******************************************************************************/
                    if (CimBooleanUtils.isTrue(equipmentProcessBatchConditionRetCode.isCassetteExchangeFlag())){
                        log.info("===== Set EmptyCassette for CassetteExchange ==========");
                        /*------------------------------------------------------*/
                        /*   Look for the first Lot when OpeStartFlag is TRUE   */
                        /*------------------------------------------------------*/
                        log.info("Look for the first Lot when OpeStartFlag is TRUE");
                        ObjectIdentifier targetLotID = new ObjectIdentifier();
                        for (i = 0; i < tmpStartCassette.get(0).getLotInCassetteList().size(); i++) {
                            if (CimBooleanUtils.isTrue(tmpStartCassette.get(0).getLotInCassetteList().get(i).getMoveInFlag())){
                                targetLotID = tmpStartCassette.get(0).getLotInCassetteList().get(i).getLotID();
                                break;
                            }
                        }
                        if (ObjectIdentifier.isEmptyWithValue(targetLotID)){
                            continue;
                        }
                        /*------------------------------------------------*/
                        /*   Look for Port to assign, and EmptyCassette   */
                        /*------------------------------------------------*/
                        log.info("Look for Port to assign, and EmptyCassette ");
                        //【step24】 - cassetteDelivery_SearchEmptyCassetteAssignPort
                        Outputs.ObjCassetteDeliverySearchEmptyCassetteAssignPortOut cassetteDeliverySearchEmptyCassetteAssignPortOutRetCode = null;
                        try{
                            cassetteDeliverySearchEmptyCassetteAssignPortOutRetCode = cassetteMethod.cassetteDeliverySearchEmptyCassetteAssignPort(objCommon, targetLotID, strPortGroupSeq.get(0).getStrPortID(),
                                    cassetteListEmptyAvailablePickupOutRetCode.getStrFoundCassette(), useEmptyCassetteIDSeq, useAssignEmptyCassettePortSeq);
                        }catch (ServiceException e) {
                            log.error("NotFound!! Assign EmptyCassette or Assign Port!   set bWhileExitFlag = TRUE  ...<<<continue>>>");
                            bWhileExitFlag = true;
                            continue;
                        }
                        // Hold EmptyCasstte and Port to prevent duplication
                        log.info("Hold EmptyCasstte and Port to prevent duplication");
                        useAssignEmptyCassettePortSeq.add(nAssignEmptyCassetteCnt,cassetteDeliverySearchEmptyCassetteAssignPortOutRetCode.getStrFoundPort().getPortID());
                        useEmptyCassetteIDSeq.add(nAssignEmptyCassetteCnt,cassetteDeliverySearchEmptyCassetteAssignPortOutRetCode.getFoundEmptyCassetteID());
                        nAssignEmptyCassetteCnt++;
                        /*-------------------------------------------------------------------------*/
                        /*   Found!! Assign EmptyCassette. ---> Add information on startCassette   */
                        /*   Put it at the head of StartCassette surely!!                          */
                        /*-------------------------------------------------------------------------*/
                        log.info("Found!! Assign EmptyCassette. ---> Add information on startCassette");
                        int lenTmp = CimArrayUtils.getSize(tmpStartCassette);
                        Infos.StartCassette startCassette = new Infos.StartCassette();
                        tmpStartCassette.add(startCassette);
                        startCassette.setLoadSequenceNumber(cassetteDeliverySearchEmptyCassetteAssignPortOutRetCode.getStrFoundPort().getLoadSequenceNoInPortGroup());
                        startCassette.setCassetteID(cassetteDeliverySearchEmptyCassetteAssignPortOutRetCode.getStrFoundPort().getCassetteID());
                        startCassette.setLoadPurposeType(BizConstant.SP_LOADPURPOSETYPE_EMPTYCASSETTE);
                        startCassette.setLoadPortID(cassetteDeliverySearchEmptyCassetteAssignPortOutRetCode.getStrFoundPort().getPortID());
                    }
                    //Add tmpStartCassette to StartCassette
                    log.info("===== Add tmpStartCassette to StartCassette ==========");
                    for (int j = 0; j < CimArrayUtils.getSize(tmpStartCassette); j++) {
                        strStartCassette.add(nSetStartCassetteCnt,tmpStartCassette.get(j));
                        nSetStartCassetteCnt++;
                    }
                    processRunCount++;
                }
            }
            //If selected Carrier does not fulfill MinBatchSize, they are omitted from candidate.
            if (CimBooleanUtils.isTrue(bCheckMinBatchSize) && processRunCount > 0 && processRunCount < equipmentProcessBatchConditionRetCode.getMinBatchSize()){
                for (int m = 0; m < nSetStartCassetteCnt; m++) {
                    omitCassetteSeq.add(strStartCassette.get(m).getCassetteID());
                }
            }else {
                break;
            }
        }
        // Final Check
        //Check processRunCount
        Validations.check (processRunCount == 0, retCodeConfigEx.getNotFoundFilledCast());
        //Check Mininum Wafer Count
        int nTotalWaferCount = 0;
        for (int i = 0; i < nSetStartCassetteCnt; i++) {
            int lenLot = CimArrayUtils.getSize(strStartCassette.get(i).getLotInCassetteList());
            for (int j = 0; j < lenLot; j++) {
                if (CimBooleanUtils.isTrue(strStartCassette.get(i).getLotInCassetteList().get(j).getMoveInFlag())){
                    nTotalWaferCount += CimArrayUtils.getSize(strStartCassette.get(i).getLotInCassetteList().get(j).getLotWaferList());
                }
            }
        }
        Validations.check (equipmentProcessBatchConditionRetCode.getMinWaferSize() > nTotalWaferCount, retCodeConfig.getInvalidInputWaferCount());
        if (ObjectIdentifier.isNotEmptyWithValue(flowBatchCheckConditionForCassetteDeliveryRetCode.getFlowBatchID())){
            //processRunCount must be the same as FlowBatching Lots Count
            int lenFlowBatchLots = CimArrayUtils.getSize(flowBatchCheckConditionForCassetteDeliveryRetCode.getStrContainedLotsInFlowBatch());
            List<ObjectIdentifier> cassetteIDSeq = new ArrayList<>();
            int nFlowBatchCasIdx = 0;
            for (int i = 0; i < lenFlowBatchLots; i++) {
                Boolean bFound = false;
                int lenCas = lenFlowBatchLots;
                for (int j = 0; j < lenCas; j++) {
                    if (ObjectIdentifier.equalsWithValue(cassetteIDSeq.get(j), flowBatchCheckConditionForCassetteDeliveryRetCode.getStrContainedLotsInFlowBatch().get(i).getCassetteID())){
                        bFound = true;
                        break;
                    }
                }
                if (CimBooleanUtils.isFalse(bFound)){
                    cassetteIDSeq.add(nFlowBatchCasIdx,flowBatchCheckConditionForCassetteDeliveryRetCode.getStrContainedLotsInFlowBatch().get(i).getCassetteID());
                    nFlowBatchCasIdx++;
                }
            }
            Validations.check (processRunCount != nFlowBatchCasIdx, retCodeConfigEx.getNotSelectAllFlowBatchLots());
        }
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
        //【step25】 - equipment_lot_CheckFlowBatchConditionForOpeStart__090
        Inputs.ObjEquipmentLotCheckFlowBatchConditionForOperationStartIn operationStartIn=new Inputs.ObjEquipmentLotCheckFlowBatchConditionForOperationStartIn();
        operationStartIn.setEquipmentID(equipmentID);
        operationStartIn.setPortGroupID(portGroupID);
        operationStartIn.setStartCassetteList(strStartCassette);
        ObjectIdentifier checkFlowBatchConditionForOperationStartOutRetCode = equipmentMethod.equipmentLotCheckFlowBatchConditionForOpeStart(objCommon, operationStartIn);
        long tmpFPCAdoptFlag = StandardProperties.OM_DOC_ENABLE_FLAG.getLongValue();
        if (1 == tmpFPCAdoptFlag){
            log.info("FPC Adopt Flag is ON. Now apply FPCInfo.");
            //【step26】 - FPCStartCassetteInfo_Exchange
            List<Infos.StartCassette> exchangeFPCStartCassetteInfo = fpcMethod.fpcStartCassetteInfoExchange(objCommon,
                    BizConstant.SP_FPC_EXCHANGETYPE_STARTRESERVEINFO,
                    equipmentID,
                    strStartCassette);

            strStartCassette = exchangeFPCStartCassetteInfo;
        }else {
            log.info("FPC Adopt Flag is OFF.");
        }
        //Set Return Struct
        return strStartCassette;
    }

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/7/23                               Neyo                create file
     *
     * @author Neyo
     * @since 2019/7/23 9:52
     * Copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Override
    public List<Infos.StartCassette> whatNextLotListToStartCassetteForDeliveryForInternalBufferReq(Infos.ObjCommon objCommonIn, ObjectIdentifier equipmentID, Outputs.EquipmentTargetPortPickupOut strEquipmentTargetPortPickupOut, Results.WhatNextLotListResult strWhatNextLotListForInternalBufferInqResult, boolean bEqpInternalBufferInfo, List<Infos.EqpInternalBufferInfo> strEqpInternalBufferInfoSeq) {

        int searchCondition = 0;
        int i,j,k;
        String searchConditionVar = StandardProperties.OM_CONSTRAINT_CHK_WITH_SEARCH.getValue();
        if (CimStringUtils.length(searchConditionVar) > 0){
            searchCondition = Integer.parseInt(searchConditionVar);
        }
        /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-*/
        /*                                                                                                            */
        /*                                                                                                            */
        /*                                                                                                            */
        /*   Check Condition and Get Information                                                                      */
        /*                                                                                                            */
        /*                                                                                                            */
        /*                                                                                                            */
        /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-*/
        /*----------------------------*/
        /*   Check Target Port Info   */
        /*----------------------------*/
        Validations.check (!CimStringUtils.equals(BizConstant.SP_PORTRSC_PORTSTATE_LOADREQ,strEquipmentTargetPortPickupOut.getTargetPortType()), retCodeConfig.getNotFoundTargetPort());
        int nCanBeUsedPortCount = CimArrayUtils.getSize(strEquipmentTargetPortPickupOut.getEqpTargetPortInfo().getPortGroups().get(0).getStrPortID());
        /*---------------------------------------------*/
        /*   Get Equipment's Process Batch Condition   */
        /*---------------------------------------------*/
        log.info("Get Equipment's Process Batch Condition");
        //【step1】 - equipment_processBatchCondition_Get
        Outputs.ObjEquipmentProcessBatchConditionGetOut strEquipmentProcessBatchConditionGetOut = equipmentMethod.equipmentProcessBatchConditionGet(objCommonIn, equipmentID);
        /*----------------------------------*/
        /*   Get Eqp Internal Buffer Info   */
        /*----------------------------------*/
        log.info("Get Eqp Internal Buffer Info");
        List<Infos.EqpInternalBufferInfo> strEquipmentInternalBufferInfoGetOut = null;
        if (CimBooleanUtils.isFalse(bEqpInternalBufferInfo)){
            //【step2】 - equipment_internalBufferInfo_Get
            strEquipmentInternalBufferInfoGetOut = equipmentMethod.equipmentInternalBufferInfoGet(objCommonIn, equipmentID);
        }else {
            log.info("TRUE == bEqpInternalBufferInfo");
            strEquipmentInternalBufferInfoGetOut = strEqpInternalBufferInfoSeq;
        }
        /*------------------------------------*/
        /*   Get Internal Buffer Free Shelf   */
        /*------------------------------------*/
        log.info("Get Internal Buffer Free Shelf");
        //【step3】 - equipment_shelfSpaceForInternalBuffer_Get
        Infos.EquipmentShelfSpaceForInternalBufferGet equipmentShelfSpaceForInternalBufferGetRetCode = equipmentMethod.equipmentShelfSpaceForInternalBufferGet(objCommonIn, equipmentID, true, strEquipmentInternalBufferInfoGetOut);
        /* *********************************************************************/
        /*                                                                    */
        /*   Get Empty Cassette                                               */
        /*                                                                    */
        /* *********************************************************************/
        log.info("Get Empty Cassette");
        Outputs.ObjCassetteListEmptyAvailablePickUpOut cassetteListEmptyAvailablePickupOutRetCode = null;
        Long nShelfSpaceOfEmptyCassette = 0L;
        int nAssignEmptyCassetteCnt = 0;
        List<ObjectIdentifier> useEmptyCassetteIDSeq = new ArrayList<>();
        List<ObjectIdentifier> useAssignEmptyCassettePortSeq  =  new ArrayList<>();
        if (CimBooleanUtils.isTrue(strEquipmentProcessBatchConditionGetOut.isCassetteExchangeFlag())
                || CimBooleanUtils.isTrue(strEquipmentProcessBatchConditionGetOut.isMonitorCreationFlag())){
            log.info("EmptyCassette is necessary");
            /*--------------------------------------------------------------------*/
            /*   EmptyCassette is necessary at This point!!                       */
            /*   But, make it an ERROR because ShelfSpace of EmptyCassette is 0   */
            /*--------------------------------------------------------------------*/
            Validations.check (0 == equipmentShelfSpaceForInternalBufferGetRetCode.getEmptyCassetteSpace(), retCodeConfig.getNotSpaceEqpSelf());
            log.info("cassette_ListGetDR__170()");
            Inputs.ObjCassetteListGetDRIn listGetDRIn = new Inputs.ObjCassetteListGetDRIn(new Params.CarrierListInqParams());
            listGetDRIn.setEmptyFlag(true);
            listGetDRIn.setCassetteStatus(BizConstant.CIMFW_DURABLE_AVAILABLE);
            listGetDRIn.setMaxRetrieveCount(-1);
            listGetDRIn.setSorterJobCreationCheckFlag(false);
            ObjectIdentifier dummyID = ObjectIdentifier.buildWithValue("");
            listGetDRIn.setBankID(dummyID);
            listGetDRIn.setDurablesSubStatus(dummyID);
            listGetDRIn.setFlowStatus("");
            //【note】: add searchCondition default by neyo
            listGetDRIn.setSearchCondition(new SearchCondition());
            //【step4】 - cassette_ListGetDR__170
            Page<Infos.FoundCassette> carrierListInq170ResultRetCode = cassetteMethod.cassetteListGetDR170(objCommonIn, listGetDRIn);
            List<Infos.FoundCassette> strFoundCassette = carrierListInq170ResultRetCode.getContent();
            /*-----------------------------------*/
            /*   Pick Up Target Empty Cassette   */
            /*-----------------------------------*/
            log.info("cassetteList_emptyAvailable_Pickup()");
            log.info("cassetteList_emptyAvailable_Pickup()");
            //【step5】 - cassetteList_emptyAvailable_Pickup
            cassetteListEmptyAvailablePickupOutRetCode = cassetteMethod.cassetteListEmptyAvailablePickup(objCommonIn,strFoundCassette);
            nShelfSpaceOfEmptyCassette = equipmentShelfSpaceForInternalBufferGetRetCode.getEmptyCassetteSpace();
        }else {
            log.info("EmptyCassette is unnecessary");
        }
        /*-------------------------------------------*/
        /*   Get Equipment's MultiRecipeCapability   */
        /*-------------------------------------------*/
        log.info("Get Equipment's MultiRecipeCapability");
        CimMachine aMachine = baseCoreFactory.getBO(CimMachine.class, equipmentID);
        Validations.check (CimObjectUtils.isEmpty(aMachine),  retCodeConfig.getNotFoundEqp());
        String multiRecipeCapability = aMachine.getMultipleRecipeCapability();
        /*------------------------------------------*/
        /*   Get Check Equipment's ProcessRunSize   */
        /*------------------------------------------*/
        log.info("Get Check Equipment's ProcessRunSize");
        int processRunCount = 0;
        Long processRunSizeMaximum = Long.valueOf(strWhatNextLotListForInternalBufferInqResult.getProcessRunSizeMaximum());
        if (processRunSizeMaximum > equipmentShelfSpaceForInternalBufferGetRetCode.getProcessLotSpace()){
            log.info("processRunSizeMaximum > strEquipment_shelfSpaceForInternalBuffer_Get_out.processLotSpace");
            processRunSizeMaximum = equipmentShelfSpaceForInternalBufferGetRetCode.getProcessLotSpace();
        }
        Validations.check (0 == processRunSizeMaximum, retCodeConfig.getNotSpaceEqpSelf(),BizConstant.SP_LOADPURPOSETYPE_PROCESSLOT);
        int attrLen = CimArrayUtils.getSize(strWhatNextLotListForInternalBufferInqResult.getStrWhatNextAttributes());
        Validations.check (0 == attrLen, retCodeConfigEx.getNotFoundFilledCast());
        /* *********************************************************************/
        /*                                                                    */
        /*   Get and Check FlowBatch Info                                     */
        /*                                                                    */
        /* *********************************************************************/
        log.info("Get and Check FlowBatch Info");
        log.info("call flowBatch_CheckConditionForCassetteDelivery()");
        Results.WhatNextLotListResult strWhatNextInqResult = strWhatNextLotListForInternalBufferInqResult;
        Outputs.ObjFlowBatchCheckConditionForCassetteDeliveryOut flowBatchCheckConditionForCassetteDeliveryRetCode = flowBatchMethod.flowBatchCheckConditionForCassetteDelivery(objCommonIn,equipmentID,strWhatNextInqResult);
        //【step6】 - flowBatch_CheckConditionForCassetteDelivery

        /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-*/
        /*                                                                                                            */
        /*                                                                                                            */
        /*                                                                                                            */
        /*   Make strStartCassette Process                                                                            */
        /*                                                                                                            */
        /*                                                                                                            */
        /*                                                                                                            */
        /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-*/
        log.info("make strStartCassette Process ");
        List<Infos.StartCassette> strStartCassette = new ArrayList<>();
        boolean bCheckMinBatchSize = false;
        String checkMinBatchSize = StandardProperties.OM_DELIVERY_CHK_MINIMUM_BATCH_SIZE_ENABLE.getValue();
        if (CimStringUtils.equals(checkMinBatchSize,"1") && 1 < strEquipmentProcessBatchConditionGetOut.getMinBatchSize()){
            log.info("bCheckMinBatchSize is 1");
            bCheckMinBatchSize = true;
        }
        Long nEmptyShelfCnt = nShelfSpaceOfEmptyCassette;
        boolean bShortOfEmptyCassette = false;
        List<ObjectIdentifier> omiCassetteSeq = new ArrayList<>();
        while (true){
            // MinBatchSize support loop.
            // Reset variable.
            processRunCount = 0;
            nAssignEmptyCassetteCnt = 0;
            nShelfSpaceOfEmptyCassette = nEmptyShelfCnt;
            String multiLotType = null;
            int startNo = 0;
            Boolean bWhileExitFlag = false;
            ObjectIdentifier baseLogicalRecipeID = new ObjectIdentifier();
            ObjectIdentifier baseMachineRecipeID = new ObjectIdentifier();
            ObjectIdentifier tmpBaseLogicalRecipeID = new ObjectIdentifier();
            ObjectIdentifier tmpBaseMachineRecipeID = new ObjectIdentifier();

            Boolean bAlreadyCheckMonitorCreationFlag = false;
            bShortOfEmptyCassette = false;
            while (processRunCount < processRunSizeMaximum){
                log.info("while ( processRunCount < processRunSizeMaximum ) --------------------------");
                log.info("processRunCount = {}",processRunCount);
                if (CimBooleanUtils.isTrue(bWhileExitFlag)){
                    log.info("bWhileExitFlag == TRUE    break!!");
                    break;
                }
                Boolean bTmpBaseRecipeFlag = false;
                List<Infos.StartCassette> tmpStartCassette = new ArrayList<>();
                /* *********************************************************************************/
                /*                                                                                */
                /*   Set Cassette Info                                                            */
                /*                                                                                */
                /* *********************************************************************************/
                log.info("Set Cassette Info");
                for (i = startNo; i < attrLen; i++) {
                    /*-----------------------------*/
                    /*   Omit CassetteID is NULL   */
                    /*-----------------------------*/
                    if (ObjectIdentifier.isEmptyWithValue(strWhatNextLotListForInternalBufferInqResult.getStrWhatNextAttributes().get(i).getCassetteID())){
                        log.info("cassetteID is NULL  ...<<<continue>>>");
                        continue;
                    }
                    Boolean bOmitCassette = false;
                    int lenOmitCst = CimArrayUtils.getSize(omiCassetteSeq);
                    for (int m = 0; m < lenOmitCst; m++) {
                        if (ObjectIdentifier.equalsWithValue(omiCassetteSeq.get(m),
                                strWhatNextLotListForInternalBufferInqResult.getStrWhatNextAttributes().get(i).getCassetteID())){
                            bOmitCassette = true;
                            break;
                        }
                    }
                    if (CimBooleanUtils.isTrue(bOmitCassette)){
                        log.info("Omit CassetteID   ..<<continue>>");
                        continue;
                    }
                    /*------------------------------------------*/
                    /*   Omit already saved strStartCassette    */
                    /*------------------------------------------*/
                    Boolean bFoundFlag = false;
                    for (j = 0; j < processRunCount; j++) {
                        if (ObjectIdentifier.equalsWithValue(strWhatNextLotListForInternalBufferInqResult.getStrWhatNextAttributes().get(i).getCassetteID(),
                                strStartCassette.get(j).getCassetteID())){
                            log.info("strWhatNextAttributes[i].cassetteID == strStartCassette[j].cassetteID  ...<<<continue>>>");
                            bFoundFlag = true;
                            break;
                        }
                    }
                    if (CimBooleanUtils.isTrue(bFoundFlag)){
                        log.info("already saved strStartCassette  ...<<<continue>>>");
                        continue;
                    }
                    if (CimArrayUtils.getSize(tmpStartCassette) != 0){
                        if (!ObjectIdentifier.equalsWithValue(strWhatNextLotListForInternalBufferInqResult.getStrWhatNextAttributes().get(i).getCassetteID(),
                                tmpStartCassette.get(0).getCassetteID())){
                            startNo = i;
                            log.info("Next for-loop StartNo.= {}",startNo);
                            break;
                        }else {
                            log.info("strWhatNextAttributes[i].cassetteID == tmpStartCassette[0].cassetteID  ...<<<continue>>>");
                            continue;
                        }
                    }
                    /*---------------------------------------------------------------------*/
                    /*   Omit Lot which is not FlowBatchingLots in the case of FlowBatch   */
                    /*---------------------------------------------------------------------*/
                    if (ObjectIdentifier.isNotEmptyWithValue(flowBatchCheckConditionForCassetteDeliveryRetCode.getFlowBatchID())){
                        log.info("Omit Lot which is not FlowBatchingLots in the case of FlowBatch");
                        Boolean bFound = false;
                        int lenFlowBatchLots = CimArrayUtils.getSize(flowBatchCheckConditionForCassetteDeliveryRetCode.getStrContainedLotsInFlowBatch());
                        for (j = 0; j < lenFlowBatchLots; j++) {
                            if (ObjectIdentifier.equalsWithValue(strWhatNextLotListForInternalBufferInqResult.getStrWhatNextAttributes().get(i).getLotID(),
                                    flowBatchCheckConditionForCassetteDeliveryRetCode.getStrContainedLotsInFlowBatch().get(j).getLotID())){
                                bFound = true;
                                break;
                            }
                        }
                        if (CimBooleanUtils.isFalse(bFound)){
                            log.info("LotID is not FlowBatchingLots   ..<<continue>>");
                            continue;
                        }
                        log.info("Lot is FlowBatching Lot!!");
                    }
                    //----------------------------------------------------------------------------------------
                    //   Omit lot if "Monitor" label is put on lot's process and the lot doesn't have EqpMonitor job
                    //----------------------------------------------------------------------------------------
                    String environmentValue = StandardProperties.OM_AUTOMON_FLAG.getValue();
                    if (CimStringUtils.equals(environmentValue,"1")){
                        log.info("OM_AUTOMON_FLAG is 1");
                        //check Lot type
                        //【step7】 - lot_lotType_Get
                        String lotTypeGetRetCode = lotMethod.lotTypeGet(objCommonIn, strWhatNextLotListForInternalBufferInqResult.getStrWhatNextAttributes().get(i).getLotID());
                        if (CimStringUtils.equals(BizConstant.SP_LOT_TYPE_EQUIPMENTMONITORLOT,lotTypeGetRetCode)
                                || CimStringUtils.equals(BizConstant.SP_LOT_TYPE_DUMMYLOT,lotTypeGetRetCode)){
                            log.info("lotType is Equipment Monitor or Dummy.");
                            //【step8】 - lot_eqpMonitorOperationLabel_Get
                            List<Infos.EqpMonitorLabelInfo> equipmentMonitorOperationLabelGetOut = lotMethod.lotEqpMonitorOperationLabelGet(objCommonIn, strWhatNextLotListForInternalBufferInqResult.getStrWhatNextAttributes().get(i).getLotID());
                            boolean bMonitorLabel = false;
                            int size = CimArrayUtils.getSize(equipmentMonitorOperationLabelGetOut);
                            for (int x = 0; x < size; x++) {
                                log.info("Loop through strEqpMonitorLabelInfoSeq");
                                if (CimStringUtils.equals(BizConstant.SP_EQPMONITOR_OPELABEL_MONITOR,equipmentMonitorOperationLabelGetOut.get(x).getOperationLabel())){
                                    log.info("Found Monitor label");
                                    bMonitorLabel = true;
                                    break;
                                }
                            }
                            if (CimBooleanUtils.isTrue(bMonitorLabel)){
                                log.info("bMonitorLabel is TRUE.");
                                //【step9】 - lot_eqpMonitorJob_Get
                                Infos.EqpMonitorJobLotInfo eqpMonitorJobLotInfoRetCode = lotMethod.lotEqpMonitorJobGet(objCommonIn, strWhatNextInqResult.getStrWhatNextAttributes().get(i).getLotID());
                                if (ObjectIdentifier.isEmptyWithValue(eqpMonitorJobLotInfoRetCode.getEqpMonitorJobID())){
                                    log.info("eqpMonitorJobID is not attached to lot");
                                    continue;
                                }
                                CimLot aPosLot = baseCoreFactory.getBO(CimLot.class, strWhatNextLotListForInternalBufferInqResult.getStrWhatNextAttributes().get(i).getLotID());
                                Validations.check (CimObjectUtils.isEmpty(aPosLot), retCodeConfig.getNotFoundLot());
                                CimProcessOperation aPosPO =aPosLot.getProcessOperation();
                                Validations.check (CimObjectUtils.isEmpty(aPosPO), new OmCode(retCodeConfig.getNotFoundProcessOperation(), ObjectIdentifier.fetchValue(strWhatNextLotListForInternalBufferInqResult.getStrWhatNextAttributes().get(i).getLotID())));
                                String strOPNumber = aPosPO.getOperationNumber();
                                CimProcessFlowContext aPFX = aPosLot.getProcessFlowContext();
                                Validations.check (CimObjectUtils.isEmpty(aPFX), retCodeConfig.getNotFoundPfx());
                                String strEqpMonitorKey =aPFX.getEqpMonOperationKey(strOPNumber);
                                if (!CimStringUtils.equals(strEqpMonitorKey,
                                        eqpMonitorJobLotInfoRetCode.getMonitorOpeKey())){
                                    //The EqpMonitor job is for another Monitor process
                                    log.info("EqpMonitorKey is not same.");
                                    continue;
                                }else {
                                    //check OK
                                    log.info("EqpMonitorKey is same.");
                                }
                            }
                        }
                    }
                    /*----------------------------------------------------*/
                    /*   Check Stocker which Lot belongs to, Available?   */
                    /*----------------------------------------------------*/
                    log.info("Call equipment_CheckAvail()   stockerID --->{}", ObjectIdentifier.fetchValue(strWhatNextLotListForInternalBufferInqResult.getStrWhatNextAttributes().get(i).getStockerID()));
                    //【step10】 - equipment_CheckAvail
                    try {
                        equipmentMethod.equipmentCheckAvail(objCommonIn, strWhatNextLotListForInternalBufferInqResult.getStrWhatNextAttributes().get(i).getStockerID());
                    } catch (ServiceException e) {
                        log.info("RC_OK != equipment_CheckAvail()   ..<<continue>>");
                        continue;
                    }
                    /*-------------------------------------------------------------------------------*/
                    /*   Check Category for Copper/Non Copper                                        */
                    /*                                                                               */
                    /*   (*) Because it is all same, CarrierCategory of Port set the first PortID.   */
                    /*-------------------------------------------------------------------------------*/
                    log.info("Check Category for Copper/Non Copper");
                    //【step11】 - lot_CassetteCategory_CheckForContaminationControl
                    try {
                        lotMethod.lotCassetteCategoryCheckForContaminationControl(objCommonIn,
                                strWhatNextLotListForInternalBufferInqResult.getStrWhatNextAttributes().get(i).getLotID(),
                                strWhatNextLotListForInternalBufferInqResult.getStrWhatNextAttributes().get(i).getCassetteID(),
                                equipmentID,
                                strEquipmentTargetPortPickupOut.getEqpTargetPortInfo().getPortGroups().get(0).getStrPortID().get(0).getPortID());
                    } catch (ServiceException e) {
                        log.info("UnMatch CarrierCategory (Copper/NonCopper)   ..<<continue>>");
                        continue;
                    }

                    /*-----------------------*/
                    /*   Check Scrap Wafer   */
                    /*-----------------------*/
                    List<ObjectIdentifier> cassetteIDs = new ArrayList<>();
                    cassetteIDs.add(strWhatNextLotListForInternalBufferInqResult.getStrWhatNextAttributes().get(i).getCassetteID());
                    log.info("call cassette_scrapWafer_SelectDR()");
                    //【step12】 - cassette_scrapWafer_SelectDR
                    List<Infos.LotWaferMap> lotWaferMaps = cassetteMethod.cassetteScrapWaferSelectDR(objCommonIn, cassetteIDs);
                    int nScrapCnt = CimArrayUtils.getSize(lotWaferMaps);
                    if (nScrapCnt > 0){
                        log.info("Cassette has ScrapWafer ..<<continue>>");
                        continue;
                    }
                    //【step13】 - cassette_DBInfo_GetDR__170
                    Infos.CassetteDBINfoGetDRInfo cassetteDBINfoGetDRInfoParam = new Infos.CassetteDBINfoGetDRInfo();
                    cassetteDBINfoGetDRInfoParam.setCassetteID(strWhatNextInqResult.getStrWhatNextAttributes().get(i).getCassetteID());
                    cassetteDBINfoGetDRInfoParam.setDurableOperationInfoFlag(false);
                    cassetteDBINfoGetDRInfoParam.setDurableWipOperationInfoFlag(false);
                    Outputs.CassetteDBInfoGetDROut cassetteDBInfoGetDROutRetCode = cassetteMethod.cassetteDBInfoGetDR(objCommonIn, cassetteDBINfoGetDRInfoParam);
                    int lotLen = 0;
                    int lotCnt = 0;
                    Boolean bCastDispatchDisableFlag = false;
                    lotLen = CimArrayUtils.getSize(cassetteDBInfoGetDROutRetCode.getCarrierDetailInfoInqResult().getCassetteStatusInfo().getStrContainedLotInfo());
                    for (lotCnt = 0;  lotCnt < lotLen; lotCnt++) {
                        if (CimBooleanUtils.isTrue(cassetteDBInfoGetDROutRetCode.getCarrierDetailInfoInqResult().getCassetteStatusInfo().getStrContainedLotInfo().get(lotCnt).isAutoDispatchDisableFlag())){
                            bCastDispatchDisableFlag = true;
                            break;
                        }
                    }
                    if (CimBooleanUtils.isTrue(bCastDispatchDisableFlag)){
                        log.info("Cassette Auto Dispatch Disable Flag == TRUE ..<<continue>>");
                        continue;
                    }
                    Infos.StartCassette strLotInCassette = new Infos.StartCassette();
                    tmpStartCassette.add(strLotInCassette);
                    strLotInCassette.setCassetteID(strWhatNextLotListForInternalBufferInqResult.getStrWhatNextAttributes().get(i).getCassetteID());
                    /*-------------------------------*/
                    /*   Set Temporary Base Recipe   */
                    /*-------------------------------*/
                    log.info("Set Temporary Base Recipe");
                    if (processRunCount == 0){
                        log.info("processRunCount == 0");
                        if (CimBooleanUtils.isFalse(bTmpBaseRecipeFlag)){
                            //Temporary Base Recipe
                            log.info("bTmpBaseRecipeFlag == FALSE");
                            tmpBaseLogicalRecipeID = strWhatNextLotListForInternalBufferInqResult.getStrWhatNextAttributes().get(i).getLogicalRecipeID();
                            tmpBaseMachineRecipeID = strWhatNextLotListForInternalBufferInqResult.getStrWhatNextAttributes().get(i).getMachineRecipeID();
                            bTmpBaseRecipeFlag = true;
                        }
                    }
                    /*-------------------*/
                    /*   Set Port Info   */
                    /*-------------------*/
                    log.info("Set Pot Info");
                    strLotInCassette.setLoadSequenceNumber(1L);
                    strLotInCassette.setLoadPurposeType(BizConstant.SP_LOADPURPOSETYPE_PROCESSLOT);
                    strLotInCassette.setLoadPortID(strEquipmentTargetPortPickupOut.getEqpTargetPortInfo().getPortGroups().get(0).getStrPortID().get(0).getPortID());
                    /*-----------------------------------*/
                    /*   Get Contained Lot in Cassette   */
                    /*-----------------------------------*/
                    log.info("cassette_GetLotList()");
                    //【step14】 - cassette_GetLotList
                    Infos.LotListInCassetteInfo objCassetteLotListGetDROut = null;
                    try {
                        objCassetteLotListGetDROut = cassetteMethod.cassetteGetLotList(objCommonIn, tmpStartCassette.get(0).getCassetteID());
                    }catch (ServiceException e) {
                        log.info("cassette_GetLotList() != RC_OK  ...<<<continue>>>");
                        continue;
                    }
                    int nLotLen = CimArrayUtils.getSize(objCassetteLotListGetDROut.getLotIDList());
                    List<Infos.LotInCassette> lotInCassetteList = new ArrayList<>();
                    strLotInCassette.setLotInCassetteList(lotInCassetteList);
                    for (j = 0; j < nLotLen; j++) {
                        Infos.LotInCassette lotInCassette = new Infos.LotInCassette();
                        lotInCassetteList.add(j,lotInCassette);
                        lotInCassette.setRecipeParameterChangeType(BizConstant.SP_RPARM_CHANGETYPE_BYLOT);
                        lotInCassette.setMoveInFlag(true);
                        lotInCassette.setLotID(objCassetteLotListGetDROut.getLotIDList().get(j));
                        lotInCassette.setMonitorLotFlag(false);
                        CimLot aLot = baseCoreFactory.getBO(CimLot.class, tmpStartCassette.get(0).getLotInCassetteList().get(j).getLotID());
                        Validations.check (CimObjectUtils.isEmpty(aLot), retCodeConfig.getNotFoundLot());
                        if(aLot != null) {
                            lotInCassette.setLotType(aLot.getLotType());
                            lotInCassette.setSubLotType(aLot.getSubLotType());
                        }
                        //【step15】 - lot_productID_Get
                        Outputs.ObjLotProductIDGetOut lotProductIDGetOutRetCode = lotMethod.lotProductIDGet(objCommonIn, tmpStartCassette.get(0).getLotInCassetteList().get(j).getLotID());

                        lotInCassette.setProductID(lotProductIDGetOutRetCode.getProductID());
                        /*------------------------*/
                        /*   Set Operation Info   */
                        /*------------------------*/
                        log.info("Set Operation Info");
                        for (int m = 0; m < attrLen; m++) {
                            if (ObjectIdentifier.equalsWithValue(tmpStartCassette.get(0).getLotInCassetteList().get(j).getLotID(),
                                    strWhatNextLotListForInternalBufferInqResult.getStrWhatNextAttributes().get(m).getLotID())){
                                Infos.StartOperationInfo startOperationInfo = new Infos.StartOperationInfo();
                                lotInCassette.setStartOperationInfo(startOperationInfo);
                                startOperationInfo.setProcessFlowID(strWhatNextLotListForInternalBufferInqResult.getStrWhatNextAttributes().get(m).getRouteID());
                                startOperationInfo.setOperationNumber(strWhatNextLotListForInternalBufferInqResult.getStrWhatNextAttributes().get(m).getOperationNumber());
                                startOperationInfo.setOperationID(strWhatNextLotListForInternalBufferInqResult.getStrWhatNextAttributes().get(m).getOperationID());
                                break;
                            }
                        }
                        /*--------------------------------*/
                        /*   Get Contained Wafer in Lot   */
                        /*--------------------------------*/
                        log.info("lot_waferMap_Get()");
                        //【step16】 - lot_waferMap_Get
                        List<Infos.LotWaferMap> lotWaferMapGetRetCode = null;
                        try {
                            lotWaferMapGetRetCode = lotMethod.lotWaferMapGet(objCommonIn, tmpStartCassette.get(0).getLotInCassetteList().get(j).getLotID());
                        } catch (ServiceException e) {
                            log.info("lot_waferMap_Get() != RC_OK  ...<<<continue>>>");
                            continue;
                        }

                        int nWafLen = CimArrayUtils.getSize(lotWaferMapGetRetCode);
                        List<Infos.LotWafer> lotWaferList = new ArrayList<>();
                        lotInCassette.setLotWaferList(lotWaferList);
                        for (k = 0; k < nWafLen; k++) {
                            Infos.LotWafer lotWafer = new Infos.LotWafer();
                            lotWaferList.add(k,lotWafer);
                            lotWafer.setWaferID(lotWaferMapGetRetCode.get(k).getWaferID());
                            lotWafer.setSlotNumber(lotWaferMapGetRetCode.get(k).getSlotNumber());
                            lotWafer.setControlWaferFlag(lotWaferMapGetRetCode.get(k).isControlWaferFlag());
                            List<Infos.StartRecipeParameter> startRecipeParameterList = new ArrayList<>();
                            lotWafer.setStartRecipeParameterList(startRecipeParameterList);
                        }
                    }
                }
                if (i == attrLen){
                    log.info("i == attLen");
                    bWhileExitFlag = true;
                }
                if (CimArrayUtils.getSize(tmpStartCassette) == 0 || ObjectIdentifier.isEmptyWithValue(tmpStartCassette.get(0).getCassetteID())){
                    log.info("tmpStartCassette[0].cassetteID.identifier is null  ..<<continue>>");
                    continue;
                }
                /* *********************************************************************************/
                /*                                                                                */
                /*   Set logicalRecipeID, machineRecipeID and Set not use operationStartFlag      */
                /*                                                                                */
                /* *********************************************************************************/
                int nLotLen = CimArrayUtils.getSize(tmpStartCassette.get(0).getLotInCassetteList());
                for (i = 0; i < nLotLen; i++) {
                    Boolean bLotFindFlag = false;
                    for (j = 0; j < attrLen; j++) {
                        if (ObjectIdentifier.equalsWithValue(strWhatNextLotListForInternalBufferInqResult.getStrWhatNextAttributes().get(j).getLotID(),
                                tmpStartCassette.get(0).getLotInCassetteList().get(i).getLotID())){
                            log.info("strWhatNextLotListForInternalBufferInqResult.strWhatNextAttributes[j].lotID == tmpStartCassette[0].strLotInCassette[i].lotID");
                            /*-------------------------------------------------------------------------------------------*/
                            /*   Set logicalRecipeID, machineRecipeID for strWhatNextLotListForInternalBufferInqResult   */
                            /*-------------------------------------------------------------------------------------------*/
                            Infos.StartRecipe startRecipe = new Infos.StartRecipe();
                            tmpStartCassette.get(0).getLotInCassetteList().get(i).setStartRecipe(startRecipe);
                            startRecipe.setLogicalRecipeID(strWhatNextLotListForInternalBufferInqResult.getStrWhatNextAttributes().get(j).getLogicalRecipeID());
                            startRecipe.setMachineRecipeID(strWhatNextLotListForInternalBufferInqResult.getStrWhatNextAttributes().get(j).getMachineRecipeID());
                            startRecipe.setPhysicalRecipeID(strWhatNextLotListForInternalBufferInqResult.getStrWhatNextAttributes().get(j).getPhysicalRecipeID());
                            bLotFindFlag = true;
                        }
                    }
                    if (CimBooleanUtils.isFalse(bLotFindFlag)){
                        /*-----------------------------------------------------------------------------*/
                        /*   Set operationStartFlag for strWhatNextLotListForInternalBufferInqResult   */
                        /*-----------------------------------------------------------------------------*/
                        log.info("operationStartFlag <--- FALSE");
                        tmpStartCassette.get(0).getLotInCassetteList().get(i).setMoveInFlag(false);
                    }
                }
                /* *********************************************************************************/
                /*                                                                                */
                /*   Set operationStartFlag for Recipe                                            */
                /*                                                                                */
                /* *********************************************************************************/
                log.info("Set operationStartFlag for Recipe");
                CimCassette aCassette = baseCoreFactory.getBO(CimCassette.class, tmpStartCassette.get(0).getCassetteID());
                Validations.check (CimObjectUtils.isEmpty(aCassette), retCodeConfig.getNotFoundCassette());
                /*---------------------------------*/
                /*   Get Cassette's MultiLotType   */
                /*---------------------------------*/
                multiLotType = aCassette.getMultiLotType();
                if (CimStringUtils.equals(BizConstant.SP_EQP_MULTIRECIPECAPABILITY_SINGLERECIPE,multiRecipeCapability)
                        && CimStringUtils.equals(BizConstant.SP_CAS_MULTILOTTYPE_MULTILOTMULTIRECIPE,multiLotType)){
                    nLotLen = CimArrayUtils.getSize(tmpStartCassette.get(0).getLotInCassetteList());
                    for (j = 0; j < nLotLen; j++) {
                        // Set operationStartFlag for Recipe
                        /*-------------------------------*/
                        /*   Set Temporary Base Recipe   */
                        /*-------------------------------*/
                        if (processRunCount == 0){
                            //Temporary Base Recipe
                            log.info("Tmporary Base Recipe");
                        }else {
                            //Final Base Recipe
                            log.info("processRunCount{}",processRunCount);
                            log.info("Final Base Recipe");
                            tmpBaseLogicalRecipeID = baseLogicalRecipeID;
                            tmpBaseMachineRecipeID = baseMachineRecipeID;
                        }
                        ObjectIdentifier logicalRecipeID = tmpStartCassette.get(0).getLotInCassetteList().get(j).getStartRecipe() == null ? null : tmpStartCassette.get(0).getLotInCassetteList().get(j).getStartRecipe().getLogicalRecipeID();
                        ObjectIdentifier machineRecipeID = tmpStartCassette.get(0).getLotInCassetteList().get(j).getStartRecipe() == null ? null : tmpStartCassette.get(0).getLotInCassetteList().get(j).getStartRecipe().getMachineRecipeID();
                        if (ObjectIdentifier.equalsWithValue(tmpBaseLogicalRecipeID, logicalRecipeID)
                                && ObjectIdentifier.equalsWithValue(tmpBaseMachineRecipeID, machineRecipeID)){
                            log.info("tmpBaseLogicalRecipeID == logicalRecipeID && tmpBaseMachineRecipeID == machineRecipeID");
                        }else {
                            log.info("#       operationStartFlag <--- FALSE");
                            tmpStartCassette.get(0).getLotInCassetteList().get(j).setMoveInFlag(false);
                        }
                    }
                }
                /* *********************************************************************************/
                /*                                                                                */
                /*   Set StartRecipeParameter                                                     */
                /*                                                                                */
                /* *********************************************************************************/
                log.info("Set StartRecipeParameter");
                for (j = 0; j < nLotLen; j++) {
                    if (CimBooleanUtils.isFalse(tmpStartCassette.get(0).getLotInCassetteList().get(j).getMoveInFlag())){
                        log.info("tmpStartCassette[0].strLotInCassette[j].operationStartFlag is FALSE  ..<continue>");
                        continue;
                    }
                    CimLogicalRecipe aLogicalRecipe = baseCoreFactory.getBO(CimLogicalRecipe.class, tmpStartCassette.get(0).getLotInCassetteList().get(j).getStartRecipe().getLogicalRecipeID());
                    Validations.check (CimObjectUtils.isEmpty(aLogicalRecipe), retCodeConfig.getNotFoundLogicRecipe());
                    CimMachineRecipe aMachineRecipe = null;
                    /* ***********************/
                    /*   Get subLotType     */
                    /* ***********************/
                    CimLot aLot = null;
                    String subLotType = null;
                    if (CimObjectUtils.isEmpty(tmpStartCassette.get(0).getLotInCassetteList().get(j).getSubLotType())){
                        aLot = baseCoreFactory.getBO(CimLot.class, tmpStartCassette.get(0).getLotInCassetteList().get(j).getLotID());
                        Validations.check (CimObjectUtils.isEmpty(aLot), retCodeConfig.getNotFoundLot());
                        subLotType = aLot.getSubLotType();
                    }else {
                        subLotType = tmpStartCassette.get(0).getLotInCassetteList().get(j).getSubLotType();
                    }
                    Boolean skipFlag = false;
                    //【step17】 - lot_effectiveFPCInfo_Get
                    String exchangeType = BizConstant.SP_FPC_EXCHANGETYPE_STARTRESERVEINFO;
                    Outputs.ObjLotEffectiveFPCInfoGetOut lotEffectiveFPCInfoGetOutRetCode = lotMethod.lotEffectiveFPCInfoGet(objCommonIn, exchangeType, equipmentID, tmpStartCassette.get(0).getLotInCassetteList().get(j).getLotID());

                    if (CimBooleanUtils.isTrue(lotEffectiveFPCInfoGetOutRetCode.isMachineRecipeActionRequiredFlag())){
                        log.info("MachineRecipe is overwritten by FPC");
                    }else {
                        if (searchCondition == 1) {
                            if (CimObjectUtils.isEmpty(aLot)) {
                                aLot = baseCoreFactory.getBO(CimLot.class, tmpStartCassette.get(0).getLotInCassetteList().get(j).getLotID());
                                Validations.check (CimObjectUtils.isEmpty(aLot), retCodeConfig.getNotFoundLot());
                                aMachineRecipe= aLogicalRecipe.findMachineRecipeFor(aLot, aMachine);

                            }
                        } else {
                            aMachineRecipe = aLogicalRecipe.findMachineRecipeForSubLotType(aMachine, subLotType);

                        }
                        List<RecipeDTO.RecipeParameter> recipeParameterSeq = aLogicalRecipe.findRecipeParametersForSubLotType(aMachine, aMachineRecipe, subLotType);


                        int rpmCnt = CimArrayUtils.getSize(recipeParameterSeq);
                        int nWafLen = CimArrayUtils.getSize(tmpStartCassette.get(0).getLotInCassetteList().get(j).getLotWaferList());
                        for (k = 0; k < nWafLen; k++) {
                            if (CimObjectUtils.isEmpty(recipeParameterSeq)){
                                List<Infos.StartRecipeParameter> startRecipeParameterList = new ArrayList<>();
                                tmpStartCassette.get(0).getLotInCassetteList().get(j).getLotWaferList().get(k).setStartRecipeParameterList(startRecipeParameterList);
                            }else {
                                for (int l = 0; l < rpmCnt; l++) {
                                    Infos.StartRecipeParameter startRecipeParameter = new Infos.StartRecipeParameter();
                                    List<Infos.StartRecipeParameter> startRecipeParameters = tmpStartCassette.get(0).getLotInCassetteList().get(j).getLotWaferList().get(k).getStartRecipeParameterList();
                                    startRecipeParameters.add(l,startRecipeParameter);
                                    startRecipeParameter.setParameterName(recipeParameterSeq.get(l).getParameterName());//parameterName
                                    if (CimBooleanUtils.isTrue(recipeParameterSeq.get(l).getUseCurrentValueFlag())){//useCurrentValueFlag
                                        log.info("(*recipeParameterSeq)[l].useCurrentValueFlag is TRUE");
                                        startRecipeParameter.setParameterValue("");
                                    }else {
                                        startRecipeParameter.setParameterValue(recipeParameterSeq.get(l).getDefaultValue());//defaultValue
                                    }
                                    startRecipeParameter.setTargetValue(recipeParameterSeq.get(l).getDefaultValue());
                                    startRecipeParameter.setUseCurrentSettingValueFlag(recipeParameterSeq.get(l).getUseCurrentValueFlag());
                                }
                            }
                        }
                    }
                }
                /* *********************************************************************************/
                /*                                                                                */
                /*   check strStartCassette Process                                               */
                /*                                                                                */
                /* *********************************************************************************/
                log.info("check strStartCassette Process");
                /*-----------------------------------------------------------------------*/
                /*                                                                       */
                /*   Check Process for Cassette                                          */
                /*                                                                       */
                /*-----------------------------------------------------------------------*/
                //【step18】 - cassette_CheckConditionForOperationForInternalBuffer
                log.info("cassette_CheckConditionForOperationForInternalBuffer()");
                try {
                    cassetteMethod.cassetteCheckConditionForOperationForInternalBuffer(objCommonIn, equipmentID, tmpStartCassette, BizConstant.SP_OPERATION_CASSETTEDELIVERY);
                }catch (ServiceException e) {
                    log.info("cassette_CheckConditionForOperationForInternalBuffer() != RC_OK  ...<<<continue>>>");
                    continue;
                }
                /*-----------------------------------------------------------------------*/
                /*                                                                       */
                /*   Check Process for Lot                                               */
                /*                                                                       */
                /*-----------------------------------------------------------------------*/
                log.info("lot_CheckConditionForOperationForInternalBuffer()");
                //【step19】 - lot_CheckConditionForOperationForInternalBuffer
                try {
                    lotMethod.lotCheckConditionForOperationForInternalBuffer(objCommonIn, equipmentID, tmpStartCassette, BizConstant.SP_OPERATION_CASSETTEDELIVERY);
                } catch (ServiceException e) {
                    log.info("lot_CheckConditionForOperationForInternalBuffer() != RC_OK  ...<<<continue>>>");
                    continue;
                }

                /*-----------------------------------------------------------------------------*/
                /*                                                                             */
                /*   Check Equipment Port for Start Reservation                                */
                /*                                                                             */
                /*-----------------------------------------------------------------------------*/
                //【step20】 - equipment_portState_CheckForStartReservationForInternalBuffer
                log.info("equipment_portState_CheckForStartReservationForInternalBuffer()");
                try {
                    equipmentMethod.equipmentPortStateCheckForStartReservationForInternalBuffer(objCommonIn,equipmentID, tmpStartCassette);
                } catch (ServiceException e) {
                    log.info("equipment_portState_CheckForStartReservation() != RC_OK  ...<<<continue>>>");
                    continue;
                }
                /*-----------------------------------------------------------------------*/
                /*                                                                       */
                /*   Check Process for Process Durable                                   */
                /*                                                                       */
                /*-----------------------------------------------------------------------*/
                log.info("equipment_processDurableRequiredFlag_Get()");
                Boolean durableRequiredFlag = false;
                //【step21】 - equipment_processDurableRequiredFlag_Get
                try {
                    equipmentMethod.equipmentProcessDurableRequiredFlagGet(objCommonIn, equipmentID);
                } catch (ServiceException e){
                    if (Validations.isEquals(retCodeConfig.getEquipmentProcessDurableReticleRequired(), e.getCode())
                            || Validations.isEquals(retCodeConfig.getEquipmentProcessDurableFixtRequired(), e.getCode())){
                        log.info("rc == RC_EQP_PROCDRBL_RTCL_REQD || rc == RC_EQP_PROCDRBL_FIXT_REQD");
                        if (!CimStringUtils.equals(BizConstant.SP_LOADPURPOSETYPE_EMPTYCASSETTE,tmpStartCassette.get(0).getLoadPurposeType())){
                            log.info("tmpStartCassette[0].loadPurposeType != [EmptyCassette]");
                            nLotLen = CimArrayUtils.getSize(tmpStartCassette.get(0).getLotInCassetteList());
                            for (j = 0; j < nLotLen; j++) {
                                if (CimBooleanUtils.isFalse(tmpStartCassette.get(0).getLotInCassetteList().get(j).getMoveInFlag())){
                                    log.info("tmpStartCassette[0].strLotInCassette[j].operationStartFlag == FALSE  ...<<<continue>>>");
                                    continue;
                                }
                                /*--------------------------------------------------*/
                                /*   Check Process Durable Condition for OpeStart   */
                                /*--------------------------------------------------*/
                                log.info("processDurable_CheckConditionForOpeStart()");
                                //【step22】 - processDurable_CheckConditionForOpeStart
                                Outputs.ObjProcessDurableCheckConditionForOperationStartOut durableCheckConditionForOperationStartOutRetCode = null;
                                try{
                                    durableCheckConditionForOperationStartOutRetCode = processMethod.processDurableCheckConditionForOpeStart(objCommonIn,
                                            equipmentID,
                                            tmpStartCassette.get(0).getLotInCassetteList().get(j).getStartRecipe().getLogicalRecipeID(),
                                            tmpStartCassette.get(0).getLotInCassetteList().get(j).getStartRecipe().getMachineRecipeID(),
                                            tmpStartCassette.get(0).getLotInCassetteList().get(j).getLotID());
                                }catch (ServiceException ex){
                                    log.error("processDurable_CheckConditionForOpeStart() != RC_OK");
                                    durableRequiredFlag = true;
                                    log.info("DurableRequiredFlag = TRUE");
                                    break;
                                }

                                /*---------------------------------------*/
                                /*   Set Available Reticles / Fixtures   */
                                /*---------------------------------------*/
                                tmpStartCassette.get(0).getLotInCassetteList().get(j).getStartRecipe().setStartReticleList(durableCheckConditionForOperationStartOutRetCode.getStartReticleList());
                                tmpStartCassette.get(0).getLotInCassetteList().get(j).getStartRecipe().setStartFixtureList(durableCheckConditionForOperationStartOutRetCode.getStartFixtureList());
                            }
                            if (CimBooleanUtils.isTrue(durableRequiredFlag)){
                                log.info("DurableRequiredFlag == TRUE  ...<<<continue>>>");
                                continue;
                            }
                        }
                    } else if (!Validations.isEquals(retCodeConfig.getEquipmentProcessDurableNotRequired(), e.getCode())){
                        log.info("rc != RC_EQP_PROCDRBL_NOT_REQD  ...<<<continue>>>");
                        continue;
                    }
                }
                /* *********************************************************************************/
                /*                                                                                */
                /*   Check multiRecipeCapability and multiLotType                                 */
                /*   and Decide finally to put it in StartCassette                                */
                /*                                                                                */
                /* *********************************************************************************/
                log.info("Check multiRecipeCapability and multiLotType");
                //--------------------------------------------------------------------
                //      Equipment's         Cassette's
                // multiRecipeCapability    multiLotType    Same Recipe Check
                // ===================================================================
                // -----------------------  --------------------------------------------
                // S-Recipe                 SL-SR           TRUE
                //                          ML-SR           TRUE
                //                          ML-MR           TRUE
                // -----------------------  --------------------------------------------
                // Batch                    SL-SR           TRUE
                //                          ML-SR           TRUE
                //                          ML-MR           Error
                // -----------------------  --------------------------------------------
                Boolean bAddStartCassette = false;
                if (CimStringUtils.equals(BizConstant.SP_EQP_MULTIRECIPECAPABILITY_MULTIPLERECIPE,multiRecipeCapability)){
                    // Same Recipe Check : FALSE
                    log.info("M-Recipe: SL-SR, ML-SR, ML-MR");
                    log.info("Same Recipe Check : FALSE");
                    bAddStartCassette = true;
                }else if (CimStringUtils.equals(BizConstant.SP_EQP_MULTIRECIPECAPABILITY_BATCH,multiRecipeCapability)
                        && CimStringUtils.equals(BizConstant.SP_CAS_MULTILOTTYPE_MULTILOTMULTIRECIPE,multiLotType)){
                    // Batch and ML-MR
                    // Error
                    log.info("Batch and ML-MR:  ...<<<continue>>>");
                    continue;
                }else {
                    // S-Recipe: SL-SR, ML-SR, ML-MR
                    // Batch   : SL-SR, ML-SR
                    // Same Recipe Check : TRUE
                    log.info("S-Recipe: SL-SR, ML-SR, ML-MR");
                    log.info("Batch   : SL-SR, ML-SR");
                    log.info("Same Recipe Check : TRUE");
                    if (processRunCount == 0){
                        log.info("processRunCount == 0");
                        bAddStartCassette = true;
                        /*------------------------------------------------------------*/
                        /*   Set Base Recipe (First operationStartFlag=TRUE Recipe)   */
                        /*------------------------------------------------------------*/
                        nLotLen = CimArrayUtils.getSize(tmpStartCassette.get(0).getLotInCassetteList());
                        for (i = 0; i < nLotLen; i++) {
                            if (CimBooleanUtils.isTrue(tmpStartCassette.get(0).getLotInCassetteList().get(i).getMoveInFlag())){
                                baseLogicalRecipeID = tmpStartCassette.get(0).getLotInCassetteList().get(i).getStartRecipe().getLogicalRecipeID();
                                baseMachineRecipeID = tmpStartCassette.get(0).getLotInCassetteList().get(i).getStartRecipe().getMachineRecipeID();
                                break;
                            }
                        }
                        if (ObjectIdentifier.isEmptyWithValue(baseLogicalRecipeID)
                                || ObjectIdentifier.isEmptyWithValue(baseMachineRecipeID)){
                            log.info("Can not Found Base Recipe!!");
                        }
                    }else {
                        ObjectIdentifier logicalRecipeID = new ObjectIdentifier();
                        ObjectIdentifier machineRecipeID = new ObjectIdentifier();
                        /*--------------------------------------------------------*/
                        /*   Find Recipe (First operationStartFlag=TRUE Recipe)   */
                        /*--------------------------------------------------------*/
                        nLotLen  = CimArrayUtils.getSize(tmpStartCassette.get(0).getLotInCassetteList());
                        for (i = 0; i < nLotLen; i++) {
                            if (CimBooleanUtils.isTrue(tmpStartCassette.get(0).getLotInCassetteList().get(i).getMoveInFlag())){
                                logicalRecipeID = tmpStartCassette.get(0).getLotInCassetteList().get(i).getStartRecipe().getLogicalRecipeID();
                                machineRecipeID = tmpStartCassette.get(0).getLotInCassetteList().get(i).getStartRecipe().getMachineRecipeID();
                                break;
                            }
                        }
                        if (ObjectIdentifier.isEmptyWithValue(logicalRecipeID) || ObjectIdentifier.isEmptyWithValue(machineRecipeID)){
                            log.info("Can not Found Recipe!!");
                        }
                        if (ObjectIdentifier.equalsWithValue(baseLogicalRecipeID, logicalRecipeID)
                                && ObjectIdentifier.equalsWithValue(baseMachineRecipeID, machineRecipeID)){
                            log.info("baseLogicalRecipeID == logicalRecipeID && baseMachineRecipeID == machineRecipeID");
                            bAddStartCassette = true;
                        }else {
                            log.info("Deferent Recipe.  ...<<<continue>>>");
                            continue;
                        }
                    }
                }
                /* *********************************************************************************/
                /*                                                                                */
                /*   Add tmpStartCassette to StartCassette                                        */
                /*                                                                                */
                /* *********************************************************************************/
                if(CimBooleanUtils.isTrue(bAddStartCassette)){
                    /* ******************************************************************************/
                    /*   Check MonitorCreationFlag                                                 */
                    /*   Only one time of the beginnings                                           */
                    /* ******************************************************************************/
                    if (CimBooleanUtils.isFalse(bAlreadyCheckMonitorCreationFlag)
                            && CimBooleanUtils.isTrue(strEquipmentProcessBatchConditionGetOut.isMonitorCreationFlag())){
                        log.info("Check MonitorCreationFlag");
                        // Stop Pickup of Lot if ShelfSpace of EmptyCassette becomes 0
                        if (0 == nShelfSpaceOfEmptyCassette){
                            log.info("0 == nShelfSpaceOfEmptyCassette  ...<<<continue>>>");
                            bWhileExitFlag  = true;
                            continue;
                        }
                        CimLogicalRecipe aLogicalRecipeID = baseCoreFactory.getBO(CimLogicalRecipe.class, tmpBaseLogicalRecipeID);
                        Validations.check (CimObjectUtils.isEmpty(aLogicalRecipeID), retCodeConfig.getNotFoundLogicRecipe());
                        CimProductSpecification aMonitorProduct = aLogicalRecipeID.getMonitorProduct();
                        if (!CimObjectUtils.isEmpty(aMonitorProduct)){
                            //EmptyCassette is necessary!
                            log.info("Set EmptyCassette for MonitorCreation");
                            /*------------------------------------------------*/
                            /*   Look for Port to assign, and EmptyCassette   */
                            /*------------------------------------------------*/
                            log.info("Look for Port to assign, and EmptyCassette");
                            //【step23】 - cassetteDelivery_SearchEmptyCassetteAssignPort
                            ObjectIdentifier dummyLotID = new ObjectIdentifier();
                            List<ObjectIdentifier> dummyPortIDSeq = new ArrayList<>();
                            Outputs.ObjCassetteDeliverySearchEmptyCassetteAssignPortOut cassetteDeliverySearchEmptyCassetteAssignPortRetCode = null;
                            try{
                                cassetteDeliverySearchEmptyCassetteAssignPortRetCode = cassetteMethod.cassetteDeliverySearchEmptyCassetteAssignPort(objCommonIn, dummyLotID,
                                        strEquipmentTargetPortPickupOut.getEqpTargetPortInfo().getPortGroups().get(0).getStrPortID(), cassetteListEmptyAvailablePickupOutRetCode.getStrFoundCassette(),
                                        useEmptyCassetteIDSeq, dummyPortIDSeq);
                            }catch (ServiceException e) {
                                log.error("NotFound!! Assign EmptyCassette or Assign Port!   set bWhileExitFlag = TRUE  ...<<<continue>>>");
                                bWhileExitFlag = true;
                                continue;
                            }
                            // Hold EmptyCasstte and Port to prevent duplication
                            log.info("Hold EmptyCasstte and Port to prevent duplication");
                            useAssignEmptyCassettePortSeq.add(nAssignEmptyCassetteCnt,cassetteDeliverySearchEmptyCassetteAssignPortRetCode.getStrFoundPort().getPortID());
                            useEmptyCassetteIDSeq.add(nAssignEmptyCassetteCnt,cassetteDeliverySearchEmptyCassetteAssignPortRetCode.getFoundEmptyCassetteID());
                            nAssignEmptyCassetteCnt++;

                            // Reduce the number of the rest of Shelf of EmptyCasstte
                            nShelfSpaceOfEmptyCassette--;
                        }
                        bAlreadyCheckMonitorCreationFlag = true;
                    }
                    /* ******************************************************************************/
                    /*   Set EmptyCassette if it is necessary                                      */
                    /* ******************************************************************************/
                    if (CimBooleanUtils.isTrue(strEquipmentProcessBatchConditionGetOut.isCassetteExchangeFlag())){
                        log.info("cassetteExchangeFlag == TRUE");
                        // Stop Pickup of Lot if ShelfSpace of EmptyCassette becomes 0
                        if (0 == nShelfSpaceOfEmptyCassette){
                            log.info("0 == nShelfSpaceOfEmptyCassette  ...<<<continue>>>");
                            bWhileExitFlag = true;
                            continue;
                        }
                        /*------------------------------------------------------*/
                        /*   Look for the first Lot when OpeStartFlag is TRUE   */
                        /*------------------------------------------------------*/
                        log.info("Look for the first Lot when OpeStartFlag is TRUE");
                        ObjectIdentifier targetLotID = new ObjectIdentifier();
                        nLotLen = CimArrayUtils.getSize(tmpStartCassette.get(0).getLotInCassetteList());
                        for (i = 0; i < nLotLen; i++) {
                            if (CimBooleanUtils.isTrue(tmpStartCassette.get(0).getLotInCassetteList().get(i).getMoveInFlag())){
                                targetLotID = tmpStartCassette.get(0).getLotInCassetteList().get(i).getLotID();
                                break;
                            }
                        }
                        if (ObjectIdentifier.isEmptyWithValue(targetLotID)){
                            log.info("targetLotID is null  ...<<<continue>>>");
                            continue;
                        }
                        /*------------------------------------------------*/
                        /*   Look for Port to assign, and EmptyCassette   */
                        /*------------------------------------------------*/
                        log.info("Look for Port to assign, and EmptyCassette");
                        //【step24】 - cassetteDelivery_SearchEmptyCassetteAssignPort
                        List<ObjectIdentifier> dummyPortIDSeq = new ArrayList<>();
                        Outputs.ObjCassetteDeliverySearchEmptyCassetteAssignPortOut  cassetteDeliverySearchEmptyCassetteAssignPortRetCode = null;
                        try{
                            cassetteDeliverySearchEmptyCassetteAssignPortRetCode = cassetteMethod.cassetteDeliverySearchEmptyCassetteAssignPort(objCommonIn,
                                    targetLotID,
                                    strEquipmentTargetPortPickupOut.getEqpTargetPortInfo().getPortGroups().get(0).getStrPortID(),
                                    cassetteListEmptyAvailablePickupOutRetCode.getStrFoundCassette(),
                                    useEmptyCassetteIDSeq,
                                    dummyPortIDSeq);
                        } catch (ServiceException e) {
                            log.error("NotFound!! Assign EmptyCassette or Assign Port!   set bWhileExitFlag = TRUE  ...<<<continue>>>");
                            bWhileExitFlag = true;
                            continue;
                        }
                        // Hold EmptyCasstte and Port to prevent duplication
                        log.info("Hold EmptyCasstte and Port to prevent duplication");
                        useAssignEmptyCassettePortSeq.add(nAssignEmptyCassetteCnt,cassetteDeliverySearchEmptyCassetteAssignPortRetCode.getStrFoundPort().getPortID());
                        useEmptyCassetteIDSeq.add(nAssignEmptyCassetteCnt,cassetteDeliverySearchEmptyCassetteAssignPortRetCode.getFoundEmptyCassetteID());
                        nAssignEmptyCassetteCnt++;

                        // Reduce the number of the rest of Shelf of EmptyCasstte
                        nShelfSpaceOfEmptyCassette--;
                    }
                    /* ******************************************************************************/
                    /*   Add tmpStartCassette to StartCassette                                     */
                    /* ******************************************************************************/
                    log.info("Add tmpStartCassette to StartCassette");
                    strStartCassette.add(processRunCount,tmpStartCassette.get(0));
                    processRunCount++;
                }
            }
            /*-----------------------------------------------------------------------------------------*/
            /*   If selected Carrier does not fulfill MinBatchSize, they are omitted from candidate.   */
            /*-----------------------------------------------------------------------------------------*/
            if (CimBooleanUtils.isTrue(bCheckMinBatchSize)
                    && 0 < processRunCount && processRunCount < strEquipmentProcessBatchConditionGetOut.getMinBatchSize()){
                log.info("processRunCount < minBatchSize");
                for (int m = 0; m < processRunCount; m++) {
                    int lenOmitCst = CimArrayUtils.getSize(omiCassetteSeq);
                    omiCassetteSeq.add(lenOmitCst,strStartCassette.get(m).getCassetteID());
                }
            }else {
                log.info("MinBatchSize support loop");
                break;
            }
        }
        /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-*/
        /*                                                                                                            */
        /*                                                                                                            */
        /*                                                                                                            */
        /*   Final Check                                                                                              */
        /*                                                                                                            */
        /*                                                                                                            */
        /*                                                                                                            */
        /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-*/
        log.info("Final Check  ");
        /*----------------------------------------*/
        /*   Check Short of EmptyCassette Count   */
        /*----------------------------------------*/
        Validations.check (processRunCount == 0 && CimBooleanUtils.isTrue(bShortOfEmptyCassette), retCodeConfigEx.getNotEnoughEmptyCassette());
        /*---------------------------*/
        /*   Check ProcessRunCount   */
        /*---------------------------*/
        log.info("Check ProcessRunCount");
        Validations.check (processRunCount == 0, retCodeConfigEx.getNotFoundFilledCast());
        /*------------------------------------------------------*/
        /*   Check MinimumBatchSize of Equipment                */
        /*       The check of MaxBatchSize is done in the top   */
        /*------------------------------------------------------*/
        log.info("Check MinimumBatchSize of Equipment The check of MaxBatchSize is done in the top");
        Validations.check (processRunCount < strEquipmentProcessBatchConditionGetOut.getMinBatchSize(),  retCodeConfig.getInvalidProcessBatchCount(),processRunCount,strEquipmentProcessBatchConditionGetOut.getMaxBatchSize(),strEquipmentProcessBatchConditionGetOut.getMinBatchSize());
        /*-------------------------------*/
        /*   Check Mininum Wafer Count   */
        /*-------------------------------*/
        log.info("Check Mininum Wafer Count");
        int nTotalWaferCount = 0;
        for (i = 0; i < processRunCount; i++) {
            int lenLot = CimArrayUtils.getSize(strStartCassette.get(i).getLotInCassetteList());
            for (j = 0; j < lenLot; j++) {
                if (CimBooleanUtils.isTrue(strStartCassette.get(i).getLotInCassetteList().get(j).getMoveInFlag())){
                    nTotalWaferCount += CimArrayUtils.getSize(strStartCassette.get(i).getLotInCassetteList().get(j).getLotWaferList());
                }
            }
        }
        Validations.check (strEquipmentProcessBatchConditionGetOut.getMinWaferSize() > nTotalWaferCount, retCodeConfig.getInvalidInputWaferCount());
        if (!ObjectIdentifier.isEmptyWithValue(flowBatchCheckConditionForCassetteDeliveryRetCode.getFlowBatchID())){
            log.info("Check!! processRunCount must be the same as FlowBatching Lots Count");
            /*-----------------------------------------------------------------*/
            /*   processRunCount must be the same as FlowBatching Lots Count   */
            /*-----------------------------------------------------------------*/
            int lenFlowBatchLots = CimArrayUtils.getSize(flowBatchCheckConditionForCassetteDeliveryRetCode.getStrContainedLotsInFlowBatch());
            List<ObjectIdentifier> cassetteIDSeq = new ArrayList<>();
            int nFlowBatchCasIdx = 0;
            for (i = 0; i < lenFlowBatchLots; i++) {
                Boolean bFound = false;
                int lenCas = CimArrayUtils.getSize(cassetteIDSeq);
                for (j = 0; j < lenCas; j++) {
                    if (ObjectIdentifier.equalsWithValue(cassetteIDSeq.get(j),
                            flowBatchCheckConditionForCassetteDeliveryRetCode.getStrContainedLotsInFlowBatch().get(i).getCassetteID())){
                        bFound = true;
                        break;
                    }
                }
                if (CimBooleanUtils.isFalse(bFound)){
                    cassetteIDSeq.add(nFlowBatchCasIdx,flowBatchCheckConditionForCassetteDeliveryRetCode.getStrContainedLotsInFlowBatch().get(i).getCassetteID());
                    nFlowBatchCasIdx ++;
                }
            }
            Validations.check (processRunCount != nFlowBatchCasIdx, retCodeConfigEx.getNotSelectAllFlowBatchLots());
        }
        /*-----------------------------------------------------------------------*/
        /*                                                                       */
        /*   Check Process for FlowBatch                                         */
        /*                                                                       */
        /*-----------------------------------------------------------------------*/
        log.info("equipment_lot_CheckFlowBatchConditionForOpeStart__090()");
        //【step25】 - equipment_lot_CheckFlowBatchConditionForOpeStart__090
        Inputs.ObjEquipmentLotCheckFlowBatchConditionForOperationStartIn operationStartIn=new Inputs.ObjEquipmentLotCheckFlowBatchConditionForOperationStartIn();
        operationStartIn.setEquipmentID(equipmentID);
        operationStartIn.setPortGroupID("");
        operationStartIn.setStartCassetteList(strStartCassette);
        ObjectIdentifier checkFlowBatchConditionForOperationStartOutRetCode = equipmentMethod.equipmentLotCheckFlowBatchConditionForOpeStart(objCommonIn, operationStartIn);
        /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-*/
        /*                                                                                                            */
        /*                                                                                                            */
        /*                                                                                                            */
        /*   Make Output Struct                                                                                       */
        /*                                                                                                            */
        /*                                                                                                            */
        /*                                                                                                            */
        /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-*/
        log.info("Make Output Struct");
        /*--------------------------------------------------------*/
        /*   Set LoadPortID of StartCassette. Only ProcessLot!!   */
        /*--------------------------------------------------------*/
        log.info("Set LoadPortID of StartCassette. Only ProcessLot!!");
        int nSetPortIdx = 0;
        for (i = 0; i < processRunCount; i++) {
            //Set PortID by the rotation
            strStartCassette.get(i).setLoadPortID(strEquipmentTargetPortPickupOut.getEqpTargetPortInfo().getPortGroups().get(0).getStrPortID().get(nSetPortIdx).getPortID());
            nSetPortIdx++;
            if (nSetPortIdx > nCanBeUsedPortCount - 1){
                nSetPortIdx = 0;
            }
        }
        /*----------------------------------------*/
        /*   Add EmptyCassette to StartCassette   */
        /*----------------------------------------*/
        log.info("Add EmptyCassette to StartCassette");
        int nSetStart = processRunCount;
        for (i = 0; i < nAssignEmptyCassetteCnt; i++) {
            Infos.StartCassette startCassette = new Infos.StartCassette();
            strStartCassette.add(nSetStart+i,startCassette);
            startCassette.setCassetteID(useEmptyCassetteIDSeq.get(i));
            startCassette.setLoadPortID(strEquipmentTargetPortPickupOut.getEqpTargetPortInfo().getPortGroups().get(0).getStrPortID().get(nSetPortIdx).getPortID());
            nSetPortIdx++;
            if (nSetPortIdx > nCanBeUsedPortCount -1){
                nSetPortIdx = 0;
            }
            startCassette.setLoadPurposeType(BizConstant.SP_LOADPURPOSETYPE_EMPTYCASSETTE);
        }
        /*-----------------------------------------------*/
        /*   Set Total SequenceLength of StartCassette   */
        /*-----------------------------------------------*/
        log.info("Set Total SequenceLength of StartCassette");
        /*--------------------------------------------*/
        /*   loadSequenceNumber is set again from 1   */
        /*--------------------------------------------*/
        log.info("loadSequenceNumber is set again from 1");
        for (i = 0; i < processRunCount + nAssignEmptyCassetteCnt; i++) {
            strStartCassette.get(i).setLoadSequenceNumber(i + 1L);
        }
        long tmpFPCAdoptFlag = StandardProperties.OM_DOC_ENABLE_FLAG.getLongValue();
        if (1 == tmpFPCAdoptFlag){
            log.info("FPC Adopt Flag is ON. Now apply FPCInfo.");
            String exchangeType = BizConstant.SP_FPC_EXCHANGETYPE_STARTRESERVEINFO;
            //【step26】 - FPCStartCassetteInfo_Exchange
            List<Infos.StartCassette> exchangeFPCStartCassetteInfoRetCode = fpcMethod.fpcStartCassetteInfoExchange(objCommonIn,
                    exchangeType,
                    equipmentID,
                    strStartCassette);

            strStartCassette = exchangeFPCStartCassetteInfoRetCode;
        }else {
            log.info("FPC Adopt Flag is OFF.");
        }
        /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-*/
        /*                                                                                                            */
        /*                                                                                                            */
        /*                                                                                                            */
        /*   Set Return Struct                                                                                        */
        /*                                                                                                            */
        /*                                                                                                            */
        /*                                                                                                            */
        /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-*/
        log.info("Set Return Struct");
        /*----------------------*/
        /*   Return to Caller   */
        /*----------------------*/
        return strStartCassette;
    }

    @Override
    public List<Infos.StartCassette> whatNextLotListToStartCassetteForSLMDeliveryReq(Infos.ObjCommon objCommonIn, ObjectIdentifier equipmentID, List<Infos.PortGroup> strPortGroupSeq, Results.WhatNextLotListResult whatNextInqResult) {

        /*----------------*/
        /*   Initialize   */
        /*----------------*/
        List<Infos.StartCassette> out = new ArrayList<>();
        String portGroupID = null;
        String multiLotType = null;

        int searchCondition = 0;
        String searchConditionVar = StandardProperties.OM_CONSTRAINT_CHK_WITH_SEARCH.getValue();
        if (CimStringUtils.isNotEmpty(searchConditionVar)){
            searchCondition = Integer.parseInt(searchConditionVar);
        }

        /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-*/
        /*                                                                                                            */
        /*                                                                                                            */
        /*                                                                                                            */
        /*   Check Condition and Get Information                                                                      */
        /*                                                                                                            */
        /*                                                                                                            */
        /*                                                                                                            */
        /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-*/

        /*--------------------------*/
        /*   Get Equipment Object   */
        /*--------------------------*/
        CimMachine aMachine = baseCoreFactory.getBO(CimMachine.class, equipmentID);
        Validations.check(null == aMachine,retCodeConfig.getNotFoundEqp(), ObjectIdentifier.fetchValue(equipmentID));

        /*-------------------------------------------*/
        /*   Get Equipment's MultiRecipeCapability   */
        /*-------------------------------------------*/
        String multiRecipeCapability = aMachine.getMultipleRecipeCapability();
        log.info("aMachine->getMultipleRecipeCapability: {}",multiRecipeCapability);

        /**********************************************************************/
        /*                                                                    */
        /*   Get PortID List                                                  */
        /*                                                                    */
        /**********************************************************************/
        log.info("Get PortID List");
        List<ObjectIdentifier> portID = new ArrayList<>();
        List<Long> loadSequenceNumber = new ArrayList<>();
        List<String> loadPurposeType = new ArrayList<>();
        int portCount = 0;
        int nPortGroupLen = CimArrayUtils.getSize(strPortGroupSeq);
        if (nPortGroupLen > 0){
            for (Infos.PortGroup portGroup : strPortGroupSeq) {
                int nPortLen = CimArrayUtils.getSize(portGroup.getStrPortID());
                if (nPortLen > 0){
                    for (Infos.PortID portId : portGroup.getStrPortID()) {
                        if (CimStringUtils.equals(BizConstant.SP_LOADPURPOSETYPE_EMPTYCASSETTE,portId.getLoadPurposeType())){
                            log.info("port.loadPurposeType == EmptyCassette   ...<<continue>>");
                            continue;
                        }
                        portID.add(portCount,portId.getPortID());
                        loadSequenceNumber.add(portCount,portId.getLoadSequenceNoInPortGroup());
                        loadPurposeType.add(portCount,portId.getLoadPurposeType());
                        portCount++;
                    }
                }
            }
        }
        /*-----------------------------------------------*/
        /*   Check PortCount and processRunSizeMaximum   */
        /*-----------------------------------------------*/
        log.info("Check PortCount and processRunSizeMaximum");
        int processRunCount = 0;
        int processRunSizeMaximum = whatNextInqResult.getProcessRunSizeMaximum();
        Validations.check (portCount == 0 || portCount < processRunSizeMaximum, retCodeConfigEx.getNotFoundFilledCast());
        /*---------------------------------------------*/
        /*   Get Equipment's Process Batch Condition   */
        /*---------------------------------------------*/
        log.info("equipmentProcessBatchConditionGet()");
        Outputs.ObjEquipmentProcessBatchConditionGetOut equipmentProcessBatchConditionRetCode = equipmentMethod.equipmentProcessBatchConditionGet(objCommonIn, equipmentID);

        /*-------------------------*/
        /*   Check WIP Lot Count   */
        /*-------------------------*/
        log.info("Check WIP Lot Count");
        int attrLen = CimArrayUtils.getSize(whatNextInqResult.getStrWhatNextAttributes());
        Validations.check (attrLen == 0, retCodeConfigEx.getNotFoundFilledCast());

        /**********************************************************************/
        /*                                                                    */
        /*   Get Empty Cassette                                               */
        /*                                                                    */
        /**********************************************************************/
        log.info("Get Empty Cassette");
        Outputs.ObjCassetteListEmptyAvailablePickUpOut cassetteListEmptyAvailablePickupOutRetCode = null;
        if (CimBooleanUtils.isTrue(equipmentProcessBatchConditionRetCode.isCassetteExchangeFlag()
                || CimBooleanUtils.isTrue(equipmentProcessBatchConditionRetCode.isMonitorCreationFlag()))){
            log.info("EmptyCassette is necessary");

            /*------------------------*/
            /*   Get Empty Cassette   */
            /*------------------------*/
            Inputs.ObjCassetteListGetDRIn listGetDRIn = new Inputs.ObjCassetteListGetDRIn(new Params.CarrierListInqParams());
            listGetDRIn.setEmptyFlag(true);
            listGetDRIn.setCassetteStatus(CIMStateConst.CIM_DURABLE_AVAILABLE);
            listGetDRIn.setMaxRetrieveCount(-1);
            listGetDRIn.setSorterJobCreationCheckFlag(false);
            ObjectIdentifier dummy = ObjectIdentifier.buildWithValue("");
            listGetDRIn.setBankID(dummy);
            listGetDRIn.setDurablesSubStatus(dummy);
            listGetDRIn.setFlowStatus("");
            //【note】: add searchCondition default by neyo
            listGetDRIn.setSearchCondition(new SearchCondition());
            Page<Infos.FoundCassette> carrierListInq170ResultRetCode = cassetteMethod.cassetteListGetDR170(objCommonIn, listGetDRIn);
            List<Infos.FoundCassette> strFoundCassette = carrierListInq170ResultRetCode.getContent();

            /*-----------------------------------*/
            /*   Pick Up Target Empty Cassette   */
            /*-----------------------------------*/
            log.info("cassetteListEmptyAvailablePickup()");
            cassetteListEmptyAvailablePickupOutRetCode = cassetteMethod.cassetteListEmptyAvailablePickup(objCommonIn,strFoundCassette);
        }else {
            log.info("EmptyCassette is unnecessary");
        }

        /**********************************************************************/
        /*                                                                    */
        /*   Get and Check FlowBatch Info                                     */
        /*                                                                    */
        /**********************************************************************/
        log.info("Get and Check FlowBatch Info");
        Outputs.ObjFlowBatchCheckConditionForCassetteDeliveryOut flowBatchCheckConditionForCassetteDeliveryRetCode = flowBatchMethod.flowBatchCheckConditionForCassetteDelivery(objCommonIn,equipmentID,whatNextInqResult);

        /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-*/
        /*                                                                                                            */
        /*                                                                                                            */
        /*                                                                                                            */
        /*   Make strStartCassette Process                                                                            */
        /*                                                                                                            */
        /*                                                                                                            */
        /*                                                                                                            */
        /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-*/
        log.info("Make strStartCassette Process");
        List<Infos.StartCassette> strStartCassette = new ArrayList<>();
        boolean bCheckMinBatchSize = false;
        String checkMinBatchSize = StandardProperties.OM_DELIVERY_CHK_MINIMUM_BATCH_SIZE_ENABLE.getValue();
        if (CimStringUtils.equals(checkMinBatchSize,BizConstant.VALUE_ONE)
                && equipmentProcessBatchConditionRetCode.getMinBatchSize() > 1){
            log.info("bCheckMinBatchSize is 1");
            bCheckMinBatchSize = true;
        }
        int nSetStartCassetteCnt = 0;
        List<ObjectIdentifier> omitCassetteSeq = new ArrayList<>();
        while(true){
            //Reset variable
            processRunCount = 0;
            int nPortLen = CimArrayUtils.getSize(strPortGroupSeq.get(0).getStrPortID());
            log.info("portID.length: {}",nPortLen);

            int startNo = 0;
            boolean bWhileExitFlag = false;
            ObjectIdentifier baseLogicalRecipeID = new ObjectIdentifier();
            ObjectIdentifier baseMachineRecipeID = new ObjectIdentifier();
            ObjectIdentifier tmpBaseLogicalRecipeID = new ObjectIdentifier();
            ObjectIdentifier tmpBaseMachineRecipeID = new ObjectIdentifier();

            Boolean bAlreadyCheckMonitorCreationFlag = false;
            nSetStartCassetteCnt = 0;
            int nAssignEmptyCassetteCnt = 0;
            List<ObjectIdentifier> useEmptyCassetteIDSeq = new ArrayList<>();
            List<ObjectIdentifier> useAssignEmptyCassettePortSeq = new ArrayList<>();

            int nWIPLotLoopEndCnt= 0;
            int nSaveProcessRunCount = -1;

            while (processRunCount < processRunSizeMaximum){
                log.info("while ( processRunCount < processRunSizeMaximum ) ");
                if (CimBooleanUtils.isTrue(bWhileExitFlag)){
                    log.info("bWhileExitFlag == TRUE    break!!");
                    break;
                }
                Boolean bTmpBaseRecipeFlag = false;
                List<Infos.StartCassette> tmpStartCassette = new ArrayList<>();

                /**********************************************************************************/
                /*                                                                                */
                /*   Set Cassette Info                                                            */
                /*                                                                                */
                /**********************************************************************************/
                log.info("Set Cassette Info");
                if (nSaveProcessRunCount != processRunCount){
                    log.info("Change processRunCount!!  WIPLot start index <--- 0");
                    startNo = 0;
                    nWIPLotLoopEndCnt = 0;
                }
                nSaveProcessRunCount = processRunCount;
                int i = startNo;
                for (i = startNo; i < attrLen; i++) {
                    /*-----------------------------*/
                    /*   Omit CassetteID is NULL   */
                    /*-----------------------------*/
                    if (ObjectIdentifier.isEmptyWithValue(whatNextInqResult.getStrWhatNextAttributes().get(i).getCassetteID())){
                        log.info("CassetteID is NULL   ..<<continue>>");
                        continue;
                    }
                    Boolean bOmitCassette = false;
                    int lenOmitCst = CimArrayUtils.getSize(omitCassetteSeq);
                    for (int m = 0; m < lenOmitCst; m++) {
                        if (ObjectIdentifier.equalsWithValue(omitCassetteSeq.get(m),
                                whatNextInqResult.getStrWhatNextAttributes().get(i).getCassetteID())){
                            bOmitCassette = true;
                            break;
                        }
                    }
                    if (CimBooleanUtils.isTrue(bOmitCassette)){
                        log.info("Omit CassetteID   ..<<continue>>");
                        continue;
                    }

                    /*------------------------------------------*/
                    /*   Omit already saved strStartCassette    */
                    /*------------------------------------------*/
                    Boolean bFoundFlag = false;
                    int lenStartCassette = CimArrayUtils.getSize(strStartCassette);
                    for (int j = 0; j < lenStartCassette; j++) {
                        if (ObjectIdentifier.equalsWithValue(whatNextInqResult.getStrWhatNextAttributes().get(i).getCassetteID(),
                                strStartCassette.get(j).getCassetteID())){
                            bFoundFlag = true;
                            break;
                        }
                    }
                    if (CimBooleanUtils.isTrue(bFoundFlag)){
                        log.info("CassetteID is Exist   ..<<continue>>");
                        continue;
                    }

                    /*---------------------------------------------------------------------*/
                    /*   Omit Lot which is not FlowBatchingLots in the case of FlowBatch   */
                    /*---------------------------------------------------------------------*/
                    if (ObjectIdentifier.isNotEmptyWithValue(flowBatchCheckConditionForCassetteDeliveryRetCode.getFlowBatchID())){
                        log.info("Omit Lot which is not FlowBatchingLots in the case of FlowBatch");
                        Boolean bFound = false;
                        int lenFlowBatchLots = CimArrayUtils.getSize(flowBatchCheckConditionForCassetteDeliveryRetCode.getStrContainedLotsInFlowBatch());
                        for (int j = 0; j < lenFlowBatchLots; j++) {
                            if (ObjectIdentifier.equalsWithValue(whatNextInqResult.getStrWhatNextAttributes().get(i).getLotID(),
                                    flowBatchCheckConditionForCassetteDeliveryRetCode.getStrContainedLotsInFlowBatch().get(j).getLotID())){
                                bFound = true;
                                break;
                            }
                        }
                        if (CimBooleanUtils.isFalse(bFound)){
                            log.info("LotID is not FlowBatchingLots   ..<<continue>>");
                            continue;
                        }
                        log.info("Lot {} is FlowBatching Lot!!", ObjectIdentifier.fetchValue(whatNextInqResult.getStrWhatNextAttributes().get(i).getLotID()));
                    }
                    //----------------------------------------------------------------------------------------
                    //   Omit lot if "Monitor" label is put on lot's process and the lot doesn't have EqpMonitor job
                    //----------------------------------------------------------------------------------------
                    if (CimStringUtils.equals(StandardProperties.OM_AUTOMON_FLAG.getValue(),"1")){
                        log.info("OM_AUTOMON_FLAG is 1");
                        //check Lot type
                        String lotType = lotMethod.lotTypeGet(objCommonIn, whatNextInqResult.getStrWhatNextAttributes().get(i).getLotID());
                        if (CimStringUtils.equals(BizConstant.SP_LOT_TYPE_EQUIPMENTMONITORLOT, lotType)
                                || CimStringUtils.equals(BizConstant.SP_LOT_TYPE_DUMMYLOT, lotType)){
                            log.info("lotType is Equipment Monitor or Dummy.");
                            List<Infos.EqpMonitorLabelInfo> equipmentMonitorOperationLabelGetOut = lotMethod.lotEqpMonitorOperationLabelGet(objCommonIn, whatNextInqResult.getStrWhatNextAttributes().get(i).getLotID());
                            boolean bMonitorLabel = false;
                            int size = CimArrayUtils.getSize(equipmentMonitorOperationLabelGetOut);
                            for (int x = 0; x < size; x++) {
                                log.info("Loop through strEqpMonitorLabelInfoSeq");
                                if (CimStringUtils.equals(BizConstant.SP_EQPMONITOR_OPELABEL_MONITOR,equipmentMonitorOperationLabelGetOut.get(x).getOperationLabel())){
                                    log.info("Found Monitor label");
                                    bMonitorLabel = true;
                                    break;
                                }
                            }
                            if (CimBooleanUtils.isTrue(bMonitorLabel)){
                                log.info("bMonitorLabel is TRUE.");
                                Infos.EqpMonitorJobLotInfo eqpMonitorJobLotInfoRetCode = lotMethod.lotEqpMonitorJobGet(objCommonIn, whatNextInqResult.getStrWhatNextAttributes().get(i).getLotID());

                                if (ObjectIdentifier.isEmptyWithValue(eqpMonitorJobLotInfoRetCode.getEqpMonitorJobID())){
                                    log.info("eqpMonitorJobID is not attached to lot");
                                    continue;
                                }
                                CimLot aPosLot = baseCoreFactory.getBO(CimLot.class, whatNextInqResult.getStrWhatNextAttributes().get(i).getLotID());
                                Validations.check(aPosLot == null, retCodeConfig.getNotFoundLot(), ObjectIdentifier.fetchValue(whatNextInqResult.getStrWhatNextAttributes().get(i).getLotID()));
                                CimProcessOperation aPosPO = aPosLot.getProcessOperation();
                                Validations.check (CimObjectUtils.isEmpty(aPosPO), retCodeConfig.getNotFoundProcessOperation(), ObjectIdentifier.fetchValue(whatNextInqResult.getStrWhatNextAttributes().get(i).getLotID()));
                                String strOPNumber = aPosPO.getOperationNumber();
                                CimProcessFlowContext aPFX = aPosLot.getProcessFlowContext();
                                Validations.check (CimObjectUtils.isEmpty(aPFX), retCodeConfig.getNotFoundPfx());
                                String strEqpMonitorKey = aPFX.getEqpMonOperationKey(strOPNumber);
                                if (!CimStringUtils.equals(strEqpMonitorKey, eqpMonitorJobLotInfoRetCode.getMonitorOpeKey())){
                                    //The EqpMonitor job is for another Monitor process
                                    log.info("EqpMonitorKey is not same.");
                                    continue;
                                }else {
                                    //check OK
                                    log.info("EqpMonitorKey is same.");
                                }
                            }
                        }
                    }
                    /*--------------------------------*/
                    /*   Search Port for ProcessLot   */
                    /*--------------------------------*/
                    log.info("Search Port for ProcessLot");
                    int nAssignPortIdx = -1;
                    int lenAssignPort = CimArrayUtils.getSize(useAssignEmptyCassettePortSeq);
                    int lenPort = CimArrayUtils.getSize(portID);
                    log.info("lenAssignPort: {}",lenAssignPort);
                    log.info("lenPort: {}",lenPort);

                    for (int j = 0; j < lenPort; j++) {
                        boolean bFoundPort = false;
                        for (int k = 0; k < lenAssignPort; k++) {
                            if (ObjectIdentifier.isEmptyWithValue(useAssignEmptyCassettePortSeq.get(k))){
                                continue;
                            }
                            if (ObjectIdentifier.equalsWithValue(useAssignEmptyCassettePortSeq.get(k), portID.get(j))){
                                bFoundPort = true;
                                break;
                            }
                        }
                        if (CimBooleanUtils.isFalse(bFoundPort)){
                            nAssignPortIdx = j;
                            break;
                        }
                    }
                    if (nAssignPortIdx < 0){
                        log.info("0 > nAssignPortIdx");
                        break;
                    }
                    log.info("assignPortID: {}", ObjectIdentifier.fetchValue(portID.get(nAssignPortIdx)));

                    /*------------------------------------------*/
                    /*   Check Category for Copper/Non Copper   */
                    /*------------------------------------------*/
                    log.info("Check Category for Copper/Non Copper");
                    try {
                        lotMethod.lotCassetteCategoryCheckForContaminationControl(objCommonIn,
                                whatNextInqResult.getStrWhatNextAttributes().get(i).getLotID(),
                                whatNextInqResult.getStrWhatNextAttributes().get(i).getCassetteID(),
                                equipmentID,
                                portID.get(nAssignPortIdx));
                    } catch (ServiceException e) {
                        log.info("UnMatch CarrierCategory (Copper/NonCopper)   ..<<continue>>");
                        continue;
                    }
                    //Check Stocker which Lot belongs to, Available?
                    ObjectIdentifier checkID = new ObjectIdentifier();
                    if (CimStringUtils.equals(BizConstant.SP_TRANSSTATE_EQUIPMENTIN, whatNextInqResult.getStrWhatNextAttributes().get(i).getTransferStatus())
                            && !ObjectIdentifier.equalsWithValue(whatNextInqResult.getStrWhatNextAttributes().get(i).getEquipmentID(), equipmentID)){
                        checkID = equipmentID;
                        log.info("Delivery Process is [EQP to EQP]");
                    }else {
                        checkID = whatNextInqResult.getStrWhatNextAttributes().get(i).getStockerID();
                        log.info("Delivery Process is [EQP to Stocker]");
                    }
                    log.info("Call equipmentCheckAvail()   check EqpID or StockerID --->{}", ObjectIdentifier.fetchValue(checkID));
                    try {
                        equipmentMethod.equipmentCheckAvail(objCommonIn, checkID);
                    } catch (ServiceException e){
                        log.info("equipmentCheckAvail() is not OK   ..<<continue>>");
                        continue;
                    }

                    /*-----------------------*/
                    /*   Check Scrap Wafer   */
                    /*-----------------------*/
                    List<ObjectIdentifier> cassetteIDs = new ArrayList<>();
                    cassetteIDs.add(whatNextInqResult.getStrWhatNextAttributes().get(i).getCassetteID());

                    log.info("call cassetteScrapWaferSelectDR()");
                    List<Infos.LotWaferMap> lotWaferMaps = cassetteMethod.cassetteScrapWaferSelectDR(objCommonIn, cassetteIDs);
                    int nScrapCnt = CimArrayUtils.getSize(lotWaferMaps);
                    if (nScrapCnt > 0){
                        log.info("Cassette has ScrapWafer ..<<continue>>");
                        continue;
                    }

                    Infos.CassetteDBINfoGetDRInfo cassetteDBINfoGetDRInfoParam = new Infos.CassetteDBINfoGetDRInfo();
                    cassetteDBINfoGetDRInfoParam.setCassetteID(whatNextInqResult.getStrWhatNextAttributes().get(i).getCassetteID());
                    cassetteDBINfoGetDRInfoParam.setDurableOperationInfoFlag(false);
                    cassetteDBINfoGetDRInfoParam.setDurableWipOperationInfoFlag(false);
                    Outputs.CassetteDBInfoGetDROut cassetteDBInfoGetDROutRetCode = cassetteMethod.cassetteDBInfoGetDR(objCommonIn, cassetteDBINfoGetDRInfoParam);

                    int lotLen = 0;
                    int lotCnt = 0;
                    Boolean bCastDispatchDisableFlag = false;
                    lotLen = CimArrayUtils.getSize(cassetteDBInfoGetDROutRetCode.getCarrierDetailInfoInqResult().getCassetteStatusInfo().getStrContainedLotInfo());
                    for (lotCnt = 0;  lotCnt < lotLen; lotCnt++) {
                        if (CimBooleanUtils.isTrue(cassetteDBInfoGetDROutRetCode.getCarrierDetailInfoInqResult().getCassetteStatusInfo().getStrContainedLotInfo().get(lotCnt).isAutoDispatchDisableFlag())){
                            bCastDispatchDisableFlag = true;
                            break;
                        }
                    }
                    if (CimBooleanUtils.isTrue(bCastDispatchDisableFlag)){
                        log.info("Cassette Auto Dispatch Disable Flag == TRUE ..<<continue>>");
                        continue;
                    }

                    Infos.StartCassette startCassette = new Infos.StartCassette();
                    tmpStartCassette.add(startCassette);
                    startCassette.setCassetteID(whatNextInqResult.getStrWhatNextAttributes().get(i).getCassetteID());
                    log.info("Candidate CassetteID: {}", ObjectIdentifier.fetchValue(tmpStartCassette.get(0).getCassetteID()));

                    /*-------------------------------*/
                    /*   Set Temporary Base Recipe   */
                    /*-------------------------------*/
                    log.info("Set Temporary Base Recipe");
                    if (processRunCount == 0){
                        log.info("processRunCount == 0");
                        if (CimBooleanUtils.isFalse(bTmpBaseRecipeFlag)){
                            //Temporary Base Recipe
                            log.info("bTmpBaseRecipeFlag == FALSE");
                            tmpBaseLogicalRecipeID = whatNextInqResult.getStrWhatNextAttributes().get(i).getLogicalRecipeID();
                            tmpBaseMachineRecipeID = whatNextInqResult.getStrWhatNextAttributes().get(i).getMachineRecipeID();
                            bTmpBaseRecipeFlag = true;
                            log.info("tmpBaseLogicalRecipeID: {}", ObjectIdentifier.fetchValue(tmpBaseLogicalRecipeID));
                            log.info("tmpBaseMachineRecipeID: {}", ObjectIdentifier.fetchValue(tmpBaseMachineRecipeID));
                        }
                    }
                    /*-------------------*/
                    /*   Set Port Info   */
                    /*-------------------*/
                    log.info("Set Pot Info");
                    startCassette.setLoadSequenceNumber(loadSequenceNumber.get(nAssignPortIdx));

                    if (CimStringUtils.equals(BizConstant.SP_LOADPURPOSETYPE_PROCESSMONITORLOT,loadPurposeType.get(nAssignPortIdx))){
                        if (CimStringUtils.equals(BizConstant.SP_LOT_TYPE_PRODUCTIONMONITORLOT,whatNextInqResult.getStrWhatNextAttributes().get(i).getLotType())){
                            log.info("loadPurposeType == ProcessMonitorLot and LotType == ProcessMonitorLot");
                            startCassette.setLoadPurposeType(BizConstant.SP_LOADPURPOSETYPE_PROCESSMONITORLOT);
                        }else {
                            log.info("UnMatch LoadPurposeType  ..<<continue>>");
                            continue;
                        }
                    }else {
                        log.info("Set LoadPurposeType [ProcessLot]");
                        startCassette.setLoadPurposeType(BizConstant.SP_LOADPURPOSETYPE_PROCESSLOT);
                    }
                    startCassette.setLoadPortID(portID.get(nAssignPortIdx));

                    /*---------------------*/
                    /*   Get PortGroupID   */
                    /*---------------------*/
                    log.info("Get PortGroupID");
                    bFoundFlag = false;
                    for (int j = 0; j < nPortGroupLen; j++) {
                        nPortLen = CimArrayUtils.getSize(strPortGroupSeq.get(j).getStrPortID());
                        for (int k = 0; k < nPortLen; k++) {
                            if (ObjectIdentifier.equalsWithValue(tmpStartCassette.get(0).getLoadPortID(),
                                    strPortGroupSeq.get(j).getStrPortID().get(k).getPortID())){
                                portGroupID = strPortGroupSeq.get(j).getPortGroup();
                                bFoundFlag = true;
                                break;
                            }
                        }
                        if (CimBooleanUtils.isTrue(bFoundFlag)){
                            break;
                        }
                    }
                    /*-----------------------------------*/
                    /*   Get Contained Lot in Cassette   */
                    /*-----------------------------------*/
                    log.info("cassetteGetLotList()");
                    Infos.LotListInCassetteInfo cassetteLotListGetDROutRetCode = null;
                    try {
                        cassetteLotListGetDROutRetCode = cassetteMethod.cassetteGetLotList(objCommonIn, tmpStartCassette.get(0).getCassetteID());
                    } catch (ServiceException e) {
                        log.info("cassetteGetLotList() is not OK   ...<<continue>>");
                        continue;
                    }
                    int nLotLen = CimArrayUtils.getSize(cassetteLotListGetDROutRetCode.getLotIDList());
                    List<Infos.LotInCassette> lotInCassetteList = new ArrayList<>();
                    startCassette.setLotInCassetteList(lotInCassetteList);
                    for (int j = 0; j < nLotLen; j++) {
                        Infos.LotInCassette lotInCassette = new Infos.LotInCassette();
                        lotInCassetteList.add(j,lotInCassette);
                        lotInCassette.setRecipeParameterChangeType(BizConstant.SP_RPARM_CHANGETYPE_BYLOT);
                        lotInCassette.setMoveInFlag(true);
                        lotInCassette.setLotID(cassetteLotListGetDROutRetCode.getLotIDList().get(j));

                        CimLot aLot = baseCoreFactory.getBO(CimLot.class, tmpStartCassette.get(0).getLotInCassetteList().get(j).getLotID());
                        Validations.check(null == aLot,retCodeConfig.getNotFoundLot(), ObjectIdentifier.fetchValue(tmpStartCassette.get(0).getLotInCassetteList().get(j).getLotID()));
                        lotInCassette.setLotType(aLot.getLotType());
                        lotInCassette.setSubLotType(aLot.getSubLotType());

                        log.info("lotType: {}",lotInCassette.getLotType());
                        log.info("subLotType: {}",lotInCassette.getSubLotType());

                        Outputs.ObjLotProductIDGetOut lotProductIDGetRetCode = lotMethod.lotProductIDGet(objCommonIn,tmpStartCassette.get(0).getLotInCassetteList().get(j).getLotID());

                        lotInCassette.setProductID(lotProductIDGetRetCode.getProductID());
                        log.info("productID: {}",lotInCassette.getProductID());

                        /*------------------------*/
                        /*   Set Operation Info   */
                        /*------------------------*/
                        log.info("Set Opertiion Info");
                        for (int m = 0; m < attrLen; m++) {
                            if (ObjectIdentifier.equalsWithValue(tmpStartCassette.get(0).getLotInCassetteList().get(j).getLotID(),
                                    whatNextInqResult.getStrWhatNextAttributes().get(m).getLotID())){
                                Infos.StartOperationInfo startOperationInfo = new Infos.StartOperationInfo();
                                lotInCassette.setStartOperationInfo(startOperationInfo);
                                startOperationInfo.setProcessFlowID(whatNextInqResult.getStrWhatNextAttributes().get(m).getRouteID());
                                startOperationInfo.setOperationID(whatNextInqResult.getStrWhatNextAttributes().get(m).getOperationID());
                                startOperationInfo.setOperationNumber(whatNextInqResult.getStrWhatNextAttributes().get(m).getOperationNumber());
                                break;
                            }
                        }
                        if (CimStringUtils.equals(BizConstant.SP_LOADPURPOSETYPE_PROCESSMONITORLOT,tmpStartCassette.get(0).getLoadPurposeType())){
                            /*------------------*/
                            /*   Get Lot Type   */
                            /*------------------*/
                            log.info("lotTypeGet");
                            String lotTypeRetCode = null;
                            try {
                                lotTypeRetCode = lotMethod.lotTypeGet(objCommonIn, tmpStartCassette.get(0).getLotInCassetteList().get(j).getLotID());
                            } catch (ServiceException e) {
                                continue;
                            }
                            if (CimStringUtils.equals(BizConstant.SP_LOT_TYPE_PRODUCTIONMONITORLOT, lotTypeRetCode)){
                                log.info("LotType == [MonitorLot]");
                                lotInCassette.setMonitorLotFlag(true);
                            }else {
                                log.info("LotType != [MonitorLot]");
                                lotInCassette.setMonitorLotFlag(false);
                            }
                        } else {
                            lotInCassette.setMonitorLotFlag(false);
                        }
                        /*-----------------------------------*/
                        /*   Get Contained Wafer in Lot      */
                        /*-----------------------------------*/
                        log.info("lotWaferMapGet");
                        List<Infos.LotWaferMap> lotWaferMapGetRetCode = null;
                        try {
                            lotWaferMapGetRetCode = lotMethod.lotWaferMapGet(objCommonIn, tmpStartCassette.get(0).getLotInCassetteList().get(j).getLotID());
                        } catch (ServiceException e) {
                            log.info("lotWaferMapGet() is not OK   ..<<continue>>");
                            continue;
                        }
                        int nWafLen = CimArrayUtils.getSize(lotWaferMapGetRetCode);
                        List<Infos.LotWafer> lotWaferList = new ArrayList<>();
                        lotInCassette.setLotWaferList(lotWaferList);
                        for (int k = 0; k < nWafLen; k++) {
                            List<Infos.StartRecipeParameter> startRecipeParameterList = new ArrayList<>();
                            Infos.LotWafer lotWafer = new Infos.LotWafer();
                            lotWaferList.add(lotWafer);
                            lotWafer.setWaferID(lotWaferMapGetRetCode.get(k).getWaferID());
                            lotWafer.setSlotNumber(lotWaferMapGetRetCode.get(k).getSlotNumber());
                            lotWafer.setControlWaferFlag(lotWaferMapGetRetCode.get(k).isControlWaferFlag());
                            lotWafer.setStartRecipeParameterList(startRecipeParameterList);
                        }
                    }
                    log.info("Finished tmpStartCassette Info  ..<<break!! WhatNextInfo Loop>>");
                    break;
                }
                /*--------------------------------------------------------------------*/
                /*   It is finished if Loop of WhatNextInfo finishes turning twice.   */
                /*--------------------------------------------------------------------*/
                if (i >= attrLen -1 ){
                    nWIPLotLoopEndCnt++;
                    log.info("Turned WhatNextInfo Loop!!  Therefore Count It");
                    if (1 < nWIPLotLoopEndCnt){
                        log.info("Turned <<Twice>> WhatNextInfo Loop!!   Therefore ..<<break!! WhatNextInfo Loop>>");
                        break;
                    }
                }
                //Set Next Index of WIPLot loop
                startNo = i + 1;
                log.info("make strStartCassette Process  done.");
                if (CimArrayUtils.getSize(tmpStartCassette) == 0 || ObjectIdentifier.isEmptyWithValue(tmpStartCassette.get(0).getCassetteID())){
                    log.info("tmpStartCassette[0].cassetteID.identifier is null  ..<<continue>>");
                    continue;
                }
                /**********************************************************************************/
                /*                                                                                */
                /*   Set logicalRecipeID, machineRecipeID and Set not use operationStartFlag      */
                /*                                                                                */
                /**********************************************************************************/
                log.info("Set logicalRecipeID, machineRecipeID and Set not use operationStartFlag ");
                int nLotLen = CimArrayUtils.getSize(tmpStartCassette.get(0).getLotInCassetteList());
                for (int i1 = 0; i1 < nLotLen; i1++) {
                    Boolean bLotFindFlag = false;
                    for (int j = 0; j < attrLen; j++) {
                        if (ObjectIdentifier.equalsWithValue(whatNextInqResult.getStrWhatNextAttributes().get(j).getLotID(),
                                tmpStartCassette.get(0).getLotInCassetteList().get(i1).getLotID())){
                            /*--------------------------------------------------------------------------*/
                            /*   Set logicalRecipeID, machineRecipeID for strWhatNextLotListInqResult   */
                            /*--------------------------------------------------------------------------*/
                            Infos.LotInCassette lotInCassette = tmpStartCassette.get(0).getLotInCassetteList().get(i1);
                            Infos.StartRecipe startRecipe = new Infos.StartRecipe();
                            lotInCassette.setStartRecipe(startRecipe);
                            startRecipe.setLogicalRecipeID(whatNextInqResult.getStrWhatNextAttributes().get(j).getLogicalRecipeID());
                            startRecipe.setMachineRecipeID(whatNextInqResult.getStrWhatNextAttributes().get(j).getMachineRecipeID());
                            startRecipe.setPhysicalRecipeID(whatNextInqResult.getStrWhatNextAttributes().get(j).getPhysicalRecipeID());
                            bLotFindFlag = true;
                        }
                    }
                    if (CimBooleanUtils.isFalse(bLotFindFlag)){
                        /*------------------------------------------------------------*/
                        /*   Set operationStartFlag for strWhatNextLotListInqResult   */
                        /*------------------------------------------------------------*/
                        log.info("operationStartFlag is FALSE");
                        tmpStartCassette.get(0).getLotInCassetteList().get(i1).setMoveInFlag(false);
                    }
                }
                /**********************************************************************************/
                /*                                                                                */
                /*   Set operationStartFlag for Recipe                                            */
                /*                                                                                */
                /**********************************************************************************/
                log.info(" Set operationStartFlag for Recipe ");
                CimCassette aCassette = baseCoreFactory.getBO(CimCassette.class, tmpStartCassette.get(0).getCassetteID());
                Validations.check(null == aCassette, retCodeConfig.getNotFoundCassette(), ObjectIdentifier.fetchValue(tmpStartCassette.get(0).getCassetteID()));
                multiLotType = aCassette.getMultiLotType();
                log.info("multiRecipeCapability: {}",multiRecipeCapability);
                log.info("multiLotType: {}",multiLotType);

                if (CimStringUtils.equals(BizConstant.SP_EQP_MULTIRECIPECAPABILITY_SINGLERECIPE, multiRecipeCapability)
                        && CimStringUtils.equals(BizConstant.SP_CAS_MULTILOTTYPE_MULTILOTMULTIRECIPE, multiLotType)){
                    log.info("multiRecipeCapability is [SingleRecipe] and multiLotType is [ML-MR]");
                    int nLotLen1 = CimArrayUtils.getSize(tmpStartCassette.get(0).getLotInCassetteList());
                    for (int j = 0; j < nLotLen1; j++) {
                        /*-------------------------------*/
                        /*   Set Temporary Base Recipe   */
                        /*-------------------------------*/
                        if (processRunCount == 0){
                            // Temporary Base Recipe
                            log.info("Temporary Base Recipe");
                        }else {
                            // Final Base Recipe
                            log.info("processRunCount: {}",processRunCount);
                            log.info("Final Base Recipe");

                            tmpBaseLogicalRecipeID = baseLogicalRecipeID;
                            tmpBaseMachineRecipeID = baseMachineRecipeID;
                        }
                        ObjectIdentifier logicalRecipeID = tmpStartCassette.get(0).getLotInCassetteList().get(j).getStartRecipe() == null ? null : tmpStartCassette.get(0).getLotInCassetteList().get(j).getStartRecipe().getLogicalRecipeID();
                        ObjectIdentifier machineRecipeID = tmpStartCassette.get(0).getLotInCassetteList().get(j).getStartRecipe() == null ? null : tmpStartCassette.get(0).getLotInCassetteList().get(j).getStartRecipe().getMachineRecipeID();
                        if (ObjectIdentifier.equalsWithValue(tmpBaseLogicalRecipeID, logicalRecipeID)
                                && ObjectIdentifier.equalsWithValue(tmpBaseMachineRecipeID, machineRecipeID)){
                            log.info("tmpBaseLogicalRecipeID == logicalRecipeID && tmpBaseMachineRecipeID == machineRecipeID");
                        }else {
                            tmpStartCassette.get(0).getLotInCassetteList().get(j).setMoveInFlag(false);
                        }
                    }
                }
                /**********************************************************************************/
                /*                                                                                */
                /*   Set StartRecipeParameter                                                     */
                /*                                                                                */
                /**********************************************************************************/

                log.info("Set StartRecipeParameter");
                for (int j = 0; j < nLotLen; j++) {
                    if (CimBooleanUtils.isFalse(tmpStartCassette.get(0).getLotInCassetteList().get(j).getMoveInFlag())) {
                        log.info("tmpStartCassette.get(0).getLotInCassetteList().get(j).getOperationStartFlag() is FALSE  ..<continue>");
                        continue;
                    }
                    CimLogicalRecipe aLogicalRecipe = baseCoreFactory.getBO(CimLogicalRecipe.class, tmpStartCassette.get(0).getLotInCassetteList().get(j).getStartRecipe().getLogicalRecipeID());
                    Validations.check(null == aLogicalRecipe, retCodeConfig.getNotFoundLogicalRecipe());
                    CimMachineRecipe aMachineRecipe = null;

                    /************************/
                    /*   Get subLotType     */
                    /************************/
                    CimLot aLot = null;
                    String subLotType = null;
                    if (CimStringUtils.isEmpty(tmpStartCassette.get(0).getLotInCassetteList().get(j).getSubLotType())){
                        aLot = baseCoreFactory.getBO(CimLot.class, tmpStartCassette.get(0).getLotInCassetteList().get(j).getLotID());
                        Validations.check(null == aLot, retCodeConfig.getNotFoundLot(), ObjectIdentifier.fetchValue(tmpStartCassette.get(0).getLotInCassetteList().get(j).getLotID()));
                        subLotType = aLot.getSubLotType();
                    }else {
                        subLotType = tmpStartCassette.get(0).getLotInCassetteList().get(j).getSubLotType();
                    }
                    Boolean skipFlag = false;
                    String exchangeType = BizConstant.SP_FPC_EXCHANGETYPE_STARTRESERVEINFO;
                    Outputs.ObjLotEffectiveFPCInfoGetOut lotEffectiveFPCInfoGetOutRetCode = lotMethod.lotEffectiveFPCInfoGet(objCommonIn, exchangeType, equipmentID, tmpStartCassette.get(0).getLotInCassetteList().get(j).getLotID());

                    if (CimBooleanUtils.isTrue(lotEffectiveFPCInfoGetOutRetCode.isMachineRecipeActionRequiredFlag())){
                        log.info("MachineRecipe is overwritten by FPC");
                    }else {
                        if (searchCondition == 1){
                            if (CimObjectUtils.isEmpty(aLot)){
                                aLot = baseCoreFactory.getBO(CimLot.class, tmpStartCassette.get(0).getLotInCassetteList().get(j).getLotID());
                                Validations.check(null == aLot, new OmCode(retCodeConfig.getNotFoundLot(), ObjectIdentifier.fetchValue(tmpStartCassette.get(0).getLotInCassetteList().get(j).getLotID())));
                            }
                            aMachineRecipe = aLogicalRecipe.findMachineRecipeFor(aLot, aMachine);
                        }else {
                            aMachineRecipe = aLogicalRecipe.findMachineRecipeForSubLotType(aMachine, subLotType);
                        }
                        List<RecipeDTO.RecipeParameter> recipeParameterSeq = aLogicalRecipe.findRecipeParametersForSubLotType(aMachine, aMachineRecipe, subLotType);

                        int rpmCnt = CimArrayUtils.getSize(recipeParameterSeq);
                        int nWafLen = CimArrayUtils.getSize(tmpStartCassette.get(0).getLotInCassetteList().get(j).getLotWaferList());
                        for (int k = 0; k < nWafLen; k++) {
                            if (CimObjectUtils.isEmpty(recipeParameterSeq)){
                                tmpStartCassette.get(0).getLotInCassetteList().get(j).getLotWaferList().get(k).setStartRecipeParameterList(new ArrayList<>());
                            }else {
                                for (int l = 0; l < rpmCnt; l++) {
                                    List<Infos.StartRecipeParameter> startRecipeParameterList = tmpStartCassette.get(0).getLotInCassetteList().get(j).getLotWaferList().get(k).getStartRecipeParameterList();
                                    Infos.StartRecipeParameter startRecipeParameter = new Infos.StartRecipeParameter();
                                    startRecipeParameterList.add(startRecipeParameter);

                                    startRecipeParameter.setParameterName(recipeParameterSeq.get(l).getParameterName());//parameterName
                                    if (CimBooleanUtils.isTrue(recipeParameterSeq.get(l).getUseCurrentValueFlag())){ //useCurrentValueFlag
                                        startRecipeParameter.setParameterValue("");
                                    } else {
                                        startRecipeParameter.setParameterValue(recipeParameterSeq.get(l).getDefaultValue());//defaultValue
                                    }
                                    startRecipeParameter.setTargetValue(recipeParameterSeq.get(l).getDefaultValue());
                                    startRecipeParameter.setUseCurrentSettingValueFlag(recipeParameterSeq.get(l).getUseCurrentValueFlag());
                                }
                            }
                        }
                    }
                }
                /**********************************************************************************/
                /*                                                                                */
                /*   check strStartCassette Process                                               */
                /*                                                                                */
                /**********************************************************************************/
                log.info("check strStartCassette Process");
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
                /*                                                                       */
                /*-----------------------------------------------------------------------*/
                log.info("Check Process for Cassette");
                long saveLoadSequenceNumber = CimNumberUtils.longValue(tmpStartCassette.get(0).getLoadSequenceNumber());
                tmpStartCassette.get(0).setLoadSequenceNumber(1L);

                //*===== for emptyCassetteCount =====*/
                if (CimBooleanUtils.isTrue(equipmentProcessBatchConditionRetCode.isCassetteExchangeFlag())
                        || CimBooleanUtils.isTrue(equipmentProcessBatchConditionRetCode.isMonitorCreationFlag())
                        || equipmentProcessBatchConditionRetCode.getMinBatchSize() > 1){
                    log.info("cassetteExchangeFlag == TRUE or monitorCreationFlag  == TRUE or minBatchSize > 1");
                    log.info("call cassetteCheckConditionForDelivery()");
                    String operation = BizConstant.SP_OPERATION_STARTRESERVATION;

                    try{
                        cassetteMethod.cassetteCheckConditionForDelivery(objCommonIn, equipmentID, portGroupID, tmpStartCassette, operation);
                    }catch (ServiceException e) {
                        log.info("cassetteCheckConditionForDelivery() is not OK   ..<<continue>>");
                        continue;
                    }
                } else {
                    log.info("!cassetteExchangeFlag == TRUE or monitorCreationFlag  == TRUE or minBatchSize > 1");
                    log.info("call cassetteCheckConditionForDelivery()");

                    String operation = BizConstant.SP_OPERATION_STARTRESERVATION;
                    try {
                        cassetteMethod.cassetteCheckConditionForOperation(objCommonIn,equipmentID, portGroupID, tmpStartCassette, operation);
                    }catch (ServiceException e) {
                        log.info("cassetteCheckConditionForDelivery() is not OK   ..<<continue>>");
                        continue;
                    }
                }
                tmpStartCassette.get(0).setLoadSequenceNumber(saveLoadSequenceNumber);
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
                log.info("lotCheckConditionForOperation");
                String operation =BizConstant.SP_OPERATION_CASSETTEDELIVERY ;
                try {
                    lotMethod.lotCheckConditionForOperation(objCommonIn,
                            equipmentID,
                            portGroupID,
                            tmpStartCassette,
                            operation);
                } catch (ServiceException e) {
                    log.info("lotCheckConditionForOperation() is not OK   ..<<continue>>");
                    continue;
                }


                /*-----------------------------------------------------------------------------*/
                /*                                                                             */
                /*   Check Equipment Port for Start Reservation                                */
                /*                                                                             */
                /*   The following conditions are checked by this object                       */
                /*                                                                             */
                /*   1. In-parm's portGroupID must not have controlJobID.                      */
                /*   2. All of ports' loadMode must be CIMFW_PortRsc_Input or _InputOutput.    */
                /*   3. All of port, which is registered as in-parm's portGroup, must be       */
                /*      _LoadAvail or _LoadReq when equipment is online.                       */
                /*      As exceptional case, if equipment's takeOutInTransferFlag is True,     */
                /*      _UnloadReq is also OK for start reservation when equipment is Online.  */
                /*   4. All of port, which is registered as in-parm's portGroup,               */
                /*      must not have loadedCassetteID.                                        */
                /*   5. strStartCassette[].loadPortID's portGroupID must be same               */
                /*      as in-parm's portGroupID.                                              */
                /*   6. strStartCassette[].loadPurposeType must be match as specified port's   */
                /*      loadPutposeType.                                                       */
                /*   7. strStartCassette[].loadSequenceNumber must be same as specified port's */
                /*      loadSequenceNumber.                                                    */
                /*                                                                             */
                /*-----------------------------------------------------------------------------*/
                log.info("equipmentPortStateCheckForStartReservation");
                try {
                    equipmentMethod.equipmentPortStateCheckForStartReservation(objCommonIn,
                            equipmentID,
                            portGroupID,
                            tmpStartCassette,
                            false);
                } catch (ServiceException e) {
                    log.info("equipmentPortStateCheckForStartReservation() is not OK   ..<<continue>>");
                    continue;
                }
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
                log.info("Check Process for Process Durable");
                Boolean  durableRequiredFlag = false;
                log.info("call equipmentProcessDurableRequiredFlagGet()");
                try {
                    equipmentMethod.equipmentProcessDurableRequiredFlagGet(objCommonIn, equipmentID);
                } catch (ServiceException e){
                    if (Validations.isEquals(retCodeConfig.getEquipmentProcessDurableReticleRequired(), e.getCode())
                            || Validations.isEquals(retCodeConfig.getEquipmentProcessDurableFixtRequired(), e.getCode())){
                        log.info("error == RC_EQP_PROCDRBL_RTCL_REQD || error == RC_EQP_PROCDRBL_FIXT_REQD");
                        if (!CimStringUtils.equals(BizConstant.SP_LOADPURPOSETYPE_EMPTYCASSETTE,tmpStartCassette.get(0).getLoadPurposeType())){
                            log.info("tmpStartCassette[0].loadPurposeType != SP_LoadPurposeType_EmptyCassette");
                            int nLotLen2 = CimArrayUtils.getSize(tmpStartCassette.get(0).getLotInCassetteList());
                            for (int j = 0; j < nLotLen2; j++) {
                                if (CimBooleanUtils.isFalse(tmpStartCassette.get(0).getLotInCassetteList().get(j).getMoveInFlag())){
                                    log.info("tmpStartCassette[0].strLotInCassette[j].operationStartFlag == FALSE");
                                    continue;
                                }
                                /*--------------------------------------------------*/
                                /*   Check Process Durable Condition for OpeStart   */
                                /*--------------------------------------------------*/
                                log.info("call processDurableCheckConditionForOpeStart()");
                                Outputs.ObjProcessDurableCheckConditionForOperationStartOut durableCheckConditionForOperationStartOutRetCode = null;
                                try{
                                    durableCheckConditionForOperationStartOutRetCode = processMethod.processDurableCheckConditionForOpeStart(objCommonIn,
                                            equipmentID,
                                            tmpStartCassette.get(0).getLotInCassetteList().get(j).getStartRecipe().getLogicalRecipeID(),
                                            tmpStartCassette.get(0).getLotInCassetteList().get(j).getStartRecipe().getMachineRecipeID(),
                                            tmpStartCassette.get(0).getLotInCassetteList().get(j).getLotID());
                                }catch(ServiceException ex){
                                    log.info("processDurableCheckConditionForOpeStart() is not OK");
                                    durableRequiredFlag = true;
                                    log.info("DurableRequiredFlag = TRUE");
                                    break;
                                }

                                /*---------------------------------------*/
                                /*   Set Available Reticles / Fixtures   */
                                /*---------------------------------------*/
                                tmpStartCassette.get(0).getLotInCassetteList().get(j).getStartRecipe().setStartReticleList(durableCheckConditionForOperationStartOutRetCode.getStartReticleList());
                                tmpStartCassette.get(0).getLotInCassetteList().get(j).getStartRecipe().setStartFixtureList(durableCheckConditionForOperationStartOutRetCode.getStartFixtureList());
                            }
                            if (CimBooleanUtils.isTrue(durableRequiredFlag)){
                                log.info("DurableRequiredFlag == TRUE   ..<<continue>>");
                                continue;
                            }
                        }
                    } else if (!Validations.isEquals(retCodeConfig.getEquipmentProcessDurableNotRequired(), e.getCode())){
                        log.info("error != RC_EQP_PROCDRBL_NOT_REQD   ..<<continue>>");
                        continue;
                    }
                }

                /* *********************************************************************************/
                /*                                                                                */
                /*   Check multiRecipeCapability and multiLotType                                 */
                /*   and Decide finally to put it in StartCassette                                */
                /*                                                                                */
                /* *********************************************************************************/
                log.info("Check multiRecipeCapability and multiLotType");
                //----------------------------------------------------------------------
                //      Equipment's         Cassette's
                // multiRecipeCapability    multiLotType    Same Recipe Check
                // =====================================================================
                // M-Recipe                 SL-SR           FALSE
                //                          ML-SR           FALSE
                //                          ML-MR           FALSE
                // -----------------------  --------------------------------------------
                // S-Recipe                 SL-SR           TRUE
                //                          ML-SR           TRUE
                //                          ML-MR           TRUE
                // -----------------------  --------------------------------------------
                // Batch                    SL-SR           TRUE
                //                          ML-SR           TRUE
                //                          ML-MR           Error
                // -----------------------  --------------------------------------------
                Boolean bAddStartCassette = false;
                if (CimStringUtils.equals(BizConstant.SP_EQP_MULTIRECIPECAPABILITY_MULTIPLERECIPE,multiRecipeCapability)){
                    // M-Recipe: SL-SR, ML-SR, ML-MR
                    // Same Recipe Check : FALSE
                    log.info("M-Recipe: SL-SR, ML-SR, ML-MR");
                    log.info("Same Recipe Check : FALSE");
                    log.info("Add cassetteID: {}", ObjectIdentifier.fetchValue(tmpStartCassette.get(0).getCassetteID()));

                    bAddStartCassette = true;
                }else if (CimStringUtils.equals(BizConstant.SP_EQP_MULTIRECIPECAPABILITY_BATCH,multiRecipeCapability)
                        && CimStringUtils.equals(BizConstant.SP_CAS_MULTILOTTYPE_MULTILOTMULTIRECIPE,multiLotType)){
                    // Batch and ML-MR
                    // Error
                    log.error("Batch and ML-MR: continue");
                    continue;
                }else {
                    // S-Recipe: SL-SR, ML-SR, ML-MR
                    // Batch   : SL-SR, ML-SR
                    // Same Recipe Check : TRUE
                    log.info("S-Recipe: SL-SR, ML-SR, ML-MR");
                    log.info("Batch   : SL-SR, ML-SR");
                    log.info("Same Recipe Check : TRUE");
                    if (processRunCount == 0){
                        log.info("processRunCount == 0");
                        log.info("Add cassetteID: {}", ObjectIdentifier.fetchValue(tmpStartCassette.get(0).getCassetteID()));
                        bAddStartCassette = true;
                        /*------------------------------------------------------------*/
                        /*   Set Base Recipe (First operationStartFlag=TRUE Recipe)   */
                        /*------------------------------------------------------------*/
                        for (i = 0; i < tmpStartCassette.get(0).getLotInCassetteList().size(); i++) {
                            if (CimBooleanUtils.isTrue(tmpStartCassette.get(0).getLotInCassetteList().get(i).getMoveInFlag())){
                                baseLogicalRecipeID = tmpStartCassette.get(0).getLotInCassetteList().get(i).getStartRecipe().getLogicalRecipeID();
                                baseMachineRecipeID = tmpStartCassette.get(0).getLotInCassetteList().get(i).getStartRecipe().getMachineRecipeID();
                                break;
                            }
                        }
                        if (ObjectIdentifier.isEmptyWithValue(baseLogicalRecipeID)
                                || ObjectIdentifier.isEmptyWithValue(baseMachineRecipeID)){
                            log.info("baseLogicalRecipeID: {}",baseLogicalRecipeID);
                            log.info("baseMachineRecipeID: {}",baseMachineRecipeID);
                        }
                    }else {
                        ObjectIdentifier logicalRecipeID = new ObjectIdentifier();
                        ObjectIdentifier machineRecipeID = new ObjectIdentifier();
                        /*--------------------------------------------------------*/
                        /*   Find Recipe (First operationStartFlag=TRUE Recipe)   */
                        /*--------------------------------------------------------*/
                        for (i = 0; i < tmpStartCassette.get(0).getLotInCassetteList().size(); i++) {
                            if (CimBooleanUtils.isTrue(tmpStartCassette.get(0).getLotInCassetteList().get(i).getMoveInFlag())){
                                logicalRecipeID = tmpStartCassette.get(0).getLotInCassetteList().get(i).getStartRecipe().getLogicalRecipeID();
                                machineRecipeID = tmpStartCassette.get(0).getLotInCassetteList().get(i).getStartRecipe().getMachineRecipeID();
                                break;
                            }
                        }
                        if (ObjectIdentifier.isEmptyWithValue(logicalRecipeID)
                                || ObjectIdentifier.isEmptyWithValue(machineRecipeID)){
                            log.info("baseLogicalRecipeID: {}",baseLogicalRecipeID);
                            log.info("baseMachineRecipeID: {}",baseMachineRecipeID);
                        }
                        if (ObjectIdentifier.equalsWithValue(baseLogicalRecipeID, logicalRecipeID)
                                && ObjectIdentifier.equalsWithValue(baseMachineRecipeID, machineRecipeID)){
                            log.info("baseLogicalRecipeID == logicalRecipeID && baseMachineRecipeID == machineRecipeID");
                            log.info("Add cassetteID: {}", ObjectIdentifier.fetchValue(tmpStartCassette.get(0).getCassetteID()));
                            bAddStartCassette = true;
                        }else {
                            log.info("Deferent Recipe.  ...<<<continue>>>");
                            continue;
                        }
                    }
                }
                /* *********************************************************************************/
                /*                                                                                */
                /*   Add tmpStartCassette to StartCassette                                        */
                /*                                                                                */
                /* *********************************************************************************/
                log.info("Add tmpStartCassette to StartCassette");
                if (CimBooleanUtils.isTrue(bAddStartCassette)){
                    /* ******************************************************************************/
                    /*   Check MonitorCreationFlag                                                 */
                    /*   Only one time of the beginnings                                           */
                    /* ******************************************************************************/
                    if (CimBooleanUtils.isFalse(bAlreadyCheckMonitorCreationFlag)
                            && CimBooleanUtils.isTrue(equipmentProcessBatchConditionRetCode.isMonitorCreationFlag())){
                        log.info("Check MonitorCreationFlag");
                        CimLogicalRecipe aLogicalRecipe = baseCoreFactory.getBO(CimLogicalRecipe.class, tmpBaseLogicalRecipeID);
                        Validations.check(null == aLogicalRecipe, retCodeConfig.getNotFoundLogicalRecipe());
                        CimProductSpecification aMonitorProduct = aLogicalRecipe.getMonitorProduct();
                        if (!CimObjectUtils.isEmpty(aMonitorProduct)){
                            //EmptyCassette is necessary!
                            log.info("Set EmptyCassette for MonitorCreation");
                            /*------------------------------------------------*/
                            /*   Look for Port to assign, and EmptyCassette   */
                            /*------------------------------------------------*/
                            log.info("Look for Port to assign, and EmptyCassette");
                            //【step23】 - cassetteDelivery_SearchEmptyCassetteAssignPort
                            ObjectIdentifier dummyLotID = new ObjectIdentifier();
                            Outputs.ObjCassetteDeliverySearchEmptyCassetteAssignPortOut cassetteDeliverySearchEmptyCassetteAssignPortRetCode = null;
                            try{
                                cassetteDeliverySearchEmptyCassetteAssignPortRetCode = cassetteMethod.cassetteDeliverySearchEmptyCassetteAssignPort(objCommonIn,
                                        dummyLotID, strPortGroupSeq.get(0).getStrPortID(), cassetteListEmptyAvailablePickupOutRetCode.getStrFoundCassette(),
                                        useEmptyCassetteIDSeq, useAssignEmptyCassettePortSeq);
                            }catch (ServiceException e) {
                                log.error("NotFound!! Assign EmptyCassette or Assign Port!   set bWhileExitFlag = TRUE  ...<<<continue>>>");
                                bWhileExitFlag = true;
                                continue;
                            }
                            //Hold EmptyCasstte and Port to prevent duplication
                            log.info("Hold EmptyCasstte and Port to prevent duplication");
                            useAssignEmptyCassettePortSeq.add(nAssignEmptyCassetteCnt,cassetteDeliverySearchEmptyCassetteAssignPortRetCode.getStrFoundPort().getPortID());
                            useEmptyCassetteIDSeq.add(nAssignEmptyCassetteCnt,cassetteDeliverySearchEmptyCassetteAssignPortRetCode.getFoundEmptyCassetteID());
                            nAssignEmptyCassetteCnt++;
                            /*-------------------------------------------------------------------------*/
                            /*   Found!! Assign EmptyCassette. ---> Add information on startCassette   */
                            /*   Put it at the head of StartCassette surely!!                          */
                            /*-------------------------------------------------------------------------*/
                            log.info("Found!! Assign EmptyCassette. ---> Add information on startCassette");
                            strStartCassette.add(new Infos.StartCassette());
                            strStartCassette.get(0).setLoadSequenceNumber(cassetteDeliverySearchEmptyCassetteAssignPortRetCode.getStrFoundPort().getLoadSequenceNoInPortGroup());
                            strStartCassette.get(0).setCassetteID(cassetteDeliverySearchEmptyCassetteAssignPortRetCode.getFoundEmptyCassetteID());
                            strStartCassette.get(0).setLoadPurposeType(BizConstant.SP_LOADPURPOSETYPE_EMPTYCASSETTE);
                            strStartCassette.get(0).setLoadPortID(cassetteDeliverySearchEmptyCassetteAssignPortRetCode.getStrFoundPort().getPortID());
                            nSetStartCassetteCnt ++;
                            if (Objects.equals(strStartCassette.get(0).getLoadSequenceNumber(), tmpStartCassette.get(0).getLoadSequenceNumber())){
                                log.info("strStartCassette[0].loadSequenceNumber == tmpStartCassette[0].loadSequenceNumber");
                                log.info("Duplication Setting PortInfo!!");
                                int lenPortIDs = CimArrayUtils.getSize(portID);
                                log.info("lenPortIDs: {}",lenPortIDs);
                                if (1 < lenPortIDs){
                                    //Set next port info
                                    tmpStartCassette.get(0).setLoadSequenceNumber(loadSequenceNumber.get(1));
                                    tmpStartCassette.get(0).setLoadPortID(portID.get(1));
                                }
                            }
                        }
                        bAlreadyCheckMonitorCreationFlag = true;
                    }
                    /*------------------------------------------------------------------------------------------------------*/
                    /*   Selected PortID and CassetteID is stocked.                                                         */
                    /*   Originally this Sequence(useAssignEmptyCassettePortSeq, useEmptyCassetteIDSeq) exists for Empty.   */
                    /*   But, by D4200302, Cassette must assign also to AnyPort.                                            */
                    /*   Here, if it does not set to these Sequences, following problem arises                              */
                    /*                                               by cassetteDelivery_SearchEmptyCassetteAssignPort().   */
                    /*     Equipment is CassetteExchange Type.                                                              */
                    /*                                                                                                      */
                    /*     PortID  PortGrp  Purpose                                                                         */
                    /*     --------------------------                                                                       */
                    /*       P1       A       Any                                                                           */
                    /*       P2       A       Any                                                                           */
                    /*                                                                                                      */
                    /*    First, P1 is assigned with ProcessLot.                                                            */
                    /*    Next, Search EmpPort by cassetteDelivery_SearchEmptyCassetteAssignPort().                         */
                    /*    But Function chooses P1.                                                                          */
                    /*    Because P1 is not set to useEmptyCassetteIDSeq.                                                   */
                    /*                                                                                                      */
                    /*    It is not necessary to put CassetteID into useAssignEmptyCassettePortSeq.                         */
                    /*    But sequence counter(nAssignEmptyCassetteCnt) becomes mismatching.                                */
                    /*    So, CassetteID is set also to useEmptyCassetteIDSeq.                                              */
                    /*------------------------------------------------------------------------------------------------------*/
                    useAssignEmptyCassettePortSeq.add(nAssignEmptyCassetteCnt,tmpStartCassette.get(0).getLoadPortID());
                    useEmptyCassetteIDSeq.add(nAssignEmptyCassetteCnt,tmpStartCassette.get(0).getCassetteID());
                    nAssignEmptyCassetteCnt++;
                    /* ******************************************************************************/
                    /*   Set EmptyCassette if it is necessary                                      */
                    /* ******************************************************************************/
                    if (CimBooleanUtils.isTrue(equipmentProcessBatchConditionRetCode.isCassetteExchangeFlag())){
                        log.info("Set EmptyCassette for CassetteExchange");
                        /*------------------------------------------------------*/
                        /*   Look for the first Lot when OpeStartFlag is TRUE   */
                        /*------------------------------------------------------*/
                        log.info("Look for the first Lot when OpeStartFlag is TRUE");
                        ObjectIdentifier targetLotID = new ObjectIdentifier();
                        for (i = 0; i < tmpStartCassette.get(0).getLotInCassetteList().size(); i++) {
                            if (CimBooleanUtils.isTrue(tmpStartCassette.get(0).getLotInCassetteList().get(i).getMoveInFlag())){
                                log.info("Found!! Target LotID: {}", ObjectIdentifier.fetchValue(tmpStartCassette.get(0).getLotInCassetteList().get(i).getLotID()));
                                targetLotID = tmpStartCassette.get(0).getLotInCassetteList().get(i).getLotID();
                                break;
                            }
                        }
                        if (ObjectIdentifier.isEmptyWithValue(targetLotID)){
                            log.info("targetLotID is null  ...<<<continue>>>");
                            continue;
                        }
                        /*------------------------------------------------*/
                        /*   Look for Port to assign, and EmptyCassette   */
                        /*------------------------------------------------*/
                        log.info("Look for Port to assign, and EmptyCassette ");
                        Outputs.ObjCassetteDeliverySearchEmptyCassetteAssignPortOut cassetteDeliverySearchEmptyCassetteAssignPortOutRetCode = null;
                        try{
                            cassetteDeliverySearchEmptyCassetteAssignPortOutRetCode = cassetteMethod.cassetteDeliverySearchEmptyCassetteAssignPort(objCommonIn,
                                    targetLotID, strPortGroupSeq.get(0).getStrPortID(), cassetteListEmptyAvailablePickupOutRetCode.getStrFoundCassette(),
                                    useEmptyCassetteIDSeq, useAssignEmptyCassettePortSeq);
                        }catch (ServiceException e) {
                            log.error("NotFound!! Assign EmptyCassette or Assign Port!   set bWhileExitFlag = TRUE  ...<<<continue>>>");
                            bWhileExitFlag = true;
                            continue;
                        }
                        // Hold EmptyCasstte and Port to prevent duplication
                        log.info("Hold EmptyCasstte and Port to prevent duplication");
                        useAssignEmptyCassettePortSeq.add(nAssignEmptyCassetteCnt,cassetteDeliverySearchEmptyCassetteAssignPortOutRetCode.getStrFoundPort().getPortID());
                        useEmptyCassetteIDSeq.add(nAssignEmptyCassetteCnt,cassetteDeliverySearchEmptyCassetteAssignPortOutRetCode.getFoundEmptyCassetteID());
                        nAssignEmptyCassetteCnt++;
                        log.info("nAssignEmptyCassetteCnt: {}",nAssignEmptyCassetteCnt);

                        /*-------------------------------------------------------------------------*/
                        /*   Found!! Assign EmptyCassette. -----> Add information on TmpCassette   */
                        /*-------------------------------------------------------------------------*/
                        log.info("Found!! Assign EmptyCassette. ---> Add information on startCassette");
                        Infos.StartCassette startCassette = new Infos.StartCassette();
                        tmpStartCassette.add(startCassette);
                        startCassette.setLoadSequenceNumber(cassetteDeliverySearchEmptyCassetteAssignPortOutRetCode.getStrFoundPort().getLoadSequenceNoInPortGroup());
                        startCassette.setCassetteID(cassetteDeliverySearchEmptyCassetteAssignPortOutRetCode.getFoundEmptyCassetteID());
                        startCassette.setLoadPurposeType(BizConstant.SP_LOADPURPOSETYPE_EMPTYCASSETTE);
                        startCassette.setLoadPortID(cassetteDeliverySearchEmptyCassetteAssignPortOutRetCode.getStrFoundPort().getPortID());
                    }
                    /*******************************************************************************/
                    /*   Add tmpStartCassette to StartCassette                                     */
                    /*******************************************************************************/
                    log.info("Add tmpStartCassette to StartCassette");
                    for (int j = 0; j < CimArrayUtils.getSize(tmpStartCassette); j++) {
                        strStartCassette.add(nSetStartCassetteCnt, tmpStartCassette.get(j));
                        nSetStartCassetteCnt++;
                    }
                    processRunCount++;
                }
            }
            log.info("Select Cassette Result");
            /*-----------------------------------------------------------------------------------------*/
            /*   If selected Carrier does not fulfill MinBatchSize, they are omitted from candidate.   */
            /*-----------------------------------------------------------------------------------------*/
            if (CimBooleanUtils.isTrue(bCheckMinBatchSize) && processRunCount > 0 && processRunCount < equipmentProcessBatchConditionRetCode.getMinBatchSize()){
                log.info("processRunCount < minBatchSize");
                for (int m = 0; m < nSetStartCassetteCnt; m++) {
                    omitCassetteSeq.add(strStartCassette.get(m).getCassetteID());
                }
            }else {
                log.info("MinBatchSize support loop");
                break;
            }
        }
        /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-*/
        /*                                                                                                            */
        /*                                                                                                            */
        /*                                                                                                            */
        /*   Final Check                                                                                              */
        /*                                                                                                            */
        /*                                                                                                            */
        /*                                                                                                            */
        /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-*/
        log.info("Final Check");
        /*---------------------------*/
        /*   Check processRunCount   */
        /*---------------------------*/
        Validations.check (processRunCount == 0, retCodeConfigEx.getNotFoundFilledCast());
        Validations.check(processRunCount < equipmentProcessBatchConditionRetCode.getMinBatchSize(), retCodeConfigEx.getNotFoundFilledCast());
        /*-------------------------------*/
        /*   Check Mininum Wafer Count   */
        /*-------------------------------*/
        log.info("Check Mininum Wafer Count");
        int nTotalWaferCount = 0;
        for (int i = 0; i < nSetStartCassetteCnt; i++) {
            int lenLot = CimArrayUtils.getSize(strStartCassette.get(i).getLotInCassetteList());
            for (int j = 0; j < lenLot; j++) {
                if (CimBooleanUtils.isTrue(strStartCassette.get(i).getLotInCassetteList().get(j).getMoveInFlag())){
                    nTotalWaferCount += CimArrayUtils.getSize(strStartCassette.get(i).getLotInCassetteList().get(j).getLotWaferList());
                }
            }
        }
        Validations.check (equipmentProcessBatchConditionRetCode.getMinWaferSize() > nTotalWaferCount, retCodeConfig.getInvalidInputWaferCount());
        //--------------------------------------------------------------------------------------
        // Count the wafer that is Stored or Retrieved in equipment container position.
        //--------------------------------------------------------------------------------------
        log.info("Count the wafer that is Stored or Retrieved in equipment container position.");
        log.info("call equipmentContainerInfoGetDR()");
        Infos.EqpContainerInfo eqpContainerInfo = equipmentMethod.equipmentContainerInfoGetDR(objCommonIn, equipmentID);
        List<Infos.EqpContainer> strEqpContSeq = eqpContainerInfo.getEqpContainerList();
        int lenEqpCont = CimArrayUtils.getSize(strEqpContSeq);
        Validations.check(0 == lenEqpCont,retCodeConfig.getNotFoundEquipmentContainer());

        int waferInEqpCnt = 0;
        int waferNotInEqpCnt = 0;
        int lenEqpContPos = CimArrayUtils.getSize(strEqpContSeq.get(0).getEqpContainerPosition());
        log.info("lenEqpContPos: {}",lenEqpContPos);
        for (int nEcp = 0; nEcp < lenEqpContPos; nEcp++) {
            if (ObjectIdentifier.isNotEmptyWithValue(strEqpContSeq.get(0).getEqpContainerPosition().get(nEcp).getWaferID())){
                waferInEqpCnt++;
                log.info("waferInEqpCnt: {}",waferInEqpCnt);
            }else {
                waferNotInEqpCnt++;
                log.info("waferNotInEqpCnt: {}",waferNotInEqpCnt);
            }
        }
        log.info("Total waferInEqpCnt: {}",waferInEqpCnt);
        log.info("Total waferNotInEqpCnt: {}",waferNotInEqpCnt);
        log.info("equipment's maxRsvCount: {}",strEqpContSeq.get(0).getMaxRsvCount());

        //--------------------------------------------------------------------------------------
        // Started wafers and wafers that is in equipment must not exceed maxRsvCount of equipment container.
        //--------------------------------------------------------------------------------------
        if (nTotalWaferCount + waferInEqpCnt > strEqpContSeq.get(0).getMaxRsvCount()){
            log.info("return RC_NOT_ENOUGH_CONTAINER_SPACE");
            // The starting wafers count [%s] and wafers that is already in container count [%s] exceed max reserve count [%s].
            Validations.check(true,retCodeConfigEx.getTotalWaferOverMaxrsvCount(),nTotalWaferCount,waferInEqpCnt,strEqpContSeq.get(0).getMaxRsvCount());
        }
        if (ObjectIdentifier.isNotEmptyWithValue(flowBatchCheckConditionForCassetteDeliveryRetCode.getFlowBatchID())){
            log.info("Check!! processRunCount must be the same as FlowBatching Lots Count");
            /*-----------------------------------------------------------------*/
            /*   processRunCount must be the same as FlowBatching Lots Count   */
            /*-----------------------------------------------------------------*/
            int lenFlowBatchLots = CimArrayUtils.getSize(flowBatchCheckConditionForCassetteDeliveryRetCode.getStrContainedLotsInFlowBatch());
            log.info("lenFlowBatchLots: {}",lenFlowBatchLots);
            List<ObjectIdentifier> cassetteIDSeq = new ArrayList<>();
            int nFlowBatchCasIdx = 0;
            for (int i = 0; i < lenFlowBatchLots; i++) {
                Boolean bFound = false;
                int lenCas = CimArrayUtils.getSize(cassetteIDSeq);
                for (int j = 0; j < lenCas; j++) {
                    if (ObjectIdentifier.equalsWithValue(cassetteIDSeq.get(j), flowBatchCheckConditionForCassetteDeliveryRetCode.getStrContainedLotsInFlowBatch().get(i).getCassetteID())){
                        bFound = true;
                        break;
                    }
                }
                if (CimBooleanUtils.isFalse(bFound)){
                    cassetteIDSeq.add(nFlowBatchCasIdx,flowBatchCheckConditionForCassetteDeliveryRetCode.getStrContainedLotsInFlowBatch().get(i).getCassetteID());
                    nFlowBatchCasIdx++;
                }
            }
            Validations.check (processRunCount != nFlowBatchCasIdx, retCodeConfigEx.getNotSelectAllFlowBatchLots());
        }
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
        log.info("equipmentLotCheckFlowBatchConditionForOpeStart");
        Inputs.ObjEquipmentLotCheckFlowBatchConditionForOperationStartIn operationStartIn=new Inputs.ObjEquipmentLotCheckFlowBatchConditionForOperationStartIn();
        operationStartIn.setEquipmentID(equipmentID);
        operationStartIn.setPortGroupID(portGroupID);
        operationStartIn.setStartCassetteList(strStartCassette);
        equipmentMethod.equipmentLotCheckFlowBatchConditionForOpeStart(objCommonIn, operationStartIn);
        long tmpFPCAdoptFlag = StandardProperties.OM_DOC_ENABLE_FLAG.getLongValue();
        if (1 == tmpFPCAdoptFlag){
            log.info("FPC Adopt Flag is ON. Now apply FPCInfo.");
            //【step26】 - FPCStartCassetteInfo_Exchange
            List<Infos.StartCassette> exchangeFPCStartCassetteInfo = fpcMethod.fpcStartCassetteInfoExchange(objCommonIn,
                    BizConstant.SP_FPC_EXCHANGETYPE_STARTRESERVEINFO,
                    equipmentID,
                    strStartCassette);

            strStartCassette = exchangeFPCStartCassetteInfo;
        }else {
            log.info("FPC Adopt Flag is OFF.");
        }
        /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-*/
        /*                                                                                                            */
        /*                                                                                                            */
        /*                                                                                                            */
        /*   Set Return Struct                                                                                        */
        /*                                                                                                            */
        /*                                                                                                            */
        /*                                                                                                            */
        /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-*/

        return strStartCassette;
    }

    @Override
    public List<Infos.StartDurable> whatNextDurableListToStartDurableForDeliveryReq(Infos.ObjCommon objCommonIn, ObjectIdentifier equipmentID, List<Infos.PortGroup> strPortGroupSeq, Results.WhatNextDurableListInqResult durableWhatNextInqResult) {
        //Initialize
        String portGroupID = null;
        String multiLotType = null;

        //Check Condition and Get Information
        //Get Equipment Object
        CimMachine aMachine = baseCoreFactory.getBO(CimMachine.class, equipmentID);
        Validations.check(null == aMachine,retCodeConfig.getNotFoundEqp(), ObjectIdentifier.fetchValue(equipmentID));

        //Get Equipment's MultiRecipeCapability
        String multiRecipeCapability = aMachine.getMultipleRecipeCapability();

        //Get PortID List
        log.info("Get PortID List");
        List<ObjectIdentifier> portID = new ArrayList<>();
        List<Long> loadSequenceNumber = new ArrayList<>();
        List<String> loadPurposeType = new ArrayList<>();
        int portCount = 0;
        int nPortGroupLen = CimArrayUtils.getSize(strPortGroupSeq);
        for (int i = 0; i < nPortGroupLen; i++) {
            int nPortLen = CimArrayUtils.getSize(strPortGroupSeq.get(i).getStrPortID());
            for (int j = 0; j < nPortLen; j++) {
                if (CimStringUtils.equals(BizConstant.SP_LOADPURPOSETYPE_EMPTYCASSETTE,strPortGroupSeq.get(i).getStrPortID().get(j).getLoadPurposeType())){
                    log.info("port.loadPurposeType == EmptyCassette   ...<<continue>>");
                    continue;
                }
                portID.add(portCount,strPortGroupSeq.get(i).getStrPortID().get(j).getPortID());
                loadSequenceNumber.add(portCount,strPortGroupSeq.get(i).getStrPortID().get(j).getLoadSequenceNoInPortGroup());
                loadPurposeType.add(portCount,strPortGroupSeq.get(i).getStrPortID().get(j).getLoadPurposeType());
                portCount++;
            }
        }
        // Check PortCount and processRunSizeMaximum
        log.info("Check PortCount and processRunSizeMaximum");
        int processRunCount = 0;
        int processRunSizeMaximum = durableWhatNextInqResult.getProcessRunSizeMaximum().intValue();
        Validations.check (portCount == 0 || portCount < processRunSizeMaximum, retCodeConfigEx.getNotFoundFilledDurable());
        //Get Equipment's Process Batch Condition
        log.info("step1 - equipmentProcessBatchConditionGet()");
        Outputs.ObjEquipmentProcessBatchConditionGetOut equipmentProcessBatchConditionRetCode = equipmentMethod.equipmentProcessBatchConditionGet(objCommonIn, equipmentID);
        //Check Durable Count
        log.info("Check Durable Count");
        int attrLen = CimArrayUtils.getSize(durableWhatNextInqResult.getStrWhatNextDurableAttributes());
        Validations.check (attrLen == 0, retCodeConfigEx.getNotFoundFilledDurable());

        //Make strStartDurable Process
        log.info("Make strStartDurable Process");
        List<Infos.StartDurable> strStartDurable = new ArrayList<>();
        boolean bCheckMinBatchSize = false;
        String checkMinBatchSize = StandardProperties.OM_DELIVERY_CHK_MINIMUM_BATCH_SIZE_ENABLE.getValue();
        if (CimStringUtils.equals(checkMinBatchSize,"1")
                && equipmentProcessBatchConditionRetCode.getMinBatchSize() > 1){
            log.info("bCheckMinBatchSize is 1");
            bCheckMinBatchSize = true;
        }
        int nSetStartDurableCnt = 0;
        List<ObjectIdentifier> omitDurableSeq = new ArrayList<>();
        while(true){
            //Reset variable
            processRunCount = 0;
            int nPortLen;
            int startNo = 0;
            boolean bWhileExitFlag = false;
            nSetStartDurableCnt = 0;

            ObjectIdentifier baseLogicalRecipeID = new ObjectIdentifier();
            ObjectIdentifier baseMachineRecipeID = new ObjectIdentifier();
            ObjectIdentifier tmpBaseLogicalRecipeID = new ObjectIdentifier();
            ObjectIdentifier tmpBaseMachineRecipeID = new ObjectIdentifier();

            int nWIPLotLoopEndCnt= 0;
            int nSaveProcessRunCount = -1;
            int nAssignEmptyCassetteCnt = 0;
            List<ObjectIdentifier> useEmptyDurableIDSeq = new ArrayList<>();
            List<ObjectIdentifier> useAssignEmptyDurablePortSeq = new ArrayList<>();
            while (processRunCount < processRunSizeMaximum){
                log.info("while ( processRunCount < processRunSizeMaximum ) ");
                if (CimBooleanUtils.isTrue(bWhileExitFlag)){
                    log.info("bWhileExitFlag == TRUE    break!!");
                    break;
                }
                Boolean bTmpBaseRecipeFlag = false;
                List<Infos.StartDurable> tmpStartDurable = new ArrayList<>();

                //Set Cassette Info
                log.info("Set Cassette Info");
                if (nSaveProcessRunCount != processRunCount){
                    log.info("Change processRunCount!!  WIPLot start index <--- 0");
                    startNo = 0;
                    nWIPLotLoopEndCnt = 0;
                }
                nSaveProcessRunCount = processRunCount;
                int i = startNo;
                for (i = startNo; i < attrLen; i++) {
                    // Omit DirableID is NULL
                    if (ObjectIdentifier.isEmptyWithValue(durableWhatNextInqResult.getStrWhatNextDurableAttributes().get(i).getDurableID())){
                        log.info("Durable is NULL   ..<<continue>>");
                        continue;
                    }
                    Boolean bOmitDurable = false;
                    int lenOmitCst = CimArrayUtils.getSize(omitDurableSeq);
                    for (int m = 0; m < lenOmitCst; m++) {
                        if (ObjectIdentifier.equalsWithValue(omitDurableSeq.get(m),
                                durableWhatNextInqResult.getStrWhatNextDurableAttributes().get(i).getDurableID())){
                            bOmitDurable = true;
                            break;
                        }
                    }
                    if (CimBooleanUtils.isTrue(bOmitDurable)){
                        log.info("Omit DurableID   ..<<continue>>");
                        continue;
                    }

                    //Omit already saved strStartDurable
                    Boolean bFoundFlag = false;
                    int lenStartDurable = CimArrayUtils.getSize(strStartDurable);
                    for (int j = 0; j < lenStartDurable; j++) {
                        if (ObjectIdentifier.equalsWithValue(durableWhatNextInqResult.getStrWhatNextDurableAttributes().get(i).getDurableID(),
                                strStartDurable.get(j).getDurableId())){
                            bFoundFlag = true;
                            break;
                        }
                    }
                    if (CimBooleanUtils.isTrue(bFoundFlag)){
                        log.info("DurableID is Exist   ..<<continue>>");
                        continue;
                    }

                    // Search Port for ProcessLot
                    log.info("Search Port for Durable");
                    int nAssignPortIdx = -1;
                    int lenPort = CimArrayUtils.getSize(portID);
                    int lenAssignPort = CimArrayUtils.getSize(useAssignEmptyDurablePortSeq);
                    for (int j = 0; j < lenPort; j++) {
                        boolean bFoundPort = false;
                        for (int k = 0; k < lenAssignPort; k++) {
                            if (ObjectIdentifier.isEmptyWithValue(useAssignEmptyDurablePortSeq.get(k))){
                                continue;
                            }
                            if (ObjectIdentifier.equalsWithValue(useAssignEmptyDurablePortSeq.get(k), portID.get(j))){
                                bFoundPort = true;
                                break;
                            }
                        }
                        if (CimBooleanUtils.isFalse(bFoundPort)){
                            nAssignPortIdx = j;
                            break;
                        }
                    }
                    if (nAssignPortIdx < 0){
                        log.info("0 > nAssignPortIdx");
                        break;
                    }

                    //Check Category for Copper/Non Copper
                    log.info("Check Category for Copper/Non Copper");
                    log.info("step2 - durableCassetteCategoryCheckForContaminationControl");
                    try {
                        durableMethod.durableCassetteCategoryCheckForContaminationControl(objCommonIn,
                                durableWhatNextInqResult.getStrWhatNextDurableAttributes().get(i).getDurableID(),
                                equipmentID,
                                portID.get(nAssignPortIdx));
                    } catch (ServiceException e) {
                        log.error("UnMatch DurableCategory (Copper/NonCopper)   ..<<continue>>");
                        continue;
                    }
                    //Check Stocker which Lot belongs to, Available?
                    ObjectIdentifier checkID = new ObjectIdentifier();
                    if (CimStringUtils.equals(BizConstant.SP_TRANSSTATE_EQUIPMENTIN, durableWhatNextInqResult.getStrWhatNextDurableAttributes().get(i).getTransferStatus())
                            && !ObjectIdentifier.equalsWithValue(durableWhatNextInqResult.getStrWhatNextDurableAttributes().get(i).getEquipmentID(), equipmentID)){
                        checkID = equipmentID;
                        log.info("Delivery Process is [EQP to EQP]");
                    }else {
                        checkID = durableWhatNextInqResult.getStrWhatNextDurableAttributes().get(i).getStockerID();
                        log.info("Delivery Process is [EQP to Stocker]");
                    }
                    if (CimStringUtils.equals(BizConstant.SP_TRANSSTATE_EQUIPMENTOUT,durableWhatNextInqResult.getStrWhatNextDurableAttributes().get(i).getTransferStatus())
                            || CimStringUtils.equals(BizConstant.SP_TRANSSTATE_STATIONOUT,durableWhatNextInqResult.getStrWhatNextDurableAttributes().get(i).getTransferStatus())){
                        log.info("Durable is EO or SO. No need to check machine availability.");
                    }else {
                        log.info("Call equipmentCheckAvail()   check EqpID or StockerID --->{}", ObjectIdentifier.fetchValue(checkID));
                        log.info("step3 - equipmentCheckAvail");
                        try {
                            equipmentMethod.equipmentCheckAvail(objCommonIn, checkID);
                        } catch (ServiceException e){
                            log.error("equipmentCheckAvail is Not OK   ..<<continue>>");
                            continue;
                        }
                    }

                    Infos.StartDurable startDurable = new Infos.StartDurable();
                    tmpStartDurable.add(startDurable);
                    startDurable.setDurableId(durableWhatNextInqResult.getStrWhatNextDurableAttributes().get(i).getDurableID());

                    //Set Temporary Base Recipe
                    log.info("Set Temporary Base Recipe");
                    if (CimBooleanUtils.isFalse(bTmpBaseRecipeFlag)){
                        //Temporary Base Recipe
                        log.info("bTmpBaseRecipeFlag == FALSE");
                        tmpBaseLogicalRecipeID = durableWhatNextInqResult.getStrWhatNextDurableAttributes().get(i).getLogicalRecipeID();
                        tmpBaseMachineRecipeID = durableWhatNextInqResult.getStrWhatNextDurableAttributes().get(i).getMachineRecipeID();
                        bTmpBaseRecipeFlag = true;
                    }

                    //Set Port Info
                    log.info("Set Pot Info");
                    Infos.StartDurablePort startDurablePort = new Infos.StartDurablePort();
                    startDurable.setStartDurablePort(startDurablePort);
                    startDurablePort.setLoadSequenceNumber(loadSequenceNumber.get(nAssignPortIdx));
                    startDurablePort.setLoadPurposeType(BizConstant.SP_LOADPURPOSETYPE_OTHER);
                    startDurablePort.setLoadPortID(portID.get(nAssignPortIdx));

                    //Get PortGroupID
                    log.info("Get PortGroupID");
                    bFoundFlag = false;
                    for (int j = 0; j < nPortGroupLen; j++) {
                        nPortLen = CimArrayUtils.getSize(strPortGroupSeq.get(j).getStrPortID());
                        for (int k = 0; k < nPortLen; k++) {
                            if (ObjectIdentifier.equalsWithValue(tmpStartDurable.get(0).getStartDurablePort().getLoadPortID(),
                                    strPortGroupSeq.get(j).getStrPortID().get(k).getPortID())){
                                portGroupID = strPortGroupSeq.get(j).getPortGroup();
                                bFoundFlag = true;
                                break;
                            }
                        }
                        if (CimBooleanUtils.isTrue(bFoundFlag)){
                            break;
                        }
                    }

                    //Set Operation info
                    log.info("Set Opertiion Info");
                    for (int m = 0; m < attrLen; m++) {
                        if (ObjectIdentifier.equalsWithValue(tmpStartDurable.get(0).getDurableId(),
                                durableWhatNextInqResult.getStrWhatNextDurableAttributes().get(m).getDurableID())){
                            Infos.StartOperationInfo startOperationInfo = new Infos.StartOperationInfo();
                            startDurable.setStartOperationInfo(startOperationInfo);
                            startOperationInfo.setProcessFlowID(durableWhatNextInqResult.getStrWhatNextDurableAttributes().get(m).getRouteID());
                            startOperationInfo.setOperationID(durableWhatNextInqResult.getStrWhatNextDurableAttributes().get(m).getOperationID());
                            startOperationInfo.setOperationNumber(durableWhatNextInqResult.getStrWhatNextDurableAttributes().get(m).getOperationNumber());
                            break;
                        }
                    }
                    log.info("Finished tmpStartDurable Info  ..<<break!! WhatNextInfo Loop>>");
                    break;
                }
                //It is finished if Loop of WhatNextInfo finishes turning twice.
                if (i >= attrLen -1 ){
                    nWIPLotLoopEndCnt++;
                    log.info("Turned WhatNextInfo Loop!!  Therefore Count It");
                    if (1 < nWIPLotLoopEndCnt){
                        log.info("Turned <<Twice>> WhatNextInfo Loop!!   Therefore ..<<break!! WhatNextInfo Loop>>");
                        break;
                    }
                }
                //Set Next Index of durableID loop
                startNo = i + 1;
                log.info("make strStartDurable Process  done.");
                if (CimArrayUtils.getSize(tmpStartDurable) == 0 || ObjectIdentifier.isEmptyWithValue(tmpStartDurable.get(0).getDurableId())){
                    log.info("tmpStartDurable[0].durableID.identifier is null  ..<<continue>>");
                    continue;
                }

                //get start recipe
                log.info("step4 - processStartDurablesReserveInformationGetBaseInfoForClient");
                Outputs.ProcessStartDurablesReserveInformationGetBaseInfoForClientOut baseInfoForClientOut = null;
                try {
                    baseInfoForClientOut = processForDurableMethod.processStartDurablesReserveInformationGetBaseInfoForClient(objCommonIn,
                            equipmentID,
                            durableWhatNextInqResult.getStrWhatNextDurableAttributes().get(i).getDurableCategory(),
                            Arrays.asList(tmpStartDurable.get(0).getDurableId()));
                } catch (ServiceException e) {
                    log.error("processStartDurablesReserveInformationGetBaseInfoForClient is Not OK   ..<<continue>>");
                    continue;
                }
                Validations.check(null == baseInfoForClientOut || null == baseInfoForClientOut.getStrDurableStartRecipe(),retCodeConfig.getInvalidRecipeForEqp());

                /* *********************************************************************************/
                /*                                                                                */
                /*   Check multiRecipeCapability and multiLotType                                 */
                /*   and Decide finally to put it in StartDurable                                */
                /*                                                                                */
                /* *********************************************************************************/
                log.info("Check multiRecipeCapability and multiLotType");
                //----------------------------------------------------------------------
                //      Equipment's         Cassette's
                // multiRecipeCapability    multiLotType    Same Recipe Check
                // =====================================================================
                // M-Recipe                 SL-SR           FALSE
                //                          ML-SR           FALSE
                //                          ML-MR           FALSE
                // -----------------------  --------------------------------------------
                // S-Recipe                 SL-SR           TRUE
                //                          ML-SR           TRUE
                //                          ML-MR           TRUE
                // -----------------------  --------------------------------------------
                // Batch                    SL-SR           TRUE
                //                          ML-SR           TRUE
                //                          ML-MR           Error
                // -----------------------  --------------------------------------------
                if (CimStringUtils.equals(BizConstant.SP_EQP_MULTIRECIPECAPABILITY_MULTIPLERECIPE,multiRecipeCapability)){
                    // M-Recipe: SL-SR, ML-SR, ML-MR
                    // Same Recipe Check : FALSE
                    log.info("M-Recipe: SL-SR, ML-SR, ML-MR");
                    log.info("Same Recipe Check : FALSE");
                }else if (CimStringUtils.equals(BizConstant.SP_EQP_MULTIRECIPECAPABILITY_BATCH,multiRecipeCapability)
                        && CimStringUtils.equals(BizConstant.SP_CAS_MULTILOTTYPE_MULTILOTMULTIRECIPE,multiLotType)){
                    // Batch and ML-MR
                    // Error
                    log.error("Batch and ML-MR: continue");
                    continue;
                }else {
                    // S-Recipe: SL-SR, ML-SR, ML-MR
                    // Batch   : SL-SR, ML-SR
                    // Same Recipe Check : TRUE
                    log.info("S-Recipe: SL-SR, ML-SR, ML-MR");
                    log.info("Batch   : SL-SR, ML-SR");
                    log.info("Same Recipe Check : TRUE");
                    if (processRunCount == 0){
                        log.info("processRunCount == 0");
                        /*------------------------------------------------------------*/
                        /*   Set Base Recipe (First operationStartFlag=TRUE Recipe)   */
                        /*------------------------------------------------------------*/
                        baseLogicalRecipeID = tmpBaseLogicalRecipeID;
                        baseMachineRecipeID = tmpBaseMachineRecipeID;

                        if (ObjectIdentifier.isEmptyWithValue(baseLogicalRecipeID)
                                || ObjectIdentifier.isEmptyWithValue(baseMachineRecipeID)){
                            log.info(" Can not Found Base Recipe!!");
                        }
                    }else {
                        if (ObjectIdentifier.isEmptyWithValue(tmpBaseLogicalRecipeID)
                                || ObjectIdentifier.isEmptyWithValue(tmpBaseMachineRecipeID)){
                            log.info(" Can not Found Base Recipe!!");
                        }
                        if (ObjectIdentifier.equalsWithValue(baseLogicalRecipeID, tmpBaseLogicalRecipeID)
                                && ObjectIdentifier.equalsWithValue(baseMachineRecipeID, tmpBaseMachineRecipeID)){
                        }else {
                            log.info("Deferent Recipe.  ...<<<continue>>>");
                            continue;
                        }
                    }
                }

                //check strStartCassette Process
                log.info("check strStartCassette Process");

                //change loadSeqNo to check conditon
                long saveLoadSequenceNumber = tmpStartDurable.get(0).getStartDurablePort().getLoadSequenceNumber();
                tmpStartDurable.get(0).getStartDurablePort().setLoadSequenceNumber(1L);

                //cassette_CheckConditionForOperation
                log.info("step5 - durableCheckConditionForOperation");
                Inputs.ObjDurableCheckConditionForOperationIn conditionForOperationIn = new Inputs.ObjDurableCheckConditionForOperationIn();
                conditionForOperationIn.setOperation(BizConstant.SP_OPERATION_STARTRESERVATION);
                conditionForOperationIn.setEquipmentId(equipmentID);
                conditionForOperationIn.setDurableCategory(durableWhatNextInqResult.getStrWhatNextDurableAttributes().get(i).getDurableCategory());
                conditionForOperationIn.setStartDurables(tmpStartDurable);
                conditionForOperationIn.setDurableStartRecipe(baseInfoForClientOut.getStrDurableStartRecipe());
                try {
                    durableMethod.durableCheckConditionForOperation(objCommonIn, conditionForOperationIn);
                }catch (ServiceException e) {
                    log.error("durableCheckConditionForOperation() is Not OK   ..<<continue>>");
                    continue;
                }

                //revert loadSeqNo
                tmpStartDurable.get(0).getStartDurablePort().setLoadSequenceNumber(saveLoadSequenceNumber);

                log.info("step6 - equipmentAndPortStateCheckForDurableOperation");
                Inputs.ObjEquipmentAndPortStateCheckForDurableOperationIn checkForDurableOperationIn = new Inputs.ObjEquipmentAndPortStateCheckForDurableOperationIn();
                checkForDurableOperationIn.setOperation(BizConstant.SP_OPERATION_STARTRESERVATION);
                checkForDurableOperationIn.setEquipmentId(equipmentID);
                checkForDurableOperationIn.setPortGroupId(portGroupID);
                checkForDurableOperationIn.setDurableCategory(durableWhatNextInqResult.getStrWhatNextDurableAttributes().get(i).getDurableCategory());
                checkForDurableOperationIn.setStartDurables(tmpStartDurable);
                try {
                    equipmentMethod.equipmentAndPortStateCheckForDurableOperation(objCommonIn, checkForDurableOperationIn);
                } catch (ServiceException e) {
                    log.error("equipmentAndPortStateCheckForDurableOperation() is Not OK   ..<<continue>>");
                    continue;
                }

                log.info("Add tmpStartDurable to strStartDurable");
                useAssignEmptyDurablePortSeq.add(nAssignEmptyCassetteCnt,tmpStartDurable.get(0).getStartDurablePort().getLoadPortID());
                useEmptyDurableIDSeq.add(nAssignEmptyCassetteCnt,tmpStartDurable.get(0).getDurableId());
                nAssignEmptyCassetteCnt++;
                for (int j = 0; j < CimArrayUtils.getSize(tmpStartDurable); j++) {
                    strStartDurable.add(nSetStartDurableCnt, tmpStartDurable.get(j));
                    nSetStartDurableCnt++;
                }

                processRunCount++;
            }
            //If selected Carrier does not fulfill MinBatchSize, they are omitted from candidate.
            if (CimBooleanUtils.isTrue(bCheckMinBatchSize) && processRunCount > 0 && processRunCount < equipmentProcessBatchConditionRetCode.getMinBatchSize()){
                for (int m = 0; m < nSetStartDurableCnt; m++) {
                    omitDurableSeq.add(strStartDurable.get(m).getDurableId());
                }
            }else {
                break;
            }
        }
        // Final Check
        //Check processRunCount
        Validations.check (processRunCount == 0, retCodeConfigEx.getNotFoundFilledDurable());
        Validations.check(processRunCount < equipmentProcessBatchConditionRetCode.getMinBatchSize(), retCodeConfigEx.getNotFoundFilledDurable());

        //Set Return Struct
        return strStartDurable;
    }

    @Override
    public List<Infos.StartDurable> whatNextDurableListToStartDurableForDeliveryForInternalBufferReq(Infos.ObjCommon objCommonIn, ObjectIdentifier equipmentID, Outputs.EquipmentTargetPortPickupOut strEquipmentTargetPortPickupOut, Results.WhatNextDurableListInqResult strWhatNextDurableListForInternalBufferInqResult, boolean bEqpInternalBufferInfo, List<Infos.EqpInternalBufferInfo> strEqpInternalBufferInfoSeq) {

        int i,j;
        /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-*/
        /*                                                                                                            */
        /*                                                                                                            */
        /*                                                                                                            */
        /*   Check Condition and Get Information                                                                      */
        /*                                                                                                            */
        /*                                                                                                            */
        /*                                                                                                            */
        /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-*/
        /*----------------------------*/
        /*   Check Target Port Info   */
        /*----------------------------*/
        Validations.check (!CimStringUtils.equals(BizConstant.SP_PORTRSC_PORTSTATE_LOADREQ,strEquipmentTargetPortPickupOut.getTargetPortType()), retCodeConfig.getNotFoundTargetPort());
        int nCanBeUsedPortCount = CimArrayUtils.getSize(strEquipmentTargetPortPickupOut.getEqpTargetPortInfo().getPortGroups().get(0).getStrPortID());

        /*---------------------------------------------*/
        /*   Get Equipment's Process Batch Condition   */
        /*---------------------------------------------*/
        log.info("Get Equipment's Process Batch Condition");
        //step1 - equipmentProcessBatchConditionGet
        log.info("step1 - equipmentProcessBatchConditionGet");
        Outputs.ObjEquipmentProcessBatchConditionGetOut strEquipmentProcessBatchConditionGetOut = equipmentMethod.equipmentProcessBatchConditionGet(objCommonIn, equipmentID);

        /*----------------------------------*/
        /*   Get Eqp Internal Buffer Info   */
        /*----------------------------------*/
        log.info("Get Eqp Internal Buffer Info");
        List<Infos.EqpInternalBufferInfo> strEquipmentInternalBufferInfoGetOut = null;
        if (CimBooleanUtils.isFalse(bEqpInternalBufferInfo)){
            //step2 - equipmentInternalBufferInfoGet
            log.info("step2 - equipmentInternalBufferInfoGet");
            strEquipmentInternalBufferInfoGetOut = equipmentMethod.equipmentInternalBufferInfoGet(objCommonIn, equipmentID);
        }else {
            log.info("TRUE == bEqpInternalBufferInfo");
            strEquipmentInternalBufferInfoGetOut = strEqpInternalBufferInfoSeq;
        }
        /*------------------------------------*/
        /*   Get Internal Buffer Free Shelf   */
        /*------------------------------------*/
        log.info("Get Internal Buffer Free Shelf");
        //step3 - equipmentShelfSpaceForInternalBufferGet
        log.info("step3 - equipmentShelfSpaceForInternalBufferGet");
        Infos.EquipmentShelfSpaceForInternalBufferGet equipmentShelfSpaceForInternalBufferGetResult = equipmentMethod.equipmentShelfSpaceForInternalBufferGet(objCommonIn, equipmentID, true, strEquipmentInternalBufferInfoGetOut);

        /*-------------------------------------------*/
        /*   Get Equipment's MultiRecipeCapability   */
        /*-------------------------------------------*/
        log.info("Get Equipment's MultiRecipeCapability");
        CimMachine aMachine = baseCoreFactory.getBO(CimMachine.class, equipmentID);
        Validations.check (CimObjectUtils.isEmpty(aMachine),  retCodeConfig.getNotFoundEqp());
        String multiRecipeCapability = aMachine.getMultipleRecipeCapability();

        /*------------------------------------------*/
        /*   Get Check Equipment's ProcessRunSize   */
        /*------------------------------------------*/
        log.info("Get Check Equipment's ProcessRunSize");
        int processRunCount = 0;
        Long processRunSizeMaximum = strWhatNextDurableListForInternalBufferInqResult.getProcessRunSizeMaximum();
        if (processRunSizeMaximum > equipmentShelfSpaceForInternalBufferGetResult.getOtherSpace()){
            log.info("processRunSizeMaximum > equipmentShelfSpaceForInternalBufferGetResult.getOtherSpace()");
            processRunSizeMaximum = equipmentShelfSpaceForInternalBufferGetResult.getOtherSpace();
        }
        Validations.check (0 == processRunSizeMaximum, retCodeConfig.getNotSpaceEqpSelf(),BizConstant.SP_LOADPURPOSETYPE_OTHER);
        int attrLen = CimArrayUtils.getSize(strWhatNextDurableListForInternalBufferInqResult.getStrWhatNextDurableAttributes());
        Validations.check (0 == attrLen, retCodeConfigEx.getNotFoundFilledDurable());

        /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-*/
        /*                                                                                                            */
        /*                                                                                                            */
        /*                                                                                                            */
        /*   Make strStartDurable Process                                                                            */
        /*                                                                                                            */
        /*                                                                                                            */
        /*                                                                                                            */
        /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-*/
        log.info("make strStartCassette Process ");
        List<Infos.StartDurable> strStartDurable = new ArrayList<>();
        boolean bCheckMinBatchSize = false;
        String checkMinBatchSize = StandardProperties.OM_DELIVERY_CHK_MINIMUM_BATCH_SIZE_ENABLE.getValue();
        if (CimStringUtils.equals(checkMinBatchSize,"1") && 1 < strEquipmentProcessBatchConditionGetOut.getMinBatchSize()){
            log.info("bCheckMinBatchSize is 1");
            bCheckMinBatchSize = true;
        }
        int nSetStartDurableCnt = 0;
        List<ObjectIdentifier> omitDurableSeq = new ArrayList<>();
        while (true){
            // MinBatchSize support loop.
            // Reset variable.
            processRunCount = 0;
            String multiLotType = null;
            int startNo = 0;
            Boolean bWhileExitFlag = false;
            ObjectIdentifier baseLogicalRecipeID = new ObjectIdentifier();
            ObjectIdentifier baseMachineRecipeID = new ObjectIdentifier();
            ObjectIdentifier tmpBaseLogicalRecipeID = new ObjectIdentifier();
            ObjectIdentifier tmpBaseMachineRecipeID = new ObjectIdentifier();

            while (processRunCount < processRunSizeMaximum){
                log.info("while ( processRunCount < processRunSizeMaximum ) --------------------------");
                log.info("processRunCount = {}",processRunCount);
                if (CimBooleanUtils.isTrue(bWhileExitFlag)){
                    //end of loop
                    log.info("bWhileExitFlag == TRUE    break!!");
                    break;
                }
                Boolean bTmpBaseRecipeFlag = false;
                List<Infos.StartDurable> tmpStartDurable = new ArrayList<>();
                /* *********************************************************************************/
                /*                                                                                */
                /*   Set Cassette Info                                                            */
                /*                                                                                */
                /* *********************************************************************************/
                log.info("Set Cassette Info");
                for (i = startNo; i < attrLen; i++) {
                    /*-----------------------------*/
                    /*   Omit CassetteID is NULL   */
                    /*-----------------------------*/
                    if (ObjectIdentifier.isEmptyWithValue(strWhatNextDurableListForInternalBufferInqResult.getStrWhatNextDurableAttributes().get(i).getDurableID())){
                        log.info("cassetteID is NULL  ...<<<continue>>>");
                        continue;
                    }
                    Boolean bOmitDurable = false;
                    int lenOmitDrb = CimArrayUtils.getSize(omitDurableSeq);
                    for (int m = 0; m < lenOmitDrb; m++) {
                        if (ObjectIdentifier.equalsWithValue(omitDurableSeq.get(m),
                                strWhatNextDurableListForInternalBufferInqResult.getStrWhatNextDurableAttributes().get(i).getDurableID())){
                            bOmitDurable = true;
                            break;
                        }
                    }
                    if (CimBooleanUtils.isTrue(bOmitDurable)){
                        log.info("Omit DurableID   ..<<continue>>");
                        continue;
                    }
                    /*------------------------------------------*/
                    /*   Omit already saved strStartCassette    */
                    /*------------------------------------------*/
                    Boolean bFoundFlag = false;
                    for (j = 0; j < processRunCount; j++) {
                        if (ObjectIdentifier.equalsWithValue(strWhatNextDurableListForInternalBufferInqResult.getStrWhatNextDurableAttributes().get(i).getDurableID(),
                                strStartDurable.get(j).getDurableId())){
                            log.info("strWhatNextDurableListForInternalBufferInqResult.getStrWhatNextDurableAttributes().get(i).getDurableID() == strStartDurable.get(j).getDurableId()  ...<<<continue>>>");
                            bFoundFlag = true;
                            break;
                        }
                    }
                    if (CimBooleanUtils.isTrue(bFoundFlag)){
                        log.info("already saved strStartDurable  ...<<<continue>>>");
                        continue;
                    }
                    if (CimArrayUtils.getSize(tmpStartDurable) != 0){
                        if (!ObjectIdentifier.equalsWithValue(strWhatNextDurableListForInternalBufferInqResult.getStrWhatNextDurableAttributes().get(i).getDurableID(),
                                tmpStartDurable.get(0).getDurableId())){
                            startNo = i;
                            log.info("Next for-loop StartNo.= {}",startNo);
                            break;
                        }else {
                            log.info("strWhatNextDurableListForInternalBufferInqResult.getStrWhatNextDurableAttributes().get(i).getDurableID() == tmpStartDurable.get(0).getDurableId()  ...<<<continue>>>");
                            continue;
                        }
                    }
                    /*----------------------------------------------------*/
                    /*   Check Stocker which Lot belongs to, Available?   */
                    /*----------------------------------------------------*/
                    log.info("Call equipmentCheckAvail()   stockerID --->{}", ObjectIdentifier.fetchValue(strWhatNextDurableListForInternalBufferInqResult.getStrWhatNextDurableAttributes().get(i).getDurableID()));
                    //step4 - equipmentCheckAvail
                    log.info("step4 - equipmentCheckAvail");
                    try {
                        equipmentMethod.equipmentCheckAvail(objCommonIn, strWhatNextDurableListForInternalBufferInqResult.getStrWhatNextDurableAttributes().get(i).getStockerID());
                    } catch (ServiceException e) {
                        log.info("equipmentCheckAvail is Not OK   ..<<continue>>");
                        continue;
                    }
                    /*-------------------------------------------------------------------------------*/
                    /*   Check Category for Copper/Non Copper                                        */
                    /*                                                                               */
                    /*   (*) Because it is all same, CarrierCategory of Port set the first PortID.   */
                    /*-------------------------------------------------------------------------------*/
                    log.info("Check Category for Copper/Non Copper");
                    log.info("step5 - durableCassetteCategoryCheckForContaminationControl");
                    try {
                        durableMethod.durableCassetteCategoryCheckForContaminationControl(objCommonIn,
                                strWhatNextDurableListForInternalBufferInqResult.getStrWhatNextDurableAttributes().get(i).getDurableID(),
                                equipmentID,
                                strEquipmentTargetPortPickupOut.getEqpTargetPortInfo().getPortGroups().get(0).getStrPortID().get(0).getPortID());
                    } catch (ServiceException e) {
                        log.error("UnMatch DurableCategory (Copper/NonCopper)   ..<<continue>>");
                        continue;
                    }

                    Infos.StartDurable startDurable = new Infos.StartDurable();
                    tmpStartDurable.add(startDurable);
                    startDurable.setDurableId(strWhatNextDurableListForInternalBufferInqResult.getStrWhatNextDurableAttributes().get(i).getDurableID());
                    /*-------------------------------*/
                    /*   Set Temporary Base Recipe   */
                    /*-------------------------------*/
                    log.info("Set Temporary Base Recipe");
                    if (CimBooleanUtils.isFalse(bTmpBaseRecipeFlag)){
                        //Temporary Base Recipe
                        log.info("bTmpBaseRecipeFlag == FALSE");
                        tmpBaseLogicalRecipeID = strWhatNextDurableListForInternalBufferInqResult.getStrWhatNextDurableAttributes().get(i).getLogicalRecipeID();
                        tmpBaseMachineRecipeID = strWhatNextDurableListForInternalBufferInqResult.getStrWhatNextDurableAttributes().get(i).getMachineRecipeID();
                        bTmpBaseRecipeFlag = true;
                    }
                    /*------------------------*/
                    /*   Set Port Info Other  */
                    /*------------------------*/
                    log.info("Set Pot Info");
                    Infos.StartDurablePort startDurablePort = new Infos.StartDurablePort();
                    startDurable.setStartDurablePort(startDurablePort);
                    startDurablePort.setLoadSequenceNumber(0L);
                    startDurablePort.setLoadPurposeType(BizConstant.SP_LOADPURPOSETYPE_OTHER);
                    startDurablePort.setLoadPortID(strEquipmentTargetPortPickupOut.getEqpTargetPortInfo().getPortGroups().get(0).getStrPortID().get(0).getPortID());

                    /*------------------------*/
                    /*   Set Operation Info   */
                    /*------------------------*/
                    log.info("Set Operation Info");
                    for (int m = 0; m < attrLen; m++) {
                        if (ObjectIdentifier.equalsWithValue(tmpStartDurable.get(0).getDurableId(),
                                strWhatNextDurableListForInternalBufferInqResult.getStrWhatNextDurableAttributes().get(m).getDurableID())){
                            Infos.StartOperationInfo startOperationInfo = new Infos.StartOperationInfo();
                            startDurable.setStartOperationInfo(startOperationInfo);
                            startOperationInfo.setProcessFlowID(strWhatNextDurableListForInternalBufferInqResult.getStrWhatNextDurableAttributes().get(m).getRouteID());
                            startOperationInfo.setOperationID(strWhatNextDurableListForInternalBufferInqResult.getStrWhatNextDurableAttributes().get(m).getOperationID());
                            startOperationInfo.setOperationNumber(strWhatNextDurableListForInternalBufferInqResult.getStrWhatNextDurableAttributes().get(m).getOperationNumber());
                            break;
                        }
                    }
                }
                if (i == attrLen){
                    log.info("i == attLen");
                    //end of loop
                    bWhileExitFlag = true;
                    i--;
                }
                if (CimArrayUtils.getSize(tmpStartDurable) == 0 || ObjectIdentifier.isEmptyWithValue(tmpStartDurable.get(0).getDurableId())){
                    log.info("tmpStartDurable.get(0).getDurableId() is null  ..<<continue>>");
                    continue;
                }
                //check start recipe
                log.info("step4 - processStartDurablesReserveInformationGetBaseInfoForClient");
                Outputs.ProcessStartDurablesReserveInformationGetBaseInfoForClientOut baseInfoForClientOut = null;
                try {
                    baseInfoForClientOut = processForDurableMethod.processStartDurablesReserveInformationGetBaseInfoForClient(objCommonIn,
                            equipmentID,
                            strWhatNextDurableListForInternalBufferInqResult.getStrWhatNextDurableAttributes().get(i).getDurableCategory(),
                            Arrays.asList(tmpStartDurable.get(0).getDurableId()));
                } catch (ServiceException e) {
                    log.error("processStartDurablesReserveInformationGetBaseInfoForClient is Not OK   ..<<continue>>");
                    continue;
                }
                Validations.check(null == baseInfoForClientOut || null == baseInfoForClientOut.getStrDurableStartRecipe(),retCodeConfig.getInvalidRecipeForEqp());

                /* *********************************************************************************/
                /*                                                                                */
                /*   check strStartCassette Process                                               */
                /*                                                                                */
                /* *********************************************************************************/
                log.info("check strStartCassette Process");
                /*-----------------------------------------------------------------------*/
                /*                                                                       */
                /*   Check Process for Cassette                                          */
                /*                                                                       */
                /*-----------------------------------------------------------------------*/
                //step6 - durableCheckConditionForOperationForInternalBuffer
                log.info("step6 - durableCheckConditionForOperationForInternalBuffer");
                Inputs.ObjDurableCheckConditionForOperationIn conditionForOperationIn = new Inputs.ObjDurableCheckConditionForOperationIn();
                conditionForOperationIn.setOperation(BizConstant.SP_OPERATION_STARTRESERVATION);
                conditionForOperationIn.setEquipmentId(equipmentID);
                conditionForOperationIn.setDurableCategory(strWhatNextDurableListForInternalBufferInqResult.getStrWhatNextDurableAttributes().get(i).getDurableCategory());
                conditionForOperationIn.setStartDurables(tmpStartDurable);
                conditionForOperationIn.setDurableStartRecipe(baseInfoForClientOut.getStrDurableStartRecipe());
                try {
                    durableMethod.durableCheckConditionForOperationForInternalBuffer(objCommonIn,conditionForOperationIn);
                }catch (ServiceException e) {
                    log.info("durableCheckConditionForOperationForInternalBuffer() is Not OK  ...<<<continue>>>");
                    continue;
                }

                /*-----------------------------------------------------------------------------*/
                /*                                                                             */
                /*   Check Equipment Port for Start Reservation                                */
                /*                                                                             */
                /*-----------------------------------------------------------------------------*/
                //step7 - equipmentAndPortStateCheckForDurableOperationForInternalBuffer
                log.info("step7 - equipmentAndPortStateCheckForDurableOperationForInternalBuffer()");
                Inputs.ObjEquipmentAndPortStateCheckForDurableOperationIn checkForDurableOperationIn = new Inputs.ObjEquipmentAndPortStateCheckForDurableOperationIn();
                checkForDurableOperationIn.setOperation(BizConstant.SP_OPERATION_STARTRESERVATION);
                checkForDurableOperationIn.setEquipmentId(equipmentID);
                checkForDurableOperationIn.setPortGroupId(strEquipmentTargetPortPickupOut.getEqpTargetPortInfo().getPortGroups().get(0).getPortGroup());
                checkForDurableOperationIn.setDurableCategory(strWhatNextDurableListForInternalBufferInqResult.getStrWhatNextDurableAttributes().get(i).getDurableCategory());
                checkForDurableOperationIn.setStartDurables(tmpStartDurable);
                try {
                    equipmentMethod.equipmentAndPortStateCheckForDurableOperationForInternalBuffer(objCommonIn,checkForDurableOperationIn);
                } catch (ServiceException e) {
                    log.info("equipmentAndPortStateCheckForDurableOperationForInternalBuffer()  is Not OK  ...<<<continue>>>");
                    continue;
                }
                /* *********************************************************************************/
                /*                                                                                */
                /*   Check multiRecipeCapability and multiLotType                                 */
                /*   and Decide finally to put it in StartDurable                                */
                /*                                                                                */
                /* *********************************************************************************/
                log.info("Check multiRecipeCapability and multiLotType");
                //----------------------------------------------------------------------
                //      Equipment's         Cassette's
                // multiRecipeCapability    multiLotType    Same Recipe Check
                // =====================================================================
                // M-Recipe                 SL-SR           FALSE
                //                          ML-SR           FALSE
                //                          ML-MR           FALSE
                // -----------------------  --------------------------------------------
                // S-Recipe                 SL-SR           TRUE
                //                          ML-SR           TRUE
                //                          ML-MR           TRUE
                // -----------------------  --------------------------------------------
                // Batch                    SL-SR           TRUE
                //                          ML-SR           TRUE
                //                          ML-MR           Error
                // -----------------------  --------------------------------------------
                if (CimStringUtils.equals(BizConstant.SP_EQP_MULTIRECIPECAPABILITY_MULTIPLERECIPE,multiRecipeCapability)){
                    // M-Recipe: SL-SR, ML-SR, ML-MR
                    // Same Recipe Check : FALSE
                    log.info("M-Recipe: SL-SR, ML-SR, ML-MR");
                    log.info("Same Recipe Check : FALSE");
                }else if (CimStringUtils.equals(BizConstant.SP_EQP_MULTIRECIPECAPABILITY_BATCH,multiRecipeCapability)
                        && CimStringUtils.equals(BizConstant.SP_CAS_MULTILOTTYPE_MULTILOTMULTIRECIPE,multiLotType)){
                    // Batch and ML-MR
                    // Error
                    log.error("Batch and ML-MR: continue");
                    continue;
                }else {
                    // S-Recipe: SL-SR, ML-SR, ML-MR
                    // Batch   : SL-SR, ML-SR
                    // Same Recipe Check : TRUE
                    log.info("S-Recipe: SL-SR, ML-SR, ML-MR");
                    log.info("Batch   : SL-SR, ML-SR");
                    log.info("Same Recipe Check : TRUE");
                    if (processRunCount == 0){
                        log.info("processRunCount == 0");
                        /*------------------------------------------------------------*/
                        /*   Set Base Recipe (First operationStartFlag=TRUE Recipe)   */
                        /*------------------------------------------------------------*/
                        baseLogicalRecipeID = tmpBaseLogicalRecipeID;
                        baseMachineRecipeID = tmpBaseMachineRecipeID;

                        if (ObjectIdentifier.isEmptyWithValue(baseLogicalRecipeID)
                                || ObjectIdentifier.isEmptyWithValue(baseMachineRecipeID)){
                            log.info(" Can not Found Base Recipe!!");
                        }
                    }else {
                        if (ObjectIdentifier.isEmptyWithValue(tmpBaseLogicalRecipeID)
                                || ObjectIdentifier.isEmptyWithValue(tmpBaseMachineRecipeID)){
                            log.info(" Can not Found Base Recipe!!");
                        }
                        if (ObjectIdentifier.equalsWithValue(baseLogicalRecipeID, tmpBaseLogicalRecipeID)
                                && ObjectIdentifier.equalsWithValue(baseMachineRecipeID, tmpBaseMachineRecipeID)){
                        }else {
                            log.info("Deferent Recipe.  ...<<<continue>>>");
                            continue;
                        }
                    }
                }
                log.info("Add tmpStartDurable to strStartDurable");
                for (j = 0; j < CimArrayUtils.getSize(tmpStartDurable); j++) {
                    strStartDurable.add(nSetStartDurableCnt, tmpStartDurable.get(j));
                    nSetStartDurableCnt++;
                }

                processRunCount++;
            }
            /*-----------------------------------------------------------------------------------------*/
            /*   If selected Carrier does not fulfill MinBatchSize, they are omitted from candidate.   */
            /*-----------------------------------------------------------------------------------------*/
            if (CimBooleanUtils.isTrue(bCheckMinBatchSize)
                    && 0 < processRunCount && processRunCount < strEquipmentProcessBatchConditionGetOut.getMinBatchSize()){
                log.info("processRunCount < minBatchSize");
                for (int m = 0; m < processRunCount; m++) {
                    int lenOmitDrb = CimArrayUtils.getSize(omitDurableSeq);
                    omitDurableSeq.add(lenOmitDrb,strStartDurable.get(m).getDurableId());
                }
            }else {
                log.info("MinBatchSize support loop");
                break;
            }
        }
        /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-*/
        /*                                                                                                            */
        /*                                                                                                            */
        /*                                                                                                            */
        /*   Final Check                                                                                              */
        /*                                                                                                            */
        /*                                                                                                            */
        /*                                                                                                            */
        /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-*/
        log.info("Final Check  ");
        /*---------------------------*/
        /*   Check ProcessRunCount   */
        /*---------------------------*/
        log.info("Check ProcessRunCount");
        Validations.check (processRunCount == 0, retCodeConfigEx.getNotFoundFilledDurable());
        /*------------------------------------------------------*/
        /*   Check MinimumBatchSize of Equipment                */
        /*       The check of MaxBatchSize is done in the top   */
        /*------------------------------------------------------*/
        log.info("Check MinimumBatchSize of Equipment The check of MaxBatchSize is done in the top");
        Validations.check (processRunCount < strEquipmentProcessBatchConditionGetOut.getMinBatchSize(),  retCodeConfig.getInvalidProcessBatchCount(),processRunCount,strEquipmentProcessBatchConditionGetOut.getMaxBatchSize(),strEquipmentProcessBatchConditionGetOut.getMinBatchSize());

        /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-*/
        /*                                                                                                            */
        /*                                                                                                            */
        /*                                                                                                            */
        /*   Make Output Struct                                                                                       */
        /*                                                                                                            */
        /*                                                                                                            */
        /*                                                                                                            */
        /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-*/
        log.info("Make Output Struct");
        /*--------------------------------------------------------*/
        /*   Set LoadPortID of StartDurable.                     */
        /*--------------------------------------------------------*/
        log.info("Set LoadPortID of StartDurable. ");
        int nSetPortIdx = 0;
        for (i = 0; i < processRunCount; i++) {
            //Set PortID by the rotation
            strStartDurable.get(i).getStartDurablePort().setLoadPortID(strEquipmentTargetPortPickupOut.getEqpTargetPortInfo().getPortGroups().get(0).getStrPortID().get(nSetPortIdx).getPortID());
            nSetPortIdx++;
            if (nSetPortIdx > nCanBeUsedPortCount - 1){
                nSetPortIdx = 0;
            }
        }

        /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-*/
        /*                                                                                                            */
        /*                                                                                                            */
        /*                                                                                                            */
        /*   Set Return Struct                                                                                        */
        /*                                                                                                            */
        /*                                                                                                            */
        /*                                                                                                            */
        /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-*/
        log.info("Set Return Struct");
        /*----------------------*/
        /*   Return to Caller   */
        /*----------------------*/
        return strStartDurable;
    }
}
