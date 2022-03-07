package com.fa.cim.frameworks.pprocess.manager.impl;

import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.utils.CimBooleanUtils;
import com.fa.cim.common.utils.SpringContextUtil;
import com.fa.cim.dto.pp.EntityType;
import com.fa.cim.entity.nonruntime.postprocess.pprocess.CimPostProcessPatternDefinitionDO;
import com.fa.cim.frameworks.dto.pp.PostProcessTaskPlan;
import com.fa.cim.frameworks.dto.pp.mode.CommitMode;
import com.fa.cim.frameworks.dto.pp.mode.ErrorMode;
import com.fa.cim.frameworks.dto.pp.mode.JoinMode;
import com.fa.cim.frameworks.pprocess.api.annotations.PostProcessTaskHandler;
import com.fa.cim.frameworks.pprocess.api.definition.PostProcessExecutor;
import com.fa.cim.frameworks.pprocess.executor.TaskExecutors;
import com.fa.cim.frameworks.pprocess.manager.PostProcessPlanManager;
import com.fa.cim.newcore.impl.bo.env.StandardProperties;
import com.fa.cim.repository.standard.pprocess.PostProcessPatternDefinitionDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Slf4j
@Component
public class PostProcessPlanManagerImpl implements PostProcessPlanManager {

    private final PostProcessPatternDefinitionDao patternDefinitionDao;
    private final Map<String, PostProcessTaskPlan> planLocalCache = new ConcurrentHashMap<>(128);

    private static final String QUERY_MODE_LOCAL = "local";

    @Autowired
    public PostProcessPlanManagerImpl(PostProcessPatternDefinitionDao patternDefinitionDao) {
        this.patternDefinitionDao = patternDefinitionDao;
    }

    @Override
    public PostProcessTaskPlan findPostProcessPlan(String transactionId) {
        // get the plan from local cahce, if not found query it from database
        return Optional.ofNullable(planLocalCache.get(transactionId)).orElseGet(() -> {
            PostProcessTaskPlan plan = newPostProcessTaskPlan(transactionId);
            String queryMode = StandardProperties.OM_PP_QUERY_MODE.getDefaultValue();
            if(log.isTraceEnabled()) {
                log.trace(">>>>>> OM_PP_QUERY_MODE == {}", queryMode);
            }
            if (QUERY_MODE_LOCAL.equals(queryMode)) {
                planLocalCache.put(transactionId, plan);
            }
            return plan;
        });
    }

    /**
     * create a new post process plan by a direct query from the database
     *
     * @param transactionId transaction id
     * @return post process plan
     */
    private PostProcessTaskPlan newPostProcessTaskPlan(String transactionId) {
        List<CimPostProcessPatternDefinitionDO> dataList = patternDefinitionDao.findAllByTransactionId(transactionId);
        String patternId = dataList.stream().map(CimPostProcessPatternDefinitionDO::getPatternId)
                .findAny().orElseThrow(() -> new ServiceException("Not Found Pattern ID - shouldn't have reached here"));
        if(log.isTraceEnabled()) {
            log.trace(">>>>> patterId == {}", patternId);
        }
        AtomicReference<PostProcessTaskPlan.Definition> tmpHolder = new AtomicReference<>();
        List<PostProcessTaskPlan.Definition> definitions = dataList.stream().map(data -> {
            PostProcessTaskPlan.Definition definition = new PostProcessTaskPlan.Definition();

            if(log.isTraceEnabled()) {
                log.trace(">>>>> EntityType == {}", data.getEntityType());
            }
            definition.setEntityType(EntityType.valueOf(data.getEntityType()));

            if(log.isTraceEnabled()) {
                log.trace(">>>>> JoinMode == {}", data.getJoinMode());
            }
            definition.setJoinMode(JoinMode.valueOf(data.getJoinMode()));

            if(log.isTraceEnabled()) {
                log.trace(">>>>> ErrorMode == {}", data.getErrorModeName());
            }
            definition.setErrorMode(ErrorMode.valueOf(data.getErrorModeName()));

            if(log.isTraceEnabled()) {
                log.trace(">>>>> CommitMode == {}", data.getCommitModeName());
            }
            definition.setCommitMode(CommitMode.valueOf(data.getCommitModeName()));

            if(log.isTraceEnabled()) {
                log.trace(">>>>> Executor == {}", data.getExecutorId());
            }
            TaskExecutors taskExecutor = TaskExecutors.valueOf(data.getExecutorId());
            Class<? extends PostProcessExecutor> executorType = taskExecutor.getExecutorType();
            definition.setExecutorId(taskExecutor.name());
            definition.setNextOperationRequired(executorType.getAnnotation(PostProcessTaskHandler.class).isNextOperationRequired());
            definition.setExecutor(SpringContextUtil.getSingletonBean(executorType));

            EntityType preEntityType = Optional.ofNullable(tmpHolder.get()).map(PostProcessTaskPlan.Definition::getEntityType)
                    .orElseGet(definition::getEntityType);
            boolean isSameEntityType = preEntityType == definition.getEntityType();
            if(log.isTraceEnabled()) {
                log.trace(">>>>> isSameEntityType == {}", isSameEntityType);
            }
            boolean isChained = isSameEntityType && CimBooleanUtils.isTrue(data.getChainedFlag());
            if(log.isTraceEnabled()) {
                log.trace(">>>>> chainedFlag == {}", isChained);
            }
            definition.setChained(isChained);

            tmpHolder.set(definition);
            return definition;
        }).collect(Collectors.toList());

        PostProcessTaskPlan plan = new PostProcessTaskPlan(patternId, transactionId);
        plan.getDefinitions().addAll(definitions);
        return plan;
    }


    /**
     * sync the pattern definition from the database to local cache
     */
    @Scheduled(fixedRateString = "#{${mycim.sys.pp.def.sync.interval:60} * 60000}")
    public void refreshDefinitions() {
        StandardProperties varible = StandardProperties.OM_PP_QUERY_MODE;
        String queryMode = varible.isReady() ? varible.getValue() : varible.getDefaultValue();
        if(log.isTraceEnabled()) log.trace("refreshDefinitions::OM_PP_QUERY_MODE == {}", queryMode);
        if (QUERY_MODE_LOCAL.equals(queryMode)) {
            if(log.isDebugEnabled()) {
                log.debug("post process definitions are sync required: begin to sync ....");
            }
            Set<String> transactionIds = this.planLocalCache.keySet();
            transactionIds.forEach(transactionId -> this.planLocalCache.put(transactionId, findPostProcessPlan(transactionId)));
            if(log.isDebugEnabled()) {
                log.debug("post process definitions are sync required: sync finished ....");
            }
        }
    }
}
