package com.fa.cim.pcs.entity;

import com.fa.cim.common.exception.PCSException;
import com.fa.cim.common.utils.CimStringUtils;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.config.RetCodeConfigEx;
import com.fa.cim.newcore.bo.abstractgroup.*;
import com.fa.cim.newcore.enums.PcsEnums;
import com.fa.cim.newcore.enums.PropertyTypeEnum;
import com.fa.cim.newcore.functions.ExtendableBO;
import com.fa.cim.pcs.attribute.property.*;
import com.fa.cim.pcs.engine.ScriptEntityFactory;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class ScriptEntity<B extends ExtendableBO> {

    private static final String UNDEFINED = "undefined";

    protected B bizObject;

    /**
     * This factory is used to generate a ScriptEntity instance.
     */
    @Autowired
    protected ScriptEntityFactory factory;

    @Autowired
    private PropertySetManager propertySetManager;

    @Autowired
    protected RetCodeConfigEx retCodeConfigEx;

    @Autowired
    protected RetCodeConfig retCodeConfig;

    private CimPropertySet propertySet;

    ScriptEntity(B bizObject) {
        this.bizObject = bizObject;
        this.propertySet = bizObject.getPropertySet();
    }

    @Override
    public String toString() {
        return bizObject.toString();
    }

    private CimPcsUserDefVariables findPcsUserDefVariableNamed(String name) {
        Class<? extends ExtendableBO> classType = bizObject.getClass();
        String keyName = PcsEnums.getKeyFor(classType);
        String key = String.format("%s;%s", name, keyName);
        CimPcsUserDefVariables pcsUserDefVariable = propertySetManager.findPcsUserDefVariableNamed(key);
        PCSException.check(null == pcsUserDefVariable, retCodeConfigEx.getPcsVariableForSpecTypeUndefined(), name, PcsEnums.getKeyFor(classType));
        return pcsUserDefVariable;
    }

    /**
     * get an extensive value of a name as String
     *
     * @param name the name of the property
     * @return {@link StringProperty}
     */
    public StringProperty stringValue(String name) {
        PCSException.check(CimStringUtils.isEmpty(name), retCodeConfigEx.getPcsVariableNameIsNull());
        PCSException.check(CimStringUtils.equals(name, UNDEFINED), retCodeConfigEx.getPcsVariableUndefined(), name);
        CimPropertyBase propertyNamed = propertySet.findPropertyNamed(name);
        if (null != propertyNamed) {
            PCSException.check(!(propertyNamed instanceof CimPropertyString), retCodeConfigEx.getPcsVariableTypeNotString(), name);
            assert propertyNamed instanceof CimPropertyString;
            return factory.generateScriptProperty(StringProperty.class, (CimPropertyString) propertyNamed);
        } else {
            CimPcsUserDefVariables pcsUserDefVariable = this.findPcsUserDefVariableNamed(name);
            CimPropertyString property = (CimPropertyString) propertySet.createPropertyNamed(name, PropertyTypeEnum.STRING_TYPE);
            String value = pcsUserDefVariable.getValue();
            if (CimStringUtils.isNotEmpty(value)) {
                property.setValue(value);
            }
            return factory.generateScriptProperty(StringProperty.class, property);
        }
    }

    /**
     * get an extensive value of a name as Integer
     *
     * @param name the name of the property
     * @return {@link IntegerProperty}
     */
    public IntegerProperty intValue(String name) {
        PCSException.check(CimStringUtils.isEmpty(name), retCodeConfigEx.getPcsVariableNameIsNull());
        PCSException.check(CimStringUtils.equals(name, UNDEFINED), retCodeConfigEx.getPcsVariableUndefined(), name);
        CimPropertyBase propertyNamed = propertySet.findPropertyNamed(name);
        if (null != propertyNamed) {
            PCSException.check(!(propertyNamed instanceof CimPropertyInteger), retCodeConfigEx.getPcsVariableTypeNotInteger(), name);
            assert propertyNamed instanceof CimPropertyInteger;
            return factory.generateScriptProperty(IntegerProperty.class, (CimPropertyInteger) propertyNamed);
        } else {
            CimPcsUserDefVariables pcsUserDefVariable = this.findPcsUserDefVariableNamed(name);
            CimPropertyInteger property = (CimPropertyInteger) propertySet.createPropertyNamed(name, PropertyTypeEnum.INTEGER_TYPE);
            String value = pcsUserDefVariable.getValue();
            if (CimStringUtils.isNotEmpty(value)) {
                property.setValue(Integer.valueOf(value));
            }
            return factory.generateScriptProperty(IntegerProperty.class, property);
        }
    }

    /**
     * get an extensive value of a name as Decimal
     *
     * @param name the name of the property
     * @return {@link DecimalProperty}
     */
    public DecimalProperty decimalValue(String name) {
        PCSException.check(CimStringUtils.isEmpty(name), retCodeConfigEx.getPcsVariableNameIsNull());
        PCSException.check(CimStringUtils.equals(name, UNDEFINED), retCodeConfigEx.getPcsVariableUndefined(), name);
        CimPropertyBase propertyNamed = propertySet.findPropertyNamed(name);
        if (null != propertyNamed) {
            PCSException.check(!(propertyNamed instanceof CimPropertyReal), retCodeConfigEx.getPcsVariableTypeNotDecimal(), name);
            assert propertyNamed instanceof CimPropertyReal;
            return factory.generateScriptProperty(DecimalProperty.class, (CimPropertyReal) propertyNamed);
        } else {
            CimPcsUserDefVariables pcsUserDefVariable = this.findPcsUserDefVariableNamed(name);
            CimPropertyReal property = (CimPropertyReal) propertySet.createPropertyNamed(name, PropertyTypeEnum.REAL_TYPE);
            String value = pcsUserDefVariable.getValue();
            if (CimStringUtils.isNotEmpty(value)) {
                property.setValue(Double.valueOf(value));
            }
            return factory.generateScriptProperty(DecimalProperty.class, property);
        }
    }

    /**
     * get an extensive value of a name as a Map of String
     *
     * @param name the name of the property
     * @return {@link StringMap}
     */
    public StringMap stringMap(String name) {
        PCSException.check(CimStringUtils.isEmpty(name), retCodeConfigEx.getPcsVariableNameIsNull());
        PCSException.check(CimStringUtils.equals(name, UNDEFINED), retCodeConfigEx.getPcsVariableUndefined(), name);
        CimPropertyBase propertyNamed = propertySet.findPropertyNamed(name);
        if (null != propertyNamed) {
            PCSException.check(!(propertyNamed instanceof CimPropertyTableSS), retCodeConfigEx.getPcsVariableTypeNotStringMap(), name);
            assert propertyNamed instanceof CimPropertyTableSS;
            return factory.generateScriptProperty(StringMap.class, (CimPropertyTableSS) propertyNamed);
        } else {
            CimPcsUserDefVariables pcsUserDefVariable = this.findPcsUserDefVariableNamed(name);
            CimPropertyTableSS property = (CimPropertyTableSS) propertySet.createPropertyNamed(name, PropertyTypeEnum.TABLE_SS_TYPE);
            String value = pcsUserDefVariable.getValue();
            if (CimStringUtils.isNotEmpty(value)) {
                property.setValueNamed(value, name);
            }
            return factory.generateScriptProperty(StringMap.class, property);
        }
    }

    /**
     * get an extensive value of a name as a Map of Integer
     *
     * @param name the name of the property
     * @return {@link IntegerMap}
     */
    public IntegerMap intMap(String name) {
        PCSException.check(CimStringUtils.isEmpty(name), retCodeConfigEx.getPcsVariableNameIsNull());
        PCSException.check(CimStringUtils.equals(name, UNDEFINED), retCodeConfigEx.getPcsVariableUndefined(), name);
        CimPropertyBase propertyNamed = propertySet.findPropertyNamed(name);
        if (null != propertyNamed) {
            PCSException.check(!(propertyNamed instanceof CimPropertyTableSI), retCodeConfigEx.getPcsVariableTypeNotIntegerMap(), name);
            assert propertyNamed instanceof CimPropertyTableSI;
            return factory.generateScriptProperty(IntegerMap.class, (CimPropertyTableSI) propertyNamed);
        } else {
            CimPcsUserDefVariables pcsUserDefVariable = this.findPcsUserDefVariableNamed(name);
            CimPropertyTableSI property = (CimPropertyTableSI) propertySet.createPropertyNamed(name, PropertyTypeEnum.TABLE_SI_TYPE);
            String value = pcsUserDefVariable.getValue();
            if (CimStringUtils.isNotEmpty(value)) {
                property.setValueNamed(Integer.valueOf(value), name);
            }
            return factory.generateScriptProperty(IntegerMap.class, property);
        }

    }

    /**
     * get an extensive value of a name as a Map of Decimal
     *
     * @param name the name of the property
     * @return {@link DecimalMap}
     */
    public DecimalMap decimalMap(String name) {
        PCSException.check(CimStringUtils.isEmpty(name), retCodeConfigEx.getPcsVariableNameIsNull());
        PCSException.check(CimStringUtils.equals(name, UNDEFINED), retCodeConfigEx.getPcsVariableUndefined(), name);
        CimPropertyBase propertyNamed = propertySet.findPropertyNamed(name);
        if (null != propertyNamed) {
            PCSException.check(!(propertyNamed instanceof CimPropertyTableSR), retCodeConfigEx.getPcsVariableTypeNotDecimalMap(), name);
            assert propertyNamed instanceof CimPropertyTableSR;
            return factory.generateScriptProperty(DecimalMap.class, (CimPropertyTableSR) propertyNamed);
        } else {
            CimPcsUserDefVariables pcsUserDefVariable = this.findPcsUserDefVariableNamed(name);
            CimPropertyTableSR property = (CimPropertyTableSR) propertySet.createPropertyNamed(name, PropertyTypeEnum.TABLE_SR_TYPE);
            String value = pcsUserDefVariable.getValue();
            if (CimStringUtils.isNotEmpty(value)) {
                property.setValueNamed(Double.valueOf(value), name);
            }
            return factory.generateScriptProperty(DecimalMap.class, property);
        }
    }

}
