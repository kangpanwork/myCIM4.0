package com.fa.cim.method.impl;

import cn.hutool.core.util.StrUtil;
import com.fa.cim.annotaion.OmMethod;
import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.OmCode;
import com.fa.cim.common.utils.*;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.config.RetCodeConfigEx;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Outputs;
import com.fa.cim.dto.Params;
import com.fa.cim.entity.nonruntime.CimBondGroupDO;
import com.fa.cim.entity.nonruntime.CimBondGroupMapDO;
import com.fa.cim.jpa.CimJpaRepository;
import com.fa.cim.method.IBondingGroupMethod;
import com.fa.cim.newcore.bo.durable.CimCassette;
import com.fa.cim.newcore.bo.machine.CimMachine;
import com.fa.cim.newcore.bo.machine.CimPortResource;
import com.fa.cim.newcore.factory.BaseCoreFactory;
import com.fa.cim.newcore.standard.prdctmng.Lot;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/7/13          ********            Nyx                create file
 *
 * @author Nyx
 * @since 2019/7/13 15:28
 * Copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@OmMethod
@Slf4j
public class BondingGroupMethod implements IBondingGroupMethod {

    @Autowired
    private RetCodeConfig retCodeConfig;

    @Autowired
    private RetCodeConfigEx retCodeConfigEx;

    @Autowired
    private CimJpaRepository cimJpaRepository;

    @Autowired
    private BaseCoreFactory baseCoreFactory;

    @Override
    public Outputs.ObjBondingGroupInfoByEqpGetDROut bondingGroupInfoByEqpGetDR(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID,
                                                                               ObjectIdentifier controlJobID, boolean bondingMapInfoFlag) {
        List<ObjectIdentifier> topLotIDSeq = new ArrayList<>();
        //------------------------------------------------------------------
        //   Get Bonding Group Information.
        //------------------------------------------------------------------
        CimBondGroupDO cimBondGroupExam = new CimBondGroupDO();
        cimBondGroupExam.setEquipmentID(ObjectIdentifier.fetchValue(equipmentID));
        cimBondGroupExam.setControlJobID(ObjectIdentifier.fetchValue(controlJobID));
        List<Infos.BondingGroupInfo> bondingGroupInfos = cimJpaRepository.findAll(Example.of(cimBondGroupExam)).stream().map(bondGroup -> {
            String bondGroupID = bondGroup.getBondGroupID();
            String bondGroupStatus = bondGroup.getBondGroupStatus();
            Infos.BondingGroupInfo bondingGroupInfo = new Infos.BondingGroupInfo();
            bondingGroupInfo.setBondingGroupID(bondGroupID);
            bondingGroupInfo.setBondingGroupState(bondGroupStatus);
            bondingGroupInfo.setTargetEquipmentID(equipmentID);
            bondingGroupInfo.setControlJobID(controlJobID);

            if (CimBooleanUtils.isTrue(bondingMapInfoFlag)) {
                log.info("bondingMapInfoFlag == true");
                //--------------------------------------------------------------
                //   Get Bonding Map Information.
                //--------------------------------------------------------------
                CimBondGroupMapDO cimBondGroupMapExam = new CimBondGroupMapDO();
                cimBondGroupMapExam.setBondGroupID(bondGroupID);
                List<Infos.BondingMapInfo> bondingMapInfoList = cimJpaRepository.findAll(Example.of(cimBondGroupMapExam)).stream().sorted()
                        .map(bondGroupMap -> {
                            Infos.BondingMapInfo bondingMapInfo = new Infos.BondingMapInfo();
                            bondingMapInfo.setBondingGroupID(bondGroupID);
                            bondingMapInfo.setBaseWaferID(ObjectIdentifier.buildWithValue(bondGroupMap.getBaseWaferID()));
                            bondingMapInfo.setBaseLotID(ObjectIdentifier.buildWithValue(bondGroupMap.getBaseLotID()));
                            bondingMapInfo.setBaseProductID(ObjectIdentifier.buildWithValue(bondGroupMap.getBaseProductSpecID()));
                            bondingMapInfo.setBaseBondingSide(bondGroupMap.getBaseBondSide());
                            bondingMapInfo.setPlanTopWaferID(ObjectIdentifier.buildWithValue(bondGroupMap.getPlanTopWaferID()));
                            bondingMapInfo.setPlanTopLotID(ObjectIdentifier.buildWithValue(bondGroupMap.getPlanTopLotID()));
                            bondingMapInfo.setPlanTopProductID(ObjectIdentifier.buildWithValue(bondGroupMap.getPlanTopProductSpecID()));
                            bondingMapInfo.setPlanTopBondingSide(bondGroupMap.getPlanTopBondSide());
                            bondingMapInfo.setActualTopWaferID(ObjectIdentifier.buildWithValue(bondGroupMap.getActiveTopWaferID()));
                            bondingMapInfo.setActualTopLotID(ObjectIdentifier.buildWithValue(bondGroupMap.getActiveTopLotID()));
                            bondingMapInfo.setActualTopBondingSide(bondGroupMap.getActiveTopBondSide());
                            bondingMapInfo.setBondingProcessState(bondGroupMap.getProcessState());
                            return bondingMapInfo;
                        }).collect(Collectors.toList());
                topLotIDSeq.addAll(bondingMapInfoList.stream().map(Infos.BondingMapInfo::getPlanTopLotID).collect(Collectors.toList()));
                bondingGroupInfo.setBondingMapInfoList(bondingMapInfoList);
            }
            return bondingGroupInfo;
        }).collect(Collectors.toList());

        Outputs.ObjBondingGroupInfoByEqpGetDROut retVal = new Outputs.ObjBondingGroupInfoByEqpGetDROut();
        retVal.setBondingGroupInfoList(bondingGroupInfos);
        //Remove duplicate elements for topLotIDSeq
        retVal.setTopLotIDSeq(topLotIDSeq.stream().distinct().collect(Collectors.toList()));
        return retVal;
    }

    @Override
    public Outputs.ObjBondingGroupInfoGetDROut bondingGroupInfoGetDR(Infos.ObjCommon objCommon, String bondingGroupID, Boolean bondingMapInfoFlag) {
        Outputs.ObjBondingGroupInfoGetDROut out = new Outputs.ObjBondingGroupInfoGetDROut();
        Infos.BondingGroupInfo bondingGroupInfo = new Infos.BondingGroupInfo();
        bondingGroupInfo.setBondingGroupID(bondingGroupID);

        //【step1】get bondingGroup info from db by bondingGroupID
        log.debug("【step1】get bondingGroup info from db by bondingGroupID: {}", bondingGroupID);
        CimBondGroupDO cimBondGroupExam = new CimBondGroupDO();
        cimBondGroupExam.setBondGroupID(bondingGroupID);
        CimBondGroupDO cimBondGroupDO = cimJpaRepository.findOne(Example.of(cimBondGroupExam)).orElse(null);
        Validations.check(null == cimBondGroupDO, new OmCode(retCodeConfig.getNotFoundBondingGroup(), bondingGroupID));
        bondingGroupInfo.setBondingGroupState(cimBondGroupDO.getBondGroupStatus());
        bondingGroupInfo.setTargetEquipmentID(ObjectIdentifier.buildWithValue(cimBondGroupDO.getEquipmentID()));
        bondingGroupInfo.setControlJobID(ObjectIdentifier.buildWithValue(cimBondGroupDO.getControlJobID()));
        out.setBondingGroupInfo(bondingGroupInfo);
        if (CimBooleanUtils.isFalse(bondingMapInfoFlag)) {
            bondingGroupInfo.setBondingMapInfoList(Collections.emptyList());
            out.setBondingLotIDList(Collections.emptyList());
            return out;
        }

        //【step2】get bonding map info if bondingMapInfoFlag is True
        log.debug("【step2】get bonding map info if bondingMapInfoFlag is True");
        CimBondGroupMapDO cimBondGroupMapExam = new CimBondGroupMapDO();
        cimBondGroupMapExam.setBondGroupID(bondingGroupID);
        List<Infos.BondingMapInfo> bondingMapInfoList = cimJpaRepository.findAll(Example.of(cimBondGroupMapExam)).stream()
                .map(cimBondGroupMapDO -> {
                    Infos.BondingMapInfo bondingMapInfo = new Infos.BondingMapInfo();
                    bondingMapInfo.setBondingGroupID(cimBondGroupMapDO.getBondGroupID());
                    bondingMapInfo.setBaseLotID(ObjectIdentifier.buildWithValue(cimBondGroupMapDO.getBaseLotID()));
                    bondingMapInfo.setBaseProductID(ObjectIdentifier.buildWithValue(cimBondGroupMapDO.getBaseProductSpecID()));
                    bondingMapInfo.setBaseWaferID(ObjectIdentifier.buildWithValue(cimBondGroupMapDO.getBaseWaferID()));
                    bondingMapInfo.setBaseBondingSide(cimBondGroupMapDO.getBaseBondSide());
                    bondingMapInfo.setPlanTopLotID(ObjectIdentifier.buildWithValue(cimBondGroupMapDO.getPlanTopLotID()));
                    bondingMapInfo.setPlanTopProductID(ObjectIdentifier.buildWithValue(cimBondGroupMapDO.getPlanTopProductSpecID()));
                    bondingMapInfo.setPlanTopWaferID(ObjectIdentifier.buildWithValue(cimBondGroupMapDO.getPlanTopWaferID()));
                    bondingMapInfo.setPlanTopBondingSide(cimBondGroupMapDO.getPlanTopBondSide());
                    bondingMapInfo.setActualTopLotID(ObjectIdentifier.buildWithValue(cimBondGroupMapDO.getActiveTopLotID()));
                    bondingMapInfo.setActualTopProductID(ObjectIdentifier.buildWithValue(cimBondGroupMapDO.getActiveTopProductSpecID()));
                    bondingMapInfo.setActualTopWaferID(ObjectIdentifier.buildWithValue(cimBondGroupMapDO.getActiveTopWaferID()));
                    bondingMapInfo.setActualTopBondingSide(cimBondGroupMapDO.getActiveTopBondSide());
                    bondingMapInfo.setBondingProcessState(cimBondGroupMapDO.getProcessState());
                    bondingMapInfo.setProcessCompleteTime(CimDateUtils.convertToSpecString(cimBondGroupMapDO.getProcessCompleteTime()));
                    return bondingMapInfo;
                }).collect(Collectors.toList());
        List<ObjectIdentifier> bondingLotIDList = bondingMapInfoList.stream()
                .flatMap(info -> Arrays.stream(new ObjectIdentifier[]{info.getBaseLotID(), info.getPlanTopLotID()}))
                .distinct()
                .collect(Collectors.toList());
        bondingGroupInfo.setBondingMapInfoList(bondingMapInfoList);
        out.setBondingLotIDList(bondingLotIDList);
        return out;
    }

    @Override
    public void bondingGroupStateUpdateDR(Infos.ObjCommon objCommon, String bondingGroupID, String bondingGroupState, ObjectIdentifier equipmentID, ObjectIdentifier controlJobID) {
        // OSBONDGRP
        CimBondGroupDO cimBondGroupExam = new CimBondGroupDO();
        cimBondGroupExam.setBondGroupID(bondingGroupID);
        CimBondGroupDO bondGroup = cimJpaRepository.findOne(Example.of(cimBondGroupExam)).orElse(null);
        Validations.check(CimObjectUtils.isEmpty(bondGroup), retCodeConfig.getNotFoundBondingGroup());
        if (!ObjectIdentifier.isEmpty(equipmentID)) {
            log.info("Update State and ControlJob");
            bondGroup.setBondGroupStatus(bondingGroupState);
            bondGroup.setEquipmentID(ObjectIdentifier.fetchValue(equipmentID));
            bondGroup.setControlJobID(ObjectIdentifier.fetchValue(controlJobID));
            cimJpaRepository.save(bondGroup);
        } else {
            log.info("Update State Only");
            bondGroup.setBondGroupStatus(bondingGroupState);
            cimJpaRepository.save(bondGroup);
        }
    }

    @Override
    public void bondingGroupInfoUpdateDR(Infos.ObjCommon objCommon, String action, Infos.BondingGroupInfo strBondingGroupInfo) {
        Validations.check(CimStringUtils.isEmpty(action), retCodeConfig.getInvalidParameter());
        Validations.check(null == strBondingGroupInfo, retCodeConfig.getInvalidParameter());
        boolean isCreate = CimStringUtils.equals(action, BizConstant.SP_BONDINGGROUPACTION_CREATE);
        boolean isUpdate = CimStringUtils.equals(action, BizConstant.SP_BONDINGGROUPACTION_UPDATE);
        boolean isDelete = CimStringUtils.equals(action, BizConstant.SP_BONDINGGROUPACTION_DELETE);
        boolean isPartialRelease = CimStringUtils.equals(action, BizConstant.SP_BONDINGGROUPACTION_PARTIALRELEASE);
        Validations.check(!isCreate && !isUpdate && !isDelete && !isPartialRelease, retCodeConfig.getInvalidParameter());

        log.debug("Action : " + action);
        if (isCreate) {
            CimBondGroupDO bondGroupDO = new CimBondGroupDO();
            bondGroupDO.setBondGroupID(strBondingGroupInfo.getBondingGroupID());
            bondGroupDO.setBondGroupStatus(BizConstant.SP_BONDINGGROUPSTATE_CREATED);
            bondGroupDO.setEquipmentID(ObjectIdentifier.fetchValue(strBondingGroupInfo.getTargetEquipmentID()));
            bondGroupDO.setControlJobID(ObjectIdentifier.fetchValue(strBondingGroupInfo.getControlJobID()));
            bondGroupDO.setClaimMemo(strBondingGroupInfo.getClaimMemo());
            bondGroupDO.setUpdateUserID(ObjectIdentifier.fetchValue(objCommon.getUser().getUserID()));
            bondGroupDO.setCreateTime(objCommon.getTimeStamp().getReportTimeStamp());
            cimJpaRepository.save(bondGroupDO);
            log.debug(" Save Success.");
        }

        if (isUpdate || isDelete || isPartialRelease) {
            CimBondGroupDO exampleDO = new CimBondGroupDO();
            exampleDO.setBondGroupID(strBondingGroupInfo.getBondingGroupID());
            CimBondGroupDO bondGroupDO = cimJpaRepository.findOne(Example.of(exampleDO)).orElse(null);
            Validations.check(null == bondGroupDO, retCodeConfig.getNotFoundBondingGroup());

            if (isUpdate || isPartialRelease) {
                bondGroupDO.setEquipmentID(ObjectIdentifier.fetchValue(strBondingGroupInfo.getTargetEquipmentID()));
                bondGroupDO.setUpdateUserID(ObjectIdentifier.fetchValue(objCommon.getUser().getUserID()));
                bondGroupDO.setUpdateTime(objCommon.getTimeStamp().getReportTimeStamp());
                bondGroupDO.setClaimMemo(strBondingGroupInfo.getClaimMemo());
                cimJpaRepository.save(bondGroupDO);
                log.debug(" Update Success.");
            }

            if (isDelete) {
                cimJpaRepository.save(bondGroupDO);

                CimBondGroupMapDO example = new CimBondGroupMapDO();
                example.setBondGroupID(strBondingGroupInfo.getBondingGroupID());
                cimJpaRepository.delete(Example.of(example));

                log.debug(" Delete Success.");
            }
        }

        if (isCreate || isPartialRelease) {
            //---------------------------------------
            // OSBONDGRP_MAP
            //---------------------------------------
            int index = 0;
            for (Infos.BondingMapInfo data : strBondingGroupInfo.getBondingMapInfoList()) {
                if (isCreate) {
                    CimBondGroupMapDO bondGroupMapDO = new CimBondGroupMapDO();
                    bondGroupMapDO.setBondGroupID(strBondingGroupInfo.getBondingGroupID());
                    bondGroupMapDO.setSequenceNumber(index++);
                    bondGroupMapDO.setBaseLotID(ObjectIdentifier.fetchValue(data.getBaseLotID()));
                    bondGroupMapDO.setBaseProductSpecID(ObjectIdentifier.fetchValue(data.getBaseProductID()));
                    bondGroupMapDO.setBaseWaferID(ObjectIdentifier.fetchValue(data.getBaseWaferID()));
                    bondGroupMapDO.setBaseBondSide(data.getBaseBondingSide());
                    bondGroupMapDO.setPlanTopLotID(ObjectIdentifier.fetchValue(data.getPlanTopLotID()));
                    bondGroupMapDO.setPlanTopProductSpecID(ObjectIdentifier.fetchValue(data.getPlanTopProductID()));
                    bondGroupMapDO.setPlanTopWaferID(ObjectIdentifier.fetchValue(data.getPlanTopWaferID()));
                    bondGroupMapDO.setPlanTopBondSide(data.getPlanTopBondingSide());
                    bondGroupMapDO.setProcessCompleteTime(CimDateUtils.initialTime());
                    cimJpaRepository.save(bondGroupMapDO);
                }

                if (isPartialRelease) {
                    CimBondGroupMapDO example = new CimBondGroupMapDO();
                    example.setBondGroupID(strBondingGroupInfo.getBondingGroupID());
                    example.setBaseLotID(ObjectIdentifier.fetchValue(data.getBaseLotID()));
                    example.setBaseWaferID(ObjectIdentifier.fetchValue(data.getBaseWaferID()));
                    cimJpaRepository.delete(Example.of(example));
                }
            }
        }
    }

    @Override
    public List<Infos.BondingGroupInfo> bondingGroupListGetDR(Infos.ObjCommon objCommon, Params.BondingGroupListInqInParams bondingGroupListInqInParams) {
        Validations.check(null == bondingGroupListInqInParams, retCodeConfig.getInvalidParameter());

        List<Infos.BondingGroupInfo> retVal = null;

        StringBuilder sql = new StringBuilder("select * from OSBONDGRP where BOND_GRP_ID is not null ");
        List<String> params = new ArrayList<>();
        if (CimStringUtils.isNotEmpty(bondingGroupListInqInParams.getBondingGroupID())) {
            log.info("bondingGroupID != 0");
            sql.append(" and BOND_GRP_ID like ?");
            params.add(bondingGroupListInqInParams.getBondingGroupID());
        }

        if (CimStringUtils.isNotEmpty(bondingGroupListInqInParams.getBondingGroupState())) {
            log.info("bondingGroupState != 0");
            sql.append(" and BOND_GRP_STATUS like ?");
            params.add(bondingGroupListInqInParams.getBondingGroupState());
        }

        if (!ObjectIdentifier.isEmptyWithValue(bondingGroupListInqInParams.getTargetEquipmentID())) {
            log.info("targetEquipmentID != 0");
            sql.append(" and EQP_ID like ?");
            params.add(bondingGroupListInqInParams.getTargetEquipmentID().getValue());
        }

        if (!ObjectIdentifier.isEmptyWithValue(bondingGroupListInqInParams.getControlJobID())) {
            log.info("controlJobID != 0");
            sql.append(" and CJ_ID like ?");
            params.add(bondingGroupListInqInParams.getControlJobID().getValue());
        }

        if (!ObjectIdentifier.isEmptyWithValue(bondingGroupListInqInParams.getUpdateUserID())) {
            log.info("updateUserID != 0");
            sql.append(" and UPDATE_USER_ID like ?");
            params.add(bondingGroupListInqInParams.getUpdateUserID().getValue());
        }

        if (!ObjectIdentifier.isEmptyWithValue(bondingGroupListInqInParams.getLotID())) {
            log.info("lotID != 0");
            sql.append("  and exists (select 1 from OSBONDGRP_MAP where OSBONDGRP_MAP.BOND_GRP_ID = OSBONDGRP.BOND_GRP_ID and (OSBONDGRP_MAP.BASE_LOT_ID like ? or OSBONDGRP_MAP.PLAN_TOP_LOT_ID like ?))");
            params.add(bondingGroupListInqInParams.getLotID().getValue());
            params.add(bondingGroupListInqInParams.getLotID().getValue());
        }

        if (!ObjectIdentifier.isEmptyWithValue(bondingGroupListInqInParams.getBaseProductID())) {
            log.info("baseProductID != 0");
            sql.append(" and exists (select 1 from OSBONDGRP_MAP where OSBONDGRP_MAP.BOND_GRP_ID = OSBONDGRP.BOND_GRP_ID and OSBONDGRP_MAP.BASE_PROD_ID like ?)");
            params.add(bondingGroupListInqInParams.getBaseProductID().getValue());
        }

        if (!ObjectIdentifier.isEmptyWithValue(bondingGroupListInqInParams.getTopProductID())) {
            log.info("topProductID != 0");
            sql.append(" and exists (select 1 from OSBONDGRP_MAP where OSBONDGRP_MAP.BOND_GRP_ID = OSBONDGRP.BOND_GRP_ID and OSBONDGRP_MAP.PLAN_TOP_PROD_ID like ?)");
            params.add(bondingGroupListInqInParams.getTopProductID().getValue());
        }
        List<CimBondGroupDO> bondGroupOut = cimJpaRepository.query(sql.toString(), CimBondGroupDO.class, params.toArray());
        if (CimArrayUtils.isNotEmpty(bondGroupOut)) {
            retVal = new ArrayList<>();
            for (CimBondGroupDO data : bondGroupOut) {
                log.info("bondingGroupID : " + data.getBondGroupID());
                Infos.BondingGroupInfo bondingGroupInfo = new Infos.BondingGroupInfo();
                bondingGroupInfo.setBondingGroupID(data.getBondGroupID());
                bondingGroupInfo.setBondingGroupState(data.getBondGroupStatus());
                bondingGroupInfo.setTargetEquipmentID(ObjectIdentifier.buildWithValue(data.getEquipmentID()));
                bondingGroupInfo.setControlJobID(ObjectIdentifier.buildWithValue(data.getControlJobID()));
                bondingGroupInfo.setUpdateUserID(ObjectIdentifier.buildWithValue(data.getUpdateUserID()));
                bondingGroupInfo.setCreateTime(data.getCreateTime().toString());
                bondingGroupInfo.setUpdateTime(CimObjectUtils.isEmpty(data.getUpdateTime())?null:data.getUpdateTime().toString());
                bondingGroupInfo.setClaimMemo(data.getClaimMemo());

                if (bondingGroupListInqInParams.getBondingMapInfoFlag()) {
                    List<Infos.BondingMapInfo> bondingMapInfos = new ArrayList<>();
                    bondingGroupInfo.setBondingMapInfoList(bondingMapInfos);
                    log.info("Getting Map Info");
                    //---------------------------------------------------------------------------------
                    //   Get Bonding Map Information.
                    //---------------------------------------------------------------------------------
                    String sql2 = "select  MAP.BASE_LOT_ID,\n" +
                            "     MAP.BASE_PROD_ID,\n" +
                            "     BASE_WAFER.CARRIER_ID as baseWafer_MTRLCONTNR_ID,       BASE_WAFER.CARRIER_RKEY as baseWafer_MTRLCONTNR_OBJ,\n" +
                            "     BASE_WAFER.POSITION as baseWafer_POSITION,\n" +
                            "     MAP.BASE_WAFER_ID,\n" +
                            "     MAP.BASE_BOND_SIDE,\n" +
                            "     MAP.PLAN_TOP_LOT_ID,\n" +
                            "     MAP.PLAN_TOP_PROD_ID,\n" +
                            "     PLAN_TOP_WAFER.CARRIER_ID as planTopWafer_MTRLCONTNR_ID,   PLAN_TOP_WAFER.CARRIER_RKEY as planTopWafer_MTRLCONTNR_OBJ,\n" +
                            "     PLAN_TOP_WAFER.POSITION as planTopWafer_POSITION,\n" +
                            "     MAP.PLAN_TOP_WAFER_ID,\n" +
                            "     MAP.PLAN_TOP_BOND_SIDE,\n" +
                            "     MAP.ACT_TOP_LOT_ID,\n" +
                            "     MAP.ACT_TOP_PROD_ID,\n" +
                            "     coalesce( ACT_TOP_WAFER.CARRIER_ID, '' ),    coalesce( ACT_TOP_WAFER.CARRIER_RKEY, '' ),\n" +
                            "     coalesce( ACT_TOP_WAFER.POSITION, 0 ),\n" +
                            "     MAP.ACT_TOP_WAFER_ID,\n" +
                            "     MAP.ACT_TOP_BOND_SIDE,\n" +
                            "     MAP.PROCESS_STATE,\n" +
                            "     MAP.PROCESS_COMP_TIME\n" +
                            "     from         OSBONDGRP_MAP      MAP\n" +
                            "     inner   join OMWAFER            BASE_WAFER     on BASE_WAFER.WAFER_ID     = MAP.BASE_WAFER_ID\n" +
                            "     inner   join OMWAFER            PLAN_TOP_WAFER on PLAN_TOP_WAFER.WAFER_ID = MAP.PLAN_TOP_WAFER_ID\n" +
                            "     left    join OMWAFER            ACT_TOP_WAFER  on ACT_TOP_WAFER.WAFER_ID  = MAP.ACT_TOP_WAFER_ID\n" +
                            "     where   MAP.BOND_GRP_ID = ?\n" +
                            "     order   by MAP.IDX_NO";
                    List<Object[]> bondMapOut = cimJpaRepository.query(sql2, data.getBondGroupID());
                    if (CimArrayUtils.isNotEmpty(bondMapOut)) {
                        bondMapOut.forEach(out -> {
                            Infos.BondingMapInfo info = new Infos.BondingMapInfo();
                            info.setBondingGroupID(data.getBondGroupID());
                            info.setBaseLotID(ObjectIdentifier.buildWithValue(CimObjectUtils.toString(out[0])));
                            info.setBaseProductID(ObjectIdentifier.buildWithValue(CimObjectUtils.toString(out[1])));
                            info.setBaseCarrierID(ObjectIdentifier.build(CimObjectUtils.toString(out[2]), CimObjectUtils.toString(out[3])));
                            info.setBaseSlotNo(Long.valueOf(CimObjectUtils.toString(out[4])));
                            info.setBaseWaferID(ObjectIdentifier.buildWithValue(CimObjectUtils.toString(out[5])));
                            info.setBaseBondingSide(CimObjectUtils.toString(out[6]));
                            info.setPlanTopLotID(ObjectIdentifier.buildWithValue(CimObjectUtils.toString(out[7])));
                            info.setPlanTopProductID(ObjectIdentifier.buildWithValue(CimObjectUtils.toString(out[8])));
                            info.setPlanTopCarrierID(ObjectIdentifier.build(CimObjectUtils.toString(out[9]), CimObjectUtils.toString(out[10])));
                            info.setPlanTopSlotNo(Long.valueOf(CimObjectUtils.toString(out[11])));
                            info.setPlanTopWaferID(ObjectIdentifier.buildWithValue(CimObjectUtils.toString(out[12])));
                            info.setPlanTopBondingSide(CimObjectUtils.toString(out[13]));
                            info.setActualTopLotID(ObjectIdentifier.buildWithValue(CimObjectUtils.toString(out[14])));
                            info.setActualTopProductID(ObjectIdentifier.buildWithValue(CimObjectUtils.toString(out[15])));
                            info.setActualTopCarrierID(ObjectIdentifier.build(CimObjectUtils.toString(out[16]), CimObjectUtils.toString(out[17])));
                            info.setActualTopSlotNo(Long.valueOf(CimObjectUtils.toString(out[18])));
                            info.setActualTopWaferID(ObjectIdentifier.buildWithValue(CimObjectUtils.toString(out[19])));
                            info.setActualTopBondingSide(CimObjectUtils.toString(out[20]));
                            info.setBondingProcessState(CimObjectUtils.toString(out[21]));
                            info.setProcessCompleteTime(CimObjectUtils.toString(out[22]));
                            bondingMapInfos.add(info);
                        });
                    }
                }
                retVal.add(bondingGroupInfo);
            }
        }
        return retVal;
    }

    @Override
	public void portWaferBondingCheck(Infos.ObjCommon objCommon, ObjectIdentifier portId, ObjectIdentifier equipmentId,
			ObjectIdentifier cassetteId) {
		// 【step 1】 find lot`s by cassette
		if (log.isInfoEnabled()) {
			log.info("portWaferBondingCheck : find lot`s by cassette");
		}
		CimCassette cassetteBO = baseCoreFactory.getBO(CimCassette.class, cassetteId);
		List<Lot> lots = cassetteBO.allLots();
		if (CollectionUtils.isEmpty(lots)) {
			log.info("portWaferBondingCheck : lot list is empty");
			return;
		}
		CimMachine eqpBo = baseCoreFactory.getBO(CimMachine.class, equipmentId);
		if (!CimStringUtils.equals(eqpBo.getCategory(), BizConstant.SP_MC_CATEGORY_WAFERBONDING)) {
			log.info("portWaferBondingCheck : eqp category is not wafer boding!");
			return;
		}

		// 【step 2】 check whether lot`s is doing wafer boding
		if (log.isInfoEnabled()) {
			log.info("portWaferBondingCheck : check whether lot`s is doing wafer boding");
		}
		List<String> lotIds = lots.parallelStream().map(Lot::getIdentifier).collect(Collectors.toList());
		String queryWaferBodingSql = "SELECT OSBONDGRP_MAP FROM OSBONDGRP_MAP WHERE "
				+ "(PLAN_TOP_LOT_ID IN ?1 OR BASE_LOT_ID IN ?2)";
		List<CimBondGroupMapDO> bondGroups = cimJpaRepository.query(queryWaferBodingSql, CimBondGroupMapDO.class,
				lotIds, lotIds);
		if (CollectionUtils.isEmpty(bondGroups)) {
			return;
		}

		// conversion tops and bases
		Map<String, String> lotBaseTop = Maps.newHashMap();
		bondGroups.forEach(cimBondGroupMapDO -> {
			if (StrUtil.isNotBlank(cimBondGroupMapDO.getPlanTopLotID())) {
				lotBaseTop.put(cimBondGroupMapDO.getPlanTopLotID(), BizConstant.WAFER_BONDING_TOP_LOAD_PORT);
			} else {
				lotBaseTop.put(cimBondGroupMapDO.getBaseLotID(), BizConstant.WAFER_BONDING_BASE_LOAD_PORT);
			}
		});

		// 【step 3】 find port , config pcs
        CimPortResource portBo = baseCoreFactory.getBO(CimPortResource.class, portId);
		Validations.check(Objects.isNull(portBo), retCodeConfig.getNotFoundPort());

		// 【step 4】 Get properties based on port , check !
		lotIds.forEach(lotId -> {
			String bondingSetting = lotBaseTop.get(lotId);
			if (StrUtil.isNotBlank(bondingSetting)) {
				Validations.check(!StrUtil.equals(portBo.getAssignBondingSetting(), bondingSetting),
						retCodeConfigEx.getBondgrpNotMatchBaseOrTop(), portId.getValue(),
                        bondingSetting);
			}
		});

	}

	@Override
	public String portWaferBodingTypeGetDR(Infos.ObjCommon objCommon, ObjectIdentifier portId,
			ObjectIdentifier equipmentId) {
		// 【step 1】 find top or base
		Validations.check(ObjectIdentifier.isEmpty(portId) || ObjectIdentifier.isEmpty(equipmentId),
				retCodeConfig.getInvalidParameter());

        CimMachine posMachine = baseCoreFactory.getBO(CimMachine.class, equipmentId);
		if (Objects.isNull(posMachine)) {
			log.error("portWaferBodingTypeGetDR->error: CimMachine is null {}", equipmentId);
			return null;
		}

		// step2. port get bonding setting
		CimPortResource portResource = baseCoreFactory.getBO(CimPortResource.class, portId);
		Validations.check(Objects.isNull(portResource), retCodeConfig.getNotFoundPort());
		return portResource.getAssignBondingSetting();
	}
}
