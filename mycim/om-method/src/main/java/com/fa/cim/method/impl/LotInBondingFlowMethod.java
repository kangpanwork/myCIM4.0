package com.fa.cim.method.impl;

import com.fa.cim.annotaion.OmMethod;
import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.utils.CimArrayUtils;
import com.fa.cim.common.utils.CimDateUtils;
import com.fa.cim.common.utils.CimNumberUtils;
import com.fa.cim.common.utils.CimStringUtils;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.config.RetCodeConfigEx;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Outputs;
import com.fa.cim.jpa.CimJpaRepository;
import com.fa.cim.method.ILotInBondingFlowMethod;
import com.fa.cim.method.ILotMethod;
import com.fa.cim.method.IMinQTimeMethod;
import com.fa.cim.method.IProcessMethod;
import com.fa.cim.newcore.bo.machine.CimMachine;
import com.fa.cim.newcore.bo.pd.CimProcessDefinition;
import com.fa.cim.newcore.bo.pd.CimProcessFlowContext;
import com.fa.cim.newcore.bo.pd.CimProcessOperation;
import com.fa.cim.newcore.bo.product.CimLot;
import com.fa.cim.newcore.dto.pd.ProcessDTO;
import com.fa.cim.newcore.factory.BaseCoreFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Timestamp;
import java.util.*;

@OmMethod
@Slf4j
public class LotInBondingFlowMethod implements ILotInBondingFlowMethod {

    private final BaseCoreFactory baseCoreFactory;

    private final ILotMethod lotMethod;

    private final CimJpaRepository cimJpaRepository;

    private final IProcessMethod processMethod;

    private final RetCodeConfig retCodeConfig;

    private final RetCodeConfigEx retCodeConfigEx;

    private final IMinQTimeMethod minQTimeMethod;

    @Autowired
    public LotInBondingFlowMethod(BaseCoreFactory baseCoreFactory,
                                  ILotMethod lotMethod,
                                  IProcessMethod processMethod,
                                  RetCodeConfig retCodeConfig,
                                  RetCodeConfigEx retCodeConfigEx,
                                  IMinQTimeMethod minQTimeMethod, CimJpaRepository cimJpaRepository) {
        this.baseCoreFactory = baseCoreFactory;
        this.lotMethod = lotMethod;
        this.processMethod = processMethod;
        this.retCodeConfig = retCodeConfig;
        this.retCodeConfigEx = retCodeConfigEx;
        this.minQTimeMethod = minQTimeMethod;
        this.cimJpaRepository = cimJpaRepository;
    }

    @Override
    public List<Infos.LotInBondingFlowInfo> lotsInBondingFlowRTDInterfaceReq(Infos.ObjCommon objCommon,
                                                                             String kind,
                                                                             ObjectIdentifier keyID) {
        return Collections.emptyList();
    }

    @Override
    public List<Infos.LotInBondingFlowInfo> lotsInBondingFlowLotInfoGetDR(Infos.ObjCommon objCommon,
                                                                          List<Infos.HashedInfo> searchConditionSeq,
                                                                          List<Infos.LotInBondingFlowInfo> strLotInBondingFlowInfoSeq) {
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

        int nLotLen = CimArrayUtils.getSize(strLotInBondingFlowInfoSeq);
        List<Infos.LotInBondingFlowInfo> tmpLotInBondingFlowInfoSeq = new ArrayList<>(nLotLen);
        for (Infos.LotInBondingFlowInfo info : strLotInBondingFlowInfoSeq) {
            ObjectIdentifier tmpLotID = info.getLotID();
            //-------------------------------------------------------------------------//
            // Get PosLot information from OMLOT                                       //
            //-------------------------------------------------------------------------//
            /*
             * equivlent to
             * EXEC SQL SELECT
             *                 LOT_OBJ,
             *                 LOT_TYPE,
             *                 SUB_LOT_TYPE,
             *                 LOT_STATE,
             *                 LOT_HOLD_STATE,
             *                 LOT_PROCESS_STATE,
             *                 LOT_INV_STATE,
             *                 LOT_PRODCTN_STATE,
             *                 LOT_FINISHED_STATE,
             *                 PO_OBJ,
             *                 PFX_OBJ,
             *                 MAINPD_ID,
             *                 MAINPD_OBJ,
             *                 OPE_NO,
             *                 PRIORITY_CLASS,
             *                 PRIORITY,
             *                 QTY,
             *                 CLAIM_TIME,
             *                 STATE_CHG_TIME,
             *                 QUEUED_TIME,
             *                 PLAN_END_TIME,
             *                 INV_CHG_TIME,
             *                 BOND_FLOW_NAME
             *             FROM
             *                 OMLOT
             *             WHERE
             *                 LOT_ID = :hFRLOTLOT_ID;
             *
             */
            CimLot cimLot = baseCoreFactory.getBO(CimLot.class, tmpLotID);
            String hFRLOTLOT_ID = cimLot.getIdentifier();
            String hFRLOTLOT_OBJ = cimLot.getPrimaryKey();
            String hFRLOTLOT_TYPE = cimLot.getLotType();
            String hFRLOTSUB_LOT_TYPE = cimLot.getSubLotType();
            String hFRLOTLOT_STATE = cimLot.getLotState();
            String hFRLOTLOT_HOLD_STATE = cimLot.getLotHoldState();
            String hFRLOTLOT_PROCESS_STATE = cimLot.getLotProcessState();
            String hFRLOTLOT_INV_STATE = cimLot.getLotInventoryState();
            String hFRLOTLOT_PRODCTN_STATE = cimLot.getLotProductionState();
            String hFRLOTLOT_FINISHED_STATE = cimLot.getLotFinishedState();
            CimProcessOperation processOperation = cimLot.getProcessOperation();
            CimProcessFlowContext processFlowContext = cimLot.getProcessFlowContext();
            CimProcessDefinition mainProcessDefinition = cimLot.getMainProcessDefinition();
            String hFRLOTMAINPD_ID = mainProcessDefinition.getIdentifier();
            String hFRLOTMAINPD_OBJ = mainProcessDefinition.getPrimaryKey();
            String hFRLOTOPE_NO = cimLot.getOperationNumber();
            String hFRLOTPRIORITY_CLASS = String.valueOf(cimLot.getPriorityClass());
            String hFRLOTPRIORITY = String.valueOf(cimLot.getPriority());
            long hFRLOTPROD_QTY = CimNumberUtils.longValue(cimLot.getProductQuantity());
            String hFRLOTCLAIM_TIME = String.valueOf(cimLot.getLastClaimedTimeStamp());
            String hFRLOTSTATE_CHG_TIME = String.valueOf(cimLot.getStateChangedTimeStamp());
            String hFRLOTQUEUED_TIME = String.valueOf(cimLot.getQueuedTimeStamp());
            String hFRLOTPLAN_END_TIME = String.valueOf(cimLot.getPlannedCompletionDateTime());
            String hFRLOTINV_CHG_TIME = String.valueOf(cimLot.getInventoryStateChangedTimeStamp());
            String hFRLOTBOND_FLOW_NAME = cimLot.getBondingFlowName();
            tmpLotID.setReferenceKey(hFRLOTLOT_OBJ);

            //---------------------------------------
            // Get Target Operation information
            //---------------------------------------
            Outputs.ObjLotBondingOperationInfoGetDROut strLot_bondingOperationInfo_GetDR_out = lotMethod
                    .lotBondingOperationInfoGetDR(objCommon, tmpLotID);
            if (CimStringUtils.isNotEmpty(searchFlowSectionName) &&
                    !CimStringUtils.equals(searchFlowSectionName, hFRLOTBOND_FLOW_NAME)) {
                log.debug(String.format("bondingFlowSectionName is not same %s vs %s", searchFlowSectionName, hFRLOTBOND_FLOW_NAME));
                continue;
            }

            ObjectIdentifier productID = strLot_bondingOperationInfo_GetDR_out.getProductID();
            if (CimStringUtils.isNotEmpty(searchProductID)) {
                boolean isWildCardSearch = searchProductID.contains(CimStringUtils.PERCENT);
                String productId = ObjectIdentifier.fetchValue(productID);
                if (isWildCardSearch) {
                    if (!CimStringUtils.wildCardCompare(searchProductID, productId)) {
                        log.debug(String.format("Search Product ID is not same %s vs %s", searchProductID, productId));
                        continue;
                    }
                } else {
                    if (!CimStringUtils.equals(searchProductID, productId)) {
                        log.debug(String.format("Search Product ID is not same %s vs %s", searchProductID, productId));
                        continue;
                    }
                }
            }

            if (CimStringUtils.isNotEmpty(searchLotType) && !CimStringUtils.equals(searchLotType, hFRLOTLOT_TYPE)) {
                log.debug(String.format("Search Lot Type is not same %s vs %s", searchLotType, hFRLOTLOT_TYPE));
                continue;
            }

            if (CimStringUtils.isNotEmpty(searchSubLoType) && !CimStringUtils.equals(searchSubLoType, hFRLOTSUB_LOT_TYPE)) {
                log.debug(String.format("Search Lot Type is not same %s vs %s", searchSubLoType, hFRLOTSUB_LOT_TYPE));
                continue;
            }

            //---------------------------------------------------------------------------------
            //  Set Data
            //---------------------------------------------------------------------------------
            Infos.LotInBondingFlowInfo tmpLotInBondingFlowInfo = new Infos.LotInBondingFlowInfo();
            tmpLotInBondingFlowInfoSeq.add(tmpLotInBondingFlowInfo);
            tmpLotInBondingFlowInfo.setLotID(tmpLotID);
            tmpLotInBondingFlowInfo.setLotType(hFRLOTLOT_TYPE);
            tmpLotInBondingFlowInfo.setSubLotType(hFRLOTSUB_LOT_TYPE);
            tmpLotInBondingFlowInfo.setLotStatus(cimLot.getState());
            List<Infos.LotStatusList> strLotStatusListSeq = new ArrayList<>(6);
            tmpLotInBondingFlowInfo.setStrLotStatusListSeq(strLotStatusListSeq);
            strLotStatusListSeq.add(new Infos.LotStatusList(BizConstant.SP_LOTSTATECAT_STATE, hFRLOTLOT_STATE));
            strLotStatusListSeq.add(new Infos.LotStatusList(BizConstant.SP_LOTSTATECAT_PRODUCTIONSTATE, hFRLOTLOT_PRODCTN_STATE));
            strLotStatusListSeq.add(new Infos.LotStatusList(BizConstant.SP_LOTSTATECAT_HOLDSTATE, hFRLOTLOT_HOLD_STATE));
            strLotStatusListSeq.add(new Infos.LotStatusList(BizConstant.SP_LOTSTATECAT_FINISHEDSTATE, hFRLOTLOT_FINISHED_STATE));
            strLotStatusListSeq.add(new Infos.LotStatusList(BizConstant.SP_LOTSTATECAT_PROCSTATE, hFRLOTLOT_PROCESS_STATE));
            strLotStatusListSeq.add(new Infos.LotStatusList(BizConstant.SP_LOTSTATECAT_INVENTORYSTATE, hFRLOTLOT_INV_STATE));
            tmpLotInBondingFlowInfo.setProductID(productID);
            tmpLotInBondingFlowInfo.setTotalWaferCount(hFRLOTPROD_QTY);
            tmpLotInBondingFlowInfo.setLastClaimedTimeStamp(hFRLOTCLAIM_TIME);
            tmpLotInBondingFlowInfo.setStateChangeTimeStamp(hFRLOTSTATE_CHG_TIME);
            tmpLotInBondingFlowInfo.setInventoryChangeTimeStamp(hFRLOTINV_CHG_TIME);
            tmpLotInBondingFlowInfo.setDueTimeStamp(hFRLOTPLAN_END_TIME);
            tmpLotInBondingFlowInfo.setQueuedTimeStamp(hFRLOTQUEUED_TIME);
            tmpLotInBondingFlowInfo.setPriorityClass(hFRLOTPRIORITY_CLASS);
            tmpLotInBondingFlowInfo.setExternalPriority(hFRLOTPRIORITY);
            tmpLotInBondingFlowInfo.setCurrentRouteID(ObjectIdentifier.build(hFRLOTMAINPD_ID, hFRLOTMAINPD_OBJ));
            tmpLotInBondingFlowInfo.setCurrentOpeNo(hFRLOTOPE_NO);
            tmpLotInBondingFlowInfo.setTargetRouteID(strLot_bondingOperationInfo_GetDR_out.getTargetRouteID());
            tmpLotInBondingFlowInfo.setTargetOpeNo(strLot_bondingOperationInfo_GetDR_out.getTargetOperationNumber());
            ObjectIdentifier targetOperationID = strLot_bondingOperationInfo_GetDR_out.getTargetOperationID();
            tmpLotInBondingFlowInfo.setTargetOpeID(targetOperationID);
            tmpLotInBondingFlowInfo.setBondingFlowSectionName(hFRLOTBOND_FLOW_NAME);

            //---------------------------------------
            // select cassette information
            //---------------------------------------
            String cassetteQuerySql =
                    "SELECT  OMCARRIER.CARRIER_ID,    OMCARRIER.ID\n" +
                    "FROM    OMCARRIER,\n" +
                    "OMCARRIER_LOT\n" +
                    "WHERE   OMCARRIER_LOT.LOT_ID     = ?1 " +
                    "AND OMCARRIER.ID = OMCARRIER_LOT.REFKEY";
            Object[] casseteQueryResult = cimJpaRepository.queryOne(cassetteQuerySql, hFRLOTLOT_ID);
            String hFRCASTCAST_ID = String.valueOf(casseteQueryResult[0]);
            String hFRCASTCAST_OBJ = String.valueOf(casseteQueryResult[1]);
            tmpLotInBondingFlowInfo.setCarrierID(ObjectIdentifier.build(hFRCASTCAST_ID, hFRCASTCAST_OBJ));

            //-------------------------------------
            // select PO information
            //-------------------------------------
            String hFRPOMAIN_PF_OBJ = processOperation.getMainProcessFlow().getPrimaryKey();
            String hFRPOMODULE_PF_OBJ = processOperation.getModuleProcessFlow().getPrimaryKey();
            String hFRPOOPE_NO = processOperation.getOperationNumber();
            CimProcessDefinition processDefinition = processOperation.getProcessDefinition();
            String hFRPOPD_ID = processDefinition.getIdentifier();
            String hFRPOPD_OBJ = processDefinition.getPrimaryKey();
            String hFRPOPLAN_START_TIME = String.valueOf(processOperation.getPlannedStartTimeStamp());
            String hFRPOPLAN_END_TIME = String.valueOf(processOperation.getPlannedCompTimeStamp());
            CimMachine plannedMachine = processOperation.getPlannedMachine();
            String hFRPOPLAN_EQP_ID = plannedMachine.getIdentifier();
            String hFRPOPLAN_EQP_OBJ = plannedMachine.getPrimaryKey();
            Double hFRPOREMAIN_CYCLE_TIME = processOperation.getRemainingCycleTime();
            tmpLotInBondingFlowInfo.setCurrentOpeID(ObjectIdentifier.build(hFRPOPD_ID, hFRPOPD_OBJ));
            if (!CimStringUtils.equals(hFRPOPLAN_START_TIME, BizConstant.SP_TIMESTAMP_NIL_OBJECT_STRING)) {
                tmpLotInBondingFlowInfo.setPlanStartTimeStamp(hFRPOPLAN_START_TIME);
            }

            if (!CimStringUtils.equals(hFRPOPLAN_END_TIME, BizConstant.SP_TIMESTAMP_NIL_OBJECT_STRING)) {
                tmpLotInBondingFlowInfo.setPlanStartTimeStamp(hFRPOPLAN_END_TIME);
            }

            tmpLotInBondingFlowInfo.setPlannedEquipmentID(ObjectIdentifier.build(hFRPOPLAN_EQP_ID, hFRPOPLAN_EQP_OBJ));

            //-------------------------------------
            // calculate internal priority
            //-------------------------------------
            Timestamp currentTimeStamp = CimDateUtils.getCurrentTimeStamp();
            Timestamp aPlannedCompDataTime = CimDateUtils.convertToOrInitialTime(hFRLOTPLAN_END_TIME);
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
            CimProcessOperation prePo = processFlowContext.getPreviousProcessOperation();
            String hFRPOACTUAL_END_TIME = String.valueOf(prePo.getActualCompTimeStamp());
            tmpLotInBondingFlowInfo.setPreOperationCompTimeStamp(hFRPOACTUAL_END_TIME);

            //==================================================
            //   Get QTime info for Lot
            //==================================================

            //---------------------------------------
            //  Check current route.
            //---------------------------------------
            // Max Q-Time
            List<Infos.LotQtimeInfo> strLot_qtime_GetForRouteDR_out = lotMethod.lotQtimeGetForRouteDR(objCommon,
                    tmpLotID,
                    hFRPOMAIN_PF_OBJ,
                    hFRPOMODULE_PF_OBJ,
                    hFRPOOPE_NO);
            List<Infos.LotQtimeInfo> strLotQtimeInfo = new ArrayList<>(strLot_qtime_GetForRouteDR_out);
            tmpLotInBondingFlowInfo.setStrLotQtimeInfo(strLotQtimeInfo);

            // Min Q-Time
            List<Infos.LotQtimeInfo> minQTimeInfos = minQTimeMethod.getRestrictInProcessArea(tmpLotID, hFRPOMAIN_PF_OBJ);
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
                String hFRPFX_RETNLISTOPE_NO = returnOperation.getOperationNumber();
                String hFRPFX_RETNLISTMAIN_PF_OBJ = returnOperation.getMainProcessFlow();
                String hFRPFX_RETNLISTMODULE_PF_OBJ = returnOperation.getModuleProcessFlow();

                strLotQtimeInfo.addAll(lotMethod.lotQtimeGetForRouteDR(objCommon,
                        tmpLotID,
                        hFRPFX_RETNLISTMAIN_PF_OBJ,
                        hFRPFX_RETNLISTMODULE_PF_OBJ,
                        hFRPFX_RETNLISTOPE_NO));

                minQTimeInfos.addAll(minQTimeMethod.getRestrictInProcessArea(tmpLotID, hFRPFX_RETNLISTMAIN_PF_OBJ));
            });

            tmpLotInBondingFlowInfo.setQtimeFlag(CimArrayUtils.isNotEmpty(strLotQtimeInfo));
            tmpLotInBondingFlowInfo.setMinQTimeFlag(CimArrayUtils.isNotEmpty(minQTimeInfos));

            //-------------------------------------
            // select Logical Recipe information
            //-------------------------------------
            ObjectIdentifier targetLogicalRecipeID;
            try {
                targetLogicalRecipeID = processMethod.processlogicalRecipeGetDR(objCommon, productID, targetOperationID);
            } catch (ServiceException se) {
                targetLogicalRecipeID = ObjectIdentifier.emptyIdentifier();
            }
            tmpLotInBondingFlowInfo.setTargetLogicalRecipeID(targetLogicalRecipeID);

            //-------------------------------------
            // Determine Top Lot or Base Lot
            //-------------------------------------
            try {
                List<Infos.BOMPartsInfo> bomPartsInfos = processMethod.processBOMPartsInfoGetDR(objCommon, productID, targetOperationID);
                if (CimArrayUtils.isEmpty(bomPartsInfos)) {
                    tmpLotInBondingFlowInfo.setBondingCategory(BizConstant.SP_LOT_BONDINGCATEGORY_TOP);
                } else if (bomPartsInfos.size() == 1){
                    tmpLotInBondingFlowInfo.setBondingCategory(BizConstant.SP_LOT_BONDINGCATEGORY_BASE);
                    tmpLotInBondingFlowInfo.setTopProductID(bomPartsInfos.get(0).getPartID());
                }
            } catch (ServiceException se) {
                Integer retCode = se.getCode();
                if (retCode != retCodeConfig.getBomNotDefined().getCode() &&
                        retCode != retCodeConfigEx.getPartsNotDefinedForProcess().getCode()) {
                    log.info("process_BOMPartsInfo_GetDR() != RC_OK : " + retCode);
                }
            }
        }
        return CimArrayUtils.isEmpty(tmpLotInBondingFlowInfoSeq) ? Collections.emptyList() : tmpLotInBondingFlowInfoSeq;
    }
}
