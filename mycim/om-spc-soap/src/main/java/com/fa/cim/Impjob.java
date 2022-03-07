/**
 * SpcCheck.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.fa.cim;

public class Impjob implements java.io.Serializable {

    private ImpJobModel[] jobs;

    private String                            type;

    public Impjob(){
    }

    public Impjob(ImpJobModel[] jobs, String type){
        this.jobs = jobs;
        this.type = type;
    }

    /**
     * Gets the jobs value for this Impjob.
     * 
     * @return jobs
     */
    public ImpJobModel[] getJobs() {
        return jobs;
    }

    /**
     * Sets the jobs value for this Impjob.
     * 
     * @param jobs
     */
    public void setJobs(ImpJobModel[] jobs) {
        this.jobs = jobs;
    }

    /**
     * Gets the type value for this Impjob.
     * 
     * @return type
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the type value for this Impjob.
     * 
     * @param type
     */
    public void setType(String type) {
        this.type = type;
    }

    private Object __equalsCalc = null;

    public synchronized boolean equals(Object obj) {
        if (obj == null) return false;
        if (!(obj instanceof Impjob)) return false;
        Impjob other = (Impjob) obj;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && ((this.jobs == null && other.getJobs() == null) || (this.jobs != null && java.util.Arrays.equals(this.jobs,
                                                                                                                           other.getJobs()))
                                                                             && ((this.type == null && other.getType() == null) || (this.type != null && this.type.equals(other.getType()))));
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
        if (getJobs() != null) {
            for (int i = 0; i < java.lang.reflect.Array.getLength(getJobs()); i++) {
                Object obj = java.lang.reflect.Array.get(getJobs(), i);
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
                                                                                                            Impjob.class,
                                                                                                            true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://tempuri.org/", ">Impjob"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("jobs");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "jobs"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://tempuri.org/", "ImpJobModel"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        elemField.setItemQName(new javax.xml.namespace.QName("http://tempuri.org/", "ImpJobModel"));
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("type");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "type"));
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
