package com.fa.cim.newIntegration.internalBuffer.scase;

import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.Response;
import com.fa.cim.common.utils.CimObjectUtils;
import com.fa.cim.common.utils.Validations;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.controller.einfo.ElectronicInformationInqController;
import com.fa.cim.controller.equipment.EquipmentController;
import com.fa.cim.controller.equipment.EquipmentInqController;
import com.fa.cim.controller.tms.TransferManagementSystemController;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;
import com.fa.cim.newIntegration.common.TestUtils;
import com.fa.cim.newIntegration.dto.TestInfos;
import com.fa.cim.newIntegration.stb.scase.STBCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/12/5        ********             Bear               create file
 *
 * @author: Bear
 * @date: 2019/12/5 10:47
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@Service
public class LoadForInternalBufferCase {

    @Autowired
    TestUtils testUtils;

    @Autowired
    ElectronicInformationInqController electronicInformationInqController;

    @Autowired
    private StartReservationForInternalBufferCase startReservationForInternalBufferCase;

    @Autowired
    private EquipmentController equipmentController;

    @Autowired
    private RetCodeConfig retCodeConfig;

    @Autowired
    private STBCase stbCase;

    @Autowired
    private EquipmentInqController equipmentInqController;

    @Autowired
    private TransferManagementSystemController transferManagementSystemController;

    public Response load(TestInfos.LoadForInternalBufferInfo loadForInternalBufferInfo) {
        ObjectIdentifier lotID = loadForInternalBufferInfo.getLotID();
        ObjectIdentifier equipmentID = loadForInternalBufferInfo.getEquipmentID();
        String loadPurposeType = loadForInternalBufferInfo.getLoadPurposeType();
        ObjectIdentifier portID = loadForInternalBufferInfo.getPortID();
        ObjectIdentifier cassetteID = loadForInternalBufferInfo.getCassetteID();
        //【step1】einfo/eqp_info_for_ib/inq
        Params.EqpInfoForIBInqParams eqpInfoForIBInqParams = new Params.EqpInfoForIBInqParams();
        eqpInfoForIBInqParams.setUser(testUtils.getUser());
        eqpInfoForIBInqParams.setEquipmentID(equipmentID);
        eqpInfoForIBInqParams.setRequestFlagForBasicInfo(true);
        eqpInfoForIBInqParams.setRequestFlagForChamberInfo(true);
        eqpInfoForIBInqParams.setRequestFlagForInprocessingLotInfo(true);
        eqpInfoForIBInqParams.setRequestFlagForInternalBufferInfo(true);
        eqpInfoForIBInqParams.setRequestFlagForPMInfo(true);
        eqpInfoForIBInqParams.setRequestFlagForPortInfo(true);
        eqpInfoForIBInqParams.setRequestFlagForRSPPortInfo(true);
        eqpInfoForIBInqParams.setRequestFlagForReservedControlJobInfo(true);
        eqpInfoForIBInqParams.setRequestFlagForStatusInfo(true);
        eqpInfoForIBInqParams.setRequestFlagForStockerInfo(true);
        Response response = equipmentInqController.eqpInfoForIBInq(eqpInfoForIBInqParams);
        Validations.isSuccessWithException(response);

        if (loadForInternalBufferInfo.isNeedStartReserved()) {
            //【step2】start reservation
            List<TestInfos.StartReservationForInternalBufferInfo> startReservationForInternalBufferInfos = new ArrayList<>();
            startReservationForInternalBufferInfos.add(new TestInfos.StartReservationForInternalBufferInfo(lotID, null, loadPurposeType, portID, portID));
            startReservationForInternalBufferCase.startReservation(loadForInternalBufferInfo.getEquipmentID(), startReservationForInternalBufferInfos);
        }

        //【step3】eqp/carrier_loading_for_ib/rpt
        Params.CarrierLoadingForIBRptParams carrierLoadingForIBRptParams = new Params.CarrierLoadingForIBRptParams();
        carrierLoadingForIBRptParams.setUser(testUtils.getUser());
        carrierLoadingForIBRptParams.setCassetteID(cassetteID);
        carrierLoadingForIBRptParams.setLoadPurposeType(loadPurposeType);
        carrierLoadingForIBRptParams.setPortID(portID);
        carrierLoadingForIBRptParams.setEquipmentID(equipmentID);
        return equipmentController.carrierLoadingForIBRpt(carrierLoadingForIBRptParams);
    }

    public Response load_WithStartReservation() {
        ObjectIdentifier equipmentID = new ObjectIdentifier("1FHI01");
        //【step1】make one product lot and skip to internal buffer eqp
        String bankID = "BNK-0S";
        String sourceProductID = "RAW-2000.01";
        String productID = "PRODUCT0.01";
        String operationNumber = "2000.0200";
        TestInfos.StbInfo stbInfo = new TestInfos.StbInfo(bankID, sourceProductID, productID, 25L, false);
        ObjectIdentifier lotID = testUtils.stbAndSkip(stbInfo, operationNumber);

        //【step2】get the cassette id by lotID
        ObjectIdentifier cassetteID = testUtils.getCassetteIDByLotID(lotID);

        //【step3】loading
        TestInfos.LoadForInternalBufferInfo loadForInternalBufferInfo = new TestInfos.LoadForInternalBufferInfo();
        loadForInternalBufferInfo.setCassetteID(cassetteID);
        loadForInternalBufferInfo.setEquipmentID(equipmentID);
        loadForInternalBufferInfo.setLoadPurposeType("Process Lot");
        loadForInternalBufferInfo.setLotID(lotID);
        loadForInternalBufferInfo.setPortID(new ObjectIdentifier("P1"));
        loadForInternalBufferInfo.setNeedStartReserved(true);
        return this.load(loadForInternalBufferInfo);
    }

    public Response load_WithStartReservation_PortNotMatch() {
        //【step1】make one product lot and skip to internal buffer eqp
        String bankID = "BNK-0S";
        String sourceProductID = "RAW-2000.01";
        String productID = "PRODUCT0.01";
        String operationNumber = "2000.0200";
        TestInfos.StbInfo stbInfo = new TestInfos.StbInfo(bankID, sourceProductID, productID, 25L, false);
        ObjectIdentifier lotID = testUtils.stbAndSkip(stbInfo, operationNumber);

        //【step2】get the cassette id by lotID
        ObjectIdentifier cassetteID = testUtils.getCassetteIDByLotID(lotID);

        ObjectIdentifier equipmentID = new ObjectIdentifier("1FHI01");
        //【step3】start reservation
        ObjectIdentifier loadPortID = new ObjectIdentifier("P1");
        ObjectIdentifier unLoadPortID = new ObjectIdentifier("P1");
        String loadPurpose = "Process Lot";
        List<TestInfos.StartReservationForInternalBufferInfo> startReservationForInternalBufferInfos = new ArrayList<>();
        startReservationForInternalBufferInfos.add(new TestInfos.StartReservationForInternalBufferInfo(lotID, null, loadPurpose, loadPortID, unLoadPortID));
        startReservationForInternalBufferCase.startReservation(equipmentID, startReservationForInternalBufferInfos);

        //【step3】loading
        TestInfos.LoadForInternalBufferInfo loadForInternalBufferInfo = new TestInfos.LoadForInternalBufferInfo();
        loadForInternalBufferInfo.setCassetteID(cassetteID);
        loadForInternalBufferInfo.setEquipmentID(equipmentID);
        loadForInternalBufferInfo.setLoadPurposeType(loadPurpose);
        loadForInternalBufferInfo.setLotID(lotID);
        loadForInternalBufferInfo.setPortID(new ObjectIdentifier("P2"));  // reserve port is 'P1', but load port is 'P2'
        loadForInternalBufferInfo.setNeedStartReserved(false);
        try {
            this.load(loadForInternalBufferInfo);
        } catch (ServiceException e) {
            Validations.check(!Validations.isEquals(e.getCode(), retCodeConfig.getInvalidLoadingPort()), "load_WithStartReservation_PortNotMatch() fail!");
        }
        return null;
    }


    public Response load_NoStartReservation() {
        ObjectIdentifier equipmentID = new ObjectIdentifier("1WSF01");
        //【step1】make one product lot and skip to internal buffer eqp
        String bankID = "BNK-0S";
        String sourceProductID = "RAW-2000.01";
        String productID = "PRODUCT0.01";
        String operationNumber = "2000.0100";
        TestInfos.StbInfo stbInfo = new TestInfos.StbInfo(bankID, sourceProductID, productID, 25L, false);
        ObjectIdentifier lotID = testUtils.stbAndSkip(stbInfo, operationNumber);

        //【step2】get the cassette id by lotID
        ObjectIdentifier cassetteID = testUtils.getCassetteIDByLotID(lotID);

        //【step3】loading
        TestInfos.LoadForInternalBufferInfo loadForInternalBufferInfo = new TestInfos.LoadForInternalBufferInfo();
        loadForInternalBufferInfo.setCassetteID(cassetteID);
        loadForInternalBufferInfo.setEquipmentID(new ObjectIdentifier("1WSF01"));
        loadForInternalBufferInfo.setLoadPurposeType("Process Lot");
        loadForInternalBufferInfo.setLotID(lotID);
        loadForInternalBufferInfo.setPortID(new ObjectIdentifier("P1"));
        loadForInternalBufferInfo.setNeedStartReserved(false);
        return this.load(loadForInternalBufferInfo);
    }

    public Response load_CassetteXferStatusIsEIStatus() {
        ObjectIdentifier equipmentID = new ObjectIdentifier("1WSF01");
        //【step1】make one product lot and skip to internal buffer eqp
        String bankID = "BNK-0S";
        String sourceProductID = "RAW-2000.01";
        String productID = "PRODUCT0.01";
        String operationNumber = "2000.0100";
        TestInfos.StbInfo stbInfo = new TestInfos.StbInfo(bankID, sourceProductID, productID, 25L, false);
        ObjectIdentifier lotID = testUtils.stbAndSkip(stbInfo, operationNumber);

        //【step2】get the cassette id by lotID
        ObjectIdentifier cassetteID = testUtils.getCassetteIDByLotID(lotID);

        //【step3】load the cassette, make the cassette xfer status is EI status
        TestInfos.LoadForInternalBufferInfo loadForInternalBufferInfo = new TestInfos.LoadForInternalBufferInfo();
        loadForInternalBufferInfo.setCassetteID(cassetteID);
        loadForInternalBufferInfo.setEquipmentID(new ObjectIdentifier("1WSF01"));
        loadForInternalBufferInfo.setLoadPurposeType("Process Lot");
        loadForInternalBufferInfo.setLotID(lotID);
        loadForInternalBufferInfo.setPortID(new ObjectIdentifier("P1"));
        loadForInternalBufferInfo.setNeedStartReserved(false);
        this.load(loadForInternalBufferInfo);

        //【step】load the cassette once again.
        try {
            this.load(loadForInternalBufferInfo);
        } catch (ServiceException e) {
            Assert.isTrue(Validations.isEquals(e.getCode(), retCodeConfig.getInvalidCassetteTransferState()), e.getMessage());
        }
        return null;
    }

    public Response load_EquipmentNotSupported() {
        ObjectIdentifier equipmentID = new ObjectIdentifier("1WSF01");
        //【step1】make one product lot and skip to internal buffer eqp
        String bankID = "BNK-0S";
        String sourceProductID = "RAW-2000.01";
        String productID = "PRODUCT0.01";
        String operationNumber = "1000.0200";   // the equipment don't support this step
        TestInfos.StbInfo stbInfo = new TestInfos.StbInfo(bankID, sourceProductID, productID, 25L, false);
        ObjectIdentifier lotID = testUtils.stbAndSkip(stbInfo, operationNumber);

        //【step2】get the cassette id by lotID
        ObjectIdentifier cassetteID = testUtils.getCassetteIDByLotID(lotID);

        //【step3】load the cassette, make the cassette xfer status is EI status
        TestInfos.LoadForInternalBufferInfo loadForInternalBufferInfo = new TestInfos.LoadForInternalBufferInfo();
        loadForInternalBufferInfo.setCassetteID(cassetteID);
        loadForInternalBufferInfo.setEquipmentID(new ObjectIdentifier("1WSF01")); // the equipment don't support this step
        loadForInternalBufferInfo.setLoadPurposeType("Process Lot");
        loadForInternalBufferInfo.setLotID(lotID);
        loadForInternalBufferInfo.setPortID(new ObjectIdentifier("P1"));
        loadForInternalBufferInfo.setNeedStartReserved(false);

        //【step】load the cassette once again.
        try {
            this.load(loadForInternalBufferInfo);
        } catch (ServiceException e) {
            Assert.isTrue(Validations.isEquals(e.getCode(), retCodeConfig.getNotCandidateLotForOperationStart()), e.getMessage());
        }
        return null;
    }

    public Response load_ProcessMonitorLotAfterReservated() {
        ObjectIdentifier equipmentID = new ObjectIdentifier("1WSF01");

        //【step1】stb one monitor lot
        TestInfos.StbMonitorLotInfo stbMonitorLotInfo = new TestInfos.StbMonitorLotInfo();
        stbMonitorLotInfo.setBankID("BK-CTRL");
        stbMonitorLotInfo.setProductCount(25);
        stbMonitorLotInfo.setSourceProductID("RAW-2000.01");
        ObjectIdentifier monitorLotID = stbCase.STB_MonitorLot(stbMonitorLotInfo);

        //【step2】get the cassette id by lotID
        ObjectIdentifier cassetteID = testUtils.getCassetteIDByLotID(monitorLotID);

        //【step3】loading
        String loadPurpose = "Process Monitor Lot";
        TestInfos.LoadForInternalBufferInfo loadForInternalBufferInfo = new TestInfos.LoadForInternalBufferInfo();
        loadForInternalBufferInfo.setCassetteID(cassetteID);
        loadForInternalBufferInfo.setEquipmentID(equipmentID);
        loadForInternalBufferInfo.setLoadPurposeType(loadPurpose);
        loadForInternalBufferInfo.setLotID(monitorLotID);
        loadForInternalBufferInfo.setPortID(new ObjectIdentifier("P1"));
        loadForInternalBufferInfo.setNeedStartReserved(false);
        return this.load(loadForInternalBufferInfo);
    }

    public Response load_CassetteCategoryNotSupport() {
        ObjectIdentifier equipmentID = new ObjectIdentifier("1FHI01");

        //【step1】find one empty cassette
        String cassetteCategory = "FOUP_CMP_IN";
        String cassetteStatus = "AVAILABLE";
        ObjectIdentifier cassetteID = testUtils.findEmptyCassette(cassetteCategory, cassetteStatus);

        //【step2】make one product lot and skip to internal buffer eqp
        String bankID = "BNK-0S";
        String sourceProductID = "RAW-2000.01";
        String productID = "PRODUCT0.01";
        String operationNumber = "2000.0200";
        TestInfos.StbInfo stbInfo = new TestInfos.StbInfo(bankID, sourceProductID, productID, 25L, false, cassetteID);
        ObjectIdentifier lotID = testUtils.stbAndSkip(stbInfo, operationNumber);

        //【step3】load the cassette, make the cassette xfer status is EI status
        TestInfos.LoadForInternalBufferInfo loadForInternalBufferInfo = new TestInfos.LoadForInternalBufferInfo();
        loadForInternalBufferInfo.setCassetteID(cassetteID);
        loadForInternalBufferInfo.setEquipmentID(equipmentID);
        loadForInternalBufferInfo.setLoadPurposeType("Process Lot");
        loadForInternalBufferInfo.setLotID(lotID);
        loadForInternalBufferInfo.setPortID(new ObjectIdentifier("P1"));
        loadForInternalBufferInfo.setNeedStartReserved(true);
        try {
            this.load(loadForInternalBufferInfo);
        } catch (ServiceException e) {
            Assert.isTrue(Validations.isEquals(e.getCode(), retCodeConfig.getCarrierPortCarrierCategoryUnmatch()), e.getMessage());
        }
        return null;
    }

    public Response load_EquipmentNotAvailable() {
        ObjectIdentifier equipmentID = new ObjectIdentifier("1FHI01");

        //【step1】make one product lot and skip to internal buffer eqp
        String bankID = "BNK-0S";
        String sourceProductID = "RAW-2000.01";
        String productID = "PRODUCT0.01";
        String operationNumber = "2000.0200";
        TestInfos.StbInfo stbInfo = new TestInfos.StbInfo(bankID, sourceProductID, productID, 25L, false);
        ObjectIdentifier lotID = testUtils.stbAndSkip(stbInfo, operationNumber);
        ObjectIdentifier cassetteID = testUtils.getCassetteIDByLotID(lotID);

        //【step2】change the equipment status from WLOT -> NST
        //【step2-1】eqp/candidate_eqp_status/inq
        Params.EqpStatusSelectionInqParams params = new Params.EqpStatusSelectionInqParams();
        params.setUser(testUtils.getUser());
        params.setEquipmentID(equipmentID);
        params.setAllInquiryFlag(false);
        Results.EqpStatusSelectionInqResult body = (Results.EqpStatusSelectionInqResult) equipmentInqController.eqpStatusSelectionInq(params).getBody();
        Predicate<Infos.CandidateE10Status> predicate = p -> CimObjectUtils.equalsWithValue(p.getE10Status(), "NST");
        Infos.CandidateE10Status candidateE10Status = body.getCandidateOtherE10List().stream().filter(predicate).findFirst().orElse(null);
        ObjectIdentifier equipmentStatusCode = null;
        if (candidateE10Status != null) {
            equipmentStatusCode = candidateE10Status.getCandidateEqpStatusList().get(0).getEquipmentStatusCode();
        }
        //【step2-2】change the port status
        Params.EqpStatusChangeReqParams eqpStatusChangeReqParams = new Params.EqpStatusChangeReqParams();
        eqpStatusChangeReqParams.setUser(testUtils.getUser());
        eqpStatusChangeReqParams.setEquipmentID(equipmentID);
        eqpStatusChangeReqParams.setEquipmentStatusCode(equipmentStatusCode);
        equipmentController.eqpStatusChangeReq(eqpStatusChangeReqParams);

        //【step3】load the cassette
        TestInfos.LoadForInternalBufferInfo loadForInternalBufferInfo = new TestInfos.LoadForInternalBufferInfo();
        loadForInternalBufferInfo.setCassetteID(cassetteID);
        loadForInternalBufferInfo.setEquipmentID(equipmentID);
        loadForInternalBufferInfo.setLoadPurposeType("Process Lot");
        loadForInternalBufferInfo.setLotID(lotID);
        loadForInternalBufferInfo.setPortID(new ObjectIdentifier("P1"));
        loadForInternalBufferInfo.setNeedStartReserved(false);
        try {
            this.load(loadForInternalBufferInfo);
        } catch (ServiceException e) {
            Assert.isTrue(Validations.isEquals(e.getCode(), retCodeConfig.getNotCandidateLotForOperationStart()), e.getMessage());
        }
        return null;
    }

    public Response load_CassetteXferStatusIsMI() {
        ObjectIdentifier equipmentID = new ObjectIdentifier("1FHI01");

        //【step1】make one product lot and skip to internal buffer eqp
        String bankID = "BNK-0S";
        String sourceProductID = "RAW-2000.01";
        String productID = "PRODUCT0.01";
        String operationNumber = "2000.0200";
        TestInfos.StbInfo stbInfo = new TestInfos.StbInfo(bankID, sourceProductID, productID, 25L, false);
        ObjectIdentifier lotID = testUtils.stbAndSkip(stbInfo, operationNumber);
        ObjectIdentifier cassetteID = testUtils.getCassetteIDByLotID(lotID);

        //【step2】change the cassette xfer status is MI
        Params.CarrierTransferStatusChangeRptParams carrierTransferStatusChangeRptParams = new Params.CarrierTransferStatusChangeRptParams();
        carrierTransferStatusChangeRptParams.setUser(testUtils.getUser());
        carrierTransferStatusChangeRptParams.setMachineID(new ObjectIdentifier("STK0101"));
        carrierTransferStatusChangeRptParams.setCarrierID(cassetteID);
        carrierTransferStatusChangeRptParams.setXferStatus("MI");
        carrierTransferStatusChangeRptParams.setManualInFlag(true);
        transferManagementSystemController.carrierTransferStatusChangeRpt(carrierTransferStatusChangeRptParams);

        //【step3】load the cassette
        TestInfos.LoadForInternalBufferInfo loadForInternalBufferInfo = new TestInfos.LoadForInternalBufferInfo();
        loadForInternalBufferInfo.setCassetteID(cassetteID);
        loadForInternalBufferInfo.setEquipmentID(equipmentID);
        loadForInternalBufferInfo.setLoadPurposeType("Process Lot");
        loadForInternalBufferInfo.setLotID(lotID);
        loadForInternalBufferInfo.setPortID(new ObjectIdentifier("P1"));
        loadForInternalBufferInfo.setNeedStartReserved(false);
        try {
            this.load(loadForInternalBufferInfo);
            Assert.isTrue(false, "the load must be fail!");
        } catch (ServiceException e) {
            Assert.isTrue(Validations.isEquals(e.getCode(), retCodeConfig.getInvalidCassetteTransferState()), e.getMessage());
        }
        return null;
    }

    public Response load_CassetteStatusNotAvailable() {
        ObjectIdentifier equipmentID = new ObjectIdentifier("1FHI01");

        //【step1】make one product lot and skip to internal buffer eqp
        String bankID = "BNK-0S";
        String sourceProductID = "RAW-2000.01";
        String productID = "PRODUCT0.01";
        String operationNumber = "2000.0200";
        TestInfos.StbInfo stbInfo = new TestInfos.StbInfo(bankID, sourceProductID, productID, 25L, false);
        ObjectIdentifier lotID = testUtils.stbAndSkip(stbInfo, operationNumber);
        ObjectIdentifier cassetteID = testUtils.getCassetteIDByLotID(lotID);

        //【step2】change the cassette status to NOTAVAILABLE
        String durableStaus = "NOTAVAILABLE";
        testUtils.changeCassetteStatus(cassetteID, durableStaus, null);

        //【step3】load the cassette
        TestInfos.LoadForInternalBufferInfo loadForInternalBufferInfo = new TestInfos.LoadForInternalBufferInfo();
        loadForInternalBufferInfo.setCassetteID(cassetteID);
        loadForInternalBufferInfo.setEquipmentID(equipmentID);
        loadForInternalBufferInfo.setLoadPurposeType("Process Lot");
        loadForInternalBufferInfo.setLotID(lotID);
        loadForInternalBufferInfo.setPortID(new ObjectIdentifier("P1"));
        loadForInternalBufferInfo.setNeedStartReserved(false);
        try {
            this.load(loadForInternalBufferInfo);
            Assert.isTrue(false, "the load must be fail!");
        } catch (ServiceException e) {
            Assert.isTrue(Validations.isEquals(e.getCode(), retCodeConfig.getInvalidCassetteState()), e.getMessage());
        }
        return null;
    }

    public Response load_CassetteStatusIsInUse() {
        ObjectIdentifier equipmentID = new ObjectIdentifier("1FHI01");

        //【step1】make one product lot and skip to internal buffer eqp
        String bankID = "BNK-0S";
        String sourceProductID = "RAW-2000.01";
        String productID = "PRODUCT0.01";
        String operationNumber = "2000.0200";
        TestInfos.StbInfo stbInfo = new TestInfos.StbInfo(bankID, sourceProductID, productID, 25L, false);
        ObjectIdentifier lotID = testUtils.stbAndSkip(stbInfo, operationNumber);
        ObjectIdentifier cassetteID = testUtils.getCassetteIDByLotID(lotID);

        //【step2】change the cassette status to NOTAVAILABLE
        String durableStaus = "INUSE";
        testUtils.changeCassetteStatus(cassetteID, durableStaus, null);

        //【step3】load the cassette
        TestInfos.LoadForInternalBufferInfo loadForInternalBufferInfo = new TestInfos.LoadForInternalBufferInfo();
        loadForInternalBufferInfo.setCassetteID(cassetteID);
        loadForInternalBufferInfo.setEquipmentID(equipmentID);
        loadForInternalBufferInfo.setLoadPurposeType("Process Lot");
        loadForInternalBufferInfo.setLotID(lotID);
        loadForInternalBufferInfo.setPortID(new ObjectIdentifier("P1"));
        loadForInternalBufferInfo.setNeedStartReserved(false);
        return this.load(loadForInternalBufferInfo);
    }
}