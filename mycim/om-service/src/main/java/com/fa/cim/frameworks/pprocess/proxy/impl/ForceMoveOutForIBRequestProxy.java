package com.fa.cim.frameworks.pprocess.proxy.impl;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;
import com.fa.cim.frameworks.pprocess.manager.PostProcessPlanManager;
import com.fa.cim.frameworks.pprocess.proxy.impl.base.BaseMoveOutRequestProxy;
import com.fa.cim.method.ILotMethod;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@Component
public class ForceMoveOutForIBRequestProxy extends BaseMoveOutRequestProxy<Params.ForceMoveOutForIBReqParams,
        Results.ForceMoveOutReqResult> {

    @Autowired
    public ForceMoveOutForIBRequestProxy(PostProcessPlanManager planManager, ILotMethod lotMethod) {
        super(planManager, lotMethod);
    }

    @Override
    public List<ObjectIdentifier> normalLots(Results.ForceMoveOutReqResult result) {
        return result.getStrOpeCompLot().stream().map(Infos.OpeCompLot::getLotID).collect(Collectors.toList());
    }

    @Override
    public ObjectIdentifier controlJob(Params.ForceMoveOutForIBReqParams param) {
        return param.getControlJobID();
    }

    @Override
    public ObjectIdentifier equipment(Params.ForceMoveOutForIBReqParams param) {
        return param.getEquipmentID();
    }

    @Override
    protected List<ObjectIdentifier> holdReleaseLots(Results.ForceMoveOutReqResult result) {
        return result.getHoldReleasedLotIDs();
    }
}
