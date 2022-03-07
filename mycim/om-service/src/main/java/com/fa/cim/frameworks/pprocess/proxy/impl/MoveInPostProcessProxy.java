package com.fa.cim.frameworks.pprocess.proxy.impl;

import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.utils.CimStringUtils;
import com.fa.cim.common.utils.ThreadContextHolder;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.frameworks.dto.pp.PostProcessTask;
import com.fa.cim.frameworks.pprocess.api.definition.PostProcessExecutor;
import com.fa.cim.frameworks.pprocess.executor.TaskExecutors;
import com.fa.cim.frameworks.pprocess.manager.PostProcessPlanManager;
import com.fa.cim.frameworks.pprocess.manager.PostProcessTraceManager;
import com.fa.cim.frameworks.pprocess.manager.PostProcessTransactionManager;
import com.fa.cim.method.IPostProcesssTaskMethod;
import com.fa.cim.newcore.impl.bo.env.StandardProperties;
import com.fa.cim.newcore.impl.factory.GenericCoreFactory;
import com.fa.cim.service.pprocess.PostProcessHoldService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * <p>MoveInPostProcessProxy .
 * change history:
 * date               defect            person            comments
 * -------------------------------------------------------------------------------------------------------------------
 * 2021/6/15 10:45    ********          ZQI               create file.
 *
 * @author ZQI
 * @version 1.0
 * @date 2021/6/15 10:45
 * @copyright 2021, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 * @since JDK1.8
 */
@Component
@Slf4j
public class MoveInPostProcessProxy extends DefaultPostProcessProxy {

    @Autowired
    public MoveInPostProcessProxy(PostProcessPlanManager planManager) {
        super(planManager);
    }

    @Override
    public List<PostProcessTask> modify(List<PostProcessTask> tasks) {
        final int updateForEqpAttrIntValue = StandardProperties.OM_PP_UPDATE_FOR_EQP_ATTR.getIntValue();
        if (log.isDebugEnabled()) {
            log.debug("runWaferUpdateByPostProc = {}", updateForEqpAttrIntValue);
        }
        if (updateForEqpAttrIntValue == 1) {
            tasks.forEach(postProcessTask -> {
                final PostProcessExecutor executor = postProcessTask.getDefinition().getExecutor();
                //---------------------------------------
                // if executor is RunWaferInfoUpdate
                //---------------------------------------
                if (TaskExecutors.RunWaferInfoUpdate.getExecutorType().equals(executor.getClass())) {
                    if (log.isDebugEnabled())
                        log.debug("Executor: {}", TaskExecutors.RunWaferInfoUpdate.getExecutorType());
                    // get thread data
                    final String strRunWaferCnt = ThreadContextHolder.getThreadSpecificDataString(
                            BizConstant.SP_THREADSPECIFICDATA_KEY_RUNWAFERCNT);
                    // add RunWaferCnt detail info
                    postProcessTask.getDetails().add(new PostProcessTask.Detail(
                            BizConstant.SP_THREADSPECIFICDATA_KEY_RUNWAFERCNT,
                            CimStringUtils.isNotEmpty(strRunWaferCnt) ? strRunWaferCnt : "0"));

                    // add OpeStartCnt detail info
                    postProcessTask.getDetails().add(new PostProcessTask.Detail(
                            BizConstant.SP_THREADSPECIFICDATA_KEY_OPESTARTCNT,
                            "1"));
                }
            });
        }
        return tasks;
    }
}
