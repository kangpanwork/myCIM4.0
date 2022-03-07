package com.fa.cim.service.post;

import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;

import java.util.List;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/9/8        ********            Nyx                create file
 *
 * @author: Nyx
 * @date: 2020/9/8 16:41
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IPostService {
    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param postTaskExecuteReqParams -
     * @return com.fa.cim.dto.result.RetCode<PostTaskRegisterReqResult>
     * @author Bear
     * @date 2018/5/7
     */
    Results.PostTaskExecuteReqResult sxPostTaskExecuteReq(Infos.ObjCommon objCommon, Params.PostTaskExecuteReqParams postTaskExecuteReqParams) ;

    Results.PostTaskRegisterReqResult sxPostTaskRegisterReq(Infos.ObjCommon objCommon, Params.PostTaskRegisterReqParams params);

    List<Infos.PostProcessActionInfo> sxPostActionModifyReq (Infos.ObjCommon objCommon,
                                                             String actionCode,
                                                             List<Infos.PostProcessActionInfo> strPostProcessActionInfoSeq,
                                                             List<Infos.PostProcessAdditionalInfo> strPostProcessAdditionalInfoSeq,
                                                             String claimMemo);

    void sxPostFilterCreateForExtReq(Infos.ObjCommon objCommon, Infos.PostFilterCreateForExtReqInParm parm, String claimMemo);

    void sxPostFilterRemoveForExtReq(Infos.ObjCommon objCommon, List<Infos.ExternalPostProcessFilterInfo> externalPostProcessFilterInfos, String claimMemo);
}