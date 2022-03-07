package com.fa.cim.frameworks.pprocess.proxy.aspect;

import com.fa.cim.common.exception.CimException;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.utils.CimArrayUtils;
import com.fa.cim.common.utils.CimStringUtils;
import com.fa.cim.common.utils.SpringContextUtil;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.pp.EntityType;
import com.fa.cim.frameworks.dto.pp.PostProcessContext;
import com.fa.cim.frameworks.dto.pp.PostProcessParam;
import com.fa.cim.frameworks.dto.pp.PostProcessTask;
import com.fa.cim.frameworks.dto.pp.PostProcessTaskPlan;
import com.fa.cim.frameworks.dto.pp.mode.ExecutePhase;
import com.fa.cim.frameworks.pprocess.PostProcessEvent;
import com.fa.cim.frameworks.pprocess.manager.PostProcessExecuteManager;
import com.fa.cim.frameworks.pprocess.manager.PostProcessPlanManager;
import com.fa.cim.frameworks.pprocess.manager.PostProcessTraceManager;
import com.fa.cim.newcore.bo.product.CimLot;
import com.fa.cim.newcore.dto.product.DispatchReadinessState;
import com.fa.cim.newcore.impl.factory.GenericCoreFactory;
import com.fa.cim.newcore.impl.factory.GenericCorePool;
import com.fa.cim.newcore.lock.executor.ObjectLockExecutor;
import com.fa.cim.service.pprocess.PostProcessHoldService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * the aspect for task executors
 *
 * @author Yuri
 */
@Slf4j
@Aspect
@Component
public class PostProcessTaskExecutorAspect {

    private final PostProcessHoldService holdService;
    private final PostProcessTraceManager traceManager;
    private final PostProcessPlanManager planManager;
    private final ObjectLockExecutor lockExecutor;
    private final GenericCoreFactory coreFactory;

    @Autowired
    public PostProcessTaskExecutorAspect(PostProcessHoldService holdService, PostProcessTraceManager traceManager,
                                         PostProcessPlanManager planManager,
                                         ObjectLockExecutor lockExecutor, GenericCoreFactory coreFactory) {
        this.holdService = holdService;
        this.traceManager = traceManager;
        this.planManager = planManager;
        this.lockExecutor = lockExecutor;
        this.coreFactory = coreFactory;
    }

    @Pointcut("@within(com.fa.cim.frameworks.pprocess.api.annotations.PostProcessTaskHandler)")
    public void isPostProcessTaskHandler() {
    }

    /**
     * replace the result as failed when an execption is thrown during the post process task executing
     *
     * @param joinPoint join point
     * @return failed result
     */
    @Around(value = "isPostProcessTaskHandler()")
    public Object taskExecutorAdvice(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        if (CimStringUtils.equals(signature.getName(), "doExecute")) {
            if (log.isTraceEnabled()) {
                log.trace(">>>>> isDoExecute == true");
            }
            return invokeDoExecute(joinPoint);
        } else {
            if (log.isTraceEnabled()) {
                log.trace(">>>>> isDoExecute == false");
            }
            try {
                return joinPoint.proceed();
            } catch (Throwable throwable) {
                throw handleException(throwable);
            }
        }
    }

    private Object invokeDoExecute(ProceedingJoinPoint joinPoint) {
        PostProcessTask.Param param = (PostProcessTask.Param) joinPoint.getArgs()[0];
        if (log.isTraceEnabled()) {
            log.trace(">>>>> Execute Phase == {}", param.getPhase().name());
        }
        String taskId = param.getTaskId();
        PostProcessContext context = traceManager.findPostProcessContextForTaskId(taskId);
        boolean lockHoldEnabled = param.getPhase() != ExecutePhase.CHAINED && context.isLockHoldEnabled();
        Infos.ObjCommon objCommon = param.getObjCommon();
        try {
            DispatchReadinessState readinessState = null;
            if (param.getEntityType() == EntityType.Lot) {
                CimLot cimLot = coreFactory.getBO(CimLot.class, param.getEntityID());
                readinessState = cimLot.getDispatchReadiness();
                cimLot.makeDispatchReady();
            }
            if (lockHoldEnabled) {
                this.releaseLockHold(param, context);
            }
            Object result = joinPoint.proceed(joinPoint.getArgs());
            if (result instanceof PostProcessTask.Result) {
                PostProcessTask.Result executeResult = (PostProcessTask.Result) result;
                List<PostProcessTask.ExtraTask> extraTasks = executeResult.getExtraTasks();
                if (CimArrayUtils.isNotEmpty(extraTasks)) {
                    this.doExtraTasks(param, extraTasks);
                }
            }
            if (lockHoldEnabled) {
                this.lockHold(param, context);
            }
            if (param.getEntityType() == EntityType.Lot) {
                CimLot cimLot = coreFactory.getBO(CimLot.class, param.getEntityID());
                cimLot.setDispatchReadiness(readinessState);
            }
            return result;
        } catch (Throwable t) {
            if (log.isErrorEnabled()) {
                log.error("Post Process Task Error", t);
            }
            if (t instanceof CimException) {
                throw (CimException) t;
            } else {
                throw new ServiceException(2037, "Unexpected Error during post process task",
                        objCommon.getTransactionID());
            }
        } finally {
            lockExecutor.forceUnlockAll();
            GenericCorePool.clear();
        }
    }

    private void doExtraTasks(PostProcessTask.Param param, List<PostProcessTask.ExtraTask> extraTasks) {
        ObjectIdentifier userID = param.getObjCommon().getUser().getUserID();
        PostProcessTaskPlan.generateTaskId(ObjectIdentifier.fetchValue(userID));
        List<PostProcessTask> tasks = extraTasks.stream().flatMap(extraTask -> {
            PostProcessTaskPlan plan = planManager.findPostProcessPlan(extraTask.getTrxId());
            PostProcessParam.CreateTask createTask = new PostProcessParam.CreateTask();
            createTask.setObjCommon(param.getObjCommon());
            createTask.setLotID(Collections.singletonList(extraTask.getEntityID()));
            createTask.setEquipmentID(param.getEquipmentID());
            createTask.setControlJobID(param.getControlJobID());
            return plan.generateTasks(createTask).stream();
        }).collect(Collectors.toList());
        PostProcessTaskPlan.clearTaskId();
        PostProcessEvent chainedEvent = PostProcessEvent.syncEvent(this, ExecutePhase.CHAINED);
        String taskId = tasks.stream().findFirst().map(PostProcessTask::getTaskId).orElse("");
        if (CimStringUtils.isNotEmpty(taskId)) {
            try {
                PostProcessContext postProcessContext = traceManager.openTraceFor(param.getObjCommon(), taskId);
                postProcessContext.setSuccessFlag(true);
                chainedEvent.setTaskId(taskId);
                chainedEvent.setTasks(tasks);
                chainedEvent.setObjCommon(param.getObjCommon());
                ApplicationContext applicationContext = SpringContextUtil.getApplicationContext();
                applicationContext.publishEvent(chainedEvent);
                PostProcessEvent joinedEvent = PostProcessEvent.syncEvent(this, ExecutePhase.JOINED);
                joinedEvent.setObjCommon(param.getObjCommon());
                joinedEvent.setTasks(tasks);
                joinedEvent.setTaskId(taskId);
                applicationContext.publishEvent(joinedEvent);
                traceManager.closeTraceFor(taskId);
            } catch (Exception e) {
                if (log.isErrorEnabled()) {
                    log.error("Post Process Error", e);
                }
                throw handleException(e);
            } finally {
                traceManager.closeTraceFor(taskId);
            }

        }
    }

    /**
     * release LOCK hold if required
     * @param param task param
     * @param context task context
     */
    private void releaseLockHold(PostProcessTask.Param param, PostProcessContext context) {
        EntityType entityType = param.getEntityType();
        ObjectIdentifier entityID = param.getEntityID();
        Infos.ObjCommon objCommon = param.getObjCommon();
        if (log.isDebugEnabled()) {
            log.debug("Perform release LOCK hold on {}::{}", entityType.name(), entityID);
        }
        switch (entityType) {
            case Lot:
                if (log.isTraceEnabled()) {
                    log.trace("Release LOCK as Lot for {}", entityID);
                }
                Map<String, Infos.LotHoldReq> lotHoldRecords = context.getLotHoldRecords();
                String lotId = ObjectIdentifier.fetchValue(entityID);
                Infos.LotHoldReq holdRecord = lotHoldRecords.get(lotId);
                this.holdService.sxReleaseLockHoldOnLot(objCommon, entityID, holdRecord);
                lotHoldRecords.remove(lotId);
                break;
            case Durable:
                if (log.isTraceEnabled()) {
                    log.trace("Release LOCK as Durable for {}", entityID);
                }
                this.holdService.sxReleaseLockHoldOnDurable(objCommon, entityID);
                break;
            default:
                if (log.isTraceEnabled()) {
                    log.trace("No need to release LOCK for Equipment");
                }
                break;
        }
    }

    /**
     * LOCK hold if required
     *
     * @param param the post process param
     * @param context task context
     */
    private void lockHold(PostProcessTask.Param param, PostProcessContext context) {
        Infos.ObjCommon objCommon = param.getObjCommon();
        ObjectIdentifier entityID = param.getEntityID();
        EntityType entityType = param.getEntityType();
        if (log.isDebugEnabled()) {
            log.debug("Perform LOCK hold on {}::{}", entityType.name(), entityID);
        }
        switch (entityType) {
            case Lot:
                if (log.isTraceEnabled()) {
                    log.trace("LOCK hold as Lot for {}", entityID);
                }
                Infos.LotHoldReq lotHoldReq = this.holdService.sxLockHoldOnLot(objCommon, entityID);
                context.getLotHoldRecords().put(ObjectIdentifier.fetchValue(entityID), lotHoldReq);
                break;
            case Durable:
                if (log.isTraceEnabled()) {
                    log.trace("LOCK hold as Durable for {}", entityID);
                }
                this.holdService.sxLockHoldOnDurable(objCommon, entityID);
                break;
            default:
                if (log.isTraceEnabled()) {
                    log.trace("No need to LOCK hold for Equipment");
                }
                break;
        }
    }

    private static CimException handleException(Throwable t) {
        if (t instanceof CimException) {
            return (CimException) t;
        } else {
            return new ServiceException(t.getMessage());
        }
    }

}
