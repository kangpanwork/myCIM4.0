package com.fa.cim.service.pcs;

import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;

import java.util.List;

public interface IProcessControlScriptInqService {
    List<Infos.UserParameterValue> sxPCSParameterValueInq(Infos.ObjCommon objCommon, Params.PCSParameterValueInqParams params);
}
