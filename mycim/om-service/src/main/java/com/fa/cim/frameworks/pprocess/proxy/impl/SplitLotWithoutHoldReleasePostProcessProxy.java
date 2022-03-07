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
public class SplitLotWithoutHoldReleasePostProcessProxy extends BasePostProcessProxy<
        Params.SplitLotWithoutHoldReleaseReqParams,
        Results.SplitLotReqResult> {

    @Autowired
    public SplitLotWithoutHoldReleasePostProcessProxy(PostProcessPlanManager planManager) {
        super(planManager);
    }

    @Override
    public List<PostProcessTask> plan(PostProcessParam.PlanTask<Params.SplitLotWithoutHoldReleaseReqParams,
            Results.SplitLotReqResult> param) {
        Infos.ObjCommon objCommon = param.getObjCommon();
        return Optional.ofNullable(param.getResult())
                .map(data -> Collections.singletonList(data.getChildLotID()))
                .map(lotIDs -> {
                    PostProcessParam.CreateTask createTask = new PostProcessParam.CreateTask();
                    createTask.setObjCommon(objCommon);
                    createTask.setLotID(lotIDs);
                    return findTaskPlan(objCommon.getTransactionID() + "C").generateTasks(createTask);
                })
                .orElseGet(Collections::emptyList);
    }
}
