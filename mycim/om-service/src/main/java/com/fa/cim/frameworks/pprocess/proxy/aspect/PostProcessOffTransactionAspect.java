package com.fa.cim.frameworks.pprocess.proxy.aspect;

import com.fa.cim.common.utils.CimArrayUtils;
import com.fa.cim.frameworks.common.TaskContextHolder;
import com.fa.cim.frameworks.dto.pp.PostProcessContext;
import com.fa.cim.frameworks.dto.pp.mode.ExecutePhase;
import com.fa.cim.frameworks.pprocess.PostProcessEvent;
import com.fa.cim.frameworks.pprocess.manager.PostProcessTraceManager;
import com.fa.cim.service.pprocess.PostProcessTaskService;
import com.fa.cim.newcore.impl.bo.env.StandardProperties;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * the aspect responsible for executing the off transactional tasks
 *
 * @author Yuri
 */
@Slf4j
@Aspect
@Order(10)
@Component
public class PostProcessOffTransactionAspect {

    private final ApplicationEventPublisher eventPublisher;
    private final PostProcessTraceManager traceManager;

    @Autowired
    public PostProcessOffTransactionAspect(ApplicationEventPublisher eventPublisher, PostProcessTraceManager traceManager) {
        this.eventPublisher = eventPublisher;
        this.traceManager = traceManager;
    }

    @Pointcut("@annotation(com.fa.cim.frameworks.pprocess.api.annotations.EnablePostProcess)")
    public void isPostProcessEnabled() {}

    /**
     * execute tasks cached locally
     * with @Order(10), the method is invocated outside of the main transaction
     */
    @After("isPostProcessEnabled()")
    public void executeLocalTasks() {
        String taskId = TaskContextHolder.getTaskId();
        TaskContextHolder.clear();
        Optional.ofNullable(taskId)
                .map(traceManager::findPostProcessContextForTaskId)
                .map(PostProcessContext::getRegister)
                .ifPresent(register -> {
                    // check if the post process executing is executed asynchronously or not
                    boolean asyncFlag = StandardProperties.OM_PP_ASYNC_FLAG.isTrue();

                    if (log.isTraceEnabled()) {
                        log.trace("OM_POST_PROCESS_ASYNC_FLAG == {}", asyncFlag);
                    }
                    if (asyncFlag) {
                        if (log.isTraceEnabled()) {
                            log.trace("clear local caching on current locals ... ");
                        }

                    }
                    PostProcessEvent postEvent = asyncFlag ?
                            PostProcessEvent.asyncEvent(this, ExecutePhase.JOINED) :
                            PostProcessEvent.syncEvent(this, ExecutePhase.JOINED);
                    postEvent.setTaskId(register.getTaskId());
                    postEvent.setTasks(register.getTasks());
                    postEvent.setObjCommon(register.getObjCommon());
                    postEvent.setTaskId(register.getTaskId());

                    // publish the executing event
                    eventPublisher.publishEvent(postEvent);
                });
    }

}
