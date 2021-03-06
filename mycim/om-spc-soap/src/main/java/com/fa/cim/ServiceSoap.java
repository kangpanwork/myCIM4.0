
package com.fa.cim;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;


/**
 * This class was generated by the JAX-WS RI.
 * JAX-WS RI 2.2.9-b130926.1035
 * Generated source version: 2.2
 * 
 */
@WebService(name = "ServiceSoap", targetNamespace = "http://tempuri.org/")
@XmlSeeAlso({
    ObjectFactory.class
})
public interface ServiceSoap {


    /**
     * 
     * @param json
     * @return
     *     returns java.lang.String
     */
    @WebMethod(operationName = "SpcCheck", action = "http://tempuri.org/SpcCheck")
    @WebResult(name = "SpcCheckResult", targetNamespace = "http://tempuri.org/")
    @RequestWrapper(localName = "SpcCheck", targetNamespace = "http://tempuri.org/", className = "com.fa.cim.SpcCheck")
    @ResponseWrapper(localName = "SpcCheckResponse", targetNamespace = "http://tempuri.org/", className = "com.fa.cim.SpcCheckResponse")
    public String spcCheck(
            @WebParam(name = "json", targetNamespace = "http://tempuri.org/")
                    String json);


    /**
     * WebServiceTest
     * 
     * @param strUserNo
     * @return
     *     returns java.lang.String
     */
    @WebMethod(operationName = "WebServiceTest", action = "http://tempuri.org/WebServiceTest")
    @WebResult(name = "WebServiceTestResult", targetNamespace = "http://tempuri.org/")
    @RequestWrapper(localName = "WebServiceTest", targetNamespace = "http://tempuri.org/", className = "com.fa.cim.WebServiceTest")
    @ResponseWrapper(localName = "WebServiceTestResponse", targetNamespace = "http://tempuri.org/", className = "com.fa.cim.WebServiceTestResponse")
    public String webServiceTest(
            @WebParam(name = "strUserNo", targetNamespace = "http://tempuri.org/")
                    String strUserNo);

    /**
     * 
     * @param fabID
     * @param jobNames
     * @return
     *     returns java.lang.String
     */
    @WebMethod(operationName = "GetRTJob", action = "http://tempuri.org/GetRTJob")
    @WebResult(name = "GetRTJobResult", targetNamespace = "http://tempuri.org/")
    @RequestWrapper(localName = "GetRTJob", targetNamespace = "http://tempuri.org/", className = "com.fa.cim.GetRTJob")
    @ResponseWrapper(localName = "GetRTJobResponse", targetNamespace = "http://tempuri.org/", className = "com.fa.cim.GetRTJobResponse")
    public String getRTJob(
            @WebParam(name = "fabID", targetNamespace = "http://tempuri.org/")
                    String fabID,
            @WebParam(name = "jobNames", targetNamespace = "http://tempuri.org/")
                    String jobNames);

    /**
     * 
     * @param chartName
     * @return
     *     returns java.lang.String
     */
    @WebMethod(operationName = "GetChartView", action = "http://tempuri.org/GetChartView")
    @WebResult(name = "GetChartViewResult", targetNamespace = "http://tempuri.org/")
    @RequestWrapper(localName = "GetChartView", targetNamespace = "http://tempuri.org/", className = "com.fa.cim.GetChartView")
    @ResponseWrapper(localName = "GetChartViewResponse", targetNamespace = "http://tempuri.org/", className = "com.fa.cim.GetChartViewResponse")
    public String getChartView(
            @WebParam(name = "chartName", targetNamespace = "http://tempuri.org/")
                    String chartName);

}
