/**
 * GetRTJobResponse.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.fa.cim;

public class GetRTJobResponse  implements java.io.Serializable {
    private String getRTJobResult;

    public GetRTJobResponse() {
    }

    public GetRTJobResponse(
           String getRTJobResult) {
           this.getRTJobResult = getRTJobResult;
    }


    /**
     * Gets the getRTJobResult value for this GetRTJobResponse.
     * 
     * @return getRTJobResult
     */
    public String getGetRTJobResult() {
        return getRTJobResult;
    }


    /**
     * Sets the getRTJobResult value for this GetRTJobResponse.
     * 
     * @param getRTJobResult
     */
    public void setGetRTJobResult(String getRTJobResult) {
        this.getRTJobResult = getRTJobResult;
    }

    private Object __equalsCalc = null;
    public synchronized boolean equals(Object obj) {
        if (obj == null) return false;
        if (!(obj instanceof GetRTJobResponse)) return false;
        GetRTJobResponse other = (GetRTJobResponse) obj;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.getRTJobResult==null && other.getGetRTJobResult()==null) || 
             (this.getRTJobResult!=null &&
              this.getRTJobResult.equals(other.getGetRTJobResult())));
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
        if (getGetRTJobResult() != null) {
            _hashCode += getGetRTJobResult().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(GetRTJobResponse.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://tempuri.org/", ">GetRTJobResponse"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("getRTJobResult");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "GetRTJobResult"));
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
