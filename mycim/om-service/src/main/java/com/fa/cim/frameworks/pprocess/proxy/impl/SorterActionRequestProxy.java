package com.fa.cim.frameworks.pprocess.proxy.impl;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.dto.Infos;
import com.fa.cim.frameworks.dto.pp.PostProcessParam;
import com.fa.cim.frameworks.dto.pp.PostProcessTask;
import com.fa.cim.frameworks.pprocess.executor.TaskExecutors;
import com.fa.cim.frameworks.pprocess.manager.PostProcessPlanManager;
import com.fa.cim.frameworks.pprocess.proxy.impl.base.BasePostProcessProxy;
import com.fa.cim.sorter.Info;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class SorterActionRequestProxy extends BasePostProcessProxy<Info.SortJobInfo, Object> {

    @Autowired
    public SorterActionRequestProxy(PostProcessPlanManager planManager) {
        super(planManager);
    }

    @Override
    public List<PostProcessTask> plan(PostProcessParam.PlanTask<Info.SortJobInfo, Object> param) {
        Infos.ObjCommon objCommon = param.getObjCommon();
        PostProcessParam.CreateTask createTask = new PostProcessParam.CreateTask();
        createTask.setControlJobID(param.getArguments().getControlJobID());
        createTask.setEquipmentID(param.getArguments().getEquipmentID());
        createTask.setObjCommon(objCommon);
        return findTaskPlan(objCommon.getTransactionID()).generateTasks(createTask).stream()
                .peek(task -> {
                    if (TaskExecutors.SortActionReq.name().equals(task.getDefinition().getExecutorId())) {
                        ObjectIdentifier sorterJobID = param.getArguments().getSorterJobID();
                        task.getDetails().add(new PostProcessTask.Detail("sorterJobId",
                                ObjectIdentifier.fetchValue(sorterJobID)));
                    }
                })
                .collect(Collectors.toList());
    }
}
