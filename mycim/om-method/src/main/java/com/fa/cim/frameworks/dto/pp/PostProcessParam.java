package com.fa.cim.frameworks.dto.pp;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.pp.Additional;
import com.fa.cim.dto.pp.AdditionalKeys;
import com.fa.cim.frameworks.dto.pp.mode.ExecutePhase;
import com.fa.cim.frameworks.dto.pp.mode.ModifyAction;
import com.fa.cim.frameworks.pprocess.api.definition.PostProcessPlanProxy;
import com.fa.cim.newcore.dto.product.DispatchReadinessState;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * the post process params
 *
 * @author Yuri
 */
@Getter
@Setter
public abstract class PostProcessParam {

    private String taskId;

    @Getter
    @Setter
    @ToString
    public static class PlanTask<P, R> {
        private final Infos.ObjCommon objCommon;
        private final P arguments;
        private final R result;

        public PlanTask(Infos.ObjCommon objCommon, P arguments, R result) {
            this.objCommon = objCommon;
            this.arguments = arguments;
            this.result = result;
        }
    }

    @Getter
    @Setter
    public static class CreateTask {
        private Infos.ObjCommon objCommon;
        private List<ObjectIdentifier> lotID = Collections.emptyList();
        private List<ObjectIdentifier> durableID = Collections.emptyList();
        private ObjectIdentifier equipmentID = ObjectIdentifier.emptyIdentifier();
        private ObjectIdentifier controlJobID = ObjectIdentifier.emptyIdentifier();

        public CreateTask duplicate() {
            CreateTask createTask = new CreateTask();
            createTask.setObjCommon(objCommon);
            createTask.setLotID(new ArrayList<>(lotID));
            createTask.setControlJobID(controlJobID.copy());
            createTask.setEquipmentID(equipmentID.copy());
            createTask.setDurableID(new ArrayList<>(durableID));
            return createTask;
        }
    }

    @Getter
    @Setter
    public static class ExecuteJoinTasks extends PostProcessParam {
        private String transactionId;
    }

    @Getter
    @Setter
    public static class Execute extends PostProcessParam {
        private Infos.ObjCommon objCommon;
        private List<PostProcessTask> tasks;
        private boolean mainSuccessful;
        private DispatchReadinessState nextStatus;
        private ExecutePhase phase;
    }

    /**
     * the param for register tasks
     */
    @Getter
    @Setter
    public static class Register extends PostProcessParam {
        private boolean mainSuccess = true;
        private List<PostProcessTask> postTasks;
    }

    /**
     * the param for execute tasks
     */
    @Getter
    @Setter
    public static class ExecutePostTasks extends PostProcessParam {
        private String taskId;
    }

    /**
     * the param for modify tasks
     */
    @Getter
    @Setter
    public static class Modify extends PostProcessParam {
        private String taskId;
        private ModifyAction modifyAction;
    }

}
