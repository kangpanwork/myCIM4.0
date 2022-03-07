/**
 * ServiceSoap12Stub.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.fa.cim;

public class ServiceSoap12Stub extends org.apache.axis.client.Stub implements ServiceSoap_PortType {

    private java.util.Vector                           cachedSerClasses     = new java.util.Vector();
    private java.util.Vector                           cachedSerQNames      = new java.util.Vector();
    private java.util.Vector                           cachedSerFactories   = new java.util.Vector();
    private java.util.Vector                           cachedDeserFactories = new java.util.Vector();

    static org.apache.axis.description.OperationDesc[] _operations;

    static {
        _operations = new org.apache.axis.description.OperationDesc[7];
        _initOperationDesc1();
    }

    private static void _initOperationDesc1() {
        org.apache.axis.description.OperationDesc oper;
        org.apache.axis.description.ParameterDesc param;
        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("SpcCheck");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://tempuri.org/",
                                                                                            "json"),
                                                              org.apache.axis.description.ParameterDesc.IN,
                                                              new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                                                                                            "string"),
                                                              String.class, false, false);
        param.setOmittable(true);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        oper.setReturnClass(String.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://tempuri.org/", "SpcCheckResult"));
        oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[0] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("WebServiceTest");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://tempuri.org/",
                                                                                            "strUserNo"),
                                                              org.apache.axis.description.ParameterDesc.IN,
                                                              new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                                                                                            "string"),
                                                              String.class, false, false);
        param.setOmittable(true);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        oper.setReturnClass(String.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://tempuri.org/", "WebServiceTestResult"));
        oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[1] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("SpcCheckSingle");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://tempuri.org/",
                                                                                            "lotID"),
                                                              org.apache.axis.description.ParameterDesc.IN,
                                                              new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                                                                                            "string"),
                                                              String.class, false, false);
        param.setOmittable(true);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://tempuri.org/",
                                                                                            "lotType"),
                                                              org.apache.axis.description.ParameterDesc.IN,
                                                              new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                                                                                            "string"),
                                                              String.class, false, false);
        param.setOmittable(true);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://tempuri.org/",
                                                                                            "pkg"),
                                                              org.apache.axis.description.ParameterDesc.IN,
                                                              new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                                                                                            "string"),
                                                              String.class, false, false);
        param.setOmittable(true);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://tempuri.org/",
                                                                                            "customer"),
                                                              org.apache.axis.description.ParameterDesc.IN,
                                                              new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                                                                                            "string"),
                                                              String.class, false, false);
        param.setOmittable(true);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://tempuri.org/",
                                                                                            "device"),
                                                              org.apache.axis.description.ParameterDesc.IN,
                                                              new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                                                                                            "string"),
                                                              String.class, false, false);
        param.setOmittable(true);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://tempuri.org/",
                                                                                            "customerLot"),
                                                              org.apache.axis.description.ParameterDesc.IN,
                                                              new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                                                                                            "string"),
                                                              String.class, false, false);
        param.setOmittable(true);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://tempuri.org/",
                                                                                            "recipeID"),
                                                              org.apache.axis.description.ParameterDesc.IN,
                                                              new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                                                                                            "string"),
                                                              String.class, false, false);
        param.setOmittable(true);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://tempuri.org/",
                                                                                            "prodArea"),
                                                              org.apache.axis.description.ParameterDesc.IN,
                                                              new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                                                                                            "string"),
                                                              String.class, false, false);
        param.setOmittable(true);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://tempuri.org/",
                                                                                            "location"),
                                                              org.apache.axis.description.ParameterDesc.IN,
                                                              new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                                                                                            "string"),
                                                              String.class, false, false);
        param.setOmittable(true);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://tempuri.org/",
                                                                                            "equipmentID"),
                                                              org.apache.axis.description.ParameterDesc.IN,
                                                              new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                                                                                            "string"),
                                                              String.class, false, false);
        param.setOmittable(true);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://tempuri.org/",
                                                                                            "reworkCount"),
                                                              org.apache.axis.description.ParameterDesc.IN,
                                                              new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                                                                                            "int"),
                                                              int.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://tempuri.org/",
                                                                                            "measureTime"),
                                                              org.apache.axis.description.ParameterDesc.IN,
                                                              new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                                                                                            "dateTime"),
                                                              java.util.Calendar.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://tempuri.org/",
                                                                                            "operatorID"),
                                                              org.apache.axis.description.ParameterDesc.IN,
                                                              new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                                                                                            "string"),
                                                              String.class, false, false);
        param.setOmittable(true);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://tempuri.org/",
                                                                                            "materialID"),
                                                              org.apache.axis.description.ParameterDesc.IN,
                                                              new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                                                                                            "string"),
                                                              String.class, false, false);
        param.setOmittable(true);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://tempuri.org/",
                                                                                            "target"),
                                                              org.apache.axis.description.ParameterDesc.IN,
                                                              new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                                                                                            "string"),
                                                              String.class, false, false);
        param.setOmittable(true);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://tempuri.org/",
                                                                                            "usl"),
                                                              org.apache.axis.description.ParameterDesc.IN,
                                                              new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                                                                                            "string"),
                                                              String.class, false, false);
        param.setOmittable(true);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://tempuri.org/",
                                                                                            "lsl"),
                                                              org.apache.axis.description.ParameterDesc.IN,
                                                              new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                                                                                            "string"),
                                                              String.class, false, false);
        param.setOmittable(true);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://tempuri.org/",
                                                                                            "dcopNo"),
                                                              org.apache.axis.description.ParameterDesc.IN,
                                                              new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                                                                                            "string"),
                                                              String.class, false, false);
        param.setOmittable(true);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://tempuri.org/",
                                                                                            "waferPosition"),
                                                              org.apache.axis.description.ParameterDesc.IN,
                                                              new javax.xml.namespace.QName("http://tempuri.org/",
                                                                                            "ArrayOfString"),
                                                              String[].class, false, false);
        param.setItemQName(new javax.xml.namespace.QName("http://tempuri.org/", "string"));
        param.setOmittable(true);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://tempuri.org/",
                                                                                            "waferID"),
                                                              org.apache.axis.description.ParameterDesc.IN,
                                                              new javax.xml.namespace.QName("http://tempuri.org/",
                                                                                            "ArrayOfString"),
                                                              String[].class, false, false);
        param.setItemQName(new javax.xml.namespace.QName("http://tempuri.org/", "string"));
        param.setOmittable(true);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://tempuri.org/",
                                                                                            "diePosition"),
                                                              org.apache.axis.description.ParameterDesc.IN,
                                                              new javax.xml.namespace.QName("http://tempuri.org/",
                                                                                            "ArrayOfString"),
                                                              String[].class, false, false);
        param.setItemQName(new javax.xml.namespace.QName("http://tempuri.org/", "string"));
        param.setOmittable(true);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://tempuri.org/",
                                                                                            "dieID"),
                                                              org.apache.axis.description.ParameterDesc.IN,
                                                              new javax.xml.namespace.QName("http://tempuri.org/",
                                                                                            "ArrayOfString"),
                                                              String[].class, false, false);
        param.setItemQName(new javax.xml.namespace.QName("http://tempuri.org/", "string"));
        param.setOmittable(true);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://tempuri.org/",
                                                                                            "dcItemName"),
                                                              org.apache.axis.description.ParameterDesc.IN,
                                                              new javax.xml.namespace.QName("http://tempuri.org/",
                                                                                            "ArrayOfString"),
                                                              String[].class, false, false);
        param.setItemQName(new javax.xml.namespace.QName("http://tempuri.org/", "string"));
        param.setOmittable(true);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://tempuri.org/",
                                                                                            "dcItemValue"),
                                                              org.apache.axis.description.ParameterDesc.IN,
                                                              new javax.xml.namespace.QName("http://tempuri.org/",
                                                                                            "ArrayOfString"),
                                                              String[].class, false, false);
        param.setItemQName(new javax.xml.namespace.QName("http://tempuri.org/", "string"));
        param.setOmittable(true);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        oper.setReturnClass(String.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://tempuri.org/", "SpcCheckSingleResult"));
        oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[2] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("SpcCheckExt4Void");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://tempuri.org/",
                                                                                            "lotID"),
                                                              org.apache.axis.description.ParameterDesc.IN,
                                                              new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                                                                                            "string"),
                                                              String.class, false, false);
        param.setOmittable(true);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://tempuri.org/",
                                                                                            "lotType"),
                                                              org.apache.axis.description.ParameterDesc.IN,
                                                              new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                                                                                            "string"),
                                                              String.class, false, false);
        param.setOmittable(true);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://tempuri.org/",
                                                                                            "pkg"),
                                                              org.apache.axis.description.ParameterDesc.IN,
                                                              new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                                                                                            "string"),
                                                              String.class, false, false);
        param.setOmittable(true);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://tempuri.org/",
                                                                                            "customer"),
                                                              org.apache.axis.description.ParameterDesc.IN,
                                                              new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                                                                                            "string"),
                                                              String.class, false, false);
        param.setOmittable(true);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://tempuri.org/",
                                                                                            "device"),
                                                              org.apache.axis.description.ParameterDesc.IN,
                                                              new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                                                                                            "string"),
                                                              String.class, false, false);
        param.setOmittable(true);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://tempuri.org/",
                                                                                            "customerLot"),
                                                              org.apache.axis.description.ParameterDesc.IN,
                                                              new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                                                                                            "string"),
                                                              String.class, false, false);
        param.setOmittable(true);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://tempuri.org/",
                                                                                            "recipeID"),
                                                              org.apache.axis.description.ParameterDesc.IN,
                                                              new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                                                                                            "string"),
                                                              String.class, false, false);
        param.setOmittable(true);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://tempuri.org/",
                                                                                            "prodArea"),
                                                              org.apache.axis.description.ParameterDesc.IN,
                                                              new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                                                                                            "string"),
                                                              String.class, false, false);
        param.setOmittable(true);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://tempuri.org/",
                                                                                            "location"),
                                                              org.apache.axis.description.ParameterDesc.IN,
                                                              new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                                                                                            "string"),
                                                              String.class, false, false);
        param.setOmittable(true);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://tempuri.org/",
                                                                                            "equipmentID"),
                                                              org.apache.axis.description.ParameterDesc.IN,
                                                              new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                                                                                            "string"),
                                                              String.class, false, false);
        param.setOmittable(true);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://tempuri.org/",
                                                                                            "reworkCount"),
                                                              org.apache.axis.description.ParameterDesc.IN,
                                                              new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                                                                                            "int"),
                                                              int.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://tempuri.org/",
                                                                                            "measureTime"),
                                                              org.apache.axis.description.ParameterDesc.IN,
                                                              new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                                                                                            "dateTime"),
                                                              java.util.Calendar.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://tempuri.org/",
                                                                                            "operatorID"),
                                                              org.apache.axis.description.ParameterDesc.IN,
                                                              new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                                                                                            "string"),
                                                              String.class, false, false);
        param.setOmittable(true);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://tempuri.org/",
                                                                                            "materialID"),
                                                              org.apache.axis.description.ParameterDesc.IN,
                                                              new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                                                                                            "string"),
                                                              String.class, false, false);
        param.setOmittable(true);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://tempuri.org/",
                                                                                            "target"),
                                                              org.apache.axis.description.ParameterDesc.IN,
                                                              new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                                                                                            "string"),
                                                              String.class, false, false);
        param.setOmittable(true);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://tempuri.org/",
                                                                                            "usl"),
                                                              org.apache.axis.description.ParameterDesc.IN,
                                                              new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                                                                                            "string"),
                                                              String.class, false, false);
        param.setOmittable(true);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://tempuri.org/",
                                                                                            "lsl"),
                                                              org.apache.axis.description.ParameterDesc.IN,
                                                              new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                                                                                            "string"),
                                                              String.class, false, false);
        param.setOmittable(true);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://tempuri.org/",
                                                                                            "dcopNo"),
                                                              org.apache.axis.description.ParameterDesc.IN,
                                                              new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                                                                                            "string"),
                                                              String.class, false, false);
        param.setOmittable(true);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://tempuri.org/",
                                                                                            "itemName"),
                                                              org.apache.axis.description.ParameterDesc.IN,
                                                              new javax.xml.namespace.QName("http://tempuri.org/",
                                                                                            "ArrayOfString"),
                                                              String[].class, false, false);
        param.setItemQName(new javax.xml.namespace.QName("http://tempuri.org/", "string"));
        param.setOmittable(true);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://tempuri.org/",
                                                                                            "itemValue"),
                                                              org.apache.axis.description.ParameterDesc.IN,
                                                              new javax.xml.namespace.QName("http://tempuri.org/",
                                                                                            "ArrayOfString"),
                                                              String[].class, false, false);
        param.setItemQName(new javax.xml.namespace.QName("http://tempuri.org/", "string"));
        param.setOmittable(true);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        oper.setReturnClass(String.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://tempuri.org/", "SpcCheckExt4VoidResult"));
        oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[3] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("Impjob");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://tempuri.org/",
                                                                                            "jobs"),
                                                              org.apache.axis.description.ParameterDesc.IN,
                                                              new javax.xml.namespace.QName("http://tempuri.org/",
                                                                                            "ArrayOfImpJobModel"),
                                                              ImpJobModel[].class, false,
                                                              false);
        param.setItemQName(new javax.xml.namespace.QName("http://tempuri.org/", "ImpJobModel"));
        param.setOmittable(true);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://tempuri.org/",
                                                                                            "type"),
                                                              org.apache.axis.description.ParameterDesc.IN,
                                                              new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                                                                                            "string"),
                                                              String.class, false, false);
        param.setOmittable(true);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        oper.setReturnClass(String.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://tempuri.org/", "ImpjobResult"));
        oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[4] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("GetRTJob");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://tempuri.org/",
                                                                                            "fabID"),
                                                              org.apache.axis.description.ParameterDesc.IN,
                                                              new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                                                                                            "string"),
                                                              String.class, false, false);
        param.setOmittable(true);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://tempuri.org/",
                                                                                            "jobNames"),
                                                              org.apache.axis.description.ParameterDesc.IN,
                                                              new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                                                                                            "string"),
                                                              String.class, false, false);
        param.setOmittable(true);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        oper.setReturnClass(String.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://tempuri.org/", "GetRTJobResult"));
        oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[5] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("GetChartView");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://tempuri.org/",
                                                                                            "chartName"),
                                                              org.apache.axis.description.ParameterDesc.IN,
                                                              new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                                                                                            "string"),
                                                              String.class, false, false);
        param.setOmittable(true);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        oper.setReturnClass(String.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://tempuri.org/", "GetChartViewResult"));
        oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[6] = oper;

    }

    public ServiceSoap12Stub() throws org.apache.axis.AxisFault{
        this(null);
    }

    public ServiceSoap12Stub(java.net.URL endpointURL, javax.xml.rpc.Service service) throws org.apache.axis.AxisFault{
        this(service);
        super.cachedEndpoint = endpointURL;
    }

    public ServiceSoap12Stub(javax.xml.rpc.Service service) throws org.apache.axis.AxisFault{
        if (service == null) {
            super.service = new org.apache.axis.client.Service();
        } else {
            super.service = service;
        }
        ((org.apache.axis.client.Service) super.service).setTypeMappingVersion("1.2");
        Class cls;
        javax.xml.namespace.QName qName;
        javax.xml.namespace.QName qName2;
        Class beansf = org.apache.axis.encoding.ser.BeanSerializerFactory.class;
        Class beandf = org.apache.axis.encoding.ser.BeanDeserializerFactory.class;
        Class enumsf = org.apache.axis.encoding.ser.EnumSerializerFactory.class;
        Class enumdf = org.apache.axis.encoding.ser.EnumDeserializerFactory.class;
        Class arraysf = org.apache.axis.encoding.ser.ArraySerializerFactory.class;
        Class arraydf = org.apache.axis.encoding.ser.ArrayDeserializerFactory.class;
        Class simplesf = org.apache.axis.encoding.ser.SimpleSerializerFactory.class;
        Class simpledf = org.apache.axis.encoding.ser.SimpleDeserializerFactory.class;
        Class simplelistsf = org.apache.axis.encoding.ser.SimpleListSerializerFactory.class;
        Class simplelistdf = org.apache.axis.encoding.ser.SimpleListDeserializerFactory.class;
        qName = new javax.xml.namespace.QName("http://tempuri.org/", "ArrayOfImpJobModel");
        cachedSerQNames.add(qName);
        cls = ImpJobModel[].class;
        cachedSerClasses.add(cls);
        qName = new javax.xml.namespace.QName("http://tempuri.org/", "ImpJobModel");
        qName2 = new javax.xml.namespace.QName("http://tempuri.org/", "ImpJobModel");
        cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
        cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

        qName = new javax.xml.namespace.QName("http://tempuri.org/", "ArrayOfString");
        cachedSerQNames.add(qName);
        cls = String[].class;
        cachedSerClasses.add(cls);
        qName = new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string");
        qName2 = new javax.xml.namespace.QName("http://tempuri.org/", "string");
        cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
        cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

        qName = new javax.xml.namespace.QName("http://tempuri.org/", "ImpJobModel");
        cachedSerQNames.add(qName);
        cls = ImpJobModel.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(beansf);
        cachedDeserFactories.add(beandf);

    }

    protected org.apache.axis.client.Call createCall() throws java.rmi.RemoteException {
        try {
            org.apache.axis.client.Call _call = super._createCall();
            if (super.maintainSessionSet) {
                _call.setMaintainSession(super.maintainSession);
            }
            if (super.cachedUsername != null) {
                _call.setUsername(super.cachedUsername);
            }
            if (super.cachedPassword != null) {
                _call.setPassword(super.cachedPassword);
            }
            if (super.cachedEndpoint != null) {
                _call.setTargetEndpointAddress(super.cachedEndpoint);
            }
            if (super.cachedTimeout != null) {
                _call.setTimeout(super.cachedTimeout);
            }
            if (super.cachedPortName != null) {
                _call.setPortName(super.cachedPortName);
            }
            java.util.Enumeration keys = super.cachedProperties.keys();
            while (keys.hasMoreElements()) {
                String key = (String) keys.nextElement();
                _call.setProperty(key, super.cachedProperties.get(key));
            }
            // All the type mapping information is registered
            // when the first call is made.
            // The type mapping information is actually registered in
            // the TypeMappingRegistry of the service, which
            // is the reason why registration is only needed for the first call.
            synchronized (this) {
                if (firstCall()) {
                    // must set encoding style before registering serializers
                    _call.setEncodingStyle(null);
                    for (int i = 0; i < cachedSerFactories.size(); ++i) {
                        Class cls = (Class) cachedSerClasses.get(i);
                        javax.xml.namespace.QName qName = (javax.xml.namespace.QName) cachedSerQNames.get(i);
                        Object x = cachedSerFactories.get(i);
                        if (x instanceof Class) {
                            Class sf = (Class) cachedSerFactories.get(i);
                            Class df = (Class) cachedDeserFactories.get(i);
                            _call.registerTypeMapping(cls, qName, sf, df, false);
                        } else if (x instanceof javax.xml.rpc.encoding.SerializerFactory) {
                            org.apache.axis.encoding.SerializerFactory sf = (org.apache.axis.encoding.SerializerFactory) cachedSerFactories.get(i);
                            org.apache.axis.encoding.DeserializerFactory df = (org.apache.axis.encoding.DeserializerFactory) cachedDeserFactories.get(i);
                            _call.registerTypeMapping(cls, qName, sf, df, false);
                        }
                    }
                }
            }
            return _call;
        } catch (Throwable _t) {
            throw new org.apache.axis.AxisFault("Failure trying to get the Call object", _t);
        }
    }

    public String spcCheck(String json) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[0]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("http://tempuri.org/SpcCheck");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP12_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://tempuri.org/", "SpcCheck"));

        setRequestHeaders(_call);
        setAttachments(_call);
        try {
            Object _resp = _call.invoke(new Object[] { json });

            if (_resp instanceof java.rmi.RemoteException) {
                throw (java.rmi.RemoteException) _resp;
            } else {
                extractAttachments(_call);
                try {
                    return (String) _resp;
                } catch (Exception _exception) {
                    return (String) org.apache.axis.utils.JavaUtils.convert(_resp, String.class);
                }
            }
        } catch (org.apache.axis.AxisFault axisFaultException) {
            throw axisFaultException;
        }
    }

    public String webServiceTest(String strUserNo) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[1]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("http://tempuri.org/WebServiceTest");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP12_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://tempuri.org/", "WebServiceTest"));

        setRequestHeaders(_call);
        setAttachments(_call);
        try {
            Object _resp = _call.invoke(new Object[] { strUserNo });

            if (_resp instanceof java.rmi.RemoteException) {
                throw (java.rmi.RemoteException) _resp;
            } else {
                extractAttachments(_call);
                try {
                    return (String) _resp;
                } catch (Exception _exception) {
                    return (String) org.apache.axis.utils.JavaUtils.convert(_resp, String.class);
                }
            }
        } catch (org.apache.axis.AxisFault axisFaultException) {
            throw axisFaultException;
        }
    }

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
                                 String[] dcItemValue) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[2]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("http://tempuri.org/SpcCheckSingle");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP12_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://tempuri.org/", "SpcCheckSingle"));

        setRequestHeaders(_call);
        setAttachments(_call);
        try {
            Object _resp = _call.invoke(new Object[] { lotID, lotType, pkg, customer, device,
                                                                           customerLot, recipeID, prodArea, location,
                                                                           equipmentID,
                                                                           new Integer(reworkCount),
                                                                           measureTime, operatorID, materialID, target,
                                                                           usl, lsl, dcopNo, waferPosition, waferID,
                                                                           diePosition, dieID, dcItemName,
                                                                           dcItemValue });

            if (_resp instanceof java.rmi.RemoteException) {
                throw (java.rmi.RemoteException) _resp;
            } else {
                extractAttachments(_call);
                try {
                    return (String) _resp;
                } catch (Exception _exception) {
                    return (String) org.apache.axis.utils.JavaUtils.convert(_resp, String.class);
                }
            }
        } catch (org.apache.axis.AxisFault axisFaultException) {
            throw axisFaultException;
        }
    }

    public String spcCheckExt4Void(String lotID, String lotType, String pkg,
                                   String customer, String device,
                                   String customerLot, String recipeID,
                                   String prodArea, String location,
                                   String equipmentID, int reworkCount,
                                   java.util.Calendar measureTime, String operatorID,
                                   String materialID, String target, String usl,
                                   String lsl, String dcopNo, String[] itemName,
                                   String[] itemValue) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[3]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("http://tempuri.org/SpcCheckExt4Void");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP12_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://tempuri.org/", "SpcCheckExt4Void"));

        setRequestHeaders(_call);
        setAttachments(_call);
        try {
            Object _resp = _call.invoke(new Object[] { lotID, lotType, pkg, customer, device,
                                                                           customerLot, recipeID, prodArea, location,
                                                                           equipmentID,
                                                                           new Integer(reworkCount),
                                                                           measureTime, operatorID, materialID, target,
                                                                           usl, lsl, dcopNo, itemName, itemValue });

            if (_resp instanceof java.rmi.RemoteException) {
                throw (java.rmi.RemoteException) _resp;
            } else {
                extractAttachments(_call);
                try {
                    return (String) _resp;
                } catch (Exception _exception) {
                    return (String) org.apache.axis.utils.JavaUtils.convert(_resp, String.class);
                }
            }
        } catch (org.apache.axis.AxisFault axisFaultException) {
            throw axisFaultException;
        }
    }

    public String impjob(ImpJobModel[] jobs,
                                   String type) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[4]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("http://tempuri.org/Impjob");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP12_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://tempuri.org/", "Impjob"));

        setRequestHeaders(_call);
        setAttachments(_call);
        try {
            Object _resp = _call.invoke(new Object[] { jobs, type });

            if (_resp instanceof java.rmi.RemoteException) {
                throw (java.rmi.RemoteException) _resp;
            } else {
                extractAttachments(_call);
                try {
                    return (String) _resp;
                } catch (Exception _exception) {
                    return (String) org.apache.axis.utils.JavaUtils.convert(_resp, String.class);
                }
            }
        } catch (org.apache.axis.AxisFault axisFaultException) {
            throw axisFaultException;
        }
    }

    public String getRTJob(String fabID,
                           String jobNames) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[5]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("http://tempuri.org/GetRTJob");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP12_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://tempuri.org/", "GetRTJob"));

        setRequestHeaders(_call);
        setAttachments(_call);
        try {
            Object _resp = _call.invoke(new Object[] { fabID, jobNames });

            if (_resp instanceof java.rmi.RemoteException) {
                throw (java.rmi.RemoteException) _resp;
            } else {
                extractAttachments(_call);
                try {
                    return (String) _resp;
                } catch (Exception _exception) {
                    return (String) org.apache.axis.utils.JavaUtils.convert(_resp, String.class);
                }
            }
        } catch (org.apache.axis.AxisFault axisFaultException) {
            throw axisFaultException;
        }
    }

    public String getChartView(String chartName) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[6]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("http://tempuri.org/GetChartView");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP12_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://tempuri.org/", "GetChartView"));

        setRequestHeaders(_call);
        setAttachments(_call);
        try {
            Object _resp = _call.invoke(new Object[] { chartName });

            if (_resp instanceof java.rmi.RemoteException) {
                throw (java.rmi.RemoteException) _resp;
            } else {
                extractAttachments(_call);
                try {
                    return (String) _resp;
                } catch (Exception _exception) {
                    return (String) org.apache.axis.utils.JavaUtils.convert(_resp, String.class);
                }
            }
        } catch (org.apache.axis.AxisFault axisFaultException) {
            throw axisFaultException;
        }
    }

}
