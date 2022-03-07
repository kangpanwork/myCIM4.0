package com.fa.cim.controller.post;

import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.result.Response;
import com.fa.cim.common.utils.ThreadContextHolder;
import com.fa.cim.controller.interfaces.post.IPostTaskController;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.frameworks.dto.pp.mode.ModifyAction;
import com.fa.cim.service.access.IAccessInqService;
import com.fa.cim.service.pprocess.PostProcessTaskService;
import com.fa.cim.service.pprocess.PostTaskParam;
import com.fa.cim.service.pprocess.PostTaskResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/post/tasks")
public class PostTaskController implements IPostTaskController {

    private final PostProcessTaskService taskService;
    private final IAccessInqService accessInqService;

    @Autowired
    public PostTaskController(PostProcessTaskService taskService, IAccessInqService accessInqService) {
        this.taskService = taskService;
        this.accessInqService = accessInqService;
    }

    @PostMapping("/active_task_list/inq")
    public Response postTaskListInq(@RequestBody PostTaskParam.TaskListInq taskListInq) {
        //init params
        final String transactionID = TransactionIDEnum.POST_PROCESS_ACTION_LIST_INQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //check input params
        //【step1】get schedule from calendar
        //【step2】call txAccessControlCheckInq(...)
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, taskListInq.getUser(),
                new Params.AccessControlCheckInqParams(true));

        // [step3] call sxPostProcessTaskListInq
        Page<PostTaskResult.Task> tasks = taskService.sxPostProcessTaskListInq(objCommon, taskListInq);

        // return result
        return Response.success(transactionID, tasks);
    }

    @PostMapping("/active_task_details/inq")
    public Response postTaskDetailsInq(@RequestBody PostTaskParam.TaskDetailsInq taskDetailsInq) {
        final String transactionID = TransactionIDEnum.POST_PROCESS_ACTION_LIST_INQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        //check input params
        //【step1】get schedule from calendar
        //【step2】call txAccessControlCheckInq(...)
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, taskDetailsInq.getUser(),
                new Params.AccessControlCheckInqParams(true));

        // [step3] call sxPostProcessTaskDetailsInq
        List<PostTaskResult.TaskDetail> taskDetails = taskService.sxPostProcessTaskDetailsInq(objCommon, taskDetailsInq);

        // return result
        return Response.success(transactionID, taskDetails);
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    @PostMapping("/active_task_exec/req")
    public Response postTaskForceExecuteReq(@RequestBody PostTaskParam.TaskExecReq taskExecReq) {
        final String transactionID = TransactionIDEnum.POST_PROCESS_EXEC_REQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        // check input params
        if (log.isInfoEnabled()) {
            log.info("【step1】get schedule from calendar");
            log.info("【step2】call txAccessControlCheckInq(...)");
        }
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, taskExecReq.getUser(),
                new Params.AccessControlCheckInqParams(true));

        // call sxExecutoePostProcessTaskByTaskIdReq
        taskService.sxExecutoePostProcessTaskByTaskIdReq(objCommon, taskExecReq.getTaskId());

        // return result
        return Response.success();
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    @PostMapping("/active_task_remove/req")
    public Response postTaskRemoveReq(@RequestBody PostTaskParam.TaskRemoveReq taskRemoveReq) {
        final TransactionIDEnum transactionId = TransactionIDEnum.POST_PROCESS_ACTION_UPDATE;
        ThreadContextHolder.setTransactionId(transactionId.getValue());
        //step1 - call txAccessControlCheckInq(...)

        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams(true);
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionId.getValue(), taskRemoveReq.getUser(),
                accessControlCheckInqParams);

        // get task id
        String taskId = taskRemoveReq.getTaskId();

        // get remove action
        ModifyAction removeAction = ModifyAction.valueOf(taskRemoveReq.getRemoveAction());

        // call sxRemovePostProcessTask
        taskService.sxRemovePostProcessTaskReq(objCommon, taskId, removeAction);
        return Response.success();
    }
}
