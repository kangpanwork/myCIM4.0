package com.fa.cim.frameworks.pprocess.proxy.impl;

import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.utils.CimArrayUtils;
import com.fa.cim.dto.Infos;
import com.fa.cim.frameworks.dto.pp.PostProcessParam;
import com.fa.cim.frameworks.dto.pp.PostProcessTask;
import com.fa.cim.frameworks.pprocess.executor.TaskExecutors;
import com.fa.cim.frameworks.pprocess.manager.PostProcessPlanManager;
import com.fa.cim.frameworks.pprocess.proxy.impl.base.BaseMoveOutRequestProxy;
import com.fa.cim.frameworks.pprocess.proxy.impl.base.BasePostProcessProxy;
import com.fa.cim.method.ILotMethod;
import com.fa.cim.sorter.Info;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class SorterActionReportProxy extends BaseMoveOutRequestProxy<Info.SortJobInfo, List<ObjectIdentifier>> {

    public SorterActionReportProxy(PostProcessPlanManager planManager, ILotMethod lotMethod) {
        super(planManager, lotMethod);
    }

    @Override
    public List<PostProcessTask> plan(PostProcessParam.PlanTask<Info.SortJobInfo, List<ObjectIdentifier>> param) {
        Infos.ObjCommon objCommon = param.getObjCommon();
        ObjectIdentifier controlJobID = param.getArguments().getControlJobID();
        List<PostProcessTask> plan ;
        List<PostProcessTask> tempPlan = new ArrayList<>();

        if (!StringUtils.isEmpty(controlJobID)) {
            objCommon.setTransactionID(TransactionIDEnum.OPERATION_COMP_WITH_DATA_REQ.getValue());
            plan = super.plan(param);
            for (PostProcessTask postProcessTask : plan) {
                if (!TaskExecutors.CollectedDataAction.name().equals(postProcessTask.getDefinition().getExecutorId())) {
                    tempPlan.add(postProcessTask);
                }
            }
            objCommon.setTransactionID(TransactionIDEnum.SORT_ACTION_RPT.getValue());
        }
        PostProcessParam.CreateTask createTask = new PostProcessParam.CreateTask();
        createTask.setControlJobID(controlJobID);
        createTask.setEquipmentID(param.getArguments().getEquipmentID());
        createTask.setObjCommon(objCommon);
        List<PostProcessTask> sortJobPlan = findTaskPlan(objCommon.getTransactionID()).generateTasks(createTask).stream()
                .peek(task -> {
                    if (TaskExecutors.SortActionRpt.name().equals(task.getDefinition().getExecutorId())) {
                        ObjectIdentifier sorterJobID = param.getArguments().getSorterJobID();
                        task.getDetails().add(new PostProcessTask.Detail("sorterJobId",
                                ObjectIdentifier.fetchValue(sorterJobID)));
                    }
                })
                .collect(Collectors.toList());
        if (!StringUtils.isEmpty(controlJobID)) {
            sortJobPlan.addAll(tempPlan);
        }
        return sortJobPlan;
    }

    @Override
    protected List<ObjectIdentifier> normalLots(List<ObjectIdentifier> result) {
        if (CimArrayUtils.getSize(result) == 0) {
            return null;
        } else {
            return result;
        }
    }

    @Override
    protected List<ObjectIdentifier> holdReleaseLots(List<ObjectIdentifier> result) {
        return null;
    }


    @Override
    protected ObjectIdentifier controlJob(Info.SortJobInfo param) {
        return null;
    }

    @Override
    protected ObjectIdentifier equipment(Info.SortJobInfo param) {
        return null;
    }

}
