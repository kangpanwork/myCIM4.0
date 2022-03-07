package com.fa.cim.method.impl;

import com.fa.cim.annotaion.OmMethod;
import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.OmCode;
import com.fa.cim.common.utils.*;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.config.RetCodeConfigEx;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Inputs;
import com.fa.cim.dto.Outputs;
import com.fa.cim.dto.Results;
import com.fa.cim.entity.runtime.durable.CimDurableDO;
import com.fa.cim.entity.runtime.reticlepod.CimReticlePodDO;
import com.fa.cim.entity.runtime.stocker.CimStockerDO;
import com.fa.cim.jpa.CimJpaRepository;
import com.fa.cim.method.IEquipmentMethod;
import com.fa.cim.method.IReticleMethod;
import com.fa.cim.method.IStockerMethod;
import com.fa.cim.newcore.bo.code.CimE10State;
import com.fa.cim.newcore.bo.durable.CimReticlePod;
import com.fa.cim.newcore.bo.durable.DurableManager;
import com.fa.cim.newcore.bo.machine.CimReticlePodPortResource;
import com.fa.cim.newcore.bo.machine.CimStorageMachine;
import com.fa.cim.newcore.bo.machine.MachineManager;
import com.fa.cim.newcore.bo.person.CimPerson;
import com.fa.cim.newcore.bo.person.PersonManager;
import com.fa.cim.newcore.dto.machine.MachineDTO;
import com.fa.cim.newcore.exceptions.CoreFrameworkException;
import com.fa.cim.newcore.factory.BaseCoreFactory;
import com.fa.cim.newcore.standard.mchnmngm.Machine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2018/7/16        ********             Bear               create file
 *
 * @author: Bear
 * @date: 2018/7/16 18:02
 * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@OmMethod
@Slf4j
public class StockerMethod  implements IStockerMethod {

    @Autowired
    private RetCodeConfig retCodeConfig;

    @Autowired
    private RetCodeConfigEx retCodeConfigEx;

    @Autowired
    private CimJpaRepository cimJpaRepository;

    @Autowired
    private BaseCoreFactory baseCoreFactory;

    @Autowired
    private IEquipmentMethod equipmentMethod;

    @Autowired
    private IReticleMethod reticleMethod;

    @Autowired
    private MachineManager machineManager;

    @Autowired
    private PersonManager personManager;

    @Autowired
    private DurableManager durableManager;

    @Override
    public Outputs.ObjStockerTypeGetDROut stockerTypeGet(Infos.ObjCommon objCommon, ObjectIdentifier stockerID) {
        Outputs.ObjStockerTypeGetDROut result = new Outputs.ObjStockerTypeGetDROut();
        com.fa.cim.newcore.bo.machine.CimStorageMachine aStorageMachine = baseCoreFactory.getBO(com.fa.cim.newcore.bo.machine.CimStorageMachine.class, stockerID);
        Validations.check(null == aStorageMachine, retCodeConfig.getNotFoundStocker());
        String stockerType = aStorageMachine.getStockerType();
        if (CimStringUtils.equals(BizConstant.SP_STOCKER_TYPE_AUTO, stockerType)
         || CimStringUtils.equals(BizConstant.SP_STOCKER_TYPE_INTERM, stockerType)
         || CimStringUtils.equals(BizConstant.SP_STOCKER_TYPE_SHELF, stockerType)
         || CimStringUtils.equals(BizConstant.SP_STOCKER_TYPE_RETICLE, stockerType)

         || CimStringUtils.equals(BizConstant.SP_STOCKER_TYPE_FIXTURE, stockerType)
         || CimStringUtils.equals(BizConstant.SP_STOCKER_TYPE_INTERBAY, stockerType)
         || CimStringUtils.equals(BizConstant.SP_STOCKER_TYPE_INTRABAY, stockerType)

         || CimStringUtils.equals(BizConstant.SP_STOCKER_TYPE_RETICLEPOD, stockerType)
         || CimStringUtils.equals(BizConstant.SP_STOCKER_TYPE_BARERETICLE, stockerType)
         || CimStringUtils.equals(BizConstant.SP_STOCKER_TYPE_RETICLESHELF, stockerType)
         || CimStringUtils.equals(BizConstant.SP_STOCKER_TYPE_ERACK, stockerType)) {
            result.setStockerType(stockerType);
            result.setUtsFlag(aStorageMachine.isUTSStocker());
            result.setMaxUtsCapacity(aStorageMachine.getMaxUTSCapacity());
        } else {
            throw new ServiceException(retCodeConfig.getUndefinedStockerType());
        }
        return result;
    }

    @Override
    public Results.StockerInfoInqResult stockerBaseInfoGet(Infos.ObjCommon objCommon, ObjectIdentifier stockerID) {
        Results.StockerInfoInqResult stockerInfoInqResult = new Results.StockerInfoInqResult();
        CimStorageMachine aStocker = baseCoreFactory.getBO(CimStorageMachine.class, stockerID);
        Validations.check(aStocker == null, retCodeConfig.getNotFoundStocker());

        stockerInfoInqResult.setStockerID(new ObjectIdentifier(aStocker.getIdentifier(), aStocker.getPrimaryKey()));
        stockerInfoInqResult.setDescription(aStocker.getDescription());
        stockerInfoInqResult.setStockerType(aStocker.getStockerType());

        com.fa.cim.newcore.bo.code.CimMachineState aCurrentState = aStocker.getCurrentMachineState();
        Validations.check(aCurrentState == null, retCodeConfig.getNotFoundEqpState());
        CimE10State aCurrentE10 = aCurrentState.getE10State();

        com.fa.cim.newcore.bo.code.CimMachineState anActualState = aStocker.getActualMachineState();

        CimE10State anActualE10 = null;
        if (anActualState != null){
            anActualE10 = anActualState.getE10State();
        }
        stockerInfoInqResult.setStockerStatusCode(new ObjectIdentifier(aCurrentState.getIdentifier(),aCurrentState.getPrimaryKey()));
        stockerInfoInqResult.setE10Status(new ObjectIdentifier(aCurrentE10.getIdentifier(),aCurrentE10.getPrimaryKey()));
        stockerInfoInqResult.setStatusName(aCurrentState.getMachineStateName());
        stockerInfoInqResult.setStatusDescription(aCurrentState.getMachineStateName());
        stockerInfoInqResult.setStatusChangeTimeStamp(aStocker.getLastStatusChangeTimeStamp());

        if (null != anActualE10) {
            stockerInfoInqResult.setActualE10Status(new ObjectIdentifier(anActualE10.getIdentifier(),anActualE10.getPrimaryKey()));
        }
        if (anActualState != null){
            stockerInfoInqResult.setActualStatusCode(new ObjectIdentifier(anActualState.getIdentifier(),anActualState.getPrimaryKey()));
            stockerInfoInqResult.setActualStatusName(anActualState.getMachineStateName());
            stockerInfoInqResult.setActualStatusDescription(anActualState.getMachineStateDescription());
        }
        stockerInfoInqResult.setActualStatusChangeTimeStamp(aStocker.getLastActualStatusChangeTimeStamp());
        stockerInfoInqResult.setUtsFlag(aStocker.isUTSStocker());
        stockerInfoInqResult.setMaxUTSCapacity(CimNumberUtils.intValue(aStocker.getMaxUTSCapacity()));
        stockerInfoInqResult.setMaxReticleCapacity(CimNumberUtils.intValue(aStocker.getMaxReticleCapacity()));
        stockerInfoInqResult.setOnlineMode(aStocker.getOnlineMode());
        stockerInfoInqResult.setSLMUTSFlag(aStocker.isSLMUTSFlagOn());
        List<MachineDTO.StorageMachineResource> aResourceDataList = aStocker.allStorageMachineResources();
        List<Infos.ResourceInfo> collectList = aResourceDataList.stream().map(
                storageMachineResource -> {
                    Infos.ResourceInfo aResourceData = new Infos.ResourceInfo();
                    aResourceData.setResourceID(storageMachineResource.getResourceID());
                    aResourceData.setResourceType(storageMachineResource.getType());
                    return aResourceData;
                }
        ).collect(Collectors.toList());
        stockerInfoInqResult.setResourceInfoData(collectList);
        return stockerInfoInqResult;
    }

    /**
     * description: stocker_FillInTxLGQ003DR
     * <p><br/></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param stockerType
     * @param availFlag
     * @return com.fa.cim.dto.RetCode<com.fa.cim.dto.Results.StockerListInqResult>
     * @author Ho
     * @date 2018/10/9 10:58:16
     */
    @Override
    public Results.StockerListInqResult stockerListInfoGetDR(Infos.ObjCommon objCommon, String stockerType, boolean availFlag) {
        Results.StockerListInqResult stockerListInqResult = new Results.StockerListInqResult();
        List<CimStockerDO> stockerList=null;
        if(availFlag){
            String hFRSTKE10_STATE = BizConstant.SP_E10STATE_STANDBY;
            String hFRSTKE10_STATE_1 = BizConstant.SP_E10STATE_PRODUCTIVE;
            stockerList = cimJpaRepository.query("SELECT * FROM OMSTOCKER WHERE STOCKER_TYPE = ?1 AND (E10_STATE_ID = ?2 OR E10_STATE_ID = ?3) ORDER BY STOCKER_ID", CimStockerDO.class, stockerType, hFRSTKE10_STATE, hFRSTKE10_STATE_1);
        }else{
            stockerList = cimJpaRepository.query(" SELECT * FROM OMSTOCKER WHERE STOCKER_TYPE = ?1 ORDER BY STOCKER_ID", CimStockerDO.class, stockerType);
        }
        if (CimArrayUtils.isEmpty(stockerList)){
            throw new ServiceException(retCodeConfig.getNotFoundStkType(), stockerListInqResult);
        }
        List<Infos.StockerInfo> stockerInfoList=new ArrayList<>();
        for(CimStockerDO stocker : stockerList){
            Infos.StockerInfo stockerInfo=new Infos.StockerInfo();
            stockerInfoList.add(stockerInfo);
            stockerInfo.setStockerID(ObjectIdentifier.build(stocker.getStockerID(), stocker.getId()));
            stockerInfo.setUtsFlag(stocker.getUtsFlag());
            stockerInfo.setDescription(stocker.getDescription());
        }

        stockerListInqResult.setStrStockerInfo(stockerInfoList);
        stockerListInqResult.setStockerType(stockerType);

        return stockerListInqResult;
    }

    /**
     * description:
     * <p><br/></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @return com.fa.cim.dto.RetCode<com.fa.cim.dto.Results.StockerForAutoTransferInqResult>
     * @author Ho
     * @date 2018/10/9 16:10:32
     */
    @Override
    public List<Infos.AvailableStocker> stockerFillInTxLGQ002DR(Infos.ObjCommon objCommon) {
        String hFRSTKE10_STATE = BizConstant.SP_E10STATE_STANDBY,
                hFRSTKE10_STATE_1 = BizConstant.SP_E10STATE_PRODUCTIVE,
                hFRSTKSTK_TYPE,
                lvhSTK_TYPE = BizConstant.SP_STOCKER_TYPE_INTERM;

        if (CimStringUtils.equals(objCommon.getUser().getUserID().getValue(), BizConstant.SP_RXMS_PERSON)) {
            hFRSTKSTK_TYPE = BizConstant.SP_STOCKER_TYPE_RETICLEPOD;
        } else {
            hFRSTKSTK_TYPE = BizConstant.SP_STOCKER_TYPE_AUTO;
        }

        List<Infos.AvailableStocker> availableStockerList = new ArrayList<>();

        List<CimStockerDO> stockerList = cimJpaRepository.query("SELECT STOCKER_ID, ID, DESCRIPTION\n" +
                "  FROM OMSTOCKER\n" +
                " WHERE (E10_STATE_ID = ?1 OR E10_STATE_ID = ?2)\n" +
                "   AND (STOCKER_TYPE = ?3 OR STOCKER_TYPE = ?4)", CimStockerDO.class, hFRSTKE10_STATE, hFRSTKE10_STATE_1, hFRSTKSTK_TYPE, lvhSTK_TYPE);

        for (CimStockerDO stocker : stockerList) {
            Infos.AvailableStocker availableStocker = new Infos.AvailableStocker();
            availableStockerList.add(availableStocker);
            ObjectIdentifier stockerID = new ObjectIdentifier();
            availableStocker.setStockerID(stockerID);
            stockerID.setValue(stocker.getStockerID());
            stockerID.setReferenceKey(stocker.getId());
            availableStocker.setStockerName(stocker.getDescription());
        }

        Validations.check(CimArrayUtils.isEmpty(availableStockerList), retCodeConfig.getNotFoundAvailStk());

        return availableStockerList;
    }


    @Override
    public Boolean stockerInventoryStateGet(Infos.ObjCommon objCommon, ObjectIdentifier stockerId) {
        CimStorageMachine aStorageMachine = baseCoreFactory.getBO(CimStorageMachine.class, stockerId);
        Validations.check(aStorageMachine==null, retCodeConfig.getNotFoundStocker());

        return aStorageMachine.isInventoryRequested();
    }

    @Override
    public void stockerInventoryStateChange(Infos.ObjCommon objCommon, ObjectIdentifier stockerID, boolean inventoryRequestedFlag) {
        CimStorageMachine stocker = baseCoreFactory.getBO(CimStorageMachine.class, stockerID);
        Validations.check (CimObjectUtils.isEmpty(stocker), retCodeConfig.getNotFoundStocker());

        CimPerson person = baseCoreFactory.getBO(CimPerson.class, objCommon.getUser().getUserID());
        Validations.check (CimObjectUtils.isEmpty(person), new OmCode(retCodeConfig.getNotFoundPerson(), objCommon.getUser().getUserID().getValue()));

        boolean inventoryReqFlag = stocker.isInventoryRequested();
        if (CimBooleanUtils.isFalse(inventoryReqFlag)) {
            if (CimBooleanUtils.isFalse(inventoryRequestedFlag)) {
                stocker.setLastClaimedTimeStamp(objCommon.getTimeStamp().getReportTimeStamp());
                stocker.setLastClaimedPerson(person);
                stocker.setLastInventoryReportTimeStamp(objCommon.getTimeStamp().getReportTimeStamp());
            } else {
                stocker.makeInventoryRequestedOn();
                stocker.setLastClaimedTimeStamp(objCommon.getTimeStamp().getReportTimeStamp());
                stocker.setLastClaimedPerson(person);
            }
        } else {
            if (CimBooleanUtils.isTrue(inventoryRequestedFlag)) {
                Validations.check(retCodeConfig.getStockerInventoryInProcess());
            } else {
                stocker.makeInventoryRequestedOff();
                stocker.setLastClaimedTimeStamp(objCommon.getTimeStamp().getReportTimeStamp());
                stocker.setLastClaimedPerson(person);
                stocker.setLastInventoryReportTimeStamp(objCommon.getTimeStamp().getReportTimeStamp());
            }
        }
    }

    @Override
    public Results.ReticleStocInfoInqResult stockerFillInTxPDQ006DR(Infos.ObjCommon objCommon, ObjectIdentifier stockerID) {
        Results.ReticleStocInfoInqResult reticleStocInfoInqResult = new Results.ReticleStocInfoInqResult();
        /*******************************/
        /*    SELECT SQL FROM FRSTK    */
        /*******************************/
        String sql = "";
        CimStockerDO example = new CimStockerDO();
        example.setStockerID(stockerID.getValue());
        CimStockerDO cimStockerDO = cimJpaRepository.findOne(Example.of(example)).orElse(null);
        if (!CimObjectUtils.isEmpty(cimStockerDO)){
            reticleStocInfoInqResult.setStockerID(stockerID);
            reticleStocInfoInqResult.setStockerType(cimStockerDO.getStockerType());
            reticleStocInfoInqResult.setE10Status(new ObjectIdentifier(cimStockerDO.getCurE10State()));
            reticleStocInfoInqResult.setStatusChangeTimeStamp(CimDateUtils.convertToSpecString(cimStockerDO.getStateChangeTime()));
            reticleStocInfoInqResult.setActualStatusChangeTimeStamp(CimDateUtils.convertToSpecString(cimStockerDO.getActualStateChangeTime()));
            reticleStocInfoInqResult.setDescription(cimStockerDO.getDescription());
            reticleStocInfoInqResult.setStockerStatusCode(new ObjectIdentifier(cimStockerDO.getCurStateId(), cimStockerDO.getCurStateObj()));
            reticleStocInfoInqResult.setActualStatusCode(new ObjectIdentifier(cimStockerDO.getCurActualStateId(),cimStockerDO.getCurActualStateObj()));
            /********************************************/
            /*    SELECT SQL FROM OMEQPST for CURENT    */
            /********************************************/
            sql = "SELECT  EQP_STATE_NAME,\n" +
                  "DESCRIPTION\n" +
                  "FROM    OMEQPST\n" +
                  "WHERE   ID = ?";
            List<Object[]> equipmentStateList1 = cimJpaRepository.query(sql,cimStockerDO.getCurStateObj());
            Object[] equipmentState1 = equipmentStateList1.get(0);
            if (!CimObjectUtils.isEmpty(equipmentState1)){
                reticleStocInfoInqResult.setStatusName((String) equipmentState1[0]);
                reticleStocInfoInqResult.setStatusDescription((String) equipmentState1[1]);
            }
            /*******************************************/
            /*    SELECT SQL FROM OMEQPST for ACTUAL   */
            /*******************************************/
            if (CimStringUtils.isNotEmpty(cimStockerDO.getCurActualStateObj())){
                sql = " SELECT  EQP_STATE_NAME,\n" +
                        "DESCRIPTION,\n" +
                        "E10_STATE_RKEY,\n" +
                        "E10_STATE_ID\n" +
                        "FROM    OMEQPST\n" +
                        "WHERE   ID = ?";
                List<Object[]> equipmentStateList2 = cimJpaRepository.query(sql,cimStockerDO.getCurActualStateObj());
                Object[] equipmentState2 = equipmentStateList2.get(0);
                if (!CimObjectUtils.isEmpty(equipmentState2)){
                    reticleStocInfoInqResult.setActualStatusName((String) equipmentState2[0]);
                    reticleStocInfoInqResult.setActualStatusDescription((String) equipmentState2[1]);
                    reticleStocInfoInqResult.setActualE10Status(new ObjectIdentifier((String)equipmentState2[3], (String) equipmentState2[2]));
                }
            }
        }
        /***********************************/
        /*    SELECT SQL FROM OMRTCLPOD    */
        /***********************************/
        sql = "SELECT  ID,\n" +
               "RTCLPOD_ID \n"+
              "FROM    OMRTCLPOD\n" +
              "WHERE   EQP_ID = ?1\n" +
              "AND XFER_STATE IN (?2, ?3, ?4, ?5, ?6)";
        List<Object[]> reticlePodDOList = cimJpaRepository.query(sql,
                stockerID.getValue(), BizConstant.SP_TRANSSTATE_SHELFIN,
                BizConstant.SP_TRANSSTATE_MANUALIN, BizConstant.SP_TRANSSTATE_STATIONIN,
                BizConstant.SP_TRANSSTATE_BAYIN, BizConstant.SP_TRANSSTATE_ABNORMALIN);

        /************************************/
        /*    FETCCH DATA FROM OMRTCLPOD    */
        /************************************/
        if (!CimObjectUtils.isEmpty(reticlePodDOList)){
            List<Infos.ReticlePodInStocker> reticlePodInStockerList = new ArrayList<>();
            reticleStocInfoInqResult.setReticlePodInStocker(reticlePodInStockerList);
            for (Object[] reticlePod : reticlePodDOList){
                Infos.ReticlePodInStocker reticlePodInStocker = new Infos.ReticlePodInStocker();
                reticlePodInStockerList.add(reticlePodInStocker);
                reticlePodInStocker.setReticlePodID(new ObjectIdentifier((String)reticlePod[1], (String)reticlePod[0]));
                /****************************************/
                /*    SELECT SQL FROM OMRTCLPOD_RTCL    */
                /****************************************/
                sql = "SELECT  RTCL_ID,\n" +
                      "RTCL_RKEY \n" +
                      "FROM    OMRTCLPOD_RTCL\n" +
                      "WHERE   REFKEY = ?\n" +
                      "AND RTCL_ID IS NOT NULL";
                List<Object[]> reticelPodToReticleList = cimJpaRepository.query(sql, (String)reticlePod[0]);
                List<ObjectIdentifier> reticleList = new ArrayList<>();
                reticlePodInStocker.setReticleID(reticleList);
                if (!CimObjectUtils.isEmpty(reticelPodToReticleList)) {
                    for (Object[] Reticle : reticelPodToReticleList) {
                        reticleList.add(new ObjectIdentifier((String)Reticle[0], (String) Reticle[1]));
                    }
                }
            }
        }
        /********************************/
        /*    SELECT SQL FROM OMPDRBL    */
        /********************************/
        sql = "SELECT  PDRBL_ID\n" +
              "FROM    OMPDRBL\n" +
              "WHERE   EQP_ID = ?\n" +
              "AND XFER_STATE IN (?, ?, ?, ?, ?)\n" +
              "AND MTRL_CONT_RKEY IS NULL";
        List<Object[]> durableList = cimJpaRepository.query(sql,stockerID.getValue(), BizConstant.SP_TRANSSTATE_SHELFIN,
                BizConstant.SP_TRANSSTATE_MANUALIN, BizConstant.SP_TRANSSTATE_STATIONIN,
                BizConstant.SP_TRANSSTATE_BAYIN, BizConstant.SP_TRANSSTATE_ABNORMALIN);
        List<ObjectIdentifier> reticleIDList = new ArrayList<>();
        reticleStocInfoInqResult.setReticleID(reticleIDList);
        if (!CimObjectUtils.isEmpty(durableList)) {
            for (Object[] durable : durableList) {
                reticleIDList.add(new ObjectIdentifier((String) durable[0]));
            }
        }
        return reticleStocInfoInqResult;
    }

    @Override
    public List<Infos.EqpStockerStatus> stockerUTSPriorityOrderGetByLotAvailability(Infos.ObjCommon objCommon, List<Infos.EqpStockerStatus> stockerInfo, ObjectIdentifier lotID) {
        List<Infos.EqpStockerStatus> eqpStockerStatusList = new ArrayList<>();
        if (!CimObjectUtils.isEmpty(stockerInfo)){
            int count = 0;
            for(Infos.EqpStockerStatus eqpStockerStatus : stockerInfo){
                /**************************************/
                /*  Check the stocker is UTS or not   */
                /**************************************/
                if(!eqpStockerStatus.isOhbFlag()){
                    continue;
                }
                /*************************/
                /*  Check UTS capacity   */
                /*************************/
                log.info("Step1 - stockerUTS_vacantSpace_CheckDR");
                Outputs.ObjStockerUTSVacantSpaceCheckDROut objStockerUTSVacantSpaceCheckDROutRetCode = stockerUTSVacantSpaceCheckDR(objCommon, eqpStockerStatus.getStockerID());
                if (objStockerUTSVacantSpaceCheckDROutRetCode.getVacantSpace() == 0){
                    continue;
                }
                /*****************************/
                /*  Check UTS availability   */
                /*****************************/
                log.info("Step2 - machineState_availability_Check");
                Boolean objMachineStateAvailabilityCheckOutRetCode = equipmentMethod.machineStateAvailabilityCheck(objCommon, eqpStockerStatus.getStockerStatus(), lotID);

                if (!objMachineStateAvailabilityCheckOutRetCode){
                    continue;
                }
                /********************************/
                /*  Reassign stocker priority   */
                /********************************/
                eqpStockerStatus.setStockerPriority(String.valueOf(count));
                eqpStockerStatusList.add(eqpStockerStatus);
                count++;
            }
        }

        return eqpStockerStatusList;
    }

    @Override
    public List<Infos.EqpStockerStatus> stockerPriorityOrderGetByLotAvailability(Infos.ObjCommon objCommon, List<Infos.EqpStockerStatus> stockerInfo, ObjectIdentifier lotID) {
        List<Integer> availSTKNumber = new ArrayList<>();
        List<Integer> unavailSTKNumber = new ArrayList<>();
        if (!CimObjectUtils.isEmpty(stockerInfo)){
            int i = 0;
            for (Infos.EqpStockerStatus eqpStockerStatus : stockerInfo){
                /*********************************/
                /*  Check stocker availability   */
                /*********************************/
                log.info("machineState_availability_Check(Check stocker availability)");
                Boolean objMachineStateAvailabilityCheckOutRetCode = equipmentMethod.machineStateAvailabilityCheck(objCommon, eqpStockerStatus.getStockerStatus(), lotID);

                if (objMachineStateAvailabilityCheckOutRetCode){
                    availSTKNumber.add(i);
                } else {
                    unavailSTKNumber.add(i);
                }
                i++;
            }
        }
        /*****************************/
        /*  Set available stockers   */
        /*****************************/
        List<Infos.EqpStockerStatus> eqpStockerStatusList = new ArrayList<>();
        int totalCnt = 0;
        if (!CimObjectUtils.isEmpty(availSTKNumber)){
            for (Integer availCount : availSTKNumber){
                Infos.EqpStockerStatus eqpStockerStatus = stockerInfo.get(availCount);
                eqpStockerStatus.setStockerPriority(String.valueOf(totalCnt));
                eqpStockerStatusList.add(eqpStockerStatus);
                totalCnt++;
            }
        }
        /*******************************/
        /*  Set unavailable stockers   */
        /*******************************/
        if (!CimObjectUtils.isEmpty(unavailSTKNumber)){
            for (Integer unavailCount : unavailSTKNumber){
                Infos.EqpStockerStatus eqpStockerStatus = stockerInfo.get(unavailCount);
                eqpStockerStatus.setStockerPriority(String.valueOf(totalCnt));
                eqpStockerStatusList.add(eqpStockerStatus);
                totalCnt++;
            }
        }
        return eqpStockerStatusList;
    }

    @Override
    public Outputs.ObjStockerUTSVacantSpaceCheckDROut stockerUTSVacantSpaceCheckDR(Infos.ObjCommon objCommon, ObjectIdentifier stockerID) {
        Outputs.ObjStockerUTSVacantSpaceCheckDROut objStockerUTSVacantSpaceCheckDROut = new Outputs.ObjStockerUTSVacantSpaceCheckDROut();
        /**************************/
        /*  Get UTS max capacity  */
        /**************************/
        String sql = "SELECT MAX_OHB_CAPACITY\n" +
                "                 FROM   OMSTOCKER\n" +
                "                 WHERE  STOCKER_ID=?";
        List<Object[]> maxUTSCapacityQuery = cimJpaRepository.query(sql, new Object[]{stockerID.getValue()});
        Integer maxUTSCapcity = 0;
        if (!CimObjectUtils.isEmpty(maxUTSCapacityQuery)){
            maxUTSCapcity = CimNumberUtils.intValue((Number) maxUTSCapacityQuery.get(0)[0]);
        }
        objStockerUTSVacantSpaceCheckDROut.setMaxCapacity(maxUTSCapcity.longValue());
        /*********************************/
        /*  Check cassette count in UTS  */
        /*********************************/
        sql = "SELECT COUNT(ID)\n" +
                "                 FROM   OMCARRIER\n" +
                "                 WHERE  EQP_ID=?\n" +
                "                 AND    XFER_STATE LIKE ? ";
        List<Object[]> castCntQuery = cimJpaRepository.query(sql, new Object[]{stockerID.getValue(), "%I"});
        int castCnt = 0;
        if (!CimObjectUtils.isEmpty(castCntQuery)){
            castCnt = CimNumberUtils.intValue((Number) castCntQuery.get(0)[0]);
        }
        /*******************************************/
        /*  Check cassette count conveying to UTS  */
        /*******************************************/

        sql = "SELECT COUNT(CARRIER_ID)\n" +
                "                 FROM   OTXFERREQ\n" +
                "                 WHERE  DEST_MACHINE_ID=?";
        List<Object[]> totalCountQuery = cimJpaRepository.query(sql, new Object[]{stockerID.getValue()});
        int totalCount = 0;
        if (!CimObjectUtils.isEmpty(totalCountQuery)){
            totalCount = CimNumberUtils.intValue((Number) totalCountQuery.get(0)[0]);
        }
        castCnt = castCnt + totalCount;
        /*****************************/
        /*  Set vacant sapce of UTS  */
        /*****************************/
        objStockerUTSVacantSpaceCheckDROut.setVacantSpace((long) (maxUTSCapcity - castCnt));
        if (objStockerUTSVacantSpaceCheckDROut.getVacantSpace() < 0){
            objStockerUTSVacantSpaceCheckDROut.setVacantSpace(0L);
        }
        return objStockerUTSVacantSpaceCheckDROut;
    }

    @Override
    public Outputs.ObjStockerTypeGetDROut stockerTypeGetDR(Infos.ObjCommon objCommon, ObjectIdentifier stockerID) {
        Outputs.ObjStockerTypeGetDROut objStockerTypeGetDROut = new Outputs.ObjStockerTypeGetDROut();
        com.fa.cim.newcore.bo.machine.CimStorageMachine stocker = baseCoreFactory.getBO(com.fa.cim.newcore.bo.machine.CimStorageMachine.class, stockerID);
//        Validations.check(stocker == null, retCodeConfig.getNotFoundStocker());
        if (null != stocker && (CimStringUtils.equals(stocker.getStockerType(), BizConstant.SP_STOCKER_TYPE_AUTO) ||
                CimStringUtils.equals(stocker.getStockerType(), BizConstant.SP_STOCKER_TYPE_INTERM) ||
                CimStringUtils.equals(stocker.getStockerType(), BizConstant.SP_STOCKER_TYPE_SHELF) ||
                CimStringUtils.equals(stocker.getStockerType(), BizConstant.SP_STOCKER_TYPE_RETICLE) ||
                CimStringUtils.equals(stocker.getStockerType(), BizConstant.SP_STOCKER_TYPE_FIXTURE) ||
                CimStringUtils.equals(stocker.getStockerType(), BizConstant.SP_STOCKER_TYPE_INTERBAY) ||
                CimStringUtils.equals(stocker.getStockerType(), BizConstant.SP_STOCKER_TYPE_INTRABAY) ||
                CimStringUtils.equals(stocker.getStockerType(), BizConstant.SP_STOCKER_TYPE_RETICLESHELF) ||
                CimStringUtils.equals(stocker.getStockerType(), BizConstant.SP_STOCKER_TYPE_RETICLEPOD) ||
                CimStringUtils.equals(stocker.getStockerType(), BizConstant.SP_STOCKER_TYPE_BARERETICLE) ||
                CimStringUtils.equals(stocker.getStockerType(), BizConstant.SP_STOCKER_TYPE_ERACK))) {
            objStockerTypeGetDROut.setStockerType(stocker.getStockerType());
            objStockerTypeGetDROut.setUtsFlag(stocker.isUTSStocker());
            objStockerTypeGetDROut.setMaxUtsCapacity(stocker.getMaxUtsCapacity());
            return objStockerTypeGetDROut;
        } else {
            Validations.check(true, retCodeConfig.getUndefinedStockerType());
        }
        return objStockerTypeGetDROut;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @param strObjCommonIn
     * @param stockerID
     * @return java.util.List<com.fa.cim.dto.Infos.StockerEqp>
     * @exception
     * @author ho
     * @date 2020/3/17 14:40
     */
    @Override
    public List<Infos.StockerEqp> stockerUTSRelatedEquipmentListGetDR ( Infos.ObjCommon strObjCommonIn, ObjectIdentifier stockerID) {
        String hFREQP_UTSSTK_ID="";
        hFREQP_UTSSTK_ID=stockerID.getValue();

        /*-----------------------------------------*/
        /*   SELECT SQL FROM FREQP, FREQP_STK      */
        /*-----------------------------------------*/
        String sql="SELECT  OMEQP_OHB.IDX_NO,\n" +
                "                OMEQP.EQP_ID,\n" +
                "                OMEQPST.EQP_AVAIL_FLAG\n" +
                "        FROM    OMEQP, OMEQP_OHB, OMEQPST\n" +
                "        WHERE   OMEQP.ID = OMEQP_OHB.REFKEY\n" +
                "        AND     OMEQP_OHB.STOCKER_ID     = ?\n" +
                "        AND     OMEQP.EQP_STATE_ID   = OMEQPST.EQP_STATE_ID\n" +
                "        ORDER BY IDX_NO, EQP_ID";

        List<Object[]> Kstk1 = cimJpaRepository.query(sql, hFREQP_UTSSTK_ID);

        /*------------------------------------------*/
        /*   Loop for Equipment Sequence length     */
        /*------------------------------------------*/
        int count = 0;
        int EXPAND_LEN = 5;
        int eqpLen = EXPAND_LEN ;
        List<Infos.StockerEqp> strStockerEqpSeq=new ArrayList<>(eqpLen);

        for (Object[] kstk1:Kstk1) {
            /*-------------------------*/
            /*   Initialize  DATA      */
            /*-------------------------*/
            String hFREQPEQP_ID="";

            Long hFREQP_UTSd_SeqNo;
            Boolean hFREQPAVAIL_STATE_FLAG;
            hFREQP_UTSd_SeqNo = CimLongUtils.longValue(kstk1[0]);
            hFREQPEQP_ID = CimObjectUtils.toString(kstk1[1]);
            hFREQPAVAIL_STATE_FLAG = CimBooleanUtils.convert(kstk1[2]);

            strStockerEqpSeq.add(new Infos.StockerEqp());
            strStockerEqpSeq.get(count).setPriority  ( hFREQP_UTSd_SeqNo);
            strStockerEqpSeq.get(count).setEquipmentID ( ObjectIdentifier.buildWithValue(hFREQPEQP_ID));
            strStockerEqpSeq.get(count).setAvailableStateFlag  ( hFREQPAVAIL_STATE_FLAG);
            count++;
        }

        return strStockerEqpSeq;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @param strObjCommonIn
     * @param stockerID
     * @param resourceID
     * @param reticlePodID
     * @param claimMemo
     * @return void
     * @exception
     * @author ho
     * @date 2020/3/19 17:47
     */
    public void stockerReticlePodUnload(
            Infos.ObjCommon strObjCommonIn,
            ObjectIdentifier stockerID,
            ObjectIdentifier resourceID,
            ObjectIdentifier reticlePodID,
            String claimMemo, Infos.ShelfPosition shelfPosition) {
        //=========================================================================
        // Get object reference from input parameter
        //=========================================================================
        CimStorageMachine aPosStorageMachine = baseCoreFactory.getBO(CimStorageMachine.class, stockerID);

        Validations.check(null == aPosStorageMachine, retCodeConfig.getNotFoundStocker());

        //=========================================================================
        // Get reticle pod port information
        //=========================================================================
        // stocker_reticlePodPortInfo_GetDR
        List<Infos.ReticlePodPortInfo> strStockerReticlePodPortInfoGetDROut = stockerReticlePodPortInfoGetDR(
                strObjCommonIn,
                stockerID);

        Boolean bPortFound = FALSE;

        int lenReticlePodPortInfoSeq = CimArrayUtils.getSize(strStockerReticlePodPortInfoGetDROut);
        if (CimArrayUtils.isNotEmpty(strStockerReticlePodPortInfoGetDROut)) {
            for (Infos.ReticlePodPortInfo reticlePodPortInfo : strStockerReticlePodPortInfoGetDROut) {
                if (ObjectIdentifier.equalsWithValue(reticlePodPortInfo.getReticlePodPortID(), resourceID)) {
                    bPortFound = TRUE;

                    if (ObjectIdentifier.isEmptyWithValue(reticlePodPortInfo.getLoadedReticlePodID())) {
                        log.debug("no reticle pod is loaded");
                        Validations.check(true, retCodeConfig.getNotFoundReticlePod());
                    } else if (!ObjectIdentifier.equalsWithValue(reticlePodPortInfo.getLoadedReticlePodID(), reticlePodID)) {
                        log.debug("different reticle pod is loaded");
                        Validations.check(true, retCodeConfig.getNotFoundReticlePod());
                    }
                    break;
                }
            }
        }

        //=========================================================================
        // check reticle pod port found or not
        //=========================================================================
        Validations.check(CimBooleanUtils.isFalse(bPortFound), retCodeConfig.getRspportNotFound(), ObjectIdentifier.fetchValue(stockerID), ObjectIdentifier.fetchValue(resourceID));


        //=========================================================================
        // Update stocker reticle pod port information
        //=========================================================================

        Infos.StockerLoadLotDeleteIn strStockerLoadLotDeleteIn = new Infos.StockerLoadLotDeleteIn();
        strStockerLoadLotDeleteIn.setStockerID(stockerID);
        strStockerLoadLotDeleteIn.setReticlePodPortID(resourceID);

        stockerLoadLotDelete(
                strObjCommonIn,
                strStockerLoadLotDeleteIn);

        //=========================================================================
        // Maintain reticle information in reticle pod
        //=========================================================================
        reticleMethod.reticlePodTransferStateChange(
                strObjCommonIn,
                stockerID,
                null,
                reticlePodID,
                BizConstant.SP_TRANSSTATE_STATIONOUT,
                "",
                claimMemo, shelfPosition);
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @param strObjCommonIn
     * @param strStockerLoadLotDeleteIn
     * @return void
     * @exception
     * @author ho
     * @date 2020/3/20 12:55
     */
    public void stockerLoadLotDelete(Infos.ObjCommon strObjCommonIn, Infos.StockerLoadLotDeleteIn strStockerLoadLotDeleteIn) {
        /*----------------*/
        /*   Initialize   */
        /*----------------*/

        //Localization
        ObjectIdentifier stockerID, reticlePodPortID;
        stockerID = strStockerLoadLotDeleteIn.getStockerID();
        reticlePodPortID = strStockerLoadLotDeleteIn.getReticlePodPortID();

        /*----------------------------------------------*/
        /*   Get object reference from input parameter  */
        /*----------------------------------------------*/
        CimStorageMachine aPosStorageMachine = baseCoreFactory.getBO(CimStorageMachine.class, stockerID);

        /*----------------------------------------------------------*/
        /*   Get reticle pod port object reference from PosMachine  */
        /*----------------------------------------------------------*/
        CimReticlePodPortResource aPosReticlePodPort = aPosStorageMachine.findReticlePodPortResourceNamed(reticlePodPortID.getValue());

        Validations.check(null == aPosReticlePodPort, retCodeConfig.getRspportNotFound(), ObjectIdentifier.fetchValue(stockerID), ObjectIdentifier.fetchValue(reticlePodPortID));

        /*--------------------------------*/
        /*   Get reticle pod port object  */
        /*--------------------------------*/
        CimReticlePod aReservedReticlePod = aPosReticlePodPort.getTransferReservedReticlePod();

        CimReticlePod aNilReticlePod = null;
        if (null != aReservedReticlePod) {
            /*---------------------------------------------------*/
            /*   Cancel reticle pod - reticle pod port relation  */
            /*---------------------------------------------------*/
            aPosReticlePodPort.setTransferReservedReticlePod(aNilReticlePod);
        }

        aPosReticlePodPort.setDispatchState(BizConstant.SP_PORTRSC_DISPATCHSTATE_REQUIRED);

        aPosReticlePodPort.setDispatchTimeStamp(CimDateUtils.getTimestampAsString(strObjCommonIn.getTimeStamp().getReportTimeStamp()));

        aPosReticlePodPort.setReserveState(BizConstant.SP_RETICLEPODPORT_NOTRESERVED);

        aPosReticlePodPort.setLoadedReticlePod(aNilReticlePod);

        /*----------------------*/
        /*   Return to Caller   */
        /*----------------------*/
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @param strObjCommonIn
     * @param stockerID
     * @return java.util.List<com.fa.cim.dto.Infos.ReticlePodPortInfo>
     * @exception
     * @author ho
     * @date 2020/3/20 12:40
     */
    public List<Infos.ReticlePodPortInfo> stockerReticlePodPortInfoGetDR(Infos.ObjCommon strObjCommonIn, ObjectIdentifier stockerID ) {
        String sqlca;

        //-------------------------------
        //  Get StockerType
        //-------------------------------
        Outputs.ObjMachineTypeGetOut strMachineTypeGetOut;

        // machine_type_Get
        strMachineTypeGetOut = equipmentMethod.machineTypeGet(
                strObjCommonIn,
                stockerID );

        if( CimBooleanUtils.isFalse(strMachineTypeGetOut.isBStorageMachineFlag())  ) {
            Validations.check(true,retCodeConfig.getNotFoundStocker());
        }

        if (!CimStringUtils.equals( strMachineTypeGetOut.getStockerType(), BizConstant.SP_STOCKER_TYPE_BARERETICLE) &&
                !CimStringUtils.equals( strMachineTypeGetOut.getStockerType(), BizConstant.SP_STOCKER_TYPE_RETICLEPOD)) {
            log.debug( "StockerType is not BareReticle, ReticlePod.");
            Validations.check(true,retCodeConfig.getInvalidStockerType(),stockerID.getValue());
        }

        //-------------------------------
        //  Get OnlineMode of Stocker
        //-------------------------------

        String strStockeronlineModeGetout;
        // stocker_onlineMode_Get
        strStockeronlineModeGetout = stockerOnlineModeGet(
                strObjCommonIn,
                stockerID );

        String hFRRSPPORTEQP_ID            ="";
        String hFRRSPPORTPORT_ID           ="";
        String hFRRSPPORTACCESS_MODE       ="";
        String hFRRSPPORTPORT_STATE        ="";
        String hFRRSPPORTSTATE_CHG_TIME    ="";
        String hFRRSPPORTSTATE_CHG_USER_ID ="";
        String hFRRSPPORTRTCLPOD_ID        ="";
        String hFRRSPPORTRESERVE_STATE     ="";
        String hFRRSPPORTXFRRSV_RTCLPOD_ID ="";
        String hFRRSPPORTXFRRSV_TIME       ="";
        String hFRRSPPORTDISPATCH_STATE    ="";
        String hFRRSPPORTDISPATCH_TIME     ="";

        //=========================================================================
        // select reticle pod port information from FRRSPPORT
        //=========================================================================

        hFRRSPPORTEQP_ID = stockerID.getValue();

        List<Object[]> RSPPORT1=cimJpaRepository.query("SELECT\n" +
                "                PORT_ID,\n" +
                "                ACCESS_MODE,\n" +
                "                PORT_STATE,\n" +
                "                LAST_STATE_CHG_TIME,\n" +
                "                LAST_STATE_CHG_USER_ID,\n" +
                "                RTCLPOD_ID,\n" +
                "                RSV_STATE,\n" +
                "                XFER_RSV_RTCLPOD_ID,\n" +
                "                XFER_RSV_TIME,\n" +
                "                PORT_DISPATCH_STATE,\n" +
                "                DISPATCH_TIME\n" +
                "        FROM   OMRTCLPODPORT\n" +
                "        WHERE  EQP_ID = ?",hFRRSPPORTEQP_ID);


        int nRSPPortCount = 0;
        int EXPAND_LEN = 5 ;
        int portLen = EXPAND_LEN ;

        List<Infos.ReticlePodPortInfo> strStockerReticlePodPortInfoGetDROut=new ArrayList<>();

        for (Object[] RSPPORT:RSPPORT1) {
            hFRRSPPORTPORT_ID= CimObjectUtils.toString(RSPPORT[0]);
            hFRRSPPORTACCESS_MODE= CimObjectUtils.toString(RSPPORT[1]);
            hFRRSPPORTPORT_STATE= CimObjectUtils.toString(RSPPORT[2]);
            hFRRSPPORTSTATE_CHG_TIME= CimObjectUtils.toString(RSPPORT[3]);
            hFRRSPPORTSTATE_CHG_USER_ID= CimObjectUtils.toString(RSPPORT[4]);
            hFRRSPPORTRTCLPOD_ID= CimObjectUtils.toString(RSPPORT[5]);
            hFRRSPPORTRESERVE_STATE= CimObjectUtils.toString(RSPPORT[6]);
            hFRRSPPORTXFRRSV_RTCLPOD_ID= CimObjectUtils.toString(RSPPORT[7]);
            hFRRSPPORTXFRRSV_TIME= CimObjectUtils.toString(RSPPORT[8]);
            hFRRSPPORTDISPATCH_STATE= CimObjectUtils.toString(RSPPORT[9]);
            hFRRSPPORTDISPATCH_TIME= CimObjectUtils.toString(RSPPORT[10]);

            if( nRSPPortCount >= portLen ) {
                portLen += EXPAND_LEN;
            }

            strStockerReticlePodPortInfoGetDROut.add(new Infos.ReticlePodPortInfo());
            strStockerReticlePodPortInfoGetDROut.get(nRSPPortCount).setReticlePodPortID            (ObjectIdentifier.buildWithValue(hFRRSPPORTPORT_ID));
            strStockerReticlePodPortInfoGetDROut.get(nRSPPortCount).setAccessMode                  (hFRRSPPORTACCESS_MODE);

            if(!CimStringUtils.equals(strStockeronlineModeGetout, BizConstant.SP_EQP_ONLINEMODE_OFFLINE)) {
                strStockerReticlePodPortInfoGetDROut.get(nRSPPortCount).setPortStatus (hFRRSPPORTPORT_STATE);
            } else {
                strStockerReticlePodPortInfoGetDROut.get(nRSPPortCount).setPortStatus (BizConstant.SP_PORTRSC_PORTSTATE_UNKNOWN);
            }

            strStockerReticlePodPortInfoGetDROut.get(nRSPPortCount).setPortStatusChangeTimestamp         (hFRRSPPORTSTATE_CHG_TIME);
            strStockerReticlePodPortInfoGetDROut.get(nRSPPortCount).setPortStatusChangeUserID(ObjectIdentifier.buildWithValue(hFRRSPPORTSTATE_CHG_USER_ID));
            strStockerReticlePodPortInfoGetDROut.get(nRSPPortCount).setLoadedReticlePodID(ObjectIdentifier.buildWithValue(hFRRSPPORTRTCLPOD_ID));
            strStockerReticlePodPortInfoGetDROut.get(nRSPPortCount).setTransferReserveStatus             (hFRRSPPORTRESERVE_STATE);
            strStockerReticlePodPortInfoGetDROut.get(nRSPPortCount).setReservedReticlePodID(ObjectIdentifier.buildWithValue(hFRRSPPORTXFRRSV_RTCLPOD_ID));
            strStockerReticlePodPortInfoGetDROut.get(nRSPPortCount).setTransferReserveTimestamp          (hFRRSPPORTXFRRSV_TIME);
            strStockerReticlePodPortInfoGetDROut.get(nRSPPortCount).setDispatchStatus                    (hFRRSPPORTDISPATCH_STATE);
            strStockerReticlePodPortInfoGetDROut.get(nRSPPortCount).setDispatchTimestamp                 (hFRRSPPORTDISPATCH_TIME);

            nRSPPortCount++;
        }

        return (strStockerReticlePodPortInfoGetDROut);
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @param strObjCommonIn
     * @param strStockerOnlineModeGetIn
     * @return java.lang.String
     * @exception
     * @author ho
     * @date 2020/3/20 13:27
     */
    public String stockerOnlineModeGet(Infos.ObjCommon strObjCommonIn, ObjectIdentifier  strStockerOnlineModeGetIn ) {
        //----------------
        //  Initialize
        //----------------
        //----------------------------------
        //  Get PosStorageMachine object
        //----------------------------------
        CimStorageMachine  aPosStorageMachine;
        aPosStorageMachine = baseCoreFactory.getBO(CimStorageMachine.class,
                strStockerOnlineModeGetIn);

        //-------------------------------
        //  Get OnlineMode of Stocker
        //-------------------------------
        String strStockeronlineModeGetout;
        strStockeronlineModeGetout = aPosStorageMachine.getOnlineMode();
        return( strStockeronlineModeGetout );
    }

    @Override
    public List<Infos.ReticlePodInfoInStocker> stockerStoredReticlePodGetDR(Infos.ObjCommon strObjCommonIn, ObjectIdentifier stockerID) {
        String reticlePodSQL = "SELECT * FROM\n" +
                "\t OMRTCLPOD WHERE EQP_ID = ?1 \n" +
                "\t AND XFER_STATE IN ( ?2, ?3, ?4, ?5, ?6, ?7)";
        List<CimReticlePodDO> reticlePods = cimJpaRepository.query(reticlePodSQL, CimReticlePodDO.class,
                ObjectIdentifier.fetchValue(stockerID), BizConstant.SP_TRANSSTATE_STATIONIN, BizConstant.SP_TRANSSTATE_BAYIN,
                BizConstant.SP_TRANSSTATE_MANUALIN, BizConstant.SP_TRANSSTATE_SHELFIN, BizConstant.SP_TRANSSTATE_INTERMEDIATEIN,
                BizConstant.SP_TRANSSTATE_ABNORMALIN);
        List<Infos.ReticlePodInfoInStocker> reticlePodInfoInStockers = new ArrayList<>();
        if (CimArrayUtils.isNotEmpty(reticlePods)) {
            for (CimReticlePodDO reticlePod : reticlePods) {
                Infos.ReticlePodInfoInStocker reticlePodInfoInStocker = new Infos.ReticlePodInfoInStocker();
                reticlePodInfoInStocker.setReticlePodID(ObjectIdentifier.build(reticlePod.getReticlePodID(),reticlePod.getId()));
                // TODO: 2021/6/1 confrim add transferStatus/ReticlePodStatus/TransferRsvUser/TransferRsvFlg. because the source code is empty
                reticlePodInfoInStocker.setTransferStatus(reticlePod.getTransferState());
                reticlePodInfoInStocker.setReticlePodStatus(reticlePod.getDurableState());
                CimPerson reservePerson = baseCoreFactory.getBO(CimPerson.class, reticlePod.getXferReserveUserObj());
                if (null != reservePerson){
                    reticlePodInfoInStocker.setTransferReserveUserID(ObjectIdentifier.build(reservePerson.getIdentifier(),reservePerson.getPrimaryKey()));
                    reticlePodInfoInStocker.setTransferReserveFlag(true);
                }else {
                    reticlePodInfoInStocker.setTransferReserveFlag(false);
                }
                reticlePodInfoInStocker.setReticlePodCategoryID(ObjectIdentifier.build(reticlePod.getCategoryID(),reticlePod.getCategoryObj()));
                reticlePodInfoInStocker.setEmptyFlag(false);
                String reticlePodAndReticleSQL = "SELECT\n" +
                        "\tRTCL_ID,\n" +
                        "\tRTCL_RKEY \n" +
                        "FROM\n" +
                        "\tOMRTCLPOD,\n" +
                        "\tOMRTCLPOD_RTCL \n" +
                        "WHERE\n" +
                        "\tOMRTCLPOD.RTCLPOD_ID = ?1 \n" +
                        "\tAND OMRTCLPOD_RTCL.RTCL_ID is not null \n" +
                        "\tAND OMRTCLPOD.ID = OMRTCLPOD_RTCL.REFKEY FETCH FIRST 1 ROW ONLY";
                Object[] query = cimJpaRepository.queryOne(reticlePodAndReticleSQL, reticlePod.getReticlePodID());
                if (CimObjectUtils.isEmpty(query)) {
                    reticlePodInfoInStocker.setEmptyFlag(true);
                } else {
                    reticlePodInfoInStocker.setReticleID(ObjectIdentifier.build((String) query[0],(String)query[1]));
                }
                reticlePodInfoInStockers.add(reticlePodInfoInStocker);
            }
        }
        return reticlePodInfoInStockers;
    }

    @Override
    public void stockerReticlePodPortAccessModeChange(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, ObjectIdentifier reticlePodPortID, String newAccessMode) {
        /*----------------*/
        /*   Initialize   */
        /*----------------*/
        log.info("in para equipmentID      : {}", equipmentID );
        log.info("in para reticlePodPortID : {}", reticlePodPortID );
        log.info("in para newAccessMode    ： {}", newAccessMode );
        /*----------------------------------------------*/
        /*   Get object reference from input parameter  */
        /*----------------------------------------------*/
        CimStorageMachine aPosStorageMachine = baseCoreFactory.getBO(CimStorageMachine.class, equipmentID);
        Validations.check(null == aPosStorageMachine, retCodeConfig.getNotFoundEqp(), equipmentID);

        /*----------------------------------------------------------*/
        /*   Get reticle pod port object reference from PosMachine  */
        /*----------------------------------------------------------*/
        CimReticlePodPortResource aPosReticlePodPort = aPosStorageMachine.findReticlePodPortResourceNamed(ObjectIdentifier.fetchValue(reticlePodPortID));
        Validations.check(null == aPosReticlePodPort, retCodeConfig.getRspportNotFound(), ObjectIdentifier.fetchValue(equipmentID), ObjectIdentifier.fetchValue(reticlePodPortID));

        /*--------------------------------------------*/
        /*   Set new access mode to reticle pod port  */
        /*--------------------------------------------*/
        aPosReticlePodPort.setAccessMode(newAccessMode);
    }

    @Override
    public void stockerStatusCheckForReticleRetrieve(Infos.ObjCommon objCommon, ObjectIdentifier stockerID, ObjectIdentifier reticlePodPortID, ObjectIdentifier reticlePodID, List<Infos.MoveReticles> strMoveReticlesSeq) {
        /*-------------------------*/
        /*   Get reticle pod port  */
        /*-------------------------*/
        List<Infos.ReticlePodPortInfo>  strStockerReticlePodPortInfoGetDROut = this.stockerReticlePodPortInfoGetDR(objCommon, stockerID);

        /*-----------------------------------------------------------*/
        /*   Check input reticle pod is on reticle pod port or not   */
        /*-----------------------------------------------------------*/
        Boolean bSomeReticlePodOnRSPPort = false;
        Boolean bCorrectReticlePodOnRSPPort = false;

        int rppLen = CimArrayUtils.getSize(strStockerReticlePodPortInfoGetDROut);
        int i = 0;
        for(i=0; i<rppLen; i++) {
            log.info("Round[i] : {}", i);
            if(ObjectIdentifier.equalsWithValue(reticlePodPortID, strStockerReticlePodPortInfoGetDROut.get(i).getReticlePodPortID())) {
                log.info("Found Reticle Pod Port : {}", ObjectIdentifier.fetchValue(reticlePodPortID));
                if(ObjectIdentifier.isNotEmpty(strStockerReticlePodPortInfoGetDROut.get(i).getLoadedReticlePodID())) {
                    log.info("bSomeReticlePodOnRSPPort = TRUE");
                    bSomeReticlePodOnRSPPort = true;
                    if(ObjectIdentifier.equalsWithValue(reticlePodID, strStockerReticlePodPortInfoGetDROut.get(i).getLoadedReticlePodID())) {
                        log.info("bCorrectReticlePodOnRSPPort = TRUE|reticlePodID : {}",reticlePodID.getValue());
                        bCorrectReticlePodOnRSPPort = Boolean.TRUE;
                    }
                }
                break;
            }
        }

        Validations.check(!bSomeReticlePodOnRSPPort, retCodeConfigEx.getRtclpodNotLoaded(), ObjectIdentifier.fetchValue(reticlePodID),
                ObjectIdentifier.fetchValue(stockerID), ObjectIdentifier.fetchValue(reticlePodPortID));
        Validations.check(!bCorrectReticlePodOnRSPPort, retCodeConfigEx.getRtclpodNotLoaded(), ObjectIdentifier.fetchValue(reticlePodID),
                ObjectIdentifier.fetchValue(stockerID), ObjectIdentifier.fetchValue(reticlePodPortID));

        /*------------------------*/
        /*   Get stored reticle   */
        /*------------------------*/
        List<Infos.StoredReticle> tmpStoredReticles = this.stockerStoredReticleGetDR(objCommon, stockerID);

        /*----------------------------------------------------*/
        /*   Check requested reticle is in equipment or not   */
        /*----------------------------------------------------*/
        int mrLen = CimArrayUtils.getSize(strMoveReticlesSeq);
        for(i=0; i<mrLen; i++) {
            log.info("Round[i] : {}", i);
            Boolean bReticleFoundInEqp = false;
            int srLen = CimArrayUtils.getSize(tmpStoredReticles);
            for(int j=0; j<srLen; j++) {
                log.info("Round[j] : {}", j);
                if(ObjectIdentifier.equalsWithValue(strMoveReticlesSeq.get(i).getReticleID(), tmpStoredReticles.get(j).getReticleID())) {
                    log.info("bReticleFoundInEqp = TRUE");
                    bReticleFoundInEqp = true;
                    break;
                }
            }

            Validations.check(!bReticleFoundInEqp, retCodeConfig.getReticleNotInTheEqp(), strMoveReticlesSeq.get(i).getReticleID().getValue());
        }

        CimReticlePod anInputReticlePod = baseCoreFactory.getBO(CimReticlePod.class, reticlePodID);

        for(i=0; i<mrLen; i++) {
            log.info("Round[i] : {}", i);
            //--------------------------------------------------------------
            // Check Condition of slotNumber
            //--------------------------------------------------------------
            int nPosTotal = 0;
            nPosTotal = anInputReticlePod.getPositionTotal();
            log.info("nPosTotal : {}", nPosTotal);

            if ( strMoveReticlesSeq.get(i).getSlotNumber() > nPosTotal ||
                    strMoveReticlesSeq.get(i).getSlotNumber() <= 0 ) {
                log.error("in para slotNumber is above Max or Min");

                // Error
                Validations.check(retCodeConfig.getInvalidReticlePodPosition(), strMoveReticlesSeq.get(i).getReticleID().getValue(), strMoveReticlesSeq.get(i).getSlotNumber());
            }
        }
    }

    @Override
    public List<Infos.StoredReticle> stockerStoredReticleGetDR(Infos.ObjCommon objCommon, ObjectIdentifier stockerID) {
        String stockerSQL = "SELECT\n" +
                "\tOMPDRBL.PDRBL_ID,\n" +
                "\tOMPDRBL.ID,\n" +
                "\tOMPDRBL.DESCRIPTION,\n" +
                "\tOMPDRBL_PDRBLGRP.PDRBL_GRP_ID,\n" +
                "\tOMPDRBL_PDRBLGRP.PDRBL_GRP_RKEY,\n" +
                "\tOMPDRBL.PDRBL_STATE \n" +
                "FROM\n" +
                "\tOMSTOCKER,\n" +
                "\tOMPDRBL,\n" +
                "\tOMPDRBL_PDRBLGRP \n" +
                "WHERE\n" +
                "\tOMPDRBL.EQP_ID = ?1 \n" +
                "\tAND OMPDRBL.ID = OMPDRBL_PDRBLGRP.REFKEY \n" +
                "\tAND OMPDRBL.MTRL_CONT_ID IS NULL \n" +
                "\tAND OMPDRBL.XFER_STATE IN (?2, ?3, ?4, ?5, ?6, ?7) \n" +
                "\tAND OMSTOCKER.STOCKER_ID = OMPDRBL.EQP_ID";
        List<Object[]> stockerReticles = cimJpaRepository.query(stockerSQL, ObjectIdentifier.fetchValue(stockerID),
                BizConstant.SP_TRANSSTATE_STATIONIN, BizConstant.SP_TRANSSTATE_BAYIN, BizConstant.SP_TRANSSTATE_MANUALIN,
                BizConstant.SP_TRANSSTATE_SHELFIN, BizConstant.SP_TRANSSTATE_INTERMEDIATEIN, BizConstant.SP_TRANSSTATE_ABNORMALIN);

        return stockerReticles.stream().map(stockerReticle -> {
            Infos.StoredReticle storedReticle = new Infos.StoredReticle();
            storedReticle.setReticleID(ObjectIdentifier.build((String) stockerReticle[0], (String)stockerReticle[1]));
            storedReticle.setDescription((String) stockerReticle[2]);
            storedReticle.setReticleGroupID(ObjectIdentifier.build((String) stockerReticle[3], (String)stockerReticle[4]));
            storedReticle.setStatus((String) stockerReticle[5]);
            return storedReticle;
        }).collect(Collectors.toList());
    }

    @Override
    public void stockerReticlePodPortStateChange(Infos.ObjCommon objCommon, ObjectIdentifier stockerID, ObjectIdentifier portID, String portStatus) {
        log.debug("in para stockerID  {}", ObjectIdentifier.fetchValue(stockerID));
        log.debug("in para portID     {}", ObjectIdentifier.fetchValue(portID));
        log.debug("in para portStatus {}", portStatus);

        //------------------------------
        //       Get Equipment Object
        //------------------------------
        CimStorageMachine aPosStorageMachine = null;
        if (ObjectIdentifier.isEmptyWithRefKey(stockerID)) {
            Validations.check(ObjectIdentifier.isEmptyWithValue(stockerID), retCodeConfig.getNotFoundStocker());
            aPosStorageMachine = machineManager.findStorageMachineNamed(stockerID.getValue());
        } else {
            baseCoreFactory.getBO(CimStorageMachine.class, stockerID.getReferenceKey());
        }
        Validations.check(null == aPosStorageMachine, retCodeConfig.getNotFoundStocker());

        /*-------------------------------*/
        /*   Get Port Resource Object    */
        /*-------------------------------*/
        CimReticlePodPortResource aPosReticlePodResource = aPosStorageMachine.findReticlePodPortResourceNamed(ObjectIdentifier.fetchValue(portID));
        Validations.check(null == aPosReticlePodResource, retCodeConfig.getRspportNotFound(),
                ObjectIdentifier.fetchValue(stockerID),
                ObjectIdentifier.fetchValue(portID));

        //-------------------------------
        //       Change Port State
        //-------------------------------
        if (CimStringUtils.equals(portStatus, BizConstant.SP_PORTRSC_PORTSTATE_LOADREQ)
                || CimStringUtils.equals(portStatus, BizConstant.SP_PORTRSC_PORTSTATE_LOADCOMP)
                || CimStringUtils.equals(portStatus, BizConstant.SP_PORTRSC_PORTSTATE_UNLOADREQ)
                || CimStringUtils.equals(portStatus, BizConstant.SP_PORTRSC_PORTSTATE_UNLOADCOMP)
                || CimStringUtils.equals(portStatus, BizConstant.SP_PORTRSC_PORTSTATE_UNKNOWN)
                || CimStringUtils.equals(portStatus, BizConstant.SP_PORTRSC_PORTSTATE_DOWN)) {
            log.debug("reticlePodResource is not null");
            aPosReticlePodResource.setPortState(portStatus);
        } else {
            Validations.check(true, retCodeConfig.getUndefinedPortState(), ObjectIdentifier.fetchValue(portID), portStatus);
        }

        //----------------------------------------------
        //       Update TimeStamp and Person Info
        //----------------------------------------------
        aPosReticlePodResource.setLastStatusChangeTimeStamp(CimDateUtils.convertToSpecString(objCommon.getTimeStamp().getReportTimeStamp()));

        CimPerson aPserson = null;
        ObjectIdentifier personID = objCommon.getUser().getUserID();
        if (ObjectIdentifier.isEmptyWithRefKey(personID)) {
            aPserson = personManager.findPersonNamed(ObjectIdentifier.fetchValue(personID));
        } else {
            aPserson = baseCoreFactory.getBO(CimPerson.class, personID.getReferenceKey());
        }
        Validations.check(null == aPserson, retCodeConfig.getNotFoundPerson());

        aPosReticlePodResource.setLastStatusChangePerson(aPserson);
        aPosReticlePodResource.setLastStatusChangeTimeStamp(CimDateUtils.convertToSpecString(objCommon.getTimeStamp().getReportTimeStamp()));

        //----------------------------------------------
        //  change dispatchInfo
        //----------------------------------------------
        //LoadReq case
        if (CimStringUtils.equals(portStatus, BizConstant.SP_PORTRSC_PORTSTATE_LOADREQ)) {
            log.debug("portStatus = LoadReq");
            /*---------------------------------------*/
            /*   Set dispatch state and timestamp    */
            /*---------------------------------------*/
            aPosReticlePodResource.setDispatchState(BizConstant.SP_PORTRSC_DISPATCHSTATE_REQUIRED);
            aPosReticlePodResource.setDispatchTimeStamp(CimDateUtils.convertToSpecString(objCommon.getTimeStamp().getReportTimeStamp()));

            /*--------------------------------------------------------------*/
            /*  Clear dispatched reticle pod info                           */
            /*  This is for error case... if equipment becomes wrong,       */
            /*  this LoadReq report will refresh reserved pod information   */
            /*--------------------------------------------------------------*/
            aPosReticlePodResource.setTransferReservedReticlePod(null);
            aPosReticlePodResource.setReserveState(BizConstant.SP_RETICLEPODPORT_NOTRESERVED);
        }
        //UnlaodReq case
        else if (CimStringUtils.equals(portStatus, BizConstant.SP_PORTRSC_PORTSTATE_UNLOADREQ)) {
            log.debug("portStatus = UnloadReq");
            /*--------------------------------------*/
            /*   Set dispatch state and timestamp   */
            /*--------------------------------------*/
            aPosReticlePodResource.setDispatchState(BizConstant.SP_PORTRSC_DISPATCHSTATE_REQUIRED);
            aPosReticlePodResource.setDispatchTimeStamp(CimDateUtils.convertToSpecString(objCommon.getTimeStamp().getReportTimeStamp()));
            aPosReticlePodResource.setReserveState(BizConstant.SP_RETICLEPODPORT_NOTRESERVED);
        }
        //UnloadComp case
        else if (CimStringUtils.equals(portStatus, BizConstant.SP_PORTRSC_PORTSTATE_UNLOADCOMP)) {
            //Do Nothing for dispatch related status
            log.debug("Do Nothing for dispatch related status");
        }
        //LoadComp case
        else if (CimStringUtils.equals(portStatus, BizConstant.SP_PORTRSC_PORTSTATE_LOADCOMP)) {
            //Do Nothing for dispatch related status
            log.debug("Do Nothing for dispatch related status");
        } else {
            //Do Nothing for dispatch related status
            log.debug("Do Nothing for dispatch related status");
        }
    }

    @Override
    public void stockerReticlePodPortDispatchStateChange(Infos.ObjCommon strObjCommonIn, Inputs.StockerReticlePodPortDispatchStateChangeIn strStockerReticlePodPortDispatchStateChangeIn) {
        //Localization
        ObjectIdentifier stockerID, reticlePodPortID, reservedReticlePodID = null;
        String transferReserveStatus, transferReserveTimestamp, dispatchStatus, dispatchTimestamp = null;
        stockerID = strStockerReticlePodPortDispatchStateChangeIn.getStockerID();
        reticlePodPortID = strStockerReticlePodPortDispatchStateChangeIn.getStrReticlePodPortInfo().getReticlePodPortID();
        reservedReticlePodID = strStockerReticlePodPortDispatchStateChangeIn.getStrReticlePodPortInfo().getReservedReticlePodID();
        transferReserveStatus = strStockerReticlePodPortDispatchStateChangeIn.getStrReticlePodPortInfo().getTransferReserveStatus();
        transferReserveTimestamp = strStockerReticlePodPortDispatchStateChangeIn.getStrReticlePodPortInfo().getTransferReserveTimestamp();
        dispatchStatus = strStockerReticlePodPortDispatchStateChangeIn.getStrReticlePodPortInfo().getDispatchStatus();
        dispatchTimestamp = strStockerReticlePodPortDispatchStateChangeIn.getStrReticlePodPortInfo().getDispatchTimestamp();

        log.info("in para stockerID                {}", ObjectIdentifier.fetchValue(stockerID));
        log.info("in para reticlePodPortID         {}", ObjectIdentifier.fetchValue(reticlePodPortID));
        log.info("in para reservedReticlePodID     {}", ObjectIdentifier.fetchValue(reservedReticlePodID));
        log.info("in para transferReserveStatus    {}", transferReserveStatus);
        log.info("in para transferReserveTimestamp {}", transferReserveTimestamp);
        log.info("in para dispatchStatus           {}", dispatchStatus);
        log.info("in para dispatchTimestamp        {}", dispatchTimestamp);

        /*--------------------------------------------*/
        /*   Get stocker object from input parameter  */
        /*--------------------------------------------*/
        CimStorageMachine aPosStorageMachine = null;
        if (ObjectIdentifier.isEmptyWithRefKey(stockerID)) {
            Validations.check(ObjectIdentifier.isEmptyWithValue(stockerID), retCodeConfig.getNotFoundStocker(), "*****");
            aPosStorageMachine = machineManager.findStorageMachineNamed(stockerID.getValue());
        } else {
            aPosStorageMachine = baseCoreFactory.getBO(CimStorageMachine.class, stockerID.getReferenceKey());
        }
        Validations.check(null == aPosStorageMachine, retCodeConfig.getNotFoundStocker(), ObjectIdentifier.fetchValue(stockerID));

        /*----------------------------------------*/
        /*   Get RSP port object from PosMachine  */
        /*----------------------------------------*/
        CimReticlePodPortResource aPosReticlePodPort = aPosStorageMachine.findReticlePodPortResourceNamed(ObjectIdentifier.fetchValue(reticlePodPortID));
        Validations.check(null == aPosReticlePodPort, retCodeConfig.getRspportNotFound(), ObjectIdentifier.fetchValue(stockerID), ObjectIdentifier.fetchValue(reticlePodPortID));

        /*--------------------------------------------*/
        /*   Set dispatch status to reticle pod port  */
        /*--------------------------------------------*/
        aPosReticlePodPort.setDispatchState(dispatchStatus);
        aPosReticlePodPort.setDispatchTimeStamp(dispatchTimestamp);
        if (ObjectIdentifier.isEmptyWithValue(reservedReticlePodID)) {
            CimReticlePod aReticlePod = null;
            if (ObjectIdentifier.isEmptyWithRefKey(reservedReticlePodID)) {
                Validations.check(ObjectIdentifier.isEmptyWithValue(reservedReticlePodID), retCodeConfig.getNotFoundReticlePod(), "*****");
                aReticlePod = durableManager.findReticlePodNamed(reservedReticlePodID.getValue());
            } else {
                aReticlePod = baseCoreFactory.getBO(CimReticlePod.class, reservedReticlePodID.getReferenceKey());
            }
            Validations.check(null == aReticlePod, retCodeConfig.getNotFoundReticlePod(), ObjectIdentifier.fetchValue(reservedReticlePodID));

            aPosReticlePodPort.setReserveState(transferReserveStatus);
            aPosReticlePodPort.setTransferReservedReticlePod(aReticlePod);
            aPosReticlePodPort.setTransferReservedTimeStamp(transferReserveTimestamp);
        } else {
            aPosReticlePodPort.setReserveState(BizConstant.SP_RETICLEPODPORT_NOTRESERVED);
            aPosReticlePodPort.setTransferReservedReticlePod(null);
        }
    }

    @Override
    public void stockerReticlePodLoad(Infos.ObjCommon objCommon, ObjectIdentifier stockerID, ObjectIdentifier resourceID, ObjectIdentifier reticlePodID, String claimMemo) {
        log.debug("stockerID    {}", ObjectIdentifier.fetchValue(stockerID));
        log.debug("resourceID   {}", ObjectIdentifier.fetchValue(resourceID));
        log.debug("reticlePodID {}", ObjectIdentifier.fetchValue(reticlePodID));
        log.debug("claimMemo    {}", claimMemo);

        //=========================================================================
        // Get Base Machine Object Reference
        //=========================================================================
        boolean isStorageBool = false;
        Machine aMachine = machineManager.findMachineNamed(ObjectIdentifier.fetchValue(stockerID));
        if(null == aMachine){
            aMachine = machineManager.findStorageMachineNamed(ObjectIdentifier.fetchValue(stockerID));
        }
        Validations.check(aMachine == null, retCodeConfig.getNotFoundEqp(), stockerID);
        isStorageBool = aMachine.isStorageMachine();
        Validations.check(!isStorageBool, retCodeConfig.getNotFoundStocker());


        //=========================================================================
        // Narrow object reference to PosStorageMachine
        //=========================================================================
        CimStorageMachine aPosStorageMachine = (CimStorageMachine) aMachine;
        Validations.check(aPosStorageMachine == null, retCodeConfig.getNotFoundStocker());

        String stockerType = aPosStorageMachine.getStockerType();
        log.debug("stockerType {}", stockerType);
        Validations.check(!CimStringUtils.equals(stockerType, BizConstant.SP_STOCKER_TYPE_BARERETICLE), retCodeConfig.getInvalidStockerType());

        //=========================================================================
        // Get current port information
        //=========================================================================
        List<Infos.ReticlePodPortInfo> reticlePodPortInfos = this.stockerReticlePodPortInfoGetDR(objCommon, stockerID);

        //=========================================================================
        // Set port information
        //
        //  - If reserved reticle pod ID is different from input reticle pod id,
        //    keep that reserved reticle pod ID
        //  - If reserved reticle pod ID is the same with input reticle pod id,
        //    erase that reserved reticle pod ID
        //=========================================================================
        int portLen = CimArrayUtils.getSize(reticlePodPortInfos);
        log.debug("portLen: {}", portLen);
        boolean bReticlePodPortFound = false;

        for (Infos.ReticlePodPortInfo reticlePodPortInfo : reticlePodPortInfos) {
            log.debug("in-para    stockerID: {}", ObjectIdentifier.fetchValue(stockerID));
            log.debug("in-para   resourceID: {}", ObjectIdentifier.fetchValue(resourceID));
            log.debug("tmp reticlePodPortID: {}", ObjectIdentifier.fetchValue(reticlePodPortInfo.getReticlePodPortID()));

            if (ObjectIdentifier.equalsWithValue(resourceID, reticlePodPortInfo.getReticlePodPortID())) {
                log.debug("reticle pod port found {}", ObjectIdentifier.fetchValue(resourceID));
                bReticlePodPortFound = TRUE;
                Validations.check(!ObjectIdentifier.isEmpty(reticlePodPortInfo.getLoadedReticlePodID()), retCodeConfigEx.getDiffRtclpodLoaded(), reticlePodPortInfo.getLoadedReticlePodID(), stockerID, resourceID);
                ObjectIdentifier reservedReticlePodID = reticlePodPortInfo.getReservedReticlePodID();
                Validations.check(!ObjectIdentifier.isEmpty(reservedReticlePodID) &&
                        !ObjectIdentifier.equalsWithValue(reservedReticlePodID, reticlePodID), retCodeConfigEx.getDiffRtclpodReserved(), stockerID, resourceID, reservedReticlePodID);
                break;
            }
        }
        Validations.check(!bReticlePodPortFound, retCodeConfig.getRspportNotFound(), stockerID, resourceID);

        CimReticlePod loadPosReticlePod = baseCoreFactory.getBO(CimReticlePod.class, reticlePodID);
        Validations.check(loadPosReticlePod == null, retCodeConfig.getNotFoundReticlePod());

        CimStorageMachine destPosStorageMachine = loadPosReticlePod.getTransferDestinationStocker();
        String destStocker = !CimObjectUtils.isEmpty(destPosStorageMachine) ? destPosStorageMachine.getIdentifier() : "";

        Validations.check(!CimObjectUtils.isEmpty(destStocker) &&
                !ObjectIdentifier.equalsWithValue(destStocker, stockerID), retCodeConfigEx.getRtclpodDestDifferent(), reticlePodID, destStocker);

        this.stockerLoadLotAdd(objCommon, stockerID, resourceID, reticlePodID);

        //=========================================================================
        // Maintain reticle information in reticle pod
        //=========================================================================
        ObjectIdentifier tmpEquipmentID = null;
        reticleMethod.reticlePodTransferStateChange(
                objCommon,
                stockerID,
                tmpEquipmentID,
                reticlePodID,
                BizConstant.SP_TRANSSTATE_STATIONIN,
                "",
                claimMemo, null);
    }

    @Override
    public void stockerLoadLotAdd(Infos.ObjCommon objCommon, ObjectIdentifier stockerID, ObjectIdentifier reticlePodPortID, ObjectIdentifier loadedReticlePodID) {
        log.debug("in para stockerID          {}", ObjectIdentifier.fetchValue(stockerID));
        log.debug("in para reticlePodPortID   {}", ObjectIdentifier.fetchValue(reticlePodPortID));
        log.debug("in para loadedReticlePodID {}", ObjectIdentifier.fetchValue(loadedReticlePodID));

        /*----------------------------------------------*/
        /*   Get object reference from input parameter  */
        /*----------------------------------------------*/
        CimStorageMachine aPosStorageMachine = baseCoreFactory.getBO(CimStorageMachine.class, stockerID);
        Validations.check(aPosStorageMachine == null, retCodeConfig.getNotFoundStocker());

        /*----------------------------------------------------------*/
        /*   Get reticle pod port object reference from PosMachine  */
        /*----------------------------------------------------------*/
        CimReticlePodPortResource aPosReticlePodPort = aPosStorageMachine.findReticlePodPortResourceNamed(ObjectIdentifier.fetchValue(reticlePodPortID));
        Validations.check(CimObjectUtils.isEmpty(aPosReticlePodPort), retCodeConfig.getRspportNotFound(), stockerID, reticlePodPortID);

        /*----------------------------------------------------------*/
        /*   Get reticle pod port object reference from PosMachine  */
        /*----------------------------------------------------------*/
        CimReticlePod aPosReticlePod = baseCoreFactory.getBO(CimReticlePod.class, loadedReticlePodID);
        Validations.check(aPosReticlePod == null, retCodeConfig.getNotFoundReticlePod());

        /*--------------------------------------------------------*/
        /*   Reticle pod xfer reserve destination stocker         */
        /*--------------------------------------------------------*/
        CimStorageMachine destPosStocker = aPosReticlePod.getTransferDestinationStocker();
        if (CimObjectUtils.isEmpty(destPosStocker)) {
            log.debug("destPosMachine is NIL");
        } else {
            String destStockerID = destPosStocker.getIdentifier();
            log.debug("Destination Stocker is NOT NIL", destStockerID);
            /*------------------------------------------------------------------*/
            /*   Compare stocker that reticle pod xfer destination and to load  */
            /*------------------------------------------------------------------*/
            Validations.check(!CimObjectUtils.isEmpty(destStockerID) &&
                    !ObjectIdentifier.equalsWithValue(destStockerID, stockerID), retCodeConfigEx.getRtclpodDestDifferent(), loadedReticlePodID, destStockerID);
        }

        /*--------------------------------*/
        /*   Get reserved reticle pod ID  */
        /*--------------------------------*/
        CimReticlePod aReservedReticlePod = aPosReticlePodPort.getTransferReservedReticlePod();
        if (CimObjectUtils.isEmpty(aReservedReticlePod)) {
            log.debug("aReservedReticlePod is NIL");
        } else {
            String reservedReticlePodID = aReservedReticlePod.getIdentifier();
            log.debug("aReservedReticlePod {} is NOT NIL", reservedReticlePodID);

            /*------------------------------------------*/
            /*   Maintain reticle pod port information  */
            /*------------------------------------------*/
            if (ObjectIdentifier.equalsWithValue(loadedReticlePodID, reservedReticlePodID)) {
                aPosReticlePodPort.setTransferReservedReticlePod(null);
            }
        }

        aPosReticlePodPort.setLoadedReticlePod(aPosReticlePod);
        aPosReticlePodPort.setDispatchState(BizConstant.SP_PORTRSC_DISPATCHSTATE_REQUIRED);
        aPosReticlePodPort.setDispatchTimeStamp(objCommon.getTimeStamp().getReportTimeStamp().toString());
        aPosReticlePodPort.setReserveState(BizConstant.SP_RETICLEPODPORT_NOTRESERVED);
    }

    @Override
    public void stockerOnlineModeChange(Infos.ObjCommon objCommon, ObjectIdentifier stockerID, String onlineMode) {
        log.debug("in para stockerID        {}", ObjectIdentifier.fetchValue(stockerID));
        log.debug("in para onlineMode       {}", onlineMode);

        /*----------------------------------------------*/
        /*   Get object reference from input parameter  */
        /*----------------------------------------------*/
        CimStorageMachine aPosStorageMachine = null;
        if (ObjectIdentifier.isEmptyWithRefKey(stockerID)) {
            Validations.check(ObjectIdentifier.isEmptyWithValue(stockerID), retCodeConfig.getNotFoundStocker(), "******");
            aPosStorageMachine = machineManager.findStorageMachineNamed(stockerID.getValue());
        } else {
            aPosStorageMachine = baseCoreFactory.getBO(CimStorageMachine.class, stockerID.getReferenceKey());
        }
        Validations.check(null == aPosStorageMachine, retCodeConfig.getNotFoundStocker(), ObjectIdentifier.fetchValue(stockerID));

        aPosStorageMachine.setOnlineMode(onlineMode);
    }


    @Override
    public Results.FixtureStockerInfoInqResult stockerFillInTxPDQ003DR(Infos.ObjCommon strObjCommonIn,
                                                                       ObjectIdentifier stockerID) {
        int temp = 0;
        com.fa.cim.newcore.bo.machine.CimStorageMachine aStorageMachine = baseCoreFactory.getBO(com.fa.cim.newcore.bo.machine.CimStorageMachine.class, stockerID);
        Results.FixtureStockerInfoInqResult fixtureStockerInfoInqResult = new Results.FixtureStockerInfoInqResult();
        fixtureStockerInfoInqResult.setDescription(aStorageMachine.getDescription());
        fixtureStockerInfoInqResult.setStockerID(stockerID);
        fixtureStockerInfoInqResult.setStockerType(aStorageMachine.getStockerType());

        List<Infos.InventoryFixtureInfo> strInventoryFixtureInfo = new ArrayList<>();

        String sql = "SELECT DRBL_ID, SHELF_POSITION_X, SHELF_POSITION_Y, SHELF_POSITION_Z FROM   OMPDRBL WHERE EQP_ID = ?1 AND TRANS_STATE = 'HI'";
        List<CimDurableDO> durableDOList = new ArrayList<>();
        try {
            durableDOList = cimJpaRepository.query(sql, CimDurableDO.class, stockerID.getValue());
        }catch (CoreFrameworkException e){
            Validations.check(true, retCodeConfig.getSystemError(), "*****",
                    stockerID.getValue());
        }
        if(durableDOList.size()!=0){//这一块代码测试修改的比较大，在测试联调的时候需要注意关于这一块内容的测试
            int i=0;
            List<Infos.InventoryFixtureInfo> inventoryLotInfoList =  new ArrayList<>();
            for(CimDurableDO cimDurableDO:durableDOList){
                Infos.InventoryFixtureInfo inventoriedLotInfo = new Infos.InventoryFixtureInfo();
                inventoriedLotInfo.setXPosition(cimDurableDO.getShelfPositionX().toString());
                inventoriedLotInfo.setYPosition(cimDurableDO.getShelfPositionY().toString());
                inventoriedLotInfo.setZPosition(cimDurableDO.getShelfPositionZ().toString());
                inventoryLotInfoList.add(inventoriedLotInfo);
                temp++;
            }
            fixtureStockerInfoInqResult.setStrInventoryFixtureInfo(inventoryLotInfoList);
        }

        if(temp == 0){
            Validations.check(true,retCodeConfig.getNotFoundAvailStk());
        }else {
            fixtureStockerInfoInqResult.setStrInventoryFixtureInfo(fixtureStockerInfoInqResult.getStrInventoryFixtureInfo());
        }

        return fixtureStockerInfoInqResult;
    }
}
