/**
 * DataCollection.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.fa.cim;

public class DataCollection implements java.io.Serializable {

    private String                                batchIDs;

    private String                                waferIDs;

    private String                                routeID;

    private String                                routeSeq;

    private String                                operationSeq;

    private String                                prod_6;

    private String                                prod_7;

    private String                                prod_8;

    private String                                prod_9;

    private String                                prod_10;

    private String                                batchFlag;

    private String                                fabID;

    private String                                areaID;

    private String                                lotId;

    private String                                lotsInBatch;

    private String                                lotType;

    private String                                customerLot;

    private String                                recipeID;

    private String                                location;

    private String                                technologyID;

    private String                                productId;

    private String                                prod_1;

    private String                                prod_2;

    private String                                prod_3;

    private String                                prod_4;

    private String                                customer;

    private String                                device;

    private String                                operatorID;

    private String                                materialID;

    private String                                operationNumber;

    private String                                dcopNo;

    private int                                             reworkCount;

    private String                                equipmentId;

    private java.util.Calendar                              completionDate;

    private java.util.Calendar                              receiveTime;

    private double                                          tartget;

    private double                                          usl;

    private double                                          lsl;

    private String                                subGroupSize;

    private String                                monitorGroupId;

    private DCItem[]          dcItems;

    private ProcessDataInfo[] processDataInfos;
    
    private String                                flowSeq;

    private String[]                              jobNames;
    
    private String                                batchInfo;
    
    private String                                batchLot;
    
    private int                                             batchSize;
    
    private String                                batchProductID;
    

    public DataCollection(){
    }

    public DataCollection(String batchIDs, String waferIDs, String routeID,
                          String routeSeq, String operationSeq, String prod_6,
                          String prod_7, String prod_8, String prod_9,
                          String prod_10, String batchFlag, String fabID,
                          String areaID, String lotId, String lotsInBatch,
                          String lotType, String customerLot, String recipeID,
                          String location, String technologyID, String productId,
                          String prod_1, String prod_2, String prod_3,
                          String prod_4, String customer, String device,
                          String operatorID, String materialID, String operationNumber,
                          String dcopNo, int reworkCount, String equipmentId,
                          java.util.Calendar completionDate, java.util.Calendar receiveTime, double tartget,
                          double usl, double lsl, String subGroupSize, String monitorGroupId,
                          DCItem[] dcItems,
                          ProcessDataInfo[] processDataInfos){
        this.batchIDs = batchIDs;
        this.waferIDs = waferIDs;
        this.routeID = routeID;
        this.routeSeq = routeSeq;
        this.operationSeq = operationSeq;
        this.prod_6 = prod_6;
        this.prod_7 = prod_7;
        this.prod_8 = prod_8;
        this.prod_9 = prod_9;
        this.prod_10 = prod_10;
        this.batchFlag = batchFlag;
        this.fabID = fabID;
        this.areaID = areaID;
        this.lotId = lotId;
        this.lotsInBatch = lotsInBatch;
        this.lotType = lotType;
        this.customerLot = customerLot;
        this.recipeID = recipeID;
        this.location = location;
        this.technologyID = technologyID;
        this.productId = productId;
        this.prod_1 = prod_1;
        this.prod_2 = prod_2;
        this.prod_3 = prod_3;
        this.prod_4 = prod_4;
        this.customer = customer;
        this.device = device;
        this.operatorID = operatorID;
        this.materialID = materialID;
        this.operationNumber = operationNumber;
        this.dcopNo = dcopNo;
        this.reworkCount = reworkCount;
        this.equipmentId = equipmentId;
        this.completionDate = completionDate;
        this.receiveTime = receiveTime;
        this.tartget = tartget;
        this.usl = usl;
        this.lsl = lsl;
        this.subGroupSize = subGroupSize;
        this.monitorGroupId = monitorGroupId;
        this.dcItems = dcItems;
        this.processDataInfos = processDataInfos;
    }
    
    public String getFlowSeq() {
        return flowSeq;
    }
    
    public void setFlowSeq(String flowSeq) {
        this.flowSeq = flowSeq;
    }

    /**
     * Gets the batchIDs value for this DataCollection.
     * 
     * @return batchIDs
     */
    public String getBatchIDs() {
        return batchIDs;
    }

    /**
     * Sets the batchIDs value for this DataCollection.
     * 
     * @param batchIDs
     */
    public void setBatchIDs(String batchIDs) {
        this.batchIDs = batchIDs;
    }

    /**
     * Gets the waferIDs value for this DataCollection.
     * 
     * @return waferIDs
     */
    public String getWaferIDs() {
        return waferIDs;
    }

    /**
     * Sets the waferIDs value for this DataCollection.
     * 
     * @param waferIDs
     */
    public void setWaferIDs(String waferIDs) {
        this.waferIDs = waferIDs;
    }

    /**
     * Gets the routeID value for this DataCollection.
     * 
     * @return routeID
     */
    public String getRouteID() {
        return routeID;
    }

    /**
     * Sets the routeID value for this DataCollection.
     * 
     * @param routeID
     */
    public void setRouteID(String routeID) {
        this.routeID = routeID;
    }

    /**
     * Gets the routeSeq value for this DataCollection.
     * 
     * @return routeSeq
     */
    public String getRouteSeq() {
        return routeSeq;
    }

    /**
     * Sets the routeSeq value for this DataCollection.
     * 
     * @param routeSeq
     */
    public void setRouteSeq(String routeSeq) {
        this.routeSeq = routeSeq;
    }

    /**
     * Gets the operationSeq value for this DataCollection.
     * 
     * @return operationSeq
     */
    public String getOperationSeq() {
        return operationSeq;
    }

    /**
     * Sets the operationSeq value for this DataCollection.
     * 
     * @param operationSeq
     */
    public void setOperationSeq(String operationSeq) {
        this.operationSeq = operationSeq;
    }

    /**
     * Gets the prod_6 value for this DataCollection.
     * 
     * @return prod_6
     */
    public String getProd_6() {
        return prod_6;
    }

    /**
     * Sets the prod_6 value for this DataCollection.
     * 
     * @param prod_6
     */
    public void setProd_6(String prod_6) {
        this.prod_6 = prod_6;
    }

    /**
     * Gets the prod_7 value for this DataCollection.
     * 
     * @return prod_7
     */
    public String getProd_7() {
        return prod_7;
    }

    /**
     * Sets the prod_7 value for this DataCollection.
     * 
     * @param prod_7
     */
    public void setProd_7(String prod_7) {
        this.prod_7 = prod_7;
    }

    /**
     * Gets the prod_8 value for this DataCollection.
     * 
     * @return prod_8
     */
    public String getProd_8() {
        return prod_8;
    }

    /**
     * Sets the prod_8 value for this DataCollection.
     * 
     * @param prod_8
     */
    public void setProd_8(String prod_8) {
        this.prod_8 = prod_8;
    }

    /**
     * Gets the prod_9 value for this DataCollection.
     * 
     * @return prod_9
     */
    public String getProd_9() {
        return prod_9;
    }

    /**
     * Sets the prod_9 value for this DataCollection.
     * 
     * @param prod_9
     */
    public void setProd_9(String prod_9) {
        this.prod_9 = prod_9;
    }

    /**
     * Gets the prod_10 value for this DataCollection.
     * 
     * @return prod_10
     */
    public String getProd_10() {
        return prod_10;
    }

    /**
     * Sets the prod_10 value for this DataCollection.
     * 
     * @param prod_10
     */
    public void setProd_10(String prod_10) {
        this.prod_10 = prod_10;
    }

    /**
     * Gets the batchFlag value for this DataCollection.
     * 
     * @return batchFlag
     */
    public String getBatchFlag() {
        return batchFlag;
    }

    /**
     * Sets the batchFlag value for this DataCollection.
     * 
     * @param batchFlag
     */
    public void setBatchFlag(String batchFlag) {
        this.batchFlag = batchFlag;
    }

    /**
     * Gets the fabID value for this DataCollection.
     * 
     * @return fabID
     */
    public String getFabID() {
        return fabID;
    }

    /**
     * Sets the fabID value for this DataCollection.
     * 
     * @param fabID
     */
    public void setFabID(String fabID) {
        this.fabID = fabID;
    }

    /**
     * Gets the areaID value for this DataCollection.
     * 
     * @return areaID
     */
    public String getAreaID() {
        return areaID;
    }

    /**
     * Sets the areaID value for this DataCollection.
     * 
     * @param areaID
     */
    public void setAreaID(String areaID) {
        this.areaID = areaID;
    }

    /**
     * Gets the lotId value for this DataCollection.
     * 
     * @return lotId
     */
    public String getLotId() {
        return lotId;
    }

    /**
     * Sets the lotId value for this DataCollection.
     * 
     * @param lotId
     */
    public void setLotId(String lotId) {
        this.lotId = lotId;
    }

    /**
     * Gets the lotsInBatch value for this DataCollection.
     * 
     * @return lotsInBatch
     */
    public String getLotsInBatch() {
        return lotsInBatch;
    }

    /**
     * Sets the lotsInBatch value for this DataCollection.
     * 
     * @param lotsInBatch
     */
    public void setLotsInBatch(String lotsInBatch) {
        this.lotsInBatch = lotsInBatch;
    }

    /**
     * Gets the lotType value for this DataCollection.
     * 
     * @return lotType
     */
    public String getLotType() {
        return lotType;
    }

    /**
     * Sets the lotType value for this DataCollection.
     * 
     * @param lotType
     */
    public void setLotType(String lotType) {
        this.lotType = lotType;
    }

    /**
     * Gets the customerLot value for this DataCollection.
     * 
     * @return customerLot
     */
    public String getCustomerLot() {
        return customerLot;
    }

    /**
     * Sets the customerLot value for this DataCollection.
     * 
     * @param customerLot
     */
    public void setCustomerLot(String customerLot) {
        this.customerLot = customerLot;
    }

    /**
     * Gets the recipeID value for this DataCollection.
     * 
     * @return recipeID
     */
    public String getRecipeID() {
        return recipeID;
    }

    /**
     * Sets the recipeID value for this DataCollection.
     * 
     * @param recipeID
     */
    public void setRecipeID(String recipeID) {
        this.recipeID = recipeID;
    }

    /**
     * Gets the location value for this DataCollection.
     * 
     * @return location
     */
    public String getLocation() {
        return location;
    }

    /**
     * Sets the location value for this DataCollection.
     * 
     * @param location
     */
    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * Gets the technologyID value for this DataCollection.
     * 
     * @return technologyID
     */
    public String getTechnologyID() {
        return technologyID;
    }

    /**
     * Sets the technologyID value for this DataCollection.
     * 
     * @param technologyID
     */
    public void setTechnologyID(String technologyID) {
        this.technologyID = technologyID;
    }

    /**
     * Gets the productId value for this DataCollection.
     * 
     * @return productId
     */
    public String getProductId() {
        return productId;
    }

    /**
     * Sets the productId value for this DataCollection.
     * 
     * @param productId
     */
    public void setProductId(String productId) {
        this.productId = productId;
    }

    /**
     * Gets the prod_1 value for this DataCollection.
     * 
     * @return prod_1
     */
    public String getProd_1() {
        return prod_1;
    }

    /**
     * Sets the prod_1 value for this DataCollection.
     * 
     * @param prod_1
     */
    public void setProd_1(String prod_1) {
        this.prod_1 = prod_1;
    }

    /**
     * Gets the prod_2 value for this DataCollection.
     * 
     * @return prod_2
     */
    public String getProd_2() {
        return prod_2;
    }

    /**
     * Sets the prod_2 value for this DataCollection.
     * 
     * @param prod_2
     */
    public void setProd_2(String prod_2) {
        this.prod_2 = prod_2;
    }

    /**
     * Gets the prod_3 value for this DataCollection.
     * 
     * @return prod_3
     */
    public String getProd_3() {
        return prod_3;
    }

    /**
     * Sets the prod_3 value for this DataCollection.
     * 
     * @param prod_3
     */
    public void setProd_3(String prod_3) {
        this.prod_3 = prod_3;
    }

    /**
     * Gets the prod_4 value for this DataCollection.
     * 
     * @return prod_4
     */
    public String getProd_4() {
        return prod_4;
    }

    /**
     * Sets the prod_4 value for this DataCollection.
     * 
     * @param prod_4
     */
    public void setProd_4(String prod_4) {
        this.prod_4 = prod_4;
    }

    /**
     * Gets the customer value for this DataCollection.
     * 
     * @return customer
     */
    public String getCustomer() {
        return customer;
    }

    /**
     * Sets the customer value for this DataCollection.
     * 
     * @param customer
     */
    public void setCustomer(String customer) {
        this.customer = customer;
    }

    /**
     * Gets the device value for this DataCollection.
     * 
     * @return device
     */
    public String getDevice() {
        return device;
    }

    /**
     * Sets the device value for this DataCollection.
     * 
     * @param device
     */
    public void setDevice(String device) {
        this.device = device;
    }

    /**
     * Gets the operatorID value for this DataCollection.
     * 
     * @return operatorID
     */
    public String getOperatorID() {
        return operatorID;
    }

    /**
     * Sets the operatorID value for this DataCollection.
     * 
     * @param operatorID
     */
    public void setOperatorID(String operatorID) {
        this.operatorID = operatorID;
    }

    /**
     * Gets the materialID value for this DataCollection.
     * 
     * @return materialID
     */
    public String getMaterialID() {
        return materialID;
    }

    /**
     * Sets the materialID value for this DataCollection.
     * 
     * @param materialID
     */
    public void setMaterialID(String materialID) {
        this.materialID = materialID;
    }

    /**
     * Gets the operationNumber value for this DataCollection.
     * 
     * @return operationNumber
     */
    public String getOperationNumber() {
        return operationNumber;
    }

    /**
     * Sets the operationNumber value for this DataCollection.
     * 
     * @param operationNumber
     */
    public void setOperationNumber(String operationNumber) {
        this.operationNumber = operationNumber;
    }

    /**
     * Gets the dcopNo value for this DataCollection.
     * 
     * @return dcopNo
     */
    public String getDcopNo() {
        return dcopNo;
    }

    /**
     * Sets the dcopNo value for this DataCollection.
     * 
     * @param dcopNo
     */
    public void setDcopNo(String dcopNo) {
        this.dcopNo = dcopNo;
    }

    /**
     * Gets the reworkCount value for this DataCollection.
     * 
     * @return reworkCount
     */
    public int getReworkCount() {
        return reworkCount;
    }

    /**
     * Sets the reworkCount value for this DataCollection.
     * 
     * @param reworkCount
     */
    public void setReworkCount(int reworkCount) {
        this.reworkCount = reworkCount;
    }

    /**
     * Gets the equipmentId value for this DataCollection.
     * 
     * @return equipmentId
     */
    public String getEquipmentId() {
        return equipmentId;
    }

    /**
     * Sets the equipmentId value for this DataCollection.
     * 
     * @param equipmentId
     */
    public void setEquipmentId(String equipmentId) {
        this.equipmentId = equipmentId;
    }

    /**
     * Gets the completionDate value for this DataCollection.
     * 
     * @return completionDate
     */
    public java.util.Calendar getCompletionDate() {
        return completionDate;
    }

    /**
     * Sets the completionDate value for this DataCollection.
     * 
     * @param completionDate
     */
    public void setCompletionDate(java.util.Calendar completionDate) {
        this.completionDate = completionDate;
    }

    /**
     * Gets the receiveTime value for this DataCollection.
     * 
     * @return receiveTime
     */
    public java.util.Calendar getReceiveTime() {
        return receiveTime;
    }

    /**
     * Sets the receiveTime value for this DataCollection.
     * 
     * @param receiveTime
     */
    public void setReceiveTime(java.util.Calendar receiveTime) {
        this.receiveTime = receiveTime;
    }

    /**
     * Gets the tartget value for this DataCollection.
     * 
     * @return tartget
     */
    public double getTartget() {
        return tartget;
    }

    /**
     * Sets the tartget value for this DataCollection.
     * 
     * @param tartget
     */
    public void setTartget(double tartget) {
        this.tartget = tartget;
    }

    /**
     * Gets the usl value for this DataCollection.
     * 
     * @return usl
     */
    public double getUsl() {
        return usl;
    }

    /**
     * Sets the usl value for this DataCollection.
     * 
     * @param usl
     */
    public void setUsl(double usl) {
        this.usl = usl;
    }

    /**
     * Gets the lsl value for this DataCollection.
     * 
     * @return lsl
     */
    public double getLsl() {
        return lsl;
    }

    /**
     * Sets the lsl value for this DataCollection.
     * 
     * @param lsl
     */
    public void setLsl(double lsl) {
        this.lsl = lsl;
    }

    /**
     * Gets the subGroupSize value for this DataCollection.
     * 
     * @return subGroupSize
     */
    public String getSubGroupSize() {
        return subGroupSize;
    }

    /**
     * Sets the subGroupSize value for this DataCollection.
     * 
     * @param subGroupSize
     */
    public void setSubGroupSize(String subGroupSize) {
        this.subGroupSize = subGroupSize;
    }

    /**
     * Gets the monitorGroupId value for this DataCollection.
     * 
     * @return monitorGroupId
     */
    public String getMonitorGroupId() {
        return monitorGroupId;
    }

    /**
     * Sets the monitorGroupId value for this DataCollection.
     * 
     * @param monitorGroupId
     */
    public void setMonitorGroupId(String monitorGroupId) {
        this.monitorGroupId = monitorGroupId;
    }

    /**
     * Gets the dcItems value for this DataCollection.
     * 
     * @return dcItems
     */
    public DCItem[] getDcItems() {
        return dcItems;
    }

    /**
     * Sets the dcItems value for this DataCollection.
     * 
     * @param dcItems
     */
    public void setDcItems(DCItem[] dcItems) {
        this.dcItems = dcItems;
    }

    /**
     * Gets the processDataInfos value for this DataCollection.
     * 
     * @return processDataInfos
     */
    public ProcessDataInfo[] getProcessDataInfos() {
        return processDataInfos;
    }

    /**
     * Sets the processDataInfos value for this DataCollection.
     * 
     * @param processDataInfos
     */
    public void setProcessDataInfos(ProcessDataInfo[] processDataInfos) {
        this.processDataInfos = processDataInfos;
    }

    public String[] getJobNames() {
        return this.jobNames;
    }

    public void setJobNames(String[] jobNames) {
        this.jobNames = jobNames;
    }
    
    private Object __equalsCalc = null;

    public synchronized boolean equals(Object obj) {
        if (!(obj instanceof DataCollection)) return false;
        DataCollection other = (DataCollection) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true
                  && ((this.batchIDs == null && other.getBatchIDs() == null) || (this.batchIDs != null && this.batchIDs.equals(other.getBatchIDs())))
                  && ((this.waferIDs == null && other.getWaferIDs() == null) || (this.waferIDs != null && this.waferIDs.equals(other.getWaferIDs())))
                  && ((this.routeID == null && other.getRouteID() == null) || (this.routeID != null && this.routeID.equals(other.getRouteID())))
                  && ((this.routeSeq == null && other.getRouteSeq() == null) || (this.routeSeq != null && this.routeSeq.equals(other.getRouteSeq())))
                  && ((this.operationSeq == null && other.getOperationSeq() == null) || (this.operationSeq != null && this.operationSeq.equals(other.getOperationSeq())))
                  && ((this.prod_6 == null && other.getProd_6() == null) || (this.prod_6 != null && this.prod_6.equals(other.getProd_6())))
                  && ((this.prod_7 == null && other.getProd_7() == null) || (this.prod_7 != null && this.prod_7.equals(other.getProd_7())))
                  && ((this.prod_8 == null && other.getProd_8() == null) || (this.prod_8 != null && this.prod_8.equals(other.getProd_8())))
                  && ((this.prod_9 == null && other.getProd_9() == null) || (this.prod_9 != null && this.prod_9.equals(other.getProd_9())))
                  && ((this.prod_10 == null && other.getProd_10() == null) || (this.prod_10 != null && this.prod_10.equals(other.getProd_10())))
                  && ((this.batchFlag == null && other.getBatchFlag() == null) || (this.batchFlag != null && this.batchFlag.equals(other.getBatchFlag())))
                  && ((this.fabID == null && other.getFabID() == null) || (this.fabID != null && this.fabID.equals(other.getFabID())))
                  && ((this.areaID == null && other.getAreaID() == null) || (this.areaID != null && this.areaID.equals(other.getAreaID())))
                  && ((this.lotId == null && other.getLotId() == null) || (this.lotId != null && this.lotId.equals(other.getLotId())))
                  && ((this.lotsInBatch == null && other.getLotsInBatch() == null) || (this.lotsInBatch != null && this.lotsInBatch.equals(other.getLotsInBatch())))
                  && ((this.lotType == null && other.getLotType() == null) || (this.lotType != null && this.lotType.equals(other.getLotType())))
                  && ((this.customerLot == null && other.getCustomerLot() == null) || (this.customerLot != null && this.customerLot.equals(other.getCustomerLot())))
                  && ((this.recipeID == null && other.getRecipeID() == null) || (this.recipeID != null && this.recipeID.equals(other.getRecipeID())))
                  && ((this.location == null && other.getLocation() == null) || (this.location != null && this.location.equals(other.getLocation())))
                  && ((this.technologyID == null && other.getTechnologyID() == null) || (this.technologyID != null && this.technologyID.equals(other.getTechnologyID())))
                  && ((this.productId == null && other.getProductId() == null) || (this.productId != null && this.productId.equals(other.getProductId())))
                  && ((this.prod_1 == null && other.getProd_1() == null) || (this.prod_1 != null && this.prod_1.equals(other.getProd_1())))
                  && ((this.prod_2 == null && other.getProd_2() == null) || (this.prod_2 != null && this.prod_2.equals(other.getProd_2())))
                  && ((this.prod_3 == null && other.getProd_3() == null) || (this.prod_3 != null && this.prod_3.equals(other.getProd_3())))
                  && ((this.prod_4 == null && other.getProd_4() == null) || (this.prod_4 != null && this.prod_4.equals(other.getProd_4())))
                  && ((this.customer == null && other.getCustomer() == null) || (this.customer != null && this.customer.equals(other.getCustomer())))
                  && ((this.device == null && other.getDevice() == null) || (this.device != null && this.device.equals(other.getDevice())))
                  && ((this.operatorID == null && other.getOperatorID() == null) || (this.operatorID != null && this.operatorID.equals(other.getOperatorID())))
                  && ((this.materialID == null && other.getMaterialID() == null) || (this.materialID != null && this.materialID.equals(other.getMaterialID())))
                  && ((this.operationNumber == null && other.getOperationNumber() == null) || (this.operationNumber != null && this.operationNumber.equals(other.getOperationNumber())))
                  && ((this.dcopNo == null && other.getDcopNo() == null) || (this.dcopNo != null && this.dcopNo.equals(other.getDcopNo())))
                  && this.reworkCount == other.getReworkCount()
                  && ((this.equipmentId == null && other.getEquipmentId() == null) || (this.equipmentId != null && this.equipmentId.equals(other.getEquipmentId())))
                  && ((this.completionDate == null && other.getCompletionDate() == null) || (this.completionDate != null && this.completionDate.equals(other.getCompletionDate())))
                  && ((this.receiveTime == null && other.getReceiveTime() == null) || (this.receiveTime != null && this.receiveTime.equals(other.getReceiveTime())))
                  && this.tartget == other.getTartget()
                  && this.usl == other.getUsl()
                  && this.lsl == other.getLsl()
                  && ((this.subGroupSize == null && other.getSubGroupSize() == null) || (this.subGroupSize != null && this.subGroupSize.equals(other.getSubGroupSize())))
                  && ((this.monitorGroupId == null && other.getMonitorGroupId() == null) || (this.monitorGroupId != null && this.monitorGroupId.equals(other.getMonitorGroupId())))
                  && ((this.dcItems == null && other.getDcItems() == null) || (this.dcItems != null && java.util.Arrays.equals(this.dcItems,
                                                                                                                               other.getDcItems())))
                  && ((this.processDataInfos == null && other.getProcessDataInfos() == null) || (this.processDataInfos != null && java.util.Arrays.equals(this.processDataInfos,
                                                                                                                                                          other.getProcessDataInfos())))
                  && ((this.flowSeq == null && other.getFlowSeq() == null) || (this.flowSeq != null && this.flowSeq.equals(other.getFlowSeq())))
                  && ((this.batchInfo == null && other.getBatchInfo() == null) || (this.batchInfo != null && this.batchInfo.equals(other.getBatchInfo())))
                  && ((this.batchLot == null && other.getBatchLot() == null) || (this.batchLot != null && this.batchLot.equals(other.getBatchLot())))
                  && this.batchSize == other.getBatchSize()
                  && ((this.batchProductID == null && other.getBatchProductID() == null) || (this.batchProductID != null && this.batchProductID.equals(other.getBatchProductID())))
                  && ((this.jobNames == null && other.getJobNames() == null) || (this.jobNames != null && java.util.Arrays.equals(this.jobNames,
                                                                                                                                  other.getJobNames())));

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
        if (getBatchIDs() != null) {
            _hashCode += getBatchIDs().hashCode();
        }
        if (getWaferIDs() != null) {
            _hashCode += getWaferIDs().hashCode();
        }
        if (getRouteID() != null) {
            _hashCode += getRouteID().hashCode();
        }
        if (getRouteSeq() != null) {
            _hashCode += getRouteSeq().hashCode();
        }
        if (getOperationSeq() != null) {
            _hashCode += getOperationSeq().hashCode();
        }
        if (getProd_6() != null) {
            _hashCode += getProd_6().hashCode();
        }
        if (getProd_7() != null) {
            _hashCode += getProd_7().hashCode();
        }
        if (getProd_8() != null) {
            _hashCode += getProd_8().hashCode();
        }
        if (getProd_9() != null) {
            _hashCode += getProd_9().hashCode();
        }
        if (getProd_10() != null) {
            _hashCode += getProd_10().hashCode();
        }
        if (getBatchFlag() != null) {
            _hashCode += getBatchFlag().hashCode();
        }
        if (getFabID() != null) {
            _hashCode += getFabID().hashCode();
        }
        if (getAreaID() != null) {
            _hashCode += getAreaID().hashCode();
        }
        if (getLotId() != null) {
            _hashCode += getLotId().hashCode();
        }
        if (getLotsInBatch() != null) {
            _hashCode += getLotsInBatch().hashCode();
        }
        if (getLotType() != null) {
            _hashCode += getLotType().hashCode();
        }
        if (getCustomerLot() != null) {
            _hashCode += getCustomerLot().hashCode();
        }
        if (getRecipeID() != null) {
            _hashCode += getRecipeID().hashCode();
        }
        if (getLocation() != null) {
            _hashCode += getLocation().hashCode();
        }
        if (getTechnologyID() != null) {
            _hashCode += getTechnologyID().hashCode();
        }
        if (getProductId() != null) {
            _hashCode += getProductId().hashCode();
        }
        if (getProd_1() != null) {
            _hashCode += getProd_1().hashCode();
        }
        if (getProd_2() != null) {
            _hashCode += getProd_2().hashCode();
        }
        if (getProd_3() != null) {
            _hashCode += getProd_3().hashCode();
        }
        if (getProd_4() != null) {
            _hashCode += getProd_4().hashCode();
        }
        if (getCustomer() != null) {
            _hashCode += getCustomer().hashCode();
        }
        if (getDevice() != null) {
            _hashCode += getDevice().hashCode();
        }
        if (getOperatorID() != null) {
            _hashCode += getOperatorID().hashCode();
        }
        if (getMaterialID() != null) {
            _hashCode += getMaterialID().hashCode();
        }
        if (getOperationNumber() != null) {
            _hashCode += getOperationNumber().hashCode();
        }
        if (getDcopNo() != null) {
            _hashCode += getDcopNo().hashCode();
        }
        _hashCode += getReworkCount();
        if (getEquipmentId() != null) {
            _hashCode += getEquipmentId().hashCode();
        }
        if (getCompletionDate() != null) {
            _hashCode += getCompletionDate().hashCode();
        }
        if (getReceiveTime() != null) {
            _hashCode += getReceiveTime().hashCode();
        }
        _hashCode += new Double(getTartget()).hashCode();
        _hashCode += new Double(getUsl()).hashCode();
        _hashCode += new Double(getLsl()).hashCode();
        if (getSubGroupSize() != null) {
            _hashCode += getSubGroupSize().hashCode();
        }
        if (getMonitorGroupId() != null) {
            _hashCode += getMonitorGroupId().hashCode();
        }
        if (getDcItems() != null) {
            for (int i = 0; i < java.lang.reflect.Array.getLength(getDcItems()); i++) {
                Object obj = java.lang.reflect.Array.get(getDcItems(), i);
                if (obj != null && !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getProcessDataInfos() != null) {
            for (int i = 0; i < java.lang.reflect.Array.getLength(getProcessDataInfos()); i++) {
                Object obj = java.lang.reflect.Array.get(getProcessDataInfos(), i);
                if (obj != null && !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getFlowSeq() != null) {
            _hashCode += getFlowSeq().hashCode();
        }
        if (getJobNames() != null) {
            for (int i = 0; i < java.lang.reflect.Array.getLength(getJobNames()); i++) {
                Object obj = java.lang.reflect.Array.get(getJobNames(), i);
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
                                                                                                            DataCollection.class,
                                                                                                            true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://tempuri.org/", "DataCollection"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("batchIDs");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "BatchIDs"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("waferIDs");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "WaferIDs"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("routeID");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "RouteID"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("routeSeq");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "RouteSeq"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("operationSeq");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "OperationSeq"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("prod_6");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "Prod_6"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("prod_7");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "Prod_7"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("prod_8");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "Prod_8"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("prod_9");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "Prod_9"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("prod_10");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "Prod_10"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("batchFlag");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "BatchFlag"));
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
        elemField.setFieldName("areaID");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "AreaID"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("lotId");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "LotId"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("lotsInBatch");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "LotsInBatch"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("lotType");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "LotType"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("customerLot");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "CustomerLot"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("recipeID");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "RecipeID"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("location");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "Location"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("technologyID");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "TechnologyID"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("productId");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "ProductId"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("prod_1");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "Prod_1"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("prod_2");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "Prod_2"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("prod_3");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "Prod_3"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("prod_4");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "Prod_4"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("customer");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "Customer"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("device");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "Device"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("operatorID");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "OperatorID"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("materialID");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "MaterialID"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("operationNumber");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "OperationNumber"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("dcopNo");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "DcopNo"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("reworkCount");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "ReworkCount"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("equipmentId");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "EquipmentId"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("completionDate");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "CompletionDate"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "dateTime"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("receiveTime");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "ReceiveTime"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "dateTime"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("tartget");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "Tartget"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "double"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("usl");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "Usl"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "double"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("lsl");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "Lsl"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "double"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("subGroupSize");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "SubGroupSize"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("monitorGroupId");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "MonitorGroupId"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("dcItems");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "DcItems"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://tempuri.org/", "DCItem"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        elemField.setItemQName(new javax.xml.namespace.QName("http://tempuri.org/", "DCItem"));
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("processDataInfos");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "ProcessDataInfos"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://tempuri.org/", "ProcessDataInfo"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        elemField.setItemQName(new javax.xml.namespace.QName("http://tempuri.org/", "ProcessDataInfo"));
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("flowSeq");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "FlowSeq"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://tempuri.org/", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("jobNames");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "JobNames"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        elemField.setItemQName(new javax.xml.namespace.QName("http://tempuri.org/", "string"));
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("batchInfo");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "BatchInfo"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("batchLot");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "BatchLot"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("batchSize");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "BatchSize"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("batchProductID");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "BatchProductID"));
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

    
    public String getBatchInfo() {
        return batchInfo;
    }

    
    public void setBatchInfo(String batchInfo) {
        this.batchInfo = batchInfo;
    }

    
    public String getBatchLot() {
        return batchLot;
    }

    
    public void setBatchLot(String batchLot) {
        this.batchLot = batchLot;
    }

    
    public int getBatchSize() {
        return batchSize;
    }

    
    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    
    public String getBatchProductID() {
        return batchProductID;
    }

    
    public void setBatchProductID(String batchProductID) {
        this.batchProductID = batchProductID;
    }
    
}
