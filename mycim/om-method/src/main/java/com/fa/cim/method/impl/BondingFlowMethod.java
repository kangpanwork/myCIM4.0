package com.fa.cim.method.impl;

import com.fa.cim.annotaion.OmMethod;
import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.utils.*;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.config.RetCodeConfigEx;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Outputs;
import com.fa.cim.entity.runtime.lot.CimLotDO;
import com.fa.cim.jpa.CimJpaRepository;
import com.fa.cim.method.IBondingFlowMethod;
import com.fa.cim.method.ILotMethod;
import com.fa.cim.method.IMinQTimeMethod;
import com.fa.cim.method.IProcessMethod;
import com.fa.cim.newcore.bo.dispatch.CimDispatcher;
import com.fa.cim.newcore.bo.dispatch.CimWhatNextLogicBase;
import com.fa.cim.newcore.bo.dispatch.DispatchingManager;
import com.fa.cim.newcore.bo.globalfunc.CimFrameWorkGlobals;
import com.fa.cim.newcore.bo.machine.CimMachine;
import com.fa.cim.newcore.bo.pd.CimProcessDefinition;
import com.fa.cim.newcore.bo.pd.CimProcessFlowContext;
import com.fa.cim.newcore.bo.pd.CimProcessOperation;
import com.fa.cim.newcore.bo.product.CimLot;
import com.fa.cim.newcore.dto.dispatch.DispatcherDTO;
import com.fa.cim.newcore.dto.global.GlobalDTO;
import com.fa.cim.newcore.dto.pd.ProcessDTO;
import com.fa.cim.newcore.factory.BaseCoreFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Timestamp;
import java.util.*;

@OmMethod
@Slf4j
public class BondingFlowMethod implements IBondingFlowMethod {


    @Autowired
    private CimJpaRepository cimJpaRepository;

    private final RetCodeConfigEx retCodeConfigEx;

    private final RetCodeConfig retCodeConfig;

    private final ILotMethod lotMethod;

    private final IProcessMethod processMethod;

    private final BaseCoreFactory baseCoreFactory;

    private final CimFrameWorkGlobals cimFrameWorkGlobals;

    private final DispatchingManager dispatchingManager;

    private final IMinQTimeMethod minQTimeMethod;

    @Autowired
    public BondingFlowMethod(RetCodeConfigEx retCodeConfigEx,
                             RetCodeConfig retCodeConfig,
                             ILotMethod lotMethod,
                             IProcessMethod processMethod,
                             BaseCoreFactory baseCoreFactory,
                             CimFrameWorkGlobals cimFrameWorkGlobals,
                             DispatchingManager dispatchingManager,
                             IMinQTimeMethod minQTimeMethod) {
        this.retCodeConfigEx = retCodeConfigEx;
        this.retCodeConfig = retCodeConfig;
        this.lotMethod = lotMethod;
        this.processMethod = processMethod;
        this.baseCoreFactory = baseCoreFactory;
        this.cimFrameWorkGlobals = cimFrameWorkGlobals;
        this.dispatchingManager = dispatchingManager;
        this.minQTimeMethod = minQTimeMethod;
    }

    @Override
    public List<Infos.LotInBondingFlowInfo> bondingFlowLotListGetDR(Infos.ObjCommon objCommon,
                                                                    List<Infos.HashedInfo> searchConditionSeq) {
        int size = (int) Math.round((((double) CimArrayUtils.getSize(searchConditionSeq)) * 1.5)) + 1;
        Map<String, String> strSearchConditionSeq = new HashMap<>(size);
        searchConditionSeq.forEach(condition -> strSearchConditionSeq.put(condition.getHashKey(), condition.getHashData()));

        ObjectIdentifier targetEquipmentID = ObjectIdentifier.emptyIdentifier();
        targetEquipmentID.setValue(strSearchConditionSeq.get(BizConstant.SP_HASHKEY_TARGETEQUIPMENTID));
        ObjectIdentifier baseLotID = ObjectIdentifier.emptyIdentifier();
        baseLotID.setValue(strSearchConditionSeq.get(BizConstant.SP_HASHKEY_LOTID));
        String searchFlowSectionName = strSearchConditionSeq.get(BizConstant.SP_HASHKEY_SECTIONNAME);
        String searchProductID = strSearchConditionSeq.get(BizConstant.SP_HASHKEY_PRODUCTID);
        String searchLotType = strSearchConditionSeq.get(BizConstant.SP_HASHKEY_LOTTYPE);
        String searchSubLoType = strSearchConditionSeq.get(BizConstant.SP_HASHKEY_SUBLOTTYPE);

        String baseLotId = ObjectIdentifier.fetchValue(baseLotID);
        if (CimStringUtils.isNotEmpty(baseLotId)) {
            //-------------------------------------------------------------------------//
            // Get PosLot information from FRLOT                                       //
            //-------------------------------------------------------------------------//
            String sql =
                    "SELECT ID, LOT_ID, BONDING_SECTION\n" +
                    "FROM   OMLOT\n" +
                    "WHERE  LOT_ID = ?1";
            Object[] sqlResult = cimJpaRepository.queryOne(sql, baseLotId);
            String hFRLOTLOT_OBJ = String.valueOf(sqlResult[0]);
            String hFRLOTLOT_ID = String.valueOf(sqlResult[1]);
            String hFRLOTBOND_FLOW_NAME = String.valueOf(sqlResult[2]);
            Validations.check(CimStringUtils.isEmpty(hFRLOTBOND_FLOW_NAME), retCodeConfigEx.getInvalidLotSituation(), baseLotId);

            ObjectIdentifier tmpBaseLotID = ObjectIdentifier.build(hFRLOTLOT_ID, hFRLOTLOT_OBJ);

            //---------------------------------------------------------------------------------
            //  Get Bonding Group ID
            //---------------------------------------------------------------------------------
            String bondingGroupId = lotMethod.lotBondingGroupIDGetDR(objCommon, tmpBaseLotID);
            Validations.check(CimStringUtils.isNotEmpty(bondingGroupId), retCodeConfigEx.getLotHasBondingGroup(),
                    baseLotId, bondingGroupId);

            //---------------------------------------
            // Get Target Operation information
            //---------------------------------------
            Outputs.ObjLotBondingOperationInfoGetDROut strLot_bondingOperationInfo_GetDR_out =
                    lotMethod.lotBondingOperationInfoGetDR(objCommon, tmpBaseLotID);

            ObjectIdentifier baseOperationID = strLot_bondingOperationInfo_GetDR_out.getTargetOperationID();
            ObjectIdentifier baseProductID = strLot_bondingOperationInfo_GetDR_out.getProductID();

            //-------------------------------------
            // select Logical Recipe information
            //-------------------------------------
            ObjectIdentifier targetLogicalRecipeID = processMethod.processlogicalRecipeGetDR(objCommon, baseProductID, baseOperationID);

            //-------------------------------------
            // Determine Top Lot or Base Lot
            //-------------------------------------
            ObjectIdentifier topProductID = ObjectIdentifier.emptyIdentifier();
            try {
                List<Infos.BOMPartsInfo> bomPartsInfos = processMethod.processBOMPartsInfoGetDR(objCommon, baseProductID, baseOperationID);
                Validations.check(CimArrayUtils.isEmpty(bomPartsInfos), retCodeConfigEx.getInvalidLotSituation(), baseLotId);
                if (bomPartsInfos.size() == 1){
                    topProductID = bomPartsInfos.get(0).getPartID();
                }
            } catch (ServiceException se) {
                Integer retCode = se.getCode();
                if (retCode != retCodeConfig.getBomNotDefined().getCode() &&
                        retCode != retCodeConfigEx.getPartsNotDefinedForProcess().getCode()) {
                    log.info("process_BOMPartsInfo_GetDR() != RC_OK : " + retCode);
                    throw se;
                }
            }
            String lotQuerySql =
                    "SELECT  LOT_ID, ID, LOT_TYPE, SUB_LOT_TYPE, LOT_PRODUCTION_STATE, LOT_FINISHED_STATE,\n" +
                            "PROPE_RKEY, PRFCX_RKEY, MAIN_PROCESS_ID, MAIN_PROCESS_RKEY, OPE_NO, LOT_PRIORITY, QTY,\n" +
                            "TRX_TIME, STATE_CHG_TIME, QUEUED_TIME, PLAN_END_TIME, INV_CHG_TIME\n" +
                            "FROM    OMLOT\n" +
                            "WHERE   BONDING_SECTION    = ?1\n" +
                            "AND     LOT_STATE         = ?2\n" +
                            "AND     LOT_HOLD_STATE    = ?3\n" +
                            "AND     LOT_PROCESS_STATE = ?4\n" +
                            "AND     LOT_INV_STATE     = ?5\n" +
                            "AND     PP_FLAG = ?6";
            String hFRLOTLOT_STATE = BizConstant.CIMFW_LOT_STATE_ACTIVE;
            String hFRLOTLOT_HOLD_STATE = BizConstant.CIMFW_LOT_HOLDSTATE_NOTONHOLD;
            String hFRLOTLOT_PROCESS_STATE = BizConstant.SP_LOT_PROCSTATE_WAITING;
            String hFRLOTLOT_INV_STATE = BizConstant.SP_LOT_INVENTORYSTATE_ONFLOOR;
            Boolean hFRLOTPOST_PROCESS_FLAG = false;
            List<CimLotDO> lotQueryResult = cimJpaRepository.query(lotQuerySql, CimLotDO.class,
                    hFRLOTBOND_FLOW_NAME,
                    hFRLOTLOT_STATE,
                    hFRLOTLOT_HOLD_STATE,
                    hFRLOTLOT_PROCESS_STATE,
                    hFRLOTLOT_INV_STATE,
                    hFRLOTPOST_PROCESS_FLAG);
            List<Infos.LotInBondingFlowInfo> tmpLotInBondingFlowInfoSeq = new ArrayList<>(lotQueryResult.size());
            for (CimLotDO cimLotDO : lotQueryResult) {
                cimLotDO.setLotState(hFRLOTLOT_STATE);
                cimLotDO.setLotHoldState(hFRLOTLOT_HOLD_STATE);
                cimLotDO.setLotProcessState(hFRLOTLOT_PROCESS_STATE);
                cimLotDO.setLotInventoryState(hFRLOTLOT_INV_STATE);
                cimLotDO.setPostProcessFlag(hFRLOTPOST_PROCESS_FLAG);
                CimLot cimLot = baseCoreFactory.getBO(CimLot.class, cimLotDO);
                hFRLOTLOT_ID = cimLot.getIdentifier();
                hFRLOTLOT_OBJ = cimLot.getPrimaryKey();
                ObjectIdentifier tmpLotID = ObjectIdentifier.build(hFRLOTLOT_ID, hFRLOTLOT_OBJ);

                //---------------------------------------------------------------------------------
                //  Get Bonding Group ID
                //---------------------------------------------------------------------------------
                try {
                    String _bondingGroupId = lotMethod.lotBondingGroupIDGetDR(objCommon, tmpLotID);
                    if (CimStringUtils.isNotEmpty(_bondingGroupId)) {
                        log.debug(String.format("Lot [%s] belongs Bonding Group [%s]. ", hFRLOTLOT_ID, _bondingGroupId));
                        continue;
                    }
                } catch (ServiceException se) {
                    log.debug("lot_bondingGroupID_GetDR() != RC_OK");
                }

                //---------------------------------------
                // Get Target Operation information
                //---------------------------------------
                ObjectIdentifier productID;
                ObjectIdentifier targetOperationID;
                Outputs.ObjLotBondingOperationInfoGetDROut _strLot_bondingOperationInfo_GetDR_out;
                try {
                    _strLot_bondingOperationInfo_GetDR_out =
                            lotMethod.lotBondingOperationInfoGetDR(objCommon, tmpLotID);
                    productID = _strLot_bondingOperationInfo_GetDR_out.getProductID();
                    targetOperationID = _strLot_bondingOperationInfo_GetDR_out.getTargetOperationID();
                    String productId = ObjectIdentifier.fetchValue(productID);
                    String topProductId = ObjectIdentifier.fetchValue(topProductID);
                    if (!CimStringUtils.equals(productId, topProductId)) {
                        log.debug("Top Product ID is not same.");
                        continue;
                    }
                } catch (ServiceException se) {
                    log.debug("### lot_bondingOperationInfo_GetDR() != RC_OK");
                    continue;
                }

                //-------------------------------------
                // select Logical Recipe information
                //-------------------------------------
                try{
                    ObjectIdentifier logicalRecipeID = processMethod.processlogicalRecipeGetDR(objCommon,
                            productID,
                            targetOperationID);
                    if (CimStringUtils.equals(ObjectIdentifier.fetchValue(logicalRecipeID), ObjectIdentifier.fetchValue(targetEquipmentID))) {
                        log.debug("Target LogicalRecipe ID is not same.");
                        continue;
                    }
                } catch (ServiceException se) {
                    log.debug("#### process_logicalRecipe_GetDR() != RC_OK ");
                    continue;
                }

                //-------------------------------------
                // Determine Top Lot or Base Lot
                //-------------------------------------
                try {
                    List<Infos.BOMPartsInfo> bomPartsInfos = processMethod.processBOMPartsInfoGetDR(objCommon, productID, targetOperationID);
                    if (CimArrayUtils.isEmpty(bomPartsInfos)) {
                        log.debug("Product is Parts at Operation");
                    } else if (bomPartsInfos.size() == 1) {
                        log.debug("Bonding Category is Base");
                        continue;
                    }
                } catch (ServiceException se) {
                    Integer retCode = se.getCode();
                    if (retCode != retCodeConfig.getBomNotDefined().getCode() &&
                            retCode != retCodeConfigEx.getPartsNotDefinedForProcess().getCode()) {
                        log.info("process_BOMPartsInfo_GetDR() != RC_OK : " + retCode);
                    }
                }
                //---------------------------------------------------------------------------------
                //  Set Data
                //---------------------------------------------------------------------------------
                Infos.LotInBondingFlowInfo tmpLotInBondingFlowInfo = setData(objCommon, cimLot,
                        _strLot_bondingOperationInfo_GetDR_out);
                tmpLotInBondingFlowInfoSeq.add(tmpLotInBondingFlowInfo);
                tmpLotInBondingFlowInfo.setProductID(productID);
                tmpLotInBondingFlowInfo.setTargetOpeID(targetOperationID);
                tmpLotInBondingFlowInfo.setBondingFlowSectionName(hFRLOTBOND_FLOW_NAME);
                tmpLotInBondingFlowInfo.setTargetLogicalRecipeID(targetLogicalRecipeID);
                tmpLotInBondingFlowInfo.setBondingCategory(BizConstant.SP_LOT_BONDINGCATEGORY_TOP);
                tmpLotInBondingFlowInfo.setTopProductID(ObjectIdentifier.emptyIdentifier());
            }
            return tmpLotInBondingFlowInfoSeq;
        } else {
            List<Object> params = new ArrayList<>(15);
            String lotQuerySql =
                    "SELECT  LOT_ID, ID, LOT_TYPE, SUB_LOT_TYPE, LOT_PRODUCTION_STATE, LOT_FINISHED_STATE, \n" +
                            "PROPE_RKEY, PRFCX_RKEY, MAIN_PROCESS_ID, MAIN_PROCESS_RKEY, OPE_NO, LOT_PRIORITY, QTY, \n" +
                            "TRX_TIME, STATE_CHG_TIME, QUEUED_TIME, PLAN_END_TIME, INV_CHG_TIME ,BONDING_SECTION \n" +
                            "FROM    OMLOT \n" +
                            "WHERE   LOT_STATE         = ?1 \n" +
                            "AND     LOT_HOLD_STATE    = ?2 \n" +
                            "AND     LOT_PROCESS_STATE = ?3 \n" +
                            "AND     LOT_INV_STATE     = ?4 \n" +
                            "AND    (PP_FLAG = '0' OR PP_FLAG IS NULL) ";
            String hFRLOTLOT_STATE = BizConstant.CIMFW_LOT_STATE_ACTIVE;
            params.add(hFRLOTLOT_STATE);
            String hFRLOTLOT_HOLD_STATE = BizConstant.CIMFW_LOT_HOLDSTATE_NOTONHOLD;
            params.add(hFRLOTLOT_HOLD_STATE);
            String hFRLOTLOT_PROCESS_STATE = BizConstant.SP_LOT_PROCSTATE_WAITING;
            params.add(hFRLOTLOT_PROCESS_STATE);
            String hFRLOTLOT_INV_STATE = BizConstant.SP_LOT_INVENTORYSTATE_ONFLOOR;
            params.add(hFRLOTLOT_INV_STATE);
            if (CimStringUtils.isNotEmpty(searchFlowSectionName)) {
                lotQuerySql += String.format(" AND BONDING_SECTION = ?%d", params.size() + 1);
                params.add(searchFlowSectionName);
            } else {
                lotQuerySql += " AND BONDING_SECTION IS NOT NULL";
            }

            if (CimStringUtils.isNotEmpty(searchProductID)) {
                lotQuerySql += String.format(" AND PROD_ID LIKE ?%d", params.size() + 1);
                params.add(searchProductID);
            }

            if (CimStringUtils.isNotEmpty(searchLotType)) {
                lotQuerySql += String.format(" AND LOT_TYPE = ?%d", params.size() + 1);
                params.add(searchLotType);
            }

            if (CimStringUtils.isNotEmpty(searchSubLoType)) {
                lotQuerySql += String.format(" AND SUB_LOT_TYPE = ?%d", params.size() + 1);
                params.add(searchSubLoType);
            }

            List<CimLotDO> lotQueryResult = cimJpaRepository.query(lotQuerySql, CimLotDO.class, params.toArray());
            List<Infos.LotInBondingFlowInfo> tmpLotInBondingFlowInfoSeq = new ArrayList<>(lotQueryResult.size());
            for (CimLotDO cimLotDO : lotQueryResult) {
                cimLotDO.setLotState(hFRLOTLOT_STATE);
                cimLotDO.setLotHoldState(hFRLOTLOT_HOLD_STATE);
                cimLotDO.setLotProcessState(hFRLOTLOT_PROCESS_STATE);
                cimLotDO.setLotInventoryState(hFRLOTLOT_INV_STATE);
                cimLotDO.setPostProcessFlag(false);
                CimLot cimLot = baseCoreFactory.getBO(CimLot.class, cimLotDO);
                String lotId = cimLot.getIdentifier();
                ObjectIdentifier tmpLotID = ObjectIdentifier.build(lotId, cimLot.getPrimaryKey());

                //---------------------------------------------------------------------------------
                //  Get Bonding Group ID
                //---------------------------------------------------------------------------------
                try {
                    String _bondingGroupId = lotMethod.lotBondingGroupIDGetDR(objCommon, tmpLotID);
                    if (CimStringUtils.isNotEmpty(_bondingGroupId)) {
                        log.debug(String.format("Lot [%s] belongs Bonding Group [%s]. ", lotId, _bondingGroupId));
                        continue;
                    }
                } catch (ServiceException se) {
                    log.debug("lot_bondingGroupID_GetDR() != RC_OK");
                }

                //---------------------------------------
                // Get Target Operation information
                //---------------------------------------
                ObjectIdentifier productID;
                ObjectIdentifier targetOperationID;
                Outputs.ObjLotBondingOperationInfoGetDROut _strLot_bondingOperationInfo_GetDR_out;
                try {
                    _strLot_bondingOperationInfo_GetDR_out = lotMethod
                            .lotBondingOperationInfoGetDR(objCommon, tmpLotID);
                    productID = _strLot_bondingOperationInfo_GetDR_out.getProductID();
                    targetOperationID = _strLot_bondingOperationInfo_GetDR_out.getTargetOperationID();
                } catch (ServiceException se) {
                    log.debug("### lot_bondingOperationInfo_GetDR() != RC_OK");
                    continue;
                }

                //-----------------------------------------------------
                // Get dispatch equipment list for target operation
                //-----------------------------------------------------
                try {
                    List<ObjectIdentifier> dispatchEquipmentsGetDR = processMethod
                            .processDispatchEquipmentsGetDR(objCommon, productID, targetOperationID);
                    //------------------------------------------------------------------
                    // Find the target equipment from the dispatch equipment list
                    //------------------------------------------------------------------
                    boolean notFoundFlag = dispatchEquipmentsGetDR.stream().parallel()
                            .noneMatch(id -> id.equals(targetEquipmentID));
                    if (notFoundFlag) {
                        log.debug("targetEquipmentFoundFlag != TRUE");
                        continue;
                    }
                } catch (ServiceException se) {
                    log.debug("#### process_dispatchEquipments_GetDR != RC_OK : rc = " + se.getCode());
                    continue;
                }

                //---------------------------------------------------------------------------------
                //  Set Data
                //---------------------------------------------------------------------------------
                Infos.LotInBondingFlowInfo tmpLotInBondingFlowInfo = this.setData(objCommon, cimLot, _strLot_bondingOperationInfo_GetDR_out);
                tmpLotInBondingFlowInfoSeq.add(tmpLotInBondingFlowInfo);

                //-------------------------------------
                // select Logical Recipe information
                //-------------------------------------
                try {
                    ObjectIdentifier logicalRecipeID = processMethod.processlogicalRecipeGetDR(objCommon, productID, targetOperationID);
                    tmpLotInBondingFlowInfo.setTargetLogicalRecipeID(logicalRecipeID);
                } catch (ServiceException se) {
                    tmpLotInBondingFlowInfo.setTargetLogicalRecipeID(ObjectIdentifier.emptyIdentifier());
                }

                //-------------------------------------
                // Determine Top Lot or Base Lot
                //-------------------------------------
                List<Infos.BOMPartsInfo> bomPartsInfos = new ArrayList<>();
                try {
                    bomPartsInfos = processMethod.processBOMPartsInfoGetDR(objCommon, productID, targetOperationID);
                } catch (ServiceException se) {
                    Integer retCode = se.getCode();
                    if (retCode != retCodeConfig.getBomNotDefined().getCode() &&
                            retCode != retCodeConfigEx.getPartsNotDefinedForProcess().getCode()) {
                        log.info("process_BOMPartsInfo_GetDR() != RC_OK : " + retCode);
                    }
                }
                if (CimArrayUtils.isEmpty(bomPartsInfos)) {
                    tmpLotInBondingFlowInfo.setBondingCategory(BizConstant.SP_LOT_BONDINGCATEGORY_TOP);
                } else if (bomPartsInfos.size() == 1){
                    tmpLotInBondingFlowInfo.setBondingCategory(BizConstant.SP_LOT_BONDINGCATEGORY_BASE);
                    tmpLotInBondingFlowInfo.setTopProductID(bomPartsInfos.get(0).getPartID());
                }
            }
            List<ObjectIdentifier> lotIDSeq = new ArrayList<>(tmpLotInBondingFlowInfoSeq.size());
            tmpLotInBondingFlowInfoSeq.forEach(info -> lotIDSeq.add(info.getLotID()));
            CimMachine cimMachine = baseCoreFactory.getBO(CimMachine.class, targetEquipmentID);
            CimDispatcher dispatcher = cimMachine.getDispatcher();
            Validations.check(dispatcher == null, retCodeConfig.getNotFoundEqpDispatcher());
            DispatcherDTO.EffectiveWhatNextLogic effectiveWhatNextLogic = dispatcher.currentWhatNextForMachine();
            List<GlobalDTO.LogicInfo> logicInfos = cimFrameWorkGlobals.convertToLogicInfoFromParameters(effectiveWhatNextLogic.getParameters());
            List<ObjectIdentifier> tempLotIDs = Collections.emptyList();
            for (GlobalDTO.LogicInfo logicInfo : logicInfos) {
                CimWhatNextLogicBase whatNextLogicObjectNamed = dispatchingManager.findWhatNextLogicObjectNamed(logicInfo.getLogicName());
                if (whatNextLogicObjectNamed != null) {
                    tempLotIDs = whatNextLogicObjectNamed.dispatchForInSQL(logicInfo.getParams(), lotIDSeq, cimMachine);
                    break;
                }
            }

            //--------------------------------------------------------------
            //   Copy lot from lotSeq to lotList
            //--------------------------------------------------------------
            List<Infos.LotInBondingFlowInfo> lotList = new ArrayList<>(tmpLotInBondingFlowInfoSeq.size());
            tempLotIDs.forEach(tempLotID -> {
                String tempLotId = ObjectIdentifier.fetchValue(tempLotID);
                tmpLotInBondingFlowInfoSeq.forEach(tmpLotInBondingFlowInfo -> {
                    String tmpLotInBondingId = ObjectIdentifier.fetchValue(tmpLotInBondingFlowInfo.getLotID());
                    if (CimStringUtils.equals(tempLotId, tmpLotInBondingId)) {
                        lotList.add(tmpLotInBondingFlowInfo);
                    }
                });
            });
            return CimArrayUtils.isNotEmpty(lotList) ? lotList : Collections.emptyList();
        }
    }

    private Infos.LotInBondingFlowInfo setData(Infos.ObjCommon objCommon,
                                               CimLot cimLot,
                                               Outputs.ObjLotBondingOperationInfoGetDROut strLot_bondingOperationInfo_GetDR_out) {

        String tLotId = cimLot.getIdentifier();
        ObjectIdentifier tmpLotID = ObjectIdentifier.build(tLotId, cimLot.getPrimaryKey());
        String tPlannedCompleteTimestamp = String.valueOf(cimLot.getPlannedCompletionDateTime());
        Infos.LotInBondingFlowInfo tmpLotInBondingFlowInfo = new Infos.LotInBondingFlowInfo();
        tmpLotInBondingFlowInfo.setLotID(tmpLotID);
        tmpLotInBondingFlowInfo.setProductID(cimLot.getProductSpecificationID());
        tmpLotInBondingFlowInfo.setLotType(cimLot.getLotType());
        tmpLotInBondingFlowInfo.setSubLotType(cimLot.getSubLotType());
        tmpLotInBondingFlowInfo.setLotStatus(cimLot.getState());
        List<Infos.LotStatusList> strLotStatusListSeq = new ArrayList<>(6);
        tmpLotInBondingFlowInfo.setStrLotStatusListSeq(strLotStatusListSeq);
        strLotStatusListSeq.add(new Infos.LotStatusList(BizConstant.SP_LOTSTATECAT_STATE, cimLot.getLotState()));
        strLotStatusListSeq.add(new Infos.LotStatusList(BizConstant.SP_LOTSTATECAT_PRODUCTIONSTATE, cimLot.getLotProductionState()));
        strLotStatusListSeq.add(new Infos.LotStatusList(BizConstant.SP_LOTSTATECAT_HOLDSTATE, cimLot.getLotHoldState()));
        strLotStatusListSeq.add(new Infos.LotStatusList(BizConstant.SP_LOTSTATECAT_FINISHEDSTATE, cimLot.getLotFinishedState()));
        strLotStatusListSeq.add(new Infos.LotStatusList(BizConstant.SP_LOTSTATECAT_PROCSTATE, cimLot.getLotProcessState()));
        strLotStatusListSeq.add(new Infos.LotStatusList(BizConstant.SP_LOTSTATECAT_INVENTORYSTATE, cimLot.getLotInventoryState()));
        tmpLotInBondingFlowInfo.setTotalWaferCount(CimNumberUtils.longValue(cimLot.getProductQuantity()));
        tmpLotInBondingFlowInfo.setLastClaimedTimeStamp(String.valueOf(cimLot.getLastClaimedTimeStamp()));
        tmpLotInBondingFlowInfo.setStateChangeTimeStamp(String.valueOf(cimLot.getStateChangedTimeStamp()));
        tmpLotInBondingFlowInfo.setInventoryChangeTimeStamp(String.valueOf(cimLot.getInventoryStateChangedTimeStamp()));
        tmpLotInBondingFlowInfo.setDueTimeStamp(tPlannedCompleteTimestamp);
        tmpLotInBondingFlowInfo.setQueuedTimeStamp(String.valueOf(cimLot.getQueuedTimeStamp()));
        tmpLotInBondingFlowInfo.setTargetOpeID(strLot_bondingOperationInfo_GetDR_out.getTargetOperationID());
        tmpLotInBondingFlowInfo.setPriorityClass(String.valueOf(cimLot.getPriorityClass()));
        tmpLotInBondingFlowInfo.setExternalPriority(String.valueOf(cimLot.getPriority()));
        CimProcessDefinition mainProcessDefinition = cimLot.getMainProcessDefinition();
        tmpLotInBondingFlowInfo.setCurrentRouteID(ObjectIdentifier.build(mainProcessDefinition.getIdentifier(),
                mainProcessDefinition.getPrimaryKey()));
        tmpLotInBondingFlowInfo.setCurrentOpeNo(cimLot.getOperationNumber());
        tmpLotInBondingFlowInfo.setTargetRouteID(strLot_bondingOperationInfo_GetDR_out.getTargetRouteID());
        tmpLotInBondingFlowInfo.setTargetOpeNo(strLot_bondingOperationInfo_GetDR_out.getTargetOperationNumber());
        tmpLotInBondingFlowInfo.setBondingFlowSectionName(strLot_bondingOperationInfo_GetDR_out.getBondingFlowSectionName());

        //---------------------------------------
        // select cassette information
        //---------------------------------------
        String cassetteQuerySql =
                "SELECT  OMCARRIER.CARRIER_ID,    OMCARRIER.ID\n" +
                        "FROM    OMCARRIER, OMCARRIER_LOT\n" +
                        "WHERE   OMCARRIER_LOT.LOT_ID     = ?1 " +
                        "AND OMCARRIER.ID = OMCARRIER_LOT.REFKEY";
        Object[] casseteQueryResult = cimJpaRepository.queryOne(cassetteQuerySql, tLotId);
        if (!CimObjectUtils.isEmpty(casseteQueryResult)){
            tmpLotInBondingFlowInfo.setCarrierID(ObjectIdentifier.build(String.valueOf(casseteQueryResult[0]),
                    String.valueOf(casseteQueryResult[1])));
        }

        //-------------------------------------
        // select PO information
        //-------------------------------------
        CimProcessOperation processOperation = cimLot.getProcessOperation();
        String mainPfObj = processOperation.getMainProcessFlow().getPrimaryKey();
        String modulePfObj = processOperation.getModuleProcessFlow().getPrimaryKey();
        String opeNumber = processOperation.getOperationNumber();
        CimProcessDefinition processDefinition = processOperation.getProcessDefinition();
        String hFRPOPD_ID = processDefinition.getIdentifier();
        String hFRPOPD_OBJ = processDefinition.getPrimaryKey();
        String hFRPOPLAN_START_TIME = String.valueOf(processOperation.getPlannedStartTimeStamp());
        String hFRPOPLAN_END_TIME = String.valueOf(processOperation.getPlannedCompTimeStamp());
        CimMachine plannedMachine = processOperation.getPlannedMachine();
        String hFRPOPLAN_EQP_ID = (CimObjectUtils.isEmpty(plannedMachine))?null:plannedMachine.getIdentifier();
        String hFRPOPLAN_EQP_OBJ = (CimObjectUtils.isEmpty(plannedMachine))?null:plannedMachine.getPrimaryKey();
        Double hFRPOREMAIN_CYCLE_TIME = processOperation.getRemainingCycleTime();
        tmpLotInBondingFlowInfo.setCurrentOpeID(ObjectIdentifier.build(hFRPOPD_ID, hFRPOPD_OBJ));
        if (!CimStringUtils.equals(hFRPOPLAN_START_TIME, BizConstant.SP_TIMESTAMP_NIL_OBJECT_STRING)) {
            tmpLotInBondingFlowInfo.setPlanStartTimeStamp(hFRPOPLAN_START_TIME);
        }

        if (!CimStringUtils.equals(hFRPOPLAN_END_TIME, BizConstant.SP_TIMESTAMP_NIL_OBJECT_STRING)) {
            tmpLotInBondingFlowInfo.setPlanEndTimeStamp(hFRPOPLAN_END_TIME);
        }

        tmpLotInBondingFlowInfo.setPlannedEquipmentID(ObjectIdentifier.build(hFRPOPLAN_EQP_ID, hFRPOPLAN_EQP_OBJ));

        //-------------------------------------
        // calculate internal priority
        //-------------------------------------
        Timestamp currentTimeStamp = CimDateUtils.getCurrentTimeStamp();
        Timestamp aPlannedCompDataTime = CimDateUtils.convertToOrInitialTime(tPlannedCompleteTimestamp);
        Long aPlannedCompDuration = CimDateUtils.substractTimeStamp(aPlannedCompDataTime.getTime(), currentTimeStamp.getTime());
        double aRemainingCycleTime = CimNumberUtils.doubleValue(hFRPOREMAIN_CYCLE_TIME);
        log.debug("aPlannedCompDuration " + aPlannedCompDuration);
        log.debug("aRemainingCycleTime" + aRemainingCycleTime);

        if (aRemainingCycleTime == 0D) {
            tmpLotInBondingFlowInfo.setInternalPriority("0");
        } else {
            double dInternalPriority = CimNumberUtils.doubleValue(aPlannedCompDuration) / (aRemainingCycleTime * 60 * 1000);
            String format = aPlannedCompDataTime.before(currentTimeStamp) ? "-%.4f" : "%.4f";
            tmpLotInBondingFlowInfo.setInternalPriority(String.format(format, dInternalPriority));
        }

        //-------------------------------------
        // select previous PO information
        //-------------------------------------
        /*
         * Equivilent to
         * SELECT FRPFX_POLIST.D_THESYSTEMKEY,
         *               FRPFX_POLIST.D_SEQNO
         *        FROM   FRPFX_POLIST,
         *               FRPFX
         *        WHERE  FRPFX_POLIST.PO_OBJ = FRPFX.CUR_PO_OBJ AND
         *               FRPFX_POLIST.D_THESYSTEMKEY = FRPFX.D_THESYSTEMKEY AND
         *               FRPFX.PFX_OBJ = :hFRLOTPFX_OBJ;
         *
         *
         * SELECT FRPO.ACTUAL_END_TIME
         *        FROM   FRPO,
         *               FRPFX_POLIST
         *        WHERE  FRPFX_POLIST.D_THESYSTEMKEY = :hFRPFXd_theSystemKey AND
         *               FRPFX_POLIST.D_SEQNO = :hFRPFX_POLISTd_SeqNo AND
         *               FRPO.PO_OBJ = FRPFX_POLIST.PO_OBJ;
         */
        CimProcessFlowContext processFlowContext = cimLot.getProcessFlowContext();

        CimProcessOperation prePo = processFlowContext.getPreviousProcessOperation();
        String prePoActualCompleteTimestamp = (CimObjectUtils.isEmpty(prePo))?null:String.valueOf(prePo.getActualCompTimeStamp());
        tmpLotInBondingFlowInfo.setPreOperationCompTimeStamp(prePoActualCompleteTimestamp);

        //==================================================
        //   Get QTime info for Lot
        //==================================================

        //---------------------------------------
        //  Check current route.
        //---------------------------------------
        // Max Q-Time
        List<Infos.LotQtimeInfo> strLot_qtime_GetForRouteDR_out = lotMethod.lotQtimeGetForRouteDR(objCommon,
                tmpLotID,
                mainPfObj,
                modulePfObj,
                opeNumber);
        List<Infos.LotQtimeInfo> strLotQtimeInfo = new ArrayList<>(strLot_qtime_GetForRouteDR_out);
        tmpLotInBondingFlowInfo.setStrLotQtimeInfo(strLotQtimeInfo);

        // Min Q-Time
        List<Infos.LotQtimeInfo> minQTimeInfos = minQTimeMethod.getRestrictInProcessArea(tmpLotID, mainPfObj);
        tmpLotInBondingFlowInfo.setMinQTimeInfos(minQTimeInfos);

        //--------------------------------------------------------
        //  Select QTime restriction for Lot on Branch Route.
        //--------------------------------------------------------
        /*
         * Equivilent to
         * SELECT OPE_NO,
         *                             MAIN_PF_OBJ,
         *                             MODULE_PF_OBJ
         *                      FROM   FRPFX_RETNLIST
         *                      WHERE  D_THESYSTEMKEY = :hFRPFXd_theSystemKey
         *                      FOR READ ONLY;
         */
        List<ProcessDTO.ReturnOperation> returnOperations = processFlowContext.allReturnOperations();
        returnOperations.forEach(returnOperation -> {
            String returnOpeNumber = returnOperation.getOperationNumber();
            String returnMainPfObj = returnOperation.getMainProcessFlow();
            String returnModulePfObj = returnOperation.getModuleProcessFlow();

            strLotQtimeInfo.addAll(lotMethod.lotQtimeGetForRouteDR(objCommon,
                    tmpLotID,
                    returnMainPfObj,
                    returnModulePfObj,
                    returnOpeNumber));

            minQTimeInfos.addAll(minQTimeMethod.getRestrictInProcessArea(tmpLotID, returnMainPfObj));
        });
        tmpLotInBondingFlowInfo.setQtimeFlag(CimArrayUtils.isNotEmpty(strLotQtimeInfo));
        tmpLotInBondingFlowInfo.setMinQTimeFlag(CimArrayUtils.isNotEmpty(minQTimeInfos));
        return tmpLotInBondingFlowInfo;
    }

    @Override
    public List<String> bondingFlowBondingFlowNameGetDR(Infos.ObjCommon objCommon) {
        String sql = "SELECT DISTINCT SECTION_NAME FROM OMPRSS WHERE SECTION_NAME IS NOT NULL ORDER BY SECTION_NAME";
        List<Object[]> sqlResult = cimJpaRepository.query(sql);
        if (CimArrayUtils.isEmpty(sqlResult)) {
            return Collections.emptyList();
        }
        List<String> retVal = new ArrayList<>(sqlResult.size());
        sqlResult.stream()
                .map(obj -> String.valueOf(obj[0]))
                .filter(CimStringUtils::isNotEmpty)
                .forEach(retVal::add);
        return retVal;
    }
}
