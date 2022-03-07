package com.fa.cim.service.lot.impl;

import com.fa.cim.annotaion.OmService;
import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.utils.Validations;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Outputs;
import com.fa.cim.method.ILotMethod;
import com.fa.cim.method.IObjectLockMethod;
import com.fa.cim.method.IProcessMethod;
import com.fa.cim.newcore.bo.product.CimLot;
import com.fa.cim.service.lot.ILotProcessOperationService;
import com.fa.cim.service.lot.ILotService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;

@Slf4j
@OmService
public class LotProcessOperationServiceImpl implements ILotProcessOperationService {

    @Autowired
    private IProcessMethod processMethod;

    @Autowired
    private IObjectLockMethod objectLockMethod;

    @Autowired
    private ILotService lotService;

    @Autowired
    private ILotMethod lotMethod;

    @Autowired
    private RetCodeConfig retCodeConfig;

    @Override
    public void sxProcessMove(Infos.ObjCommon objCommon, ObjectIdentifier lotID) {
        // step 1 - check if the lot ID is empty, throw ServiceException if so
        if(log.isDebugEnabled()) {
            log.debug("step 1 - check if the lot ID is empty");
        }
        Validations.check(ObjectIdentifier.isEmpty(lotID), retCodeConfig.getInvalidInputParam());

        // step 2 - lock the lot for process move
        if (log.isDebugEnabled()) {
            log.debug("step 2 - obtain the lock on the lot");
        }
        objectLockMethod.objectLock(objCommon, CimLot.class, lotID);

        // step 3 - perform process move
        if(log.isDebugEnabled()) {
            log.debug("step 3 - perform process move");
        }
        try {
            processMethod.processMove(objCommon, lotID);
            if (log.isDebugEnabled()) {
                log.debug("step 3.1 - process move successfully");
            }

            // step 4 - set the PendingMoveNext flag to false
            lotMethod.setLotMoveNextRequired(objCommon, lotID, false);
            if (log.isDebugEnabled()) {
                log.debug("step 4 - set the PendingMoveNext flag to false");
            }
        } catch (ServiceException ex) {
            // check if the reason for failing the process move is due to failing to add the lot at the next eqp's queue
            // if that's the case, hold the lot instead, rather not failed the whole transaction
            if (Validations.isEquals(ex.getCode(), retCodeConfig.getAddToQueueFail())) {
                if (log.isDebugEnabled()) {
                    log.debug("step 3.2 - get lot current operation info");
                }
                Outputs.ObjLotCurrentOperationInfoGetDROut currentOpeInfo = lotMethod.lotCurrentOperationInfoGetDR(objCommon, lotID);

                if (log.isDebugEnabled()) {
                    log.debug("step 3.2 - hold lot");
                }
                Infos.LotHoldReq lotHoldReq = new Infos.LotHoldReq();
                lotHoldReq.setHoldType(BizConstant.SP_HOLDTYPE_ADDTOQUEUEERRHOLD);
                lotHoldReq.setHoldReasonCodeID(new ObjectIdentifier(BizConstant.SP_REASON_ADDTOQUEUEERRHOLD));
                lotHoldReq.setHoldUserID(objCommon.getUser().getUserID());
                lotHoldReq.setResponsibleOperationMark(BizConstant.SP_RESPONSIBLEOPERATION_CURRENT);
                lotHoldReq.setRouteID(currentOpeInfo.getMainPDID());
                lotHoldReq.setOperationNumber(currentOpeInfo.getOpeNo());
                lotHoldReq.setClaimMemo("");
                lotService.sxHoldLotReq(objCommon, lotID, Collections.singletonList(lotHoldReq));
            } else {
                throw ex;
            }
        }
    }
}
