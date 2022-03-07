/**
 * ServiceReq.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.fa.cim;

public class ServiceReq  implements java.io.Serializable {
    private String starttime;

    private String endtime;

    private String conditionString;

    private String machineId;

    private String userId;

    private String currentUserID;

    private String fabID;

    private long configID;

    private String condition;

    private int firstResult;

    private int pageSize;

    private String jobID;

    public ServiceReq() {
    }

    public ServiceReq(
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
           String jobID) {
           this.starttime = starttime;
           this.endtime = endtime;
           this.conditionString = conditionString;
           this.machineId = machineId;
           this.userId = userId;
           this.currentUserID = currentUserID;
           this.fabID = fabID;
           this.configID = configID;
           this.condition = condition;
           this.firstResult = firstResult;
           this.pageSize = pageSize;
           this.jobID = jobID;
    }


    /**
     * Gets the starttime value for this ServiceReq.
     * 
     * @return starttime
     */
    public String getStarttime() {
        return starttime;
    }


    /**
     * Sets the starttime value for this ServiceReq.
     * 
     * @param starttime
     */
    public void setStarttime(String starttime) {
        this.starttime = starttime;
    }


    /**
     * Gets the endtime value for this ServiceReq.
     * 
     * @return endtime
     */
    public String getEndtime() {
        return endtime;
    }


    /**
     * Sets the endtime value for this ServiceReq.
     * 
     * @param endtime
     */
    public void setEndtime(String endtime) {
        this.endtime = endtime;
    }


    /**
     * Gets the conditionString value for this ServiceReq.
     * 
     * @return conditionString
     */
    public String getConditionString() {
        return conditionString;
    }


    /**
     * Sets the conditionString value for this ServiceReq.
     * 
     * @param conditionString
     */
    public void setConditionString(String conditionString) {
        this.conditionString = conditionString;
    }


    /**
     * Gets the machineId value for this ServiceReq.
     * 
     * @return machineId
     */
    public String getMachineId() {
        return machineId;
    }


    /**
     * Sets the machineId value for this ServiceReq.
     * 
     * @param machineId
     */
    public void setMachineId(String machineId) {
        this.machineId = machineId;
    }


    /**
     * Gets the userId value for this ServiceReq.
     * 
     * @return userId
     */
    public String getUserId() {
        return userId;
    }


    /**
     * Sets the userId value for this ServiceReq.
     * 
     * @param userId
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }


    /**
     * Gets the currentUserID value for this ServiceReq.
     * 
     * @return currentUserID
     */
    public String getCurrentUserID() {
        return currentUserID;
    }


    /**
     * Sets the currentUserID value for this ServiceReq.
     * 
     * @param currentUserID
     */
    public void setCurrentUserID(String currentUserID) {
        this.currentUserID = currentUserID;
    }


    /**
     * Gets the fabID value for this ServiceReq.
     * 
     * @return fabID
     */
    public String getFabID() {
        return fabID;
    }


    /**
     * Sets the fabID value for this ServiceReq.
     * 
     * @param fabID
     */
    public void setFabID(String fabID) {
        this.fabID = fabID;
    }


    /**
     * Gets the configID value for this ServiceReq.
     * 
     * @return configID
     */
    public long getConfigID() {
        return configID;
    }


    /**
     * Sets the configID value for this ServiceReq.
     * 
     * @param configID
     */
    public void setConfigID(long configID) {
        this.configID = configID;
    }


    /**
     * Gets the condition value for this ServiceReq.
     * 
     * @return condition
     */
    public String getCondition() {
        return condition;
    }


    /**
     * Sets the condition value for this ServiceReq.
     * 
     * @param condition
     */
    public void setCondition(String condition) {
        this.condition = condition;
    }


    /**
     * Gets the firstResult value for this ServiceReq.
     * 
     * @return firstResult
     */
    public int getFirstResult() {
        return firstResult;
    }


    /**
     * Sets the firstResult value for this ServiceReq.
     * 
     * @param firstResult
     */
    public void setFirstResult(int firstResult) {
        this.firstResult = firstResult;
    }


    /**
     * Gets the pageSize value for this ServiceReq.
     * 
     * @return pageSize
     */
    public int getPageSize() {
        return pageSize;
    }


    /**
     * Sets the pageSize value for this ServiceReq.
     * 
     * @param pageSize
     */
    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }


    /**
     * Gets the jobID value for this ServiceReq.
     * 
     * @return jobID
     */
    public String getJobID() {
        return jobID;
    }


    /**
     * Sets the jobID value for this ServiceReq.
     * 
     * @param jobID
     */
    public void setJobID(String jobID) {
        this.jobID = jobID;
    }

    private Object __equalsCalc = null;
    public synchronized boolean equals(Object obj) {
        if (!(obj instanceof ServiceReq)) return false;
        ServiceReq other = (ServiceReq) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.starttime==null && other.getStarttime()==null) || 
             (this.starttime!=null &&
              this.starttime.equals(other.getStarttime()))) &&
            ((this.endtime==null && other.getEndtime()==null) || 
             (this.endtime!=null &&
              this.endtime.equals(other.getEndtime()))) &&
            ((this.conditionString==null && other.getConditionString()==null) || 
             (this.conditionString!=null &&
              this.conditionString.equals(other.getConditionString()))) &&
            ((this.machineId==null && other.getMachineId()==null) || 
             (this.machineId!=null &&
              this.machineId.equals(other.getMachineId()))) &&
            ((this.userId==null && other.getUserId()==null) || 
             (this.userId!=null &&
              this.userId.equals(other.getUserId()))) &&
            ((this.currentUserID==null && other.getCurrentUserID()==null) || 
             (this.currentUserID!=null &&
              this.currentUserID.equals(other.getCurrentUserID()))) &&
            ((this.fabID==null && other.getFabID()==null) || 
             (this.fabID!=null &&
              this.fabID.equals(other.getFabID()))) &&
            this.configID == other.getConfigID() &&
            ((this.condition==null && other.getCondition()==null) || 
             (this.condition!=null &&
              this.condition.equals(other.getCondition()))) &&
            this.firstResult == other.getFirstResult() &&
            this.pageSize == other.getPageSize() &&
            ((this.jobID==null && other.getJobID()==null) || 
             (this.jobID!=null &&
              this.jobID.equals(other.getJobID())));
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
        if (getStarttime() != null) {
            _hashCode += getStarttime().hashCode();
        }
        if (getEndtime() != null) {
            _hashCode += getEndtime().hashCode();
        }
        if (getConditionString() != null) {
            _hashCode += getConditionString().hashCode();
        }
        if (getMachineId() != null) {
            _hashCode += getMachineId().hashCode();
        }
        if (getUserId() != null) {
            _hashCode += getUserId().hashCode();
        }
        if (getCurrentUserID() != null) {
            _hashCode += getCurrentUserID().hashCode();
        }
        if (getFabID() != null) {
            _hashCode += getFabID().hashCode();
        }
        _hashCode += new Long(getConfigID()).hashCode();
        if (getCondition() != null) {
            _hashCode += getCondition().hashCode();
        }
        _hashCode += getFirstResult();
        _hashCode += getPageSize();
        if (getJobID() != null) {
            _hashCode += getJobID().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(ServiceReq.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://tempuri.org/", "ServiceReq"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("starttime");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "Starttime"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("endtime");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "Endtime"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("conditionString");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "ConditionString"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("machineId");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "MachineId"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("userId");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "UserId"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("currentUserID");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "CurrentUserID"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("fabID");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "FabID"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("configID");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "ConfigID"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "long"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("condition");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "Condition"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("firstResult");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "FirstResult"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("pageSize");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "PageSize"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("jobID");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "JobID"));
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
