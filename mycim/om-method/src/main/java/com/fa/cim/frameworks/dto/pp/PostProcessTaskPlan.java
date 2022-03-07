package com.fa.cim.frameworks.dto.pp;

import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.utils.CimArrayUtils;
import com.fa.cim.common.utils.SnowflakeIDWorker;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.pp.EntityType;
import com.fa.cim.frameworks.dto.pp.mode.ExecutePhase;
import com.fa.cim.frameworks.dto.pp.mode.JoinMode;
import com.fa.cim.frameworks.dto.pp.mode.CommitMode;
import com.fa.cim.frameworks.dto.pp.mode.ErrorMode;
import com.fa.cim.frameworks.pprocess.api.definition.PostProcessExecutor;
import lombok.Getter;
import lombok.Setter;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * the post process execution plan
 *
 * @author Yuri
 */
@Getter
public class PostProcessTaskPlan {

    private final String patternId;
    private final String transactionId;
    private final List<Definition> definitions = new LinkedList<>();

    public PostProcessTaskPlan(String patternId, String transactionId) {
        this.patternId = patternId;
        this.transactionId = transactionId;
    }

    private static final ThreadLocal<String> LOCAL_TASK_ID = new ThreadLocal<>();

    public static void generateTaskId(String userId) {
        LOCAL_TASK_ID.set(String.format("%s++%s", SnowflakeIDWorker.getInstance().nextID(), userId));
    }

    public static void clearTaskId() {
        LOCAL_TASK_ID.remove();
    }

    public List<PostProcessTask> generateTasks(PostProcessParam.CreateTask param) {
        Infos.ObjCommon objCommon = param.getObjCommon();
        String taskId = LOCAL_TASK_ID.get();

        // split the definitions into sections, which allow the creating of the task is by lot not by definition,
        // thefore, the tasks with the same entity ID should be executing in a continuous order in one section
        List<List<PostProcessTaskPlan.Definition>> sectioned = CimArrayUtils.split(this.getDefinitions(),
                definition -> definition.getEntityType() == EntityType.Equipment);

        AtomicBoolean chainToMain = new AtomicBoolean(true);
        AtomicInteger indexNo = new AtomicInteger(0);

        List<PostProcessTask> tasks = sectioned.stream()
                .flatMap(definitions -> {
                    // find the entity type of current section, if the current section is for Equipment, the list should be a
                    // singleton list
                    EntityType entityType = definitions.stream()
                            .map(PostProcessTaskPlan.Definition::getEntityType)
                            .distinct().findAny()
                            .orElseThrow(() -> new ServiceException("Shouldn't have reached here"));

                    return getEntityIDs(param, entityType).stream()
                            .flatMap(entityID -> definitions.stream()
                                    .map(taskDefinition -> {
                                        PostProcessTask task = new PostProcessTask();
                                        task.setTaskId(taskId);
                                        task.setObjCommon(objCommon);
                                        task.setDefinition(taskDefinition);
                                        task.setEntityID(entityID);
                                        task.setEquipmentID(param.getEquipmentID());
                                        task.setControlJobID(param.getControlJobID());
                                        return task;
                                    }));
                })
                .peek(task -> {
                    task.setPhase(ExecutePhase.getPhase(task, chainToMain));
                    task.setIndexNo(indexNo.getAndIncrement());
                })
                .collect(Collectors.toList());

        return tasks;
    }

    /**
     * get the entity ID based on the {@link EntityType}
     *
     * @param param plan parameter
     * @param entityType entity type
     * @return a list of entity ID
     */
    private List<ObjectIdentifier> getEntityIDs(PostProcessParam.CreateTask param, EntityType entityType) {
        List<ObjectIdentifier> entityIDs;
        switch (entityType) {
            case Lot:
                entityIDs = param.getLotID();
                break;
            case Durable:
                entityIDs = param.getDurableID();
                break;
            default:
                entityIDs = Collections.singletonList(ObjectIdentifier.emptyIdentifier());
                break;
        }
        return entityIDs;
    }

    /**
     * the task definition
     */
    @Getter
    @Setter
    public static class Definition {
        private String executorId;
        private PostProcessExecutor executor;
        private boolean isNextOperationRequired;
        private boolean chained;
        private JoinMode joinMode;
        private EntityType entityType;
        private ErrorMode errorMode;
        private CommitMode commitMode;
    }
}
