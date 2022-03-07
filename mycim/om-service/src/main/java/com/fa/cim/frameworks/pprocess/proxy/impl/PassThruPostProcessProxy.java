package com.fa.cim.frameworks.pprocess.proxy.impl;

import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;
import com.fa.cim.frameworks.dto.pp.PostProcessParam;
import com.fa.cim.frameworks.dto.pp.PostProcessTask;
import com.fa.cim.frameworks.pprocess.manager.PostProcessPlanManager;
import com.fa.cim.frameworks.pprocess.proxy.impl.base.BasePostProcessProxy;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * <p>PassThruPostProcessProxy .
 * change history:
 * date               defect            person            comments
 * -------------------------------------------------------------------------------------------------------------------
 * 2021/6/18 16:18    ********          ZQI               create file.
 *
 * @author ZQI
 * @version 1.0
 * @date 2021/6/18 16:18
 * @copyright 2021, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 * @since JDK1.8
 */
@Component
public class PassThruPostProcessProxy extends BasePostProcessProxy<Params.PassThruReqParams, Results.PassThruReqResult> {

    public PassThruPostProcessProxy(PostProcessPlanManager planManager) {
        super(planManager);
    }

    @Override
    public List<PostProcessTask> plan(PostProcessParam.PlanTask<Params.PassThruReqParams, Results.PassThruReqResult> param) {
        Infos.ObjCommon objCommon = param.getObjCommon();
        //----------------------------
        // Normal Lots
        //----------------------------
        PostProcessParam.CreateTask createTask = new PostProcessParam.CreateTask();
        createTask.setObjCommon(objCommon);
        createTask.setLotID(Optional.ofNullable(param.getResult())
                .map(Results.PassThruReqResult::getStrGatePassLotsResult)
                .orElseGet(Collections::emptyList).stream()
                .map(Results.GatePassLotsResult::getLotID)
                .collect(Collectors.toList()));
        List<PostProcessTask> retVal = findTaskPlan(objCommon.getTransactionID()).generateTasks(createTask);

        //-----------------------------
        // HoldRelease Lots
        //-----------------------------
        createTask.setLotID(Optional.ofNullable(param.getResult())
                .map(Results.PassThruReqResult::getHoldReleasedLotIDs)
                .orElseGet(Collections::emptyList));
        retVal.addAll(findTaskPlan(TransactionIDEnum.HOLD_LOT_RELEASE_REQ.getValue()).generateTasks(createTask));
        return retVal;
    }

}
