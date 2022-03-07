package com.fa.cim.method;

import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;

import java.util.List;

public interface IBondingLotMethod {

    List<Infos.BondingLotAttributes> bondingLotListFillInTxPCQ039DR (Infos.ObjCommon objCommon,
                                                                     Params.BondingLotListInqInParams bondingLotListInqInParams);

}
