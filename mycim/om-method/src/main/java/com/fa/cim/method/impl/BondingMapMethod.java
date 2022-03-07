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
import com.fa.cim.dto.Outputs;
import com.fa.cim.entity.nonruntime.CimBondGroupMapDO;
import com.fa.cim.jpa.CimJpaRepository;
import com.fa.cim.method.IBondingMapMethod;
import com.fa.cim.method.ILotMethod;
import com.fa.cim.method.IProcessMethod;
import com.fa.cim.newcore.bo.machine.CimMachine;
import com.fa.cim.newcore.bo.pd.CimProcessDefinition;
import com.fa.cim.newcore.bo.pd.CimProcessOperation;
import com.fa.cim.newcore.bo.person.CimPerson;
import com.fa.cim.newcore.bo.prodspec.CimProductSpecification;
import com.fa.cim.newcore.bo.product.CimControlJob;
import com.fa.cim.newcore.bo.product.CimLot;
import com.fa.cim.newcore.bo.product.CimWafer;
import com.fa.cim.newcore.bo.recipe.CimLogicalRecipe;
import com.fa.cim.newcore.bo.recipe.CimMachineRecipe;
import com.fa.cim.newcore.dto.product.ProductDTO;
import com.fa.cim.newcore.factory.BaseCoreFactory;
import com.fa.cim.newcore.impl.bo.env.StandardProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;

import java.util.*;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/10/9          ********            Nyx                create file
 *
 * @author Nyx
 * @since 2019/10/9 14:57
 * Copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@OmMethod
@Slf4j
public class BondingMapMethod implements IBondingMapMethod {

    @Autowired
    private RetCodeConfig retCodeConfig;

    @Autowired
    private RetCodeConfigEx retCodeConfigEx;

    @Autowired
    private BaseCoreFactory baseCoreFactory;

    @Autowired
    private ILotMethod lotMethod;

    @Autowired
    private IProcessMethod processMethod;

    @Autowired
    private CimJpaRepository cimJpaRepository;

    @Override
    public void bondingMapInfoConsistencyCheck(Infos.ObjCommon objCommon, List<Infos.BondingMapInfo> strBondingMapInfoSeq, ObjectIdentifier targetEquipmentID, boolean actualTopFlag) {
        CimWafer aBaseWafer, aTopWafer;
        CimLot aBaseLot, aTopLot;
        CimProductSpecification aBaseProdSpec, aTopProdSpec;
        //----------------------------------------------------------------------
        // Check Inputed Bonding Map
        //----------------------------------------------------------------------
        for (int mapCnt = 0, mapAllCnt = strBondingMapInfoSeq.size(); mapCnt < mapAllCnt; mapCnt++) {
            Infos.BondingMapInfo bondingMapInfo = strBondingMapInfoSeq.get(mapCnt);
            //----------------------------------------------------------------------
            // Check Fields Filled in
            //----------------------------------------------------------------------
            ObjectIdentifier infoBaseWaferID = bondingMapInfo.getBaseWaferID();
            Validations.check(ObjectIdentifier.isEmpty(infoBaseWaferID), retCodeConfigEx.getInvalidParmForBondingMapReport(), "Base Wafer ID");
            ObjectIdentifier infoBaseLotID = bondingMapInfo.getBaseLotID();
            Validations.check(ObjectIdentifier.isEmpty(infoBaseLotID), retCodeConfigEx.getInvalidParmForBondingMapReport(), "Base Lot ID");
            ObjectIdentifier infoBaseProductID = bondingMapInfo.getBaseProductID();
            Validations.check(ObjectIdentifier.isEmpty(infoBaseProductID), retCodeConfigEx.getInvalidParmForBondingMapReport(), "Base Product ID");
            ObjectIdentifier infoActualTopLotID = bondingMapInfo.getActualTopLotID();
            ObjectIdentifier infoActualTopWaferID = bondingMapInfo.getActualTopWaferID();
            ObjectIdentifier infoPlanTopLotID = bondingMapInfo.getPlanTopLotID();
            ObjectIdentifier infoPlanTopWaferID = bondingMapInfo.getPlanTopWaferID();
            ObjectIdentifier infoActualTopProductID = bondingMapInfo.getActualTopProductID();
            ObjectIdentifier infoPlanTopProductID = bondingMapInfo.getPlanTopProductID();
            String infoBondingProcessState = bondingMapInfo.getBondingProcessState();
            if (actualTopFlag) {
                Validations.check(ObjectIdentifier.isEmpty(infoActualTopWaferID), retCodeConfigEx.getInvalidParmForBondingMapReport(), "Top Wafer ID");
                Validations.check(ObjectIdentifier.isEmpty(infoActualTopLotID), retCodeConfigEx.getInvalidParmForBondingMapReport(), "Top Lot ID");
                Validations.check(ObjectIdentifier.isEmpty(infoActualTopProductID), retCodeConfigEx.getInvalidParmForBondingMapReport(), "Top Product ID");
                Validations.check(CimObjectUtils.isEmpty(infoBondingProcessState), retCodeConfigEx.getInvalidParmForBondingMapReport(), "Bonding Process State");
            } else {
                Validations.check(ObjectIdentifier.isEmpty(infoPlanTopWaferID), retCodeConfigEx.getInvalidParmForBondingMapReport(), "Top Wafer ID");
                Validations.check(ObjectIdentifier.isEmpty(infoPlanTopLotID), retCodeConfigEx.getInvalidParmForBondingMapReport(), "Top Lot ID");
                Validations.check(ObjectIdentifier.isEmpty(infoPlanTopProductID), retCodeConfigEx.getInvalidParmForBondingMapReport(), "Top Product ID");
            }

            //----------------------------------------------------------------------
            // Check Wafer-Lot-Product Consistency
            //----------------------------------------------------------------------
            aBaseWafer = baseCoreFactory.getBO(CimWafer.class, infoBaseWaferID);
            Validations.check(CimObjectUtils.isEmpty(aBaseWafer), retCodeConfig.getNotFoundWafer());
            aTopWafer = baseCoreFactory.getBO(CimWafer.class, actualTopFlag ? infoActualTopWaferID : infoPlanTopWaferID);
            Validations.check(CimObjectUtils.isEmpty(aTopWafer), retCodeConfig.getNotFoundWafer());

            aBaseLot = (CimLot) aBaseWafer.getLot();
            aTopLot = (CimLot) aTopWafer.getLot();
            String baseLotID = !CimObjectUtils.isEmpty(aBaseLot) ? ObjectIdentifier.fetchValue(aBaseLot.getLotID()) : null;
            String topLotID = !CimObjectUtils.isEmpty(aTopLot) ? ObjectIdentifier.fetchValue(aTopLot.getLotID()) : null;

            Validations.check(!ObjectIdentifier.equalsWithValue(infoBaseLotID, baseLotID), retCodeConfig.getLotWaferUnmatch(), infoBaseLotID, infoBaseWaferID);
            Validations.check(actualTopFlag&&!ObjectIdentifier.equalsWithValue(infoActualTopLotID, topLotID), retCodeConfig.getLotWaferUnmatch(), infoActualTopLotID, infoActualTopWaferID);
            Validations.check(!actualTopFlag&&!ObjectIdentifier.equalsWithValue(infoPlanTopLotID, topLotID), retCodeConfig.getLotWaferUnmatch(), infoPlanTopLotID, infoPlanTopWaferID);

            aBaseProdSpec = (CimProductSpecification) aBaseWafer.getProductSpecification();
            aTopProdSpec = (CimProductSpecification) aTopWafer.getProductSpecification();
            String baseProdSpecID = CimObjectUtils.isEmpty(aBaseProdSpec) ? null : ObjectIdentifier.fetchValue(aBaseProdSpec.getProductSpecID());
            String topProdSpecID = CimObjectUtils.isEmpty(aTopProdSpec) ? null : ObjectIdentifier.fetchValue(aTopProdSpec.getProductSpecID());

            Validations.check(!ObjectIdentifier.equalsWithValue(infoBaseProductID, baseProdSpecID), retCodeConfig.getProductSpecUnMatch(), infoBaseProductID, infoBaseWaferID);
            Validations.check(actualTopFlag && !ObjectIdentifier.equalsWithValue(infoActualTopProductID, topProdSpecID), retCodeConfig.getProductSpecUnMatch(), infoActualTopProductID, infoActualTopWaferID);
            Validations.check(!actualTopFlag && !ObjectIdentifier.equalsWithValue(infoPlanTopProductID, topProdSpecID), retCodeConfig.getProductSpecUnMatch(), infoPlanTopProductID, infoPlanTopWaferID);

            //----------------------------------------------------------------------
            // Check Bonding Side Consistency
            //----------------------------------------------------------------------
            Validations.check(!CimStringUtils.equals(bondingMapInfo.getBaseBondingSide(), BizConstant.SP_BONDINGSIDE_BOTTOM) &&
                    !CimStringUtils.equals(bondingMapInfo.getPlanTopBondingSide(), BizConstant.SP_BONDINGSIDE_BOTTOM), retCodeConfig.getInvalidParameter());
            Validations.check(!CimStringUtils.equals(bondingMapInfo.getBaseBondingSide(), BizConstant.SP_BONDINGSIDE_TOP) &&
                    !CimStringUtils.equals(bondingMapInfo.getPlanTopBondingSide(), BizConstant.SP_BONDINGSIDE_TOP), retCodeConfig.getInvalidParameter());

            //----------------------------------------------------------------------
            // Check Bonding Process State
            //----------------------------------------------------------------------
            Validations.check(actualTopFlag &&
                    !CimStringUtils.equals(infoBondingProcessState, BizConstant.SP_BONDINGPROCESSSTATE_COMPLETED) &&
                    !CimStringUtils.equals(infoBondingProcessState, BizConstant.SP_BONDINGPROCESSSTATE_ERROR) &&
                    !CimStringUtils.equals(infoBondingProcessState, BizConstant.SP_BONDINGPROCESSSTATE_UNKNOWN), retCodeConfigEx.getBondmapStateInvalid(), infoBondingProcessState, infoBaseWaferID);

            Validations.check(!actualTopFlag && !CimObjectUtils.isEmpty(infoBondingProcessState), retCodeConfigEx.getBondmapStateInvalid(), infoBondingProcessState, infoBaseWaferID);

            //----------------------------------------------------------------------
            // Check Duplication of wafer
            //----------------------------------------------------------------------
            for (int j = 0; j < mapCnt; j++) {
                Infos.BondingMapInfo bondingMapInfoJ = strBondingMapInfoSeq.get(j);
                Validations.check(ObjectIdentifier.equalsWithValue(infoBaseWaferID, bondingMapInfoJ.getBaseWaferID()), retCodeConfigEx.getBondgrpWaferDuplicate(), infoBaseWaferID);
                Validations.check(actualTopFlag && ObjectIdentifier.equalsWithValue(infoActualTopWaferID, bondingMapInfoJ.getActualTopWaferID()), retCodeConfigEx.getBondgrpWaferDuplicate(), infoActualTopWaferID);
                Validations.check(!actualTopFlag && ObjectIdentifier.equalsWithValue(infoPlanTopWaferID, bondingMapInfoJ.getPlanTopWaferID()), retCodeConfigEx.getBondgrpWaferDuplicate(), infoPlanTopWaferID);
            }
        }

        //----------------------------------------------------------------------
        // Collect uniquely pairs of BaseLot and TopLot
        //----------------------------------------------------------------------
        List<ObjectIdentifier> baseLotIDs = new ArrayList<>(), baseProdIDs = new ArrayList<>(), topLotIDs = new ArrayList<>(), topProdIDs = new ArrayList<>();
        List<String> baseWaferIDs = new ArrayList<>(), topWaferIDs = new ArrayList<>();
        List<Integer> baseWaferNums = new ArrayList<>(), topWaferNums = new ArrayList<>();
        int lotLen = 0;
        int lotCnt = 0;
        for (Infos.BondingMapInfo bondingMapInfo : strBondingMapInfoSeq) {
            ObjectIdentifier baseLotID = bondingMapInfo.getBaseLotID();
            ObjectIdentifier baseProdID = bondingMapInfo.getBaseProductID();
            ObjectIdentifier topLotID = actualTopFlag ? bondingMapInfo.getActualTopLotID() : bondingMapInfo.getPlanTopLotID();
            ObjectIdentifier topProdID = actualTopFlag ? bondingMapInfo.getActualTopProductID() : bondingMapInfo.getPlanTopProductID();

            boolean infoFound = false;
            for (int j = lotCnt; j > 0; j--) {
                if (ObjectIdentifier.equalsWithValue(baseLotID, baseLotIDs.get(j - 1)) &&
                        ObjectIdentifier.equalsWithValue(baseProdID, baseProdIDs.get(j - 1)) &&
                        ObjectIdentifier.equalsWithValue(topLotID, topLotIDs.get(j - 1)) &&
                        ObjectIdentifier.equalsWithValue(topProdID, topProdIDs.get(j - 1))) {
                    infoFound = true;
                    baseWaferNums.set(j - 1, (baseWaferNums.get(j - 1)) + 1);
                    topWaferNums.set(j - 1, (topWaferNums.get(j - 1)) + 1);
                    break;
                }
            }

            if (infoFound) {
                continue;
            }

            baseLotIDs.add(baseLotID);
            baseProdIDs.add(baseProdID);
            topLotIDs.add(topLotID);
            topProdIDs.add(topProdID);
            baseWaferIDs.add(ObjectIdentifier.fetchValue(bondingMapInfo.getBaseWaferID()));
            topWaferIDs.add(actualTopFlag ? ObjectIdentifier.fetchValue(bondingMapInfo.getActualTopWaferID()) : ObjectIdentifier.fetchValue(bondingMapInfo.getPlanTopWaferID()));
            baseWaferNums.add(1);
            topWaferNums.add(1);
            lotCnt++;
        }
        lotLen = lotCnt;

        CimMachine aEqp = null;
        if (!ObjectIdentifier.isEmpty(targetEquipmentID)) {
            aEqp = baseCoreFactory.getBO(CimMachine.class, targetEquipmentID);
        }

        for (lotCnt = 0; lotCnt < lotLen; lotCnt++) {
            aBaseLot = baseCoreFactory.getBO(CimLot.class, baseLotIDs.get(lotCnt));
            aTopLot = baseCoreFactory.getBO(CimLot.class, topLotIDs.get(lotCnt));
            Validations.check(CimObjectUtils.isEmpty(aBaseLot), retCodeConfig.getNotFoundLot());
            Validations.check(CimObjectUtils.isEmpty(aTopLot), retCodeConfig.getNotFoundLot());

            Infos.BondingLotAttributes baseBondingLotAttributes = new Infos.BondingLotAttributes(), topBondingLotAttributes = new Infos.BondingLotAttributes();
            baseBondingLotAttributes.setLotID(aBaseLot.getLotID());
            topBondingLotAttributes.setLotID(aTopLot.getLotID());

            //--------------------------------------------------------------
            // Check Relation of Lot and Product
            //--------------------------------------------------------------
            aBaseProdSpec = aBaseLot.getProductSpecification();
            aTopProdSpec = aTopLot.getProductSpecification();
            baseBondingLotAttributes.setProductID(aBaseProdSpec.getProductSpecID());
            topBondingLotAttributes.setProductID(aTopProdSpec.getProductSpecID());

            Validations.check(ObjectIdentifier.isEmpty(baseBondingLotAttributes.getProductID()) || !ObjectIdentifier.equalsWithValue(baseBondingLotAttributes.getProductID(), baseProdIDs.get(lotCnt)),
                    retCodeConfig.getProductSpecUnMatch(), baseProdIDs.get(lotCnt), baseWaferIDs.get(lotCnt), baseBondingLotAttributes.getProductID());

            Validations.check(ObjectIdentifier.isEmpty(topBondingLotAttributes.getProductID()) || !ObjectIdentifier.equalsWithValue(topBondingLotAttributes.getProductID(), topProdIDs.get(lotCnt)),
                    retCodeConfig.getProductSpecUnMatch(), topProdIDs.get(lotCnt), topWaferIDs.get(lotCnt), topBondingLotAttributes.getProductID());

            if (actualTopFlag) {
                log.info("actualTopFlag == TRUE");

                //-------------------------------------------------------------------
                // There is a case such that not all wafers in Bonding Map.
                // There is a case such that Bonding Operation is previous Operation.
                //-------------------------------------------------------------------
                continue;
            }

            //--------------------------------------------------------------
            // Check Number of Wafers in Lot
            //--------------------------------------------------------------
            log.info("actualTopFlag == FALSE");

            baseBondingLotAttributes.setTotalWaferCount(aBaseLot.getQuantity());
            topBondingLotAttributes.setTotalWaferCount(aTopLot.getQuantity());

            int baseWaferNum = 0, topWaferNum = 0;
            for (int j = 0; j < lotLen; j++) {
                if (ObjectIdentifier.equalsWithValue(baseBondingLotAttributes.getLotID(), baseLotIDs.get(j))) {
                    baseWaferNum += baseWaferNums.get(j);
                }
                if (ObjectIdentifier.equalsWithValue(topBondingLotAttributes.getLotID(), topLotIDs.get(j))) {
                    topWaferNum += topWaferNums.get(j);
                }
            }
            log.info("The number of Wafers from BondingMap {} {}", baseWaferNum, topWaferNum);

            Validations.check(baseBondingLotAttributes.getTotalWaferCount() != baseWaferNum, retCodeConfigEx.getBondgrpLotQuantityUnmatch(), baseBondingLotAttributes.getLotID());
            Validations.check(topBondingLotAttributes.getTotalWaferCount() != topWaferNum, retCodeConfigEx.getBondgrpLotQuantityUnmatch(), topBondingLotAttributes.getLotID());

            //--------------------------------------------------------------
            // Get Base Operation ID.
            //--------------------------------------------------------------
            Outputs.ObjLotBondingOperationInfoGetDROut objLotBondingOperationInfoGetDROut = lotMethod.lotBondingOperationInfoGetDR(objCommon, baseBondingLotAttributes.getLotID());
            baseBondingLotAttributes.setTargetRouteID(objLotBondingOperationInfoGetDROut.getTargetRouteID());
            baseBondingLotAttributes.setTargetOpeNo(objLotBondingOperationInfoGetDROut.getTargetOperationNumber());
            baseBondingLotAttributes.setTargetOpeID(objLotBondingOperationInfoGetDROut.getTargetOperationID());
            baseBondingLotAttributes.setBondingFlowSectionName(objLotBondingOperationInfoGetDROut.getBondingFlowSectionName());

            //--------------------------------------------------------------
            // Check if the Top Product is Parts of Base Product
            //--------------------------------------------------------------
            List<Infos.BOMPartsInfo> bomPartsInfos = null;
            try {
                bomPartsInfos = processMethod.processBOMPartsInfoGetDR(objCommon, baseBondingLotAttributes.getProductID(), baseBondingLotAttributes.getTargetOpeID());
            } catch (ServiceException e) {
                if (!Validations.isEquals(retCodeConfig.getBomNotDefined(), e.getCode()) && !Validations.isEquals(retCodeConfigEx.getPartsNotDefinedForProcess(), e.getCode())) {
                    throw e;
                }
            }
            Validations.check(CimArrayUtils.getSize(bomPartsInfos) != 1, retCodeConfigEx.getNotBaseProdForBonding(), baseBondingLotAttributes.getProductID());
            Validations.check(!ObjectIdentifier.equalsWithValue(bomPartsInfos.get(0).getPartID(), topBondingLotAttributes.getProductID()),
                    retCodeConfigEx.getBondingTopProdspecMismatch(), topBondingLotAttributes.getProductID(), baseBondingLotAttributes.getProductID());

            baseBondingLotAttributes.setTopProductID(bomPartsInfos.get(0).getPartID());

            //--------------------------------------------------------------
            // Check Top Operation ID
            //--------------------------------------------------------------
            Outputs.ObjLotBondingOperationInfoGetDROut objLotBondingOperationInfoGetDROut1 = lotMethod.lotBondingOperationInfoGetDR(objCommon, topBondingLotAttributes.getLotID());
            topBondingLotAttributes.setTargetRouteID(objLotBondingOperationInfoGetDROut1.getTargetRouteID());
            topBondingLotAttributes.setTargetOpeNo(objLotBondingOperationInfoGetDROut1.getTargetOperationNumber());
            topBondingLotAttributes.setTargetOpeID(objLotBondingOperationInfoGetDROut1.getTargetOperationID());
            topBondingLotAttributes.setBondingFlowSectionName(objLotBondingOperationInfoGetDROut1.getBondingFlowSectionName());
            //---------------------------------------------------------------------
            // Check if the Top Product is not Base Product of any other Product
            //---------------------------------------------------------------------

            List<Infos.BOMPartsInfo> bomPartsInfos1 = new ArrayList<>();
            try {
                bomPartsInfos1 = processMethod.processBOMPartsInfoGetDR(objCommon, topBondingLotAttributes.getProductID(), topBondingLotAttributes.getTargetOpeID());
            } catch (ServiceException e) {
                if (!Validations.isEquals(retCodeConfig.getBomNotDefined(), e.getCode()) && !Validations.isEquals(retCodeConfigEx.getPartsNotDefinedForProcess(), e.getCode())) {
                    throw e;
                }
            }
            Validations.check(CimArrayUtils.getSize(bomPartsInfos1) > 0, retCodeConfigEx.getNotBaseProdForBonding(), baseBondingLotAttributes.getProductID());
            //--------------------------------------------------------------
            // Check if Bonding Flow Section is same
            //--------------------------------------------------------------
            Validations.check(!CimStringUtils.equals(baseBondingLotAttributes.getBondingFlowSectionName(), topBondingLotAttributes.getBondingFlowSectionName()),
                    retCodeConfigEx.getNotSameFlowsection(), baseBondingLotAttributes.getLotID(), topBondingLotAttributes.getLotID());

            CimProcessDefinition aBasePD = baseCoreFactory.getBO(CimProcessDefinition.class, baseBondingLotAttributes.getTargetOpeID());
            Validations.check(CimObjectUtils.isEmpty(aBasePD), retCodeConfig.getNotFoundProcessDefinition());
            CimProcessDefinition aTopPD = baseCoreFactory.getBO(CimProcessDefinition.class, topBondingLotAttributes.getTargetOpeID());
            Validations.check(CimObjectUtils.isEmpty(aTopPD), retCodeConfig.getNotFoundProcessDefinition());

            //--------------------------------------------------------------
            // Check if Current Operation is Target Operation
            //--------------------------------------------------------------
            CimProcessDefinition aBaseMainPD = aBaseLot.getMainProcessDefinition();
            CimProcessDefinition aTopMainPD = aTopLot.getMainProcessDefinition();
            String baseRouteID = !CimObjectUtils.isEmpty(aBaseMainPD) ? aBaseMainPD.getIdentifier() : null;
            String topRouteID = !CimObjectUtils.isEmpty(aTopMainPD) ? aTopMainPD.getIdentifier() : null;


            String baseOpeNo = aBaseLot.getOperationNumber(), topOpeNo = aTopLot.getOperationNumber();
            log.info("Route Operation of Base Lot {}", baseRouteID, baseOpeNo);
            log.info("Route Operation of Top Lot {}", topRouteID, topOpeNo);

            //--------------------------------------------------------------
            // Check Lot Reserved
            //--------------------------------------------------------------
            CimProcessOperation aBasePO = null, aTopPO = null;

            if (ObjectIdentifier.equalsWithValue(baseBondingLotAttributes.getTargetRouteID(), baseRouteID) && CimStringUtils.equals(baseBondingLotAttributes.getTargetOpeNo(), baseOpeNo)) {
                log.info("Current Operation for Base Lot is Bonding Target Operation.");
                CimControlJob aBaseCtrlJob = aBaseLot.getControlJob();

                if (!CimObjectUtils.isEmpty(aBaseCtrlJob)) {
                    log.info("Base Lot is reserved.");
                    aBasePO = aBaseLot.getProcessOperation();
                }
            }

            if (ObjectIdentifier.equalsWithValue(topBondingLotAttributes.getTargetRouteID(), topRouteID) && CimStringUtils.equals(topBondingLotAttributes.getTargetOpeNo(), topOpeNo)) {
                log.info("Current Operation for Top Lot is Bonding Target Operation.");

                CimControlJob aTopCtrlJob = aTopLot.getControlJob();
                if (!CimObjectUtils.isEmpty(aTopCtrlJob)) {
                    log.info("Top Lot is reserved.");
                    aTopPO = aTopLot.getProcessOperation();
                }
            }

            //--------------------------------------------------------------
            // Check if the LogicalRecipe are the same.
            //--------------------------------------------------------------
            CimLogicalRecipe aBaseLR, aTopLR;

            if (!CimObjectUtils.isEmpty(aBasePO)) {
                aBaseLR = aBasePO.getAssignedLogicalRecipe();
            } else {
                aBaseLR = aBasePD.findLogicalRecipeFor(aBaseProdSpec);
            }

            if (!CimObjectUtils.isEmpty(aTopPO)) {
                aTopLR = aTopPO.getAssignedLogicalRecipe();
            } else {
                aTopLR = aTopPD.findLogicalRecipeFor(aTopProdSpec);
            }

            baseBondingLotAttributes.setTargetLogicalRecipeID(aBaseLR.getLogicalRecipeID());
            topBondingLotAttributes.setTargetLogicalRecipeID(aTopLR.getLogicalRecipeID());

            Validations.check(ObjectIdentifier.isEmpty(baseBondingLotAttributes.getTargetLogicalRecipeID()) || ObjectIdentifier.isEmpty(topBondingLotAttributes.getTargetLogicalRecipeID()) ||
                    !ObjectIdentifier.equalsWithValue(baseBondingLotAttributes.getTargetLogicalRecipeID(),
                            topBondingLotAttributes.getTargetLogicalRecipeID()), retCodeConfigEx.getNotSameLogicalRecipe(), topBondingLotAttributes.getLotID());

            //--------------------------------------------------------------
            // Check if the MachineRecipe are the same.
            //--------------------------------------------------------------
            CimMachine aBaseEqp = null, aTopEqp = null;
            if (!CimObjectUtils.isEmpty(aBasePO)) {
                log.info("Base Lot is reserved for Bonding Operation.");
                aBaseEqp = aBasePO.getAssignedMachine();
                CimMachineRecipe aBaseMR = aBasePO.getAssignedMachineRecipe();
                baseBondingLotAttributes.setTargetMachineRecipeID(aBaseMR.getMachineRecipeID());
            }
            if (!CimObjectUtils.isEmpty(aTopPO)) {
                log.info("Top Lot is reserved for Bonding Operation.");
                aTopEqp = aTopPO.getAssignedMachine();
                CimMachineRecipe aTopMR = aTopPO.getAssignedMachineRecipe();
                topBondingLotAttributes.setTargetMachineRecipeID(aTopMR.getMachineRecipeID());
            }

            if (!CimObjectUtils.isEmpty(aBaseEqp)) {
                String baseEqpID = aBaseEqp.getIdentifier();

                log.info("Assigned Machine for Base Lot {}");

                Validations.check(!ObjectIdentifier.equalsWithValue(targetEquipmentID, baseEqpID), retCodeConfig.getBondGroupInvalidEqp(), baseBondingLotAttributes.getLotID(), targetEquipmentID, baseBondingLotAttributes.getTargetOpeID());
            } else if (!CimObjectUtils.isEmpty(aEqp)) {
                //-------------------------------------------------------------
                // Get DOC Information of the Base Lot at Target Operation
                //-------------------------------------------------------------
                Outputs.ObjLotEffectiveFPCInfoForOperationGetOut objLotEffectiveFPCInfoForOperationGetOut = lotMethod.lotEffectiveFPCInfoForOperationGet
                        (objCommon, BizConstant.SP_FPC_EXCHANGETYPE_BONDINGGROUP, targetEquipmentID, baseBondingLotAttributes.getLotID(), baseBondingLotAttributes.getTargetRouteID(), baseBondingLotAttributes.getTargetOpeNo());

                //-------------------------------------------------------------
                // Check if the Base Lot can be processed by Target Equipment
                //-------------------------------------------------------------
                boolean baseEqpFound = false;
                if (objLotEffectiveFPCInfoForOperationGetOut.isEquipmentActionRequired()) {
                    log.info("Base Lot can be processed by Target Equipment.");
                    baseEqpFound = true;
                }

                Infos.FPCDispatchEqpInfo fpcDispatchEqpInfo = null;
                if (!baseEqpFound) {
                    //----------------------------------------------------
                    // Get DOC Information of the Lot at Target Operation
                    //----------------------------------------------------
                    fpcDispatchEqpInfo = lotMethod.lotFPCdispatchEqpInfoGet(objCommon, baseBondingLotAttributes.getLotID(), baseBondingLotAttributes.getTargetRouteID(), baseBondingLotAttributes.getTargetOpeNo());
                    List<ObjectIdentifier> dispatchEqpIDs = fpcDispatchEqpInfo.getDispatchEqpIDs();
                    for (ObjectIdentifier dispatchEqpID : dispatchEqpIDs) {
                        if (ObjectIdentifier.equalsWithValue(dispatchEqpID, targetEquipmentID)) {
                            baseEqpFound = true;
                            break;
                        }
                    }
                }

                if (!baseEqpFound && !CimBooleanUtils.isTrue(fpcDispatchEqpInfo.getRestrictEqpFlag())) {
                    log.info("DOC Setting of Equipment is not Found.");

                    /************************************************/
                    /* Call PosProcessDefinition::findMachinesFor() */
                    /************************************************/
                    List<CimMachine> aBaseEqpSeq = aBasePD.findMachinesFor(aBaseProdSpec);
                    for (CimMachine machine : aBaseEqpSeq) {
                        if (CimStringUtils.equals(aEqp.getIdentifier(),machine.getIdentifier())) {
                            baseEqpFound = true;
                            break;
                        }
                    }
                }
                Validations.check(!baseEqpFound, retCodeConfig.getBondGroupInvalidEqp(), baseBondingLotAttributes.getLotID(), targetEquipmentID, baseBondingLotAttributes.getTargetOpeID());

                //-------------------------------------------------------------
                // Getting Machine Recipe for Base Lot
                //-------------------------------------------------------------
                if (objLotEffectiveFPCInfoForOperationGetOut.isMachineRecipeActionRequired()) {
                    log.info("DOC Setting of Machine Recipe Found. Use it.");
                    baseBondingLotAttributes.setTargetMachineRecipeID(objLotEffectiveFPCInfoForOperationGetOut.getStrFPCInfo().getMachineRecipeID());
                } else {
                    log.info("DOC Setting of Machine Recipe is not Found. calling PosLogicalRecipe::findMachineRecipeFor().");

                    CimMachineRecipe aBaseMR = aBaseLR.findMachineRecipeFor(aBaseLot, aEqp);
                    baseBondingLotAttributes.setTargetMachineRecipeID(aBaseMR.getMachineRecipeID());
                }
            }

            if (!CimObjectUtils.isEmpty(aTopEqp)) {
                String topEqpID = aTopEqp.getIdentifier();
                log.info("Assigned Machine for Top Lot {}", topEqpID);
                Validations.check(!ObjectIdentifier.equalsWithValue(targetEquipmentID, topEqpID), retCodeConfig.getBondGroupInvalidEqp(), topBondingLotAttributes.getLotID(), targetEquipmentID, topBondingLotAttributes.getTargetOpeID());
            } else if (!CimObjectUtils.isEmpty(aEqp)) {
                //-------------------------------------------------------------
                // Get DOC Information of the Top Lot at Current Operation
                //-------------------------------------------------------------
                Outputs.ObjLotEffectiveFPCInfoForOperationGetOut objLotEffectiveFPCInfoForOperationGetOut = lotMethod.lotEffectiveFPCInfoForOperationGet
                        (objCommon, BizConstant.SP_FPC_EXCHANGETYPE_BONDINGGROUP, targetEquipmentID, topBondingLotAttributes.getLotID(), topBondingLotAttributes.getTargetRouteID(), topBondingLotAttributes.getTargetOpeNo());

                //-------------------------------------------------------------
                // Check if the Base Lot can be processed by Target Equipment
                //-------------------------------------------------------------
                boolean topEqpFound = false;
                if (objLotEffectiveFPCInfoForOperationGetOut.isEquipmentActionRequired()) {
                    log.info("Top Lot can be processed by Target Equipment.");
                    topEqpFound = true;
                }

                Infos.FPCDispatchEqpInfo fpcDispatchEqpInfo = null;
                if (!topEqpFound) {
                    //----------------------------------------------------
                    // Get DOC Information of the Lot at Target Operation
                    //----------------------------------------------------
                    fpcDispatchEqpInfo = lotMethod.lotFPCdispatchEqpInfoGet(objCommon, topBondingLotAttributes.getLotID(), topBondingLotAttributes.getTargetRouteID(), topBondingLotAttributes.getTargetOpeNo());
                    List<ObjectIdentifier> dispatchEqpIDs = fpcDispatchEqpInfo.getDispatchEqpIDs();
                    for (ObjectIdentifier dispatchEqpID : dispatchEqpIDs) {
                        if (ObjectIdentifier.equalsWithValue(dispatchEqpID, targetEquipmentID)) {
                            topEqpFound = true;
                            break;
                        }
                    }
                }

                if (!topEqpFound && !CimBooleanUtils.isTrue(fpcDispatchEqpInfo.getRestrictEqpFlag())) {
                    log.info("DOC Setting of Equipment is not Found.");

                    /************************************************/
                    /* Call PosProcessDefinition::findMachinesFor() */
                    /************************************************/
                    List<CimMachine> aTopEqpSeq = aTopPD.findMachinesFor(aTopProdSpec);
                    for (CimMachine machine : aTopEqpSeq) {
                        if (CimStringUtils.equals(aEqp.getIdentifier(),machine.getIdentifier())) {
                            topEqpFound = true;
                            break;
                        }
                    }
                }
                Validations.check(!topEqpFound, retCodeConfig.getBondGroupInvalidEqp(), topBondingLotAttributes.getLotID(), targetEquipmentID, topBondingLotAttributes.getTargetOpeID());

                //-------------------------------------------------------------
                // Getting Machine Recipe for Base Lot
                //-------------------------------------------------------------
                if (objLotEffectiveFPCInfoForOperationGetOut.isEquipmentActionRequired()) {
                    log.info("DOC Setting of Machine Recipe Found. Use it.");
                    topBondingLotAttributes.setTargetMachineRecipeID(objLotEffectiveFPCInfoForOperationGetOut.getStrFPCInfo().getMachineRecipeID());
                } else {
                    log.info("DOC Setting of Machine Recipe is not Found. calling PosLogicalRecipe::findMachineRecipeFor().");
                    CimMachineRecipe aTopMR = aTopLR.findMachineRecipeFor(aTopLot, aEqp);
                    topBondingLotAttributes.setTargetMachineRecipeID(aTopMR.getMachineRecipeID());
                }
            }

            if (!CimObjectUtils.isEmpty(aEqp)) {
                //--------------------------------------------------------------
                // Check if the MachineRecipe are the same.
                //--------------------------------------------------------------

                Validations.check(ObjectIdentifier.isEmpty(baseBondingLotAttributes.getTargetMachineRecipeID()) ||
                        ObjectIdentifier.isEmpty(topBondingLotAttributes.getTargetMachineRecipeID()) ||
                        !ObjectIdentifier.equalsWithValue(baseBondingLotAttributes.getTargetMachineRecipeID(),
                                topBondingLotAttributes.getTargetMachineRecipeID()), retCodeConfigEx.getNotSameRecipe(), topBondingLotAttributes.getLotID());
            }
        }
    }


    @Override
    public Outputs.ObjBondingMapFillInTxPCR003DROut bondingMapFillInTxPCR003DR(Infos.ObjCommon objCommon, List<Infos.BondingMapInfo> strBondingMapInfoSeq) {
        //----------------------------------------------------------------------
        //   Get Lot/Product ID from Wafer
        //----------------------------------------------------------------------
        Outputs.ObjBondingMapFillInTxPCR003DROut retVal = new Outputs.ObjBondingMapFillInTxPCR003DROut();
        Optional.ofNullable(strBondingMapInfoSeq).ifPresent(list -> {
            Set<ObjectIdentifier> bondingLotIDSeq = new HashSet<>();
            for (Infos.BondingMapInfo data : list) {
                Validations.check(CimStringUtils.isEmpty(ObjectIdentifier.fetchValue(data.getBaseWaferID())), retCodeConfigEx.getInvalidParmForBondingMapReport(),"Base Wafer ID");
                // Check the Base Lot is null
                if (CimStringUtils.isEmpty(ObjectIdentifier.fetchValue(data.getBaseLotID()))
                        || CimStringUtils.isEmpty(ObjectIdentifier.fetchValue(data.getBaseProductID()))) {
                    CimWafer wafer = baseCoreFactory.getBO(CimWafer.class, data.getBaseWaferID());
                    if (CimStringUtils.isEmpty(ObjectIdentifier.fetchValue(data.getBaseLotID()))) {
                        data.setBaseLotID(ObjectIdentifier.build(wafer.getLot().getIdentifier(), wafer.getLot().getPrimaryKey()));
                    }
                    if (CimObjectUtils.isEmpty(CimStringUtils.isEmpty(ObjectIdentifier.fetchValue(data.getBaseProductID())))) {
                        data.setBaseProductID(ObjectIdentifier.build(wafer.getProductSpecification().getIdentifier(), wafer.getProductSpecification().getPrimaryKey()));
                    }
                }

                Validations.check(CimStringUtils.isEmpty(ObjectIdentifier.fetchValue(data.getActualTopWaferID())), retCodeConfigEx.getInvalidParmForBondingMapReport(),"Top Wafer ID");
                // Check the Actual Lot is null
                if (CimStringUtils.isEmpty(ObjectIdentifier.fetchValue(data.getActualTopLotID()))
                        || CimStringUtils.isEmpty(ObjectIdentifier.fetchValue(data.getActualTopProductID()))) {
                    CimWafer actualWafer = baseCoreFactory.getBO(CimWafer.class, data.getActualTopWaferID());
                    if (CimStringUtils.isEmpty(ObjectIdentifier.fetchValue(data.getActualTopLotID()))) {
                        data.setActualTopLotID(ObjectIdentifier.build(actualWafer.getLot().getIdentifier(), actualWafer.getLot().getPrimaryKey()));
                    }
                    if (CimObjectUtils.isEmpty(CimStringUtils.isEmpty(ObjectIdentifier.fetchValue(data.getActualTopProductID())))) {
                        data.setActualTopProductID(ObjectIdentifier.build(actualWafer.getProductSpecification().getIdentifier(), actualWafer.getProductSpecification().getPrimaryKey()));
                    }
                }

                bondingLotIDSeq.add(data.getBaseLotID());
                bondingLotIDSeq.add(data.getActualTopLotID());
            }
            retVal.setStrBondingMapInfoSeq(strBondingMapInfoSeq);
            retVal.setBondingLotIDSeq(new ArrayList<>(bondingLotIDSeq));
        });
        return retVal;
    }

    @Override
    public List<Infos.BondingMapInfo> bondingMapResultMerge(Infos.ObjCommon objCommon, List<Infos.BondingMapInfo> strBondingMapInfoSeq, List<Infos.BondingGroupInfo> strBondingGroupInfoSeq) {
        // todo:ZQI need to check logic......
        List<Infos.BondingMapInfo> retVal = new ArrayList<>();
        Optional.ofNullable(strBondingMapInfoSeq).ifPresent(bondingMapInfos -> {
            for (Infos.BondingMapInfo inBondingMapInfo : bondingMapInfos) {
                log.info("Check Base Wafer: " + ObjectIdentifier.fetchValue(inBondingMapInfo.getBaseWaferID()));
                //--------------------------------------------------------------
                //   Check if Base Wafer is in the Bonding Group.
                //--------------------------------------------------------------
                boolean waferFound = false;
                Infos.BondingMapInfo tempBondingMap = null;
                List<Infos.BondingMapInfo> tempBondingMapInfoList = null;
                for (Infos.BondingGroupInfo bondingGroupInfo : strBondingGroupInfoSeq) {
                    for (Infos.BondingMapInfo data : bondingGroupInfo.getBondingMapInfoList()) {
                        if (ObjectIdentifier.equalsWithValue(inBondingMapInfo.getBaseWaferID(), data.getBaseWaferID())) {
                            waferFound = true;
                            tempBondingMap = data;
                            break;
                        }
                    }
                    if (waferFound) {
                        tempBondingMapInfoList = bondingGroupInfo.getBondingMapInfoList();
                        break;
                    }
                }
                Validations.check(!waferFound, retCodeConfigEx.getNotFoundBondingMap());

                Infos.BondingMapInfo outBindingMap = new Infos.BondingMapInfo();
                retVal.add(outBindingMap);

                //--------------------------------------------------------------
                //   Merge Bonding Group ID.
                //--------------------------------------------------------------
                if (CimStringUtils.isEmpty(inBondingMapInfo.getBondingGroupID())) {
                    outBindingMap.setBondingGroupID(tempBondingMap.getBondingGroupID());
                } else if (CimStringUtils.equals(inBondingMapInfo.getBondingGroupID(), tempBondingMap.getBondingGroupID())) {
                    outBindingMap.setBondingGroupID(inBondingMapInfo.getBondingGroupID());
                } else {
                    log.error("Bonding Group ID differs.");
                    throw new ServiceException(retCodeConfigEx.getNotFoundBondingMap());
                }

                //--------------------------------------------------------------
                //   Merge Base Wafer ID.
                //--------------------------------------------------------------
                outBindingMap.setBaseWaferID(inBondingMapInfo.getBaseWaferID());

                //--------------------------------------------------------------
                //   Merge Base Lot ID.
                //--------------------------------------------------------------
                if (ObjectIdentifier.isEmpty(inBondingMapInfo.getBaseLotID())) {
                    outBindingMap.setBaseLotID(tempBondingMap.getBaseLotID());
                } else if (ObjectIdentifier.equalsWithValue(inBondingMapInfo.getBaseLotID(), tempBondingMap.getBaseLotID())) {
                    outBindingMap.setBaseLotID(inBondingMapInfo.getBaseLotID());
                } else {
                    log.error("Base Lot ID is not correct.");
                    throw new ServiceException(retCodeConfig.getLotWaferUnmatch());
                }

                //--------------------------------------------------------------
                //   Merge Base Product ID.
                //--------------------------------------------------------------
                if (ObjectIdentifier.isEmpty(inBondingMapInfo.getBaseProductID())) {
                    outBindingMap.setBaseProductID(tempBondingMap.getBaseProductID());
                } else if (ObjectIdentifier.equalsWithValue(inBondingMapInfo.getBaseProductID(), tempBondingMap.getBaseProductID())) {
                    outBindingMap.setBaseProductID(inBondingMapInfo.getBaseProductID());
                } else {
                    log.error("Base Product ID is not correct.");
                    throw new ServiceException(retCodeConfig.getProductSpecUnMatch());
                }

                //--------------------------------------------------------------
                //   Merge Base Bonding Side.
                //--------------------------------------------------------------
                if (CimStringUtils.isEmpty(inBondingMapInfo.getBaseBondingSide())) {
                    outBindingMap.setBaseBondingSide(tempBondingMap.getBaseBondingSide());
                } else if (CimStringUtils.equals(inBondingMapInfo.getBaseBondingSide(), tempBondingMap.getBaseBondingSide())) {
                    outBindingMap.setBaseBondingSide(inBondingMapInfo.getBaseBondingSide());
                } else {
                    log.error("Base Bonding Side differs.");
                    throw new ServiceException(retCodeConfigEx.getInvalidParmForBondingMapReport(),"Base Bonding Side");
                }

                //--------------------------------------------------------------
                //   Merge Plan Top Wafer ID.
                //--------------------------------------------------------------
                if (ObjectIdentifier.isEmpty(inBondingMapInfo.getPlanTopWaferID())) {
                    outBindingMap.setPlanTopWaferID(tempBondingMap.getPlanTopWaferID());
                } else if (ObjectIdentifier.equalsWithValue(inBondingMapInfo.getPlanTopWaferID(), tempBondingMap.getPlanTopWaferID())) {
                    outBindingMap.setPlanTopWaferID(inBondingMapInfo.getPlanTopWaferID());
                } else {
                    log.error("Plan Top Wafer ID differs.");
                    throw new ServiceException(retCodeConfigEx.getNotFoundBondingMap());
                }

                //--------------------------------------------------------------
                //   Merge Plan Top Lot ID.
                //--------------------------------------------------------------
                if (ObjectIdentifier.isEmpty(inBondingMapInfo.getPlanTopLotID())) {
                    outBindingMap.setPlanTopLotID(tempBondingMap.getPlanTopLotID());
                } else if (ObjectIdentifier.equalsWithValue(inBondingMapInfo.getPlanTopLotID(), tempBondingMap.getPlanTopLotID())) {
                    outBindingMap.setPlanTopLotID(inBondingMapInfo.getPlanTopLotID());
                } else {
                    log.error("Plan Top Wafer ID differs.");
                    throw new ServiceException(retCodeConfigEx.getNotFoundBondingMap());
                }

                //--------------------------------------------------------------
                //   Merge Plan Top Product ID.
                //--------------------------------------------------------------
                if (ObjectIdentifier.isEmpty(inBondingMapInfo.getPlanTopProductID())) {
                    outBindingMap.setPlanTopProductID(tempBondingMap.getPlanTopProductID());
                } else if (ObjectIdentifier.equalsWithValue(inBondingMapInfo.getPlanTopProductID(), tempBondingMap.getPlanTopProductID())) {
                    outBindingMap.setPlanTopProductID(inBondingMapInfo.getPlanTopProductID());
                } else {
                    log.error("Plan Top Product ID differs.");
                    throw new ServiceException(retCodeConfig.getProductSpecUnMatch());
                }

                //--------------------------------------------------------------
                //   Merge Plan Top Bonding Side
                //--------------------------------------------------------------
                if (CimStringUtils.isEmpty(inBondingMapInfo.getPlanTopBondingSide())) {
                    outBindingMap.setPlanTopBondingSide(tempBondingMap.getPlanTopBondingSide());
                } else if (CimStringUtils.equals(inBondingMapInfo.getPlanTopBondingSide(), tempBondingMap.getPlanTopBondingSide())) {
                    outBindingMap.setPlanTopBondingSide(inBondingMapInfo.getPlanTopBondingSide());
                } else {
                    log.error("Plan Top Bonding Side differs.");
                    throw new ServiceException(retCodeConfigEx.getInvalidParmForBondingMapReport(),"Top Bonding Side");
                }

                //--------------------------------------------------------------
                //   Set Actual Top Bonding Side.
                //--------------------------------------------------------------
                if (CimStringUtils.isEmpty(inBondingMapInfo.getActualTopBondingSide())) {
                    outBindingMap.setActualTopBondingSide(tempBondingMap.getPlanTopBondingSide());
                } else {
                    outBindingMap.setActualTopBondingSide(inBondingMapInfo.getActualTopBondingSide());
                }

                //--------------------------------------------------------------
                //   Set Bonding Process State.
                //--------------------------------------------------------------
                outBindingMap.setBondingProcessState(inBondingMapInfo.getBondingProcessState());

                if (ObjectIdentifier.isEmpty(inBondingMapInfo.getActualTopWaferID())) {
                    log.info("Actual Top Wafer is not Filled.");
                    continue;
                }

                if (!ObjectIdentifier.equalsWithValue(inBondingMapInfo.getActualTopWaferID(), tempBondingMap.getPlanTopWaferID())) {
                    //--------------------------------------------------------------
                    //   Check if Top Wafer is in the Bonding Group.
                    //--------------------------------------------------------------
                    waferFound = false;
                    for (Infos.BondingMapInfo data : tempBondingMapInfoList) {
                        if (ObjectIdentifier.equalsWithValue(data.getPlanTopWaferID(), inBondingMapInfo.getPlanTopWaferID())) {
                            waferFound = true;
                            tempBondingMap = data;
                            break;
                        }
                    }
                    if (!waferFound) {
                        log.error("Top Wafer is not found in Bonding Group.");
                        throw new ServiceException(retCodeConfigEx.getNotFoundBondingMap());
                    }
                    //--------------------------------------------------------------
                    //   Check Bonding Group ID.
                    //--------------------------------------------------------------
                    if (CimStringUtils.isNotEmpty(inBondingMapInfo.getBondingGroupID())
                            && !CimStringUtils.equals(inBondingMapInfo.getBondingGroupID(), tempBondingMap.getBondingGroupID())) {
                        log.error("Bonding Group ID differs.");
                        throw new ServiceException(retCodeConfigEx.getNotFoundBondingMap());
                    }
                }

                //--------------------------------------------------------------
                //   Merge Actual Top Wafer ID.
                //--------------------------------------------------------------
                outBindingMap.setActualTopWaferID(inBondingMapInfo.getActualTopWaferID());

                //--------------------------------------------------------------
                //   Merge Actual Top Lot ID.
                //--------------------------------------------------------------
                if (ObjectIdentifier.isEmpty(inBondingMapInfo.getActualTopLotID())) {
                    outBindingMap.setActualTopLotID(tempBondingMap.getActualTopLotID());
                } else if (ObjectIdentifier.equalsWithValue(inBondingMapInfo.getActualTopLotID(), tempBondingMap.getPlanTopLotID())) {
                    outBindingMap.setActualTopLotID(inBondingMapInfo.getActualTopLotID());
                } else {
                    log.error("Top Lot ID is not correct.");
                    throw new ServiceException(new OmCode(retCodeConfig.getLotWaferUnmatch(),inBondingMapInfo.getActualTopLotID().getValue(),inBondingMapInfo.getActualTopWaferID().getValue()));
                }

                //--------------------------------------------------------------
                //   Merge Actual Top Product ID.
                //--------------------------------------------------------------
                if (ObjectIdentifier.isEmpty(inBondingMapInfo.getActualTopProductID())) {
                    outBindingMap.setActualTopProductID(tempBondingMap.getPlanTopProductID());
                } else if (ObjectIdentifier.equalsWithValue(inBondingMapInfo.getActualTopProductID(), tempBondingMap.getPlanTopProductID())) {
                    outBindingMap.setActualTopProductID(inBondingMapInfo.getActualTopProductID());
                } else {
                    log.error("Top Product ID is not correct.");
                    throw new ServiceException(retCodeConfig.getProductSpecUnMatch());
                }

                //--------------------------------------------------------------
                //   Check if Top Wafer is already Reported or not.
                //--------------------------------------------------------------
                waferFound = false;
                for (Infos.BondingMapInfo data : tempBondingMapInfoList) {
                    if (ObjectIdentifier.equalsWithValue(inBondingMapInfo.getActualTopWaferID(), data.getActualTopWaferID())) {
                        log.info("Found");
                        waferFound = true;
                        tempBondingMap = data;
                        break;
                    }
                }
                if (!waferFound) {
                    log.info("Top Wafer is not found in Bonding Group.");
                    continue;
                }

                // If Base Wafer is the same, it means that bonding map result is overwrited. It is OK.
                if (ObjectIdentifier.equalsWithValue(inBondingMapInfo.getBaseWaferID(), tempBondingMap.getBaseWaferID())) {
                    log.info("Top Wafer is not found in Bonding Group.");
                    continue;
                }

                // If existed Actual Top Wafer ID will be changed by this report, it is OK.
                boolean bInputExist = false;
                for (Infos.BondingMapInfo bondingMapInfo : strBondingMapInfoSeq) {
                    if (ObjectIdentifier.equalsWithValue(tempBondingMap.getBaseWaferID(), bondingMapInfo.getBaseWaferID())) {
                        log.info("Same Base Wafer is found in input.");
                        bInputExist = true;
                        break;
                    }
                }
                if (bInputExist) {
                    continue;
                }

                //--------------------------------------------------------------
                //   Check Bonding Process State.
                //--------------------------------------------------------------
                if (CimStringUtils.isNotEmpty(tempBondingMap.getBondingProcessState())) {
                    log.error("Top Wafer is already reported.");
                    throw new ServiceException(retCodeConfigEx.getAlreadyBondingMapResultReported());
                }
            }

        });
        return retVal;
    }

    @Override
    public void bondingMapResultUpdateDR(Infos.ObjCommon objCommon, List<Infos.BondingMapInfo> strBondingMapInfoSeq) {
        Validations.check(null == strBondingMapInfoSeq, retCodeConfig.getInvalidParameter());
        Optional.of(strBondingMapInfoSeq).ifPresent(list -> list.forEach(data -> {
            CimBondGroupMapDO example = new CimBondGroupMapDO();
            example.setBondGroupID(data.getBondingGroupID());
            example.setBaseLotID(ObjectIdentifier.fetchValue(data.getBaseLotID()));
            example.setBaseWaferID(ObjectIdentifier.fetchValue(data.getBaseWaferID()));

            CimBondGroupMapDO bondGroupMapDO = cimJpaRepository.findOne(Example.of(example)).orElse(null);

            Validations.check(null == bondGroupMapDO, retCodeConfigEx.getNotFoundBondingMap());
            bondGroupMapDO.setActiveTopLotID(ObjectIdentifier.fetchValue(data.getActualTopLotID()));
            bondGroupMapDO.setActiveTopProductSpecID(ObjectIdentifier.fetchValue(data.getActualTopProductID()));
            bondGroupMapDO.setActiveTopWaferID(ObjectIdentifier.fetchValue(data.getActualTopWaferID()));
            bondGroupMapDO.setActiveTopBondSide(data.getActualTopBondingSide());
            bondGroupMapDO.setProcessState(data.getBondingProcessState());
            bondGroupMapDO.setProcessCompleteTime(objCommon.getTimeStamp().getReportTimeStamp());

            cimJpaRepository.save(bondGroupMapDO);

        }));
    }

    @Override
    public void bondingMapWaferStackMake(Infos.ObjCommon objCommon, List<Infos.BondingMapInfo> bondingMapInfoSeq) {
        Validations.check(null == bondingMapInfoSeq, retCodeConfig.getInvalidParameter());
        CimPerson aPerson = baseCoreFactory.getBO(CimPerson.class, objCommon.getUser().getUserID());
        Validations.check(null == aPerson, retCodeConfig.getNotFoundPerson());

        //----------------------------------------------------------------------
        // Check Inputed Bonding Map
        //----------------------------------------------------------------------
        Optional.of(bondingMapInfoSeq).ifPresent(bondingMapInfos -> bondingMapInfos.forEach(bondingMapInfo -> {
            //----------------------------------------------------------------------
            // Get Stacked Wafers
            //----------------------------------------------------------------------
            CimWafer aBaseWafer = baseCoreFactory.getBO(CimWafer.class, bondingMapInfo.getBaseWaferID());
            Validations.check(null == aBaseWafer, retCodeConfig.getNotFoundWafer());
            CimWafer aTopWafer = baseCoreFactory.getBO(CimWafer.class, bondingMapInfo.getActualTopWaferID());
            Validations.check(null == aTopWafer, retCodeConfig.getNotFoundWafer());

            List<ProductDTO.StackedWafer> baseStackedWaferSeq = aBaseWafer.allStackedWafers();
            List<ProductDTO.StackedWafer> topStackedWaferSeq = aTopWafer.allStackedWafers();

            //----------------------------------------------------------------------
            // Get Max Material Offset
            //----------------------------------------------------------------------
            long baseMaxMaterialOffset = baseStackedWaferSeq.stream().mapToLong(ProductDTO.StackedWafer::getMaterialOffset).max().orElse(0L);
            long topMaxMaterialOffset = topStackedWaferSeq.stream().mapToLong(ProductDTO.StackedWafer::getMaterialOffset).max().orElse(0L);

            log.info("baseMaxMaterialOffset = " + baseMaxMaterialOffset);
            log.info("topMaxMaterialOffset = " + topMaxMaterialOffset);

            //----------------------------------------------------------------------
            // Set Stacked Wafer Information
            //----------------------------------------------------------------------
            ProductDTO.StackedWafer aPosStackedWafer = new ProductDTO.StackedWafer();
            aPosStackedWafer.setTopWaferID(ObjectIdentifier.build(aTopWafer.getIdentifier(), aTopWafer.getPrimaryKey()));
            aPosStackedWafer.setTopAliasWaferName(aTopWafer.getAliasWaferName());
            aPosStackedWafer.setTopLotID(bondingMapInfo.getActualTopLotID());
            aPosStackedWafer.setStackedTimeStamp(bondingMapInfo.getProcessCompleteTime());
            aPosStackedWafer.setFabID(StandardProperties.OM_SITE_ID.getValue());
            aPosStackedWafer.setMaterialOffset(baseMaxMaterialOffset + topMaxMaterialOffset + 1);

            for (ProductDTO.StackedWafer stackedWafer : topStackedWaferSeq) {
                stackedWafer.setMaterialOffset(aPosStackedWafer.getMaterialOffset() - stackedWafer.getMaterialOffset() );
            }

            if (CimStringUtils.equals(bondingMapInfo.getActualTopBondingSide(), BizConstant.SP_BONDINGSIDE_BOTTOM)) {
                //----------------------------------------------------------------------
                // Handle Alias Wafer Name
                //----------------------------------------------------------------------
                aPosStackedWafer.setPreviousAliasWaferName(aBaseWafer.getAliasWaferName());
                aBaseWafer.setAliasWaferName(aPosStackedWafer.getTopAliasWaferName());

                //----------------------------------------------------------------------
                // Negate All Material Offset
                //----------------------------------------------------------------------
                aPosStackedWafer.setMaterialOffset(- aPosStackedWafer.getMaterialOffset());

                Optional.of(baseStackedWaferSeq).ifPresent(baseStackedWafers -> baseStackedWafers.forEach(baseStackedWafer -> {
                    aBaseWafer.removeStackedWafer(baseStackedWafer);
                    baseStackedWafer.setMaterialOffset(- baseStackedWafer.getMaterialOffset());
                    aBaseWafer.addStackedWafer(baseStackedWafer);
                }));

                Optional.of(topStackedWaferSeq).ifPresent(list -> list.forEach(data -> data.setMaterialOffset(- data.getMaterialOffset())));
            }

            aBaseWafer.addStackedWafer(aPosStackedWafer);
            Optional.of(topStackedWaferSeq).ifPresent(list -> list.forEach(aBaseWafer::addStackedWafer));

            //----------------------------------------------------------------------
            // Make Stacked
            //----------------------------------------------------------------------
            aTopWafer.makeStacked();

            //--------------------------------------------------
            // Set Last Claimed Record
            //--------------------------------------------------
            aBaseWafer.setLastClaimedTimeStamp(objCommon.getTimeStamp().getReportTimeStamp());
            aTopWafer.setLastClaimedTimeStamp(objCommon.getTimeStamp().getReportTimeStamp());

            aBaseWafer.setLastClaimedPerson(aPerson);
            aTopWafer.setLastClaimedPerson(aPerson);
        }));
    }

    @Override
    public Outputs.BondingMapInfoConsistencyCheckForPartialReleaseOut bondingMapInfoConsistencyCheckForPartialRelease(Infos.ObjCommon objCommon,
                                                                                                                      List<Infos.BondingMapInfo> strBondingMapInfoSeq,
                                                                                                                      List<Infos.BondingGroupReleaseLotWafer> bondingGroupReleaseLotWaferInfos) {
        Outputs.BondingMapInfoConsistencyCheckForPartialReleaseOut out = new Outputs.BondingMapInfoConsistencyCheckForPartialReleaseOut();
        int mapLen = CimArrayUtils.getSize(strBondingMapInfoSeq);
        int lotLen = CimArrayUtils.getSize(bondingGroupReleaseLotWaferInfos);
        int[] numBaseWaferInBondingMapSeq = new int[lotLen];
        int[] numTopWaferInBondingMapSeq = new int[lotLen];
        List<Infos.BondingGroupReleaseLotWafer> strBondingGroupReleaseLotWaferSeq = new ArrayList<>();
        out.setStrBondingGroupReleaseLotWaferSeq(strBondingGroupReleaseLotWaferSeq);

        List<Infos.BondingMapInfo> srcMap = new ArrayList<>(mapLen);
        out.setStrPartialReleaseSourceMapSeq(srcMap);

        List<Infos.BondingMapInfo> destMap = new ArrayList<>(mapLen);
        out.setStrPartialReleaseDestinationMapSeq(destMap);
        int numNotReportedWafer = 0;
        if (lotLen > 0) {
            for (int i = 0; i < bondingGroupReleaseLotWaferInfos.size(); i++) {
                numBaseWaferInBondingMapSeq[i] = 0;
                numTopWaferInBondingMapSeq[i] = 0;
                Infos.BondingGroupReleaseLotWafer bondingGroupReleaseLotWafer = bondingGroupReleaseLotWaferInfos.get(i);
                if (CimArrayUtils.isNotEmpty(strBondingMapInfoSeq)) {
                    for (Infos.BondingMapInfo info : strBondingMapInfoSeq) {
                        //--------------------------------------------------------------
                        //   Count Base Wafer.
                        //--------------------------------------------------------------
                        String parentId = ObjectIdentifier.fetchValue(bondingGroupReleaseLotWafer.getParentLotID());
                        if (CimStringUtils.equals(parentId, ObjectIdentifier.fetchValue(info.getBaseLotID()))) {
                            numBaseWaferInBondingMapSeq[i]++;
                        }

                        //--------------------------------------------------------------
                        //   Count Top Wafer.
                        //--------------------------------------------------------------
                        if (CimStringUtils.equals(parentId, ObjectIdentifier.fetchValue(info.getPlanTopLotID()))) {
                            numTopWaferInBondingMapSeq[i]++;
                        }
                    }
                }
                int wfrLen = CimArrayUtils.getSize(bondingGroupReleaseLotWafer.getChildWaferIDSeq());
                if (numBaseWaferInBondingMapSeq[i] > 0) {
                    if (numBaseWaferInBondingMapSeq[i] >= wfrLen) {
                        strBondingGroupReleaseLotWaferSeq.add(bondingGroupReleaseLotWafer);
                    }
                } else if (numTopWaferInBondingMapSeq[i] > 0) {
                    if (numTopWaferInBondingMapSeq[i] >= wfrLen) {
                        strBondingGroupReleaseLotWaferSeq.add(bondingGroupReleaseLotWafer);
                    }
                    for (Infos.BondingMapInfo info : strBondingMapInfoSeq) {
                        String actualTopWaferId = ObjectIdentifier.fetchValue(info.getActualTopWaferID());
                        if (CimStringUtils.isNotEmpty(actualTopWaferId) &&
                                !CimArrayUtils.isFoundInList(actualTopWaferId, bondingGroupReleaseLotWafer.getChildWaferIDSeq())) {
                            numNotReportedWafer++;
                        }
                    }
                } else {
                    throw new ServiceException(retCodeConfigEx.getNotFoundBondingMap());
                }
            }

            if (mapLen > 0) {
                for (Infos.BondingMapInfo info : strBondingMapInfoSeq) {
                    boolean baseWaferFound = false;
                    boolean topWaferFound = false;
                    String baseWaferId = ObjectIdentifier.fetchValue(info.getBaseWaferID());
                    String topWaferId = ObjectIdentifier.fetchValue(info.getActualTopWaferID());
                    for (int i = 0; i < numBaseWaferInBondingMapSeq.length; i++) {
                        Infos.BondingGroupReleaseLotWafer bondingGroupReleaseLotWafer = strBondingGroupReleaseLotWaferSeq.get(i);
                        //--------------------------------------------------------------
                        //   Check if Base Wafer is Released.
                        //--------------------------------------------------------------
                        if (numBaseWaferInBondingMapSeq[i] != 0) {
                            baseWaferFound = CimArrayUtils.isFoundInList(baseWaferId, bondingGroupReleaseLotWafer.getChildWaferIDSeq());
                        }
                        //--------------------------------------------------------------
                        //   Check if Top Wafer is Released.
                        //--------------------------------------------------------------
                        topWaferFound = CimStringUtils.isEmpty(topWaferId) && numNotReportedWafer-- > 0 ||
                                numTopWaferInBondingMapSeq[i] != 0 && CimArrayUtils.isFoundInList(topWaferId, bondingGroupReleaseLotWafer.getChildWaferIDSeq());
                    }
                    if (!baseWaferFound && !topWaferFound) {
                        srcMap.add(info);
                        continue;
                    }
                    Validations.check(!baseWaferFound && topWaferFound, retCodeConfigEx.getBondgrpWaferMissedForRelease(),
                            baseWaferId, ObjectIdentifier.fetchValue(info.getBaseLotID()));

                    Validations.check(baseWaferFound && !topWaferFound, retCodeConfigEx.getBondgrpWaferMissedForRelease(),
                            topWaferId, ObjectIdentifier.fetchValue(info.getActualTopLotID()));

                    //--------------------------------------------------------------
                    //   Check Bonding Process State.
                    //--------------------------------------------------------------
                    String bondingProcessState = info.getBondingProcessState();
                    Validations.check(CimStringUtils.equals(bondingProcessState, BizConstant.SP_BONDINGPROCESSSTATE_COMPLETED),
                            retCodeConfigEx.getBondmapStateInvalid(), bondingProcessState);
                    destMap.add(info);
                }
            }
        }
        return out;
    }

}
