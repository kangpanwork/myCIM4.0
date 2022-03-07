package com.fa.cim.frameworks.pprocess.executor.impl;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.pp.EntityType;
import com.fa.cim.frameworks.dto.pp.PostProcessTask;
import com.fa.cim.frameworks.dto.pp.mode.AvailablePhase;
import com.fa.cim.frameworks.pprocess.api.annotations.PostProcessTaskHandler;
import com.fa.cim.frameworks.pprocess.api.definition.PostProcessExecutor;
import com.fa.cim.method.IOcapMethod;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * description: OCAP PostProcess Executor after SPC check end if existed ocapInfo about relation lot
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2021/7/8                              Neyo                create file
 *
 * @author: Neyo
 * @date: 2021/7/8 10:24
 * @copyright: 2021, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@PostProcessTaskHandler
public class OcapHoldAfterSPCExecutor implements PostProcessExecutor {

    private final IOcapMethod ocapMethod;

    @Autowired
    public OcapHoldAfterSPCExecutor(IOcapMethod ocapMethod) {
        this.ocapMethod = ocapMethod;
    }

    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public PostProcessTask.Result doExecute(PostProcessTask.Param param) {
        // step1 - check if the Entity Type is target to Lot, if not do nothing
        if (log.isDebugEnabled()) {
            log.debug("Step 1 - check the entity type ...");
        }
        EntityType entityType = param.getEntityType();
        if (entityType != EntityType.Lot) {
            if (log.isWarnEnabled()) {
                log.warn("Try to perform ocapHold is non-lot entity[{}], ignore the executing", entityType.name());
            }
            return PostProcessTask.success();
        }

        Infos.ObjCommon objCommon = param.getObjCommon();
        ObjectIdentifier lotID = param.getEntityID();

        //Step2 - excute ocap hold action
        if (log.isDebugEnabled()){
            log.debug("Step2 - excute ocap hold action");
        }
        ocapMethod.ocapHoldActionAfterSPCCheckByPostTaskReq(objCommon,lotID);
        // return success
        return PostProcessTask.success();
    }
}
