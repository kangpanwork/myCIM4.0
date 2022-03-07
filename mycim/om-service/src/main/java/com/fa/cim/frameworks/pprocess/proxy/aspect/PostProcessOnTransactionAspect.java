package com.fa.cim.frameworks.pprocess.proxy.aspect;

import com.fa.cim.common.exception.CimException;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.OmCode;
import com.fa.cim.common.support.Response;
import com.fa.cim.common.utils.CimArrayUtils;
import com.fa.cim.common.utils.SpringContextUtil;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.pp.EntityType;
import com.fa.cim.frameworks.common.TaskContextHolder;
import com.fa.cim.frameworks.dto.pp.*;
import com.fa.cim.frameworks.dto.pp.mode.ExecutePhase;
import com.fa.cim.frameworks.dto.pp.mode.JoinMode;
import com.fa.cim.frameworks.pprocess.PostProcessEvent;
import com.fa.cim.frameworks.pprocess.api.annotations.EnablePostProcess;
import com.fa.cim.frameworks.pprocess.api.definition.PostProcessPlanProxy;
import com.fa.cim.frameworks.pprocess.manager.PostProcessTraceManager;
import com.fa.cim.frameworks.pprocess.proxy.impl.DefaultPostProcessProxy;
import com.fa.cim.method.ILotMethod;
import com.fa.cim.method.IPostProcesssTaskMethod;
import com.fa.cim.newcore.dto.product.DispatchReadinessState;
import com.fa.cim.newcore.impl.factory.GenericCorePool;
import com.fa.cim.newcore.lock.executor.ObjectLockExecutor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.stream.Collectors;

/**
 * the after aspect for {@link EnablePostProcess} which is within the same transaction of the main process
 *
 * @author Yuri
 */
@Slf4j
@Aspect
@Component
public class PostProcessOnTransactionAspect {

    private final ApplicationEventPublisher eventPublisher;
    private final PostProcessTraceManager traceManager;
    private final IPostProcesssTaskMethod taskMethod;
    private final ObjectLockExecutor objectLockExecutor;
    private final ILotMethod lotMethod;
    private final RetCodeConfig retCodeConfig;
    private final DefaultPostProcessProxy defaultProxy;

    @Autowired
    public PostProcessOnTransactionAspect(ApplicationEventPublisher eventPublisher,
                                          PostProcessTraceManager traceManager,
                                          IPostProcesssTaskMethod taskMethod,
                                          ObjectLockExecutor objectLockExecutor,
                                          ILotMethod lotMethod,
                                          RetCodeConfig retCodeConfig,
                                          @Qualifier("defaultPostProcessProxy") DefaultPostProcessProxy defaultProxy) {
        this.eventPublisher = eventPublisher;
        this.traceManager = traceManager;
        this.taskMethod = taskMethod;
        this.objectLockExecutor = objectLockExecutor;
        this.lotMethod = lotMethod;
        this.retCodeConfig = retCodeConfig;
        this.defaultProxy = defaultProxy;
    }

    @Pointcut("@annotation(com.fa.cim.frameworks.pprocess.api.annotations.EnablePostProcess)")
    public void isPostProcessEnabled() {}

    @SuppressWarnings({"rawtypes", "unchecked"})
    private PostProcessParam.Register registerParams(Infos.ObjCommon objCommon, JoinPoint joinPoint, Object out) {
        // find proxy
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Class<? extends PostProcessPlanProxy> proxyType = signature.getMethod().getAnnotation(EnablePostProcess.class).proxy();
        PostProcessPlanProxy planProxy = proxyType == PostProcessPlanProxy.class ?
                defaultProxy : SpringContextUtil.getSingletonBean(proxyType);
        PostProcessParam.PlanTask planTask = new PostProcessParam.PlanTask(objCommon, joinPoint.getArgs()[0], out);
        if (log.isDebugEnabled()) {
            log.debug(">>>>>> Plan the tasks");
        }
        PostProcessTaskPlan.generateTaskId(ObjectIdentifier.fetchValue(objCommon.getUser().getUserID()));
        List<PostProcessTask> plannedTask = planProxy.plan(planTask);
        if (log.isDebugEnabled()) {
            log.debug(">>>>>> Modify the tasks");
        }
        List<PostProcessTask> task = planProxy.modify(plannedTask);
        PostProcessParam.Register retVal = new PostProcessParam.Register();
        retVal.setPostTasks(task);
        retVal.setTaskId(task.stream()
                .findFirst()
                .map(PostProcessTask::getTaskId)
                .orElse(""));
        PostProcessTaskPlan.clearTaskId();
        PostProcessContext context = traceManager.openTraceFor(objCommon, retVal.getTaskId());
        context.setMainGrgument(joinPoint.getArgs()[0]);
        context.setMainResult(out);
        return retVal;
    }

    /**
     * Upon a successful returning register the post process task with an output from the main process and the success mark
     *
     * @param joinPoint joinPoint
     * @param result process result
     */
    @AfterReturning(value = "isPostProcessEnabled()", returning = "result")
    public void registerTaskOnSuccess(JoinPoint joinPoint, Object result) {
        if (log.isTraceEnabled()) {
            log.trace(">>>>> Extract result");
        }
        Object out;
        if (result instanceof Response) {
            out = ((Response) result).getBody();
        } else {
            out = result;
        }

        Infos.ObjCommon objCommon = TaskContextHolder.getObjCommon();
        PostProcessParam.Register register = registerParams(objCommon, joinPoint, out);
        try {
            if (log.isDebugEnabled()) {
                log.debug(">>>>>> Check if any lots are already in a post process executing");
            }
            List<ObjectIdentifier> errorLots = register.getPostTasks().stream()
                    .filter(task -> task.getDefinition().getEntityType() == EntityType.Lot)
                    .map(PostProcessTask::getEntityID)
                    .distinct()
                    .filter(lotID -> lotMethod.lotDispatchReadinessGet(objCommon, lotID) != DispatchReadinessState.Ready)
                    .collect(Collectors.toList());
            boolean notEmpty = CimArrayUtils.isNotEmpty(errorLots);
            if (log.isTraceEnabled()) {
                log.trace("notValidateRequest == {}", notEmpty);
            }
            if (CimArrayUtils.isNotEmpty(errorLots)) {
                StringJoiner lotIDs = new StringJoiner(",");
                errorLots.forEach(lotID -> lotIDs.add(ObjectIdentifier.fetchValue(lotID)));
                OmCode errorCode = new OmCode(retCodeConfig.getLotInPostProcess(), lotIDs.toString());
                throw new ServiceException(errorCode, objCommon.getTransactionID());
            }
            if (log.isDebugEnabled()) {
                log.debug(">>>>>> Execute chained tasks");
            }
            PostProcessEvent chainedEvent = PostProcessEvent.syncEvent(this, ExecutePhase.CHAINED);
            chainedEvent.setTaskId(register.getTaskId());
            chainedEvent.setObjCommon(objCommon);
            chainedEvent.setTasks(register.getPostTasks());
            eventPublisher.publishEvent(chainedEvent);
            if (log.isDebugEnabled()) {
                log.debug(">>>>>> chained tasks executing successfully");
            }
            register.setMainSuccess(true);
            this.doCreateTask(objCommon, register);
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug(">>>>>> chained tasks executing with errors");
            }
            throw handleException(objCommon, e);
        } finally {
            GenericCorePool.clear();
            objectLockExecutor.forceUnlockAll();
        }

    }

    /**
     * Upon an unsuccessful returning of the main process, register only the finally post process tasks
     *
     * @param joinPoint joinPoint
     */
//    @AfterThrowing("isPostProcessEnabled()")
    public void registerTaskOnError(JoinPoint joinPoint) {
        Infos.ObjCommon objCommon = TaskContextHolder.getObjCommon();
        if(!Optional.ofNullable(objCommon).isPresent()) {
            return;
        }
        PostProcessParam.Register register = registerParams(objCommon, joinPoint, null);

        try {

            PostProcessEvent chainedEvent = PostProcessEvent.syncEvent(this, ExecutePhase.CHAINED);
            chainedEvent.setTaskId(register.getTaskId());
            chainedEvent.setObjCommon(objCommon);
            chainedEvent.setTasks(Collections.emptyList());
            eventPublisher.publishEvent(chainedEvent);

        } finally {
            GenericCorePool.clear();
            objectLockExecutor.forceUnlockAll();
            doCreateTask(objCommon, register);
        }


    }

    private void doCreateTask(Infos.ObjCommon objCommon, PostProcessParam.Register register) {
        List<PostProcessTask> postTasks = register.getPostTasks().stream()
                .filter(task -> task.getPhase() != ExecutePhase.CHAINED
                        && (register.isMainSuccess() || task.getDefinition().getJoinMode() == JoinMode.FINALLY))
                .collect(Collectors.toList());
        taskMethod.createTaskRecords(objCommon, postTasks);
        PostProcessResult.Register registerResult = new PostProcessResult.Register();
        registerResult.setTasks(postTasks);
        registerResult.setObjCommon(objCommon);
        registerResult.setTaskId(register.getTaskId());

        TaskContextHolder.setTaskId(register.getTaskId());
        Optional.ofNullable(traceManager.findPostProcessContextForTaskId(register.getTaskId()))
                .ifPresent(context -> {
                    context.setSuccessFlag(true);
                    context.setRegister(registerResult);
                });
    }

    private CimException handleException(Infos.ObjCommon objCommon, Exception e) {
        if(log.isErrorEnabled()) {
            log.error("Chained Task execute with error", e);
        }
        if (e instanceof CimException) {
            return (CimException) e;
        } else {
            ServiceException se = new ServiceException(2037, objCommon.getTransactionID(), e.getMessage());
            se.setData(e);
            return se;
        }
    }

}
