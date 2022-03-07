package com.fa.cim.frameworks.pprocess.proxy.impl;

import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;
import com.fa.cim.frameworks.dto.pp.PostProcessParam;
import com.fa.cim.frameworks.dto.pp.PostProcessTask;
import com.fa.cim.frameworks.pprocess.manager.PostProcessPlanManager;
import com.fa.cim.frameworks.pprocess.manager.PostProcessTraceManager;
import com.fa.cim.frameworks.pprocess.manager.PostProcessTransactionManager;
import com.fa.cim.frameworks.pprocess.proxy.impl.base.BasePostProcessProxy;
import com.fa.cim.method.IPostProcesssTaskMethod;
import com.fa.cim.newcore.impl.factory.GenericCoreFactory;
import com.fa.cim.service.pprocess.PostProcessHoldService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
@Slf4j
public class PartialReworkWithoutHoldReleasePostProcessProxy extends BasePostProcessProxy<
        Params.PartialReworkWithoutHoldReleaseReqParams,
        Results.PartialReworkReqResult> {


    public PartialReworkWithoutHoldReleasePostProcessProxy(PostProcessPlanManager planManager) {
        super(planManager);
    }

    @Override
    public List<PostProcessTask> plan(PostProcessParam.PlanTask<
            Params.PartialReworkWithoutHoldReleaseReqParams,
            Results.PartialReworkReqResult> param) {
        Params.PartialReworkWithoutHoldReleaseReqParams reworkReqParams = param.getArguments();
        TransactionIDEnum postProcTxID;
        boolean dynamicRouteFlag = reworkReqParams.getPartialReworkReq().getBDynamicRoute();
        boolean forceReworkFlag = reworkReqParams.getPartialReworkReq().getBForceRework();
        if (forceReworkFlag) {
            postProcTxID = dynamicRouteFlag ?
                    TransactionIDEnum.PARTIAL_REWORK_WITHOUT_HOLD_RELEASE_REQ_SPECIAL_ENGINEERING_POST_PROC :
                    TransactionIDEnum.PARTIAL_REWORK_WITHOUT_HOLD_RELEASE_REQ_SPECIAL_POST_PROC;
        } else {
            postProcTxID = dynamicRouteFlag ?
                    TransactionIDEnum.PARTIAL_REWORK_WITHOUT_HOLD_RELEASE_REQ_ENGINEERING_POST_PROC :
                    TransactionIDEnum.PARTIAL_REWORK_WITHOUT_HOLD_RELEASE_REQ_POST_PROC;
        }

        return Optional.ofNullable(param.getResult())
                .map(result -> {
                    PostProcessParam.CreateTask createTask = new PostProcessParam.CreateTask();
                    createTask.setObjCommon(param.getObjCommon());
                    createTask.setLotID(Collections.singletonList(result.getCreatedLotID()));
                    return findTaskPlan(postProcTxID.getValue()).generateTasks(createTask);
                })
                .orElseGet(Collections::emptyList);
    }
}
