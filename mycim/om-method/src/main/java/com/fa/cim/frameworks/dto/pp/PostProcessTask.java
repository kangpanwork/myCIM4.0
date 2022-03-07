package com.fa.cim.frameworks.dto.pp;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.utils.CimStringUtils;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.pp.EntityType;
import com.fa.cim.frameworks.dto.pp.mode.ExecutePhase;
import com.fa.cim.frameworks.dto.pp.mode.TaskStatus;
import com.fa.cim.frameworks.pprocess.api.definition.PostProcessExecutor;
import com.fa.cim.frameworks.pprocess.manager.PostProcessTransactionManager;
import com.fa.cim.jpa.SearchCondition;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * the post process task for execution
 *
 * @author Yuri
 */
@Getter
@Setter
public class PostProcessTask {

    private String taskId;
    private ExecutePhase phase;
    private int indexNo;
    private PostProcessTaskPlan.Definition definition;
    private TaskStatus status = TaskStatus.Reserved;
    private Infos.ObjCommon objCommon;
    private ObjectIdentifier entityID;
    private ObjectIdentifier equipmentID;
    private ObjectIdentifier controlJobID; // optional
    private List<Detail> details = new ArrayList<>();
    private Timestamp createTime;
    private Timestamp trxTime;
    private ObjectIdentifier trxUserID;
    private String trxMemo;

    public Param toParam() {
        return new Param(this);
    }

    /**
     * do compensate according to the executor
     */
    public void compensate() {
        this.getDefinition().getExecutor().doCompensate(this.toParam());
    }

    /**
     * do execute according to the executor
     *
     * @return the execute result
     */
    public Result execute() {
        return this.getDefinition().getExecutor().doExecute(this.toParam());
    }

    /**
     * do execute according to the executor with independent transaction
     *
     * @param transactionManager the post process transaction manager
     * @return the execute reuslt
     */
    public Result transactionalExecute(PostProcessTransactionManager transactionManager) {
        return this.getDefinition().getCommitMode().apply(transactionManager, this);
    }

    @Getter
    @Setter
    @ToString
    public static class Conditions {
        private String taskId;
        private Integer indexNo;
        private TaskStatus taskStatus;
        private String lotId;
        private String equipmentId;
        private String carrierId;
        private String controlJobId;
        private String trxUserId;
        private String executorId;
        private String transactionId;
        private Integer maxRecordCount;
        private Long passedTime;
        private SearchCondition searchCondition;
    }

    /**
     * the execute result
     */
    @Getter
    @Setter
    public static class Result {
        private final boolean success;
        private final boolean releaseHold;
        private final Object body;
        private final List<Detail> details = new ArrayList<>();
        private final List<ExtraTask> extraTasks = new ArrayList<>();

        public Result(boolean success, boolean releaseHold) {
            this.success = success;
            this.releaseHold = releaseHold;
            this.body = null;
        }

        public Result(boolean success, boolean releaseHold, Object body) {
            this.success = success;
            this.releaseHold = releaseHold;
            this.body = body;
        }

        public Result(boolean success, Object body) {
            this.success = success;
            this.body = body;
            this.releaseHold = true;
        }

        public Result(boolean success) {
            this.success = success;
            this.releaseHold = true;
            this.body = null;
        }

        public void addDetail(String name, String value) {
            this.details.add(new Detail(name, value));
        }
    }

    /**
     * execute param for executor
     */
    @ToString
    @Getter
    public static class Param {

        private final String taskId;
        private final int indexNo;
        private final Infos.ObjCommon objCommon;
        private final ExecutePhase phase;
        private final EntityType entityType;
        private final ObjectIdentifier entityID;
        private final ObjectIdentifier equipmentID;
        private final ObjectIdentifier controlJobID;
        private final List<Detail> details;

        private Param(PostProcessTask task) {
            this.taskId = task.taskId;
            this.indexNo = task.indexNo;
            this.objCommon = task.objCommon;
            this.phase = task.phase;
            this.entityType = task.definition.getEntityType();
            this.entityID = task.entityID;
            this.equipmentID = task.equipmentID;
            this.controlJobID = task.controlJobID;
            this.details = task.details;
        }

        public String getDetailValue (String name) {
            return this.details.stream()
                    .filter(detail -> CimStringUtils.equals(detail.name, name))
                    .findFirst()
                    .map(Detail::getValue)
                    .orElse("");
        }
    }

    @Getter
    public static class ExtraTask {
        private final String trxId;
        private final EntityType entityType;
        private final ObjectIdentifier entityID;

        private ExtraTask(String trxId, EntityType entityType, ObjectIdentifier entityID) {
            this.trxId = trxId;
            this.entityType = entityType;
            this.entityID = entityID;
        }
    }

    public static ExtraTask extraLotTask(String trxId, ObjectIdentifier lotID) {
        return new ExtraTask(trxId, EntityType.Lot, lotID);
    }

    /**
     * additional information of the post process task
     */
    @Getter
    public static class Detail {
        private final String name;
        private final String value;

        public Detail(String name, String value) {
            this.name = name;
            this.value = value;
        }
    }

    public static PostProcessTask.Result error(PostProcessExecutor executor) {
        final Result result = new Result(false, false);
        result.addDetail("ErrorMessage",
                "PostProcess [" + executor.getClass().getSimpleName() + "] execution error!!!");
        return result;
    }

    public static Result success() {
        return new PostProcessTask.Result(true);
    }

    public static Result success(List<ExtraTask> extraTasks) {
        Result result = new Result(true);
        result.getExtraTasks().addAll(extraTasks);
        return result;
    }

    public static Result success(Object body) {
        return new Result(true, body);
    }

}
