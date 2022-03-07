/**
 * SpcCheckSingleResponse.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.fa.cim;

public class SpcCheckSingleResponse implements java.io.Serializable {

    private String spcCheckSingleResult;

    public SpcCheckSingleResponse(){
    }

    public SpcCheckSingleResponse(String spcCheckSingleResult){
        this.spcCheckSingleResult = spcCheckSingleResult;
    }

    /**
     * Gets the spcCheckSingleResult value for this SpcCheckSingleResponse.
     * 
     * @return spcCheckSingleResult
     */
    public String getSpcCheckSingleResult() {
        return spcCheckSingleResult;
    }

    /**
     * Sets the spcCheckSingleResult value for this SpcCheckSingleResponse.
     * 
     * @param spcCheckSingleResult
     */
    public void setSpcCheckSingleResult(String spcCheckSingleResult) {
        this.spcCheckSingleResult = spcCheckSingleResult;
    }

    private Object __equalsCalc = null;

    public synchronized boolean equals(Object obj) {
        if (obj == null) return false;
        if (!(obj instanceof SpcCheckSingleResponse)) return false;
        SpcCheckSingleResponse other = (SpcCheckSingleResponse) obj;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && ((this.spcCheckSingleResult == null && other.getSpcCheckSingleResult() == null) || (this.spcCheckSingleResult != null && this.spcCheckSingleResult.equals(other.getSpcCheckSingleResult())));
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
        if (getSpcCheckSingleResult() != null) {
            _hashCode += getSpcCheckSingleResult().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc = new org.apache.axis.description.TypeDesc(
                                                                                                            SpcCheckSingleResponse.class,
                                                                                                            true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://tempuri.org/", ">SpcCheckSingleResponse"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("spcCheckSingleResult");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "SpcCheckSingleResult"));
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

}
