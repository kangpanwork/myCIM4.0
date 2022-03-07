package com.fa.cim.method;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.dto.Infos;
import com.fa.cim.newcore.dto.rcpgrp.RecipeGroup;

import java.sql.Timestamp;
import java.util.List;


/**
 * description:
 * <p></p>
 * change history:
 * date             defect             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * @exception
 * @author ho
 * @date 2020/5/20 14:18
 */
public interface ISeasonMethod {

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
      * @param seasonProduct
     * @return void
     * @exception
     * @author ho
     * @date 2020/5/20 14:41
     */
    void checkSeasonProduct(ObjectIdentifier seasonProduct);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
      * @param seasonParam
     * @return void
     * @exception
     * @author ho
     * @date 2020/5/20 14:45
     */
    void checkIdleParam(Infos.Season seasonParam);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
      * @param seasonParam
     * @return void
     * @exception
     * @author ho
     * @date 2020/7/29 13:35
     */
    void checkRecipeIdleParam(Infos.Season seasonParam);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
      * @param seasonParam
     * @return void
     * @exception
     * @author ho
     * @date 2020/5/20 14:45
     */
    void checkIntervalParam(Infos.Season seasonParam);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
      * @param seasonParam
     * @return void
     * @exception
     * @author ho
     * @date 2020/5/20 14:45
     */
    void checkRecipeGroupParam(Infos.Season seasonParam);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
      * @param seasonParam
     * @return void
     * @exception
     * @author ho
     * @date 2020/5/20 14:45
     */
    void checkPMParam(Infos.Season seasonParam);


    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
      * @param season
     * @return void
     * @exception
     * @author ho
     * @date 2020/5/20 14:45
     */
    void checkSeason(Infos.Season season);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
      * @param objCommon
     * @param startCassetteList
     * @return void
     * @exception
     * @author ho
     * @date 2020/5/26 10:51
     */
    boolean checkIfSeasonLot(Infos.ObjCommon objCommon, List<Infos.StartCassette> startCassetteList);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
      * @param recipeGroupID
     * @return com.fa.cim.dto.Infos.RecipeGroup
     * @exception
     * @author ho
     * @date 2020/5/21 10:37
     */
    RecipeGroup.Info getRecipeGroup(ObjectIdentifier recipeGroupID);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
      * @param
     * @return java.util.List<com.fa.cim.dto.Infos.RecipeGroup>
     * @exception
     * @author ho
     * @date 2020/6/5 10:13
     */
    List<RecipeGroup.Info> allRecipeGroup();

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @param recipeInfos
     * @return void
     * @exception
     * @author ho
     * @date 2020/5/25 14:37
     */
    RecipeGroup.Info createRecipeGroup(List<ObjectIdentifier> recipeInfos);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
      * @param recipeGroup
     * @return com.fa.cim.dto.Infos.RecipeGroup
     * @exception
     * @author ho
     * @date 2020/6/2 15:12
     */
    RecipeGroup.Info createRecipeGroup(RecipeGroup.Info recipeGroup);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
      * @param recipeGroup
     * @return void
     * @exception
     * @author ho
     * @date 2020/5/25 14:43
     */
    void updateRecipeGroup(RecipeGroup.Info recipeGroup);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
      * @param recipeGroup
     * @return void
     * @exception
     * @author ho
     * @date 2020/5/25 14:46
     */
    void deleteRecipeGroup(RecipeGroup.Info recipeGroup);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
      * @param seasonID
     * @return com.fa.cim.dto.Infos.Season
     * @exception
     * @author ho
     * @date 2020/5/21 11:13
     */
    Infos.Season getSeason(ObjectIdentifier seasonID);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
      * @param seasons
     * @return void
     * @exception
     * @author ho
     * @date 2020/5/21 11:17
     */
    void deleteSeasonPlan(List<Infos.Season> seasons);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
      * @param eqpID
     * @return java.util.List<com.fa.cim.dto.Infos.Season>
     * @exception
     * @author ho
     * @date 2020/5/21 12:44
     */
    List<Infos.Season> querySeason(ObjectIdentifier eqpID);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
      * @param eqpID
     * @return java.util.List<java.lang.String>
     * @exception
     * @author ho
     * @date 2020/6/23 11:10
     */
    List<String> getAllSeasonProduct(ObjectIdentifier eqpID);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
      * @param eqpID
     * @return java.util.List<java.lang.String>
     * @exception
     * @author ho
     * @date 2020/6/23 11:10
     */
    List<String> getAllSeasonProductRecipe(ObjectIdentifier eqpID);

    /**
     * description: 检查设备是否符合SeasonPlan条件
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
      * @param eqpID 设备ID必须
     * @param chamberID 可以为null
     * @param recipeID  可以为null
     * @param productID 可以为null
     * @return void
     * @exception
     * @author ho
     * @date 2020/5/21 16:30
     */
    List<Infos.Season> checkMachineWhetherMeetsConditionsForSeason(ObjectIdentifier eqpID, String chamberID, String recipeID, String productID);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
      * @param eqpID
     * @param recipeID
     * @param productID
     * @return java.util.List<com.fa.cim.dto.Infos.Season>
     * @exception
     * @author ho
     * @date 2020/5/25 16:55
     */
    List<Infos.Season> checkMachineWhetherMeetsConditionsForSeason(Infos.ObjCommon objCommon,ObjectIdentifier eqpID, String recipeID, String productID);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
      * @param objCommon
     * @param startCassetteList
     * @return java.util.List<com.fa.cim.dto.Infos.Season>
     * @exception
     * @author ho
     * @date 2020/5/25 17:21
     */
    List<Infos.Season> checkMachineWhetherMeetsConditionsForSeason(Infos.ObjCommon objCommon,ObjectIdentifier eqpID,List<Infos.StartCassette> startCassetteList);

    /**
     * description: 创建SeasonJob 状态Requested
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
      * @param season
     * @return void
     * @exception
     * @author ho
     * @date 2020/5/22 14:33
     */
    Infos.SeasonJob createSeasonJob(Infos.ObjCommon objCommon, Infos.Season season, String claimMemo);

    /**
     * description: seasoning检查逻辑
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
      * @param objCommon
     * @param equipmentID
     * @param startCassetteList
     * @return void
     * @exception
     * @author ho
     * @date 2020/5/26 14:07
     */
    void checkSeasonAndCreateSeasonJobForOperation(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID,
                                                   List<Infos.StartCassette> startCassetteList,String seasonJobStatus,String claimMemo);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
      * @param objCommon
     * @param seasonID
     * @return com.fa.cim.dto.Infos.SeasonJob
     * @exception
     * @author ho
     * @date 2020/5/26 11:14
     */
    Infos.SeasonJob getSeasonJob(Infos.ObjCommon objCommon, ObjectIdentifier seasonID);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
      * @param objCommon
     * @param eqpID
     * @return java.util.List<com.fa.cim.dto.Infos.SeasonJob>
     * @exception
     * @author ho
     * @date 2020/5/27 13:58
     */
    List<Infos.SeasonJob> getMachineSeasonJob(Infos.ObjCommon objCommon,ObjectIdentifier eqpID);

    /**
     * description: 更新seasonJob状态
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @param seasonJob
     * @param seasonJobStatus
     * @return void
     * @exception
     * @author ho
     * @date 2020/5/25 10:23
     */
    void updateSeasonJob(Infos.SeasonJob seasonJob,String seasonJobStatus);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
      * @param recipeID
     * @param eqpID
     * @return java.util.List<com.fa.cim.dto.Infos.Season>
     * @exception
     * @author ho
     * @date 2020/6/19 13:23
     */
    List<Infos.Season> checkTheActiveSeason(String recipeID,ObjectIdentifier eqpID,ObjectIdentifier lotID,
                                            ObjectIdentifier mRecipeID, ObjectIdentifier lRecipeID);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
      * @param season
     * @return void
     * @exception
     * @author ho
     * @date 2020/5/25 15:00
     */
    void createSeason(Infos.Season season);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
      * @param season
     * @return void
     * @exception
     * @author ho
     * @date 2020/5/25 15:06
     */
    void updateSeason(Infos.Season season);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
      * @param eqpID
     * @return java.util.List<com.fa.cim.dto.Infos.Season>
     * @exception
     * @author ho
     * @date 2020/5/21 17:24
     */
    List<Infos.Season> checkTheActiveSeason(ObjectIdentifier eqpID,String recipeID,ObjectIdentifier lotID,
                                            ObjectIdentifier lRecipeID);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
      * @param eqpID
     * @return java.util.List<com.fa.cim.dto.Infos.Season>
     * @exception
     * @author ho
     * @date 2020/6/9 18:04
     */
    List<Infos.Season> getMachineSeasonForPM(ObjectIdentifier eqpID);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
      * @param objCommon
     * @param startCassetteList
     * @param eqpID
     * @return boolean
     * @exception
     * @author ho
     * @date 2020/6/15 16:39
     */
    void checkSeasonForMoveInReserve(Infos.ObjCommon objCommon, List<Infos.StartCassette> startCassetteList, ObjectIdentifier eqpID);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
      * @param objCommon
     * @param equipmentID
     * @param cassetteID
     * @return boolean
     * @exception
     * @author ho
     * @date 2020/6/16 15:17
     */
    void checkSeasonForLoading(Infos.ObjCommon objCommon,ObjectIdentifier equipmentID,ObjectIdentifier cassetteID);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
      * @param objCommon
     * @param equipmentID
     * @param cassetteID
     * @return boolean
     * @exception
     * @author ho
     * @date 2020/6/16 15:30
     */
    boolean checkSeasonForCassette(Infos.ObjCommon objCommon,ObjectIdentifier equipmentID,ObjectIdentifier cassetteID);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
      * @param objCommon
     * @param equipmentID
     * @param cassetteID
     * @return boolean
     * @exception
     * @author ho
     * @date 2020/6/16 15:32
     */
    boolean checkSeasonForSeasonJob(Infos.ObjCommon objCommon,ObjectIdentifier equipmentID,ObjectIdentifier cassetteID);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
      * @param objCommon
     * @param equipmentID
     * @param startCassetteList
     * @return boolean
     * @exception
     * @author ho
     * @date 2020/6/17 17:23
     */
    void checkSeasonForMoveIn(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, List<Infos.StartCassette> startCassetteList);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
      * @param objCommon
     * @param equipmentID
     * @return void
     * @exception
     * @author ho
     * @date 2020/6/18 10:20
     */
    void deleteSeasonJobForMachine(Infos.ObjCommon objCommon,ObjectIdentifier equipmentID);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
      * @param objCommon
     * @param seasonJob
     * @return void
     * @exception
     * @author ho
     * @date 2020/6/18 10:37
     */
    void deleteSeasonJob(Infos.ObjCommon objCommon, Infos.SeasonJob seasonJob);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
      * @param objCommon
     * @param equipmentID
     * @return void
     * @exception
     * @author ho
     * @date 2020/6/19 17:13
     */
    ObjectIdentifier getLastMachineRecipeForMachine(Infos.ObjCommon objCommon,ObjectIdentifier equipmentID);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
      * @param objCommon
     * @param equipmentID
     * @param cassetteID
     * @return void
     * @exception
     * @author ho
     * @date 2020/6/20 11:22
     */
    void checkSeasonForSeasonLot(Infos.ObjCommon objCommon,ObjectIdentifier equipmentID, ObjectIdentifier cassetteID);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
      * @param objCommon
     * @param equipmentID
     * @param season
     * @return java.util.List<com.fa.cim.dto.Infos.Season>
     * @exception
     * @author ho
     * @date 2020/6/20 14:24
     */
    Infos.EqpChamberStatusInfo checkSeasonForChamber(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, Infos.Season season,
                                                     ObjectIdentifier lotID, ObjectIdentifier mRecipeID, ObjectIdentifier lRecipeID);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
      * @param objCommon
     * @param recipeID
     * @param seasons
     * @return void
     * @exception
     * @author ho
     * @date 2020/6/20 14:43
     */
    List<Infos.Season> checkSeasonForRecipe(Infos.ObjCommon objCommon, String recipeID, List<Infos.Season> seasons);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
      * @param eqpID
     * @param season
     * @return java.sql.Timestamp
     * @exception
     * @author ho
     * @date 2020/7/1 12:53
     */
    Timestamp getLastSBYTime(ObjectIdentifier eqpID, Infos.Season season);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
      * @param eqpID
     * @return java.sql.Timestamp
     * @exception
     * @author ho
     * @date 2020/7/1 13:18
     */
    Timestamp getLastSeasonTime(ObjectIdentifier eqpID);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
      * @param chamberStatusInfo
     * @return java.sql.Timestamp
     * @exception
     * @author ho
     * @date 2020/7/8 10:18
     */
    Timestamp getLastSBYTime(Infos.EqpChamberStatusInfo chamberStatusInfo);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
      * @param objCommonIn
     * @param equipmentId
     * @param equipmentStatusCode
     * @param claimMemo
     * @return void
     * @exception
     * @author ho
     * @date 2020/7/27 14:08
     */
    void makeSeasonForPM(Infos.ObjCommon objCommonIn, ObjectIdentifier equipmentId, ObjectIdentifier equipmentStatusCode, String claimMemo);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @param objCommon
     * @param equipmentId
     * @param eqpStatusCode
     * @param claimMemo
     * @return void
     * @exception
     * @author ho
     * @date 2020/7/27 14:08
     */
    void createSeasonJobForPM(Infos.ObjCommon objCommon, ObjectIdentifier equipmentId,
                              ObjectIdentifier eqpStatusCode, String claimMemo);

    /**
     * 为保养创建一个SeasonJob
     * @param objCommon
     * @param equipmentId
     * @param eqpStatusCode
     * @param eqpStatusInfo
     * @param claimMemo
     * @version 0.1
     * @author Grant
     * @date 2021/7/20
     */
    void createSeasonJobForPM(Infos.ObjCommon objCommon, ObjectIdentifier equipmentId, ObjectIdentifier eqpStatusCode,
                              Infos.EqpStatusInfo eqpStatusInfo, String claimMemo);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
      * @param objCommon
     * @param startCassetteList
     * @return void
     * @exception
     * @author ho
     * @date 2020/7/29 15:57
     */
    void updateMachineRecipeUsedTime(Infos.ObjCommon objCommon, List<Infos.StartCassette> startCassetteList,ObjectIdentifier equipmentID);

    boolean productWhatAttributes(Infos.Season season, Infos.WhatNextAttributes whatNextAttributes, ObjectIdentifier lastRecipeID);
}
