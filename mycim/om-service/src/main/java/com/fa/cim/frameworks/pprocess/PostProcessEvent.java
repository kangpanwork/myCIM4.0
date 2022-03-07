package com.fa.cim.frameworks.pprocess;


import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.utils.CimStringUtils;
import com.fa.cim.dto.Infos;
import com.fa.cim.frameworks.common.TaskContextHolder;
import com.fa.cim.frameworks.dto.pp.PostProcessContext;
import com.fa.cim.frameworks.dto.pp.PostProcessParam;
import com.fa.cim.frameworks.dto.pp.PostProcessTask;
import com.fa.cim.frameworks.dto.pp.mode.ExecutePhase;
import com.fa.cim.frameworks.pprocess.manager.PostProcessExecuteManager;
import com.fa.cim.frameworks.pprocess.manager.PostProcessTraceManager;
import com.fa.cim.newcore.dto.product.DispatchReadinessState;
import com.fa.cim.newcore.impl.bo.env.StandardProperties;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * the post process execution event
 *
 * @author Yuri
 */
@Slf4j
@Getter
@Setter
public abstract class PostProcessEvent extends ApplicationEvent {
    private static final long serialVersionUID = 264244907480278232L;

    private List<PostProcessTask> tasks;
    private Infos.ObjCommon objCommon;
    private String taskId;

    public PostProcessEvent(Object source) {
        super(source);
    }

    public PostProcessEvent(PostProcessEvent event) {
        super(event.source);
        this.tasks = event.tasks;
        this.objCommon = event.objCommon;
        this.taskId = event.taskId;
    }

    /**
     * synchronized execute event
     *
     * @param source source
     * @param phase execute phase
     * @return post process event
     */
    public static PostProcessEvent syncEvent(Object source, ExecutePhase phase) {
        return new SyncEvent(source, phase);
    }

    /**
     * asynchronized execute event
     *
     * @param source source
     * @param phase execute phase
     * @return post process event
     */
    public static PostProcessEvent asyncEvent(Object source, ExecutePhase phase) {
        return StandardProperties.OM_PP_SENTINEL_FLAG.isTrue() ?
                new SentinelEvent(source, phase) : new AsyncEvent(source, phase);
    }

    /**
     * the listener for the post process events
     */
    @Component
    public static class Listener {

        private final ApplicationEventPublisher eventPublisher;
        private final PostProcessTraceManager traceManager;
        private final PostProcessExecuteManager executeManager;

        @Autowired
        public Listener(ApplicationEventPublisher eventPublisher, PostProcessTraceManager traceManager,
                        PostProcessExecuteManager executeManager) {
            this.eventPublisher = eventPublisher;
            this.traceManager = traceManager;
            this.executeManager = executeManager;
        }

        /**
         * execute chained tasks
         *
         * @param event chained process event
         */
        @EventListener
        public void chainedEventProcessor(ChainedEvent event) {
            if (log.isInfoEnabled()) {
                log.info("Initialize Task<{}> CHAINED phase", event.getTaskId());
            }
            List<PostProcessTask> tasks = event.getTasks();

            List<PostProcessTask> chainedTasks = tasks.stream()
                    .filter(task -> task.getPhase() == ExecutePhase.CHAINED)
                    .collect(Collectors.toList());

            if(log.isTraceEnabled()) {
                log.trace(">>>> execute CHAINED tasks: size <{}>", chainedTasks.size());
            }
            PostProcessParam.Execute chainedParam = new PostProcessParam.Execute();
            chainedParam.setTasks(chainedTasks);
            chainedParam.setTaskId(event.getTaskId());
            chainedParam.setObjCommon(event.getObjCommon());
            chainedParam.setPhase(ExecutePhase.CHAINED);
            chainedParam.setMainSuccessful(true);
            DispatchReadinessState nextState;
            if (tasks.stream().anyMatch(task -> task.getPhase() == ExecutePhase.JOINED)) {
                nextState = DispatchReadinessState.Pending_JoinedTasks;
            } else if (tasks.stream().anyMatch(task -> task.getPhase() == ExecutePhase.POST)) {
                nextState = DispatchReadinessState.Pending_PostTasks;
            } else {
                nextState = DispatchReadinessState.Ready;
            }
            try {
                chainedParam.setNextStatus(nextState);
            } catch (RuntimeException e) {
                traceManager.closeTraceFor(TaskContextHolder.getTaskId());
                throw e;
            }
            executeManager.executeChained(chainedParam);
            if (log.isInfoEnabled()) {
                log.info("Task<{}> CHAINED phase end", event.getTaskId());
            }
        }

        /**
         * synchronously executing the post process tasks
         *
         * @param event sync tasks
         */
        @EventListener
        public void syncEventProcessor(SyncEvent event) {
            if (log.isInfoEnabled()) {
                log.info("Synchronously execute Post Process Task start ...");
            }
            String taskId = event.getTaskId();
            Optional.ofNullable(traceManager.findPostProcessContextForTaskId(taskId))
                    .orElseGet(() -> traceManager.openTraceFor(event.getObjCommon(), taskId));
            eventPublisher.publishEvent(event.newPhasedEvent());
            if (log.isInfoEnabled()) {
                log.info("Synchronously execute Post Process Task end ...");
            }
        }

        /**
         * asynchronously executing the post process tasks
         *
         * @param event async event
         */
        @Async(value = "postProcessExecutor")
        @EventListener
        public void asyncEventProcessor(AsyncEvent event) {
            if (log.isInfoEnabled()) {
                log.info("Asynchronously execute Post Process Task start ...");
            }
            Optional.ofNullable(traceManager.findPostProcessContextForTaskId(event.getTaskId()))
                    .orElseGet(() -> traceManager.openTraceFor(event.getObjCommon(), event.getTaskId()));
            eventPublisher.publishEvent(event.newPhasedEvent());
            if (log.isInfoEnabled()) {
                log.info("Asynchronously execute Post Process Task end ...");
            }
        }

        /**
         * pass the post process tasks execution to sentinel
         *
         * @param event sentinel event
         */
        @EventListener
        public void sentinelEventProcessor(SentinelEvent event) {
            traceManager.closeTraceFor(event.getTaskId());
            throw new ServiceException("Post Process Executing Sentinel is not found");
        }

        /**
         * execute joined tasks
         *
         * @param event joined process event
         */
        @EventListener
        @Order(10)
        public void joinedEventProcessor(JoinedEvent event) {
            if (log.isInfoEnabled()) {
                log.info("Initialize Task<{}> JOINED phase", event.getTaskId());
            }
            PostProcessContext context = traceManager.findPostProcessContextForTaskId(event.getTaskId());
            if (context == null) {
                if (log.isInfoEnabled()) {
                    log.info("Skip JOINED tasks executing: No Tasks Found");
                }
                return;
            } else if (!context.isSuccessFlag()) {
                if (log.isInfoEnabled()) {
                    log.info("Skip JOINED tasks executing: Main Process Failed");
                }
                return;
            }
            List<PostProcessTask> joinedTasks = event.getTasks().stream()
                    .filter(task -> task.getPhase() == ExecutePhase.JOINED)
                    .collect(Collectors.toList());

            if(log.isTraceEnabled()) {
                log.trace(">>>> execute JOINED tasks: size <{}>", joinedTasks.size());
            }
            PostProcessParam.Execute joinedParam = new PostProcessParam.Execute();
            joinedParam.setTasks(joinedTasks);
            joinedParam.setTaskId(event.getTaskId());
            joinedParam.setObjCommon(event.getObjCommon());
            joinedParam.setPhase(ExecutePhase.JOINED);
            joinedParam.setMainSuccessful(true);
            joinedParam.setNextStatus(event.getTasks().stream().anyMatch(task -> task.getPhase() == ExecutePhase.POST) ?
                    DispatchReadinessState.Pending_PostTasks : DispatchReadinessState.Ready);
            try {
                boolean isSuccessful = executeManager.executeJoined(joinedParam);
                if(log.isTraceEnabled()) {
                    log.trace(">>>> JOINED tasks proceed with no error <{}>", isSuccessful);
                }
                context.setSuccessFlag(isSuccessful);
            } catch (Exception e) {
                if (log.isErrorEnabled()) {
                    log.error("Unexpected error occured whilist processing JOINED tasks", e);
                }
            }
            if (log.isInfoEnabled()) {
                log.info("Task<{}> JOINED phase end", event.getTaskId());
            }
        }

        /**
         * execute post tasks
         *
         * @param event post process event
         */
        @EventListener
        @Order(50)
        public void postEventProcessor(PostEvent event) {
            if (log.isInfoEnabled()) {
                log.info("Initialize Task<{}> POST phase", event.getTaskId());
            }
            List<PostProcessTask> tasks = event.getTasks();
            List<PostProcessTask> postTasks = tasks.stream()
                    .filter(task -> task.getPhase() == ExecutePhase.POST)
                    .collect(Collectors.toList());
            if(log.isTraceEnabled()) {
                log.trace(">>>>>> Executing POST tasks: size <{}>", event.getTasks().size());
            }
            PostProcessParam.Execute postParam = new PostProcessParam.Execute();
            postParam.setTaskId(event.getTaskId());
            postParam.setTasks(postTasks);
            postParam.setPhase(ExecutePhase.POST);
            postParam.setObjCommon(event.getObjCommon());
            String taskId = event.getTaskId();
            postParam.setMainSuccessful(traceManager.findPostProcessContextForTaskId(taskId).isSuccessFlag());
            postParam.setNextStatus(DispatchReadinessState.Ready);
            try {
                executeManager.executePost(postParam);
            }finally {
                traceManager.closeTraceFor(taskId);
                if (log.isInfoEnabled()) {
                    log.info("Task<{}> POST phase end", event.getTaskId());
                }
            }
        }
    }

    /**
     * execute the tasks in chained phase
     *
     * @author Yuri
     */
    public static class ChainedEvent extends PostProcessEvent {
        private static final long serialVersionUID = 8286571416271347721L;

        public ChainedEvent(PostProcessEvent event) {
            super(event);
        }
    }

    /**
     * execute the tasks in joined phase
     *
     * @author Yuri
     */
    public static class JoinedEvent extends PostProcessEvent {
        private static final long serialVersionUID = 9152743231700176973L;

        public JoinedEvent(PostProcessEvent event) {
            super(event);
        }
    }

    /**
     * execute the tasks in post phase
     *
     * @author Yuri
     */
    public static class PostEvent extends JoinedEvent {
        private static final long serialVersionUID = 7340918072389454373L;

        private PostEvent(PostProcessEvent event) {
            super(event);
        }
    }

    /**
     * the entry point event
     *
     * @author Yuri
     */
    public static abstract class ProcessingEvent extends PostProcessEvent {

        private static final long serialVersionUID = 4771368704571860575L;

        private final ExecutePhase phase;

        private ProcessingEvent(Object source, ExecutePhase phase) {
            super(source);
            this.phase = phase;
        }

        /**
         * create the actual event for executing the task based on the phase
         *
         * @return post process even with phased
         */
        public PostProcessEvent newPhasedEvent() {
            if (phase == ExecutePhase.CHAINED) {
                return new ChainedEvent(this);
            } else {
                return new PostEvent(this);
            }
        }
    }

    /**
     * executing the tasks asychronously
     *
     * @author Yuri
     */
    public static class AsyncEvent extends ProcessingEvent {
        private static final long serialVersionUID = -9125139920275488795L;

        private AsyncEvent(Object source, ExecutePhase phase) {
            super(source, phase);
        }
    }

    /**
     * execute the tasks in the sentinel
     *
     * @author Yuri
     */
    public static class SentinelEvent extends ProcessingEvent {
        private static final long serialVersionUID = -7670259086235612598L;

        private SentinelEvent(Object source, ExecutePhase phase) {
            super(source, phase);
        }
    }

    /**
     * execute the tasks synchronously
     *
     * @author Yuri
     */
    public static class SyncEvent extends ProcessingEvent {
        private static final long serialVersionUID = 1588825829713502515L;

        private SyncEvent(Object source, ExecutePhase phase) {
            super(source, phase);
        }
    }
}
