/**
 * SpcCheckResponse.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.fa.cim;

public class SpcCheckResponse implements java.io.Serializable {

    private SpcCheckResults[] spcCheckResult;

    public SpcCheckResponse(){
    }

    public SpcCheckResponse(SpcCheckResults[] spcCheckResult){
        this.spcCheckResult = spcCheckResult;
    }

    /**
     * Gets the spcCheckResult value for this SpcCheckResponse.
     * 
     * @return spcCheckResult
     */
    public SpcCheckResults[] getSpcCheckResult() {
        return spcCheckResult;
    }

    /**
     * Sets the spcCheckResult value for this SpcCheckResponse.
     * 
     * @param spcCheckResult
     */
    public void setSpcCheckResult(SpcCheckResults[] spcCheckResult) {
        this.spcCheckResult = spcCheckResult;
    }

    private Object __equalsCalc = null;

    public synchronized boolean equals(Object obj) {
        if (obj == null) return false;
        if (!(obj instanceof SpcCheckResponse)) return false;
        SpcCheckResponse other = (SpcCheckResponse) obj;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && ((this.spcCheckResult == null && other.getSpcCheckResult() == null) || (this.spcCheckResult != null && java.util.Arrays.equals(this.spcCheckResult,
                                                                                                                                                         other.getSpcCheckResult())));
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
        if (getSpcCheckResult() != null) {
            for (int i = 0; i < java.lang.reflect.Array.getLength(getSpcCheckResult()); i++) {
                Object obj = java.lang.reflect.Array.get(getSpcCheckResult(), i);
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
                                                                                                            SpcCheckResponse.class,
                                                                                                            true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://tempuri.org/", ">SpcCheckResponse"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("spcCheckResult");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "SpcCheckResult"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://tempuri.org/", "SpcCheckResults"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        elemField.setItemQName(new javax.xml.namespace.QName("http://tempuri.org/", "SpcCheckResults"));
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
