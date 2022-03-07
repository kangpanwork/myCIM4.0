/**
 * DCItem.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.fa.cim;

public class DCItem implements java.io.Serializable {

    private String   dataCollectionItemName;

    private String   dataCollectionMode;

    private String   dataCollectionUnit;

    private String   dataType;

    private String   itemType;

    private String   measurementType;

    private String   waferId;

    private String   siteID;

    private String   bumpID;

    private String   waferPosition;

    private String   sitePosition;

    private String   bumpPosition;

    private boolean            historyRequiredFlag;

    private String   calculationType;

    private String   calculationExpression;

    private String   dataValue;

    private String   targetValue;

    private String   specCheckResult;

    private String   actionCode;

    private Object   siInfo;

    private Object[] defectCodeList;

    private Object[] defectCountList;

    private String   recipeId;

    public DCItem(){
    }

    public DCItem(String dataCollectionItemName, String dataCollectionMode,
                  String dataCollectionUnit, String dataType, String itemType,
                  String measurementType, String waferId, String siteID,
                  String bumpID, String waferPosition, String sitePosition,
                  String bumpPosition, boolean historyRequiredFlag, String calculationType,
                  String calculationExpression, String dataValue, String targetValue,
                  String specCheckResult, String actionCode, Object siInfo,
                  Object[] defectCodeList, Object[] defectCountList){
        this.dataCollectionItemName = dataCollectionItemName;
        this.dataCollectionMode = dataCollectionMode;
        this.dataCollectionUnit = dataCollectionUnit;
        this.dataType = dataType;
        this.itemType = itemType;
        this.measurementType = measurementType;
        this.waferId = waferId;
        this.siteID = siteID;
        this.bumpID = bumpID;
        this.waferPosition = waferPosition;
        this.sitePosition = sitePosition;
        this.bumpPosition = bumpPosition;
        this.historyRequiredFlag = historyRequiredFlag;
        this.calculationType = calculationType;
        this.calculationExpression = calculationExpression;
        this.dataValue = dataValue;
        this.targetValue = targetValue;
        this.specCheckResult = specCheckResult;
        this.actionCode = actionCode;
        this.siInfo = siInfo;
        this.defectCodeList = defectCodeList;
        this.defectCountList = defectCountList;
    }

    /**
     * Gets the dataCollectionItemName value for this DCItem.
     * 
     * @return dataCollectionItemName
     */
    public String getDataCollectionItemName() {
        return dataCollectionItemName;
    }

    /**
     * Sets the dataCollectionItemName value for this DCItem.
     * 
     * @param dataCollectionItemName
     */
    public void setDataCollectionItemName(String dataCollectionItemName) {
        this.dataCollectionItemName = dataCollectionItemName;
    }

    /**
     * Gets the dataCollectionMode value for this DCItem.
     * 
     * @return dataCollectionMode
     */
    public String getDataCollectionMode() {
        return dataCollectionMode;
    }

    /**
     * Sets the dataCollectionMode value for this DCItem.
     * 
     * @param dataCollectionMode
     */
    public void setDataCollectionMode(String dataCollectionMode) {
        this.dataCollectionMode = dataCollectionMode;
    }

    /**
     * Gets the dataCollectionUnit value for this DCItem.
     * 
     * @return dataCollectionUnit
     */
    public String getDataCollectionUnit() {
        return dataCollectionUnit;
    }

    /**
     * Sets the dataCollectionUnit value for this DCItem.
     * 
     * @param dataCollectionUnit
     */
    public void setDataCollectionUnit(String dataCollectionUnit) {
        this.dataCollectionUnit = dataCollectionUnit;
    }

    /**
     * Gets the dataType value for this DCItem.
     * 
     * @return dataType
     */
    public String getDataType() {
        return dataType;
    }

    /**
     * Sets the dataType value for this DCItem.
     * 
     * @param dataType
     */
    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    /**
     * Gets the itemType value for this DCItem.
     * 
     * @return itemType
     */
    public String getItemType() {
        return itemType;
    }

    /**
     * Sets the itemType value for this DCItem.
     * 
     * @param itemType
     */
    public void setItemType(String itemType) {
        this.itemType = itemType;
    }

    /**
     * Gets the measurementType value for this DCItem.
     * 
     * @return measurementType
     */
    public String getMeasurementType() {
        return measurementType;
    }

    /**
     * Sets the measurementType value for this DCItem.
     * 
     * @param measurementType
     */
    public void setMeasurementType(String measurementType) {
        this.measurementType = measurementType;
    }

    /**
     * Gets the waferId value for this DCItem.
     * 
     * @return waferId
     */
    public String getWaferId() {
        return waferId;
    }

    /**
     * Sets the waferId value for this DCItem.
     * 
     * @param waferId
     */
    public void setWaferId(String waferId) {
        this.waferId = waferId;
    }

    /**
     * Gets the siteID value for this DCItem.
     * 
     * @return siteID
     */
    public String getSiteID() {
        return siteID;
    }

    /**
     * Sets the siteID value for this DCItem.
     * 
     * @param siteID
     */
    public void setSiteID(String siteID) {
        this.siteID = siteID;
    }

    /**
     * Gets the bumpID value for this DCItem.
     * 
     * @return bumpID
     */
    public String getBumpID() {
        return bumpID;
    }

    /**
     * Sets the bumpID value for this DCItem.
     * 
     * @param bumpID
     */
    public void setBumpID(String bumpID) {
        this.bumpID = bumpID;
    }

    /**
     * Gets the waferPosition value for this DCItem.
     * 
     * @return waferPosition
     */
    public String getWaferPosition() {
        return waferPosition;
    }

    /**
     * Sets the waferPosition value for this DCItem.
     * 
     * @param waferPosition
     */
    public void setWaferPosition(String waferPosition) {
        this.waferPosition = waferPosition;
    }

    /**
     * Gets the sitePosition value for this DCItem.
     * 
     * @return sitePosition
     */
    public String getSitePosition() {
        return sitePosition;
    }

    /**
     * Sets the sitePosition value for this DCItem.
     * 
     * @param sitePosition
     */
    public void setSitePosition(String sitePosition) {
        this.sitePosition = sitePosition;
    }

    /**
     * Gets the bumpPosition value for this DCItem.
     * 
     * @return bumpPosition
     */
    public String getBumpPosition() {
        return bumpPosition;
    }

    /**
     * Sets the bumpPosition value for this DCItem.
     * 
     * @param bumpPosition
     */
    public void setBumpPosition(String bumpPosition) {
        this.bumpPosition = bumpPosition;
    }

    /**
     * Gets the historyRequiredFlag value for this DCItem.
     * 
     * @return historyRequiredFlag
     */
    public boolean isHistoryRequiredFlag() {
        return historyRequiredFlag;
    }

    /**
     * Sets the historyRequiredFlag value for this DCItem.
     * 
     * @param historyRequiredFlag
     */
    public void setHistoryRequiredFlag(boolean historyRequiredFlag) {
        this.historyRequiredFlag = historyRequiredFlag;
    }

    /**
     * Gets the calculationType value for this DCItem.
     * 
     * @return calculationType
     */
    public String getCalculationType() {
        return calculationType;
    }

    /**
     * Sets the calculationType value for this DCItem.
     * 
     * @param calculationType
     */
    public void setCalculationType(String calculationType) {
        this.calculationType = calculationType;
    }

    /**
     * Gets the calculationExpression value for this DCItem.
     * 
     * @return calculationExpression
     */
    public String getCalculationExpression() {
        return calculationExpression;
    }

    /**
     * Sets the calculationExpression value for this DCItem.
     * 
     * @param calculationExpression
     */
    public void setCalculationExpression(String calculationExpression) {
        this.calculationExpression = calculationExpression;
    }

    /**
     * Gets the dataValue value for this DCItem.
     * 
     * @return dataValue
     */
    public String getDataValue() {
        return dataValue;
    }

    /**
     * Sets the dataValue value for this DCItem.
     * 
     * @param dataValue
     */
    public void setDataValue(String dataValue) {
        this.dataValue = dataValue;
    }

    /**
     * Gets the targetValue value for this DCItem.
     * 
     * @return targetValue
     */
    public String getTargetValue() {
        return targetValue;
    }

    /**
     * Sets the targetValue value for this DCItem.
     * 
     * @param targetValue
     */
    public void setTargetValue(String targetValue) {
        this.targetValue = targetValue;
    }

    /**
     * Gets the specCheckResult value for this DCItem.
     * 
     * @return specCheckResult
     */
    public String getSpecCheckResult() {
        return specCheckResult;
    }

    /**
     * Sets the specCheckResult value for this DCItem.
     * 
     * @param specCheckResult
     */
    public void setSpecCheckResult(String specCheckResult) {
        this.specCheckResult = specCheckResult;
    }

    /**
     * Gets the actionCode value for this DCItem.
     * 
     * @return actionCode
     */
    public String getActionCode() {
        return actionCode;
    }

    /**
     * Sets the actionCode value for this DCItem.
     * 
     * @param actionCode
     */
    public void setActionCode(String actionCode) {
        this.actionCode = actionCode;
    }

    /**
     * Gets the siInfo value for this DCItem.
     * 
     * @return siInfo
     */
    public Object getSiInfo() {
        return siInfo;
    }

    /**
     * Sets the siInfo value for this DCItem.
     * 
     * @param siInfo
     */
    public void setSiInfo(Object siInfo) {
        this.siInfo = siInfo;
    }

    /**
     * Gets the defectCodeList value for this DCItem.
     * 
     * @return defectCodeList
     */
    public Object[] getDefectCodeList() {
        return defectCodeList;
    }

    /**
     * Sets the defectCodeList value for this DCItem.
     * 
     * @param defectCodeList
     */
    public void setDefectCodeList(Object[] defectCodeList) {
        this.defectCodeList = defectCodeList;
    }

    /**
     * Gets the defectCountList value for this DCItem.
     * 
     * @return defectCountList
     */
    public Object[] getDefectCountList() {
        return defectCountList;
    }

    /**
     * Sets the defectCountList value for this DCItem.
     * 
     * @param defectCountList
     */
    public void setDefectCountList(Object[] defectCountList) {
        this.defectCountList = defectCountList;
    }

    private Object __equalsCalc = null;

    public synchronized boolean equals(Object obj) {
        if (!(obj instanceof DCItem)) return false;
        DCItem other = (DCItem) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true
                  && ((this.dataCollectionItemName == null && other.getDataCollectionItemName() == null) || (this.dataCollectionItemName != null && this.dataCollectionItemName.equals(other.getDataCollectionItemName())))
                  && ((this.dataCollectionMode == null && other.getDataCollectionMode() == null) || (this.dataCollectionMode != null && this.dataCollectionMode.equals(other.getDataCollectionMode())))
                  && ((this.dataCollectionUnit == null && other.getDataCollectionUnit() == null) || (this.dataCollectionUnit != null && this.dataCollectionUnit.equals(other.getDataCollectionUnit())))
                  && ((this.dataType == null && other.getDataType() == null) || (this.dataType != null && this.dataType.equals(other.getDataType())))
                  && ((this.itemType == null && other.getItemType() == null) || (this.itemType != null && this.itemType.equals(other.getItemType())))
                  && ((this.measurementType == null && other.getMeasurementType() == null) || (this.measurementType != null && this.measurementType.equals(other.getMeasurementType())))
                  && ((this.waferId == null && other.getWaferId() == null) || (this.waferId != null && this.waferId.equals(other.getWaferId())))
                  && ((this.siteID == null && other.getSiteID() == null) || (this.siteID != null && this.siteID.equals(other.getSiteID())))
                  && ((this.bumpID == null && other.getBumpID() == null) || (this.bumpID != null && this.bumpID.equals(other.getBumpID())))
                  && ((this.waferPosition == null && other.getWaferPosition() == null) || (this.waferPosition != null && this.waferPosition.equals(other.getWaferPosition())))
                  && ((this.sitePosition == null && other.getSitePosition() == null) || (this.sitePosition != null && this.sitePosition.equals(other.getSitePosition())))
                  && ((this.bumpPosition == null && other.getBumpPosition() == null) || (this.bumpPosition != null && this.bumpPosition.equals(other.getBumpPosition())))
                  && this.historyRequiredFlag == other.isHistoryRequiredFlag()
                  && ((this.calculationType == null && other.getCalculationType() == null) || (this.calculationType != null && this.calculationType.equals(other.getCalculationType())))
                  && ((this.calculationExpression == null && other.getCalculationExpression() == null) || (this.calculationExpression != null && this.calculationExpression.equals(other.getCalculationExpression())))
                  && ((this.dataValue == null && other.getDataValue() == null) || (this.dataValue != null && this.dataValue.equals(other.getDataValue())))
                  && ((this.targetValue == null && other.getTargetValue() == null) || (this.targetValue != null && this.targetValue.equals(other.getTargetValue())))
                  && ((this.specCheckResult == null && other.getSpecCheckResult() == null) || (this.specCheckResult != null && this.specCheckResult.equals(other.getSpecCheckResult())))
                  && ((this.actionCode == null && other.getActionCode() == null) || (this.actionCode != null && this.actionCode.equals(other.getActionCode())))
                  && ((this.siInfo == null && other.getSiInfo() == null) || (this.siInfo != null && this.siInfo.equals(other.getSiInfo())))
                  && ((this.defectCodeList == null && other.getDefectCodeList() == null) || (this.defectCodeList != null && java.util.Arrays.equals(this.defectCodeList,
                                                                                                                                                    other.getDefectCodeList())))
                  && ((this.defectCountList == null && other.getDefectCountList() == null) || (this.defectCountList != null && java.util.Arrays.equals(this.defectCountList,
                                                                                                                                                       other.getDefectCountList())));
        __equalsCalc = null;
        return _equals;
    }

    private boolean __hashCodeCalc = false;

    public synchronized int hashCode() {
        if (__hashCodeCalc) {
            return 0;
        }
        __hashCodeCalc = true;
        int _hashCode = 1;
        if (getDataCollectionItemName() != null) {
            _hashCode += getDataCollectionItemName().hashCode();
        }
        if (getDataCollectionMode() != null) {
            _hashCode += getDataCollectionMode().hashCode();
        }
        if (getDataCollectionUnit() != null) {
            _hashCode += getDataCollectionUnit().hashCode();
        }
        if (getDataType() != null) {
            _hashCode += getDataType().hashCode();
        }
        if (getItemType() != null) {
            _hashCode += getItemType().hashCode();
        }
        if (getMeasurementType() != null) {
            _hashCode += getMeasurementType().hashCode();
        }
        if (getWaferId() != null) {
            _hashCode += getWaferId().hashCode();
        }
        if (getSiteID() != null) {
            _hashCode += getSiteID().hashCode();
        }
        if (getBumpID() != null) {
            _hashCode += getBumpID().hashCode();
        }
        if (getWaferPosition() != null) {
            _hashCode += getWaferPosition().hashCode();
        }
        if (getSitePosition() != null) {
            _hashCode += getSitePosition().hashCode();
        }
        if (getBumpPosition() != null) {
            _hashCode += getBumpPosition().hashCode();
        }
        _hashCode += (isHistoryRequiredFlag() ? Boolean.TRUE : Boolean.FALSE).hashCode();
        if (getCalculationType() != null) {
            _hashCode += getCalculationType().hashCode();
        }
        if (getCalculationExpression() != null) {
            _hashCode += getCalculationExpression().hashCode();
        }
        if (getDataValue() != null) {
            _hashCode += getDataValue().hashCode();
        }
        if (getTargetValue() != null) {
            _hashCode += getTargetValue().hashCode();
        }
        if (getSpecCheckResult() != null) {
            _hashCode += getSpecCheckResult().hashCode();
        }
        if (getActionCode() != null) {
            _hashCode += getActionCode().hashCode();
        }
        if (getSiInfo() != null) {
            _hashCode += getSiInfo().hashCode();
        }
        if (getDefectCodeList() != null) {
            for (int i = 0; i < java.lang.reflect.Array.getLength(getDefectCodeList()); i++) {
                Object obj = java.lang.reflect.Array.get(getDefectCodeList(), i);
                if (obj != null && !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getDefectCountList() != null) {
            for (int i = 0; i < java.lang.reflect.Array.getLength(getDefectCountList()); i++) {
                Object obj = java.lang.reflect.Array.get(getDefectCountList(), i);
                if (obj != null && !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc = new org.apache.axis.description.TypeDesc(
                                                                                                            DCItem.class,
                                                                                                            true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://tempuri.org/", "DCItem"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("dataCollectionItemName");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "DataCollectionItemName"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("dataCollectionMode");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "DataCollectionMode"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("dataCollectionUnit");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "DataCollectionUnit"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("dataType");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "DataType"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("itemType");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "ItemType"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("measurementType");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "MeasurementType"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("waferId");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "WaferId"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("siteID");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "SiteID"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("bumpID");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "BumpID"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("waferPosition");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "WaferPosition"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("sitePosition");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "SitePosition"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("bumpPosition");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "BumpPosition"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("historyRequiredFlag");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "HistoryRequiredFlag"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("calculationType");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "CalculationType"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("calculationExpression");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "CalculationExpression"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("dataValue");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "DataValue"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("targetValue");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "TargetValue"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("specCheckResult");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "SpecCheckResult"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("actionCode");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "ActionCode"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("siInfo");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "SiInfo"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "anyType"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("defectCodeList");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "DefectCodeList"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "anyType"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        elemField.setItemQName(new javax.xml.namespace.QName("http://tempuri.org/", "anyType"));
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("defectCountList");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "DefectCountList"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "anyType"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        elemField.setItemQName(new javax.xml.namespace.QName("http://tempuri.org/", "anyType"));
        typeDesc.addFieldDesc(elemField);
    }

    /**
     * Return type metadata object
     */
    public static org.apache.axis.description.TypeDesc getTypeDesc() {
        return typeDesc;
    }

    /**
     * Get Custom Serializer
     */
    public static org.apache.axis.encoding.Serializer getSerializer(String mechType,
                                                                    Class _javaType,
                                                                    javax.xml.namespace.QName _xmlType) {
        return new org.apache.axis.encoding.ser.BeanSerializer(_javaType, _xmlType, typeDesc);
    }

    /**
     * Get Custom Deserializer
     */
    public static org.apache.axis.encoding.Deserializer getDeserializer(String mechType,
                                                                        Class _javaType,
                                                                        javax.xml.namespace.QName _xmlType) {
        return new org.apache.axis.encoding.ser.BeanDeserializer(_javaType, _xmlType, typeDesc);
    }

    public String getRecipeId() {
        return recipeId;
    }

    public void setRecipeId(String recipeId) {
        this.recipeId = recipeId;
    }

}
