package com.fa.cim.service.reticle.impl;

import com.fa.cim.annotaion.OmService;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.utils.Validations;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.dto.Infos;
import com.fa.cim.method.IReticleMethod;
import com.fa.cim.rtms.ReticleUpdateParamsInfo;
import com.fa.cim.service.reticle.IReticleInqService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Slf4j
@OmService
public class ReticleInqServiceImpl implements IReticleInqService {
    @Autowired
    private RetCodeConfig retCodeConfig;

    @Autowired
    private IReticleMethod reticleMethod;

    @Override
    public List<Infos.ReticleHoldListAttributes> sxHoldReticleListInq(Infos.ObjCommon objCommon, ObjectIdentifier reticleID) {
        Validations.check(ObjectIdentifier.isEmpty(reticleID), retCodeConfig.getNotFoundReticle());
        return reticleMethod.findHoldReticleListInq(objCommon, reticleID);
    }

    @Override
    public ReticleUpdateParamsInfo sxReticleUpdateParamsInq(Infos.ObjCommon objCommon) {
        return reticleMethod.reticleUpdateParamsGet(objCommon);
    }
}
