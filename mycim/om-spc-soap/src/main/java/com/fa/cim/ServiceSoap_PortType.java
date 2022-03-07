/**
 * ServiceSoap_PortType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.fa.cim;

public interface ServiceSoap_PortType extends java.rmi.Remote {

    public String spcCheck(String json) throws java.rmi.RemoteException;

    /**
     * WebServiceTest
     */
    public String webServiceTest(String strUserNo) throws java.rmi.RemoteException;

    public String spcCheckSingle(String lotID, String lotType, String pkg,
                                 String customer, String device,
                                 String customerLot, String recipeID,
                                 String prodArea, String location,
                                 String equipmentID, int reworkCount,
                                 java.util.Calendar measureTime, String operatorID,
                                 String materialID, String target, String usl,
                                 String lsl, String dcopNo,
                                 String[] waferPosition, String[] waferID,
                                 String[] diePosition, String[] dieID,
                                 String[] dcItemName,
                                 String[] dcItemValue) throws java.rmi.RemoteException;

    public String spcCheckExt4Void(String lotID, String lotType, String pkg,
                                   String customer, String device,
                                   String customerLot, String recipeID,
                                   String prodArea, String location,
                                   String equipmentID, int reworkCount,
                                   java.util.Calendar measureTime, String operatorID,
                                   String materialID, String target, String usl,
                                   String lsl, String dcopNo, String[] itemName,
                                   String[] itemValue) throws java.rmi.RemoteException;

    public String impjob(ImpJobModel[] jobs,
                         String type) throws java.rmi.RemoteException;

    public String getRTJob(String fabID, String jobNames) throws java.rmi.RemoteException;

    public String getChartView(String chartName) throws java.rmi.RemoteException;

}
