package com.fa.cim.frameworks.pprocess.proxy.impl.base;

import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.utils.CimStringUtils;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Outputs;
import com.fa.cim.frameworks.dto.pp.PostProcessParam;
import com.fa.cim.frameworks.dto.pp.PostProcessTask;
import com.fa.cim.frameworks.pprocess.api.definition.PostProcessExecutor;
import com.fa.cim.frameworks.pprocess.executor.TaskExecutors;
import com.fa.cim.frameworks.pprocess.manager.PostProcessPlanManager;
import com.fa.cim.method.ILotMethod;
import com.fa.cim.newcore.impl.bo.env.StandardProperties;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
public abstract class BaseMoveOutRequestProxy<P, R> extends BasePostProcessProxy<P, R>{

    private final ILotMethod lotMethod;

    public BaseMoveOutRequestProxy(PostProcessPlanManager planManager, ILotMethod lotMethod) {
        super(planManager);
        this.lotMethod = lotMethod;
    }

    @Override
    public List<PostProcessTask> plan(PostProcessParam.PlanTask<P, R> param) {
        Infos.ObjCommon objCommon = param.getObjCommon();
        String transactionId = objCommon.getTransactionID() + StandardProperties.OM_PP_PROCESS_MOVE_MODE.getValue();
        if (log.isTraceEnabled()) {
            log.trace("transactionId == {}", transactionId);
        }
        P arguments = param.getArguments();
        PostProcessParam.CreateTask createTask = createTaskParam(objCommon, arguments);
        createTask.setLotID(Optional.ofNullable(param.getResult())
                .map(this::normalLots)
                .orElseGet(Collections::emptyList));
        List<PostProcessTask> tasks = findTaskPlan(transactionId).generateTasks(createTask);
        createTask.setLotID(Optional.ofNullable(param.getResult())
                .map(this::holdReleaseLots)
                .orElseGet(Collections::emptyList));
        List<PostProcessTask> holdTasks = findTaskPlan(TransactionIDEnum.HOLD_LOT_RELEASE_REQ.getValue()).generateTasks(createTask);
        tasks.addAll(holdTasks);
        return tasks;
    }

    protected PostProcessParam.CreateTask createTaskParam(Infos.ObjCommon objCommon, P arguments) {
        PostProcessParam.CreateTask createTask = new PostProcessParam.CreateTask();
        createTask.setEquipmentID(equipment(arguments));
        createTask.setControlJobID(controlJob(arguments));
        createTask.setObjCommon(objCommon);
        return createTask;
    }

    @Override
    public List<PostProcessTask> modify(List<PostProcessTask> tasks) {
        tasks.forEach(postProcessTask -> {
            final PostProcessExecutor executor = postProcessTask.getDefinition().getExecutor();
            if (TaskExecutors.QTime.getExecutorType().equals(executor.getClass())) {
                int postProcForLotFlag = StandardProperties.OM_PP_FOR_LOT_MOVEOUT_FLAG.getIntValue();
                if (1 == postProcForLotFlag) {
                    Outputs.ObjLotPostProcessPreviousBranchAndReturnInfoGetDRout branchAndReturnInfoGetDRout;
                    //------------------------------------------------------------------
                    // call lotPostProcessPreviousBranchAndReturnInfoGetDR()...
                    //------------------------------------------------------------------
                    branchAndReturnInfoGetDRout = lotMethod.lotPostProcessPreviousBranchAndReturnInfoGetDR(
                            postProcessTask.getObjCommon(),
                            postProcessTask.getEntityID());

                    // branch route info
                    if (ObjectIdentifier.isNotEmpty(branchAndReturnInfoGetDRout.getBranchRouteID())) {
                        String previousBranchInfo = CimStringUtils.join(BizConstant.DOT,
                                ObjectIdentifier.fetchValue(branchAndReturnInfoGetDRout.getBranchRouteID()),
                                branchAndReturnInfoGetDRout.getBranchOperationNumber());
                        postProcessTask.getDetails().add(new PostProcessTask.Detail(
                                BizConstant.SP_THREADSPECIFICDATA_KEY_PREVIOUSBRANCHINFO,
                                previousBranchInfo));
                    }

                    // return route info
                    if (ObjectIdentifier.isNotEmpty(branchAndReturnInfoGetDRout.getReturnRouteID())) {
                        String previousReturnInfo = CimStringUtils.join(BizConstant.DOT,
                                ObjectIdentifier.fetchValue(branchAndReturnInfoGetDRout.getReturnRouteID()),
                                branchAndReturnInfoGetDRout.getBranchOperationNumber());
                        postProcessTask.getDetails().add(new PostProcessTask.Detail(
                                BizConstant.SP_THREADSPECIFICDATA_KEY_PREVIOUSRETURNINFO,
                                previousReturnInfo));
                    }

                    // rework out key
                    if (CimStringUtils.isNotEmpty(branchAndReturnInfoGetDRout.getReworkOutKey())) {
                        postProcessTask.getDetails().add(new PostProcessTask.Detail(
                                BizConstant.SP_THREADSPECIFICDATA_KEY_PREVIOUSREWORKOUTKEY,
                                branchAndReturnInfoGetDRout.getReworkOutKey()));
                    }
                }
            }
        });
        return tasks;
    }

    protected abstract List<ObjectIdentifier> normalLots(R result);

    protected abstract List<ObjectIdentifier> holdReleaseLots(R result);

    protected abstract ObjectIdentifier controlJob(P param);

    protected abstract ObjectIdentifier equipment(P param);

}
