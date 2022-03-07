package com.fa.cim.method.impl;

import com.fa.cim.annotaion.OmMethod;
import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.constant.ConstraintClassEnum;
import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.OmCode;
import com.fa.cim.common.support.User;
import com.fa.cim.common.utils.*;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.config.RetCodeConfigEx;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Inputs;
import com.fa.cim.dto.Outputs;
import com.fa.cim.dto.Params;
import com.fa.cim.entity.nonruntime.CimEqpRecipeTimeDO;
import com.fa.cim.entity.runtime.code.CimCodeDO;
import com.fa.cim.entity.runtime.durable.CimDurableDO;
import com.fa.cim.entity.runtime.durablegroup.CimDurableGroupDO;
import com.fa.cim.entity.runtime.eqp.CimEquipmentDO;
import com.fa.cim.entity.runtime.lot.CimLotDO;
import com.fa.cim.entity.runtime.mrecipe.CimMachineRecipeDO;
import com.fa.cim.entity.runtime.prcrsc.CimProcessResourceDO;
import com.fa.cim.entity.runtime.processdefinition.CimProcessDefinitionDO;
import com.fa.cim.entity.runtime.productspec.CimProductSpecificationDO;
import com.fa.cim.entity.runtime.restrict.CimRestrictionDO;
import com.fa.cim.entity.runtime.restrict.CimRestrictionExpLotDO;
import com.fa.cim.entity.runtime.restrict.CimRestrictionRsnInfoDO;
import com.fa.cim.entity.runtime.stage.CimStageDO;
import com.fa.cim.jpa.CimJpaRepository;
import com.fa.cim.jpa.SearchCondition;
import com.fa.cim.method.*;
import com.fa.cim.mfg.MfgInfoParams;
import com.fa.cim.newcore.bo.code.CimCategory;
import com.fa.cim.newcore.bo.code.CimCode;
import com.fa.cim.newcore.bo.durable.CimProcessDurable;
import com.fa.cim.newcore.bo.durable.CimProcessDurableCapability;
import com.fa.cim.newcore.bo.factory.CimStage;
import com.fa.cim.newcore.bo.machine.CimMachine;
import com.fa.cim.newcore.bo.pd.CimProcessDefinition;
import com.fa.cim.newcore.bo.person.CimPerson;
import com.fa.cim.newcore.bo.prodspec.CimProductSpecification;
import com.fa.cim.newcore.bo.product.CimLot;
import com.fa.cim.newcore.bo.recipe.CimMachineRecipe;
import com.fa.cim.newcore.bo.restrict.CimRestriction;
import com.fa.cim.newcore.bo.restrict.RestrictionManager;
import com.fa.cim.newcore.dto.restriction.Constrain;
import com.fa.cim.newcore.exceptions.CoreFrameworkException;
import com.fa.cim.newcore.factory.BaseCoreFactory;
import com.fa.cim.newcore.impl.bo.env.StandardProperties;
import com.fa.cim.newcore.standard.mchnmngm.MachineResource;
import com.fa.cim.newcore.standard.mchnmngm.ProcessResource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.sql.Timestamp;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/12/4        ********            Nyx                create file
 *
 * @author: Nyx
 * @date: 2020/12/4 14:15
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@OmMethod
public class ConstraintMethod implements IConstraintMethod {
    @Autowired
    private RetCodeConfig retCodeConfig;
    @Autowired
    private RetCodeConfigEx retCodeConfigEx;
    @Autowired
    private BaseCoreFactory baseCoreFactory;
    @Autowired
    private ITimeStampMethod timeStampMethod;
    @Autowired
    private CimJpaRepository cimJpaRepository;
    @Autowired
    private RestrictionManager restrictionManager;
    @Autowired
    private IEventMethod eventMethod;
    @Autowired
    private ILotMethod lotMethod;
    @Autowired
    private ICodeMethod codeMethod;
    @Autowired
    private IProcessMethod processMethod;
    @Autowired
    private IEquipmentMethod equipmentMethod;

    @Override
    public void constraintCheckValidity(Infos.ObjCommon objCommon, Infos.EntityInhibitDetailAttributes strEntityInhibition) {
        log.info("【Method Entry】entityInhibitCheckValidity()");

        //【Step-1】 checking entities
        int entitySize = CimArrayUtils.getSize(strEntityInhibition.getEntities());

        log.info("#### start timestamp : {}", strEntityInhibition.getStartTimeStamp());
        log.info("#### end timestamp   : {}", strEntityInhibition.getEndTimeStamp());
        //confirm TimeStampImpl for startTime/endTime in source code;

        String startTimeStr = strEntityInhibition.getStartTimeStamp();
        String endTimeStr = strEntityInhibition.getEndTimeStamp();
        log.info("#### Converted start timestamp : {}", startTimeStr);
        log.info("#### Converted end timestamp   : {}", endTimeStr);

        if (!CimStringUtils.equals(BizConstant.SP_TIMESTAMP_NIL_OBJECT_STRING, startTimeStr)
                && !CimObjectUtils.isEmpty(startTimeStr)
                && !CimStringUtils.equals(BizConstant.SP_TIMESTAMP_NIL_OBJECT_STRING, endTimeStr)
                && !CimObjectUtils.isEmpty(endTimeStr)) {
            Validations.check(startTimeStr.compareTo(endTimeStr) >= 0, retCodeConfig.getInvalidStartTime());
        }

        //Check entity count
        int entityCount = 0;
        for (int entNum = 0; entNum < entitySize; entNum++) {
            Infos.EntityIdentifier entityIdentifier = strEntityInhibition.getEntities().get(entNum);

            if (CimStringUtils.equals(BizConstant.SP_INHIBITCLASSID_CHAMBER, entityIdentifier.getClassName())) {
                // SP_InhibitClassID_Chamber means 'eqp' + 'Chamber'. So entityCount increases 2.
                entityCount += 2;
            } else {
                entityCount++;
            }
        }

        log.info(" ### entityCount =  {}", entityCount);
        int configLimitCount = 0;
        if (!CimObjectUtils.isEmpty(BizConstant.SP_ENTITYINHIBIT_LIMIT_COUNT)) {
            configLimitCount = BizConstant.SP_ENTITYINHIBIT_LIMIT_COUNT;
        }
//        Validations.check(entityCount > configLimitCount, retCodeConfig.getInvalidEntityCount());

        String useWildCard = StandardProperties.OM_CONSTRAINT_USE_WILDCARD.getValue();
        log.info("OM_CONSTRAINT_USE_WILDCARD : {} ", useWildCard);

        String wildCardChar = StandardProperties.OM_CONSTRAINT_WILDCARD_VALUE.getValue();

        String wildCardPos;
        String hSearchKey;
        for (int i = 0; i < entitySize; i++) {
            boolean wildCardFlag;

            Infos.EntityIdentifier entityIdentifier = strEntityInhibition.getEntities().get(i);
            log.info("class name = {}", entityIdentifier.getClassName());
            String anObjectID = ObjectIdentifier.fetchValue(entityIdentifier.getObjectID());

            // Not USE 'WILDCARD'
            if (CimObjectUtils.isEmpty(useWildCard) || CimStringUtils.equals(useWildCard, "0")) {
                log.info(" useWildCard = 0 ");
                hSearchKey = anObjectID;
                log.info("Identifier ( hSearchKey ): {} ", hSearchKey);
                wildCardFlag = false;
            } else {
                // USE 'WILDCARD'
                int lastIndex = anObjectID.lastIndexOf(wildCardChar);
                // Compare the address. The heading character is '*' .
                Validations.check(lastIndex == 0, retCodeConfig.getNotFoundEntityInhibit());
                // Wild card is not used in input identifier.
                if (lastIndex == -1) {
                    log.info("useWildCard = 0  & wildCardPos == NULL");
                    hSearchKey = anObjectID;
                    log.info("Identifier ( hSearchKey ): {} ", hSearchKey);
                    wildCardFlag = false;
                } else {
                    // Wild card is used.
                    log.info("useWildCard = 0  & wildCardPos != NULL ");
                    int lastPosition = anObjectID.lastIndexOf(wildCardChar);
                    wildCardPos = anObjectID.substring(lastPosition);

                    int objIDLen = anObjectID.length();
                    int tillWildCardLen = wildCardPos.length() - anObjectID.length() + 1;
                    log.info("### Input objID length = {}", objIDLen);
                    log.info("### head to wildCard length = {}", tillWildCardLen);
                    Validations.check(objIDLen != tillWildCardLen, retCodeConfig.getInvalidWildcardPostion());
                    wildCardPos = BizConstant.EMPTY;

                    hSearchKey = anObjectID + "%";
                    log.info("Identifier ( hSearchKey ): {}", hSearchKey);
                    wildCardFlag = true;
                }
            }

            int hCount = 0;
            // checking product specification
            if (CimStringUtils.equals(BizConstant.SP_INHIBITCLASSID_PRODUCT, entityIdentifier.getClassName())) {
                String querySql = String.format("select * from OMPRODINFO product where product.PROD_ID like '%s' ", hSearchKey);
                List<CimProductSpecificationDO> productSpecifications = cimJpaRepository.query(querySql, CimProductSpecificationDO.class);

                hCount = CimArrayUtils.getSize(productSpecifications);
                Validations.check(hCount == 0, retCodeConfig.getNotFoundProductSpec());
            }
            // checking main route
            else if (CimStringUtils.equals(BizConstant.SP_INHIBITCLASSID_ROUTE, entityIdentifier.getClassName())) {
                String hCondition = BizConstant.SP_PD_FLOWLEVEL_MAIN;

                String hVersion = BizConstant.SP_ACTIVE_VERSION;

                String querySql = String.format("select * from OMPRP pd where pd.PRP_ID like '%s' and pd.PRP_LEVEL = '%s' and pd.VERSION_ID != '%s' ", hSearchKey, hCondition, hVersion);
                List<CimProcessDefinitionDO> processDefinitions = cimJpaRepository.query(querySql, CimProcessDefinitionDO.class);

                hCount = CimArrayUtils.getSize(processDefinitions);
                Validations.check(hCount == 0, retCodeConfig.getNotFoundProcessDefinition());
            }
            // checking main route and operation
            else if (CimStringUtils.equals(BizConstant.SP_INHIBITCLASSID_OPERATION, entityIdentifier.getClassName())) {
                String hCondition = BizConstant.SP_PD_FLOWLEVEL_MAIN;

                String hVersion = BizConstant.SP_ACTIVE_VERSION;

                String querySql = String.format("select * from OMPRP pd where pd.PRP_ID like '%s' and pd.PRP_LEVEL = '%s' and pd.VERSION_ID != '%s' ", hSearchKey, hCondition, hVersion);
                List<CimProcessDefinitionDO> processDefinitions = cimJpaRepository.query(querySql, CimProcessDefinitionDO.class);

                hCount = CimArrayUtils.getSize(processDefinitions);
                Validations.check(hCount == 0, retCodeConfig.getNotFoundProcessDefinition());


                /*******************************************/
                /*   Validity check for Operation Number   */
                /*  1) At least one period is necessary.   */
                /*  2) And plural period isn't allowed.    */
                /*  Ex)                                    */
                /*   "100.200" : OK                        */
                /*   "100,200" : NG                        */
                /*   "1000000" : NG                        */
                /*   ""        : NG                        */
                /*   "10.20.30": NG                        */
                /*******************************************/
                log.info("#### Check Operation Number validity");
                boolean opeNoValidFlag = checkOperationNumberValidity(entityIdentifier.getAttribution());
                Validations.check(CimBooleanUtils.isFalse(opeNoValidFlag), retCodeConfig.getInvalidOperationNumber());

                if (CimBooleanUtils.isFalse(wildCardFlag)) {
                    /****************************************/
                    /*   check Operation Number existence   */
                    /****************************************/
                    log.info("#### Check Operation Number existence");
                    ObjectIdentifier routeID = new ObjectIdentifier();
                    routeID.setValue(hSearchKey);

                    Inputs.ProcessOperationListForRoute strProcessOperationListForRouteIn = new Inputs.ProcessOperationListForRoute();
                    strProcessOperationListForRouteIn.setRouteID(routeID);
                    ObjectIdentifier operationID = new ObjectIdentifier("");
                    strProcessOperationListForRouteIn.setOperationID(operationID);
                    strProcessOperationListForRouteIn.setOperationNumber(entityIdentifier.getAttribution());
                    strProcessOperationListForRouteIn.setPdType("");
                    strProcessOperationListForRouteIn.setSearchCount(1);
                    List<Infos.OperationNameAttributes> strProcess_operationListForRoute_out = null;
                    try {
                        strProcess_operationListForRoute_out = processMethod.processOperationListForRoute(objCommon, strProcessOperationListForRouteIn);
                    } catch (ServiceException e) {
                        if (Validations.isEquals(retCodeConfig.getNotFoundOperation(), e.getCode())) {
                            Validations.check(true, retCodeConfig.getNotFoundRouteOpe(), routeID, entityIdentifier.getAttribution());
                        } else {
                            throw e;
                        }
                    }

                }
            }
            // checking module process definition
            else if (CimStringUtils.equals(BizConstant.SP_INHIBITCLASSID_MODULEPD, entityIdentifier.getClassName())) {
                String hCondition = BizConstant.SP_PD_FLOWLEVEL_MODULE;

                String hVersion = BizConstant.SP_ACTIVE_VERSION;

                String querySql = String.format("select * from OMPRP pd where pd.PRP_ID like '%s' and pd.PRP_LEVEL = '%s' and pd.VERSION_ID != '%s' ", hSearchKey, hCondition, hVersion);
                List<CimProcessDefinitionDO> processDefinitions = cimJpaRepository.query(querySql, CimProcessDefinitionDO.class);

                hCount = CimArrayUtils.getSize(processDefinitions);
                Validations.check(hCount == 0, retCodeConfig.getNotFoundProcessDefinition());
            }
            // checking process definition
            else if (CimStringUtils.equals(BizConstant.SP_INHIBITCLASSID_PROCESS, entityIdentifier.getClassName())) {

                String hCondition = BizConstant.SP_PD_FLOWLEVEL_OPERATION;

                String hVersion = BizConstant.SP_ACTIVE_VERSION;

                String querySql = String.format("select * from OMPRP pd where pd.PRP_ID like '%s' and pd.PRP_LEVEL = '%s' and pd.VERSION_ID != '%s' ", hSearchKey, hCondition, hVersion);
                List<CimProcessDefinitionDO> processDefinitions = cimJpaRepository.query(querySql, CimProcessDefinitionDO.class);

                hCount = CimArrayUtils.getSize(processDefinitions);
                Validations.check(hCount == 0, retCodeConfig.getNotFoundProcessDefinition());
            }
            // checking machine recipe
            else if (CimStringUtils.equals(BizConstant.SP_INHIBITCLASSID_MACHINERECIPE, entityIdentifier.getClassName())) {

                String hVersion = BizConstant.SP_ACTIVE_VERSION;

                String querySql = String.format("select * from OMRCP mrcp where mrcp.RECIPE_ID like '%s' and mrcp.VERSION_ID != '%s' ", hSearchKey, hVersion);
                List<CimMachineRecipeDO> machineRecipes = cimJpaRepository.query(querySql, CimMachineRecipeDO.class);

                hCount = CimArrayUtils.getSize(machineRecipes);
                Validations.check(hCount == 0, retCodeConfig.getNotFoundMachineRecipe());
            }
            // checking machine
            else if (CimStringUtils.equals(BizConstant.SP_INHIBITCLASSID_EQUIPMENT, entityIdentifier.getClassName())) {
                String querySql = String.format("select * from OMEQP eqp where eqp.EQP_ID like '%s' ", hSearchKey);
                List<CimEquipmentDO> equipments = cimJpaRepository.query(querySql, CimEquipmentDO.class);

                hCount = CimArrayUtils.getSize(equipments);
                Validations.check(hCount == 0, new OmCode(retCodeConfig.getNotFoundEqp(), hSearchKey));
            }
            // checking reticle
            else if (CimStringUtils.equals(BizConstant.SP_INHIBITCLASSID_RETICLE, entityIdentifier.getClassName())) {
                String hCondition = BizConstant.SP_DURABLECAT_RETICLE;

                String querySql = String.format("select * from OMPDRBL durable where durable.PDRBL_ID like '%s' and durable.PDRBL_CATEGORY = '%s' ", hSearchKey, hCondition);
                List<CimDurableDO> durables = cimJpaRepository.query(querySql, CimDurableDO.class);

                hCount = CimArrayUtils.getSize(durables);
                Validations.check(hCount == 0, retCodeConfig.getNotFoundReticle());
            }
            // checking fixture
            else if (CimStringUtils.equals(BizConstant.SP_INHIBITCLASSID_FIXTURE, entityIdentifier.getClassName())) {
                String hCondition = BizConstant.SP_DURABLECAT_FIXTURE;

                String querySql = String.format("select * from OMPDRBL durable where durable.PDRBL_ID like '%s' and durable.PDRBL_CATEGORY = '%s' ", hSearchKey, hCondition);
                List<CimDurableDO> durables = cimJpaRepository.query(querySql, CimDurableDO.class);

                hCount = CimArrayUtils.getSize(durables);
                Validations.check(hCount == 0, retCodeConfig.getNotFoundFixture());
            }
            // checking reticle group
            else if (CimStringUtils.equals(BizConstant.SP_INHIBITCLASSID_RETICLEGROUP, entityIdentifier.getClassName())) {
                String hCondition = BizConstant.SP_DURABLECAT_RETICLE;

                String querySql = String.format("select * from OMPDRBLGRP dg where dg.PDRBL_GRP_ID like '%s' and dg.PDRBL_TYPE = '%s'", hSearchKey, hCondition);
                List<CimDurableGroupDO> durables = cimJpaRepository.query(querySql, CimDurableGroupDO.class); //todo : change durable to durablegroup;

                hCount = CimArrayUtils.getSize(durables);
                Validations.check(hCount == 0, retCodeConfig.getNotFoundReticleGrp());
            }
            // checking fixture group
            else if (CimStringUtils.equals(BizConstant.SP_INHIBITCLASSID_FIXTUREGROUP, entityIdentifier.getClassName())) {
                String hCondition = BizConstant.SP_DURABLECAT_FIXTURE;

                String querySql = String.format("select * from OMPDRBLGRP dg where dg.PDRBL_GRP_ID like '%s' and dg.PDRBL_TYPE = '%s'", hSearchKey, hCondition);
                List<CimDurableGroupDO> durables = cimJpaRepository.query(querySql, CimDurableGroupDO.class); //todo : change durable to durablegroup;

                hCount = CimArrayUtils.getSize(durables);
                Validations.check(hCount == 0, retCodeConfig.getNotFoundFixtureGrp());
            }

            // checking stage
            else if (CimStringUtils.equals(BizConstant.SP_INHIBITCLASSID_STAGE, entityIdentifier.getClassName())) {
                String querySql = String.format("select * from OMSTAGE stage where stage.STAGE_ID like '%s' ", hSearchKey);
                List<CimStageDO> stages = cimJpaRepository.query(querySql, CimStageDO.class);

                hCount = CimArrayUtils.getSize(stages);
                Validations.check(hCount == 0, retCodeConfig.getNotFoundStage());
            }
            //Checking Chamber
            else if (CimStringUtils.equals(BizConstant.SP_INHIBITCLASSID_CHAMBER, entityIdentifier.getClassName())) {
                String querySql = String.format("select * from OMEQP eqp where eqp.EQP_ID like '%s' ", hSearchKey);
                List<CimEquipmentDO> equipments = cimJpaRepository.query(querySql, CimEquipmentDO.class);

                hCount = CimArrayUtils.getSize(equipments);
                Validations.check(hCount == 0, retCodeConfig.getNotFoundEqp());

                String hFRPRCRSCPROCRSC_ID = entityIdentifier.getAttribution();

                String queryPrSql = String.format("select * from OMPROCRES p where p.EQP_ID like '%s' and p.PROCRES_ID = '%s' ", hSearchKey, hFRPRCRSCPROCRSC_ID);
                List<CimProcessResourceDO> processResources = cimJpaRepository.query(queryPrSql, CimProcessResourceDO.class);

                hCount = CimArrayUtils.getSize(processResources);
                Validations.check(hCount == 0, retCodeConfig.getNotFoundChamber());
            }
            // no corresponding class
            else {
                log.info("no class name");
                Validations.check(true, retCodeConfig.getInhibitNoClass());
            }
        }

        // checking reason code
        CimCategory aCategory = baseCoreFactory.getBO(CimCategory.class, new ObjectIdentifier(BizConstant.SP_REASONCAT_ENTITYINHIBIT));
        Validations.check(aCategory == null, retCodeConfig.getNotFoundCategory());

        ObjectIdentifier reasonCodeID = new ObjectIdentifier(strEntityInhibition.getReasonCode());

        CimCode aReasonCode = null;
        String aCategoryIdentifier = aCategory.getIdentifier();
        if (ObjectIdentifier.isEmptyWithRefKey(reasonCodeID)) {
            Validations.check(ObjectIdentifier.isEmptyWithValue(reasonCodeID), retCodeConfig.getNotFoundCode(), aCategoryIdentifier, "*****");
            aReasonCode = aCategory.findCodeNamed(reasonCodeID.getValue());
        } else {
            aReasonCode = baseCoreFactory.getBO(com.fa.cim.newcore.bo.code.CimCode.class, reasonCodeID.getReferenceKey());
        }
        Validations.check(null == aReasonCode, retCodeConfig.getNotFoundCode(), aCategoryIdentifier,
                ObjectIdentifier.fetchValue(reasonCodeID));

        // checking user
        String ownerID = ObjectIdentifier.fetchValue(strEntityInhibition.getOwnerID());
        Validations.check(ownerID == null, retCodeConfig.getNotFoundPerson());

        CimPerson person = baseCoreFactory.getBO(CimPerson.class, strEntityInhibition.getOwnerID());
        Validations.check(person == null, retCodeConfig.getNotFoundPerson());

        // checking reason detail information
        int rsnLength = CimArrayUtils.getSize(strEntityInhibition.getEntityInhibitReasonDetailInfos());

        for (int nRsnCnt = 0; nRsnCnt < rsnLength; nRsnCnt++) {
            // checking related lot
            Infos.EntityInhibitReasonDetailInfo entityInhibitReasonDetailInfo = strEntityInhibition
                    .getEntityInhibitReasonDetailInfos().get(nRsnCnt);
            if (!CimObjectUtils.isEmpty(entityInhibitReasonDetailInfo.getRelatedLotID()) &&
                    CimStringUtils.isEmpty(entityInhibitReasonDetailInfo.getRelatedFabID())) {
                log.info("#### Check Related lot existence");
                hSearchKey = entityInhibitReasonDetailInfo.getRelatedLotID();

                String sql = String.format("SELECT * FROM OMLOT WHERE LOT_ID LIKE '%s' ", hSearchKey);
                List<CimLotDO> lots = cimJpaRepository.query(sql, CimLotDO.class);

                int hCount = CimArrayUtils.getSize(lots);
                Validations.check(hCount == 0, retCodeConfig.getNotFoundLot());

            }

            // checking related main route
            if (!CimObjectUtils.isEmpty(entityInhibitReasonDetailInfo.getRelatedRouteID())) {
                log.info("#### Check Related Route existence");

                hSearchKey = entityInhibitReasonDetailInfo.getRelatedRouteID();
                String hCondition = BizConstant.SP_PD_FLOWLEVEL_MAIN;
                String hVersion = BizConstant.SP_ACTIVE_VERSION;

                log.info("Identifier ( hSearchKey ): {} ", hSearchKey);
                String querySql = String
                        .format("select * from OMPRP pd where pd.PRP_ID like '%s' and pd.PRP_LEVEL = '%s' " +
                                "and pd.VERSION_ID != '%s'", hSearchKey, hCondition, hVersion);
                List<CimProcessDefinitionDO> processDefinitions = cimJpaRepository
                        .query(querySql, CimProcessDefinitionDO.class);

                int hCount = CimArrayUtils.getSize(processDefinitions);
                Validations.check(hCount == 0, retCodeConfig.getNotFoundProcessDefinition());

                // checking related operatoin number
                if (!CimObjectUtils.isEmpty(entityInhibitReasonDetailInfo.getRelatedOperationNumber())) {
                    log.info("#### Check Related Operation Number existence");

                    boolean opeNoValidFlag = checkOperationNumberValidity(entityInhibitReasonDetailInfo
                            .getRelatedOperationNumber());
                    Validations.check(CimBooleanUtils.isFalse(opeNoValidFlag), retCodeConfig
                            .getInvalidOperationNumber(), entityInhibitReasonDetailInfo.getRelatedOperationNumber());

                    /****************************************/
                    /*   check Operation Number existence   */
                    /****************************************/
                    log.info("#### Check Operation Number existence");
                    ObjectIdentifier routeID = new ObjectIdentifier(hSearchKey);

                    Inputs.ProcessOperationListForRoute strProcessOperationListForRouteIn = new Inputs
                            .ProcessOperationListForRoute();
                    strProcessOperationListForRouteIn.setRouteID(routeID);
                    ObjectIdentifier operationID = new ObjectIdentifier("");
                    strProcessOperationListForRouteIn.setOperationID(operationID);
                    strProcessOperationListForRouteIn.setOperationNumber(entityInhibitReasonDetailInfo
                            .getRelatedOperationNumber());
                    strProcessOperationListForRouteIn.setPdType("");
                    strProcessOperationListForRouteIn.setSearchCount(1);
                    List<Infos.OperationNameAttributes> strProcessOperationListForRouteOut = null;
                    try {
                        strProcessOperationListForRouteOut = processMethod.processOperationListForRoute(objCommon, strProcessOperationListForRouteIn);
                    } catch (ServiceException e) {
                        if (Validations.isEquals(retCodeConfig.getNotFoundOperation(), e.getCode())) {
                            Validations.check(true, retCodeConfig.getNotFoundRouteOpe(), routeID, entityInhibitReasonDetailInfo.getRelatedOperationNumber());
                        } else {
                            throw e;
                        }

                    }
                }
            }

            // checking related process definition
            if (!CimObjectUtils.isEmpty(entityInhibitReasonDetailInfo.getRelatedProcessDefinitionID())) {
                log.info("#### Check Related Process Definition existence");

                hSearchKey = entityInhibitReasonDetailInfo.getRelatedProcessDefinitionID();
                String hCondition = BizConstant.SP_PD_FLOWLEVEL_OPERATION;
                String hVersion = BizConstant.SP_ACTIVE_VERSION;

                log.info("Identifier ( hSearchKey ): {} ", hSearchKey);
                String querySql = String.format("select * from OMPRP pd where pd.PRP_ID like '%s' " +
                        "and pd.PRP_LEVEL = '%s' and pd.VERSION_ID != '%s' ", hSearchKey, hCondition, hVersion);
                List<CimProcessDefinitionDO> processDefinitions = cimJpaRepository.query(querySql,
                        CimProcessDefinitionDO.class);

                int hCount = CimArrayUtils.getSize(processDefinitions);
                Validations.check(hCount == 0, retCodeConfig.getNotFoundProcessDefinition());
            }
        }

        // checking sub lot types information
        int sltLength = CimArrayUtils.getSize(strEntityInhibition.getSubLotTypes());
        for (int nSltCnt = 0; nSltCnt < sltLength; nSltCnt++) {
            Outputs.LotSubLotTypeGetDetailInfoDR outputs = lotMethod.lotSubLotTypeGetDetailInfoDR(objCommon,
                    strEntityInhibition.getSubLotTypes().get(nSltCnt));
        }
    }

    //region Private Methods
    private boolean checkOperationNumberValidity(String operationNumber) {
        //----------------------------//
        //  Check following patterns  //
        //   "100,200" : NG           //
        //   "1000000" : NG           //
        //   ""        : NG           //
        //----------------------------//

        int lastIndex = operationNumber.lastIndexOf(".");
        String sepChar = operationNumber.substring(lastIndex);
        if (CimStringUtils.isEmpty(sepChar)) return false;

        //----------------------------//
        //  Check following pattern   //
        //   "10.20.30": NG           //
        //----------------------------//

        int firstIndex = operationNumber.indexOf(".");
        String sepChar2 = operationNumber.substring(firstIndex);
        if (sepChar.compareTo(sepChar2) != 0) return false;

        return true;
    }

    @Override
    public Infos.EntityInhibitDetailInfo constraintRegistrationReq(Infos.ObjCommon objCommon,
                                                                   Infos.EntityInhibitDetailAttributes entityInhibition,
                                                                   String claimMemo) {
        Constrain.EntityInhibitRecord entityInhibitRecord = new Constrain.EntityInhibitRecord();
        List<Constrain.EntityIdentifier> entities = new ArrayList<>();
        entityInhibitRecord.setEntities(entities);
        // checking entities
        log.info("start to check entities");
        String useWildCard = StandardProperties.OM_CONSTRAINT_USE_WILDCARD.getValue();
        log.info("OM_CONSTRAINT_USE_WILDCARD :: {}", useWildCard);
        List<Infos.EntityIdentifier> numOfEntities = entityInhibition.getEntities();
        for (Infos.EntityIdentifier numOfEntity : numOfEntities) {
            boolean wildCardFlag;
            String className = numOfEntity.getClassName();
            String attribution = numOfEntity.getAttribution();
            log.info("class name {}", className);
            ObjectIdentifier objectID = numOfEntity.getObjectID();
            String anObjectId = ObjectIdentifier.fetchValue(objectID);

            if (CimStringUtils.equals(useWildCard, BizConstant.LEVEL_ZERO)) {// Not USE 'WILDCARD'
                log.info("entities.className {}", "useWildCard = 0");
                wildCardFlag = false;
            } else { // USE 'WILDCARD'
                if (!Pattern.matches(".*\\*.*", anObjectId)) {// Wild card is not used in input identifier.
                    log.info("entities.className {}", "useWildCard = 0  & wildCardPos == NULL ");
                    wildCardFlag = false;
                } else {// Wild card is used.
                    Validations.check(Pattern.matches(".*\\*{2,}.*", anObjectId), retCodeConfig
                            .getNotFoundEntityInhibit()); //Wildcards should not be greater than or equal to 2
                    log.info("entities.className {}", "useWildCard = 0  & wildCardPos != NULL ");
                    wildCardFlag = true;
                }
            }

            Constrain.EntityIdentifier entity = new Constrain.EntityIdentifier();
            entities.add(entity);
            if (wildCardFlag) { //如果有objectID通配符*就简单的检查className是否存在
                switch (Objects.requireNonNull(ConstraintClassEnum.get(className))) {
                    case INHIBITCLASSID_PRODUCT:
                    case INHIBITCLASSID_MODULEPD:
                    case INHIBITCLASSID_PROCESS:
                    case INHIBITCLASSID_MACHINERECIPE:
                    case INHIBITCLASSID_EQUIPMENT:
                    case INHIBITCLASSID_RETICLE:
                    case INHIBITCLASSID_FIXTURE:
                    case INHIBITCLASSID_RETICLEGROUP:
                    case INHIBITCLASSID_FIXTUREGROUP:
                    case INHIBITCLASSID_STAGE:
                    case INHIBITCLASSID_LOT:
                    case INHIBITCLASSID_ROUTE:
                    case INHIBITCLASSID_OPERATION:
                    case INHIBITCLASSID_CHAMBER:{
                        log.info("Class name = {}", className);
                        break;
                    }
                    default: {
                        log.info("no class name");
                        Validations.check(retCodeConfig.getInhibitNoClass());
                    }
                }
            } else { //如果objectID没有通配符*就检查具体的objectID对象是否存在
                log.info("class name {}", className);
                checkValueOfClassNameExists(className, anObjectId);
                if (CimStringUtils.equals(className,ConstraintClassEnum.INHIBITCLASSID_CHAMBER.getValue())
                  || CimStringUtils.equals(className,ConstraintClassEnum.INHIBITCLASSID_OPERATION.getValue())){
                    attribCheck(objCommon,className,objectID,attribution);
                }
            }

            // setting entities into posEntityInhibitRecord
            log.info("converting Operation to Route");
            switch (Objects.requireNonNull(ConstraintClassEnum.get(className))) {
                case INHIBITCLASSID_ROUTE: {
                    entity.setClassName(className);
                    entity.setAttrib("*");
                    break;
                }
                case INHIBITCLASSID_OPERATION: {
                    entity.setClassName(BizConstant.SP_INHIBITCLASSID_ROUTE);
                    entity.setAttrib(attribution);
                    break;
                }
                case INHIBITCLASSID_CHAMBER: {
                    entity.setClassName(className);
                    entity.setAttrib(attribution);
                    break;
                }
                case INHIBITCLASSID_EQUIPMENT: {
                    entity.setClassName(className);
                    //set spec tool, If there is equipment, it means tool constraint
                    entityInhibitRecord.setSpecificTool(true);
                    break;
                }
                default: {
                    entity.setClassName(className);
                    entity.setAttrib("");
                }
            }
            entity.setObjectId(ObjectIdentifier.fetchValue(objectID));
        }

        //Check if the class name of the exception entity exists
        List<Constrain.EntityIdentifier> constraintExceptionEntities = new ArrayList<>();
        List<Infos.EntityIdentifier> exceptionEntities = entityInhibition.getExceptionEntities();
        if (!CimObjectUtils.isEmpty(exceptionEntities)) {
            for (Infos.EntityIdentifier exceptionEntity : exceptionEntities) {
                checkValueOfClassNameExists(exceptionEntity.getClassName(), ObjectIdentifier
                        .fetchValue(exceptionEntity.getObjectID()));
                //additionall check for general view
//                if (StringUtils.equals(exceptionEntity.getClassName(),ConstraintClassEnum.INHIBITCLASSID_CHAMBER.getValue())
//                ||StringUtils.equals(exceptionEntity.getClassName(),ConstraintClassEnum.INHIBITCLASSID_OPERATION.getValue())){
//                    attribCheck(exceptionEntity.getClassName(),exceptionEntity.getObjectID(),exceptionEntity.getAttribution());
//                }
                Constrain.EntityIdentifier constraintExceptionEntity = new Constrain.EntityIdentifier();
                constraintExceptionEntity.setAttrib(exceptionEntity.getAttribution());
                constraintExceptionEntity.setClassName(exceptionEntity.getClassName());
                constraintExceptionEntity.setObjectId(ObjectIdentifier.fetchValue(exceptionEntity.getObjectID()));
                constraintExceptionEntities.add(constraintExceptionEntity);
            }
            entityInhibitRecord.setExceptionEntities(constraintExceptionEntities);
        }

        // checking reason code
        log.info("getting category");
        CimCategory aCategory = baseCoreFactory.getBO(CimCategory.class, new ObjectIdentifier(BizConstant
                .SP_REASONCAT_ENTITYINHIBIT));
        Validations.check(aCategory == null, retCodeConfig.getNotFoundCategory());

        log.info("set code");
        String reasonCode = entityInhibition.getReasonCode();
        //Constraint tool does not need to set reason code, reason code can be empty
        if (!CimObjectUtils.isEmpty(reasonCode)) {
            CimCode aReasonCode = aCategory.findCodeNamed(reasonCode);
            Validations.check(null == aReasonCode, retCodeConfig.getNotFoundCode(), BizConstant
                    .SP_REASONCAT_ENTITYINHIBIT, reasonCode);
            entityInhibitRecord.setReasonCode(new ObjectIdentifier(aReasonCode.getIdentifier(), aReasonCode.getPrimaryKey()));
        }

        // checking user
        log.info("Set owner");
        CimPerson person = baseCoreFactory.getBO(CimPerson.class, entityInhibition.getOwnerID());
        Validations.check(person == null, new OmCode(retCodeConfig.getNotFoundPerson(),
                entityInhibition.getOwnerID().getValue()));
        entityInhibitRecord.setOwner(new ObjectIdentifier(person.getIdentifier(), person.getPrimaryKey()));

        // setting posEntityInhibitRecord struct
        log.info("set others");
        entityInhibitRecord.setSubLotTypes(entityInhibition.getSubLotTypes());

        log.info("set function rule : blacklist or whitelist");
        String functionRule = entityInhibition.getFunctionRule();
        entityInhibitRecord.setFunctionRule(functionRule);

        //Set Start time Stamp
        log.info("Set Start time Stamp");
        String startTimeStr = entityInhibition.getStartTimeStamp();
        entityInhibitRecord.setStartTimeStamp(CimObjectUtils.isEmpty(startTimeStr)
                ? Timestamp.valueOf(BizConstant.SP_TIMESTAMP_NIL_OBJECT_STRING) : Timestamp.valueOf(startTimeStr));
        entityInhibitRecord.setChangedTimeStamp(CimDateUtils.getCurrentTimeStamp());

        //Set End time Stamp
        log.info("Set End time Stamp");
        String endTimeStr = entityInhibition.getEndTimeStamp();

        if (CimStringUtils.equals(BizConstant.SP_TIMESTAMP_NIL_OBJECT_STRING, endTimeStr)
                || CimObjectUtils.isEmpty(endTimeStr)) {
            String startTime = CimStringUtils.equals(BizConstant.SP_TIMESTAMP_NIL_OBJECT_STRING, startTimeStr)
                    || CimObjectUtils.isEmpty(startTimeStr)
                    ? objCommon.getTimeStamp().getReportTimeStamp().toString() : startTimeStr;

            // Get end time from OM_CONSTRAINT_DURATION
            int inhibitDurationDays = 0;
            String configDuration = StandardProperties.OM_CONSTRAINT_DURATION.getValue();
            if (!CimObjectUtils.isEmpty(configDuration)) {
                inhibitDurationDays = Integer.parseInt(configDuration);
            }
            if (0 >= inhibitDurationDays) {
                inhibitDurationDays = 10;
            }
            try {
                timeStampMethod.timeStampDoCalculation(objCommon, startTime,
                        inhibitDurationDays, 0, 0, 0, 0);
                log.info("timeStampDoCalculation return RC_OK");
                entityInhibitRecord.setEndTimeStamp(new Timestamp(Timestamp
                        .valueOf(startTime).getTime() + inhibitDurationDays * 24 * 60 * 60 * 1000));
            } catch (ServiceException e) {
                log.info("timeStampDoCalculation return an error.");
                entityInhibitRecord.setEndTimeStamp(Timestamp.valueOf(BizConstant.SP_TIMESTAMP_NIL_OBJECT_STRING));
            }
        } else {
            entityInhibitRecord.setEndTimeStamp(Timestamp.valueOf(endTimeStr));
        }
        Validations.check(entityInhibitRecord.getStartTimeStamp().after(entityInhibitRecord
                .getEndTimeStamp()),retCodeConfigEx.getEntityInhibitModifyFailed());

        log.info("Set Claim Memo");
        entityInhibitRecord.setClaimMemo(entityInhibition.getMemo());

        // create an entity inhibit
        CimRestriction anEntityInhibit;
        try {
            anEntityInhibit = restrictionManager.createEntityInhibit(entityInhibitRecord);
            Validations.check(anEntityInhibit == null, retCodeConfigEx.getEntityInhibitCreateFailed());
        } catch (CoreFrameworkException e) {
            throw new ServiceException(e.getCoreCode());
        }


        // set reason detail information
        List<Infos.EntityInhibitReasonDetailInfo> entityInhibitReasonDetailInfos = entityInhibition
                .getEntityInhibitReasonDetailInfos();
        if (!CimObjectUtils.isEmpty(entityInhibitReasonDetailInfos)) {
            List<Constrain.EntityInhibitReasonDetailInfo> reasonDetailInfos = new ArrayList<>();
            for (Infos.EntityInhibitReasonDetailInfo entityInhibitReasonDetailInfo : entityInhibitReasonDetailInfos) {
                Constrain.EntityInhibitReasonDetailInfo reasonDetailInfo = new Constrain.EntityInhibitReasonDetailInfo();
                reasonDetailInfo.setRelatedControlJobID(entityInhibitReasonDetailInfo.getRelatedControlJobID());
                reasonDetailInfo.setRelatedFabID(entityInhibitReasonDetailInfo.getRelatedFabID());
                reasonDetailInfo.setRelatedLotID(entityInhibitReasonDetailInfo.getRelatedLotID());
                reasonDetailInfo.setRelatedOperationNumber(entityInhibitReasonDetailInfo.getRelatedOperationNumber());
                reasonDetailInfo.setRelatedOperationPassCount(entityInhibitReasonDetailInfo.getRelatedOperationPassCount());
                reasonDetailInfo.setRelatedProcessDefinitionID(entityInhibitReasonDetailInfo.getRelatedProcessDefinitionID());
                reasonDetailInfo.setRelatedRouteID(entityInhibitReasonDetailInfo.getRelatedRouteID());

                List<Constrain.EntityInhibitSpcChartInfo> spcChartInfos = new ArrayList<>();
                List<Infos.EntityInhibitSpcChartInfo> strEntityInhibitSpcChartInfos = entityInhibitReasonDetailInfo
                        .getStrEntityInhibitSpcChartInfos();
                if (CimArrayUtils.isNotEmpty(strEntityInhibitSpcChartInfos)) {
                    for (Infos.EntityInhibitSpcChartInfo strEntityInhibitSpcChartInfo : strEntityInhibitSpcChartInfos) {
                        Constrain.EntityInhibitSpcChartInfo spcChartInfo = new Constrain.EntityInhibitSpcChartInfo();
                        spcChartInfo.setRelatedSpcChartGroupID(strEntityInhibitSpcChartInfo.getRelatedSpcChartGroupID());
                        spcChartInfo.setRelatedSpcChartID(strEntityInhibitSpcChartInfo.getRelatedSpcChartID());
                        spcChartInfo.setRelatedSpcChartType(strEntityInhibitSpcChartInfo.getRelatedSpcChartType());
                        spcChartInfo.setRelatedSpcChartUrl(strEntityInhibitSpcChartInfo.getRelatedSpcChartUrl());
                        spcChartInfo.setRelatedSpcDcType(strEntityInhibitSpcChartInfo.getRelatedSpcDcType());
                        spcChartInfos.add(spcChartInfo);
                    }
                }
                reasonDetailInfo.setStrEntityInhibitSpcChartInfos(spcChartInfos);
                reasonDetailInfos.add(reasonDetailInfo);
            }
            anEntityInhibit.setReasonDetailInfos(reasonDetailInfos);
        }

        // set entity inhibit attributes into output parameter
        Constrain.EntityInhibitRecord inhibitRecord = anEntityInhibit.getInhibitRecord();

        // set entity inhibit attributes into output parameter
        Infos.EntityInhibitDetailInfo entityInhibitInfo = new Infos.EntityInhibitDetailInfo();
        entityInhibitInfo.setEntityInhibitID(new ObjectIdentifier(inhibitRecord.getId(), inhibitRecord.getReferenceKey()));
        entityInhibitInfo.setEntityInhibitDetailAttributes(entityInhibition);
        Infos.EntityInhibitDetailAttributes entityInhibitDetailAttributes = entityInhibitInfo
                .getEntityInhibitDetailAttributes();
        entityInhibitDetailAttributes.setStartTimeStamp(inhibitRecord.getStartTimeStamp().toString());
        entityInhibitDetailAttributes.setEndTimeStamp(inhibitRecord.getEndTimeStamp().toString());
        CimCode code = baseCoreFactory.getBO(CimCode.class, inhibitRecord.getReasonCode());
        entityInhibitDetailAttributes.setReasonDesc(code == null ? "" : code.getDescription());
        return entityInhibitInfo;
    }

    /**
     * description:Check value of ClassNme exists
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/12/12 2:07                     Nyx                Create
     *
     * @author Nyx
     * @date 2020/12/12 2:07
     * @param className
     * @param anObjectId -
     * @return void
     */
    private void checkValueOfClassNameExists(String className, String anObjectId) {
        Validations.check(Pattern.matches(".*,{2,}.*", anObjectId), retCodeConfig.getNotFoundEntityInhibit()); //Comma should not be greater than or equal to 2
        String[] splits = anObjectId.split(BizConstant.SEPARATOR_COMMA); //anObjectId可使用逗号分隔
        for (String objectId : splits) {
            ObjectIdentifier anObjectID = new ObjectIdentifier(objectId.trim());
            switch (Objects.requireNonNull(ConstraintClassEnum.get(className))) {
                case INHIBITCLASSID_PRODUCT: { // checking product specification
                    CimProductSpecification bo = baseCoreFactory.getBO(CimProductSpecification.class, anObjectID);
                    Validations.check(bo == null, retCodeConfig.getInvalidProdId(), anObjectID);
                    break;
                }
                case INHIBITCLASSID_ROUTE: { // checking main route
                    CimProcessDefinition bo = baseCoreFactory.getBO(CimProcessDefinition.class, anObjectID);
                    if (bo == null) {
                        OmCode pdCode = (OmCode) retCodeConfig.getNotFoundProcessDefinition().clone();
                        pdCode.setMessage("Process %s information has not been found.");
                        Validations.check(true, pdCode, anObjectID);
                    }
                    break;
                }
                case INHIBITCLASSID_MODULEPD: { // checking module process definition
                    CimProcessDefinition bo = baseCoreFactory.getBO(CimProcessDefinition.class, anObjectID);
                    if (bo == null) {
                        OmCode pdCode = (OmCode) retCodeConfig.getNotFoundProcessDefinition().clone();
                        pdCode.setMessage("Route %s information has not been found.");
                        Validations.check(true, pdCode, anObjectID);
                    }
                    break;
                }
                case INHIBITCLASSID_OPERATION: // checking main route and operation
                case INHIBITCLASSID_PROCESS: {// checking process definition
                    CimProcessDefinition bo = baseCoreFactory.getBO(CimProcessDefinition.class, anObjectID);
                    Validations.check(bo == null, retCodeConfig.getNotFoundProcessDefinition(), anObjectID);
                    break;
                }
                case INHIBITCLASSID_MACHINERECIPE: {// checking machine recipe
                    CimMachineRecipe bo = baseCoreFactory.getBO(CimMachineRecipe.class, anObjectID);
                    Validations.check(bo == null, retCodeConfig.getNotFoundMachineRecipe(), anObjectID);
                    break;
                }
                case INHIBITCLASSID_EQUIPMENT: // checking machine
                case INHIBITCLASSID_CHAMBER: { // checking chamber
                    CimMachine bo = baseCoreFactory.getBO(CimMachine.class, anObjectID);
                    Validations.check(bo == null, retCodeConfig.getNotFoundEqp(), anObjectID);
                    break;
                }
                case INHIBITCLASSID_RETICLE: {// checking reticle
                    CimProcessDurable bo = baseCoreFactory.getBO(CimProcessDurable.class, anObjectID);
                    Validations.check(bo == null, retCodeConfig.getNotFoundReticle(), anObjectID);
                    break;
                }
                case INHIBITCLASSID_FIXTURE: {// checking fixture
                    CimProcessDurable bo = baseCoreFactory.getBO(CimProcessDurable.class, anObjectID);
                    Validations.check(bo == null, retCodeConfig.getNotFoundFixture(), anObjectID);
                    break;
                }
                case INHIBITCLASSID_RETICLEGROUP: {// checking reticle group
                    CimProcessDurableCapability bo = baseCoreFactory.getBO(CimProcessDurableCapability.class, anObjectID);
                    Validations.check(bo == null, retCodeConfig.getNotFoundReticleGrp(), anObjectID);
                    break;
                }
                case INHIBITCLASSID_FIXTUREGROUP: { // checking fixture group
                    CimProcessDurableCapability bo = baseCoreFactory.getBO(CimProcessDurableCapability.class, anObjectID);
                    Validations.check(bo == null, retCodeConfig.getNotFoundFixtureGrp(), anObjectID);
                    break;
                }
                case INHIBITCLASSID_STAGE: { // checking stage
                    CimStage bo = baseCoreFactory.getBO(CimStage.class, anObjectID);
                    Validations.check(bo == null, retCodeConfig.getNotFoundStage(), anObjectID);
                    break;
                }
                case INHIBITCLASSID_LOT: { // checking lot
                    CimLot bo = baseCoreFactory.getBO(CimLot.class, anObjectID);
                    Validations.check(bo == null, retCodeConfig.getNotFoundLot(), anObjectID);
                    break;
                }
                default: {
                    log.info("no class name");
                    Validations.check(retCodeConfig.getInhibitNoClass());
                }
            }
        }
    }

    private void attribCheck(Infos.ObjCommon objCommon,String className, ObjectIdentifier anObjectId,String attrib) {
        if (CimStringUtils.equals(className, ConstraintClassEnum.INHIBITCLASSID_CHAMBER.getValue())) {
            CimMachine aMachine = baseCoreFactory.getBO(CimMachine.class, anObjectId);
            List<ProcessResource> processResources = aMachine.allProcessResources();
            Validations.check(CimArrayUtils.isEmpty(processResources), retCodeConfig.getNotFoundChamber(), anObjectId);

            List<String> chambers = processResources.stream().map(MachineResource::getIdentifier).collect(Collectors.toList());
            Validations.check(Pattern.matches(".*,{2,}.*", attrib), retCodeConfig.getNotFoundEntityInhibit()); //Comma should not be greater than or equal to 2
            String[] splits = attrib.split(BizConstant.SEPARATOR_COMMA); //anObjectId可使用逗号分隔
            for (String split : splits) {
                Validations.check(chambers.stream().noneMatch(x -> x.equals(split)), retCodeConfig.getNotFoundChamber(),
                        anObjectId);
            }
        } else if (CimStringUtils.equals(className, ConstraintClassEnum.INHIBITCLASSID_OPERATION.getValue())) {
            ObjectIdentifier aRouteID = processMethod.processActiveIDGet(objCommon, anObjectId);
            /*-----------------------------------------------------------*/
            /*   Use P.O., P.O.S., P.D.                                  */
            /*-----------------------------------------------------------*/
            Inputs.ProcessOperationListForRoute strProcessOperationListForRouteIn = new Inputs.ProcessOperationListForRoute();
            strProcessOperationListForRouteIn.setRouteID(aRouteID);
            strProcessOperationListForRouteIn.setOperationID(ObjectIdentifier.emptyIdentifier());
            strProcessOperationListForRouteIn.setOperationNumber("");
            strProcessOperationListForRouteIn.setPdType("");
            strProcessOperationListForRouteIn.setSearchCount(0L);
            List<Infos.OperationNameAttributes> operationNameAttributesList = processMethod
                    .processOperationListForRoute(objCommon, strProcessOperationListForRouteIn);
            boolean findFlag = false;
            for (Infos.OperationNameAttributes operationNameAttributes : operationNameAttributesList) {
                if (CimStringUtils.equals(operationNameAttributes.getOperationNumber(), attrib)) {
                    findFlag = true;
                    break;
                }
            }
            Validations.check(!findFlag, retCodeConfig.getNotFoundOperation(), attrib);
        }

    }

    @Override
    public List<Infos.EntityInhibitDetailInfo> constraintAttributesGetDR(
            Infos.ObjCommon objCommon, Infos.EntityInhibitDetailAttributes entityInhibitAttributes,
            boolean entityInhibitReasonDetailInfoFlag) {

        //Step-0:Method Entry Log and Initialize Parameters;
        log.info("Step-0:【Method Entry】entityInhibitAttributesGetDR()");

        Boolean hInhibitSearchConditionExistFlag = false;
        Boolean hInhibitEntitySearchConditionExistFlag = false;
        Boolean hInhibitRsnInfoSearchConditionExistFlag = false;
        Boolean hInhibitSPCInfoSearchConditionExistFlag = false;
        String searchConditionForInhibit = BizConstant.EMPTY;
        //Step-1:Check input parameter and create SQL sentence for OMRESTRICT;
        log.info("Step-1:Check input parameter and create SQL sentence for OMRESTRICT");
        if (!ObjectIdentifier.isEmpty(entityInhibitAttributes.getOwnerID()) ||
                !CimStringUtils.isEmpty(entityInhibitAttributes.getReasonCode()) ||
                !CimStringUtils.isEmpty(entityInhibitAttributes.getEndTimeStamp())) {
            hInhibitSearchConditionExistFlag = true;
            if (!ObjectIdentifier.isEmpty(entityInhibitAttributes.getOwnerID())) {
                String tempSql = String.format(" C.OWNER_ID='%s' ", entityInhibitAttributes.getOwnerID().getValue());
                searchConditionForInhibit += tempSql;
            }

            if (!CimStringUtils.isEmpty(entityInhibitAttributes.getReasonCode())) {
                if (!CimStringUtils.isEmpty(searchConditionForInhibit)) {
                    searchConditionForInhibit += " AND ";
                }
                String tempSql = String.format(" C.REASON_CODE='%s' ", entityInhibitAttributes.getReasonCode());
                searchConditionForInhibit += tempSql;
            }

            if (!CimStringUtils.isEmpty(entityInhibitAttributes.getEndTimeStamp())) {
                log.info("entityInhibitDetailAttributes.endTimeStamp = {}", entityInhibitAttributes.getEndTimeStamp());
                log.info(" strObjCommonIn.strTimeStamp.reportTimeStamp = {}", objCommon.getTimeStamp().getReportTimeStamp());
                if (!CimStringUtils.isEmpty(searchConditionForInhibit)) {
                    searchConditionForInhibit += " AND ";
                }

                String searchEndTime = BizConstant.EMPTY;
                Boolean pastFlag = false;

                String endTimeStr = entityInhibitAttributes.getEndTimeStamp();
                if (CimStringUtils.equals(endTimeStr, entityInhibitAttributes.getEndTimeStamp())) {
                    if (CimStringUtils.equals(BizConstant.SP_ENDTIME_TODAY, entityInhibitAttributes
                            .getEndTimeStamp())) {
                        Timestamp tmpTimeStamp = objCommon.getTimeStamp().getReportTimeStamp();
                        long year = tmpTimeStamp.toLocalDateTime().getYear();
                        long month = tmpTimeStamp.toLocalDateTime().getMonthValue();
                        long day = tmpTimeStamp.toLocalDateTime().getDayOfMonth();

                        searchEndTime = String.format("%04d%s%02d%s%02d%s%02d%s%02d%s%02d%s%06d",
                                year, BizConstant.HYPHEN, month, BizConstant.HYPHEN, day, BizConstant.BLANK,
                                BizConstant.SP_TIMESTAMP_MAX_HOUR, BizConstant.COLON,
                                BizConstant.SP_TIMESTAMP_MAX_MINUTE, BizConstant.COLON,
                                BizConstant.SP_TIMESTAMP_MAX_SEC, BizConstant.DOT,
                                BizConstant.SP_TIMESTAMP_MAX_MILLISEC);
                    } else if (CimStringUtils.equals(BizConstant.SP_ENDTIME_PAST, entityInhibitAttributes
                            .getEndTimeStamp())) {
                        searchEndTime = objCommon.getTimeStamp().getReportTimeStamp().toString();
                        pastFlag = true;
                    } else {
                        int durationDays = Integer.parseInt(entityInhibitAttributes.getEndTimeStamp());
                        if (0 < durationDays) {
                            try {
                                String targetTimeStamp = timeStampMethod.timeStampDoCalculation(objCommon,
                                        objCommon.getTimeStamp().getReportTimeStamp().toString(),
                                        durationDays, 0, 0, 0, 0);
                                searchEndTime = targetTimeStamp;
                            } catch (ServiceException e) {
                                searchEndTime = BizConstant.SP_TIMESTAMP_NIL_OBJECT_STRING;
                            }
                        }
                    }
                    Timestamp endTime = Timestamp.valueOf(searchEndTime);
                    Validations.check(/*!StringUtils.equals(endTime.toString(),searchEndTime) ||*/
                            CimStringUtils.equals(BizConstant.SP_TIMESTAMP_NIL_OBJECT_STRING,
                                    endTime.toString()), retCodeConfig.getInvalidSearchCondition());
                } else {
                    searchEndTime = entityInhibitAttributes.getEndTimeStamp();
                    String reportTime = objCommon.getTimeStamp().getReportTimeStamp().toString();
                    if (CimDateUtils.compare(searchEndTime, reportTime) <= 0) {
                        pastFlag = true;
                    }
                }
                log.info("searchEndTime = {}", searchEndTime);
                String tempSql = String.format(" C.END_TIME<=TO_TIMESTAMP('%s','yyyy-MM-dd HH24:mi:SSxFF') ",
                        searchEndTime);
                searchConditionForInhibit += tempSql;
                searchConditionForInhibit += " AND ";

                if (CimBooleanUtils.isTrue(pastFlag)) {
                    searchConditionForInhibit += String.format(" C.END_TIME<>TO_TIMESTAMP('%s','yyyy-MM-dd HH24:mi:SSxFF') ",
                            BizConstant.SP_TIMESTAMP_NIL_OBJECT_STRING);
                } else {
                    searchConditionForInhibit += String.format(" C.END_TIME>TO_TIMESTAMP('%s','yyyy-MM-dd HH24:mi:SSxFF') ",
                            CimDateUtils.getTimestampAsString(objCommon.getTimeStamp().getReportTimeStamp()));
                }
            }

            log.info("convertedSQL for OMRESTRICT table : {}", searchConditionForInhibit);
        }

        //Step-2:Check input parameter and create SQL sentence for OMRESTRICT_ENTITY;
        String searchConditionForInhibitEntity = BizConstant.EMPTY;
        log.info("Step-2:Check input parameter and create SQL sentence for OMRESTRICT_ENTITY");
        Integer entitiesLen = CimArrayUtils.getSize(entityInhibitAttributes.getEntities());
        Boolean wildCardFlag = false;
        Boolean checkWildCardAfterSelectFlag = false;
        Integer useWildCardFlg = 0;
        String wildCardStr = BizConstant.SP_ADCSETTING_ASTERISK;

        if (entitiesLen > 0) {
            hInhibitEntitySearchConditionExistFlag = true;
            searchConditionForInhibitEntity = " (SELECT B.REFKEY FROM OMRESTRICT_ENTITY B WHERE ";
            //check a use of wiidcard;
            String useWildCardStr = StandardProperties.OM_CONSTRAINT_USE_WILDCARD.getValue();
            if (CimObjectUtils.isEmpty(useWildCardStr)) {
                useWildCardFlg = 0;
            } else {
                useWildCardFlg = Integer.parseInt(useWildCardStr);
            }

            for (int i = 0; i < entitiesLen; i++) {
                Infos.EntityIdentifier entityIdentifier = entityInhibitAttributes.getEntities().get(i);
                if (entityIdentifier == null) {
                    continue;
                }

                if (i != 0) {
                    searchConditionForInhibitEntity += " INTERSECT SELECT B.REFKEY FROM OMRESTRICT_ENTITY B WHERE ";
                }

                //check condition and create SQL sentence;
                Boolean entityIDFlag = false;
                Boolean setFlag = false;

                String objectId = entityIdentifier.getObjectID() == null ?
                        BizConstant.EMPTY :
                        entityIdentifier.getObjectID().getValue();
                if (CimStringUtils.equals(BizConstant.SP_ALL_WILD_CARD, objectId) && useWildCardFlg > 0) {
                    String tempSql = String.format("  B.ENTITY_ID LIKE '%%%s%%' ", wildCardStr);
                    searchConditionForInhibitEntity += tempSql;
                    wildCardFlag = true;
                    setFlag = true;
                }

                if (!CimStringUtils.isEmpty(entityIdentifier.getClassName())) {
                    if (setFlag) {
                        searchConditionForInhibitEntity += " AND ";
                    }

                    if (CimStringUtils.equals(BizConstant.SP_INHIBITCLASSID_ROUTE, entityIdentifier.getClassName())) {
                        searchConditionForInhibitEntity += String.format("(B.ENTITY_TYPE='%s' AND B.ENTITY_ATTRIB='%s')",
                                BizConstant.SP_INHIBITCLASSID_ROUTE, BizConstant.SP_INHIBITALLOPERATIONS);
                    } else if (BizConstant.equalsIgnoreCase(BizConstant.SP_INHIBITCLASSID_OPERATION,
                            entityIdentifier.getClassName())) {
                        searchConditionForInhibitEntity += String.format("(B.ENTITY_TYPE='%s' AND B.ENTITY_ATTRIB<>'%s')",
                                BizConstant.SP_INHIBITCLASSID_ROUTE, BizConstant.SP_INHIBITALLOPERATIONS);
                    } else {
                        searchConditionForInhibitEntity += String.format(" B.ENTITY_TYPE='%s' ",
                                entityIdentifier.getClassName());
                    }
                    entityIDFlag = true;
                }

                if (CimBooleanUtils.isTrue(entityIDFlag)
                        && !ObjectIdentifier.isEmpty(entityIdentifier.getObjectID())
                        && !CimStringUtils.equals(BizConstant.SP_ALL_WILD_CARD,
                        entityIdentifier.getObjectID().getValue())) {
                    searchConditionForInhibitEntity += " AND ";

                    String anObjectID = entityIdentifier.getObjectID().getValue();
                    String wildCardPos = BizConstant.EMPTY;

                    if (!CimObjectUtils.isEmpty(wildCardStr)) {
                        int lastPosition = anObjectID.lastIndexOf(wildCardStr);
                        if (lastPosition != -1) {
                            wildCardPos = anObjectID.substring(lastPosition);
                        }
                    }

                    if (CimObjectUtils.isEmpty(wildCardPos)) {
                        log.info("wildCard is NOT used : {}", anObjectID);
                        searchConditionForInhibitEntity += String.format(" (B.ENTITY_ID='%s' ", anObjectID);
                        if (!CimObjectUtils.isEmpty(anObjectID) && useWildCardFlg > 0) {
                            searchConditionForInhibitEntity += " OR ";

                            searchConditionForInhibitEntity += String.format(" B.ENTITY_ID like '%c%%%s%%'",
                                    anObjectID.charAt(0), wildCardStr);
                            checkWildCardAfterSelectFlag = true;
                        }
                        searchConditionForInhibitEntity += ")";
                    } else {
                        log.info("wildCard is used : {}", anObjectID);
                        /* Check WildCard position.
                         * 1. Whether the heading character is '*' or not.  Compare the address.
                         * 2. Whether WildCard is possessed at the end of the objectID or not.
                         * If objectID == xxx* , wildCardPos - anObjectID.identifier == 3 (xxx), so + 1 == 4 (xxx*)
                         ***********************************************************************************************************/
                        Validations.check(CimStringUtils.equals(wildCardPos, anObjectID),
                                retCodeConfig.getInvalidWildcardPostion());

                        Integer firstPosition = anObjectID.indexOf(wildCardStr);
                        wildCardPos = anObjectID.substring(firstPosition);

                        Integer objIdLen = anObjectID.length();
                        Integer tillWildCardLen = wildCardPos.length() - anObjectID.length() + 1;
                        Validations.check(!objIdLen.equals(tillWildCardLen),
                                retCodeConfig.getInvalidWildcardPostion());

                        String hSearchKey = anObjectID + "%";
                        searchConditionForInhibitEntity += String.format(" B.ENTITY_ID like '%s' ", hSearchKey);
                    }
                }

                if (CimObjectUtils.isEmpty(searchConditionForInhibitEntity)) {
                    hInhibitEntitySearchConditionExistFlag = false;
                }
            }
            if (CimBooleanUtils.isTrue(hInhibitEntitySearchConditionExistFlag)) {
                searchConditionForInhibitEntity += ") A ";
            } else {
                log.info("input entities is invalid ");
            }
        }

        //Step-3:Check input parameter and create SQL sentence for OMRESTRICT_RSNINFO;
        String searchConditionForInhibitRsnInfo = BizConstant.EMPTY;
        log.info("Step-3:Check input parameter and create SQL sentence for OMRESTRICT_RSNINFO");
        List<Infos.EntityInhibitReasonDetailInfo> entityInhibitReasonDetailInfos = entityInhibitAttributes
                .getEntityInhibitReasonDetailInfos();
        int rsnInfoLen = CimArrayUtils.getSize(entityInhibitReasonDetailInfos);
        if (rsnInfoLen > 0) {
            hInhibitRsnInfoSearchConditionExistFlag = true;
            searchConditionForInhibitRsnInfo = " (SELECT E.REFKEY FROM OMRESTRICT_RSNINFO E WHERE ";

            for (int nRsnCnt = 0; nRsnCnt < rsnInfoLen; nRsnCnt++) {
                Infos.EntityInhibitReasonDetailInfo rsnInfo = entityInhibitReasonDetailInfos.get(nRsnCnt);
                if (rsnInfo == null) {
                    continue;
                }
                if (nRsnCnt != 0) {
                    searchConditionForInhibitRsnInfo += " INTERSECT SELECT E.REFKEY FROM OMRESTRICT_RSNINFO E WHERE ";
                }

                //check condition and create SQL Sentence;
                Boolean bConditionSetFlag = false;
                if (!CimObjectUtils.isEmpty(rsnInfo.getRelatedLotID())) {
                    searchConditionForInhibitRsnInfo += String.format(" E.LOT_ID like '%s' ", rsnInfo.getRelatedLotID());
                    bConditionSetFlag = true;
                }

                if (!CimObjectUtils.isEmpty(rsnInfo.getRelatedControlJobID())) {
                    if (bConditionSetFlag) {
                        searchConditionForInhibitRsnInfo += " AND ";
                    }
                    searchConditionForInhibitRsnInfo += String.format(" E.CJ_ID like '%s' ", rsnInfo.getRelatedControlJobID());
                    bConditionSetFlag = true;
                }
                if (!CimObjectUtils.isEmpty(rsnInfo.getRelatedFabID())) {
                    if (bConditionSetFlag) {
                        searchConditionForInhibitRsnInfo += " AND ";
                    }
                    searchConditionForInhibitRsnInfo += String.format(" E.FAB_ID like '%s' ", rsnInfo.getRelatedFabID());
                    bConditionSetFlag = true;
                }
                if (!CimObjectUtils.isEmpty(rsnInfo.getRelatedRouteID())) {
                    if (bConditionSetFlag) {
                        searchConditionForInhibitRsnInfo += " AND ";
                    }
                    searchConditionForInhibitRsnInfo += String.format(" E.MAIN_PROCESS_ID like '%s' ",
                            rsnInfo.getRelatedRouteID());
                    bConditionSetFlag = true;
                }
                if (!CimObjectUtils.isEmpty(rsnInfo.getRelatedProcessDefinitionID())) {
                    if (bConditionSetFlag) {
                        searchConditionForInhibitRsnInfo += " AND ";
                    }
                    searchConditionForInhibitRsnInfo += String.format(" E.STEP_ID like '%s' ",
                            rsnInfo.getRelatedProcessDefinitionID());
                    bConditionSetFlag = true;
                }
                if (!CimObjectUtils.isEmpty(rsnInfo.getRelatedOperationNumber())) {
                    if (bConditionSetFlag) {
                        searchConditionForInhibitRsnInfo += " AND ";
                    }
                    searchConditionForInhibitRsnInfo += String.format(" E.OPE_NO like '%s' ",
                            rsnInfo.getRelatedOperationNumber());
                    bConditionSetFlag = true;
                }
                if (!CimObjectUtils.isEmpty(rsnInfo.getRelatedOperationPassCount())) {
                    if (bConditionSetFlag) {
                        searchConditionForInhibitRsnInfo += " AND ";
                    }
                    searchConditionForInhibitRsnInfo += String.format(" E.PASS_COUNT like '%s' ",
                            rsnInfo.getRelatedOperationPassCount());
                    bConditionSetFlag = true;
                }
                if (CimObjectUtils.isEmpty(searchConditionForInhibitRsnInfo)) {
                    hInhibitRsnInfoSearchConditionExistFlag = false;
                }
            }
            if (CimBooleanUtils.isTrue(hInhibitRsnInfoSearchConditionExistFlag)) {
                searchConditionForInhibitRsnInfo += ") F ";
            } else {
                log.info("input strEntityInhibitReasonDetailInfos is invalid ");
            }
        }

        //Step-4:Check input parameter and create SQL sentence for OMRESTRICT_RSNINFO_SPC;
        String searchConditionForInhibitSPCInfo = BizConstant.EMPTY;
        log.info("Step-4:Check input parameter and create SQL sentence for OMRESTRICT_RSNINFO_SPC");
        if (rsnInfoLen > 0) {
            for (int nRsnCnt = 0; nRsnCnt < rsnInfoLen; nRsnCnt++) {
                Infos.EntityInhibitReasonDetailInfo rsnInfo = entityInhibitReasonDetailInfos.get(nRsnCnt);
                if (rsnInfo == null) {
                    continue;
                }

                List<Infos.EntityInhibitSpcChartInfo> entityInhibitSpcChartInfos = rsnInfo.getStrEntityInhibitSpcChartInfos();
                int spcInfoLen = CimArrayUtils.getSize(entityInhibitSpcChartInfos);
                if (spcInfoLen > 0) {
                    hInhibitSPCInfoSearchConditionExistFlag = true;
                    break;
                }
            }
            if (CimBooleanUtils.isTrue(hInhibitSPCInfoSearchConditionExistFlag)) {
                searchConditionForInhibitSPCInfo = " (SELECT G.REFKEY FROM OMRESTRICT_RSNINFO_SPC G WHERE ";
                for (int nRsnCnt = 0; nRsnCnt < rsnInfoLen; nRsnCnt++) {
                    Infos.EntityInhibitReasonDetailInfo rsnInfo = entityInhibitReasonDetailInfos.get(nRsnCnt);
                    if (rsnInfo == null) {
                        continue;
                    }

                    if (nRsnCnt != 0) {
                        searchConditionForInhibitSPCInfo += " INTERSECT SELECT G.REFKEY FROM OMRESTRICT_RSNINFO_SPC G WHERE ";
                    }
                    List<Infos.EntityInhibitSpcChartInfo> entityInhibitSpcChartInfos = rsnInfo
                            .getStrEntityInhibitSpcChartInfos();
                    int spcInfoLen = CimArrayUtils.getSize(entityInhibitSpcChartInfos);
                    for (int nSpcCnt = 0; nSpcCnt < spcInfoLen; nSpcCnt++) {
                        Infos.EntityInhibitSpcChartInfo spcChartInfo = entityInhibitSpcChartInfos.get(nSpcCnt);
                        if (spcChartInfo == null) {
                            continue;
                        }

                        if (nSpcCnt != 0) {
                            searchConditionForInhibitSPCInfo += " INTERSECT SELECT G.REFKEY FROM " +
                                    "OMRESTRICT_RSNINFO_SPC G WHERE ";
                        }
                        //check condition and create SQL sentence;
                        Boolean bConditionSetFlag = false;
                        if (!CimObjectUtils.isEmpty(spcChartInfo.getRelatedSpcDcType())) {
                            if (CimBooleanUtils.isTrue(bConditionSetFlag)) {
                                searchConditionForInhibitSPCInfo += " AND ";
                            }
                            searchConditionForInhibitSPCInfo += String.format(" G.EDC_TYPE like '%s' ",
                                    spcChartInfo.getRelatedSpcDcType());
                            bConditionSetFlag = true;
                        }
                        if (!CimObjectUtils.isEmpty(spcChartInfo.getRelatedSpcChartGroupID())) {
                            if (CimBooleanUtils.isTrue(bConditionSetFlag)) {
                                searchConditionForInhibitSPCInfo += " AND ";
                            }
                            searchConditionForInhibitSPCInfo += String.format(" G.CHART_GRP_ID like '%s' ",
                                    spcChartInfo.getRelatedSpcChartGroupID());
                            bConditionSetFlag = true;
                        }
                        if (!CimObjectUtils.isEmpty(spcChartInfo.getRelatedSpcChartID())) {
                            if (CimBooleanUtils.isTrue(bConditionSetFlag)) {
                                searchConditionForInhibitSPCInfo += " AND ";
                            }
                            searchConditionForInhibitSPCInfo += String.format(" G.CHART_ID like '%s' ",
                                    spcChartInfo.getRelatedSpcChartID());
                            bConditionSetFlag = true;
                        }
                        if (!CimObjectUtils.isEmpty(spcChartInfo.getRelatedSpcChartType())) {
                            if (CimBooleanUtils.isTrue(bConditionSetFlag)) {
                                searchConditionForInhibitSPCInfo += " AND ";
                            }
                            String chartTypeInitial = ""; //todo:convert spc chart type text;

                            searchConditionForInhibitSPCInfo += String.format(" G.CHART_TYPE like '%s' ",
                                    spcChartInfo.getRelatedSpcChartType());
                            bConditionSetFlag = true;
                        }
                        if (CimObjectUtils.isEmpty(searchConditionForInhibitSPCInfo)) {
                            hInhibitSPCInfoSearchConditionExistFlag = false;
                        }
                    }
                }
            }
            if (CimBooleanUtils.isTrue(hInhibitSPCInfoSearchConditionExistFlag)) {
                searchConditionForInhibitSPCInfo += ") H ";
            } else {
                log.info("input strEntityInhibitSpcChartInfos is invalid ");
            }
        }

        //Step-5: Construct SQL sentence;
        log.info("Step-5: Construct SQL sentence");
        Validations.check(CimBooleanUtils.isFalse(hInhibitSearchConditionExistFlag)
                && CimBooleanUtils.isFalse(hInhibitEntitySearchConditionExistFlag)
                && CimBooleanUtils.isFalse(hInhibitRsnInfoSearchConditionExistFlag)
                && CimBooleanUtils.isFalse(hInhibitSPCInfoSearchConditionExistFlag), retCodeConfig.getInvalidSearchCondition());

        Boolean bSetFlag = false;
        /*String tmpBuf = "SELECT P.ID, P.RESTRICT_ID, P.DESCRIPTION, " +
                "P.START_TIME, P.END_TIME, P.CHANGE_TIME, P.OWNER_ID, P.OWNER_OBJ, P.RSN_CODE, " +
                "P.RSN_OBJ, P.CLAIM_MEMO, S1.D_SEQNO, S1.ENTITY_TYPE, S1.ENTITY_ID, " +
                "S1.ENTITY_ATTRIB, COALESCE(S2.D_SEQNO,0), COALESCE(S2.SUB_LOT_TYPE,''), COALESCE(S3.DESCRIPTION,'') " +
                "FROM ( SELECT OMRESTRICT.ID, OMRESTRICT.RESTRICT_ID, OMRESTRICT.DESCRIPTION, " +
                "OMRESTRICT.START_TIME, OMRESTRICT.END_TIME, OMRESTRICT.CHANGE_TIME, OMRESTRICT.OWNER_ID, OMRESTRICT.OWNER_OBJ, " +
                "OMRESTRICT.RSN_CODE, OMRESTRICT.RSN_OBJ, OMRESTRICT.CLAIM_MEMO FROM OMRESTRICT WHERE OMRESTRICT.ID IN ( ";*/
        //String tmpPreBuf = "SELECT COUNT(P.ID) FROM ( ";
        String tmpBuf = "SELECT P.ID, P.RESTRICT_ID, P.DESCRIPTION, P.START_TIME, P.END_TIME, P.CHANGE_TIME, P.OWNER_ID, P.OWNER_RKEY, " +
                "P.REASON_CODE, P.REASON_RKEY, P.TRX_MEMO, S1.IDX_NO, S1.ENTITY_TYPE, S1.ENTITY_ID, S1.ENTITY_ATTRIB, " +
                "COALESCE(S2.IDX_NO, 0), COALESCE(S2.SUB_LOT_TYPE, ''), COALESCE(S3.DESCRIPTION, '') FROM ( SELECT B.ID, B.RESTRICT_ID, " +
                "B.DESCRIPTION, B.START_TIME, B.END_TIME, B.CHANGE_TIME, B.OWNER_ID, B.OWNER_RKEY, B.REASON_CODE, B.REASON_RKEY, B.TRX_MEMO " +
                "FROM ( SELECT A.ID, A.RESTRICT_ID, A.DESCRIPTION, A.START_TIME, A.END_TIME, A.CHANGE_TIME, A.OWNER_ID, A.OWNER_RKEY, A.REASON_CODE, " +
                "A.REASON_RKEY, A.TRX_MEMO, ROWNUM AS RN FROM (SELECT OMRESTRICT.ID, OMRESTRICT.RESTRICT_ID, OMRESTRICT.DESCRIPTION,OMRESTRICT.START_TIME," +
                "OMRESTRICT.END_TIME , OMRESTRICT.CHANGE_TIME,OMRESTRICT.OWNER_ID,OMRESTRICT.OWNER_RKEY,OMRESTRICT.REASON_CODE,OMRESTRICT.REASON_RKEY ," +
                "OMRESTRICT.TRX_MEMO FROM OMRESTRICT WHERE OMRESTRICT.FUNC_RULE='BLIST' AND SPECIFIC_TOOL=0 AND OMRESTRICT.ID IN (";

        String tmpPreBuf = "SELECT COUNT(*) FROM (SELECT * FROM OMRESTRICT WHERE OMRESTRICT.FUNC_RULE='BLIST' AND SPECIFIC_TOOL=0 AND OMRESTRICT.ID IN (";
        if (CimBooleanUtils.isTrue(hInhibitSearchConditionExistFlag)) {
            String tmpSelectBuf = String.format("  SELECT C.ID FROM OMRESTRICT C WHERE OMRESTRICT.FUNC_RULE='BLIST' AND SPECIFIC_TOOL=0 AND %s", searchConditionForInhibit);
            tmpBuf += tmpSelectBuf;
            tmpPreBuf += tmpSelectBuf;
            bSetFlag = true;
        }
        if (CimBooleanUtils.isTrue(hInhibitEntitySearchConditionExistFlag)) {
            if (CimBooleanUtils.isTrue(bSetFlag)) {
                tmpBuf += " INTERSECT ";
                tmpPreBuf += " INTERSECT ";
            }
            String tmpSelectBuf = String.format("SELECT D.REFKEY FROM %s, OMRESTRICT_ENTITY D WHERE A.REFKEY = D.REFKEY GROUP BY D.REFKEY ", searchConditionForInhibitEntity);
            tmpBuf += tmpSelectBuf;
            tmpPreBuf += tmpSelectBuf;
            if (CimBooleanUtils.isFalse(wildCardFlag)) {
                String tmpHavingBuf = String.format(" HAVING COUNT(D.REFKEY) = %d", entitiesLen);
                tmpBuf += tmpHavingBuf;
                tmpPreBuf += tmpHavingBuf;
            }
            bSetFlag = true;
        }
        if (CimBooleanUtils.isTrue(hInhibitRsnInfoSearchConditionExistFlag)) {
            if (CimBooleanUtils.isTrue(bSetFlag)) {
                tmpBuf += " INTERSECT ";
                tmpPreBuf += " INTERSECT ";
            }
            String tmpSelectBuf = String.format("SELECT E.REFKEY FROM %s, OMRESTRICT_RSNINFO E WHERE " +
                    "F.REFKEY =E.REFKEY GROUP BY E.REFKEY ", searchConditionForInhibitRsnInfo);
            tmpBuf += tmpSelectBuf;
            tmpPreBuf += tmpSelectBuf;
            bSetFlag = true;
        }
        if (CimBooleanUtils.isTrue(hInhibitSPCInfoSearchConditionExistFlag)) {
            if (CimBooleanUtils.isTrue(bSetFlag)) {
                tmpBuf += " INTERSECT ";
                tmpPreBuf += " INTERSECT ";
            }
            String tmpSelectBuf = String.format("SELECT G.REFKEY FROM %s, OMRESTRICT_RSNINFO_SPC G " +
                    "WHERE H.REFKEY =G.REFKEY GROUP BY G.REFKEY ", searchConditionForInhibitSPCInfo);
            tmpBuf += tmpSelectBuf;
            tmpPreBuf += tmpSelectBuf;
            bSetFlag = true;
        }

        String hFrCodeCateGoryID = BizConstant.SP_REASONCAT_ENTITYINHIBIT;
        String configInhibitMaxSeqLen = StandardProperties.OM_CONSTRAINT_MAX_LIST_INQ.getValue();
        int fetchLimitCount = CimStringUtils.isEmpty(configInhibitMaxSeqLen)
                ? 0 : Integer.parseInt(configInhibitMaxSeqLen);
        if (fetchLimitCount < 1) {
            fetchLimitCount = Integer.parseInt(BizConstant.CONST_TWO_THOUSAND);
        }
        SearchCondition searchCondition = new SearchCondition();
        Integer from = 0;
        Integer to = fetchLimitCount;
        Integer page = 1;

        if ((null != searchCondition) && (searchCondition.getPage() != null) && (searchCondition.getSize() != null)) {
            from = (searchCondition.getPage() - 1) * (searchCondition.getSize());
            to = from + searchCondition.getSize();
            page = searchCondition.getPage();
        }

        tmpBuf += String.format("\t )ORDER BY  OMRESTRICT.ID ) A  WHERE ROWNUM <= %d ) B WHERE B.RN > %d ) P\n" +
                "\tLEFT JOIN OMRESTRICT_ENTITY S1 ON P.ID = S1.REFKEY\n" +
                "\tLEFT JOIN OMRESTRICT_LOTTP S2 ON P.ID = S2.REFKEY\n" +
                "\tLEFT JOIN OMCODE S3\n" +
                "\tON S3.CODETYPE_ID = '%s' AND P.REASON_CODE = S3.CODE_ID\n" +
                "ORDER BY P.ID, S1.IDX_NO, S2.IDX_NO", to, from, hFrCodeCateGoryID);

        tmpPreBuf += String.format("\t) ) P LEFT JOIN OMCODE S3 ON S3.CODETYPE_ID = '%s' AND P.REASON_CODE = S3.CODE_ID", hFrCodeCateGoryID);
        Object[] queryResult = cimJpaRepository.queryOne(tmpPreBuf);
        Integer contextSize = (queryResult == null || queryResult.length == 0) ? 0 : Integer.parseInt(queryResult[0].toString());
       /* tmpBuf += String.format(") ORDER BY OMRESTRICT.ID) P " +
                "LEFT JOIN OMRESTRICT_ENTITY S1 ON P.ID = S1.REFKEY " +
                "LEFT JOIN OMRESTRICT_LOTTP S2 ON P.ID = S2.REFKEY " +
                "LEFT JOIN OMCODE S3 ON S3.CODETYPE_ID = '%s' AND P.RSN_CODE = S3.CODE_ID " +
                "ORDER BY P.ID, S1.D_SEQNO, S2.D_SEQNO ", hFrCodeCateGoryID);*/

        log.info("tmpBufSql : {}", tmpBuf);
        List<Infos.QueriedEntityInhibit> queriedEntityInhibits = new ArrayList<>();
        List<Object[]> results = cimJpaRepository.query(tmpBuf);
        if (CimArrayUtils.getSize(results) > 0) {
            for (int i = 0; i < CimArrayUtils.getSize(results); i++) {
                Object[] objects = (Object[]) results.get(i);

                Infos.QueriedEntityInhibit queriedEntityInhibit = new Infos.QueriedEntityInhibit();

                queriedEntityInhibit.setId(String.valueOf(objects[0]));
                queriedEntityInhibit.setEntityInhibitID(String.valueOf(objects[1]));
                queriedEntityInhibit.setEntityInhibitDescription(String.valueOf(objects[2]));
                queriedEntityInhibit.setEntityInhibitStartTime(String.valueOf(objects[3]));
                queriedEntityInhibit.setEntityInhibitEndTime(String.valueOf(objects[4]));
                queriedEntityInhibit.setEntityInhibitChangeTime(String.valueOf(objects[5]));
                queriedEntityInhibit.setEntityInhibitOwnerID(String.valueOf(objects[6]));
                queriedEntityInhibit.setEntityInhibitOwnerObj(String.valueOf(objects[7]));
                queriedEntityInhibit.setEntityInhibitRsnCode(String.valueOf(objects[8]));
                queriedEntityInhibit.setEntityInhibitRsnObj(String.valueOf(objects[9]));
                queriedEntityInhibit.setEntityInhibitClaimMemo(String.valueOf(objects[10]));
                queriedEntityInhibit.setEntityInhibitEntityDataSeqNo(Integer.valueOf(objects[11].toString()));
                queriedEntityInhibit.setEntityInhibitEntityClassName(String.valueOf(objects[12]));
                queriedEntityInhibit.setEntityInhibitEntityID(String.valueOf(objects[13]));
                queriedEntityInhibit.setEntityInhibitEntityAttrib(String.valueOf(objects[14]));
                queriedEntityInhibit.setEntityInhibitSlotTPDataSeqNo(Integer.valueOf(objects[15].toString()));
                queriedEntityInhibit.setEntityInhibitSlotTPSubLotType(String.valueOf(objects[16]));
                queriedEntityInhibit.setCodeDescription(String.valueOf(objects[17]));

                queriedEntityInhibits.add(queriedEntityInhibit);
            }
        }
        //Step-6:Get record from OMRESTRICT table;
        log.info("Step-6:Get record from OMRESTRICT table");

        Integer expandRecordLen = Integer.parseInt(BizConstant.CONST_FIVE_HUNDRED);
        Integer listCount = 0;
        String checkKey = BizConstant.EMPTY;
        Integer checkNo = 0;
        Boolean countSubLotTypeFlag = true;
        //boolean firstFlag = true;


        log.info("Maximum of Fetch Count = {}", fetchLimitCount);

        log.info("listCount : {}", listCount);
        List<String> sysKeySeq = new ArrayList<>();
        int count = 0;
        int entityInhibitionsCount = CimArrayUtils.getSize(queriedEntityInhibits);
        List<Infos.EntityInhibitDetailInfo> tmpEntityInhibitDetailInfos = new ArrayList<>();

        for (int i = 0; i < entityInhibitionsCount; i++) {
            Infos.QueriedEntityInhibit queriedEntityInhibit = queriedEntityInhibits.get(i);

            log.info("EntityInhibit_ID : {}", queriedEntityInhibit.getEntityInhibitID());
            log.info("checkKey : {}", checkKey);

            if (!CimStringUtils.equals(checkKey, queriedEntityInhibit.getId())) {
                log.info("this is a new checkKey.");

                //intialize;
                Infos.EntityInhibitDetailInfo tmpEntityInfo = new Infos.EntityInhibitDetailInfo();
                Infos.EntityInhibitDetailAttributes tmpEntityInhibitAttributes = new Infos.EntityInhibitDetailAttributes();
                List<Infos.EntityIdentifier> tmpEntities = new ArrayList<>();
                List<String> tmpSubLotTypes = new ArrayList<>();

                tmpEntityInhibitAttributes.setEntities(tmpEntities);
                tmpEntityInfo.setEntityInhibitDetailAttributes(tmpEntityInhibitAttributes);
                //tmpEntityInhibitDetailInfos.add(tmpEntityInfo);

                //get checkKey and checkNo;
                checkKey = queriedEntityInhibit.getId();
                sysKeySeq.add(checkKey);
                checkNo = queriedEntityInhibit.getEntityInhibitEntityDataSeqNo();
                countSubLotTypeFlag = true;

                if (listCount <= i + 1) {
                    listCount += expandRecordLen;
                }
                count++;

                if (i + 1 >= fetchLimitCount) {
                    log.info("Length of entityinhibit reached the maximum = {}", i + 1);
                    break;
                }
                tmpEntityInfo.setEntityInhibitID(new ObjectIdentifier(queriedEntityInhibit.getEntityInhibitID(),
                        queriedEntityInhibit.getId()));
                tmpEntityInhibitAttributes.setStartTimeStamp(queriedEntityInhibit.getEntityInhibitStartTime());
                tmpEntityInhibitAttributes.setEndTimeStamp(queriedEntityInhibit.getEntityInhibitEndTime());
                tmpEntityInhibitAttributes.setReasonCode(queriedEntityInhibit.getEntityInhibitRsnCode());
                tmpEntityInhibitAttributes.setReasonDesc(queriedEntityInhibit.getCodeDescription());
                tmpEntityInhibitAttributes.setMemo(queriedEntityInhibit.getEntityInhibitClaimMemo());
                tmpEntityInhibitAttributes.setOwnerID(new ObjectIdentifier(queriedEntityInhibit.getEntityInhibitOwnerID(),
                        queriedEntityInhibit.getEntityInhibitOwnerObj()));
                tmpEntityInhibitAttributes.setClaimedTimeStamp(queriedEntityInhibit.getEntityInhibitChangeTime());

                List<Infos.EntityInhibitExceptionLotInfo> exceptionLotInfoList = new ArrayList<>();
                String queryLot = "SELECT * FROM OMRESTRICT_EXPLOT WHERE REFKEY = ?1";
                List<CimRestrictionExpLotDO> exceptionLots = cimJpaRepository.query(queryLot, CimRestrictionExpLotDO.class, queriedEntityInhibit.getId());
                if (!CimObjectUtils.isEmpty(exceptionLots)) {
                    for (CimRestrictionExpLotDO item : exceptionLots) {
                        Infos.EntityInhibitExceptionLotInfo exceptionLotInfo = new Infos.EntityInhibitExceptionLotInfo();
                        exceptionLotInfo.setLotID(ObjectIdentifier.build(item.getExceptLotID(), item.getExceptLotObj()));
                        exceptionLotInfo.setSingleTriggerFlag(item.getSingleTrigFlag());
                        exceptionLotInfo.setUsedFlag(item.getUsedFlag());
                        exceptionLotInfo.setClaimUserID(ObjectIdentifier.build(item.getClaimUserID(), item.getClaimUserObj()));
                        exceptionLotInfo.setClaimMemo(item.getClaimMemo());

                        String claimTime = (item.getClaimTime() == null) ?
                                BizConstant.EMPTY :
                                item.getClaimTime().toString();
                        exceptionLotInfo.setClaimTime(claimTime);
                        exceptionLotInfoList.add(exceptionLotInfo);
                    }
                }
                tmpEntityInhibitAttributes.setEntityInhibitExceptionLotInfos(exceptionLotInfoList);


                /*tmpEntityInhibitAttributes.setEntityInhibitReasonDetailInfos();*/


                checkNo = queriedEntityInhibit.getEntityInhibitEntityDataSeqNo();

                Infos.EntityIdentifier tmpEntityIdentifier = new Infos.EntityIdentifier();
                ObjectIdentifier entityID = new ObjectIdentifier(queriedEntityInhibit.getEntityInhibitEntityID(),
                        queriedEntityInhibit.getEntityInhibitEntityObj());
                tmpEntityIdentifier.setObjectID(entityID);
                tmpEntityIdentifier.setClassName(queriedEntityInhibit.getEntityInhibitEntityClassName());
                tmpEntityIdentifier.setAttribution(queriedEntityInhibit.getEntityInhibitEntityAttrib());

                tmpEntities.add(tmpEntityIdentifier);

                if (!CimObjectUtils.isEmpty(queriedEntityInhibit.getEntityInhibitSlotTPSubLotType())) {
                    tmpSubLotTypes.add(queriedEntityInhibit.getEntityInhibitSlotTPSubLotType());
                    tmpEntityInhibitAttributes.setSubLotTypes(tmpSubLotTypes);
                }
                tmpEntityInhibitDetailInfos.add(tmpEntityInfo);
            } else {
                if (checkNo != queriedEntityInhibit.getEntityInhibitEntityDataSeqNo()) {
                    log.info("this is a new EntityInhibit_Entity_dSeqNo");

                    Infos.EntityIdentifier tmpEntityIdentifier = new Infos.EntityIdentifier();
                    ObjectIdentifier entityID = new ObjectIdentifier(queriedEntityInhibit.getEntityInhibitEntityID(),
                            queriedEntityInhibit.getEntityInhibitEntityObj());
                    tmpEntityIdentifier.setObjectID(entityID);
                    tmpEntityIdentifier.setClassName(queriedEntityInhibit.getEntityInhibitEntityClassName());
                    tmpEntityIdentifier.setAttribution(queriedEntityInhibit.getEntityInhibitEntityAttrib());

                    //get index to add entity for the same entity inhibit;
                    if (CimArrayUtils.isNotEmpty(tmpEntityInhibitDetailInfos)) {
                        for (Infos.EntityInhibitDetailInfo item : tmpEntityInhibitDetailInfos) {
                            if (CimStringUtils.equals(item.getEntityInhibitID().getReferenceKey(), checkKey)) {
                                item.getEntityInhibitDetailAttributes().getEntities().add(tmpEntityIdentifier);
                                break;
                            }
                        }
                    }
                    countSubLotTypeFlag = false;

                    checkNo = queriedEntityInhibit.getEntityInhibitEntityDataSeqNo();
                } else {
                    if (CimObjectUtils.isEmpty(queriedEntityInhibit.getEntityInhibitSlotTPSubLotType())) {
                        continue;
                    } else {
                        if (CimBooleanUtils.isTrue(countSubLotTypeFlag)) {
                            log.info("countSubLotTypeFlag == TRUE");

                            //get index to add sub lot type for the same entity inhibit;
                            if (CimArrayUtils.isNotEmpty(tmpEntityInhibitDetailInfos)) {
                                for (Infos.EntityInhibitDetailInfo item : tmpEntityInhibitDetailInfos) {
                                    if (CimStringUtils.equals(item.getEntityInhibitID().getReferenceKey(), checkKey)) {
                                        item.getEntityInhibitDetailAttributes().getSubLotTypes().add(queriedEntityInhibit.getEntityInhibitSlotTPSubLotType());
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        //Step-7:  Set return structure;
        log.info("Step-7:  Set return structure");
        if (count > 0) {
            if (CimBooleanUtils.isTrue(checkWildCardAfterSelectFlag)
                    && useWildCardFlg != 0
                    && (!CimObjectUtils.isEmpty(wildCardStr))) {
                int recordLen = CimArrayUtils.getSize(tmpEntityInhibitDetailInfos);

                List<Infos.EntityInhibitDetailInfo> checkEntityInhibitInfos = new ArrayList<>();
                List<String> checkSyskeySeq = new ArrayList<>();
                int markCount = 0;
                Boolean bInhibitFlag = true;

                for (int j = 0; j < recordLen; j++) {
                    Infos.EntityInhibitDetailAttributes checkEntityInhibitAttributes = tmpEntityInhibitDetailInfos.get(j).getEntityInhibitDetailAttributes();
                    int recordEntitiesLen = CimArrayUtils.getSize(checkEntityInhibitAttributes.getEntities());
                    for (int k = 0; k < recordEntitiesLen; k++) {
                        for (int i = 0; i < entitiesLen; i++) {
                            String classNameStr = BizConstant.EMPTY;
                            if (CimStringUtils.equals(BizConstant.SP_INHIBITCLASSID_OPERATION, checkEntityInhibitAttributes.getEntities().get(i).getClassName())) {
                                classNameStr = BizConstant.SP_INHIBITCLASSID_ROUTE;
                            } else {
                                classNameStr = checkEntityInhibitAttributes.getEntities().get(i).getClassName();
                            }

                            if (classNameStr.compareTo(checkEntityInhibitAttributes.getEntities().get(k).getClassName()) == 0) {
                                int lastIndex = checkEntityInhibitAttributes.getEntities().get(i).getObjectID().getValue().indexOf(wildCardStr);
                                String wildCardExist = checkEntityInhibitAttributes.getEntities().get(i).getObjectID().getValue().substring(lastIndex);

                                if (!CimObjectUtils.isEmpty(wildCardExist)) {
                                    break;
                                }

                                int lastPosition = checkEntityInhibitAttributes.getEntities().get(k).getObjectID().getValue().indexOf(wildCardStr);
                                String wildCardPos = checkEntityInhibitAttributes.getEntities().get(k).getObjectID().getValue().substring(lastPosition);

                                int compareLength = 0;
                                if (CimObjectUtils.isEmpty(wildCardPos))   // WildCard is not used.
                                {
                                    break;
                                } else                        // WildCard is used;
                                {
                                    compareLength = wildCardPos.length() - checkEntityInhibitAttributes.getEntities().get(k).getObjectID().getValue().length();
                                }

                                if (checkEntityInhibitAttributes.getEntities().get(i).getObjectID().getValue().compareTo(checkEntityInhibitAttributes.getEntities().get(k).getObjectID().getValue()) == 0) {
                                    bInhibitFlag = false;
                                    break;
                                }
                            }
                        }

                        if (CimBooleanUtils.isFalse(bInhibitFlag)) {
                            break;
                        }
                    }

                    if (CimBooleanUtils.isTrue(bInhibitFlag)) {
                        checkEntityInhibitInfos.add(tmpEntityInhibitDetailInfos.get(j));
                        checkSyskeySeq.add(sysKeySeq.get(j));
                    }

                    bInhibitFlag = true;
                }
            }

            //Step-8:Set Entity Inhibit Reason Detail Information;
            log.info("Step-8:Set Entity Inhibit Reason Detail Information");
            if (CimBooleanUtils.isTrue(entityInhibitReasonDetailInfoFlag)) {
                log.info("entityInhibitReasonDetailInfoFlag == TRUE");
                String wspcLinkUrl = StandardProperties.OM_SPC_URL.getValue();
                log.info("OM_SPC_URL : {}", wspcLinkUrl);

                int inhibitInfoLen = CimArrayUtils.getSize(tmpEntityInhibitDetailInfos);
                for (int inhibitCnt = 0; inhibitCnt < inhibitInfoLen; inhibitCnt++) {
                    Infos.EntityInhibitDetailInfo loopEntityInhibitDetailInfo = tmpEntityInhibitDetailInfos.get(inhibitCnt);
                    if (loopEntityInhibitDetailInfo == null) {
                        continue;
                    }
                    log.info("EntityInhibit_ID = {}", loopEntityInhibitDetailInfo.getEntityInhibitID());
                    List<CimRestrictionRsnInfoDO> entityInhibitRsnInfos = cimJpaRepository.query("SELECT * FROM OMRESTRICT_RSNINFO WHERE REFKEY = ?1", CimRestrictionRsnInfoDO.class, loopEntityInhibitDetailInfo.getEntityInhibitID().getReferenceKey());
                    int hCount = CimArrayUtils.getSize(entityInhibitRsnInfos);
                    log.info("hCount = {}", hCount);
                    if (hCount == 0) {
                        log.info("hCount == 0 Continue!!!");
                        continue;
                    }
                    //need to confirm:allEntityInhibitReasonDetailInfoFor;
                    List<Constrain.EntityInhibitReasonDetailInfo> reasonDetailInfos = restrictionManager.allEntityInhibitReasonDetailInfoFor(loopEntityInhibitDetailInfo.getEntityInhibitID());

                    int rsnDetailInfoLen = CimArrayUtils.getSize(reasonDetailInfos);
                    log.info("rsnDetailInfoLen : {}", rsnDetailInfoLen);

                    Infos.EntityInhibitDetailAttributes outEntityInhibitDetailAttributes = tmpEntityInhibitDetailInfos.get(inhibitCnt).getEntityInhibitDetailAttributes();
                    List<Infos.EntityInhibitReasonDetailInfo> outEntityInhibitReasonDetailInfos = new ArrayList<>();
                    outEntityInhibitDetailAttributes.setEntityInhibitReasonDetailInfos(outEntityInhibitReasonDetailInfos);
                    for (int rsnCnt = 0; rsnCnt < rsnDetailInfoLen; rsnCnt++) {
                        Infos.EntityInhibitReasonDetailInfo outEntityInhibitReasonDetailInfo = new Infos.EntityInhibitReasonDetailInfo();
                        outEntityInhibitReasonDetailInfo.setRelatedLotID(reasonDetailInfos.get(rsnCnt).getRelatedLotID());
                        outEntityInhibitReasonDetailInfo.setRelatedControlJobID(reasonDetailInfos.get(rsnCnt).getRelatedControlJobID());
                        outEntityInhibitReasonDetailInfo.setRelatedFabID(reasonDetailInfos.get(rsnCnt).getRelatedFabID());
                        outEntityInhibitReasonDetailInfo.setRelatedRouteID(reasonDetailInfos.get(rsnCnt).getRelatedRouteID());
                        outEntityInhibitReasonDetailInfo.setRelatedProcessDefinitionID(reasonDetailInfos.get(rsnCnt).getRelatedProcessDefinitionID());
                        outEntityInhibitReasonDetailInfo.setRelatedOperationNumber(reasonDetailInfos.get(rsnCnt).getRelatedOperationNumber());
                        outEntityInhibitReasonDetailInfo.setRelatedOperationPassCount(reasonDetailInfos.get(rsnCnt).getRelatedOperationPassCount());


                        int spcChartInfoLen = CimArrayUtils.getSize(reasonDetailInfos.get(rsnCnt).getStrEntityInhibitSpcChartInfos());

                        log.info("spcChartInfoLen : {}", spcChartInfoLen);

                        List<Infos.EntityInhibitSpcChartInfo> outStrEntityInhibitSpcChartInfos = new ArrayList<>();

                        for (int spcInfoCnt = 0; spcInfoCnt < spcChartInfoLen; spcInfoCnt++) {
                            Infos.EntityInhibitSpcChartInfo outEntityInhibitSpcChartInfo = new Infos.EntityInhibitSpcChartInfo();

                            Constrain.EntityInhibitSpcChartInfo tmpEntityInhibitSpcChartInfo = reasonDetailInfos.get(rsnCnt).getStrEntityInhibitSpcChartInfos().get(spcInfoCnt);
                            outEntityInhibitSpcChartInfo.setRelatedSpcDcType(tmpEntityInhibitSpcChartInfo.getRelatedSpcDcType());
                            outEntityInhibitSpcChartInfo.setRelatedSpcChartGroupID(tmpEntityInhibitSpcChartInfo.getRelatedSpcChartGroupID());
                            outEntityInhibitSpcChartInfo.setRelatedSpcChartID(tmpEntityInhibitSpcChartInfo.getRelatedSpcChartID());
                            outEntityInhibitSpcChartInfo.setRelatedSpcChartType(tmpEntityInhibitSpcChartInfo.getRelatedSpcChartType());
                            String tmpURL = String.format(wspcLinkUrl, tmpEntityInhibitSpcChartInfo.getRelatedSpcDcType(), tmpEntityInhibitSpcChartInfo.getRelatedSpcChartGroupID(),
                                    tmpEntityInhibitSpcChartInfo.getRelatedSpcChartID(), tmpEntityInhibitSpcChartInfo.getRelatedSpcChartType());
                            outEntityInhibitSpcChartInfo.setRelatedSpcChartUrl(tmpURL);

                            outStrEntityInhibitSpcChartInfos.add(outEntityInhibitSpcChartInfo);
                        }
                        outEntityInhibitReasonDetailInfo.setStrEntityInhibitSpcChartInfos(outStrEntityInhibitSpcChartInfos);
                        outEntityInhibitReasonDetailInfos.add(outEntityInhibitReasonDetailInfo);
                    }
                }
            }
        }
        return tmpEntityInhibitDetailInfos;
    }

    @Override
    public List<Infos.EntityInhibitDetailInfo> constraintCancelReq(Infos.ObjCommon objCommon, List<Infos.EntityInhibitDetailInfo> entityInhibitions) {
        int numOfEntities = CimArrayUtils.getSize(entityInhibitions);
        List<Infos.EntityInhibitDetailInfo> outEntityInhibitions = new ArrayList<>();
        for (int i = 0; i < numOfEntities; i++) {
            Infos.EntityInhibitDetailInfo entityInhibitIn = entityInhibitions.get(i);
            // check if an entity inhibit exists;
            CimRestriction anEntityInhibit = baseCoreFactory.getBO(CimRestriction.class, entityInhibitIn.getEntityInhibitID());
            Validations.check(CimObjectUtils.isEmpty(anEntityInhibit), retCodeConfig.getNotFoundEntityInhibit());

            // set entity inhibit attributes into output parameter：
            Constrain.EntityInhibitRecord aRecord = anEntityInhibit.getInhibitRecord();
            //------------------------------------------------------------------------------------------------------------------------
            // If RC of inhibit is SOOR and SP_SOOR_INHIBIT_CANCEL_BY_ACK is 1, then it can be released only when one of the following
            // conditions are fulfilled.
            // 1. Request is from WSPC server.
            // 2. User ID of request is pre-defined SPC administrator user ID.
            //------------------------------------------------------------------------------------------------------------------------
            if (CimStringUtils.equals(BizConstant.SP_REASON_SPCOUTOFRANGEINHIBIT, aRecord.getReasonCode().getValue())) {
                // todo remove env , SOOR_INHIBIT_CANCEL_BY_ACK = '1' logic
            }

            Infos.EntityInhibitDetailInfo outEntityInhibitDetailInfo = new Infos.EntityInhibitDetailInfo();
            outEntityInhibitions.add(outEntityInhibitDetailInfo);
            ObjectIdentifier entityInhibitID = new ObjectIdentifier(anEntityInhibit.getIdentifier(), anEntityInhibit.getPrimaryKey());
            outEntityInhibitDetailInfo.setEntityInhibitID(entityInhibitID);

            Infos.EntityInhibitDetailAttributes entityInhibitAttributes = new Infos.EntityInhibitDetailAttributes();
            outEntityInhibitDetailInfo.setEntityInhibitDetailAttributes(entityInhibitAttributes);

            List<Infos.EntityIdentifier> entities = new ArrayList<>();
            int numOfClasses = CimArrayUtils.getSize(aRecord.getEntities());
            for (int j = 0; j < numOfClasses; j++) {
                //-----------------------------------------------------
                // Convert Class Name for Inhibition of RouteOperation.
                // If className was "Route" and attrib is not "*",
                // It means RouteOperation Inhibition.
                //-----------------------------------------------------
                Constrain.EntityIdentifier entityIdentifier = aRecord.getEntities().get(j);
                if (entityIdentifier == null) {
                    continue;
                }
                String recordClassName = entityIdentifier.getClassName();
                String recordAttrib = entityIdentifier.getAttrib();

                Infos.EntityIdentifier outEntityIdentifier = new Infos.EntityIdentifier();
                if (CimStringUtils.equals(BizConstant.SP_INHIBITCLASSID_ROUTE, recordClassName) &&
                        !CimStringUtils.isEmpty(recordAttrib) && !CimStringUtils.equals(BizConstant.SP_ADCSETTING_ASTERISK, recordAttrib)) {
                    log.info("Class Name Conversion. Route => Operation.");
                    outEntityIdentifier.setClassName(BizConstant.SP_INHIBITCLASSID_OPERATION);
                    outEntityIdentifier.setAttribution(recordAttrib);
                } else if (CimStringUtils.equals(BizConstant.SP_INHIBITCLASSID_CHAMBER, recordClassName)
                        && !CimObjectUtils.isEmpty(recordAttrib)) {
                    //-----------------------------------------------------
                    // If className is "Chamber", enter the attribute to
                    // output.
                    //-----------------------------------------------------
                    outEntityIdentifier.setClassName(recordClassName);
                    outEntityIdentifier.setAttribution(recordAttrib);
                } else {
                    outEntityIdentifier.setClassName(recordClassName);
                }
                outEntityIdentifier.setObjectID(new ObjectIdentifier(entityIdentifier.getObjectId()));
                entities.add(outEntityIdentifier);
            }
            entityInhibitAttributes.setEntities(entities);
            entityInhibitAttributes.setSubLotTypes(aRecord.getSubLotTypes());
            String startTime = (aRecord.getStartTimeStamp() != null) ? aRecord.getStartTimeStamp().toString() : BizConstant.EMPTY;
            entityInhibitAttributes.setStartTimeStamp(startTime);
            String endTime = (aRecord.getEndTimeStamp() != null) ? aRecord.getEndTimeStamp().toString() : BizConstant.EMPTY;
            entityInhibitAttributes.setEndTimeStamp(endTime);
            String claimedTime = (aRecord.getChangedTimeStamp() != null) ? aRecord.getChangedTimeStamp().toString() : BizConstant.EMPTY;
            entityInhibitAttributes.setClaimedTimeStamp(claimedTime);
            String reasonCode = (aRecord.getReasonCode() != null) ? aRecord.getReasonCode().getValue() : BizConstant.EMPTY;
            entityInhibitAttributes.setReasonCode(reasonCode);
            entityInhibitAttributes.setOwnerID(aRecord.getOwner());
            entityInhibitAttributes.setMemo(aRecord.getClaimMemo());
            entityInhibitAttributes.setFunctionRule(aRecord.getFunctionRule());
            entityInhibitAttributes.setSpecTool(aRecord.getSpecificTool());
            entityInhibitAttributes.setExceptionEntities(Optional.ofNullable(aRecord.getExceptionEntities())
                    .map(entityList -> entityList
                            .parallelStream()
                            .map(detail -> new Infos.EntityIdentifier(detail.getClassName(),
                                    ObjectIdentifier.buildWithValue(detail.getObjectId()),
                                    Optional.ofNullable(detail.getAttrib()).orElse(BizConstant.EMPTY)))
                            .collect(Collectors.toList())).orElse(Collections.emptyList()));
            CimCode codeDO = baseCoreFactory.getBO(CimCode.class, aRecord.getReasonCode().getReferenceKey());
            if (codeDO != null) {
                entityInhibitAttributes.setReasonDesc(codeDO.getDescription());
            } else {
                entityInhibitAttributes.setReasonDesc(BizConstant.EMPTY);
            }

            //get reason detail information :
            List<Constrain.EntityInhibitReasonDetailInfo> reasonDetailInfoList = anEntityInhibit.getReasonDetailInfos();

            List<Infos.EntityInhibitReasonDetailInfo> outReasonDetailInfoList = new ArrayList<>();
            int numOfReasonDetailInfos = CimArrayUtils.getSize(reasonDetailInfoList);
            for (int j = 0; j < numOfReasonDetailInfos; j++) {
                Constrain.EntityInhibitReasonDetailInfo reasonDetailInfo = reasonDetailInfoList.get(j);
                if (reasonDetailInfo == null) {
                    continue;
                }
                Infos.EntityInhibitReasonDetailInfo outReasonDetailInfo = new Infos.EntityInhibitReasonDetailInfo();
                outReasonDetailInfo.setRelatedLotID(reasonDetailInfo.getRelatedLotID());
                outReasonDetailInfo.setRelatedControlJobID(reasonDetailInfo.getRelatedControlJobID());
                outReasonDetailInfo.setRelatedFabID(reasonDetailInfo.getRelatedFabID());
                outReasonDetailInfo.setRelatedRouteID(reasonDetailInfo.getRelatedRouteID());
                outReasonDetailInfo.setRelatedProcessDefinitionID(reasonDetailInfo.getRelatedProcessDefinitionID());
                outReasonDetailInfo.setRelatedOperationNumber(reasonDetailInfo.getRelatedOperationNumber());
                outReasonDetailInfo.setRelatedOperationPassCount(reasonDetailInfo.getRelatedOperationPassCount());

                int numOfSpcChartInfos = CimArrayUtils.getSize(reasonDetailInfo.getStrEntityInhibitSpcChartInfos());
                List<Infos.EntityInhibitSpcChartInfo> spcChartInfoList = new ArrayList<>();
                for (int k = 0; k < numOfSpcChartInfos; k++) {
                    Constrain.EntityInhibitSpcChartInfo spcChartInfo = reasonDetailInfo.getStrEntityInhibitSpcChartInfos().get(k);
                    if (spcChartInfo == null) {
                        continue;
                    }
                    Infos.EntityInhibitSpcChartInfo outSpcChartInfo = new Infos.EntityInhibitSpcChartInfo();
                    outSpcChartInfo.setRelatedSpcDcType(spcChartInfo.getRelatedSpcDcType());
                    outSpcChartInfo.setRelatedSpcChartGroupID(spcChartInfo.getRelatedSpcChartGroupID());
                    outSpcChartInfo.setRelatedSpcChartID(spcChartInfo.getRelatedSpcChartID());
                    outSpcChartInfo.setRelatedSpcChartType(spcChartInfo.getRelatedSpcChartType());
                    spcChartInfoList.add(outSpcChartInfo);
                }
                outReasonDetailInfo.setStrEntityInhibitSpcChartInfos(spcChartInfoList);
                outReasonDetailInfoList.add(outReasonDetailInfo);
            }
            entityInhibitAttributes.setEntityInhibitReasonDetailInfos(outReasonDetailInfoList);

            //get reason detail information :
            List<Constrain.ExceptionLotRecord> exceptionLotRecordList = anEntityInhibit.getExceptionLotRecords();

            List<Infos.EntityInhibitExceptionLotInfo> outExceptionLotInfoList = new ArrayList<>();
            int numOfExpLots = CimArrayUtils.getSize(exceptionLotRecordList);
            for (int j = 0; j < numOfExpLots; j++) {
                Constrain.ExceptionLotRecord exceptionLotRecord = exceptionLotRecordList.get(j);
                if (exceptionLotRecord == null) {
                    continue;
                }
                Infos.EntityInhibitExceptionLotInfo outExceptionLotInfo = new Infos.EntityInhibitExceptionLotInfo();
                outExceptionLotInfo.setLotID(exceptionLotRecord.getLotID());
                outExceptionLotInfo.setSingleTriggerFlag(CimBooleanUtils.isTrue(exceptionLotRecord.getSingleTriggerFlag()));
                outExceptionLotInfo.setUsedFlag(CimBooleanUtils.isTrue(exceptionLotRecord.getUsedFlag()));
                outExceptionLotInfo.setClaimUserID(exceptionLotRecord.getClaimUserID());
                outExceptionLotInfo.setClaimMemo(exceptionLotRecord.getClaimMemo());
                String claimTime = (exceptionLotRecord.getClaimTimeStamp() != null) ? exceptionLotRecord.getClaimTimeStamp().toString() : BizConstant.EMPTY;
                outExceptionLotInfo.setClaimTime(claimTime);
                outExceptionLotInfoList.add(outExceptionLotInfo);
            }
            entityInhibitAttributes.setEntityInhibitExceptionLotInfos(outExceptionLotInfoList);

            // remove an entity inhibit:
            restrictionManager.removeEntityInhibit(anEntityInhibit);
        }
        return outEntityInhibitions;
    }

    @Override
    public Infos.EntityInhibitCheckForEntitiesOut constraintCheckForEntities(Infos.ObjCommon strObjCommonIn, Infos.EntityInhibitAttributes entityInhibitAttributes) {
        List<Constrain.EntityIdentifier> entities = new ArrayList<>();
        for (int i = 0; i < CimArrayUtils.getSize(entityInhibitAttributes.getEntities()); i++) {
            Constrain.EntityIdentifier entitie = new Constrain.EntityIdentifier();
            entities.add(entitie);
            if (!CimStringUtils.equals(entityInhibitAttributes.getEntities().get(i).getClassName(), "Operation")) {
                entitie.setClassName(entityInhibitAttributes.getEntities().get(i).getClassName());

                if (CimStringUtils.equals(entityInhibitAttributes.getEntities().get(i).getClassName(), BizConstant.SP_INHIBITCLASSID_ROUTE)) {
                    entitie.setAttrib("*");
                } else if (CimStringUtils.equals(entityInhibitAttributes.getEntities().get(i).getClassName(), BizConstant.SP_INHIBITCLASSID_CHAMBER)) {
                    entitie.setAttrib(entityInhibitAttributes.getEntities().get(i).getAttribution());
                }
            } else {
                entitie.setClassName(BizConstant.SP_INHIBITCLASSID_ROUTE);
                entitie.setAttrib(entityInhibitAttributes.getEntities().get(i).getAttribution());
            }
            entitie.setObjectId(ObjectIdentifier.fetchValue(entityInhibitAttributes.getEntities().get(i).getObjectID()));
        }

        List<String> sublottypes = new ArrayList<>();

        if (CimArrayUtils.getSize(entityInhibitAttributes.getSubLotTypes()) > 0) {
            sublottypes.add(entityInhibitAttributes.getSubLotTypes().get(0));
        }

        List<Constrain.EntityInhibitRecord> inhibitSeq = restrictionManager.allEntityInhibitRecordsForLotEntities(entities, sublottypes);
        int numOfInhibitSeq = CimArrayUtils.getSize(inhibitSeq);
        Infos.EntityInhibitCheckForEntitiesOut strEntityInhibit_CheckForEntities_out = new Infos.EntityInhibitCheckForEntitiesOut();
        List<Infos.EntityInhibitInfo> entityInhibitInfo = new ArrayList<>();
        strEntityInhibit_CheckForEntities_out.setEntityInhibitInfo(entityInhibitInfo);


        int count = 0;
        for (int n = 0; n < numOfInhibitSeq; n++) {
            Constrain.EntityInhibitRecord aRecord = inhibitSeq.get(n);
            Timestamp currentTimeStamp = CimDateUtils.getCurrentTimeStamp();
            if ((CimDateUtils.compare(aRecord.getEndTimeStamp(), new Timestamp(0)) == 0 ||
                    CimDateUtils.compare(currentTimeStamp, aRecord.getEndTimeStamp()) <= 0) &&
                    (CimDateUtils.compare(aRecord.getStartTimeStamp(), new Timestamp(0)) == 0 ||
                            CimDateUtils.compare(aRecord.getStartTimeStamp(), currentTimeStamp) <= 0)) {

                int numOfEntities = CimArrayUtils.getSize(aRecord.getEntities());
                Infos.EntityInhibitInfo entityInhibit = new Infos.EntityInhibitInfo();
                entityInhibitInfo.add(entityInhibit);
                Infos.EntityInhibitAttributes entityInhibitAttributesOut = new Infos.EntityInhibitAttributes();
                entityInhibit.setEntityInhibitAttributes(entityInhibitAttributesOut);
                List<Infos.EntityIdentifier> entitiesOut = new ArrayList<>();
                entityInhibitAttributesOut.setEntities(entitiesOut);
                for (int j = 0; j < numOfEntities; j++) {

                    Infos.EntityIdentifier entitie = new Infos.EntityIdentifier();
                    entitiesOut.add(entitie);
                    entitie.setClassName(aRecord.getEntities().get(j).getClassName());
                    entitie.setObjectID(new ObjectIdentifier(aRecord.getEntities().get(j).getObjectId()));
                    entitie.setAttribution(aRecord.getEntities().get(j).getAttrib());
                }

                entityInhibit.setEntityInhibitID(ObjectIdentifier.build(
                        aRecord.getId(), aRecord.getReferenceKey()
                ));

                entityInhibitAttributesOut.setSubLotTypes(aRecord.getSubLotTypes());
                entityInhibitAttributesOut.setStartTimeStamp(CimDateUtils.convert("yyyy-MM-dd HH:mm:ss", aRecord.getStartTimeStamp()));
                entityInhibitAttributesOut.setEndTimeStamp(CimDateUtils.convert("yyyy-MM-dd HH:mm:ss", aRecord.getEndTimeStamp()));
                entityInhibitAttributesOut.setClaimedTimeStamp(CimDateUtils.convert("yyyy-MM-dd HH:mm:ss", aRecord.getChangedTimeStamp()));
                entityInhibitAttributesOut.setReasonCode(aRecord.getReasonCode().getValue());
                entityInhibitAttributesOut.setOwnerID(aRecord.getOwner());
                entityInhibitAttributesOut.setMemo(aRecord.getClaimMemo());

                {

                    String hFRCODECATEGORY_ID;
                    String hFRCODECODE_ID;
                    String hFRCODEDESCRIPTION;

                    hFRCODECATEGORY_ID = BizConstant.SP_REASONCAT_ENTITYINHIBIT;
                    if (aRecord.getReasonCode() == null) {
                        hFRCODECODE_ID = null;
                    } else {
                        hFRCODECODE_ID = aRecord.getReasonCode().getValue();
                    }

                    Object[] one = cimJpaRepository.queryOne("SELECT DESCRIPTION,CODETYPE_ID FROM OMCODE\n" +
                            "                    WHERE CODETYPE_ID=? AND CODE_ID=?", hFRCODECATEGORY_ID, hFRCODECODE_ID);

                    if (!CimObjectUtils.isEmpty(one)) {
                        hFRCODEDESCRIPTION = CimObjectUtils.toString(one[0]);
                        entityInhibitAttributesOut.setReasonDesc(hFRCODEDESCRIPTION);
                    } else {
                        entityInhibitAttributesOut.setReasonDesc("");
                    }
                }

                count++;
            }
        }
        return strEntityInhibit_CheckForEntities_out;
    }

    @Override
    public List<Infos.EntityInhibitDetailInfo> constraintExceptionLotAttributesGetDR(Infos.ObjCommon objCommon, List<Infos.EntityIdentifier> entityIdentifierList, Infos.EntityInhibitExceptionLotInfo exceptionLotInfo) {
        //【Step0】:Method Entry Log and Initialize Parameters;
        log.info("Step-0:【Method Entry】entityInhibitExceptionLotAttributesGetDR()");
        List<Infos.EntityInhibitDetailInfo> entityInhibitDetailInfoList = new ArrayList<>();

        List<String> parameters = new ArrayList<>();
        Boolean entityExistFlag = false;
        Boolean expLotExistFlag = false;
        String entitySearchCondition = BizConstant.EMPTY;
        String expLotSearchCondition = BizConstant.EMPTY;

        //【Step1】Check a use of wildCard;
        Long useWildCardFlag = 0L;
        String wildCardChar = StandardProperties.OM_CONSTRAINT_USE_WILDCARD.getValue();
        if (!CimObjectUtils.isEmpty(wildCardChar)) {
            useWildCardFlag = Long.parseLong(wildCardChar);
        }

        if (useWildCardFlag != 0) {
            String tmpWildCardChar = StandardProperties.OM_CONSTRAINT_WILDCARD_VALUE.getValue();
            wildCardChar = tmpWildCardChar.substring(0, 1);
        } else {
            wildCardChar = BizConstant.SP_ADCSETTING_ASTERISK;
        }

        //【Step2】Check input parameter and create SQL sentence for OMRESTRICT_ENTITY;
        Integer entitiesLen = CimArrayUtils.getSize(entityIdentifierList);
        Boolean classIDFlag = false;
        Boolean checkWildCardAfterSelectFlag = false;
        StringBuilder querySql = new StringBuilder(BizConstant.EMPTY);
        if (0 < entitiesLen) {
            entityExistFlag = true;
            querySql = new StringBuilder("( select A.REFKEY from OMRESTRICT_ENTITY A where ");
            for (int i = 0; i < entitiesLen; i++) {
                if (i > 0) {
                    querySql.append(" intersect select A.REFKEY from OMRESTRICT_ENTITY A where ");
                }
                Infos.EntityIdentifier entityIdentifier = entityIdentifierList.get(i);
                //  Check condition and create SQL sentence
                if (!CimObjectUtils.isEmpty(entityIdentifier.getClassName())) {
                    classIDFlag = true;

                    if (CimStringUtils.equals(BizConstant.SP_INHIBITCLASSID_ROUTE, entityIdentifier.getClassName())) {
                        querySql.append(String.format("A.ENTITY_TYPE = '%s' AND A.ENTITY_ATTRIB = '%s' ",
                                BizConstant.SP_INHIBITCLASSID_ROUTE,
                                BizConstant.SP_INHIBITALLOPERATIONS));
                        parameters.add(BizConstant.SP_INHIBITCLASSID_ROUTE);
                        parameters.add(BizConstant.SP_INHIBITALLOPERATIONS);
                    } else if (CimStringUtils.equals(BizConstant.SP_INHIBITCLASSID_OPERATION, entityIdentifier.getClassName())) {
                        querySql.append(String.format("A.ENTITY_TYPE = '%s' AND A.ENTITY_ATTRIB <> '%s' ",
                                BizConstant.SP_INHIBITCLASSID_ROUTE,
                                BizConstant.SP_INHIBITALLOPERATIONS));
                        parameters.add(BizConstant.SP_INHIBITCLASSID_ROUTE);
                        parameters.add(BizConstant.SP_INHIBITALLOPERATIONS);
                    } else {
                        querySql.append(String.format("A.ENTITY_TYPE = '%s' ",
                                entityIdentifier.getClassName()));
                        parameters.add(entityIdentifier.getClassName());
                    }
                }

                if (!ObjectIdentifier.isEmpty(entityIdentifier.getObjectID())) {
                    if (CimBooleanUtils.isTrue(classIDFlag)) {
                        querySql.append(" AND ");
                    }

                    String anObjectID = entityIdentifier.getObjectID().getValue();
                    String wildCardPos = null;
                    if (!CimObjectUtils.isEmpty(wildCardChar)) {
                        int lastIndex = anObjectID.lastIndexOf(wildCardChar);
                        if (lastIndex > -1) {
                            wildCardPos = anObjectID.substring(lastIndex);
                        }
                    }

                    // Wild card is not used in input identifier.
                    if (CimObjectUtils.isEmpty(wildCardPos)) {
                        log.info("WildCard is NOT used : {}", anObjectID);
                        querySql.append(String.format(" ( A.ENTITY_ID = '%s' ", anObjectID));
                        parameters.add(anObjectID);
                        if (!CimObjectUtils.isEmpty(anObjectID) && useWildCardFlag > 0L) {
                            checkWildCardAfterSelectFlag = true;
                            String searchWildCard = String.format("%c%%%s%%", anObjectID.substring(0, 1), wildCardChar);
                            querySql.append(String.format(" OR  A.ENTITY_ID like '%s' ", searchWildCard));
                            parameters.add(searchWildCard);
                        }
                        querySql.append(")");
                    } else {
                        // Wild card is used.
                        log.info("WildCard is used : {}", anObjectID);
                        //Check WildCard position.
                        //1. Whether the heading character is '*' or not.  Compare the address.
                        //2. Whether WildCard is possessed at the end of the objectID or not.
                        //    If objectID == xxx* , wildCardPos - anObjectID.identifier == 3 (xxx), so + 1 == 4 (xxx*)
                        Validations.check(CimStringUtils.equals(wildCardPos, anObjectID), retCodeConfig.getInvalidWildcardPostion());

                        int firstIndex = anObjectID.indexOf(wildCardChar);
                        wildCardPos = anObjectID.substring(firstIndex);

                        int objIDLen = anObjectID.length();
                        int tillWildCardLen = wildCardPos.replaceAll(anObjectID, "").length();

                        Validations.check(objIDLen != tillWildCardLen, retCodeConfig.getInvalidWildcardPostion());

                        String searchKey = anObjectID + "%";
                        querySql.append(String.format("A.ENTITY_ID like '%s' ", searchKey));
                        parameters.add(searchKey);
                    }
                }

                if (CimArrayUtils.getSize(parameters) == 0) {
                    entityExistFlag = false;
                }
            }

            if (CimBooleanUtils.isTrue(entityExistFlag)) {
                querySql.append(") B ");
                entitySearchCondition = querySql.toString();
                if (CimBooleanUtils.isTrue(classIDFlag)) {
                    parameters.add(String.format("%d", entitiesLen));
                }
            } else {
                log.info("input entities is invalid ");
            }
        }

        //【Step3】 Check input parameter and create SQL sentence for OMRESTRICT_EXPLOT;
        if (!CimObjectUtils.isEmpty(exceptionLotInfo.getLotID().getValue())
                || !CimObjectUtils.isEmpty(exceptionLotInfo.getClaimUserID().getValue())
                || CimBooleanUtils.isTrue(exceptionLotInfo.getUsedFlag())
                || CimBooleanUtils.isTrue(exceptionLotInfo.getSingleTriggerFlag())) {
            expLotExistFlag = true;
            querySql = new StringBuilder(BizConstant.EMPTY);
            if (!CimObjectUtils.isEmpty(exceptionLotInfo.getLotID().getValue())) {
                querySql.append(String.format(" E.EXCEPT_LOT_ID like '%s' ", exceptionLotInfo.getLotID().getValue()));
                parameters.add(exceptionLotInfo.getLotID().getValue());
            }
            if (!CimObjectUtils.isEmpty(exceptionLotInfo.getClaimUserID().getValue())) {
                if (!CimObjectUtils.isEmpty(querySql.toString())) {
                    querySql.append(" AND ");
                }
                querySql.append(String.format(" E.LAST_TRX_USER_ID = '%s' ", exceptionLotInfo.getClaimUserID().getValue()));
                parameters.add(exceptionLotInfo.getClaimUserID().getValue());
            }
            if (CimBooleanUtils.isTrue(exceptionLotInfo.getUsedFlag())) {
                if (!CimObjectUtils.isEmpty(querySql.toString())) {
                    querySql.append(" AND ");
                }
                querySql.append(" E.USED_FLAG = 1 ");
            }
            if (CimBooleanUtils.isTrue(exceptionLotInfo.getSingleTriggerFlag())) {
                if (!CimObjectUtils.isEmpty(querySql.toString())) {
                    querySql.append(" AND ");
                }
                querySql.append(" E.SINGLE_TRIG_FLAG = 1 ");
            }
            expLotSearchCondition = querySql.toString();
        }

        //【Step4】Construct SQL sentence ;
        Validations.check(CimBooleanUtils.isFalse(entityExistFlag) && CimBooleanUtils.isFalse(expLotExistFlag), retCodeConfig.getInvalidSearchCondition());

        String tempSqlBuf = "select * from ";
        Boolean bSetFlag = false;
        if (CimBooleanUtils.isTrue(entityExistFlag)) {
            String tmpSelectBuf = String.format("select C.REFKEY from %s,OMRESTRICT_ENTITY C where B.REFKEY = C.REFKEY group by C.REFKEY ", entitySearchCondition);
            tempSqlBuf += "  OMRESTRICT_EXPLOT E, (" + tmpSelectBuf;

            if (CimBooleanUtils.isTrue(classIDFlag)) {
                tempSqlBuf += String.format(" having count(C.ID) = '%s' ", entitiesLen);
            }
            tempSqlBuf += ") D WHERE E.REFKEY = D.REFKEY ";
            bSetFlag = true;
        } else {
            tempSqlBuf += " OMRESTRICT_EXPLOT E where ";
        }

        if (CimBooleanUtils.isTrue(expLotExistFlag)) {
            if (CimBooleanUtils.isTrue(bSetFlag)) {
                tempSqlBuf += " AND ";
            }
            tempSqlBuf += expLotSearchCondition;
        }
        tempSqlBuf += " order by E.ID,EXCEPT_LOT_ID";

        //【Step5】get records from db;
        Long fetchLimitCount = 0L;
        String maxInhibitSeqSize = StandardProperties.OM_CONSTRAINT_MAX_LIST_INQ.getValue();
        if (!CimObjectUtils.isEmpty(maxInhibitSeqSize)) {
            fetchLimitCount = Long.parseLong(maxInhibitSeqSize);
        }

        if (fetchLimitCount < 1) {
            fetchLimitCount = Long.parseLong(BizConstant.CONST_TWO_THOUSAND);
        }
        List<CimRestrictionExpLotDO> exceptionLots = cimJpaRepository.query(tempSqlBuf, CimRestrictionExpLotDO.class);
        int exceptionLotsSize = CimArrayUtils.getSize(exceptionLots);
        Boolean bInhibitFlag = true;
        if (exceptionLotsSize > 0) {
            for (CimRestrictionExpLotDO item : exceptionLots) {
                Constrain.EntityInhibitRecord entityInhibitRecord = restrictionManager.findEntityInhibitRecordByKey(item.getReferenceKey());
                if (checkWildCardAfterSelectFlag && useWildCardFlag > 0L && !CimStringUtils.isEmpty(wildCardChar) && classIDFlag) {
                    int recordEntitiesLen = CimArrayUtils.getSize(entityInhibitRecord.getEntities());
                    for (int k = 0; k < recordEntitiesLen; k++) {
                        for (int n = 0; n < entitiesLen; n++) {
                            String classNameStr;
                            if (CimStringUtils.equals(BizConstant.SP_INHIBITCLASSID_OPERATION,
                                    entityIdentifierList.get(n).getClassName())) {
                                classNameStr = BizConstant.SP_INHIBITCLASSID_ROUTE;
                            } else {
                                classNameStr = entityIdentifierList.get(n).getClassName();
                            }

                            if (CimStringUtils.equals(classNameStr,
                                    entityInhibitRecord.getEntities().get(k).getClassName())) {
                                String objectID = entityIdentifierList.get(n).getObjectID().getValue();
                                int lastIndex = objectID.lastIndexOf(wildCardChar);
                                String wildCardExist = objectID.substring(lastIndex);
                                if (!CimObjectUtils.isEmpty(wildCardExist)) {
                                    break;
                                }

                                objectID = entityInhibitRecord.getEntities().get(k).getObjectId();
                                lastIndex = objectID.lastIndexOf(wildCardChar);
                                String wildCardPos = objectID.substring(lastIndex);

                                Integer compareLength = 0;
                                // WildCard is not used.
                                if (CimObjectUtils.isEmpty(wildCardPos)) {
                                    break;
                                } else {
                                    // WildCard is used.
                                    String beReplaced = entityInhibitRecord.getEntities().get(k).getObjectId();
                                    compareLength = wildCardPos.replaceAll(beReplaced, BizConstant.EMPTY).length();
                                }

                                String inObjectID = entityIdentifierList.get(n).getObjectID().getValue().substring(0, compareLength + 1);
                                String recordObjectID = entityInhibitRecord.getEntities().get(k).getObjectId().substring(0, compareLength + 1);
                                if (!CimStringUtils.equals(inObjectID, recordObjectID)) {
                                    bInhibitFlag = false;
                                    break;
                                }
                            }
                        }

                        if (CimBooleanUtils.isFalse(bInhibitFlag)) {
                            break;
                        }
                    }
                }
                if (CimBooleanUtils.isTrue(bInhibitFlag)) {
                    int numOfEntities = CimArrayUtils.getSize(entityInhibitRecord.getEntities());

                    Infos.EntityInhibitDetailInfo entityInhibitDetailInfo = new Infos.EntityInhibitDetailInfo();
                    Infos.EntityInhibitDetailAttributes entityInhibitDetailAttributes = new Infos.EntityInhibitDetailAttributes();
                    entityInhibitDetailInfo.setEntityInhibitDetailAttributes(entityInhibitDetailAttributes);

                    List<Infos.EntityIdentifier> entityIdentifiers = new ArrayList<>();
                    for (int m = 0; m < numOfEntities; m++) {
                        Constrain.EntityIdentifier recordEntity = entityInhibitRecord.getEntities().get(m);
                        Infos.EntityIdentifier entityIdentifier = new Infos.EntityIdentifier();
                        entityIdentifier.setClassName(recordEntity.getClassName());
                        entityIdentifier.setObjectID(new ObjectIdentifier(recordEntity.getObjectId()));
                        entityIdentifier.setAttribution(recordEntity.getAttrib());
                        entityIdentifiers.add(entityIdentifier);
                    }
                    entityInhibitDetailAttributes.setEntities(entityIdentifiers);
                    entityInhibitDetailInfo.setEntityInhibitID(ObjectIdentifier.build(entityInhibitRecord.getId(), entityInhibitRecord.getReferenceKey()));
                    entityInhibitDetailAttributes.setSubLotTypes(entityInhibitRecord.getSubLotTypes());

                    String startTime = (entityInhibitRecord.getStartTimeStamp() == null) ?
                            BizConstant.EMPTY :
                            entityInhibitRecord.getStartTimeStamp().toString();
                    entityInhibitDetailAttributes.setStartTimeStamp(startTime);

                    String endTime = (entityInhibitRecord.getEndTimeStamp() == null) ?
                            BizConstant.EMPTY :
                            entityInhibitRecord.getEndTimeStamp().toString();
                    entityInhibitDetailAttributes.setEndTimeStamp(endTime);

                    String changedTime = (entityInhibitRecord.getChangedTimeStamp() == null) ?
                            BizConstant.EMPTY :
                            entityInhibitRecord.getChangedTimeStamp().toString();
                    entityInhibitDetailAttributes.setClaimedTimeStamp(changedTime);

                    entityInhibitDetailAttributes.setReasonCode(entityInhibitRecord.getReasonCode().getValue());
                    entityInhibitDetailAttributes.setOwnerID(entityInhibitRecord.getOwner());
                    entityInhibitDetailAttributes.setMemo(entityInhibitRecord.getClaimMemo());
                    entityInhibitDetailAttributes.setMemo(entityInhibitRecord.getClaimMemo());

                    List<Infos.EntityInhibitExceptionLotInfo> exceptionLotInfoList = new ArrayList<>();
                    entityInhibitDetailAttributes.setEntityInhibitExceptionLotInfos(exceptionLotInfoList);

                    Infos.EntityInhibitExceptionLotInfo entityInhibitExceptionLotInfo = new Infos.EntityInhibitExceptionLotInfo();
                    entityInhibitExceptionLotInfo.setLotID(ObjectIdentifier.build(item.getExceptLotID(), item.getExceptLotObj()));
                    entityInhibitExceptionLotInfo.setSingleTriggerFlag(item.getSingleTrigFlag());
                    entityInhibitExceptionLotInfo.setUsedFlag(item.getUsedFlag());
                    entityInhibitExceptionLotInfo.setClaimUserID(ObjectIdentifier.build(item.getClaimUserID(), item.getClaimUserObj()));
                    entityInhibitExceptionLotInfo.setClaimMemo(item.getClaimMemo());

                    String claimTime = (item.getClaimTime() == null) ?
                            BizConstant.EMPTY :
                            item.getClaimTime().toString();
                    entityInhibitExceptionLotInfo.setClaimTime(claimTime);
                    exceptionLotInfoList.add(entityInhibitExceptionLotInfo);

                    entityInhibitDetailInfoList.add(entityInhibitDetailInfo);
                    int nCount = CimArrayUtils.getSize(entityInhibitDetailInfoList);
                    if (nCount >= fetchLimitCount) {
                        log.info("The length of EntityInhibitException ({}) reached the maximum ", nCount);
                        break;
                    }
                }
            }
        }

        //Step-6:Outputs;
        log.info("【Method Exit】entityInhibitExceptionLotAttributesGetDR()");

        return entityInhibitDetailInfoList;
    }

    @Override
    public List<Infos.ConstraintEqpDetailInfo> constraintAttributeListGetDR(Params.MfgRestrictListInqParams mfgRestrictListInqParams, Infos.ObjCommon objCommon) {
        //Step-0:Method Entry Log and Initialize Parameters;
        log.info("Step-0:【Method Entry】entityInhibitAttributesGetDR()");

        List<Infos.ConstraintEqpDetailInfo> tmpEntityInhibitDetailInfos = new ArrayList<>();
        String constraintID = mfgRestrictListInqParams.getConstraintID();
        if (CimStringUtils.isNotEmpty(constraintID)){
            CimRestrictionDO example = new CimRestrictionDO();
            example.setRestrictID(constraintID);
            CimRestrictionDO cimRestrictionDO = cimJpaRepository.findOne(Example.of(example)).orElse(null);
            if (CimObjectUtils.isEmpty(cimRestrictionDO)){
                return new ArrayList<>();
            }
            CimRestriction aRestric = baseCoreFactory.getBO(CimRestriction.class,cimRestrictionDO.getId());
            Constrain.EntityInhibitRecord inhibitRecord = aRestric.getInhibitRecord();
            Infos.ConstraintEqpDetailInfo tempInfo = new Infos.ConstraintEqpDetailInfo();
            tmpEntityInhibitDetailInfos.add(tempInfo);
            tempInfo.setEntityInhibitID(ObjectIdentifier.build(cimRestrictionDO.getRestrictID(),cimRestrictionDO.getId()));
            Infos.ConstraintDetailAttributes constraintDetailAttributes = new Infos.ConstraintDetailAttributes();
            tempInfo.setEntityInhibitDetailAttributes(constraintDetailAttributes);
            constraintDetailAttributes.setReasonCode(inhibitRecord.getReasonCode().getValue());
            constraintDetailAttributes.setStatus(inhibitRecord.getStatus());
            constraintDetailAttributes.setClaimedTimeStamp(aRestric.getChangedTimeStamp().toString());
            constraintDetailAttributes.setEndTimeStamp(aRestric.getEndTimeStamp().toString());
            constraintDetailAttributes.setEntities(combineValueWithCommas(aRestric.getEntities()));
            constraintDetailAttributes.setExceptionEntities(combineValueWithCommas(aRestric.getExceptionEntities()));
            constraintDetailAttributes.setExceptionLotList(exceptionLotInfoExchange(aRestric.getExceptionLotRecords()));
            constraintDetailAttributes.setFunctionRule(aRestric.getFunctionRule());
            constraintDetailAttributes.setMemo(aRestric.getClaimMemo());
            constraintDetailAttributes.setOwnerID(inhibitRecord.getOwner());
            constraintDetailAttributes.setSpecTool(inhibitRecord.getSpecificTool());
            constraintDetailAttributes.setStartTimeStamp(inhibitRecord.getStartTimeStamp().toString());
            constraintDetailAttributes.setSubLotTypes(inhibitRecord.getSubLotTypes());
        }else {
            mfgListGetByEntities(mfgRestrictListInqParams, objCommon, tmpEntityInhibitDetailInfos);
        }
        return tmpEntityInhibitDetailInfos;
    }

    private void mfgListGetByEntities(Params.MfgRestrictListInqParams mfgRestrictListInqParams, Infos.ObjCommon objCommon, List<Infos.ConstraintEqpDetailInfo> tmpEntityInhibitDetailInfos) {
        Infos.EntityInhibitDetailAttributes entityInhibitAttributes = mfgRestrictListInqParams.getEntityInhibitDetailAttributes();
        Boolean entityInhibitReasonDetailInfoFlag = mfgRestrictListInqParams.getEntityInhibitReasonDetailInfoFlag();

        Boolean hInhibitSearchConditionExistFlag = false;
        Boolean hInhibitEntitySearchConditionExistFlag = false;
        Boolean hInhibitRsnInfoSearchConditionExistFlag = false;
        Boolean hInhibitSPCInfoSearchConditionExistFlag = false;
        String searchConditionForInhibit = BizConstant.EMPTY;
        //Step-1:Check input parameter and create SQL sentence for OMRESTRICT;
        log.info("Step-1:Check input parameter and create SQL sentence for OMRESTRICT");
        if (!ObjectIdentifier.isEmpty(entityInhibitAttributes.getOwnerID()) ||
                !CimStringUtils.isEmpty(entityInhibitAttributes.getReasonCode()) ||
                !CimStringUtils.isEmpty(entityInhibitAttributes.getEndTimeStamp())) {
            hInhibitSearchConditionExistFlag = true;
            if (!ObjectIdentifier.isEmpty(entityInhibitAttributes.getOwnerID())) {
                String tempSql = String.format(" C.OWNER_ID='%s' ", entityInhibitAttributes.getOwnerID().getValue());
                searchConditionForInhibit += tempSql;
            }

            if (!CimStringUtils.isEmpty(entityInhibitAttributes.getReasonCode())) {
                if (!CimStringUtils.isEmpty(searchConditionForInhibit)) {
                    searchConditionForInhibit += " AND ";
                }
                String tempSql = String.format(" C.REASON_CODE='%s' ", entityInhibitAttributes.getReasonCode());
                searchConditionForInhibit += tempSql;
            }

            if (!CimStringUtils.isEmpty(entityInhibitAttributes.getEndTimeStamp())) {
                log.info("entityInhibitDetailAttributes.endTimeStamp = {}", entityInhibitAttributes.getEndTimeStamp());
                log.info(" strObjCommonIn.strTimeStamp.reportTimeStamp = {}", objCommon.getTimeStamp().getReportTimeStamp());
                if (!CimStringUtils.isEmpty(searchConditionForInhibit)) {
                    searchConditionForInhibit += " AND ";
                }

                String searchEndTime = BizConstant.EMPTY;
                Boolean pastFlag = false;

                String endTimeStr = entityInhibitAttributes.getEndTimeStamp();
                if (CimStringUtils.equals(endTimeStr, entityInhibitAttributes.getEndTimeStamp())) {
                    if (CimStringUtils.equals(BizConstant.SP_ENDTIME_TODAY, entityInhibitAttributes.getEndTimeStamp())) {
                        Timestamp tmpTimeStamp = objCommon.getTimeStamp().getReportTimeStamp();
                        long year = tmpTimeStamp.toLocalDateTime().getYear();
                        long month = tmpTimeStamp.toLocalDateTime().getMonthValue();
                        long day = tmpTimeStamp.toLocalDateTime().getDayOfMonth();

                        searchEndTime = String.format("%04d%s%02d%s%02d%s%02d%s%02d%s%02d%s%06d",
                                year, BizConstant.HYPHEN, month, BizConstant.HYPHEN, day, BizConstant.BLANK,
                                BizConstant.SP_TIMESTAMP_MAX_HOUR, BizConstant.COLON,
                                BizConstant.SP_TIMESTAMP_MAX_MINUTE, BizConstant.COLON,
                                BizConstant.SP_TIMESTAMP_MAX_SEC, BizConstant.DOT,
                                BizConstant.SP_TIMESTAMP_MAX_MILLISEC);
                    } else if (CimStringUtils.equals(BizConstant.SP_ENDTIME_PAST, entityInhibitAttributes.getEndTimeStamp())) {
                        searchEndTime = objCommon.getTimeStamp().getReportTimeStamp().toString();
                        pastFlag = true;
                    } else {
                        int durationDays = Integer.parseInt(entityInhibitAttributes.getEndTimeStamp());
                        if (0 < durationDays) {
                            try {
                                String targetTimeStamp = timeStampMethod.timeStampDoCalculation(objCommon, objCommon.getTimeStamp().getReportTimeStamp().toString(),
                                        durationDays, 0, 0, 0, 0);
                                searchEndTime = targetTimeStamp;
                            } catch (ServiceException e) {
                                searchEndTime = BizConstant.SP_TIMESTAMP_NIL_OBJECT_STRING;
                            }
                        }
                    }
                    Timestamp endTime = Timestamp.valueOf(searchEndTime);
                    Validations.check(/*!StringUtils.equals(endTime.toString(),searchEndTime) ||*/
                            CimStringUtils.equals(BizConstant.SP_TIMESTAMP_NIL_OBJECT_STRING, endTime.toString()), retCodeConfig.getInvalidSearchCondition());
                } else {
                    searchEndTime = entityInhibitAttributes.getEndTimeStamp();
                    String reportTime = objCommon.getTimeStamp().getReportTimeStamp().toString();
                    if (CimDateUtils.compare(searchEndTime, reportTime) <= 0) {
                        pastFlag = true;
                    }
                }
                log.info("searchEndTime = {}", searchEndTime);
                String tempSql = String.format(" C.END_TIME<=TO_TIMESTAMP('%s','yyyy-MM-dd HH24:mi:SSxFF') ", searchEndTime);
                searchConditionForInhibit += tempSql;
                searchConditionForInhibit += " AND ";

                if (CimBooleanUtils.isTrue(pastFlag)) {
                    searchConditionForInhibit += String.format(" C.END_TIME<>TO_TIMESTAMP('%s','yyyy-MM-dd HH24:mi:SSxFF') ", BizConstant.SP_TIMESTAMP_NIL_OBJECT_STRING);
                } else {
                    searchConditionForInhibit += String.format(" C.END_TIME>TO_TIMESTAMP('%s','yyyy-MM-dd HH24:mi:SSxFF') ", CimDateUtils.getTimestampAsString(objCommon.getTimeStamp().getReportTimeStamp()));
                }
            }

            log.info("convertedSQL for OMRESTRICT table : {}", searchConditionForInhibit);
        }

        //Step-2:Check input parameter and create SQL sentence for OMRESTRICT_ENTITY;
        String searchConditionForInhibitEntity = BizConstant.EMPTY;
        log.info("Step-2:Check input parameter and create SQL sentence for OMRESTRICT_ENTITY");
        Integer entitiesLen = CimArrayUtils.getSize(entityInhibitAttributes.getEntities());
        Boolean wildCardFlag = false;
        Boolean checkWildCardAfterSelectFlag = false;
        Integer useWildCardFlg = 0;
        String wildCardStr = BizConstant.EMPTY;

        if (entitiesLen > 0) {
            hInhibitEntitySearchConditionExistFlag = true;
            searchConditionForInhibitEntity = " (SELECT B.REFKEY FROM OMRESTRICT_ENTITY B WHERE ";
            //check a use of wiidcard;
            String useWildCardStr = StandardProperties.OM_CONSTRAINT_USE_WILDCARD.getValue();
            if (CimObjectUtils.isEmpty(useWildCardStr)) {
                useWildCardFlg = 0;
            } else {
                useWildCardFlg = Integer.parseInt(useWildCardStr);
            }

            if (0 != useWildCardFlg) {
                String tempWildChar = StandardProperties.OM_CONSTRAINT_WILDCARD_VALUE.getValue();
                if (!CimObjectUtils.isEmpty(tempWildChar)) {
                    wildCardStr = tempWildChar.substring(0, 1);
                }
            } else {
                wildCardStr = BizConstant.SP_ADCSETTING_ASTERISK;
            }

            for (int i = 0; i < entitiesLen; i++) {
                Infos.EntityIdentifier entityIdentifier = entityInhibitAttributes.getEntities().get(i);
                if (entityIdentifier == null) {
                    continue;
                }

                if (i != 0) {
                    searchConditionForInhibitEntity += " INTERSECT SELECT B.REFKEY FROM OMRESTRICT_ENTITY B WHERE ";
                }

                //check condition and create SQL sentence;
                Boolean entityIDFlag = false;
                Boolean setFlag = false;

                String objectId = entityIdentifier.getObjectID() == null ?
                        BizConstant.EMPTY :
                        entityIdentifier.getObjectID().getValue();
                if (CimStringUtils.equals(BizConstant.SP_ALL_WILD_CARD, objectId) && useWildCardFlg > 0) {
                    String tempSql = String.format("  B.ENTITY_ID LIKE '%%%s%%' ", wildCardStr);
                    searchConditionForInhibitEntity += tempSql;
                    wildCardFlag = true;
                    setFlag = true;
                }

                if (!CimStringUtils.isEmpty(entityIdentifier.getClassName())) {
                    if (setFlag) {
                        searchConditionForInhibitEntity += " AND ";
                    }

                    if (CimStringUtils.equals(BizConstant.SP_INHIBITCLASSID_ROUTE, entityIdentifier.getClassName())) {
                        searchConditionForInhibitEntity += String.format("(B.ENTITY_TYPE='%s' AND B.ENTITY_ATTRIB='%s')", BizConstant.SP_INHIBITCLASSID_ROUTE, BizConstant.SP_INHIBITALLOPERATIONS);
                    } else if (BizConstant.equalsIgnoreCase(BizConstant.SP_INHIBITCLASSID_OPERATION, entityIdentifier.getClassName())) {
                        // bug-1415 Constraint List
                        // bug-1457 Constraint List
                        if (CimStringUtils.isNotEmpty(entityIdentifier.getAttribution())) {

                            searchConditionForInhibitEntity += String.format("(B.ENTITY_TYPE='%s' AND B.ENTITY_ATTRIB='%s')", BizConstant.SP_INHIBITCLASSID_ROUTE, entityIdentifier.getAttribution());
                        } else {
                            searchConditionForInhibitEntity += String.format("(B.ENTITY_TYPE='%s' AND B.ENTITY_ATTRIB<>'%s')", BizConstant.SP_INHIBITCLASSID_ROUTE, BizConstant.SP_INHIBITALLOPERATIONS);
                        }
                    } else {
                        if (CimStringUtils.isNotEmpty(entityIdentifier.getAttribution())){

                            searchConditionForInhibitEntity += String.format("(B.ENTITY_TYPE='%s' AND B.ENTITY_ATTRIB='%s')", entityIdentifier.getClassName(),entityIdentifier.getAttribution());
                        } else {

                            searchConditionForInhibitEntity += String.format(" B.ENTITY_TYPE='%s' ", entityIdentifier.getClassName());
                        }
                    }
                    entityIDFlag = true;
                }

                if (CimBooleanUtils.isTrue(entityIDFlag)
                        && !ObjectIdentifier.isEmpty(entityIdentifier.getObjectID())
                        && !CimStringUtils.equals(BizConstant.SP_ALL_WILD_CARD, entityIdentifier.getObjectID().getValue())) {
                    searchConditionForInhibitEntity += " AND ";

                    String anObjectID = entityIdentifier.getObjectID().getValue();
                    String wildCardPos = BizConstant.EMPTY;

                    if (!CimObjectUtils.isEmpty(wildCardStr)) {
                        int lastPosition = anObjectID.lastIndexOf(wildCardStr);
                        if (lastPosition != -1) {
                            wildCardPos = anObjectID.substring(lastPosition);
                        }
                    }

                    if (CimObjectUtils.isEmpty(wildCardPos)) {
                        log.info("wildCard is NOT used : {}", anObjectID);
                        searchConditionForInhibitEntity += String.format(" (B.ENTITY_ID='%s' ", anObjectID);
                        if (!CimObjectUtils.isEmpty(anObjectID) && useWildCardFlg > 0) {
                            searchConditionForInhibitEntity += " OR ";
                            searchConditionForInhibitEntity += String.format(" B.ENTITY_ID like '%c%%%s%%'", anObjectID.charAt(0), wildCardStr);
                            checkWildCardAfterSelectFlag = true;
                        }
                        searchConditionForInhibitEntity += ")";
                    } else {
                        log.info("wildCard is used : {}", anObjectID);
                        /* Check WildCard position.
                         * 1. Whether the heading character is '*' or not.  Compare the address.
                         * 2. Whether WildCard is possessed at the end of the objectID or not.
                         * If objectID == xxx* , wildCardPos - anObjectID.identifier == 3 (xxx), so + 1 == 4 (xxx*)
                         ***********************************************************************************************************/
                        Validations.check(CimStringUtils.equals(wildCardPos, anObjectID), retCodeConfig.getInvalidWildcardPostion());

                        Integer firstPosition = anObjectID.indexOf(wildCardStr);
                        wildCardPos = anObjectID.substring(firstPosition);

                        Integer objIdLen = anObjectID.length();
                        String[] split = anObjectID.split("\\*");
                        anObjectID = split[0];
                        Integer tillWildCardLen = anObjectID.length() + 1;
                        Validations.check(!objIdLen.equals(tillWildCardLen), retCodeConfig.getInvalidWildcardPostion());

                        String hSearchKey = anObjectID + "%";
                        searchConditionForInhibitEntity += String.format(" B.ENTITY_ID like '%s' ", hSearchKey);
                    }
                }

                if (CimObjectUtils.isEmpty(searchConditionForInhibitEntity)) {
                    hInhibitEntitySearchConditionExistFlag = false;
                }
            }
            if (CimBooleanUtils.isTrue(hInhibitEntitySearchConditionExistFlag)) {
                searchConditionForInhibitEntity += ") A ";
            } else {
                log.info("input entities is invalid ");
            }
        }

        //Step-3:Check input parameter and create SQL sentence for OMRESTRICT_RSNINFO;
        String searchConditionForInhibitRsnInfo = BizConstant.EMPTY;
        log.info("Step-3:Check input parameter and create SQL sentence for OMRESTRICT_RSNINFO");
        List<Infos.EntityInhibitReasonDetailInfo> entityInhibitReasonDetailInfos = entityInhibitAttributes.getEntityInhibitReasonDetailInfos();
        int rsnInfoLen = CimArrayUtils.getSize(entityInhibitReasonDetailInfos);
        if (rsnInfoLen > 0) {
            hInhibitRsnInfoSearchConditionExistFlag = true;
            searchConditionForInhibitRsnInfo = " (SELECT E.REFKEY FROM OMRESTRICT_RSNINFO E WHERE ";

            for (int nRsnCnt = 0; nRsnCnt < rsnInfoLen; nRsnCnt++) {
                Infos.EntityInhibitReasonDetailInfo rsnInfo = entityInhibitReasonDetailInfos.get(nRsnCnt);
                if (rsnInfo == null) {
                    continue;
                }
                if (nRsnCnt != 0) {
                    searchConditionForInhibitRsnInfo += " INTERSECT SELECT E.REFKEY FROM OMRESTRICT_RSNINFO E WHERE ";
                }

                //check condition and create SQL Sentence;
                Boolean bConditionSetFlag = false;
                if (!CimObjectUtils.isEmpty(rsnInfo.getRelatedLotID())) {
                    searchConditionForInhibitRsnInfo += String.format(" E.LOT_ID like '%s' ", rsnInfo.getRelatedLotID());
                    bConditionSetFlag = true;
                }

                if (!CimObjectUtils.isEmpty(rsnInfo.getRelatedControlJobID())) {
                    if (bConditionSetFlag) {
                        searchConditionForInhibitRsnInfo += " AND ";
                    }
                    searchConditionForInhibitRsnInfo += String.format(" E.CJ_ID like '%s' ", rsnInfo.getRelatedControlJobID());
                    bConditionSetFlag = true;
                }
                if (!CimObjectUtils.isEmpty(rsnInfo.getRelatedFabID())) {
                    if (bConditionSetFlag) {
                        searchConditionForInhibitRsnInfo += " AND ";
                    }
                    searchConditionForInhibitRsnInfo += String.format(" E.FAB_ID like '%s' ", rsnInfo.getRelatedFabID());
                    bConditionSetFlag = true;
                }
                if (!CimObjectUtils.isEmpty(rsnInfo.getRelatedRouteID())) {
                    if (bConditionSetFlag) {
                        searchConditionForInhibitRsnInfo += " AND ";
                    }
                    searchConditionForInhibitRsnInfo += String.format(" E.MAIN_PROCESS_ID like '%s' ", rsnInfo.getRelatedRouteID());
                    bConditionSetFlag = true;
                }
                if (!CimObjectUtils.isEmpty(rsnInfo.getRelatedProcessDefinitionID())) {
                    if (bConditionSetFlag) {
                        searchConditionForInhibitRsnInfo += " AND ";
                    }
                    searchConditionForInhibitRsnInfo += String.format(" E.STEP_ID like '%s' ", rsnInfo.getRelatedProcessDefinitionID());
                    bConditionSetFlag = true;
                }
                if (!CimObjectUtils.isEmpty(rsnInfo.getRelatedOperationNumber())) {
                    if (bConditionSetFlag) {
                        searchConditionForInhibitRsnInfo += " AND ";
                    }
                    searchConditionForInhibitRsnInfo += String.format(" E.OPE_NO like '%s' ", rsnInfo.getRelatedOperationNumber());
                    bConditionSetFlag = true;
                }
                if (!CimObjectUtils.isEmpty(rsnInfo.getRelatedOperationPassCount())) {
                    if (bConditionSetFlag) {
                        searchConditionForInhibitRsnInfo += " AND ";
                    }
                    searchConditionForInhibitRsnInfo += String.format(" E.PASS_COUNT like '%s' ", rsnInfo.getRelatedOperationPassCount());
                    bConditionSetFlag = true;
                }
                if (CimObjectUtils.isEmpty(searchConditionForInhibitRsnInfo)) {
                    hInhibitRsnInfoSearchConditionExistFlag = false;
                }
            }
            if (CimBooleanUtils.isTrue(hInhibitRsnInfoSearchConditionExistFlag)) {
                searchConditionForInhibitRsnInfo += ") F ";
            } else {
                log.info("input strEntityInhibitReasonDetailInfos is invalid ");
            }
        }

        //Step-4:Check input parameter and create SQL sentence for OMRESTRICT_RSNINFO_SPC;
        String searchConditionForInhibitSPCInfo = BizConstant.EMPTY;
        log.info("Step-4:Check input parameter and create SQL sentence for OMRESTRICT_RSNINFO_SPC");
        if (rsnInfoLen > 0) {
            for (int nRsnCnt = 0; nRsnCnt < rsnInfoLen; nRsnCnt++) {
                Infos.EntityInhibitReasonDetailInfo rsnInfo = entityInhibitReasonDetailInfos.get(nRsnCnt);
                if (rsnInfo == null) {
                    continue;
                }

                List<Infos.EntityInhibitSpcChartInfo> entityInhibitSpcChartInfos = rsnInfo.getStrEntityInhibitSpcChartInfos();
                int spcInfoLen = CimArrayUtils.getSize(entityInhibitSpcChartInfos);
                if (spcInfoLen > 0) {
                    hInhibitSPCInfoSearchConditionExistFlag = true;
                    break;
                }
            }
            if (CimBooleanUtils.isTrue(hInhibitSPCInfoSearchConditionExistFlag)) {
                searchConditionForInhibitSPCInfo = " (SELECT G.REFKEY FROM OMRESTRICT_RSNINFO_SPC G WHERE ";
                for (int nRsnCnt = 0; nRsnCnt < rsnInfoLen; nRsnCnt++) {
                    Infos.EntityInhibitReasonDetailInfo rsnInfo = entityInhibitReasonDetailInfos.get(nRsnCnt);
                    if (rsnInfo == null) {
                        continue;
                    }

                    if (nRsnCnt != 0) {
                        searchConditionForInhibitSPCInfo += " INTERSECT SELECT G.REFKEY FROM OMRESTRICT_RSNINFO_SPC G WHERE ";
                    }
                    List<Infos.EntityInhibitSpcChartInfo> entityInhibitSpcChartInfos = rsnInfo.getStrEntityInhibitSpcChartInfos();
                    int spcInfoLen = CimArrayUtils.getSize(entityInhibitSpcChartInfos);
                    for (int nSpcCnt = 0; nSpcCnt < spcInfoLen; nSpcCnt++) {
                        Infos.EntityInhibitSpcChartInfo spcChartInfo = entityInhibitSpcChartInfos.get(nSpcCnt);
                        if (spcChartInfo == null) {
                            continue;
                        }

                        if (nSpcCnt != 0) {
                            searchConditionForInhibitSPCInfo += " INTERSECT SELECT G.REFKEY FROM OMRESTRICT_RSNINFO_SPC G WHERE ";
                        }
                        //check condition and create SQL sentence;
                        Boolean bConditionSetFlag = false;
                        if (!CimObjectUtils.isEmpty(spcChartInfo.getRelatedSpcDcType())) {
                            if (CimBooleanUtils.isTrue(bConditionSetFlag)) {
                                searchConditionForInhibitSPCInfo += " AND ";
                            }
                            searchConditionForInhibitSPCInfo += String.format(" G.EDC_TYPE like '%s' ", spcChartInfo.getRelatedSpcDcType());
                            bConditionSetFlag = true;
                        }
                        if (!CimObjectUtils.isEmpty(spcChartInfo.getRelatedSpcChartGroupID())) {
                            if (CimBooleanUtils.isTrue(bConditionSetFlag)) {
                                searchConditionForInhibitSPCInfo += " AND ";
                            }
                            searchConditionForInhibitSPCInfo += String.format(" G.CHART_GRP_ID like '%s' ", spcChartInfo.getRelatedSpcChartGroupID());
                            bConditionSetFlag = true;
                        }
                        if (!CimObjectUtils.isEmpty(spcChartInfo.getRelatedSpcChartID())) {
                            if (CimBooleanUtils.isTrue(bConditionSetFlag)) {
                                searchConditionForInhibitSPCInfo += " AND ";
                            }
                            searchConditionForInhibitSPCInfo += String.format(" G.CHART_ID like '%s' ", spcChartInfo.getRelatedSpcChartID());
                            bConditionSetFlag = true;
                        }
                        if (!CimObjectUtils.isEmpty(spcChartInfo.getRelatedSpcChartType())) {
                            if (CimBooleanUtils.isTrue(bConditionSetFlag)) {
                                searchConditionForInhibitSPCInfo += " AND ";
                            }
                            String chartTypeInitial = ""; //todo:convert spc chart type text;

                            searchConditionForInhibitSPCInfo += String.format(" G.CHART_TYPE like '%s' ", spcChartInfo.getRelatedSpcChartType());
                            bConditionSetFlag = true;
                        }
                        if (CimObjectUtils.isEmpty(searchConditionForInhibitSPCInfo)) {
                            hInhibitSPCInfoSearchConditionExistFlag = false;
                        }
                    }
                }
            }
            if (CimBooleanUtils.isTrue(hInhibitSPCInfoSearchConditionExistFlag)) {
                searchConditionForInhibitSPCInfo += ") H ";
            } else {
                log.info("input strEntityInhibitSpcChartInfos is invalid ");
            }
        }

        //Step-5: Construct SQL sentence;
        log.info("Step-5: Construct SQL sentence");
        Validations.check(CimBooleanUtils.isFalse(hInhibitSearchConditionExistFlag)
                && CimBooleanUtils.isFalse(hInhibitEntitySearchConditionExistFlag)
                && CimBooleanUtils.isFalse(hInhibitRsnInfoSearchConditionExistFlag)
                && CimBooleanUtils.isFalse(hInhibitSPCInfoSearchConditionExistFlag), retCodeConfig.getInvalidSearchCondition());

        Boolean bSetFlag = false;
    /*String tmpBuf = "SELECT P.ID, P.RESTRICT_ID, P.DESCRIPTION, " +
            "P.START_TIME, P.END_TIME, P.CHANGE_TIME, P.OWNER_ID, P.OWNER_OBJ, P.RSN_CODE, " +
            "P.RSN_OBJ, P.CLAIM_MEMO, S1.D_SEQNO, S1.ENTITY_TYPE, S1.ENTITY_ID, " +
            "S1.ENTITY_ATTRIB, COALESCE(S2.D_SEQNO,0), COALESCE(S2.SUB_LOT_TYPE,''), COALESCE(S3.DESCRIPTION,'') " +
            "FROM ( SELECT OMRESTRICT.ID, OMRESTRICT.RESTRICT_ID, OMRESTRICT.DESCRIPTION, " +
            "OMRESTRICT.START_TIME, OMRESTRICT.END_TIME, OMRESTRICT.CHANGE_TIME, OMRESTRICT.OWNER_ID, OMRESTRICT.OWNER_OBJ, " +
            "OMRESTRICT.RSN_CODE, OMRESTRICT.RSN_OBJ, OMRESTRICT.CLAIM_MEMO FROM OMRESTRICT WHERE OMRESTRICT.ID IN ( ";*/
        //String tmpPreBuf = "SELECT COUNT(P.ID) FROM ( ";
        String tmpBuf = "SELECT P.ID, P.RESTRICT_ID, P.DESCRIPTION, P.START_TIME, P.END_TIME, P.CHANGE_TIME, P.OWNER_ID, P.OWNER_RKEY, " +
                "P.REASON_CODE, P.REASON_RKEY, P.TRX_MEMO, S1.IDX_NO, S1.ENTITY_TYPE, S1.ENTITY_ID, S1.ENTITY_ATTRIB, " +
                "COALESCE(S2.IDX_NO, 0), COALESCE(S2.SUB_LOT_TYPE, ''), COALESCE(S3.DESCRIPTION, '') FROM ( SELECT B.ID, B.RESTRICT_ID, " +
                "B.DESCRIPTION, B.START_TIME, B.END_TIME, B.CHANGE_TIME, B.OWNER_ID, B.OWNER_RKEY, B.REASON_CODE, B.REASON_RKEY, B.TRX_MEMO " +
                "FROM ( SELECT A.ID, A.RESTRICT_ID, A.DESCRIPTION, A.START_TIME, A.END_TIME, A.CHANGE_TIME, A.OWNER_ID, A.OWNER_RKEY, A.REASON_CODE, " +
                "A.REASON_RKEY, A.TRX_MEMO, ROWNUM AS RN FROM (SELECT OMRESTRICT.ID, OMRESTRICT.RESTRICT_ID, OMRESTRICT.DESCRIPTION,OMRESTRICT.START_TIME," +
                "OMRESTRICT.END_TIME , OMRESTRICT.CHANGE_TIME,OMRESTRICT.OWNER_ID,OMRESTRICT.OWNER_RKEY,OMRESTRICT.REASON_CODE,OMRESTRICT.REASON_RKEY ," +
                "OMRESTRICT.TRX_MEMO FROM OMRESTRICT WHERE OMRESTRICT.FUNC_RULE='BLIST' AND SPECIFIC_TOOL=0 AND OMRESTRICT.ID IN (";

        String tmpPreBuf = "SELECT COUNT(*) FROM (SELECT * FROM OMRESTRICT WHERE OMRESTRICT.FUNC_RULE='BLIST' AND SPECIFIC_TOOL=0 AND OMRESTRICT.ID IN (";
        if (CimBooleanUtils.isTrue(hInhibitSearchConditionExistFlag)) {
            String tmpSelectBuf = String.format("  SELECT C.ID FROM OMRESTRICT C WHERE OMRESTRICT.FUNC_RULE='BLIST' AND SPECIFIC_TOOL=0 AND %s", searchConditionForInhibit);
            tmpBuf += tmpSelectBuf;
            tmpPreBuf += tmpSelectBuf;
            bSetFlag = true;
        }
        if (CimBooleanUtils.isTrue(hInhibitEntitySearchConditionExistFlag)) {
            if (CimBooleanUtils.isTrue(bSetFlag)) {
                tmpBuf += " INTERSECT ";
                tmpPreBuf += " INTERSECT ";
            }
            String tmpSelectBuf = String.format("SELECT D.REFKEY FROM %s, OMRESTRICT_ENTITY D WHERE A.REFKEY = D.REFKEY GROUP BY D.REFKEY ", searchConditionForInhibitEntity);
            tmpBuf += tmpSelectBuf;
            tmpPreBuf += tmpSelectBuf;
            if (CimBooleanUtils.isFalse(wildCardFlag)) {
                String tmpHavingBuf = String.format(" HAVING COUNT(D.REFKEY) = %d", entitiesLen);
                tmpBuf += tmpHavingBuf;
                tmpPreBuf += tmpHavingBuf;
            }
            bSetFlag = true;
        }
        if (CimBooleanUtils.isTrue(hInhibitRsnInfoSearchConditionExistFlag)) {
            if (CimBooleanUtils.isTrue(bSetFlag)) {
                tmpBuf += " INTERSECT ";
                tmpPreBuf += " INTERSECT ";
            }
            String tmpSelectBuf = String.format("SELECT E.REFKEY FROM %s, OMRESTRICT_RSNINFO E WHERE F.REFKEY =E.REFKEY GROUP BY E.REFKEY ", searchConditionForInhibitRsnInfo);
            tmpBuf += tmpSelectBuf;
            tmpPreBuf += tmpSelectBuf;
            bSetFlag = true;
        }
        if (CimBooleanUtils.isTrue(hInhibitSPCInfoSearchConditionExistFlag)) {
            if (CimBooleanUtils.isTrue(bSetFlag)) {
                tmpBuf += " INTERSECT ";
                tmpPreBuf += " INTERSECT ";
            }
            String tmpSelectBuf = String.format("SELECT G.REFKEY FROM %s, OMRESTRICT_RSNINFO_SPC G " +
                    "WHERE H.REFKEY =G.REFKEY GROUP BY G.REFKEY ", searchConditionForInhibitSPCInfo);
            tmpBuf += tmpSelectBuf;
            tmpPreBuf += tmpSelectBuf;
            bSetFlag = true;
        }

        String hFrCodeCateGoryID = BizConstant.SP_REASONCAT_ENTITYINHIBIT;
        String configInhibitMaxSeqLen = StandardProperties.OM_CONSTRAINT_MAX_LIST_INQ.getValue();
        int fetchLimitCount = CimStringUtils.isEmpty(configInhibitMaxSeqLen) ? 0 : Integer.parseInt(configInhibitMaxSeqLen);
        if (fetchLimitCount < 1) {
            fetchLimitCount = Integer.parseInt(BizConstant.CONST_TWO_THOUSAND);
        }
        SearchCondition searchCondition = mfgRestrictListInqParams.getSearchCondition();
        Integer from = 0;
        Integer to = fetchLimitCount;
        Integer page = 1;

//        if ((null != searchCondition) && (searchCondition.getPage() != null) && (searchCondition.getSize() != null)) {
//            from = (searchCondition.getPage() - 1) * (searchCondition.getSize());
//            to = from + searchCondition.getSize();
//            page = searchCondition.getPage();
//        }

        tmpBuf += String.format("\t )ORDER BY  OMRESTRICT.ID ) A  WHERE ROWNUM <= %d ) B WHERE B.RN > %d ) P\n" +
                "\tLEFT JOIN OMRESTRICT_ENTITY S1 ON P.ID = S1.REFKEY\n" +
                "\tLEFT JOIN OMRESTRICT_LOTTP S2 ON P.ID = S2.REFKEY\n" +
                "\tLEFT JOIN OMCODE S3\n" +
                "\tON S3.CODETYPE_ID = '%s' AND P.REASON_CODE = S3.CODE_ID\n" +
                "ORDER BY P.ID, S1.IDX_NO, S2.IDX_NO", to, from, hFrCodeCateGoryID);

        tmpPreBuf += String.format("\t) ) P LEFT JOIN OMCODE S3 ON S3.CODETYPE_ID = '%s' AND P.REASON_CODE = S3.CODE_ID", hFrCodeCateGoryID);
        Object[] queryResult = cimJpaRepository.queryOne(tmpPreBuf);
        Integer contextSize = (queryResult == null || queryResult.length == 0) ? 0 : Integer.parseInt(queryResult[0].toString());
   /* tmpBuf += String.format(") ORDER BY OMRESTRICT.ID) P " +
            "LEFT JOIN OMRESTRICT_ENTITY S1 ON P.ID = S1.REFKEY " +
            "LEFT JOIN OMRESTRICT_LOTTP S2 ON P.ID = S2.REFKEY " +
            "LEFT JOIN OMCODE S3 ON S3.CODETYPE_ID = '%s' AND P.RSN_CODE = S3.CODE_ID " +
            "ORDER BY P.ID, S1.D_SEQNO, S2.D_SEQNO ", hFrCodeCateGoryID);*/

        log.info("tmpBufSql : {}", tmpBuf);
        List<Infos.QueriedEntityInhibit> queriedEntityInhibits = new ArrayList<>();
        List<Object[]> results = cimJpaRepository.query(tmpBuf);
        if (CimArrayUtils.getSize(results) > 0) {
            for (int i = 0; i < CimArrayUtils.getSize(results); i++) {
                Object[] objects = (Object[]) results.get(i);

                Infos.QueriedEntityInhibit queriedEntityInhibit = new Infos.QueriedEntityInhibit();

                queriedEntityInhibit.setId(String.valueOf(objects[0]));
                queriedEntityInhibit.setEntityInhibitID(String.valueOf(objects[1]));
                queriedEntityInhibit.setEntityInhibitDescription(String.valueOf(objects[2]));
                queriedEntityInhibit.setEntityInhibitStartTime(String.valueOf(objects[3]));
                queriedEntityInhibit.setEntityInhibitEndTime(String.valueOf(objects[4]));
                queriedEntityInhibit.setEntityInhibitChangeTime(String.valueOf(objects[5]));
                queriedEntityInhibit.setEntityInhibitOwnerID(String.valueOf(objects[6]));
                queriedEntityInhibit.setEntityInhibitOwnerObj(String.valueOf(objects[7]));
                queriedEntityInhibit.setEntityInhibitRsnCode(String.valueOf(objects[8]));
                queriedEntityInhibit.setEntityInhibitRsnObj(String.valueOf(objects[9]));
                if (!CimObjectUtils.isEmpty(objects[10])){
                    queriedEntityInhibit.setEntityInhibitClaimMemo(String.valueOf(objects[10]));
                }
                queriedEntityInhibit.setEntityInhibitEntityDataSeqNo(Integer.valueOf(objects[11].toString()));
                queriedEntityInhibit.setEntityInhibitEntityClassName(String.valueOf(objects[12]));
                queriedEntityInhibit.setEntityInhibitEntityID(String.valueOf(objects[13]));
                queriedEntityInhibit.setEntityInhibitEntityAttrib(String.valueOf(objects[14]));
                queriedEntityInhibit.setEntityInhibitSlotTPDataSeqNo(Integer.valueOf(objects[15].toString()));
                if (!CimObjectUtils.isEmpty(objects[16])) {
                    queriedEntityInhibit.setEntityInhibitSlotTPSubLotType(String.valueOf(objects[16]));
                }
                queriedEntityInhibit.setCodeDescription(String.valueOf(objects[17]));

                queriedEntityInhibits.add(queriedEntityInhibit);
            }
        }
        //Step-6:Get record from OMRESTRICT table;
        log.info("Step-6:Get record from OMRESTRICT table");

        Integer expandRecordLen = Integer.parseInt(BizConstant.CONST_FIVE_HUNDRED);
        Integer listCount = 0;
        String checkKey = BizConstant.EMPTY;
        Integer checkNo = 0;
        Boolean countSubLotTypeFlag = true;
        //boolean firstFlag = true;


        log.info("Maximum of Fetch Count = {}", fetchLimitCount);

        log.info("listCount : {}", listCount);
        List<String> sysKeySeq = new ArrayList<>();
        int count = 0;
        int entityInhibitionsCount = CimArrayUtils.getSize(queriedEntityInhibits);


        for (int i = 0; i < entityInhibitionsCount; i++) {
            Infos.QueriedEntityInhibit queriedEntityInhibit = queriedEntityInhibits.get(i);

            log.info("EntityInhibit_ID : {}", queriedEntityInhibit.getEntityInhibitID());
            log.info("checkKey : {}", checkKey);

            if (!CimStringUtils.equals(checkKey, queriedEntityInhibit.getId())) {
                log.info("this is a new checkKey.");
                Timestamp currentTimeStamp = CimDateUtils.getCurrentTimeStamp();
                boolean activeFlag = false;
                if (currentTimeStamp.after(CimDateUtils.convertTo(queriedEntityInhibit.getEntityInhibitStartTime()))
                        && currentTimeStamp.before(CimDateUtils.convertTo(queriedEntityInhibit.getEntityInhibitEndTime()))) {
                    activeFlag = true;
                }
                String status = mfgRestrictListInqParams.getEntityInhibitDetailAttributes().getStatus();
                if ((CimStringUtils.equals(status,BizConstant.SP_MFGSTATE_INACTIVE)&&activeFlag)
                ||(CimStringUtils.equals(status,BizConstant.SP_MFGSTATE_ACTIVE)&&!activeFlag)){
                    continue;
                }

                //intialize;
                Infos.ConstraintEqpDetailInfo tmpEntityInfo = new Infos.ConstraintEqpDetailInfo();
                Infos.ConstraintDetailAttributes tmpEntityInhibitAttributes = new Infos.ConstraintDetailAttributes();
                List<Infos.EntityIdentifier> tmpEntities = new ArrayList<>();
                List<String> tmpSubLotTypes = new ArrayList<>();

                tmpEntityInhibitAttributes.setEntities(tmpEntities);
                tmpEntityInfo.setEntityInhibitDetailAttributes(tmpEntityInhibitAttributes);
                //tmpEntityInhibitDetailInfos.add(tmpEntityInfo);

                //get checkKey and checkNo;
                checkKey = queriedEntityInhibit.getId();
                sysKeySeq.add(checkKey);
                checkNo = queriedEntityInhibit.getEntityInhibitEntityDataSeqNo();
                countSubLotTypeFlag = true;

                if (listCount <= i + 1) {
                    listCount += expandRecordLen;
                }
                count++;

                if (i + 1 >= fetchLimitCount) {
                    log.info("Length of entityinhibit reached the maximum = {}", i + 1);
                    break;
                }

                if (activeFlag) {
                    tmpEntityInhibitAttributes.setStatus(BizConstant.SP_MFGSTATE_ACTIVE);
                } else {
                    tmpEntityInhibitAttributes.setStatus(BizConstant.SP_MFGSTATE_INACTIVE);
                }
                tmpEntityInfo.setEntityInhibitID(new ObjectIdentifier(queriedEntityInhibit.getEntityInhibitID(),
                        queriedEntityInhibit.getId()));
                tmpEntityInhibitAttributes.setStartTimeStamp(queriedEntityInhibit.getEntityInhibitStartTime());
                tmpEntityInhibitAttributes.setEndTimeStamp(queriedEntityInhibit.getEntityInhibitEndTime());
                tmpEntityInhibitAttributes.setReasonCode(queriedEntityInhibit.getEntityInhibitRsnCode());
                tmpEntityInhibitAttributes.setMemo(queriedEntityInhibit.getEntityInhibitClaimMemo());
                tmpEntityInhibitAttributes.setOwnerID(new ObjectIdentifier(queriedEntityInhibit.getEntityInhibitOwnerID(),
                        queriedEntityInhibit.getEntityInhibitOwnerObj()));
                tmpEntityInhibitAttributes.setClaimedTimeStamp(queriedEntityInhibit.getEntityInhibitChangeTime());
                tmpEntityInhibitAttributes.setSpecTool(false);
                tmpEntityInhibitAttributes.setFunctionRule(BizConstant.FUNCTION_RULE_BLIST);

                List<Infos.EntityInhibitExceptionLotInfo> exceptionLotInfoList = new ArrayList<>();
                String queryLot = "SELECT * FROM OMRESTRICT_EXPLOT WHERE REFKEY = ?1";
                List<CimRestrictionExpLotDO> exceptionLots = cimJpaRepository.query(queryLot, CimRestrictionExpLotDO.class, queriedEntityInhibit.getId());
                if (!CimObjectUtils.isEmpty(exceptionLots)) {
                    for (CimRestrictionExpLotDO item : exceptionLots) {
                        Infos.EntityInhibitExceptionLotInfo exceptionLotInfo = new Infos.EntityInhibitExceptionLotInfo();
                        exceptionLotInfo.setLotID(ObjectIdentifier.build(item.getExceptLotID(), item.getExceptLotObj()));
                        exceptionLotInfo.setSingleTriggerFlag(item.getSingleTrigFlag());
                        exceptionLotInfo.setUsedFlag(item.getUsedFlag());
                        exceptionLotInfo.setClaimUserID(ObjectIdentifier.build(item.getClaimUserID(), item.getClaimUserObj()));
                        exceptionLotInfo.setClaimMemo(item.getClaimMemo());

                        String claimTime = (item.getClaimTime() == null) ?
                                BizConstant.EMPTY :
                                item.getClaimTime().toString();
                        exceptionLotInfo.setClaimTime(claimTime);
                        exceptionLotInfoList.add(exceptionLotInfo);
                    }
                }
                tmpEntityInhibitAttributes.setExceptionLotList(exceptionLotInfoList);

                /*tmpEntityInhibitAttributes.setEntityInhibitReasonDetailInfos();*/

                checkNo = queriedEntityInhibit.getEntityInhibitEntityDataSeqNo();

                Infos.EntityIdentifier tmpEntityIdentifier = new Infos.EntityIdentifier();
                ObjectIdentifier entityID = new ObjectIdentifier(queriedEntityInhibit.getEntityInhibitEntityID(),
                        queriedEntityInhibit.getEntityInhibitEntityObj());
                tmpEntityIdentifier.setObjectID(entityID);
                tmpEntityIdentifier.setClassName(queriedEntityInhibit.getEntityInhibitEntityClassName());
                tmpEntityIdentifier.setAttribution(queriedEntityInhibit.getEntityInhibitEntityAttrib());

                tmpEntities.add(tmpEntityIdentifier);

                if (!CimObjectUtils.isEmpty(queriedEntityInhibit.getEntityInhibitSlotTPSubLotType()) &&
                        !CimStringUtils.equals(queriedEntityInhibit.getEntityInhibitSlotTPSubLotType(),"null")) {
                    tmpSubLotTypes.add(queriedEntityInhibit.getEntityInhibitSlotTPSubLotType());
                    tmpEntityInhibitAttributes.setSubLotTypes(tmpSubLotTypes);
                }else {
                    List<String> sb = new ArrayList<>();
                    sb.add("*");
                    tmpEntityInhibitAttributes.setSubLotTypes(sb);
                }
                tmpEntityInhibitDetailInfos.add(tmpEntityInfo);
            } else {
                if (checkNo != queriedEntityInhibit.getEntityInhibitEntityDataSeqNo()) {
                    log.info("this is a new EntityInhibit_Entity_dSeqNo");

                    Infos.EntityIdentifier tmpEntityIdentifier = new Infos.EntityIdentifier();
                    ObjectIdentifier entityID = new ObjectIdentifier(queriedEntityInhibit.getEntityInhibitEntityID(),
                            queriedEntityInhibit.getEntityInhibitEntityObj());
                    tmpEntityIdentifier.setObjectID(entityID);
                    tmpEntityIdentifier.setClassName(queriedEntityInhibit.getEntityInhibitEntityClassName());
                    tmpEntityIdentifier.setAttribution(queriedEntityInhibit.getEntityInhibitEntityAttrib());

                    //get index to add entity for the same entity inhibit;
                    if (CimArrayUtils.isNotEmpty(tmpEntityInhibitDetailInfos)) {
                        for (Infos.ConstraintEqpDetailInfo item : tmpEntityInhibitDetailInfos) {
                            if (CimStringUtils.equals(item.getEntityInhibitID().getReferenceKey(), checkKey)) {
                                item.getEntityInhibitDetailAttributes().getEntities().add(tmpEntityIdentifier);
                                break;
                            }
                        }
                    }
                    countSubLotTypeFlag = false;

                    checkNo = queriedEntityInhibit.getEntityInhibitEntityDataSeqNo();
                } else {
                    if (CimObjectUtils.isEmpty(queriedEntityInhibit.getEntityInhibitSlotTPSubLotType())) {
                        continue;
                    } else {
                        if (CimBooleanUtils.isTrue(countSubLotTypeFlag)) {
                            log.info("countSubLotTypeFlag == TRUE");

                            //get index to add sub lot type for the same entity inhibit;
                            if (CimArrayUtils.isNotEmpty(tmpEntityInhibitDetailInfos)) {
                                for (Infos.ConstraintEqpDetailInfo item : tmpEntityInhibitDetailInfos) {
                                    if (CimStringUtils.equals(item.getEntityInhibitID().getReferenceKey(), checkKey)) {
                                        item.getEntityInhibitDetailAttributes().getSubLotTypes().add(queriedEntityInhibit.getEntityInhibitSlotTPSubLotType());
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        //Step-7:  Set return structure;
        log.info("Step-7:  Set return structure");
        if (count > 0) {
            if (CimBooleanUtils.isTrue(checkWildCardAfterSelectFlag)
                    && useWildCardFlg != 0
                    && (!CimObjectUtils.isEmpty(wildCardStr))) {
                int recordLen = CimArrayUtils.getSize(tmpEntityInhibitDetailInfos);

                List<Infos.ConstraintEqpDetailInfo> checkEntityInhibitInfos = new ArrayList<>();
                List<String> checkSyskeySeq = new ArrayList<>();
                int markCount = 0;
                Boolean bInhibitFlag = true;

                for (int j = 0; j < recordLen; j++) {
                    Infos.ConstraintDetailAttributes checkEntityInhibitAttributes = tmpEntityInhibitDetailInfos.get(j).getEntityInhibitDetailAttributes();
                    int recordEntitiesLen = CimArrayUtils.getSize(checkEntityInhibitAttributes.getEntities());
                    for (int k = 0; k < recordEntitiesLen; k++) {
                        for (int i = 0; i < entitiesLen; i++) {
                            String classNameStr = BizConstant.EMPTY;
                            if (CimStringUtils.equals(BizConstant.SP_INHIBITCLASSID_OPERATION, checkEntityInhibitAttributes.getEntities().get(i).getClassName())) {
                                classNameStr = BizConstant.SP_INHIBITCLASSID_ROUTE;
                            } else {
                                classNameStr = checkEntityInhibitAttributes.getEntities().get(i).getClassName();
                            }

                            if (classNameStr.compareTo(checkEntityInhibitAttributes.getEntities().get(k).getClassName()) == 0) {
                                int lastIndex = checkEntityInhibitAttributes.getEntities().get(i).getObjectID().getValue().indexOf(wildCardStr);
                                String wildCardExist = null;
                                if(lastIndex > 0) {
                                    wildCardExist = checkEntityInhibitAttributes.getEntities().get(i).getObjectID().getValue().substring(lastIndex);
                                }

                                if (!CimObjectUtils.isEmpty(wildCardExist)) {
                                    break;
                                }

                                int lastPosition = checkEntityInhibitAttributes.getEntities().get(k).getObjectID().getValue().indexOf(wildCardStr);
                                String wildCardPos = null;
                                if(lastPosition > 0) {
                                    wildCardPos = checkEntityInhibitAttributes.getEntities().get(k).getObjectID().getValue().substring(lastPosition);
                                }

                                int compareLength = 0;
                                if (CimObjectUtils.isEmpty(wildCardPos))   // WildCard is not used.
                                {
                                    break;
                                } else                        // WildCard is used;
                                {
                                    compareLength = wildCardPos.length() - checkEntityInhibitAttributes.getEntities().get(k).getObjectID().getValue().length();
                                }

                                if (checkEntityInhibitAttributes.getEntities().get(i).getObjectID().getValue().compareTo(checkEntityInhibitAttributes.getEntities().get(k).getObjectID().getValue()) == 0) {
                                    bInhibitFlag = false;
                                    break;
                                }
                            }
                        }

                        if (CimBooleanUtils.isFalse(bInhibitFlag)) {
                            break;
                        }
                    }

                    if (CimBooleanUtils.isTrue(bInhibitFlag)) {
                        checkEntityInhibitInfos.add(tmpEntityInhibitDetailInfos.get(j));
                        checkSyskeySeq.add(sysKeySeq.get(j));
                    }

                    bInhibitFlag = true;
                }
            }

            //Step-8:Set Entity Inhibit Reason Detail Information;
            log.info("Step-8:Set Entity Inhibit Reason Detail Information");
            if (CimBooleanUtils.isTrue(entityInhibitReasonDetailInfoFlag)) {
                log.info("entityInhibitReasonDetailInfoFlag == TRUE");
                String wspcLinkUrl = StandardProperties.OM_SPC_URL.getValue();
                log.info("OM_SPC_URL : {}", wspcLinkUrl);

                int inhibitInfoLen = CimArrayUtils.getSize(tmpEntityInhibitDetailInfos);
                for (int inhibitCnt = 0; inhibitCnt < inhibitInfoLen; inhibitCnt++) {
                    Infos.ConstraintEqpDetailInfo loopEntityInhibitDetailInfo = tmpEntityInhibitDetailInfos.get(inhibitCnt);
                    if (loopEntityInhibitDetailInfo == null) {
                        continue;
                    }
                    log.info("EntityInhibit_ID = {}", loopEntityInhibitDetailInfo.getEntityInhibitID());
                    List<CimRestrictionRsnInfoDO> entityInhibitRsnInfos = cimJpaRepository.query("SELECT * FROM OMRESTRICT_RSNINFO WHERE REFKEY = ?1", CimRestrictionRsnInfoDO.class, loopEntityInhibitDetailInfo.getEntityInhibitID().getReferenceKey());
                    int hCount = CimArrayUtils.getSize(entityInhibitRsnInfos);
                    log.info("hCount = {}", hCount);
                    if (hCount == 0) {
                        log.info("hCount == 0 Continue!!!");
                        continue;
                    }
                    //need to confirm:allEntityInhibitReasonDetailInfoFor;
                    List<Constrain.EntityInhibitReasonDetailInfo> reasonDetailInfos = restrictionManager.allEntityInhibitReasonDetailInfoFor(loopEntityInhibitDetailInfo.getEntityInhibitID());
                    //List<Infos.EntityInhibitReasonDetailInfo> reasonDetailInfos = entityInhibitManager.allEntityInhibitReasonDetailInfoFor(loopEntityInhibitDetailInfo.getEntityInhibitID());

                    int rsnDetailInfoLen = CimArrayUtils.getSize(reasonDetailInfos);
                    log.info("rsnDetailInfoLen : {}", rsnDetailInfoLen);

                    Infos.ConstraintDetailAttributes outEntityInhibitDetailAttributes = tmpEntityInhibitDetailInfos.get(inhibitCnt).getEntityInhibitDetailAttributes();
                    List<Infos.EntityInhibitReasonDetailInfo> outEntityInhibitReasonDetailInfos = new ArrayList<>();
                    for (int rsnCnt = 0; rsnCnt < rsnDetailInfoLen; rsnCnt++) {
                        Infos.EntityInhibitReasonDetailInfo outEntityInhibitReasonDetailInfo = new Infos.EntityInhibitReasonDetailInfo();
                        outEntityInhibitReasonDetailInfo.setRelatedLotID(reasonDetailInfos.get(rsnCnt).getRelatedLotID());
                        outEntityInhibitReasonDetailInfo.setRelatedControlJobID(reasonDetailInfos.get(rsnCnt).getRelatedControlJobID());
                        outEntityInhibitReasonDetailInfo.setRelatedFabID(reasonDetailInfos.get(rsnCnt).getRelatedFabID());
                        outEntityInhibitReasonDetailInfo.setRelatedRouteID(reasonDetailInfos.get(rsnCnt).getRelatedRouteID());
                        outEntityInhibitReasonDetailInfo.setRelatedProcessDefinitionID(reasonDetailInfos.get(rsnCnt).getRelatedProcessDefinitionID());
                        outEntityInhibitReasonDetailInfo.setRelatedOperationNumber(reasonDetailInfos.get(rsnCnt).getRelatedOperationNumber());
                        outEntityInhibitReasonDetailInfo.setRelatedOperationPassCount(reasonDetailInfos.get(rsnCnt).getRelatedOperationPassCount());


                        int spcChartInfoLen = CimArrayUtils.getSize(reasonDetailInfos.get(rsnCnt).getStrEntityInhibitSpcChartInfos());

                        log.info("spcChartInfoLen : {}", spcChartInfoLen);

                        List<Infos.EntityInhibitSpcChartInfo> outStrEntityInhibitSpcChartInfos = new ArrayList<>();

                        for (int spcInfoCnt = 0; spcInfoCnt < spcChartInfoLen; spcInfoCnt++) {
                            Infos.EntityInhibitSpcChartInfo outEntityInhibitSpcChartInfo = new Infos.EntityInhibitSpcChartInfo();

                            Constrain.EntityInhibitSpcChartInfo tmpEntityInhibitSpcChartInfo = reasonDetailInfos.get(rsnCnt).getStrEntityInhibitSpcChartInfos().get(spcInfoCnt);
                            outEntityInhibitSpcChartInfo.setRelatedSpcDcType(tmpEntityInhibitSpcChartInfo.getRelatedSpcDcType());
                            outEntityInhibitSpcChartInfo.setRelatedSpcChartGroupID(tmpEntityInhibitSpcChartInfo.getRelatedSpcChartGroupID());
                            outEntityInhibitSpcChartInfo.setRelatedSpcChartID(tmpEntityInhibitSpcChartInfo.getRelatedSpcChartID());
                            outEntityInhibitSpcChartInfo.setRelatedSpcChartType(tmpEntityInhibitSpcChartInfo.getRelatedSpcChartType());
                            String tmpURL = String.format(wspcLinkUrl, tmpEntityInhibitSpcChartInfo.getRelatedSpcDcType(), tmpEntityInhibitSpcChartInfo.getRelatedSpcChartGroupID(),
                                    tmpEntityInhibitSpcChartInfo.getRelatedSpcChartID(), tmpEntityInhibitSpcChartInfo.getRelatedSpcChartType());
                            outEntityInhibitSpcChartInfo.setRelatedSpcChartUrl(tmpURL);

                            outStrEntityInhibitSpcChartInfos.add(outEntityInhibitSpcChartInfo);
                        }
                        outEntityInhibitReasonDetailInfo.setStrEntityInhibitSpcChartInfos(outStrEntityInhibitSpcChartInfos);
                        outEntityInhibitReasonDetailInfos.add(outEntityInhibitReasonDetailInfo);
                    }
                }
            }
        }
    }

    @Override
    public List<Infos.ConstraintEqpDetailInfo> constraintAttributesGetByEqpDR(
            Params.MfgRestrictListByEqpInqParams mfgRestrictListByEqpInqParams, Infos.ObjCommon objCommon) {

        String eqpID = mfgRestrictListByEqpInqParams.getEqpID();

        List<Infos.ConstraintEqpDetailInfo> constraintEqpDetailInfos = constraintListByEqp(objCommon,
                ObjectIdentifier.buildWithValue(eqpID), BizConstant.FUNCTION_RULE_BLIST, false);

        return constraintEqpDetailInfos;
    }

    @Override
    public List<Infos.ConstraintEqpDetailInfo> constraintAttributesGetByEqpListDR(List<Infos.AreaEqp> areaEqpList,
                                                                                  Infos.ObjCommon objCommon) {
        List<Infos.ConstraintEqpDetailInfo> constraintEqpDetailInfos = new ArrayList<>();
        for (Infos.AreaEqp areaEqp : areaEqpList) {
            String eqpID = areaEqp.getEquipmentID().getValue();
            List<Infos.ConstraintEqpDetailInfo> constraintEqpDetailInfosList = constraintListByEqp(objCommon,
                    ObjectIdentifier.buildWithValue(eqpID), BizConstant.FUNCTION_RULE_BLIST, false);
            constraintEqpDetailInfos.addAll(constraintEqpDetailInfosList);
        }
        return constraintEqpDetailInfos;
    }

    @Override
    public void constraintExceptionLotCheckValidity(List<Infos.EntityInhibitExceptionLot> exceptionLotList,
                                                    Infos.ObjCommon objCommon) {
        log.info("【Method Entry】entityInhibitExceptionLotCheckValidity()");

        int numOfExpLots = CimArrayUtils.getSize(exceptionLotList);
        for (int i = 0; i < numOfExpLots; i++) {
            //【Step1】Check entity inhibit existence ;
            log.info("#### Check entity inhibit existence ####");
            Infos.EntityInhibitExceptionLot exceptionLot = exceptionLotList.get(i);
            CimRestriction anEntityInhibit = baseCoreFactory.getBO(CimRestriction.class,
                    exceptionLot.getEntityInhibitID());
            Validations.check(anEntityInhibit == null, retCodeConfig.getNotFoundEntityInhibit());

            //【Step2】Check lot existence;
            log.info("#### Check lot existence ####");
            String lotId = ObjectIdentifier.fetchValue(exceptionLot.getLotID());
            log.info("lot identifier = {}", lotId);

            CimLotDO example = new CimLotDO();
            example.setLotID(lotId);
            CimLotDO lot = cimJpaRepository.findOne(Example.of(example)).orElse(null);
            Validations.check(lot == null, retCodeConfig.getNotFoundLot());

            //【Step3】Check the duplicate data;
            log.info("#### Check exception lot existence ####");
            Constrain.ExceptionLotRecord anExceptionLotRecord = anEntityInhibit
                    .getExceptionLotRecord(exceptionLot.getLotID());
            boolean isExist = true;

            if (anExceptionLotRecord == null) {
                isExist = false;
            }
            Validations.check(isExist, new OmCode(retCodeConfig.getDuplicateEntityInhibitExceptLot(),
                    ObjectIdentifier.fetchValue(exceptionLot.getLotID())));
        }

        log.info("【Method Exit】entityInhibitExceptionLotCheckValidity()");

    }

    @Override
    public List<Infos.EntityInhibitDetailInfo> constraintExceptionLotRegistrationReq(
            Infos.ObjCommon objCommon, List<Infos.EntityInhibitExceptionLot> entityInhibitExceptionLots,
            String claimMemo) {

        log.info("【Method Entry】entityInhibitExceptionLotRegistrationReq()");
        List<Infos.EntityInhibitDetailInfo> entityInhibitDetailInfoList = new ArrayList<>();

        //【Step1】Convert input parameters;
        List<Infos.EntityInhibitExceptionLot> entityInhibitions = entityInhibitExceptionLots;

        //【Step2】 Set output parameters;
        List<String> hasEntityInhibits = new ArrayList<>();
        int numOfExpLots = CimArrayUtils.getSize(entityInhibitions);
        if (numOfExpLots > 0) {
            for (Infos.EntityInhibitExceptionLot item : entityInhibitions) {
                //Get Entity Inhibit Information;
                CimRestriction anEntityInhibit = baseCoreFactory.getBO(CimRestriction.class, item.getEntityInhibitID());
                Validations.check(CimObjectUtils.isEmpty(anEntityInhibit), retCodeConfig.getNotFoundEntityInhibit());
                Constrain.EntityInhibitRecord aRecord = anEntityInhibit.getInhibitRecord();

                //get except lot info(Get lot OBJ);
                String lotId = ObjectIdentifier.fetchValue(item.getLotID());
                CimLotDO example = new CimLotDO();
                example.setLotID(lotId);
                CimLotDO lot = cimJpaRepository.findOne(Example.of(example)).orElse(null);
                Validations.check(lot == null, retCodeConfig.getNotFoundLot());

                ObjectIdentifier lotID = ObjectIdentifier.build(lotId, lot.getId());

                //Set exception lot data.
                Constrain.ExceptionLotRecord anExceptionLotRecord = new Constrain.ExceptionLotRecord();
                anExceptionLotRecord.setLotID(lotID);
                anExceptionLotRecord.setSingleTriggerFlag(item.getSingleTriggerFlag());
                anExceptionLotRecord.setUsedFlag(false);
                anExceptionLotRecord.setClaimUserID(objCommon.getUser().getUserID());
                anExceptionLotRecord.setClaimMemo(claimMemo);
                anExceptionLotRecord.setClaimTimeStamp(objCommon.getTimeStamp().getReportTimeStamp());

                //set except lot record information for the entity inhibit to table;
                anEntityInhibit.setExceptionLotRecord(lotID, anExceptionLotRecord);

                Infos.EntityInhibitExceptionLotInfo exceptionLot = new Infos.EntityInhibitExceptionLotInfo();
                exceptionLot.setLotID(lotID);
                exceptionLot.setSingleTriggerFlag(item.getSingleTriggerFlag());

                String timeStamp = (objCommon.getTimeStamp().getReportTimeStamp() == null) ?
                        BizConstant.EMPTY :
                        objCommon.getTimeStamp().getReportTimeStamp().toString();
                exceptionLot.setClaimTime(timeStamp);

                if (hasEntityInhibits.contains(item.getEntityInhibitID())) {
                    //this except lot need to add the entity inhibit,but not out put entity inhibit detail information;
                    int index = hasEntityInhibits.indexOf(item.getEntityInhibitID());
                    if (index > -1) {
                        log.info("entityInhibitExceptionLotRegistrationReq() : exclude the same entity inhibit.");
                        Infos.EntityInhibitDetailInfo indexInfo = entityInhibitDetailInfoList.get(index);
                        if (indexInfo != null) {
                            indexInfo.getEntityInhibitDetailAttributes().getEntityInhibitExceptionLotInfos()
                                    .add(exceptionLot);
                        }
                    }
                    continue;
                }

                hasEntityInhibits.add(aRecord.getId());

                //Set output information;
                Infos.EntityInhibitDetailInfo entityInhibitDetailInfo = new Infos.EntityInhibitDetailInfo();
                entityInhibitDetailInfo.setEntityInhibitID(ObjectIdentifier.build(aRecord.getId(),
                        aRecord.getReferenceKey()));

                Infos.EntityInhibitDetailAttributes attributes = new Infos.EntityInhibitDetailAttributes();
                entityInhibitDetailInfo.setEntityInhibitDetailAttributes(attributes);

                attributes.setEntities(new ArrayList<>());
                attributes.setSubLotTypes(new ArrayList<>());
                String startTimeStamp = (aRecord.getStartTimeStamp() == null) ?
                        BizConstant.EMPTY :
                        aRecord.getStartTimeStamp().toString();
                attributes.setStartTimeStamp(startTimeStamp);

                String endTimeStamp = (aRecord.getEndTimeStamp() == null) ?
                        BizConstant.EMPTY :
                        aRecord.getEndTimeStamp().toString();
                attributes.setEndTimeStamp(endTimeStamp);

                String reasonCode = (aRecord.getReasonCode() == null) ?
                        BizConstant.EMPTY :
                        aRecord.getReasonCode().getValue();
                attributes.setReasonCode(reasonCode);

                attributes.setMemo(aRecord.getClaimMemo());
                attributes.setOwnerID(aRecord.getOwner());

                String changedTimeStamp = (aRecord.getChangedTimeStamp() == null) ?
                        BizConstant.EMPTY :
                        aRecord.getChangedTimeStamp().toString();
                attributes.setClaimedTimeStamp(changedTimeStamp);

                //Set ReasonCodeDescription
                String reasonCodeObj = (aRecord.getReasonCode() == null) ?
                        BizConstant.EMPTY :
                        aRecord.getReasonCode().getReferenceKey();
                CimCode aCode = baseCoreFactory.getBO(CimCode.class, reasonCodeObj);
                if (aCode == null) {
                    attributes.setReasonDesc(BizConstant.EMPTY);
                } else {
                    attributes.setReasonDesc(aCode.getDescription());
                }

                //set exception lot attributes into output parameter
                List<Infos.EntityInhibitExceptionLotInfo> exceptionLotInfoList = new ArrayList<>();
                exceptionLotInfoList.add(exceptionLot);
                attributes.setEntityInhibitExceptionLotInfos(exceptionLotInfoList);

                entityInhibitDetailInfoList.add(entityInhibitDetailInfo);
            }
        }


        log.info("【Method Exit】entityInhibitExceptionLotRegistrationReq()");
        return entityInhibitDetailInfoList;
    }

    @Override
    public List<Infos.EntityInhibitDetailInfo> constraintExceptionLotCancelReq(
            List<Infos.EntityInhibitExceptionLot> entityInhibitExceptionLots, Infos.ObjCommon objCommon) {
        List<Infos.EntityInhibitDetailInfo> strEntityInhibitions = new ArrayList<>();

        //【Step2】 Set output parameters;
        List<String> hasEntityInhibits = new ArrayList<>();
        int numOfCancelReq = CimArrayUtils.getSize(entityInhibitExceptionLots);
        if (numOfCancelReq > 0) {
            for (Infos.EntityInhibitExceptionLot exceptionLotItem : entityInhibitExceptionLots) {
                //Get Entity Inhibit Information;
                CimRestriction anEntityInhibit = baseCoreFactory.getBO(CimRestriction.class,
                        exceptionLotItem.getEntityInhibitID());
                Validations.check(CimObjectUtils.isEmpty(anEntityInhibit), retCodeConfig.getNotFoundEntityInhibit());
                Constrain.EntityInhibitRecord aRecord = anEntityInhibit.getInhibitRecord();
                Validations.check(null == aRecord, retCodeConfig.getNotFoundEntityInhibit());

                //get except lot info;
                Constrain.ExceptionLotRecord exceptionLotRecordRetCode = anEntityInhibit
                        .getExceptionLotRecord(exceptionLotItem.getLotID());
                Validations.check(null == exceptionLotRecordRetCode, retCodeConfig
                        .getNotFoundEntityInhibitExceptLot());


                Infos.EntityInhibitExceptionLotInfo exceptionLot = new Infos.EntityInhibitExceptionLotInfo();
                exceptionLot.setLotID(exceptionLotRecordRetCode.getLotID());
                exceptionLot.setSingleTriggerFlag(exceptionLotRecordRetCode.getSingleTriggerFlag());

                String timeStamp = (objCommon.getTimeStamp().getReportTimeStamp() == null) ?
                        BizConstant.EMPTY :
                        objCommon.getTimeStamp().getReportTimeStamp().toString();
                exceptionLot.setClaimTime(timeStamp);

                //Remove Entity Inhibit Exception lot;
                anEntityInhibit.removeExceptionLot(exceptionLotItem.getLotID());

                if (hasEntityInhibits.contains(exceptionLotItem.getEntityInhibitID())) {
                    //this except lot need to add the entity inhibit,but not out put entity inhibit detail information;
                    int index = hasEntityInhibits.indexOf(exceptionLotItem.getEntityInhibitID());
                    if (index > -1) {
                        log.info("mfgRestrictExclusionLotCancelReq(): exclude the same entity inhibit.");
                        Infos.EntityInhibitDetailInfo indexExceptLotInfo = strEntityInhibitions.get(index);
                        if (indexExceptLotInfo != null) {
                            indexExceptLotInfo.getEntityInhibitDetailAttributes()
                                    .getEntityInhibitExceptionLotInfos().add(exceptionLot);
                        }
                    }
                    continue;
                }

                hasEntityInhibits.add(aRecord.getId());

                //Set output information;
                Infos.EntityInhibitDetailInfo entityInhibitDetailInfo = new Infos.EntityInhibitDetailInfo();
                entityInhibitDetailInfo.setEntityInhibitID(ObjectIdentifier.build(aRecord.getId(),
                        aRecord.getReferenceKey()));

                Infos.EntityInhibitDetailAttributes attributes = new Infos.EntityInhibitDetailAttributes();
                entityInhibitDetailInfo.setEntityInhibitDetailAttributes(attributes);
                attributes.setEntities(new ArrayList<>());
                attributes.setSubLotTypes(new ArrayList<>());

                String startTimeStamp = (aRecord.getStartTimeStamp() == null) ?
                        BizConstant.EMPTY :
                        aRecord.getStartTimeStamp().toString();
                attributes.setStartTimeStamp(startTimeStamp);

                String endTimeStamp = (aRecord.getEndTimeStamp() == null) ?
                        BizConstant.EMPTY :
                        aRecord.getEndTimeStamp().toString();
                attributes.setEndTimeStamp(endTimeStamp);

                String reasonCode = (aRecord.getReasonCode() == null) ?
                        BizConstant.EMPTY :
                        aRecord.getReasonCode().getValue();
                attributes.setReasonCode(reasonCode);

                attributes.setMemo(aRecord.getClaimMemo());
                attributes.setOwnerID(aRecord.getOwner());

                String changedTimeStamp = (aRecord.getChangedTimeStamp() == null) ?
                        BizConstant.EMPTY :
                        aRecord.getChangedTimeStamp().toString();
                attributes.setClaimedTimeStamp(changedTimeStamp);

                //Set ReasonCodeDescription
                String reasonCodeObj = (aRecord.getReasonCode() == null) ?
                        BizConstant.EMPTY :
                        aRecord.getReasonCode().getReferenceKey();
                CimCode aCode = baseCoreFactory.getBO(CimCode.class, reasonCodeObj);
                if (aCode == null) {
                    attributes.setReasonDesc(BizConstant.EMPTY);
                } else {
                    attributes.setReasonDesc(aCode.getDescription());
                }

                //set exception lot attributes into output parameter
                List<Infos.EntityInhibitExceptionLotInfo> exceptionLotInfoList = new ArrayList<>();
                exceptionLotInfoList.add(exceptionLot);
                attributes.setEntityInhibitExceptionLotInfos(exceptionLotInfoList);

                strEntityInhibitions.add(entityInhibitDetailInfo);
            }
        }

        return strEntityInhibitions;
    }

    @Override
    public List<Infos.EntityInhibitInfo> constraintEffectiveForLotGetDR(
            Infos.ObjCommon objCommon, List<Infos.EntityInhibitInfo> entityInhibitInfos, ObjectIdentifier lotID) {

        List<Infos.EntityInhibitInfo> entityInhibitInfoList = new ArrayList<>();
        //---------------------------------------------
        //  Set input parameters into local variable
        //---------------------------------------------
        if (CimArrayUtils.isNotEmpty(entityInhibitInfos)) {
            long count = cimJpaRepository.count("SELECT COUNT(ID) FROM OMRESTRICT_EXPLOT WHERE EXCEPT_LOT_ID = ?",
                    ObjectIdentifier.fetchValue(lotID));
            if (count <= 0) {
                entityInhibitInfoList.addAll(entityInhibitInfos);
            } else {
                for (Infos.EntityInhibitInfo entityInhibitInfo : entityInhibitInfos) {
                    String sql = "SELECT COUNT(B.REFKEY) FROM OMRESTRICT A, OMRESTRICT_EXPLOT B WHERE  A.RESTRICT_ID = ?"
                            + " AND A.ID = B.REFKEY AND  B.EXCEPT_LOT_ID = ? AND A.FUNC_RULE = 'BLIST'";
                    count = cimJpaRepository.count(sql, ObjectIdentifier.fetchValue(entityInhibitInfo
                                    .getEntityInhibitID()),
                            ObjectIdentifier.fetchValue(lotID));
                    if (count <= 0) {
                        entityInhibitInfoList.add(entityInhibitInfo);
                    }
                }
            }
        }
        return entityInhibitInfoList;
    }

    @Override
    public void constraintExceptionLotChangeForOpeComp(Infos.ObjCommon objCommon, List<ObjectIdentifier> lotIDs,
                                                       ObjectIdentifier controlJobID) {
        //Set input parameters into local variable;
        int lotCount = CimArrayUtils.getSize(lotIDs);
        for (int i = 0; i < lotCount; i++) {
            ObjectIdentifier lotID = lotIDs.get(i);
            List<CimRestriction> tmpEntityInhibitList = restrictionManager.getEntityInhibitsWithExceptionLotByLot(lotID);

            int numOfInhibit = CimArrayUtils.getSize(tmpEntityInhibitList);
            for (int j = 0; j < numOfInhibit; j++) {
                CimRestriction entityInhibit = tmpEntityInhibitList.get(j);
                if (entityInhibit != null) {
                    Constrain.EntityInhibitRecord entityInhibitRecord = entityInhibit.getInhibitRecord();
                    Constrain.ExceptionLotRecord exceptionLotRecord = entityInhibit.getExceptionLotRecord(lotID);

                    Validations.check(null == entityInhibitRecord, retCodeConfig
                            .getNotFoundEntityInhibitExceptLot());

                    if (exceptionLotRecord.getUsedFlag()) {
                        //Make History Data;
                        Inputs.EntityInhibitEventMakeParams entityInhibitEventMakeParams = new Inputs
                                .EntityInhibitEventMakeParams();
                        Infos.EntityInhibitDetailInfo entityInhibitDetailInfo = new Infos.EntityInhibitDetailInfo();
                        entityInhibitEventMakeParams.setEntityInhibitDetailInfo(entityInhibitDetailInfo);
                        entityInhibitDetailInfo.setEntityInhibitID(ObjectIdentifier.build(entityInhibitRecord.getId(),
                                entityInhibitRecord.getReferenceKey()));
                        Infos.EntityInhibitDetailAttributes entityInhibitAttributes = new Infos
                                .EntityInhibitDetailAttributes();
                        entityInhibitDetailInfo.setEntityInhibitDetailAttributes(entityInhibitAttributes);
                        entityInhibitAttributes.setEntities(new ArrayList<>());
                        entityInhibitAttributes.setSubLotTypes(new ArrayList<>());
                        entityInhibitAttributes.setStartTimeStamp(entityInhibitRecord.getStartTimeStamp().toString());
                        entityInhibitAttributes.setEndTimeStamp(entityInhibitRecord.getEndTimeStamp().toString());
                        entityInhibitAttributes.setReasonCode(entityInhibitRecord.getReasonCode().getValue());
                        entityInhibitAttributes.setMemo(entityInhibitRecord.getClaimMemo());
                        entityInhibitAttributes.setOwnerID(entityInhibitRecord.getOwner());
                        entityInhibitAttributes.setClaimedTimeStamp(entityInhibitRecord.getChangedTimeStamp().toString());
                        Infos.EntityInhibitExceptionLotInfo exceptionLotInfo = new Infos.EntityInhibitExceptionLotInfo();
                        exceptionLotInfo.setClaimTime(exceptionLotRecord.getClaimTimeStamp().toString());
                        exceptionLotInfo.setLotID(lotID);
                        exceptionLotInfo.setSingleTriggerFlag(exceptionLotRecord.getSingleTriggerFlag());
                        exceptionLotInfo.setClaimTime(BizConstant.EMPTY);

                        entityInhibitAttributes.setEntityInhibitExceptionLotInfos(new ArrayList<>());
                        entityInhibitAttributes.setEntityInhibitReasonDetailInfos(new ArrayList<>());
                        entityInhibitAttributes.getEntityInhibitExceptionLotInfos().add(exceptionLotInfo);
                        // Set ReasonCodeDescription;
                        CimCode aCode = baseCoreFactory.getBO(CimCode.class, entityInhibitRecord.getReasonCode());
                        if (aCode == null) {
                            entityInhibitAttributes.setReasonDesc(BizConstant.EMPTY);
                        } else {
                            entityInhibitAttributes.setReasonDesc(aCode.getDescription());
                        }

                        if (exceptionLotRecord.getSingleTriggerFlag()) {
                            // Remove Entity Inhibit Exception lot;
                            entityInhibit.removeExceptionLot(lotID);
                            entityInhibitEventMakeParams.setTransactionID(TransactionIDEnum
                                    .ENTITY_INHIBIT_EXCEPTION_LOT_CANCEL_REQ.getValue());
                            eventMethod.entityInhibitEventMake(objCommon, entityInhibitEventMakeParams);

                        } else {
                            exceptionLotRecord.setUsedFlag(false);
                            entityInhibit.setExceptionLotRecord(lotID, exceptionLotRecord);
                        }
                        entityInhibitEventMakeParams.setTransactionID(objCommon.getTransactionID());
                        entityInhibitEventMakeParams.setControlJobID(controlJobID);
                        eventMethod.entityInhibitEventMake(objCommon, entityInhibitEventMakeParams);
                    }
                }
            }
        }

    }

    @Override
    public List<Infos.EntityInhibitInfo> constraintCheckForReticleInhibition(
            Infos.ObjCommon objCommon, Inputs.EntityInhibitCheckForReticleInhibition inputs) {

        //-------------------------------//
        //-- ReticleGroups restructure --//
        //-------------------------------//
        List<Infos.EntityInhibitInfo> entityInhibitInfos = new ArrayList<>();
        List<ObjectIdentifier> reticleGroupIDs = new ArrayList<>();
        int reticleGrpLen = 0;
        int reticleSeqLen = CimArrayUtils.getSize(inputs.getReticleSeq());
        for (int i = 0; i < reticleSeqLen; i++) {
            int j;
            for (j = 0; j < reticleGrpLen; j++) {
                if (ObjectIdentifier.equalsWithValue(reticleGroupIDs.get(j), inputs.getReticleSeq()
                        .get(i).getReticleGroupID())) {
                    break;
                }
            }
            if (j == reticleGrpLen) {
                reticleGroupIDs.add(inputs.getReticleSeq().get(i).getReticleGroupID());
                reticleGrpLen++;
            }
        }
        log.info("Reticle Group Num : {}", reticleGrpLen);
        //--------------------------------------//
        //   Check usable Reticle existence.    //
        //--------------------------------------//
        if (0 < reticleGrpLen) {
            //-------------------------------//
            //-- InhibitRecord restructure --//
            //-------------------------------//
            List<Constrain.EntityInhibitRecord> inhibitRecords = new ArrayList<>();
            int numOfInhibits = CimArrayUtils.getSize(inputs.getEntityInhibitInfoSeq());
            for (int i = 0; i < numOfInhibits; i++) {
                Infos.EntityInhibitInfo entityInhibitInfo = inputs.getEntityInhibitInfoSeq().get(i);
                Constrain.EntityInhibitRecord entityInhibitRecord = new Constrain.EntityInhibitRecord();
                List<Constrain.EntityIdentifier> entities = new ArrayList<>();
                int numOfEntities = CimArrayUtils.getSize(entityInhibitInfo.getEntityInhibitAttributes().getEntities());
                for (int j = 0; j < numOfEntities; j++) {
                    Infos.EntityIdentifier entityIdentifier = entityInhibitInfo.getEntityInhibitAttributes()
                            .getEntities().get(j);
                    Constrain.EntityIdentifier entity = new Constrain.EntityIdentifier();
                    entity.setClassName(entityIdentifier.getClassName());
                    entity.setObjectId(ObjectIdentifier.fetchValue(entityIdentifier.getObjectID()));
                    entity.setAttrib(entityIdentifier.getAttribution());
                    entities.add(entity);
                }
                entityInhibitRecord.setEntities(entities);
                entityInhibitRecord.setId(entityInhibitInfo.getEntityInhibitID().getValue());
                entityInhibitRecord.setReferenceKey(entityInhibitInfo.getEntityInhibitID().getReferenceKey());
                entityInhibitRecord.setSubLotTypes(entityInhibitInfo.getEntityInhibitAttributes().getSubLotTypes());
                entityInhibitRecord.setStartTimeStamp(CimDateUtils.convertToOrInitialTime(entityInhibitInfo
                        .getEntityInhibitAttributes().getStartTimeStamp()));
                entityInhibitRecord.setEndTimeStamp(CimDateUtils.convertToOrInitialTime(entityInhibitInfo
                        .getEntityInhibitAttributes().getEndTimeStamp()));
                entityInhibitRecord.setChangedTimeStamp(CimDateUtils.convertToOrInitialTime(entityInhibitInfo
                        .getEntityInhibitAttributes().getClaimedTimeStamp()));
                entityInhibitRecord.setReasonCode(new ObjectIdentifier(entityInhibitInfo.getEntityInhibitAttributes()
                        .getReasonCode()));
                entityInhibitRecord.setOwner(entityInhibitInfo.getEntityInhibitAttributes().getOwnerID());
                entityInhibitRecord.setClaimMemo(entityInhibitInfo.getEntityInhibitAttributes().getMemo());
                inhibitRecords.add(entityInhibitRecord);
            }
            //--------------------------//
            //-- Entities restructure --//
            //--------------------------//
            List<Constrain.EntityIdentifier> entities = new ArrayList<>();
            int entLen = CimArrayUtils.getSize(inputs.getEntityIDSeq());
            for (int i = 0; i < entLen; i++) {
                Constrain.EntityIdentifier entityIdentifier = new Constrain.EntityIdentifier();
                entityIdentifier.setClassName(inputs.getEntityIDSeq().get(i).getClassName());
                entityIdentifier.setObjectId(ObjectIdentifier.fetchValue(inputs.getEntityIDSeq().get(i).getObjectID()));
                entityIdentifier.setAttrib(inputs.getEntityIDSeq().get(i).getAttribution());
                entities.add(entityIdentifier);
            }
            //------------------------------------------//
            //   Abstruct Inhibit Record for Reticle    //
            //------------------------------------------//
            List<Constrain.EntityInhibitRecord> tmpInhibitRecords = new ArrayList<>();
            List<Constrain.EntityInhibitRecord> tmpReticleInhibitRecords = new ArrayList<>();
            for (int i = 0; i < numOfInhibits; i++) {
                boolean reticleInhibitFlag = false;
                int entitiesLen = CimArrayUtils.getSize(inhibitRecords.get(i).getEntities());
                for (int j = 0; j < entitiesLen; j++) {
                    if (CimStringUtils.equals(BizConstant.SP_INHIBITCLASSID_RETICLE, inhibitRecords
                            .get(i).getEntities().get(j).getClassName())) {
                        reticleInhibitFlag = true;
                        break;
                    }
                }
                if (reticleInhibitFlag) {
                    tmpReticleInhibitRecords.add(inhibitRecords.get(i));
                } else {
                    tmpInhibitRecords.add(inhibitRecords.get(i));
                }
            }
            if (CimArrayUtils.isNotEmpty(tmpReticleInhibitRecords)) {
                List<Constrain.EntityIdentifier> tmpEntities = new ArrayList<>();
                for (int i = 0; i < entLen; i++) {
                    if (!CimStringUtils.equals(BizConstant.SP_INHIBITCLASSID_RETICLE, entities.get(i).getClassName())) {
                        tmpEntities.add(entities.get(i));
                    }
                }
                for (int i = 0; i < reticleGrpLen; i++) {
                    //-------------------------------------------------------//
                    //   Abstruct Entity Sequence for each Reticle groups    //
                    //-------------------------------------------------------//
                    List<ObjectIdentifier> tmpReticleIDs = new ArrayList<>();
                    int retLen = CimArrayUtils.getSize(inputs.getReticleSeq());
                    for (int j = 0; j < retLen; j++) {
                        if (ObjectIdentifier.equalsWithValue(reticleGroupIDs.get(i), inputs.getReticleSeq()
                                .get(j).getReticleGroupID())) {
                            tmpReticleIDs.add(inputs.getReticleSeq().get(j).getReticleID());
                        }
                    }
                    boolean usableRtclExistFlag = false;
                    Boolean notUsableRtclExistFlag = false;
                    List<Constrain.EntityInhibitRecord> tmpReticleInhibitRecordsforGroups = new ArrayList<>();
                    int count2 = CimArrayUtils.getSize(tmpReticleIDs);
                    for (int j = 0; j < count2; j++) {
                        Constrain.EntityIdentifier entityIdentifier = new Constrain.EntityIdentifier();
                        entityIdentifier.setClassName(BizConstant.SP_INHIBITCLASSID_RETICLE);
                        entityIdentifier.setObjectId(ObjectIdentifier.fetchValue(tmpReticleIDs.get(j)));
                        tmpEntities.add(entityIdentifier);
                        //--------------------------------------------------//
                        //   Check Inhibit Record by each Reticle Groups    //
                        //--------------------------------------------------//
                        usableRtclExistFlag = false;
                        boolean inhibitRecordExistanceFlag = false;
                        for (Constrain.EntityInhibitRecord tmpReticleInhibitRecord : tmpReticleInhibitRecords) {
                            boolean inhibitedFlag = restrictionManager.isInhibitRecordFor(tmpReticleInhibitRecord,
                                    tmpEntities, inputs.getSublottypes());
                            if (inhibitedFlag) {
                                tmpReticleInhibitRecordsforGroups.add(tmpReticleInhibitRecord);
                                inhibitRecordExistanceFlag = true;
                            }
                        }
                        if (!inhibitRecordExistanceFlag) {
                            usableRtclExistFlag = true;
                            if (!inputs.getUseFPCInfo()) {
                                break;
                            }
                        } else {
                            notUsableRtclExistFlag = true;
                        }
                    }
                    if ((!inputs.getUseFPCInfo() && !usableRtclExistFlag)
                            || (inputs.getUseFPCInfo() && notUsableRtclExistFlag)) {
                        //---------------------------------------------------//
                        //   Usable Reticle doesn't exist, then set record   //
                        //---------------------------------------------------//
                        for (Constrain.EntityInhibitRecord tmpReticleInhibitRecordsforGroup
                                : tmpReticleInhibitRecordsforGroups) {
                            //-----------------------------------------//
                            //   If already exists a record, omit it   //
                            //-----------------------------------------//
                            if (!tmpInhibitRecords.contains(tmpReticleInhibitRecordsforGroup)) {
                                tmpInhibitRecords.add(tmpReticleInhibitRecordsforGroup);
                            }
                        }
                    }
                }
                inhibitRecords = tmpInhibitRecords;
                //-------------------------//
                //  Set Return Structure   //
                //-------------------------//
                for (Constrain.EntityInhibitRecord inhibitRecord : inhibitRecords) {
                    Infos.EntityInhibitInfo entityInhibitInfo = new Infos.EntityInhibitInfo();
                    Infos.EntityInhibitAttributes entityInhibitAttributes = new Infos.EntityInhibitAttributes();
                    List<Infos.EntityIdentifier> entitys = new ArrayList<>();
                    for (Constrain.EntityIdentifier entityIdentifier : inhibitRecord.getEntities()) {
                        Infos.EntityIdentifier entity = new Infos.EntityIdentifier();
                        entity.setObjectID(new ObjectIdentifier(entityIdentifier.getObjectId()));
                        entity.setClassName(entityIdentifier.getClassName());
                        entity.setAttribution(entityIdentifier.getAttrib());

                        entitys.add(entity);
                    }
                    entityInhibitAttributes.setEntities(entitys);
                    entityInhibitAttributes.setSubLotTypes(inhibitRecord.getSubLotTypes());
                    entityInhibitAttributes.setStartTimeStamp(inhibitRecord.getStartTimeStamp().toString());
                    entityInhibitAttributes.setEndTimeStamp(inhibitRecord.getEndTimeStamp().toString());
                    entityInhibitAttributes.setClaimedTimeStamp(inhibitRecord.getChangedTimeStamp().toString());
                    entityInhibitAttributes.setReasonCode(inhibitRecord.getReasonCode().getValue());
                    entityInhibitAttributes.setOwnerID(inhibitRecord.getOwner());
                    entityInhibitAttributes.setMemo(inhibitRecord.getClaimMemo());
                    entityInhibitInfo.setEntityInhibitAttributes(entityInhibitAttributes);
                    entityInhibitInfo.setEntityInhibitID(new ObjectIdentifier(inhibitRecord.getId(),
                            inhibitRecord.getReferenceKey()));
                    entityInhibitInfos.add(entityInhibitInfo);
                }
                return entityInhibitInfos;
            } else {
                return inputs.getEntityInhibitInfoSeq();
            }
        } else {
            return inputs.getEntityInhibitInfoSeq();
        }
    }

    @Override
    public String makeInhibitListFromEntityInhibits(List<CimRestriction> entityInhibitList) {
        int size = CimArrayUtils.getSize(entityInhibitList);
        StringJoiner resultSj = new StringJoiner(",");
        for (int i = 0; i < size && i < 10; i++) {
            String result = "";
            CimRestriction entityInhibit = entityInhibitList.get(i);
            if (entityInhibit == null) {
                continue;
            }
            Constrain.EntityInhibitRecord entityInhibitRecord = entityInhibit.getInhibitRecord();
            int entitySize = CimArrayUtils.getSize(entityInhibitRecord.getEntities());
            for (int j = 0; j < entitySize; j++) {
                Constrain.EntityIdentifier entities = entityInhibitRecord.getEntities().get(j);
                String entityClassName = entities.getClassName();
                if (entityClassName.equalsIgnoreCase(BizConstant.SP_INHIBITCLASSID_ROUTE)){
                    entityClassName = "Main PF";
                }
                result = String.format("%s%s:%s", result, entityClassName, entities.getObjectId());
                if (!CimStringUtils.isEmpty(entities.getAttrib())) {
                    result = String.format("%s.%s", result, entities.getAttrib());
                }
                if (j < entitySize - 1) {
                    result = result + "+";
                }
            }
            result = result + "+" + entityInhibit.getIdentifier();
            resultSj.add(result);
        }
        return resultSj.toString();
    }

    @Override
    public void constraintRequestForMultiFab(
            Infos.ObjCommon strObjCommonIn,
            List<Infos.EntityInhibitAttributesWithFabInfo> strEntityInhibitAttributesWithFabInfoSequence,
            String claimMemo) {

        log.info("mfgRestrictRequestForMultiFab_101");

        String currentFabID = StandardProperties.OM_SITE_ID.getValue();
        log.info("" + "currentFabID" + currentFabID);

        int reqLen = CimArrayUtils.getSize(strEntityInhibitAttributesWithFabInfoSequence);
        log.info("" + "request count for other fab" + reqLen);

        for (int i = 0; i < reqLen; i++) {
            log.info("" + "requested fabID" + strEntityInhibitAttributesWithFabInfoSequence.get(i).getFabID());
            /* TODO: NOTIMPL
            EntityInhibitXmlCreateOut strEntityInhibitXmlCreateOut;

            EntityInhibitXmlCreateIn_101  strEntityInhibitXmlCreateIn;
            strEntityInhibitXmlCreateIn.setStrEntityInhibitionWithFabInfo ( strEntityInhibitAttributesWithFabInfoSequence.get(i));
            strEntityInhibitXmlCreateIn.setClaimMemo ( "");

            rc = entityInhibitXmlCreate_101( strEntityInhibitXmlCreateOut, strObjCommonIn,
                    strEntityInhibitXmlCreateIn );
            char sarEvent.get(4001);

            sarEvent.get(0) = '\0';

            snprintf( sarEvent, sizeof(sarEvent), "|1|%s|%s|%s|%s|%s|%s",
                    (String)SP_INTERFAB_SAR_EVENT_COMPONENTNAME,
                    (String)SP_INTERFAB_SAR_EVENT_ACTION_START,
                    (String)currentFabID,
                    (String)strEntityInhibitAttributesWithFabInfoSequence.get(i).getFabID(),
                    (String)SP_INTERFAB_ACTIONTX_TXENTITYINHIBITREQ,
                    (String)strEntityInhibitXmlCreateOut.getXml() );

            SarInterFabQueuePutDROut strSarInterFabQueuePutDROut;
            SarInterFabQueuePutDRIn  strSarInterFabQueuePutDRIn;
            strSarInterFabQueuePutDRIn.setSarEvent ( sarEvent);

            rc = sarInterFabQueuePutDR( strSarInterFabQueuePutDROut, strObjCommonIn,
                    strSarInterFabQueuePutDRIn );
            if( rc != RcOk ){
                log.info(""+ "sarInterFabQueuePutDR() != RcOk"+ rc);
                strMfgRestrictRequestForMultiFabOut.setStrResult ( strSarInterFabQueuePutDROut.getStrResult());
                return rc;
            }*/
        }

        log.info("mfgRestrictRequestForMultiFab_101");
    }

    @Override
    public List<Infos.EntityInhibitInfo> constraintCheckForLotDR(Infos.ObjCommon objCommon,
                                                                 ObjectIdentifier lotID, ObjectIdentifier eqpID) {

        List<Infos.EntityInhibitInfo> result = new ArrayList<>();
        //-----------------------------------------------------
        // Step 1 - Get entityInhibit record for lot
        //-----------------------------------------------------
        if (!ObjectIdentifier.isEmpty(lotID)) {
            List<Infos.EntityIdentifier> objLotEntityIDListOut = lotMethod.lotEntityIDListGetDR(objCommon, lotID, eqpID);

            //-----------------------------------------------------
            // Stet 2 - Get lot SubLotType.
            //-----------------------------------------------------
            CimLotDO example = new CimLotDO();
            example.setLotID(ObjectIdentifier.fetchValue(lotID));
            CimLotDO lot = cimJpaRepository.findOne(Example.of(example)).orElse(null);
            Validations.check(lot == null, retCodeConfig.getNotFoundLot());

            //-----------------------------------------------------
            // Stet 3 - Get entityInhibit record
            //-----------------------------------------------------
            List<Constrain.EntityIdentifier> entityIdentifiers = new ArrayList<>();
            int entityIdListCount = CimArrayUtils.getSize(objLotEntityIDListOut);
            for (int i = 0; i < entityIdListCount; i++) {
                Constrain.EntityIdentifier entityIdentifier = new Constrain.EntityIdentifier();
                Infos.EntityIdentifier outEntityIdentifier = objLotEntityIDListOut.get(i);
                entityIdentifier.setObjectId(ObjectIdentifier.fetchValue(outEntityIdentifier.getObjectID()));
                entityIdentifier.setClassName(outEntityIdentifier.getClassName());
                entityIdentifier.setAttrib(outEntityIdentifier.getAttribution());
                entityIdentifiers.add(entityIdentifier);
            }

            List<String> subLotSeq = new ArrayList<>();
            subLotSeq.add(lot.getSubLotType());
            List<Constrain.EntityInhibitRecord> entityInhibitRecords = restrictionManager
                    .getEntityInhibitRecordsFor(entityIdentifiers, subLotSeq);
            int inhibitLen = CimArrayUtils.getSize(entityInhibitRecords);
            if (inhibitLen > 0) {
                List<Infos.EntityInhibitInfo> entityInhibitInfos = new ArrayList<>();
                this.setEntityInhibitRecordsToEntityInhibitInfos(entityInhibitInfos, entityInhibitRecords);
                List<Infos.EntityInhibitInfo> objEntityInhibitFilterExceptionLotOut = this
                        .constraintFilterExceptionLot(objCommon, lotID, entityInhibitInfos);

                this.setEntityInhibitInfosToEntityInhibitRecords(entityInhibitRecords,
                        objEntityInhibitFilterExceptionLotOut);
                //this.setEntityInhibitRecordsToEntityInhibitInfos(objEntityInhibitFilterExceptionLotOut, entityInhibitRecords);
                inhibitLen = CimArrayUtils.getSize(entityInhibitRecords);
            }

            for (int i = 0; i < inhibitLen; i++) {
                Constrain.EntityInhibitRecord entityInhibitRecord = entityInhibitRecords.get(i);
                Infos.EntityInhibitInfo entityInhibitInfo = new Infos.EntityInhibitInfo();
                result.add(i, entityInhibitInfo);
                Infos.EntityInhibitAttributes entityInhibitAttributes = new Infos.EntityInhibitAttributes();
                entityInhibitInfo.setEntityInhibitAttributes(entityInhibitAttributes);
                List<Infos.EntityIdentifier> outEntityList = new ArrayList<>();

                int entitiesCount = CimArrayUtils.getSize(entityInhibitRecord.getEntities());
                for (int j = 0; j < entitiesCount; j++) {
                    Infos.EntityIdentifier outEntityIdentifier = new Infos.EntityIdentifier();
                    Constrain.EntityIdentifier entityIdentifier = entityInhibitRecord.getEntities().get(j);
                    outEntityIdentifier.setObjectID(new ObjectIdentifier(entityIdentifier.getObjectId()));
                    outEntityIdentifier.setClassName(entityIdentifier.getClassName());
                    outEntityIdentifier.setAttribution(entityIdentifier.getAttrib());
                    outEntityList.add(outEntityIdentifier);
                }
                entityInhibitAttributes.setEntities(outEntityList);
                entityInhibitInfo.setEntityInhibitID(ObjectIdentifier.build(
                        entityInhibitRecord.getId(), entityInhibitRecord.getReferenceKey()));
                entityInhibitInfo.setStringifiedObjectReference(entityInhibitRecord.getReferenceKey());
                entityInhibitAttributes.setSubLotTypes(entityInhibitRecord.getSubLotTypes());
                String startTimeStamp = (entityInhibitRecord.getStartTimeStamp() != null) ?
                        entityInhibitRecord.getStartTimeStamp().toString() : BizConstant.EMPTY;
                entityInhibitAttributes.setStartTimeStamp(startTimeStamp);

                String endTimeStamp = (entityInhibitRecord.getEndTimeStamp() != null) ?
                        entityInhibitRecord.getEndTimeStamp().toString() : BizConstant.EMPTY;
                entityInhibitAttributes.setEndTimeStamp(endTimeStamp);

                String changedTimeStamp = (entityInhibitRecord.getChangedTimeStamp() != null) ?
                        entityInhibitRecord.getChangedTimeStamp().toString() : BizConstant.EMPTY;
                entityInhibitAttributes.setClaimedTimeStamp(changedTimeStamp);

                String codeID = (entityInhibitRecord.getReasonCode() != null) ?
                        entityInhibitRecord.getReasonCode().getValue() : BizConstant.EMPTY;
                entityInhibitAttributes.setReasonCode(codeID);
                entityInhibitAttributes.setOwnerID(entityInhibitRecord.getOwner());
                entityInhibitAttributes.setMemo(entityInhibitRecord.getClaimMemo());

                String codeCategory = BizConstant.SP_REASONCAT_ENTITYINHIBIT;
                CimCodeDO code = cimJpaRepository
                        .queryOne("SELECT * FROM OMCODE WHERE CODETYPE_ID = ?1 AND CODE_ID = ?2",
                                CimCodeDO.class, codeCategory, codeID);
                if (code != null) {
                    entityInhibitAttributes.setReasonDesc(code.getDescription());
                }
            }
        }
        return result;
    }

    @Override
    public List<Infos.EntityInhibitInfo> constraintFilterExceptionLot(Infos.ObjCommon objCommon,
                                                                      ObjectIdentifier lotID, List<Infos.EntityInhibitInfo> entityInhibitInfos) {

        List<Infos.EntityInhibitInfo> entityInhibitInfoList = new ArrayList<>();
        //step 1 - Set input parameters into local variable
        int numOfInhibits = CimArrayUtils.getSize(entityInhibitInfos);
        if (0 == numOfInhibits) {
            //---------------------------------------------------------------------------
            // By following condition for update usedFlag
            //  Called by TxMoveInCancelForIBReq or TxMoveInCancelReq
            //---------------------------------------------------------------------------
            if ("OEQPW009".equals(objCommon.getTransactionID())
                    || "OEQPW010".equals(objCommon.getTransactionID())
                    || "OEQPW012".equals(objCommon.getTransactionID())
                    || "OEQPW024".equals(objCommon.getTransactionID())) {

                List<CimRestriction> aEntityInhibitSeq = restrictionManager.getEntityInhibitsWithExceptionLotByLot(lotID);

                int entLen = CimArrayUtils.getSize(aEntityInhibitSeq);
                for (int i = 0; i < entLen; i++) {
                    Constrain.ExceptionLotRecord exceptionLotRecordRetCode = aEntityInhibitSeq.get(i)
                            .getExceptionLotRecord(lotID);

                    Validations.check(null == exceptionLotRecordRetCode, retCodeConfig
                            .getNotFoundEntityInhibitExceptLot());

                    if (exceptionLotRecordRetCode.getUsedFlag()) {
                        exceptionLotRecordRetCode.setUsedFlag(false);
                        aEntityInhibitSeq.get(i).setExceptionLotRecord(lotID, exceptionLotRecordRetCode);
                    }
                }

            }
        }
        for (int i = 0; i < numOfInhibits; i++) {
            CimRestriction anEntityInhibit = baseCoreFactory.getBO(CimRestriction.class,
                    entityInhibitInfos.get(i).getEntityInhibitID());
            Validations.check(null == anEntityInhibit, retCodeConfig.getNotFoundEntityInhibit());

            //Get Entity Inhibit Exception Lot Info
            Constrain.ExceptionLotRecord anExceptionLotRecord = anEntityInhibit.getExceptionLotRecord(lotID);
            if (null == anExceptionLotRecord) {
                entityInhibitInfoList.add(entityInhibitInfos.get(i));
                continue;
            }
            //----------------------------------------------------------------
            // By following condition for update usedFlag
            //  1. Called by TxMoveInForIBReq or TxMoveInReq
            //  2. Exception Lot is single trigger
            //----------------------------------------------------------------
            if ("OEQPW005".equals(objCommon.getTransactionID())
                    || "OEQPW004".equals(objCommon.getTransactionID())) {
                anExceptionLotRecord.setUsedFlag(true);
                anEntityInhibit.setExceptionLotRecord(lotID, anExceptionLotRecord);
            }
        }
        return entityInhibitInfoList;

    }

    @Override
    public List<Infos.EntityInhibitInfo> constraintCheckForLot(Infos.ObjCommon objCommon,
                                                               ObjectIdentifier lotID, ObjectIdentifier equipmentID) {
        List<Infos.EntityInhibitInfo> entityInhibitInfosOut = new ArrayList<>();

        if (ObjectIdentifier.isNotEmptyWithValue(lotID)) {
            CimLot aLot = baseCoreFactory.getBO(CimLot.class, lotID);
            CimMachine aMachine = null;
            if (ObjectIdentifier.isNotEmptyWithValue(equipmentID)) {
                aMachine = baseCoreFactory.getBO(CimMachine.class, equipmentID);
            }

            List<Constrain.EntityInhibitRecord> aRecordSeq = restrictionManager
                    .allEntityInhibitRecordsForLot(aLot, aMachine);
            int numOfInhibits = 0;
            if (aRecordSeq != null) {
                numOfInhibits = CimArrayUtils.getSize(aRecordSeq);
            }

            if (numOfInhibits > 0) {
                List<Infos.EntityInhibitInfo> entityInhibitInfoList = new ArrayList<>();
                setEntityInhibitRecordsToEntityInhibitInfos(entityInhibitInfoList, aRecordSeq);
                List<Infos.EntityInhibitInfo> effectiveForLotRetCode = this.constraintEffectiveForLotGetDR(objCommon,
                        entityInhibitInfoList, lotID);
                setEntityInhibitInfosToEntityInhibitRecords(aRecordSeq, effectiveForLotRetCode);

                numOfInhibits = CimArrayUtils.getSize(aRecordSeq);
                log.info("number of Inhibits [{}] filtered exception", numOfInhibits);
            }

            setEntityInhibitRecordsToEntityInhibitInfos(entityInhibitInfosOut, aRecordSeq);
        }

        return entityInhibitInfosOut;
    }

    @Override
    public void setEntityInhibitRecordsToEntityInhibitInfos(List<Infos.EntityInhibitInfo> dest,
                                                            List<Constrain.EntityInhibitRecord> source) {
        source.forEach(t -> {
            Infos.EntityInhibitInfo entityInhibitInfo = new Infos.EntityInhibitInfo();
            Infos.EntityInhibitAttributes entityInhibitAttributes = new Infos.EntityInhibitAttributes();
            List<Infos.EntityIdentifier> entities = new ArrayList<>();
            entityInhibitAttributes.setEntities(entities);
            entityInhibitInfo.setEntityInhibitAttributes(entityInhibitAttributes);

            int numberOfEntities = CimArrayUtils.getSize(t.getEntities());
            if (numberOfEntities > 0) {
                t.getEntities().forEach(k -> {
                    Infos.EntityIdentifier entity = new Infos.EntityIdentifier();
                    entity.setObjectID(new ObjectIdentifier(k.getObjectId()));
                    entity.setClassName(k.getClassName());
                    entity.setAttribution(k.getAttrib());
                    entities.add(entity);
                });
            }
            ObjectIdentifier entityInhibitID = new ObjectIdentifier(t.getId(), t.getReferenceKey());
            entityInhibitInfo.setEntityInhibitID(entityInhibitID);
            entityInhibitInfo.setStringifiedObjectReference(t.getReferenceKey());
            entityInhibitAttributes.setSubLotTypes(t.getSubLotTypes());
            String startTime = (t.getStartTimeStamp() == null) ? BizConstant.EMPTY : t.getStartTimeStamp().toString();
            entityInhibitAttributes.setStartTimeStamp(startTime);
            String endTime = (t.getEndTimeStamp() == null) ? BizConstant.EMPTY : t.getEndTimeStamp().toString();
            entityInhibitAttributes.setEndTimeStamp(endTime);
            String claimedTime = (t.getChangedTimeStamp() == null) ? BizConstant.EMPTY : t.getChangedTimeStamp().toString();
            entityInhibitAttributes.setClaimedTimeStamp(claimedTime);
            String reasonCode = (t.getReasonCode() != null) ? t.getReasonCode().getValue() : BizConstant.EMPTY;
            entityInhibitAttributes.setReasonCode(reasonCode);
            entityInhibitAttributes.setOwnerID(t.getOwner());
            entityInhibitAttributes.setMemo(t.getClaimMemo());
            List<CimCodeDO> codes = cimJpaRepository.query("SELECT * FROM OMCODE WHERE CODE_ID = ?1",
                    CimCodeDO.class, ObjectIdentifier.fetchValue(t.getReasonCode()));
            if (codes != null && codes.get(0) != null) {
                entityInhibitAttributes.setReasonDesc(codes.get(0).getDescription());
            }

            dest.add(entityInhibitInfo);
        });

    }

    @Override
    public void setEntityInhibitInfosToEntityInhibitRecords(
            List<Constrain.EntityInhibitRecord> dest, List<Infos.EntityInhibitInfo> source) {

        dest.clear();
        //dest = new ArrayList<>();
        for (Infos.EntityInhibitInfo t : source) {
            Constrain.EntityInhibitRecord entityInhibitRecord = new Constrain.EntityInhibitRecord();
            List<Constrain.EntityIdentifier> entities = new ArrayList<>();
            entityInhibitRecord.setEntities(entities);

            int numberOfEntities = 0;
            if (t.getEntityInhibitAttributes() != null) {
                numberOfEntities = CimArrayUtils.getSize(t.getEntityInhibitAttributes().getEntities());
            }

            if (numberOfEntities > 0) {
                t.getEntityInhibitAttributes().getEntities().forEach(k -> {
                    Constrain.EntityIdentifier entity = new Constrain.EntityIdentifier();
                    entity.setObjectId(ObjectIdentifier.fetchValue(k.getObjectID()));
                    entity.setClassName(k.getClassName());
                    entity.setAttrib(k.getAttribution());
                    entities.add(entity);
                });
            }

            entityInhibitRecord.setId(t.getEntityInhibitID().getValue());
            entityInhibitRecord.setReferenceKey(t.getEntityInhibitID().getReferenceKey());
            if (t.getEntityInhibitAttributes() != null) {
                entityInhibitRecord.setSubLotTypes(t.getEntityInhibitAttributes().getSubLotTypes());
                String startTime = (t.getEntityInhibitAttributes().getStartTimeStamp() == null)
                        ? BizConstant.EMPTY : t.getEntityInhibitAttributes().getStartTimeStamp();
                entityInhibitRecord.setStartTimeStamp(Timestamp.valueOf(startTime));
                String endTime = (t.getEntityInhibitAttributes().getEndTimeStamp() == null)
                        ? BizConstant.EMPTY : t.getEntityInhibitAttributes().getEndTimeStamp();
                entityInhibitRecord.setEndTimeStamp(Timestamp.valueOf(endTime));
                String claimedTime = (t.getEntityInhibitAttributes().getClaimedTimeStamp() == null)
                        ? BizConstant.EMPTY : t.getEntityInhibitAttributes().getClaimedTimeStamp();
                entityInhibitRecord.setChangedTimeStamp(Timestamp.valueOf(claimedTime));
                String reasonCode = t.getEntityInhibitAttributes().getReasonCode();
                entityInhibitRecord.setReasonCode(new ObjectIdentifier(reasonCode));
                entityInhibitRecord.setOwner(t.getEntityInhibitAttributes().getOwnerID());
                entityInhibitRecord.setClaimMemo(t.getEntityInhibitAttributes().getMemo());
                List<CimCodeDO> codes = cimJpaRepository.query("SELECT * FROM OMCODE WHERE CODE_ID = ?1",
                        CimCodeDO.class, t.getEntityInhibitAttributes().getReasonCode());
                if (codes != null && codes.get(0) != null) {
                    entityInhibitRecord.setDescription(codes.get(0).getDescription());
                }
            }

            dest.add(entityInhibitRecord);
        }
    }

    @Override
    public List<Infos.RecipeTime> recipeTimeLimitListQuery(Params.RecipeTimeInqParams recipeTimeInqParams) {
        String recipeID = recipeTimeInqParams.getRecipeID();
        List<Infos.RecipeTime> soitecRecipeTimes = new ArrayList<>();
        if (CimStringUtils.isNotEmpty(recipeID)) {
            CimEqpRecipeTimeDO example = new CimEqpRecipeTimeDO();
            example.setRecipeID(recipeID);
            CimEqpRecipeTimeDO query = cimJpaRepository.findOne(Example.of(example)).orElse(null);
            if (CimObjectUtils.isEmpty(query)) {
                return null;
            }
            Infos.RecipeTime soitecRecipeTime = new Infos.RecipeTime();
            soitecRecipeTimes.add(soitecRecipeTime);
            soitecRecipeTime.setRecipeID(recipeID);
            soitecRecipeTime.setTime(query.getExpireTime());
            soitecRecipeTime.setLastUseTime(query.getLastUseTime());
        } else {
            List<CimEqpRecipeTimeDO> cimEqpRecipeTimeDOS = cimJpaRepository.findAll(CimEqpRecipeTimeDO.class);
            if (CimObjectUtils.isEmpty(cimEqpRecipeTimeDOS)) {
                return null;
            }
            for (CimEqpRecipeTimeDO cimEqpRecipeTimeDO : cimEqpRecipeTimeDOS) {
                Infos.RecipeTime recipeTime = new Infos.RecipeTime();
                soitecRecipeTimes.add(recipeTime);
                recipeTime.setRecipeID(cimEqpRecipeTimeDO.getRecipeID());
                recipeTime.setTime(cimEqpRecipeTimeDO.getExpireTime());
                recipeTime.setLastUseTime(cimEqpRecipeTimeDO.getLastUseTime());
            }
        }
        return soitecRecipeTimes;
    }

    @Override
    public void recipeTimeLimitSet(Params.RecipeTimeSetParams params) {
        String recipeID = params.getRecipeID();
        CimEqpRecipeTimeDO example = new CimEqpRecipeTimeDO();
        example.setRecipeID(recipeID);
        CimEqpRecipeTimeDO query = cimJpaRepository.findOne(Example.of(example)).orElse(null);
        if (CimObjectUtils.isEmpty(query)) {
            example.setExpireTime(params.getTime());
            example.setSetTime(CimDateUtils.getCurrentTimeStamp());
            cimJpaRepository.save(example);
        } else {
            query.setExpireTime(params.getTime());
            cimJpaRepository.save(query);
        }
    }

    @Override
    public void recipeTimeLimitDelete(Params.RecipeTimeCancelParams params) {
        String recipeID = params.getRecipeID();
        CimEqpRecipeTimeDO example = new CimEqpRecipeTimeDO();
        example.setRecipeID(recipeID);
        CimEqpRecipeTimeDO query = cimJpaRepository.findOne(Example.of(example)).orElse(null);
        if (CimObjectUtils.isEmpty(query)) {
            throw new ServiceException(retCodeConfig.getNotFoundMachineRecipe());
        } else {
            cimJpaRepository.delete(query);
        }

    }

    @Override
    public void recipeTimeUseSet(Params.RecipeUseSetParams params) {
        List<Infos.TimeRecipeUse> soitecRecipeUseSetList = params.getTimeRecipeUseList();
        for (Infos.TimeRecipeUse timeRecipeUse : soitecRecipeUseSetList) {
            String recipeID = timeRecipeUse.getRecipeID();
            CimEqpRecipeTimeDO example = new CimEqpRecipeTimeDO();
            example.setRecipeID(recipeID);
            CimEqpRecipeTimeDO query = cimJpaRepository.findOne(Example.of(example)).orElse(null);
            if (!CimObjectUtils.isEmpty(query)) {
                query.setLastUseTime(timeRecipeUse.getLastUseTime());
                cimJpaRepository.save(query);
            }
        }
    }

    @Override
    public void recipeTimeUseCheck(Infos.ObjCommon objCommon) {
        List<CimEqpRecipeTimeDO> cimEqpRecipeTimeDOS = cimJpaRepository.findAll(CimEqpRecipeTimeDO.class);
        for (CimEqpRecipeTimeDO recipeTimeDO : cimEqpRecipeTimeDOS) {
            try {
                Timestamp currentTimeStamp = CimDateUtils.getCurrentTimeStamp();
                long currentTime = currentTimeStamp.getTime();
                String recipeID = recipeTimeDO.getRecipeID();
                Timestamp lastUseTime = recipeTimeDO.getLastUseTime();
                Integer expireTime = recipeTimeDO.getExpireTime();
                if (expireTime == 0){
                    continue;
                }
                Timestamp setTime = recipeTimeDO.getSetTime();
                long setTime1 = setTime.getTime();
                int hours = 0;
                if (CimObjectUtils.isEmpty(lastUseTime)) {
                    //use set time to judge
                    hours = (int) ((currentTime - setTime1) / (1000 * 60 * 60));
                } else {
                    long useTime = lastUseTime.getTime();
                    //use used time to judge
                    hours = (int) ((currentTime - useTime) / (1000 * 60 * 60));
                }
                if (hours > expireTime) {
                    //check if the recipe is inhibitied
                    Params.MfgRestrictListInqParams MfgListInqParams = new Params.MfgRestrictListInqParams();
                    Infos.EntityInhibitDetailAttributes entityInhibitDetailAttributes = new Infos
                            .EntityInhibitDetailAttributes();

                    MfgListInqParams.setEntityInhibitDetailAttributes(entityInhibitDetailAttributes);
                    MfgListInqParams.setUser(objCommon.getUser());
                    entityInhibitDetailAttributes.setSubLotTypes(new ArrayList<>());
                    List<Infos.EntityIdentifier> entities = new ArrayList<>();
                    Infos.EntityIdentifier entityIdentifier = new Infos.EntityIdentifier();
                    entities.add(entityIdentifier);
                    entityIdentifier.setAttribution("");
                    entityIdentifier.setClassName(BizConstant.SP_INHIBITCLASSID_MACHINERECIPE);
                    entityIdentifier.setObjectID(ObjectIdentifier.buildWithValue(recipeID));
                    entityInhibitDetailAttributes.setEntities(entities);
                    List<Infos.ConstraintEqpDetailInfo> content = constraintAttributeListGetDR(MfgListInqParams,objCommon);
                    boolean lockFlag = false;
                    for (Infos.ConstraintEqpDetailInfo entityInhibitDetailInfo : content) {
                        String reasonCode = entityInhibitDetailInfo.getEntityInhibitDetailAttributes().getReasonCode();
                        if (CimStringUtils.equals(reasonCode, BizConstant.RECIPE_TIME_LOCK_REASON)) {
                            lockFlag = true;
                            break;
                        }
                    }
                    if (lockFlag) {
                        continue;
                    }
                    //mfg the recipe
                    Params.MfgRestrictReqParams attributes = getAttributes(objCommon, recipeID);
                    constraintRegistrationReq(objCommon, attributes.getEntityInhibitDetailAttributes(),
                            attributes.getClaimMemo());
                }
            }catch (ServiceException e){
                log.error(e.getReasonText());
            }
        }
    }

    @Override
    public List<Infos.ConstraintEqpDetailInfo> constraintListByEqp(
            Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, String functionRule, Boolean isSpecificTool) {

        List<Constrain.EntityInhibitRecord> cimRestrictions;
        if (BizConstant.FUNCTION_RULE_BLIST.equals(functionRule)) {
            cimRestrictions = restrictionManager.findBlacklist(ConstraintClassEnum.INHIBITCLASSID_EQUIPMENT.getValue(),
                    equipmentID, false, isSpecificTool);
        } else if (BizConstant.FUNCTION_RULE_WLIST.equals(functionRule)) {
            cimRestrictions = restrictionManager.findWhitelist(ConstraintClassEnum.INHIBITCLASSID_EQUIPMENT.getValue(),
                    equipmentID, false, isSpecificTool);
        } else {
            cimRestrictions = restrictionManager.allToolRestrictions(ConstraintClassEnum
                    .INHIBITCLASSID_EQUIPMENT.getValue(), equipmentID, false, isSpecificTool);
        }
        List<Infos.ConstraintEqpDetailInfo> constraintByEqpInfos = new ArrayList<>();
        for (Constrain.EntityInhibitRecord restriction : cimRestrictions) {
            Infos.ConstraintEqpDetailInfo constraintByEqpInfo = new Infos.ConstraintEqpDetailInfo();
            constraintByEqpInfo.setEntityInhibitID(new ObjectIdentifier(restriction.getId(),
                    restriction.getReferenceKey()));

            Infos.ConstraintDetailAttributes attributes = new Infos.ConstraintDetailAttributes();
            Timestamp currentTimeStamp = CimDateUtils.getCurrentTimeStamp();
            boolean activeFlag = false;
            if (currentTimeStamp.after(restriction.getStartTimeStamp())
                    && currentTimeStamp.before(restriction.getEndTimeStamp())) {
                activeFlag = true;
            }
            attributes.setStatus(activeFlag ? BizConstant.SP_MFGSTATE_ACTIVE : BizConstant.SP_MFGSTATE_INACTIVE);
            attributes.setStartTimeStamp(restriction.getStartTimeStamp().toString());
            attributes.setEndTimeStamp(restriction.getEndTimeStamp().toString());
            attributes.setClaimedTimeStamp(restriction.getChangedTimeStamp().toString());
            attributes.setFunctionRule(restriction.getFunctionRule());
            attributes.setMemo(restriction.getClaimMemo());
            attributes.setOwnerID(restriction.getOwner());
            List<String> subLotTypes = restriction.getSubLotTypes();
            if (CimArrayUtils.isNotEmpty(subLotTypes)){
                attributes.setSubLotTypes(subLotTypes);
            }else {
                List<String> sbList = new ArrayList<>();
                sbList.add("*");
                attributes.setSubLotTypes(sbList);
            }
            attributes.setEntities(combineValueWithCommas(restriction.getEntities()));
            attributes.setExceptionEntities(combineValueWithCommas(restriction.getExceptionEntities()));
            attributes.setReasonCode(restriction.getReasonCode().getValue());
            attributes.setSpecTool(restriction.getSpecificTool());
            constraintByEqpInfo.setEntityInhibitDetailAttributes(attributes);
            constraintByEqpInfos.add(constraintByEqpInfo);
        }
        return constraintByEqpInfos;
    }

    @Override
    public List<Infos.EntityInhibitDetailAttributes> convertMfgExcel(List<MfgInfoParams> mfgInfoParamsList) {
        List<Infos.EntityInhibitDetailAttributes> entityInhibitDetailAttributesList = new ArrayList<>();
        for (MfgInfoParams mfgInfo : mfgInfoParamsList){
            if (CimStringUtils.isEmpty(mfgInfo.getConstraintType())){
                continue;
            }
            Infos.EntityInhibitDetailAttributes attributes = new Infos.EntityInhibitDetailAttributes();
            entityInhibitDetailAttributesList.add(attributes);
            attributes.setStartTimeStamp(mfgInfo.getStartTime());
            attributes.setEndTimeStamp(mfgInfo.getEndTime());
            attributes.setClaimedTimeStamp(CimDateUtils.getCurrentTimeStamp().toString());
            attributes.setReasonCode(mfgInfo.getReasonCode());
            attributes.setOwnerID(ObjectIdentifier.buildWithValue(mfgInfo.getOwner()));
            attributes.setMemo(mfgInfo.getMemo());
            attributes.setFunctionRule(mfgInfo.getRuleFunction());
            String subLotType = mfgInfo.getSubLotType();
            if (CimStringUtils.isEmpty(subLotType)){
                attributes.setSubLotTypes(new ArrayList<>());
            }else {
                List<String> subLotTypes = Arrays.asList(subLotType.split(","));
                attributes.setSubLotTypes(subLotTypes);
            }
            String constraintType = mfgInfo.getConstraintType();
            if (CimStringUtils.equals(BizConstant.CONSTRAINT_TYPE_EQP,constraintType)){
                //tool constriant
                attributes.setSpecTool(true);
                //add exceptions
                List<Infos.EntityIdentifier> exceptionEntities = new ArrayList<>();
                String exLotID = mfgInfo.getExLotID();
                String exMainPF = mfgInfo.getExMainPF();
                String exProduct = mfgInfo.getExProduct();
                if (CimStringUtils.isNotEmpty(exLotID)){
                    Infos.EntityIdentifier entityIdentifier = new Infos.EntityIdentifier();
                    exceptionEntities.add(entityIdentifier);
                    entityIdentifier.setClassName(BizConstant.SP_INHIBITCLASSID_LOT);
                    entityIdentifier.setObjectID(ObjectIdentifier.buildWithValue(exLotID));
                }
                if (CimStringUtils.isNotEmpty(exMainPF)){
                    Infos.EntityIdentifier entityIdentifier = new Infos.EntityIdentifier();
                    exceptionEntities.add(entityIdentifier);
                    entityIdentifier.setClassName(BizConstant.SP_INHIBITCLASSID_ROUTE);
                    entityIdentifier.setObjectID(ObjectIdentifier.buildWithValue(exMainPF));
                }
                if (CimStringUtils.isNotEmpty(exProduct)){
                    Infos.EntityIdentifier entityIdentifier = new Infos.EntityIdentifier();
                    exceptionEntities.add(entityIdentifier);
                    entityIdentifier.setClassName(BizConstant.SP_INHIBITCLASSID_PRODUCT);
                    entityIdentifier.setObjectID(ObjectIdentifier.buildWithValue(exProduct));
                }
                attributes.setExceptionEntities(exceptionEntities);
                //add entities
                List<Infos.EntityIdentifier> entities = new ArrayList<>();
                attributes.setEntities(entities);
                if (CimStringUtils.isNotEmpty(mfgInfo.getEquipment())){
                    Infos.EntityIdentifier entityIdentifier = new Infos.EntityIdentifier();
                    entities.add(entityIdentifier);
                    if (CimStringUtils.isNotEmpty(mfgInfo.getChamber())){
                        entityIdentifier.setClassName(BizConstant.SP_INHIBITCLASSID_CHAMBER);
                        entityIdentifier.setObjectID(ObjectIdentifier.buildWithValue(mfgInfo.getEquipment()));
                        entityIdentifier.setAttribution(mfgInfo.getChamber());
                    }else {
                        entityIdentifier.setClassName(BizConstant.SP_INHIBITCLASSID_EQUIPMENT);
                        entityIdentifier.setObjectID(ObjectIdentifier.buildWithValue(mfgInfo.getEquipment()));
                    }
                }
                if (CimStringUtils.isNotEmpty(mfgInfo.getProduct())){
                    Infos.EntityIdentifier entityIdentifier = new Infos.EntityIdentifier();
                    entities.add(entityIdentifier);
                    entityIdentifier.setClassName(BizConstant.SP_INHIBITCLASSID_PRODUCT);
                    entityIdentifier.setObjectID(ObjectIdentifier.buildWithValue(mfgInfo.getProduct()));
                }
                if (CimStringUtils.isNotEmpty(mfgInfo.getRecipe())){
                    Infos.EntityIdentifier entityIdentifier = new Infos.EntityIdentifier();
                    entities.add(entityIdentifier);
                    entityIdentifier.setClassName(BizConstant.SP_INHIBITCLASSID_MACHINERECIPE);
                    entityIdentifier.setObjectID(ObjectIdentifier.buildWithValue(mfgInfo.getRecipe()));
                }
                if (CimStringUtils.isNotEmpty(mfgInfo.getMainPF())){
                    Infos.EntityIdentifier entityIdentifier = new Infos.EntityIdentifier();
                    entities.add(entityIdentifier);
                    if (CimStringUtils.isNotEmpty(mfgInfo.getOperation())){
                        entityIdentifier.setClassName(BizConstant.SP_INHIBITCLASSID_OPERATION);
                        entityIdentifier.setObjectID(ObjectIdentifier.buildWithValue(mfgInfo.getMainPF()));
                        entityIdentifier.setAttribution(mfgInfo.getOperation());
                    }else {
                        entityIdentifier.setClassName(BizConstant.SP_INHIBITCLASSID_ROUTE);
                        entityIdentifier.setObjectID(ObjectIdentifier.buildWithValue(mfgInfo.getMainPF()));
                    }
                }
                if (CimStringUtils.isNotEmpty(mfgInfo.getRoute())){
                    Infos.EntityIdentifier entityIdentifier = new Infos.EntityIdentifier();
                    entities.add(entityIdentifier);
                    entityIdentifier.setClassName(BizConstant.SP_INHIBITCLASSID_MODULEPD);
                    entityIdentifier.setObjectID(ObjectIdentifier.buildWithValue(mfgInfo.getRoute()));
                }
                if (CimStringUtils.isNotEmpty(mfgInfo.getStep())){
                    Infos.EntityIdentifier entityIdentifier = new Infos.EntityIdentifier();
                    entities.add(entityIdentifier);
                    entityIdentifier.setClassName(BizConstant.SP_INHIBITCLASSID_PROCESS);
                    entityIdentifier.setObjectID(ObjectIdentifier.buildWithValue(mfgInfo.getStep()));
                }
                if (CimStringUtils.isNotEmpty(mfgInfo.getReticle())){
                    Infos.EntityIdentifier entityIdentifier = new Infos.EntityIdentifier();
                    entities.add(entityIdentifier);
                    entityIdentifier.setClassName(BizConstant.SP_INHIBITCLASSID_RETICLE);
                    entityIdentifier.setObjectID(ObjectIdentifier.buildWithValue(mfgInfo.getReticle()));
                }
                if (CimStringUtils.isNotEmpty(mfgInfo.getReticleGrp())){
                    Infos.EntityIdentifier entityIdentifier = new Infos.EntityIdentifier();
                    entities.add(entityIdentifier);
                    entityIdentifier.setClassName(BizConstant.SP_INHIBITCLASSID_RETICLEGROUP);
                    entityIdentifier.setObjectID(ObjectIdentifier.buildWithValue(mfgInfo.getReticleGrp()));
                }
                if (CimStringUtils.isNotEmpty(mfgInfo.getLotID())){
                    Infos.EntityIdentifier entityIdentifier = new Infos.EntityIdentifier();
                    entities.add(entityIdentifier);
                    entityIdentifier.setClassName(BizConstant.SP_INHIBITCLASSID_LOT);
                    entityIdentifier.setObjectID(ObjectIdentifier.buildWithValue(mfgInfo.getLotID()));
                }

            }else if (CimStringUtils.equals(BizConstant.CONSTRAINT_TYPE_PRODUCT,constraintType)){
                attributes.setSpecTool(false);
                List<Infos.EntityIdentifier> entities = new ArrayList<>();
                attributes.setEntities(entities);
                Infos.EntityIdentifier entityIdentifier = new Infos.EntityIdentifier();
                entities.add(entityIdentifier);
                entityIdentifier.setClassName(BizConstant.SP_INHIBITCLASSID_PRODUCT);
                entityIdentifier.setObjectID(ObjectIdentifier.buildWithValue(mfgInfo.getProduct()));
            }else if (CimStringUtils.equals(BizConstant.CONSTRAINT_TYPE_PF,constraintType)){
                attributes.setSpecTool(false);
                List<Infos.EntityIdentifier> entities = new ArrayList<>();
                attributes.setEntities(entities);
                Infos.EntityIdentifier entityIdentifier = new Infos.EntityIdentifier();
                entities.add(entityIdentifier);
                entityIdentifier.setClassName(BizConstant.SP_INHIBITCLASSID_ROUTE);
                entityIdentifier.setObjectID(ObjectIdentifier.buildWithValue(mfgInfo.getMainPF()));
            }else if (CimStringUtils.equals(BizConstant.CONSTRAINT_TYPE_ROUTE,constraintType)){
                attributes.setSpecTool(false);
                List<Infos.EntityIdentifier> entities = new ArrayList<>();
                attributes.setEntities(entities);
                Infos.EntityIdentifier entityIdentifier = new Infos.EntityIdentifier();
                entities.add(entityIdentifier);
                entityIdentifier.setClassName(BizConstant.SP_INHIBITCLASSID_MODULEPD);
                entityIdentifier.setObjectID(ObjectIdentifier.buildWithValue(mfgInfo.getRoute()));
            }else if (CimStringUtils.equals(BizConstant.CONSTRAINT_TYPE_STEP,constraintType)){
                attributes.setSpecTool(false);
                List<Infos.EntityIdentifier> entities = new ArrayList<>();
                attributes.setEntities(entities);
                Infos.EntityIdentifier entityIdentifier = new Infos.EntityIdentifier();
                entities.add(entityIdentifier);
                entityIdentifier.setClassName(BizConstant.SP_INHIBITCLASSID_PROCESS);
                entityIdentifier.setObjectID(ObjectIdentifier.buildWithValue(mfgInfo.getStep()));
            }else if (CimStringUtils.equals(BizConstant.CONSTRAINT_TYPE_PFOPE,constraintType)){
                attributes.setSpecTool(false);
                List<Infos.EntityIdentifier> entities = new ArrayList<>();
                attributes.setEntities(entities);
                Infos.EntityIdentifier entityIdentifier = new Infos.EntityIdentifier();
                entities.add(entityIdentifier);
                entityIdentifier.setClassName(BizConstant.SP_INHIBITCLASSID_OPERATION);
                entityIdentifier.setObjectID(ObjectIdentifier.buildWithValue(mfgInfo.getMainPF()));
                entityIdentifier.setAttribution(mfgInfo.getOperation());
            }else if (CimStringUtils.equals(BizConstant.CONSTRAINT_TYPE_PFOPE_PRODUCT,constraintType)){
                attributes.setSpecTool(false);
                List<Infos.EntityIdentifier> entities = new ArrayList<>();
                attributes.setEntities(entities);
                Infos.EntityIdentifier entityIdentifier1 = new Infos.EntityIdentifier();
                entities.add(entityIdentifier1);
                entityIdentifier1.setClassName(BizConstant.SP_INHIBITCLASSID_PRODUCT);
                entityIdentifier1.setObjectID(ObjectIdentifier.buildWithValue(mfgInfo.getProduct()));
                Infos.EntityIdentifier entityIdentifier2 = new Infos.EntityIdentifier();
                entities.add(entityIdentifier2);
                entityIdentifier2.setClassName(BizConstant.SP_INHIBITCLASSID_OPERATION);
                entityIdentifier2.setObjectID(ObjectIdentifier.buildWithValue(mfgInfo.getMainPF()));
                entityIdentifier2.setAttribution(mfgInfo.getOperation());
            }else if (CimStringUtils.equals(BizConstant.CONSTRAINT_TYPE_RECIPE,constraintType)){
                attributes.setSpecTool(false);
                List<Infos.EntityIdentifier> entities = new ArrayList<>();
                attributes.setEntities(entities);
                Infos.EntityIdentifier entityIdentifier = new Infos.EntityIdentifier();
                entities.add(entityIdentifier);
                entityIdentifier.setClassName(BizConstant.SP_INHIBITCLASSID_MACHINERECIPE);
                entityIdentifier.setObjectID(ObjectIdentifier.buildWithValue(mfgInfo.getRecipe()));
            }else if (CimStringUtils.equals(BizConstant.CONSTRAINT_TYPE_RETICLE,constraintType)){
                attributes.setSpecTool(false);
                List<Infos.EntityIdentifier> entities = new ArrayList<>();
                attributes.setEntities(entities);
                Infos.EntityIdentifier entityIdentifier = new Infos.EntityIdentifier();
                entities.add(entityIdentifier);
                entityIdentifier.setClassName(BizConstant.SP_INHIBITCLASSID_RETICLE);
                entityIdentifier.setObjectID(ObjectIdentifier.buildWithValue(mfgInfo.getReticle()));
            }else if (CimStringUtils.equals(BizConstant.CONSTRAINT_TYPE_RETICLEGRP,constraintType)){
                attributes.setSpecTool(false);
                List<Infos.EntityIdentifier> entities = new ArrayList<>();
                attributes.setEntities(entities);
                Infos.EntityIdentifier entityIdentifier = new Infos.EntityIdentifier();
                entities.add(entityIdentifier);
                entityIdentifier.setClassName(BizConstant.SP_INHIBITCLASSID_RETICLEGROUP);
                entityIdentifier.setObjectID(ObjectIdentifier.buildWithValue(mfgInfo.getReticleGrp()));
            }
        }
        return entityInhibitDetailAttributesList;
    }

    @Override
    public Page<Infos.ConstraintHistoryDetailInfo> constrintHistoryGet(String userID, String functionRule,
                                                                       Boolean specificTool,
                                                                       SearchCondition searchCondition) {

        String sql = "SELECT ID," +
                     "RESTRICT_ID," +
                     "TASK_TYPE," +
                     "START_TIME," +
                     "END_TIME," +
                     "REASON_CODE," +
                     "REASON_DESC," +
                     "DESCRIPTION," +
                     "TRX_TIME," +
                     "TRX_WORK_DATE," +
                     "TRX_USER_ID," +
                     "TRX_MEMO," +
                     "STORE_TIME," +
                     "EVENT_CREATE_TIME," +
                     "RESTRICT_CONTEXT," +
                     "FUNC_RULE," +
                     "SPECIFIC_TOOL FROM OHRESTRICT WHERE 1=1";

        if (null != specificTool) {
            sql += " AND SPECIFIC_TOOL = '%s' ";
            if ( CimBooleanUtils.isTrue(specificTool)){
                sql = String.format(sql,1);
            } else {
                sql = String.format(sql,0);
            }
        }
        if (CimStringUtils.isNotEmpty(functionRule)) {
            sql += "AND FUNC_RULE = '%s'";
            sql = String.format(sql,functionRule);
        }
        // TODO: 2021/7/26 if need user information
//        if (CimStringUtils.isNotEmpty(userID)) {
//            sql += "AND TRX_USER_ID = '%s'";
//            sql = String.format(sql,userID);
//        }

        sql += " ORDER BY EVENT_CREATE_TIME DESC";
        Page<Object[]> cimEventEntityInhibitDOS = cimJpaRepository.query(sql, searchCondition);

        List<Infos.ConstraintHistoryDetailInfo> constraintHistoryDetailInfos = Optional
                .ofNullable(cimEventEntityInhibitDOS).map(Do -> Do
                .stream().map(detail -> {
                    Infos.ConstraintHistoryDetailInfo constraintHistoryDetailInfo = new Infos
                            .ConstraintHistoryDetailInfo();
                    List<Infos.EntityRecord> entityRecords = new ArrayList<>();
                    List<Infos.ExceptionEntityRecord> exceptionEntityRecords = new ArrayList<>();
                    constraintHistoryDetailInfo.setExceptionEntiyRecords(exceptionEntityRecords);
                    constraintHistoryDetailInfo.setEntityRecords(entityRecords);
                    String restrictID = CimObjectUtils.toString(detail[0]);
                    constraintHistoryDetailInfo.setId(restrictID);

                    String constraintID = CimObjectUtils.toString(detail[1]);
                    constraintHistoryDetailInfo.setConstriantID(constraintID);

                    // TODO: 2021/6/11 taskType:r->add  c->delete
                    //-----------change the taskType----
                    //  R->add C->delete M->edit
                    //----------------- end ------------
                    String historyCategory = CimObjectUtils.toString(detail[2]);
                    if (CimStringUtils.equals(BizConstant.TOOL_CONSTRAINT_REQ,historyCategory)) {
                        historyCategory = "Add";
                    } else if (CimStringUtils.equalsIgnoreCase(BizConstant.TOOL_CONSTRAINT_DELETE,historyCategory)
                            || CimStringUtils.equalsIgnoreCase(BizConstant.TOOL_CONSTRAINT_CANCEL,historyCategory)) {
                        historyCategory = "Delete";
                    } else if (CimStringUtils.equalsIgnoreCase(BizConstant.TOOL_CONSTRAINT_MODIFY
                            ,historyCategory)) {
                        historyCategory = "Edit";
                    }else if (CimStringUtils.equalsIgnoreCase(BizConstant.TOOL_CONSTRAINT_EDCFail
                            ,historyCategory)) {
                        historyCategory = "EDC Fail";
                    }
                    constraintHistoryDetailInfo.setHistoryCategory(historyCategory);

                    constraintHistoryDetailInfo.setStartTime(CimDateUtils.convertToSpecString((Timestamp) detail[3]));
                    constraintHistoryDetailInfo.setEndTime(CimDateUtils.convertToSpecString((Timestamp) detail[4]));
                    constraintHistoryDetailInfo.setResonCode(CimObjectUtils.toString(detail[5]));
                    constraintHistoryDetailInfo.setDescription(CimObjectUtils.toString(detail[7]));
                    constraintHistoryDetailInfo.setClaimTime(CimDateUtils.convertToSpecString((Timestamp) detail[8]));
                    constraintHistoryDetailInfo.setTrxWorkTime(CimDateUtils.convertToSpecString((Timestamp) detail[9]));
                    constraintHistoryDetailInfo.setUserID(new User(ObjectIdentifier.buildWithValue((String) detail[10])));
                    constraintHistoryDetailInfo.setMemo(CimObjectUtils.toString(detail[11]));
                    constraintHistoryDetailInfo.setStoreTime(CimDateUtils.convertToSpecString((Timestamp) detail[12]));
                    constraintHistoryDetailInfo.setReportTime(CimDateUtils.convertToSpecString((Timestamp) detail[13]));
                    constraintHistoryDetailInfo.setRestrictContext(CimObjectUtils.toString(detail[14]));
                    constraintHistoryDetailInfo.setType(CimObjectUtils.toString(detail[15]));
                    boolean toolFlag = CimBooleanUtils.isTrue((Number) detail[16]);
                    constraintHistoryDetailInfo.setSpecificTool(toolFlag);

                    // input entity information
                    String entityRecordSql = "SELECT ENTITY_TYPE FROM OHRESTRICT_ENTITY WHERE REFKEY = '%s'";
                    entityRecordSql = String.format(entityRecordSql, restrictID);
                    List<Object[]> recodrs = cimJpaRepository.query(entityRecordSql);
                    /**
                     *
                     * toolFlag:true ,for toolConstraint ->constraintType is Equipment Related
                     *          false,for generalConstraint ->constraintType is Main PF,
                     *          Product, Route, Step, Recipe,
                     *          Reticle, Reticle Group, Main PF Operation,
                     *          Main PF Operation and Product
                     */
//                    if (toolFlag) {
//                        Infos.EntityRecord entityRecord = new Infos.EntityRecord();
//                        entityRecord.setConstraintType("Equipment Related");
//                        entityRecords.add(entityRecord);
//                    } else {
//                        Optional.ofNullable(recodrs).ifPresent(r->r.forEach(re->{
//                            Infos.EntityRecord entityRecord = new Infos.EntityRecord();
//                            entityRecord.setConstraintType(CimObjectUtils.toString(re[0]));
//                            entityRecords.add(entityRecord);
//                        }));
//                    }
                    Optional.ofNullable(recodrs).ifPresent(r->r.forEach(re->{
                        Infos.EntityRecord entityRecord = new Infos.EntityRecord();
                        entityRecord.setConstraintType(CimObjectUtils.toString(re[0]));
                        entityRecords.add(entityRecord);
                    }));
                    return constraintHistoryDetailInfo;
                }).collect(Collectors.toList())).orElse(Collections.emptyList());
        return new PageImpl<>(constraintHistoryDetailInfos,cimEventEntityInhibitDOS.getPageable(),
                cimEventEntityInhibitDOS.getTotalElements());
    }

    /**
     * description: Concatenate the values ​​of the same ClassName in List<Infos.EntityIdentifier> with commas
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/12/12 1:37                     Nyx                Create
     *
     * @author Nyx
     * @date 2020/12/12 1:37
     * @param restrictionEntities -
     * @return java.util.List<com.fa.cim.dto.Infos.EntityIdentifier>
     */
    private List<Infos.EntityIdentifier> combineValueWithCommas(List<Constrain.EntityIdentifier> restrictionEntities) {
        //toMap(map的key, 将原来的对象作为map的value, 如果o1和o2的key相同就以逗号合并value值)
        return CimArrayUtils.isEmpty(restrictionEntities) ? Collections.emptyList() : new ArrayList<>(restrictionEntities.stream()
                .map(x -> new Infos.EntityIdentifier(x.getClassName(), new ObjectIdentifier(x.getObjectId()), x.getAttrib()))
                .collect(Collectors.toMap(Infos.EntityIdentifier::getClassName, x -> x, (o1, o2) -> {
                    String className = o1.getClassName();
                    //如果class为chamber,拼接Attribution, 其他className其他拼接objID
                    if (ConstraintClassEnum.INHIBITCLASSID_CHAMBER.getValue().equals(className)) {
                        o1.setAttribution(o1.getAttribution() + BizConstant.SEPARATOR_COMMA + o2.getAttribution());
                    } else {
                        o1.setObjectID(new ObjectIdentifier(ObjectIdentifier.fetchValue(o1.getObjectID()) + BizConstant.SEPARATOR_COMMA + ObjectIdentifier.fetchValue(o2.getObjectID())));
                    }
                    return o1;
                })).values());
    }

    private List<Infos.EntityInhibitExceptionLotInfo> exceptionLotInfoExchange(List<Constrain.ExceptionLotRecord> exceptionLotRecords){
        List<Infos.EntityInhibitExceptionLotInfo> entityInhibitExceptionLotInfos = new ArrayList<>();
        if (CimArrayUtils.isEmpty(exceptionLotRecords)){
            return entityInhibitExceptionLotInfos;
        }
        for (Constrain.ExceptionLotRecord exceptionLotRecord : exceptionLotRecords){
            Infos.EntityInhibitExceptionLotInfo entityInhibitExceptionLotInfo = new Infos.EntityInhibitExceptionLotInfo();
            entityInhibitExceptionLotInfos.add(entityInhibitExceptionLotInfo);
            entityInhibitExceptionLotInfo.setLotID(exceptionLotRecord.getLotID());
            entityInhibitExceptionLotInfo.setSingleTriggerFlag(exceptionLotRecord.getSingleTriggerFlag());
            entityInhibitExceptionLotInfo.setClaimTime(exceptionLotRecord.getClaimTimeStamp().toString());
            entityInhibitExceptionLotInfo.setClaimUserID(exceptionLotRecord.getClaimUserID());
            entityInhibitExceptionLotInfo.setUsedFlag(exceptionLotRecord.getUsedFlag());
            entityInhibitExceptionLotInfo.setClaimMemo(exceptionLotRecord.getClaimMemo());
        }
        return entityInhibitExceptionLotInfos;
    }

    private Params.MfgRestrictReqParams getAttributes(Infos.ObjCommon objCommon, String recipeID) {
        //get reason
        ObjectIdentifier userID = objCommon.getUser().getUserID();
        Params.MfgRestrictReqParams mfgRestrictReqParams = new Params.MfgRestrictReqParams();
        mfgRestrictReqParams.setUser(objCommon.getUser());
        List<Infos.ReasonCodeAttributes> reasonCodeAttributes = codeMethod.codeFillInTxPLQ010DR(objCommon, BizConstant.MFG_REASON);
        Infos.EntityInhibitDetailAttributes entityInhibitDetailAttributes = new Infos.EntityInhibitDetailAttributes();
        mfgRestrictReqParams.setEntityInhibitDetailAttributes(entityInhibitDetailAttributes);
        entityInhibitDetailAttributes.setEntityInhibitReasonDetailInfos(null);
        mfgRestrictReqParams.setClaimMemo("Recipe Idle Constraint");
        for (Infos.ReasonCodeAttributes reasonCodeAttribute : reasonCodeAttributes) {
            if (CimStringUtils.equals(reasonCodeAttribute.getReasonCodeID().getValue(), BizConstant.RECIPE_TIME_LOCK_REASON)) {
                entityInhibitDetailAttributes.setReasonCode(reasonCodeAttribute.getReasonCodeID().getValue());
                entityInhibitDetailAttributes.setReasonDesc(reasonCodeAttribute.getCodeDescription());
                break;
            }
        }
        entityInhibitDetailAttributes.setStartTimeStamp(CimDateUtils.getCurrentDateTimeWithDefault());
        entityInhibitDetailAttributes.setSubLotTypes(new ArrayList<>());
        entityInhibitDetailAttributes.setOwnerID(userID);
        List<Infos.EntityIdentifier> entityIdentifiers = new ArrayList<>();
        Infos.EntityIdentifier entityIdentifier = new Infos.EntityIdentifier();
        entityIdentifiers.add(entityIdentifier);
        entityIdentifier.setClassName(BizConstant.SP_INHIBITCLASSID_MACHINERECIPE);
        entityIdentifier.setObjectID(ObjectIdentifier.buildWithValue(recipeID));
        entityIdentifier.setAttribution("");
        entityInhibitDetailAttributes.setEntities(entityIdentifiers);

        String endTime = BizConstant.SOITEC_MFG_ENDTIME;
        entityInhibitDetailAttributes.setEndTimeStamp(endTime);
        return mfgRestrictReqParams;
    }
}
