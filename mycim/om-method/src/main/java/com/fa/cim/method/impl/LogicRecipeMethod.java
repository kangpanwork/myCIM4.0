package com.fa.cim.method.impl;

import com.fa.cim.annotaion.OmMethod;
import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.OmCode;
import com.fa.cim.common.support.RetCode;
import com.fa.cim.common.utils.*;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Inputs;
import com.fa.cim.dto.Outputs;
import com.fa.cim.dto.Params;
import com.fa.cim.entity.runtime.eqpstate.CimEquipmentStateDO;
import com.fa.cim.entity.runtime.logicalrecipe.CimLogicalRecipeDO;
import com.fa.cim.entity.runtime.logicalrecipe.CimLogicalRecipeDSetFxtrDO;
import com.fa.cim.entity.runtime.logicalrecipe.CimLogicalRecipeDSetPrstDO;
import com.fa.cim.entity.runtime.logicalrecipe.CimLogicalRecipeDSetRcpParmDO;
import com.fa.cim.entity.runtime.lot.CimLotDO;
import com.fa.cim.entity.runtime.mrecipe.CimMachineRecipeDO;
import com.fa.cim.entity.runtime.prcrsc.CimProcessResourceDO;
import com.fa.cim.entity.runtime.processdefinition.CimPDLcRecipeDO;
import com.fa.cim.entity.runtime.processdefinition.CimPDLcRecipeProductGroupDO;
import com.fa.cim.entity.runtime.processdefinition.CimPDLcRecipeTechDO;
import com.fa.cim.entity.runtime.processdefinition.CimProcessDefinitionDO;
import com.fa.cim.entity.runtime.productgroup.CimProductGroupDO;
import com.fa.cim.entity.runtime.productspec.CimProductSpecificationDO;
import com.fa.cim.jpa.CimJpaRepository;
import com.fa.cim.method.*;
import com.fa.cim.newcore.bo.globalfunc.CimFrameWorkGlobals;
import com.fa.cim.newcore.bo.machine.CimMachine;
import com.fa.cim.newcore.bo.pd.CimProcessDefinition;
import com.fa.cim.newcore.bo.prodspec.CimProductSpecification;
import com.fa.cim.newcore.bo.product.CimLot;
import com.fa.cim.newcore.bo.recipe.CimLogicalRecipe;
import com.fa.cim.newcore.bo.recipe.CimMachineRecipe;
import com.fa.cim.newcore.bo.restrict.RestrictionManager;
import com.fa.cim.newcore.dto.recipe.RecipeDTO;
import com.fa.cim.newcore.dto.restriction.Constrain;
import com.fa.cim.newcore.factory.BaseCoreFactory;
import com.fa.cim.newcore.impl.bo.env.StandardProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2018/6/26        ********             Bear               create file
 *
 * @author: Bear
 * @date: 2018/6/26 14:36
 * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@OmMethod
@Slf4j
public class LogicRecipeMethod  implements ILogicalRecipeMethod {

    @Autowired
    private RetCodeConfig retCodeConfig;

    @Autowired
    private ICimComp cimComp;

    @Autowired
    private ILotMethod lotMethod;

    @Autowired
    private CimJpaRepository cimJpaRepository;

//    @Autowired
//    @Qualifier("EntityInhibitManagerCore")
//    private EntityInhibitManager entityInhibitManager;
    @Autowired
    private RestrictionManager entityInhibitManager;

    @Autowired
    private IMachineRecipeMethod machineRecipeMethod;

    @Autowired
    private BaseCoreFactory  baseCoreFactory;

    @Autowired
    private CimFrameWorkGlobals cimFrameWorkGlobals;

    @Autowired
    private IConstraintMethod constraintMethod;

    @Override
    public Infos.DefaultRecipeSetting logicalRecipeDefaultRecipeSettingGetDR(Infos.ObjCommon objCommon,
                                                                             ObjectIdentifier lotID,
                                                                             ObjectIdentifier equipmentID,
                                                                             ObjectIdentifier logicalRecipeID,
                                                                             Boolean processResourceReqFlag,
                                                                             Boolean recipeParameterReqFlag,
                                                                             Boolean fixtureRequireFlag) {
        long count = 0 ;
        Infos.DefaultRecipeSetting defaultRecipeSetting = new Infos.DefaultRecipeSetting();
        /*---------------------------------------------*/
        /*   Get all DefaultSettinginfo from OMLRCP    */
        /*---------------------------------------------*/
        List<Infos.DefaultRecipeSetting> strLogicalRecipeAllDefaultRecipeSettingGetDROut =
                this.logicalRecipeAllDefaultRecipeSettingGetDR(objCommon, logicalRecipeID, processResourceReqFlag, recipeParameterReqFlag, fixtureRequireFlag);
        List<String> subLotTypes = new ArrayList<>();
        int searchCondition = 0;
        String searchConditionVar = StandardProperties.OM_CONSTRAINT_CHK_WITH_SEARCH.getValue();
        if( CimStringUtils.isNotEmpty(searchConditionVar)) {
            searchCondition = Integer.valueOf(searchConditionVar);
        }

        CimLotDO lot = null;
        if( searchCondition == 1 ) {
            /*------------------------------*/
            /*   Get ProductID, subLotTypes */
            /*------------------------------*/
            CimLotDO example = new CimLotDO();
            example.setLotID(ObjectIdentifier.fetchValue(lotID));
            lot = cimJpaRepository.findOne(Example.of(example)).orElse(null);
            Validations.check(null == lot, retCodeConfig.getNotFoundLot());

            subLotTypes.add(lot.getSubLotType());
        }

        int defaultSetLen = CimArrayUtils.getSize(strLogicalRecipeAllDefaultRecipeSettingGetDROut);
        for(int i = 0 ; i < defaultSetLen ; i++){
            Infos.DefaultRecipeSetting tmpDSet = strLogicalRecipeAllDefaultRecipeSettingGetDROut.get(i);

            /*---------------------------*/
            /*   Get info from OMRCP    */
            /*---------------------------*/
            List<ObjectIdentifier> strMachineRecipeAllEquipmentGetDROut = machineRecipeMethod.machineRecipeAllEquipmentGetDR(objCommon, tmpDSet.getRecipe());

            Boolean usedFlag = false;
            int eqpLen = CimArrayUtils.getSize(strMachineRecipeAllEquipmentGetDROut);
            for(int j = 0 ; j < eqpLen ; j++){
                /*---------------------------------------------------------------------*/
                /* Check if the MachineRecipe can be used by the specified Machine.    */
                /*---------------------------------------------------------------------*/
                if(ObjectIdentifier.equalsWithValue(strMachineRecipeAllEquipmentGetDROut.get(j), equipmentID)) {
                    log.info("#### inparaEqp exist. usedFlag == TRUE");
                    usedFlag = true;
                    break ;
                }
            }

            if(usedFlag) {
                int chamberLen = CimArrayUtils.getSize(tmpDSet.getChamberSeq());
                Boolean sameProcRscFlag = true;

                /*-------------------------------------------------------------*/
                /* Check if the chamber status is matching with equipment .    */
                /*-------------------------------------------------------------*/
                for(int k = 0 ; k < chamberLen ; k++) {

                    CimProcessResourceDO example = new CimProcessResourceDO();
                    example.setProcessResourceId(ObjectIdentifier.fetchValue(tmpDSet.getChamberSeq().get(k).getChamberID()));
                    example.setEquipmentID(ObjectIdentifier.fetchValue(equipmentID));
                    CimProcessResourceDO processResource = cimJpaRepository.findOne(Example.of(example)).orElse(null);
                    Validations.check(null == processResource, retCodeConfig.getNotFoundReqChamber());

                    CimEquipmentStateDO equipmentState = cimJpaRepository.queryOne("SELECT * FROM OMEQPST WHERE EQP_STATE_ID = ?1",
                            CimEquipmentStateDO.class, processResource.getEquipmentStateId());
                    Validations.check(null == equipmentState, retCodeConfig.getNotFoundEqpState());
                    if(CimBooleanUtils.isTrue(equipmentState.getCondtnAvailableFlag())) {

                        count = cimJpaRepository.count("SELECT COUNT(OMEQPST_SLTYP.ID)\n" +
                                "                        FROM   OMLOT, OMEQPST_SLTYP\n" +
                                "                        WHERE  OMLOT.LOT_ID= ?1 \n" +
                                "                        AND    OMEQPST_SLTYP.SUB_LOT_TYPE = OMLOT.SUB_LOT_TYPE\n" +
                                "                        AND    OMEQPST_SLTYP.REFKEY= ?2", ObjectIdentifier.fetchValue(lotID), equipmentState.getId());
                    }

                    if(CimBooleanUtils.isTrue(tmpDSet.getChamberSeq().get(k).getState())) {
                        if(CimBooleanUtils.isTrue(equipmentState.getCondtnAvailableFlag())) {
                            if(0 == count) {
                                sameProcRscFlag = false;
                                break;
                            }
                        } else if (!CimBooleanUtils.isTrue(equipmentState.getAvailableFlag())) {
                            sameProcRscFlag = false;
                            break;
                        }
                    } else {
                        if(CimBooleanUtils.isTrue(equipmentState.getCondtnAvailableFlag())) {
                            if(0 != count) {
                                sameProcRscFlag = false;
                                break;
                            }
                        } else if (CimBooleanUtils.isTrue(equipmentState.getAvailableFlag())) {
                            sameProcRscFlag = false;
                            break;
                        }
                    }
                }

                /*-------------------------------------------------------------------------------------------*/
                /*   Set return Structure if all of the chamber statuses are matching with equipment's one.  */
                /*-------------------------------------------------------------------------------------------*/
                if(sameProcRscFlag) {
                    if( searchCondition == 1 ) {
                        //-------------------------------------------------------------
                        // Check Entity Inhibit for ProductID + EquipmentID + ChamberID + RecipeID
                        //-------------------------------------------------------------
                        int chamberSize = CimArrayUtils.getSize(tmpDSet.getChamberSeq());

                        List<Constrain.EntityIdentifier> entities = new ArrayList<>();
                        // Set ProductID
                        if( CimStringUtils.isNotEmpty(lot.getProductSpecificationID()) ) {
                            entities.add(new Constrain.EntityIdentifier(BizConstant.SP_INHIBITCLASSID_PRODUCT, lot.getProductSpecificationID(), ""));
                        }

                        // Set EquipmentID
                        if(!ObjectIdentifier.isEmpty(equipmentID)) {
                            entities.add(new Constrain.EntityIdentifier(BizConstant.SP_INHIBITCLASSID_EQUIPMENT, ObjectIdentifier.fetchValue(equipmentID), ""));
                        }

                        // Set RecipeID
                        if( !ObjectIdentifier.isEmpty(tmpDSet.getRecipe())) {
                            String versionID = BaseStaticMethod.extractVersionFromID(ObjectIdentifier.fetchValue(tmpDSet.getRecipe()));
                            if( CimStringUtils.equals(versionID,BizConstant.SP_ACTIVE_VERSION) ) {

                                CimMachineRecipeDO machineRecipe = cimJpaRepository.queryOne("SELECT * FROM OMRCP WHERE RECIPE_ID = ?1", CimMachineRecipeDO.class, ObjectIdentifier.fetchValue(tmpDSet.getRecipe()));
                                Validations.check(null == machineRecipe, retCodeConfig.getNotFoundMachine());

                                if( CimStringUtils.isNotEmpty(machineRecipe.getActiveID())) {
                                    entities.add(new Constrain.EntityIdentifier(BizConstant.SP_INHIBITCLASSID_MACHINERECIPE, machineRecipe.getActiveID(), ""));
                                }
                            } else {
                                entities.add(new Constrain.EntityIdentifier(BizConstant.SP_INHIBITCLASSID_MACHINERECIPE, ObjectIdentifier.fetchValue(tmpDSet.getRecipe()), ""));
                            }
                        }

                        // Set ChamberID
                        for(int j=0; j < chamberSize; j++) {
                            if( !tmpDSet.getChamberSeq().get(j).getState() ) {
                                continue;
                            }
                            if( !ObjectIdentifier.isEmpty(tmpDSet.getChamberSeq().get(j).getChamberID()))  {
                                entities.add(new Constrain.EntityIdentifier(BizConstant.SP_INHIBITCLASSID_CHAMBER, ObjectIdentifier.fetchValue(equipmentID), ObjectIdentifier.fetchValue(tmpDSet.getChamberSeq().get(j).getChamberID())));
                            }
                        }

                        // Check EntityInhibits
                        List<Constrain.EntityInhibitRecord> inhibitRecordsVar = entityInhibitManager.allEntityInhibitRecordsForLotEntities(entities, subLotTypes);


                        int uLen = CimArrayUtils.getSize(inhibitRecordsVar);
                        if ( uLen > 0 ) {
                            List<Infos.EntityInhibitInfo> entityInhibitInfos = new ArrayList<>();

                            constraintMethod.setEntityInhibitRecordsToEntityInhibitInfos(entityInhibitInfos, inhibitRecordsVar);
                            // filter exception for entity inhibit
                            List<Infos.EntityInhibitInfo> strEntityInhibitFilterExceptionLotOut = constraintMethod.constraintFilterExceptionLot(objCommon, lotID, entityInhibitInfos);

                            uLen = CimArrayUtils.getSize(strEntityInhibitFilterExceptionLotOut);
                        }
                        if( uLen > 0 ) {
                            continue;
                        }
                    }

                    //------------------------------------------
                    //  Check active version control.
                    //------------------------------------------
                    String versionID = BaseStaticMethod.extractVersionFromID(ObjectIdentifier.fetchValue(tmpDSet.getRecipe()));
                    if(CimStringUtils.equals(versionID,BizConstant.SP_ACTIVE_VERSION)) {
                        CimMachineRecipeDO machineRecipe = cimJpaRepository.queryOne("SELECT * FROM OMRCP WHERE RECIPE_ID = ?1",CimMachineRecipeDO.class, ObjectIdentifier.fetchValue(tmpDSet.getRecipe()));
                        Validations.check(null == machineRecipe, retCodeConfig.getNotFoundMachine());

                        tmpDSet.setRecipe(ObjectIdentifier.build(machineRecipe.getActiveID(),machineRecipe.getActiveObj()));
                    }

                    defaultRecipeSetting = tmpDSet;
                    break;
                }
            }
        }
        /*----------------------*/
        /*   Return to caller   */
        /*----------------------*/

        return defaultRecipeSetting;
    }

    @Override
    public ObjectIdentifier processLogicalRecipeGetDR(Infos.ObjCommon objCommon, ObjectIdentifier productID, ObjectIdentifier operationID) {
        ObjectIdentifier logicalRecipe = null;
        Validations.check(ObjectIdentifier.isEmptyWithValue(productID) || ObjectIdentifier.isEmptyWithValue(operationID), retCodeConfig.getInvalidInputParam());
        CimProcessDefinitionDO processDefinition = cimJpaRepository.queryOne("SELECT * FROM OMPRP WHERE PRP_ID = ?1 AND PRP_LEVEL = ?2",CimProcessDefinitionDO.class, ObjectIdentifier.fetchValue(operationID), BizConstant.SP_PD_FLOWLEVEL_OPERATION);
        Validations.check(null == processDefinition, retCodeConfig.getMsgNotFoundPd());

        CimPDLcRecipeDO processDefinitionLcRecipe = cimJpaRepository.queryOne("SELECT * FROM OMPRP_LRPRD WHERE REFKEY = ?1 AND PROD_ID = ?2",CimPDLcRecipeDO.class, processDefinition.getId(), ObjectIdentifier.fetchValue(productID));
        if (null != processDefinitionLcRecipe) {
            logicalRecipe = new ObjectIdentifier(processDefinitionLcRecipe.getRecipeID(), processDefinitionLcRecipe.getRecipeObj());
        } else {
            //--------------------
            //  Get ProductGrp
            //--------------------
            CimProductSpecificationDO productSpecification = cimJpaRepository.queryOne("SELECT * FROM OMPRODINFO WHERE PROD_ID = ?1",
                    CimProductSpecificationDO.class, ObjectIdentifier.fetchValue(productID));
            Validations.check(null == productSpecification, retCodeConfig.getNotFoundProductSpec());

            //------------------------------------------
            //  Get recipeID from product group level
            //------------------------------------------
            CimPDLcRecipeProductGroupDO pdLcRecipeProductGroup = cimJpaRepository.queryOne("SELECT * FROM OMPRP_LRPRODFMLY WHERE REFKEY = ?1 AND PRODFMLY_ID = ?2",
                    CimPDLcRecipeProductGroupDO.class,
                    processDefinition.getId(),
                    productSpecification.getProductGroupID());
            if (null != pdLcRecipeProductGroup) {
                logicalRecipe = new ObjectIdentifier(pdLcRecipeProductGroup.getRecipeID(), pdLcRecipeProductGroup.getRecipeObj());
            } else {
                //---------------------
                //  Get Technology
                //---------------------
                CimProductGroupDO productGroup = cimJpaRepository.queryOne("SELECT * FROM OMPRODFMLY WHERE PRODFMLY_ID = ?1", CimProductGroupDO.class, productSpecification.getProductGroupID());
                Validations.check(null == productGroup, retCodeConfig.getNotFoundProductGroup());

                //------------------------------------------
                //  Get recipeID from technology level
                //------------------------------------------
                CimPDLcRecipeTechDO processDefinitionLcRecipeTech = cimJpaRepository.queryOne("SELECT * FROM OMPRP_LRTECH WHERE REFKEY = ?1 AND TECH_ID = ?2", CimPDLcRecipeTechDO.class, processDefinition.getId(), productGroup.getTechnologyID());
                if (null != processDefinitionLcRecipeTech) {
                    logicalRecipe = new ObjectIdentifier(processDefinitionLcRecipeTech.getRecipeID(), processDefinitionLcRecipeTech.getRecipeObj());
                } else {
                    //-----------------------------------
                    //  Set default recipeID
                    //-----------------------------------
                    logicalRecipe = new ObjectIdentifier(processDefinition.getRecipeID(), processDefinition.getRecipeObj());
                }
            }
        }

        //------------------------------------------
        //  Check active version control.
        //------------------------------------------
        String versionID = BaseStaticMethod.extractVersionFromID(ObjectIdentifier.fetchValue(logicalRecipe));
        if (CimStringUtils.equals(BizConstant.SP_ACTIVE_VERSION,versionID)) {
            CimLogicalRecipeDO logicalRecipeDO = cimJpaRepository.queryOne("SELECT * FROM OMLRCP WHERE LRCP_ID = ?1", CimLogicalRecipeDO.class, ObjectIdentifier.fetchValue(logicalRecipe));
            Validations.check(null == logicalRecipeDO, retCodeConfig.getNotFoundLogicalRecipe());
            logicalRecipe = new ObjectIdentifier(logicalRecipeDO.getActiveID(),logicalRecipeDO.getActiveObj());
        }
        return logicalRecipe;
    }

    @Override
    public Outputs.ObjLogicalRecipeCandidateChamberInfoGetByMachineRecipeOut logicalRecipeCandidateChamberInfoGetByMachineRecipe(
            Infos.ObjCommon objCommon, Inputs.ObjLogicalRecipeCandidateChamberInfoGetByMachineRecipeIn objLogicalRecipeCandidateChamberInfoGetByMachineRecipeIn) {

        Outputs.ObjLogicalRecipeCandidateChamberInfoGetByMachineRecipeOut result = new Outputs.ObjLogicalRecipeCandidateChamberInfoGetByMachineRecipeOut();

        ObjectIdentifier lotID = objLogicalRecipeCandidateChamberInfoGetByMachineRecipeIn.getLotID();
        ObjectIdentifier equipmentID = objLogicalRecipeCandidateChamberInfoGetByMachineRecipeIn.getEquipmentID();
        ObjectIdentifier logicalRecipeID = objLogicalRecipeCandidateChamberInfoGetByMachineRecipeIn.getLogicalRecipeID();
        ObjectIdentifier machineRecipeID = objLogicalRecipeCandidateChamberInfoGetByMachineRecipeIn.getMachineRecipeID();

        //【step1】get all default recipe setting
        log.debug("【step1】get all default recipe setting");
        CimLogicalRecipe logicalRecipe = baseCoreFactory.getBO(CimLogicalRecipe.class, logicalRecipeID);
        Validations.check(null == logicalRecipe, retCodeConfig.getNotFoundLogicalRecipe());

        //【step2】check multi chamber support of logical recipe
        log.debug("【step2】check multi chamber support of logical recipe");
        boolean multiChamberSupportedFlag = logicalRecipe.isMultipleChamberSupported();
        result.setRecipeDefinedFlag(false);
        result.setMultiChamberFlag(multiChamberSupportedFlag);

        List<Infos.CandidateChamber> candidateChamberList = new ArrayList<>();
        result.setCandidateChamberList(candidateChamberList);

        //【step3】get all default recipe setting
        log.debug("【step3】get all default recipe setting");
        List<RecipeDTO.DefaultRecipeSetting> defaultRecipeSettingList = logicalRecipe.getDefaultRecipeSettings();
        CimLot lot = baseCoreFactory.getBO(CimLot.class, lotID);
        Validations.check(null == lot, new OmCode(retCodeConfig.getNotFoundLot(), lotID.getValue()));

        //【step4】get subLotType
        log.debug("【step4】get subLotType");
        String subLotType = lot.getSubLotType();
        ObjectIdentifier productID = null;
        List<String> subLotTypeList = new ArrayList<>();
        if (objLogicalRecipeCandidateChamberInfoGetByMachineRecipeIn.isInhibitCheckFlag()) {
            log.debug("Prepare for inhibit check.");
            CimProductSpecification productSpecification = lot.getProductSpecification();
            Validations.check(null == productSpecification, retCodeConfig.getNotFoundProductSpec());
            productID = productSpecification.getProductSpecID();
            subLotTypeList.add(subLotType);
        }

        CimMachine equipment = baseCoreFactory.getBO(CimMachine.class, equipmentID);
        Validations.check(null == equipment, new OmCode(retCodeConfig.getNotFoundEqp(),equipmentID.getValue()));

        CimMachineRecipe machineRecipe = null;
        int defaultSettingSize = CimArrayUtils.isEmpty(defaultRecipeSettingList) ? 0 : defaultRecipeSettingList.size();
        int nSetCount = 0;
        for (int i = 0; i < defaultSettingSize ; i++) {
            RecipeDTO.DefaultRecipeSetting defaultRecipeSettingObj = defaultRecipeSettingList.get(i);
            ObjectIdentifier recipeID = defaultRecipeSettingObj.getRecipe();
            machineRecipe = baseCoreFactory.getBO(CimMachineRecipe.class, recipeID);
            Validations.check(null == machineRecipe, retCodeConfig.getNotFoundMachineRecipe());
            String versionID = cimFrameWorkGlobals.extractVersionFromID(ObjectIdentifier.fetchValue(recipeID));
            if (CimStringUtils.equals(BizConstant.SP_ACTIVE_VERSION, versionID)) {
                log.debug("versionID is ##");
                com.fa.cim.newcore.bo.recipe.CimMachineRecipe activeObject = machineRecipe.getActiveObject();
                Validations.check(null == activeObject, retCodeConfig.getNotFoundMachineRecipe());
                recipeID = activeObject.getMachineRecipeID();
            } else {
                log.debug("versionID is ##");
            }
            //String refKey = defaultRecipeSettingObj.getReferenceKey();
            //List<CimLogicalRecipeDSetPrstDO> processResourceStateList = logicalRecipeCore.findLogicalRecipeDefaultSettingProcessResourceStateListByLogicalRecipeDefaultSettingRefKey(refKey);
            if (ObjectIdentifier.equalsWithValue(machineRecipeID, recipeID)) {
                result.setRecipeDefinedFlag(true);
                //【step6】check process resource state
                log.debug("【step5】check process resource state");
                if (CimArrayUtils.isEmpty(defaultRecipeSettingObj.getProcessResourceStates())) {
                    Infos.CandidateChamber candidateChamber = new Infos.CandidateChamber();
                    candidateChamberList.add(candidateChamber);
                    log.debug("#### Chamber condition for all chambers are Not Use");
                    if (objLogicalRecipeCandidateChamberInfoGetByMachineRecipeIn.isInhibitCheckFlag()) {
                        candidateChamber.setInhibitFlag(false);
                    }
                    nSetCount++;
                } else {
                    log.debug("#### MRCP has some Process Resource");
                    boolean sameProcessResourceFlag = equipment.isSameProcessResourceStatesForSubLotType(defaultRecipeSettingObj.getProcessResourceStates(), subLotType);
                    if (sameProcessResourceFlag) {
                        Infos.CandidateChamber candidateChamber = new Infos.CandidateChamber();
                        candidateChamberList.add(candidateChamber);
                        int chamberSize = CimArrayUtils.getSize(defaultRecipeSettingObj.getProcessResourceStates());
                        List<Infos.Chamber> chamberList = new ArrayList<>();
                        candidateChamber.setChamberList(chamberList);
                        for (int j = 0; j < chamberSize; j++) {
                            RecipeDTO.ProcessResourceState processResourceState = defaultRecipeSettingObj.getProcessResourceStates().get(j);
                            Infos.Chamber chamber = new Infos.Chamber();
                            chamberList.add(chamber);
                            chamber.setChamberID(new ObjectIdentifier(processResourceState.getProcessResourceName()));
                            chamber.setState(processResourceState.getState());
                        }
                        if (objLogicalRecipeCandidateChamberInfoGetByMachineRecipeIn.isInhibitCheckFlag()) {
                            //【step6】check entity inhibit for ProductID + EquipmentID + ChamberID + RecipeID
                            log.debug("【step6】check entity inhibit for ProductID + EquipmentID + ChamberID + RecipeID");
                            List<Constrain.EntityIdentifier> entityIdentifierList = new ArrayList<>();

                            //set productID
                            if (!ObjectIdentifier.isEmptyWithValue(productID)) {
                                Constrain.EntityIdentifier entityIdentifier = new Constrain.EntityIdentifier();
                                entityIdentifier.setClassName(BizConstant.SP_INHIBITCLASSID_PRODUCT);
                                entityIdentifier.setObjectId(ObjectIdentifier.fetchValue(productID));
                                entityIdentifierList.add(entityIdentifier);
                            }

                            // set equipmentID
                            if (!ObjectIdentifier.isEmptyWithValue(equipmentID)) {
                                Constrain.EntityIdentifier entityIdentifier = new Constrain.EntityIdentifier();
                                entityIdentifier.setClassName(BizConstant.SP_INHIBITCLASSID_EQUIPMENT);
                                entityIdentifier.setObjectId(ObjectIdentifier.fetchValue(equipmentID));
                                entityIdentifierList.add(entityIdentifier);
                            }

                            // set recipeID
                            if (!ObjectIdentifier.isEmptyWithValue(defaultRecipeSettingObj.getRecipe())) {
                                Constrain.EntityIdentifier entityIdentifier = new Constrain.EntityIdentifier();
                                entityIdentifier.setClassName(BizConstant.SP_INHIBITCLASSID_MACHINERECIPE);
                                entityIdentifier.setObjectId(ObjectIdentifier.fetchValue(recipeID));
                                entityIdentifierList.add(entityIdentifier);
                            }

                            // set chamberID
                            for (int k = 0; k < chamberSize; k++) {
                                RecipeDTO.ProcessResourceState processResourceState = defaultRecipeSettingObj.getProcessResourceStates().get(k);
                                if (!processResourceState.getState()) {
                                    log.debug("ship chamberID:%s", processResourceState.getProcessResourceName());
                                    continue;
                                }
                                if (!CimStringUtils.isEmpty(processResourceState.getProcessResourceName())) {
                                    Constrain.EntityIdentifier entityIdentifier = new Constrain.EntityIdentifier();
                                    entityIdentifier.setClassName(BizConstant.SP_INHIBITCLASSID_CHAMBER);
                                    entityIdentifier.setObjectId(ObjectIdentifier.fetchValue(equipmentID));
                                    entityIdentifier.setAttrib(processResourceState.getProcessResourceName());
                                    entityIdentifierList.add(entityIdentifier);
                                }
                            }

                            // check entityInhibits - line:340
                            List<Constrain.EntityInhibitRecord> inhibitRecordList = entityInhibitManager.allEntityInhibitRecordsForLotEntities(entityIdentifierList, subLotTypeList);
                            if (!CimArrayUtils.isEmpty(inhibitRecordList)) {
                                List<Infos.EntityInhibitInfo> entityInhibitInfoList = new ArrayList<>();
                                List<Infos.EntityInhibitInfo> out = constraintMethod.constraintFilterExceptionLot(objCommon, lotID, entityInhibitInfoList);
                                if (!CimArrayUtils.isEmpty(out)) {
                                    result.getCandidateChamberList().get(nSetCount).setInhibitFlag(false);
                                }
                            }

                        } else {
                            //Inhibit check is skipped
                            log.debug("inhibit check is skipped");
                            result.getCandidateChamberList().get(nSetCount).setInhibitFlag(false);
                        }
                        nSetCount++;
                    }
                }
            }
        }

        return result;
    }

    /**
      * description:
      * <p></p>
      * change history:
      * date             defect             person             comments
      * ---------------------------------------------------------------------------------------------------------------------
      * 2018/11/27                          Wind
      * @param objCommon
      * @param params
      * @return RetCode<List<Infos.RecipeParameterInfo>>
      * @author Wind
      * @date 2018/11/27 22:16
      */
    @Override
    public List<Infos.RecipeParameterInfo> logicalRecipeRecipeParameterInfoGetByPD(Infos.ObjCommon objCommon, Params.EqpRecipeParameterListInq params) {
        List<Infos.RecipeParameterInfo> recipeParameterInfoList = new ArrayList<>();

        CimLot lot = baseCoreFactory.getBO(CimLot.class, params.getLotID());
        CimMachine equipment = baseCoreFactory.getBO(CimMachine.class, params.getEquipmentID());
        CimMachineRecipe mRecipe = baseCoreFactory.getBO(CimMachineRecipe.class, params.getMachineRecipeID());

        //Get PD object
        boolean processDefinitionFound = true;
        ObjectIdentifier pdID = params.getPdID();
        CimProcessDefinition aProcessDefinition = baseCoreFactory.getBO(CimProcessDefinition.class, pdID);
        Validations.check(null == aProcessDefinition, retCodeConfig.getNotFoundProcessDefinition());

        //Get ProdSpec object
        CimProductSpecification aProdSpec = lot.getProductSpecification();
        //Get logicalrecipe object

        com.fa.cim.newcore.bo.recipe.CimLogicalRecipe aLogicalRecipe = aProcessDefinition.findLogicalRecipeFor(aProdSpec);
        Validations.check(null == aLogicalRecipe, retCodeConfig.getNotFoundLogicalRecipe());

        //Get SubLotType
        String subLotType = lot.getSubLotType();
        //Get Recipe Parameters
        List<RecipeDTO.RecipeParameter> logicalRecipeDefaultSettingRecipeParamList = aLogicalRecipe.findRecipeParametersForSubLotType(equipment,mRecipe,subLotType);
        int nLen = CimArrayUtils.getSize(logicalRecipeDefaultSettingRecipeParamList);
        for (int i = 0; i < nLen; i++) {
            RecipeDTO.RecipeParameter recipeParameter = logicalRecipeDefaultSettingRecipeParamList.get(i);
            Infos.RecipeParameterInfo recipeParameterInfo = new Infos.RecipeParameterInfo();
            recipeParameterInfo.setSequenceNumber(i);
            recipeParameterInfo.setParameterName(recipeParameter.getParameterName());
            recipeParameterInfo.setParameterUnit(recipeParameter.getUnit());
            recipeParameterInfo.setParameterDataType(recipeParameter.getDataType());
            recipeParameterInfo.setParameterLowerLimit(recipeParameter.getLowerLimit());
            recipeParameterInfo.setParameterUpperLimit(recipeParameter.getUpperLimit());
            recipeParameterInfo.setUseCurrentSettingValueFlag(recipeParameter.getUseCurrentValueFlag());
            recipeParameterInfo.setParameterTargetValue(recipeParameter.getDefaultValue());
            recipeParameterInfo.setParameterValue(recipeParameter.getDefaultValue());
            recipeParameterInfoList.add(recipeParameterInfo);
        }
        return recipeParameterInfoList;
    }

    @Override
    public ObjectIdentifier logicalRecipeMachineRecipeGetDR(Infos.ObjCommon objCommon, ObjectIdentifier logicalRecipeID, ObjectIdentifier lotID, ObjectIdentifier equipmentID) {
        ObjectIdentifier out = new ObjectIdentifier();

        Integer count = 0;
        //【step1】get info from OMLRCP
        log.debug("【step1】get info from OMLRCP");
        String sql = "SELECT OMLRCP_DFLT.RECIPE_ID,\n" +
                "       OMLRCP_DFLT.RECIPE_RKEY,\n" +
                "       OMLRCP.ID,\n" +
                "       OMLRCP_DFLT.IDX_NO\n" +
                "FROM   OMLRCP, OMLRCP_DFLT\n" +
                "WHERE  OMLRCP.LRCP_ID= ?1\n" +
                "AND    OMLRCP.ID = OMLRCP_DFLT.REFKEY\n" +
                "ORDER BY OMLRCP_DFLT.IDX_NO ";
        List<Object[]> logicalRecipeDO = cimJpaRepository.query(sql, ObjectIdentifier.fetchValue(logicalRecipeID));
        Validations.check(CimArrayUtils.isEmpty(logicalRecipeDO), retCodeConfig.getSystemError());


        int searchCondition = StandardProperties.OM_CONSTRAINT_CHK_WITH_SEARCH.getIntValue();
        List<String> subLotTypeList = new ArrayList<>();
        CimLotDO lotDO = null;
        if (1 == searchCondition) {
            //【step2】get prooductID, subLotTypes
            log.debug("【step2】get prooductID, subLotTypes");
            CimLotDO example = new CimLotDO();
            example.setLotID(ObjectIdentifier.fetchValue(lotID));
            lotDO = cimJpaRepository.findOne(Example.of(example)).orElse(null);
            Validations.check(null == lotDO, new OmCode(retCodeConfig.getNotFoundLot(), lotID.getValue()));
            subLotTypeList.add(lotDO.getSubLotType());
        }

        //【step3】get info from OMRCP
        int size  = CimArrayUtils.getSize(logicalRecipeDO);
        for (int i = 0; i < size; i++) {
            String MRCPID = logicalRecipeDO.get(i)[0].toString();
            log.debug("【step3】get info from OMRCP");
            String MRCPSql = "SELECT OMRCP.RECIPE_ID, OMRCP.ID, OMRCP_EQP.EQP_ID, OMRCP_EQP.EQP_RKEY FROM OMRCP, OMRCP_EQP " +
                    "WHERE OMRCP.RECIPE_ID= ?1 AND  OMRCP.ID = OMRCP_EQP.REFKEY";
            List<Object[]> objects = cimJpaRepository.query(MRCPSql, MRCPID);
            if (CimArrayUtils.isEmpty(objects)) {
                continue;
            }
            ObjectIdentifier machineRecipeID = null;
            ObjectIdentifier eqpID = null;
            Boolean usedFlag = false;
            for (int j = 0; j < CimArrayUtils.getSize(objects); j++) {
                machineRecipeID = new ObjectIdentifier((String)objects.get(j)[0], (String)objects.get(j)[1]);
                eqpID = new ObjectIdentifier((String)objects.get(j)[2], (String)objects.get(j)[3]);

                //【step4】check if the machineRecipe can be used by the  specified machine.
                log.debug("【step4】check if the machineRecipe can be used by the  specified machine.");
                if (ObjectIdentifier.equalsWithValue(eqpID, equipmentID)) {
                    usedFlag = true;
                    break;
                }
            }

            if (CimBooleanUtils.isTrue(usedFlag)) {
                String SeqNo = logicalRecipeDO.get(i)[3].toString();
                sql = " SELECT *  FROM OMLRCP_DFLT_PRST where REFKEY = ?1 AND LINK_MARKER = ?2";
                List<CimLogicalRecipeDSetPrstDO> cimLogicalRecipeDSetPrstDOList = cimJpaRepository.query(sql, CimLogicalRecipeDSetPrstDO.class, logicalRecipeID.getReferenceKey(), SeqNo);
                int setPrstSize = CimArrayUtils.getSize(cimLogicalRecipeDSetPrstDOList);
                Boolean sameProcessResourceFlag = true;

                List<Infos.Chamber> chamberList = new ArrayList<>();
                for (int j = 0; j < setPrstSize; j++) {
                    CimLogicalRecipeDSetPrstDO cimLogicalRecipeDSetPrstDO = cimLogicalRecipeDSetPrstDOList.get(j);
                    Infos.Chamber chamber = new Infos.Chamber();
                    chamber.setChamberID(new ObjectIdentifier(cimLogicalRecipeDSetPrstDO.getProcessResourceID()));
                    chamber.setState(cimLogicalRecipeDSetPrstDO.getState());
                    chamberList.add(chamber);

                    CimProcessResourceDO example = new CimProcessResourceDO();
                    example.setProcessResourceId(cimLogicalRecipeDSetPrstDO.getProcessResourceID());
                    example.setEquipmentID(ObjectIdentifier.fetchValue(equipmentID));
                    List<CimProcessResourceDO> cimProcessResourceDOList = cimJpaRepository.findAll(Example.of(example));

                    int processResourceDoListSize = CimArrayUtils.getSize(cimProcessResourceDOList);
                    for (int k = 0; k < processResourceDoListSize; k++) {
                        CimProcessResourceDO cimProcessResourceDO = cimProcessResourceDOList.get(k);
                        sql = "SELECT * FROM OMEQPST WHERE EQP_STATE_ID = ?";
                        CimEquipmentStateDO cimEquipmentStateDOList = cimJpaRepository.queryOne(sql, CimEquipmentStateDO.class, cimProcessResourceDO.getEquipmentStateId());
                        if (null == cimEquipmentStateDOList) {
                            continue;
                        }

                        if (CimBooleanUtils.isTrue(cimEquipmentStateDOList.getCondtnAvailableFlag())) {
                            sql = "SELECT COUNT(OMEQPST_SLTYP.ID) FROM OMLOT, OMEQPST_SLTYP WHERE OMLOT.LOT_ID = ? AND OMEQPST_SLTYP.SUB_LOT_TYPE = OMLOT.SUB_LOT_TYPE " +
                                    " OMEQPST_SLTYP.REFKEY = ?";
                            Object[]  objects1 = cimJpaRepository.queryOne(sql, lotID.getValue(), cimEquipmentStateDOList.getId());
                            count = Integer.parseInt((String) objects1[0]);
                        }

                        if (CimBooleanUtils.isTrue(cimLogicalRecipeDSetPrstDO.getState())) {
                            if (CimBooleanUtils.isTrue(cimEquipmentStateDOList.getCondtnAvailableFlag())) {
                                if (count == 0) {
                                    sameProcessResourceFlag = false;
                                }
                            } else if (!cimEquipmentStateDOList.getAvailableFlag()) {
                                sameProcessResourceFlag = false;
                            }
                        } else {
                            if (CimBooleanUtils.isTrue(cimEquipmentStateDOList.getCondtnAvailableFlag())) {
                                if (count > 0) {
                                    sameProcessResourceFlag = false;
                                }
                            } else if (cimEquipmentStateDOList.getAvailableFlag()) {
                                sameProcessResourceFlag = false;
                            }
                        }
                    }
                }

                if (CimBooleanUtils.isTrue(sameProcessResourceFlag)) {
                    if (searchCondition == 1) {
                        //【step5】check Entity Inhibit for ProductID + EquipmentID + ChamberID + RecipeID
                        log.debug("【step5】check Entity Inhibit for ProductID + EquipmentID + ChamberID + RecipeID");
                        List<Constrain.EntityIdentifier> entityIdentifiers = new ArrayList<>();
                        //【step5-1】set productID
                        if (!CimStringUtils.isEmpty(lotDO.getProductSpecificationID())) {
                            Constrain.EntityIdentifier entityIdentifier = new Constrain.EntityIdentifier();
                            entityIdentifier.setClassName(BizConstant.SP_INHIBITCLASSID_PRODUCT);
                            entityIdentifier.setObjectId(lotDO.getProductSpecificationID());
                            entityIdentifiers.add(entityIdentifier);
                        }

                        //【step5-1】set equipmentID
                        if (ObjectIdentifier.isNotEmptyWithValue(equipmentID)) {
                            Constrain.EntityIdentifier entityIdentifier = new Constrain.EntityIdentifier();
                            entityIdentifier.setClassName(BizConstant.SP_INHIBITCLASSID_EQUIPMENT);
                            entityIdentifier.setObjectId(ObjectIdentifier.fetchValue(equipmentID));
                            entityIdentifiers.add(entityIdentifier);
                        }

                        //【step5-2】set recipeID
                        if (ObjectIdentifier.isNotEmptyWithValue(machineRecipeID)) {
                            String version_ID = BaseStaticMethod.extractVersionFromID(machineRecipeID.getValue());
                            if (CimStringUtils.equals(BizConstant.SP_ACTIVE_VERSION, version_ID)) {
                                log.debug("machineRecipeID is under active version control. Set activeID");

                                CimMachineRecipeDO cimMachineRecipeDO = cimJpaRepository.queryOne("SELECT * FROM OMRCP WHERE RECIPE_ID = ?1", CimMachineRecipeDO.class, ObjectIdentifier.fetchValue(machineRecipeID));
                                if (null == cimMachineRecipeDO) {
                                    throw new ServiceException(new OmCode(retCodeConfig.getNotFoundMachineRecipe(), machineRecipeID.getValue()));
                                }
                                Constrain.EntityIdentifier entityIdentifier = new Constrain.EntityIdentifier();
                                entityIdentifier.setClassName(BizConstant.SP_INHIBITCLASSID_MACHINERECIPE);
                                entityIdentifier.setObjectId(cimMachineRecipeDO.getRecipeID());
                                entityIdentifiers.add(entityIdentifier);
                            } else {
                                Constrain.EntityIdentifier entityIdentifier = new Constrain.EntityIdentifier();
                                entityIdentifier.setClassName(BizConstant.SP_INHIBITCLASSID_MACHINERECIPE);
                                entityIdentifier.setObjectId(ObjectIdentifier.fetchValue(machineRecipeID));
                                entityIdentifiers.add(entityIdentifier);
                            }
                        }

                        //【step5-3】set chamber ID
                        for (int l = 0; l < CimArrayUtils.getSize(chamberList); l++) {
                            Infos.Chamber chamber1 = chamberList.get(l);
                            if (CimBooleanUtils.isFalse(chamber1.getState())) {
                                continue;
                            }

                            if (ObjectIdentifier.isNotEmptyWithValue(chamber1.getChamberID())) {
                                Constrain.EntityIdentifier entityIdentifier = new Constrain.EntityIdentifier();
                                entityIdentifier.setClassName(BizConstant.SP_INHIBITCLASSID_CHAMBER);
                                entityIdentifier.setObjectId(ObjectIdentifier.fetchValue(equipmentID));
                                entityIdentifier.setAttrib(chamber1.getChamberID().getValue());
                                entityIdentifiers.add(entityIdentifier);
                            }
                        }

                        //【step5-4】check entityInhibits
                        log.debug("【step5-4】check entityInhibits");

                        List<Constrain.EntityInhibitRecord> inhibitRecordList = entityInhibitManager.allEntityInhibitRecordsForLotEntities(entityIdentifiers, subLotTypeList);
                        int uLen = CimArrayUtils.getSize(inhibitRecordList);
                        if (uLen > 0){
                            List<Infos.EntityInhibitInfo> strEntityInhibitInfos = new ArrayList<>();
                            for (Constrain.EntityInhibitRecord entityInhibitRecord : inhibitRecordList) {
                                Infos.EntityInhibitInfo entityInhibitInfo = new Infos.EntityInhibitInfo();
                                entityInhibitInfo.setEntityInhibitID(new ObjectIdentifier(entityInhibitRecord.getId(), entityInhibitRecord.getReferenceKey()));
                                Infos.EntityInhibitAttributes entityInhibitAttributes = new Infos.EntityInhibitAttributes();
                                entityInhibitAttributes.setSubLotTypes(entityInhibitRecord.getSubLotTypes());
                                entityInhibitAttributes.setStartTimeStamp(CimDateUtils.getTimestampAsString(entityInhibitRecord.getStartTimeStamp()));
                                entityInhibitAttributes.setEndTimeStamp(CimDateUtils.getTimestampAsString(entityInhibitRecord.getEndTimeStamp()));
                                entityInhibitAttributes.setClaimedTimeStamp(CimDateUtils.getTimestampAsString(entityInhibitRecord.getChangedTimeStamp()));
                                entityInhibitAttributes.setReasonCode(ObjectIdentifier.fetchValue(entityInhibitRecord.getReasonCode()));
                                entityInhibitAttributes.setOwnerID(entityInhibitRecord.getOwner());
                                entityInhibitAttributes.setMemo(entityInhibitRecord.getClaimMemo());
                                List<Constrain.EntityIdentifier> entits = entityInhibitRecord.getEntities();
                                if(!CimObjectUtils.isEmpty(entits)){
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

                                entityInhibitInfo.setEntityInhibitAttributes(entityInhibitAttributes);
                                strEntityInhibitInfos.add(entityInhibitInfo);
                            }

                            Inputs.ObjEntityInhibiteffectiveForLotGetDRIn inhibiteffectiveForLotGetDRIn = new Inputs.ObjEntityInhibiteffectiveForLotGetDRIn();
                            inhibiteffectiveForLotGetDRIn.setStrEntityInhibitInfos(strEntityInhibitInfos);
                            inhibiteffectiveForLotGetDRIn.setLotID(lotID);
                            List<Infos.EntityInhibitInfo> entityInhibitInfos = constraintMethod.constraintEffectiveForLotGetDR(objCommon, inhibiteffectiveForLotGetDRIn.getStrEntityInhibitInfos(), inhibiteffectiveForLotGetDRIn.getLotID());
                            uLen = CimArrayUtils.getSize(entityInhibitInfos);
                        }
                        if (uLen > 0){
                            continue;
                        }
                    }

                    // check active version control.
                    String versionID = BaseStaticMethod.extractVersionFromID(machineRecipeID.getValue());
                    out = machineRecipeID;
                    if (CimStringUtils.equals(BizConstant.SP_ACTIVE_VERSION, versionID)) {
                        CimMachineRecipeDO cimMachineRecipeDO = cimJpaRepository.queryOne("SELECT * FROM OMRCP WHERE RECIPE_ID = ?1",CimMachineRecipeDO.class, ObjectIdentifier.fetchValue(machineRecipeID));
                        if (null == cimMachineRecipeDO) {
                            throw new ServiceException(new OmCode(retCodeConfig.getNotFoundMachineRecipe(), machineRecipeID.getValue()));
                        }
                        out = new ObjectIdentifier(cimMachineRecipeDO.getRecipeID(), cimMachineRecipeDO.getId());
                    }
                }
            }
            if (ObjectIdentifier.isNotEmptyWithValue(out)) {
                break;
            }
        }


        return out;
    }

    @Override
    public Infos.DefaultRecipeSetting logicalRecipeDefaultRecipeSettingGet(Infos.ObjCommon objCommon, ObjectIdentifier lotID, ObjectIdentifier equipmentID, ObjectIdentifier logicalRecipeID) {

        Infos.DefaultRecipeSetting out = new Infos.DefaultRecipeSetting();

        //【step1】get all default recipe setting
        log.debug("【step1】get all default recipe setting");
        CimLogicalRecipe cimLogicalRecipeDO = baseCoreFactory.getBO(CimLogicalRecipe.class, logicalRecipeID);
        Validations.check(null == cimLogicalRecipeDO, retCodeConfig.getNotFoundLogicalRecipe(), objCommon.getTransactionID());

        List<RecipeDTO.DefaultRecipeSetting> defaultRecipeSettingList = cimLogicalRecipeDO.getDefaultRecipeSettings();

        //【step2】get lot info
        log.debug("【step2】get lot info");
        CimLot lotDO = baseCoreFactory.getBO(CimLot.class, lotID);
        Validations.check(null == lotDO, retCodeConfig.getNotFoundLot(), objCommon.getTransactionID());

        //【step3】get subLotTypes
        String subLotType = lotDO.getSubLotType();
        List<String> subLotTypeList = new ArrayList<>();

        ObjectIdentifier productID = null;
        String searchCondition = StandardProperties.OM_CONSTRAINT_CHK_WITH_SEARCH.getValue();
        if (CimStringUtils.equals(searchCondition, "1")) {

            //【step4】get productID
            log.debug("【step4】get productID");

            CimProductSpecification productSpecificationDO = lotDO.getProductSpecification();
            Validations.check(null == productSpecificationDO, retCodeConfig.getNotFoundProductSpec(), objCommon.getTransactionID());
            productID = new ObjectIdentifier(productSpecificationDO.getIdentifier(), productSpecificationDO.getPrimaryKey());
            subLotTypeList.add(subLotType);
        }

        //【step5】get machine
        log.debug("【step5】get machine");
        CimMachine equipmentDO = baseCoreFactory.getBO(CimMachine.class, equipmentID);
        Validations.check(null == equipmentDO, retCodeConfig.getNotFoundEqp(), objCommon.getTransactionID());

        int size = CimArrayUtils.getSize(defaultRecipeSettingList);
        Boolean foudFlag = false;
        CimMachineRecipe machineRecipeDO = null;
        RecipeDTO.DefaultRecipeSetting defaultRecipeSetting = null;
        for (int i = 0; i < size; i++) {
            defaultRecipeSetting = defaultRecipeSettingList.get(i);
            machineRecipeDO = baseCoreFactory.getBO(CimMachineRecipe.class, defaultRecipeSetting.getRecipe());
            Validations.check(null == machineRecipeDO, retCodeConfig.getNotFoundMachineRecipe(), objCommon.getTransactionID());

            //【step5-1】specified machine in included in tihs MRCP or not
            log.debug("【step5-1】specified machine in included in tihs MRCP or not");
            Boolean usedFlag = machineRecipeDO.isUsedBy(equipmentDO);
            if (CimBooleanUtils.isFalse(usedFlag)) {
                continue;
            }

            //【step5-2】check process resource state
            log.debug("【step5-2】check process resource state");
            if (CimArrayUtils.isEmpty(defaultRecipeSetting.getProcessResourceStates())) {
                log.debug("machine recipe doesn't have process resource");
                foudFlag = true;
                break;
            }
            log.debug("machine recipe have process resource");
            Boolean sameProcessResourceFlag = false;

            sameProcessResourceFlag = equipmentDO.isSameProcessResourceStatesForSubLotType(defaultRecipeSetting.getProcessResourceStates(), subLotType);
            if (CimBooleanUtils.isTrue(sameProcessResourceFlag)) {
                log.debug("process resource state is matching...");
                if (CimStringUtils.equals(searchCondition, "1")) {
                    // check entity inhibit for productID + EquipmentID + ChamberID + RecipeID
                    log.debug("check entity inhibit for productID + EquipmentID + ChamberID + RecipeID");
                    List<RecipeDTO.ProcessResourceState> processResourceStates = defaultRecipeSetting.getProcessResourceStates();
                    List<Constrain.EntityIdentifier> entityIdentifierList = new ArrayList<>();

                    //set ProductID
                    if (!ObjectIdentifier.isEmptyWithValue(productID)) {
                        Constrain.EntityIdentifier entityIdentifier = new Constrain.EntityIdentifier();
                        entityIdentifier.setClassName(BizConstant.SP_INHIBITCLASSID_PRODUCT);
                        entityIdentifier.setObjectId(ObjectIdentifier.fetchValue(productID));
                        entityIdentifierList.add(entityIdentifier);
                    }

                    //set EquipmentID
                    if (!ObjectIdentifier.isEmptyWithValue(equipmentID)) {
                        Constrain.EntityIdentifier entityIdentifier = new Constrain.EntityIdentifier();
                        entityIdentifier.setClassName(BizConstant.SP_INHIBITCLASSID_EQUIPMENT);
                        entityIdentifier.setObjectId(ObjectIdentifier.fetchValue(equipmentID));
                        entityIdentifierList.add(entityIdentifier);
                    }

                    //set RecipeID
                    if (!ObjectIdentifier.isEmptyWithValue(defaultRecipeSetting.getRecipe())) {
                        String versionID = BaseStaticMethod.extractVersionFromID(ObjectIdentifier.fetchValue(defaultRecipeSetting.getRecipe()));
                        Constrain.EntityIdentifier entityIdentifier = new Constrain.EntityIdentifier();
                        entityIdentifier.setClassName(BizConstant.SP_INHIBITCLASSID_MACHINERECIPE);
                        if (BizConstant.equalsIgnoreCase(BizConstant.SP_ACTIVE_VERSION, versionID)) {
                            CimMachineRecipe activeObject = machineRecipeDO.getActiveObject();
                            Validations.check(null == activeObject, retCodeConfig.getNotFoundMachineRecipe(), objCommon.getTransactionID());
                            entityIdentifier.setObjectId(activeObject.getIdentifier());
                        } else {
                            entityIdentifier.setObjectId(ObjectIdentifier.fetchValue(defaultRecipeSetting.getRecipe()));
                        }
                        entityIdentifierList.add(entityIdentifier);
                    }

                    //set ChamberID
                    int processResourceStateListSize = CimArrayUtils.getSize(processResourceStates);
                    for (int j = 0; j < processResourceStateListSize; j++) {
                        if (CimBooleanUtils.isFalse(processResourceStates.get(j).getState())) {
                            continue;
                        }

                        if (!CimStringUtils.isEmpty(processResourceStates.get(j).getProcessResourceName())) {
                            Constrain.EntityIdentifier entityIdentifier = new Constrain.EntityIdentifier();
                            entityIdentifier.setClassName(BizConstant.SP_INHIBITCLASSID_CHAMBER);
                            entityIdentifier.setObjectId(ObjectIdentifier.fetchValue(equipmentID));
                            entityIdentifier.setAttrib(processResourceStates.get(j).getProcessResourceName());
                            entityIdentifierList.add(entityIdentifier);
                        }
                    }

                    // check entityInhibits
                    List<Constrain.EntityInhibitRecord> inhibitRecordList = entityInhibitManager.allEntityInhibitRecordsForLotEntities(entityIdentifierList, subLotTypeList);
                    int inhibitRecordListSize = CimArrayUtils.getSize(inhibitRecordList);
                    if (inhibitRecordListSize > 0) {
                        List<Infos.EntityInhibitInfo> entityInhibitInfoList = new ArrayList<>();
                        for (int j = 0; j < inhibitRecordListSize; j++) {
                            Constrain.EntityInhibitRecord entityInhibitRecord = inhibitRecordList.get(j);
                            Infos.EntityInhibitInfo entityInhibitInfo = new Infos.EntityInhibitInfo();

                            // set entityInhibitAttributes
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
                            entityInhibitInfo.setEntityInhibitAttributes(entityInhibitAttributes);
                            entityInhibitInfo.setEntityInhibitID(new ObjectIdentifier(entityInhibitRecord.getId(), entityInhibitRecord.getReferenceKey()));
                            entityInhibitAttributes.setSubLotTypes(entityInhibitRecord.getSubLotTypes());
                            entityInhibitAttributes.setStartTimeStamp(CimDateUtils.getTimestampAsString(entityInhibitRecord.getStartTimeStamp()));
                            entityInhibitAttributes.setEndTimeStamp(CimDateUtils.getTimestampAsString(entityInhibitRecord.getEndTimeStamp()));
                            entityInhibitAttributes.setClaimedTimeStamp(CimDateUtils.getTimestampAsString(entityInhibitRecord.getChangedTimeStamp()));
                            entityInhibitAttributes.setReasonCode(ObjectIdentifier.fetchValue(entityInhibitRecord.getReasonCode()));
                            entityInhibitAttributes.setOwnerID(entityInhibitRecord.getOwner());
                            entityInhibitAttributes.setMemo(entityInhibitRecord.getClaimMemo());
                            entityInhibitInfoList.add(entityInhibitInfo);
                        }

                        List<Infos.EntityInhibitInfo> objEntityInhibitFilterExceptionLotOut = constraintMethod.constraintFilterExceptionLot(objCommon, lotID, entityInhibitInfoList);

                        inhibitRecordListSize = CimArrayUtils.getSize(objEntityInhibitFilterExceptionLotOut);
                    }

                    if (inhibitRecordListSize > 0) {
                        continue;
                    }
                }

                foudFlag = true;
                break;
            }
        }

        //【step6】active version control
        log.debug("【step6】active version control");
        if (CimBooleanUtils.isTrue(foudFlag) && !ObjectIdentifier.isEmptyWithValue(defaultRecipeSetting.getRecipe())) {
            log.debug("active versionID check.");
            String versionID = BaseStaticMethod.extractVersionFromID(ObjectIdentifier.fetchValue(defaultRecipeSetting.getRecipe()));
            if (CimStringUtils.equals(BizConstant.SP_ACTIVE_VERSION, versionID)) {
                if (null == machineRecipeDO) {
                    throw new ServiceException(retCodeConfig.getNotFoundMachineRecipe());
                }
                CimMachineRecipe activeObject = machineRecipeDO.getActiveObject();
                if (null == activeObject) {
                    throw new ServiceException(retCodeConfig.getNotFoundMachineRecipe());
                }
                ObjectIdentifier recipe = new ObjectIdentifier(activeObject.getIdentifier(), activeObject.getPrimaryKey());
                defaultRecipeSetting.setRecipe(recipe);
            }
        }

        out.setRecipe(defaultRecipeSetting.getRecipe());
        out.setBinDefinition(defaultRecipeSetting.getBinDefinition());
        out.setDcDefinition(defaultRecipeSetting.getDcDefinition());
        out.setDcSpec(defaultRecipeSetting.getDcSpec());
        out.setSampleSpecification(defaultRecipeSetting.getSampleSpecification());

        List<Infos.RecipeParameter> recipeParameters = Optional.ofNullable(defaultRecipeSetting.getRecipeParameters()).map(data -> data.stream().map(recipeParameter -> {
            Infos.RecipeParameter parameter = new Infos.RecipeParameter();
            parameter.setParameterName(recipeParameter.getParameterName());
            parameter.setDefaultValue(recipeParameter.getDefaultValue());
            parameter.setDataType(recipeParameter.getDataType());
            parameter.setLowerLimit(recipeParameter.getLowerLimit());
            parameter.setTag(recipeParameter.getTag());
            parameter.setUnit(recipeParameter.getUnit());
            parameter.setUseCurrentValueFlag(recipeParameter.getUseCurrentValueFlag());
            parameter.setUpperLimit(recipeParameter.getUpperLimit());
            return parameter;
        }).collect(Collectors.toList())).orElseGet(Collections::emptyList);
        out.setRecipeParameters(recipeParameters);

        List<Infos.Chamber> chamberSeq = Optional.ofNullable(defaultRecipeSetting.getChamberSeq()).map(data -> data.stream().map(chamber -> {
            Infos.Chamber ch = new Infos.Chamber();
            ch.setState(chamber.getState());
            ch.setChamberID(chamber.getChamberID());
            return ch;
        }).collect(Collectors.toList())).orElseGet(Collections::emptyList);
        out.setChamberSeq(chamberSeq);

        out.setFixtureGroups(defaultRecipeSetting.getFixtureGroups());

        List<Infos.ProcessResourceState> processResourceStates = Optional.ofNullable(defaultRecipeSetting.getProcessResourceStates())
                .map(data -> data.stream().map(recipeSetting -> {
                    Infos.ProcessResourceState state = new Infos.ProcessResourceState();
                    state.setState(recipeSetting.getState());
                    state.setProcessResourceName(recipeSetting.getProcessResourceName());
                    return state;
                }).collect(Collectors.toList())).orElseGet(Collections::emptyList);
        out.setProcessResourceStates(processResourceStates);

        return out;
    }


    @Override
    public List<Infos.DefaultRecipeSetting> logicalRecipeAllDefaultRecipeSettingGetDR(Infos.ObjCommon objCommon,
                                                                                      ObjectIdentifier logicalRecipeID,
                                                                                      Boolean processResourceReqFlag,
                                                                                      Boolean recipeParameterReqFlag,
                                                                                      Boolean fixtureRequireFlag) {
        log.info("LogicalRecipeCore: findLogicalRecipeAllDefaultRecipeSettingGetDR(...)");
        /*----------------*/
        /*   Initialize   */
        /*----------------*/
        RetCode<List<Infos.DefaultRecipeSetting>> retCode = new RetCode<>();
        retCode.setReturnCode(retCodeConfig.getSucc());
        List<Infos.DefaultRecipeSetting> resultList = new ArrayList<>();
        /*---------------------------------------*/
        /*   Get info from OMLRCP/OMLRCP_DFLT    */
        /*---------------------------------------*/
        List<Object[]> logicalRecipeList = cimJpaRepository.query("SELECT OMLRCP_DFLT.RECIPE_ID,\n" +
                "       OMLRCP_DFLT.RECIPE_RKEY,\n" +
                "       OMLRCP.ID,\n" +
                "       OMLRCP_DFLT.IDX_NO,\n" +
                "       OMLRCP_DFLT.EDC_PLAN_ID,\n" +
                "       OMLRCP_DFLT.EDC_PLAN_RKEY,\n" +
                "       OMLRCP_DFLT.EDC_SPEC_ID,\n" +
                "       OMLRCP_DFLT.EDC_SPEC_RKEY,\n" +
                "       OMLRCP_DFLT.BIN_SETUP_ID,\n" +
                "       OMLRCP_DFLT.BIN_SETUP_RKEY,\n" +
                "       OMLRCP_DFLT.QSAMPLE_SPEC_ID,\n" +
                "       OMLRCP_DFLT.QSAMPLE_SPEC_RKEY\n" +
                "FROM   OMLRCP, OMLRCP_DFLT\n" +
                "WHERE  OMLRCP.LRCP_ID = ?1 \n" +
                "AND    OMLRCP.ID = OMLRCP_DFLT.REFKEY\n" +
                "ORDER BY OMLRCP_DFLT.IDX_NO", ObjectIdentifier.fetchValue(logicalRecipeID));

        for (Object[] loop : logicalRecipeList) {
            Infos.DefaultRecipeSetting defaultRecipeSetting = new Infos.DefaultRecipeSetting();
            defaultRecipeSetting.setRecipe(new ObjectIdentifier((String) loop[0], (String) loop[1]));
            defaultRecipeSetting.setDcDefinition(new ObjectIdentifier((String) loop[4], (String) loop[5]));
            defaultRecipeSetting.setDcSpec(new ObjectIdentifier((String) loop[6], (String) loop[7]));
            defaultRecipeSetting.setBinDefinition(new ObjectIdentifier((String) loop[8], (String) loop[9]));
            defaultRecipeSetting.setSampleSpecification(new ObjectIdentifier((String) loop[10], (String) loop[11]));

            if (processResourceReqFlag) {
                /*---------------------------*/
                /*   Get Process Resource    */
                /*---------------------------*/
                List<CimLogicalRecipeDSetPrstDO> logicalRecipeDefaultReqList = cimJpaRepository.query("SELECT * FROM OMLRCP_DFLT_PRST WHERE REFKEY = ?1 AND LINK_MARKER = ?2",
                        CimLogicalRecipeDSetPrstDO.class, loop[2].toString(), loop[3].toString());
                List<Infos.Chamber> chamberList = new ArrayList<>();
                for (CimLogicalRecipeDSetPrstDO logicalRecipeDefaultReq : logicalRecipeDefaultReqList) {
                    Infos.Chamber chamber = new Infos.Chamber();
                    chamber.setChamberID(new ObjectIdentifier(logicalRecipeDefaultReq.getProcessResourceID()));
                    chamber.setState(logicalRecipeDefaultReq.getState());
                    chamberList.add(chamber);
                }
                defaultRecipeSetting.setChamberSeq(chamberList);
            }
            if (recipeParameterReqFlag) {
                /*---------------------------*/
                /*   Get Recipe Parameter    */
                /*---------------------------*/

                List<CimLogicalRecipeDSetRcpParmDO> recipePraameterReqList = cimJpaRepository.query("SELECT * FROM OMLRCP_DFLT_RPARAM WHERE REFKEY = ?1 AND LINK_MARKER = ?2",
                        CimLogicalRecipeDSetRcpParmDO.class, loop[2].toString(), loop[3].toString());
                List<Infos.RecipeParameter> recipeParametersList = new ArrayList<>();
                for (CimLogicalRecipeDSetRcpParmDO recipePraameterReq : recipePraameterReqList) {
                    Infos.RecipeParameter recipeParameter = new Infos.RecipeParameter();
                    recipeParameter.setParameterName(recipePraameterReq.getRecipeParamName());
                    recipeParameter.setUnit(recipePraameterReq.getRecipeParamUnit());
                    recipeParameter.setDataType(recipePraameterReq.getRecipeParamDataType());
                    recipeParameter.setDefaultValue(recipePraameterReq.getRecipeParamDefault());
                    recipeParameter.setLowerLimit(recipePraameterReq.getRecipeParamLowerLimit());
                    recipeParameter.setUpperLimit(recipePraameterReq.getRecipeParamUpperLimit());
                    recipeParameter.setUseCurrentValueFlag(recipePraameterReq.getFlag());
                    recipeParametersList.add(recipeParameter);
                }
                defaultRecipeSetting.setRecipeParameters(recipeParametersList);
            }
            if (fixtureRequireFlag) {
                /*------------------*/
                /*   Get Fixture    */
                /*------------------*/
                List<CimLogicalRecipeDSetFxtrDO> fixTureRequireList = cimJpaRepository.query("SELECT * FROM OMLRCP_DFLT_FIXT WHERE REFKEY = ?1 AND LINK_MARKER = ?2", CimLogicalRecipeDSetFxtrDO.class, loop[2].toString(), loop[3].toString());
                List<ObjectIdentifier> fixtureGroupsList = new ArrayList<>();
                for (CimLogicalRecipeDSetFxtrDO fixTureRequire : fixTureRequireList) {
                    ObjectIdentifier objectIdentifier = new ObjectIdentifier();
                    objectIdentifier.setValue(fixTureRequire.getFixtureID());
                    objectIdentifier.setReferenceKey(fixTureRequire.getObjRef());
                    fixtureGroupsList.add(objectIdentifier);
                }
                defaultRecipeSetting.setFixtureGroups(fixtureGroupsList);
            }
            resultList.add(defaultRecipeSetting);
        }
        return resultList;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/11/8                          Wind
     *
     * @param objCommon
     * @param operationID
     * @return RetCode<ObjectIdentifier>
     * @author Wind
     * @date 2018/11/8 17:24
     */
    @Override
    public ObjectIdentifier processDefaultLogicalRecipeGetDR(Infos.ObjCommon objCommon, ObjectIdentifier operationID) {
        ObjectIdentifier result = new ObjectIdentifier();
        CimProcessDefinitionDO processDefinition = cimJpaRepository.queryOne("SELECT LRCP_ID, LRCP_RKEY FROM OMPRP WHERE PRP_ID = ?1 AND PRP_LEVEL = ?2",
                CimProcessDefinitionDO.class, operationID.getValue(), BizConstant.SP_PD_FLOWLEVEL_OPERATION);
        result.setValue(processDefinition.getRecipeID());
        result.setReferenceKey(processDefinition.getRecipeObj());

        //------------------------------------------
        //  Check active version control.
        //------------------------------------------
        String versionID = BaseStaticMethod.extractVersionFromID(result.getValue());
        if (!CimStringUtils.equals(versionID, BizConstant.SP_ACTIVE_VERSION)) {
            CimLogicalRecipeDO logicalRecipe = cimJpaRepository.queryOne("SELECT ACTIVE_VER, ACTIVE_RKEY FROM OMLRCP WHERE LRCP_ID = ?1",
                    CimLogicalRecipeDO.class, result.getValue());
            result.setValue(logicalRecipe.getActiveID());
            result.setReferenceKey(logicalRecipe.getActiveObj());
        }
        /*----------------------*/
        /*   Return to Caller   */
        /*----------------------*/
        return result;
    }

    @Override
    public Outputs.ObjLogicalRecipeMachineRecipeForSubLotTypeGetDROut logicalRecipeMachineRecipeForSubLotTypeGetDR(Infos.ObjCommon objCommon,
                                                                                                                   Inputs.ObjLogicalRecipeMachineRecipeForSubLotTypeGetDRIn in) {
        Outputs.ObjLogicalRecipeMachineRecipeForSubLotTypeGetDROut out = new Outputs.ObjLogicalRecipeMachineRecipeForSubLotTypeGetDROut();
        List<Infos.EqpChamberAvailableInfo> strEqpChamberAvailableInfoSeq = in.getStrEqpChamberAvailableInfoSeq();
        int eqpChamberLen = strEqpChamberAvailableInfoSeq.size();
        List<Infos.Chamber> chamberSeq = new ArrayList<>(eqpChamberLen);
        out.setChamberSeq(chamberSeq);
        String lrcpTableCursor =
                "SELECT  LRCP.ID,\n" +
                "        LRCP_DSET.IDX_NO,\n" +
                "        MRCP.RECIPE_ID,\n" +
                "        MRCP.RECIPE_RKEY\n" +
                "FROM    OMRCP      MRCP\n" +
                "INNER   JOIN OMRCP_EQP  MRCP_EQP  on MRCP_EQP.REFKEY = MRCP.ID\n" +
                "INNER   JOIN OMLRCP_DFLT LRCP_DSET on LRCP_DSET.RECIPE_ID     = MRCP.RECIPE_ID\n" +
                "INNER   JOIN OMLRCP      LRCP      on LRCP.ID     = LRCP_DSET.REFKEY\n" +
                "WHERE   LRCP.LRCP_ID = ?1\n" +
                "AND     MRCP_EQP.EQP_ID  = ?2\n" +
                "ORDER BY LRCP_DSET.IDX_NO";
        String equipmentId = ObjectIdentifier.fetchValue(in.getEquipmentID());
        String logicalRecipeId = ObjectIdentifier.fetchValue(in.getLogicalRecipeID());
        List<Object[]> lrcpTableCursorResult = cimJpaRepository.query(lrcpTableCursor, logicalRecipeId, equipmentId);
        for (Object[] result : lrcpTableCursorResult) {
            String hFRLRCPd_theSystemKey = (String) result[0];
            Integer hFRLRCP_DSETd_SeqNo = (Integer) result[1];
            String hFRMRCPRECIPE_ID = (String) result[2];
            String hFRMRCPRECIPE_OBJ = (String) result[3];

            boolean sameProcRscFlag = true;
            if (CimArrayUtils.isNotEmpty(strEqpChamberAvailableInfoSeq)) {
                String lrcpDsPtblCursor =
                        "SELECT PROCRSC_ID,\n" +
                                "       STATE\n" +
                                "FROM   OMLRCP_DFLT_PRST\n" +
                                "WHERE  REFKEY   = ?1\n" +
                                "AND    LINK_MARKER in (?2, ?3)";
                String theTableMarker = String.valueOf(hFRLRCP_DSETd_SeqNo);
                List<Object[]> LRCP_DS_PTBL2__150_result = cimJpaRepository.query(lrcpDsPtblCursor,
                        hFRLRCPd_theSystemKey,
                        theTableMarker,
                        theTableMarker);
                for (Object[] objects : LRCP_DS_PTBL2__150_result) {
                    String hFRLRCP_DSET_PRSTPROCRSC_ID = (String) objects[0];
                    Boolean hFRLRCP_DSET_PRSTSTATE = CimBooleanUtils.isTrue((Integer) objects[1]);

                    Infos.EqpChamberAvailableInfo eqpChamberAvailableInfo = find(strEqpChamberAvailableInfoSeq, hFRLRCP_DSET_PRSTPROCRSC_ID)
                            .orElseThrow(() -> Validations.buildException(retCodeConfig.getNotFoundReqChamber(),
                                    hFRLRCP_DSET_PRSTPROCRSC_ID, equipmentId));
                    int HV_COUNT = 0;
                    if (eqpChamberAvailableInfo.isConditionalAvailableFlag()) {
                        String dTheSystemKeyPos = ObjectIdentifier.fetchReferenceKey(eqpChamberAvailableInfo.getChamberStateCode());
                        String sql =
                                "SELECT  COUNT(OMEQPST_SLTYP.REFKEY)\n" +
                                        "FROM    OMEQPST_SLTYP\n" +
                                        "WHERE   OMEQPST_SLTYP.SUB_LOT_TYPE   = ?1\n" +
                                        "AND     OMEQPST_SLTYP.REFKEY = ?2";
                        Object[] sqlResult = cimJpaRepository.queryOne(sql, dTheSystemKeyPos, in.getSubLotType());
                        HV_COUNT = (Integer) sqlResult[0];
                        if (hFRLRCP_DSET_PRSTSTATE && HV_COUNT == 0) {
                            sameProcRscFlag = false;
                            break;
                        }
                        if (!hFRLRCP_DSET_PRSTSTATE && HV_COUNT > 0) {
                            sameProcRscFlag = false;
                            break;
                        }
                    } else {
                        if (hFRLRCP_DSET_PRSTSTATE && eqpChamberAvailableInfo.isAvailableFlag()) {
                            sameProcRscFlag = false;
                            break;
                        }
                        if (!hFRLRCP_DSET_PRSTSTATE && eqpChamberAvailableInfo.isAvailableFlag()) {
                            sameProcRscFlag = false;
                            break;
                        }
                    }
                    Infos.Chamber chamber = new Infos.Chamber();
                    chamber.setChamberID(ObjectIdentifier.build(hFRLRCP_DSET_PRSTPROCRSC_ID, ""));
                    chamber.setState(hFRLRCP_DSET_PRSTSTATE);
                    chamberSeq.add(chamber);
                }
            }
            if (!sameProcRscFlag) {
                continue;
            }
            String versionID = extractVersionFromID(hFRMRCPRECIPE_ID);
            if (CimStringUtils.equals(versionID, BizConstant.SP_ACTIVE_VERSION)) {
                String sql =
                        "SELECT  OMRCP.ACTIVE_ID,  OMRCP.ACTIVE_RKEY\n" +
                        "FROM    OMRCP\n" +
                        "WHERE   RECIPE_ID = ?1";
                Object[] sqlResult = cimJpaRepository.queryOne(sql, hFRMRCPRECIPE_ID);
                hFRMRCPRECIPE_ID = (String) sqlResult[0];
                hFRMRCPRECIPE_OBJ = (String) sqlResult[1];
            }
            String searchCondition = StandardProperties.OM_CONSTRAINT_CHK_WITH_SEARCH.getValue();
            if (CimStringUtils.equals(searchCondition, "1")) {
                List<Constrain.EntityIdentifier> entities = new ArrayList<>(chamberSeq.size() + 3);
                if (CimStringUtils.isNotEmpty(ObjectIdentifier.fetchValue(in.getProductID()))) {
                    entities.add(buildEntityIdentifier(BizConstant.SP_INHIBITCLASSID_PRODUCT, in.getProductID(), ""));
                }
                if (CimStringUtils.isNotEmpty(ObjectIdentifier.fetchValue(in.getEquipmentID()))) {
                    entities.add(buildEntityIdentifier(BizConstant.SP_INHIBITCLASSID_EQUIPMENT, in.getEquipmentID(), ""));
                }
                if (CimStringUtils.isNotEmpty(hFRMRCPRECIPE_ID)) {
                    entities.add(buildEntityIdentifier(BizConstant.SP_INHIBITCLASSID_MACHINERECIPE,
                            ObjectIdentifier.build(hFRMRCPRECIPE_ID, hFRMRCPRECIPE_OBJ), ""));
                }
                chamberSeq.forEach(chamber -> {
                    if (chamber.getState()) {
                        entities.add(buildEntityIdentifier(BizConstant.SP_INHIBITCLASSID_CHAMBER, in.getEquipmentID(),
                                ObjectIdentifier.fetchValue(chamber.getChamberID())));
                    }
                });
                List<String> subLotTypes = new ArrayList<>(1);
                subLotTypes.add(in.getSubLotType());
                List<Constrain.EntityInhibitRecord> inhibitRecords_var = entityInhibitManager.allEntityInhibitRecordsForLotEntities(entities, subLotTypes);
                int uLen = CimArrayUtils.getSize(inhibitRecords_var);
                if (uLen > 0) {
                    List<Infos.EntityInhibitInfo> entityInhibitInfos = new ArrayList<>(uLen);
                    for (Constrain.EntityInhibitRecord record : inhibitRecords_var) {
                        Infos.EntityInhibitInfo entityInhibitInfo = new Infos.EntityInhibitInfo();
                        entityInhibitInfos.add(entityInhibitInfo);
                        entityInhibitInfo.setEntityInhibitID(ObjectIdentifier.build(record.getId(), record.getReferenceKey()));
                        Infos.EntityInhibitAttributes entityInhibitAttributes = new Infos.EntityInhibitAttributes();
                        entityInhibitAttributes.setSubLotTypes(record.getSubLotTypes());
                        entityInhibitAttributes.setStartTimeStamp(record.getStartTimeStamp().toString());
                        entityInhibitAttributes.setEndTimeStamp(record.getEndTimeStamp().toString());
                        entityInhibitAttributes.setClaimedTimeStamp(record.getChangedTimeStamp().toString());
                        entityInhibitAttributes.setReasonCode(ObjectIdentifier.fetchValue(record.getReasonCode()));
                        entityInhibitAttributes.setOwnerID(record.getOwner());
                        entityInhibitAttributes.setMemo(record.getClaimMemo());
                        List<Constrain.EntityIdentifier> recordEntities = record.getEntities();
                        if (CimArrayUtils.isNotEmpty(recordEntities)) {
                            List<Infos.EntityIdentifier> entityIdentifiers = new ArrayList<>(CimArrayUtils.getSize(recordEntities));
                            recordEntities.forEach(entityIdentifier -> {
                                Infos.EntityIdentifier entity = new Infos.EntityIdentifier();
                                entity.setAttribution(entityIdentifier.getAttrib());
                                entity.setClassName(entityIdentifier.getClassName());
                                entity.setObjectID(new ObjectIdentifier(entityIdentifier.getObjectId()));
                                entityIdentifiers.add(entity);
                            });
                            entityInhibitAttributes.setEntities(entityIdentifiers);
                        } else {
                            entityInhibitAttributes.setEntities(Collections.emptyList());
                        }
                        entityInhibitInfo.setEntityInhibitAttributes(entityInhibitAttributes);
                        Inputs.ObjEntityInhibiteffectiveForLotGetDRIn inhibiteForLotIn = new Inputs.ObjEntityInhibiteffectiveForLotGetDRIn();
                        inhibiteForLotIn.setLotID(in.getLotID());
                        inhibiteForLotIn.setStrEntityInhibitInfos(entityInhibitInfos);
                        List<Infos.EntityInhibitInfo> inhibitInfos = constraintMethod.constraintEffectiveForLotGetDR(objCommon, inhibiteForLotIn.getStrEntityInhibitInfos(), inhibiteForLotIn.getLotID());
                        uLen = CimArrayUtils.getSize(inhibitInfos);
                    }
                }
                if (uLen > 0) {
                    continue;
                }
            }
            out.setMachineRecipeID(ObjectIdentifier.build(hFRMRCPRECIPE_ID, hFRMRCPRECIPE_OBJ));
            break;
        }
        return out;
    }

    @Override
    public ObjectIdentifier findBaseMachineRecipe(Infos.ObjCommon objCommon,
                                                  ObjectIdentifier logicalRecipeID,
                                                  ObjectIdentifier lotID,
                                                  ObjectIdentifier equpmentID) {
        CimLot aLot = baseCoreFactory.getBO(CimLot.class, lotID);
        Validations.check(null == aLot, retCodeConfig.getNotFoundLot());

        CimLogicalRecipe aLogicalRecipe = baseCoreFactory.getBO(CimLogicalRecipe.class, logicalRecipeID);
        Validations.check(null == aLogicalRecipe, retCodeConfig.getNotFoundLogicalRecipe());

        CimMachine aMachine = baseCoreFactory.getBO(CimMachine.class, equpmentID);
        Validations.check(null == aMachine, retCodeConfig.getNotFoundEqp(),
                ObjectIdentifier.fetchValue(equpmentID));


        int strSearchCondition = StandardProperties.OM_CONSTRAINT_CHK_WITH_SEARCH.getIntValue();
        CimMachineRecipe aMachineRecipe = null;
        if (strSearchCondition == 1) {
            aMachineRecipe = aLogicalRecipe.findMachineRecipeFor(aLot, aMachine);
        } else {
            aMachineRecipe = aLogicalRecipe.findMachineRecipeForSubLotType(aMachine, aLot.getSubLotType());
        }
        Validations.check(null == aMachineRecipe, retCodeConfig.getNotFoundMachineRecipe());
        return ObjectIdentifier.build(aMachineRecipe.getIdentifier(),aMachineRecipe.getPrimaryKey());
    }

    private Constrain.EntityIdentifier buildEntityIdentifier(String className, ObjectIdentifier objectIdentifier, String attrib) {
        Constrain.EntityIdentifier entity = new Constrain.EntityIdentifier();
        entity.setClassName(className);
        entity.setObjectId(ObjectIdentifier.fetchValue(objectIdentifier));
        entity.setAttrib(attrib);
        return entity;
    }

    private String extractVersionFromID (String identifier) {
        if (CimStringUtils.isEmpty(identifier))
            return "";
        return identifier.substring(identifier.indexOf("\\.") + 1);
    }

    private Optional<Infos.EqpChamberAvailableInfo> find(List<Infos.EqpChamberAvailableInfo> strEqpChamberAvailableInfoSeq, String target) {
        if (CimArrayUtils.isEmpty(strEqpChamberAvailableInfoSeq))
            return Optional.empty();
        for (Infos.EqpChamberAvailableInfo info : strEqpChamberAvailableInfoSeq) {
            if (CimStringUtils.equals(ObjectIdentifier.fetchValue(info.getChamberID()), target))
                return Optional.of(info);
        }
        return Optional.empty();
    }
}
