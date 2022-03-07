package com.fa.cim.service.pcs.impl;

import com.fa.cim.annotaion.OmService;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.method.IScriptMethod;
import com.fa.cim.service.pcs.IProcessControlScriptInqService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@OmService
@Slf4j
public class ProcessControlScriptInqServiceImpl implements IProcessControlScriptInqService {

    @Autowired
    private IScriptMethod scriptMethod;

    @Override
    public List<Infos.UserParameterValue> sxPCSParameterValueInq(Infos.ObjCommon objCommon, Params.PCSParameterValueInqParams params){
        return scriptMethod.scriptGetUserParameter(objCommon, params.getParameterClass(), params.getIdentifier());
    }
}
