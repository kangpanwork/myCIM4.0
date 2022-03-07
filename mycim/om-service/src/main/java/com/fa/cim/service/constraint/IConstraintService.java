package com.fa.cim.service.constraint;

import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/9/9        ********             Bear               create file
 *
 * @author: LiaoYunChuan
 * @date: 2020/9/9 10:45
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IConstraintService {
    void sxMfgRestrictCancelReq(Params.MfgRestrictCancelReqParams mfgRestrictCancelReqParams, Infos.ObjCommon objCommon);
    void sxMfgRestrictExclusionLotCancelReq(Params.MfgRestrictExclusionLotReqParams cancelParams, Infos.ObjCommon objCommon);
    void sxMfgRestrictExclusionLotReq(Params.MfgRestrictExclusionLotReqParams params, Infos.ObjCommon objCommon) ;
    Infos.EntityInhibitDetailInfo sxMfgRestrictReq(Params.MfgRestrictReqParams mfgRestrictReqParams, Infos.ObjCommon objCommon);
    Infos.MfgMMResponse sxMfgRestrictReq_110(Params.MfgRestrictReq_110Params mfgRestrictReq_110Params, Infos.ObjCommon objCommon);
    void sxMfgRecipeTimeLimitSet(Params.RecipeTimeSetParams params, Infos.ObjCommon objCommon);
    void sxMfgRecipeTimeLimitDelete(Params.RecipeTimeCancelParams params, Infos.ObjCommon objCommon);
    void sxMfgRecipeUseSet(Params.RecipeUseSetParams params);
    void sxMfgRecipeUseCheck(Infos.ObjCommon objCommon);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/12/8 19:55                     Nyx                Create
     *
     * @author Nyx
     * @date 2020/12/8 19:55
     * @param objCommon
     * @param entityInhibitDetailAttributes -
     * @return com.fa.cim.dto.Infos.ConstraintEqpDetailInfo
     */
    Infos.ConstraintEqpDetailInfo sxConstraintEqpAddReq(Infos.ObjCommon objCommon, Infos.ConstraintDetailAttributes entityInhibitDetailAttributes, String claimMemo);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/12/8 19:54                     Nyx                Create
     *
     * @author Nyx
     * @date 2020/12/8 19:54
     * @param objCommon
     * @param constraintEqpDetailInfo
     * @param claimMemo -
     * @return com.fa.cim.dto.Infos.ConstraintEqpDetailInfo
     */
    Infos.ConstraintEqpDetailInfo sxConstraintEqpModifyReq(Infos.ObjCommon objCommon, Infos.ConstraintEqpDetailInfo constraintEqpDetailInfo, String claimMemo);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/12/8 19:53                     Nyx                Create
     *
     * @author Nyx
     * @date 2020/12/8 19:53
     * @param objCommon
     * @param constraintEqpDetailInfos
     * @param claimMemo -
     * @return void
     */
    void sxConstraintEqpCancelReq(Infos.ObjCommon objCommon, List<Infos.ConstraintEqpDetailInfo> constraintEqpDetailInfos, String claimMemo);

    /*
    * description:
    * change history:
    * date             defect             person             comments
    * ---------------------------------------------------------------------------------------------------------------------
    * 2021/5/8 15:16                      Decade                Create
    *
    * @author Decade
    * @date 2021/5/8 15:16
    * @param null -
    * @return
    */
    List<Infos.EntityInhibitDetailAttributes> sxConstraintInfoImportReq(Infos.ObjCommon objCommon, MultipartFile multipartFile);

}
