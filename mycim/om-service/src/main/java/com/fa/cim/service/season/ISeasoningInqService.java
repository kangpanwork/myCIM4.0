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
public interface ISeasoningInqService {

    List<Infos.Season> sxMachineSeasonPlanInq(Infos.ObjCommon objCommon, ObjectIdentifier eqpID);

    List<String> sxAllSeasonProductInq(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID);

    List<String> sxAllSeasonProductRecipeInq(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID);


    Infos.Season sxSeasonPlanInfoInq(Infos.ObjCommon objCommon, ObjectIdentifier seasonID);


    RecipeGroup.Info sxRrecipeGroupInfoInq(Infos.ObjCommon objCommon, ObjectIdentifier recipeGroupID);

    List<RecipeGroup.Info> sxRecipeGroupInq(Infos.ObjCommon objCommon);

    List<Infos.SeasonJob> sxMachineSeasonJobInq(Infos.ObjCommon objCommon, ObjectIdentifier eqpID);


    Infos.SeasonJob sxSeasonJobInfoInq(Infos.ObjCommon objCommon, ObjectIdentifier seasonJobID);


    Results.SeasonWhatNextLotListResult sxSeasonWhatNextInq(Infos.ObjCommon objCommon, Params.SeasonWhatNextLotListParams seasonWhatNextLotListParams);


    Results.SeasonLotsMoveInReserveInfoInqResult sxSeasonLotsMoveInReserveInfoInq(Infos.ObjCommon objCommon, Params.SeasonLotsMoveInReserveInfoInqParams seasonLotsMoveInReserveInfoInqParams);


    Results.SeasonLotsMoveInReserveInfoInqResult sxSeasonLotsMoveInReserveInfoForIBInq(Infos.ObjCommon objCommon, Params.SeasonLotsMoveInReserveInfoForIBInqParams seasonLotsMoveInReserveInfoForIBInqParams);

}
