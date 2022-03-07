package com.fa.cim.service.pprocess.impl;

import com.fa.cim.annotaion.OmService;
import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.utils.CimArrayUtils;
import com.fa.cim.common.utils.CimStringUtils;
import com.fa.cim.common.utils.Validations;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.pp.EntityType;
import com.fa.cim.frameworks.dto.pp.PostProcessTask;
import com.fa.cim.frameworks.dto.pp.PostProcessTaskPlan;
import com.fa.cim.frameworks.dto.pp.mode.ModifyAction;
import com.fa.cim.method.ILotMethod;
import com.fa.cim.method.IPostProcesssTaskMethod;
import com.fa.cim.newcore.dto.product.DispatchReadinessState;
import com.fa.cim.service.pprocess.PostProcessHoldService;
import com.fa.cim.service.pprocess.PostProcessTaskService;
import com.fa.cim.service.pprocess.PostTaskParam;
import com.fa.cim.service.pprocess.PostTaskResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@OmService
public class PostProcessTaskServiceImpl implements PostProcessTaskService {

    private final IPostProcesssTaskMethod postProcesssTaskMethod;
    private final ILotMethod lotMethod;
    private final PostProcessHoldService holdService;
    private final RetCodeConfig retCodeConfig;

    @Autowired
    public PostProcessTaskServiceImpl(IPostProcesssTaskMethod postProcesssTaskMethod, ILotMethod lotMethod,
                                      PostProcessHoldService holdService, RetCodeConfig retCodeConfig) {
        this.postProcesssTaskMethod = postProcesssTaskMethod;
        this.lotMethod = lotMethod;
        this.holdService = holdService;
        this.retCodeConfig = retCodeConfig;
    }

    @Override
    public Page<PostTaskResult.Task> sxPostProcessTaskListInq(Infos.ObjCommon objCommon,
                                                              PostTaskParam.TaskListInq taskListInq) {
        // validate the param is not null
        Validations.check(taskListInq == null, retCodeConfig.getInvalidInputParam());

        // create query condition base on the input
        PostProcessTask.Conditions conditions = new PostProcessTask.Conditions();
        conditions.setIndexNo(taskListInq.getIndexNo());
        conditions.setCarrierId(ObjectIdentifier.fetchValue(taskListInq.getCarrierID()));
        conditions.setControlJobId(ObjectIdentifier.fetchValue(taskListInq.getControlJobID()));
        conditions.setEquipmentId(ObjectIdentifier.fetchValue(taskListInq.getEquipmentID()));
        conditions.setExecutorId(taskListInq.getExecutorId());
        conditions.setLotId(ObjectIdentifier.fetchValue(taskListInq.getLotID()));
        conditions.setMaxRecordCount(taskListInq.getMaxRecordCount());
        conditions.setTaskId(taskListInq.getTaskId());
        conditions.setTransactionId(taskListInq.getTransactionId());
        conditions.setTrxUserId(ObjectIdentifier.fetchValue(taskListInq.getTrxUserID()));
        conditions.setSearchCondition(taskListInq.getSearchCondition());
        if(log.isDebugEnabled()) {
            log.debug("The query conditions are {}", conditions);
        }

        Page<PostProcessTask> postProcessTaskPage = postProcesssTaskMethod.findTaskRecordsByCondition(objCommon, conditions);
        return new PageImpl<>(postProcessTaskPage.getContent().stream().map(record -> {

            // mapping the task into
            PostProcessTaskPlan.Definition definition = record.getDefinition();
            PostTaskResult.Task task = new PostTaskResult.Task();
            task.setIndexNo(record.getIndexNo());
            task.setTransactionId(record.getObjCommon().getTransactionID());
            task.setChainedFlag(definition.isChained());
            task.setCommitMode(definition.getCommitMode().name());
            task.setControlJobID(record.getControlJobID());
            task.setCreateTime(record.getCreateTime().toString());
            task.setEntityID(record.getEntityID());
            task.setEntityType(definition.getEntityType().name());
            task.setEquipmentID(record.getEquipmentID());
            task.setErrorMode(definition.getErrorMode().name());
            task.setExecutorId(definition.getExecutor().getExecutorId());
            task.setIndexNo(record.getIndexNo());
            task.setJoinMode(definition.getJoinMode().name());
            task.setStatus(record.getStatus().name());
            task.setTrxMemo(record.getTrxMemo());
            task.setTaskId(record.getTaskId());
            task.setTrxTime(record.getTrxTime().toString());
            task.setTrxUserID(record.getTrxUserID());
            return task;

            // sort the result by the index number and task id
        }).sorted(Comparator.comparingInt(PostTaskResult.Task::getIndexNo))
                .sorted(Comparator.comparing(PostTaskResult.Task::getTaskId)).collect(Collectors.toList()),
                postProcessTaskPage.getPageable(), postProcessTaskPage.getTotalElements());
    }

    @Override
    public List<PostTaskResult.TaskDetail> sxPostProcessTaskDetailsInq(Infos.ObjCommon objCommon,
                                                                       PostTaskParam.TaskDetailsInq taskDetailsInq) {

        // validate the param is not null
        Validations.check(taskDetailsInq == null, retCodeConfig.getInvalidInputParam());

        // prepare method input
        String taskId = taskDetailsInq.getTaskId();
        if (log.isDebugEnabled()) {
            log.debug("The task Id is {}", taskId);
        }

        Integer indexNo = taskDetailsInq.getIndexNo();
        boolean queryAllFlag = null == indexNo || indexNo < 0;
        if (log.isDebugEnabled()) {
            log.debug("The index Number is {}", indexNo);
            log.debug("Query all flag is {}", queryAllFlag);
        }

        // query the tasks
        List<PostProcessTask> tasks = (queryAllFlag ?
                postProcesssTaskMethod.findTaskRecordsByTaskId(objCommon, taskId) :
                Collections.singletonList(postProcesssTaskMethod.findTaskRecordByTaskIdAndIndexNumber(objCommon, taskId, indexNo)))
                .stream().filter(Objects::nonNull).collect(Collectors.toList());

        // return empty list if the task is not found or the details are empty
        if (CimArrayUtils.isEmpty(tasks)) {
            return Collections.emptyList();
        }

        // create return list
        List<PostTaskResult.TaskDetail> retVal = new ArrayList<>();

        // populate the return list
        tasks.forEach(task -> {
            String executorId = task.getDefinition().getExecutor().getExecutorId();
            retVal.addAll(task.getDetails().stream().map(detail -> {
                PostTaskResult.TaskDetail taskDetail = new PostTaskResult.TaskDetail();
                taskDetail.setExecutorId(executorId);
                taskDetail.setName(detail.getName());
                taskDetail.setValue(detail.getValue());
                return taskDetail;
            }).collect(Collectors.toList()));
        });

        return CimArrayUtils.isEmpty(retVal) ? Collections.emptyList() : retVal;
    }

    @Override
    public void sxExecutoePostProcessTaskByTaskIdReq(Infos.ObjCommon objCommon, String taskId) {

    }

    @Override
    public void sxRemovePostProcessTaskReq(Infos.ObjCommon objCommon, String taskId, ModifyAction modifyAction) {
        Validations.check(CimStringUtils.isEmpty(taskId), retCodeConfig.getInvalidInputParam());

        if (log.isDebugEnabled()) {
            log.debug("Remove Action is {}", modifyAction.name());
        }

        Map<EntityType, List<PostProcessTask>> effectEntityMap = postProcesssTaskMethod.findTaskRecordsByTaskId(objCommon, taskId)
                .stream().collect(Collectors.groupingBy(task -> task.getDefinition().getEntityType()));

        Validations.check(effectEntityMap.isEmpty(), String.format("No Active Task By this ID <%s>", taskId));

        Set<ObjectIdentifier> effectedLots = effectEntityMap.get(EntityType.Lot).stream()
                .map(PostProcessTask::getEntityID).collect(Collectors.toSet());

        if (log.isDebugEnabled()) {
            log.debug("Start to remove the task");
        }
        switch (modifyAction) {
            case RemoveTask_byChain:
            case RemoveTask_byTaskId:
                postProcesssTaskMethod.removePostProcessTasksByTaskId(objCommon, taskId);
                break;
            case RemoveTask_Completed:
                postProcesssTaskMethod.removeCompletedTasksByTaskId(objCommon, taskId);
                break;
            case RemoveTask_Top:
                postProcesssTaskMethod.removePostProcessTaskTop(objCommon, taskId);
                break;
            default:
                throw new ServiceException(retCodeConfig.getInvalidParameter());

        }

        HashMap<ObjectIdentifier, Boolean> noActiveTaskFlags = new HashMap<>();

        effectedLots.forEach(lotID -> {
            boolean noActiveTaskFlag = !postProcesssTaskMethod.checkActiveTaskByLotId(objCommon,
                    ObjectIdentifier.fetchValue(lotID));
            noActiveTaskFlags.put(lotID, noActiveTaskFlag);
        });

        noActiveTaskFlags.forEach((lotID, noActiveTasks) -> {
            if (noActiveTasks) {
                lotMethod.lotDispatchReadinessSet(objCommon, lotID, DispatchReadinessState.Ready);
                List<Infos.LotHoldRecordInfo> lotHolds = lotMethod.lotHoldRecordGetDR(ObjectIdentifier.fetchValue(lotID));
                if (CimArrayUtils.isNotEmpty(lotHolds) && lotHolds.stream()
                        .map(hold -> ObjectIdentifier.fetchValue(hold.getHoldReasonID()))
                        .anyMatch(BizConstant.SP_REASON_LOTLOCK::equals)) {
                    holdService.sxReleaseLockHoldOnLot(objCommon, lotID, null);
                }
            }
        });
    }
}
