package com.fa.cim;

public class NonRTServiceSoapProxy implements NonRTServiceSoap {
  private String _endpoint = null;
  private NonRTServiceSoap service1Soap = null;
  
  public NonRTServiceSoapProxy() {
    _initService1SoapProxy();
  }
  
  public NonRTServiceSoapProxy(String endpoint) {
    _endpoint = endpoint;
    _initService1SoapProxy();
  }
  
  private void _initService1SoapProxy() {
    try {
      service1Soap = (new NonRTServiceLocator()).getService1Soap();
      if (service1Soap != null) {
        if (_endpoint != null)
          ((javax.xml.rpc.Stub)service1Soap)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
        else
          _endpoint = (String)((javax.xml.rpc.Stub)service1Soap)._getProperty("javax.xml.rpc.service.endpoint.address");
      }
      
    }
    catch (javax.xml.rpc.ServiceException serviceException) {}
  }
  
  public String getEndpoint() {
    return _endpoint;
  }
  
  public void setEndpoint(String endpoint) {
    _endpoint = endpoint;
    if (service1Soap != null)
      ((javax.xml.rpc.Stub)service1Soap)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
    
  }
  
  public NonRTServiceSoap getService1Soap() {
    if (service1Soap == null)
      _initService1SoapProxy();
    return service1Soap;
  }
  
  public NonRTUploadCheckResult checkNonRTUploadData(NonRTUploadData req) throws java.rmi.RemoteException{
    if (service1Soap == null)
      _initService1SoapProxy();
    return service1Soap.checkNonRTUploadData(req);
  }
  
  public NonRTUploadResult nonRTUploadData(NonRTUploadData reqData, String userId) throws java.rmi.RemoteException{
    if (service1Soap == null)
      _initService1SoapProxy();
    return service1Soap.nonRTUploadData(reqData, userId);
  }
  
  public String getNonRTJob(String key1) throws java.rmi.RemoteException{
    if (service1Soap == null)
      _initService1SoapProxy();
    return service1Soap.getNonRTJob(key1);
  }
  
  public String MESNonRTUploadData(NonRTUploadData reqData, String userId) throws java.rmi.RemoteException{
    if (service1Soap == null)
      _initService1SoapProxy();
    return service1Soap.MESNonRTUploadData(reqData, userId);
  }
  
  public ServiceRes isValidUser(IsValidUserReq req) throws java.rmi.RemoteException{
    if (service1Soap == null)
      _initService1SoapProxy();
    return service1Soap.isValidUser(req);
  }
  
  public String test() throws java.rmi.RemoteException {
      if (service1Soap == null) _initService1SoapProxy();
      return service1Soap.test();
  }
}