package com.fa.cim.newIntegration.internalBuffer.scase;

import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.OmCode;
import com.fa.cim.common.support.Response;
import com.fa.cim.common.utils.CimObjectUtils;
import com.fa.cim.common.utils.CimStringUtils;
import com.fa.cim.common.utils.Validations;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.controller.dispatch.DispatchController;
import com.fa.cim.controller.dispatch.DispatchInqController;
import com.fa.cim.controller.einfo.ElectronicInformationInqController;
import com.fa.cim.controller.equipment.EquipmentController;
import com.fa.cim.controller.equipment.EquipmentInqController;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;
import com.fa.cim.newIntegration.LotOperation.scase.LotHoldCase;
import com.fa.cim.newIntegration.LotOperation.scase.ScrapCase;
import com.fa.cim.newIntegration.bank.scase.VendorLotReceiveCase;
import com.fa.cim.newIntegration.common.TestUtils;
import com.fa.cim.newIntegration.dto.TestInfos;
import com.fa.cim.newIntegration.equipment.scase.MoveInCase;
import com.fa.cim.newIntegration.stb.scase.STBCase;
import com.fa.cim.newIntegration.tcase.ElectronicInformationTestCase;
import com.fa.cim.newIntegration.tcase.EquipmentTestCase;
import com.fa.cim.newIntegration.tcase.LotGeneralTestCase;
import com.fa.cim.newIntegration.tcase.OperationSkipTestCase;
import com.fa.cim.utils.GenerateVendorlot;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/11/26        ********             Bear               create file
 *
 * @author: Bear
 * @date: 2019/11/26 10:40
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@Service
public class StartReservationForInternalBufferCase {

    @Autowired
    ElectronicInformationInqController electronicInformationInqController;

    @Autowired
    EquipmentInqController equipmentInqController;

    @Autowired
    private EquipmentController equipmentController;

    @Autowired
    private STBCase stbCase;

    @Autowired
    private LotGeneralTestCase lotGeneralTestCase;

    @Autowired
    private OperationSkipTestCase operationSkipTestCase;

    @Autowired
    private EquipmentTestCase equipmentTestCase;

    @Autowired
    private ElectronicInformationTestCase electronicInformationTestCase;

    @Autowired
    private DispatchInqController dispatchInqController;

    @Autowired
    private DispatchController dispatchController;

    @Autowired
    private RetCodeConfig  retCodeConfig;

    @Autowired
    private LotHoldCase lotHoldCase;

    @Autowired
    private MoveInCase moveInCase;

    @Autowired
    private VendorLotReceiveCase vendorLotReceiveCase;

    @Autowired
    private ScrapCase scrapCase;

    @Autowired
    private TestUtils testUtils;

    public Response lotsMoveInReserveInfoForIBInqCase(List<Results.LotInfoInqResult> lotInfoInqResultList, ObjectIdentifier equipmentID) {
        List<Infos.StartCassette> startCassettes = new ArrayList<>();
        for (Results.LotInfoInqResult lotInfoInqResult : lotInfoInqResultList) {
            Infos.LotListInCassetteInfo lotListInCassetteInfo = lotInfoInqResult.getLotListInCassetteInfo();
            List<Infos.WaferMapInCassetteInfo> waferMapInCassetteInfoList = lotInfoInqResult.getWaferMapInCassetteInfoList();
            Infos.StartCassette startCassette = new Infos.StartCassette();
            startCassette.setCassetteID(lotListInCassetteInfo.getCassetteID());
            List<Infos.LotInCassette> lotInCassetteList = new ArrayList<>();
            List<Infos.LotInfo> lotInfoList = lotInfoInqResult.getLotInfoList();
            for (Infos.LotInfo lotInfo : lotInfoList) {
                Infos.LotInCassette lotInCassette = new Infos.LotInCassette();
                lotInCassette.setLotID(lotInfo.getLotBasicInfo().getLotID());
                lotInCassette.setLotType(lotInfo.getLotBasicInfo().getLotType());

                // set lot-wafer info
                List<Infos.LotWafer> lotWaferList = new ArrayList<>();
                List<Infos.WaferMapInCassetteInfo> waferMapInCassetteInfos = waferMapInCassetteInfoList.stream().filter(x -> CimObjectUtils.equalsWithValue(x.getLotID(), lotInfo.getLotBasicInfo().getLotID())).collect(Collectors.toList());
                for (Infos.WaferMapInCassetteInfo waferMapInCassetteInfo : waferMapInCassetteInfos) {
                    Infos.LotWafer lotWafer = new Infos.LotWafer();
                    lotWafer.setSlotNumber(Long.parseLong(waferMapInCassetteInfo.getSlotNumber().toString()));
                    lotWafer.setWaferID(waferMapInCassetteInfo.getWaferID());
                    lotWafer.setProcessJobExecFlag(!CimStringUtils.equals(lotInCassette.getLotType(),"Vendor")); // the lot which will go to in-processing must be set true. else it will be set false, eg：vendor lot
                    lotWafer.setProcessJobStatus("");     //the default value is ""
                    lotWafer.setControlWaferFlag(false); // the default value is false
                    lotWafer.setParameterUpdateFlag(false); // the default value is falsed
                    lotWaferList.add(lotWafer);
                }
                lotInCassette.setLotWaferList(lotWaferList);
                lotInCassette.setMonitorLotFlag(false);
                lotInCassette.setMoveInFlag(true);
                lotInCassette.setProductID(lotInfo.getLotProductInfo().getProductID());
                lotInCassette.setStartOperationInfo(new Infos.StartOperationInfo());
                lotInCassette.setStartRecipe(new Infos.StartRecipe());
                lotInCassette.setSubLotType(lotInfo.getLotBasicInfo().getSubLotType());
                lotInCassetteList.add(lotInCassette);
            }
            startCassette.setLotInCassetteList(lotInCassetteList);
            startCassettes.add(startCassette);
        }
        Params.LotsMoveInReserveInfoForIBInqParams lotsMoveInReserveInfoInqParams = new Params.LotsMoveInReserveInfoForIBInqParams();
        lotsMoveInReserveInfoInqParams.setUser(testUtils.getUser());
        lotsMoveInReserveInfoInqParams.setEquipmentID(equipmentID);
        lotsMoveInReserveInfoInqParams.setStartCassettes(startCassettes);
        Response response = dispatchInqController.lotsMoveInReserveInfoForIBInq(lotsMoveInReserveInfoInqParams);
        Validations.isSuccessWithException(response);
        return response;
    }

    public Response moveInReserveForIBReqCase(Results.LotsMoveInReserveInfoInqResult lotsMoveInReserveInfoInqResult
        , List<TestInfos.LoadCassetteInfo> loadCassetteInfoList) {
        Params.MoveInReserveForIBReqParams moveInReserveReqParams = new Params.MoveInReserveForIBReqParams();
        moveInReserveReqParams.setUser(testUtils.getUser());
        moveInReserveReqParams.setControlJobID(null);
        moveInReserveReqParams.setEquipmentID(lotsMoveInReserveInfoInqResult.getEquipmentID());
        List<Infos.StartCassette> strStartCassettes = lotsMoveInReserveInfoInqResult.getStrStartCassette();
        for (TestInfos.LoadCassetteInfo loadCassetteInfo : loadCassetteInfoList) {
            Infos.StartCassette startCassette = null;
            if (CimStringUtils.equals("Empty Cassette", loadCassetteInfo.getLoadPurposeType())) {
                startCassette = new Infos.StartCassette();
                strStartCassettes.add(startCassette);
            } else {
                Predicate<Infos.StartCassette> predicate = x -> CimObjectUtils.equalsWithValue(x.getCassetteID(), loadCassetteInfo.getCassetteID());
                startCassette = strStartCassettes.stream().filter(predicate).findFirst().orElse(null);
            }
            if (startCassette != null) {
                startCassette.setCassetteID(loadCassetteInfo.getCassetteID());
                startCassette.setLoadPortID(loadCassetteInfo.getLoadPortID());
                startCassette.setUnloadPortID(loadCassetteInfo.getLoadPortID());
                startCassette.setLoadPurposeType(loadCassetteInfo.getLoadPurposeType());
            }
        }
        //single cassette
        moveInReserveReqParams.setStartCassetteList(strStartCassettes);
        return dispatchController.moveInReserveForIBReq(moveInReserveReqParams);
    }

    public Response startReservation_SingleLot() {
        //[step1]make one product lot and skip to internal buffer eqp
        String bankID = "BNK-0S";
        String sourceProductID = "RAW-2000.01";
        String productID = "PRODUCT0.01";
        String operationNumber = "2000.0200";
        ObjectIdentifier equipmentID = new ObjectIdentifier("1FHI01");
        TestInfos.StbInfo stbInfo = new TestInfos.StbInfo(bankID, sourceProductID, productID, 25L, false);
        ObjectIdentifier lotID = testUtils.stbAndSkip(stbInfo, operationNumber);

        List<TestInfos.StartReservationForInternalBufferInfo> StartReservationForInternalBufferInfos = new ArrayList<>();
        StartReservationForInternalBufferInfos.add(new TestInfos.StartReservationForInternalBufferInfo(lotID, null, "Process Lot", new ObjectIdentifier("P1"),  new ObjectIdentifier("P1")));
        return this.startReservation(equipmentID, StartReservationForInternalBufferInfos);
    }

    public Response startReservation(ObjectIdentifier equipmentID, String operationNumber, TestInfos.StbInfo stbInfo, List<TestInfos.StartReservationForInternalBufferInfo> StartReservationForInternalBufferInfos) {
        ObjectIdentifier lotID = testUtils.stbAndSkip(stbInfo, operationNumber);
        return this.startReservation(equipmentID, StartReservationForInternalBufferInfos);
    }

    public Response startReservation_WithEmptyCassette() {
        ObjectIdentifier equipmentID = new ObjectIdentifier("1WSF01");
        //[step1]make one product lot and skip to internal buffer eqp
        String bankID = "BNK-0S";
        String sourceProductID = "RAW-2000.01";
        String productID = "PRODUCT0.01";
        String operationNumber = "2000.0100";
        TestInfos.StbInfo stbInfo = new TestInfos.StbInfo(bankID, sourceProductID, productID, 25L, false);
        ObjectIdentifier lotID = testUtils.stbAndSkip(stbInfo, operationNumber);

        //[step4]find one empty cassette
        String cassetteCategory = "FOUP";
        String cassetteStatus = "AVAILABLE";
        ObjectIdentifier emptyCassette = testUtils.findEmptyCassette(cassetteCategory, cassetteStatus);

        List<TestInfos.StartReservationForInternalBufferInfo> StartReservationForInternalBufferInfos = new ArrayList<>();
        StartReservationForInternalBufferInfos.add(new TestInfos.StartReservationForInternalBufferInfo(lotID, null, "Process Lot", new ObjectIdentifier("P1"),  new ObjectIdentifier("P1")));
        StartReservationForInternalBufferInfos.add(new TestInfos.StartReservationForInternalBufferInfo(null, emptyCassette, "Empty Cassette", new ObjectIdentifier("P2"),  new ObjectIdentifier("P2")));
        return this.startReservation(equipmentID, StartReservationForInternalBufferInfos);
    }

    public Response startReservation_CassetteCagetoryNotMatched() {
        //[step1]find one empty cassette
        ObjectIdentifier emptyCassetteID = testUtils.findEmptyCassette("CarCat1", "AVAILABLE");

        ObjectIdentifier equipmentID = new ObjectIdentifier("IN-FOUP");
        //[step2]make one product lot
        String bankID = "BNK-0S";
        String sourceProductID = "RAW-2000.01";
        String productID = "PRODUCT0.01";
        TestInfos.StbInfo stbInfo = new TestInfos.StbInfo(bankID, sourceProductID, productID, 25L, false, emptyCassetteID);
        ObjectIdentifier lotID = stbCase.STB_Normal(stbInfo);

        //[step3]make start reservation
        List<TestInfos.StartReservationForInternalBufferInfo> StartReservationForInternalBufferInfos = new ArrayList<>();
        StartReservationForInternalBufferInfos.add(new TestInfos.StartReservationForInternalBufferInfo(lotID, null, "Process Lot", new ObjectIdentifier("P1"),  new ObjectIdentifier("P1")));
        try {
            this.startReservation(equipmentID, StartReservationForInternalBufferInfos);
        }catch (ServiceException e) {
            Assert.isTrue(Validations.isEquals(e.getCode(), retCodeConfig.getInvalidCategoryCheck()), e.getMessage());
        }
        return null;
    }


    public Response startReservation_MutipleLotSinglePort() {
        //[step1]make two product lot and skip to internal buffer eqp
        String bankID = "BNK-0S";
        String sourceProductID = "RAW-2000.01";
        String productID = "PRODUCT0.01";
        String operationNumber = "2000.0200";
        TestInfos.StbInfo stbInfo = new TestInfos.StbInfo(bankID, sourceProductID, productID, 25L, false);

        ObjectIdentifier lotIDA = testUtils.stbAndSkip(stbInfo, operationNumber);
        ObjectIdentifier lotIDB = testUtils.stbAndSkip(stbInfo, operationNumber);

        ObjectIdentifier equipmentID = new ObjectIdentifier("1FHI01");
        List<TestInfos.StartReservationForInternalBufferInfo> StartReservationForInternalBufferInfos = new ArrayList<>();
        StartReservationForInternalBufferInfos.add(new TestInfos.StartReservationForInternalBufferInfo(lotIDA, null, "Process Lot", new ObjectIdentifier("P1"),  new ObjectIdentifier("P1")));
        StartReservationForInternalBufferInfos.add(new TestInfos.StartReservationForInternalBufferInfo(lotIDB, null, "Process Lot", new ObjectIdentifier("P1"),  new ObjectIdentifier("P1")));
        return this.startReservation(equipmentID, StartReservationForInternalBufferInfos);
    }

    public Response startReservation (ObjectIdentifier equipmentID, List<TestInfos.StartReservationForInternalBufferInfo> StartReservationForInternalBufferInfos) {
        List<TestInfos.LoadCassetteInfo> loadCassetteInfoList = new ArrayList<>();
        //[step1]einfo/eqp_info_for_ib/inq
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

        //[step3]what's next
        Results.WhatNextLotListResult whatNextLotListResult = (Results.WhatNextLotListResult) equipmentTestCase.whatNextInqCase(equipmentID).getBody();
        List<Infos.WhatNextAttributes> whatNextAttributesContent = (List<Infos.WhatNextAttributes> ) whatNextLotListResult.getWhatNextAttributesPage().getContent();

        List<Results.LotInfoInqResult> lotInfoInqResultList = new ArrayList<>();
        for (TestInfos.StartReservationForInternalBufferInfo StartReservationForInternalBufferInfo: StartReservationForInternalBufferInfos) {
            ObjectIdentifier lotID = StartReservationForInternalBufferInfo.getLotID();
            ObjectIdentifier emptyCassette = StartReservationForInternalBufferInfo.getEmptyCassette();
            String loadPurposeType = StartReservationForInternalBufferInfo.getLoadPurposeType();
            ObjectIdentifier loadPortID = StartReservationForInternalBufferInfo.getLoadPortID();
            ObjectIdentifier unLoadPortID = StartReservationForInternalBufferInfo.getUnLoadPortID();
            if (CimObjectUtils.isNotEmptyWithValue(emptyCassette)) {
                loadCassetteInfoList.add(new TestInfos.LoadCassetteInfo(emptyCassette, loadPurposeType, loadPortID,  unLoadPortID));
                continue;
            }

            Optional<Infos.WhatNextAttributes> first = whatNextAttributesContent.stream().filter(x -> CimObjectUtils.equalsWithValue(x.getLotID(), lotID)).findFirst();
            if (!first.isPresent()) {
                return null;
            }
            Infos.WhatNextAttributes whatNextAttributes = first.get();
            loadCassetteInfoList.add(new TestInfos.LoadCassetteInfo(whatNextAttributes.getCassetteID(), loadPurposeType, loadPortID,  unLoadPortID));

            //[step4]einfo/lot_list_in_cassette/inq
            Results.LotListByCarrierInqResult lotListByCarrierInqResult = (Results.LotListByCarrierInqResult) electronicInformationTestCase.lotListByCarrierInqCase(whatNextAttributes.getCassetteID()).getBody();

            //[step5]einfo/lot_info/inq
            Results.LotInfoInqResult lotInfoInqResult = (Results.LotInfoInqResult) lotGeneralTestCase.getLotInfoCase(lotListByCarrierInqResult.getLotListInCassetteInfo().getLotIDList()).getBody();
            lotInfoInqResultList.add(lotInfoInqResult);
        }

        //[step6]dispatch/lots_info_for_start_reservation_for_internal_buffer/inq
        Results.LotsMoveInReserveInfoInqResult lotsMoveInReserveInfoInqResult = (Results.LotsMoveInReserveInfoInqResult) this.lotsMoveInReserveInfoForIBInqCase(lotInfoInqResultList, equipmentID).getBody();

        //[step7]dispatch/start_lots_reservation/req
        Response response1 = this.moveInReserveForIBReqCase(lotsMoveInReserveInfoInqResult, loadCassetteInfoList);
        Validations.isSuccessWithException(response1);
        return response1;
    }

    public Response startReservation_MutipleLotMutiplePort() {
        //[step1]make two product lot and skip to internal buffer eqp
        String bankID = "BNK-0S";
        String sourceProductID = "RAW-2000.01";
        String productID = "PRODUCT0.01";
        String operationNumber = "2000.0200";
        TestInfos.StbInfo stbInfo = new TestInfos.StbInfo(bankID, sourceProductID, productID, 25L, false);
        ObjectIdentifier lotIDA = testUtils.stbAndSkip(stbInfo, operationNumber);
        ObjectIdentifier lotIDB = testUtils.stbAndSkip(stbInfo, operationNumber);

        ObjectIdentifier equipmentID = new ObjectIdentifier("1FHI01");

        List<TestInfos.StartReservationForInternalBufferInfo> StartReservationForInternalBufferInfos = new ArrayList<>();
        StartReservationForInternalBufferInfos.add(new TestInfos.StartReservationForInternalBufferInfo(lotIDA, null, "Process Lot", new ObjectIdentifier("P1"),  new ObjectIdentifier("P1")));
        StartReservationForInternalBufferInfos.add(new TestInfos.StartReservationForInternalBufferInfo(lotIDB, null, "Process Lot", new ObjectIdentifier("P2"),  new ObjectIdentifier("P2")));
        return this.startReservation(equipmentID, StartReservationForInternalBufferInfos);
    }


    public Response startReservation_MutipleLotPerCarrier() {
        String bankID = "BNK-0S";
        String sourceProductID = "RAW-2000.01";
        String productID = "PRODUCT0.01";
        String operationNumber = "2000.0100";
        TestInfos.StbInfo stbInfo = new TestInfos.StbInfo(bankID, sourceProductID, productID, 15L, true);
        ObjectIdentifier lotID = testUtils.stbAndSkip(stbInfo, operationNumber);

        ObjectIdentifier equipmentID = new ObjectIdentifier("1WSF01");
        List<TestInfos.StartReservationForInternalBufferInfo> StartReservationForInternalBufferInfos = new ArrayList<>();
        StartReservationForInternalBufferInfos.add(new TestInfos.StartReservationForInternalBufferInfo(lotID, null, "Process Lot", new ObjectIdentifier("P1"),  new ObjectIdentifier("P1")));
        return this.startReservation(equipmentID, StartReservationForInternalBufferInfos);
    }


    public Response startReservation_ReserveDuplicated() {
        String bankID = "BNK-0S";
        String sourceProductID = "RAW-2000.01";
        String productID = "PRODUCT0.01";
        String operationNumber = "2000.0200";
        TestInfos.StbInfo stbInfo = new TestInfos.StbInfo(bankID, sourceProductID, productID, 25L, true);
        ObjectIdentifier lotID = testUtils.stbAndSkip(stbInfo, operationNumber);

        ObjectIdentifier equipmentID = new ObjectIdentifier("1FHI01");
        List<TestInfos.StartReservationForInternalBufferInfo> StartReservationForInternalBufferInfos = new ArrayList<>();
        StartReservationForInternalBufferInfos.add(new TestInfos.StartReservationForInternalBufferInfo(lotID, null, "Process Lot", new ObjectIdentifier("P1"),  new ObjectIdentifier("P1")));
        this.startReservation(equipmentID, StartReservationForInternalBufferInfos);
        Response response = null;
        try {
            // duplicate reservation
            response = this.startReservation(equipmentID, StartReservationForInternalBufferInfos);
        }catch (ServiceException ex) {
            Validations.check(!Validations.isEquals(ex.getCode(), retCodeConfig.getCassetteControlJobFilled()), new OmCode(ex.getCode(), ex.getMessage()));
        }
        return response;
    }

    public Response startReservation_PortStatusAbnormal() {
        String bankID = "BNK-0S";
        String sourceProductID = "RAW-2000.01";
        String productID = "PRODUCT0.01";
        String operationNumber = "2000.0200";

        //[step1]stb one lot and skip to the 2000.0200
        TestInfos.StbInfo stbInfo = new TestInfos.StbInfo(bankID, sourceProductID, productID, 25L, true);
        ObjectIdentifier lotID = testUtils.stbAndSkip(stbInfo, operationNumber);

        ObjectIdentifier equipmentID = new ObjectIdentifier("1FHI01");
        List<TestInfos.StartReservationForInternalBufferInfo> StartReservationForInternalBufferInfos = new ArrayList<>();
        StartReservationForInternalBufferInfos.add(new TestInfos.StartReservationForInternalBufferInfo(lotID, null, "Process Lot", new ObjectIdentifier("P1"),  new ObjectIdentifier("P1")));

        //[step2]change the port status except for LoadReq and LoadAAvail
        //[step2-1]change the port mode to Auto-1 first
        Params.EqpModeChangeReqPrams params = new Params.EqpModeChangeReqPrams();
        params.setUser(testUtils.getUser());
        params.setEquipmentID(equipmentID);
        params.setNotifyToEqpFlag(false);
        params.setNotifyToEAPFlag(false);
        List<Infos.PortOperationMode> strPortOperationMode = new ArrayList<>();
        Infos.PortOperationMode P1 = new Infos.PortOperationMode();
        Infos.PortOperationMode P2 = new Infos.PortOperationMode();
        P1.setPortID(new ObjectIdentifier("P1", "OMPORT.51659697221178515"));
        P1.setPortGroup("P1");
        P1.setPortUsage("INPUT_OUTPUT");
        Infos.OperationMode operationMode1 = new Infos.OperationMode();
        operationMode1.setAccessMode("Manual");
        operationMode1.setDescription("Auto-1");
        operationMode1.setDispatchMode("Manual");
        operationMode1.setOnlineMode("On-Line Remote");
        operationMode1.setMoveOutMode("Auto");
        operationMode1.setMoveInMode("Auto");
        operationMode1.setOperationMode(new ObjectIdentifier("Auto-1"));
        P1.setOperationMode(operationMode1);
        strPortOperationMode.add(P1);

        P2.setPortID(new ObjectIdentifier("P2", "OMPORT.53631185115417292"));
        P2.setPortGroup("P2");
        P2.setPortUsage("INPUT_OUTPUT");
        Infos.OperationMode operationMode2 = new Infos.OperationMode();
        operationMode2.setAccessMode("Manual");
        operationMode2.setDescription("Auto-1");
        operationMode2.setDispatchMode("Manual");
        operationMode2.setOnlineMode("On-Line Remote");
        operationMode2.setMoveOutMode("Auto");
        operationMode2.setMoveInMode("Auto");
        operationMode2.setOperationMode(new ObjectIdentifier("Auto-1"));
        P2.setOperationMode(operationMode2);

        strPortOperationMode.add(P2);
        params.setPortOperationModeList(strPortOperationMode);
        equipmentController.EqpModeChangeReq(params);

        //[step2-2]change the port status
        Params.PortStatusChangeRptParam portStatusChangeRptParam = new Params.PortStatusChangeRptParam();
        portStatusChangeRptParam.setUser(testUtils.getUser());
        portStatusChangeRptParam.setEquipmentID(equipmentID);
        List<Infos.EqpPortEventOnTCS> eqpPortEventOnTCSes = new ArrayList<>();
        Infos.EqpPortEventOnTCS eqpPortEventOnTCS = new Infos.EqpPortEventOnTCS();
        eqpPortEventOnTCS.setPortID(new ObjectIdentifier("P1"));
        eqpPortEventOnTCS.setPortStatus("LoadComp");
        eqpPortEventOnTCSes.add(eqpPortEventOnTCS);
        portStatusChangeRptParam.setEqpPortEventOnEAPesList(eqpPortEventOnTCSes);
        equipmentController.portStatusChangeRpt(portStatusChangeRptParam);

        //[step3]start reservation
        return this.startReservation(equipmentID, StartReservationForInternalBufferInfos);
    }

    public Response startReservation_LotOnHold() {
        String bankID = "BNK-0S";
        String sourceProductID = "RAW-2000.01";
        String productID = "PRODUCT0.01";
        String operationNumber = "2000.0100";

        //【step1】stb one lot, and skip to 2000.0100
        TestInfos.StbInfo stbInfo = new TestInfos.StbInfo(bankID, sourceProductID, productID, 25L, false);
        ObjectIdentifier lotID = testUtils.stbAndSkip(stbInfo, operationNumber);

        //【step2】hold lot
        String reasonCode = "SOHL";
        String reasonableOperation = "C";
        TestInfos.LotHoldInfo lotHoldInfo = new TestInfos.LotHoldInfo(lotID, reasonCode, reasonableOperation);
        lotHoldCase.lotHold(lotHoldInfo);

        //【step3】start reservation
        ObjectIdentifier equipmentID = new ObjectIdentifier("1WSF01");
        List<TestInfos.StartReservationForInternalBufferInfo> StartReservationForInternalBufferInfos = new ArrayList<>();
        StartReservationForInternalBufferInfos.add(new TestInfos.StartReservationForInternalBufferInfo(lotID, null, "Process Lot", new ObjectIdentifier("P1"),  new ObjectIdentifier("P1")));

        // the response will be return null, because of what's next won't shown the hold lot.
        Response response = this.startReservation(equipmentID, StartReservationForInternalBufferInfos);
        Validations.check(null != response, "run startReservation_LotOnHold() fail!");
        return response;
    }

    public Response startReservation_HasProcessHoldRecord() {
        return null;
    }

    public Response startReservation_NotWaiting() {
        String bankID = "BNK-0S";
        String sourceProductID = "RAW-2000.01";
        String productID = "PRODUCT0.01";
        String operationNumber = "2000.0100";

        //【step1】stb one lot, and skip to 2000.0100
        TestInfos.StbInfo stbInfo = new TestInfos.StbInfo(bankID, sourceProductID, productID, 25L, false);
        ObjectIdentifier lotID = testUtils.stbAndSkip(stbInfo, operationNumber);

        //【step2】make the lot is not waiting
        moveInCase.moveIn_GenerateControlJob_Without_StartReservation(lotID, new ObjectIdentifier("WB201"));

        //【step3】start reservation
        ObjectIdentifier equipmentID = new ObjectIdentifier("1WSF01");
        List<TestInfos.StartReservationForInternalBufferInfo> StartReservationForInternalBufferInfos = new ArrayList<>();
        StartReservationForInternalBufferInfos.add(new TestInfos.StartReservationForInternalBufferInfo(lotID, null, "Process Lot", new ObjectIdentifier("P1"),  new ObjectIdentifier("P1")));

        // the response will be return null, because of what's next won't shown the hold lot.
        Response response = this.startReservation(equipmentID, StartReservationForInternalBufferInfos);
        Validations.check(null != response, "run startReservation_NotWaiting() fail!");
        return response;
    }

    public Response startReservation_LotVentoryState_InBank () {
        String bankID = "BNK-0S";
        String subLotType = "Vendor";
        String productID = "RAW-2000.01";
        String lotID = GenerateVendorlot.getVendorLot();;
        //【step1】make one vendor lot (vendor lot inventory state is inBank)
        TestInfos.VendorLotReceiveInfo vendorLotReceiveInfo = new TestInfos.VendorLotReceiveInfo(bankID, subLotType, lotID, productID, 1000);
        vendorLotReceiveCase.VendorLotReceive(vendorLotReceiveInfo);

        //【step2】start reservation
        ObjectIdentifier equipmentID = new ObjectIdentifier("1WSF01");
        List<TestInfos.StartReservationForInternalBufferInfo> StartReservationForInternalBufferInfos = new ArrayList<>();
        StartReservationForInternalBufferInfos.add(new TestInfos.StartReservationForInternalBufferInfo(new ObjectIdentifier(lotID), null, "Process Lot", new ObjectIdentifier("P1"),  new ObjectIdentifier("P1")));

        // the response will be return null, because of what's next won't shown the hold lot.
        Response response = this.startReservation(equipmentID, StartReservationForInternalBufferInfos);
        Validations.check(null != response, "startReservation_LotVentoryState_InBank() fail!");
        return response;
    }

    public Response startReservation_NotMatchWaferSize () {
        String bankID = "BNK-0S";
        String sourceProductID = "RAW-2000.01";
        String productID = "PRODUCT0.01";
        String operationNumber = "2000.0200";

        //【step1】stb one lot, and skip to 2000.0200
        TestInfos.StbInfo stbInfo = new TestInfos.StbInfo(bankID, sourceProductID, productID, 15L, false);
        ObjectIdentifier lotID = testUtils.stbAndSkip(stbInfo, operationNumber);

        //【step2】start reservation
        // 1FHI01 Min Wafers is 25
        ObjectIdentifier equipmentID = new ObjectIdentifier("1FHI01");
        List<TestInfos.StartReservationForInternalBufferInfo> StartReservationForInternalBufferInfos = new ArrayList<>();
        StartReservationForInternalBufferInfos.add(new TestInfos.StartReservationForInternalBufferInfo(lotID, null, "Process Lot", new ObjectIdentifier("P1"),  new ObjectIdentifier("P1")));

        // the response will be return null, because of what's next won't shown the hold lot.
        Response response = null;
        try {
            response = this.startReservation(equipmentID, StartReservationForInternalBufferInfos);
        }catch (ServiceException e) {
            Validations.check(!Validations.isEquals(e.getCode(), retCodeConfig.getInvalidInputWaferCount()), e.getMessage());
        }
        return response;
    }

    public Response startReservation_MonitorCreationFlagTrue_WithEmptyCassette () {
        String bankID = "BNK-0S";
        String sourceProductID = "RAW-2000.01";
        String productID = "PRODUCT0.01";
        String operationNumber = "2000.0200";

        //【step1】stb one lot, and skip to 2000.0200
        TestInfos.StbInfo stbInfo = new TestInfos.StbInfo(bankID, sourceProductID, productID, 25L, false);
        ObjectIdentifier lotID = testUtils.stbAndSkip(stbInfo, operationNumber);

        //[step2]find one empty cassette
        String cassetteCategory = "FOUP";
        String cassetteStatus = "AVAILABLE";
        ObjectIdentifier emptyCassette = testUtils.findEmptyCassette(cassetteCategory, cassetteStatus);

        //【step3】start reservation
        ObjectIdentifier equipmentID = new ObjectIdentifier("1FHI04");
        List<TestInfos.StartReservationForInternalBufferInfo> StartReservationForInternalBufferInfos = new ArrayList<>();
        StartReservationForInternalBufferInfos.add(new TestInfos.StartReservationForInternalBufferInfo(lotID, null, "Process Lot", new ObjectIdentifier("P1"),  new ObjectIdentifier("P1")));
        StartReservationForInternalBufferInfos.add(new TestInfos.StartReservationForInternalBufferInfo(null, emptyCassette, "Empty Cassette", new ObjectIdentifier("P2"),  new ObjectIdentifier("P2")));
        return this.startReservation(equipmentID, StartReservationForInternalBufferInfos);
    }


    public Response startReservation_CarrierExchangeFlagTrue_TwoLotOneEmptyCassette () {
        String bankID = "BNK-0S";
        String sourceProductID = "RAW-2000.01";
        String productID = "PRODUCT0.01";
        String operationNumber = "2000.0200";

        //【step1】stb one lot, and skip to 2000.0200
        TestInfos.StbInfo stbInfo = new TestInfos.StbInfo(bankID, sourceProductID, productID, 25L, false);
        ObjectIdentifier lotID1 = testUtils.stbAndSkip(stbInfo, operationNumber);
        ObjectIdentifier lotID2 = testUtils.stbAndSkip(stbInfo, operationNumber);
        //[step2]find one empty cassette
        String cassetteCategory = "FOUP";
        String cassetteStatus = "AVAILABLE";
        ObjectIdentifier emptyCassette = testUtils.findEmptyCassette(cassetteCategory, cassetteStatus);

        //【step3】start reservation
        ObjectIdentifier equipmentID = new ObjectIdentifier("1FHI04");
        List<TestInfos.StartReservationForInternalBufferInfo> StartReservationForInternalBufferInfos = new ArrayList<>();
        StartReservationForInternalBufferInfos.add(new TestInfos.StartReservationForInternalBufferInfo(lotID1, null, "Process Lot", new ObjectIdentifier("P1"),  new ObjectIdentifier("P1")));
        StartReservationForInternalBufferInfos.add(new TestInfos.StartReservationForInternalBufferInfo(null, emptyCassette, "Empty Cassette", new ObjectIdentifier("P2"),  new ObjectIdentifier("P2")));
        StartReservationForInternalBufferInfos.add(new TestInfos.StartReservationForInternalBufferInfo(lotID2, null, "Process Lot", new ObjectIdentifier("P1"),  new ObjectIdentifier("P3")));

        try {
            this.startReservation(equipmentID, StartReservationForInternalBufferInfos);
        }catch (ServiceException e) {
            Validations.check(!Validations.isEquals(e.getCode(), retCodeConfig.getInvalidEmptyCount()), e.getMessage());
        }
        return null;
    }

    public Response startReservation_MonitorCreationFlagTrue_WithOutEmptyCassette () {
        String bankID = "BNK-0S";
        String sourceProductID = "RAW-2000.01";
        String productID = "PRODUCT0.01";
        String operationNumber = "2000.0200";

        //【step1】stb one lot, and skip to 2000.0200
        TestInfos.StbInfo stbInfo = new TestInfos.StbInfo(bankID, sourceProductID, productID, 25L, false);
        ObjectIdentifier lotID = testUtils.stbAndSkip(stbInfo, operationNumber);

        //【step3】start reservation
        ObjectIdentifier equipmentID = new ObjectIdentifier("1FHI04");
        List<TestInfos.StartReservationForInternalBufferInfo> StartReservationForInternalBufferInfos = new ArrayList<>();
        StartReservationForInternalBufferInfos.add(new TestInfos.StartReservationForInternalBufferInfo(lotID, null, "Process Lot", new ObjectIdentifier("P1"),  new ObjectIdentifier("P1")));
        Response response = null;
        try {
            response = this.startReservation(equipmentID, StartReservationForInternalBufferInfos);
        }catch (ServiceException e) {
            Validations.check(!Validations.isEquals(e.getCode(), retCodeConfig.getInvalidEmptyCount()), e.getMessage());
        }

        return response;
    }


    public Response startReservation_WithScrappedWafers () {
        String bankID = "BNK-0S";
        String sourceProductID = "RAW-2000.01";
        String productID = "PRODUCT0.01";
        String operationNumber = "2000.0100";

        //【step1】stb one lot, and skip to 2000.0100
        TestInfos.StbInfo stbInfo = new TestInfos.StbInfo(bankID, sourceProductID, productID, 25L, false);
        ObjectIdentifier lotID = testUtils.stbAndSkip(stbInfo, operationNumber);

        //【step2】scrap some wafer
        ObjectIdentifier reasonRouteID = new ObjectIdentifier("LAYER0MA.01");
        ObjectIdentifier reasonCodeID = new ObjectIdentifier("MISC");
        String reasonOperationNumber = operationNumber;
        int scrapWaferCount = 2;
        TestInfos.ScrapInfo scrapInfo = new TestInfos.ScrapInfo(lotID, reasonRouteID, reasonCodeID, reasonOperationNumber, scrapWaferCount);
        scrapCase.scrap(scrapInfo);

        //【step3】start reservation
        ObjectIdentifier equipmentID = new ObjectIdentifier("1WSF01");
        List<TestInfos.StartReservationForInternalBufferInfo> StartReservationForInternalBufferInfos = new ArrayList<>();
        StartReservationForInternalBufferInfos.add(new TestInfos.StartReservationForInternalBufferInfo(lotID, null, "Process Lot", new ObjectIdentifier("P1"),  new ObjectIdentifier("P1")));
        Response response = null;
        try {
            response = this.startReservation(equipmentID, StartReservationForInternalBufferInfos);
        }catch (ServiceException e) {
            Validations.check(!Validations.isEquals(e.getCode(), retCodeConfig.getFoundScrap()), e.getMessage());
        }

        return response;
    }
}