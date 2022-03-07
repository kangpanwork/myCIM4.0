package com.fa.cim.service.season;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;
import com.fa.cim.newcore.dto.rcpgrp.RecipeGroup;

import java.util.List;

/**
 * description:
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/9/8                              Neyo                create file
 *
 * @author: Neyo
 * @date: 2020/9/8 16:26
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface ISeasoningService {

    void sxSeasonPlanCreateReq(Infos.ObjCommon objCommon, Infos.Season season, String claimMemo);


    void sxUpdateSeasonPlan(Infos.ObjCommon objCommon, List<Infos.Season> seasons);


    void sxSeasonPlansDeleteReq(Infos.ObjCommon objCommon, List<Infos.Season> seasons, String claimMemo);


    void sxSeasonPlanDeleteReq(Infos.ObjCommon objCommon, Infos.Season season, String claimMemo);


    void sxSeasonRecipeGroupCreateReq(Infos.ObjCommon objCommon, RecipeGroup.Info recipeGroup, String claimMemo);


    void sxSeasonRecipeGroupModifyReq(Infos.ObjCommon objCommon, RecipeGroup.Info recipeGroup, String claimMemo);


    void sxSeasonRecipeGroupDeleteReq(Infos.ObjCommon objCommon, RecipeGroup.Info recipeGroup, String claimMemo);

    void sxUpdateSeasonJob(Infos.ObjCommon objCommon, Infos.SeasonJob seasonJob, String seasonJobStatus, String claimMemo);


    void seasonUpdateForMoveOut(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, List<Infos.StartCassette> startCassetteList);


    void seasonJobCreateForPM(Infos.ObjCommon objCommon, ObjectIdentifier eqpID, String claimMemo);


    Results.SeasonLotMoveInReserveReqResult sxSeasonLotMoveInReserveReq(Infos.ObjCommon objCommon, Params.SeasonLotMoveInReserveReqParams seasonLotMoveInReserveReqParams);


    Results.SeasonLotMoveInReserveReqResult sxSeasonMoveInReserveReq(Infos.ObjCommon objCommon, Params.SeasonMoveInReserveReqParams seasonMoveInReserveReqParams);



    Results.SeasonMoveInReserveReqResult moveInReserveReq(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID
            , Params.SeasonMoveInReserveParams moveInReserveParams);

    Results.SeasonMoveInReserveReqResult moveInReserveReq(Infos.ObjCommon objCommon, Params.MoveInReserveReqParams moveInReserveParams);


    Results.SeasonMoveInReserveReqResult moveInReserveForIBReq(Infos.ObjCommon objCommon, Params.MoveInReserveForIBReqParams moveInReserveParams);


    Results.SeasonLotMoveInReserveReqResult sxMoveInReserveReq(Infos.ObjCommon objCommon, Params.MoveInReserveReqForSeasonParams moveInReserveReqForSeasonParams);


    Results.SeasonLotMoveInReserveReqResult sxMoveInReserveForIBReq(Infos.ObjCommon objCommon, Params.MoveInReserveForIBReqForSeasonParams moveInReserveReqForSeasonParams);


    void sxSeasonJobForMoveInUpdate(Infos.ObjCommon objCommon, ObjectIdentifier eqpID);


    void sxSeasonForMoveInCancel(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, ObjectIdentifier controlJobID);


    void sxSeasonForMoveInReserveCancel(Infos.ObjCommon objCommon, Params.MoveInReserveCancelReqParams params);


    void sxSeasonForUnloading(Infos.ObjCommon objCommonIn, ObjectIdentifier equipmentID, ObjectIdentifier cassetteID);


    void sxSeasonForMoveOut(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID);


    void sxSeasonForForceMoveOut(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID);

}
