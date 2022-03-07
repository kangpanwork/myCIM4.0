package com.fa.cim.method.impl;

import com.fa.cim.annotaion.OmMethod;
import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.constant.CIMStateConst;
import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.OmCode;
import com.fa.cim.common.utils.*;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.config.RetCodeConfigEx;
import com.fa.cim.dto.*;
import com.fa.cim.entity.runtime.cassette.*;
import com.fa.cim.entity.runtime.durable.CimDurableDurableGroupDO;
import com.fa.cim.entity.runtime.durablepfx.CimDurablePFXDurablePOListDO;
import com.fa.cim.entity.runtime.durablepo.CimDurableProcessOperationDO;
import com.fa.cim.entity.runtime.durablesubstate.CimDurableSubStateDO;
import com.fa.cim.entity.runtime.env.CimEnvironmentZoneTypeDO;
import com.fa.cim.entity.runtime.eqp.CimEquipmentDO;
import com.fa.cim.entity.runtime.lot.CimLotDO;
import com.fa.cim.entity.runtime.processdefinition.CimProcessDefinitionDO;
import com.fa.cim.entity.runtime.processflow.CimPFPosListDO;
import com.fa.cim.entity.runtime.processflow.CimProcessFlowDO;
import com.fa.cim.enums.MethodEnums;
import com.fa.cim.jpa.SearchCondition;
import com.fa.cim.method.*;
import com.fa.cim.newcore.bo.durable.CimCassette;
import com.fa.cim.newcore.bo.durable.CimDurableSubState;
import com.fa.cim.newcore.bo.factory.CimArea;
import com.fa.cim.newcore.bo.machine.CimMachine;
import com.fa.cim.newcore.bo.machine.CimMaterialLocation;
import com.fa.cim.newcore.bo.machine.CimPortResource;
import com.fa.cim.newcore.bo.machine.CimStorageMachine;
import com.fa.cim.newcore.bo.pd.CimQTimeRestriction;
import com.fa.cim.newcore.bo.person.CimPerson;
import com.fa.cim.newcore.bo.prodspec.CimProductSpecification;
import com.fa.cim.newcore.bo.product.CimControlJob;
import com.fa.cim.newcore.bo.product.CimLot;
import com.fa.cim.newcore.bo.product.ProductManager;
import com.fa.cim.newcore.bo.recipe.CimLogicalRecipe;
import com.fa.cim.newcore.bo.recipe.CimMachineRecipe;
import com.fa.cim.newcore.dto.pd.ProcessDTO;
import com.fa.cim.newcore.dto.product.ProductDTO;
import com.fa.cim.newcore.dto.recipe.RecipeDTO;
import com.fa.cim.newcore.factory.BaseCoreFactory;
import com.fa.cim.newcore.framework.jpa.CoreJpaRepository;
import com.fa.cim.newcore.impl.bo.env.StandardProperties;
import com.fa.cim.newcore.standard.drblmngm.MaterialContainer;
import com.fa.cim.newcore.standard.fctrycmp.Area;
import com.fa.cim.newcore.standard.mchnmngm.BufferResource;
import com.fa.cim.newcore.standard.mchnmngm.Machine;
import com.fa.cim.newcore.standard.mchnmngm.MaterialLocation;
import com.fa.cim.newcore.standard.mchnmngm.PortResource;
import com.fa.cim.newcore.standard.mtrlmngm.Material;
import com.fa.cim.newcore.standard.prdctmng.Lot;
import com.fa.cim.sorter.Info;
import com.fa.cim.sorter.SorterHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;

import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;
import java.util.stream.Collectors;


/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2018/6/21        ********             Bear               create file
 *
 * @author Bear
 * @since 2018/6/21 15:16
 * Copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@OmMethod
@Slf4j
public class CassetteMethod implements ICassetteMethod {

    @Autowired
    private IEquipmentMethod equipmentMethod;

    @Autowired
    private RetCodeConfigEx retCodeConfigEx;

    @Autowired
    private ILotMethod lotMethod;

    @Autowired
    private IPortMethod portMethod;

    @Autowired
    private IStockerMethod stockerComp;

    @Autowired
    private IDurableMethod durableMethod;

    @Autowired
    private IProcessMethod processMethod;

    @Autowired
    private IControlJobMethod controlJobMethod;

    @Autowired
    private CoreJpaRepository cimJpaRepository;

    @Autowired
    private IWaferMethod waferMethod;

    @Autowired
    private IPostProcessMethod postProcessMethod;

    @Autowired
    private IEventMethod eventMethod;

    @Autowired
    private IAutoDispatchControlMethod autoDispatchControlMethod;

    @Autowired
    private IVirtualOperationMethod virtualOperationMethod;

    @Autowired
    private BaseCoreFactory baseCoreFactory;

    @Autowired
    @Qualifier("ProductManagerCore")
    private ProductManager productManager;

    @Autowired
    private RetCodeConfig retCodeConfig;

    @Autowired
    private IOperationMethod operationMethod;

    @Autowired
    private IInterFabMethod interFabMethod;

    @Autowired
    private IEquipmentContainerPositionMethod equipmentContainerPositionMethod;

    @Autowired
    private ISorterNewMethod sorterNewMethod;

    @Autowired
    private IAreaMethod areaMethod;

    @Override
    public Page<Infos.FoundCassette> cassetteListGetDR170(Infos.ObjCommon objCommon, Inputs.ObjCassetteListGetDRIn objCassetteListGetDRIn) {
        AtomicBoolean firstCondition = new AtomicBoolean();
        firstCondition.set(true);
        long maxRetrieveCount = objCassetteListGetDRIn.getMaxRetrieveCount();
        long fetchLimitCount = 0;
        long cassetteListInq = StandardProperties.OM_MAX_SIZE_CARRIER_LIST_INQ.getLongValue();
        if (maxRetrieveCount <= 0 || maxRetrieveCount > cassetteListInq) {
            fetchLimitCount = cassetteListInq;
        } else {
            fetchLimitCount = maxRetrieveCount;
        }
        String sql = this.getCarrierListInqSearchCondition(objCassetteListGetDRIn, firstCondition);
        log.info("sql : {}", sql);
        List<CimCassetteDO> queriedCasts = cimJpaRepository.query(sql, CimCassetteDO.class);
        if (CimArrayUtils.isEmpty(queriedCasts)) {
            return null;
        }
        //【step5】get durable hold record
        log.debug("【step5】get durable hold record");
        Map<String, CimCassetteDO> cimCassetteDOMap = new HashMap<>();
        List<Infos.FoundCassette> foundCassetteList = new ArrayList<>();
        for (CimCassetteDO cassette : queriedCasts) {
            Infos.FoundCassette foundCassette = new Infos.FoundCassette();
            /*******************************/
            /*  Get cassette sorter job    */
            /*******************************/
            com.fa.cim.sorter.Params.ObjSorterJobListGetDRIn objSorterJobListGetDRIn = new com.fa.cim.sorter.Params.ObjSorterJobListGetDRIn();
            objSorterJobListGetDRIn.setCarrierID(new ObjectIdentifier(cassette.getCassetteID(), cassette.getId()));
            foundCassette.setRelationFoupFlag(cassette.getRelationFoupFlag()==null?false:cassette.getRelationFoupFlag());
            List<Info.SortJobListAttributes> sortJobListAttributesList = null;
            try {
                sortJobListAttributesList = sorterNewMethod.sorterJobListGetDR(objCommon, objSorterJobListGetDRIn);
            } catch (ServiceException e) {
                break;
            }
            int sorterLen = CimArrayUtils.getSize(sortJobListAttributesList);
            if (CimBooleanUtils.isTrue(objCassetteListGetDRIn.isSorterJobCreationCheckFlag())) {
                /******************************************************/
                /*  If this cassette has Sorter Job, then continue.   */
                /*  If this cassette has control job, then continue.  */
                /******************************************************/
                if (sorterLen > 0 || !CimStringUtils.isEmpty(cassette.getControlJobID())) {
                    log.info("{} this cassette is reserved for SLM operation!", cassette.getControlJobID());
                    continue;
                }
                if (!CimStringUtils.isEmpty(cassette.getSlmReservedEquipmentID()) || !CimStringUtils.isEmpty(cassette.getSlmReservedEquipmentObj())) {
                    log.info("{}  this cassette is reserved for SLM operation!", cassette.getSlmReservedEquipmentID());
                    continue;
                }

            }
            /***************************************************************/
            /*  Set Sorter Job existence flag if cassette has Sorter Job.  */
            /***************************************************************/
            foundCassette.setSorterJobExistFlag(sorterLen > 0);
            foundCassette.setCassetteID(new ObjectIdentifier(cassette.getCassetteID(), cassette.getId()));
            cimCassetteDOMap.put(cassette.getCassetteID(), cassette);
            if (objCassetteListGetDRIn.getNeedTransferState() != null && !objCassetteListGetDRIn.getNeedTransferState()
                    && CimStringUtils.equals(cassette.getTransferState(), BizConstant.SP_TRANSSTATE_EQUIPMENTIN)) {
                continue;
            }
            foundCassetteList.add(foundCassette);
            if (foundCassetteList.size() >= fetchLimitCount) {
                log.info("Reach to fetchLimitCount. break!");
                break;
            }
        }
        Page page = CimPageUtils.convertListToPage(foundCassetteList, objCassetteListGetDRIn.getSearchCondition().getPage(), objCassetteListGetDRIn.getSearchCondition().getSize());
        List<Infos.FoundCassette> pagedFoundCassetteList = page.getContent();
        Map<String, Boolean> availableForDurableFlagList = new HashMap<>();
        for (Infos.FoundCassette foundCassette : pagedFoundCassetteList) {
            CimCassetteDO cassette = cimCassetteDOMap.get(foundCassette.getCassetteID().getValue());
            List<Infos.HashedInfo> hashedInfoList = new ArrayList<>();
            //--------------------------------------
            // Set InPostProcessFlag of Cassette
            //--------------------------------------
            foundCassette.setInPostProcessFlagOfCassette(cassette.getPostProcessingFlag());
            foundCassette.setDescription(cassette.getDescription());
            foundCassette.setCassetteCategory(cassette.getCassetteCategory());
            long cassetteUsedCapacity = null == cassette.getCastUsedCapacity() ? 0L : cassette.getCastUsedCapacity();
            foundCassette.setEmptyFlag(cassetteUsedCapacity == 0);
            foundCassette.setCassetteStatus(cassette.getDurableState());
            foundCassette.setTransferStatus(cassette.getTransferState());
            String zoneTypeNeedFlg = StandardProperties.OM_CARRIER_LIST_NEED_ZONE_TYPE.getValue();
            if (CimStringUtils.equals(zoneTypeNeedFlg, "1") && CimStringUtils.equals(objCommon.getTransactionID(), TransactionIDEnum.CASSETTE_LIST_INQ.getValue())) {
                Outputs.CassetteZoneTypeGetOut cassetteZoneTypeGetOut = null;
                try {
                    cassetteZoneTypeGetOut = this.cassetteZoneTypeGet(objCommon, foundCassette.getCassetteID());
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
                    CimDurableSubState aDurableSubState = baseCoreFactory.getBOByIdentifier(CimDurableSubState.class, cassette.getDurableSubStateID());
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
                String sql1 = "SELECT START_BANK_ID, START_BANK_RKEY FROM OMPRP WHERE PRP_ID = ?1 AND PRP_LEVEL = ?2";
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
            log.debug("【step5】get durable hold record");
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
            foundCassetteList.add(foundCassette);
        }
        return page;
    }

    private String getCarrierListInqSearchCondition(Inputs.ObjCassetteListGetDRIn objCassetteListGetDRIn, AtomicBoolean firstCondition) {
        String sql = "SELECT * FROM OMCARRIER ";
        if (!CimStringUtils.isEmpty(objCassetteListGetDRIn.getCassetteCategory())) {
            firstCondition.set(false);
            sql += " WHERE ";

            sql += String.format(" CARRIER_CATEGORY = '%s'", objCassetteListGetDRIn.getCassetteCategory());
        }
        if (objCassetteListGetDRIn.isEmptyFlag()) {
            if (firstCondition.get()) {
                firstCondition.set(false);
                sql += " WHERE ";
            } else {
                sql += " AND ";
            }
            sql += " CARRIER_USED_CAP = 0 ";
        }
        if (!ObjectIdentifier.isEmptyWithValue(objCassetteListGetDRIn.getStockerID())) {
            if (firstCondition.get()) {
                firstCondition.set(false);
                sql += " WHERE ";
            } else {
                sql += " AND ";
            }
            sql += String.format(" EQP_ID = '%s' ", objCassetteListGetDRIn.getStockerID().getValue());
        }
        if (CimStringUtils.isNotEmpty(objCassetteListGetDRIn.getCassetteTye())) {
            if (firstCondition.get()) {
                firstCondition.set(false);
                sql += " WHERE ";
            } else {
                sql += " AND ";
            }
            sql += String.format(" CARRIER_TYPE = '%s' ", objCassetteListGetDRIn.getCassetteTye());
        }
        if (!ObjectIdentifier.isEmptyWithValue(objCassetteListGetDRIn.getCassetteID())) {
            if (firstCondition.get()) {
                firstCondition.set(false);
                sql += " WHERE ";
            } else {
                sql += " AND ";
            }
            sql += String.format(" CARRIER_ID LIKE '%s' ", objCassetteListGetDRIn.getCassetteID().getValue());
        }
        if (!CimStringUtils.isEmpty(objCassetteListGetDRIn.getCassetteStatus())) {
            if (firstCondition.get()) {
                firstCondition.set(false);
                sql += " WHERE ";
            } else {
                sql += " AND ";
            }
            sql += String.format(" CARRIER_STATE = '%s' ", objCassetteListGetDRIn.getCassetteStatus());
        }
        if (!CimStringUtils.isEmpty(objCassetteListGetDRIn.getUsageType())) {
            if (firstCondition.get()) {
                firstCondition.set(false);
                sql += " WHERE ";
            } else {
                sql += " AND ";
            }
            sql += String.format(" PRODUCT_USAGE = '%s' ", objCassetteListGetDRIn.getUsageType());
        }
        if (!CimStringUtils.isEmpty(objCassetteListGetDRIn.getInterFabTransferState())) {
            if (firstCondition.get()) {
                firstCondition.set(false);
                sql += " WHERE ";
            } else {
                sql += " AND ";
            }
            sql += String.format(" IFB_XFER_STATE = '%s' ", objCassetteListGetDRIn.getInterFabTransferState());
        }
        if (!ObjectIdentifier.isEmptyWithValue(objCassetteListGetDRIn.getBankID())) {
            if (firstCondition.get()) {
                firstCondition.set(false);
                sql += " WHERE ";
            } else {
                sql += " AND ";
            }
            sql += String.format(" BANK_ID = '%s' ", objCassetteListGetDRIn.getBankID().getValue());
        }
        if (!ObjectIdentifier.isEmpty(objCassetteListGetDRIn.getDurablesSubStatus())) {
            if (firstCondition.get()) {
                firstCondition.set(false);
                sql += " WHERE ";
            } else {
                sql += " AND ";
            }
            sql += String.format(" CARRIER_SUB_STATE_ID = '%s' ", objCassetteListGetDRIn.getDurablesSubStatus().getValue());
        }
        if (!CimStringUtils.isEmpty(objCassetteListGetDRIn.getFlowStatus())) {
            boolean durableStatusFlag = true;
            StringBuffer tempBuffer = new StringBuffer();
            if (CIMStateConst.CIM_DURABLE_SCRAPPED.equals(objCassetteListGetDRIn.getFlowStatus())) {
                tempBuffer.append(String.format(" MAIN_PROCESS_ID <> '' AND CARRIER_STATE = '%s' ", objCassetteListGetDRIn.getFlowStatus()));
            } else if (CIMStateConst.CIM_LOT_HOLD_STATE_ONHOLD.equals(objCassetteListGetDRIn.getFlowStatus())) {
                tempBuffer.append(String.format(" MAIN_PROCESS_ID <> '' AND DRBL_HOLD_STATE = '%s' ", objCassetteListGetDRIn.getFlowStatus()));
            } else if (CIMStateConst.CIM_LOT_FINISHED_STATE_COMPLETED.equals(objCassetteListGetDRIn.getFlowStatus())) {
                tempBuffer.append(String.format(" MAIN_PROCESS_ID <> '' AND DRBL_FINISHED_STATE = '%s' ", objCassetteListGetDRIn.getFlowStatus()));
            } else if (CIMStateConst.CIM_LOT_FINISHED_STATE_WAITING.equals(objCassetteListGetDRIn.getFlowStatus())
                    || CIMStateConst.SP_LOT_PROCESS_STATE_PROCESSING.equals(objCassetteListGetDRIn.getFlowStatus())) {
                tempBuffer.append(String.format(" MAIN_PROCESS_ID <> '' AND DRBL_PROCESS_STATE = '%s' ", objCassetteListGetDRIn.getFlowStatus()));
            } else {
                durableStatusFlag = false;
            }
            if (durableStatusFlag) {
                if (firstCondition.get()) {
                    firstCondition.set(false);
                    sql += " WHERE ";
                } else {
                    sql += " AND ";
                }
                sql += tempBuffer.toString();
                if (CIMStateConst.CIM_LOT_HOLD_STATE_ONHOLD.equals(objCassetteListGetDRIn.getFlowStatus())) {
                    sql += String.format(" AND CARRIER_STATE <> '%s' ", CIMStateConst.CIM_DURABLE_SCRAPPED);
                } else if (CIMStateConst.CIM_LOT_FINISHED_STATE_COMPLETED.equals(objCassetteListGetDRIn.getFlowStatus())) {
                    sql += String.format("  AND CARRIER_STATE <> '%s' ", CIMStateConst.CIM_DURABLE_SCRAPPED);
                    sql += String.format("  AND DRBL_HOLD_STATE <> '%s' ", CIMStateConst.CIM_PRODUCT_HOLD_STATE_ONHOLD);
                } else if (CIMStateConst.CIM_LOT_PROCESS_STATE_WAITING.equals(objCassetteListGetDRIn.getFlowStatus())
                        || CIMStateConst.SP_LOT_PROCESS_STATE_PROCESSING.equals(objCassetteListGetDRIn.getFlowStatus())) {
                    sql += String.format("  AND CARRIER_STATE <> '%s' ", CIMStateConst.CIM_DURABLE_SCRAPPED);
                    sql += String.format("  AND DRBL_HOLD_STATE <> '%s' ", CIMStateConst.CIM_PRODUCT_HOLD_STATE_ONHOLD);
                    sql += String.format(" AND DRBL_FINISHED_STATE <> '%s' ", CIMStateConst.CIM_LOT_FINISHED_STATE_COMPLETED);
                }
            }
        }
        return sql;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param strObjCommonIn -
     * @param cassetteID     -
     * @return com.fa.cim.dto.RetCode<Infos.CassetteAssignedMahineGetDR>
     * @author Ho
     * @since 2019/1/9 15:55:52
     */
    @Override
    public Infos.CassetteAssignedMahineGetDR cassetteAssignedMahineGetDR(Infos.ObjCommon strObjCommonIn, ObjectIdentifier cassetteID) {
        Infos.CassetteAssignedMahineGetDR out = new Infos.CassetteAssignedMahineGetDR();
        CimCassetteDO example = new CimCassetteDO();
        example.setCassetteID(ObjectIdentifier.fetchValue(cassetteID));
        CimCassetteDO cimCassetteDO = cimJpaRepository.findOne(Example.of(example)).orElse(null);
        Validations.check(null == cimCassetteDO, new OmCode(retCodeConfig.getNotFoundCassette(), cassetteID.getValue()));

        out.setMachineID(ObjectIdentifier.build(cimCassetteDO.getEquipmentID(), cimCassetteDO.getEquipmentObj()));
        if (ObjectIdentifier.isNotEmptyWithValue(out.getMachineID())) {
            Boolean retCode = equipmentMethod.equipmentMachineTypeCheckDR(strObjCommonIn, out.getMachineID());
            out.setEquipmentFlag(retCode);
        }
        return out;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param strObjCommonIn   -
     * @param equipmentID      -
     * @param strStartCassette -
     * @param operation        -
     * @author Ho
     * @since 2018/10/31 15:39:20
     */
    @Override
    public void cassetteCheckConditionForOperationForInternalBuffer(Infos.ObjCommon strObjCommonIn, ObjectIdentifier equipmentID, List<Infos.StartCassette> strStartCassette, String operation) {

        int searchCondition = 0;
        String searchCondition_var = StandardProperties.OM_CONSTRAINT_CHK_WITH_SEARCH.getValue();
        if (!CimStringUtils.isEmpty(searchCondition_var)) {
            searchCondition = Integer.parseInt(searchCondition_var);
        }
        /*-------------------------------------------------------------------------------------*/
        /*                                                                                     */
        /*   Check Condition of controlJobID, multiLotType, transferState, transferReserved,   */
        /*   dispatchState, and cassetteState for all cassettes                                */
        /*   dispatchState, cassetteState, and loadingSequenceNumber for all cassettes         */
        /*                                                                                     */
        /*-------------------------------------------------------------------------------------*/

        /*--------------------------*/
        /*   Get Equipment Object   */
        /*--------------------------*/
        CimMachine aMachine = baseCoreFactory.getBO(CimMachine.class, equipmentID);
        /*-------------------------------------------*/
        /*   Get Equipment's MultiRecipeCapability   */
        /*-------------------------------------------*/
        String multiRecipeCapability = aMachine.getMultipleRecipeCapability();

        /*----------------------------------*/
        /*   Get Eqp Internal Buffer Info   */
        /*----------------------------------*/
        // step1 - equipment_internalBufferInfo_Get
        List<Infos.EqpInternalBufferInfo> strEquipmentInternalBufferInfoGetOut = equipmentMethod.equipmentInternalBufferInfoGet(strObjCommonIn, equipmentID);
        /* **************************************/
        /*     Check Cassette Condition        */
        /* **************************************/
        ObjectIdentifier saveControlJobID = null;
        int cjCastCnt = 0;
        int lenCassette = CimArrayUtils.getSize(strStartCassette);
        for (int i = 0; i < lenCassette; i++) {
            Infos.StartCassette startCassette = strStartCassette.get(i);
            if (CimStringUtils.equals(operation, BizConstant.SP_OPERATION_NPWCARRIERXFER)
                    || CimStringUtils.equals(operation, BizConstant.SP_OPERATION_STARTRESERVATION)
                    || CimStringUtils.equals(operation, BizConstant.SP_OPERATION_OPESTART)) {
                Infos.EquipmentLoadPortAttribute equipmentPortAttribute = new Infos.EquipmentLoadPortAttribute();
                List<Infos.CassetteLoadPort> strCassetteLoadPortSeq = new ArrayList<>();
                Infos.CassetteLoadPort strCassetteLoadPort = new Infos.CassetteLoadPort();
                strCassetteLoadPort.setPortID(startCassette.getLoadPortID());
                strCassetteLoadPort.setCassetteID(startCassette.getCassetteID());
                strCassetteLoadPortSeq.add(strCassetteLoadPort);
                equipmentPortAttribute.setEquipmentID(equipmentID);
                equipmentPortAttribute.setCassetteLoadPortList(strCassetteLoadPortSeq);

                List<ObjectIdentifier> cassetteIDList = new ArrayList<>();
                cassetteIDList.add(startCassette.getCassetteID());
                Inputs.ObjWaferSorterJobCheckForOperation objWaferSorterJobCheckForOperation = new Inputs.ObjWaferSorterJobCheckForOperation();
                objWaferSorterJobCheckForOperation.setEquipmentLoadPortAttribute(equipmentPortAttribute);
                objWaferSorterJobCheckForOperation.setCassetteIDList(cassetteIDList);
                objWaferSorterJobCheckForOperation.setOperation(operation);

                // step2 - waferSorter_sorterJob_CheckForOperation
                waferMethod.waferSorterSorterJobCheckForOperation(strObjCommonIn, objWaferSorterJobCheckForOperation);
            }
            /*-------------------------*/
            /*   Get Cassette Object   */
            /*-------------------------*/
            CimCassette aCassette = baseCoreFactory.getBO(CimCassette.class, startCassette.getCassetteID());
            /*--------------------------------*/
            /*   Get and Check ControlJobID   */
            /*--------------------------------*/
            com.fa.cim.newcore.bo.product.CimControlJob aControlJob = aCassette.getControlJob();
            if (CimStringUtils.equals(operation, BizConstant.SP_OPERATION_OPESTART)) {
                if (i == 0) {
                    saveControlJobID = (null != aControlJob) ? new ObjectIdentifier(aControlJob.getIdentifier(), aControlJob.getPrimaryKey()) : null;
                } else {
                    ObjectIdentifier castControlJobID = (null != aControlJob) ? new ObjectIdentifier(aControlJob.getIdentifier(), aControlJob.getPrimaryKey()) : null;
                    if (!ObjectIdentifier.equalsWithValue(castControlJobID, saveControlJobID)) {
                        throw new ServiceException(retCodeConfig.getCassetteControlJobMix());
                    }
                }
            } else {
                if (aControlJob != null) {
                    throw new ServiceException(retCodeConfig.getCassetteControlJobFilled());
                }
            }

            /*---------------------------------*/
            /*   Get Cassette's MultiLotType   */
            /*---------------------------------*/
            if (CimStringUtils.equals(operation, BizConstant.SP_OPERATION_NPWCARRIERXFER)) {
                //-----------------------------------------------//
                //   For NPWCarrierXfer, MultiRecipeCapability   //
                //   VS MultiLotType is not required             //
                //-----------------------------------------------//
                //result.setReturnCode(retCodeConfig.getSucc());
            } else {
                String multiLotType = aCassette.getMultiLotType();
                /*-------------------------------------------------*/
                /*   Check MultiRecipeCapability VS MultiLotType   */
                /*-------------------------------------------------*/
                if (CimStringUtils.equals(startCassette.getLoadPurposeType(), BizConstant.SP_LOADPURPOSETYPE_EMPTYCASSETTE)) {
                    //result.setReturnCode(retCodeConfig.getSucc());
                } else if (CimStringUtils.equals(multiRecipeCapability, BizConstant.SP_EQP_MULTIRECIPECAPABILITY_MULTIPLERECIPE)) {
                    //result.setReturnCode(retCodeConfig.getSucc());
                } else if (CimStringUtils.equals(multiRecipeCapability, BizConstant.SP_EQP_MULTIRECIPECAPABILITY_SINGLERECIPE)) {
                    //result.setReturnCode(retCodeConfig.getSucc());
                } else if (CimStringUtils.equals(multiRecipeCapability, BizConstant.SP_EQP_MULTIRECIPECAPABILITY_BATCH)) {
                    if (!CimStringUtils.equals(multiLotType, BizConstant.SP_CAS_MULTILOTTYPE_SINGLELOTSINGLERECIPE)
                            && !CimStringUtils.equals(multiLotType, BizConstant.SP_CAS_MULTILOTTYPE_MULTILOTSINGLERECIPE)) {
                        throw new ServiceException(retCodeConfig.getCassetteEquipmentConditionError());
                    }
                }
            }
            /*--------------------------------------*/
            /*   Check Cassette's Transfer Status   */
            /*--------------------------------------*/

            /*-----------------------*/
            /*   Get TransferState   */
            /*-----------------------*/
            String transferState = aCassette.getTransportState();

            /*------------------------------*/
            /*   Get TransferReserveState   */
            /*------------------------------*/
            Boolean transferReserved = aCassette.isReserved();
            if (CimStringUtils.equals(operation, BizConstant.SP_OPERATION_OPESTART)) {
                if (!CimStringUtils.equals(transferState, BizConstant.SP_TRANSSTATE_EQUIPMENTIN)) {
                    // result.setReturnCode(retCodeConfig.getSucc());
                    throw new ServiceException(new OmCode(retCodeConfig.getInvalidCassetteTransferState(), transferState, startCassette.getCassetteID().getValue()));
                }
            } else if (CimStringUtils.equals(operation, BizConstant.SP_OPERATION_FLOWBATCHING)) {
                if ((CimStringUtils.equals(transferState, BizConstant.SP_TRANSSTATE_STATIONIN)
                        || CimStringUtils.equals(transferState, BizConstant.SP_TRANSSTATE_BAYIN)
                        || CimStringUtils.equals(transferState, BizConstant.SP_TRANSSTATE_MANUALIN))
                        && (!transferReserved)) {
                    //result.setReturnCode(retCodeConfig.getSucc());
                } else {
                    throw new ServiceException(new OmCode(retCodeConfig.getInvalidCassetteTransferState(), transferState, aCassette.getIdentifier()));
                }
            } else if (CimStringUtils.equals(operation, BizConstant.SP_OPERATION_STARTRESERVATION)
                    || CimStringUtils.equals(operation, BizConstant.SP_OPERATION_CASSETTEDELIVERY)
                    || CimStringUtils.equals(operation, BizConstant.SP_OPERATION_NPWCARRIERXFER)) {
                // step3 - portResource_currentOperationMode_Get
                Outputs.ObjPortResourceCurrentOperationModeGetOut strPortResourceCurrentOperationModeGetOut = portMethod.portResourceCurrentOperationModeGet(strObjCommonIn, equipmentID, startCassette.getLoadPortID());

                Infos.EqpPortStatus strOrgEqpPortStatus = null;
                CimMachine aOrgMachine = null;

                if (CimStringUtils.equals(transferState, BizConstant.SP_TRANSSTATE_EQUIPMENTIN)) {
                    /*-------------------------------*/
                    /*   Get Originator Eqp's Info   */
                    /*-------------------------------*/

                    /*--------------------------------*/
                    /*   Get Originator EquipmentID   */
                    /*--------------------------------*/
                    ObjectIdentifier orgEquipmentID = null;

                    Machine aMachine1 = aCassette.currentAssignedMachine();
                    if (aMachine1 != null) {
                        boolean isStorageBool = aMachine1.isStorageMachine();
                        if (!isStorageBool) {
                            aOrgMachine = (CimMachine) aMachine1;
                            orgEquipmentID = new ObjectIdentifier(aOrgMachine.getIdentifier(), aOrgMachine.getPrimaryKey());
                        }
                    }

                    if (aOrgMachine == null) {
                        throw new ServiceException(retCodeConfig.getNotFoundEqp());
                    }
                    /*---------------------------------*/
                    /*   Get Cassette Info in OrgEqp   */
                    /*---------------------------------*/
                    Infos.EqpPortInfo equipmentPortInfo = null;
                    String equipmentCategory = aOrgMachine.getCategory();
                    if (CimStringUtils.equals(equipmentCategory, BizConstant.SP_MC_CATEGORY_INTERNALBUFFER)) {
                        // step4 - equipment_portInfoForInternalBuffer_GetDR
                        equipmentPortInfo = equipmentMethod.equipmentPortInfoForInternalBufferGetDR(strObjCommonIn, orgEquipmentID);

                        // step5 - equipment_internalBufferInfo_Get
                        List<Infos.EqpInternalBufferInfo> equipmentInternalBufferInfo = equipmentMethod.equipmentInternalBufferInfoGet(strObjCommonIn, orgEquipmentID);
                        ;
                        int lenBufCate = CimArrayUtils.getSize(equipmentInternalBufferInfo);

                        for (int iBuf = 0; iBuf < lenBufCate; iBuf++) {
                            List<Infos.ShelfInBuffer> strShelfInBuffer = equipmentInternalBufferInfo.get(iBuf).getShelfInBufferList();
                            int lenShelf = CimArrayUtils.getSize(strShelfInBuffer);

                            for (int jShelf = 0; jShelf < lenShelf; jShelf++) {
                                if (CimObjectUtils.equals(startCassette.getCassetteID(), strShelfInBuffer.get(jShelf).getReservedCarrierID())
                                        && !ObjectIdentifier.isEmpty(strShelfInBuffer.get(jShelf).getReservedLoadPortID())) {
                                    throw new ServiceException(new OmCode(retCodeConfig.getInvalidCassetteTransferState(), transferState, startCassette.getCassetteID().getValue()));
                                }
                            }
                        }
                    } else {
                        // step6 - equipment_portInfo_Get
                        equipmentPortInfo = equipmentMethod.equipmentPortInfoGet(strObjCommonIn, orgEquipmentID);
                    }
                    /*--------------------------------------*/
                    /*   Find Assigned Port's portGroupID   */
                    /*--------------------------------------*/
                    boolean bFound = false;
                    List<Infos.EqpPortStatus> strEqpPortStatus = equipmentPortInfo.getEqpPortStatuses();
                    int lenEqpPort = CimArrayUtils.getSize(strEqpPortStatus);
                    for (int j = 0; j < lenEqpPort; j++) {
                        if (ObjectIdentifier.equalsWithValue(startCassette.getCassetteID(),
                                strEqpPortStatus.get(j).getLoadedCassetteID())) {
                            strOrgEqpPortStatus = strEqpPortStatus.get(j);
                            bFound = true;
                            break;
                        }
                    }

                    if (!bFound) {
                        throw new ServiceException(new OmCode(retCodeConfig.getInvalidCassetteTransferState(), transferState, startCassette.getCassetteID().getValue()));
                    }
                }

                if (CimStringUtils.equals(strPortResourceCurrentOperationModeGetOut.getOperationMode().getAccessMode(), BizConstant.SP_EQP_ACCESSMODE_MANUAL)) {
                    /*-------------------------------------------------------------------------*/
                    /*   When TransferStatus is EI, AccessMode makes it an error with Manual   */
                    /*-------------------------------------------------------------------------*/
                    if (CimStringUtils.equals(transferState, BizConstant.SP_TRANSSTATE_EQUIPMENTIN)) {
                        /*---------------------------------------------------------------------------*/
                        /*   Permit Carrier which a person can deliver in StartLotReserve.           */
                        /*   As for the condition, OperationMode is "***-1" and XferState is "EI".   */
                        /*                                                                           */
                        /*   And, Only in the case of InternalBufferEQP!!                            */
                        /*   Carrier must be on Port.                                                */
                        /*   Because, carrier may be in Shelf.                                       */
                        /*---------------------------------------------------------------------------*/
                        if (!CimStringUtils.equals(strOrgEqpPortStatus.getAccessMode(), BizConstant.SP_EQP_ACCESSMODE_MANUAL)
                                || ObjectIdentifier.isEmptyWithValue(strOrgEqpPortStatus.getLoadedCassetteID())) {
                            throw new ServiceException(new OmCode(retCodeConfig.getInvalidCassetteTransferState(), transferState, startCassette.getCassetteID().getValue()));
                        }
                    }
                } else {
                    boolean bReRouteFlg = false;

                    String reRouteXferFlag = StandardProperties.OM_XFER_REROUTE_FLAG.getValue();
                    if (CimStringUtils.equals(reRouteXferFlag, "1")
                            && (ObjectIdentifier.equalsWithValue(strPortResourceCurrentOperationModeGetOut.getOperationMode().getOperationMode(), BizConstant.SP_EQP_PORT_OPERATIONMODE_AUTO_3))
                            && (CimStringUtils.equals(transferState, BizConstant.SP_TRANSSTATE_STATIONIN)
                            || CimStringUtils.equals(transferState, BizConstant.SP_TRANSSTATE_BAYIN)
                            || CimStringUtils.equals(transferState, BizConstant.SP_TRANSSTATE_MANUALIN)
                            || CimStringUtils.equals(transferState, BizConstant.SP_TRANSSTATE_BAYOUT))
                            && (!transferReserved)) {
                        //result.setReturnCode(retCodeConfig.getSucc());
                    } else if ((CimStringUtils.equals(transferState, BizConstant.SP_TRANSSTATE_STATIONIN)
                            || CimStringUtils.equals(transferState, BizConstant.SP_TRANSSTATE_BAYIN)
                            || CimStringUtils.equals(transferState, BizConstant.SP_TRANSSTATE_MANUALIN))
                            && (!transferReserved)) {
                        //result.setReturnCode(retCodeConfig.getSucc());
                    } else if (CimStringUtils.equals(transferState, BizConstant.SP_TRANSSTATE_EQUIPMENTIN) && !transferReserved) {
                        /*--------------------------------------------------------------*/
                        /*   Make it an error when Carrier of FromEQP is [EI].          */
                        /*   Because, InternalBufferEQP can't do transfer EQP to EQP.   */
                        /*   Therefore, All Logics here are unnecessary.                */
                        /*--------------------------------------------------------------*/
                        throw new ServiceException(new OmCode(retCodeConfig.getInvalidCassetteTransferState(), transferState, aCassette.getIdentifier()));
                    } else {
                        throw new ServiceException(new OmCode(retCodeConfig.getInvalidCassetteTransferState(), transferState, aCassette.getIdentifier()));
                    }
                }
            }
            /*----------------------------------------------*/
            /*   Get and Check Cassette's Dispatch Status   */
            /*----------------------------------------------*/

            /*------------------------------------*/
            /*   Get Cassette's Dispatch Status   */
            /*------------------------------------*/
            if (CimStringUtils.equals(operation, BizConstant.SP_OPERATION_STARTRESERVATION)
                    || CimStringUtils.equals(operation, BizConstant.SP_OPERATION_FLOWBATCHING)
                    || CimStringUtils.equals(operation, BizConstant.SP_OPERATION_CASSETTEDELIVERY)) {
                boolean dispatchReserveFlag = aCassette.isReserved();
                if (dispatchReserveFlag) {
                    throw new ServiceException(retCodeConfig.getAlreadyDispatchReservedCassette());
                }
            }
            /*-------------------------------------*/
            /*   Get and Check Cassette's Status   */
            /*-------------------------------------*/
            String cassetteState = aCassette.getDurableState();

            if (CimStringUtils.equals(operation, BizConstant.SP_OPERATION_OPESTART) || CimStringUtils.equals(operation, BizConstant.SP_OPERATION_STARTRESERVATION)) {
                // SPDynamicTableS< char*, char*, char*, char*, stringSequence, stringSequence > subLotTypeList;
                Map<String, String> subLotTypeList = new HashMap<>();

                if (CimStringUtils.equals(startCassette.getLoadPurposeType(), BizConstant.SP_LOADPURPOSETYPE_EMPTYCASSETTE)) {
                    int startCastLen = CimArrayUtils.getSize(strStartCassette);
                    for (int startCastIndex = 0; startCastIndex < startCastLen; startCastIndex++) {
                        List<Infos.LotInCassette> strLotInCassette = strStartCassette.get(startCastIndex).getLotInCassetteList();
                        int lotInCastLen = CimArrayUtils.getSize(strLotInCassette);
                        for (int lotInCastIndex = 0; lotInCastIndex < lotInCastLen; lotInCastIndex++) {
                            Infos.LotInCassette lotInCassette = strLotInCassette.get(lotInCastIndex);
                            if (lotInCassette.getMoveInFlag()) {
                                String subLotType = lotInCassette.getSubLotType();
                                if (!subLotTypeList.containsKey(subLotType)) {
                                    subLotTypeList.put(subLotType, subLotType);
                                }
                            }
                        }
                    }
                } else {
                    List<Infos.LotInCassette> strLotInCassette = startCassette.getLotInCassetteList();
                    for (int lotInCastIndex = 0, lotInCastLen = CimArrayUtils.getSize(strLotInCassette); lotInCastIndex < lotInCastLen; lotInCastIndex++) {
                        Infos.LotInCassette lotInCassette = strLotInCassette.get(lotInCastIndex);
                        if (lotInCassette.getMoveInFlag()) {
                            String subLotType = lotInCassette.getSubLotType();
                            if (!subLotTypeList.containsKey(subLotType)) {
                                subLotTypeList.put(subLotType, subLotType);
                            }
                        }
                    }
                }
                List<String> subLotTypeListSeq = new ArrayList<>(subLotTypeList.values());
                Boolean availableFlag = aCassette.isLotProcessAvailable(subLotTypeListSeq);
                if (!availableFlag) {
                    throw new ServiceException(retCodeConfig.getDurableNotAvailableStateForLotProcess());
                }
            } else {
                if (!CimStringUtils.equals(cassetteState, CIMStateConst.CIM_DURABLE_AVAILABLE)
                        && !CimStringUtils.equals(cassetteState, CIMStateConst.CIM_DURABLE_INUSE)) {
                    throw new ServiceException(new OmCode(retCodeConfig.getInvalidCassetteState(), cassetteState, aCassette.getIdentifier()));
                }
            }

            /*------------------------------------------------------*/
            /*   Check Exist LoadPort reserved and LoadedCassette   */
            /*------------------------------------------------------*/
            if (CimStringUtils.equals(operation, BizConstant.SP_OPERATION_STARTRESERVATION)
                    || CimStringUtils.equals(operation, BizConstant.SP_OPERATION_CASSETTEDELIVERY)) {
                //List<Infos.EqpInternalBufferInfo> equipmentInternalBufferInfo = strEquipmentInternalBufferInfoGetOut;
                int lenBufCategory = CimArrayUtils.getSize(strEquipmentInternalBufferInfoGetOut);

                for (int j = 0; j < lenBufCategory; j++) {
                    List<Infos.ShelfInBuffer> strShelfInBuffer = strEquipmentInternalBufferInfoGetOut.get(j).getShelfInBufferList();
                    int nShelfLen = CimArrayUtils.getSize(strShelfInBuffer);
                    for (int k = 0; k < nShelfLen; k++) {
                        if (!ObjectIdentifier.isEmpty(strShelfInBuffer.get(k).getReservedCarrierID())
                                && ObjectIdentifier.equalsWithValue(startCassette.getCassetteID(), strShelfInBuffer.get(k).getReservedCarrierID())) {
                            throw new ServiceException(new OmCode(retCodeConfig.getInternalBufferAlreadyReserved(), equipmentID.getValue(), strShelfInBuffer.get(k).getReservedCarrierID().getValue()));
                        }
                        if (!ObjectIdentifier.isEmpty(strShelfInBuffer.get(k).getLoadedCarrierID())
                                && ObjectIdentifier.equalsWithValue(startCassette.getCassetteID(), strShelfInBuffer.get(k).getLoadedCarrierID())) {
                            throw new ServiceException(new OmCode(retCodeConfig.getCstAlreadyLoaded(), strShelfInBuffer.get(k).getLoadedCarrierID().getValue()));
                        }
                    }
                }
            }

            //-----------------------------------------------
            // Check Start Cassette And Start Lot Combination
            //-----------------------------------------------
            if ((CimStringUtils.equals(operation, BizConstant.SP_OPERATION_OPESTART))
                    || (CimStringUtils.equals(operation, BizConstant.SP_OPERATION_STARTRESERVATION))) {

                String sql = "select * from OMLOT c where c.LOT_ID in (select b.LOT_ID from OMCARRIER_LOT b where b.REFKEY in (select ID from OMCARRIER where CARRIER_ID = ?1) )";
                List<CimLotDO> aLotSequence = cimJpaRepository.query(sql, CimLotDO.class, aCassette.getIdentifier());

                int lotLen = CimArrayUtils.getSize(aLotSequence);
                List<Infos.LotInCassette> strLotInCassette = startCassette.getLotInCassetteList();
                int lotLenInCassette = CimArrayUtils.getSize(strLotInCassette);
                if (lotLen == lotLenInCassette) {
                    Boolean bLotMatch = false;
                    //--------------------------------
                    // All Lot in Start Cassette Loop
                    //--------------------------------
                    for (int jj = 0; jj < lotLen; jj++) {
                        bLotMatch = false;
                        String strTempLotID = null;

                        CimLotDO aLot = aLotSequence.get(jj);
                        if (aLot != null) {
                            strTempLotID = aLot.getLotID();
                            //---------------------------------
                            // Inpara Lot in StartCassette Loop
                            //---------------------------------
                            for (int kk = 0; kk < lotLenInCassette; kk++) {
                                if (ObjectIdentifier.equalsWithValue(strTempLotID, strLotInCassette.get(kk).getLotID())) {
                                    bLotMatch = true;
                                    break;
                                }
                            }
                            if (!bLotMatch) {
                                throw new ServiceException(retCodeConfig.getLotStartCassetteUnMatch());
                            }
                        } else {
                            throw new ServiceException(new OmCode(retCodeConfig.getLotCastUnmatch(), "", aCassette.getIdentifier()));
                        }
                    }
                    //--------------------------------
                    // Check Cassette & Lot combination
                    //  Inpara Lot --> Lot in Cassette
                    //--------------------------------
                    for (int jj = 0; jj < lotLenInCassette; jj++) {
                        bLotMatch = false;
                        String strTempLotID = null;
                        //---------------------------------
                        // All Lot of in Cassette Loop
                        //---------------------------------
                        for (int kk = 0; kk < lotLen; kk++) {
                            if (aLotSequence.get(kk) != null) {
                                strTempLotID = aLotSequence.get(kk).getLotID();
                                if (ObjectIdentifier.equalsWithValue(strTempLotID, strLotInCassette.get(jj).getLotID())) {
                                    bLotMatch = true;
                                    break;
                                }
                            } else {
                                throw new ServiceException(new OmCode(retCodeConfig.getLotCastUnmatch(), strTempLotID, aCassette.getIdentifier()));
                            }
                        }

                        if (!bLotMatch) {
                            throw new ServiceException(retCodeConfig.getLotStartCassetteUnMatch());
                        }
                    }
                } else {
                    throw new ServiceException(retCodeConfig.getLotStartCassetteUnMatch());
                }
            }
        }

        /**************************************************/
        /*     Check unload port reserved                 */
        /**************************************************/
        if (CimStringUtils.equals(operation, BizConstant.SP_OPERATION_STARTRESERVATION)
                || CimStringUtils.equals(operation, BizConstant.SP_OPERATION_NPWCARRIERXFER)
                || CimStringUtils.equals(operation, BizConstant.SP_OPERATION_CASSETTEDELIVERY)) {
            //List<Infos.EqpInternalBufferInfo> equipmentInternalBufferInfo = strEquipmentInternalBufferInfoGetOut;
            int lenBufCategory = CimArrayUtils.getSize(strEquipmentInternalBufferInfoGetOut);

            for (int i = 0; i < lenBufCategory; i++) {
                List<Infos.ShelfInBuffer> strShelfInBuffer = strEquipmentInternalBufferInfoGetOut.get(i).getShelfInBufferList();
                int nShelfLen = CimArrayUtils.getSize(strShelfInBuffer);

                for (int j = 0; j < nShelfLen; j++) {
                    for (int k = 0; k < lenCassette; k++) {
                        if (!ObjectIdentifier.isEmpty(strShelfInBuffer.get(j).getReservedUnloadPortID())
                                && ObjectIdentifier.equalsWithValue(strStartCassette.get(k).getLoadPortID(), strShelfInBuffer.get(j).getReservedUnloadPortID())) {
                            throw new ServiceException(retCodeConfig.getInternalBufferAlreadyReserved());
                        }
                    }
                }
            }
        }
        /******************************************************/
        /*                                                    */
        /*     Count of Cassette LoadPurposeType              */
        /*                                                    */
        /******************************************************/
        int emptyCassetteCount = 0;
        int fillerDummyLotCount = 0;
        int processLotCount = 0;
        int processMonitorLotCount = 0;
        int sideDummyLotCount = 0;
        int waitingMonitorLotCount = 0;

        for (int i = 0; i < lenCassette; i++) {
            if (CimStringUtils.equals(BizConstant.SP_LOADPURPOSETYPE_EMPTYCASSETTE, strStartCassette.get(i).getLoadPurposeType())) {
                emptyCassetteCount++;
            } else if (CimStringUtils.equals(BizConstant.SP_LOADPURPOSETYPE_FILLERDUMMY, strStartCassette.get(i).getLoadPurposeType())) {
                fillerDummyLotCount++;
            } else if (CimStringUtils.equals(BizConstant.SP_LOADPURPOSETYPE_PROCESSLOT, strStartCassette.get(i).getLoadPurposeType())) {
                processLotCount++;
            } else if (CimStringUtils.equals(BizConstant.SP_LOADPURPOSETYPE_PROCESSMONITORLOT, strStartCassette.get(i).getLoadPurposeType())) {
                processMonitorLotCount++;
            } else if (CimStringUtils.equals(BizConstant.SP_LOADPURPOSETYPE_SIDEDUMMYLOT, strStartCassette.get(i).getLoadPurposeType())) {
                sideDummyLotCount++;
            } else if (CimStringUtils.equals(BizConstant.SP_LOADPURPOSETYPE_WAITINGMONITORLOT, strStartCassette.get(i).getLoadPurposeType())) {
                waitingMonitorLotCount++;
            } else {
            }
            if (CimStringUtils.equals(operation, BizConstant.SP_OPERATION_NPWCARRIERXFER)) {
                if (0 != processLotCount || 0 != emptyCassetteCount || 0 != processMonitorLotCount) {
                    throw new ServiceException(new OmCode(retCodeConfig.getInvalidPurposeType(), strStartCassette.get(i).getCassetteID().getValue(), equipmentID.getValue()));
                }
            }
        }

        /******************************************************************************************/
        /*                                                                                        */
        /*     Check ProcessLot Count                                                             */
        /*                                                                                        */
        /******************************************************************************************/
        if (CimStringUtils.equals(operation, BizConstant.SP_OPERATION_CASSETTEDELIVERY)) {
            if (0 == processLotCount) {
                throw new ServiceException(retCodeConfig.getNotFoundLot());
            }
        }
        /******************************************************************************************/
        /*                                                                                        */
        /*     Check Internal Buffer Free Space VS StartCassette LoadPurposeType Total            */
        /*                                                                                        */
        /******************************************************************************************/
        if (CimStringUtils.equals(operation, BizConstant.SP_OPERATION_STARTRESERVATION)
                || CimStringUtils.equals(operation, BizConstant.SP_OPERATION_NPWCARRIERXFER)
                || CimStringUtils.equals(operation, BizConstant.SP_OPERATION_CASSETTEDELIVERY)) {
            /*------------------------------------*/
            /*   Get Internal Buffer Free Space   */
            /*------------------------------------*/
            // step7 - equipment_shelfSpaceForInternalBuffer_Get
            Infos.EquipmentShelfSpaceForInternalBufferGet strEquipment_shelfSpaceForInternalBuffer_Get_out = equipmentMethod.equipmentShelfSpaceForInternalBufferGet(strObjCommonIn, equipmentID, true, strEquipmentInternalBufferInfoGetOut);
            String shelfCategory = null;
            boolean bFreeShelfErr = false;

            if (emptyCassetteCount > strEquipment_shelfSpaceForInternalBuffer_Get_out.getEmptyCassetteSpace()) {
                shelfCategory = BizConstant.SP_LOADPURPOSETYPE_EMPTYCASSETTE;
                bFreeShelfErr = true;
            }
            if (fillerDummyLotCount > strEquipment_shelfSpaceForInternalBuffer_Get_out.getFillerDummyLotSpace()) {
                shelfCategory = BizConstant.SP_LOADPURPOSETYPE_FILLERDUMMY;
                bFreeShelfErr = true;
            }
            if (processLotCount > strEquipment_shelfSpaceForInternalBuffer_Get_out.getProcessLotSpace()) {
                shelfCategory = BizConstant.SP_LOADPURPOSETYPE_PROCESSLOT;
                bFreeShelfErr = true;
            }
            if (processMonitorLotCount > strEquipment_shelfSpaceForInternalBuffer_Get_out.getProcessMonitorLotSpace()) {
                shelfCategory = BizConstant.SP_LOADPURPOSETYPE_PROCESSMONITORLOT;
                bFreeShelfErr = true;
            }
            if (sideDummyLotCount > strEquipment_shelfSpaceForInternalBuffer_Get_out.getSideDummyLotSpace()) {
                shelfCategory = BizConstant.SP_LOADPURPOSETYPE_SIDEDUMMYLOT;
                bFreeShelfErr = true;
            }
            if (waitingMonitorLotCount > strEquipment_shelfSpaceForInternalBuffer_Get_out.getWaitingMonitorLotSpace()) {
                shelfCategory = BizConstant.SP_LOADPURPOSETYPE_WAITINGMONITORLOT;
                bFreeShelfErr = true;
            }
            if (bFreeShelfErr) {
                throw new ServiceException(new OmCode(retCodeConfig.getNotSpaceEqpSelf(), shelfCategory));
            }
        }

        /*---------------------------------------------------------*/
        /*   Check Cassette's ControlJobID vs Eqp's ControlJobID   */
        /*---------------------------------------------------------*/
        if (CimStringUtils.equals(operation, BizConstant.SP_OPERATION_OPESTART)) {
            // step8 - equipment_reservedControlJobID_Get
            List<Infos.StartReservedControlJobInfo> startReservedControlJobInfos = equipmentMethod.equipmentReservedControlJobIDGetDR(strObjCommonIn, equipmentID);
            ObjectIdentifier eqpControlJobID = null;
            List<ProductDTO.PosStartCassetteInfo> startCassetteInfo = null;
            if (!ObjectIdentifier.isEmptyWithValue(saveControlJobID)) {
                //List<Infos.StartReservedControlJobInfo> strStartReservedControlJobInfo = startReservedControlJobInfos;
                int rsvCJLen = CimArrayUtils.getSize(startReservedControlJobInfos);
                for (int nCJ = 0; nCJ < rsvCJLen; nCJ++) {
                    Infos.StartReservedControlJobInfo startReservedControlJobInfo = startReservedControlJobInfos.get(nCJ);
                    if (ObjectIdentifier.equalsWithValue(saveControlJobID, startReservedControlJobInfo.getControlJobID())) {
                        eqpControlJobID = startReservedControlJobInfo.getControlJobID();
                        CimControlJob aReserveControlJob = baseCoreFactory.getBO(CimControlJob.class, eqpControlJobID);
                        startCassetteInfo = aReserveControlJob.getStartCassetteInfo();
                        cjCastCnt = CimArrayUtils.getSize(startCassetteInfo);
                        break;
                    }
                }
            }
            if (!ObjectIdentifier.equalsWithValue(saveControlJobID, eqpControlJobID)) {
                throw new ServiceException(retCodeConfig.getCassettePortControlJobUnMatch());
            }

            if (!ObjectIdentifier.isEmptyWithValue(saveControlJobID)) {
                lenCassette = CimArrayUtils.getSize(strStartCassette);
                if (cjCastCnt != lenCassette) {
                    throw new ServiceException(retCodeConfig.getCastPortCtrljobCountUnmatch());
                }
            }

            if (null != startCassetteInfo) {
                int nCstLen = CimArrayUtils.getSize(strStartCassette);
                for (int nCst = 0; nCst < nCstLen; nCst++) {
                    List<Infos.LotInCassette> strLotInCassette = strStartCassette.get(nCst).getLotInCassetteList();
                    int nLotLen = CimArrayUtils.getSize(strLotInCassette);
                    for (int nLot = 0; nLot < nLotLen; nLot++) {
                        boolean bSameCondition = false;
                        int nCJCstLen = CimArrayUtils.getSize(startCassetteInfo);
                        for (int nCstCJ = 0; nCstCJ < nCJCstLen; nCstCJ++) {
                            List<ProductDTO.PosLotInCassetteInfo> lotInCassetteInfo = startCassetteInfo.get(nCstCJ).getLotInCassetteInfo();
                            int nCJLotLen = CimArrayUtils.getSize(lotInCassetteInfo);
                            for (int nLotCJ = 0; nLotCJ < nCJLotLen; nLotCJ++) {
                                if (CimObjectUtils.equals(strStartCassette.get(nCst).getLotInCassetteList().get(nLot).getLotID(), lotInCassetteInfo.get(nLotCJ).getLotID())
                                        && strLotInCassette.get(nLot).getMoveInFlag() == lotInCassetteInfo.get(nLotCJ).isOperationStartFlag()) {
                                    bSameCondition = true;
                                    break;
                                }
                            }
                            if (bSameCondition) {
                                break;
                            }
                        }
                        if (!bSameCondition) {
                            throw new ServiceException(retCodeConfig.getStartReserveControlJobOperationStateControlJobUnMatch());
                        }
                    }
                }
            }
        }

        /******************************************************************************************/
        /*                                                                                        */
        /*     Check Condition for maxBatchSize, minBatchSize, emptyCassetteCount                 */
        /*                                                                                        */
        /******************************************************************************************/
        if (CimStringUtils.equals(operation, BizConstant.SP_OPERATION_OPESTART)
                || CimStringUtils.equals(operation, BizConstant.SP_OPERATION_STARTRESERVATION)) {
            /*---------------------------------------------*/
            /*   Get Equipment's Process Batch Condition   */
            /*---------------------------------------------*/
            // step9 - equipment_processBatchCondition_Get
            Outputs.ObjEquipmentProcessBatchConditionGetOut strEquipmentProcessBatchConditionGetOut = equipmentMethod.equipmentProcessBatchConditionGet(strObjCommonIn, equipmentID);
            /*------------------------------------*/
            /*   for maxBatchSize, minBatchSize   */
            /*------------------------------------*/

            int processAndProcMonCount = processLotCount + processMonitorLotCount;
            long maxBatchSize = strEquipmentProcessBatchConditionGetOut.getMaxBatchSize();
            long minBatchSize = strEquipmentProcessBatchConditionGetOut.getMinBatchSize();
            if ((processAndProcMonCount >= minBatchSize)
                    && (processAndProcMonCount <= maxBatchSize)) {
                //result.setReturnCode(retCodeConfig.getSucc());
            } else {
                throw new ServiceException(new OmCode(retCodeConfig.getInvalidProcessBatchCount(),
                        String.valueOf(processAndProcMonCount), String.valueOf(maxBatchSize), String.valueOf(minBatchSize)));
            }

            boolean bMonitorCreationFlag = false;
            if (strEquipmentProcessBatchConditionGetOut.isMonitorCreationFlag()) {
                ObjectIdentifier logicalRecipeID = null;
                lenCassette = CimArrayUtils.getSize(strStartCassette);
                for (int i = 0; i < lenCassette; i++) {
                    List<Infos.LotInCassette> strLotInCassette = strStartCassette.get(i).getLotInCassetteList();
                    int lenLotInCassette = CimArrayUtils.getSize(strLotInCassette);
                    for (int j = 0; j < lenLotInCassette; j++) {
                        if (null == strLotInCassette.get(j).getStartRecipe()
                                || ObjectIdentifier.isEmpty(strLotInCassette.get(j).getStartRecipe().getLogicalRecipeID())) {
                            continue;
                        }
                        logicalRecipeID = strLotInCassette.get(j).getStartRecipe().getLogicalRecipeID();
                        break;
                    }
                    if (!ObjectIdentifier.isEmpty(logicalRecipeID)) {
                        break;
                    }
                }
                CimLogicalRecipe aLogicalRecipe = baseCoreFactory.getBO(CimLogicalRecipe.class, logicalRecipeID);
                CimProductSpecification aMonitorProduct = aLogicalRecipe.getMonitorProduct();
                if (aMonitorProduct != null) {
                    bMonitorCreationFlag = true;
                }
            }

            if (strEquipmentProcessBatchConditionGetOut.isCassetteExchangeFlag() && bMonitorCreationFlag) {
                if (emptyCassetteCount != (processAndProcMonCount + 1)) {
                    throw new ServiceException(new OmCode(retCodeConfig.getInvalidEmptyCount(), String.valueOf(emptyCassetteCount), String.valueOf(processAndProcMonCount + 1)));
                }
            } else if (strEquipmentProcessBatchConditionGetOut.isCassetteExchangeFlag() && !bMonitorCreationFlag) {
                if (emptyCassetteCount != processAndProcMonCount) {
                    throw new ServiceException(new OmCode(retCodeConfig.getInvalidEmptyCount(), String.valueOf(emptyCassetteCount), String.valueOf(processAndProcMonCount)));
                }
            } else if (!strEquipmentProcessBatchConditionGetOut.isCassetteExchangeFlag() && bMonitorCreationFlag) {
                if (emptyCassetteCount != 1) {
                    throw new ServiceException(new OmCode(retCodeConfig.getInvalidEmptyCount(), String.valueOf(emptyCassetteCount), String.valueOf(1)));
                }
            } else if (!strEquipmentProcessBatchConditionGetOut.isCassetteExchangeFlag() && !bMonitorCreationFlag) {
                if (emptyCassetteCount != 0) {
                    throw new ServiceException(new OmCode(retCodeConfig.getInvalidEmptyCount(), String.valueOf(emptyCassetteCount), String.valueOf(0)));
                }
            }
        }

        if (CimStringUtils.equals(operation, BizConstant.SP_OPERATION_OPESTART)
                || CimStringUtils.equals(operation, BizConstant.SP_OPERATION_STARTRESERVATION)
                || CimStringUtils.equals(operation, BizConstant.SP_OPERATION_CASSETTEDELIVERY)) {
            if (CimStringUtils.equals(multiRecipeCapability, BizConstant.SP_EQP_MULTIRECIPECAPABILITY_SINGLERECIPE)
                    || CimStringUtils.equals(multiRecipeCapability, BizConstant.SP_EQP_MULTIRECIPECAPABILITY_BATCH)) {
                boolean baseSetFlag = false;
                int baseI = 0;
                int baseJ = 0;
                int baseRPLen = 0;

                for (int i = 0; i < lenCassette; i++) {
                    if (CimStringUtils.equals(strStartCassette.get(i).getLoadPurposeType(), BizConstant.SP_LOADPURPOSETYPE_EMPTYCASSETTE)) {
                        continue;
                    }
                    List<Infos.LotInCassette> strLotInCassette = strStartCassette.get(i).getLotInCassetteList();
                    int lenLotInCassette = CimArrayUtils.getSize(strLotInCassette);

                    for (int j = 0; j < lenLotInCassette; j++) {
                        Infos.LotInCassette lotInCassette = strLotInCassette.get(j);
                        if (!lotInCassette.getMoveInFlag()) {
                            continue;
                        }

                        if (!CimStringUtils.equals(lotInCassette.getRecipeParameterChangeType(), BizConstant.SP_RPARM_CHANGETYPE_BYLOT)) {
                            throw new ServiceException(new OmCode(retCodeConfig.getInvalidRecipeParamChangeType(),
                                    ObjectIdentifier.fetchValue(lotInCassette.getLotID()), ObjectIdentifier.fetchValue(strStartCassette.get(i).getCassetteID())));
                        }

                        List<Infos.LotWafer> strLotWafer = lotInCassette.getLotWaferList();
                        if (!baseSetFlag) {
                            baseSetFlag = true;
                            baseI = i;
                            baseJ = j;

                            if (CimArrayUtils.getSize(strLotWafer) > 0) {
                                baseRPLen = CimArrayUtils.getSize(strLotWafer.get(0).getStartRecipeParameterList());
                            } else {
                                throw new ServiceException(new OmCode(retCodeConfig.getInvalidWaferCount(),
                                        ObjectIdentifier.fetchValue(lotInCassette.getLotID()), ObjectIdentifier.fetchValue(strStartCassette.get(i).getCassetteID())));
                            }
                        }

                        int lwLen = CimArrayUtils.getSize(strLotWafer);
                        for (int k = 0; k < lwLen; k++) {
                            List<Infos.StartRecipeParameter> strStartRecipeParameter = strLotWafer.get(k).getStartRecipeParameterList();
                            int rpLen = CimArrayUtils.getSize(strStartRecipeParameter);

                            if (rpLen != baseRPLen) {
                                throw new ServiceException(new OmCode(retCodeConfig.getNotSameRecipeParamInfo(),
                                        ObjectIdentifier.fetchValue(lotInCassette.getLotID()),
                                        ObjectIdentifier.fetchValue(strStartCassette.get(i).getCassetteID()),
                                        ObjectIdentifier.fetchValue(strStartCassette.get(baseI).getLotInCassetteList().get(baseJ).getLotID()),
                                        ObjectIdentifier.fetchValue(strStartCassette.get(baseI).getCassetteID())));
                            }

                            for (int l = 0; l < rpLen; l++) {
                                Infos.StartRecipeParameter startRecipeParameter = strStartRecipeParameter.get(l);
                                Infos.StartRecipeParameter baseStartRecipeParameter = strStartCassette.get(baseI).getLotInCassetteList().get(baseJ).getLotWaferList().get(0).getStartRecipeParameterList().get(l);
                                if (!CimStringUtils.equals(startRecipeParameter.getParameterName(), baseStartRecipeParameter.getParameterName())
                                        || !CimStringUtils.equals(startRecipeParameter.getParameterValue(), baseStartRecipeParameter.getParameterValue())
                                        || (startRecipeParameter.getUseCurrentSettingValueFlag() != null && !startRecipeParameter.getUseCurrentSettingValueFlag().equals(baseStartRecipeParameter.getUseCurrentSettingValueFlag()))) {
                                    throw new ServiceException(new OmCode(retCodeConfig.getNotSameRecipeParamInfo(),
                                            ObjectIdentifier.fetchValue(lotInCassette.getLotID()),
                                            ObjectIdentifier.fetchValue(strStartCassette.get(i).getCassetteID()),
                                            ObjectIdentifier.fetchValue(strStartCassette.get(baseI).getLotInCassetteList().get(baseJ).getLotID()),
                                            ObjectIdentifier.fetchValue(strStartCassette.get(baseI).getCassetteID())));
                                }
                            }
                        }
                    }
                }
            }
        }


        if (CimStringUtils.equals(operation, BizConstant.SP_OPERATION_OPESTART)
                || CimStringUtils.equals(operation, BizConstant.SP_OPERATION_STARTRESERVATION)
                || CimStringUtils.equals(operation, BizConstant.SP_OPERATION_CASSETTEDELIVERY)) {
            for (int i = 0; i < lenCassette; i++) {
                if (CimStringUtils.equals(strStartCassette.get(i).getLoadPurposeType(), BizConstant.SP_LOADPURPOSETYPE_EMPTYCASSETTE)) {
                    continue;
                }
                List<Infos.LotInCassette> strLotInCassette = strStartCassette.get(i).getLotInCassetteList();
                int lenLotInCassette = CimArrayUtils.getSize(strLotInCassette);
                for (int j = 0; j < lenLotInCassette; j++) {
                    Infos.LotInCassette lotInCassette = strLotInCassette.get(j);
                    if (!lotInCassette.getMoveInFlag()) {
                        continue;
                    }

                    boolean skipFlag = false;
                    boolean paramCheckWithFPC = false;
                    Outputs.ObjLotEffectiveFPCInfoGetOut lotEffectiveFPCInfoGet = lotMethod.lotEffectiveFPCInfoGet(strObjCommonIn, BizConstant.SP_FPC_EXCHANGETYPE_STARTRESERVEINFO, equipmentID, lotInCassette.getLotID());
                    paramCheckWithFPC = lotEffectiveFPCInfoGet.isRecipeParameterActionRequiredFlag();

                    CimMachine aMachine1 = baseCoreFactory.getBO(CimMachine.class, equipmentID);
                    //CimEquipmentDO aMachine1 = convertObjectIdentifierToEntity(equipmentID,CimEquipmentDO.class);
                    CimLogicalRecipe aLogicalRecipe = baseCoreFactory.getBO(CimLogicalRecipe.class, lotInCassette.getStartRecipe().getLogicalRecipeID());
                    CimMachineRecipe aMachineRecipe = null;
                    CimLot aLot = null;
                    String subLotType = null;
                    if (CimStringUtils.isEmpty(lotInCassette.getSubLotType())) {
                        aLot = baseCoreFactory.getBO(CimLot.class, lotInCassette.getLotID());
                        subLotType = aLot.getSubLotType();
                    } else {
                        subLotType = lotInCassette.getSubLotType();
                    }
                    if (searchCondition == 1) {
                        if (aLot == null) {
                            aLot = baseCoreFactory.getBO(CimLot.class, lotInCassette.getLotID());
                        }
                        aMachineRecipe = aLogicalRecipe.findMachineRecipeFor(aLot, aMachine1);
                    } else {
                        aMachineRecipe = aLogicalRecipe.findMachineRecipeForSubLotType(aMachine1, subLotType);
                    }
                    if (aMachineRecipe == null && lotEffectiveFPCInfoGet.isMachineRecipeActionRequiredFlag()) {
                        log.info("MachineRecipe is overwritten by DOC");
                        skipFlag = true;
                    }
                    // PosRecipeParameterSequence* aRecipeParameters = NULL;
                    List<RecipeDTO.RecipeParameter> aRecipeParameters = null;
                    if (!skipFlag && !paramCheckWithFPC) {
                        if (aMachineRecipe == null) {
                            throw new ServiceException(retCodeConfig.getNotFoundMachineRecipe());
                        }
                        aRecipeParameters = aLogicalRecipe.findRecipeParametersForSubLotType(aMachine1, aMachineRecipe, subLotType);
                    }
                    if (!paramCheckWithFPC) {
                        int lenParams = CimArrayUtils.getSize(aRecipeParameters);
                        for (int k = 0; k < lenParams; k++) {
                            RecipeDTO.RecipeParameter aRecipeParameter = aRecipeParameters.get(k);
                            if (CimStringUtils.equals(aRecipeParameter.getDataType(), BizConstant.SP_DCDEF_VAL_STRING)) {
                                continue;
                            }
                            List<Infos.LotWafer> strLotWafer = lotInCassette.getLotWaferList();
                            int lenLotWafer = CimArrayUtils.getSize(strLotWafer);
                            for (int l = 0; l < lenLotWafer; l++) {
                                List<Infos.StartRecipeParameter> strStartRecipeParameter = strLotWafer.get(l).getStartRecipeParameterList();
                                int lenRecipeParam = CimArrayUtils.getSize(strStartRecipeParameter);
                                for (int m = 0; m < lenRecipeParam; m++) {
                                    Infos.StartRecipeParameter startRecipeParameter = strStartRecipeParameter.get(m);
                                    if (CimStringUtils.equals(startRecipeParameter.getParameterName(), aRecipeParameter.getParameterName())) {
                                        // useCurrentValueFlag
                                        if (CimBooleanUtils.isTrue(aRecipeParameter.getUseCurrentValueFlag())) {
                                            if (!CimStringUtils.isEmpty(startRecipeParameter.getParameterValue())) {
                                                throw new ServiceException(new OmCode(retCodeConfig.getInvalidParameterValueMustBeNull(), aRecipeParameter.getParameterName()));
                                            }
                                        } else {
                                            if (CimStringUtils.equals(aRecipeParameter.getDataType(), BizConstant.SP_DCDEF_VAL_INTEGER)) {
                                                boolean bIsLong = true;
                                                int errno = 0;
                                                StringBuilder endptr = new StringBuilder();
                                                Long nResult;
                                                try {
                                                    nResult = BaseStaticMethod.strtol(startRecipeParameter.getParameterValue(), endptr);
                                                } catch (Exception e) {
                                                    bIsLong = false;
                                                }

                                                if (CimStringUtils.isEmpty(endptr.toString())) {
                                                    bIsLong = true;
                                                } else {
                                                    bIsLong = false;
                                                }

                                                Long parameterValue, lowerLimit, upperLimit;

                                                parameterValue = Long.parseLong(startRecipeParameter.getParameterValue());
                                                lowerLimit = Long.parseLong(aRecipeParameter.getLowerLimit());
                                                upperLimit = Long.parseLong(aRecipeParameter.getUpperLimit());

                                                if (!bIsLong || ((parameterValue < lowerLimit) || (parameterValue > upperLimit))) {
                                                    throw new ServiceException(new OmCode(retCodeConfig.getInvalidParameterValueRange(),
                                                            aRecipeParameter.getParameterName(), aRecipeParameter.getLowerLimit(), aRecipeParameter.getUpperLimit()));
                                                }
                                                log.info("limit check ok...");
                                            } else if (CimStringUtils.equals(aRecipeParameter.getDataType(), BizConstant.SP_DCDEF_VAL_FLOAT)) {
                                                Double parameterValue, lowerLimit, upperLimit;
                                                parameterValue = Double.parseDouble(startRecipeParameter.getParameterValue());
                                                lowerLimit = Double.parseDouble(aRecipeParameter.getLowerLimit());
                                                upperLimit = Double.parseDouble(aRecipeParameter.getUpperLimit());
                                                if ((parameterValue < lowerLimit) || (parameterValue > upperLimit)) {
                                                    throw new ServiceException(new OmCode(retCodeConfig.getInvalidParameterValueRange(),
                                                            aRecipeParameter.getParameterName(), aRecipeParameter.getLowerLimit(), aRecipeParameter.getUpperLimit()));

                                                }
                                                log.info("limit check ok...");
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        log.info("Recipe Parameter Check with DOC Information...");
                        List<Infos.LotWafer> strLotWafer = lotInCassette.getLotWaferList();
                        long lenLotWafer = CimArrayUtils.getSize(strLotWafer);
                        for (int l = 0; l < lenLotWafer; l++) {
                            Infos.LotWafer lotWafer = strLotWafer.get(l);
                            ObjectIdentifier tmpWaferID = lotWafer.getWaferID();
                            List<Infos.LotWaferInfo> lotWaferInfoList = lotEffectiveFPCInfoGet.getFpcInfo().getLotWaferInfoList();
                            int fpcWaferCount = CimArrayUtils.getSize(lotWaferInfoList);
                            log.info(String.format("DOC Info WaferCount: %d", fpcWaferCount));
                            int wPos = 0;
                            for (wPos = 0; wPos < fpcWaferCount; wPos++) {
                                if (ObjectIdentifier.equalsWithValue(tmpWaferID, lotWaferInfoList.get(wPos).getWaferID())) {
                                    log.info("wafer found in DOC Info...");
                                    break;
                                }
                            }
                            if (wPos == fpcWaferCount) {
                                log.info(String.format("wafer not found in FPCInfo:%s", tmpWaferID.getValue()));
                                throw new ServiceException(retCodeConfig.getFpcWaferMismatchInFpcGroup());
                            }
                            //DOC wafer Matching END. use wPos.
                            List<Infos.StartRecipeParameter> strStartRecipeParameter = lotWafer.getStartRecipeParameterList();
                            int lenRecipeParam = CimArrayUtils.getSize(strStartRecipeParameter);
                            for (int m = 0; m < lenRecipeParam; m++) {
                                Infos.StartRecipeParameter startRecipeParameter = strStartRecipeParameter.get(m);
                                String fpcParamName = startRecipeParameter.getParameterName();
                                int fpcRParamCount = CimArrayUtils.getSize(lotWaferInfoList.get(wPos).getRecipeParameterInfoList());
                                int pPos = 0;
                                for (pPos = 0; pPos < fpcRParamCount; pPos++) {
                                    if (CimStringUtils.equals(fpcParamName, lotWaferInfoList.get(wPos).getRecipeParameterInfoList().get(pPos).getParameterName())) {
                                        log.info("recipeParam found in DOC Info.");
                                        break;
                                    }
                                }
                                if (pPos == fpcRParamCount) {
                                    throw new ServiceException(retCodeConfig.getFpcRecipeParamError());
                                }
                                Infos.RecipeParameterInfo recipeParameterInfo = lotWaferInfoList.get(wPos).getRecipeParameterInfoList().get(pPos);
                                String fpcParamUnit = recipeParameterInfo.getParameterUnit();
                                String fpcParamDataType = recipeParameterInfo.getParameterDataType();
                                String fpcParamLowerLimit = recipeParameterInfo.getParameterLowerLimit();
                                String fpcParamUpperLimit = recipeParameterInfo.getParameterUpperLimit();
                                boolean fpcUseCurrentSettingFlag = recipeParameterInfo.isUseCurrentSettingValueFlag();
                                String fpcParamTargetValue = recipeParameterInfo.getParameterTargetValue();
                                String fpcParamValue = recipeParameterInfo.getParameterValue();

                                long lowerLimit = Long.parseLong(fpcParamLowerLimit);
                                long upperLimit = Long.parseLong(fpcParamUpperLimit);

                                if (CimStringUtils.equals(fpcParamDataType, BizConstant.SP_DCDEF_VAL_STRING)) {
                                    log.info("dataType is SP_DCDef_Val_String...");
                                    continue;
                                }
                                if (fpcUseCurrentSettingFlag) {
                                    if (!CimStringUtils.isEmpty(startRecipeParameter.getParameterValue())) {
                                        throw new ServiceException(new OmCode(retCodeConfig.getInvalidParameterValueMustBeNull(), fpcParamName));
                                    }
                                } else {
                                    if (CimStringUtils.equals(fpcParamDataType, BizConstant.SP_DCDEF_VAL_INTEGER)) {
                                        boolean bIsLong = true;
                                        long parameterValue = 0;
                                        try {
                                            parameterValue = Long.parseLong(startRecipeParameter.getParameterValue());
                                            bIsLong = true;
                                        } catch (NumberFormatException e) {
                                            log.warn(String.format("The character which cannot be recognized as a numerical value: %s", startRecipeParameter.getParameterValue()));
                                            bIsLong = false;
                                            throw new ServiceException(new OmCode(retCodeConfig.getInvalidParameterValueRange(), fpcParamName, fpcParamLowerLimit, fpcParamUpperLimit));
                                        }
                                        if (parameterValue < lowerLimit || parameterValue > upperLimit) {
                                            throw new ServiceException(new OmCode(retCodeConfig.getInvalidParameterValueRange(), fpcParamName, fpcParamLowerLimit, fpcParamUpperLimit));
                                        }
                                    } else if (CimStringUtils.equals(fpcParamDataType, BizConstant.SP_DCDEF_VAL_FLOAT)) {
                                        double parameterValue = Double.parseDouble(startRecipeParameter.getParameterValue());
                                        if (parameterValue < lowerLimit || parameterValue > upperLimit) {
                                            throw new ServiceException(new OmCode(retCodeConfig.getInvalidParameterValueRange(), fpcParamName, fpcParamLowerLimit, fpcParamUpperLimit));
                                        }
                                    }
                                }


                            } //end of [m]
                        } //end of [l]
                    }

                }
            }
        }


        if (CimStringUtils.equals(operation, BizConstant.SP_OPERATION_OPESTART)
                || CimStringUtils.equals(operation, BizConstant.SP_OPERATION_STARTRESERVATION)
                || CimStringUtils.equals(operation, BizConstant.SP_OPERATION_CASSETTEDELIVERY)) {
            int nStartLotCnt = 0;
            int nMonitorLotCnt = 0;

            lenCassette = CimArrayUtils.getSize(strStartCassette);

            for (int i = 0; i < lenCassette; i++) {

                if (CimStringUtils.equals(strStartCassette.get(i).getLoadPurposeType(), BizConstant.SP_LOADPURPOSETYPE_PROCESSLOT)
                        || CimStringUtils.equals(strStartCassette.get(i).getLoadPurposeType(), BizConstant.SP_LOADPURPOSETYPE_PROCESSMONITORLOT)) {
                    List<Infos.LotInCassette> strLotInCassette = strStartCassette.get(i).getLotInCassetteList();
                    int lenLotInCassette = CimArrayUtils.getSize(strLotInCassette);

                    for (int j = 0; j < lenLotInCassette; j++) {
                        Infos.LotInCassette lotInCassette = strLotInCassette.get(j);
                        if (lotInCassette.getMoveInFlag()) {
                            nStartLotCnt++;

                            if (lotInCassette.getMonitorLotFlag()) {
                                nMonitorLotCnt++;
                            }
                        }
                    }
                }
            }

            if (nMonitorLotCnt > 1) {
                throw new ServiceException(retCodeConfig.getInvalidProductMonitorCount());
            }

            if (nMonitorLotCnt == 1 && nStartLotCnt == 1) {
                throw new ServiceException(new OmCode(retCodeConfig.getInvalidInputLotCount(), String.valueOf(nStartLotCnt), "2", "n"));
            }

            if (0 == nStartLotCnt) {
                throw new ServiceException(new OmCode(retCodeConfig.getInvalidInputLotCount(), "0", "1", "n"));
            }
            int i_cast = 0;
            int cassetteLen = CimArrayUtils.getSize(strStartCassette);
            for (i_cast = 0; i_cast < cassetteLen; i_cast++) {
                Infos.StartCassette startCassette = strStartCassette.get(i_cast);
                if (CimStringUtils.equals(startCassette.getLoadPurposeType(), BizConstant.SP_LOADPURPOSETYPE_PROCESSMONITORLOT)) {
                    if (nMonitorLotCnt != 1) {
                        throw new ServiceException(retCodeConfig.getInvalidProductMonitorCount());
                    }

                    int j_lot = 0;
                    List<Infos.LotInCassette> strLotInCassette = startCassette.getLotInCassetteList();
                    int lotLen = CimArrayUtils.getSize(strLotInCassette);

                    for (j_lot = 0; j_lot < lotLen; j_lot++) {
                        Infos.LotInCassette lotInCassette = strLotInCassette.get(j_lot);
                        if ((lotInCassette.getMonitorLotFlag()) &&
                                (lotInCassette.getMoveInFlag())) {
                            if (!CimStringUtils.equals(lotInCassette.getLotType(), BizConstant.SP_LOT_TYPE_PRODUCTIONMONITORLOT)) {
                                // step11 - lot_monitorRouteFlag_Get
                                boolean strLot_monitorRouteFlag_Get_out = lotMethod.lotMonitorRouteFlagGet(strObjCommonIn, lotInCassette.getLotID());
                                if (!strLot_monitorRouteFlag_Get_out) {
                                    throw new ServiceException(new OmCode(retCodeConfig.getInvalidLotType(), lotInCassette.getLotType(), lotInCassette.getLotID().getValue()));
                                }
                            }
                            break;
                        }
                    }
                }
            }
        }
        return;
    }

    /**
     * description:
     * <p><br/></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param cassetteID
     * @return com.fa.cim.dto.RetCode<java.lang.String>
     * @author Ho
     * @date 2018/9/28 15:43:50
     */
    @Override
    public String cassetteInterFabXferStateGet(Infos.ObjCommon objCommon, ObjectIdentifier cassetteID) {
        //step1 Check input parameter
        com.fa.cim.newcore.bo.durable.CimCassette cimCassette = baseCoreFactory.getBO(com.fa.cim.newcore.bo.durable.CimCassette.class, cassetteID);
        Validations.check(null == cimCassette, new OmCode(retCodeConfig.getNotFoundCassette(), cassetteID.getValue()));
        return cimCassette.getInterFabTransferState();
    }

    /**
     * description:cassette_baseInfo_GetDR
     * <p><br/></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param cassetteID
     * @return com.fa.cim.dto.RetCode<com.fa.cim.dto.Results.CarrierBasicInfoInqResult>
     * @author Ho
     * @date 2018/10/15 13:41:56
     */
    @Override
    public Infos.DurableAttribute cassetteBaseInfoGetDR(Infos.ObjCommon objCommon, ObjectIdentifier cassetteID) {

        CimCassetteDO example = new CimCassetteDO();
        example.setCassetteID(ObjectIdentifier.fetchValue(cassetteID));
        CimCassetteDO cassette = cimJpaRepository.findOne(Example.of(example)).orElse(null);
        Validations.check(cassette == null, retCodeConfig.getNotFoundCassette());

        Double durationLimit = cassette.getDurationLimit();
        durationLimit = durationLimit == null || durationLimit < 0 ? 0d : durationLimit / 60 / 1000;
        long maximumRunTime = durationLimit.longValue();

        Infos.DurableAttribute strDurableAttribute = new Infos.DurableAttribute();
        strDurableAttribute.setDurableID(new ObjectIdentifier(cassette.getCassetteID(), cassette.getId()));
        strDurableAttribute.setDescription(cassette.getDescription());
        strDurableAttribute.setCategory(cassette.getCassetteCategory());
        strDurableAttribute.setUsageCheckFlag(cassette.getUsageCheckReq());
        strDurableAttribute.setMaximumRunTime(String.valueOf(maximumRunTime));
        strDurableAttribute.setMaximumOperationStartCount(cassette.getTimesUsedLimit());
        strDurableAttribute.setIntervalBetweenPM(cassette.getIntervalBetweenPM());
        strDurableAttribute.setCapacity(cassette.getCassetteCapacity());
        strDurableAttribute.setNominalSize(cassette.getWaferSize());
        strDurableAttribute.setContents(cassette.getMaterialContents());
        strDurableAttribute.setInstanceName(cassette.getInstanceName());
        strDurableAttribute.setUserDatas(new ArrayList<>());
        strDurableAttribute.setCarrierType(cassette.getCarrierType());
        strDurableAttribute.setProductUsage(cassette.getProductUsage());
        return strDurableAttribute;
    }

    /**
     * description:cassette_userDataInfo_GetDR
     * <p><br/></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param cassetteID
     * @return com.fa.cim.dto.RetCode<java.util.List < com.fa.cim.pojo.Infos.UserData>>
     * @author Ho
     * @date 2018/10/15 14:46:37
     */
    @Override
    public List<Infos.UserData> cassetteUserDataInfoGetDR(Infos.ObjCommon objCommon, ObjectIdentifier cassetteID) {
        CimCassetteDO cimCassetteExample = new CimCassetteDO();
        cimCassetteExample.setCassetteID(ObjectIdentifier.fetchValue(cassetteID));

        return cimJpaRepository.findChildEntities(CimCassetteUserDataDO.class, Example.of(cimCassetteExample))
                .stream().sorted()
                .map(record -> {
                    Infos.UserData userData = new Infos.UserData();
                    userData.setName(record.getName());
                    userData.setType(record.getType());
                    userData.setValue(record.getValue());
                    userData.setOriginator(record.getOriginal());
                    return userData;
                }).collect(Collectors.toList());
    }

    /**
     * description:
     * <p><br/></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param cassetteID
     * @return com.fa.cim.dto.RetCode<java.lang.Object>
     * @author Ho
     * @date 2018/10/16 11:08:52
     */
    @Override
    public void cassetteUsageInfoReset(Infos.ObjCommon objCommon, ObjectIdentifier cassetteID) {

        com.fa.cim.newcore.bo.durable.CimCassette aCassette = baseCoreFactory.getBO(com.fa.cim.newcore.bo.durable.CimCassette.class, cassetteID);

        aCassette.setTimesUsed(0L);

        aCassette.setDurationUsed(0d);

        CimPerson aPerson = baseCoreFactory.getBO(CimPerson.class, objCommon.getUser().getUserID());

        aCassette.setLastMaintenanceTimeStamp(objCommon.getTimeStamp().getReportTimeStamp().toString());

        aCassette.setLastMaintenancePerson(aPerson);

        aCassette.setLastClaimedTimeStamp(objCommon.getTimeStamp().getReportTimeStamp());

        aCassette.setLastClaimedPerson(aPerson);
    }

    /**
     * description:  cassette_state_Change
     * <p><br/></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param cassetteID
     * @param cassetteStatus
     * @return com.fa.cim.dto.RetCode<java.lang.Object>
     * @author Ho
     * @date 2018/9/28 17:52:26
     */
    @Override
    public void cassetteStateChange(Infos.ObjCommon objCommon, ObjectIdentifier cassetteID, String cassetteStatus) {
        boolean Flag = false;

        CimCassette aCassette = baseCoreFactory.getBO(CimCassette.class, cassetteID);

        String aDurableState = aCassette.getDurableState();

        if (CimStringUtils.equals(cassetteStatus, CIMStateConst.CIM_DURABLE_AVAILABLE)) {
            Flag = aCassette.isAvailable();
            if (Flag) {
                throw new ServiceException(new OmCode(retCodeConfig.getSameCastStat(), cassetteStatus));
            } else {
                aCassette.makeAvailable();
            }
        } else if (CimStringUtils.equals(cassetteStatus, CIMStateConst.CIM_DURABLE_INUSE)) {
            Flag = aCassette.isInUse();
            if (Flag) {
                throw new ServiceException(new OmCode(retCodeConfig.getSameCastStat(), cassetteStatus));
            } else {
                aCassette.makeInUse();
            }
        } else if (CimStringUtils.equals(cassetteStatus, CIMStateConst.CIM_DURABLE_NOTAVAILABLE)) {
            Flag = aCassette.isNotAvailable();
            if (Flag) {
                throw new ServiceException(new OmCode(retCodeConfig.getSameCastStat(), cassetteStatus));
            } else {
                aCassette.makeNotAvailable();
            }
        } else if (CimStringUtils.equals(cassetteStatus, CIMStateConst.CIM_DURABLE_SCRAPPED)) {
            Flag = aCassette.isScrapped();
            if (Flag) {
                throw new ServiceException(new OmCode(retCodeConfig.getSameCastStat(), cassetteStatus));
            } else {
                List<Lot> lotseq = aCassette.allLots();
                Validations.check(!CimArrayUtils.isEmpty(lotseq), new OmCode(retCodeConfig.getCastHasAnyLots(), cassetteStatus));
                aCassette.makeScrapped();
            }
        } else {
            throw new ServiceException(new OmCode(retCodeConfig.getInvalidCassetteState(), aDurableState, ObjectIdentifier.fetchValue(cassetteID)));
        }
        CimPerson aPerson = baseCoreFactory.getBO(CimPerson.class, objCommon.getUser().getUserID());
        aCassette.setLastClaimedTimeStamp(objCommon.getTimeStamp().getReportTimeStamp());
        aCassette.setLastClaimedPerson(aPerson);
        aCassette.setStateChangedTimeStamp(objCommon.getTimeStamp().getReportTimeStamp());
        aCassette.setStateChangedPerson(aPerson);
    }

    /**
     * description: cassette_FillInTxLGQ004DR
     * <p><br/></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param stockerID
     * @return com.fa.cim.dto.RetCode<com.fa.cim.dto.Results.StockerInfoInqResult>
     * @author Ho
     * @since 2018/10/8 14:09:34
     */
    @Override
    public Results.StockerInfoInqResult cassetteFillInTxLGQ004DR(Infos.ObjCommon objCommon, ObjectIdentifier stockerID) {
        Results.StockerInfoInqResult stockerInfoInqResult = new Results.StockerInfoInqResult();
        CimCassetteDO cimCassetteExample = new CimCassetteDO();
        cimCassetteExample.setEquipmentID(ObjectIdentifier.fetchValue(stockerID));
        List<CimCassetteDO> cassetteList = cimJpaRepository.findAll(Example.of(cimCassetteExample));
        if (CimArrayUtils.isEmpty(cassetteList)) {
            return stockerInfoInqResult;
        }
        List<Infos.CarrierInStocker> carrierInStockerList = new ArrayList<>();
        for (CimCassetteDO cassette : cassetteList) {
            Infos.CarrierInStocker carrierInStocker = new Infos.CarrierInStocker();
            carrierInStocker.setCassetteID(ObjectIdentifier.build(cassette.getCassetteID(), cassette.getId()));
            carrierInStocker.setCarrierCategory(cassette.getCassetteCategory());
            if (cassette.getCastUsedCapacity() > 0) {
                carrierInStocker.setEmptyFlag(false);
            } else {
                carrierInStocker.setEmptyFlag(true);
            }
            carrierInStocker.setTransferJobStatus(cassette.getTransferState());
            carrierInStocker.setMultiLotType(cassette.getMultiLotType());
            carrierInStocker.setResrvUserId(cassette.getReserveUserID());
            carrierInStocker.setDispatchReserved(cassette.getDispatchReserved());
            carrierInStockerList.add(carrierInStocker);
        }

        stockerInfoInqResult.setStrCarrierInStocker(carrierInStockerList);

        return stockerInfoInqResult;
    }

    @Override
    public Infos.LotListInCassetteInfo cassetteGetLotList(Infos.ObjCommon objCommon, ObjectIdentifier cassetteID) {
        Infos.LotListInCassetteInfo lotListInCassetteInfo = new Infos.LotListInCassetteInfo();
        List<ObjectIdentifier> lotIDList = new ArrayList<>();
        lotListInCassetteInfo.setLotIDList(lotIDList);
        //【step1】get cassette object,carrier=FOSB11111 跳过验证
        log.debug("【step1】get cassette object");
        if (!SorterHandler.containsFOSB(cassetteID)) {
            com.fa.cim.newcore.bo.durable.CimCassette aCassette = baseCoreFactory.getBO(com.fa.cim.newcore.bo.durable.CimCassette.class, cassetteID);
            Validations.check(null == aCassette, new OmCode(retCodeConfig.getNotFoundCassette(), ObjectIdentifier.fetchValue(cassetteID)));

            //----------------------------------------//
            //   Set CassetteID to return structure   //
            //----------------------------------------//
            lotListInCassetteInfo.setCassetteID(new ObjectIdentifier(aCassette.getIdentifier(), aCassette.getPrimaryKey()));
            lotListInCassetteInfo.setMultiLotType(aCassette.getMultiLotType());
            //【step2】get lotIDs which are contained in cassette
            log.debug("【step2】get lotIDs which are contained in cassette");
            List<Lot> aLotList = aCassette.allLots();
            Validations.check(CimArrayUtils.isEmpty(aLotList), lotListInCassetteInfo, new OmCode(retCodeConfig.getNotFoundLot(), "*****"));

            if (CimArrayUtils.isNotEmpty(aLotList)) {
                for (Lot lot : aLotList) {
                    lotIDList.add(new ObjectIdentifier(lot.getIdentifier(), lot.getPrimaryKey()));
                }
            }
        }
        return lotListInCassetteInfo;
    }


    @Override
    public List<Infos.WaferMapInCassetteInfo> cassetteGetWaferMapDR(Infos.ObjCommon objCommon, ObjectIdentifier cassetteID) {
        List<Infos.WaferMapInCassetteInfo> waferMapInCassetteInfoSeq = new ArrayList<>();
        /*-------------------------------------------------*/
        /*   Get d_theSystemKey of cassetteID of In-parm   */
        /*-------------------------------------------------*/
        com.fa.cim.newcore.bo.durable.CimCassette cassette = baseCoreFactory.getBO(com.fa.cim.newcore.bo.durable.CimCassette.class, cassetteID);
        Validations.check(cassette == null, new OmCode(retCodeConfig.getNotFoundCassette(), cassetteID.getValue()));
        //【step1】Get waferID and slot# which are in cassetteID
        log.info("【step1】get waferID and slot# which are in cassetteID");
        //Fetch Cursor Table
        CimCassetteWaferDO cimCassetteWaferExample = new CimCassetteWaferDO();
        cimCassetteWaferExample.setReferenceKey(cassette.getPrimaryKey());
        List<CimCassetteWaferDO> cassetteWaferList = cimJpaRepository.findAll(Example.of(cimCassetteWaferExample)).stream()
                .sorted().collect(Collectors.toList());
        for (CimCassetteWaferDO cassetteWafer : cassetteWaferList) {
            if (cassetteWafer.getSequenceNumber() == 0) {
                continue;
            }
            Infos.WaferMapInCassetteInfo waferMapInCassetteInfo = new Infos.WaferMapInCassetteInfo();
            waferMapInCassetteInfo.setSlotNumber(cassetteWafer.getSequenceNumber());
            waferMapInCassetteInfo.setWaferID(new ObjectIdentifier(cassetteWafer.getWaferID(), cassetteWafer.getWaferObject()));
            waferMapInCassetteInfoSeq.add(waferMapInCassetteInfo);
        }

        //【step2】get sourceLotID corresponding to waferID
        log.debug("【step2】get sourceLotID corresponding to waferID");
        for (Infos.WaferMapInCassetteInfo waferMapInCassetteInfo : waferMapInCassetteInfoSeq) {
            com.fa.cim.newcore.bo.product.CimWafer wafer = baseCoreFactory.getBO(com.fa.cim.newcore.bo.product.CimWafer.class, waferMapInCassetteInfo.getWaferID());
            if (null == wafer) {
                continue;
            }
            ProductDTO.WaferInfo waferInfo = wafer.getWaferInfo();
            waferMapInCassetteInfo.setLotID(waferInfo.getLotID());
            waferMapInCassetteInfo.setAliasWaferName(wafer.getAliasWaferName());
            waferMapInCassetteInfo.setScrapState(waferInfo.getScrapState());
        }
        return waferMapInCassetteInfoSeq;
    }

    @Override
    public String cassetteMultiLotTypeUpdate(Infos.ObjCommon objCommon, ObjectIdentifier cassetteID) {
        String out = null;
        //【step2】get the set of lots is in cassette
        log.info("【step2】get the set of lots is in cassette");
        com.fa.cim.newcore.bo.durable.CimCassette aCassette = baseCoreFactory.getBO(com.fa.cim.newcore.bo.durable.CimCassette.class, cassetteID);
        Validations.check(aCassette == null, new OmCode(retCodeConfig.getNotFoundCassette(), "*****"));
        //【step3】set multiple lot type
        log.info("【step3】set multiple lot type");
        List<Lot> aLotList = aCassette.allLots();
        int nLen = CimArrayUtils.getSize(aLotList);
        if (nLen != 0) {
            // step2-1 getting scrap flag marked record from db that were on MMDB
            log.info(String.format("checking scrap wafer is existed on cassette which cassette ID is '%s'", cassetteID));
            String transactionID = objCommon.getTransactionID();
            if (TransactionIDEnum.equals(TransactionIDEnum.SCRAP_WAFER_CANCEL_REQ, transactionID)
                    || TransactionIDEnum.equals(TransactionIDEnum.SCRAP_WAFER_REQ, transactionID)
                    || TransactionIDEnum.equals(TransactionIDEnum.WAFER_SORTER_RPT, transactionID)
                    || TransactionIDEnum.equals(TransactionIDEnum.WAFER_SORTER_POSITION_ADJUST_REQ, transactionID)
                    || TransactionIDEnum.equals(TransactionIDEnum.WAFER_SORTER_REQ, transactionID)
                    || TransactionIDEnum.equals(TransactionIDEnum.WAFER_SORTER_ON_EQP_RPT, transactionID)) {
                log.info("set multiLotType [ML-MR]...");
                List<Material> tmpMaterialList = aCassette.containedMaterial();
                if (!CimArrayUtils.isEmpty(tmpMaterialList)) {
                    for (Material material : tmpMaterialList) {
                        com.fa.cim.newcore.bo.product.CimWafer aPosWafer = (com.fa.cim.newcore.bo.product.CimWafer) material;
                        if (aPosWafer.isScrap()) {
                            log.info("wafer.isScrap() = TRUE");
                            aCassette.setMultiLotType(BizConstant.SP_CAS_MULTILOTTYPE_MULTILOTMULTIRECIPE);
                            out = BizConstant.SP_CAS_MULTILOTTYPE_MULTILOTMULTIRECIPE;
                            return out;
                        }
                    }
                }
            } else {
                log.info("set multiple lot type is [ML_MR].");
                List<ObjectIdentifier> cassetteIDList = new ArrayList<>();
                cassetteIDList.add(cassetteID);
                List<Infos.LotWaferMap> lotWaferMaps = this.cassetteScrapWaferSelectDR(objCommon, cassetteIDList);
                if (!CimArrayUtils.isEmpty(lotWaferMaps)) {
                    aCassette.setMultiLotType(BizConstant.SP_CAS_MULTILOTTYPE_MULTILOTMULTIRECIPE);
                    out = BizConstant.SP_CAS_MULTILOTTYPE_MULTILOTMULTIRECIPE;
                    return out;
                }
            }
        }
        log.info("not found scrap wafer...");
        if (nLen == 0) {
            // step3 - no lot in cassette
            // set blank to multiple lot type
            aCassette.setMultiLotType(null);
            return out;
        } else if (1 == nLen) {
            // set single lot single recipe to multiple lot type
            aCassette.setMultiLotType(BizConstant.SP_CAS_MULTILOTTYPE_SINGLELOTSINGLERECIPE);
            out = BizConstant.SP_CAS_MULTILOTTYPE_SINGLELOTSINGLERECIPE;
        } else {
            // step4 - multiple lot cassette
            boolean holdFlag = false;
            boolean bankFlag = false;
            boolean inprFlag = false;
            String tmpLogicalRecipeID = null;
            for (int i = 0; i < nLen; i++) {
                com.fa.cim.newcore.bo.product.CimLot aLot = (com.fa.cim.newcore.bo.product.CimLot) aLotList.get(i);
                Validations.check(aLot == null, new OmCode(retCodeConfig.getNotFoundLot(), ""));
                // check if status is onHold or InBank
                holdFlag = aLot.isOnHold();
                bankFlag = aLot.isInBank();
                inprFlag = aLot.isProcessing();
                if (holdFlag || bankFlag || inprFlag) {
                    aCassette.setMultiLotType(BizConstant.SP_CAS_MULTILOTTYPE_MULTILOTMULTIRECIPE);
                    out = BizConstant.SP_CAS_MULTILOTTYPE_MULTILOTMULTIRECIPE;
                    return out;
                }
                ObjectIdentifier tempLotID = new ObjectIdentifier(aLot.getIdentifier(), aLot.getPrimaryKey());
                Outputs.ObjLotEffectiveFPCInfoGetOut getLotEffectiveFPCInfoOut = lotMethod.lotEffectiveFPCInfoGet(objCommon, BizConstant.SP_FPC_EXCHANGETYPE_ALL, null, tempLotID);

                Outputs.ObjLotEffectiveFPCInfoGetOut getOut = getLotEffectiveFPCInfoOut;
                if (!CimObjectUtils.isEmpty(getOut) && (CimBooleanUtils.isTrue(getOut.isEquipmentActionRequiredFlag())
                        || CimBooleanUtils.isTrue(getOut.isMachineRecipeActionRequiredFlag())
                        || CimBooleanUtils.isTrue(getOut.isRecipeParameterActionRequiredFlag())
                        || CimBooleanUtils.isTrue(getOut.isDcDefActionRequiredFlag())
                        || CimBooleanUtils.isTrue(getOut.isDcSpecActionRequiredFlag())
                        || CimBooleanUtils.isTrue(getOut.isReticleActionRequiredFlag()))) {
                    log.info("If lot has DOC definition, update cassette multiple lot type to MLMR...");
                    aCassette.setMultiLotType(BizConstant.SP_CAS_MULTILOTTYPE_MULTILOTMULTIRECIPE);
                    out = BizConstant.SP_CAS_MULTILOTTYPE_MULTILOTMULTIRECIPE;
                    return out;
                }
                // get PO object
                com.fa.cim.newcore.bo.pd.CimProcessOperation aPO = aLot.getProcessOperation();
                if (null == aPO) {
                    ObjectIdentifier lotID = new ObjectIdentifier(aLot.getIdentifier(), aLot.getPrimaryKey());
                    throw new ServiceException(new OmCode(retCodeConfig.getNotFoundProcessOperation(), "", lotID.getValue()));
                }
                // get productSpecification object
                CimProductSpecification aProdSpec = aLot.getProductSpecification();
                Validations.check(aProdSpec == null, retCodeConfig.getNotFoundProductSpec());
                /*-------------------------------*/
                /*   Get Logical Recipe Object   */
                /*-------------------------------*/
                com.fa.cim.newcore.bo.recipe.CimLogicalRecipe aLogicalRecipe = aPO.findLogicalRecipeFor(aProdSpec);
                if (null == aLogicalRecipe) {
                    //set multiLotMultiRecipe to multiLotType
                    aCassette.setMultiLotType(BizConstant.SP_CAS_MULTILOTTYPE_MULTILOTMULTIRECIPE);
                    out = BizConstant.SP_CAS_MULTILOTTYPE_MULTILOTMULTIRECIPE;
                    return out;
                }
                if (i == 0) {
                    tmpLogicalRecipeID = aLogicalRecipe.getIdentifier();
                } else {
                    String aLogicalRecipeID = aLogicalRecipe.getIdentifier();
                    if (!CimStringUtils.equals(tmpLogicalRecipeID, aLogicalRecipeID)) {
                        //set multiLotMultiRecipe to multiLotType
                        aCassette.setMultiLotType(BizConstant.SP_CAS_MULTILOTTYPE_MULTILOTMULTIRECIPE);
                        out = BizConstant.SP_CAS_MULTILOTTYPE_MULTILOTMULTIRECIPE;
                        return out;
                    }
                }
            }
            /*-----------------------------------------------*/
            /*   Set MultiLotSingleRecipe to multiLotType    */
            /*-----------------------------------------------*/
            aCassette.setMultiLotType(BizConstant.SP_CAS_MULTILOTTYPE_MULTILOTSINGLERECIPE);
            return BizConstant.SP_CAS_MULTILOTTYPE_MULTILOTSINGLERECIPE;
        }
        return out;
    }

    /**
     * cassette_dispatchState_Get
     *
     * @param objCommon
     * @param cassetteID
     * @return
     * @author ho
     */
    @Override
    public Boolean cassetteDispatchStateGet(Infos.ObjCommon objCommon, ObjectIdentifier cassetteID) {
        com.fa.cim.newcore.bo.durable.CimCassette aCassette = baseCoreFactory.getBO(com.fa.cim.newcore.bo.durable.CimCassette.class, cassetteID);
        Validations.check(aCassette == null, new OmCode(retCodeConfig.getNotFoundCassette(), cassetteID.getValue()));
        return aCassette.isDispatchReserved();
    }

    /**
     * cassette_reservedState_Get
     *
     * @param objCommon
     * @param carrierID
     * @return
     * @author ho
     */
    @Override
    public Outputs.ObjCassetteReservedStateGetOut cassetteReservedStateGet(Infos.ObjCommon objCommon, ObjectIdentifier carrierID) {
        Outputs.ObjCassetteReservedStateGetOut result = new Outputs.ObjCassetteReservedStateGetOut();
        com.fa.cim.newcore.bo.durable.CimCassette cassette = baseCoreFactory.getBO(com.fa.cim.newcore.bo.durable.CimCassette.class, carrierID);
        Validations.check(CimObjectUtils.isEmpty(cassette), retCodeConfig.getNotFoundCassette());
        result.setTransferReserved(cassette.isReserved());
        result.setReservedPerson(cassette.getReservePersonID());
        return result;
    }

    @Override
    public void cassetteTransferStateChange(Infos.ObjCommon objCommon, ObjectIdentifier stockerID, ObjectIdentifier equipmentID, ObjectIdentifier carrierID, Infos.XferCassette xferCassette, Timestamp transferStatusChangeTimeStamp,
                                            Infos.ShelfPosition shelfPosition) {
        /*------------------------*/
        /*   Get Machine Object   */
        /*------------------------*/
        CimMachine aMachine = null;
        if (!ObjectIdentifier.isEmptyWithValue(equipmentID)) {
            aMachine = baseCoreFactory.getBO(CimMachine.class, equipmentID);
            Validations.check(aMachine == null, new OmCode(retCodeConfig.getNotFoundEqp(), ObjectIdentifier.fetchValue(equipmentID)));
        }
        /*------------------------*/
        /*   Get Stocker Object   */
        /*------------------------*/
        CimStorageMachine aStocker = null;
        if (!ObjectIdentifier.isEmptyWithValue(stockerID)) {
            aStocker = baseCoreFactory.getBO(CimStorageMachine.class, stockerID);
            Validations.check(aStocker == null, new OmCode(retCodeConfig.getNotFoundStocker()));
        }
        /*-------------------------*/
        /*   Get Cassette Object   */
        /*-------------------------*/
        CimCassette aCassette = baseCoreFactory.getBO(CimCassette.class, carrierID);
        Validations.check(aCassette == null, new OmCode(retCodeConfig.getNotFoundCassette(), ObjectIdentifier.fetchValue(carrierID)));
        /*------------------------------------*/
        /*   Check In-Parm's Validity Check   */
        /*------------------------------------*/
        String transferStatus = xferCassette.getTransferStatus();
        if (CimStringUtils.equals(transferStatus, BizConstant.SP_TRANSSTATE_EQUIPMENTIN)
                || CimStringUtils.equals(transferStatus, BizConstant.SP_TRANSSTATE_EQUIPMENTOUT)) {
            Validations.check(aMachine == null, retCodeConfig.getInvalidDataCombinAtion());
        } else {
            Validations.check(aStocker == null, retCodeConfig.getInvalidDataCombinAtion());
        }
        if (CimStringUtils.equals(transferStatus, BizConstant.SP_TRANSSTATE_STATIONIN)
                || CimStringUtils.equals(transferStatus, BizConstant.SP_TRANSSTATE_STATIONOUT)
                || CimStringUtils.equals(transferStatus, BizConstant.SP_TRANSSTATE_BAYIN)
                || CimStringUtils.equals(transferStatus, BizConstant.SP_TRANSSTATE_BAYOUT)
                || CimStringUtils.equals(transferStatus, BizConstant.SP_TRANSSTATE_MANUALIN)
                || CimStringUtils.equals(transferStatus, BizConstant.SP_TRANSSTATE_MANUALOUT)
                || CimStringUtils.equals(transferStatus, BizConstant.SP_TRANSSTATE_EQUIPMENTIN)
                || CimStringUtils.equals(transferStatus, BizConstant.SP_TRANSSTATE_EQUIPMENTOUT)
                || CimStringUtils.equals(transferStatus, BizConstant.SP_TRANSSTATE_SHELFIN)
                || CimStringUtils.equals(transferStatus, BizConstant.SP_TRANSSTATE_SHELFOUT)
                || CimStringUtils.equals(transferStatus, BizConstant.SP_TRANSSTATE_INTERMEDIATEIN)
                || CimStringUtils.equals(transferStatus, BizConstant.SP_TRANSSTATE_INTERMEDIATEOUT)
                || CimStringUtils.equals(transferStatus, BizConstant.SP_TRANSSTATE_ABNORMALIN)
                || CimStringUtils.equals(transferStatus, BizConstant.SP_TRANSSTATE_ABNORMALOUT)) {
            log.info("transferStatus == SI, SO, BI, BO, MI, MO, EI, EO, HI, HO, II, IO, AI, AO");
        } else {
            log.info("transferStatus != SI, SO, BI, BO, MI, MO, EI, EO, HI, HO, II, IO, AI, AO");
            throw new ServiceException(retCodeConfig.getInvalidDataCombinAtion());
        }
        /*-----------------------------------*/
        /*   Check Transition from EI case   */
        /*-----------------------------------*/
        String curState = aCassette.getTransportState();
        if (CimStringUtils.equals(curState, BizConstant.SP_TRANSSTATE_EQUIPMENTIN)) {
            Validations.check(!CimStringUtils.equals(transferStatus, BizConstant.SP_TRANSSTATE_EQUIPMENTOUT),
                    new OmCode(retCodeConfig.getInvalidCassetteTransferState(), curState, carrierID.getValue()));
        }
        /*---------------------------------------*/
        /*   Check Cassette's XferChgTimeStamp   */
        /*---------------------------------------*/
        String xferStatChgTimeStamp = CimDateUtils.getTimestampAsString(aCassette.getTransferStatusChangedTimeStamp());
        //----------------------------------------
        //   If EI/EO case, change Forcely.
        //   Otherwise, must compare timestama
        //----------------------------------------
        if (!CimStringUtils.equals(transferStatus, BizConstant.SP_TRANSSTATE_EQUIPMENTIN)
                && !CimStringUtils.equals(transferStatus, BizConstant.SP_TRANSSTATE_EQUIPMENTOUT)) {
            if (CimDateUtils.compare(transferStatusChangeTimeStamp, xferStatChgTimeStamp) < 0) {
                log.info("XferStat is not updated. because it is old event!!");
                return;
            }
        }
        aCassette.setTransferStatusChangedTimeStamp(transferStatusChangeTimeStamp);
        /*---------------------------------------*/
        /*   Update Cassette's Transfer Status   */
        /*---------------------------------------*/
        aCassette.setTransportState(transferStatus);
        /*---------------------------*/
        /*   Set AssignedToMachine   */
        /*---------------------------*/
        if (CimStringUtils.equals(transferStatus, BizConstant.SP_TRANSSTATE_EQUIPMENTIN)
                || CimStringUtils.equals(transferStatus, BizConstant.SP_TRANSSTATE_EQUIPMENTOUT)) {
            aCassette.assignToMachine(aMachine);
            aCassette.setShelfPosition(0, 0, 0); //for e-rack, add by nyx
        } else {
            aCassette.assignToMachine(aStocker);
            if (CimObjectUtils.isEmpty(shelfPosition)) {
                aCassette.setShelfPosition(0, 0, 0); //for e-rack, add by nyx
            } else {
                int shelfPositionX = shelfPosition.getShelfPositionX();
                int shelfPositionY = shelfPosition.getShelfPositionY();
                int shelfPositionZ = shelfPosition.getShelfPositionZ();
                aCassette.setShelfPosition(shelfPositionX, shelfPositionY, shelfPositionZ); //for e-rack, add by nyx
                xferCassette.setXPosition(String.valueOf(shelfPositionX));
                xferCassette.setYPosition(String.valueOf(shelfPositionY));
                xferCassette.setZPosition(String.valueOf(shelfPositionZ));
            }
        }
        /*--------------------------*/
        /*  Set Claimed Time Stamp  */
        /*--------------------------*/
        aCassette.setLastClaimedTimeStamp(objCommon.getTimeStamp().getReportTimeStamp());
        CimPerson aPerson = baseCoreFactory.getBO(CimPerson.class, objCommon.getUser().getUserID());
        Validations.check(aPerson == null, new OmCode(retCodeConfig.getNotFoundPerson(), objCommon.getUser().getUserID().getValue()));
        aCassette.setLastClaimedPerson(aPerson);
    }

    @Override
    public List<Infos.LotWaferMap> cassetteScrapWaferSelectDR(Infos.ObjCommon objCommon, List<ObjectIdentifier> cassetteIDList) {
        List<Infos.LotWaferMap> lotWaferMapList = new ArrayList<>();
        String maxWaferInLot = StandardProperties.OM_MAX_WAFER_COUNT_FOR_LOT.getValue();
        int maxInAlot = CimNumberUtils.intValue(maxWaferInLot);
        //------------------------------------------------------------
        // Getting Scraped wafers from each carriers
        //-------------------------------------------------------------
        if (!CimObjectUtils.isEmpty(cassetteIDList)) {
            for (ObjectIdentifier cassetteID : cassetteIDList) {
                String querySql = "SELECT\n" +
                        "                OMWAFER.LOT_ID,\n" +
                        "                OMWAFER.WAFER_ID,\n" +
                        "                OMWAFER.POSITION\n" +
                        "            FROM\n" +
                        "                OMCARRIER,\n" +
                        "                OMCARRIER_WAFER,\n" +
                        "                OMWAFER\n" +
                        "            WHERE\n" +
                        "                OMCARRIER.CARRIER_ID = ?\n" +
                        "            AND\n" +
                        "                OMCARRIER.ID = OMCARRIER_WAFER.REFKEY\n" +
                        "            AND\n" +
                        "                OMCARRIER_WAFER.WAFER_RKEY = OMWAFER.ID\n" +
                        "            AND\n" +
                        "                OMWAFER.SCRAP_STATE = ? ";
                List<Object[]> queryResult = cimJpaRepository.query(querySql, cassetteID.getValue(), BizConstant.SP_SCRAPSTATE_SCRAP);
                if (!CimObjectUtils.isEmpty(queryResult)) {
                    for (int i = 0; i < Math.min(maxInAlot, queryResult.size()); i++) {
                        Object[] object = queryResult.get(i);
                        Infos.LotWaferMap lotWaferMap = new Infos.LotWaferMap();
                        lotWaferMapList.add(lotWaferMap);
                        lotWaferMap.setCassetteID(new ObjectIdentifier(cassetteID.getValue()));
                        lotWaferMap.setLotID(new ObjectIdentifier((String) object[0]));
                        lotWaferMap.setWaferID(new ObjectIdentifier((String) object[1]));
                        lotWaferMap.setSlotNumber(CimNumberUtils.longValue((Number) object[2]));
                    }
                }
            }
        }
        return lotWaferMapList;
    }

    @Override
    public Boolean cassetteInPostProcessFlagGet(Infos.ObjCommon objCommon, ObjectIdentifier cassetteID) {
        //Initialize
        //step1 Check input parameter
        Validations.check(ObjectIdentifier.isEmpty(cassetteID), retCodeConfig.getInvalidInputParam());
        //step2 Convert cassetteID to cassette object
        CimCassette aCassette = baseCoreFactory.getBO(CimCassette.class, cassetteID);
        Validations.check(null == aCassette, new OmCode(retCodeConfig.getNotFoundCassette(), cassetteID.getValue()));
        // step3 Get InPostProcessFlag of cassette
        return aCassette.isPostProcessFlagOn();
    }


    @Override
    public String cassetteTransferStateGet(Infos.ObjCommon objCommon, ObjectIdentifier cassetteID) {
        com.fa.cim.newcore.bo.durable.CimCassette aCassette = baseCoreFactory.getBO(com.fa.cim.newcore.bo.durable.CimCassette.class, cassetteID);
        if (null == aCassette) {
            log.info(String.format("can't found the cassette object by cassetteID which is %s...", cassetteID));
            throw new ServiceException(new OmCode(retCodeConfig.getNotFoundCassette()));
        }
        return aCassette.getTransportState();
    }

    @Override
    public void cassetteCheckConditionForOperation(Infos.ObjCommon objCommon, ObjectIdentifier eqpID, String portGroupID,
                                                   List<Infos.StartCassette> startCassetteList, String operation) {
        String strSearchCondition = StandardProperties.OM_CONSTRAINT_CHK_WITH_SEARCH.getValue();
        long searchSearchCondition = 0;
        if (!CimStringUtils.isEmpty(strSearchCondition)) {
            searchSearchCondition = Long.parseLong(strSearchCondition);
        }

        //【step1】check virtual operation by start cassette (line: 260 - 280)
        log.debug("【step1】check virtual operation by start cassette");
        boolean virtualOperationFlag = false;
        if (CimStringUtils.isEmpty(portGroupID) && CimStringUtils.equals(BizConstant.SP_OPERATION_OPESTART, operation)) {
            virtualOperationFlag = virtualOperationMethod.virtualOperationCheckByStartCassette(objCommon, startCassetteList);
        }

        /**********************************************************************************************/
        /*【step2】Check Condition For All Cassettes                                                   */
        /*  The following conditions are checked by this object                                       */
        /*  - controlJobID、multiLotType、transferState、transferReserved                             */
        /*  - dispatchSate、cassetteState、and loadingSequenceNumber for all cassette                 */
        /**********************************************************************************************/
        int lenCassette = CimArrayUtils.getSize(startCassetteList);
        log.debug("【step2】Check Condition For All Cassettes");

        //【step2-1】get eqp object
        log.debug("【step2-1】get eqp object");
        CimMachine aMachine = baseCoreFactory.getBO(CimMachine.class, eqpID);
        Validations.check(null == aMachine, retCodeConfig.getNotFoundEqp(), eqpID);

        //【step2-2】get eqp's multiRecipeCapability
        log.debug("【step2-2】get eqp's multiRecipeCapability");
        String multipleRecipeCapability = aMachine.getMultipleRecipeCapability();

        //【step2-3】get eqp's operation mode info
        log.debug("【step2-3】get eqp's operation mode info");
        if (CimStringUtils.equals(BizConstant.SP_OPERATION_OPESTART, operation)
                || CimStringUtils.equals(BizConstant.SP_OPERATION_STARTRESERVATION, operation)) {
            log.debug("operation == SP_Operation_OpeStart, SP_Operation_StartReservation");
            if (!CimBooleanUtils.isTrue(virtualOperationFlag)) {
                portMethod.portResourceCurrentOperationModeGet(objCommon, eqpID, startCassetteList.get(0).getLoadPortID());
            }
        } else {
            log.debug("operation != SP_Operation_OpeStart && operation != SP_Operation_StartReservation");
        }

        //【step2-4】check cassette condition
        log.debug("【step2-4】check cassette condition");
        int mBak = 0;   // mBak = m
        long startCassetteSize = startCassetteList.size();
//        String saveControlJobID = null;
        ObjectIdentifier saveControlJobID = null;
        log.debug("check cassette condition, startCassetteSize = %d", startCassetteSize);
        String strSequenceCondition = StandardProperties.OM_CARRIER_LOAD_SEQ_CHK.getValue();
        long sequenceCondition = 0;
        if (!CimStringUtils.isEmpty(strSequenceCondition)) {
            sequenceCondition = Long.parseLong(strSequenceCondition);
        }
        boolean cassetteLoadingCheck = false;
        if (0 == sequenceCondition && (CimStringUtils.equals(BizConstant.SP_OPERATION_OPESTART, operation)
                || CimStringUtils.equals(BizConstant.SP_OPERATION_STARTRESERVATION, operation))) {
            log.debug("operation == SP_Operation_OpeStart) || (operation == SP_Operation_StartReservation");
            cassetteLoadingCheck = true;
        }
        if (1 == sequenceCondition && CimStringUtils.equals(BizConstant.SP_OPERATION_STARTRESERVATION, operation)) {
            log.debug("1 == sequenceCondition && operation == SP_Operation_StartReservation)");
            cassetteLoadingCheck = true;
        }

        for (int i = 0; i < startCassetteSize; i++) {
            Infos.StartCassette startCassette = startCassetteList.get(i);
            //【step2-4-1】check SorterJob existence
            log.debug("【step2-4-1】 check SorterJob Existence");
            if (CimStringUtils.equals(BizConstant.SP_OPERATION_NPWCARRIERXFER, operation)
                    || CimStringUtils.equals(BizConstant.SP_OPERATION_STARTRESERVATION, operation)
                    || CimStringUtils.equals(BizConstant.SP_OPERATION_OPESTART, operation)) {
                Infos.EquipmentLoadPortAttribute equipmentLoadPortAttribute = new Infos.EquipmentLoadPortAttribute();
                List<Infos.CassetteLoadPort> cassetteLoadPortList = new ArrayList<>();
                Infos.CassetteLoadPort cassetteLoadPort = new Infos.CassetteLoadPort();
                cassetteLoadPort.setPortID(startCassette.getLoadPortID());
                cassetteLoadPort.setCassetteID(startCassette.getCassetteID());
                cassetteLoadPortList.add(cassetteLoadPort);
                equipmentLoadPortAttribute.setCassetteLoadPortList(cassetteLoadPortList);
                equipmentLoadPortAttribute.setEquipmentID(eqpID);

                Inputs.ObjWaferSorterJobCheckForOperation in = new Inputs
                        .ObjWaferSorterJobCheckForOperation();
                in.setEquipmentLoadPortAttribute(equipmentLoadPortAttribute);
                List<ObjectIdentifier> cassetteIDList = new ArrayList<>();
                cassetteIDList.add(startCassette.getCassetteID());
                in.setCassetteIDList(cassetteIDList);
                in.setOperation(operation);
                waferMethod.waferSorterSorterJobCheckForOperation(objCommon, in);
            }

            //【step2-4-2】check start cassette's loading order
            log.debug("【step2-4-2】check start cassette's loading order");
            if (cassetteLoadingCheck && !virtualOperationFlag) {
                if (startCassette.getLoadSequenceNumber() != (i + 1)) {
                    log.error("strStartCassette[i].loadSequenceNumber != i");
                    throw new ServiceException(new OmCode(retCodeConfig.getInvalidLoadingSequence(), startCassette.getCassetteID().getValue()));
                }
            }

            //get cassette object
            log.debug("get cassette object");
            com.fa.cim.newcore.bo.durable.CimCassette aCassette = baseCoreFactory.getBO(com.fa.cim.newcore.bo.durable.CimCassette.class, startCassette.getCassetteID());
            Validations.check(null == aCassette, new OmCode(retCodeConfig.getNotFoundCassette(), startCassette.getCassetteID().getValue()));

            //get and check controlJobID, all of startCassetteList[i].getCassetteID() must be equal.
            log.debug("get and check controlJobID");
            com.fa.cim.newcore.bo.product.CimControlJob aControlJob = aCassette.getControlJob();
            if (CimStringUtils.equals(BizConstant.SP_OPERATION_OPESTART, operation)) {
                log.debug("operation == SP_Operation_OpeStart");
                if (0 == i) {
                    saveControlJobID = CimObjectUtils.isEmpty(aControlJob) ? null : aControlJob.getControlJobID();
                } else {
                    //String cassetteControlJobID = cassette.getControlJobID();
                    ObjectIdentifier cassetteControlJobID = CimObjectUtils.isEmpty(aControlJob) ? null : aControlJob.getControlJobID();
                    Validations.check(!ObjectIdentifier.equalsWithValue(saveControlJobID, cassetteControlJobID), retCodeConfig.getCassetteControlJobMix());
                }
            } else {
                Validations.check(null != aControlJob, retCodeConfig.getCassetteControlJobFilled());
            }

            //get cassette's multiLotType
            log.debug("get cassette's multiLotType");
            String multiLotType = aCassette.getMultiLotType();
            //【step2-4-3】check multiRecipeCapability VS multiLotType
            log.debug("【step2-4-3】check multiRecipeCapability VS multiLotType");
            String loadPurposeType = startCassette.getLoadPurposeType();
            if (CimStringUtils.equals(BizConstant.SP_LOADPURPOSETYPE_EMPTYCASSETTE, loadPurposeType)) {
                log.debug("strStartCassette[i].loadPurposeType == SP_LoadPurposeType_EmptyCassette");
            } else if (CimStringUtils.equals(BizConstant.SP_EQP_MULTIRECIPECAPABILITY_MULTIPLERECIPE, multipleRecipeCapability)) {
                log.debug("multiRecipeCapability == SP_Eqp_MultiRecipeCapability_A");
            } else if (CimStringUtils.equals(BizConstant.SP_EQP_MULTIRECIPECAPABILITY_SINGLERECIPE, multipleRecipeCapability)) {
                log.debug("multiRecipeCapability == SP_Eqp_MultiRecipeCapability_B");
            } else if (CimStringUtils.equals(BizConstant.SP_EQP_MULTIRECIPECAPABILITY_BATCH, multipleRecipeCapability)) {
                log.debug("multiRecipeCapability == SP_Eqp_MultiRecipeCapability_C");
                if (CimStringUtils.equals(BizConstant.SP_OPERATION_NPWCARRIERXFER, operation)) {
                    log.debug("operation == SP_Operation_NPWCarrierXfer");
                } else if (CimStringUtils.equals(BizConstant.SP_CAS_MULTILOTTYPE_SINGLELOTSINGLERECIPE, multiLotType)
                        || CimStringUtils.equals(BizConstant.SP_CAS_MULTILOTTYPE_MULTILOTSINGLERECIPE, multiLotType)) {
                    log.debug("multiLotType ==  SP_Cas_MultiLotType_SingleLotSingleRecipe || SP_Cas_MultiLotType_MultiLotSingleRecipe");
                } else {
                    log.error("return RC_CAST_EQP_CONDITION_ERROR!!");
                    throw new ServiceException(retCodeConfig.getCassetteEquipmentConditionError());
                }
            } else {
                log.debug("No Process <Check MultiRecipeCapability VS MultiLotType>");
            }

            //【step2-4-4】check cassette's transfer status
            log.debug("【step2-4-4】check cassette's transfer status");
            String transferState = aCassette.getTransportState();
            boolean transferReserved = aCassette.isReserved();
            // For Operation Start
            if (CimStringUtils.equals(BizConstant.SP_OPERATION_OPESTART, operation)) {
                if (CimStringUtils.equals(BizConstant.SP_TRANSSTATE_EQUIPMENTIN, transferState)) {
                    log.debug("transferState == SP_TransState_EquipmentIn");
                } else {
                    Validations.check(!virtualOperationFlag, retCodeConfig.getInvalidCassetteTransferState(), transferState, startCassette.getCassetteID());
                }
            } else if (CimStringUtils.equals(BizConstant.SP_OPERATION_FLOWBATCHING, operation)) {
                log.debug("operation = SP_Operation_FlowBatching");
                if (!transferReserved && (CimStringUtils.equals(BizConstant.SP_TRANSSTATE_STATIONIN, transferState)
                        || CimStringUtils.equals(BizConstant.SP_TRANSSTATE_BAYIN, transferState)
                        || CimStringUtils.equals(BizConstant.SP_TRANSSTATE_MANUALIN, transferState))) {
                    log.debug("rc == success");
                } else {
                    throw new ServiceException(new OmCode(retCodeConfig.getInvalidCassetteTransferState(), transferState, startCassette.getCassetteID().getValue()));
                }
            } else if (CimStringUtils.equals(BizConstant.SP_OPERATION_STARTRESERVATION, operation)
                    || CimStringUtils.equals(BizConstant.SP_OPERATION_NPWCARRIERXFER, operation)) {
                log.debug("operation = SP_Operation_StartReservation");

                Infos.EqpPortStatus orgEqpPortStatus = null;
                CimMachine aOrgMachine = null;
                Outputs.ObjPortResourceCurrentOperationModeGetOut modeGetOut =
                        portMethod.portResourceCurrentOperationModeGet(objCommon, eqpID, startCassette.getLoadPortID());

                if (CimStringUtils.equals(BizConstant.SP_TRANSSTATE_EQUIPMENTIN, transferState)) {
                    //get originator eqp's info
                    log.debug("get originator eqp");
                    Machine aMachine1 = aCassette.currentAssignedMachine();
                    if (null != aMachine1) {
                        Boolean isStorageBool = aMachine1.isStorageMachine();
                        if (!isStorageBool) {
                            aOrgMachine = (CimMachine) aMachine1;
                            log.debug("isStorageBool is not TRUE: eqp. So narrow to PosMachine");
                        } else {
                            log.debug("isStorageBool is TRUE: Storage");
                        }
                    }
                    Validations.check(null == aOrgMachine, new OmCode(retCodeConfig.getNotFoundEqp(), eqpID.getValue()));

                    ObjectIdentifier originalEquipmentID = new ObjectIdentifier(aOrgMachine.getIdentifier(), aOrgMachine.getPrimaryKey());
                    //get cassette info in original eqp
                    log.debug("get cassette info in original eqp");
                    Infos.EqpPortInfo eqpPortInfo;
                    String equipmentCategory = aOrgMachine.getCategory();
                    if (CimStringUtils.equals(BizConstant.SP_MC_CATEGORY_INTERNALBUFFER, equipmentCategory)) {
                        log.debug("equipmentCategory is [InternalBuffer]");
                        //line:rc = equipment_portInfoForInternalBuffer_GetDR(...) - line:778 -787
                        eqpPortInfo = equipmentMethod.equipmentPortInfoForInternalBufferGetDR(objCommon, originalEquipmentID);
                    } else {
                        log.debug("equipmentCategory is not [InternalBuffer]");
                        eqpPortInfo = equipmentMethod.equipmentPortInfoGet(objCommon, originalEquipmentID);
                    }
                    //find assigned port's portGroupID
                    log.debug("find assigned port's portGroupID");
                    boolean foundFlag = false;
                    for (Infos.EqpPortStatus eqpPortStatus : eqpPortInfo.getEqpPortStatuses()) {
                        if (ObjectIdentifier.equalsWithValue(startCassette.getCassetteID(),
                                eqpPortStatus.getLoadedCassetteID())) {
                            orgEqpPortStatus = eqpPortStatus;
                            foundFlag = true;
                            break;
                        }
                    }
                    Validations.check(!foundFlag, retCodeConfig.getInvalidCassetteTransferState(), transferState, startCassette.getCassetteID());
                }

                if (CimStringUtils.equals(BizConstant.SP_EQP_ACCESSMODE_MANUAL, modeGetOut.getOperationMode().getAccessMode())) {
                    log.debug("when TransferStatus is EI, AccessMode makes it an error with Manual");
                    if (CimStringUtils.equals(BizConstant.SP_TRANSSTATE_EQUIPMENTIN, transferState)) {
                        log.debug("transferState is [EI]");
                        //permit Carrier which a person can deliver in StartLotReserve.
                        //as for the condition, OperationMode is "***-1" and XferState is "EI".
                        if (!CimStringUtils.equals(BizConstant.SP_EQP_ACCESSMODE_MANUAL, orgEqpPortStatus.getAccessMode())
                                || ObjectIdentifier.isEmptyWithValue(orgEqpPortStatus.getLoadedCassetteID())) {
                            log.error("##### return RC_INVALID_CAST_XFERSTAT");
                            throw new ServiceException(new OmCode(retCodeConfig.getInvalidCassetteTransferState(), transferState, startCassette.getCassetteID().getValue()));
                        }
                    }
                } else {
                    boolean reRouteFlag = false;
                    String reRouteXferFlag = StandardProperties.OM_XFER_REROUTE_FLAG.getValue();
                    String operationMode = modeGetOut.getOperationMode().getOperationMode().getValue();
                    log.debug("OperationMode:%s", operationMode);
                    if (CimStringUtils.equals(reRouteXferFlag, "1")
                            && CimStringUtils.equals(BizConstant.SP_EQP_PORT_OPERATIONMODE_AUTO_3, operationMode)
                            && !transferReserved
                            && (CimStringUtils.equals(BizConstant.SP_TRANSSTATE_STATIONIN, transferState)
                            || CimStringUtils.equals(BizConstant.SP_TRANSSTATE_BAYIN, transferState)
                            || CimStringUtils.equals(BizConstant.SP_TRANSSTATE_MANUALIN, transferState)
                            || CimStringUtils.equals(BizConstant.SP_TRANSSTATE_BAYOUT, transferState))) {
                        log.debug("operationMode is Auto-3");
                        log.debug("transferState = [SI], [BI], [MI], [BO] and transferReserved is FALSE");
                    } else if ((CimStringUtils.equals(BizConstant.SP_TRANSSTATE_STATIONIN, transferState)
                            || CimStringUtils.equals(BizConstant.SP_TRANSSTATE_BAYIN, transferState)
                            || CimStringUtils.equals(BizConstant.SP_TRANSSTATE_MANUALIN, transferState))
                            && !transferReserved) {
                        log.debug("transferState = [SI], [BI], [MI] and transferReserved is FALSE");
                    } else if ((CimStringUtils.equals(BizConstant.SP_TRANSSTATE_STATIONOUT, transferState)
                            || CimStringUtils.equals(BizConstant.SP_TRANSSTATE_EQUIPMENTOUT, transferState)
                            && !transferReserved)) {
                        log.debug("transferState = [SO], [EO], and transferReserved is FALSE");
                        //【step2-4-6】check transfer job existence (line:919 - )
                        Infos.CarrierJobResult jobRecordGetOut = null;
                        try {
                            jobRecordGetOut = this.cassetteTransferJobRecordGetDR(objCommon, startCassette.getCassetteID());
                        } catch (ServiceException e) {
                            if (Validations.isEquals(retCodeConfig.getCarrierNotTransfering(), e.getCode())) {
                                throw new ServiceException(new OmCode(retCodeConfig.getInvalidCassetteTransferState(), transferState, startCassette.getCassetteID().getValue()));
                            } else {
                                throw e;
                            }
                        }
                        // call stocker_type_GetDR (line: 941 - 953)
                        try {
                            stockerComp.stockerTypeGet(objCommon, jobRecordGetOut.getToMachine());
                        } catch (ServiceException e) {
                            if (Validations.isEquals(retCodeConfig.getUndefinedStockerType(), e.getCode())) {
                                throw new ServiceException(new OmCode(retCodeConfig.getInvalidCassetteTransferState(), transferState, startCassette.getCassetteID().getValue()));
                            } else {
                                throw e;
                            }
                        }
                    } else if (CimStringUtils.equals(BizConstant.SP_TRANSSTATE_EQUIPMENTIN, transferState) && !transferReserved) {
                        /*-----------------------------------------------------------------------------------------------*/
                        /*   The following StartLotReserve isn't permitted in Transfer EQP to EQP of CassetteDelivery.   */
                        /*                                                                                               */
                        /*   -----------------------------                                                               */
                        /*   |         FromEQP           |                                                               */
                        /*   ----------------------------|                                                               */
                        /*   | OperationMode : Offline-2 |                                                               */
                        /*   | XferState     : EI        |                                                               */
                        /*   -----------------------------                                                               */
                        /*-----------------------------------------------------------------------------------------------*/
                        log.debug("(transferState = SP_TransState_EquipmentIn) and (transferReserved == FALSE)");
                        if (CimStringUtils.equals(BizConstant.SP_EQP_ACCESSMODE_AUTO, orgEqpPortStatus.getAccessMode())
                                && CimStringUtils.equals(BizConstant.SP_EQP_ONLINEMODE_OFFLINE, orgEqpPortStatus.getOnlineMode())) {
                            throw new ServiceException(new OmCode(retCodeConfig.getInvalidCassetteTransferState(), transferState, startCassette.getCassetteID().getValue()));
                        }
                        /*-----------------------------------------------------------------------------------------------*/
                        /*   The following StartLotReserve isn't permitted in Transfer EQP to EQP of CassetteDelivery.   */
                        /*                                                                                               */
                        /*   ToEQP's OperationMode : ***-2                                                               */
                        /*-----------------------------------------------------------------------------------------------*/
                        if (CimStringUtils.equals(BizConstant.SP_EQP_ACCESSMODE_AUTO, modeGetOut.getOperationMode().getAccessMode())
                                || CimStringUtils.equals(BizConstant.SP_EQP_DISPATCHMODE_AUTO, modeGetOut.getOperationMode().getDispatchMode())) {
                            throw new ServiceException(new OmCode(retCodeConfig.getInvalidCassetteTransferState(), transferState, startCassette.getCassetteID().getValue()));
                        }

                        //【step2-4-7】Check orgEqp's EqpToEqpTransfer Flag is TRUE or Not
                        log.debug("【step2-4-7】Check orgEqp's EqpToEqpTransfer Flag is TRUE or not");
                        boolean eqpToEqpXferFlag = aOrgMachine.isEqpToEqpTransferFlagOn();
                        if (!eqpToEqpXferFlag) {
                            throw new ServiceException(new OmCode(retCodeConfig.getInvalidCassetteTransferState(), transferState, startCassette.getCassetteID().getValue()));
                        }
                    } else {
                        throw new ServiceException(new OmCode(retCodeConfig.getInvalidCassetteTransferState(), transferState, startCassette.getCassetteID().getValue()));
                    }
                }
            }
            //get cassette's dispatch status
            boolean dispatchReserveFlag = aCassette.isDispatchReserved();
            if (CimStringUtils.equals(BizConstant.SP_OPERATION_STARTRESERVATION, operation)
                    || CimStringUtils.equals(BizConstant.SP_OPERATION_FLOWBATCHING, operation)
                    || CimStringUtils.equals(BizConstant.SP_OPERATION_NPWCARRIERXFER, operation)) {
                Validations.check(dispatchReserveFlag, retCodeConfig.getAlreadyDispatchReservedCassette());
            }
            //get and check cassette's status
            String cassetteState = aCassette.getDurableState();
            if (CimStringUtils.equals(BizConstant.SP_OPERATION_NPWCARRIERXFER, operation)
                    && (CimStringUtils.equals(BizConstant.SP_LOADPURPOSETYPE_OTHER, startCassette.getLoadPurposeType())
                    || CimStringUtils.equals(BizConstant.SP_LOADPURPOSETYPE_EMPTYCASSETTE, startCassette.getLoadPurposeType()))) {
            } else if (CimStringUtils.equals(BizConstant.SP_OPERATION_OPESTART, operation)
                    || CimStringUtils.equals(BizConstant.SP_OPERATION_STARTRESERVATION, operation)) {
                log.debug("operation == SP_Operation_OpeStart or SP_Operation_StartReservation");
                Set<String> subLotTypeSet = new HashSet<>();
                // for empty carrier
                if (CimStringUtils.equals(startCassetteList.get(i).getLoadPurposeType(), BizConstant.SP_LOADPURPOSETYPE_EMPTYCASSETTE)) {
                    log.info("Empty carrier");
                    int startCastLen = CimArrayUtils.getSize(startCassetteList);
                    for (int startCastIndex = 0; startCastIndex < startCastLen; startCastIndex++) {
                        List<Infos.LotInCassette> lotInCassetteList = startCassetteList.get(startCastIndex).getLotInCassetteList();
                        int lotInCastLen = CimArrayUtils.getSize(lotInCassetteList);
                        for (int lotInCastIndex = 0; lotInCastIndex < lotInCastLen; lotInCastIndex++) {
                            if (lotInCassetteList.get(lotInCastIndex).getMoveInFlag()) {
                                subLotTypeSet.add(lotInCassetteList.get(lotInCastIndex).getSubLotType());
                            }
                        }
                    }
                }
                // for lot in carrier
                else {
                    log.info("Lot in carrier");
                    // Get carrier's start lots SubLotType
                    List<Infos.LotInCassette> lotInCassetteList = startCassetteList.get(i).getLotInCassetteList();
                    int lotInCastLen = CimArrayUtils.getSize(lotInCassetteList);
                    for (int lotInCastIndex = 0; lotInCastIndex < lotInCastLen; lotInCastIndex++) {
                        if (lotInCassetteList.get(lotInCastIndex).getMoveInFlag()) {
                            subLotTypeSet.add(lotInCassetteList.get(lotInCastIndex).getSubLotType());
                        }
                    }
                }
                boolean availableFlag = aCassette.isLotProcessAvailable(new ArrayList<>(subLotTypeSet));
                Validations.check(!availableFlag, new OmCode(retCodeConfig.getDurableNotAvailableStateForLotProcess(), startCassette.getCassetteID().getValue()));
            } else {
                if (!CimStringUtils.equals(CIMStateConst.CIM_DURABLE_AVAILABLE, cassetteState)
                        && !CimStringUtils.equals(CIMStateConst.CIM_DURABLE_INUSE, cassetteState)) {
                    throw new ServiceException(new OmCode(retCodeConfig.getInvalidCassetteState(), cassetteState, startCassette.getCassetteID().getValue()));
                }
            }

            //【step2-4-9】check start cassette and start lot combination - line: 1352 - 1495
            log.debug("【step2-4-9】check start cassette and start lot combination");
            if (CimStringUtils.equals(BizConstant.SP_OPERATION_OPESTART, operation)
                    || CimStringUtils.equals(BizConstant.SP_OPERATION_STARTRESERVATION, operation)) {
                log.debug("Start cassette and Start lot Combination Check");
                List<Lot> lotList = aCassette.allLots();
                if (CimArrayUtils.getSize(lotList) == CimArrayUtils.getSize(startCassette.getLotInCassetteList())) {
                    log.debug("lot count is maching, the size:{}", lotList.size());

                    // all lot in start cassette loop - line:1383 - 1483
                    //【bear】from the line:1383 - 1483, each lot of lotList must be match in lotInCassetteList, otherwise return lot_start_cassette_unmatch
                    boolean lotMatchFlag = false;
                    for (Lot lot : lotList) {
                        Validations.check(null == lot, new OmCode(retCodeConfig.getLotCastUnmatch(), "*****", startCassette.getCassetteID().getValue()));
                        String lotID = lot.getIdentifier();
                        for (Infos.LotInCassette lotInCassette : startCassette.getLotInCassetteList()) {
                            if (CimStringUtils.equals(lotID, lotInCassette.getLotID().getValue())) {
                                log.debug("find matching lot");
                                lotMatchFlag = true;
                                break;
                            }
                        }
                        Validations.check(!lotMatchFlag, retCodeConfig.getLotStartCassetteUnMatch());
                    }

                    // check cassette & lot combination - line: 1436 - 1483
                    // inpara lot -> lot in cassette
                    //【bear】 it's the same with all lot in start cassette loop, so we can't do once again.
                } else {
                    log.error("lot in start cassette miss match");
                    throw new ServiceException(retCodeConfig.getLotStartCassetteUnMatch());
                }
            }
        }
        //【step2-4-10】check cassette's controJobID vs eqp's controlJobID - line:1502 - 1660
        log.debug("【step2-4-10】check cassette's controJobID vs eqp's controlJobID");
        if (CimStringUtils.equals(BizConstant.SP_OPERATION_OPESTART, operation)) {
            List<Infos.StartReservedControlJobInfo> startReservedControlJobInfos = equipmentMethod.equipmentReservedControlJobIDGet(objCommon, eqpID);
            ObjectIdentifier eqpControlJobID = null;
            List<ProductDTO.PosStartCassetteInfo> startCassetteInfo = null;
            List<ProductDTO.PosStartCassetteInfo> startCassetteInfoVar = null;
            int reservedControlJobLen = (null == startReservedControlJobInfos ? 0 : startReservedControlJobInfos.size());
            for (int j = 0; j < reservedControlJobLen; j++) {
                Infos.StartReservedControlJobInfo startReservedControlJobInfo = startReservedControlJobInfos.get(j);
                if (CimStringUtils.equals(startReservedControlJobInfo.getPortGroupID(), portGroupID)) {
                    log.debug("in-parm's portGroup is found in reservedControlJobInfo...");
                    eqpControlJobID = startReservedControlJobInfo.getControlJobID();

                    //get controlJob object
                    com.fa.cim.newcore.bo.product.CimControlJob reserveControlJob = baseCoreFactory.getBO(com.fa.cim.newcore.bo.product.CimControlJob.class, eqpControlJobID);
                    Validations.check(null == reserveControlJob, retCodeConfig.getNotFoundControlJob());
                    startCassetteInfo = reserveControlJob.getStartCassetteInfo();
                    startCassetteInfoVar = startCassetteInfo;
                    break;
                }
            }

            // compare reserved controlJobID vs cassette's controlJobID - line:1573 - 1583
            log.debug("compare reserved controlJobID vs cassette's controlJobID");
            Validations.check(!ObjectIdentifier.equalsWithValue(saveControlJobID, eqpControlJobID), retCodeConfig.getCassettePortControlJobUnMatch());
            /*===== check reserved controlJobID's cassette count vs in-parm's cassette count =====*/
            if (!ObjectIdentifier.isEmptyWithValue(saveControlJobID)) {
                Validations.check(CimArrayUtils.getSize(startCassetteList) != CimArrayUtils.getSize(startCassetteInfo), retCodeConfig.getCastPortCtrljobCountUnmatch());
            }
            // check reserved controlJobID's StartCassetteInfo vs in-parm's StartCassetteInfo - line: 1605 - 1655
            log.debug("check reserved controlJobID's StartCassetteInfo vs in-parm's StartCassetteInfo");
            if (!CimArrayUtils.isEmpty(startCassetteInfo)) {
                log.debug("check reserved controlJobID's StartCassetteInfo vs in-parm's StartCassetteInfo");
                for (int j = 0; j < startCassetteSize; j++) {
                    Infos.StartCassette startCassetteTemp = startCassetteList.get(j);
                    int lotLen = null == startCassetteTemp.getLotInCassetteList()
                            ? 0 : startCassetteTemp.getLotInCassetteList().size();
                    for (int k = 0; k < lotLen; k++) {
                        Infos.LotInCassette lotInCassette = startCassetteTemp.getLotInCassetteList().get(k);
                        boolean sameConditionFlag = false;

                        for (ProductDTO.PosStartCassetteInfo startCassetteObj : startCassetteInfo) {
                            long controlLotLen = null == startCassetteObj.getLotInCassetteInfo()
                                    ? 0 : startCassetteObj.getLotInCassetteInfo().size();
                            for (int l = 0; l < controlLotLen; l++) {
                                ProductDTO.PosLotInCassetteInfo lotInCassetteObj = startCassetteObj.getLotInCassetteInfo().get(l);
                                if (CimStringUtils.equals(lotInCassette.getLotID().getValue(), lotInCassetteObj.getLotID().getValue())
                                        && (lotInCassette.getMoveInFlag().equals(lotInCassetteObj.isOperationStartFlag()))) {
                                    log.debug("found same condition lot.");
                                    sameConditionFlag = true;
                                    break;
                                }
                            }
                            if (sameConditionFlag) {
                                break;
                            }
                        }
                        Validations.check(!sameConditionFlag, new OmCode(retCodeConfig.getStartReserveControlJobOperationStateControlJobUnMatch(), eqpID.getValue(), eqpControlJobID.getValue()));
                    }
                }
            }

        } else {
            log.debug("operation != SP_Operation_OpeStart");
        }

        //【step2-4-11】check condition for maxBatchSize, minBatchSize, emptyCassetteCount
        log.debug("【step2-4-11】check condition for maxBatchSize, minBatchSize, emptyCassetteCount");
        if (CimStringUtils.equals(BizConstant.SP_OPERATION_OPESTART, operation)
                || CimStringUtils.equals(BizConstant.SP_OPERATION_STARTRESERVATION, operation)) {
            log.debug("operation = SP_Operation_OpeStart or SP_Operation_StartReservation");
            Outputs.ObjEquipmentProcessBatchConditionGetOut batchConditionGetOut = equipmentMethod.equipmentProcessBatchConditionGet(objCommon, eqpID);
            // check cassette count
            int processCassetteCount = 0;
            int emptyCassetteCount = 0;
            int lenCassetteSize = CimArrayUtils.getSize(startCassetteList);
            for (int j = 0; j < lenCassetteSize; j++) {
                Infos.StartCassette startCassette1 = startCassetteList.get(j);
                String loadPurposeType1 = startCassette1.getLoadPurposeType();
                if (CimStringUtils.equals(BizConstant.SP_LOADPURPOSETYPE_PROCESSLOT, loadPurposeType1)
                        || CimStringUtils.equals(BizConstant.SP_LOADPURPOSETYPE_PROCESSMONITORLOT, loadPurposeType1)) {
                    processCassetteCount++;
                } else if (CimStringUtils.equals(BizConstant.SP_LOADPURPOSETYPE_EMPTYCASSETTE, loadPurposeType1)) {
                    emptyCassetteCount++;
                }
            }
            if ((processCassetteCount >= batchConditionGetOut.getMinBatchSize())
                    && (processCassetteCount <= batchConditionGetOut.getMaxBatchSize())) {
                log.debug("result = ok");
            } else {
                Validations.check(CimBooleanUtils.isFalse(virtualOperationFlag), retCodeConfig.getInvalidInputCassetteCount());
            }
            if (CimBooleanUtils.isTrue(batchConditionGetOut.isCassetteExchangeFlag())
                    && CimBooleanUtils.isTrue(batchConditionGetOut.isMonitorCreationFlag())) {
                Validations.check(emptyCassetteCount != (processCassetteCount + 1),
                        new OmCode(retCodeConfig.getInvalidEmptyCount(), String.valueOf(emptyCassetteCount), String.valueOf(processCassetteCount + 1)));
            } else if (CimBooleanUtils.isTrue(batchConditionGetOut.isCassetteExchangeFlag())
                    && CimBooleanUtils.isFalse(batchConditionGetOut.isMonitorCreationFlag())) {
                boolean categoryChkMode = StandardProperties.OM_EXCHANGE_CARRIER_CATEGORY_CHK_MODE.isOn();
                if (categoryChkMode){
                    for (Infos.StartCassette startCassette : startCassetteList){
                        List<Infos.LotInCassette> lotInCassetteList = startCassette.getLotInCassetteList();
                        for (Infos.LotInCassette lotInCassette : lotInCassetteList){
                            if (lotInCassette.getMoveInFlag()){
                                ObjectIdentifier lotID = lotInCassette.getLotID();
                                CimLot aLot = baseCoreFactory.getBO(CimLot.class, lotID);
                                String requiredCassetteCategory = aLot.getRequiredCassetteCategory();
                                if (!CimStringUtils.isEmpty(requiredCassetteCategory)){
                                    if (requiredCassetteCategory.contains("2")){
                                        String[] split = requiredCassetteCategory.split("2",2);
                                        if (CimStringUtils.isEmpty(split[0]) || CimStringUtils.isEmpty(split[1])){
                                            processCassetteCount --;
                                            break;
                                        }
                                    }else {
                                        processCassetteCount --;
                                        break;
                                    }
                                }else {
                                    processCassetteCount --;
                                    break;
                                }
                            }
                        }
                    }
                    Validations.check(emptyCassetteCount != processCassetteCount,
                            new OmCode(retCodeConfig.getInvalidEmptyCount(), String.valueOf(emptyCassetteCount), String.valueOf(processCassetteCount)));
                }else {
                    Validations.check(emptyCassetteCount != processCassetteCount,
                            new OmCode(retCodeConfig.getInvalidEmptyCount(), String.valueOf(emptyCassetteCount), String.valueOf(processCassetteCount)));
                }
            } else if (CimBooleanUtils.isFalse(batchConditionGetOut.isCassetteExchangeFlag())
                    && CimBooleanUtils.isTrue(batchConditionGetOut.isMonitorCreationFlag())) {
                Validations.check(emptyCassetteCount != 1,
                        new OmCode(retCodeConfig.getInvalidEmptyCount(), String.valueOf(emptyCassetteCount), String.valueOf(1)));
            } else if (CimBooleanUtils.isFalse(batchConditionGetOut.isCassetteExchangeFlag())
                    && CimBooleanUtils.isFalse(batchConditionGetOut.isMonitorCreationFlag())) {
                Validations.check(emptyCassetteCount != 0,
                        new OmCode(retCodeConfig.getInvalidEmptyCount(), String.valueOf(emptyCassetteCount), String.valueOf(0)));
            }

        }
        /*------------------------------------------------------------------------------*/
        /*                                                                              */
        /*   Check Condition for Eqp's MultiRecipeCapability VS RecipeParameterValue    */
        /*                                                                              */
        /*------------------------------------------------------------------------------*/
        if (CimStringUtils.equals(operation, BizConstant.SP_OPERATION_OPESTART)
                || CimStringUtils.equals(operation, BizConstant.SP_OPERATION_STARTRESERVATION)) {
            if (CimStringUtils.equals(multipleRecipeCapability, BizConstant.SP_EQP_MULTIRECIPECAPABILITY_SINGLERECIPE)
                    || CimStringUtils.equals(multipleRecipeCapability, BizConstant.SP_EQP_MULTIRECIPECAPABILITY_BATCH)) {
                /*-----------------------------------*/
                /*   Work Valiable for Check Logic   */
                /*-----------------------------------*/
                boolean baseSetFlag = false;
                int baseI = 0;
                int baseJ = 0;
                int baseRPLen = 0;
                /*-------------------------------*/
                /*   Loop for strStartCassette   */
                /*-------------------------------*/
                for (int i = 0; i < lenCassette; i++) {
                    /*------------------------*/
                    /*   Omit EmptyCassette   */
                    /*------------------------*/
                    Infos.StartCassette startCassette = startCassetteList.get(i);
                    if (CimStringUtils.equals(startCassette.getLoadPurposeType(), BizConstant.SP_LOADPURPOSETYPE_EMPTYCASSETTE)) {
                        continue;
                    }
                    /*-------------------------------*/
                    /*   Loop for strLotInCassette   */
                    /*-------------------------------*/
                    List<Infos.LotInCassette> lotInCassetteList = startCassette.getLotInCassetteList();
                    int lenLotInCassette = CimArrayUtils.getSize(lotInCassetteList);
                    for (int j = 0; j < lenLotInCassette; j++) {
                        /*------------------------*/
                        /*   Omit Non-Start Lot   */
                        /*------------------------*/
                        if (!lotInCassetteList.get(j).getMoveInFlag()) {
                            continue;
                        }
                        /*-------------------------------------*/
                        /*   Check RecipeParameterChangeType   */
                        /*-------------------------------------*/
                        Validations.check(!CimStringUtils.equals(lotInCassetteList.get(j).getRecipeParameterChangeType(), BizConstant.SP_RPARM_CHANGETYPE_BYLOT),
                                new OmCode(retCodeConfig.getInvalidRecipeParamChangeType(), lotInCassetteList.get(j).getLotID().getValue(), startCassette.getCassetteID().getValue()));
                        /*--------------------*/
                        /*   Save Base Info   */
                        /*--------------------*/
                        if (!baseSetFlag) {
                            baseSetFlag = true;
                            baseI = i;
                            baseJ = j;
                            if (!CimArrayUtils.isEmpty(lotInCassetteList.get(j).getLotWaferList())) {
                                baseRPLen = CimArrayUtils.getSize(lotInCassetteList.get(j).getLotWaferList().get(0).getStartRecipeParameterList());
                            } else {
                                throw new ServiceException(new OmCode(retCodeConfig.getInvalidWaferCount(), lotInCassetteList.get(j).getLotID().getValue(), startCassette.getCassetteID().getValue()));
                            }
                        }
                        /*--------------------------*/
                        /*   Loop for strLotWafer   */
                        /*--------------------------*/
                        List<Infos.LotWafer> lotWaferList = lotInCassetteList.get(j).getLotWaferList();
                        int lwLen = CimArrayUtils.getSize(lotWaferList);
                        for (int k = 0; k < lwLen; k++) {
                            /*---------------------------------*/
                            /*   Check RecipeParameter Count   */
                            /*---------------------------------*/
                            List<Infos.StartRecipeParameter> startRecipeParameterList = lotWaferList.get(k).getStartRecipeParameterList();
                            int rpLen = CimArrayUtils.getSize(startRecipeParameterList);
                            Validations.check(rpLen != baseRPLen,
                                    new OmCode(retCodeConfig.getNotSameRecipeParamInfo(), lotInCassetteList.get(j).getLotID().getValue(), startCassette.getCassetteID().getValue(),
                                            startCassetteList.get(baseI).getLotInCassetteList().get(baseJ).getLotID().getValue(), startCassetteList.get(baseI).getCassetteID().getValue()));
                            /*--------------------------------------*/
                            /*   Loop for strStartRecipeParameter   */
                            /*--------------------------------------*/
                            for (int l = 0; l < rpLen; l++) {
                                /*-----------------------------------------------*/
                                /*   Check RecipeParameter Info is Same or Not   */
                                /*-----------------------------------------------*/
                                /*===== parameterName check (string) =====*/
                                if (!CimStringUtils.equals(startRecipeParameterList.get(l).getParameterName(),
                                        startCassetteList.get(baseI).getLotInCassetteList().get(baseJ).getLotWaferList().get(0).getStartRecipeParameterList().get(l).getParameterName())) {
                                    throw new ServiceException(new OmCode(retCodeConfig.getNotSameRecipeParamInfo(), lotInCassetteList.get(j).getLotID().getValue(), startCassette.getCassetteID().getValue(),
                                            startCassetteList.get(baseI).getLotInCassetteList().get(baseJ).getLotID().getValue(), startCassetteList.get(baseI).getCassetteID().getValue()));

                                }
                                /*===== parameterValue check (string) =====*/
                                if (!CimStringUtils.equals(startRecipeParameterList.get(l).getParameterValue(),
                                        startCassetteList.get(baseI).getLotInCassetteList().get(baseJ).getLotWaferList().get(0).getStartRecipeParameterList().get(l).getParameterValue())) {
                                    throw new ServiceException(new OmCode(retCodeConfig.getNotSameRecipeParamInfo(), lotInCassetteList.get(j).getLotID().getValue(), startCassette.getCassetteID().getValue(),
                                            startCassetteList.get(baseI).getLotInCassetteList().get(baseJ).getLotID().getValue(), startCassetteList.get(baseI).getCassetteID().getValue()));

                                }
                                /*===== useCurrentSettingValueFlag check (boolean) =====*/
                                if (!startRecipeParameterList.get(l).getUseCurrentSettingValueFlag().equals(
                                        startCassetteList.get(baseI).getLotInCassetteList().get(baseJ).getLotWaferList().get(0).getStartRecipeParameterList().get(l).getUseCurrentSettingValueFlag())) {
                                    throw new ServiceException(new OmCode(retCodeConfig.getNotSameRecipeParamInfo(), lotInCassetteList.get(j).getLotID().getValue(), startCassette.getCassetteID().getValue(),
                                            startCassetteList.get(baseI).getLotInCassetteList().get(baseJ).getLotID().getValue(), startCassetteList.get(baseI).getCassetteID().getValue()));

                                }
                            }
                        }
                    }
                }
            }
        }
        //【step2-4-13】check upper/lower limit for recipe parameter change - line:2169 - 2773
        log.debug("【step2-4-13】check upper/lower limit for recipe parameter change");
        if (CimStringUtils.equals(BizConstant.SP_OPERATION_OPESTART, operation)
                || CimStringUtils.equals(BizConstant.SP_OPERATION_STARTRESERVATION, operation)) {
            log.debug("operation = SP_Operation_OpeStart or SP_Operation_StartReservation");
            //check start recipe parameters
            for (int j = 0; j < startCassetteSize; j++) {
                Infos.StartCassette startCassetteObj = startCassetteList.get(j);
                if (CimStringUtils.equals(BizConstant.SP_LOADPURPOSETYPE_EMPTYCASSETTE, startCassetteObj.getLoadPurposeType())) {
                    continue;
                }

                long lotInCassetteSize = CimArrayUtils.isEmpty(startCassetteObj.getLotInCassetteList())
                        ? 0 : startCassetteObj.getLotInCassetteList().size();
                for (int k = 0; k < lotInCassetteSize; k++) {
                    Infos.LotInCassette lotInCassetteObj = startCassetteObj.getLotInCassetteList().get(k);
                    if (!lotInCassetteObj.getMoveInFlag()) {
                        continue;
                    }
                    boolean skipFlag = false;
                    Outputs.ObjLotEffectiveFPCInfoGetOut fpcInfoGetOut = lotMethod.lotEffectiveFPCInfoGet(objCommon,
                            BizConstant.SP_FPC_EXCHANGETYPE_STARTRESERVEINFO, eqpID, lotInCassetteObj.getLotID());
                    boolean paramCheckWithFPC = fpcInfoGetOut.isRecipeParameterActionRequiredFlag();
                    CimMachine equipmentObj = baseCoreFactory.getBO(CimMachine.class, eqpID);
                    Validations.check(null == equipmentObj, new OmCode(retCodeConfig.getNotFoundEqp(), eqpID.getValue()));
                    com.fa.cim.newcore.bo.recipe.CimLogicalRecipe logicalRecipe = baseCoreFactory.getBO(com.fa.cim.newcore.bo.recipe.CimLogicalRecipe.class, lotInCassetteObj.getStartRecipe().getLogicalRecipeID());
                    Validations.check(null == logicalRecipe, retCodeConfig.getNotFoundLogicalRecipe());

                    // get subLotType
                    String subLotType = lotInCassetteObj.getSubLotType();
                    com.fa.cim.newcore.bo.product.CimLot lot = baseCoreFactory.getBO(com.fa.cim.newcore.bo.product.CimLot.class, lotInCassetteObj.getLotID());
                    Validations.check(null == lot, retCodeConfig.getNotFoundLot());

                    if (CimStringUtils.isEmpty(subLotType)) {
                        subLotType = lot.getSubLotType();
                    }

                    CimMachineRecipe machineRecipe;
                    if (searchSearchCondition == 1) {
                        machineRecipe = logicalRecipe.findMachineRecipeFor(lot, equipmentObj);
                    } else {
                        machineRecipe = logicalRecipe.findMachineRecipeForSubLotType(equipmentObj, subLotType);
                    }
                    if (CimObjectUtils.isEmpty(machineRecipe) && fpcInfoGetOut.isMachineRecipeActionRequiredFlag()) {
                        log.info("MachineRecipe is overwritten by DOC.");
                        skipFlag = true;
                    }

                    List<RecipeDTO.RecipeParameter> recipeParameters = null;
                    if (!skipFlag && !paramCheckWithFPC) {
                        Validations.check(CimObjectUtils.isEmpty(machineRecipe), retCodeConfig.getNotFoundMachineRecipe());
                        recipeParameters = logicalRecipe.findRecipeParametersForSubLotType(equipmentObj, machineRecipe, subLotType);
                    }
                    if (!paramCheckWithFPC) {
                        log.debug("Recipe Parameter Check with SM Information");
                        long recipeParametersSize = CimArrayUtils.isEmpty(recipeParameters) ? 0 : recipeParameters.size();
                        long lotWaferSize = CimArrayUtils.isEmpty(lotInCassetteObj.getLotWaferList()) ? 0 : lotInCassetteObj.getLotWaferList().size();
                        String recipeParameterChangeType = lotInCassetteObj.getRecipeParameterChangeType();
                        if (CimStringUtils.equals(BizConstant.SP_RPARM_CHANGETYPE_BYLOT, recipeParameterChangeType)) {
                            log.debug("recipeParameterChangeType is ByLot, check first wafer only");
                            lotWaferSize = 1;
                        }
                        for (int l = 0; l < recipeParametersSize; l++) {
                            RecipeDTO.RecipeParameter recipeParameter = recipeParameters.get(l);
                            if (CimStringUtils.equals(BizConstant.SP_DCDEF_VAL_STRING, recipeParameter.getDataType())) {
                                log.debug("dataType is SP_DCDef_Val_String -----> continue");
                                continue;
                            }

                            // find strStartRecipeParameter from strStartCassette
                            for (int m = 0; m < lotWaferSize; m++) {
                                Infos.LotWafer lotWaferObj = lotInCassetteObj.getLotWaferList().get(m);
                                long recipeParamSize = CimArrayUtils.isEmpty(lotWaferObj.getStartRecipeParameterList())
                                        ? 0 : lotWaferObj.getStartRecipeParameterList().size();
                                boolean recipeParamFound = false;
                                if (l < recipeParamSize && CimStringUtils.equals(
                                        lotWaferObj.getStartRecipeParameterList().get(l).getParameterName(), recipeParameter.getParameterName())) {
                                    recipeParamFound = true;
                                    mBak = l;
                                } else {
                                    for (int n = 0; n < recipeParamSize; n++) {
                                        Infos.StartRecipeParameter startRecipeParameterObj = lotWaferObj.getStartRecipeParameterList().get(n);
                                        if (CimStringUtils.equals(startRecipeParameterObj.getParameterName(),
                                                recipeParameter.getParameterName())) {
                                            recipeParamFound = true;
                                            break;
                                        }
                                    }
                                }

                                if (recipeParamFound) {
                                    if (recipeParameter.getUseCurrentValueFlag()) {
                                        Validations.check(CimStringUtils.isNotEmpty(lotWaferObj.getStartRecipeParameterList().get(mBak).getParameterValue()),
                                                new OmCode(retCodeConfig.getInvalidParameterValueMustBeNull(), recipeParameter.getParameterName()));
                                    } else {
                                        if (CimStringUtils.equals(BizConstant.SP_DCDEF_VAL_INTEGER, recipeParameter.getDataType())) {
                                            log.debug("dataType is SP_DCDef_Val_Integer");
                                            // isLong should be used to judge if parameterValue is a Numeric String. But as it is converted to Long in the next line, so the judgement is not needed.  BUG-1428  [edit by Zack]
                                            boolean isLong = true;
                                            long parameterValue = 0;
                                            try {
                                                parameterValue = Long.parseLong(lotWaferObj.getStartRecipeParameterList().get(mBak).getParameterValue());
                                            } catch (NumberFormatException e) {
                                                isLong = false;
                                            }
                                            long lowerLimit = Long.parseLong(recipeParameter.getLowerLimit());
                                            long upperLimit = Long.parseLong(recipeParameter.getUpperLimit());
                                            if (!isLong || (parameterValue < lowerLimit || parameterValue > upperLimit)) {
                                                log.info("the waferID is {}", ObjectIdentifier.fetchValue(lotWaferObj.getWaferID()));
                                                throw new ServiceException(new OmCode(retCodeConfig.getInvalidParameterValueRange(),
                                                        lotWaferObj.getStartRecipeParameterList().get(mBak).getParameterName(),
                                                        CimObjectUtils.toString(lowerLimit), CimObjectUtils.toString(upperLimit)));
                                            }
                                            log.debug("limit check ok!");
                                        } else if (CimStringUtils.equals(BizConstant.SP_DCDEF_VAL_FLOAT, recipeParameter.getDataType())) {
                                            log.debug("dataType is SP_DCDef_Val_Float");
                                            double parameterValue = CimStringUtils.isEmpty(lotWaferObj.getStartRecipeParameterList().get(mBak).getParameterValue())
                                                    ? 0.0 : Double.parseDouble(lotWaferObj.getStartRecipeParameterList().get(mBak).getParameterValue());
                                            double lowerLimit = CimStringUtils.isEmpty(recipeParameter.getLowerLimit()) ? 0.0 : Double.parseDouble(recipeParameter.getLowerLimit());
                                            double upperLimit = CimStringUtils.isEmpty(recipeParameter.getUpperLimit()) ? 0.0 : Double.parseDouble(recipeParameter.getUpperLimit());


                                            OmCode omCode = new OmCode(retCodeConfig.getInvalidParameterValueRange(), lotWaferObj.getStartRecipeParameterList().get(mBak).getParameterName(), CimObjectUtils.toString(lowerLimit), CimObjectUtils.toString(upperLimit));
                                            Validations.check(parameterValue < lowerLimit || parameterValue > upperLimit, omCode);
                                        }
                                    }
                                }

                            }
                        }
                    } else {    //end if (!paramCheckWithFPC) {...}
                        log.debug("recipe parameter Check with DOC Information.");
                        long lotWaferSize = CimArrayUtils.isEmpty(lotInCassetteObj.getLotWaferList()) ? 0 : lotInCassetteObj.getLotWaferList().size();
                        for (int l = 0; l < lotWaferSize; l++) {
                            Infos.LotWafer lotWaferObj = lotInCassetteObj.getLotWaferList().get(l);
                            ObjectIdentifier tmpWaferID = lotWaferObj.getWaferID();
                            int fpcWaferCount = CimArrayUtils.isEmpty(fpcInfoGetOut.getFpcInfo().getLotWaferInfoList())
                                    ? 0 : fpcInfoGetOut.getFpcInfo().getLotWaferInfoList().size();
                            int wpos = 0;
                            for (wpos = 0; wpos < fpcWaferCount; wpos++) {
                                String targetWaferID = fpcInfoGetOut.getFpcInfo().getLotWaferInfoList().get(wpos).getWaferID().getValue();
                                if (ObjectIdentifier.equalsWithValue(tmpWaferID, targetWaferID)) {
                                    log.debug("wafer found in FPCInfo");
                                    break;
                                }
                            }

                            Validations.check(wpos == fpcWaferCount, retCodeConfig.getFpcWaferMismatchInFpcGroup());

                            int recipeParamSize = CimArrayUtils.isEmpty(lotWaferObj.getStartRecipeParameterList())
                                    ? 0 : lotWaferObj.getStartRecipeParameterList().size();
                            for (mBak = 0; mBak < recipeParamSize; mBak++) {
                                Infos.StartRecipeParameter startRecipeParameterObj = lotWaferObj.getStartRecipeParameterList().get(mBak);
                                String fpcParamName = startRecipeParameterObj.getParameterName();

                                Infos.LotWaferInfo lotWaferInfoTemp = fpcInfoGetOut.getFpcInfo().getLotWaferInfoList().get(wpos);
                                int fpcRecipeParamCount = CimArrayUtils.isEmpty(lotWaferInfoTemp.getRecipeParameterInfoList())
                                        ? 0 : lotWaferInfoTemp.getRecipeParameterInfoList().size();
                                int pPos = 0;
                                for (pPos = 0; pPos < fpcRecipeParamCount; pPos++) {
                                    if (CimStringUtils.equals(fpcParamName, lotWaferInfoTemp.getRecipeParameterInfoList().get(pPos).getParameterName())) {
                                        log.debug("recipe param found in FPCInfo");
                                        break;
                                    }
                                }
                                Validations.check(pPos == fpcRecipeParamCount, retCodeConfig.getFpcRecipeParamError());

                                String fpcParamUnit = lotWaferInfoTemp.getRecipeParameterInfoList().get(pPos).getParameterUnit();
                                String fpcParamDataType = lotWaferInfoTemp.getRecipeParameterInfoList().get(pPos).getParameterDataType();
                                String fpcParamLowerLimit = lotWaferInfoTemp.getRecipeParameterInfoList().get(pPos).getParameterLowerLimit();
                                String fpcParamUpperLimit = lotWaferInfoTemp.getRecipeParameterInfoList().get(pPos).getParameterUpperLimit();
                                boolean fpcUseCurrentSettingFlag = lotWaferInfoTemp.getRecipeParameterInfoList().get(pPos).isUseCurrentSettingValueFlag();
                                String fpcParamTargetValue = lotWaferInfoTemp.getRecipeParameterInfoList().get(pPos).getParameterTargetValue();
                                String fpcParamValue = lotWaferInfoTemp.getRecipeParameterInfoList().get(pPos).getParameterValue();
                                if (CimStringUtils.equals(BizConstant.SP_DCDEF_VAL_STRING, fpcParamDataType)) {
                                    log.debug("dataType is SP_DCDef_Val_String -----> continue");
                                    continue;
                                }
                                if (fpcUseCurrentSettingFlag) {
                                    log.debug("DOC useCurrentSettingValueFlag is TRUE");
                                    Validations.check(CimStringUtils.isNotEmpty(lotWaferObj.getStartRecipeParameterList().get(mBak).getParameterValue()),
                                            new OmCode(retCodeConfig.getInvalidParameterValueMustBeNull(), fpcParamName));
                                    log.debug("parameterValue = NULL ---> <<<<< Check OK!! >>>>>");
                                } else {
                                    log.debug("DOC useCurrentSettingValueFlag is FALSE");
                                    if (CimStringUtils.equals(BizConstant.SP_DCDEF_VAL_INTEGER, fpcParamDataType)) {
                                        log.debug("DOC dataType is SP_DCDef_Val_Integer");
                                        boolean isLong = true;
                                        long parameterValue = 0;
                                        long lowerLimit = 0;
                                        try {
                                            lowerLimit = Long.parseLong(fpcParamLowerLimit);
                                        } catch (NumberFormatException e) {
                                            isLong = false;
                                        }
                                        long upperLimit = 0;
                                        try {
                                            upperLimit = CimStringUtils.isEmpty(fpcParamUpperLimit) ? 0 : Long.parseLong(fpcParamUpperLimit);
                                        } catch (NumberFormatException e) {
                                            isLong = false;
                                        }
                                        try {
                                            parameterValue = Long.parseLong(lotWaferObj.getStartRecipeParameterList().get(mBak).getParameterValue());
                                        } catch (NumberFormatException e) {
                                            isLong = false;
                                        }
                                        Validations.check(!isLong || (parameterValue < lowerLimit || parameterValue > upperLimit),
                                                new OmCode(retCodeConfig.getInvalidParameterValueRange(), fpcParamName, fpcParamLowerLimit, fpcParamUpperLimit));
                                        log.debug("<<<<< Limit Check OK!! >>>>>");
                                    } else if (CimStringUtils.equals(BizConstant.SP_DCDEF_VAL_FLOAT, fpcParamDataType)) {
                                        log.debug("dataType is SP_DCDef_Val_Float");
                                        double parameterValue = CimStringUtils.isEmpty(lotWaferObj.getStartRecipeParameterList().get(mBak).getParameterValue())
                                                ? 0.0 : Double.parseDouble(lotWaferObj.getStartRecipeParameterList().get(mBak).getParameterValue());
                                        double lowerLimit = CimStringUtils.isEmpty(fpcParamLowerLimit) ? 0.0 : Double.parseDouble(fpcParamLowerLimit);
                                        double upperLimit = CimStringUtils.isEmpty(fpcParamUpperLimit) ? 0.0 : Double.parseDouble(fpcParamUpperLimit);
                                        Validations.check(parameterValue < lowerLimit || parameterValue > upperLimit,
                                                new OmCode(retCodeConfig.getInvalidParameterValueRange(), fpcParamName, fpcParamLowerLimit, fpcParamUpperLimit));
                                        log.debug("<<<<< Limit Check OK!! >>>>>");
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        //【step2-4-14】check monitor lot count and operation start lot count
        log.debug("【step2-4-14】check monitor lot count and operation start lot count");
        if (CimStringUtils.equals(BizConstant.SP_OPERATION_OPESTART, operation)
                || CimStringUtils.equals(BizConstant.SP_OPERATION_STARTRESERVATION, operation)) {
            log.debug("operation = SP_Operation_OpeStart or SP_Operation_StartReservation");

            long startLotCount = 0;
            long monitorLotCount = 0;
            //loop for strStartCassette
            startCassetteSize = CimArrayUtils.isEmpty(startCassetteList) ? 0 : startCassetteList.size();
            for (int j = 0; j < startCassetteSize; j++) {
                Infos.StartCassette startCassetteObj = startCassetteList.get(j);
                if (CimStringUtils.equals(BizConstant.SP_LOADPURPOSETYPE_PROCESSLOT, startCassetteObj.getLoadPurposeType())
                        || CimStringUtils.equals(BizConstant.SP_LOADPURPOSETYPE_PROCESSMONITORLOT, startCassetteObj.getLoadPurposeType())) {
                    log.debug("loadPurposeType = SP_LoadPurposeType_ProcessLot or SP_LoadPurposeType_ProcessMonitorLot");
                    // loop for strLotInCassette
                    long lotInCassetteSize = CimArrayUtils.isEmpty(startCassetteObj.getLotInCassetteList())
                            ? 0 : startCassetteObj.getLotInCassetteList().size();
                    for (int k = 0; k < lotInCassetteSize; k++) {
                        Infos.LotInCassette lotInCassetteObj = startCassetteObj.getLotInCassetteList().get(k);
                        if (CimBooleanUtils.isTrue(lotInCassetteObj.getMoveInFlag())) {
                            startLotCount++;
                            if (CimBooleanUtils.isTrue(lotInCassetteObj.getMonitorLotFlag())) {
                                monitorLotCount++;
                            }
                        }
                    }
                }
            }
            if (0 == startLotCount) {
                Validations.check(!virtualOperationFlag, new OmCode(retCodeConfig.getInvalidInputLotCount(), "0", "1", "n"));
            }
            Validations.check(monitorLotCount > 1, retCodeConfig.getInvalidProductMonitorCount());
            Validations.check(monitorLotCount == 1 && startLotCount == 1,
                    new OmCode(retCodeConfig.getInvalidInputLotCount(), String.valueOf(startLotCount), "2", "n"));

            /******************************************************************************************************/
            /*  Final check for LoadPurposeType:ProcessMonitorLot                                                 */
            /* The lot, which meets fhe following conditions must be exist, and its lot count must be 1.          */
            /*      - OpeStartFlag   : TRUE                                                                       */
            /*      - lottype        : ProcessMonitor                                                             */
            /*      - MonitorLotFlag : TRUE                                                                       */
            /******************************************************************************************************/
            int iCast = 0;
            for (iCast = 0; iCast < startCassetteSize; iCast++) {
                Infos.StartCassette startCassetteObj = startCassetteList.get(iCast);
                if (CimStringUtils.equals(BizConstant.SP_LOADPURPOSETYPE_PROCESSMONITORLOT, startCassetteObj.getLoadPurposeType())) {
                    Validations.check(monitorLotCount != 1, retCodeConfig.getInvalidProductMonitorCount());
                    int jLot = 0;
                    long lotSize = CimArrayUtils.isEmpty(startCassetteObj.getLotInCassetteList())
                            ? 0 : startCassetteObj.getLotInCassetteList().size();
                    for (jLot = 0; jLot < lotSize; jLot++) {
                        Infos.LotInCassette lotInCassetteObj = startCassetteObj.getLotInCassetteList().get(jLot);
                        if (lotInCassetteObj.getMonitorLotFlag() && lotInCassetteObj.getMoveInFlag()) {
                            if (!CimStringUtils.equals(BizConstant.SP_LOT_TYPE_PRODUCTIONMONITORLOT, lotInCassetteObj.getLotType())) {
                                boolean routeFlagGetOut = lotMethod.lotMonitorRouteFlagGet(objCommon, lotInCassetteObj.getLotID());
                                Validations.check(!routeFlagGetOut, new OmCode(retCodeConfig.getInvalidLotType(), lotInCassetteObj.getLotType(), lotInCassetteObj.getLotID().getValue()));
                            }
                            break;
                        }
                    }
                }
            }
        }
    }


    @Override
    public Boolean cassetteCheckConditionForOperationForBackSideClean(Infos.ObjCommon objCommon, ObjectIdentifier eqpID, String portGroupID, List<Infos.StartCassette> startCassetteList, String operation) {
        Boolean realCarrierExchangeFlag = null;
        String strSearchCondition = StandardProperties.OM_CONSTRAINT_CHK_WITH_SEARCH.getValue();
        long searchSearchCondition = 0;
        if (!CimStringUtils.isEmpty(strSearchCondition)) {
            searchSearchCondition = Long.parseLong(strSearchCondition);
        }

        //【step1】check virtual operation by start cassette (line: 260 - 280)
        log.debug("【step1】check virtual operation by start cassette");
        boolean virtualOperationFlag = false;
        if (CimStringUtils.isEmpty(portGroupID) && CimStringUtils.equals(BizConstant.SP_OPERATION_OPESTART, operation)) {
            virtualOperationFlag = virtualOperationMethod.virtualOperationCheckByStartCassette(objCommon, startCassetteList);
        }

        /**********************************************************************************************/
        /*【step2】Check Condition For All Cassettes                                                   */
        /*  The following conditions are checked by this object                                       */
        /*  - controlJobID、multiLotType、transferState、transferReserved                             */
        /*  - dispatchSate、cassetteState、and loadingSequenceNumber for all cassette                 */
        /**********************************************************************************************/
        int lenCassette = CimArrayUtils.getSize(startCassetteList);
        log.debug("【step2】Check Condition For All Cassettes");

        //【step2-1】get eqp object
        log.debug("【step2-1】get eqp object");
        CimMachine aMachine = baseCoreFactory.getBO(CimMachine.class, eqpID);
        Validations.check(null == aMachine, retCodeConfig.getNotFoundEqp(), eqpID);

        //【step2-2】get eqp's multiRecipeCapability
        log.debug("【step2-2】get eqp's multiRecipeCapability");
        String multipleRecipeCapability = aMachine.getMultipleRecipeCapability();

        //【step2-3】get eqp's operation mode info
        log.debug("【step2-3】get eqp's operation mode info");
        if (CimStringUtils.equals(BizConstant.SP_OPERATION_OPESTART, operation)
                || CimStringUtils.equals(BizConstant.SP_OPERATION_STARTRESERVATION, operation)) {
            log.debug("operation == SP_Operation_OpeStart, SP_Operation_StartReservation");
            if (!CimBooleanUtils.isTrue(virtualOperationFlag)) {
                portMethod.portResourceCurrentOperationModeGet(objCommon, eqpID, startCassetteList.get(0).getLoadPortID());
            }
        } else {
            log.debug("operation != SP_Operation_OpeStart && operation != SP_Operation_StartReservation");
        }

        //【step2-4】check cassette condition
        log.debug("【step2-4】check cassette condition");
        int mBak = 0;   // mBak = m
        long startCassetteSize = startCassetteList.size();
//        String saveControlJobID = null;
        ObjectIdentifier saveControlJobID = null;
        log.debug("check cassette condition, startCassetteSize = %d", startCassetteSize);
        String strSequenceCondition = StandardProperties.OM_CARRIER_LOAD_SEQ_CHK.getValue();
        long sequenceCondition = 0;
        if (!CimStringUtils.isEmpty(strSequenceCondition)) {
            sequenceCondition = Long.parseLong(strSequenceCondition);
        }
        boolean cassetteLoadingCheck = false;
        if (0 == sequenceCondition && (CimStringUtils.equals(BizConstant.SP_OPERATION_OPESTART, operation)
                || CimStringUtils.equals(BizConstant.SP_OPERATION_STARTRESERVATION, operation))) {
            log.debug("operation == SP_Operation_OpeStart) || (operation == SP_Operation_StartReservation");
            cassetteLoadingCheck = true;
        }
        if (1 == sequenceCondition && CimStringUtils.equals(BizConstant.SP_OPERATION_STARTRESERVATION, operation)) {
            log.debug("1 == sequenceCondition && operation == SP_Operation_StartReservation)");
            cassetteLoadingCheck = true;
        }

        for (int i = 0; i < startCassetteSize; i++) {
            Infos.StartCassette startCassette = startCassetteList.get(i);
            //【step2-4-1】check SorterJob existence
            log.debug("【step2-4-1】 check SorterJob Existence");
            if (CimStringUtils.equals(BizConstant.SP_OPERATION_NPWCARRIERXFER, operation)
                    || CimStringUtils.equals(BizConstant.SP_OPERATION_STARTRESERVATION, operation)
                    || CimStringUtils.equals(BizConstant.SP_OPERATION_OPESTART, operation)) {
                Infos.EquipmentLoadPortAttribute equipmentLoadPortAttribute = new Infos.EquipmentLoadPortAttribute();
                List<Infos.CassetteLoadPort> cassetteLoadPortList = new ArrayList<>();
                Infos.CassetteLoadPort cassetteLoadPort = new Infos.CassetteLoadPort();
                cassetteLoadPort.setPortID(startCassette.getLoadPortID());
                cassetteLoadPort.setCassetteID(startCassette.getCassetteID());
                cassetteLoadPortList.add(cassetteLoadPort);
                equipmentLoadPortAttribute.setCassetteLoadPortList(cassetteLoadPortList);
                equipmentLoadPortAttribute.setEquipmentID(eqpID);

                Inputs.ObjWaferSorterJobCheckForOperation in = new Inputs
                        .ObjWaferSorterJobCheckForOperation();
                in.setEquipmentLoadPortAttribute(equipmentLoadPortAttribute);
                List<ObjectIdentifier> cassetteIDList = new ArrayList<>();
                cassetteIDList.add(startCassette.getCassetteID());
                in.setCassetteIDList(cassetteIDList);
                in.setOperation(operation);
                //on-route模式下，sorter设备做了MoveInReserve后是存在sorterJob的，故应该跳过sorterJobCheck
                if (!CimStringUtils.equals(TransactionIDEnum.SORT_ACTION_REQ.getValue(), objCommon.getTransactionID())) {
                    waferMethod.waferSorterSorterJobCheckForOperation(objCommon, in);
                }
            }

            //【step2-4-2】check start cassette's loading order
            log.debug("【step2-4-2】check start cassette's loading order");
            if (cassetteLoadingCheck && !virtualOperationFlag) {
                if (startCassette.getLoadSequenceNumber() != (i + 1)) {
                    log.error("strStartCassette[i].loadSequenceNumber != i");
                    throw new ServiceException(new OmCode(retCodeConfig.getInvalidLoadingSequence(), startCassette.getCassetteID().getValue()));
                }
            }

            //get cassette object
            log.debug("get cassette object");
            com.fa.cim.newcore.bo.durable.CimCassette aCassette = baseCoreFactory.getBO(com.fa.cim.newcore.bo.durable.CimCassette.class, startCassette.getCassetteID());
            Validations.check(null == aCassette, new OmCode(retCodeConfig.getNotFoundCassette(), startCassette.getCassetteID().getValue()));

            //get and check controlJobID, all of startCassetteList[i].getCassetteID() must be equal.
            log.debug("get and check controlJobID");
            com.fa.cim.newcore.bo.product.CimControlJob aControlJob = aCassette.getControlJob();
            if (CimStringUtils.equals(BizConstant.SP_OPERATION_OPESTART, operation)) {
                log.debug("operation == SP_Operation_OpeStart");
                if (0 == i) {
                    saveControlJobID = CimObjectUtils.isEmpty(aControlJob) ? null : aControlJob.getControlJobID();
                } else {
                    //String cassetteControlJobID = cassette.getControlJobID();
                    ObjectIdentifier cassetteControlJobID = CimObjectUtils.isEmpty(aControlJob) ? null : aControlJob.getControlJobID();
                    Validations.check(!ObjectIdentifier.equalsWithValue(saveControlJobID, cassetteControlJobID), retCodeConfig.getCassetteControlJobMix());
                }
            } else {
                Validations.check(null != aControlJob, retCodeConfig.getCassetteControlJobFilled());
            }

            //get cassette's multiLotType
            log.debug("get cassette's multiLotType");
            String multiLotType = aCassette.getMultiLotType();
            //【step2-4-3】check multiRecipeCapability VS multiLotType
            log.debug("【step2-4-3】check multiRecipeCapability VS multiLotType");
            String loadPurposeType = startCassette.getLoadPurposeType();
            if (CimStringUtils.equals(BizConstant.SP_LOADPURPOSETYPE_EMPTYCASSETTE, loadPurposeType)) {
                log.debug("strStartCassette[i].loadPurposeType == SP_LoadPurposeType_EmptyCassette");
            } else if (CimStringUtils.equals(BizConstant.SP_EQP_MULTIRECIPECAPABILITY_MULTIPLERECIPE, multipleRecipeCapability)) {
                log.debug("multiRecipeCapability == SP_Eqp_MultiRecipeCapability_A");
            } else if (CimStringUtils.equals(BizConstant.SP_EQP_MULTIRECIPECAPABILITY_SINGLERECIPE, multipleRecipeCapability)) {
                log.debug("multiRecipeCapability == SP_Eqp_MultiRecipeCapability_B");
            } else if (CimStringUtils.equals(BizConstant.SP_EQP_MULTIRECIPECAPABILITY_BATCH, multipleRecipeCapability)) {
                log.debug("multiRecipeCapability == SP_Eqp_MultiRecipeCapability_C");
                if (CimStringUtils.equals(BizConstant.SP_OPERATION_NPWCARRIERXFER, operation)) {
                    log.debug("operation == SP_Operation_NPWCarrierXfer");
                } else if (CimStringUtils.equals(BizConstant.SP_CAS_MULTILOTTYPE_SINGLELOTSINGLERECIPE, multiLotType)
                        || CimStringUtils.equals(BizConstant.SP_CAS_MULTILOTTYPE_MULTILOTSINGLERECIPE, multiLotType)) {
                    log.debug("multiLotType ==  SP_Cas_MultiLotType_SingleLotSingleRecipe || SP_Cas_MultiLotType_MultiLotSingleRecipe");
                } else {
                    log.error("return RC_CAST_EQP_CONDITION_ERROR!!");
                    throw new ServiceException(retCodeConfig.getCassetteEquipmentConditionError());
                }
            } else {
                log.debug("No Process <Check MultiRecipeCapability VS MultiLotType>");
            }

            //【step2-4-4】check cassette's transfer status
            log.debug("【step2-4-4】check cassette's transfer status");
            String transferState = aCassette.getTransportState();
            boolean transferReserved = aCassette.isReserved();
            // For Operation Start
            if (CimStringUtils.equals(BizConstant.SP_OPERATION_OPESTART, operation)) {
                if (CimStringUtils.equals(BizConstant.SP_TRANSSTATE_EQUIPMENTIN, transferState)) {
                    log.debug("transferState == SP_TransState_EquipmentIn");
                } else {
                    Validations.check(!virtualOperationFlag, retCodeConfig.getInvalidCassetteTransferState(), transferState, startCassette.getCassetteID());
                }
            } else if (CimStringUtils.equals(BizConstant.SP_OPERATION_FLOWBATCHING, operation)) {
                log.debug("operation = SP_Operation_FlowBatching");
                if (!transferReserved && (CimStringUtils.equals(BizConstant.SP_TRANSSTATE_STATIONIN, transferState)
                        || CimStringUtils.equals(BizConstant.SP_TRANSSTATE_BAYIN, transferState)
                        || CimStringUtils.equals(BizConstant.SP_TRANSSTATE_MANUALIN, transferState))) {
                    log.debug("rc == success");
                } else {
                    throw new ServiceException(new OmCode(retCodeConfig.getInvalidCassetteTransferState(), transferState, startCassette.getCassetteID().getValue()));
                }
            } else if (CimStringUtils.equals(BizConstant.SP_OPERATION_STARTRESERVATION, operation)
                    || CimStringUtils.equals(BizConstant.SP_OPERATION_NPWCARRIERXFER, operation)) {
                log.debug("operation = SP_Operation_StartReservation");

                Infos.EqpPortStatus orgEqpPortStatus = null;
                CimMachine aOrgMachine = null;
                Outputs.ObjPortResourceCurrentOperationModeGetOut modeGetOut =
                        portMethod.portResourceCurrentOperationModeGet(objCommon, eqpID, startCassette.getLoadPortID());

                if (CimStringUtils.equals(BizConstant.SP_TRANSSTATE_EQUIPMENTIN, transferState)) {
                    //get originator eqp's info
                    log.debug("get originator eqp");
                    Machine aMachine1 = aCassette.currentAssignedMachine();
                    if (null != aMachine1) {
                        Boolean isStorageBool = aMachine1.isStorageMachine();
                        if (!isStorageBool) {
                            aOrgMachine = (CimMachine) aMachine1;
                            log.debug("isStorageBool is not TRUE: eqp. So narrow to PosMachine");
                        } else {
                            log.debug("isStorageBool is TRUE: Storage");
                        }
                    }
                    Validations.check(null == aOrgMachine, new OmCode(retCodeConfig.getNotFoundEqp(), eqpID.getValue()));

                    ObjectIdentifier originalEquipmentID = new ObjectIdentifier(aOrgMachine.getIdentifier(), aOrgMachine.getPrimaryKey());
                    //get cassette info in original eqp
                    log.debug("get cassette info in original eqp");
                    Infos.EqpPortInfo eqpPortInfo;
                    String equipmentCategory = aOrgMachine.getCategory();
                    if (CimStringUtils.equals(BizConstant.SP_MC_CATEGORY_INTERNALBUFFER, equipmentCategory)) {
                        log.debug("equipmentCategory is [InternalBuffer]");
                        //line:rc = equipment_portInfoForInternalBuffer_GetDR(...) - line:778 -787
                        eqpPortInfo = equipmentMethod.equipmentPortInfoForInternalBufferGetDR(objCommon, originalEquipmentID);
                    } else {
                        log.debug("equipmentCategory is not [InternalBuffer]");
                        eqpPortInfo = equipmentMethod.equipmentPortInfoGet(objCommon, originalEquipmentID);
                    }
                    //find assigned port's portGroupID
                    log.debug("find assigned port's portGroupID");
                    boolean foundFlag = false;
                    for (Infos.EqpPortStatus eqpPortStatus : eqpPortInfo.getEqpPortStatuses()) {
                        if (ObjectIdentifier.equalsWithValue(startCassette.getCassetteID(),
                                eqpPortStatus.getLoadedCassetteID())) {
                            orgEqpPortStatus = eqpPortStatus;
                            foundFlag = true;
                            break;
                        }
                    }
                    Validations.check(!foundFlag, retCodeConfig.getInvalidCassetteTransferState(), transferState, startCassette.getCassetteID());
                }

                if (CimStringUtils.equals(BizConstant.SP_EQP_ACCESSMODE_MANUAL, modeGetOut.getOperationMode().getAccessMode())) {
                    log.debug("when TransferStatus is EI, AccessMode makes it an error with Manual");
                    if (CimStringUtils.equals(BizConstant.SP_TRANSSTATE_EQUIPMENTIN, transferState)) {
                        log.debug("transferState is [EI]");
                        //permit Carrier which a person can deliver in StartLotReserve.
                        //as for the condition, OperationMode is "***-1" and XferState is "EI".
                        if (!CimStringUtils.equals(BizConstant.SP_EQP_ACCESSMODE_MANUAL, orgEqpPortStatus.getAccessMode())
                                || ObjectIdentifier.isEmptyWithValue(orgEqpPortStatus.getLoadedCassetteID())) {
                            log.error("##### return RC_INVALID_CAST_XFERSTAT");
                            throw new ServiceException(new OmCode(retCodeConfig.getInvalidCassetteTransferState(), transferState, startCassette.getCassetteID().getValue()));
                        }
                    }
                } else {
                    boolean reRouteFlag = false;
                    String reRouteXferFlag = StandardProperties.OM_XFER_REROUTE_FLAG.getValue();
                    String operationMode = modeGetOut.getOperationMode().getOperationMode().getValue();
                    log.debug("OperationMode:%s", operationMode);
                    if (CimStringUtils.equals(reRouteXferFlag, "1")
                            && CimStringUtils.equals(BizConstant.SP_EQP_PORT_OPERATIONMODE_AUTO_3, operationMode)
                            && !transferReserved
                            && (CimStringUtils.equals(BizConstant.SP_TRANSSTATE_STATIONIN, transferState)
                            || CimStringUtils.equals(BizConstant.SP_TRANSSTATE_BAYIN, transferState)
                            || CimStringUtils.equals(BizConstant.SP_TRANSSTATE_MANUALIN, transferState)
                            || CimStringUtils.equals(BizConstant.SP_TRANSSTATE_BAYOUT, transferState))) {
                        log.debug("operationMode is Auto-3");
                        log.debug("transferState = [SI], [BI], [MI], [BO] and transferReserved is FALSE");
                    } else if ((CimStringUtils.equals(BizConstant.SP_TRANSSTATE_STATIONIN, transferState)
                            || CimStringUtils.equals(BizConstant.SP_TRANSSTATE_BAYIN, transferState)
                            || CimStringUtils.equals(BizConstant.SP_TRANSSTATE_MANUALIN, transferState))
                            && !transferReserved) {
                        log.debug("transferState = [SI], [BI], [MI] and transferReserved is FALSE");
                    } else if ((CimStringUtils.equals(BizConstant.SP_TRANSSTATE_STATIONOUT, transferState)
                            || CimStringUtils.equals(BizConstant.SP_TRANSSTATE_EQUIPMENTOUT, transferState)
                            && !transferReserved)) {
                        log.debug("transferState = [SO], [EO], and transferReserved is FALSE");
                        //【step2-4-6】check transfer job existence (line:919 - )
                        Infos.CarrierJobResult jobRecordGetOut = null;
                        try {
                            jobRecordGetOut = this.cassetteTransferJobRecordGetDR(objCommon, startCassette.getCassetteID());
                        } catch (ServiceException e) {
                            if (Validations.isEquals(retCodeConfig.getCarrierNotTransfering(), e.getCode())) {
                                throw new ServiceException(new OmCode(retCodeConfig.getInvalidCassetteTransferState(), transferState, startCassette.getCassetteID().getValue()));
                            } else {
                                throw e;
                            }
                        }
                        // call stocker_type_GetDR (line: 941 - 953)
                        try {
                            stockerComp.stockerTypeGet(objCommon, jobRecordGetOut.getToMachine());
                        } catch (ServiceException e) {
                            if (Validations.isEquals(retCodeConfig.getUndefinedStockerType(), e.getCode())) {
                                throw new ServiceException(new OmCode(retCodeConfig.getInvalidCassetteTransferState(), transferState, startCassette.getCassetteID().getValue()));
                            } else {
                                throw e;
                            }
                        }
                    } else if (CimStringUtils.equals(BizConstant.SP_TRANSSTATE_EQUIPMENTIN, transferState) && !transferReserved) {
                        /*-----------------------------------------------------------------------------------------------*/
                        /*   The following StartLotReserve isn't permitted in Transfer EQP to EQP of CassetteDelivery.   */
                        /*                                                                                               */
                        /*   -----------------------------                                                               */
                        /*   |         FromEQP           |                                                               */
                        /*   ----------------------------|                                                               */
                        /*   | OperationMode : Offline-2 |                                                               */
                        /*   | XferState     : EI        |                                                               */
                        /*   -----------------------------                                                               */
                        /*-----------------------------------------------------------------------------------------------*/
                        log.debug("(transferState = SP_TransState_EquipmentIn) and (transferReserved == FALSE)");
                        if (CimStringUtils.equals(BizConstant.SP_EQP_ACCESSMODE_AUTO, orgEqpPortStatus.getAccessMode())
                                && CimStringUtils.equals(BizConstant.SP_EQP_ONLINEMODE_OFFLINE, orgEqpPortStatus.getOnlineMode())) {
                            throw new ServiceException(new OmCode(retCodeConfig.getInvalidCassetteTransferState(), transferState, startCassette.getCassetteID().getValue()));
                        }
                        /*-----------------------------------------------------------------------------------------------*/
                        /*   The following StartLotReserve isn't permitted in Transfer EQP to EQP of CassetteDelivery.   */
                        /*                                                                                               */
                        /*   ToEQP's OperationMode : ***-2                                                               */
                        /*-----------------------------------------------------------------------------------------------*/
                        if (CimStringUtils.equals(BizConstant.SP_EQP_ACCESSMODE_AUTO, modeGetOut.getOperationMode().getAccessMode())
                                || CimStringUtils.equals(BizConstant.SP_EQP_DISPATCHMODE_AUTO, modeGetOut.getOperationMode().getDispatchMode())) {
                            throw new ServiceException(new OmCode(retCodeConfig.getInvalidCassetteTransferState(), transferState, startCassette.getCassetteID().getValue()));
                        }

                        //【step2-4-7】Check orgEqp's EqpToEqpTransfer Flag is TRUE or Not
                        log.debug("【step2-4-7】Check orgEqp's EqpToEqpTransfer Flag is TRUE or not");
                        boolean eqpToEqpXferFlag = aOrgMachine.isEqpToEqpTransferFlagOn();
                        if (!eqpToEqpXferFlag) {
                            throw new ServiceException(new OmCode(retCodeConfig.getInvalidCassetteTransferState(), transferState, startCassette.getCassetteID().getValue()));
                        }
                    } else {
                        throw new ServiceException(new OmCode(retCodeConfig.getInvalidCassetteTransferState(), transferState, startCassette.getCassetteID().getValue()));
                    }
                }
            }
            //get cassette's dispatch status
            boolean dispatchReserveFlag = aCassette.isDispatchReserved();
            if (CimStringUtils.equals(BizConstant.SP_OPERATION_STARTRESERVATION, operation)
                    || CimStringUtils.equals(BizConstant.SP_OPERATION_FLOWBATCHING, operation)
                    || CimStringUtils.equals(BizConstant.SP_OPERATION_NPWCARRIERXFER, operation)) {
                Validations.check(dispatchReserveFlag, retCodeConfig.getAlreadyDispatchReservedCassette());
            }
            //get and check cassette's status
            String cassetteState = aCassette.getDurableState();
            if (CimStringUtils.equals(BizConstant.SP_OPERATION_NPWCARRIERXFER, operation)
                    && (CimStringUtils.equals(BizConstant.SP_LOADPURPOSETYPE_OTHER, startCassette.getLoadPurposeType())
                    || CimStringUtils.equals(BizConstant.SP_LOADPURPOSETYPE_EMPTYCASSETTE, startCassette.getLoadPurposeType()))) {
            } else if (CimStringUtils.equals(BizConstant.SP_OPERATION_OPESTART, operation)
                    || CimStringUtils.equals(BizConstant.SP_OPERATION_STARTRESERVATION, operation)) {
                log.debug("operation == SP_Operation_OpeStart or SP_Operation_StartReservation");
                Set<String> subLotTypeSet = new HashSet<>();
                // for empty carrier
                if (CimStringUtils.equals(startCassetteList.get(i).getLoadPurposeType(), BizConstant.SP_LOADPURPOSETYPE_EMPTYCASSETTE)) {
                    log.info("Empty carrier");
                    int startCastLen = CimArrayUtils.getSize(startCassetteList);
                    for (int startCastIndex = 0; startCastIndex < startCastLen; startCastIndex++) {
                        List<Infos.LotInCassette> lotInCassetteList = startCassetteList.get(startCastIndex).getLotInCassetteList();
                        int lotInCastLen = CimArrayUtils.getSize(lotInCassetteList);
                        for (int lotInCastIndex = 0; lotInCastIndex < lotInCastLen; lotInCastIndex++) {
                            if (lotInCassetteList.get(lotInCastIndex).getMoveInFlag()) {
                                subLotTypeSet.add(lotInCassetteList.get(lotInCastIndex).getSubLotType());
                            }
                        }
                    }
                }
                // for lot in carrier
                else {
                    log.info("Lot in carrier");
                    // Get carrier's start lots SubLotType
                    List<Infos.LotInCassette> lotInCassetteList = startCassetteList.get(i).getLotInCassetteList();
                    int lotInCastLen = CimArrayUtils.getSize(lotInCassetteList);
                    for (int lotInCastIndex = 0; lotInCastIndex < lotInCastLen; lotInCastIndex++) {
                        if (lotInCassetteList.get(lotInCastIndex).getMoveInFlag()) {
                            subLotTypeSet.add(lotInCassetteList.get(lotInCastIndex).getSubLotType());
                        }
                    }
                }
                boolean availableFlag = aCassette.isLotProcessAvailable(new ArrayList<>(subLotTypeSet));
                Validations.check(!availableFlag, new OmCode(retCodeConfig.getDurableNotAvailableStateForLotProcess(), startCassette.getCassetteID().getValue()));
            } else {
                if (!CimStringUtils.equals(CIMStateConst.CIM_DURABLE_AVAILABLE, cassetteState)
                        && !CimStringUtils.equals(CIMStateConst.CIM_DURABLE_INUSE, cassetteState)) {
                    throw new ServiceException(new OmCode(retCodeConfig.getInvalidCassetteState(), cassetteState, startCassette.getCassetteID().getValue()));
                }
            }

            //【step2-4-9】check start cassette and start lot combination - line: 1352 - 1495
            log.debug("【step2-4-9】check start cassette and start lot combination");
            if (CimStringUtils.equals(BizConstant.SP_OPERATION_OPESTART, operation)
                    || CimStringUtils.equals(BizConstant.SP_OPERATION_STARTRESERVATION, operation)) {
                log.debug("Start cassette and Start lot Combination Check");
                List<Lot> lotList = aCassette.allLots();
                if (CimArrayUtils.getSize(lotList) == CimArrayUtils.getSize(startCassette.getLotInCassetteList())) {
                    log.debug("lot count is maching, the size:{}", lotList.size());

                    // all lot in start cassette loop - line:1383 - 1483
                    //【bear】from the line:1383 - 1483, each lot of lotList must be match in lotInCassetteList, otherwise return lot_start_cassette_unmatch
                    boolean lotMatchFlag = false;
                    for (Lot lot : lotList) {
                        Validations.check(null == lot, new OmCode(retCodeConfig.getLotCastUnmatch(), "*****", startCassette.getCassetteID().getValue()));
                        String lotID = lot.getIdentifier();
                        for (Infos.LotInCassette lotInCassette : startCassette.getLotInCassetteList()) {
                            if (CimStringUtils.equals(lotID, lotInCassette.getLotID().getValue())) {
                                log.debug("find matching lot");
                                lotMatchFlag = true;
                                break;
                            }
                        }
                        Validations.check(!lotMatchFlag, retCodeConfig.getLotStartCassetteUnMatch());
                    }

                    // check cassette & lot combination - line: 1436 - 1483
                    // inpara lot -> lot in cassette
                    //【bear】 it's the same with all lot in start cassette loop, so we can't do once again.
                } else {
                    log.error("lot in start cassette miss match");
                    throw new ServiceException(retCodeConfig.getLotStartCassetteUnMatch());
                }
            }
        }
        //因为sortActionReq调用了move in 此时TransactionID应该由TransactionIDEnum.SORT_ACTION_REQ变成
        //TransactionIDEnum.OPERATION_START_REQ
        if(CimStringUtils.equals(TransactionIDEnum.SORT_ACTION_REQ.getValue(), objCommon.getTransactionID())){
            objCommon.setTransactionID(TransactionIDEnum.OPERATION_START_REQ.getValue());
        }
        //【step2-4-10】check cassette's controJobID vs eqp's controlJobID - line:1502 - 1660
        log.debug("【step2-4-10】check cassette's controJobID vs eqp's controlJobID");
        if (CimStringUtils.equals(BizConstant.SP_OPERATION_OPESTART, operation)) {
            List<Infos.StartReservedControlJobInfo> startReservedControlJobInfos = equipmentMethod.equipmentReservedControlJobIDGet(objCommon, eqpID);
            ObjectIdentifier eqpControlJobID = null;
            List<ProductDTO.PosStartCassetteInfo> startCassetteInfo = null;
            List<ProductDTO.PosStartCassetteInfo> startCassetteInfoVar = null;
            int reservedControlJobLen = (null == startReservedControlJobInfos ? 0 : startReservedControlJobInfos.size());
            for (int j = 0; j < reservedControlJobLen; j++) {
                Infos.StartReservedControlJobInfo startReservedControlJobInfo = startReservedControlJobInfos.get(j);
                if (CimStringUtils.equals(startReservedControlJobInfo.getPortGroupID(), portGroupID)) {
                    log.debug("in-parm's portGroup is found in reservedControlJobInfo...");
                    eqpControlJobID = startReservedControlJobInfo.getControlJobID();

                    //get controlJob object
                    com.fa.cim.newcore.bo.product.CimControlJob reserveControlJob = baseCoreFactory.getBO(com.fa.cim.newcore.bo.product.CimControlJob.class, eqpControlJobID);
                    Validations.check(null == reserveControlJob, retCodeConfig.getNotFoundControlJob());
                    startCassetteInfo = reserveControlJob.getStartCassetteInfo();
                    startCassetteInfoVar = startCassetteInfo;
                    break;
                }
            }

            // compare reserved controlJobID vs cassette's controlJobID - line:1573 - 1583
            log.debug("compare reserved controlJobID vs cassette's controlJobID");
            Validations.check(!ObjectIdentifier.equalsWithValue(saveControlJobID, eqpControlJobID), retCodeConfig.getCassettePortControlJobUnMatch());
            /*===== check reserved controlJobID's cassette count vs in-parm's cassette count =====*/
            if (!ObjectIdentifier.isEmptyWithValue(saveControlJobID)) {
                Validations.check(CimArrayUtils.getSize(startCassetteList) != CimArrayUtils.getSize(startCassetteInfo), retCodeConfig.getCastPortCtrljobCountUnmatch());
            }
            // check reserved controlJobID's StartCassetteInfo vs in-parm's StartCassetteInfo - line: 1605 - 1655
            log.debug("check reserved controlJobID's StartCassetteInfo vs in-parm's StartCassetteInfo");
            if (!CimArrayUtils.isEmpty(startCassetteInfo)) {
                log.debug("check reserved controlJobID's StartCassetteInfo vs in-parm's StartCassetteInfo");
                for (int j = 0; j < startCassetteSize; j++) {
                    Infos.StartCassette startCassetteTemp = startCassetteList.get(j);
                    int lotLen = null == startCassetteTemp.getLotInCassetteList()
                            ? 0 : startCassetteTemp.getLotInCassetteList().size();
                    for (int k = 0; k < lotLen; k++) {
                        Infos.LotInCassette lotInCassette = startCassetteTemp.getLotInCassetteList().get(k);
                        boolean sameConditionFlag = false;

                        for (ProductDTO.PosStartCassetteInfo startCassetteObj : startCassetteInfo) {
                            long controlLotLen = null == startCassetteObj.getLotInCassetteInfo()
                                    ? 0 : startCassetteObj.getLotInCassetteInfo().size();
                            for (int l = 0; l < controlLotLen; l++) {
                                ProductDTO.PosLotInCassetteInfo lotInCassetteObj = startCassetteObj.getLotInCassetteInfo().get(l);
                                if (CimStringUtils.equals(lotInCassette.getLotID().getValue(), lotInCassetteObj.getLotID().getValue())
                                        && (lotInCassette.getMoveInFlag().equals(lotInCassetteObj.isOperationStartFlag()))) {
                                    log.debug("found same condition lot.");
                                    sameConditionFlag = true;
                                    break;
                                }
                            }
                            if (sameConditionFlag) {
                                break;
                            }
                        }
                        Validations.check(!sameConditionFlag, new OmCode(retCodeConfig.getStartReserveControlJobOperationStateControlJobUnMatch(), eqpID.getValue(), eqpControlJobID.getValue()));
                    }
                }
            }

        } else {
            log.debug("operation != SP_Operation_OpeStart");
        }

        //【step2-4-11】check condition for maxBatchSize, minBatchSize, emptyCassetteCount
        log.debug("【step2-4-11】check condition for maxBatchSize, minBatchSize, emptyCassetteCount");
        if (CimStringUtils.equals(BizConstant.SP_OPERATION_OPESTART, operation)
                || CimStringUtils.equals(BizConstant.SP_OPERATION_STARTRESERVATION, operation)) {
            log.debug("operation = SP_Operation_OpeStart or SP_Operation_StartReservation");
            Outputs.ObjEquipmentProcessBatchConditionGetOut batchConditionGetOut = equipmentMethod.equipmentProcessBatchConditionGet(objCommon, eqpID);
            if (CimBooleanUtils.isTrue(batchConditionGetOut.isCassetteExchangeFlag())) {
                realCarrierExchangeFlag = operationMethod.getAndCheckBackSideCleanCarrierExchangeByFlowStep(objCommon, startCassetteList);
                //replace batchConditionGetOut.carrierExchangeFlag to reCarrierExchangeFlag.
                batchConditionGetOut.setCassetteExchangeFlag(realCarrierExchangeFlag);
            } else {
                realCarrierExchangeFlag = batchConditionGetOut.isCassetteExchangeFlag();
            }

            // check cassette count
            int processCassetteCount = 0;
            int emptyCassetteCount = 0;
            int lenCassetteSize = CimArrayUtils.getSize(startCassetteList);
            for (int j = 0; j < lenCassetteSize; j++) {
                Infos.StartCassette startCassette1 = startCassetteList.get(j);
                String loadPurposeType1 = startCassette1.getLoadPurposeType();
                if (CimStringUtils.equals(BizConstant.SP_LOADPURPOSETYPE_PROCESSLOT, loadPurposeType1)
                        || CimStringUtils.equals(BizConstant.SP_LOADPURPOSETYPE_PROCESSMONITORLOT, loadPurposeType1)
                        //添加Other类型
                        || CimStringUtils.equals(BizConstant.SP_LOADPURPOSETYPE_OTHER, loadPurposeType1)) {
                    processCassetteCount++;
                } else if (CimStringUtils.equals(BizConstant.SP_LOADPURPOSETYPE_EMPTYCASSETTE, loadPurposeType1)) {
                    emptyCassetteCount++;
                }
            }
            if ((processCassetteCount >= batchConditionGetOut.getMinBatchSize())
                    && (processCassetteCount <= batchConditionGetOut.getMaxBatchSize())) {
                log.debug("result = ok");
            } else {
                Validations.check(CimBooleanUtils.isFalse(virtualOperationFlag), retCodeConfig.getInvalidInputCassetteCount());
            }
            if (CimBooleanUtils.isTrue(batchConditionGetOut.isCassetteExchangeFlag())
                    && CimBooleanUtils.isTrue(batchConditionGetOut.isMonitorCreationFlag())) {
                Validations.check(emptyCassetteCount != (processCassetteCount + 1),
                        new OmCode(retCodeConfig.getInvalidEmptyCount(), String.valueOf(emptyCassetteCount), String.valueOf(processCassetteCount + 1)));
            } else if (CimBooleanUtils.isTrue(batchConditionGetOut.isCassetteExchangeFlag())
                    && CimBooleanUtils.isFalse(batchConditionGetOut.isMonitorCreationFlag())) {
                boolean categoryChkMode = StandardProperties.OM_EXCHANGE_CARRIER_CATEGORY_CHK_MODE.isOn();
                if (categoryChkMode){
                    for (Infos.StartCassette startCassette : startCassetteList){
                        List<Infos.LotInCassette> lotInCassetteList = startCassette.getLotInCassetteList();
                        for (Infos.LotInCassette lotInCassette : lotInCassetteList){
                            if (lotInCassette.getMoveInFlag()){
                                ObjectIdentifier lotID = lotInCassette.getLotID();
                                CimLot aLot = baseCoreFactory.getBO(CimLot.class, lotID);
                                String requiredCassetteCategory = aLot.getRequiredCassetteCategory();
                                if (!CimStringUtils.isEmpty(requiredCassetteCategory)){
                                    if (requiredCassetteCategory.contains("2")){
                                        String[] split = requiredCassetteCategory.split("2",2);
                                        if (CimStringUtils.isEmpty(split[0]) || CimStringUtils.isEmpty(split[1])){
                                            processCassetteCount --;
                                            break;
                                        }
                                    }else {
                                        processCassetteCount --;
                                        break;
                                    }
                                }else {
                                    processCassetteCount --;
                                    break;
                                }
                            }
                        }
                    }
                    Validations.check(emptyCassetteCount != processCassetteCount,
                            new OmCode(retCodeConfig.getInvalidEmptyCount(), String.valueOf(emptyCassetteCount), String.valueOf(processCassetteCount)));
                }else {
                    Validations.check(emptyCassetteCount != processCassetteCount,
                            new OmCode(retCodeConfig.getInvalidEmptyCount(), String.valueOf(emptyCassetteCount), String.valueOf(processCassetteCount)));
                }
            } else if (CimBooleanUtils.isFalse(batchConditionGetOut.isCassetteExchangeFlag())
                    && CimBooleanUtils.isTrue(batchConditionGetOut.isMonitorCreationFlag())) {
                Validations.check(emptyCassetteCount != 1,
                        new OmCode(retCodeConfig.getInvalidEmptyCount(), String.valueOf(emptyCassetteCount), String.valueOf(1)));
            } else if (CimBooleanUtils.isFalse(batchConditionGetOut.isCassetteExchangeFlag())
                    && CimBooleanUtils.isFalse(batchConditionGetOut.isMonitorCreationFlag())) {
                String eqpCategory = aMachine.getCategory();
                if (!CimStringUtils.equals(BizConstant.SP_MC_CATEGORY_WAFERSORTER, eqpCategory)) {
                    Validations.check(emptyCassetteCount != 0,
                            new OmCode(retCodeConfig.getInvalidEmptyCount(), String.valueOf(emptyCassetteCount), String.valueOf(0)));
                }
            }

        }
        /*------------------------------------------------------------------------------*/
        /*                                                                              */
        /*   Check Condition for Eqp's MultiRecipeCapability VS RecipeParameterValue    */
        /*                                                                              */
        /*------------------------------------------------------------------------------*/
        if (CimStringUtils.equals(operation, BizConstant.SP_OPERATION_OPESTART)
                || CimStringUtils.equals(operation, BizConstant.SP_OPERATION_STARTRESERVATION)) {
            if (CimStringUtils.equals(multipleRecipeCapability, BizConstant.SP_EQP_MULTIRECIPECAPABILITY_SINGLERECIPE)
                    || CimStringUtils.equals(multipleRecipeCapability, BizConstant.SP_EQP_MULTIRECIPECAPABILITY_BATCH)) {
                /*-----------------------------------*/
                /*   Work Valiable for Check Logic   */
                /*-----------------------------------*/
                boolean baseSetFlag = false;
                int baseI = 0;
                int baseJ = 0;
                int baseRPLen = 0;
                /*-------------------------------*/
                /*   Loop for strStartCassette   */
                /*-------------------------------*/
                for (int i = 0; i < lenCassette; i++) {
                    /*------------------------*/
                    /*   Omit EmptyCassette   */
                    /*------------------------*/
                    Infos.StartCassette startCassette = startCassetteList.get(i);
                    if (CimStringUtils.equals(startCassette.getLoadPurposeType(), BizConstant.SP_LOADPURPOSETYPE_EMPTYCASSETTE)) {
                        continue;
                    }
                    /*-------------------------------*/
                    /*   Loop for strLotInCassette   */
                    /*-------------------------------*/
                    List<Infos.LotInCassette> lotInCassetteList = startCassette.getLotInCassetteList();
                    int lenLotInCassette = CimArrayUtils.getSize(lotInCassetteList);
                    for (int j = 0; j < lenLotInCassette; j++) {
                        /*------------------------*/
                        /*   Omit Non-Start Lot   */
                        /*------------------------*/
                        if (!lotInCassetteList.get(j).getMoveInFlag()) {
                            continue;
                        }
                        /*-------------------------------------*/
                        /*   Check RecipeParameterChangeType   */
                        /*-------------------------------------*/
                        Validations.check(!CimStringUtils.equals(lotInCassetteList.get(j).getRecipeParameterChangeType(), BizConstant.SP_RPARM_CHANGETYPE_BYLOT),
                                new OmCode(retCodeConfig.getInvalidRecipeParamChangeType(), lotInCassetteList.get(j).getLotID().getValue(), startCassette.getCassetteID().getValue()));
                        /*--------------------*/
                        /*   Save Base Info   */
                        /*--------------------*/
                        if (!baseSetFlag) {
                            baseSetFlag = true;
                            baseI = i;
                            baseJ = j;
                            if (!CimArrayUtils.isEmpty(lotInCassetteList.get(j).getLotWaferList())) {
                                baseRPLen = CimArrayUtils.getSize(lotInCassetteList.get(j).getLotWaferList().get(0).getStartRecipeParameterList());
                            } else {
                                throw new ServiceException(new OmCode(retCodeConfig.getInvalidWaferCount(), lotInCassetteList.get(j).getLotID().getValue(), startCassette.getCassetteID().getValue()));
                            }
                        }
                        /*--------------------------*/
                        /*   Loop for strLotWafer   */
                        /*--------------------------*/
                        List<Infos.LotWafer> lotWaferList = lotInCassetteList.get(j).getLotWaferList();
                        int lwLen = CimArrayUtils.getSize(lotWaferList);
                        for (int k = 0; k < lwLen; k++) {
                            /*---------------------------------*/
                            /*   Check RecipeParameter Count   */
                            /*---------------------------------*/
                            List<Infos.StartRecipeParameter> startRecipeParameterList = lotWaferList.get(k).getStartRecipeParameterList();
                            int rpLen = CimArrayUtils.getSize(startRecipeParameterList);
                            Validations.check(rpLen != baseRPLen,
                                    new OmCode(retCodeConfig.getNotSameRecipeParamInfo(), lotInCassetteList.get(j).getLotID().getValue(), startCassette.getCassetteID().getValue(),
                                            startCassetteList.get(baseI).getLotInCassetteList().get(baseJ).getLotID().getValue(), startCassetteList.get(baseI).getCassetteID().getValue()));
                            /*--------------------------------------*/
                            /*   Loop for strStartRecipeParameter   */
                            /*--------------------------------------*/
                            for (int l = 0; l < rpLen; l++) {
                                /*-----------------------------------------------*/
                                /*   Check RecipeParameter Info is Same or Not   */
                                /*-----------------------------------------------*/
                                /*===== parameterName check (string) =====*/
                                if (!CimStringUtils.equals(startRecipeParameterList.get(l).getParameterName(),
                                        startCassetteList.get(baseI).getLotInCassetteList().get(baseJ).getLotWaferList().get(0).getStartRecipeParameterList().get(l).getParameterName())) {
                                    throw new ServiceException(new OmCode(retCodeConfig.getNotSameRecipeParamInfo(), lotInCassetteList.get(j).getLotID().getValue(), startCassette.getCassetteID().getValue(),
                                            startCassetteList.get(baseI).getLotInCassetteList().get(baseJ).getLotID().getValue(), startCassetteList.get(baseI).getCassetteID().getValue()));

                                }
                                /*===== parameterValue check (string) =====*/
                                if (!CimStringUtils.equals(startRecipeParameterList.get(l).getParameterValue(),
                                        startCassetteList.get(baseI).getLotInCassetteList().get(baseJ).getLotWaferList().get(0).getStartRecipeParameterList().get(l).getParameterValue())) {
                                    throw new ServiceException(new OmCode(retCodeConfig.getNotSameRecipeParamInfo(), lotInCassetteList.get(j).getLotID().getValue(), startCassette.getCassetteID().getValue(),
                                            startCassetteList.get(baseI).getLotInCassetteList().get(baseJ).getLotID().getValue(), startCassetteList.get(baseI).getCassetteID().getValue()));

                                }
                                /*===== useCurrentSettingValueFlag check (boolean) =====*/
                                if (!startRecipeParameterList.get(l).getUseCurrentSettingValueFlag().equals(
                                        startCassetteList.get(baseI).getLotInCassetteList().get(baseJ).getLotWaferList().get(0).getStartRecipeParameterList().get(l).getUseCurrentSettingValueFlag())) {
                                    throw new ServiceException(new OmCode(retCodeConfig.getNotSameRecipeParamInfo(), lotInCassetteList.get(j).getLotID().getValue(), startCassette.getCassetteID().getValue(),
                                            startCassetteList.get(baseI).getLotInCassetteList().get(baseJ).getLotID().getValue(), startCassetteList.get(baseI).getCassetteID().getValue()));

                                }
                            }
                        }
                    }
                }
            }
        }
        //【step2-4-13】check upper/lower limit for recipe parameter change - line:2169 - 2773
        log.debug("【step2-4-13】check upper/lower limit for recipe parameter change");
        if (CimStringUtils.equals(BizConstant.SP_OPERATION_OPESTART, operation)
                || CimStringUtils.equals(BizConstant.SP_OPERATION_STARTRESERVATION, operation)) {
            log.debug("operation = SP_Operation_OpeStart or SP_Operation_StartReservation");
            //check start recipe parameters
            for (int j = 0; j < startCassetteSize; j++) {
                Infos.StartCassette startCassetteObj = startCassetteList.get(j);
                if (CimStringUtils.equals(BizConstant.SP_LOADPURPOSETYPE_EMPTYCASSETTE, startCassetteObj.getLoadPurposeType())) {
                    continue;
                }

                long lotInCassetteSize = CimArrayUtils.isEmpty(startCassetteObj.getLotInCassetteList())
                        ? 0 : startCassetteObj.getLotInCassetteList().size();
                for (int k = 0; k < lotInCassetteSize; k++) {
                    Infos.LotInCassette lotInCassetteObj = startCassetteObj.getLotInCassetteList().get(k);
                    if (!lotInCassetteObj.getMoveInFlag()) {
                        continue;
                    }
                    boolean skipFlag = false;
                    Outputs.ObjLotEffectiveFPCInfoGetOut fpcInfoGetOut = lotMethod.lotEffectiveFPCInfoGet(objCommon,
                            BizConstant.SP_FPC_EXCHANGETYPE_STARTRESERVEINFO, eqpID, lotInCassetteObj.getLotID());
                    boolean paramCheckWithFPC = fpcInfoGetOut.isRecipeParameterActionRequiredFlag();
                    CimMachine equipmentObj = baseCoreFactory.getBO(CimMachine.class, eqpID);
                    Validations.check(null == equipmentObj, new OmCode(retCodeConfig.getNotFoundEqp(), eqpID.getValue()));
                    com.fa.cim.newcore.bo.recipe.CimLogicalRecipe logicalRecipe = baseCoreFactory.getBO(com.fa.cim.newcore.bo.recipe.CimLogicalRecipe.class, lotInCassetteObj.getStartRecipe().getLogicalRecipeID());
                    Validations.check(null == logicalRecipe, retCodeConfig.getNotFoundLogicalRecipe());

                    // get subLotType
                    String subLotType = lotInCassetteObj.getSubLotType();
                    com.fa.cim.newcore.bo.product.CimLot lot = baseCoreFactory.getBO(com.fa.cim.newcore.bo.product.CimLot.class, lotInCassetteObj.getLotID());
                    Validations.check(null == lot, retCodeConfig.getNotFoundLot());

                    if (CimStringUtils.isEmpty(subLotType)) {
                        subLotType = lot.getSubLotType();
                    }

                    CimMachineRecipe machineRecipe;
                    if (searchSearchCondition == 1) {
                        machineRecipe = logicalRecipe.findMachineRecipeFor(lot, equipmentObj);
                    } else {
                        machineRecipe = logicalRecipe.findMachineRecipeForSubLotType(equipmentObj, subLotType);
                    }
                    if (CimObjectUtils.isEmpty(machineRecipe) && fpcInfoGetOut.isMachineRecipeActionRequiredFlag()) {
                        log.info("MachineRecipe is overwritten by DOC.");
                        skipFlag = true;
                    }

                    List<RecipeDTO.RecipeParameter> recipeParameters = null;
                    if (!skipFlag && !paramCheckWithFPC) {
                        Validations.check(CimObjectUtils.isEmpty(machineRecipe), retCodeConfig.getNotFoundMachineRecipe());
                        recipeParameters = logicalRecipe.findRecipeParametersForSubLotType(equipmentObj, machineRecipe, subLotType);
                    }
                    if (!paramCheckWithFPC) {
                        log.debug("Recipe Parameter Check with SM Information");
                        long recipeParametersSize = CimArrayUtils.isEmpty(recipeParameters) ? 0 : recipeParameters.size();
                        long lotWaferSize = CimArrayUtils.isEmpty(lotInCassetteObj.getLotWaferList()) ? 0 : lotInCassetteObj.getLotWaferList().size();
                        String recipeParameterChangeType = lotInCassetteObj.getRecipeParameterChangeType();
                        if (CimStringUtils.equals(BizConstant.SP_RPARM_CHANGETYPE_BYLOT, recipeParameterChangeType)) {
                            log.debug("recipeParameterChangeType is ByLot, check first wafer only");
                            lotWaferSize = 1;
                        }
                        for (int l = 0; l < recipeParametersSize; l++) {
                            RecipeDTO.RecipeParameter recipeParameter = recipeParameters.get(l);
                            if (CimStringUtils.equals(BizConstant.SP_DCDEF_VAL_STRING, recipeParameter.getDataType())) {
                                log.debug("dataType is SP_DCDef_Val_String -----> continue");
                                continue;
                            }

                            // find strStartRecipeParameter from strStartCassette
                            for (int m = 0; m < lotWaferSize; m++) {
                                Infos.LotWafer lotWaferObj = lotInCassetteObj.getLotWaferList().get(m);
                                long recipeParamSize = CimArrayUtils.isEmpty(lotWaferObj.getStartRecipeParameterList())
                                        ? 0 : lotWaferObj.getStartRecipeParameterList().size();
                                boolean recipeParamFound = false;
                                if (l < recipeParamSize && CimStringUtils.equals(
                                        lotWaferObj.getStartRecipeParameterList().get(l).getParameterName(), recipeParameter.getParameterName())) {
                                    recipeParamFound = true;
                                    mBak = l;
                                } else {
                                    for (int n = 0; n < recipeParamSize; n++) {
                                        Infos.StartRecipeParameter startRecipeParameterObj = lotWaferObj.getStartRecipeParameterList().get(n);
                                        if (CimStringUtils.equals(startRecipeParameterObj.getParameterName(),
                                                recipeParameter.getParameterName())) {
                                            recipeParamFound = true;
                                            break;
                                        }
                                    }
                                }

                                if (recipeParamFound) {
                                    if (recipeParameter.getUseCurrentValueFlag()) {
                                        Validations.check(CimStringUtils.isNotEmpty(lotWaferObj.getStartRecipeParameterList().get(mBak).getParameterValue()),
                                                new OmCode(retCodeConfig.getInvalidParameterValueMustBeNull(), recipeParameter.getParameterName()));
                                    } else {
                                        if (CimStringUtils.equals(BizConstant.SP_DCDEF_VAL_INTEGER, recipeParameter.getDataType())) {
                                            log.debug("dataType is SP_DCDef_Val_Integer");
                                            // isLong should be used to judge if parameterValue is a Numeric String. But as it is converted to Long in the next line, so the judgement is not needed.  BUG-1428  [edit by Zack]
                                            boolean isLong = true;
                                            long parameterValue = 0;
                                            try {
                                                parameterValue = Long.parseLong(lotWaferObj.getStartRecipeParameterList().get(mBak).getParameterValue());
                                            } catch (NumberFormatException e) {
                                                isLong = false;
                                            }
                                            long lowerLimit = Long.parseLong(recipeParameter.getLowerLimit());
                                            long upperLimit = Long.parseLong(recipeParameter.getUpperLimit());
                                            if (!isLong || (parameterValue < lowerLimit || parameterValue > upperLimit)) {
                                                log.info("the waferID is {}", ObjectIdentifier.fetchValue(lotWaferObj.getWaferID()));
                                                throw new ServiceException(new OmCode(retCodeConfig.getInvalidParameterValueRange(),
                                                        lotWaferObj.getStartRecipeParameterList().get(mBak).getParameterName(),
                                                        CimObjectUtils.toString(lowerLimit), CimObjectUtils.toString(upperLimit)));
                                            }
                                            log.debug("limit check ok!");
                                        } else if (CimStringUtils.equals(BizConstant.SP_DCDEF_VAL_FLOAT, recipeParameter.getDataType())) {
                                            log.debug("dataType is SP_DCDef_Val_Float");
                                            double parameterValue = CimStringUtils.isEmpty(lotWaferObj.getStartRecipeParameterList().get(mBak).getParameterValue())
                                                    ? 0.0 : Double.parseDouble(lotWaferObj.getStartRecipeParameterList().get(mBak).getParameterValue());
                                            double lowerLimit = CimStringUtils.isEmpty(recipeParameter.getLowerLimit()) ? 0.0 : Double.parseDouble(recipeParameter.getLowerLimit());
                                            double upperLimit = CimStringUtils.isEmpty(recipeParameter.getUpperLimit()) ? 0.0 : Double.parseDouble(recipeParameter.getUpperLimit());

                                            OmCode omCode = new OmCode(retCodeConfig.getInvalidParameterValueRange(), lotWaferObj.getStartRecipeParameterList().get(mBak).getParameterName(), CimObjectUtils.toString(lowerLimit), CimObjectUtils.toString(upperLimit));
                                            Validations.check(parameterValue < lowerLimit || parameterValue > upperLimit, omCode);
                                        }
                                    }
                                }

                            }
                        }
                    } else {    //end if (!paramCheckWithFPC) {...}
                        log.debug("recipe parameter Check with DOC Information.");
                        long lotWaferSize = CimArrayUtils.isEmpty(lotInCassetteObj.getLotWaferList()) ? 0 : lotInCassetteObj.getLotWaferList().size();
                        for (int l = 0; l < lotWaferSize; l++) {
                            Infos.LotWafer lotWaferObj = lotInCassetteObj.getLotWaferList().get(l);
                            ObjectIdentifier tmpWaferID = lotWaferObj.getWaferID();
                            int fpcWaferCount = CimArrayUtils.isEmpty(fpcInfoGetOut.getFpcInfo().getLotWaferInfoList())
                                    ? 0 : fpcInfoGetOut.getFpcInfo().getLotWaferInfoList().size();
                            int wpos = 0;
                            for (wpos = 0; wpos < fpcWaferCount; wpos++) {
                                String targetWaferID = fpcInfoGetOut.getFpcInfo().getLotWaferInfoList().get(wpos).getWaferID().getValue();
                                if (ObjectIdentifier.equalsWithValue(tmpWaferID, targetWaferID)) {
                                    log.debug("wafer found in FPCInfo");
                                    break;
                                }
                            }

                            Validations.check(wpos == fpcWaferCount, retCodeConfig.getFpcWaferMismatchInFpcGroup());

                            int recipeParamSize = CimArrayUtils.isEmpty(lotWaferObj.getStartRecipeParameterList())
                                    ? 0 : lotWaferObj.getStartRecipeParameterList().size();
                            for (mBak = 0; mBak < recipeParamSize; mBak++) {
                                Infos.StartRecipeParameter startRecipeParameterObj = lotWaferObj.getStartRecipeParameterList().get(mBak);
                                String fpcParamName = startRecipeParameterObj.getParameterName();

                                Infos.LotWaferInfo lotWaferInfoTemp = fpcInfoGetOut.getFpcInfo().getLotWaferInfoList().get(wpos);
                                int fpcRecipeParamCount = CimArrayUtils.isEmpty(lotWaferInfoTemp.getRecipeParameterInfoList())
                                        ? 0 : lotWaferInfoTemp.getRecipeParameterInfoList().size();
                                int pPos = 0;
                                for (pPos = 0; pPos < fpcRecipeParamCount; pPos++) {
                                    if (CimStringUtils.equals(fpcParamName, lotWaferInfoTemp.getRecipeParameterInfoList().get(pPos).getParameterName())) {
                                        log.debug("recipe param found in FPCInfo");
                                        break;
                                    }
                                }
                                Validations.check(pPos == fpcRecipeParamCount, retCodeConfig.getFpcRecipeParamError());

                                String fpcParamUnit = lotWaferInfoTemp.getRecipeParameterInfoList().get(pPos).getParameterUnit();
                                String fpcParamDataType = lotWaferInfoTemp.getRecipeParameterInfoList().get(pPos).getParameterDataType();
                                String fpcParamLowerLimit = lotWaferInfoTemp.getRecipeParameterInfoList().get(pPos).getParameterLowerLimit();
                                String fpcParamUpperLimit = lotWaferInfoTemp.getRecipeParameterInfoList().get(pPos).getParameterUpperLimit();
                                boolean fpcUseCurrentSettingFlag = lotWaferInfoTemp.getRecipeParameterInfoList().get(pPos).isUseCurrentSettingValueFlag();
                                String fpcParamTargetValue = lotWaferInfoTemp.getRecipeParameterInfoList().get(pPos).getParameterTargetValue();
                                String fpcParamValue = lotWaferInfoTemp.getRecipeParameterInfoList().get(pPos).getParameterValue();
                                if (CimStringUtils.equals(BizConstant.SP_DCDEF_VAL_STRING, fpcParamDataType)) {
                                    log.debug("dataType is SP_DCDef_Val_String -----> continue");
                                    continue;
                                }
                                if (fpcUseCurrentSettingFlag) {
                                    log.debug("DOC useCurrentSettingValueFlag is TRUE");
                                    Validations.check(CimStringUtils.isNotEmpty(lotWaferObj.getStartRecipeParameterList().get(mBak).getParameterValue()),
                                            new OmCode(retCodeConfig.getInvalidParameterValueMustBeNull(), fpcParamName));
                                    log.debug("parameterValue = NULL ---> <<<<< Check OK!! >>>>>");
                                } else {
                                    log.debug("DOC useCurrentSettingValueFlag is FALSE");
                                    if (CimStringUtils.equals(BizConstant.SP_DCDEF_VAL_INTEGER, fpcParamDataType)) {
                                        log.debug("DOC dataType is SP_DCDef_Val_Integer");
                                        boolean isLong = true;
                                        long parameterValue = 0;
                                        long lowerLimit = 0;
                                        try {
                                            lowerLimit = Long.parseLong(fpcParamLowerLimit);
                                        } catch (NumberFormatException e) {
                                            isLong = false;
                                        }
                                        long upperLimit = 0;
                                        try {
                                            upperLimit = CimStringUtils.isEmpty(fpcParamUpperLimit) ? 0 : Long.parseLong(fpcParamUpperLimit);
                                        } catch (NumberFormatException e) {
                                            isLong = false;
                                        }
                                        try {
                                            parameterValue = Long.parseLong(lotWaferObj.getStartRecipeParameterList().get(mBak).getParameterValue());
                                        } catch (NumberFormatException e) {
                                            isLong = false;
                                        }
                                        Validations.check(!isLong || (parameterValue < lowerLimit || parameterValue > upperLimit),
                                                new OmCode(retCodeConfig.getInvalidParameterValueRange(), fpcParamName, fpcParamLowerLimit, fpcParamUpperLimit));
                                        log.debug("<<<<< Limit Check OK!! >>>>>");
                                    } else if (CimStringUtils.equals(BizConstant.SP_DCDEF_VAL_FLOAT, fpcParamDataType)) {
                                        log.debug("dataType is SP_DCDef_Val_Float");
                                        double parameterValue = CimStringUtils.isEmpty(lotWaferObj.getStartRecipeParameterList().get(mBak).getParameterValue())
                                                ? 0.0 : Double.parseDouble(lotWaferObj.getStartRecipeParameterList().get(mBak).getParameterValue());
                                        double lowerLimit = CimStringUtils.isEmpty(fpcParamLowerLimit) ? 0.0 : Double.parseDouble(fpcParamLowerLimit);
                                        double upperLimit = CimStringUtils.isEmpty(fpcParamUpperLimit) ? 0.0 : Double.parseDouble(fpcParamUpperLimit);
                                        Validations.check(parameterValue < lowerLimit || parameterValue > upperLimit,
                                                new OmCode(retCodeConfig.getInvalidParameterValueRange(), fpcParamName, fpcParamLowerLimit, fpcParamUpperLimit));
                                        log.debug("<<<<< Limit Check OK!! >>>>>");
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        //【step2-4-14】check monitor lot count and operation start lot count
        log.debug("【step2-4-14】check monitor lot count and operation start lot count");
        if (CimStringUtils.equals(BizConstant.SP_OPERATION_OPESTART, operation)
                || CimStringUtils.equals(BizConstant.SP_OPERATION_STARTRESERVATION, operation)) {
            log.debug("operation = SP_Operation_OpeStart or SP_Operation_StartReservation");

            long startLotCount = 0;
            long monitorLotCount = 0;
            //loop for strStartCassette
            startCassetteSize = CimArrayUtils.isEmpty(startCassetteList) ? 0 : startCassetteList.size();
            for (int j = 0; j < startCassetteSize; j++) {
                Infos.StartCassette startCassetteObj = startCassetteList.get(j);
                if (CimStringUtils.equals(BizConstant.SP_LOADPURPOSETYPE_PROCESSLOT, startCassetteObj.getLoadPurposeType())
                        || CimStringUtils.equals(BizConstant.SP_LOADPURPOSETYPE_PROCESSMONITORLOT, startCassetteObj.getLoadPurposeType())
                        //添加Other类型
                        || CimStringUtils.equals(BizConstant.SP_LOADPURPOSETYPE_OTHER, startCassetteObj.getLoadPurposeType())) {
                    log.debug("loadPurposeType = SP_LoadPurposeType_ProcessLot or SP_LoadPurposeType_ProcessMonitorLot");
                    // loop for strLotInCassette
                    long lotInCassetteSize = CimArrayUtils.isEmpty(startCassetteObj.getLotInCassetteList())
                            ? 0 : startCassetteObj.getLotInCassetteList().size();
                    for (int k = 0; k < lotInCassetteSize; k++) {
                        Infos.LotInCassette lotInCassetteObj = startCassetteObj.getLotInCassetteList().get(k);
                        if (CimBooleanUtils.isTrue(lotInCassetteObj.getMoveInFlag())) {
                            startLotCount++;
                            if (CimBooleanUtils.isTrue(lotInCassetteObj.getMonitorLotFlag())) {
                                monitorLotCount++;
                            }
                        }
                    }
                }
            }
            if (0 == startLotCount) {
                Validations.check(!virtualOperationFlag, new OmCode(retCodeConfig.getInvalidInputLotCount(), "0", "1", "n"));
            }
            Validations.check(monitorLotCount > 1, retCodeConfig.getInvalidProductMonitorCount());
            Validations.check(monitorLotCount == 1 && startLotCount == 1,
                    new OmCode(retCodeConfig.getInvalidInputLotCount(), String.valueOf(startLotCount), "2", "n"));

            /******************************************************************************************************/
            /*  Final check for LoadPurposeType:ProcessMonitorLot                                                 */
            /* The lot, which meets fhe following conditions must be exist, and its lot count must be 1.          */
            /*      - OpeStartFlag   : TRUE                                                                       */
            /*      - lottype        : ProcessMonitor                                                             */
            /*      - MonitorLotFlag : TRUE                                                                       */
            /******************************************************************************************************/
            int iCast = 0;
            for (iCast = 0; iCast < startCassetteSize; iCast++) {
                Infos.StartCassette startCassetteObj = startCassetteList.get(iCast);
                if (CimStringUtils.equals(BizConstant.SP_LOADPURPOSETYPE_PROCESSMONITORLOT, startCassetteObj.getLoadPurposeType())) {
                    Validations.check(monitorLotCount != 1, retCodeConfig.getInvalidProductMonitorCount());
                    int jLot = 0;
                    long lotSize = CimArrayUtils.isEmpty(startCassetteObj.getLotInCassetteList())
                            ? 0 : startCassetteObj.getLotInCassetteList().size();
                    for (jLot = 0; jLot < lotSize; jLot++) {
                        Infos.LotInCassette lotInCassetteObj = startCassetteObj.getLotInCassetteList().get(jLot);
                        if (lotInCassetteObj.getMonitorLotFlag() && lotInCassetteObj.getMoveInFlag()) {
                            if (!CimStringUtils.equals(BizConstant.SP_LOT_TYPE_PRODUCTIONMONITORLOT, lotInCassetteObj.getLotType())) {
                                boolean routeFlagGetOut = lotMethod.lotMonitorRouteFlagGet(objCommon, lotInCassetteObj.getLotID());
                                Validations.check(!routeFlagGetOut, new OmCode(retCodeConfig.getInvalidLotType(), lotInCassetteObj.getLotType(), lotInCassetteObj.getLotID().getValue()));
                            }
                            break;
                        }
                    }
                }
            }
        }
        return realCarrierExchangeFlag;
    }


    /**
     * [Function Description]:
     * Change dispatch reserved status of cassette by input parameter.
     *
     * @param objCommon           objCommon
     * @param cassetteID          cassetteID
     * @param dispatchReserveFlag dispatchReserveFlag
     * @return
     * @author Ho
     */
    @Override
    public void cassetteDispatchStateChange(Infos.ObjCommon objCommon, ObjectIdentifier cassetteID, boolean dispatchReserveFlag) {
        if(!SorterHandler.containsFOSB(cassetteID)){
            com.fa.cim.newcore.bo.durable.CimCassette cassette = baseCoreFactory.getBO(com.fa.cim.newcore.bo.durable.CimCassette.class, cassetteID);
            Validations.check(null == cassette, new OmCode(retCodeConfig.getNotFoundCassette(), cassetteID.getValue()));
            boolean curDispatchReservedFlag = cassette.isDispatchReserved();
            if (dispatchReserveFlag) {
                Validations.check(curDispatchReservedFlag, retCodeConfig.getAlreadyDispatchReservedCassette());
                cassette.makeDispatchReserved();
            } else {
                cassette.makeNotDispatchReserved();
            }
        }
    }

    /**
     * [Function Description]:
     * Set in-param's loadPurposeType to NPWLoadPurposeTypeReset PosCassette.
     *
     * @param objCommon
     * @param cassetteID
     * @param loadPurposeType
     * @return
     * @author Ho
     */
    @Override
    public void cassetteSetNPWLoadPurposeType(Infos.ObjCommon objCommon, ObjectIdentifier cassetteID, String loadPurposeType) {
        /*---------------------------*/
        /*  carrier=FOSB11111 跳过验证   */
        /*---------------------------*/
        if(!SorterHandler.containsFOSB(cassetteID)){
            /*---------------------------*/
            /*   Get cassetteID Object   */
            /*---------------------------*/
            com.fa.cim.newcore.bo.durable.CimCassette aCassette = baseCoreFactory.getBO(com.fa.cim.newcore.bo.durable.CimCassette.class, cassetteID);
            Validations.check(aCassette == null, new OmCode(retCodeConfig.getNotFoundCassette(), cassetteID.getValue()));
            aCassette.setNPWLoadPurposeType(loadPurposeType);
        }
    }

    @Override
    public void cassetteCheckConditionForUnloading(Infos.ObjCommon objCommonIn, ObjectIdentifier cassetteID) {
        if(!SorterHandler.containsFOSB(cassetteID)) {

            //-------------------------
            //   Get cassette Object
            //-------------------------
            CimCassette cassette = baseCoreFactory.getBO(CimCassette.class, cassetteID);
            //-------------------------
            //   Get TransferState
            //-------------------------
            String transferState = cassette.getTransportState();
            Validations.check(!CimStringUtils.equals(BizConstant.SP_TRANSSTATE_EQUIPMENTIN, transferState), new OmCode(retCodeConfig.getInvalidCassetteTransferState(), transferState, ObjectIdentifier.fetchValue(cassetteID)));
        }
    }

    @Override
    public ObjectIdentifier cassetteControlJobIDGet(Infos.ObjCommon objCommonIn, ObjectIdentifier cassetteID) {
        log.debug("getCassetteControlJobID(): enter getCassetteControlJobID");
        //Get cassette Object
        com.fa.cim.newcore.bo.durable.CimCassette aCassette = baseCoreFactory.getBO(com.fa.cim.newcore.bo.durable.CimCassette.class, cassetteID);
        Validations.check(aCassette == null, retCodeConfig.getNotFoundCassette(), ObjectIdentifier.fetchValue(cassetteID));
        //Get controlJobID
        com.fa.cim.newcore.bo.product.CimControlJob aControlJob = aCassette.getControlJob();
        ObjectIdentifier controlJobID = (null == aControlJob)
                ? new ObjectIdentifier() : new ObjectIdentifier(aControlJob.getIdentifier(), aControlJob.getPrimaryKey());
        return controlJobID;
    }

    @Override
    public void emptyCassetteCheckCategoryForOperation(Infos.ObjCommon objCommon, List<Infos.StartCassette> startCassetteList) {
        //Check whether StartCassette has EmptyCassette.
        boolean existEmpty = false;
        if (!CimArrayUtils.isEmpty(startCassetteList)) {
            for (Infos.StartCassette startCassette : startCassetteList) {
                try {
                    cassetteCheckEmpty(startCassette.getCassetteID());
                    existEmpty = true;
                    break;
                } catch (ServiceException e) {
                    if (!Validations.isEquals(retCodeConfig.getCastNotEmpty(), e.getCode())) {
                        throw e;
                    }
                }
            }
        }
        if (!existEmpty) {
            return;
        }
        //Check category of EmptyCassette.
        List<String> alreadyCheckedEmptyCastList = new ArrayList();
        Predicate<Infos.LotInCassette> operationStartFilter = lotInCassette -> lotInCassette.getMoveInFlag();
        startCassetteList.forEach(startCassette -> {
            List<Infos.LotInCassette> lotInCassetteList = startCassette.getLotInCassetteList();
            if (!CimArrayUtils.isEmpty(lotInCassetteList)) {
                for (Infos.LotInCassette lotInCassette : lotInCassetteList) {
                    if (operationStartFilter.test(lotInCassette)) {
                        //Get lot's next operation category. lot_requiredCassetteCategory_GetForNextOperation
                        String nextRequiredCassetteCategory = lotMethod.lotRequiredCassetteCategoryGetForNextOperation(objCommon, lotInCassette.getLotID());
                        if (CimStringUtils.isEmpty(nextRequiredCassetteCategory)) {
                            continue;
                        }
                        boolean foundMatchCategory = false;
                        for (Infos.StartCassette cassetteItem : startCassetteList) {
                            if (alreadyCheckedEmptyCastList.contains(ObjectIdentifier.fetchValue(cassetteItem.getCassetteID()))) {
                                continue;
                            }
                            if (CimStringUtils.equals(BizConstant.SP_LOADPURPOSETYPE_EMPTYCASSETTE, cassetteItem.getLoadPurposeType())) {
                                CimCassette cassette = baseCoreFactory.getBO(CimCassette.class, cassetteItem.getCassetteID());
                                String emptyCastCategory = cassette.getCassetteCategory();
                                if (CimStringUtils.equals(nextRequiredCassetteCategory, emptyCastCategory)) {
                                    foundMatchCategory = true;
                                    alreadyCheckedEmptyCastList.add(ObjectIdentifier.fetchValue(cassetteItem.getCassetteID()));
                                    break;
                                }
                            }
                        }
                        Validations.check(!foundMatchCategory, retCodeConfig.getCassetteCategoryMismatch());
                        break;
                    }
                }
            }
        });
    }

    @Override
    public void cassetteCheckEmpty(ObjectIdentifier cassetteID) {
        if(!SorterHandler.containsFOSB(cassetteID)){
            CimCassette aCassette = baseCoreFactory.getBO(CimCassette.class, cassetteID);
            Validations.check(null == aCassette, new OmCode(retCodeConfig.getNotFoundCassette(), "******"));
            Validations.check(!aCassette.isEmpty(), new OmCode(retCodeConfig.getCastNotEmpty(), ObjectIdentifier.fetchValue(cassetteID)));
        }
    }

    @Override
    public void cassetteUsageCountIncrement(Infos.ObjCommon objCommon, ObjectIdentifier cassetteID) {
        log.debug("cassetteUsageCountIncrement(): enter cassetteUsageCountIncrement");

        com.fa.cim.newcore.bo.durable.CimCassette cassette = baseCoreFactory.getBO(com.fa.cim.newcore.bo.durable.CimCassette.class, cassetteID);
        Validations.check(cassette == null, new OmCode(retCodeConfig.getNotFoundCassette(), cassetteID.getValue()));

        //Increse timesUsed
        cassette.incrementTimesUsed();
        long totalTimesUsed = null == cassette.getTotalTimesUsed() ? 0L : cassette.getTotalTimesUsed();
        totalTimesUsed = totalTimesUsed + 1;
        cassette.setTotalTimesUsed(totalTimesUsed);
    }

    /**
     * cassette_CheckConditionForOpeStartCancel
     *
     * @param objCommon         objCommon
     * @param startCassetteList startCassetteList
     * @author Ho
     */
    @Override
    public void cassetteCheckConditionForOpeStartCancel(Infos.ObjCommon objCommon, List<Infos.StartCassette> startCassetteList) {
        for (Infos.StartCassette startCassette : startCassetteList) {
            if (ObjectIdentifier.isEmpty(startCassette.getCassetteID())) {
                continue;
            }
            /*-------------------------*/
            /*   Get cassette Object   */
            /*-------------------------*/
            com.fa.cim.newcore.bo.durable.CimCassette cassette = baseCoreFactory.getBO(com.fa.cim.newcore.bo.durable.CimCassette.class, startCassette.getCassetteID());
            Validations.check(CimObjectUtils.isEmpty(cassette), retCodeConfig.getNotFoundCassette());
            String transferState = cassette.getTransportState();
            Validations.check(!CimStringUtils.equals(transferState, BizConstant.SP_TRANSSTATE_EQUIPMENTIN), new OmCode(retCodeConfig.getInvalidCassetteTransferState(), transferState, cassette.getIdentifier()));
        }
    }

    /**
     * cassette_usageCount_Decrement
     * Decrease the usage count of specified cassette.
     *
     * @param objCommon  objCommon
     * @param cassetteID cassetteID
     * @return
     * @author Ho
     */
    @Override
    public void cassetteUsageCountDecrement(Infos.ObjCommon objCommon, ObjectIdentifier cassetteID) {
        /*-------------------------*/
        /*   Get cassette Object   */
        /*-------------------------*/
        com.fa.cim.newcore.bo.durable.CimCassette cassette = baseCoreFactory.getBO(com.fa.cim.newcore.bo.durable.CimCassette.class, cassetteID);
        Validations.check(CimObjectUtils.isEmpty(cassette), retCodeConfig.getNotFoundCassette());

        /*-----------------------*/
        /*   Decrese timesUsed   */
        /*-----------------------*/
        Long timesUsed = cassette.getTimeUsed();
        if (timesUsed == null) {
            timesUsed = 0L;
        }
        timesUsed--;
        if (timesUsed < 0) {
            timesUsed = 0L;
        }
        cassette.setTimesUsed(timesUsed);

        /*---------------------------*/
        /*   Decrese totalTimeUsed   */
        /*---------------------------*/
        Long totalDurUsed = cassette.getTotalTimesUsed();
        if (totalDurUsed == null) {
            totalDurUsed = 0L;
        }
        totalDurUsed--;
        if (totalDurUsed < 0) {
            totalDurUsed = 0L;
        }
        cassette.setTotalTimesUsed(totalDurUsed);
    }

    @Override
    public void cassetteCheckConditionForLoading(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, ObjectIdentifier portID, ObjectIdentifier cassetteID) {
        com.fa.cim.newcore.bo.durable.CimCassette aCassette = baseCoreFactory.getBO(com.fa.cim.newcore.bo.durable.CimCassette.class, cassetteID);
        if (!SorterHandler.containsFOSB(cassetteID)) {
            Validations.check(null == aCassette, new OmCode(retCodeConfig.getNotFoundCassette(), cassetteID.getValue()));

            //---------------------------------------
            // Check cassette interFabXferState
            //---------------------------------------
            String interFabTransferState = this.cassetteInterFabXferStateGet(objCommon, cassetteID);
            Validations.check(CimStringUtils.equals(interFabTransferState, BizConstant.SP_INTERFAB_XFERSTATE_TRANSFERRING),
                    new OmCode(retCodeConfig.getInterfabInvalidLotXferstateForReq(), cassetteID.getValue(), interFabTransferState));
        }

        //------------------------------//
        //                              //
        //   Judge NPW Loading or Not   //
        //                              //
        //------------------------------/
        boolean NPWFlag = false;
        //-------------------------
        //   Get eqp Object
        //-------------------------
        CimMachine aMachine = baseCoreFactory.getBO(CimMachine.class, equipmentID);
        Validations.check(null == aMachine, new OmCode(retCodeConfig.getNotFoundEqp(), equipmentID.getValue()));
        //--------------------
        //   Get port Object
        //--------------------
        PortResource aBasePort = aMachine.findPortResourceNamed(portID.getValue());
        Validations.check(aBasePort == null, new OmCode(retCodeConfig.getNotFoundPort(), portID.getValue()));
        com.fa.cim.newcore.bo.machine.CimPortResource aPort = (com.fa.cim.newcore.bo.machine.CimPortResource) aBasePort;
        //--------------------------------//
        //   Get Port's LoadPurposeType   //
        //--------------------------------//
        String portLoadPurposeType = aPort.getLoadPurposeType();
        //-----------------
        //   Set NPWFlag
        //-----------------
        if (BizConstant.SP_LOADPURPOSETYPE_SIDEDUMMYLOT.equals(portLoadPurposeType)
                || BizConstant.SP_LOADPURPOSETYPE_FILLERDUMMY.equals(portLoadPurposeType)
                || BizConstant.SP_LOADPURPOSETYPE_WAITINGMONITORLOT.equals(portLoadPurposeType)
                || BizConstant.SP_LOADPURPOSETYPE_OTHER.equals(portLoadPurposeType)) {
            NPWFlag = true;
        }
        //------------------------------
        //   Check SorterJob existence
        //------------------------------
        Infos.EquipmentLoadPortAttribute equipmentLoadPortAttribute = new Infos.EquipmentLoadPortAttribute();
        List<Infos.CassetteLoadPort> cassetteLoadPorts = new ArrayList<>();
        Infos.CassetteLoadPort cassetteLoadPort = new Infos.CassetteLoadPort();
        cassetteLoadPort.setPortID(portID);
        cassetteLoadPort.setCassetteID(cassetteID);
        cassetteLoadPorts.add(cassetteLoadPort);
        equipmentLoadPortAttribute.setCassetteLoadPortList(cassetteLoadPorts);
        equipmentLoadPortAttribute.setEquipmentID(equipmentID);

        Inputs.ObjWaferSorterJobCheckForOperation operation = new Inputs.ObjWaferSorterJobCheckForOperation();
        List<ObjectIdentifier> dummyIDs = null;
        operation.setEquipmentLoadPortAttribute(equipmentLoadPortAttribute);
        operation.setCassetteIDList(dummyIDs);
        operation.setLotIDList(dummyIDs);
        operation.setOperation(BizConstant.SP_OPERATION_LOADINGLOT);
        waferMethod.waferSorterSorterJobCheckForOperation(objCommon, operation);
        //-------------------------------------
        //   Check Condition for MultiLotType
        //-------------------------------------
        if (!NPWFlag && !SorterHandler.containsFOSB(cassetteID)) {
            //-------------------------------------------
            //   Get eqp's MultiRecipeCapability
            //-------------------------------------------
            String multiRecipeCapability = aMachine.getMultipleRecipeCapability();
            //---------------------------------
            //   Get cassette's MultiLotType
            //---------------------------------
            String multiLotType = aCassette.getMultiLotType();
            //---------------------------------
            //   Get cassette's Empty Flag
            //---------------------------------
            boolean emptyFlag = aCassette.isEmpty();
            if (!emptyFlag) {
                //-------------------------------------
                //   Get cassette's Cotrol Job ID
                //-------------------------------------
                boolean controlJobFlag = true;
                com.fa.cim.newcore.bo.product.CimControlJob aControlJob = aCassette.getControlJob();
                if (aControlJob == null) {
                    controlJobFlag = false;
                }
                /*-------------------------------------------------*/
                /*   Check MultiRecipeCapability VS MultiLotType   */
                /*-------------------------------------------------*/
                if (CimStringUtils.equals(multiRecipeCapability, BizConstant.SP_EQP_MULTIRECIPECAPABILITY_MULTIPLERECIPE)) {

                } else if (CimStringUtils.equals(multiRecipeCapability, BizConstant.SP_EQP_MULTIRECIPECAPABILITY_SINGLERECIPE)
                        && CimBooleanUtils.isTrue(controlJobFlag)) {

                } else if (CimStringUtils.equals(multiRecipeCapability, BizConstant.SP_EQP_MULTIRECIPECAPABILITY_SINGLERECIPE)
                        && CimBooleanUtils.isFalse(controlJobFlag)) {

                    if (CimStringUtils.equals(multiLotType, BizConstant.SP_CAS_MULTILOTTYPE_SINGLELOTSINGLERECIPE)
                            || CimStringUtils.equals(multiLotType, BizConstant.SP_CAS_MULTILOTTYPE_MULTILOTSINGLERECIPE)) {

                    } else {
                        Validations.check(retCodeConfig.getCassetteEquipmentConditionError());
                    }
                } else if (CimStringUtils.equals(multiRecipeCapability, BizConstant.SP_EQP_MULTIRECIPECAPABILITY_BATCH)) {
                    if (CimStringUtils.equals(multiLotType, BizConstant.SP_CAS_MULTILOTTYPE_SINGLELOTSINGLERECIPE)
                            || CimStringUtils.equals(multiLotType, BizConstant.SP_CAS_MULTILOTTYPE_MULTILOTSINGLERECIPE)) {
                    } else {
                        Validations.check(retCodeConfig.getCassetteEquipmentConditionError());
                    }
                }
            }
        }

        //--------------------------------------
        //   Check cassette's Transfer Status
        //--------------------------------------
        if (!SorterHandler.containsFOSB(cassetteID)) {
            String transferState = aCassette.getTransportState();
            if (!CimStringUtils.equals(transferState, BizConstant.SP_TRANSSTATE_STATIONOUT)
                    && !CimStringUtils.equals(transferState, BizConstant.SP_TRANSSTATE_MANUALOUT)
                    && !CimStringUtils.equals(transferState, BizConstant.SP_TRANSSTATE_EQUIPMENTOUT)
                    && !CimStringUtils.equals(transferState, BizConstant.SP_TRANSSTATE_INTERMEDIATEOUT)
                    && !CimStringUtils.equals(transferState, BizConstant.SP_TRANSSTATE_SHELFOUT)
                    && !CimStringUtils.equals(transferState, BizConstant.SP_TRANSSTATE_ABNORMALOUT)
                    && !CimStringUtils.equals(transferState, BizConstant.SP_UNDEFINED_STATE)) {
                throw new ServiceException(new OmCode(retCodeConfig.getInvalidCassetteTransferState(), transferState, aCassette.getIdentifier()));
            }
        }
        //--------------------------------------------------------
        //   If port's LoadPurposeType = _Other, no-need to Check
        //--------------------------------------------------------
        if (!CimStringUtils.equals(portLoadPurposeType, BizConstant.SP_LOADPURPOSETYPE_OTHER)) {
            //----------------------
            //   Get TransferState carrier=FOSB跳过验证
            //----------------------
            if (!SorterHandler.containsFOSB(cassetteID)) {
                String cassetteState = aCassette.getDurableState();
                if (!CimStringUtils.equals(BizConstant.CIMFW_DURABLE_AVAILABLE, cassetteState)
                        && !CimStringUtils.equals(BizConstant.CIMFW_DURABLE_INUSE, cassetteState)) {
                    throw new ServiceException(new OmCode(retCodeConfig.getInvalidCassetteState(), cassetteState, aCassette.getIdentifier()));
                }
            }        }
    }

    @Override
    public void cassetteCheckConditionForOpeComp(Infos.ObjCommon objCommon, List<Infos.StartCassette> startCassettes) {
        for (Infos.StartCassette startCassette : startCassettes) {
            if (ObjectIdentifier.isEmpty(startCassette.getCassetteID())) {
                break;
            }
            com.fa.cim.newcore.bo.durable.CimCassette cassette = baseCoreFactory.getBO(com.fa.cim.newcore.bo.durable.CimCassette.class, startCassette.getCassetteID());
            Validations.check(null == cassette, new OmCode(retCodeConfig.getNotFoundCassette(), startCassette.getCassetteID().getValue()));

            String transportState = cassette.getTransportState();
            Validations.check(!CimStringUtils.equals(BizConstant.SP_TRANSSTATE_EQUIPMENTIN, transportState), new OmCode(retCodeConfig.getInvalidCassetteTransferState(), transportState, cassette.getIdentifier()));
        }
        return;
    }

    @Override
    public Outputs.ObjCassetteUsageLimitationCheckOut cassetteUsageLimitationCheck(Infos.ObjCommon objCommon, ObjectIdentifier cassetteId) {
        Outputs.ObjCassetteUsageLimitationCheckOut outObject = new Outputs.ObjCassetteUsageLimitationCheckOut();

        /*----------------*/
        /*   Initialize   */
        /*----------------*/
        outObject.setUsageLimitOverFlag(false);
        outObject.setRunTimeOverFlag(false);
        outObject.setStartCountOverFlag(false);
        outObject.setPmTimeOverFlag(false);

        /*-------------------------*/
        /*   Get cassette Object   */
        /*-------------------------*/
        CimCassette cassette = baseCoreFactory.getBO(CimCassette.class, cassetteId);
        Validations.check(null == cassette, new OmCode(retCodeConfig.getNotFoundCassette(), ObjectIdentifier.fetchValue(cassetteId)));
        /*--------------------------*/
        /*   Get Usage Check Flag   */
        /*--------------------------*/
        boolean usageCheckRequiredFlag = CimBooleanUtils.isTrue(cassette.isUsageCheckRequired());
        if (CimBooleanUtils.isFalse(cassette.isUsageCheckRequired())) {
            return outObject;
        }

        /*--------------------------*/
        /*   Get Usage Limitation   */
        /*--------------------------*/
        outObject.setMaxRunTime((long) (cassette.getDurationLimit() / (60 * 100)));
        outObject.setStartCount(cassette.getTimeUsed());
        outObject.setMaxStartCount(cassette.getTimesUsedLimit().longValue());
        outObject.setIntervalBetweenPM(cassette.getIntervalBetweenPM());

        if (outObject.getMaxRunTime() == 0 && outObject.getMaxStartCount() == 0 && outObject.getIntervalBetweenPM() == 0) {
            return outObject;
        }

        /*-------------------------------------*/
        /*   Calcurate Run Time from Last PM   */
        /*-------------------------------------*/
        outObject.setRunTime(0);
        outObject.setPassageTimeFromLastPM((objCommon.getTimeStamp().getReportTimeStamp().getTime() - cassette.getLastMaintenanceTimeStamp().getTime()) / (1000 * 60));       //minutes

        /*--------------------------*/
        /*   Set Flag Information   */
        /*--------------------------*/
        if (outObject.getStartCount() >= outObject.getMaxStartCount()) {
            outObject.setUsageLimitOverFlag(true);
            outObject.setStartCountOverFlag(true);
        }

        if (outObject.getPassageTimeFromLastPM() >= outObject.getIntervalBetweenPM()) {
            outObject.setUsageLimitOverFlag(true);
            outObject.setPmTimeOverFlag(true);
        }

        /*-------------------------*/
        /*   Create Message Text   */
        /*-------------------------*/
        if (outObject.isUsageLimitOverFlag()) {
            String stringBuffer = "<<< cassette Usage Limitation Over >>>" +
                    "\n    cassette ID           : " + cassetteId +
                    "\n    Start Count           : " + outObject.getStartCount() +
                    "\n    Max Start Count       : " + outObject.getMaxStartCount() +
                    "\n    Passage from PM (min) : " + outObject.getPassageTimeFromLastPM() +
                    "\n    Interval for PM (min) : " + outObject.getIntervalBetweenPM();
            outObject.setMessageText(stringBuffer);
        }

        return outObject;
    }

    @Override
    public Infos.LotLocationInfo cassetteLocationInfoGetDR(ObjectIdentifier cassetteID) {
        Infos.LotLocationInfo lotLocationInfo = new Infos.LotLocationInfo();
        CimCassetteDO cimCassetteExample = new CimCassetteDO();
        cimCassetteExample.setCassetteID(ObjectIdentifier.fetchValue(cassetteID));
        CimCassetteDO cimCassetteDO = cimJpaRepository.findOne(Example.of(cimCassetteExample)).orElse(null);
        Validations.check(cimCassetteDO == null, new OmCode(retCodeConfig.getNotFoundCassette(), cassetteID.getValue()));
        String transportState = cimCassetteDO.getTransferState();
        lotLocationInfo.setCassetteID(cassetteID);
        lotLocationInfo.setTransferStatus(transportState);
        lotLocationInfo.setTransferReserveUserID(cimCassetteDO.getReserveUserID());
        lotLocationInfo.setCassetteCategory(cimCassetteDO.getCassetteCategory());
        log.trace("StringUtils.equals(transportState, BizConstant.SP_TRANSSTATE_EQUIPMENTIN)\n" +
                        "                || StringUtils.equals(transportState, BizConstant.SP_TRANSSTATE_EQUIPMENTOUT) : {}",
                CimStringUtils.equals(transportState, BizConstant.SP_TRANSSTATE_EQUIPMENTIN)
                        || CimStringUtils.equals(transportState, BizConstant.SP_TRANSSTATE_EQUIPMENTOUT));

        if (CimStringUtils.equals(transportState, BizConstant.SP_TRANSSTATE_EQUIPMENTIN)
                || CimStringUtils.equals(transportState, BizConstant.SP_TRANSSTATE_EQUIPMENTOUT)) {
            lotLocationInfo.setEquipmentID(ObjectIdentifier.build(cimCassetteDO.getEquipmentID(), cimCassetteDO.getEquipmentObj()));
        } else {
            lotLocationInfo.setStockerID(ObjectIdentifier.build(cimCassetteDO.getEquipmentID(), cimCassetteDO.getEquipmentObj()));
        }
        return lotLocationInfo;
    }

    @Override
    public Infos.LotListInCassetteInfo cassetteLotIDListGetDR(Infos.ObjCommon objCommon, ObjectIdentifier cassetteID) {
        Infos.LotListInCassetteInfo lotListInCassetteInfo = new Infos.LotListInCassetteInfo();
        // Get lot information in cassette from FRCTRLJOB_CAST_LOT
        CimCassette cast = baseCoreFactory.getBO(CimCassette.class, cassetteID);
        Validations.check(null == cast, new OmCode(retCodeConfig.getNotFoundCassette(), ObjectIdentifier.fetchValue(cassetteID)));

        //Get Lot information in Cassette  from OMCARRIER_LOT
        CimCassetteLotDO example = new CimCassetteLotDO();
        example.setReferenceKey(cast.getPrimaryKey());
        List<ObjectIdentifier> lotIDs = cimJpaRepository.findAll(Example.of(example)).stream()
                .map(data -> ObjectIdentifier.build(data.getLotID(), data.getLotObj()))
                .collect(Collectors.toList());
        if (CimArrayUtils.isNotEmpty(lotIDs)) {
            lotIDs.sort((l1, l2) -> {
                return l1.getValue().compareTo(l2.getValue());
            });
        }
        lotListInCassetteInfo.setLotIDList(lotIDs);
        lotListInCassetteInfo.setCassetteID(ObjectIdentifier.build(cast.getIdentifier(), cast.getPrimaryKey()));
        lotListInCassetteInfo.setMultiLotType(cast.getMultiLotType());
        lotListInCassetteInfo.setRelationFoupFlag(CimBooleanUtils.isTrue(cast.getRelationFoupFlag()));
        return lotListInCassetteInfo;
    }

    @Override
    public void cassetteCheckConditionForExchange(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, List<Infos.WaferTransfer> waferXferList) {
        log.debug("cassetteCheckConditionForExchange(): enter in");
        //Collect cassette ID of input parameter
        List<ObjectIdentifier> cassetteIDList = new ArrayList<>();
        collectCassetteIDsOfParams(waferXferList, cassetteIDList);
        //Collect cassette's eqp ID / cassette ObjRef
        List<com.fa.cim.newcore.bo.durable.CimCassette> cassetteList = new ArrayList<>();
        List<String> strLoadedEquipments = new ArrayList<>();
        if (!CimArrayUtils.isEmpty(cassetteIDList)) {
            for (ObjectIdentifier cassetteID : cassetteIDList) {
                com.fa.cim.newcore.bo.durable.CimCassette cassette = baseCoreFactory.getBO(com.fa.cim.newcore.bo.durable.CimCassette.class, cassetteID);
                Validations.check(null == cassette, retCodeConfig.getNotFoundCassette());
                String tmpCassetteTransportState = cassette.getTransportState();
                Machine tmpEquipment = cassette.currentAssignedMachine();
                cassetteList.add(cassette);
                if (tmpEquipment != null && CimStringUtils.equals(tmpCassetteTransportState, BizConstant.SP_TRANSSTATE_EQUIPMENTIN)) {
                    log.debug("cassetteCheckConditionForExchange(): tmpMachine is not nil");
                    strLoadedEquipments.add(tmpEquipment.getIdentifier());
                }
            }
        }

        //Check all cassette is on the same eqp
        String tmpLoadedEquipmentID = BizConstant.EMPTY;
        if (!CimArrayUtils.isEmpty(strLoadedEquipments)) {
            tmpLoadedEquipmentID = strLoadedEquipments.get(0);
            for (int i = 1; i < strLoadedEquipments.size(); i++) {
                if (!CimStringUtils.equals(tmpLoadedEquipmentID, strLoadedEquipments.get(i))) {
                    throw new ServiceException(retCodeConfig.getEquipmentOfCassetteNotSame());
                }
            }
        }
        //Check Carriers are loaded to eqp or not
        if (CimStringUtils.isEmpty(equipmentID.getValue()) && CimStringUtils.isEmpty(tmpLoadedEquipmentID)) {
            throw new ServiceException(retCodeConfig.getCassetteNotOnEquipment());
        }
        //Check eqp of cassette and input eqp is same or not
        Validations.check(!CimStringUtils.equals(equipmentID.getValue(), tmpLoadedEquipmentID), retCodeConfig.getCassetteEquipmentDifferent());
        //Get object reference of PosMachine
        CimMachine equipment = baseCoreFactory.getBO(CimMachine.class, equipmentID);
        Validations.check(null == equipment, new OmCode(retCodeConfig.getNotFoundEqp(), equipmentID.getValue()));
        //Get eqp attributes
        Boolean cassetteExchangeType = equipment.isCassetteChangeRequired();
        //Check eqp type
        Validations.check(CimBooleanUtils.isFalse(cassetteExchangeType), retCodeConfig.getInvalidMachineId());
        //Check cassette control jobs
        List<ObjectIdentifier> cassetteControlJobIDs = new ArrayList<>();
        cassetteList.forEach(cassette -> cassetteControlJobIDs.add(cassette.getControlJobID()));
        for (int i = 1; i < cassetteList.size(); i++) {
            Validations.check(!ObjectIdentifier.equalsWithValue(cassetteControlJobIDs.get(0), cassetteControlJobIDs.get(i)), retCodeConfig.getNotSameControlJobId());
        }
    }

    @Override
    public List<Infos.InventoriedLotInfo> cassettePositionUpdateByStockerInventory(Infos.ObjCommon objCommon, ObjectIdentifier stockerID, Infos.ShelfPosition shelfPosition, List<Infos.InventoryLotInfo> carrierInfos) {
        List<Infos.InventoriedLotInfo> inventoriedLotInfos = new ArrayList<>();
        int errorCount = 0;
        int txSeqLen = CimArrayUtils.getSize(carrierInfos);

        CimStorageMachine stocker = baseCoreFactory.getBO(CimStorageMachine.class, stockerID);
        Validations.check(null == stocker, retCodeConfig.getNotFoundStocker());

        String stockerType = stocker.getStockerType();

        CimPerson person = baseCoreFactory.getBO(CimPerson.class, objCommon.getUser().getUserID());
        Validations.check(null == person, new OmCode(retCodeConfig.getNotFoundPerson(), objCommon.getUser().getUserID().getValue()));

        String sql = "SELECT * FROM OMCARRIER WHERE EQP_ID = ?1 AND XFER_STATE IN (?2, ?3, ?4, ?5, ?6)";
        List<CimCassetteDO> cassettes = cimJpaRepository.query(sql, CimCassetteDO.class, ObjectIdentifier.fetchValue(stockerID),
                BizConstant.SP_TRANSSTATE_STATIONIN, BizConstant.SP_TRANSSTATE_MANUALIN, BizConstant.SP_TRANSSTATE_BAYIN,
                BizConstant.SP_TRANSSTATE_INTERMEDIATEIN, BizConstant.SP_TRANSSTATE_ABNORMALIN);
        if (CimArrayUtils.isNotEmpty(cassettes)) {
            for (CimCassetteDO cassette : cassettes) {
                Infos.InventoriedLotInfo inventoriedLotInfo = new Infos.InventoriedLotInfo();
                inventoriedLotInfo.setCassetteID(new ObjectIdentifier(cassette.getCassetteID(), cassette.getId()));
                inventoriedLotInfos.add(inventoriedLotInfo);
            }
        }

        /*-------------------------------------*/
        /*   Change to AbnormalOut Procedure   */
        /*-------------------------------------*/
        int pptSeqLen = CimArrayUtils.getSize(inventoriedLotInfos);
        for (int i = 0; i < pptSeqLen; i++) {
            boolean foundFlag = false;
            Infos.InventoriedLotInfo inventoriedLotInfo = inventoriedLotInfos.get(i);
            for (int j = 0; j < txSeqLen; j++) {
                Infos.InventoryLotInfo carrierInfo = carrierInfos.get(j);
                if (ObjectIdentifier.equalsWithValue(inventoriedLotInfo.getCassetteID(), carrierInfo.getCassetteID())) {
                    foundFlag = true;
                    break;
                }
            }

            if (CimBooleanUtils.isTrue(foundFlag)) {
                continue;
            } else {
                /*-------------------------*/
                /*   Get cassette Object   */
                /*-------------------------*/
                boolean cassetteFound = false;

                CimCassette cassette = baseCoreFactory.getBO(CimCassette.class, inventoriedLotInfo.getCassetteID());
                if (cassette == null) {
                    errorCount++;
                    continue;
                }

                /*---------------------------*/
                /*   Change Transfer State   */
                /*---------------------------*/
                cassette.makeAbnormalOut();
                CimPerson aPPTPerson = baseCoreFactory.getBO(CimPerson.class, ObjectIdentifier.build(BizConstant.SP_PPTSVCMGR_PERSON, ""));
                Validations.check(null == aPPTPerson, retCodeConfig.getNotFoundPerson());

                cassette.setTransferStatusChangedTimeStamp(objCommon.getTimeStamp().getReportTimeStamp());
                cassette.setLastClaimedTimeStamp(objCommon.getTimeStamp().getReportTimeStamp());
                cassette.setLastClaimedPerson(person);

                //Step1 - durableXferStatusChangeEvent_Make
                Inputs.DurableXferStatusChangeEventMakeParams durableXferStatusChangeEventMakeParams = new Inputs.DurableXferStatusChangeEventMakeParams();
                durableXferStatusChangeEventMakeParams.setTransactionID("");
                durableXferStatusChangeEventMakeParams.setDurableID(inventoriedLotInfos.get(i).getCassetteID());
                durableXferStatusChangeEventMakeParams.setDurableType(BizConstant.SP_DURABLECAT_CASSETTE);
                durableXferStatusChangeEventMakeParams.setActionCode(BizConstant.SP_DURABLEEVENT_ACTION_XFERSTATECHANGE);
                durableXferStatusChangeEventMakeParams.setDurableStatus("");
                durableXferStatusChangeEventMakeParams.setXferStatus(BizConstant.SP_TRANSSTATE_ABNORMALOUT);
                durableXferStatusChangeEventMakeParams.setTransferStatusChangeTimeStamp(CimDateUtils.convertToSpecString(objCommon.getTimeStamp().getReportTimeStamp()));
                durableXferStatusChangeEventMakeParams.setLocation(stockerID.getValue() + CimObjectUtils.toString(shelfPosition));
                durableXferStatusChangeEventMakeParams.setClaimMemo("");
                eventMethod.durableXferStatusChangeEventMake(objCommon, durableXferStatusChangeEventMakeParams);
            }
        }

        /*--------------------------------------------*/
        /*   Change to ManualIn / ShelfIn Procedure   */
        /*--------------------------------------------*/
        List<ObjectIdentifier> processingCastIDs = new ArrayList<>();
        List<ObjectIdentifier> otherErrorCassetteIDs = new ArrayList<>();
        List<ObjectIdentifier> reservedCassetteIDs = new ArrayList<>();
        List<ObjectIdentifier> sorterCassetteIDs = new ArrayList<>();
        List<ObjectIdentifier> notOnEqpCastIDs = new ArrayList<>();

        List<Infos.InventoriedLotInfo> inventoriedLotInfoList = new ArrayList<>();

        for (int i = 0; i < txSeqLen; i++) {
            boolean foundFlag = false;
            Infos.InventoryLotInfo carrierInfo = carrierInfos.get(i);

            for (int j = 0; j < inventoriedLotInfos.size(); j++) {
                Infos.InventoriedLotInfo inventoriedLotInfo = inventoriedLotInfos.get(j);
                if (ObjectIdentifier.equalsWithValue(carrierInfo.getCassetteID(), inventoriedLotInfo.getCassetteID())) {
                    foundFlag = true;
                    break;
                }
            }

            if (CimBooleanUtils.isTrue(foundFlag)) {
                Infos.InventoriedLotInfo inventoriedLotInfo = new Infos.InventoriedLotInfo();
                inventoriedLotInfo.setCassetteID(carrierInfo.getCassetteID());
                inventoriedLotInfo.setReturnCode(String.valueOf(retCodeConfig.getSucc().getCode()));
                inventoriedLotInfoList.add(inventoriedLotInfo);
                continue;
            } else {
                /*-------------------------*/
                /*   Get cassette Object   */
                /*-------------------------*/
                CimCassette cassette = baseCoreFactory.getBO(CimCassette.class, carrierInfo.getCassetteID());
                if (cassette == null) {
                    Infos.InventoriedLotInfo inventoriedLotInfo = new Infos.InventoriedLotInfo();
                    inventoriedLotInfo.setCassetteID(carrierInfo.getCassetteID());
                    inventoriedLotInfo.setReturnCode(String.valueOf(retCodeConfig.getNotFoundCassette().getCode()));
                    inventoriedLotInfoList.add(inventoriedLotInfo);

                    errorCount++;
                    otherErrorCassetteIDs.add(new ObjectIdentifier(inventoriedLotInfo.getCassetteID().getValue(), inventoriedLotInfo.getCassetteID().getReferenceKey()));
                    continue;
                }

                /*-------------------------------*/
                /*   Get cassette's Xfer State   */
                /*-------------------------------*/
                String transferState = cassette.getTransportState();
                if (CimStringUtils.equals(BizConstant.SP_TRANSSTATE_EQUIPMENTIN, transferState)) {
                    /*--------------------*/
                    /*   Get lot Object   */
                    /*--------------------*/
                    ObjectIdentifier cassetteId = carrierInfo.getCassetteID();

                    //Step2 - cassette_lotList_GetDR
                    Infos.LotListInCassetteInfo cassetteLotListOut = null;
                    try {
                        cassetteLotListOut = this.cassetteGetLotList(objCommon, cassetteId);
                    } catch (ServiceException e) {
                        Infos.InventoriedLotInfo inventoriedLotInfo = new Infos.InventoriedLotInfo();
                        inventoriedLotInfo.setCassetteID(cassetteId);
                        inventoriedLotInfo.setReturnCode(String.valueOf(retCodeConfig.getNotFoundCassette().getCode()));
                        inventoriedLotInfoList.add(inventoriedLotInfo);

                        errorCount++;
                        otherErrorCassetteIDs.add(inventoriedLotInfo.getCassetteID());
                        continue;
                    }

                    List<ObjectIdentifier> lotIDList = cassetteLotListOut.getLotIDList();
                    boolean continueFlag = false;
                    int lotLen = CimArrayUtils.getSize(lotIDList);
                    for (int j = 0; j < lotLen; j++) {
                        ObjectIdentifier lotID = lotIDList.get(j);
                        String lotProcessStateGet = null;
                        try {
                            lotProcessStateGet = lotMethod.lotProcessStateGet(objCommon, lotID);
                        } catch (ServiceException e) {
                            Infos.InventoriedLotInfo inventoriedLotInfo = new Infos.InventoriedLotInfo();
                            inventoriedLotInfo.setCassetteID(carrierInfo.getCassetteID());
                            inventoriedLotInfo.setReturnCode(String.valueOf(retCodeConfig.getNotFoundCassette().getCode()));
                            inventoriedLotInfoList.add(inventoriedLotInfo);

                            errorCount++;
                            continueFlag = true;
                            otherErrorCassetteIDs.add(new ObjectIdentifier(inventoriedLotInfo.getCassetteID().getValue(), inventoriedLotInfo.getCassetteID().getReferenceKey()));
                            break;
                        }



                        /*-------------------------*/
                        /*   Omit Processing lot   */
                        /*-------------------------*/
                        //Step3 - lot_processState_Get
                        if (CimStringUtils.equals(BizConstant.SP_LOT_PROCSTATE_PROCESSING, lotProcessStateGet)) {
                            Infos.InventoriedLotInfo inventoriedLotInfo = new Infos.InventoriedLotInfo();
                            inventoriedLotInfo.setCassetteID(cassetteId);
                            inventoriedLotInfo.setReturnCode(String.valueOf(retCodeConfig.getNotFoundCassette().getCode()));
                            inventoriedLotInfoList.add(inventoriedLotInfo);
                            errorCount++;
                            continueFlag = true;
                            processingCastIDs.add(new ObjectIdentifier(inventoriedLotInfo.getCassetteID().getValue(), inventoriedLotInfo.getCassetteID().getReferenceKey()));
                            break;
                        }
                    }

                    if (CimBooleanUtils.isTrue(continueFlag)) {
                        continue;
                    }

                    /*----------------------------------------------------------------*/
                    /*   Omit reserved lot (check controljob existence on cassette)   */
                    /*----------------------------------------------------------------*/
                    //Step4 - cassette_controlJobID_Get
                    ObjectIdentifier controlJobId = null;
                    try {
                        controlJobId = this.cassetteControlJobIDGet(objCommon, cassetteId);
                    } catch (ServiceException e) {
                        Infos.InventoriedLotInfo inventoriedLotInfo = new Infos.InventoriedLotInfo();
                        inventoriedLotInfo.setCassetteID(carrierInfo.getCassetteID());
                        inventoriedLotInfo.setReturnCode(String.valueOf(retCodeConfig.getNotFoundCassette().getCode()));
                        inventoriedLotInfoList.add(inventoriedLotInfo);
                        errorCount++;
                        otherErrorCassetteIDs.add(cassetteId);
                        continue;
                    }
                    if (!ObjectIdentifier.isEmpty(controlJobId)) {
                        Infos.InventoriedLotInfo inventoriedLotInfo = new Infos.InventoriedLotInfo();
                        inventoriedLotInfo.setCassetteID(cassetteId);
                        inventoriedLotInfo.setReturnCode(String.valueOf(retCodeConfig.getNotFoundCassette().getCode()));
                        inventoriedLotInfoList.add(inventoriedLotInfo);
                        errorCount++;
                        reservedCassetteIDs.add(cassetteId);
                        continue;
                    }

                    /*----------------------------------------------*/
                    /*   Get cassette's AssignedToMachine (EqpID)   */
                    /*----------------------------------------------*/
                    Machine aMachine = cassette.currentAssignedMachine();
                    if (null == aMachine) {
                        Infos.InventoriedLotInfo inventoriedLotInfo = new Infos.InventoriedLotInfo();
                        inventoriedLotInfo.setCassetteID(carrierInfo.getCassetteID());
                        inventoriedLotInfo.setReturnCode(String.valueOf(retCodeConfig.getCtrljobEqpctnpstUnmatch().getCode()));
                        inventoriedLotInfoList.add(inventoriedLotInfo);
                        errorCount++;
                        otherErrorCassetteIDs.add(cassetteId);
                        continue;
                    }

                    if (CimBooleanUtils.isTrue(aMachine.isStorageMachine())) {
                        Infos.InventoriedLotInfo inventoriedLotInfo = new Infos.InventoriedLotInfo();
                        inventoriedLotInfo.setCassetteID(carrierInfo.getCassetteID());
                        inventoriedLotInfo.setReturnCode(String.valueOf(retCodeConfig.getCtrljobEqpctnpstUnmatch().getCode()));
                        inventoriedLotInfoList.add(inventoriedLotInfo);
                        errorCount++;
                        otherErrorCassetteIDs.add(cassetteId);
                        continue;
                    }

                    /*-------------------------------------------------------*/
                    /* At here, the carrier can be removed from eqp.   */
                    /*                                                       */
                    /*  Check eqp category (FB or IB)                  */
                    /*  If eqp is FB, then                             */
                    /*   * Remove carrier from port.                         */
                    /*                                                       */
                    /*  If eqp is IB and carrier is on port , then     */
                    /*   * Remove carrier from port.(also Remove shelf info) */
                    /*   * If NPW reserved , remove NPW from shelf           */
                    /*                                                       */
                    /*  If eqp is IB and carrier is in shelf, then     */
                    /*   * Remove carrier from shelf.                        */
                    /*-------------------------------------------------------*/
                    //Step5 equipment_brInfoForInternalBuffer_GetDR__120
                    ObjectIdentifier equipmentID = ObjectIdentifier.build(aMachine.getIdentifier(), aMachine.getPrimaryKey());

                    Infos.EqpBrInfoForInternalBuffer strEquipmentBrInfoForInternalBufferGetDROut = null;
                    String messager = null;
                    try {
                        strEquipmentBrInfoForInternalBufferGetDROut = equipmentMethod.equipmentBrInfoForInternalBufferGetDR(objCommon, equipmentID);
                    } catch (ServiceException e) {
                        Infos.InventoriedLotInfo inventoriedLotInfo = new Infos.InventoriedLotInfo();
                        inventoriedLotInfo.setCassetteID(carrierInfo.getCassetteID());
                        messager = e.getMessage();
                        inventoriedLotInfo.setReturnCode(messager);
                        inventoriedLotInfoList.add(inventoriedLotInfo);
                        otherErrorCassetteIDs.add(cassetteId);
                        errorCount++;
                        continue;
                    }


                    //--------------------------------------------------------
                    //  Omit Sorter Equipment
                    //--------------------------------------------------------
                    if (CimStringUtils.equals(strEquipmentBrInfoForInternalBufferGetDROut.getEquipmentCategory(), BizConstant.SP_MC_CATEGORY_WAFERSORTER)) {
                        Infos.InventoriedLotInfo inventoriedLotInfo = new Infos.InventoriedLotInfo();
                        inventoriedLotInfo.setCassetteID(carrierInfo.getCassetteID());
                        inventoriedLotInfo.setReturnCode(messager);
                        inventoriedLotInfoList.add(inventoriedLotInfo);
                        sorterCassetteIDs.add(cassetteId);
                        errorCount++;
                        continue;
                    }
                    //--------------------------------------------------------
                    //   Check for F/B equipment
                    //--------------------------------------------------------
                    else if (!CimStringUtils.equals(strEquipmentBrInfoForInternalBufferGetDROut.getEquipmentCategory(), BizConstant.SP_MC_CATEGORY_INTERNALBUFFER)) {
                        //--------------------------------------
                        //  Get loaded port
                        //--------------------------------------
                        Infos.EqpPortInfo strEquipmentPortInfoGetDROut = null;
                        try {
                            strEquipmentPortInfoGetDROut = equipmentMethod.equipmentPortInfoGet(objCommon, equipmentID);
                        } catch (ServiceException e) {
                            Infos.InventoriedLotInfo inventoriedLotInfo = new Infos.InventoriedLotInfo();
                            inventoriedLotInfo.setCassetteID(carrierInfo.getCassetteID());
                            inventoriedLotInfo.setReturnCode(messager);
                            inventoriedLotInfoList.add(inventoriedLotInfo);
                            otherErrorCassetteIDs.add(cassetteId);
                            errorCount++;
                            continue;
                        }


                        ObjectIdentifier targetPortID = null;
                        String targetPortGrp = null;
                        int pLen = CimArrayUtils.getSize(strEquipmentPortInfoGetDROut.getEqpPortStatuses());
                        for (int j = 0; j < pLen; j++) {
                            if (ObjectIdentifier.equalsWithValue(cassetteId, strEquipmentPortInfoGetDROut.getEqpPortStatuses().get(j).getLoadedCassetteID())) {
                                targetPortID = strEquipmentPortInfoGetDROut.getEqpPortStatuses().get(j).getPortID();
                                targetPortGrp = strEquipmentPortInfoGetDROut.getEqpPortStatuses().get(j).getPortGroup();
                                break;
                            }
                        }

                        if (ObjectIdentifier.isEmpty(targetPortID)) {
                            Infos.InventoriedLotInfo inventoriedLotInfo = new Infos.InventoriedLotInfo();
                            inventoriedLotInfo.setCassetteID(carrierInfo.getCassetteID());
                            inventoriedLotInfo.setReturnCode(messager);
                            inventoriedLotInfoList.add(inventoriedLotInfo);
                            otherErrorCassetteIDs.add(cassetteId);
                            errorCount++;
                            continue;
                        }

                        //--------------------------------------
                        //  Remove from equipment port
                        //--------------------------------------
                        equipmentMethod.equipmentLoadLotDelete(objCommon, equipmentID, targetPortID, cassetteId);
                    } else {

                        Infos.EqpPortInfo eqpBrInfoForInternalBuffer = null;
                        try {
                            eqpBrInfoForInternalBuffer = equipmentMethod.equipmentPortInfoForInternalBufferGetDR(objCommon, equipmentID);
                        } catch (ServiceException e) {
                            Infos.InventoriedLotInfo inventoriedLotInfo = new Infos.InventoriedLotInfo();
                            inventoriedLotInfo.setCassetteID(carrierInfo.getCassetteID());
                            inventoriedLotInfo.setReturnCode(String.valueOf(retCodeConfig.getNotFoundBufferResource().getCode()));
                            inventoriedLotInfoList.add(inventoriedLotInfo);
                            otherErrorCassetteIDs.add(cassetteId);
                            errorCount++;
                            continue;
                        }


                        int pLen = CimArrayUtils.getSize(eqpBrInfoForInternalBuffer.getEqpPortStatuses());
                        ObjectIdentifier targetPortID = null;
                        for (int j = 0; j < pLen; j++) {
                            if (ObjectIdentifier.equalsWithValue(cassetteId, eqpBrInfoForInternalBuffer.getEqpPortStatuses().get(j).getLoadedCassetteID())) {
                                targetPortID = eqpBrInfoForInternalBuffer.getEqpPortStatuses().get(j).getPortID();
                                break;
                            }
                        }
                        if (ObjectIdentifier.isEmpty(targetPortID)) {
                            CimMachine aPosMachine = baseCoreFactory.getBO(CimMachine.class, equipmentID);

                            List<BufferResource> aBufferResourceSeq = aPosMachine.allBufferResources();

                            ObjectIdentifier targetMtrlLocID;
                            int lenBufferResource = CimArrayUtils.getSize(aBufferResourceSeq);

                            CimMaterialLocation aMaterialLocation = null;

                            for (int j = 0; j < lenBufferResource; j++) {
                                if (null == aBufferResourceSeq.get(j)) {
                                    Infos.InventoriedLotInfo inventoriedLotInfo = new Infos.InventoriedLotInfo();
                                    inventoriedLotInfo.setCassetteID(carrierInfo.getCassetteID());
                                    inventoriedLotInfo.setReturnCode(String.valueOf(retCodeConfig.getNotFoundBufferResource().getCode()));
                                    inventoriedLotInfoList.add(inventoriedLotInfo);
                                    otherErrorCassetteIDs.add(cassetteId);
                                    continueFlag = true;
                                    errorCount++;
                                    break;
                                }


                                List<MaterialLocation> aMaterialLocationSeq = aBufferResourceSeq.get(j).allMaterialLocations();

                                int lenShelfInBuffer = CimArrayUtils.getSize(aMaterialLocationSeq);

                                for (int k = 0; k < lenShelfInBuffer; k++) {
                                    //----------------------------------------------------------
                                    //  Get contained material
                                    //----------------------------------------------------------
                                    if (null == aMaterialLocationSeq.get(k)) {
                                        Infos.InventoriedLotInfo inventoriedLotInfo = new Infos.InventoriedLotInfo();
                                        inventoriedLotInfo.setCassetteID(carrierInfo.getCassetteID());
                                        inventoriedLotInfo.setReturnCode(String.valueOf(retCodeConfig.getNotFoundMaterialLocation().getCode()));
                                        inventoriedLotInfoList.add(inventoriedLotInfo);
                                        continueFlag = true;
                                        otherErrorCassetteIDs.add(cassetteId);
                                        errorCount++;
                                        break;
                                    }

                                    Material aContainedMaterial = aMaterialLocationSeq.get(k).getMaterial();

                                    CimCassette aContainedCassette = (CimCassette) aContainedMaterial;

                                    if (null != aContainedCassette) {
                                        String loadedCarrierID = aContainedCassette.getIdentifier();

                                        if (ObjectIdentifier.equalsWithValue(loadedCarrierID, carrierInfo.getCassetteID())) {
                                            aMaterialLocation = (CimMaterialLocation) aMaterialLocationSeq.get(k);
                                            break;
                                        }
                                    }
                                }

                                if (CimBooleanUtils.isTrue(continueFlag)) {
                                    break;
                                }
                            }

                            if (CimBooleanUtils.isTrue(continueFlag)) {
                                continue;
                            }

                            if (null == aMaterialLocation) {
                                Infos.InventoriedLotInfo inventoriedLotInfo = new Infos.InventoriedLotInfo();
                                inventoriedLotInfo.setCassetteID(carrierInfo.getCassetteID());
                                inventoriedLotInfo.setReturnCode(String.valueOf(retCodeConfig.getNotFoundMaterialLocation().getCode()));
                                inventoriedLotInfoList.add(inventoriedLotInfo);
                                notOnEqpCastIDs.add(carrierInfo.getCassetteID());
                                errorCount++;
                                continue;
                            }

                            /***********************************/
                            /*  Clear reservedUnloadPortID     */
                            /***********************************/
                            aMaterialLocation.setReservedUnloadPort(null);

                            /***********************************/
                            /*  Clear containedCarrierID       */
                            /***********************************/
                            aMaterialLocation.materialSent();

                            /********************************/
                            /*    Remove MachineCassette    */
                            /********************************/
                            aPosMachine.removeCassette(cassette);

                        } else {
                            equipmentMethod.equipmentLoadLotDeleteForInternalBuffer(objCommon, equipmentID, targetPortID, carrierInfo.getCassetteID());
                        }
                    }
                }

                /*---------------------------*/
                /*   Change Transfer State   */
                /*---------------------------*/
                cassette.assignToMachine(stocker);
                if (!CimObjectUtils.isEmpty(shelfPosition)) {
                    cassette.setShelfPosition(shelfPosition.getShelfPositionX(), shelfPosition.getShelfPositionY(), shelfPosition.getShelfPositionZ()); //for e-rack, add by nyx
                }

                if (CimStringUtils.equals(BizConstant.SP_STOCKER_TYPE_INTERM, stockerType)) {
                    cassette.makeIntermediateIn();
                } else {
                    cassette.makeManualIn();
                }
                cassette.setTransferStatusChangedTimeStamp(objCommon.getTimeStamp().getReportTimeStamp());
                cassette.setLastClaimedTimeStamp(objCommon.getTimeStamp().getReportTimeStamp());
                cassette.setLastClaimedPerson(person);

                Inputs.DurableXferStatusChangeEventMakeParams durableXferStatusChangeEventMakeParams = new Inputs.DurableXferStatusChangeEventMakeParams();
                durableXferStatusChangeEventMakeParams.setTransactionID("");
                durableXferStatusChangeEventMakeParams.setDurableID(carrierInfos.get(i).getCassetteID());
                durableXferStatusChangeEventMakeParams.setDurableType(BizConstant.SP_DURABLECAT_CASSETTE);
                durableXferStatusChangeEventMakeParams.setActionCode(BizConstant.SP_DURABLEEVENT_ACTION_XFERSTATECHANGE);
                durableXferStatusChangeEventMakeParams.setDurableStatus("");
                durableXferStatusChangeEventMakeParams.setTransferStatusChangeTimeStamp(CimDateUtils.convertToSpecString(objCommon.getTimeStamp().getReportTimeStamp()));
                durableXferStatusChangeEventMakeParams.setLocation(stockerID.getValue() + CimObjectUtils.toString(shelfPosition));
                durableXferStatusChangeEventMakeParams.setClaimMemo("");
                if (CimStringUtils.equals(stockerType, BizConstant.SP_STOCKER_TYPE_INTERM)) {
                    //Step10 - durableXferStatusChangeEvent_Make
                    durableXferStatusChangeEventMakeParams.setXferStatus(BizConstant.SP_TRANSSTATE_INTERMEDIATEIN);
                    eventMethod.durableXferStatusChangeEventMake(objCommon, durableXferStatusChangeEventMakeParams);
                } else {
                    //Step11 - durableXferStatusChangeEvent_Make
                    durableXferStatusChangeEventMakeParams.setXferStatus(BizConstant.SP_TRANSSTATE_MANUALIN);
                    eventMethod.durableXferStatusChangeEventMake(objCommon, durableXferStatusChangeEventMakeParams);
                }

                Infos.InventoriedLotInfo inventoriedLotInfo = new Infos.InventoriedLotInfo();
                inventoriedLotInfo.setCassetteID(carrierInfo.getCassetteID());
                inventoriedLotInfo.setReturnCode(String.valueOf(retCodeConfig.getSucc().getCode()));
                inventoriedLotInfoList.add(inventoriedLotInfo);
            }
        }
        if (errorCount == 0) {
            //omCode = retCodeConfig.getSucc();
        } else if (errorCount == inventoriedLotInfoList.size()) {
            throw new ServiceException(retCodeConfig.getAllCassetteInventoryDataError());
        } else {
            /*--------------------*/
            /*  Set reason text   */
            /*--------------------*/
            StringBuffer reasonText = new StringBuffer();
            if (processingCastIDs.size() > 0) {
                reasonText.append("[Information : lot processing : Following reported carriers are 'EI', but include processing lot. Its location was not changed.] =>").append("\n");
                processingCastIDs.forEach(x -> {
                    reasonText.append(x.getValue());
                    if (!x.getValue().equals(processingCastIDs.get(processingCastIDs.size() - 1).getValue())) {
                        reasonText.append(",");
                    }
                });
            }

            if (reservedCassetteIDs.size() > 0) {
                if (CimStringUtils.isNotEmpty(reasonText.toString())) {
                    reasonText.append("\n");
                }
                reasonText.append("[Information : lot reserved : Following reported carriers are 'EI', but have control job. Its location was not changed.] =>").append("\n");
                reservedCassetteIDs.forEach(x -> {
                    reasonText.append(x.getValue());
                    if (!x.getValue().equals(reservedCassetteIDs.get(reservedCassetteIDs.size() - 1).getValue())) {
                        reasonText.append(",");
                    }
                });
            }

            if (sorterCassetteIDs.size() > 0) {
                if (CimStringUtils.isNotEmpty(reasonText.toString())) {
                    reasonText.append("\n");
                }
                reasonText.append("[Informat|ion : On Sorter eqp : Following reported carriers are 'EI' on wafer Sorter eqp . Its location was not changed.] =>").append("\n");
                sorterCassetteIDs.forEach(x -> {
                    reasonText.append(x.getValue());
                    if (!x.getValue().equals(sorterCassetteIDs.get(sorterCassetteIDs.size() - 1).getValue())) {
                        reasonText.append(",");
                    }
                });
            }

            if (notOnEqpCastIDs.size() > 0) {
                if (CimStringUtils.isNotEmpty(reasonText.toString())) {
                    reasonText.append("\n");
                }
                reasonText.append("[Information : On Sorter eqp : Following reported carriers are 'EI', but can not be found on the eqp port or shelf. Its location was not changed.] =>").append("\n");
                notOnEqpCastIDs.forEach(x -> {
                    reasonText.append(x.getValue());
                    if (!x.getValue().equals(notOnEqpCastIDs.get(notOnEqpCastIDs.size() - 1).getValue())) {
                        reasonText.append(",");
                    }
                });
            }

            if (otherErrorCassetteIDs.size() > 0) {
                if (CimStringUtils.isNotEmpty(reasonText.toString())) {
                    reasonText.append("\n");
                }
                reasonText.append("[Information : Other error detected : Following reported carrier location can not be changed by some reason. Please check its data and recover manually.] =>").append("\n");
                otherErrorCassetteIDs.forEach(x -> {
                    reasonText.append(x.getValue());
                    if (!x.getValue().equals(otherErrorCassetteIDs.get(otherErrorCassetteIDs.size() - 1).getValue())) {
                        reasonText.append(",");
                    }
                });
            }

            throw new ServiceException(retCodeConfig.getSomeCassetteInventoryDataError(), reasonText.toString());
        }
        return inventoriedLotInfos;
    }

    public static void collectCassetteIDsOfParams(List<Infos.WaferTransfer> waferXferList, List<ObjectIdentifier> cassetteIDList) {
        if (!CimArrayUtils.isEmpty(waferXferList)) {
            waferXferList.forEach(waferTransfer -> {
                boolean destCassetteAdded = false;
                boolean origCassetteAdded = false;
                for (ObjectIdentifier cassetteID : cassetteIDList) {
                    if (CimStringUtils.equals(waferTransfer.getDestinationCassetteID().getValue(), cassetteID.getValue())) {
                        destCassetteAdded = true;
                    }
                    if (CimStringUtils.equals(waferTransfer.getOriginalCassetteID().getValue(), cassetteID.getValue())) {
                        origCassetteAdded = true;
                    }
                    if (destCassetteAdded && origCassetteAdded) {
                        break;
                    }
                }
                if (!destCassetteAdded) {
                    cassetteIDList.add(waferTransfer.getDestinationCassetteID());
                }
                if (!origCassetteAdded) {
                    cassetteIDList.add(waferTransfer.getOriginalCassetteID());
                }
            });
        }
    }

    @Override
    public Outputs.ObjCassetteStatusOut cassetteGetStatusDR(Infos.ObjCommon objCommon, ObjectIdentifier cassetteID) {
        log.info("【Method Entry】cassetteGetStatusDR()");
        com.fa.cim.newcore.bo.durable.CimCassette cimCassette = baseCoreFactory.getBO(com.fa.cim.newcore.bo.durable.CimCassette.class, cassetteID);
        Validations.check(null == cimCassette, new OmCode(retCodeConfig.getNotFoundCassette(), cassetteID.getValue()));
        Outputs.ObjCassetteStatusOut objCassetteStatusOut = new Outputs.ObjCassetteStatusOut();
        objCassetteStatusOut.setCastUsedCapacity(cimCassette.getCapacity() == null ? 0 : cimCassette.getCapacity().longValue());
        objCassetteStatusOut.setCastCategory(cimCassette.getCassetteCategory());
        objCassetteStatusOut.setDurableState(cimCassette.getDurableState());
        log.info("【Method Exit】cassetteGetStatusDR()");
        return objCassetteStatusOut;
    }

    @Override
    public String cassetteMultiLotTypeGet(Infos.ObjCommon objCommon, ObjectIdentifier cassetteID) {
        log.info("【Method Entry】cassetteMultiLotTypeGet()");
        CimCassette cassette = baseCoreFactory.getBO(CimCassette.class, cassetteID);
        Validations.check(null == cassette, retCodeConfig.getNotFoundCassette());

        log.info("【Method Exit】cassetteMultiLotTypeGet()");
        return cassette.getMultiLotType();
    }

    public Outputs.CassetteReservationInfoGetDROut cassetteReservationInfoGetDR(Infos.ObjCommon objCommonIn, ObjectIdentifier cassetteID) {
        CimCassette cassette = baseCoreFactory.getBO(CimCassette.class, cassetteID);
        Validations.check(null == cassette, retCodeConfig.getNotFoundCassette());

        Outputs.CassetteReservationInfoGetDROut cassetteReservationInfoGetDROut = new Outputs.CassetteReservationInfoGetDROut();
        cassetteReservationInfoGetDROut.setReserveUserID(new ObjectIdentifier(cassette.getReservePersonID()));
        cassetteReservationInfoGetDROut.setControlJobID(cassette.getControlJobID());
        cassetteReservationInfoGetDROut.setNPWLoadPurposeType(cassette.getNPWLoadPurposeType());
        return cassetteReservationInfoGetDROut;
    }

    @Override
    public Outputs.CassetteLotReserveOut cassetteLotReserve(Infos.ObjCommon objCommonIn, List<Infos.ReserveLot> reserveLot, String claimMemo) {
        CimPerson person = baseCoreFactory.getBO(CimPerson.class, objCommonIn.getUser().getUserID());
        Validations.check(CimObjectUtils.isEmpty(person), retCodeConfig.getNotFoundPerson());
        for (Infos.ReserveLot lot : reserveLot) {
            com.fa.cim.newcore.bo.durable.CimCassette cassette = baseCoreFactory.getBO(com.fa.cim.newcore.bo.durable.CimCassette.class, lot.getCassetteID());
            Validations.check(CimObjectUtils.isEmpty(cassette), retCodeConfig.getNotFoundCassette());
            Validations.check(cassette.isReserved(), retCodeConfig.getAlreadyReservedCst());
            Validations.check(CimBooleanUtils.isFalse(cassette.reserveFor(person)), retCodeConfig.getAlreadyReservedCst());

        }
        Outputs.CassetteLotReserveOut cassetteLotReserveOut = new Outputs.CassetteLotReserveOut();
        cassetteLotReserveOut.setClaimMemo(claimMemo);
        cassetteLotReserveOut.setReserveLot(reserveLot);
        return cassetteLotReserveOut;
    }

    @Override
    public Outputs.ObjCassetteLotReserveCancelOut cassetteLotReserveCancel(Infos.ObjCommon objCommon, List<Infos.ReserveCancelLot> reserveCancelLots, String claimMemo) {
        Outputs.ObjCassetteLotReserveCancelOut objCassetteLotReserveCancelOut = new Outputs.ObjCassetteLotReserveCancelOut();
        CimPerson aPerson = baseCoreFactory.getBO(CimPerson.class, objCommon.getUser().getUserID());
        Validations.check(aPerson == null, new OmCode(retCodeConfig.getNotFoundPerson(), objCommon.getUser().getUserID().getValue()));
        boolean cassetteTransferReserved = false;
        com.fa.cim.newcore.bo.durable.CimCassette aCassette = null;
        for (int i = 0; i < reserveCancelLots.size(); i++) {
            Infos.ReserveCancelLot reserveCancelLot = reserveCancelLots.get(i);
            aCassette = baseCoreFactory.getBO(com.fa.cim.newcore.bo.durable.CimCassette.class, reserveCancelLot.getCassetteID());
            Validations.check(aCassette == null, new OmCode(retCodeConfig.getNotFoundCassette(), reserveCancelLot.getCassetteID().getValue()));
            cassetteTransferReserved = aCassette.isReserved();
            Validations.check(!cassetteTransferReserved, new OmCode(retCodeConfig.getNotReservedCassette(), reserveCancelLot.getCassetteID().getValue()));
            aCassette.unReserve();
        }
        objCassetteLotReserveCancelOut.setClaimMemo(claimMemo);
        objCassetteLotReserveCancelOut.setStrReserveCancelLot(reserveCancelLots);
        return objCassetteLotReserveCancelOut;
    }


    @Override
    public Outputs.CassetteDBInfoGetDROut cassetteDBInfoGetDR(Infos.ObjCommon objCommonIn, Infos.CassetteDBINfoGetDRInfo cassetteDBINfoGetDRInfo) {
        Outputs.CassetteDBInfoGetDROut cassetteDBInfoGetDROut = new Outputs.CassetteDBInfoGetDROut();

        ObjectIdentifier cassetteID = cassetteDBINfoGetDRInfo.getCassetteID();
        cassetteDBINfoGetDRInfo.setCassetteID(cassetteID);
        CimCassetteDO cimCassetteExample = new CimCassetteDO();
        cimCassetteExample.setCassetteID(ObjectIdentifier.fetchValue(cassetteID));
        CimCassetteDO cassette = cimJpaRepository.findOne(Example.of(cimCassetteExample)).orElse(null);
        Validations.check(null == cassette, new OmCode(retCodeConfig.getNotFoundCassette(), cassetteID.getValue()));

        Infos.CassetteBrInfo cassetteBrInfo = new Infos.CassetteBrInfo();
        Infos.CassetteStatusInfo cassetteStatusInfo = new Infos.CassetteStatusInfo();
        Infos.CassettePmInfo cassettePmInfo = new Infos.CassettePmInfo();
        Infos.DurableLocationInfo cassetteLocationInfo = new Infos.DurableLocationInfo();

        cassetteBrInfo.setDescription(cassette.getDescription());
        cassetteBrInfo.setCassetteCategory(cassette.getCassetteCategory());
        cassetteBrInfo.setUsageCheckFlag(CimBooleanUtils.isTrue(cassette.getUsageCheckReq()));
        Long cassetteCapacity = (cassette.getCassetteCapacity() == null) ? 0L : cassette.getCassetteCapacity().longValue();
        cassetteBrInfo.setCapacity(cassetteCapacity);
        Long waferSize = (cassette.getWaferSize() == null) ? 0L : cassette.getWaferSize().longValue();
        cassetteBrInfo.setNominalSize(waferSize);
        cassetteBrInfo.setContents(cassette.getMaterialContents());
        cassetteBrInfo.setProductUsage(cassette.getProductUsage());
        cassetteBrInfo.setCarrierType(cassette.getCarrierType());
        cassetteBrInfo.setRelationFoupFlag(cassette.getRelationFoupFlag()==null?false:cassette.getRelationFoupFlag());

        cassetteStatusInfo.setCassetteStatus(cassette.getDurableState());
        cassetteStatusInfo.setTransferStatus(cassette.getTransferState());
        cassetteStatusInfo.setTransferReserveUserID(ObjectIdentifier.build(cassette.getReserveUserID(), cassette.getReserveUserObj()));
        cassetteStatusInfo.setTransferReserveUserReference(cassette.getReserveUserObj());

        // Add Clear Job Status for DMS clean.
        cassetteStatusInfo.setJobStatus(cassette.getJobStatusID());

        if (CimStringUtils.equals(cassette.getTransferState(), BizConstant.SP_TRANSSTATE_EQUIPMENTIN)
                || CimStringUtils.equals(cassette.getTransferState(), BizConstant.SP_TRANSSTATE_EQUIPMENTOUT)) {
            cassetteStatusInfo.setEquipmentID(ObjectIdentifier.build(cassette.getEquipmentID(), cassette.getEquipmentObj()));
        } else {
            cassetteStatusInfo.setStockerID(ObjectIdentifier.build(cassette.getEquipmentID(), cassette.getEquipmentObj()));
            cassetteStatusInfo.setShelfPosition(new Infos.ShelfPosition(cassette.getShelfPositionX(), cassette.getShelfPositionY(), cassette.getShelfPositionZ()));
        }

        cassetteStatusInfo.setEmptyFlag(cassette.getCastUsedCapacity() == 0);

        cassetteStatusInfo.setSlmRsvEquipmentID(ObjectIdentifier.build(cassette.getSlmReservedEquipmentID(), cassette.getSlmReservedEquipmentObj()));
        cassetteStatusInfo.setInterFabXferState(cassette.getInterFabTransferState());

        Outputs.CassetteZoneTypeGetOut cassetteZoneTypeGetOut = this.cassetteZoneTypeGet(objCommonIn, cassetteID);
        if (null != cassetteZoneTypeGetOut) {
            cassetteStatusInfo.setZoneType(cassetteZoneTypeGetOut.getZoneType());
            cassetteStatusInfo.setPriority(cassetteZoneTypeGetOut.getPriority());
        }
        cassetteStatusInfo.setMultiLotType(cassette.getMultiLotType());
        cassetteStatusInfo.setLastClaimedPerson(ObjectIdentifier.build(cassette.getClaimUserID(), cassette.getClaimUserObj()));
        cassetteStatusInfo.setLastClaimedTimeStamp(String.valueOf(cassette.getClaimTime()));
        cassetteStatusInfo.setControlJobID(new ObjectIdentifier(cassette.getControlJobID(), cassette.getControlJobObject()));
        cassetteStatusInfo.setDurableControlJobID(new ObjectIdentifier(cassette.getDurableControlJobID(), cassette.getDurableControlJobObj()));
        cassetteStatusInfo.setDurableSTBFlag(!CimStringUtils.isEmpty(cassette.getDurableProcessFlowContextObj()));
        //--- Set pptHashedInfoSequence strDurableStatusList; ----------------------//
        List<Infos.HashedInfo> durableStatusList = new ArrayList<>();
        String durableFlowState = "";
        if (!CimStringUtils.isEmpty(cassette.getRouteID())) {
            if (CimStringUtils.equals(cassette.getDurableState(), CIMStateConst.CIM_DURABLE_SCRAPPED)) {
                durableFlowState = CIMStateConst.CIM_DURABLE_SCRAPPED;
            } else if (CimStringUtils.equals(cassette.getDurableHoldState(), BizConstant.SP_DURABLE_HOLDSTATE_ONHOLD)) {
                durableFlowState = BizConstant.SP_DURABLE_HOLDSTATE_ONHOLD;
            } else if (CimStringUtils.equals(cassette.getDurableFinishedState(),
                    BizConstant.SP_DURABLE_FINISHEDSTATE_COMPLETED)) {
                durableFlowState = BizConstant.SP_DURABLE_FINISHEDSTATE_COMPLETED;
            } else {
                durableFlowState = cassette.getDurableProcessState();
            }
        }
        durableStatusList.add(new Infos.HashedInfo("Durable Flow State", durableFlowState));

        String durableState = "";
        if (!CimStringUtils.isEmpty(cassette.getRouteID())) {
            if (CimStringUtils.equals(cassette.getDurableState(), CIMStateConst.CIM_DURABLE_SCRAPPED)
                    || CimStringUtils.equals(cassette.getDurableFinishedState(), BizConstant.SP_DURABLE_FINISHEDSTATE_COMPLETED)) {
                durableState = BizConstant.SP_DURABLE_ONROUTESTATE_FINISHED;
            } else {
                durableState = BizConstant.SP_DURABLE_ONROUTESTATE_ACTIVE;
            }
        }
        durableStatusList.add(new Infos.HashedInfo("Durable State", durableState));
        durableStatusList.add(new Infos.HashedInfo("Durable Production State", cassette.getDurableProductionState()));
        durableStatusList.add(new Infos.HashedInfo("Durable Hold State", cassette.getDurableHoldState()));
        durableStatusList.add(new Infos.HashedInfo("Durable Finished State", cassette.getDurableFinishedState()));
        durableStatusList.add(new Infos.HashedInfo("Durable Process State", cassette.getDurableProcessState()));
        durableStatusList.add(new Infos.HashedInfo("Durable Inventory", cassette.getDurableInventoryState()));
        cassetteStatusInfo.setStrDurableStatusList(durableStatusList);
        cassetteStatusInfo.setDurableSubStatus(new ObjectIdentifier(cassette.getDurableSubStateID(), cassette.getDurableSubStateObj()));
        String querySql;
        String cassetteSubStateID = cassette.getDurableSubStateID();
        if (!CimStringUtils.isEmpty(cassetteSubStateID)) {
            CimDurableSubStateDO cimDurableSubStateExam = new CimDurableSubStateDO();
            cimDurableSubStateExam.setDurableSubStateID(cassetteSubStateID);
            cimJpaRepository.findOne(Example.of(cimDurableSubStateExam)).ifPresent(durableSubState -> {
                cassetteStatusInfo.setDurableSubStatus(ObjectIdentifier.build(durableSubState.getDurableSubStateID(),
                        durableSubState.getId()));
                cassetteStatusInfo.setDurableSubStatusDescription(durableSubState.getDescription());
                cassetteStatusInfo.setAvailableForDurableFlag(durableSubState.getProcessAvailableFlag());
            });
        }
        //--- Set string processLagTime; ------------------------------------//
        Timestamp timestamp = cassette.getProcessLagTime();
        cassetteStatusInfo.setProcessLagTime(null != timestamp ? timestamp.toString() : "");

        //--------------------------------------------------------------------------------------------//
        // set DurableOperationInfo & DurableWipOperationInfo                                         //
        //--------------------------------------------------------------------------------------------//
        Infos.DurableOperationInfo durableOperationInfo = new Infos.DurableOperationInfo();
        Infos.DurableWipOperationInfo durableWipOperationInfo = new Infos.DurableWipOperationInfo();
        //--- Set string dueTimeStamp; --------------------------------------------//
        durableOperationInfo.setDueTimeStamp("");
        //--- Set objectIdentifier bankID; ----------------------------------------//
        durableOperationInfo.setBankID(new ObjectIdentifier(cassette.getBankID(), cassette.getBankObj()));

        ObjectIdentifier activePDID = null;
        String processFlowMainPdID = null;
        Boolean processState = null;
        String processFlowObj = null;
        Integer processFlowPosListIDSeqNo = null;
        String processFlowPosListObj = null;
        String activeProcessDefinitionDepartment = null;
        Double activeProcessDefinitionStdProcTime = null;
        String processDefinitionStageObj = null;
        String processDefinitionStageID = null;
        Integer processDefinitionDSeqNo = null;
        String mainProcessID = null;
        Boolean mandatoryFlag = null;
        String processFlowType = null;
        Long cassetteReworkCount = null;
        CimDurableProcessOperationDO durableProcessOperation = null;
        if (CimBooleanUtils.isTrue(cassetteDBINfoGetDRInfo.getDurableOperationInfoFlag())) {
            querySql = String.format("SELECT ID,\n" +
                    "               ROUTE_PRSS_RKEY,\n" +
                    "               ROUTE_NO,\n" +
                    "               ROUTE_OPE_NO,\n" +
                    "               ROUTE_PRF_RKEY,\n" +
                    "               MROUTE_PRF_RKEY,\n" +
                    "               MAIN_PROCESS_ID,\n" +
                    "               MAIN_PROCESS_RKEY,\n" +
                    "               STEP_ID,\n" +
                    "               STEP_RKEY,\n" +
                    "               STEP_NAME,\n" +
                    "               OPE_NO,\n" +
                    "               ACTUAL_MOVIN_TIME,\n" +
                    "               ACTUAL_MOVOUT_TIME,\n" +
                    "               ALLOC_EQP_ID,\n" +
                    "               ALLOC_EQP_RKEY,\n" +
                    "               MPROCESS_PRF_RKEY,\n" +
                    "               REMAIN_CYCLE_TIME,\n" +
                    "               ALLOC_LRCP_ID,\n" +
                    "               ALLOC_LRCP_RKEY\n" +
                    "               FROM OMDRBLPROPE\n" +
                    "               WHERE ID = '%s'", cassette.getDurableProcessOperation());
            durableProcessOperation = cimJpaRepository.queryOne(querySql, CimDurableProcessOperationDO.class);
            String hFRDRBLPOMODPOS_OBJ = null;
            if (null != durableProcessOperation) {
                hFRDRBLPOMODPOS_OBJ = durableProcessOperation.getModuleProcessOperationSpecificationsObj();
                activePDID = new ObjectIdentifier(durableProcessOperation.getProcessDefinitionID(), durableProcessOperation.getProcessDefinitionObj());
                //------------------------------------------//
                //  Get systemkey of current module PF      //
                //------------------------------------------//
                String modulePdID = durableProcessOperation.getModuleProcessFlowObj();

                //------------------------------------------------------------//
                //  Get seqno of current module POS in current module PF      //
                //------------------------------------------------------------//
                querySql = String.format("SELECT IDX_NO\n" +
                        "               FROM OMPRF_PRSSSEQ\n" +
                        "                WHERE REFKEY = '%s'\n" +
                        "                AND LINK_KEY  = '%s'", modulePdID, durableProcessOperation.getModuleOperationNumber());
                CimPFPosListDO cimPFPosListDO = cimJpaRepository.queryOne(querySql, CimPFPosListDO.class);
                int dSeqNo = cimPFPosListDO.getSequenceNumber();

                //----------------------------------------//
                //  Check state of current module PF      //
                //----------------------------------------//
                querySql = String.format("SELECT PRP_ID,\n" +
                        "               ACTIVE_FLAG\n" +
                        "               FROM OMPRF\n" +
                        "               WHERE ID = '%s'", modulePdID);
                CimProcessFlowDO processFlowByModulePdID = cimJpaRepository.queryOne(querySql, CimProcessFlowDO.class);
                processFlowMainPdID = processFlowByModulePdID.getMainProcessDefinitionID();
                processState = processFlowByModulePdID.getState();

                //----------------------------------------//
                //  Get info from active module PF        //
                //----------------------------------------//
                querySql = String.format(" SELECT OMPRF.ID,\n" +
                                "         OMPRF_PRSSSEQ.IDX_NO,\n" +
                                "         OMPRF_PRSSSEQ.PRSS_RKEY\n" +
                                "         FROM OMPRF, OMPRF_PRSSSEQ\n" +
                                "         WHERE OMPRF.PRP_ID      = '%s'\n" +
                                "         AND OMPRF.ACTIVE_FLAG          = 1\n" +
                                "         AND OMPRF.ID = OMPRF_PRSSSEQ.REFKEY\n" +
                                "         AND OMPRF_PRSSSEQ.LINK_KEY  = '%s'\n" +
                                "         AND OMPRF.PRP_LEVEL       = '%s'",
                        processFlowMainPdID,
                        durableProcessOperation.getModuleOperationNumber(),
                        BizConstant.SP_PD_FLOWLEVEL_MODULE);
                Object[] objects = cimJpaRepository.queryOne(querySql);
                if (null != objects) {
                    // if current module openo is found in an active module PF
                    log.info("#### Current module openo is found in an active module PF.");
                    hFRDRBLPOMODPOS_OBJ = String.valueOf(objects[2]);
                }
                String processDefinitionObj = activePDID.getReferenceKey();
                //---------------------------------------//
                //  Get information from active PD       //
                //---------------------------------------//
                querySql = String.format("SELECT DEPT,\n" +
                        "                                STD_PROCESS_TIME\n" +
                        "                           FROM OMPRP\n" +
                        "                          WHERE ID = '%s'", processDefinitionObj);
                CimProcessDefinitionDO activeProcessDefinition = cimJpaRepository.queryOne(querySql, CimProcessDefinitionDO.class);
                activeProcessDefinitionDepartment = activeProcessDefinition.getDepartment();
                activeProcessDefinitionStdProcTime = activeProcessDefinition.getStandardProcessTIme();

                String mainProcessFlowObj = durableProcessOperation.getMainProcessFlowObj();
                //---------------------------------------------------------------//
                //  Get seqno & stage of current module PD in current main PF    //
                //---------------------------------------------------------------//
                querySql = String.format("SELECT OMPRF.PRP_ID,\n" +
                        "                                OMPRF_ROUTESEQ.IDX_NO,\n" +
                        "                                OMPRF_ROUTESEQ.STAGE_ID,\n" +
                        "                                OMPRF_ROUTESEQ.STAGE_RKEY\n" +
                        "                           FROM OMPRF, OMPRF_ROUTESEQ\n" +
                        "                          WHERE OMPRF.ID        = '%s'\n" +
                        "                            AND OMPRF_ROUTESEQ.REFKEY = OMPRF.ID\n" +
                        "                            AND OMPRF_ROUTESEQ.LINK_KEY  = '%s'", mainProcessFlowObj, durableProcessOperation.getModuleNumber());
                Object[] objects2 = cimJpaRepository.queryOne(querySql);
                mainProcessID = (String) objects2[0];
                processDefinitionDSeqNo = CimNumberUtils.intValue((Number) objects2[1]);
                processDefinitionStageID = (String) objects2[2];
                processDefinitionStageObj = (String) objects2[3];

            }
            //--- Set objectIdentifier routeID; ----------------------------------------//
            durableOperationInfo.setRouteID(new ObjectIdentifier(cassette.getRouteID(), cassette.getRouteObj()));
            //--- Set objectIdentifier startBankID; ------------------------------------//
            String processDefinitionLevel = BizConstant.SP_PD_FLOWLEVEL_MAIN;
            List<CimProcessDefinitionDO> processDefinitions = cimJpaRepository.query("SELECT * FROM OMPRP WHERE PRP_ID = ?1 AND PRP_LEVEL = ?2", CimProcessDefinitionDO.class, cassette.getRouteID(), processDefinitionLevel);
            if (!CimObjectUtils.isEmpty(processDefinitions)) {
                durableOperationInfo.setStartBankID(new ObjectIdentifier(processDefinitions.get(0).getStartBankID(), processDefinitions.get(0).getStartBankObj()));
            }

            //--- Set string operationNumber; ------------------------------------//
            durableOperationInfo.setOperationNumber(cassette.getOperationNumber());
            if (!CimStringUtils.equals(cassette.getDurableInventoryState(), BizConstant.SP_DURABLE_INVENTORYSTATE_ONFLOOR)) {
                List<Infos.LotEquipmentList> lotEquipmentLists = this.emptyLotEquipmentList();
                durableOperationInfo.setStrEquipmentList(lotEquipmentLists);
            } else if (CimStringUtils.equals(cassette.getDurableProcessState(), BizConstant.SP_DURABLE_PROCSTATE_PROCESSING)) {
                if (null != durableProcessOperation) {
                    List<Infos.LotEquipmentList> lotEquipmentLists = new ArrayList<>(1);
                    Infos.LotEquipmentList lotEquipmentList = new Infos.LotEquipmentList();
                    lotEquipmentLists.add(lotEquipmentList);
                    lotEquipmentList.setEquipmentID(new ObjectIdentifier(
                            durableProcessOperation.getAssignEquipmentID(),
                            durableProcessOperation.getAssignEquipmentObj()
                    ));
                    querySql = String.format("SELECT DESCRIPTION\n" +
                            "                               FROM OMEQP\n" +
                            "                              WHERE ID = '%s'", durableProcessOperation.getAssignEquipmentObj());
                    CimEquipmentDO cimEquipmentDO = cimJpaRepository.queryOne(querySql, CimEquipmentDO.class);
                    lotEquipmentList.setEquipmentName(cimEquipmentDO.getDescription());
                    durableOperationInfo.setStrEquipmentList(lotEquipmentLists);
                }
            } else if (CimStringUtils.equals(cassette.getDurableHoldState(), BizConstant.SP_DURABLE_HOLDSTATE_ONHOLD)) {
                querySql = String.format(" SELECT OMCARRIER_HOLDEQP.EQP_ID,\n" +
                        "                        OMCARRIER_HOLDEQP.EQP_RKEY,\n" +
                        "                        OMEQP.DESCRIPTION\n" +
                        "                   FROM OMCARRIER_HOLDEQP, OMEQP\n" +
                        "                  WHERE OMCARRIER_HOLDEQP.REFKEY = '%s'\n" +
                        "                    AND OMCARRIER_HOLDEQP.EQP_ID = OMEQP.EQP_ID", cassette.getId());
                List<Object[]> queryResult = cimJpaRepository.query(querySql);
                int count = CimArrayUtils.getSize(queryResult);
                List<Infos.LotEquipmentList> holdLotEquipmentLists = this.fromEquipmentListToLotEquipmentList(queryResult);
                durableOperationInfo.setStrEquipmentList(holdLotEquipmentLists);
                if (count == 0) {
                    querySql = String.format(" SELECT OMCARRIER_EQP.EQP_ID,\n" +
                            "                                OMCARRIER_EQP.EQP_RKEY,\n" +
                            "                                OMEQP.DESCRIPTION\n" +
                            "                           FROM OMCARRIER_EQP, OMEQP\n" +
                            "                          WHERE OMCARRIER_EQP.REFKEY = '%S'\n" +
                            "                            AND OMCARRIER_EQP.EQP_ID = OMEQP.EQP_ID ", cassette.getId());
                    List<Object[]> queryResult2 = cimJpaRepository.query(querySql);
                    List<Infos.LotEquipmentList> lotEquipmentLists = this.fromEquipmentListToLotEquipmentList(queryResult2);
                    durableOperationInfo.setStrEquipmentList(lotEquipmentLists);
                }
            } else if (CimStringUtils.equals(cassette.getDurableProcessState(), BizConstant.SP_DURABLE_PROCSTATE_WAITING)) {
                querySql = String.format(" SELECT OMCARRIER_EQP.EQP_ID,\n" +
                        "                            OMCARRIER_EQP.EQP_RKEY,\n" +
                        "                            OMEQP.DESCRIPTION\n" +
                        "                       FROM OMCARRIER_EQP, OMEQP\n" +
                        "                      WHERE OMCARRIER_EQP.REFKEY = '%s'\n" +
                        "                        AND OMCARRIER_EQP.EQP_ID = OMEQP.EQP_ID ", cassette.getId());
                List<Object[]> queryResult = cimJpaRepository.query(querySql);
                List<Infos.LotEquipmentList> holdLotEquipmentLists = this.fromEquipmentListToLotEquipmentList(queryResult);
                durableOperationInfo.setStrEquipmentList(holdLotEquipmentLists);
            } else {
                List<Infos.LotEquipmentList> lotEquipmentLists = this.emptyLotEquipmentList();
                durableOperationInfo.setStrEquipmentList(lotEquipmentLists);
            }
            //--- Set string operationNumber; ------------------------------------//
            durableOperationInfo.setQueuedTimeStamp(cassette.getQueuedTime());
            if (durableProcessOperation != null) {
                //--- Get process information from main PF
                querySql = String.format("SELECT PRSS_RKEY\n" +
                                "                           FROM OMPRF_PRSSSEQ\n" +
                                "                          WHERE REFKEY = '%s'\n" +
                                "                            AND LINK_KEY          = '%s'",
                        durableProcessOperation.getProcessFlowObj(), durableProcessOperation.getOperationNumber());
                CimPFPosListDO cimPFPosListDO = cimJpaRepository.queryOne(querySql, CimPFPosListDO.class);
                if (null != cimPFPosListDO) {
                    String posObj = cimPFPosListDO.getProcessOperationSpecificationsObj();
                }

                //--- Get process information from module POS
                querySql = String.format("SELECT ID, COMPULSORY_FLAG\n" +
                        "                           FROM OMPRSS\n" +
                        "                          WHERE OMPRSS.ID = '%s'", hFRDRBLPOMODPOS_OBJ);
                mandatoryFlag = Boolean.valueOf(CimObjectUtils.toString(cimJpaRepository.queryOne(querySql)[1]));

                //--- Set objectIdentifier operationID;-----------------------//
                durableOperationInfo.setOperationID(activePDID);
                //--- Set string operationName;-------------------------------//
                durableOperationInfo.setOperationName(durableProcessOperation.getOperationName());
                //--- Set string department;----------------------------------//
                durableOperationInfo.setDepartment(activeProcessDefinitionDepartment);
                //--- Set string standardProcessTime;-------------------------//
                durableOperationInfo.setStandardProcessTime(activeProcessDefinitionStdProcTime);
                //--- Set boolean mandatoryOperationFlag;---------------------//
                durableOperationInfo.setMandatoryOperationFlag(mandatoryFlag);
                //--- Set objectIdentifier stageID;---------------------------//
                durableOperationInfo.setStageID(new ObjectIdentifier(processDefinitionStageID, processDefinitionStageObj));
                //--- Set long reworkCount;-----------------------------------//
                durableOperationInfo.setReworkCount(0L);

                String key = "";
                String cassetteReworkCountKey = "";

                ObjectIdentifier dummyID = new ObjectIdentifier();
                Params.ProcessOperationListForDurableDRParams processOperationListForDurableDRParams = new Params.ProcessOperationListForDurableDRParams();
                processOperationListForDurableDRParams.setDurableCategory(BizConstant.SP_DURABLE_CATEGORY_CASSETTE);
                processOperationListForDurableDRParams.setDurableID(cassetteID);
                processOperationListForDurableDRParams.setSearchDirection(false);
                processOperationListForDurableDRParams.setPosSearchFlag(true);
                processOperationListForDurableDRParams.setCurrentFlag(false);
                processOperationListForDurableDRParams.setSearchCount(1);
                processOperationListForDurableDRParams.setSearchRouteID(dummyID);
                processOperationListForDurableDRParams.setSearchOperationNumber("");

                // process_OperationListForDurableDR
                try {
                    List<Infos.DurableOperationNameAttributes> durableOperationNameAttributesList = processMethod.processOperationListForDurableDR(
                            objCommonIn, processOperationListForDurableDRParams);
                    if (!CimArrayUtils.isEmpty(durableOperationNameAttributesList)) {
                        boolean firstOperationFlag = false;
                        Infos.DurableOperationNameAttributes firstDurableOperationNameAttributes = durableOperationNameAttributesList.get(0);
                        if (!CimStringUtils.equals(mainProcessID,
                                firstDurableOperationNameAttributes.getRouteID().getValue())) {
                            querySql = String.format("SELECT PRF_TYPE\n" +
                                            "                                       FROM OMPRP\n" +
                                            "                                      WHERE PRP_ID    = '%s'\n" +
                                            "                                        AND PRP_LEVEL = '%s'",
                                    mainProcessID, BizConstant.SP_PD_FLOWLEVEL_MAIN);
                            CimProcessDefinitionDO processDefinition = cimJpaRepository.queryOne(querySql, CimProcessDefinitionDO.class);
                            processFlowType = processDefinition.getFlowType();
                            if (CimStringUtils.isNotEmpty(processFlowType)) {
                                firstOperationFlag = true;
                            }
                        }

                        if (CimStringUtils.isNotEmpty(firstDurableOperationNameAttributes.getRouteID().getValue())
                                && CimStringUtils.isNotEmpty(firstDurableOperationNameAttributes.getOperationNumber())
                                && !firstOperationFlag) {
                            key = firstDurableOperationNameAttributes.getRouteID().getValue()
                                    + BizConstant.SP_POSPROCESSFLOWCONTEXT_SEPARATOR_CHAR
                                    + firstDurableOperationNameAttributes.getOperationNumber();
                            cassetteReworkCountKey = key;
                        }
                    }
                } catch (ServiceException e) {
                    if (Validations.isEquals(retCodeConfig.getSomeopelistDataError(), e.getCode())) {
                        log.debug("process_OperationListForDurableDR() == RC_SOMEOPELIST_DATA_ERROR");
                    } else if (Validations.isEquals(retCodeConfig.getNotFoundDurablePo(), e.getCode())) {
                        log.debug("process_OperationListForDurableDR() == RC_NOT_FOUND_DURABLEPO : This means This durable doesn't on route.");
                    } else if (Validations.isEquals(retCodeConfig.getNotFoundPfForDurable(), e.getCode())) {
                        log.debug("process_OperationListForDurableDR() == RC_NOT_FOUND_PF_FOR_DURABLE : This means This durable doesn't on route.");
                    } else {
                        throw e;
                    }
                }
                if (CimStringUtils.isNotEmpty(cassetteReworkCountKey)) {
                    CimCassetteReworkCountDO cimCassetteReworkCountExample = new CimCassetteReworkCountDO();
                    cimCassetteReworkCountExample.setLinkKey(cassetteReworkCountKey);
                    cimCassetteReworkCountExample.setReferenceKey(cassette.getId());
                    CimCassetteReworkCountDO cimCassetteReworkCountDO = cimJpaRepository.findOne(Example.of(cimCassetteReworkCountExample)).orElse(null);
                    if (null != cimCassetteReworkCountDO) {
                        cassetteReworkCount = CimNumberUtils.longValue(cimCassetteReworkCountDO.getReworkCount());
                        durableOperationInfo.setReworkCount(cassetteReworkCount);
                    }
                } else {
                    log.debug("#### Rework Count = 0");
                }

                if (CimStringUtils.isNotEmpty(cassette.getControlJobID())) {
                    durableOperationInfo.setLogicalRecipeID(new ObjectIdentifier(
                            durableProcessOperation.getAssignLogicalRecipeID(),
                            durableProcessOperation.getAssignEquipmentObj()));
                } else {
                    try {
                        ObjectIdentifier logicalRecipeID = processMethod.processDefaultLogicalRecipeGetDR(objCommonIn, activePDID);
                        durableOperationInfo.setLogicalRecipeID(logicalRecipeID);
                    } catch (ServiceException e) {
                        if (!Validations.isEquals(retCodeConfig.getNotFoundProcessDefinition(), e.getCode())
                                && !Validations.isEquals(retCodeConfig.getNotFoundLogicalRecipe(), e.getCode())) {
                            throw e;
                        }
                    }
                }
            } else {
                durableOperationInfo.setMandatoryOperationFlag(false);
                durableOperationInfo.setReworkCount(0L);
            }
        }
        if (CimBooleanUtils.isTrue(cassetteDBINfoGetDRInfo.getDurableWipOperationInfoFlag())) {
            if (!cassette.getRespOperationFlag()) {
                durableWipOperationInfo.setResponsibleRouteID(durableOperationInfo.getRouteID());
                durableWipOperationInfo.setResponsibleOperationID(durableOperationInfo.getOperationID());
                durableWipOperationInfo.setResponsibleOperationNumber(durableOperationInfo.getOperationNumber());
                durableWipOperationInfo.setResponsibleOperationName(durableOperationInfo.getOperationName());
            } else {
                //-----------------------------------------------------------------------------------//
                // Get Previous Process Operation                                                    //
                //-----------------------------------------------------------------------------------//
                querySql = String.format("SELECT ID,\n" +
                        "                                IDX_NO\n" +
                        "                           FROM OMDRBLPRFCX_PROPESEQ\n" +
                        "                          WHERE PROPE_RKEY = '%s'", durableProcessOperation.getId());
                CimDurablePFXDurablePOListDO cimDurablePFXDurablePOListDO = cimJpaRepository.queryOne(querySql, CimDurablePFXDurablePOListDO.class);
                if (cimDurablePFXDurablePOListDO != null) {
                    Integer sequenceNumber = cimDurablePFXDurablePOListDO.getSequenceNumber();
                    if (sequenceNumber != null) {
                        sequenceNumber--;
                        querySql = String.format("SELECT PROPE_RKEY\n" +
                                "                                   FROM OMDRBLPRFCX_PROPESEQ\n" +
                                "                                  WHERE REFKEY = '%s'\n" +
                                "                                    AND IDX_NO = '%s'", cimDurablePFXDurablePOListDO.getId(), sequenceNumber);
                        CimDurablePFXDurablePOListDO cimDurablePFXDurablePOListDO2 = cimJpaRepository.queryOne(querySql, CimDurablePFXDurablePOListDO.class);
                        if (cimDurablePFXDurablePOListDO2 != null) {
                            querySql = String.format("SELECT MAIN_PROCESS_ID,\n" +
                                    "                                            MAIN_PROCESS_RKEY,\n" +
                                    "                                            STEP_ID,\n" +
                                    "                                            STEP_RKEY,\n" +
                                    "                                            STEP_NAME,\n" +
                                    "                                            OPE_NO\n" +
                                    "                                       FROM OMDRBLPROPE\n" +
                                    "                                     WHERE  ID = '%s'", cimDurablePFXDurablePOListDO2.getProcessOperationObj());
                            CimDurableProcessOperationDO cimDurableProcessOperationDO = cimJpaRepository.queryOne(querySql, CimDurableProcessOperationDO.class);
                            if (cimDurableProcessOperationDO != null) {
                                durableWipOperationInfo.setResponsibleRouteID(new ObjectIdentifier(cimDurableProcessOperationDO.getMainProcessDefinitionID(), cimDurableProcessOperationDO.getMainProcessDefinitionObj()));
                                durableWipOperationInfo.setResponsibleOperationID(new ObjectIdentifier(cimDurableProcessOperationDO.getProcessDefinitionID(), cimDurableProcessOperationDO.getProcessDefinitionObj()));
                                durableWipOperationInfo.setResponsibleOperationNumber(cimDurableProcessOperationDO.getOperationNumber());
                                durableWipOperationInfo.setResponsibleOperationName(cimDurableProcessOperationDO.getOperationName());
                            }
                        }
                    }
                }
            }
        }
        // Calculate runTime ( currentTime - lastMaintenanceTime )
        Timestamp aTimeStamp = objCommonIn.getTimeStamp().getReportTimeStamp();
        Timestamp aTmp = cassette.getMaintainTime();
        Long tmpTime = (aTmp != null) ? aTmp.getTime() : 0L;
        Long timeStampTime = (aTimeStamp != null) ? aTimeStamp.getTime() : 0L;
        Long duration = CimDateUtils.substractTimeStamp(tmpTime, timeStampTime);
        Double runTime = duration.doubleValue() / 1000;
        String runTimeValue = String.valueOf((int) (runTime / 60));
        cassettePmInfo.setRunTime(runTimeValue);

        Double cassetteDurationLimit = (cassette.getDurationLimit() == null || cassette.getDurationLimit() <= 1E-100)
                ? Double.parseDouble("0") : cassette.getDurationLimit();
        if (Double.compare(cassetteDurationLimit, Double.parseDouble("0")) == 0) {
            cassette.setDurationLimit(cassetteDurationLimit);
        } else {
            cassette.setDurationLimit(cassetteDurationLimit / 60 / 1000);// Change from milisec to minutes
        }

        cassettePmInfo.setMaximumRunTime(String.valueOf(cassetteDurationLimit.intValue()));

        Long timesUsed = (cassette.getTimesUsed() != null) ? cassette.getTimesUsed().longValue() : 0L;
        cassettePmInfo.setOperationStartCount(timesUsed);

        Long timesUsedLimit = (cassette.getTimesUsedLimit() != null) ? cassette.getTimesUsedLimit().longValue() : 0L;
        cassettePmInfo.setMaximumOperationStartCount(timesUsedLimit);

        String maintainTime = (cassette.getMaintainTime() != null) ? cassette.getMaintainTime().toString() : BizConstant.EMPTY;
        cassettePmInfo.setLastMaintenanceTimeStamp(maintainTime);

        cassettePmInfo.setLastMaintenancePerson(cassette.getMaintainUserID());

        cassettePmInfo.setIntervalBetweenPM(CimNumberUtils.longValue(cassette.getIntervalBetweenPM()));

        Timestamp aTimeStamp2 = cassette.getMaintainTime();
        Timestamp aTmp2 = objCommonIn.getTimeStamp().getReportTimeStamp();

        long timeStamp2 = (aTimeStamp2 != null) ? aTimeStamp2.getTime() : 0L;
        long tmpTime2 = (aTmp2 != null) ? aTmp2.getTime() : 0L;
        Long aDuration = CimDateUtils.substractTimeStamp(timeStamp2, tmpTime2);

        double remainSeconds = aDuration.doubleValue() / 1000;
        Long minutes = (long) (remainSeconds / 60);

        cassettePmInfo.setPassageTimeFromLastPM(minutes);
        cassetteLocationInfo.setInstanceName(cassette.getInstanceName());
        cassetteLocationInfo.setCurrentLocationFlag(CimBooleanUtils.isTrue(cassette.getCurrentLocationFlag()));
        cassetteLocationInfo.setBackupState(cassette.getBackupState());
        CimCassetteLotDO cimCassetteLotExample = new CimCassetteLotDO();
        cimCassetteLotExample.setReferenceKey(cassette.getId());
        List<CimCassetteLotDO> containedLotList = cimJpaRepository.findAll(Example.of(cimCassetteLotExample));
        List<Infos.ContainedLotInfo> containedLotInfoList = new ArrayList<>(containedLotList.size());
        for (CimCassetteLotDO containedLot : containedLotList) {
            Infos.ContainedLotInfo containedLotInfo = new Infos.ContainedLotInfo();
            containedLotInfo.setLotID(new ObjectIdentifier(containedLot.getLotID(), containedLot.getLotObj()));
            containedLotInfoList.add(containedLotInfo);
            Outputs.ObjLotCurrentOperationInfoGetOut objLotCurrentOperationInfoGetOut = null;
            try {
                objLotCurrentOperationInfoGetOut = lotMethod.lotCurrentOperationInfoGet(objCommonIn, new ObjectIdentifier(containedLot.getLotID(), containedLot.getLotObj()));
            } catch (ServiceException e) {
                if (Validations.isEquals(retCodeConfig.getNotFoundProcessOperation(), e.getCode())) {
                    containedLotInfo.setAutoDispatchDisableFlag(false);
                } else {
                    throw e;
                }
            }
            //--------------------------------------------
            //  Get Auto Dispatch Control Information
            //--------------------------------------------
            Inputs.ObjAutoDispatchControlInfoGetDRIn objAutoDispatchControlInfoGetDRIn = new Inputs.ObjAutoDispatchControlInfoGetDRIn();
            objAutoDispatchControlInfoGetDRIn.setLotID(containedLotInfo.getLotID());
            List<Infos.LotAutoDispatchControlInfo> lotAutoDispatchControlInfoList = autoDispatchControlMethod.autoDispatchControlInfoGetDR(objCommonIn, objAutoDispatchControlInfoGetDRIn);
            containedLotInfo.setAutoDispatchDisableFlag(false);
            if (CimArrayUtils.getSize(lotAutoDispatchControlInfoList) > 0) {
                for (Infos.LotAutoDispatchControlInfo info : lotAutoDispatchControlInfoList) {
                    if (objLotCurrentOperationInfoGetOut == null) {
                        objLotCurrentOperationInfoGetOut = new Outputs.ObjLotCurrentOperationInfoGetOut();
                    }
                    if (ObjectIdentifier.equalsWithValue(info.getRouteID(), objLotCurrentOperationInfoGetOut.getRouteID())
                            && CimStringUtils.equals(info.getOperationNumber(), objLotCurrentOperationInfoGetOut.getOperationNumber())) {
                        containedLotInfo.setAutoDispatchDisableFlag(true);
                        break;
                    } else if (ObjectIdentifier.equalsWithValue(info.getRouteID(), objLotCurrentOperationInfoGetOut.getRouteID())
                            && CimStringUtils.equals(info.getOperationNumber(), "*")) {
                        containedLotInfo.setAutoDispatchDisableFlag(true);
                        break;
                    } else if (CimStringUtils.equals(info.getRouteID().getValue(), "*")
                            && CimStringUtils.equals(info.getOperationNumber(), "*")) {
                        containedLotInfo.setAutoDispatchDisableFlag(true);
                        break;
                    }
                }
            }
        }
        Results.CarrierDetailInfoInqResult carrierDetailInfoInqResult = new Results.CarrierDetailInfoInqResult();
        carrierDetailInfoInqResult.setCassetteID(cassetteDBINfoGetDRInfo.getCassetteID()); //corresponding to source code line 100;
        carrierDetailInfoInqResult.setCassetteBRInfo(cassetteBrInfo);
        carrierDetailInfoInqResult.setCassetteStatusInfo(cassetteStatusInfo);
        carrierDetailInfoInqResult.setCassettePMInfo(cassettePmInfo);
        cassetteStatusInfo.setStrContainedLotInfo(containedLotInfoList);

        carrierDetailInfoInqResult.setCassetteLocationInfo(cassetteLocationInfo);
        carrierDetailInfoInqResult.setStrDurableOperationInfo(durableOperationInfo);
        carrierDetailInfoInqResult.setStrDurableWipOperationInfo(durableWipOperationInfo);
        cassetteDBInfoGetDROut.setCarrierDetailInfoInqResult(carrierDetailInfoInqResult);

        return cassetteDBInfoGetDROut;
    }

    private List<Infos.LotEquipmentList> fromEquipmentListToLotEquipmentList(List<Object[]> objectList) {
        if (CimArrayUtils.isEmpty(objectList)) {
            return new ArrayList<>();
        }
        List<Infos.LotEquipmentList> lotEquipmentLists = new ArrayList<>();
        for (Object[] objects : objectList) {
            Infos.LotEquipmentList lotEquipmentList = new Infos.LotEquipmentList();
            lotEquipmentList.setEquipmentName((String) objects[2]);
            lotEquipmentList.setEquipmentID(new ObjectIdentifier(
                    (String) objects[0], (String) objects[1]));
            lotEquipmentLists.add(lotEquipmentList);
        }
        return lotEquipmentLists;
    }

    private List<Infos.LotEquipmentList> emptyLotEquipmentList() {
        List<Infos.LotEquipmentList> lotEquipmentLists = new ArrayList<>(1);
        Infos.LotEquipmentList lotEquipmentList = new Infos.LotEquipmentList();
        lotEquipmentLists.add(lotEquipmentList);
        lotEquipmentList.setEquipmentID(new ObjectIdentifier());
        lotEquipmentList.setEquipmentName("");
        return lotEquipmentLists;
    }

    @Override
    public Outputs.CassetteZoneTypeGetOut cassetteZoneTypeGet(Infos.ObjCommon objCommonIn, ObjectIdentifier cassetteID) {
        Outputs.CassetteZoneTypeGetOut cassetteZoneTypeGetOut = new Outputs.CassetteZoneTypeGetOut();
        Infos.LotInfo zoneLotInfo = new Infos.LotInfo();
        List<Lot> aLotList = null;
        CimLot aLot = null;
        cassetteZoneTypeGetOut.setZoneType(BizConstant.SP_ZONE_DEFAULT);
        if (!ObjectIdentifier.isEmptyWithValue(cassetteID)) {
            Outputs.ObjCassetteStatusOut objCassetteStatusOut = this.cassetteGetStatusDR(objCommonIn, cassetteID);
            String emptyValue = objCassetteStatusOut.getCastUsedCapacity() == 0L ? BizConstant.SP_ZONE_EMPTY : BizConstant.SP_ZONE_OCUPIED;
            String ccateValue = objCassetteStatusOut.getCastCategory();
            String cstatValue = objCassetteStatusOut.getDurableState();
            com.fa.cim.newcore.bo.durable.CimCassette aCassette = baseCoreFactory.getBO(com.fa.cim.newcore.bo.durable.CimCassette.class, cassetteID);
            Validations.check(aCassette == null, new OmCode(retCodeConfig.getNotFoundCassette(), cassetteID.getValue()));
            aLotList = aCassette.allLots();

            Infos.LotInfo aNowLotInfo = null;
            int statWaitingCnt = 0;
            int statOnHoldCnt = 0;
            int statInBankCnt = 0;
            int statScrappedCnt = 0;
            int typeProdCnt = 0;
            int typeEngCnt = 0;
            int typePMonCnt = 0;
            int typeEMonCnt = 0;
            int typeDumyCnt = 0;
            int typeOthCnt = 0;
            int LSLength = CimArrayUtils.getSize(aLotList);
            boolean bFirstSetting = false;
            long highRemainTime = 0;
            long nowRemainTime = 0;
            long zoneQTime = 0;
            int nowPriortyClass = 0;
            int zonePriortyClass = 0;
            long nowExPriority = 0L;
            long zoneExPriority = 0L;
            int nowInPriorty = 0;
            int zoneInPriorty = 0;
            boolean firstLotFlag = true;

            for (int i = 0; i < LSLength; i++) {
                aLot = (CimLot) aLotList.get(i);
                Validations.check(null == aLot, new OmCode(retCodeConfig.getNotFoundLot(), ""));
                aNowLotInfo = new Infos.LotInfo();
                ProductDTO.LotBaseInfo lotBaseInfo = aLot.getLotBaseInfo();
                Infos.LotBasicInfo lotBasicInfo = new Infos.LotBasicInfo();
                aNowLotInfo.setLotBasicInfo(lotBasicInfo);
                lotBasicInfo.setLotID(lotBaseInfo.getLotID());
                lotBasicInfo.setInternalPriority(lotBaseInfo.getInternalPriority());
                lotBasicInfo.setExternalPriority(lotBaseInfo.getExternalPriority());
                lotBasicInfo.setPriorityClass(lotBaseInfo.getPriorityClass().intValue());
                lotBasicInfo.setLotStatus(lotBaseInfo.getRepresentativeState());
                lotBasicInfo.setLotType(lotBaseInfo.getLotType());

                List<ProductDTO.QTimeInformation> qTimeInfoList = aLot.getQTimeInfo();
                if (!CimArrayUtils.isEmpty(qTimeInfoList)) {
                    lotBasicInfo.setQtimeFlag(false);
                    for (ProductDTO.QTimeInformation qTimeInfo : qTimeInfoList) {
                        if (CimStringUtils.isNotEmpty(qTimeInfo.getQrestrictionTargetTimeStamp())
                                || !CimStringUtils.equals(qTimeInfo.getQrestrictionTargetTimeStamp(), BizConstant.SP_TIMESTAMP_NIL_OBJECT_STRING)) {
                            lotBasicInfo.setQtimeFlag(true);
                            break;
                        }
                    }
                }
                if (CimBooleanUtils.isFalse(lotBasicInfo.getQtimeFlag())) {
                    Integer qTimeSeqForWaferCount = aLot.getWaferLevelQTimeCount();
                    if (qTimeSeqForWaferCount > 0) {
                        List<com.fa.cim.newcore.bo.pd.CimQTimeRestriction> qtimeSeqForWafer = aLot.allQTimeRestrictionsForWaferLevelQTime();
                        qTimeSeqForWaferCount = CimArrayUtils.getSize(qtimeSeqForWafer);
                        if (qTimeSeqForWaferCount > 0) {
                            for (com.fa.cim.newcore.bo.pd.CimQTimeRestriction aQTime : qtimeSeqForWafer) {
                                if (null == aQTime) {
                                    continue;
                                }
                                ProcessDTO.QTimeRestrictionInfo aQTimeInfo = aQTime.getQTimeRestrictionInfo();
                                if (null == aQTimeInfo || CimStringUtils.isEmpty(aQTimeInfo.getTriggerOperationNumber())) {
                                    continue;
                                }
                                if (CimStringUtils.isEmpty(aQTimeInfo.getTargetTimeStamp()) || CimStringUtils.equals(aQTimeInfo.getTargetTimeStamp(), BizConstant.SP_TIMESTAMP_NIL_OBJECT_STRING)) {
                                    continue;
                                }
                                lotBasicInfo.setQtimeFlag(true);
                                break;
                            }
                        }
                    }
                }

                // lot Stat
                if (CimStringUtils.equals(aNowLotInfo.getLotBasicInfo().getLotStatus(), BizConstant.SP_LOT_PROCSTATE_WAITING)) {
                    statWaitingCnt++;
                } else if (CimStringUtils.equals(aNowLotInfo.getLotBasicInfo().getLotStatus(), CIMStateConst.CIM_LOT_HOLD_STATE_ONHOLD)) {
                    statOnHoldCnt++;
                } else if (CimStringUtils.equals(aNowLotInfo.getLotBasicInfo().getLotStatus(), BizConstant.SP_LOT_INVENTORYSTATE_INBANK)) {
                    statInBankCnt++;
                } else if (CimStringUtils.equals(aNowLotInfo.getLotBasicInfo().getLotStatus(), CIMStateConst.CIM_LOT_FINISHED_STATE_SCRAPPED)) {
                    statScrappedCnt++;
                }
                // lot Type
                if (CimStringUtils.equals(aNowLotInfo.getLotBasicInfo().getLotType(), BizConstant.SP_LOT_TYPE_PRODUCTIONLOT)) {
                    typeProdCnt++;
                } else if (CimStringUtils.equals(aNowLotInfo.getLotBasicInfo().getLotType(), BizConstant.SP_LOT_TYPE_ENGINEERINGLOT)) {
                    typeEngCnt++;
                } else if (CimStringUtils.equals(aNowLotInfo.getLotBasicInfo().getLotType(), BizConstant.SP_LOT_TYPE_PRODUCTIONMONITORLOT)) {
                    typePMonCnt++;
                } else if (CimStringUtils.equals(aNowLotInfo.getLotBasicInfo().getLotType(), BizConstant.SP_LOT_TYPE_EQUIPMENTMONITORLOT)) {
                    typeEMonCnt++;
                } else if (CimStringUtils.equals(aNowLotInfo.getLotBasicInfo().getLotType(), BizConstant.SP_LOT_TYPE_DUMMYLOT)) {
                    typeDumyCnt++;
                } else {
                    typeOthCnt++;
                }
                /*-----------------------------------------------------*/
                /*  Sorting fields;                                    */
                /*  1st field :  qrestrictionRemainTime                */
                /*  2st field :  priortyClass                          */
                /*  3rd field :  externalPriority                      */
                /*  4th field :  internalPriority                      */
                /*-----------------------------------------------------*/

                if (CimBooleanUtils.isTrue(aNowLotInfo.getLotBasicInfo().getQtimeFlag())) {
                    ObjectIdentifier lotID = new ObjectIdentifier(aLot.getIdentifier(), aLot.getPrimaryKey());
                    Outputs.ObjLotQTimeGetDROut lotQTimeGetDRout = lotMethod.lotQTimeGetDR(objCommonIn, lotID);
                    List<Infos.LotQtimeInfo> jQTimeInfos = lotQTimeGetDRout.getStrLotQtimeInfoList();
                    int LQTILength = CimArrayUtils.getSize(jQTimeInfos);
                    boolean firstOneFlag = true;
                    // find the highest remainTime in the jQTimeInfos List
                    int qactionCount = 0;
                    for (Infos.LotQtimeInfo jQTimeInfo : jQTimeInfos) {
                        if (CimStringUtils.isEmpty(jQTimeInfo.getQrestrictionTargetTimeStamp())
                                || CimStringUtils.equals(jQTimeInfo.getQrestrictionTargetTimeStamp(), BizConstant.SP_TIMESTAMP_NIL_OBJECT_STRING)) {
                            qactionCount++;
                            continue;
                        }
                        if (firstOneFlag) {
                            highRemainTime = (long) Double.parseDouble(jQTimeInfo.getQrestrictionRemainTime());
                            firstOneFlag = false;
                        } else {
                            nowRemainTime = (long) Double.parseDouble(jQTimeInfo.getQrestrictionRemainTime());
                            if (highRemainTime > nowRemainTime) {
                                highRemainTime = nowRemainTime;
                            }
                        }
                    }
                    if (LQTILength == qactionCount) {
                        continue;
                    }
                    // Determin Q-Time Lot
                    if (!bFirstSetting) {
                        zoneQTime = highRemainTime;
                        zoneLotInfo = aNowLotInfo;
                        bFirstSetting = true;
                    } else {
                        if (zoneQTime > highRemainTime) {
                            zoneQTime = highRemainTime;
                            zoneLotInfo = aNowLotInfo;
                        } else if (zoneQTime == highRemainTime) {
                            /*********************************/
                            /* If Q-Time is same and highest */
                            /* Check Priority Class          */
                            /*********************************/
                            nowPriortyClass = aNowLotInfo.getLotBasicInfo().getPriorityClass();
                            zonePriortyClass = zoneLotInfo.getLotBasicInfo().getPriorityClass();
                            if (zonePriortyClass > nowPriortyClass) {
                                zoneLotInfo = aNowLotInfo;
                            } else if (nowPriortyClass == zonePriortyClass) {
                                /*******************************************/
                                /* If Priority Class is same and highest   */
                                /* Check externalPriority                  */
                                /*******************************************/
                                nowExPriority = aNowLotInfo.getLotBasicInfo().getExternalPriority();
                                zoneExPriority = zoneLotInfo.getLotBasicInfo().getExternalPriority();
                                if (zoneExPriority > nowExPriority) {
                                    zoneLotInfo = aNowLotInfo;
                                } else if (zoneExPriority == nowExPriority) {
                                    /*******************************************/
                                    /* If externalPriority is same and highest */
                                    /* Check internalPriority                  */
                                    /*******************************************/
                                    nowInPriorty = aNowLotInfo.getLotBasicInfo().getInternalPriority() == null ? 0 : aNowLotInfo.getLotBasicInfo().getInternalPriority().intValue();
                                    zoneInPriorty = zoneLotInfo.getLotBasicInfo().getInternalPriority() == null ? 0 : zoneLotInfo.getLotBasicInfo().getInternalPriority().intValue();
                                    if (zoneInPriorty > nowInPriorty) {
                                        zoneLotInfo = aNowLotInfo;
                                    }
                                }
                            }
                        }
                    }
                } else if (i == 0) {
                    zoneLotInfo = aNowLotInfo;
                } else {

                    /*******************************************/
                    /* If Priority Class is same and highest   */
                    /* Check externalPriority                  */
                    /*******************************************/

                    /*******************************************/
                    /* If externalPriority is same and highest */
                    /* Check internalPriority                  */
                    /*******************************************/
                    nowInPriorty = aNowLotInfo.getLotBasicInfo().getInternalPriority() == null ? 0 : aNowLotInfo.getLotBasicInfo().getInternalPriority().intValue();
                    zoneInPriorty = zoneLotInfo.getLotBasicInfo().getInternalPriority() == null ? 0 : zoneLotInfo.getLotBasicInfo().getInternalPriority().intValue();
                    if (zoneInPriorty > nowInPriorty) {
                        zoneLotInfo = aNowLotInfo;
                    }

                }
            }
            /*******************************************/
            // Add Priority for representative Lot
            /*******************************************/
            if (LSLength > 0) {
                cassetteZoneTypeGetOut.setPriority(String.valueOf(zoneLotInfo.getLotBasicInfo().getPriorityClass()));
            }
            /*******************************************/
            // Get Next EquipmentID
            /*******************************************/
            boolean lotFound = true;
            com.fa.cim.newcore.bo.product.CimLot aLotInner = null;
            if (null != zoneLotInfo.getLotBasicInfo() && ObjectIdentifier.isNotEmptyWithValue(zoneLotInfo.getLotBasicInfo().getLotID())) {
                aLotInner = productManager.findLotNamed(zoneLotInfo.getLotBasicInfo().getLotID().getValue());
            }
            if (aLotInner == null) {
                lotFound = false;
            }
            String etype_value = null;
            List<ObjectIdentifier> equipmentIDs = new ArrayList<>();
            boolean bEquipmentTypeGet = false;
            if (lotFound) {
                List<CimMachine> aMachineSeq = aLotInner.getQueuedMachines();
                int lenMachine = CimArrayUtils.getSize(aMachineSeq);
                for (int j = 0; j < lenMachine; j++) {
                    if (aMachineSeq.get(j) != null) {
                        ObjectIdentifier equipmentID = new ObjectIdentifier(aMachineSeq.get(j).getIdentifier(), aMachineSeq.get(j).getPrimaryKey());
                        equipmentIDs.add(equipmentID);
                    }
                }
                if (lenMachine > 0) {
                    /*******************************************/
                    // get equipmentType
                    /*******************************************/
                    etype_value = equipmentMethod.equipmentGetTypeDR(objCommonIn, equipmentIDs.get(0));
                    bEquipmentTypeGet = true;
                }
            }
            String zoneFlag = StandardProperties.OM_STK_ZONE_TYPE_FLAG.getValue();
            if (!CimStringUtils.equals(zoneFlag, "ON")) {
                Infos.LotBasicInfo lotBasicInfo = zoneLotInfo.getLotBasicInfo();
                if (CimStringUtils.equals(emptyValue, BizConstant.SP_ZONE_EMPTY)) {
                    cassetteZoneTypeGetOut.setZoneType(BizConstant.SP_ZONE_EMPTY);
                } else if (lotBasicInfo != null && CimStringUtils.equals(lotBasicInfo.getLotStatus(), CIMStateConst.CIM_LOT_HOLD_STATE_ONHOLD)) {
                    cassetteZoneTypeGetOut.setZoneType(CIMStateConst.CIM_LOT_HOLD_STATE_ONHOLD);
                } else if (lotBasicInfo != null && CimStringUtils.equals(lotBasicInfo.getLotStatus(), BizConstant.SP_LOT_INVENTORYSTATE_INBANK)) {
                    cassetteZoneTypeGetOut.setZoneType(BizConstant.SP_LOT_INVENTORYSTATE_INBANK);
                } else if (lotBasicInfo != null && CimStringUtils.equals(lotBasicInfo.getLotStatus(), CIMStateConst.CIM_LOT_FINISHED_STATE_SCRAPPED)) {
                    cassetteZoneTypeGetOut.setZoneType(CIMStateConst.CIM_LOT_FINISHED_STATE_SCRAPPED);
                } else if (lotBasicInfo != null && CimStringUtils.equals(lotBasicInfo.getLotStatus(), BizConstant.SP_LOT_TYPE_ENGINEERINGLOT)) {
                    cassetteZoneTypeGetOut.setZoneType(BizConstant.SP_LOT_TYPE_ENGINEERINGLOT);
                } else if (lotBasicInfo != null && CimStringUtils.equals(lotBasicInfo.getLotStatus(), BizConstant.SP_LOT_TYPE_PRODUCTIONMONITORLOT)) {
                    cassetteZoneTypeGetOut.setZoneType(BizConstant.SP_LOT_TYPE_PRODUCTIONMONITORLOT);
                } else if (lotBasicInfo != null && CimStringUtils.equals(lotBasicInfo.getLotStatus(), BizConstant.SP_LOT_TYPE_EQUIPMENTMONITORLOT)) {
                    cassetteZoneTypeGetOut.setZoneType(BizConstant.SP_LOT_TYPE_EQUIPMENTMONITORLOT);
                } else if (lotBasicInfo != null && CimStringUtils.equals(lotBasicInfo.getLotStatus(), BizConstant.SP_LOT_TYPE_DUMMYLOT)) {
                    cassetteZoneTypeGetOut.setZoneType(BizConstant.SP_LOT_TYPE_DUMMYLOT);
                }
            } else {
                List<CimEnvironmentZoneTypeDO> environmentZoneTypeDOList = cimJpaRepository.query("SELECT * FROM OMENV_ZONETYPE", CimEnvironmentZoneTypeDO.class);
                if (!CimArrayUtils.isEmpty(environmentZoneTypeDOList)) {
                    for (CimEnvironmentZoneTypeDO cimEnvironmentZoneTypeDO : environmentZoneTypeDOList) {
                        String carrierAttributteType = cimEnvironmentZoneTypeDO.getCarrierAttributteType();
                        String carrierAttributeValue = cimEnvironmentZoneTypeDO.getCarrierAttributeValue();
                        String zoneTypeValue = cimEnvironmentZoneTypeDO.getZoneType();
                        if (CimStringUtils.equals(carrierAttributteType, BizConstant.SP_ZONE_ATTR_EMPTY)) {
                            if (CimStringUtils.equals(carrierAttributeValue, emptyValue)) {
                                cassetteZoneTypeGetOut.setZoneType(zoneTypeValue);
                                break;
                            }
                        } else if (CimStringUtils.equals(carrierAttributteType, BizConstant.SP_ZONE_ATTR_CATEGORY)) {
                            if (CimStringUtils.equals(carrierAttributeValue, ccateValue)) {
                                cassetteZoneTypeGetOut.setZoneType(zoneTypeValue);
                                break;
                            }
                        } else if (CimStringUtils.equals(carrierAttributteType, BizConstant.SP_ZONE_ATTR_CARRIER_STATE)) {
                            if (CimStringUtils.equals(carrierAttributeValue, cstatValue)) {
                                cassetteZoneTypeGetOut.setZoneType(zoneTypeValue);
                                break;
                            }
                        } else if (CimStringUtils.equals(carrierAttributteType, BizConstant.SP_ZONE_ATTR_PRIORITY_CLASS)) {
                            if (zoneLotInfo.getLotBasicInfo() != null) {
                                if (CimStringUtils.equals(carrierAttributeValue, String.valueOf(zoneLotInfo.getLotBasicInfo().getPriorityClass()))) {
                                    cassetteZoneTypeGetOut.setZoneType(zoneTypeValue);
                                    break;
                                }
                            }
                        } else if (CimStringUtils.equals(carrierAttributteType, BizConstant.SP_ZONE_ATTR_NEXT_EQUIPMENT)) {
                            int lenNextEqp = CimArrayUtils.getSize(equipmentIDs);
                            for (int j = 0; j < lenNextEqp; j++) {
                                if (ObjectIdentifier.equalsWithValue(carrierAttributeValue, equipmentIDs.get(j))) {
                                    cassetteZoneTypeGetOut.setZoneType(zoneTypeValue);
                                    break;
                                }
                            }
                        } else if (CimStringUtils.equals(carrierAttributteType, BizConstant.SP_ZONE_ATTR_NEXT_EQUIPMENT_TYPE)) {
                            if (CimStringUtils.equals(carrierAttributeValue, etype_value) && bEquipmentTypeGet) {
                                cassetteZoneTypeGetOut.setZoneType(zoneTypeValue);
                                break;
                            }
                        } else if (CimStringUtils.equals(carrierAttributteType, BizConstant.SP_ZONE_ATTR_LOT_STATUS_ALL)) {
                            if (CimStringUtils.equals(carrierAttributeValue, BizConstant.SP_LOT_PROCSTATE_WAITING)) {
                                if (statWaitingCnt == LSLength) {
                                    cassetteZoneTypeGetOut.setZoneType(zoneTypeValue);
                                    break;
                                }
                            } else if (CimStringUtils.equals(carrierAttributeValue, CIMStateConst.CIM_LOT_HOLD_STATE_ONHOLD)) {
                                if (statOnHoldCnt == LSLength) {
                                    cassetteZoneTypeGetOut.setZoneType(zoneTypeValue);
                                    break;
                                }
                            } else if (CimStringUtils.equals(carrierAttributeValue, BizConstant.SP_LOT_INVENTORYSTATE_INBANK)) {
                                if (statInBankCnt == LSLength) {
                                    cassetteZoneTypeGetOut.setZoneType(zoneTypeValue);
                                    break;
                                }
                            } else if (CimStringUtils.equals(carrierAttributeValue, CIMStateConst.CIM_LOT_FINISHED_STATE_SCRAPPED)) {
                                if (statScrappedCnt == LSLength) {
                                    cassetteZoneTypeGetOut.setZoneType(zoneTypeValue);
                                    break;
                                }
                            }
                        } else if (CimStringUtils.equals(carrierAttributteType, BizConstant.SP_ZONE_ATTR_LOT_TYPE_ALL)) {
                            if (CimStringUtils.equals(carrierAttributeValue, BizConstant.SP_LOT_TYPE_PRODUCTIONLOT)) {
                                if (typeProdCnt == LSLength) {
                                    cassetteZoneTypeGetOut.setZoneType(zoneTypeValue);
                                    break;
                                }
                            } else if (CimStringUtils.equals(carrierAttributeValue, BizConstant.SP_LOT_TYPE_ENGINEERINGLOT)) {
                                if (typeEngCnt == LSLength) {
                                    cassetteZoneTypeGetOut.setZoneType(zoneTypeValue);
                                    break;
                                }
                            } else if (CimStringUtils.equals(carrierAttributeValue, BizConstant.SP_LOT_TYPE_PRODUCTIONMONITORLOT)) {
                                if (typePMonCnt == LSLength) {
                                    cassetteZoneTypeGetOut.setZoneType(zoneTypeValue);
                                    break;
                                }
                            } else if (CimStringUtils.equals(carrierAttributeValue, BizConstant.SP_LOT_TYPE_EQUIPMENTMONITORLOT)) {
                                if (typeEMonCnt == LSLength) {
                                    cassetteZoneTypeGetOut.setZoneType(zoneTypeValue);
                                    break;
                                }
                            } else if (CimStringUtils.equals(carrierAttributeValue, BizConstant.SP_LOT_TYPE_DUMMYLOT)) {
                                if (typeDumyCnt == LSLength) {
                                    cassetteZoneTypeGetOut.setZoneType(zoneTypeValue);
                                    break;
                                }
                            }
                        } else if (CimStringUtils.equals(carrierAttributteType, BizConstant.SP_ZONE_ATTR_LOT_STATUS_ONE)) {
                            if (CimStringUtils.equals(carrierAttributeValue, BizConstant.SP_LOT_PROCSTATE_WAITING)) {
                                if (statWaitingCnt > 0) {
                                    cassetteZoneTypeGetOut.setZoneType(zoneTypeValue);
                                    break;
                                }
                            } else if (CimStringUtils.equals(carrierAttributeValue, CIMStateConst.CIM_LOT_HOLD_STATE_ONHOLD)) {
                                if (statOnHoldCnt > 0) {
                                    cassetteZoneTypeGetOut.setZoneType(zoneTypeValue);
                                    break;
                                }
                            } else if (CimStringUtils.equals(carrierAttributeValue, BizConstant.SP_LOT_INVENTORYSTATE_INBANK)) {
                                if (statInBankCnt > 0) {
                                    cassetteZoneTypeGetOut.setZoneType(zoneTypeValue);
                                    break;
                                }
                            } else if (CimStringUtils.equals(carrierAttributeValue, CIMStateConst.CIM_LOT_FINISHED_STATE_SCRAPPED)) {
                                if (statScrappedCnt > 0) {
                                    cassetteZoneTypeGetOut.setZoneType(zoneTypeValue);
                                    break;
                                }
                            }
                        } else if (CimStringUtils.equals(carrierAttributteType, BizConstant.SP_ZONE_ATTR_LOT_TYPE_ONE)) {
                            if (CimStringUtils.equals(carrierAttributeValue, BizConstant.SP_LOT_TYPE_PRODUCTIONLOT)) {
                                if (typeProdCnt > 0) {
                                    cassetteZoneTypeGetOut.setZoneType(zoneTypeValue);
                                    break;
                                }
                            } else if (CimStringUtils.equals(carrierAttributeValue, BizConstant.SP_LOT_TYPE_ENGINEERINGLOT)) {
                                if (typeEngCnt > 0) {
                                    cassetteZoneTypeGetOut.setZoneType(zoneTypeValue);
                                    break;
                                }
                            } else if (CimStringUtils.equals(carrierAttributeValue, BizConstant.SP_LOT_TYPE_PRODUCTIONMONITORLOT)) {
                                if (typePMonCnt > 0) {
                                    cassetteZoneTypeGetOut.setZoneType(zoneTypeValue);
                                    break;
                                }
                            } else if (CimStringUtils.equals(carrierAttributeValue, BizConstant.SP_LOT_TYPE_EQUIPMENTMONITORLOT)) {
                                if (typeEMonCnt > 0) {
                                    cassetteZoneTypeGetOut.setZoneType(zoneTypeValue);
                                    break;
                                }
                            } else if (CimStringUtils.equals(carrierAttributeValue, BizConstant.SP_LOT_TYPE_DUMMYLOT)) {
                                if (typeDumyCnt > 0) {
                                    cassetteZoneTypeGetOut.setZoneType(zoneTypeValue);
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        } else {
            throw new ServiceException(new OmCode(retCodeConfig.getNotFoundCassette(), ObjectIdentifier.fetchValue(cassetteID)));
        }

        return cassetteZoneTypeGetOut;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/11/14                          Wind
     *
     * @param objCommon
     * @param params
     * @return RetCode<Object>
     * @author Wind
     * @date 2018/11/14 17:37
     */
    @Override
    public void cassetteCategoryPortCapabilityCheckForContaminationControl(Infos.ObjCommon objCommon, Params.CarrierMoveFromIBRptParams params) {
        /*---------------------------------------------*/
        /* Get PosCassette Object And cassetteCategory */
        /*---------------------------------------------*/
        CimCassette cassette = baseCoreFactory.getBO(CimCassette.class, params.getCarrierID());
        Validations.check(null == cassette, new OmCode(retCodeConfig.getNotFoundCassette(), ObjectIdentifier.fetchValue(params.getCarrierID())));
        String cassetteCategory = cassette.getCassetteCategory();

        /*-----------------------------------------------------------*/
        /* Get PosPortResource Object And cassetteCategoryCapability */
        /*-----------------------------------------------------------*/
        CimMachine equipment = baseCoreFactory.getBO(CimMachine.class, params.getEquipmentID());
        Validations.check(null == equipment, new OmCode(retCodeConfig.getNotFoundEqp(), ObjectIdentifier.fetchValue(params.getEquipmentID())));

        CimPortResource port = (CimPortResource) equipment.findPortResourceNamed(ObjectIdentifier.fetchValue(params.getDestinationPortID()));
        Validations.check(null == equipment, new OmCode(retCodeConfig.getNotFoundPort(), ObjectIdentifier.fetchValue(params.getDestinationPortID())));


        List<String> portCassetteCapabilitys = port.getCassetteCategoryCapability();
        /*-----------------------------------------------------------*/
        /* It is checked whether a category is the same.             */
        /*-----------------------------------------------------------*/
        boolean bCategory = CimArrayUtils.isEmpty(portCassetteCapabilitys);
        for (int i = 0; i < CimArrayUtils.getSize(portCassetteCapabilitys); i++) {
            if (CimStringUtils.equals(cassetteCategory, portCassetteCapabilitys.get(i))) {
                bCategory = true;
                break;
            }
        }
        Validations.check(!bCategory, retCodeConfig.getCategoryInconsistency());
    }

    @Override
    public void cassetteCheckConditionForFlowBatch(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID,
                                                   ObjectIdentifier flowBatchID, List<Infos.FlowBatchByManualActionReqCassette> flowBatchByManualActionReqCassettes, String operation) {
        String portGroupID = null;

        List<Infos.StartCassette> strStartCassette = new ArrayList<>();
        if (!CimArrayUtils.isEmpty(flowBatchByManualActionReqCassettes)) {
            for (Infos.FlowBatchByManualActionReqCassette flowBatchByManualActionReqCassette : flowBatchByManualActionReqCassettes) {
                Infos.StartCassette startCassette = new Infos.StartCassette();
                startCassette.setCassetteID(flowBatchByManualActionReqCassette.getCassetteID());

                List<ObjectIdentifier> lotID = flowBatchByManualActionReqCassette.getLotID();
                if (!CimArrayUtils.isEmpty(lotID)) {
                    for (ObjectIdentifier lotId : lotID) {
                        List<Infos.LotInCassette> lotInCassetteList = new ArrayList<>();
                        Infos.LotInCassette lotInCassette = new Infos.LotInCassette();
                        lotInCassette.setLotID(lotId);
                        lotInCassette.setMoveInFlag(true);

                        //step1 - lot_materials_GetWafers
                        log.info("step1 - lot_materials_GetWafers");
                        List<Infos.LotWaferAttributes> lotWaferAttributesList = lotMethod.lotMaterialsGetWafers(objCommon, lotId);
                        if (!CimArrayUtils.isEmpty(lotWaferAttributesList)) {
                            List<Infos.LotWafer> lotWaferList = new ArrayList<>();
                            lotInCassette.setLotWaferList(lotWaferList);
                            for (Infos.LotWaferAttributes lotWaferAttributes : lotWaferAttributesList) {
                                Infos.LotWafer lotWafer = new Infos.LotWafer();
                                lotWafer.setWaferID(lotWaferAttributes.getWaferID());
                                lotWafer.setSlotNumber(lotWaferAttributes.getSlotNumber().longValue());
                                lotWafer.setControlWaferFlag(lotWaferAttributes.getControlWaferFlag());
                                lotWaferList.add(lotWafer);
                            }
                            lotInCassetteList.add(lotInCassette);
                            startCassette.setLotInCassetteList(lotInCassetteList);
                        }
                    }
                }
                strStartCassette.add(startCassette);
            }
        }

        //step2 - lot_CheckConditionForFlowBatch
        log.info("step2 - lot_CheckConditionForFlowBatch");
        lotMethod.lotCheckConditionForFlowBatch(objCommon, equipmentID, portGroupID, strStartCassette, operation);
        if (!CimArrayUtils.isEmpty(flowBatchByManualActionReqCassettes)) {
            for (Infos.FlowBatchByManualActionReqCassette flowBatchByManualActionReqCassette : flowBatchByManualActionReqCassettes) {
                // Check cassette's TransferReserveUserID
                if (CimStringUtils.equals(operation, BizConstant.SP_OPERATION_FLOWBATCHING) ||
                        CimStringUtils.equals(operation, BizConstant.SP_OPERATION_FLOWBATCHINGAUTO) ||
                        CimStringUtils.equals(operation, BizConstant.SP_OPERATION_FLOWBATCH_REBATCH)) {
                    Infos.CassetteDBINfoGetDRInfo cassetteDBINfoGetDRInfo = new Infos.CassetteDBINfoGetDRInfo();
                    cassetteDBINfoGetDRInfo.setCassetteID(flowBatchByManualActionReqCassette.getCassetteID());
                    cassetteDBINfoGetDRInfo.setDurableOperationInfoFlag(false);
                    cassetteDBINfoGetDRInfo.setDurableWipOperationInfoFlag(false);
                    //step3 - cassette_DBInfo_GetDR__170
                    log.info("cassette_DBInfo_GetDR__170");
                    Outputs.CassetteDBInfoGetDROut cassetteDBInfo = this.cassetteDBInfoGetDR(objCommon, cassetteDBINfoGetDRInfo);
                    ObjectIdentifier transferReserveUserID = cassetteDBInfo.getCarrierDetailInfoInqResult().getCassetteStatusInfo().getTransferReserveUserID();
                    Validations.check(!ObjectIdentifier.isEmptyWithValue(transferReserveUserID),
                            new OmCode(retCodeConfig.getAlreadyXferReservedCassette(), flowBatchByManualActionReqCassette.getCassetteID().getValue(), transferReserveUserID.getValue()));
                }
                //Check cassette's controljob
                com.fa.cim.newcore.bo.durable.CimCassette aCassette = baseCoreFactory.getBO(com.fa.cim.newcore.bo.durable.CimCassette.class, flowBatchByManualActionReqCassette.getCassetteID());
                Validations.check(aCassette == null, new OmCode(retCodeConfig.getNotFoundCassette(), flowBatchByManualActionReqCassette.getCassetteID().getValue()));
                com.fa.cim.newcore.bo.product.CimControlJob aControlJob = aCassette.getControlJob();
                Validations.check(aControlJob != null, new OmCode(retCodeConfig.getCassetteControlJobFilled()));
                List<ObjectIdentifier> lotIDs = flowBatchByManualActionReqCassette.getLotID();
                for (ObjectIdentifier lotID : lotIDs) {
                    //Check TransferStatus
                    if (CimStringUtils.equals(operation, BizConstant.SP_OPERATION_FLOWBATCHING) ||
                            CimStringUtils.equals(operation, BizConstant.SP_OPERATION_FLOWBATCHINGAUTO) ||
                            CimStringUtils.equals(operation, BizConstant.SP_OPERATION_FLOWBATCH_REBATCH)) {
                        //step4 - lot_transferState_Get
                        log.info("step4 - lot_transferState_Get");
                        String transferStateOut = lotMethod.lotTransferStateGet(objCommon, lotID);
                        if (!CimStringUtils.equals(transferStateOut, BizConstant.SP_TRANSSTATE_STATIONIN) &&
                                !CimStringUtils.equals(transferStateOut, BizConstant.SP_TRANSSTATE_BAYIN) &&
                                !CimStringUtils.equals(transferStateOut, BizConstant.SP_TRANSSTATE_MANUALIN) &&
                                !CimStringUtils.equals(transferStateOut, BizConstant.SP_TRANSSTATE_INTERMEDIATEIN)) {
                            throw new ServiceException(new OmCode(retCodeConfig.getInvalidLotXferstat(), lotID.getValue(), transferStateOut));
                        }
                    }

                    // Check cassette-lot connection
                    //step5 - lot_cassette_CheckSame
                    log.info("step5 - lot_cassette_CheckSame");
                    lotMethod.lotCassetteCheckSame(objCommon, flowBatchByManualActionReqCassette.getLotID());
                    if (!CimStringUtils.equals(operation, BizConstant.SP_OPERATION_FLOWBATCH_EQPRESERVECANCEL)) {
                        if (CimStringUtils.equals(operation, BizConstant.SP_OPERATION_FLOWBATCHING) ||
                                CimStringUtils.equals(operation, BizConstant.SP_OPERATION_FLOWBATCHINGAUTO) ||
                                CimStringUtils.equals(operation, BizConstant.SP_OPERATION_FLOWBATCH_REBATCH) ||
                                CimStringUtils.equals(operation, BizConstant.SP_OPERATION_FLOWBATCH_LOTREMOVE)) {

                            //step6 - lot_flowBatchID_Get
                            log.info("step6 - lot_flowBatchID_Get");
                            try {
                                lotMethod.lotFlowBatchIDGet(objCommon, lotID);
                            } catch (ServiceException e) {
                                ObjectIdentifier tmpFlowBatchID = e.getData(ObjectIdentifier.class);
                                if (Validations.isEquals(retCodeConfig.getLotFlowBatchIdBlank(), e.getCode())) {
                                    if (CimStringUtils.equals(operation, BizConstant.SP_OPERATION_FLOWBATCH_LOTREMOVE)) {
                                        throw new ServiceException(new OmCode(retCodeConfig.getNotBatchedLot(), lotID.getValue()));
                                    }
                                } else if (Validations.isEquals(retCodeConfig.getLotFlowBatchIdFilled(), e.getCode())) {
                                    if (CimStringUtils.equals(operation, BizConstant.SP_OPERATION_FLOWBATCH_LOTREMOVE)) {
                                        if (!ObjectIdentifier.equalsWithValue(tmpFlowBatchID, flowBatchID)) {
                                            throw new ServiceException(new OmCode(retCodeConfig.getLotFlowBatchMismatch(), tmpFlowBatchID.getValue()));
                                        }
                                        throw new ServiceException(retCodeConfig.getLotFlowBatchIdFilled());
                                    }
                                } else {
                                    throw e;
                                }
                            }

                        }
                        if (CimStringUtils.equals(operation, BizConstant.SP_OPERATION_FLOWBATCHING) ||
                                CimStringUtils.equals(operation, BizConstant.SP_OPERATION_FLOWBATCHINGAUTO)) {
                            //Check lot's next oeperation
                            Infos.BatchingReqLot batchingReqLot = new Infos.BatchingReqLot();
                            batchingReqLot.setLotID(lotID);
                            batchingReqLot.setCassetteID(flowBatchByManualActionReqCassette.getCassetteID());
                            //step7 - lot_nextOperation_CheckEntryPointOfFlowBatch
                            log.info("step7 - lot_nextOperation_CheckEntryPointOfFlowBatch");
                            lotMethod.lotNextOperationCheckEntryPointOfFlowBatch(objCommon, batchingReqLot);
                        }
                    }
                    if (CimStringUtils.equals(operation, BizConstant.SP_OPERATION_FLOWBATCH_EQPRESERVECANCEL) ||
                            CimStringUtils.equals(operation, BizConstant.SP_OPERATION_FLOWBATCH_LOTREMOVE)) {
                        //Check dispath status
                        //step8 - cassette_dispatchState_Get
                        log.info("step8 - cassette_dispatchState_Get");
                        Boolean cassetteDispatchState = this.cassetteDispatchStateGet(objCommon, flowBatchByManualActionReqCassette.getCassetteID());
                        Validations.check(CimBooleanUtils.isTrue(cassetteDispatchState), new OmCode(retCodeConfig.getInvalidCastDispatchStat()));
                    }
                }
            }
        }
        /*---------------------------------*/
        /*   Check all Lots in Cassette.   */
        /*---------------------------------*/
        if (CimStringUtils.equals(operation, BizConstant.SP_OPERATION_FLOWBATCHING)) {
            if (!CimArrayUtils.isEmpty(flowBatchByManualActionReqCassettes)) {
                for (Infos.FlowBatchByManualActionReqCassette flowBatchByManualActionReqCassette : flowBatchByManualActionReqCassettes) {
                    //step9 - cassette_GetLotList
                    log.info("step9 - cassette_GetLotList");
                    Infos.LotListInCassetteInfo objCassetteLotListGetDROut = this.cassetteGetLotList(objCommon, flowBatchByManualActionReqCassette.getCassetteID());
                    List<ObjectIdentifier> lotIDList = objCassetteLotListGetDROut.getLotIDList();
                    List<ObjectIdentifier> flowBatchByManualActionReqCassetteLotIDList = flowBatchByManualActionReqCassette.getLotID();
                    Validations.check(flowBatchByManualActionReqCassetteLotIDList.size() != lotIDList.size(), retCodeConfig.getNotEnoughLotForFlowBatch());
                    boolean bFound = false;
                    for (ObjectIdentifier lotID : lotIDList) {
                        bFound = false;
                        for (ObjectIdentifier flowBatchByManualActionReqCassetteLotID : flowBatchByManualActionReqCassetteLotIDList) {
                            if (ObjectIdentifier.equalsWithValue(lotID, flowBatchByManualActionReqCassetteLotID)) {
                                bFound = true;
                                break;
                            }
                        }
                        Validations.check(!bFound, retCodeConfig.getNotEnoughLotForFlowBatch());
                    }
                }
            }
        }
    }

    @Override
    public void cassetteCheckCountForFlowBatch(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, List<Infos.FlowBatchByManualActionReqCassette> flowBatchByManualActionReqCassettes, String operation, String claimMemo) {
        int cassetteLen = CimArrayUtils.getSize(flowBatchByManualActionReqCassettes);
        long savedMaxSize = 0;
        long savedMinSize = 0;
        long savedMinWaferSize = 0;
        long totalWaferCount = 0;
        long nAllLotCount = 0;
        //----------------------------------------------
        //   Check lot count to create new Flow Batch
        //----------------------------------------------
        for (int i = 0; i < cassetteLen; i++) {
            List<ObjectIdentifier> lotIDs = flowBatchByManualActionReqCassettes.get(i).getLotID();
            int lotLen = CimArrayUtils.getSize(lotIDs);
            nAllLotCount += lotLen;
            for (int j = 0; j < lotLen; j++) {
                //--------------------------
                //   Get WaferCount of lot
                //--------------------------
                List<Infos.LotWaferAttributes> lotMaterialsGetWafersOut = lotMethod.lotMaterialsGetWafers(objCommon, lotIDs.get(j));
                totalWaferCount += CimArrayUtils.getSize(lotMaterialsGetWafersOut);
                /*----------------------------------------------*/
                /*   Get lot's flow batch section information   */
                /*----------------------------------------------*/
                Outputs.ObjProcessGetFlowBatchDefinitionOut objProcessGetFlowBatchDefinitionOut = processMethod.processGetFlowBatchDefinition(objCommon, lotIDs.get(j));
                //--------------------------------------------
                //   Save max / min batch size for each lot
                //--------------------------------------------
                if (i == 0 && j == 0) {
                    savedMaxSize = objProcessGetFlowBatchDefinitionOut.getSize();
                    savedMinSize = objProcessGetFlowBatchDefinitionOut.getMinimumSize();
                    savedMinWaferSize = objProcessGetFlowBatchDefinitionOut.getMinWaferCount();
                    continue;
                } else {
                    if (objProcessGetFlowBatchDefinitionOut.getSize() < savedMaxSize) {
                        savedMaxSize = objProcessGetFlowBatchDefinitionOut.getSize();
                    }
                    if (objProcessGetFlowBatchDefinitionOut.getMinimumSize() > savedMinSize) {
                        savedMinSize = objProcessGetFlowBatchDefinitionOut.getMinimumSize();
                    }
                    if (objProcessGetFlowBatchDefinitionOut.getMinWaferCount() < savedMinWaferSize) {
                        savedMinWaferSize = objProcessGetFlowBatchDefinitionOut.getMinWaferCount();
                    }
                }
            }
        }
        //--------------------------------
        //   Check max / min batch size
        //--------------------------------
        Validations.check((cassetteLen > savedMaxSize || cassetteLen < savedMinSize),
                new OmCode(retCodeConfig.getInvalidCassetteCountForBatch(), String.valueOf(savedMinSize), String.valueOf(savedMaxSize)));
        //---------------------------------------------
        //  AutoFlowBatching must surely be MaxSize.
        //---------------------------------------------
        if (CimStringUtils.equals(operation, BizConstant.SP_OPERATION_FLOWBATCHINGAUTO)) {
            Outputs.ObjEquipmentProcessBatchConditionGetOut objEquipmentProcessBatchConditionGetOut = equipmentMethod.equipmentProcessBatchConditionGet(objCommon, equipmentID);
            if (savedMaxSize > objEquipmentProcessBatchConditionGetOut.getMaxBatchSize()) {
                savedMaxSize = objEquipmentProcessBatchConditionGetOut.getMaxBatchSize();
            }
            Validations.check(cassetteLen != savedMaxSize,
                    new OmCode(retCodeConfig.getInvalidCassetteCountForBatch(), String.valueOf(savedMaxSize), String.valueOf(savedMaxSize)));
        }
        //----------------------------
        //   Check MinimumWaferSize
        //----------------------------
        if (CimStringUtils.equals(operation, BizConstant.SP_OPERATION_FLOWBATCHINGAUTO)
                || CimStringUtils.equals(operation, BizConstant.SP_OPERATION_FLOWBATCHING)
                || CimStringUtils.equals(operation, BizConstant.SP_OPERATION_FLOWBATCH_LOTREMOVE)
                || CimStringUtils.equals(operation, BizConstant.SP_OPERATION_FLOWBATCH_EQPRESERVE)
                || CimStringUtils.equals(operation, BizConstant.SP_OPERATION_FLOWBATCH_REBATCH)) {
            Validations.check(savedMinWaferSize > totalWaferCount,
                    new OmCode(retCodeConfig.getInvalidFlowBatchMinwaferCnt(), String.valueOf(totalWaferCount), String.valueOf(savedMinWaferSize)));
        }
        if (!CimStringUtils.equals(operation, BizConstant.SP_OPERATION_FLOWBATCH_LOTREMOVE)
                && !CimStringUtils.equals(operation, BizConstant.SP_OPERATION_FLOWBATCH_EQPRESERVE)) {
            List<Infos.BatchingReqLot> strBatchingReqLot = new ArrayList<>();
            for (int i = 0; i < cassetteLen; i++) {
                List<ObjectIdentifier> lotIDs = flowBatchByManualActionReqCassettes.get(i).getLotID();
                int lotLen = lotIDs.size();
                for (int j = 0; j < lotLen; j++) {
                    Infos.BatchingReqLot batchingReqLot = new Infos.BatchingReqLot();
                    batchingReqLot.setCassetteID(flowBatchByManualActionReqCassettes.get(i).getCassetteID());
                    batchingReqLot.setLotID(lotIDs.get(j));
                    strBatchingReqLot.add(batchingReqLot);
                }
            }
            // lot_operation_CheckSameForFlowBatch
            lotMethod.lotOperationCheckSameForFlowBatch(objCommon, equipmentID, strBatchingReqLot);
        }
        //---------------------------------------------------------
        //   Check about MaxSize, MinSize of EQP-BatchCondition.
        //---------------------------------------------------------
        if (CimStringUtils.equals(operation, BizConstant.SP_OPERATION_FLOWBATCHING)
                || CimStringUtils.equals(operation, BizConstant.SP_OPERATION_FLOWBATCH_LOTREMOVE)
                || CimStringUtils.equals(operation, BizConstant.SP_OPERATION_FLOWBATCH_EQPRESERVE)) {
            if (!ObjectIdentifier.isEmptyWithValue(equipmentID)) {
                Outputs.ObjEquipmentProcessBatchConditionGetOut objEquipmentProcessBatchConditionGetOut = equipmentMethod.equipmentProcessBatchConditionGet(objCommon, equipmentID);
                if (cassetteLen < objEquipmentProcessBatchConditionGetOut.getMinBatchSize()
                        || cassetteLen > objEquipmentProcessBatchConditionGetOut.getMaxBatchSize()) {
                    String minSize = String.valueOf(objEquipmentProcessBatchConditionGetOut.getMinBatchSize());
                    String maxSize = String.valueOf(objEquipmentProcessBatchConditionGetOut.getMaxBatchSize());
                    throw new ServiceException(new OmCode(retCodeConfig.getInvalidProcessBatchCount(), String.valueOf(cassetteLen), maxSize, minSize));
                }

                // cassetteLen is checked about MinWaferSize of EQP-BatchCondition.
                if (objEquipmentProcessBatchConditionGetOut.getMinWaferSize() > totalWaferCount) {
                    throw new ServiceException(retCodeConfig.getInvalidInputWaferCount());
                }
            }
        }
    }

    @Override
    public void cassetteCheckConditionForStartReserveCancel(Infos.ObjCommon objCommon, ObjectIdentifier controlJobID, List<Infos.StartCassette> strStartCassette) {
        /*----------------*/
        /*   Initialize   */
        /*----------------*/
        /*--------------------------------------*/
        /*   Check Cassette's Transfer Status   */
        /*--------------------------------------*/
        int sclength = CimArrayUtils.getSize(strStartCassette);
        log.info("loop to strStartCassette.size() : {}", sclength);
        for (int i = 0; i < sclength; i++) {
            /*-------------------------*/
            /*   Get Cassette Object   */
            /*-------------------------*/
            com.fa.cim.newcore.bo.durable.CimCassette aCassette = baseCoreFactory.getBO(com.fa.cim.newcore.bo.durable.CimCassette.class, strStartCassette.get(i).getCassetteID());
            Validations.check(aCassette == null, new OmCode(retCodeConfig.getNotFoundCassette(), strStartCassette.get(i).getCassetteID().getValue()));
            /*-----------------------*/
            /*   Get DispatchState   */
            /*-----------------------*/
            boolean dispatchState = aCassette.isDispatchReserved();
            log.info("dispatchState : {}", dispatchState);
            Validations.check(!dispatchState, retCodeConfig.getNotDispatchReservedCassette());
            /*----------------------------------*/
            /*   Get controlJobID of Cassette   */
            /*----------------------------------*/
            com.fa.cim.newcore.bo.product.CimControlJob aControlJob = aCassette.getControlJob();
            Validations.check(null == aControlJob, retCodeConfig.getCastControlJobIdBlank());
            Validations.check(!ObjectIdentifier.equalsWithValue(controlJobID, aControlJob.getControlJobID()), retCodeConfig.getCassetteControlJobMix());
        }
        //D7000009 If this check is carried out, StartReserveCancel can NOT perform immediately after loading Carrier.
        //D7000009 So the following checks are not performed when TCS user does claim.
        if (!CimStringUtils.equals(BizConstant.SP_TCS_PERSON, objCommon.getUser().getUserID().getValue())) {
            /*-----------------------------------*/
            /*   Check Cassettes of CJ on Port   */
            /*-----------------------------------*/
            log.info("Check Cassettes of CJ on Port");
            com.fa.cim.newcore.bo.product.CimControlJob aControlJob = baseCoreFactory.getBO(com.fa.cim.newcore.bo.product.CimControlJob.class, controlJobID);
            Validations.check(aControlJob == null, retCodeConfig.getNotFoundControlJob());
            CimMachine aMachine = aControlJob.getMachine();
            Validations.check(null == aMachine, new OmCode(retCodeConfig.getNotFoundEqp(), "*****"));
            String equipmentCategory = aMachine.getCategory();
            log.info("equipmentCategory : {}", equipmentCategory);
            if (CimStringUtils.equals(BizConstant.SP_MC_CATEGORY_INTERNALBUFFER, equipmentCategory)) {
                ObjectIdentifier equipmentID = new ObjectIdentifier(aMachine.getIdentifier(), aMachine.getPrimaryKey());
                Infos.EqpPortInfo objEquipmentPortInfoGetDROut = equipmentMethod.equipmentPortInfoForInternalBufferGetDR(objCommon, equipmentID);
                List<Infos.ControlJobCassette> controlJobContainedLotOut = controlJobMethod.controlJobContainedLotGet(objCommon, controlJobID);
                int lenPort = CimArrayUtils.getSize(objEquipmentPortInfoGetDROut.getEqpPortStatuses());
                log.info("lenPort : {}", lenPort);
                int lenCJCas = CimArrayUtils.getSize(controlJobContainedLotOut);
                log.info("lenCJCas : {}", lenCJCas);
                for (int i = 0; i < lenCJCas; i++) {
                    Infos.ControlJobCassette controlJobCassette = controlJobContainedLotOut.get(i);
                    log.info("CJ-cassetteID : {}", controlJobCassette.getCassetteID());
                    for (int j = 0; j < lenPort; j++) {
                        Infos.EqpPortStatus eqpPortStatus = objEquipmentPortInfoGetDROut.getEqpPortStatuses().get(j);
                        log.info("portID : {}", eqpPortStatus.getPortID());
                        log.info("loadedCassetteID : {}", eqpPortStatus.getLoadedCassetteID());
                        Validations.check(ObjectIdentifier.equalsWithValue(controlJobCassette.getCassetteID(), eqpPortStatus.getLoadedCassetteID()),
                                new OmCode(retCodeConfig.getNotCassetteOnPort(), controlJobCassette.getCassetteID().getValue(), eqpPortStatus.getPortID().getValue()));
                    }
                }
            }
        }
    }

    @Override
    public Outputs.CassetteDestinationInfoGetOut cassetteDestinationInfoGet(Infos.ObjCommon objCommon, ObjectIdentifier cassetteID, ObjectIdentifier lotID) {
        Outputs.CassetteDestinationInfoGetOut cassetteDestinationInfoGetOut = new Outputs.CassetteDestinationInfoGetOut();
        Results.WhereNextStockerInqResult destinationOrder = new Results.WhereNextStockerInqResult();
        cassetteDestinationInfoGetOut.setDestinationOrder(destinationOrder);
        ObjectIdentifier lotCastID = null;
        Outputs.ObjCassetteLotListGetWithPriorityOrderOut cassetteLotListGetWithPriorityOrderOut = new Outputs.ObjCassetteLotListGetWithPriorityOrderOut();
        if (CimObjectUtils.isEmpty(ObjectIdentifier.fetchValue(lotID))) {
            Validations.check(ObjectIdentifier.isEmptyWithValue(cassetteID), retCodeConfig.getInvalidInputParam());
            /* *****************************************/
            /*  Get Lot priority order in cassette    */
            /* *****************************************/
            log.info("Step1 - cassette_lotList_GetWithPriorityOrder(Get Lot priority order in cassette)");
            try {
                cassetteLotListGetWithPriorityOrderOut = this.cassetteLotListGetWithPriorityOrder(objCommon, cassetteID);
            } catch (ServiceException e) {
                if (Validations.isEquals(retCodeConfig.getCastIsEmpty(), e.getCode())) {
                    boolean bStayOnPort = true;
                    String envStayOnPort = StandardProperties.OM_XFER_STAY_ON_PORT_WITH_NO_DESTINATION.getValue();
                    if (CimStringUtils.equals(envStayOnPort, "0")) {
                        bStayOnPort = false;
                    }
                    if (CimBooleanUtils.isFalse(bStayOnPort)) {
                        /*------------------------------------*/
                        /*   Get Cassette Equipment Info      */
                        /*------------------------------------*/
                        log.info("Step2 - cassette_equipmentID_Get");
                        Outputs.ObjCassetteEquipmentIDGetOut cassetteEquipmentIDResult = this.cassetteEquipmentIDGet(objCommon, cassetteID);
                        if (!ObjectIdentifier.isEmptyWithValue(cassetteEquipmentIDResult.getEquipmentID())) {
                            /*---------------------------------------------------------*/
                            /*   Get Cassette Equipment related stocker information    */
                            /*---------------------------------------------------------*/
                            log.info("Step3 - equipment_stockerInfo_Get");
                            Infos.EqpStockerInfo equipmentStockerInfo = equipmentMethod.equipmentStockerInfoGetDR(objCommon, cassetteEquipmentIDResult.getEquipmentID());
                            List<Infos.EqpStockerStatus> tmpEqpStockerStatus = new ArrayList<>();
                            List<Infos.EqpStockerStatus> eqpStockerStatusList = equipmentStockerInfo.getEqpStockerStatusList();
                            if (!CimObjectUtils.isEmpty(eqpStockerStatusList)) {
                                for (Infos.EqpStockerStatus eqpStockerStatus : eqpStockerStatusList) {
                                    if (!CimObjectUtils.isEmpty(eqpStockerStatus.getStockerID().getValue())) {
                                        if (!CimStringUtils.equals(eqpStockerStatus.getStockerType(), BizConstant.SP_STOCKER_TYPE_AUTO)) {
                                            continue;
                                        }
                                        tmpEqpStockerStatus.add(eqpStockerStatus);
                                    }
                                }
                            }
                            List<Infos.WhereNextEqpStatus> whereNextEqpStatusList = new ArrayList<>();
                            destinationOrder.setWhereNextEqpStatus(whereNextEqpStatusList);
                            if (!CimObjectUtils.isEmpty(tmpEqpStockerStatus)) {
                                Infos.WhereNextEqpStatus whereNextEqpStatus = new Infos.WhereNextEqpStatus();
                                whereNextEqpStatus.setEqpStockerStatus(tmpEqpStockerStatus);
                                whereNextEqpStatusList.add(whereNextEqpStatus);
                            }
                        }
                    }
                    return cassetteDestinationInfoGetOut;
                } else {
                    throw e;
                }
            }

            /*****************************/
            /* Set input Cassette ID     */
            /*****************************/
            lotCastID = cassetteID;

        } else {
            /********************/
            /*  Set lot count   */
            /********************/
            cassetteLotListGetWithPriorityOrderOut.setWaitingLotCount(1);
            cassetteLotListGetWithPriorityOrderOut.setHoldLotCount(0);
            cassetteLotListGetWithPriorityOrderOut.setBankInLotCount(0);
            /********************************/
            /*  Set specified Lot status    */
            /********************************/
            List<Infos.LotStatusInfo> lotStatusInfos = new ArrayList<>();
            cassetteLotListGetWithPriorityOrderOut.setLotStatusInfos(lotStatusInfos);
            Infos.LotStatusInfo lotStatusInfo = new Infos.LotStatusInfo();
            lotStatusInfos.add(lotStatusInfo);
            lotStatusInfo.setLotID(lotID);
            log.info("Step4 - lot_allState_Get");
            Outputs.ObjLotAllStateGetOut lotAllState = lotMethod.lotAllStateGet(objCommon, lotID);
            Infos.LotStatusAttributes lotStatusAttributes = new Infos.LotStatusAttributes();
            lotStatusInfo.setCurrentStatus(lotStatusAttributes);
            lotStatusAttributes.setLotState(lotAllState.getLotState());
            lotStatusAttributes.setProductionState(lotAllState.getProductionState());
            lotStatusAttributes.setHoldState(lotAllState.getHoldState());
            lotStatusAttributes.setFinishedState(lotAllState.getFinishedState());
            lotStatusAttributes.setProcessState(lotAllState.getProcessState());
            lotStatusAttributes.setInventoryState(lotAllState.getInventoryState());
            /* ******************************************/
            /* Set Lot inventory sate and hold state   */
            /* ******************************************/
            CimLot aLot = baseCoreFactory.getBO(CimLot.class, lotID);
            /* *******************************/
            /*   Get lot Base information   */
            /* *******************************/
            ProductDTO.LotBaseInfo aLotBaseInfo = aLot.getLotBaseInfo();
            lotStatusInfo.setRepresentativeState(aLotBaseInfo.getRepresentativeState());
            lotStatusInfo.setProductID(aLotBaseInfo.getProductSpecificationID());
            lotStatusInfo.setOnFloorFlag(aLot.isOnFloor());
            lotStatusInfo.setOnHoldFlag(aLot.isOnHold());
            /* ****************************/
            /* Set Cassette ID of Lot    */
            /* ****************************/
            log.info("Step5 - lot_cassetteList_GetDR");
            lotCastID = lotMethod.lotCassetteListGetDR(objCommon, cassetteLotListGetWithPriorityOrderOut.getLotStatusInfos().get(0).getLotID());
            Validations.check(ObjectIdentifier.isEmptyWithValue(lotCastID), retCodeConfig.getNotFoundCst());
        }
        List<Infos.WhereNextEqpStatus> priorEqpList = new ArrayList<>();
        Infos.LotStatusInfo priorLot = new Infos.LotStatusInfo();
        Validations.check(CimObjectUtils.isEmpty(cassetteLotListGetWithPriorityOrderOut.getLotStatusInfos()), retCodeConfig.getInvalidDataContents());
        /* ***************************************/
        /*  Get cassette detail information     */
        /* ***************************************/
        log.info("Step6 - cassette_LocationInfo_GetDR");
        Infos.LotLocationInfo lotLocationInfo = this.cassetteLocationInfoGetDR(lotCastID);
        /* **************************************************************/
        /*  Check and get cassette(or lot) destination information.    */
        /* **************************************************************/
        if (cassetteLotListGetWithPriorityOrderOut.getWaitingLotCount() > 0) {
            /* **********************/
            /*  Search prior Lot   */
            /* **********************/
            for (int i = 0; i < cassetteLotListGetWithPriorityOrderOut.getWaitingLotCount(); i++) {
                /* *****************************/
                /*  Get Lot equipment order   */
                /* *****************************/
                log.info("Step7 - lot_equipmentOrder_GetByLotStatus");
                Outputs.ObjLotEquipmentOrderGetByLotStatusOut objLotEquipmentOrderGetByLotStatusOutRetCode = lotMethod.lotEquipmentOrderGetByLotStatus(objCommon, lotLocationInfo, cassetteLotListGetWithPriorityOrderOut.getLotStatusInfos().get(i));
                /* *************************************************************************/
                /*  In any case, first lot in the cassete should be set as prior Lot.     */
                /* *************************************************************************/
                if (i == 0) {
                    priorLot = cassetteLotListGetWithPriorityOrderOut.getLotStatusInfos().get(i);
                    if (!CimObjectUtils.isEmpty(objLotEquipmentOrderGetByLotStatusOutRetCode.getWhereNextEqpStatusList())) {
                        priorEqpList = objLotEquipmentOrderGetByLotStatusOutRetCode.getWhereNextEqpStatusList();
                    }
                }
                /* *********************************************************************************/
                /*  RC_OK and "availableEqpExist" means...                                        */
                /*  1. The Lot is wip on the available Equipment.                                 */
                /*  2. Or the Lot does not have machine list. (It does not mean equipment down)   */
                /* *********************************************************************************/
                if (CimBooleanUtils.isTrue(objLotEquipmentOrderGetByLotStatusOutRetCode.isAvailableEqpExistFlag())) {
                    /* ****************************************************************/
                    /*  If lotCnt == 0, the equipment information has already set.   */
                    /* ****************************************************************/
                    if (i != 0) {
                        priorLot = cassetteLotListGetWithPriorityOrderOut.getLotStatusInfos().get(i);
                        priorEqpList = objLotEquipmentOrderGetByLotStatusOutRetCode.getWhereNextEqpStatusList();
                    }
                    break;
                }
            }
        }
        /* *************************************************/
        /*  Only Hold or BankIn Lot exists in Cassette.   */
        /*  Always set first Lot                          */
        /* *************************************************/
        else {
            /* *****************************/
            /*  Get Lot equipment order   */
            /* *****************************/
            log.info("Step8 - lot_equipmentOrder_GetByLotStatus");
            Outputs.ObjLotEquipmentOrderGetByLotStatusOut objLotEquipmentOrderGetByLotStatusOutRetCode = lotMethod.lotEquipmentOrderGetByLotStatus(objCommon, lotLocationInfo, cassetteLotListGetWithPriorityOrderOut.getLotStatusInfos().get(0));
            priorLot = cassetteLotListGetWithPriorityOrderOut.getLotStatusInfos().get(0);
            priorEqpList = objLotEquipmentOrderGetByLotStatusOutRetCode.getWhereNextEqpStatusList();
        }
        /* ****************************************************/
        /*  At here, cassette represent Lot is decided.      */
        /*  Then, if the Lot is in Process, return error!    */
        /* ****************************************************/
        Validations.check(CimStringUtils.equals(BizConstant.SP_LOT_PROCSTATE_PROCESSING, priorLot.getCurrentStatus().getProcessState()), retCodeConfig.getInvalidLotProcstat(), ObjectIdentifier.fetchValue(priorLot.getLotID()), priorLot.getCurrentStatus().getProcessState());
        /* *****************************/
        /*  Get equipment stockers.   */
        /* *****************************/
        log.info("Step9 - equipment_stockerOrder_GetByLotStatus(Get equipment stockers)");
        List<Infos.WhereNextEqpStatus> objEquipmentStockerOrderGetByLotStatusOutRetCode = equipmentMethod.equipmentStockerOrderGetByLotStatus(objCommon, lotLocationInfo, priorLot, priorEqpList);
        /* *********************************/
        /*  Set destination information   */
        /* *********************************/
        destinationOrder.setWhereNextEqpStatus(objEquipmentStockerOrderGetByLotStatusOutRetCode);
        /* **************************/
        /*  Set return structure   */
        /* **************************/
        /* **************************/
        /*  Set Lot information    */
        /* **************************/
        destinationOrder.setLotID(priorLot.getLotID());
        destinationOrder.setLotState(priorLot.getCurrentStatus().getLotState());
        destinationOrder.setProductionState(priorLot.getCurrentStatus().getProductionState());
        destinationOrder.setHoldState(priorLot.getCurrentStatus().getHoldState());
        destinationOrder.setFinishedState(priorLot.getCurrentStatus().getFinishedState());
        destinationOrder.setProcessState(priorLot.getCurrentStatus().getProcessState());
        destinationOrder.setInventoryState(priorLot.getCurrentStatus().getInventoryState());

        /* *******************************/
        /*  Set cassette information    */
        /* *******************************/
        destinationOrder.setCassetteID(lotLocationInfo.getCassetteID());
        destinationOrder.setTransferStatus(lotLocationInfo.getTransferStatus());
        if (CimStringUtils.equals(destinationOrder.getTransferStatus(), BizConstant.SP_TRANSSTATE_EQUIPMENTIN)
                || CimStringUtils.equals(destinationOrder.getTransferStatus(), BizConstant.SP_TRANSSTATE_EQUIPMENTOUT)) {
            destinationOrder.setCurrent_equipmentID(lotLocationInfo.getEquipmentID());
        } else {
            destinationOrder.setCurrent_stockerID(lotLocationInfo.getStockerID());
        }
        destinationOrder.setTransferReserveUserID(new ObjectIdentifier(lotLocationInfo.getTransferReserveUserID()));
        return cassetteDestinationInfoGetOut;
    }

    @Override
    public Outputs.CassetteDestinationInfoGetForSLMOut cassetteDestinationInfoGetForSLM(Infos.ObjCommon objCommon, ObjectIdentifier cassetteID, ObjectIdentifier lotID) {
        Outputs.CassetteDestinationInfoGetForSLMOut cassetteDestinationInfoGetForSLMOut = new Outputs.CassetteDestinationInfoGetForSLMOut();
        //------------------------------------------------------
        // Decide CassetteID
        //------------------------------------------------------
        if (CimObjectUtils.isEmpty(ObjectIdentifier.fetchValue(lotID))) {
            Validations.check(ObjectIdentifier.isEmptyWithValue(cassetteID), retCodeConfig.getInvalidInputParam());
        } else {
            cassetteID = lotMethod.lotCassetteGet(objCommon, lotID);
        }
        log.info("cassetteID : {}", ObjectIdentifier.fetchValue(cassetteID));
        //------------------------------------------------------
        // Get SLM reserved equipment of cassette.
        //------------------------------------------------------
        log.info("Get SLM reserved equipment of cassette.");
        CimCassette aCassette = baseCoreFactory.getBO(CimCassette.class, cassetteID);
        Validations.check(null == aCassette, retCodeConfig.getNotFoundCassette());
        CimMachine aSLMReservedMachine = aCassette.getSLMReservedMachine();
        Validations.check(null == aSLMReservedMachine, retCodeConfig.getNotReservedCastSLM(), ObjectIdentifier.fetchValue(cassetteID));
        //------------------------------------------------------
        // Get UTS/SLMUTS stocker which can be used.
        //------------------------------------------------------
        List<CimStorageMachine> aUTSStockerSeq = aSLMReservedMachine.getSLMUnderTrackStorages();
        log.info("lenUTSStocker (SLMStk) : {}", CimArrayUtils.getSize(aUTSStockerSeq));
        List<Infos.EqpStockerStatus> strUTSStocker = new ArrayList<>();
        List<Infos.EqpStockerStatus> strSLMUTSStocker = new ArrayList<>();

        int nSLMUTSCnt = 0;
        int nUTSCnt = 0;
        int nAutoCnt = 0;
        for (int i = 0; i < CimArrayUtils.getSize(aUTSStockerSeq); i++) {
            Infos.EqpStockerStatus tmpStocker = new Infos.EqpStockerStatus();
            if (!CimObjectUtils.isEmpty(aUTSStockerSeq.get(i))) {
                tmpStocker.setStockerID(ObjectIdentifier.build(aUTSStockerSeq.get(i).getIdentifier(), aUTSStockerSeq.get(i).getPrimaryKey()));
            } else {
                tmpStocker.setStockerID(ObjectIdentifier.build("", ""));
            }
            log.info("stockerID: {}", ObjectIdentifier.fetchValue(tmpStocker.getStockerID()));

            // Get Stocker Type
            String stockerType = aUTSStockerSeq.get(i).getStockerType();
            log.info("stockerType: {}", stockerType);

            tmpStocker.setStockerType(stockerType);
            tmpStocker.setMaxOHBFlag(0L);

            //Get Avalible State
            Boolean bAvalible = false;
            bAvalible = aUTSStockerSeq.get(i).isAvailableState();
            log.info("bAvalible: {}", CimBooleanUtils.convertBooleanToLong(bAvalible));

            if (CimBooleanUtils.isFalse(bAvalible)) {
                continue;
            }

            Boolean bUTSFlag = false;
            bUTSFlag = aUTSStockerSeq.get(i).isUTSStocker();
            log.info("bUTSFlag: {}", CimBooleanUtils.convertBooleanToLong(bUTSFlag));

            Boolean bSLMUTSFlag = false;
            bSLMUTSFlag = aUTSStockerSeq.get(i).isSLMUTSFlagOn();
            log.info("bAvailable: {}", CimBooleanUtils.convertBooleanToLong(bSLMUTSFlag));

            // Get Vacant Space
            log.info("call stockerUTSVacantSpaceCheckDR()");
            Outputs.ObjStockerUTSVacantSpaceCheckDROut objStockerUTSVacantSpaceCheckDROutRetCode = stockerComp.stockerUTSVacantSpaceCheckDR(objCommon, tmpStocker.getStockerID());
            log.info("vacantSpace: {}", objStockerUTSVacantSpaceCheckDROutRetCode.getVacantSpace());
            if (objStockerUTSVacantSpaceCheckDROutRetCode.getVacantSpace() == 0) {
                log.info("vacantSpace is 0");
                continue;
            }

            tmpStocker.setOhbFlag(true);

            if (CimBooleanUtils.isTrue(bUTSFlag)) {
                strUTSStocker.add(nUTSCnt, tmpStocker);
                nUTSCnt++;
            } else if (CimBooleanUtils.isTrue(bSLMUTSFlag)) {
                strSLMUTSStocker.add(nSLMUTSCnt, tmpStocker);
                nSLMUTSCnt++;
            }
        }
        if (0 < nUTSCnt || 0 < nSLMUTSCnt) {
            cassetteDestinationInfoGetForSLMOut.setDestinationOrder(new Results.WhereNextStockerInqResult());
            cassetteDestinationInfoGetForSLMOut.getDestinationOrder().setWhereNextEqpStatus(new ArrayList<>());
            Infos.WhereNextEqpStatus whereNextEqpStatus = new Infos.WhereNextEqpStatus();
            cassetteDestinationInfoGetForSLMOut.getDestinationOrder().getWhereNextEqpStatus().add(whereNextEqpStatus);
            if (0 < nSLMUTSCnt) {
                whereNextEqpStatus.setEqpStockerStatus(strSLMUTSStocker);
                log.info("Return SLM-UTS Stocker");
            } else if (0 < nUTSCnt) {
                log.info("Return UTS Stocker");
                whereNextEqpStatus.setEqpStockerStatus(strUTSStocker);
            }
        } else {
            //Get Auto stocker which can be used.
            log.info("Get Auto stocker which can be used.");
            List<CimStorageMachine> aStockerSeq = aSLMReservedMachine.getStorageMachines();
            log.info("lenStocker: {}", CimArrayUtils.getSize(aStockerSeq));

            List<Infos.EqpStockerStatus> strAutoStocker = new ArrayList<>();
            for (int i = 0; i < CimArrayUtils.getSize(aStockerSeq); i++) {
                Infos.EqpStockerStatus tmpStocker = new Infos.EqpStockerStatus();
                if (!CimObjectUtils.isEmpty(aStockerSeq.get(i))) {
                    tmpStocker.setStockerID(ObjectIdentifier.build(aStockerSeq.get(i).getIdentifier(), aStockerSeq.get(i).getPrimaryKey()));
                } else {
                    tmpStocker.setStockerID(ObjectIdentifier.build("", ""));
                }
                log.info("stockerID: {}", ObjectIdentifier.fetchValue(tmpStocker.getStockerID()));

                // Get Stocker Type
                String stockerType = aStockerSeq.get(i).getStockerType();
                log.info("stockerType: {}", stockerType);

                if (!CimStringUtils.equals(BizConstant.SP_STOCKER_TYPE_AUTO, stockerType)) {
                    continue;
                }

                tmpStocker.setStockerType(stockerType);
                tmpStocker.setMaxOHBFlag(0L);
                tmpStocker.setOhbFlag(false);

                // Get Available State
                Boolean bAvalible = false;
                bAvalible = aStockerSeq.get(i).isAvailableState();
                log.info("bAvailable: {}", CimBooleanUtils.convertBooleanToLong(bAvalible));

                if (CimBooleanUtils.isFalse(bAvalible)) {
                    continue;
                }

                strAutoStocker.add(nAutoCnt, tmpStocker);
                nAutoCnt++;
            }
            if (0 < nAutoCnt) {
                cassetteDestinationInfoGetForSLMOut.setDestinationOrder(new Results.WhereNextStockerInqResult());
                cassetteDestinationInfoGetForSLMOut.getDestinationOrder().setWhereNextEqpStatus(new ArrayList<>());
                Infos.WhereNextEqpStatus whereNextEqpStatus = new Infos.WhereNextEqpStatus();
                whereNextEqpStatus.setEqpStockerStatus(strAutoStocker);
                cassetteDestinationInfoGetForSLMOut.getDestinationOrder().getWhereNextEqpStatus().add(whereNextEqpStatus);
            } else {
                log.info("Return No Stocker");
            }
        }
        return cassetteDestinationInfoGetForSLMOut;
    }

    @Override
    public void cassetteCheckConditionForWaferSort(Infos.ObjCommon objCommon, List<Infos.WaferTransfer> waferXferList, ObjectIdentifier equipmentID) {
        //--------------------------------------------------
        // Collect cassette ID of input parameter
        //--------------------------------------------------
        List<ObjectIdentifier> cassetteIDSeq = new ArrayList<>();
        List<Boolean> bCassetteManagedBySiViewSeq = new ArrayList<>();
        boolean bCassetteAdded = false;
        int lenWaferXferSeq = CimArrayUtils.getSize(waferXferList);
        int lenCasIDSeq = 0;
        for (int i = 0; i < lenWaferXferSeq; i++) {
            bCassetteAdded = false;
            lenCasIDSeq = CimArrayUtils.getSize(cassetteIDSeq);
            for (int j = 0; j < lenCasIDSeq; j++) {
                if (ObjectIdentifier.equalsWithValue(waferXferList.get(i).getDestinationCassetteID(), cassetteIDSeq.get(j))) {
                    bCassetteAdded = true;
                    break;
                }
            }
            if (CimBooleanUtils.isFalse(bCassetteAdded)) {
                cassetteIDSeq.add(waferXferList.get(i).getDestinationCassetteID());
                bCassetteManagedBySiViewSeq.add(waferXferList.get(i).getBDestinationCassetteManagedByOM());
            }
            bCassetteAdded = false;
            lenCasIDSeq = CimArrayUtils.getSize(cassetteIDSeq);
            for (int j = 0; j < lenCasIDSeq; j++) {
                if (ObjectIdentifier.equalsWithValue(waferXferList.get(i).getOriginalCassetteID(), cassetteIDSeq.get(j))) {
                    bCassetteAdded = true;
                    break;
                }
            }
            if (CimBooleanUtils.isFalse(bCassetteAdded)) {
                cassetteIDSeq.add(waferXferList.get(i).getOriginalCassetteID());
                bCassetteManagedBySiViewSeq.add(waferXferList.get(i).getBOriginalCassetteManagedByOM());
            }
        }
        //--------------------------------------------------
        // Collect cassette's equipment ID / Cassette ObjRef
        //--------------------------------------------------
        lenCasIDSeq = CimArrayUtils.getSize(cassetteIDSeq);
        List<com.fa.cim.newcore.bo.durable.CimCassette> aPosCassetteSeq = new ArrayList<>();
        List<String> strLoadedEquipments = new ArrayList<>();
        int LoadedEqpCnt = 0;
        for (int i = 0; i < lenCasIDSeq; i++) {
            boolean cassetteFound = false;
            com.fa.cim.newcore.bo.durable.CimCassette aPosCassette = baseCoreFactory.getBO(com.fa.cim.newcore.bo.durable.CimCassette.class, cassetteIDSeq.get(i));
            if (null != aPosCassette) {
                cassetteFound = true;
            }
            if (CimBooleanUtils.isFalse(cassetteFound)) {
                continue;
            }
            String tmpCassetteTransportState = aPosCassette.getTransportState();
            Machine tmpMachine = aPosCassette.currentAssignedMachine();
            LoadedEqpCnt++;
            aPosCassetteSeq.add(aPosCassette);
            if (null != tmpMachine && CimStringUtils.equals(tmpCassetteTransportState, BizConstant.SP_TRANSSTATE_EQUIPMENTIN)) {
                String strLoadedEquipment = tmpMachine.getIdentifier();
                strLoadedEquipments.add(strLoadedEquipment);
            }
        }
        //-----------------------------
        //  Check InPostProcessFlag
        //-----------------------------
        for (int i = 0; i < lenCasIDSeq; i++) {
            if (bCassetteManagedBySiViewSeq.get(i)) {
                //---------------------------------------
                //  Get InPostProcessFlag of Cassette
                //---------------------------------------
                Boolean strCassettInPostProcessFlagGetOut = this.cassetteInPostProcessFlagGet(objCommon, cassetteIDSeq.get(i));

                String strCassetteInterFabXferStateGetOut = this.cassetteInterFabXferStateGet(objCommon, cassetteIDSeq.get(i));
                Validations.check(CimStringUtils.equals(BizConstant.SP_INTERFAB_XFERSTATE_TRANSFERRING, strCassetteInterFabXferStateGetOut), retCodeConfig.getInterfabInvalidCassetteTransferStateForRequest());
                Infos.LotListInCassetteInfo strCassetteLotListGetDROut = this.cassetteLotIDListGetDR(objCommon, cassetteIDSeq.get(i));
                int lotLen = CimArrayUtils.getSize(strCassetteLotListGetDROut.getLotIDList());
                for (int j = 0; j < lotLen; j++) {
                    String lotInterFabXferStateResultOut = lotMethod.lotInterFabXferStateGet(objCommon, strCassetteLotListGetDROut.getLotIDList().get(j));
                    if (CimStringUtils.equals(BizConstant.SP_INTERFAB_XFERSTATE_ORIGINDELETING, lotInterFabXferStateResultOut)
                            || CimStringUtils.equals(BizConstant.SP_INTERFAB_XFERSTATE_TRANSFERRING, lotInterFabXferStateResultOut)) {
                        throw new ServiceException(retCodeConfig.getInterfabInvalidCassetteTransferStateForRequest());
                    }
                }
                //---------------------------------------------------
                //  If Cassette is in post process, returns error
                //---------------------------------------------------
                Validations.check(CimBooleanUtils.isTrue(strCassettInPostProcessFlagGetOut), retCodeConfig.getCassetteInPostProcess());

                com.fa.cim.newcore.bo.durable.CimCassette aCassette = baseCoreFactory.getBO(com.fa.cim.newcore.bo.durable.CimCassette.class, cassetteIDSeq.get(i));
                Machine aSLMReservedMachine = aCassette.getSLMReservedMachine();
                Validations.check(null != aSLMReservedMachine, retCodeConfig.getAlreadyReservedCassetteSlm(), aCassette.getIdentifier());
            }
        }
        //--------------------------------------------------
        // Check all cassette is on the same equipment
        //--------------------------------------------------
        String tmpLoadedEquipment = null;
        int lenLoadedEqp = CimArrayUtils.getSize(strLoadedEquipments);
        for (int i = 0; i < lenLoadedEqp; i++) {
            if (0 == i) {
                tmpLoadedEquipment = strLoadedEquipments.get(0);
            } else {
                Validations.check(!CimStringUtils.equals(tmpLoadedEquipment, strLoadedEquipments.get(i)), retCodeConfig.getEquipmentOfCassetteNotSame());
            }
        }
        //--------------------------------------------------
        // Check equipment of cassette and input equipment is same or not
        //--------------------------------------------------
        Validations.check(!ObjectIdentifier.equalsWithValue(equipmentID, tmpLoadedEquipment), retCodeConfig.getCassetteEquipmentDifferent());
        //--------------------------------------------------
        // If equipmetnID is not input, return OK
        //--------------------------------------------------
        if (ObjectIdentifier.isEmpty(equipmentID) && CimStringUtils.isEmpty(tmpLoadedEquipment)) {
            //ok
            return;
        }

        //--------------------------------------------------
        // Get object reference of PosMachine
        //--------------------------------------------------
        CimMachine aPosMachine = baseCoreFactory.getBO(CimMachine.class, equipmentID);
        //--------------------------------------------------
        // Get Equipment attributes
        //--------------------------------------------------
        String strMachineCategory = aPosMachine.getCategory();

        //--------------------------------------------------
        // Check Equipment Type
        //--------------------------------------------------
        Validations.check(!CimStringUtils.equals(BizConstant.SP_MC_CATEGORY_WAFERSORTER, strMachineCategory), retCodeConfig.getMachineTypeNotSorter());

        //--------------------------------------------------
        // Check Control Job Info
        //--------------------------------------------------
        String strCassetteControlJob = null;
        String tmpCassetteControlJob = null;

        int lenCasSeq = CimArrayUtils.getSize(aPosCassetteSeq);
        for (int i = 0; i < lenCasSeq; i++) {
            com.fa.cim.newcore.bo.product.CimControlJob aPosCtrlJob = aPosCassetteSeq.get(i).getControlJob();
//            com.fa.cim.newcore.bo.product.CimControlJob aPosCtrlJob = null;
//            if (0 == i) {
//                aPosCtrlJob = aPosCassetteSeq.get(i).getControlJob();
//
//                //Validations.check(null == aPosCtrlJob, retCodeConfig.getNotSameControlJobId());
//                //strCassetteControlJob = aPosCtrlJob.getIdentifier();
//
//            } else {
//                aPosCtrlJob = aPosCassetteSeq.get(i).getControlJob();
//                //Validations.check(null == aPosCtrlJob, retCodeConfig.getNotSameControlJobId());
//
//                //tmpCassetteControlJob = aPosCtrlJob.getIdentifier();
//                //Validations.check(StringUtils.equals(strCassetteControlJob, tmpCassetteControlJob), retCodeConfig.getNotSameControlJobId());
//
//            }
        }
    }


    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/6/13                               Neyo                create file
     *
     * @author Neyo
     * @since 2019/6/13 17:56
     * Copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Override
    public void cassetteInPostProcessFlagSet(Infos.ObjCommon objCommon, Inputs.ObjCassetteInPostProcessFlagSetIn in) {
        //----------------
        //  Initialize
        //----------------
        //---------------------------
        //  Check input parameter
        //---------------------------
        Validations.check(ObjectIdentifier.isEmptyWithValue(in.getLotID()) && ObjectIdentifier.isEmptyWithValue(in.getCassetteID()), retCodeConfig.getInvalidInputParam());
        com.fa.cim.newcore.bo.durable.CimCassette aCassette = null;
        if (!ObjectIdentifier.isEmptyWithValue(in.getCassetteID())) {
            aCassette = baseCoreFactory.getBO(com.fa.cim.newcore.bo.durable.CimCassette.class, in.getCassetteID());
            Validations.check(aCassette == null, new OmCode(retCodeConfig.getNotFoundCassette(), in.getCassetteID().getValue()));
        } else {
            //---------------------------------
            //  Convert lotID to lot object
            //---------------------------------
            com.fa.cim.newcore.bo.product.CimLot aLot = baseCoreFactory.getBO(com.fa.cim.newcore.bo.product.CimLot.class, in.getLotID());
            Validations.check(aLot == null, new OmCode(retCodeConfig.getNotFoundLot(), in.getLotID().getValue()));
            List<MaterialContainer> aMaterialContainerSeq = aLot.materialContainers();
            if (!CimArrayUtils.isEmpty(aMaterialContainerSeq)) {
                log.info("Lot is related to Cassette.");
                aCassette = (com.fa.cim.newcore.bo.durable.CimCassette) aMaterialContainerSeq.get(0);
            }
        }
        if (aCassette == null) {
            //result.setReturnCode(retCodeConfig.getSucc());
            return;
        }
        if (in.isInPostProcessFlag()) {
            //---------------------------------------------
            //  Set InPostProcessFlag of Cassette to ON
            //---------------------------------------------
            aCassette.makePostProcessFlagOn();
        } else {
            //----------------------------------------------
            //  Set InPostProcessFlag of Cassette to OFF
            //----------------------------------------------
            aCassette.makePostProcessFlagOff();
        }
    }

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/7/3                               Neyo                create file
     *
     * @author Neyo
     * @since 2019/7/3 16:02
     * Copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Override
    public Outputs.ObjMultiCarrierXferFillInOTMSW005InParmOut multiCarrierXferFillInOTMSW005InParm(Infos.ObjCommon objCommonIn, ObjectIdentifier equipmentID, List<Infos.StartCassette> startCassetteList) {
        //Initialize
        Outputs.ObjMultiCarrierXferFillInOTMSW005InParmOut out = new Outputs.ObjMultiCarrierXferFillInOTMSW005InParmOut();
        List<Infos.CarrierXferReq> strCarrierXferReq = new ArrayList<>();
        int nLen = CimArrayUtils.getSize(startCassetteList);
        for (int i = 0; i < nLen; i++) {
            Infos.CarrierXferReq carrierXferReq = new Infos.CarrierXferReq();
            strCarrierXferReq.add(carrierXferReq);
            carrierXferReq.setN2PurgeFlag(false);
            carrierXferReq.setMandatoryFlag(false);
            log.info("fromMachineID: Get Current Stocker Object By Assigned Cassette Object");
            //fromMachineID: Get Current Stocker Object By Assigned Cassette Object
            CimCassette aCassette = baseCoreFactory.getBO(CimCassette.class, startCassetteList.get(i).getCassetteID());
            Machine aMachine = aCassette.currentAssignedMachine();
            String xferStat = aCassette.getTransportState();

            Validations.check(null == aMachine, new OmCode(retCodeConfig.getInvalidCassetteTransferState(), startCassetteList.get(i).getCassetteID().getValue()));
            if (!CimStringUtils.equals(xferStat, BizConstant.SP_TRANSSTATE_EQUIPMENTOUT)) {
                String identifier = aMachine.getIdentifier();
                strCarrierXferReq.get(i).setFromMachineID(ObjectIdentifier.build(identifier, ""));
                Boolean bStorageFlag = aMachine.isStorageMachine();
                if (CimBooleanUtils.isFalse(bStorageFlag)) {
                    CimMachine aEquipment = (CimMachine) aMachine;
                    String equCategory = aEquipment.getCategory();
                    Infos.EqpPortInfo strEqpPortInfo = new Infos.EqpPortInfo();
                    if (CimStringUtils.equals(equCategory, BizConstant.SP_MC_CATEGORY_INTERNALBUFFER)) {
                        log.info("call equipment_portInfoForInternalBuffer_GetDR()");
                        //【step1】 - equipment_portInfoForInternalBuffer_GetDR
                        strEqpPortInfo = equipmentMethod.equipmentPortInfoForInternalBufferGetDR(objCommonIn, strCarrierXferReq.get(i).getFromMachineID());
                    } else {
                        log.info("call equipment_portInfo_GetDR()");
                        //【step2】 - equipment_portInfo_GetDR
                        strEqpPortInfo = equipmentMethod.equipmentPortInfoGet(objCommonIn, strCarrierXferReq.get(i).getFromMachineID());
                    }
                    int lenPort = CimArrayUtils.getSize(strEqpPortInfo.getEqpPortStatuses());
                    for (int j = 0; j < lenPort; j++) {
                        if (ObjectIdentifier.equalsWithValue(strEqpPortInfo.getEqpPortStatuses().get(j).getLoadedCassetteID(),
                                startCassetteList.get(i).getCassetteID())) {
                            strCarrierXferReq.get(i).setFromPortID(strEqpPortInfo.getEqpPortStatuses().get(j).getPortID());
                            break;
                        }
                    }

                    Validations.check(ObjectIdentifier.isEmpty(strCarrierXferReq.get(i).getFromPortID()) || ObjectIdentifier.isEmptyWithValue(strCarrierXferReq.get(i).getFromPortID())
                            , retCodeConfig.getNotFoundPort());
                }
            }
            log.info("set carrierID");
            //carrierID
            strCarrierXferReq.get(i).setCarrierID(startCassetteList.get(i).getCassetteID());
            log.info("zoneType: Get By Cassette");
            //zoneType: Get By Cassette
            //【step3】 - cassette_zoneType_Get
            Outputs.CassetteZoneTypeGetOut cassetteZoneTypeGetOutRetCode = this.cassetteZoneTypeGet(objCommonIn, startCassetteList.get(i).getCassetteID());
            strCarrierXferReq.get(i).setZoneType(cassetteZoneTypeGetOutRetCode.getZoneType());
            strCarrierXferReq.get(i).setPriority(cassetteZoneTypeGetOutRetCode.getPriority());
            log.info("strToMachine.toPortID: Set Load PortID");
            //strToMachine.toPortID: Set Load PortID
            List<Infos.ToMachine> strToMachine = new ArrayList<>();
            strCarrierXferReq.get(i).setStrToMachine(strToMachine);
            Infos.ToMachine toMachine = new Infos.ToMachine();
            strToMachine.add(toMachine);
            toMachine.setToMachineID(equipmentID);
            toMachine.setToPortID(startCassetteList.get(i).getLoadPortID());
            log.info("expectedEndTime: Set 1 Year After");
            //expectedEndTime: Set 1 Year After
            Timestamp currentTime = new Timestamp(System.currentTimeMillis());
            long time = currentTime.getTime();
            Calendar ca = Calendar.getInstance();
            ca.add(Calendar.YEAR, 1);
            //ca.add(Calendar.MONTH,1);
            Timestamp scTime = new Timestamp(ca.get(Calendar.YEAR) - 1900,
                    ca.get(Calendar.MONTH),
                    ca.get(Calendar.DATE),
                    ca.get(Calendar.HOUR),
                    ca.get(Calendar.MINUTE),
                    ca.get(Calendar.SECOND), 0);
            strCarrierXferReq.get(i).setExpectedEndTime(scTime.toLocalDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
        out.setStrCarrierXferReq(strCarrierXferReq);
        return out;
    }

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/7/4                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2019/7/4 18:17
     * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Override
    public Outputs.ObjCassetteDeliveryRTDInterfaceReqOut cassetteDeliveryRTDInterfaceReq(Infos.ObjCommon objCommonIn, String kind, ObjectIdentifier keyID) {
        // TODO: 2019/11/4
        Outputs.ObjCassetteDeliveryRTDInterfaceReqOut out = null;
        //return out;
        //test throw not found rtd
        throw new ServiceException(retCodeConfigEx.getNotFoundRTD());
    }

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/7/5                               Neyo                create file
     *
     * @author Neyo
     * @since 2019/7/5 13:34
     * Copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Override
    public Outputs.ObjSingleCarrierXferFillInOTMSW006InParmOut singleCarrierXferFillInOTMSW006InParm(Infos.ObjCommon objCommonIn, ObjectIdentifier equipmentID, ObjectIdentifier unLoadPortID, ObjectIdentifier cassetteID, Results.WhereNextStockerInqResult strWhereNextStockerInqResult) {

        /*----------------*/
        /*   Initialize   */
        /*----------------*/
        Outputs.ObjSingleCarrierXferFillInOTMSW006InParmOut out = new Outputs.ObjSingleCarrierXferFillInOTMSW006InParmOut();

        out.setRerouteFlag(false);
        Infos.CarrierXferReq strCarrierXferReq = new Infos.CarrierXferReq();
        out.setStrCarrierXferReq(strCarrierXferReq);
        strCarrierXferReq.setFromMachineID(equipmentID);
        strCarrierXferReq.setFromPortID(unLoadPortID);
        strCarrierXferReq.setCarrierID(cassetteID);
        strCarrierXferReq.setN2PurgeFlag(false);
        strCarrierXferReq.setMandatoryFlag(false);
        log.info("Get Zone Type for Cassette");
        /*--------------------------------*/
        /*   Get Zone Type for Cassette   */
        /*--------------------------------*/
        //【step1】 - cassette_zoneType_Get
        Outputs.CassetteZoneTypeGetOut cassetteZoneTypeGetOutRetCode = this.cassetteZoneTypeGet(objCommonIn, cassetteID);

        strCarrierXferReq.setZoneType(cassetteZoneTypeGetOutRetCode.getZoneType());
        strCarrierXferReq.setPriority(cassetteZoneTypeGetOutRetCode.getPriority());
        log.info("Abstract Stocker List");
        /*---------------------------*/
        /*   Abstract Stocker List   */
        /*---------------------------*/
        int mLen = CimArrayUtils.getSize(strWhereNextStockerInqResult.getWhereNextEqpStatus());
        List<ObjectIdentifier> machines = new ArrayList<>();
        int stkLen = 0;
        for (int j = 0; j < mLen; j++) {
            int nLen = CimArrayUtils.getSize(strWhereNextStockerInqResult.getWhereNextEqpStatus().get(j).getEqpStockerStatus());
            for (int k = 0; k < nLen; k++) {
                if (ObjectIdentifier.isNotEmpty(strWhereNextStockerInqResult.getWhereNextEqpStatus().get(j).getEqpStockerStatus().get(k).getStockerID())) {
                    log.info("strWhereNextStockerInqResult.strWhereNextEqpStatus[j].strEqpStockerStatus[k].stockerID is null");
                    if (!CimStringUtils.equals(BizConstant.SP_STOCKER_TYPE_AUTO, strWhereNextStockerInqResult.getWhereNextEqpStatus().get(j).getEqpStockerStatus().get(k).getStockerType())) {
                        log.info("Not AutoStocker ...<continue>");
                        continue;
                    }
                    stkLen++;
                    machines.add(strWhereNextStockerInqResult.getWhereNextEqpStatus().get(j).getEqpStockerStatus().get(k).getStockerID());
                }
            }
        }
        boolean bStayOnPort = true;
        String envStayOnPort = StandardProperties.OM_XFER_STAY_ON_PORT_WITH_NO_DESTINATION.getValue();
        if (CimStringUtils.equals(envStayOnPort, "0")) {
            bStayOnPort = false;
            log.info("bStayOnPort = FALSE");
        }
        log.info("If Stocker List is Nothing, Get Stocker info for Equipment");
        /*----------------------------------------------------------------*/
        /*   If Stocker List is Nothing, Get Stocker info for Equipment   */
        /*----------------------------------------------------------------*/
        if (stkLen == 0 && CimBooleanUtils.isFalse(bStayOnPort)) {
            log.info("stkLen == 0 && bStayOnPort != TRUE");
            /*-------------------------------*/
            /*   Get Stocker for Equipment   */
            /*-------------------------------*/
            //【step2】 - equipment_stockerInfo_Get
            Infos.EqpStockerInfo eqpStockerInfo = equipmentMethod.equipmentStockerInfoGetDR(objCommonIn, equipmentID);
            int tmpStkLen = CimArrayUtils.getSize(eqpStockerInfo.getEqpStockerStatusList());
            for (int j = 0; j < tmpStkLen; j++) {
                if (!CimStringUtils.equals(BizConstant.SP_STOCKER_TYPE_AUTO, eqpStockerInfo.getEqpStockerStatusList().get(j).getStockerType())) {
                    log.info("Not AutoStocker ...<continue>");
                    continue;
                }
                stkLen++;
                machines.add(eqpStockerInfo.getEqpStockerStatusList().get(j).getStockerID());
            }
            if (stkLen == 0) {
                throw new ServiceException(new OmCode(retCodeConfigEx.getNoStockerForCurrentEqp(), ObjectIdentifier.fetchValue(unLoadPortID), ObjectIdentifier.fetchValue(equipmentID)), out);
            }
        }
        Validations.check((stkLen == 0) && CimBooleanUtils.isTrue(bStayOnPort), new OmCode(retCodeConfigEx.getNoXferNeeded(), ObjectIdentifier.fetchValue(unLoadPortID), ObjectIdentifier.fetchValue(equipmentID)));
        log.info("Cut Duplicate Stocker");
        /*----------------------------*/
        /*   Cut Duplicate Stocker   */
        /*----------------------------*/
        List<ObjectIdentifier> toMachines = new ArrayList<>();
        toMachines = machines;
        List<Infos.ToMachine> strToMachine = new ArrayList<>();
        strCarrierXferReq.setStrToMachine(strToMachine);
        int cnt = 0;
        for (int j = 0; j < stkLen; j++) {
            boolean existFlag = false;
            for (int k = 0; k < j; k++) {
                if (ObjectIdentifier.equalsWithValue(machines.get(j), toMachines.get(k))) {
                    existFlag = true;
                }
            }
            if (CimBooleanUtils.isFalse(existFlag) || j == 0) {
                log.info("existFlag == FALSE || j == 0");
                Infos.ToMachine toMachine = new Infos.ToMachine();
                strToMachine.add(cnt, toMachine);
                toMachine.setToMachineID(machines.get(j));
                cnt++;
            }
        }
        /*--------------------------*/
        /*   Set Output Parameter   */
        /*--------------------------*/
        return out;
    }

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/7/9                               Neyo                create file
     *
     * @author Neyo
     * @since 2019/7/9 14:26
     * Copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Override
    public Outputs.ObjCassetteListEmptyAvailablePickUpOut cassetteListEmptyAvailablePickup(Infos.ObjCommon objCommonIn, List<Infos.FoundCassette> strFoundCassette) {
        //Initialize Flag.
        Outputs.ObjCassetteListEmptyAvailablePickUpOut out = new Outputs.ObjCassetteListEmptyAvailablePickUpOut();
        int i = 0;
        int nOutPutCasLen = 0;
        int nFoundCasLen = CimArrayUtils.getSize(strFoundCassette);
        boolean bEmptyCasFoundFlag = false;
        for (i = 0; i < nFoundCasLen; i++) {
            log.info("Select empty cassette from input cassette sequence.  Round,{}", i);
            //Data Condition Check
            if (CimBooleanUtils.isFalse(strFoundCassette.get(i).getEmptyFlag())) {
                log.info("Input cassette is not empty");
                continue;
            }
            if (!CimStringUtils.equals(BizConstant.CIMFW_DURABLE_AVAILABLE, strFoundCassette.get(i).getCassetteStatus())
                    && !CimStringUtils.equals(BizConstant.CIMFW_DURABLE_INUSE, strFoundCassette.get(i).getCassetteStatus())) {
                log.info("Input cassette is not 'Available' and not InUse.");
                continue;
            }
            String reRouteXferFlag = StandardProperties.OM_XFER_REROUTE_FLAG.getValue();
            if (CimStringUtils.equals(reRouteXferFlag, "1")) {
                log.info("reRouteXferFlag is 1");
                if (!CimStringUtils.equals(BizConstant.SP_TRANSSTATE_STATIONIN, strFoundCassette.get(i).getTransferStatus())
                        && !CimStringUtils.equals(BizConstant.SP_TRANSSTATE_BAYIN, strFoundCassette.get(i).getTransferStatus())
                        && !CimStringUtils.equals(BizConstant.SP_TRANSSTATE_MANUALIN, strFoundCassette.get(i).getTransferStatus())
                        && !CimStringUtils.equals(BizConstant.SP_TRANSSTATE_BAYOUT, strFoundCassette.get(i).getTransferStatus())) {
                    log.info("Input cassette is not [SI] [BI] [MI] [BO]");
                    continue;
                }
            } else {
                if (!CimStringUtils.equals(BizConstant.SP_TRANSSTATE_STATIONIN, strFoundCassette.get(i).getTransferStatus())
                        && !CimStringUtils.equals(BizConstant.SP_TRANSSTATE_BAYIN, strFoundCassette.get(i).getTransferStatus())
                        && !CimStringUtils.equals(BizConstant.SP_TRANSSTATE_MANUALIN, strFoundCassette.get(i).getTransferStatus())) {
                    log.info("Input cassette is not [SI] [BI] [MI]");
                    continue;
                }
            }
            //【step1】 - cassette_controlJobID_Get
            ObjectIdentifier cassetteControlJobIDRetCode = this.cassetteControlJobIDGet(objCommonIn, strFoundCassette.get(i).getCassetteID());
            if (CimStringUtils.length(cassetteControlJobIDRetCode.getValue()) != 0) {
                log.info("Input cassette is not 'controlJobID is nil'");
                continue;
            }
            CimCassette aCassette = baseCoreFactory.getBO(CimCassette.class, strFoundCassette.get(i).getCassetteID());
            //Check XferReserve
            Boolean transferReserved = aCassette.isReserved();
            if (CimBooleanUtils.isTrue(transferReserved)) {
                log.info("Cassette has XferReservation");
                continue;
            }
            //Check NPWReserve
            Boolean dispatchReserveFlag = aCassette.isDispatchReserved();
            if (CimBooleanUtils.isTrue(dispatchReserveFlag)) {
                log.info("Cassette has DispatchReservation");
                continue;
            }
            CimMachine aSLMReservedMachine = aCassette.getSLMReservedMachine();
            if (!CimObjectUtils.isEmpty(aSLMReservedMachine)) {
                log.info("Cassette has SLM Reservation");
                continue;
            }
            log.info("Assign cassette as available empty cassette.");
            List<Infos.FoundCassette> strFoundCassette1 = new ArrayList<>();
            if (CimObjectUtils.isEmpty(out.getStrFoundCassette())) {
                out.setStrFoundCassette(strFoundCassette1);
            }
            out.getStrFoundCassette().add(nOutPutCasLen, strFoundCassette.get(i));
            nOutPutCasLen++;
            bEmptyCasFoundFlag = true;
        }
        Validations.check(CimBooleanUtils.isFalse(bEmptyCasFoundFlag), retCodeConfigEx.getNotFoundEmptyCast());
        return out;
    }

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2019/7/11                               Neyo                create file
     *
     * @author Neyo
     * @since 2019/7/11 15:23
     * Copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Override
    public void cassetteCheckConditionForDelivery(Infos.ObjCommon objCommonIn, ObjectIdentifier equipmentID, String portGroupID, List<Infos.StartCassette> strStartCassette, String operation) {
        //Initialize
        /*-------------------------------------------------------------------------------------*/
        /*                                                                                     */
        /*   Check Condition of controlJobID, multiLotType, transferState, transferReserved,   */
        /*   dispatchState, and cassetteState for all cassettes                                */
        /*   dispatchState, cassetteState, and loadingSequenceNumber for all cassettes         */
        /*                                                                                     */
        /*-------------------------------------------------------------------------------------*/
        /*--------------------------*/
        /*   Get Equipment Object   */
        /*--------------------------*/
        log.info("Get Equipment Object");
        CimMachine aMachine = baseCoreFactory.getBO(CimMachine.class, equipmentID);
        /*-------------------------------------------*/
        /*   Get Equipment's MultiRecipeCapability   */
        /*-------------------------------------------*/
        log.info("Get Equipment's MultiRecipeCapability");
        String multipleRecipeCapability = aMachine.getMultipleRecipeCapability();
        /*-----------------------------------------*/
        /*   Get Equipment's Operation Mode Info   */
        /*-----------------------------------------*/
        log.info("Get Equipment's Operation Mode Info");
        Validations.check(CimArrayUtils.isEmpty(strStartCassette), retCodeConfig.getNotFoundCassette());
        if (CimStringUtils.equals(BizConstant.SP_OPERATION_OPESTART, operation)
                || CimStringUtils.equals(BizConstant.SP_OPERATION_STARTRESERVATION, operation)) {
            log.info("operation == SP_Operation_OpeStart, SP_Operation_StartReservation");
            //【step1】 - portResource_currentOperationMode_Get
            Outputs.ObjPortResourceCurrentOperationModeGetOut portResourceCurrentOperationModeRetCode = portMethod.portResourceCurrentOperationModeGet(objCommonIn,
                    equipmentID, strStartCassette.get(0).getLoadPortID());
            log.info("portResource_currentOperationMode_Get=RC_OK");
        } else {
            log.info("operation != SP_Operation_OpeStart && operation != SP_Operation_StartReservation");
        }
        /*------------------------------*/
        /*   Check Cassette Condition   */
        /*------------------------------*/
        int lenCassette = CimArrayUtils.getSize(strStartCassette);
        ObjectIdentifier saveControlJobID = new ObjectIdentifier();
        int cjCastCnt = 0;
        int i = 0;
        int j = 0;
        for (i = 0; i < lenCassette; i++) {
            /*------------------------------------------*/
            /*   Check Start Cassette's Loading Order   */
            /*------------------------------------------*/
            if (CimStringUtils.equals(BizConstant.SP_OPERATION_OPESTART, operation)
                    || CimStringUtils.equals(BizConstant.SP_OPERATION_STARTRESERVATION, operation)) {
                log.info("(operation == SP_Operation_OpeStart) || (operation == SP_Operation_StartReservation)");
                Validations.check(strStartCassette.get(i).getLoadSequenceNumber() != (i + 1), retCodeConfig.getInvalidLoadingSequence());
            }
            /*-------------------------*/
            /*   Get Cassette Object   */
            /*-------------------------*/
            CimCassette aCassette = baseCoreFactory.getBO(CimCassette.class, strStartCassette.get(i).getCassetteID());
            /*--------------------------------*/
            /*   Get and Check ControlJobID   */
            /*--------------------------------*/
            CimControlJob aControlJob = aCassette.getControlJob();
            if (CimStringUtils.equals(BizConstant.SP_OPERATION_OPESTART, operation)) {
                if (i == 0) {
                    if (!CimObjectUtils.isEmpty(aControlJob)) {
                        saveControlJobID = aControlJob.getControlJobID();
                    } else {
                        saveControlJobID = new ObjectIdentifier("", "");
                    }
                } else {
                    ObjectIdentifier castControlJobID = new ObjectIdentifier();
                    if (!CimObjectUtils.isEmpty(aControlJob)) {
                        castControlJobID = aControlJob.getControlJobID();
                    } else {
                        castControlJobID = new ObjectIdentifier("", "");
                    }
                    Validations.check(!ObjectIdentifier.equalsWithValue(castControlJobID, saveControlJobID), retCodeConfig.getCassetteControlJobMix());
                }
            } else {
                Validations.check(!CimObjectUtils.isEmpty(aControlJob), retCodeConfig.getCassetteControlJobFilled());
            }
            /*---------------------------------*/
            /*   Get Cassette's MultiLotType   */
            /*---------------------------------*/
            log.info("Get Cassette's MultiLotType");
            String multiLotType = aCassette.getMultiLotType();
            /*-------------------------------------------------*/
            /*   Check MultiRecipeCapability VS MultiLotType   */
            /*-------------------------------------------------*/
            log.info("Check MultiRecipeCapability VS MultiLotType");
            if (CimStringUtils.equals(BizConstant.SP_LOADPURPOSETYPE_EMPTYCASSETTE, strStartCassette.get(i).getLoadPurposeType())) {
                log.info("strStartCassette[i].loadPurposeType == SP_LoadPurposeType_EmptyCassette");
                //result.setReturnCode(retCodeConfig.getSucc());
            } else if (CimStringUtils.equals(BizConstant.SP_EQP_MULTIRECIPECAPABILITY_MULTIPLERECIPE, multipleRecipeCapability)) {
                log.info("multiRecipeCapability == SP_Eqp_MultiRecipeCapability_MultipleRecipe");
                //result.setReturnCode(retCodeConfig.getSucc());
            } else if (CimStringUtils.equals(BizConstant.SP_EQP_MULTIRECIPECAPABILITY_SINGLERECIPE, multipleRecipeCapability)) {
                log.info("multiRecipeCapability == SP_Eqp_MultiRecipeCapability_SingleRecipe");
                //result.setReturnCode(retCodeConfig.getSucc());
            } else if (CimStringUtils.equals(BizConstant.SP_EQP_MULTIRECIPECAPABILITY_BATCH, multipleRecipeCapability)) {
                log.info("multiRecipeCapability == SP_Eqp_MultiRecipeCapability_Batch");
                if (CimStringUtils.equals(BizConstant.SP_CAS_MULTILOTTYPE_SINGLELOTSINGLERECIPE, multiLotType)
                        || CimStringUtils.equals(BizConstant.SP_CAS_MULTILOTTYPE_MULTILOTSINGLERECIPE, multiLotType)) {
                    log.info("rc = RC_OK");
                    //result.setReturnCode(retCodeConfig.getSucc());
                } else {
                    log.error("return RC_CAST_EQP_CONDITION_ERROR!!");
                    throw new ServiceException(retCodeConfig.getCassetteEquipmentConditionError());
                }
            } else {
                log.info("No Process <Check MultiRecipeCapability VS MultiLotType>");
            }
            /*--------------------------------------*/
            /*   Check Cassette's Transfer Status   */
            /*--------------------------------------*/
            /*-----------------------*/
            /*   Get TransferState   */
            /*-----------------------*/
            log.info("Get TransferState");
            String transportState = aCassette.getTransportState();
            /*------------------------------*/
            /*   Get TransferReserveState   */
            /*------------------------------*/
            log.info("Get TransferReserveState");
            boolean transferReserved = aCassette.isReserved();
            /*===== for OpeStart =====*/
            if (CimStringUtils.equals(BizConstant.SP_OPERATION_OPESTART, operation)) {
                Validations.check(!CimStringUtils.equals(BizConstant.SP_TRANSSTATE_EQUIPMENTIN, transportState), retCodeConfig.getInvalidCassetteTransferState());
            }
            /*===== for FlowBatching =====*/
            else if (CimStringUtils.equals(BizConstant.SP_OPERATION_FLOWBATCHING, operation)) {
                log.info("operation = SP_Operation_FlowBatching");
                if (CimStringUtils.equals(BizConstant.SP_TRANSSTATE_STATIONIN, transportState)
                        || CimStringUtils.equals(BizConstant.SP_TRANSSTATE_BAYIN, transportState)
                        || CimStringUtils.equals(BizConstant.SP_TRANSSTATE_MANUALIN, transportState)
                        && (CimBooleanUtils.isFalse(transferReserved))) {
                    //result.setReturnCode(retCodeConfig.getSucc());
                } else {
                    throw new ServiceException(retCodeConfig.getInvalidCassetteTransferState());
                }
            }
            /*===== for StartReservation =====*/
            else if (CimStringUtils.equals(BizConstant.SP_OPERATION_STARTRESERVATION, operation)) {
                log.info("operation = SP_Operation_StartReservation");
                //【step2】 - portResource_currentOperationMode_Get
                Outputs.ObjPortResourceCurrentOperationModeGetOut portResourceCurrentOperationModeRetCode = portMethod.portResourceCurrentOperationModeGet(objCommonIn,
                        equipmentID,
                        strStartCassette.get(0).getLoadPortID());
                if (CimStringUtils.equals(BizConstant.SP_EQP_ACCESSMODE_MANUAL, portResourceCurrentOperationModeRetCode.getOperationMode().getAccessMode())) {
                    log.info(".strOperationMode.accessMode = SP_Eqp_AccessMode_Manual");
                    /*--------------------------------------------------------------------------------------------------*/
                    /*   Logic which put it here in [cassette_CheckConditionForOperation] by P4100105 is unnecessary.   */
                    /*   Because, This Function is used only at the time of CassetteDelivery.                           */
                    /*   Then, it doesn't come here because Check has already been done.                                */
                    /*--------------------------------------------------------------------------------------------------*/
                    //result.setReturnCode(retCodeConfig.getSucc());
                } else {
                    Boolean bReRouteFlag = false;
                    String reRouteXferFlag = StandardProperties.OM_XFER_REROUTE_FLAG.getValue();
                    if (CimStringUtils.equals(reRouteXferFlag, "1")
                            && (CimStringUtils.equals(BizConstant.SP_EQP_PORT_OPERATIONMODE_AUTO_3, portResourceCurrentOperationModeRetCode.getOperationMode().getOperationMode().getValue()))
                            && (CimStringUtils.equals(BizConstant.SP_TRANSSTATE_STATIONIN, transportState) ||
                            CimStringUtils.equals(BizConstant.SP_TRANSSTATE_BAYIN, transportState) ||
                            CimStringUtils.equals(BizConstant.SP_TRANSSTATE_MANUALIN, transportState) ||
                            CimStringUtils.equals(BizConstant.SP_TRANSSTATE_BAYOUT, transportState))
                            && (CimBooleanUtils.isFalse(transferReserved))) {
                        log.info("operationMode is Auto-3");
                        log.info("transferState = [SI], [BI], [MI], [BO] and transferReserved is FALSE");
                        //result.setReturnCode(retCodeConfig.getSucc());
                    } else if (CimStringUtils.equals(BizConstant.SP_TRANSSTATE_STATIONIN, transportState)
                            || CimStringUtils.equals(BizConstant.SP_TRANSSTATE_BAYIN, transportState)
                            || CimStringUtils.equals(BizConstant.SP_TRANSSTATE_MANUALIN, transportState)
                            && (CimBooleanUtils.isFalse(transferReserved))) {
                        log.info("transferState = [SI], [BI], [MI] and transferReserved is FALSE");
                        //result.setReturnCode(retCodeConfig.getSucc());
                    } else if (CimStringUtils.equals(BizConstant.SP_TRANSSTATE_EQUIPMENTIN, transportState)
                            && CimBooleanUtils.isFalse(transferReserved)) {
                        log.info("(transferState = SP_TransState_EquipmentIn) and (transferReserved == false)");
                        /*--------------------------------------------------------------------------------------------------*/
                        /*   Logic which put it here in [cassette_CheckConditionForOperation] by P4100105 is unnecessary.   */
                        /*   Because, This Function is used only at the time of CassetteDelivery.                           */
                        /*   Then, it doesn't come here because Check has already been done.                                */
                        /*--------------------------------------------------------------------------------------------------*/
                        /*--------------------------------------*/
                        /*   Get Originator Eqp's Access Mode   */
                        /*--------------------------------------*/

                        /*--------------------------------*/
                        /*   Get Originator EquipmentID   */
                        /*--------------------------------*/
                        log.info("Get Originator EquipmentID");
                        Machine aMachine1 = aCassette.currentAssignedMachine();
                        if (!CimObjectUtils.isEmpty(aMachine1)) {
                            boolean isStorageBool = false;
                            isStorageBool = aMachine1.isStorageMachine();
                            if (CimBooleanUtils.isTrue(isStorageBool)) {
                                log.info("isStorageBool is TRUE: Storage");
                            } else {
                                log.info("isStorageBool is not TRUE: Equipment. So narrow to PosMachine");
                            }
                        } else {
                            log.info("aMachine is Null");
                        }
                        Validations.check(null == aMachine1, retCodeConfig.getNotFoundEqp());
                        ObjectIdentifier orgEquipmentID = new ObjectIdentifier();
                        orgEquipmentID = ObjectIdentifier.build(aMachine1.getIdentifier(), aMachine1.getPrimaryKey());
                        /*---------------------------------*/
                        /*   Get Cassette Info in OrgEqp   */
                        /*---------------------------------*/
                        log.info("Get Cassette Info in OrgEqp");
                        Infos.EqpPortStatus strOrgEqpPortStatus = new Infos.EqpPortStatus();
                        Infos.EqpPortInfo equipmentPortInfo = new Infos.EqpPortInfo();
                        String equipmentCategory = ((CimMachine) aMachine1).getCategory();
                        if (CimStringUtils.equals(BizConstant.SP_MC_CATEGORY_INTERNALBUFFER, equipmentCategory)) {
                            log.info("equipmentCategory is [InternalBuffer]");
                            //【step3】 - equipment_portInfoForInternalBuffer_GetDR
                            equipmentPortInfo = equipmentMethod.equipmentPortInfoForInternalBufferGetDR(objCommonIn, orgEquipmentID);
                        } else {
                            log.info("equipmentCategory is not [InternalBuffer]");
                            //【step4】 - equipment_portInfo_Get
                            equipmentPortInfo = equipmentMethod.equipmentPortInfoGet(objCommonIn, orgEquipmentID);
                        }
                        /*--------------------------------------*/
                        /*   Find Assigned Port's portGroupID   */
                        /*--------------------------------------*/
                        log.info("Find Assigned Port's portGroupID");
                        boolean bFound = false;
                        int lenEqpPort = CimArrayUtils.getSize(equipmentPortInfo.getEqpPortStatuses());
                        for (j = 0; j < lenEqpPort; j++) {
                            if (CimStringUtils.equals(strStartCassette.get(i).getCassetteID().getValue(), equipmentPortInfo.getEqpPortStatuses().get(j).getLoadedCassetteID().getValue())) {
                                log.info("break!!");
                                strOrgEqpPortStatus = equipmentPortInfo.getEqpPortStatuses().get(j);
                                bFound = true;
                                break;
                            }
                        }
                        Validations.check(CimBooleanUtils.isFalse(bFound), retCodeConfig.getInvalidCassetteTransferState());
                        /*-----------------------------------------------------------------------------------------------*/
                        /*   The following StartLotReserve isn't permitted in Transfer EQP to EQP of CassetteDelivery.   */
                        /*                                                                                               */
                        /*   ToEQP's OperationMode : ***-2                                                               */
                        /*-----------------------------------------------------------------------------------------------*/
                        if (CimStringUtils.equals(BizConstant.SP_EQP_ACCESSMODE_AUTO, portResourceCurrentOperationModeRetCode.getOperationMode().getAccessMode())
                                && CimStringUtils.equals(BizConstant.SP_EQP_DISPATCHMODE_AUTO, portResourceCurrentOperationModeRetCode.getOperationMode().getDispatchMode())) {
                            log.info("rc = RC_OK");
                            //result.setReturnCode(retCodeConfig.getSucc());
                        } else {
                            log.error("return RC_INVALID_CAST_XFERSTAT");
                            throw new ServiceException(retCodeConfig.getInvalidCassetteTransferState());
                        }
                    } else {
                        log.error("return RC_INVALID_CAST_XFERSTAT");
                        throw new ServiceException(retCodeConfig.getInvalidCassetteTransferState());
                    }
                }
            } else {
                log.info("No Process <for Ope Start>");
            }
            /*----------------------------------------------*/
            /*   Get and Check Cassette's Dispatch Status   */
            /*----------------------------------------------*/

            /*------------------------------------*/
            /*   Get Cassette's Dispatch Status   */
            /*------------------------------------*/
            log.info("Get Cassette's Dispatch Status");
            Boolean dispatchReserveFlag = aCassette.isDispatchReserved();
            if (CimStringUtils.equals(BizConstant.SP_OPERATION_STARTRESERVATION, operation)
                    || CimStringUtils.equals(BizConstant.SP_OPERATION_FLOWBATCHING, operation)) {
                log.info("operation == SP_Operation_StartReservation, SP_Operation_FlowBatching");
                Validations.check(CimBooleanUtils.isTrue(dispatchReserveFlag), retCodeConfig.getAlreadyDispatchReservedCassette());
            } else {
                log.info("operation != SP_Operation_StartReservation && operation != SP_Operation_FlowBatching");
            }
            /*-------------------------------------*/
            /*   Get and Check Cassette's Status   */
            /*-------------------------------------*/
            log.info("Get and Check Cassette's Status");
            String cassetteState = aCassette.getDurableState();
            if (CimStringUtils.equals(BizConstant.CIMFW_DURABLE_AVAILABLE, cassetteState)
                    || CimStringUtils.equals(BizConstant.CIMFW_DURABLE_INUSE, cassetteState)) {
                log.info("cassetteState = CIMFW_Durable_Available or CIMFW_Durable_InUse");
                //result.setReturnCode(retCodeConfig.getSucc());
            } else {
                log.error("return RC_INVALID_CAST_STAT");
                throw new ServiceException(retCodeConfig.getInvalidCassetteState());
            }
        }
        /*---------------------------------------------------------*/
        /*   Check Cassette's ControlJobID vs Eqp's ControlJobID   */
        /*---------------------------------------------------------*/
        log.info("Check Cassette's ControlJobID vs Eqp's ControlJobID");
        if (CimStringUtils.equals(BizConstant.SP_OPERATION_OPESTART, operation)) {
            log.info("operation == SP_Operation_OpeStart");
            /*===== get reserved controlJobID for each portGroup =====*/
            //【step5】 - equipment_reservedControlJobID_Get
            List<Infos.StartReservedControlJobInfo> startReservedControlJobInfos = equipmentMethod.equipmentReservedControlJobIDGetDR(objCommonIn, equipmentID);
            int rsvCJLen = CimArrayUtils.getSize(startReservedControlJobInfos);
            /*===== find reserved controlJobID for specified portGroup =====*/
            ObjectIdentifier eqpControlJobID = new ObjectIdentifier();
            for (i = 0; i < rsvCJLen; i++) {
                if (CimStringUtils.equals(portGroupID, startReservedControlJobInfos.get(i).getPortGroupID())) {
                    log.info("in-parm's portGroup is found in reservedControlJobInfo...");
                    eqpControlJobID = startReservedControlJobInfos.get(i).getControlJobID();
                    /*---------------------------*/
                    /*   Get ControlJob Object   */
                    /*---------------------------*/
                    log.info("Get ControlJob Object...");
                    CimControlJob aReserveControlJob = baseCoreFactory.getBO(CimControlJob.class, eqpControlJobID);
                    /*-------------------------------------------*/
                    /*   Get PosStartCassetteInfoSequence Info   */
                    /*-------------------------------------------*/
                    List<ProductDTO.PosStartCassetteInfo> startCassetteInfo = aReserveControlJob.getStartCassetteInfo();
                    cjCastCnt = CimArrayUtils.getSize(startCassetteInfo);
                    break;
                }
            }
            /*===== compare reserved controlJobID vs cassette's controlJobID =====*/

            Validations.check(!ObjectIdentifier.equalsWithValue(saveControlJobID, eqpControlJobID), retCodeConfig.getCassettePortControlJobUnMatch());
            /*===== check reserved controlJobID's cassette count vs in-parm's cassette count =====*/
            if (CimStringUtils.length(saveControlJobID.getValue()) > 0) {
                log.info("check reserved controlJobID's cassette count vs in-parm's cassette count");
                lenCassette = CimArrayUtils.getSize(strStartCassette);
                Validations.check(lenCassette != cjCastCnt, retCodeConfig.getCassettePortControlJobUnMatch());
            }
        } else {
            log.info("operation != SP_Operation_OpeStart");
        }
        /*------------------------------------------------------------------------------*/
        /*                                                                              */
        /*   Check Condition for Eqp's MultiRecipeCapability VS RecipeParameterValue    */
        /*                                                                              */
        /*------------------------------------------------------------------------------*/
        if (CimStringUtils.equals(BizConstant.SP_OPERATION_OPESTART, operation)
                || CimStringUtils.equals(BizConstant.SP_OPERATION_STARTRESERVATION, operation)) {
            log.info("operation = SP_Operation_OpeStart or SP_Operation_StartReservation");
            if (CimStringUtils.equals(BizConstant.SP_EQP_MULTIRECIPECAPABILITY_SINGLERECIPE, multipleRecipeCapability)
                    || CimStringUtils.equals(BizConstant.SP_EQP_MULTIRECIPECAPABILITY_BATCH, multipleRecipeCapability)) {
                log.info("multiRecipeCapability = SP_Eqp_MultiRecipeCapability_SingleRecipe or SP_Eqp_MultiRecipeCapability_Batch");
                /*-----------------------------------*/
                /*   Work Valiable for Check Logic   */
                /*-----------------------------------*/
                boolean baseSetFlag = false;
                int baseI = 0;
                int baseJ = 0;
                int baseRPLen = 0;
                /*-------------------------------*/
                /*   Loop for strStartCassette   */
                /*-------------------------------*/
                for (i = 0; i < lenCassette; i++) {
                    /*------------------------*/
                    /*   Omit EmptyCassette   */
                    /*------------------------*/
                    if (CimStringUtils.equals(BizConstant.SP_LOADPURPOSETYPE_EMPTYCASSETTE, strStartCassette.get(i).getLoadPurposeType())) {
                        log.info("strStartCassette[i].loadPurposeType == SP_LoadPurposeType_EmptyCassette");
                        continue;
                    }
                    /*-------------------------------*/
                    /*   Loop for strLotInCassette   */
                    /*-------------------------------*/
                    int lenLotInCassette = CimArrayUtils.getSize(strStartCassette.get(i).getLotInCassetteList());
                    for (j = 0; j < lenLotInCassette; j++) {
                        /*------------------------*/
                        /*   Omit Non-Start Lot   */
                        /*------------------------*/
                        if (CimBooleanUtils.isFalse(strStartCassette.get(i).getLotInCassetteList().get(j).getMoveInFlag())) {
                            log.info("strStartCassette[i].strLotInCassette[j].operationStartFlag == FALSE");
                            continue;
                        }
                        /*-------------------------------------*/
                        /*   Check RecipeParameterChangeType   */
                        /*-------------------------------------*/
                        if (!CimStringUtils.equals(BizConstant.SP_RPARM_CHANGETYPE_BYLOT, strStartCassette.get(i).getLotInCassetteList().get(j).getRecipeParameterChangeType())) {
                            log.error("return RC_INVALID_RPARM_CHANGETYPE!!");
                            throw new ServiceException(retCodeConfig.getInvalidRecipeParamChangeType());
                        }

                        /*--------------------*/
                        /*   Save Base Info   */
                        /*--------------------*/
                        if (CimBooleanUtils.isFalse(baseSetFlag)) {
                            log.info("baseSetFlag == FALSE");
                            baseSetFlag = true;
                            baseI = i;
                            baseJ = j;
                            if (CimArrayUtils.getSize(strStartCassette.get(i).getLotInCassetteList().get(j).getLotWaferList()) > 0) {
                                log.info("strStartCassette[i].strLotInCassette[j].strLotWafer.length() > 0");
                                baseRPLen = CimArrayUtils.getSize(strStartCassette.get(i).getLotInCassetteList().get(j).getLotWaferList().get(0).getStartRecipeParameterList());
                            } else {
                                log.error("return RC_INVALID_CAST_XFERSTAT!!");
                                throw new ServiceException(retCodeConfig.getInvalidWaferCount());
                            }
                        }
                        int lwLen = CimArrayUtils.getSize(strStartCassette.get(i).getLotInCassetteList().get(j).getLotWaferList());
                        for (int k = 0; k < lwLen; k++) {
                            /*---------------------------------*/
                            /*   Check RecipeParameter Count   */
                            /*---------------------------------*/
                            int rpLen = CimArrayUtils.getSize(strStartCassette.get(i).getLotInCassetteList().get(j).getLotWaferList().get(k).getStartRecipeParameterList());
                            Validations.check(rpLen != baseRPLen, retCodeConfig.getNotSameRecipeParamInfo());
                            /*--------------------------------------*/
                            /*   Loop for strStartRecipeParameter   */
                            /*--------------------------------------*/
                            for (int l = 0; l < rpLen; l++) {
                                /*-----------------------------------------------*/
                                /*   Check RecipeParameter Info is Same or Not   */
                                /*-----------------------------------------------*/
                                /*===== parameterName check (string) =====*/
                                if (!CimStringUtils.equals(strStartCassette.get(i).getLotInCassetteList().get(j).getLotWaferList().get(k).getStartRecipeParameterList().get(l).getParameterName(),
                                        strStartCassette.get(baseI).getLotInCassetteList().get(baseJ).getLotWaferList().get(0).getStartRecipeParameterList().get(l).getParameterName())) {
                                    log.error("return RC_NOT_SAME_RPARM_INFO!!");
                                    throw new ServiceException(retCodeConfig.getNotSameRecipeParamInfo());
                                }
                                /*===== parameterValue check (string) =====*/
                                if (!CimStringUtils.equals(strStartCassette.get(i).getLotInCassetteList().get(j).getLotWaferList().get(k).getStartRecipeParameterList().get(l).getParameterValue(),
                                        strStartCassette.get(baseI).getLotInCassetteList().get(baseJ).getLotWaferList().get(0).getStartRecipeParameterList().get(l).getTargetValue())) {
                                    log.error("return RC_NOT_SAME_RPARM_INFO!!");
                                    throw new ServiceException(retCodeConfig.getNotSameRecipeParamInfo());
                                }
                                /*===== useCurrentSettingValueFlag check (boolean) =====*/
                                if (strStartCassette.get(i).getLotInCassetteList().get(j).getLotWaferList().get(k).getStartRecipeParameterList().get(l).getUseCurrentSettingValueFlag() != null &&
                                        !strStartCassette.get(i).getLotInCassetteList().get(j).getLotWaferList().get(k).getStartRecipeParameterList().get(l).getUseCurrentSettingValueFlag().equals(
                                                strStartCassette.get(baseI).getLotInCassetteList().get(baseJ).getLotWaferList().get(0).getStartRecipeParameterList().get(l).getUseCurrentSettingValueFlag()
                                        )) {
                                    log.error("return RC_NOT_SAME_RPARM_INFO!!");
                                    throw new ServiceException(retCodeConfig.getNotSameRecipeParamInfo());
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public Outputs.ObjCassetteDeliverySearchEmptyCassetteAssignPortOut cassetteDeliverySearchEmptyCassetteAssignPort(Infos.ObjCommon objCommonIn, ObjectIdentifier lotID, List<Infos.PortID> portIDSeq, List<Infos.FoundCassette> emptyCassetteSeq, List<ObjectIdentifier> omitCassetteSeq, List<ObjectIdentifier> omitPortIDSeq) {
        /*----------------*/
        /*   Initialize   */
        /*----------------*/
        Outputs.ObjCassetteDeliverySearchEmptyCassetteAssignPortOut out = new Outputs.ObjCassetteDeliverySearchEmptyCassetteAssignPortOut();
        /* *********************************************************************************/
        /*                                                                                */
        /*   Acquire CassetteCategory if there is designation of Lot                      */
        /*                                                                                */
        /* *********************************************************************************/
        log.info("Acquire CassetteCategory if there is designation of Lot");
        String limitCategory = "";
        if (CimStringUtils.length(lotID.getValue()) > 0) {
            /*----------------------------------------------------------*/
            /*   Get EmptyCassette which should be necessary Lot next   */
            /*----------------------------------------------------------*/
            log.info("lot_requiredCassetteCategory_GetForNextOperation");
            //【step1】 - lot_requiredCassetteCategory_GetForNextOperation

            limitCategory = lotMethod.lotRequiredCassetteCategoryGetForNextOperation(objCommonIn, lotID);
        } else {
            log.info("No Check Limit Category");
        }
        /* *********************************************************************************/
        /*                                                                                */
        /*   Look for EmptyCassette that LoadPurposeType is suitable for EmptyCassette    */
        /*                                                                                */
        /* *********************************************************************************/
        log.info("Look for EmptyCassette that LoadPurposeType is suitable for EmptyCassette");
        Boolean bFoundAssignPort = false;
        int lenPortID = CimArrayUtils.getSize(portIDSeq);
        int lenOmitCassette = CimArrayUtils.getSize(omitCassetteSeq);
        int lenOmitPort = CimArrayUtils.getSize(omitPortIDSeq);
        int lenEmptyCassette = CimArrayUtils.getSize(emptyCassetteSeq);
        for (int i = 0; i < lenEmptyCassette; i++) {
            /*------------------------------------*/
            /*   Omit Cassette of limitCategory   */
            /*------------------------------------*/
            log.info("Omit Cassette of limitCategory");
            if (CimStringUtils.isNotEmpty(limitCategory) && !CimStringUtils.equals(limitCategory, emptyCassetteSeq.get(i).getCassetteCategory())) {
                log.info("Omit Cassette of limitCategory   ...<<continue>>");
                continue;
            }
            /*--------------------------------------*/
            /*   Omit Cassette of omitCassetteSeq   */
            /*--------------------------------------*/
            log.info("Omit Cassette of omitCassetteSeq");
            boolean bOmitCassette = false;
            for (int j = 0; j < lenOmitCassette; j++) {
                if (CimStringUtils.isEmpty(omitCassetteSeq.get(j).getValue())) {
                    continue;
                }
                if (CimStringUtils.equals(omitCassetteSeq.get(j).getValue(), emptyCassetteSeq.get(i).getCassetteID().getValue())) {
                    bOmitCassette = true;
                    break;
                }
            }
            if (CimBooleanUtils.isTrue(bOmitCassette)) {
                log.info("Omit Cassette of omitCassetteSeq   ...<<continue>>");
                continue;
            }
            /*---------------------------------------------------------------------------------------------------------*/
            /*   Look for Port which categoryCapability of Port and CassetteCategory of EmptyCassttte corresponds to   */
            /*---------------------------------------------------------------------------------------------------------*/
            log.info("Look for Port which categoryCapability of Port and CassetteCategory of EmptyCassttte corresponds to");
            for (int j = 0; j < lenPortID; j++) {
                /*--------------------------------*/
                /*   Omit Port of omitPortIDSeq   */
                /*--------------------------------*/
                log.info("Omit Port of omitPortIDSeq");
                boolean bOmitPort = false;
                for (int k = 0; k < lenOmitPort; k++) {
                    if (CimStringUtils.isEmpty(portIDSeq.get(j).getPortID().getValue())) {
                        continue;
                    }
                    if (CimStringUtils.equals(portIDSeq.get(j).getPortID().getValue(), omitPortIDSeq.get(k).getValue())) {
                        bOmitPort = true;
                        break;
                    }
                }
                if (CimBooleanUtils.isTrue(bOmitPort)) {
                    log.info("Omit Port of omitPortIDSeq   ...<<continue>>");
                    continue;
                }
                /*------------------------------------------------*/
                /*   Omit Port LoadPurposeType is EmptyCassette   */
                /*------------------------------------------------*/
                log.info("Omit Port LoadPurposeType is EmptyCassette");
                if (!CimStringUtils.equals(BizConstant.SP_LOADPURPOSETYPE_EMPTYCASSETTE, portIDSeq.get(j).getLoadPurposeType())
                        && !CimStringUtils.equals(BizConstant.SP_LOADPURPOSETYPE_ANY, portIDSeq.get(j).getLoadPurposeType())
                        && !CimStringUtils.equals(BizConstant.SP_LOADPURPOSETYPE_INTERNALBUFFER, portIDSeq.get(j).getLoadPurposeType())) {
                    log.info("Omit Port LoadPurposeType is not [EmptyCassette] and [InternalBuffer] and [Any]   ...<<continue>>");
                    continue;
                }
                /*------------------------------------------------*/
                /*   Look for Port of the same cassetteCategory   */
                /*------------------------------------------------*/
                log.info("Look for Port of the same cassetteCategory");
                int lenCategoryCapability = CimArrayUtils.getSize(portIDSeq.get(j).getCategoryCapability());
                if (lenCategoryCapability > 0) {
                    for (int k = 0; k < lenCategoryCapability; k++) {
                        if (CimStringUtils.equals(portIDSeq.get(j).getCategoryCapability().get(k), emptyCassetteSeq.get(i).getCassetteCategory())) {
                            out.setStrFoundPort(portIDSeq.get(j));
                            out.setFoundEmptyCassetteID(emptyCassetteSeq.get(i).getCassetteID());
                            bFoundAssignPort = true;
                            break;
                        }
                    }
                } else {
                    // Assign EmptyCassttte found first because it doesn't have categoryCapability
                    log.info("Assign EmptyCassttte found first because it doesn't have categoryCapability");
                    out.setStrFoundPort(portIDSeq.get(j));
                    out.setFoundEmptyCassetteID(emptyCassetteSeq.get(i).getCassetteID());
                    bFoundAssignPort = true;
                }
                if (CimBooleanUtils.isTrue(bFoundAssignPort)) {
                    break;
                }
            }
            if (CimBooleanUtils.isTrue(bFoundAssignPort)) {
                break;
            }
        }
        Validations.check(CimBooleanUtils.isFalse(bFoundAssignPort), retCodeConfigEx.getNotEnoughEmptyCassette());
        return out;
    }

    @Override
    public void cassetteCheckConditionForArrivalCarrierCancel(Infos.ObjCommon objCommon, List<Infos.NPWXferCassette> strNPWXferCassette) {
        /*----------------*/
        /*   Initialize   */
        /*----------------*/
        int i;
        int lenCassette;

        /*--------------------------------------*/
        /*                                      */
        /*   Check Cassette's Transfer Status   */
        /*                                      */
        /*--------------------------------------*/
        lenCassette = CimArrayUtils.getSize(strNPWXferCassette);
        log.info("Check Cassette's Transfer Status  lenCassette= {}", lenCassette);

        for (i = 0; i < lenCassette; i++) {
            log.info("loop to lenCassette");
            /*---------------------------*/
            /*   Get cassetteID Object   */
            /*---------------------------*/
            log.info("Get cassetteID Object");
            com.fa.cim.newcore.bo.durable.CimCassette aCassette = baseCoreFactory.getBO(com.fa.cim.newcore.bo.durable.CimCassette.class, strNPWXferCassette.get(i).getCassetteID());
            Validations.check(null == aCassette, retCodeConfig.getNotFoundCassette());
            /*-----------------------*/
            /*   Get DispatchState   */
            /*-----------------------*/
            log.info("Get DispatchState");
            Boolean dispatchState = aCassette.isDispatchReserved();
            Validations.check(CimBooleanUtils.isFalse(dispatchState), retCodeConfig.getNotDispatchReservedCassette());

            /*----------------------------------*/
            /*   Get controlJobID of Cassette   */
            /*----------------------------------*/
            com.fa.cim.newcore.bo.product.CimControlJob aControlJob = aCassette.getControlJob();
            Validations.check(null != aControlJob, retCodeConfig.getCassetteControlJobFilled());
        }

        return;
    }

    @Override
    public Outputs.ObjCassetteLotListGetWithPriorityOrderOut cassetteLotListGetWithPriorityOrder(Infos.ObjCommon objCommon, ObjectIdentifier cassetteID) {
        Outputs.ObjCassetteLotListGetWithPriorityOrderOut objCassetteLotListGetWithPriorityOrderOut = new Outputs.ObjCassetteLotListGetWithPriorityOrderOut();
        /* ****************************/
        /*  Check input parameter    */
        /* ****************************/
        Validations.check(ObjectIdentifier.isEmptyWithValue(cassetteID), retCodeConfig.getInvalidInputParam());
        CimCassette aCassette = baseCoreFactory.getBO(CimCassette.class, cassetteID);
        /* *******************************/
        /*  Get all Lots in cassette    */
        /* *******************************/
        List<Lot> aLotList = aCassette.allLots();
        int lotLen = CimArrayUtils.getSize(aLotList);
        Validations.check(lotLen == 0, new OmCode(retCodeConfig.getCastIsEmpty(), cassetteID.getValue()));
        /* ****************************/
        /*   Get Lots information    */
        /* ****************************/
        log.info("Step1 - Get Lots information");
        List<Infos.LotStatusInfo> lotInfoList = new ArrayList<>();
        List<Integer> waitingLotNumber = new ArrayList<>();
        List<Integer> heldLotNumber = new ArrayList<>();
        List<Integer> bankLotNumber = new ArrayList<>();
        int i = 0;
        for (Lot lotDO : aLotList) {
            CimLot aLot = (CimLot) lotDO;
            Validations.check(null == aLot, retCodeConfig.getNotFoundLot());
            /* *****************/
            /*   Get lot ID   */
            /* *****************/
            Infos.LotStatusInfo lotStatusInfo = new Infos.LotStatusInfo();
            lotInfoList.add(lotStatusInfo);
            lotStatusInfo.setLotID(new ObjectIdentifier(aLot.getIdentifier(), aLot.getPrimaryKey()));
            /* *******************************/
            /*   Get lot Base information   */
            /* *******************************/
            ProductDTO.LotBaseInfo aLotBaseInfo = aLot.getLotBaseInfo();
            lotStatusInfo.setRepresentativeState(aLotBaseInfo.getRepresentativeState());
            lotStatusInfo.setProductID(aLotBaseInfo.getProductSpecificationID());
            lotStatusInfo.setPriorityClass(aLotBaseInfo.getPriorityClass());
            lotStatusInfo.setExternalPriority(aLotBaseInfo.getExternalPriority());
            lotStatusInfo.setInternalPriority(aLotBaseInfo.getInternalPriority());
            /* ****************************/
            /*   Get lot current state   */
            /* ****************************/
            Infos.LotStatusAttributes currentStatus = new Infos.LotStatusAttributes();
            lotStatusInfo.setCurrentStatus(currentStatus);
            currentStatus.setLotState(aLot.getLotState());
            /* *******************************/
            /*   Get lot production state   */
            /* *******************************/
            currentStatus.setProcessState(aLot.getLotProductionState());
            /* *************************/
            /*   Get lot hold state   */
            /* *************************/
            currentStatus.setHoldState(aLot.getLotHoldState());
            /* *****************************/
            /*   Get lot finished state   */
            /* *****************************/
            currentStatus.setFinishedState(aLot.getLotFinishedState());
            /* ****************************/
            /*   Get lot process state   */
            /* ****************************/
            currentStatus.setProcessState(aLot.getLotProcessState());
            /* ******************************/
            /*   Get lot inventory state   */
            /* ******************************/
            currentStatus.setInventoryState(aLot.getLotInventoryState());
            /* ***********************/
            /*   Get lot isOnHold   */
            /* ***********************/
            lotStatusInfo.setOnHoldFlag(aLot.isOnHold());
            /* ************************/
            /*   Get lot isOnFloor   */
            /* ************************/
            lotStatusInfo.setOnFloorFlag(aLot.isOnFloor());
            /* ********************************************************/
            /*   Check lot InterFabXfer Plan  existence              */
            /* ********************************************************/
            log.info("Step2 - Check lot InterFabXfer Plan  existence");
            //------------------------------
            //  Get Lot current operation
            //------------------------------
            Outputs.ObjLotCurrentOperationInfoGetDROut objLotCurrentOperationInfoGetDROutRetCode = lotMethod.lotCurrentOperationInfoGetDR(objCommon, lotStatusInfo.getLotID());

            //--------------------------------------
            //  Get original FabID
            //--------------------------------------
            String orgFabID = StandardProperties.OM_SITE_ID.getValue();
            //--------------------------------------
            //  Check lot InterFab transfer plan
            //--------------------------------------
            Inputs.ObjInterFabXferPlanListGetDRIn objInterFabXferPlanListGetDRIn = new Inputs.ObjInterFabXferPlanListGetDRIn();
            Infos.InterFabLotXferPlanInfo strInterFabLotXferPlanInfo = new Infos.InterFabLotXferPlanInfo();
            objInterFabXferPlanListGetDRIn.setStrInterFabLotXferPlanInfo(strInterFabLotXferPlanInfo);
            strInterFabLotXferPlanInfo.setLotID(lotStatusInfo.getLotID());
            strInterFabLotXferPlanInfo.setOriginalFabID(orgFabID);
            strInterFabLotXferPlanInfo.setOriginalRouteID(objLotCurrentOperationInfoGetDROutRetCode.getMainPDID());
            strInterFabLotXferPlanInfo.setOriginalOpeNumber(objLotCurrentOperationInfoGetDROutRetCode.getOpeNo());
            Outputs.ObjInterFabXferPlanListGetDROut objInterFabXferPlanListGetDROut = null;
            try {
                objInterFabXferPlanListGetDROut = interFabMethod.interFabXferPlanListGetDR(objCommon, objInterFabXferPlanListGetDRIn);
            } catch (ServiceException e) {
                objInterFabXferPlanListGetDROut = e.getData(Outputs.ObjInterFabXferPlanListGetDROut.class);
                if (!Validations.isEquals(retCodeConfig.getInterfabNotFoundXferPlan(), e.getCode())) {
                    throw e;
                }
            }
            if (!CimObjectUtils.isEmpty(objInterFabXferPlanListGetDROut.getStrInterFabLotXferPlanInfoSeq())) {
                List<Infos.InterFabLotXferPlanInfo> strInterFabLotXferPlanInfoList = objInterFabXferPlanListGetDROut.getStrInterFabLotXferPlanInfoSeq();
                if (CimStringUtils.equals(strInterFabLotXferPlanInfoList.get(0).getState(), BizConstant.SP_INTERFAB_XFERPLANSTATE_CREATED)
                        || CimStringUtils.equals(strInterFabLotXferPlanInfoList.get(0).getState(), BizConstant.SP_INTERFAB_XFERPLANSTATE_CANCELED)) {
                    lotStatusInfo.setOnFloorFlag(true);
                    lotStatusInfo.setOnHoldFlag(true);
                }
            }
            /* ********************************************************/
            /*   Check lot state and set element number for sorting  */
            /* ********************************************************/
            log.info("Step3 - Check lot state and set element number for sorting");
            if (lotStatusInfo.isOnFloorFlag()) {
                /* *****************************************/
                /*   When lot is not Held and not InBank  */
                /* *****************************************/
                if (!lotStatusInfo.isOnHoldFlag()) {
                    waitingLotNumber.add(i);
                }
                /* ***************************/
                /*   When lot is held      */
                /* ***************************/
                else {
                    heldLotNumber.add(i);
                }
            }
            /* ***************************/
            /*   When lot is InBank     */
            /* ***************************/
            else {
                bankLotNumber.add(i);
            }
            i++;
        }
        /* *********************************************/
        /*  Set each lot count to return value        */
        /* *********************************************/
        objCassetteLotListGetWithPriorityOrderOut.setWaitingLotCount(CimArrayUtils.getSize(waitingLotNumber));
        objCassetteLotListGetWithPriorityOrderOut.setHoldLotCount(CimArrayUtils.getSize(heldLotNumber));
        objCassetteLotListGetWithPriorityOrderOut.setBankInLotCount(CimArrayUtils.getSize(bankLotNumber));
        /* *********************************************/
        /*  Check Lot in cassette count               */
        /*  If lot count is 1, then no need to sort.  */
        /* *********************************************/
        if (lotLen == 1) {
            /* ***********************/
            /*   Return to Caller   */
            /* ***********************/
            objCassetteLotListGetWithPriorityOrderOut.setLotStatusInfos(lotInfoList);
            return objCassetteLotListGetWithPriorityOrderOut;
        }
        /* **********************************/
        /*  Plural Lots exist in cassette  */
        /*  Sort is required.              */
        /* **********************************/
        List<Infos.LotStatusInfo> retLotInfoList = new ArrayList<>();
        for (MethodEnums.CassetteLotListGetWithPriorityOrderEnums enums : MethodEnums.CassetteLotListGetWithPriorityOrderEnums.values()) {
            List<Integer> currentLotNumber = new ArrayList<>();
            if (enums.name().equals("waiting")) {
                /* **********************************/
                /*                                 */
                /*   Firstly, sort waiting Lots    */
                /*                                 */
                /* **********************************/
                currentLotNumber = waitingLotNumber;
            } else if (enums.name().equals("hold")) {
                /* **********************************/
                /*                                 */
                /*   Secondly, sort hold Lots      */
                /*                                 */
                /* **********************************/
                currentLotNumber = heldLotNumber;
            } else if (enums.name().equals("bankIn")) {
                /* **********************************/
                /*                                 */
                /*   Finaly, sort bank Lots        */
                /*                                 */
                /* **********************************/
                currentLotNumber = bankLotNumber;
            }
            int currentLotLen = CimArrayUtils.getSize(currentLotNumber);
            if (currentLotLen > 1) {
                /* ******************************************************/
                /*               Sort by Lot priority                  */
                /*  Sorting fields;                                    */
                /*  1st field :  PriortyClass                          */
                /*  2nd field :  ExternalPriority                      */
                /*  3rd field :  InternalPriority                      */
                /* ******************************************************/
                List<Integer> tmpLotNumber = new ArrayList<>();
                int tmpUpCount = 0;
                for (i = 0; i < currentLotLen; i++) {
                    int highLotNumber = 0;
                    Boolean firstFlag = true;
                    for (int j = 0; j < currentLotLen; j++) {
                        int lotNum = currentLotNumber.get(j);
                        /* ********************************/
                        /*   Omit already checked Lot    */
                        /* ********************************/
                        Boolean alreadyCheckedFlag = false;
                        for (int assignedCnt = 0; assignedCnt < tmpUpCount; assignedCnt++) {
                            if (lotNum == tmpLotNumber.get(assignedCnt)) {
                                alreadyCheckedFlag = true;
                                break;
                            }
                        }
                        if (alreadyCheckedFlag) {
                            continue;
                        }
                        if (firstFlag) {
                            highLotNumber = lotNum;
                            firstFlag = false;
                        } else {
                            long nowPriorty = lotInfoList.get(lotNum).getPriorityClass();
                            long highPriorty = lotInfoList.get(highLotNumber).getPriorityClass();
                            if (highPriorty > nowPriorty) {
                                highLotNumber = lotNum;
                            } else if (highPriorty == nowPriorty) {
                                nowPriorty = lotInfoList.get(lotNum).getExternalPriority();
                                highPriorty = lotInfoList.get(highLotNumber).getExternalPriority();
                                if (highPriorty > nowPriorty) {
                                    highLotNumber = lotNum;
                                } else if (highPriorty == nowPriorty) {
                                    nowPriorty = (long) lotInfoList.get(lotNum).getInternalPriority();
                                    highPriorty = (long) lotInfoList.get(highLotNumber).getInternalPriority();
                                    if (highPriorty > nowPriorty) {
                                        highLotNumber = lotNum;
                                    }
                                }
                            }
                        }
                    }
                    tmpLotNumber.add(highLotNumber);
                    tmpUpCount++;
                }
                if (enums.name().equals("bankIn")) {
                    for (Integer num : tmpLotNumber) {
                        retLotInfoList.add(lotInfoList.get(num));
                    }
                    continue;
                }
                currentLotNumber = tmpLotNumber;
                /* ******************************/
                /*                             */
                /*  Prepare Qtime structure.   */
                /*                             */
                /* ******************************/
                log.info("Step4 - Prepare Qtime structure");
                List<Long> lotQtimeRemaintimeList = new ArrayList<>();
                for (i = 0; i < lotLen; i++) {
                    Long tempLotQtimeRemaintime = 0L;
                    if ((enums.name().equals("waiting") && lotInfoList.get(i).isOnFloorFlag() && !lotInfoList.get(i).isOnHoldFlag())
                            || (enums.name().equals("hold") && lotInfoList.get(i).isOnFloorFlag() && lotInfoList.get(i).isOnHoldFlag())) {
                        CimLot aLot = (CimLot) aLotList.get(i);
                        List<ProductDTO.QTimeInformation> aQTimeInfoList = aLot.getQTimeInfo();
                        tempLotQtimeRemaintime = Long.MAX_VALUE;
                        int qTimeLen = CimArrayUtils.getSize(aQTimeInfoList);
                        Long nowRemainTime = 0L;
                        Boolean firstOneFlag = true;
                        for (int Qcnt = 0; Qcnt < qTimeLen; Qcnt++) {
                            if (CimStringUtils.equals(aQTimeInfoList.get(Qcnt).getQrestrictionTargetTimeStamp(), BizConstant.SP_TIMESTAMP_NIL_OBJECT_STRING)) {
                                continue;
                            }
                            nowRemainTime = CimDateUtils.convertToOrInitialTime(aQTimeInfoList.get(Qcnt).getQrestrictionRemainTime()).getTime() / 1000;
                            if (firstOneFlag) {
                                tempLotQtimeRemaintime = nowRemainTime;
                                firstOneFlag = false;
                            } else {
                                if (tempLotQtimeRemaintime > nowRemainTime) {
                                    tempLotQtimeRemaintime = nowRemainTime;
                                }
                            }
                        }
                        int qtimeSeqForWaferCount = aLot.getWaferLevelQTimeCount();
                        if (qtimeSeqForWaferCount > 0) {
                            List<CimQTimeRestriction> qtimeSeqForWafer = aLot.allQTimeRestrictionsWithWaferLevelQTime();
                            if (!CimObjectUtils.isEmpty(qtimeSeqForWafer)) {
                                for (CimQTimeRestriction aQtime : qtimeSeqForWafer) {
                                    if (aQtime == null) {
                                        continue;
                                    }
                                    ProcessDTO.QTimeRestrictionInfo aQTimeInfo = aQtime.getQTimeRestrictionInfo();
                                    if (aQTimeInfo == null || CimObjectUtils.isEmpty(aQTimeInfo.getTriggerOperationNumber())) {
                                        continue;
                                    }
                                    if (CimStringUtils.equals(aQTimeInfo.getTargetTimeStamp(), BizConstant.SP_TIMESTAMP_NIL_OBJECT_STRING)) {
                                        continue;
                                    }
                                    String aTimeStamp = aQTimeInfo.getTargetTimeStamp();
                                    Long aDuration = CimDateUtils.convertToOrInitialTime(aTimeStamp).getTime() - objCommon.getTimeStamp().getReportTimeStamp().getTime();
                                    Double remainSeconds = (double) aDuration / 1000;
                                    if (CimDateUtils.compare(objCommon.getTimeStamp().getReportTimeStamp(), aQTimeInfo.getTargetTimeStamp()) > 0) {
                                        remainSeconds = -1 * remainSeconds;
                                    }
                                    nowRemainTime = Math.round(remainSeconds);
                                    if (firstOneFlag) {
                                        tempLotQtimeRemaintime = nowRemainTime;
                                        firstOneFlag = false;
                                    } else {
                                        if (tempLotQtimeRemaintime > nowRemainTime) {
                                            tempLotQtimeRemaintime = nowRemainTime;
                                        }
                                    }
                                }
                            }
                        }
                    }
                    lotQtimeRemaintimeList.add(tempLotQtimeRemaintime);
                }
                /* ******************************/
                /*                             */
                /*   Check Qtime remain time   */
                /*                             */
                /* ******************************/
                log.info("Step5 - heck Qtime remain time");
                for (i = 0; i < currentLotLen; i++) {
                    int highLotNumber = 0;
                    Boolean firstFlag = true;
                    Long highRemainTime = 0L;
                    for (int j = 0; j < currentLotLen; j++) {
                        int lotNum = currentLotNumber.get(j);
                        /* *******************************/
                        /*   Omit already checked Lot   */
                        /* *******************************/
                        Boolean alreadyCheckedFlag = false;
                        for (int assignedCnt = 0; assignedCnt < retLotInfoList.size(); assignedCnt++) {
                            if (ObjectIdentifier.equalsWithValue(lotInfoList.get(lotNum).getLotID(), retLotInfoList.get(assignedCnt).getLotID())) {
                                alreadyCheckedFlag = true;
                                break;
                            }
                        }
                        if (alreadyCheckedFlag) {
                            continue;
                        }

                        /* ******************************/
                        /*   Check Qtime remainTime    */
                        /* ******************************/
                        if (firstFlag) {
                            highRemainTime = lotQtimeRemaintimeList.get(lotNum);
                            highLotNumber = lotNum;
                            firstFlag = false;
                        } else {
                            if (highRemainTime > lotQtimeRemaintimeList.get(lotNum)) {
                                highRemainTime = lotQtimeRemaintimeList.get(lotNum);
                                highLotNumber = lotNum;
                            }
                        }
                    }
                    retLotInfoList.add(lotInfoList.get(highLotNumber));
                }
            } else if (currentLotLen == 1) {
                retLotInfoList.add(lotInfoList.get(currentLotNumber.get(0)));
            } else {
                ; //Do nothing...
            }
        }
        /* ***************************/
        /*   Set return structure   */
        /* ***************************/
        Validations.check(lotLen != retLotInfoList.size(), retCodeConfig.getInvalidDataContents());
        objCassetteLotListGetWithPriorityOrderOut.setLotStatusInfos(retLotInfoList);
        return objCassetteLotListGetWithPriorityOrderOut;
    }

    @Override
    public Infos.CarrierJobResult cassetteTransferJobRecordGetDR(Infos.ObjCommon objCommon, ObjectIdentifier cassetteID) {
        String sql = "SELECT CARRIER_JOB_ID,\n" +
                "                        CARRIER_JOB_STATUS,\n" +
                "                        CARRIER_ID,\n" +
                "                        ZONE_TYPE,\n" +
                "                        N2PURGE_FLAG,\n" +
                "                        ORIG_MACHINE_ID,\n" +
                "                        ORIG_PORT_ID,\n" +
                "                        TO_STOCKER_GROUP,\n" +
                "                        DEST_MACHINE_ID,\n" +
                "                        DEST_PORT_ID,\n" +
                "                        EXP_START_TIME,\n" +
                "                        EXP_END_TIME,\n" +
                "                        MANDATORY_FLAG,\n" +
                "                        PRIORITY,\n" +
                "                        EST_START_TIME,\n" +
                "                        EST_END_TIME\n" +
                "            FROM   OTXFERREQ\n" +
                "            WHERE  CARRIER_ID=?";
        List<Object[]> queryResultList = cimJpaRepository.query(sql, cassetteID.getValue());
        Infos.CarrierJobResult carrierJobResult = new Infos.CarrierJobResult();
        if (!CimObjectUtils.isEmpty(queryResultList)) {
            Object[] queryResult = queryResultList.get(0);
            carrierJobResult.setCarrierJobID((String) queryResult[0]);
            carrierJobResult.setCarrierJobStatus((String) queryResult[1]);
            carrierJobResult.setCarrierID(ObjectIdentifier.buildWithValue((String) queryResult[2]));
            carrierJobResult.setZoneType((String) queryResult[3]);
            carrierJobResult.setN2PurgeFlag((Integer.parseInt(String.valueOf(queryResult[4]))) > 0);
            carrierJobResult.setFromMachineID(ObjectIdentifier.buildWithValue((String) queryResult[5]));
            carrierJobResult.setFromPortID(ObjectIdentifier.buildWithValue((String) queryResult[6]));
            carrierJobResult.setToStockerGroup((String) queryResult[7]);
            carrierJobResult.setToMachine(ObjectIdentifier.buildWithValue((String) queryResult[8]));
            carrierJobResult.setToPortID(ObjectIdentifier.buildWithValue((String) queryResult[9]));
            carrierJobResult.setExpectedStartTime((String) queryResult[10]);
            carrierJobResult.setEstimatedEndTime((String) queryResult[11]);
            carrierJobResult.setMandatoryFlag((Integer.parseInt(String.valueOf(queryResult[12]))) > 0);
            carrierJobResult.setPriority((String) queryResult[13]);
            carrierJobResult.setEstimatedStartTime((String) queryResult[14]);
            carrierJobResult.setEstimatedEndTime((String) queryResult[15]);
        } else {
            throw new ServiceException(new OmCode(retCodeConfig.getCarrierNotTransfering(), cassetteID.getValue()));
        }
        return carrierJobResult;
    }

    @Override
    public Outputs.ObjCassetteTransferInfoGetDROut cassetteTransferInfoGetDR(Infos.ObjCommon objCommon, ObjectIdentifier cassetteID) {
        Outputs.ObjCassetteTransferInfoGetDROut objCassetteTransferInfoGetDROut = new Outputs.ObjCassetteTransferInfoGetDROut();
        if (!SorterHandler.containsFOSB(cassetteID)) {
            CimCassetteDO cimCassetteExample = new CimCassetteDO();
            cimCassetteExample.setCassetteID(ObjectIdentifier.fetchValue(cassetteID));
            List<CimCassetteDO> cassetteList = cimJpaRepository.findAll(Example.of(cimCassetteExample));
            Validations.check(CimObjectUtils.isEmpty(cassetteList), retCodeConfig.getNotFoundCassette());
            CimCassetteDO cimCassetteDO = cassetteList.get(0);
            //----- Set out structure -----//
            String transferState = cimCassetteDO.getTransferState();
            objCassetteTransferInfoGetDROut.setTransferStatus(transferState);
            if (transferState.startsWith("E")) {
                objCassetteTransferInfoGetDROut.setEquipmentID(new ObjectIdentifier(cimCassetteDO.getEquipmentID()));
            } else {
                objCassetteTransferInfoGetDROut.setStockerID(new ObjectIdentifier(cimCassetteDO.getEquipmentID()));
            }
        }
        return objCassetteTransferInfoGetDROut;
    }

    @Override
    public Outputs.ObjCassetteEquipmentIDGetOut cassetteEquipmentIDGet(Infos.ObjCommon objCommon, ObjectIdentifier cassetteID) {
        // step 1 - Get cassette Object
        com.fa.cim.newcore.bo.durable.CimCassette cassette = baseCoreFactory.getBO(com.fa.cim.newcore.bo.durable.CimCassette.class, cassetteID);
        Validations.check(cassette == null, retCodeConfig.getNotFoundCassette());
        Machine equipment = cassette.currentAssignedMachine();
        Outputs.ObjCassetteEquipmentIDGetOut objCassetteEquipmentIDGetOut = new Outputs.ObjCassetteEquipmentIDGetOut();
        objCassetteEquipmentIDGetOut.setEquipmentID(new ObjectIdentifier("", ""));
        if (!CimObjectUtils.isEmpty(equipment)) {
            Boolean isStorageBool = equipment.isStorageMachine();
            if (CimBooleanUtils.isTrue(isStorageBool)) {
                log.info("isStorageBool is TRUE: Storage");
            } else {
                objCassetteEquipmentIDGetOut.setEquipmentID(new ObjectIdentifier(equipment.getIdentifier(), equipment.getPrimaryKey()));
                log.info("isStorageBool is not TRUE: Equipment");
            }
        } else {
            log.info("aMachine is Nill");
        }
        return objCassetteEquipmentIDGetOut;
    }

    @Override
    public Infos.LotListInCassetteInfo cassetteLotListGetDR(Infos.ObjCommon objCommon, ObjectIdentifier cassetteID) {
        Infos.LotListInCassetteInfo lotListInCassetteInfo = new Infos.LotListInCassetteInfo();
        CimCassetteDO cimCassetteExample = new CimCassetteDO();
        cimCassetteExample.setCassetteID(ObjectIdentifier.fetchValue(cassetteID));
        CimCassetteDO cassette = cimJpaRepository.findOne(Example.of(cimCassetteExample)).orElse(null);
        Validations.check(CimObjectUtils.isEmpty(cassette), retCodeConfig.getNotFoundCassette());
        lotListInCassetteInfo.setCassetteID(new ObjectIdentifier(cassette.getCassetteID(), cassette.getId()));
        lotListInCassetteInfo.setMultiLotType(cassette.getMultiLotType());

        /*------------------------------------------------------*/
        /*  Get Lot information in Cassette  from OMCARRIER_LOT    */
        /*------------------------------------------------------*/
        CimCassetteLotDO example = new CimCassetteLotDO();
        example.setReferenceKey(cassette.getId());
        List<CimCassetteLotDO> cassetteLots = cimJpaRepository.findAll(Example.of(example));
        if (!CimObjectUtils.isEmpty(cassetteLots)) {
            lotListInCassetteInfo.setLotIDList(cassetteLots.stream()
                    .map(x -> new ObjectIdentifier(x.getLotID(), x.getLotObj()))
                    .collect(Collectors.toList()));
        }
        return lotListInCassetteInfo;
    }

    @Override
    public void cassetteDispatchAttributeUpdate(Infos.ObjCommon objCommon, ObjectIdentifier cassetteID, Boolean setFlag, String actionCode) {

        CimCassette aCassette = baseCoreFactory.getBO(CimCassette.class, cassetteID);

        /*------------------------------------------------------------------------*/
        /* Case1. If setFlag is on, update the Dispatch Attribute                      */
        /*------------------------------------------------------------------------*/
        if (CimBooleanUtils.isTrue(setFlag)) {
            // Check String
            if ((!CimStringUtils.equals(actionCode, BizConstant.SP_SORTER_READ)) &&
                    (!CimStringUtils.equals(actionCode, BizConstant.SP_SORTER_START)) &&
                    (!CimStringUtils.equals(actionCode, BizConstant.SP_SORTER_MINIREAD)) &&
                    (!CimStringUtils.equals(actionCode, BizConstant.SP_SORTER_POSITIONCHANGE)) &&
                    (!CimStringUtils.equals(actionCode, BizConstant.SP_SORTER_LOTTRANSFEROFF)) &&
                    (!CimStringUtils.equals(actionCode, BizConstant.SP_SORTER_SEPARATEOFF)) &&
                    (!CimStringUtils.equals(actionCode, BizConstant.SP_SORTER_COMBINEOFF)) &&
                    (!CimStringUtils.equals(actionCode, BizConstant.SP_SORTER_JUSTIN)) &&
                    (!CimStringUtils.equals(actionCode, BizConstant.SP_SORTER_JUSTOUT)) &&
                    (!CimStringUtils.equals(actionCode, BizConstant.SP_SORTER_WAFERENDOFF)) &&
                    (!CimStringUtils.equals(actionCode, BizConstant.SP_SORTER_SCRAP)) &&
                    (!CimStringUtils.equals(actionCode, BizConstant.SP_SORTER_VENDLOT_R_AND_P))) {
                Validations.check(retCodeConfigEx.getInvalidCastDispAttr());
            }

            // Set DispatchAttributeFlag
            aCassette.makeDispatchAttributeFlagOn();

            //set Action Code
            aCassette.setDispatchAttributeString(actionCode);
        }

        /*------------------------------------------------------------------------*/
        /* Case2. If setFlag is off, delete the Dispatch Attribute                */
        /*------------------------------------------------------------------------*/
        else {
            aCassette.makeDispatchAttributeFlagOff();

            // Reset Action Code
            aCassette.setDispatchAttributeString("");
        }
    }

    @Override
    public void cassetteStatusFinalizeForPostProcess(Infos.ObjCommon objCommon, ObjectIdentifier cassetteID) {
        if (!ObjectIdentifier.isEmptyWithValue(cassetteID)) {
            //----------------------
            //   Update cassette MultiLotType
            //----------------------
            String multiLotType = this.cassetteMultiLotTypeGet(objCommon, cassetteID);
            //----------------------
            //   Set cassette's PostProcessFlag to FALSE
            //----------------------
            int ppChainMode = StandardProperties.OM_PP_CHAIN_FLAG.getIntValue();
            //----------------------
            //   Set cassette's PostProcessFlag to FALSE
            //----------------------
            Inputs.ObjCassetteInPostProcessFlagSetIn in = new Inputs.ObjCassetteInPostProcessFlagSetIn();
            in.setCassetteID(cassetteID);
            in.setInPostProcessFlag(false);
            if (ppChainMode == 1) {
                boolean lotPostProcessingFlag = postProcessMethod.postProcessLastFlagForCarrierGetDR(objCommon, cassetteID);
                in.setInPostProcessFlag(lotPostProcessingFlag);
            }
            this.cassetteInPostProcessFlagSet(objCommon, in);
        }
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param strObjCommonIn
     * @param cassetteID
     * @return com.fa.cim.common.support.ObjectIdentifier
     * @throws
     * @author ho
     * @date 2020/3/17 13:44
     */
    @Override
    public ObjectIdentifier cassetteUTSInfoGet(Infos.ObjCommon strObjCommonIn, ObjectIdentifier cassetteID) {
        if (CimStringUtils.length(cassetteID.getValue()) == 0) {
            Validations.check(true, retCodeConfig.getInvalidDataContents());
        } else {
            log.info("in param cassetteID {}", cassetteID.getValue());
        }

        /*---------------------------------------------*/
        /*  Get carrier location information           */
        /*---------------------------------------------*/
        // step-1 cassette_LocationInfo_GetDR
        Infos.LotLocationInfo strCassetteLocationInfoGetDROut, cassetteLocationInfo;
        cassetteLocationInfo = strCassetteLocationInfoGetDROut = cassetteLocationInfoGetDR(
                cassetteID);

        /*---------------------------------------------*/
        /*  Verify if the carrier is in a UTS          */
        /*---------------------------------------------*/
        ObjectIdentifier stockerID;
        stockerID = cassetteLocationInfo.getStockerID();

        if (CimStringUtils.length(stockerID.getValue()) != 0) {

            CimStorageMachine aStorageMachine = baseCoreFactory.getBO(CimStorageMachine.class, stockerID);

            boolean aBool = false;
            aBool = aStorageMachine.isUTSStocker();

            Validations.check(!aBool, retCodeConfigEx.getCarrierNotInUts());
        } else {
            // stockerID is empty, the cassette is not in any stockers
            Validations.check(true, retCodeConfigEx.getCarrierNotInUts());
        }

        /*---------------------------------------------*/
        /*  Set return structure                       */
        /*---------------------------------------------*/

        /*------------------------------------------------------------------------*/
        /*   Return                                                               */
        /*------------------------------------------------------------------------*/
        return stockerID;
    }

    @Override
    public List<Infos.ApcBaseCassette> cassetteAPCInformationGetDR(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, List<Infos.StartCassette> strStartCassette) {
        /*---------------------------*/
        /*   Set strStartOperation   */
        /*---------------------------*/
        List<Infos.StartCassette> tmpStartCassette = operationMethod.operationStartCassetteSet(objCommon, strStartCassette);
        List<Infos.ApcBaseCassette> strAPCBaseCassette = new ArrayList<>();
        /*---------------------------------*/
        /*   Set strAPCBaseCassette       */
        /*---------------------------------*/
        int scLen = CimArrayUtils.getSize(tmpStartCassette);
        for (int i = 0; i < scLen; i++) {
            Infos.ApcBaseCassette apcBaseCassette = new Infos.ApcBaseCassette();
            strAPCBaseCassette.add(apcBaseCassette);
            apcBaseCassette.setCassetteID(ObjectIdentifier.fetchValue(tmpStartCassette.get(i).getCassetteID()));
            apcBaseCassette.setLoadSequenceNumber(String.valueOf(tmpStartCassette.get(i).getLoadSequenceNumber()));
            apcBaseCassette.setLoadPortID(ObjectIdentifier.fetchValue(tmpStartCassette.get(i).getLoadPortID()));
            apcBaseCassette.setUnloadPortID(ObjectIdentifier.fetchValue(tmpStartCassette.get(i).getUnloadPortID()));
            List<Infos.LotInCassette> lotInCassetteList = tmpStartCassette.get(i).getLotInCassetteList();
            int licLen = CimArrayUtils.getSize(lotInCassetteList);
            List<Infos.ApcBaseLot> apcBaseLotList = new ArrayList<>();
            apcBaseCassette.setApcBaseLotList(apcBaseLotList);
            for (int j = 0; j < licLen; j++) {
                /*----------------------------*/
                /*   Set strAPCBaseLot        */
                /*----------------------------*/
                Infos.ApcBaseLot apcBaseLot = new Infos.ApcBaseLot();
                apcBaseLotList.add(apcBaseLot);
                apcBaseLot.setOperationStartFlag(lotInCassetteList.get(j).getMoveInFlag());
                apcBaseLot.setMonitorLotFlag(lotInCassetteList.get(j).getMonitorLotFlag());
                apcBaseLot.setSendAheadType("");
                apcBaseLot.setSpecialInstructionID("");
                apcBaseLot.setLotID(ObjectIdentifier.fetchValue(lotInCassetteList.get(j).getLotID()));
                apcBaseLot.setLotType(lotInCassetteList.get(j).getLotType());
                apcBaseLot.setSubLotType(lotInCassetteList.get(j).getSubLotType());
                if (null != lotInCassetteList.get(j).getStartRecipe()) {
                    apcBaseLot.setLogicalRecipeID(ObjectIdentifier.fetchValue(lotInCassetteList.get(j).getStartRecipe().getLogicalRecipeID()));
                    apcBaseLot.setMachineRecipeID(ObjectIdentifier.fetchValue(lotInCassetteList.get(j).getStartRecipe().getMachineRecipeID()));
                    apcBaseLot.setPhysicalRecipeID(lotInCassetteList.get(j).getStartRecipe().getPhysicalRecipeID());
                }
                apcBaseLot.setProductID(ObjectIdentifier.fetchValue(lotInCassetteList.get(j).getProductID()));
                if (null != lotInCassetteList.get(j).getStartOperationInfo()) {
                    apcBaseLot.setRouteID(ObjectIdentifier.fetchValue(lotInCassetteList.get(j).getStartOperationInfo().getProcessFlowID()));
                    apcBaseLot.setOperationID(ObjectIdentifier.fetchValue(lotInCassetteList.get(j).getStartOperationInfo().getOperationID()));
                    apcBaseLot.setOperationPassCount(CimNumberUtils.longValue(lotInCassetteList.get(j).getStartOperationInfo().getPassCount()));
                }

                /*-----------------------------*/
                /*   Set Reticle information   */
                /*-----------------------------*/
                List<Infos.ApcBaseReticle> apcBaseReticleList = new ArrayList<>();
                apcBaseLot.setApcBaseReticleList(apcBaseReticleList);
                List<Infos.StartReticleInfo> startReticleList = new ArrayList<>();
                if (null != lotInCassetteList.get(j).getStartRecipe()) {
                    startReticleList = lotInCassetteList.get(j).getStartRecipe().getStartReticleList();
                }
                int rLen = CimArrayUtils.getSize(startReticleList);
                if (rLen <= 0) {
                    /*-----------------------------------------*/
                    /*   Check Process Durable Required Flag   */
                    /*-----------------------------------------*/
                    try {
                        equipmentMethod.equipmentProcessDurableRequiredFlagGet(objCommon, equipmentID);
                    } catch (ServiceException e) {
                        if (Validations.isEquals(retCodeConfig.getEquipmentProcessDurableReticleRequired(), e.getCode())) {
                            if (!CimStringUtils.equals(tmpStartCassette.get(i).getLoadPurposeType(), BizConstant.SP_LOADPURPOSETYPE_EMPTYCASSETTE)
                                    && lotInCassetteList.get(j).getMoveInFlag()) {
                                /*--------------------------------------------------*/
                                /*   Check Process Durable Condition for OpeStart   */
                                /*--------------------------------------------------*/
                                Outputs.ObjProcessDurableCheckConditionForOperationStartOut objProcessDurableCheckConditionForOperationStartOut = processMethod.processDurableCheckConditionForOpeStart(objCommon, equipmentID, lotInCassetteList.get(j).getStartRecipe().getLogicalRecipeID(),
                                        lotInCassetteList.get(j).getStartRecipe().getMachineRecipeID(), lotInCassetteList.get(j).getLotID());
                                /*------------------------------*/
                                /*   Set Available Reticles     */
                                /*------------------------------*/
                                lotInCassetteList.get(j).getStartRecipe().setStartReticleList(objProcessDurableCheckConditionForOperationStartOut.getStartReticleList());
                            }
                        } else if (Validations.isEquals(retCodeConfig.getEquipmentProcessDurableFixtRequired(), e.getCode())
                                || Validations.isEquals(retCodeConfig.getEquipmentProcessDurableNotRequired(), e.getCode())) {
                            log.info("rc == RC_EQP_PROCDRBL_FIXT_REQD || rc == RC_EQP_PROCDRBL_NOT_REQD");
                        } else {
                            throw e;
                        }
                    }
                }
                rLen = CimArrayUtils.getSize(startReticleList);
                for (int k = 0; k < rLen; k++) {
                    Infos.StartReticleInfo startReticleInfo = lotInCassetteList.get(j).getStartRecipe().getStartReticleList().get(k);
                    Infos.ApcBaseReticle apcBaseReticle = new Infos.ApcBaseReticle();
                    apcBaseReticleList.add(apcBaseReticle);
                    apcBaseReticle.setReticleID(startReticleInfo.getReticleID().getValue());
                    String sql = "SELECT OMPDRBL_PDRBLGRP.PDRBL_GRP_ID\n" +
                            "                          FROM  OMPDRBL, OMPDRBL_PDRBLGRP\n" +
                            "                         WHERE  OMPDRBL.PDRBL_ID = ?\n" +
                            "                           AND  OMPDRBL.ID = OMPDRBL_PDRBLGRP.REFKEY";
                    CimDurableDurableGroupDO cimDurableDurableGroupDO = cimJpaRepository.queryOne(sql, CimDurableDurableGroupDO.class,
                            ObjectIdentifier.fetchValue(startReticleInfo.getReticleID()));
                    if (cimDurableDurableGroupDO != null) {
                        apcBaseReticle.setGroupID(cimDurableDurableGroupDO.getDurableGroupId());
                    }
                }

                CimLot aLot = baseCoreFactory.getBO(CimLot.class, lotInCassetteList.get(j).getLotID());
                Validations.check(aLot == null, new OmCode(retCodeConfig.getNotFoundLot(), lotInCassetteList.get(j).getLotID().getValue()));
                ProductDTO.LotBaseInfo aLotBaseInfo = aLot.getLotBaseInfo();
                apcBaseLot.setLotStatus(aLotBaseInfo.getRepresentativeState());
                apcBaseLot.setDueTimeStamp(aLotBaseInfo.getPlanEndTimeStamp());
                apcBaseLot.setPriorityClass(String.valueOf(aLotBaseInfo.getPriorityClass()));
                apcBaseLot.setInternalPriority(String.valueOf(aLotBaseInfo.getInternalPriority()));
                apcBaseLot.setExternalPriority(String.valueOf(aLotBaseInfo.getExternalPriority()));
                apcBaseLot.setTotalWaferCount(aLotBaseInfo.getTotalWaferCount());
                apcBaseLot.setProductWaferCount(aLotBaseInfo.getProductWaferCount());
                apcBaseLot.setControlWaferCount(aLotBaseInfo.getControlWaferCount());
                apcBaseLot.setLastClaimedTimeStamp(aLotBaseInfo.getClaimedTimeStamp());
                apcBaseLot.setLastClaimedUserID(aLotBaseInfo.getClaimUSerID().getValue());
                apcBaseLot.setStateChangeTimeStamp(aLotBaseInfo.getStateChangedTimeStamp());
                apcBaseLot.setOperationNumber(aLotBaseInfo.getOpeNo());
                apcBaseLot.setQualityCode(aLotBaseInfo.getQueuedTimeStamp());

                List<ProductDTO.QTimeInformation> aQTimeInfoList = aLot.getQTimeInfo();
                if (!CimArrayUtils.isEmpty(aQTimeInfoList)) {
                    apcBaseLot.setQTimeFlag(true);
                } else {
                    apcBaseLot.setQTimeFlag(false);
                    if (CimStringUtils.equals(objCommon.getTransactionID(), TransactionIDEnum.PARTIAL_OPERATION_COMP_WITH_DATA_REQ.getValue())
                            || CimStringUtils.equals(objCommon.getTransactionID(), TransactionIDEnum.PARTIAL_OPERATION_COMP_FOR_INTERNAL_BUFFER_REQ.getValue())) {
                        List<CimQTimeRestriction> qtimeSeqForWafer = aLot.allQTimeRestrictionsForWaferLevelQTime();
                        if (!CimArrayUtils.isEmpty(qtimeSeqForWafer)) {
                            apcBaseLot.setQTimeFlag(true);
                        }
                    } else {
                        int qtimeSeqForWaferCount = aLot.getWaferLevelQTimeCount();
                        if (qtimeSeqForWaferCount > 0) {
                            apcBaseLot.setQTimeFlag(true);
                        }
                    }
                }

                Outputs.ObjLotProductProcessOperationInfoGetOut objLotProductProcessOperationInfoGetOut = lotMethod.lotProductProcessOperationInfoGet(objCommon, lotInCassetteList.get(j).getLotID());
                apcBaseLot.setProductType(objLotProductProcessOperationInfoGetOut.getStrLotProductInfo().getProductType());
                apcBaseLot.setProductGroupID(ObjectIdentifier.fetchValue(objLotProductProcessOperationInfoGetOut.getStrLotProductInfo().getProductGroupID()));
                apcBaseLot.setTechnologyCode(objLotProductProcessOperationInfoGetOut.getStrLotProductInfo().getTechnologyCode());
                apcBaseLot.setOperationName(objLotProductProcessOperationInfoGetOut.getStrProcessOperationInformation().getOperationName());
                apcBaseLot.setStageID(ObjectIdentifier.fetchValue(objLotProductProcessOperationInfoGetOut.getStrProcessOperationInformation().getStageID()));
                apcBaseLot.setMaskLevel(objLotProductProcessOperationInfoGetOut.getStrProcessOperationInformation().getMaskLevel());
                apcBaseLot.setDepartment(objLotProductProcessOperationInfoGetOut.getStrProcessOperationInformation().getDepartment());
                apcBaseLot.setPlanStartTimeStamp(objLotProductProcessOperationInfoGetOut.getStrProcessOperationInformation().getPlanStartTimeStamp());
                apcBaseLot.setPlanEndTimeStamp(objLotProductProcessOperationInfoGetOut.getStrProcessOperationInformation().getPlanEndTimeStamp());
                apcBaseLot.setTestSpecID(ObjectIdentifier.fetchValue(objLotProductProcessOperationInfoGetOut.getStrProcessOperationInformation().getTestSpecID()));
                apcBaseLot.setInspectionType(objLotProductProcessOperationInfoGetOut.getStrProcessOperationInformation().getInspectionType());
                apcBaseLot.setReworkCount(CimNumberUtils.longValue(objLotProductProcessOperationInfoGetOut.getStrProcessOperationInformation().getReworkCount()));
                apcBaseLot.setExperimentSplitLot("false");
                apcBaseLot.setTotalGoodDieCount(0L);
                apcBaseLot.setTotalBadDieCount(0L);

                /*---------------------------*/
                /*   Set Wafer information   */
                /*---------------------------*/
                CimMachine aMachine = null;
                if (!ObjectIdentifier.isEmptyWithValue(equipmentID)) {
                    aMachine = baseCoreFactory.getBO(CimMachine.class, equipmentID);
                }
                CimLogicalRecipe aLogicalRecipe = null;
                if (!CimObjectUtils.isEmpty(lotInCassetteList.get(j).getStartRecipe())) {
                    if (!ObjectIdentifier.isEmptyWithValue(lotInCassetteList.get(j).getStartRecipe().getLogicalRecipeID())) {
                        aLogicalRecipe = baseCoreFactory.getBO(CimLogicalRecipe.class, lotInCassetteList.get(j).getStartRecipe().getLogicalRecipeID());
                    }
                }
                CimMachineRecipe aMachineRecipe = null;
                if (!CimObjectUtils.isEmpty(lotInCassetteList.get(j).getStartRecipe())) {
                    if (!ObjectIdentifier.isEmptyWithValue(lotInCassetteList.get(j).getStartRecipe().getMachineRecipeID())) {
                        aMachineRecipe = baseCoreFactory.getBO(CimMachineRecipe.class, lotInCassetteList.get(j).getStartRecipe().getMachineRecipeID());
                    }
                }
                List<RecipeDTO.RecipeParameter> aRecipeParameters = new ArrayList<>();
                if (aLogicalRecipe != null && aMachineRecipe != null) {
                    aRecipeParameters = aLogicalRecipe.findRecipeParametersForSubLotType(aMachine, aMachineRecipe, apcBaseLot.getSubLotType());
                }
                List<Infos.LotWafer> lotWaferList = lotInCassetteList.get(j).getLotWaferList();

                List<Infos.ApcBaseWafer> apcBaseWaferList = new ArrayList<>();
                apcBaseLot.setApcBaseWaferList(apcBaseWaferList);
                int wLen = CimArrayUtils.getSize(lotWaferList);
                int aRPLen = CimArrayUtils.getSize(aRecipeParameters);
                for (int k = 0; k < wLen; k++) {
                    Infos.ApcBaseWafer apcBaseWafer = new Infos.ApcBaseWafer();
                    apcBaseWaferList.add(apcBaseWafer);
                    apcBaseWafer.setWaferID(lotWaferList.get(k).getWaferID().getValue());
                    apcBaseWafer.setSlotNumber(lotWaferList.get(k).getSlotNumber());
                    apcBaseWafer.setControlWaferFlag(CimBooleanUtils.isTrue(lotWaferList.get(k).getControlWaferFlag()));
                    apcBaseWafer.setProcessFlag(CimBooleanUtils.isTrue(lotWaferList.get(k).getProcessJobExecFlag()));

                    /*-------------------------------------*/
                    /*   Set RecipeParameter information   */
                    /*-------------------------------------*/
                    List<Infos.StartRecipeParameter> startRecipeParameterList = lotWaferList.get(k).getStartRecipeParameterList();

                    List<Infos.ApcBaseRecipeParameter> apcBaseRecipeParameterList = new ArrayList<>();
                    apcBaseWafer.setApcBaseRecipeParameterList(apcBaseRecipeParameterList);
                    int rpLen = CimArrayUtils.getSize(startRecipeParameterList);
                    for (int l = 0; l < rpLen; l++) {
                        Infos.ApcBaseRecipeParameter apcBaseRecipeParameter = new Infos.ApcBaseRecipeParameter();
                        apcBaseRecipeParameterList.add(apcBaseRecipeParameter);
                        apcBaseRecipeParameter.setName(startRecipeParameterList.get(l).getParameterName());
                        apcBaseRecipeParameter.setValue(startRecipeParameterList.get(l).getParameterValue());
                        apcBaseRecipeParameter.setUseCurrentValueFlag(startRecipeParameterList.get(l).getUseCurrentSettingValueFlag());
                        apcBaseRecipeParameter.setValueLowerLimit("");
                        apcBaseRecipeParameter.setValueUpperLimit("");
                        boolean bRcpParamFound = false;
                        int x = 0;
                        if (l < aRPLen && CimStringUtils.equals(aRecipeParameters.get(l).getParameterName(), startRecipeParameterList.get(l).getParameterName())) {
                            bRcpParamFound = true;
                            x = l;
                        } else {
                            for (x = 0; x < aRPLen; x++) {
                                if (CimStringUtils.equals(aRecipeParameters.get(x).getParameterName(), startRecipeParameterList.get(l).getParameterName())) {
                                    bRcpParamFound = true;
                                    break;
                                }
                            }
                        }
                        if (bRcpParamFound) {
                            apcBaseRecipeParameter.setValueLowerLimit(aRecipeParameters.get(x).getLowerLimit());
                            apcBaseRecipeParameter.setValueUpperLimit(aRecipeParameters.get(x).getUpperLimit());
                        }
                    }
                    apcBaseWafer.setSendAheadWaferFlag(false);
                    apcBaseWafer.setExperimentSplitWafer(false);

                }
            }
        }
        return strAPCBaseCassette;
    }

    @Override
    public void cassetteCheckConditionForBondingGroup(Infos.ObjCommon objCommon, List<ObjectIdentifier> cassetteIDs) {
        Optional.ofNullable(cassetteIDs).ifPresent(list -> list.forEach(cassetteID -> {
            CimCassette cimCassette = baseCoreFactory.getBO(CimCassette.class, cassetteID);
            Validations.check(cimCassette == null, retCodeConfig.getNotFoundCassette());
            String castID = cimCassette.getIdentifier();

            String transportState = cimCassette.getTransportState();
            Validations.check(CimStringUtils.equals(transportState, BizConstant.SP_TRANSSTATE_EQUIPMENTIN),
                    retCodeConfig.getInvalidCassetteTransferState(), transportState, castID);

            /*------------------------------*/
            /*   Get TransferReserveState   */
            /*------------------------------*/
            boolean transferReserved = CimBooleanUtils.isTrue(cimCassette.isReserved());
            if (transferReserved) {
                CimPerson cimPerson = cimCassette.reservedBy();
                Validations.check(cimPerson == null, retCodeConfig.getNotFoundPerson());
                String userId = cimPerson.getIdentifier();
                Validations.check(true, retCodeConfig.getAlreadyReservedCst(), castID, userId);
            }

            /*----------------------------------------------*/
            /*   Get and Check Cassette's Dispatch Status   */
            /*----------------------------------------------*/
            Validations.check(CimBooleanUtils.isTrue(cimCassette.isDispatchReserved()), retCodeConfig.getAlreadyDispatchReservedCassette());

            Validations.check(cimCassette.getControlJob() != null, retCodeConfig.getCassetteControlJobFilled());

            /*-------------------------------------*/
            /*   Get and Check Cassette's Status   */
            /*-------------------------------------*/
            String durableState = cimCassette.getDurableState();
            Validations.check(!CimStringUtils.equalsIn(durableState, BizConstant.CIMFW_DURABLE_AVAILABLE, BizConstant.CIMFW_DURABLE_INUSE),
                    retCodeConfig.getInvalidCassetteState(), durableState, castID);
        }));
    }

    @Override
    public void cassetteCheckConditionForSLMDestCassette(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, ObjectIdentifier controlJobID, ObjectIdentifier cassetteID, ObjectIdentifier lotID) {
        CimCassette aCassette = baseCoreFactory.getBO(CimCassette.class, cassetteID);
        Validations.check(null == aCassette, retCodeConfig.getNotFoundCassette());

        //-----------------------------------------------------------
        // Check ControlJob of Cassette
        //-----------------------------------------------------------
        log.info("Check ControlJob of Cassette");
        CimControlJob aControlJob = aCassette.getControlJob();
        if (null != aControlJob) {
            if (!CimStringUtils.equals(aControlJob.getIdentifier(), controlJobID.getValue())) {
                Validations.check(retCodeConfigEx.getSlmDstcastReservedAnotherCtrljob(), cassetteID.getValue());
            }
        }

        //--------------------------------------------------
        // Check SLMReserveEquipmentID of Cassette
        //--------------------------------------------------
        log.info("Check SLMReserveEquipmentID of Cassette");
        CimMachine aSLMReservedMachine = aCassette.getSLMReservedMachine();
        if (null != aSLMReservedMachine) {
            String castSLMRsvEqpID = aSLMReservedMachine.getIdentifier();
            log.info("castSLMRsvEqpID : " + castSLMRsvEqpID);
            if (!ObjectIdentifier.equalsWithValue(cassetteID, equipmentID)) {
                log.error("!!!!! Error Because the specified destination cassette is reserved by another Equipment SLM.");
                Validations.check(retCodeConfig.getAlreadyReservedCassetteSlm());
            } else {
                List<Infos.EqpContainerPosition> eqpContainerPositions = equipmentContainerPositionMethod.equipmentContainerPositionInfoGetByDestCassette(objCommon, equipmentID, cassetteID);
                if (CimArrayUtils.isNotEmpty(eqpContainerPositions) && !ObjectIdentifier.equalsWithValue(eqpContainerPositions.get(0).getControlJobID(), controlJobID)) {
                    Validations.check(retCodeConfig.getAlreadyReservedCassetteSlm(), aCassette.getIdentifier(), aCassette.getIdentifier());
                }
            }
        }


        //----------------------------------------------------
        // Check SortJob of Cassette
        //----------------------------------------------------
        log.info("Check SortJob of Cassette");
        com.fa.cim.sorter.Params.ObjSorterJobListGetDRIn objSorterJobListGetDRIn = new com.fa.cim.sorter.Params.ObjSorterJobListGetDRIn();
        objSorterJobListGetDRIn.setCarrierID(cassetteID);
        List<Info.SortJobListAttributes> sortJobListAttributes = sorterNewMethod.sorterJobListGetDR(objCommon, objSorterJobListGetDRIn);
        if (CimArrayUtils.getSize(sortJobListAttributes) > 0) {
            Validations.check(retCodeConfigEx.getExistSorterjobForCassette(), cassetteID.getValue(), sortJobListAttributes.get(0).getSorterJobID().getValue());
        }


        //-------------------------------------------------------
        // Check PostProcessFlag of cassette
        //-------------------------------------------------------
        log.info("Check PostProcessFlag of cassette");
        boolean bPostProcFlg = aCassette.isPostProcessFlagOn();
        Validations.check(bPostProcFlg, retCodeConfig.getCassetteInPostProcess());

        //-------------------------------------------------------
        // Check cassette InterFabXfer State
        //-------------------------------------------------------
        String castInterFabState = aCassette.getInterFabTransferState();
        log.info("#### interFabXferState : " + castInterFabState);
        if (CimStringUtils.equals(castInterFabState, BizConstant.SP_INTERFAB_XFERSTATE_TRANSFERRING)) {
            Validations.check(retCodeConfig.getInterfabInvalidCassetteTransferStateForRequest());
        }


        //-------------------------------------------------------
        // Check available condition of cassette
        //-------------------------------------------------------
        log.info("Check available condition of cassette");
        String cassetteState = aCassette.getDurableState();
        log.info(" cassetteState : " + cassetteState);
        if (!CimStringUtils.equals(cassetteState, BizConstant.CIMFW_DURABLE_AVAILABLE)
                && !CimStringUtils.equals(cassetteState, BizConstant.CIMFW_DURABLE_INUSE)) {
            Validations.check(retCodeConfig.getInvalidCassetteState());
        }

        //-------------------------------------------------------
        // Check dispatch reservation condition of cassette
        //-------------------------------------------------------
        log.info("Check dispatch reservation condition of cassette");
        boolean bDispatchReserved = aCassette.isDispatchReserved();
        log.info(" bDispatchReserved : " + bDispatchReserved);
        if (bDispatchReserved) {
            String NPWLoadPurposeType = aCassette.getNPWLoadPurposeType();
            log.info(" NPWLoadPurposeType : " + NPWLoadPurposeType);
            if (CimStringUtils.equals(NPWLoadPurposeType, BizConstant.SP_LOADPURPOSETYPE_SLMRETRIEVING)) {
                Validations.check(retCodeConfig.getAlreadyDispatchReservedCassette());
            }
        }


        //-------------------------------------------------------
        // Check CarrierCategory of cassette
        //-------------------------------------------------------
        log.info("Check CarrierCategory of cassette");
        String castCategory = aCassette.getCassetteCategory();
        log.info(" castCategory : " + castCategory);
        // Get required cassette category of current operation of Lot.
        CimLot aPosLot = baseCoreFactory.getBO(CimLot.class, lotID);
        Validations.check(null == aPosLot, retCodeConfig.getNotFoundLot());

        //-------------------------------------------------------
        // Check cassette InterFabXfer State
        //-------------------------------------------------------
        String lotInterFabState = aPosLot.getInterFabTransferState();
        log.info(" #### interFabXferState : " + lotInterFabState);
        if (CimStringUtils.equals(lotInterFabState, BizConstant.SP_INTERFAB_XFERSTATE_TRANSFERRING)
                || CimStringUtils.equals(lotInterFabState, BizConstant.SP_INTERFAB_XFERSTATE_REQUIRED)) {
            Validations.check(retCodeConfig.getInterfabInvalidLotXferstateForReq(), aPosLot.getIdentifier(), lotInterFabState);
        }

        String curOpeCastCategory = aPosLot.getRequiredCassetteCategory();
        log.info(" curOpeCastCategory : " + curOpeCastCategory);
        if (CimStringUtils.isNotEmpty(castCategory) && CimStringUtils.isNotEmpty(curOpeCastCategory)) {
            // Get required cassette category of next operation of Lot.
            String nextRequiredCassetteCategory = lotMethod.lotRequiredCassetteCategoryGetForNextOperation(objCommon, lotID);
            log.info(" nextRequiredCassetteCategory : " + nextRequiredCassetteCategory);
            if (CimStringUtils.isNotEmpty(nextRequiredCassetteCategory) && !CimStringUtils.equals(castCategory, nextRequiredCassetteCategory)) {
                Validations.check(retCodeConfigEx.getMismatchDestCastCategory(), cassetteID.getValue(), castCategory, curOpeCastCategory, nextRequiredCassetteCategory);
            }
        }

        //-------------------------------------------------------
        // Check cassette transfer status
        // When any of Equipment port access mode is "Manual"
        //     Retrieving cassette should be any of "SI, MI" status;
        //     If cassette transfer status is "EI", the current cassette loading port should have access mode of "Auto" too.
        //     If Reroute is allowed, cassette status is allowed to be "BO/BI" status; if cassette has a transfer job, "SO" and "EO" are also allowed
        //-------------------------------------------------------
        //  Get equipment port information
        Infos.EqpPortInfo eqpPortInfo = equipmentMethod.equipmentPortInfoGet(objCommon, equipmentID);

        boolean manualAccessFlag = false;
        // check access mode
        if (null != eqpPortInfo && CimArrayUtils.isNotEmpty(eqpPortInfo.getEqpPortStatuses())) {
            for (Infos.EqpPortStatus eqpPortStatus : eqpPortInfo.getEqpPortStatuses()) {
                log.info("# Loop[nCnt1], portID = " + eqpPortStatus.getPortID().getValue());
                if (CimStringUtils.equals(eqpPortStatus.getAccessMode(), BizConstant.SP_EQP_ACCESSMODE_MANUAL)) {
                    log.info(String.format(" # Port[%s]/AccessMode[%s] ", eqpPortStatus.getPortID().getValue(), eqpPortStatus.getAccessMode()));
                    manualAccessFlag = true;
                    break;
                }
            }
        }
        if (!manualAccessFlag) {
            // All "Auto" Access mode
            //Get transferState (cassette)
            String transferState = aCassette.getTransportState();
            log.info(" transferState : " + transferState);
            // transfer status of SI and MI is allowed
            if (CimStringUtils.equals(transferState, BizConstant.SP_TRANSSTATE_STATIONIN)
                    || CimStringUtils.equals(transferState, BizConstant.SP_TRANSSTATE_MANUALIN)) {
                log.info(" transferState = MI or SI ");
            } else if (CimStringUtils.equals(transferState, BizConstant.SP_TRANSSTATE_EQUIPMENTIN)) {
                log.info("transferState = EI");
                // When cassette xferstatus is EI, cassette current loaded equipment(port) access mode should be Auto
                //get cassette eqp access mode
                Machine aMachine = aCassette.currentAssignedMachine();
                // get current loading equipment port information
                Validations.check(null == aMachine, retCodeConfig.getNotFoundMachine());
                ObjectIdentifier currentEquipmentID = ObjectIdentifier.build(aMachine.getIdentifier(), aMachine.getPrimaryKey());
                // get current loading equipment port information
                Infos.EqpPortInfo eqpPortInfoCurrent = equipmentMethod.equipmentPortInfoGet(objCommon, currentEquipmentID);
                Optional.ofNullable(eqpPortInfoCurrent).ifPresent(current -> {
                    int nPortLen2 = CimArrayUtils.getSize(current.getEqpPortStatuses());
                    int i;
                    for (i = 0; i < nPortLen2; i++) {
                        if (ObjectIdentifier.equalsWithValue(current.getEqpPortStatuses().get(i).getLoadedCassetteID(), cassetteID)) {
                            log.info(" # cassette is loaded on port ");
                            if (CimStringUtils.equals(current.getEqpPortStatuses().get(i).getAccessMode(), BizConstant.SP_EQP_ACCESSMODE_AUTO)) {
                                log.info("Access Mode = Auto");
                            } else {
                                log.error(" Access Mode != Auto ");
                                Validations.check(retCodeConfig.getInvalidCassetteTransferState());
                            }
                            break;
                        }
                    }

                    if (i == nPortLen2) {
                        log.error(" Data inconsistency ");
                        Validations.check(retCodeConfig.getInvalidParameterWithMsg());
                    }
                });
            } else {
                //get rerouteFlag
                String reRouteXferFlag = StandardProperties.OM_XFER_REROUTE_FLAG.getValue();
                log.info("OM_XFER_REROUTE_FLAG : " + reRouteXferFlag);
                if (CimStringUtils.equals(reRouteXferFlag, "1")) {
                    log.info("reRouteXferFlag = 1");
                    if (CimStringUtils.equals(transferState, BizConstant.SP_TRANSSTATE_BAYIN)
                            || CimStringUtils.equals(transferState, BizConstant.SP_TRANSSTATE_BAYOUT)) {
                        log.info("transferState = BI or BO");
                    } else if (CimStringUtils.equals(transferState, BizConstant.SP_TRANSSTATE_STATIONOUT)
                            || CimStringUtils.equals(transferState, BizConstant.SP_TRANSSTATE_EQUIPMENTOUT)) {
                        log.info("transferState = SO or EO");
                        //get transferjob (cassetterID)
                        try {
                            Infos.CarrierJobResult carrierJobResult = this.cassetteTransferJobRecordGetDR(objCommon, cassetteID);
                        } catch (ServiceException e) {
                            if (Validations.isEquals(e.getCode(), retCodeConfig.getCarrierNotTransfering())) {
                                Validations.check(retCodeConfig.getInvalidCassetteTransferState(), transferState, cassetteID.getValue());
                            }
                        }
                        log.info("transfer job existing");

                    } else {
                        log.error("Invalid transfer status");
                        Validations.check(retCodeConfig.getInvalidCassetteTransferState(), transferState, cassetteID.getValue());
                    }
                }
            }
        } else {
            log.info("access mode = false");
        }
        log.info("This cassette's condition is Good");
    }

    public ObjectIdentifier cassetteEmptyCassetteForRetrievingGet(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, ObjectIdentifier lotID) {
        /*----------------*/
        /*   Initialize   */
        /*----------------*/
        ObjectIdentifier dummy = new ObjectIdentifier();
        // -----------------------------------------------------------------------------------
        // 1. select source cassette as destination cassette if OM_FMC_SOURCE_CARRIER_SELECT_PRIORITY=ON
        // -----------------------------------------------------------------------------------
        String srcCassettePriority = StandardProperties.OM_FMC_SOURCE_CARRIER_SELECT_PRIORITY.getValue();
        if (CimStringUtils.equals(srcCassettePriority, BizConstant.SP_SLM_SWITCH_ON)) {
            //  Get equipmentContainer position objects by lotID
            Inputs.ObjEquipmentContainerPositionInfoGetIn objEquipmentContainerPositionInfo = new Inputs.ObjEquipmentContainerPositionInfoGetIn();
            objEquipmentContainerPositionInfo.setEquipmentID(equipmentID);
            objEquipmentContainerPositionInfo.setKey(lotID);
            objEquipmentContainerPositionInfo.setKeyCategory(BizConstant.SP_SLM_KEYCATEGORY_LOT);
            Infos.EqpContainerPositionInfo containerPositionInfoResult = equipmentContainerPositionMethod.equipmentContainerPositionInfoGet(objCommon,objEquipmentContainerPositionInfo);
            List<Infos.EqpContainerPosition> strEqpContainerPositionSeq = containerPositionInfoResult.getEqpContainerPositionList();
            // check source cassette
            if (CimObjectUtils.isEmpty(strEqpContainerPositionSeq) || ObjectIdentifier.isEmpty(strEqpContainerPositionSeq.get(0).getSrcCassetteID())) {
                throw new ServiceException(new OmCode(retCodeConfigEx.getSlmSrcastNotExist(), "", lotID.getValue(), ""));
            }
            try {
                this.cassetteCheckConditionForSLMDestCassette(objCommon, dummy, dummy, strEqpContainerPositionSeq.get(0).getSrcCassetteID(), dummy);
            } catch (ServiceException e) {
                log.info("Not a Empty Cassette");
            }
            return strEqpContainerPositionSeq.get(0).getSrcCassetteID();
        }
        // -----------------------------------------------------------------------------------
        // 2. select loaded empty cassettes on the same equipment as the processing lot
        // -----------------------------------------------------------------------------------
        //  Get equipment port information
        Infos.EqpPortInfo eqpPortInfo = equipmentMethod.equipmentPortInfoGet(objCommon, equipmentID);
        List<Infos.EqpPortStatus> eqpPortStatuses = eqpPortInfo.getEqpPortStatuses();
        for (Infos.EqpPortStatus eqpPortStatus : eqpPortStatuses) {
            if (!ObjectIdentifier.isEmpty(eqpPortStatus.getLoadedCassetteID())) {
                log.info("# Found a cassette at port");
                // check if the cassette is empty
                try {
                    this.cassetteCheckEmpty(eqpPortStatus.getLoadedCassetteID());
                } catch (ServiceException e) {
                    log.info("Not a Empty Cassette");
                    continue;
                }
                try {
                    this.cassetteCheckConditionForSLMDestCassette(objCommon, dummy, dummy, eqpPortStatus.getLoadedCassetteID(), dummy);
                } catch (ServiceException e) {
                    log.info("Not a Empty Cassette");
                }
                return eqpPortStatus.getLoadedCassetteID();
            }
        }

        // -----------------------------------------------------------------------------------
        // 3. select empty cassettes in equipment associated available auto stockers
        //    (SLMUTS is prior to normal auto stockers)
        // -----------------------------------------------------------------------------------
        // get associated SLMUTS stockers
        Infos.EqpStockerInfo SLMUTSStockerInfo = equipmentMethod.equipmentSLMUTSInfoGetDR(objCommon, equipmentID);
        List<Infos.EqpStockerStatus> slmUTStks = SLMUTSStockerInfo.getEqpStockerStatusList();
        // get associated auto stockers
        Infos.EqpStockerInfo autoStockerInfo = equipmentMethod.equipmentStockerInfoGetDR(objCommon, equipmentID);
        List<Infos.EqpStockerStatus> autoStks = autoStockerInfo.getEqpStockerStatusList();

        List<Infos.EqpStockerStatus> strEqpStockerStatus = new ArrayList<>(slmUTStks);
        for (Infos.EqpStockerStatus autoStk : autoStks) {
            if (CimStringUtils.equals(autoStk.getStockerType(), BizConstant.SP_STOCKER_TYPE_AUTO)) {
                strEqpStockerStatus.add(autoStk);
            }
        }
        if (!CimObjectUtils.isEmpty(strEqpStockerStatus)) {
            Infos.EqpStockerStatus aEquipmentStockerInfo = new Infos.EqpStockerStatus();
            for (Infos.EqpStockerStatus eqpStockerStatus : strEqpStockerStatus) {
                aEquipmentStockerInfo = eqpStockerStatus;
                // check availabilit
                try {
                    equipmentMethod.equipmentCheckAvail(objCommon, aEquipmentStockerInfo.getStockerID());
                } catch (ServiceException e) {
                    if (e.getCode() == retCodeConfig.getEquipmentNotAvailableStat().getCode()) {
                        log.info("# This stocker is not Available.");
                        continue;
                    } else {
                        throw e;
                    }
                }
                //get cassette list in the stocker
                Inputs.ObjCassetteListGetDRIn listGetDRIn = new Inputs.ObjCassetteListGetDRIn(new Params.CarrierListInqParams());
                listGetDRIn.setEmptyFlag(true);
                listGetDRIn.setStockerID(aEquipmentStockerInfo.getStockerID());
                listGetDRIn.setCassetteStatus(CIMStateConst.CIM_DURABLE_AVAILABLE);
                listGetDRIn.setMaxRetrieveCount(-1);
                listGetDRIn.setSorterJobCreationCheckFlag(false);
                listGetDRIn.setBankID(dummy);
                listGetDRIn.setDurablesSubStatus(dummy);
                listGetDRIn.setFlowStatus("");
                //【note】: add searchCondition default by neyo
                listGetDRIn.setSearchCondition(new SearchCondition());
                //【step2】 - cassette_ListGetDR__170
                Page<Infos.FoundCassette> carrierListInq170ResultRetCode = this.cassetteListGetDR170(objCommon, listGetDRIn);
                List<Infos.FoundCassette> strFoundCassettes = carrierListInq170ResultRetCode.getContent();
                if (!CimObjectUtils.isEmpty(strFoundCassettes)) {
                    for (Infos.FoundCassette foundCassette : strFoundCassettes) {
                        try {
                            this.cassetteCheckConditionForSLMDestCassette(objCommon, dummy, dummy, foundCassette.getCassetteID(), dummy);
                        } catch (ServiceException e) {
                            log.info("SLMDestCassette Not Available");
                            continue;
                        }
                        return foundCassette.getCassetteID();
                    }
                }
            }
        }

        // -----------------------------------------------------------------------------------
        // 4. select empty cassettes in available auto stockers in the same workArea
        // -----------------------------------------------------------------------------------
        Outputs.ObjMachineTypeGetOut machineTypeGet = equipmentMethod.machineTypeGet(objCommon, equipmentID);
        Params.EqpListByBayInqInParm eqpListByBayInqInParm = new Params.EqpListByBayInqInParm();
        eqpListByBayInqInParm.setEquipmentID(dummy);
        eqpListByBayInqInParm.setEquipmentCategory("");
        eqpListByBayInqInParm.setFpcCategories(new ArrayList<>());
        eqpListByBayInqInParm.setWhiteDefSearchCriteria("");
        eqpListByBayInqInParm.setWorkArea(machineTypeGet.getAreaID());
        Results.EqpListByBayInqResult eqpListByBayInqResult = equipmentMethod.equipmentFillInTxEQQ003DR(objCommon, eqpListByBayInqInParm);
        List<Infos.AreaStocker> strAreaStockers = eqpListByBayInqResult.getStrAreaStocker();
        for (Infos.AreaStocker strAreaStocker : strAreaStockers) {
            if (CimStringUtils.equals(strAreaStocker.getStockerType(), BizConstant.SP_STOCKER_TYPE_AUTO)) {
                log.info("Auto Stocker");
                // check availability
                try {
                    equipmentMethod.equipmentCheckAvail(objCommon, strAreaStocker.getStockerID());
                } catch (ServiceException e) {
                    if (e.getCode() == retCodeConfig.getEquipmentNotAvailableStat().getCode()) {
                        log.info("# This stocker is not Available.");
                        continue;
                    } else {
                        throw e;
                    }
                }
                //get cassette list in the stocker
                Inputs.ObjCassetteListGetDRIn listGetDRIn = new Inputs.ObjCassetteListGetDRIn(new Params.CarrierListInqParams());
                listGetDRIn.setEmptyFlag(true);
                listGetDRIn.setStockerID(strAreaStocker.getStockerID());
                listGetDRIn.setCassetteStatus(CIMStateConst.CIM_DURABLE_AVAILABLE);
                listGetDRIn.setMaxRetrieveCount(-1);
                listGetDRIn.setSorterJobCreationCheckFlag(false);
                listGetDRIn.setBankID(dummy);
                listGetDRIn.setDurablesSubStatus(dummy);
                listGetDRIn.setFlowStatus("");
                //【note】: add searchCondition default by neyo
                listGetDRIn.setSearchCondition(new SearchCondition());
                //【step2】 - cassette_ListGetDR__170
                Page<Infos.FoundCassette> carrierListInq170ResultRetCode = this.cassetteListGetDR170(objCommon, listGetDRIn);
                List<Infos.FoundCassette> strFoundCassettes = carrierListInq170ResultRetCode.getContent();
                if (!CimObjectUtils.isEmpty(strFoundCassettes)) {
                    for (Infos.FoundCassette foundCassette : strFoundCassettes) {
                        try {
                            this.cassetteCheckConditionForSLMDestCassette(objCommon, dummy, dummy, foundCassette.getCassetteID(), dummy);
                        } catch (ServiceException e) {
                            log.info("SLMDestCassette Not Available");
                            continue;
                        }
                        return foundCassette.getCassetteID();
                    }
                }
            }
        }
        // -----------------------------------------------------------------------------------
        // 5. select empty cassettes in available auto stockers in the same location
        // -----------------------------------------------------------------------------------
        CimMachine aPosMachine = baseCoreFactory.getBO(CimMachine.class, equipmentID);
        if (CimObjectUtils.isEmpty(aPosMachine)) {
            throw new ServiceException(retCodeConfigEx.getUnexpectedNilObject());
        }
        CimArea aPosArea = aPosMachine.getWorkArea();
        if (CimObjectUtils.isEmpty(aPosArea)) {
            throw new ServiceException(new OmCode(retCodeConfigEx.getNotFoundWorkarea(), "*****"));
        }
        Area anArea = aPosArea.getSuperArea();
        if (CimObjectUtils.isEmpty(anArea)) {
            throw new ServiceException(new OmCode(retCodeConfigEx.getNotFoundArea(), "*****"));
        }
        String locationID = anArea.getIdentifier();

        List<ObjectIdentifier> strArea_GetByLocationIDDR_out = areaMethod.areaGetByLocationID(objCommon, locationID);
        for (ObjectIdentifier workAreaID : strArea_GetByLocationIDDR_out) {
            Params.EqpListByBayInqInParm eqpListByBayInqInParm1 = new Params.EqpListByBayInqInParm();
            eqpListByBayInqInParm1.setEquipmentID(dummy);
            eqpListByBayInqInParm1.setEquipmentCategory("");
            eqpListByBayInqInParm1.setFpcCategories(new ArrayList<>());
            eqpListByBayInqInParm1.setWhiteDefSearchCriteria("");
            eqpListByBayInqInParm1.setWorkArea(workAreaID);
            Results.EqpListByBayInqResult eqpListByBayInqResult1 = equipmentMethod.equipmentFillInTxEQQ003DR(objCommon, eqpListByBayInqInParm1);
            List<Infos.AreaStocker> strAreaStockers1 = eqpListByBayInqResult1.getStrAreaStocker();
            for (Infos.AreaStocker strAreaStocker : strAreaStockers1) {
                if (CimStringUtils.equals(strAreaStocker.getStockerType(), BizConstant.SP_STOCKER_TYPE_AUTO)) {
                    log.info("Auto Stocker");
                    // check availability
                    try {
                        equipmentMethod.equipmentCheckAvail(objCommon, strAreaStocker.getStockerID());
                    } catch (ServiceException e) {
                        if (e.getCode() == retCodeConfig.getEquipmentNotAvailableStat().getCode()) {
                            log.info("# This stocker is not Available.");
                            continue;
                        } else {
                            throw e;
                        }
                    }
                    //get cassette list in the stocker
                    Inputs.ObjCassetteListGetDRIn listGetDRIn = new Inputs.ObjCassetteListGetDRIn(new Params.CarrierListInqParams());
                    listGetDRIn.setEmptyFlag(true);
                    listGetDRIn.setStockerID(strAreaStocker.getStockerID());
                    listGetDRIn.setCassetteStatus(CIMStateConst.CIM_DURABLE_AVAILABLE);
                    listGetDRIn.setMaxRetrieveCount(-1);
                    listGetDRIn.setSorterJobCreationCheckFlag(false);
                    listGetDRIn.setBankID(dummy);
                    listGetDRIn.setDurablesSubStatus(dummy);
                    listGetDRIn.setFlowStatus("");
                    //【note】: add searchCondition default by neyo
                    listGetDRIn.setSearchCondition(new SearchCondition());
                    //【step2】 - cassette_ListGetDR__170
                    Page<Infos.FoundCassette> carrierListInq170ResultRetCode = this.cassetteListGetDR170(objCommon, listGetDRIn);
                    List<Infos.FoundCassette> strFoundCassettes = carrierListInq170ResultRetCode.getContent();
                    if (!CimObjectUtils.isEmpty(strFoundCassettes)) {
                        for (Infos.FoundCassette foundCassette : strFoundCassettes) {
                            try {
                                this.cassetteCheckConditionForSLMDestCassette(objCommon, dummy, dummy, foundCassette.getCassetteID(), dummy);
                            } catch (ServiceException e) {
                                log.info("SLMDestCassette Not Available");
                                continue;
                            }
                            return foundCassette.getCassetteID();
                        }
                    }
                }
            }
        }
        return null;
    }

    @Override
    public void cassetteSLMReserveEquipmentSet(Infos.ObjCommon objCommon,
                                               ObjectIdentifier cassetteID,
                                               ObjectIdentifier slmReserveEquipmentID) {

        /*--------------------------*/
        /*   Get Equipment Object   */
        /*--------------------------*/
        CimMachine cimMachine = baseCoreFactory.getBO(CimMachine.class, slmReserveEquipmentID);

        /*-------------------------*/
        /*   Get Cassette Object   */
        /*-------------------------*/
        CimCassette cimCassette = baseCoreFactory.getBO(CimCassette.class, cassetteID);

        /*-----------------------------*/
        /*   Set SLMReserved Machine   */
        /*-----------------------------*/
        cimCassette.setSLMReservedMachine(cimMachine);

    }
}
