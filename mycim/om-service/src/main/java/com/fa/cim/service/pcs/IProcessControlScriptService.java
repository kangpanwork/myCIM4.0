package com.fa.cim.service.pcs;

import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;

public interface IProcessControlScriptService {
    void sxPCSParameterValueSetReq(Infos.ObjCommon objCommon, Params.PCSParameterValueSetReqParams pcsParameterValueSetReqParams);
    void sxProcessControlScriptRunReq(Infos.ObjCommon objCommon, Params.ProcessControlScriptRunReqParams params);

}
