package com.fa.cim.method.impl;

import com.fa.cim.annotaion.OmMethod;
import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.utils.CimArrayUtils;
import com.fa.cim.common.utils.CimStringUtils;
import com.fa.cim.common.utils.Validations;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.config.RetCodeConfigEx;
import com.fa.cim.dto.Infos;
import com.fa.cim.method.IScriptMethod;
import com.fa.cim.newcore.bo.abstractgroup.*;
import com.fa.cim.newcore.bo.durable.CimCassette;
import com.fa.cim.newcore.bo.durable.CimProcessDurable;
import com.fa.cim.newcore.bo.durable.CimReticlePod;
import com.fa.cim.newcore.bo.durable.DurableManager;
import com.fa.cim.newcore.bo.factory.CimMESFactory;
import com.fa.cim.newcore.bo.machine.CimMachine;
import com.fa.cim.newcore.bo.machine.MachineManager;
import com.fa.cim.newcore.bo.pd.CimProcessDefinition;
import com.fa.cim.newcore.bo.pd.ProcessDefinitionManager;
import com.fa.cim.newcore.bo.person.CimPerson;
import com.fa.cim.newcore.bo.person.PersonManager;
import com.fa.cim.newcore.bo.prodspec.CimProductSpecification;
import com.fa.cim.newcore.bo.prodspec.CimTechnology;
import com.fa.cim.newcore.bo.prodspec.ProductSpecificationManager;
import com.fa.cim.newcore.bo.product.CimLot;
import com.fa.cim.newcore.bo.product.CimWafer;
import com.fa.cim.newcore.bo.product.ProductManager;
import com.fa.cim.newcore.bo.recipe.CimLogicalRecipe;
import com.fa.cim.newcore.bo.recipe.CimMachineRecipe;
import com.fa.cim.newcore.bo.recipe.RecipeManager;
import com.fa.cim.newcore.dto.global.GlobalDTO;
import com.fa.cim.newcore.dto.property.Property;
import com.fa.cim.newcore.enums.PropertyTypeEnum;
import com.fa.cim.newcore.standard.drblmngm.Cassette;
import com.fa.cim.newcore.standard.drblmngm.ProcessDurable;
import com.fa.cim.newcore.standard.prdctmng.Product;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.fa.cim.common.constant.BizConstant.*;

/**
 * description:
 * <p></p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/1/8                            Wind                create file
 *
 * @author Wind
 * @since 2019/1/8 09:28
 * Copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@OmMethod
public class ScriptMethod implements IScriptMethod {

    @Autowired
    private RetCodeConfigEx retCodeConfigEx;

    @Autowired
    private RetCodeConfig retCodeConfig;

    @Autowired
    private ProductManager productManager;

    @Autowired
    private MachineManager machineManager;

    @Autowired
    private RecipeManager recipeManager;

    @Autowired
    private DurableManager durableManager;

    @Autowired
    private ProcessDefinitionManager processDefinitionManager;

    @Autowired
    private ProductSpecificationManager productSpecificationManager;

    @Autowired
    private PersonManager personManager;

    @Autowired
    private CimMESFactory mesFactory;

    @Autowired
    private PCSManager pcsManager;

    @Override
    public List<Infos.UserParameterValue> scriptGetUserParameter(Infos.ObjCommon objCommon, String parameterClass, String identifier) {
        log.info("in param parameterClass      : " + parameterClass);
        log.info("in param identifier          : " + identifier);
        List<Infos.UserParameterValue> retVal = new ArrayList<>();

        CimPropertySet aPropertySet = this.getPropertySet(parameterClass, identifier);
        List<CimPropertyBase> aPropertySequence = aPropertySet.allProperties();

        List<GlobalDTO.BRSUserDefinedVariableInfo> aUserDefinedVariableInfos = pcsManager.getAllUserDefinedParameterForAClassNamed(parameterClass);
        Optional.ofNullable(aUserDefinedVariableInfos).ifPresent(userDefinedVariableInfos -> userDefinedVariableInfos.forEach(variable -> {
            Infos.UserParameterValue tempParameter = new Infos.UserParameterValue();
            tempParameter.setChangeType((long) BizConstant.SP_PARVAL_NOCHANGE);
            tempParameter.setParameterName(variable.getPVariableName());
            tempParameter.setDataType(variable.getPVariableType());
            tempParameter.setDescription(variable.getPDescription());

            tempParameter.setValue(variable.getPDefaultValue());
            tempParameter.setValueFlag(false);
            retVal.add(tempParameter);

            for (CimPropertyBase property : aPropertySequence) {
                if (CimStringUtils.equals(tempParameter.getParameterName(), property.getIdentifier())) {
                    switch (tempParameter.getDataType()) {
                        case SP_SCRIPTPARM_DATATYPE_SI:
                            CimPropertyTableSI tableSI = (CimPropertyTableSI) property;
                            List<Property.TableSIData> tableSIDatas = tableSI.allValues();
                            int size = CimArrayUtils.getSize(tableSIDatas);
                            if (size > 0) {
                                tempParameter.setValueFlag(true);
                                tempParameter.setKeyValue(tableSIDatas.get(0).getKeyValue());
                                tempParameter.setValue(String.valueOf(tableSIDatas.get(0).getValue()));
                                for (int k = 1; k < size; k++) {
                                    Infos.UserParameterValue target = new Infos.UserParameterValue();
                                    BeanUtils.copyProperties(target, retVal.get(CimArrayUtils.getSize(retVal) - 1));
                                    target.setKeyValue(tableSIDatas.get(k).getKeyValue());
                                    target.setValue(String.valueOf(tableSIDatas.get(k).getValue()));
                                    retVal.add(target);
                                }
                            }
                            break;
                        case SP_SCRIPTPARM_DATATYPE_SR:
                            CimPropertyTableSR tableSR = (CimPropertyTableSR) property;
                            List<Property.TableSRData> tableSRDatas = tableSR.allValues();
                            size = CimArrayUtils.getSize(tableSRDatas);
                            if (size > 0) {
                                tempParameter.setValueFlag(true);
                                tempParameter.setKeyValue(tableSRDatas.get(0).getKeyValue());
                                tempParameter.setValue(String.valueOf(tableSRDatas.get(0).getValue()));
                                for (int k = 0; k < size; k++) {
                                    Infos.UserParameterValue target = new Infos.UserParameterValue();
                                    retVal.add(target);
                                    BeanUtils.copyProperties(target, retVal.get(CimArrayUtils.getSize(retVal) - 1));
                                    target.setValue(String.valueOf(tableSRDatas.get(k).getValue()));
                                    target.setKeyValue(tableSRDatas.get(k).getKeyValue());
                                }
                            }
                            break;
                        case SP_SCRIPTPARM_DATATYPE_SS:
                            CimPropertyTableSS tableSS = (CimPropertyTableSS) property;
                            List<Property.TableSSData> tableSSDatas = tableSS.allValues();
                            size = CimArrayUtils.getSize(tableSSDatas);
                            if (size > 0) {
                                tempParameter.setValueFlag(true);
                                tempParameter.setValue(String.valueOf(tableSSDatas.get(0).getValue()));
                                tempParameter.setKeyValue(tableSSDatas.get(0).getKeyValue());
                                for (int k = 0; k < size; k++) {
                                    Infos.UserParameterValue target = new Infos.UserParameterValue();
                                    BeanUtils.copyProperties(target, retVal.get(CimArrayUtils.getSize(retVal) - 1));
                                    target.setValue(String.valueOf(tableSSDatas.get(k).getValue()));
                                    target.setKeyValue(tableSSDatas.get(k).getKeyValue());
                                    retVal.add(target);
                                }
                            }
                            break;
                        case SP_SCRIPTPARM_DATATYPE_INT:
                            CimPropertyInteger integer = (CimPropertyInteger) property;
                            tempParameter.setValueFlag(true);
                            tempParameter.setValue(String.valueOf(integer.getValue()));
                            break;
                        case SP_SCRIPTPARM_DATATYPE_REAL:
                            CimPropertyReal real = (CimPropertyReal) property;
                            tempParameter.setValueFlag(true);
                            tempParameter.setValue(String.valueOf(real.getValue()));
                            break;
                        case SP_SCRIPTPARM_DATATYPE_STRING:
                            CimPropertyString string = (CimPropertyString) property;
                            tempParameter.setValueFlag(true);
                            tempParameter.setValue(string.getValue());
                            break;
                        default:
                            break;
                    }
                    break;
                }
            }
        }));
        return retVal;
    }

    @Override
    public void scriptSetUserParameter(Infos.ObjCommon objCommon, String parameterClass, String identifier, List<Infos.UserParameterValue> parameters) {
        Validations.check(null == objCommon
                || CimStringUtils.isEmpty(parameterClass)
                || CimStringUtils.isEmpty(identifier)
                || null == parameters, retCodeConfig.getInvalidInputParam());
        CimPropertySet aPropertySet = this.getPropertySet(parameterClass, identifier);

        List<Infos.UserParameterValue> strParams = this.scriptGetUserParameter(objCommon, parameterClass, identifier);
        Optional.of(parameters).ifPresent(params -> params.forEach(parameterIn -> {
            long changeType = parameterIn.getChangeType();
            PropertyTypeEnum valType = null;
            switch (parameterIn.getDataType()) {
                case SP_SCRIPTPARM_DATATYPE_INT:
                    valType = PropertyTypeEnum.INTEGER_TYPE;
                    break;
                case SP_SCRIPTPARM_DATATYPE_REAL:
                    valType = PropertyTypeEnum.REAL_TYPE;
                    break;
                case SP_SCRIPTPARM_DATATYPE_STRING:
                    valType = PropertyTypeEnum.STRING_TYPE;
                    break;
                case SP_SCRIPTPARM_DATATYPE_SI:
                    valType = PropertyTypeEnum.TABLE_SI_TYPE;
                    break;
                case SP_SCRIPTPARM_DATATYPE_SR:
                    valType = PropertyTypeEnum.TABLE_SR_TYPE;
                    break;
                case SP_SCRIPTPARM_DATATYPE_SS:
                    valType = PropertyTypeEnum.TABLE_SS_TYPE;
                    break;
                default:
                    Validations.check(retCodeConfigEx.getInvalidDataType());
            }

            if (changeType == SP_PARVAL_ADD || changeType == SP_PARVAL_UPDATE) {
                log.info(" Change Type : SP_PARVAL_ADD or SP_PARVAL_UPDATE");
                //Compare with defined data type
                for (Infos.UserParameterValue parameterGet : strParams) {
                    if (CimStringUtils.equals(parameterIn.getParameterName(), parameterGet.getParameterName())) {
                        if (!CimStringUtils.equals(parameterIn.getDataType(), parameterGet.getDataType())) {
                            Validations.check(retCodeConfigEx.getInvalidDataType());
                        }
                    }
                }

                //Check if length greater than 512(string, table string, real, table real)
                if (valType == PropertyTypeEnum.STRING_TYPE || valType == PropertyTypeEnum.TABLE_SS_TYPE
                        || valType == PropertyTypeEnum.REAL_TYPE || valType == PropertyTypeEnum.TABLE_SR_TYPE) {
                    if (CimStringUtils.length(parameterIn.getValue()) > 512) {
                        Validations.check(retCodeConfig.getInvalidInputParam());
                    }
                }

                //Check if datavalue matches datatype
                if (valType == PropertyTypeEnum.INTEGER_TYPE || valType == PropertyTypeEnum.TABLE_SI_TYPE) {
                    log.info("Check INTEGER type data.");
                    // todo: we need this check ?
                }

                if (valType == PropertyTypeEnum.REAL_TYPE || valType == PropertyTypeEnum.TABLE_SR_TYPE) {
                    log.info("Check REAL type data.");
                    // todo: we need this check ?
                }

                CimPropertyBase aPropertyBase = aPropertySet.findPropertyNamed(parameterIn.getParameterName());

                //-------------------
                // Add Parameter
                //-------------------
                if (null == aPropertyBase && changeType == BizConstant.SP_PARVAL_ADD) {
                    // When new data is registered, specified parameterName must exist.
                    boolean bFound = false;
                    for (Infos.UserParameterValue parameterGet : strParams) {
                        if (CimStringUtils.equals(parameterIn.getParameterName(), parameterGet.getParameterName())) {
                            log.info("Found same ParameterName");
                            bFound = true;
                            break;
                        }
                    }
                    Validations.check(!bFound, retCodeConfig.getNotFoundObject());
                    aPropertyBase = aPropertySet.createPropertyNamed(parameterIn.getParameterName(), valType);
                }

                Validations.check(null == aPropertyBase, retCodeConfig.getNotFoundObject());

                assert valType != null;
                switch (valType) {
                    case TABLE_SI_TYPE:
                        CimPropertyTableSI tableSI = (CimPropertyTableSI) aPropertyBase;
                        if (CimStringUtils.isEmpty(parameterIn.getKeyValue())) {
                            log.error("no keyValue is specified");
                            Validations.check(retCodeConfigEx.getSomeDataValBlank());
                        }
                        tableSI.setValueNamed(Integer.parseInt(parameterIn.getValue()), parameterIn.getKeyValue());
                        break;
                    case TABLE_SR_TYPE:
                        CimPropertyTableSR tableSR = (CimPropertyTableSR) aPropertyBase;
                        if (CimStringUtils.isEmpty(parameterIn.getKeyValue())) {
                            log.error("no keyValue is specified");
                            Validations.check(retCodeConfigEx.getSomeDataValBlank());
                        }
                        tableSR.setValueNamed(Double.parseDouble(parameterIn.getValue()), parameterIn.getKeyValue());
                        break;
                    case TABLE_SS_TYPE:
                        CimPropertyTableSS tableSS = (CimPropertyTableSS) aPropertyBase;
                        if (CimStringUtils.isEmpty(parameterIn.getKeyValue())) {
                            log.error("no keyValue is specified");
                            Validations.check(retCodeConfigEx.getSomeDataValBlank());
                        }
                        tableSS.setValueNamed(parameterIn.getValue(), parameterIn.getKeyValue());
                        break;
                    case INTEGER_TYPE:
                        CimPropertyInteger integer = (CimPropertyInteger) aPropertyBase;
                        integer.setValue(Integer.parseInt(parameterIn.getValue()));
                        break;
                    case REAL_TYPE:
                        CimPropertyReal real = (CimPropertyReal) aPropertyBase;
                        real.setValue(Double.parseDouble(parameterIn.getValue()));
                        break;
                    case STRING_TYPE:
                        CimPropertyString string = (CimPropertyString) aPropertyBase;
                        string.setValue(parameterIn.getValue());
                        break;
                }
            }

            if (changeType == BizConstant.SP_PARVAL_DELETE) {
                CimPropertyBase aPropertyBase = aPropertySet.findPropertyNamed(parameterIn.getParameterName());
                Validations.check(null == aPropertyBase, retCodeConfig.getNotFoundObject());
                assert valType != null;
                switch (valType) {
                    case TABLE_SI_TYPE:
                        CimPropertyTableSI tableSI = (CimPropertyTableSI) aPropertyBase;
                        tableSI.removeValueNamed(parameterIn.getKeyValue());
                        List<Property.TableSIData> valueSeq = tableSI.allValues();
                        if (CimArrayUtils.isEmpty(valueSeq)) {
                            aPropertySet.removeProperty(aPropertyBase);
                        }
                        break;
                    case TABLE_SR_TYPE:
                        CimPropertyTableSR tableSR = (CimPropertyTableSR) aPropertyBase;
                        tableSR.removeValueNamed(parameterIn.getKeyValue());
                        List<Property.TableSRData> tableSRData = tableSR.allValues();
                        if (CimArrayUtils.isEmpty(tableSRData)) {
                            aPropertySet.removeProperty(aPropertyBase);
                        }
                        break;
                    case TABLE_SS_TYPE:
                        CimPropertyTableSS tableSS = (CimPropertyTableSS) aPropertyBase;
                        tableSS.removeValueNamed(parameterIn.getKeyValue());
                        List<Property.TableSSData> tableSSData = tableSS.allValues();
                        if (CimArrayUtils.isEmpty(tableSSData)) {
                            aPropertySet.removeProperty(aPropertyBase);
                        }
                        break;
                    default:
                        aPropertySet.removeProperty(aPropertyBase);
                        break;

                }
            }

            if (changeType == BizConstant.SP_PARVAL_NOCHANGE) {
                log.info("changeType is SP_ParVal_NoChange");
            }
        }));
    }


    private CimPropertySet getPropertySet(String parameterClass, String identifier) {
        CimPropertySet aPropertySet = null;
        switch (parameterClass) {
            case SP_SCRIPTPARM_CLASS_LOT:
                CimLot aLot = productManager.findLotNamed(identifier);
                Validations.check(aLot == null, retCodeConfig.getNotFoundLot());
                aPropertySet = aLot.getPropertySet();
                break;
            case SP_SCRIPTPARM_CLASS_EQP:
                CimMachine aMachine = machineManager.findMachineNamed(identifier);
                Validations.check(aMachine == null, retCodeConfig.getNotFoundEquipment());
                aPropertySet = aMachine.getPropertySet();
                break;
            case SP_SCRIPTPARM_CLASS_LOGICALRECIPE:
                CimLogicalRecipe aLogicalRecipe = recipeManager.findLogicalRecipeNamed(identifier);
                Validations.check(aLogicalRecipe == null, retCodeConfig.getNotFoundLogicalRecipe());
                aPropertySet = aLogicalRecipe.getPropertySet();
                break;
            case SP_SCRIPTPARM_CLASS_RECIPE:
                CimMachineRecipe aMachineRecipe = recipeManager.findMachineRecipeNamed(identifier);
                Validations.check(aMachineRecipe == null, retCodeConfig.getNotFoundMachineRecipe());
                aPropertySet = aMachineRecipe.getPropertySet();
                break;
            case SP_SCRIPTPARM_CLASS_RETICLE:
                ProcessDurable aPrDr = durableManager.findProcessDurableNamed(identifier);
                Validations.check(aPrDr == null, retCodeConfig.getNotFoundDurable());
                aPropertySet = ((CimProcessDurable) aPrDr).getPropertySet();
                break;
            case SP_SCRIPTPARM_CLASS_FIXTURE:
                aPrDr = durableManager.findProcessDurableNamed(identifier);
                Validations.check(aPrDr == null, retCodeConfig.getNotFoundDurable());
                aPropertySet = ((CimProcessDurable) aPrDr).getPropertySet();
                break;
            case SP_SCRIPTPARM_CLASS_ROUTE:
                CimProcessDefinition processDefinition = processDefinitionManager.findMainProcessDefinitionNamed(identifier);
                Validations.check(processDefinition == null, retCodeConfig.getNotFoundProcessDefinition());
                aPropertySet = processDefinition.getPropertySet();
                break;
            case SP_SCRIPTPARM_CLASS_PROCEESS:
                processDefinition = processDefinitionManager.findProcessDefinitionNamed(identifier);
                Validations.check(processDefinition == null, retCodeConfig.getNotFoundProcessDefinition());
                aPropertySet = processDefinition.getPropertySet();
                break;
            case SP_SCRIPTPARM_CLASS_PRODUCT:
                CimProductSpecification productSpecification = productSpecificationManager.findProductSpecificationNamed(identifier);
                Validations.check(productSpecification == null, retCodeConfig.getNotFoundProductSpec());
                aPropertySet = productSpecification.getPropertySet();
                break;
            case SP_SCRIPTPARM_CLASS_TECH:
                CimTechnology technology = productSpecificationManager.findTechnologyNamed(identifier);
                Validations.check(technology == null, retCodeConfig.getNotFoundTechnology());
                aPropertySet = technology.getPropertySet();
                break;
            case SP_SCRIPTPARM_CLASS_USER:
                CimPerson person = personManager.findPersonNamed(identifier);
                Validations.check(person == null, retCodeConfig.getNotFoundPerson());
                aPropertySet = person.getPropertySet();
                break;
            case SP_SCRIPTPARM_CLASS_FACTORY:
                aPropertySet = mesFactory.getPropertySet();
                break;
            case SP_SCRIPTPARM_CLASS_WAFER:
                Product product = productManager.findProductNamed(identifier);
                Validations.check(product == null, retCodeConfig.getNotFoundProductSpec());
                aPropertySet = ((CimWafer) product).getPropertySet();
                break;
            case SP_SCRIPTPARM_CLASS_CARRIER:
                Cassette cassette = durableManager.findCassetteNamed(identifier);
                Validations.check(cassette == null, retCodeConfig.getNotFoundCassette());
                aPropertySet = ((CimCassette) cassette).getPropertySet();
                break;
            case SP_SCRIPTPARM_CLASS_RETICLEPOD:
                CimReticlePod reticlePod = durableManager.findReticlePodNamed(identifier);
                Validations.check(reticlePod == null, retCodeConfig.getNotFoundReticlePod());
                aPropertySet = reticlePod.getPropertySet();
                break;
            default:
                Validations.check(retCodeConfig.getNotFoundObject());
        }
        return aPropertySet;
    }
}
