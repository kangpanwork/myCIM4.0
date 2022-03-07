/**
 * SpcCheck.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.fa.cim;

public class SpcCheck implements java.io.Serializable {

    private DataCollection[] dcs;

    public SpcCheck(){
    }

    public SpcCheck(DataCollection[] dcs){
        this.dcs = dcs;
    }

    /**
     * Gets the dcs value for this SpcCheck.
     * 
     * @return dcs
     */
    public DataCollection[] getDcs() {
        return dcs;
    }

    /**
     * Sets the dcs value for this SpcCheck.
     * 
     * @param dcs
     */
    public void setDcs(DataCollection[] dcs) {
        this.dcs = dcs;
    }

    private Object __equalsCalc = null;

    public synchronized boolean equals(Object obj) {
        if (obj == null) return false;
        if (!(obj instanceof SpcCheck)) return false;
        SpcCheck other = (SpcCheck) obj;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && ((this.dcs == null && other.getDcs() == null) || (this.dcs != null && java.util.Arrays.equals(this.dcs,
                                                                                                                        other.getDcs())));
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
        if (getDcs() != null) {
            for (int i = 0; i < java.lang.reflect.Array.getLength(getDcs()); i++) {
                Object obj = java.lang.reflect.Array.get(getDcs(), i);
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
                                                                                                            SpcCheck.class,
                                                                                                            true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://tempuri.org/", ">SpcCheck"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("dcs");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "dcs"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://tempuri.org/", "DataCollection"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        elemField.setItemQName(new javax.xml.namespace.QName("http://tempuri.org/", "DataCollection"));
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
