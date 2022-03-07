package com.fa.cim.service.pprocess.impl;

import com.fa.cim.annotaion.OmService;
import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.exception.CimException;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.utils.CimArrayUtils;
import com.fa.cim.common.utils.Validations;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.method.ILotMethod;
import com.fa.cim.service.lot.ILotService;
import com.fa.cim.service.pprocess.PostProcessHoldService;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@OmService
@Slf4j
public class PostProcessHoldServiceImpl implements PostProcessHoldService {

    private final ILotService lotService;
    private final ILotMethod lotMethod;
    private final RetCodeConfig retCodeConfig;

    public PostProcessHoldServiceImpl(ILotService lotService, ILotMethod lotMethod, RetCodeConfig retCodeConfig) {
        this.lotService = lotService;
        this.lotMethod = lotMethod;
        this.retCodeConfig = retCodeConfig;
    }

    @Override
    public Infos.LotHoldReq sxLockHoldOnLot(Infos.ObjCommon objCommon, ObjectIdentifier lotID) {
        Infos.LotHoldReq lotHoldReq = new Infos.LotHoldReq();
        lotHoldReq.setHoldType(BizConstant.SP_HOLDTYPE_LOTHOLD);
        lotHoldReq.setHoldReasonCodeID(ObjectIdentifier.buildWithValue(BizConstant.SP_REASON_LOTLOCK));
        lotHoldReq.setHoldUserID(ObjectIdentifier.buildWithValue(BizConstant.SP_POSTPROC_PERSON));
        lotHoldReq.setResponsibleOperationMark(BizConstant.SP_RESPONSIBLEOPERATION_CURRENT);
        lotHoldReq.setRelatedLotID(ObjectIdentifier.emptyIdentifier());
        lotHoldReq.setClaimMemo("");

        //-----------------------------
        // For PostProcess Hold
        //-----------------------------
        Infos.ObjCommon postObjCommon = objCommon.duplicate();
        postObjCommon.setTransactionID(TransactionIDEnum.POST_PROCESS_EXEC_REQ.getValue());
        try {
            lotService.sxHoldLotReq(postObjCommon, lotID, Collections.singletonList(lotHoldReq));
        } catch (ServiceException e) {
            if (Validations.isEquals(retCodeConfig.getInvalidLotStat(), e.getCode())
                    || Validations.isEquals(retCodeConfig.getExistSameHold(), e.getCode())) {
                if (log.isDebugEnabled()) {
                    log.debug("{}{}", "Post Process Lot Hold: ", e.getMessage());
                }
            } else {
                throw e;
            }
        }
        return lotHoldReq;
    }

    @Override
    public void sxReleaseLockHoldOnLot(Infos.ObjCommon objCommon, ObjectIdentifier lotID, Infos.LotHoldReq lotHoldReq) {
        //-----------------------------
        // For PostProcess Hold
        //-----------------------------
        Infos.ObjCommon postObjCommon = objCommon.duplicate();
        postObjCommon.setTransactionID(TransactionIDEnum.POST_PROCESS_EXEC_REQ.getValue());

        Infos.LotHoldReq holdRecord = Optional.ofNullable(lotHoldReq)
                .orElseGet(() -> {
                    List<Infos.LotHoldRecordInfo> lotHoldRecords = lotMethod.lotHoldRecordGetDR(ObjectIdentifier.fetchValue(lotID));
                    return lotHoldRecords.stream()
                            .filter(record -> ObjectIdentifier.fetchValue(record.getHoldReasonID())
                                    .equals(BizConstant.SP_REASON_LOTLOCK))
                            .findFirst().map(info -> {
                                Infos.LotHoldReq tmpRecord = new Infos.LotHoldReq();
                                tmpRecord.setRelatedLotID(info.getRelatedLotID());
                                tmpRecord.setHoldUserID(info.getHoldUserID());
                                tmpRecord.setHoldReasonCodeID(info.getHoldReasonID());
                                tmpRecord.setHoldType(info.getHoldType());
                                tmpRecord.setResponsibleOperationMark(info.getResponsibleOperationMark());
                                return tmpRecord;
                            }).orElse(null);
                });
        Optional.ofNullable(holdRecord)
                .ifPresent(record -> {
                    Params.HoldLotReleaseReqParams holdParam = new Params.HoldLotReleaseReqParams();
                    holdParam.setReleaseReasonCodeID(ObjectIdentifier.buildWithValue(BizConstant.SP_REASON_LOTLOCKRELEASE));
                    holdParam.setLotID(lotID);
                    holdParam.setHoldReqList(Collections.singletonList(record));
                    try {
                        List<Infos.LotHoldRecordInfo> lotHolds = lotMethod.lotHoldRecordGetDR(ObjectIdentifier.fetchValue(lotID));
                        if (CimArrayUtils.isNotEmpty(lotHolds) && lotHolds.stream()
                                .map(hold -> ObjectIdentifier.fetchValue(hold.getHoldReasonID()))
                                .anyMatch(BizConstant.SP_REASON_LOTLOCK::equals)) {
                            lotService.sxHoldLotReleaseReq(postObjCommon, holdParam);
                        }
                    } catch (CimException e) {
                        int code = Optional.ofNullable(e.getCode()).orElse(-777);
                        if (retCodeConfig.getNotExistHold().getCode() == code
                                || retCodeConfig.getLotNotHeld().getCode() == code) {
                            if (log.isDebugEnabled()) {
                                log.debug("{}{}", "Post Process Lot Hold Release: ", e.getMessage());
                            }
                        } else {
                            throw e;
                        }
                    }
                });
    }

    @Override
    public void sxLockHoldOnDurable(Infos.ObjCommon objCommon, ObjectIdentifier durableID) {

    }

    @Override
    public void sxReleaseLockHoldOnDurable(Infos.ObjCommon objCommon, ObjectIdentifier durableID) {

    }
}
