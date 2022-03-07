package com.fa.cim.service.fam.impl;

import com.fa.cim.annotaion.OmService;
import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.constant.CIMStateConst;
import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.utils.CimArrayUtils;
import com.fa.cim.common.utils.CimObjectUtils;
import com.fa.cim.common.utils.CimStringUtils;
import com.fa.cim.common.utils.Validations;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.entity.runtime.cassette.CimCassetteDO;
import com.fa.cim.entity.runtime.lot.CimLotDO;
import com.fa.cim.entity.runtime.plannedsplitjob.CimPlannedSplitJobDO;
import com.fa.cim.fam.Outputs;
import com.fa.cim.jpa.CimJpaRepository;
import com.fa.cim.method.ICassetteMethod;
import com.fa.cim.method.IDurableMethod;
import com.fa.cim.method.impl.SorterNewMethod;
import com.fa.cim.newcore.bo.durable.CimCassette;
import com.fa.cim.newcore.bo.durable.CimDurableSubState;
import com.fa.cim.newcore.bo.pd.CimProcessFlow;
import com.fa.cim.newcore.bo.pd.CimProcessOperationSpecification;
import com.fa.cim.newcore.factory.BaseCoreFactory;
import com.fa.cim.newcore.impl.bo.env.StandardProperties;
import com.fa.cim.service.fam.IFAMInqService;
import com.fa.cim.sorter.Info;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.stream.Collectors;

/**
 * description:
 * <p>
 * change history:
 * date defect# person comments
 * ‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐
 * 2021/4/21 ******** zh create file
 *
 * @author: zh
 * @date: 2021/4/21 17:17
 * @copyright: 2021, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@OmService
@Slf4j
public class FAMInqServiceImpl implements IFAMInqService {

    @Autowired
    private CimJpaRepository cimJpaRepository;
    @Autowired
    private SorterNewMethod sorterMethod;
    @Autowired
    private ICassetteMethod cassetteMethod;
    @Autowired
    private BaseCoreFactory baseCoreFactory;
    @Autowired
    private IDurableMethod durableMethod;
    @Autowired
    private RetCodeConfig retCodeConfig;

    @Override
    public Boolean sxHasSJHistoryListInq(Params.SortJobHistoryParams sortJobHistoryParams) {
        List<Object[]> query = cimJpaRepository.query(
                "SELECT\n" +
                        "\tf.SORTER_JOB_ID\n" +
                        "FROM\n" +
                        "\tFHSORTJOBHS f,\n" +
                        "\tFHSORTJOBHS_COMPONENT fc\n" +
                        "WHERE\n" +
                        "\tfc.SORTER_JOB_ID = f.SORTER_JOB_ID\n" +
                        "\tAND f.SORTER_JOB_STATUS = 'Completed'\n" +
                        "\tAND f.SORTER_JOB_CATEGORY = ?\n" +
                        "\tAND fc.DEST_CAST_ID = ?\n" +
                        "\tAND fc.SRC_CAST_ID = ?", sortJobHistoryParams.getSortJobCategory(), sortJobHistoryParams.getDestCastID(), sortJobHistoryParams.getSrcCastID());
        return CimArrayUtils.isNotEmpty(query);
    }

    @Override
    public List<Outputs.AutoSplitOut> autoSplits() {
        List<Outputs.AutoSplitOut> autoSplitOuts = new ArrayList<>();
        String reasonSprHold = BizConstant.SP_REASON_SPR_HOLD;
        String holdType = BizConstant.SP_HOLDTYPE_LOTHOLD;

        log.debug("获取separate hold");
        List<CimPlannedSplitJobDO> plannedSplitJobs = cimJpaRepository.query("SELECT * FROM FRPLSPLIT WHERE SEPARATE_ACTION = 1", CimPlannedSplitJobDO.class);
        for (CimPlannedSplitJobDO plannedSplitJob : plannedSplitJobs) {

            log.debug("通过lotfamily获取指定hold reason的lot");
            String lotFamilyID = plannedSplitJob.getLotFamilyID();
            List<CimLotDO> lots = getLotByLotFamilyAndHoldReason(lotFamilyID, holdType, reasonSprHold);
            if (CimArrayUtils.isEmpty(lots)) {
                log.debug("lots is null");
                continue;
            }

//        --3.检查lots是否在同一个casstte
            List<String> lotIDs = lots.stream().map(CimLotDO::getLotID).collect(Collectors.toList());
            String casstteID = getCassettesByHoldLot(lotIDs);
            if (CimStringUtils.isEmpty(casstteID)) {
                log.debug("lots 不属于同一个carrier");
                continue;
            }

            Outputs.AutoSplitOut autoSplitOut = new Outputs.AutoSplitOut();
            autoSplitOut.setCassetteID(casstteID);
            autoSplitOut.setSorterJobCategory("AutoSplitWait");
            autoSplitOut.setSorterLotID(lots.stream().filter(x -> x.getOriginalLot().equals(false)).findFirst().get().getLotID());
            autoSplitOut.setLotIDs(lotIDs);
            autoSplitOut.setHoldType(holdType);
            autoSplitOut.setHoldReasonCodeID(reasonSprHold);
            autoSplitOut.setReleaseReasonCodeID(BizConstant.SP_REASON_LOTHOLD_RELEASE);
            autoSplitOuts.add(autoSplitOut);

        }
        return autoSplitOuts;
    }

    @Override
    public List<Outputs.AutoMergeOut> autoMerges() {
        List<Outputs.AutoMergeOut> autoMergeOuts = new ArrayList<>();
        String reasonCombineHold = BizConstant.SP_REASON_COMBINEHOLD;
        String holdType = BizConstant.SP_HOLDTYPE_MERGEHOLD;

        log.debug("获取separate hold");
        List<CimPlannedSplitJobDO> plannedSplitJobs = cimJpaRepository.query("SELECT LOTFAMILY_ID FROM FRPLSPLIT WHERE COMBINE_ACTION = 1", CimPlannedSplitJobDO.class);
        for (CimPlannedSplitJobDO plannedSplitJob : plannedSplitJobs) {

            log.debug("通过lotfamily获取指定hold reason的lot");
            String lotFamilyID = plannedSplitJob.getLotFamilyID();
            List<CimLotDO> lots = getLotByLotFamilyAndHoldReason(lotFamilyID, holdType, reasonCombineHold);
            if (CimArrayUtils.isEmpty(lots)) {
                log.debug("lots is null");
                continue;
            }

            List<String> lotIDs = new ArrayList<>();
            log.debug("get parent lotID and carrier");
            String lotID = lots.stream().filter(x -> x.getOriginalLot().equals(true)).findFirst().get().getLotID();
            CimCassetteDO cimCassetteDO = cimJpaRepository.queryOne("SELECT f1.CAST_ID FROM FRCAST f1, FRCAST_LOT f2 WHERE f1.ID = f2.REFKEY AND F2.LOT_ID = ?", CimCassetteDO.class, lotID);
            String destinationCassetteID = cimCassetteDO.getCassetteID();
            lotIDs.add(lotID);

            log.debug("get child lotID and carrier");
            List<CimLotDO> childs = lots.stream().filter(x -> x.getOriginalLot().equals(false)).collect(Collectors.toList());
            boolean isSameCarrier = true;
            String cassetteID = null, sorterLotID = null;
            for (CimLotDO child : childs) {
                sorterLotID = child.getLotID();
                CimCassetteDO cassette = cimJpaRepository.queryOne("SELECT f1.CAST_ID FROM FRCAST f1, FRCAST_LOT f2 WHERE f1.ID = f2.REFKEY AND F2.LOT_ID = ?", CimCassetteDO.class, sorterLotID);
                cassetteID = cassette.getCassetteID();

                log.debug("检查parent carrier 和 child carrier是否为同一个");
                if(!destinationCassetteID.equals(cassetteID)){
                    isSameCarrier = false;
                    lotIDs.add(sorterLotID);
                    log.debug("isSameCarrier = false");
                }
            }
            if(isSameCarrier){
                log.debug("isSameCarrier = true");
                log.error("parent carrier 和 child carrier是同一个，不能进行auto merge的sorter流程");
                continue;
            }

            Outputs.AutoMergeOut autoMergeOut = new Outputs.AutoMergeOut();
            autoMergeOut.setCassetteID(cassetteID);
            autoMergeOut.setDestinationCassetteID(destinationCassetteID);
            autoMergeOut.setSorterJobCategory("AutoMergeWait");
            autoMergeOut.setSorterLotID(sorterLotID);
            autoMergeOut.setLotIDs(lotIDs);
            autoMergeOut.setHoldType(holdType);
            autoMergeOut.setHoldReasonCodeID(reasonCombineHold);
            autoMergeOut.setReleaseReasonCodeID(BizConstant.SP_REASON_LOTHOLD_RELEASE);
            autoMergeOut.setLotID(lotID);
            autoMergeOut.setChildLotID(sorterLotID);
            autoMergeOuts.add(autoMergeOut);

        }
        return autoMergeOuts;
    }

    public List<CimLotDO> getLotByLotFamilyAndHoldReason(String lotFamily, String holdType, String holdReasonCode) {
        return cimJpaRepository.query("SELECT f1.LOT_ID,f1.ORIGINAL_LOT FROM FRLOT f1, FRLOT_HOLDRECORD f2 WHERE f1.LOT_FAMILY_ID = ?1 AND f1.ID = f2.REFKEY AND " +
                "f1.LOT_HOLD_STATE = 'ONHOLD' AND f1.LOT_STATE = 'ACTIVE' AND f2.HOLD_REASON_ID = ?2 AND f2.HOLD_TYPE = ?3", CimLotDO.class, lotFamily, holdReasonCode, holdType);
    }

    public String getCassettesByHoldLot(List<String> lotIDs) {
        String firstCst = null;
        for (int i = 0; i < lotIDs.size(); i++) {
            CimCassetteDO cassette = cimJpaRepository.queryOne("SELECT f1.CAST_ID FROM FRCAST f1, FRCAST_LOT f2 WHERE f1.ID = f2.REFKEY AND F2.LOT_ID = ?", CimCassetteDO.class, lotIDs.get(i));
            if (i == 0) {
                firstCst = cassette.getCassetteID();
            }
            if (!firstCst.equals(cassette.getCassetteID())) {
                return null;
            }
        }
        return firstCst;
    }

    @Override
    public List<Infos.AvailableCarrierOut> sxAvailableCarrierListForLotStartInq(Infos.ObjCommon objCommon, com.fa.cim.fam.Params.AvailableCarrierInqParams params) {

        String productRequestID = params.getProductRequestID();
        ObjectIdentifier cassetteID = params.getCassetteID();
        String cassetteCategory = params.getCassetteCategory();
        if (CimObjectUtils.isEmpty(cassetteCategory)) {
            if (!CimObjectUtils.isEmpty(productRequestID)) {
                cassetteCategory = getCastReq(new ObjectIdentifier(productRequestID));
            } else {
                CimCassette cassette = baseCoreFactory.getBO(CimCassette.class, cassetteID);
                Validations.check(cassette == null, retCodeConfig.getNotFoundCassette());
                cassetteCategory = cassette.getCassetteCategory();
            }
        }

        /*------------------------*/
        /*   Get Empty Cassette   */
        /*------------------------*/
        List<Infos.AvailableCarrierOut> val = new ArrayList<>();
        List<Infos.FoundCassette> strFoundCassette = new ArrayList<>();
        Map<String, Boolean> availableForDurableFlagList = new HashMap<>();

        String sql = "select * from (SELECT * FROM FRCAST WHERE DRBL_STATE = 'AVAILABLE' AND TRANS_STATE = 'MI' AND CAST_USED_CAPACITY = 0 AND CAST_CATEGORY = ?1 order by TRANS_STATE desc ) where rownum<=10";
        List<CimCassetteDO> queriedCasts = cimJpaRepository.query(sql, CimCassetteDO.class, cassetteCategory);

        if (CimArrayUtils.isNotEmpty(queriedCasts)) {
            for (CimCassetteDO cassette : queriedCasts) {

                List<Info.SortJobListAttributes> sortJobListAttributesList = null;
                com.fa.cim.sorter.Params.ObjSorterJobListGetDRIn objSorterJobListGetDRIn = new com.fa.cim.sorter.Params.ObjSorterJobListGetDRIn();
                objSorterJobListGetDRIn.setCarrierID(new ObjectIdentifier(cassette.getCassetteID(), cassette.getId()));
                try {
                    sortJobListAttributesList = sorterMethod.sorterJobListGetDR(objCommon, objSorterJobListGetDRIn);
                } catch (ServiceException e) {
                    break;
                }

                int sorterLen = CimArrayUtils.getSize(sortJobListAttributesList);
                if (sorterLen > 0) {
                    continue;
                }
                List<Infos.HashedInfo> hashedInfoList = new ArrayList<>();

                Infos.FoundCassette foundCassette = new Infos.FoundCassette();
                //--------------------------------------
                // Set InPostProcessFlag of Cassette
                //--------------------------------------
                foundCassette.setSorterJobExistFlag(sorterLen > 0);
                foundCassette.setCassetteID(new ObjectIdentifier(cassette.getCassetteID(), cassette.getId()));
                foundCassette.setInPostProcessFlagOfCassette(cassette.getPostProcessingFlag());
                foundCassette.setDescription(cassette.getDescription());
                foundCassette.setCassetteCategory(cassette.getCassetteCategory());
                long cassetteUsedCapacity = null == cassette.getCastUsedCapacity() ? 0L : cassette.getCastUsedCapacity();
                foundCassette.setEmptyFlag(cassetteUsedCapacity == 0);
                foundCassette.setCassetteStatus(cassette.getDurableState());
                foundCassette.setTransferStatus(cassette.getTransferState());
                String zoneTypeNeedFlg = StandardProperties.OM_CARRIER_LIST_NEED_ZONE_TYPE.getValue();
                if (CimStringUtils.equals(zoneTypeNeedFlg, "1") && CimStringUtils.equals(objCommon.getTransactionID(), TransactionIDEnum.CASSETTE_LIST_INQ.getValue())) {
                    com.fa.cim.dto.Outputs.CassetteZoneTypeGetOut cassetteZoneTypeGetOut = null;
                    try {
                        cassetteZoneTypeGetOut = cassetteMethod.cassetteZoneTypeGet(objCommon, foundCassette.getCassetteID());
                    } catch (ServiceException e) {
                        break;
                    }
                    foundCassette.setZoneType(cassetteZoneTypeGetOut == null ? null : cassetteZoneTypeGetOut.getZoneType());
                }
                foundCassette.setMultiLotType(cassette.getMultiLotType());

                String transferState = cassette.getTransferState();
                if (BizConstant.equalsIgnoreCase(BizConstant.SP_TRANSSTATE_EQUIPMENTIN, transferState)
                        || BizConstant.equalsIgnoreCase(BizConstant.SP_TRANSSTATE_EQUIPMENTOUT, transferState)) {
                    foundCassette.setEquipmentID(new ObjectIdentifier(cassette.getEquipmentID(), cassette.getEquipmentObj()));
                } else {
                    foundCassette.setStockerID(new ObjectIdentifier(cassette.getEquipmentID(), cassette.getEquipmentObj()));
                }
                foundCassette.setUsageCheckFlag(cassette.getUsageCheckReq());

                double cassetteDurationLimit = (null == cassette.getDurationLimit()
                        ? 0 : (cassette.getDurationLimit() < 0 ? 0 : cassette.getDurationLimit()));
                final int minutesToMilliSec = 60 * 1000; // millisec -> minutes
                foundCassette.setMaximumRunTime(String.valueOf(cassetteDurationLimit / minutesToMilliSec));     // milisec -> minutes
                foundCassette.setMaximumOperationStartCount(cassette.getTimesUsedLimit());
                foundCassette.setIntervalBetweenPM(cassette.getIntervalBetweenPM());
                foundCassette.setCapacity(cassette.getCassetteCapacity());
                foundCassette.setNominalSize(cassette.getWaferSize());
                foundCassette.setContents(cassette.getMaterialContents());
                foundCassette.setInstanceName(cassette.getInstanceName());
                foundCassette.setCurrentLocationFlag(cassette.getCurrentLocationFlag());
                foundCassette.setBackupState(cassette.getBackupState());
                foundCassette.setSlmReservedEquipmentID(new ObjectIdentifier(cassette.getSlmReservedEquipmentID(), cassette.getSlmReservedEquipmentObj()));
                foundCassette.setInterFabTransferState(cassette.getInterFabTransferState());
                foundCassette.setDurableControlJobID(new ObjectIdentifier(cassette.getDurableControlJobID(), cassette.getDurableControlJobObj()));
                boolean durableSTBFlag = !CimStringUtils.isEmpty(cassette.getDurableProcessFlowContextObj());
                foundCassette.setDurablesSTBFlag(durableSTBFlag);
                foundCassette.setDurableSubStatus(new ObjectIdentifier(cassette.getDurableSubStateID(), cassette.getDurableSubStateObj()));
                foundCassette.setProductUsage(cassette.getProductUsage());
                foundCassette.setCarrierType(cassette.getCarrierType());
                //--- Set availableForDurableFlag; ------------------------------------//
                boolean flag = false;
                if (!CimStringUtils.isEmpty(cassette.getDurableSubStateID())) {
                    if (!availableForDurableFlagList.containsKey(cassette.getDurableSubStateID())) {
                        CimDurableSubState aDurableSubState = baseCoreFactory.getBO(CimDurableSubState.class, cassette.getDurableSubStateID());
                        if (null != aDurableSubState) {
                            flag = aDurableSubState.isDurableProcessAvailable();
                            availableForDurableFlagList.put(cassette.getDurableSubStateID(), flag);
                        }
                    } else {
                        flag = availableForDurableFlagList.get(cassette.getDurableSubStateID());
                    }
                }
                foundCassette.setAvailableForDurableFlag(flag);
                //--- Set pptHashedInfoSequence strDurableStatusList; ----------------------//
                String durableFlowState = "";
                if (CIMStateConst.equals(CIMStateConst.CIM_DURABLE_SCRAPPED, cassette.getDurableState())) {
                    durableFlowState = cassette.getDurableState();
                } else if (CimStringUtils.equals(BizConstant.SP_DURABLE_HOLDSTATE_ONHOLD, cassette.getDurableHoldState())) {
                    durableFlowState = cassette.getDurableHoldState();
                } else if (CimStringUtils.equals(BizConstant.SP_DURABLE_FINISHEDSTATE_COMPLETED, cassette.getDurableFinishedState())) {
                    durableFlowState = cassette.getDurableFinishedState();
                } else {
                    durableFlowState = cassette.getDurableProcessState();
                }

                Infos.HashedInfo hashedInfo0 = new Infos.HashedInfo();
                //DurableStatusList[0]
                hashedInfo0.setHashKey("durable Flow State");
                hashedInfo0.setHashData(durableFlowState);
                hashedInfoList.add(hashedInfo0);

                String durableState = "";
                if (!CimStringUtils.isEmpty(cassette.getRouteID())) {
                    if (CIMStateConst.equals(CIMStateConst.CIM_DURABLE_SCRAPPED, cassette.getDurableState())
                            || BizConstant.equalsIgnoreCase(BizConstant.SP_DURABLE_FINISHEDSTATE_COMPLETED, cassette.getDurableFinishedState())) {
                        durableState = BizConstant.SP_DURABLE_ONROUTESTATE_FINISHED;
                    } else {
                        durableState = BizConstant.SP_DURABLE_ONROUTESTATE_ACTIVE;
                    }
                }
                Infos.HashedInfo hashedInfo1 = new Infos.HashedInfo();
                //DurableStatusList[1]
                hashedInfo1.setHashKey("durable State");
                hashedInfo1.setHashData(durableState);
                hashedInfoList.add(hashedInfo1);

                //DurableStatusList[2]
                Infos.HashedInfo hashedInfo2 = new Infos.HashedInfo();
                hashedInfo2.setHashKey("durable Production State");
                hashedInfo2.setHashData(cassette.getDurableProductionState());
                hashedInfoList.add(hashedInfo2);

                //DurableStatusList[3]
                Infos.HashedInfo hashedInfo3 = new Infos.HashedInfo();
                hashedInfo3.setHashKey("durable Hold State");
                hashedInfo3.setHashData(cassette.getDurableHoldState());
                hashedInfoList.add(hashedInfo3);

                //DurableStatusList[4]
                Infos.HashedInfo hashedInfo4 = new Infos.HashedInfo();
                hashedInfo4.setHashKey("durable Finished State");
                hashedInfo4.setHashData(cassette.getDurableFinishedState());
                hashedInfoList.add(hashedInfo4);

                //DurableStatusList[5]
                Infos.HashedInfo hashedInfo5 = new Infos.HashedInfo();
                hashedInfo5.setHashKey("durable Process State");
                hashedInfo5.setHashData(cassette.getDurableProcessState());
                hashedInfoList.add(hashedInfo5);

                //DurableStatusList[6]
                Infos.HashedInfo hashedInfo6 = new Infos.HashedInfo();
                hashedInfo6.setHashKey("durable Inventory State");
                hashedInfo6.setHashData(cassette.getDurableInventoryState());
                hashedInfoList.add(hashedInfo6);
                foundCassette.setDurableStatusList(hashedInfoList);

                foundCassette.setBankID(new ObjectIdentifier(cassette.getBankID(), cassette.getBankObj()));
                foundCassette.setDueTimeStamp("");

                // get start bank id
                if (!CimStringUtils.isEmpty(cassette.getRouteID())) {
                    //--- Set objectIdentifier startBankID; ------------------------------------//
                    String processDefLevel = BizConstant.SP_PD_FLOWLEVEL_MAIN;
                    String sql1 = "SELECT START_BANK_ID, START_BANK_RKEY FROM FRPD WHERE PD_ID = ?1 AND PD_LEVEL = ?2";
                    List<Object[]> objects = cimJpaRepository.query(sql1, cassette.getRouteID(), processDefLevel);
                    if (!CimArrayUtils.isEmpty(objects)) {
                        Object[] object = objects.get(0);
                        foundCassette.setStartBankID(new ObjectIdentifier((String) object[0], (String) object[1]));
                    }
                }

                foundCassette.setRouteID(new ObjectIdentifier(cassette.getRouteID(), cassette.getRouteObj()));
                foundCassette.setOperationNumber(cassette.getOperationNumber());
                foundCassette.setBankInRequiredFlag(cassette.getBankInRequired());

                //【step5】get durable hold record
                ObjectIdentifier durableID = foundCassette.getCassetteID();
                List<Infos.DurableHoldRecord> retCode = null;
                try {
                    retCode = durableMethod.durableHoldRecordGetDR(objCommon, durableID, BizConstant.SP_DURABLECAT_CASSETTE);
                } catch (ServiceException e) {
                    break;
                }
                //--- Set objectIdentifier holdReasonCodeID; -------------------------//
                int holdLen = CimArrayUtils.getSize(retCode);
                ObjectIdentifier holdReasonCodeID = new ObjectIdentifier();
                if (1 == holdLen) {
                    holdReasonCodeID = retCode.get(0).getHoldReasonCodeID();
                } else if (1 < holdLen) {
                    holdReasonCodeID = new ObjectIdentifier(String.format("%s*", retCode.get(0).getHoldReasonCodeID().getValue()));
                }
                foundCassette.setHoldReasonCodeID(holdReasonCodeID);

                strFoundCassette.add(foundCassette);
            }
        }

        /*-----------------------------------*/
        /*   Pick Up Target Empty Cassette   */
        /*-----------------------------------*/
        com.fa.cim.dto.Outputs.ObjCassetteListEmptyAvailablePickUpOut cassetteListEmptyAvailablePickupOutRetCode = cassetteMethod.cassetteListEmptyAvailablePickup(objCommon, strFoundCassette);
        if (CimArrayUtils.isNotEmpty(cassetteListEmptyAvailablePickupOutRetCode.getStrFoundCassette())) {
            for (Infos.FoundCassette foundCassette : cassetteListEmptyAvailablePickupOutRetCode.getStrFoundCassette()) {
                Infos.AvailableCarrierOut availableCarrierOut = new Infos.AvailableCarrierOut();
                availableCarrierOut.setFoupID(ObjectIdentifier.fetchValue(foundCassette.getCassetteID()));
                availableCarrierOut.setTransferStatus(foundCassette.getTransferStatus());
                val.add(availableCarrierOut);
            }
        }
        int n = new Random().nextInt(val.size());
        Infos.AvailableCarrierOut availableCarrierOut = val.get(n);
        val.clear();

        val.add(availableCarrierOut);
        return val;
    }

    private String getCastReq(ObjectIdentifier productRequestID) {
        String reqCastCategory;
        String sql = "SELECT\n" +
                "    TMP.MODULE_NO,\n" +
                "    TMP.STEP_NO,\n" +
                "    TMP.ID,\n" +
                "    TMP.MAIN_OPE_PF\n" +
                "    FROM\n" +
                "    (\n" +
                "        SELECT\n" +
                "            PF_PD.MODULE_NO AS MODULE_NO,\n" +
                "            POS.OPE_NO      AS STEP_NO,\n" +
                "            POS.ID,\n" +
                "            PF_PD.D_SEQNO  AS MAIN_SEQNO,\n" +
                "            PF_POS.D_SEQNO AS MODULE_SEQNO,\n" +
                "            MAINPD.ACTIVE_PRPF_RKEY AS MAIN_OPE_PF\n" +
                "        FROM\n" +
                "            FRPRODREQ PRO_ORDER,\n" +
                "            FRPD MAINPD,\n" +
                "            FRPF MAIN_MODPF,\n" +
                "            FRPF_PDLIST PF_PD,\n" +
                "            FRPD MODULEPD,\n" +
                "            FRPF MOUDLEPF,\n" +
                "            FRPF_POSLIST PF_POS,\n" +
                "            FRPOS POS\n" +
                "        WHERE\n" +
                "            PRO_ORDER.PRODREQ_ID = ?1 \n" +
                "        AND PRO_ORDER.MAINPD_ID = MAINPD.PD_ID\n" +
                "        AND MAINPD.ACTIVE_MAINPF_RKEY = MAIN_MODPF.ID\n" +
                "        AND MAIN_MODPF.ID = PF_PD.REFKEY\n" +
                "        AND PF_PD.PD_OBJ = MODULEPD.ID\n" +
                "        AND MODULEPD.ACTIVE_PF_OBJ = MOUDLEPF.ID\n" +
                "        AND MOUDLEPF.ID = PF_POS.REFKEY\n" +
                "        AND PF_POS.POS_OBJ = POS.ID\n" +
                "        ORDER BY\n" +
                "            4,5 ) TMP\n" +
                "WHERE\n" +
                "    ROWNUM = 1 ";


        Object[] resultQuery = cimJpaRepository.queryOne(sql, productRequestID.getValue());
        Validations.check(CimObjectUtils.isEmpty(resultQuery), retCodeConfig.getInvalidParameter());
        String opeNum = CimObjectUtils.toString(resultQuery[0]) + "." + CimObjectUtils.toString(resultQuery[1]);
        String PF_OBJ = CimObjectUtils.toString(resultQuery[3]);
        CimProcessFlow aPF = baseCoreFactory.getBO(CimProcessFlow.class, PF_OBJ);
        CimProcessOperationSpecification processOperationSpecificationOnDefault = aPF.findProcessOperationSpecificationOnDefault(opeNum);
        if (CimObjectUtils.isEmpty(processOperationSpecificationOnDefault)) {
            return null;
        }
        reqCastCategory = processOperationSpecificationOnDefault.getRequiredCassetteCategory();
        return reqCastCategory;
    }

}