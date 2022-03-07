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
public class MoveOutReqPostProcessProxy extends BaseMoveOutRequestProxy<Params.OpeComWithDataReqParams, Results.MoveOutReqResult> {

    @Autowired
    public MoveOutReqPostProcessProxy(PostProcessPlanManager planManager, ILotMethod lotMethod) {
        super(planManager, lotMethod);
    }

    @Override
    protected List<ObjectIdentifier> normalLots(Results.MoveOutReqResult result) {
        return result.getMoveOutLot().stream().map(Infos.OpeCompLot::getLotID).collect(Collectors.toList());
    }

    @Override
    protected List<ObjectIdentifier> holdReleaseLots(Results.MoveOutReqResult result) {
        return result.getHoldReleasedLotIDs();
    }

    @Override
    protected ObjectIdentifier controlJob(Params.OpeComWithDataReqParams param) {
        return param.getControlJobID();
    }

    @Override
    protected ObjectIdentifier equipment(Params.OpeComWithDataReqParams param) {
        return param.getEquipmentID();
    }

}
