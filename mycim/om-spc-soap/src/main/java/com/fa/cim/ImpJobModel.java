/**
 * ImpJobModel.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.fa.cim;

public class ImpJobModel implements java.io.Serializable {
    private String jobName;

    private String product;

    private String process;

    private String flowSeq;

    private String m1Operation;

    private String step;

    private String stepDesc;

    private String parameter;

    private String area;

    private String EDCLevel;

    private String sampleRule;

    private Double USL;

    private Double UCL;

    private Double target;

    private Double LCL;

    private Double LSL;

    private String ruleVioloca;

    private String specVioloca;

    private String ocap;

    private Double xbar_UCL;

    private Double xbar_CL;

    private Double xbar_LCL;

    private String xbar_Rules;

    private Double RS_UCL;

    private Double RS_CL;

    private Double RS_LCL;

    private String RS_RULES;

    private Double SI_USL;

    private Double SI_UCL;

    private Double SI_CL;

    private Double SI_LCL;

    private Double SI_LSL;

    private String SI_RULES;

    private String fabID;

    private String UNITS;

    private String keyChartFlag;

    private String cpPara;

    private int              SItes;

    public ImpJobModel(){
    }

    public ImpJobModel(String jobName, String product, String process,
                       String flowSeq, String m1Operation, String step,
                       String stepDesc, String parameter, String area,
                       String EDCLevel, String sampleRule, Double USL,
                       Double UCL, Double target, Double LCL, Double LSL,
                       String ruleVioloca, String specVioloca, String ocap,
                       Double xbar_UCL, Double xbar_CL, Double xbar_LCL,
                       String xbar_Rules, Double RS_UCL, Double RS_CL,
                       Double RS_LCL, String RS_RULES, Double SI_USL,
                       Double SI_UCL, Double SI_CL, Double SI_LCL,
                       Double SI_LSL, String SI_RULES, String fabID,
                       String UNITS, String keyChartFlag, String cpPara, int SItes){
        this.jobName = jobName;
        this.product = product;
        this.process = process;
        this.flowSeq = flowSeq;
        this.m1Operation = m1Operation;
        this.step = step;
        this.stepDesc = stepDesc;
        this.parameter = parameter;
        this.area = area;
        this.EDCLevel = EDCLevel;
        this.sampleRule = sampleRule;
        this.USL = USL;
        this.UCL = UCL;
        this.target = target;
        this.LCL = LCL;
        this.LSL = LSL;
        this.ruleVioloca = ruleVioloca;
        this.specVioloca = specVioloca;
        this.ocap = ocap;
        this.xbar_UCL = xbar_UCL;
        this.xbar_CL = xbar_CL;
        this.xbar_LCL = xbar_LCL;
        this.xbar_Rules = xbar_Rules;
        this.RS_UCL = RS_UCL;
        this.RS_CL = RS_CL;
        this.RS_LCL = RS_LCL;
        this.RS_RULES = RS_RULES;
        this.SI_USL = SI_USL;
        this.SI_UCL = SI_UCL;
        this.SI_CL = SI_CL;
        this.SI_LCL = SI_LCL;
        this.SI_LSL = SI_LSL;
        this.SI_RULES = SI_RULES;
        this.fabID = fabID;
        this.UNITS = UNITS;
        this.keyChartFlag = keyChartFlag;
        this.cpPara = cpPara;
        this.SItes = SItes;
    }


    /**
     * Gets the jobName value for this ImpJobModel.
     * 
     * @return jobName
     */
    public String getJobName() {
        return jobName;
    }


    /**
     * Sets the jobName value for this ImpJobModel.
     * 
     * @param jobName
     */
    public void setJobName(String jobName) {
        this.jobName = jobName;
    }


    /**
     * Gets the product value for this ImpJobModel.
     * 
     * @return product
     */
    public String getProduct() {
        return product;
    }


    /**
     * Sets the product value for this ImpJobModel.
     * 
     * @param product
     */
    public void setProduct(String product) {
        this.product = product;
    }


    /**
     * Gets the process value for this ImpJobModel.
     * 
     * @return process
     */
    public String getProcess() {
        return process;
    }


    /**
     * Sets the process value for this ImpJobModel.
     * 
     * @param process
     */
    public void setProcess(String process) {
        this.process = process;
    }


    /**
     * Gets the flowSeq value for this ImpJobModel.
     * 
     * @return flowSeq
     */
    public String getFlowSeq() {
        return flowSeq;
    }


    /**
     * Sets the flowSeq value for this ImpJobModel.
     * 
     * @param flowSeq
     */
    public void setFlowSeq(String flowSeq) {
        this.flowSeq = flowSeq;
    }


    /**
     * Gets the m1Operation value for this ImpJobModel.
     * 
     * @return m1Operation
     */
    public String getM1Operation() {
        return m1Operation;
    }


    /**
     * Sets the m1Operation value for this ImpJobModel.
     * 
     * @param m1Operation
     */
    public void setM1Operation(String m1Operation) {
        this.m1Operation = m1Operation;
    }


    /**
     * Gets the step value for this ImpJobModel.
     * 
     * @return step
     */
    public String getStep() {
        return step;
    }


    /**
     * Sets the step value for this ImpJobModel.
     * 
     * @param step
     */
    public void setStep(String step) {
        this.step = step;
    }


    /**
     * Gets the stepDesc value for this ImpJobModel.
     * 
     * @return stepDesc
     */
    public String getStepDesc() {
        return stepDesc;
    }


    /**
     * Sets the stepDesc value for this ImpJobModel.
     * 
     * @param stepDesc
     */
    public void setStepDesc(String stepDesc) {
        this.stepDesc = stepDesc;
    }


    /**
     * Gets the parameter value for this ImpJobModel.
     * 
     * @return parameter
     */
    public String getParameter() {
        return parameter;
    }


    /**
     * Sets the parameter value for this ImpJobModel.
     * 
     * @param parameter
     */
    public void setParameter(String parameter) {
        this.parameter = parameter;
    }


    /**
     * Gets the area value for this ImpJobModel.
     * 
     * @return area
     */
    public String getArea() {
        return area;
    }


    /**
     * Sets the area value for this ImpJobModel.
     * 
     * @param area
     */
    public void setArea(String area) {
        this.area = area;
    }


    /**
     * Gets the EDCLevel value for this ImpJobModel.
     * 
     * @return EDCLevel
     */
    public String getEDCLevel() {
        return EDCLevel;
    }


    /**
     * Sets the EDCLevel value for this ImpJobModel.
     * 
     * @param EDCLevel
     */
    public void setEDCLevel(String EDCLevel) {
        this.EDCLevel = EDCLevel;
    }


    /**
     * Gets the sampleRule value for this ImpJobModel.
     * 
     * @return sampleRule
     */
    public String getSampleRule() {
        return sampleRule;
    }


    /**
     * Sets the sampleRule value for this ImpJobModel.
     * 
     * @param sampleRule
     */
    public void setSampleRule(String sampleRule) {
        this.sampleRule = sampleRule;
    }


    /**
     * Gets the USL value for this ImpJobModel.
     * 
     * @return USL
     */
    public Double getUSL() {
        return USL;
    }


    /**
     * Sets the USL value for this ImpJobModel.
     * 
     * @param USL
     */
    public void setUSL(Double USL) {
        this.USL = USL;
    }


    /**
     * Gets the UCL value for this ImpJobModel.
     * 
     * @return UCL
     */
    public Double getUCL() {
        return UCL;
    }


    /**
     * Sets the UCL value for this ImpJobModel.
     * 
     * @param UCL
     */
    public void setUCL(Double UCL) {
        this.UCL = UCL;
    }


    /**
     * Gets the target value for this ImpJobModel.
     * 
     * @return target
     */
    public Double getTarget() {
        return target;
    }


    /**
     * Sets the target value for this ImpJobModel.
     * 
     * @param target
     */
    public void setTarget(Double target) {
        this.target = target;
    }


    /**
     * Gets the LCL value for this ImpJobModel.
     * 
     * @return LCL
     */
    public Double getLCL() {
        return LCL;
    }


    /**
     * Sets the LCL value for this ImpJobModel.
     * 
     * @param LCL
     */
    public void setLCL(Double LCL) {
        this.LCL = LCL;
    }


    /**
     * Gets the LSL value for this ImpJobModel.
     * 
     * @return LSL
     */
    public Double getLSL() {
        return LSL;
    }


    /**
     * Sets the LSL value for this ImpJobModel.
     * 
     * @param LSL
     */
    public void setLSL(Double LSL) {
        this.LSL = LSL;
    }


    /**
     * Gets the ruleVioloca value for this ImpJobModel.
     * 
     * @return ruleVioloca
     */
    public String getRuleVioloca() {
        return ruleVioloca;
    }


    /**
     * Sets the ruleVioloca value for this ImpJobModel.
     * 
     * @param ruleVioloca
     */
    public void setRuleVioloca(String ruleVioloca) {
        this.ruleVioloca = ruleVioloca;
    }


    /**
     * Gets the specVioloca value for this ImpJobModel.
     * 
     * @return specVioloca
     */
    public String getSpecVioloca() {
        return specVioloca;
    }


    /**
     * Sets the specVioloca value for this ImpJobModel.
     * 
     * @param specVioloca
     */
    public void setSpecVioloca(String specVioloca) {
        this.specVioloca = specVioloca;
    }


    /**
     * Gets the ocap value for this ImpJobModel.
     * 
     * @return ocap
     */
    public String getOcap() {
        return ocap;
    }


    /**
     * Sets the ocap value for this ImpJobModel.
     * 
     * @param ocap
     */
    public void setOcap(String ocap) {
        this.ocap = ocap;
    }


    /**
     * Gets the xbar_UCL value for this ImpJobModel.
     * 
     * @return xbar_UCL
     */
    public Double getXbar_UCL() {
        return xbar_UCL;
    }


    /**
     * Sets the xbar_UCL value for this ImpJobModel.
     * 
     * @param xbar_UCL
     */
    public void setXbar_UCL(Double xbar_UCL) {
        this.xbar_UCL = xbar_UCL;
    }


    /**
     * Gets the xbar_CL value for this ImpJobModel.
     * 
     * @return xbar_CL
     */
    public Double getXbar_CL() {
        return xbar_CL;
    }


    /**
     * Sets the xbar_CL value for this ImpJobModel.
     * 
     * @param xbar_CL
     */
    public void setXbar_CL(Double xbar_CL) {
        this.xbar_CL = xbar_CL;
    }


    /**
     * Gets the xbar_LCL value for this ImpJobModel.
     * 
     * @return xbar_LCL
     */
    public Double getXbar_LCL() {
        return xbar_LCL;
    }


    /**
     * Sets the xbar_LCL value for this ImpJobModel.
     * 
     * @param xbar_LCL
     */
    public void setXbar_LCL(Double xbar_LCL) {
        this.xbar_LCL = xbar_LCL;
    }


    /**
     * Gets the xbar_Rules value for this ImpJobModel.
     * 
     * @return xbar_Rules
     */
    public String getXbar_Rules() {
        return xbar_Rules;
    }


    /**
     * Sets the xbar_Rules value for this ImpJobModel.
     * 
     * @param xbar_Rules
     */
    public void setXbar_Rules(String xbar_Rules) {
        this.xbar_Rules = xbar_Rules;
    }


    /**
     * Gets the RS_UCL value for this ImpJobModel.
     * 
     * @return RS_UCL
     */
    public Double getRS_UCL() {
        return RS_UCL;
    }


    /**
     * Sets the RS_UCL value for this ImpJobModel.
     * 
     * @param RS_UCL
     */
    public void setRS_UCL(Double RS_UCL) {
        this.RS_UCL = RS_UCL;
    }


    /**
     * Gets the RS_CL value for this ImpJobModel.
     * 
     * @return RS_CL
     */
    public Double getRS_CL() {
        return RS_CL;
    }


    /**
     * Sets the RS_CL value for this ImpJobModel.
     * 
     * @param RS_CL
     */
    public void setRS_CL(Double RS_CL) {
        this.RS_CL = RS_CL;
    }


    /**
     * Gets the RS_LCL value for this ImpJobModel.
     * 
     * @return RS_LCL
     */
    public Double getRS_LCL() {
        return RS_LCL;
    }


    /**
     * Sets the RS_LCL value for this ImpJobModel.
     * 
     * @param RS_LCL
     */
    public void setRS_LCL(Double RS_LCL) {
        this.RS_LCL = RS_LCL;
    }


    /**
     * Gets the RS_RULES value for this ImpJobModel.
     * 
     * @return RS_RULES
     */
    public String getRS_RULES() {
        return RS_RULES;
    }


    /**
     * Sets the RS_RULES value for this ImpJobModel.
     * 
     * @param RS_RULES
     */
    public void setRS_RULES(String RS_RULES) {
        this.RS_RULES = RS_RULES;
    }


    /**
     * Gets the SI_USL value for this ImpJobModel.
     * 
     * @return SI_USL
     */
    public Double getSI_USL() {
        return SI_USL;
    }


    /**
     * Sets the SI_USL value for this ImpJobModel.
     * 
     * @param SI_USL
     */
    public void setSI_USL(Double SI_USL) {
        this.SI_USL = SI_USL;
    }


    /**
     * Gets the SI_UCL value for this ImpJobModel.
     * 
     * @return SI_UCL
     */
    public Double getSI_UCL() {
        return SI_UCL;
    }


    /**
     * Sets the SI_UCL value for this ImpJobModel.
     * 
     * @param SI_UCL
     */
    public void setSI_UCL(Double SI_UCL) {
        this.SI_UCL = SI_UCL;
    }


    /**
     * Gets the SI_CL value for this ImpJobModel.
     * 
     * @return SI_CL
     */
    public Double getSI_CL() {
        return SI_CL;
    }


    /**
     * Sets the SI_CL value for this ImpJobModel.
     * 
     * @param SI_CL
     */
    public void setSI_CL(Double SI_CL) {
        this.SI_CL = SI_CL;
    }


    /**
     * Gets the SI_LCL value for this ImpJobModel.
     * 
     * @return SI_LCL
     */
    public Double getSI_LCL() {
        return SI_LCL;
    }


    /**
     * Sets the SI_LCL value for this ImpJobModel.
     * 
     * @param SI_LCL
     */
    public void setSI_LCL(Double SI_LCL) {
        this.SI_LCL = SI_LCL;
    }


    /**
     * Gets the SI_LSL value for this ImpJobModel.
     * 
     * @return SI_LSL
     */
    public Double getSI_LSL() {
        return SI_LSL;
    }


    /**
     * Sets the SI_LSL value for this ImpJobModel.
     * 
     * @param SI_LSL
     */
    public void setSI_LSL(Double SI_LSL) {
        this.SI_LSL = SI_LSL;
    }


    /**
     * Gets the SI_RULES value for this ImpJobModel.
     * 
     * @return SI_RULES
     */
    public String getSI_RULES() {
        return SI_RULES;
    }


    /**
     * Sets the SI_RULES value for this ImpJobModel.
     * 
     * @param SI_RULES
     */
    public void setSI_RULES(String SI_RULES) {
        this.SI_RULES = SI_RULES;
    }


    /**
     * Gets the fabID value for this ImpJobModel.
     * 
     * @return fabID
     */
    public String getFabID() {
        return fabID;
    }


    /**
     * Sets the fabID value for this ImpJobModel.
     * 
     * @param fabID
     */
    public void setFabID(String fabID) {
        this.fabID = fabID;
    }


    /**
     * Gets the UNITS value for this ImpJobModel.
     * 
     * @return UNITS
     */
    public String getUNITS() {
        return UNITS;
    }


    /**
     * Sets the UNITS value for this ImpJobModel.
     * 
     * @param UNITS
     */
    public void setUNITS(String UNITS) {
        this.UNITS = UNITS;
    }


    /**
     * Gets the keyChartFlag value for this ImpJobModel.
     * 
     * @return keyChartFlag
     */
    public String getKeyChartFlag() {
        return keyChartFlag;
    }


    /**
     * Sets the keyChartFlag value for this ImpJobModel.
     * 
     * @param keyChartFlag
     */
    public void setKeyChartFlag(String keyChartFlag) {
        this.keyChartFlag = keyChartFlag;
    }


    /**
     * Gets the cpPara value for this ImpJobModel.
     * 
     * @return cpPara
     */
    public String getCpPara() {
        return cpPara;
    }


    /**
     * Sets the cpPara value for this ImpJobModel.
     * 
     * @param cpPara
     */
    public void setCpPara(String cpPara) {
        this.cpPara = cpPara;
    }


    /**
     * Gets the SItes value for this ImpJobModel.
     * 
     * @return SItes
     */
    public int getSItes() {
        return SItes;
    }


    /**
     * Sets the SItes value for this ImpJobModel.
     * 
     * @param SItes
     */
    public void setSItes(int SItes) {
        this.SItes = SItes;
    }

    private Object __equalsCalc = null;
    public synchronized boolean equals(Object obj) {
        if (!(obj instanceof ImpJobModel)) return false;
        ImpJobModel other = (ImpJobModel) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true
                  && ((this.jobName == null && other.getJobName() == null) || (this.jobName != null && this.jobName.equals(other.getJobName())))
                  && ((this.product == null && other.getProduct() == null) || (this.product != null && this.product.equals(other.getProduct())))
                  && ((this.process == null && other.getProcess() == null) || (this.process != null && this.process.equals(other.getProcess())))
                  && ((this.flowSeq == null && other.getFlowSeq() == null) || (this.flowSeq != null && this.flowSeq.equals(other.getFlowSeq())))
                  && ((this.m1Operation == null && other.getM1Operation() == null) || (this.m1Operation != null && this.m1Operation.equals(other.getM1Operation())))
                  && ((this.step == null && other.getStep() == null) || (this.step != null && this.step.equals(other.getStep())))
                  && ((this.stepDesc == null && other.getStepDesc() == null) || (this.stepDesc != null && this.stepDesc.equals(other.getStepDesc())))
                  && ((this.parameter == null && other.getParameter() == null) || (this.parameter != null && this.parameter.equals(other.getParameter())))
                  && ((this.area == null && other.getArea() == null) || (this.area != null && this.area.equals(other.getArea())))
                  && ((this.EDCLevel == null && other.getEDCLevel() == null) || (this.EDCLevel != null && this.EDCLevel.equals(other.getEDCLevel())))
                  && ((this.sampleRule == null && other.getSampleRule() == null) || (this.sampleRule != null && this.sampleRule.equals(other.getSampleRule())))
                  && ((this.USL == null && other.getUSL() == null) || (this.USL != null && this.USL.equals(other.getUSL())))
                  && ((this.UCL == null && other.getUCL() == null) || (this.UCL != null && this.UCL.equals(other.getUCL())))
                  && ((this.target == null && other.getTarget() == null) || (this.target != null && this.target.equals(other.getTarget())))
                  && ((this.LCL == null && other.getLCL() == null) || (this.LCL != null && this.LCL.equals(other.getLCL())))
                  && ((this.LSL == null && other.getLSL() == null) || (this.LSL != null && this.LSL.equals(other.getLSL())))
                  && ((this.ruleVioloca == null && other.getRuleVioloca() == null) || (this.ruleVioloca != null && this.ruleVioloca.equals(other.getRuleVioloca())))
                  && ((this.specVioloca == null && other.getSpecVioloca() == null) || (this.specVioloca != null && this.specVioloca.equals(other.getSpecVioloca())))
                  && ((this.ocap == null && other.getOcap() == null) || (this.ocap != null && this.ocap.equals(other.getOcap())))
                  && ((this.xbar_UCL == null && other.getXbar_UCL() == null) || (this.xbar_UCL != null && this.xbar_UCL.equals(other.getXbar_UCL())))
                  && ((this.xbar_CL == null && other.getXbar_CL() == null) || (this.xbar_CL != null && this.xbar_CL.equals(other.getXbar_CL())))
                  && ((this.xbar_LCL == null && other.getXbar_LCL() == null) || (this.xbar_LCL != null && this.xbar_LCL.equals(other.getXbar_LCL())))
                  && ((this.xbar_Rules == null && other.getXbar_Rules() == null) || (this.xbar_Rules != null && this.xbar_Rules.equals(other.getXbar_Rules())))
                  && ((this.RS_UCL == null && other.getRS_UCL() == null) || (this.RS_UCL != null && this.RS_UCL.equals(other.getRS_UCL())))
                  && ((this.RS_CL == null && other.getRS_CL() == null) || (this.RS_CL != null && this.RS_CL.equals(other.getRS_CL())))
                  && ((this.RS_LCL == null && other.getRS_LCL() == null) || (this.RS_LCL != null && this.RS_LCL.equals(other.getRS_LCL())))
                  && ((this.RS_RULES == null && other.getRS_RULES() == null) || (this.RS_RULES != null && this.RS_RULES.equals(other.getRS_RULES())))
                  && ((this.SI_USL == null && other.getSI_USL() == null) || (this.SI_USL != null && this.SI_USL.equals(other.getSI_USL())))
                  && ((this.SI_UCL == null && other.getSI_UCL() == null) || (this.SI_UCL != null && this.SI_UCL.equals(other.getSI_UCL())))
                  && ((this.SI_CL == null && other.getSI_CL() == null) || (this.SI_CL != null && this.SI_CL.equals(other.getSI_CL())))
                  && ((this.SI_LCL == null && other.getSI_LCL() == null) || (this.SI_LCL != null && this.SI_LCL.equals(other.getSI_LCL())))
                  && ((this.SI_LSL == null && other.getSI_LSL() == null) || (this.SI_LSL != null && this.SI_LSL.equals(other.getSI_LSL())))
                  && ((this.SI_RULES == null && other.getSI_RULES() == null) || (this.SI_RULES != null && this.SI_RULES.equals(other.getSI_RULES())))
                  && ((this.fabID == null && other.getFabID() == null) || (this.fabID != null && this.fabID.equals(other.getFabID())))
                  && ((this.UNITS == null && other.getUNITS() == null) || (this.UNITS != null && this.UNITS.equals(other.getUNITS())))
                  && ((this.keyChartFlag == null && other.getKeyChartFlag() == null) || (this.keyChartFlag != null && this.keyChartFlag.equals(other.getKeyChartFlag())))
                  && ((this.cpPara == null && other.getCpPara() == null) || (this.cpPara != null && this.cpPara.equals(other.getCpPara())))
                  && this.SItes == other.getSItes();
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
        if (getJobName() != null) {
            _hashCode += getJobName().hashCode();
        }
        if (getProduct() != null) {
            _hashCode += getProduct().hashCode();
        }
        if (getProcess() != null) {
            _hashCode += getProcess().hashCode();
        }
        if (getFlowSeq() != null) {
            _hashCode += getFlowSeq().hashCode();
        }
        if (getM1Operation() != null) {
            _hashCode += getM1Operation().hashCode();
        }
        if (getStep() != null) {
            _hashCode += getStep().hashCode();
        }
        if (getStepDesc() != null) {
            _hashCode += getStepDesc().hashCode();
        }
        if (getParameter() != null) {
            _hashCode += getParameter().hashCode();
        }
        if (getArea() != null) {
            _hashCode += getArea().hashCode();
        }
        if (getEDCLevel() != null) {
            _hashCode += getEDCLevel().hashCode();
        }
        if (getSampleRule() != null) {
            _hashCode += getSampleRule().hashCode();
        }
        if (getUSL() != null) {
            _hashCode += getUSL().hashCode();
        }
        if (getUCL() != null) {
            _hashCode += getUCL().hashCode();
        }
        if (getTarget() != null) {
            _hashCode += getTarget().hashCode();
        }
        if (getLCL() != null) {
            _hashCode += getLCL().hashCode();
        }
        if (getLSL() != null) {
            _hashCode += getLSL().hashCode();
        }
        if (getRuleVioloca() != null) {
            _hashCode += getRuleVioloca().hashCode();
        }
        if (getSpecVioloca() != null) {
            _hashCode += getSpecVioloca().hashCode();
        }
        if (getOcap() != null) {
            _hashCode += getOcap().hashCode();
        }
        if (getXbar_UCL() != null) {
            _hashCode += getXbar_UCL().hashCode();
        }
        if (getXbar_CL() != null) {
            _hashCode += getXbar_CL().hashCode();
        }
        if (getXbar_LCL() != null) {
            _hashCode += getXbar_LCL().hashCode();
        }
        if (getXbar_Rules() != null) {
            _hashCode += getXbar_Rules().hashCode();
        }
        if (getRS_UCL() != null) {
            _hashCode += getRS_UCL().hashCode();
        }
        if (getRS_CL() != null) {
            _hashCode += getRS_CL().hashCode();
        }
        if (getRS_LCL() != null) {
            _hashCode += getRS_LCL().hashCode();
        }
        if (getRS_RULES() != null) {
            _hashCode += getRS_RULES().hashCode();
        }
        if (getSI_USL() != null) {
            _hashCode += getSI_USL().hashCode();
        }
        if (getSI_UCL() != null) {
            _hashCode += getSI_UCL().hashCode();
        }
        if (getSI_CL() != null) {
            _hashCode += getSI_CL().hashCode();
        }
        if (getSI_LCL() != null) {
            _hashCode += getSI_LCL().hashCode();
        }
        if (getSI_LSL() != null) {
            _hashCode += getSI_LSL().hashCode();
        }
        if (getSI_RULES() != null) {
            _hashCode += getSI_RULES().hashCode();
        }
        if (getFabID() != null) {
            _hashCode += getFabID().hashCode();
        }
        if (getUNITS() != null) {
            _hashCode += getUNITS().hashCode();
        }
        if (getKeyChartFlag() != null) {
            _hashCode += getKeyChartFlag().hashCode();
        }
        if (getCpPara() != null) {
            _hashCode += getCpPara().hashCode();
        }
        _hashCode += getSItes();
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc = new org.apache.axis.description.TypeDesc(
                                                                                                            ImpJobModel.class,
                                                                                                            true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://tempuri.org/", "ImpJobModel"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("jobName");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "JobName"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("product");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "Product"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("process");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "Process"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("flowSeq");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "FlowSeq"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("m1Operation");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "M1Operation"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("step");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "Step"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("stepDesc");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "StepDesc"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("parameter");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "Parameter"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("area");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "Area"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("EDCLevel");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "EDCLevel"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("sampleRule");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "SampleRule"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("USL");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "USL"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "double"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("UCL");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "UCL"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "double"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("target");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "Target"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "double"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("LCL");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "LCL"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "double"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("LSL");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "LSL"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "double"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("ruleVioloca");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "RuleVioloca"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("specVioloca");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "SpecVioloca"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("ocap");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "Ocap"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("xbar_UCL");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "Xbar_UCL"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "double"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("xbar_CL");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "Xbar_CL"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "double"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("xbar_LCL");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "Xbar_LCL"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "double"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("xbar_Rules");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "Xbar_Rules"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("RS_UCL");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "RS_UCL"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "double"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("RS_CL");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "RS_CL"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "double"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("RS_LCL");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "RS_LCL"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "double"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("RS_RULES");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "RS_RULES"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("SI_USL");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "SI_USL"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "double"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("SI_UCL");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "SI_UCL"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "double"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("SI_CL");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "SI_CL"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "double"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("SI_LCL");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "SI_LCL"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "double"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("SI_LSL");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "SI_LSL"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "double"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("SI_RULES");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "SI_RULES"));
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
        elemField.setFieldName("UNITS");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "UNITS"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("keyChartFlag");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "KeyChartFlag"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("cpPara");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "CpPara"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("SItes");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "SItes"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
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

}
