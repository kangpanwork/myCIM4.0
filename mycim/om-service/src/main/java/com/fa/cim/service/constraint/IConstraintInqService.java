package com.fa.cim.service.constraint;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;
import com.fa.cim.mfg.MfgInfoParams;
import com.fa.cim.jpa.SearchCondition;
import org.springframework.data.domain.Page;

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
 * @date: 2020/9/9 10:56
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IConstraintInqService {

    List<Infos.EntityInhibitDetailInfo> sxMfgRestrictExclusionLotListInq(
            Params.MfgRestrictExclusionLotListInqParams params, Infos.ObjCommon objCommon);
    Results.NewMfgRestrictListInqResult sxMfgRestrictListInq(
            Params.MfgRestrictListInqParams mfgRestrictListInqParams, Infos.ObjCommon objCommon);
    List<Infos.ConstraintEqpDetailInfo> sxMfgRestrictListInq_110(
            Params.MfgRestrictListInqParams mfgRestrictListInqParams, Infos.ObjCommon objCommon);
    Results.MfgRestrictListByEqpInqResult sxMfgRestrictListByEqpInq(
            Params.MfgRestrictListByEqpInqParams mfgRestrictListByEqpInqParams, Infos.ObjCommon objCommon);

    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param params
     * @param objCommon
     * @return RetCode<Results.ProcessDefinitionIndexListInqResult>
     * @author Sun
     * @date 2018/10/24
     */
    public List<Infos.ProcessDefinitionIndexList> sxRouteListInq(Params.RouteListInqParams params,
                                                                 Infos.ObjCommon objCommon);

    /**
     * description:
     * <p>
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon -
     * @param params -
     * @return com.fa.cim.dto.RetCode<List<Results.StageListInqResult>>
     * @author Sun
     * @since 2018/10/24
     */
    Results.StageListInqResult sxStageListInq(Infos.ObjCommon objCommon, Params.StageListInqParams params) ;

    Results.RecipeTimeInqResult recipeTimeLimitListInq(Params.RecipeTimeInqParams recipeTimeInqParams,
                                                       Infos.ObjCommon objCommon);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/12/8 16:41                     Nyx                Create
     *
     * @author Nyx
     * @date 2020/12/8 16:41
     * @param objCommon
     * @param equipmentID
     * @param functionRule -
     * @return java.util.List<com.fa.cim.dto.Infos.ConstraintByEqp>
     */
    List<Infos.ConstraintEqpDetailInfo> sxConstraintListByEqpInq(Infos.ObjCommon objCommon,
                                                                 ObjectIdentifier equipmentID, String functionRule);

    /*
    * description:
    * change history:
    * date             defect             person             comments
    * ---------------------------------------------------------------------------------------------------------------------
    * 2021/5/11 10:57                      Decade                Create
    *
    * @author Decade
    * @date 2021/5/11 10:57
    * @param null -
    * @return
    */
    List<MfgInfoParams> sxConstraintInfoExportInq(Infos.ObjCommon objCommon,
                                                  List<Infos.ConstraintEqpDetailInfo> constraintEqpDetailInfos);

    /**
    * description: inquire the specific constraint history
    * change history:
    * date             defect             person             comments
    * ---------------------------------------------------------------------------------------------------------------------
    * 2021/6/11 14:26                       AOKI              Create
    * @author AOKI
    * @date 2021/6/11 14:26
    * @param objCommon user information
    * @param functionRule BLIST black/WLIST white
    * @param specificTool 1 toolContraint /0 general Constraint
    * @return java.util.List<com.fa.cim.dto.Infos.ConstraintEqpDetailInfo>
    */
    Page<Infos.ConstraintHistoryDetailInfo> sxConstraintHistoryListInq(Infos.ObjCommon objCommon,
                                                                       String functionRule, Boolean specificTool,
                                                                       SearchCondition searchCondition);
}
