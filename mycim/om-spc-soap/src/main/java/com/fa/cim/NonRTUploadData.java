/**
 * NonRTUploadData.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.fa.cim;

public class NonRTUploadData  extends ServiceReq  implements java.io.Serializable {
    private String fabId;

    private String area;

    private String jobId;

    private String nonRTKey;

    private String retestFlag;

    private String subgroupSizeFlag;

    private String pro1;

    private String pro2;

    private String pro3;

    private String pro4;

    private String pro5;

    private double[] valueList;

    public NonRTUploadData() {
    }

    public NonRTUploadData(
           String starttime,
           String endtime,
           String conditionString,
           String machineId,
           String userId,
           String currentUserID,
           String fabID,
           long configID,
           String condition,
           int firstResult,
           int pageSize,
           String jobID,
           String fabId,
           String area,
           String jobId,
           String nonRTKey,
           String retestFlag,
           String subgroupSizeFlag,
           String pro1,
           String pro2,
           String pro3,
           String pro4,
           String pro5,
           double[] valueList) {
        super(
            starttime,
            endtime,
            conditionString,
            machineId,
            userId,
            currentUserID,
            fabID,
            configID,
            condition,
            firstResult,
            pageSize,
            jobID);
        this.fabId = fabId;
        this.area = area;
        this.jobId = jobId;
        this.nonRTKey = nonRTKey;
        this.retestFlag = retestFlag;
        this.subgroupSizeFlag = subgroupSizeFlag;
        this.pro1 = pro1;
        this.pro2 = pro2;
        this.pro3 = pro3;
        this.pro4 = pro4;
        this.pro5 = pro5;
        this.valueList = valueList;
    }


    /**
     * Gets the fabId value for this NonRTUploadData.
     * 
     * @return fabId
     */
    public String getFabId() {
        return fabId;
    }


    /**
     * Sets the fabId value for this NonRTUploadData.
     * 
     * @param fabId
     */
    public void setFabId(String fabId) {
        this.fabId = fabId;
    }


    /**
     * Gets the area value for this NonRTUploadData.
     * 
     * @return area
     */
    public String getArea() {
        return area;
    }


    /**
     * Sets the area value for this NonRTUploadData.
     * 
     * @param area
     */
    public void setArea(String area) {
        this.area = area;
    }


    /**
     * Gets the jobId value for this NonRTUploadData.
     * 
     * @return jobId
     */
    public String getJobId() {
        return jobId;
    }


    /**
     * Sets the jobId value for this NonRTUploadData.
     * 
     * @param jobId
     */
    public void setJobId(String jobId) {
        this.jobId = jobId;
    }


    /**
     * Gets the nonRTKey value for this NonRTUploadData.
     * 
     * @return nonRTKey
     */
    public String getNonRTKey() {
        return nonRTKey;
    }


    /**
     * Sets the nonRTKey value for this NonRTUploadData.
     * 
     * @param nonRTKey
     */
    public void setNonRTKey(String nonRTKey) {
        this.nonRTKey = nonRTKey;
    }


    /**
     * Gets the retestFlag value for this NonRTUploadData.
     * 
     * @return retestFlag
     */
    public String getRetestFlag() {
        return retestFlag;
    }


    /**
     * Sets the retestFlag value for this NonRTUploadData.
     * 
     * @param retestFlag
     */
    public void setRetestFlag(String retestFlag) {
        this.retestFlag = retestFlag;
    }


    /**
     * Gets the subgroupSizeFlag value for this NonRTUploadData.
     * 
     * @return subgroupSizeFlag
     */
    public String getSubgroupSizeFlag() {
        return subgroupSizeFlag;
    }


    /**
     * Sets the subgroupSizeFlag value for this NonRTUploadData.
     * 
     * @param subgroupSizeFlag
     */
    public void setSubgroupSizeFlag(String subgroupSizeFlag) {
        this.subgroupSizeFlag = subgroupSizeFlag;
    }


    /**
     * Gets the pro1 value for this NonRTUploadData.
     * 
     * @return pro1
     */
    public String getPro1() {
        return pro1;
    }


    /**
     * Sets the pro1 value for this NonRTUploadData.
     * 
     * @param pro1
     */
    public void setPro1(String pro1) {
        this.pro1 = pro1;
    }


    /**
     * Gets the pro2 value for this NonRTUploadData.
     * 
     * @return pro2
     */
    public String getPro2() {
        return pro2;
    }


    /**
     * Sets the pro2 value for this NonRTUploadData.
     * 
     * @param pro2
     */
    public void setPro2(String pro2) {
        this.pro2 = pro2;
    }


    /**
     * Gets the pro3 value for this NonRTUploadData.
     * 
     * @return pro3
     */
    public String getPro3() {
        return pro3;
    }


    /**
     * Sets the pro3 value for this NonRTUploadData.
     * 
     * @param pro3
     */
    public void setPro3(String pro3) {
        this.pro3 = pro3;
    }


    /**
     * Gets the pro4 value for this NonRTUploadData.
     * 
     * @return pro4
     */
    public String getPro4() {
        return pro4;
    }


    /**
     * Sets the pro4 value for this NonRTUploadData.
     * 
     * @param pro4
     */
    public void setPro4(String pro4) {
        this.pro4 = pro4;
    }


    /**
     * Gets the pro5 value for this NonRTUploadData.
     * 
     * @return pro5
     */
    public String getPro5() {
        return pro5;
    }


    /**
     * Sets the pro5 value for this NonRTUploadData.
     * 
     * @param pro5
     */
    public void setPro5(String pro5) {
        this.pro5 = pro5;
    }


    /**
     * Gets the valueList value for this NonRTUploadData.
     * 
     * @return valueList
     */
    public double[] getValueList() {
        return valueList;
    }


    /**
     * Sets the valueList value for this NonRTUploadData.
     * 
     * @param valueList
     */
    public void setValueList(double[] valueList) {
        this.valueList = valueList;
    }

    private Object __equalsCalc = null;
    public synchronized boolean equals(Object obj) {
        if (!(obj instanceof NonRTUploadData)) return false;
        NonRTUploadData other = (NonRTUploadData) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = super.equals(obj) && 
            ((this.fabId==null && other.getFabId()==null) || 
             (this.fabId!=null &&
              this.fabId.equals(other.getFabId()))) &&
            ((this.area==null && other.getArea()==null) || 
             (this.area!=null &&
              this.area.equals(other.getArea()))) &&
            ((this.jobId==null && other.getJobId()==null) || 
             (this.jobId!=null &&
              this.jobId.equals(other.getJobId()))) &&
            ((this.nonRTKey==null && other.getNonRTKey()==null) || 
             (this.nonRTKey!=null &&
              this.nonRTKey.equals(other.getNonRTKey()))) &&
            ((this.retestFlag==null && other.getRetestFlag()==null) || 
             (this.retestFlag!=null &&
              this.retestFlag.equals(other.getRetestFlag()))) &&
            ((this.subgroupSizeFlag==null && other.getSubgroupSizeFlag()==null) || 
             (this.subgroupSizeFlag!=null &&
              this.subgroupSizeFlag.equals(other.getSubgroupSizeFlag()))) &&
            ((this.pro1==null && other.getPro1()==null) || 
             (this.pro1!=null &&
              this.pro1.equals(other.getPro1()))) &&
            ((this.pro2==null && other.getPro2()==null) || 
             (this.pro2!=null &&
              this.pro2.equals(other.getPro2()))) &&
            ((this.pro3==null && other.getPro3()==null) || 
             (this.pro3!=null &&
              this.pro3.equals(other.getPro3()))) &&
            ((this.pro4==null && other.getPro4()==null) || 
             (this.pro4!=null &&
              this.pro4.equals(other.getPro4()))) &&
            ((this.pro5==null && other.getPro5()==null) || 
             (this.pro5!=null &&
              this.pro5.equals(other.getPro5()))) &&
            ((this.valueList==null && other.getValueList()==null) || 
             (this.valueList!=null &&
              java.util.Arrays.equals(this.valueList, other.getValueList())));
        __equalsCalc = null;
        return _equals;
    }

    private boolean __hashCodeCalc = false;
    public synchronized int hashCode() {
        if (__hashCodeCalc) {
            return 0;
        }
        __hashCodeCalc = true;
        int _hashCode = super.hashCode();
        if (getFabId() != null) {
            _hashCode += getFabId().hashCode();
        }
        if (getArea() != null) {
            _hashCode += getArea().hashCode();
        }
        if (getJobId() != null) {
            _hashCode += getJobId().hashCode();
        }
        if (getNonRTKey() != null) {
            _hashCode += getNonRTKey().hashCode();
        }
        if (getRetestFlag() != null) {
            _hashCode += getRetestFlag().hashCode();
        }
        if (getSubgroupSizeFlag() != null) {
            _hashCode += getSubgroupSizeFlag().hashCode();
        }
        if (getPro1() != null) {
            _hashCode += getPro1().hashCode();
        }
        if (getPro2() != null) {
            _hashCode += getPro2().hashCode();
        }
        if (getPro3() != null) {
            _hashCode += getPro3().hashCode();
        }
        if (getPro4() != null) {
            _hashCode += getPro4().hashCode();
        }
        if (getPro5() != null) {
            _hashCode += getPro5().hashCode();
        }
        if (getValueList() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getValueList());
                 i++) {
                Object obj = java.lang.reflect.Array.get(getValueList(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(NonRTUploadData.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://tempuri.org/", "NonRTUploadData"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("fabId");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "FabId"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("area");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "Area"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("jobId");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "JobId"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("nonRTKey");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "NonRTKey"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("retestFlag");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "RetestFlag"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("subgroupSizeFlag");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "SubgroupSizeFlag"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("pro1");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "Pro1"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("pro2");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "Pro2"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("pro3");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "Pro3"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("pro4");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "Pro4"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("pro5");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "Pro5"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("valueList");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "ValueList"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "double"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        elemField.setItemQName(new javax.xml.namespace.QName("http://tempuri.org/", "double"));
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
