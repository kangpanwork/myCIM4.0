package com.fa.cim.frameworks.pprocess.proxy.impl;

import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;
import com.fa.cim.frameworks.pprocess.manager.PostProcessPlanManager;
import com.fa.cim.frameworks.pprocess.proxy.impl.base.BaseMoveOutRequestProxy;
import com.fa.cim.method.ILotMethod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;


@Component
public class ForceMoveOutPostProcessProxy extends BaseMoveOutRequestProxy<Params.ForceMoveOutReqParams,
        Results.ForceMoveOutReqResult> {

    @Autowired
    public ForceMoveOutPostProcessProxy(PostProcessPlanManager planManager, ILotMethod lotMethod) {
        super(planManager, lotMethod);
    }

    @Override
    protected List<ObjectIdentifier> normalLots(Results.ForceMoveOutReqResult result) {
        return result.getStrOpeCompLot().stream().map(Infos.OpeCompLot::getLotID).collect(Collectors.toList());
    }

    @Override
    protected List<ObjectIdentifier> holdReleaseLots(Results.ForceMoveOutReqResult result) {
        return result.getHoldReleasedLotIDs();
    }

    @Override
    protected ObjectIdentifier controlJob(Params.ForceMoveOutReqParams param) {
        return param.getControlJobId();
    }

    @Override
    protected ObjectIdentifier equipment(Params.ForceMoveOutReqParams param) {
        return param.getEquipmentId();
    }

}
