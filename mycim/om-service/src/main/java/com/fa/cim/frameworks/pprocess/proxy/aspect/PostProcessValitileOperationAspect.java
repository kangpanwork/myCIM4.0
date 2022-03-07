package com.fa.cim.frameworks.pprocess.proxy.aspect;

import com.fa.cim.common.exception.CimException;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.frameworks.common.TaskContextHolder;
import com.fa.cim.frameworks.pprocess.manager.PostProcessTraceManager;
import com.fa.cim.newcore.impl.bo.env.StandardProperties;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * aspect for {@link com.fa.cim.frameworks.pprocess.api.annotations.ValitileOperation}
 *
 * @author Yuri
 */
@Slf4j
@Aspect
@Component
public class PostProcessValitileOperationAspect {

    private final PostProcessTraceManager traceManager;

    @Autowired
    public PostProcessValitileOperationAspect(PostProcessTraceManager traceManager) {
        this.traceManager = traceManager;
    }

    @Pointcut("@annotation(com.fa.cim.frameworks.pprocess.api.annotations.ValitileOperation)")
    public void isValitielOperation() {
    }

    /**
     * count up the operation changes
     *
     */
    @Before("isValitielOperation()")
    @Order(10)
    public void countingOperationChange() {
        Optional.ofNullable(TaskContextHolder.getTaskId())
                .ifPresent(traceManager::increateChangedCountFor);
    }

    /**
     * check if the operation change count is okay to proceed during the post process
     *
     * @param joinPoint joinPoint
     * @return the proceed result
     */
    @Around("isValitielOperation()")
    @Order(20)
    public Object checkOperationCount(ProceedingJoinPoint joinPoint) {
        Optional<String> taskIdOpt = Optional.ofNullable(TaskContextHolder.getTaskId());
        try {
            if (taskIdOpt.isPresent()) {
                String taskId = taskIdOpt.get();
                int curCount = traceManager.currentChangedCountFor(taskId);
                int maxCount = StandardProperties.OM_PP_MAX_OPE_COUNT.getIntValue();
                if (maxCount < curCount) {
                    throw new ServiceException(String.format("max operation change count exceeded: current<%d>, max<%d>",
                            curCount, maxCount));
                }  else {
                    if (log.isTraceEnabled()) {
                        log.trace(">>>> current<{}>, max<{}>", curCount, maxCount);
                    }
                    return joinPoint.proceed();
                }
            } else {
                return joinPoint.proceed();
            }
        } catch (CimException e) {
            throw e;
        } catch (Throwable t) {
            throw new ServiceException(t.getMessage());
        }
    }
}
