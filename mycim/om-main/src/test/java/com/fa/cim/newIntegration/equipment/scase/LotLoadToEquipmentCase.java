package com.fa.cim.newIntegration.equipment.scase;

import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.utils.CimArrayUtils;
import com.fa.cim.common.utils.Validations;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.core.TestCommonData;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Results;
import com.fa.cim.newIntegration.LotOperation.scase.LotHoldCase;
import com.fa.cim.newIntegration.LotOperation.scase.LotSplitCase;
import com.fa.cim.newIntegration.LotOperation.scase.OperationSkipCase;
import com.fa.cim.newIntegration.LotOperation.scase.ScrapCase;
import com.fa.cim.newIntegration.processControl.scase.EntityInhibityCase;
import com.fa.cim.newIntegration.stb.scase.STBCase;
import com.fa.cim.newIntegration.tcase.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/9/12       ********             lightyh             create file
 *
 * @author: lightyh
 * @date: 2019/9/12 15:00
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Service
@Slf4j
public class LotLoadToEquipmentCase {

    @Autowired
    private StartLotsReservationCase startLotsReservationCase;

    @Autowired
    @Qualifier("testCommonDataWithDB")
    private TestCommonData testCommonData;

    @Autowired
    private EquipmentTestCase equipmentTestCase;

    @Autowired
    private ElectronicInformationTestCase electronicInformationTestCase;

    @Autowired
    private STBCase stbCase;

    @Autowired
    private LotGeneralTestCase lotGeneralTestCase;

    @Autowired
    private OperationSkipCase operationSkipCase;

    @Autowired
    private CommonTestCase commonTestCase;

    @Autowired
    private LotSubRouteBranchTestCase lotSubRouteBranchTestCase;

    @Autowired
    private LotHoldCase lotHoldCase;

    @Autowired
    private EntityInhibityCase entityInhibityCase;

    @Autowired
    private ScrapCase scrapCase;

    @Autowired
    private LotSplitCase lotSplitCase;

    @Autowired
    private RetCodeConfig retCodeConfig;

    public ObjectIdentifier manual_Loading_After_StartLotsReservation(){
        //【step1】lot reserve
        ObjectIdentifier cassetteID = startLotsReservationCase.startLotsReservation();
        //【step2】load purpose
        List<String> purposeList = (List<String>) equipmentTestCase.getLoadPurposeTypeCase(BizConstant.SP_LOADPURPOSETYPE_PROCESSLOT).getBody();
        //【step2】load
        equipmentTestCase.carrierLoadingRpt(testCommonData.getEQUIPMENTID(), cassetteID, testCommonData.getPROTID(), purposeList.get(0));
        return cassetteID;
    }

    public ObjectIdentifier manual_Loading_Without_StartLotsReservation(){
        //【step1】stb
        ObjectIdentifier lotID = ((Results.WaferLotStartReqResult) stbCase.STB_NotPreparedLot().getBody()).getLotID();
        //【step2】load
        ObjectIdentifier cassetteID = manual_Loading_Without_StartLotsReservation(lotID);
        //【step3】einfo/eqp_info/inq
        Results.EqpInfoInqResult eqpInfoInqResult = (Results.EqpInfoInqResult) electronicInformationTestCase.eqpInfoInqCase(testCommonData.getEQUIPMENTID()).getBody();
        Assert.isTrue(eqpInfoInqResult.getEquipmentStatusInfo().getEquipmentAvailableFlag(), "test fail");
        return cassetteID;
    }

    public ObjectIdentifier manual_Loading_Without_StartLotsReservation(ObjectIdentifier lotID){
        //【step2】einfo/eqp_info/inq
        electronicInformationTestCase.eqpInfoInqCase(testCommonData.getEQUIPMENTID());
        //【step3】lot info and get cassette id
        List<ObjectIdentifier> lotIDList = new ArrayList<>();
        lotIDList.add(lotID);
        Results.LotInfoInqResult lotInfoInqResult = (Results.LotInfoInqResult) lotGeneralTestCase.getLotInfoCase(lotIDList).getBody();
        Infos.LotListInCassetteInfo lotListInCassetteInfo = lotInfoInqResult.getLotListInCassetteInfo();
        ObjectIdentifier cassetteID = lotListInCassetteInfo.getCassetteID();
        //【step4】load purpose
        List<String> purposeList = (List<String>) equipmentTestCase.getLoadPurposeTypeCase(BizConstant.SP_LOADPURPOSETYPE_PROCESSLOT).getBody();
        equipmentTestCase.carrierLoadingRpt(testCommonData.getEQUIPMENTID(), cassetteID, testCommonData.getPROTID(), purposeList.get(0));
        return cassetteID;
    }

    public ObjectIdentifier manual_Loading_Without_StartLotsReservation(ObjectIdentifier lotID,ObjectIdentifier equipmentID){
       return this.manual_Loading_Without_StartLotsReservation(lotID, equipmentID, null);
    }

    public ObjectIdentifier manual_Loading_Without_StartLotsReservation(ObjectIdentifier lotID,ObjectIdentifier equipmentID, ObjectIdentifier portID){
        //【step2】einfo/eqp_info/inq
        electronicInformationTestCase.eqpInfoInqCase(equipmentID);
        //【step3】lot info and get cassette id
        List<ObjectIdentifier> lotIDList = new ArrayList<>();
        lotIDList.add(lotID);
        Results.LotInfoInqResult lotInfoInqResult = (Results.LotInfoInqResult) lotGeneralTestCase.getLotInfoCase(lotIDList).getBody();
        Infos.LotListInCassetteInfo lotListInCassetteInfo = lotInfoInqResult.getLotListInCassetteInfo();
        ObjectIdentifier cassetteID = lotListInCassetteInfo.getCassetteID();
        //【step4】load purpose
        List<String> purposeList = (List<String>) equipmentTestCase.getLoadPurposeTypeCase(BizConstant.SP_LOADPURPOSETYPE_PROCESSLOT).getBody();
        if (portID == null){
            portID = testCommonData.getPROTID();
        }
        equipmentTestCase.carrierLoadingRpt(equipmentID, cassetteID, portID, purposeList.get(0));
        return cassetteID;
    }

    public void manualLoadingCarrierAlreadyReservedInAnotherEquipment(){
        //【step1】reserve in one equipment
        ObjectIdentifier cassetteID = startLotsReservationCase.startLotsReservation();
        // 【step2】load on another equipment
        List<String> purposeList = (List<String>) equipmentTestCase.getLoadPurposeTypeCase(BizConstant.SP_LOADPURPOSETYPE_PROCESSLOT).getBody();
        try {
            equipmentTestCase.carrierLoadingRpt(testCommonData.getEQUIPMENTID2(), cassetteID, testCommonData.getPROTID(), purposeList.get(0));
            Assert.isTrue(false, "test fail");
        } catch (ServiceException e) {
            Assert.isTrue(Validations.isEquals(retCodeConfig.getCastResvedForAnotherEqp(), e.getCode()), "test fail");
        }
    }

    public void manualLoadingCarrierThatTransferStatusIsEI(){
        //【step1】load on one equipment
        ObjectIdentifier cassetteID = this.manual_Loading_Without_StartLotsReservation();
        // 【step2】load on another equipment
        List<String> purposeList = (List<String>) equipmentTestCase.getLoadPurposeTypeCase(BizConstant.SP_LOADPURPOSETYPE_PROCESSLOT).getBody();
        try {
            equipmentTestCase.carrierLoadingRpt(testCommonData.getEQUIPMENTID2(), cassetteID, testCommonData.getPROTID(), purposeList.get(0));
            Assert.isTrue(false, "test fail");
        } catch (ServiceException e) {
            Assert.isTrue(Validations.isEquals(retCodeConfig.getInvalidCassetteTransferState(), e.getCode()), "test fail");
        }
    }

    public void manualLoadingCarrierNotSupposedToBeProcessedInCurrentSelectedEquipment(){
        //【step1】 product n stb lots
        List<ObjectIdentifier> lots = stbCase.stb_NLots_NotPreparedCase(1, true);
        //【step2】 skip to 1000.0200
        operationSkipCase.skipSpecificStep(lots, "1000.0200", true);
        //【step3】 load
        try {
            this.manual_Loading_Without_StartLotsReservation(lots.get(0));
            Assert.isTrue(false, "test fail");
        } catch (ServiceException e) {
            Assert.isTrue(Validations.isEquals(retCodeConfig.getNotCandidateLotForOperationStart(), e.getCode()), "test fail");
        }
    }

    public void loadReservedProcessLotCarrierToPortThatPurposeTypeIsProcessMonitorLot(){
        //【step1】 start reserve
        ObjectIdentifier cassetteID = startLotsReservationCase.startLotsReservation();
        //【step2】 load
        List<String> purposeList = (List<String>) equipmentTestCase.getLoadPurposeTypeCase(BizConstant.SP_LOADPURPOSETYPE_PROCESSMONITORLOT).getBody();
        try {
            equipmentTestCase.carrierLoadingRpt(testCommonData.getEQUIPMENTID(), cassetteID, testCommonData.getPROTID(), purposeList.get(0));
            Assert.isTrue(false, "test fail");
        } catch (ServiceException e) {
            Assert.isTrue(Validations.isEquals(retCodeConfig.getNotSameLoadPurpose(), e.getCode()), "test fail");
        }
    }

    public void loadCarrierWithCategoryFOUPToPortThatCagetoryDoesNotIncludeFOUP(){
        //【step1】stb and skip
        List<Infos.LotInfo> lotInfoList = operationSkipCase.stbNLotsAndSkipSpecificStep(1, true, "2000.0400", true);
        Infos.LotInfo lotInfo = lotInfoList.get(0);
        //【step2】get connectedRouteList
        List<Infos.ConnectedRouteList> connectedRouteList = commonTestCase.getConnectedRouteList(lotInfo.getLotBasicInfo().getLotID(), lotInfo.getLotLocationInfo().getCassetteID(), "");
        //【step3】sub route
        lotSubRouteBranchTestCase.subRouteBranchReq(lotInfo.getLotBasicInfo().getLotID(),
                lotInfo.getLotOperationInfo().getRouteID(),
                lotInfo.getLotOperationInfo().getOperationNumber(),
                connectedRouteList.get(0).getRouteID(), connectedRouteList.get(0).getReturnOperationNumber());
        //【step3】lot info
        Results.LotInfoInqResult lotInfoInqResult = (Results.LotInfoInqResult) lotGeneralTestCase.getLotInfoCase(Arrays.asList(lotInfo.getLotBasicInfo().getLotID())).getBody();
        Assert.isTrue(lotInfoInqResult.getLotInfoList().get(0).getLotOperationInfo().getOperationNumber().equals("1000.0100"), "test fail");
        //【step4】skip
        List<Infos.LotInfo> lotInfoList2 = operationSkipCase.skipSpecificStep(Arrays.asList(lotInfo.getLotBasicInfo().getLotID()), "1000.0200", true);
        List<Infos.LotEquipmentList> lotEquipmentList = lotInfoList2.get(0).getLotOperationInfo().getLotEquipmentList();
        //【step5】load
        List<String> purposeList = (List<String>) equipmentTestCase.getLoadPurposeTypeCase(BizConstant.SP_LOADPURPOSETYPE_PROCESSLOT).getBody();
        try {
            equipmentTestCase.carrierLoadingRpt(lotEquipmentList.get(0).getEquipmentID(), lotInfoList2.get(0).getLotLocationInfo().getCassetteID(), new ObjectIdentifier("P1"), purposeList.get(0));
            Assert.isTrue(false, "test fail");
        } catch (ServiceException e) {
            Assert.isTrue(Validations.isEquals(retCodeConfig.getNotCandidateLotForOperationStart(), e.getCode()), "test fail");
        }
    }

    public ObjectIdentifier loadCarrierWithAuto1OperationMode(){
        //【step1】einfo/eqp_info/inq
        Results.EqpInfoInqResult eqpInfoInqResult = (Results.EqpInfoInqResult) electronicInformationTestCase.eqpInfoInqCase(testCommonData.getEQUIPMENTID()).getBody();
        //【step2】change eqp mode
        equipmentTestCase.changeOperationModeToAuto(testCommonData.getEQUIPMENTID(), "Auto-1");
        //【step3】change port status to loadReq
        List<ObjectIdentifier> portIDs = new ArrayList<>();
        eqpInfoInqResult.getEquipmentPortInfo().getEqpPortStatuses().forEach(eqpPortStatus -> portIDs.add(eqpPortStatus.getAssociatedPortID()));
        equipmentTestCase.changePortStatus(testCommonData.getEQUIPMENTID(), portIDs, "LoadReq");
        //【step4】reserve
        ObjectIdentifier cassetteID = startLotsReservationCase.startLotsReservation();
        //【step5】change port1 status to loadComp
        equipmentTestCase.changePortStatus(testCommonData.getEQUIPMENTID(), portIDs, "LoadComp");
        //【step6】load
        List<String> purposeList = (List<String>) equipmentTestCase.getLoadPurposeTypeCase(BizConstant.SP_LOADPURPOSETYPE_PROCESSLOT).getBody();
        equipmentTestCase.carrierLoadingRpt(testCommonData.getEQUIPMENTID(), cassetteID, testCommonData.getPROTID(), purposeList.get(0));
        return cassetteID;
    }

    public void loadCarrierWithAuto2OperationMode(){
        //【step1】einfo/eqp_info/inq
        Results.EqpInfoInqResult eqpInfoInqResult = (Results.EqpInfoInqResult) electronicInformationTestCase.eqpInfoInqCase(testCommonData.getEQUIPMENTID()).getBody();
        //【step2】change eqp mode
        equipmentTestCase.changeOperationModeToAuto(testCommonData.getEQUIPMENTID(), "Auto-2");
        //【step3】change port status to loadReq
        List<ObjectIdentifier> portIDs = new ArrayList<>();
        eqpInfoInqResult.getEquipmentPortInfo().getEqpPortStatuses().forEach(eqpPortStatus -> portIDs.add(eqpPortStatus.getAssociatedPortID()));
        equipmentTestCase.changePortStatus(testCommonData.getEQUIPMENTID(), portIDs, "LoadReq");
        //【step4】reserve
        ObjectIdentifier cassetteID = startLotsReservationCase.startLotsReservation();
        //【step5】change port1 status to loadComp
        equipmentTestCase.changePortStatus(testCommonData.getEQUIPMENTID(), portIDs, "LoadComp");
        //【step6】load
        List<String> purposeList = (List<String>) equipmentTestCase.getLoadPurposeTypeCase(BizConstant.SP_LOADPURPOSETYPE_PROCESSLOT).getBody();
        equipmentTestCase.carrierLoadingRpt(testCommonData.getEQUIPMENTID(), cassetteID, testCommonData.getPROTID(), purposeList.get(0));
    }

    public void loadingCarrierContainedProcessLotToEquipmentWithMultiRecipeCapabilityBatch(){
        //【step1】einfo/eqp_info/inq
        Results.EqpInfoInqResult eqpInfoInqResult = (Results.EqpInfoInqResult) electronicInformationTestCase.eqpInfoInqCase(new ObjectIdentifier("1BKD01")).getBody();
        Assert.isTrue(eqpInfoInqResult.getEquipmentBasicInfo().getMultiRecipeCapability().equals("Batch"), " eqp can not use");
        //【step2】stb 1 lot
        List<ObjectIdentifier> lots = stbCase.stb_NLots_NotPreparedCase(1, true);
        //【step3】lot in cassette
        Results.LotInfoInqResult lotInfoInqResult = (Results.LotInfoInqResult) lotGeneralTestCase.getLotInfoCase(lots).getBody();
        //【step4】load
        List<String> purposeList = (List<String>) equipmentTestCase.getLoadPurposeTypeCase(BizConstant.SP_LOADPURPOSETYPE_PROCESSLOT).getBody();
        equipmentTestCase.carrierLoadingRpt(new ObjectIdentifier("1BKD01"), lotInfoInqResult.getLotListInCassetteInfo().getCassetteID(), new ObjectIdentifier("P2"), purposeList.get(0));

    }

    public void loadingCarrierContainedProcessLotToEquipmentWithMultiRecipeCapabilityMultiRecipe(){
        //【step1】einfo/eqp_info/inq
        Results.EqpInfoInqResult eqpInfoInqResult = (Results.EqpInfoInqResult) electronicInformationTestCase.eqpInfoInqCase(testCommonData.getEQUIPMENTID()).getBody();
        Assert.isTrue(eqpInfoInqResult.getEquipmentBasicInfo().getMultiRecipeCapability().equals("Multiple Recipe"), " eqp can not use");
        //【step2】load
        this.manual_Loading_Without_StartLotsReservation();
    }

    public void currentEquipmentStatusIsConditionalAvailableAndSubLotTypeIsEngineering(){
        //【step1】einfo/eqp_info/inq
        Results.EqpInfoInqResult eqpInfoInqResult = (Results.EqpInfoInqResult) electronicInformationTestCase.eqpInfoInqCase(testCommonData.getEQUIPMENTID()).getBody();
        //【step2】change eqp status
        equipmentTestCase.eqpStatusChange(eqpInfoInqResult.getEquipmentID(), "ENG", "AM");
        // 【step3】load
        try {
            this.manual_Loading_Without_StartLotsReservation();
            Assert.isTrue(false, "test fail");
        } catch (ServiceException e) {
            Assert.isTrue(Validations.isEquals(retCodeConfig.getNotCandidateLotForOperationStart(), e.getCode()), "test fail");
        }
    }

    public void loadCarrierWhichTransferStateIsManualIn(){
        //【step1】 stb
        List<ObjectIdentifier> lots = stbCase.stb_NLots_NotPreparedCase(1, true);
        //【step2】get lotInfo
        Results.LotInfoInqResult lotInfoInqResult = (Results.LotInfoInqResult) lotGeneralTestCase.getLotInfoCase(lots).getBody();
        // 【step3】change tansfer status
        commonTestCase.lotCassetteXferStatusChange(lotInfoInqResult.getLotInfoList().get(0).getLotLocationInfo().getCassetteID(), "MI");
        // 【step4】load
        try {
            this.manual_Loading_Without_StartLotsReservation(lots.get(0));
            Assert.isTrue(false, "test fail");
        } catch (ServiceException e) {
            Assert.isTrue(Validations.isEquals(retCodeConfig.getInvalidCassetteTransferState(), e.getCode()), "test fail");
        }
    }

    public void loadCarrierWhichStatusIsNotAvailable(){
        //【step1】 stb
        List<ObjectIdentifier> lots = stbCase.stb_NLots_NotPreparedCase(1, true);
        //【step2】get lotInfo
        Results.LotInfoInqResult lotInfoInqResult = (Results.LotInfoInqResult) lotGeneralTestCase.getLotInfoCase(lots).getBody();
        //【step3】change carrier's status
        Infos.LotInfo lotInfo = lotInfoInqResult.getLotInfoList().get(0);
        commonTestCase.changeDurableStatus(lotInfo.getLotLocationInfo().getCassetteID(), "NOTAVAILABLE", "Cassette");
        // 【step4】load
        try {
            this.manual_Loading_Without_StartLotsReservation(lots.get(0));
            Assert.isTrue(false, "test fail");
        } catch (ServiceException e) {
            Assert.isTrue(Validations.isEquals(retCodeConfig.getInvalidCassetteState(), e.getCode()), "test fail");
        }
    }

    public void loadCarrierWhichStatusIsInuse(){
        //【step1】 stb
        List<ObjectIdentifier> lots = stbCase.stb_NLots_NotPreparedCase(1, true);
        //【step2】get lotInfo
        Results.LotInfoInqResult lotInfoInqResult = (Results.LotInfoInqResult) lotGeneralTestCase.getLotInfoCase(lots).getBody();
        //【step3】change carrier's status
        Infos.LotInfo lotInfo = lotInfoInqResult.getLotInfoList().get(0);
        commonTestCase.changeDurableStatus(lotInfo.getLotLocationInfo().getCassetteID(), "INUSE", "Cassette");
        // 【step4】load
        this.manual_Loading_Without_StartLotsReservation(lots.get(0));
    }

    public void loadCarrierWhichLotHoldStateIsOnHold(){
        //【step1】 stb and lot hold
        Infos.LotInfo lotInfo = lotHoldCase.STB_After_LotHold_On_Current();
        // 【step2】load
        try {
            this.manual_Loading_Without_StartLotsReservation(lotInfo.getLotBasicInfo().getLotID());
            Assert.isTrue(false, "test fail");
        } catch (ServiceException e) {
            Assert.isTrue(Validations.isEquals(retCodeConfig.getNotCandidateLotForOperationStart(), e.getCode()), "test fail");
        }
    }

    public void restrictionEQPWhenMoveIn(){
        // 【step1】inhibit equipment
        entityInhibityCase.inhibitSpecCondition(testCommonData.getEQUIPMENTID());
        // 【step2】load
        try {
            this.manual_Loading_Without_StartLotsReservation();
            Assert.isTrue(false, "test fail");
        } catch (ServiceException e) {
            Assert.isTrue(Validations.isEquals(retCodeConfig.getNotCandidateLotForOperationStart(), e.getCode()), "test fail");
        }
    }

    public void loadCarrierWhichExistScrapWaferInLots(){
        //【step1】stb and scrap some wafers
        Infos.LotInfo lotInfo = scrapCase.scrap_norml();
        // 【step2】load
        try {
            this.manual_Loading_Without_StartLotsReservation(lotInfo.getLotBasicInfo().getLotID());
            Assert.isTrue(false, "test fail");
        } catch (ServiceException e) {
            Assert.isTrue(Validations.isEquals(retCodeConfig.getFoundScrap(), e.getCode()), "test fail");
        }

    }

    public void loadingCarrierContainedProcessLotToEquipmentWithMultiRecipeCapabilitySingleRecipe(){
        //【step1】einfo/eqp_info/inq
        Results.EqpInfoInqResult eqpInfoInqResult = (Results.EqpInfoInqResult) electronicInformationTestCase.eqpInfoInqCase(new ObjectIdentifier("1CDS02")).getBody();
        Assert.isTrue(eqpInfoInqResult.getEquipmentBasicInfo().getMultiRecipeCapability().equals("Single Recipe"), " eqp can not use");
        //【step2】stb and skip to 2000.0350
        List<Infos.LotInfo> lotInfoList = operationSkipCase.stbNLotsAndSkipSpecificStep(1, true, "2000.0350", true);
        Infos.LotInfo lotInfo = lotInfoList.get(0);
        //【step3】split
        ObjectIdentifier childLot = lotSplitCase.splitBySpecificLotID(lotInfo.getLotBasicInfo().getLotID());
        //【step4】child lot skip
        operationSkipCase.skipSpecificStep(Arrays.asList(childLot), "5000.0100", true);
        //【step5】load
        try {
            this.manual_Loading_Without_StartLotsReservation(lotInfo.getLotBasicInfo().getLotID(), new ObjectIdentifier("1CDS02"));
            Assert.isTrue(false, "test fail");
        } catch (ServiceException e) {
            Assert.isTrue(Validations.isEquals(retCodeConfig.getCassetteEquipmentConditionError(), e.getCode()), "test fail");
        }
    }

    public void loadCarrierToEquipmentWhichNeedReticle(){
        // 【step1】stb product 1.01
        ObjectIdentifier lotID = stbCase.STB_PRODUCT101(false);
        // 【step2】skip to the 7000.0100
        List<Infos.LotInfo> lotInfoList = operationSkipCase.skipSpecificStep(Arrays.asList(lotID), "7000.0100", true);
        Infos.LotInfo lotInfo = lotInfoList.get(0);
        // 【step3】eqp info
        Results.EqpInfoInqResult eqpInfo = electronicInformationTestCase.getEqpInfo(new ObjectIdentifier("1TKD01_EXD01"));
        List<Infos.StoredReticle> storedReticleList = eqpInfo.getEquipmentAdditionalReticleAttribute().getStoredReticleList();
        Assert.isTrue(CimArrayUtils.getSize(storedReticleList) > 0, "the equipment you selected is not proper");
        // 【step3】judge the equipment need to reticle equipment in
        List<Infos.StoredReticle> storedReticleList2 = equipmentTestCase.keepEquipmentReticleAvailable(new ObjectIdentifier("1TKD01_EXD01"), 2);
        // 【step4】change all of the reticles to notavailable
        for (Infos.StoredReticle storedReticle : storedReticleList2){
            commonTestCase.changeDurableStatus(storedReticle.getReticleID(), "NOTAVAILABLE", "Reticle");
        }
        // 【step4】 load
        try {
            this.manual_Loading_Without_StartLotsReservation(lotInfo.getLotBasicInfo().getLotID(), new ObjectIdentifier("1TKD01_EXD01"));
            Assert.isTrue(false, "test fail");
        } catch (ServiceException e) {
            Assert.isTrue(Validations.isEquals(retCodeConfig.getNotCandidateLotForOperationStart(), e.getCode()), "test fail, because exception infomation is :" + e.getMessage());
        }
        // 【step5】 change one of reticles to available
        commonTestCase.changeDurableStatus(storedReticleList2.get(0).getReticleID(), "AVAILABLE", "Reticle");
        // 【step6】 then load ,should success
        this.manual_Loading_Without_StartLotsReservation(lotInfo.getLotBasicInfo().getLotID(), new ObjectIdentifier("1TKD01_EXD01"));
    }

    public void loading_Without_StartLotsReservationSpecifiedEqpAndCassettes(ObjectIdentifier equipmentID, List<ObjectIdentifier> cassetteIDList){
        //【step1】einfo/eqp_info/inq
        Results.EqpInfoInqResult eqpInfoInqResult = (Results.EqpInfoInqResult) electronicInformationTestCase.eqpInfoInqCase(equipmentID).getBody();
        int count = 0;
        String loadPurpose = BizConstant.SP_LOADPURPOSETYPE_PROCESSLOT;
        for (ObjectIdentifier cassetteID : cassetteIDList){
            List<Infos.LotInfo> lotInfosByCassette = lotGeneralTestCase.getLotInfosByCassette(cassetteID);
            if (CimArrayUtils.isEmpty(lotInfosByCassette)){
                loadPurpose = BizConstant.SP_LOADPURPOSETYPE_EMPTYCASSETTE;
            }
            equipmentTestCase.carrierLoadingRpt(equipmentID, cassetteID, eqpInfoInqResult.getEquipmentPortInfo().getEqpPortStatuses().get(count).getPortID(), loadPurpose);
            count++;
        }
    }

}