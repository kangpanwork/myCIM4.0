package com.fa.cim.service.season.impl;

import com.fa.cim.annotaion.OmService;
import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.utils.CimArrayUtils;
import com.fa.cim.common.utils.CimStringUtils;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;
import com.fa.cim.method.ILotMethod;
import com.fa.cim.method.ISeasonMethod;
import com.fa.cim.newcore.bo.machine.CimMachine;
import com.fa.cim.newcore.bo.season.CimSeason;
import com.fa.cim.newcore.bo.season.CimSeasonJob;
import com.fa.cim.newcore.dto.rcpgrp.RecipeGroup;
import com.fa.cim.newcore.factory.BaseCoreFactory;
import com.fa.cim.service.dispatch.IDispatchInqService;
import com.fa.cim.service.lot.ILotInqService;
import com.fa.cim.service.season.ISeasoningInqService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * description: change history: date defect# person comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/9/8 Neyo create file
 *
 * @author: Neyo
 * @date: 2020/9/8 16:57
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@OmService
@Slf4j
public class SeasoningInqServiceImpl implements ISeasoningInqService {

  @Autowired
  private ISeasonMethod seasonMethod;

  @Autowired
  private ILotMethod lotMethod;

  @Autowired
  private ILotInqService lotInqService;

  @Autowired
  private IDispatchInqService dispatchInqService;

  @Autowired
  private BaseCoreFactory baseCoreFactory;

  /**
   * description:
   *
   * <p>change history: date defect person comments
   * ---------------------------------------------------------------------------------------------------------------------
   *
   * @param objCommon
   * @param eqpID
   * @return java.util.List<com.fa.cim.dto.Infos.Season>
   * @throws
   * @author ho
   * @date 2020/5/21 12:44
   */
  @Override
  public List<Infos.Season> sxMachineSeasonPlanInq(
      Infos.ObjCommon objCommon, ObjectIdentifier eqpID) {
    List<Infos.Season> seasonList = seasonMethod.querySeason(eqpID);
    List<Infos.Season> checkTheActiveSeason =
        seasonMethod.checkTheActiveSeason(eqpID, "", null, null);
    List<Infos.Season> result = new ArrayList<>();
    Optional.ofNullable(checkTheActiveSeason)
        .ifPresent(result::addAll);
    Optional.ofNullable(seasonList)
        .ifPresent(
            seasons -> {
              seasons.forEach(
                  season -> {
                    if (!result.stream()
                        .anyMatch(
                            rseason ->
                                    ObjectIdentifier.equalsWithValue(
                                            season.getSeasonID(), rseason.getSeasonID()))) {
                      result.add(season);
                    }
                  });
            });
    return result;
  }

  /**
   * description:
   *
   * <p>change history: date defect person comments
   * ---------------------------------------------------------------------------------------------------------------------
   *
   * @param objCommon
   * @param equipmentID
   * @return java.util.List<java.lang.String>
   * @throws
   * @author ho
   * @date 2020/6/23 12:42
   */
  @Override
  public List<String> sxAllSeasonProductInq(
      Infos.ObjCommon objCommon, ObjectIdentifier equipmentID) {
    return seasonMethod.getAllSeasonProduct(equipmentID);
  }

  /**
   * description:
   *
   * <p>change history: date defect person comments
   * ---------------------------------------------------------------------------------------------------------------------
   *
   * @param objCommon
   * @param equipmentID
   * @return java.util.List<java.lang.String>
   * @throws
   * @author ho
   * @date 2020/6/23 12:42
   */
  @Override
  public List<String> sxAllSeasonProductRecipeInq(
      Infos.ObjCommon objCommon, ObjectIdentifier equipmentID) {
    return seasonMethod.getAllSeasonProductRecipe(equipmentID);
  }

  /**
   * description:
   *
   * <p>change history: date defect person comments
   * ---------------------------------------------------------------------------------------------------------------------
   *
   * @param objCommon
   * @param seasonID
   * @return com.fa.cim.dto.Infos.Season
   * @throws
   * @author ho
   * @date 2020/5/27 13:49
   */
  @Override
  public Infos.Season sxSeasonPlanInfoInq(Infos.ObjCommon objCommon, ObjectIdentifier seasonID) {
    return seasonMethod.getSeason(seasonID);
  }

  /**
   * description:
   *
   * <p>change history: date defect person comments
   * ---------------------------------------------------------------------------------------------------------------------
   *
   * @param objCommon
   * @param recipeGroupID
   * @return com.fa.cim.dto.Infos.RecipeGroup
   * @throws
   * @author ho
   * @date 2020/5/27 13:53
   */
  @Override
  public RecipeGroup.Info sxRrecipeGroupInfoInq(
      Infos.ObjCommon objCommon, ObjectIdentifier recipeGroupID) {
    return seasonMethod.getRecipeGroup(recipeGroupID);
  }

  /**
   * description:
   *
   * <p>change history: date defect person comments
   * ---------------------------------------------------------------------------------------------------------------------
   *
   * @param objCommon
   * @return java.util.List<com.fa.cim.dto.Infos.RecipeGroup>
   * @throws
   * @author ho
   * @date 2020/6/5 10:13
   */
  @Override
  public List<RecipeGroup.Info> sxRecipeGroupInq(Infos.ObjCommon objCommon) {
    return seasonMethod.allRecipeGroup();
  }

  /**
   * description:
   *
   * <p>change history: date defect person comments
   * ---------------------------------------------------------------------------------------------------------------------
   *
   * @param objCommon
   * @param eqpID
   * @return java.util.List<com.fa.cim.dto.Infos.SeasonJob>
   * @throws
   * @author ho
   * @date 2020/5/27 13:58
   */
  @Override
  public List<Infos.SeasonJob> sxMachineSeasonJobInq(
      Infos.ObjCommon objCommon, ObjectIdentifier eqpID) {
    CimMachine machine = baseCoreFactory.getBO(CimMachine.class, eqpID);
    List<CimSeason> seasonList = machine.allSeasons();
    if (CimArrayUtils.isEmpty(seasonList)) {
      return null;
    }
    List<Infos.SeasonJob> seasonJobList = new ArrayList<>();
    for (CimSeason season : seasonList) {
      CimSeasonJob seasonJob = season.findSeasonJobNamed();
      if (seasonJob == null) {
        continue;
      }
      if (CimStringUtils.equals(
              BizConstant.SEASONJOB_STATUS_ABORTED, seasonJob.getSeasonJobStatus())
          || CimStringUtils.equals(
              BizConstant.SEASONJOB_STATUS_COMPLETED, seasonJob.getSeasonJobStatus())) {
        continue;
      }
      seasonJobList.add(new Infos.SeasonJob(seasonJob.getSeasonJobInfo()));
    }
    return seasonJobList;
  }

  /**
   * description:
   *
   * <p>change history: date defect person comments
   * ---------------------------------------------------------------------------------------------------------------------
   *
   * @param objCommon
   * @param seasonID
   * @return com.fa.cim.dto.Infos.SeasonJob
   * @throws
   * @author ho
   * @date 2020/5/26 11:11
   */
  @Override
  public Infos.SeasonJob sxSeasonJobInfoInq(Infos.ObjCommon objCommon, ObjectIdentifier seasonID) {
    CimSeason cimSeason = baseCoreFactory.getBO(CimSeason.class, seasonID);
    CimSeasonJob seasonJobNamed = cimSeason.findSeasonJobNamed();
    return new Infos.SeasonJob(seasonJobNamed.getSeasonJobInfo());
  }

  /**
   * description:
   *
   * <p>change history: date defect person comments
   * ---------------------------------------------------------------------------------------------------------------------
   *
   * @param objCommon
   * @param seasonWhatNextLotListParams
   * @return void
   * @throws
   * @author ho
   * @date 2020/6/11 10:48
   */
  @Override
  public Results.SeasonWhatNextLotListResult sxSeasonWhatNextInq(
      Infos.ObjCommon objCommon, Params.SeasonWhatNextLotListParams seasonWhatNextLotListParams) {
    Results.SeasonWhatNextLotListResult seasonWhatNextLotListResult =
        new Results.SeasonWhatNextLotListResult();

    Results.WhatNextLotListResult whatNextLotListResult =
        dispatchInqService.sxWhatNextLotListInfo(
            objCommon, seasonWhatNextLotListParams.getWhatNextLotListParams());
    List<Infos.WhatNextAttributes> lotListResultStrWhatNextAttributes =
        whatNextLotListResult.getStrWhatNextAttributes();
    if (CimArrayUtils.isEmpty(lotListResultStrWhatNextAttributes)) {
      if (whatNextLotListResult.getWhatNextAttributesPage() == null) {
        return seasonWhatNextLotListResult;
      }
      lotListResultStrWhatNextAttributes =
          whatNextLotListResult.getWhatNextAttributesPage().getContent();
    }

    seasonWhatNextLotListResult.setEquipmentID(whatNextLotListResult.getEquipmentID());
    seasonWhatNextLotListResult.setEquipmentCategory(whatNextLotListResult.getEquipmentCategory());
    seasonWhatNextLotListResult.setLastRecipeID(whatNextLotListResult.getLastRecipeID());
    seasonWhatNextLotListResult.setDispatchRule(whatNextLotListResult.getDispatchRule());
    seasonWhatNextLotListResult.setProcessRunSizeMaximum(
        whatNextLotListResult.getProcessRunSizeMaximum());
    seasonWhatNextLotListResult.setProcessRunSizeMinimum(
        whatNextLotListResult.getProcessRunSizeMinimum());

    if (CimArrayUtils.isEmpty(lotListResultStrWhatNextAttributes)) {
      return seasonWhatNextLotListResult;
    }

    Infos.Season season = seasonWhatNextLotListParams.getSeason();
    List<Infos.WhatNextAttributes> seasonWhatAttributes = new ArrayList<>();
    seasonWhatNextLotListResult.setSeasonWhatNextAttributes(seasonWhatAttributes);
    List<Infos.WhatNextAttributes> productWhatAttributes = new ArrayList<>();
    seasonWhatNextLotListResult.setProductWhatNextAttributes(productWhatAttributes);

    ObjectIdentifier equipmentID = seasonWhatNextLotListParams.getWhatNextLotListParams().getEquipmentID();
    ObjectIdentifier lastRecipe =
        seasonMethod.getLastMachineRecipeForMachine(
            objCommon, seasonWhatNextLotListParams.getWhatNextLotListParams().getEquipmentID());

    // bug-2650 get productID from seasonJob,if seasonJob not exist,get  it from the first of SeasonPlan`s ProductsList
    ObjectIdentifier seasonProductID = null;
    List<Infos.SeasonJob> machineSeasonJob = seasonMethod.getMachineSeasonJob(objCommon, equipmentID);
    
    List<ObjectIdentifier> seasonProdcutIDs = machineSeasonJob.stream()
            .filter(seasonJob -> season.getSeasonID()
                    .equals(seasonJob.getSeasonID()))
            .map(Infos.SeasonJob::getSeasonProductID)
            .collect(Collectors.toList());

    if (CimArrayUtils.isNotEmpty(seasonProdcutIDs)) {
      seasonProductID = seasonProdcutIDs.get(0);
    } else {
      seasonProductID = season.getSeasonProducts().get(0).getProductID();
    }
    for (Infos.WhatNextAttributes lotListResultStrWhatNextAttribute :
        lotListResultStrWhatNextAttributes) {
      String subLotType =
          lotMethod.lotSubLotTypeGetDR(objCommon, lotListResultStrWhatNextAttribute.getLotID());
      //todo: need to validation waferCount ?
      /*&& lotListResultStrWhatNextAttribute.getTotalWaferCount() >= seasonProduct.getQuantity()*/
      if (CimStringUtils.equals(subLotType, BizConstant.SEASON_LOT_TYPE)) {
          if (ObjectIdentifier.equalsWithValue(
                  seasonProductID, lotListResultStrWhatNextAttribute.getProductID())) {
            seasonWhatAttributes.add(lotListResultStrWhatNextAttribute);
          }
      }

      if (seasonMethod.productWhatAttributes(
          season, lotListResultStrWhatNextAttribute, lastRecipe)) {
        productWhatAttributes.add(lotListResultStrWhatNextAttribute);
      }
    }

    return seasonWhatNextLotListResult;
  }

  /**
   * description:
   *
   * <p>change history: date defect person comments
   * ---------------------------------------------------------------------------------------------------------------------
   *
   * @param objCommon
   * @param seasonLotsMoveInReserveInfoInqParams
   * @return com.fa.cim.dto.Results.SeasonLotsMoveInReserveInfoInqResult
   * @throws
   * @author ho
   * @date 2020/7/28 15:06
   */
  @Override
  public Results.SeasonLotsMoveInReserveInfoInqResult sxSeasonLotsMoveInReserveInfoInq(
      Infos.ObjCommon objCommon,
      Params.SeasonLotsMoveInReserveInfoInqParams seasonLotsMoveInReserveInfoInqParams) {
    Results.SeasonLotsMoveInReserveInfoInqResult seasonLotsMoveInReserveInfoInqResult =
        new Results.SeasonLotsMoveInReserveInfoInqResult();
    seasonLotsMoveInReserveInfoInqResult.setEquipmentID(
        seasonLotsMoveInReserveInfoInqParams.getEquipmentID());

    // step-1 查询season startCassette
    Params.LotsMoveInReserveInfoInqParams lotsMoveInReserveInfoInqParams =
        new Params.LotsMoveInReserveInfoInqParams();
    lotsMoveInReserveInfoInqParams.setEquipmentID(
        seasonLotsMoveInReserveInfoInqParams.getEquipmentID());
    lotsMoveInReserveInfoInqParams.setUser(seasonLotsMoveInReserveInfoInqParams.getUser());
    lotsMoveInReserveInfoInqParams.setStartCassettes(
        seasonLotsMoveInReserveInfoInqParams.getSeasonStartCassettes());
    Results.LotsMoveInReserveInfoInqResult lotsMoveInReserveInfoInqResult =
        lotInqService.sxLotsMoveInReserveInfoInq(objCommon, lotsMoveInReserveInfoInqParams);
    seasonLotsMoveInReserveInfoInqResult.setSeasonStrStartCassette(
        lotsMoveInReserveInfoInqResult.getStrStartCassette());

    if (CimArrayUtils.isEmpty(seasonLotsMoveInReserveInfoInqParams.getProductStartCassettes())) {
      return seasonLotsMoveInReserveInfoInqResult;
    }

    // step-2 查询 product startCassette
    lotsMoveInReserveInfoInqParams = new Params.LotsMoveInReserveInfoInqParams();
    lotsMoveInReserveInfoInqParams.setEquipmentID(
        seasonLotsMoveInReserveInfoInqParams.getEquipmentID());
    lotsMoveInReserveInfoInqParams.setUser(seasonLotsMoveInReserveInfoInqParams.getUser());
    lotsMoveInReserveInfoInqParams.setStartCassettes(
        seasonLotsMoveInReserveInfoInqParams.getProductStartCassettes());
    lotsMoveInReserveInfoInqResult =
        lotInqService.sxLotsMoveInReserveInfoInq(objCommon, lotsMoveInReserveInfoInqParams);
    seasonLotsMoveInReserveInfoInqResult.setProductStrStartCassette(
        lotsMoveInReserveInfoInqResult.getStrStartCassette());

    return seasonLotsMoveInReserveInfoInqResult;
  }

  /**
   * description:
   *
   * <p>change history: date defect person comments
   * ---------------------------------------------------------------------------------------------------------------------
   *
   * @param objCommon
   * @param seasonLotsMoveInReserveInfoForIBInqParams
   * @return com.fa.cim.dto.Results.SeasonLotsMoveInReserveInfoInqResult
   * @throws
   * @author ho
   * @date 2020/7/28 15:06
   */
  @Override
  public Results.SeasonLotsMoveInReserveInfoInqResult sxSeasonLotsMoveInReserveInfoForIBInq(
      Infos.ObjCommon objCommon,
      Params.SeasonLotsMoveInReserveInfoForIBInqParams seasonLotsMoveInReserveInfoForIBInqParams) {
    Results.SeasonLotsMoveInReserveInfoInqResult seasonLotsMoveInReserveInfoInqResult =
        new Results.SeasonLotsMoveInReserveInfoInqResult();
    seasonLotsMoveInReserveInfoInqResult.setEquipmentID(
        seasonLotsMoveInReserveInfoForIBInqParams.getEquipmentID());

    // step-1 查询season startCassette
    Results.LotsMoveInReserveInfoInqResult lotsMoveInReserveInfoInqResult =
        dispatchInqService.sxLotsMoveInReserveInfoForIBInq(
            objCommon,
            seasonLotsMoveInReserveInfoForIBInqParams.getEquipmentID(),
            seasonLotsMoveInReserveInfoForIBInqParams.getSeasonStartCassettes());
    seasonLotsMoveInReserveInfoInqResult.setSeasonStrStartCassette(
        lotsMoveInReserveInfoInqResult.getStrStartCassette());

    if (seasonLotsMoveInReserveInfoForIBInqParams.getProductStartCassettes() == null) {
      return seasonLotsMoveInReserveInfoInqResult;
    }

    // step-2 查询 product startCassette
    lotsMoveInReserveInfoInqResult =
        dispatchInqService.sxLotsMoveInReserveInfoForIBInq(
            objCommon,
            seasonLotsMoveInReserveInfoForIBInqParams.getEquipmentID(),
            seasonLotsMoveInReserveInfoForIBInqParams.getProductStartCassettes());
    seasonLotsMoveInReserveInfoInqResult.setProductStrStartCassette(
        lotsMoveInReserveInfoInqResult.getStrStartCassette());

    return seasonLotsMoveInReserveInfoInqResult;
  }
}
