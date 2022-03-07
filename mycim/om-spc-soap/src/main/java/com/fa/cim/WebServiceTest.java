/**
 * WebServiceTest.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.fa.cim;

public class WebServiceTest  implements java.io.Serializable {
    private String strUserNo;

    public WebServiceTest() {
    }

    public WebServiceTest(
           String strUserNo) {
           this.strUserNo = strUserNo;
    }


    /**
     * Gets the strUserNo value for this WebServiceTest.
     * 
     * @return strUserNo
     */
    public String getStrUserNo() {
        return strUserNo;
    }


    /**
     * Sets the strUserNo value for this WebServiceTest.
     * 
     * @param strUserNo
     */
    public void setStrUserNo(String strUserNo) {
        this.strUserNo = strUserNo;
    }

    private Object __equalsCalc = null;
    public synchronized boolean equals(Object obj) {
        if (obj == null) return false;
        if (!(obj instanceof WebServiceTest)) return false;
        WebServiceTest other = (WebServiceTest) obj;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.strUserNo==null && other.getStrUserNo()==null) || 
             (this.strUserNo!=null &&
              this.strUserNo.equals(other.getStrUserNo())));
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
        if (getStrUserNo() != null) {
            _hashCode += getStrUserNo().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(WebServiceTest.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://tempuri.org/", ">WebServiceTest"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("strUserNo");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "strUserNo"));
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
