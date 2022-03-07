package com.fa.cim.frameworks.pprocess.proxy.impl;

import com.fa.cim.common.support.ObjectIdentifier;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@Slf4j
public class SplitLotWithHoldReleasePostProcessProxy extends BasePostProcessProxy<
        Params.SplitLotWithHoldReleaseReqParams,
        Results.SplitLotReqResult> {

    @Autowired
    public SplitLotWithHoldReleasePostProcessProxy(PostProcessPlanManager planManager) {
        super(planManager);
    }

    @Override
    public List<PostProcessTask> plan(PostProcessParam.PlanTask<Params.SplitLotWithHoldReleaseReqParams,
            Results.SplitLotReqResult> param) {
        Infos.ObjCommon objCommon = param.getObjCommon();
        List<ObjectIdentifier> lotIDs = Stream.concat(
                Stream.of(param.getArguments().getParentLotID()),
                Optional.ofNullable(param.getResult())
                        .map(data -> Collections.singletonList(data.getChildLotID()))
                        .orElseGet(Collections::emptyList).stream()
        ).collect(Collectors.toList());
        PostProcessParam.CreateTask createTask = new PostProcessParam.CreateTask();
        createTask.setLotID(lotIDs);
        createTask.setObjCommon(objCommon);
        return findTaskPlan(objCommon.getTransactionID()).generateTasks(createTask);
    }
}
