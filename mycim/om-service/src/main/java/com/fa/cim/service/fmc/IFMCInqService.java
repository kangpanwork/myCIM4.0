package com.fa.cim.service.fmc;

import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;

/**
 * <p>IFMCInqController .
 *
 * @author ZQI
 * @version 1.0
 * @date 2020/4/21 11:01
 * @copyright 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 * @since JDK1.8
 */
public interface IFMCInqService {

    Results.SLMCandidateCassetteForRetrievingInqResult sxSLMCandidateCassetteForRetrievingInq(Infos.ObjCommon objCommon , Params.SLMCandidateCassetteForRetrievingInqInParams slmCandidateCassetteForRetrievingInqInParams);

}
