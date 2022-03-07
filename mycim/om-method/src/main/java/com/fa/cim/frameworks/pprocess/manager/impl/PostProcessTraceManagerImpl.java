package com.fa.cim.frameworks.pprocess.manager.impl;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.utils.CimStringUtils;
import com.fa.cim.dto.Infos;
import com.fa.cim.frameworks.common.TaskContextHolder;
import com.fa.cim.frameworks.dto.pp.PostProcessContext;
import com.fa.cim.frameworks.dto.pp.PostProcessTrace;
import com.fa.cim.frameworks.pprocess.manager.PostProcessTraceManager;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
public class PostProcessTraceManagerImpl implements PostProcessTraceManager {

    private final Map<String, PostProcessContext> contextPool = new ConcurrentHashMap<>(1024);


    @Override
    public PostProcessContext openTraceFor(Infos.ObjCommon objCommon, String taskId) {
        PostProcessContext postProcessContext = new PostProcessContext(objCommon, taskId);
        this.contextPool.put(taskId, postProcessContext);
        TaskContextHolder.setTaskId(taskId);
        return postProcessContext;
    }

    @Override
    public void addTracerFor(PostProcessTrace trace) {
        Optional.ofNullable(this.contextPool.get(trace.getTask().getTaskId()))
                .ifPresent(context -> context.getTrace().add(trace));
    }

    @Override
    public void increateChangedCountFor(String taskId) {
        Optional.ofNullable(this.contextPool.get(taskId))
                .ifPresent(context -> context.getOperationChangedCount().getAndIncrement());
    }

    @Override
    public int currentChangedCountFor(String taskId) {
        return Optional.ofNullable(this.contextPool.get(taskId))
                .map(context -> context.getOperationChangedCount().get())
                .orElse(0);
    }

    @Override
    public List<PostProcessTrace> allTraceForExecutor(String taskId, String executorId) {
        return Optional.ofNullable(this.contextPool.get(taskId))
                .map(PostProcessContext::getTrace)
                .orElseGet(Collections::emptyList).stream()
                .filter(t -> CimStringUtils.equals(executorId, t.getTask().getDefinition().getExecutor().getExecutorId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<PostProcessTrace> allTrace(String taskId) {
        return Optional.ofNullable(this.contextPool.get(taskId))
                .map(PostProcessContext::getTrace)
                .orElseGet(Collections::emptyList);
    }

    @Override
    public List<PostProcessTrace> allTraceForEntityID(String taskId, ObjectIdentifier entityID) {
        return Optional.ofNullable(this.contextPool.get(taskId))
                .map(PostProcessContext::getTrace)
                .orElseGet(Collections::emptyList).stream()
                .filter(t -> entityID.equals(t.getTask().getEntityID()))
                .collect(Collectors.toList());
    }

    @Override
    public List<PostProcessTrace> allTraceForEquipmentID(String taskId, ObjectIdentifier equipmentID) {
        return Optional.ofNullable(this.contextPool.get(taskId))
                .map(PostProcessContext::getTrace)
                .orElseGet(Collections::emptyList).stream()
                .filter(t -> equipmentID.equals(t.getTask().getEntityID()))
                .collect(Collectors.toList());
    }

    @Override
    public PostProcessContext findPostProcessContextForTaskId(String taskId) {
        return this.contextPool.get(taskId);
    }

    @Override
    public void closeTraceFor(String taskId) {
        this.contextPool.remove(taskId);
    }
}
