package com.fa.cim.service.reticle;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.dto.Infos;
import com.fa.cim.rtms.ReticleUpdateParamsInfo;

import java.util.List;

public interface IReticleInqService {

    List<Infos.ReticleHoldListAttributes> sxHoldReticleListInq(Infos.ObjCommon objCommon, ObjectIdentifier reticleID);

    ReticleUpdateParamsInfo sxReticleUpdateParamsInq(Infos.ObjCommon objCommon);
}
