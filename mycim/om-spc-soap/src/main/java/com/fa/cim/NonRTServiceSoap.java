/**
 * Service1Soap.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.fa.cim;

public interface NonRTServiceSoap extends java.rmi.Remote {
    public NonRTUploadCheckResult checkNonRTUploadData(NonRTUploadData req) throws java.rmi.RemoteException;
    public NonRTUploadResult nonRTUploadData(NonRTUploadData reqData, String userId) throws java.rmi.RemoteException;
    public String getNonRTJob(String key1) throws java.rmi.RemoteException;
    public String MESNonRTUploadData(NonRTUploadData reqData, String userId) throws java.rmi.RemoteException;
    public ServiceRes isValidUser(IsValidUserReq req) throws java.rmi.RemoteException;
    public String test() throws java.rmi.RemoteException;
}
