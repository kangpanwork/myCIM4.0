package com.fa.cim.method.impl;

import com.alibaba.fastjson.JSON;
import com.fa.cim.common.constant.BizConstant;
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
import com.fa.cim.entity.nonruntime.apc.CimApcRunCapaDO;
import com.fa.cim.entity.nonruntime.apc.CimApcRunCapaInstDO;
import com.fa.cim.entity.nonruntime.apc.CimApcRunCapaLotDO;
import com.fa.cim.entity.nonruntime.apc.CimIFApcSetupDO;
import com.fa.cim.entity.runtime.eqp.CimEquipmentDO;
import com.fa.cim.entity.runtime.restrict.CimRestrictionEntityDO;
import com.fa.cim.jpa.CimJpaRepository;
import com.fa.cim.method.IAPCMethod;
import com.fa.cim.method.IEquipmentMethod;
import com.fa.cim.method.IEventMethod;
import com.fa.cim.method.IProcessMethod;
import com.fa.cim.middleware.standard.core.exception.base.CimIntegrationException;
import com.fa.cim.newcore.bo.pd.CimProcessDefinition;
import com.fa.cim.newcore.bo.pd.CimProcessOperation;
import com.fa.cim.newcore.bo.prodspec.CimProductGroup;
import com.fa.cim.newcore.bo.prodspec.CimProductSpecification;
import com.fa.cim.newcore.bo.prodspec.CimTechnology;
import com.fa.cim.newcore.bo.product.CimLot;
import com.fa.cim.newcore.factory.BaseCoreFactory;
import com.fa.cim.newcore.impl.bo.env.StandardProperties;
import com.fa.cim.remote.IAPCRemoteManager;
import com.fa.cim.remote.INewAPCRemoteManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/10/14       ********             lightyh             create file
 *
 * @author lightyh
 * @since 2019/10/14 16:58
 * Copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Service
@Slf4j
public class APCMethod implements IAPCMethod {

    @Autowired
    private CimJpaRepository cimJpaRepository;

    @Autowired
    private RetCodeConfig retCodeConfig;

    @Autowired
    private RetCodeConfigEx retCodeConfigEx;

    @Autowired
    private IEventMethod eventMethod;

    @Autowired
    private BaseCoreFactory baseCoreFactory;

    @Autowired
    private IAPCRemoteManager apcRemoteManager;

    @Autowired
    private INewAPCRemoteManager newAPCRemoteManager;

    @Autowired
    private IProcessMethod processMethod;

    @Autowired
    private IEquipmentMethod equipmentMethod;

    @Override
    public boolean apcInterfaceFlagCheck(Infos.ObjCommon objCommon, List<Infos.StartCassette> strStartCassetteList) {
        /*------------------------------------------------------*/
        /*         Check environmentValue for APC I/F           */
        /*------------------------------------------------------*/
        String APCAvailable = StandardProperties.OM_APC_AVAILABLE.getValue();
        if (CimStringUtils.equals(APCAvailable, BizConstant.SP_APC_NOTAVAILABLE) || CimStringUtils.isEmpty(APCAvailable)){
            log.info("APC interface no use...");
            return false;
        }
        /*----------------------------------*/
        /*                                  */
        /*   Check to use APC I/F or not    */
        /*                                  */
        /*----------------------------------*/
        int cassetteLen = CimArrayUtils.getSize(strStartCassetteList);
        for (int i = 0; i < cassetteLen; i++){
            List<Infos.LotInCassette> lotInCassetteList = strStartCassetteList.get(i).getLotInCassetteList();
            int lotLen = CimArrayUtils.getSize(lotInCassetteList);
            for (int j = 0; j < lotLen; j++){
                List<Infos.LotWafer> lotWaferList = lotInCassetteList.get(j).getLotWaferList();
                int waferLen = CimArrayUtils.getSize(lotWaferList);
                for (int k = 0; k < waferLen; k++){
                    List<Infos.StartRecipeParameter> startRecipeParameterList = lotWaferList.get(k).getStartRecipeParameterList();
                    int recipeParamlen = CimArrayUtils.getSize(startRecipeParameterList);
                    /*------------------------------------*/
                    /*   check Recipe Param definition    */
                    /*------------------------------------*/
                    if (recipeParamlen != 0){
                        for (int l = 0; l < recipeParamlen; l++){
                            /*-------------------------------------------------*/
                            /*   check Recipe Param CurrentSettingValueFlag    */
                            /*-------------------------------------------------*/
                            if (CimBooleanUtils.isFalse(startRecipeParameterList.get(l).getUseCurrentSettingValueFlag())){
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @param strObjCommonIn
     * @param controlJobID
     * @param strAPCRunTimeCapabilityResponse
     * @return void
     * @exception
     * @author ho
     * @date 2019/12/30 11:20
     */
    @Override
    public void apcRuntimeCapabilityRegistDR( Infos.ObjCommon strObjCommonIn, ObjectIdentifier controlJobID, List<Infos.APCRunTimeCapabilityResponse>  strAPCRunTimeCapabilityResponse ) {
        int response_cnt = CimArrayUtils.getSize(strAPCRunTimeCapabilityResponse);
        for (int loop_response_cnt = 0; loop_response_cnt < response_cnt; loop_response_cnt++){
            List<Infos.APCRunTimeCapability> strAPCRunTimeCapability = strAPCRunTimeCapabilityResponse.get(loop_response_cnt).getStrAPCRunTimeCapability();
            int capability_cnt = CimArrayUtils.getSize(strAPCRunTimeCapability);
            for (int loop_capability_cnt = 0; loop_capability_cnt < capability_cnt; loop_capability_cnt++){
                List<Infos.APCLotWaferCollection> strAPCLotWaferCollection = strAPCRunTimeCapability.get(loop_capability_cnt).getStrAPCLotWaferCollection();
                int collection_len = CimArrayUtils.getSize(strAPCLotWaferCollection);
                if (collection_len == 0){
                    continue;
                }
                List<Infos.APCBaseAPCSystemFunction1> strAPCBaseAPCSystemFunction1List = strAPCRunTimeCapability.get(loop_capability_cnt).getStrAPCBaseAPCSystemFunction1();
                int function_cnt = CimArrayUtils.getSize(strAPCBaseAPCSystemFunction1List);
                for (int loop_function_cnt = 0; loop_function_cnt < function_cnt; loop_function_cnt++){
                    Infos.APCBaseAPCSystemFunction1 apcBaseAPCSystemFunction1 = strAPCBaseAPCSystemFunction1List.get(loop_function_cnt);
                    //--------------------------------------
                    // INSERT FSRUNCAPA
                    //--------------------------------------
                    CimApcRunCapaDO cimApcRunCapaDO = new CimApcRunCapaDO();
                    cimApcRunCapaDO.setControlJobID(ObjectIdentifier.fetchValue(controlJobID));
                    cimApcRunCapaDO.setApcSystemName(strAPCRunTimeCapabilityResponse.get(loop_response_cnt).getStrAPCBaseIdentification().getSystemName());
                    cimApcRunCapaDO.setCtrlFqequency(apcBaseAPCSystemFunction1.getControlFrequency());
                    cimApcRunCapaDO.setDescription(apcBaseAPCSystemFunction1.getDescription());
                    cimApcRunCapaDO.setClaimTime(strObjCommonIn.getTimeStamp().getReportTimeStamp());
                    cimApcRunCapaDO.setActionType(apcBaseAPCSystemFunction1.getType());
                    cimJpaRepository.save(cimApcRunCapaDO);
                }
                List<Infos.APCLotWaferCollection> strAPCLotWaferCollectionList = strAPCRunTimeCapability.get(loop_capability_cnt).getStrAPCLotWaferCollection();
                int collection_cnt = CimArrayUtils.getSize(strAPCLotWaferCollectionList);
                for (int loop_collection_cnt = 0; loop_collection_cnt < collection_cnt; loop_collection_cnt++){
                    Infos.APCLotWaferCollection apcLotWaferCollection = strAPCLotWaferCollectionList.get(loop_collection_cnt);
                    List<String> waferIDs = apcLotWaferCollection.getWaferID();
                    int wafer_cnt = CimArrayUtils.getSize(waferIDs);
                    for (int loop_wafer_cnt = 0; loop_wafer_cnt < wafer_cnt; loop_wafer_cnt++){
                        CimApcRunCapaLotDO cimApcRunCapaLotDO = new CimApcRunCapaLotDO();
                        cimApcRunCapaLotDO.setControlJobID(ObjectIdentifier.fetchValue(controlJobID));
                        cimApcRunCapaLotDO.setApcSystemName(strAPCRunTimeCapabilityResponse.get(loop_response_cnt).getStrAPCBaseIdentification().getSystemName());
                        cimApcRunCapaLotDO.setLotID(apcLotWaferCollection.getLotID());
                        cimApcRunCapaLotDO.setWaferID(waferIDs.get(loop_wafer_cnt));
                        cimApcRunCapaLotDO.setClaimTime(strObjCommonIn.getTimeStamp().getReportTimeStamp());
                        cimJpaRepository.save(cimApcRunCapaLotDO);
                    }
                }
                List<Infos.APCSpecialInstruction> strAPCSpecialInstruction = strAPCRunTimeCapability.get(loop_capability_cnt).getStrAPCSpecialInstruction();
                int instruction_cnt = CimArrayUtils.getSize(strAPCSpecialInstruction);
                for (int loop_instruction_cnt = 0; loop_instruction_cnt < instruction_cnt; loop_instruction_cnt++){
                    Infos.APCSpecialInstruction apcSpecialInstruction = strAPCSpecialInstruction.get(loop_instruction_cnt);
                    List<Infos.APCBaseFactoryEntity> strAPCBaseFactoryEntity = apcSpecialInstruction.getStrAPCBaseFactoryEntity();
                    int entity_cnt = CimArrayUtils.getSize(strAPCBaseFactoryEntity);
                    for (int loop_entity_cnt = 0; loop_entity_cnt < entity_cnt; loop_entity_cnt++){
                        Infos.APCBaseFactoryEntity apcBaseFactoryEntity = strAPCBaseFactoryEntity.get(loop_entity_cnt);
                        CimApcRunCapaInstDO cimApcRunCapaInstDO = new CimApcRunCapaInstDO();
                        cimApcRunCapaInstDO.setControlJobID(ObjectIdentifier.fetchValue(controlJobID));
                        cimApcRunCapaInstDO.setApcSystemName(strAPCRunTimeCapabilityResponse.get(loop_response_cnt).getStrAPCBaseIdentification().getSystemName());
                        cimApcRunCapaInstDO.setInstructionID(apcSpecialInstruction.getInstructionID());
                        cimApcRunCapaInstDO.setClassID(apcBaseFactoryEntity.getClassName());
                        cimApcRunCapaInstDO.setValue(apcBaseFactoryEntity.getId());
                        cimApcRunCapaInstDO.setAttribute(apcBaseFactoryEntity.getAttrib());
                        cimApcRunCapaInstDO.setClaimTime(strObjCommonIn.getTimeStamp().getReportTimeStamp());
                        cimJpaRepository.save(cimApcRunCapaInstDO);
                    }
                }
            }
        }
    }

    @Override
    public void apcRuntimeCapabilityDeleteDR(Infos.ObjCommon objCommon, ObjectIdentifier controlJobID){

        CimApcRunCapaDO cimApcRunCapaExam = new CimApcRunCapaDO();
        cimApcRunCapaExam.setControlJobID(ObjectIdentifier.fetchValue(controlJobID));
        cimJpaRepository.delete(Example.of(cimApcRunCapaExam));

        //----------------------------
        // DELETE  FSRUNCAPA_LOT
        //----------------------------
        CimApcRunCapaLotDO cimApcRunCapaLotExam = new CimApcRunCapaLotDO();
        cimApcRunCapaLotExam.setControlJobID(ObjectIdentifier.fetchValue(controlJobID));
        cimJpaRepository.delete(Example.of(cimApcRunCapaLotExam));

        //----------------------------
        // DELETE  FSRUNCAPA_INST
        //----------------------------
        CimApcRunCapaInstDO cimApcRunCapaInstExam = new CimApcRunCapaInstDO();
        cimApcRunCapaInstExam.setControlJobID(ObjectIdentifier.fetchValue(controlJobID));
        cimJpaRepository.delete(Example.of(cimApcRunCapaInstExam));
    }

    @Override
    public List<Infos.APCRecipeParameterResponse> APCMgrSendRecipeParameterRequest(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, List<Infos.ApcBaseCassette> strAPCBaseCassetteList,
                                                                                   List<Infos.APCRunTimeCapabilityResponse> strAPCRunTimeCapabilityResponse, boolean finalBoolean) {
        int systemname_cnt = 0;
        List<String> systemNameList = new ArrayList<>();
        int response_cnt = CimArrayUtils.getSize(strAPCRunTimeCapabilityResponse);
        for (int loop_response_cnt = 0; loop_response_cnt < response_cnt; loop_response_cnt++){
            Infos.APCRunTimeCapabilityResponse apcRunTimeCapabilityResponse = strAPCRunTimeCapabilityResponse.get(loop_response_cnt);
            List<Infos.APCRunTimeCapability> strAPCRunTimeCapability = apcRunTimeCapabilityResponse.getStrAPCRunTimeCapability();
            int capability_cnt = CimArrayUtils.getSize(strAPCRunTimeCapability);
            for (int loop_capability_cnt = 0; loop_capability_cnt < capability_cnt; loop_capability_cnt++){
                Infos.APCRunTimeCapability apcRunTimeCapability = strAPCRunTimeCapability.get(loop_capability_cnt);
                List<Infos.APCBaseAPCSystemFunction1> strAPCBaseAPCSystemFunction1 = apcRunTimeCapability.getStrAPCBaseAPCSystemFunction1();
                int function_cnt = CimArrayUtils.getSize(strAPCBaseAPCSystemFunction1);
                for (int loop_function_cnt = 0; loop_function_cnt < function_cnt; loop_function_cnt++ ){
                    if (CimStringUtils.equals(strAPCBaseAPCSystemFunction1.get(loop_function_cnt).getType(), BizConstant.SP_APCFUNCTIONTYPE_RECIPEPARAMETERADJUST)){
                        int tmp_cnt = 0;
                        boolean systemNameFlag = false;
                        for (tmp_cnt = 0; tmp_cnt < systemname_cnt; tmp_cnt++){
                            if (CimStringUtils.equals(systemNameList.get(tmp_cnt), apcRunTimeCapabilityResponse.getStrAPCBaseIdentification().getSystemName())){
                                systemNameFlag = true;
                                break;
                            }
                        }
                        if (!systemNameFlag){
                            String systemName = apcRunTimeCapabilityResponse.getStrAPCBaseIdentification().getSystemName();
                            if (!CimStringUtils.isEmpty(systemName)){
                                systemNameList.add(systemName);
                                systemname_cnt++;
                            }
                        }
                    }
                }
            }
        }
        if (systemname_cnt > 0){
            String tmpFinal = "";
            if (finalBoolean){
                tmpFinal = BizConstant.SP_RECIPEPARAMETERREQUEST_FINAL_TRUE;
            } else {
                tmpFinal = BizConstant.SP_RECIPEPARAMETERREQUEST_FINAL_FALSE;
            }
            /*---------------------------------*/
            /*   Get Server Name               */
            /*---------------------------------*/
            String tmpAPCServerName = StandardProperties.OM_APC_SERVER.getValue();
            String APCServerName = null;
            if (!CimStringUtils.isEmpty(tmpAPCServerName) && !tmpAPCServerName.contains(":")){
                APCServerName = ":" + tmpAPCServerName;
            } else {
                APCServerName = tmpAPCServerName;
            }
            /*---------------------------------*/
            /*   Get Host Name                 */
            /*---------------------------------*/
            String APCHostName = StandardProperties.OM_APC_HOST.getValue();
            /*-------------------------*/
            /*   Send Request to APC   */
            /*-------------------------*/
            int cassette_cnt = CimArrayUtils.getSize(strAPCBaseCassetteList);
            for (int loop_cassette_cnt = 0 ; loop_cassette_cnt < cassette_cnt ; loop_cassette_cnt++){
                List<Infos.ApcBaseLot> apcBaseLotList = strAPCBaseCassetteList.get(loop_cassette_cnt).getApcBaseLotList();
                int lot_cnt = CimArrayUtils.getSize(apcBaseLotList);
            }
            User tmpRequestUer = this.getRequestUser(objCommon);
            // TxRecipeParameterRequest
            Inputs.RecipeParameterRequestIn recipeParameterRequestIn = new Inputs.RecipeParameterRequestIn();
            recipeParameterRequestIn.setRequestUser(tmpRequestUer);
            recipeParameterRequestIn.setEquipmentID(ObjectIdentifier.fetchValue(equipmentID));
            recipeParameterRequestIn.setSystemNameList(systemNameList);
            recipeParameterRequestIn.setFinalBoolean(tmpFinal);
            recipeParameterRequestIn.setSendName("RecipeParameterRequest");
            recipeParameterRequestIn.setStrAPCBaseCassetteList(strAPCBaseCassetteList);
            String result = this.sendToApcServer(recipeParameterRequestIn);
            List<Infos.APCRecipeParameterResponse> apcRecipeParameterResponseList = JSON.parseArray(result, Infos.APCRecipeParameterResponse.class);
            //-------------------------------------------------------------------------------------------------
            // APC return only 0 or 2. 0 is nomal end, 2 is error. This returnCode is strResult.returnCode.
            // But when strAPCBaseReturnCode.state is not "OK", TxRecipeParameterRequest is fail.
            // By the above reason, we check strAPCBaseReturnCode.state, though returnCode is 0.
            // If except "OK" is found, this function returns error as RC_APC_RECIPEPARAMETERREQ_ERROR.
            //-------------------------------------------------------------------------------------------------
            int recipepara_cnt = CimArrayUtils.getSize(apcRecipeParameterResponseList);
            //----------------------------------------------
            // Check strAPCBaseReturnCode.state
            //----------------------------------------------
            Boolean errorFlag = false;
            for (int loop_check_cnt = 0; loop_check_cnt < recipepara_cnt; loop_check_cnt++){
                if (!CimStringUtils.equals(apcRecipeParameterResponseList.get(loop_check_cnt).getStrAPCBaseReturnCode().getState(), BizConstant.SP_APCRETURNCODE_OK)) {
                    errorFlag = true;
                    break;
                }
            }
            Validations.check(errorFlag, retCodeConfig.getApcRecipeparameterreqError());
            return apcRecipeParameterResponseList;
        }
        return null;
    }

    @Override
    public List<Infos.StartCassette> APCMgrSendRecipeParamInq(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, String portGroupID, ObjectIdentifier controlJobID, List<Infos.StartCassette> strOrgStartCassette,String operation) {
        /*-----------------------------------------------------------------------------------------*/
        /*     Set startOperationInfo                                                              */
        /*-----------------------------------------------------------------------------------------*/
        int scLen = CimArrayUtils.getSize(strOrgStartCassette);
        /*-------------------------------*/
        /*   Loop for strStartCassette   */
        /*-------------------------------*/
        for (int i = 0; i < scLen; i++){
            Infos.StartCassette startCassette = strOrgStartCassette.get(i);
            List<Infos.LotInCassette> lotInCassetteList = startCassette.getLotInCassetteList();
            int lenLotInCassette = CimArrayUtils.getSize(lotInCassetteList);
            for (int j = 0; j < lenLotInCassette; j++){
                if (!lotInCassetteList.get(j).getMoveInFlag()){
                    continue;
                }
                /*----------------------------------------------------------------*/
                /*   Set StartOperationInfo                                       */
                /*----------------------------------------------------------------*/
                CimLot aLot = baseCoreFactory.getBO(CimLot.class, lotInCassetteList.get(j).getLotID());
                Validations.check(aLot == null, new OmCode(retCodeConfig.getNotFoundLot(), lotInCassetteList.get(j).getLotID().getValue()));
                CimProcessOperation aPO = aLot.getProcessOperation();

                CimProductSpecification productSpecification = aLot.getProductSpecification();
                Validations.check(null == productSpecification,retCodeConfig.getNotFoundProductSpec());

                CimProductGroup productGroup = productSpecification.getProductGroup();
                Validations.check(null == productGroup,retCodeConfig.getNotFoundProductGroup(),"******");

                CimTechnology technology = productGroup.getTechnology();
                Validations.check(null == technology,retCodeConfig.getNotFoundTechnology(),"******");

                ObjectIdentifier productGroupID = ObjectIdentifier.build(productGroup.getIdentifier(), productGroup.getPrimaryKey());
                ObjectIdentifier technologyID = ObjectIdentifier.build(technology.getIdentifier(), technology.getPrimaryKey());

                lotInCassetteList.get(j).setProductGroupID(productGroupID);
                lotInCassetteList.get(j).setTechnologyID(technologyID);

                if (aPO != null){
                    CimProcessDefinition aMainPD = aLot.getMainProcessDefinition();
                    Validations.check(aMainPD == null, new OmCode(retCodeConfig.getNotFoundRoute(), ""));
                    Infos.StartOperationInfo startOperationInfo = lotInCassetteList.get(j).getStartOperationInfo();
                    if (startOperationInfo == null){
                        startOperationInfo = new Infos.StartOperationInfo();
                        lotInCassetteList.get(j).setStartOperationInfo(startOperationInfo);
                    }
                    startOperationInfo.setProcessFlowID(new ObjectIdentifier(aMainPD.getIdentifier(), aMainPD.getPrimaryKey()));
                    /*---------------------------------------------------------------*/
                    /* Set     objectIdentifier            operationID;              */
                    /*---------------------------------------------------------------*/
                    CimProcessDefinition aPD = aPO.getProcessDefinition();
                    Validations.check(aPD == null, new OmCode(retCodeConfig.getNotFoundProcessDefinition(), ""));
                    startOperationInfo.setOperationID(new ObjectIdentifier(aPD.getIdentifier(), aPD.getPrimaryKey()));
                    /*---------------------------------------------------------------*/
                    /* Set     string                      operationNumber;          */
                    /*---------------------------------------------------------------*/
                    startOperationInfo.setOperationNumber(aLot.getOperationNumber());
                    startOperationInfo.setPassCount(aPO.getPassCount().intValue());

                    /*---------------------------------------------------------------*/
                    /* Set                   startRereticles to APC if exsit         */
                    /*---------------------------------------------------------------*/
                    if (CimArrayUtils.isEmpty(lotInCassetteList.get(j).getStartRecipe().getStartReticleList())){
                        try {
                            equipmentMethod.equipmentProcessDurableRequiredFlagGet(objCommon, equipmentID);
                        } catch (ServiceException e) {
                            if (Validations.isEquals(retCodeConfig.getEquipmentProcessDurableReticleRequired(), e.getCode())) {
                                if (!CimStringUtils.equals(startCassette.getLoadPurposeType(), BizConstant.SP_LOADPURPOSETYPE_EMPTYCASSETTE)
                                        && lotInCassetteList.get(j).getMoveInFlag()) {
                                    /*--------------------------------------------------*/
                                    /*   Check Process Durable Condition for OpeStart   */
                                    /*--------------------------------------------------*/
                                    Outputs.ObjProcessDurableCheckConditionForOperationStartOut objProcessDurableCheckConditionForOperationStartOut = processMethod.processDurableCheckConditionForOpeStart(objCommon,
                                            equipmentID,
                                            lotInCassetteList.get(j).getStartRecipe().getLogicalRecipeID(),
                                            lotInCassetteList.get(j).getStartRecipe().getMachineRecipeID(),
                                            lotInCassetteList.get(j).getLotID());
                                    /*------------------------------*/
                                    /*   Set Available Reticles     */
                                    /*------------------------------*/
                                    if (CimArrayUtils.isNotEmpty(objProcessDurableCheckConditionForOperationStartOut.getStartReticleList())){
                                        lotInCassetteList.get(j).getStartRecipe().setStartReticleList(objProcessDurableCheckConditionForOperationStartOut.getStartReticleList());
                                    }
                                }
                            } else if (Validations.isEquals(retCodeConfig.getEquipmentProcessDurableFixtRequired(), e.getCode())
                                    || Validations.isEquals(retCodeConfig.getEquipmentProcessDurableNotRequired(), e.getCode())) {
                                log.info("return == EQP_PROCDRBL_FIXT_REQD || return == EQP_PROCDRBL_NOT_REQD");
                            } else {
                                throw e;
                            }
                        }
                    }
                    /*---------------------------------------------------------------*/
                    /* Set                   maskLevel to APC if exsit               */
                    /*---------------------------------------------------------------*/
                    String maskLevel = aPO.getPhotoLayer();
                    if (CimStringUtils.isEmpty(lotInCassetteList.get(j).getStartOperationInfo().getMaskLevel())){
                        lotInCassetteList.get(j).getStartOperationInfo().setMaskLevel(maskLevel);
                    }
                }
            }
        }

        //qiandao project update apcReserveReq interface
        Infos.APCParamInfo apcParamInfo = new Infos.APCParamInfo();
        apcParamInfo.setPortGroupID(portGroupID);
        apcParamInfo.setEquipmentID(equipmentID);
        apcParamInfo.setControlJobID(controlJobID);
        apcParamInfo.setStartCassetteList(strOrgStartCassette);
        Object apcResult = null;
        try {
            if (CimStringUtils.equals(operation,BizConstant.SP_OPERATION_STARTRESERVATION)){
                apcResult  = newAPCRemoteManager.apcReserveReq(apcParamInfo);
            }
            if (CimStringUtils.equals(operation,BizConstant.SP_OPERATION_OPESTART)){
                apcResult = newAPCRemoteManager.apcParamInq(apcParamInfo);
            }
        } catch(CimIntegrationException e) {
            Validations.check(true,new OmCode((int) e.getCode(),e.getMessage()));
        }
        Infos.APCParamInfo apcReserveResult = null;
        if (null != apcResult) {
            apcReserveResult = JSON.parseObject(apcResult.toString(), Infos.APCParamInfo.class);
        }
        if (CimStringUtils.equals(operation,BizConstant.SP_OPERATION_STARTRESERVATION) && null != apcReserveResult){
            //clear maskLevel and photoLayer when moveInReserveInfoInq after apc I/F
            apcReserveResult.getStartCassetteList().parallelStream().map(Infos.StartCassette::getLotInCassetteList).forEach(lotInCassettes -> lotInCassettes.forEach(lotInCassette -> {
                if (CimArrayUtils.isNotEmpty(lotInCassette.getStartRecipe().getStartReticleList())){
                    lotInCassette.getStartRecipe().getStartReticleList().clear();
                    lotInCassette.getStartOperationInfo().setMaskLevel("");
                }
            }));
        }
        return apcReserveResult == null ? null : apcReserveResult.getStartCassetteList();
    }

    @Override
    public  List<Infos.APCRunTimeCapabilityResponse> APCMgrSendAPCRunTimeCapabilityRequestDR(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, List<Infos.ApcBaseCassette> strAPCBaseCassetteList) {
        String sql = "SELECT DISTINCT APC_SYSTEM_NAME\n" +
                "              FROM IFAPCSETUP\n" +
                "             WHERE EQP_ID = ?\n" +
                "               AND APC_CONFIG_STATE = ?\n" +
                "             ORDER BY APC_SYSTEM_NAME";
        List<CimIFApcSetupDO> cimApcIFList = cimJpaRepository.query(sql, CimIFApcSetupDO.class, ObjectIdentifier.fetchValue(equipmentID), BizConstant.SP_APC_CONFIG_STATE_APPROVED);
        List<String> systemName = new ArrayList<>();
        int systemName_cnt = 0;
        if (!CimArrayUtils.isEmpty(cimApcIFList)){
            for (CimIFApcSetupDO cimIFApcSetupDO : cimApcIFList){
                boolean systemNameFlag = false;
                for (int loop_systemName_cnt = 0; loop_systemName_cnt < systemName_cnt; loop_systemName_cnt++){
                    if (CimStringUtils.equals(cimIFApcSetupDO.getApcSystemName(), systemName.get(loop_systemName_cnt))){
                        systemNameFlag = true;
                        break;
                    }
                }
                if (!systemNameFlag){
                    systemName.add(cimIFApcSetupDO.getApcSystemName());
                    systemName_cnt++;
                }
            }
        }
        if (systemName_cnt > 0){
            /*---------------------------------------*/
            /*   Check equipment entity inhibt       */
            /*---------------------------------------*/
            long hINHIBIT_COUNT = 0;
            sql = "SELECT OMRESTRICT_ENTITY.ID\n" +
                    "              FROM OMRESTRICT, OMRESTRICT_ENTITY\n" +
                    "             WHERE OMRESTRICT.ID   = OMRESTRICT_ENTITY.REFKEY\n" +
                    "               AND OMRESTRICT_ENTITY.ENTITY_ID  = ?\n" +
                    "               AND OMRESTRICT_ENTITY.ENTITY_TYPE = ?\n" +
                    "               AND OMRESTRICT.REASON_CODE IN ( ?, ?, ?)";
            List<CimRestrictionEntityDO> cimEntityInhibitEntityDOList = cimJpaRepository.query(sql, CimRestrictionEntityDO.class,
                    ObjectIdentifier.fetchValue(equipmentID), BizConstant.SP_INHIBITCLASSID_EQUIPMENT, BizConstant.SP_REASON_ENTITYINHIBIT_FOR_APCNORESPONSE,
                    BizConstant.SP_REASON_ENTITYINHIBIT_FOR_APCRETURNCODENG, BizConstant.SP_REASON_ENTITYINHIBIT_FOR_APCRETURNVALUEERROR);
            if (!CimArrayUtils.isEmpty(cimEntityInhibitEntityDOList)){
                for (CimRestrictionEntityDO cimEntityInhibitEntityDO : cimEntityInhibitEntityDOList){
                    hINHIBIT_COUNT = 0;
                    sql = "SELECT COUNT(ID)\n" +
                            "                  FROM OMRESTRICT_ENTITY\n" +
                            "                 WHERE ID = ?";
                    hINHIBIT_COUNT = cimJpaRepository.count(sql, cimEntityInhibitEntityDO.getId());
                    Validations.check(hINHIBIT_COUNT == 1, new OmCode(retCodeConfig.getInhibitEntity(), ObjectIdentifier.fetchValue(equipmentID), BizConstant.SP_INHIBITCLASSID_EQUIPMENT));
                }
            }
            // External System Interface Security Control
            User tmpRequestUser = this.getRequestUser(objCommon);
            Infos.APCBaseIdentification strAPCBaseIdentification = new Infos.APCBaseIdentification();
            strAPCBaseIdentification.setUserId(tmpRequestUser.getUserID().getValue());
            strAPCBaseIdentification.setPassword(tmpRequestUser.getPassword());
            strAPCBaseIdentification.setSystemName(StandardProperties.OM_SYSTEM_NAME.getValue());
            strAPCBaseIdentification.setClientNode(objCommon.getUser().getClientNode());

            /*---------------------------------*/
            /*   Get Server Name               */
            /*---------------------------------*/
            String tmpAPCServerName = StandardProperties.OM_APC_SERVER.getValue();
            String APCServerName = null;
            if (!CimStringUtils.isEmpty(tmpAPCServerName) && !tmpAPCServerName.contains(":")){
                APCServerName = ":" + tmpAPCServerName;
            } else {
                APCServerName = tmpAPCServerName;
            }
            /*---------------------------------*/
            /*   Get Host Name                 */
            /*---------------------------------*/
            String APCHostName = StandardProperties.OM_APC_HOST.getValue();

            /*-------------------------*/
            /*   Send Request to APC   */
            /*-------------------------*/
            // TxAPCRunTimeCapabilityRequest
            Inputs.APCRunTimeCapabilityRequestIn apcRunTimeCapabilityRequestIn = new Inputs.APCRunTimeCapabilityRequestIn();
            apcRunTimeCapabilityRequestIn.setSendName("APCRunTimeCapabilityRequest");
            apcRunTimeCapabilityRequestIn.setEquipmentID(ObjectIdentifier.fetchValue(equipmentID));
            apcRunTimeCapabilityRequestIn.setStrAPCBaseIdentification(strAPCBaseIdentification);
            apcRunTimeCapabilityRequestIn.setStrAPCBaseCassetteList(strAPCBaseCassetteList);
            apcRunTimeCapabilityRequestIn.setSystemNameList(systemName);
            apcRunTimeCapabilityRequestIn.setRequestUser(tmpRequestUser);
            String result = this.sendToApcServer(apcRunTimeCapabilityRequestIn);
            List<Infos.APCRunTimeCapabilityResponse> tmpApcRunTimeCapabilityResponseList = JSON.parseArray(result, Infos.APCRunTimeCapabilityResponse.class);
            //-------------------------------------------------------------------------------------------------
            // APC return only 0 or 2. 0 is nomal end, 2 is error. This returnCode is strResult.returnCode.
            // But when strAPCBaseReturnCode.state is not "OK", TxAPCRunTimeCapabilityRequest is fail.
            // By the above reason, we check strAPCBaseReturnCode.state, though returnCode is 0.
            // If except "OK" is found, this function returns error as RC_APC_RUNTIMECAPABILITY_ERROR.
            //-------------------------------------------------------------------------------------------------
            boolean errorFlag = false;
            int respones_cnt = CimArrayUtils.getSize(tmpApcRunTimeCapabilityResponseList);
            //----------------------------------------------
            // Check strAPCBaseReturnCode.state
            //----------------------------------------------
            for (int loop_check_cnt = 0; loop_check_cnt < respones_cnt; loop_check_cnt++){
                if (!CimStringUtils.equals(tmpApcRunTimeCapabilityResponseList.get(loop_check_cnt).getStrAPCBaseReturnCode().getState(), BizConstant.SP_APCRETURNCODE_OK)){
                    errorFlag = true;
                    break;
                }
            }
            Validations.check(errorFlag, retCodeConfig.getApcRuntimecapabilityError());
            return tmpApcRunTimeCapabilityResponseList;
        }
        return null;
    }

    @Override
    public void APCMgrSendControlJobInformationDR(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, ObjectIdentifier controlJobID, String controlJobStatus, List<Infos.ApcBaseCassette> strAPCBaseCassetteList) {
        String sql = "SELECT DISTINCT APC_SYSTEM_NAME\n" +
                "          FROM IFAPCSETUP\n" +
                "          WHERE EQP_ID = ?\n" +
                "            AND APC_CONFIG_STATE = ?\n" +
                "          ORDER BY APC_SYSTEM_NAME";
        List<CimIFApcSetupDO> apcIFList = cimJpaRepository.query(sql, CimIFApcSetupDO.class, ObjectIdentifier.fetchValue(equipmentID), BizConstant.SP_APC_CONFIG_STATE_APPROVED);
        int sysNameCnt = 0;
        List<String> systemNameList = new ArrayList<>();
        if (!CimArrayUtils.isEmpty(apcIFList)){
            for (CimIFApcSetupDO cimIFApcSetupDO : apcIFList){
                int tmp_cnt = 0;
                boolean systemNameFlag = false;
                for (tmp_cnt = 0; tmp_cnt < sysNameCnt; tmp_cnt++){
                    if (CimStringUtils.equals(systemNameList.get(tmp_cnt), cimIFApcSetupDO.getApcSystemName())){
                        systemNameFlag = true;
                        break;
                    }
                }
                if (!systemNameFlag){
                    systemNameList.add(cimIFApcSetupDO.getApcSystemName());
                    sysNameCnt++;
                }
            }
        }
        if (!CimArrayUtils.isEmpty(systemNameList)){
            Infos.APCBaseReturnCode apcBaseReturnCode = new Infos.APCBaseReturnCode();
            apcBaseReturnCode.setState(BizConstant.SP_APCRETURNCODE_OK);
            String tmpAPCServerName = StandardProperties.OM_APC_SERVER.getValue();
            String APCServerName = null;
            if (!CimStringUtils.isEmpty(tmpAPCServerName) && !tmpAPCServerName.contains(":")){
                APCServerName = ":" + tmpAPCServerName;
            } else {
                APCServerName = tmpAPCServerName;
            }
            String APCHostName = StandardProperties.OM_APC_HOST.getValue();
            //TxControlJobInformation
            Inputs.ControlJobInformationIn controlJobInformationIn = new Inputs.ControlJobInformationIn();
            controlJobInformationIn.setControlJobID(ObjectIdentifier.fetchValue(controlJobID));
            controlJobInformationIn.setEquipmentID(ObjectIdentifier.fetchValue(equipmentID));
            controlJobInformationIn.setControlJobStatus(controlJobStatus);
            controlJobInformationIn.setApcBaseReturnCode(apcBaseReturnCode);
            controlJobInformationIn.setStrAPCBaseCassetteList(strAPCBaseCassetteList);
            controlJobInformationIn.setSystemNameList(systemNameList);
            controlJobInformationIn.setSendName("ControlJobInformation");
            this.sendToApcServer(controlJobInformationIn);
        } else {
            log.info("Return as OK, but not interface to APC.");
            throw new ServiceException(retCodeConfigEx.getOkNoIF());
        }
    }

    @Override
    public List<Infos.APCIf> APCIFListGetDR(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID) {
        List<Infos.APCIf> apcIfList = new ArrayList<>();
        String sql = "SELECT EQP_ID,\n" +
                "                   EQP_DESCRIPTION,\n" +
                "                   APC_SYSTEM_NAME,\n" +
                "                   IGNOREABLE,\n" +
                "                   APC_REP1_USER_ID,\n" +
                "                   APC_REP2_USER_ID,\n" +
                "                   APC_CONFIG_STATE,\n" +
                "                   REGISTERED_USER_ID,\n" +
                "                   REGISTERED_TIME,\n" +
                "                   REGISTERED_MEMO,\n" +
                "                   APPROVED_USER_ID,\n" +
                "                   APPROVED_TIME,\n" +
                "                   APPROVED_MEMO,\n" +
                "                   UPDATE_TIME\n" +
                "             FROM IFAPCSETUP\n" +
                "             WHERE EQP_ID LIKE ? ";
        List<CimIFApcSetupDO> apcIFList = cimJpaRepository.query(sql, CimIFApcSetupDO.class, ObjectIdentifier.fetchValue(equipmentID));
        int count = 0;
        if (!CimArrayUtils.isEmpty(apcIFList)){
            for (CimIFApcSetupDO cimIFApcSetupDO : apcIFList){
                Infos.APCIf apcIf = new Infos.APCIf();
                apcIfList.add(apcIf);
                apcIf.setEquipmentID(new ObjectIdentifier(cimIFApcSetupDO.getEquipmentID()));
                apcIf.setEqpDescription(cimIFApcSetupDO.getEquipmentDescription());
                apcIf.setAPCSystemName(cimIFApcSetupDO.getApcSystemName());
                apcIf.setAPCIgnoreable(cimIFApcSetupDO.getIgnoreable() > 0);
                apcIf.setAPCRep1UserID(new ObjectIdentifier(cimIFApcSetupDO.getApcRep1UserID()));
                apcIf.setAPCRep2UserID(new ObjectIdentifier(cimIFApcSetupDO.getApcRep2UserID()));
                apcIf.setAPCConfigStatus(cimIFApcSetupDO.getApcConfigState());
                apcIf.setRegisteredUserID(new ObjectIdentifier(cimIFApcSetupDO.getRegisteredUserID()));
                apcIf.setRegisteredTimeStamp(CimDateUtils.convertToSpecString(cimIFApcSetupDO.getRegisteredTime()));
                apcIf.setRegisteredMemo(cimIFApcSetupDO.getRegisteredMemo());
                apcIf.setApprovedTimeStamp(CimDateUtils.convertToSpecString(cimIFApcSetupDO.getApprovedTime()));
                apcIf.setApprovedMemo(cimIFApcSetupDO.getApprovedMemo());
                apcIf.setUpdateTimeStamp(CimDateUtils.convertToSpecString(cimIFApcSetupDO.getUpdateTime()));
                count++;
            }
        }
        return apcIfList;
    }

    @Override
    public void APCIFPointInsertDR(Infos.ObjCommon objCommon, String operation, Infos.APCIf apcIf) {
        //-------------------------------------------------------------------------------
        // Check APCSystemName
        // The characters of APCSystemName cannot use
        //          except A-Z, a-z, 0-9, '.'(piriod), '/'(slash), '_'(underscore) .
        //-------------------------------------------------------------------------------
        String pAPCSystemName = apcIf.getAPCSystemName();
        String regEx = "[a-zA-Z0-9./_]*";
        Matcher matcher = Pattern.compile(regEx).matcher(pAPCSystemName);
        if (!matcher.matches()){
            log.info("The characters of APCSystemName Error! -->> {}", apcIf.getAPCSystemName());
            throw new ServiceException(retCodeConfig.getInvalidInputParam());
        }
        //--------------------------
        // Main Process
        //--------------------------
        String sql = "SELECT OMEQP.DESCRIPTION\n" +
                "                 FROM  OMEQP\n" +
                "                 WHERE OMEQP.EQP_ID = ?";
        CimEquipmentDO equipmentDO = cimJpaRepository.queryOne(sql, CimEquipmentDO.class, ObjectIdentifier.fetchValue(apcIf.getEquipmentID()));
        Validations.check(null == equipmentDO,retCodeConfig.getNotFoundEqp(),ObjectIdentifier.fetchValue(apcIf.getEquipmentID()));
        //---------------------
        // Config Status
        //---------------------
        Infos.APCIf tmpAPCIf = apcIf;
//        if (tmpAPCIf == null){
//            tmpAPCIf = new Infos.APCIf();
//        }
        String apcConfigState = null;
        if (CimStringUtils.equals(operation, BizConstant.SP_APCIF_OPERATION_NEW)){
            apcConfigState = BizConstant.SP_APC_CONFIG_STATE_REQUESTED;
            tmpAPCIf.setAPCConfigStatus(BizConstant.SP_APC_CONFIG_STATE_REQUESTED);
        } else if (CimStringUtils.equals(operation, BizConstant.SP_APCIF_OPERATION_APPROVEX)){
            apcConfigState = BizConstant.SP_APC_CONFIG_STATE_APPROVED;
            tmpAPCIf.setAPCConfigStatus(BizConstant.SP_APC_CONFIG_STATE_APPROVED);
        }
        CimIFApcSetupDO cimIFApcSetupDO = new CimIFApcSetupDO();
        cimIFApcSetupDO.setEquipmentID(ObjectIdentifier.fetchValue(apcIf.getEquipmentID()));
        cimIFApcSetupDO.setEquipmentDescription(equipmentDO.getDescription());
        cimIFApcSetupDO.setApcSystemName(apcIf.getAPCSystemName());
        cimIFApcSetupDO.setIgnoreable(apcIf.isAPCIgnoreable() ? 1 : 0);
        cimIFApcSetupDO.setApcRep1UserID(ObjectIdentifier.fetchValue(apcIf.getAPCRep1UserID()));
        cimIFApcSetupDO.setApcRep2UserID(ObjectIdentifier.fetchValue(apcIf.getAPCRep2UserID()));
        cimIFApcSetupDO.setApcConfigState(apcConfigState);
        cimIFApcSetupDO.setRegisteredUserID(ObjectIdentifier.fetchValue(apcIf.getRegisteredUserID()));
        cimIFApcSetupDO.setRegisteredTime(CimDateUtils.convertToOrInitialTime(apcIf.getRegisteredTimeStamp()));
        cimIFApcSetupDO.setRegisteredMemo(apcIf.getRegisteredMemo());
        cimIFApcSetupDO.setApprovedUserID(ObjectIdentifier.fetchValue(apcIf.getApprovedUserID()));
        cimIFApcSetupDO.setApprovedTime(CimDateUtils.convertToOrInitialTime(apcIf.getApprovedTimeStamp()));
        cimIFApcSetupDO.setApprovedMemo(apcIf.getApprovedMemo());
        cimIFApcSetupDO.setUpdateTime(CimDateUtils.getCurrentTimeStamp());
        cimJpaRepository.save(cimIFApcSetupDO);
        //------------------------------------------------------
        // Event Make APCIF_point_UpdateEvent for APC I/F
        //------------------------------------------------------
        eventMethod.APCIFPointUpdateEventMake(objCommon, objCommon.getTransactionID(), tmpAPCIf, BizConstant.SP_APCIFOPECATEGORY_NEW);
    }

    @Override
    public void APCIFPointUpdateDR(Infos.ObjCommon objCommon, String operation, Infos.APCIf apcIf) {
        //---------------------
        // Config Status
        //---------------------
        Infos.APCIf tmpAPCIf = apcIf;
        String apcConfigState = null;
        if (CimStringUtils.equals(operation, BizConstant.SP_APCIF_OPERATION_UPDATE)){
            apcConfigState = apcIf.getAPCConfigStatus();
        } else if (CimStringUtils.equals(operation, BizConstant.SP_APCIF_OPERATION_APPROVE)){
            apcConfigState = BizConstant.SP_APC_CONFIG_STATE_APPROVED;
            tmpAPCIf.setAPCConfigStatus(BizConstant.SP_APC_CONFIG_STATE_APPROVED);
        } else if (CimStringUtils.equals(operation, BizConstant.SP_APCIF_OPERATION_REJECT)){
            apcConfigState = BizConstant.SP_APC_CONFIG_STATE_REJECTED;
            tmpAPCIf.setAPCConfigStatus(BizConstant.SP_APC_CONFIG_STATE_REJECTED);
        }
        String sql = "SELECT * FROM IFAPCSETUP  WHERE EQP_ID    = ?\n" +
                "            AND APC_SYSTEM_NAME    = ?\n" +
                "            AND UPDATE_TIME=TO_TIMESTAMP(?,'yyyy-MM-dd-HH24.mi.SSxFF')";
        CimIFApcSetupDO cimIFApcSetupDO = cimJpaRepository.queryOne(sql, CimIFApcSetupDO.class, ObjectIdentifier.fetchValue(apcIf.getEquipmentID()), apcIf.getAPCSystemName(), apcIf.getUpdateTimeStamp());
        Validations.check(cimIFApcSetupDO == null, retCodeConfigEx.getNotFoundApcIFPoint());
        cimIFApcSetupDO.setApcConfigState(apcConfigState);
        cimIFApcSetupDO.setIgnoreable(apcIf.isAPCIgnoreable() ? 1 : 0);
        cimIFApcSetupDO.setApcRep1UserID(ObjectIdentifier.fetchValue(apcIf.getAPCRep1UserID()));
        cimIFApcSetupDO.setApcRep2UserID(ObjectIdentifier.fetchValue(apcIf.getAPCRep2UserID()));
        cimIFApcSetupDO.setRegisteredUserID(ObjectIdentifier.fetchValue(apcIf.getRegisteredUserID()));
        cimIFApcSetupDO.setRegisteredTime(CimDateUtils.convertToOrInitialTime(apcIf.getRegisteredTimeStamp()));
        cimIFApcSetupDO.setRegisteredMemo(apcIf.getRegisteredMemo());
        cimIFApcSetupDO.setApprovedUserID(ObjectIdentifier.fetchValue(apcIf.getApprovedUserID()));
        cimIFApcSetupDO.setApprovedTime(CimDateUtils.convertToOrInitialTime(apcIf.getApprovedTimeStamp()));
        cimIFApcSetupDO.setApprovedMemo(apcIf.getApprovedMemo());
        cimIFApcSetupDO.setUpdateTime(CimDateUtils.getCurrentTimeStamp());
        cimJpaRepository.save(cimIFApcSetupDO);
        //------------------------------------------------------
        // Event Make APCIF_point_UpdateEvent for APC I/F
        //------------------------------------------------------
        eventMethod.APCIFPointUpdateEventMake(objCommon, objCommon.getTransactionID(), tmpAPCIf, BizConstant.SP_APCIFOPECATEGORY_UPDATE);
    }

    @Override
    public void APCIFPointDeleteDR(Infos.ObjCommon objCommon, String operation, Infos.APCIf apcIf) {
        String sql = "SELECT * FROM IFAPCSETUP  WHERE EQP_ID    = ?\n" +
                "            AND APC_SYSTEM_NAME    = ?\n" +
                "            AND UPDATE_TIME        = TO_TIMESTAMP(?,'yyyy-MM-dd-HH24.mi.SSxFF')";
        CimIFApcSetupDO cimIFApcSetupDO = cimJpaRepository.queryOne(sql, CimIFApcSetupDO.class, ObjectIdentifier.fetchValue(apcIf.getEquipmentID()), apcIf.getAPCSystemName(), apcIf.getUpdateTimeStamp());
        Validations.check(cimIFApcSetupDO == null, retCodeConfigEx.getNotFoundApcIFPoint());
        cimJpaRepository.delete(cimIFApcSetupDO);
        //------------------------------------------------------
        // Event Make APCIF_point_UpdateEvent for APC I/F
        //------------------------------------------------------
        eventMethod.APCIFPointUpdateEventMake(objCommon, objCommon.getTransactionID(), apcIf, BizConstant.SP_APCIFOPECATEGORY_DELETE);
    }

    private User getRequestUser(Infos.ObjCommon objCommon){
        User tmpRequestUser = objCommon.getUser();
        return tmpRequestUser;
    }

    @Override
    public String sendToApcServer(Inputs.ApcIn apcIn){
        Outputs.ApcOut apcOut = apcRemoteManager.sendInfo(apcIn);
        if(apcOut == null){
            throw new ServiceException(retCodeConfig.getNoResponseApc());
        }
        if (apcOut.getCode() == 0){
            return apcOut.getResultBody();
        } else {
            throw new ServiceException(new OmCode(apcOut.getCode(), apcOut.getMessage()));
        }
    }


}
