/**
 * SpcCheckExt4Void.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.fa.cim;

public class SpcCheckExt4Void implements java.io.Serializable {

    private String   lotID;

    private String   lotType;

    private String   pkg;

    private String   customer;

    private String   device;

    private String   customerLot;

    private String   recipeID;

    private String   prodArea;

    private String   location;

    private String   equipmentID;

    private int                reworkCount;

    private java.util.Calendar measureTime;

    private String   operatorID;

    private String   materialID;

    private String   target;

    private String   usl;

    private String   lsl;

    private String   dcopNo;

    private String[] itemName;

    private String[] itemValue;

    public SpcCheckExt4Void(){
    }

    public SpcCheckExt4Void(String lotID, String lotType, String pkg,
                            String customer, String device, String customerLot,
                            String recipeID, String prodArea, String location,
                            String equipmentID, int reworkCount, java.util.Calendar measureTime,
                            String operatorID, String materialID, String target,
                            String usl, String lsl, String dcopNo,
                            String[] itemName, String[] itemValue){
        this.lotID = lotID;
        this.lotType = lotType;
        this.pkg = pkg;
        this.customer = customer;
        this.device = device;
        this.customerLot = customerLot;
        this.recipeID = recipeID;
        this.prodArea = prodArea;
        this.location = location;
        this.equipmentID = equipmentID;
        this.reworkCount = reworkCount;
        this.measureTime = measureTime;
        this.operatorID = operatorID;
        this.materialID = materialID;
        this.target = target;
        this.usl = usl;
        this.lsl = lsl;
        this.dcopNo = dcopNo;
        this.itemName = itemName;
        this.itemValue = itemValue;
    }

    /**
     * Gets the lotID value for this SpcCheckExt4Void.
     * 
     * @return lotID
     */
    public String getLotID() {
        return lotID;
    }

    /**
     * Sets the lotID value for this SpcCheckExt4Void.
     * 
     * @param lotID
     */
    public void setLotID(String lotID) {
        this.lotID = lotID;
    }

    /**
     * Gets the lotType value for this SpcCheckExt4Void.
     * 
     * @return lotType
     */
    public String getLotType() {
        return lotType;
    }

    /**
     * Sets the lotType value for this SpcCheckExt4Void.
     * 
     * @param lotType
     */
    public void setLotType(String lotType) {
        this.lotType = lotType;
    }

    /**
     * Gets the pkg value for this SpcCheckExt4Void.
     * 
     * @return pkg
     */
    public String getPkg() {
        return pkg;
    }

    /**
     * Sets the pkg value for this SpcCheckExt4Void.
     * 
     * @param pkg
     */
    public void setPkg(String pkg) {
        this.pkg = pkg;
    }

    /**
     * Gets the customer value for this SpcCheckExt4Void.
     * 
     * @return customer
     */
    public String getCustomer() {
        return customer;
    }

    /**
     * Sets the customer value for this SpcCheckExt4Void.
     * 
     * @param customer
     */
    public void setCustomer(String customer) {
        this.customer = customer;
    }

    /**
     * Gets the device value for this SpcCheckExt4Void.
     * 
     * @return device
     */
    public String getDevice() {
        return device;
    }

    /**
     * Sets the device value for this SpcCheckExt4Void.
     * 
     * @param device
     */
    public void setDevice(String device) {
        this.device = device;
    }

    /**
     * Gets the customerLot value for this SpcCheckExt4Void.
     * 
     * @return customerLot
     */
    public String getCustomerLot() {
        return customerLot;
    }

    /**
     * Sets the customerLot value for this SpcCheckExt4Void.
     * 
     * @param customerLot
     */
    public void setCustomerLot(String customerLot) {
        this.customerLot = customerLot;
    }

    /**
     * Gets the recipeID value for this SpcCheckExt4Void.
     * 
     * @return recipeID
     */
    public String getRecipeID() {
        return recipeID;
    }

    /**
     * Sets the recipeID value for this SpcCheckExt4Void.
     * 
     * @param recipeID
     */
    public void setRecipeID(String recipeID) {
        this.recipeID = recipeID;
    }

    /**
     * Gets the prodArea value for this SpcCheckExt4Void.
     * 
     * @return prodArea
     */
    public String getProdArea() {
        return prodArea;
    }

    /**
     * Sets the prodArea value for this SpcCheckExt4Void.
     * 
     * @param prodArea
     */
    public void setProdArea(String prodArea) {
        this.prodArea = prodArea;
    }

    /**
     * Gets the location value for this SpcCheckExt4Void.
     * 
     * @return location
     */
    public String getLocation() {
        return location;
    }

    /**
     * Sets the location value for this SpcCheckExt4Void.
     * 
     * @param location
     */
    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * Gets the equipmentID value for this SpcCheckExt4Void.
     * 
     * @return equipmentID
     */
    public String getEquipmentID() {
        return equipmentID;
    }

    /**
     * Sets the equipmentID value for this SpcCheckExt4Void.
     * 
     * @param equipmentID
     */
    public void setEquipmentID(String equipmentID) {
        this.equipmentID = equipmentID;
    }

    /**
     * Gets the reworkCount value for this SpcCheckExt4Void.
     * 
     * @return reworkCount
     */
    public int getReworkCount() {
        return reworkCount;
    }

    /**
     * Sets the reworkCount value for this SpcCheckExt4Void.
     * 
     * @param reworkCount
     */
    public void setReworkCount(int reworkCount) {
        this.reworkCount = reworkCount;
    }

    /**
     * Gets the measureTime value for this SpcCheckExt4Void.
     * 
     * @return measureTime
     */
    public java.util.Calendar getMeasureTime() {
        return measureTime;
    }

    /**
     * Sets the measureTime value for this SpcCheckExt4Void.
     * 
     * @param measureTime
     */
    public void setMeasureTime(java.util.Calendar measureTime) {
        this.measureTime = measureTime;
    }

    /**
     * Gets the operatorID value for this SpcCheckExt4Void.
     * 
     * @return operatorID
     */
    public String getOperatorID() {
        return operatorID;
    }

    /**
     * Sets the operatorID value for this SpcCheckExt4Void.
     * 
     * @param operatorID
     */
    public void setOperatorID(String operatorID) {
        this.operatorID = operatorID;
    }

    /**
     * Gets the materialID value for this SpcCheckExt4Void.
     * 
     * @return materialID
     */
    public String getMaterialID() {
        return materialID;
    }

    /**
     * Sets the materialID value for this SpcCheckExt4Void.
     * 
     * @param materialID
     */
    public void setMaterialID(String materialID) {
        this.materialID = materialID;
    }

    /**
     * Gets the target value for this SpcCheckExt4Void.
     * 
     * @return target
     */
    public String getTarget() {
        return target;
    }

    /**
     * Sets the target value for this SpcCheckExt4Void.
     * 
     * @param target
     */
    public void setTarget(String target) {
        this.target = target;
    }

    /**
     * Gets the usl value for this SpcCheckExt4Void.
     * 
     * @return usl
     */
    public String getUsl() {
        return usl;
    }

    /**
     * Sets the usl value for this SpcCheckExt4Void.
     * 
     * @param usl
     */
    public void setUsl(String usl) {
        this.usl = usl;
    }

    /**
     * Gets the lsl value for this SpcCheckExt4Void.
     * 
     * @return lsl
     */
    public String getLsl() {
        return lsl;
    }

    /**
     * Sets the lsl value for this SpcCheckExt4Void.
     * 
     * @param lsl
     */
    public void setLsl(String lsl) {
        this.lsl = lsl;
    }

    /**
     * Gets the dcopNo value for this SpcCheckExt4Void.
     * 
     * @return dcopNo
     */
    public String getDcopNo() {
        return dcopNo;
    }

    /**
     * Sets the dcopNo value for this SpcCheckExt4Void.
     * 
     * @param dcopNo
     */
    public void setDcopNo(String dcopNo) {
        this.dcopNo = dcopNo;
    }

    /**
     * Gets the itemName value for this SpcCheckExt4Void.
     * 
     * @return itemName
     */
    public String[] getItemName() {
        return itemName;
    }

    /**
     * Sets the itemName value for this SpcCheckExt4Void.
     * 
     * @param itemName
     */
    public void setItemName(String[] itemName) {
        this.itemName = itemName;
    }

    /**
     * Gets the itemValue value for this SpcCheckExt4Void.
     * 
     * @return itemValue
     */
    public String[] getItemValue() {
        return itemValue;
    }

    /**
     * Sets the itemValue value for this SpcCheckExt4Void.
     * 
     * @param itemValue
     */
    public void setItemValue(String[] itemValue) {
        this.itemValue = itemValue;
    }

    private Object __equalsCalc = null;

    public synchronized boolean equals(Object obj) {
        if (!(obj instanceof SpcCheckExt4Void)) return false;
        SpcCheckExt4Void other = (SpcCheckExt4Void) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true
                  && ((this.lotID == null && other.getLotID() == null) || (this.lotID != null && this.lotID.equals(other.getLotID())))
                  && ((this.lotType == null && other.getLotType() == null) || (this.lotType != null && this.lotType.equals(other.getLotType())))
                  && ((this.pkg == null && other.getPkg() == null) || (this.pkg != null && this.pkg.equals(other.getPkg())))
                  && ((this.customer == null && other.getCustomer() == null) || (this.customer != null && this.customer.equals(other.getCustomer())))
                  && ((this.device == null && other.getDevice() == null) || (this.device != null && this.device.equals(other.getDevice())))
                  && ((this.customerLot == null && other.getCustomerLot() == null) || (this.customerLot != null && this.customerLot.equals(other.getCustomerLot())))
                  && ((this.recipeID == null && other.getRecipeID() == null) || (this.recipeID != null && this.recipeID.equals(other.getRecipeID())))
                  && ((this.prodArea == null && other.getProdArea() == null) || (this.prodArea != null && this.prodArea.equals(other.getProdArea())))
                  && ((this.location == null && other.getLocation() == null) || (this.location != null && this.location.equals(other.getLocation())))
                  && ((this.equipmentID == null && other.getEquipmentID() == null) || (this.equipmentID != null && this.equipmentID.equals(other.getEquipmentID())))
                  && this.reworkCount == other.getReworkCount()
                  && ((this.measureTime == null && other.getMeasureTime() == null) || (this.measureTime != null && this.measureTime.equals(other.getMeasureTime())))
                  && ((this.operatorID == null && other.getOperatorID() == null) || (this.operatorID != null && this.operatorID.equals(other.getOperatorID())))
                  && ((this.materialID == null && other.getMaterialID() == null) || (this.materialID != null && this.materialID.equals(other.getMaterialID())))
                  && ((this.target == null && other.getTarget() == null) || (this.target != null && this.target.equals(other.getTarget())))
                  && ((this.usl == null && other.getUsl() == null) || (this.usl != null && this.usl.equals(other.getUsl())))
                  && ((this.lsl == null && other.getLsl() == null) || (this.lsl != null && this.lsl.equals(other.getLsl())))
                  && ((this.dcopNo == null && other.getDcopNo() == null) || (this.dcopNo != null && this.dcopNo.equals(other.getDcopNo())))
                  && ((this.itemName == null && other.getItemName() == null) || (this.itemName != null && java.util.Arrays.equals(this.itemName,
                                                                                                                                  other.getItemName())))
                  && ((this.itemValue == null && other.getItemValue() == null) || (this.itemValue != null && java.util.Arrays.equals(this.itemValue,
                                                                                                                                     other.getItemValue())));
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
        if (getLotID() != null) {
            _hashCode += getLotID().hashCode();
        }
        if (getLotType() != null) {
            _hashCode += getLotType().hashCode();
        }
        if (getPkg() != null) {
            _hashCode += getPkg().hashCode();
        }
        if (getCustomer() != null) {
            _hashCode += getCustomer().hashCode();
        }
        if (getDevice() != null) {
            _hashCode += getDevice().hashCode();
        }
        if (getCustomerLot() != null) {
            _hashCode += getCustomerLot().hashCode();
        }
        if (getRecipeID() != null) {
            _hashCode += getRecipeID().hashCode();
        }
        if (getProdArea() != null) {
            _hashCode += getProdArea().hashCode();
        }
        if (getLocation() != null) {
            _hashCode += getLocation().hashCode();
        }
        if (getEquipmentID() != null) {
            _hashCode += getEquipmentID().hashCode();
        }
        _hashCode += getReworkCount();
        if (getMeasureTime() != null) {
            _hashCode += getMeasureTime().hashCode();
        }
        if (getOperatorID() != null) {
            _hashCode += getOperatorID().hashCode();
        }
        if (getMaterialID() != null) {
            _hashCode += getMaterialID().hashCode();
        }
        if (getTarget() != null) {
            _hashCode += getTarget().hashCode();
        }
        if (getUsl() != null) {
            _hashCode += getUsl().hashCode();
        }
        if (getLsl() != null) {
            _hashCode += getLsl().hashCode();
        }
        if (getDcopNo() != null) {
            _hashCode += getDcopNo().hashCode();
        }
        if (getItemName() != null) {
            for (int i = 0; i < java.lang.reflect.Array.getLength(getItemName()); i++) {
                Object obj = java.lang.reflect.Array.get(getItemName(), i);
                if (obj != null && !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getItemValue() != null) {
            for (int i = 0; i < java.lang.reflect.Array.getLength(getItemValue()); i++) {
                Object obj = java.lang.reflect.Array.get(getItemValue(), i);
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
                                                                                                            SpcCheckExt4Void.class,
                                                                                                            true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://tempuri.org/", ">SpcCheckExt4Void"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("lotID");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "lotID"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("lotType");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "lotType"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("pkg");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "pkg"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("customer");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "customer"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("device");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "device"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("customerLot");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "customerLot"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("recipeID");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "recipeID"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("prodArea");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "prodArea"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("location");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "location"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("equipmentID");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "equipmentID"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("reworkCount");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "reworkCount"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("measureTime");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "measureTime"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "dateTime"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("operatorID");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "operatorID"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("materialID");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "materialID"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("target");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "target"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("usl");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "usl"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("lsl");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "lsl"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("dcopNo");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "dcopNo"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("itemName");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "itemName"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        elemField.setItemQName(new javax.xml.namespace.QName("http://tempuri.org/", "string"));
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("itemValue");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "itemValue"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        elemField.setItemQName(new javax.xml.namespace.QName("http://tempuri.org/", "string"));
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
