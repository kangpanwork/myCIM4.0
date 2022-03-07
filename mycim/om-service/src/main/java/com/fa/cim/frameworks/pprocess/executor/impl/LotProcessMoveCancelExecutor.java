package com.fa.cim.frameworks.pprocess.executor.impl;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.pp.EntityType;
import com.fa.cim.frameworks.dto.pp.PostProcessTask;
import com.fa.cim.frameworks.dto.pp.mode.AvailablePhase;
import com.fa.cim.frameworks.pprocess.api.annotations.PostProcessTaskHandler;
import com.fa.cim.frameworks.pprocess.api.definition.PostProcessExecutor;
import com.fa.cim.method.ILotMethod;
import com.fa.cim.newcore.bo.product.CimLot;
import com.fa.cim.newcore.dto.product.DispatchReadinessState;
import com.fa.cim.newcore.lock.manager.ObjectLockManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@PostProcessTaskHandler(available = AvailablePhase.CHAINED_OR_JOINED)
public class LotProcessMoveCancelExecutor implements PostProcessExecutor {

    private final ILotMethod lotMethod;
    private final ObjectLockManager objectLockManager;

    @Autowired
    public LotProcessMoveCancelExecutor(ILotMethod lotMethod, ObjectLockManager objectLockManager) {
        this.lotMethod = lotMethod;
        this.objectLockManager = objectLockManager;
    }

    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public PostProcessTask.Result doExecute(PostProcessTask.Param param) {

        // step 1 - check if the target type is lot, do nothing if it's not
        if (log.isDebugEnabled()) {
            log.debug("Step 1 - check the working type");
        }
        EntityType entityType = param.getEntityType();
        if(log.isTraceEnabled()) {
            log.trace("::EntityType == {}", entityType.name());
        }
        if (entityType != EntityType.Lot) {
            if (log.isWarnEnabled()) {
                log.warn("LotProcessMoveCancelExecutor[step 1] >>> Entity Type is not Lot, do nothing");
            }
            return PostProcessTask.success();
        }

        // step 2 - check if the lot id is empty, do nothing if it is
        if (log.isDebugEnabled()) {
            log.debug("Step 2 - check if the lot ID is empty");
        }
        ObjectIdentifier lotID = param.getEntityID();
        if (log.isTraceEnabled()) {
            log.trace("::LotID == {}", lotID);
        }
        if (ObjectIdentifier.isEmpty(lotID)) {
            if (log.isWarnEnabled()) {
                log.warn("LotProcessMoveCancelExecutor[step 2] >>> LotID is empty, do nothing");
            }
            return PostProcessTask.success();
        }

        // step 3 - lock the lot for update
        if (log.isDebugEnabled()) {
            log.debug("Step 3 - lock the lot for update");
        }
        objectLockManager.lock(CimLot.class, lotID);


        Infos.ObjCommon objCommon = param.getObjCommon();
        // step 4 - get the lot's pending move next
        boolean moveNextRequired = lotMethod.checkLotMoveNextRequired(objCommon, lotID);
        if (log.isTraceEnabled()) {
            log.trace("Lot<{}> pendingMoveNext == {}", lotID, moveNextRequired);
        }

        // step 5 - update the lot's move next flag to false to cancel the pending move to next status
        if (log.isDebugEnabled()) {
            log.debug("Step 4 - update lot's PENDING_MOVE_NEXT_FLAG to false");
        }
        if (moveNextRequired) {
            lotMethod.setLotMoveNextRequired(objCommon, lotID, false);
        }

        // return success
        return PostProcessTask.success();
    }
}
