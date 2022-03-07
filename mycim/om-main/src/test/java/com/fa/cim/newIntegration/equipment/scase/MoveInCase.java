package com.fa.cim.newIntegration.equipment.scase;

import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.Response;
import com.fa.cim.common.utils.CimArrayUtils;
import com.fa.cim.common.utils.CimObjectUtils;
import com.fa.cim.common.utils.CimStringUtils;
import com.fa.cim.common.utils.Validations;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.core.TestCommonData;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Results;
import com.fa.cim.newIntegration.LotOperation.scase.LotHoldCase;
import com.fa.cim.newIntegration.LotOperation.scase.LotSplitCase;
import com.fa.cim.newIntegration.LotOperation.scase.OperationSkipCase;
import com.fa.cim.newIntegration.common.TestUtils;
import com.fa.cim.newIntegration.processControl.scase.EntityInhibityCase;
import com.fa.cim.newIntegration.stb.scase.STBCase;
import com.fa.cim.newIntegration.tcase.CommonTestCase;
import com.fa.cim.newIntegration.tcase.ElectronicInformationTestCase;
import com.fa.cim.newIntegration.tcase.EquipmentTestCase;
import com.fa.cim.newIntegration.tcase.LotGeneralTestCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/9/14       ********             lightyh             create file
 *
 * @author: lightyh
 * @date: 2019/9/14 20:10
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Service
@Slf4j
public class MoveInCase {

    @Autowired
    private LotLoadToEquipmentCase lotLoadToEquipmentCase;

    @Autowired
    private EquipmentTestCase equipmentTestCase;

    @Autowired
    private STBCase stbCase;

    @Autowired
    private ElectronicInformationTestCase electronicInformationTestCase;

    @Autowired
    private LotGeneralTestCase lotGeneralTestCase;

    @Autowired
    private LotHoldCase lotHoldCase;

    @Autowired
    private OperationSkipCase operationSkipCase;

    @Autowired
    private MoveOutCase moveOutCase;

    @Autowired
    private EntityInhibityCase entityInhibityCase;

    @Autowired
    private LotSplitCase lotSplitCase;

    @Autowired
    private CommonTestCase commonTestCase;

    @Autowired
    private LotUnloadFromEquipmentCase lotUnloadFromEquipmentCase;

    @Autowired
    private StartLotsReservationCase startLotsReservationCase;

    @Autowired
    @Qualifier("testCommonDataWithDB")
    private TestCommonData testCommonData;

    @Autowired
    private TestUtils testUtils;

    @Autowired
    private RetCodeConfig retCodeConfig;

    private static final String RETICLE_EQUIPMENTID = "1TKD01_EXD01";

    public Results.MoveInReqResult moveIn_GenerateControlJob_Without_StartReservation(){
        //【step1】load
        ObjectIdentifier cassetteID = lotLoadToEquipmentCase.manual_Loading_Without_StartLotsReservation();
        // 【step2】get lotsInfoForOpeStart info
        List<ObjectIdentifier> cassetteIDs = new ArrayList<>();
        cassetteIDs.add(cassetteID);
        Results.LotsMoveInInfoInqResult lotsMoveInInfoInqResult = (Results.LotsMoveInInfoInqResult) equipmentTestCase.lotsMoveInInfoInq(testCommonData.getEQUIPMENTID(), cassetteIDs).getBody();
        //【step3】einfo/eqp_info/inq
        Results.EqpInfoInqResult eqpInfoInqResult = electronicInformationTestCase.getEqpInfo(testCommonData.getEQUIPMENTID());
        Assert.isTrue(CimArrayUtils.isEmpty(eqpInfoInqResult.getEquipmentInprocessingControlJobList()), "test fail");
        // 【step3】move in
        Results.MoveInReqResult moveInReqResult = (Results.MoveInReqResult) equipmentTestCase.movInReq(lotsMoveInInfoInqResult.getControlJobID(), lotsMoveInInfoInqResult.getEquipmentID(), lotsMoveInInfoInqResult.getPortGroupID(), false, lotsMoveInInfoInqResult.getStartCassetteList()).getBody();
        // 【step4】get lot Info
        List<Infos.LotInfo> lotInfosList = lotGeneralTestCase.getLotInfosByCassette(cassetteID);
        Infos.LotInfo lotInfo = lotInfosList.get(0);
        Assert.isTrue(lotInfo.getLotBasicInfo().getLotStatus().equals("Processing"), "test fail");
        //【step3】einfo/eqp_info/inq
        Results.EqpInfoInqResult eqpInfoInqResult2 = electronicInformationTestCase.getEqpInfo(testCommonData.getEQUIPMENTID());
        Assert.isTrue(!CimArrayUtils.isEmpty(eqpInfoInqResult2.getEquipmentInprocessingControlJobList()), "test fail");
        return moveInReqResult;
    }

    public void clearReservedControlJobInformationAfterMoveIn(){
        //【step1】load
        ObjectIdentifier cassetteID = lotLoadToEquipmentCase.manual_Loading_After_StartLotsReservation();
        // 【step2】eqp info
        Results.EqpInfoInqResult eqpInfoInqResult = (Results.EqpInfoInqResult) electronicInformationTestCase.eqpInfoInqCase(testCommonData.getEQUIPMENTID()).getBody();
        List<Infos.EqpPortStatus> eqpPortStatuses = eqpInfoInqResult.getEquipmentPortInfo().getEqpPortStatuses();
        for (Infos.EqpPortStatus eqpPortStatus : eqpPortStatuses){
            if (CimObjectUtils.equalsWithValue(eqpPortStatus.getPortID(), testCommonData.getPROTID())){
                Assert.isTrue(CimObjectUtils.equalsWithValue(cassetteID, eqpPortStatus.getLoadResrvedCassetteID()), "test fail");
                break;
            }
        }
        Infos.EqpReservedControlJobInfo equipmentReservedControlJobInfo = eqpInfoInqResult.getEquipmentReservedControlJobInfo();
        Assert.isTrue(equipmentReservedControlJobInfo.getMoveInReservedControlJobInfoList().get(0) != null, "test fail");
        // 【step3】move in
        this.onlyMoveIn(Arrays.asList(cassetteID), testCommonData.getEQUIPMENTID());
        // 【step4】eqp info
        Results.EqpInfoInqResult eqpInfoInqResult2 = (Results.EqpInfoInqResult) electronicInformationTestCase.eqpInfoInqCase(testCommonData.getEQUIPMENTID()).getBody();
        List<Infos.EqpPortStatus> eqpPortStatuses2 = eqpInfoInqResult2.getEquipmentPortInfo().getEqpPortStatuses();
        for (Infos.EqpPortStatus eqpPortStatus : eqpPortStatuses2){
            if (CimObjectUtils.equalsWithValue(eqpPortStatus.getPortID(), testCommonData.getPROTID())){
                Assert.isTrue(eqpPortStatus.getLoadResrvedCassetteID() == null, "test fail");
                break;
            }
        }
        Infos.EqpReservedControlJobInfo equipmentReservedControlJobInfo2 = eqpInfoInqResult2.getEquipmentReservedControlJobInfo();
        Assert.isTrue(equipmentReservedControlJobInfo2 == null, "test fail");
    }

    public void reportingChangeEventInEquipmentStatusAfterMoveIn(){
        // 【step1】eqp info
        Results.EqpInfoInqResult eqpInfoInqResult = (Results.EqpInfoInqResult) electronicInformationTestCase.eqpInfoInqCase(testCommonData.getEQUIPMENTID()).getBody();
        String e10Status = eqpInfoInqResult.getEquipmentStatusInfo().getE10Status();
        String equipmentStatusCode = eqpInfoInqResult.getEquipmentStatusInfo().getEquipmentStatusCode().getValue();
        Assert.isTrue(e10Status.equals("SBY") && equipmentStatusCode.equals("WLOT"), "please setting the equipment that can use");
        // 【step2】load after startreserve
        ObjectIdentifier cassetteID = lotLoadToEquipmentCase.manual_Loading_After_StartLotsReservation();
        // 【step3】eqp info again
        Results.EqpInfoInqResult eqpInfoInqResult2 = (Results.EqpInfoInqResult) electronicInformationTestCase.eqpInfoInqCase(testCommonData.getEQUIPMENTID()).getBody();
        List<Infos.EqpPortStatus> eqpPortStatuses = eqpInfoInqResult2.getEquipmentPortInfo().getEqpPortStatuses();
        for (Infos.EqpPortStatus eqpPortStatus : eqpPortStatuses){
            if (CimObjectUtils.equalsWithValue(eqpPortStatus.getPortID(), testCommonData.getPROTID())){
                Assert.isTrue(CimObjectUtils.equalsWithValue(cassetteID, eqpPortStatus.getLoadResrvedCassetteID()), "test fail");
                break;
            }
        }
        // 【step4】move in
        this.onlyMoveIn(Arrays.asList(cassetteID), testCommonData.getEQUIPMENTID());
        // 【step5】eqp info again
        Results.EqpInfoInqResult eqpInfoInqResult3 = (Results.EqpInfoInqResult) electronicInformationTestCase.eqpInfoInqCase(testCommonData.getEQUIPMENTID()).getBody();
        String e10Status2 = eqpInfoInqResult3.getEquipmentStatusInfo().getE10Status();
        String equipmentStatusCode2 = eqpInfoInqResult3.getEquipmentStatusInfo().getEquipmentStatusCode().getValue();
        Assert.isTrue(e10Status2.equals("PRD") && equipmentStatusCode2.equals("PRD"), "test fail");

    }

    public void moveInWithOnHoldLot(){
        // 【step1】load
        ObjectIdentifier cassetteID = lotLoadToEquipmentCase.manual_Loading_Without_StartLotsReservation();
        // 【step2】lot hold
        lotHoldCase.lotHoldOnCurrent(cassetteID);
        // 【step3】move in
        try {
            this.onlyMoveIn(Arrays.asList(cassetteID), testCommonData.getEQUIPMENTID());
            Assert.isTrue(false, "test fail");
        } catch (ServiceException e) {
            Assert.isTrue(Validations.isEquals(retCodeConfig.getInvalidLotHoldStat(), e.getCode()), e.getMessage());
        }
    }

    public void incrementOperationCountOfEquipmentAfterMoveIn(){
        // 【step1】load
        ObjectIdentifier cassetteID = lotLoadToEquipmentCase.manual_Loading_Without_StartLotsReservation();
        // 【step2】eqp info
        Results.EqpInfoInqResult eqpInfoInqResult = (Results.EqpInfoInqResult) electronicInformationTestCase.eqpInfoInqCase(testCommonData.getEQUIPMENTID()).getBody();
        Integer operationStartCount = eqpInfoInqResult.getEquipmentPMInfo().getMoveInCount();
        // 【step3】move in
        this.onlyMoveIn(Arrays.asList(cassetteID), testCommonData.getEQUIPMENTID());
        // 【step2】eqp info again
        Results.EqpInfoInqResult eqpInfoInqResult2 = (Results.EqpInfoInqResult) electronicInformationTestCase.eqpInfoInqCase(testCommonData.getEQUIPMENTID()).getBody();
        Integer operationStartCount2 = eqpInfoInqResult2.getEquipmentPMInfo().getMoveInCount();
        Assert.isTrue(operationStartCount2 == operationStartCount + 1, "test fail");
    }

    public void incrementOperationCountOfEmployedReticleAfterMoveIn(){
        // 【step1】stb product 1.01
        ObjectIdentifier lotID = stbCase.STB_PRODUCT101(false);
        // 【step2】skip to the 7000.0100
        List<Infos.LotInfo> lotInfoList = operationSkipCase.skipSpecificStep(Arrays.asList(lotID), "7000.0100", true);
        Infos.LotInfo lotInfo = lotInfoList.get(0);
        // 【step3】einfo/eqp_info/inq
        Results.EqpInfoInqResult eqpInfo = electronicInformationTestCase.getEqpInfo(new ObjectIdentifier(RETICLE_EQUIPMENTID));
        List<Infos.StoredReticle> storedReticleList = eqpInfo.getEquipmentAdditionalReticleAttribute().getStoredReticleList();
        Assert.isTrue(CimArrayUtils.getSize(storedReticleList) > 0, "the equipment you selected is not proper");
        // 【step4】get reticle count before move in
        List<Integer> reticleCountBeforeMoveIn = new ArrayList<>(storedReticleList.size());
        for (Infos.StoredReticle storedReticle : storedReticleList){
            Results.ReticleDetailInfoInqResult reticleInfomation = commonTestCase.getReticleInfomation(storedReticle.getReticleID());
            reticleCountBeforeMoveIn.add(reticleInfomation.getReticlePMInfo().getOperationStartCount());
        }
        // 【step5】load
        ObjectIdentifier cassetteID = lotLoadToEquipmentCase.manual_Loading_Without_StartLotsReservation(lotInfo.getLotBasicInfo().getLotID(), new ObjectIdentifier(RETICLE_EQUIPMENTID));
        // 【step6】move in
        this.onlyMoveIn(Arrays.asList(cassetteID), new ObjectIdentifier(RETICLE_EQUIPMENTID));
        // 【step7】einfo/eqp_info/inq again
        Results.EqpInfoInqResult eqpInfo2 = electronicInformationTestCase.getEqpInfo(new ObjectIdentifier(RETICLE_EQUIPMENTID));
        // 【step8】get reticle count after move in
        List<Infos.StoredReticle> storedReticleList2 = eqpInfo2.getEquipmentAdditionalReticleAttribute().getStoredReticleList();
        List<Integer> reticleCountAfterMoveIn = new ArrayList<>(storedReticleList.size());
        for (Infos.StoredReticle storedReticle : storedReticleList2){
            Results.ReticleDetailInfoInqResult reticleInfomation = commonTestCase.getReticleInfomation(storedReticle.getReticleID());
            reticleCountAfterMoveIn.add(reticleInfomation.getReticlePMInfo().getOperationStartCount());
        }
        // 【step9】compare the reticle count between before move int and after move in
        boolean assertIsTrue = false;
        for (int i = 0; i < reticleCountAfterMoveIn.size(); i++){
            if (reticleCountAfterMoveIn.get(i) == reticleCountBeforeMoveIn.get(i) + 1){
                assertIsTrue = true;
                break;
            }
        }
        Assert.isTrue(assertIsTrue, "test fail");
    }

    public void multiRecipeCapabilityOfEquipmentIsSingleRecipeOrBatch(){
        // Single Recipe
        //【step1】eqp info
        Results.EqpInfoInqResult eqpInfo2 = electronicInformationTestCase.getEqpInfo(new ObjectIdentifier("1CMI02"));
        Assert.isTrue(eqpInfo2.getEquipmentBasicInfo().getMultiRecipeCapability().equals("Single Recipe"), " eqp can not use");
        //【step2】stb PRODUCT1.01
        ObjectIdentifier lotID = stbCase.STB_PRODUCT101(false);
        //【step3】skip
        List<Infos.LotInfo> lotInfoList = operationSkipCase.skipSpecificStep(Arrays.asList(lotID), "8000.0500", true);
        Infos.LotInfo lotInfo = lotInfoList.get(0);
        //【step4】split
        lotSplitCase.splitBySpecificLotID(lotInfo.getLotBasicInfo().getLotID());
        //【step5】load and move in
        this.moveIn_GenerateControlJob_Without_StartReservation(lotInfo.getLotBasicInfo().getLotID(), new ObjectIdentifier("1CMI02"));
        // batch
        //【step6】stb
        List<ObjectIdentifier> lotIDs = stbCase.stb_NLots_NotPreparedCase(1, true);
        //【step7】split
        lotSplitCase.splitBySpecificLotID(lotIDs.get(0));
        //【step8】eqp info
        Results.EqpInfoInqResult eqpInfo = electronicInformationTestCase.getEqpInfo(new ObjectIdentifier("1BKD01"));
        Assert.isTrue(eqpInfo.getEquipmentBasicInfo().getMultiRecipeCapability().equals("Batch"), " eqp can not use");
        //【step9】load and move in
        this.moveIn_GenerateControlJob_Without_StartReservation(lotIDs.get(0), new ObjectIdentifier("1BKD01"), new ObjectIdentifier("P2"));

    }

    public void durableStatusIsUnavailableOrScrappedWhenMoveIn(){
        // 【step1】stb product 1.01
        ObjectIdentifier lotID = stbCase.STB_PRODUCT101(false);
        // 【step2】skip to the 7000.0100
        List<Infos.LotInfo> lotInfoList = operationSkipCase.skipSpecificStep(Arrays.asList(lotID), "7000.0100", true);
        Infos.LotInfo lotInfo = lotInfoList.get(0);
        // 【step3】judge the equipment need to reticle equipment in
        List<Infos.StoredReticle> storedReticleList = equipmentTestCase.keepEquipmentReticleAvailable(new ObjectIdentifier(RETICLE_EQUIPMENTID), 2);
        // 【step4】load
        lotLoadToEquipmentCase.manual_Loading_Without_StartLotsReservation(lotInfo.getLotBasicInfo().getLotID(), new ObjectIdentifier(RETICLE_EQUIPMENTID));
        // 【step5】change all of the reticles to notavailable
        for (Infos.StoredReticle storedReticle : storedReticleList){
            commonTestCase.changeDurableStatus(storedReticle.getReticleID(), "NOTAVAILABLE", "Reticle");
        }
        // 【step6】move in
        try {
            this.onlyMoveIn(Arrays.asList(lotInfo.getLotLocationInfo().getCassetteID()), new ObjectIdentifier(RETICLE_EQUIPMENTID));
            Assert.isTrue(false, "test fail");
        } catch (ServiceException e) {
            Assert.isTrue(Validations.isEquals(retCodeConfig.getNotAvailableReticle(), e.getCode()), "test fail, because the exception information is :" + e.getMessage());
        }
        // 【step7】change all of the reticles to notavailable
        for (Infos.StoredReticle storedReticle : storedReticleList){
            commonTestCase.changeDurableStatus(storedReticle.getReticleID(), "SCRAPPED", "Reticle");
        }
        // 【step8】move in
        try {
            this.onlyMoveIn(Arrays.asList(lotInfo.getLotLocationInfo().getCassetteID()), new ObjectIdentifier(RETICLE_EQUIPMENTID));
            Assert.isTrue(false, "test fail");
        } catch (ServiceException e) {
            Assert.isTrue(Validations.isEquals(retCodeConfig.getNotAvailableReticle(), e.getCode()), "test fail, because the exception information is :" + e.getMessage());
        }

    }

    public void assignedProcessDurableWhenMoveIn(){
        // 【step1】stb product 1.01
        ObjectIdentifier lotID = stbCase.STB_PRODUCT101(false);
        // 【step2】skip to the 7000.0100
        List<Infos.LotInfo> lotInfoList = operationSkipCase.skipSpecificStep(Arrays.asList(lotID), "7000.0100", true);
        Infos.LotInfo lotInfo = lotInfoList.get(0);
        // 【step3】judge the equipment need to reticle equipment in
        List<Infos.StoredReticle> storedReticleList = equipmentTestCase.keepEquipmentReticleAvailable(new ObjectIdentifier(RETICLE_EQUIPMENTID), 2);
        // 【step4】load
        lotLoadToEquipmentCase.manual_Loading_Without_StartLotsReservation(lotInfo.getLotBasicInfo().getLotID(), new ObjectIdentifier(RETICLE_EQUIPMENTID));
        // 【step5】all of reticle eqp out
        List<Infos.XferReticle> xferReticleList = new ArrayList<>();
        for (Infos.StoredReticle storedReticle : storedReticleList){
            Infos.XferReticle xferReticle = new Infos.XferReticle();
            xferReticleList.add(xferReticle);
            xferReticle.setReticleID(storedReticle.getReticleID());
            xferReticle.setTransferStatus("EO");
        }
        commonTestCase.reticleTransferStatusChangeRpt(new ObjectIdentifier(RETICLE_EQUIPMENTID), xferReticleList);
        // 【step6】move in
        try {
            this.onlyMoveIn(Arrays.asList(lotInfo.getLotLocationInfo().getCassetteID()), new ObjectIdentifier(RETICLE_EQUIPMENTID));
            Assert.isTrue(false, "test fail");
        } catch (ServiceException e) {
            Assert.isTrue(Validations.isEquals(retCodeConfig.getNotAvailableReticle(), e.getCode()), e.getMessage());
        }
    }


    public void restrictionEQPWhenMoveIn(){
        // 【step1】stb and load
        ObjectIdentifier cassetteID = lotLoadToEquipmentCase.manual_Loading_Without_StartLotsReservation();
        // 【step2】inhibit equipment
        entityInhibityCase.inhibitSpecCondition(testCommonData.getEQUIPMENTID());
        // 【step3】move in
        try {
            this.onlyMoveIn(Arrays.asList(cassetteID), testCommonData.getEQUIPMENTID());
            Assert.isTrue(false, "test fail");
        } catch (ServiceException e) {
            Assert.isTrue(Validations.isEquals(retCodeConfig.getInhibitLot(), e.getCode()), e.getMessage());
        }
    }

    public void carrierDurableStatusIsUnavailableWhenMoveIn(){
        // 【step1】stb and load
        ObjectIdentifier cassetteID = lotLoadToEquipmentCase.manual_Loading_Without_StartLotsReservation();
        // 【step2】change the carrier status
        commonTestCase.changeDurableStatus(cassetteID, "NOTAVAILABLE", "Cassette");
        // 【step3】move in
        try {
            this.onlyMoveIn(Arrays.asList(cassetteID), testCommonData.getEQUIPMENTID());
            Assert.isTrue(false, "test fail");
        } catch (ServiceException e) {
            Assert.isTrue(Validations.isEquals(retCodeConfig.getDurableNotAvailableStateForLotProcess(), e.getCode()), "test fail, because exception information is :" + e.getMessage());
        }
        // 【step4】change the carrier status
        commonTestCase.changeDurableStatus(cassetteID, "AVAILABLE", "Cassette");
        // 【step5】move in
        this.onlyMoveIn(Arrays.asList(cassetteID), testCommonData.getEQUIPMENTID());
    }

    public void monitorCreationFlagOfEquipmentIsTrueAndEmptyCarriersMustExistInMoveInInformation(){
        //【step1】eqp info
        Results.EqpInfoInqResult eqpInfo = electronicInformationTestCase.getEqpInfo(new ObjectIdentifier("1FHI01_NORM"));
        boolean monitorCreationFlag = eqpInfo.getEquipmentBasicInfo().isMonitorCreationFlag();
        Assert.isTrue(monitorCreationFlag, "the equipment you selected is not proper, because the monitorCreationFlag is :" + monitorCreationFlag);
        //【step2】stb and skip
        List<Infos.LotInfo> lotInfoList = operationSkipCase.stbNLotsAndSkipSpecificStep(1, true, "2000.0200", true);
        Results.LotInfoInqResult lotInfoInqResult = (Results.LotInfoInqResult) lotGeneralTestCase.getLotInfoCase(Arrays.asList(lotInfoList.get(0).getLotBasicInfo().getLotID())).getBody();
        try {
            //【step3】load and move in
            this.moveIn_GenerateControlJob_Without_StartReservation(lotInfoList.get(0).getLotBasicInfo().getLotID(), new ObjectIdentifier("1FHI01_NORM"));
            Assert.isTrue(false, "test fail");
        } catch (ServiceException e) {
            Assert.isTrue(Validations.isEquals(retCodeConfig.getInvalidEmptyCount(), e.getCode()), e.getMessage());
        }
        //【step3】unload
        lotUnloadFromEquipmentCase.unLoad_WithSpecifiedEqpAndCassettes(new ObjectIdentifier("1FHI01_NORM"), Arrays.asList(lotInfoList.get(0).getLotLocationInfo().getCassetteID()));
        //【step4】reserve process lot
        List<ObjectIdentifier> cassettes = startLotsReservationCase.whenMonitorCreationFlagOfEquipmentIsTrueReserveCarrierWithAnotherLoadedEmptyCarrier(new ObjectIdentifier("1FHI01_NORM"), lotInfoInqResult);
        ObjectIdentifier emptyCarrier = null;
        ObjectIdentifier emptyCarrierPort = null;
        ObjectIdentifier processLotCarrierPort = null;
        int count = 0;
        for (ObjectIdentifier cassette : cassettes){
            count++;
            if (!CimObjectUtils.equalsWithValue(cassette, lotInfoList.get(0).getLotLocationInfo().getCassetteID())){
                emptyCarrier = cassette;
                break;
            }
        }
        emptyCarrierPort = new ObjectIdentifier("P2");
        processLotCarrierPort = new ObjectIdentifier("P1");
        if (count == 1){
            emptyCarrierPort = new ObjectIdentifier("P1");
            processLotCarrierPort = new ObjectIdentifier("P2");
        }
        //【step5】load process lot
        equipmentTestCase.carrierLoadingRpt(new ObjectIdentifier("1FHI01_NORM"), lotInfoList.get(0).getLotLocationInfo().getCassetteID(), processLotCarrierPort, BizConstant.SP_LOADPURPOSETYPE_PROCESSLOT);
        //【step6】load empty carrier
        equipmentTestCase.carrierLoadingRpt(new ObjectIdentifier("1FHI01_NORM"), emptyCarrier, emptyCarrierPort, BizConstant.SP_LOADPURPOSETYPE_EMPTYCASSETTE);
        //【step7】move in
        this.onlyMoveIn(cassettes, new ObjectIdentifier("1FHI01_NORM"));
    }

    public void carrierExchangeRequiredFlagOfEquipmentIsTrue(){
        //【step1】eqp info
        Results.EqpInfoInqResult eqpInfo = electronicInformationTestCase.getEqpInfo(new ObjectIdentifier("DF106"));
        boolean cassetteChangeFlag = eqpInfo.getEquipmentBasicInfo().isCassetteChangeFlag();
        Assert.isTrue(cassetteChangeFlag, "the equipment you selected is not proper, because the cassetteChangeFlag is :" + cassetteChangeFlag);
        List<Infos.EqpPortStatus> eqpPortStatuses = eqpInfo.getEquipmentPortInfo().getEqpPortStatuses();
        Map<String, List<Infos.EqpPortStatus>> map = eqpPortStatuses.stream().collect(Collectors.groupingBy(Infos.EqpPortStatus::getPortGroup));
        AtomicReference<List<Infos.EqpPortStatus>> eqpPortStatusesSelectedAtomic = new AtomicReference<>();
        map.forEach((k,v)->{
            if (v.size() > 3){
                eqpPortStatusesSelectedAtomic.set(v);
            }
        });
        Assert.isTrue(!CimArrayUtils.isEmpty(eqpPortStatusesSelectedAtomic.get()), "the equipment you selected doesn't have enough ports");
        //【step2】stb and skip
        List<Infos.LotInfo> lotInfoList = operationSkipCase.stbNLotsAndSkipSpecificStep("PROD1-LYR0.01", 2, true, "3000.0200", true);
        List<ObjectIdentifier> lots = lotInfoList.stream().map(lotInfo -> lotInfo.getLotBasicInfo().getLotID()).collect(Collectors.toList());
        try {
            //【step3】start reserve
            startLotsReservationCase.moveInReserveReqWhithSpecifiedLotsAndEqp(lots, new ObjectIdentifier("DF106"), 1);
            Assert.isTrue(false, "test fail");
        } catch (ServiceException e) {
            Assert.isTrue(Validations.isEquals(retCodeConfig.getInvalidEmptyCount(), e.getCode()), "test fail, because the exception is :" + e.getMessage());
        }
        //【step4】start reserve again
        List<ObjectIdentifier> cassettes = startLotsReservationCase.moveInReserveReqWhithSpecifiedLotsAndEqp(lots, new ObjectIdentifier("DF106"), 2);
        //【step5】load
        lotLoadToEquipmentCase.loading_Without_StartLotsReservationSpecifiedEqpAndCassettes(new ObjectIdentifier("DF106"), cassettes);
        //【step6】move in
        this.onlyMoveIn(cassettes, new ObjectIdentifier("DF106"));
    }

    public void multiRecipeCapabilityOfequipmentisBatchMultipleLotsCanBeReservedAtSamePortGroupAndMoveIn(){
        //【step1】eqp info
        Results.EqpInfoInqResult eqpInfo = electronicInformationTestCase.getEqpInfo(new ObjectIdentifier("1SRT01"));
        Assert.isTrue(eqpInfo.getEquipmentBasicInfo().getMultiRecipeCapability().equals("Multiple Recipe") || eqpInfo.getEquipmentBasicInfo().getMultiRecipeCapability().equals("Batch"),
                "the equipment you selected is not proper");
        List<Infos.EqpPortStatus> eqpPortStatuses = eqpInfo.getEquipmentPortInfo().getEqpPortStatuses();
        Map<String, List<Infos.EqpPortStatus>> map = eqpPortStatuses.stream().collect(Collectors.groupingBy(Infos.EqpPortStatus::getPortGroup));
        AtomicReference<List<Infos.EqpPortStatus>> eqpPortStatusesSelectedAtomic = new AtomicReference<>();
        map.forEach((k,v)->{
            if (v.size() > 1){
                eqpPortStatusesSelectedAtomic.set(v);
            }
        });
        Assert.isTrue(!CimArrayUtils.isEmpty(eqpPortStatusesSelectedAtomic.get()), "the equipment you selected doesn't have enough ports");
        List<Infos.EqpPortStatus> eqpPortStatusesSelected = eqpPortStatusesSelectedAtomic.get();
        //【step2】stb
        List<ObjectIdentifier> lots = stbCase.stb_NLots_NotPreparedCase(2, true);
        //【step3】lot info
        Infos.LotInfo lotInfo1 = lotGeneralTestCase.getLotInfos(Arrays.asList(lots.get(0))).get(0);
        Infos.LotInfo lotInfo2 = lotGeneralTestCase.getLotInfos(Arrays.asList(lots.get(1))).get(0);
        //【step4】reserve
        startLotsReservationCase.moveInReserveReqWhithSpecifiedLotsAndEqp(lots, new ObjectIdentifier("1SRT01"));
        List<String> purposeList = (List<String>) equipmentTestCase.getLoadPurposeTypeCase(BizConstant.SP_LOADPURPOSETYPE_PROCESSLOT).getBody();
        //【step5】load one lot
        equipmentTestCase.carrierLoadingRpt(new ObjectIdentifier("1SRT01"), lotInfo1.getLotLocationInfo().getCassetteID(), eqpPortStatusesSelected.get(0).getAssociatedPortID(), purposeList.get(0));
        //【step6】load another lot
        equipmentTestCase.carrierLoadingRpt(new ObjectIdentifier("1SRT01"), lotInfo2.getLotLocationInfo().getCassetteID(), eqpPortStatusesSelected.get(1).getAssociatedPortID(), purposeList.get(0));
        //【step7】move in
        this.onlyMoveIn(Arrays.asList(lotInfo1.getLotLocationInfo().getCassetteID(), lotInfo2.getLotLocationInfo().getCassetteID()), new ObjectIdentifier("1SRT01"));
        //【step8】eqp info
        Results.EqpInfoInqResult eqpInfo2 = electronicInformationTestCase.getEqpInfo(new ObjectIdentifier("1SRT01"));
        List<Infos.EqpInprocessingControlJob> equipmentInprocessingControlJobList = eqpInfo2.getEquipmentInprocessingControlJobList();
        Assert.isTrue(CimArrayUtils.getSize(equipmentInprocessingControlJobList) == 1, " test fail");
        List<Infos.EqpInprocessingLot> strEqpInprocessingLot = equipmentInprocessingControlJobList.get(0).getEqpInprocessingLotList();
        Assert.isTrue(CimArrayUtils.getSize(strEqpInprocessingLot) == 2, "test fail");
    }


    public void moveInLotWhichProductHasBeenRestricted(){
        // 【step1】load
        ObjectIdentifier cassetteID = lotLoadToEquipmentCase.manual_Loading_Without_StartLotsReservation();
        // 【step2】entityInhibit
        entityInhibityCase.inhibitSpecCondtition(null, new ObjectIdentifier("PRODUCT0.01"));
        // 【step5】move in
        try {
            this.onlyMoveIn(Arrays.asList(cassetteID), testCommonData.getEQUIPMENTID());
            Assert.isTrue(false, "test fail");
        } catch (ServiceException e) {
            Assert.isTrue(Validations.isEquals(retCodeConfig.getInhibitLot(), e.getCode()), "test fail, because exception information is :" + e.getMessage());
        }
    }

    public void moveInLotWhichCarrierActuallyMoveoOutFromThisEquipment(){
        // 【step1】stb and move out
        List<Infos.LotInfo> lotInfoList = moveOutCase.moveOut_Normal();
        // 【step2】move in again
        try {
            this.onlyMoveIn(Arrays.asList(lotInfoList.get(0).getLotLocationInfo().getCassetteID()), testCommonData.getEQUIPMENTID());
            Assert.isTrue(false, "test fail");
        } catch (ServiceException e) {
            Assert.isTrue(Validations.isEquals(retCodeConfig.getCannotGetStartinfo(), e.getCode()), "test fail, because exception information is :" + e.getMessage());
        }
        // 【step3】lot info
        Results.LotInfoInqResult lotInfoInqResult = (Results.LotInfoInqResult) lotGeneralTestCase.getLotInfoCase(Arrays.asList(lotInfoList.get(0).getLotBasicInfo().getLotID())).getBody();
        Infos.LotInfo lotInfo2 = lotInfoInqResult.getLotInfoList().get(0);
        List<Infos.LotEquipmentList> lotEquipmentList = lotInfo2.getLotOperationInfo().getLotEquipmentList();
        // 【step4】load on next step equipment
        try {
            lotLoadToEquipmentCase.manual_Loading_Without_StartLotsReservation(lotInfo2.getLotBasicInfo().getLotID(), lotEquipmentList.get(0).getEquipmentID());
            Assert.isTrue(false, "test fail");
        } catch (ServiceException e) {
            Assert.isTrue(Validations.isEquals(retCodeConfig.getInvalidCassetteTransferState(), e.getCode()), "test fail, because exception information is :" + e.getMessage());
        }
        // 【step5】unload
        lotUnloadFromEquipmentCase.unLoad_WithSpecifiedEqpAndCassettes(testCommonData.getEQUIPMENTID(), Arrays.asList(lotInfo2.getLotLocationInfo().getCassetteID()));
        // 【step6】move in
        try {
            this.onlyMoveIn(Arrays.asList(lotInfo2.getLotLocationInfo().getCassetteID()), lotEquipmentList.get(0).getEquipmentID());
            Assert.isTrue(false, "test fail");
        } catch (ServiceException e) {
            Assert.isTrue(Validations.isEquals(retCodeConfig.getCassetteNotInLoader(), e.getCode()), "test fail, because exception information is :" + e.getMessage());
        }
        // 【step7】then load and move in will be success
        lotLoadToEquipmentCase.manual_Loading_Without_StartLotsReservation(lotInfo2.getLotBasicInfo().getLotID(), lotEquipmentList.get(0).getEquipmentID());
        // 【step8】move in
        this.onlyMoveIn(Arrays.asList(lotInfo2.getLotLocationInfo().getCassetteID()), lotEquipmentList.get(0).getEquipmentID());
    }

    public void moveInCarrierWhichPortIsAuto1ModeAndLoadCompStatus(){
        // 【step1】stb and load
        ObjectIdentifier cassette = lotLoadToEquipmentCase.loadCarrierWithAuto1OperationMode();
        // 【step2】move in
        this.onlyMoveIn(Arrays.asList(cassette), testCommonData.getEQUIPMENTID());
    }

    public void moveInLotToPortGroupPG2AndPG1HasLotIsInprocessing(){
        //【step1】eqp info
        Results.EqpInfoInqResult eqpInfo = electronicInformationTestCase.getEqpInfo(new ObjectIdentifier("1THK01"));
        List<Infos.EqpPortStatus> eqpPortStatuses = eqpInfo.getEquipmentPortInfo().getEqpPortStatuses();
        Set<String> portGroupSet = eqpPortStatuses.stream().map(Infos.EqpPortStatus::getPortGroup).collect(Collectors.toSet());
        Assert.isTrue(portGroupSet.size() > 1, "the equipment you selected is not proper");
        // 【step2】stb 2 lots and skip to 2000.0300
        List<Infos.LotInfo> lotInfoList = operationSkipCase.stbNLotsAndSkipSpecificStep(2, true, "2000.0300", true);
        ObjectIdentifier selectedPort1 = eqpPortStatuses.get(0).getAssociatedPortID();
        ObjectIdentifier selectedPort2 = null;
        for (Infos.EqpPortStatus eqpPortStatus : eqpPortStatuses){
            if (CimObjectUtils.equalsWithValue(selectedPort1, eqpPortStatus.getAssociatedPortID())){
                continue;
            }
            if (!CimStringUtils.equals(eqpPortStatuses.get(0).getPortGroup(), eqpPortStatus.getPortGroup())){
                selectedPort2 = eqpPortStatus.getAssociatedPortID();
                break;
            }
        }
        // 【step3】load and move int the first one
        this.moveIn_GenerateControlJob_Without_StartReservation(lotInfoList.get(0).getLotBasicInfo().getLotID(), new ObjectIdentifier("1THK01"), selectedPort1);
        // 【step4】load and move int the second one
        this.moveIn_GenerateControlJob_Without_StartReservation(lotInfoList.get(1).getLotBasicInfo().getLotID(), new ObjectIdentifier("1THK01"), selectedPort2);
        // 【step5】get the lots infomation
        for (Infos.LotInfo lotInfo : lotInfoList){
            Results.LotInfoInqResult lotInfoInqResult = (Results.LotInfoInqResult) lotGeneralTestCase.getLotInfoCase(Arrays.asList(lotInfo.getLotBasicInfo().getLotID())).getBody();
            Assert.isTrue(lotInfoInqResult.getLotInfoList().get(0).getLotBasicInfo().getLotStatus().equals("Processing"), "test fail");
        }
        // 【step6】get eqp info
        Results.EqpInfoInqResult eqpInfo2 = electronicInformationTestCase.getEqpInfo(new ObjectIdentifier("1THK01"));
        List<Infos.EqpInprocessingControlJob> equipmentInprocessingControlJobList = eqpInfo2.getEquipmentInprocessingControlJobList();
        Assert.isTrue(equipmentInprocessingControlJobList.size() == 2, "test fail");
        Assert.isTrue(!CimObjectUtils.equalsWithValue(equipmentInprocessingControlJobList.get(0).getControlJobID(), equipmentInprocessingControlJobList.get(1).getControlJobID()), "test fail");
    }

    public Response moveIn_GenerateControlJob_Without_StartReservation(ObjectIdentifier lotID,ObjectIdentifier equipmentID){
        return this.moveIn_GenerateControlJob_Without_StartReservation(lotID, equipmentID, null);
    }

    public Response moveIn_GenerateControlJob_Without_StartReservation(ObjectIdentifier lotID,ObjectIdentifier equipmentID, ObjectIdentifier portID){
        //【step1】load
        ObjectIdentifier cassetteID = lotLoadToEquipmentCase.manual_Loading_Without_StartLotsReservation(lotID,equipmentID, portID);
        // 【step2】get lotsInfoForOpeStart info
        List<ObjectIdentifier> cassetteIDs = new ArrayList<>();
        cassetteIDs.add(cassetteID);
        Results.LotsMoveInInfoInqResult lotsMoveInInfoInqResult = (Results.LotsMoveInInfoInqResult) equipmentTestCase.lotsMoveInInfoInq(equipmentID, cassetteIDs).getBody();
        //【step3】einfo/eqp_info/inq
        Results.EqpInfoInqResult eqpInfoInqResult = electronicInformationTestCase.getEqpInfo(equipmentID);
        // 【step3】move in
        Response response = equipmentTestCase.movInReq(lotsMoveInInfoInqResult.getControlJobID(), lotsMoveInInfoInqResult.getEquipmentID(), lotsMoveInInfoInqResult.getPortGroupID(), false, lotsMoveInInfoInqResult.getStartCassetteList());
        // 【step4】get lot Info
        List<Infos.LotInfo> lotInfosList = lotGeneralTestCase.getLotInfosByCassette(cassetteID);
        Infos.LotInfo lotInfo = lotInfosList.get(0);
        Assert.isTrue(lotInfo.getLotBasicInfo().getLotStatus().equals("Processing"), "test fail");
        //【step3】einfo/eqp_info/inq
        Results.EqpInfoInqResult eqpInfoInqResult2 = electronicInformationTestCase.getEqpInfo(equipmentID);
        Assert.isTrue(!CimArrayUtils.isEmpty(eqpInfoInqResult2.getEquipmentInprocessingControlJobList()), "test fail");
        return response;
    }

    public Response onlyMoveIn(List<ObjectIdentifier> cassetteList,ObjectIdentifier equipmentID){
        Results.LotsMoveInInfoInqResult lotsMoveInInfoInqResult = (Results.LotsMoveInInfoInqResult) equipmentTestCase.lotsMoveInInfoInq(equipmentID, cassetteList).getBody();
        List<ObjectIdentifier> cassetteListMoveIn = lotsMoveInInfoInqResult.getStartCassetteList().stream().map(startCassette -> startCassette.getCassetteID()).collect(Collectors.toList());
        Assert.isTrue(cassetteList.containsAll(cassetteListMoveIn) && cassetteListMoveIn.size() == cassetteList.size(), "test fail");
        return equipmentTestCase.movInReq(lotsMoveInInfoInqResult.getControlJobID(), lotsMoveInInfoInqResult.getEquipmentID(), lotsMoveInInfoInqResult.getPortGroupID(), false, lotsMoveInInfoInqResult.getStartCassetteList());
    }

}