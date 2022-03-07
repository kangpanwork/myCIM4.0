/**
 * SpcCheckExt4VoidResponse.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.fa.cim;

public class SpcCheckExt4VoidResponse implements java.io.Serializable {

    private String spcCheckExt4VoidResult;

    public SpcCheckExt4VoidResponse(){
    }

    public SpcCheckExt4VoidResponse(String spcCheckExt4VoidResult){
        this.spcCheckExt4VoidResult = spcCheckExt4VoidResult;
    }

    /**
     * Gets the spcCheckExt4VoidResult value for this SpcCheckExt4VoidResponse.
     * 
     * @return spcCheckExt4VoidResult
     */
    public String getSpcCheckExt4VoidResult() {
        return spcCheckExt4VoidResult;
    }

    /**
     * Sets the spcCheckExt4VoidResult value for this SpcCheckExt4VoidResponse.
     * 
     * @param spcCheckExt4VoidResult
     */
    public void setSpcCheckExt4VoidResult(String spcCheckExt4VoidResult) {
        this.spcCheckExt4VoidResult = spcCheckExt4VoidResult;
    }

    private Object __equalsCalc = null;

    public synchronized boolean equals(Object obj) {
        if (obj == null) return false;
        if (!(obj instanceof SpcCheckExt4VoidResponse)) return false;
        SpcCheckExt4VoidResponse other = (SpcCheckExt4VoidResponse) obj;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && ((this.spcCheckExt4VoidResult == null && other.getSpcCheckExt4VoidResult() == null) || (this.spcCheckExt4VoidResult != null && this.spcCheckExt4VoidResult.equals(other.getSpcCheckExt4VoidResult())));
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
        if (getSpcCheckExt4VoidResult() != null) {
            _hashCode += getSpcCheckExt4VoidResult().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc = new org.apache.axis.description.TypeDesc(
                                                                                                            SpcCheckExt4VoidResponse.class,
                                                                                                            true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://tempuri.org/", ">SpcCheckExt4VoidResponse"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("spcCheckExt4VoidResult");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "SpcCheckExt4VoidResult"));
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
