package com.fa.cim.frameworks.pprocess.proxy.impl;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.pp.PostProcessSource;
import com.fa.cim.frameworks.dto.pp.*;
import com.fa.cim.frameworks.pprocess.api.definition.PostProcessPlanProxy;
import com.fa.cim.frameworks.pprocess.manager.PostProcessPlanManager;
import com.fa.cim.frameworks.pprocess.proxy.impl.base.BasePostProcessProxy;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * the default implements for the {@link PostProcessPlanProxy}, it is recommanded for the other implements extends from this
 * class
 *
 * @author Yuri
 */
@Slf4j
@Getter(AccessLevel.PROTECTED)
@Component("defaultPostProcessProxy")
public class DefaultPostProcessProxy extends BasePostProcessProxy<Object, Object> {

    @Autowired
    public DefaultPostProcessProxy(PostProcessPlanManager planManager) {
        super(planManager);
    }

    @Override
    public List<PostProcessTask> plan(PostProcessParam.PlanTask<Object, Object> param) {
        Infos.ObjCommon objCommon = param.getObjCommon();
        PostProcessTaskPlan postProcessPlan = findTaskPlan(objCommon.getTransactionID());
        PostProcessSource args = param.getArguments() instanceof PostProcessSource ?
                (PostProcessSource) param.getArguments() :
                PostProcessSource.emptySource();
        PostProcessSource result = param.getResult() instanceof PostProcessSource ?
                (PostProcessSource) param.getResult() :
                PostProcessSource.emptySource();
        PostProcessParam.CreateTask createTask = new PostProcessParam.CreateTask();
        createTask.setObjCommon(objCommon);
        createTask.setEquipmentID(ObjectIdentifier.isNotEmpty(result.equipmentID()) ?
                result.equipmentID() : args.equipmentID());
        createTask.setControlJobID(ObjectIdentifier.isNotEmpty(result.controlJobID()) ?
                result.controlJobID() : args.controlJobID());
        createTask.setLotID(Stream.concat(args.lotIDs().stream(), result.lotIDs().stream())
                .distinct()
                .collect(Collectors.toList()));
        createTask.setDurableID(Stream.concat(args.durableIDs().stream(), result.durableIDs().stream())
                .distinct()
                .collect(Collectors.toList()));
        return postProcessPlan.generateTasks(createTask);
    }

}
