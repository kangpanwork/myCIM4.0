package com.fa.cim.method.impl;

import com.fa.cim.annotaion.OmMethod;
import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.OmCode;
import com.fa.cim.common.utils.*;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.config.RetCodeConfigEx;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Inputs;
import com.fa.cim.dto.Outputs;
import com.fa.cim.entity.runtime.season.CimSeasonDO;
import com.fa.cim.jpa.CimJpaRepository;
import com.fa.cim.method.*;
import com.fa.cim.newcore.bo.code.CimE10State;
import com.fa.cim.newcore.bo.code.CimMachineState;
import com.fa.cim.newcore.bo.machine.CimMachine;
import com.fa.cim.newcore.bo.person.PersonManager;
import com.fa.cim.newcore.bo.planning.PlanManager;
import com.fa.cim.newcore.bo.prodspec.CimProductSpecification;
import com.fa.cim.newcore.bo.prodspec.ProductSpecificationManager;
import com.fa.cim.newcore.bo.product.CimLot;
import com.fa.cim.newcore.bo.product.ProductManager;
import com.fa.cim.newcore.bo.recipe.CimMachineRecipe;
import com.fa.cim.newcore.bo.recipe.CimRecipeGroup;
import com.fa.cim.newcore.bo.recipe.RecipeManager;
import com.fa.cim.newcore.bo.season.CimSeason;
import com.fa.cim.newcore.bo.season.CimSeasonJob;
import com.fa.cim.newcore.dto.machine.MachineDTO;
import com.fa.cim.newcore.dto.rcpgrp.RecipeGroup;
import com.fa.cim.newcore.dto.season.SeasonDTO;
import com.fa.cim.newcore.exceptions.CoreFrameworkException;
import com.fa.cim.newcore.factory.BaseCoreFactory;
import com.fa.cim.newcore.impl.bo.env.StandardProperties;
import com.fa.cim.newcore.standard.mchnmngm.ProcessResource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.chrono.ChronoLocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * description:
 *
 * <p>change history: date defect person comments
 * ---------------------------------------------------------------------------------------------------------------------
 *
 * @exception
 * @author ho
 * @date 2020/5/20 14:17
 */
@OmMethod
@Slf4j
public class SeasonMethod implements ISeasonMethod {
  @Autowired
  private RetCodeConfig retCodeConfig;

  @Autowired
  private RetCodeConfigEx retCodeConfigEx;

  @Autowired
  private ILotMethod lotMethod;

  @Autowired
  private IObjectLockMethod objectLockMethod;

  @Autowired
  private IEventMethod eventMethod;

  @Autowired
  private IEquipmentMethod equipmentMethod;

  @Autowired
  private ILogicalRecipeMethod logicalRecipeMethod;

  @Autowired
  private ICassetteMethod cassetteMethod;

  @Autowired
  private ProductSpecificationManager productSpecificationManager;

  @Autowired
  private PersonManager personManager;

  @Autowired
  private PlanManager planManager;

  @Autowired
  private ProductManager productManager;

  @Autowired
  private RecipeManager recipeManager;

  @Autowired
  private BaseCoreFactory baseCoreFactory;

  @Autowired
  private CimJpaRepository cimJpaRepository;

  /**
   * description:
   *
   * <p>change history: date defect person comments
   * ---------------------------------------------------------------------------------------------------------------------
   *
   * @return void
   * @exception
   * @author ho
   * @date 2020/5/20 14:26
   */
  @Override
  public void checkSeasonProduct(ObjectIdentifier seasonProduct) {
    Validations.check(
        ObjectIdentifier.isEmpty(seasonProduct), retCodeConfigEx.getSeasonProductEmpty());
    CimProductSpecification productSpecification =
        baseCoreFactory.getBO(CimProductSpecification.class, seasonProduct);
    Validations.check(
        productSpecification == null, retCodeConfig.getInvalidProdId(), seasonProduct.getValue());
    if (CimStringUtils.isEmpty(seasonProduct.getValue())) {
      seasonProduct.setValue(productSpecification.getIdentifier());
    }
    Validations.check(
        !CimStringUtils.equals(seasonProduct.getValue(), productSpecification.getIdentifier()),
        retCodeConfigEx.getInvalidRefkey());
    seasonProduct.setReferenceKey(productSpecification.getPrimaryKey());
  }

  /**
   * description:
   *
   * <p>change history: date defect person comments
   * ---------------------------------------------------------------------------------------------------------------------
   *
   * @param season
   * @return void
   * @exception
   * @author ho
   * @date 2020/5/20 14:45
   */
  @Override
  public void checkIdleParam(Infos.Season season) {
    Infos.SeasonParam seasonParam = season.getParam();
    checkParam(seasonParam);
    List<Infos.SeasonChamber> chambers = season.getChambers();
    // 不能一个Season Plan指定多个Chamber ID去创建
    if (!CimObjectUtils.isEmpty(chambers)) {
      Validations.check(chambers.size() > 1, retCodeConfigEx.getIdleSeasonPlanMultiChamber());
    }
    Validations.check(
        seasonParam.getMaxIdleTime() == null || seasonParam.getMaxIdleTime() == 0,
        retCodeConfigEx.getMaxIdleTimeEmpty());
    // No chamber的设备只能创建一条By Idle time 的Season Plan
    String sql = "SELECT * FROM OMSEASON WHERE EQP_ID = ?1 AND SEASON_TYPE = ?2 AND SEASON_ID<> ?3";
    List<CimSeasonDO> cimSeasons =
        cimJpaRepository.query(
            sql,
            CimSeasonDO.class,
            season.getEqpID().getValue(),
            BizConstant.SEASON_TYPE_IDLE,
            season.getSeasonID().getValue());
    if (seasonParam.getNoChamberFlag()) {
      Validations.check(
          !CimArrayUtils.isEmpty(cimSeasons), retCodeConfigEx.getNoChamberEqpNotUniqueIdlePlan());
    }
    // 不同Season Plan的chamber ID不能重复
    if (!CimArrayUtils.isEmpty(cimSeasons)) {
      for (CimSeasonDO cimSeasonDo : cimSeasons) {
        ObjectIdentifier seasonID =
            ObjectIdentifier.build(cimSeasonDo.getSeasonId(), cimSeasonDo.getId());
        CimSeason cimSeason = baseCoreFactory.getBO(CimSeason.class, seasonID);
        List<SeasonDTO.SeasonChamber> seasonChambers = cimSeason.allSeasonChambers();
        // 要么一直有chamberID，要么一直没有
        boolean noChamberflag = CimArrayUtils.isEmpty(seasonChambers);
        boolean setChamberFlag = !CimArrayUtils.isEmpty(chambers);
        Validations.check(noChamberflag, retCodeConfigEx.getNotAllowedSeasonSetting());
        Validations.check(!setChamberFlag, retCodeConfigEx.getNotAllowedSeasonSetting());
        if (!CimArrayUtils.isEmpty(seasonChambers)) {
          for (SeasonDTO.SeasonChamber seasonChamber : seasonChambers) {
            for (Infos.SeasonChamber chamber : chambers) {
              Validations.check(
                  CimObjectUtils.equals(seasonChamber.getChamberID(), chamber.getChamberID()),
                  retCodeConfigEx.getDifferentDilePlanSameChamber());
            }
          }
        }
        SeasonDTO.SeasonParam seasonParameters = cimSeason.getSeasonParameters();
        // 两个Season Plan的idle time不能相同
        Validations.check(
            CimObjectUtils.equals(seasonParameters.getMaxIdleTime(), seasonParam.getMaxIdleTime()),
            retCodeConfigEx.getDifferentDilePlanSameIdleTime());
      }
    }
  }

  /**
   * description:
   *
   * <p>change history: date defect person comments
   * ---------------------------------------------------------------------------------------------------------------------
   *
   * @param season
   * @return void
   * @exception
   * @author ho
   * @date 2020/5/20 14:45
   */
  @Override
  public void checkRecipeIdleParam(Infos.Season season) {
    Infos.SeasonParam seasonParam = season.getParam();
    checkParam(seasonParam);
    Validations.check(
        seasonParam.getMaxIdleTime() == null || seasonParam.getMaxIdleTime() == 0,
        retCodeConfigEx.getMaxIdleTimeEmpty());
    String sql = "SELECT * FROM OMSEASON WHERE EQP_ID = ?1 AND SEASON_TYPE = ?2 AND SEASON_ID<> ?3";
    List<CimSeasonDO> cimSeasons =
        cimJpaRepository.query(
            sql,
            CimSeasonDO.class,
            season.getEqpID().getValue(),
            BizConstant.SEASON_TYPE_RECIPEIDLE,
            season.getSeasonID().getValue());
    if (!CimArrayUtils.isEmpty(cimSeasons)) {
      for (CimSeasonDO cimSeasonDO : cimSeasons) {
        ObjectIdentifier seasonID =
            ObjectIdentifier.build(cimSeasonDO.getSeasonId(), cimSeasonDO.getId());
        CimSeason oldSeason = baseCoreFactory.getBO(CimSeason.class, seasonID);
        List<SeasonDTO.SeasonProdRecipe> oldSeasonProdRecipes = oldSeason.allSeasonProductRecipes();
        for (SeasonDTO.SeasonProdRecipe oldSeasonProdRecipe : oldSeasonProdRecipes) {
          ObjectIdentifier oldRecipeID = oldSeasonProdRecipe.getRecipeID();
          List<Infos.SeasonProdRecipe> newSeasonRecipes = season.getRecipes();
          for (Infos.SeasonProdRecipe newSeasonProdRecipe : newSeasonRecipes) {
            if (CimObjectUtils.equals(oldRecipeID, newSeasonProdRecipe.getRecipeID())) {
              SeasonDTO.SeasonParam oldSeasonParameters = oldSeason.getSeasonParameters();
              Validations.check(
                  CimObjectUtils.equals(
                      oldSeasonParameters.getMaxIdleTime(), seasonParam.getMaxIdleTime()),
                  retCodeConfigEx.getRecipeIdleSeasonPlanSameRecipeAndTime());
            }
          }
        }
      }
    }
  }

  /**
   * description:
   *
   * <p>change history: date defect person comments
   * ---------------------------------------------------------------------------------------------------------------------
   *
   * @param season
   * @return void
   * @exception
   * @author ho
   * @date 2020/5/20 17:26
   */
  @Override
  public void checkIntervalParam(Infos.Season season) {
    Infos.SeasonParam seasonParam = season.getParam();
    checkParam(seasonParam);
    Validations.check(
        seasonParam.getIntervalBetweenSeason() == null
            || seasonParam.getIntervalBetweenSeason() == 0,
        retCodeConfigEx.getIntervalBetweenSeasonEmpty());
  }

  /**
   * description:
   *
   * <p>change history: date defect person comments
   * ---------------------------------------------------------------------------------------------------------------------
   *
   * @param season
   * @return void
   * @exception
   * @author ho
   * @date 2020/5/20 17:26
   */
  @Override
  public void checkRecipeGroupParam(Infos.Season season) {
    Infos.SeasonParam seasonParam = season.getParam();
    ObjectIdentifier fromRecipeGroup = seasonParam.getFromRecipeGroup();
    ObjectIdentifier toRecipeGroup = seasonParam.getToRecipeGroup();
    String sql = "SELECT * FROM OMSEASON WHERE EQP_ID = ?1 AND SEASON_TYPE = ?2 AND SEASON_ID<> ?3";
    List<CimSeasonDO> cimSeasons =
        cimJpaRepository.query(
            sql,
            CimSeasonDO.class,
            season.getEqpID().getValue(),
            BizConstant.SEASON_TYPE_RECIPEGROUP,
            season.getSeasonID().getValue());
    if (!CimArrayUtils.isEmpty(cimSeasons)) {
      for (CimSeasonDO cimSeasonDO : cimSeasons) {
        ObjectIdentifier seasonID =
            ObjectIdentifier.build(cimSeasonDO.getSeasonId(), cimSeasonDO.getId());
        CimSeason cimSeason = baseCoreFactory.getBO(CimSeason.class, seasonID);
        SeasonDTO.SeasonParam seasonParameters = cimSeason.getSeasonParameters();
        Validations.check(
            CimObjectUtils.equals(fromRecipeGroup, seasonParameters.getFromRecipeGroup())
                && CimObjectUtils.equals(toRecipeGroup, seasonParameters.getToRecipeGroup()),
            retCodeConfigEx.getDifferentRecipeGroupPlanSameSetting());
      }
    }
    checkParam(seasonParam);

    Validations.check(
        ObjectIdentifier.isEmpty(fromRecipeGroup) || ObjectIdentifier.isEmpty(toRecipeGroup),
        retCodeConfigEx.getRecipeGroupParamError());
    Validations.check(
        getRecipeGroup(fromRecipeGroup) == null,
        retCodeConfigEx.getRecipeGroupIdError(),
        fromRecipeGroup.getValue());
    Validations.check(
        getRecipeGroup(toRecipeGroup) == null,
        retCodeConfigEx.getRecipeGroupIdError(),
        toRecipeGroup.getValue());
  }

  /**
   * description:
   *
   * <p>change history: date defect person comments
   * ---------------------------------------------------------------------------------------------------------------------
   *
   * @param season
   * @return void
   * @exception
   * @author ho
   * @date 2020/5/20 17:26
   */
  @Override
  public void checkPMParam(Infos.Season season) {
    Infos.SeasonParam seasonParam = season.getParam();
    // No chamber的设备只能创建一条By Idle time 的Season Plan
    if (seasonParam.getNoChamberFlag()) {
      String sql =
          "SELECT * FROM OMSEASON WHERE EQP_ID = ?1 AND SEASON_TYPE = ?2 AND SEASON_ID<> ?3";
      List<CimSeasonDO> cimSeasons =
          cimJpaRepository.query(
              sql,
              CimSeasonDO.class,
              season.getEqpID().getValue(),
              BizConstant.SEASON_TYPE_PM,
              season.getSeasonID().getValue());
      Validations.check(
          !CimArrayUtils.isEmpty(cimSeasons), retCodeConfigEx.getNoChamberEqpNotUniquePMPlan());
    }
    checkParam(seasonParam);
  }

  /**
   * description:
   *
   * <p>change history: date defect person comments
   * ---------------------------------------------------------------------------------------------------------------------
   *
   * @param season
   * @return void
   * @exception
   * @author ho
   * @date 2020/5/20 17:29
   */
  @Override
  public void checkSeason(Infos.Season season) {
    Validations.check(season == null, retCodeConfig.getInvalidInputParam());
    ObjectIdentifier eqpID = season.getEqpID();
    Validations.check(ObjectIdentifier.isEmpty(eqpID), retCodeConfig.getInvalidInputParam());
    Validations.check(
        ObjectIdentifier.isEmpty(season.getSeasonID()), retCodeConfigEx.getSeasonIdRequired());
    String[] seasonTypes = {
      BizConstant.SEASON_TYPE_IDLE,
      BizConstant.SEASON_TYPE_RECIPEIDLE,
      BizConstant.SEASON_TYPE_INTERVAL,
      BizConstant.SEASON_TYPE_PM,
      BizConstant.SEASON_TYPE_RECIPEGROUP
    };
    String seasonType = season.getSeasonType();
    Validations.check(
        !CimArrayUtils.binarySearch(seasonTypes, seasonType), retCodeConfig.getInvalidInputParam());
    Validations.check(
        CimArrayUtils.isEmpty(season.getSeasonProducts()),
        retCodeConfigEx.getSeasonProductRequired());
    season
        .getSeasonProducts()
        .forEach(
            seasonProduct ->
                Validations.check(
                    seasonProduct.getQuantity() == null,
                    retCodeConfigEx.getSeasonWaferCountEmpty()));
    Infos.SeasonParam param = season.getParam();
    // check if the eqp has no chamber
    CimMachine machine = baseCoreFactory.getBO(CimMachine.class, eqpID);
    List<ProcessResource> processResources = machine.allProcessResources();
    param.setNoChamberFlag(false);
    if (CimArrayUtils.isEmpty(processResources)) {
      param.setNoChamberFlag(true);
    }
    switch (season.getSeasonType()) {
      case BizConstant.SEASON_TYPE_IDLE:
        checkIdleParam(season);
        break;
      case BizConstant.SEASON_TYPE_RECIPEIDLE:
        checkRecipeIdleParam(season);
        break;
      case BizConstant.SEASON_TYPE_INTERVAL:
        checkIntervalParam(season);
        break;
      case BizConstant.SEASON_TYPE_PM:
        checkPMParam(season);
        break;
      case BizConstant.SEASON_TYPE_RECIPEGROUP:
        checkRecipeGroupParam(season);
    }
  }

  /**
   * description:
   *
   * <p>change history: date defect person comments
   * ---------------------------------------------------------------------------------------------------------------------
   *
   * @param objCommon
   * @param startCassetteList
   * @return void
   * @exception
   * @author ho
   * @date 2020/5/26 10:51
   */
  @Override
  public boolean checkIfSeasonLot(
      Infos.ObjCommon objCommon, List<Infos.StartCassette> startCassetteList) {
    for (Infos.StartCassette startCassette : startCassetteList) {
      for (Infos.LotInCassette lotInCassette : startCassette.getLotInCassetteList()) {
        if (CimBooleanUtils.isTrue(lotInCassette.getMoveInFlag())
            && BizConstant.SEASON_LOT_TYPE.equals(lotInCassette.getSubLotType())) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * description:
   *
   * <p>change history: date defect person comments
   * ---------------------------------------------------------------------------------------------------------------------
   *
   * @param recipeGroupID
   * @return com.fa.cim.dto.Infos.RecipeGroup
   * @exception
   * @author ho
   * @date 2020/5/20 17:49
   */
  @Override
  public RecipeGroup.Info getRecipeGroup(ObjectIdentifier recipeGroupID) {
    CimRecipeGroup recipeGroup = baseCoreFactory.getBO(CimRecipeGroup.class, recipeGroupID);

    if (recipeGroup == null) {
      return null;
    }

    if (recipeGroup.getType() != RecipeGroup.Type.Season) {
      return null;
    }

    return recipeGroup.getRecipeGroupInfo();
  }

  /**
   * description:
   *
   * <p>change history: date defect person comments
   * ---------------------------------------------------------------------------------------------------------------------
   *
   * @param
   * @return java.util.List<com.fa.cim.dto.Infos.RecipeGroup>
   * @exception
   * @author ho
   * @date 2020/6/5 10:12
   */
  @Override
  public List<RecipeGroup.Info> allRecipeGroup() {
    List<RecipeGroup.Info> recipeGroups = new ArrayList<>();
    List<CimRecipeGroup> cimRecipeGroups =
        recipeManager.findAllRecipeGroupByTpe(RecipeGroup.Type.Season);
    if (cimRecipeGroups == null) {
      return recipeGroups;
    }
    for (CimRecipeGroup cimRecipeGroup : cimRecipeGroups) {
      recipeGroups.add(cimRecipeGroup.getRecipeGroupInfo());
    }
    return recipeGroups;
  }

  /**
   * description: 创建recipeGroup
   *
   * <p>change history: date defect person comments
   * ---------------------------------------------------------------------------------------------------------------------
   *
   * @param recipeInfos
   * @return void
   * @exception
   * @author ho
   * @date 2020/5/25 14:38
   */
  @Override
  public RecipeGroup.Info createRecipeGroup(List<ObjectIdentifier> recipeInfos) {
    RecipeGroup.Info recipeGroup = new RecipeGroup.Info();
    recipeGroup.setRecipeInfos(recipeInfos);

    recipeGroup.setRecipeGroupID(ObjectIdentifier.buildWithValue(null));
    createRecipeGroup(recipeGroup);

    return recipeGroup;
  }

  /**
   * description:
   *
   * <p>change history: date defect person comments
   * ---------------------------------------------------------------------------------------------------------------------
   *
   * @param recipeGroup
   * @return com.fa.cim.dto.Infos.RecipeGroup
   * @exception
   * @author ho
   * @date 2020/6/2 15:12
   */
  @Override
  public RecipeGroup.Info createRecipeGroup(RecipeGroup.Info recipeGroup) {
    Validations.check(
        CimArrayUtils.isEmpty(recipeGroup.getRecipeInfos()), retCodeConfig.getInvalidInputParam());
    CimRecipeGroup recipeGroupNamed =
        recipeManager.createRecipeGroupNamed(
            ObjectIdentifier.fetchValue(recipeGroup.getRecipeGroupID()));
    recipeGroupNamed.setRecipeGroupInfo(convertToCoreInfo(recipeGroup));
    return recipeGroup;
  }

  private RecipeGroup.Info convertToCoreInfo(RecipeGroup.Info recipeGroup) {
    RecipeGroup.Info info = new RecipeGroup.Info();
    info.setType(RecipeGroup.Type.Season);
    info.setRecipeInfos(recipeGroup.getRecipeInfos());
    info.setRecipeGroupID(recipeGroup.getRecipeGroupID());
    info.setDescription(recipeGroup.getDescription());
    return info;
  }

  /**
   * description:
   *
   * <p>change history: date defect person comments
   * ---------------------------------------------------------------------------------------------------------------------
   *
   * @param recipeGroup
   * @return void
   * @exception
   * @author ho
   * @date 2020/5/25 14:43
   */
  @Override
  public void updateRecipeGroup(RecipeGroup.Info recipeGroup) {
    Validations.check(
        getRecipeGroup(recipeGroup.getRecipeGroupID()) == null,
        retCodeConfigEx.getRecipeGroupIdError());
    CimRecipeGroup cimRecipeGroup =
        baseCoreFactory.getBO(CimRecipeGroup.class, recipeGroup.getRecipeGroupID());
    cimRecipeGroup.setRecipeGroupInfo(convertToCoreInfo(recipeGroup));
  }

  /**
   * description:
   *
   * <p>change history: date defect person comments
   * ---------------------------------------------------------------------------------------------------------------------
   *
   * @param recipeGroup
   * @return void
   * @exception
   * @author ho
   * @date 2020/5/25 14:46
   */
  @Override
  public void deleteRecipeGroup(RecipeGroup.Info recipeGroup) {
    CimRecipeGroup cimRecipeGroup =
        baseCoreFactory.getBO(CimRecipeGroup.class, recipeGroup.getRecipeGroupID());
    recipeManager.removeRecipeGroup(cimRecipeGroup);
  }

  /**
   * description:
   *
   * <p>change history: date defect person comments
   * ---------------------------------------------------------------------------------------------------------------------
   *
   * @param seasonID
   * @return com.fa.cim.dto.Infos.Season
   * @exception
   * @author ho
   * @date 2020/5/21 11:15
   */
  @Override
  public Infos.Season getSeason(ObjectIdentifier seasonID) {
    CimSeason cimSeason = baseCoreFactory.getBO(CimSeason.class, seasonID);
    Validations.check(cimSeason == null, retCodeConfigEx.getSeasonNoExist());
    return new Infos.Season(cimSeason.getSeasonInfo());
  }

  /**
   * description:
   *
   * <p>change history: date defect person comments
   * ---------------------------------------------------------------------------------------------------------------------
   *
   * @param seasons
   * @return void
   * @exception
   * @author ho
   * @date 2020/5/21 11:15
   */
  @Override
  public void deleteSeasonPlan(List<Infos.Season> seasons) {
    if (seasons == null) {
      return;
    }
    for (Infos.Season season : seasons) {
      CimMachine cimMachine = baseCoreFactory.getBO(CimMachine.class, season.getEqpID());
      CimSeason cimSeason = baseCoreFactory.getBO(CimSeason.class, season.getSeasonID());
      cimMachine.removeSeason(cimSeason);
    }
  }

  /**
   * description:
   *
   * <p>change history: date defect person comments
   * ---------------------------------------------------------------------------------------------------------------------
   *
   * @param eqpID
   * @return java.util.List<com.fa.cim.dto.Infos.Season>
   * @exception
   * @author ho
   * @date 2020/5/21 12:44
   */
  @Override
  public List<Infos.Season> querySeason(ObjectIdentifier eqpID) {
    CimMachine cimMachine = baseCoreFactory.getBO(CimMachine.class, eqpID);
    List<CimSeason> cimSeasonList = cimMachine.allSeasons();
    if (CimArrayUtils.isEmpty(cimSeasonList)) {
      return null;
    }
    List<Infos.Season> seasonList = new ArrayList<>();
    for (CimSeason cimSeason : cimSeasonList) {
      seasonList.add(new Infos.Season(cimSeason.getSeasonInfo()));
    }
    return seasonList;
  }

  /**
   * description:
   *
   * <p>change history: date defect person comments
   * ---------------------------------------------------------------------------------------------------------------------
   *
   * @param eqpID
   * @return java.util.List<java.lang.String>
   * @exception
   * @author ho
   * @date 2020/6/23 11:10
   */
  @Override
  public List<String> getAllSeasonProduct(ObjectIdentifier eqpID) {
    AtomicReference<List<String>> result = new AtomicReference<>();
    Optional.ofNullable(productSpecificationManager.allProductSpecifications())
        .ifPresent(
            cimProductSpecifications -> {
              result.set(
                  cimProductSpecifications.stream()
                      .filter(
                          cimProductSpecification ->
                              CimStringUtils.equals(
                                  cimProductSpecification.getProductCategory().getIdentifier(),
                                  BizConstant.SP_PRODUCTCATEGORY_DUMMY))
                      .map(cimProductSpecification -> cimProductSpecification.getIdentifier())
                      .collect(Collectors.toList()));
            });
    return result.get();
  }

  /**
   * description:
   *
   * <p>change history: date defect person comments
   * ---------------------------------------------------------------------------------------------------------------------
   *
   * @param eqpID
   * @return java.util.List<java.lang.String>
   * @exception
   * @author ho
   * @date 2020/6/30 13:12
   */
  @Override
  public List<String> getAllSeasonProductRecipe(ObjectIdentifier eqpID) {
    List<String> recipes = new ArrayList<>();
    CimMachine cimMachine = baseCoreFactory.getBO(CimMachine.class, eqpID);
    Optional.ofNullable(recipeManager.allMachineRecipesFor(cimMachine))
        .ifPresent(
            cimMachineRecipes -> {
              cimMachineRecipes.forEach(
                  cimMachineRecipe -> recipes.add(cimMachineRecipe.getIdentifier()));
            });
    return recipes;
  }

  /**
   * description: 检查设备是否符合SeasonPlan条件
   *
   * <p>change history: date defect person comments
   * ---------------------------------------------------------------------------------------------------------------------
   *
   * @param eqpID 设备ID必须
   * @param chamberID 可以为null
   * @param recipeID 可以为null
   * @param productID 可以为null
   * @return void
   * @exception
   * @author ho
   * @date 2020/5/21 16:29
   */
  @Override
  public List<Infos.Season> checkMachineWhetherMeetsConditionsForSeason(
      ObjectIdentifier eqpID, String chamberID, String recipeID, String productID) {
    List<Infos.Season> activeSeasons = checkTheActiveSeason(eqpID, recipeID, null, null);
    if (CimArrayUtils.isEmpty(activeSeasons)) {
      return null;
    }
    return activeSeasons.stream()
        .filter(
            activeSeason -> {
              if (Arrays.asList(BizConstant.SEASON_TYPE_PM, BizConstant.SEASON_TYPE_RECIPEGROUP)
                  .contains(activeSeason.getSeasonType())) {
                return true;
              }
              if (!ObjectIdentifier.isEmpty(activeSeason.getProductID())
                  && !ObjectIdentifier.equalsWithValue(productID, activeSeason.getProductID())) {
                return false;
              }
              if (!checkRecipeOrChamber(
                  chamberID, activeSeason.getChambers(), Infos.SeasonChamber::getChamberID)) {
                return false;
              }
              if (!checkRecipeOrChamber(
                  recipeID, activeSeason.getRecipes(), Infos.SeasonProdRecipe::getRecipeID)) {
                return false;
              }
              return true;
            })
        .collect(Collectors.toList());
  }

  /**
   * description:
   *
   * <p>change history: date defect person comments
   * ---------------------------------------------------------------------------------------------------------------------
   *
   * @param eqpID
   * @param recipeID
   * @param productID
   * @return java.util.List<com.fa.cim.dto.Infos.Season>
   * @exception
   * @author ho
   * @date 2020/5/25 16:56
   */
  @Override
  public List<Infos.Season> checkMachineWhetherMeetsConditionsForSeason(
      Infos.ObjCommon objCommon, ObjectIdentifier eqpID, String recipeID, String productID) {
    Infos.EqpChamberInfo eqpChamberInfo =
        equipmentMethod.equipmentChamberInfoGetDR(objCommon, eqpID);
    if (eqpChamberInfo == null || CimArrayUtils.isEmpty(eqpChamberInfo.getEqpChamberStatuses())) {
      return checkMachineWhetherMeetsConditionsForSeason(eqpID, null, recipeID, productID);
    }
    List<Infos.Season> seasons = new ArrayList<>();
    eqpChamberInfo
        .getEqpChamberStatuses()
        .forEach(
            eqpChamberStatusInfo -> {
              if (!eqpChamberStatusInfo.isChamberAvailableFlag()) {
                return;
              }
              seasons.addAll(
                  checkMachineWhetherMeetsConditionsForSeason(
                      eqpID,
                      ObjectIdentifier.fetchValue(eqpChamberStatusInfo.getChamberID()),
                      recipeID,
                      productID));
            });
    return seasons;
  }

  /**
   * description:
   *
   * <p>change history: date defect person comments
   * ---------------------------------------------------------------------------------------------------------------------
   *
   * @param objCommon
   * @param startCassetteList
   * @return java.util.List<com.fa.cim.dto.Infos.Season>
   * @exception
   * @author ho
   * @date 2020/5/25 17:22
   */
  @Override
  public List<Infos.Season> checkMachineWhetherMeetsConditionsForSeason(
      Infos.ObjCommon objCommon,
      ObjectIdentifier eqpID,
      List<Infos.StartCassette> startCassetteList) {
    List<Infos.Season> seasons = new ArrayList<>();
    Optional.ofNullable(startCassetteList)
        .ifPresent(
            startCassetteList1 -> {
              startCassetteList1.forEach(
                  startCassette -> {
                    Optional.ofNullable(startCassette.getLotInCassetteList())
                        .ifPresent(
                            lotInCassettes -> {
                              lotInCassettes.forEach(
                                  lotInCassette -> {
                                    if (CimBooleanUtils.isFalse(lotInCassette.getMoveInFlag())) {
                                      return;
                                    }
                                    seasons.addAll(
                                        checkMachineWhetherMeetsConditionsForSeason(
                                            objCommon,
                                            eqpID,
                                            ObjectIdentifier.fetchValue(
                                                lotInCassette
                                                    .getStartRecipe()
                                                    .getMachineRecipeID()),
                                            ObjectIdentifier.fetchValue(
                                                lotInCassette.getProductID())));
                                  });
                            });
                  });
            });
    return seasons;
  }

  /**
   * description: seasonJobID由core生成,初始状态为Requested
   *
   * <p>change history: date defect person comments
   * ---------------------------------------------------------------------------------------------------------------------
   *
   * @param season
   * @return void
   * @exception
   * @author ho
   * @date 2020/5/22 14:34
   */
  @Override
  public Infos.SeasonJob createSeasonJob(
      Infos.ObjCommon objCommon, Infos.Season season, String claimMemo) {
    objectLockMethod.objectLock(objCommon, CimSeason.class, season.getSeasonID());
    Infos.SeasonJob seasonJob = new Infos.SeasonJob();
    seasonJob.setCondType(season.getCondType());
    seasonJob.setSeasonType(season.getSeasonType());
    seasonJob.setEqpID(season.getEqpID());
    seasonJob.setCreateTime(objCommon.getTimeStamp().getReportTimeStamp());
    seasonJob.setLastModifyTime(objCommon.getTimeStamp().getReportTimeStamp());
    seasonJob.setUserID(objCommon.getUser().getUserID());
    seasonJob.setPriority(season.getPriority());
    seasonJob.setSeasonID(season.getSeasonID());
    seasonJob.setSeasonJobStatus(BizConstant.SEASONJOB_STATUS_REQUESTED);
    seasonJob.setMaxIdleTime(season.getParam().getMaxIdleTime());
    seasonJob.setIntervalBetweenSeason(season.getParam().getIntervalBetweenSeason());
    //
    // seasonJob.setMinSeasonWaferPerChamber(season.getParam().getMinSeasonWaferPerChamber());
    //        seasonJob.setMinSeasonWaferPerJob(season.getParam().getMinSeasonWaferPerJob());
    seasonJob.setSeasonGroupFlag(season.getParam().getSeasonGroupFlag());
    //        seasonJob.setNoIdleFlag(season.getParam().getNoIdleFlag());
    seasonJob.setWaitFlag(season.getParam().getWaitFlag());
    //        seasonJob.setFromRecipeGroup(season.getParam().getFromRecipeGroup());
    //        seasonJob.setToRecipeGroup(season.getParam().getToRecipeGroup());

    seasonJob.setSeasonProductID(season.getSeasonProducts().get(0).getProductID());
    seasonJob.setMinSeasonWaferCount(season.getSeasonProducts().get(0).getQuantity());

    CimSeason cimSeason = baseCoreFactory.getBO(CimSeason.class, season.getSeasonID());
    CimSeasonJob cimSeasonJob = cimSeason.createSeasonJob();
    cimSeasonJob.setSeasonJobInfo(seasonJob.convert());

    // season job MakeEvent
    eventMethod.seasonJobEventMake(
        objCommon, BizConstant.SEASONPLAN_ACTION_CREATE, seasonJob, claimMemo);

    return new Infos.SeasonJob(cimSeasonJob.getSeasonJobInfo());
  }

  /**
   * description:
   *
   * <p>change history: date defect person comments
   * ---------------------------------------------------------------------------------------------------------------------
   *
   * @param objCommon
   * @param equipmentID
   * @param startCassetteList
   * @return void
   * @exception
   * @author ho
   * @date 2020/5/26 14:07
   */
  @Override
  public void checkSeasonAndCreateSeasonJobForOperation(
      Infos.ObjCommon objCommon,
      ObjectIdentifier equipmentID,
      List<Infos.StartCassette> startCassetteList,
      String seasonJobStatus,
      String claimMemo) {
    List<Infos.Season> seasons =
        checkMachineWhetherMeetsConditionsForSeason(objCommon, equipmentID, startCassetteList);
    if (!checkIfSeasonLot(objCommon, startCassetteList) && !CimArrayUtils.isEmpty(seasons)) {
      Validations.check(
          !CimArrayUtils.isEmpty(seasons),
          retCodeConfigEx.getSeasonRequired(),
          ObjectIdentifier.fetchValue(seasons.get(0).getSeasonProducts().get(0).getProductID()));
    } else {
      Optional.ofNullable(seasons)
          .ifPresent(
              seasons1 ->
                  seasons1.forEach(
                      season -> {
                        Infos.SeasonJob seasonJob = getSeasonJob(objCommon, season.getSeasonID());
                        if (seasonJob == null) {
                          seasonJob = createSeasonJob(objCommon, season, claimMemo);
                          seasonJob.setSeasonJobStatus(BizConstant.SEASONJOB_STATUS_REQUESTED);
                          CimSeasonJob cimSeasonJob =
                              baseCoreFactory.getBO(CimSeasonJob.class, seasonJob.getSeasonJobID());
                          cimSeasonJob.setSeasonJobInfo(seasonJob.convert());
                        }
                        for (Infos.SeasonProduct seasonProduct : season.getSeasonProducts()) {
                          if (ObjectIdentifier.equalsWithValue(
                                  seasonProduct.getProductID(), seasonJob.getSeasonProductID())) {
                            continue;
                          }
                          boolean productInCassetteList =
                              searchSeasonProductInCassetteList(
                                  seasonProduct, startCassetteList, seasonJob);
                          Validations.check(
                              !productInCassetteList,
                              retCodeConfigEx.getSeasonRequired(),
                              ObjectIdentifier.fetchValue(seasonProduct.getProductID()));
                          seasonJob.setSeasonProductID(seasonProduct.getProductID());
                          break;
                        }
                        boolean existProductLot = searchForProductLot(startCassetteList, seasonJob);
                        if (CimBooleanUtils.isTrue(season.getParam().getSeasonGroupFlag())) {
                          Validations.check(
                              !existProductLot, retCodeConfigEx.getProductLotRequired());
                        }
                        seasonJob.setSeasonJobStatus(seasonJobStatus);
                        switch (seasonJob.getSeasonType()) {
                          case BizConstant.SEASON_TYPE_IDLE:
                          case BizConstant.SEASON_TYPE_RECIPEIDLE:
                          case BizConstant.SEASON_TYPE_INTERVAL:
                          case BizConstant.SEASON_TYPE_PM:
                            seasonJob.setFromRecipe(null);
                            break;
                          case BizConstant.SEASON_TYPE_RECIPEGROUP:
                            seasonJob.setFromRecipe(
                                ObjectIdentifier.buildWithValue(season.getLastRecipe()));
                            break;
                        }
                      }));
    }
  }

  /**
   * description:
   *
   * <p>change history: date defect person comments
   * ---------------------------------------------------------------------------------------------------------------------
   *
   * @param seasonProduct
   * @param startCassetteList
   * @param seasonJob
   * @return boolean
   * @exception
   * @author ho
   * @date 2020/5/26 14:18
   */
  private boolean searchSeasonProductInCassetteList(
      Infos.SeasonProduct seasonProduct,
      List<Infos.StartCassette> startCassetteList,
      Infos.SeasonJob seasonJob) {
    for (Infos.StartCassette startCassette : startCassetteList) {
      for (Infos.LotInCassette lotInCassette : startCassette.getLotInCassetteList()) {
        if (CimBooleanUtils.isFalse(lotInCassette.getMoveInFlag())) {
          continue;
        }
        if (ObjectIdentifier.equalsWithValue(
                seasonProduct.getProductID(), lotInCassette.getProductID())
            && CimStringUtils.equals(lotInCassette.getSubLotType(), BizConstant.SEASON_LOT_TYPE)) {
          seasonJob.setSeasonLotID(lotInCassette.getLotID());
          seasonJob.setSeasonCarrierID(startCassette.getCassetteID());
          seasonJob.setToRecipe(lotInCassette.getStartRecipe().getMachineRecipeID());
          Validations.check(
              seasonProduct.getQuantity() > CimArrayUtils.getSize(lotInCassette.getLotWaferList()),
              retCodeConfigEx.getMinWaferCountError(),
              seasonProduct.getQuantity());
          seasonJob.setWaferQty(CimArrayUtils.getSize(lotInCassette.getLotWaferList()));
          return true;
        }
      }
    }
    return false;
  }

  /**
   * description:
   *
   * <p>change history: date defect person comments
   * ---------------------------------------------------------------------------------------------------------------------
   *
   * @param startCassetteList
   * @param seasonJob
   * @return boolean
   * @exception
   * @author ho
   * @date 2020/5/26 14:45
   */
  private boolean searchForProductLot(
      List<Infos.StartCassette> startCassetteList, Infos.SeasonJob seasonJob) {
    for (Infos.StartCassette startCassette : startCassetteList) {
      for (Infos.LotInCassette lotInCassette : startCassette.getLotInCassetteList()) {
        if (!CimStringUtils.equals(BizConstant.SEASON_LOT_TYPE, lotInCassette.getSubLotType())) {
          seasonJob.setLotID(lotInCassette.getLotID());
          seasonJob.setCarrierID(seasonJob.getCarrierID());
          return true;
        }
      }
    }
    return false;
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
   * @exception
   * @author ho
   * @date 2020/5/26 11:11
   */
  @Override
  public Infos.SeasonJob getSeasonJob(Infos.ObjCommon objCommon, ObjectIdentifier seasonID) {
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
   * @param eqpID
   * @return java.util.List<com.fa.cim.dto.Infos.SeasonJob>
   * @exception
   * @author ho
   * @date 2020/5/27 13:58
   */
  @Override
  public List<Infos.SeasonJob> getMachineSeasonJob(
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
      CimLot seasonLot = seasonJob.getSeasonLot();
      String seasonType = seasonJob.getSeasonType();
      //BUG-3094
//      if (seasonLot == null && CimStringUtils.equals(seasonType, BizConstant.SEASON_TYPE_PM ) && CimStringUtils.unEqual(objCommon.getTransactionID(), TransactionIDEnum.SEASON_WHAT_NEXT_INQ.getValue())) {
//        deleteSeasonJob(objCommon, new Infos.SeasonJob(seasonJob.getSeasonJobInfo()));
//        continue;
//      }
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
   * description: 更新seasonJob状态 MoveInReserve->Reserved, MoveIn->Executing,MoveOut->Completed
   *
   * <p>change history: date defect person comments
   * ---------------------------------------------------------------------------------------------------------------------
   *
   * @param seasonJob
   * @param seasonJobStatus
   * @return void
   * @exception
   * @author ho
   * @date 2020/5/25 10:23
   */
  @Override
  public void updateSeasonJob(Infos.SeasonJob seasonJob, String seasonJobStatus) {
    seasonJob.setSeasonJobStatus(seasonJobStatus);
    CimSeasonJob cimSeasonJob =
        baseCoreFactory.getBO(CimSeasonJob.class, seasonJob.getSeasonJobID());

    cimSeasonJob.setSeasonJobInfo(seasonJob.convert());
  }

  /**
   * description:
   *
   * <p>change history: date defect person comments
   * ---------------------------------------------------------------------------------------------------------------------
   *
   * @param chamberID
   * @param chambers
   * @param function
   * @return boolean
   * @exception
   * @author ho
   * @date 2020/5/21 18:21
   */
  private <T> boolean checkRecipeOrChamber(
      String chamberID, List<T> chambers, Function<T, ObjectIdentifier> function) {
    AtomicBoolean matchChamber = new AtomicBoolean(false);
    if (CimStringUtils.isEmpty(chamberID)) {
      matchChamber.set(true);
      chambers = null;
    }
    Optional.ofNullable(chambers)
        .ifPresent(
            seasonChambers -> {
              seasonChambers.forEach(
                  seasonChamber -> {
                    Pattern pattern =
                        Pattern.compile(
                            ObjectIdentifier.fetchValue(function.apply(seasonChamber))
                                .replaceAll("\\*", ".*"));
                    Matcher matcher = pattern.matcher(chamberID);
                    if (matcher.find()) {
                      matchChamber.set(true);
                    }
                  });
            });
    return matchChamber.get();
  }

  /**
   * description: 获取所有激活的season
   *
   * <p>change history: date defect person comments
   * ---------------------------------------------------------------------------------------------------------------------
   *
   * @param recipeID
   * @param eqpID
   * @return java.util.List<com.fa.cim.dto.Infos.Season>
   * @exception
   * @author ho
   * @date 2020/6/19 12:59
   */
  @Override
  public List<Infos.Season> checkTheActiveSeason(
      String recipeID,
      ObjectIdentifier eqpID,
      ObjectIdentifier lotID,
      ObjectIdentifier mRecipeID,
      ObjectIdentifier lRecipeID) {
    CimMachine cimMachine = baseCoreFactory.getBO(CimMachine.class, eqpID);
    List<Infos.Season> seasons = querySeason(eqpID);
    if (CimArrayUtils.isEmpty(seasons)) {
      return null;
    }
    if (lotID != null) {
      CimLot cimLot = baseCoreFactory.getBO(CimLot.class, lotID);
      seasons =
          seasons.stream()
              .filter(
                  season ->
                      ObjectIdentifier.isEmpty(season.getProductID())
                          || ObjectIdentifier.equalsWithValue(
                              season.getProductID(), cimLot.getProductSpecificationID()))
              .collect(Collectors.toList());
    }
    seasons = checkSeasonForRecipe(null, recipeID, seasons);
    return seasons.stream()
        .filter(
            season -> {
              Timestamp lastSBYTime = null;
              switch (season.getSeasonType()) {
                case BizConstant.SEASON_TYPE_IDLE:
                  if (season.getLastSeasonTime() == null) {
                    lastSBYTime = season.getParam().getFirstTriggerTime();
                    if (lastSBYTime != null) {
                      return lessThanCurrentTime(lastSBYTime, 0);
                    }
                  }
                  Infos.EqpChamberStatusInfo chamber =
                      checkSeasonForChamber(null, eqpID, season, lotID, mRecipeID, lRecipeID);
                  lastSBYTime = getLastSBYTime(chamber);
                  if (lastSBYTime == null) {
                    lastSBYTime = getLastSBYTime(eqpID, season);
                  }
                  if (lastSBYTime == null) {
                    lastSBYTime = season.getLastModifyTime();
                  }
                  return lessThanCurrentTime(lastSBYTime, season.getParam().getMaxIdleTime())
                      && lessThanCurrentTime(
                          season.getLastSeasonTime(), season.getParam().getMaxIdleTime());
                case BizConstant.SEASON_TYPE_RECIPEIDLE:
                  if (mRecipeID == null) {
                    return false;
                  }
                  if (season.getLastSeasonTime() == null) {
                    lastSBYTime = season.getParam().getFirstTriggerTime();
                    if (lastSBYTime != null) {
                      return lessThanCurrentTime(lastSBYTime, 0);
                    }
                  }
                  CimMachineRecipe cimMachineRecipe =
                      baseCoreFactory.getBO(CimMachineRecipe.class, mRecipeID);
                  lastSBYTime = cimMachineRecipe.getLastUsedTime(cimMachine);
                  if (lastSBYTime == null) {
                    lastSBYTime = season.getLastModifyTime();
                  }
                  return lessThanCurrentTime(lastSBYTime, season.getParam().getMaxIdleTime())
                      && lessThanCurrentTime(
                          season.getLastSeasonTime(), season.getParam().getMaxIdleTime());
                case BizConstant.SEASON_TYPE_INTERVAL:
                  Timestamp lastSeasonTime = season.getLastSeasonTime();
                  if (lastSeasonTime == null) {
                    lastSeasonTime = season.getParam().getFirstTriggerTime();
                    if (lastSeasonTime != null) {
                      return lessThanCurrentTime(lastSeasonTime, 0);
                    }
                  }
                  if (lastSeasonTime == null) {
                    lastSeasonTime = season.getLastModifyTime();
                  }
                  Integer intervalBetweenSeason = season.getParam().getIntervalBetweenSeason();
                  return lessThanCurrentTime(lastSeasonTime, intervalBetweenSeason);
                case BizConstant.SEASON_TYPE_PM:
                  return CimBooleanUtils.isTrue(season.getPmFlag());
                case BizConstant.SEASON_TYPE_RECIPEGROUP:
                  return recipeInRecipeGroup(recipeID, season.getParam().getToRecipeGroup())
                      && recipeInRecipeGroup(
                          cimMachine.getUsedMachineRecipeID().getValue(),
                          season.getParam().getFromRecipeGroup());
                default:
                  return false;
              }
            })
        .collect(Collectors.toList());
  }

  /**
   * description:
   *
   * <p>change history: date defect person comments
   * ---------------------------------------------------------------------------------------------------------------------
   *
   * @param season
   * @return void
   * @exception
   * @author ho
   * @date 2020/5/25 15:00
   */
  @Override
  public void createSeason(Infos.Season season) {
    CimMachine cimMachine = baseCoreFactory.getBO(CimMachine.class, season.getEqpID());
    try {
      CimSeason cimSeason = cimMachine.createSeasonNamed(season.getSeasonID().getValue());
      cimSeason.setSeasonInfo(season.convert());
    } catch (CoreFrameworkException ex) {
      Validations.check(new OmCode(ex.getCode(), ex.getMessage()));
    }
  }

  /**
   * description:
   *
   * <p>change history: date defect person comments
   * ---------------------------------------------------------------------------------------------------------------------
   *
   * @param season
   * @return void
   * @exception
   * @author ho
   * @date 2020/5/25 15:04
   */
  @Override
  public void updateSeason(Infos.Season season) {
    Validations.check(getSeason(season.getSeasonID()) == null, retCodeConfigEx.getSeasonNoExist());
    CimSeason cimSeason = baseCoreFactory.getBO(CimSeason.class, season.getSeasonID());
    cimSeason.setSeasonInfo(season.convert());
  }

  /**
   * description:
   *
   * <p>change history: date defect person comments
   * ---------------------------------------------------------------------------------------------------------------------
   *
   * @param recipe
   * @param recipeGroup
   * @return boolean
   * @exceptionh
   * @author ho
   * @date 2020/5/22 13:53
   */
  private boolean recipeInRecipeGroup(String recipe, ObjectIdentifier recipeGroup) {
    RecipeGroup.Info group = getRecipeGroup(recipeGroup);
    if (group == null) {
      return false;
    }
    return group.getRecipeInfos().stream()
        .anyMatch(recipeInfo -> CimStringUtils.equals(recipe, recipeInfo.getValue()));
  }

  /**
   * description:
   *
   * <p>change history: date defect person comments
   * ---------------------------------------------------------------------------------------------------------------------
   *
   * @param target
   * @param hours
   * @return boolean
   * @exception
   * @author ho
   * @date 2020/5/22 13:06
   */
  private boolean lessThanCurrentTime(Timestamp target, long hours) {
    return target
        .toLocalDateTime()
        .plus(hours, ChronoUnit.MINUTES)
        .isBefore(ChronoLocalDateTime.from(LocalDateTime.now()));
  }

  /**
   * description: 检查所有激活的season,带优先级
   *
   * <p>change history: date defect person comments
   * ---------------------------------------------------------------------------------------------------------------------
   *
   * @param eqpID
   * @return java.util.List<com.fa.cim.dto.Infos.Season>
   * @exception
   * @author ho
   * @date 2020/5/21 17:24
   */
  @Override
  public List<Infos.Season> checkTheActiveSeason(
      ObjectIdentifier eqpID, String recipeID, ObjectIdentifier lotID, ObjectIdentifier lRecipeID) {
    List<Infos.Season> seasons =
        checkTheActiveSeason(
            recipeID, eqpID, lotID, ObjectIdentifier.buildWithValue(recipeID), lRecipeID);
    if (CimArrayUtils.isEmpty(seasons)) {
      return seasons;
    }
    List<Infos.Season> prioritySeasons = new ArrayList<>();
    Infos.Season prioritySeason = null;
    for (Infos.Season season : seasons) {
      if (season.getPriority() == null) {
        prioritySeasons.add(season);
        continue;
      }
      if (prioritySeason == null) {
        prioritySeason = season;
      }
      if (season.getPriority() > prioritySeason.getPriority()) {
        prioritySeason = season;
      }
    }
    if (prioritySeason != null) {
      prioritySeasons.add(prioritySeason);
    }
    return prioritySeasons;
  }

  /**
   * description:
   *
   * <p>change history: date defect person comments
   * ---------------------------------------------------------------------------------------------------------------------
   *
   * @param eqpID
   * @return java.util.List<com.fa.cim.dto.Infos.Season>
   * @exception
   * @author ho
   * @date 2020/6/9 17:55
   */
  @Override
  public List<Infos.Season> getMachineSeasonForPM(ObjectIdentifier eqpID) {
    CimMachine cimMachine = baseCoreFactory.getBO(CimMachine.class, eqpID);
    if (null == cimMachine) {
      return null;
    }
    List<CimSeason> cimSeasons = cimMachine.allSeasons();
    if (CimArrayUtils.isEmpty(cimSeasons)) {
      return null;
    }
    List<Infos.Season> pmSeasons = new ArrayList<>();
    for (CimSeason cimSeason : cimSeasons) {
      if (CimStringUtils.equals(cimSeason.getSeasonType(), BizConstant.SEASON_TYPE_PM)) {
        pmSeasons.add(new Infos.Season(cimSeason.getSeasonInfo()));
      }
    }
    return pmSeasons;
  }

  /**
   * description:
   *
   * <p>change history: date defect person comments
   * ---------------------------------------------------------------------------------------------------------------------
   *
   * @param seasonParam
   * @return void
   * @exception
   * @author ho
   * @date 2020/5/20 17:26
   */
  private void checkParam(Infos.SeasonParam seasonParam) {
    //        Validations.check(seasonParam.getMinSeasonWaferPerChamber()==null
    //                        &&seasonParam.getMinSeasonWaferPerJob()==null,
    //                retCodeConfigEx.getSeasonWaferCountEmpty());
    //        Validations.check(BooleanUtils.isTrue(seasonParam.getNoIdleFlag())&&
    //                        BooleanUtils.isFalse(seasonParam.getSeasonGroupFlag()),
    //                retCodeConfigEx.getNoIdleFlagError());
  }

  /**
   * description:
   *
   * <p>change history: date defect person comments
   * ---------------------------------------------------------------------------------------------------------------------
   *
   * @param objCommon
   * @param startCassetteList
   * @param eqpID
   * @return boolean
   * @exception
   * @author ho
   * @date 2020/6/15 16:41
   */
  @Override
  public void checkSeasonForMoveInReserve(
      Infos.ObjCommon objCommon,
      List<Infos.StartCassette> startCassetteList,
      ObjectIdentifier eqpID) {
    if (CimStringUtils.equals(
        objCommon.getTransactionID(), TransactionIDEnum.SEASON_OPERATION_REQ.getValue())) {
      return;
    }
    List<Infos.SeasonJob> seasonJobList = getMachineSeasonJob(objCommon, eqpID);
    if (!CimArrayUtils.isEmpty(seasonJobList)) {
      Validations.check(
          true, seasonJobList.get(0), retCodeConfigEx.getSeasonRequired(), seasonJobList.get(0));
    }
    if (CimArrayUtils.isEmpty(startCassetteList)) {
      return;
    }
    for (Infos.StartCassette startCassette : startCassetteList) {
      List<Infos.LotInCassette> lotInCassetteList = startCassette.getLotInCassetteList();
      if (CimArrayUtils.isEmpty(lotInCassetteList)) {
        continue;
      }
      for (Infos.LotInCassette lotInCassette : lotInCassetteList) {
        if (CimBooleanUtils.isFalse(lotInCassette.getMoveInFlag())) {
          continue;
        }
        // BUG-2469 not Validaiton Season plan if exist
        List<Infos.Season> activeSeason =
            checkTheActiveSeason(
                eqpID,
                ObjectIdentifier.fetchValue(lotInCassette.getStartRecipe().getMachineRecipeID()),
                lotInCassette.getLotID(),
                lotInCassette.getStartRecipe().getLogicalRecipeID());
        if (!CimArrayUtils.isEmpty(activeSeason)) {
          Validations.check(
              true,
              activeSeason.get(0),
              retCodeConfigEx.getSeasonRequired(),
              ObjectIdentifier.fetchValue(activeSeason.get(0).getSeasonID()));
        }
      }
    }
  }

  /**
   * description:
   *
   * <p>change history: date defect person comments
   * ---------------------------------------------------------------------------------------------------------------------
   *
   * @param objCommon
   * @param equipmentID
   * @param cassetteID
   * @return boolean
   * @exception
   * @author ho
   * @date 2020/6/16 15:17
   */
  @Override
  public void checkSeasonForLoading(
      Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, ObjectIdentifier cassetteID) {
    List<Infos.SeasonJob> seasonJobList = getMachineSeasonJob(objCommon, equipmentID);
    if (CimArrayUtils.isEmpty(seasonJobList)) {
      return;
    }
    for (Infos.SeasonJob seasonJob : seasonJobList) {
      //            ObjectIdentifier carrierID = seasonJob.getCarrierID();
    }
  }

  /**
   * description:
   *
   * <p>change history: date defect person comments
   * ---------------------------------------------------------------------------------------------------------------------
   *
   * @param objCommon
   * @param equipmentID
   * @param cassetteID
   * @return boolean
   * @exception
   * @author ho
   * @date 2020/6/16 15:30
   */
  @Override
  public boolean checkSeasonForCassette(
      Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, ObjectIdentifier cassetteID) {
    AtomicBoolean isCheckSeasonForCassette = new AtomicBoolean(false);
    Infos.LotListInCassetteInfo lotListInCassetteInfo = null;
    try {
      lotListInCassetteInfo = cassetteMethod.cassetteGetLotList(objCommon, cassetteID);
    } catch (Exception ex) {
      return isCheckSeasonForCassette.get();
    }
    Optional.ofNullable(lotListInCassetteInfo.getLotIDList())
        .ifPresent(
            lotIDList -> {
              isCheckSeasonForCassette.set(
                  lotIDList.stream()
                      .anyMatch(
                          lotID -> {
                            String subLotTypeGetDR = lotMethod.lotSubLotTypeGetDR(objCommon, lotID);
                            if (CimStringUtils.equals(
                                subLotTypeGetDR, BizConstant.SEASON_LOT_TYPE)) {
                              return false;
                            }
                            try {
                              Outputs.ObjLotRecipeGetOut lotRecipeGetOut =
                                  lotMethod.lotRecipeGet(objCommon, equipmentID, lotID);
                              CimLot cimLot = baseCoreFactory.getBO(CimLot.class, lotID);
                              List<Infos.Season> activeSeason =
                                  checkTheActiveSeason(
                                      equipmentID,
                                      ObjectIdentifier.fetchValue(
                                          lotRecipeGetOut.getMachineRecipeId()),
                                      lotID,
                                      lotRecipeGetOut.getLogicalRecipeId());
                              if (CimArrayUtils.isEmpty(activeSeason)) {
                                return false;
                              }
                              return activeSeason.stream()
                                  .anyMatch(
                                      season ->
                                          CimStringUtils.isEmpty(
                                                  ObjectIdentifier.fetchValue(
                                                      season.getProductID()))
                                              || ObjectIdentifier.equalsWithValue(
                                                  cimLot.getProductSpecificationID(),
                                                  season.getProductID()));
                            } catch (ServiceException ex) {
                              return false;
                            }
                          }));
            });
    return isCheckSeasonForCassette.get();
  }

  /**
   * description:
   *
   * <p>change history: date defect person comments
   * ---------------------------------------------------------------------------------------------------------------------
   *
   * @param objCommon
   * @param equipmentID
   * @param cassetteID
   * @return boolean
   * @exception
   * @author ho
   * @date 2020/6/16 15:31
   */
  @Override
  public boolean checkSeasonForSeasonJob(
      Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, ObjectIdentifier cassetteID) {
    AtomicBoolean isCheckSeasonForLoading = new AtomicBoolean(false);
    List<Infos.SeasonJob> machineSeasonJob = getMachineSeasonJob(objCommon, equipmentID);
    if (CimArrayUtils.isEmpty(machineSeasonJob)) {
      return isCheckSeasonForLoading.get();
    }
    machineSeasonJob.forEach(
        seasonJob -> {
          String carriers = ObjectIdentifier.fetchValue(seasonJob.getCarrierID());
          String scarriers = ObjectIdentifier.fetchValue(seasonJob.getSeasonCarrierID());
          if (!CimStringUtils.isEmpty(carriers)) {
            scarriers += BizConstant.SEASON_SEPARATOR + carriers;
          }
          for (String carrier : scarriers.split(BizConstant.SEASON_SEPARATOR)) {
            if (ObjectIdentifier.equalsWithValue(carrier, cassetteID)) {
              isCheckSeasonForLoading.set(true);
              return;
            }
          }
        });
    return isCheckSeasonForLoading.get();
  }

  /**
   * description:
   *
   * <p>change history: date defect person comments
   * ---------------------------------------------------------------------------------------------------------------------
   *
   * @param objCommon
   * @param equipmentID
   * @param startCassetteList
   * @return boolean
   * @exception
   * @author ho
   * @date 2020/6/17 17:22
   */
  @Override
  public void checkSeasonForMoveIn(
      Infos.ObjCommon objCommon,
      ObjectIdentifier equipmentID,
      List<Infos.StartCassette> startCassetteList) {
    if (CimArrayUtils.isEmpty(startCassetteList)) {
      return;
    }
    CimMachine cimMachine = baseCoreFactory.getBO(CimMachine.class, equipmentID);
    if (cimMachine == null) {
      return;
    }
    List<Infos.SeasonJob> seasonJobs = getMachineSeasonJob(objCommon, equipmentID);
    if (!CimArrayUtils.isEmpty(seasonJobs)) {
      for (Infos.SeasonJob seasonJob : seasonJobs) {
        // 当season group flag为true时,检查production lot是否loading
        if (CimBooleanUtils.isTrue(seasonJob.getWaitFlag())) {
          String carrierStr = ObjectIdentifier.fetchValue(seasonJob.getCarrierID());
          Validations.check(
              CimStringUtils.isEmpty(carrierStr), retCodeConfigEx.getProductLotRequired());
          List<MachineDTO.MachineCassette> machineCassettes = cimMachine.allLoadedCassettes();
          Validations.check(
              CimArrayUtils.isEmpty(machineCassettes), retCodeConfigEx.getProductLotRequired());
          for (String carrier : carrierStr.split(BizConstant.SEASON_SEPARATOR)) {
            if (machineCassettes.stream()
                .noneMatch(
                    startCassette ->
                            ObjectIdentifier.equalsWithValue(carrier, startCassette.getCassetteID()))) {
              Validations.check(true, retCodeConfigEx.getProductLotRequired());
            }
          }
        }
        // PM时,检查是否为当前的season product
        if (CimStringUtils.equals(seasonJob.getSeasonType(), BizConstant.SEASON_TYPE_PM)) {
          for (Infos.StartCassette startCassette : startCassetteList) {
            Optional.ofNullable(startCassette.getLotInCassetteList())
                .ifPresent(
                    lotInCassettes ->
                        Validations.check(
                            lotInCassettes.stream()
                                .anyMatch(
                                    lotInCassette ->
                                        !ObjectIdentifier.equalsWithValue(
                                                lotInCassette.getProductID(),
                                                seasonJob.getSeasonProductID())),
                            retCodeConfigEx.getLotNotMeetSeason()));
          }
          return;
        }
        // 存在season job检查是否是season job关联的season carrier
        String seasonCarrierStr = ObjectIdentifier.fetchValue(seasonJob.getSeasonCarrierID());
        for (String seasonCarrier : seasonCarrierStr.split(BizConstant.SEASON_SEPARATOR)) {
          if (startCassetteList.stream()
              .anyMatch(
                  startCassette ->
                          ObjectIdentifier.equalsWithValue(
                                  seasonCarrier, startCassette.getCassetteID()))) {
            return;
          }
        }
      }
      Validations.check(true, retCodeConfigEx.getLotNotMeetSeason());
    } else {
      // 检查是否有激活的season需要做
      checkSeasonForMoveInReserve(objCommon, startCassetteList, equipmentID);
    }
  }

  /**
   * description:
   *
   * <p>change history: date defect person comments
   * ---------------------------------------------------------------------------------------------------------------------
   *
   * @param objCommon
   * @param equipmentID
   * @return void
   * @exception
   * @author ho
   * @date 2020/6/18 10:20
   */
  @Override
  public void deleteSeasonJobForMachine(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID) {
    CimMachine machine = baseCoreFactory.getBO(CimMachine.class, equipmentID);
    List<CimSeason> seasons = machine.allSeasons();
    Optional.ofNullable(seasons)
        .ifPresent(
            seasonList -> {
              seasonList.forEach(
                  season -> {
                    season.removeSeasonJob();
                  });
            });
  }

  /**
   * description:
   *
   * <p>change history: date defect person comments
   * ---------------------------------------------------------------------------------------------------------------------
   *
   * @param objCommon
   * @param seasonJob
   * @return void
   * @exception
   * @author ho
   * @date 2020/6/18 10:37
   */
  @Override
  public void deleteSeasonJob(Infos.ObjCommon objCommon, Infos.SeasonJob seasonJob) {
    CimSeasonJob cimSeasonJob =
        baseCoreFactory.getBO(CimSeasonJob.class, seasonJob.getSeasonJobID());
    cimSeasonJob.remove();
  }

  /**
   * description:
   *
   * <p>change history: date defect person comments
   * ---------------------------------------------------------------------------------------------------------------------
   *
   * @param objCommon
   * @param equipmentID
   * @return com.fa.cim.common.support.ObjectIdentifier
   * @exception
   * @author ho
   * @date 2020/6/19 17:13
   */
  @Override
  public ObjectIdentifier getLastMachineRecipeForMachine(
      Infos.ObjCommon objCommon, ObjectIdentifier equipmentID) {
    CimMachine cimMachine = baseCoreFactory.getBO(CimMachine.class, equipmentID);
    CimMachineRecipe usedMachineRecipe = cimMachine.getUsedMachineRecipe();
    if (usedMachineRecipe == null) {
      return null;
    }
    return ObjectIdentifier.build(
        usedMachineRecipe.getIdentifier(), usedMachineRecipe.getPrimaryKey());
  }

  /**
   * description:
   *
   * <p>change history: date defect person comments
   * ---------------------------------------------------------------------------------------------------------------------
   *
   * @param objCommon
   * @param equipmentID
   * @param cassetteID
   * @return void
   * @exception
   * @author ho
   * @date 2020/6/20 11:24
   */
  @Override
  public void checkSeasonForSeasonLot(
      Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, ObjectIdentifier cassetteID) {
    List<Infos.SeasonJob> machineSeasonJob = getMachineSeasonJob(objCommon, equipmentID);
    Optional.ofNullable(machineSeasonJob)
        .ifPresent(
            seasonJobs -> {
              seasonJobs.forEach(
                  seasonJob -> {
                    ObjectIdentifier seasonProductID = seasonJob.getSeasonProductID();
                    Infos.LotListInCassetteInfo lotListInCassetteInfo =
                        cassetteMethod.cassetteGetLotList(objCommon, cassetteID);
                    lotListInCassetteInfo
                        .getLotIDList()
                        .forEach(
                            lotID -> {
                              Outputs.ObjLotProductIDGetOut objLotProductIDGetOut =
                                  lotMethod.lotProductIDGet(objCommon, lotID);
                              seasonJob.setSeasonLotID(lotID);
                              try {
                                Outputs.ObjLotRecipeGetOut objLotRecipeGetOut =
                                    lotMethod.lotRecipeGet(objCommon, equipmentID, lotID);
                                seasonJob.setSeasonRcpID(objLotRecipeGetOut.getMachineRecipeId());
                              } catch (ServiceException ex) {
                              }
                              CimLot lot = baseCoreFactory.getBO(CimLot.class, lotID);
                              Validations.check(
                                  !ObjectIdentifier.equalsWithValue(
                                          seasonProductID, objLotProductIDGetOut.getProductID()),
                                  retCodeConfigEx.getLotNotMeetSeason());
                            });
                    seasonJob.setSeasonCarrierID(cassetteID);
                    this.updateSeasonJob(seasonJob, seasonJob.getSeasonJobStatus());
                  });
            });
  }

  /**
   * description:
   *
   * <p>change history: date defect person comments
   * ---------------------------------------------------------------------------------------------------------------------
   *
   * @param objCommon
   * @param equipmentID
   * @param season
   * @return void
   * @exception
   * @author ho
   * @date 2020/6/20 14:30
   */
  @Override
  public Infos.EqpChamberStatusInfo checkSeasonForChamber(
      Infos.ObjCommon objCommon,
      ObjectIdentifier equipmentID,
      Infos.Season season,
      ObjectIdentifier lotID,
      ObjectIdentifier mRecipeID,
      ObjectIdentifier lRecipeID) {
    if (ObjectIdentifier.isEmptyWithValue(mRecipeID) || ObjectIdentifier.isEmptyWithValue(lRecipeID)) {
      return null;
    }
    if (season == null) {
      return null;
    }
    List<Pattern> chamberPatterns = new ArrayList<>();
    List<Infos.SeasonChamber> chambers = season.getChambers();
    if (CimArrayUtils.isEmpty(chambers)) {
      return null;
    }
    for (Infos.SeasonChamber chamber : chambers) {
      String value = ObjectIdentifier.fetchValue(chamber.getChamberID());
      if (CimStringUtils.isEmpty(value)) {
        continue;
      }
      chamberPatterns.add(Pattern.compile(value.replace("\\*", ".*")));
    }
    if (chamberPatterns.isEmpty()) {
      return null;
    }
    Infos.EqpChamberInfo eqpChamberInfo =
        equipmentMethod.equipmentChamberInfoGetDR(objCommon, equipmentID);
    if (eqpChamberInfo == null || CimArrayUtils.isEmpty(eqpChamberInfo.getEqpChamberStatuses())) {
      return null;
    }
    Inputs.ObjLogicalRecipeCandidateChamberInfoGetByMachineRecipeIn
        objLogicalRecipeCandidateChamberInfoGetByMachineRecipeIn =
            new Inputs.ObjLogicalRecipeCandidateChamberInfoGetByMachineRecipeIn();
    objLogicalRecipeCandidateChamberInfoGetByMachineRecipeIn.setLotID(lotID);
    objLogicalRecipeCandidateChamberInfoGetByMachineRecipeIn.setEquipmentID(equipmentID);
    objLogicalRecipeCandidateChamberInfoGetByMachineRecipeIn.setLogicalRecipeID(lRecipeID);
    objLogicalRecipeCandidateChamberInfoGetByMachineRecipeIn.setMachineRecipeID(mRecipeID);
    objLogicalRecipeCandidateChamberInfoGetByMachineRecipeIn.setInhibitCheckFlag(false);
    Outputs.ObjLogicalRecipeCandidateChamberInfoGetByMachineRecipeOut
        objLogicalRecipeCandidateChamberInfoGetByMachineRecipeOut =
            logicalRecipeMethod.logicalRecipeCandidateChamberInfoGetByMachineRecipe(
                objCommon, objLogicalRecipeCandidateChamberInfoGetByMachineRecipeIn);
    if (objLogicalRecipeCandidateChamberInfoGetByMachineRecipeOut == null
        || CimArrayUtils.isEmpty(
            objLogicalRecipeCandidateChamberInfoGetByMachineRecipeOut.getCandidateChamberList())) {
      return null;
    }
    for (Infos.CandidateChamber candidateChamber :
        objLogicalRecipeCandidateChamberInfoGetByMachineRecipeOut.getCandidateChamberList()) {
      if (candidateChamber == null || CimArrayUtils.isEmpty(candidateChamber.getChamberList())) {
        continue;
      }
      for (Infos.Chamber chamber : candidateChamber.getChamberList()) {
        String value = ObjectIdentifier.fetchValue(chamber.getChamberID());
        for (Pattern chamberPattern : chamberPatterns) {
          Matcher matcher = chamberPattern.matcher(value);
          if (matcher.find()) {
            for (Infos.EqpChamberStatusInfo eqpChamberStatus :
                eqpChamberInfo.getEqpChamberStatuses()) {
              if (ObjectIdentifier.equalsWithValue(eqpChamberStatus.getChamberID(), value)) {
                return eqpChamberStatus;
              }
            }
          }
        }
      }
    }
    return null;
  }

  /**
   * description:
   *
   * <p>change history: date defect person comments
   * ---------------------------------------------------------------------------------------------------------------------
   *
   * @param objCommon
   * @param recipeID
   * @param seasons
   * @return void
   * @exception
   * @author ho
   * @date 2020/6/20 14:42
   */
  @Override
  public List<Infos.Season> checkSeasonForRecipe(
      Infos.ObjCommon objCommon, String recipeID, List<Infos.Season> seasons) {
    List<Infos.Season> seasonList = new ArrayList<>();
    if (CimStringUtils.isEmpty(recipeID)) {
      return seasonList;
    }
    seasons.forEach(
        season -> {
          if (CimArrayUtils.isEmpty(season.getRecipes())) {
            seasonList.add(season);
          }
          if (season.getRecipes().stream()
              .anyMatch(
                  recipe -> {
                    String _recipe = ObjectIdentifier.fetchValue(recipe.getRecipeID());
                    _recipe = _recipe == null ? "" : _recipe;
                    Pattern pattern = Pattern.compile(_recipe.replace("\\*", ".*"));
                    Matcher matcher = pattern.matcher(recipeID);
                    return matcher.find();
                  })) {
            seasonList.add(season);
          }
          ;
        });
    return seasonList;
  }

  /**
   * description:
   *
   * <p>change history: date defect person comments
   * ---------------------------------------------------------------------------------------------------------------------
   *
   * @param eqpID
   * @return java.sql.Timestamp
   * @exception
   * @author ho
   * @date 2020/7/1 12:54
   */
  @Override
  public Timestamp getLastSBYTime(ObjectIdentifier eqpID, Infos.Season season) {
    CimMachine cimMachine = baseCoreFactory.getBO(CimMachine.class, eqpID);
    CimE10State e10State = cimMachine.getCurrentMachineState().getE10State();
    if (CimStringUtils.equals(e10State.getIdentifier(), BizConstant.SP_E10STATE_STANDBY)) {
      return cimMachine.getLastStatusChangeTimeStamp();
    }
    return season.getLastSBYTime();
  }

  /**
   * description:
   *
   * <p>change history: date defect person comments
   * ---------------------------------------------------------------------------------------------------------------------
   *
   * @param eqpID
   * @return java.sql.Timestamp
   * @exception
   * @author ho
   * @date 2020/7/1 13:18
   */
  @Override
  public Timestamp getLastSeasonTime(ObjectIdentifier eqpID) {
    AtomicReference<Timestamp> lastSeasonTime = new AtomicReference<>(null);
    CimMachine cimMachine = baseCoreFactory.getBO(CimMachine.class, eqpID);
    Optional.ofNullable(cimMachine.allSeasons())
        .ifPresent(
            cimSeasons -> {
              cimSeasons.stream()
                  .anyMatch(
                      cimSeason -> {
                        lastSeasonTime.set(cimSeason.getLastSeasonTime());
                        return lastSeasonTime.get() != null;
                      });
            });
    return lastSeasonTime.get();
  }

  /**
   * description:
   *
   * <p>change history: date defect person comments
   * ---------------------------------------------------------------------------------------------------------------------
   *
   * @param chamberStatusInfo
   * @return java.sql.Timestamp
   * @exception
   * @author ho
   * @date 2020/7/8 10:17
   */
  @Override
  public Timestamp getLastSBYTime(Infos.EqpChamberStatusInfo chamberStatusInfo) {
    if (chamberStatusInfo == null) {
      return null;
    }
    if (ObjectIdentifier.equalsWithValue(
            chamberStatusInfo.getE10Status(), BizConstant.SP_E10STATE_STANDBY)) {
      return CimDateUtils.convertTo(chamberStatusInfo.getChangeTimeStamp());
    }
    return null;
  }

  /**
   * description:
   *
   * <p>change history: date defect person comments
   * ---------------------------------------------------------------------------------------------------------------------
   *
   * @param objCommonIn
   * @param equipmentId
   * @param equipmentStatusCode
   * @param claimMemo
   * @return void
   * @exception
   * @author ho
   * @date 2020/7/27 14:02
   */
  @Override
  public void makeSeasonForPM(
      Infos.ObjCommon objCommonIn,
      ObjectIdentifier equipmentId,
      ObjectIdentifier equipmentStatusCode,
      String claimMemo) {
    // 获取特定设备PM状态
    String pmStatus = StandardProperties.SP_SEASON_PM_STATUS.getValue();
    // 获取设备type为PM的season plan
    List<Infos.Season> seasonList = getMachineSeasonForPM(equipmentId);
    // 获取目标状态
    CimMachineState machineState =
        baseCoreFactory.getBO(CimMachineState.class, equipmentStatusCode);
    if (machineState == null) {
      return;
    }
    String targetStatus =
        machineState.getE10State().getIdentifier() + BizConstant.DOT + machineState.getIdentifier();
    // 修改状态和特定状态匹配,标记PM flag为true
    if (CimArrayUtils.isEmpty(seasonList)) {
      return;
    }
    for (Infos.Season season : seasonList) {
      if (CimStringUtils.equals(pmStatus, targetStatus)) {
        season.setPmFlag(true);
        updateSeason(season);
      }
    }
  }

  /**
   * description:
   *
   * <p>change history: date defect person comments
   * ---------------------------------------------------------------------------------------------------------------------
   *
   * @param objCommon
   * @param equipmentId
   * @param eqpStatusCode
   * @param claimMemo
   * @return void
   * @exception
   * @author ho
   * @date 2020/7/27 14:02
   */
  @Override
  public void createSeasonJobForPM(
      Infos.ObjCommon objCommon,
      ObjectIdentifier equipmentId,
      ObjectIdentifier eqpStatusCode,
      String claimMemo) {
    createSeasonJobForPM(objCommon, equipmentId, eqpStatusCode, null, claimMemo);
  }

  @Override
  public void createSeasonJobForPM(
      Infos.ObjCommon objCommon,
      ObjectIdentifier equipmentId,
      ObjectIdentifier eqpStatusCode,
      Infos.EqpStatusInfo eqpStatusInfo,
      String claimMemo) {
    // 获取特定设备PM状态
    String pmStatus = StandardProperties.SP_SEASON_PM_STATUS.getValue();

    if (CimStringUtils.isEmpty(pmStatus)) {
      return;
    }

    // 获取设备type为PM的season plan
    List<Infos.Season> seasonList = getMachineSeasonForPM(equipmentId);

    if (CimArrayUtils.isEmpty(seasonList)) {
      return;
    }

    if (eqpStatusInfo == null) {
      eqpStatusInfo = equipmentMethod.equipmentStatusInfoGet(objCommon, equipmentId);
    }
    String e10Status = eqpStatusInfo.getE10Status();
    String value = ObjectIdentifier.fetchValue(eqpStatusInfo.getEquipmentStatusCode());
    if (!CimStringUtils.equals(pmStatus, e10Status + BizConstant.DOT + value)) {
      return;
    }

    // 获取目标状态
    CimMachineState machineState = baseCoreFactory.getBO(CimMachineState.class, eqpStatusCode);
    if (machineState == null) {
      return;
    }
    CimE10State e10State = machineState.getE10State();
    if (e10State == null) {
      return;
    }
    String targetStatus = e10State.getIdentifier();
    for (Infos.Season season : seasonList) {
      if (CimStringUtils.equals(BizConstant.SP_E10STATE_STANDBY, targetStatus)) {
        season.setPmFlag(true);
        updateSeason(season);
        createSeasonJob(objCommon, season, claimMemo);
      }
    }
  }

  /**
   * description:
   *
   * <p>change history: date defect person comments
   * ---------------------------------------------------------------------------------------------------------------------
   *
   * @param objCommon
   * @param startCassetteList
   * @return void
   * @exception
   * @author ho
   * @date 2020/7/29 15:32
   */
  public void updateMachineRecipeUsedTime(
      Infos.ObjCommon objCommon,
      List<Infos.StartCassette> startCassetteList,
      ObjectIdentifier equipmentID) {
    if (CimArrayUtils.isEmpty(startCassetteList)) {
      return;
    }
    CimMachine cimMachine = baseCoreFactory.getBO(CimMachine.class, equipmentID);
    for (Infos.StartCassette startCassette : startCassetteList) {
      Optional.ofNullable(startCassette.getLotInCassetteList())
          .ifPresent(
              lotInCassettes -> {
                for (Infos.LotInCassette lotInCassette : lotInCassettes) {
                  if (CimBooleanUtils.isFalse(lotInCassette.getMoveInFlag())) {
                    continue;
                  }
                  ObjectIdentifier machineRecipeID =
                      lotInCassette.getStartRecipe().getMachineRecipeID();
                  CimMachineRecipe cimMachineRecipe =
                      baseCoreFactory.getBO(CimMachineRecipe.class, machineRecipeID);
                  cimMachineRecipe.setLastUsedTime(
                      cimMachine, objCommon.getTimeStamp().getReportTimeStamp());
                }
              });
    }
  }

  /**
   * description:
   *
   * <p>change history: date defect person comments
   * ---------------------------------------------------------------------------------------------------------------------
   *
   * @param season
   * @param whatNextAttributes
   * @param lastRecipeID
   * @return boolean
   * @throws
   * @author ho
   * @date 2020/6/11 11:21
   */
  @Override
  public boolean productWhatAttributes(
      Infos.Season season,
      Infos.WhatNextAttributes whatNextAttributes,
      ObjectIdentifier lastRecipeID) {
    if (CimArrayUtils.isEmpty(
        this.checkSeasonForRecipe(
            null,
            ObjectIdentifier.fetchValue(whatNextAttributes.getMachineRecipeID()),
            CimArrayUtils.generateList(season)))) {
      return false;
    }
    // BUG-2153 productLot productID not should be season`s productID
    //        if (!ObjectIdentifier.isEmptyWithValue(season.getProductID())
    //                && !ObjectIdentifier.equalsWithValue(season.getProductID(),
    // whatNextAttributes.getProductID())) {
    //            return false;
    //        }
    if (!CimStringUtils.equals(
        whatNextAttributes.getLotType(), BizConstant.SP_LOT_TYPE_PRODUCTIONLOT)) {
      return false;
    }
    switch (season.getSeasonType()) {
      case BizConstant.SEASON_TYPE_IDLE:
      case BizConstant.SEASON_TYPE_RECIPEIDLE:
      case BizConstant.SEASON_TYPE_INTERVAL:
      case BizConstant.SEASON_TYPE_PM:
        return true;
      case BizConstant.SEASON_TYPE_RECIPEGROUP:
        ObjectIdentifier fromRecipeGroup = season.getParam().getFromRecipeGroup();
        RecipeGroup.Info recipeGroup = this.getRecipeGroup(fromRecipeGroup);
        boolean match =
            recipeGroup.getRecipeInfos().stream()
                .anyMatch(
                    objectIdentifier ->
                            ObjectIdentifier.equalsWithValue(objectIdentifier, lastRecipeID));
        if (match) {
          recipeGroup = this.getRecipeGroup(season.getParam().getToRecipeGroup());
          return recipeGroup.getRecipeInfos().stream()
              .anyMatch(
                  objectIdentifier ->
                          ObjectIdentifier.equalsWithValue(
                                  objectIdentifier, whatNextAttributes.getMachineRecipeID()));
        }
    }
    return false;
  }
}
