package com.fa.cim.method.impl.equipment;

import com.fa.cim.annotaion.OmMethod;
import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.constant.CIMStateConst;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.utils.*;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.config.RetCodeConfigEx;
import com.fa.cim.crcp.ChamberLevelRecipeWhatNextParam;
import com.fa.cim.dto.*;
import com.fa.cim.entity.runtime.cassette.CimCassetteDO;
import com.fa.cim.entity.runtime.code.CimCodeDO;
import com.fa.cim.entity.runtime.eqp.CimEquipmentDO;
import com.fa.cim.entity.runtime.logicalrecipe.CimLogicalRecipeDO;
import com.fa.cim.entity.runtime.logicalrecipe.CimLogicalRecipeDSetDO;
import com.fa.cim.entity.runtime.mrecipe.CimMachineRecipeDO;
import com.fa.cim.entity.runtime.pfx.CimProcessFlowContextReturnDO;
import com.fa.cim.entity.runtime.po.CimProcessOperationDO;
import com.fa.cim.entity.runtime.pos.CimProcessOperationSpecificationDO;
import com.fa.cim.entity.runtime.processdefinition.CimProcessDefinitionDO;
import com.fa.cim.entity.runtime.processflow.CimPFDefinitionListDO;
import com.fa.cim.entity.runtime.processflow.CimPFPosListDO;
import com.fa.cim.entity.runtime.processflow.CimProcessFlowDO;
import com.fa.cim.entity.runtime.productgroup.CimProductGroupDO;
import com.fa.cim.entity.runtime.productspec.CimProductSpecificationDO;
import com.fa.cim.entity.runtime.reticleset.*;
import com.fa.cim.jpa.CimJpaRepository;
import com.fa.cim.method.*;
import com.fa.cim.method.impl.SorterNewMethod;
import com.fa.cim.newcore.bo.dispatch.CimDispatcher;
import com.fa.cim.newcore.bo.globalfunc.CimFrameWorkGlobals;
import com.fa.cim.newcore.bo.machine.CimMachine;
import com.fa.cim.newcore.bo.pd.CimProcessOperation;
import com.fa.cim.newcore.bo.pd.CimProcessOperationSpecification;
import com.fa.cim.newcore.bo.person.CimPerson;
import com.fa.cim.newcore.bo.product.CimControlJob;
import com.fa.cim.newcore.bo.product.CimLot;
import com.fa.cim.newcore.bo.product.CimLotComment;
import com.fa.cim.newcore.bo.restrict.RestrictionManager;
import com.fa.cim.newcore.dto.dispatch.DispatcherDTO;
import com.fa.cim.newcore.dto.machine.MachineDTO;
import com.fa.cim.newcore.dto.pd.ProcessDTO;
import com.fa.cim.newcore.dto.restriction.Constrain;
import com.fa.cim.newcore.factory.BaseCoreFactory;
import com.fa.cim.newcore.impl.bo.env.StandardProperties;
import com.fa.cim.sorter.Info;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * <p>EquipmentWhatNextMethod .
 * change history:
 * date                      defect#             person            comments
 * -------------------------------------------------------------------------------------------------------------------
 * 2020/12/23 13:34         ********              ZQI             create file.
 *
 * @author ZQI
 * @date 2020/12/23 13:34
 * @copyright 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 * @since JDK1.8
 */
@Slf4j
@OmMethod
public class EquipmentWhatNextMethod {

    @Autowired
    private RetCodeConfigEx retCodeConfigEx;

    @Autowired
    private IReticleMethod reticleMethod;

    @Autowired
    private SorterNewMethod sorterMethod;

    @Autowired
    private IBondingGroupMethod bondingGroupMethod;

    @Autowired
    private IDurableMethod durableMethod;

    @Autowired
    private CimJpaRepository cimJpaRepository;

    @Autowired
    private ILotMethod lotMethod;

    @Autowired
    private IAutoDispatchControlMethod autoDispatchControlMethod;

    @Autowired
    private IProcessMethod processMethod;

    @Autowired
    private ICassetteMethod cassetteMethod;

    @Autowired
    private RestrictionManager entityInhibitManager;

    @Autowired
    private BaseCoreFactory baseCoreFactory;

    @Autowired
    private CimFrameWorkGlobals cimFrameWorkGlobals;

    @Autowired
    private RetCodeConfig retCodeConfig;

    @Autowired
    private IConstraintMethod constraintMethod;

    @Autowired
    private IEquipmentMethod equipmentMethod;

    @Autowired
    private IMinQTimeMethod minQTimeMethod;

    @Autowired
    private IChamberLevelRecipeMethod chamberLevelRecipeMethod;

    @Data
    static class FilterValueForWhatNextOut {
        private CimLot lot;
        private CimCassetteDO cassetteDO;
    }

    /**
     * Check the Lot state and select criteria.
     *
     * @param lot            aLot
     * @param selectCriteria select criteria
     * @return return true if the lot meets the filter condition.
     * @author ZQI
     * @date 2021/1/22 11:13
     */
    private boolean whatNextLotStateCheck(CimLot lot, String selectCriteria) {
        //--------------------------
        // Lot Finished Status Check
        //--------------------------
        if (CIMStateConst.CIM_LOT_FINISHED_STATE_EMPTIED.equals(lot.getLotFinishedState())) {
            log.debug("LOTLOT_FINISHED_STATE is Empty. So Continue. {}", lot.getLotFinishedState());
            return false;
        }
        if (BizConstant.SP_DP_SELECTCRITERIA_EQPMONKIT.equals(selectCriteria)
                || BizConstant.SP_DP_SELECTCRITERIA_EQPMONNOKIT.equals(selectCriteria)) {
            //--------------------------
            // lot Process State Check
            //--------------------------
            if (!CIMStateConst.CIM_LOT_PROCESS_STATE_WAITING.equals(lot.getLotProcessState())) {
                log.debug("LOTLOT_PROCESS_STATE isn't Waiting. So Continue {}", lot.getLotProcessState());
                return false;
            }
            //--------------------------
            // lot Inventry State Check
            //--------------------------
            if (!CIMStateConst.CIM_LOT_INVENTORY_STATE_ONFLOOR.equals(lot.getLotInventoryState())) {
                log.debug("LOTLOT_INV_STATE isn't OnFloor. So Continue. {}", lot.getLotInventoryState());
                return false;
            }
            //--------------------------
            // lot LOT_TYPE Check
            //--------------------------
            if (!BizConstant.SP_LOT_TYPE_EQUIPMENTMONITORLOT.equals(lot.getLotType())
                    && !BizConstant.SP_LOT_TYPE_DUMMYLOT.equals(lot.getLotType())) {
                log.debug(" LOTLOT_TYPE isn't EquipmentMonitor or Dummy. So Continue. {}", lot.getLotType());
                return false;
            }
        }
        return true;
    }

    /**
     * Gets the logic recipe for Lot by ProcessDefinition and ProductSpecification.
     *
     * @param processDefinitionDO    ProcessDefinition
     * @param productSpecificationDO ProductSpecification
     * @return LogicalRecipe
     * @author ZQI
     * @date 2021/1/22 11:14
     */
    private CimLogicalRecipeDO whatNextProcessDefinitionInfo(CimProcessDefinitionDO processDefinitionDO,
                                                             CimProductSpecificationDO productSpecificationDO) {
        CimLogicalRecipeDO cimLogicalRecipeDO = new CimLogicalRecipeDO();
        String productSpecID = "";
        if (productSpecificationDO != null) {
            productSpecID = productSpecificationDO.getProductSpecID();
        }
        boolean logicalRecipeFoundFlag = false;
        String logicalRecipeID = null;
        String sql = "SELECT OMPRP.PRP_ID,\n" +   // PRP_ID 代替
                "                            OMPRP_LRPRD.LRCP_ID,\n" +
                "                            OMPRP_LRPRD.LRCP_RKEY\n" +
                "                     FROM   OMPRP,\n" +
                "                            OMPRP_LRPRD\n" +
                "                     WHERE  OMPRP.ID = ? AND\n" +
                "                            OMPRP_LRPRD.REFKEY = ? AND\n" +
                "                            OMPRP_LRPRD.PROD_ID = ?";
        Object[] objects = cimJpaRepository.queryOne(sql, processDefinitionDO.getId(),
                processDefinitionDO.getId(), productSpecID);
        if (objects != null) {
            logicalRecipeID = String.valueOf(objects[1]);
            if (CimStringUtils.isNotEmpty(logicalRecipeID)) {
                logicalRecipeFoundFlag = true;
            }
        }
        //----------------------------------------------------------------------------------
        // Search Logical Recipe by Product Group
        //----------------------------------------------------------------------------------
        if (!logicalRecipeFoundFlag) {
            if (productSpecificationDO != null) {
                sql = "SELECT OMPRP.PRP_ID,\n" +   // PRP_ID 代替
                        "                                     OMPRP_LRPRODFMLY.LRCP_ID,\n" +
                        "                                     OMPRP_LRPRODFMLY.LRCP_RKEY\n" +
                        "                              FROM   OMPRP,\n" +
                        "                                     OMPRP_LRPRODFMLY\n" +
                        "                              WHERE  OMPRP.ID              = ? AND\n" +
                        "                                     OMPRP_LRPRODFMLY.REFKEY = ? AND\n" +
                        "                                     OMPRP_LRPRODFMLY.PRODFMLY_ID     = ?";
                Object[] objects2 = cimJpaRepository.queryOne(sql, processDefinitionDO.getId(), processDefinitionDO.getId(), productSpecID);
                if (objects2 != null) {
                    logicalRecipeID = String.valueOf(objects2[1]);
                    if (!CimStringUtils.isEmpty(logicalRecipeID)) {
                        logicalRecipeFoundFlag = true;
                    }
                }
            }
        }
        //----------------------------------------------------------------------------------
        // Search Logical Recipe by Technology
        //----------------------------------------------------------------------------------
        if (!logicalRecipeFoundFlag) {
            sql = "SELECT * FROM OMPRODFMLY WHERE PRODFMLY_ID = ?";
            CimProductGroupDO cimProductGroupDO = cimJpaRepository.queryOne(sql, CimProductGroupDO.class, productSpecID);
            if (cimProductGroupDO != null) {
                sql = "SELECT OMPRP.PRP_ID,\n" +   // PRP_ID 代替
                        "                                     OMPRP_LRTECH.LRCP_ID,\n" +
                        "                                     OMPRP_LRTECH.LRCP_RKEY\n" +
                        "                              FROM   OMPRP,\n" +
                        "                                     OMPRP_LRTECH\n" +
                        "                              WHERE  OMPRP.ID              = ? AND\n" +
                        "                                     OMPRP_LRTECH.REFKEY = ? AND\n" +
                        "                                     OMPRP_LRTECH.TECH_ID        = ?";
                Object[] objects3 = cimJpaRepository.queryOne(sql, processDefinitionDO.getId(), processDefinitionDO.getId(), cimProductGroupDO.getTechnologyID());
                if (objects3 != null) {
                    logicalRecipeID = String.valueOf(objects3[1]);
                    if (!CimStringUtils.isEmpty(logicalRecipeID)) {
                        logicalRecipeFoundFlag = true;
                    }
                }
            }
        }
        //----------------------------------------------------------------------------------
        // Search Default Logical Recipe
        //----------------------------------------------------------------------------------
        if (!logicalRecipeFoundFlag) {
            logicalRecipeID = processDefinitionDO.getRecipeID();
            if (CimStringUtils.isNotEmpty(logicalRecipeID)) {
                logicalRecipeFoundFlag = true;
            }
        }
        if (CimStringUtils.isNotEmpty(logicalRecipeID)) {
            String version = cimFrameWorkGlobals.extractVersionFromID(logicalRecipeID);
            if (CimStringUtils.equals(version, BizConstant.SP_ACTIVE_VERSION)) {
                sql = "SELECT B.LRCP_ID, B.LRCP_RKEY\n" +
                        "                              FROM  OMLRCP A, OMLRCP B\n" +
                        "                              WHERE A.LRCP_ID = ? AND\n" +
                        "                                    B.LRCP_ID = A.ACTIVE_VER";
                cimLogicalRecipeDO = cimJpaRepository.queryOne(sql, CimLogicalRecipeDO.class, logicalRecipeID);
            } else {
                sql = "SELECT * FROM OMLRCP WHERE LRCP_ID = ?";
                cimLogicalRecipeDO = cimJpaRepository.queryOne(sql, CimLogicalRecipeDO.class, logicalRecipeID);
            }
        }
        return cimLogicalRecipeDO;
    }

    /**
     * Get WIP lots list from WIP queue for specified equipment, and set those lotIDs to WhatNextAttributes.
     * The sequence of setting lotIDs should follow dispatching rule of the equipment at that time.
     * <p>
     * From this queue, these conditioned lots are to be listed.
     * - lotState is CIMFW_Lot_State_Active
     * - lotHoldState is CIMFW_Lot_HoldState_NotOnHold
     * - lotProcessState is SP_Lot_ProcState_Waiting
     * - lotInventoryState is SP_Lot_InventoryStateOnFloor
     * - lotDispatchState is SP_Lot_DispatchStateNotDispatched
     * - lotType is SP_Lot_Type_ProductionLot or SP_Lot_Type_EngineeringLot
     * - processOperation can be done by equipmentID of In Parameter
     * <p>
     * If selectCriteria of In Parameter is "SALL" (Select all lots),set all lot IDs matched to above condition to WhatNextAttributes.
     * <p>
     * If selectCriteria of In Parameter is "SAVL" (Select lots can be processed),set all lot IDs matched to above conditions and
     * can be processed with the equipment to WhatNextAttributes.
     * <p>
     * If there are no available machine recipe for the current chamber status of Multiple chamber equipment,machineRecipeID will be set *.
     * <p>
     * If selectCriteria of In Parameter is "Auto3" (Select for Auto3),
     * set all lot IDs matched to above condition and the machine recipe is available with checking factor for "SAVL", and also the lot is not reserved.
     * <p>
     * If selectCriteria of In Parameter is "SHLD" (Select for hold lots),set all lot IDs which lotHoldState is CIMFW_Lot_HoldState_OnHold.
     *
     * @author ZQI
     * @date 2021/1/22 11:19
     */
    public Results.WhatNextLotListResult equipmentLotsWhatNextDR(Inputs.ObjEquipmentLotsWhatNextDRIn objEquipmentLotsWhatNextDRIn, Infos.ObjCommon objCommon) {
        Validations.check(null == objEquipmentLotsWhatNextDRIn || null == objCommon, retCodeConfig.getInvalidInputParam());

        /*StopWatch stopWatch = new StopWatch();*/

        // Declare results.
        Results.WhatNextLotListResult out = new Results.WhatNextLotListResult();
        //---------------------------------------------
        //  Set input parameters into local variable
        //---------------------------------------------
        ObjectIdentifier equipmentID = objEquipmentLotsWhatNextDRIn.getEquipmentID();
        String selectCriteria = objEquipmentLotsWhatNextDRIn.getSelectCriteria();
        log.debug("equipmentID = {}", equipmentID);
        log.debug("selectCriteria = {}", selectCriteria);

        // This environment variable can take 1 or 0 as value. If 1 is set to this environment variable, DOC
        // function becomes available. If 0 is set to this environment variable, DOC function cannot become
        // available (Even though DOC setting data exists, DOC function won’t be executed to any lots.).
        boolean adoptFPCInfo = StandardProperties.OM_DOC_ENABLE_FLAG.isTrue();
        log.debug("environmentVariable[SP_FPC_ADAPTATION_FLAG] = {}", adoptFPCInfo);

        List<Infos.StartSeqNo> strEqpMonJobMinStartSeqNoSeq = new ArrayList<>();

        List<Infos.EqpMonitorDetailInfo> eqpMonitorListGetDROut = new ArrayList<>();
        boolean select_criteria_EqpMonitor = BizConstant.SP_DP_SELECTCRITERIA_EQPMONKIT.equals(selectCriteria)
                || BizConstant.SP_DP_SELECTCRITERIA_EQPMONNOKIT.equals(selectCriteria);
        if (select_criteria_EqpMonitor) {
            log.debug("selectCriteria is EqpMonKit or EqpMonNoKit, prepare EqpMonitor information.");
            Inputs.ObjEqpMonitorListGetDRIn eqpMonitorListGetDRIn = new Inputs.ObjEqpMonitorListGetDRIn();
            eqpMonitorListGetDRIn.setEqpMonitorID(objEquipmentLotsWhatNextDRIn.getEqpMonitorID());
            eqpMonitorListGetDRIn.setEquipmentID(objEquipmentLotsWhatNextDRIn.getEquipmentID());
            eqpMonitorListGetDROut.addAll(equipmentMethod.eqpMonitorListGetDR(eqpMonitorListGetDRIn, objCommon));
        }

        ObjectIdentifier inhibitMachineRecipe = null;
        // This variable configures error handling patterns when the default machine recipe or related object
        // (e.g. chamber) is inhibited in start lot reservation. Specify “1” to allow OMS to continue
        // searching for the next available recipe according to equipment (chamber) status (Multiple Chamber
        // Logical Recipe only); if “0” is specified (default), OMS will return an error to the caller
        // immediately if the default recipe cannot be used.
        boolean entityInhibitSearchFlag = StandardProperties.OM_CONSTRAINT_CHK_WITH_SEARCH.isTrue();
        log.debug("entityInhibitSearchCondition[OM_CONSTRAINT_CHK_WITH_SEARCH] = {}", entityInhibitSearchFlag);


        //-------------------------------------
        // Step 1 - select Machine information
        //-------------------------------------
        /*stopWatch.start("What's Next - Step 1 - select Machine information");*/
        Object[] equipmentInfo = cimJpaRepository.queryOne("SELECT OMEQP.ID, " + // 0
                "                        OMEQP.LAST_USED_RECIPE_ID, " + //1
                "                        OMEQP.LAST_USED_RECIPE_RKEY, " + //2
                "                        OMEQP.MAX_BATCH_SIZE, " + //3
                "                        OMEQP.MIN_BATCH_SIZE, " + //4
                "                        OMEQP.DISP_RKEY, " + //5
                "                        OMEQP.EQP_STATE_ID, " + //6
                "                        OMEQP.EQP_STATE_RKEY, " + //7
                "                        OMEQP.RTCL_NEED_FLAG, " + //8
                "                        OMEQP.MULTI_RECIPE_CAPABLE, " + //9
                "                        OMEQP.EQP_CATEGORY " + //10
                "                 FROM   OMEQP " +
                "                 WHERE  OMEQP.EQP_ID = ?1", equipmentID.getValue());

        Validations.check(null == equipmentInfo, retCodeConfig.getNotFoundEqp());

        // Parse Value
        String _equipmentObj = CimObjectUtils.toString(equipmentInfo[0]);
        String _usedRecipeID = CimObjectUtils.toString(equipmentInfo[1]);
        String _usedRecipeObj = CimObjectUtils.toString(equipmentInfo[2]);
        String _batchSizeMax = CimObjectUtils.toString(equipmentInfo[3]);
        String _batchSizeMin = CimObjectUtils.toString(equipmentInfo[4]);
        String _dispatchObj = CimObjectUtils.toString(equipmentInfo[5]);
        String _eqpCurrentStateID = CimObjectUtils.toString(equipmentInfo[6]);
        String _eqpCurrentStateObj = CimObjectUtils.toString(equipmentInfo[7]);
        String _reticleReqFlag = CimObjectUtils.toString(equipmentInfo[8]);
        String _multiRecipeCapability = CimObjectUtils.toString(equipmentInfo[9]);
        String _eqpCategory = CimObjectUtils.toString(equipmentInfo[10]);


        equipmentID.setReferenceKey(_equipmentObj);
        out.setEquipmentID(equipmentID);
        out.setEquipmentCategory(_eqpCategory);

        boolean isEqpReticleReq = CimBooleanUtils.getBoolean(_reticleReqFlag);
        log.debug("Eqp reticle require = {} ", isEqpReticleReq);

        // Specify 1 to update UsedMachineRecipe of equipment when equipment performs operation start and
        // operation completion. The equipment object is locked during the update.
        // Specify 0 to discard updating of UsedMachineRecipe of equipment when equipment performs
        // operation start and operation completion.
        boolean lastUsedRecipeUpdateFlag = StandardProperties.OM_UPDATE_LAST_USED_RECIPE.isTrue();
        ;
        log.debug("environmentVariable[OM_UPDATE_LAST_USED_RECIPE] = {}", lastUsedRecipeUpdateFlag);

        if (lastUsedRecipeUpdateFlag) {
            log.debug("set recipeID");
            out.setLastRecipeID(ObjectIdentifier.build(_usedRecipeID, _usedRecipeObj));
        }

        out.setProcessRunSizeMaximum(Integer.valueOf(_batchSizeMax));
        out.setProcessRunSizeMinimum(Integer.valueOf(_batchSizeMin));

        boolean bondingEqpFlag;
        if (CIMStateConst.CIM_MC_CATEGORY_WAFER_BONDING.equals(out.getEquipmentCategory())) {
            log.debug("Equipment Category is SP_Mc_Category_WaferBonding");
            bondingEqpFlag = true;
        } else {
            bondingEqpFlag = false;
        }
        /*stopWatch.stop();*/


        //-------------------------------------------------
        // Step 2 - select Dispatching Rule information
        //-------------------------------------------------
        /*stopWatch.start("What's Next - Step 2 - select Dispatching Rule information");*/
        CimDispatcher aDispatcher = baseCoreFactory.getBO(CimDispatcher.class, _dispatchObj);
        Validations.check(null == aDispatcher, retCodeConfig.getNotFoundEqpDispatcher());

        log.debug("Dispatch = {}", aDispatcher.currentWhatNextForMachine().getParameters());
        out.setDispatchRule(aDispatcher.currentWhatNextForMachine().getParameters());
        /*stopWatch.stop();*/

        /*stopWatch.start("What's Next - Step 3 - Check Some rule about chamber,EQP state etc.");*/
        //-------------------------------------
        // check machine has chamber or not
        //-------------------------------------
        AtomicBoolean bMultiChamberMachineFlag = new AtomicBoolean(false);
        boolean saveMultiChamberMachineFlag = false;
        long nProcessResourceCount;


        List<Infos.WhatNextChamberInfoInfo> tmpChamberInfo = new ArrayList<>();
        long totalCount = cimJpaRepository.count("SELECT COUNT(OMPROCRES.EQP_ID) FROM OMPROCRES WHERE OMPROCRES.EQP_ID = ?1", equipmentID.getValue());

        if (0 < totalCount) {
            log.debug("The equipment has MultiChamber");
            int maxTotal = BizConstant.SP_MAX_CHAMBER_LEN;
            if (maxTotal < totalCount) {
                log.debug("Chamber count is over SP_MAX_CHAMBER_LEN(Default 20). Adjust it to SP_MAX_CHAMBER_LEN.");
                totalCount = maxTotal;
            }
            bMultiChamberMachineFlag.compareAndSet(false, true);
            saveMultiChamberMachineFlag = true;
            nProcessResourceCount = totalCount;
        } else {
            nProcessResourceCount = 0;
        }
        //-------------------------------------
        // check Conditional Available condition
        // and if machine is multi chamber type,
        // collect chamber information here.
        //-------------------------------------
        AtomicBoolean bConditionalAvailableFlagForChamber = new AtomicBoolean(false);
        boolean bConditionalAvailableFlagForEqp;

        List<String> availableSubLotTypesForEqp = new ArrayList<>();
        //----------------------------------------
        // Check Eqp's chamber available State
        //----------------------------------------
        if (bMultiChamberMachineFlag.get()) {
            String sql = " SELECT OMPROCRES.ID,\n" +
                    "        OMPROCRES.PROCRES_ID,\n" +
                    "        OMPROCRES.EQP_STATE_ID,\n" +
                    "        OMPROCRES.EQP_STATE_RKEY,\n" +
                    "        OMEQPST.EQP_AVAIL_FLAG,\n" +
                    "        OMEQPST.COND_AVAIL_FLAG\n" +
                    "   FROM OMPROCRES, OMEQPST\n" +
                    "  WHERE OMEQPST.EQP_STATE_ID = OMPROCRES.EQP_STATE_ID\n" +
                    "    AND OMPROCRES.EQP_ID = ?1 ";
            List<Object[]> eqpChamberQuery = cimJpaRepository.query(sql, equipmentID.getValue());
            if (CimArrayUtils.isNotEmpty(eqpChamberQuery)) {
                for (Object[] objects : eqpChamberQuery) {
                    Infos.WhatNextChamberInfoInfo whatNextChamberInfoInfo = new Infos.WhatNextChamberInfoInfo();
                    whatNextChamberInfoInfo.setChamberID(CimObjectUtils.toString(objects[1]));
                    whatNextChamberInfoInfo.setCurrentStateID(CimObjectUtils.toString(objects[2]));
                    whatNextChamberInfoInfo.setCurrentStateObjRef(CimObjectUtils.toString(objects[3]));
                    whatNextChamberInfoInfo.setAvailableFlag(CimBooleanUtils.isTrue(CimObjectUtils.toString(objects[4])));
                    boolean conditionalAvailable = CimBooleanUtils.isTrue(CimObjectUtils.toString(objects[5]));
                    whatNextChamberInfoInfo.setConditionalAvailable(conditionalAvailable);
                    tmpChamberInfo.add(whatNextChamberInfoInfo);

                    if (conditionalAvailable) {
                        log.debug("conditionalAvailable == TRUE");
                        bConditionalAvailableFlagForChamber.compareAndSet(false, true);
                    }
                }
            }
        }

        //-----------------------------------
        // Check available State Eqp itself
        //-----------------------------------
        String sql = "SELECT OMEQPST.ID, \n" +     // 0
                "   OMEQPST.EQP_AVAIL_FLAG, \n" +       // 1
                "   OMEQPST.COND_AVAIL_FLAG\n" +  // 2
                "   FROM OMEQPST\n" +
                "   WHERE OMEQPST.ID = ?1";
        Object[] eqpSTQuery = cimJpaRepository.queryOne(sql, _eqpCurrentStateObj);
        boolean condtnAvailableFlag = false;
        boolean availableFlag = false;
        if (null != eqpSTQuery) {
            availableFlag = CimBooleanUtils.isTrue(CimObjectUtils.toString(eqpSTQuery[1]));
            condtnAvailableFlag = CimBooleanUtils.isTrue(CimObjectUtils.toString(eqpSTQuery[2]));
        }

        boolean select_criteria_CANBEPROCESSED_OR_EQPMONNOKIT_OR_AUTO3 = BizConstant.SP_DP_SELECTCRITERIA_CANBEPROCESSED.equals(selectCriteria)
                || BizConstant.SP_DP_SELECTCRITERIA_EQPMONNOKIT.equals(selectCriteria)
                || BizConstant.SP_DP_SELECTCRITERIA_AUTO3.equals(selectCriteria);

        if (condtnAvailableFlag) {
            //--------------------------------------------
            //  Eqp is conditional available
            //  Get all subLotTypes
            //--------------------------------------------
            log.debug("OMEQPST.COND_AVAIL_FLAG = TRUE");
            bConditionalAvailableFlagForEqp = true;
            List<Object> eqpStateSubLotTypeList = cimJpaRepository.queryOneColumn(" SELECT SUB_LOT_TYPE FROM OMEQPST_SLTYP WHERE REFKEY = ?1", CimObjectUtils.toString(eqpSTQuery[0]));
            eqpStateSubLotTypeList.forEach(data -> availableSubLotTypesForEqp.add(CimObjectUtils.toString(data)));
        } else {
            //---------------------------------------------------------------
            //  Eqp is available for all Lots or not available for all Lots
            //---------------------------------------------------------------
            bConditionalAvailableFlagForEqp = false;
            log.debug("bMachineIsAvailableForLot = {}", availableFlag);

            if (!availableFlag) {
                //------------------------------------------------------------------------------------------------
                // If select criteria is "SP_DP_SelectCriteria_CanBeProcessed" or "SP_DP_SelectCriteria_Auto3",
                // then it means all Lots are NOT operable on this eqp.
                // What'sNext Process is not necessary to continue any more.
                //------------------------------------------------------------------------------------------------
                if (select_criteria_CANBEPROCESSED_OR_EQPMONNOKIT_OR_AUTO3)
                    return out;
            }
        }

        /*stopWatch.stop();*/

        //----------------------------------------------
        // Use Framework Method in order to retrieve
        // lot Sequence which is sorted by sort logic
        //----------------------------------------------
        /*stopWatch.start("What's Next - Step 4 - Get the Candidate Lot by Core Methods");*/
        CimMachine aMachine = baseCoreFactory.getBO(CimMachine.class, out.getEquipmentID());

        List<DispatcherDTO.DispatchDecision> aDispatchDecisionSequence;
        if (BizConstant.SP_DP_SELECTCRITERIA_HOLD.equals(selectCriteria)) {
            log.debug("DP_SELECTCRITERIA_HOLD == selectCriteria");
            aDispatchDecisionSequence = aDispatcher.whatNextHoldLotForMachine(aMachine);
        } else {
            log.debug("DP_SELECTCRITERIA_HOLD != selectCriteria");

            aDispatchDecisionSequence = aDispatcher.whatNextForMachine(aMachine);
            log.debug("aDispatchDecisionSequence = {}", aDispatchDecisionSequence);
        }
        /*stopWatch.stop();*/

        //------------------------------------------
        // set length of output lot info structure
        // Initiate parameters for output lot info.
        //------------------------------------------
        /*stopWatch.start("What's Next - Step 5 - Loop CandidateLot");*/
        int nCandidateLotCount = CimArrayUtils.getSize(aDispatchDecisionSequence);
        log.debug(">>>>>>>>>>>>>>>> nCandidateLotCount : " + nCandidateLotCount);
        if (nCandidateLotCount == 0) return out;

        //--------------------------------------------------------------------------------------------------------------
        //--------------------------------------------------------------------------------------------------------------
        //--------------------------------------------------------------------------------------------------------------
        boolean select_criteria_CANBEPROCESSED_OR_EQPMONKIT_OR_EQPMONNOKIT_OR_AUTO3 =
                BizConstant.SP_DP_SELECTCRITERIA_CANBEPROCESSED.equals(selectCriteria)
                        || BizConstant.SP_DP_SELECTCRITERIA_EQPMONKIT.equals(selectCriteria)
                        || BizConstant.SP_DP_SELECTCRITERIA_EQPMONNOKIT.equals(selectCriteria)
                        || BizConstant.SP_DP_SELECTCRITERIA_AUTO3.equals(selectCriteria);

        Map<String, Object> durableSubStatusList = new ConcurrentHashMap<>();
        List<Infos.FoundReticle> eqpInReticleSeq = new ArrayList<>();
        AtomicBoolean bReticleGroupListQueryDone = new AtomicBoolean(false);
        Map<String, Boolean> fPCApplied = new ConcurrentHashMap<>();
        Map<String, Boolean> bLotHasAssignedMRecipeSeq = new ConcurrentHashMap<>();
        LinkedList<Infos.DefaultRecipeSetting> strDefaultRecipeSetSeq = new LinkedList<>();
        Map<String, Infos.WhatNextAttributes> samePropertiesForLot = new ConcurrentHashMap<>();
        Map<String, String> subLotTypeSeqForCheck = new ConcurrentHashMap<>();
        Map<String, String> reticleSetSeqForCheck = new ConcurrentHashMap<>();
        Map<String, Boolean> FPCAppliedForCheck = new ConcurrentHashMap<>();
        Map<String, Boolean> inhibitCollectedBeforeContinue = new ConcurrentHashMap<>();
        Map<String, Inputs.ObjEntityInhibiteffectiveForLotGetDRIn> entityInhibiteffectiveForLotGetDRIns = new ConcurrentHashMap<>();


        List<Infos.WhatNextAttributes> whatNextAttributesList = aDispatchDecisionSequence.parallelStream()
                .filter(dispatchDecision -> null != dispatchDecision.getActivity())
                //-------------------------------------
                // Select lot info
                //-------------------------------------
                .map(dispatchDecision -> {
                    ProcessDTO.ProcessActivity processActivity = (ProcessDTO.ProcessActivity) dispatchDecision.getActivity();
                    return baseCoreFactory.getBO(CimLot.class, processActivity.getProcessMaterialGroups().get(0));
                })
                //--------------------------
                // Lot Status Check
                //--------------------------
                .filter(aLot -> this.whatNextLotStateCheck(aLot, selectCriteria))
                //--------------------------
                // Lot Dispatch Readiness State Check
                //--------------------------
                .filter(CimLot::isDispatchReady)
                //-------------------------------------------------------------------------
                //  When Lot is in post process, it is not included in return structure
                //-------------------------------------------------------------------------
                .filter(aLot -> {
                    if (CimBooleanUtils.isTrue(aLot.isPostProcessFlagOn())) {
                        return !select_criteria_CANBEPROCESSED_OR_EQPMONKIT_OR_EQPMONNOKIT_OR_AUTO3;
                    }
                    return true;
                })
                //-----------------------------------------------------------------------------------------
                //  When Lot's PENDING_MOVE_NEXT_FLAG  is true, it is not included in return structure
                //-----------------------------------------------------------------------------------------
                .filter(aLot -> CimBooleanUtils.isFalse(aLot.isPendingMoveNext()))
                //-----------------------------------------------------------------------
                // Get the cassette information
                //-----------------------------------------------------------------------
                .map(aLot -> {
                    String cassetteSql = "SELECT OMCARRIER.*\n" +
                            "  FROM OMCARRIER, OMCARRIER_LOT\n" +
                            " WHERE OMCARRIER_LOT.LOT_ID = ?\n" +
                            "   AND OMCARRIER.ID = OMCARRIER_LOT.REFKEY";
                    List<CimCassetteDO> cassetteDOList = cimJpaRepository.query(cassetteSql, CimCassetteDO.class, aLot.getIdentifier());
                    if (CimArrayUtils.isEmpty(cassetteDOList)) return null;
                    CimCassetteDO cassette = cassetteDOList.get(0);
                    FilterValueForWhatNextOut retVal = new FilterValueForWhatNextOut();
                    retVal.setLot(aLot);
                    retVal.setCassetteDO(cassette);
                    return retVal;
                })
                .filter(Objects::nonNull)
                //---------------------------------------------------------------------------------------------
                //  When Cassette related to Lot is in post process, Lot is not included in return structure
                //---------------------------------------------------------------------------------------------
                .filter(retValueForWhatNext -> {
                    CimCassetteDO cassette = retValueForWhatNext.getCassetteDO();
                    if (cassette.getPostProcessingFlag()) {
                        log.debug("Cassette related to Lot is in PostProcess.");
                        if (select_criteria_CANBEPROCESSED_OR_EQPMONKIT_OR_EQPMONNOKIT_OR_AUTO3) {
                            log.debug("SelectCriteria {} is CanBeProcessed or Auto3. ", selectCriteria);
                            return false;
                        }
                    }
                    return true;
                })
                //---------------------------------------------------------
                // Cassette sub state check
                //---------------------------------------------------------
                .filter(retValueForWhatNext -> {
                    CimLot aLot = retValueForWhatNext.getLot();
                    CimCassetteDO cassette = retValueForWhatNext.getCassetteDO();
                    if (CimStringUtils.isNotEmpty(cassette.getDurableSubStateID())) {
                        log.debug("cassetteSubStateID : {}", cassette.getDurableSubStateID());
                        if (select_criteria_CANBEPROCESSED_OR_EQPMONKIT_OR_EQPMONNOKIT_OR_AUTO3) {
                            log.debug("selectCriteria == CanBeProcessed or EqpMonKit or EqpMonNoKit or Auto3");
                            if (!durableSubStatusList.containsKey(cassette.getDurableSubStateID())) {
                                log.debug("durableSubStatusList.find() return false");
                                Inputs.ObjDurableSubStateDBInfoGetDRIn in = new Inputs.ObjDurableSubStateDBInfoGetDRIn();
                                in.setDurableSubStatus(cassette.getDurableSubStateID());
                                in.setAvailableSubLotTypeInfoFlag(true);
                                in.setNextTransitionDurableSubStatusInfoFlag(false);
                                Infos.DurableSubStatusInfo dbInfoGetDROut = durableMethod.durableSubStateDBInfoGetDR(objCommon, in);
                                durableSubStatusList.put(cassette.getDurableSubStateID(), dbInfoGetDROut);
                            }
                            Infos.DurableSubStatusInfo durableSubStatusInfo = (Infos.DurableSubStatusInfo) durableSubStatusList.get(cassette.getDurableSubStateID());
                            if (durableSubStatusInfo.getConditionalAvailableFlag()) {
                                log.debug("conditionalAvailableFlag == TRUE");
                                boolean existSubLotTypeFlag = false;
                                if (CimArrayUtils.isNotEmpty(durableSubStatusInfo.getAvailableSubLotTypes())) {
                                    for (String type : durableSubStatusInfo.getAvailableSubLotTypes()) {
                                        log.debug("subLotType : {}", type);
                                        if (type.equals(aLot.getSubLotType())) {
                                            existSubLotTypeFlag = true;
                                            break;
                                        }
                                    }
                                }
                                if (CimBooleanUtils.isFalse(existSubLotTypeFlag)) {
                                    log.debug("existSubLotTypeFlag == false");
                                    return false;
                                }
                            }
                        }
                    }
                    return true;
                })
                //-------------------------------------
                // Cassette for Auto3 check
                //-------------------------------------
                .filter(retValueForWhatNext -> {
                    CimLot aLot = retValueForWhatNext.getLot();
                    CimCassetteDO cassette = retValueForWhatNext.getCassetteDO();
                    if (BizConstant.SP_DP_SELECTCRITERIA_AUTO3.equals(selectCriteria)) {
                        if (CimStringUtils.isEmpty(cassette.getCassetteID())) {
                            log.debug("cassette is blank");
                            return false;
                        }
                        ObjectIdentifier cassetteID = ObjectIdentifier.build(cassette.getCassetteID(), cassette.getId());
                        /*-----------------------------------------*/
                        /*   Get cassette associated information   */
                        /*-----------------------------------------*/
                        Infos.LotListInCassetteInfo cassetteLotListGetResult = cassetteMethod.cassetteGetLotList(objCommon, cassetteID);

                        boolean bSkipFlag = false;
                        if (null != cassetteLotListGetResult) {
                            for (ObjectIdentifier lotID : cassetteLotListGetResult.getLotIDList()) {
                                log.debug("call lot_currentOperationInfo_Get()");
                                // maybe we need't this logic that check the associated lot is exist.
                                /*try {
                                    Outputs.ObjLotCurrentOperationInfoGetOut lotCurrentOperationInfoOut = lotMethod.lotCurrentOperationInfoGet(objCommon, lotID);
                                } catch (Exception e) {
                                    throw e;
                                }*/

                                //--------------------------------------------
                                //  Get Auto Dispatch Control Information
                                //--------------------------------------------
                                Inputs.ObjAutoDispatchControlInfoGetDRIn in = new Inputs.ObjAutoDispatchControlInfoGetDRIn();
                                in.setLotID(lotID);
                                List<Infos.LotAutoDispatchControlInfo> strAutoDispatchControlInfoGetDROut = autoDispatchControlMethod.autoDispatchControlInfoGetDR(objCommon, in);

                                String mainPDId = aLot.getMainProcessDefinition().getIdentifier();
                                for (Infos.LotAutoDispatchControlInfo lotAutoDispatchControlInfo : strAutoDispatchControlInfoGetDROut) {
                                    if (ObjectIdentifier.equalsWithValue(mainPDId, lotAutoDispatchControlInfo.getRouteID())
                                            && CimStringUtils.equals(aLot.getOperationNumber(), lotAutoDispatchControlInfo.getOperationNumber())) {
                                        bSkipFlag = true;
                                        break;
                                    } else if (ObjectIdentifier.equalsWithValue(mainPDId, lotAutoDispatchControlInfo.getRouteID())
                                            && BizConstant.SP_ADCSETTING_ASTERISK.equals(lotAutoDispatchControlInfo.getOperationNumber())) {
                                        bSkipFlag = true;
                                        break;
                                    } else if (BizConstant.SP_ADCSETTING_ASTERISK.equals(ObjectIdentifier.fetchValue(lotAutoDispatchControlInfo.getRouteID()))
                                            && BizConstant.SP_ADCSETTING_ASTERISK.equals(lotAutoDispatchControlInfo.getOperationNumber())) {
                                        bSkipFlag = true;
                                        break;
                                    }
                                }
                            }
                        }
                        if (bSkipFlag) {
                            log.debug("Auto Dispatch Control Lot : {}", aLot.getIdentifier());
                            return false;
                        }
                    }
                    return true;
                })
                //--------------------------------------------
                // Cassette for Auto3/EqpMonNoKit check
                //--------------------------------------------
                .filter(retValueForWhatNext -> {
                    CimLot aLot = retValueForWhatNext.getLot();
                    CimCassetteDO cassette = retValueForWhatNext.getCassetteDO();
                    if (BizConstant.SP_DP_SELECTCRITERIA_AUTO3.equals(selectCriteria)
                            || BizConstant.SP_DP_SELECTCRITERIA_EQPMONNOKIT.equals(selectCriteria)) {
                        if (CimStringUtils.isNotEmpty(cassette.getReserveUserID())) {
                            log.debug("Cassette is already reserved for transfering. now passing through");
                            return false;
                        }
                        // Check Cassette state
                        if (BizConstant.SP_DP_SELECTCRITERIA_EQPMONNOKIT.equals(selectCriteria)) {
                            if (!CIMStateConst.CIM_DURABLE_AVAILABLE.equals(cassette.getDurableState())
                                    && !CIMStateConst.CIM_DURABLE_INUSE.equals(cassette.getDurableState())) {
                                log.debug("Cassette is NotAvailable. now passing through");
                                return false;
                            }
                        }
                        //---------------------------------------------------------------------------------------------
                        //  When the cassette related to lots is reserved for retrieving lots, lot is not included in
                        //  return structure.
                        //---------------------------------------------------------------------------------------------
                        if (CimStringUtils.isNotEmpty(cassette.getSlmReservedEquipmentID())) {
                            log.debug("Cassette related to Lot is reserved for SLM operation.");
                            return false;
                        }
                        if (CimStringUtils.equals(BizConstant.SP_INTERFAB_XFERSTATE_REQUIRED, aLot.getInterFabTransferState())
                                || CimStringUtils.equals(BizConstant.SP_INTERFAB_XFERSTATE_TRANSFERRING, aLot.getInterFabTransferState())) {
                            log.debug("#### The lot INTERFAB_XFER_STATE == Required or Transferring. Skip this Lot. ");
                            return false;
                        }
                        boolean envRerouteXferFlag = StandardProperties.OM_XFER_REROUTE_FLAG.isTrue();
                        log.debug("OM_XFER_REROUTE_FLAG : {}", envRerouteXferFlag);
                        if (envRerouteXferFlag) {
                            // cassette transfer states: MI/SI/BI/II/AI/BOenvRerouteXferFlag
                            if (BizConstant.SP_TRANSSTATE_MANUALIN.equals(cassette.getTransferState())
                                    || BizConstant.SP_TRANSSTATE_STATIONIN.equals(cassette.getTransferState())
                                    || BizConstant.SP_TRANSSTATE_BAYIN.equals(cassette.getTransferState())
                                    || BizConstant.SP_TRANSSTATE_INTERMEDIATEIN.equals(cassette.getTransferState())
                                    || BizConstant.SP_TRANSSTATE_ABNORMALIN.equals(cassette.getTransferState())
                                    || BizConstant.SP_TRANSSTATE_BAYOUT.equals(cassette.getTransferState())) {
                                log.debug("transferStatus allows re-route. : {}", cassette.getTransferState());
                            }
                            // cassette transfer status: SO/EO
                            else if (BizConstant.SP_TRANSSTATE_STATIONOUT.equals(cassette.getTransferState())
                                    || BizConstant.SP_TRANSSTATE_EQUIPMENTOUT.equals(cassette.getTransferState())) {
                                log.debug(" check transfer job existence.");
                                Object singleSqlResult = cimJpaRepository.queryOne("SELECT DEST_MACHINE_ID\n" +
                                        "                            FROM   OTXFERREQ\n" +
                                        "                            WHERE  CARRIER_ID= ?1 \n" +
                                        "                            AND ROWNUM = 1", cassette.getCassetteID());
                                if (null == singleSqlResult) {
                                    log.debug("Transfer job does not exist. continue...");
                                    return false;
                                }
                                String fxtrnReqToMachineID = String.valueOf(singleSqlResult);

                                log.debug("Transfer job exists. Next, confirm the to_machine is stocker.");
                                singleSqlResult = cimJpaRepository.queryOne("SELECT ID\n" +
                                        "                                 FROM   OMSTOCKER\n" +
                                        "                                 WHERE  STOCKER_ID= ?1 ", fxtrnReqToMachineID);
                                if (null == singleSqlResult) {
                                    log.debug("To machine is NOT stocker. continue...");
                                    return false;
                                }

                                log.debug("transferStatus {} allows re-route. ", cassette.getTransferState());

                            }
                            // transferStatus is not MI, SI, BI, II, AI, BO, SO, EO
                            else {
                                log.debug("transferStatus is not MI, SI, BI, II, AI, BO, SO, EO : {}", cassette.getTransferState());
                                return false;
                            }
                        }
                        // transferStatus is not MI, SI, BI, II, AI
                        else {
                            if (!BizConstant.SP_TRANSSTATE_MANUALIN.equals(cassette.getTransferState())
                                    && !BizConstant.SP_TRANSSTATE_STATIONIN.equals(cassette.getTransferState())
                                    && !BizConstant.SP_TRANSSTATE_BAYIN.equals(cassette.getTransferState())
                                    && !BizConstant.SP_TRANSSTATE_INTERMEDIATEIN.equals(cassette.getTransferState())
                                    && !BizConstant.SP_TRANSSTATE_ABNORMALIN.equals(cassette.getTransferState())) {
                                log.debug("transferStatus is not MI, SI, BI, II, AI: {}", cassette.getTransferState());
                                return false;
                            }
                        }
                    }
                    // Cassette for EqpMonKit check
                    else if (BizConstant.SP_DP_SELECTCRITERIA_EQPMONKIT.equals(selectCriteria)) {
                        log.debug("selectCriteria == EqpMonKit");
                        // Check Cassette Resereved state
                        if (CimStringUtils.isNotEmpty(cassette.getReserveUserID())) {
                            log.debug("Cassette is already reserved for transfering. now passing through");
                            return false;
                        }
                        // Check Cassette Dispatch state
                        if (cassette.getDispatchReserved()) {
                            log.debug("Cassette is already dispatched.");
                            return false;
                        }
                        // Check Cassette Xfer state
                        if (CimStringUtils.equals(BizConstant.SP_INTERFAB_XFERSTATE_TRANSFERRING, cassette.getInterFabTransferState())) {
                            log.debug("Cassette INTERFAB_XFER_STATE is Transferring. now passing through");
                            return false;
                        }
                        // Check Lot Xfer state
                        if (BizConstant.SP_INTERFAB_XFERSTATE_REQUIRED.equals(aLot.getInterFabTransferState())
                                || BizConstant.SP_INTERFAB_XFERSTATE_TRANSFERRING.equals(aLot.getInterFabTransferState())) {
                            log.debug("The lot INTERFAB_XFER_STATE == Required or Transferring. Skip this Lot.");
                            return false;
                        }
                        if (!BizConstant.SP_TRANSSTATE_MANUALIN.equals(cassette.getTransferState())
                                && !BizConstant.SP_TRANSSTATE_STATIONIN.equals(cassette.getTransferState())
                                && !BizConstant.SP_TRANSSTATE_BAYIN.equals(cassette.getTransferState())) {
                            log.debug("transferStatus is not MI, SI, BI : {}", cassette.getTransferState());
                            return false;
                        }
                    }
                    return true;
                })
                //--------------------------
                // Results collection
                //--------------------------
                .map(retValueForWhatNext -> {
                    // BUG-7338:Auto monitor设备有多chamber时根据不同的chamber状态系统选择对应的recipe功能失效
                    boolean bMultiChamberMachineFlag_LocalThead = bMultiChamberMachineFlag.get();

                    CimLot aLot = retValueForWhatNext.getLot();
                    CimCassetteDO cassette = retValueForWhatNext.getCassetteDO();

                    boolean bMachineIsAvailableForLot = false;
                    //------------------------------------------------------------------
                    // Check whether Lot is operable on this Eqp with current status
                    //------------------------------------------------------------------
                    if (bConditionalAvailableFlagForEqp) {
                        int j;
                        int slotLen = CimArrayUtils.getSize(availableSubLotTypesForEqp);
                        for (j = 0; j < slotLen; j++) {
                            if (aLot.getSubLotType().equals(availableSubLotTypesForEqp.get(j))) {
                                //-------------------------------
                                //  The Lot is operable.
                                //-------------------------------
                                bMachineIsAvailableForLot = true;
                                break;
                            }
                        }
                        if (j == slotLen) {
                            //------------------------------------------------------------------------------------------------
                            //  The Lot is not operable.
                            //  If select criteria is "SP_DP_SelectCriteria_CanBeProcessed" or "SP_DP_SelectCriteria_Auto3",
                            //  then it means this lot don't have to be set in return structure. Continue to next Lot !!
                            //------------------------------------------------------------------------------------------------
                            if (select_criteria_CANBEPROCESSED_OR_EQPMONNOKIT_OR_AUTO3) {
                                log.debug("operableFlagForCurrentMachineState is not TRUE");
                                return null;
                            }
                            bMachineIsAvailableForLot = false;
                        }
                    }
                    // Eqp is NOT conditional available.
                    else {
                        //--------------------------------------------------------------------
                        //  Eqp is NOT conditional available.
                        //  It means bMachineIsAvailableForLot can be shared for all Lots.
                        //  bMachineIsAvailableForLot is not necessary to switch TRUE or false.
                        //--------------------------------------------------------------------
                        log.debug("Eqp is NOT conditional available.");
                    }

                    // 65 rows of results
                    Infos.WhatNextAttributes whatNextAttributes = new Infos.WhatNextAttributes();
                    whatNextAttributes.setOperableFlagForCurrentMachineState(bMachineIsAvailableForLot);
                    ObjectIdentifier tmpLotID = ObjectIdentifier.build(aLot.getIdentifier(), aLot.getPrimaryKey());
                    whatNextAttributes.setLotID(tmpLotID);
                    whatNextAttributes.setLotType(aLot.getLotType());
                    if (null != aLot.getFlowBatch()) {
                        whatNextAttributes.setFlowBatchID(ObjectIdentifier.build(aLot.getFlowBatch().getIdentifier(), aLot.getFlowBatch().getPrimaryKey()));
                    }

                    // productID is set to productspecID;
                    whatNextAttributes.setProductID(aLot.getProductSpecificationID());
                    if (null != aLot.getMainProcessDefinition()) {
                        whatNextAttributes.setRouteID(ObjectIdentifier.build(aLot.getMainProcessDefinition().getIdentifier(), aLot.getMainProcessDefinition().getPrimaryKey()));
                    }

                    whatNextAttributes.setOperationNumber(aLot.getOperationNumber());
                    whatNextAttributes.setTotalWaferCount(CimArrayUtils.getSize(aLot.getAllWaferInfo()));
                    whatNextAttributes.setLastClaimedTimeStamp(aLot.getLastClaimedTimeStamp());
                    whatNextAttributes.setStateChangeTimeStamp(aLot.getStateChangedTimeStamp());
                    whatNextAttributes.setQueuedTimeStamp(aLot.getQueuedTimeStamp());
                    whatNextAttributes.setDueTimeStamp(aLot.getPlannedCompletionDateTime());
                    whatNextAttributes.setInventoryChangeTimeStamp(aLot.getInventoryStateChangedTimeStamp());
                    whatNextAttributes.setProcessHoldFlag(false);
                    whatNextAttributes.setTotalGoodDieCount(0);
                    whatNextAttributes.setRequiredCassetteCategory(aLot.getRequiredCassetteCategory());
                    whatNextAttributes.setInPostProcessFlagOfLot(aLot.isPostProcessFlagOn());
                    whatNextAttributes.setBondingFlowSectionName(aLot.getBondingFlowName());


                    //***********************//
                    // Bonding flow
                    //***********************//
                    boolean isLotInBondingFlow = CimStringUtils.isNotEmpty(aLot.getBondingFlowName());
                    String bondingGroupId = "";
                    if (isLotInBondingFlow || bondingEqpFlag) {
                        log.debug("Lot is in a Bonding Flow Section or Equipment is WaferBonding.");
                        //---------------------------------------------------------------------------------
                        //  Get Bonding Group ID
                        //---------------------------------------------------------------------------------
                        bondingGroupId = lotMethod.lotBondingGroupIDGetDR(objCommon, tmpLotID);

                        if (select_criteria_CANBEPROCESSED_OR_EQPMONNOKIT_OR_AUTO3) {
                            if (CimStringUtils.isEmpty(bondingGroupId)) {
                                log.debug("Lot {} is not bonding grouped. ", tmpLotID.getValue());
                                //---------------------------------------------------------------------------------
                                //  When the Lot does not belong to any Bonding Group,
                                //  it is not included in return structure if it is in Bonding Flow Section.
                                //---------------------------------------------------------------------------------
                                if (!bondingEqpFlag) {
                                    log.debug("Lot is in a Bonding Flow Section.");
                                    return null;
                                }
                            } else {
                                log.debug("Lot belongs to a bonding group {}. ", bondingGroupId);
                                //---------------------------------------------------------------------------------
                                //  When the Lot belongs to a Bonding Group,
                                //  it is not included in return structure if the Bonding Group has Error State.
                                //---------------------------------------------------------------------------------
                                Outputs.ObjBondingGroupInfoGetDROut strBondingGroupInfoGetDROut = bondingGroupMethod.bondingGroupInfoGetDR(objCommon, bondingGroupId, false);
                                if (CimStringUtils.equals(strBondingGroupInfoGetDROut.getBondingGroupInfo().getBondingGroupState(), BizConstant.SP_BONDINGGROUPSTATE_ERROR)) {
                                    return null;
                                }

                                if (bondingEqpFlag
                                        && ObjectIdentifier.isNotEmpty(strBondingGroupInfoGetDROut.getBondingGroupInfo().getTargetEquipmentID())
                                        && !CimStringUtils.equals(strBondingGroupInfoGetDROut.getBondingGroupInfo().getTargetEquipmentID().getValue(), equipmentID.getValue())) {
                                    log.debug("Target Equipment of Bonding Group differs {}.", strBondingGroupInfoGetDROut.getBondingGroupInfo().getTargetEquipmentID().getValue());
                                    return null;
                                }
                            }
                        }
                    }
                    whatNextAttributes.setBondingGroupID(bondingGroupId);

                    //*****************************//
                    //   EQP Monitor               //
                    //*****************************//
                    // get eqp monitor switch environment variable value.
                    boolean bMonitorOperationFlag = false;
                    String strEqpMonitorJobID = null;
                    int nStartSeq = 0;
                    if (StandardProperties.OM_AUTOMON_FLAG.isTrue()
                            && (BizConstant.SP_LOT_TYPE_EQUIPMENTMONITORLOT.equals(aLot.getLotType())
                            || BizConstant.SP_LOT_TYPE_DUMMYLOT.equals(aLot.getLotType()))) {
                        log.debug("Equipment monitor SWITCH is 1 and lot type is EquipmentMonitor or Dummy");

                        // Check logic for WhatNextAMLotInq
                        if (select_criteria_EqpMonitor) {
                            log.debug("selectCriteria is EqpMonKit or EqpMonNoKit, checking product type");
                            boolean bFoundProd = false;
                            if (CimArrayUtils.isNotEmpty(eqpMonitorListGetDROut)) {

                                List<Infos.EqpMonitorProductInfo> eqpMonitorProductInfoSeq = eqpMonitorListGetDROut
                                        .get(0).getStrEqpMonitorProductInfoSeq();

                                if (CimArrayUtils.isNotEmpty(eqpMonitorProductInfoSeq)) {
                                    for (int j = 0; j < eqpMonitorProductInfoSeq.size(); j++) {

                                        ObjectIdentifier machineRecipeId = null;
                                        try {
                                            Outputs.ObjLotRecipeGetOut objLotRecipeGetOut = lotMethod
                                                    .lotRecipeGet(objCommon,
                                                    equipmentID,
                                                    ObjectIdentifier.build(aLot.getIdentifier(), aLot.getPrimaryKey()));
                                            machineRecipeId = objLotRecipeGetOut.getMachineRecipeId();
                                        } catch (ServiceException ex) {
                                            continue;
                                        }
                                        if (!ObjectIdentifier.equalsWithValue(machineRecipeId, eqpMonitorProductInfoSeq.get(j).getRecipeID())) {
                                            continue;
                                        }

                                        ObjectIdentifier productID = eqpMonitorProductInfoSeq.get(j).getProductID();
                                        log.debug("loop {} through strEqpMonitorProductInfoSeq ,product: {}", j, productID.getValue());
                                        if (ObjectIdentifier.equalsWithValue(aLot.getProductSpecificationID(), productID)) {
                                            log.debug("Found productID {}.", aLot.getProductSpecificationID());
                                            bFoundProd = true;
                                            break;
                                        }
                                    }
                                    if (!bFoundProd) {
                                        log.debug("Product is not matched.");
                                        return null;
                                    }
                                }
                            }
                        }

                        //Get EqpMonitorJob information
                        String monitorJobEqp = null;
                        String monitorJobStatus = null;
                        String monitorOpeKey = null;
                        String sqlEqpmonJob = " SELECT " +
                                "          a.AM_JOB_ID,\n" +       // 0
                                "          a.EQP_ID,\n" +             // 1
                                "          a.AM_JOB_STATUS,\n" +      // 2
                                "          b.START_SEQ_NO,\n" +       // 3
                                "          b.AM_OPE_KEY,\n" +    // 4
                                "          b.AM_LOT_STATUS\n" +       // 5
                                "     FROM OMAMJOB a, OMAMJOB_LOT b\n" +
                                "    WHERE b.LOT_ID = ?1 \n" +
                                "      AND a.ID = b.REFKEY\n";
                        Object[] eqpMonJobAndEqpMonJobLot = cimJpaRepository.queryOne(sqlEqpmonJob, aLot.getIdentifier());
                        if (null != eqpMonJobAndEqpMonJobLot) {
                            log.debug("Lot is included in EqpMonitor job");
                            if (BizConstant.SP_EQPMONITOR_LEVEL_EQPMONKIT.equals(selectCriteria)
                                    || BizConstant.SP_EQPMONITOR_LEVEL_EQPMONNOKIT.equals(selectCriteria)) {
                                log.debug("lot is included in EqpMonitor job, remove the lot.");
                                // If lot is included in EqpMonitor job, the lot is out of target for EqpMonitor lot reservation
                                return null;
                            }
                            strEqpMonitorJobID = String.valueOf(eqpMonJobAndEqpMonJobLot[0]);
                            monitorJobEqp = String.valueOf(eqpMonJobAndEqpMonJobLot[1]);
                            monitorJobStatus = String.valueOf(eqpMonJobAndEqpMonJobLot[2]);
                            nStartSeq = ((BigDecimal) eqpMonJobAndEqpMonJobLot[3]).intValue();
                            monitorOpeKey = String.valueOf(eqpMonJobAndEqpMonJobLot[4]);
                        } else {
                            log.debug("Can't find equipment monitor job information of Lot.");
                        }

                        //Check monitor process or not
                        String sql2 = "SELECT COUNT(1)\n" +
                                "  FROM OMPRSS_AM a, OMLOT b, OMPROPE c\n" +
                                "  WHERE b.LOT_ID = ?1 \n" +
                                "  AND b.PROPE_RKEY = c.ID\n" +
                                "  AND c.ROUTE_PRSS_RKEY = a.PRSS_RKEY\n" +
                                "  AND a.OPE_TAG = ?2 ";
                        long monitorList = cimJpaRepository.count(sql2,
                                aLot.getIdentifier(),
                                BizConstant.SP_EQPMONITOR_SECTIONLABEL_MONITOR);
                        if (monitorList > 0) {
                            log.debug("monitorList > 0");
                            bMonitorOperationFlag = true;
                        }

                        // Check logic for WhatNextAMLotInq
                        if (select_criteria_EqpMonitor) {
                            log.debug("selectCriteria is EqpMonKit or EqpMonNoKit");
                            if (CimBooleanUtils.isFalse(bMonitorOperationFlag)) {
                                // Lot's current operation isn't "Monitor"
                                log.debug("Lot's current operation isn't Monitor");
                                return null;
                            }
                        }
                        if (BizConstant.SP_DP_SELECTCRITERIA_AUTO3.equals(selectCriteria)
                                && CimStringUtils.isEmpty(strEqpMonitorJobID) && bMonitorOperationFlag) {
                            log.debug("Lot's current operation isn't Monitor without eqpMonitorJob.");
                            return null;
                        }

                        if (BizConstant.SP_DP_SELECTCRITERIA_AUTO3.equals(selectCriteria)
                                && CimStringUtils.isNotEmpty(strEqpMonitorJobID) && bMonitorOperationFlag) {
                            String sql3 = " SELECT OMPROPE.MAIN_PROCESS_ID, OMPROPE.OPE_NO \n" +
                                    "   FROM OMLOT, OMPRFCX, OMPRFCX_BCKPROPESEQ, OMPROPE \n" +
                                    "  WHERE OMLOT.LOT_ID = ? \n" +
                                    "    AND OMLOT.PRFCX_RKEY = OMPRFCX.ID \n" +
                                    "    AND OMPRFCX.ID = OMPRFCX_BCKPROPESEQ.REFKEY \n" +
                                    "    AND OMPROPE.ID = OMPRFCX_BCKPROPESEQ.PROPE_RKEY \n" +
                                    "  ORDER BY OMPRFCX_BCKPROPESEQ.IDX_NO";
                            List<Object[]> list3 = cimJpaRepository.query(sql3, aLot.getIdentifier());
                            StringBuffer strMonOpeKey = null;
                            if (CimArrayUtils.isNotEmpty(list3)) {
                                for (Object[] list : list3) {
                                    if (null == strMonOpeKey) {
                                        log.debug("strMonOpeKey is empty, copy {}", CimObjectUtils.toString(list[0]));
                                        strMonOpeKey = new StringBuffer();
                                    } else {
                                        strMonOpeKey.append(BizConstant.SP_EQPMONITOROPERATIONKEY_SEPARATOR_CHAR);
                                    }
                                    strMonOpeKey.append(list[0]);
                                    strMonOpeKey.append(BizConstant.SP_EQPMONITOROPERATIONKEY_SEPARATOR_CHAR);
                                    strMonOpeKey.append(list[1]);
                                    strMonOpeKey.append(BizConstant.SP_EQPMONITOROPERATIONKEY_SEPARATOR_CHAR);
                                }
                            }

                            String sql4 = "SELECT OMPROPE.MAIN_PROCESS_ID, OMPROPE.OPE_NO\n" +
                                    "  FROM OMPROPE, OMLOT \n" +
                                    " WHERE OMLOT.LOT_ID = ?\n" +
                                    "   AND OMLOT.PROPE_RKEY = OMPROPE.ID";
                            List<Object[]> list4 = cimJpaRepository.query(sql4, aLot.getIdentifier());
                            if (CimArrayUtils.isNotEmpty(list4)) {
                                for (Object[] list : list4) {
                                    if (null == strMonOpeKey) {
                                        strMonOpeKey = new StringBuffer();
                                    } else {
                                        strMonOpeKey.append(BizConstant.SP_EQPMONITOROPERATIONKEY_SEPARATOR_CHAR);
                                    }
                                    strMonOpeKey.append(list[0]);
                                    strMonOpeKey.append(BizConstant.SP_EQPMONITOROPERATIONKEY_SEPARATOR_CHAR);
                                    strMonOpeKey.append(list[1]);
                                }
                            }

                            if (!strMonOpeKey.toString().equals(monitorOpeKey)) {
                                bMonitorOperationFlag = false;
                            } else {
                                // The lot is on "Monitor" process for EqpMonitor job
                                //Check of EqpMonitor Information
                                if (!monitorJobEqp.equals(equipmentID.getValue())) {
                                    return null;
                                }
                                //Check of EqpMonitor Job Status
                                if (!BizConstant.SP_EQPMONITORJOB_STATUS_READY.equals(monitorJobStatus)
                                        && !BizConstant.SP_EQPMONITORJOB_STATUS_EXECUTING.equals(monitorJobStatus)) {
                                    return null;
                                }
                                //Check StartSeq number
                                boolean bEqpMonJobFound = false;
                                boolean bIsMin = false;
                                for (Infos.StartSeqNo startSeqNo : strEqpMonJobMinStartSeqNoSeq) {
                                    if (strEqpMonitorJobID.equals(startSeqNo.getKey())) {
                                        bEqpMonJobFound = true;
                                        if (nStartSeq == startSeqNo.getStartSeqNo()) {
                                            bIsMin = true;
                                        }
                                        break;
                                    }
                                }

                                if (CimBooleanUtils.isFalse(bEqpMonJobFound)) {
                                    log.debug("Not Found eqpMonitorJobID, get minimal startSeqNo from database.");
                                    //get smallest StartSeqNo for the EqpMonitor job among EqpMonitor lots whose status is "Reserved";
                                    String sql5 = "SELECT MIN(OMAMJOB_LOT.START_SEQ_NO)\n" +
                                            "  FROM OMAMJOB, OMAMJOB_LOT\n" +
                                            " WHERE OMAMJOB.AM_JOB_ID = ?\n" +
                                            "   AND OMAMJOB_LOT.AM_LOT_STATUS = 'Reserved'\n" +
                                            "   AND OMAMJOB.ID = OMAMJOB_LOT.REFKEY";

                                    Object[] miniamlResult = cimJpaRepository.queryOne(sql5, strEqpMonitorJobID);
                                    if (null != miniamlResult) {
                                        Infos.StartSeqNo startSeqNo = new Infos.StartSeqNo();
                                        startSeqNo.setKey(strEqpMonitorJobID);
                                        int minRet = ((BigDecimal) miniamlResult[0]).intValue();
                                        startSeqNo.setStartSeqNo(minRet);
                                        strEqpMonJobMinStartSeqNoSeq.add(startSeqNo);
                                        if (nStartSeq != minRet) {
                                            log.debug("It's not minimal startSeqNo, bypass this lot");
                                            return null;
                                        }

                                    }
                                } else if (CimBooleanUtils.isFalse(bIsMin)) {
                                    return null;
                                }
                            }
                        }
                    }
                    whatNextAttributes.setMonitorOperationFlag(bMonitorOperationFlag);
                    whatNextAttributes.setEqpMonitorJobID(strEqpMonitorJobID);
                    whatNextAttributes.setStartSeqNo(nStartSeq);

                    //*******************************************//
                    //  Get Auto Dispatch Control Information
                    //*******************************************//
                    boolean bAutoDispatchDisableFlag = false;
                    Inputs.ObjAutoDispatchControlInfoGetDRIn objAutoDispatchControlInfoGetDRIn = new Inputs.ObjAutoDispatchControlInfoGetDRIn();
                    objAutoDispatchControlInfoGetDRIn.setLotID(ObjectIdentifier.build(aLot.getIdentifier(), aLot.getPrimaryKey()));
                    // call autoDispatchControlMethod.autoDispatchControlInfoGetDR(...)
                    List<Infos.LotAutoDispatchControlInfo> objAutoDispatchControlInfoGetDROut = autoDispatchControlMethod.autoDispatchControlInfoGetDR(objCommon, objAutoDispatchControlInfoGetDRIn);
                    for (Infos.LotAutoDispatchControlInfo lotAutoDispatchControlInfo : objAutoDispatchControlInfoGetDROut) {
                        if (CimStringUtils.equals(aLot.getMainProcessDefinition().getIdentifier(), ObjectIdentifier.fetchValue(lotAutoDispatchControlInfo.getRouteID()))
                                && aLot.getOperationNumber().equals(lotAutoDispatchControlInfo.getOperationNumber())) {
                            bAutoDispatchDisableFlag = true;
                            break;
                        } else if (aLot.getMainProcessDefinition().getIdentifier().equals(ObjectIdentifier.fetchValue(lotAutoDispatchControlInfo.getRouteID()))
                                && BizConstant.SP_DEFAULT_CHAR.equals(lotAutoDispatchControlInfo.getOperationNumber())) {
                            bAutoDispatchDisableFlag = true;
                            break;
                        } else if (BizConstant.SP_DEFAULT_CHAR.equals(ObjectIdentifier.fetchValue(lotAutoDispatchControlInfo.getRouteID()))
                                && BizConstant.SP_DEFAULT_CHAR.equals(lotAutoDispatchControlInfo.getOperationNumber())) {
                            bAutoDispatchDisableFlag = true;
                            break;
                        }
                    }
                    if (BizConstant.SP_DP_SELECTCRITERIA_AUTO3.equals(selectCriteria)) {
                        if (bAutoDispatchDisableFlag) {
                            log.debug("Auto Dispatch Control Information is exist. continue...");
                            return null;
                        }
                    }
                    whatNextAttributes.setAutoDispatchDisableFlag(bAutoDispatchDisableFlag);


                    //***********************************//
                    // Priority
                    //***********************************//
                    whatNextAttributes.setPriorityClass(null != aLot.getPriorityClass() ? aLot.getPriorityClass().toString() : "");
                    whatNextAttributes.setExternalPriority(null != aLot.getPriority() ? aLot.getPriority().toString() : "");


                    //***********************************//
                    // Get LotNote Info
                    //***********************************//
                    Infos.LotNoteFlagInfo lotNoteFlagInfo = new Infos.LotNoteFlagInfo();
                    lotNoteFlagInfo.setLotCommentFlag(false);

                    CimLotComment aLotComment = aLot.getLotComment();
                    if (null != aLotComment && CimStringUtils.isNotEmpty(aLotComment.getContents())) {
                        lotNoteFlagInfo.setLotCommentFlag(true);
                    }

                    long lotNoteCount = cimJpaRepository.count(" SELECT COUNT(1) FROM OMLOT_MEMO WHERE REFKEY = ?1", aLot.getPrimaryKey());
                    lotNoteFlagInfo.setLotNoteFlag(lotNoteCount > 0);

                    // if Note of the lot one the Step exists set the  LotOperationNoteflag to true, otherwise false.
                    long lotOpeNotesCount = cimJpaRepository.count("SELECT COUNT(ID) FROM OMLOTOPEMEMO WHERE  LOT_ID = ?1 AND\n" +
                            "                            MAIN_PROCESS_ID = ?2 AND\n" +
                            "                            OPE_NO = ?3", aLot.getIdentifier(), aLot.getMainProcessDefinition().getIdentifier(), aLot.getOperationNumber());
                    lotNoteFlagInfo.setLotOperationNoteFlag(lotOpeNotesCount > 0);
                    whatNextAttributes.setStrLotNoteFlagInfo(lotNoteFlagInfo);

                    // todo: 需要再次确认该逻辑是否有问题
//                    subLotTypeSeq.add(aLot.getSubLotType());
//                    reticleSetSeq.add(null != aLot.getReticleSet() ? aLot.getReticleSet().getIdentifier() : null);
                    String reticleSetID = null == aLot.getReticleSet() ? null : aLot.getReticleSet().getIdentifier();

                    log.debug("Collected Reticle Set : {}", reticleSetID);

                    //***********************************//
                    //  Lot Effective DOC info
                    //***********************************//
                    Outputs.ObjLotEffectiveFPCInfoGetOut strLotEffectiveFPCInfoGetOut;
                    if (adoptFPCInfo) {
                        try {
                            strLotEffectiveFPCInfoGetOut = lotMethod.lotEffectiveFPCInfoGet(objCommon, BizConstant.SP_FPC_EXCHANGETYPE_ALL, equipmentID, tmpLotID);
                        } catch (ServiceException e) {
                            return null;
                        }
                        if (strLotEffectiveFPCInfoGetOut.isEquipmentActionRequiredFlag()
                                || strLotEffectiveFPCInfoGetOut.isMachineRecipeActionRequiredFlag()
                                || strLotEffectiveFPCInfoGetOut.isRecipeParameterActionRequiredFlag()
                                || strLotEffectiveFPCInfoGetOut.isDcDefActionRequiredFlag()
                                || strLotEffectiveFPCInfoGetOut.isDcSpecActionRequiredFlag()
                                || strLotEffectiveFPCInfoGetOut.isReticleActionRequiredFlag()) {
                            log.debug("Effective FPCInfo found.");
                            fPCApplied.put(aLot.getIdentifier(), true);
                        } else {
                            log.debug("Effective FPCInfo NOT found.");
                            fPCApplied.put(aLot.getIdentifier(), false);
                        }
                    }
                    // adoptFPCInfo == false
                    else {
                        log.debug("adoptFPCInfo == false");
                        strLotEffectiveFPCInfoGetOut = new Outputs.ObjLotEffectiveFPCInfoGetOut();
                        strLotEffectiveFPCInfoGetOut.setEquipmentActionRequiredFlag(false);
                        strLotEffectiveFPCInfoGetOut.setMachineRecipeActionRequiredFlag(false);
                        strLotEffectiveFPCInfoGetOut.setRecipeParameterActionRequiredFlag(false);
                        strLotEffectiveFPCInfoGetOut.setDcDefActionRequiredFlag(false);
                        strLotEffectiveFPCInfoGetOut.setDcSpecActionRequiredFlag(false);
                        strLotEffectiveFPCInfoGetOut.setReticleActionRequiredFlag(false);
                        fPCApplied.put(aLot.getIdentifier(), false);
                    }
                    /* *** end of lot information *****/


                    //***********************************//
                    // Cassette Information              //
                    //***********************************//
                    // set cassette info
                    whatNextAttributes.setCassetteID(ObjectIdentifier.build(cassette.getCassetteID(), cassette.getId()));
                    whatNextAttributes.setTransferStatus(cassette.getTransferState());
                    whatNextAttributes.setTransferReserveUserID(ObjectIdentifier.build(cassette.getReserveUserID(), cassette.getReserveUserObj()));
                    whatNextAttributes.setMultiLotType(cassette.getMultiLotType());
                    whatNextAttributes.setCassetteCategory(cassette.getCassetteCategory());
                    whatNextAttributes.setInPostProcessFlagOfCassette(cassette.getPostProcessingFlag());

                    log.debug("transferStatus : {}", cassette.getTransferState());
                    if (CimStringUtils.compare(cassette.getTransferState(), "E", 1)) {
                        whatNextAttributes.setEquipmentID(ObjectIdentifier.build(cassette.getEquipmentID(), cassette.getEquipmentObj()));
                    } else {
                        whatNextAttributes.setStockerID(ObjectIdentifier.build(cassette.getEquipmentID(), cassette.getEquipmentObj()));
                    }

                    //*********************************************//
                    // Operable Flag For MultiRecipe Capability    //
                    //*********************************************//
                    whatNextAttributes.setOperableFlagForMultiRecipeCapability(true);
                    log.debug("MultiRecipeCapability : {}", _multiRecipeCapability);
                    if (BizConstant.SP_EQP_MULTIRECIPECAPABILITY_BATCH.equals(_multiRecipeCapability)) {
                        log.debug("MultiRecipeCapability == Batch");
                        if (BizConstant.SP_CAS_MULTILOTTYPE_MULTILOTMULTIRECIPE.equals(cassette.getMultiLotType())) {
                            log.debug("MultiLotType == MultiLotMultiRecipe");
                            whatNextAttributes.setOperableFlagForMultiRecipeCapability(false);
                        }
                    }
                    log.debug("operableFlagForMultiRecipeCapability : {}", whatNextAttributes.getOperableFlagForMultiRecipeCapability());
                    if (select_criteria_CANBEPROCESSED_OR_EQPMONNOKIT_OR_AUTO3) {
                        if (!whatNextAttributes.getOperableFlagForMultiRecipeCapability()) {
                            log.debug("operableFlagForMultiRecipeCapability is not TRUE");
                            return null;
                        }
                    }

                    //**************************************//
                    // select ControlJob information        //
                    //**************************************//
                    CimControlJob aLotControlJob = aLot.getControlJob();
                    if (null != aLotControlJob) {
                        whatNextAttributes.setControlJob(ObjectIdentifier.build(aLotControlJob.getIdentifier(), aLotControlJob.getPrimaryKey()));
                        CimMachine machine = aLotControlJob.getMachine();
                        assert null != machine;
                        whatNextAttributes.setProcessReserveEquipmentID(ObjectIdentifier.build(machine.getIdentifier(), machine.getPrimaryKey()));
                        CimPerson owner = aLotControlJob.getOwner();
                        assert null != owner;
                        whatNextAttributes.setProcessReserveUserID(ObjectIdentifier.build(owner.getIdentifier(), owner.getPrimaryKey()));
                        if (BizConstant.SP_DP_SELECTCRITERIA_CANBEPROCESSED.equals(selectCriteria)
                                || BizConstant.SP_DP_SELECTCRITERIA_EQPMONNOKIT.equals(selectCriteria)
                                || BizConstant.SP_DP_SELECTCRITERIA_EQPMONKIT.equals(selectCriteria)
                                || BizConstant.SP_DP_SELECTCRITERIA_SORTER.equals(selectCriteria)
                                || BizConstant.SP_DP_SELECTCRITERIA_AUTO3.equals(selectCriteria)) {
                            if (CimStringUtils.isNotEmpty(aLotControlJob.getIdentifier())) {
                                log.debug("controlJob is not nil");
                                return null;
                            }
                        }
                    }

                    //*********************************//
                    // Set sorter information          //
                    //*********************************//
                    com.fa.cim.sorter.Params.ObjSorterJobListGetDRIn in = new com.fa.cim.sorter.Params.ObjSorterJobListGetDRIn();
                    in.setLotID(whatNextAttributes.getLotID());
                    List<Info.SortJobListAttributes> objSorterJobListGetDROut = sorterMethod.sorterJobListGetDR(objCommon, in);
                    if (0 == CimArrayUtils.getSize(objSorterJobListGetDROut)) {
                        whatNextAttributes.setSorterJobExistFlag(false);
                    }
                    //
                    else {
                        if (BizConstant.SP_DP_SELECTCRITERIA_CANBEPROCESSED.equals(selectCriteria)
                                || BizConstant.SP_DP_SELECTCRITERIA_EQPMONNOKIT.equals(selectCriteria)
                                || BizConstant.SP_DP_SELECTCRITERIA_SORTER.equals(selectCriteria)
                                || BizConstant.SP_DP_SELECTCRITERIA_AUTO3.equals(selectCriteria)) {
                            log.debug("SorterJob exists.");
                            return null;
                        }
                        whatNextAttributes.setSorterJobExistFlag(true);
                    }


                    //*****************************//
                    // select PO information       //
                    //*****************************//
                    CimProcessOperationDO processOperation = cimJpaRepository.queryOne("SELECT OMPROPE.MPROCESS_PRSS_RKEY,\n" +
                            "                            OMPROPE.ROUTE_ID,\n" +
                            "                            OMPROPE.ROUTE_PRSS_RKEY,\n" +
                            "                            OMPROPE.ROUTE_NO,\n" +
                            "                            OMPROPE.ROUTE_OPE_NO,\n" +
                            "                            OMPROPE.ROUTE_PRF_RKEY,\n" +
                            "                            OMPROPE.MROUTE_PRF_RKEY,\n" +
                            "                            OMPROPE.STEP_ID,\n" +
                            "                            OMPROPE.STEP_RKEY,\n" +
                            "                            OMPROPE.PLAN_START_TIME,\n" +
                            "                            OMPROPE.PLAN_END_TIME,\n" +
                            "                            OMPROPE.PLAN_EQP_ID,\n" +
                            "                            OMPROPE.PLAN_EQP_OBJ,\n" +
                            "                            OMPROPE.REMAIN_CYCLE_TIME,\n" +
                            "                            OMPROPE.MPROCESS_PRF_RKEY,\n" +
                            "                            OMPROPE.OPE_NO,\n" +
                            "                            OMPROPE.ALLOC_LRCP_ID,\n" +
                            "                            OMPROPE.ALLOC_LRCP_RKEY,\n" +
                            "                            OMPROPE.ALLOC_MRCP_ID, \n" +
                            "                            OMPROPE.ALLOC_MRCP_RKEY,\n" +
                            "                            OMPROPE.ALLOC_PRCP_ID\n" +
                            "                     FROM   OMPROPE\n" +
                            "                     WHERE  OMPROPE.ID = ?1", CimProcessOperationDO.class, aLot.getProcessOperationObj());
                    if (null == processOperation) {
                        /*throw new ServiceException(retCodeConfig.getNotFoundProcessOperation());*/
                        log.error("WhatNext Error: Cannot found the ProcessOperation: [{}] for Lot: [{}], StepNumber: [{}]",
                                aLot.getProcessOperationObj(),
                                aLot.getIdentifier(),
                                aLot.getOperationNumber());
                        return null;
                    }
                    log.debug("Assigned LogicalRecipe at PO : {}", processOperation.getAssignLogicalRecipeID());
                    log.debug("Assigned MachineRecipe at PO : {}", processOperation.getAssignRecipeID());

                    if (CimStringUtils.isEmpty(processOperation.getAssignRecipeID())) {
                        bLotHasAssignedMRecipeSeq.put(aLot.getIdentifier(), false);
                    } else {
                        bLotHasAssignedMRecipeSeq.put(aLot.getIdentifier(), true);
                    }

                    //-------------------------------------------------------------------------------------------------
                    //  if current module PF is inactive, search current module POS and PD_ID from an active module PF
                    //-------------------------------------------------------------------------------------------------
                    //--- Check state of current module PF
                    CimProcessFlowDO processFlow = cimJpaRepository.queryOne("SELECT PRP_ID, ACTIVE_FLAG FROM OMPRF WHERE ID = ?1",
                            CimProcessFlowDO.class,
                            processOperation.getModuleProcessFlowObj());
                    if (null == processFlow) {
                        Validations.check(retCodeConfig.getNotFoundProcessFlow());
                    }
                    //--- if current module PF is inactive, get an active module POS in active current module PF
                    String posID = null;
                    if (!processFlow.getState()) {
                        String sql2 = "SELECT OMPRF_PRSSSEQ.PRSS_RKEY \n" +
                                "                FROM  OMPRF, OMPRF_PRSSSEQ\n" +
                                "                WHERE OMPRF.PRP_ID = ?\n" +
                                "                AND OMPRF.ACTIVE_FLAG = 1\n" +
                                "                AND OMPRF.ID = OMPRF_PRSSSEQ.REFKEY\n" +
                                "                AND OMPRF_PRSSSEQ.LINK_KEY = ?\n" +
                                "                AND OMPRF.PRP_LEVEL = ?";
                        Object[] obj = cimJpaRepository.queryOne(sql2, processFlow.getMainProcessDefinitionID(), processOperation.getModuleOperationNumber(), CIMStateConst.SP_PD_FLOWLEVEL_MODULE);
                        if (null != obj) {
                            posID = String.valueOf(obj[0]);
                        }
                    }

                    whatNextAttributes.setOperationID(ObjectIdentifier.build(processOperation.getProcessDefinitionID(), processOperation.getProcessDefinitionObj()));

                    Timestamp planStartTime = processOperation.getPlanStartTime();
                    if (null != planStartTime && !planStartTime.equals(CimDateUtils.initialTime())) {
                        log.debug("set planStartTimeStamp");
                        whatNextAttributes.setPlanStartTimeStamp(processOperation.getPlanStartTime());
                    }
                    Timestamp planEndTime = processOperation.getPlanEndTime();
                    if (null != planEndTime && !planEndTime.equals(CimDateUtils.initialTime())) {
                        log.debug("set planEndTimeStamp");
                        whatNextAttributes.setPlanEndTimeStamp(processOperation.getPlanEndTime());
                    }
                    whatNextAttributes.setPlannedEquipmentID(ObjectIdentifier.build(processOperation.getPlanEuipmentID(), processOperation.getPlanEuipmentObj()));
                    // *********** end PO ***************/

                    //*************************************//
                    // select previous PO information
                    //*************************************//
                    CimProcessOperation previousPO = aLot.getPreviousProcessOperation();
                    if (null != previousPO) {
                        whatNextAttributes.setPreOperationCompTimeStamp(previousPO.getActualCompTimeStamp());
                    }
                    // **** end of previous PO ****/

                    //**************************************//
                    // calculate internal priority
                    //**************************************//
                    Timestamp aCurrentTime = CimDateUtils.getCurrentTimeStamp();
                    Timestamp aPlannedCompDataTime = aLot.getPlannedCompletionDateTime();
                    Long aPlannedCompDuration = CimDateUtils.substractTimeStamp(aPlannedCompDataTime.getTime(), aCurrentTime.getTime());
                    Double aRemainingCycleTime = processOperation.getRemainCycleTime();
                    aRemainingCycleTime = CimObjectUtils.isEmpty(aRemainingCycleTime) ? 0 : aRemainingCycleTime;
                    log.debug("aPlannedCompDuration : {}", aPlannedCompDuration);
                    log.debug("aRemainingCycleTime : {}", aRemainingCycleTime);
                    if (0 == aRemainingCycleTime) {
                        log.debug("OMPROPE.REMAIN_CYCLE_TIME == 0");
                        whatNextAttributes.setInternalPriority(0);
                    } else {
                        double dInternalPriority = aPlannedCompDuration / (aRemainingCycleTime * 60 * 1000);
                        whatNextAttributes.setInternalPriority(Math.abs(dInternalPriority));
                    }
                    // *** end of internal priority ****/

                    //***********************************************//
                    // select POS information (get photo_layer)
                    //***********************************************//
                    CimPFPosListDO processFlowPosList = cimJpaRepository.queryOne("SELECT PRSS_RKEY FROM OMPRF_PRSSSEQ WHERE REFKEY = ?1 AND LINK_KEY= ?2",
                            CimPFPosListDO.class,
                            processOperation.getProcessFlowObj(),
                            processOperation.getOperationNumber());
                    if (null != processFlowPosList) {
                        posID = processFlowPosList.getProcessOperationSpecificationsObj();
                    }
                    String posPhotoLayer = null;
                    CimProcessOperationSpecificationDO pos = cimJpaRepository.queryOne("SELECT PHOTO_LAYER FROM OMPRSS WHERE OMPRSS.ID = ?1",
                            CimProcessOperationSpecificationDO.class,
                            posID);
                    if (null != pos) {
                        posPhotoLayer = pos.getPhotoLayer();
                    }

                    //---------------------------------------------------
                    // select Module POS information (get mandatory_flag)
                    //---------------------------------------------------
                    Boolean mandatoryFlag = false;
                    CimProcessOperationSpecificationDO modulePos = cimJpaRepository.queryOne("SELECT COMPULSORY_FLAG FROM OMPRSS WHERE  OMPRSS.ID = ?1",
                            CimProcessOperationSpecificationDO.class,
                            processOperation.getModuleProcessOperationSpecificationsObj());
                    if (null != modulePos) {
                        mandatoryFlag = modulePos.getMandatoryFlag();
                    }
                    whatNextAttributes.setMandatoryOperationFlag(mandatoryFlag);

                    //------------------------------------------------------------------------------
                    // select Main PF information (get stage_id, stage_obj, current seqno in mainPF)
                    //------------------------------------------------------------------------------
                    CimPFDefinitionListDO mianPfPdList = cimJpaRepository.queryOne("SELECT IDX_NO, STAGE_ID, STAGE_RKEY\n" +
                            "                     FROM   OMPRF_ROUTESEQ\n" +
                            "                     WHERE  REFKEY = ?1 \n" +
                            "                     AND    LINK_KEY = ?2", CimPFDefinitionListDO.class, processOperation.getMainProcessFlowObj(), processOperation.getModuleNumber());
                    if (null != mianPfPdList) {
                        whatNextAttributes.setStageID(ObjectIdentifier.build(mianPfPdList.getStageID(), mianPfPdList.getStageObj()));
                    }


                    //-----------------------------------------------------
                    // check lot's routeID and operation NO and productID
                    //-----------------------------------------------------
                    boolean bEntityInhibitInfoCollected = false;
                    boolean bNextOperationInfoRetrieved = false;
                    boolean bondingInfoRetrieved = false;
                    String samePropertiesCheckKey = String.format("%s.%s.%s.%s",
                            whatNextAttributes.getRouteID().getValue(),
                            whatNextAttributes.getOperationNumber(),
                            whatNextAttributes.getProductID().getValue(),
                            whatNextAttributes.getOperationID().getValue());
                    Infos.WhatNextAttributes sameAttributes = samePropertiesForLot.get(samePropertiesCheckKey);
                    if (null != sameAttributes) {
                        log.debug("Same Type Lot Found");
                        if (!bNextOperationInfoRetrieved) {
                            log.debug("First Found");
                            whatNextAttributes.setNext2EquipmentID(sameAttributes.getNext2EquipmentID());
                            whatNextAttributes.setNext2LogicalRecipeID(sameAttributes.getNext2LogicalRecipeID());
                            whatNextAttributes.setNext2requiredCassetteCategory(sameAttributes.getNext2requiredCassetteCategory());
                            bNextOperationInfoRetrieved = true;
                        }

                        if (!bondingInfoRetrieved) {
                            log.debug("First Found");
                            whatNextAttributes.setBondingCategory(sameAttributes.getBondingCategory());
                            whatNextAttributes.setTopProductID(sameAttributes.getTopProductID());
                            bondingInfoRetrieved = true;
                        }

                        if (CimStringUtils.equals(subLotTypeSeqForCheck.get(samePropertiesCheckKey), aLot.getSubLotType())
                                && CimStringUtils.equals(reticleSetSeqForCheck.get(samePropertiesCheckKey), reticleSetID)
                                && CimBooleanUtils.isFalse(FPCAppliedForCheck.get(samePropertiesCheckKey))
                                && CimBooleanUtils.isFalse(fPCApplied.get(aLot.getIdentifier()))
                                && CimBooleanUtils.isTrue(inhibitCollectedBeforeContinue.get(samePropertiesCheckKey))) {
                            log.debug("SubLotType & ReticleSet Matching");
                            log.debug("Found Same Entity Inhibit Information");

                            Inputs.ObjEntityInhibiteffectiveForLotGetDRIn strEntityInhibitEffectiveForLotGetDRIn = new Inputs.ObjEntityInhibiteffectiveForLotGetDRIn();
                            strEntityInhibitEffectiveForLotGetDRIn.setLotID(tmpLotID);
                            strEntityInhibitEffectiveForLotGetDRIn.setStrEntityInhibitInfos(entityInhibiteffectiveForLotGetDRIns.get(samePropertiesCheckKey).getStrEntityInhibitInfos());

                            List<Infos.EntityInhibitInfo> strEntityInhibitEffectiveForLotGetDROut = constraintMethod.constraintEffectiveForLotGetDR(objCommon,
                                    strEntityInhibitEffectiveForLotGetDRIn.getStrEntityInhibitInfos(),
                                    strEntityInhibitEffectiveForLotGetDRIn.getLotID());
                            List<Infos.EntityInhibitAttributes> entityInhibitAttributes = new ArrayList<>();
                            strEntityInhibitEffectiveForLotGetDROut.forEach(entityInhibitInfo -> {
                                Infos.EntityInhibitAttributes entityInhibitAttribute = entityInhibitInfo.getEntityInhibitAttributes();
                                entityInhibitAttributes.add(entityInhibitAttribute);
                            });
                            whatNextAttributes.setEntityInhibitions(entityInhibitAttributes);
                            bEntityInhibitInfoCollected = true;
                        }
                    }

                    //-------------------------------------
                    // select next 2 EQP information
                    //-------------------------------------
                    // todo: 该查询逻辑比较消耗性能，还可以优化
                    if (!bNextOperationInfoRetrieved) {
                        log.debug("bNextOperationInfoRetrieved == false");
                        boolean searchDirection = true;
                        boolean posSearchFlag = true;
                        int searchCount = 1;
                        boolean currentFlag = false;

                        Inputs.ObjProcessOperationProcessRefListForLotIn refListForLotIn = new Inputs.ObjProcessOperationProcessRefListForLotIn();
                        refListForLotIn.setSearchDirection(searchDirection);
                        refListForLotIn.setPosSearchFlag(posSearchFlag);
                        refListForLotIn.setSearchCount(searchCount);
                        refListForLotIn.setCurrentFlag(currentFlag);
                        refListForLotIn.setLotID(tmpLotID);
                        List<Infos.OperationProcessRefListAttributes> refListForLotRepeatRetCode = processMethod.processOperationProcessRefListForLot(objCommon, refListForLotIn);

                        String tmpPosID = null;
                        String tmpModulePosID = null;
                        if (CimArrayUtils.isNotEmpty(refListForLotRepeatRetCode)) {
                            tmpPosID = refListForLotRepeatRetCode.get(0).getProcessRef().getProcessOperationSpecification();
                            tmpModulePosID = refListForLotRepeatRetCode.get(0).getProcessRef().getModulePOS();
                        }
                        //--- Get main POS information ( get REQD_CAST_CTGRY )
                        CimProcessOperationSpecificationDO tmpMainPos = cimJpaRepository.queryOne("SELECT CARRIER_CATEGORY\n" +
                                "                         FROM   OMPRSS\n" +
                                "                         WHERE  ID = ?1", CimProcessOperationSpecificationDO.class, tmpPosID);
                        String requiredCassetteCategory = null;
                        if (null != tmpMainPos) {
                            requiredCassetteCategory = tmpMainPos.getRequiredCassetteCategory();
                        }
                        //--- Get module POS information ( get PD_OBJ )
                        CimProcessOperationSpecificationDO tmpModulePos = cimJpaRepository.queryOne("SELECT STEP_RKEY\n" +
                                "                         FROM   OMPRSS\n" +
                                "                         WHERE  OMPRSS.ID = ?1", CimProcessOperationSpecificationDO.class, tmpModulePosID);
                        String pdRefKey = null;
                        if (null != tmpModulePos) {
                            pdRefKey = tmpModulePos.getProcessDefinitionObj();
                        }

                        // TODO: 2019/10/24. 3488 - 3506 if condition is missing.
                        // todo: 逻辑还没有写完
                        //-----------------------------------------------------
                        // Provide Specific EQP by Product Group and Technology
                        //-----------------------------------------------------

                        //--------------------------------------------------------------------
                        //Search Logic of OMPRODINFO by Product Spec ID for get Product Group
                        //--------------------------------------------------------------------
                        CimProductSpecificationDO productSpecification = cimJpaRepository.queryOne("SELECT * " +
                                "                             FROM   OMPRODINFO\n" +
                                "                             WHERE  OMPRODINFO.PROD_ID = ?1", CimProductSpecificationDO.class, aLot.getProductSpecificationID().getValue());

                        CimProcessDefinitionDO processDefinition = cimJpaRepository.queryOne("SELECT * " +
                                "                             FROM   OMPRP\n" +
                                "                             WHERE  OMPRP.ID = ?1", CimProcessDefinitionDO.class, pdRefKey);

                        if (null != processDefinition && null != productSpecification) {
                            List<CimEquipmentDO> equipments;
                            CimLogicalRecipeDO logicalRecipe;
                            List<CimEquipmentDO> outEquipments = processMethod.getEquipmentsByprocessDefinitionAndEquipment(processDefinition, productSpecification);
                            if (CimArrayUtils.isNotEmpty(outEquipments)) {
                                equipments = outEquipments;
                                for (CimEquipmentDO tmpEquipment : equipments) {
                                    whatNextAttributes.setNext2EquipmentID(ObjectIdentifier.build(tmpEquipment.getEquipmentID(), tmpEquipment.getId()));
                                    whatNextAttributes.setNext2requiredCassetteCategory(requiredCassetteCategory);
                                }
                            } else {
                                logicalRecipe = this.whatNextProcessDefinitionInfo(processDefinition, productSpecification);
                                if (null != logicalRecipe) {
                                    List<CimEquipmentDO> logicalRecepeSpecificRecipeSettingCode = processMethod.getAllMachinesBySpecificRecipeSetting(logicalRecipe);
                                    if (CimArrayUtils.isNotEmpty(logicalRecepeSpecificRecipeSettingCode)) {
                                        equipments = logicalRecepeSpecificRecipeSettingCode;
                                        for (CimEquipmentDO tmpEquipment : equipments) {
                                            whatNextAttributes.setNext2EquipmentID(ObjectIdentifier.build(tmpEquipment.getEquipmentID(), tmpEquipment.getId()));
                                            whatNextAttributes.setNext2requiredCassetteCategory(requiredCassetteCategory);
                                            whatNextAttributes.setNext2LogicalRecipeID(ObjectIdentifier.build(logicalRecipe.getLogicalRecipeID(), logicalRecipe.getId()));
                                        }
                                    } else {
                                        List<CimMachineRecipeDO> machineRecipes = processMethod.getAllMachineRecipe(logicalRecipe);
                                        if (CimArrayUtils.isNotEmpty(machineRecipes)) {
                                            for (CimMachineRecipeDO mRecipe : machineRecipes) {
                                                List<CimEquipmentDO> eqpListByRecipe = processMethod.getAllMachines(mRecipe);

                                                for (CimEquipmentDO tmpEquipment : eqpListByRecipe) {
                                                    whatNextAttributes.setNext2EquipmentID(ObjectIdentifier.build(tmpEquipment.getEquipmentID(), tmpEquipment.getId()));
                                                    whatNextAttributes.setNext2requiredCassetteCategory(requiredCassetteCategory);
                                                    whatNextAttributes.setNext2LogicalRecipeID(ObjectIdentifier.build(logicalRecipe.getLogicalRecipeID(), logicalRecipe.getId()));
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    //-------------------------------------
                    // select qTime information
                    //-------------------------------------
                    // Max Q-Time
                    List<Infos.LotQtimeInfo> objLotQtimeGetForRouteDROut = lotMethod.lotQtimeGetForRouteDR(objCommon,
                            tmpLotID,
                            processOperation.getMainProcessFlowObj(),
                            processOperation.getModuleProcessFlowObj(),
                            processOperation.getOperationNumber());
                    List<Infos.LotQtimeInfo> lotQTimeInfos = new ArrayList<>();
                    objLotQtimeGetForRouteDROut.forEach(qTimeInfo -> {
                        if (!CimDateUtils.convertTo(qTimeInfo.getQrestrictionTargetTimeStamp()).equals(CimDateUtils.initialTime())) {
                            lotQTimeInfos.add(qTimeInfo);
                        }
                    });
                    whatNextAttributes.setStrLotQtimeInfo(lotQTimeInfos);

                    // Min Q-Time
                    List<Infos.LotQtimeInfo> minQTimeInfos = minQTimeMethod.getRestrictInProcessArea(tmpLotID,
                            processOperation.getMainProcessFlowObj());
                    whatNextAttributes.setMinQTimeInfos(minQTimeInfos);

                    //--------------------------------------------------------
                    //  Select QTime restriction for Lot on Branch Route.
                    //--------------------------------------------------------
                    List<CimProcessFlowContextReturnDO> processFlowContextReturnDOS = cimJpaRepository.query(
                            "SELECT MROUTE_PRF_RKEY, OPE_NO, ROUTE_PRF_RKEY FROM OMPRFCX_RTNSEQ WHERE REFKEY = ?1",
                            CimProcessFlowContextReturnDO.class,
                            aLot.getProcessFlowContext().getPrimaryKey());
                    for (CimProcessFlowContextReturnDO processFlowContextReturnDO : processFlowContextReturnDOS) {
                        List<Infos.LotQtimeInfo> strLotQtimeGetForRouteDROut;
                        try {
                            strLotQtimeGetForRouteDROut = lotMethod.lotQtimeGetForRouteDR(objCommon,
                                    tmpLotID,
                                    processFlowContextReturnDO.getMainProcessFlowObj(),
                                    processFlowContextReturnDO.getModuleProcessFlowObj(),
                                    processFlowContextReturnDO.getOperationNumber());
                        } catch (ServiceException e) {
                            log.error("lotMethod.lotQtimeGetForRouteDR() != ok");
                            break;
                        }
                        strLotQtimeGetForRouteDROut.stream().filter(qTimeInfo ->
                                !CimDateUtils.convertTo(qTimeInfo.getQrestrictionTargetTimeStamp())
                                        .equals(CimDateUtils.initialTime())).forEach(lotQTimeInfos::add);
                        // 这里会存在重复添加的BUG, 应该删除
                        // whatNextAttributes.getStrLotQtimeInfo().addAll(lotQTimeInfos);

                        minQTimeInfos.addAll(minQTimeMethod.getRestrictInProcessArea(tmpLotID,
                                processFlowContextReturnDO.getMainProcessFlowObj()));
                    }
                    whatNextAttributes.setQtimeFlag(CimArrayUtils.isNotEmpty(whatNextAttributes.getStrLotQtimeInfo()));
                    whatNextAttributes.setMinQTimeFlag(CimArrayUtils.isNotEmpty(minQTimeInfos));

                    //-------------------------------------
                    // select PD information
                    //-------------------------------------
                    CimProductSpecificationDO productSpecification = cimJpaRepository.queryOne(
                            "SELECT * FROM OMPRODINFO WHERE PROD_ID = ?1",
                            CimProductSpecificationDO.class,
                            ObjectIdentifier.fetchValue(aLot.getProductSpecificationID()));
                    CimProcessDefinitionDO processDefinition = cimJpaRepository.queryOne(
                            "SELECT * FROM OMPRP WHERE ID = ?1 ",
                            CimProcessDefinitionDO.class,
                            processOperation.getProcessDefinitionObj());

                    //---------------------------------------------------------------
                    // Search Recipe info
                    //---------------------------------------------------------------
                    CimLogicalRecipeDO logicalRecipe = this.whatNextProcessDefinitionInfo(processDefinition, productSpecification);
                    whatNextAttributes.setInspectionType(processDefinition.getInspectionType());

                    //--------------------------
                    //Lot Reservation Check
                    //--------------------------
                    if (!ObjectIdentifier.isEmptyWithValue(aLot.getControlJobID())) {
                        log.debug("Lot was Reserved. So set Assigned LogicalRecipe From PO : {}", aLot.getControlJobID().getValue());
                        whatNextAttributes.setLogicalRecipeID(ObjectIdentifier.build(processOperation.getAssignLogicalRecipeID(), processOperation.getAssignLogicalRecipeObj()));
                    } else {
                        whatNextAttributes.setLogicalRecipeID(ObjectIdentifier.build(logicalRecipe.getLogicalRecipeID(), logicalRecipe.getId()));
                    }
                    log.debug("Logical Recipe and Object : {}", whatNextAttributes.getLogicalRecipeID());

                    //********************************//
                    // Bonding Group info             //
                    //********************************//
                    if (!bondingInfoRetrieved) {
                        ObjectIdentifier bondingTargetOperationID = null;
                        if (bondingEqpFlag) {
                            bondingTargetOperationID = whatNextAttributes.getOperationID();
                        } else if (isLotInBondingFlow) {
                            //--------------------------------------
                            // Get Target Operation of Bonding Flow.
                            //--------------------------------------
                            Outputs.ObjLotBondingOperationInfoGetDROut lotBondingOperationInfoGetDROut = lotMethod.lotBondingOperationInfoGetDR(objCommon, whatNextAttributes.getLotID());
                            if (null != lotBondingOperationInfoGetDROut) {
                                bondingTargetOperationID = lotBondingOperationInfoGetDROut.getTargetOperationID();
                            }
                        }

                        if (!ObjectIdentifier.isEmptyWithValue(bondingTargetOperationID)) {
                            //--------------------------------------------
                            // Check if the Lot is on Bonding Operation
                            //--------------------------------------------
                            List<Infos.BOMPartsInfo> bomPartsInfos = null;
                            try {
                                bomPartsInfos = processMethod.processBOMPartsInfoGetDR(objCommon, whatNextAttributes.getProductID(), bondingTargetOperationID);
                            } catch (ServiceException e) {
                                if (!Validations.isEquals(e.getCode(), retCodeConfig.getBomNotDefined()) && !Validations.isEquals(e.getCode(), retCodeConfigEx.getPartsNotDefinedForProcess())) {
                                    throw e;
                                }
                            }
                            if (CimArrayUtils.isEmpty(bomPartsInfos)) {
                                whatNextAttributes.setBondingCategory(BizConstant.SP_LOT_BONDINGCATEGORY_TOP);
                            } else if (CimArrayUtils.getSize(bomPartsInfos) == 1) {
                                whatNextAttributes.setBondingCategory(BizConstant.SP_LOT_BONDINGCATEGORY_BASE);
                                whatNextAttributes.setTopProductID(bomPartsInfos.get(0).getPartID().getValue());
                            }
                        }
                    }


                    // todo: tmpWhatNextAttributeForCheck 考虑使用 ConcurrentHashMap 数据结构来解决此问题
                    Infos.WhatNextAttributes tmpWhatNextAttributesForCheck = new Infos.WhatNextAttributes();
                    tmpWhatNextAttributesForCheck.setRouteID(whatNextAttributes.getRouteID());
                    tmpWhatNextAttributesForCheck.setOperationNumber(whatNextAttributes.getOperationNumber());
                    tmpWhatNextAttributesForCheck.setProductID(whatNextAttributes.getProductID());
                    tmpWhatNextAttributesForCheck.setOperationID(whatNextAttributes.getOperationID());
                    tmpWhatNextAttributesForCheck.setControlJob(whatNextAttributes.getControlJob());
                    tmpWhatNextAttributesForCheck.setNext2EquipmentID(whatNextAttributes.getNext2EquipmentID());
                    tmpWhatNextAttributesForCheck.setNext2requiredCassetteCategory(whatNextAttributes.getNext2requiredCassetteCategory());
                    tmpWhatNextAttributesForCheck.setBondingCategory(whatNextAttributes.getBondingCategory());
                    tmpWhatNextAttributesForCheck.setTopProductID(whatNextAttributes.getTopProductID());
                    tmpWhatNextAttributesForCheck.setNext2LogicalRecipeID(whatNextAttributes.getNext2LogicalRecipeID());
                    samePropertiesForLot.put(samePropertiesCheckKey, tmpWhatNextAttributesForCheck);

                    // todo: 以下逻辑需要check
                    if (CimStringUtils.isNotEmpty(aLot.getSubLotType())) {
                        subLotTypeSeqForCheck.put(samePropertiesCheckKey, aLot.getSubLotType());
                    }
                    if (CimStringUtils.isNotEmpty(reticleSetID)) {
                        reticleSetSeqForCheck.put(samePropertiesCheckKey, reticleSetID);
                    }
                    FPCAppliedForCheck.put(samePropertiesCheckKey, CimBooleanUtils.isTrue(fPCApplied.get(aLot.getIdentifier())));
                    //entityInhibiteffectiveForLotGetDRIns.put(samePropertiesCheckKey, null);
                    //inhibitCollectedBeforeContinue.put(samePropertiesCheckKey, false);

                    //**********************************************//
                    // Collect reticle/reticleGroup information
                    //**********************************************//
                    // todo: 该业务逻辑还可以优化
                    int InhibitReticleIDLen;
                    List<Infos.FoundReticle> reticleSeq = new ArrayList<>();
                    if (!strLotEffectiveFPCInfoGetOut.isReticleActionRequiredFlag()) {
                        log.debug("FPCInfo does not change reticles.");
                        if (isEqpReticleReq && CimStringUtils.isNotEmpty(posPhotoLayer)) {
                            //--------------------------------------------------------------------------
                            // Set reticle group sequence from reticle set
                            //--------------------------------------------------------------------------
                            List<ObjectIdentifier> reticleGroupIDs = new ArrayList<>();
                            //--------------------------------------------------------------
                            // Get FRPEC.prodspec_id
                            //--------------------------------------------------------------
                            String reticalSetID = aLot.getReticleSet() == null ? "" : aLot.getReticleSet().getIdentifier();
                            log.debug("OMLOT.rtclset_id : {}", reticalSetID);
                            CimReticleSetDO cimReticleSetExample = new CimReticleSetDO();
                            cimReticleSetExample.setReticleSetID(reticalSetID);
                            CimReticleSetDO reticleSetDO = cimJpaRepository.findOne(Example.of(cimReticleSetExample)).orElse(null);

                            if (null != reticleSetDO) {
                                int reticleDfSequenceNumber;
                                boolean reticleDFOverrideFlag;
                                CimReticleSetDefinitionDO cimReticleSetDefinitionExample = new CimReticleSetDefinitionDO();
                                cimReticleSetDefinitionExample.setReferenceKey(reticleSetDO.getId());
                                cimReticleSetDefinitionExample.setPhotoLayer(posPhotoLayer);
                                CimReticleSetDefinitionDO reticleSetDefinitionDO = cimJpaRepository.findOne(Example.of(cimReticleSetDefinitionExample)).orElse(null);

                                reticleDfSequenceNumber = reticleSetDefinitionDO.getSequenceNumber();
                                reticleDFOverrideFlag = reticleSetDefinitionDO.getOverrideFlag();
                                //---------------------------------------------------------------------------
                                // Search Specific Reticle Group when override flag is TRUE.
                                //---------------------------------------------------------------------------
                                if (reticleDFOverrideFlag) {
                                    boolean foundFlag = false;
                                    //---------------------------------------------------------------------------
                                    // Check product ID + equipment ID
                                    //---------------------------------------------------------------------------
                                    CimReticleSetSpecificationDO cimReticleSetSpecExam = new CimReticleSetSpecificationDO();
                                    cimReticleSetSpecExam.setReferenceKey(reticleSetDO.getId());
                                    cimReticleSetSpecExam.setEquipmentID(ObjectIdentifier.fetchValue(objEquipmentLotsWhatNextDRIn.getEquipmentID()));
                                    cimReticleSetSpecExam.setPhotoLayer(posPhotoLayer);
                                    cimReticleSetSpecExam.setProductSpecificationID(ObjectIdentifier.fetchValue(aLot.getProductSpecificationID()));
                                    CimReticleSetSpecificationDO reticleGroupDataDO = cimJpaRepository.findOne(Example.of(cimReticleSetSpecExam)).orElse(null);

                                    if (null != reticleGroupDataDO) {
                                        foundFlag = true;
                                    }
                                    //---------------------------------------------------------------------------
                                    //  Check product ID + equipment ID "*"
                                    //---------------------------------------------------------------------------
                                    if (!foundFlag) {
                                        CimReticleSetSpecificationDO cimReticleSetSpecExam1 = new CimReticleSetSpecificationDO();
                                        cimReticleSetSpecExam1.setReferenceKey(reticleSetDO.getId());
                                        cimReticleSetSpecExam1.setEquipmentID("*");
                                        cimReticleSetSpecExam1.setPhotoLayer(posPhotoLayer);
                                        cimReticleSetSpecExam1.setProductSpecificationID(ObjectIdentifier.fetchValue(aLot.getProductSpecificationID()));
                                        reticleGroupDataDO = cimJpaRepository.findOne(Example.of(cimReticleSetSpecExam1)).orElse(null);

                                        if (null != reticleGroupDataDO) {
                                            foundFlag = true;
                                        }
                                        //---------------------------------------------------------------------------
                                        //  Check product ID "*" + equipment ID
                                        //---------------------------------------------------------------------------
                                        if (!foundFlag){
                                            CimReticleSetSpecificationDO cimReticleSetSpecExam2 = new CimReticleSetSpecificationDO();
                                            cimReticleSetSpecExam2.setReferenceKey(reticleSetDO.getId());
                                            cimReticleSetSpecExam2.setEquipmentID(ObjectIdentifier.fetchValue(objEquipmentLotsWhatNextDRIn.getEquipmentID()));
                                            cimReticleSetSpecExam2.setPhotoLayer(posPhotoLayer);
                                            cimReticleSetSpecExam2.setProductSpecificationID("*");
                                            reticleGroupDataDO = cimJpaRepository.findOne(Example.of(cimReticleSetSpecExam2)).orElse(null);

                                            if (null != reticleGroupDataDO) {
                                                foundFlag = true;
                                            }
                                        }
                                    }
                                    //-------------------------------------------------------------------------------
                                    // Set objectIdentifier of specific reticle groups when any record is found.
                                    //-------------------------------------------------------------------------------
                                    if (foundFlag) {
                                        CimReticleSetSpecificGroupDO cimReticleSetSpecificGroupExam = new CimReticleSetSpecificGroupDO();
                                        cimReticleSetSpecificGroupExam.setReferenceKey(reticleSetDO.getId());
                                        cimReticleSetSpecificGroupExam.setTheTableMarker(String.valueOf(reticleGroupDataDO.getSequenceNumber()));
                                        List<CimReticleSetSpecificGroupDO> reticleSetSpecificGroupDOS = cimJpaRepository.findAll(Example.of(cimReticleSetSpecificGroupExam));

                                        for (CimReticleSetSpecificGroupDO reticleSetSpecificGroupDO : reticleSetSpecificGroupDOS) {
                                            reticleGroupIDs.add(ObjectIdentifier.build(reticleSetSpecificGroupDO.getIdentifier(), reticleSetSpecificGroupDO.getObjectRef()));
                                        }
                                    }
                                }
                                //-----------------------------------------------------------------------------------
                                // Set default reticle groups, when override flag is false, or any specific reticle
                                // group is not found.
                                //-----------------------------------------------------------------------------------
                                if (CimArrayUtils.isEmpty(reticleGroupIDs)) {
                                    CimReticleSetDFReticleGroupDO cimReticleSetDFRtclGrpExam = new CimReticleSetDFReticleGroupDO();
                                    cimReticleSetDFRtclGrpExam.setReferenceKey(reticleSetDO.getId());
                                    cimReticleSetDFRtclGrpExam.setTheTableMarker(String.valueOf(reticleDfSequenceNumber));
                                    List<CimReticleSetDFReticleGroupDO> reticleSetDFReticleGroupDOS = cimJpaRepository.findAll(Example.of(cimReticleSetDFRtclGrpExam));
                                    for (CimReticleSetDFReticleGroupDO reticleSetDFReticleGroupDO : reticleSetDFReticleGroupDOS) {
                                        reticleGroupIDs.add(ObjectIdentifier.build(reticleSetDFReticleGroupDO.getIdentifier(), reticleSetDFReticleGroupDO.getObjectRef()));
                                    }
                                }
                            }

                            whatNextAttributes.setReticleGroupIDs(reticleGroupIDs);
                            //-----------------------------
                            // Check Reticle availability
                            //-----------------------------
                            whatNextAttributes.setReticleExistFlag(false);
                            boolean bReticleIsAvailable = false;
                            boolean bReticleCheckFlag = false;
                            int nCandidateReticleGroupLen = CimArrayUtils.getSize(reticleGroupIDs);
                            if (nCandidateReticleGroupLen > 0) {
                                log.debug("nCandidateReticleGroupLen > 0");
                                if (isEqpReticleReq) {
                                    log.debug("OMEQP.RTCL_NEED_FLAG == TRUE");
                                    bReticleCheckFlag = true;
                                } else {
                                    log.debug("OMEQP.RTCL_NEED_FLAG == false");
                                    bReticleIsAvailable = true;
                                }
                            }
                            if (bReticleCheckFlag) {
                                if (!bReticleGroupListQueryDone.get()) {
                                    String drblSql = "SELECT OMPDRBL_PDRBLGRP.PDRBL_GRP_ID,\n" +
                                            "       OMPDRBL_PDRBLGRP.PDRBL_GRP_RKEY,\n" +
                                            "       OMPDRBL.PDRBL_ID,\n" +
                                            "       OMPDRBL.PDRBL_SUB_STATE_ID,\n" +
                                            "       OMPDRBL.PDRBL_SUB_STATE_RKEY\n" +
                                            "  FROM OMPDRBL, OMPDRBL_PDRBLGRP\n" +
                                            " WHERE OMPDRBL.EQP_ID = ?1 \n" +
                                            "   AND OMPDRBL.XFER_STATE = ?2 \n" +
                                            "   AND OMPDRBL.ID = OMPDRBL_PDRBLGRP.REFKEY";
                                    List<Object[]> drblQuery = cimJpaRepository.query(drblSql, ObjectIdentifier.fetchValue(equipmentID),
                                            BizConstant.SP_TRANSSTATE_EQUIPMENTIN);
                                    for (Object[] objects : drblQuery) {
                                        String durableGroupID = String.valueOf(objects[0]);
                                        String durableGroupOBJ = String.valueOf(objects[1]);
                                        String durableID = String.valueOf(objects[2]);
                                        String durableStateID = String.valueOf(objects[3]);
                                        String durableStateOBJ = String.valueOf(objects[4]);

                                        Infos.FoundReticle strReticleInfo = new Infos.FoundReticle();
                                        strReticleInfo.setReticleID(ObjectIdentifier.buildWithValue(durableID));
                                        strReticleInfo.setReticleGroupID(ObjectIdentifier.build(durableGroupID, durableGroupOBJ));

                                        Infos.ReticleStatusInfo reticleStatusInfo = new Infos.ReticleStatusInfo();
                                        reticleStatusInfo.setReticleStatus(durableStateID);
                                        strReticleInfo.setReticleStatusInfo(reticleStatusInfo);
                                        eqpInReticleSeq.add(strReticleInfo);
                                    }
                                    bReticleGroupListQueryDone.compareAndSet(false, true);
                                }
                                int eqpInReticleCnt = CimArrayUtils.getSize(eqpInReticleSeq);
                                for (int j = 0; j < nCandidateReticleGroupLen; j++) {
                                    bReticleIsAvailable = false;
                                    for (int k = 0; k < eqpInReticleCnt; k++) {
                                        if (ObjectIdentifier.equalsWithValue(whatNextAttributes.getReticleGroupIDs().get(j), eqpInReticleSeq.get(k).getReticleGroupID())) {
                                            String durableSubStatus = ObjectIdentifier.fetchValue(eqpInReticleSeq.get(k).getReticleStatusInfo().getDurableSubStatus());
                                            boolean restrictionFlag = false;
                                            if (CimStringUtils.isNotEmpty(durableSubStatus)) {
                                                log.debug("durableSubStatus {}", durableSubStatus);
                                                Infos.DurableSubStatusInfo strDurableSubStatusInfo = null;
                                                if (!durableSubStatusList.containsKey(durableSubStatus)) {
                                                    Inputs.ObjDurableSubStateDBInfoGetDRIn durableSubStateDBInfoGetDRIn = new Inputs.ObjDurableSubStateDBInfoGetDRIn();
                                                    durableSubStateDBInfoGetDRIn.setDurableSubStatus(durableSubStatus);
                                                    durableSubStateDBInfoGetDRIn.setAvailableSubLotTypeInfoFlag(true);
                                                    durableSubStateDBInfoGetDRIn.setNextTransitionDurableSubStatusInfoFlag(false);
                                                    strDurableSubStatusInfo = durableMethod.durableSubStateDBInfoGetDR(objCommon, durableSubStateDBInfoGetDRIn);
                                                    durableSubStatusList.put(durableSubStatus, strDurableSubStatusInfo);
                                                }
                                                if (null != strDurableSubStatusInfo && strDurableSubStatusInfo.getConditionalAvailableFlag()) {
                                                    log.debug("conditionalAvailableFlag == TRUE");
                                                    restrictionFlag = true;
                                                    int subLotTypeNum = CimArrayUtils.getSize(strDurableSubStatusInfo.getAvailableSubLotTypes());
                                                    for (int l = 0; l < subLotTypeNum; l++) {
                                                        if (CimStringUtils.equals(aLot.getSubLotType(), strDurableSubStatusInfo.getAvailableSubLotTypes().get(l))) {
                                                            log.debug("Exist SubLotType :{}", aLot.getSubLotType());
                                                            restrictionFlag = false;
                                                            break;
                                                        }
                                                    }
                                                }
                                            }
                                            if (!restrictionFlag) {
                                                log.debug("restrictionFlag is false");
                                                bReticleIsAvailable = true;
                                                break;
                                            }
                                        }
                                    }
                                    if (!bReticleIsAvailable) {
                                        log.debug("bReticleIsAvailable == false");
                                        break;
                                    }
                                }
                            }
                            if (bReticleIsAvailable) {
                                log.debug("reticleExistFlag == TRUE");
                                whatNextAttributes.setReticleExistFlag(true);
                            } else {
                                log.debug("reticleExistFlag == false");
                                whatNextAttributes.setReticleExistFlag(false);
                            }
                            if (select_criteria_CANBEPROCESSED_OR_EQPMONNOKIT_OR_AUTO3) {
                                if (bReticleCheckFlag && !whatNextAttributes.getReticleExistFlag()) {
                                    log.debug("reticleExistFlag is not TRUE");
                                    return null;
                                }
                            }
                        } else {
                            whatNextAttributes.setReticleExistFlag(false);
                        }
                    }
                    // FPCInfo changes reticle.
                    else {
                        int fpcReticleCount = CimArrayUtils.getSize(strLotEffectiveFPCInfoGetOut.getFpcInfo().getReticleInfoList());
                        List<ObjectIdentifier> reticleGroupIDs = new ArrayList<>();
                        for (int j = 0; j < fpcReticleCount; j++) {
                            Infos.ReticleInfo reticleInfo = strLotEffectiveFPCInfoGetOut.getFpcInfo().getReticleInfoList().get(j);
                            reticleGroupIDs.add(reticleInfo.getReticleGroup());
                        }
                        whatNextAttributes.setReticleExistFlag(true);
                        whatNextAttributes.setReticleGroupIDs(reticleGroupIDs);
                        ObjectIdentifier drblID;
                        for (int j = 0; j < fpcReticleCount; j++) {
                            Infos.ReticleInfo reticleInfo = strLotEffectiveFPCInfoGetOut.getFpcInfo().getReticleInfoList().get(j);
                            drblID = reticleInfo.getReticleID();
                            String drblSql = " SELECT A.EQP_ID,\n" +
                                    "        A.XFER_STATE,\n" +
                                    "        A.PDRBL_STATE,\n" +
                                    "        A.PDRBL_SUB_STATE_ID,\n" +
                                    "        B.PDRBL_GRP_ID,\n" +
                                    "        B.PDRBL_GRP_RKEY\n" +
                                    "   FROM OMPDRBL A, OMPDRBL_PDRBLGRP B\n" +
                                    "  WHERE A.PDRBL_ID = ?1 \n" +
                                    "    AND A.ID = B.REFKEY\n";
                            List<Object[]> drblQuery = cimJpaRepository.query(drblSql, ObjectIdentifier.fetchValue(drblID));
                            String drblEqpID = null;
                            String drblState = null;
                            String drblTranState = null;
                            String drblSubState = null;
                            String drblgrpID = null;
                            String drblgrpOBJ = null;
                            if (CimArrayUtils.isNotEmpty(drblQuery)) {
                                for (Object[] objects : drblQuery) {
                                    drblEqpID = String.valueOf(objects[0]);
                                    drblState = String.valueOf(objects[1]);
                                    drblTranState = String.valueOf(objects[2]);
                                    drblSubState = String.valueOf(objects[3]);
                                    drblgrpID = String.valueOf(objects[4]);
                                    drblgrpOBJ = String.valueOf(objects[5]);
                                }
                            }
                            boolean restrictionFlag = false;
                            if (CimStringUtils.isNotEmpty(drblSubState)) {
                                Infos.DurableSubStatusInfo strDurableSubStatusInfo = null;
                                if (!durableSubStatusList.containsKey(drblSubState)) {
                                    Inputs.ObjDurableSubStateDBInfoGetDRIn durableSubStateDBInfoGetDRIn = new Inputs.ObjDurableSubStateDBInfoGetDRIn();
                                    durableSubStateDBInfoGetDRIn.setDurableSubStatus(drblSubState);
                                    durableSubStateDBInfoGetDRIn.setAvailableSubLotTypeInfoFlag(true);
                                    durableSubStateDBInfoGetDRIn.setNextTransitionDurableSubStatusInfoFlag(false);
                                    strDurableSubStatusInfo = durableMethod.durableSubStateDBInfoGetDR(objCommon, durableSubStateDBInfoGetDRIn);
                                    durableSubStatusList.put(drblSubState, strDurableSubStatusInfo);
                                }
                                if (null != strDurableSubStatusInfo && strDurableSubStatusInfo.getConditionalAvailableFlag()) {
                                    restrictionFlag = true;
                                    int subLotTypeNum = CimArrayUtils.getSize(strDurableSubStatusInfo.getAvailableSubLotTypes());
                                    for (int l = 0; l < subLotTypeNum; l++) {
                                        if (CimStringUtils.equals(aLot.getSubLotType(), strDurableSubStatusInfo.getAvailableSubLotTypes().get(l))) {
                                            restrictionFlag = false;
                                            break;
                                        }
                                    }
                                }
                            }
                            if (ObjectIdentifier.equalsWithValue(equipmentID, drblEqpID)
                                    && CimStringUtils.equals(drblState, BizConstant.SP_TRANSSTATE_EQUIPMENTIN)
                                    && !restrictionFlag
                                    && (CimStringUtils.equals(drblState, CIMStateConst.CIM_DURABLE_AVAILABLE)
                                    || CimStringUtils.equals(drblState, CIMStateConst.CIM_DURABLE_INUSE))) {
                                Infos.FoundReticle foundReticle = new Infos.FoundReticle();
                                foundReticle.setReticleID(drblID);
                                foundReticle.setReticleGroupID(new ObjectIdentifier(drblgrpID, drblgrpOBJ));
                                Infos.ReticleStatusInfo reticleStatusInfo = new Infos.ReticleStatusInfo();
                                reticleStatusInfo.setReticleStatus(drblState);
                                reticleStatusInfo.setTransferStatus(drblTranState);
                                reticleStatusInfo.setEquipmentID(equipmentID);
                                foundReticle.setReticleStatusInfo(reticleStatusInfo);
                                reticleSeq.add(foundReticle);
                            } else {
                                whatNextAttributes.setReticleExistFlag(false);
                            }
                            if (!ObjectIdentifier.equalsWithValue(drblgrpID, reticleInfo.getReticleGroup())) {
                                whatNextAttributes.setReticleExistFlag(false);
                            }
                        }
                        if (select_criteria_CANBEPROCESSED_OR_EQPMONNOKIT_OR_AUTO3) {
                            if (!whatNextAttributes.getReticleExistFlag()) {
                                log.debug("reticleExistFlag is not TRUE");
                                return null;
                            }
                        }
                    }


                    //*********************************************************//
                    // Check recipe information is already collected or not
                    //*********************************************************//
                    //*********************************************************//
                    // Find referable Lot for performance improvement.
                    //*********************************************************//
                    // todo: Lot可能共有的属性，可以不需要多次查询，使用ConcurrentHashMap数据结构来解决此问题
                    boolean nInfoCollected = false;
                    Infos.DefaultRecipeSetting defaultRecipeSetting = new Infos.DefaultRecipeSetting();

                    boolean bFirstChamberFlag = false;
                    int nFirstChamberCount;
                    List<Infos.Chamber> firstChamberSeq = new ArrayList<>();
                    List<Infos.Chamber> chambers = new ArrayList<>();
                    defaultRecipeSetting.setChamberSeq(chambers);
                    strDefaultRecipeSetSeq.add(defaultRecipeSetting);

                    // todo: 该业务逻辑比较复杂并且浪费性能
                    if (!nInfoCollected) {
                        //---------------------------------
                        //  Referable Lot is not found.
                        //---------------------------------
                        log.debug("InfoCollected == false");
                        if (/*bMultiChamberMachineFlag.get()*/bMultiChamberMachineFlag_LocalThead) {
                            //------------------------------------------------------------------------------------------
                            //  Check LRCP multi Chamber support Flag. If not, turn off the bMultiChamberMachineFlag,
                            //  even if Eqp has some Chambers.
                            //------------------------------------------------------------------------------------------
                            boolean logicRecipeMultiChmberFlag = logicalRecipe.getMultiChmberFlag();
                            if (!CimBooleanUtils.isTrue(logicRecipeMultiChmberFlag)) {
                                /*bMultiChamberMachineFlag.compareAndSet(true, false);*/
                                bMultiChamberMachineFlag_LocalThead = false;
                            }
                        }

                        // if use FPCInfo, get machineRecipe and so on.
                        Boolean FPCLogicalRecipeFound = false;
                        Boolean FPCMachineRecipeFound = false;
                        CimMachineRecipeDO mRecipe = null;
                        if (strLotEffectiveFPCInfoGetOut.isMachineRecipeActionRequiredFlag()) {
                            FPCLogicalRecipeFound = true;
                            String tmpMachineRecipeID = strLotEffectiveFPCInfoGetOut.getFpcInfo().getMachineRecipeID().getValue();
                            if (CimStringUtils.isNotEmpty(tmpMachineRecipeID)) {
                                mRecipe = cimJpaRepository.queryOne("SELECT * FROM OMRCP WHERE RECIPE_ID = ?1", CimMachineRecipeDO.class, tmpMachineRecipeID);
                                // search physical recipe from machine recipe.
                                if (null != mRecipe) {
                                    FPCMachineRecipeFound = true;
                                }
                            }
                        }
                        if (null != mRecipe) {
                            whatNextAttributes.setMachineRecipeID(new ObjectIdentifier(mRecipe.getRecipeID(), mRecipe.getId()));
                        }
                        //---------------------------------------------
                        // Special control for multi chamber machine
                        //---------------------------------------------
                        if (/*bMultiChamberMachineFlag.get()*/bMultiChamberMachineFlag_LocalThead) {
                            if (!strLotEffectiveFPCInfoGetOut.isMachineRecipeActionRequiredFlag()) {
                                if (bConditionalAvailableFlagForChamber.get()) {
                                    //-------------------------------------------------------------------------------------------------------------
                                    //  Eqp has Chambers and LRCP multi Chamber support flag is true and Chamber conditional available is True.
                                    //  So available Falg for each Chamber should be updated by considering Lot's SubLotType.
                                    //-------------------------------------------------------------------------------------------------------------
                                    for (int j = 0; j < nProcessResourceCount; j++) {
                                        if (CimArrayUtils.isNotEmpty(tmpChamberInfo)) {
                                            if (tmpChamberInfo.get(j).getConditionalAvailable()) {
                                                String equipmentStateSltRefKey = tmpChamberInfo.get(j).getCurrentStateObjRef();
                                                long ttlCount = cimJpaRepository.count("SELECT COUNT(ID) FROM OMEQPST_SLTYP WHERE REFKEY = ?1 AND LINK_KEY = ?2", equipmentStateSltRefKey, aLot.getLotType());
                                                Infos.WhatNextChamberInfoInfo chamberInfoInfo = tmpChamberInfo.get(j);
                                                if (0 < ttlCount) {
                                                    chamberInfoInfo.setAvailableFlag(true);
                                                } else {
                                                    chamberInfoInfo.setAvailableFlag(false);
                                                }
                                            }
                                        }
                                    }
                                }

                                List<CimLogicalRecipeDSetDO> logicalRecipeDefaultSettings = cimJpaRepository.query("SELECT * FROM OMLRCP_DFLT f1 WHERE f1.REFKEY in (SELECT f2.ID FROM OMLRCP f2 WHERE f2.LRCP_ID = ?1 ) ORDER BY IDX_NO",
                                        CimLogicalRecipeDSetDO.class,
                                        logicalRecipe.getLogicalRecipeID());
                                int nCandidateMachineRecipeCount = CimArrayUtils.getSize(logicalRecipeDefaultSettings);

                                boolean bChamberConbinationMatch = false;
                                boolean bFirstFound = false;
                                int nSeqNoOfLRDSet = 0;

                                for (int j = 0; j < nCandidateMachineRecipeCount; j++) {
                                    String mrcpSql = "SELECT COUNT(*)\n" +
                                            "  FROM OMLRCP_DFLT, OMRCP, OMRCP_EQP\n" +
                                            " WHERE OMLRCP_DFLT.REFKEY = ?1\n" +
                                            "   AND OMLRCP_DFLT.RECIPE_ID = OMRCP.RECIPE_ID\n" +
                                            "   AND OMLRCP_DFLT.IDX_NO = ?2\n" +
                                            "   AND OMRCP.ID = OMRCP_EQP.REFKEY\n" +
                                            "   AND OMRCP_EQP.EQP_ID = ?3\n";
                                    long mrcpCount = cimJpaRepository.count(mrcpSql, logicalRecipe.getId(), j, equipmentID.getValue());
                                    if (0L == mrcpCount) {
                                        continue;
                                    }
                                    ObjectIdentifier mrrcpRcipeID = null;
                                    if (entityInhibitSearchFlag) {
                                        //-------------------------------------------------------------
                                        // Check Entity Inhibit for MachineRecipe
                                        //-------------------------------------------------------------
                                        CimLogicalRecipeDSetDO logicalRecipeDSetDO = cimJpaRepository.queryOne("SELECT * FROM OMLRCP_DFLT WHERE REFKEY = ?1 AND IDX_NO = ?2",
                                                CimLogicalRecipeDSetDO.class,
                                                logicalRecipe.getId(),
                                                j);
                                        mrrcpRcipeID = ObjectIdentifier.build(logicalRecipeDSetDO.getRecipeID(), logicalRecipeDSetDO.getRecipeObj());
                                    }

                                    String nTmpRecipeNum = String.format("%d", j);
                                    bChamberConbinationMatch = true;
                                    String lrcpDserPrstSql = "SELECT OMLRCP_DFLT_PRST.PROCRSC_ID, OMLRCP_DFLT_PRST.STATE\n" +
                                            "  FROM OMLRCP_DFLT_PRST\n" +
                                            " WHERE OMLRCP_DFLT_PRST.REFKEY = ?1\n" +
                                            "   AND OMLRCP_DFLT_PRST.LINK_MARKER = ?2";
                                    List<Object[]> lrcpDserPrstQuery = cimJpaRepository.query(lrcpDserPrstSql, logicalRecipe.getId(), nTmpRecipeNum);
                                    for (int k = 0; k < CimArrayUtils.getSize(lrcpDserPrstQuery); k++) {
                                        String lrcpDsetPrstProcrscID = lrcpDserPrstQuery.get(k)[0].toString();
                                        String lrcpDsetPrstState = lrcpDserPrstQuery.get(k)[1].toString();
                                        Boolean state = "1".equals(lrcpDsetPrstState);
                                        for (int l = 0; l < nProcessResourceCount; l++) {
                                            Infos.WhatNextChamberInfoInfo chamberInfoInfo = tmpChamberInfo.get(l);
                                            Infos.Chamber chamber = new Infos.Chamber();
                                            if (CimStringUtils.equals(chamberInfoInfo.getChamberID(), lrcpDsetPrstProcrscID)) {
                                                if (chamberInfoInfo.getAvailableFlag() != state) {
                                                    bChamberConbinationMatch = false;
                                                }
                                                if (bChamberConbinationMatch) {
                                                    if (chamberInfoInfo.getAvailableFlag()) {
                                                        chamber.setChamberID(new ObjectIdentifier(chamberInfoInfo.getChamberID()));
                                                        chambers.add(chamber);
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    if (bChamberConbinationMatch) {
                                        if (entityInhibitSearchFlag) {
                                            if (!bFirstFound) {
                                                nSeqNoOfLRDSet = j;
                                                bFirstFound = true;
                                                nFirstChamberCount = CimArrayUtils.getSize(defaultRecipeSetting.getChamberSeq());
                                                for (int z = 0; z < nFirstChamberCount; z++) {
                                                    firstChamberSeq.add(defaultRecipeSetting.getChamberSeq().get(z));
                                                }
                                            }
                                            //------------------------------------------------------------------------
                                            // Check Entity Inhibit for ProductID + EquipmentID + RecipeID + ChamberID
                                            //------------------------------------------------------------------------
                                            List<Constrain.EntityIdentifier> entities = new ArrayList<>();
                                            int processResourceStatesLen = CimArrayUtils.getSize(defaultRecipeSetting.getChamberSeq());
                                            if (ObjectIdentifier.isNotEmpty(whatNextAttributes.getProductID())) {
                                                Constrain.EntityIdentifier entity = new Constrain.EntityIdentifier();
                                                entity.setClassName(BizConstant.SP_INHIBITCLASSID_PRODUCT);
                                                entity.setObjectId(ObjectIdentifier.fetchValue(whatNextAttributes.getProductID()));
                                                entities.add(entity);
                                            }
                                            if (ObjectIdentifier.isNotEmpty(equipmentID)) {
                                                Constrain.EntityIdentifier entity = new Constrain.EntityIdentifier();
                                                entity.setClassName(BizConstant.SP_INHIBITCLASSID_EQUIPMENT);
                                                entity.setObjectId(ObjectIdentifier.fetchValue(equipmentID));
                                                entities.add(entity);
                                            }
                                            if (ObjectIdentifier.isNotEmpty(mrrcpRcipeID)) {
                                                String versionId = BaseStaticMethod.extractVersionFromID(mrrcpRcipeID.getValue());
                                                if (!CimStringUtils.equals(CIMStateConst.ACTIVE_VERSION, versionId)) {

                                                    CimMachineRecipeDO machineRecipeDO = cimJpaRepository.queryOne(" SELECT ACTIVE_ID, ACTIVE_RKEY\n" +
                                                            "                                                     FROM   OMRCP\n" +
                                                            "                                                     WHERE  RECIPE_ID= ?1 ", CimMachineRecipeDO.class, ObjectIdentifier.fetchValue(mrrcpRcipeID));
                                                    if (null != machineRecipeDO && CimStringUtils.isNotEmpty(machineRecipeDO.getActiveID())) {
                                                        Constrain.EntityIdentifier entity = new Constrain.EntityIdentifier();
                                                        entity.setClassName(BizConstant.SP_INHIBITCLASSID_MACHINERECIPE);
                                                        entity.setObjectId(machineRecipeDO.getActiveID());
                                                        entities.add(entity);
                                                    }
                                                } else {
                                                    Constrain.EntityIdentifier entity = new Constrain.EntityIdentifier();
                                                    entity.setClassName(BizConstant.SP_INHIBITCLASSID_MACHINERECIPE);
                                                    entity.setObjectId(ObjectIdentifier.fetchValue(mrrcpRcipeID));
                                                    entities.add(entity);
                                                }
                                            }
                                            for (int y = 0; y < processResourceStatesLen; y++) {
                                                if (!ObjectIdentifier.isEmpty(defaultRecipeSetting.getChamberSeq().get(y).getChamberID())) {
                                                    Constrain.EntityIdentifier entity = new Constrain.EntityIdentifier();
                                                    entity.setClassName(BizConstant.SP_INHIBITCLASSID_CHAMBER);
                                                    entity.setObjectId(ObjectIdentifier.fetchValue(defaultRecipeSetting.getChamberSeq().get(y).getChamberID()));
                                                    entities.add(entity);
                                                }
                                            }
                                            // setting sub lot type for checking entity inhibit
                                            List<Constrain.EntityInhibitRecord> inhibitSeq = entityInhibitManager.allEntityInhibitRecordsForLotEntities(entities, Collections.singletonList(aLot.getSubLotType()));
                                            int uLen = CimArrayUtils.getSize(inhibitSeq);
                                            if (0 < uLen) {
                                                List<Infos.EntityInhibitInfo> entityInhibitInfos = new ArrayList<>();
                                                for (Constrain.EntityInhibitRecord entityInhibitRecord : inhibitSeq) {
                                                    Infos.EntityInhibitInfo entityInhibitInfo = new Infos.EntityInhibitInfo();
                                                    entityInhibitInfo.setEntityInhibitID(new ObjectIdentifier(entityInhibitRecord.getId(), entityInhibitRecord.getReferenceKey()));
                                                    Infos.EntityInhibitAttributes entityInhibitAttributes = new Infos.EntityInhibitAttributes();
                                                    List<Constrain.EntityIdentifier> entits = entityInhibitRecord.getEntities();
                                                    if (!CimObjectUtils.isEmpty(entits)) {
                                                        List<Infos.EntityIdentifier> ens = new ArrayList<>();
                                                        for (Constrain.EntityIdentifier entit : entits) {
                                                            Infos.EntityIdentifier en = new Infos.EntityIdentifier();
                                                            en.setClassName(entit.getClassName());
                                                            en.setObjectID(new ObjectIdentifier(entit.getObjectId()));
                                                            en.setAttribution(entit.getAttrib());
                                                            ens.add(en);
                                                        }
                                                        entityInhibitAttributes.setEntities(ens);
                                                    }
                                                    entityInhibitAttributes.setSubLotTypes(entityInhibitRecord.getSubLotTypes());
                                                    entityInhibitAttributes.setStartTimeStamp(entityInhibitRecord.getStartTimeStamp().toString());
                                                    entityInhibitAttributes.setEndTimeStamp(entityInhibitRecord.getEndTimeStamp().toString());
                                                    entityInhibitAttributes.setClaimedTimeStamp(entityInhibitRecord.getChangedTimeStamp().toString());
                                                    entityInhibitAttributes.setReasonCode(entityInhibitRecord.getReasonCode().getValue());
                                                    entityInhibitAttributes.setOwnerID(entityInhibitRecord.getOwner());
                                                    entityInhibitInfo.setEntityInhibitAttributes(entityInhibitAttributes);
                                                }
                                                Inputs.ObjEntityInhibiteffectiveForLotGetDRIn strEntityInhibitEffectiveForLotGetDRIn = new Inputs.ObjEntityInhibiteffectiveForLotGetDRIn();
                                                strEntityInhibitEffectiveForLotGetDRIn.setStrEntityInhibitInfos(entityInhibitInfos);
                                                strEntityInhibitEffectiveForLotGetDRIn.setLotID(whatNextAttributes.getLotID());
                                                // step6 - entityInhibit_effectiveForLot_GetDR
                                                List<Infos.EntityInhibitInfo> strEntityInhibitEffectiveForLotGetDROut = constraintMethod.constraintEffectiveForLotGetDR(objCommon, strEntityInhibitEffectiveForLotGetDRIn.getStrEntityInhibitInfos(), strEntityInhibitEffectiveForLotGetDRIn.getLotID());

                                                uLen = CimArrayUtils.getSize(strEntityInhibitEffectiveForLotGetDROut);
                                                if (0 < uLen) {
                                                    bChamberConbinationMatch = false;
                                                    continue;
                                                }
                                            }
                                        }
                                        nSeqNoOfLRDSet = j;
                                        break;
                                    }
                                    // chamber availbility combination match recipe DOES NOT FOUND THIS TIME!!
                                    else {
                                        log.debug("chamber availbility combination match recipe DOES NOT FOUND THIS TIME!!");
                                    }
                                }
                                if (entityInhibitSearchFlag) {
                                    if (bFirstFound && !bChamberConbinationMatch) {
                                        bChamberConbinationMatch = true;
                                        bFirstChamberFlag = true;
                                    }
                                }
                                //---------------------------------------
                                // Chamber availability combination
                                // does not match with recipe definition
                                //---------------------------------------
                                if (!bChamberConbinationMatch) {
                                    log.debug("chamber - recipe availability = false");
                                    whatNextAttributes.setRecipeAvailableFlag(false);
                                    if (!ObjectIdentifier.isEmpty(whatNextAttributes.getControlJob()) && bLotHasAssignedMRecipeSeq.get(aLot.getIdentifier())) {
                                        log.debug("controlJob and AssignedMachineRecipe exists");
                                        whatNextAttributes.setRecipeAvailableFlag(true);
                                        whatNextAttributes.setMachineRecipeID(new ObjectIdentifier(processOperation.getAssignRecipeID(), processOperation.getAssignRecipeObj()));
                                        whatNextAttributes.setPhysicalRecipeID(processOperation.getAssignPhysicalRecipeID());
                                    }
                                }
                                //---------------------------------------
                                // Chamber availability combination
                                // match with recipe definition
                                //---------------------------------------
                                else {
                                    log.debug("chamber - recipe availability = true");
                                    String lrcpSQL = "SELECT OMLRCP.MON_PROD_ID,\n" +
                                            "       OMLRCP.MON_PROD_RKEY,\n" +
                                            "       OMLRCP.TEST_TYPE_ID,\n" +
                                            "       OMLRCP.TEST_TYPE_RKEY,\n" +
                                            "       OMLRCP_DFLT.RECIPE_ID,\n" +
                                            "       OMLRCP_DFLT.RECIPE_RKEY\n" +
                                            "  FROM OMLRCP, OMLRCP_DFLT\n" +
                                            " WHERE OMLRCP.ID = ?1\n" +
                                            "   AND OMLRCP_DFLT.REFKEY = ?2\n" +
                                            "   AND OMLRCP_DFLT.IDX_NO = ?3";
                                    List<Object[]> lrcpDsetQuery = cimJpaRepository.query(lrcpSQL, logicalRecipe.getId(), logicalRecipe.getId(), nSeqNoOfLRDSet);
                                    String lrcpMntrProdSpecID = null;
                                    String lrcpMntrProdSpecObj = null;
                                    String lrcpTestTypeID = null;
                                    String lrcpTestTypeObj = null;
                                    String mRecipeID = null;
                                    String mRecipeObj = null;

                                    for (Object[] objects : lrcpDsetQuery) {
                                        lrcpMntrProdSpecID = String.valueOf(objects[0]);
                                        lrcpMntrProdSpecObj = String.valueOf(objects[1]);
                                        lrcpTestTypeID = String.valueOf(objects[2]);
                                        lrcpTestTypeObj = String.valueOf(objects[3]);
                                        mRecipeID = String.valueOf(objects[4]);
                                        mRecipeObj = String.valueOf(objects[5]);
                                    }

                                    if (CimStringUtils.isNotEmpty(mRecipeID)) {
                                        String versionId = BaseStaticMethod.extractVersionFromID(mRecipeID);
                                        if (CimStringUtils.equals(CIMStateConst.ACTIVE_VERSION, versionId)) {
                                            Object[] machineRecipeDO = cimJpaRepository.queryOne(
                                                    "SELECT B.RECIPE_ID, B.ID FROM OMRCP A, OMRCP B WHERE A.RECIPE_ID = ?1 AND B.RECIPE_ID = A.ACTIVE_ID",
                                                    mRecipeID);
                                            mRecipeID = String.valueOf(machineRecipeDO[0]);
                                            mRecipeObj = String.valueOf(machineRecipeDO[1]);
                                        }
                                    }

                                    List<Object> objects = cimJpaRepository.queryOneColumn("SELECT OMRCP.PHY_RECIPE_ID FROM OMRCP WHERE ID = ?1", mRecipeObj);

                                    whatNextAttributes.setRecipeAvailableFlag(true);
                                    whatNextAttributes.setProcessMonitorProductID(ObjectIdentifier.build(lrcpMntrProdSpecID, lrcpMntrProdSpecObj));
                                    whatNextAttributes.setTestTypeID(ObjectIdentifier.build(lrcpTestTypeID, lrcpTestTypeObj));
                                    //---------------------
                                    //Lot Reservation Check
                                    //---------------------
                                    if (ObjectIdentifier.isNotEmptyWithValue(aLot.getControlJobID())) {
                                        log.debug("Lot was Reserved. So set Assigned MachineRecipe From PO");
                                        whatNextAttributes.setMachineRecipeID(ObjectIdentifier.build(processOperation.getAssignRecipeID(), processOperation.getAssignRecipeObj()));
                                        whatNextAttributes.setPhysicalRecipeID(processOperation.getAssignPhysicalRecipeID());
                                    } else {
                                        whatNextAttributes.setMachineRecipeID(ObjectIdentifier.build(mRecipeID, mRecipeObj));
                                        if (null != objects) {
                                            whatNextAttributes.setPhysicalRecipeID(String.valueOf(objects.get(0)));
                                        }
                                    }
                                    log.debug("MachineRecipe and Object");
                                }

                            }
                            // Followins check for chamber is executed even in case machine recipe is changed by DOC.
                            else {
                                log.debug("FPCInfo changes machineRecipe.");
                                // If the EQP is multi-chamber EQP and the chamber is conditional available,
                                // then machineRecipe is usually checked with the LogicalRecipe's recipe check table.
                                // OM_CHAMBER_CHK_RULE=0
                                // DOC changed machineRecipe is not checked, because it is treated as Dynamic Recipe Changed machineRecipe.
                                // The lot is operable or not with the EQP state must be checked by tmpWhatNextAttributes[].operableFlagForCurrentMachineState
                                // OM_CHAMBER_CHK_RULE=1
                                // Followins check for chamber is executed even in case machine recipe is changed by DOC.
                                boolean bChamberFoundFlag = true;
                                boolean tmpChamberCheckPolicy = StandardProperties.OM_CHAMBER_CHK_RULE.isTrue();
                                if (FPCLogicalRecipeFound && FPCMachineRecipeFound && bChamberFoundFlag && tmpChamberCheckPolicy) {
                                    bChamberFoundFlag = false;
                                    if (bConditionalAvailableFlagForChamber.get()) {
                                        //-------------------------------------------------------------------------------------------------------------
                                        //  Eqp has Chambers and LRCP multi Chamber support flag is true and Chamber conditional available is True.
                                        //  So available Falg for each Chamber should be updated by considering Lot's SubLotType.
                                        //-------------------------------------------------------------------------------------------------------------
                                        for (int j = 0; j < nProcessResourceCount; j++) {
                                            Infos.WhatNextChamberInfoInfo whatNextChamberInfoInfo = tmpChamberInfo.get(j);
                                            if (whatNextChamberInfoInfo.getConditionalAvailable()) {
                                                String eqpSTSltSQL = "SELECT COUNT(*)\n" +
                                                        "  FROM OMEQPST_SLTYP\n" +
                                                        " WHERE OMEQPST_SLTYP.LINK_KEY = ?1 \n" +
                                                        "   AND OMEQPST_SLTYP.REFKEY = ?2 ";
                                                long eqpSTSltcount = cimJpaRepository.count(eqpSTSltSQL, aLot.getSubLotType(), whatNextChamberInfoInfo.getCurrentStateObjRef());
                                                if (0L < eqpSTSltcount) {
                                                    whatNextChamberInfoInfo.setAvailableFlag(true);
                                                } else {
                                                    whatNextChamberInfoInfo.setAvailableFlag(false);
                                                }
                                            }
                                        }
                                    }

                                    long ttlCount = cimJpaRepository.count("SELECT COUNT(f1.ID) FROM OMLRCP_DFLT f1 WHERE f1.REFKEY in (SELECT f2.ID FROM OMLRCP f2 WHERE f2.LRCP_ID = ?1 ) ORDER BY IDX_NO", processDefinition.getRecipeObj());
                                    //  List<CimLogicalRecipeDSetDO> logicalRecipeDefaultSettings = logicalRecipeCore.findLogicalRecipeDefaultSettingsByLogicalRecipeID(processDefinition.getRecipeObj());
                                    int nCandidateMachineRecipeCount = CimNumberUtils.intValue(ttlCount);
                                    if (0 == nCandidateMachineRecipeCount) {
                                        bChamberFoundFlag = true;
                                    } else {
                                /*
                                String lrcpDsetPrstSQL = " SELECT OMLRCP_DFLT_PRST.PROCRSC_ID, OMLRCP_DFLT_PRST.STATE\n" +
                                        "   FROM OMLRCP_DFLT_PRST\n" +
                                        "  WHERE OMLRCP_DFLT_PRST.REFKEY = ?1 \n" +
                                        "    AND (OMLRCP_DFLT_PRST.LINK_MARKER = ?2 OR\n" +
                                        "        OMLRCP_DFLT_PRST.LINK_MARKER = ?3 )";
                                String lrcpDsetPrstProcrscID = null;
                                String lrcpDsetPrstState = null;
                                List<Object[]> lrcpDsetPrstQuery = cimJpaRepository.query(lrcpDsetPrstSQL, processDefinition.getRecipeObj(), j, j);
                                if (ArrayUtils.isNotEmpty(lrcpDsetPrstQuery)) {
                                    for (Object[] objects : lrcpDsetPrstQuery) {
                                        lrcpDsetPrstProcrscID = objects[0].toString();
                                        lrcpDsetPrstState = objects[1].toString();
                                    }
                                }
                                */
                                        boolean bFirstFound = false;
                                        boolean bDefinedInLRCP = false;
                                        boolean bChamberConbinationMatch = false;
                                        for (int j = 0; j < nCandidateMachineRecipeCount; j++) {
                                            //----------------------
                                            // Get machine recipe
                                            //----------------------
                                            CimLogicalRecipeDSetDO logicalRecipeDefaultSetting = cimJpaRepository.queryOne("SELECT * FROM OMLRCP_DFLT WHERE REFKEY = ?1 AND IDX_NO = ?2", CimLogicalRecipeDSetDO.class, processDefinition.getRecipeObj(), j);
                                            //logicalRecipeCore.findLogicalRecipeDefaultSettingByRefKeyAndSequenceNumber(processDefinition.getRecipeObj(), j);
                                            boolean bActiveVersion = false;
                                            ObjectIdentifier activeID = null;
                                            if (null != logicalRecipeDefaultSetting && null != logicalRecipeDefaultSetting.getRecipeID()) {
                                                String versionId = BaseStaticMethod.extractVersionFromID(logicalRecipeDefaultSetting.getRecipeID());
                                                if (!CimStringUtils.equals(CIMStateConst.ACTIVE_VERSION, versionId)) {
                                                    CimMachineRecipeDO machineRecipeDO = cimJpaRepository.queryOne("SELECT ACTIVE_ID, ACTIVE_RKEY\n" +
                                                            "                                                     FROM   OMRCP\n" +
                                                            "                                                     WHERE  RECIPE_ID = ?1", CimMachineRecipeDO.class, logicalRecipeDefaultSetting.getRecipeID());
                                                    if (null != machineRecipeDO) {
                                                        activeID = new ObjectIdentifier(machineRecipeDO.getActiveID(), machineRecipeDO.getActiveObj());
                                                        if (ObjectIdentifier.equalsWithValue(whatNextAttributes.getMachineRecipeID(), activeID)) {
                                                            continue;
                                                        }
                                                    }
                                                }
                                            }
                                            bDefinedInLRCP = true;
                                            String nTmpRecipeNum = String.format("%d", j);
                                            String lrcpDsetPrstSQL = " SELECT OMLRCP_DFLT_PRST.PROCRSC_ID, OMLRCP_DFLT_PRST.STATE\n" +
                                                    "   FROM OMLRCP_DFLT_PRST\n" +
                                                    "  WHERE OMLRCP_DFLT_PRST.REFKEY = ?1 \n" +
                                                    "    AND (OMLRCP_DFLT_PRST.LINK_MARKER = ?2 OR\n" +
                                                    "        OMLRCP_DFLT_PRST.LINK_MARKER = ?3 )";
                                            String lrcpDsetPrstProcrscID;
                                            Boolean lrcpDsetPrstState;
                                            String tmpLrcpDsetPrstState;
                                            List<Object[]> lrcpDsetPrstQuery = cimJpaRepository.query(lrcpDsetPrstSQL, processDefinition.getRecipeObj(), nTmpRecipeNum, nTmpRecipeNum);
                                            if (CimArrayUtils.isNotEmpty(lrcpDsetPrstQuery)) {
                                                bChamberConbinationMatch = true;
                                                for (Object[] objects : lrcpDsetPrstQuery) {
                                                    lrcpDsetPrstProcrscID = objects[0].toString();
                                                    tmpLrcpDsetPrstState = objects[1].toString();
                                                    lrcpDsetPrstState = "1".equals(tmpLrcpDsetPrstState);

                                                    for (int k = 0; k < nProcessResourceCount; k++) {
                                                        if (tmpChamberInfo.get(k).getChamberID().equals(lrcpDsetPrstProcrscID)) {
                                                            if (tmpChamberInfo.get(k).getAvailableFlag() != lrcpDsetPrstState) {
                                                                bChamberConbinationMatch = false;
                                                            }
                                                            if (bChamberConbinationMatch) {
                                                                if (tmpChamberInfo.get(k).getAvailableFlag()) {
                                                                    Infos.Chamber chamber = new Infos.Chamber();
                                                                    chamber.setChamberID(new ObjectIdentifier(tmpChamberInfo.get(k).getChamberID()));
                                                                    chambers.add(chamber);
                                                                }
                                                            } else {
                                                                if (tmpChamberInfo.get(k).getAvailableFlag()) {
                                                                    log.debug("availableFlag==TRUE, OMLRCP_DFLT_PRST.STATE==false");
                                                                } else {
                                                                    log.debug("availableFlag==false, OMLRCP_DFLT_PRST.STATE==TRUE");
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                            if (bChamberConbinationMatch) {
                                                log.debug("chamber availbility combination match recipe FOUND!!");
                                                if (!bFirstFound) {
                                                    bFirstFound = true;
                                                    nFirstChamberCount = CimArrayUtils.getSize(defaultRecipeSetting.getChamberSeq());
                                                    for (int k = 0; k < nFirstChamberCount; k++) {
                                                        firstChamberSeq.add(defaultRecipeSetting.getChamberSeq().get(k));
                                                    }
                                                }
                                                //------------------------------------------------------------------------
                                                // Check Entity Inhibit for ProductID + EquipmentID + RecipeID + ChamberID
                                                //------------------------------------------------------------------------
                                                log.debug("Check Entity Inhibit for ProductID + EquipmentID + RecipeID + ChamberID");
                                                int processResourceStatesLen = CimArrayUtils.getSize(defaultRecipeSetting.getChamberSeq());
                                                List<Constrain.EntityIdentifier> entities = new ArrayList<>();
                                                if (!ObjectIdentifier.isEmpty(whatNextAttributes.getProductID())) {
                                                    Constrain.EntityIdentifier entitie = new Constrain.EntityIdentifier();
                                                    entitie.setClassName(BizConstant.SP_INHIBITCLASSID_PRODUCT);
                                                    entitie.setObjectId(ObjectIdentifier.fetchValue(whatNextAttributes.getProductID()));
                                                    entities.add(entitie);
                                                }
                                                if (!ObjectIdentifier.isEmpty(equipmentID)) {
                                                    Constrain.EntityIdentifier entitie = new Constrain.EntityIdentifier();
                                                    entitie.setClassName(BizConstant.SP_INHIBITCLASSID_EQUIPMENT);
                                                    entitie.setObjectId(ObjectIdentifier.fetchValue(equipmentID));
                                                    entities.add(entitie);
                                                }
                                                if (bActiveVersion) {
                                                    log.debug("bActiveVersion == TRUE");
                                                    if (!ObjectIdentifier.isEmpty(activeID)) {
                                                        Constrain.EntityIdentifier entitie = new Constrain.EntityIdentifier();
                                                        entitie.setClassName(BizConstant.SP_INHIBITCLASSID_MACHINERECIPE);
                                                        entitie.setObjectId(ObjectIdentifier.fetchValue(activeID));
                                                        entities.add(entitie);
                                                    } else {
                                                        Constrain.EntityIdentifier entitie = new Constrain.EntityIdentifier();
                                                        entitie.setClassName(BizConstant.SP_INHIBITCLASSID_MACHINERECIPE);
                                                        entitie.setObjectId(ObjectIdentifier.fetchValue(whatNextAttributes.getMachineRecipeID()));
                                                        entities.add(entitie);
                                                    }
                                                }
                                                for (int k = 0; k < processResourceStatesLen; k++) {
                                                    if (!ObjectIdentifier.isEmpty(defaultRecipeSetting.getChamberSeq().get(k).getChamberID())) {
                                                        Constrain.EntityIdentifier entitie = new Constrain.EntityIdentifier();
                                                        entitie.setClassName(BizConstant.SP_INHIBITCLASSID_CHAMBER);
                                                        entitie.setObjectId(ObjectIdentifier.fetchValue(equipmentID));
                                                        entities.add(entitie);
                                                    }
                                                }
                                                // setting sub lot type for checking entity inhibit
                                                List<Constrain.EntityInhibitRecord> inhibitSeq = entityInhibitManager.allEntityInhibitRecordsForLotEntities(entities, Collections.singletonList(aLot.getSubLotType()));
                                                int uLen = CimArrayUtils.getSize(inhibitSeq);
                                                if (0 < uLen) {
                                                    List<Infos.EntityInhibitInfo> entityInhibitInfos = new ArrayList<>();
                                                    for (Constrain.EntityInhibitRecord entityInhibitRecord : inhibitSeq) {
                                                        Infos.EntityInhibitInfo entityInhibitInfo = new Infos.EntityInhibitInfo();
                                                        entityInhibitInfo.setEntityInhibitID(new ObjectIdentifier(entityInhibitRecord.getId(), entityInhibitRecord.getReferenceKey()));
                                                        Infos.EntityInhibitAttributes entityInhibitAttributes = new Infos.EntityInhibitAttributes();
                                                        List<Constrain.EntityIdentifier> entits = entityInhibitRecord.getEntities();
                                                        if (!CimObjectUtils.isEmpty(entits)) {
                                                            List<Infos.EntityIdentifier> ens = new ArrayList<>();
                                                            for (Constrain.EntityIdentifier entit : entits) {
                                                                Infos.EntityIdentifier en = new Infos.EntityIdentifier();
                                                                en.setClassName(entit.getClassName());
                                                                en.setObjectID(new ObjectIdentifier(entit.getObjectId()));
                                                                en.setAttribution(entit.getAttrib());
                                                                ens.add(en);
                                                            }
                                                            entityInhibitAttributes.setEntities(ens);
                                                        }
                                                        entityInhibitAttributes.setSubLotTypes(entityInhibitRecord.getSubLotTypes());
                                                        entityInhibitAttributes.setStartTimeStamp(entityInhibitRecord.getStartTimeStamp().toString());
                                                        entityInhibitAttributes.setEndTimeStamp(entityInhibitRecord.getEndTimeStamp().toString());
                                                        entityInhibitAttributes.setClaimedTimeStamp(entityInhibitRecord.getChangedTimeStamp().toString());
                                                        entityInhibitAttributes.setReasonCode(entityInhibitRecord.getReasonCode().getValue());
                                                        entityInhibitAttributes.setOwnerID(entityInhibitRecord.getOwner());
                                                        entityInhibitInfo.setEntityInhibitAttributes(entityInhibitAttributes);
                                                    }
                                                    Inputs.ObjEntityInhibiteffectiveForLotGetDRIn strEntityInhibitEffectiveForLotGetDRIn = new Inputs.ObjEntityInhibiteffectiveForLotGetDRIn();
                                                    strEntityInhibitEffectiveForLotGetDRIn.setStrEntityInhibitInfos(entityInhibitInfos);
                                                    strEntityInhibitEffectiveForLotGetDRIn.setLotID(whatNextAttributes.getLotID());
                                                    // step6 - entityInhibit_effectiveForLot_GetDR
                                                    List<Infos.EntityInhibitInfo> strEntityInhibitEffectiveForLotGetDROut = constraintMethod.constraintEffectiveForLotGetDR(objCommon, strEntityInhibitEffectiveForLotGetDRIn.getStrEntityInhibitInfos(), strEntityInhibitEffectiveForLotGetDRIn.getLotID());

                                                    uLen = CimArrayUtils.getSize(strEntityInhibitEffectiveForLotGetDROut);
                                                    if (0 < uLen) {
                                                        bChamberConbinationMatch = false;
                                                        continue;
                                                    }
                                                    break;
                                                }
                                            }
                                        }
                                        if (!bDefinedInLRCP) {
                                            log.debug("overwritten recipe isn't defined in default setting of logical recipe, skip chamber check.");
                                            bChamberFoundFlag = true;
                                        }
                                        if (bFirstFound && !bChamberConbinationMatch) {
                                            log.debug("bFirstFound = TRUE && bChamberConbinationMatch = false");
                                            bFirstChamberFlag = true;
                                        }
                                    }
                                }
                                if (!bChamberFoundFlag) {
                                    log.debug("Chamber check is performed, but available ones aren't found.");
                                }
                                if (FPCLogicalRecipeFound && FPCMachineRecipeFound && bChamberFoundFlag) {
                                    log.debug("Logical/Machine recipe found.");
                                    whatNextAttributes.setProcessMonitorProductID(new ObjectIdentifier(logicalRecipe.getMonitorProductSpecificationID(), logicalRecipe.getMonitorProductSpecificationObj()));
                                    whatNextAttributes.setTestTypeID(new ObjectIdentifier(logicalRecipe.getTestTypeID(), logicalRecipe.getTestTypeObj()));
                                    if (ObjectIdentifier.isNotEmptyWithValue(aLot.getControlJobID())) {
                                        log.debug("reserved");
                                        whatNextAttributes.setRecipeAvailableFlag(true);
                                        whatNextAttributes.setMachineRecipeID(new ObjectIdentifier(processOperation.getAssignRecipeID(), processOperation.getAssignRecipeObj()));
                                        whatNextAttributes.setPhysicalRecipeID(processOperation.getAssignPhysicalRecipeID());
                                    } else {
                                        log.debug("not reserved");
                                        whatNextAttributes.setRecipeAvailableFlag(true);
                                        whatNextAttributes.setMachineRecipeID(new ObjectIdentifier(mRecipe.getRecipeID(), mRecipe.getId()));
                                        whatNextAttributes.setPhysicalRecipeID(mRecipe.getPhysicalRecipeID());
                                    }
                                } else {
                                    log.debug("Logical/Machine recipe not found.");
                                    if (ObjectIdentifier.isNotEmptyWithValue(aLot.getControlJobID())) {
                                        log.debug("reserved");
                                        whatNextAttributes.setRecipeAvailableFlag(true);
                                        whatNextAttributes.setMachineRecipeID(new ObjectIdentifier(processOperation.getAssignRecipeID(), processOperation.getAssignRecipeObj()));
                                        whatNextAttributes.setPhysicalRecipeID(processOperation.getAssignPhysicalRecipeID());
                                    }
                                }
                            }
                        }
                        //---------------------------------------------
                        // Not multi chamber machine case
                        //---------------------------------------------
                        else {
                            log.debug("machine is not multi chamber type");
                            if (!strLotEffectiveFPCInfoGetOut.isMachineRecipeActionRequiredFlag()) {
                                //-------------------------------------------------------------
                                // select Logical and Machine and Physical Recipe information
                                //-------------------------------------------------------------
                                String logicalRecipeMonitorProductSpecificationID = null;
                                String logicalRecipeMonitorProductSpecificationObj = null;
                                String logicalRecipeTestTypeID = null;
                                String logicalRecipeTestTypeObj = null;
                                String machineRecipeID = null;
                                String machineRecipePhysicalRecipeID = null;
                                Integer sequenceNumber = null;
                                String lrcpSQL = "SELECT OMLRCP.MON_PROD_ID,\n" +
                                        "       OMLRCP.MON_PROD_RKEY,\n" +
                                        "       OMLRCP.TEST_TYPE_ID,\n" +
                                        "       OMLRCP.TEST_TYPE_RKEY,\n" +
                                        "       OMRCP.RECIPE_ID,\n" +
                                        "       OMRCP.PHY_RECIPE_ID,\n" +
                                        "       OMLRCP_DFLT.IDX_NO\n" +
                                        "  FROM OMLRCP, OMLRCP_DFLT, OMRCP, OMRCP_EQP\n" +
                                        " WHERE OMLRCP.ID = ?1 \n" +
                                        "   AND OMLRCP_DFLT.REFKEY = ?2 \n" +
                                        "   AND OMLRCP_DFLT.RECIPE_ID = OMRCP.RECIPE_ID\n" +
                                        "   AND OMRCP.ID = OMRCP_EQP.REFKEY\n" +
                                        "   AND OMRCP_EQP.EQP_ID = ?3 ";
                                List<Object[]> lrcpQuery = cimJpaRepository.query(lrcpSQL, logicalRecipe.getId(), logicalRecipe.getId(), equipmentID.getValue());
                                if (CimArrayUtils.isNotEmpty(lrcpQuery)) {
                                    for (Object[] objects : lrcpQuery) {
                                        logicalRecipeMonitorProductSpecificationID = String.valueOf(objects[0]);
                                        logicalRecipeMonitorProductSpecificationObj = String.valueOf(objects[1]);
                                        logicalRecipeTestTypeID = String.valueOf(objects[2]);
                                        logicalRecipeTestTypeObj = String.valueOf(objects[3]);
                                        machineRecipeID = String.valueOf(objects[4]);
                                        if (CimStringUtils.isNotEmpty(machineRecipeID)) {
                                            final String versionId = cimFrameWorkGlobals.extractVersionFromID(machineRecipeID);
                                            if (CimStringUtils.equals(versionId, BizConstant.SP_ACTIVE_VERSION)) {
                                                String mrcpSQL = "SELECT B.RECIPE_ID\n" +
                                                        " FROM  OMRCP A, OMRCP B\n" +
                                                        " WHERE A.RECIPE_ID = ?1 AND\n" +
                                                        " B.RECIPE_ID = A.ACTIVE_ID";
                                                List<Object[]> mrcpQuery = cimJpaRepository.query(mrcpSQL, machineRecipeID);
                                                if (CimArrayUtils.isNotEmpty(mrcpQuery)) {
                                                    for (Object[] objects1 : mrcpQuery) {
                                                        machineRecipeID = String.valueOf(objects1[0]);
                                                    }
                                                }
                                            }
                                        }
                                        machineRecipePhysicalRecipeID = String.valueOf(objects[5]);
                                        // not used    sequenceNumber = null != objects[6] ? Integer.parseInt(String.valueOf(objects[6])) : 0;
                                    }
                                }
                                whatNextAttributes.setRecipeAvailableFlag(true);
                                whatNextAttributes.setProcessMonitorProductID(new ObjectIdentifier(logicalRecipeMonitorProductSpecificationID, logicalRecipeMonitorProductSpecificationObj));
                                whatNextAttributes.setTestTypeID(new ObjectIdentifier(logicalRecipeTestTypeID, logicalRecipeTestTypeObj));
                                //---------------------
                                //Lot Reservation Check
                                //---------------------
                                if (ObjectIdentifier.isNotEmptyWithValue(aLot.getControlJobID())) {
                                    whatNextAttributes.setMachineRecipeID(new ObjectIdentifier(processOperation.getAssignRecipeID(), processOperation.getAssignRecipeObj()));
                                    whatNextAttributes.setPhysicalRecipeID(processOperation.getAssignPhysicalRecipeID());
                                } else {
                                    //------------------------------------------------------------------------------------------------------
                                    //  Even if the Lot does not have relation with any logical recipe, "Recipe Available Flag" is "Yes".
                                    //  There is no problem in Auto-3 mode, but this flag should be "No" in case of other mode.
                                    //  Therefore, we changed to turn the flag "No" by condition.
                                    //
                                    //   1. Logical Recipe(Machine Recipe) is not found.
                                    //   2. And WhatNext mode is not "Auto-3".
                                    //   3. And the Lot does not have control job.
                                    //------------------------------------------------------------------------------------------------------
                                    if (CimStringUtils.isEmpty(machineRecipeID) && (!BizConstant.SP_DP_SELECTCRITERIA_AUTO3.equals(selectCriteria)
                                            || !BizConstant.SP_DP_SELECTCRITERIA_EQPMONNOKIT.equals(selectCriteria))) {
                                        whatNextAttributes.setRecipeAvailableFlag(false);
                                    }

                                    whatNextAttributes.setMachineRecipeID(ObjectIdentifier.buildWithValue(machineRecipeID));
                                    whatNextAttributes.setPhysicalRecipeID(machineRecipePhysicalRecipeID);
                                }
                            } else {
                                if (FPCLogicalRecipeFound && FPCMachineRecipeFound) {
                                    whatNextAttributes.setProcessMonitorProductID(ObjectIdentifier.build(logicalRecipe.getMonitorProductSpecificationID(), logicalRecipe.getMonitorProductSpecificationObj()));
                                    whatNextAttributes.setTestTypeID(ObjectIdentifier.build(logicalRecipe.getTestTypeID(), logicalRecipe.getTestTypeObj()));
                                    if (ObjectIdentifier.isNotEmptyWithValue(aLot.getControlJobID())) {
                                        whatNextAttributes.setRecipeAvailableFlag(true);
                                        whatNextAttributes.setMachineRecipeID(ObjectIdentifier.build(processOperation.getAssignRecipeID(), processOperation.getAssignRecipeObj()));
                                        whatNextAttributes.setPhysicalRecipeID(processOperation.getAssignPhysicalRecipeID());
                                    } else {
                                        whatNextAttributes.setRecipeAvailableFlag(true);
                                        whatNextAttributes.setMachineRecipeID(ObjectIdentifier.build(mRecipe.getRecipeID(), mRecipe.getId()));
                                        whatNextAttributes.setPhysicalRecipeID(mRecipe.getPhysicalRecipeID());
                                    }
                                } else {
                                    whatNextAttributes.setProcessMonitorProductID(ObjectIdentifier.build("", ""));
                                    whatNextAttributes.setTestTypeID(ObjectIdentifier.build("", ""));
                                    if (ObjectIdentifier.isNotEmptyWithValue(aLot.getControlJobID())) {
                                        whatNextAttributes.setRecipeAvailableFlag(true);
                                        whatNextAttributes.setMachineRecipeID(ObjectIdentifier.build(processOperation.getAssignRecipeID(), processOperation.getAssignRecipeObj()));
                                        whatNextAttributes.setPhysicalRecipeID(processOperation.getAssignPhysicalRecipeID());
                                    } else {
                                        whatNextAttributes.setRecipeAvailableFlag(false);
                                        whatNextAttributes.setMachineRecipeID(ObjectIdentifier.build("*", ""));
                                        whatNextAttributes.setPhysicalRecipeID("");
                                    }
                                }
                            }
                        }
                    }

                    if (select_criteria_CANBEPROCESSED_OR_EQPMONNOKIT_OR_AUTO3) {
                        if (!whatNextAttributes.getRecipeAvailableFlag()) {
                            log.debug("recipeAvailableFlag is not TRUE");
                            return null;
                        }
                    }

                    //*********************************************//
                    // Get entity inhibit information for lot
                    //*********************************************//
                    List<Infos.EntityInhibitInfo> entityInhibitInfoSeq = new ArrayList<>();
                    if (!bEntityInhibitInfoCollected) {
                        List<Constrain.EntityIdentifier> entities = new ArrayList<>();
                        if (ObjectIdentifier.isNotEmpty(whatNextAttributes.getProductID())) {
                            Constrain.EntityIdentifier entity = new Constrain.EntityIdentifier();
                            entity.setClassName(BizConstant.SP_INHIBITCLASSID_PRODUCT);
                            entity.setObjectId(ObjectIdentifier.fetchValue(whatNextAttributes.getProductID()));
                            entities.add(entity);
                        }
                        if (ObjectIdentifier.isNotEmpty(whatNextAttributes.getRouteID())) {
                            Constrain.EntityIdentifier entity = new Constrain.EntityIdentifier();
                            entity.setClassName(BizConstant.SP_INHIBITCLASSID_ROUTE);
                            entity.setObjectId(ObjectIdentifier.fetchValue(whatNextAttributes.getRouteID()));
                            if (CimStringUtils.isNotEmpty(whatNextAttributes.getOperationNumber())) {
                                entity.setAttrib(whatNextAttributes.getOperationNumber());
                            }
                            entities.add(entity);
                        }
                        if (ObjectIdentifier.isNotEmpty(whatNextAttributes.getOperationID())) {
                            Constrain.EntityIdentifier entity = new Constrain.EntityIdentifier();
                            entity.setClassName(BizConstant.SP_INHIBITCLASSID_PROCESS);
                            entity.setObjectId(ObjectIdentifier.fetchValue(whatNextAttributes.getOperationID()));
                            entities.add(entity);
                        }
                        if (CimStringUtils.isNotEmpty(processOperation.getModuleProcessDefinitionID())) {
                            Constrain.EntityIdentifier entity = new Constrain.EntityIdentifier();
                            entity.setClassName(BizConstant.SP_INHIBITCLASSID_MODULEPD);
                            entity.setObjectId(processOperation.getModuleProcessDefinitionID());
                            entities.add(entity);
                        }
                        if (ObjectIdentifier.isNotEmpty(equipmentID)) {
                            Constrain.EntityIdentifier entity = new Constrain.EntityIdentifier();
                            entity.setClassName(BizConstant.SP_INHIBITCLASSID_EQUIPMENT);
                            entity.setObjectId(ObjectIdentifier.fetchValue(equipmentID));
                            entities.add(entity);
                        }
                        if (ObjectIdentifier.isNotEmpty(whatNextAttributes.getMachineRecipeID())
                                && !ObjectIdentifier.equalsWithValue(whatNextAttributes.getMachineRecipeID(), "*")) {
                            Constrain.EntityIdentifier entity = new Constrain.EntityIdentifier();
                            entity.setClassName(BizConstant.SP_INHIBITCLASSID_MACHINERECIPE);
                            entity.setObjectId(ObjectIdentifier.fetchValue(whatNextAttributes.getMachineRecipeID()));
                            entities.add(entity);
                        } else {
                            if (entityInhibitSearchFlag) {
                                // todo: 这里逻辑有问题，该值永远为空 inhibitMachineRecipe === null
                                if (ObjectIdentifier.isNotEmpty(inhibitMachineRecipe)) {
                                    Constrain.EntityIdentifier entity = new Constrain.EntityIdentifier();
                                    entity.setClassName(BizConstant.SP_INHIBITCLASSID_MACHINERECIPE);
                                    entity.setObjectId(ObjectIdentifier.fetchValue(inhibitMachineRecipe));
                                    entities.add(entity);
                                }
                            }
                        }
                        int reticleGrpLen = CimArrayUtils.getSize(whatNextAttributes.getReticleGroupIDs());
                        for (int j = 0; j < reticleGrpLen; j++) {
                            ObjectIdentifier reticleGroup = whatNextAttributes.getReticleGroupIDs().get(j);
                            if (!ObjectIdentifier.isEmpty(reticleGroup)) {
                                Constrain.EntityIdentifier entity = new Constrain.EntityIdentifier();
                                entity.setClassName(BizConstant.SP_INHIBITCLASSID_RETICLEGROUP);
                                entity.setObjectId(ObjectIdentifier.fetchValue(reticleGroup));
                                entities.add(entity);
                            }
                        }

                        //---------------------
                        // Reticle Group Loop
                        //---------------------
                        if (!strLotEffectiveFPCInfoGetOut.isRecipeParameterActionRequiredFlag()) {
                            for (int j = 0; j < reticleGrpLen; j++) {
                                ObjectIdentifier reticleGroup = whatNextAttributes.getReticleGroupIDs().get(j);
                                if (ObjectIdentifier.isEmpty(reticleGroup)) {
                                    continue;
                                }
                                Params.ReticleListInqParams reticleListInqParams = new Params.ReticleListInqParams();
                                reticleListInqParams.setReticleGroupID(reticleGroup);
                                reticleListInqParams.setMaxRetrieveCount(100L);
                                // todo: tmpFPCCategory === null
                                reticleListInqParams.setFPCCategory(null);
                                reticleListInqParams.setWhiteDefSearchCriteria(BizConstant.SP_WHITEDEF_SEARCHCRITERIA_ALL);
                                reticleListInqParams.setReticleGroupID(reticleGroup);

                                Results.ReticleListInqResult strReticleListGetDROut = reticleMethod.reticleListGetDR(objCommon, reticleListInqParams);
                                int FRLength = CimArrayUtils.getSize(strReticleListGetDROut.getStrFoundReticle());
                                //----------------------------------
                                // Reticle ID In Reticle Group Loop
                                //----------------------------------
                                for (int k = 0; k < FRLength; k++) {
                                    boolean restrictionFlag = false;
                                    ObjectIdentifier durableSubStatus = strReticleListGetDROut.getStrFoundReticle().get(k).getReticleStatusInfo().getDurableSubStatus();
                                    if (!ObjectIdentifier.isEmpty(durableSubStatus)) {
                                        Infos.DurableSubStatusInfo strDurableSubStatusInfo = new Infos.DurableSubStatusInfo();
                                        if (!durableSubStatusList.containsKey(ObjectIdentifier.fetchValue(durableSubStatus))) {
                                            Inputs.ObjDurableSubStateDBInfoGetDRIn durableSubStateDBInfoGetDRIn = new Inputs.ObjDurableSubStateDBInfoGetDRIn();
                                            durableSubStateDBInfoGetDRIn.setDurableSubStatus(durableSubStatus.getValue());
                                            durableSubStateDBInfoGetDRIn.setAvailableSubLotTypeInfoFlag(true);
                                            durableSubStateDBInfoGetDRIn.setNextTransitionDurableSubStatusInfoFlag(false);
                                            strDurableSubStatusInfo = durableMethod.durableSubStateDBInfoGetDR(objCommon, durableSubStateDBInfoGetDRIn);
                                            durableSubStatusList.put(ObjectIdentifier.fetchValue(durableSubStatus), strDurableSubStatusInfo);
                                        }
                                        boolean AvailableFlag = null == strDurableSubStatusInfo.getConditionalAvailableFlag() ? false : strDurableSubStatusInfo.getConditionalAvailableFlag();
                                        if (AvailableFlag) {
                                            restrictionFlag = true;
                                            int lenSubLotType = CimArrayUtils.getSize(strDurableSubStatusInfo.getAvailableSubLotTypes());
                                            for (int l = 0; l < lenSubLotType; l++) {
                                                String subLotType = strDurableSubStatusInfo.getAvailableSubLotTypes().get(l);
                                                if (CimStringUtils.equals(aLot.getSubLotType(), subLotType)) {
                                                    restrictionFlag = false;
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                    Infos.ReticleStatusInfo reticleStatusInfo = strReticleListGetDROut.getStrFoundReticle().get(k).getReticleStatusInfo();
                                    if (ObjectIdentifier.equalsWithValue(reticleStatusInfo.getEquipmentID(), equipmentID)
                                            && CimStringUtils.equals(reticleStatusInfo.getTransferStatus(), BizConstant.SP_TRANSSTATE_EQUIPMENTIN)
                                            && !restrictionFlag
                                            && (CimStringUtils.equals(reticleStatusInfo.getReticleStatus(), CIMStateConst.CIM_DURABLE_AVAILABLE)
                                            || CimStringUtils.equals(reticleStatusInfo.getReticleStatus(), CIMStateConst.CIM_DURABLE_INUSE))) {

                                        Constrain.EntityIdentifier entity = new Constrain.EntityIdentifier();
                                        entity.setClassName(BizConstant.SP_INHIBITCLASSID_RETICLE);
                                        entity.setObjectId(ObjectIdentifier.fetchValue(strReticleListGetDROut.getStrFoundReticle().get(k).getReticleID()));
                                        entities.add(entity);
                                        Infos.FoundReticle foundReticle = new Infos.FoundReticle();
                                        foundReticle.setReticleID(ObjectIdentifier.buildWithValue(entity.getObjectId()));
                                        foundReticle.setReticleGroupID(strReticleListGetDROut.getStrFoundReticle().get(k).getReticleGroupID());
                                        reticleSeq.add(foundReticle);
                                    }
                                }
                            }
                        } else {
                            InhibitReticleIDLen = CimArrayUtils.getSize(reticleSeq);
                            for (int j = 0; j < InhibitReticleIDLen; j++) {
                                Constrain.EntityIdentifier entity = new Constrain.EntityIdentifier();
                                entity.setClassName(BizConstant.SP_INHIBITCLASSID_RETICLE);
                                entity.setObjectId(ObjectIdentifier.fetchValue(reticleSeq.get(j).getReticleID()));
                                entities.add(entity);
                            }
                        }
                        if (!ObjectIdentifier.isEmpty(whatNextAttributes.getStageID())) {
                            Constrain.EntityIdentifier entity = new Constrain.EntityIdentifier();
                            entity.setClassName(BizConstant.SP_INHIBITCLASSID_STAGE);
                            entity.setObjectId(ObjectIdentifier.fetchValue(whatNextAttributes.getStageID()));
                            entities.add(entity);
                        }
                        int chamberLen = CimArrayUtils.getSize(strDefaultRecipeSetSeq.peekLast().getChamberSeq());
                        if (chamberLen > 0) {
                            for (int j = 0; j < chamberLen; j++) {
                                Infos.Chamber chamber = strDefaultRecipeSetSeq.peekLast().getChamberSeq().get(j);
                                if (ObjectIdentifier.isEmpty(chamber.getChamberID())) {
                                    continue;
                                }
                                Constrain.EntityIdentifier entity = new Constrain.EntityIdentifier();
                                entity.setClassName(BizConstant.SP_INHIBITCLASSID_CHAMBER);
                                entity.setObjectId(ObjectIdentifier.fetchValue(equipmentID));
                                entity.setAttrib(ObjectIdentifier.fetchValue(chamber.getChamberID()));
                                entities.add(entity);
                            }
                        } else {
                            if (bFirstChamberFlag) {
                                chamberLen = CimArrayUtils.getSize(firstChamberSeq);
                                for (int j = 0; j < chamberLen; j++) {
                                    if (ObjectIdentifier.isEmpty(firstChamberSeq.get(j).getChamberID())) {
                                        continue;
                                    }
                                    Constrain.EntityIdentifier entity = new Constrain.EntityIdentifier();
                                    entity.setClassName(BizConstant.SP_INHIBITCLASSID_CHAMBER);
                                    entity.setObjectId(ObjectIdentifier.fetchValue(equipmentID));
                                    entity.setAttrib(ObjectIdentifier.fetchValue(firstChamberSeq.get(j).getChamberID()));
                                    entities.add(entity);
                                }
                                defaultRecipeSetting.setChamberSeq(firstChamberSeq);
                            }
                        }
                        List<String> sublotTypes = new ArrayList<>();
                        sublotTypes.add(aLot.getSubLotType());
                        // setting sub lot type for checking entity inhibit
                        // checking inhibit with entity information and sub lot type
                        List<Constrain.EntityInhibitRecord> inhibitSeq = entityInhibitManager.allEntityInhibitRecordsForLotEntities(entities, sublotTypes);
                        int numOfInhibits = CimArrayUtils.getSize(inhibitSeq);
                        entityInhibitInfoSeq = inhibitSeq.stream().map(entityInhibitRecord -> {
                            Infos.EntityInhibitInfo entityInhibitInfo = new Infos.EntityInhibitInfo();
                            Infos.EntityInhibitAttributes entityInhibitAttributes = new Infos.EntityInhibitAttributes();
                            entityInhibitInfo.setEntityInhibitAttributes(entityInhibitAttributes);
                            List<Constrain.EntityIdentifier> entits = entityInhibitRecord.getEntities();
                            if (CimArrayUtils.isNotEmpty(entits)) {
                                List<Infos.EntityIdentifier> ens = new ArrayList<>();
                                for (Constrain.EntityIdentifier entit : entits) {
                                    Infos.EntityIdentifier en = new Infos.EntityIdentifier();
                                    en.setClassName(entit.getClassName());
                                    en.setObjectID(ObjectIdentifier.buildWithValue(entit.getObjectId()));
                                    en.setAttribution(entit.getAttrib());
                                    ens.add(en);
                                }
                                entityInhibitAttributes.setEntities(ens);
                            }
                            entityInhibitInfo.setEntityInhibitID(ObjectIdentifier.build(entityInhibitRecord.getId(), entityInhibitRecord.getReferenceKey()));
                            entityInhibitAttributes.setSubLotTypes(entityInhibitRecord.getSubLotTypes());
                            entityInhibitAttributes.setStartTimeStamp(CimDateUtils.convertToSpecString(entityInhibitRecord.getStartTimeStamp()));
                            entityInhibitAttributes.setEndTimeStamp(CimDateUtils.convertToSpecString(entityInhibitRecord.getEndTimeStamp()));
                            entityInhibitAttributes.setClaimedTimeStamp(CimDateUtils.convertToSpecString(entityInhibitRecord.getChangedTimeStamp()));
                            entityInhibitAttributes.setReasonCode(entityInhibitRecord.getReasonCode().getValue());
                            entityInhibitAttributes.setOwnerID(entityInhibitRecord.getOwner());
                            entityInhibitAttributes.setMemo(entityInhibitRecord.getClaimMemo());
                            return entityInhibitInfo;
                        }).collect(Collectors.toList());
                        //------------------------------------------------------------------//
                        //   Check Inhibition for each Reticle.                             //
                        //   This check is necessary if this Equipment requires Reticle,    //
                        //   and the Reticle is available for Lot.                          //
                        //------------------------------------------------------------------//
                        if (0 < reticleGrpLen) {
                            // entityInhibit_CheckForReticleInhibition
                            Inputs.EntityInhibitCheckForReticleInhibition entityInhibitCheckForReticleInhibition = new Inputs.EntityInhibitCheckForReticleInhibition();
                            entityInhibitCheckForReticleInhibition.setEntityInhibitInfoSeq(entityInhibitInfoSeq);
                            if (!CimObjectUtils.isEmpty(entities)) {
                                entityInhibitCheckForReticleInhibition.setEntityIDSeq(entities.stream()
                                        .map(x -> new Infos.EntityIdentifier(x.getClassName(), ObjectIdentifier.buildWithValue(x.getObjectId()), x.getAttrib()))
                                        .collect(Collectors.toList()));
                            }
                            entityInhibitCheckForReticleInhibition.setReticleSeq(reticleSeq);
                            entityInhibitCheckForReticleInhibition.setSublottypes(sublotTypes);
                            entityInhibitCheckForReticleInhibition.setUseFPCInfo(strLotEffectiveFPCInfoGetOut.isReticleActionRequiredFlag());

                            entityInhibitInfoSeq = constraintMethod.constraintCheckForReticleInhibition(objCommon, entityInhibitCheckForReticleInhibition);
                            numOfInhibits = CimArrayUtils.getSize(entityInhibitInfoSeq);
                        }
                        for (int j = 0; j < numOfInhibits; j++) {
                            Infos.EntityInhibitAttributes entityInhibitAttributes = entityInhibitInfoSeq.get(j).getEntityInhibitAttributes();

                            CimCodeDO codeDo = cimJpaRepository.queryOne("SELECT * FROM OMCODE WHERE CODETYPE_ID = ?1 AND CODE_ID = ?2",
                                    CimCodeDO.class,
                                    BizConstant.SP_REASONCAT_ENTITYINHIBIT,
                                    entityInhibitAttributes.getReasonCode());
                            //     cimCode.findCodeByCategoryIDAndCodeID(BizConstant.SP_ReasonCat_EntityInhibit.getValue(), entityInhibitAttributes.getReasonCode());
                            if (null != codeDo) {
                                entityInhibitAttributes.setReasonDesc(codeDo.getDescription());
                            }
                        }
                        Inputs.ObjEntityInhibiteffectiveForLotGetDRIn inhibiteffectiveForLotGetDRIn = new Inputs.ObjEntityInhibiteffectiveForLotGetDRIn();
                        inhibiteffectiveForLotGetDRIn.setLotID(whatNextAttributes.getLotID());
                        inhibiteffectiveForLotGetDRIn.setStrEntityInhibitInfos(entityInhibitInfoSeq);

                        // Save backup for entity inhibit sequence before filter because it might be copied to the lot following this one.
                        entityInhibiteffectiveForLotGetDRIns.put(samePropertiesCheckKey, inhibiteffectiveForLotGetDRIn);
                        inhibitCollectedBeforeContinue.put(samePropertiesCheckKey, true);

                        if (numOfInhibits > 0) {
                            entityInhibitInfoSeq = constraintMethod.constraintEffectiveForLotGetDR(objCommon, inhibiteffectiveForLotGetDRIn.getStrEntityInhibitInfos(), inhibiteffectiveForLotGetDRIn.getLotID());
                        }
                        List<Infos.EntityInhibitAttributes> entityInhibitAttributes = new ArrayList<>();
                        for (Infos.EntityInhibitInfo entityInhibitInfo : entityInhibitInfoSeq) {
                            entityInhibitAttributes.add(entityInhibitInfo.getEntityInhibitAttributes());
                        }
                        whatNextAttributes.setEntityInhibitions(entityInhibitAttributes);
                    }

                    if (CimArrayUtils.isNotEmpty(entityInhibitInfoSeq)) {
                        if (select_criteria_CANBEPROCESSED_OR_EQPMONNOKIT_OR_AUTO3) {
                            return null;
                        }
                    }


                    //*****************************//
                    // Pilot run flag              //
                    //*****************************//
                    String processOperationSpecificationObj = processOperation.getProcessOperationSpecificationObj();
                    CimProcessOperationSpecification processOperationSpecificationBO = baseCoreFactory.getBO(CimProcessOperationSpecification.class, processOperationSpecificationObj);
                    if (null == processOperationSpecificationBO) {
                        whatNextAttributes.setPilotRunFlag(false);
                    } else {
                        if (processOperationSpecificationBO.isPilotRunRequired() && CimArrayUtils.isNotEmpty(processOperationSpecificationBO.getPilotRunSectionInfo())) {
                            whatNextAttributes.setPilotRunFlag(true);
                        }
                    }

                    //check capability
                    //get eqpCapability
                    MachineDTO.EqpCapabilityInfo eqpCapabilities = aMachine.getEqpCapabilities();
                    List<MachineDTO.EqpCapabilityDetail> eqpCapabilityDetailList = eqpCapabilities.getEqpCapabilityDetailList();
                    //get Lot req Capability
                    String capabilityReq = processDefinition.getCapabilityReq();

                    if (CimStringUtils.isEmpty(capabilityReq)) {
                        whatNextAttributes.setCapabilityFlag(true);
                    } else {
                        whatNextAttributes.setCapabilityFlag(false);
                        for (MachineDTO.EqpCapabilityDetail eqpCapabilityDetail : eqpCapabilityDetailList) {
                            if (CimStringUtils.equals(eqpCapabilityDetail.getEqpCapability(), capabilityReq)) {
                                whatNextAttributes.setCapabilityFlag(true);
                                break;
                            }
                        }
                    }

                    return whatNextAttributes;
                })
                .filter(Objects::nonNull)
                .limit(StandardProperties.OM_WHATS_NEXT_MAX_COUNT.getIntValue())
                .collect(Collectors.toList());
        if (CimArrayUtils.isNotEmpty(whatNextAttributesList)) {
            out.setStrWhatNextAttributes(chamberLevelRecipeMethod.whatNextAttributesListChamberCheckRpt(objCommon,
                    new ChamberLevelRecipeWhatNextParam(equipmentID, whatNextAttributesList)));
        } else {
            out.setStrWhatNextAttributes(whatNextAttributesList);
        }

        return out;
    }
}
