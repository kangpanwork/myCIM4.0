package com.fa.cim.service.season.impl;

import com.fa.cim.annotaion.OmService;
import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.utils.*;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.config.RetCodeConfigEx;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;
import com.fa.cim.method.*;
import com.fa.cim.newcore.bo.machine.CimMachine;
import com.fa.cim.newcore.bo.recipe.CimRecipeGroup;
import com.fa.cim.newcore.bo.season.CimSeason;
import com.fa.cim.newcore.bo.season.CimSeasonJob;
import com.fa.cim.newcore.dto.rcpgrp.RecipeGroup;
import com.fa.cim.service.dispatch.IDispatchService;
import com.fa.cim.service.season.ISeasoningService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * description:
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/9/8                              Neyo                create file
 *
 * @author: Neyo
 * @date: 2020/9/8 16:58
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@OmService
@Slf4j
public class SeasoningServiceImpl implements ISeasoningService {

    @Autowired
    private RetCodeConfigEx retCodeConfigEx;

    @Autowired
    private RetCodeConfig retCodeConfig;

    @Autowired
    private ISeasonMethod seasonMethod;

    @Autowired
    private IEventMethod eventMethod;

    @Autowired
    private ICassetteMethod cassetteMethod;

    @Autowired
    private ILotMethod lotMethod;

    @Autowired
    private IControlJobMethod controlJobMethod;

    @Autowired
    private IProcessMethod processMethod;

    @Autowired
    private IDispatchService dispatchService;

    @Autowired
    private IObjectLockMethod objectLockMethod;


    /**
     * description: 用于创建按season plan.
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param season
     * @return void
     * @throws
     * @author ho
     * @date 2020/5/20 13:08
     */
    @Override
    public void sxSeasonPlanCreateReq(Infos.ObjCommon objCommon, Infos.Season season, String claimMemo) {
        // 检查season配置数据是否合理
        seasonMethod.checkSeason(season);
        // 设置season默认参数
        season.setUserID(objCommon.getUser().getUserID());
        season.setStatus(BizConstant.SEASON_STATUS_ACTIVE);
        season.setCreateTime(objCommon.getTimeStamp().getReportTimeStamp());
        season.setLastModifyTime(objCommon.getTimeStamp().getReportTimeStamp());
        // 生成sequence No.不对chamber做检查
        AtomicInteger seqNo = new AtomicInteger();
        Optional.ofNullable(season.getChambers()).ifPresent(seasonChambers -> {
            seasonChambers.forEach(
                    seasonChamber -> seasonChamber.setSeqNo(seqNo.getAndIncrement()));
        });
        // 生成sequence No.不对product recipe做检查
        seqNo.set(0);
        Optional.ofNullable(season.getRecipes()).ifPresent(seasonProdRecipes -> seasonProdRecipes.forEach(
                seasonProdRecipe -> seasonProdRecipe.setSeqNo(seqNo.getAndIncrement())
        ));
        // 生成sequence No.并检查seasonProduct.错误的season product将不能seasoning.
        seqNo.set(0);
        season.getSeasonProducts().forEach(seasonProduct -> {
            seasonMethod.checkSeasonProduct(seasonProduct.getProductID());
            seasonProduct.setSeqNo(seqNo.getAndIncrement());
        });

        seasonMethod.createSeason(season);

        // SeasonPlan MakeEvent
        eventMethod.seasonPlanEventMake(objCommon, BizConstant.SEASONPLAN_ACTION_CREATE, season, claimMemo);

    }

    /**
     * description: 提供season plan编辑功能
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param seasons
     * @return void
     * @throws
     * @author ho
     * @date 2020/5/21 10:31
     */
    @Override
    public void sxUpdateSeasonPlan(Infos.ObjCommon objCommon, List<Infos.Season> seasons) {
        if (CimArrayUtils.isEmpty(seasons)){
            return;
        }
        seasons.stream().forEach(season -> {
            objectLockMethod.objectLock(objCommon, CimSeason.class,season.getSeasonID());
        });

        Optional.ofNullable(seasons).ifPresent(seasons1 -> seasons1.forEach(season -> {
            Validations.check(seasonMethod.getSeason(season.getSeasonID()) == null,
                    retCodeConfigEx.getSeasonNoExist());
            seasonMethod.checkSeason(season);
            season.setLastModifyTime(objCommon.getTimeStamp().getReportTimeStamp());
            season.setUserID(objCommon.getUser().getUserID());
            seasonMethod.updateSeason(season);
        }));
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param seasons
     * @return void
     * @throws
     * @author ho
     * @date 2020/5/21 11:15
     */
    @Override
    public void sxSeasonPlansDeleteReq(Infos.ObjCommon objCommon, List<Infos.Season> seasons, String claimMemo) {
        seasonMethod.deleteSeasonPlan(seasons);

        // Season Plan MakeEvent
        Optional.ofNullable(seasons).ifPresent(seasons1 -> {
            seasons1.forEach(season -> {
                eventMethod.seasonPlanEventMake(objCommon, BizConstant.SEASONPLAN_ACTION_DELETE, season, claimMemo);
            });
        });
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param season
     * @return void
     * @throws
     * @author ho
     * @date 2020/5/21 11:17
     */
    @Override
    public void sxSeasonPlanDeleteReq(Infos.ObjCommon objCommon, Infos.Season season, String claimMemo) {
        seasonMethod.deleteSeasonPlan(CimArrayUtils.generateList(season));

        // season plan MakeEvent
        eventMethod.seasonPlanEventMake(objCommon, BizConstant.SEASONPLAN_ACTION_DELETE, season, claimMemo);
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param recipeGroup
     * @param claimMemo
     * @return void
     * @throws
     * @author ho
     * @date 2020/5/27 12:54
     */
    @Override
    public void sxSeasonRecipeGroupCreateReq(Infos.ObjCommon objCommon, RecipeGroup.Info recipeGroup, String claimMemo) {
        seasonMethod.createRecipeGroup(recipeGroup);
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param recipeGroup
     * @param claimMemo
     * @return void
     * @throws
     * @author ho
     * @date 2020/5/27 13:03
     */
    @Override
    public void sxSeasonRecipeGroupModifyReq(Infos.ObjCommon objCommon, RecipeGroup.Info recipeGroup, String claimMemo) {
        objectLockMethod.objectLock(objCommon, CimRecipeGroup.class,recipeGroup.getRecipeGroupID());

        seasonMethod.updateRecipeGroup(recipeGroup);
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param recipeGroup
     * @param claimMemo
     * @return void
     * @throws
     * @author ho
     * @date 2020/5/27 13:04
     */
    @Override
    public void sxSeasonRecipeGroupDeleteReq(Infos.ObjCommon objCommon, RecipeGroup.Info recipeGroup, String claimMemo) {
        seasonMethod.deleteRecipeGroup(recipeGroup);
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param seasonJob
     * @param seasonJobStatus
     * @return void
     * @throws
     * @author ho
     * @date 2020/5/28 13:36
     */
    @Override
    public void sxUpdateSeasonJob(Infos.ObjCommon objCommon, Infos.SeasonJob seasonJob, String seasonJobStatus, String claimMemo) {
        objectLockMethod.objectLock(objCommon, CimSeasonJob.class,seasonJob.getSeasonJobID());

        seasonMethod.updateSeasonJob(seasonJob, seasonJobStatus);

        // season job MakeEvent
        eventMethod.seasonJobEventMake(objCommon, BizConstant.SEASONJOB_ACTION_MODIFY, seasonJob, claimMemo);
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param equipmentID
     * @param startCassetteList
     * @return void
     * @throws
     * @author ho
     * @date 2020/6/9 16:06
     */
    @Override
    public void seasonUpdateForMoveOut(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, List<Infos.StartCassette> startCassetteList) {
        if (!seasonMethod.checkIfSeasonLot(objCommon, startCassetteList)) {
            return;
        }
        List<Infos.SeasonJob> machineSeasonJobList = seasonMethod.getMachineSeasonJob(objCommon, equipmentID);
        if (CimArrayUtils.isEmpty(machineSeasonJobList)) {
            return;
        }
        for (Infos.SeasonJob seasonJob : machineSeasonJobList) {
            seasonMethod.updateSeasonJob(seasonJob, BizConstant.SEASONJOB_STATUS_COMPLETED);
            Infos.Season season = seasonMethod.getSeason(seasonJob.getSeasonID());
            season.setLastSeasonTime(objCommon.getTimeStamp().getReportTimeStamp());
            season.setPmFlag(null);
            seasonMethod.updateSeason(season);
        }
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param eqpID
     * @param claimMemo
     * @return void
     * @throws
     * @author ho
     * @date 2020/6/9 18:05
     */
    @Override
    public void seasonJobCreateForPM(Infos.ObjCommon objCommon, ObjectIdentifier eqpID, String claimMemo) {
        List<Infos.Season> pmSeasonList = seasonMethod.getMachineSeasonForPM(eqpID);
        if (pmSeasonList == null) {
            return;
        }
        for (Infos.Season season : pmSeasonList) {
            season.setPmFlag(true);
            seasonMethod.updateSeason(season);
        }
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param seasonLotMoveInReserveReqParams
     * @return com.fa.cim.dto.Results.SeasonLotMoveInReserveReqResult
     * @throws
     * @author ho
     * @date 2020/6/10 14:06
     */
    @Override
    public Results.SeasonLotMoveInReserveReqResult sxSeasonLotMoveInReserveReq(Infos.ObjCommon objCommon, Params.SeasonLotMoveInReserveReqParams seasonLotMoveInReserveReqParams) {
        Results.SeasonLotMoveInReserveReqResult result = new Results.SeasonLotMoveInReserveReqResult();

        List<Infos.Season> seasonList = seasonMethod.querySeason(seasonLotMoveInReserveReqParams.getEquipmentID());
        Validations.check(CimArrayUtils.isEmpty(seasonList), retCodeConfigEx.getSeasonNoExist());
        if (seasonLotMoveInReserveReqParams.getProductLotParams() == null) {
            boolean flag = seasonList.stream().anyMatch(season -> CimBooleanUtils.isFalse(season.getParam().getSeasonGroupFlag()));
            Validations.check(!flag, retCodeConfigEx.getProductLotRequired());
        } else {
            List<Infos.StartCassette> startCassetteList = processMethod.processStartReserveInformationGetByCassette(objCommon,
                    seasonLotMoveInReserveReqParams.getEquipmentID(),
                    CimArrayUtils.generateList(seasonLotMoveInReserveReqParams.getProductLotParams().getCassetteID()),
                    false);
            List<Infos.Season> activeSeasonList = seasonMethod.checkMachineWhetherMeetsConditionsForSeason(objCommon,
                    seasonLotMoveInReserveReqParams.getEquipmentID(),
                    startCassetteList);
            Validations.check(CimArrayUtils.isEmpty(activeSeasonList), retCodeConfigEx.getLotNotMeetSeason());
        }

        // step-1 season lot move in reserve
        Params.SeasonMoveInReserveParams moveInReserveParams = seasonLotMoveInReserveReqParams.getSeasonLotParams();
        Results.SeasonMoveInReserveReqResult moveInReserveReqResult = moveInReserveReq(objCommon, seasonLotMoveInReserveReqParams.getEquipmentID(), moveInReserveParams);
        result.setSeasonLotControlJobID(moveInReserveReqResult.getControlJobID());

        // step-2 product lot move in reserve
        moveInReserveParams = seasonLotMoveInReserveReqParams.getProductLotParams();
        if (moveInReserveParams == null) {
            return result;
        }
        moveInReserveReqResult = moveInReserveReq(objCommon, seasonLotMoveInReserveReqParams.getEquipmentID(), moveInReserveParams);
        result.setSeasonLotControlJobID(moveInReserveReqResult.getControlJobID());
        return result;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param seasonMoveInReserveReqParams
     * @return com.fa.cim.dto.Results.SeasonLotMoveInReserveReqResult
     * @throws
     * @author ho
     * @date 2020/6/10 14:06
     */
    @Override
    public Results.SeasonLotMoveInReserveReqResult sxSeasonMoveInReserveReq(Infos.ObjCommon objCommon, Params.SeasonMoveInReserveReqParams seasonMoveInReserveReqParams) {
        Results.SeasonLotMoveInReserveReqResult result = new Results.SeasonLotMoveInReserveReqResult();

        Infos.Season paramSeason = seasonMoveInReserveReqParams.getSeason();
        Validations.check(paramSeason == null, retCodeConfigEx.getSeasonNoExist());
        if (CimBooleanUtils.isTrue(paramSeason.getParam().getSeasonGroupFlag())) {
            Validations.check(seasonMoveInReserveReqParams.getProductLotParams() == null, retCodeConfigEx.getProductLotRequired());
        }

        // step-1 create season  job
        Infos.SeasonJob seasonJob = seasonMethod.createSeasonJob(objCommon, paramSeason, seasonMoveInReserveReqParams.getClaimMemo());

        // step-2 season lot move in reserve
        Params.SeasonMoveInReserveParams moveInReserveParams = seasonMoveInReserveReqParams.getSeasonLotParams();
        Results.SeasonMoveInReserveReqResult moveInReserveReqResult = moveInReserveReq(objCommon, seasonMoveInReserveReqParams.getEquipmentID(), moveInReserveParams);
        result.setSeasonLotControlJobID(moveInReserveReqResult.getControlJobID());
        seasonJob.setSeasonLotID(moveInReserveReqResult.getLotID());
        seasonJob.setSeasonCarrierID(moveInReserveParams.getCassetteID());
        seasonJob.setSeasonRcpID(moveInReserveReqResult.getRecipeID());

        // step-3 product lot move in reserve
        moveInReserveParams = seasonMoveInReserveReqParams.getProductLotParams();
        if (moveInReserveParams != null) {
            moveInReserveReqResult = moveInReserveReq(objCommon, seasonMoveInReserveReqParams.getEquipmentID(), moveInReserveParams);
            result.setSeasonLotControlJobID(moveInReserveReqResult.getControlJobID());
            seasonJob.setLotID(moveInReserveReqResult.getLotID());
            seasonJob.setCarrierID(moveInReserveParams.getCassetteID());
        }
        seasonMethod.updateSeasonJob(seasonJob, BizConstant.SEASONJOB_STATUS_RESERVED);
        return result;
    }


    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param equipmentID
     * @param moveInReserveParams
     * @return com.fa.cim.dto.Results.MoveInReserveReqResult
     * @throws
     * @author ho
     * @date 2020/6/10 14:26
     */
    @Override
    public Results.SeasonMoveInReserveReqResult moveInReserveReq(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, Params.SeasonMoveInReserveParams moveInReserveParams) {
        Results.SeasonMoveInReserveReqResult seasonMoveInReserveReqResult = new Results.SeasonMoveInReserveReqResult();
        List<Infos.StartCassette> startCassettes = processMethod.processStartReserveInformationGetByCassette(objCommon,
                equipmentID,
                CimArrayUtils.generateList(moveInReserveParams.getCassetteID()),
                false);
        Validations.check(CimArrayUtils.isEmpty(startCassettes), retCodeConfig.getInvalidInputParam());
        for (Infos.StartCassette startCassette : startCassettes) {
            startCassette.setLoadSequenceNumber(moveInReserveParams.getLoadSequenceNumber());
            startCassette.setLoadPurposeType(moveInReserveParams.getLoadPurposeType());
            startCassette.setLoadPortID(moveInReserveParams.getLoadPortID());
            startCassette.setUnloadPortID(moveInReserveParams.getUnloadPortID());
            List<Infos.LotInCassette> lotInCassetteList = startCassette.getLotInCassetteList();
            Validations.check(CimArrayUtils.isEmpty(lotInCassetteList), retCodeConfig.getCastIsEmpty());
            for (Infos.LotInCassette lotInCassette : lotInCassetteList) {
                seasonMoveInReserveReqResult.setLotID(lotInCassette.getLotID());
                seasonMoveInReserveReqResult.setProductID(lotInCassette.getProductID());
                seasonMoveInReserveReqResult.setRecipeID(lotInCassette.getStartRecipe().getMachineRecipeID());
                break;
            }
        }

        Params.MoveInReserveReqParams moveInReserveReqParams = new Params.MoveInReserveReqParams();
        moveInReserveReqParams.setUser(moveInReserveParams.getUser());
        moveInReserveReqParams.setEquipmentID(equipmentID);
        moveInReserveReqParams.setPortGroupID(moveInReserveParams.getPortGroupID());
        moveInReserveReqParams.setStartCassetteList(startCassettes);
        Results.MoveInReserveReqResult moveInReserveReqResult = (Results.MoveInReserveReqResult) dispatchService.sxMoveInReserveReq(objCommon, moveInReserveReqParams);
        seasonMoveInReserveReqResult.setControlJobID(moveInReserveReqResult.getControlJobID());
        return seasonMoveInReserveReqResult;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param moveInReserveParams
     * @return com.fa.cim.dto.Results.MoveInReserveReqResult
     * @throws
     * @author ho
     * @date 2020/6/10 14:26
     */
    @Override
    public Results.SeasonMoveInReserveReqResult moveInReserveReq(Infos.ObjCommon objCommon, Params.MoveInReserveReqParams moveInReserveParams) {
        Results.SeasonMoveInReserveReqResult seasonMoveInReserveReqResult = new Results.SeasonMoveInReserveReqResult();
        List<Infos.StartCassette> startCassettes = moveInReserveParams.getStartCassetteList();
        Validations.check(CimArrayUtils.isEmpty(startCassettes), retCodeConfig.getInvalidInputParam());
        List<String> strLots = new ArrayList<>();
        List<String> strRecipes = new ArrayList<>();
        List<String> strProducts = new ArrayList<>();
        List<String> strCassettes = new ArrayList<>();
        for (Infos.StartCassette startCassette : startCassettes) {
            List<Infos.LotInCassette> lotInCassetteList = startCassette.getLotInCassetteList();
            Validations.check(CimArrayUtils.isEmpty(lotInCassetteList), retCodeConfig.getCastIsEmpty());
            for (Infos.LotInCassette lotInCassette : lotInCassetteList) {
                if (CimBooleanUtils.isFalse(lotInCassette.getMoveInFlag())){
                    continue;
                }
                strLots.add(ObjectIdentifier.fetchValue(lotInCassette.getLotID()));
                strRecipes.add(ObjectIdentifier.fetchValue(lotInCassette.getStartRecipe().getMachineRecipeID()));
                strProducts.add(ObjectIdentifier.fetchValue(lotInCassette.getProductID()));
                strCassettes.add(ObjectIdentifier.fetchValue(startCassette.getCassetteID()));
            }
        }
        seasonMoveInReserveReqResult.setLotID(ObjectIdentifier.buildWithValue(CimStringUtils.join(strLots,BizConstant.SEASON_SEPARATOR)));
        seasonMoveInReserveReqResult.setProductID(ObjectIdentifier.buildWithValue(CimStringUtils.join(strProducts,BizConstant.SEASON_SEPARATOR)));
        seasonMoveInReserveReqResult.setRecipeID(ObjectIdentifier.buildWithValue(CimStringUtils.join(strRecipes,BizConstant.SEASON_SEPARATOR)));
        seasonMoveInReserveReqResult.setCassetteID(ObjectIdentifier.buildWithValue(CimStringUtils.join(strCassettes,BizConstant.SEASON_SEPARATOR)));
        Results.MoveInReserveReqResult moveInReserveReqResult = (Results.MoveInReserveReqResult) dispatchService.sxMoveInReserveReq(objCommon, moveInReserveParams);
        seasonMoveInReserveReqResult.setControlJobID(moveInReserveReqResult.getControlJobID());
        return seasonMoveInReserveReqResult;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param moveInReserveParams
     * @return com.fa.cim.dto.Results.MoveInReserveReqResult
     * @throws
     * @author ho
     * @date 2020/6/10 14:26
     */
    @Override
    public Results.SeasonMoveInReserveReqResult moveInReserveForIBReq(Infos.ObjCommon objCommon, Params.MoveInReserveForIBReqParams moveInReserveParams) {
        Results.SeasonMoveInReserveReqResult seasonMoveInReserveReqResult = new Results.SeasonMoveInReserveReqResult();
        List<Infos.StartCassette> startCassettes = moveInReserveParams.getStartCassetteList();
        Validations.check(CimArrayUtils.isEmpty(startCassettes), retCodeConfig.getInvalidInputParam());
        List<String> strLots = new ArrayList<>();
        List<String> strRecipes = new ArrayList<>();
        List<String> strProducts = new ArrayList<>();
        List<String> strCassettes = new ArrayList<>();
        for (Infos.StartCassette startCassette : startCassettes) {
            List<Infos.LotInCassette> lotInCassetteList = startCassette.getLotInCassetteList();
            Validations.check(CimArrayUtils.isEmpty(lotInCassetteList), retCodeConfig.getCastIsEmpty());
            for (Infos.LotInCassette lotInCassette : lotInCassetteList) {
                if (CimBooleanUtils.isFalse(lotInCassette.getMoveInFlag())){
                    continue;
                }
                strLots.add(ObjectIdentifier.fetchValue(lotInCassette.getLotID()));
                strRecipes.add(ObjectIdentifier.fetchValue(lotInCassette.getStartRecipe().getMachineRecipeID()));
                strProducts.add(ObjectIdentifier.fetchValue(lotInCassette.getProductID()));
                strCassettes.add(ObjectIdentifier.fetchValue(startCassette.getCassetteID()));
            }
        }
        seasonMoveInReserveReqResult.setLotID(ObjectIdentifier.buildWithValue(CimStringUtils.join(strLots,BizConstant.SEASON_SEPARATOR)));
        seasonMoveInReserveReqResult.setProductID(ObjectIdentifier.buildWithValue(CimStringUtils.join(strProducts,BizConstant.SEASON_SEPARATOR)));
        seasonMoveInReserveReqResult.setRecipeID(ObjectIdentifier.buildWithValue(CimStringUtils.join(strRecipes,BizConstant.SEASON_SEPARATOR)));
        seasonMoveInReserveReqResult.setCassetteID(ObjectIdentifier.buildWithValue(CimStringUtils.join(strCassettes,BizConstant.SEASON_SEPARATOR)));
        ObjectIdentifier moveInReserveReqResult = dispatchService.sxMoveInReserveForIBReq(objCommon, moveInReserveParams, moveInReserveParams.getApcifControlStatus());
        seasonMoveInReserveReqResult.setControlJobID(moveInReserveReqResult);
        return seasonMoveInReserveReqResult;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param moveInReserveReqForSeasonParams
     * @return com.fa.cim.dto.Results.SeasonMoveInReserveReqResult
     * @throws
     * @author ho
     * @date 2020/6/15 15:00
     */
    @Override
    public Results.SeasonLotMoveInReserveReqResult sxMoveInReserveReq(
            Infos.ObjCommon objCommon, Params.MoveInReserveReqForSeasonParams moveInReserveReqForSeasonParams) {
        objCommon.setTransactionID(TransactionIDEnum.SEASON_OPERATION_REQ.getValue());
        Results.SeasonLotMoveInReserveReqResult result = new Results.SeasonLotMoveInReserveReqResult();

        Infos.Season paramSeason = moveInReserveReqForSeasonParams.getSeason();
        Validations.check(paramSeason == null, retCodeConfigEx.getSeasonNoExist());
        if (CimBooleanUtils.isTrue(paramSeason.getParam().getSeasonGroupFlag())) {
            Validations.check(moveInReserveReqForSeasonParams.getProductMoveinReserveReqParams() == null,
                    retCodeConfigEx.getProductLotRequired());
        }

        // step-1 create season  job
        // check if PM seasonPlan and season Job exist
        Infos.SeasonJob seasonJob = null;
        try{
            seasonJob = seasonMethod.getSeasonJob(objCommon, paramSeason.getSeasonID());
        } catch (NullPointerException ne) {
            if(log.isDebugEnabled()){
                log.debug("seasonJob not exist");
            }
        }
        if (BizConstant.SEASON_TYPE_PM.equals(paramSeason.getSeasonType())) {
            if (null == seasonJob) {
                seasonJob = seasonMethod.createSeasonJob(
                        objCommon, paramSeason, moveInReserveReqForSeasonParams.getClaimMemo());
            }
        } else {
            seasonJob = seasonMethod.createSeasonJob(
                    objCommon, paramSeason, moveInReserveReqForSeasonParams.getClaimMemo());
        }
        // get productID from the seasonJob
        ObjectIdentifier seasonProductID = seasonJob.getSeasonProductID();

        // step-2 season lot move in reserve
        Params.MoveInReserveReqParams moveInReserveReqParams
                = moveInReserveReqForSeasonParams.getSeasonMoveinReserveReqParams();
        moveInReserveReqParams.setUser(moveInReserveReqForSeasonParams.getUser());

        Validations.check(CimArrayUtils.isEmpty(
                moveInReserveReqParams.getStartCassetteList()), retCodeConfig.getInvalidInputParam());
        AtomicInteger totalWaferCount= new AtomicInteger();
        for (Infos.StartCassette startCassette : moveInReserveReqParams.getStartCassetteList()) {
            Optional.ofNullable(startCassette.getLotInCassetteList()).ifPresent(lotInCassettes -> {
                for (Infos.LotInCassette lotInCassette : lotInCassettes) {
                    if (CimBooleanUtils.isFalse(lotInCassette.getMoveInFlag())){
                        continue;
                    }
                    Validations.check(!ObjectIdentifier.equalsWithValue(
                            seasonProductID,lotInCassette.getProductID()),retCodeConfigEx.getLotNotMeetSeason());
                    String subLotType = lotInCassette.getSubLotType();
                    Validations.check(!CimStringUtils.equals(
                            subLotType,BizConstant.SEASON_LOT_TYPE),retCodeConfigEx.getLotNotMeetSeason());
                    int waferCount = CimArrayUtils.getSize(lotInCassette.getLotWaferList());
                    totalWaferCount.addAndGet(waferCount);
                }
            });
        }
        Validations.check(CimNumberUtils.intValue(seasonJob.getMinSeasonWaferCount())>totalWaferCount.get(),
                retCodeConfigEx.getMinWaferCountError(),CimNumberUtils.intValue(seasonJob.getMinSeasonWaferCount()));

        Results.SeasonMoveInReserveReqResult moveInReserveReqResult
                = moveInReserveReq(objCommon, moveInReserveReqParams);
        result.setSeasonLotControlJobID(moveInReserveReqResult.getControlJobID());
        seasonJob.setSeasonLotID(moveInReserveReqResult.getLotID());
        seasonJob.setSeasonCarrierID(moveInReserveReqResult.getCassetteID());
        seasonJob.setSeasonRcpID(moveInReserveReqResult.getRecipeID());

        // step-3 product lot move in reserve
        if (moveInReserveReqForSeasonParams.getProductMoveinReserveReqParams() != null) {
            moveInReserveReqParams=moveInReserveReqForSeasonParams.getProductMoveinReserveReqParams();
            moveInReserveReqParams.setUser(moveInReserveReqForSeasonParams.getUser());

            moveInReserveReqResult = moveInReserveReq(objCommon, moveInReserveReqParams);
            result.setProductLotControlJobID(moveInReserveReqResult.getControlJobID());
            seasonJob.setLotID(moveInReserveReqResult.getLotID());
            seasonJob.setCarrierID(moveInReserveReqResult.getCassetteID());
        }
        seasonMethod.updateSeasonJob(seasonJob, BizConstant.SEASONJOB_STATUS_RESERVED);
        return result;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param moveInReserveReqForSeasonParams
     * @return com.fa.cim.dto.Results.SeasonMoveInReserveReqResult
     * @throws
     * @author ho
     * @date 2020/6/15 15:00
     */
    @Override
    public Results.SeasonLotMoveInReserveReqResult sxMoveInReserveForIBReq(Infos.ObjCommon objCommon, Params.MoveInReserveForIBReqForSeasonParams moveInReserveReqForSeasonParams) {
        objCommon.setTransactionID(TransactionIDEnum.SEASON_OPERATION_REQ.getValue());
        Results.SeasonLotMoveInReserveReqResult result = new Results.SeasonLotMoveInReserveReqResult();

        Infos.Season paramSeason = moveInReserveReqForSeasonParams.getSeason();
        Validations.check(paramSeason == null, retCodeConfigEx.getSeasonNoExist());
        if (CimBooleanUtils.isTrue(paramSeason.getParam().getSeasonGroupFlag())) {
            Validations.check(moveInReserveReqForSeasonParams.getProductMoveInReserveForIBReqParams() == null, retCodeConfigEx.getProductLotRequired());
        }

        // step-1 create season  job
        Infos.SeasonJob seasonJob = seasonMethod.createSeasonJob(objCommon, paramSeason, moveInReserveReqForSeasonParams.getClaimMemo());

        // step-2 season lot move in reserve
        Params.MoveInReserveForIBReqParams moveInReserveReqParams = moveInReserveReqForSeasonParams.getSeasonMoveInReserveForIBReqParams();
        moveInReserveReqParams.setUser(moveInReserveReqForSeasonParams.getUser());

        Validations.check(CimArrayUtils.isEmpty(moveInReserveReqParams.getStartCassetteList()), retCodeConfig.getInvalidInputParam());
        AtomicInteger totalWaferCount= new AtomicInteger();
        for (Infos.StartCassette startCassette : moveInReserveReqParams.getStartCassetteList()) {
            Optional.ofNullable(startCassette.getLotInCassetteList()).ifPresent(lotInCassettes -> {
                for (Infos.LotInCassette lotInCassette : lotInCassettes) {
                    if (CimBooleanUtils.isFalse(lotInCassette.getMoveInFlag())){
                        continue;
                    }
                    Validations.check(!ObjectIdentifier.equalsWithValue(seasonJob.getSeasonProductID(), lotInCassette.getProductID()),retCodeConfigEx.getLotNotMeetSeason());
                    String subLotType = lotInCassette.getSubLotType();
                    Validations.check(!CimStringUtils.equals(subLotType,BizConstant.SEASON_LOT_TYPE),retCodeConfigEx.getLotNotMeetSeason());
                    int waferCount = CimArrayUtils.getSize(lotInCassette.getLotWaferList());
                    totalWaferCount.addAndGet(waferCount);
                }
            });
        }
        Validations.check(CimNumberUtils.intValue(seasonJob.getMinSeasonWaferCount())>totalWaferCount.get(),
                retCodeConfigEx.getMinWaferCountError(),CimNumberUtils.intValue(seasonJob.getMinSeasonWaferCount()));

        Results.SeasonMoveInReserveReqResult moveInReserveReqResult = moveInReserveForIBReq(objCommon, moveInReserveReqParams);
        result.setSeasonLotControlJobID(moveInReserveReqResult.getControlJobID());
        seasonJob.setSeasonLotID(moveInReserveReqResult.getLotID());
        seasonJob.setSeasonCarrierID(moveInReserveReqResult.getCassetteID());
        seasonJob.setSeasonRcpID(moveInReserveReqResult.getRecipeID());

        // step-3 product lot move in reserve
        if (moveInReserveReqForSeasonParams.getProductMoveInReserveForIBReqParams() != null) {
            moveInReserveReqParams = moveInReserveReqForSeasonParams.getProductMoveInReserveForIBReqParams();
            moveInReserveReqParams.setUser(moveInReserveReqForSeasonParams.getUser());

            moveInReserveReqResult = moveInReserveForIBReq(objCommon, moveInReserveReqParams);
            result.setProductLotControlJobID(moveInReserveReqResult.getControlJobID());
            seasonJob.setLotID(moveInReserveReqResult.getLotID());
            seasonJob.setCarrierID(moveInReserveReqResult.getCassetteID());
        }
        seasonMethod.updateSeasonJob(seasonJob, BizConstant.SEASONJOB_STATUS_RESERVED);
        return result;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param eqpID
     * @return void
     * @throws
     * @author ho
     * @date 2020/6/15 17:35
     */
    @Override
    public void sxSeasonJobForMoveInUpdate(Infos.ObjCommon objCommon, ObjectIdentifier eqpID) {
        objectLockMethod.objectLock(objCommon, CimMachine.class,eqpID);
        List<Infos.SeasonJob> seasonJobList = seasonMethod.getMachineSeasonJob(objCommon, eqpID);
        if (CimArrayUtils.isEmpty(seasonJobList)){
            return;
        }
        seasonJobList.stream().forEach(seasonJob -> {
            objectLockMethod.objectLock(objCommon,CimSeasonJob.class,seasonJob.getSeasonJobID());
        });

        Optional.ofNullable(seasonJobList).ifPresent(seasonJobs -> {
            for (Infos.SeasonJob seasonJob : seasonJobs) {
                sxUpdateSeasonJob(objCommon, seasonJob, BizConstant.SEASONJOB_STATUS_EXECUTING, null);
            }
        });
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------
     *
     * @param objCommon
     * @param equipmentID
     * @param controlJobID
     * @return void
     * @throws
     * @author ho
     * @date 2020/6/17 18:16
     */
    @Override
    public void sxSeasonForMoveInCancel(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, ObjectIdentifier controlJobID) {
        objectLockMethod.objectLock(objCommon,CimMachine.class,equipmentID);

        List<Infos.SeasonJob> machineSeasonJob = seasonMethod.getMachineSeasonJob(objCommon, equipmentID);
        if (CimArrayUtils.isEmpty(machineSeasonJob)) {
            return;
        }

        machineSeasonJob.stream().forEach(seasonJob -> {
            objectLockMethod.objectLock(objCommon,CimSeasonJob.class,seasonJob.getSeasonJobID());
        });

        machineSeasonJob.forEach(seasonJob -> {
            String carrierStr = ObjectIdentifier.fetchValue(seasonJob.getSeasonCarrierID());
            ObjectIdentifier controlJobIDGet = cassetteMethod.cassetteControlJobIDGet(objCommon,
                    ObjectIdentifier.buildWithValue(carrierStr.split(BizConstant.SEASON_SEPARATOR)[0]));
            if (ObjectIdentifier.equalsWithValue(controlJobID, controlJobIDGet)){
                //BUG-3106
                seasonMethod.updateSeasonJob(seasonJob, BizConstant.SEASONJOB_STATUS_RESERVED);
            }
        });
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param params
     * @return void
     * @throws
     * @author ho
     * @date 2020/6/18 11:09
     */
    @Override
    public void sxSeasonForMoveInReserveCancel(Infos.ObjCommon objCommon, Params.MoveInReserveCancelReqParams params) {
        if (CimStringUtils.equals(TransactionIDEnum.UN_LOADING_LOT_RPT.getValue(), objCommon.getTransactionID())) {
            return;
        }
        objectLockMethod.objectLock(objCommon,CimMachine.class,params.getEquipmentID());

        List<Infos.SeasonJob> machineSeasonJob = seasonMethod.getMachineSeasonJob(objCommon, params.getEquipmentID());
        if (CimArrayUtils.isEmpty(machineSeasonJob)) {
            return;
        }

        machineSeasonJob.stream().forEach(seasonJob -> {
            objectLockMethod.objectLock(objCommon,CimSeasonJob.class,seasonJob.getSeasonJobID());
        });

        List<ObjectIdentifier> controlJobLots = controlJobMethod.controlJobLotIDListGetDR(objCommon, params.getControlJobID());
        AtomicBoolean isNotSeasonLot = new AtomicBoolean(false);
        Optional.ofNullable(controlJobLots).ifPresent(lots -> {
            isNotSeasonLot.set(lots.stream().anyMatch(lot -> {
                String subLotTypeGetDR = lotMethod.lotSubLotTypeGetDR(objCommon, lot);
                return !CimStringUtils.equals(BizConstant.SEASON_LOT_TYPE, subLotTypeGetDR);
            }));
        });
        Validations.check(isNotSeasonLot.get(), retCodeConfigEx.getEquipmentDoingSeasoning());
        machineSeasonJob.forEach(seasonJob -> {
            //BUG-3106
            seasonMethod.updateSeasonJob(seasonJob,BizConstant.SEASONJOB_STATUS_REQUESTED);
            /*ObjectIdentifier carrierID = seasonJob.getCarrierID();
            if (ObjectUtils.isEmptyWithValue(carrierID)){
                return;
            }
            ObjectIdentifier lotControlJobIDGet = cassetteMethod.cassetteControlJobIDGet(objCommon, ObjectIdentifier.buildWithValue(
                    carrierID.getValue().split(BizConstant.SEASON_SEPARATOR)[0]
            ));
            Params.MoveInReserveCancelReqParams moveInReserveCancelReqParams = new Params.MoveInReserveCancelReqParams();
            moveInReserveCancelReqParams.setEquipmentID(params.getEquipmentID());
            moveInReserveCancelReqParams.setControlJobID(lotControlJobIDGet);
            dispatchService.sxMoveInReserveCancelReqService(objCommon, moveInReserveCancelReqParams);*/
        });
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommonIn
     * @param equipmentID
     * @param cassetteID
     * @return void
     * @throws
     * @author ho
     * @date 2020/6/18 13:01
     */
    @Override
    public void sxSeasonForUnloading(Infos.ObjCommon objCommonIn, ObjectIdentifier equipmentID, ObjectIdentifier cassetteID) {
        List<Infos.SeasonJob> machineSeasonJob = seasonMethod.getMachineSeasonJob(objCommonIn, equipmentID);
        if (CimArrayUtils.isEmpty(machineSeasonJob)) {
            return;
        }
        ObjectIdentifier controlJob = cassetteMethod.cassetteControlJobIDGet(objCommonIn, cassetteID);
        if (ObjectIdentifier.isEmptyWithValue(controlJob)) {
            return;
        }
        AtomicBoolean isNotSeasonLot = new AtomicBoolean(false);
        Infos.LotListInCassetteInfo lotListInCassetteInfo = cassetteMethod.cassetteGetLotList(objCommonIn, cassetteID);
        Optional.ofNullable(lotListInCassetteInfo.getLotIDList()).ifPresent(lotIDList -> {
            isNotSeasonLot.set(lotIDList.stream().anyMatch(lotID -> {
                String subLotTypeGetDR = lotMethod.lotSubLotTypeGetDR(objCommonIn, lotID);
                return !CimStringUtils.equals(subLotTypeGetDR, BizConstant.SEASON_LOT_TYPE);
            }));
        });
        Validations.check(isNotSeasonLot.get(), retCodeConfigEx.getEquipmentDoingSeasoning());
        ObjectIdentifier controlJobIDGet = cassetteMethod.cassetteControlJobIDGet(objCommonIn, cassetteID);
        if (!ObjectIdentifier.isEmpty(controlJobIDGet)) {
            return;
        }
        Optional.ofNullable(machineSeasonJob).ifPresent(seasonJobs -> {
            seasonJobs.forEach(seasonJob -> {
                if (!CimStringUtils.equals(seasonJob.getSeasonJobStatus(), BizConstant.SEASONJOB_STATUS_RESERVED)) {
                    return;
                }
                ObjectIdentifier lotID = seasonJob.getLotID();
                if (lotID == null) {
                    return;
                }
                ObjectIdentifier lotControlJobIDGet = lotMethod.lotControlJobIDGet(objCommonIn, lotID);
                Params.MoveInReserveCancelReqParams params = new Params.MoveInReserveCancelReqParams();
                params.setEquipmentID(equipmentID);
                params.setControlJobID(lotControlJobIDGet);
                dispatchService.sxMoveInReserveCancelReqService(objCommonIn, params);
                seasonMethod.deleteSeasonJob(objCommonIn, seasonJob);
            });
        });
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param equipmentID
     * @return void
     * @throws
     * @author ho
     * @date 2020/6/18 14:26
     */
    @Override
    public void sxSeasonForMoveOut(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID) {
        List<Infos.SeasonJob> machineSeasonJob = seasonMethod.getMachineSeasonJob(objCommon, equipmentID);
        if (CimArrayUtils.isEmpty(machineSeasonJob)) {
            return;
        }

        machineSeasonJob.stream()
                .forEach(seasonJob -> objectLockMethod.objectLock(objCommon,CimSeasonJob.class, seasonJob.getSeasonJobID()));

        machineSeasonJob.forEach(seasonJob -> {
            Infos.Season season = seasonMethod.getSeason(seasonJob.getSeasonID());
            if (CimStringUtils.equals(BizConstant.SEASON_TYPE_PM, seasonJob.getSeasonType())) {
                List<Infos.SeasonProduct> seasonProducts = season.getSeasonProducts();
                for (int i = 0; i < seasonProducts.size(); i++) {
                    if (ObjectIdentifier.equalsWithValue(seasonJob.getSeasonProductID(), seasonProducts.get(i).getProductID())) {
                        if (i + 1 == seasonProducts.size()) {
                            season.setPmFlag(false);
                            break;
                        } else {
                            seasonJob.setSeasonProductID(seasonProducts.get(i + 1).getProductID());
                            seasonMethod.updateSeasonJob(seasonJob, BizConstant.SEASONJOB_STATUS_REQUESTED);
                            return;
                        }
                    }
                }
            }
            seasonMethod.updateSeasonJob(seasonJob, BizConstant.SEASONJOB_STATUS_COMPLETED);
            season.setLastSeasonTime(objCommon.getTimeStamp().getReportTimeStamp());
            season.setLastModifyTime(objCommon.getTimeStamp().getReportTimeStamp());
            seasonMethod.updateSeason(season);
        });
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param equipmentID
     * @return void
     * @throws
     * @author ho
     * @date 2020/6/18 14:26
     */
    @Override
    public void sxSeasonForForceMoveOut(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID) {
        List<Infos.SeasonJob> machineSeasonJob = seasonMethod.getMachineSeasonJob(objCommon, equipmentID);
        if (CimArrayUtils.isEmpty(machineSeasonJob)) {
            return;
        }
        machineSeasonJob.forEach(seasonJob -> {
            seasonMethod.updateSeasonJob(seasonJob, BizConstant.SEASONJOB_STATUS_ABORTED);
        });
    }
}
