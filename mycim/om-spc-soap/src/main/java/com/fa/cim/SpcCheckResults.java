/**
 * SpcCheckResults.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.fa.cim;

import java.util.List;

public class SpcCheckResults implements java.io.Serializable {

    private String errorCode;

    private String errorMessage;

    private boolean          lotHold;

    private String lotID;

    private String reasonCode;

    private String alarmID;

    private List<HoldEquipmentModel> holdEquipmentModel;

    public SpcCheckResults(){
    }

    public SpcCheckResults(String errorCode, String errorMessage, boolean lotHold,
                           String lotID, String reasonCode, String alarmID){
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.lotHold = lotHold;
        this.lotID = lotID;
        this.reasonCode = reasonCode;
        this.alarmID = alarmID;
    }

    /**
     * Gets the errorCode value for this SpcCheckResults.
     * 
     * @return errorCode
     */
    public String getErrorCode() {
        return errorCode;
    }

    /**
     * Sets the errorCode value for this SpcCheckResults.
     * 
     * @param errorCode
     */
    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    /**
     * Gets the errorMessage value for this SpcCheckResults.
     * 
     * @return errorMessage
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Sets the errorMessage value for this SpcCheckResults.
     * 
     * @param errorMessage
     */
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    /**
     * Gets the lotHold value for this SpcCheckResults.
     * 
     * @return lotHold
     */
    public boolean isLotHold() {
        return lotHold;
    }

    /**
     * Sets the lotHold value for this SpcCheckResults.
     * 
     * @param lotHold
     */
    public void setLotHold(boolean lotHold) {
        this.lotHold = lotHold;
    }

    /**
     * Gets the lotID value for this SpcCheckResults.
     * 
     * @return lotID
     */
    public String getLotID() {
        return lotID;
    }

    /**
     * Sets the lotID value for this SpcCheckResults.
     * 
     * @param lotID
     */
    public void setLotID(String lotID) {
        this.lotID = lotID;
    }

    /**
     * Gets the reasonCode value for this SpcCheckResults.
     * 
     * @return reasonCode
     */
    public String getReasonCode() {
        return reasonCode;
    }

    /**
     * Sets the reasonCode value for this SpcCheckResults.
     * 
     * @param reasonCode
     */
    public void setReasonCode(String reasonCode) {
        this.reasonCode = reasonCode;
    }

    /**
     * Gets the alarmID value for this SpcCheckResults.
     * 
     * @return alarmID
     */
    public String getAlarmID() {
        return alarmID;
    }

    /**
     * Sets the alarmID value for this SpcCheckResults.
     * 
     * @param alarmID
     */
    public void setAlarmID(String alarmID) {
        this.alarmID = alarmID;
    }

    private Object __equalsCalc = null;

    public synchronized boolean equals(Object obj) {
        if (obj == null) return false;
        if (!(obj instanceof SpcCheckResults)) return false;
        SpcCheckResults other = (SpcCheckResults) obj;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true
                  && ((this.errorCode == null && other.getErrorCode() == null) || (this.errorCode != null && this.errorCode.equals(other.getErrorCode())))
                  && ((this.errorMessage == null && other.getErrorMessage() == null) || (this.errorMessage != null && this.errorMessage.equals(other.getErrorMessage())))
                  && this.lotHold == other.isLotHold()
                  && ((this.lotID == null && other.getLotID() == null) || (this.lotID != null && this.lotID.equals(other.getLotID())))
                  && ((this.reasonCode == null && other.getReasonCode() == null) || (this.reasonCode != null && this.reasonCode.equals(other.getReasonCode())))
                  && ((this.alarmID == null && other.getAlarmID() == null) || (this.alarmID != null && this.alarmID.equals(other.getAlarmID())));
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
        if (getErrorCode() != null) {
            _hashCode += getErrorCode().hashCode();
        }
        if (getErrorMessage() != null) {
            _hashCode += getErrorMessage().hashCode();
        }
        _hashCode += (isLotHold() ? Boolean.TRUE : Boolean.FALSE).hashCode();
        if (getLotID() != null) {
            _hashCode += getLotID().hashCode();
        }
        if (getReasonCode() != null) {
            _hashCode += getReasonCode().hashCode();
        }
        if (getAlarmID() != null) {
            _hashCode += getAlarmID().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc = new org.apache.axis.description.TypeDesc(
                                                                                                            SpcCheckResults.class,
                                                                                                            true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://tempuri.org/", "SpcCheckResults"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("errorCode");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "ErrorCode"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("errorMessage");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "ErrorMessage"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("lotHold");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "LotHold"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("lotID");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "LotID"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("reasonCode");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "ReasonCode"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("alarmID");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "AlarmID"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
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

    public List<HoldEquipmentModel> getHoldEquipmentModel() {
        return holdEquipmentModel;
    }

    public void setHoldEquipmentModel(List<HoldEquipmentModel> holdEquipmentModel) {
        this.holdEquipmentModel = holdEquipmentModel;
    }
}
