/**
 * ServiceLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.fa.cim;

public class ServiceLocator extends org.apache.axis.client.Service implements Service {

    public ServiceLocator(){
    }

    public ServiceLocator(org.apache.axis.EngineConfiguration config){
        super(config);
    }

    public ServiceLocator(String wsdlLoc,
                          javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException{
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for ServiceSoap
    private String ServiceSoap_address = "http://118.123.246.35:58080/RTService/Service.asmx";

    public String getServiceSoapAddress() {
        return ServiceSoap_address;
    }

    // The WSDD service name defaults to the port name.
    private String ServiceSoapWSDDServiceName = "ServiceSoap";

    public String getServiceSoapWSDDServiceName() {
        return ServiceSoapWSDDServiceName;
    }

    public void setServiceSoapWSDDServiceName(String name) {
        ServiceSoapWSDDServiceName = name;
    }

    public ServiceSoap_PortType getServiceSoap() throws javax.xml.rpc.ServiceException {
        java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(ServiceSoap_address);
        } catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getServiceSoap(endpoint);
    }

    public ServiceSoap_PortType getServiceSoap(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            ServiceSoap_BindingStub _stub = new ServiceSoap_BindingStub(portAddress,
                                                                                                                                    this);
            _stub.setPortName(getServiceSoapWSDDServiceName());
            return _stub;
        } catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setServiceSoapEndpointAddress(String address) {
        ServiceSoap_address = address;
    }

    // Use to get a proxy class for ServiceSoap12
    private String ServiceSoap12_address = "http://118.123.246.35:58080/RTService/Service.asmx";

    public String getServiceSoap12Address() {
        return ServiceSoap12_address;
    }

    // The WSDD service name defaults to the port name.
    private String ServiceSoap12WSDDServiceName = "ServiceSoap12";

    public String getServiceSoap12WSDDServiceName() {
        return ServiceSoap12WSDDServiceName;
    }

    public void setServiceSoap12WSDDServiceName(String name) {
        ServiceSoap12WSDDServiceName = name;
    }

    public ServiceSoap_PortType getServiceSoap12() throws javax.xml.rpc.ServiceException {
        java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(ServiceSoap12_address);
        } catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getServiceSoap12(endpoint);
    }

    public ServiceSoap_PortType getServiceSoap12(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            ServiceSoap12Stub _stub = new ServiceSoap12Stub(portAddress,
                                                                                                                        this);
            _stub.setPortName(getServiceSoap12WSDDServiceName());
            return _stub;
        } catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setServiceSoap12EndpointAddress(String address) {
        ServiceSoap12_address = address;
    }

    /**
     * For the given interface, get the stub implementation. If this service has no port for the given interface, then
     * ServiceException is thrown. This service has multiple ports for a given interface; the proxy implementation
     * returned may be indeterminate.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (ServiceSoap_PortType.class.isAssignableFrom(serviceEndpointInterface)) {
                ServiceSoap_BindingStub _stub = new ServiceSoap_BindingStub(new java.net.URL(ServiceSoap_address),
                                                                                                                                        this);
                _stub.setPortName(getServiceSoapWSDDServiceName());
                return _stub;
            }
            if (ServiceSoap_PortType.class.isAssignableFrom(serviceEndpointInterface)) {
                ServiceSoap12Stub _stub = new ServiceSoap12Stub(new java.net.URL(ServiceSoap12_address),
                                                                                                                            this);
                _stub.setPortName(getServiceSoap12WSDDServiceName());
                return _stub;
            }
        } catch (Throwable t) {
            throw new javax.xml.rpc.ServiceException(t);
        }
        throw new javax.xml.rpc.ServiceException("There is no stub implementation for the interface:  "
                                                 + (serviceEndpointInterface == null ? "null" : serviceEndpointInterface.getName()));
    }

    /**
     * For the given interface, get the stub implementation. If this service has no port for the given interface, then
     * ServiceException is thrown.
     */
    public java.rmi.Remote getPort(javax.xml.namespace.QName portName,
                                   Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        if (portName == null) {
            return getPort(serviceEndpointInterface);
        }
        String inputPortName = portName.getLocalPart();
        if ("ServiceSoap".equals(inputPortName)) {
            return getServiceSoap();
        } else if ("ServiceSoap12".equals(inputPortName)) {
            return getServiceSoap12();
        } else {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://tempuri.org/", "Service");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("http://tempuri.org/", "ServiceSoap"));
            ports.add(new javax.xml.namespace.QName("http://tempuri.org/", "ServiceSoap12"));
        }
        return ports.iterator();
    }

    /**
     * Set the endpoint address for the specified port name.
     */
    public void setEndpointAddress(String portName,
                                   String address) throws javax.xml.rpc.ServiceException {

        if ("ServiceSoap".equals(portName)) {
            setServiceSoapEndpointAddress(address);
        } else if ("ServiceSoap12".equals(portName)) {
            setServiceSoap12EndpointAddress(address);
        } else { // Unknown Port Name
            throw new javax.xml.rpc.ServiceException(" Cannot set Endpoint Address for Unknown Port" + portName);
        }
    }

    /**
     * Set the endpoint address for the specified port name.
     */
    public void setEndpointAddress(javax.xml.namespace.QName portName,
                                   String address) throws javax.xml.rpc.ServiceException {
        setEndpointAddress(portName.getLocalPart(), address);
    }

}
