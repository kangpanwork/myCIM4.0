package com.fa.cim.frameworks.pprocess.proxy.impl;

import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.utils.CimStringUtils;
import com.fa.cim.common.utils.ThreadContextHolder;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;
import com.fa.cim.frameworks.dto.pp.PostProcessParam;
import com.fa.cim.frameworks.dto.pp.PostProcessTask;
import com.fa.cim.frameworks.pprocess.api.definition.PostProcessExecutor;
import com.fa.cim.frameworks.pprocess.executor.TaskExecutors;
import com.fa.cim.frameworks.pprocess.manager.PostProcessPlanManager;
import com.fa.cim.frameworks.pprocess.proxy.impl.base.BaseMoveOutRequestProxy;
import com.fa.cim.method.ILotMethod;
import com.fa.cim.newcore.impl.bo.env.StandardProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


@Component
@Slf4j
public class PartialMoveOutRequestProxy extends BaseMoveOutRequestProxy<Params.PartialMoveOutReqParams,
        Results.PartialMoveOutReqResult> {

    @Autowired
    public PartialMoveOutRequestProxy(PostProcessPlanManager planManager, ILotMethod lotMethod) {
        super(planManager, lotMethod);
    }

    @Override
    public List<PostProcessTask> plan(PostProcessParam.PlanTask<Params.PartialMoveOutReqParams,
            Results.PartialMoveOutReqResult> param) {
        List<PostProcessTask> tasks = super.plan(param);
        Infos.ObjCommon objCommon = param.getObjCommon();
        String transactionID = objCommon.getTransactionID();
        Map<String, List<Infos.PartialOpeCompLot>> grouped = Optional.ofNullable(param.getResult())
                .map(Results.PartialMoveOutReqResult::getPartialOpeCompLotList)
                .orElseGet(Collections::emptyList).stream()
                .collect(Collectors.groupingBy(Infos.PartialOpeCompLot::getActionCode));
        PostProcessParam.CreateTask createTask = createTaskParam(objCommon, param.getArguments());

        createTask.setLotID(fetchLotIDs(BizConstant.SP_PARTIALOPECOMP_ACTION_OPECOMPWITHHOLD, grouped));
        String moveHoldTrxId = transactionID + StandardProperties.OM_PP_PROCESS_MOVE_MODE.getValue() + "H";
        tasks.addAll(findTaskPlan(moveHoldTrxId).generateTasks(createTask));

        createTask.setLotID(fetchLotIDs(BizConstant.SP_PARTIALOPECOMP_ACTION_OPESTARTCANCEL, grouped));
        String moveCancelTrxId = transactionID + "C";
        tasks.addAll(findTaskPlan(moveCancelTrxId).generateTasks(createTask));

        createTask.setLotID(fetchLotIDs(BizConstant.SP_PARTIALOPECOMP_ACTION_OPESTARTCANCELWITHHOLD, grouped));
        String moveCancelHoldTrxId = transactionID + "CH";
        tasks.addAll(findTaskPlan(moveCancelHoldTrxId).generateTasks(createTask));
        return tasks;
    }

    private List<ObjectIdentifier> fetchLotIDs(String actionCode, Map<String, List<Infos.PartialOpeCompLot>> grouped) {
        return grouped.getOrDefault(actionCode, Collections.emptyList())
                .stream().map(Infos.PartialOpeCompLot::getLotID)
                .collect(Collectors.toList());
    }

    @Override
    public List<PostProcessTask> modify(List<PostProcessTask> tasks) {
        return super.modify(tasks).stream().peek(task -> {
            List<PostProcessTask.Detail> details = task.getDetails();
            PostProcessExecutor executor = task.getDefinition().getExecutor();
            // RunWaferInfoUpdate
            //---------------------------------------
            // if executor is RunWaferInfoUpdate
            //---------------------------------------
            if (TaskExecutors.RunWaferInfoUpdate.getExecutorType().equals(executor.getClass())) {
                final int updateForEqpAttrIntValue = StandardProperties.OM_PP_UPDATE_FOR_EQP_ATTR.getIntValue();
                if (log.isDebugEnabled()) log.debug("Env runWaferUpdateByPostProc = {}", updateForEqpAttrIntValue);
                if (updateForEqpAttrIntValue == 1) {
                    if (log.isDebugEnabled())
                        log.debug("Executor: {}", TaskExecutors.RunWaferInfoUpdate.getExecutorType());
                    // get thread data
                    final String strRunWaferCnt = ThreadContextHolder.getThreadSpecificDataString(
                            BizConstant.SP_THREADSPECIFICDATA_KEY_RUNWAFERCNT);
                    // add RunWaferCnt detail info
                    details.add(new PostProcessTask.Detail(
                            BizConstant.SP_THREADSPECIFICDATA_KEY_RUNWAFERCNT,
                            CimStringUtils.isNotEmpty(strRunWaferCnt) ? strRunWaferCnt : BizConstant.VALUE_ZERO));

                    // add OpeStartCnt detail info
                    details.add(new PostProcessTask.Detail(
                            BizConstant.SP_THREADSPECIFICDATA_KEY_OPESTARTCNT,
                            BizConstant.VALUE_ONE));
                }
            }
        }).collect(Collectors.toList());
    }

    @Override
    protected List<ObjectIdentifier> normalLots(Results.PartialMoveOutReqResult result) {
        return result.getPartialOpeCompLotList().stream()
                .filter(data -> BizConstant.SP_PARTIALOPECOMP_ACTION_OPECOMP.equals(data.getActionCode()))
                .map(Infos.PartialOpeCompLot::getLotID)
                .collect(Collectors.toList());
    }

    @Override
    protected List<ObjectIdentifier> holdReleaseLots(Results.PartialMoveOutReqResult result) {
        return result.getHoldReleasedLotIDs();
    }

    @Override
    protected ObjectIdentifier controlJob(Params.PartialMoveOutReqParams param) {
        return param.getControlJobID();
    }

    @Override
    protected ObjectIdentifier equipment(Params.PartialMoveOutReqParams param) {
        return param.getEquipmentID();
    }
}
