package com.fa.cim.method;

import com.fa.cim.dto.Infos;
import com.fa.cim.frameworks.dto.pp.PostProcessTask;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * the post process task data manipulation methods
 *
 * @author Yuri
 */
public interface IPostProcesssTaskMethod {

    /**
     * create FIFO record in the database to track the executing status of the post tasks
     *
     * @param objCommon objCommon
     * @param postProcessTask a list of post process task
     */
    void createTaskRecords(Infos.ObjCommon objCommon, List<PostProcessTask> postProcessTask);

    void removeCompletedTasksByTaskId(Infos.ObjCommon objCommon, String taskId);

    boolean checkActiveTaskByLotId(Infos.ObjCommon objCommon, String lotId);

    /**
     * find a list of post process task with specified task id
     *
     * @param objCommon objCommon
     * @param conditions the task id of the post process tasks
     * @return a list of post process task
     */
    Page<PostProcessTask> findTaskRecordsByCondition(Infos.ObjCommon objCommon, PostProcessTask.Conditions conditions);

    /**
     * find one task record by task id and intex number
     *
     * @param objCommon objCommon
     * @param taskId task id
     * @param index index number
     * @return post process task
     */
    PostProcessTask findTaskRecordByTaskIdAndIndexNumber(Infos.ObjCommon objCommon, String taskId, int index);

    /**
     * find a list of post process task by specified task id
     *
     * @param objCommon objCommon
     * @param taskId the task id
     * @return a list of post process task
     */
    List<PostProcessTask> findTaskRecordsByTaskId(Infos.ObjCommon objCommon, String taskId);

    /**
     * remove a post process task record
     *
     * @param objCommon objCommon
     * @param taskId the task id of the post process tasks
     * @param indexNo the index number of the task
     */
    void removeTaskRecord(Infos.ObjCommon objCommon, String taskId, int indexNo);

    /**
     * add a list of detail information to a specific post process task
     *
     * @param objCommon objCommon
     * @param taskId the task id of the post process tasks
     * @param indexNo the index number of the task
     * @param details the information to add
     */
    void addTaskDetails(Infos.ObjCommon objCommon, String taskId, int indexNo, List<PostProcessTask.Detail> details);

    /**
     * remove the tasks that joinMode == NORMAL from execution
     *
     * @param objCommon objCommon
     * @param taskId the task id of the post process tasks
     */
    void removeNormalTaskRecords(Infos.ObjCommon objCommon, String taskId);

    /**
     * update a specific task's executing status
     * @param objCommon objCommon
     * @param task the task to update
     */
    void updateTaskStatus(Infos.ObjCommon objCommon, PostProcessTask task);

    void removePostProcessTasksByTaskId(Infos.ObjCommon objCommon, String taskId);

    void removePostProcessTaskTop(Infos.ObjCommon objCommon, String taskId);

}
