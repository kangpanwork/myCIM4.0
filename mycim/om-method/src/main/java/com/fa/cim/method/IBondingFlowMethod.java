package com.fa.cim.method;

import com.fa.cim.dto.Infos;

import java.util.List;

public interface IBondingFlowMethod {

    List<Infos.LotInBondingFlowInfo> bondingFlowLotListGetDR (Infos.ObjCommon objCommon, List<Infos.HashedInfo> strSearchConditionSeq);

    List<String> bondingFlowBondingFlowNameGetDR (Infos.ObjCommon objCommon);
}
