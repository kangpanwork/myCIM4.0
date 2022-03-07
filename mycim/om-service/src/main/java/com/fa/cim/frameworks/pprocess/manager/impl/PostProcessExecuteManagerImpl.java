package com.fa.cim.frameworks.pprocess.manager.impl;

import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.OmCode;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.pp.EntityType;
import com.fa.cim.frameworks.dto.pp.*;
import com.fa.cim.frameworks.dto.pp.mode.JoinMode;
import com.fa.cim.frameworks.dto.pp.mode.TaskStatus;
import com.fa.cim.frameworks.pprocess.api.definition.NonExecutor;
import com.fa.cim.frameworks.pprocess.api.definition.PostProcessExecutor;
import com.fa.cim.frameworks.pprocess.manager.PostProcessExecuteManager;
import com.fa.cim.frameworks.pprocess.manager.PostProcessTraceManager;
import com.fa.cim.method.IPostProcesssTaskMethod;
import com.fa.cim.newcore.bo.product.CimLot;
import com.fa.cim.newcore.dto.product.DispatchReadinessState;
import com.fa.cim.newcore.exceptions.CoreFrameworkException;
import com.fa.cim.newcore.impl.factory.GenericCoreFactory;
import com.fa.cim.newcore.impl.factory.GenericCorePool;
import com.fa.cim.newcore.lock.executor.ObjectLockExecutor;
import com.fa.cim.service.pprocess.PostProcessHoldService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.ProxyUtils;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Slf4j
@Component
public class PostProcessExecuteManagerImpl implements PostProcessExecuteManager {

    private static final String DETAIL_NAME_ERROR_CODE = "ErrorCode";
    private static final String DETAIL_NAME_ERROR_MSG = "ErrorMessage";

    private final PostProcessHoldService holdService;
    private final PostProcessTraceManager traceManager;
    private final IPostProcesssTaskMethod taskMethod;
    private final ObjectLockExecutor lockExecutor;
    private final GenericCoreFactory genericCoreFactory;
    private final TaskExecuteFinalizeService finalizeService;
    private final RetCodeConfig retCodeConfig;

    @Autowired
    public PostProcessExecuteManagerImpl(PostProcessHoldService holdService,
                                         PostProcessTraceManager traceManager,
                                         IPostProcesssTaskMethod taskMethod,
                                         ObjectLockExecutor lockExecutor,
                                         GenericCoreFactory genericCoreFactory,
                                         TaskExecuteFinalizeService finalizeService,
                                         RetCodeConfig retCodeConfig) {
        this.holdService = holdService;
        this.traceManager = traceManager;
        this.taskMethod = taskMethod;
        this.lockExecutor = lockExecutor;
        this.genericCoreFactory = genericCoreFactory;
        this.finalizeService = finalizeService;
        this.retCodeConfig = retCodeConfig;
    }


    @Override
    public void executeChained(PostProcessParam.Execute param) {
        Infos.ObjCommon objCommon = param.getObjCommon();
        this.execute(param)
                .onTaskError((task, result) -> {
                    Map<String, String> detailMap = result.getDetails().stream()
                            .collect(Collectors.toMap(PostProcessTask.Detail::getName, PostProcessTask.Detail::getValue));
                    OmCode systemError = retCodeConfig.getSystemError();
                    String errorCode = detailMap.getOrDefault(DETAIL_NAME_ERROR_CODE, String.valueOf(systemError.getCode()));
                    String errorMessage = detailMap.getOrDefault(DETAIL_NAME_ERROR_MSG, systemError.getMessage());
                    String taskId = task.getTaskId();
                    traceManager.findPostProcessContextForTaskId(taskId).setSuccessFlag(false);
                    throw new ServiceException(Integer.valueOf(errorCode), errorMessage, objCommon.getTransactionID());
                })
                .onFineLot(cimLot -> {
                    PostProcessContext context = traceManager.findPostProcessContextForTaskId(param.getTaskId());
                    if (context.isLockHoldEnabled()) {
                        DispatchReadinessState dispatchReadiness = cimLot.getDispatchReadiness();
                        cimLot.makeDispatchReady();
                        ObjectIdentifier lotID = ObjectIdentifier.build(cimLot.getIdentifier(), cimLot.getPrimaryKey());
                        Infos.LotHoldReq lotHoldReq = holdService.sxLockHoldOnLot(objCommon, lotID);
                        context.getLotHoldRecords().put(ObjectIdentifier.fetchValue(lotID), lotHoldReq);
                        cimLot.setDispatchReadiness(dispatchReadiness);
                    }
                })
                .execute();
    }

    @Override
    public boolean executeJoined(PostProcessParam.Execute param) {
        return this.transactionalExecute(param).execute();
    }

    @Override
    public void executePost(PostProcessParam.Execute param) {
        PostProcessContext context = traceManager.findPostProcessContextForTaskId(param.getTaskId());
        this.transactionalExecute(param)
                .onErrorLot(lotID -> {
                    if (context.isLockHoldEnabled()) {
                        holdService.sxLockHoldOnLot(param.getObjCommon(), lotID);
                    }
                })
                .onPost(action -> taskMethod.removeCompletedTasksByTaskId(param.getObjCommon(), param.getTaskId()))
                .execute();
    }

    private TaskExecuteAction execute(PostProcessParam.Execute param) {
        return new TaskExecuteAction(param, false);
    }

    private TaskExecuteAction transactionalExecute(PostProcessParam.Execute param) {
        return new TaskExecuteAction(param, true);
    }

    @Getter
    class TaskExecuteAction {

        private final PostProcessParam.Execute param;
        private final boolean transactionFlag;
        private final Set<ObjectIdentifier> fineLotIDs;
        private final Set<ObjectIdentifier> errorLotIDs;
        private final List<PostProcessTask> executedTasks;

        // actions
        private BiConsumer<PostProcessTask, PostProcessTask.Result> taskErrorAction = (task, result) -> {};
        private Consumer<CimLot> fineLotAction = fineLot -> {};
        private Consumer<ObjectIdentifier> errorLotAction = errorLot -> {};
        private Consumer<TaskExecuteAction> postAction = action -> {};
        private Consumer<TaskExecuteAction> finallyAction = action -> {};

        // execute result
        private boolean success = true;

        private TaskExecuteAction(PostProcessParam.Execute param, boolean transactionFlag) {
            this.param = param;
            this.transactionFlag = transactionFlag;
            this.fineLotIDs = param.getTasks().stream()
                    .filter(task -> task.getDefinition().getEntityType() == EntityType.Lot)
                    .map(PostProcessTask::getEntityID)
                    .collect(Collectors.toSet());
            this.errorLotIDs = new HashSet<>(fineLotIDs.size());
            this.executedTasks = new ArrayList<>(param.getTasks().size());
        }

        public TaskExecuteAction onTaskError(BiConsumer<PostProcessTask, PostProcessTask.Result> consumer) {
            this.taskErrorAction = consumer;
            return this;
        }

        public TaskExecuteAction onPost(Consumer<TaskExecuteAction> consumer) {
            this.postAction = consumer;
            return this;
        }

        public TaskExecuteAction onFinally(Consumer<TaskExecuteAction> consumer) {
            this.finallyAction = consumer;
            return this;
        }

        public TaskExecuteAction onFineLot(Consumer<CimLot> action) {
            this.fineLotAction = action;
            return this;
        }

        public TaskExecuteAction onErrorLot(Consumer<ObjectIdentifier> action) {
            this.errorLotAction = action;
            return this;
        }

        public boolean execute() {
            try {
                // ------------------------------------------------------------------- //
                //                        Begin Tasks Executing                        //
                // ------------------------------------------------------------------- //
                this.doTasksExecuting(new LinkedList<>(param.getTasks()));

                // ------------------------------------------------------------------- //
                //                      Finalizing Tasks Executing                     //
                // ------------------------------------------------------------------- //
                try {
                    finalizeService.doFinalize(this);
                } catch (Exception e) {
                    if (log.isErrorEnabled()) {
                        log.error("Errors whilist finalizing post process tasks", e);
                    }
                    return false;
                }
                return success;
            } finally {
                finallyAction.accept(this);
                lockExecutor.forceUnlockAll();
                GenericCorePool.clear();
            }
        }

        private void doTasksExecuting(List<PostProcessTask> tasks) {
            while (!tasks.isEmpty()) {
                // step 1:  pop the first task in the queue
                PostProcessTask task = tasks.remove(0);

                PostProcessTaskPlan.Definition definition = task.getDefinition();
                EntityType entityType = definition.getEntityType();
                ObjectIdentifier entityID = task.getEntityID();
                Class<? extends PostProcessExecutor> executorType = definition.getExecutor().getClass();

                if (log.isInfoEnabled()) {
                    log.info("Post Process Task<{}>, Executor<{}>, {}<{}> start ... ",
                            task.getTaskId(), ProxyUtils.getUserClass(executorType).getSimpleName(), entityType, entityID);
                }

                // step 2:  check the executor class is not NonExecutor, which do absolutely nothing
                if (NonExecutor.class.isAssignableFrom(executorType)) {
                    if (log.isDebugEnabled()) {
                        log.debug("Task is NonExecutor, ignore executing");
                    }
                    continue;
                }

                // step 3:  check if the executor requires the lot is already on the next operation if process move
                //          is involved
                if ((!param.isMainSuccessful() && definition.getJoinMode() != JoinMode.FINALLY)
                        || (entityType == EntityType.Lot
                        && definition.isNextOperationRequired()
                        && Optional.ofNullable(genericCoreFactory.getBO(CimLot.class, entityID))
                        .map(CimLot::isPendingMoveNext).orElse(true))) {
                    if (log.isWarnEnabled()) {
                        log.warn("<{}> requires Lot<{}> not pending move to next operation, skipped the excuting",
                                executorType.getSimpleName(), entityID);
                    }
                    // step 4-1:    the executor requires the lot is already on the next operation however the lot is
                    //              pending move to next operation, mark the task as skipped and ignore the executing
                    task.setStatus(TaskStatus.Skipped);
                } else {
                    if (log.isTraceEnabled()) {
                        log.trace("Proceeding executing <{}>, {}<{}>", executorType.getSimpleName(),
                                entityType, entityID);
                    }
                    // step 4-2:    the executor's executing condition is all met, begin the executing
                    PostProcessTask.Result result;
                    try {
                        result = task.execute();
                    } catch (Exception e) {
                        result = errorReturn(e);
                    }

                    // step 4-3:    recording the executing of the task in the context tracing
                    task.getDetails().addAll(result.getDetails());
                    traceManager.addTracerFor(new PostProcessTrace(task, result));
                    if (log.isDebugEnabled()) {
                        log.debug("<{}> execute result == {}", executorType.getSimpleName(), result.isSuccess());
                    }
                    if (result.isSuccess()) {
                        task.setStatus(TaskStatus.Completed);
                    } else {
                        task.setStatus(TaskStatus.Error);
                        // step 4-4-1:  the executing of the task is not a success, perform the error actions
                        success = false;
                        taskErrorAction.accept(task, result);
                        definition.getErrorMode().filterList(tasks, task);

                        // step 4-4-2:  recording the error lotIDs
                        if (task.getDefinition().getEntityType() == EntityType.Lot) {
                            CimLot cimLot = genericCoreFactory.getBO(CimLot.class, entityID);
                            if (null != cimLot) {
                                fineLotIDs.remove(entityID);
                                errorLotIDs.add(entityID);
                            }
                        }
                    }
                }
                executedTasks.add(task);
                if (log.isInfoEnabled()) {
                    log.info("Post Process Task<{}>, Executor<{}>, {}<{}> end ... ",
                            task.getTaskId(), ProxyUtils.getUserClass(executorType).getSimpleName(), entityType, entityID);
                }
            }
        }
    }

    private PostProcessTask.Result errorReturn(Throwable e) {
        PostProcessTask.Result retVal = new PostProcessTask.Result(false);
        if (e instanceof ServiceException) {
            ServiceException se = (ServiceException) e;
            int errorCode = se.getCode() != null ? se.getCode() : 2037;
            retVal.addDetail(DETAIL_NAME_ERROR_CODE, String.valueOf(errorCode));
        } else if (e instanceof CoreFrameworkException) {
            CoreFrameworkException ce = (CoreFrameworkException) e;
            int errorCode = ce.getCoreCode() != null ? ce.getCoreCode().getCode() : 2037;
            retVal.addDetail(DETAIL_NAME_ERROR_CODE, String.valueOf(errorCode));
        } else {
            retVal.addDetail(DETAIL_NAME_ERROR_CODE, String.valueOf(2037));
        }
        retVal.addDetail(DETAIL_NAME_ERROR_MSG, e.getMessage());
        return retVal;
    }

}
