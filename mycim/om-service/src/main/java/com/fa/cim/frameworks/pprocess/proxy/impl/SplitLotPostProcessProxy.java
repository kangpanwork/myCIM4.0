package com.fa.cim.frameworks.pprocess.proxy.impl;

import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;
import com.fa.cim.frameworks.dto.pp.PostProcessParam;
import com.fa.cim.frameworks.dto.pp.PostProcessTask;
import com.fa.cim.frameworks.pprocess.manager.PostProcessPlanManager;
import com.fa.cim.frameworks.pprocess.proxy.impl.base.BasePostProcessProxy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
@Slf4j
public class SplitLotPostProcessProxy extends BasePostProcessProxy<Params.SplitLotReqParams, Results.SplitLotReqResult> {

    @Autowired
    public SplitLotPostProcessProxy(PostProcessPlanManager planManager) {
        super(planManager);
    }

    @Override
    public List<PostProcessTask> plan(PostProcessParam.PlanTask<Params.SplitLotReqParams, Results.SplitLotReqResult> param) {
        Infos.ObjCommon objCommon = param.getObjCommon();
        PostProcessParam.CreateTask createTask = new PostProcessParam.CreateTask();
        createTask.setLotID(Collections.singletonList(param.getArguments().getParentLotID()));
        createTask.setObjCommon(objCommon);
        List<PostProcessTask> retVal = findTaskPlan(objCommon.getTransactionID()).generateTasks(createTask);
        retVal.addAll(Optional.ofNullable(param.getResult())
                .map(Results.SplitLotReqResult::getChildLotID)
                .map(childLotID -> {
                    createTask.setLotID(Collections.singletonList(childLotID));
                    return findTaskPlan(objCommon.getTransactionID() + "C").generateTasks(createTask);
                }).orElseGet(Collections::emptyList));
        return retVal;
    }
}
