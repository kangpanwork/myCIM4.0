/**
 * ImpjobResponse.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.fa.cim;

public class ImpjobResponse  implements java.io.Serializable {
    private String impjobResult;

    public ImpjobResponse() {
    }

    public ImpjobResponse(
           String impjobResult) {
           this.impjobResult = impjobResult;
    }


    /**
     * Gets the impjobResult value for this ImpjobResponse.
     * 
     * @return impjobResult
     */
    public String getImpjobResult() {
        return impjobResult;
    }


    /**
     * Sets the impjobResult value for this ImpjobResponse.
     * 
     * @param impjobResult
     */
    public void setImpjobResult(String impjobResult) {
        this.impjobResult = impjobResult;
    }

    private Object __equalsCalc = null;
    public synchronized boolean equals(Object obj) {
        if (obj == null) return false;
        if (!(obj instanceof ImpjobResponse)) return false;
        ImpjobResponse other = (ImpjobResponse) obj;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.impjobResult==null && other.getImpjobResult()==null) || 
             (this.impjobResult!=null &&
              this.impjobResult.equals(other.getImpjobResult())));
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
        if (getImpjobResult() != null) {
            _hashCode += getImpjobResult().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(ImpjobResponse.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://tempuri.org/", ">ImpjobResponse"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("impjobResult");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "ImpjobResult"));
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
