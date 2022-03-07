package com.fa.cim.method.impl;

import com.fa.cim.annotaion.OmMethod;
import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.utils.*;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.config.RetCodeConfigEx;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Inputs;
import com.fa.cim.dto.Outputs;
import com.fa.cim.dto.Params;
import com.fa.cim.entity.runtime.lot.CimLotDO;
import com.fa.cim.jpa.CimJpaRepository;
import com.fa.cim.method.*;
import com.fa.cim.newcore.bo.globalfunc.CimFrameWorkGlobals;
import com.fa.cim.newcore.bo.pd.CimProcessDefinition;
import com.fa.cim.newcore.bo.pd.CimProcessFlow;
import com.fa.cim.newcore.bo.pd.CimProcessOperationSpecification;
import com.fa.cim.newcore.bo.product.CimLot;
import com.fa.cim.newcore.bo.recipe.CimMachineRecipe;
import com.fa.cim.newcore.bo.restrict.RestrictionManager;
import com.fa.cim.newcore.dto.restriction.Constrain;
import com.fa.cim.newcore.exceptions.CoreFrameworkException;
import com.fa.cim.newcore.factory.BaseCoreFactory;
import com.fa.cim.newcore.standard.prcssdfn.ProcessDefinition;
import com.fa.cim.sorter.Info;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@OmMethod
@Slf4j
public class BondingLotMethod implements IBondingLotMethod {

    @Autowired
    private CimJpaRepository cimJpaRepository;

    private final ILotMethod lotMethod;

    private final IEquipmentMethod equipmentMethod;

    private final ILogicalRecipeMethod logicalRecipeMethod;

    private final IProcessMethod processMethod;

    private final BaseCoreFactory baseCoreFactory;

    private final RetCodeConfig retCodeConfig;

    private final RetCodeConfigEx retCodeConfigEx;

    private final CimFrameWorkGlobals cimFrameWorkGlobals;

    @Autowired
    private IConstraintMethod constraintMethod;
    @Autowired
    private RestrictionManager entityInhibitManager;

    @Autowired
    private ISorterNewMethod sorterNewMethod;

    @Autowired
    public BondingLotMethod(ILotMethod lotMethod,
                            IEquipmentMethod equipmentMethod,
                            ILogicalRecipeMethod logicalRecipeMethod,
                            IProcessMethod processMethod,
                            BaseCoreFactory baseCoreFactory,
                            RetCodeConfig retCodeConfig,
                            RetCodeConfigEx retCodeConfigEx,
                            CimFrameWorkGlobals cimFrameWorkGlobals) {
        this.lotMethod = lotMethod;
        this.equipmentMethod = equipmentMethod;
        this.logicalRecipeMethod = logicalRecipeMethod;
        this.processMethod = processMethod;
        this.baseCoreFactory = baseCoreFactory;
        this.retCodeConfig = retCodeConfig;
        this.retCodeConfigEx = retCodeConfigEx;
        this.cimFrameWorkGlobals = cimFrameWorkGlobals;
    }

    @Override
    public List<Infos.BondingLotAttributes> bondingLotListFillInTxPCQ039DR(Infos.ObjCommon objCommon,
                                                                           Params.BondingLotListInqInParams params) {
        ObjectIdentifier equipmentID = params.getTargetEquipmentID();
        String selectCriteria = params.getSelectCriteria();
        log.debug("Equipment ID: " + equipmentID);
        log.debug("Selection Critiria: " + selectCriteria);

        //---------------------------------------------
        //  Set input parameters into local variable
        //---------------------------------------------
        Outputs.EquipmentAvailableInfoGetDROut equipmentAvailableInfoGetDROut = null;
        Inputs.ObjLogicalRecipeMachineRecipeForSubLotTypeGetDRIn strLogicalRecipe_machineRecipeForSubLotTypeGetDRin =
                new Inputs.ObjLogicalRecipeMachineRecipeForSubLotTypeGetDRIn();
        String hFREQPMLTRCP_CAPA = "";
        if (CimStringUtils.isNotEmpty(ObjectIdentifier.fetchValue(equipmentID))) {
            //-------------------------------------
            // select Machine information
            //-------------------------------------
            String sql =    "SELECT OMEQP.MULTI_RECIPE_CAPABLE\n" +
                            "FROM   OMEQP\n" +
                            "WHERE  OMEQP.EQP_ID = ?1";
            Object[] queryOne = cimJpaRepository.queryOne(sql, ObjectIdentifier.fetchValue(equipmentID));
            hFREQPMLTRCP_CAPA = String.valueOf(queryOne[0]);

            equipmentAvailableInfoGetDROut = equipmentMethod.equipmentAvailableInfoGetDR(objCommon, equipmentID);
            strLogicalRecipe_machineRecipeForSubLotTypeGetDRin.setStrEqpChamberAvailableInfoSeq(equipmentAvailableInfoGetDROut.getStrEqpChamberAvailableInfoSeq());
            //------------------------------------------------------------------------------------------------
            // If select criteria is "SP_DP_SelectCriteria_CanBeProcessed",
            // then it means all Lots are NOT operable on this Equipment.
            // What'sNext Process is not necessary to continue any more.
            //------------------------------------------------------------------------------------------------
            if (CimStringUtils.equals(selectCriteria, BizConstant.SP_DP_SELECTCRITERIA_CANBEPROCESSED) &&
                    !equipmentAvailableInfoGetDROut.isAvailableFlagForEqp() &&
                    !equipmentAvailableInfoGetDROut.isConditionalAvailableFlagForEqp()) {
                log.debug("This Equipment is down. Reture");
                return Collections.emptyList();
            }
        }

        //---------------------------------------------
        // Get ModulePOS
        //---------------------------------------------
        CimProcessDefinition aMainPD = baseCoreFactory.getBO(CimProcessDefinition.class, params.getTargetRouteID());
        CimProcessFlow aMainPF = aMainPD.getActiveMainProcessFlow();
        Validations.check(aMainPF == null, retCodeConfig.getNotFoundPosForPd(), aMainPD.getIdentifier());

        AtomicReference<CimProcessFlow> outMainProcessFlow = new AtomicReference<>(),
                outModuleProcessFlow = new AtomicReference<>();
        CimProcessOperationSpecification aModulePOS = aMainPF.getProcessOperationSpecificationFor(params.getTargetOperationNumber(),
                outMainProcessFlow, outModuleProcessFlow);
        Validations.check(aModulePOS == null,
                retCodeConfig.getNotFoundRouteOpe(),
                ObjectIdentifier.fetchValue(params.getTargetRouteID()), params.getTargetOperationNumber());
        CimProcessFlow outMainPF = outMainProcessFlow.get();
        CimProcessFlow outModulePF = outModuleProcessFlow.get();
        Validations.check(outMainPF == null && aModulePOS == null && outModulePF == null,
                retCodeConfig.getNotFoundRouteOpe(),
                ObjectIdentifier.fetchValue(params.getTargetRouteID()), params.getTargetOperationNumber());
        List<ProcessDefinition> pds = aModulePOS.getProcessDefinitions();
        Validations.check(CimArrayUtils.isEmpty(pds), retCodeConfig.getNotFoundRouteOpe(),
                ObjectIdentifier.fetchValue(params.getTargetRouteID()), params.getTargetOperationNumber());
        ProcessDefinition pd = pds.get(0);
        ObjectIdentifier pdID = ObjectIdentifier.build(pd.getIdentifier(), pd.getPrimaryKey());
        List<Infos.BOMPartsInfo> bomPartsInfos = Collections.emptyList();
        try {
            bomPartsInfos = processMethod.processBOMPartsInfoGetDR(objCommon, params.getProductID(), pdID);
        } catch (ServiceException se) {
            Integer code = se.getCode();
            if (code != retCodeConfig.getBomNotDefined().getCode() &&
                    code != retCodeConfigEx.getPartsNotDefinedForProcess().getCode()){
                throw se;
            }
        }
        Validations.check(bomPartsInfos.size() != 1,
                retCodeConfigEx.getNotBaseProdForBonding(),
                ObjectIdentifier.fetchValue(params.getProductID()));
        Infos.BOMPartsInfo bomPartsInfo = bomPartsInfos.get(0);
        Boolean isBondingFlowSectionTargetOperation = aModulePOS.isBondingFlowSectionTargetOperation();
        String flowSectionName = aModulePOS.getFlowSectionName();
        String hFRLOTBOND_FLOW_NAME;
        List<CimLotDO> lotsData;
        List<Object[]> results;
        if (isBondingFlowSectionTargetOperation) {
            hFRLOTBOND_FLOW_NAME = flowSectionName;
            String sql =
                    "           SELECT   OMLOT.CJ_ID,\n" +
                    "                    OMLOT.PROPE_RKEY,\n" +
                    "                    OMLOT.LOT_ID,       OMLOT.ID,\n" +
                    "                    OMLOT.LOT_TYPE,\n" +
                    "                    OMLOT.SUB_LOT_TYPE,\n" +
                    "                    OMLOT.QTY,\n" +
                    "                    OMLOT.LOT_PRODUCTION_STATE,\n" +
                    "                    OMLOT.LOT_FINISHED_STATE,\n" +
                    "                    OMLOT.PP_FLAG\n" +
                    "            FROM    OMLOT\n" +
                    "            where   OMLOT.PROD_ID       = ?1\n" +
                    "            and     OMLOT.BONDING_SECTION    = ?2\n" +
                    "            and     OMLOT.LOT_STATE         = ?3\n" +
                    "            and     OMLOT.LOT_HOLD_STATE    = ?4\n" +
                    "            and     OMLOT.LOT_PROCESS_STATE = ?5\n" +
                    "            and     OMLOT.LOT_INV_STATE     = ?6";
            results = cimJpaRepository.query(sql,
                    ObjectIdentifier.fetchValue(bomPartsInfo.getPartID()),
                    hFRLOTBOND_FLOW_NAME,
                    BizConstant.CIMFW_LOT_STATE_ACTIVE,
                    BizConstant.CIMFW_LOT_HOLDSTATE_NOTONHOLD,
                    BizConstant.SP_LOT_PROCSTATE_WAITING,
                    BizConstant.SP_LOT_INVENTORYSTATE_ONFLOOR);
        } else {
            hFRLOTBOND_FLOW_NAME = "";
            String sql =
                    "           SELECT   OMLOT.CJ_ID,\n" +
                    "                    OMLOT.PROPE_RKEY,\n" +
                    "                    OMLOT.LOT_ID,       OMLOT.ID,\n" +
                    "                    OMLOT.LOT_TYPE,\n" +
                    "                    OMLOT.SUB_LOT_TYPE,\n" +
                    "                    OMLOT.QTY,\n" +
                    "                    OMLOT.LOT_PRODUCTION_STATE,\n" +
                    "                    OMLOT.LOT_FINISHED_STATE,\n" +
                    "                    OMLOT.PP_FLAG\n" +
                    "            FROM    OMLOT\n" +
                    "            inner   join OMLOT_EQP on OMLOT_EQP.REFKEY = OMLOT.ID\n" +
                    "            where   OMLOT.PROD_ID       = ?1\n" +
                    "            and     OMLOT.BONDING_SECTION  IS NULL\n" +
                    "            and     OMLOT.LOT_STATE         = ?2\n" +
                    "            and     OMLOT.LOT_HOLD_STATE    = ?3\n" +
                    "            and     OMLOT.LOT_PROCESS_STATE = ?4\n" +
                    "            and     OMLOT.LOT_INV_STATE     = ?5\n" +
                    "            and     OMLOT_EQP.EQP_ID        = ?6\n";
            results = cimJpaRepository.query(sql,
                    ObjectIdentifier.fetchValue(bomPartsInfo.getPartID()),
                    BizConstant.CIMFW_LOT_STATE_ACTIVE,
                    BizConstant.CIMFW_LOT_HOLDSTATE_NOTONHOLD,
                    BizConstant.SP_LOT_PROCSTATE_WAITING,
                    BizConstant.SP_LOT_INVENTORYSTATE_ONFLOOR,
                    ObjectIdentifier.fetchValue(equipmentID));
        }

        //------------------------------------------
        // set length of output lot info structure
        //------------------------------------------
        int nBondingLotLen = results.size();
        List<Infos.BondingLotAttributes> tmpBondingLotAttributesSeq = new ArrayList<>(nBondingLotLen);
        List<Boolean> isLotReservedSeq = new ArrayList<>(nBondingLotLen);
        List<Boolean> bLotHasAssignedMRecipeSeq = new ArrayList<>(nBondingLotLen);
        List<Boolean> FPCAppliedSeq = new ArrayList<>(nBondingLotLen);
        List<Infos.DefaultRecipeSetting> strDefaultRecipeSetSeq = new ArrayList<>(nBondingLotLen);

        for (Object[] result : results) {
            Infos.DefaultRecipeSetting strDefaultRecipeSet = new Infos.DefaultRecipeSetting();
            strDefaultRecipeSetSeq.add(strDefaultRecipeSet);
            String hFRLOTCTRLJOB_ID = (String) result[0];
            String hFRLOTPO_OBJ = (String) result[1];
            String hFRLOTLOT_ID = (String) result[2];
            String hFRLOTLOT_OBJ = (String) result[3];
            String hFRLOTLOT_TYPE = (String) result[4];
            String hFRLOTSUB_LOT_TYPE = (String) result[5];
            Number hFRLOTPROD_QTY = (Number) result[6];
            String hFRLOTLOT_PRODCTN_STATE = (String) result[7];
            String hFRLOTLOT_FINISHED_STATE = (String) result[8];
            Number hFRLOTPOST_PROCESS_FLAG = (Number) result[9];

            //--------------------------
            // Lot Finished Status Check
            //--------------------------
            if(CimStringUtils.equals(hFRLOTLOT_FINISHED_STATE, BizConstant.CIMFW_LOT_FINISHEDSTATE_EMPTIED)) {
                continue;
            }

            if (CimStringUtils.equals(selectCriteria, BizConstant.SP_DP_SELECTCRITERIA_CANBEPROCESSED)) {
                //-------------------------------------------------------------------------
                //  When Lot is in post process, it is not included in return structure
                //-------------------------------------------------------------------------
                if (CimBooleanUtils.isTrue(hFRLOTPOST_PROCESS_FLAG)) {
                    continue;
                }
                if (CimStringUtils.isNotEmpty(hFRLOTCTRLJOB_ID)) {
                    continue;
                }
                //------------------------------------------------------------------
                // Check whether Lot is operable on this Eqp with current status
                //------------------------------------------------------------------
                if (equipmentAvailableInfoGetDROut.isConditionalAvailableFlagForEqp()) {
                    if (!CimStringUtils.equalsIn(hFRLOTSUB_LOT_TYPE, equipmentAvailableInfoGetDROut.getAvailableSubLotTypesForEqp())) {
                        //------------------------------------------------------------------------------------------------
                        //  The Lot is not operable.
                        //  If select criteria is "SP_DP_SelectCriteria_CanBeProcessed",
                        //  then it means this lot don't have to be set in return structure. Continue to next Lot !!
                        //------------------------------------------------------------------------------------------------
                        continue;
                    }
                }
            }


            String bondingGroupId;
            try {
                bondingGroupId = lotMethod.lotBondingGroupIDGetDR(objCommon, ObjectIdentifier.build(hFRLOTLOT_ID, hFRLOTLOT_OBJ));
            } catch (ServiceException | CoreFrameworkException e) {
                bondingGroupId = "";
            }
            if (CimStringUtils.equals(selectCriteria, BizConstant.SP_DP_SELECTCRITERIA_CANBEPROCESSED)) {
                //----------------------------------------------------------------------------------
                //  When Lot is belongs to a Bonding Group, it is not included in return structure
                //-----------------------------------------------------------------------------------
                if (CimStringUtils.isNotEmpty(bondingGroupId)) {
                    continue;
                }
            }
            String frcastSql =
                "SELECT  OMCARRIER.CARRIER_ID,     OMCARRIER.ID,\n" +
                "                    OMCARRIER.MULTI_LOT_TYPE,\n" +
                "                    OMCARRIER.CJ_ID,\n" +
                "                    OMCARRIER.PP_FLAG\n" +
                "            FROM    OMCARRIER,\n" +
                "                    OMCARRIER_LOT\n" +
                "            WHERE   OMCARRIER_LOT.LOT_ID     = ?1 AND\n" +
                "                    OMCARRIER.ID = OMCARRIER_LOT.REFKEY";
            Object[] frcastSqlResult = cimJpaRepository.queryOne(frcastSql, hFRLOTLOT_ID);
            String hFRCASTCAST_ID = null;
            String hFRCASTCAST_OBJ = null;
            String hFRCASTMULTI_LOT_TYPE = null;
            String hFRCASTCTRLJOB_ID = null;
            String hFRCASTPOST_PROCESS_FLAG = null;
            if (!CimObjectUtils.isEmpty(frcastSqlResult)){
                hFRCASTCAST_ID = String.valueOf(frcastSqlResult[0]);
                hFRCASTCAST_OBJ = String.valueOf(frcastSqlResult[1]);
                hFRCASTMULTI_LOT_TYPE = String.valueOf(frcastSqlResult[2]);
                hFRCASTCTRLJOB_ID = String.valueOf(frcastSqlResult[3]);
                hFRCASTPOST_PROCESS_FLAG = String.valueOf(frcastSqlResult[4]);
            }


            //---------------------------------------------------------------------------------------------
            //  When Cassette related to Lot is in post process, Lot is not included in return structure
            //---------------------------------------------------------------------------------------------
            if (CimStringUtils.equals(selectCriteria, BizConstant.SP_DP_SELECTCRITERIA_CANBEPROCESSED)) {
                if (CimBooleanUtils.getBoolean(hFRCASTPOST_PROCESS_FLAG)) {
                    continue;
                }
                if (CimStringUtils.isNotEmpty(hFRCASTCTRLJOB_ID)) {
                    continue;
                }
                if (CimStringUtils.equals(hFRCASTMULTI_LOT_TYPE, BizConstant.SP_CAS_MULTILOTTYPE_MULTILOTMULTIRECIPE)) {
                    if (CimStringUtils.equals(hFREQPMLTRCP_CAPA, BizConstant.SP_EQP_MULTIRECIPECAPABILITY_BATCH)) {
                        continue;
                    }
                }
            }
            boolean isLotReserved = CimStringUtils.isNotEmpty(hFRLOTCTRLJOB_ID);
            isLotReservedSeq.add(isLotReserved);
            Infos.BondingLotAttributes tmpBondingLotAttributes = new Infos.BondingLotAttributes();
            tmpBondingLotAttributesSeq.add(tmpBondingLotAttributes);
            tmpBondingLotAttributes.setLotID(ObjectIdentifier.build(hFRLOTLOT_ID, hFRLOTLOT_OBJ));
            tmpBondingLotAttributes.setLotType(hFRLOTLOT_TYPE);
            tmpBondingLotAttributes.setSubLotType(hFRLOTSUB_LOT_TYPE);
            tmpBondingLotAttributes.setProductID(bomPartsInfo.getPartID());
            tmpBondingLotAttributes.setTotalWaferCount(CimNumberUtils.intValue(hFRLOTPROD_QTY));
            tmpBondingLotAttributes.setInPostProcessOfLot(CimBooleanUtils.isTrue(hFRLOTPOST_PROCESS_FLAG));
            tmpBondingLotAttributes.setBondingCategory(BizConstant.SP_LOT_BONDINGCATEGORY_TOP);
            tmpBondingLotAttributes.setTopProductID(ObjectIdentifier.emptyIdentifier());
            tmpBondingLotAttributes.setBondingFlowSectionName(hFRLOTBOND_FLOW_NAME);
            tmpBondingLotAttributes.setBondingGroupID(bondingGroupId);
            tmpBondingLotAttributes.setCarrierID(ObjectIdentifier.build(hFRCASTCAST_ID, hFRCASTCAST_OBJ));
            tmpBondingLotAttributes.setInPostProcessOfCassette(CimBooleanUtils.getBoolean(hFRCASTPOST_PROCESS_FLAG));
            CimLot lot = baseCoreFactory.getBO(CimLot.class, ObjectIdentifier.build(hFRLOTLOT_ID, hFRLOTLOT_OBJ));
            tmpBondingLotAttributes.setLotStatus(lot.getState());
            List<Infos.LotStatusList> strLotStatusListListSeq = new ArrayList<>(6);
            tmpBondingLotAttributes.setStrLotStatusListListSeq(strLotStatusListListSeq);
            strLotStatusListListSeq.add(new Infos.LotStatusList(BizConstant.SP_LOTSTATECAT_STATE, BizConstant.CIMFW_LOT_STATE_ACTIVE));
            strLotStatusListListSeq.add(new Infos.LotStatusList(BizConstant.SP_LOTSTATECAT_PRODUCTIONSTATE, hFRLOTLOT_PRODCTN_STATE));
            strLotStatusListListSeq.add(new Infos.LotStatusList(BizConstant.SP_LOTSTATECAT_HOLDSTATE, BizConstant.CIMFW_LOT_HOLDSTATE_NOTONHOLD));
            strLotStatusListListSeq.add(new Infos.LotStatusList(BizConstant.SP_LOTSTATECAT_FINISHEDSTATE, hFRLOTLOT_FINISHED_STATE));
            strLotStatusListListSeq.add(new Infos.LotStatusList(BizConstant.SP_LOTSTATECAT_PROCSTATE, BizConstant.SP_LOT_PROCSTATE_WAITING));
            strLotStatusListListSeq.add(new Infos.LotStatusList(BizConstant.SP_LOTSTATECAT_INVENTORYSTATE, BizConstant.SP_LOT_INVENTORYSTATE_ONFLOOR));
            if (CimStringUtils.equals(selectCriteria, BizConstant.SP_DP_SELECTCRITERIA_CANBEPROCESSED)) {
                com.fa.cim.sorter.Params.ObjSorterJobListGetDRIn objSorterJobListGetDRIn = new com.fa.cim.sorter.Params.ObjSorterJobListGetDRIn();
                objSorterJobListGetDRIn.setCarrierID(tmpBondingLotAttributes.getCarrierID());
                objSorterJobListGetDRIn.setLotID(tmpBondingLotAttributes.getLotID());
                List<Info.SortJobListAttributes> sortJobListAttributes = sorterNewMethod.sorterJobListGetDR(objCommon, objSorterJobListGetDRIn);
                if (CimArrayUtils.isNotEmpty(sortJobListAttributes)) {
                    continue;
                }
            }

            String posSql =
                    "SELECT  OMPROPE.ROUTE_NO,\n" +
                    "        OMPROPE.ROUTE_PRF_RKEY,\n" +
                    "        OMPROPE.MROUTE_PRF_RKEY,\n" +
                    "        OMPROPE.ROUTE_ID,       OMPROPE.ROUTE_RKEY,\n" +
                    "        OMPROPE.MAIN_PROCESS_ID,         OMPROPE.MAIN_PROCESS_RKEY,\n" +
                    "        OMPROPE.OPE_NO,\n" +
                    "        OMPROPE.STEP_ID,             OMPROPE.STEP_RKEY,\n" +
                    "        OMPROPE.ALLOC_LRCP_ID,  OMPROPE.ALLOC_LRCP_RKEY,\n" +
                    "        OMPROPE.ALLOC_MRCP_ID,    OMPROPE.ALLOC_MRCP_RKEY\n" +
                    "FROM    OMPROPE\n" +
                    "WHERE   OMPROPE.ID = ?1";
            Object[] objects = cimJpaRepository.queryOne(posSql, hFRLOTPO_OBJ);
            String hFRPOMODULE_NO = String.valueOf(objects[0]);
            String hFRPOMODULE_PF_OBJ = String.valueOf(objects[1]);
            String hFRPOMAIN_PF_OBJ = String.valueOf(objects[2]);
            String hFRPOMODULEPD_ID = String.valueOf(objects[3]);
            String hFRPOMODULEPD_OBJ = String.valueOf(objects[4]);
            String hFRPOMAINPD_ID = String.valueOf(objects[5]);
            String hFRPOMAINPD_OBJ = String.valueOf(objects[6]);
            String hFRPOOPE_NO = String.valueOf(objects[7]);
            String hFRPOPD_ID = String.valueOf(objects[8]);
            String hFRPOPD_OBJ = String.valueOf(objects[9]);
            String hFRPOASGN_LCRECIPE_ID = String.valueOf(objects[10]);
            String hFRPOASGN_LCRECIPE_OBJ = String.valueOf(objects[11]);
            String hFRPOASGN_RECIPE_ID = String.valueOf(objects[12]);
            String hFRPOASGN_RECIPE_OBJ = String.valueOf(objects[13]);
            tmpBondingLotAttributes.setTargetRouteID(ObjectIdentifier.build(hFRPOMAINPD_ID, hFRPOMAINPD_OBJ));
            boolean bLotHasAssignedMRecipe;
            if (!isBondingFlowSectionTargetOperation) {
                tmpBondingLotAttributes.setTargetOpeNo(hFRPOOPE_NO);
                tmpBondingLotAttributes.setTargetOpeID(ObjectIdentifier.build(hFRPOPD_ID, hFRPOPD_OBJ));
                bLotHasAssignedMRecipe = CimStringUtils.isNotEmpty(hFRPOASGN_RECIPE_ID);
            } else {
                if (CimStringUtils.isNotEmpty(hFRPOMODULE_PF_OBJ)) {
                    String pfSql =
                            "select  POS.OPE_NO,\n" +
                            "        POS.STEP_ID, POS.STEP_RKEY\n" +
                            "from    OMPRSS POS\n" +
                            "inner   join OMPRF_PRSSSEQ POSLIST on POSLIST.PRSS_RKEY = POS.ID\n" +
                            "inner   join OMPRF         PF      on PF.ID = POSLIST.REFKEY\n" +
                            "where   PF.ID = ?1\n" +
                            "and     POS.SECTION_CONTROL = ?2\n" +
                            "and     POS.SECTION_NAME     = ?3\n" +
                            "and     POS.SECTION_END_FLAG = 1";
                    Object[] pfSqlResult = cimJpaRepository.queryOne(pfSql,
                            hFRPOMODULE_PF_OBJ,
                            BizConstant.SP_FLOWSECTIONCONTROLCATEGORY_BONDINGFLOW,
                            hFRLOTBOND_FLOW_NAME);
                    String hFRPOSOPE_NO = null;
                    String hFRPOSPD_ID = null;
                    String hFRPOSPD_OBJ = null;
                    if (!CimObjectUtils.isEmpty(pfSqlResult)) {
                        hFRPOSOPE_NO = String.valueOf(pfSqlResult[0]);
                        hFRPOSPD_ID = String.valueOf(pfSqlResult[1]);
                        hFRPOSPD_OBJ = String.valueOf(pfSqlResult[2]);
                    }
                    tmpBondingLotAttributes.setTargetOpeNo(convertModuleOpeNotoOpeNo(hFRPOMODULE_NO, hFRPOSOPE_NO));
                    tmpBondingLotAttributes.setTargetOpeID(ObjectIdentifier.build(hFRPOSPD_ID, hFRPOSPD_OBJ));
                } else {
                    tmpBondingLotAttributes.setTargetOpeID(ObjectIdentifier.emptyIdentifier());
                    tmpBondingLotAttributes.setTargetOpeNo("");
                }
                bLotHasAssignedMRecipe = false;
            }
            bLotHasAssignedMRecipeSeq.add(bLotHasAssignedMRecipe);
            String hFRPF_PDLISTSTAGE_ID = "";
            String hFRPF_PDLISTSTAGE_OBJ = "";
            if (CimStringUtils.isNotEmpty(hFRPOMODULE_PF_OBJ)) {
                String pdSql =
                        "SELECT  OMPRF_ROUTESEQ.STAGE_ID,  OMPRF_ROUTESEQ.STAGE_RKEY\n" +
                        "FROM    OMPRF_ROUTESEQ\n" +
                        "WHERE   OMPRF_ROUTESEQ.REFKEY = ?1\n" +
                        "AND     OMPRF_ROUTESEQ.LINK_KEY = ?2";
                Object[] pdSqlResult = cimJpaRepository.queryOne(pdSql, hFRPOMODULE_PF_OBJ, hFRPOMODULE_NO);
                if (!CimObjectUtils.isEmpty(pdSqlResult)){
                    hFRPF_PDLISTSTAGE_ID = String.valueOf(pdSqlResult[0]);
                    hFRPF_PDLISTSTAGE_OBJ = String.valueOf(pdSqlResult[1]);
                }
            }

            if(!isBondingFlowSectionTargetOperation && isLotReserved) {
                tmpBondingLotAttributes.setTargetLogicalRecipeID(ObjectIdentifier.build(hFRPOASGN_LCRECIPE_ID, hFRPOASGN_LCRECIPE_OBJ));
            } else {
                tmpBondingLotAttributes.getTargetRouteID();
                ObjectIdentifier logicalID = processMethod.processlogicalRecipeGetDR(objCommon,
                        tmpBondingLotAttributes.getProductID(), tmpBondingLotAttributes.getTargetOpeID());
                tmpBondingLotAttributes.setTargetLogicalRecipeID(logicalID);
            }
            ObjectIdentifier targetEquipmentID = params.getTargetEquipmentID();
            if (!CimStringUtils.isEmpty(ObjectIdentifier.fetchValue(targetEquipmentID))) {
                Outputs.ObjLotEffectiveFPCInfoForOperationGetOut objLotEffectiveFPCInfoForOperationGetOut = new Outputs.ObjLotEffectiveFPCInfoForOperationGetOut();
                try {
                    objLotEffectiveFPCInfoForOperationGetOut = lotMethod.lotEffectiveFPCInfoForOperationGet(objCommon,
                            BizConstant.SP_FPC_EXCHANGETYPE_BONDINGGROUP,
                            targetEquipmentID,
                            tmpBondingLotAttributes.getLotID(),
                            tmpBondingLotAttributes.getTargetRouteID(),
                            tmpBondingLotAttributes.getTargetOpeNo());
                }catch (ServiceException se) {
                    objLotEffectiveFPCInfoForOperationGetOut.setEquipmentActionRequired(false);
                    objLotEffectiveFPCInfoForOperationGetOut.setMachineRecipeActionRequired(false);
                }
                boolean isEqpRequired = objLotEffectiveFPCInfoForOperationGetOut.isEquipmentActionRequired();
                boolean FPCApplied = isEqpRequired ||
                        objLotEffectiveFPCInfoForOperationGetOut.isMachineRecipeActionRequired();
                FPCAppliedSeq.add(FPCApplied);
                if (isBondingFlowSectionTargetOperation && !isEqpRequired) {
                    Infos.FPCDispatchEqpInfo fpcDispatchEqpInfo = new Infos.FPCDispatchEqpInfo();
                    try {
                        fpcDispatchEqpInfo = lotMethod.lotFPCdispatchEqpInfoGet(objCommon,
                                tmpBondingLotAttributes.getLotID(),
                                tmpBondingLotAttributes.getTargetRouteID(),
                                tmpBondingLotAttributes.getTargetOpeNo());
                    } catch (ServiceException se) {
                        fpcDispatchEqpInfo.setRestrictEqpFlag(false);
                        fpcDispatchEqpInfo.setDispatchEqpIDs(Collections.emptyList());
                    }
                    List<ObjectIdentifier> dispatchEqpIDs = fpcDispatchEqpInfo.getDispatchEqpIDs();
                    boolean eqpFound = false;
                    for (ObjectIdentifier dispatchEqpID : dispatchEqpIDs) {
                        if (CimStringUtils.equals(ObjectIdentifier.fetchValue(dispatchEqpID), ObjectIdentifier.fetchValue(targetEquipmentID))) {
                            eqpFound = true;
                            break;
                        }
                    }
                    if (!eqpFound) {
                        if (fpcDispatchEqpInfo.getRestrictEqpFlag()) {
                            continue;
                        }
                        List<ObjectIdentifier> dispatchEquipmentIDs;
                        try {
                            dispatchEquipmentIDs = processMethod.processDispatchEquipmentsGetDR(objCommon,
                                    tmpBondingLotAttributes.getProductID(), tmpBondingLotAttributes.getTargetOpeID());
                        } catch (ServiceException se) {
                            continue;
                        }
                        eqpFound = false;
                        for (ObjectIdentifier dispatchEquipmentID : dispatchEquipmentIDs) {
                            if (CimStringUtils.equals(ObjectIdentifier.fetchValue(dispatchEquipmentID), ObjectIdentifier.fetchValue(targetEquipmentID))) {
                                eqpFound = true;
                                break;
                            }
                        }
                        if (!eqpFound) {
                            continue;
                        }
                    }

                }
                boolean nInfoCollected = false;
                for (int i = 0; i < tmpBondingLotAttributesSeq.size(); i++) {
                    Infos.BondingLotAttributes bondingLotAttributes = tmpBondingLotAttributesSeq.get(i);
                    if (CimStringUtils.equals(ObjectIdentifier.fetchValue(bondingLotAttributes.getTargetLogicalRecipeID()),
                            ObjectIdentifier.fetchValue(tmpBondingLotAttributes.getTargetLogicalRecipeID())) &&
                            !FPCAppliedSeq.get(i) && !FPCApplied) {
                        if (equipmentAvailableInfoGetDROut.isConditionalAvailableFlagForChamber() &&
                                !CimStringUtils.equals(bondingLotAttributes.getSubLotType(), tmpBondingLotAttributes.getSubLotType())) {
                            continue;
                        }
                        if (isLotReserved && bLotHasAssignedMRecipe) {
                            strDefaultRecipeSet.setChamberSeq(strDefaultRecipeSetSeq.get(i).getChamberSeq());
                            tmpBondingLotAttributes.setTargetMachineRecipeID(ObjectIdentifier.build(hFRPOASGN_RECIPE_ID, hFRPOASGN_RECIPE_OBJ));
                            nInfoCollected = true;
                            break;
                        } else if (!isLotReservedSeq.get(i) && !bLotHasAssignedMRecipeSeq.get(i)) {
                            strDefaultRecipeSet.setChamberSeq(strDefaultRecipeSetSeq.get(i).getChamberSeq());
                            tmpBondingLotAttributes.setTargetMachineRecipeID(tmpBondingLotAttributesSeq.get(i).getTargetMachineRecipeID());
                            nInfoCollected = true;
                            break;
                        }
                    }
                }

                if (!nInfoCollected) {
                    List<Infos.Chamber> chamberSeq = new ArrayList<>();
                    strDefaultRecipeSet.setChamberSeq(chamberSeq);
                    if (objLotEffectiveFPCInfoForOperationGetOut.isMachineRecipeActionRequired()) {
                        String hFRMRCPRECIPE_ID = ObjectIdentifier.fetchValue(objLotEffectiveFPCInfoForOperationGetOut.getStrFPCInfo().getMachineRecipeID());
                        String hFRMRCPRECIPE_OBJ = ObjectIdentifier.fetchReferenceKey(objLotEffectiveFPCInfoForOperationGetOut.getStrFPCInfo().getMachineRecipeID());
                        if (CimStringUtils.isNotEmpty(hFRMRCPRECIPE_ID)) {
                            String versionID = cimFrameWorkGlobals.extractVersionFromID(hFRMRCPRECIPE_ID);
                            if (CimStringUtils.equals(versionID, BizConstant.SP_ACTIVE_VERSION)) {
                                CimMachineRecipe cimMachineRecipe = baseCoreFactory.getBO(CimMachineRecipe.class, hFRMRCPRECIPE_OBJ);
                                CimMachineRecipe activeObject = cimMachineRecipe.getActiveObject();
                                hFRMRCPRECIPE_ID = activeObject.getIdentifier();
                                hFRMRCPRECIPE_OBJ = activeObject.getPrimaryKey();
                            }
                        }
                        if (CimStringUtils.isNotEmpty(hFRMRCPRECIPE_ID)) {
                            tmpBondingLotAttributes.setTargetMachineRecipeID(isLotReserved ?
                                    ObjectIdentifier.build(hFRPOASGN_RECIPE_ID, hFRPOASGN_RECIPE_OBJ) :
                                    ObjectIdentifier.build(hFRMRCPRECIPE_ID, hFRMRCPRECIPE_OBJ));
                        } else {
                            tmpBondingLotAttributes.setTargetMachineRecipeID(isLotReserved ?
                                    ObjectIdentifier.build(hFRPOASGN_RECIPE_ID, hFRPOASGN_RECIPE_OBJ) :
                                    ObjectIdentifier.build("*", ""));
                        }
                    } else {
                        strLogicalRecipe_machineRecipeForSubLotTypeGetDRin.setLogicalRecipeID(tmpBondingLotAttributes.getTargetLogicalRecipeID());
                        strLogicalRecipe_machineRecipeForSubLotTypeGetDRin.setSubLotType(tmpBondingLotAttributes.getSubLotType());
                        strLogicalRecipe_machineRecipeForSubLotTypeGetDRin.setProductID(tmpBondingLotAttributes.getProductID());
                        strLogicalRecipe_machineRecipeForSubLotTypeGetDRin.setLotID(tmpBondingLotAttributes.getLotID());
                        try {
                            Outputs.ObjLogicalRecipeMachineRecipeForSubLotTypeGetDROut objLogicalRecipeMachineRecipeForSubLotTypeGetDROut =
                                    logicalRecipeMethod.logicalRecipeMachineRecipeForSubLotTypeGetDR(objCommon, strLogicalRecipe_machineRecipeForSubLotTypeGetDRin);
                            tmpBondingLotAttributes.setTargetMachineRecipeID(objLogicalRecipeMachineRecipeForSubLotTypeGetDROut.getMachineRecipeID());
                            strDefaultRecipeSet.setChamberSeq(objLogicalRecipeMachineRecipeForSubLotTypeGetDROut.getChamberSeq());
                        } catch (ServiceException se) {
                            tmpBondingLotAttributes.setTargetMachineRecipeID(ObjectIdentifier.emptyIdentifier());
                            strDefaultRecipeSet.setChamberSeq(Collections.emptyList());
                        }

                    }
                    if (CimStringUtils.equals(selectCriteria, BizConstant.SP_DP_SELECTCRITERIA_CANBEPROCESSED)) {
                        String targetMachineRecipeId = ObjectIdentifier.fetchValue(tmpBondingLotAttributes.getTargetMachineRecipeID());
                        if (CimStringUtils.isEmpty(targetMachineRecipeId) || CimStringUtils.equals(targetMachineRecipeId, "*")) {
                            continue;
                        }
                    }
                }
            }

            if (CimStringUtils.equals(selectCriteria, BizConstant.SP_DP_SELECTCRITERIA_CANBEPROCESSED)) {
                boolean bEntityInhibitInfoCollected = false;
                for (Infos.BondingLotAttributes bondingLotAttributes : tmpBondingLotAttributesSeq) {
                    if (CimStringUtils.equals(ObjectIdentifier.fetchValue(bondingLotAttributes.getTargetRouteID()), ObjectIdentifier.fetchValue(tmpBondingLotAttributes.getTargetRouteID())) &&
                        CimStringUtils.equals(ObjectIdentifier.fetchValue(bondingLotAttributes.getProductID()), ObjectIdentifier.fetchValue(tmpBondingLotAttributes.getProductID())) &&
                        CimStringUtils.equals(bondingLotAttributes.getTargetOpeNo(), tmpBondingLotAttributes.getTargetOpeNo()) &&
                            CimStringUtils.equals(ObjectIdentifier.fetchValue(bondingLotAttributes.getTargetOpeID()), ObjectIdentifier.fetchValue(tmpBondingLotAttributes.getTargetOpeID()))
                    ){
                        bEntityInhibitInfoCollected = true;
                        break;
                    }
                }
                if (!bEntityInhibitInfoCollected) {
                    List<Constrain.EntityIdentifier> entities = new ArrayList<>();
                    ObjectIdentifier productID = tmpBondingLotAttributes.getProductID();
                    if (CimStringUtils.isNotEmpty(ObjectIdentifier.fetchValue(productID))) {
                        entities.add(newEntityIdentifier(BizConstant.SP_INHIBITCLASSID_PRODUCT, productID, ""));
                    }

                    ObjectIdentifier targetRouteID = tmpBondingLotAttributes.getTargetRouteID();
                    if (CimStringUtils.isNotEmpty(ObjectIdentifier.fetchValue(productID))) {
                        String targetOpeNo = tmpBondingLotAttributes.getTargetOpeNo();
                        entities.add(newEntityIdentifier(BizConstant.SP_INHIBITCLASSID_ROUTE, targetRouteID,
                                CimStringUtils.isNotEmpty(targetOpeNo) ? targetOpeNo : ""));
                    }

                    if (CimStringUtils.isNotEmpty(hFRPOMODULEPD_ID)) {
                        entities.add(newEntityIdentifier(BizConstant.SP_INHIBITCLASSID_MODULEPD,
                                ObjectIdentifier.build(hFRPOMODULEPD_ID, hFRPOMODULEPD_OBJ), ""));
                    }

                    List<Infos.Chamber> chamberSeq = strDefaultRecipeSet.getChamberSeq();
                    chamberSeq.forEach(chamber -> {
                        ObjectIdentifier chamberID = chamber.getChamberID();
                        if (CimStringUtils.isNotEmpty(ObjectIdentifier.fetchValue(chamberID))) {
                            entities.add(newEntityIdentifier(BizConstant.SP_INHIBITCLASSID_CHAMBER,
                                    equipmentID, ObjectIdentifier.fetchValue(chamberID)));
                        }
                    });

                    if (CimStringUtils.isNotEmpty(hFRPF_PDLISTSTAGE_ID)) {
                        entities.add(newEntityIdentifier(BizConstant.SP_INHIBITCLASSID_STAGE,
                                ObjectIdentifier.build(hFRPF_PDLISTSTAGE_ID, hFRPF_PDLISTSTAGE_OBJ), ""));
                    }

                    List<String> sublottypes = new ArrayList<>(1);
                    sublottypes.add(tmpBondingLotAttributes.getSubLotType());
                    List<Constrain.EntityInhibitRecord> entityInhibitRecords = entityInhibitManager.allEntityInhibitRecordsForLotEntities(entities, sublottypes);

                    List<Infos.EntityInhibitInfo>  entityInhibitInfoSeq = new ArrayList<>(entities.size());
                    if (CimArrayUtils.isNotEmpty(entityInhibitRecords)) {
                        entityInhibitRecords.forEach(record -> {
                            Infos.EntityInhibitInfo entityInhibitInfo = new Infos.EntityInhibitInfo();
                            entityInhibitInfo.setEntityInhibitID(new ObjectIdentifier(record.getId(), record.getReferenceKey()));
                            Infos.EntityInhibitAttributes entityInhibitAttributes = new Infos.EntityInhibitAttributes();
                            entityInhibitAttributes.setEntities(record.getEntities().stream().map(x-> new Infos.EntityIdentifier(x.getClassName(), new ObjectIdentifier(x.getObjectId()), x.getAttrib())).collect(Collectors.toList()));
                            entityInhibitAttributes.setClaimedTimeStamp(record.getChangedTimeStamp().toString());
                            entityInhibitAttributes.setEndTimeStamp(record.getEndTimeStamp().toString());
                            entityInhibitAttributes.setOwnerID(record.getOwner());
                            entityInhibitAttributes.setStartTimeStamp(record.getStartTimeStamp().toString());
                            entityInhibitAttributes.setSubLotTypes(record.getSubLotTypes());
                            entityInhibitAttributes.setMemo(record.getClaimMemo());
                            entityInhibitAttributes.setReasonCode(ObjectIdentifier.fetchValue(record.getReasonCode()));
                            entityInhibitInfo.setEntityInhibitAttributes(entityInhibitAttributes);
                            entityInhibitInfoSeq.add(entityInhibitInfo);
                        });
                        Inputs.ObjEntityInhibiteffectiveForLotGetDRIn objEntityInhibiteffectiveForLotGetDRIn = new Inputs.ObjEntityInhibiteffectiveForLotGetDRIn();
                        objEntityInhibiteffectiveForLotGetDRIn.setStrEntityInhibitInfos(entityInhibitInfoSeq);
                        objEntityInhibiteffectiveForLotGetDRIn.setLotID(tmpBondingLotAttributes.getLotID());
                        List<Infos.EntityInhibitInfo> entityInhibitInfos = constraintMethod
                                .constraintEffectiveForLotGetDR(objCommon, objEntityInhibiteffectiveForLotGetDRIn.getStrEntityInhibitInfos(), objEntityInhibiteffectiveForLotGetDRIn.getLotID());
                        if (CimArrayUtils.isNotEmpty(entityInhibitInfos)) {
                            continue;
                        }
                    }
                }
            }
            log.debug("check entityInhibit record end\"");
        }

        return tmpBondingLotAttributesSeq;
    }

    private String convertModuleOpeNotoOpeNo (String moduleNo, String moduleOpeNo) {
        return moduleNo + "." + moduleOpeNo;
    }

    private Constrain.EntityIdentifier newEntityIdentifier (String className, ObjectIdentifier objectID, String attrId) {
        Constrain.EntityIdentifier entityIdentifier = new Constrain.EntityIdentifier();
        entityIdentifier.setClassName(className);
        entityIdentifier.setObjectId(ObjectIdentifier.fetchValue(objectID));
        entityIdentifier.setAttrib(attrId);
        return entityIdentifier;
    }
}
