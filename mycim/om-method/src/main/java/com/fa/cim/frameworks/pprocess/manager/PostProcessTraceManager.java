package com.fa.cim.frameworks.pprocess.manager;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.dto.Infos;
import com.fa.cim.frameworks.dto.pp.PostProcessContext;
import com.fa.cim.frameworks.dto.pp.PostProcessTrace;

import java.util.List;

/**
 * for recording the task execution result
 *
 * @author Yuri
 */
public interface PostProcessTraceManager {

    /**
     * open a new trace for the task
     *  @param taskId task id
     * @return
     */
    PostProcessContext openTraceFor(Infos.ObjCommon objCommon, String taskId);

    /**
     * add a new trace for the task
     *
     * @param trace post process trace
     */
    void addTracerFor(PostProcessTrace trace);

    /**
     * increate the operation changed count
     * opertation changed includes, but not necessary to, skip, passthrough, branch, rework
     *
     * @param taskId task id
     */
    void increateChangedCountFor(String taskId);

    /**
     * get the operation changed count for the task
     * opertation changed includes, but not necessary to, skip, passthrough, branch, rework
     *
     * @param taskId task id
     * @return operation changed cout
     */
    int currentChangedCountFor(String taskId);

    /**
     * find all trace by the executorId
     *
     * @param taskId task id
     * @param executorId executor id
     * @return matched trace
     */
    List<PostProcessTrace> allTraceForExecutor(String taskId, String executorId);

    /**
     * get all trace for the task
     *
     * @param taskId task id
     * @return matched trace
     */
    List<PostProcessTrace> allTrace(String taskId);

    /**
     * find all trace by the entity id
     *
     * @param taskId task id
     * @param entityID entity ID
     * @return matched trace
     */
    List<PostProcessTrace> allTraceForEntityID(String taskId, ObjectIdentifier entityID);

    /**
     * find all trace by the equipment id
     *
     * @param taskId task id
     * @param equipmentID equipment ID
     * @return matched trace
     */
    List<PostProcessTrace> allTraceForEquipmentID(String taskId, ObjectIdentifier equipmentID);

    /**
     * get the execute context of the task
     *
     * @param taskId task id
     * @return matched trace
     */
    PostProcessContext findPostProcessContextForTaskId(String taskId);

    /**
     * close the trace for the task
     *
     * @param taskId task id
     */
    void closeTraceFor(String taskId);

}
