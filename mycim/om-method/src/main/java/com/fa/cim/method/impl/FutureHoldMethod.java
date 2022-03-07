package com.fa.cim.method.impl;

import com.fa.cim.annotaion.OmMethod;
import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.utils.CimStringUtils;
import com.fa.cim.common.utils.Validations;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Outputs;
import com.fa.cim.method.IFutureHoldMethod;
import com.fa.cim.method.ILotMethod;
import com.fa.cim.newcore.bo.pd.CimProcessDefinition;
import com.fa.cim.newcore.bo.pd.CimProcessOperation;
import com.fa.cim.newcore.bo.product.CimLot;
import com.fa.cim.newcore.dto.product.ProductDTO;
import com.fa.cim.newcore.factory.BaseCoreFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

@OmMethod
public class FutureHoldMethod implements IFutureHoldMethod {

    private final BaseCoreFactory baseCoreFactory;
    private final RetCodeConfig retCodeConfig;

    @Autowired
    private ILotMethod lotMethod;

    @Autowired
    public FutureHoldMethod(BaseCoreFactory baseCoreFactory, RetCodeConfig retCodeConfig) {
        this.baseCoreFactory = baseCoreFactory;
        this.retCodeConfig = retCodeConfig;
    }

    @Override
    public Outputs.FutureHoldRequestsForPreviousPO getForResponsibleOperation(Infos.ObjCommon objCommon,
                                                                              ObjectIdentifier lotID,
                                                                              Infos.EffectCondition effectCondition) {
        CimLot cimLot = baseCoreFactory.getBO(CimLot.class, lotID);

        // 当EDC check 失败后，会被Hold在当前站点，不会做 process move 操作，因此需要判断，这个添加这个 Flag 的逻辑判断
        final boolean checkConditionForPO = lotMethod.lotCheckConditionForPO(objCommon, lotID);
        CimProcessOperation cimProcessOperation;
        if (checkConditionForPO) {
            cimProcessOperation = cimLot.getProcessOperation();
        } else {
            cimProcessOperation = cimLot.getPreviousProcessOperation();
        }

        Validations.check(cimProcessOperation == null, retCodeConfig.getNotFoundOperation());

        CimProcessDefinition mainProcessDefinition = cimProcessOperation.getMainProcessDefinition();

        String operationNumber = cimProcessOperation.getOperationNumber();

        boolean postFlag = CimStringUtils.equals(effectCondition.getPhase(), BizConstant.SP_FUTUREHOLD_POST);
        boolean singleTriggerFlag = CimStringUtils.equals(effectCondition.getTriggerLevel(),  BizConstant.SP_FUTUREHOLD_SINGLE);

        List<ProductDTO.FutureHoldRecord> futureHoldRecords = cimLot.findFutureHoldRecordsFor(mainProcessDefinition, operationNumber);
        List<Infos.LotHoldReq> lotHoldReqs = futureHoldRecords.stream()
                .filter(record -> (CimStringUtils.equals(effectCondition.getPhase(), BizConstant.SP_FUTUREHOLD_ALL) ||
                        postFlag == record.isPostFlag()) &&
                        (CimStringUtils.equals(effectCondition.getTriggerLevel(), BizConstant.SP_FUTUREHOLD_ALL) ||
                                singleTriggerFlag == record.isSingleTriggerFlag()))
                .map(record -> {
                    Infos.LotHoldReq lotHoldReq = new Infos.LotHoldReq();
                    lotHoldReq.setHoldType(record.getHoldType());
                    lotHoldReq.setHoldReasonCodeID(record.getReasonCode());
                    lotHoldReq.setHoldUserID(record.getRequestPerson());
                    lotHoldReq.setResponsibleOperationMark(record.isPostFlag() ?
                            BizConstant.SP_RESPONSIBLEOPERATION_PREVIOUS : BizConstant.SP_RESPONSIBLEOPERATION_CURRENT);
                    lotHoldReq.setRelatedLotID(record.getRelatedLot());
                    lotHoldReq.setClaimMemo(record.getClaimMemo());
                    return lotHoldReq;
                }).collect(Collectors.toList());

        Outputs.FutureHoldRequestsForPreviousPO retVal = new Outputs.FutureHoldRequestsForPreviousPO();
        retVal.setLotID(lotID);
        retVal.setStrLotHoldReqList(lotHoldReqs);
        return retVal;
    }

    @Override
    public Outputs.FutureHoldRequestsDeleteForPreviousPO deleteForResponsibleOperation(Infos.ObjCommon objCommon,
                                                                                       ObjectIdentifier lotID,
                                                                                       Infos.EffectCondition effectCondition) {
        CimLot cimLot = baseCoreFactory.getBO(CimLot.class, lotID);

//        // 当EDC check 失败后，会被Hold在当前站点，不会做 process move 操作，因此需要判断，这个添加这个 Flag 的逻辑判断
//        final boolean checkConditionForPO = lotMethod.lotCheckConditionForPO(objCommon, lotID);
//        CimProcessOperation processOperation;
//        if (checkConditionForPO) {
//            processOperation = cimLot.getProcessOperation();
//        } else {
//            processOperation = cimLot.getPreviousProcessOperation();
//        }
        CimProcessOperation processOperation = cimLot.getResponsibleProcessOperation();

        CimProcessDefinition mainProcessDefinition = processOperation.getMainProcessDefinition();
        String operationNumber = processOperation.getOperationNumber();

        boolean postFlag = CimStringUtils.equals(effectCondition.getPhase(), BizConstant.SP_FUTUREHOLD_POST);
        boolean singleTriggerFlag = CimStringUtils.equals(effectCondition.getTriggerLevel(),  BizConstant.SP_FUTUREHOLD_SINGLE);

        List<ProductDTO.FutureHoldRecord> futureHoldRecords = cimLot.findFutureHoldRecordsFor(mainProcessDefinition, operationNumber);
        List<Infos.LotHoldReq> lotHoldReqs = futureHoldRecords.stream()
                .filter(record -> (CimStringUtils.equals(effectCondition.getPhase(), BizConstant.SP_FUTUREHOLD_ALL) ||
                        postFlag == record.isPostFlag()) &&
                        (CimStringUtils.equals(effectCondition.getTriggerLevel(), BizConstant.SP_FUTUREHOLD_ALL) ||
                                singleTriggerFlag == record.isSingleTriggerFlag()))
                .map(record -> {
                    Infos.LotHoldReq lotHoldReq = new Infos.LotHoldReq();
                    lotHoldReq.setHoldType(record.getHoldType());
                    lotHoldReq.setHoldReasonCodeID(record.getReasonCode());
                    lotHoldReq.setHoldUserID(record.getRequestPerson());
                    lotHoldReq.setOperationNumber(operationNumber);
                    lotHoldReq.setRouteID(record.getMainProcessDefinition());
                    lotHoldReq.setRelatedLotID(record.getRelatedLot());
                    lotHoldReq.setClaimMemo(record.getClaimMemo());
                    return lotHoldReq;
                }).collect(Collectors.toList());
        Outputs.FutureHoldRequestsDeleteForPreviousPO retVal = new Outputs.FutureHoldRequestsDeleteForPreviousPO();
        retVal.setLotID(lotID);
        retVal.setStrFutureHoldReleaseReqList(lotHoldReqs);
        return retVal;
    }
}
