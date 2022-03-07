package com.fa.cim.method.impl;

import com.fa.cim.annotaion.OmMethod;
import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.utils.CimDateUtils;
import com.fa.cim.common.utils.CimStringUtils;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;
import com.fa.cim.entity.nonruntime.eqparea.CimEqpAreaDO;
import com.fa.cim.entity.runtime.areagroup.CimAreaGroupAreaDO;
import com.fa.cim.entity.runtime.eqp.CimEquipmentDO;
import com.fa.cim.entity.runtime.port.CimPortDO;
import com.fa.cim.jpa.CimJpaRepository;
import com.fa.cim.method.IEquipmentAreaMethod;
import com.fa.cim.newcore.bo.machine.CimMachine;
import com.fa.cim.newcore.dto.machine.MachineDTO;
import com.fa.cim.newcore.factory.BaseCoreFactory;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Example;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * description: eqp area method
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2021/2/19 0019        ********             YJ               create file
 *
 * @author: YJ
 * @date: 2021/2/19 0019 19:43
 * @copyright: 2021, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@OmMethod
public class EquipmentAreaMethod implements IEquipmentAreaMethod {

    @Autowired
    private CimJpaRepository cimJpaRepository;

    @Autowired
    private BaseCoreFactory baseCoreFactory;

    @Override
    public List<Results.EqpSearchForSettingEqpBoardResult> eqpSearchForSettingEqpBoard(Infos.ObjCommon objCommon, Params.EqpSearchForSettingEqpBoardParams eqpSearchForSettingEqpBoardParams) {
        log.info("eqpSearchForSettingEqpBoard()->info: params {}", eqpSearchForSettingEqpBoardParams.toString());
        // 【step 1】 query sub sql
        StringBuilder querySql = new StringBuilder("SELECT F.* FROM OMEQP F WHERE 1 = 1");

        List<Object> searchParams = Lists.newArrayList();

        String eqpId = eqpSearchForSettingEqpBoardParams.getEqpId();
        if (CimStringUtils.isNotEmpty(eqpId)) {
            querySql.append(" AND EQP_ID LIKE '%'||?").append(searchParams.size() + 1).append("||'%'");
            searchParams.add(eqpId);
        }

        String bayId = eqpSearchForSettingEqpBoardParams.getBayId();
        if (CimStringUtils.isNotEmpty(bayId)) {
            querySql.append(" AND BAY_ID LIKE '%'||?").append(searchParams.size() + 1).append("||'%'");
            searchParams.add(bayId);
        }

        String eqpStatus = eqpSearchForSettingEqpBoardParams.getEqpStatus();
        if (CimStringUtils.isNotEmpty(eqpStatus)) {
            querySql.append(" AND E10_STATE_ID LIKE '%'||?").append(searchParams.size() + 1).append("||'%'");
            searchParams.add(eqpStatus);
        }

        String subStatus = eqpSearchForSettingEqpBoardParams.getSubStatus();
        if (CimStringUtils.isNotEmpty(subStatus)) {
            querySql.append(" AND EQP_STATE_ID LIKE '%'||?").append(searchParams.size() + 1).append("||'%'");
            searchParams.add(subStatus);
        }

        // STEP2 find areaId by bay group.
        log.info("eqpSearchForSettingEqpBoard()->info: Query area id by BayGroup! ");
        String bayGroup = eqpSearchForSettingEqpBoardParams.getBayGroup();
        if (CimStringUtils.isNotEmpty(bayGroup)) {
            querySql.append(" AND BAY_ID IN(?").append(searchParams.size() + 1).append(")");

            String areaSql = "SELECT DISTINCT A.BAY_ID FROM OMBAYGRP BG " +
                    "LEFT JOIN OMBAYGRP_BAY A ON BG.ID = A.REFKEY WHERE BG.BAY_GRP_ID LIKE '%'||?||'%'";

            List<CimAreaGroupAreaDO> areaGroupAreaDos = cimJpaRepository.query(areaSql, CimAreaGroupAreaDO.class, bayGroup);
            if (CollectionUtils.isEmpty(areaGroupAreaDos)) {
                log.info("eqpSearchForSettingEqpBoard()->info:find area by bay group , not find .");
                return Lists.newArrayList();
            }

            List<String> areaIds = areaGroupAreaDos.parallelStream().map(CimAreaGroupAreaDO::getAreaID).collect(Collectors.toList());
            searchParams.add(areaIds);
        }

        List<CimEquipmentDO> eqpList = cimJpaRepository.query(querySql.toString(), CimEquipmentDO.class, searchParams.toArray());

        // step3 if bayGroup the null
        log.info("eqpSearchForSettingEqpBoard()->info: find bayGroup by areaId!");
        Map<Object, String> bayGroupMap = Maps.newHashMap();
        if (CimStringUtils.isEmpty(bayGroup) && !CollectionUtils.isEmpty(eqpList)) {
            // find bay group
            String queryBayGroupSql = "SELECT DISTINCT bgr.BAY_ID, bg.BAY_GRP_ID FROM OMBAYGRP_BAY bgr LEFT JOIN OMBAYGRP bg ON bg.id = bgr.REFKEY "
                    + "WHERE bgr.BAY_ID IN (?1)";
            List<String> areaIds = eqpList.parallelStream().map(CimEquipmentDO::getAreaID).distinct().collect(Collectors.toList());
            List<Object[]> areaAndBayGroup = cimJpaRepository.query(queryBayGroupSql, areaIds);
            Map<Object, String> conversionAreaMap = areaAndBayGroup.parallelStream()
                    .collect(Collectors.groupingBy(area -> area[0], Collectors.mapping(f -> (String) f[1], Collectors.joining(BizConstant.SEPARATOR_COMMA))));
            bayGroupMap.putAll(conversionAreaMap);
        }

        // conversion eqp
        return eqpList.stream().map(equipmentDO -> {
            Results.EqpSearchForSettingEqpBoardResult eqpSearchForSettingEqpBoardResult = new Results.EqpSearchForSettingEqpBoardResult();
            eqpSearchForSettingEqpBoardResult.setBayId(equipmentDO.getAreaID());
            eqpSearchForSettingEqpBoardResult.setEqpStatus(equipmentDO.getCurE10State());
            eqpSearchForSettingEqpBoardResult.setEqpId(ObjectIdentifier.build(equipmentDO.getEquipmentID(), equipmentDO.getId()));
            eqpSearchForSettingEqpBoardResult.setEqpStatus(equipmentDO.getCurE10State());
            eqpSearchForSettingEqpBoardResult.setSubStatus(equipmentDO.getCurrentStateID());
            if (CimStringUtils.isEmpty(bayGroup)) {
                eqpSearchForSettingEqpBoardResult.setBayGroup(bayGroupMap.get(equipmentDO.getAreaID()));
            } else {
                eqpSearchForSettingEqpBoardResult.setBayGroup(bayGroup);
            }
            return eqpSearchForSettingEqpBoardResult;
        }).collect(Collectors.toList());
    }

    @Override
    public List<Results.EqpAreaBoardListResult> eqpAreaBoardList(Infos.ObjCommon objCommon, Params.EqpAreaBoardListParams eqpAreaBoardListParams) {
        log.info("eqpAreaBoardList()->info: find by work zone or user");
        // step1 find by work zone or user
        String workZone = eqpAreaBoardListParams.getWorkZone();
        String category;
        String queryZoneParams;
        if (CimStringUtils.isNotEmpty(workZone)) {
            category = BizConstant.EQP_WORK_ZONE_CATEGORY;
            queryZoneParams = workZone;
        } else {
            category = BizConstant.EQP_WORK_USER_CATEGORY;
            queryZoneParams = ObjectIdentifier.fetchValue(objCommon.getUser().getUserID());
        }

        // step 2 query sql
        log.info("eqpAreaBoardList()->info: query eqp params {},{}", category, queryZoneParams);
        String eapAreaSql = "SELECT * FROM OSEQPAREA EA WHERE EA.CATEGORY = ?1 AND EA.ZONE = ?2 ORDER BY EA.SEQNO ASC";
        List<CimEqpAreaDO> eqpAreaDOList = cimJpaRepository.query(eapAreaSql, CimEqpAreaDO.class, category, queryZoneParams);
        List<String> eqpIds = eqpAreaDOList.parallelStream().map(CimEqpAreaDO::getEqpId).collect(Collectors.toList());

        Map<String, CimEquipmentDO> eqpMap = Maps.newHashMap();
        if (!CollectionUtils.isEmpty(eqpIds)) {
            String queryEqpSql = "SELECT DISTINCT OMEQP.* FROM OMEQP WHERE EQP_ID IN (?1)";
            List<CimEquipmentDO> eqpList = cimJpaRepository.query(queryEqpSql, CimEquipmentDO.class, eqpIds);
            eqpMap.putAll(eqpList.parallelStream().collect(Collectors.toMap(CimEquipmentDO::getEquipmentID, eqp -> eqp)));
        }

        // step 3 query eqp mode
        log.info("eqpAreaBoardList()->info: query eqp mode");
        Map<String, List<CimPortDO>> portMap = Maps.newHashMap();
        if (!CollectionUtils.isEmpty(eqpIds)) {
            String portSql = "SELECT OMPORT.* FROM OMPORT WHERE EQP_ID IN (?1)";
            List<CimPortDO> portList = cimJpaRepository.query(portSql, CimPortDO.class, eqpIds);
            portMap = portList.parallelStream().collect(Collectors.groupingBy(CimPortDO::getEquipmentObj));
        }

        // step4 conversion eqp area and eqp ....
        log.info("eqpAreaBoardList()->info: conversion eqp area and eqp ....");
        Map<String, List<CimPortDO>> finalPortMap = portMap;
        return eqpAreaDOList.stream()
                .filter(eqpArea -> Objects.nonNull(eqpMap.get(eqpArea.getEqpId())))
                .map(eqpArea -> {
                    CimEquipmentDO equipmentDO = eqpMap.get(eqpArea.getEqpId());
                    Results.EqpAreaBoardListResult eqpAreaBoardListResult = new Results.EqpAreaBoardListResult();
                    eqpAreaBoardListResult.setEqpId(ObjectIdentifier.build(equipmentDO.getEquipmentID(), equipmentDO.getId()));
                    eqpAreaBoardListResult.setEqpStatus(equipmentDO.getCurE10State());
                    eqpAreaBoardListResult.setSubStatus(equipmentDO.getCurrentStateID());
                    eqpAreaBoardListResult.setBayId(equipmentDO.getAreaID());
                    eqpAreaBoardListResult.setEqpCategory(equipmentDO.getEquipmentCategory());
                    eqpAreaBoardListResult.setChangeUserId(equipmentDO.getStateChangeUserID());
                    eqpAreaBoardListResult.setChangeTime(equipmentDO.getStateChangeTime());
                    eqpAreaBoardListResult.setCapableRcpMode(equipmentDO.getMultiRecipeCapability());

                    // set eqp mode
                    List<CimPortDO> cimPortDOS = finalPortMap.get(equipmentDO.getId());
                    if (!CollectionUtils.isEmpty(cimPortDOS)) {
                        String eqpMode = cimPortDOS.parallelStream().map(CimPortDO::getMachineOperationModeID).collect(Collectors.joining(BizConstant.SEPARATOR_COMMA));
                        eqpAreaBoardListResult.setEqpMode(eqpMode);
                    }

                    // find in process lot
                    CimMachine cimMachine = baseCoreFactory.getBO(CimMachine.class, eqpArea.getEqpObject());
                    List<MachineDTO.MachineLot> machineLots = cimMachine.allProcessingLots();

                    Set<String> lotIds = Sets.newHashSet();
                    Set<String> carrierIds = Sets.newHashSet();
                    machineLots.forEach(machineLot -> {
                        lotIds.add(ObjectIdentifier.fetchValue(machineLot.getLotID()));
                        carrierIds.add(ObjectIdentifier.fetchValue(machineLot.getUnloadCassetteID()));
                    });

                    eqpAreaBoardListResult.setLotId(lotIds.parallelStream().collect(Collectors.joining(BizConstant.SEPARATOR_COMMA)));
                    eqpAreaBoardListResult.setCarrierId(carrierIds.parallelStream().collect(Collectors.joining(BizConstant.SEPARATOR_COMMA)));

                    return eqpAreaBoardListResult;
                }).collect(Collectors.toList());
    }

    @Override
    public void eqpBoardWorkZoneBinding(Infos.ObjCommon objCommon, Params.EqpBoardWorkZoneBindingParams eqpBoardWorkZoneBindingParams) {
        List<ObjectIdentifier> eqpIds = eqpBoardWorkZoneBindingParams.getEqpIds();
        eqpIds.forEach(eqpId -> {
            String lockKey = null;
            try {
                // step1 lock eqp board
                log.info("eqpBoardWorkZoneBinding()->info: eqp area for update");
                List<CimEqpAreaDO> area = eqpAreaLockListByCategoryAndZone(objCommon, eqpBoardWorkZoneBindingParams.getCategory(), eqpBoardWorkZoneBindingParams.getZone());
                Optional<CimEqpAreaDO> max = area.parallelStream().max(Comparator.comparingInt(eqpArea -> Integer.parseInt(eqpArea.getLockKey().split("\\.")[2])));
                int sequence = area.size();
                if (max.isPresent()) {
                    sequence = Integer.parseInt(max.get().getLockKey().split("\\.")[2]) + 1;
                }

                lockKey = String.join(BizConstant.DOT, eqpBoardWorkZoneBindingParams.getCategory(), eqpBoardWorkZoneBindingParams.getZone(), String.valueOf(sequence));
                boolean isExistZone = area.parallelStream()
                        .anyMatch(cimEqpAreaDO -> CimStringUtils.equals(cimEqpAreaDO.getEqpId(), ObjectIdentifier.fetchValue(eqpId)));
                if (isExistZone) {
                    log.info("eqpBoardWorkZoneBinding()->info: eqp exist!!!");
                    return;
                }
                //step2 generate eqp area db
                log.info("eqpBoardWorkZoneBinding()->info: insert eqp area ...");
                CimEqpAreaDO cimEqpAreaDO = new CimEqpAreaDO();
                BeanUtils.copyProperties(eqpBoardWorkZoneBindingParams, cimEqpAreaDO);

                cimEqpAreaDO.setEqpId(ObjectIdentifier.fetchValue(eqpId));
                cimEqpAreaDO.setEqpObject(ObjectIdentifier.fetchReferenceKey(eqpId));
                cimEqpAreaDO.setSequenceNumber(area.size());
                cimEqpAreaDO.setCreatedTime(CimDateUtils.getCurrentTimeStamp());
                cimEqpAreaDO.setLastModifyTime(CimDateUtils.getCurrentTimeStamp());
                cimEqpAreaDO.setCreatedUser(ObjectIdentifier.fetchValue(objCommon.getUser().getUserID()));
                cimEqpAreaDO.setLastModifyUser(ObjectIdentifier.fetchValue(objCommon.getUser().getUserID()));
                cimEqpAreaDO.setLockKey(lockKey);

                //step3 insert to db
                log.info("eqpBoardWorkZoneBinding()->info: insert to db ...");
                cimJpaRepository.save(cimEqpAreaDO);

            } catch (DuplicateKeyException e) {
                log.warn("eqpBoardWorkZoneBinding()->warn: eqp area insert lock key duplicate {}", lockKey);
                eqpBoardWorkZoneBinding(objCommon, eqpBoardWorkZoneBindingParams);
            }
        });
    }

    @Override
    public List<String> eqpWorkZoneList(Infos.ObjCommon objCommon, Params.EqpWorkZoneListParams eqpWorkZoneListParams) {
        log.info("eqpWorkZoneList()->info: eqp work zone query list...");
        // step1 conversion sql
        String workZoneSql = "SELECT DISTINCT ZONE FROM OSEQPAREA WHERE CATEGORY = ?1";
        List<CimEqpAreaDO> eqpAreaList = cimJpaRepository.query(workZoneSql, CimEqpAreaDO.class, BizConstant.EQP_WORK_ZONE_CATEGORY);
        return eqpAreaList.parallelStream().map(CimEqpAreaDO::getZone).distinct().collect(Collectors.toList());
    }

    @Override
    public List<CimEqpAreaDO> eqpAreaLockListByCategoryAndZone(Infos.ObjCommon objCommon, String category, String zone) {
        // step1 lock eqp board
        log.info("eqpAreaLockListByCategoryAndZone()->info: eqp area for update");
        if (CimStringUtils.isEmpty(category) || CimStringUtils.isEmpty(zone)) {
            return Lists.newArrayList();
        }
        String lockQuerySql = "SELECT OSEQPAREA.* FROM OSEQPAREA WHERE CATEGORY = ?1 " +
                " AND ZONE = ?2 FOR UPDATE";
        return cimJpaRepository.query(lockQuerySql, CimEqpAreaDO.class, category, zone);
    }

    @Override
    public void eqpAreaCancel(Infos.ObjCommon objCommon, Params.EqpAreaCancelParams eqpAreaCancelParams) {
        log.info("eqpAreaCancel()->info: call eqpAreaLockListByCategoryAndZone lock... ");
        //step1 lock eqp board
        List<CimEqpAreaDO> eqpAreas = eqpAreaLockListByCategoryAndZone(objCommon, eqpAreaCancelParams.getCategory(), eqpAreaCancelParams.getZone());
        if (CollectionUtils.isEmpty(eqpAreas)) {
            log.info("eqpAreaCancel()->info : lock eqp area list is null! ");
            return;
        }

        //step2 conversion delete sql
        log.info("eqpAreaCancel()->info: conversion delete sql ");
        CimEqpAreaDO cimEqpAreaDO = new CimEqpAreaDO();
        cimEqpAreaDO.setCategory(eqpAreaCancelParams.getCategory());
        cimEqpAreaDO.setZone(eqpAreaCancelParams.getZone());
        Boolean clearAll = eqpAreaCancelParams.getClearAll();
        if (!clearAll) {
            cimEqpAreaDO.setEqpId(ObjectIdentifier.fetchValue(eqpAreaCancelParams.getEqpId()));
        }

        //step3 execute delete sql
        log.info("eqpAreaCancel()->info: execute delete sql ");
        cimJpaRepository.delete(Example.of(cimEqpAreaDO));

        //step4 reorder eqp area
        log.info("eqpAreaCancel()->info: reorder eqp area ");
        if (!clearAll) {
            Optional<CimEqpAreaDO> first = eqpAreas.parallelStream()
                    .filter(eqpAreaDO -> CimStringUtils.equals(eqpAreaDO.getEqpId(), ObjectIdentifier.fetchValue(eqpAreaCancelParams.getEqpId())))
                    .findFirst();
            first.ifPresent(removeEqpAreaDo -> eqpAreas.forEach(eqpAreaDO -> {
                if (eqpAreaDO.getSequenceNumber() > removeEqpAreaDo.getSequenceNumber()) {
                    eqpAreaDO.setLastModifyTime(CimDateUtils.getCurrentTimeStamp());
                    eqpAreaDO.setLastModifyUser(ObjectIdentifier.fetchValue(objCommon.getUser().getUserID()));
                    int sequenceNumber = eqpAreaDO.getSequenceNumber() - 1;
                    eqpAreaDO.setSequenceNumber(sequenceNumber);
                    cimJpaRepository.save(eqpAreaDO);
                }
            }));
        }
    }

    @Override
    public void eqpAreaMove(Infos.ObjCommon objCommon, Params.EqpAreaMoveParams eqpAreaMoveParams) {
        log.info("eqpAreaMove()->info: call eqpAreaLockListByCategoryAndZone lock... ");
        //step1 lock eqp board
        List<CimEqpAreaDO> eqpAreas = eqpAreaLockListByCategoryAndZone(objCommon, eqpAreaMoveParams.getCategory(), eqpAreaMoveParams.getZone());
        if (CollectionUtils.isEmpty(eqpAreas)) {
            log.info("eqpAreaMove()->info : lock eqp area list is null! ");
            return;
        }
        Map<String, CimEqpAreaDO> eqpAreaDOMap = eqpAreas.parallelStream().collect(Collectors.toMap(CimEqpAreaDO::getEqpId, eqpArea -> eqpArea));

        //step2 update eqp sequence
        log.info("eqpAreaMove()->info: update eqp sequence ");
        List<ObjectIdentifier> eqpIds = eqpAreaMoveParams.getEqpIds();
        for (int sequence = 0; sequence < eqpIds.size(); sequence++) {
            ObjectIdentifier eqpId = eqpIds.get(sequence);
            CimEqpAreaDO cimEqpAreaDO = eqpAreaDOMap.get(ObjectIdentifier.fetchValue(eqpId));
            if (Objects.isNull(cimEqpAreaDO)) {
                log.warn("eqpAreaMove()->warn: eqp area not find ! {}", ObjectIdentifier.fetchValue(eqpId));
                continue;
            }
            cimEqpAreaDO.setSequenceNumber(sequence);
            cimEqpAreaDO.setLastModifyTime(CimDateUtils.getCurrentTimeStamp());
            cimEqpAreaDO.setLastModifyUser(ObjectIdentifier.fetchValue(objCommon.getUser().getUserID()));
            cimJpaRepository.save(cimEqpAreaDO);
        }

    }
}
