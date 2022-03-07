package com.fa.cim.service.automonitor.Impl;

import com.fa.cim.annotaion.OmService;
import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.utils.*;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.config.RetCodeConfigEx;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Inputs;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;
import com.fa.cim.dto.am.AllRecipeByProductSpecificationInqParam;
import com.fa.cim.method.IEquipmentMethod;
import com.fa.cim.method.ILotMethod;
import com.fa.cim.method.IProcessMethod;
import com.fa.cim.method.IProductMethod;
import com.fa.cim.method.impl.equipment.EquipmentWhatNextMethod;
import com.fa.cim.newcore.bo.machine.CimMachine;
import com.fa.cim.newcore.bo.recipe.CimLogicalRecipe;
import com.fa.cim.newcore.bo.recipe.RecipeManager;
import com.fa.cim.newcore.dto.recipe.RecipeDTO;
import com.fa.cim.service.automonitor.IAutoMonitorInqService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@OmService
@Slf4j
public class AutoMonitorInqServiceImpl implements IAutoMonitorInqService {

    @Autowired
    private RetCodeConfig retCodeConfig;

    @Autowired
    private RetCodeConfigEx retCodeConfigEx;

    @Autowired
    private IEquipmentMethod equipmentMethod;

    @Autowired
    private IProductMethod productMethod;

    @Autowired
    private IProcessMethod processMethod;

    @Autowired
    private RecipeManager recipeManager;

    @Autowired
    private ILotMethod lotMethod;

    public Results.AMJobListInqResult sxAMJobListInq(Infos.ObjCommon objCommon, Params.AMJobListInqParams params) {

        Results.AMJobListInqResult resultObj = new Results.AMJobListInqResult();
        //----------------------------------------------------------------
        //  In-Parameter Trace
        //----------------------------------------------------------------
        if (ObjectIdentifier.isEmpty(params.getEquipmentID())) {
            throw new ServiceException(retCodeConfigEx.getBlankInputParameter());
        }
        if (ObjectIdentifier.isEmpty(params.getEqpMonitorID())
                && ObjectIdentifier.isEmpty(params.getEqpMonitorJobID())) {
            throw new ServiceException(retCodeConfig.getInvalidParameterWithMsg());
        }
        //----------------------------------------------------------------
        //  Get EqpMonitor information from DB
        //----------------------------------------------------------------
        Infos.EqpMonitorJobListGetDRIn eqpMonitorJobListGetDRIn = new Infos.EqpMonitorJobListGetDRIn();
        eqpMonitorJobListGetDRIn.setEquipmentID(params.getEquipmentID());
        eqpMonitorJobListGetDRIn.setEqpMonitorID(params.getEqpMonitorID());
        eqpMonitorJobListGetDRIn.setEqpMonitorJobID(params.getEqpMonitorJobID());
        List<Infos.EqpMonitorJobDetailInfo> eqpMonitorJobListGetDR = equipmentMethod.eqpMonitorJobListGetDR(objCommon, eqpMonitorJobListGetDRIn);

        log.info(" Get EqpMonitor information from DB is success");
        //------------------------------------------------------
        //  Set output result
        //------------------------------------------------------
        resultObj.setStrEqpMonitorJobDetailInfoSeq(eqpMonitorJobListGetDR);
        return resultObj;
    }

    public List<Infos.EqpMonitorDetailInfo> sxAMListInq(Infos.ObjCommon objCommon, Params.AMListInqParams params) {
        //----------------------------------------------------------------
        //  In-Parameter Trace
        //----------------------------------------------------------------
        Validations.check(ObjectIdentifier.isEmpty(params.getEquipmentID()), retCodeConfigEx.getBlankInputParameter());
        //----------------------------------------------------------------
        //  Get EqpMonitor information from DB
        //----------------------------------------------------------------
        Inputs.ObjEqpMonitorListGetDRIn eqpMonitorListGetDRIn = new Inputs.ObjEqpMonitorListGetDRIn();
        eqpMonitorListGetDRIn.setEquipmentID(params.getEquipmentID());
        eqpMonitorListGetDRIn.setEqpMonitorID(params.getEqpMonitorID());
        List<Infos.EqpMonitorDetailInfo> eqpMonitorDetailInfos = equipmentMethod.eqpMonitorListGetDR(eqpMonitorListGetDRIn, objCommon);
        log.info("EqpMonitor information ： {}", eqpMonitorDetailInfos);
        //----------------------------------------------------------------
        //  Calculate nextExecutionTime by collected EqpMonitor
        //----------------------------------------------------------------
        int lenEqpMonitorDetailInfos = CimArrayUtils.getSize(eqpMonitorDetailInfos);
        for (int i = 0; i < lenEqpMonitorDetailInfos; i++) {
            if (BizConstant.SP_EQPMONITOR_TYPE_ROUTINE.equals(eqpMonitorDetailInfos.get(i).getMonitorType())) {
                Infos.EqpMonitorNextExecutionTimeCalculateIn eqpMonitorNextExecutionTimeCalculateIn = new Infos.EqpMonitorNextExecutionTimeCalculateIn();
                eqpMonitorNextExecutionTimeCalculateIn.setCurrentScheduleBaseTime(Timestamp.valueOf(eqpMonitorDetailInfos.get(i).getScheduleBaseTimeStamp()));
                eqpMonitorNextExecutionTimeCalculateIn.setExecutionInterval(eqpMonitorDetailInfos.get(i).getExecutionInterval());
                eqpMonitorNextExecutionTimeCalculateIn.setScheduleAdjustment(eqpMonitorDetailInfos.get(i).getScheduleAdjustment());
                eqpMonitorNextExecutionTimeCalculateIn.setLastMonitorPassedTime(eqpMonitorDetailInfos.get(i).getLastMonitorPassedTimeStamp());
                eqpMonitorNextExecutionTimeCalculateIn.setExpirationInterval(eqpMonitorDetailInfos.get(i).getExpirationInterval());
                eqpMonitorNextExecutionTimeCalculateIn.setFutureTimeRequireFlag(false);
                Results.EqpMonitorNextExecutionTimeCalculateResult eqpMonitorNextExecutionTimeCalculateResult = null;
                try {
                    eqpMonitorNextExecutionTimeCalculateResult = equipmentMethod.eqpMonitorNextExecutionTimeCalculate(objCommon, eqpMonitorNextExecutionTimeCalculateIn);
                } catch (ServiceException e) {
                    if (!Validations.isEquals(retCodeConfig.getExceedExpirationTime(), e.getCode())){
                        throw e;
                    }
                    eqpMonitorNextExecutionTimeCalculateResult = (Results.EqpMonitorNextExecutionTimeCalculateResult) e.getData();
                }

                log.info(" Calculate nextExecutionTime ： {}", eqpMonitorNextExecutionTimeCalculateResult);
                Infos.EqpMonitorDetailInfo eqpMonitorDetailInfo = eqpMonitorDetailInfos.get(i);
                eqpMonitorDetailInfo.setNextExecutionTime(eqpMonitorNextExecutionTimeCalculateResult.getNextExecutionTime());
                eqpMonitorDetailInfo.setExpirationTime(eqpMonitorNextExecutionTimeCalculateResult.getExpirationTime());
            }
        }
        //--------------------------
        //  Set output result
        //--------------------------
        return eqpMonitorDetailInfos;
    }

    @Autowired
    private EquipmentWhatNextMethod equipmentWhatNextMethod;

    @Override
    public Results.WhatNextAMLotInqResult sxWhatNextAMLotInq(Infos.ObjCommon objCommon, Params.WhatNextAMLotInqInParm parm) {
        Results.WhatNextAMLotInqResult out = new Results.WhatNextAMLotInqResult();

        //----------------------------------------------------------------
        //  In-Parameter Trace
        //----------------------------------------------------------------
        Validations.check(ObjectIdentifier.isEmpty(parm.getEquipmentID()), retCodeConfigEx.getBlankInputParameter());
        Validations.check(ObjectIdentifier.isEmpty(parm.getEqpMonitorID()), retCodeConfigEx.getBlankInputParameter());
        Validations.check(!CimStringUtils.equals(BizConstant.SP_EQPMONITOR_LEVEL_EQPMONKIT, parm.getSelectCriteria())
                && !CimStringUtils.equals(BizConstant.SP_EQPMONITOR_LEVEL_EQPMONNOKIT, parm.getSelectCriteria()), retCodeConfig.getInvalidInputParam());

        Inputs.ObjEquipmentLotsWhatNextDRIn in = new Inputs.ObjEquipmentLotsWhatNextDRIn();
        in.setEquipmentID(parm.getEquipmentID());
        in.setSelectCriteria(parm.getSelectCriteria());
        in.setEqpMonitorID(parm.getEqpMonitorID());
//        Results.WhatNextLotListResult strEquipmentLotsWhatNextDROut = equipmentMethod.equipmentLotsWhatNextDR(in, objCommon);
        Results.WhatNextLotListResult strEquipmentLotsWhatNextDROut = equipmentWhatNextMethod.equipmentLotsWhatNextDR(in, objCommon);


        //Set return value
        out.setEquipmentID(parm.getEquipmentID());
        out.setEqpMonitorID(parm.getEqpMonitorID());
        out.setDispatchRule(strEquipmentLotsWhatNextDROut.getDispatchRule());
        //Lot reserved by EqpMonitor job should be excepted from candidate
        List<Infos.WhatNextAttributes> strWhatNextAttributesList = new ArrayList<Infos.WhatNextAttributes>();
        List<Infos.WhatNextAttributes> strWhatNextAttributes = strEquipmentLotsWhatNextDROut.getStrWhatNextAttributes();
        int whatNextAttriButeSize = CimArrayUtils.getSize(strWhatNextAttributes);
        for (int i = 0; i < whatNextAttriButeSize; i++) {
            if(CimObjectUtils.isEmpty(strWhatNextAttributes.get(i).getEqpMonitorJobID())){
                strWhatNextAttributesList.add(strWhatNextAttributes.get(i));
            }

        }

        Inputs.ObjLotCheckConditionForWhatNextEqpMonitorLotIn objLotCheckConditionForWhatNextEqpMonitorLotIn = new Inputs.ObjLotCheckConditionForWhatNextEqpMonitorLotIn();
        objLotCheckConditionForWhatNextEqpMonitorLotIn.setEqpMonitorID(parm.getEqpMonitorID());
        objLotCheckConditionForWhatNextEqpMonitorLotIn.setCheckLevel(parm.getSelectCriteria());
        objLotCheckConditionForWhatNextEqpMonitorLotIn.setStrWhatNextAttributes(strWhatNextAttributesList);
        List<Infos.WhatNextAttributes> strLotCheckConditionForWhatNextEqpMonitorLotOut = lotMethod.lotCheckConditionForWhatNextEqpMonitorLot(objCommon, objLotCheckConditionForWhatNextEqpMonitorLotIn);

        //------------------------------------------------------
        //  Set output result
        //------------------------------------------------------
        out.setStrWhatNextAttributes(strLotCheckConditionForWhatNextEqpMonitorLotOut);
        return out;
    }

    /**
     * ho
     * @param objCommon
     * @param allRecipeByProductSpecificationInqParam
     */
    @Override
    public List<Infos.DefaultRecipeSetting> sxAllRecipeByProductSpecificationInq(Infos.ObjCommon objCommon, AllRecipeByProductSpecificationInqParam allRecipeByProductSpecificationInqParam) {
        List<Infos.DefaultRecipeSetting> result=new ArrayList<>();

        // check params
        if (allRecipeByProductSpecificationInqParam==null){
            log.info("allRecipeByProductSpecificationInqParam is null!");
            return result;
        }

        ObjectIdentifier productID = allRecipeByProductSpecificationInqParam.getProductID();
        ObjectIdentifier equipmentID = allRecipeByProductSpecificationInqParam.getEquipmentID();
        ObjectIdentifier chamberID = allRecipeByProductSpecificationInqParam.getChamberID();
        Validations.check(ObjectIdentifier.isEmptyWithValue(productID),retCodeConfig.getInvalidParameter());
        Validations.check(ObjectIdentifier.isEmptyWithValue(equipmentID),retCodeConfig.getInvalidParameter());

        ObjectIdentifier routeID = productMethod.productRouteInfoGet(objCommon, productID);

        Inputs.ProcessOperationListForRoute strProcessOperationListForRouteIn=new Inputs.ProcessOperationListForRoute();
        strProcessOperationListForRouteIn.setRouteID(routeID);
        List<Infos.OperationNameAttributes> operationNameAttributes = processMethod.processOperationListForRoute(objCommon, strProcessOperationListForRouteIn);

        if (CimArrayUtils.isEmpty(operationNameAttributes)){
            log.info("operationNameAttributes is empty!");
            return result;
        }

        List<Infos.OperationNameAttributes> operationNameAttributesList = operationNameAttributes.stream().filter(operationNameAttribute -> {
            List<ObjectIdentifier> machineList = operationNameAttribute.getMachineList();
            if (CimArrayUtils.isEmpty(machineList)) {
//                return false; 此接口未返回machineList
                return true;
            }
            return machineList.stream().anyMatch(machine -> CimStringUtils.equals(equipmentID, machine.getValue()));
        }).collect(Collectors.toList());

        if (CimArrayUtils.isEmpty(operationNameAttributesList)){
            log.info("operationNameAttributesList is empty!");
            return result;
        }

        operationNameAttributesList.forEach(operationNameAttribute -> {
            ObjectIdentifier logicRecipeID = processMethod.processDefaultLogicalRecipeGetDR(objCommon, operationNameAttribute.getOperationID());
            CimLogicalRecipe logicalRecipe = recipeManager.findLogicalRecipeNamed(ObjectIdentifier.fetchValue(logicRecipeID));
            if (logicalRecipe==null){
                return;
            }

            List<CimMachine> machineList = logicalRecipe.retrieveMachines();
            if (CimArrayUtils.isEmpty(machineList)){
                log.debug("machineList is empty!");
                return;
            }

            if (!machineList.stream().anyMatch(cimMachine -> ObjectIdentifier.equalsWithValue(equipmentID,cimMachine.getIdentifier()))){
                log.debug("machineList is not exist {}!",ObjectIdentifier.fetchValue(equipmentID));
                return;
            }

            List<RecipeDTO.DefaultRecipeSetting> defaultRecipeSettings = logicalRecipe.getDefaultRecipeSettings();
            if (CimArrayUtils.isEmpty(defaultRecipeSettings)){
                log.info("defaultRecipeSettings is empty!");
                return;
            }
            if (ObjectIdentifier.isEmptyWithValue(chamberID)){
                result.addAll(defaultRecipeSettings.stream().map(this::convertDTO).collect(Collectors.toList()));
                return;
            }
            defaultRecipeSettings.forEach(defaultRecipeSetting -> {
                List<RecipeDTO.ProcessResourceState> processResourceStates = defaultRecipeSetting.getProcessResourceStates();
                if (CimArrayUtils.isEmpty(processResourceStates)){
                    return;
                }
                if (processResourceStates.stream().anyMatch(chamber -> ObjectIdentifier.equalsWithValue(chamberID,chamber.getProcessResourceName())&& CimBooleanUtils.isTrue(chamber.getState()))){
                    result.add(this.convertDTO(defaultRecipeSetting));
                }
            });
        });
        //filter
        List<Infos.DefaultRecipeSetting> recipeSettings = new ArrayList<>();
        if (CimArrayUtils.isNotEmpty(result)){
            Map<String, List<Infos.DefaultRecipeSetting>> collect = result.parallelStream().collect(Collectors.groupingBy(s -> s.getRecipe().getValue()));
            recipeSettings = collect.values().parallelStream().map(recipes -> recipes.get(0)).collect(Collectors.toList());
        }
        return recipeSettings;
    }

    private Infos.DefaultRecipeSetting convertDTO(RecipeDTO.DefaultRecipeSetting defaultRecipeSetting) {
        if(null == defaultRecipeSetting) return null;
        Infos.DefaultRecipeSetting recipeSetting = new Infos.DefaultRecipeSetting();

        recipeSetting.setRecipe(defaultRecipeSetting.getRecipe());
        recipeSetting.setDcDefinition(defaultRecipeSetting.getDcDefinition());
        recipeSetting.setDcSpec(defaultRecipeSetting.getDcSpec());
        recipeSetting.setBinDefinition(defaultRecipeSetting.getBinDefinition());
        recipeSetting.setSampleSpecification(defaultRecipeSetting.getSampleSpecification());
        recipeSetting.setFixtureGroups(defaultRecipeSetting.getFixtureGroups());

        // chambers
        final List<RecipeDTO.Chamber> chamberSeqTmp = defaultRecipeSetting.getChamberSeq();
        List<Infos.Chamber> chamberSeq = new ArrayList<>();
        if(CimArrayUtils.isNotEmpty(chamberSeqTmp)) {
            for (RecipeDTO.Chamber chamber : chamberSeqTmp) {
                Infos.Chamber chmb = new Infos.Chamber();
                chmb.setChamberID(chamber.getChamberID());
                chmb.setState(chamber.getState());
                chamberSeq.add(chmb);
            }
        }
        recipeSetting.setChamberSeq(chamberSeq);

        // rocessResource
        final List<RecipeDTO.ProcessResourceState> processResourceStatesTmp = defaultRecipeSetting.getProcessResourceStates();
        List<Infos.ProcessResourceState> processResourceStates = new ArrayList<>();
        if(CimArrayUtils.isNotEmpty(processResourceStatesTmp)) {
            for (RecipeDTO.ProcessResourceState processResourceState : processResourceStatesTmp) {
                Infos.ProcessResourceState state = new Infos.ProcessResourceState();
                state.setState(processResourceState.getState());
                state.setProcessResourceName(processResourceState.getProcessResourceName());
                processResourceStates.add(state);
            }
        }
        recipeSetting.setProcessResourceStates(processResourceStates);

        // RecipeParameters
        final List<RecipeDTO.RecipeParameter> recipeParametersTmp = defaultRecipeSetting.getRecipeParameters();
        List<Infos.RecipeParameter> recipeParameters = new ArrayList<>();
        if(CimArrayUtils.isNotEmpty(recipeParametersTmp)) {
            for (RecipeDTO.RecipeParameter recipeParameter : recipeParametersTmp) {
                Infos.RecipeParameter parameter = new Infos.RecipeParameter();
                parameter.setParameterName(recipeParameter.getParameterName());
                parameter.setUnit(recipeParameter.getUnit());
                parameter.setDataType(recipeParameter.getDataType());
                parameter.setDefaultValue(recipeParameter.getDefaultValue());
                parameter.setLowerLimit(recipeParameter.getLowerLimit());
                parameter.setUpperLimit(recipeParameter.getUpperLimit());
                parameter.setUseCurrentValueFlag(recipeParameter.getUseCurrentValueFlag());
                parameter.setTag(recipeParameter.getTag());
                recipeParameters.add(parameter);
            }
        }
        recipeSetting.setRecipeParameters(recipeParameters);
        return recipeSetting;
    }
}
