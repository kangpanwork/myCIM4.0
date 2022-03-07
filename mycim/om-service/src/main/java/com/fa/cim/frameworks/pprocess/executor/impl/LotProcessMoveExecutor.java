package com.fa.cim.frameworks.pprocess.executor.impl;

import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.utils.CimArrayUtils;
import com.fa.cim.common.utils.CimStringUtils;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.pp.EntityType;
import com.fa.cim.frameworks.dto.pp.PostProcessTask;
import com.fa.cim.frameworks.dto.pp.mode.AvailablePhase;
import com.fa.cim.frameworks.pprocess.api.annotations.PostProcessTaskHandler;
import com.fa.cim.frameworks.pprocess.api.definition.PostProcessExecutor;
import com.fa.cim.method.ILotMethod;
import com.fa.cim.service.lot.ILotProcessOperationService;
import com.fa.cim.service.processcontrol.IProcessControlService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Slf4j
@PostProcessTaskHandler(available = AvailablePhase.CHAINED_OR_JOINED)
public class LotProcessMoveExecutor implements PostProcessExecutor {

    private final ILotMethod lotMethod;
    private final ILotProcessOperationService lotProcessOperationService;

    @Autowired
    private IProcessControlService processControlService;


    @Autowired
    public LotProcessMoveExecutor(ILotMethod lotMethod, ILotProcessOperationService lotProcessOperationService) {
        this.lotMethod = lotMethod;
        this.lotProcessOperationService = lotProcessOperationService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public PostProcessTask.Result doExecute(PostProcessTask.Param param) {
        // step 1 - check if the Entity Type is target to Lot, if not do nothing
        if (log.isDebugEnabled()) {
            log.debug("Step 1 - check the entity type ...");
        }
        EntityType entityType = param.getEntityType();
        if (entityType != EntityType.Lot) {
            if (log.isWarnEnabled()) {
                log.warn("Try to perform lot process move on non-lot entity[{}], ignore the executing", entityType.name());
            }
            return PostProcessTask.success();
        }

        Infos.ObjCommon objCommon = param.getObjCommon();
        ObjectIdentifier lotID = param.getEntityID();

        // step 2 - check the conditions for lot proceed to next step base according to different transaction
        if (log.isDebugEnabled()) {
            log.debug("Step 2 - check if the lot is on hold if the transaction is normal moveOutReq");
        }
        String transactionID = objCommon.getTransactionID();
        boolean isMoveOutReq = CimStringUtils.equalsIn(transactionID,
                TransactionIDEnum.OPERATION_COMP_WITH_DATA_REQ.getValue(),              // normal move out
                TransactionIDEnum.OPERATION_COMP_FOR_INTERNAL_BUFFER_REQ.getValue(),    // move out for Internal buffer
                TransactionIDEnum.FORCE__OPE_COMP_REQ.getValue(),                       // force move out
                TransactionIDEnum.FORCE_OPE_COMP_FOR_INTERNAL_BUFFER_REQ.getValue(),    // force move out for IB
                TransactionIDEnum.PARTIAL_OPERATION_COMP_WITH_DATA_REQ.getValue(),      // partial move out
                TransactionIDEnum.PARTIAL_OPERATION_COMP_FOR_INTERNAL_BUFFER_REQ.getValue()); // partial move out for IB
        if (log.isInfoEnabled()) {
            log.info("::isMoveOutReq == {}， transaction ID == {}", isMoveOutReq, transactionID);
        }
        // for normal move out req (moveOutReq / moveOutForIBReq), do not proceed to next step when the lot is ONHOLD
        if (isMoveOutReq) {
            String lotHoldState = lotMethod.lotHoldStateGet(objCommon, lotID);
            if (log.isDebugEnabled()) {
                log.debug("Lot<{}>::holdState == {}", lotID, lotHoldState);
            }
            if (CimStringUtils.equals(lotHoldState, BizConstant.CIMFW_LOT_HOLDSTATE_ONHOLD)) {
                if (log.isDebugEnabled()) {
                    log.debug("Lot <{}> is ONHOLD, do not proceed to next operation", lotID);
                }
                return PostProcessTask.success();
            }
        }
        // for lot hold release, check if the lot is still ONHOLD with SpecOverHold, do not proceed to next step if it is
        else if (CimStringUtils.equals(transactionID, TransactionIDEnum.HOLD_LOT_RELEASE_REQ.getValue())) {
            if (log.isDebugEnabled()) {
                log.debug("isHoldLotReleaseReq == true");
            }
            if (log.isDebugEnabled()) {
                log.debug("check the if there is SpecOverHold");
            }
            List<Infos.LotHoldRecordInfo> lotHoldRecordInfos = lotMethod.lotHoldRecordGetDR(ObjectIdentifier.fetchValue(lotID));
            if (CimArrayUtils.isNotEmpty(lotHoldRecordInfos)) {
                if (log.isDebugEnabled()) {
                    log.debug("The lot<{}> is still with ONHOLD status, do not proceed to next step", lotID);
                }
                return PostProcessTask.success();
            }
        }

        // step 3 - call sxProcessMove
        if (log.isDebugEnabled()) {
            log.debug("Step 3 - call sxProcessMove");
        }
        boolean isMoveNextRequired = lotMethod.checkLotMoveNextRequired(objCommon, lotID);
        if (log.isTraceEnabled()) {
            log.trace("::isMoveNextRequired == {}", isMoveNextRequired);
        }
        if (!isMoveNextRequired) {
            if (log.isDebugEnabled()) {
                log.debug("Lot<{}> is not move to next required, end the task executing", lotID);
            }
            return PostProcessTask.success();
        }
        if (log.isInfoEnabled()) {
            log.info("Transaction ID = {}, Lot ID = {}, apply the LotProcessMove executor.",
                    transactionID,
                    ObjectIdentifier.fetchValue(lotID));
        }
        lotProcessOperationService.sxProcessMove(objCommon, lotID);

        // return success
        return PostProcessTask.success();
    }
}
