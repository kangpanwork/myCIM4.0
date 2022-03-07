package com.fa.cim.frameworks.pprocess.executor.impl;

import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.dto.Infos;
import com.fa.cim.frameworks.dto.pp.PostProcessTask;
import com.fa.cim.frameworks.dto.pp.mode.AvailablePhase;
import com.fa.cim.frameworks.pprocess.api.annotations.PostProcessTaskHandler;
import com.fa.cim.frameworks.pprocess.api.definition.PostProcessExecutor;
import com.fa.cim.method.IProcessMethod;
import com.fa.cim.newcore.impl.bo.env.StandardProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>OperationDelQueuePutExecutor .
 * change history:
 * date               defect            person            comments
 * -------------------------------------------------------------------------------------------------------------------
 * 2021/6/10 14:03    ********          ZQI               create file.
 *
 * @author ZQI
 * @version 1.0
 * @date 2021/6/10 14:03
 * @copyright 2021, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 * @since JDK1.8
 */
@Slf4j
@PostProcessTaskHandler(isNextOperationRequired = true)
public class OperationDelQueuePutExecutor implements PostProcessExecutor {

    private final IProcessMethod processMethod;

    @Autowired
    public OperationDelQueuePutExecutor(IProcessMethod processMethod) {
        this.processMethod = processMethod;
    }

    /**
     * Process operation deletion queue put action
     *
     * @param param the necessary params for task handling
     * @return post process result
     */
    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public PostProcessTask.Result doExecute(PostProcessTask.Param param) {
        if (null == param) return PostProcessTask.error(this);
        final Infos.ObjCommon objCommon = param.getObjCommon();
        final ObjectIdentifier lotID = param.getEntityID();

        int envEventCreateType = StandardProperties.OM_MAINT_PO_EVENT_CREATE_TYPE.getIntValue();
        if (envEventCreateType == BizConstant.SP_POMAINTEVENTCREATETYPE_ACTIVELOTENABLED
                || envEventCreateType == BizConstant.SP_POMAINTEVENTCREATETYPE_ENABLED) {
            //--------------------------------------------------------
            //   Put Event Queue for PO Maintenance
            //--------------------------------------------------------
            if (log.isDebugEnabled()) log.debug("call poDelQueuePutDR()...");
            processMethod.poDelQueuePutDR(objCommon, lotID);
        }
        return PostProcessTask.success();
    }
}
