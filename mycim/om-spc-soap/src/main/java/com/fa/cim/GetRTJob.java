/**
 * GetRTJob.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.fa.cim;

public class GetRTJob  implements java.io.Serializable {
    private String fabID;

    private String jobNames;

    public GetRTJob() {
    }

    public GetRTJob(
           String fabID,
           String jobNames) {
           this.fabID = fabID;
           this.jobNames = jobNames;
    }


    /**
     * Gets the fabID value for this GetRTJob.
     * 
     * @return fabID
     */
    public String getFabID() {
        return fabID;
    }


    /**
     * Sets the fabID value for this GetRTJob.
     * 
     * @param fabID
     */
    public void setFabID(String fabID) {
        this.fabID = fabID;
    }


    /**
     * Gets the jobNames value for this GetRTJob.
     * 
     * @return jobNames
     */
    public String getJobNames() {
        return jobNames;
    }


    /**
     * Sets the jobNames value for this GetRTJob.
     * 
     * @param jobNames
     */
    public void setJobNames(String jobNames) {
        this.jobNames = jobNames;
    }

    private Object __equalsCalc = null;
    public synchronized boolean equals(Object obj) {
        if (obj == null) return false;
        if (!(obj instanceof GetRTJob)) return false;
        GetRTJob other = (GetRTJob) obj;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.fabID==null && other.getFabID()==null) || 
             (this.fabID!=null &&
              this.fabID.equals(other.getFabID()))) &&
            ((this.jobNames==null && other.getJobNames()==null) || 
             (this.jobNames!=null &&
              this.jobNames.equals(other.getJobNames())));
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
        if (getFabID() != null) {
            _hashCode += getFabID().hashCode();
        }
        if (getJobNames() != null) {
            _hashCode += getJobNames().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(GetRTJob.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://tempuri.org/", ">GetRTJob"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("fabID");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "fabID"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("jobNames");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "jobNames"));
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
    public static org.apache.axis.encoding.Serializer getSerializer(
           String mechType,
           Class _javaType,
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanSerializer(
            _javaType, _xmlType, typeDesc);
    }

    /**
     * Get Custom Deserializer
     */
    public static org.apache.axis.encoding.Deserializer getDeserializer(
           String mechType,
           Class _javaType,
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanDeserializer(
            _javaType, _xmlType, typeDesc);
    }

}
