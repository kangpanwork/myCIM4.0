/**
 * GetChartViewResponse.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.fa.cim;

public class GetChartViewResponse  implements java.io.Serializable {
    private String getChartViewResult;

    public GetChartViewResponse() {
    }

    public GetChartViewResponse(
           String getChartViewResult) {
           this.getChartViewResult = getChartViewResult;
    }


    /**
     * Gets the getChartViewResult value for this GetChartViewResponse.
     * 
     * @return getChartViewResult
     */
    public String getGetChartViewResult() {
        return getChartViewResult;
    }


    /**
     * Sets the getChartViewResult value for this GetChartViewResponse.
     * 
     * @param getChartViewResult
     */
    public void setGetChartViewResult(String getChartViewResult) {
        this.getChartViewResult = getChartViewResult;
    }

    private Object __equalsCalc = null;
    public synchronized boolean equals(Object obj) {
        if (obj == null) return false;
        if (!(obj instanceof GetChartViewResponse)) return false;
        GetChartViewResponse other = (GetChartViewResponse) obj;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.getChartViewResult==null && other.getGetChartViewResult()==null) || 
             (this.getChartViewResult!=null &&
              this.getChartViewResult.equals(other.getGetChartViewResult())));
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
        if (getGetChartViewResult() != null) {
            _hashCode += getGetChartViewResult().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(GetChartViewResponse.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://tempuri.org/", ">GetChartViewResponse"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("getChartViewResult");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "GetChartViewResult"));
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
