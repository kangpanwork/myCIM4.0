package com.fa.cim.service.access;

import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Results;
import com.fa.cim.newcore.bo.CimBO;

import java.util.List;

public interface IAccessService {
    Results.OwnerChangeReqResult sxOwnerChangeReq(List<Infos.OwnerChangeObject> strOwnerChangeObjectSeq, Infos.ObjCommon objCommon, Infos.OwnerChangeReqInParm strOwnerChangeReqInParm, Infos.OwnerChangeDefinition strOwnerChangeDefinition, Infos.OwnerChangeDefObjDefinition strOwnerChangeDefObjDefinition, String claimMemo, CimBO bo);
}
