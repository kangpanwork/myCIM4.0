package com.fa.cim.method;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.dto.Infos;

import java.util.List;

public interface ILotInBondingFlowMethod {

    List<Infos.LotInBondingFlowInfo> lotsInBondingFlowRTDInterfaceReq (Infos.ObjCommon objCommon,
                                                                       String kind,
                                                                       ObjectIdentifier keyID);

    List<Infos.LotInBondingFlowInfo>  lotsInBondingFlowLotInfoGetDR (Infos.ObjCommon objCommon,
                                                                     List<Infos.HashedInfo> strSearchConditionSeq,
                                                                     List<Infos.LotInBondingFlowInfo> strLotInBondingFlowInfoSeq);

}
