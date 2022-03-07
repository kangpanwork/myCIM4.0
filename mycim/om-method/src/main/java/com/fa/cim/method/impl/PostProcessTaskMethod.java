package com.fa.cim.method.impl;

import com.fa.cim.annotaion.OmMethod;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.utils.*;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.pp.EntityType;
import com.fa.cim.entity.nonruntime.postprocess.pprocess.CimPostProcessTaskDO;
import com.fa.cim.entity.nonruntime.postprocess.pprocess.CimPostProcessTaskDetailDO;
import com.fa.cim.entitysuper.BaseCimEntity;
import com.fa.cim.frameworks.dto.pp.PostProcessTask;
import com.fa.cim.frameworks.dto.pp.PostProcessTaskPlan;
import com.fa.cim.frameworks.dto.pp.mode.CommitMode;
import com.fa.cim.frameworks.dto.pp.mode.ErrorMode;
import com.fa.cim.frameworks.dto.pp.mode.JoinMode;
import com.fa.cim.frameworks.dto.pp.mode.TaskStatus;
import com.fa.cim.frameworks.pprocess.api.definition.NonExecutor;
import com.fa.cim.frameworks.pprocess.api.definition.PostProcessExecutor;
import com.fa.cim.jpa.CimJpaRepository;
import com.fa.cim.jpa.SearchCondition;
import com.fa.cim.method.ILotMethod;
import com.fa.cim.method.IPostProcesssTaskMethod;
import com.fa.cim.repository.standard.pprocess.PostProcessTaskDao;
import com.fa.cim.repository.standard.pprocess.PostProcessTaskDetailDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
@OmMethod
public class PostProcessTaskMethod implements IPostProcesssTaskMethod {

    private final PostProcessTaskDao taskDao;
    private final PostProcessTaskDetailDao taskDetailDao;
    private final ILotMethod lotMethod;

    @Autowired
    private CimJpaRepository cimJpaRepository;

    @Autowired
    public PostProcessTaskMethod(PostProcessTaskDao taskDao,
                                 PostProcessTaskDetailDao taskDetailDao,
                                 ILotMethod lotMethod) {
        this.taskDao = taskDao;
        this.taskDetailDao = taskDetailDao;
        this.lotMethod = lotMethod;
    }

    @Override
    public void removeCompletedTasksByTaskId(Infos.ObjCommon objCommon, String taskId) {
        List<CimPostProcessTaskDO> tasks = taskDao.findAllByTaskIdAndTaskStatusIn(taskId,
                TaskStatus.Completed.name(), TaskStatus.Skipped.name());
        tasks.stream().map(BaseCimEntity::getId).forEach(taskDetailDao::removeAllByRefKey);
        taskDao.deleteAll(tasks);
    }

    @Override
    public void createTaskRecords(Infos.ObjCommon objCommon, List<PostProcessTask> postProcessTask) {
        if (CimArrayUtils.isEmpty(postProcessTask)) return;
        AtomicInteger taskCounter = new AtomicInteger(0);
        List<TempTaskData> tempTasks = postProcessTask.stream()
                .filter(task -> !NonExecutor.class.isAssignableFrom(task.getDefinition().getExecutor().getClass()))
                .map(task -> createTempTaskData(objCommon, taskCounter, task))
                .collect(Collectors.toList());

        // save OQPPTASK
        List<CimPostProcessTaskDO> taskDataList = tempTasks.stream()
                .map(taskData -> taskData.taskData)
                .collect(Collectors.toList());
        this.taskDao.saveAll(taskDataList);

        // save OQPPTASK_DETAIL
        List<CimPostProcessTaskDetailDO> detaiDataList = tempTasks.stream()
                .flatMap(taskData -> taskData.detailDataList.stream())
                .collect(Collectors.toList());
        this.taskDetailDao.saveAll(detaiDataList);
    }

    @Override
    public boolean checkActiveTaskByLotId(Infos.ObjCommon objCommon, String lotId) {
        return taskDao.countAllByEntityIdAndEntityTypeAndTaskStatusNotIn(lotId,
                EntityType.Lot.name(), TaskStatus.Completed.name(), TaskStatus.Skipped.name()) > 0;
    }

    @Override
    public Page<PostProcessTask> findTaskRecordsByCondition(Infos.ObjCommon objCommon, PostProcessTask.Conditions conditions) {
        String querySql = "SELECT * FROM OQPPTASK WHERE 1=1 ";
        List<Object> args = new ArrayList<>(10);
        if (CimStringUtils.isNotEmpty(conditions.getTaskId())) {
            querySql += " AND TASK_ID = ?";
            args.add(conditions.getTaskId());
        }

        if (conditions.getIndexNo() != null) {
            querySql += " AND IDX_NO = ?";
            args.add(conditions.getIndexNo());
        }

        if (CimStringUtils.isNotEmpty(conditions.getExecutorId())) {
            querySql += " AND EXECUTOR_ID = ?";
            args.add(conditions.getExecutorId());
        }

        if (CimStringUtils.isNotEmpty(conditions.getTransactionId())) {
            querySql += " AND TRX_ID = ?";
            args.add(conditions.getTransactionId());
        }

        if (CimStringUtils.isNotEmpty(conditions.getControlJobId())) {
            querySql += " AND CJ_ID = ?";
            args.add(conditions.getControlJobId());
        }

        if (CimStringUtils.isNotEmpty(conditions.getTrxUserId())) {
            querySql += " AND TRX_USER_ID = ?";
            args.add(conditions.getTrxUserId());
        }

        String equipmentId = conditions.getEquipmentId();
        if (CimStringUtils.isNotEmpty(equipmentId)) {
            boolean wildCardFlag = equipmentId.contains("*");
            querySql += " AND EQP_ID " + (wildCardFlag ? " LIKE ?" : " = ?");
            args.add(wildCardFlag ? equipmentId.replaceAll("\\*", "%") : equipmentId);
        }

        String lotId = conditions.getLotId();
        if (CimStringUtils.isNotEmpty(lotId)) {
            boolean wildCardFlag = lotId.contains("*");
            querySql += " AND ENTITY_ID " + (wildCardFlag ? " LIKE ?" : " = ?" + " AND ENTITY_TYPE = 'Lot'");
            args.add(wildCardFlag ? lotId.replaceAll("\\*", "%") : lotId);
        }

        String carrierId = conditions.getCarrierId();
        if (CimStringUtils.isNotEmpty(carrierId)) {
            boolean wildCardFlag = carrierId.contains("*");
            querySql += " AND ENTITY_ID " + (wildCardFlag ? " LIKE ?" : " = ?" + " AND ENTITY_TYPE = 'Durable'");
            args.add(wildCardFlag ? carrierId.replaceAll("\\*", "%") : carrierId);
        }

        Page<CimPostProcessTaskDO> query;
        SearchCondition searchCondition = conditions.getSearchCondition();
        if (searchCondition != null) {
            query = cimJpaRepository.query(querySql, CimPostProcessTaskDO.class, searchCondition, args.toArray());
        } else {
            query = new PageImpl<>(cimJpaRepository.query(querySql, CimPostProcessTaskDO.class, args.toArray()));
        }

        return new PageImpl<>(query.stream().map(taskData -> convertToPostProcessTask(objCommon, taskData)).
                collect(Collectors.toList()), query.getPageable(), query.getTotalElements());
    }

    @Override
    public PostProcessTask findTaskRecordByTaskIdAndIndexNumber(Infos.ObjCommon objCommon, String taskId, int index) {
        return Optional.ofNullable(this.taskDao.findByTaskIdAndIndexNumber(taskId, index))
                .map(data -> convertToPostProcessTask(objCommon, data))
                .orElse(null);
    }

    private PostProcessTask convertToPostProcessTask(Infos.ObjCommon objCommon, CimPostProcessTaskDO taskData) {
        PostProcessTask task = new PostProcessTask();
        task.setTaskId(taskData.getTaskId());
        task.setControlJobID(ObjectIdentifier.build(taskData.getControJobId(), taskData.getControlJobRkey()));
        task.setEntityID(ObjectIdentifier.build(taskData.getEntityId(), taskData.getEntityRkey()));
        task.setEquipmentID(ObjectIdentifier.build(taskData.getEquipmentId(), taskData.getEquipmentRkey()));
        task.setIndexNo(taskData.getIndexNumber());
        Infos.ObjCommon mainCommon = new Infos.ObjCommon();
        mainCommon.setTransactionID(taskData.getTransactionId());
        mainCommon.setUser(objCommon.getUser());
        Infos.TimeStamp timeStamp = new Infos.TimeStamp();
        timeStamp.setReportTimeStamp(taskData.getTrxTime());
        mainCommon.setTimeStamp(timeStamp);
        task.setObjCommon(objCommon);
        task.setCreateTime(taskData.getCreateTime());
        task.setTrxUserID(ObjectIdentifier.buildWithValue(taskData.getTrxUserId()));
        task.setTrxMemo(taskData.getTrxMemo());
        task.setTrxTime(taskData.getTrxTime());
        task.setStatus(TaskStatus.valueOf(taskData.getTaskStatus()));
        PostProcessTaskPlan.Definition definition = new PostProcessTaskPlan.Definition();
        task.setDefinition(definition);
        String executorId = taskData.getExecutorId();
        String executeorBeanName = Character.isUpperCase(executorId.charAt(1)) ? executorId :
                Character.toLowerCase(executorId.charAt(0)) + executorId.substring(1);

        try {
            definition.setExecutor(SpringContextUtil.getSingletonBean(executeorBeanName, PostProcessExecutor.class));
        } catch (NoSuchBeanDefinitionException e) {
            if (log.isWarnEnabled()) {
                log.warn("No executor bean named {} found, ignore this task's creation", executorId);
            }
            return null;
        }
        definition.setChained(CimBooleanUtils.isTrue(taskData.getChainedFlag()));
        definition.setCommitMode(CommitMode.valueOf(taskData.getCommitMode()));
        definition.setErrorMode(ErrorMode.valueOf(taskData.getErrorMode()));
        definition.setJoinMode(JoinMode.valueOf(taskData.getJoinMode()));
        definition.setEntityType(EntityType.valueOf(taskData.getEntityType()));
        task.setDetails(taskDetailDao.findAllByRefKeyOrderByIndexNo(taskData.getId()).stream()
                .map(detailData -> new PostProcessTask.Detail(detailData.getName(), detailData.getValue()))
                .collect(Collectors.toList()));
        return task;
    }

    @Override
    public List<PostProcessTask> findTaskRecordsByTaskId(Infos.ObjCommon objCommon, String taskId) {
        return taskDao.findAllByTaskId(taskId).stream()
                .map(data -> convertToPostProcessTask(objCommon, data))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public void removeTaskRecord(Infos.ObjCommon objCommon, String taskId, int indexNo) {
        if (CimStringUtils.isEmpty(taskId)) return;
        this.taskDetailDao.removeAllByTaskIdAndIndexNumber(taskId, indexNo);
        this.taskDao.removeByTaskIdAndIndexNumber(taskId, indexNo);
    }

    @Override
    public void addTaskDetails(Infos.ObjCommon objCommon, String taskId, int indexNo, List<PostProcessTask.Detail> details) {
        if (details.isEmpty()) return;
        details.forEach(detail -> this.taskDetailDao.insertANewDetail(
                SnowflakeIDWorker.getInstance().generateId(CimPostProcessTaskDetailDO.class),
                taskId, indexNo, detail.getName(), detail.getValue()));
    }

    @Override
    public void removeNormalTaskRecords(Infos.ObjCommon objCommon, String taskId) {
        if (CimStringUtils.isEmpty(taskId)) return;
        this.taskDetailDao.removeAllByTaskIdAndJoinMode(taskId, JoinMode.NORMAL.name());
        this.taskDao.removeAllByTaskIdAndJoinMode(taskId, JoinMode.NORMAL.name());
    }

    @Override
    public void updateTaskStatus(Infos.ObjCommon objCommon, PostProcessTask task) {
        String taskId = task.getTaskId();
        int indexNo = task.getIndexNo();
        List<PostProcessTask.Detail> details = task.getDetails();
        this.taskDao.updateTaskStatus(taskId, indexNo, task.getStatus().name());
        if (CimArrayUtils.isNotEmpty(details)) {
            details.forEach(detail -> this.taskDetailDao.insertANewDetail(
                    SnowflakeIDWorker.getInstance().generateId(CimPostProcessTaskDetailDO.class),
                    taskId, indexNo, detail.getName(), detail.getValue()));
        }
    }

    @Override
    public void removePostProcessTasksByTaskId(Infos.ObjCommon objCommon, String taskId) {
        if (CimStringUtils.isEmpty(taskId)) {
            return;
        }

        // if the tasks to remove contains "LotProcessMoveExecutor", set the related lots' pending move next flag to false
        taskDao.findAllByTaskId(taskId).stream()
                .filter(task -> CimStringUtils.equals(task.getExecutorId(), "LotProcessMoveExecutor"))
                .forEach(task -> {
                    ObjectIdentifier lotID = ObjectIdentifier.build(task.getEntityId(), task.getEntityRkey());
                    lotMethod.setLotMoveNextRequired(objCommon, lotID, false);
                });

        taskDetailDao.removeAllByTaskId(taskId);
        taskDao.removeAllByTaskId(taskId);
    }

    @Override
    public void removePostProcessTaskTop(Infos.ObjCommon objCommon, String taskId) {
        if (CimStringUtils.isEmpty(taskId)) {
            return;
        }

        // if the task to remove contains "LotProcessMoveExecutor", set the related lot's pending move next flag to false
        CimPostProcessTaskDO topTask = taskDao.findTopByTaskIdOrderByIndexNumber(taskId);
        if (null == topTask) {
            return;
        }
        boolean isProcessMoveExcutor = CimStringUtils.equals(topTask.getExecutorId(), "LotProcessMoveExecutor");
        if (isProcessMoveExcutor) {
            ObjectIdentifier lotID = ObjectIdentifier.build(topTask.getEntityId(), topTask.getEntityRkey());
            lotMethod.setLotMoveNextRequired(objCommon, lotID, false);
        }
        taskDetailDao.removeTopByTaskId(taskId);
        taskDao.removeTopByTaskId(taskId);
    }

    private TempTaskData createTempTaskData(Infos.ObjCommon objCommon, AtomicInteger taskCounter, PostProcessTask task) {
        int count = taskCounter.getAndIncrement();
        task.setIndexNo(count);
        TempTaskData tempTaskData = new TempTaskData();
        CimPostProcessTaskDO taskData = new CimPostProcessTaskDO();
        tempTaskData.taskData = taskData;
        taskData.setTransactionId(task.getObjCommon().getTransactionID());
        taskData.setTaskStatus(TaskStatus.Reserved.name());
        taskData.setJoinMode(task.getDefinition().getJoinMode().name());
        taskData.setTrxUserId(ObjectIdentifier.fetchValue(objCommon.getUser().getUserID()));
        taskData.setId(SnowflakeIDWorker.getInstance().generateId(CimPostProcessTaskDO.class));
        taskData.setTrxTime(objCommon.getTimeStamp().getReportTimeStamp());
        taskData.setTaskId(task.getTaskId());
        taskData.setIndexNumber(count);
        taskData.setExecutorId(task.getDefinition().getExecutor().getExecutorId());
        taskData.setErrorMode(task.getDefinition().getErrorMode().name());
        taskData.setEntityType(task.getDefinition().getEntityType().name());
        taskData.setEntityRkey(ObjectIdentifier.fetchReferenceKey(task.getEntityID()));
        taskData.setEntityId(ObjectIdentifier.fetchValue(task.getEntityID()));
        taskData.setCreateTime(objCommon.getTimeStamp().getReportTimeStamp());
        taskData.setControlJobRkey(ObjectIdentifier.fetchReferenceKey(task.getControlJobID()));
        taskData.setControJobId(ObjectIdentifier.fetchValue(task.getControlJobID()));
        taskData.setCommitMode(task.getDefinition().getCommitMode().name());
        taskData.setChainedFlag(task.getDefinition().isChained());
        taskData.setEquipmentId(ObjectIdentifier.fetchValue(task.getEquipmentID()));
        taskData.setEquipmentRkey(ObjectIdentifier.fetchReferenceKey(task.getEquipmentID()));
        taskData.setUpdateTime(objCommon.getTimeStamp().getReportTimeStamp());
        AtomicInteger detailCounter = new AtomicInteger(0);
        tempTaskData.detailDataList = task.getDetails().stream().map(detail -> {
            CimPostProcessTaskDetailDO detailData = new CimPostProcessTaskDetailDO();
            detailData.setRefKey(taskData.getId());
            detailData.setName(detail.getName());
            detailData.setValue(detail.getValue());
            detailData.setIndexNo(detailCounter.getAndIncrement());
            return detailData;
        }).collect(Collectors.toList());
        return tempTaskData;
    }

    private static class TempTaskData {
        private CimPostProcessTaskDO taskData;
        private List<CimPostProcessTaskDetailDO> detailDataList;
    }
}
