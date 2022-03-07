package com.fa.cim.service.pprocess;

import com.fa.cim.dto.Infos;
import com.fa.cim.frameworks.dto.pp.PostProcessParam;
import com.fa.cim.frameworks.dto.pp.PostProcessResult;
import com.fa.cim.frameworks.dto.pp.PostProcessTask;
import com.fa.cim.frameworks.dto.pp.mode.ModifyAction;
import org.springframework.data.domain.Page;

import java.util.List;

public interface PostProcessTaskService {

    /**
     * query post process task list with conditions
     *
     * @param objCommon objCommon
     * @param taskListInq parameters
     * @return a list of response tasks
     */
    Page<PostTaskResult.Task> sxPostProcessTaskListInq(Infos.ObjCommon objCommon, PostTaskParam.TaskListInq taskListInq);

    /**
     * query post process task details
     *
     * @param objCommon objCommon
     * @param taskDetailsInq parameters
     * @return a list of response task details
     */
    List<PostTaskResult.TaskDetail> sxPostProcessTaskDetailsInq(Infos.ObjCommon objCommon, PostTaskParam.TaskDetailsInq taskDetailsInq);

    void sxExecutoePostProcessTaskByTaskIdReq(Infos.ObjCommon objCommon, String taskId);

    void sxRemovePostProcessTaskReq(Infos.ObjCommon objCommon, String taskId, ModifyAction modifyAction);
}
