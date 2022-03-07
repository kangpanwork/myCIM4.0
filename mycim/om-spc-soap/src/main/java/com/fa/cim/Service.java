/**
 * Service.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.fa.cim;

public interface Service extends javax.xml.rpc.Service {
    public String getServiceSoapAddress();

    public ServiceSoap_PortType getServiceSoap() throws javax.xml.rpc.ServiceException;

    public ServiceSoap_PortType getServiceSoap(java.net.URL portAddress) throws javax.xml.rpc.ServiceException;
    public String getServiceSoap12Address();

    public ServiceSoap_PortType getServiceSoap12() throws javax.xml.rpc.ServiceException;

    public ServiceSoap_PortType getServiceSoap12(java.net.URL portAddress) throws javax.xml.rpc.ServiceException;
}
