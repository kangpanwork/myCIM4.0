package com.fa.cim.frameworks.pprocess.proxy.impl;

import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.utils.CimBooleanUtils;
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
public class PartialReworkWithHoldReleasePostProcessProxy extends BasePostProcessProxy<
        Params.PartialReworkWithHoldReleaseReqParams,
        Results.PartialReworkReqResult> {

    @Autowired
    public PartialReworkWithHoldReleasePostProcessProxy(PostProcessPlanManager planManager) {
        super(planManager);
    }

    @Override
    public List<PostProcessTask> plan(PostProcessParam.PlanTask<Params.PartialReworkWithHoldReleaseReqParams,
            Results.PartialReworkReqResult> param) {
        Params.PartialReworkWithHoldReleaseReqParams reworkReqParams = param.getArguments();
        TransactionIDEnum partialReworkWithHoldReleaseReq;
        TransactionIDEnum partialReworkWithHoldReleaseReqPostProc;
        boolean forceReworkFlag = reworkReqParams.getPartialReworkReq().getBForceRework();
        boolean dynamicRouteFlag = reworkReqParams.getPartialReworkReq().getBDynamicRoute();
        if (CimBooleanUtils.isTrue(forceReworkFlag)) {
            partialReworkWithHoldReleaseReq = CimBooleanUtils.isTrue(dynamicRouteFlag) ?
                    TransactionIDEnum.PARTIAL_REWORK_WITH_HOLD_RELEASE_REQ_ONE :
                    TransactionIDEnum.PARTIAL_REWORK_WITH_HOLD_RELEASE_REQ_TWO;

            partialReworkWithHoldReleaseReqPostProc = CimBooleanUtils.isTrue(dynamicRouteFlag) ?
                    TransactionIDEnum.PARTIAL_REWORK_WITH_HOLD_RELEASE_REQ_POST_PROC_ONE :
                    TransactionIDEnum.PARTIAL_REWORK_WITH_HOLD_RELEASE_REQ_POST_PROC_TWO;
        } else {
            partialReworkWithHoldReleaseReq = CimBooleanUtils.isTrue(dynamicRouteFlag) ?
                    TransactionIDEnum.PARTIAL_REWORK_WITH_HOLD_RELEASE_REQ_THREE :
                    TransactionIDEnum.PARTIAL_REWORK_WITH_HOLD_RELEASE_REQ_FOUR;

            partialReworkWithHoldReleaseReqPostProc = CimBooleanUtils.isTrue(dynamicRouteFlag) ?
                    TransactionIDEnum.PARTIAL_REWORK_WITH_HOLD_RELEASE_REQ_POST_PROC_THREE :
                    TransactionIDEnum.PARTIAL_REWORK_WITH_HOLD_RELEASE_REQ_POST_PROC_FOUR;
        }

        String transactionID = partialReworkWithHoldReleaseReq.getValue();
        String postProcTxID = partialReworkWithHoldReleaseReqPostProc.getValue();
        PostProcessParam.CreateTask createTask = new PostProcessParam.CreateTask();
        createTask.setObjCommon(param.getObjCommon());
        createTask.setLotID(Collections.singletonList(reworkReqParams.getPartialReworkReq().getParentLotID()));
        List<PostProcessTask> retVal = findTaskPlan(transactionID).generateTasks(createTask);

        List<ObjectIdentifier> createLotIDs = Optional.ofNullable(param.getResult())
                .map(data -> Collections.singletonList(data.getCreatedLotID()))
                .orElseGet(Collections::emptyList);
        createTask.setLotID(createLotIDs);
        retVal.addAll(findTaskPlan(postProcTxID).generateTasks(createTask));
        return retVal;
    }
}
