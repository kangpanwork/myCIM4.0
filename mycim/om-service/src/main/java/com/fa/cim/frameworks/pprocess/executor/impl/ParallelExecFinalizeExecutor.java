package com.fa.cim.frameworks.pprocess.executor.impl;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.dto.Infos;
import com.fa.cim.frameworks.dto.pp.PostProcessTask;
import com.fa.cim.frameworks.dto.pp.mode.AvailablePhase;
import com.fa.cim.frameworks.pprocess.api.annotations.PostProcessTaskHandler;
import com.fa.cim.frameworks.pprocess.api.definition.PostProcessExecutor;
import com.fa.cim.method.ICassetteMethod;
import com.fa.cim.method.IObjectLockMethod;
import com.fa.cim.newcore.bo.durable.CimCassette;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>ParallelExecFinalizeExecutor .
 * change history:
 * date               defect            person            comments
 * -------------------------------------------------------------------------------------------------------------------
 * 2021/6/10 15:01    ********          ZQI               create file.
 *
 * @author ZQI
 * @version 1.0
 * @date 2021/6/10 15:01
 * @copyright 2021, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 * @since JDK1.8
 */
@Slf4j
@PostProcessTaskHandler
public class ParallelExecFinalizeExecutor implements PostProcessExecutor {

    private final IObjectLockMethod objectLockMethod;
    private final ICassetteMethod cassetteMethod;

    @Autowired
    public ParallelExecFinalizeExecutor(IObjectLockMethod objectLockMethod, ICassetteMethod cassetteMethod) {
        this.objectLockMethod = objectLockMethod;
        this.cassetteMethod = cassetteMethod;
    }

    /**
     * Parallel execution finalize
     *
     * @param param the necessary params for task handling
     * @return post process result
     */
    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public PostProcessTask.Result doExecute(PostProcessTask.Param param) {
        if (null == param) return PostProcessTask.error(this);
        final Infos.ObjCommon objCommon = param.getObjCommon();
        final ObjectIdentifier cassetteID = param.getEntityID();

        // object_lock
        objectLockMethod.objectLock(objCommon, CimCassette.class, cassetteID);

        //-------------------------------------------
        // Finalize action for cassette
        //-------------------------------------------
        cassetteMethod.cassetteStatusFinalizeForPostProcess(objCommon, cassetteID);
        return PostProcessTask.success();
    }
}
