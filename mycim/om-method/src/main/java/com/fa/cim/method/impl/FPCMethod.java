package com.fa.cim.method.impl;

import com.alibaba.fastjson.JSONObject;
import com.fa.cim.annotaion.OmMethod;
import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.OmCode;
import com.fa.cim.common.utils.*;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Inputs;
import com.fa.cim.dto.Outputs;
import com.fa.cim.dto.Results;
import com.fa.cim.entity.nonruntime.fpc.*;
import com.fa.cim.entity.nonruntime.runcard.CimRunCardPsmDocDO;
import com.fa.cim.entitysuper.NonRuntimeEntity;
import com.fa.cim.jpa.CimJpaRepository;
import com.fa.cim.method.*;
import com.fa.cim.newcore.bo.dc.CimDataCollectionDefinition;
import com.fa.cim.newcore.bo.dc.CimDataCollectionSpecification;
import com.fa.cim.newcore.bo.durable.CimProcessDurableCapability;
import com.fa.cim.newcore.bo.machine.CimMachine;
import com.fa.cim.newcore.bo.pd.CimProcessDefinition;
import com.fa.cim.newcore.bo.product.CimLot;
import com.fa.cim.newcore.bo.product.CimLotFamily;
import com.fa.cim.newcore.bo.recipe.CimLogicalRecipe;
import com.fa.cim.newcore.bo.recipe.CimMachineRecipe;
import com.fa.cim.newcore.dto.dc.EDCDTO;
import com.fa.cim.newcore.dto.product.ProductDTO;
import com.fa.cim.newcore.factory.BaseCoreFactory;
import com.fa.cim.newcore.impl.bo.env.StandardProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/3/21       ********              Nyx             create file
 *
 * @author: Nyx
 * @date: 2019/3/21 16:15
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@OmMethod
public class FPCMethod implements IFPCMethod {

    @Autowired
    private RetCodeConfig retCodeConfig;

    @Autowired
    private CimJpaRepository cimJpaRepository;

    @Autowired
    private ILotMethod lotMethod;

    @Autowired
    private IProcessMethod processMethod;

    @Autowired
    private IDataCollectionMethod dataCollectionMethod;

    @Autowired
    private IRecipeMethod recipeMethod;

    @Autowired
    private BaseCoreFactory baseCoreFactory;

    @Autowired
    private com.fa.cim.newcore.bo.dispatch.DispatchingManager newDispatchingManager;

    @Autowired
    private IRunCardMethod runCardMethod;

    @Override
    public List<Infos.FPCInfo> fpcInfoGetDR(Infos.ObjCommon objCommon, Inputs.ObjFPCInfoGetDRIn in) {
        List<String> fpciDs = in.getFPCIDs();
        ObjectIdentifier lotID = in.getLotID();
        ObjectIdentifier lotFamilyID = in.getLotFamilyID();
        ObjectIdentifier orgMainPDID = in.getOrgMainPDID();
        String orgOperNo = in.getOrgOperNo();
        ObjectIdentifier mainPDID = in.getMainPDID();
        String mainOperNo = in.getMainOperNo();
        ObjectIdentifier subMainPDID = in.getSubMainPDID();
        String subOperNo = in.getSubOperNo();
        ObjectIdentifier equipmentID = in.getEquipmentID();

        boolean dcSpecItemInfoGetFlag = in.isDcSpecItemInfoGetFlag();
        boolean recipeParmInfoGetFlag = in.isRecipeParmInfoGetFlag();
        boolean reticleInfoGetFlag = in.isReticleInfoGetFlag();
        boolean waferIDInfoGetFlag = in.isWaferIDInfoGetFlag();


        /*----------------*/
        /*   Initialize   */
        /*----------------*/
        StringBuilder buffer = new StringBuilder(
                "SELECT   DOC_ID," +
                "               LOTFAMILY_ID," +
                "               LOTFAMILY_RKEY," +
                "               PROCESS_ID," +
                "               PROCESS_RKEY," +
                "               OPE_NO," +
                "               ORIG_PROCESS_ID," +
                "               ORIG_PROCESS_RKEY," +
                "               ORIG_OPE_NO," +
                "               SUB_PROCESS_ID," +
                "               SUB_PROCESS_RKEY," +
                "               SUB_OPE_NO," +
                "               DOC_GROUP_NO," +
                "               DOC_TYPE," +
                "               MERGE_PROCESS_ID," +
                "               MERGE_PROCESS_RKEY," +
                "               MERGE_OPE_NO," +
                "               DOC_CATEGORY_ID," +
                "               STEP_ID," +
                "               STEP_RKEY," +
                "               STEP_TYPE," +
                "               CORR_OPE_NO," +
                "               SKIP_FLAG," +
                "               LIMIT_EQP_FLAG," +
                "               EQP_ID," +
                "               EQP_RKEY," +
                "               RPARAM_CHG_TYPE," +
                "               MRCP_ID," +
                "               MRCP_RKEY," +
                "               EDC_PLAN_ID," +
                "               EDC_PLAN_RKEY," +
                "               EDC_SPEC_ID," +
                "               EDC_SPEC_RKEY," +
                "               SEND_EMAIL_FLAG," +
                "               HOLD_LOT_FLAG," +
                "               DESCRIPTION," +
                "               TRX_USER_ID," +
                "               CREATE_TIME," +
                "               UPDATE_TIME " +
                "FROM           OSDOC WHERE");
        if (!CimObjectUtils.isEmpty(fpciDs)) {
            log.debug("FPC_IDs is specified. FPC_ID count is {}", fpciDs.size());

            boolean firstCondition = true;
            for (String fpciD : fpciDs) {
                log.debug("FPC_ID = {}", fpciD);
                if (firstCondition) {
                    firstCondition = false;
                    buffer.append(String.format(" DOC_ID = '%s'", fpciD));
                } else {
                    buffer.append(String.format(" OR DOC_ID = '%s'", fpciD));
                }
            }
        } else {
            log.debug("FPC_IDs does not specified.");

            if ((!ObjectIdentifier.isEmpty(orgMainPDID) && CimStringUtils.isEmpty(orgOperNo)) ||
                    (ObjectIdentifier.isEmpty(orgMainPDID) && !CimStringUtils.isEmpty(orgOperNo))) {
                log.error("data combination error. {} {}", orgMainPDID, orgOperNo);
                throw new ServiceException(retCodeConfig.getInvalidInputParam());
            }

            if ((!ObjectIdentifier.isEmpty(subMainPDID) && CimStringUtils.isEmpty(subOperNo)) ||
                    (ObjectIdentifier.isEmpty(subMainPDID) && !CimStringUtils.isEmpty(subOperNo))) {
                log.error("data combination error. {} {}", subMainPDID, subOperNo);
                throw new ServiceException(retCodeConfig.getInvalidInputParam());
            }

            if (ObjectIdentifier.isEmpty(orgMainPDID) && !ObjectIdentifier.isEmpty(subMainPDID)) {
                log.error("data combination error. {} {}", orgMainPDID, subMainPDID);
                throw new ServiceException(retCodeConfig.getInvalidInputParam());
            }

            if (ObjectIdentifier.isEmpty(lotID) && ObjectIdentifier.isEmpty(lotFamilyID)) {
                return Collections.emptyList();
            }

            String tmpLotFamilyID = ObjectIdentifier.fetchValue(lotFamilyID);
            if (!ObjectIdentifier.isEmpty(lotID)) {
                // Get Lot Object
                CimLot lot = baseCoreFactory.getBO(CimLot.class, lotID);
                if (null == lot) {
                    throw new ServiceException(retCodeConfig.getNotFoundLot());
                }

                // Get LotFamily ID
                CimLotFamily lotFamily = lot.getLotFamily();
                if (null == lotFamily) {
                    log.error("Cannot get LotFamily for this lot. {}", lotID);
                    throw new ServiceException(retCodeConfig.getNotFoundLotFamily());
                }
                tmpLotFamilyID = lotFamily.getIdentifier();
            }
            log.debug("LotFamilyID {} of the specified lotID.", tmpLotFamilyID);

            if (!ObjectIdentifier.isEmpty(lotFamilyID)) {
                Validations.check(!ObjectIdentifier.equalsWithValue(lotFamilyID, tmpLotFamilyID), retCodeConfig.getNotFoundLotFamily());
            }
            log.debug("LotFamily ID {}", tmpLotFamilyID);

            buffer.append(String.format(" LOTFAMILY_ID = '%s'", tmpLotFamilyID));

            if (!ObjectIdentifier.isEmpty(mainPDID)) {
                log.debug("mainPDID is {}", mainPDID);
                // mainPD ID
                buffer.append(String.format(" AND PROCESS_ID = '%s'", mainPDID.getValue()));
                // operation Number
                buffer.append(String.format(" AND OPE_NO = '%s'", mainOperNo));

                if (!ObjectIdentifier.isEmpty(orgMainPDID)) {
                    // original MainPD ID
                    buffer.append(String.format(" AND ORIG_PROCESS_ID = '%s'", orgMainPDID.getValue()));

                    // original operation Number
                    buffer.append(String.format(" AND ORIG_OPE_NO = '%s'", orgOperNo));
                    if (!ObjectIdentifier.isEmpty(subMainPDID)) {
                        // sub MainPD ID
                        buffer.append(String.format(" AND SUB_PROCESS_ID = '%s'", subMainPDID.getValue()));

                        // sub operation Number
                        buffer.append(String.format(" AND SUB_OPE_NO = '%s'", subOperNo));
                    }
                }
            }

            // equipmentID
            if (!ObjectIdentifier.isEmpty(equipmentID)) {
                buffer.append(String.format(" AND (EQP_ID = '%s' OR EQP_ID IS NULL)", equipmentID.getValue()));
            }
        }
        //------------------
        // Check record count OSDOC
        //------------------
        List<Object[]> fpcs = cimJpaRepository.query(buffer.toString());
        List<Infos.FPCInfo> fpcInfos = new ArrayList<>();
        if (!CimObjectUtils.isEmpty(fpcs)) {
            for (Object[] fpc : fpcs) {
                Infos.FPCInfo fpcInfo = new Infos.FPCInfo();
                fpcInfos.add(fpcInfo);
                fpcInfo.setFpcID(CimObjectUtils.toString(fpc[0]));//FPC_ID
                fpcInfo.setLotFamilyID(new ObjectIdentifier(CimObjectUtils.toString(fpc[1]), CimObjectUtils.toString(fpc[2]))); //LOTFAMILY_ID, LOTFAMILY_OBJ,
                fpcInfo.setMainProcessDefinitionID(new ObjectIdentifier(CimObjectUtils.toString(fpc[3]), CimObjectUtils.toString(fpc[4])));//MAINPD_ID,MAINPD_OBJ,
                fpcInfo.setOperationNumber(CimObjectUtils.toString(fpc[5]));
                fpcInfo.setOriginalMainProcessDefinitionID(new ObjectIdentifier(CimObjectUtils.toString(fpc[6]), CimObjectUtils.toString(fpc[7])));//ORG_MAINPD_ID,ORG_MAINPD_OBJ,
                fpcInfo.setOriginalOperationNumber(CimObjectUtils.toString(fpc[8]));//ORG_OPE_NO
                fpcInfo.setSubMainProcessDefinitionID(new ObjectIdentifier(CimObjectUtils.toString(fpc[9]), CimObjectUtils.toString(fpc[10])));//SUB_MAINPD_ID,SUB_MAINPD_OBJ,
                fpcInfo.setSubOperationNumber(CimObjectUtils.toString(fpc[11]));//SUB_OPE_NO
                fpcInfo.setFpcGroupNumber(Integer.valueOf(CimObjectUtils.toString(fpc[12])));//FPC_GROUP_NO
                fpcInfo.setFpcType(CimObjectUtils.toString(fpc[13]));//FPC_TYPE
                fpcInfo.setMergeMainProcessDefinitionID(new ObjectIdentifier(CimObjectUtils.toString(fpc[14]), CimObjectUtils.toString(fpc[15])));//MERGE_MAINPD_ID,MERGE_MAINPD_OBJ,
                fpcInfo.setMergeOperationNumber(CimObjectUtils.toString(fpc[16]));//MERGE_OPE_NO
                fpcInfo.setFpcCategory(CimObjectUtils.toString(fpc[17]));//FPC_CATEGORY_ID
                fpcInfo.setProcessDefinitionID(new ObjectIdentifier(CimObjectUtils.toString(fpc[18]), CimObjectUtils.toString(fpc[19])));//PD_ID,PD_OBJ,
                fpcInfo.setProcessDefinitionType(CimObjectUtils.toString(fpc[20]));//PD_TYPE
                fpcInfo.setCorrespondOperationNumber(CimObjectUtils.toString(fpc[21]));//CORRESPOND_OPE_NO
                fpcInfo.setSkipFalg(CimBooleanUtils.getBoolean(CimObjectUtils.toString(fpc[22])));//SKIP_FLAG
                fpcInfo.setRestrictEquipmentFlag(CimBooleanUtils.getBoolean(CimObjectUtils.toString(fpc[23])));//RESTRICT_EQP_FLAG
                fpcInfo.setEquipmentID(new ObjectIdentifier(CimObjectUtils.toString(fpc[24]), CimObjectUtils.toString(fpc[25])));//EQP_ID,EQP_OBJ,
                fpcInfo.setRecipeParameterChangeType(CimObjectUtils.toString(fpc[26]));//RPRM_CHANGE_TYPE
                fpcInfo.setMachineRecipeID(new ObjectIdentifier(CimObjectUtils.toString(fpc[27]), CimObjectUtils.toString(fpc[28])));//RECIPE_ID,RECIPE_OBJ,
                fpcInfo.setDcDefineID(new ObjectIdentifier(CimObjectUtils.toString(fpc[29]), CimObjectUtils.toString(fpc[30])));//DCDEF_ID,DCDEF_OBJ,
                fpcInfo.setDcSpecID(new ObjectIdentifier(CimObjectUtils.toString(fpc[31]), CimObjectUtils.toString(fpc[32])));//DCSPEC_ID,DCSPEC_OBJ,
                fpcInfo.setSendEmailFlag(CimBooleanUtils.getBoolean(CimObjectUtils.toString(fpc[33])));//SEND_EMAIL_FLAG
                fpcInfo.setHoldLotFlag(CimBooleanUtils.getBoolean(CimObjectUtils.toString(fpc[34])));//HOLD_LOT_FLAG,
                fpcInfo.setDescription(CimObjectUtils.toString(fpc[35]));//DESCRIPTION,
                fpcInfo.setClaimUserID(CimObjectUtils.toString(fpc[36]));//CLAIM_USER_ID
                fpcInfo.setCreateTime(CimObjectUtils.toString(fpc[37]));//CREATE_TIME
                fpcInfo.setUpdateTime(CimObjectUtils.toString(fpc[38]));//UPDATE_TIME

                if (waferIDInfoGetFlag || recipeParmInfoGetFlag) {
                    CimFPCWaferDO cimFPCWaferExam = new CimFPCWaferDO();
                    cimFPCWaferExam.setFpcID(CimObjectUtils.toString(fpc[0]));
                    List<Infos.LotWaferInfo> lotWaferInfos = cimJpaRepository.findAll(Example.of(cimFPCWaferExam)).stream().map(data -> {
                        Infos.LotWaferInfo lotWaferInfo = new Infos.LotWaferInfo();
                        lotWaferInfo.setWaferID(ObjectIdentifier.build(data.getWaferID(), data.getWaferObj()));

                        CimFPCWaferParamterDO cimFPCWaferParamterExam = new CimFPCWaferParamterDO();
                        cimFPCWaferParamterExam.setFpcID(CimObjectUtils.toString(fpc[0]));
                        cimFPCWaferParamterExam.setWaferID(data.getWaferID());
                        lotWaferInfo.setRecipeParameterInfoList(recipeParmInfoGetFlag ?
                                cimJpaRepository.findAll(Example.of(cimFPCWaferParamterExam)).stream()
                                .sorted()
                                .map(childData -> {
                                    Infos.RecipeParameterInfo info = new Infos.RecipeParameterInfo();
                                    info.setSequenceNumber(CimNumberUtils.longValue(childData.getSequenceNumber()));//SEQ_NO
                                    info.setParameterName(childData.getParamterName());//RPARM_NAME
                                    info.setParameterUnit(childData.getParamterUnit());//RPARM_UNIT
                                    info.setParameterDataType(childData.getParamterDataType());//RPARM_DATA_TYPE
                                    info.setParameterLowerLimit(childData.getParamterLowerLimit());//RPARM_LOWER_LIMIT
                                    info.setParameterUpperLimit(childData.getParamterUpperLimit());//RPARM_UPPER_LIMIT
                                    info.setUseCurrentSettingValueFlag(childData.getParamterUseCurrentFlag());//RPARM_USE_CUR_FLAG
                                    info.setParameterTargetValue(childData.getParamterTargetValue());//RPARM_TARGET_VALUE
                                    info.setParameterValue(childData.getParamterValue());//RPARM_VALUE
                                    return info;
                                }).collect(Collectors.toList()) :
                                Collections.emptyList());
                        return lotWaferInfo;
                    }).collect(Collectors.toList());
                    fpcInfo.setLotWaferInfoList(lotWaferInfos);
                }

                if (reticleInfoGetFlag) {
                    CimFPCReticleDO cimFPCReticleExam = new CimFPCReticleDO();
                    cimFPCReticleExam.setFpcID(CimObjectUtils.toString(fpc[0]));
                    List<Infos.ReticleInfo> reticleInfos = cimJpaRepository.findAll(Example.of(cimFPCReticleExam)).stream().sorted()
                            .map(data -> {
                                Infos.ReticleInfo reticleInfo = new Infos.ReticleInfo();
                                //SEQ_NO
                                reticleInfo.setSequenceNumber(data.getSequenceNumber());
                                //RTCL_ID RTCL_OBJ
                                reticleInfo.setReticleID(ObjectIdentifier.build(data.getReticleID(), data.getReticleObj()));
                                //RTCL_GROUP RTCL_GROUP_OBJ
                                reticleInfo.setReticleGroup(ObjectIdentifier.build(data.getReticleGroupID(), data.getReticleGroupObj()));
                                return reticleInfo;
                            }).collect(Collectors.toList());
                    fpcInfo.setReticleInfoList(reticleInfos);
                }

                if (dcSpecItemInfoGetFlag) {
                    CimFPCDataCollectionSpecificationDO specExam = new CimFPCDataCollectionSpecificationDO();
                    specExam.setFpcID(CimObjectUtils.toString(fpc[0]));
                    List<Infos.DCSpecDetailInfo> dcSpecs = cimJpaRepository.findAll(Example.of(specExam)).stream().map(data -> {
                        Infos.DCSpecDetailInfo dcSpec = new Infos.DCSpecDetailInfo();
                        dcSpec.setDataItemName(data.getDataCollectionItemName());               //DCITEM_NAME
                        dcSpec.setScreenLimitUpperRequired(data.getScreenUpperRequired());      //SCRN_UPPER_REQ
                        dcSpec.setScreenLimitUpper(data.getScreenUpperLimit());                 //SCRN_UPPER_LIMIT
                        dcSpec.setActionCodes_uscrn(data.getScreenUpperActions());              //SCRN_UPPER_ACTIONS
                        dcSpec.setScreenLimitLowerRequired(data.getScreenLowerRequired());      //SCRN_LOWER_REQ
                        dcSpec.setScreenLimitLower(data.getScreenLowerLimit());                 //SCRN_LOWER_LIMIT
                        dcSpec.setActionCodes_lscrn(data.getScreenLowerActions());              //SCRN_LOWER_ACTIONS
                        dcSpec.setSpecLimitUpperRequired(data.getSpecificationUpperRequired()); //SPEC_UPPER_REQ
                        dcSpec.setSpecLimitUpper(data.getSpecificationUpperLimit());            //SPEC_UPPER_LIMIT
                        dcSpec.setActionCodes_usl(data.getSpecificationUpperActions());         //SPEC_UPPER_ACTIONS
                        dcSpec.setSpecLimitLowerRequired(data.getSpecificationLowerRequired()); //SPEC_LOWER_REQ
                        dcSpec.setSpecLimitLower(data.getSpecificationLowerLimit());            //SPEC_LOWER_LIMIT
                        dcSpec.setActionCodes_lsl(data.getSpecificationLowerActions());         //SPEC_LOWER_ACTIONS
                        dcSpec.setControlLimitUpperRequired(data.getControlUpperRequired());    //CNTL_UPPER_REQ
                        dcSpec.setControlLimitUpper(data.getControlUpperLimit());               //CNTL_UPPER_LIMIT
                        dcSpec.setActionCodes_ucl(data.getControlUpperActions());               //CNTL_UPPER_ACTIONS
                        dcSpec.setControlLimitLowerRequired(data.getControlLowerRequired());    //CNTL_LOWER_REQ
                        dcSpec.setControlLimitLower(data.getControlLowerLimit());               //CNTL_LOWER_LIMIT
                        dcSpec.setActionCodes_lcl(data.getControlLowerActions());               //CNTL_LOWER_ACTIONS
                        dcSpec.setTarget(data.getDataCollectionItemTarget());                   //DCITEM_TARGET
                        dcSpec.setTag(data.getDataCollectionItemTag());                         //DCITEM_TAG
                        dcSpec.setDcSpecGroup(data.getDataCollectionSpecificationGroup());      //DC_SPEC_GROUP
                        return dcSpec;
                    }).collect(Collectors.toList());
                    fpcInfo.setDcSpecList(dcSpecs);
                }

                List<Object[]> fpcCoropes = cimJpaRepository.query(
                        "SELECT     CORR_OPE_NO, " +
                                "           EDC_SPEC_GROUP " +
                                "FROM       OSDOC_COROPE " +
                                "WHERE      DOC_ID = ?1", CimObjectUtils.toString(fpc[0]));
                List<Infos.CorrespondingOperationInfo> correspondingOperationInfos = new ArrayList<>();
                fpcInfo.setCorrespondingOperationInfoList(correspondingOperationInfos);
                for (Object[] fpcCorope : fpcCoropes) {
                    Infos.CorrespondingOperationInfo correspondingOperationInfo = new Infos.CorrespondingOperationInfo();
                    correspondingOperationInfos.add(correspondingOperationInfo);
                    correspondingOperationInfo.setCorrespondingOperationNumber(CimObjectUtils.toString(fpcCorope[0]));//CORRESPOND_OPE_NO
                    correspondingOperationInfo.setDcSpecGroup(CimObjectUtils.toString(fpcCorope[1]));//DC_SPEC_GROUP
                }

                String multiCorrespondingOperationoMode = StandardProperties.OM_EDC_MULTI_CORRESPOND_FLAG.getValue();
                String correspondOperationNO = fpcInfo.getCorrespondOperationNumber();
                if (CimStringUtils.equals(multiCorrespondingOperationoMode, "1")) {
                    if (!CimObjectUtils.isEmpty(fpcCoropes) && !CimObjectUtils.isEmpty(correspondOperationNO)) {
                        fpcInfo.setCorrespondOperationNumber("");
                        log.debug("single correspondingOperNo --------------> {}", correspondOperationNO);
                        log.debug("multi correspondingOperNo length --------> {}", fpcCoropes.size());
                    } else {
                        log.debug("multi correspondingOperNo length --------> {}", fpcCoropes.size());
                    }
                } else {
                    if (!CimObjectUtils.isEmpty(fpcCoropes) && !CimObjectUtils.isEmpty(correspondOperationNO)) {
                        log.debug("single correspondingOperNo --------------> {}", correspondOperationNO);
                        fpcInfo.setCorrespondingOperationInfoList(null);
                    } else {
                        log.debug("single correspondingOperNo --------------> {}", correspondOperationNO);
                    }
                }
            }
        }

        //set runCard flag for runCard - added by Nyx
        for (Infos.FPCInfo fpcInfo : fpcInfos) {
            CimFPCWaferDO cimFPCWaferExam = new CimFPCWaferDO();
            cimFPCWaferExam.setFpcID(fpcInfo.getFpcID());
            List<ObjectIdentifier> docWafer = cimJpaRepository.findAll(Example.of(cimFPCWaferExam)).stream()
                    .map(data -> ObjectIdentifier.build(data.getWaferID(), data.getWaferObj()))
                    .collect(Collectors.toList());
            List<Infos.LotWaferInfo> tmpLotWaferList = fpcInfo.getLotWaferInfoList();
            List<Infos.LotWaferInfo> actualLotWaferList = new ArrayList<>();
            if (CimArrayUtils.isNotEmpty(docWafer)){
                docWafer.forEach(data -> {
                    Infos.LotWaferInfo lotWaferInfo = new Infos.LotWaferInfo();
                    lotWaferInfo.setWaferID(data);
                    actualLotWaferList.add(lotWaferInfo);
                });
            }
            //modify lotWaferList to get runcard info
            fpcInfo.setLotWaferInfoList(actualLotWaferList);
            boolean runCardFlag = Objects.nonNull(runCardMethod.getRunCardInfoByDoc(objCommon,fpcInfo));
            fpcInfo.setRunCardFlag(runCardFlag);
            //reset lotWaferList to return
            fpcInfo.setLotWaferInfoList(tmpLotWaferList);
        }
        return fpcInfos;
    }

    @Override
    public List<Infos.FPCDispatchEqpInfo> fpcLotDispatchEquipmentsInfoCreate(Infos.ObjCommon objCommon, ObjectIdentifier lotID) {

        List<Infos.FPCDispatchEqpInfo> fpcDispatchEqpInfosObj = new ArrayList<>();
        //-------------------------------------
        // Get FPC Information for input Lot.
        //-------------------------------------
        // call parameters
        List<Infos.FPCInfo> fpcInfos = lotMethod.lotCurrentFPCInfoGet(objCommon, lotID, null, true, false, false, false);

        List<Infos.FPCDispatchEqpInfo> fpcDispatchEqpInfos = fpcInfoConsistencyCheck(objCommon, fpcInfos, false, false);

        // Collect EQP/MachineRecipe/PD(Operation)/DCDef/DCSpec from FPCInfo as FPCProcessCondition.
        // And check whiteFlag/FPCcategory with the FPCProcessCondition by each FPCInfoRecord.

        int i;
        for (Infos.FPCInfo fpcInfo : fpcInfos) {
            List<Infos.FPCProcessCondition> strFPCProcessConditionList = new ArrayList<>(); // Max for EQP/MachineRecipe/PD/DCDef/DCSpec items.
            Integer condUsed = 0;
            // 1: for Equipment
            ObjectIdentifier tmpObjectID = fpcInfo.getEquipmentID();
            if (!ObjectIdentifier.isEmpty(tmpObjectID)) {
                Infos.FPCProcessCondition fpcProcessCondition = new Infos.FPCProcessCondition();
                strFPCProcessConditionList.add(fpcProcessCondition);

                fpcProcessCondition.setObjectID(tmpObjectID);
                fpcProcessCondition.setObjectType(BizConstant.SP_FPC_PROCESSCONDITION_TYPE_EQUIPMENT);
                log.debug("ProcessCondition (EQP)  {}", tmpObjectID);
                condUsed++;
            }

            // 2: for MachineRecipe
            tmpObjectID = fpcInfo.getMachineRecipeID();
            if (!ObjectIdentifier.isEmpty(tmpObjectID)) {
                Infos.FPCProcessCondition fpcProcessCondition = new Infos.FPCProcessCondition();
                strFPCProcessConditionList.add(fpcProcessCondition);

                fpcProcessCondition.setObjectID(tmpObjectID);
                fpcProcessCondition.setObjectType(BizConstant.SP_FPC_PROCESSCONDITION_TYPE_MACHINERECIPE);
                log.debug("ProcessCondition (MachineRecipe)  {}", tmpObjectID);
                condUsed++;
            }

            // 3: for ProcessDefinition
            tmpObjectID = fpcInfo.getProcessDefinitionID();
            if (!ObjectIdentifier.isEmpty(tmpObjectID)) {
                Infos.FPCProcessCondition fpcProcessCondition = new Infos.FPCProcessCondition();
                strFPCProcessConditionList.add(fpcProcessCondition);

                fpcProcessCondition.setObjectID(tmpObjectID);
                fpcProcessCondition.setObjectType(BizConstant.SP_FPC_PROCESSCONDITION_TYPE_PD);
                log.debug("ProcessCondition (PD)  {}", tmpObjectID);
                condUsed++;
            }

            // 4: for DCDef
            tmpObjectID = fpcInfo.getDcDefineID();
            if (!ObjectIdentifier.isEmpty(tmpObjectID)) {
                Infos.FPCProcessCondition fpcProcessCondition = new Infos.FPCProcessCondition();
                strFPCProcessConditionList.add(fpcProcessCondition);

                fpcProcessCondition.setObjectID(tmpObjectID);
                fpcProcessCondition.setObjectType(BizConstant.SP_FPC_PROCESSCONDITION_TYPE_DCDEF);
                log.debug("ProcessCondition (DCDef)  {}", tmpObjectID);
                condUsed++;
            }

            // 5: for DCSpec
            tmpObjectID = fpcInfo.getDcSpecID();
            if (!ObjectIdentifier.isEmpty(tmpObjectID)) {
                Infos.FPCProcessCondition fpcProcessCondition = new Infos.FPCProcessCondition();
                strFPCProcessConditionList.add(fpcProcessCondition);

                fpcProcessCondition.setObjectID(tmpObjectID);
                fpcProcessCondition.setObjectType(BizConstant.SP_FPC_PROCESSCONDITION_TYPE_DCSPEC);
                log.debug("ProcessCondition (DCSpec)  {}", tmpObjectID);
                condUsed++;
            }

            log.debug("Used processConditions {}", condUsed);

            if (condUsed > 0) {
                log.debug("There is some processConditions to be checked. {}", condUsed);
                fpcProcessConditionCheck(objCommon, "", strFPCProcessConditionList, true, false);
            }
        }

        //--------------------------------------
        // Mandatory Operation Information get
        //--------------------------------------
        com.fa.cim.newcore.bo.product.CimLot lot = baseCoreFactory.getBO(com.fa.cim.newcore.bo.product.CimLot.class, lotID);
        if (null == lot) {
            log.error("lot == null");
            throw new ServiceException(retCodeConfig.getNotFoundLot());
        }

        com.fa.cim.newcore.bo.pd.CimProcessOperation aProcessOperation = lot.getProcessOperation();
        if (null == aProcessOperation) {
            log.debug("Not found operation for lot {}", lotID);
            throw new ServiceException(retCodeConfig.getNotFoundProcessOperation());
        }

        Boolean mandatoryOperationFlag = aProcessOperation.isMandatoryOperation();
        if (mandatoryOperationFlag) {
            for (Infos.FPCDispatchEqpInfo fpcDispatchEqpInfo : fpcDispatchEqpInfos) {
                if (CimBooleanUtils.isTrue(fpcDispatchEqpInfo.getSkipFlag())) {
                    log.debug("SkipFlag is true, but mandatoryOperationFlag also true.");
                    throw new ServiceException(retCodeConfig.getCannotPassOperation());
                }
            }
        }

        // Match conditions.
        for (Infos.FPCDispatchEqpInfo fpcDispatchEqpInfo : fpcDispatchEqpInfos) {
            // If holdLotFlag specified, then all other directive must be ignore.
            if (CimBooleanUtils.isTrue(fpcDispatchEqpInfo.getHoldLotFlag())) {
                log.debug("HoldLot specified for this Group {} ", fpcDispatchEqpInfo.getFPCGroupNo());
                fpcDispatchEqpInfo.setSkipFlag(false);
                fpcDispatchEqpInfo.setRestrictEqpFlag(false);
                fpcDispatchEqpInfo.setDispatchEqpIDs(null);
            }
            // If skipFlag specified, then all directives for the target operation must be ignore.
            else if (fpcDispatchEqpInfo.getSkipFlag()) {
                log.debug("Skip specified for this Group {}", fpcDispatchEqpInfo.getFPCGroupNo());
                fpcDispatchEqpInfo.setRestrictEqpFlag(false);
                fpcDispatchEqpInfo.setDispatchEqpIDs(null);
            }
        }

        //-----------------------------------------------
        // Get Wafers in Lot
        //
        // waferCountInLot  : wafer count in lot.
        // lotWaferIDList[] : wafer ID list in the lot.
        //-----------------------------------------------
        Inputs.ObjLotWaferIDListGetDRIn objLotWaferIDListGetDRIn = new Inputs.ObjLotWaferIDListGetDRIn();
        objLotWaferIDListGetDRIn.setLotID(lotID);
        objLotWaferIDListGetDRIn.setScrapCheckFlag(true);
        List<ObjectIdentifier> lotWaferIDList = lotMethod.lotWaferIDListGetDR(objCommon, objLotWaferIDListGetDRIn);
        Integer waferCountInLot = CimArrayUtils.getSize(lotWaferIDList);

        //---------------------------------------------
        // Collect Wafers in FPCInfo
        //
        // waferIDList[]  : wafer ID list in FPCInfo.
        // fpcWaferCount  : wafer count in FPCInfo.
        //---------------------------------------------
        List<ObjectIdentifier> waferIDList = new ArrayList<>();
        fpcDispatchEqpInfos.forEach(fpcDispatchEqpInfo -> waferIDList.addAll(fpcDispatchEqpInfo.getWaferIDs()));
        int fpcWaferCount = CimArrayUtils.getSize(waferIDList);

        //--------------------------
        // Build output structures
        //--------------------------
        for (Infos.FPCDispatchEqpInfo fpcDispatchEqpInfo : fpcDispatchEqpInfos) {
            List<ObjectIdentifier> waferIDs = fpcDispatchEqpInfo.getWaferIDs();
            List<ObjectIdentifier> tmpWaferIDList = new ArrayList<>();

            for (ObjectIdentifier tmpWaferID : waferIDs) {
                Boolean foundInLot = false;
                for (ObjectIdentifier lotWaferID : lotWaferIDList) {
                    if (ObjectIdentifier.equalsWithValue(lotWaferID, tmpWaferID)) {
                        foundInLot = true;
                        break;
                    }
                }
                if (foundInLot) {
                    tmpWaferIDList.add(tmpWaferID);
                }
            }

            if (!CimObjectUtils.isEmpty(tmpWaferIDList)) {
                fpcDispatchEqpInfo.setWaferIDs(tmpWaferIDList);
                fpcDispatchEqpInfosObj.add(fpcDispatchEqpInfo);
            }
        }

        //Judge for Auto Split.
        if (!CimObjectUtils.isEmpty(fpcDispatchEqpInfosObj)) {
            // check if all wafers in lot were in FPCInfo or not.
            Boolean allWaferCoveredByFPC = true;
            for (i = 0; i < waferCountInLot; i++) {
                int j;
                for (j = 0; j < fpcWaferCount; j++) {
                    if (ObjectIdentifier.equalsWithValue(lotWaferIDList.get(i), waferIDList.get(j))) {
                        log.info("Wafer in lot found in FPCInfo.", lotWaferIDList.get(i));
                        break;
                    }
                }
                if (j == fpcWaferCount) {
                    log.info("Wafer in lot NOT found in FPCInfo.", lotWaferIDList.get(i));
                    allWaferCoveredByFPC = false;
                    break;
                }
            }

            for (Infos.FPCDispatchEqpInfo fpcDispatchEqpInfo : fpcDispatchEqpInfosObj) {
                if (CimBooleanUtils.isTrue(fpcDispatchEqpInfo.getHoldLotFlag())) {
                    log.info("HoldLot Specified. AutoSplit does not occur.");
                    fpcDispatchEqpInfo.setSplitFlag(false);
                } else {
                    log.info("Set AutoSplit");
                    fpcDispatchEqpInfo.setSplitFlag(true);
                }
            }

            if (allWaferCoveredByFPC) {
                log.info("AutoSplit for the last DOC group is not required.");
                // actualGroups-1 access is safe because now actualGroups > 0
                fpcDispatchEqpInfosObj.get(CimArrayUtils.getSize(fpcDispatchEqpInfosObj) - 1).setSplitFlag(false);
            }
        }
        return fpcDispatchEqpInfosObj;
    }

    @Override
    public void fpcLotDispatchEquipmentsSet(Infos.ObjCommon objCommon, Boolean restrictEqp, ObjectIdentifier lotID, List<ObjectIdentifier> equipmentIDs) {


        log.info("PPTManager_i::FPCLotDispatchEquipments_Set");
        // restrictEqp Flag Check
        Integer inputEqpCount = CimArrayUtils.getSize(equipmentIDs);
        log.info("Input EQP count {}", inputEqpCount);
        if (restrictEqp && inputEqpCount != 1) {
            log.info("restrictEqp is true, but input EQP count is {}", inputEqpCount);
            throw new ServiceException(retCodeConfig.getInvalidParameter());

        } else if (inputEqpCount == 0) {
            log.info("No Equipment Input.");
            return;
        }

        // Lot Existence Check
        com.fa.cim.newcore.bo.product.CimLot lot = baseCoreFactory.getBO(com.fa.cim.newcore.bo.product.CimLot.class, lotID);
        if (null == lot) {
            log.error("lot == null");
            throw new ServiceException(retCodeConfig.getNotFoundLot());
        }

        // Equipment Duplication Check
        int i = 0;
        Boolean duplicateInputEqpFlag = false;
        for (i = 1; i < inputEqpCount; i++)   //if the inputEqpCount == 1 then skip this loop.
        {
            int j = 0;
            for (j = 0; j < i; j++) {
                if (ObjectIdentifier.equalsWithValue(equipmentIDs.get(i), equipmentIDs.get(j))) {
                    log.info("inputEqpID duplicate {}", j, i, equipmentIDs.get(i));
                    duplicateInputEqpFlag = true;
                    break;
                }
            }
            if (duplicateInputEqpFlag) {
                break;
            }
        }
        if (duplicateInputEqpFlag) {
            log.info("detect input Eqp ID duplication.");
            throw new ServiceException(retCodeConfig.getInvalidParameter());
        }

        // get Object and check White Flag for Input Equipment
        List<CimMachine> validInMachineList = new ArrayList<>();
        List<ObjectIdentifier> validInputEqpIDs = new ArrayList<>();

        Integer validEqpCount = 0;
        CimMachine aMachine;
        Boolean inputContainsNonWhiteFlag = false;
        for (i = 0; i < inputEqpCount; i++) {
            log.info("get Machine Obj {}", equipmentIDs.get(i));
            aMachine = baseCoreFactory.getBO(CimMachine.class, equipmentIDs.get(i));
            if (null == aMachine) {
                log.error("equipment == null");
                throw new ServiceException(new OmCode(retCodeConfig.getNotFoundEqp(), equipmentIDs.get(i).getValue()));
            }

            Boolean isWhiteFlag = false;
            //check white Definition flag.
            isWhiteFlag = aMachine.isWhiteFlagOn();
            if (isWhiteFlag) {
                log.info("This machine is WHITE {}", equipmentIDs.get(i));

                if (inputContainsNonWhiteFlag) {
                    log.info("  skip this EQP.");
                } else {
                    log.info("  add this EQP as WHITE EQP");
                    validInputEqpIDs.add(equipmentIDs.get(i));
                    validInMachineList.add(aMachine);
                    validEqpCount++;
                }
            } else {
                log.info("This machine is NON-WHITE", equipmentIDs.get(i));

                if (inputContainsNonWhiteFlag) {
                    log.info("  add this EQP as NON-WHITE EQP");
                    validInputEqpIDs.add(validEqpCount, equipmentIDs.get(i));
                    validInMachineList.add(validEqpCount, aMachine);
                    validEqpCount++;
                } else {
                    log.info("  reset WHITE EQP List and add this EQP as NON-WHITE EQP");
                    validInputEqpIDs.add(validEqpCount, equipmentIDs.get(i));
                    validInMachineList.add(validEqpCount, aMachine);
                    validEqpCount = 1;
                }
                inputContainsNonWhiteFlag = true;
            }
        }
        // DO NOT USE equipmentIDs[] BELOW HERE!!!

        // get Lot HoldState
        String holdState = lot.getLotHoldState();
        log.info("Lot's holdState is {}", holdState);

        Boolean lotNotOnHoldFlag = false;
        if (BizConstant.SP_DURABLE_HOLDSTATE_NOTONHOLD.equals(holdState)) {
            log.info("Lot is NotOnHold.");
            lotNotOnHoldFlag = true;
        }

        List<ObjectIdentifier> queuedMachineIDList = new ArrayList<>();
        List<CimMachine> queuedMachineObjList = new ArrayList<>();
        // primary assignment is for restricted case
        Integer dispatchedEqpCount = 0;
        if (restrictEqp) {
            if (validEqpCount != 1) {
                log.info("restrictEqp is true, but valid EQP count is {}", validEqpCount);
                log.info("This must not be occured. Please check the logic.");
                throw new ServiceException(retCodeConfig.getInvalidParameter());
            }

            if (lotNotOnHoldFlag) {
                log.info("Lot is NotOnHold && restrictEqp is true");
                newDispatchingManager.removeFromQueue(lot);
            } else {
                log.info("Lot is ONHOLD && restrictEqp is true");
                newDispatchingManager.removeFromHoldQueue(lot);
            }
            queuedMachineIDList.add(validInputEqpIDs.get(0));
            queuedMachineObjList.add(validInMachineList.get(0));
            dispatchedEqpCount = 1;
        } else {
            List<CimMachine> aCurrentQMachineListPtr;

            if (lotNotOnHoldFlag) {
                log.info("Lot is NotOnHold && restrictEqp is false");
                aCurrentQMachineListPtr = lot.getQueuedMachines();
            } else {
                log.info("Lot is ONHOLD && restrictEqp is false");
                aCurrentQMachineListPtr = lot.getHoldQueuedMachines();
            }

            int currentQCount = CimArrayUtils.getSize(aCurrentQMachineListPtr);

            log.info("Current QueuedMachine count {}", currentQCount);
            for (i = 0; i < currentQCount; i++) {
                if (inputContainsNonWhiteFlag) {
                    log.info("Remove whiteEQP required.");

                    Boolean isWhite = false;
                    isWhite = aCurrentQMachineListPtr.get(i).isWhiteFlagOn();
                    if (isWhite) {
                        log.info("  This queued EQP is white. now skip.");
                        continue;    // for next queued EQP.
                    } else {
                        log.info("  This queued EQP is non-white. add it.");
                    }
                }

                ObjectIdentifier tmpMachineID = new ObjectIdentifier(aCurrentQMachineListPtr.get(i).getIdentifier(), aCurrentQMachineListPtr.get(i).getPrimaryKey());
                queuedMachineIDList.add(tmpMachineID);
                queuedMachineObjList.add(dispatchedEqpCount, aCurrentQMachineListPtr.get(i));
                dispatchedEqpCount++;
            }

            for (i = 0; i < validEqpCount; i++) {
                Boolean addForQueue = true;
                Integer j = 0;
                for (j = 0; j < dispatchedEqpCount; j++) {
                    if (ObjectIdentifier.equalsWithValue(validInputEqpIDs.get(i), queuedMachineIDList.get(j))) {
                        log.info("Already queued machine found. {}", validInputEqpIDs.get(i));
                        addForQueue = false;
                        break;
                    }
                }
                if (addForQueue) {
                    log.info("Add the lot for equipment queue.", validInputEqpIDs.get(i));

                    queuedMachineIDList.add(dispatchedEqpCount, validInputEqpIDs.get(i));
                    queuedMachineObjList.add(dispatchedEqpCount, validInMachineList.get(i));
                    dispatchedEqpCount++;
                }
            }
        }
        if (lotNotOnHoldFlag) {
            lot.setQueuedMachines(queuedMachineObjList);
        } else {
            lot.setHoldQueuedMachines(queuedMachineObjList);
        }
        lot.setQueuedTimeStamp(CimDateUtils.getCurrentTimeStamp());
    }

    @Override
    public List<Infos.FPCDispatchEqpInfo> fpcInfoConsistencyCheck(Infos.ObjCommon objCommon, List<Infos.FPCInfo> strFPCInfoList, Boolean recipeParmCheckFlag, Boolean dcSpecItemCheckFlag) {

        List<Infos.FPCDispatchEqpInfo> fpcDispatchEqpInfos = new ArrayList<>();
        //-----------------------------------------------------------------------------------------
        // Analyze input FPC Info
        //
        //Limits/Counts
        //  FPCGroupNoLimit       : Range max of FPC GroupNo. (default 25)
        //  FPCGroouSeqLimit      : Sequence Limit for FPC Groups. (default 26)
        //  MaxFPCGroupNo         : Max GroupNo of FPC Groups.
        //  FPCInfoCount          : Count of input FPCInfo.
        //  FPCGroupCount         : Count of FPC Gropus.
        //  FPCGroupInfoCount[gr] : Count of FPC Info for each Group.
        //  groupWaferCount[gr]   : Count of wafers for each Group.
        //  FPCWaferCount         : Count of wafers in input FPC informations.
        //
        //
        //Flags
        //  holdLotFlag            : hold lot Flag
        //  sendEmailFlag          : send E-mail Flag
        //  skipFlagOfGroup[gr]    : skip flag of each Group.
        //  groupEQPRestricted[gr] : RestrictEQP flag of each Group.
        //
        //Lists
        //  FPCGroupNoList[]       : GroupNo of each input FPC information.
        //  waferIDList[waf]       : Wafer ID List in input FPC informations.
        //  waferGroupNoList[waf]  : GroupNo List for each wafer.
        //
        //Indexes
        //  FPCGroupNumIndex[]     : Index for reading FPC Items along with each GroupNo.
        //  FPCGroupNumChange[]    : Flags of GroupNo change position when reading with FPCGroupNumIndex[]
        //
        //-----------------------------------------------------------------------------------------

        // get limit.
        String maxWafersInLot = StandardProperties.OM_MAX_WAFER_COUNT_FOR_LOT.getValue();
        Integer FPCGroupNoLimit = Integer.valueOf(maxWafersInLot);
        Integer FPCGroupSeqLimit = FPCGroupNoLimit + 1;

        int FPCInfoCount = CimArrayUtils.getSize(strFPCInfoList);
        // base list.
        List<Integer> FPCGroupNoList = new ArrayList<>();
        // sequences for groups.
        List<Integer> FPCGroupInfoCount = new ArrayList<>();
        List<Boolean> groupEQPRestricted = new ArrayList<>();
        List<Boolean> skipFlagOfGroup = new ArrayList<>();
        List<Integer> groupWaferCount = new ArrayList<>();

        for (int i = 0; i < FPCGroupSeqLimit; i++) {
            FPCGroupInfoCount.add(0);
            groupEQPRestricted.add(false);
            skipFlagOfGroup.add(false);
            groupWaferCount.add(0);
        }

        // build groupNo List and count FPCInfo in each groupNo
        for (int i = 0; i < FPCInfoCount; i++) {
            Integer FPCGroupNum = strFPCInfoList.get(i).getFpcGroupNumber();

            if (FPCGroupNum < 0 || FPCGroupNum > FPCGroupNoLimit) {
                log.info("DOC GroupNo out of range {} {}", FPCGroupNum, FPCGroupNoLimit);
                throw new ServiceException(retCodeConfig.getFpcInvalidGroupNumber());
            } else {
                FPCGroupNoList.add(FPCGroupNum);
                FPCGroupInfoCount.set(FPCGroupNum, FPCGroupInfoCount.get(FPCGroupNum) + 1);
            }
        }

        int FPCGroupCount = 0;
        int MaxFPCGroupNo = 0;
        List<Integer> FPCGroupNumIndex = new ArrayList<>();
        List<Boolean> FPCGroupNumChange = new ArrayList<>();

        for (int i = 0; i < FPCInfoCount; i++) {
            FPCGroupNumIndex.add(0);
            FPCGroupNumChange.add(false);
        }

        // build index
        int usedIndex = 0;
        int curGrpNo;
        for (curGrpNo = 0; curGrpNo < FPCGroupInfoCount.size(); curGrpNo++) {
            if (FPCGroupInfoCount.get(curGrpNo) == 0) {
                continue;
            }

            MaxFPCGroupNo = curGrpNo;
            FPCGroupCount++;
            FPCGroupNumChange.set(usedIndex, true);

            for (int i = 0; i < FPCInfoCount; i++) {
                if (FPCGroupNoList.get(i) != curGrpNo) {
                    continue;
                }

                FPCGroupNumIndex.set(usedIndex, i);
                usedIndex++;
            }
        }

        log.info("FPCInfo count {}", FPCInfoCount);
        log.info("Count of FPCGroup {}", FPCGroupCount);
        log.info("Max FPCGroupNo {}", MaxFPCGroupNo);

        //Index check.
        Integer tmpPos;
        log.info("########## INDEX CHECK ##########");
        for (int i = 0; i < FPCInfoCount; i++) {
            log.info("  FPCInfo (order by group)", i);
            tmpPos = FPCGroupNumIndex.get(i);
            log.info("  FPCInfo (orig  position)", tmpPos);
            curGrpNo = FPCGroupNoList.get(tmpPos);
            log.info("    Group Number", curGrpNo);
            if (FPCGroupNumChange.get(i)) {
                log.info("      is First Record in Group : TRUE");
            } else {
                log.info("      is First Record in Group : FALSE");
            }
            log.info("      info count for this Group {}", FPCGroupInfoCount.get(curGrpNo));
            log.info("##########");
        }
        //end Index check.

        //--------------------------------------------
        // Consistency check for each FPCInfo Items.
        //
        //--------------------------------------------
        for (int i = 0; i < FPCInfoCount; i++) {
            Infos.FPCInfo fpcInfo = strFPCInfoList.get(i);
            // if FPCGroupNo == 0 then the FPCType must be ByLot
            if (0 == fpcInfo.getFpcGroupNumber()) {
                if (!BizConstant.SP_FPCTYPE_BYLOT.equals(fpcInfo.getFpcType())) {
                    log.info("Inconsistent FPCType-GroupNo detected. {}", i);
                    throw new ServiceException(retCodeConfig.getFpcGroupnoAndTypeMismatch());
                }
            } else    // FPCGroupNo != 0 then the FPCType must be ByWafer
            {
                if (!BizConstant.SP_FPCTYPE_BYWAFER.equals(fpcInfo.getFpcType())) {
                    log.info("Inconsistent FPCType-GroupNo detected. {}", i);
                    throw new ServiceException(retCodeConfig.getFpcGroupnoAndTypeMismatch());
                }
            }

            // if MachineRecipeID specified, check the ID is not active version.
            ObjectIdentifier machineRecipeID = fpcInfo.getMachineRecipeID();
            if (!ObjectIdentifier.isEmpty(machineRecipeID)) {
                log.info("Check MachineRecipeID", machineRecipeID);
                String ver = BaseStaticMethod.extractVersionFromID(machineRecipeID.getValue());
                if (BizConstant.SP_ACTIVE_VERSION.equals(ver)) {
                    log.info("Invalid machineRecipeID (Active Version) detected. {}", i);
                    throw new ServiceException(retCodeConfig.getFpcActiveversionError());
                }
            }

            // if DCSpecID is specified then DCDefID must be filled.
            if (!ObjectIdentifier.isEmpty(fpcInfo.getDcSpecID())) {
                if (ObjectIdentifier.isEmpty(fpcInfo.getDcDefineID())) {
                    log.info("Inconsistent dcDef-dcSpec detected. {}", i);
                    throw new ServiceException(retCodeConfig.getFpcDcdefRequiredForDcspec());
                }
            }

            // if recipeParameterChangeType is specified, then EquipmentID required.
            String recipeParameterChangeType = fpcInfo.getRecipeParameterChangeType();
            if (!CimObjectUtils.isEmpty(recipeParameterChangeType)) {
                if (ObjectIdentifier.isEmpty(fpcInfo.getEquipmentID())) {
                    log.info("Inconsistent eqpID-recipeParam detected. {}", i);
                    throw new ServiceException(retCodeConfig.getFpcEqpidRequiredForRcpParam());
                }
            }

            // recipe parameter additional check.
            List<Infos.LotWaferInfo> lotWaferInfoList = fpcInfo.getLotWaferInfoList();
            if (recipeParmCheckFlag) {
                log.info("Recipe Parameter Additional Check.");
                Integer tmpWaferCount = CimArrayUtils.getSize(lotWaferInfoList);
                // recipeParm needs wafer.
                if (CimObjectUtils.isEmpty(lotWaferInfoList)) {
                    log.info("Inconsistent recipeParameter (no wafer...) detected. {}", i);
                    throw new ServiceException(retCodeConfig.getFpcInvalidRcpParamchange());
                }

                // if wafer has recpieParameters then recipeParameterChangeType is required.
                // if the recipeParameterChangeType is not specified, then wafer must not have recipeParameter.
                List<Infos.RecipeParameterInfo> recipeParameterInfoList = lotWaferInfoList.get(0).getRecipeParameterInfoList();
                int rcpParamCount = CimArrayUtils.getSize(recipeParameterInfoList);
                if ((CimObjectUtils.isEmpty(recipeParameterChangeType) && 0 != rcpParamCount)
                        || (!CimObjectUtils.isEmpty(recipeParameterChangeType) && 0 == rcpParamCount)) {
                    log.info("Inconsistent recipeParam-ChangeType detected. {}", i);
                    throw new ServiceException(retCodeConfig.getFpcInvalidRcpParamchange());

                }

                // if recipeParameterChangeType is specified then the type is "ByLot" or "ByWafer"
                if (!CimObjectUtils.isEmpty(recipeParameterChangeType)
                        && !CimStringUtils.equals(recipeParameterChangeType, BizConstant.SP_RPARM_CHANGETYPE_BYLOT)
                        && !CimStringUtils.equals(recipeParameterChangeType, BizConstant.SP_RPARM_CHANGETYPE_BYWAFER)) {
                    log.info("Invalid recipeParmChangeType detected. {}", i);
                    throw new ServiceException(retCodeConfig.getFpcInvalidRcpParamchange());
                }

                // recipeParams in wafer must not duplicate.
                for (int j = 1; j < rcpParamCount; j++) {
                    for (int k = 0; k < j; k++) {
                        if (CimStringUtils.equals(recipeParameterInfoList.get(j).getParameterName(),
                                recipeParameterInfoList.get(k).getParameterName())) {
                            log.info("recipeParam duplication detected.", i);
                            throw new ServiceException(retCodeConfig.getFpcInvalidRcpParamchange());
                        }
                    }
                }

                // all wafers in FPCinfo has same recipeParameter Name. (except the order when byWafer)

                Boolean changeTypeIsByLot = false;
                if (CimStringUtils.equals(recipeParameterChangeType, BizConstant.SP_RPARM_CHANGETYPE_BYLOT)) {
                    log.info("RecipeParam ChgType is ByLot");
                    changeTypeIsByLot = true;
                } else {
                    log.info("RecipeParam ChgType is ByWafer");
                }

                log.info("WaferCount", tmpWaferCount);
                for (int j = 1; j < tmpWaferCount; j++) {
                    log.info("check for wafer (versus 0)", j);

                    // recipeParameter count must be same for each wafers.
                    if (CimArrayUtils.getSize(lotWaferInfoList.get(j).getRecipeParameterInfoList()) != rcpParamCount) {
                        log.info("Invalid recipeParam (count mismatch) detected. {}", i);
                        throw new ServiceException(retCodeConfig.getFpcEqpidRequiredForRcpParam());
                    }

                    // do not change the loop order, because the duplication check for 2nd(or later) wafer is omitted.
                    // (There is the probability which wafer[j].param[k].parameterName == wafer[0].param[readPos].parameterName
                    //  consists of some k and unique readPos.)
                    Integer readPos;
                    for (readPos = 0; readPos < rcpParamCount; readPos++) {
                        Boolean itemFound = false;
                        Integer k;
                        for (k = 0; k < rcpParamCount; k++) {
                            if (CimStringUtils.equals(lotWaferInfoList.get(j).getRecipeParameterInfoList().get(k).getParameterName(),
                                    lotWaferInfoList.get(0).getRecipeParameterInfoList().get(readPos).getParameterName())) {
                                log.info("  Recipe Param found. {}", lotWaferInfoList.get(0).getRecipeParameterInfoList().get(readPos).getParameterName());
                                itemFound = true;
                                break;
                            }
                        }
                        if (!itemFound) {
                            log.info("Invalid recipeParam (item name mismatch) detected. {}", i);
                            throw new ServiceException(retCodeConfig.getFpcEqpidRequiredForRcpParam());
                        }
                        // if the parameter change type is "ByLot" then all parameter and the order must be same.
                        else if (changeTypeIsByLot) {
                            Infos.RecipeParameterInfo recipeParameterInfoK = lotWaferInfoList.get(j).getRecipeParameterInfoList().get(k);
                            Infos.RecipeParameterInfo recipeParameterInfoReadPos = lotWaferInfoList.get(0).getRecipeParameterInfoList().get(readPos);
                            if (!k.equals(readPos)) {
                                log.info("  Recipe Param Order is different in spite of the ByLot changeType... {} {}", readPos, k);
                                throw new ServiceException(retCodeConfig.getFpcEqpidRequiredForRcpParam());
                            }
                            if (recipeParameterInfoK.getSequenceNumber()
                                    != recipeParameterInfoReadPos.getSequenceNumber()
                                    || !CimStringUtils.equals(recipeParameterInfoK.getParameterUnit(),
                                    recipeParameterInfoReadPos.getParameterUnit())
                                    || !CimStringUtils.equals(recipeParameterInfoK.getParameterDataType(),
                                    recipeParameterInfoReadPos.getParameterDataType())
                                    || !CimStringUtils.equals(recipeParameterInfoK.getParameterLowerLimit(),
                                    recipeParameterInfoReadPos.getParameterLowerLimit())
                                    || !CimStringUtils.equals(recipeParameterInfoK.getParameterUpperLimit(),
                                    recipeParameterInfoReadPos.getParameterUpperLimit())
                                    || recipeParameterInfoK.isUseCurrentSettingValueFlag()
                                    != recipeParameterInfoReadPos.isUseCurrentSettingValueFlag()
                                    || !CimStringUtils.equals(recipeParameterInfoK.getParameterTargetValue(),
                                    recipeParameterInfoReadPos.getParameterTargetValue())
                                    || !CimStringUtils.equals(recipeParameterInfoK.getParameterValue(),
                                    recipeParameterInfoReadPos.getParameterValue())) {
                                log.info("Invalid recipeParam (some item mismatch when ByLot) detected.", i);
                                throw new ServiceException(retCodeConfig.getFpcEqpidRequiredForRcpParam());
                            }
                        }
                    }
                }
            }

            // DCSpec additional check.
            if (dcSpecItemCheckFlag) {
                log.info("DCSpec Additional Check.");
                Integer DCSpecCount = CimArrayUtils.getSize(fpcInfo.getDcSpecList());
                log.info("DCSpec Item count {}", DCSpecCount);
                if (ObjectIdentifier.isEmpty(fpcInfo.getDcSpecID()) && 0 != DCSpecCount) {
                    log.info("Inconsistent dcSpec-dcItem detected. {}", i);
                    throw new ServiceException(retCodeConfig.getFpcInvalidDcdefChange());
                }

                if (DCSpecCount > 1) {
                    log.info("There are 2 or more DCItems. Now check duplication.");
                    Integer j;
                    for (j = 1; j < DCSpecCount; j++) {
                        Integer k;
                        for (k = 0; k < j; k++) {
                            if (CimStringUtils.equals(fpcInfo.getDcSpecList().get(j).getDataItemName(),
                                    fpcInfo.getDcSpecList().get(k).getDataItemName())) {
                                log.info("duplicate DCItem detected. {}", i);
                                throw new ServiceException(retCodeConfig.getFpcInvalidDcdefChange());
                            }
                        }
                    }
                }
            }
        }

        //-------------------------------------------------------------------------
        // check for all FPCInfo
        //
        //  Following Items must have same value in all FPCInfo on the Operation.
        //   LotFamily
        //   mainPDID, operationNumber
        //   originalMainPDID, originalOperationNumber
        //   subMainPDID, subOperationNumber
        //   holdLotFlag
        //   sendEmailFlag
        //-------------------------------------------------------------------------
        ObjectIdentifier lotFamilyID = null;
        ObjectIdentifier mainPDID = null;
        ObjectIdentifier originalMainPDID = null;
        ObjectIdentifier subMainPDID = null;

        String operationNumber = null;
        String originalOperationNumber = null;
        String subOperationNumber = null;

        Boolean holdLotFlag = false;
        Boolean sendEmailFlag = false;

        if (FPCInfoCount > 0)   // > 1 is enough for check, but for retrieve value
        {
            Infos.FPCInfo fpcInfo0 = strFPCInfoList.get(0);
            lotFamilyID = fpcInfo0.getLotFamilyID();

            mainPDID = fpcInfo0.getMainProcessDefinitionID();
            originalMainPDID = fpcInfo0.getOriginalMainProcessDefinitionID();
            subMainPDID = fpcInfo0.getSubMainProcessDefinitionID();

            operationNumber = fpcInfo0.getOperationNumber();
            originalOperationNumber = fpcInfo0.getOriginalOperationNumber();
            subOperationNumber = fpcInfo0.getSubOperationNumber();

            holdLotFlag = fpcInfo0.isHoldLotFlag();
            sendEmailFlag = fpcInfo0.isSendEmailFlag();
        }

        for (int i = 1; i < FPCInfoCount; i++) {
            Infos.FPCInfo fpcInfo = strFPCInfoList.get(i);
            if (!ObjectIdentifier.equalsWithValue(lotFamilyID, fpcInfo.getLotFamilyID())) {
                log.info("Inconsistent lotFamilyID detected. {}", i);
                throw new ServiceException(retCodeConfig.getFpcNotUniquesetInOperation());
            }

            if (!ObjectIdentifier.equalsWithValue(mainPDID, fpcInfo.getMainProcessDefinitionID()) ||
                    !CimStringUtils.equals(operationNumber, fpcInfo.getOperationNumber()) ||
                    !ObjectIdentifier.equalsWithValue(originalMainPDID, fpcInfo.getOriginalMainProcessDefinitionID()) ||
                    !CimStringUtils.equals(originalOperationNumber, fpcInfo.getOriginalOperationNumber()) ||
                    !ObjectIdentifier.equalsWithValue(subMainPDID, fpcInfo.getSubMainProcessDefinitionID()) ||
                    !CimStringUtils.equals(subOperationNumber, fpcInfo.getSubOperationNumber())) {
                log.info("Inconsistent PD combination detected. {}", i);
                throw new ServiceException(retCodeConfig.getInvalidParameter());
            }

            if (holdLotFlag != fpcInfo.isHoldLotFlag()) {
                log.info("Inconsistent holdLotFlag detected. {}", i);
                throw new ServiceException(retCodeConfig.getFpcNotUniquesetInOperation());
            }

            if (sendEmailFlag != fpcInfo.isSendEmailFlag()) {
                log.info("Inconsistent sendEmailFlag detected. {}", i);
                throw new ServiceException(retCodeConfig.getFpcNotUniquesetInOperation());
            }
        }

        //------------------------------------------------------------------------------------------------
        // Check for Groups.
        //
        // These flags have same value in group.
        //   skipFlag
        //   restrictEqpFlag
        //
        // Equipments consistency
        //   Do not duplicate the equipment ID.
        //   If restrictEqpFlag is TRUE then the blank equipment ID is not valid.
        //
        // Wafer consistency
        //   In all FPCInfo in same group, the wafer list must be completely same except the list order.
        //   Wafer ID must not be duplicate in the list.
        //   Blank wafer ID is not allowed in list.
        //
        //
        // Check for Inter-Groups.
        //   A wafer must not belong to different groups.
        //------------------------------------------------------------------------------------------------
//        int readPos;
        List<String> groupEQPList = new ArrayList<>();
        Integer groupEQPCount = 0;

        List<ObjectIdentifier> waferIDList = new ArrayList<>();
        Integer waferListSizeLimit = FPCGroupNoLimit;
        List<Integer> waferGroupNoList = new ArrayList<>();
        Integer FPCWaferCount = 0;

        for (int i = 0; i < FPCInfoCount; i++) {
            int readPos = FPCGroupNumIndex.get(i);
            ObjectIdentifier tmpEQPID = strFPCInfoList.get(readPos).getEquipmentID();
            String tmpEQPIDVal = ObjectIdentifier.isEmpty(tmpEQPID) ? "" : tmpEQPID.getValue();
            if (FPCGroupNumChange.get(i)) {
                curGrpNo = FPCGroupNoList.get(readPos);

                skipFlagOfGroup.set(curGrpNo, strFPCInfoList.get(readPos).isSkipFalg());
                groupEQPRestricted.set(curGrpNo, strFPCInfoList.get(readPos).isRestrictEquipmentFlag());

                if (CimObjectUtils.isEmpty(groupEQPList)) {
                    groupEQPList.add(tmpEQPIDVal);
                } else {
                    groupEQPList.set(0, tmpEQPIDVal);
                }
                groupEQPCount = 1;

                if (groupEQPRestricted.get(curGrpNo) && ObjectIdentifier.isEmpty(tmpEQPID)) {
                    log.info("Inconsistent equipment info (restricted for voidEQP) detected. {} {}", readPos, curGrpNo);
                    throw new ServiceException(retCodeConfig.getFpcInvalidRestrictSetting());
                }

                groupWaferCount.set(curGrpNo, CimArrayUtils.getSize(strFPCInfoList.get(readPos).getLotWaferInfoList()));

            } else {
                if (skipFlagOfGroup.get(curGrpNo) != strFPCInfoList.get(readPos).isSkipFalg()) {
                    log.info("Inconsistent skipFlag detected. {} {}", readPos, curGrpNo);
                    throw new ServiceException(retCodeConfig.getFpcNotUniquesetInGroup());
                }

                if (groupEQPRestricted.get(curGrpNo)) {
                    log.info("Inconsistent restrictEqpFlag detected. {} {}", readPos, curGrpNo);
                    throw new ServiceException(retCodeConfig.getFpcInvalidRestrictSetting());
                } else if (strFPCInfoList.get(readPos).isRestrictEquipmentFlag()) {
                    log.info("Inconsistent restrictEqpFlag detected.", readPos, curGrpNo);
                    throw new ServiceException(retCodeConfig.getFpcInvalidRestrictSetting());
                }

                for (int j = 0; j < groupEQPCount; j++) {
                    if (ObjectIdentifier.equalsWithValue(groupEQPList.get(j), strFPCInfoList.get(readPos).getEquipmentID())) {
                        log.info("Inconsistent equipment info (eqpID duplicate in same group) detected. {} {}", readPos, curGrpNo);
                        throw new ServiceException(retCodeConfig.getFpcDuplicateEqpInGroup());
                    }
                }
                //groupEQPList.add(strFPCInfoList.get(readPos).getEquipmentID().getValue());
                groupEQPList.add(ObjectIdentifier.fetchValue(strFPCInfoList.get(readPos).getEquipmentID()));
                groupEQPCount++;

                if (groupWaferCount.get(curGrpNo) != CimArrayUtils.getSize(strFPCInfoList.get(readPos).getLotWaferInfoList())) {
                    log.info("Inconsistent wafer info (different wafer count in same group) detected.", readPos, curGrpNo);
                    throw new ServiceException(retCodeConfig.getFpcWaferMismatchInFpcGroup());
                }
            }

            for (int j = 1; j < groupWaferCount.get(curGrpNo); j++) {
                Integer k;
                for (k = 0; k < j; k++) {
                    if (ObjectIdentifier.equalsWithValue(strFPCInfoList.get(readPos).getLotWaferInfoList().get(j).getWaferID(),
                            strFPCInfoList.get(readPos).getLotWaferInfoList().get(k).getWaferID())) {
                        log.info("Inconsistent wafer info (duplicate wafer in same FPCInfo) detected.", readPos, curGrpNo);
                        throw new ServiceException(retCodeConfig.getFpcDuplicateWafer());
                    }
                }
            }

            for (int j = 0; j < groupWaferCount.get(curGrpNo); j++) {
                ObjectIdentifier tmpWaferID;
                tmpWaferID = strFPCInfoList.get(readPos).getLotWaferInfoList().get(j).getWaferID();
                if (ObjectIdentifier.isEmpty(tmpWaferID)) {
                    log.info("Inconsistent wafer info (void waferID) detected.", readPos, curGrpNo);
                    throw new ServiceException(retCodeConfig.getFpcWaferMismatchInFpcGroup());
                }

                int k;
                for (k = 0; k < FPCWaferCount; k++) {
                    if (ObjectIdentifier.equalsWithValue(waferIDList.get(k), tmpWaferID)) {
                        break;
                    }
                }
                if (k == FPCWaferCount) {
                    if (FPCGroupNumChange.get(i)) {
                        if (FPCWaferCount >= waferListSizeLimit) {
                            waferListSizeLimit += waferListSizeLimit;
                            log.info("WaferIDList size (accidental) extend for", waferListSizeLimit);
                        }
                        log.info("waferID/groupNo {} {}", tmpWaferID, curGrpNo);
                        waferIDList.add(tmpWaferID);
                        waferGroupNoList.add(curGrpNo);
                        FPCWaferCount++;
                    } else {
                        log.info("Inconsistent wafer info (different wafer in same group) detected. {} {}", readPos, curGrpNo);
                        throw new ServiceException(retCodeConfig.getFpcWaferMismatchInFpcGroup());
                    }
                } else {
                    if (curGrpNo != waferGroupNoList.get(k)) {
                        log.info("Inconsistent wafer info (same wafer in different group) detected.", readPos, waferGroupNoList.get(k), curGrpNo);
                        throw new ServiceException(retCodeConfig.getFpcWaferMismatchInFpcGroup());
                    }
                }
            }
        }
        groupEQPList.clear();    //not use below. because it is used for each groups.

        // Build checked FPCDispatchEqpInfo.
        Integer dispatchEQPCount = 0;
        Integer outWaferCount = 0;
        Integer grpCnt = 0;
        for (int i = 0; i < FPCInfoCount; i++) {
            Infos.FPCDispatchEqpInfo fpcDispatchEqpInfo = new Infos.FPCDispatchEqpInfo();
            fpcDispatchEqpInfos.add(fpcDispatchEqpInfo);
            int readPos = FPCGroupNumIndex.get(i);
            if (FPCGroupNumChange.get(i)) {
                if (i > 0)  // skip first.
                {
                    // commit dispatch Equipment length for previous group.
                    log.info("GroupNo/groupCount/EQPCount {} {} {}", curGrpNo, grpCnt, dispatchEQPCount);
                    // increment index and reset counter for new group.
                    grpCnt++;
                }
                dispatchEQPCount = 0;
                outWaferCount = 0;

                // build new structure for new group.
                curGrpNo = FPCGroupNoList.get(readPos);
                log.info("Build output structure for groupNo/groupCount", curGrpNo, grpCnt);
                fpcDispatchEqpInfo.setFPCGroupNo(curGrpNo);
                fpcDispatchEqpInfo.setSendEmailFlag(sendEmailFlag);
                fpcDispatchEqpInfo.setHoldLotFlag(holdLotFlag);
                fpcDispatchEqpInfo.setSkipFlag(skipFlagOfGroup.get(curGrpNo));

                if (groupEQPRestricted.get(curGrpNo)) {
                    log.info("EQP is restricted");
                    fpcDispatchEqpInfo.setRestrictEqpFlag(true);
                } else {
                    log.info("EQP is not restricted - MaxEQPCount {} {}", FPCGroupInfoCount.get(curGrpNo));
                    fpcDispatchEqpInfo.setRestrictEqpFlag(false);
                }

                Integer j = 0;
                List<ObjectIdentifier> waferIDs = new ArrayList<>();
                fpcDispatchEqpInfo.setWaferIDs(waferIDs);
                for (j = 0; j < FPCWaferCount; j++) {
                    if (waferGroupNoList.get(j) != curGrpNo) {
                        continue;
                    }

                    waferIDs.add(waferIDList.get(j));
                    outWaferCount++;
                }
                log.info("FPCItemNo/groupNo/groupCount/groupWafer {} {} {} {}", readPos, curGrpNo, grpCnt, outWaferCount);
                //it is not needed because outWaferCount must be equal to groupWaferCount[curGrpNo]
            }


            log.info("FPCItemNo/groupNo/groupCount/eqpCount {} {} {} {}", readPos, curGrpNo, grpCnt, dispatchEQPCount);
            ObjectIdentifier tmpEQPID;
            tmpEQPID = strFPCInfoList.get(readPos).getEquipmentID();
            List<ObjectIdentifier> dispatchEqpIDs = new ArrayList<>();
            fpcDispatchEqpInfo.setDispatchEqpIDs(dispatchEqpIDs);
            if (groupEQPRestricted.get(curGrpNo)) {
                log.info("restricted eqpID/FPCItemNo/groupNo/groupCount {} {} {} {}", tmpEQPID, readPos, curGrpNo, grpCnt);
                dispatchEqpIDs.add(tmpEQPID);
                dispatchEQPCount = 1;
            } else {
                //add equipment. (duplication check is already done.)
                //increment dispatchEQPCount
                if (!ObjectIdentifier.isEmpty(tmpEQPID))   //do not add void equipmentID to list
                {
                    log.info("non-restricted eqpID/FPCItemNo/groupNo/groupCount {} {} {} {}", tmpEQPID, readPos, curGrpNo, grpCnt);
                    dispatchEqpIDs.add(tmpEQPID);
                    dispatchEQPCount++;
                } else {
                    log.info("null equipment...FPCItemNo/groupNo/groupCount {} {} {}", readPos, curGrpNo, grpCnt);
                }
            }
            log.info("FPCItemNo/groupNo/groupCount/eqpCount {} {} {} {}", readPos, curGrpNo, grpCnt, dispatchEQPCount);
        }

        if (FPCInfoCount > 0) {
            log.info("eqpCount for last group", dispatchEQPCount);
            grpCnt++;
        }
        log.info("Group count for output structure is", grpCnt);

        // Return to Caller
        return fpcDispatchEqpInfos;
    }

    @Override
    public List<Infos.FPCProcessCondition> fpcProcessConditionCheck(Infos.ObjCommon objCommon, String FPCCategory, List<Infos.FPCProcessCondition> strFPCProcessConditionList, Boolean FPCCategoryCheckFlag, Boolean whiteDefCheckFlag) {
        List<Infos.FPCProcessCondition> fpcProcessConditions = new ArrayList<>();
        int pCLen = CimArrayUtils.getSize(strFPCProcessConditionList);
        //-----------------------------------------------------------------------
        // Get Identifier and FPC Categoris and WhiteDefFlag
        // Target : PD/Eqp/MachineRecipe/DCDef/DCSpec/Reticle/LogicalRecipe
        //-----------------------------------------------------------------------
        log.info("## Check Process Condition Status", pCLen);
        for (int pCCnt = 0; pCCnt < pCLen; pCCnt++)                                 //loop of process condition
        {
            Boolean aWhiteDefFlag;
            String tmpFPCCategory;
            ObjectIdentifier objectID;
            //------------------------------------
            // Process Definition
            //------------------------------------
            Infos.FPCProcessCondition fpcProcessCondition = strFPCProcessConditionList.get(pCCnt);
            ObjectIdentifier conditionObjectID = fpcProcessCondition.getObjectID();
            String conditionObjectType = fpcProcessCondition.getObjectType();
            if (CimStringUtils.equals(conditionObjectType, BizConstant.SP_FPC_PROCESSCONDITION_TYPE_PD)) {
                log.info("## Object Type is ProcessDefinition {}", conditionObjectID);
                CimProcessDefinition aPD = baseCoreFactory.getBO(CimProcessDefinition.class, conditionObjectID);
                Validations.check(null == aPD, retCodeConfig.getNotFoundProcessDefinition());

                objectID = new ObjectIdentifier(aPD.getIdentifier(), aPD.getPrimaryKey());
                aWhiteDefFlag = aPD.isWhiteFlagOn();
                tmpFPCCategory = aPD.getFPCCategory();

                Infos.FPCProcessCondition fpcProcessConditionObj = new Infos.FPCProcessCondition();
                fpcProcessConditions.add(fpcProcessConditionObj);
                fpcProcessConditionObj.setObjectID(objectID);
                fpcProcessConditionObj.setObjectType(BizConstant.SP_FPC_PROCESSCONDITION_TYPE_PD);
                fpcProcessConditionObj.setWhiteDefFlag(aWhiteDefFlag);
                if (!CimObjectUtils.isEmpty(tmpFPCCategory)) {
                    fpcProcessConditionObj.setFpcCategories(Collections.singletonList(tmpFPCCategory));
                }
            }
            //------------------------------------
            // Equipment
            //------------------------------------
            else if (CimStringUtils.equals(conditionObjectType, BizConstant.SP_FPC_PROCESSCONDITION_TYPE_EQUIPMENT)) {
                log.info("## Object Type is Equipment      {}  ", conditionObjectID);
                CimMachine aPosMachine = baseCoreFactory.getBO(CimMachine.class, conditionObjectID);
                Validations.check(null == aPosMachine, new OmCode(retCodeConfig.getNotFoundEqp(), conditionObjectID.getValue()));
                objectID = new ObjectIdentifier(aPosMachine.getIdentifier(), aPosMachine.getPrimaryKey());
                aWhiteDefFlag = aPosMachine.isWhiteFlagOn();
                List<String> checkEqpFPCCategoryListPtr = aPosMachine.getFPCCategories();
                Infos.FPCProcessCondition fpcProcessConditionObj = new Infos.FPCProcessCondition();
                fpcProcessConditions.add(fpcProcessConditionObj);
                fpcProcessConditionObj.setObjectID(objectID);
                fpcProcessConditionObj.setObjectType(BizConstant.SP_FPC_PROCESSCONDITION_TYPE_EQUIPMENT);
                fpcProcessConditionObj.setWhiteDefFlag(aWhiteDefFlag);
                if (!CimArrayUtils.isEmpty(checkEqpFPCCategoryListPtr)){
                    fpcProcessConditionObj.setFpcCategories(checkEqpFPCCategoryListPtr);
                }
            }
            //------------------------------------
            // Machine Recipe
            //------------------------------------
            else if (CimStringUtils.equals(conditionObjectType, BizConstant.SP_FPC_PROCESSCONDITION_TYPE_MACHINERECIPE)) {
                log.info("## Object Type is MachineRecipe   {} ", conditionObjectID);
                com.fa.cim.newcore.bo.recipe.CimMachineRecipe aPosMachineRecipe = baseCoreFactory.getBO(com.fa.cim.newcore.bo.recipe.CimMachineRecipe.class, conditionObjectID);
                Validations.check(null == aPosMachineRecipe, retCodeConfig.getNotFoundMachineRecipe());

                objectID = aPosMachineRecipe.getMachineRecipeID();
                aWhiteDefFlag = aPosMachineRecipe.isWhiteFlagOn();
                tmpFPCCategory = aPosMachineRecipe.getFPCCategory();

                Infos.FPCProcessCondition fpcProcessConditionObj = new Infos.FPCProcessCondition();
                fpcProcessConditions.add(fpcProcessConditionObj);
                fpcProcessConditionObj.setObjectID(objectID);
                fpcProcessConditionObj.setObjectType(BizConstant.SP_FPC_PROCESSCONDITION_TYPE_MACHINERECIPE);
                fpcProcessConditionObj.setWhiteDefFlag(aWhiteDefFlag);
                if (!CimStringUtils.isEmpty(tmpFPCCategory)){
                    fpcProcessConditionObj.setFpcCategories(Collections.singletonList(tmpFPCCategory));
                }
            }
            //------------------------------------
            // DC Definition
            //------------------------------------
            else if (CimStringUtils.equals(conditionObjectType, BizConstant.SP_FPC_PROCESSCONDITION_TYPE_DCDEF)) {
                log.info("## Object Type is DCDefinition   {}  ", conditionObjectID);

                com.fa.cim.newcore.bo.dc.CimDataCollectionDefinition aDCDef = baseCoreFactory.getBO(com.fa.cim.newcore.bo.dc.CimDataCollectionDefinition.class, conditionObjectID);
                Validations.check(null == aDCDef, retCodeConfig.getNotFoundDcdef());
                objectID = new ObjectIdentifier(aDCDef.getIdentifier(), aDCDef.getPrimaryKey());
                aWhiteDefFlag = aDCDef.isWhiteFlagOn();
                tmpFPCCategory = aDCDef.getFPCCategory();

                //--------------
                // Set
                //--------------
                Infos.FPCProcessCondition fpcProcessConditionObj = new Infos.FPCProcessCondition();
                fpcProcessConditions.add(fpcProcessConditionObj);
                fpcProcessConditionObj.setObjectID(objectID);
                fpcProcessConditionObj.setObjectType(BizConstant.SP_FPC_PROCESSCONDITION_TYPE_DCDEF);
                fpcProcessConditionObj.setWhiteDefFlag(aWhiteDefFlag);
                if (!CimStringUtils.isEmpty(tmpFPCCategory)){
                    fpcProcessConditionObj.setFpcCategories(Collections.singletonList(tmpFPCCategory));
                }
            }
            //------------------------------------
            // DC Specification
            //------------------------------------
            else if (CimStringUtils.equals(conditionObjectType, BizConstant.SP_FPC_PROCESSCONDITION_TYPE_DCSPEC)) {
                log.info("## Object Type is DCSpecification  {}", conditionObjectID);
                com.fa.cim.newcore.bo.dc.CimDataCollectionSpecification aDCSpec = baseCoreFactory.getBO(com.fa.cim.newcore.bo.dc.CimDataCollectionSpecification.class, conditionObjectID);
                Validations.check(null == aDCSpec, retCodeConfig.getNotFoundDcspec());
                objectID = new ObjectIdentifier(aDCSpec.getIdentifier(), aDCSpec.getPrimaryKey());
                aWhiteDefFlag = aDCSpec.isWhiteFlagOn();
                tmpFPCCategory = aDCSpec.getFPCCategory();

                //--------------
                // Set
                //--------------
                Infos.FPCProcessCondition fpcProcessConditionObj = new Infos.FPCProcessCondition();
                fpcProcessConditions.add(fpcProcessConditionObj);
                fpcProcessConditionObj.setObjectID(objectID);
                fpcProcessConditionObj.setObjectType(BizConstant.SP_FPC_PROCESSCONDITION_TYPE_DCSPEC);
                fpcProcessConditionObj.setWhiteDefFlag(aWhiteDefFlag);
                if (!CimStringUtils.isEmpty(tmpFPCCategory)){
                    fpcProcessConditionObj.setFpcCategories(Collections.singletonList(tmpFPCCategory));
                }

            }
            //------------------------------------
            // Reticle
            //------------------------------------
            else if (CimStringUtils.equals(conditionObjectType, BizConstant.SP_FPC_PROCESSCONDITION_TYPE_RETICLE)) {
                log.info("## Object Type is Reticle        {}  ", conditionObjectID);
                com.fa.cim.newcore.bo.durable.CimProcessDurable aReticle = baseCoreFactory.getBO(com.fa.cim.newcore.bo.durable.CimProcessDurable.class, conditionObjectID);
                Validations.check(null == aReticle, retCodeConfig.getNotFoundReticle());

                objectID = new ObjectIdentifier(aReticle.getIdentifier(), aReticle.getPrimaryKey());
                aWhiteDefFlag = aReticle.isWhiteFlagOn();
                tmpFPCCategory = aReticle.getFPCCategory();

                //--------------
                // Set
                //--------------
                Infos.FPCProcessCondition fpcProcessConditionObj = new Infos.FPCProcessCondition();
                fpcProcessConditions.add(fpcProcessConditionObj);
                fpcProcessConditionObj.setObjectID(objectID);
                fpcProcessConditionObj.setObjectType(BizConstant.SP_FPC_PROCESSCONDITION_TYPE_RETICLE);
                fpcProcessConditionObj.setWhiteDefFlag(aWhiteDefFlag);
                if (!CimStringUtils.isEmpty(tmpFPCCategory)){
                    fpcProcessConditionObj.setFpcCategories(Collections.singletonList(tmpFPCCategory));
                }
            }
            //------------------------------------
            // Logical Recipe
            //------------------------------------
            else if (CimStringUtils.equals(conditionObjectType, BizConstant.SP_FPC_PROCESSCONDITION_TYPE_LOGICALRECIPE)) {
                log.info("## Object Type is LogicalRecipe {}", conditionObjectID);

                CimLogicalRecipe aLogicalRecipe = baseCoreFactory.getBO(CimLogicalRecipe.class, conditionObjectID);
                Validations.check(null == aLogicalRecipe, retCodeConfig.getNotFoundLogicalRecipe());

                objectID = aLogicalRecipe.getLogicalRecipeID();
                aWhiteDefFlag = aLogicalRecipe.isWhiteFlagOn();
                tmpFPCCategory = aLogicalRecipe.getFPCCategory();

                //--------------
                // Set
                //--------------
                Infos.FPCProcessCondition fpcProcessConditionObj = new Infos.FPCProcessCondition();
                fpcProcessConditions.add(fpcProcessConditionObj);
                fpcProcessConditionObj.setObjectID(objectID);
                fpcProcessConditionObj.setObjectType(BizConstant.SP_FPC_PROCESSCONDITION_TYPE_LOGICALRECIPE);
                fpcProcessConditionObj.setWhiteDefFlag(aWhiteDefFlag);
                if (!CimStringUtils.isEmpty(tmpFPCCategory)){
                    fpcProcessConditionObj.setFpcCategories(Collections.singletonList(tmpFPCCategory));
                }
            } else {
                log.info("The specified ObjectType is NOT normal. {}", conditionObjectID);
                throw new ServiceException(retCodeConfig.getInvalidParameter());
            }
        }

        //--------------------------------------------------------------
        // ** Check consistency FPC Category of each Process Condition
        //--------------------------------------------------------------
        if (FPCCategoryCheckFlag) {
            log.info("## Check Process Condition of DOC. (FPCCategory)");
            this.fpcCategoryConditionCheck(objCommon, FPCCategory, fpcProcessConditions);

        }

        //--------------------------------------------------------------
        // ** Check white definition of each Process Condition
        //--------------------------------------------------------------
        if (whiteDefCheckFlag) {
            log.info("## Check Process Condition of DOC. (WhiteDefinition)");
            this.fpcWhiteProcessConditionCheck(objCommon, fpcProcessConditions);
        }
        return fpcProcessConditions;
    }

    @Override
    public void fpcCategoryConditionCheck(Infos.ObjCommon objCommon, String FPCCategory, List<Infos.FPCProcessCondition> strFPCProcessConditionList) {

        //---------------------------
        // In parameter check
        //---------------------------
        Integer FPCProcCondCount = CimArrayUtils.getSize(strFPCProcessConditionList);
        log.info("InParm : Process Condition Count {}", FPCProcCondCount);
        log.info("InParm : FPCCategory             {}", FPCCategory);

        if (0 == FPCProcCondCount) {
            return;
        }

        Integer i;
        Integer j;

        //---------------------------
        // Prepare for checking
        //---------------------------
        // Set representative FPCCategory.
        String specificCategory;
        specificCategory = "";
        Integer pCLen = CimArrayUtils.getSize(strFPCProcessConditionList);

        log.info("Check representative FPCCategory");
        if (CimObjectUtils.isEmpty(FPCCategory)) {
            for (i = 0; i < pCLen; i++) {
                Infos.FPCProcessCondition fpcProcessCondition = strFPCProcessConditionList.get(i);
                Integer FPCCateLen = CimArrayUtils.getSize(fpcProcessCondition.getFpcCategories());
                if (0 == FPCCateLen ||
                        CimStringUtils.equals(fpcProcessCondition.getObjectType(), BizConstant.SP_FPC_PROCESSCONDITION_TYPE_EQUIPMENT) ||
                        CimStringUtils.equals(fpcProcessCondition.getObjectType(), BizConstant.SP_FPC_PROCESSCONDITION_TYPE_LOGICALRECIPE) ||
                        CimStringUtils.equals(fpcProcessCondition.getObjectType(), BizConstant.SP_FPC_PROCESSCONDITION_TYPE_RETICLE)) {
                    continue;
                }

                // If the objectType is not equipment, object must have only one category. So Only refer to 0 point.
                if (!CimObjectUtils.isEmpty(fpcProcessCondition.getFpcCategories().get(0))) {
                    specificCategory = fpcProcessCondition.getFpcCategories().get(0);
                    log.info(" Found Category {}", specificCategory);
                    break;
                }
            }
        } else {
            specificCategory = FPCCategory;
        }
        log.info("##### Representative FPCCategory  {}", specificCategory);

        //-------------------------------
        // Check matching FPC Category
        //-------------------------------
        if (!CimObjectUtils.isEmpty(specificCategory)) {
            //--------------------
            // Check without EQP
            //--------------------
            log.info("## Check FPCCategory without EQP  ");
            for (i = 0; i < pCLen; i++) {
                Infos.FPCProcessCondition fpcProcessCondition = strFPCProcessConditionList.get(i);
                Integer FPCCateLen = CimArrayUtils.getSize(fpcProcessCondition.getFpcCategories());
                log.info("## Check : ObjectType/ObjectID/FPCCategoryLen {} {} {} ", fpcProcessCondition.getObjectType(), fpcProcessCondition.getObjectID(), FPCCateLen);
                if (0 == FPCCateLen ||
                        CimStringUtils.equals(fpcProcessCondition.getObjectType(), BizConstant.SP_FPC_PROCESSCONDITION_TYPE_EQUIPMENT) ||
                        CimStringUtils.equals(fpcProcessCondition.getObjectType(), BizConstant.SP_FPC_PROCESSCONDITION_TYPE_LOGICALRECIPE) ||
                        CimStringUtils.equals(fpcProcessCondition.getObjectType(), BizConstant.SP_FPC_PROCESSCONDITION_TYPE_RETICLE)) {
                    log.info("Omit Equipmemt , LogicalRecipe, Reticle or FPCCategory is NOT defined.. ");
                    continue;
                }

                if (CimObjectUtils.isEmpty(fpcProcessCondition.getFpcCategories().get(0))) {
                    log.info("FPCCategory is null string..");
                    continue;
                }

                log.info("## Check : FPCCategory  {} ", fpcProcessCondition.getFpcCategories().get(0));
                if (!CimStringUtils.equals(specificCategory, fpcProcessCondition.getFpcCategories().get(0))) {
                    log.info("Detect mismatch object {} {}", fpcProcessCondition.getObjectType(), fpcProcessCondition.getObjectID());
                    throw new ServiceException(retCodeConfig.getFpcCategoryMismatch());
                }
            }
            log.info("## Check FPCCategory without EQP ---  OK");

            //--------------------
            // Check  EQP
            //--------------------
            log.info("## Check FPCCategory EQP");
            for (i = 0; i < pCLen; i++) {
                Infos.FPCProcessCondition fpcProcessCondition = strFPCProcessConditionList.get(i);
                Integer FPCCateLen = CimArrayUtils.getSize(fpcProcessCondition.getFpcCategories());
                log.info("## Check : ObjectType/ObjectID/FPCCategoryLen ", fpcProcessCondition.getObjectType(), fpcProcessCondition.getObjectID(), FPCCateLen);
                if (0 == FPCCateLen ||
                        !CimStringUtils.equals(fpcProcessCondition.getObjectType(), BizConstant.SP_FPC_PROCESSCONDITION_TYPE_EQUIPMENT)) {
                    log.info("Omit Equipmemt or FPCCategory is NOT defined.");
                    continue;
                }

                log.info("## Check for EQP", fpcProcessCondition.getObjectID());
                Boolean matchFound = false;
                for (j = 0; j < FPCCateLen; j++) {
                    log.info("## Check : FPCCategories                      ", fpcProcessCondition.getFpcCategories().get(j));
                    if (CimStringUtils.equals(specificCategory, fpcProcessCondition.getFpcCategories().get(j))) {
                        matchFound = true;
                        break;
                    }
                }
                if (!matchFound) {
                    log.info("Detect mismatch for EQP {} {}", fpcProcessCondition.getObjectID(), specificCategory);
                    throw new ServiceException(retCodeConfig.getFpcCategoryMismatch());
                }
            }
            log.info("## Check FPCCategory  EQP ---  OK");
        }//specificCategory?
    }

    @Override
    public void fpcWhiteProcessConditionCheck(Infos.ObjCommon objCommon, List<Infos.FPCProcessCondition> strFPCProcessConditionList) {

        // In parameter check
        Integer FPCProcCondCount = CimArrayUtils.getSize(strFPCProcessConditionList);
        log.info("Check ProcessCondition count {}", FPCProcCondCount);
        if (CimObjectUtils.isEmpty(strFPCProcessConditionList)) {
            return;
        }

        // for error message creation
        Boolean aFlag;
        Integer i;
        StringBuilder errorMessages = new StringBuilder();         //For error message.
        for (i = 0; i < FPCProcCondCount; i++) {
            Infos.FPCProcessCondition fpcProcessCondition = strFPCProcessConditionList.get(i);
            String tmpObjType;
            ObjectIdentifier tmpObjID;
            tmpObjType = fpcProcessCondition.getObjectType();
            tmpObjID = fpcProcessCondition.getObjectID();
            aFlag = fpcProcessCondition.isWhiteDefFlag();
            log.info("Check : ObjectType {}, ObjectID {}, whiteDefFlag {} ", tmpObjType, tmpObjID, aFlag);
            //-------------------------------
            // Equipment
            //-------------------------------
            if (CimStringUtils.equals(tmpObjType, BizConstant.SP_FPC_PROCESSCONDITION_TYPE_EQUIPMENT)) {
                log.info("Object Type is Equipment");

                if (aFlag) {
                    log.info("White EQP found!!!!!   {}", tmpObjID);
                    errorMessages.append(tmpObjType).append(":").append(tmpObjID.getValue()).append("\n");
                } else {
                    continue;
                }
            }
            //-------------------------------
            // Machine Recipe
            //-------------------------------
            else if (CimStringUtils.equals(tmpObjType, BizConstant.SP_FPC_PROCESSCONDITION_TYPE_MACHINERECIPE)) {
                log.info("Object Type is MachineRecipe");
                if (aFlag) {
                    log.info("White MachineRecipe found!!!!!   {}", tmpObjID);
                    errorMessages.append(tmpObjType).append(":").append(tmpObjID.getValue()).append("\n");
                }
            }
            //-------------------------------
            // DC Def
            //-------------------------------
            else if (CimStringUtils.equals(tmpObjType, BizConstant.SP_FPC_PROCESSCONDITION_TYPE_DCDEF)) {
                log.info("Object Type is DCDef");
                if (aFlag) {
                    log.info("White DCDef found!!!!!  {} ", tmpObjID);
                    errorMessages.append(tmpObjType).append(":").append(tmpObjID.getValue()).append("\n");
                }
            }
            //-------------------------------
            // DC Spec
            //-------------------------------
            else if (CimStringUtils.equals(tmpObjType, BizConstant.SP_FPC_PROCESSCONDITION_TYPE_DCSPEC)) {
                log.info("Object Type is DCSpec");
                if (aFlag) {
                    log.info("White DCSpec found!!!!!   {}", tmpObjID);
                    errorMessages.append(tmpObjType).append(":").append(tmpObjID.getValue()).append("\n");
                }
            }
            //-------------------------------
            // PD
            //-------------------------------
            else if (CimStringUtils.equals(tmpObjType, BizConstant.SP_FPC_PROCESSCONDITION_TYPE_PD)) {
                log.info("Unsupported object (Process Definition) {}", tmpObjID);
            }
            //-------------------------------
            // Logical Recipe
            //-------------------------------
            else if (CimStringUtils.equals(tmpObjType, BizConstant.SP_FPC_PROCESSCONDITION_TYPE_LOGICALRECIPE)) {
                log.info("Unsupported object (Logical Recipe) {}", tmpObjID);
            }
            //-------------------------------
            // Reticle
            //-------------------------------
            else if (CimStringUtils.equals(tmpObjType, BizConstant.SP_FPC_PROCESSCONDITION_TYPE_RETICLE)) {
                log.info("Unsupported object (Reticle) {}", tmpObjID);
            } else {
                log.info("Invalid object type. {} {}", tmpObjType, tmpObjID);
                throw new ServiceException(retCodeConfig.getInvalidParameter());
            }
        }

        //---------------------------------
        // Ganarate error message.
        // Format: "ObjectType:ObjectID,,
        //---------------------------------
        if (!CimStringUtils.isEmpty(errorMessages.toString())) {
            log.info("White Flag found.{}", errorMessages.toString());
            throw new ServiceException(new OmCode(retCodeConfig.getFpcWhitedefinitionExistence(), errorMessages.toString()));
        }
    }

    @Override
    public List<Infos.FPCInfoAction> fpcInfoMerge(Infos.ObjCommon objCommon, List<Infos.FPCInfoAction> strFPCInfoActionList) {
        //===================================
        // Check count of Update/Create
        //===================================
        int createCnt = 0;
        int updateCnt = 0;
        int noChangeCnt = 0;
        int FPCInfoActionLen = CimArrayUtils.getSize(strFPCInfoActionList);
        String currentTime = CimDateUtils.getCurrentDateTimeWithDefault();
        int FPCGroupNo = -1;

        //Check of inpara length
        if (CimObjectUtils.isEmpty(strFPCInfoActionList)) {
            log.info("FPC info doesn't exit.");
            throw new ServiceException(retCodeConfig.getInvalidParameter());
        }

        for (Infos.FPCInfoAction fpcInfoAction : strFPCInfoActionList) {
            String actionType = fpcInfoAction.getActionType();
            if (CimStringUtils.equals(actionType, BizConstant.SP_FPCINFO_CREATE)) createCnt++;
            else if (CimStringUtils.equals(actionType, BizConstant.SP_FPCINFO_UPDATE)) updateCnt++;
            else if (CimStringUtils.equals(actionType, BizConstant.SP_FPCINFO_NOCHANGE)) noChangeCnt++;
        }

        //=========================================
        // Get defined FPC all information.
        //=========================================
        Infos.FPCInfo strFPCInfo0 = strFPCInfoActionList.get(0).getStrFPCInfo();
        log.info("#### Get defined FPC all information. ");
        log.info(" {} {} {} #### LotFamilyID, mainPDID, OperNo,                 ",strFPCInfo0.getLotFamilyID(),strFPCInfo0.getMainProcessDefinitionID(),strFPCInfo0.getOperationNumber());
        log.info(" {} {} {} {}#### OrgMainPDID, OrgOperNO, SubMainPDID, SubOperNo ",strFPCInfo0.getOriginalMainProcessDefinitionID(),strFPCInfo0.getOriginalOperationNumber(),strFPCInfo0.getSubMainProcessDefinitionID(),strFPCInfo0.getSubOperationNumber());

        Inputs.ObjFPCInfoGetDRIn objFPCInfoGetDRIn = new Inputs.ObjFPCInfoGetDRIn();
        objFPCInfoGetDRIn.setLotFamilyID(strFPCInfo0.getLotFamilyID());
        objFPCInfoGetDRIn.setMainPDID(strFPCInfo0.getMainProcessDefinitionID());
        objFPCInfoGetDRIn.setMainOperNo(strFPCInfo0.getOperationNumber());
        objFPCInfoGetDRIn.setOrgMainPDID(strFPCInfo0.getOriginalMainProcessDefinitionID());
        objFPCInfoGetDRIn.setOrgOperNo(strFPCInfo0.getOriginalOperationNumber());
        objFPCInfoGetDRIn.setSubMainPDID(strFPCInfo0.getSubMainProcessDefinitionID());
        objFPCInfoGetDRIn.setSubOperNo(strFPCInfo0.getSubOperationNumber());
        objFPCInfoGetDRIn.setDcSpecItemInfoGetFlag(true);
        objFPCInfoGetDRIn.setReticleInfoGetFlag(true);
        objFPCInfoGetDRIn.setRecipeParmInfoGetFlag(true);
        objFPCInfoGetDRIn.setWaferIDInfoGetFlag(true);
        List<Infos.FPCInfo> definedFPCs = this.fpcInfoGetDR(objCommon, objFPCInfoGetDRIn);

        //================================================
        // Compare the defined FPC with the update FPC.
        //================================================
        int definedFPCLen = CimArrayUtils.getSize(definedFPCs);
        log.info("### Compare the defined FPC with the update FPC. ");
        log.info(" {} ### Defined FPC                           ", definedFPCLen);
        log.info(" {} {} {} {}### All updateFPC(All/create/update/no change)", FPCInfoActionLen, createCnt, updateCnt, noChangeCnt);
        //---------------------------------------------------
        // Check count
        //---------------------------------------------------

        if (definedFPCLen != (updateCnt + noChangeCnt)) {
            log.info("FPC information count is not match.");
            throw new ServiceException(retCodeConfig.getFpcUpdatedByAnother());
        }

        //P9000061 add start
        int FPCGroupNumUseLimit = StandardProperties.OM_MAX_WAFER_COUNT_FOR_LOT.getIntValue();
        List<Boolean> FPCGroupNumUse = new ArrayList<>();
        for (int i = 0; i < FPCGroupNumUseLimit; i++) {
            FPCGroupNumUse.add(false);
        }

        //---------------------------------------------------
        //Check FPC_ID/UpdateTime.
        //---------------------------------------------------
        for (Infos.FPCInfoAction fpcInfoAction : strFPCInfoActionList) {
            Infos.FPCInfo strFPCInfo = fpcInfoAction.getStrFPCInfo();
            //---------------------------------------------------
            // Omit "Create" action record.
            //---------------------------------------------------
            String actionType = fpcInfoAction.getActionType();
            if (CimStringUtils.equals(actionType, BizConstant.SP_FPCINFO_CREATE)) {
                continue;
            }
            boolean cmpCheckFlag = false;
            for (Infos.FPCInfo definedFPC : definedFPCs)       //defined FPC
            {
                log.info(" {} {} ### Check FPCID existence. DefinedFPC vs UpdateFPC", strFPCInfo.getFpcID(), definedFPC.getFpcID());
                //---------------------------------------------------
                // FPCID of defined FPC info is blank ?
                //---------------------------------------------------
                if (CimObjectUtils.isEmpty(definedFPC.getFpcID())) {
                    log.info("Can NOT merge FPC info because defined FPCID or createTime or updateTime is blank.");
                    Validations.check(retCodeConfig.getInvalidParameter());
                }
                //---------------------------------------------------
                // Check FPC_ID existence.
                //---------------------------------------------------
                if (!CimStringUtils.equals(strFPCInfo.getFpcID(), definedFPC.getFpcID())) {
                    continue;
                } else {
                    log.info(" {} {} ### Defined FPC VS update FPC : Compare create time stamp", strFPCInfo.getCreateTime(), definedFPC.getCreateTime());
                    //---------------------------------------------------
                    //Compare both updateTime.
                    //---------------------------------------------------
                    if (!CimStringUtils.equals(strFPCInfo.getCreateTime(), definedFPC.getCreateTime())) {
                        log.info("Defined FPC VS update FPC : Create Time stamp is not same.");
                        Validations.check(retCodeConfig.getInvalidParameter());
                    }

                    log.info(" {} {} ### Defined FPC VS update FPC : Compare update time stamp", strFPCInfo.getUpdateTime(), definedFPC.getUpdateTime());
                    //---------------------------------------------------
                    //Compare both updateTime.
                    //---------------------------------------------------
                    if (!CimStringUtils.equals(strFPCInfo.getUpdateTime(), definedFPC.getUpdateTime())) {
                        log.info("Defined FPC VS update FPC : Update Time stamp is not same.");
                        Validations.check(retCodeConfig.getFpcUpdatedByAnother());
                    }
                    //OK
                    cmpCheckFlag = true;
                }
            }
            //---------------------------------------------------
            //FPC_ID existence ?
            //---------------------------------------------------
            if (!cmpCheckFlag) {
                log.info("Update/NoChange FPC information doesn't exist in the defined FPC information.");
                Validations.check(retCodeConfig.getFpcInfoNotFound(),"");
            }

            //Get used Group number.
            Integer fpcGroupNumber = strFPCInfo.getFpcGroupNumber();
            if (fpcGroupNumber > 0) {
                if (fpcGroupNumber >= FPCGroupNumUseLimit) {
                    log.info(" {} {} FPC GroupNo out of range", fpcGroupNumber, FPCGroupNumUseLimit);
                    throw new ServiceException(retCodeConfig.getFpcInvalidGroupNumber());
                } else {
                    FPCGroupNumUse.set(fpcGroupNumber, true);
                    log.info("{} ### used Group number", fpcGroupNumber);
                }
            }
        }

        //Get waferIDs' minimum Group number.
        for (int i = 1; i < FPCGroupNumUseLimit; i++) {
            if (!FPCGroupNumUse.get(i)) {
                FPCGroupNo = i;
                break;
            }
            //else do nothing
        }
        log.info("{} ### minimum Group number", FPCGroupNo);

        //=========================================
        // Merge defined FPC and update FPC.
        //=========================================
        log.info("### Merge defined FPC and update FPC. ");
        List<Infos.FPCInfoAction> strFPCInfoActionListMerged = strFPCInfoActionList;     //copy to Merged structure.
        for (int i = 0; i < FPCInfoActionLen; i++) {
            Infos.FPCInfoAction fpcInfoAction = strFPCInfoActionList.get(i);
            Infos.FPCInfo strFPCInfo = fpcInfoAction.getStrFPCInfo();
            String actionType = fpcInfoAction.getActionType();
            String psmKey = fpcInfoAction.getStrFPCInfo().getPsmKey();
            log.info("{} ### Action Type                       ", actionType);
            if (CimStringUtils.equals(actionType, BizConstant.SP_FPCINFO_NOCHANGE)) {
                //----------------------------------------------------
                // ReSet NoChange FPC information
                //----------------------------------------------------
                log.info("### Reset for NoChange Info ");
                for (Infos.FPCInfo definedFPC : definedFPCs) {
                    log.info("{} Check FPCID existence", strFPCInfo.getFpcID());
                    if (!CimStringUtils.equals(strFPCInfo.getFpcID(), definedFPC.getFpcID())) {
                        continue;
                    }

                    if (CimStringUtils.equals(actionType, BizConstant.SP_FPCINFO_NOCHANGE)) {
                        //reset
                        log.info("{} ### Reset for NoChange Info ", definedFPC.getFpcID());
                        strFPCInfoActionListMerged.get(i).setStrFPCInfo(definedFPC);
                        continue;
                    }
                }

                continue;
            }
            //---------------------------------------------------
            //Get Current Time stamp
            //---------------------------------------------------
            Infos.FPCInfo strFPCInfoMaerged = strFPCInfoActionListMerged.get(i).getStrFPCInfo();
            if (CimStringUtils.equals(actionType, BizConstant.SP_FPCINFO_CREATE)) {
                //-------------------------------------------
                //  Create FPC ID
                //   FPC_ID  :"LotFamilyID.TimeStamp(usec)"
                //-------------------------------------------
                log.info("### FPC Update Action is Create.");
                String newFPCID = fpcInfoAction.getStrFPCInfo().getLotFamilyID().getValue() + BizConstant.SP_KEY_SEPARATOR_DOT + CimDateUtils.getCurrentDateTimeByPattern("yyyy-MM-dd-HH.mm.ss.SSSSSS");

                //Set new FPCID
                strFPCInfoMaerged.setFpcID(newFPCID);
                strFPCInfoMaerged.setCreateTime(currentTime);
                log.info("{} ### Newly FPC ID    ", newFPCID);
                log.info("{} ### CreateTime      ", currentTime);
            }

            //----------------------------------
            // Set Update Time
            //  Create:  createTime, updateTime
            //  Update:  updateTime
            //----------------------------------
            strFPCInfoMaerged.setUpdateTime(currentTime);
            log.info("{} ### UpdateTime      ", currentTime);

            //---------------------------
            // Set Claim user
            //---------------------------
            strFPCInfoMaerged.setClaimUserID(objCommon.getUser().getUserID().getValue());
            log.info("{} ### ClaimUserID     ", objCommon.getUser().getUserID().getValue());

            //------------------------------------------------------------------
            // ReSet LotFamilyID, WaferID
            //------------------------------------------------------------------
            List<Infos.LotWaferInfo> lotWaferInfoList = strFPCInfoMaerged.getLotWaferInfoList();
            int waferLen = CimArrayUtils.getSize(lotWaferInfoList);
            ObjectIdentifier lotFamilyID = strFPCInfoMaerged.getLotFamilyID();
            log.info(" {} {} ### Reset FPCGroupNo/FPCType  ", strFPCInfoMaerged.getFpcGroupNumber(), strFPCInfoMaerged.getFpcType());
            log.info("{} ### waferLen", waferLen);
            List<Infos.WaferListInLotFamilyInfo> waferListInLotFamilyInfos = lotMethod.lotWafersStatusListGetDR(objCommon, lotFamilyID);

            //Existent LotFamilyID ?
            if (CimObjectUtils.isEmpty(waferListInLotFamilyInfos)) {
                log.info("{} The specified waferID is NOT exist in the specified LotFamily.", lotFamilyID);
                throw new ServiceException(retCodeConfig.getNotFoundLotFamily());
            }

            //--------------------
            //Reset LotFamilyID
            //--------------------
            com.fa.cim.newcore.bo.product.CimLotFamily lotFamily = baseCoreFactory.getBO(com.fa.cim.newcore.bo.product.CimLotFamily.class, lotFamilyID);
            if (null == lotFamily) {
                throw new ServiceException(retCodeConfig.getNotFoundLotFamily());
            }


            if (ObjectIdentifier.equalsWithValue(lotFamilyID, lotFamily.getIdentifier())) {
                strFPCInfoMaerged.setLotFamilyID(new ObjectIdentifier(lotFamily.getIdentifier(), lotFamily.getPrimaryKey()));
            }
            log.info(" {} {} ### Reset LotFamilyID, ObjRef", lotFamilyID.getValue(), lotFamily.getPrimaryKey());

            //--------------------
            //Reset Wafer ID
            //--------------------
            for (Infos.WaferListInLotFamilyInfo waferListInLotFamilyInfo : waferListInLotFamilyInfos) {
                for (Infos.LotWaferInfo lotWaferInfo : lotWaferInfoList) {
                    if (ObjectIdentifier.equalsWithValue(waferListInLotFamilyInfo.getWaferID(),
                            lotWaferInfo.getWaferID())) {
                        lotWaferInfo.setWaferID(waferListInLotFamilyInfo.getWaferID());
                        log.info(" {} ### Reset WaferID , ObjRef", waferListInLotFamilyInfo.getWaferID());
                    }
                }// Merged info
            }// Actual info

            //------------------------------------------------------------------
            // ReSet FPC Group No
            //  FPCType :
            //  ByLot   -> Set 0.
            //             Check whether all wafers are specified.
            //  ByWafer -> Set smallest waferID's suffix number among FPCGroup.
            //------------------------------------------------------------------
            //---------------------------------
            // FPC Type : ByLot
            //---------------------------------
            String fpcType = fpcInfoAction.getStrFPCInfo().getFpcType();
            if (CimStringUtils.equals(fpcType, BizConstant.SP_FPCTYPE_BYLOT)) {
                if (CimStringUtils.isEmpty(psmKey)) { //If psmKey is empty, it means runCard psm
                    log.info("Mismatch the specified FPCType and the number of wafers.");
                    Validations.check(CimArrayUtils.getSize(waferListInLotFamilyInfos) !=
                            CimArrayUtils.getSize(lotWaferInfoList), retCodeConfig.getFpcTypeMismatch());
                } else {
                    int rcPSMWaferLen = CimArrayUtils.getSize(runCardMethod.getRunCardPsmKeyInfoByPSMKey(psmKey)
                            .getWaferList());
                    Validations.check(rcPSMWaferLen != CimArrayUtils.getSize(lotWaferInfoList),
                            retCodeConfig.getFpcTypeMismatch());
                }
                log.info("## Reset FPCGroupNo to 0.");
                strFPCInfoMaerged.setFpcGroupNumber(0);
            }
            //---------------------------------
            // FPC Type : ByWafer
            //---------------------------------
            else if (CimStringUtils.equals(fpcType, BizConstant.SP_FPCTYPE_BYWAFER)) {
                if (CimArrayUtils.getSize(waferListInLotFamilyInfos) == CimArrayUtils.getSize(lotWaferInfoList)) {
                    log.info("Mismatch the specified FPCType and the number of wafers.");
                    throw new ServiceException(retCodeConfig.getFpcTypeMismatch());
                }

                if (CimStringUtils.equals(actionType, BizConstant.SP_FPCINFO_CREATE) || fpcInfoAction.getStrFPCInfo().getFpcGroupNumber() == 0) {
                    strFPCInfoMaerged.setFpcGroupNumber(FPCGroupNo);
                }
            }
            log.info("{} ### FPCGroupNo", FPCGroupNo);
        }//loop of FPC all info
        return strFPCInfoActionListMerged;
    }

    @Override
    public List<Infos.FPCInfoAction> fpcInfoConsistencyCheckForUpdate(Infos.ObjCommon objCommon, List<Infos.FPCInfoAction> strFPCInfoActionList) {

        List<Infos.FPCInfoAction> strFPCInfoActionListAll = strFPCInfoActionList;

        if (CimObjectUtils.isEmpty(strFPCInfoActionList)) {
            log.info("FPC Action Info doesn't exist.");
            throw new ServiceException(retCodeConfig.getInvalidParameter());
        }

        //-----------------------
        // Preset out structure
        //-----------------------

        //===================================================================================
        // **** Check consistency between FPC information.
        //===================================================================================
        List<Infos.FPCInfo> strFPCInfoListforCheck = new ArrayList<>();
        for (Infos.FPCInfoAction fpcInfoAction : strFPCInfoActionList) {
            strFPCInfoListforCheck.add(fpcInfoAction.getStrFPCInfo());
        }

        //-----------------------------------------------------------
        // FPC_infoConsistency_Check()
        // Output:
        // pptFPCDispatchEqpInfoSequence strFPCDispatchEqpInfoList
        //    long            FPCGroupNo
        //    boolean         skipFlag
        //    boolean         restrictEqpFlag
        //    boolean         sendEmailFlag
        //    boolean         holdLotFlag
        //    boolean         splitFlag
        //    ObjectIdentifierSequence dispatchEqpIDs
        //    ObjectIdentifierSequence waferIDSequence
        //-----------------------------------------------------------
        log.info("## Check consistency between FPC information.(FPC_infoConsistency_Check()");
        List<Infos.FPCDispatchEqpInfo> fpcDispatchEqpInfos = this.fpcInfoConsistencyCheck(objCommon, strFPCInfoListforCheck, true, true);

        //===================================================================================
        // **** Check consistency between FPC definition and SM definition.
        //===================================================================================
        log.info("## Check consistency between FPC definition and SM definition. ");
        //-------------------------------------------------------------
        // *** Target : LotFamily, Lot, Wafer
        //-------------------------------------------------------------
        //---------------------------------------------
        // Get lotID and waferID from lotFamilyID.
        //---------------------------------------------
        Infos.FPCInfo strFPCInfo0 = strFPCInfoActionListAll.get(0).getStrFPCInfo();
        List<Infos.WaferListInLotFamilyInfo> waferListInLotFamilyInfos = lotMethod.lotWafersStatusListGetDR(objCommon, strFPCInfo0.getLotFamilyID());

        //-----------------------------------------------
        // Gather Lot ID from LotFamily
        //-----------------------------------------------
        List<ObjectIdentifier> workLotIDs = new ArrayList<>();
        int actualLotLen = CimArrayUtils.getSize(waferListInLotFamilyInfos);   //actual wafers

        log.info("{} ## Actual Lot(Wafer)Len ", actualLotLen);
        if (CimObjectUtils.isEmpty(waferListInLotFamilyInfos)) {
            log.info("{} The specified waferID is NOT exist in the specified LotFamily.", strFPCInfo0.getLotFamilyID());
            throw new ServiceException(retCodeConfig.getNotFoundLotFamily());
        }

        for (Infos.WaferListInLotFamilyInfo waferListInLotFamilyInfo : waferListInLotFamilyInfos) {
            boolean lotIDFlag = false;
            for (ObjectIdentifier workLotID : workLotIDs) {
                if (ObjectIdentifier.equalsWithValue(waferListInLotFamilyInfo.getLotID(), workLotID)) {
                    //Same lot.
                    lotIDFlag = true;
                    break;
                }
            }//loop of gathering lots
            if (!lotIDFlag) {
                //Set LotID into work structure.
                log.info("{} lotID", waferListInLotFamilyInfo.getLotID());
                workLotIDs.add(waferListInLotFamilyInfo.getLotID());
            }
        }//loop of actual Lots

        //-----------------------------------------------------------------------------
        // Check whether all wafers are setting in case sof FPCType ByLot or not.
        //-----------------------------------------------------------------------------
        int docWaferLen = CimArrayUtils.getSize(strFPCInfo0.getLotWaferInfoList());
        log.info("{} {} {} ## Check whether all wafers are setting in case of FPCType ByLot or not. WaferLen, " +
                "actualWaferLen, FPCType",actualLotLen, docWaferLen, strFPCInfo0.getFpcType());
        String psmKey = strFPCInfo0.getPsmKey();
        if (CimStringUtils.isEmpty(psmKey)) { //If psmKey is empty, it means runCard psm
            if ((actualLotLen == docWaferLen &&
                CimStringUtils.equals(strFPCInfo0.getFpcType(), BizConstant.SP_FPCTYPE_BYLOT)) ||
                (actualLotLen != docWaferLen &&
                CimStringUtils.equals(strFPCInfo0.getFpcType(), BizConstant.SP_FPCTYPE_BYWAFER))) {
                //OK
            } else {
                log.info("Mismatch the specified FPCType and the number of wafers.");
                Validations.check(retCodeConfig.getFpcTypeMismatch());
            }
        } else {
            int rcPSMWaferLen = CimArrayUtils.getSize(runCardMethod.getRunCardPsmKeyInfoByPSMKey(psmKey).getWaferList());
            if ((rcPSMWaferLen == docWaferLen &&
                CimStringUtils.equals(strFPCInfo0.getFpcType(), BizConstant.SP_FPCTYPE_BYLOT)) ||
                (rcPSMWaferLen != docWaferLen &&
                CimStringUtils.equals(strFPCInfo0.getFpcType(), BizConstant.SP_FPCTYPE_BYWAFER))) {
                //OK
            } else {
                log.info("Mismatch the specified FPCType and the number of wafers.");
                Validations.check(retCodeConfig.getFpcTypeMismatch());
            }
        }


        //-----------------------------------------------------------------------------
        // Check whether the specified waferIDs are included the lotFamily or not.
        //-----------------------------------------------------------------------------
        log.info("## Check whether the specified waferIDs are included the lotFamily or not. ");
        for (Infos.FPCDispatchEqpInfo fpcDispatchEqpInfo : fpcDispatchEqpInfos) {
            List<ObjectIdentifier> waferIDs = fpcDispatchEqpInfo.getWaferIDs();
            if (CimArrayUtils.isNotEmpty(waferIDs)) {
                for (ObjectIdentifier waferID : waferIDs) {
                    boolean waferExistFlag = false;
                    for (Infos.WaferListInLotFamilyInfo waferListInLotFamilyInfo : waferListInLotFamilyInfos) {
                        if (ObjectIdentifier.equalsWithValue(waferID,
                                waferListInLotFamilyInfo.getWaferID())) {
                            log.info("{} {} Some wafers of FPC info VS All wafers of Lot ",waferIDs,waferListInLotFamilyInfo.getWaferID());
                            waferExistFlag = true;
                            break;
                        }
                    }//actualwafer vs updatewafer
                    if (!waferExistFlag) {
                        log.info("{} {} The specified waferID is NOT exist in the specified LotFamily. LotFamilyID/WaferID ",strFPCInfo0.getLotFamilyID(),waferID);
                        throw new ServiceException(retCodeConfig.getInvalidInputWafer());
                    }
                }// loop of wafer
            }
        }//loop of FPCGroup


        //-------------------------------------------------------------------------
        //
        // *** Check existence of the specified Route / Operation.
        //
        //    - Current main route of lot ?
        //    - Route / Operation exist ?
        // Can create/update about the lot's current Main Route or connected it.
        //-------------------------------------------------------------------------
        log.info("## Check existence of the specified Route / Operation.  ");

        List<ObjectIdentifier> workMainPDIDs = new ArrayList<>();
        for (ObjectIdentifier workLotID : workLotIDs) {
            //----------------------------------------------
            // Get mainPDID of each Lot
            //----------------------------------------------
            ObjectIdentifier lotMainRouteIDGet = lotMethod.lotMainRouteIDGet(objCommon, workLotID);

            //Set mainPDID in to work structure.
            log.info("{} {} ## Lot's mainPDID", workLotID, lotMainRouteIDGet);
            workMainPDIDs.add(lotMainRouteIDGet);
        }// Lot loop

        //--------------------------------------------------------------------------------------------
        //
        // ** Check whether the specified Route is the lot's current Main Route or not.
        //
        //    Target : Main Route
        //--------------------------------------------------------------------------------------------
        ObjectIdentifier MAINROUTE = null;
        ObjectIdentifier SUBROUTE = null;
        ObjectIdentifier SUB2ROUTE = null;
        for (ObjectIdentifier workMainPDID : workMainPDIDs)      //Main route of FPC info is set "mainPDID" or "originalMainPDID" of FPC definition.
        {
            if (ObjectIdentifier.equalsWithValue(workMainPDID, strFPCInfo0.getMainProcessDefinitionID()) ||
                    ObjectIdentifier.equalsWithValue(workMainPDID, strFPCInfo0.getOriginalMainProcessDefinitionID())) {
                log.info("{} MainRoute defined FPC information is ", workMainPDID);
                MAINROUTE = workMainPDID;
            }
        }

        //Current  ?
        if (ObjectIdentifier.isEmpty(MAINROUTE)) {
            log.info("MainRoute defined FPC information is not found. ");
            throw new ServiceException(retCodeConfig.getFpcRouteInfoError());
        }

        //--------------------------------------------------------------------------------------------
        //
        // ** Check whether the specified Route/Operation is in the specified Route or not.
        //
        //    Target : Route/Operation
        //--------------------------------------------------------------------------------------------
        log.info("{} ## Check whether the specified Route/Operation is in the specified Route or not.", MAINROUTE);
        String fpcDefRoute = this.fpcRouteOperationConsistencyCheck(objCommon, strFPCInfo0, MAINROUTE);

        //-------------------------------------------------------------------------
        // ** Check defined point FPC info
        //-------------------------------------------------------------------------
        log.info("{} ## Defined point of FPC Info   ", fpcDefRoute);
        if (CimStringUtils.equals(fpcDefRoute, BizConstant.SP_FPC_DEFINITION_ROUTE_MAIN)) {
        }
        if (CimStringUtils.equals(fpcDefRoute, BizConstant.SP_FPC_DEFINITION_ROUTE_SUB)) {
            //SubRoute
            CimProcessDefinition aPD = baseCoreFactory.getBO(CimProcessDefinition.class, strFPCInfo0.getMainProcessDefinitionID());
            if (null == aPD) {
                throw new ServiceException(retCodeConfig.getNotFoundProcessDefinition());
            }
            SUBROUTE = new ObjectIdentifier(aPD.getIdentifier(), aPD.getPrimaryKey());
        }
        if (CimStringUtils.equals(fpcDefRoute, BizConstant.SP_FPC_DEFINITION_ROUTE_SUB2)) {
            //SubRoute
            CimProcessDefinition aPD = baseCoreFactory.getBO(CimProcessDefinition.class, strFPCInfo0.getSubMainProcessDefinitionID());
            if (null == aPD) {
                throw new ServiceException(retCodeConfig.getNotFoundProcessDefinition());
            }
            SUBROUTE = new ObjectIdentifier(aPD.getIdentifier(), aPD.getPrimaryKey());

            CimProcessDefinition aPD2 = baseCoreFactory.getBO(CimProcessDefinition.class, strFPCInfo0.getMainProcessDefinitionID());
            if (null == aPD2) {
                throw new ServiceException(retCodeConfig.getNotFoundProcessDefinition());
            }
            SUB2ROUTE = new ObjectIdentifier(aPD2.getIdentifier(), aPD2.getPrimaryKey());
        }
        log.info("{} ## MainRoute   ", MAINROUTE);
        log.info("{} ## SubRoute    ", SUBROUTE);
        log.info("{} ## Sub2Route   ", SUB2ROUTE);

        //--------------------------------------------------------------------------------------------
        //
        // ** Check Process Definition
        //
        //    Target : MandatoryOperation, PD_Type, FPCCategory
        //--------------------------------------------------------------------------------------------
        log.info("## Check Process Definition. ");
        Inputs.ObjProcessOperationListInRouteForFpcGetDRIn in = new Inputs.ObjProcessOperationListInRouteForFpcGetDRIn();
        in.setRouteID(strFPCInfo0.getMainProcessDefinitionID());
        in.setOperationNumber(strFPCInfo0.getOperationNumber());
        in.setSubRouteID(strFPCInfo0.getSubMainProcessDefinitionID());
        in.setSubOperationNumber(strFPCInfo0.getSubOperationNumber());
        in.setOrgRouteID(strFPCInfo0.getOriginalMainProcessDefinitionID());
        in.setOrgOperationNumber(strFPCInfo0.getOperationNumber());
        in.setLotFamilyID(strFPCInfo0.getLotFamilyID());
        in.setFPCCountGetFlag(Boolean.TRUE);
        List<Infos.ConnectedSubRouteOperationInfo> operationInfoList = processMethod.processOperationListInRouteForFPCGetDR(objCommon, in);

        if (CimObjectUtils.isEmpty(operationInfoList)) {
            log.info("The specified FPC information is not found. ");
            throw new ServiceException(retCodeConfig.getFpcRouteInfoError());
        }

        Infos.ConnectedSubRouteOperationInfo operationInfo = operationInfoList.get(0);
        log.info("The specified PD Information ");
        log.info("{} operationID     :", operationInfo.getOperationID());
        log.info("{} operationNumber :", operationInfo.getOperationNumber());
        log.info("{} mandatoryFlag   :", (operationInfo.getMandatoryFlag() ? "TRUE" : "FALSE"));
        log.info("{} whiteDefFlag    :", (operationInfo.getWhiteDefFlag() ? "TRUE" : "FALSE"));
        log.info("{} FPC Category    :", operationInfo.getFPCCategory());
        log.info("{} FPC Info Count  :", operationInfo.getFPCInfoCount());

        //Skip Flag should be False if MandatoryOperation is True.
        log.info("## Skip Flag should be False if MandatoryOperation is True. ");
        if (operationInfo.getMandatoryFlag()) {
            for (Infos.FPCInfoAction fpcInfoAction : strFPCInfoActionListAll)            //input FPCInfoSeq
            {
                Infos.FPCInfo strFPCInfo = fpcInfoAction.getStrFPCInfo();
                log.info("{} {} ## (FPCGroupNo)SkipFlag   ", strFPCInfo.getFpcGroupNumber(), strFPCInfo.isSkipFalg() ? "TRUE" : "FALSE");
                if (strFPCInfo.isSkipFalg()) {
                    log.info("{} Mandatory Operation cannot be skipped. (FPCGroup:SkipFlag : MandatoryFlag)",strFPCInfo.getFpcGroupNumber());
                    throw new ServiceException(retCodeConfig.getFpcCannotSkipOperation());
                }
            }//loop of allFPCinfo
        }//check mandatoryflag


        //--------------------------------------------------------------------------------------------
        //
        // ** Check Process Condition of FPC
        //
        //    Target : each Process Condition item
        //--------------------------------------------------------------------------------------------
        log.info("## Check Process Condition of FPC. ");
        for (Infos.FPCInfoAction fpcInfoAction : strFPCInfoActionListAll)         //all FPCInfo
        {
            Infos.FPCInfo strFPCInfo = fpcInfoAction.getStrFPCInfo();
            log.info("{} {} ## Check FPC ID and Action Type", strFPCInfo.getFpcID(), fpcInfoAction.getActionType());
            List<Infos.FPCProcessCondition> strFPCProcessConditionList = new ArrayList<>();
            //PD
            if (!ObjectIdentifier.isEmpty(strFPCInfo.getProcessDefinitionID())) {
                log.info("{} ## Check FPC Category (PD    ) ", strFPCInfo.getProcessDefinitionID());
                Infos.FPCProcessCondition fpcProcessCondition = new Infos.FPCProcessCondition();
                fpcProcessCondition.setObjectID(strFPCInfo.getProcessDefinitionID());
                fpcProcessCondition.setObjectType(BizConstant.SP_FPC_PROCESSCONDITION_TYPE_PD);
                strFPCProcessConditionList.add(fpcProcessCondition);
            }
            //Equipment
            if (!ObjectIdentifier.isEmpty(strFPCInfo.getEquipmentID())) {
                log.info("{} ## Check FPC Category (EQP   ) ", strFPCInfo.getEquipmentID());
                Infos.FPCProcessCondition fpcProcessCondition = new Infos.FPCProcessCondition();
                fpcProcessCondition.setObjectID(strFPCInfo.getEquipmentID());
                fpcProcessCondition.setObjectType(BizConstant.SP_FPC_PROCESSCONDITION_TYPE_EQUIPMENT);
                strFPCProcessConditionList.add(fpcProcessCondition);
            }
            //Machine Recipe
            if (!ObjectIdentifier.isEmpty(strFPCInfo.getMachineRecipeID())) {
                log.info("{} ## Check FPC Category (MRCP  ) ", strFPCInfo.getMachineRecipeID());
                Infos.FPCProcessCondition fpcProcessCondition = new Infos.FPCProcessCondition();
                fpcProcessCondition.setObjectID(strFPCInfo.getMachineRecipeID());
                fpcProcessCondition.setObjectType(BizConstant.SP_FPC_PROCESSCONDITION_TYPE_MACHINERECIPE);
                strFPCProcessConditionList.add(fpcProcessCondition);
            }
            //DC Def
            if (!ObjectIdentifier.isEmpty(strFPCInfo.getDcDefineID())) {
                log.info("{} ## Check FPC Category (DCDEF ) ", strFPCInfo.getDcDefineID());
                Infos.FPCProcessCondition fpcProcessCondition = new Infos.FPCProcessCondition();
                fpcProcessCondition.setObjectID(strFPCInfo.getDcDefineID());
                fpcProcessCondition.setObjectType(BizConstant.SP_FPC_PROCESSCONDITION_TYPE_DCDEF);
                strFPCProcessConditionList.add(fpcProcessCondition);
            }
            //DC Spec
            if (!ObjectIdentifier.isEmpty(strFPCInfo.getDcSpecID())) {
                log.info("{} ## Check FPC Category (DCSPEC) ", strFPCInfo.getDcSpecID());
                Infos.FPCProcessCondition fpcProcessCondition = new Infos.FPCProcessCondition();
                fpcProcessCondition.setObjectID(strFPCInfo.getDcSpecID());
                fpcProcessCondition.setObjectType(BizConstant.SP_FPC_PROCESSCONDITION_TYPE_DCSPEC);
                strFPCProcessConditionList.add(fpcProcessCondition);
            }
            //Reticle
            List<Infos.ReticleInfo> reticleInfoList = strFPCInfo.getReticleInfoList();
            if (!CimObjectUtils.isEmpty(reticleInfoList)) {
                for (Infos.ReticleInfo reticleInfo : reticleInfoList) {
                    if (!ObjectIdentifier.isEmpty(reticleInfo.getReticleID())) {
                        if (ObjectIdentifier.isEmpty(reticleInfo.getReticleGroup())) {
                            log.info("Reticle Group is NOT specified despite being ReticleList length");
                            throw new ServiceException(retCodeConfig.getNotFoundReticleGrp());
                        }
                        log.info("{} ## Check FPC Category (RETICL) ", reticleInfo.getReticleID());
                        Infos.FPCProcessCondition fpcProcessCondition = new Infos.FPCProcessCondition();
                        fpcProcessCondition.setObjectID(reticleInfo.getReticleID());
                        fpcProcessCondition.setObjectType(BizConstant.SP_FPC_PROCESSCONDITION_TYPE_RETICLE);
                        strFPCProcessConditionList.add(fpcProcessCondition);
                    } else {
                        log.info("Reticle is NOT specified despite being ReticleList length");
                        throw new ServiceException(retCodeConfig.getNotFoundReticle());
                    }
                }
                //LogicalRecipe
            }

            //--------------------------------------------------------------
            // ** Check existence of each Process Condition item.
            // ** Check consistency FPC Category of each Process Condition
            // ** Check white definition of each Process Condition
            //--------------------------------------------------------------
            List<Infos.FPCProcessCondition> fpcProcessConditions = this.fpcProcessConditionCheck(objCommon, "", strFPCProcessConditionList, true, true);

            if (!CimObjectUtils.isEmpty(reticleInfoList)) {
                //Check ReticleGroup
                for (Infos.ReticleInfo reticleInfo : reticleInfoList) {
                    log.info("{} {} ## Check Reticle Group relation and existence.", reticleInfo.getReticleID(), reticleInfo.getReticleGroup());
                    com.fa.cim.newcore.bo.durable.CimProcessDurable aReticle = baseCoreFactory.getBO(com.fa.cim.newcore.bo.durable.CimProcessDurable.class, reticleInfo.getReticleID());
                    if (null == aReticle) {
                        throw new ServiceException(retCodeConfig.getNotFoundReticle());
                    }
                    List<CimProcessDurableCapability> seq = aReticle.allAssignedProcessDurableCapabilities();
                    ObjectIdentifier tmpReticleGroup;
                    if (CimArrayUtils.isEmpty(seq)) {
                        log.info("Reticle Group does NOT exist.");
                        throw new ServiceException(new OmCode(retCodeConfig.getNotFoundReticleGrp(), reticleInfo.getReticleID().getValue()));
                    } else {
                        tmpReticleGroup = new ObjectIdentifier(seq.get(0).getIdentifier(), seq.get(0).getPrimaryKey());
                        if (!ObjectIdentifier.equalsWithValue(tmpReticleGroup, reticleInfo.getReticleGroup())) {
                            log.info("Reticle Group does NOT exist.");
                            throw new ServiceException(new OmCode(retCodeConfig.getNotFoundReticleGrp(), reticleInfo.getReticleID().getValue()));
                        } else {
                            reticleInfo.setReticleGroup(tmpReticleGroup);
                        }
                    }
                }// Check ReticleGroup
            }

            //----------------------------------------------------------------------------------
            // ** Check about whether machine recipe is specified in case of setting equipment.
            //----------------------------------------------------------------------------------
            if (!ObjectIdentifier.isEmpty(strFPCInfo.getEquipmentID())) {
                if (ObjectIdentifier.isEmpty(strFPCInfo.getMachineRecipeID())) {
                    //error
                    log.info("Machine recipe is NOT specified in spite of setting equipment.");
                    throw new ServiceException(retCodeConfig.getFpcMrecipeSetError());
                }
            }

            //--------------------------------------------------------------------------------
            // ** Check about whether recipe Parameter is as the specified equipment or not.
            //--------------------------------------------------------------------------------
            List<Infos.RecipeParameterInfo> recipeParameterInfoList0 = strFPCInfo.getLotWaferInfoList().get(0).getRecipeParameterInfoList();
            log.info("## Check Process Condition of FPC. (RecipeParm)");
            int recipeParmLen = CimArrayUtils.getSize(recipeParameterInfoList0);
            if (0 < recipeParmLen && !CimStringUtils.isEmpty(strFPCInfo.getRecipeParameterChangeType())) {
                List<String> recipeParmNames = new ArrayList<>();
                recipeParameterInfoList0.forEach(para -> recipeParmNames.add(para.getParameterName()));
                this.fpcRecipeParameterConsistencyCheck(objCommon, strFPCInfo.getEquipmentID(), recipeParmNames);
            }//recipeParm

            //------------------------------------------------------------------------------------
            // ** Check about whether the dcSpec ID has the relation with the dcDef ID or not.
            //------------------------------------------------------------------------------------
            log.info("{} {} ## Check Process Condition of FPC. (DCSpec)", strFPCInfo.getDcDefineID(), strFPCInfo.getDcSpecID());
            if (!ObjectIdentifier.isEmptyWithValue(strFPCInfo.getDcDefineID()) && !ObjectIdentifier.isEmptyWithValue(strFPCInfo.getDcSpecID())) {
                List<Infos.DataCollection> dataCollections = dataCollectionMethod.dcSpecListGetDR(objCommon, strFPCInfo.getDcDefineID(), strFPCInfo.getDcSpecID(), BizConstant.SP_WHITEDEF_SEARCHCRITERIA_NONWHITE, 9999l, "");
                if (CimArrayUtils.isEmpty(dataCollections)) {
                    log.info("{} {} The specified DCSpecID does NOT have the relation with the DCDefID.", strFPCInfo.getDcDefineID(), strFPCInfo.getDcSpecID());
                    throw new ServiceException(new OmCode(new OmCode(retCodeConfig.getFpcDcdefDcspecMismatch()),
                            ObjectIdentifier.fetchValue(strFPCInfo.getDcDefineID()), ObjectIdentifier.fetchValue(strFPCInfo.getDcSpecID()), ""));
                }
            }

            //--------------------------------------------------------------------------
            // ** Check about whether dcDef is Delta DC Def or not.
            //--------------------------------------------------------------------------
            log.info("## Check Process Condition of FPC. (DCDef is Delta?)");
            if (!ObjectIdentifier.isEmptyWithValue(strFPCInfo.getDcDefineID())) {
                Results.EDCPlanInfoInqResult edcPlanInfoInqResult = dataCollectionMethod.dcDefDetailInfoGetDR(objCommon, strFPCInfo.getDcDefineID());
                List<Infos.DCItem> strDCItemList = edcPlanInfoInqResult.getStrDCItemList();
                for (Infos.DCItem dcItem : strDCItemList) {
                    if (CimStringUtils.equals(dcItem.getCalculationType(), BizConstant.SP_DCDEF_CALC_DELTA)) {
                        log.info("The specified DCDefID is Delta DC Def ID.");
                        throw new ServiceException(new OmCode(retCodeConfig.getFpcInvalidDcinfo(), ObjectIdentifier.fetchValue(strFPCInfo.getDcDefineID())));
                    }
                }
            }

            //--------------------------------------------------------------------------
            // ** Check about whether dc spec items is as the specified DC Def or not.
            //--------------------------------------------------------------------------
            log.info("## Check Process Condition of FPC. (DCSpecItems)");
            List<Infos.DCSpecDetailInfo> dcSpecList = strFPCInfo.getDcSpecList();
            if (!CimArrayUtils.isEmpty(dcSpecList)) {
                List<String> dcSpecItems = new ArrayList<>();
                dcSpecList.forEach(dcs -> dcSpecItems.add(dcs.getDataItemName()));
                this.fpcDcSpecItemConsistencyCheck(objCommon, strFPCInfo.getDcSpecID(), dcSpecItems);
            }

            //----------------------------------------------------------------------------
            // ** Check Equipment's Multi Recipe Capability and Recipe Parm Change Type.
            //----------------------------------------------------------------------------
            if (CimStringUtils.equals(strFPCInfo.getRecipeParameterChangeType(), BizConstant.SP_RPARM_CHANGETYPE_BYWAFER)) {
                if (!ObjectIdentifier.isEmpty(strFPCInfo.getEquipmentID())) {
                    log.info("{} ## Check Eqp's multiRcpCapa vs RcpPrmChgType(ByWafer) ", strFPCInfo.getEquipmentID());
                    CimMachine aPosMachine = baseCoreFactory.getBO(CimMachine.class, strFPCInfo.getEquipmentID());
                    if (null == aPosMachine) {
                        throw new ServiceException(new OmCode(retCodeConfig.getNotFoundEqp(), strFPCInfo.getEquipmentID().getValue()));
                    }

                    //multi recipe capability
                    String multiRecipeCapability = aPosMachine.getMultipleRecipeCapability();
                    log.info("{} ##  multiRecipeCapability", multiRecipeCapability);

                    if (CimStringUtils.equals(multiRecipeCapability, BizConstant.SP_EQP_MULTIRECIPECAPABILITY_SINGLERECIPE) ||
                            CimStringUtils.equals(multiRecipeCapability, BizConstant.SP_EQP_MULTIRECIPECAPABILITY_BATCH)) {
                        log.info("{} {} Can NOT set RecipeParameterChangeType is ByWafer because Equipment's multi recipe capability is SingleRecipe or Batch.",strFPCInfo.getEquipmentID(), strFPCInfo.getFpcID());
                        throw new ServiceException(new OmCode(retCodeConfig.getFpcMultipulRecipeError(), strFPCInfo.getFpcID(), strFPCInfo.getEquipmentID().getValue()));
                    }
                }
            }

            //-------------------------------------------------------
            // ** Reset each Process Condition Item object
            //-------------------------------------------------------
            log.info("## Reset each Process Condition Items object (Route)");
            //mainPDID/orgMainPDID/subMainPDID
            if (CimStringUtils.equals(fpcDefRoute, BizConstant.SP_FPC_DEFINITION_ROUTE_MAIN)) {
                //mainPDID
                strFPCInfo.setMainProcessDefinitionID(MAINROUTE);
            }
            if (CimStringUtils.equals(fpcDefRoute, BizConstant.SP_FPC_DEFINITION_ROUTE_SUB)) {
                //mainPDID
                strFPCInfo.setMainProcessDefinitionID(SUBROUTE);
                //orgMainPDID
                strFPCInfo.setOriginalMainProcessDefinitionID(MAINROUTE);
            }
            if (CimStringUtils.equals(fpcDefRoute, BizConstant.SP_FPC_DEFINITION_ROUTE_SUB2)) {
                //mainPDID
                strFPCInfo.setMainProcessDefinitionID(SUB2ROUTE);
                //orgMainPDID
                strFPCInfo.setOriginalMainProcessDefinitionID(MAINROUTE);
                //subMainPDID
                strFPCInfo.setSubMainProcessDefinitionID(SUBROUTE);
            }
            log.info("## Defined point ?  mainPDID/OrgMainPDID/SubMainPDID ",fpcDefRoute,strFPCInfo.getMainProcessDefinitionID(),strFPCInfo.getOriginalMainProcessDefinitionID(),strFPCInfo.getSubMainProcessDefinitionID());

            log.info("## Reset each Process Condition Itesm object (Other)");
            for (Infos.FPCProcessCondition fpcProcessCondition : fpcProcessConditions) {
                String objectType = fpcProcessCondition.getObjectType();
                ObjectIdentifier objectID = fpcProcessCondition.getObjectID();
                //PD
                if (CimStringUtils.equals(objectType, BizConstant.SP_FPC_PROCESSCONDITION_TYPE_PD) &&
                        ObjectIdentifier.equalsWithValue(objectID, strFPCInfo.getProcessDefinitionID())) {
                    strFPCInfo.setProcessDefinitionID(objectID);
                    log.info("{} ## Reset PDID              ", strFPCInfo.getProcessDefinitionID());
                }
                //Equipment
                if (CimStringUtils.equals(objectType, BizConstant.SP_FPC_PROCESSCONDITION_TYPE_EQUIPMENT) &&
                        ObjectIdentifier.equalsWithValue(objectID, strFPCInfo.getEquipmentID())) {
                    strFPCInfo.setEquipmentID(objectID);
                    log.info("{}  ## Reset EquipmentID       ", strFPCInfo.getEquipmentID());
                }
                //Machine Recipe
                if (CimStringUtils.equals(objectType, BizConstant.SP_FPC_PROCESSCONDITION_TYPE_MACHINERECIPE) &&
                        ObjectIdentifier.equalsWithValue(objectID, strFPCInfo.getMachineRecipeID())) {
                    strFPCInfo.setMachineRecipeID(objectID);
                    log.info("{} {} ## Reset MachineRecipeID   ", strFPCInfo.getMachineRecipeID());
                }
                //DC Def
                if (CimStringUtils.equals(objectType, BizConstant.SP_FPC_PROCESSCONDITION_TYPE_DCDEF) &&
                        ObjectIdentifier.equalsWithValue(objectID, strFPCInfo.getDcDefineID())) {
                    strFPCInfo.setDcDefineID(objectID);
                    log.info("{} {} ## Reset DCDefID           ", strFPCInfo.getDcDefineID());
                }
                //DC Spec
                if (CimStringUtils.equals(objectType, BizConstant.SP_FPC_PROCESSCONDITION_TYPE_DCSPEC) &&
                        ObjectIdentifier.equalsWithValue(objectID, strFPCInfo.getDcSpecID())) {
                    strFPCInfo.setDcSpecID(objectID);
                    log.info("{} {} ## Reset DCSpecID          ", strFPCInfo.getDcSpecID());
                }
                //Reticle
                if (CimStringUtils.equals(objectType, BizConstant.SP_FPC_PROCESSCONDITION_TYPE_RETICLE)) {
                    for (Infos.ReticleInfo reticleInfo : reticleInfoList) {
                        if (ObjectIdentifier.equalsWithValue(objectID, reticleInfo.getReticleID())) {
                            log.info("{} {} ## Reset ReticleID         ", reticleInfo.getReticleID());
                            reticleInfo.setReticleID(objectID);
                        }
                    }//loop of FPCInfo's reticle
                }
            }//loop of Process Condition

        }//Process Condition check

        //---------------------
        // Reset out structure
        //---------------------
        return strFPCInfoActionListAll;
    }

    @Override
    public List<Infos.FPCInfoAction> fpcInfoUpdateDR(Infos.ObjCommon objCommon, List<Infos.FPCInfoAction> strFPCInfoActionList) {

        List<Infos.FPCInfoAction> out = new ArrayList<>();

        int multiCorrespondingOperationoMode = 0;
        String aTemp_var = StandardProperties.OM_EDC_MULTI_CORRESPOND_FLAG.getValue();
        if (!CimStringUtils.isEmpty(aTemp_var)) {
            multiCorrespondingOperationoMode = Integer.parseInt(aTemp_var);
        }
        if (multiCorrespondingOperationoMode > 1) {
            multiCorrespondingOperationoMode = 0;
        }

        //-----------------------------
        // Initialize
        //-----------------------------

        int FPCInfoLen = CimArrayUtils.getSize(strFPCInfoActionList);
        Infos.FPCInfo strFPCInfo;

        for (int FPCInfoCnt = 0; FPCInfoCnt < FPCInfoLen; FPCInfoCnt++) {
            Infos.FPCInfoAction fpcInfoAction = strFPCInfoActionList.get(FPCInfoCnt);

            //Copy input Pram into out Pram
            strFPCInfo = fpcInfoAction.getStrFPCInfo();
            List<Infos.CorrespondingOperationInfo> correspondingOperationInfoList = strFPCInfo.getCorrespondingOperationInfoList();
            int corrOpeLen = CimObjectUtils.isEmpty(correspondingOperationInfoList) ? 0 : CimArrayUtils.getSize(correspondingOperationInfoList);
            //---------------------------
            // Omit NoChange record.
            //---------------------------
            if (CimStringUtils.equals(fpcInfoAction.getActionType(), BizConstant.SP_FPCINFO_NOCHANGE)) {
                continue;
            }

            //---------------------------------------
            // OSDOC
            //---------------------------------------
            //initialize of host variables. ---------------------------------------------------------------------------------
            //OSDOC
            String hFSFPCFPC_ID = strFPCInfo.getFpcID();
            ObjectIdentifier lotFamilyID = ObjectIdentifier.isEmpty(strFPCInfo.getLotFamilyID()) ? new ObjectIdentifier() : strFPCInfo.getLotFamilyID();
            String hFSFPCLOTFAMILY_ID = lotFamilyID.getValue();
            String hFSFPCLOTFAMILY_OBJ = lotFamilyID.getReferenceKey();
            ObjectIdentifier mainProcessDefinitionID = ObjectIdentifier.isEmpty(strFPCInfo.getMainProcessDefinitionID()) ? new ObjectIdentifier() : strFPCInfo.getMainProcessDefinitionID();
            String hFSFPCMAINPD_ID = mainProcessDefinitionID.getValue();
            String hFSFPCMAINPD_OBJ = mainProcessDefinitionID.getReferenceKey();
            String hFSFPCOPE_NO = strFPCInfo.getOperationNumber();
            ObjectIdentifier originalMainProcessDefinitionID = ObjectIdentifier.isEmpty(strFPCInfo.getOriginalMainProcessDefinitionID()) ? new ObjectIdentifier() : strFPCInfo.getOriginalMainProcessDefinitionID();
            String hFSFPCORG_MAINPD_ID = originalMainProcessDefinitionID.getValue();
            String hFSFPCORG_MAINPD_OBJ = originalMainProcessDefinitionID.getReferenceKey();
            String hFSFPCORG_OPE_NO = strFPCInfo.getOriginalOperationNumber();
            ObjectIdentifier subMainProcessDefinitionID = ObjectIdentifier.isEmpty(strFPCInfo.getSubMainProcessDefinitionID()) ? new ObjectIdentifier() : strFPCInfo.getSubMainProcessDefinitionID();
            String hFSFPCSUB_MAINPD_ID = subMainProcessDefinitionID.getValue();
            String hFSFPCSUB_MAINPD_OBJ = subMainProcessDefinitionID.getReferenceKey();
            String hFSFPCSUB_OPE_NO = strFPCInfo.getSubOperationNumber();
            Integer hFSFPCFPC_GROUP_NO = strFPCInfo.getFpcGroupNumber();
            String hFSFPCFPC_TYPE = strFPCInfo.getFpcType();
            ObjectIdentifier mergeMainProcessDefinitionID = ObjectIdentifier.isEmpty(strFPCInfo.getMergeMainProcessDefinitionID()) ? new ObjectIdentifier() : strFPCInfo.getMergeMainProcessDefinitionID();
            String hFSFPCMERGE_MAINPD_ID = mergeMainProcessDefinitionID.getValue();
            String hFSFPCMERGE_MAINPD_OBJ = mergeMainProcessDefinitionID.getReferenceKey();
            String hFSFPCMERGE_OPE_NO = strFPCInfo.getMergeOperationNumber();
            String hFSFPCFPC_CATEGORY_ID = null;
            ObjectIdentifier processDefinitionID = ObjectIdentifier.isEmpty(strFPCInfo.getProcessDefinitionID()) ? new ObjectIdentifier() : strFPCInfo.getProcessDefinitionID();
            String hFSFPCPD_ID = processDefinitionID.getValue();
            String hFSFPCPD_OBJ = processDefinitionID.getReferenceKey();
            String hFSFPCPD_TYPE = strFPCInfo.getProcessDefinitionType();
            String hFSFPCCORRESPOND_OPE_NO = null;
            String correspondOperationNumber = strFPCInfo.getCorrespondOperationNumber();
            if (multiCorrespondingOperationoMode == 0) {
                if ((CimObjectUtils.isEmpty(correspondOperationNumber) && corrOpeLen > 1) ||
                        (!CimObjectUtils.isEmpty(correspondOperationNumber) && corrOpeLen == 1) ||
                        (!CimObjectUtils.isEmpty(correspondOperationNumber) && corrOpeLen > 1)) {
                    throw new ServiceException(retCodeConfig.getOmEdcMultiCorrespondFlag());
                } else if (CimObjectUtils.isEmpty(correspondOperationNumber) && corrOpeLen == 1) {
                    hFSFPCCORRESPOND_OPE_NO = correspondingOperationInfoList.get(0).getCorrespondingOperationNumber();
                    corrOpeLen = 0;
                } else {
                    hFSFPCCORRESPOND_OPE_NO = correspondOperationNumber;
                }
            } else {
                if ((!CimObjectUtils.isEmpty(correspondOperationNumber) && corrOpeLen == 1) ||
                        (!CimObjectUtils.isEmpty(correspondOperationNumber) && corrOpeLen > 1)) {
                    throw new ServiceException(retCodeConfig.getOmEdcMultiCorrespondFlag());
                }
                hFSFPCCORRESPOND_OPE_NO = "";
            }
            boolean hFSFPCSKIP_FLAG = strFPCInfo.isSkipFalg();
            boolean hFSFPCRESTRICT_EQP_FLAG = strFPCInfo.isRestrictEquipmentFlag();
            ObjectIdentifier equipmentID = ObjectIdentifier.isEmpty(strFPCInfo.getEquipmentID()) ? new ObjectIdentifier() : strFPCInfo.getEquipmentID();
            String hFSFPCEQP_ID = equipmentID.getValue();
            String hFSFPCEQP_OBJ = equipmentID.getReferenceKey();
            String hFSFPCRPRM_CHANGE_TYPE = strFPCInfo.getRecipeParameterChangeType();
            ObjectIdentifier machineRecipeID = ObjectIdentifier.isEmpty(strFPCInfo.getMachineRecipeID()) ? new ObjectIdentifier() : strFPCInfo.getMachineRecipeID();
            String hFSFPCRECIPE_ID = machineRecipeID.getValue();
            String hFSFPCRECIPE_OBJ = machineRecipeID.getReferenceKey();
            ObjectIdentifier dcDefineID = ObjectIdentifier.isEmpty(strFPCInfo.getDcDefineID()) ? new ObjectIdentifier() : strFPCInfo.getDcDefineID();
            String hFSFPCDCDEF_ID = dcDefineID.getValue();
            String hFSFPCDCDEF_OBJ = dcDefineID.getReferenceKey();
            ObjectIdentifier dcSpecID = ObjectIdentifier.isEmpty(strFPCInfo.getDcSpecID()) ? new ObjectIdentifier() : strFPCInfo.getDcSpecID();
            String hFSFPCDCSPEC_ID = dcSpecID.getValue();
            String hFSFPCDCSPEC_OBJ = dcSpecID.getReferenceKey();
            Boolean hFSFPCSEND_EMAIL_FLAG = strFPCInfo.isSendEmailFlag();
            Boolean hFSFPCHOLD_LOT_FLAG = strFPCInfo.isHoldLotFlag();
            String hFSFPCDESCRIPTION = strFPCInfo.getDescription();
            String hFSFPCCLAIM_USER_ID = strFPCInfo.getClaimUserID();
            String hFSFPCCREATE_TIME = strFPCInfo.getCreateTime();
            String hFSFPCUPDATE_TIME = strFPCInfo.getUpdateTime();

            //---------------------------
            // Delete the update record.
            //---------------------------
            if (CimStringUtils.equals(fpcInfoAction.getActionType(), BizConstant.SP_FPCINFO_UPDATE)) {
                CimFPCDO cimFPCExam = new CimFPCDO();
                cimFPCExam.setFpcID(hFSFPCFPC_ID);
                cimFPCExam.setLotFamilyID(hFSFPCLOTFAMILY_ID);
                cimFPCExam.setMainProcessDefinitionID(hFSFPCMAINPD_ID);
                cimFPCExam.setOperationNumber(hFSFPCOPE_NO);
                cimFPCExam.setOriginalMainProcessDefinitionID(hFSFPCORG_MAINPD_ID);
                cimFPCExam.setOriginalOperationNumber(hFSFPCORG_OPE_NO);
                cimFPCExam.setSubOperationNumber(hFSFPCSUB_MAINPD_ID);
                cimFPCExam.setSubOperationNumber(hFSFPCSUB_OPE_NO);
                cimJpaRepository.delete(Example.of(cimFPCExam));

                CimFPCWaferDO cimFPCWaferExam = new CimFPCWaferDO();
                cimFPCWaferExam.setFpcID(hFSFPCFPC_ID);
                cimJpaRepository.delete(Example.of(cimFPCWaferExam));

                CimFPCWaferParamterDO cimFPCWaferParamterExam = new CimFPCWaferParamterDO();
                cimFPCWaferParamterExam.setFpcID(hFSFPCFPC_ID);
                cimJpaRepository.delete(Example.of(cimFPCWaferParamterExam));

                CimFPCReticleDO cimFPCReticleExam = new CimFPCReticleDO();
                cimFPCReticleExam.setFpcID(hFSFPCFPC_ID);
                cimJpaRepository.delete(Example.of(cimFPCReticleExam));

                CimFPCDataCollectionSpecificationDO specExam = new CimFPCDataCollectionSpecificationDO();
                specExam.setFpcID(hFSFPCFPC_ID);
                cimJpaRepository.delete(Example.of(specExam));

                CimFPCCoropeDO cimFPCCoropeExam = new CimFPCCoropeDO();
                cimFPCCoropeExam.setFpcID(hFSFPCFPC_ID);
                cimJpaRepository.delete(Example.of(cimFPCCoropeExam));
            }

            //--------------------------
            // Create/Update
            //--------------------------
            CimFPCDO fpcDO = new CimFPCDO();
            CimFPCDO cimFPCExam = new CimFPCDO();
            cimFPCExam.setFpcID(hFSFPCFPC_ID);
            List<CimFPCDO> fpcs = cimJpaRepository.findAll(Example.of(cimFPCExam));
            if (!CimObjectUtils.isEmpty(fpcs)) {
                fpcDO = fpcs.get(0);
            }
            fpcDO.setFpcID(hFSFPCFPC_ID);
            fpcDO.setLotFamilyID(hFSFPCLOTFAMILY_ID);
            fpcDO.setLotFamilyObj(hFSFPCLOTFAMILY_OBJ);
            fpcDO.setMainProcessDefinitionID(hFSFPCMAINPD_ID);
            fpcDO.setMainProcessDefinitionObj(hFSFPCMAINPD_OBJ);
            fpcDO.setOperationNumber(hFSFPCOPE_NO);
            fpcDO.setOriginalMainProcessDefinitionID(hFSFPCORG_MAINPD_ID);
            fpcDO.setOriginalMainProcessDefinitionObj(hFSFPCORG_MAINPD_OBJ);
            fpcDO.setOriginalOperationNumber(hFSFPCORG_OPE_NO);
            fpcDO.setSubMainProcessDefinitionID(hFSFPCSUB_MAINPD_ID);
            fpcDO.setSubMainProcessDefinitionObj(hFSFPCSUB_MAINPD_OBJ);
            fpcDO.setSubOperationNumber(hFSFPCSUB_OPE_NO);
            fpcDO.setFpcGroupNumber(hFSFPCFPC_GROUP_NO);
            fpcDO.setFpcType(hFSFPCFPC_TYPE);
            fpcDO.setMergeMainProcessDefinitionID(hFSFPCMERGE_MAINPD_ID);
            fpcDO.setMergeMainProcessDefinitionObj(hFSFPCMERGE_MAINPD_OBJ);
            fpcDO.setMergeOperationNumber(hFSFPCMERGE_OPE_NO);
            fpcDO.setFpcCategoryID(hFSFPCFPC_CATEGORY_ID);
            fpcDO.setProcessDefinitionID(hFSFPCPD_ID);
            fpcDO.setProcessDefinitionObj(hFSFPCPD_OBJ);
            fpcDO.setProcessDefinitionType(hFSFPCPD_TYPE);
            fpcDO.setCorrespondingOperationNumber(hFSFPCCORRESPOND_OPE_NO);
            fpcDO.setSkipFlag(hFSFPCSKIP_FLAG);
            fpcDO.setRestReticleEquipmentFlag(hFSFPCRESTRICT_EQP_FLAG);
            fpcDO.setEquipmentID(hFSFPCEQP_ID);
            fpcDO.setEquipmentObj(hFSFPCEQP_OBJ);
            fpcDO.setParameterChangeType(hFSFPCRPRM_CHANGE_TYPE);
            fpcDO.setRecipeID(hFSFPCRECIPE_ID);
            fpcDO.setRecipeObj(hFSFPCRECIPE_OBJ);
            fpcDO.setDataCollectionDefID(hFSFPCDCDEF_ID);
            fpcDO.setDataCollectionDefObj(hFSFPCDCDEF_OBJ);
            fpcDO.setDataCollectionSpecificationID(hFSFPCDCSPEC_ID);
            fpcDO.setDataCollectionSpecificationObj(hFSFPCDCSPEC_OBJ);
            fpcDO.setSendEmailFlag(hFSFPCSEND_EMAIL_FLAG);
            fpcDO.setHoldLotFlag(hFSFPCHOLD_LOT_FLAG);
            fpcDO.setDescription(hFSFPCDESCRIPTION);
            fpcDO.setClaimUserID(hFSFPCCLAIM_USER_ID);
            fpcDO.setCreateTime(Timestamp.valueOf(hFSFPCCREATE_TIME));
            fpcDO.setUpdateTime(Timestamp.valueOf(hFSFPCUPDATE_TIME));
            CimFPCDO cimFPCDO = cimJpaRepository.save(fpcDO);
            String fpcID = cimFPCDO.getFpcID();
            String fpcId = cimFPCDO.getId();

            //---------------------------------------
            // OSDOC_WAFER
            //---------------------------------------
            List<Infos.LotWaferInfo> lotWaferInfoList = strFPCInfo.getLotWaferInfoList();
            int waferLen = CimArrayUtils.getSize(lotWaferInfoList);
            int waferCnt;
            for (waferCnt = 0; waferCnt < waferLen; waferCnt++) {
                Infos.LotWaferInfo lotWaferInfo = lotWaferInfoList.get(waferCnt);
                CimFPCWaferDO fpcWaferDO = new CimFPCWaferDO();
                fpcWaferDO.setFpcID(fpcID);
                fpcWaferDO.setReferenceKey(fpcId);
                fpcWaferDO.setWaferID(lotWaferInfo.getWaferID().getValue());
                fpcWaferDO.setWaferObj(lotWaferInfo.getWaferID().getReferenceKey());
                cimJpaRepository.save(fpcWaferDO);

                List<Infos.RecipeParameterInfo> recipeParameterInfoList = lotWaferInfo.getRecipeParameterInfoList();
                int recipeParmLen = CimObjectUtils.isEmpty(recipeParameterInfoList) ? 0 : CimArrayUtils.getSize(recipeParameterInfoList);
                int recipeParmCnt;
                for (recipeParmCnt = 0; recipeParmCnt < recipeParmLen; recipeParmCnt++) {
                    Infos.RecipeParameterInfo recipeParameterInfo = recipeParameterInfoList.get(recipeParmCnt);

                    CimFPCWaferParamterDO fpcWaferParamterDO = new CimFPCWaferParamterDO();
                    fpcWaferParamterDO.setReferenceKey(fpcId);
                    fpcWaferParamterDO.setFpcID(fpcID);
                    fpcWaferParamterDO.setWaferID(lotWaferInfo.getWaferID().getValue());
                    fpcWaferParamterDO.setSequenceNumber((int)recipeParameterInfo.getSequenceNumber());
                    fpcWaferParamterDO.setParamterName(recipeParameterInfo.getParameterName());
                    fpcWaferParamterDO.setParamterUnit(recipeParameterInfo.getParameterUnit());
                    fpcWaferParamterDO.setParamterDataType(recipeParameterInfo.getParameterDataType());
                    fpcWaferParamterDO.setParamterLowerLimit(recipeParameterInfo.getParameterLowerLimit());
                    fpcWaferParamterDO.setParamterUpperLimit(recipeParameterInfo.getParameterUpperLimit());
                    fpcWaferParamterDO.setParamterUseCurrentFlag(recipeParameterInfo.isUseCurrentSettingValueFlag());
                    fpcWaferParamterDO.setParamterTargetValue(recipeParameterInfo.getParameterTargetValue());
                    fpcWaferParamterDO.setParamterValue(recipeParameterInfo.getParameterValue());
                    cimJpaRepository.save(fpcWaferParamterDO);
                }//loop of recipeParmLen
            }// loop of waferLen

            //---------------------------------------
            // OSDOC_RTCL
            //---------------------------------------
            List<Infos.ReticleInfo> reticleInfoList = strFPCInfo.getReticleInfoList();
            int rtclLen = CimObjectUtils.isEmpty(reticleInfoList) ? 0 : CimArrayUtils.getSize(reticleInfoList);
            int rtclCnt;
            for (rtclCnt = 0; rtclCnt < rtclLen; rtclCnt++) {
                Infos.ReticleInfo reticleInfo = reticleInfoList.get(rtclCnt);

                CimFPCReticleDO fpcReticleDO = new CimFPCReticleDO();
                fpcReticleDO.setReferenceKey(fpcId);
                fpcReticleDO.setFpcID(fpcID);
                fpcReticleDO.setSequenceNumber(new Long(reticleInfo.getSequenceNumber()).intValue());
                fpcReticleDO.setReticleID(reticleInfo.getReticleID().getValue());
                fpcReticleDO.setReticleObj(reticleInfo.getReticleID().getReferenceKey());
                fpcReticleDO.setReticleGroupID(reticleInfo.getReticleGroup().getValue());
                fpcReticleDO.setReticleGroupObj(reticleInfo.getReticleGroup().getReferenceKey());
                cimJpaRepository.save(fpcReticleDO);
            }


            //---------------------------------------
            // OSDOC_EDCSPEC
            //---------------------------------------
            List<Infos.DCSpecDetailInfo> dcSpecList = strFPCInfo.getDcSpecList();
            int dcSpecLen = CimObjectUtils.isEmpty(dcSpecList) ? 0 : CimArrayUtils.getSize(dcSpecList);
            int dcSpecCnt;
            for (dcSpecCnt = 0; dcSpecCnt < dcSpecLen; dcSpecCnt++) {
                Infos.DCSpecDetailInfo dcSpecDetailInfo = dcSpecList.get(dcSpecCnt);

                CimFPCDataCollectionSpecificationDO fpcDataCollectionSpecificationDO = new CimFPCDataCollectionSpecificationDO();
                fpcDataCollectionSpecificationDO.setReferenceKey(fpcId);
                fpcDataCollectionSpecificationDO.setFpcID(fpcID);
                fpcDataCollectionSpecificationDO.setDataCollectionItemName(dcSpecDetailInfo.getDataItemName());
                fpcDataCollectionSpecificationDO.setScreenUpperRequired(dcSpecDetailInfo.getScreenLimitUpperRequired());
                fpcDataCollectionSpecificationDO.setScreenUpperLimit(dcSpecDetailInfo.getScreenLimitUpper());
                fpcDataCollectionSpecificationDO.setScreenUpperActions(dcSpecDetailInfo.getActionCodes_uscrn());
                fpcDataCollectionSpecificationDO.setScreenLowerRequired(dcSpecDetailInfo.getScreenLimitLowerRequired());
                fpcDataCollectionSpecificationDO.setScreenLowerLimit(dcSpecDetailInfo.getScreenLimitLower());
                fpcDataCollectionSpecificationDO.setScreenLowerActions(dcSpecDetailInfo.getActionCodes_lscrn());
                fpcDataCollectionSpecificationDO.setSpecificationUpperRequired(dcSpecDetailInfo.getSpecLimitUpperRequired());
                fpcDataCollectionSpecificationDO.setSpecificationUpperLimit(dcSpecDetailInfo.getSpecLimitUpper());
                fpcDataCollectionSpecificationDO.setSpecificationUpperActions(dcSpecDetailInfo.getActionCodes_usl());
                fpcDataCollectionSpecificationDO.setSpecificationLowerRequired(dcSpecDetailInfo.getSpecLimitLowerRequired());
                fpcDataCollectionSpecificationDO.setSpecificationLowerLimit(dcSpecDetailInfo.getSpecLimitLower());
                fpcDataCollectionSpecificationDO.setSpecificationLowerActions(dcSpecDetailInfo.getActionCodes_lsl());
                fpcDataCollectionSpecificationDO.setControlUpperRequired(dcSpecDetailInfo.getControlLimitUpperRequired());
                fpcDataCollectionSpecificationDO.setControlUpperLimit(dcSpecDetailInfo.getControlLimitUpper());
                fpcDataCollectionSpecificationDO.setControlUpperActions(dcSpecDetailInfo.getActionCodes_ucl());
                fpcDataCollectionSpecificationDO.setControlLowerRequired(dcSpecDetailInfo.getControlLimitLowerRequired());
                fpcDataCollectionSpecificationDO.setControlLowerLimit(dcSpecDetailInfo.getControlLimitLower());
                fpcDataCollectionSpecificationDO.setControlLowerActions(dcSpecDetailInfo.getActionCodes_lcl());
                fpcDataCollectionSpecificationDO.setDataCollectionItemTarget(dcSpecDetailInfo.getTarget());
                fpcDataCollectionSpecificationDO.setDataCollectionItemTag(dcSpecDetailInfo.getTag());
                fpcDataCollectionSpecificationDO.setDataCollectionSpecificationGroup(multiCorrespondingOperationoMode == 0 ? "" : dcSpecDetailInfo.getDcSpecGroup());
                cimJpaRepository.save(fpcDataCollectionSpecificationDO);
            }

            if (multiCorrespondingOperationoMode == 0) {
                //do nothing
            } else {
                //---------------------------------------
                // OSDOC_COROPE
                //---------------------------------------
                int corrOpeCnt = 0;
                if (corrOpeLen == 0 && !CimObjectUtils.isEmpty(strFPCInfo.getCorrespondOperationNumber())) {
                    CimFPCCoropeDO fpcCoropeDO = new CimFPCCoropeDO();
                    fpcCoropeDO.setReferenceKey(fpcId);
                    fpcCoropeDO.setFpcID(fpcID);
                    fpcCoropeDO.setSequenceNumber(corrOpeCnt);
                    fpcCoropeDO.setCorrespondOperationNumber(strFPCInfo.getCorrespondOperationNumber());
                    cimJpaRepository.save(fpcCoropeDO);

                } else {
                    for (corrOpeCnt = 0; corrOpeCnt < corrOpeLen; corrOpeCnt++) {
                        Infos.CorrespondingOperationInfo correspondingOperationInfo = correspondingOperationInfoList.get(corrOpeCnt);
                        CimFPCCoropeDO fpcCoropeDO = new CimFPCCoropeDO();
                        fpcCoropeDO.setReferenceKey(fpcId);
                        fpcCoropeDO.setFpcID(fpcID);
                        fpcCoropeDO.setSequenceNumber(corrOpeCnt);
                        fpcCoropeDO.setCorrespondOperationNumber(correspondingOperationInfo.getCorrespondingOperationNumber());
                        fpcCoropeDO.setDataCollectionSpecificationGroup(correspondingOperationInfo.getDcSpecGroup());
                        cimJpaRepository.save(fpcCoropeDO);
                    }
                }
            }

            //Insert data into the RUNCARD_PSM_DOC table -- added by Nyx
            String psmKey = strFPCInfo.getPsmKey();
            String psmIDObj = "";
            Object[] psmIDArray = cimJpaRepository.queryOne("SELECT rp.PSM_JOB_ID FROM RUNCARD_PSM rp WHERE rp.PSM_KEY = ?1", psmKey);
            if (!CimObjectUtils.isEmpty(psmIDArray)){
                if (!CimObjectUtils.isEmpty(psmIDArray[0])){
                    psmIDObj = (String) psmIDArray[0];
                }
            }
            String psmID = CimObjectUtils.isEmpty(psmKey) ? "" : psmIDObj;
            if (!CimObjectUtils.isEmpty(psmID) && !CimObjectUtils.isEmpty(psmKey)
                    && waferLen > 0 && Objects.nonNull(runCardMethod.getRunCardInfoByDoc(objCommon,strFPCInfo))) {
                CimRunCardPsmDocDO runCardPsmDoc = cimJpaRepository.queryOne("SELECT * FROM RUNCARD_PSM_DOC WHERE DOC_JOB_ID = ?1", CimRunCardPsmDocDO.class, fpcID);
                if (CimObjectUtils.isEmpty(runCardPsmDoc)) {
                    runCardPsmDoc = new CimRunCardPsmDocDO();
                    runCardPsmDoc.setDocJobID(fpcID);
                    runCardPsmDoc.setPsmJobID(psmID);
                    runCardPsmDoc.setPsmKey(psmKey);
                    runCardPsmDoc.setCreateTime(objCommon.getTimeStamp().getReportTimeStamp());
                    Object[] seq = cimJpaRepository.queryOne(
                            "select IDX_NO\n" +
                                    "  from (select IDX_NO, rownum rn\n" +
                                    "          from (SELECT IDX_NO\n" +
                                    "                  FROM RUNCARD_PSM_DOC\n" +
                                    "                 WHERE PSM_JOB_ID = ?1\n" +
                                    "                   AND PSM_KEY = ?2\n" +
                                    "                 ORDER BY IDX_NO DESC))\n" +
                                    " where rn = 1",
                            psmID, psmKey);
                    runCardPsmDoc.setSequenceNumber(CimObjectUtils.isEmpty(seq) ? 0 : CimNumberUtils.intValue((Number) seq[0]) + 1);
                }
                runCardPsmDoc.setUpdateTime(objCommon.getTimeStamp().getReportTimeStamp());
                cimJpaRepository.save(runCardPsmDoc);
            }
        }
        return out;
    }

    @Override
    public String fpcRouteOperationConsistencyCheck(Infos.ObjCommon objCommon, Infos.FPCInfo strFPCInfo, ObjectIdentifier routeID) {

        String out;
        log.info("### INPUT Parameter");
        log.info("{}### mainRouteID             ", routeID);
        log.info("{} {}### mainPDID   , mainOperNo ", strFPCInfo.getMainProcessDefinitionID(), strFPCInfo.getOperationNumber());
        log.info("{} {}### orgMainPDID, orgOperNo  ", strFPCInfo.getOriginalMainProcessDefinitionID(), strFPCInfo.getOriginalOperationNumber());
        log.info("{} {}### subMainPDID, subOperNo  ", strFPCInfo.getSubMainProcessDefinitionID(), strFPCInfo.getSubOperationNumber());
        log.info("{}### pdID                    ", strFPCInfo.getProcessDefinitionID());
        log.info("{}### correspondingOperNo     ", strFPCInfo.getCorrespondOperationNumber());


        boolean mainDefFlag = false;
        //----------------------------------------------------------------------
        // Check definition point of FPC (MainRoute or SubRoute of Sub2Route )
        //----------------------------------------------------------------------
        List<Infos.OperationInfo> strProcess_operationListInRoute_GetDR_out;
        if (ObjectIdentifier.equalsWithValue(routeID, strFPCInfo.getMainProcessDefinitionID())) {
            strProcess_operationListInRoute_GetDR_out = processMethod.processOperationListInRouteGetDR(objCommon, strFPCInfo.getMainProcessDefinitionID(), strFPCInfo.getOperationNumber(), BizConstant.SP_MAINPDTYPE_REWORK);
            mainDefFlag = true;
        } else if (ObjectIdentifier.equalsWithValue(routeID, strFPCInfo.getOriginalMainProcessDefinitionID())) {
            //-------------------------
            // SubRoute or Sub2Route
            //-------------------------
            strProcess_operationListInRoute_GetDR_out = processMethod.processOperationListInRouteGetDR(objCommon, strFPCInfo.getOriginalMainProcessDefinitionID(), strFPCInfo.getOriginalOperationNumber(), BizConstant.SP_MAINPDTYPE_REWORK);
        } else {
            log.info("MainRoute defined FPC information is not found. ");
            throw new ServiceException(retCodeConfig.getFpcRouteInfoError());
        }

        //-------------------------------------------------------------
        // Check the existence of Route/Operation in Specification.
        //-------------------------------------------------------------
        List<Infos.OperationInfo> operationInfos = strProcess_operationListInRoute_GetDR_out;
        int mainLen = CimArrayUtils.getSize(operationInfos);  //by MainRoute
        int exitNum;
        log.info("{}### OperationList Len       ", mainLen);
        if (0 == mainLen) {
            log.info("MainRoute defined FPC information is not found. ");
            throw new ServiceException(retCodeConfig.getFpcRouteInfoError());
        }
        exitNum = 0;


        //---------------------------------------------------
        // Defined on MainRoute : mainDefFlag is TRUE.
        //---------------------------------------------------
        Infos.OperationInfo operationInfoEx = operationInfos.get(exitNum);
        boolean existPDFlag;
        if (mainDefFlag) {
            log.info("### Defined on MainRoute?   ");
            log.info("{}### OperID (Main)           ", operationInfoEx.getOperationID());
            log.info("{}### OpeNo  (Main)           ", operationInfoEx.getOperationNumber());
            if (!ObjectIdentifier.equalsWithValue(strFPCInfo.getProcessDefinitionID(), operationInfoEx.getOperationID()) ||
                    !CimStringUtils.equals(strFPCInfo.getOperationNumber(), operationInfoEx.getOperationNumber())) {
                //error
                log.info("PDID defined FPC information is not found. ");
                throw new ServiceException(retCodeConfig.getFpcRouteInfoError());
            }

            //-----------------------
            // Set output Parm
            //-----------------------
            log.info("## The specified operation point of FPC info is MainRoute.");
            out = BizConstant.SP_FPC_DEFINITION_ROUTE_MAIN;
        }
        //---------------------------------------------------
        // Defined on Sub/Sub2Route : mainDefFlag is FALSE.
        //---------------------------------------------------
        else {
            List<Infos.LotWaferInfo> lotWaferInfoList = strFPCInfo.getLotWaferInfoList(); //add for runCard
            boolean runCard = !CimObjectUtils.isEmpty(lotWaferInfoList)
                    && Objects.nonNull(runCardMethod.getRunCardInfoByDoc(objCommon,strFPCInfo)); //add for runCard
            if (ObjectIdentifier.isEmpty(strFPCInfo.getSubMainProcessDefinitionID())) {
                //---------------------------------------------------
                // Defined on SubRoute
                //---------------------------------------------------
                existPDFlag = false;
                List<Infos.OperationInfo> strProcess_operationListInRoute_GetDR_out_Sub = null;
                List<Infos.ConnectedRoute> connectedRouteList = operationInfoEx.getConnectedRouteList();
                int mainConLen = CimArrayUtils.getSize(connectedRouteList);
                log.info("{}### Defined on SubRoute?    ", mainConLen);
                for (int i = 0; i < mainConLen; i++) {
                    Infos.ConnectedRoute connectedRoute = connectedRouteList.get(i);
                    log.info("{}### ConnectedRoute(SubRoute)", connectedRoute.getRouteID());
                    if (ObjectIdentifier.equalsWithValue(strFPCInfo.getMainProcessDefinitionID(), connectedRoute.getRouteID())) {
                        strProcess_operationListInRoute_GetDR_out_Sub = processMethod.processOperationListInRouteGetDR(objCommon, strFPCInfo.getMainProcessDefinitionID(), strFPCInfo.getOperationNumber(), "");
                        existPDFlag = true;
                        break;
                    }
                }//for mainConLen

                List<Infos.OperationInfo> operationInfoSubs = strProcess_operationListInRoute_GetDR_out_Sub;
                int subLen = CimArrayUtils.getSize(operationInfoSubs);
                log.info("{}### Connected Oper List Len ", subLen);
                int exitNum2;
                if (!runCard) { //add for runCard
                    if (0 == subLen || !existPDFlag) {
                        log.info("The Route/Operation defined FPC information is not found. ");
                        throw new ServiceException(retCodeConfig.getFpcRouteInfoError());
                    }
                    exitNum2 = 0;
                    Infos.OperationInfo operationInfoEx2 = operationInfoSubs.get(exitNum2);
                    //Check operation ID.
                    log.info("{}### OperID (Sub)            ", operationInfoEx2.getOperationID());
                    log.info("{}### OpeNo  (Sub)            ", operationInfoEx2.getOperationNumber());
                    if (!ObjectIdentifier.equalsWithValue(strFPCInfo.getProcessDefinitionID(), operationInfoEx2.getOperationID()) ||
                            !CimStringUtils.equals(strFPCInfo.getOperationNumber(), operationInfoEx2.getOperationNumber())) {
                        log.info("PDID defi|ned FPC information is not found. ");
                        throw new ServiceException(retCodeConfig.getFpcRouteInfoError());
                    }
                } //add for runCard

                //-----------------------
                // Set output Parm
                //-----------------------
                log.info("## The specified operation point of FPC info is SubRoute.");
                out = BizConstant.SP_FPC_DEFINITION_ROUTE_SUB;
            } else {
                //---------------------------------------------------
                // Defined on Sub2Route
                //---------------------------------------------------
                // Check route is "Main -> Sub -> Sub2 "
                existPDFlag = false;
                List<Infos.OperationInfo> strProcess_operationListInRoute_GetDR_out_Sub = new ArrayList<>();
                List<Infos.ConnectedRoute> connectedRouteList = operationInfoEx.getConnectedRouteList();
                int mainConLen = CimArrayUtils.getSize(connectedRouteList);
                log.info("{}### Defined on Sub2Route?    ", mainConLen);
                for (int i = 0; i < mainConLen; i++) {
                    Infos.ConnectedRoute connectedRoute = connectedRouteList.get(i);
                    //Check the specified MainPDID
                    log.info("{}### ConnectedRoute(SubRoute)", connectedRoute.getRouteID());
                    if (ObjectIdentifier.equalsWithValue(strFPCInfo.getSubMainProcessDefinitionID(), connectedRoute.getRouteID())) {
                        //Check existence of connected Route = SubRoute and the specified operation(strFPCInfo.subOperationNumber)
                        strProcess_operationListInRoute_GetDR_out_Sub = processMethod.processOperationListInRouteGetDR(objCommon, strFPCInfo.getSubMainProcessDefinitionID(), strFPCInfo.getSubOperationNumber(), BizConstant.SP_MAINPDTYPE_REWORK);

                        existPDFlag = true;
                        break;
                    }
                }//for mainConLen

                //Check existence of Sub2Route.
                List<Infos.OperationInfo> operationInfoSubs = strProcess_operationListInRoute_GetDR_out_Sub;
                int subLen = CimArrayUtils.getSize(operationInfoSubs);
                log.info("{} {}### Connected Oper List Len ", subLen, (existPDFlag ? "TRUE" : "FALSE"));
                int exitNum2;
                if (0 == subLen || !existPDFlag) {
                    log.info("The Route/Operation defined FPC information is not found. ");
                    throw new ServiceException(retCodeConfig.getFpcRouteInfoError());
                }
                exitNum2 = 0;
                if (!runCard){
                    //Point Sub2Route.
                    List<Infos.OperationInfo> strProcess_operationListInRoute_GetDR_out_Sub2 = null;
                    List<Infos.ConnectedRoute> connectedRoutes = operationInfoSubs.get(exitNum2).getConnectedRouteList();
                    int subConLen = CimArrayUtils.getSize(connectedRoutes);
                    existPDFlag = false;
                    for (int i = 0; i < subConLen; i++) {
                        Infos.ConnectedRoute connectedRoute = connectedRoutes.get(i);
                        //Check existence of connected Route = Sub2Route.
                        log.info("{}### ConnectedRoute(Sub2Route)", connectedRoute.getRouteID());
                        if (ObjectIdentifier.equalsWithValue(strFPCInfo.getMainProcessDefinitionID(), connectedRoute.getRouteID())) {
                            //Check the specified operation(strFPCInfo.operationNumber)
                            strProcess_operationListInRoute_GetDR_out_Sub2 = processMethod.processOperationListInRouteGetDR(objCommon, strFPCInfo.getMainProcessDefinitionID(), strFPCInfo.getOperationNumber(), "");
                            existPDFlag = true;
                            break;
                        }
                    }

                    List<Infos.OperationInfo> operationInfoSub2s = strProcess_operationListInRoute_GetDR_out_Sub2;
                    int subLen2 = CimArrayUtils.getSize(operationInfoSub2s);
                    log.info("{} {}### Connected Oper List Len ", subLen2, (existPDFlag ? "TRUE" : "FALSE"));
                    int exitNum3;
                    if (0 == subLen2 || !existPDFlag) {
                        log.info("The Route/Operation defined FPC information is not found. ");
                        throw new ServiceException(retCodeConfig.getFpcRouteInfoError());
                    }
                    exitNum3 = 0;

                    Infos.OperationInfo operationInfoEx3 = operationInfoSub2s.get(exitNum3);
                    //Check operation ID.
                    log.info("{}### OperID (Sub2)           ", operationInfoEx3.getOperationID());
                    log.info("{}### OpeNo  (Sub2)           ", operationInfoEx3.getOperationNumber());
                    if (!ObjectIdentifier.equalsWithValue(strFPCInfo.getProcessDefinitionID(), operationInfoEx3.getOperationID()) ||
                            !CimStringUtils.equals(strFPCInfo.getOperationNumber(), operationInfoEx3.getOperationNumber())) {
                        log.info("PDID defined FPC information is not found. ");
                        throw new ServiceException(retCodeConfig.getFpcRouteInfoError());
                    }
                }
                //-----------------------
                // Set output Parm
                //-----------------------
                log.info("## The specified operation point of FPC info is Sub2Route.");
                out = BizConstant.SP_FPC_DEFINITION_ROUTE_SUB2;

            }//Sub2
        }//Sub or Sub2


        //-------------------------------------
        // Check Corresponding Oper No
        //-------------------------------------
        List<Infos.CorrespondingOperationInfo> correspondingOperationInfoList = strFPCInfo.getCorrespondingOperationInfoList();
        int corrOperLen = CimArrayUtils.getSize(correspondingOperationInfoList);
        if (!CimObjectUtils.isEmpty(strFPCInfo.getCorrespondOperationNumber()) || 0 != corrOperLen) {
            log.info("{}## Check : Corresponding Oper No                   ", strFPCInfo.getCorrespondOperationNumber());
            strProcess_operationListInRoute_GetDR_out = processMethod.processOperationListInRouteGetDR(objCommon, strFPCInfo.getMainProcessDefinitionID(), null, "");


            //Get The specified FPC info Point in route operation.
            int dfPoint = -1;
            List<Infos.OperationInfo> operationInfoList = strProcess_operationListInRoute_GetDR_out;
            int operLen = CimArrayUtils.getSize(operationInfoList);
            for (int i = 0; i < operLen; i++) {
                Infos.OperationInfo operationInfo = operationInfoList.get(i);
                if (CimStringUtils.equals(strFPCInfo.getOperationNumber(), operationInfo.getOperationNumber())) {
                    dfPoint = i;
                    break;
                }
            }

            log.info("{} {} {} ## The specified FPC info Point in route operation.", dfPoint, strFPCInfo.getMainProcessDefinitionID(), strFPCInfo.getOperationNumber());
            if (0 > dfPoint) {
                log.info("MainRoute defined FPC information is not found. ");
                throw new ServiceException(retCodeConfig.getFpcRouteInfoError());
            } else {
                //Corresponding Oper No point should be before the specified FPC info point.
                boolean existCorrFlag = false;
                if (0 != corrOperLen) {
                    //loop for number of corresponding operations
                    int corrOpeCount = 0;
                    for (int j = 0; j < corrOperLen; j++) {
                        Infos.CorrespondingOperationInfo correspondingOperationInfo = strFPCInfo.getCorrespondingOperationInfoList().get(j);
                        for (int i = 0; i < dfPoint; i++) {
                            Infos.OperationInfo operationInfo = operationInfoList.get(i);
                            if (CimStringUtils.equals(correspondingOperationInfo.getCorrespondingOperationNumber(), operationInfo.getOperationNumber())) {
                                corrOpeCount++;
                                break;
                            }
                        }
                    }
                    if (corrOpeCount == corrOperLen) {
                        //Existence OK
                        existCorrFlag = true;
                    }
                } else {
                    for (int i = 0; i < dfPoint; i++) {
                        Infos.OperationInfo operationInfo = operationInfoList.get(i);
                        if (CimStringUtils.equals(strFPCInfo.getCorrespondOperationNumber(), operationInfo.getOperationNumber())) {
                            existCorrFlag = true;
                            break;
                        }
                    }
                }
                if (!existCorrFlag) {
                    //error
                    log.info("The specified corresponding Oper is NOT found before the spefied FPC info point.");
                    throw new ServiceException(retCodeConfig.getFpcRouteInfoError());
                }
            }
            log.info("## The specified CorrespondingOperNo exist before the FPC info Point.");
        }

        return out;
    }

    @Override
    public void fpcRecipeParameterConsistencyCheck(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, List<String> recipeParmNames) {


        //----------------------------------------
        // Get Recipe Parameter of Equipment
        //----------------------------------------
        List<Infos.RecipeParameterInfo> recipeParameterInfos = recipeMethod.recipeParameterInfoGetDR(objCommon, equipmentID);

        //----------------------------------------------------------
        // Compare Recipe Parameter of Equipment with FPC Info
        //----------------------------------------------------------
        int smRecipeParmLen = CimArrayUtils.getSize(recipeParameterInfos);
        int inRecipeParmLen = CimArrayUtils.getSize(recipeParmNames);
        if (smRecipeParmLen != inRecipeParmLen) {
            log.info("Recipe Parameter Name Count is mismatch. Defined vs FPC ", smRecipeParmLen, inRecipeParmLen);
            throw new ServiceException(retCodeConfig.getFpcRecipeParamError());
        }

        boolean inConsistentFlag;
        for (String recipename : recipeParmNames) {
            log.info(" {} Recipe Parameter Name of FPC", recipename);
            inConsistentFlag = false;
            for (Infos.RecipeParameterInfo recipeParameterInfo : recipeParameterInfos) {
                if (CimStringUtils.equals(recipename, recipeParameterInfo.getParameterName())) {
                    inConsistentFlag = true;
                    break;
                }
            }
            if (!inConsistentFlag) {
                log.error("Recipe Parameter Name is mismatch. ");
                throw new ServiceException(retCodeConfig.getFpcRecipeParamError());
            }
        }
    }

    @Override
    public void fpcDcSpecItemConsistencyCheck(Infos.ObjCommon objCommon, ObjectIdentifier dcSpecID, List<String> dcSpecItems) {

        //----------------------------------------
        // Get Items of DC Spec
        //----------------------------------------
        Results.EDCSpecInfoInqResult edcSpecInfoInqResult = dataCollectionMethod.dcSpecDetailInfoGetDR(objCommon, dcSpecID);

        List<Infos.DCSpecDetailInfo> strDCSpecList = edcSpecInfoInqResult.getStrDCSpecList();

        //----------------------------------------------------------
        // Compare item name of DC Spec with FPC Info
        //----------------------------------------------------------
        int smDcSpecLen = CimArrayUtils.getSize(strDCSpecList);
        int inDcSpecLen = CimArrayUtils.getSize(dcSpecItems);
        if (smDcSpecLen != inDcSpecLen) {
            throw new ServiceException(retCodeConfig.getFpcDcspecitemError());
        }

        boolean inConsistentFlag;
        for (String dcSpecItem : dcSpecItems) {
            log.info("DC Spec Item name of FPC {}", dcSpecItem);
            inConsistentFlag = false;
            for (Infos.DCSpecDetailInfo dcSpecDetailInfo : strDCSpecList) {
                if (CimStringUtils.equals(dcSpecItem, dcSpecDetailInfo.getDataItemName())) {
                    inConsistentFlag = true;
                    break;
                }
            }
            if (!inConsistentFlag) {
                log.error("DC Spec Item is mismatch. ");
                throw new ServiceException(retCodeConfig.getFpcDcspecitemError());
            }
        }
    }

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param querySql
     * @param clz
     * @param objects  -
     * @return void
     * @author Nyx
     * @date 2019/4/10 15:51
     */
    private <S extends NonRuntimeEntity> void removeEntitys(String querySql, Class<S> clz, Object... objects) {
        List<S> objs = cimJpaRepository.query(querySql, clz, objects);
        if (!CimObjectUtils.isEmpty(objs)) {
            objs.forEach(obj -> cimJpaRepository.delete(obj));
        }
    }

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/4/2                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/4/2 16:21
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Override
    public Outputs.ObjFPCCheckConditionForUpdateOut fpcCheckConditionForUpdate(Infos.ObjCommon objCommon, Inputs.FPCCheckConditionForUpdateIn in) {
        //init
        Outputs.ObjFPCCheckConditionForUpdateOut out = new Outputs.ObjFPCCheckConditionForUpdateOut();
        log.info("in-para : {}", JSONObject.toJSONString(in));

        ObjectIdentifier lotFamilyID = in.getLotFamilyID();
        ObjectIdentifier mainProcessDefinitionID = in.getMainPDID();
        String operationNumber = in.getMainOpeNo();
        ObjectIdentifier originalMainProcessDefinitionID = in.getOrgMainPDID();
        String originalOperationNumber = in.getOrgOpeNo();
        ObjectIdentifier subMainProcessDefinitionID = in.getSubMainPDID();
        String subOperationNumber = in.getSubOpeNo();
        String actionType = in.getActionType();
        out.setHoldFlag(false);
        if (ObjectIdentifier.isEmptyWithValue(lotFamilyID)) {
            throw new ServiceException(retCodeConfig.getInvalidInputParam());
        }
        if (ObjectIdentifier.isEmptyWithValue(mainProcessDefinitionID) ||
                CimStringUtils.isEmpty(operationNumber) ||
                (!ObjectIdentifier.isEmptyWithValue(originalMainProcessDefinitionID) && CimStringUtils.isEmpty(originalOperationNumber)) ||
                (ObjectIdentifier.isEmptyWithValue(originalMainProcessDefinitionID) && !CimStringUtils.isEmpty(originalOperationNumber)) ||
                (!ObjectIdentifier.isEmptyWithValue(subMainProcessDefinitionID) && CimStringUtils.isEmpty(subOperationNumber)) ||
                (ObjectIdentifier.isEmptyWithValue(subMainProcessDefinitionID) && !CimStringUtils.isEmpty(subOperationNumber)) ||
                (ObjectIdentifier.isEmptyWithValue(originalMainProcessDefinitionID) && !ObjectIdentifier.isEmptyWithValue(subMainProcessDefinitionID))) {
            throw new ServiceException(retCodeConfig.getInvalidInputParam());

        }
        //Get LotIDList from FRWAFER
        //【step1】lot_wafersStatusList_GetDR
        List<Infos.WaferListInLotFamilyInfo> lotWaferStatusListDROut = lotMethod.lotWafersStatusListGetDR(objCommon, lotFamilyID);

        // narrow down to unique lot
        int tmpLotLen = CimArrayUtils.getSize(lotWaferStatusListDROut);
        log.info("Lot count in lotFamily. {}", tmpLotLen);
        List<ObjectIdentifier> lotIDList = new ArrayList<>();
        int lotCnt = 0;
        boolean firstCondition = true;
        boolean existFlag = false;
        for (int tmpLotCnt = 0; tmpLotCnt < tmpLotLen; tmpLotCnt++) {
            if (firstCondition) {
                ObjectIdentifier lotID = lotWaferStatusListDROut.get(tmpLotCnt).getLotID();
                lotIDList.add(lotCnt, lotID);
                log.info("ActiveLotID in this LotFamily {}", lotIDList.get(lotCnt).getValue());
                firstCondition = false;
                lotCnt++;
            } else {
                existFlag = false;
                for (int tmpCnt = 0; tmpCnt < lotCnt; tmpCnt++) {
                    if (CimStringUtils.equals(lotWaferStatusListDROut.get(tmpLotCnt).getLotID().getValue(), lotIDList.get(tmpCnt).getValue())) {
                        log.info("this lot is already obtained.");
                        existFlag = true;
                        break;
                    }
                }
                if (!existFlag) {
                    ObjectIdentifier lotID = lotWaferStatusListDROut.get(tmpLotCnt).getLotID();
                    lotIDList.add(lotCnt, lotID);
                    log.info("ActiveLotID in this LotFamily {}", lotIDList.get(lotCnt).getValue());
                    lotCnt++;
                }
            }
        }
        int lotLen = lotCnt;
        log.info("lotLen {}", lotLen);
        int count = 0;
        List<ObjectIdentifier> strLotFamilyCurrentStatusList = new ArrayList<>();
        if (lotLen > 0) {
            for (lotCnt = 0; lotCnt < lotLen; lotCnt++) {
                //Check AvailFlag
                //【step2】lot_FPCAvailFlag_Get
                Boolean lotFPCAvailFlagGetOut = lotMethod.lotFPCAvailFlagGet(objCommon, lotIDList.get(lotCnt));

                if (!lotFPCAvailFlagGetOut) {
                    log.error("LotFPCAvailableFlag is OFF");
                    throw new ServiceException(retCodeConfig.getFpcNotavailableError());
                }
                //Get Lot current info
                //【tep3】lot_currentOperationInfo_Get
                Outputs.ObjLotCurrentOperationInfoGetOut lotCurrentOperationInfoOut = lotMethod.lotCurrentOperationInfoGet(objCommon, lotIDList.get(lotCnt));

                if (ObjectIdentifier.equalsWithValue(mainProcessDefinitionID, lotCurrentOperationInfoOut.getRouteID())
                        && CimStringUtils.equals(operationNumber, lotCurrentOperationInfoOut.getOperationNumber())) {
                    log.info("Defined routeID {} in FPC info and the routeID  {} of currentLot is corresponded.",lotCurrentOperationInfoOut.getRouteID().getValue(),lotCurrentOperationInfoOut.getOperationNumber());
                    log.info("LotID =  {}", lotIDList.get(lotCnt).getValue());
                    //Check controlJob
                    //【step4】lot_controlJobID_Get
                    ObjectIdentifier lotControlJobIDOut = lotMethod.lotControlJobIDGet(objCommon, lotIDList.get(lotCnt));
                    Validations.check(!ObjectIdentifier.isEmpty(lotControlJobIDOut), new OmCode(retCodeConfig.getLotControlJobidFilled(),
                            ObjectIdentifier.fetchValue(lotIDList.get(lotCnt)), ObjectIdentifier.fetchValue(lotControlJobIDOut)));
                    // Get Cassette from Lot
                    //【step5】lot_cassette_Get
                    int retCode = 0;
                    try {
                        lotMethod.lotCassetteGet(objCommon, lotIDList.get(lotCnt));
                    } catch (ServiceException e) {
                        retCode = e.getCode();
                        if (!Validations.isEquals(retCodeConfig.getNotFoundCst(), e.getCode())){
                            throw e;
                        }
                    }
                    if (retCode == 0){
                        //Check Xfer state
                        //【step6】lot_transferState_Get
                        String lotTransferStateGetOut = lotMethod.lotTransferStateGet(objCommon, lotIDList.get(lotCnt));

                        log.info("xferState {}", lotTransferStateGetOut);
                        if (CimStringUtils.equals(BizConstant.SP_TRANSSTATE_EQUIPMENTIN, lotTransferStateGetOut)) {
                            log.info("Lot transfer state is 'EI'.");
                            throw new ServiceException(new OmCode(retCodeConfig.getInvalidLotXferstat(), ObjectIdentifier.fetchValue(lotIDList.get(lotCnt)), lotTransferStateGetOut));
                        }
                    }
                    // Check process state
                    //【step7】lot_processState_Get
                    String lotProcessStateGetOut = lotMethod.lotProcessStateGet(objCommon, lotIDList.get(lotCnt));
                    log.info("lot processState {}", lotProcessStateGetOut);
                    if (CimStringUtils.equals(BizConstant.SP_LOT_PROCSTATE_PROCESSING, lotProcessStateGetOut)) {
                        log.info("Lot processState is 'Processing'");
                        throw new ServiceException(retCodeConfig.getInvalidLotProcstat());
                    }
                    ObjectIdentifier lotFamilyCurrentStatus = ObjectIdentifier.build(lotIDList.get(lotCnt).getValue(), lotIDList.get(lotCnt).getReferenceKey());
                    strLotFamilyCurrentStatusList.add(count, lotFamilyCurrentStatus);
                    count++;
                }
            }
        }
        log.info("lot count of same operation {}", count);
        List<ObjectIdentifier> heldLotIDs = new ArrayList<>();
        if (count > 0) {
            //Check Hold Record
            boolean bNonHoldFlag = true;
            boolean bFoundFPCHFlag = false;
            boolean bFoundRSOPFlag = false;
            boolean bOKFlag = false;
            int nHeldLotCnt = 0;
            for (lotCnt = 0; lotCnt < count; lotCnt++) {
                bFoundFPCHFlag = false;
                bFoundRSOPFlag = false;
                com.fa.cim.newcore.bo.product.CimLot aLot = baseCoreFactory.getBO(com.fa.cim.newcore.bo.product.CimLot.class, strLotFamilyCurrentStatusList.get(lotCnt));
                int hrLen = 0;
                List<ProductDTO.HoldRecord> strHoldRecords = aLot.allHoldRecords();
                hrLen = CimArrayUtils.getSize(strHoldRecords);
                log.info("strHoldRecords->length() : {}", hrLen);
                if (hrLen == 0) {
                    log.info("Not found hold record.");
                    continue;
                }
                for (int hrCnt = 0; hrCnt < hrLen; hrCnt++) {
                    bNonHoldFlag = false;
                    ObjectIdentifier reasonCodeID = strHoldRecords.get(hrCnt).getReasonCode();
                    log.info("This holdReason is {}", reasonCodeID.getValue());
                    boolean responsibleOpeFlag = strHoldRecords.get(hrCnt).isResponsibleOperationFlag();
                    if (CimStringUtils.equals(BizConstant.SP_REASON_FPCHOLD, reasonCodeID.getValue())) {
                        log.info("holdReason is 'FPCH'. break.");
                        bFoundFPCHFlag = true;
                        break;
                    } else if (responsibleOpeFlag) {
                        log.info("responsibleOperationFlag is TRUE. break.");
                        bFoundRSOPFlag = true;
                        break;
                    }
                }
                if (bFoundFPCHFlag || bFoundRSOPFlag) {
                    log.info("This Lot has 'FPCH' hold record or responsibleOpeFlag is true. return OK");
                    bOKFlag = true;
                    ObjectIdentifier lotID = strLotFamilyCurrentStatusList.get(lotCnt);
                    heldLotIDs.add(nHeldLotCnt, lotID);
                    nHeldLotCnt++;
                }
            }
            out.setHeldLotIDs(heldLotIDs);
            if (bOKFlag) {
                log.info("return LotIDs");
                out.setStrLotFamilyCurrentStatusList(strLotFamilyCurrentStatusList);
                out.setHoldFlag(true);
            } else if (bNonHoldFlag) {
                log.info("all lot of same operations has no hold record.");
                if (CimStringUtils.equals(BizConstant.SP_FPCINFO_UPDATE, actionType)) {
                    log.info("actionType is 'Update'. return OK.");
                    out.setStrLotFamilyCurrentStatusList(strLotFamilyCurrentStatusList);
                    out.setHoldFlag(false);
                } else {
                    throw new ServiceException(retCodeConfig.getFpcLotExistCurrentOpeWithNotOnhold());
                }
            } else {
                log.error("error case.");
                throw new ServiceException(retCodeConfig.getFpcLotExistCurrentOpeWithOnhold());
            }
        }
        return out;
    }

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/4/2                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/4/2 16:21
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Override
    public void fpcInfoDeleteDR(Infos.ObjCommon objCommon, List<String> fpcIDs) {
        //Initialize
        int fpcLen = CimArrayUtils.getSize(fpcIDs);
        log.info("fpcLen", fpcLen);
        for (int fpcCnt = 0; fpcCnt < fpcLen; fpcCnt++) {
            String fpcID = fpcIDs.get(fpcCnt);
            //Delete OSDOC
            CimFPCDO cimFPCExam = new CimFPCDO();
            cimFPCExam.setFpcID(fpcID);
            cimJpaRepository.delete(Example.of(cimFPCExam));

            //Delete OSDOC_WAFER
            CimFPCWaferDO cimFPCWaferExam = new CimFPCWaferDO();
            cimFPCWaferExam.setFpcID(fpcID);
            cimJpaRepository.delete(Example.of(cimFPCWaferExam));

            //Delete OSDOC_WAFER_PARAM
            CimFPCWaferParamterDO cimFPCWaferParamterExam = new CimFPCWaferParamterDO();
            cimFPCWaferParamterExam.setFpcID(fpcID);
            cimJpaRepository.delete(Example.of(cimFPCWaferParamterExam));

            //Delete OSDOC_RTCL
            CimFPCReticleDO cimFPCReticleExam = new CimFPCReticleDO();
            cimFPCReticleExam.setFpcID(fpcID);
            cimJpaRepository.delete(Example.of(cimFPCReticleExam));

            //Delete OSDOC_EDCSPEC
            CimFPCDataCollectionSpecificationDO specExam = new CimFPCDataCollectionSpecificationDO();
            specExam.setFpcID(fpcID);
            cimJpaRepository.delete(Example.of(specExam));

            //Delete OSDOC_COROPE
            CimFPCCoropeDO cimFPCCoropeExam = new CimFPCCoropeDO();
            cimFPCCoropeExam.setFpcID(fpcID);
            cimJpaRepository.delete(Example.of(cimFPCCoropeExam));

            //Delete RUNCARD_PSM_DOC for runCard -- added by Nyx
            runCardMethod.removeRunCardPsmDocFromDocJobID(fpcID);
        }
    }

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/6/3                             Zack               create file
     *
     * @author: Zack
     * @date: 2019/6/3 14:10
     * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    public List<Infos.FPCInfo> fpcListGetDR(Infos.ObjCommon objCommon, Inputs.ObjFPCListGetDRIn fpcListGetDRIn) {
        List<Infos.FPCInfo> out = new ArrayList<>();

        /*---------------------*/
        /*   Input Parameter   */
        /*---------------------*/
        List<String> fpcIDs = fpcListGetDRIn.getFpcIDs();
        ObjectIdentifier lotID = fpcListGetDRIn.getLotID();
        ObjectIdentifier lotFamilyID = fpcListGetDRIn.getLotFamilyID();
        ObjectIdentifier mainPDID = fpcListGetDRIn.getMainPDID();
        String mainOperNo = fpcListGetDRIn.getMainOperNo();
        ObjectIdentifier orgMainPDID = fpcListGetDRIn.getOrgMainPDID();
        String orgOperNo = fpcListGetDRIn.getOrgOperNo();
        ObjectIdentifier subMainPDID = fpcListGetDRIn.getSubMainPDID();
        String subOperNo = fpcListGetDRIn.getSubOperNo();
        ObjectIdentifier equipmentID = fpcListGetDRIn.getEquipmentID();
        Boolean waferIDInfoGetFlag = fpcListGetDRIn.getWaferIDInfoGetFlag();
        Boolean recipeParmInfoGetFlag = fpcListGetDRIn.getRecipeParmInfoGetFlag();
        Boolean reticleInfoGetFlag = fpcListGetDRIn.getReticleInfoGetFlag();
        Boolean dcSpecItemInfoGetFlag = fpcListGetDRIn.getDcSpecItemInfoGetFlag();

        /*----------------*/
        /*   Initialize   */
        /*----------------*/
        StringBuffer buffer = new StringBuffer("SELECT   DOC_ID," +
                "               LOTFAMILY_ID," +
                "               LOTFAMILY_RKEY," +
                "               PROCESS_ID," +
                "               PROCESS_RKEY," +
                "               OPE_NO," +
                "               ORIG_PROCESS_ID," +
                "               ORIG_PROCESS_RKEY," +
                "               ORIG_OPE_NO," +
                "               SUB_PROCESS_ID," +
                "               SUB_PROCESS_RKEY," +
                "               SUB_OPE_NO," +
                "               DOC_GROUP_NO," +
                "               DOC_TYPE," +
                "               MERGE_PROCESS_ID," +
                "               MERGE_PROCESS_RKEY," +
                "               MERGE_OPE_NO," +
                "               DOC_CATEGORY_ID," +
                "               STEP_ID," +
                "               STEP_RKEY," +
                "               STEP_TYPE," +
                "               CORR_OPE_NO," +
                "               SKIP_FLAG," +
                "               LIMIT_EQP_FLAG," +
                "               EQP_ID," +
                "               EQP_RKEY," +
                "               RPARAM_CHG_TYPE," +
                "               MRCP_ID," +
                "               MRCP_RKEY," +
                "               EDC_PLAN_ID," +
                "               EDC_PLAN_RKEY," +
                "               EDC_SPEC_ID," +
                "               EDC_SPEC_RKEY," +
                "               SEND_EMAIL_FLAG," +
                "               HOLD_LOT_FLAG," +
                "               DESCRIPTION," +
                "               TRX_USER_ID," +
                "               CREATE_TIME," +
                "               UPDATE_TIME " +
                "FROM           OSDOC WHERE");
        if (!CimObjectUtils.isEmpty(fpcIDs)) {
            log.info("FPC_IDs is specified. FPC_ID count is {}", fpcIDs.size());
            Boolean firstCondition = true;
            for (String fpcID : fpcIDs) {
                log.info("FPC_ID = {}", fpcID);
                if (firstCondition) {
                    firstCondition = false;
                    buffer.append(String.format(" DOC_ID = '%s'", fpcID));
                } else {
                    buffer.append(String.format(" OR DOC_ID = '%s'", fpcID));
                }
            }
        } else {
            log.info("FPC_IDs does not specified.");

            if ((!ObjectIdentifier.isEmpty(orgMainPDID) && CimObjectUtils.isEmpty(orgOperNo)) ||
                    (ObjectIdentifier.isEmpty(orgMainPDID) && !CimObjectUtils.isEmpty(orgOperNo))) {
                log.error("data combination error. {} {}", orgMainPDID, orgOperNo);
                throw new ServiceException(retCodeConfig.getFpcRouteInfoError());
            }

            if ((!ObjectIdentifier.isEmpty(subMainPDID) && CimObjectUtils.isEmpty(subOperNo)) ||
                    (ObjectIdentifier.isEmpty(subMainPDID) && !CimObjectUtils.isEmpty(subOperNo))) {
                log.error("data combination error. {} {}", subMainPDID, subOperNo);
                throw new ServiceException(retCodeConfig.getFpcRouteInfoError());
            }

            if (ObjectIdentifier.isEmpty(orgMainPDID) && !ObjectIdentifier.isEmpty(subMainPDID)) {
                log.error("data combination error. {} {}", orgMainPDID, subMainPDID);
                throw new ServiceException(retCodeConfig.getFpcRouteInfoError());
            }

            // Lot Family ID
            if (ObjectIdentifier.isEmpty(lotID) && ObjectIdentifier.isEmpty(lotFamilyID)) {
                log.info("Not specified lotID and lotFamilyID.");
                return out;
            }

            String tmpLotFamilyID = ObjectIdentifier.isEmpty(lotFamilyID) ? "" : lotFamilyID.getValue();
            if (!ObjectIdentifier.isEmpty(lotID)) {
                // Get Lot Object
                CimLot aLot = baseCoreFactory.getBO(CimLot.class, lotID);
                if (aLot == null) {
                    throw new ServiceException((retCodeConfig.getNotFoundLot()));
                }

                // Get Lot Family ID
                CimLotFamily aLotFamily = aLot.getLotFamily();
                if (aLotFamily == null) {
                    throw new ServiceException(retCodeConfig.getNotFoundLotFamily());
                }

                tmpLotFamilyID = aLotFamily.getIdentifier();
            }
            log.info("LotFamilyID of the specified lotID. {}", tmpLotFamilyID);

            if (!ObjectIdentifier.isEmpty(lotFamilyID)) {
                if (!ObjectIdentifier.equalsWithValue(lotFamilyID, tmpLotFamilyID)) {
                    log.error("The specified lot's familyID and the specified familyId are mismatch. {} {}", tmpLotFamilyID, lotFamilyID);
                    throw new ServiceException(retCodeConfig.getNotFoundLotFamily());
                }
            }

            log.info("LotFamily ID", tmpLotFamilyID);

            buffer.append(String.format(" LOTFAMILY_ID = '%s'", tmpLotFamilyID));

            // mainPD ID
            if (!ObjectIdentifier.isEmpty(mainPDID)) {
                buffer.append(String.format(" AND PROCESS_ID = '%s'", mainPDID));
            }

            // operation Number
            if (!CimObjectUtils.isEmpty(mainOperNo)) {
                buffer.append(String.format(" AND OPE_NO = '%s'", mainOperNo));
            }

            // original MainPD ID
            if (!ObjectIdentifier.isEmpty(orgMainPDID)) {
                buffer.append(String.format("AND ORIG_PROCESS_ID = '%s'", orgMainPDID));
            }

            // original operation Number
            if (!CimObjectUtils.isEmpty(orgOperNo)) {
                buffer.append(String.format(" AND ORIG_OPE_NO = '%s'", orgOperNo));
            }

            // sub MainPD ID
            if (!ObjectIdentifier.isEmpty(subMainPDID)) {
                buffer.append(String.format(" AND SUB_PROCESS_ID = '%s'", subMainPDID));
            }

            // sub opeartion Number
            if (!CimObjectUtils.isEmpty(subOperNo)) {
                buffer.append(String.format(" AND SUB_OPE_NO = '%s'", subOperNo));
            }

            // equipmentID
            if (!ObjectIdentifier.isEmpty(equipmentID)) {
                buffer.append(String.format(" AND (EQP_ID = '%s' OR EQP_ID = '')", equipmentID));
            }
        }
        //-------------------
        // SQL PREPARE
        //-------------------
        List<CimFPCDO> fpcs = cimJpaRepository.query(buffer.toString(), CimFPCDO.class);
        List<Infos.FPCInfo> fpcInfos = new ArrayList<>();
        if (!CimObjectUtils.isEmpty(fpcs)) {
            for (CimFPCDO fpc : fpcs) {
                Infos.FPCInfo fpcInfo = new Infos.FPCInfo();
                fpcInfos.add(fpcInfo);
                fpcInfo.setFpcID(fpc.getFpcID());
                fpcInfo.setLotFamilyID(new ObjectIdentifier(fpc.getLotFamilyID(), fpc.getLotFamilyObj()));
                fpcInfo.setMainProcessDefinitionID(new ObjectIdentifier(fpc.getMainProcessDefinitionID()));
                fpcInfo.setOperationNumber(fpc.getOperationNumber());
                fpcInfo.setOriginalMainProcessDefinitionID(new ObjectIdentifier(fpc.getOriginalMainProcessDefinitionID(), fpc.getOriginalMainProcessDefinitionObj()));
                fpcInfo.setOriginalOperationNumber(fpc.getOriginalOperationNumber());
                fpcInfo.setSubMainProcessDefinitionID(new ObjectIdentifier(fpc.getSubMainProcessDefinitionID(), fpc.getSubMainProcessDefinitionObj()));
                fpcInfo.setSubOperationNumber(fpc.getSubOperationNumber());
                fpcInfo.setFpcGroupNumber(fpc.getFpcGroupNumber());
                fpcInfo.setFpcType(fpc.getFpcType());
                fpcInfo.setMergeMainProcessDefinitionID(new ObjectIdentifier(fpc.getMergeMainProcessDefinitionID(), fpc.getMergeMainProcessDefinitionObj()));
                fpcInfo.setMergeOperationNumber(fpc.getMergeOperationNumber());
                fpcInfo.setFpcCategory(fpc.getFpcCategoryID());
                fpcInfo.setProcessDefinitionID(new ObjectIdentifier(fpc.getProcessDefinitionID(), fpc.getProcessDefinitionObj()));
                fpcInfo.setProcessDefinitionType(fpc.getProcessDefinitionType());
                fpcInfo.setSkipFalg(fpc.getSkipFlag());
                fpcInfo.setCorrespondOperationNumber(fpc.getCorrespondingOperationNumber());
                fpcInfo.setRestrictEquipmentFlag(fpc.getRestReticleEquipmentFlag());
                fpcInfo.setEquipmentID(new ObjectIdentifier(fpc.getEquipmentID(), fpc.getEquipmentObj()));
                fpcInfo.setMachineRecipeID(new ObjectIdentifier(fpc.getRecipeID(), fpc.getRecipeObj()));
                fpcInfo.setDcDefineID(new ObjectIdentifier(fpc.getDataCollectionDefID(), fpc.getDataCollectionDefObj()));
                fpcInfo.setDcSpecID(new ObjectIdentifier(fpc.getDataCollectionSpecificationID(), fpc.getDataCollectionSpecificationObj()));
                fpcInfo.setSendEmailFlag(fpc.getSendEmailFlag());
                fpcInfo.setHoldLotFlag(fpc.getHoldLotFlag());
                fpcInfo.setRecipeParameterChangeType(fpc.getParameterChangeType());
                fpcInfo.setDescription(fpc.getDescription());
                fpcInfo.setCreateTime(String.valueOf(fpc.getCreateTime()));
                fpcInfo.setUpdateTime(String.valueOf(fpc.getUpdateTime()));
                fpcInfo.setClaimUserID(fpc.getClaimUserID());

                if (waferIDInfoGetFlag || recipeParmInfoGetFlag) {
                    CimFPCWaferDO cimFPCWaferExam = new CimFPCWaferDO();
                    cimFPCWaferExam.setFpcID(fpc.getFpcID());
                    List<Infos.LotWaferInfo> lotWaferInfos = cimJpaRepository.findAll(Example.of(cimFPCWaferExam)).stream().map(fpcwafer -> {
                        Infos.LotWaferInfo lotWaferInfo = new Infos.LotWaferInfo();
                        lotWaferInfo.setWaferID(ObjectIdentifier.build(fpcwafer.getWaferID(), fpcwafer.getWaferObj()));

                        CimFPCWaferParamterDO childExam = new CimFPCWaferParamterDO();
                        childExam.setFpcID(fpc.getFpcID());
                        childExam.setWaferID(fpcwafer.getWaferID());
                        List<Infos.RecipeParameterInfo> rcpParamInfo = recipeParmInfoGetFlag ?
                                cimJpaRepository.findAll(Example.of(childExam)).stream()
                                        .sorted()
                                        .map(param -> {
                                            Infos.RecipeParameterInfo recipeParameterInfo = new Infos.RecipeParameterInfo();
                                            recipeParameterInfo.setSequenceNumber(param.getSequenceNumber());
                                            recipeParameterInfo.setParameterName(param.getParamterName());
                                            recipeParameterInfo.setParameterUnit(param.getParamterUnit());
                                            recipeParameterInfo.setParameterDataType(param.getParamterDataType());
                                            recipeParameterInfo.setParameterLowerLimit(param.getParamterLowerLimit());
                                            recipeParameterInfo.setParameterUpperLimit(param.getParamterUpperLimit());
                                            recipeParameterInfo.setUseCurrentSettingValueFlag(param.getParamterUseCurrentFlag());
                                            recipeParameterInfo.setParameterTargetValue(param.getParamterTargetValue());
                                            recipeParameterInfo.setParameterValue(param.getParamterValue());
                                            return recipeParameterInfo;
                                        }).collect(Collectors.toList()) :
                                Collections.emptyList();
                        lotWaferInfo.setRecipeParameterInfoList(rcpParamInfo);
                        return lotWaferInfo;
                    }).collect(Collectors.toList());
                    fpcInfo.setLotWaferInfoList(lotWaferInfos);
                }

                if (reticleInfoGetFlag) {
                    CimFPCReticleDO cimFPCReticleExam = new CimFPCReticleDO();
                    cimFPCReticleExam.setFpcID(fpc.getFpcID());
                    List<Infos.ReticleInfo> reticleInfos = cimJpaRepository.findAll(Example.of(cimFPCReticleExam)).stream().sorted()
                            .map(fpcReticle -> {
                                Infos.ReticleInfo reticleInfo = new Infos.ReticleInfo();
                                reticleInfo.setSequenceNumber(fpcReticle.getSequenceNumber());
                                reticleInfo.setReticleID(ObjectIdentifier.build(fpcReticle.getReticleID(), fpcReticle.getReticleObj()));
                                reticleInfo.setReticleGroup(ObjectIdentifier.build(fpcReticle.getReticleGroupID(), fpcReticle.getReticleGroupObj()));
                                return reticleInfo;
                            }).collect(Collectors.toList());
                    fpcInfo.setReticleInfoList(reticleInfos);
                }

                if (dcSpecItemInfoGetFlag) {
                    CimFPCDataCollectionSpecificationDO specExam = new CimFPCDataCollectionSpecificationDO();
                    specExam.setFpcID(fpc.getFpcID());
                    List<Infos.DCSpecDetailInfo> dcSpecs = cimJpaRepository.findAll(Example.of(specExam)).stream().map(fpcDcsecs -> {
                        Infos.DCSpecDetailInfo dcSpec = new Infos.DCSpecDetailInfo();
                        dcSpec.setDataItemName(fpcDcsecs.getDataCollectionItemName());
                        dcSpec.setScreenLimitUpperRequired(fpcDcsecs.getScreenUpperRequired());
                        dcSpec.setScreenLimitUpper(fpcDcsecs.getScreenUpperLimit());
                        dcSpec.setActionCodes_uscrn(fpcDcsecs.getScreenUpperActions());
                        dcSpec.setScreenLimitLowerRequired(fpcDcsecs.getScreenLowerRequired());
                        dcSpec.setScreenLimitLower(fpcDcsecs.getScreenLowerLimit());
                        dcSpec.setActionCodes_lscrn(fpcDcsecs.getScreenLowerActions());
                        dcSpec.setSpecLimitUpperRequired(fpcDcsecs.getSpecificationUpperRequired());
                        dcSpec.setSpecLimitUpper(fpcDcsecs.getSpecificationUpperLimit());
                        dcSpec.setActionCodes_usl(fpcDcsecs.getSpecificationUpperActions());
                        dcSpec.setSpecLimitLowerRequired(fpcDcsecs.getSpecificationLowerRequired());
                        dcSpec.setSpecLimitLower(fpcDcsecs.getSpecificationLowerLimit());
                        dcSpec.setActionCodes_lsl(fpcDcsecs.getSpecificationLowerActions());
                        dcSpec.setControlLimitUpperRequired(fpcDcsecs.getControlUpperRequired());
                        dcSpec.setControlLimitUpper(fpcDcsecs.getControlUpperLimit());
                        dcSpec.setActionCodes_ucl(fpcDcsecs.getControlUpperActions());
                        dcSpec.setControlLimitLowerRequired(fpcDcsecs.getControlLowerRequired());
                        dcSpec.setControlLimitLower(fpcDcsecs.getControlLowerLimit());
                        dcSpec.setActionCodes_lcl(fpcDcsecs.getControlLowerActions());
                        dcSpec.setTarget(fpcDcsecs.getDataCollectionItemTarget());
                        dcSpec.setTag(fpcDcsecs.getDataCollectionItemTag());
                        dcSpec.setDcSpecGroup(fpcDcsecs.getDataCollectionSpecificationGroup());
                        return dcSpec;
                    }).collect(Collectors.toList());
                    fpcInfo.setDcSpecList(dcSpecs);
                }

            }
        }
        out = fpcInfos;
        return out;
    }

    @Override
    public List<Infos.StartCassette> fpcStartCassetteInfoExchange(Infos.ObjCommon objCommon, String exchangeType, ObjectIdentifier equipmentID, List<Infos.StartCassette> strStartCassette) {
        List<Infos.StartCassette> out = new ArrayList<>();

        boolean whiteCheckRequired = true;
        boolean categoryCheckRequired = true;
        if (CimStringUtils.equals(exchangeType, BizConstant.SP_FPC_EXCHANGETYPE_STARTRESERVEINFO)
                || CimStringUtils.equals(exchangeType, BizConstant.SP_FPC_EXCHANGETYPE_OPECOMPREQ)) {
            log.info("{} white and category check is not required", exchangeType);
            whiteCheckRequired = false;
            categoryCheckRequired = false;
        }
        for (Infos.StartCassette startCassette : strStartCassette) {
            List<Infos.LotInCassette> lotInCassetteList = startCassette.getLotInCassetteList();
            if (!CimArrayUtils.isEmpty(lotInCassetteList)) {
                for (Infos.LotInCassette lotInCassette : lotInCassetteList) {
                    Infos.StartRecipe startRecipe = lotInCassette.getStartRecipe();
                    List<Infos.LotWafer> lotWaferList = lotInCassette.getLotWaferList();

                    if (CimBooleanUtils.isFalse(lotInCassette.getMoveInFlag())) {
                        continue;
                    }

                    ObjectIdentifier tmpLotID = lotInCassette.getLotID();

                    // Get effective FPC Infomation
                    Outputs.ObjLotEffectiveFPCInfoGetOut lotEffectiveFPCInfoGetOut = lotMethod.lotEffectiveFPCInfoGet(objCommon, exchangeType, equipmentID, tmpLotID);
                    Infos.FPCInfo lotEffFPCInfo = lotEffectiveFPCInfoGetOut.getFpcInfo();
                    boolean equipmentActionRequiredFlag = lotEffectiveFPCInfoGetOut.isEquipmentActionRequiredFlag();
                    boolean machineRecipeActionRequiredFlag = lotEffectiveFPCInfoGetOut.isMachineRecipeActionRequiredFlag();
                    boolean recipeParameterActionRequiredFlag = lotEffectiveFPCInfoGetOut.isRecipeParameterActionRequiredFlag();
                    boolean dcDefActionRequiredFlag = lotEffectiveFPCInfoGetOut.isDcDefActionRequiredFlag();
                    boolean dcSpecActionRequiredFlag = lotEffectiveFPCInfoGetOut.isDcSpecActionRequiredFlag();
                    boolean reticleActionRequiredFlag = lotEffectiveFPCInfoGetOut.isReticleActionRequiredFlag();
                    if (equipmentActionRequiredFlag
                            || machineRecipeActionRequiredFlag
                            || recipeParameterActionRequiredFlag
                            || dcDefActionRequiredFlag
                            || dcSpecActionRequiredFlag
                            || reticleActionRequiredFlag) {
                        log.info("{} Do startCassette Exchange for ", tmpLotID);
                        log.info("{} Now use FPCInfo which FPC_ID is ", lotEffFPCInfo.getFpcID());
                    } else {
                        log.info("{} FPC is not applied for this lot", tmpLotID);
                    }

                    //////// Equipment Exchange
                    if (equipmentActionRequiredFlag) {
                        log.info("Equipment Exchange does not support.");
                    }

                    //////// Machine Recipe Exchange
                    if (machineRecipeActionRequiredFlag) {
                        log.info("Exchange Machine Recipe.");

                        // Exchange Machine Recipe
                        ObjectIdentifier machineRecipeID = lotEffFPCInfo.getMachineRecipeID();
                        log.info("{} Change MachineRecipe to", machineRecipeID);
                        startRecipe.setMachineRecipeID(machineRecipeID);

                        CimMachineRecipe aMachineRecipe = baseCoreFactory.getBO(CimMachineRecipe.class, machineRecipeID);
                        Validations.check(null == aMachineRecipe, new OmCode(retCodeConfig.getNotFoundMachineRecipe(), ObjectIdentifier.fetchValue(machineRecipeID)));

                        // Exchange Physical Recipe along with MachineRecipe.
                        log.info("{} Get PhysicalRecipe from MachineRecipe", machineRecipeID);
                        startRecipe.setPhysicalRecipeID(aMachineRecipe.getPhysicalRecipeId());
                        log.info("{} PhysicalRecipeID", startRecipe.getPhysicalRecipeID());
                    }

                    //////// Recipe Parameter Exchange
                    log.info("Exchange Recipe Parameter.");
                    if (recipeParameterActionRequiredFlag) {
                        // for all lotwafers in startCassette.
                        for (Infos.LotWafer lotWafer : lotWaferList) {
                            ObjectIdentifier tmpWaferID = lotWafer.getWaferID();
                            List<Infos.LotWaferInfo> lotWaferInfos = lotEffFPCInfo.getLotWaferInfoList().stream().filter(x -> ObjectIdentifier.equalsWithValue(tmpWaferID, x.getWaferID())).collect(Collectors.toList());
                            if (CimObjectUtils.isEmpty(lotWaferInfos)) {
                                log.info("{} Wafer not found in FPCInfo (perhaps strange).", tmpWaferID);
                                throw new ServiceException(retCodeConfig.getFpcWaferMismatchInFpcGroup());
                            }
                            Infos.LotWaferInfo lotEffLotWaferInfo = lotWaferInfos.get(0);

                            //do exchange for each parameters.
                            int FPCParamCount = CimArrayUtils.getSize(lotEffLotWaferInfo.getRecipeParameterInfoList());
                            List<Infos.StartRecipeParameter> startRecipeParameterList = new ArrayList<>();
                            lotWafer.setStartRecipeParameterList(startRecipeParameterList);
                            for (int l = 0; l < FPCParamCount; l++) {
                                startRecipeParameterList.add(new Infos.StartRecipeParameter());
                                Infos.StartRecipeParameter startRecipeParameter = startRecipeParameterList.get(l);
                                Infos.RecipeParameterInfo lotEffectiveRecipeParameterInfo = lotEffLotWaferInfo.getRecipeParameterInfoList().get(l);

                                startRecipeParameter.setParameterName(lotEffectiveRecipeParameterInfo.getParameterName());
                                // if useCurrentValue == TRUE, then set null string for parameterValue.
                                if (lotEffectiveRecipeParameterInfo.isUseCurrentSettingValueFlag()) {
                                    startRecipeParameter.setParameterValue("");
                                } else {
                                    startRecipeParameter.setParameterValue(lotEffectiveRecipeParameterInfo.getParameterValue());
                                }

                                startRecipeParameter.setTargetValue(lotEffectiveRecipeParameterInfo.getParameterTargetValue());
                                startRecipeParameter.setUseCurrentSettingValueFlag(lotEffectiveRecipeParameterInfo.isUseCurrentSettingValueFlag());

                            }
                        }
                        lotInCassette.setRecipeParameterChangeType(lotEffFPCInfo.getRecipeParameterChangeType());
                    }

                    List<Infos.DataCollectionInfo> dcDefList = startRecipe.getDcDefList();
                    //////// DC Definition Exchange
                    if (dcDefActionRequiredFlag) {
                        log.info("Exchange DCDef");
                        startRecipe.setDataCollectionFlag(true);

                        ObjectIdentifier tmpDCDefID = lotEffFPCInfo.getDcDefineID();
                        log.info("{} FPC DCDefID ", tmpDCDefID);

                        ObjectIdentifier tmpDCSpecID = lotEffFPCInfo.getDcSpecID();
                        log.info("{} FPC DCSpecID", tmpDCSpecID);

                        // strDCDef[0] in startCassette is the RAW DCDef, so change by FPCInfo only strDCDef[0]
                        // (strDCDef[1], [2]... is Delta DCDef.)
                        ObjectIdentifier defaultDCDefID = null, defaultDCSpecID = null;
                        if (CimObjectUtils.isEmpty(dcDefList)) {
                            dcDefList.add(new Infos.DataCollectionInfo());
                        } else {
                            defaultDCDefID = dcDefList.get(0).getDataCollectionDefinitionID();
                            defaultDCSpecID = dcDefList.get(0).getDataCollectionSpecificationID();
                        }

                        log.info("{} default DCDefID ", defaultDCDefID);
                        log.info("{} default DCSpecID", defaultDCSpecID);

                        if (ObjectIdentifier.isEmpty(tmpDCSpecID)) {
                            log.info("{} DCSpecID not changed by FPC. use Default", defaultDCSpecID);
                            tmpDCSpecID = defaultDCSpecID;
                        }

                        if ((!ObjectIdentifier.equalsWithValue(tmpDCDefID, defaultDCDefID))
                                || (!ObjectIdentifier.equalsWithValue(tmpDCSpecID, defaultDCSpecID))) {
                            log.info("DCDef and (or) DCSpec are changed by FPC.");

                            // Check DCDef-DCSpec relation.
                            // cf. process_dataCollectionDefinition_Get.cpp line 350-402
                            CimDataCollectionSpecification aDCSpec = null;
                            if (!ObjectIdentifier.isEmpty(tmpDCSpecID)) {
                                log.info("{} DCSpecID specified. check DCDef-DCSpec Rel.", tmpDCSpecID);
                                aDCSpec = baseCoreFactory.getBO(CimDataCollectionSpecification.class, tmpDCSpecID);
                                Validations.check(null == aDCSpec, retCodeConfig.getNotFoundDcspec());
                                List<com.fa.cim.newcore.bo.dc.CimDataCollectionDefinition> usedDCDefListVar = aDCSpec.allDataCollectionDefinitions();

                                int usedDCDefCount = CimArrayUtils.getSize(usedDCDefListVar);
                                log.info("{} DCDef count used by FPC DCSpec", usedDCDefCount);

                                boolean foundDCDef = false;
                                String anUsedDCDefIDStr;
                                for (int i = 0; i < usedDCDefCount; i++) {
                                    com.fa.cim.newcore.bo.dc.CimDataCollectionDefinition cimDataCollectionDefDO = usedDCDefListVar.get(i);
                                    if (null == cimDataCollectionDefDO) {
                                        continue;
                                    }

                                    anUsedDCDefIDStr = cimDataCollectionDefDO.getIdentifier();
                                    log.info("{}   used DCDef:", anUsedDCDefIDStr);
                                    if (ObjectIdentifier.equalsWithValue(tmpDCDefID, anUsedDCDefIDStr)) {
                                        log.info("  found in used DCDef.");
                                        foundDCDef = true;
                                        break;
                                    }
                                }

                                if (!foundDCDef) {
                                    String dcRelChkFlag = StandardProperties.OM_EDC_PLAN_SPEC_RELATION_CHECK.getValue();
                                    if (CimStringUtils.equals(dcRelChkFlag, "1")) {
                                        log.info("OM_EDC_PLAN_SPEC_RELATION_CHECK == \"1\"");
                                        throw new ServiceException(retCodeConfig.getDcdefDcspecMismatch());
                                    } else {
                                        log.info("{} OM_EDC_PLAN_SPEC_RELATION_CHECK != \"1\"", dcRelChkFlag);
                                        // reset aDCSpec not to use.
                                        aDCSpec = null;
                                        tmpDCSpecID = null;
                                    }
                                }
                            } else {
                                log.info("DCSpecID is not specified.");
                                aDCSpec = null;
                            }

                            CimDataCollectionDefinition aDCDef = baseCoreFactory.getBO(CimDataCollectionDefinition.class, tmpDCDefID);
                            Validations.check(null == aDCDef, retCodeConfig.getNotFoundDcdef());
                            //change DCDef infomations.
                            // cf. process_dataCollectionDefinition_Get.cpp line 500-625
                            Infos.DataCollectionInfo dataCollectionInfo = dcDefList.get(0);
                            dataCollectionInfo.setDataCollectionDefinitionID(tmpDCDefID);
                            dataCollectionInfo.setDescription(aDCDef.getDescription());
                            dataCollectionInfo.setDataCollectionType(aDCDef.getCollectionType());
                            if (null == aDCSpec) {
                                log.info("Spec check Required is false");
                                dataCollectionInfo.setSpecCheckRequiredFlag(false);
                            } else {
                                log.info("Spec check Required is TRUE");
                                dataCollectionInfo.setSpecCheckRequiredFlag(true);
                                dataCollectionInfo.setDataCollectionSpecificationID(tmpDCSpecID);
                            }

                            List<EDCDTO.DCItemDefinition> dcItemListPtr = aDCDef.getDCItems();
                            int itemCount = CimArrayUtils.getSize(dcItemListPtr);
                            log.info("{} DCDef Item Count", itemCount);
                            boolean derivedItemFound = false;
                            List<Infos.DataCollectionItemInfo> dataCollectionItemInfos = new ArrayList<>();
                            dataCollectionInfo.setDcItems(dataCollectionItemInfos);
                            for (int k = 0; k < itemCount; k++) {
                                log.info("{}   set DCItem", k);
                                EDCDTO.DCItemDefinition dcItemDefinition = dcItemListPtr.get(k);
                                ObjectIdentifier dummyID = null;
                                Infos.DataCollectionItemInfo dataCollectionItemInfo = new Infos.DataCollectionItemInfo();
                                dataCollectionItemInfo.setDataCollectionItemName(dcItemDefinition.getDataItemName());
                                dataCollectionItemInfo.setDataCollectionMode(dcItemDefinition.getDataCollectionMethod());
                                dataCollectionItemInfo.setDataCollectionUnit(dcItemDefinition.getUnitOfMeasure());
                                dataCollectionItemInfo.setDataType(dcItemDefinition.getValType());
                                dataCollectionItemInfo.setItemType(dcItemDefinition.getItemType());
                                dataCollectionItemInfo.setMeasurementType(dcItemDefinition.getMeasType());
                                dataCollectionItemInfo.setWaferID(dummyID);
                                dataCollectionItemInfo.setWaferPosition(dcItemDefinition.getWaferPosition());
                                dataCollectionItemInfo.setSitePosition(dcItemDefinition.getSitePosition());
                                dataCollectionItemInfo.setHistoryRequiredFlag(dcItemDefinition.isStored());
                                dataCollectionItemInfo.setCalculationType(dcItemDefinition.getCalculationType());
                                dataCollectionItemInfo.setCalculationExpression(dcItemDefinition.getCalculationExpression());
                                dataCollectionItemInfo.setDataValue("");
                                dataCollectionItemInfo.setSpecCheckResult("");
                                dataCollectionItemInfos.add(dataCollectionItemInfo);

                                String itemType = dataCollectionItemInfo.getItemType();
                                if (CimStringUtils.equals(itemType, BizConstant.SP_DCDEF_ITEM_DERIVED)
                                        || CimStringUtils.equals(itemType, BizConstant.SP_DCDEF_ITEM_USERFUNCTION)) {
                                    log.info("{} Calculation Required ItemType Found", itemType);
                                    derivedItemFound = true;
                                }

                                if (CimObjectUtils.isEmpty(aDCSpec)) {
                                    log.info("DCSpec is nil. so the target value is not required.");
                                    dataCollectionItemInfo.setTargetValue("");
                                } else {
                                    log.info("{} DCSpec is not nil. get target value.", tmpDCSpecID);
                                    String dcItemName = dataCollectionItemInfo.getDataCollectionItemName();
                                    log.info("{} dcItemName", dcItemName);
                                    EDCDTO.DCItemSpecification aSpecItemVar = aDCSpec.findDCSpec(dcItemName);
                                    String dataItemName = CimObjectUtils.isEmpty(aSpecItemVar) ? "" : aSpecItemVar.getDataItemName();
                                    if (CimObjectUtils.isEmpty(dataItemName)) {
                                        log.info("dataItemName is nil.");
                                        dataCollectionItemInfo.setTargetValue("");
                                    } else {
                                        String dbTarget;
                                        log.info("{} dataItemName is not nil.", dataItemName);
                                        Double theTarget = aSpecItemVar.getTarget();
                                        if (-1.0 < theTarget && theTarget < 1.0) {
                                            dbTarget = String.format("%f", theTarget);
                                        } else if (-100000000000.0 < theTarget && theTarget < 1000000000000.0) {
                                            dbTarget = String.format("%f", theTarget);
                                            if (dbTarget.length() > 12) {
                                                dbTarget.toCharArray()[12] = '\0';
                                            }
                                        } else {
                                            dbTarget = String.format("%f", theTarget);
                                        }
                                        log.info("{} formatted target", dbTarget);
                                        dataCollectionItemInfo.setTargetValue(dbTarget);
                                    }
                                }
                            }
                            dataCollectionInfo.setCalculationRequiredFlag(derivedItemFound);
                        } else {
                            log.info("DCDefID and DCSpecID are not changed by FPC. They are same as default.");
                        }
                    }

                    //////// DCSpec Exchange
                    if (dcSpecActionRequiredFlag) {
                        log.info("Exchange DCSpec");

                        // strDCDef[0] in startCassette is the RAW DCDef, so change by FPCInfo only strDCDef[0]
                        // (strDCDef[1], [2]... is Delta DCDef.)
                        if (CimObjectUtils.isEmpty(dcDefList)) {
                            dcDefList.add(new Infos.DataCollectionInfo());
                        }

                        int dcSpecCount = CimArrayUtils.getSize(lotEffFPCInfo.getDcSpecList());
                        log.info("{} Now exchange DCSpec. count", dcSpecCount);
                        List<Infos.DataCollectionSpecInfo> dcInfos = new ArrayList<>();
                        List<Infos.DCSpecDetailInfo> dcSpecList = lotEffFPCInfo.getDcSpecList();
                        dcSpecList.forEach(x -> {
                            Infos.DataCollectionSpecInfo dcInfo = new Infos.DataCollectionSpecInfo();
                            dcInfos.add(dcInfo);
                            dcInfo.setDataItemName(x.getDataItemName());
                            dcInfo.setScreenLimitUpperRequired(x.getScreenLimitUpperRequired());
                            dcInfo.setScreenLimitUpper(x.getScreenLimitUpper());
                            dcInfo.setActionCodesUscrn(x.getActionCodes_uscrn());
                            dcInfo.setScreenLimitLowerRequired(x.getScreenLimitLowerRequired());
                            dcInfo.setScreenLimitLower(x.getScreenLimitLower());
                            dcInfo.setActionCodesLscrn(x.getActionCodes_lscrn());
                            dcInfo.setSpecLimitUpperRequired(x.getSpecLimitUpperRequired());
                            dcInfo.setSpecLimitUpper(x.getSpecLimitUpper());
                            dcInfo.setActionCodesUsl(x.getActionCodes_usl());
                            dcInfo.setSpecLimitLowerRequired(x.getSpecLimitLowerRequired());
                            dcInfo.setSpecLimitLower(x.getSpecLimitLower());
                            dcInfo.setActionCodesLsl(x.getActionCodes_lsl());
                            dcInfo.setControlLimitUpperRequired(x.getControlLimitUpperRequired());
                            dcInfo.setControlLimitUpper(x.getControlLimitUpper());
                            dcInfo.setActionCodesUcl(x.getActionCodes_ucl());
                            dcInfo.setControlLimitLowerRequired(x.getControlLimitLowerRequired());
                            dcInfo.setControlLimitLower(x.getControlLimitLower());
                            dcInfo.setActionCodesLcl(x.getActionCodes_lcl());
                            dcInfo.setTarget(x.getTarget());
                            dcInfo.setTag(x.getTag());
                            dcInfo.setDcSpecGroup(x.getDcSpecGroup());
                        });
                        dcDefList.get(0).setDcSpecs(dcInfos);
                    }

                    //////// Reticle Exchange
                    if (reticleActionRequiredFlag) {
                        log.info("Exchange Reticle");
                        int FPCReticleCount = CimArrayUtils.getSize(lotEffFPCInfo.getReticleInfoList());

                        log.info("{} Reticle Count", FPCReticleCount);
                        if (FPCReticleCount > 0) {
                            log.info("Exchange reticle info");
                            List<Infos.StartReticleInfo> startReticleInfos = new ArrayList<>();
                            startRecipe.setStartReticleList(startReticleInfos);
                            for (int k = 0; k < FPCReticleCount; k++) {
                                Infos.StartReticleInfo startReticleInfo = new Infos.StartReticleInfo();
                                startReticleInfo.setSequenceNumber(lotEffFPCInfo.getReticleInfoList().get(k).getSequenceNumber());
                                startReticleInfo.setReticleID(lotEffFPCInfo.getReticleInfoList().get(k).getReticleID());
                                startReticleInfos.add(startReticleInfo);
                            }
                        } else {
                            log.info("There is no FPC Reticle. not exchange.");
                        }
                    }
                }
            }
        }

        if (whiteCheckRequired || categoryCheckRequired) {
            log.info("{} required white check   ", (whiteCheckRequired ? "TRUE" : "false"));
            log.info("{} required category check", (categoryCheckRequired ? "TRUE" : "false"));

            List<Infos.FPCProcessCondition> FPCStartCassetteProcessConditionCheck = fpcStartCassetteProcessConditionCheck(objCommon, equipmentID, strStartCassette, categoryCheckRequired, whiteCheckRequired);

        }

        // set out structure.
        out = strStartCassette;
        return out;
    }

    @Override
    public List<Infos.FPCProcessCondition> fpcStartCassetteProcessConditionCheck(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, List<Infos.StartCassette> strStartCassette, Boolean FPCCategoryCheckFlag, Boolean whiteDefCheckFlag) {
        List<Infos.FPCProcessCondition> out = new ArrayList<>();


        // FPC ProcessCondition is  EQP/LogicalRecipe/MachineRecipe/PD(Operation)/Reticle/DCDef/DCSpec.
        // But, do not check whiteFlag/FPCCategory for LogicalRecipe and Reticle. (it is spec.)

        List<ObjectIdentifier> machineRecipeIDList = new ArrayList<>();
        int machineRecipeUsed = 0;

        List<ObjectIdentifier> processDefIDList = new ArrayList<>();
        int processDefUsed = 0;

        List<ObjectIdentifier> dcDefIDList = new ArrayList<>();
        int dcDefUsed = 0;

        List<ObjectIdentifier> dcSpecIDList = new ArrayList<>();
        int dcSpecUsed = 0;

        // collect from startCassette
        int casCount = CimArrayUtils.getSize(strStartCassette);
        log.info("{} cassette count", casCount);
        int i = 0;
        for (Infos.StartCassette startCassette : strStartCassette) {
            List<Infos.LotInCassette> lotInCassetteList = startCassette.getLotInCassetteList();
            if (!CimArrayUtils.isEmpty(lotInCassetteList)) {
                for (Infos.LotInCassette lotInCassette : lotInCassetteList) {
                    if (!CimObjectUtils.isEmpty(lotInCassette.getStartRecipe()) || !CimObjectUtils.isEmpty(lotInCassette.getStartOperationInfo())) {
                        //LogicalRecipe, MachineRecipe, PD is one by one for the Lot.
                        ObjectIdentifier tmpIDToAdd;
                        // Add MachineRecipe
                        Infos.StartRecipe startRecipe = lotInCassette.getStartRecipe();
                        tmpIDToAdd = startRecipe.getMachineRecipeID();
                        int sPos;
                        if (!ObjectIdentifier.isEmpty(tmpIDToAdd)) {
                            for (sPos = 0; sPos < machineRecipeUsed; sPos++) {
                                if (ObjectIdentifier.equalsWithValue(tmpIDToAdd, machineRecipeIDList.get(sPos))) {
                                    break;
                                }
                            }
                            if (sPos == machineRecipeUsed) {
                                log.info("{} Add MachineRecipeID", tmpIDToAdd);
                                machineRecipeIDList.add(machineRecipeUsed, tmpIDToAdd);
                                machineRecipeUsed++;
                            }
                        }

                        // Add ProcessDef (operationID)
                        Infos.StartOperationInfo startOperationInfo = lotInCassette.getStartOperationInfo();
                        if (null != startOperationInfo) {
                            tmpIDToAdd = startOperationInfo.getOperationID();
                        }
                        if (!ObjectIdentifier.isEmpty(tmpIDToAdd)) {
                            for (sPos = 0; sPos < processDefUsed; sPos++) {
                                if (ObjectIdentifier.equalsWithValue(tmpIDToAdd, processDefIDList.get(sPos))) {
                                    break;
                                }
                            }
                            if (sPos == processDefUsed) {
                                log.info("{} Add ProcessDefID", tmpIDToAdd);
                                processDefIDList.add(processDefUsed, tmpIDToAdd);
                                processDefUsed++;
                            }
                        }

                        List<Infos.DataCollectionInfo> dcDefList = startRecipe.getDcDefList();
                        int dcDefCount = CimArrayUtils.getSize(dcDefList);

                        int k = 0;
                        for (k = 0; k < dcDefCount; k++) {
                            // Add dcDef
                            Infos.DataCollectionInfo dcDef = dcDefList.get(k);
                            tmpIDToAdd = dcDef.getDataCollectionDefinitionID();
                            if (!ObjectIdentifier.isEmpty(tmpIDToAdd)) {
                                for (sPos = 0; sPos < dcDefUsed; sPos++) {
                                    if (ObjectIdentifier.equalsWithValue(tmpIDToAdd, dcDefIDList.get(sPos))) {
                                        break;
                                    }
                                }
                                if (sPos == dcDefUsed) {
                                    log.info("{} Add dcDefID", tmpIDToAdd);
                                    dcDefIDList.add(dcDefUsed, tmpIDToAdd);
                                    dcDefUsed++;
                                }
                            }

                            // Add dcSpec
                            tmpIDToAdd = dcDef.getDataCollectionSpecificationID();
                            if (!ObjectIdentifier.isEmpty(tmpIDToAdd)) {
                                for (sPos = 0; sPos < dcSpecUsed; sPos++) {
                                    if (ObjectIdentifier.equalsWithValue(tmpIDToAdd, dcSpecIDList.get(sPos))) {
                                        break;
                                    }
                                }
                                if (sPos == dcSpecUsed) {
                                    log.info("{} Add dcSpecID", tmpIDToAdd);
                                    dcSpecIDList.add(dcSpecUsed, tmpIDToAdd);
                                    dcSpecUsed++;
                                }
                            }
                        }
                    }
                }
            }

        }

        // set FPC ProcessConditionList
        List<Infos.FPCProcessCondition> strFPCProcessConditionList = new ArrayList<>();
        if (!ObjectIdentifier.isEmpty(equipmentID)) {
            strFPCProcessConditionList.add(new Infos.FPCProcessCondition(equipmentID, BizConstant.SP_FPC_PROCESSCONDITION_TYPE_EQUIPMENT));
            log.info("{} add checkList", equipmentID);
        }

        for (i = 0; i < machineRecipeUsed; i++) {
            strFPCProcessConditionList.add(new Infos.FPCProcessCondition(machineRecipeIDList.get(i), BizConstant.SP_FPC_PROCESSCONDITION_TYPE_MACHINERECIPE));
            log.info("{} add checkList", machineRecipeIDList.get(i));
        }

        for (i = 0; i < processDefUsed; i++) {
            strFPCProcessConditionList.add(new Infos.FPCProcessCondition(processDefIDList.get(i), BizConstant.SP_FPC_PROCESSCONDITION_TYPE_PD));
            log.info("{} add checkList", processDefIDList.get(i));
        }

        for (i = 0; i < dcDefUsed; i++) {
            strFPCProcessConditionList.add(new Infos.FPCProcessCondition(dcDefIDList.get(i), BizConstant.SP_FPC_PROCESSCONDITION_TYPE_DCDEF));
            log.info("{} add checkList", dcDefIDList.get(i));
        }

        for (i = 0; i < dcSpecUsed; i++) {
            strFPCProcessConditionList.add(new Infos.FPCProcessCondition(dcSpecIDList.get(i), BizConstant.SP_FPC_PROCESSCONDITION_TYPE_DCSPEC));
            log.info("{} add checkList", dcSpecIDList.get(i));
        }

        // FPC processCondition check.
        if (!CimObjectUtils.isEmpty(strFPCProcessConditionList)) {
            out = this.fpcProcessConditionCheck(objCommon, "", strFPCProcessConditionList, FPCCategoryCheckFlag, whiteDefCheckFlag);
        } else {
            log.info("There is no processCondition for check.");
        }
        return out;
    }

    @Override
    public Outputs.ObjProcessDataCollectionDefinitionGetOut fpcDCInfoExchangeByEDCSet(Infos.ObjCommon objCommon,
                                                     String exchangeType,
                                                     ObjectIdentifier equipmentID,
                                                     ObjectIdentifier lotID,
                                                     Outputs.ObjProcessDataCollectionDefinitionGetOut edcInformation) {
        Outputs.ObjProcessDataCollectionDefinitionGetOut retVel = new Outputs.ObjProcessDataCollectionDefinitionGetOut();
        List<Infos.DataCollectionInfo> actionList = new ArrayList<>();
        retVel.setDataCollectionFlag(edcInformation.isDataCollectionFlag());
        List<Infos.DataCollectionInfo> dcDefList = edcInformation.getDataCollectionDefList();
        //Get effective DOC Infomation
        Outputs.ObjLotEffectiveFPCInfoGetOut lotEffectiveFPCInfoGetOut = lotMethod.lotEffectiveFPCInfoGet(objCommon,
                exchangeType, equipmentID, lotID);
        Infos.FPCInfo lotEffFPCInfo = lotEffectiveFPCInfoGetOut.getFpcInfo();
        boolean dcDefActionRequiredFlag = lotEffectiveFPCInfoGetOut.isDcDefActionRequiredFlag();
        boolean dcSpecActionRequiredFlag = lotEffectiveFPCInfoGetOut.isDcSpecActionRequiredFlag();
        if (dcDefActionRequiredFlag || dcSpecActionRequiredFlag) {
            if (log.isDebugEnabled()){
                log.debug("Do startCassette Exchange edc and edcSpec for {}", ObjectIdentifier.fetchValue(lotID));
                log.debug("Now use DOCInfo which DOC_ID is {}", lotEffFPCInfo.getFpcID());
            }
        } else {
            if (log.isDebugEnabled()){
                log.debug("DOC edc and edcSpec is not applied for this lot {}", ObjectIdentifier.fetchValue(lotID));
            }
        }

        //DC Definition Exchange
        if (dcDefActionRequiredFlag) {
            if (log.isDebugEnabled()){
                log.debug("Exchange DCDef");
            }
            retVel.setDataCollectionFlag(true);

            ObjectIdentifier tmpDCDefID = lotEffFPCInfo.getDcDefineID();
            ObjectIdentifier tmpDCSpecID = lotEffFPCInfo.getDcSpecID();
            if (log.isDebugEnabled()){
                log.debug("DOC DCDefID {}", ObjectIdentifier.fetchValue(tmpDCDefID));
                log.debug("DOC DCSpecID {}", ObjectIdentifier.fetchValue(tmpDCSpecID));
            }

            // strDCDef[0] in startCassette is the RAW DCDef, so change by FPCInfo only strDCDef[0]
            // (strDCDef[1], [2]... is Delta DCDef.)
            ObjectIdentifier defaultDCDefID = null, defaultDCSpecID = null;
            if (CimObjectUtils.isEmpty(dcDefList)) {
                dcDefList.add(new Infos.DataCollectionInfo());
            } else {
                defaultDCDefID = dcDefList.get(0).getDataCollectionDefinitionID();
                defaultDCSpecID = dcDefList.get(0).getDataCollectionSpecificationID();
            }

            if (log.isDebugEnabled()){
                log.debug("default DCDefID {} ", defaultDCDefID);
                log.debug("default DCSpecID {} ", defaultDCSpecID);
            }

            if (ObjectIdentifier.isEmpty(tmpDCSpecID)) {
                if (log.isDebugEnabled()){
                    log.debug("DCSpecID not changed by DOC. use Default {}",
                            ObjectIdentifier.fetchValue(defaultDCSpecID));
                }
                tmpDCSpecID = defaultDCSpecID;
            }

            if ((!ObjectIdentifier.equalsWithValue(tmpDCDefID, defaultDCDefID))
                    || (!ObjectIdentifier.equalsWithValue(tmpDCSpecID, defaultDCSpecID))) {
                if (log.isDebugEnabled()){
                    log.debug("DCDef and (or) DCSpec are changed by DOC.");
                }

                // Check DCDef-DCSpec relation.
                // cf. process_dataCollectionDefinition_Get.cpp line 350-402
                CimDataCollectionSpecification aDCSpec = null;
                if (!ObjectIdentifier.isEmpty(tmpDCSpecID)) {
                    if (log.isDebugEnabled()){
                        log.debug("DCSpecID {} specified. check DCDef-DCSpec Rel.",
                                ObjectIdentifier.fetchValue(tmpDCSpecID));
                    }
                    aDCSpec = baseCoreFactory.getBO(CimDataCollectionSpecification.class, tmpDCSpecID);
                    Validations.check(null == aDCSpec, retCodeConfig.getNotFoundDcspec());
                    List<CimDataCollectionDefinition> usedDCDefListVar =
                            aDCSpec.allDataCollectionDefinitions();

                    int usedDCDefCount = CimArrayUtils.getSize(usedDCDefListVar);
                    log.info("DCDef count : {} used by FPC DCSpec", usedDCDefCount);

                    boolean foundDCDef = usedDCDefListVar.stream().filter(Objects::nonNull)
                            .anyMatch(data -> ObjectIdentifier.equalsWithValue(data.getIdentifier(), tmpDCDefID));

                    if (!foundDCDef) {
                        String dcRelChkFlag = StandardProperties.OM_EDC_PLAN_SPEC_RELATION_CHECK.getValue();
                        if (CimStringUtils.equals(dcRelChkFlag, BizConstant.VALUE_ONE)) {
                            log.error("OM_EDC_PLAN_SPEC_RELATION_CHECK == '1'");
                            Validations.check(retCodeConfig.getDcdefDcspecMismatch(),
                                    ObjectIdentifier.fetchValue(tmpDCDefID),
                                    ObjectIdentifier.fetchValue(tmpDCSpecID),
                                    ObjectIdentifier.fetchValue(lotID));
                        } else {
                            if (log.isDebugEnabled()){
                                log.debug("OM_EDC_PLAN_SPEC_RELATION_CHECK != '1'");
                            }
                            // reset aDCSpec not to use.
                            aDCSpec = null;
                            tmpDCSpecID = null;
                        }
                    }
                } else {
                    if (log.isDebugEnabled()){
                        log.debug("DCSpecID is not specified.");
                    }
                    aDCSpec = null;
                }
                CimDataCollectionDefinition aDCDef = baseCoreFactory.getBO(CimDataCollectionDefinition.class, tmpDCDefID);
                Validations.check(null == aDCDef, retCodeConfig.getNotFoundDcdef());
                //change DCDef infomations.
                // cf. process_dataCollectionDefinition_Get.cpp line 500-625
                Infos.DataCollectionInfo dataCollectionInfo = dcDefList.get(0);
                dataCollectionInfo.setDataCollectionDefinitionID(tmpDCDefID);
                dataCollectionInfo.setDescription(aDCDef.getDescription());
                dataCollectionInfo.setDataCollectionType(aDCDef.getCollectionType());
                if (null == aDCSpec) {
                    if (log.isDebugEnabled()){
                        log.debug("Spec check Required is false");
                    }
                    dataCollectionInfo.setSpecCheckRequiredFlag(false);
                } else {
                    if (log.isDebugEnabled()){
                        log.debug("Spec check Required is TRUE");
                    }
                    dataCollectionInfo.setSpecCheckRequiredFlag(true);
                    dataCollectionInfo.setDataCollectionSpecificationID(tmpDCSpecID);
                }

                List<EDCDTO.DCItemDefinition> dcItemListPtr = aDCDef.getDCItems();
                boolean derivedItemFound = false;
                List<Infos.DataCollectionItemInfo> dataCollectionItemInfos = new ArrayList<>();
                dataCollectionInfo.setDcItems(dataCollectionItemInfos);
                if (CimArrayUtils.isNotEmpty(dcItemListPtr)){
                    for (EDCDTO.DCItemDefinition dcItemDefinition : dcItemListPtr) {
                        ObjectIdentifier dummyID = null;
                        Infos.DataCollectionItemInfo dataCollectionItemInfo = new Infos.DataCollectionItemInfo();
                        dataCollectionItemInfo.setDataCollectionItemName(dcItemDefinition.getDataItemName());
                        dataCollectionItemInfo.setDataCollectionMode(dcItemDefinition.getDataCollectionMethod());
                        dataCollectionItemInfo.setDataCollectionUnit(dcItemDefinition.getUnitOfMeasure());
                        dataCollectionItemInfo.setDataType(dcItemDefinition.getValType());
                        dataCollectionItemInfo.setItemType(dcItemDefinition.getItemType());
                        dataCollectionItemInfo.setMeasurementType(dcItemDefinition.getMeasType());
                        dataCollectionItemInfo.setWaferID(dummyID);
                        dataCollectionItemInfo.setWaferPosition(dcItemDefinition.getWaferPosition());
                        dataCollectionItemInfo.setSitePosition(dcItemDefinition.getSitePosition());
                        dataCollectionItemInfo.setHistoryRequiredFlag(dcItemDefinition.isStored());
                        dataCollectionItemInfo.setCalculationType(dcItemDefinition.getCalculationType());
                        dataCollectionItemInfo.setCalculationExpression(dcItemDefinition.getCalculationExpression());
                        dataCollectionItemInfo.setDataValue(BizConstant.EMPTY);
                        dataCollectionItemInfo.setSpecCheckResult(BizConstant.EMPTY);
                        dataCollectionItemInfos.add(dataCollectionItemInfo);

                        String itemType = dataCollectionItemInfo.getItemType();
                        if (CimStringUtils.equalsIn(itemType, BizConstant.SP_DCDEF_ITEM_DERIVED,
                                BizConstant.SP_DCDEF_ITEM_USERFUNCTION)) {
                            if (log.isDebugEnabled()){
                                log.debug("Calculation Required ItemType Found: {} ", itemType);
                            }
                            derivedItemFound = true;
                        }

                        if (CimObjectUtils.isEmpty(aDCSpec)) {
                            if (log.isDebugEnabled()){
                                log.debug("DCSpec is nil. so the target value is not required.");
                            }
                            dataCollectionItemInfo.setTargetValue(BizConstant.EMPTY);
                        } else {
                            String dcItemName = dataCollectionItemInfo.getDataCollectionItemName();
                            if (log.isDebugEnabled()){
                                log.info("DCSpec: {} is not nil. get target value.", tmpDCSpecID);
                                log.info("dcItemName: {}", dcItemName);
                            }
                            EDCDTO.DCItemSpecification aSpecItemVar = aDCSpec.findDCSpec(dcItemName);
                            String dataItemName = CimObjectUtils.isEmpty(aSpecItemVar) ?
                                    BizConstant.EMPTY : aSpecItemVar.getDataItemName();
                            if (CimStringUtils.isEmpty(dataItemName)) {
                                if (log.isDebugEnabled()){
                                    log.debug("dataItemName is nil.");
                                }
                                dataCollectionItemInfo.setTargetValue(BizConstant.EMPTY);
                            } else {
                                String dbTarget;
                                if (log.isDebugEnabled()){
                                    log.debug("dataItemName: {} is not nil.", dataItemName);
                                }
                                Double theTarget = aSpecItemVar.getTarget();
                                if (-1.0 < theTarget && theTarget < 1.0) {
                                    dbTarget = String.format("%f", theTarget);
                                } else if (-100000000000.0 < theTarget && theTarget < 1000000000000.0) {
                                    dbTarget = String.format("%f", theTarget);
                                    if (dbTarget.length() > 12) {
                                        dbTarget.toCharArray()[12] = '\0';
                                    }
                                } else {
                                    dbTarget = String.format("%f", theTarget);
                                }
                                if (log.isDebugEnabled()){
                                    log.debug("formatted target: {}", dbTarget);
                                }
                                dataCollectionItemInfo.setTargetValue(dbTarget);
                            }
                        }
                    }
                }
                dataCollectionInfo.setCalculationRequiredFlag(derivedItemFound);
            } else {
                if (log.isDebugEnabled()){
                    log.debug("DCDefID and DCSpecID are not changed by DOC. They are same as default.");
                }
            }
        }

        //DCSpec Exchange
        if (dcSpecActionRequiredFlag) {
            if (log.isDebugEnabled()){
                log.debug("Exchange DCSpec");
            }
            // strDCDef[0] in startCassette is the RAW DCDef, so change by FPCInfo only strDCDef[0]
            // (strDCDef[1], [2]... is Delta DCDef.)
            if (CimObjectUtils.isEmpty(dcDefList)) {
                dcDefList.add(new Infos.DataCollectionInfo());
            }
            int dcSpecCount = CimArrayUtils.getSize(lotEffFPCInfo.getDcSpecList());
            if (log.isDebugEnabled()){
                log.debug("Now exchange DCSpec. count: {}", dcSpecCount);
            }
            List<Infos.DataCollectionSpecInfo> dcInfos = new ArrayList<>();
            List<Infos.DCSpecDetailInfo> dcSpecList = lotEffFPCInfo.getDcSpecList();
            dcSpecList.forEach(x -> {
                Infos.DataCollectionSpecInfo dcInfo = new Infos.DataCollectionSpecInfo();
                dcInfos.add(dcInfo);
                dcInfo.setDataItemName(x.getDataItemName());
                dcInfo.setScreenLimitUpperRequired(x.getScreenLimitUpperRequired());
                dcInfo.setScreenLimitUpper(x.getScreenLimitUpper());
                dcInfo.setActionCodesUscrn(x.getActionCodes_uscrn());
                dcInfo.setScreenLimitLowerRequired(x.getScreenLimitLowerRequired());
                dcInfo.setScreenLimitLower(x.getScreenLimitLower());
                dcInfo.setActionCodesLscrn(x.getActionCodes_lscrn());
                dcInfo.setSpecLimitUpperRequired(x.getSpecLimitUpperRequired());
                dcInfo.setSpecLimitUpper(x.getSpecLimitUpper());
                dcInfo.setActionCodesUsl(x.getActionCodes_usl());
                dcInfo.setSpecLimitLowerRequired(x.getSpecLimitLowerRequired());
                dcInfo.setSpecLimitLower(x.getSpecLimitLower());
                dcInfo.setActionCodesLsl(x.getActionCodes_lsl());
                dcInfo.setControlLimitUpperRequired(x.getControlLimitUpperRequired());
                dcInfo.setControlLimitUpper(x.getControlLimitUpper());
                dcInfo.setActionCodesUcl(x.getActionCodes_ucl());
                dcInfo.setControlLimitLowerRequired(x.getControlLimitLowerRequired());
                dcInfo.setControlLimitLower(x.getControlLimitLower());
                dcInfo.setActionCodesLcl(x.getActionCodes_lcl());
                dcInfo.setTarget(x.getTarget());
                dcInfo.setTag(x.getTag());
                dcInfo.setDcSpecGroup(x.getDcSpecGroup());
            });
            dcDefList.get(0).setDcSpecs(dcInfos);
        }

        //set out structure.
        actionList = dcDefList;
        retVel.setDataCollectionDefList(actionList);
        return retVel;
    }
}
