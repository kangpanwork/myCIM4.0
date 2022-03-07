package com.fa.cim.method.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fa.cim.annotaion.OmMethod;
import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.OmCode;
import com.fa.cim.common.support.Response;
import com.fa.cim.common.support.User;
import com.fa.cim.common.utils.CimNumberUtils;
import com.fa.cim.common.utils.Validations;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.crcp.ChamberLevelRecipeQueryParam;
import com.fa.cim.crcp.ChamberLevelRecipeReserveParam;
import com.fa.cim.crcp.ChamberLevelRecipeWhatNextParam;
import com.fa.cim.crcp.DisabledChamberQueryParam;
import com.fa.cim.dto.Infos;
import com.fa.cim.feign.IEsecFeign;
import com.fa.cim.method.IChamberLevelRecipeMethod;
import com.fa.cim.method.IConstraintMethod;
import com.fa.cim.method.IEquipmentMethod;
import com.fa.cim.newcore.bo.machine.CimMachine;
import com.fa.cim.newcore.bo.pd.CimProcessDefinition;
import com.fa.cim.newcore.bo.pd.CimProcessOperation;
import com.fa.cim.newcore.bo.product.CimLot;
import com.fa.cim.newcore.bo.recipe.CimLogicalRecipe;
import com.fa.cim.newcore.dto.machine.MachineDTO;
import com.fa.cim.newcore.factory.BaseCoreFactory;
import com.fa.cim.newcore.impl.bo.env.StandardProperties;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2021/9/15          ********              YJ               create file
 *
 * @author: YJ
 * @date: 2021/9/15 10:43
 * @copyright: 2021, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@OmMethod
@Slf4j
public class ChamberLevelRecipeMethod implements IChamberLevelRecipeMethod {

    /**
     * error message
     */
    @Autowired
    private RetCodeConfig retCodeConfig;

    /**
     * core
     */
    @Autowired
    private BaseCoreFactory coreFactory;

    /**
     * equipment method
     */
    @Autowired
    private IEquipmentMethod equipmentMethod;

    /**
     * constraint method
     */
    @Autowired
    private IConstraintMethod constraintMethod;

    /**
     * esec service
     */
    @Autowired
    private IEsecFeign esecFeign;

    @Override
    public List<Infos.StartCassette> chamberLevelRecipeMoveQueryRpt(Infos.ObjCommon objCommon,
                                                                    ChamberLevelRecipeReserveParam chamberLevelRecipeReserveParam) {
        // step1. check equipment ID and machine recipe ID
        Validations.check(Objects.isNull(chamberLevelRecipeReserveParam) ||
                        ObjectIdentifier.isEmpty(chamberLevelRecipeReserveParam.getEquipmentId()) ||
                        CollectionUtil.isEmpty(chamberLevelRecipeReserveParam.getStartCassettes()),
                retCodeConfig.getInvalidInputParam());

        ObjectIdentifier equipmentId = chamberLevelRecipeReserveParam.getEquipmentId();
        CimMachine machine = coreFactory.getBO(CimMachine.class, equipmentId);
        Validations.check(Objects.isNull(machine), retCodeConfig.getNotFoundEqp());

        // step2. 获取设备的所有chamber
        Infos.EqpChamberInfo eqpChamberInfo = equipmentMethod.equipmentChamberInfoGetDR(objCommon, equipmentId);
        if (CollectionUtil.isEmpty(eqpChamberInfo.getEqpChamberStatuses())) {
            if (log.isInfoEnabled()) {
                log.info("chamberLevelRecipeMoveQueryRpt() -> info : 设备{} 不存在chamber.", equipmentId);
            }
            return chamberLevelRecipeReserveParam.getStartCassettes();
        }
        Set<String> disabledChamberList = eqpChamberInfo.getEqpChamberStatuses().parallelStream()
                .filter(chamber -> !chamber.isChamberAvailableFlag())
                .map(chamber -> ObjectIdentifier.fetchValue(chamber.getChamberID()))
                .collect(Collectors.toSet());
        if (log.isInfoEnabled()) {
            log.info("chamberLevelRecipeMoveQueryRpt() -> info : 设备={}, chamber state 获取不可用chamber={}",
                    equipmentId, disabledChamberList);
        }

        if (NumberUtil.equals(disabledChamberList.size(), eqpChamberInfo.getEqpChamberStatuses().size())) {
            if (log.isInfoEnabled()) {
                log.info("chamberLevelRecipeMoveQueryRpt() -> info : 设备={},chamber state 所有chamber 被禁用",
                        equipmentId);
            }
            return chamberLevelRecipeReserveParam.getStartCassettes();
        }

        // step2. 获取哪些lot不能使用ESEC Chamber level Recipe
        settingMultipleChamberStatus(chamberLevelRecipeReserveParam);

        // step3. tool constraint
        toolConstraintSettingDisabledChamber(objCommon, chamberLevelRecipeReserveParam, disabledChamberList);
        if (NumberUtil.equals(disabledChamberList.size(), eqpChamberInfo.getEqpChamberStatuses().size())) {
            if (log.isInfoEnabled()) {
                log.info("chamberLevelRecipeMoveQueryRpt() -> info : 设备={},tool constraint 所有chamber 被禁用",
                        equipmentId);
            }
            return chamberLevelRecipeReserveParam.getStartCassettes();
        }


        // step4. tool capability
        // step4.1 获取当前加工工步的capability
        toolCapabilitySettingDisabledChamber(chamberLevelRecipeReserveParam, machine, eqpChamberInfo, disabledChamberList);


        // 如果全部禁用就不需要调用ESEC
        if (NumberUtil.equals(disabledChamberList.size(), eqpChamberInfo.getEqpChamberStatuses().size())) {
            if (log.isInfoEnabled()) {
                log.info("chamberLevelRecipeMoveQueryRpt() -> info : 设备={},tool capability 所有chamber 被禁用",
                        equipmentId);
            }
            return chamberLevelRecipeReserveParam.getStartCassettes();
        }

        // 获取machine recipe ID
        doChamberLevelRecipeSetting(objCommon, disabledChamberList, equipmentId, chamberLevelRecipeReserveParam);
        return chamberLevelRecipeReserveParam.getStartCassettes();
    }

    /**
     * description:  设置multiple chamber的状态
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     * 2021/9/16 17:17                        YJ                Create
     *
     * @param chamberLevelRecipeReserveParam - chamberLevelRecipeReserveParam
     * @author YJ
     * @date 2021/9/16 17:17
     */
    private void settingMultipleChamberStatus(ChamberLevelRecipeReserveParam chamberLevelRecipeReserveParam) {
        Map<String, String> multipleChamberMap = Maps.newHashMap();
        // for start carrier
        chamberLevelRecipeReserveParam.getStartCassettes().forEach(
                // for lot
                startCassette -> startCassette.getLotInCassetteList().forEach(
                        lotInCassette -> {
                            // 获取lot加工的logic recipe
                            Infos.StartRecipe startRecipe = lotInCassette.getStartRecipe();
                            if (Objects.nonNull(startRecipe) &&
                                    ObjectIdentifier.isNotEmpty(startRecipe.getLogicalRecipeID())) {
                                CimLogicalRecipe logicalRecipe =
                                        coreFactory.getBO(CimLogicalRecipe.class, startRecipe.getLogicalRecipeID());
                                if (Objects.nonNull(logicalRecipe) && logicalRecipe.isMultipleChamberSupported()) {
                                    multipleChamberMap.put(
                                            ObjectIdentifier.fetchValue(startRecipe.getLogicalRecipeID()),
                                            ObjectIdentifier.fetchValue(startRecipe.getLogicalRecipeID())
                                    );
                                }
                            }
                        }
                )
        );
        chamberLevelRecipeReserveParam.setMultipleChamberStatus(multipleChamberMap);

    }

    @Override
    public List<Infos.WhatNextAttributes> whatNextAttributesListChamberCheckRpt(
            Infos.ObjCommon objCommon, ChamberLevelRecipeWhatNextParam chamberLevelRecipeWhatNextParam) {
        // step1. check basic param
        Validations.check(Objects.isNull(chamberLevelRecipeWhatNextParam) ||
                        ObjectIdentifier.isEmpty(chamberLevelRecipeWhatNextParam.getEquipmentId()) ||
                        CollectionUtil.isEmpty(chamberLevelRecipeWhatNextParam.getWhatNextAttributesList()),
                retCodeConfig.getInvalidInputParam());


        // step2. tool chamber state
        Infos.EqpChamberInfo eqpChamberInfo = equipmentMethod.equipmentChamberInfoGetDR(objCommon,
                chamberLevelRecipeWhatNextParam.getEquipmentId());
        if (CollectionUtil.isEmpty(eqpChamberInfo.getEqpChamberStatuses())) {
            return chamberLevelRecipeWhatNextParam.getWhatNextAttributesList();
        }
        Set<String> disabledChamberList = eqpChamberInfo.getEqpChamberStatuses().parallelStream()
                .filter(chamber -> !chamber.isChamberAvailableFlag())
                .map(chamber -> ObjectIdentifier.fetchValue(chamber.getChamberID()))
                .collect(Collectors.toSet());

        // 所有chamber 被禁用直接返回空
        if (NumberUtil.equals(disabledChamberList.size(), eqpChamberInfo.getEqpChamberStatuses().size())) {
            return Lists.newArrayList();
        }

        CimMachine machine = coreFactory.getBO(CimMachine.class, chamberLevelRecipeWhatNextParam.getEquipmentId());
        Validations.check(Objects.isNull(machine), retCodeConfig.getNotFoundEqp());

        // step3. tool constraint
        List<ObjectIdentifier> lotIds = chamberLevelRecipeWhatNextParam.getWhatNextAttributesList()
                .parallelStream()
                .map(Infos.WhatNextAttributes::getLotID)
                .collect(Collectors.toList());
        disabledChamberList.addAll(
                toolConstraintSettingDisabledChamber(objCommon, chamberLevelRecipeWhatNextParam.getEquipmentId()));

        // 所有chamber 被禁用直接返回空
        if (NumberUtil.equals(disabledChamberList.size(), eqpChamberInfo.getEqpChamberStatuses().size())) {
            return Lists.newArrayList();
        }

        // step4. tool capability
        toolCapabilitySettingDisabledChamber(machine, eqpChamberInfo, disabledChamberList, lotIds,
                chamberLevelRecipeWhatNextParam.getEquipmentId());
        // 所有chamber 被禁用直接返回空
        if (NumberUtil.equals(disabledChamberList.size(), eqpChamberInfo.getEqpChamberStatuses().size())) {
            return Lists.newArrayList();
        }

        // step5. 通过equipment ID + machine Recipe ID 获取被禁用的chamber
        JSONArray disabledChamberArray = doCallEsecDisabledChamberList(objCommon, chamberLevelRecipeWhatNextParam);

        // step6. 通过esec返回的禁用chamber开始过滤next lot
        return chamberLevelRecipeWhatNextParam.getWhatNextAttributesList()
                .stream()
                .filter(whatNextAttributes -> {
                    // 被ESEC禁用的chamber level recipe
                    if (CollectionUtil.isEmpty(disabledChamberArray)) {
                        return true;
                    }
                    // 被tool capability / constraint禁用的chamber
                    disabledChamberList.addAll(disabledChamberArray.toJavaList(String.class));
                    return !NumberUtil.equals(disabledChamberList.size(), eqpChamberInfo.getEqpChamberStatuses().size());
                })
                .collect(Collectors.toList());
    }

    /**
     * description:  调用ESEC 获取被禁用的Chamber list
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     * 2021/9/16 17:42                        YJ                Create
     *
     * @param objCommon                       - common param
     * @param chamberLevelRecipeWhatNextParam - what next param
     * @return result json object
     * @author YJ
     * @date 2021/9/16 17:42
     */
    private JSONArray doCallEsecDisabledChamberList(Infos.ObjCommon objCommon,
                                                    ChamberLevelRecipeWhatNextParam chamberLevelRecipeWhatNextParam) {
        DisabledChamberQueryParam disabledChamberQueryParam = new DisabledChamberQueryParam();
        disabledChamberQueryParam.setEquipmentId(chamberLevelRecipeWhatNextParam.getEquipmentId());
        User user = objCommon.getUser().duplicate();
        user.setFunctionID(DisabledChamberQueryParam.TX_ID);
        disabledChamberQueryParam.setUser(user);
        List<ObjectIdentifier> machineRecipeIds = chamberLevelRecipeWhatNextParam.getWhatNextAttributesList()
                .parallelStream()
                .map(Infos.WhatNextAttributes::getMachineRecipeID)
                .collect(Collectors.toList());
        disabledChamberQueryParam.setBaseRecipeIds(machineRecipeIds);
        Response response = esecFeign.disabledChamberListRpt(disabledChamberQueryParam);
        if (log.isInfoEnabled()) {
            log.info("esecFeign.disabledChamberListRpt() -> info : " +
                    "call esec 获取被禁用的chamber , response = {}", response);
        }
        Validations.check(!CimNumberUtils.eq(OmCode.SUCCESS_CODE, response.getCode()),
                response.getMessage());
        return JSONArray.parseArray(response.getBody().toString());
    }

    /**
     * description:  执行chamber level recipe的设置
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     * 2021/9/15 15:33                        YJ                Create
     *
     * @param common                         - common
     * @param disabledChamberList            - 禁用的chamber
     * @param equipmentId                    - 设备ID
     * @param chamberLevelRecipeReserveParam - chamber level recipe param
     * @author YJ
     * @date 2021/9/15 15:33
     */
    private void doChamberLevelRecipeSetting(Infos.ObjCommon common, Set<String> disabledChamberList,
                                             ObjectIdentifier equipmentId,
                                             ChamberLevelRecipeReserveParam chamberLevelRecipeReserveParam) {

        // step1. 获取所有的machine recipe
        List<ObjectIdentifier> machineRecipeIds =
                chamberLevelRecipeReserveParam.getStartCassettes().parallelStream()
                        .flatMap(startCassette -> startCassette.getLotInCassetteList().parallelStream())
                        .filter(lotInCassette -> Objects.nonNull(lotInCassette.getStartRecipe()))
                        .map(lotInCassette -> lotInCassette.getStartRecipe().getMachineRecipeID())
                        .filter(Objects::nonNull)
                        .distinct()
                        .collect(Collectors.toList());

        Map<String, ObjectIdentifier> machineToChamberMap = machineRecipeIds.parallelStream().collect(
                Collectors.toMap(
                        ObjectIdentifier::fetchValue,
                        machineRecipeId -> {
                            // 通过设备ID/base recipe ID / 禁用的chamber， 获取chamber level recipe
                            ChamberLevelRecipeQueryParam queryChamberRcp = new ChamberLevelRecipeQueryParam();
                            queryChamberRcp.setEquipmentId(equipmentId);
                            queryChamberRcp.setBaseRecipeId(machineRecipeId);
                            queryChamberRcp.setDisabledChamber(Lists.newArrayList(disabledChamberList));
                            User user = common.getUser().duplicate();
                            user.setFunctionID(ChamberLevelRecipeQueryParam.TX_ID);
                            queryChamberRcp.setUser(user);
                            // call ESEC
                            Response response = esecFeign.chamberLevelRecipeMoveQueryRpt(queryChamberRcp);
                            if (log.isInfoEnabled()) {
                                log.info("esecFeign.chamberLevelRecipeMoveQueryRpt() -> info : " +
                                        "call esec 获取chamber level recipe , response = {}", response);
                            }
                            Validations.check(!CimNumberUtils.eq(OmCode.SUCCESS_CODE, response.getCode()),
                                    response.getMessage());
                            return JSONObject.toJavaObject(JSONObject.parseObject(response.getBody().toString()),
                                    ObjectIdentifier.class);
                        }
                )
        );
        if (log.isInfoEnabled()) {
            log.info("esecFeign() -> info : 设备 = {} , 获取所有的chamber level recipe{} ", equipmentId, machineToChamberMap);
        }

        chamberLevelRecipeReserveParam.getStartCassettes().forEach(
                startCassette -> startCassette.getLotInCassetteList().forEach(
                        lotInCassette -> {
                            if (isMultipleChamberUseChamberLevelRecipe(chamberLevelRecipeReserveParam, lotInCassette)) {
                                if (Objects.nonNull(lotInCassette.getStartRecipe())) {
                                    ObjectIdentifier chamberLevelRecipe = machineToChamberMap.get(
                                            ObjectIdentifier.fetchValue(lotInCassette.getStartRecipe().getMachineRecipeID())
                                    );
                                    lotInCassette.getStartRecipe().setChamberLevelRecipeID(chamberLevelRecipe);
                                }
                            }
                        }
                )
        );

    }

    /**
     * description:  tool constraint setting disabled chamber list
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     * 2021/9/15 14:44                        YJ                Create
     *
     * @param objCommon                      - common
     * @param chamberLevelRecipeReserveParam - chamber level recipe query param
     * @param disabledChamberList            - 禁用的chamber list
     * @author YJ
     * @date 2021/9/15 14:44
     */
    private void toolConstraintSettingDisabledChamber(Infos.ObjCommon objCommon,
                                                      ChamberLevelRecipeReserveParam chamberLevelRecipeReserveParam,
                                                      Set<String> disabledChamberList) {

        // 判断是否可以multiple 和 esec 能否一起使用，如果不可以，也不需要去查询constraint了。
        boolean isMultipleChamberUseChamberLevelRecipe =
                chamberLevelRecipeReserveParam.getStartCassettes().parallelStream()
                        // 将carrier中的lot 通过flat map 取出
                        .flatMap(startCassette -> startCassette.getLotInCassetteList().parallelStream())
                        // 获取所有的lot 只要有一个不可以用，就不允许和Esec共同使用
                        .anyMatch(lotInCassette ->
                                !isMultipleChamberUseChamberLevelRecipe(chamberLevelRecipeReserveParam, lotInCassette));

        // 满足可以一起使用时才去查询constraint
        if (!isMultipleChamberUseChamberLevelRecipe) {
            disabledChamberList.addAll(
                    toolConstraintSettingDisabledChamber(objCommon, chamberLevelRecipeReserveParam.getEquipmentId()));
        }
    }


    /**
     * description:  判断是否可以Multiple chamber 和 ESEC 同时使用
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     * 2021/9/16 17:27                        YJ                Create
     *
     * @param chamberLevelRecipeReserveParam - check param
     * @param lotInCassette                  - lot
     * @return is common use
     * @author YJ
     * @date 2021/9/16 17:27
     */
    private boolean isMultipleChamberUseChamberLevelRecipe(ChamberLevelRecipeReserveParam chamberLevelRecipeReserveParam,
                                                           Infos.LotInCassette lotInCassette) {
        // 如果存在multiple chamber
        if (Objects.nonNull(lotInCassette.getStartRecipe()) &&
                StrUtil.isNotBlank(chamberLevelRecipeReserveParam.getMultipleChamberStatus().get(
                        ObjectIdentifier.fetchValue(lotInCassette.getStartRecipe().getLogicalRecipeID())))) {
            // 通过环境变量判断是否需要继续执行
            return StandardProperties.OM_CRCP_MULTIPLE_COMMON_USE.isTrue();
        }

        return true;
    }

    /**
     * description:  tool constraint setting disabled chamber list
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     * 2021/9/15 14:44                        YJ                Create
     *
     * @param objCommon   - common
     * @param equipmentId - 设备ID
     * @author YJ
     * @date 2021/9/15 14:44
     */
    private Set<String> toolConstraintSettingDisabledChamber(Infos.ObjCommon objCommon, ObjectIdentifier equipmentId) {
        // 获取设备的Constraint
        List<Infos.ConstraintEqpDetailInfo> constraintEqpDetailInfos = constraintMethod.constraintListByEqp(
                objCommon, equipmentId,
                String.join(
                        BizConstant.SEPARATOR_COMMA, BizConstant.FUNCTION_RULE_BLIST, BizConstant.FUNCTION_RULE_WLIST),
                true
        );

        Set<String> disableChamberSet = Sets.newHashSet();
        // 如果存在constraint, 那么就判断是否有禁用的chamber
        if (CollectionUtil.isNotEmpty(constraintEqpDetailInfos)) {
            // 处理黑白名单
            Set<String> whiteSet = Sets.newHashSet();
            constraintEqpDetailInfos.stream()
                    .filter(constraintEqpDetailInfo ->
                            Objects.nonNull(constraintEqpDetailInfo.getEntityInhibitDetailAttributes()))
                    .forEach(constraintEqpDetailInfo -> {
                        // 如果是黑名单，那么将chamber放到disable chamber中
                        if (StrUtil.equals(BizConstant.FUNCTION_RULE_BLIST,
                                constraintEqpDetailInfo.getEntityInhibitDetailAttributes().getFunctionRule())) {
                            disableChamberSet.addAll(extractedChamber(constraintEqpDetailInfo));
                        } else {
                            // 如果是白名单，先放入白名单的set中
                            whiteSet.addAll(extractedChamber(constraintEqpDetailInfo));
                        }
                    });

            // 白名单不为空
            if (CollectionUtil.isNotEmpty(whiteSet)) {
                // 获取当前设备的所有Chamber
                Infos.EqpChamberInfo eqpChamberInfo = equipmentMethod.equipmentChamberInfoGetDR(objCommon, equipmentId);
                Set<String> chamberList =
                        eqpChamberInfo.getEqpChamberStatuses()
                                .parallelStream()
                                .map(chamber -> ObjectIdentifier.fetchValue(chamber.getChamberID()))
                                .collect(Collectors.toSet());
                chamberList.forEach(chamber -> {
                    if (!whiteSet.contains(chamber)) {
                        // 如果当前chamber不在白名单中，就被禁用
                        disableChamberSet.add(chamber);
                    }
                });
            }

            return disableChamberSet;

        }
        return new HashSet<>();
    }

    /**
     * description:  entity list 中的chamber
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     * 2021/11/17 19:21                        YJ                Create
     *
     * @param constraintEqpDetailInfo - eqp info
     * @return chamber list
     * @author YJ
     * @date 2021/11/17 19:21
     */
    private Set<String> extractedChamber(Infos.ConstraintEqpDetailInfo constraintEqpDetailInfo) {
        List<Infos.EntityIdentifier> entityIdentifierList =
                constraintEqpDetailInfo.getEntityInhibitDetailAttributes().getEntities();
        if (CollectionUtil.isNotEmpty(entityIdentifierList)) {
            return entityIdentifierList.parallelStream()
                    .filter(
                            entities -> StrUtil.equals(entities.getClassName(), BizConstant.SP_INHIBITCLASSID_CHAMBER))
                    .map(Infos.EntityIdentifier::getAttribution)
                    .collect(Collectors.toSet());
        } else {
            return Sets.newHashSet();
        }
    }

    /**
     * description: 获取被tool capability禁用的chamber
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     * 2021/9/15 11:35                        YJ                Create
     *
     * @param machine             - equipment
     * @param eqpChamberInfo      - chamber list all
     * @param disabledChamberList - 禁用的chamber
     * @param lotIds              - lot ID
     * @param equipmentId         - 设备ID
     * @author YJ
     * @date 2021/9/15 11:35
     */
    private void toolCapabilitySettingDisabledChamber(CimMachine machine, Infos.EqpChamberInfo eqpChamberInfo,
                                                      Set<String> disabledChamberList, List<ObjectIdentifier> lotIds,
                                                      ObjectIdentifier equipmentId) {
        // step1. 获取当前工步需要的 capability
        List<String> useCapabilityList = lotIds.parallelStream()
                .map(lotId -> {
                    CimLot lot = coreFactory.getBO(CimLot.class, lotId);
                    Validations.check(Objects.isNull(lot), retCodeConfig.getNotFoundLot());
                    CimProcessOperation processOperation = lot.getProcessOperation();
                    if (Objects.nonNull(processOperation)) {
                        CimProcessDefinition processDefinition = processOperation.getProcessDefinition();
                        if (Objects.nonNull(processDefinition)) {
                            String capabilityReq = processDefinition.getCapabilityReq();
                            if (StrUtil.isNotBlank(capabilityReq)) {
                                return capabilityReq;
                            }
                        }
                    }
                    return BizConstant.EMPTY;
                })
                .filter(StrUtil::isNotBlank)
                .distinct()
                .collect(Collectors.toList());

        // step2. 当前加工没有设置capability
        if (CollectionUtil.isEmpty(useCapabilityList)) {
            if (log.isInfoEnabled()) {
                log.info("toolCapabilitySettingDisabledChamber() -> info : tool capability 没有禁用的chamber");
            }
            return;
        }

        // step3. 获取当前设备设置chamber capability
        MachineDTO.EqpCapabilityInfo eqpCapabilities = machine.getEqpCapabilities();
        if (CollectionUtil.isNotEmpty(eqpCapabilities.getEqpCapabilityDetailList())) {
            if (log.isInfoEnabled()) {
                log.info("chamberLevelRecipeMoveQueryRpt() -> info : 设备={},capability 验证={}",
                        equipmentId, eqpCapabilities.getEqpCapabilityDetailList());
            }

            // step3.1 获取哪些capability属性对应失败，被禁用的chamber
            Map<String, List<String>> chamberCapabilityMap = eqpCapabilities.getEqpCapabilityDetailList()
                    .parallelStream()
                    .collect(Collectors.groupingBy(
                            chamber -> ObjectIdentifier.fetchValue(chamber.getChamberID()),
                            Collectors.mapping(MachineDTO.EqpCapabilityDetail::getEqpCapability, Collectors.toList())
                    ));

            Map<String, String> operationCapabilityMap =
                    useCapabilityList.parallelStream()
                            .collect(Collectors.toMap(Function.identity(), Function.identity()));

            eqpChamberInfo.getEqpChamberStatuses().forEach(chamber -> {
                String chamberId = ObjectIdentifier.fetchValue(chamber.getChamberID());
                List<String> chamberCapability = chamberCapabilityMap.get(chamberId);
                // 如果这个chamber 没有 capability 或者 当前chamber的capability都不满足当前加工所需的capability 那么就禁用chamber
                if (CollectionUtil.isEmpty(chamberCapability) ||
                        chamberCapability.stream()
                                .noneMatch(capability -> StrUtil.isNotBlank(operationCapabilityMap.get(capability)))) {
                    disabledChamberList.add(chamberId);
                }
            });
        } else {
            if (log.isInfoEnabled()) {
                log.info("toolCapabilitySettingDisabledChamber() -> info : tool capability 没有可使用的chamber");
            }
            // step3.2 如果当前工步有capability的设置，但是chamber 没有capability，那么所有的chamber 都被禁用
            disabledChamberList.addAll(
                    eqpChamberInfo.getEqpChamberStatuses()
                            .parallelStream()
                            .map(chamber -> ObjectIdentifier.fetchValue(chamber.getChamberID()))
                            .collect(Collectors.toSet())
            );
        }
    }

    /**
     * description: 获取被tool capability禁用的chamber
     * change history:
     * date             defect             person             comments
     * -----------------------------------------------------------------------------------------------------------------
     * 2021/9/15 11:35                        YJ                Create
     *
     * @param chamberLevelRecipeReserveParam -  tool param
     * @param machine                        - equipment
     * @param eqpChamberInfo                 - chamber list all
     * @param disabledChamberList            - 禁用的chamber
     * @author YJ
     * @date 2021/9/15 11:35
     */
    private void toolCapabilitySettingDisabledChamber(ChamberLevelRecipeReserveParam chamberLevelRecipeReserveParam,
                                                      CimMachine machine, Infos.EqpChamberInfo eqpChamberInfo,
                                                      Set<String> disabledChamberList) {
        List<ObjectIdentifier> lotIds = chamberLevelRecipeReserveParam.getStartCassettes().parallelStream()
                .flatMap(startCassette -> startCassette.getLotInCassetteList().parallelStream())
                .map(lotInCassette -> {
                    if (isMultipleChamberUseChamberLevelRecipe(chamberLevelRecipeReserveParam, lotInCassette)) {
                        return lotInCassette.getLotID();
                    } else {
                        return ObjectIdentifier.emptyIdentifier();
                    }
                })
                .filter(ObjectIdentifier::isNotEmptyWithValue)
                .collect(Collectors.toList());
        toolCapabilitySettingDisabledChamber(machine, eqpChamberInfo, disabledChamberList, lotIds,
                chamberLevelRecipeReserveParam.getEquipmentId());
    }
}
