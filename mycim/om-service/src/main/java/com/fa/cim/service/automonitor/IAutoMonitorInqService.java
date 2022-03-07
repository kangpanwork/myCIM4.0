package com.fa.cim.service.automonitor;

import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;
import com.fa.cim.dto.am.AllRecipeByProductSpecificationInqParam;

import java.util.List;

public interface IAutoMonitorInqService {
    Results.AMJobListInqResult sxAMJobListInq(Infos.ObjCommon objCommon, Params.AMJobListInqParams params);
    List<Infos.EqpMonitorDetailInfo> sxAMListInq(Infos.ObjCommon objCommon, Params.AMListInqParams params);
    Results.WhatNextAMLotInqResult sxWhatNextAMLotInq(Infos.ObjCommon objCommon, Params.WhatNextAMLotInqInParm parm);

    /**
     * ho
     * @param objCommon
     * @param allRecipeByProductSpecificationInqParam
     * @return
     */
    List<Infos.DefaultRecipeSetting> sxAllRecipeByProductSpecificationInq(Infos.ObjCommon objCommon, AllRecipeByProductSpecificationInqParam allRecipeByProductSpecificationInqParam);
}
