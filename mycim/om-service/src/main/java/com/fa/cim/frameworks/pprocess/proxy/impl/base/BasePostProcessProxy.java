package com.fa.cim.frameworks.pprocess.proxy.impl.base;

import com.fa.cim.frameworks.dto.pp.PostProcessTaskPlan;
import com.fa.cim.frameworks.pprocess.api.definition.PostProcessPlanProxy;
import com.fa.cim.frameworks.pprocess.manager.PostProcessPlanManager;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

@Getter(AccessLevel.PROTECTED)
@Slf4j
public abstract class BasePostProcessProxy<P, R> implements PostProcessPlanProxy<P, R> {

    private final PostProcessPlanManager planManager;

    @Autowired
    public BasePostProcessProxy(PostProcessPlanManager planManager) {
        this.planManager = planManager;
    }

    protected PostProcessTaskPlan findTaskPlan(String transactionId) {
        return planManager.findPostProcessPlan(transactionId);
    }
}
