package com.fa.cim.method.impl;

import com.fa.cim.annotaion.OmMethod;
import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.OmCode;
import com.fa.cim.common.utils.*;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.config.RetCodeConfigEx;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Inputs;
import com.fa.cim.dto.Outputs;
import com.fa.cim.entity.runtime.area.CimAreaDO;
import com.fa.cim.entity.runtime.bank.CimBankDO;
import com.fa.cim.entity.runtime.cassette.CimCassetteDO;
import com.fa.cim.entity.runtime.customer.CimCustomerDO;
import com.fa.cim.entity.runtime.durable.CimDurableDO;
import com.fa.cim.entity.runtime.durablegroup.CimDurableGroupDO;
import com.fa.cim.entity.runtime.durablesubstate.CimDurableSubStateDO;
import com.fa.cim.entity.runtime.eqp.CimEquipmentDO;
import com.fa.cim.entity.runtime.lot.CimLotDO;
import com.fa.cim.entity.runtime.messagedefinition.CimMessageDefinitionDO;
import com.fa.cim.entity.runtime.mrecipe.CimMachineRecipeDO;
import com.fa.cim.entity.runtime.processdefinition.CimProcessDefinitionDO;
import com.fa.cim.entity.runtime.productgroup.CimProductGroupDO;
import com.fa.cim.entity.runtime.productspec.CimProductSpecificationDO;
import com.fa.cim.entity.runtime.reticlepod.CimReticlePodDO;
import com.fa.cim.entity.runtime.stocker.CimStockerDO;
import com.fa.cim.entity.runtime.technology.CimTechnologyDO;
import com.fa.cim.entitysuper.BaseEntity;
import com.fa.cim.jpa.CimJpaRepository;
import com.fa.cim.method.IEquipmentMethod;
import com.fa.cim.method.IObjectMethod;
import com.fa.cim.method.ISorterNewMethod;
import com.fa.cim.newcore.bo.BaseBO;
import com.fa.cim.newcore.bo.CimBO;
import com.fa.cim.newcore.bo.code.CimCategory;
import com.fa.cim.newcore.bo.code.CimE10State;
import com.fa.cim.newcore.bo.dispatch.CimDispatcher;
import com.fa.cim.newcore.bo.dispatch.CimFlowBatch;
import com.fa.cim.newcore.bo.durable.*;
import com.fa.cim.newcore.bo.factory.CimFactoryNote;
import com.fa.cim.newcore.bo.machine.*;
import com.fa.cim.newcore.bo.pd.CimProcessDefinition;
import com.fa.cim.newcore.bo.pd.CimProcessFlow;
import com.fa.cim.newcore.bo.pd.CimProcessFlowContext;
import com.fa.cim.newcore.bo.pd.ProcessDefinitionManager;
import com.fa.cim.newcore.bo.person.CimUserGroup;
import com.fa.cim.newcore.bo.planning.CimLotSchedule;
import com.fa.cim.newcore.bo.planning.CimProductRequest;
import com.fa.cim.newcore.bo.prodspec.CimCustomer;
import com.fa.cim.newcore.bo.product.CimControlJob;
import com.fa.cim.newcore.bo.product.CimLot;
import com.fa.cim.newcore.bo.product.CimWafer;
import com.fa.cim.newcore.bo.restrict.CimRestriction;
import com.fa.cim.newcore.dto.global.GlobalDTO;
import com.fa.cim.newcore.dto.restriction.Constrain;
import com.fa.cim.newcore.factory.BaseCoreFactory;
import com.fa.cim.newcore.impl.bo.env.StandardProperties;
import com.fa.cim.newcore.standard.drblmngm.Cassette;
import com.fa.cim.newcore.standard.mchnmngm.BufferResource;
import com.fa.cim.newcore.standard.mchnmngm.Machine;
import com.fa.cim.newcore.standard.mchnmngm.MachineResource;
import com.fa.cim.newcore.standard.mchnmngm.PortResource;
import com.fa.cim.newcore.standard.prcssdfn.ProcessDefinition;
import com.fa.cim.sorter.Info;
import com.fa.cim.sorter.Params;
import com.fa.cim.sorter.SorterType;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import javax.persistence.Table;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2018/9/25        ********             Bear               create file
 *
 * @author: Bear
 * @date: 2018/9/25 18:04
 * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@OmMethod
@Slf4j
public class ObjectMethod  implements IObjectMethod {

    @Autowired
    private RetCodeConfig retCodeConfig;

    @Autowired
    private RetCodeConfigEx retCodeConfigEx;

    @Autowired
    private CimJpaRepository cimJpaRepository;

    @Autowired
    private BaseCoreFactory baseCoreFactory;

    @Autowired
    @Qualifier("DurableManagerCore")
    private DurableManager durableManager;

    @Autowired
    private ISorterNewMethod sorterNewMethod;

    @Autowired
    private IEquipmentMethod equipmentMethod;

    @Autowired
    @Qualifier("ProcessDefinitionManagerCore")
    private ProcessDefinitionManager processDefinitionManager;


    private static String[][] classKeys = {
            {"PosArea",                          "BAY_ID", "", "", "", ""},
            {"PosAreaGroup",                     "AREAGRP_ID", "", "", "", ""},
            {"PosBank",                          "BANK_ID", "", "", "", ""},
            {"PosBinDefinition",                 "BINDEF_ID", "", "", "", ""},
            {"PosBinSpecification",              "BINSPEC_ID", "", "", "", ""},
            {"PosBOM",                           "BOM_ID", "", "", "", ""},
            {"PosBufferResource",                "BUFFRES_ID", "EQP_ID", "", "", ""},
            {"PosCalendarDate",                  "CALENDAR_DATE", "", "", "", ""},
            {"PosCassette",                      "CAST_ID", "", "", "", ""},
            {"PosCode",                          "CODE_ID", "CATEGORY_ID", "", "", ""},
            {"PosControlJob",                    "CTRLJOB_ID", "", "", "", ""},
            {"PosCustomer",                      "CUSTOMER_ID", "", "", "", ""},
            {"PosCustomerProduct",               "CUSTOMER_ID", "PRODSPEC_ID", "", "", ""},
            {"PosDataCollectionDefinition",      "DCDEF_ID", "", "", "", ""},
            {"PosDataCollectionSpecification",   "DCSPEC_ID", "", "", "", ""},
            {"PosDispatcher",                    "EQP_ID", "", "", "", ""},
            {"PosReticle",                       "DRBL_ID", "", "", "", ""},
            {"PosProcessDurableCapability",      "DRBLGRP_ID", "", "", "", ""},
            {"PosE10State",                      "E10STATE_ID", "", "", "", ""},
            {"PosEntityInhibit",                 "RESTRICT_ID", "", "", "", ""},
            {"PosMachine",                       "EQP_ID", "", "", "", ""},
            {"PosMachineContainer",              "EQPCTN_ID", "EQP_ID", "", "", ""},
            {"PosMachineContainerPosition",      "EQPCTNPST_ID", "EQP_ID", "EQPCTN_ID", "", ""},
            {"PosMachineNote",                   "NOTE_TITLE", "OWNER_ID", "CREATED_TIME", "EQP_ID", ""},
            {"PosMachineOperationProcedure",     "NOTE_TITLE", "OWNER_ID", "CREATED_TIME", "EQP_ID", ""},
            {"PosMachineState",                  "EQPSTATE_ID", "E10STATE_ID", "", "", ""},
            {"PosFactoryNote",                   "OWNER_ID", "CREATED_TIME", "", "", ""},
            {"PosFlowBatch",                     "FLOWBATCH_ID", "", "", "", ""},
            {"PosFlowBatchDispatcher",           "", "", "", "", ""},
            {"PosFutureReworkRequest",           "FUTUREREWORK_ID", "", "", "", ""},
            {"PosLot",                           "LOT_ID", "", "", "", ""},
            {"PosLotComment",                    "LOT_ID", "", "", "", ""},
            {"PosLotFamily",                     "LOTFAMILY_ID", "", "", "", ""},
            {"PosLotNote",                       "OWNER_ID", "CREATED_TIME", "LOT_ID", "", ""},
            {"PosLotOperationNote",              "OWNER_ID", "CREATED_TIME", "OPE_NO", "LOT_ID", "MAINPD_ID"},
            {"PosLotOperationSchedule",          "OPE_NO", "LOTSCHE_ID", "PD_ID", "", ""},
            {"PosLotSchedule",                   "LOTSCHE_ID", "", "", "", ""},
            {"PosLotType",                       "LOTTYPE_ID", "", "", "", ""},
            {"PosLogicalRecipe",                 "LCRECIPE_ID", "", "", "", ""},
            {"PosMonitorGroup",                  "MONITOR_GRP_ID", "", "", "", ""},
            {"PosMachineRecipe",                 "RECIPE_ID", "", "", "", ""},
            {"PosMessageDefinition",             "MSGDEF_ID", "", "", "", ""},
            {"PosMaterialLocation",              "MTRLLOC_ID", "RESOURCE_TYPE", "PORT_ID", "BUFFRES_ID", "EQP_ID"},
            {"PosMachineOperationMode",          "OPEMODE_ID", "", "", "", ""},
            {"PosProcessDefinition",             "PD_ID", "PD_LEVEL", "", "", ""},
            {"PosProcessFlow",                   "PD_ID", "PD_LEVEL", "", "", ""},
            {"PosProcessFlowContext",            "LOT_ID", "", "", "", ""},
            {"PosPlannedSplitJob",               "PLSPLITJOB_ID", "", "", "", ""},
            {"PosProcessOperation",              "OPE_NO", "ROUTE_ID", "LOT_ID", "", ""},
            {"PosPortResource",                  "PORT_ID", "EQP_ID", "", "", "" },
            {"PosProcessOperationSpecification", "OPE_NO", "PD_ID", "PD_LEVEL", "", ""},
            {"PosProcessResource",               "PROCRSC_ID", "EQP_ID", "", "", ""},
            {"PosPrivilegeGroup",                "PRIVGRP_KEY", "", "", "", ""},
            {"PosProductCategory",               "PROD_CATEGORY_ID", "", "", "", ""},
            {"PosProductGroup",                  "PRODGRP_ID", "", "", "", ""},
            {"PosProductRequest",                "PRODREQ_ID", "", "", "", ""},
            {"PosProductSpecification",          "PRODSPEC_ID", "", "", "", ""},
            {"PosQTimeRestriction",              "TRIGGER_OPE_NO", "TARGET_OPE_NO", "LOT_ID", "TRIGGER_MAINPD_ID", "TARGET_MAINPD_ID"},
            {"PosQTimeRestrictionByWafer",       "TRIGGER_OPE_NO", "TARGET_OPE_NO", "WAFER_ID", "TRIGGER_MAINPD_ID", "TARGET_MAINPD_ID"},
            {"PosRawMachineStateSet",            "RAWEQPSTATE_ID", "", "", "", ""},
            {"PosReticlePodPortResource",        "PORT_ID", "EQP_ID", "", "", ""},
            {"PosReticlePod",                    "RTCLPOD_ID", "", "", "", ""},
            {"PosReticleSet",                    "RTCLSET_ID", "", "", "", ""},
            {"PosScript",                        "SCRIPT_ID", "", "", "", ""},
            {"PosSampleSpecification",           "SMPLSPEC_ID", "", "", "", ""},
            {"PosStage",                         "STAGE_ID", "", "", "", ""},
            {"PosStageGroup",                    "STAGEGRP_ID", "", "", "", ""},
            {"PosStorageMachine",                "STK_ID", "", "", "", ""},
            {"PosSystemMessageCode",             "SYSMESSAGE_ID", "", "", "", ""},
            {"PosTechnology",                    "TECH_ID", "", "", "", ""},
            {"PosTestSpecification",             "TESTSPEC_ID", "", "", "", ""},
            {"PosTestType",                      "TESTTYPE_ID", "", "", "", ""},
            {"PosPerson",                        "USER_ID", "", "", "", ""},
            {"PosUserGroup",                     "USERGRP_ID", "", "", "", ""},
            {"PosWafer",                         "WAFER_ID", "", "", "", ""},
            {"PosEqpMonitor",                    "EQP_MONITOR_ID", "", "", "", ""},
            {"PosEqpMonitorJob",                 "EQP_MONITOR_JOB_ID", "EQP_MONITOR_ID", "", "", ""},
            {"PosDurableSubState",               "DRBLSUBSTATE_ID", "", "", "", ""},
            {"PosDurableControlJob",             "DCTRLJOB_ID", "", "", "", ""},
            {"PosDurableProcessFlowContext",     "DRBL_CATEGORY", "DRBL_ID", "", "", ""},
            {"PosDurableProcessOperation",       "OPE_NO", "ROUTE_ID", "DRBL_CATEGORY", "DRBL_ID", ""},
            {"",                                              "", "", "", "", ""}
    };

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param className
     * @param durableID
     * @return com.fa.cim.dto.RetCode<java.lang.Object>
     * @author Ho
     * @date 2019/2/19 13:21:05
     */
    @Override
    public void objectCheckForCreation(Infos.ObjCommon objCommon, String className, ObjectIdentifier durableID) {
        if( CimStringUtils.equals( className, BizConstant.SP_DURABLECAT_CASSETTE)) {
            CimCassette aCassette = null;
           boolean cassetteFound = true;
           if (CimStringUtils.isEmpty(durableID.getReferenceKey())){
               if(CimStringUtils.isEmpty(durableID.getValue())){
                   cassetteFound = false;
               } else {
                   Cassette aTempCassette = durableManager.findCassetteNamed(durableID.getValue());
                   aCassette = (CimCassette) aTempCassette;
                   if (aCassette == null){
                       cassetteFound = false;
                   }
               }
           } else {
               aCassette = baseCoreFactory.getBO(CimCassette.class, durableID.getReferenceKey());
               if (aCassette == null){
                   cassetteFound = false;
               }
           }
           Validations.check(cassetteFound, retCodeConfig.getObjectAlreadyExist());
        } else if( CimStringUtils.equals(className, BizConstant.SP_DURABLECAT_RETICLEPOD)) {
            CimReticlePod aReticlePod = null;
            Boolean reticlePodFound = true;
            if (CimStringUtils.isEmpty(durableID.getReferenceKey())){
                if(CimStringUtils.isEmpty(durableID.getValue())){
                    reticlePodFound = false;
                } else {
                    aReticlePod = durableManager.findReticlePodNamed(durableID.getValue());
                    if (aReticlePod == null){
                        reticlePodFound = false;
                    }
                }
            } else {
                aReticlePod = baseCoreFactory.getBO(CimReticlePod.class, durableID.getReferenceKey());
                if (aReticlePod == null){
                    reticlePodFound = false;
                }
            }
            Validations.check(reticlePodFound, retCodeConfig.getObjectAlreadyExist());
        }
        String checkID=durableID.getValue();
        char invalidCharacters[] = { '^', '`', '~', '|', '.', ',', '\'', '\"', '\\', '\0' };

        int  charCnt = 0;
        while( invalidCharacters[charCnt] != '\0' ) {
            String getPointer = BaseStaticMethod.strrchr(checkID,invalidCharacters[charCnt++]+"");
            Validations.check(null != getPointer, retCodeConfig.getProhibitiveCharacterUse());
        }
    }

    @Override
    public Outputs.ObjectIDList objectIDListGetDR(Infos.ObjCommon objCommon, Infos.objObjectIDListGetDR objectIDListGetDR) {
        // InParameter Trace
        log.info("# ClassName = {}", objectIDListGetDR.getClassName());
        log.info("# ObjectID = {} ", objectIDListGetDR.getObjectID().getValue());

        List<Infos.AdditionalSearchCondition> strAdditionalSearchConditionList = objectIDListGetDR.getStrAdditionalSearchConditionSeq();

        int nCnt = 0;
        int strAdditionalSearchConditionListLen = CimArrayUtils.getSize(strAdditionalSearchConditionList);
        log.info("strAdditionalSearchConditionSeqLen = {}", strAdditionalSearchConditionListLen);

        // InParameter Trace
        for (nCnt = 0; nCnt < strAdditionalSearchConditionListLen; nCnt++) {
            log.info("# Loop[nCnt] ", nCnt);
            log.info("# className = {}", strAdditionalSearchConditionList.get(nCnt).getClassName());
            log.info("# conditionName = {} ", strAdditionalSearchConditionList.get(nCnt).getConditionName());
            log.info("# conditionValue = {} ", strAdditionalSearchConditionList.get(nCnt).getConditionValue());
        }

        //Initialize;
        List<Infos.ObjectIDInformation> objectIDInformationList = new ArrayList<>();
        String sqlBuffer = "";
        log.info("Create SQL...");
        if (CimStringUtils.equals(BizConstant.SP_CLASSNAME_POSPRODUCTSPECIFICATION, objectIDListGetDR.getClassName())) {
            sqlBuffer = "select *from OMPRODINFO where 1=1 ";
            if (!CimStringUtils.isEmpty(objectIDListGetDR.getObjectID().getValue())) {
                log.info("objectID = {}", objectIDListGetDR.getObjectID().getValue());
                sqlBuffer += String.format(" and PROD_ID like '%s' ", objectIDListGetDR.getObjectID().getValue());
                log.info("sqlBuffer = {}", sqlBuffer);

                if (strAdditionalSearchConditionListLen > 0) {
                    log.info("strAdditionalSearchConditionSeqLen > 0");
                    boolean noFirstProductCategoryCondition = false;
                    for (nCnt = 0; nCnt < strAdditionalSearchConditionListLen; nCnt++) {
                        if (CimStringUtils.equals(BizConstant.SP_CLASSNAME_POSPRODUCTSPECIFICATION, strAdditionalSearchConditionList.get(nCnt).getClassName()) &&
                                CimStringUtils.equals(BizConstant.SP_POSTPROCESS_CONDITION_PRODUCTCATEGORY, strAdditionalSearchConditionList.get(nCnt).getConditionName())) {
                            log.info("# className =  PosProductSpecification and conditionName = ProductCategory");
                            if (!noFirstProductCategoryCondition) {
                                sqlBuffer += "and (";
                                noFirstProductCategoryCondition = true;
                            }else {
                                sqlBuffer += "or ";
                            }
                            sqlBuffer += String.format("PROD_CAT_ID = '%s' ", strAdditionalSearchConditionList.get(nCnt).getConditionValue());
                            log.info("# sqlBuffer = {} ", sqlBuffer);
                        }
                    }
                    if (noFirstProductCategoryCondition) {
                        sqlBuffer += ")";
                    }

                }

            }

            log.info("sqlBuffer = {} ", sqlBuffer);
            List<CimProductSpecificationDO> objectList = cimJpaRepository.query(sqlBuffer, CimProductSpecificationDO.class);
            for (CimProductSpecificationDO object : objectList) {
                Infos.ObjectIDInformation objectIDInformation = new Infos.ObjectIDInformation();
                objectIDInformation.setObjectID(new ObjectIdentifier(object.getProductSpecID(), object.getId()));
                objectIDInformation.setDescription(object.getDescription());
                objectIDInformationList.add(objectIDInformation);
            }

        } else if (CimStringUtils.equals(BizConstant.SP_CLASSNAME_POSPRODUCTGROUP, objectIDListGetDR.getClassName())) {
            sqlBuffer = "select * from OMPRODFMLY where 1=1 ";
            log.info("sqlBuffer = {} ", sqlBuffer);

            if (!CimStringUtils.isEmpty(objectIDListGetDR.getObjectID().getValue())) {
                sqlBuffer += String.format(" and PRODFMLY_ID like '%s' ", objectIDListGetDR.getObjectID().getValue());
                log.info("sqlBuffer = {} ", sqlBuffer);
            }

            log.info("sqlBuffer = {} ", sqlBuffer);
            List<CimProductGroupDO> objectList = cimJpaRepository.query(sqlBuffer, CimProductGroupDO.class);
            for (CimProductGroupDO object : objectList) {
                Infos.ObjectIDInformation objectIDInformation = new Infos.ObjectIDInformation();
                objectIDInformation.setObjectID(new ObjectIdentifier(object.getProductGroupID(), object.getId()));
                objectIDInformation.setDescription(object.getDescription());
                objectIDInformationList.add(objectIDInformation);
            }
        } else if (CimStringUtils.equals(BizConstant.SP_CLASSNAME_POSTECHNOLOGY, objectIDListGetDR.getClassName())) {
            sqlBuffer = "select * from OMTECH where 1=1 ";
            log.info("sqlBuffer = {} ", sqlBuffer);
            if (CimStringUtils.isEmpty(objectIDListGetDR.getObjectID().getValue())) {
                sqlBuffer += String.format(" and TECH_ID like '%s' ", objectIDListGetDR.getObjectID().getValue());
                log.info("sqlBuffer = {} ", sqlBuffer);
            }

            log.info("sqlBuffer = {} ", sqlBuffer);
            List<CimTechnologyDO> objectList = cimJpaRepository.query(sqlBuffer, CimTechnologyDO.class);
            for (CimTechnologyDO object : objectList) {
                Infos.ObjectIDInformation objectIDInformation = new Infos.ObjectIDInformation();
                objectIDInformation.setObjectID(new ObjectIdentifier(object.getTechnologyID(), object.getId()));
                objectIDInformation.setDescription(object.getDescription());
                objectIDInformationList.add(objectIDInformation);
            }
        } else if (CimStringUtils.equals(BizConstant.SP_CLASSNAME_POSMESSAGEDEFINITION, objectIDListGetDR.getClassName())) {
            sqlBuffer = "select * from OMNOTIFYDEF where 1=1 ";
            log.info("sqlBuffer = {} ", sqlBuffer);

            if (!ObjectIdentifier.isEmptyWithValue(objectIDListGetDR.getObjectID())) {
                log.info("objectID = {} ", objectIDListGetDR.getObjectID().getValue());
                sqlBuffer += String.format(" and NOTIFY_ID like '%s' ", objectIDListGetDR.getObjectID().getValue());
                log.info("sqlBuffer = {} ", sqlBuffer);
            }

            log.info("sqlBuffer = {} ", sqlBuffer);
            List<CimMessageDefinitionDO> objectList = cimJpaRepository.query(sqlBuffer, CimMessageDefinitionDO.class);
            for (CimMessageDefinitionDO object : objectList) {
                Infos.ObjectIDInformation objectIDInformation = new Infos.ObjectIDInformation();
                objectIDInformation.setObjectID(new ObjectIdentifier(object.getMessageDefinitionID(), object.getId()));
                objectIDInformation.setDescription(object.getDescription());
                objectIDInformationList.add(objectIDInformation);
            }
        } else if (CimStringUtils.equals(BizConstant.SP_CLASSNAME_POSRETICLEGROUP, objectIDListGetDR.getClassName())) {
            sqlBuffer = "select * from OMPDRBLGRP where ";
            sqlBuffer += String.format(" PDRBL_TYPE = '%s' ", BizConstant.SP_DURABLECAT_RETICLE);
            log.info("sqlBuffer = {} ", sqlBuffer);
            if (!CimStringUtils.isEmpty(ObjectIdentifier.fetchValue(objectIDListGetDR.getObjectID()))) {
                log.info("objectID = {}", ObjectIdentifier.fetchValue(objectIDListGetDR.getObjectID()));
                sqlBuffer += String.format(" and PDRBL_GRP_ID like '%s' ", objectIDListGetDR.getObjectID().getValue());
                log.info("sqlBuffer = {} ", sqlBuffer);
            }

            log.info("sqlBuffer = {} ", sqlBuffer);
            List<CimDurableGroupDO> objectList = cimJpaRepository.query(sqlBuffer, CimDurableGroupDO.class);
            for (CimDurableGroupDO object : objectList) {
                Infos.ObjectIDInformation objectIDInformation = new Infos.ObjectIDInformation();
                objectIDInformation.setObjectID(ObjectIdentifier.build(object.getDurableGroupId(), object.getId()));
                objectIDInformation.setDescription(object.getDescription());
                objectIDInformationList.add(objectIDInformation);
            }
        } else if (CimStringUtils.equals(BizConstant.SP_CLASSNAME_POSMACHINERECIPE, objectIDListGetDR.getClassName())) {
            sqlBuffer = "select * from OMRCP A ";
            log.info("sqlBuffer = {} ", sqlBuffer);
            if (strAdditionalSearchConditionListLen > 0) {
                log.info("strAdditionalSearchConditionSeqLen > 0");
                for (nCnt = 0; nCnt < strAdditionalSearchConditionListLen; nCnt++) {
                    log.info("# Loop[{}] ", nCnt);
                    Infos.AdditionalSearchCondition additionalSearchCondition = strAdditionalSearchConditionList.get(nCnt);
                    if (CimStringUtils.equals(BizConstant.SP_CLASSNAME_POSMACHINERECIPE, additionalSearchCondition.getClassName()) &&
                            CimStringUtils.equals(BizConstant.SP_MACHINERECIPE_CONDITION_EQUIPMENTID, additionalSearchCondition.getConditionName())) {
                        log.info("# className =  PosMachineRecipe and conditionName = EquipmentID");
                        sqlBuffer += String.format(" INNER JOIN OMRCP_EQP B ON A.ID=B.REFKEY AND B.EQP_ID LIKE '%s' ", additionalSearchCondition.getConditionValue());
                        log.info("sqlBuffer = {} ", sqlBuffer);
                        break;
                    }
                }

            }

            if (!ObjectIdentifier.isEmptyWithValue(objectIDListGetDR.getObjectID())) {
                log.info("objectID = {}", objectIDListGetDR.getObjectID().getValue());
                sqlBuffer += String.format(" WHERE A.RECIPE_ID like '%s' ", objectIDListGetDR.getObjectID().getValue());
                log.info("sqlBuffer = {} ", sqlBuffer);
            }
            log.info("sqlBuffer = {} ", sqlBuffer);
            List<CimMachineRecipeDO> objectList = cimJpaRepository.query(sqlBuffer, CimMachineRecipeDO.class);
            for (CimMachineRecipeDO object : objectList) {
                Infos.ObjectIDInformation objectIDInformation = new Infos.ObjectIDInformation();
                objectIDInformation.setObjectID(new ObjectIdentifier(object.getRecipeID(), object.getId()));
                objectIDInformation.setDescription(object.getDescription());
                objectIDInformationList.add(objectIDInformation);
            }
        } else if (CimStringUtils.equals(BizConstant.SP_CLASSNAME_POSCUSTOMER, objectIDListGetDR.getClassName())) {
            sqlBuffer = "select * from OMCUSTOMER where 1=1 ";
            log.info("sqlBuffer = {} ", sqlBuffer);
            if (!CimStringUtils.isEmpty(objectIDListGetDR.getObjectID().getValue())) {
                log.info("objectID = {}", objectIDListGetDR.getObjectID().getValue());
                sqlBuffer += String.format(" and CUSTOMER_ID like '%s' ", objectIDListGetDR.getObjectID().getValue());
                log.info("sqlBuffer = {} ", sqlBuffer);
            }
            sqlBuffer += " ORDER BY CUSTOMER_ID";
            log.info("sqlBuffer = {} ", sqlBuffer);

            log.info("sqlBuffer = {} ", sqlBuffer);
            List<CimCustomerDO> objectList = cimJpaRepository.query(sqlBuffer, CimCustomerDO.class);
            for (CimCustomerDO object : objectList) {
                Infos.ObjectIDInformation objectIDInformation = new Infos.ObjectIDInformation();
                objectIDInformation.setObjectID(new ObjectIdentifier(object.getCustomerID(), object.getId()));
                objectIDInformation.setDescription(object.getDescription());
                objectIDInformationList.add(objectIDInformation);
            }
        } else {
            return null;
        }
        Outputs.ObjectIDList objectIDList = new Outputs.ObjectIDList();
        objectIDList.setObjectIDInformationList(objectIDInformationList);
        return objectIDList;
    }

    @Override
    public List<Infos.ObjectIDList> objectIDListGetForProcessDefinitionDR(Infos.ObjCommon objCommon, Inputs.ObjObjectIDListGetDRIn in) {

        String className = in.getClassName();
        ObjectIdentifier objectID = in.getObjectID();
        List<Infos.AdditionalSearchCondition> strAdditionalSearchConditionSeq = in.getStrAdditionalSearchConditionSeq();
        // InParameter Trace
        log.info("{} # ClassName     ", className);
        log.info("{} # ObjectID      ", objectID);

        log.info("Create SQL...");
        List<Infos.ObjectIDList> objectIDLists = new ArrayList<>();
        StringBuffer sb;
        //For Process Definition.
        if (CimStringUtils.equals(className, BizConstant.SP_CLASSNAME_POSPROCESSDEFINITION)) {
            sb = new StringBuffer("SELECT PRP_ID, DESCRIPTION FROM OMPRP ");
            sb.append(String.format(" WHERE PRP_LEVEL = '%s' ", BizConstant.SP_PD_FLOWLEVEL_OPERATION));
            sb.append(String.format(" AND VERSION_ID != '%s' ", BizConstant.SP_ACTIVE_VERSION));
            if (!ObjectIdentifier.isEmptyWithValue(objectID)) {
                log.info("{} objectID ", objectID);
                sb.append(String.format(" AND PRP_ID like '%s' ", objectID.getValue()));
            }
            for (Infos.AdditionalSearchCondition additionalSearchCondition : strAdditionalSearchConditionSeq) {
                String className1 = additionalSearchCondition.getClassName();
                String conditionName = additionalSearchCondition.getConditionName();
                String conditionValue = additionalSearchCondition.getConditionValue();
                if (CimObjectUtils.isEmpty(conditionValue)) {
                    continue;
                }
                if (CimStringUtils.equals(className1, BizConstant.SP_CLASSNAME_POSPROCESSDEFINITION) &&
                        CimStringUtils.equals(conditionName, BizConstant.SP_PROCESSDEFINITION_CONDITION_PDTYPE)) {
                    sb.append(String.format(" AND PRP_TYPE = '%s' ", conditionValue));
                }
            }
        }

        //For Main Process Definition.
        else if (CimStringUtils.equals(className, BizConstant.SP_CLASSNAME_POSMAINPROCESSDEFINITION)) {
            sb = new StringBuffer("SELECT PRP_ID, DESCRIPTION FROM OMPRP ");
            sb.append(String.format("WHERE PRP_LEVEL = '%s' ", BizConstant.SP_PD_FLOWLEVEL_MAIN));
            if (!ObjectIdentifier.isEmptyWithValue(objectID)) {
                sb.append(String.format(" AND PRP_ID like '%s' ", objectID.getValue()));
            }
            for (Infos.AdditionalSearchCondition additionalSearchCondition : strAdditionalSearchConditionSeq) {
                String className1 = additionalSearchCondition.getClassName();
                String conditionName = additionalSearchCondition.getConditionName();
                String conditionValue = additionalSearchCondition.getConditionValue();
                if (CimObjectUtils.isEmpty(conditionValue)) {
                    continue;
                }
                if (CimStringUtils.equals(className1, BizConstant.SP_CLASSNAME_POSMAINPROCESSDEFINITION) &&
                        CimStringUtils.equals(conditionName, BizConstant.SP_MAINPROCESSDEFINITION_CONDITION_ROUTETYPE)) {
                    sb.append(String.format(" AND PRF_TYPE = '%s' ", conditionValue));
                }

                if (CimStringUtils.equals(className1, BizConstant.SP_CLASSNAME_POSMAINPROCESSDEFINITION) &&
                        CimStringUtils.equals(conditionName, BizConstant.SP_MAINPROCESSDEFINITION_CONDITION_PROCDEFTYPE)) {
                    sb.append(String.format(" AND PRP_TYPE = '%s' ", conditionValue));
                }

                if (CimStringUtils.equals(className1, BizConstant.SP_CLASSNAME_POSMAINPROCESSDEFINITION) &&
                        CimStringUtils.equals(conditionName, BizConstant.SP_MAINPROCESSDEFINITION_CONDITION_ACTIVESHOWFLAG)) {
                    if (!CimStringUtils.equals(conditionValue, BizConstant.SP_CHECKFLAG_ON)) {
                        sb.append(String.format(" AND VERSION_ID != '%s' ", BizConstant.SP_ACTIVE_VERSION));
                    }
                }
            }
        } else if (CimStringUtils.equals(className, BizConstant.SP_CLASSNAME_POSMODULEPROCESSDEFINITION)) {
            sb = new StringBuffer("SELECT PRP_ID, DESCRIPTION FROM OMPRP ");
            sb.append(String.format("WHERE PRP_LEVEL = '%s' ", BizConstant.SP_PD_FLOWLEVEL_MODULE));
            if (!ObjectIdentifier.isEmptyWithValue(objectID)) {
                sb.append(String.format(" AND PRP_ID like '%s' ", objectID.getValue()));
            }
            for (Infos.AdditionalSearchCondition additionalSearchCondition : strAdditionalSearchConditionSeq) {
                String className1 = additionalSearchCondition.getClassName();
                String conditionName = additionalSearchCondition.getConditionName();
                String conditionValue = additionalSearchCondition.getConditionValue();
                if (CimObjectUtils.isEmpty(conditionValue)) {
                    continue;
                }
                if (CimStringUtils.equals(className1, BizConstant.SP_CLASSNAME_POSMODULEPROCESSDEFINITION) &&
                        CimStringUtils.equals(conditionName, BizConstant.SP_MAINPROCESSDEFINITION_CONDITION_ACTIVESHOWFLAG)) {
                    if (!CimStringUtils.equals(conditionValue, BizConstant.SP_CHECKFLAG_ON)) {
                        sb.append(String.format(" AND VERSION_ID != '%s' ", BizConstant.SP_ACTIVE_VERSION));
                    }
                }
            }
        } else {
            // unsupported class. return RC_OK
            return null;
        }
        List<CimProcessDefinitionDO> results = cimJpaRepository.query(sb.toString(), CimProcessDefinitionDO.class);
        results = results.stream().sorted(Comparator.comparing(CimProcessDefinitionDO::getProcessDefinitionID)).collect(Collectors.toList());
        if (!CimObjectUtils.isEmpty(results)) {
            results.forEach(x -> objectIDLists.add(new Infos.ObjectIDList(new ObjectIdentifier(x.getProcessDefinitionID(), x.getId()), x.getDescription())));
        }
        return objectIDLists;
    }

    @Override
    public List<Infos.ObjectIDList> objectIDListGetForLotDR(Infos.ObjCommon objCommon, Inputs.ObjObjectIDListGetDRIn in) {
        String className = in.getClassName();
        ObjectIdentifier objectID = in.getObjectID();
        // InParameter Trace
        log.info("{} # ClassName     ", className);
        log.info("{} # ObjectID      ", objectID);

        log.info("Create SQL...");
        List<Infos.ObjectIDList> objectIDLists = new ArrayList<>();
        StringBuffer sb;
        if (CimStringUtils.equals(className, BizConstant.SP_CLASSNAME_POSLOT)) {
            sb = new StringBuffer("SELECT LOT_ID FROM OMLOT WHERE 1=1 ");
            log.info("{} HV_BUFFER ", sb.toString());
            if (!ObjectIdentifier.isEmpty(objectID)) {
                log.info("{} objectID ", objectID);
                sb.append(String.format(" AND LOT_ID LIKE '%s' ", objectID.getValue()));
            }
        } else {
            // unsupported class. return RC_OK
            return null;
        }

        List<CimLotDO> results = cimJpaRepository.query(sb.toString(), CimLotDO.class);
        results = results.stream().sorted(Comparator.comparing(CimLotDO::getLotID)).collect(Collectors.toList());
        if (!CimObjectUtils.isEmpty(results)) {
            results.forEach(x -> objectIDLists.add(new Infos.ObjectIDList(new ObjectIdentifier(x.getLotID(), x.getId()), "")));
        }
        return objectIDLists;
    }

    @Override
    public List<Infos.ObjectIDList> objectIDListGetForEquipmentDR(Infos.ObjCommon objCommon, Inputs.ObjObjectIDListGetDRIn in) {
        ObjectIdentifier objectID = in.getObjectID();
        String className = in.getClassName();
        List<Infos.AdditionalSearchCondition> strAdditionalSearchConditionSeq = in.getStrAdditionalSearchConditionSeq();
        // InParameter Trace
        log.info("{}# ClassName     ", className);
        log.info("{}# ObjectID      ", objectID);

        List<Infos.ObjectIDList> objectIDLists = new ArrayList<>();
        StringBuffer sb;
        log.info("Create SQL...");
        if (CimStringUtils.equals(className, BizConstant.SP_CLASSNAME_POSMACHINE)) {
            sb = new StringBuffer("SELECT EQP_ID, DESCRIPTION FROM OMEQP WHERE 1=1 ");

            if (!ObjectIdentifier.isEmpty(objectID)) {
                sb.append(String.format(" AND EQP_ID LIKE '%s' ", objectID.getValue()));
            }

            List<CimEquipmentDO> results = cimJpaRepository.query(sb.toString(), CimEquipmentDO.class);
            results = results.stream().sorted(Comparator.comparing(CimEquipmentDO::getEquipmentID)).collect(Collectors.toList());
            if (!CimObjectUtils.isEmpty(results)) {
                results.forEach(x -> objectIDLists.add(new Infos.ObjectIDList(new ObjectIdentifier(x.getEquipmentID(), x.getId()), x.getDescription())));
            }
        } else if (CimStringUtils.equals(className, BizConstant.SP_CLASSNAME_POSSTORAGEMACHINE)) {
            sb = new StringBuffer("SELECT STOCKER_ID, DESCRIPTION FROM OMSTOCKER WHERE 1=1 ");

            if (!ObjectIdentifier.isEmpty(objectID)) {
                sb.append(String.format(" AND STOCKER_ID LIKE '%s' ", objectID.getValue()));

                if (!CimObjectUtils.isEmpty(strAdditionalSearchConditionSeq)) {
                    log.info("strAdditionalSearchConditionSeqLen > 0");
                    for (Infos.AdditionalSearchCondition additionalSearchCondition : strAdditionalSearchConditionSeq) {
                        String className1 = additionalSearchCondition.getClassName();
                        String conditionName = additionalSearchCondition.getConditionName();
                        String conditionValue = additionalSearchCondition.getConditionValue();
                        if (CimStringUtils.equals(className1, BizConstant.SP_CLASSNAME_POSSTORAGEMACHINE) &&
                                CimStringUtils.equals(conditionName, BizConstant.SP_STORAGEMACHINE_CONDITION_STOCKERTYPE)) {
                            log.info("# className =  SP_ClassName_PosStorageMachine and conditionName = StockerType");

                            if (CimStringUtils.equals(conditionValue, BizConstant.SP_STORAGEMACHINE_CONDITIONVALUE_OTHER)) {
                                sb.append(String.format(" AND STOCKER_TYPE <> '%s' ", BizConstant.SP_STOCKER_TYPE_BARERETICLE));
                                sb.append(String.format(" AND STOCKER_TYPE <> '%s' ", BizConstant.SP_STOCKER_TYPE_RETICLEPOD));
                                sb.append(String.format(" AND STOCKER_TYPE <> '%s' ", BizConstant.SP_STOCKER_TYPE_RETICLESHELF));
                            } else {
                                sb.append(String.format(" AND STOCKER_TYPE = '%s' ", conditionValue));
                                break;
                            }
                        }
                    }
                }
                List<CimStockerDO> results = cimJpaRepository.query(sb.toString(), CimStockerDO.class);
                results = results.stream().sorted(Comparator.comparing(CimStockerDO::getStockerID)).collect(Collectors.toList());
                if (!CimObjectUtils.isEmpty(results)) {
                    results.forEach(x -> objectIDLists.add(new Infos.ObjectIDList(new ObjectIdentifier(x.getStockerID(), x.getId()), x.getDescription())));
                }
            }
        } else {
            // unsupported class. return RC_OK
            return null;
        }
        return objectIDLists;
    }

    @Override
    public List<Infos.ObjectIDList> objectIDListGetForAreaBankDR(Infos.ObjCommon objCommon, Inputs.ObjObjectIDListGetDRIn in) {
        ObjectIdentifier objectID = in.getObjectID();
        String className = in.getClassName();
        // InParameter Trace
        log.info("{}# ClassName     ", className);
        log.info("{}# ObjectID      ", objectID);

        List<Infos.ObjectIDList> objectIDLists = new ArrayList<>();
        StringBuffer sb;
        log.info("Create SQL...");
        if (CimStringUtils.equals(className, BizConstant.SP_CLASSNAME_POSAREA)) {
            sb = new StringBuffer("SELECT BAY_ID, DESCRIPTION FROM OMBAY WHERE 1=1 ");
            if (!ObjectIdentifier.isEmpty(objectID)) {
                sb.append(String.format(" AND AREA_CATEGORY = '%s' ", BizConstant.SP_AREACATEGORY_WORKAREA));
                sb.append(String.format(" AND BAY_ID LIKE '%s' ", objectID.getValue()));
            }

            List<CimAreaDO> results = cimJpaRepository.query(sb.toString(), CimAreaDO.class);
            if (!CimObjectUtils.isEmpty(results)) {
                results.forEach(x -> objectIDLists.add(new Infos.ObjectIDList(new ObjectIdentifier(x.getAreaID(), x.getId()), x.getDescription())));
            }
        } else if (CimStringUtils.equals(className, BizConstant.SP_CLASSNAME_POSBANK)) {
            sb = new StringBuffer("SELECT BANK_ID, DESCRIPTION FROM OMBANK WHERE 1=1 ");
            if (!ObjectIdentifier.isEmpty(objectID)) {
                sb.append(String.format(" AND BANK_ID LIKE '%s' ", objectID.getValue()));
            }

            List<CimBankDO> results = cimJpaRepository.query(sb.toString(), CimBankDO.class);
            results = results.stream().sorted(Comparator.comparing(CimBankDO::getBankID)).collect(Collectors.toList());
            if (!CimObjectUtils.isEmpty(results)) {
                results.forEach(x -> objectIDLists.add(new Infos.ObjectIDList(new ObjectIdentifier(x.getBankID(), x.getId()), x.getDescription())));
            }
        } else {
            // unsupported class. return RC_OK
            return null;
        }
        return objectIDLists;
    }


    @Override
    public List<Infos.ObjectIDList> objectIDListGetForDurableDR(Infos.ObjCommon objCommon, Inputs.ObjObjectIDListGetDRIn in) {

        ObjectIdentifier objectID = in.getObjectID();
        String className = in.getClassName();
        // InParameter Trace
        log.info("{}# ClassName     ", className);
        log.info("{}# ObjectID      ", objectID);

        List<Infos.ObjectIDList> objectIDLists = new ArrayList<>();
        StringBuffer sb;
        log.info("Create SQL...");
        if (CimStringUtils.equals(className, BizConstant.SP_CLASSNAME_POSCASSETTE)) {
            sb = new StringBuffer("SELECT CARRIER_ID, DESCRIPTION FROM OMCARRIER WHERE 1=1 ");
            if (!ObjectIdentifier.isEmpty(objectID)) {
                sb.append(String.format(" AND CARRIER_ID LIKE '%s' ", objectID.getValue()));
            }
            List<CimCassetteDO> results = cimJpaRepository.query(sb.toString(), CimCassetteDO.class);
            results = results.stream().sorted(Comparator.comparing(CimCassetteDO::getCassetteID)).collect(Collectors.toList());
            if (!CimObjectUtils.isEmpty(results)) {
                results.forEach(x -> objectIDLists.add(new Infos.ObjectIDList(new ObjectIdentifier(x.getCassetteID(), x.getId()), x.getDescription())));
            }
        } else if (CimStringUtils.equals(className, BizConstant.SP_CLASSNAME_POSRETICLE)) {
            sb = new StringBuffer("SELECT PDRBL_ID, DESCRIPTION FROM OMPDRBL WHERE 1=1 ");
            sb.append(String.format(" AND PDRBL_CATEGORY = '%s' ", BizConstant.SP_DURABLECAT_RETICLE));
            if (!ObjectIdentifier.isEmpty(objectID)) {
                sb.append(String.format(" AND PDRBL_ID LIKE '%s' ", objectID.getValue()));
            }
            List<CimDurableDO> results = cimJpaRepository.query(sb.toString(), CimDurableDO.class);
            results = results.stream().sorted(Comparator.comparing(CimDurableDO::getDurableId)).collect(Collectors.toList());
            if (!CimObjectUtils.isEmpty(results)) {
                results.forEach(x -> objectIDLists.add(new Infos.ObjectIDList(new ObjectIdentifier(x.getDurableId(), x.getId()), x.getDescription())));
            }
        } else if (CimStringUtils.equals(className, BizConstant.SP_CLASSNAME_POSRETICLEPOD)) {
            sb = new StringBuffer("SELECT RTCLPOD_ID, DESCRIPTION FROM OMRTCLPOD WHERE 1=1 ");
            if (!ObjectIdentifier.isEmpty(objectID)) {
                sb.append(String.format(" AND RTCLPOD_ID LIKE '%s' ", ObjectIdentifier.fetchValue(objectID)));
            }
            List<CimReticlePodDO> results = cimJpaRepository.query(sb.toString(), CimReticlePodDO.class);
            results = results.stream().sorted(Comparator.comparing(CimReticlePodDO::getReticlePodID)).collect(Collectors.toList());
            if (!CimObjectUtils.isEmpty(results)) {
                results.forEach(x -> objectIDLists.add(new Infos.ObjectIDList(new ObjectIdentifier(x.getReticlePodID(), x.getId()), x.getDescription())));
            }
        } else {
            return null;
        }
        return objectIDLists;
    }

    @Override
    public List<Infos.ObjectIDList> objectIDListGetForDurableSubStateDR(Infos.ObjCommon objCommon, Inputs.ObjObjectIDListGetDRIn in) {

        ObjectIdentifier objectID = in.getObjectID();
        log.info("{} sub status ID ", objectID);

        String subStateID = !ObjectIdentifier.isEmpty(objectID) ? objectID.getValue() : "%";
        List<CimDurableSubStateDO> results = cimJpaRepository.query("SELECT DRBL_SUBSTATE_ID, DESCRIPTION FROM OMDRBLST WHERE DRBL_SUBSTATE_ID like ?1", CimDurableSubStateDO.class, subStateID)
                .stream().sorted(Comparator.comparing(CimDurableSubStateDO::getDurableSubStateID))
                .collect(Collectors.toList());
        if (CimArrayUtils.isEmpty(results)) return Collections.emptyList();
        return results.stream().map(x -> {
            Infos.ObjectIDList objectIDList = new Infos.ObjectIDList();
            String objectId = String.format("%s.%s", x.getDurableState(), x.getDurableSubStateID());
            objectIDList.setObjectID(ObjectIdentifier.buildWithValue(objectId));
            objectIDList.setDescription(x.getDescription());
            return objectIDList;
        }).collect(Collectors.toList());
    }

    @Override
    public <T extends com.fa.cim.newcore.bo.CimBO> List<Infos.UserData> objectUserDataGet(Infos.ObjCommon objCommon, Infos.CDAValueInqInParm strCDAValueInqInParm, T bo) {
        List<Infos.UserData> out =  new ArrayList<>();
        List<GlobalDTO.UserDataSet> userDataSetList = bo.getUserDataSetNamedAndOrig(strCDAValueInqInParm.getUserDataName(), strCDAValueInqInParm.getUserDataOriginator());
        Validations.check(CimArrayUtils.isEmpty(userDataSetList), retCodeConfig.getNotFoundUData(),
                bo.getPrimaryKey(), strCDAValueInqInParm.getClassName(),
                CimArrayUtils.get(strCDAValueInqInParm.getStrHashedInfoSeq(), 0)
                        .orElse(new Infos.HashedInfo()).getHashData(),
                strCDAValueInqInParm.getUserDataName());
        userDataSetList.forEach(x -> {
            Infos.UserData userData = new Infos.UserData();
            userData.setName(x.getName());
            userData.setType(x.getType());
            userData.setValue(x.getValue());
            userData.setOriginator(x.getOriginator());
            out.add(userData);
        });
        return out;
    }

    @Override
    public Outputs.ObjLockModeOut objectLockModeGet(Infos.ObjCommon objCommon, Inputs.ObjLockModeIn objLockModeIn) {
        Outputs.ObjLockModeOut objLockModeOut = new Outputs.ObjLockModeOut();
        //----------------
        //  Initialize
        //----------------
        Long aLockMode = BizConstant.SP_EQP_LOCK_MODE_WRITE;
        Long aRequiredLock = (long)BizConstant.SP_OBJECTLOCK_LOCKTYPE_WRITE;
        //---------------------------------
        // Input Parameter
        //---------------------------------
        ObjectIdentifier objectID = objLockModeIn.getObjectID();
        String className = objLockModeIn.getClassName();
        String functionCategory = objLockModeIn.getFunctionCategory();
        boolean userDataUpdateFlag = objLockModeIn.getUserDataUpdateFlag();
        //-------------------------------
        // Initialize Output Parameters
        //-------------------------------
        objLockModeOut.setObjectID(objectID);
        objLockModeOut.setClassName(className);
        objLockModeOut.setLockMode(aLockMode);
        objLockModeOut.setRequiredLockForMainObject(aRequiredLock);
        // stringifiedObjectReference is specified
        // This logic is necessary when it was called by TxCDAValueUpdateReq
        if (CimStringUtils.isEmpty(className) && !CimStringUtils.isEmpty(objectID.getReferenceKey())){
            AtomicReference<String> classNameReference = new AtomicReference<>();
            AtomicReference<ObjectIdentifier> objectIDReference = new AtomicReference<>();
            // For PosMachine
            getClassNameAndObjectID(CimMachine.class, BizConstant.SP_CLASSNAME_POSMACHINE, classNameReference, objectIDReference);
            // For PosStorageMachine
            getClassNameAndObjectID(CimStorageMachine.class, BizConstant.SP_CLASSNAME_POSSTORAGEMACHINE, classNameReference, objectIDReference);
            // For PosCassette
            getClassNameAndObjectID(CimCassette.class, BizConstant.SP_CLASSNAME_POSCASSETTE, classNameReference, objectIDReference);
            // For PosLot
            getClassNameAndObjectID(CimLot.class, BizConstant.SP_CLASSNAME_POSLOT, classNameReference, objectIDReference);
            // For PosProductRequest
            getClassNameAndObjectID(CimProductRequest.class, BizConstant.SP_CLASSNAME_POSPRODUCTREQUEST, classNameReference, objectIDReference);
            // For PosReticlePod
            getClassNameAndObjectID(CimReticlePod.class, BizConstant.SP_CLASSNAME_POSRETICLEPOD, classNameReference, objectIDReference);
            // For PosControlJob
            getClassNameAndObjectID(CimControlJob.class, BizConstant.SP_CLASSNAME_POSCONTROLJOB, classNameReference, objectIDReference);
            // For PosDispatcher
            getClassNameAndObjectID(CimDispatcher.class, BizConstant.SP_CLASSNAME_POSDISPATCHER, classNameReference, objectIDReference);
            className = classNameReference.get();
            objectID = objectIDReference.get();
        }
        //---------------------------
        //  Check input parameter
        //---------------------------
        // Check that there is specification of objectID and className
        // Even if there is invalid parameter, return RC_OK.
        if ((CimStringUtils.isEmpty(objectID.getValue()) && CimStringUtils.isEmpty(objectID.getReferenceKey()))
                || CimStringUtils.isEmpty(className)){
            return objLockModeOut;
        }
        /*--------------------*/
        /*   For PosMachine   */
        /*--------------------*/
        if (CimStringUtils.equals(className, BizConstant.SP_CLASSNAME_POSMACHINE)){
            /*--------------------------*/
            /*   Get Equipment Object   */
            /*--------------------------*/
            CimMachine anEquipment = baseCoreFactory.getBO(CimMachine.class, objectID);
            Validations.check(anEquipment == null, new OmCode(retCodeConfig.getNotFoundEqp(), ObjectIdentifier.fetchValue(objectID)));
            //--------------------------------------------
            // Get lock mode of Equipment
            //--------------------------------------------
            // Call getLockMode() method of the Equipment object
            aLockMode = anEquipment.getLockMode();
            // Object Level Write lock only
            if (BizConstant.SP_EQP_LOCK_MODE_WRITE.equals(aLockMode)){
                aRequiredLock = (long)BizConstant.SP_OBJECTLOCK_LOCKTYPE_WRITE;
            } else {
                // Sorter Tx
                if (CimStringUtils.equals(functionCategory, BizConstant.SP_FUNCTIONCATEGORY_SORTERTXID)){
                    // Read lock fixed
                    aRequiredLock = (long)BizConstant.SP_OBJECTLOCK_LOCKTYPE_READ;
                    Long sorterJobLockFlag = StandardProperties.OM_SORTER_LOCK_JOB_FLAG.getLongValue();
                    if (sorterJobLockFlag != 1){
                        aLockMode = BizConstant.SP_EQP_LOCK_MODE_WRITE;
                    }
                }
                // The function that does not support parallel processing
                else if (CimStringUtils.equals(functionCategory, TransactionIDEnum.EQP_STATUS_CHANGE_REQ.getValue())
                            || CimStringUtils.equals(functionCategory, TransactionIDEnum.EQP_STATUS_CHANGE_RPT.getValue())
                            || CimStringUtils.equals(functionCategory, TransactionIDEnum.CHAMBER_STATUS_CHANGE_REQ.getValue())
                            || CimStringUtils.equals(functionCategory, TransactionIDEnum.CHAMBER_STATUS_CHANGE_RPT.getValue())
                            || CimStringUtils.equals(functionCategory, BizConstant.SP_POSTPROCESS_ACTIONID_RUNWAFERINFOUPDATE)
                            || CimStringUtils.equals(functionCategory, TransactionIDEnum.EQP_MODE_CHANGE_REQ.getValue())
                            || CimStringUtils.equals(functionCategory, TransactionIDEnum.EQP_NOTE_REGIST_REQ.getValue())
                            || CimStringUtils.equals(functionCategory, TransactionIDEnum.EQP_FLOW_BATCH_MAX_COUNT_CHANGE_REQ.getValue())
                            || CimStringUtils.equals(functionCategory, TransactionIDEnum.EQP_USAGE_COUNT_RESET_REQ.getValue())
                            || CimStringUtils.equals(functionCategory, TransactionIDEnum.SLM_START_LOTS_RECEIVE_MAX_COUNT_UPDATE_REQ.getValue())
                            || CimStringUtils.equals(functionCategory, TransactionIDEnum.FMC_MODE_CHANGE_REQ.getValue())
                            || CimStringUtils.equals(functionCategory, TransactionIDEnum.AUTO_FLOW_BATCHING_REQ.getValue())
                            || CimStringUtils.equals(functionCategory, TransactionIDEnum.EQP_RESERVE_REQ.getValue())
                            || CimStringUtils.equals(functionCategory, TransactionIDEnum.EQP_RESERVE_CANCEL_REQ.getValue())
                            || CimStringUtils.equals(functionCategory, TransactionIDEnum.FLOW_BATCHING_REQ.getValue())
                            || CimStringUtils.equals(functionCategory, TransactionIDEnum.REFLOW_BATCHING_REQ.getValue())
                            || CimStringUtils.equals(functionCategory, TransactionIDEnum.LOT_REMOVE_FROM_FLOW_BATCH_REQ.getValue())){
                    // Write lock fixed
                    aRequiredLock = (long)BizConstant.SP_OBJECTLOCK_LOCKTYPE_WRITE;
                }
                // The function that supports parallel processing
                else {
                    aRequiredLock = (long)BizConstant.SP_OBJECTLOCK_LOCKTYPE_READ;
                    boolean bCJIDGenCheck    = false;
                    boolean bLastRecipeCheck = false;
                    boolean bPMAttrCheck     = false;
                    boolean bFlowBatchCheck  = false;
                    boolean bOnlineModeCheck = false;
                    // StartLotsReservation
                    if (CimStringUtils.equals(functionCategory, TransactionIDEnum.START_LOTS_RESERVATION_REQ.getValue())
                            || CimStringUtils.equals(functionCategory, TransactionIDEnum.START_LOTS_RESERVATION_FOR_INTERNAL_BUFFER_REQ.getValue())
                            || CimStringUtils.equals(functionCategory, TransactionIDEnum.START_LOTS_RESERVATION_FOR_TAKE_OUT_IN_REQ.getValue())
                            || CimStringUtils.equals(functionCategory, TransactionIDEnum.FMC_MOVE_IN_RESERVE_REQ.getValue())){
                        bCJIDGenCheck = true;
                    }
                    // OpeStart
                    else if (CimStringUtils.equals(functionCategory, TransactionIDEnum.OPERATION_START_REQ.getValue())
                            || CimStringUtils.equals(functionCategory, TransactionIDEnum.OPERATION_START_FOR_INTERNAL_BUFFER_REQ.getValue())){
                        bLastRecipeCheck = true;
                        bPMAttrCheck     = true;
                        bFlowBatchCheck  = true;
                        bOnlineModeCheck = true;
                    }
                    // OpeStart with control job ID generation
                    else if (CimStringUtils.equals(functionCategory, BizConstant.SP_FUNCTIONCATEGORY_OPESTARTWITHCJIDGENTXID)
                                || CimStringUtils.equals(functionCategory, BizConstant.SP_FUNCTIONCATEGORY_OPESTARTFORIBWITHCJIDGENTXID)){
                        bCJIDGenCheck    = true;
                        bLastRecipeCheck = true;
                        bPMAttrCheck     = true;
                        bFlowBatchCheck  = true;
                        bOnlineModeCheck = true;
                    }
                    // OpeComp, ForceOpeComp, PartialOpeComp
                    else if (CimStringUtils.equals(functionCategory, TransactionIDEnum.OPERATION_COMP_WITH_DATA_REQ.getValue())
                                || CimStringUtils.equals(functionCategory, TransactionIDEnum.OPERATION_COMP_FOR_INTERNAL_BUFFER_REQ.getValue())
                                || CimStringUtils.equals(functionCategory, TransactionIDEnum.FORCE__OPE_COMP_REQ.getValue())
                                || CimStringUtils.equals(functionCategory, TransactionIDEnum.FORCE_OPERATION_COMP_FOR_INTERNAL_BUFFER_REQ.getValue())){
                        bLastRecipeCheck = true;
                        bFlowBatchCheck  = true;
                        bOnlineModeCheck = true;
                    }
                    // OpeStartCancel
                    else if (CimStringUtils.equals(functionCategory, TransactionIDEnum.OPERATION_START_CANCEL_REQ.getValue())
                                || CimStringUtils.equals(functionCategory, TransactionIDEnum.OPERATION_START_CANCEL_FOR_INTERNAL_BUFFER_REQ.getValue())){
                        bPMAttrCheck     = true;
                        bFlowBatchCheck  = true;
                        bOnlineModeCheck = true;
                    }
                    // OpeComp, ForceOpeComp, PartialOpeComp
                    else if (CimStringUtils.equals(functionCategory, TransactionIDEnum.PARTIAL_OPERATION_COMP_WITH_DATA_REQ.getValue())
                                || CimStringUtils.equals(functionCategory, TransactionIDEnum.PARTIAL_OPERATION_COMP_FOR_INTERNAL_BUFFER_REQ.getValue())){
                        bPMAttrCheck     = true;
                        bLastRecipeCheck = true;
                        bFlowBatchCheck  = true;
                        bOnlineModeCheck = true;
                    }
                    // todo durable
                    // Check controlJobID maintain mode
                    if (bCJIDGenCheck){
                        Long ctrlJobIdGenByDisp = StandardProperties.OM_CJID_BY_DISP.getLongValue();
                        // control job ID generation indicator is maintained by equipment object
                        if (ctrlJobIdGenByDisp != 1){
                            aRequiredLock = (long)BizConstant.SP_OBJECTLOCK_LOCKTYPE_WRITE;
                        } else {
                            boolean isControlJobGenerateByMachineFlag = anEquipment.isControlJobGenerateByMachine();
                            if (isControlJobGenerateByMachineFlag){
                                aRequiredLock = (long)BizConstant.SP_OBJECTLOCK_LOCKTYPE_WRITE;
                            }
                        }
                    }
                    // Check whether mms will update the last used machine recipe
                    if (bLastRecipeCheck && aRequiredLock != BizConstant.SP_OBJECTLOCK_LOCKTYPE_WRITE){
                        Long updateRecipeFlag = StandardProperties.OM_UPDATE_LAST_USED_RECIPE.getLongValue();
                        // updates used machine recipe information
                        if (updateRecipeFlag != 0){
                            aRequiredLock = (long)BizConstant.SP_OBJECTLOCK_LOCKTYPE_WRITE;
                        }
                    }
                    // Check whether mms will update the several equipment attributes in post process operation
                    if (bPMAttrCheck && aRequiredLock != BizConstant.SP_OBJECTLOCK_LOCKTYPE_WRITE){
                        Long runWaferUpdateByPostProc = StandardProperties.OM_PP_UPDATE_FOR_EQP_ATTR.getLongValue();
                        if (runWaferUpdateByPostProc != 1){
                            aRequiredLock = (long)BizConstant.SP_OBJECTLOCK_LOCKTYPE_WRITE;
                        }
                    }
                    // Check whether equipment online mode is offline
                    if (bOnlineModeCheck && aRequiredLock != BizConstant.SP_OBJECTLOCK_LOCKTYPE_WRITE){
                        //--------------------------------------------------------------------------
                        // Get Equipment on-line mode
                        //--------------------------------------------------------------------------
                        String onlineMode = equipmentMethod.equipmentOnlineModeGet(objCommon, objectID);
                        // Offline mode
                        if (CimStringUtils.equals(onlineMode, BizConstant.SP_EQP_ONLINEMODE_OFFLINE)){
                            // mms will automatically change equipment status which requires write lock on equipment object
                            aRequiredLock = (long)BizConstant.SP_OBJECTLOCK_LOCKTYPE_WRITE;
                        }
                    }
                    // Check whether equipment is flowbatch target equipment
                    if (bFlowBatchCheck && aRequiredLock != BizConstant.SP_OBJECTLOCK_LOCKTYPE_WRITE){
                        // Call allFlowBatches() to confirm whether it is target of the flow batch
                        List<CimFlowBatch> eqpFlowBatches = anEquipment.allFlowBatches();
                        // Flowbatch target (Flow batch exists)
                        if (!CimArrayUtils.isEmpty(eqpFlowBatches)){
                            aRequiredLock = (long)BizConstant.SP_OBJECTLOCK_LOCKTYPE_WRITE;
                        }
                    }
                }
                // Check user defined data control behavior mode
                if (userDataUpdateFlag && aRequiredLock != BizConstant.SP_OBJECTLOCK_LOCKTYPE_WRITE){
                    //--------------------------------------------
                    // Get user defined data mode
                    //--------------------------------------------
                    // Call getUserDefinedDataMode() method of the Equipment object
                    Long aUdataMode = anEquipment.getUserDefinedDataMode();
                    if ( 1 != aUdataMode){
                        aRequiredLock = (long)BizConstant.SP_OBJECTLOCK_LOCKTYPE_WRITE;
                    }
                }
            }
        }
        //----------------------
        // Set Output Parameters
        //----------------------
        objLockModeOut.setObjectID(objectID);
        objLockModeOut.setClassName(className);
        objLockModeOut.setLockMode(aLockMode);
        objLockModeOut.setRequiredLockForMainObject(aRequiredLock);
        return objLockModeOut;
    }

    public void getClassNameAndObjectID(Class<? extends CimBO> clazz, String theClassName, AtomicReference<String> className, AtomicReference<ObjectIdentifier> objectID){
        if (CimObjectUtils.isEmpty(className)){
            CimBO cimBO = null;
            try {
                cimBO = clazz.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
            if (cimBO instanceof CimMachine){
                CimMachine cimMachine = baseCoreFactory.getBO(CimMachine.class, objectID.get().getReferenceKey());
                objectID.get().setValue(cimMachine.getIdentifier());
            } else if (cimBO instanceof CimStorageMachine){
                CimStorageMachine cimStorageMachine = baseCoreFactory.getBO(CimStorageMachine.class, objectID.get().getReferenceKey());
                objectID.get().setValue(cimStorageMachine.getIdentifier());
            } else if (cimBO instanceof CimCassette){
                CimCassette cimCassette = baseCoreFactory.getBO(CimCassette.class, objectID.get().getReferenceKey());
                objectID.get().setValue(cimCassette.getIdentifier());
            } else if (cimBO instanceof CimLot){
                CimLot cimLot = baseCoreFactory.getBO(CimLot.class, objectID.get().getReferenceKey());
                objectID.get().setValue(cimLot.getIdentifier());
            } else if (cimBO instanceof CimProductRequest){
                CimProductRequest cimProductRequest = baseCoreFactory.getBO(CimProductRequest.class, objectID.get().getReferenceKey());
                objectID.get().setValue(cimProductRequest.getIdentifier());
            } else if (cimBO instanceof CimReticlePod){
                CimReticlePod cimReticlePod = baseCoreFactory.getBO(CimReticlePod.class, objectID.get().getReferenceKey());
                objectID.get().setValue(cimReticlePod.getIdentifier());
            } else if (cimBO instanceof CimControlJob){
                CimControlJob cimControlJob = baseCoreFactory.getBO(CimControlJob.class, objectID.get().getReferenceKey());
                objectID.get().setValue(cimControlJob.getIdentifier());
            }
            className.set(theClassName);
        }
    }

    public static void main(String[] args) {

    }

    @Override
    public <T extends com.fa.cim.newcore.bo.CimBO> T objectGet(Infos.ObjCommon objCommon, Inputs.ObjObjectGetIn objObjectGetIn) {
        BaseBO t = null;
        String className = objObjectGetIn.getClassName();

        if (!CimStringUtils.isEmpty(objObjectGetIn.getStringifiedObjectReference())) {
            // TODO
        } else{
            Validations.check(CimStringUtils.isEmpty(className), retCodeConfigEx.getBlankInputParameter());
        }

        boolean materialLocationFlag = false;
        if (CimStringUtils.equals(BizConstant.SP_CLASSNAME_POSMATERIALLOCATION, className)) {
            materialLocationFlag = true;
        }

        Map<String, Class<?>> tableMap = new HashMap<>();
        tableMap.put(BizConstant.SP_CLASSNAME_POSAREA, com.fa.cim.newcore.bo.factory.CimArea.class);
        tableMap.put(BizConstant.SP_CLASSNAME_POSAREAGROUP, com.fa.cim.newcore.bo.factory.CimAreaGroup.class);
        tableMap.put(BizConstant.SP_CLASSNAME_POSBANK,  com.fa.cim.newcore.bo.factory.CimBank.class);
        tableMap.put(BizConstant.SP_CLASSNAME_POSBINDEFINITION, com.fa.cim.newcore.bo.prodspec.CimBinDefinition.class);
        tableMap.put(BizConstant.SP_CLASSNAME_POSBINSPECIFICATION, com.fa.cim.newcore.bo.prodspec.CimBinSpecification.class);

        tableMap.put(BizConstant.SP_CLASSNAME_POSBOM,   com.fa.cim.newcore.bo.parts.CimBom.class);
        tableMap.put(BizConstant.SP_CLASSNAME_POSBUFFERRESOURCE, com.fa.cim.newcore.bo.machine.CimBufferResource.class);
        tableMap.put(BizConstant.SP_CLASSNAME_POSCALENDARDATE, com.fa.cim.newcore.bo.factory.CimCalendarDate.class);
        tableMap.put(BizConstant.SP_CLASSNAME_POSCASSETTE,  com.fa.cim.newcore.bo.durable.CimCassette.class);
        tableMap.put(BizConstant.SP_CLASSNAME_POSCODE, com.fa.cim.newcore.bo.code.CimCode.class);

        tableMap.put(BizConstant.SP_CLASSNAME_POSCONTROLJOB,  com.fa.cim.newcore.bo.product.CimControlJob.class);
        tableMap.put(BizConstant.SP_CLASSNAME_POSCUSTOMER,   com.fa.cim.newcore.bo.prodspec.CimCustomer.class);
        tableMap.put(BizConstant.SP_CLASSNAME_POSCUSTOMERPRODUCT,  com.fa.cim.newcore.bo.prodspec.CimCustomerProduct.class);
        tableMap.put(BizConstant.SP_CLASSNAME_POSDATACOLLECTIONDEFINITION, com.fa.cim.newcore.bo.dc.CimDataCollectionDefinition.class);
        tableMap.put(BizConstant.SP_CLASSNAME_POSDATACOLLECTIONSPECIFICATION,  com.fa.cim.newcore.bo.dc.CimDataCollectionSpecification.class);
        tableMap.put(BizConstant.SP_CLASSNAME_POSDISPATCHER, com.fa.cim.newcore.bo.dispatch.CimDispatcher.class);
        tableMap.put(BizConstant.SP_CLASSNAME_POSDURABLECONTROLJOB,  com.fa.cim.newcore.bo.durable.CimDurableControlJob.class);
        tableMap.put(BizConstant.SP_CLASSNAME_POSDURABLEPROCESSFLOWCONTEXT, com.fa.cim.newcore.bo.durable.CimDurableProcessFlowContext.class);
        tableMap.put(BizConstant.SP_CLASSNAME_POSPROCESSOPERATION, com.fa.cim.newcore.bo.pd.CimProcessOperation.class);


        tableMap.put(BizConstant.SP_CLASSNAME_POSDURABLESUBSTATE, com.fa.cim.newcore.bo.durable.CimDurableSubState.class);
        tableMap.put(BizConstant.SP_CLASSNAME_POSE10STATE,  com.fa.cim.newcore.bo.code.CimE10State.class);
        tableMap.put(BizConstant.SP_CLASSNAME_POSENTITYINHIBIT, CimRestriction.class);
        tableMap.put(BizConstant.SP_CLASSNAME_POSEQPMONITOR,  com.fa.cim.newcore.bo.machine.CimEqpMonitor.class);
        tableMap.put(BizConstant.SP_CLASSNAME_POSEQPMONITORJOB, com.fa.cim.newcore.bo.machine.CimEqpMonitorJob.class);
        tableMap.put(BizConstant.SP_CLASSNAME_POSFLOWBATCH, com.fa.cim.newcore.bo.dispatch.CimFlowBatch.class);
        tableMap.put(BizConstant.SP_CLASSNAME_POSFUTUREREWORKREQUEST, com.fa.cim.newcore.bo.product.CimFutureReworkRequest.class);
        tableMap.put(BizConstant.SP_CLASSNAME_POSLOGICALRECIPE,  com.fa.cim.newcore.bo.recipe.CimLogicalRecipe.class);
        tableMap.put(BizConstant.SP_CLASSNAME_POSLOT, com.fa.cim.newcore.bo.product.CimLot.class);
        tableMap.put(BizConstant.SP_CLASSNAME_POSLOTCOMMENT, com.fa.cim.newcore.bo.product.CimLotComment.class);
        tableMap.put(BizConstant.SP_CLASSNAME_POSLOTFAMILY, com.fa.cim.newcore.bo.product.CimLotFamily.class);
        tableMap.put(BizConstant.SP_CLASSNAME_POSLOTOPERATIONNOTE, com.fa.cim.newcore.bo.product.CimLotOperationNote.class);
        tableMap.put(BizConstant.SP_CLASSNAME_POSLOTOPERATIONSCHEDULE, com.fa.cim.newcore.bo.planning.CimLotOperationSchedule.class);
        tableMap.put(BizConstant.SP_CLASSNAME_POSLOTSCHEDULE, com.fa.cim.newcore.bo.planning.CimLotSchedule.class);
        tableMap.put(BizConstant.SP_CLASSNAME_POSLOTTYPE, com.fa.cim.newcore.bo.product.CimLotType.class);
        tableMap.put(BizConstant.SP_CLASSNAME_POSMACHINE, com.fa.cim.newcore.bo.machine.CimMachine.class);
        tableMap.put(BizConstant.SP_CLASSNAME_POSMACHINECONTAINER, com.fa.cim.newcore.bo.machine.CimMachineContainer.class);
        tableMap.put(BizConstant.SP_CLASSNAME_POSMACHINECONTAINERPOSITION, com.fa.cim.newcore.bo.machine.CimMachineContainerPosition.class);
        tableMap.put(BizConstant.SP_CLASSNAME_POSMACHINENOTE, com.fa.cim.newcore.bo.machine.CimMachineNote.class);
        tableMap.put(BizConstant.SP_CLASSNAME_POSMACHINEOPERATIONPROCEDURE, com.fa.cim.newcore.bo.machine.CimMachineOperationProcedure.class);
        tableMap.put(BizConstant.SP_CLASSNAME_POSMACHINEOPERATIONMODE, com.fa.cim.newcore.bo.code.CimMachineOperationMode.class);
        tableMap.put(BizConstant.SP_CLASSNAME_POSMACHINERECIPE, com.fa.cim.newcore.bo.recipe.CimMachineRecipe.class);
        tableMap.put(BizConstant.SP_CLASSNAME_POSMACHINESTATE,  com.fa.cim.newcore.bo.code.CimMachineState.class);
        tableMap.put(BizConstant.SP_CLASSNAME_POSFACTORYNOTE,  com.fa.cim.newcore.bo.factory.CimFactoryNote.class);
        tableMap.put(BizConstant.SP_CLASSNAME_POSMESSAGEDEFINITION, com.fa.cim.newcore.bo.msgdistribution.CimMessageDefinition.class);
        tableMap.put(BizConstant.SP_CLASSNAME_POSFLOWBATCHDISPATCHER, com.fa.cim.newcore.bo.dispatch.CimFlowBatchDispatcher.class);
        tableMap.put(BizConstant.SP_CLASSNAME_POSSAMPLESPECIFICATION, com.fa.cim.newcore.bo.prodspec.CimSampleSpecification.class);
        tableMap.put(BizConstant.SP_CLASSNAME_POSTESTSPECIFICATION, com.fa.cim.newcore.bo.prodspec.CimTestSpecification.class);
        tableMap.put(BizConstant.SP_CLASSNAME_POSTESTTYPE, com.fa.cim.newcore.bo.prodspec.CimTestType.class);
        tableMap.put(BizConstant.SP_CLASSNAME_POSDURABLEPROCESSOPERATION, com.fa.cim.newcore.bo.pd.CimDurableProcessOperation.class);
        tableMap.put(BizConstant.SP_CLASSNAME_POSMONITORGROUP, com.fa.cim.newcore.bo.product.CimMonitorGroup.class);
        tableMap.put(BizConstant.SP_CLASSNAME_POSPERSON, com.fa.cim.newcore.bo.person.CimPerson.class);
        tableMap.put(BizConstant.SP_CLASSNAME_POSPLANNEDSPLITJOB, com.fa.cim.newcore.bo.product.CimPlannedSplitJob.class);
        tableMap.put(BizConstant.SP_CLASSNAME_POSPORTRESOURCE, com.fa.cim.newcore.bo.machine.CimPortResource.class);                // yes, it's true
        /*tableMap.put(BizConstant.SP_CLASSNAME_POSPRIVILEGEGROUP,   com.fa.cim.newcore.bo.factory.CimPrivilegeGroupDO.class);
        tableMap.put(BizConstant.SP_CLASSNAME_POSPROCESSDEFINITION,  com.fa.cim.newcore.bo.factory.CimProcessDefinitionDO.class);
        tableMap.put(BizConstant.SP_CLASSNAME_POSPROCESSDURABLECAPABILITY,  com.fa.cim.newcore.bo.factory.CimDurableGroupDO.class);   // yes, it's true
        tableMap.put(BizConstant.SP_CLASSNAME_POSPROCESSFLOW,  com.fa.cim.newcore.bo.factory.CimProcessFlowDO.class);
        tableMap.put(BizConstant.SP_CLASSNAME_POSPROCESSFLOWCONTEXT,  com.fa.cim.newcore.bo.factory.CimProcessFlowContextDO.class);
        tableMap.put(BizConstant.SP_CLASSNAME_POSPROCESSOPERATIONSPECIFICATION,  com.fa.cim.newcore.bo.factory.CimProcessOperationSpecificationDO.class);
        tableMap.put(BizConstant.SP_CLASSNAME_POSPROCESSRESOURCE,  com.fa.cim.newcore.bo.factory.CimProcessResourceDO.class);
        tableMap.put(BizConstant.SP_CLASSNAME_POSPRODUCTCATEGORY,  com.fa.cim.newcore.bo.factory.CimProductCategoryDO.class);
        tableMap.put(BizConstant.SP_CLASSNAME_POSPRODUCTGROUP,  com.fa.cim.newcore.bo.factory.CimProductGroupDO.class);
        tableMap.put(BizConstant.SP_CLASSNAME_POSPRODUCTREQUEST,  com.fa.cim.newcore.bo.factory.CimProductRequestDO.class);
        tableMap.put(BizConstant.SP_CLASSNAME_POSPRODUCTSPECIFICATION,  com.fa.cim.newcore.bo.factory.CimProductSpecificationDO.class);
        tableMap.put(BizConstant.SP_CLASSNAME_POSQTIMERESTRICTION,  com.fa.cim.newcore.bo.factory.CimQTimeDO.class);    // yes, it's true
        tableMap.put(BizConstant.SP_CLASSNAME_POSQTIMERESTRICTIONBYWAFER,  com.fa.cim.newcore.bo.factory.CimQTimeDO.class);   // ???
        tableMap.put(BizConstant.SP_CLASSNAME_POSRAWMACHINESTATESET,  com.fa.cim.newcore.bo.factory.CimRawEquipmentStateDO.class);
        tableMap.put(BizConstant.SP_CLASSNAME_POSRETICLE,  com.fa.cim.newcore.bo.factory.CimReticlePodDO.class);
        tableMap.put(BizConstant.SP_CLASSNAME_POSRETICLEPOD,  com.fa.cim.newcore.bo.factory.CimReticlePodDO.class);
        tableMap.put(BizConstant.SP_CLASSNAME_POSRETICLEPODPORTRESOURCE,   com.fa.cim.newcore.bo.factory.CimReservePortDO.class);
        tableMap.put(BizConstant.SP_CLASSNAME_POSRETICLESET,  com.fa.cim.newcore.bo.factory.CimReticleSetDO.class);
        tableMap.put(BizConstant.SP_CLASSNAME_POSSCRIPT,  com.fa.cim.newcore.bo.factory.CimScriptDO.class);
        tableMap.put(BizConstant.SP_CLASSNAME_POSSTAGE,  com.fa.cim.newcore.bo.factory.CimStageDO.class);
        tableMap.put(BizConstant.SP_CLASSNAME_POSSTAGEGROUP,  com.fa.cim.newcore.bo.factory.CimStageGroupDO.class);
        tableMap.put(BizConstant.SP_CLASSNAME_POSSTORAGEMACHINE,  com.fa.cim.newcore.bo.factory.CimStockerDO.class);
        tableMap.put(BizConstant.SP_CLASSNAME_POSSYSTEMMESSAGECODE,  com.fa.cim.newcore.bo.factory.CimSystemMessageDO.class);
        tableMap.put(BizConstant.SP_CLASSNAME_POSTECHNOLOGY,  com.fa.cim.newcore.bo.factory.CimTechnologyDO.class);
        tableMap.put(BizConstant.SP_CLASSNAME_POSUSERGROUP,  com.fa.cim.newcore.bo.factory.CimPersonGroupDO.class);
        tableMap.put(BizConstant.SP_CLASSNAME_POSWAFER, com.fa.cim.newcore.bo.product.CimWafer.class); */

        tableMap.put(BizConstant.SP_CLASSNAME_POSPRIVILEGEGROUP, com.fa.cim.newcore.bo.person.CimPrivilegeGroup.class);
        tableMap.put(BizConstant.SP_CLASSNAME_POSPROCESSDEFINITION, com.fa.cim.newcore.bo.pd.CimProcessDefinition.class);
        tableMap.put(BizConstant.SP_CLASSNAME_POSPROCESSDURABLECAPABILITY, com.fa.cim.newcore.bo.durable.CimProcessDurableCapability.class);   // yes, it's true
        tableMap.put(BizConstant.SP_CLASSNAME_POSPROCESSFLOW, com.fa.cim.newcore.bo.pd.CimProcessFlow.class);
        tableMap.put(BizConstant.SP_CLASSNAME_POSPROCESSFLOWCONTEXT, com.fa.cim.newcore.bo.pd.CimProcessFlowContext.class);
        tableMap.put(BizConstant.SP_CLASSNAME_POSPROCESSOPERATIONSPECIFICATION,  com.fa.cim.newcore.bo.pd.CimProcessOperationSpecification.class);
        tableMap.put(BizConstant.SP_CLASSNAME_POSPROCESSRESOURCE, com.fa.cim.newcore.bo.machine.CimProcessResource.class);


        tableMap.put(BizConstant.SP_CLASSNAME_POSPRODUCTCATEGORY, com.fa.cim.newcore.bo.prodspec.CimProductCategory.class);
        tableMap.put(BizConstant.SP_CLASSNAME_POSPRODUCTGROUP, com.fa.cim.newcore.bo.prodspec.CimProductGroup.class);
        tableMap.put(BizConstant.SP_CLASSNAME_POSPRODUCTREQUEST, com.fa.cim.newcore.bo.planning.CimProductRequest.class);
        tableMap.put(BizConstant.SP_CLASSNAME_POSPRODUCTSPECIFICATION,  com.fa.cim.newcore.bo.prodspec.CimProductSpecification.class);
        tableMap.put(BizConstant.SP_CLASSNAME_POSQTIMERESTRICTION, com.fa.cim.newcore.bo.pd.CimQTimeRestriction.class);    // yes, it's true
        tableMap.put(BizConstant.SP_CLASSNAME_POSQTIMERESTRICTIONBYWAFER, com.fa.cim.newcore.bo.pd.CimQTimeRestriction.class);
        tableMap.put(BizConstant.SP_CLASSNAME_POSRAWMACHINESTATESET, com.fa.cim.newcore.bo.code.CimRawMachineStateSet.class);
        tableMap.put(BizConstant.SP_CLASSNAME_POSRETICLE, com.fa.cim.newcore.bo.durable.CimProcessDurable.class);
        tableMap.put(BizConstant.SP_CLASSNAME_POSRETICLEPOD,  com.fa.cim.newcore.bo.durable.CimReticlePod.class);
        tableMap.put(BizConstant.SP_CLASSNAME_POSRETICLEPODPORTRESOURCE, com.fa.cim.newcore.bo.machine.CimReticlePodPortResource.class);
        tableMap.put(BizConstant.SP_CLASSNAME_POSRETICLESET, com.fa.cim.newcore.bo.durable.CimReticleSet.class);
        tableMap.put(BizConstant.SP_CLASSNAME_POSSCRIPT,  com.fa.cim.newcore.bo.code.CimScript.class);
        tableMap.put(BizConstant.SP_CLASSNAME_POSSTAGE, com.fa.cim.newcore.bo.factory.CimStage.class);
        tableMap.put(BizConstant.SP_CLASSNAME_POSSTAGEGROUP,  com.fa.cim.newcore.bo.factory.CimStageGroup.class);
        tableMap.put(BizConstant.SP_CLASSNAME_POSSTORAGEMACHINE,  com.fa.cim.newcore.bo.machine.CimStorageMachine.class);
        tableMap.put(BizConstant.SP_CLASSNAME_POSSYSTEMMESSAGECODE, com.fa.cim.newcore.bo.code.CimSystemMessageCode.class);
        tableMap.put(BizConstant.SP_CLASSNAME_POSTECHNOLOGY, com.fa.cim.newcore.bo.prodspec.CimTechnology.class);
        tableMap.put(BizConstant.SP_CLASSNAME_POSUSERGROUP, com.fa.cim.newcore.bo.person.CimUserGroup.class);
        tableMap.put(BizConstant.SP_CLASSNAME_POSWAFER, com.fa.cim.newcore.bo.product.CimWafer.class);

        List<Infos.HashedInfo> strHashedInfoSeq = objObjectGetIn.getStrHashedInfoSeq();
        Validations.check(CimArrayUtils.isEmpty(strHashedInfoSeq), retCodeConfigEx.getBlankInputParameter());


        //PosBufferResource
        if(CimStringUtils.equals(objObjectGetIn.getClassName(),BizConstant.SP_CLASSNAME_POSBUFFERRESOURCE)){
            if (CimArrayUtils.getSize(strHashedInfoSeq) == 2){
                if (CimStringUtils.isNotEmpty(strHashedInfoSeq.get(1).getHashData()) && CimStringUtils.equals(BizConstant.SP_HASHDATA_EQP_ID,strHashedInfoSeq.get(1).getHashKey())){
                    CimMachine machine = baseCoreFactory.getBOByIdentifier(CimMachine.class, strHashedInfoSeq.get(1).getHashData());
                    if (null != machine && CimStringUtils.isNotEmpty(strHashedInfoSeq.get(0).getHashData())&& CimStringUtils.equals(BizConstant.SP_HASHDATA_BUFFRSC_ID,strHashedInfoSeq.get(0).getHashKey()) ){
                        t = machine.findBufferResourceNamed(strHashedInfoSeq.get(0).getHashData());
                    }
                }
            }
        }
        //PosCode
        else if(CimStringUtils.equals(objObjectGetIn.getClassName(),BizConstant.SP_CLASSNAME_POSCODE)){
            if (CimArrayUtils.getSize(strHashedInfoSeq) == 2) {
                if (CimStringUtils.isNotEmpty(strHashedInfoSeq.get(0).getHashData()) && CimStringUtils.equals(BizConstant.SP_HASHDATA_CATEGORY_ID, strHashedInfoSeq.get(1).getHashKey())) {
                    CimCategory category = baseCoreFactory.getBOByIdentifier(CimCategory.class, strHashedInfoSeq.get(1).getHashData());
                    if (null != category && CimStringUtils.isNotEmpty(strHashedInfoSeq.get(1).getHashData()) && CimStringUtils.equals(BizConstant.SP_HASHDATA_CODE_ID, strHashedInfoSeq.get(0).getHashKey())) {
                        t = category.findCodeNamed(strHashedInfoSeq.get(0).getHashData());
                    }
                }
            }
        }
        //PosCustomerProduct
        else if(CimStringUtils.equals(objObjectGetIn.getClassName(),BizConstant.SP_CLASSNAME_POSCUSTOMERPRODUCT)){
            if (CimArrayUtils.getSize(strHashedInfoSeq) == 2) {
                if (CimStringUtils.isNotEmpty(strHashedInfoSeq.get(1).getHashData())
                        && CimStringUtils.equals(BizConstant.SP_HASHDATA_CUSTOMER_ID,
                        strHashedInfoSeq.get(0).getHashKey())) {
                    CimCustomer customer = baseCoreFactory.getBOByIdentifier(CimCustomer.class,
                            strHashedInfoSeq.get(0).getHashData());
                    if (null != customer && CimStringUtils.isNotEmpty(strHashedInfoSeq.get(0).getHashData())
                            && CimStringUtils.equals(BizConstant.SP_HASHDATA_PRODSPEC_ID,
                            strHashedInfoSeq.get(1).getHashKey())) {
                        t = customer.findCustomerProductNamed(customer.getIdentifier() + "."
                                + strHashedInfoSeq.get(1).getHashData());
                    }
                }
            }
        }
        //PosDispatcher
        else if(CimStringUtils.equals(objObjectGetIn.getClassName(),BizConstant.SP_CLASSNAME_POSDISPATCHER)){
            if (CimArrayUtils.getSize(strHashedInfoSeq) == 1) {
                if (CimStringUtils.isNotEmpty(strHashedInfoSeq.get(0).getHashData()) && CimStringUtils.equals(BizConstant.SP_HASHDATA_EQP_ID, strHashedInfoSeq.get(0).getHashKey())) {
                    CimMachine machine = baseCoreFactory.getBOByIdentifier(CimMachine.class, strHashedInfoSeq.get(0).getHashData());
                    if (null != machine){
                        t = machine.getDispatcher();
                    }
                }
            }
        }
        //PosMachineContainer
        else if(CimStringUtils.equals(objObjectGetIn.getClassName(),BizConstant.SP_CLASSNAME_POSMACHINECONTAINER)){
            if (CimArrayUtils.getSize(strHashedInfoSeq) == 2) {
                if (CimStringUtils.isNotEmpty(strHashedInfoSeq.get(1).getHashData())) {
                    CimMachine machine = baseCoreFactory.getBOByIdentifier(CimMachine.class, strHashedInfoSeq.get(1).getHashData());
                    if (null != machine && CimStringUtils.isNotEmpty(strHashedInfoSeq.get(0).getHashData())) {
                        t = machine.findMachineContainerNamed(strHashedInfoSeq.get(0).getHashData());
                    }
                }
            }
        }
        //PosMachineContainerPosition
        else if(CimStringUtils.equals(objObjectGetIn.getClassName(),BizConstant.SP_CLASSNAME_POSMACHINECONTAINERPOSITION)){
            if (CimArrayUtils.getSize(strHashedInfoSeq) == 3) {
                if (CimStringUtils.isNotEmpty(strHashedInfoSeq.get(1).getHashData()) ) {
                    CimMachine machine = baseCoreFactory.getBOByIdentifier(CimMachine.class, strHashedInfoSeq.get(1).getHashData());
                    if (null != machine && CimStringUtils.isNotEmpty(strHashedInfoSeq.get(2).getHashData())) {
                        CimMachineContainer machineContainer = machine.findMachineContainerNamed(strHashedInfoSeq.get(2).getHashData());
                        if (null != machineContainer && CimStringUtils.isNotEmpty(strHashedInfoSeq.get(0).getHashData())){
                              t = machineContainer.findMachineContainerPositionNamed(strHashedInfoSeq.get(0).getHashData());
                        }
                    }
                }
            }
        }
        //PosMachineNote
        else if(CimStringUtils.equals(objObjectGetIn.getClassName(),BizConstant.SP_CLASSNAME_POSMACHINENOTE)){
            if (CimArrayUtils.getSize(strHashedInfoSeq) == 4) {
                if (CimStringUtils.isNotEmpty(strHashedInfoSeq.get(3).getHashData())) {
                    CimMachine machine = baseCoreFactory.getBOByIdentifier(CimMachine.class, strHashedInfoSeq.get(3).getHashData());
                    if (null != machine && CimStringUtils.isNotEmpty(strHashedInfoSeq.get(2).getHashData()) && CimStringUtils.isNotEmpty(strHashedInfoSeq.get(1).getHashData()) && CimStringUtils.isNotEmpty(strHashedInfoSeq.get(0).getHashData())) {
                        t = machine.findMachineNoteNamed(strHashedInfoSeq.get(2).getHashData(),strHashedInfoSeq.get(1).getHashData(),strHashedInfoSeq.get(0).getHashData());
                    }
                }
            }
        }
        //PosMachineOperationProcedure
        else if(CimStringUtils.equals(objObjectGetIn.getClassName(),BizConstant.SP_CLASSNAME_POSMACHINEOPERATIONPROCEDURE)){
            if (CimArrayUtils.getSize(strHashedInfoSeq) == 4) {
                if (CimStringUtils.isNotEmpty(strHashedInfoSeq.get(3).getHashData())) {
                    CimMachine machine = baseCoreFactory.getBOByIdentifier(CimMachine.class, strHashedInfoSeq.get(3).getHashData());
                    if (null != machine && CimStringUtils.isNotEmpty(strHashedInfoSeq.get(2).getHashData()) && CimStringUtils.isNotEmpty(strHashedInfoSeq.get(1).getHashData()) && CimStringUtils.isNotEmpty(strHashedInfoSeq.get(0).getHashData())) {
                        t = machine.findMachineOperationProcedureNamed(strHashedInfoSeq.get(2).getHashData(),strHashedInfoSeq.get(1).getHashData(),strHashedInfoSeq.get(0).getHashData());
                    }
                }
            }
        }

        //PosMachineState
        else if(CimStringUtils.equals(objObjectGetIn.getClassName(),BizConstant.SP_CLASSNAME_POSMACHINESTATE)){
            if (CimArrayUtils.getSize(strHashedInfoSeq) == 2) {
                if (CimStringUtils.isNotEmpty(strHashedInfoSeq.get(1).getHashData())) {
                    CimE10State e10State = baseCoreFactory.getBOByIdentifier(CimE10State.class, strHashedInfoSeq.get(1).getHashData());
                    if (null != e10State && CimStringUtils.isNotEmpty(strHashedInfoSeq.get(0).getHashData())) {
                        t = e10State.findMachineStateNamed(strHashedInfoSeq.get(0).getHashData());
                    }
                }
            }
        }
        //PosFactoryNote
        else if(CimStringUtils.equals(objObjectGetIn.getClassName(),BizConstant.SP_CLASSNAME_POSFACTORYNOTE)){
            if (CimArrayUtils.getSize(strHashedInfoSeq) == 2) {
                if (CimStringUtils.isNotEmpty(strHashedInfoSeq.get(1).getHashData()) && CimStringUtils.isNotEmpty(strHashedInfoSeq.get(0).getHashData())) {
                    String key = strHashedInfoSeq.get(1).getHashData() + BizConstant.DOT + strHashedInfoSeq.get(0).getHashData();
                    t = baseCoreFactory.getBOByIdentifier(CimFactoryNote.class, key);
                }
            }
        }
        //PosLotComment
        else if (CimStringUtils.equals(objObjectGetIn.getClassName(), BizConstant.SP_CLASSNAME_POSLOTCOMMENT)) {
            if (CimArrayUtils.getSize(strHashedInfoSeq) == 1) {
                if (CimStringUtils.isNotEmpty(strHashedInfoSeq.get(0).getHashData())) {
                    CimLot lot = baseCoreFactory.getBOByIdentifier(CimLot.class, strHashedInfoSeq.get(0).getHashData());
                    if (null != lot) {
                        t = lot.getLotComment();
                    }
                }
            }
        }
        //PosLotNote
        else if (CimStringUtils.equals(objObjectGetIn.getClassName(), BizConstant.SP_CLASSNAME_POSLOTNOTE)) {
            if (CimArrayUtils.getSize(strHashedInfoSeq) == 3) {
                if (CimStringUtils.isNotEmpty(strHashedInfoSeq.get(2).getHashData())) {
                    CimLot lot = baseCoreFactory.getBOByIdentifier(CimLot.class, strHashedInfoSeq.get(2).getHashData());
                    if (null != lot && CimStringUtils.isNotEmpty(strHashedInfoSeq.get(1).getHashData()) && CimStringUtils.isNotEmpty(strHashedInfoSeq.get(0).getHashData())) {
                        t = lot.findLotNoteNamed(strHashedInfoSeq.get(1).getHashData(), strHashedInfoSeq.get(0).getHashData());
                    }
                }
            }
        }
        //PosLotOperationNote
        else if(CimStringUtils.equals(objObjectGetIn.getClassName(),BizConstant.SP_CLASSNAME_POSLOTOPERATIONNOTE)){
            if (CimArrayUtils.getSize(strHashedInfoSeq) == 5) {
                if (CimStringUtils.isNotEmpty(strHashedInfoSeq.get(3).getHashData()) ) {
                    CimLot lot = baseCoreFactory.getBOByIdentifier(CimLot.class, strHashedInfoSeq.get(3).getHashData());
                    if (null != lot && CimStringUtils.isNotEmpty(strHashedInfoSeq.get(1).getHashData()) && CimStringUtils.isNotEmpty(strHashedInfoSeq.get(0).getHashData()) && CimStringUtils.isNotEmpty(strHashedInfoSeq.get(4).getHashData()) && CimStringUtils.isNotEmpty(strHashedInfoSeq.get(2).getHashData())) {
                        t = lot.findLotOperationNoteNamed(strHashedInfoSeq.get(1).getHashData(),
                                strHashedInfoSeq.get(0).getHashData(),
                                strHashedInfoSeq.get(4).getHashData(),
                                strHashedInfoSeq.get(2).getHashData());
                    }
                }
            }
        }
        //PosLotOperationSchedule
        else if(CimStringUtils.equals(objObjectGetIn.getClassName(),BizConstant.SP_CLASSNAME_POSLOTOPERATIONSCHEDULE)){
            if (CimArrayUtils.getSize(strHashedInfoSeq) == 3) {
                if (CimStringUtils.isNotEmpty(strHashedInfoSeq.get(1).getHashData()) ) {
                    CimLot lot = baseCoreFactory.getBOByIdentifier(CimLot.class, strHashedInfoSeq.get(1).getHashData());
                    if (null != lot && CimStringUtils.isNotEmpty(strHashedInfoSeq.get(0).getHashData()) && CimStringUtils.isNotEmpty(strHashedInfoSeq.get(2).getHashData())) {
                        CimLotSchedule lotSchedule = lot.getLotSchedule();
                        if (null != lotSchedule){
                            String key = strHashedInfoSeq.get(2).getHashData() + BizConstant.SP_KEY_SEPARATOR_DOT + strHashedInfoSeq.get(0).getHashData();
                            t = lotSchedule.findLotOperationScheduleNamed(key);
                        }
                    }
                }
            }
        }
        //PosMaterialLocation
        else if(CimStringUtils.equals(objObjectGetIn.getClassName(),BizConstant.SP_CLASSNAME_POSMATERIALLOCATION)){
            if (CimArrayUtils.getSize(strHashedInfoSeq) == 5) {
                if (CimStringUtils.isNotEmpty(strHashedInfoSeq.get(4).getHashData())){
                    CimMachine machine = baseCoreFactory.getBOByIdentifier(CimMachine.class, strHashedInfoSeq.get(4).getHashData());
                    if (null != machine && CimStringUtils.isNotEmpty(strHashedInfoSeq.get(1).getHashData())){
                        //Buffer
                        if (CimStringUtils.equals(BizConstant.SP_RESOURCELEVEL_BUFFERRESOURCE,strHashedInfoSeq.get(1).getHashData())){
                            if (CimStringUtils.isNotEmpty(strHashedInfoSeq.get(3).getHashData())){
                                BufferResource bufferResource = machine.findBufferResourceNamed(strHashedInfoSeq.get(3).getHashData());
                                if (null != bufferResource && CimStringUtils.isNotEmpty(strHashedInfoSeq.get(0).getHashData())){
                                    t = bufferResource.findMaterialLocationNamed(strHashedInfoSeq.get(0).getHashData());
                                }
                            }
                        }
                        //Port
                        else if (CimStringUtils.equals(BizConstant.SP_RESOURCELEVEL_PORTRESOURCE,strHashedInfoSeq.get(1).getHashData())){
                            if (CimStringUtils.isNotEmpty(strHashedInfoSeq.get(2).getHashData())){
                                PortResource portResource = machine.findPortResourceNamed(strHashedInfoSeq.get(2).getHashData());
                                if (null != portResource && CimStringUtils.isNotEmpty(strHashedInfoSeq.get(0).getHashData())){
                                    t = portResource.findMaterialLocationNamed(strHashedInfoSeq.get(0).getHashData());
                                }
                            }
                        }
                        //Other
                        else {
                            Validations.check(retCodeConfig.getInvalidParameterWithMsg(),"ResourceType is incorrect");
                        }
                    }
                }
            }
        }
        //PosProcessDefinition
        else if(CimStringUtils.equals(objObjectGetIn.getClassName(),BizConstant.SP_CLASSNAME_POSPROCESSDEFINITION)){
            if (CimArrayUtils.getSize(strHashedInfoSeq) == 2) {
                if (CimStringUtils.isNotEmpty(strHashedInfoSeq.get(1).getHashData()) && CimStringUtils.isNotEmpty(strHashedInfoSeq.get(0).getHashData())){
                    //Main
                    if (CimStringUtils.equals(BizConstant.SP_PD_FLOWLEVEL_MAIN,strHashedInfoSeq.get(1).getHashData())){
                        t = processDefinitionManager.findMainProcessDefinitionNamed(strHashedInfoSeq.get(0).getHashData());
                    }
                    //Module
                    else if (CimStringUtils.equals(BizConstant.SP_PD_FLOWLEVEL_MODULE,strHashedInfoSeq.get(1).getHashData())){
                        t = processDefinitionManager.findModuleProcessDefinitionNamed(strHashedInfoSeq.get(0).getHashData());
                    }
                    //Operation
                    else if (CimStringUtils.equals(BizConstant.SP_PD_FLOWLEVEL_OPERATION,strHashedInfoSeq.get(1).getHashData())){
                        t = processDefinitionManager.findProcessDefinitionNamed(strHashedInfoSeq.get(0).getHashData());
                    }
                    //Other
                    else {
                        Validations.check(retCodeConfig.getInvalidPDLevel(),strHashedInfoSeq.get(1).getHashData());
                    }
                }
            }
        }
        //PosProcessFlow
        else if(CimStringUtils.equals(objObjectGetIn.getClassName(),BizConstant.SP_CLASSNAME_POSPROCESSFLOW)){
            if (CimArrayUtils.getSize(strHashedInfoSeq) == 2) {
                if (CimStringUtils.isNotEmpty(strHashedInfoSeq.get(1).getHashData()) && CimStringUtils.isNotEmpty(strHashedInfoSeq.get(0).getHashData())) {
                    CimProcessDefinition processDefinition = null;
                    //Module
                    if (CimStringUtils.equals(BizConstant.SP_PD_FLOWLEVEL_MODULE, strHashedInfoSeq.get(1).getHashData())) {
                        processDefinition = processDefinitionManager.findModuleProcessDefinitionNamed(strHashedInfoSeq.get(0).getHashData());
                    }
                    //Main_Ope, Main_Mod
                    else if (CimStringUtils.equals(BizConstant.SP_PD_FLOWLEVEL_MAIN_FOR_OPERATION, strHashedInfoSeq.get(1).getHashData())
                            || CimStringUtils.equals(BizConstant.SP_PD_FLOWLEVEL_MAIN_FOR_MODULE, strHashedInfoSeq.get(1).getHashData())) {
                        processDefinition = processDefinitionManager.findMainProcessDefinitionNamed(strHashedInfoSeq.get(0).getHashData());
                    }
                    //Other
                    else {
                        Validations.check(retCodeConfig.getInvalidPDLevel(), strHashedInfoSeq.get(1).getHashData());
                    }
                    if (null != processDefinition){
                        if (CimStringUtils.equals(BizConstant.SP_PD_FLOWLEVEL_MODULE, strHashedInfoSeq.get(1).getHashData())
                                || CimStringUtils.equals(BizConstant.SP_PD_FLOWLEVEL_MAIN_FOR_OPERATION, strHashedInfoSeq.get(1).getHashData())) {
                            t = processDefinition.getActiveProcessFlow();
                        }
                        else if (CimStringUtils.equals(BizConstant.SP_PD_FLOWLEVEL_MAIN_FOR_MODULE, strHashedInfoSeq.get(1).getHashData())){
                            t = processDefinition.getActiveMainProcessFlow();
                        }
                    }
                }
            }
        }
        //PosProcessFlowContext
        else if(CimStringUtils.equals(objObjectGetIn.getClassName(),BizConstant.SP_CLASSNAME_POSPROCESSFLOWCONTEXT)){
            if (CimArrayUtils.getSize(strHashedInfoSeq) == 1) {
                if (CimStringUtils.isNotEmpty(strHashedInfoSeq.get(0).getHashData())) {
                    CimLot lot = baseCoreFactory.getBOByIdentifier(CimLot.class, strHashedInfoSeq.get(0).getHashData());
                    if (null != lot){
                        t = lot.getProcessFlowContext();
                    }
                }
            }
        }
        //PosProcessOperation
        else if(CimStringUtils.equals(objObjectGetIn.getClassName(),BizConstant.SP_CLASSNAME_POSPROCESSOPERATION)){
            if (CimArrayUtils.getSize(strHashedInfoSeq) == 3) {
                if (CimStringUtils.isNotEmpty(strHashedInfoSeq.get(2).getHashData()) ) {
                    CimLot lot = baseCoreFactory.getBOByIdentifier(CimLot.class, strHashedInfoSeq.get(2).getHashData());
                    if (null != lot && CimStringUtils.isNotEmpty(strHashedInfoSeq.get(0).getHashData()) && CimStringUtils.isNotEmpty(strHashedInfoSeq.get(1).getHashData())) {
                        CimProcessFlowContext processFlowContext = lot.getProcessFlowContext();
                        if (null != processFlowContext){
                            t = processFlowContext.findProcessOperationForRouteOperationNumberBefore(strHashedInfoSeq.get(1).getHashData(),strHashedInfoSeq.get(0).getHashData());
                        }
                    }
                }
            }
        }
        //PosPortResource
        else if(CimStringUtils.equals(objObjectGetIn.getClassName(),BizConstant.SP_CLASSNAME_POSPORTRESOURCE)){
            if (CimArrayUtils.getSize(strHashedInfoSeq) == 2) {
                if (CimStringUtils.isNotEmpty(strHashedInfoSeq.get(1).getHashData())) {
                    CimMachine machine = baseCoreFactory.getBOByIdentifier(CimMachine.class, strHashedInfoSeq.get(1).getHashData());
                    if (null != machine && CimStringUtils.isNotEmpty(strHashedInfoSeq.get(0).getHashData())) {
                        t = machine.findPortResourceNamed(strHashedInfoSeq.get(0).getHashData());
                    }
                }
            }
        }
        //PosProcessOperationSpecification
        else if(CimStringUtils.equals(objObjectGetIn.getClassName(),BizConstant.SP_CLASSNAME_POSPROCESSOPERATIONSPECIFICATION)){
            if (CimArrayUtils.getSize(strHashedInfoSeq) == 3){
                if (CimStringUtils.isNotEmpty(strHashedInfoSeq.get(2).getHashData()) && CimStringUtils.isNotEmpty(strHashedInfoSeq.get(1).getHashData())){
                    CimProcessDefinition processDefinition = null;
                    //Module
                    if (CimStringUtils.equals(BizConstant.SP_PD_FLOWLEVEL_MODULE,strHashedInfoSeq.get(2).getHashData())){
                        processDefinition = processDefinitionManager.findModuleProcessDefinitionNamed(strHashedInfoSeq.get(1).getHashData());
                    }
                    //Main
                    else if (CimStringUtils.equals(BizConstant.SP_PD_FLOWLEVEL_MAIN,strHashedInfoSeq.get(2).getHashData())){
                        processDefinition = processDefinitionManager.findMainProcessDefinitionNamed(strHashedInfoSeq.get(1).getHashData());
                    }
                    //Other
                    else {
                        Validations.check(retCodeConfig.getInvalidPDLevel(),strHashedInfoSeq.get(2).getHashData());
                    }
                    CimProcessFlow activeProcessFlow = null;
                    if (null != processDefinition){
                         activeProcessFlow = processDefinition.getActiveProcessFlow();
                    }
                    if (null != activeProcessFlow){
                        t = activeProcessFlow.findProcessOperationSpecificationOnDefault(strHashedInfoSeq.get(0).getHashData());
                    }
                }
            }
        }
        //PosProcessResource
        else if(CimStringUtils.equals(objObjectGetIn.getClassName(),BizConstant.SP_CLASSNAME_POSPROCESSRESOURCE)){
            if (CimArrayUtils.getSize(strHashedInfoSeq) == 2){
                if (CimStringUtils.isNotEmpty(strHashedInfoSeq.get(1).getHashData())) {
                    CimMachine machine = baseCoreFactory.getBOByIdentifier(CimMachine.class, strHashedInfoSeq.get(1).getHashData());
                    if (null != machine && CimStringUtils.isNotEmpty(strHashedInfoSeq.get(0).getHashData())) {
                        t = machine.findProcessResourceNamed(strHashedInfoSeq.get(0).getHashData());
                    }
                }
            }
        }
        //PosQTimeRestriction
        else if(CimStringUtils.equals(objObjectGetIn.getClassName(),BizConstant.SP_CLASSNAME_POSQTIMERESTRICTION)){
            if (CimArrayUtils.getSize(strHashedInfoSeq) == 5){
                if (CimStringUtils.isNotEmpty(strHashedInfoSeq.get(2).getHashData())){
                    CimLot lot = baseCoreFactory.getBOByIdentifier(CimLot.class, strHashedInfoSeq.get(2).getHashData());
                    if (null != lot && CimStringUtils.isNotEmpty(strHashedInfoSeq.get(3).getHashData())
                        && CimStringUtils.isNotEmpty(strHashedInfoSeq.get(0).getHashData())
                        && CimStringUtils.isNotEmpty(strHashedInfoSeq.get(4).getHashData())
                        && CimStringUtils.isNotEmpty(strHashedInfoSeq.get(1).getHashData()) ){
                        t = lot.findQTimeRestrictionNamed(strHashedInfoSeq.get(3).getHashData(),
                                strHashedInfoSeq.get(0).getHashData(),
                                strHashedInfoSeq.get(4).getHashData(),
                                strHashedInfoSeq.get(1).getHashData());
                    }
                }
            }
        }

        //PosQTimeRestrictionByWafer
        else if(CimStringUtils.equals(objObjectGetIn.getClassName(),BizConstant.SP_CLASSNAME_POSQTIMERESTRICTIONBYWAFER)){
            if (CimArrayUtils.getSize(strHashedInfoSeq) == 5){
                if (CimStringUtils.isNotEmpty(strHashedInfoSeq.get(2).getHashData())){
                    CimWafer wafer = baseCoreFactory.getBOByIdentifier(CimWafer.class, strHashedInfoSeq.get(2).getHashData());
                    if (null != wafer && CimStringUtils.isNotEmpty(strHashedInfoSeq.get(3).getHashData())
                            && CimStringUtils.isNotEmpty(strHashedInfoSeq.get(0).getHashData())
                            && CimStringUtils.isNotEmpty(strHashedInfoSeq.get(4).getHashData())
                            && CimStringUtils.isNotEmpty(strHashedInfoSeq.get(1).getHashData()) ){
                        t = wafer.findQTimeRestrictionNamed(strHashedInfoSeq.get(3).getHashData(),
                                strHashedInfoSeq.get(0).getHashData(),
                                strHashedInfoSeq.get(4).getHashData(),
                                strHashedInfoSeq.get(1).getHashData());
                    }
                }
            }
        }

        //PosReticlePodPortResource
        else if(CimStringUtils.equals(objObjectGetIn.getClassName(),BizConstant.SP_CLASSNAME_POSRETICLEPODPORTRESOURCE)){
            if (CimArrayUtils.getSize(strHashedInfoSeq) == 2){
                if (CimStringUtils.isNotEmpty(strHashedInfoSeq.get(1).getHashData())) {
                    CimMachine machine = baseCoreFactory.getBOByIdentifier(CimMachine.class, strHashedInfoSeq.get(1).getHashData());
                    if (null != machine && CimStringUtils.isNotEmpty(strHashedInfoSeq.get(0).getHashData())) {
                        t = machine.findReticlePodPortResourceNamed(strHashedInfoSeq.get(0).getHashData());
                    }
                }
            }
        }
        //PosEqpMonitorJob
        else if(CimStringUtils.equals(objObjectGetIn.getClassName(),BizConstant.SP_CLASSNAME_POSEQPMONITORJOB)){
            if (CimArrayUtils.getSize(strHashedInfoSeq) == 2){
                if (CimStringUtils.isNotEmpty(strHashedInfoSeq.get(1).getHashData())) {
                    CimEqpMonitor eqpMonitor = baseCoreFactory.getBOByIdentifier(CimEqpMonitor.class, strHashedInfoSeq.get(1).getHashData());
                    if (null != eqpMonitor && CimStringUtils.isNotEmpty(strHashedInfoSeq.get(0).getHashData())) {
                        t = eqpMonitor.findEqpMonitorJobNamed(strHashedInfoSeq.get(0).getHashData());
                    }
                }
            }
        }
        //PosDurableProcessFlowContext
        else if(CimStringUtils.equals(objObjectGetIn.getClassName(),BizConstant.SP_CLASSNAME_POSDURABLEPROCESSFLOWCONTEXT)){
            if (CimArrayUtils.getSize(strHashedInfoSeq) == 2){
                if (CimStringUtils.isNotEmpty(strHashedInfoSeq.get(0).getHashData()) && CimStringUtils.isNotEmpty(strHashedInfoSeq.get(1).getHashData())) {
                    if (CimStringUtils.equals(BizConstant.SP_DURABLECAT_CASSETTE,strHashedInfoSeq.get(0).getHashData())){
                        CimCassette cassette = baseCoreFactory.getBOByIdentifier(CimCassette.class, strHashedInfoSeq.get(1).getHashData());
                        if (null != cassette){
                            t = cassette.getDurableProcessFlowContext();
                        }
                    }
                    else if (CimStringUtils.equals(BizConstant.SP_DURABLECAT_RETICLE,strHashedInfoSeq.get(0).getHashData())){
                        CimProcessDurable reticle = baseCoreFactory.getBOByIdentifier(CimProcessDurable.class, strHashedInfoSeq.get(1).getHashData());
                        if (null != reticle){
                            t = reticle.getDurableProcessFlowContext();
                        }
                    }
                    else if (CimStringUtils.equals(BizConstant.SP_DURABLECAT_RETICLEPOD,strHashedInfoSeq.get(0).getHashData())){
                        CimReticlePod reticlePod = baseCoreFactory.getBOByIdentifier(CimReticlePod.class, strHashedInfoSeq.get(1).getHashData());
                        if (null != reticlePod){
                            t = reticlePod.getDurableProcessFlowContext();
                        }
                    }
                }
            }
        }

        //PosDurableProcessOperation
        else if(CimStringUtils.equals(objObjectGetIn.getClassName(),BizConstant.SP_CLASSNAME_POSDURABLEPROCESSOPERATION)){
            if (CimArrayUtils.getSize(strHashedInfoSeq) == 4){
                if (CimStringUtils.isNotEmpty(strHashedInfoSeq.get(2).getHashData())
                        && CimStringUtils.isNotEmpty(strHashedInfoSeq.get(3).getHashData())
                        && CimStringUtils.isNotEmpty(strHashedInfoSeq.get(1).getHashData())
                        && CimStringUtils.isNotEmpty(strHashedInfoSeq.get(0).getHashData())) {
                    if (CimStringUtils.equals(BizConstant.SP_DURABLECAT_CASSETTE,strHashedInfoSeq.get(2).getHashData())){
                        CimCassette cassette = baseCoreFactory.getBOByIdentifier(CimCassette.class, strHashedInfoSeq.get(3).getHashData());
                        if (null != cassette){
                            CimDurableProcessFlowContext durableProcessFlowContext = cassette.getDurableProcessFlowContext();
                            if (null != durableProcessFlowContext){
                                t = durableProcessFlowContext.findProcessOperationForRouteOperationNumberBefore(strHashedInfoSeq.get(1).getHashData(),strHashedInfoSeq.get(0).getHashData());
                            }
                        }
                    }
                    else if (CimStringUtils.equals(BizConstant.SP_DURABLECAT_RETICLE,strHashedInfoSeq.get(2).getHashData())){
                        CimProcessDurable reticle = baseCoreFactory.getBOByIdentifier(CimProcessDurable.class, strHashedInfoSeq.get(3).getHashData());
                        if (null != reticle){
                            CimDurableProcessFlowContext durableProcessFlowContext = reticle.getDurableProcessFlowContext();
                            if (null != durableProcessFlowContext){
                                t = durableProcessFlowContext.findProcessOperationForRouteOperationNumberBefore(strHashedInfoSeq.get(1).getHashData(),strHashedInfoSeq.get(0).getHashData());
                            }
                        }
                    }
                    else if (CimStringUtils.equals(BizConstant.SP_DURABLECAT_RETICLEPOD,strHashedInfoSeq.get(2).getHashData())){
                        CimReticlePod reticlePod = baseCoreFactory.getBOByIdentifier(CimReticlePod.class, strHashedInfoSeq.get(3).getHashData());
                        if (null != reticlePod){
                            CimDurableProcessFlowContext durableProcessFlowContext = reticlePod.getDurableProcessFlowContext();
                            if (null != durableProcessFlowContext){
                                t = durableProcessFlowContext.findProcessOperationForRouteOperationNumberBefore(strHashedInfoSeq.get(1).getHashData(),strHashedInfoSeq.get(0).getHashData());
                            }
                        }
                    }
                }
            }
        }
        //normal function
        else {
            String condition = "";
            for (Infos.HashedInfo hashedInfo : strHashedInfoSeq) {
                if (!CimStringUtils.isEmpty(condition)) {
                    condition = condition + " and ";
                }
                condition = condition + String.format("%s = '%s'", hashedInfo.getHashKey(), hashedInfo.getHashData());
            }
            Class classType = tableMap.get(objObjectGetIn.getClassName());

            Table table = (Table) baseCoreFactory.getEntityClass(classType).getAnnotation(Table.class);
            String tableName = table.name();
            String sql = String.format("select * from %s where %s", tableName, condition);
            t = baseCoreFactory.getBOByCustom(classType, sql);
        }

        Validations.check(t == null, retCodeConfig.getNotFoundObject());
        return (T)t;
    }

    @Override
    public <T extends com.fa.cim.newcore.bo.CimBO> void objectUserDataSet(Infos.ObjCommon objCommon, Infos.UserData userData, T bo) {
        List<GlobalDTO.UserDataSet> userDataSetList = new ArrayList<>();
        GlobalDTO.UserDataSet userDataSet = new GlobalDTO.UserDataSet();
        userDataSet.setName(userData.getName());
        userDataSet.setType(userData.getType());
        userDataSet.setOriginator(userData.getOriginator());
        userDataSet.setValue(userData.getValue());
        userDataSetList.add(userDataSet);
        bo.setUserDataSetNamedAndOrig(userDataSetList);
    }

    @Override
    public <T extends com.fa.cim.newcore.bo.CimBO> void objectUserDataRemove(Infos.ObjCommon objCommon, Infos.UserData userData, T bo) {
        bo.removeUserDataSetNamedAndOrig(userData.getName(), userData.getOriginator());
    }

    @Override
    public <T extends com.fa.cim.newcore.bo.CimBO> Outputs.ObjectClassIDInfoGetDROut ObjectClassIDInfoGetDR(Infos.ObjCommon objCommon, T anObject) {
        Outputs.ObjectClassIDInfoGetDROut out = new Outputs.ObjectClassIDInfoGetDROut();
        //anObject      = reactivateCIMFWBO( aStringObjRef );
        Validations.check(CimObjectUtils.isEmpty(anObject), retCodeConfig.getNotFoundObject());
        String aStringObjRef = anObject.getPrimaryKey();
        List<Infos.HashedInfo> strHashedInfoSeq = new ArrayList<>();
        String simpleName = anObject.getClass().getSimpleName();
        int index = simpleName.indexOf("BO");
        String aClassName = simpleName.substring(0, index).replaceAll("Cim","Pos");
        if (CimStringUtils.isEmpty(aClassName)){
            log.debug("Unknown Class");
            aClassName = "Unknown Class";
        }
        //PosProcessDurable
        if(CimStringUtils.equals(aClassName, "PosProcessDurable")){
            log.debug("ClassName = PosProcessDurable -> PosReticle");
            aClassName = BizConstant.SP_CLASSNAME_POSRETICLE;
        }

        //PosArea
        if (anObject instanceof com.fa.cim.newcore.bo.factory.CimArea){
            com.fa.cim.newcore.bo.factory.CimArea cimArea = (com.fa.cim.newcore.bo.factory.CimArea) anObject;
            getHashedInfoNew(cimArea.getIdentifier(),strHashedInfoSeq,aClassName, BizConstant.SP_CLASSNAME_POSAREA,new HashedInfoHashKey(BizConstant.SP_HASHDATA_AREA_ID));
        }

        //PosPlannedSplitJob
        if (anObject instanceof com.fa.cim.newcore.bo.product.CimPlannedSplitJob){
            com.fa.cim.newcore.bo.product.CimPlannedSplitJob cimPlannedSplitJob = (com.fa.cim.newcore.bo.product.CimPlannedSplitJob) anObject;
            getHashedInfoNew(cimPlannedSplitJob.getIdentifier(),strHashedInfoSeq,aClassName, BizConstant.SP_CLASSNAME_POSPLANNEDSPLITJOB,new HashedInfoHashKey(BizConstant.SP_HASHDATA_PLSPLITJOB_ID));
        }

        //PosControlJob
        if (anObject instanceof com.fa.cim.newcore.bo.product.CimControlJob){
            com.fa.cim.newcore.bo.product.CimControlJob cimControlJob = (com.fa.cim.newcore.bo.product.CimControlJob) anObject;
            getHashedInfoNew(cimControlJob.getIdentifier(),strHashedInfoSeq,aClassName, BizConstant.SP_CLASSNAME_POSCONTROLJOB,new HashedInfoHashKey(BizConstant.SP_HASHDATA_CTRLJOB_ID));
        }
        //PosAreaGroup
        if (anObject instanceof com.fa.cim.newcore.bo.factory.CimAreaGroup){
            com.fa.cim.newcore.bo.factory.CimAreaGroup cimAreaGroup = (com.fa.cim.newcore.bo.factory.CimAreaGroup) anObject;
            getHashedInfoNew(cimAreaGroup.getIdentifier(),strHashedInfoSeq,aClassName, BizConstant.SP_CLASSNAME_POSAREAGROUP,new HashedInfoHashKey(BizConstant.SP_HASHDATA_AREAGRP_ID));
        }

        //PosBank
        if (anObject instanceof com.fa.cim.newcore.bo.factory.CimBank){
            com.fa.cim.newcore.bo.factory.CimBank cimBank = (com.fa.cim.newcore.bo.factory.CimBank) anObject;
            getHashedInfoNew(cimBank.getIdentifier(),strHashedInfoSeq,aClassName, BizConstant.SP_CLASSNAME_POSBANK,new HashedInfoHashKey(BizConstant.SP_HASHDATA_BANK_ID));
        }

        //PosBinDefinition
        if (anObject instanceof com.fa.cim.newcore.bo.prodspec.CimBinDefinition){
            com.fa.cim.newcore.bo.prodspec.CimBinDefinition cimBinDefinition = (com.fa.cim.newcore.bo.prodspec.CimBinDefinition) anObject;
            getHashedInfoNew(cimBinDefinition.getIdentifier(),strHashedInfoSeq,aClassName, BizConstant.SP_CLASSNAME_POSBINDEFINITION,new HashedInfoHashKey(BizConstant.SP_HASHDATA_BINDEF_ID));
        }

        //PosBinSpecification
        if (anObject instanceof com.fa.cim.newcore.bo.prodspec.CimBinSpecification){
            com.fa.cim.newcore.bo.prodspec.CimBinSpecification cimBinSpecification = (com.fa.cim.newcore.bo.prodspec.CimBinSpecification) anObject;
            getHashedInfoNew(cimBinSpecification.getIdentifier(),strHashedInfoSeq,aClassName, BizConstant.SP_CLASSNAME_POSBINSPECIFICATION,new HashedInfoHashKey(BizConstant.SP_HASHDATA_BINSPEC_ID));
        }

        //PosBOM
        if (anObject instanceof com.fa.cim.newcore.bo.parts.CimBom){
            com.fa.cim.newcore.bo.parts.CimBom cimBom = (com.fa.cim.newcore.bo.parts.CimBom) anObject;
            getHashedInfoNew(cimBom.getIdentifier(),strHashedInfoSeq,aClassName, BizConstant.SP_CLASSNAME_POSBOM,new HashedInfoHashKey(BizConstant.SP_HASHDATA_BOM_ID));
        }

        //PosBufferResource
        if (CimStringUtils.equals(aClassName,BizConstant.SP_CLASSNAME_POSBUFFERRESOURCE)){
            if (anObject instanceof com.fa.cim.newcore.bo.machine.CimBufferResource){
                com.fa.cim.newcore.bo.machine.CimBufferResource cimBufferResource = (com.fa.cim.newcore.bo.machine.CimBufferResource) anObject;
                getHashedInfoNew(cimBufferResource.getIdentifier(),strHashedInfoSeq,aClassName, BizConstant.SP_CLASSNAME_POSBUFFERRESOURCE,new HashedInfoHashKey(BizConstant.SP_HASHDATA_BUFFRSC_ID));
                Machine machine = cimBufferResource.getMachine();
                getHashedInfoNew(machine.getIdentifier(),strHashedInfoSeq,BizConstant.SP_CLASSNAME_POSMACHINE, BizConstant.SP_CLASSNAME_POSMACHINE,new HashedInfoHashKey(BizConstant.SP_HASHDATA_EQP_ID));
            }
        }
        //PosCalendarDate
        if (anObject instanceof com.fa.cim.newcore.bo.factory.CimCalendarDate){
            com.fa.cim.newcore.bo.factory.CimCalendarDate cimCalendarDate = (com.fa.cim.newcore.bo.factory.CimCalendarDate) anObject;
            getHashedInfoNew(cimCalendarDate.getIdentifier(),strHashedInfoSeq,aClassName, BizConstant.SP_CLASSNAME_POSCALENDARDATE,new HashedInfoHashKey(BizConstant.SP_HASHDATA_CALENDAR_DATE));
        }
        //PosCassette
        if (anObject instanceof com.fa.cim.newcore.bo.durable.CimCassette){
            com.fa.cim.newcore.bo.durable.CimCassette cimCassette = (com.fa.cim.newcore.bo.durable.CimCassette) anObject;
            getHashedInfoNew(cimCassette.getIdentifier(),strHashedInfoSeq,aClassName, BizConstant.SP_CLASSNAME_POSCASSETTE,new HashedInfoHashKey(BizConstant.SP_HASHDATA_CAST_ID));
        }
        //PosCode
        if (anObject instanceof com.fa.cim.newcore.bo.code.CimCode){
            com.fa.cim.newcore.bo.code.CimCode cimCode = (com.fa.cim.newcore.bo.code.CimCode) anObject;
            getHashedInfoNew(cimCode.getIdentifier(),strHashedInfoSeq,aClassName, BizConstant.SP_CLASSNAME_POSCODE,new HashedInfoHashKey(BizConstant.SP_HASHDATA_CODE_ID));
        }
        //PosControlJob
        if (anObject instanceof com.fa.cim.newcore.bo.product.CimControlJob){
            com.fa.cim.newcore.bo.product.CimControlJob cimControlJob = (com.fa.cim.newcore.bo.product.CimControlJob) anObject;
            getHashedInfoNew(cimControlJob.getIdentifier(),strHashedInfoSeq,aClassName, BizConstant.SP_CLASSNAME_POSCONTROLJOB,new HashedInfoHashKey(BizConstant.SP_HASHDATA_CTRLJOB_ID));
        }
        //PosCustomer
        if (anObject instanceof com.fa.cim.newcore.bo.prodspec.CimCustomer){
            com.fa.cim.newcore.bo.prodspec.CimCustomer cimCustomer = (com.fa.cim.newcore.bo.prodspec.CimCustomer) anObject;
            getHashedInfoNew(cimCustomer.getIdentifier(),strHashedInfoSeq,aClassName, BizConstant.SP_CLASSNAME_POSCUSTOMER,new HashedInfoHashKey(BizConstant.SP_HASHDATA_CUSTOMER_ID));
        }
        //PosCustomerProduct
        if (CimStringUtils.equals(aClassName,BizConstant.SP_CLASSNAME_POSCUSTOMERPRODUCT)){
            if (anObject instanceof com.fa.cim.newcore.bo.prodspec.CimCustomerProduct){
                com.fa.cim.newcore.bo.prodspec.CimCustomerProduct cimCustomerProduct = (com.fa.cim.newcore.bo.prodspec.CimCustomerProduct) anObject;
                //get parent PosCustomer
                com.fa.cim.newcore.bo.prodspec.CimCustomer cimCustomer = cimCustomerProduct.getCustomer();
                getHashedInfoNew(cimCustomer.getIdentifier(),strHashedInfoSeq,BizConstant.SP_CLASSNAME_POSCUSTOMER,BizConstant.SP_CLASSNAME_POSCUSTOMER,new HashedInfoHashKey(BizConstant.SP_HASHDATA_CUSTOMER_ID));
                //get parent PosProductSpecification
                com.fa.cim.newcore.bo.prodspec.CimProductSpecification cimProductSpecification = cimCustomerProduct.getProductSpecification();
                getHashedInfoNew(cimProductSpecification.getIdentifier(),strHashedInfoSeq,BizConstant.SP_CLASSNAME_POSPRODUCTSPECIFICATION,BizConstant.SP_CLASSNAME_POSPRODUCTSPECIFICATION,new HashedInfoHashKey(BizConstant.SP_HASHDATA_PRODSPEC_ID));
            }
        }

        //PosDataCollectionDefinition
        if (anObject instanceof com.fa.cim.newcore.bo.dc.CimDataCollectionDefinition){
            com.fa.cim.newcore.bo.dc.CimDataCollectionDefinition cimDataCollectionDefinition = (com.fa.cim.newcore.bo.dc.CimDataCollectionDefinition) anObject;
            getHashedInfoNew(cimDataCollectionDefinition.getIdentifier(),strHashedInfoSeq,aClassName, BizConstant.SP_CLASSNAME_POSDATACOLLECTIONDEFINITION,new HashedInfoHashKey(BizConstant.SP_HASHDATA_DCDEF_ID));
        }

        //PosDataCollectionSpecification
        if (anObject instanceof com.fa.cim.newcore.bo.dc.CimDataCollectionSpecification){
            com.fa.cim.newcore.bo.dc.CimDataCollectionSpecification cimDataCollectionSpecification = (com.fa.cim.newcore.bo.dc.CimDataCollectionSpecification) anObject;
            getHashedInfoNew(cimDataCollectionSpecification.getIdentifier(),strHashedInfoSeq,aClassName, BizConstant.SP_CLASSNAME_POSDATACOLLECTIONSPECIFICATION,new HashedInfoHashKey(BizConstant.SP_HASHDATA_DCSPEC_ID));
        }
        //PosDispatcher
        if(CimStringUtils.equals(BizConstant.SP_CLASSNAME_POSDISPATCHER,aClassName)){
            if (anObject instanceof CimDispatcher){
                CimDispatcher cimDispatcher = (CimDispatcher) anObject;
                CimMachine cimMachine = baseCoreFactory.getBO(CimMachine.class, cimDispatcher.getPrimaryKey());
                Validations.check(CimObjectUtils.isEmpty(cimMachine), retCodeConfig.getNotFoundEqp());
                Infos.HashedInfo hashedInfo = new Infos.HashedInfo();
                hashedInfo.setHashKey(BizConstant.SP_HASHDATA_EQP_ID);
                hashedInfo.setHashData(cimMachine.getIdentifier());
                strHashedInfoSeq.add(hashedInfo);
            }
        }
        //PosReticle
        if (anObject instanceof com.fa.cim.newcore.bo.durable.CimProcessDurable){
            com.fa.cim.newcore.bo.durable.CimProcessDurable cimDurable = (com.fa.cim.newcore.bo.durable.CimProcessDurable) anObject;
            getHashedInfoNew(cimDurable.getIdentifier(),strHashedInfoSeq,aClassName, BizConstant.SP_CLASSNAME_POSRETICLE,new HashedInfoHashKey(BizConstant.SP_HASHDATA_DRBL_ID));
        }
        //PosProcessDurableCapability
        if (anObject instanceof com.fa.cim.newcore.bo.durable.CimProcessDurableCapability){
            com.fa.cim.newcore.bo.durable.CimProcessDurableCapability cimProcessDurableCapability = (com.fa.cim.newcore.bo.durable.CimProcessDurableCapability) anObject;
            getHashedInfoNew(cimProcessDurableCapability.getIdentifier(),strHashedInfoSeq,aClassName, BizConstant.SP_CLASSNAME_POSPROCESSDURABLECAPABILITY,new HashedInfoHashKey(BizConstant.SP_HASHDATA_DRBLGRP_ID));
        }
        //PosE10State
        if (anObject instanceof com.fa.cim.newcore.bo.code.CimE10State){
            com.fa.cim.newcore.bo.code.CimE10State cimE10State = (com.fa.cim.newcore.bo.code.CimE10State) anObject;
            getHashedInfoNew(cimE10State.getIdentifier(),strHashedInfoSeq,aClassName, BizConstant.SP_CLASSNAME_POSE10STATE,new HashedInfoHashKey(BizConstant.SP_HASHDATA_E10STATE_ID));
        }
        //PosEntityInhibit
        if (CimStringUtils.equals(aClassName,BizConstant.SP_CLASSNAME_POSENTITYINHIBIT)){
            if (anObject instanceof CimRestriction){
                CimRestriction cimEntityInhibit = (CimRestriction) anObject;
                Constrain.EntityInhibitRecord inhibitRecord = cimEntityInhibit.getInhibitRecord();
                String anInhibitID = inhibitRecord.getId();
                Infos.HashedInfo hashedInfo = new Infos.HashedInfo();
                hashedInfo.setHashKey(BizConstant.SP_HASHDATA_INHIBIT_ID);
                hashedInfo.setHashData(anInhibitID);
                strHashedInfoSeq.add(hashedInfo);
            }
        }
        //PosMachine
        if (anObject instanceof CimMachine){
            CimMachine cimMachine = (CimMachine) anObject;
            getHashedInfoNew(cimMachine.getIdentifier(),strHashedInfoSeq,aClassName, BizConstant.SP_CLASSNAME_POSMACHINE,new HashedInfoHashKey(BizConstant.SP_HASHDATA_EQP_ID));
        }
        //PosMachineContainer
        if (CimStringUtils.equals(aClassName,BizConstant.SP_CLASSNAME_POSMACHINECONTAINER)){
            if (anObject instanceof com.fa.cim.newcore.bo.machine.CimMachineContainer){
                com.fa.cim.newcore.bo.machine.CimMachineContainer cimMachineContainer = (com.fa.cim.newcore.bo.machine.CimMachineContainer) anObject;
                getHashedInfoNew(cimMachineContainer.getIdentifier(),strHashedInfoSeq,aClassName, BizConstant.SP_CLASSNAME_POSMACHINECONTAINER,new HashedInfoHashKey(BizConstant.SP_HASHDATA_EQPCTN_ID));
                // getSuper PosMachine
                CimMachine machine = cimMachineContainer.getMachine();
                getHashedInfoNew(machine.getIdentifier(),strHashedInfoSeq,BizConstant.SP_CLASSNAME_POSMACHINE, BizConstant.SP_CLASSNAME_POSMACHINE,new HashedInfoHashKey(BizConstant.SP_HASHDATA_EQP_ID));
            }
        }
        //PosMachineContainerPosition
        if (CimStringUtils.equals(aClassName,BizConstant.SP_CLASSNAME_POSMACHINECONTAINERPOSITION)){
            if (anObject instanceof com.fa.cim.newcore.bo.machine.CimMachineContainerPosition){
                com.fa.cim.newcore.bo.machine.CimMachineContainerPosition cimMachineContainerPosition = (com.fa.cim.newcore.bo.machine.CimMachineContainerPosition) anObject;
                getHashedInfoNew(cimMachineContainerPosition.getIdentifier(),strHashedInfoSeq,aClassName, BizConstant.SP_CLASSNAME_POSMACHINECONTAINER,new HashedInfoHashKey(BizConstant.SP_HASHDATA_EQPCTN_ID));
                //getSuper PosMachine
                CimMachine machine = cimMachineContainerPosition.getMachine();
                getHashedInfoNew(machine.getIdentifier(),strHashedInfoSeq,BizConstant.SP_CLASSNAME_POSMACHINE, BizConstant.SP_CLASSNAME_POSMACHINE,new HashedInfoHashKey(BizConstant.SP_HASHDATA_EQP_ID));
                //getSuper PosMahineContainer
                com.fa.cim.newcore.bo.machine.CimMachineContainer cimMachineContainer = cimMachineContainerPosition.getMachineContainer();
                getHashedInfoNew(cimMachineContainer.getIdentifier(),strHashedInfoSeq,BizConstant.SP_CLASSNAME_POSMACHINECONTAINER, BizConstant.SP_CLASSNAME_POSMACHINECONTAINER,new HashedInfoHashKey(BizConstant.SP_HASHDATA_EQPCTN_ID));
            }
        }
        //PosMachineNote
        if (CimStringUtils.equals(aClassName,BizConstant.SP_CLASSNAME_POSMACHINENOTE)){
            if (anObject instanceof com.fa.cim.newcore.bo.machine.CimMachineNote){
                com.fa.cim.newcore.bo.machine.CimMachineNote cimMachineNote = (com.fa.cim.newcore.bo.machine.CimMachineNote) anObject;
                getHashedInfoNew(cimMachineNote.getTitle(),strHashedInfoSeq,aClassName, BizConstant.SP_CLASSNAME_POSMACHINENOTE,new HashedInfoHashKey(BizConstant.SP_HASHDATA_NOTE_TITLE));
                getHashedInfoNew(cimMachineNote.getPersonID(),strHashedInfoSeq,aClassName, BizConstant.SP_CLASSNAME_POSMACHINENOTE,new HashedInfoHashKey(BizConstant.SP_HASHDATA_OWNER_ID));
                getHashedInfoNew(CimDateUtils.convertToSpecString(cimMachineNote.getCreatedTimeStamp()),strHashedInfoSeq,aClassName, BizConstant.SP_CLASSNAME_POSMACHINENOTE,new HashedInfoHashKey(BizConstant.SP_HASHDATA_CREATED_TIME));
                //getSuper Machine
                Machine machine = cimMachineNote.getMachine();
                getHashedInfoNew(machine.getIdentifier(),strHashedInfoSeq,BizConstant.SP_CLASSNAME_POSMACHINE, BizConstant.SP_CLASSNAME_POSMACHINE,new HashedInfoHashKey(BizConstant.SP_HASHDATA_EQP_ID));
            }
        }
        //PosMachineOperationProcedure
        if (CimStringUtils.equals(aClassName,BizConstant.SP_CLASSNAME_POSMACHINEOPERATIONPROCEDURE)){
            if (anObject instanceof com.fa.cim.newcore.bo.machine.CimMachineOperationProcedure){
                com.fa.cim.newcore.bo.machine.CimMachineOperationProcedure cimMachineOperationProcedure = (com.fa.cim.newcore.bo.machine.CimMachineOperationProcedure) anObject;
                getHashedInfoNew(cimMachineOperationProcedure.getTitle(),strHashedInfoSeq,aClassName, BizConstant.SP_CLASSNAME_POSMACHINEOPERATIONPROCEDURE,new HashedInfoHashKey(BizConstant.SP_HASHDATA_NOTE_TITLE));
                getHashedInfoNew(cimMachineOperationProcedure.getPersonID(),strHashedInfoSeq,aClassName, BizConstant.SP_CLASSNAME_POSMACHINEOPERATIONPROCEDURE,new HashedInfoHashKey(BizConstant.SP_HASHDATA_OWNER_ID));
                getHashedInfoNew(CimDateUtils.convertToSpecString(cimMachineOperationProcedure.getCreatedTimeStamp()),strHashedInfoSeq,aClassName, BizConstant.SP_CLASSNAME_POSMACHINEOPERATIONPROCEDURE,new HashedInfoHashKey(BizConstant.SP_HASHDATA_CREATED_TIME));
                //getSuper Machine
                Machine machine = cimMachineOperationProcedure.getMachine();
                getHashedInfoNew(machine.getIdentifier(),strHashedInfoSeq,BizConstant.SP_CLASSNAME_POSMACHINE, BizConstant.SP_CLASSNAME_POSMACHINE,new HashedInfoHashKey(BizConstant.SP_HASHDATA_EQP_ID));
            }
        }
        //PosMachineState
        if (CimStringUtils.equals(aClassName,BizConstant.SP_CLASSNAME_POSMACHINESTATE)){
            if (anObject instanceof com.fa.cim.newcore.bo.code.CimMachineState){
                com.fa.cim.newcore.bo.code.CimMachineState cimMachineState = (com.fa.cim.newcore.bo.code.CimMachineState) anObject;
                getHashedInfoNew(cimMachineState.getIdentifier(),strHashedInfoSeq,aClassName, BizConstant.SP_CLASSNAME_POSMACHINESTATE,new HashedInfoHashKey(BizConstant.SP_HASHDATA_EQPCTN_ID));
                //getSuper PosE10State
                com.fa.cim.newcore.bo.code.CimE10State cimE10State = cimMachineState.getE10State();
                getHashedInfoNew(cimE10State.getIdentifier(),strHashedInfoSeq,BizConstant.SP_CLASSNAME_POSE10STATE, BizConstant.SP_CLASSNAME_POSE10STATE,new HashedInfoHashKey(BizConstant.SP_HASHDATA_E10STATE_ID));

            }
        }
        //PosFactoryNote
        if (anObject instanceof com.fa.cim.newcore.bo.factory.CimFactoryNote){
            com.fa.cim.newcore.bo.factory.CimFactoryNote cimFactoryNote = (com.fa.cim.newcore.bo.factory.CimFactoryNote) anObject;
            getHashedInfoNew(cimFactoryNote.getPersonID(),strHashedInfoSeq,aClassName, BizConstant.SP_CLASSNAME_POSFACTORYNOTE,new HashedInfoHashKey(BizConstant.SP_HASHDATA_OWNER_ID));
            getHashedInfoNew(CimDateUtils.convertToSpecString(cimFactoryNote.getCreatedTimeStamp()),strHashedInfoSeq,aClassName, BizConstant.SP_CLASSNAME_POSFACTORYNOTE,new HashedInfoHashKey(BizConstant.SP_HASHDATA_CREATED_TIME));
        }

        //PosFlowBatch
        if (anObject instanceof com.fa.cim.newcore.bo.dispatch.CimFlowBatch){
            com.fa.cim.newcore.bo.dispatch.CimFlowBatch cimFlowBatch = (com.fa.cim.newcore.bo.dispatch.CimFlowBatch) anObject;
            getHashedInfoNew(cimFlowBatch.getIdentifier(),strHashedInfoSeq,aClassName, BizConstant.SP_CLASSNAME_POSFLOWBATCH,new HashedInfoHashKey(BizConstant.SP_HASHDATA_FLOWBATCH_ID));
        }

        //PosFlowBatchDispatcher does not have ID.

        //PosFutureReworkRequest
        if (anObject instanceof com.fa.cim.newcore.bo.product.CimFutureReworkRequest){
            com.fa.cim.newcore.bo.product.CimFutureReworkRequest cimFutureReworkRequest = (com.fa.cim.newcore.bo.product.CimFutureReworkRequest) anObject;
            getHashedInfoNew(cimFutureReworkRequest.getIdentifier(),strHashedInfoSeq,aClassName, BizConstant.SP_CLASSNAME_POSFUTUREREWORKREQUEST,new HashedInfoHashKey(BizConstant.SP_HASHDATA_FUTUREREWORK_ID));
        }

        //PosLot
        if (anObject instanceof com.fa.cim.newcore.bo.product.CimLot){
            com.fa.cim.newcore.bo.product.CimLot cimLot = (com.fa.cim.newcore.bo.product.CimLot) anObject;
            getHashedInfoNew(cimLot.getIdentifier(),strHashedInfoSeq,aClassName, BizConstant.SP_CLASSNAME_POSLOT,new HashedInfoHashKey(BizConstant.SP_HASHDATA_LOT_ID));
        }

        //PosLotComment
        if (CimStringUtils.equals(aClassName,BizConstant.SP_CLASSNAME_POSLOTCOMMENT)){
            if (anObject instanceof com.fa.cim.newcore.bo.product.CimLotComment){
                com.fa.cim.newcore.bo.product.CimLotComment cimLotComment = (com.fa.cim.newcore.bo.product.CimLotComment) anObject;
                //get parent cimLot
                com.fa.cim.newcore.bo.product.CimLot lot = cimLotComment.getLot();
                getHashedInfoNew(lot.getIdentifier(),strHashedInfoSeq,BizConstant.SP_CLASSNAME_POSLOT, BizConstant.SP_CLASSNAME_POSLOT,new HashedInfoHashKey(BizConstant.SP_HASHDATA_LOT_ID));
            }
        }
        //PosLotFamily
        if (anObject instanceof com.fa.cim.newcore.bo.product.CimLotFamily){
            com.fa.cim.newcore.bo.product.CimLotFamily cimLotFamily = (com.fa.cim.newcore.bo.product.CimLotFamily) anObject;
            getHashedInfoNew(cimLotFamily.getIdentifier(),strHashedInfoSeq,aClassName, BizConstant.SP_CLASSNAME_POSLOTFAMILY,new HashedInfoHashKey(BizConstant.SP_HASHDATA_LOTFAMILY_ID));
        }
        //PosLotNote
        if (CimStringUtils.equals(aClassName,BizConstant.SP_CLASSNAME_POSLOTNOTE)){
            if (anObject instanceof com.fa.cim.newcore.bo.product.CimLotNote){
                com.fa.cim.newcore.bo.product.CimLotNote cimLotNote = (com.fa.cim.newcore.bo.product.CimLotNote) anObject;
                getHashedInfoNew(cimLotNote.getPersonID(),strHashedInfoSeq,aClassName, BizConstant.SP_CLASSNAME_POSLOTNOTE,new HashedInfoHashKey(BizConstant.SP_HASHDATA_OWNER_ID));
                getHashedInfoNew(CimDateUtils.convertToSpecString(cimLotNote.getCreatedTimeStamp()),strHashedInfoSeq,aClassName, BizConstant.SP_CLASSNAME_POSLOTNOTE,new HashedInfoHashKey(BizConstant.SP_HASHDATA_CREATED_TIME));
                //get parent cimLot
                com.fa.cim.newcore.bo.product.CimLot lot = cimLotNote.getLot();
                getHashedInfoNew(lot.getIdentifier(),strHashedInfoSeq,BizConstant.SP_CLASSNAME_POSLOT, BizConstant.SP_CLASSNAME_POSLOT,new HashedInfoHashKey(BizConstant.SP_HASHDATA_LOT_ID));
            }
        }
        //PosLotOperationNote
        if (CimStringUtils.equals(aClassName,BizConstant.SP_CLASSNAME_POSLOTOPERATIONNOTE)){
            if (anObject instanceof com.fa.cim.newcore.bo.product.CimLotOperationNote){
                com.fa.cim.newcore.bo.product.CimLotOperationNote cimLotOperationNote = (com.fa.cim.newcore.bo.product.CimLotOperationNote) anObject;
                getHashedInfoNew(cimLotOperationNote.getPersonID(),strHashedInfoSeq,aClassName, BizConstant.SP_CLASSNAME_POSLOTOPERATIONNOTE,new HashedInfoHashKey(BizConstant.SP_HASHDATA_OWNER_ID));
                getHashedInfoNew(CimDateUtils.convertToSpecString(cimLotOperationNote.getCreatedTimeStamp()),strHashedInfoSeq,aClassName, BizConstant.SP_CLASSNAME_POSLOTOPERATIONNOTE,new HashedInfoHashKey(BizConstant.SP_HASHDATA_CREATED_TIME));
                getHashedInfoNew(cimLotOperationNote.getOperationNumber(),strHashedInfoSeq,aClassName, BizConstant.SP_CLASSNAME_POSLOTOPERATIONNOTE,new HashedInfoHashKey(BizConstant.SP_HASHDATA_OPE_NO));
                //get parent cimLot
                com.fa.cim.newcore.bo.product.CimLot lot = cimLotOperationNote.getLot();
                getHashedInfoNew(lot.getIdentifier(),strHashedInfoSeq,BizConstant.SP_CLASSNAME_POSLOT, BizConstant.SP_CLASSNAME_POSLOT,new HashedInfoHashKey(BizConstant.SP_HASHDATA_LOT_ID));

                //get parent cimProcessDefinition
                com.fa.cim.newcore.bo.pd.CimProcessDefinition cimProcessDefinition = cimLotOperationNote.getMainProcessDefinition();
                getHashedInfoNew(cimProcessDefinition.getIdentifier(),strHashedInfoSeq,BizConstant.SP_CLASSNAME_POSPROCESSDEFINITION, BizConstant.SP_CLASSNAME_POSPROCESSDEFINITION,new HashedInfoHashKey(BizConstant.SP_HASHDATA_MAINPD_ID));
            }
        }
        //PosLotOperationSchedule
        if (CimStringUtils.equals(aClassName,BizConstant.SP_CLASSNAME_POSLOTOPERATIONSCHEDULE)){
            if (anObject instanceof com.fa.cim.newcore.bo.planning.CimLotOperationSchedule){
                com.fa.cim.newcore.bo.planning.CimLotOperationSchedule cimLotOperationSchedule = (com.fa.cim.newcore.bo.planning.CimLotOperationSchedule) anObject;
                getHashedInfoNew(cimLotOperationSchedule.getOperationNumber(),strHashedInfoSeq,aClassName, BizConstant.SP_CLASSNAME_POSLOTOPERATIONSCHEDULE,new HashedInfoHashKey(BizConstant.SP_HASHDATA_OPE_NO));
                //get parent
                com.fa.cim.newcore.bo.CimBO objectManager = cimLotOperationSchedule.getObjectManager();
                if (objectManager instanceof com.fa.cim.newcore.bo.planning.CimLotSchedule){
                    com.fa.cim.newcore.bo.planning.CimLotSchedule cimLotSchedule = (com.fa.cim.newcore.bo.planning.CimLotSchedule) objectManager;
                    getHashedInfoNew(cimLotSchedule.getIdentifier(),strHashedInfoSeq,aClassName, BizConstant.SP_CLASSNAME_POSLOTSCHEDULE,new HashedInfoHashKey(BizConstant.SP_HASHDATA_LOTSCHE_ID));
                }
                com.fa.cim.newcore.bo.planning.CimLotSchedule cimLotSchedule = (CimLotSchedule) cimLotOperationSchedule.getObjectManager();
                getHashedInfoNew(cimLotSchedule.getIdentifier(),strHashedInfoSeq,BizConstant.SP_CLASSNAME_POSLOTSCHEDULE, BizConstant.SP_CLASSNAME_POSLOTSCHEDULE,new HashedInfoHashKey(BizConstant.SP_HASHDATA_LOTSCHE_ID));
                //get parent cimProcessDefinition
                com.fa.cim.newcore.bo.pd.CimProcessDefinition cimProcessDefinition = cimLotOperationSchedule.getMainProcessDefinition();
                getHashedInfoNew(cimProcessDefinition.getIdentifier(),strHashedInfoSeq,BizConstant.SP_CLASSNAME_POSPROCESSDEFINITION, BizConstant.SP_CLASSNAME_POSPROCESSDEFINITION,new HashedInfoHashKey(BizConstant.SP_HASHDATA_MAINPD_ID));
            }
        }
        //PosLotSchedule
        if (anObject instanceof com.fa.cim.newcore.bo.planning.CimLotSchedule){
            com.fa.cim.newcore.bo.planning.CimLotSchedule cimLotSchedule = (com.fa.cim.newcore.bo.planning.CimLotSchedule) anObject;
            getHashedInfoNew(cimLotSchedule.getIdentifier(),strHashedInfoSeq,aClassName, BizConstant.SP_CLASSNAME_POSLOTSCHEDULE,new HashedInfoHashKey(BizConstant.SP_HASHDATA_LOTSCHE_ID));
        }

        //PosLotType
        if (anObject instanceof com.fa.cim.newcore.bo.product.CimLotType){
            com.fa.cim.newcore.bo.product.CimLotType cimLotType = (com.fa.cim.newcore.bo.product.CimLotType) anObject;
            getHashedInfoNew(cimLotType.getIdentifier(),strHashedInfoSeq,aClassName, BizConstant.SP_CLASSNAME_POSLOTTYPE,new HashedInfoHashKey(BizConstant.SP_HASHDATA_LOTTYPE_ID));
        }

        //PosLogicalRecipe
        if (anObject instanceof com.fa.cim.newcore.bo.recipe.CimLogicalRecipe){
            com.fa.cim.newcore.bo.recipe.CimLogicalRecipe cimLogicalRecipe = (com.fa.cim.newcore.bo.recipe.CimLogicalRecipe) anObject;
            getHashedInfoNew(cimLogicalRecipe.getIdentifier(),strHashedInfoSeq,aClassName, BizConstant.SP_CLASSNAME_POSLOGICALRECIPE,new HashedInfoHashKey(BizConstant.SP_HASHDATA_LCRECIPE_ID));
        }

        //PosMonitorGroup
        if (anObject instanceof com.fa.cim.newcore.bo.product.CimMonitorGroup){
            com.fa.cim.newcore.bo.product.CimMonitorGroup cimMonitorGroup = (com.fa.cim.newcore.bo.product.CimMonitorGroup) anObject;
            getHashedInfoNew(cimMonitorGroup.getIdentifier(),strHashedInfoSeq,aClassName, BizConstant.SP_CLASSNAME_POSMONITORGROUP,new HashedInfoHashKey(BizConstant.SP_HASHDATA_MONITOR_GRP_ID));
        }

        //PosMachineRecipe
        if (anObject instanceof com.fa.cim.newcore.bo.recipe.CimMachineRecipe){
            com.fa.cim.newcore.bo.recipe.CimMachineRecipe cimMachineRecipe = (com.fa.cim.newcore.bo.recipe.CimMachineRecipe) anObject;
            getHashedInfoNew(cimMachineRecipe.getIdentifier(),strHashedInfoSeq,aClassName, BizConstant.SP_CLASSNAME_POSMACHINERECIPE,new HashedInfoHashKey(BizConstant.SP_HASHDATA_RECIPE_ID));
        }

        //PosMessageDefinition
        if (anObject instanceof com.fa.cim.newcore.bo.msgdistribution.CimMessageDefinition){
            com.fa.cim.newcore.bo.msgdistribution.CimMessageDefinition cimMessageDefinition = (com.fa.cim.newcore.bo.msgdistribution.CimMessageDefinition) anObject;
            getHashedInfoNew(cimMessageDefinition.getIdentifier(),strHashedInfoSeq,aClassName, BizConstant.SP_CLASSNAME_POSMESSAGEDEFINITION,new HashedInfoHashKey(BizConstant.SP_HASHDATA_MSGDEF_ID));
        }

        //PosMaterialLocation
        if (CimStringUtils.equals(aClassName,BizConstant.SP_CLASSNAME_POSMATERIALLOCATION)){
            if (anObject instanceof com.fa.cim.newcore.bo.machine.CimMaterialLocation){
                com.fa.cim.newcore.bo.machine.CimMaterialLocation cimMaterialLocation = (com.fa.cim.newcore.bo.machine.CimMaterialLocation) anObject;
                getHashedInfoNew(cimMaterialLocation.getIdentifier(),strHashedInfoSeq,aClassName, BizConstant.SP_CLASSNAME_POSMATERIALLOCATION,new HashedInfoHashKey(BizConstant.SP_HASHDATA_MTRLLOC_ID));
                //get parent cimMachineResource
                MachineResource cimMaterialLocationResource = cimMaterialLocation.getResource();
                getHashedInfoNew(cimMaterialLocationResource.resourceLevel(),strHashedInfoSeq,"MachineResource", "MachineResource",new HashedInfoHashKey(BizConstant.SP_HASHDATA_RESOURCE_TYPE));
                for (Infos.HashedInfo hashedInfo : strHashedInfoSeq) {
                    if (CimStringUtils.equals(BizConstant.SP_HASHDATA_RESOURCE_TYPE,hashedInfo.getHashKey())){
                        log.info("RESOURCE_TYPE exists");
                        if (CimStringUtils.equals(BizConstant.SP_RESOURCELEVEL_BUFFERRESOURCE,hashedInfo.getHashData())){
                            log.info("RESOURCE_TYPE is BufferResource");
                            getHashedInfoNew(cimMaterialLocationResource.getIdentifier(),strHashedInfoSeq,"MachineResource", "MachineResource",new HashedInfoHashKey(BizConstant.SP_HASHDATA_BUFFRSC_ID));
                        }else if (CimStringUtils.equals(BizConstant.SP_RESOURCELEVEL_PORTRESOURCE,hashedInfo.getHashData())){
                            log.info("RESOURCE_TYPE is PortResource");
                            getHashedInfoNew(cimMaterialLocationResource.getIdentifier(),strHashedInfoSeq,"MachineResource", "MachineResource",new HashedInfoHashKey(BizConstant.SP_HASHDATA_PORT_ID));
                        }
                        break;
                    }
                }
                //get parent cimMachine
                Machine machine = cimMaterialLocationResource.getMachine();
                getHashedInfoNew(machine.getIdentifier(),strHashedInfoSeq,BizConstant.SP_CLASSNAME_POSMACHINE, BizConstant.SP_CLASSNAME_POSMACHINE,new HashedInfoHashKey(BizConstant.SP_HASHDATA_EQP_ID));
            }
        }

        //PosMachineOperationMode
        if (anObject instanceof com.fa.cim.newcore.bo.code.CimMachineOperationMode){
            com.fa.cim.newcore.bo.code.CimMachineOperationMode cimMachineOperationMode = (com.fa.cim.newcore.bo.code.CimMachineOperationMode) anObject;
            getHashedInfoNew(cimMachineOperationMode.getIdentifier(),strHashedInfoSeq,aClassName, BizConstant.SP_CLASSNAME_POSMACHINEOPERATIONMODE,new HashedInfoHashKey(BizConstant.SP_HASHDATA_OPEMODE_ID));
        }

        //PosProcessDefinition
        if (anObject instanceof com.fa.cim.newcore.bo.pd.CimProcessDefinition){
            com.fa.cim.newcore.bo.pd.CimProcessDefinition cimProcessDefinition = (com.fa.cim.newcore.bo.pd.CimProcessDefinition) anObject;
            getHashedInfoNew(cimProcessDefinition.getIdentifier(),strHashedInfoSeq,aClassName, BizConstant.SP_CLASSNAME_POSPROCESSDEFINITION,new HashedInfoHashKey(BizConstant.SP_HASHDATA_PD_ID));
            getHashedInfoNew(cimProcessDefinition.getProcessDefinitionLevel(),strHashedInfoSeq,aClassName, BizConstant.SP_CLASSNAME_POSPROCESSDEFINITION,new HashedInfoHashKey(BizConstant.SP_HASHDATA_PD_LEVEL));
        }

        //PosProcessFlow
        if (CimStringUtils.equals(aClassName,BizConstant.SP_CLASSNAME_POSPROCESSFLOW)){
            //get prarent ProcessDefinition
            if (anObject instanceof com.fa.cim.newcore.bo.pd.CimProcessFlow){
                com.fa.cim.newcore.bo.pd.CimProcessFlow cimProcessFlow = (com.fa.cim.newcore.bo.pd.CimProcessFlow) anObject;
                ProcessDefinition cimProcessDefinition = cimProcessFlow.getRootProcessDefinition();
                getHashedInfoNew(cimProcessDefinition.getIdentifier(),strHashedInfoSeq,BizConstant.SP_CLASSNAME_POSPROCESSDEFINITION, BizConstant.SP_CLASSNAME_POSPROCESSDEFINITION,new HashedInfoHashKey(BizConstant.SP_HASHDATA_PD_ID));
                getHashedInfoNew(cimProcessFlow.getProcessDefinitionLevel(),strHashedInfoSeq,aClassName, BizConstant.SP_CLASSNAME_POSPROCESSFLOW,new HashedInfoHashKey(BizConstant.SP_HASHDATA_PD_LEVEL));
            }
        }
        //PosProcessFlowContext
        if (CimStringUtils.equals(aClassName,BizConstant.SP_CLASSNAME_POSPROCESSFLOWCONTEXT)){
            //get cimLot
            if (anObject instanceof com.fa.cim.newcore.bo.pd.CimProcessFlowContext){
                com.fa.cim.newcore.bo.pd.CimProcessFlowContext cimProcessFlowContext = (com.fa.cim.newcore.bo.pd.CimProcessFlowContext) anObject;
                com.fa.cim.newcore.bo.product.CimLot lotCore = baseCoreFactory.getBO(com.fa.cim.newcore.bo.product.CimLot.class, cimProcessFlowContext.getPrimaryKey());
                Validations.check(CimObjectUtils.isEmpty(lotCore), retCodeConfig.getNotFoundEqp());
                Infos.HashedInfo hashedInfo = new Infos.HashedInfo();
                hashedInfo.setHashKey(BizConstant.SP_HASHDATA_LOT_ID);
                hashedInfo.setHashData(lotCore.getIdentifier());
            }
        }


        //PosProcessOperation
        if (CimStringUtils.equals(aClassName,BizConstant.SP_CLASSNAME_POSPROCESSOPERATION)){
            if (anObject instanceof com.fa.cim.newcore.bo.pd.CimProcessOperation){
                com.fa.cim.newcore.bo.pd.CimProcessOperation cimProcessOperation = (com.fa.cim.newcore.bo.pd.CimProcessOperation) anObject;
                getHashedInfoNew(cimProcessOperation.getOperationNumber(),strHashedInfoSeq,aClassName, BizConstant.SP_CLASSNAME_POSPROCESSOPERATION,new HashedInfoHashKey(BizConstant.SP_HASHDATA_OPE_NO));
                //get parent cimProcessDefinition
                com.fa.cim.newcore.bo.pd.CimProcessDefinition processDefinition = cimProcessOperation.getMainProcessDefinition();
                getHashedInfoNew(processDefinition.getIdentifier(),strHashedInfoSeq,BizConstant.SP_CLASSNAME_POSPROCESSDEFINITION, BizConstant.SP_CLASSNAME_POSPROCESSDEFINITION,new HashedInfoHashKey(BizConstant.SP_HASHDATA_ROUTE_ID));
                //get cimLot
                com.fa.cim.newcore.bo.product.CimLot lotCore = baseCoreFactory.getBO(com.fa.cim.newcore.bo.product.CimLot.class,cimProcessOperation.getProcessFlowContext().getPrimaryKey());
                Validations.check(CimObjectUtils.isEmpty(lotCore), retCodeConfig.getNotFoundEqp());
                Infos.HashedInfo hashedInfo = new Infos.HashedInfo();
                hashedInfo.setHashKey(BizConstant.SP_HASHDATA_LOT_ID);
                hashedInfo.setHashData(lotCore.getIdentifier());
            }
        }
        //PosPortResource
        if (CimStringUtils.equals(aClassName,BizConstant.SP_CLASSNAME_POSPORTRESOURCE)){
            if (anObject instanceof com.fa.cim.newcore.bo.machine.CimPortResource){
                com.fa.cim.newcore.bo.machine.CimPortResource cimPortResource = (com.fa.cim.newcore.bo.machine.CimPortResource) anObject;
                getHashedInfoNew(cimPortResource.getIdentifier(),strHashedInfoSeq,aClassName, BizConstant.SP_CLASSNAME_POSPORTRESOURCE,new HashedInfoHashKey(BizConstant.SP_HASHDATA_PORT_ID));
                //get parent cimMachine
                Machine machine = cimPortResource.getMachine();
                getHashedInfoNew(machine.getIdentifier(),strHashedInfoSeq,BizConstant.SP_CLASSNAME_POSMACHINE, BizConstant.SP_CLASSNAME_POSMACHINE,new HashedInfoHashKey(BizConstant.SP_HASHDATA_EQP_ID));
            }
        }
        //PosProcessOperationSpecification
        if (CimStringUtils.equals(aClassName,BizConstant.SP_CLASSNAME_POSPROCESSOPERATIONSPECIFICATION)){
            if (anObject instanceof com.fa.cim.newcore.bo.pd.CimProcessOperationSpecification){
                com.fa.cim.newcore.bo.pd.CimProcessOperationSpecification cimProcessOperationSpecification = (com.fa.cim.newcore.bo.pd.CimProcessOperationSpecification) anObject;
                getHashedInfoNew(cimProcessOperationSpecification.getOperationNumber(),strHashedInfoSeq,aClassName, BizConstant.SP_CLASSNAME_POSPROCESSOPERATIONSPECIFICATION,new HashedInfoHashKey(BizConstant.SP_HASHDATA_OPE_NO));
                //get parent cimProcessFlow
                com.fa.cim.newcore.bo.pd.CimProcessFlow cimProcessFlow = cimProcessOperationSpecification.getProcessFlow();
                com.fa.cim.newcore.bo.pd.CimProcessDefinition cimProcessDefinition = (com.fa.cim.newcore.bo.pd.CimProcessDefinition) cimProcessFlow.getRootProcessDefinition();
                getHashedInfoNew(cimProcessDefinition.getIdentifier(),strHashedInfoSeq,BizConstant.SP_CLASSNAME_POSPROCESSDEFINITION, BizConstant.SP_CLASSNAME_POSPROCESSDEFINITION,new HashedInfoHashKey(BizConstant.SP_HASHDATA_PD_ID));
                getHashedInfoNew(cimProcessDefinition.getProcessDefinitionLevel(),strHashedInfoSeq,BizConstant.SP_CLASSNAME_POSPROCESSDEFINITION, BizConstant.SP_CLASSNAME_POSPROCESSDEFINITION,new HashedInfoHashKey(BizConstant.SP_HASHDATA_PD_LEVEL));
            }
        }
        //PosProcessResource
        if (CimStringUtils.equals(aClassName,BizConstant.SP_CLASSNAME_POSPROCESSRESOURCE)){
            com.fa.cim.newcore.bo.machine.CimProcessResource cimProcessResource = (com.fa.cim.newcore.bo.machine.CimProcessResource) anObject;
            getHashedInfoNew(cimProcessResource.getIdentifier(),strHashedInfoSeq,aClassName, BizConstant.SP_CLASSNAME_POSPROCESSRESOURCE,new HashedInfoHashKey(BizConstant.SP_HASHDATA_PROCRSC_ID));
            //get parent cimMachine
            Machine machine = cimProcessResource.getMachine();
            getHashedInfoNew(machine.getIdentifier(),strHashedInfoSeq,BizConstant.SP_CLASSNAME_POSMACHINE, BizConstant.SP_CLASSNAME_POSMACHINE,new HashedInfoHashKey(BizConstant.SP_HASHDATA_EQP_ID));
        }
        //PosPrivilegeGroup
        if (anObject instanceof com.fa.cim.newcore.bo.person.CimPrivilegeGroup){
            com.fa.cim.newcore.bo.person.CimPrivilegeGroup cimPrivilegeGroup = (com.fa.cim.newcore.bo.person.CimPrivilegeGroup) anObject;
            getHashedInfoNew(cimPrivilegeGroup.getIdentifier(),strHashedInfoSeq,aClassName, BizConstant.SP_CLASSNAME_POSPRIVILEGEGROUP,new HashedInfoHashKey(BizConstant.SP_HASHDATA_PRIVGRP_KEY));
        }

        //PosProductCategory
        if (anObject instanceof com.fa.cim.newcore.bo.prodspec.CimProductCategory){
            com.fa.cim.newcore.bo.prodspec.CimProductCategory cimProductCategory = (com.fa.cim.newcore.bo.prodspec.CimProductCategory) anObject;
            getHashedInfoNew(cimProductCategory.getIdentifier(),strHashedInfoSeq,aClassName, BizConstant.SP_CLASSNAME_POSPRODUCTCATEGORY,new HashedInfoHashKey(BizConstant.SP_HASHDATA_CATEGORY_ID));
        }
        //PosProductGroup
        if (anObject instanceof com.fa.cim.newcore.bo.prodspec.CimProductGroup){
            com.fa.cim.newcore.bo.prodspec.CimProductGroup cimProductGroup = (com.fa.cim.newcore.bo.prodspec.CimProductGroup) anObject;
            getHashedInfoNew(cimProductGroup.getIdentifier(),strHashedInfoSeq,aClassName, BizConstant.SP_CLASSNAME_POSPRODUCTGROUP,new HashedInfoHashKey(BizConstant.SP_HASHDATA_PRODGRP_ID));
        }

        //PosProductRequest
        if (anObject instanceof com.fa.cim.newcore.bo.planning.CimProductRequest){
            com.fa.cim.newcore.bo.planning.CimProductRequest cimProductRequest = (com.fa.cim.newcore.bo.planning.CimProductRequest) anObject;
            getHashedInfoNew(cimProductRequest.getIdentifier(),strHashedInfoSeq,aClassName, BizConstant.SP_CLASSNAME_POSPRODUCTREQUEST,new HashedInfoHashKey(BizConstant.SP_HASHDATA_PRODREQ_ID));
        }

        //PosProductSpecification
        if (anObject instanceof com.fa.cim.newcore.bo.prodspec.CimProductSpecification){
            com.fa.cim.newcore.bo.prodspec.CimProductSpecification cimProductSpecification = (com.fa.cim.newcore.bo.prodspec.CimProductSpecification) anObject;
            getHashedInfoNew(cimProductSpecification.getIdentifier(),strHashedInfoSeq,aClassName, BizConstant.SP_CLASSNAME_POSPRODUCTSPECIFICATION,new HashedInfoHashKey(BizConstant.SP_HASHDATA_PRODSPEC_ID));
        }

        //PosQTimeRestriction
        if (CimStringUtils.equals(aClassName,BizConstant.SP_CLASSNAME_POSQTIMERESTRICTION)){
            if (anObject instanceof com.fa.cim.newcore.bo.pd.CimQTimeRestriction){
                com.fa.cim.newcore.bo.pd.CimQTimeRestriction cimQTimeRestriction = (com.fa.cim.newcore.bo.pd.CimQTimeRestriction) anObject;
                getHashedInfoNew(cimQTimeRestriction.getTriggerOperationNumber(),strHashedInfoSeq,aClassName, BizConstant.SP_CLASSNAME_POSQTIMERESTRICTION,new HashedInfoHashKey(BizConstant.SP_HASHDATA_TRIGGER_OPE_NO));
                getHashedInfoNew(cimQTimeRestriction.getTargetOperationNumber(),strHashedInfoSeq,aClassName, BizConstant.SP_CLASSNAME_POSQTIMERESTRICTION,new HashedInfoHashKey(BizConstant.SP_HASHDATA_TARGET_OPE_NO));
                //get parent cimLot
                com.fa.cim.newcore.bo.product.CimLot lotCore = cimQTimeRestriction.getLot();
                getHashedInfoNew(lotCore.getIdentifier(),strHashedInfoSeq,BizConstant.SP_CLASSNAME_POSLOT, BizConstant.SP_CLASSNAME_POSLOT,new HashedInfoHashKey(BizConstant.SP_HASHDATA_LOT_ID));
                //get parent cimProcessDefinition
                com.fa.cim.newcore.bo.pd.CimProcessDefinition cimProcessDefinitionTrigger = cimQTimeRestriction.getTriggerMainProcessDefinition();
                getHashedInfoNew(cimProcessDefinitionTrigger.getIdentifier(),strHashedInfoSeq,BizConstant.SP_CLASSNAME_POSPROCESSDEFINITION, BizConstant.SP_CLASSNAME_POSPROCESSDEFINITION,new HashedInfoHashKey(BizConstant.SP_HASHDATA_TRIGGER_MAINPD_ID));
                //get parent cimProcessDefinition
                com.fa.cim.newcore.bo.pd.CimProcessDefinition cimProcessDefinitionTarget = cimQTimeRestriction.getTargetMainProcessDefinition();
                getHashedInfoNew(cimProcessDefinitionTarget.getIdentifier(),strHashedInfoSeq,BizConstant.SP_CLASSNAME_POSPROCESSDEFINITION, BizConstant.SP_CLASSNAME_POSPROCESSDEFINITION,new HashedInfoHashKey(BizConstant.SP_HASHDATA_TARGET_MAINPD_ID));
            }
        }
        //PosQTimeRestrictionByWafer
        if (CimStringUtils.equals(aClassName,BizConstant.SP_CLASSNAME_POSQTIMERESTRICTIONBYWAFER)){
            //todo can not find the QTimeRestrictionByWafer Bo

        }

        //PosRawMachineStateSet
        if (anObject instanceof com.fa.cim.newcore.bo.code.CimRawMachineStateSet){
            com.fa.cim.newcore.bo.code.CimRawMachineStateSet cimRawMachineStateSet = (com.fa.cim.newcore.bo.code.CimRawMachineStateSet) anObject;
            getHashedInfoNew(cimRawMachineStateSet.getIdentifier(),strHashedInfoSeq,aClassName, BizConstant.SP_CLASSNAME_POSRAWMACHINESTATESET,new HashedInfoHashKey(BizConstant.SP_HASHDATA_RAWEQPSTATE_ID));
        }

        //PosReticlePodPortResource
        if (CimStringUtils.equals(aClassName,BizConstant.SP_CLASSNAME_POSRETICLEPODPORTRESOURCE)){
            if (anObject instanceof com.fa.cim.newcore.bo.machine.CimReticlePodPortResource){
                com.fa.cim.newcore.bo.machine.CimReticlePodPortResource cimReticlePodPortResource = (com.fa.cim.newcore.bo.machine.CimReticlePodPortResource) anObject;
                getHashedInfoNew(cimReticlePodPortResource.getIdentifier(),strHashedInfoSeq,aClassName, BizConstant.SP_CLASSNAME_POSRETICLEPODPORTRESOURCE,new HashedInfoHashKey(BizConstant.SP_HASHDATA_PORT_ID));
                //get parent cimMachine
                Machine machine = cimReticlePodPortResource.getMachine();
                getHashedInfoNew(machine.getIdentifier(),strHashedInfoSeq,BizConstant.SP_CLASSNAME_POSMACHINE, BizConstant.SP_CLASSNAME_POSMACHINE,new HashedInfoHashKey(BizConstant.SP_HASHDATA_EQP_ID));
            }
        }
        //PosReticlePod
        if (anObject instanceof com.fa.cim.newcore.bo.durable.CimReticlePod){
            com.fa.cim.newcore.bo.durable.CimReticlePod cimReticlePod = (com.fa.cim.newcore.bo.durable.CimReticlePod) anObject;
            getHashedInfoNew(cimReticlePod.getIdentifier(),strHashedInfoSeq,aClassName, BizConstant.SP_CLASSNAME_POSRETICLEPOD,new HashedInfoHashKey(BizConstant.SP_HASHDATA_RTCLPOD_ID));
        }

        //PosReticleSet
        if (anObject instanceof com.fa.cim.newcore.bo.durable.CimReticleSet){
            com.fa.cim.newcore.bo.durable.CimReticleSet cimReticleSet = (com.fa.cim.newcore.bo.durable.CimReticleSet) anObject;
            getHashedInfoNew(cimReticleSet.getIdentifier(),strHashedInfoSeq,aClassName, BizConstant.SP_CLASSNAME_POSRETICLESET,new HashedInfoHashKey(BizConstant.SP_HASHDATA_RTCLSET_ID));
        }

        //PosScript
        if (anObject instanceof com.fa.cim.newcore.bo.code.CimScript){
            com.fa.cim.newcore.bo.code.CimScript cimScript = (com.fa.cim.newcore.bo.code.CimScript) anObject;
            getHashedInfoNew(cimScript.getIdentifier(),strHashedInfoSeq,aClassName, BizConstant.SP_CLASSNAME_POSSCRIPT,new HashedInfoHashKey(BizConstant.SP_HASHDATA_SCRIPT_ID));
        }

        //PosSampleSpecification
        if (anObject instanceof com.fa.cim.newcore.bo.prodspec.CimSampleSpecification){
            com.fa.cim.newcore.bo.prodspec.CimSampleSpecification cimSampleSpecification = (com.fa.cim.newcore.bo.prodspec.CimSampleSpecification) anObject;
            getHashedInfoNew(cimSampleSpecification.getIdentifier(),strHashedInfoSeq,aClassName, BizConstant.SP_CLASSNAME_POSSAMPLESPECIFICATION,new HashedInfoHashKey(BizConstant.SP_HASHDATA_SMPLSPEC_ID));
        }

        //PosStage
        if (anObject instanceof com.fa.cim.newcore.bo.factory.CimStage){
            com.fa.cim.newcore.bo.factory.CimStage cimStage = (com.fa.cim.newcore.bo.factory.CimStage) anObject;
            getHashedInfoNew(cimStage.getIdentifier(),strHashedInfoSeq,aClassName, BizConstant.SP_CLASSNAME_POSSTAGE,new HashedInfoHashKey(BizConstant.SP_HASHDATA_STAGE_ID));
        }

        //PosStageGroup
        if (anObject instanceof com.fa.cim.newcore.bo.factory.CimStageGroup){
            com.fa.cim.newcore.bo.factory.CimStageGroup cimStageGroup = (com.fa.cim.newcore.bo.factory.CimStageGroup) anObject;
            getHashedInfoNew(cimStageGroup.getIdentifier(),strHashedInfoSeq,aClassName, BizConstant.SP_CLASSNAME_POSSTAGEGROUP,new HashedInfoHashKey(BizConstant.SP_HASHDATA_STAGEGRP_ID));
        }

        //PosStorageMachine
        if (anObject instanceof com.fa.cim.newcore.bo.machine.CimStorageMachine){
            com.fa.cim.newcore.bo.machine.CimStorageMachine cimStorageMachine = (com.fa.cim.newcore.bo.machine.CimStorageMachine) anObject;
            getHashedInfoNew(cimStorageMachine.getIdentifier(),strHashedInfoSeq,aClassName, BizConstant.SP_CLASSNAME_POSSTORAGEMACHINE,new HashedInfoHashKey(BizConstant.SP_HASHDATA_STK_ID));
        }

        //PosSystemMessageCode
        if (anObject instanceof com.fa.cim.newcore.bo.code.CimSystemMessageCode){
            com.fa.cim.newcore.bo.code.CimSystemMessageCode cimSystemMessageCode = (com.fa.cim.newcore.bo.code.CimSystemMessageCode) anObject;
            getHashedInfoNew(cimSystemMessageCode.getIdentifier(),strHashedInfoSeq,aClassName, BizConstant.SP_CLASSNAME_POSSYSTEMMESSAGECODE,new HashedInfoHashKey(BizConstant.SP_HASHDATA_SYSMESSAGE_ID));
        }

        //PosTechnology
        if (anObject instanceof com.fa.cim.newcore.bo.prodspec.CimTechnology){
            com.fa.cim.newcore.bo.prodspec.CimTechnology cimTechnology = (com.fa.cim.newcore.bo.prodspec.CimTechnology) anObject;
            getHashedInfoNew(cimTechnology.getIdentifier(),strHashedInfoSeq,aClassName, BizConstant.SP_CLASSNAME_POSTECHNOLOGY,new HashedInfoHashKey(BizConstant.SP_HASHDATA_TECH_ID));
        }

        //PosTestSpecification
        if (anObject instanceof com.fa.cim.newcore.bo.prodspec.CimTestSpecification){
            com.fa.cim.newcore.bo.prodspec.CimTestSpecification cimTestSpecification = (com.fa.cim.newcore.bo.prodspec.CimTestSpecification) anObject;
            getHashedInfoNew(cimTestSpecification.getIdentifier(),strHashedInfoSeq,aClassName, BizConstant.SP_CLASSNAME_POSTESTSPECIFICATION,new HashedInfoHashKey(BizConstant.SP_HASHDATA_TESTSPEC_ID));
        }

        //PosTestType
        if (anObject instanceof com.fa.cim.newcore.bo.prodspec.CimTestType){
            com.fa.cim.newcore.bo.prodspec.CimTestType cimTestType = (com.fa.cim.newcore.bo.prodspec.CimTestType) anObject;
            getHashedInfoNew(cimTestType.getIdentifier(),strHashedInfoSeq,aClassName, BizConstant.SP_CLASSNAME_POSTESTTYPE,new HashedInfoHashKey(BizConstant.SP_HASHDATA_TESTTYPE_ID));
        }

        //PosPerson
        if (anObject instanceof com.fa.cim.newcore.bo.person.CimPerson){
            com.fa.cim.newcore.bo.person.CimPerson cimPerson = (com.fa.cim.newcore.bo.person.CimPerson) anObject;
            getHashedInfoNew(cimPerson.getIdentifier(),strHashedInfoSeq,aClassName, BizConstant.SP_CLASSNAME_POSPERSON,new HashedInfoHashKey(BizConstant.SP_HASHDATA_USER_ID));
        }

        //PosUserGroup
        if (anObject instanceof CimUserGroup){
            CimUserGroup cimUserGroup = (CimUserGroup) anObject;
            getHashedInfoNew(cimUserGroup.getIdentifier(),strHashedInfoSeq,aClassName, BizConstant.SP_CLASSNAME_POSUSERGROUP,new HashedInfoHashKey(BizConstant.SP_HASHDATA_USERGRP_ID));
        }
        //PosWafer
        if (anObject instanceof com.fa.cim.newcore.bo.product.CimWafer){
            com.fa.cim.newcore.bo.product.CimWafer cimWafer = (com.fa.cim.newcore.bo.product.CimWafer) anObject;
            getHashedInfoNew(cimWafer.getIdentifier(),strHashedInfoSeq,aClassName, BizConstant.SP_CLASSNAME_POSWAFER,new HashedInfoHashKey(BizConstant.SP_HASHDATA_WAFER_ID));
        }

        //PosEqpMonitor
        if (anObject instanceof CimEqpMonitor){
            CimEqpMonitor cimEqpMonitor = (CimEqpMonitor) anObject;
            getHashedInfoNew(cimEqpMonitor.getIdentifier(),strHashedInfoSeq,aClassName, BizConstant.SP_CLASSNAME_POSEQPMONITOR,new HashedInfoHashKey(BizConstant.SP_HASHDATA_EQP_MONITOR_ID));
        }

        //PosEqpMonitorJob
        if (CimStringUtils.equals(aClassName, BizConstant.SP_CLASSNAME_POSEQPMONITORJOB)) {
            if (anObject instanceof CimEqpMonitorJob){
                CimEqpMonitorJob cimEqpMonitorJob = (CimEqpMonitorJob) anObject;
                getHashedInfoNew(cimEqpMonitorJob.getIdentifier(),strHashedInfoSeq,aClassName, BizConstant.SP_CLASSNAME_POSEQPMONITORJOB,new HashedInfoHashKey(BizConstant.SP_HASHDATA_EQP_MONITOR_JOB_ID));
                //get parent cimEqpMonitor
                CimEqpMonitor cimEqpMonitor = cimEqpMonitorJob.getEqpMonitor();
                getHashedInfoNew(cimEqpMonitor.getIdentifier(),strHashedInfoSeq,BizConstant.SP_CLASSNAME_POSEQPMONITOR, BizConstant.SP_CLASSNAME_POSEQPMONITOR,new HashedInfoHashKey(BizConstant.SP_HASHDATA_EQP_MONITOR_ID));
            }
        }
        //Unknown ID
        if (CimStringUtils.equals(aClassName,BizConstant.SP_CLASSNAME_POSFLOWBATCHDISPATCHER) && strHashedInfoSeq.size() == 0){
            log.info("Unknown ID");
            strHashedInfoSeq.add(new Infos.HashedInfo("OBJ", aStringObjRef));
        }
        out.setClassName(aClassName);
        out.setStrHashedInfoSeq(strHashedInfoSeq);
        return out;
    }

    /**
     * description:
     * <p>PPT_GET_OBJECT_FROM_CLASSNAME_AND_KEY</p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/1/17                          Wind
     * @param currentClass
     * @param className
     * @param entityType
     * @param theClass
     * @param methodName
     * @param methodParams
     * @return <T extends BaseEntity> T
     * @author Wind
     * @date 2019/1/17 11:35
     */
    private  <T extends BaseEntity> T getObjectFromClassNameAndKey(String currentClass, String className, T entityType, Class<?> theClass, String methodName, Object... methodParams) {
        Validations.check(!CimStringUtils.equals(currentClass, className), retCodeConfig.getNotFoundObject());
        Method method = null;
        T obj = null;
        if(methodParams == null || methodParams.length == 0){
            return null;
        }
        List<Object> Params = new ArrayList<>();
        try {
            method = theClass.getMethod(methodName, String.class);
            for (Object methodParam : methodParams) {
                obj = (T)method.invoke(entityType, methodParam);
            }
        }catch (Exception e){
            log.info("get method by method name failed !");
        }
        return obj;
    }

    /**
     * description:
     * <p>PPT_GET_HASHEDINFO</p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/1/17                          Wind
     * @param hashedInfoList
     * @param cimCore
     * @param anObject
     * @param theClassName
     * @param theCurrentClass
     * @param forMethodNameAndKeys
     * @return List<Infos.HashedInfo>
     * @author Wind
     * @date 2019/1/17 11:37
     */
    private void getHashedInfo(List<Infos.HashedInfo> hashedInfoList, Class<? extends BaseEntity> anObject, String theCurrentClass, String theClassName, Class<? extends CimBO> cimCore, HashedInfoForMethodNameAndKey... forMethodNameAndKeys){
        if(!CimStringUtils.equals(theCurrentClass, theClassName) || forMethodNameAndKeys == null || forMethodNameAndKeys.length == 0){
            return;
        }
        for (HashedInfoForMethodNameAndKey forMethodNameAndKey : forMethodNameAndKeys) {
            try {
                Method method = cimCore.getMethod(forMethodNameAndKey.getMethodName(), anObject);
                CimBO cimBO = cimCore.newInstance();
                BaseEntity baseEntity = anObject.newInstance();
                Object identifier = method.invoke(cimBO, baseEntity);

                Infos.HashedInfo hashedInfo = new Infos.HashedInfo();
                hashedInfo.setHashKey(forMethodNameAndKey.getHashedKey());
                hashedInfo.setHashData(String.valueOf(identifier));

                hashedInfoList.add(hashedInfo);

            } catch (NoSuchMethodException e) {
                log.error(e.getMessage(),e);
            } catch (IllegalAccessException e) {
                log.error(e.getMessage(),e);
            } catch (InstantiationException e) {
                log.error(e.getMessage(),e);
            } catch (InvocationTargetException e) {
                log.error(e.getMessage(),e);
            }
        }
    }
    private void getHashedInfoNew(String hashData, List<Infos.HashedInfo> hashedInfoList, String aClassName, String theCurrentClass, HashedInfoHashKey... hashedInfoHashKeys){
        if(!CimStringUtils.equals(theCurrentClass, aClassName) || hashedInfoHashKeys == null || hashedInfoHashKeys.length == 0){
            return;
        }
        if (CimStringUtils.isNotEmpty(hashData)){
            for (HashedInfoHashKey hashedInfoHashKey : hashedInfoHashKeys) {
                String hashKey = hashedInfoHashKey.getHashedKey();
                Infos.HashedInfo hashedInfo = new Infos.HashedInfo();
                hashedInfo.setHashKey(hashKey);
                hashedInfo.setHashData(hashData);
                hashedInfoList.add(hashedInfo);
            }
        }
    }

    /**
     * use to binding methodName and Key
     */
    @Getter
    @Setter
    class HashedInfoForMethodNameAndKey {
        private String methodName;
        private String hashedKey;

        HashedInfoForMethodNameAndKey(String methodName, String hashedKey){
            this.methodName = methodName;
            this.hashedKey = hashedKey;
        }
    }

    @Getter
    @Setter
    class HashedInfoHashKey {
        private String hashedKey;

        public HashedInfoHashKey(String hashedKey) {
            this.hashedKey = hashedKey;
        }
    }

    /**
     * description:
     * <p>PPT_GET_PARENTOBJ</p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/1/18                          Wind
     * @param
     * @return
     * @author Wind
     * @date 2019/1/18 10:24
     */
    private void getParentObject(Class<? extends BaseEntity> parentObject, Class<? extends BaseEntity> anObject, Class<? extends CimBO> theClass, Class<? extends CimBO> parentClass, String methodName){
        /*parentObject = null;
        theClass = null;*/

        /*Class<? extends BaseEntity> aClassObj = obj.getClass();
        if(aClassObj!=null){
            Method method = BeanUtils.findMethod(aClassObj, methodName);
            try {
                Object value = method.invoke(obj);
                parentEntity = convertIdToObject(parentEntity.getClass(), value.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }*/
    }

    @Override
    public Info.ObjObjectValidSorterJobGetOut objectValidSorterJobGet(Infos.ObjCommon objCommon, Info.ObjectValidSorterJobGetIn objectValidSorterJobGetIn) {
        Info.ObjObjectValidSorterJobGetOut objObjectValidSorterJobGetOut = new Info.ObjObjectValidSorterJobGetOut();
        List<Info.SortJobListAttributes> strValidSorterJob = new ArrayList<>();
        List<Info.SortJobListAttributes> strOtherSorterJob = new ArrayList<>();
        objObjectValidSorterJobGetOut.setStrValidSorterJob(strValidSorterJob);
        objObjectValidSorterJobGetOut.setStrOtherSorterJob(strOtherSorterJob);
        String classification = objectValidSorterJobGetIn.getClassification();
        ObjectIdentifier objectID = objectValidSorterJobGetIn.getObjectID();
        /**************************************/
        /* Get cassette sorterJob information */
        /**************************************/
        Params.ObjSorterJobListGetDRIn objSorterJobListGetDRIn = new Params.ObjSorterJobListGetDRIn();
        if (CimStringUtils.equals(classification, BizConstant.SP_CLASSNAME_POSMACHINE)){
            objSorterJobListGetDRIn.setEquipmentID(objectID);
        } else if (CimStringUtils.equals(classification, BizConstant.SP_CLASSNAME_POSLOT)){
            objSorterJobListGetDRIn.setLotID(objectID);
        } else if (CimStringUtils.equals(classification, BizConstant.SP_CLASSNAME_POSCASSETTE)){
            objSorterJobListGetDRIn.setCarrierID(objectID);
        } else if (CimStringUtils.equals(classification, BizConstant.SP_CLASSNAME_POSUSER)){
            objSorterJobListGetDRIn.setCreateUser(objectID);
        } else if (CimStringUtils.equals(classification, BizConstant.SP_CLASSNAME_SORTERJOB)){
            objSorterJobListGetDRIn.setSorterJob(objectID);
        } else {
            throw new ServiceException(retCodeConfig.getInvalidInputParam());
        }
        /**************************************/
        /* Get object sorterJob information   */
        /**************************************/
        List<Info.SortJobListAttributes>  objSorterJobListGetDROut = sorterNewMethod.sorterJobListGetDR(objCommon, objSorterJobListGetDRIn);
        List<Info.SortJobListAttributes> sortJobListAttributesList = objSorterJobListGetDROut;
        int nLen = CimArrayUtils.getSize(sortJobListAttributesList);
        if (nLen > 0){
            for (Info.SortJobListAttributes sortJobListAttributes : sortJobListAttributesList){
                /********************************************************************************************************/
                /*   Set return value.                                                                                  */
                /*   Set only item whose sorter job status is "Wait To Executing" "Xfer" into "strValidSorterJob"       */
                /*   Set only item whose sorter job status is not "Wait To Executing" "Xfer" into "strOtherSorterJob"   */
                /********************************************************************************************************/
                List<Info.SorterComponentJobListAttributes> sorterComponentJobListAttributesList = sortJobListAttributes.getSorterComponentJobListAttributesList();
                List<Info.SorterComponentJobListAttributes> tmpValidSorterComponentJobListAttributesList = new ArrayList<>();
                List<Info.SorterComponentJobListAttributes> tmpOtherSorterComponentJobListAttributesList = new ArrayList<>();
                if (CimArrayUtils.isNotEmpty(sorterComponentJobListAttributesList)) {
                    for (Info.SorterComponentJobListAttributes sorterComponentJobListAttributes : sorterComponentJobListAttributesList){
                        //todo 千岛二期增加
                        if (CimStringUtils.equals(sorterComponentJobListAttributes.getComponentSorterJobStatus(), SorterType.Status.Created.getValue())
                        ||CimStringUtils.equals(sorterComponentJobListAttributes.getComponentSorterJobStatus(), SorterType.Status.Xfer.getValue())){
                            tmpValidSorterComponentJobListAttributesList.add(sorterComponentJobListAttributes);
                        } else {
                            tmpOtherSorterComponentJobListAttributesList.add(sorterComponentJobListAttributes);
                        }
                    }
                }
                if (!CimObjectUtils.isEmpty(tmpValidSorterComponentJobListAttributesList)){
                    Info.SortJobListAttributes validSortJobListAttributes = new Info.SortJobListAttributes();
                    strValidSorterJob.add(validSortJobListAttributes);
                    validSortJobListAttributes.setSorterJobID(sortJobListAttributes.getSorterJobID());
                    validSortJobListAttributes.setEquipmentID(sortJobListAttributes.getEquipmentID());
                    validSortJobListAttributes.setPortGroupID(sortJobListAttributes.getPortGroupID());
                    validSortJobListAttributes.setSorterJobStatus(sortJobListAttributes.getSorterJobStatus());
                    validSortJobListAttributes.setRequestUserID(sortJobListAttributes.getRequestUserID());
                    validSortJobListAttributes.setRequestTimeStamp(sortJobListAttributes.getRequestTimeStamp());
                    validSortJobListAttributes.setComponentCount(sortJobListAttributes.getComponentCount());
                    validSortJobListAttributes.setPreSorterJobID(sortJobListAttributes.getPreSorterJobID());
                    validSortJobListAttributes.setWaferIDReadFlag(sortJobListAttributes.isWaferIDReadFlag());
                    validSortJobListAttributes.setSorterComponentJobListAttributesList(tmpValidSorterComponentJobListAttributesList);
                }
                if (!CimObjectUtils.isEmpty(tmpOtherSorterComponentJobListAttributesList)){
                    Info.SortJobListAttributes otherSortJobListAttributes = new Info.SortJobListAttributes();
                    strOtherSorterJob.add(otherSortJobListAttributes);
                    otherSortJobListAttributes.setSorterJobID(sortJobListAttributes.getSorterJobID());
                    otherSortJobListAttributes.setEquipmentID(sortJobListAttributes.getEquipmentID());
                    otherSortJobListAttributes.setPortGroupID(sortJobListAttributes.getPortGroupID());
                    otherSortJobListAttributes.setSorterJobStatus(sortJobListAttributes.getSorterJobStatus());
                    otherSortJobListAttributes.setRequestUserID(sortJobListAttributes.getRequestUserID());
                    otherSortJobListAttributes.setRequestTimeStamp(sortJobListAttributes.getRequestTimeStamp());
                    otherSortJobListAttributes.setComponentCount(sortJobListAttributes.getComponentCount());
                    otherSortJobListAttributes.setPreSorterJobID(sortJobListAttributes.getPreSorterJobID());
                    otherSortJobListAttributes.setWaferIDReadFlag(sortJobListAttributes.isWaferIDReadFlag());
                    otherSortJobListAttributes.setSorterComponentJobListAttributesList(tmpOtherSorterComponentJobListAttributesList);
                }
            }
        }
        return objObjectValidSorterJobGetOut;
    }
}
