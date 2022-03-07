package com.fa.cim.method;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Inputs;
import com.fa.cim.dto.Params;
import com.fa.cim.jpa.SearchCondition;
import com.fa.cim.mfg.MfgInfoParams;
import com.fa.cim.newcore.bo.restrict.CimRestriction;
import com.fa.cim.newcore.dto.restriction.Constrain;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/12/4        ********            Nyx                create file
 *
 * @author: Nyx
 * @date: 2020/12/4 14:14
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IConstraintMethod {

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param strEntityInhibition -
     * @return void
     */
    void constraintCheckValidity(Infos.ObjCommon objCommon, Infos.EntityInhibitDetailAttributes strEntityInhibition);

    /**
     * description:Rewrite this method according to new core
     * entityInhibitRegistrationReq -> constraintRegistrationReq
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/11/27 16:41                     Nyx                Create
     *
     * @author Nyx
     * @date 2020/11/27 16:41
     * @param objCommon
     * @param constraintDetailAttributes -
     * @return com.fa.cim.dto.Infos.EntityInhibitDetailInfo
     */
    Infos.EntityInhibitDetailInfo constraintRegistrationReq(
            Infos.ObjCommon objCommon, Infos.EntityInhibitDetailAttributes constraintDetailAttributes, String claimMemo);

    /**
     * description:entityInhibitAttributeListGetDR -> enhanceConstraintAttributeListGetDR
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/12/3 18:24                     Nyx                Create
     *
     * @author Nyx
     * @date 2020/12/3 18:24
     * @param objCommon
     * @param attributes
     * @param entityInhibitReasonDetailInfoFlag -
     * @return java.util.List<com.fa.cim.dto.ConstrainInfos.EnhanceConstraintDetailInfo>
     */
    List<Infos.EntityInhibitDetailInfo> constraintAttributesGetDR(Infos.ObjCommon objCommon,
                                                                  Infos.EntityInhibitDetailAttributes attributes,
                                                                  boolean entityInhibitReasonDetailInfoFlag);

    /**
     * description:mfgRestrictCancelReq -> enhanceConstraintCancelReq
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/12/4 15:42                     Nyx                Create
     *
     * @author Nyx
     * @date 2020/12/4 15:42
     * @param objCommon
     * @param entityInhibitions -
     * @return java.util.List<com.fa.cim.dto.ConstrainInfos.EntityInhibitDetailInfo>
     */
    List<Infos.EntityInhibitDetailInfo> constraintCancelReq(Infos.ObjCommon objCommon,
                                                            List<Infos.EntityInhibitDetailInfo> entityInhibitions);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/12/6 23:41                     Nyx                Create
     *
     * @author Nyx
     * @date 2020/12/6 23:41
     * @param strObjCommonIn
     * @param entityInhibitAttributes -
     * @return com.fa.cim.dto.Infos.EntityInhibitCheckForEntitiesOut
     */
    Infos.EntityInhibitCheckForEntitiesOut constraintCheckForEntities(
            Infos.ObjCommon strObjCommonIn, Infos.EntityInhibitAttributes entityInhibitAttributes);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon -
     * @return com.fa.cim.dto.RetCode<com.fa.cim.dto.Results.MfgRestrictListInqResult>
     * @author Sun
     * @date 11/27/2018 10:20 AM
     */
    List<Infos.EntityInhibitDetailInfo> constraintExceptionLotAttributesGetDR(
            Infos.ObjCommon objCommon, List<Infos.EntityIdentifier> entityIdentifierList,
            Infos.EntityInhibitExceptionLotInfo exceptionLotInfo);

    /**
     * description:
     * <p></p>
     * change history:
     * date   defect   person   comments
     * ------------------------------------------------------------------------------------------------------------------
     *
     * @return
     * @author Decade
     * @date 2020/11/23/023 10:25
     */
    List<Infos.ConstraintEqpDetailInfo> constraintAttributeListGetDR(
            Params.MfgRestrictListInqParams mfgRestrictListInqParams, Infos.ObjCommon objCommon);

    /**
     * description:
     * <p></p>
     * change history:
     * date   defect   person   comments
     * ------------------------------------------------------------------------------------------------------------------
     *
     * @return
     * @author Decade
     * @date 2020/11/10/010 14:55
     */
    List<Infos.ConstraintEqpDetailInfo> constraintAttributesGetByEqpDR(
            Params.MfgRestrictListByEqpInqParams mfgRestrictListByEqpInqParams, Infos.ObjCommon objCommon);

    /**
     * description:
     * <p></p>
     * change history:
     * date   defect   person   comments
     * ------------------------------------------------------------------------------------------------------------------
     *
     * @return
     * @author Decade
     * @date 2020/11/17/017 12:20
     */
    List<Infos.ConstraintEqpDetailInfo> constraintAttributesGetByEqpListDR(List<Infos.AreaEqp> areaEqpList,
                                                                           Infos.ObjCommon objCommon);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     *
     * @param exceptionLotList -
     * @param objCommon        -
     * @return com.fa.cim.dto.RetCode<java.lang.Object>
     * @author Sun
     * @date 11/28/2018 10:03 AM
     */
    void constraintExceptionLotCheckValidity(List<Infos.EntityInhibitExceptionLot> exceptionLotList,
                                             Infos.ObjCommon objCommon);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon -
     * @return com.fa.cim.dto.RetCode<com.fa.cim.dto.Results.MfgRestrictListInqResult>
     * @author Sun
     * @date 11/28/2018 10:15 AM
     */
    List<Infos.EntityInhibitDetailInfo> constraintExceptionLotRegistrationReq(
            Infos.ObjCommon objCommon, List<Infos.EntityInhibitExceptionLot> entityInhibitExceptionLots,
            String claimMemo);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     *
     * @param entityInhibitExceptionLots -
     * @param objCommon                  -
     * @return com.fa.cim.dto.RetCode<com.fa.cim.dto.Results.MfgRestrictListInqResult>
     * @author Sun
     * @date 11/28/2018 6:11 PM
     */
    List<Infos.EntityInhibitDetailInfo> constraintExceptionLotCancelReq(
            List<Infos.EntityInhibitExceptionLot> entityInhibitExceptionLots, Infos.ObjCommon objCommon);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/12/6 20:50                     Nyx                Create
     *
     * @author Nyx
     * @date 2020/12/6 20:50
     * @param objCommon
     * @param strEntityInhibitInfos
     * @param lotID -
     * @return java.util.List<com.fa.cim.dto.Infos.EntityInhibitInfo>
     */
    List<Infos.EntityInhibitInfo> constraintEffectiveForLotGetDR(Infos.ObjCommon objCommon,
                                                                 List<Infos.EntityInhibitInfo> strEntityInhibitInfos,
                                                                 ObjectIdentifier lotID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon -
     * @return com.fa.cim.dto.RetCode<java.lang.Object>
     * @author Sun
     * @date 12/19/2018 2:26 PM
     */
    void constraintExceptionLotChangeForOpeComp(Infos.ObjCommon objCommon,
                                                List<ObjectIdentifier> lotIDs, ObjectIdentifier controlJobID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author lightyh
     * @date 2019/9/24 13:03
     * @param inputs -
     * @return java.util.List<com.fa.cim.dto.Infos.EntityInhibitInfo>
     */
    List<Infos.EntityInhibitInfo> constraintCheckForReticleInhibition(
            Infos.ObjCommon objCommon, Inputs.EntityInhibitCheckForReticleInhibition inputs);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     *
     * @param entityInhibitList -
     * @return java.lang.String
     * @author Bear
     * @date 2019/5/26 13:57
     */
    String makeInhibitListFromEntityInhibits(List<CimRestriction> entityInhibitList);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param strObjCommonIn
     * @return void
     * @exception
     * @author Ho
     * @date 2019/9/26 10:36
     */
    void constraintRequestForMultiFab(Infos.ObjCommon strObjCommonIn,
                                      List<Infos.EntityInhibitAttributesWithFabInfo> strEntityInhibitionsWithFabInfo,
                                      String claimMemo);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param lotID
     * @param eqpID     -
     * @return com.fa.cim.pojo.Outputs.ObjEntityInhibitCheckForLotDRout
     * @author Jerry
     * @date 13:34
     */
    List<Infos.EntityInhibitInfo> constraintCheckForLotDR(
            Infos.ObjCommon objCommon, ObjectIdentifier lotID, ObjectIdentifier eqpID);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param lotID
     * @param entityInhibitInfos -
     * @return com.fa.cim.pojo.Outputs.ObjEntityInhibitFilterExceptionLotOut
     * @author jerry
     * @date 2018/6/4 11:14
     */
    List<Infos.EntityInhibitInfo> constraintFilterExceptionLot(
            Infos.ObjCommon objCommon, ObjectIdentifier lotID, List<Infos.EntityInhibitInfo> entityInhibitInfos);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     * @author Sun
     * @date 6/20/2019 2:43 PM
     * @param objCommon -
     * @param lotID -
     * @param equipmentID -
     * @return com.fa.cim.dto.Outputs.ObjEntityInhibitCheckForLotOut
     */
    List<Infos.EntityInhibitInfo> constraintCheckForLot(Infos.ObjCommon objCommon,
                                                        ObjectIdentifier lotID, ObjectIdentifier equipmentID);

    void setEntityInhibitRecordsToEntityInhibitInfos(List<Infos.EntityInhibitInfo> dest,
                                                     List<Constrain.EntityInhibitRecord> source);

    void setEntityInhibitInfosToEntityInhibitRecords(List<Constrain.EntityInhibitRecord> dest,
                                                     List<Infos.EntityInhibitInfo> source);

    List<Infos.RecipeTime> recipeTimeLimitListQuery(Params.RecipeTimeInqParams recipeTimeInqParams);

    void recipeTimeLimitSet(Params.RecipeTimeSetParams params) ;

    void recipeTimeLimitDelete(Params.RecipeTimeCancelParams params) ;

    void recipeTimeUseSet(Params.RecipeUseSetParams params) ;

    void recipeTimeUseCheck(Infos.ObjCommon objCommon);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/12/8 17:18                     Nyx                Create
     *
     * @author Nyx
     * @date 2020/12/8 17:18
     * @param objCommon
     * @param equipmentID
     * @param functionRule -
     * @param isSpecificTool
     * @return java.util.List<com.fa.cim.dto.Infos.ConstraintByEqpInfo>
     */
    List<Infos.ConstraintEqpDetailInfo> constraintListByEqp(
            Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, String functionRule, Boolean isSpecificTool);
    
    /*       
    * description:  
    * change history:  
    * date             defect             person             comments  
    * --------------------------------------------------------------------------------------------------------------------- 
    * 2021/5/10 10:12                      Decade                Create
    *         
    * @author Decade  
    * @date 2021/5/10 10:12  
    * @param null -  
    * @return  
    */
    List<Infos.EntityInhibitDetailAttributes> convertMfgExcel(List<MfgInfoParams> mfgInfoParamsList);

    /**
    * description:  for search tool constraint history
    * change history:
    * date             defect             person             comments
    * ---------------------------------------------------------------------------------------------------------------------
    * 2021/6/11 19:20                       AOKI              Create
    * @author AOKI
    * @date 2021/6/11 19:20
    * @param userID
    * @param functionRule BLIST or WLIST
    * @param specificTool 1 tool or 0 normal
    * @param searchCondition page and size for webUI
    * @return org.springframework.data.domain.Page<com.fa.cim.dto.Infos.ConstraintHistoryDetailInfo>
    */
    Page<Infos.ConstraintHistoryDetailInfo> constrintHistoryGet(String userID, String functionRule,
                                                                Boolean specificTool, SearchCondition searchCondition);
}