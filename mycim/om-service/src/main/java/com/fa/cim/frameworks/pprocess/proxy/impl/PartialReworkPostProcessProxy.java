package com.fa.cim.frameworks.pprocess.proxy.impl;

import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.utils.CimBooleanUtils;
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

import javax.swing.text.html.Option;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
@Slf4j
public class PartialReworkPostProcessProxy extends BasePostProcessProxy<Params.PartialReworkReqParams, Results.PartialReworkReqResult> {

    @Autowired
    public PartialReworkPostProcessProxy(PostProcessPlanManager planManager) {
        super(planManager);
    }

    @Override
    public List<PostProcessTask> plan(PostProcessParam.PlanTask<Params.PartialReworkReqParams, Results.PartialReworkReqResult> param) {
        return Optional.ofNullable(param.getArguments())
                .map(arguments -> {
                    boolean dynamicRouteFlag = arguments.getPartialReworkReqInformation().getBDynamicRoute();
                    TransactionIDEnum partialReworkTransactionID;
                    TransactionIDEnum postPartialReworkTransactionID;
                    if (CimBooleanUtils.isTrue(arguments.getPartialReworkReqInformation().getBForceRework())) {
                        partialReworkTransactionID = CimBooleanUtils.isTrue(dynamicRouteFlag) ?
                                TransactionIDEnum.PARTIAL_REWORK_FORCE_AND_DYNAMIC_ROUTE :
                                TransactionIDEnum.PARTIAL_REWORK_FORCE;

                        postPartialReworkTransactionID = CimBooleanUtils.isTrue(dynamicRouteFlag) ?
                                TransactionIDEnum.POST_PARTIAL_REWORK_FORCE_AND_DYNAMIC_ROUTE :
                                TransactionIDEnum.POST_PARTIAL_REWORK_FORCE;
                    } else {
                        partialReworkTransactionID = CimBooleanUtils.isTrue(dynamicRouteFlag) ?
                                TransactionIDEnum.PARTIAL_REWORK_DYNAMIC_ROUTE :
                                TransactionIDEnum.PARTIAL_REWORK;

                        postPartialReworkTransactionID = CimBooleanUtils.isTrue(dynamicRouteFlag) ?
                                TransactionIDEnum.POST_PARTIAL_REWORK_DYNAMIC_ROUTE :
                                TransactionIDEnum.POST_PARTIAL_REWORK;
                    }
                    PostProcessParam.CreateTask createTask = new PostProcessParam.CreateTask();
                    createTask.setObjCommon(param.getObjCommon());
                    createTask.setLotID(Collections.singletonList(arguments.getPartialReworkReqInformation().getParentLotID()));
                    List<PostProcessTask> retVal = findTaskPlan(partialReworkTransactionID.getValue()).generateTasks(createTask);

                    retVal.addAll(Optional.ofNullable(param.getResult())
                            .map(Results.PartialReworkReqResult::getCreatedLotID)
                            .map(Collections::singletonList)
                            .map(lotIDs -> {
                                createTask.setLotID(lotIDs);
                                return findTaskPlan(postPartialReworkTransactionID.getValue()).generateTasks(createTask);
                            }).orElseGet(Collections::emptyList));
                    return retVal;
                }).orElseGet(Collections::emptyList);
    }
}
