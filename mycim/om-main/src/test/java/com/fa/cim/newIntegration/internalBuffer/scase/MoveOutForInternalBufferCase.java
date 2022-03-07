package com.fa.cim.newIntegration.internalBuffer.scase;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.Response;
import com.fa.cim.common.utils.CimArrayUtils;
import com.fa.cim.common.utils.Validations;
import com.fa.cim.controller.einfo.ElectronicInformationInqController;
import com.fa.cim.controller.equipment.EquipmentController;
import com.fa.cim.controller.interfaces.sort.ISortController;
import com.fa.cim.controller.lot.LotController;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;
import com.fa.cim.newIntegration.common.TestUtils;
import com.fa.cim.newIntegration.dto.TestInfos;
import com.fa.cim.newIntegration.tcase.ElectronicInformationTestCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/12/6        ********             Bear               create file
 *
 * @author: Bear
 * @date: 2019/12/6 17:37
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Service
public class MoveOutForInternalBufferCase {
    @Autowired
    private TestUtils testUtils;

    @Autowired
    private EquipmentController equipmentController;

    @Autowired
    ElectronicInformationInqController electronicInformationInqController;

    @Autowired
    private LoadForInternalBufferCase loadForInternalBufferCase;

    @Autowired
    private MoveToSelfCase moveToSelfCase;

    @Autowired
    private MoveInForInternalBufferCase moveInForInternalBufferCase;

    @Autowired
    private StartReservationForInternalBufferCase startReservationForInternalBufferCase;

    @Autowired
    private ElectronicInformationTestCase electronicInformationTestCase;

    @Autowired
    private LotController lotController;

    @Autowired
    private ISortController sortController;

    public Response moveOut(Params.MoveOutForIBReqParams params) {
        return equipmentController.moveOutForIBReq(params);
    }

    public Response moveOut_WithStartReservation() {
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
        loadForInternalBufferCase.load(loadForInternalBufferInfo);

        //【step4】move to self
        TestInfos.MoveToSelfInfo moveToSelfInfo = new TestInfos.MoveToSelfInfo(cassetteID, new ObjectIdentifier("P1"), equipmentID);
        moveToSelfCase.movetoSelf(moveToSelfInfo);

        //【step5】move in
        TestInfos.MoveInForInternalBufferInfo moveInForInternalBufferInfo = new TestInfos.MoveInForInternalBufferInfo();
        moveInForInternalBufferInfo.setEquipmentID(equipmentID);
        moveInForInternalBufferInfo.setProcessJobPauseFlag(false); // when not do start reserve, it's false
        List<ObjectIdentifier> cassetteIDList = new ArrayList<>();
        cassetteIDList.add(cassetteID);
        moveInForInternalBufferInfo.setCassetteIDList(cassetteIDList);
        moveInForInternalBufferCase.moveIn(moveInForInternalBufferInfo);

        //【step6】check the lot info screem have control job or not.
        ObjectIdentifier controlJobID = testUtils.getControlJobIDByLotID(lotID);
        Validations.check(null == controlJobID, "null == controlJobID");

        //【step7】move out
        Params.MoveOutForIBReqParams params = new Params.MoveOutForIBReqParams();
        params.setUser(testUtils.getUser());
        params.setEquipmentID(equipmentID);
        params.setControlJobID(controlJobID);
        params.setSpcResultRequiredFlag(false);
        Response response = this.moveOut(params);
        return response;
    }


    public Response moveOut_WithOutStartReservation() {
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
        loadForInternalBufferInfo.setNeedStartReserved(false);
        loadForInternalBufferCase.load(loadForInternalBufferInfo);

        //【step4】move to self
        TestInfos.MoveToSelfInfo moveToSelfInfo = new TestInfos.MoveToSelfInfo(cassetteID, new ObjectIdentifier("P1"), equipmentID);
        moveToSelfCase.movetoSelf(moveToSelfInfo);

        //【step5】move in
        TestInfos.MoveInForInternalBufferInfo moveInForInternalBufferInfo = new TestInfos.MoveInForInternalBufferInfo();
        moveInForInternalBufferInfo.setEquipmentID(equipmentID);
        moveInForInternalBufferInfo.setProcessJobPauseFlag(false); // when not do start reserve, it's false
        List<ObjectIdentifier> cassetteIDList = new ArrayList<>();
        cassetteIDList.add(cassetteID);
        moveInForInternalBufferInfo.setCassetteIDList(cassetteIDList);
        moveInForInternalBufferCase.moveIn(moveInForInternalBufferInfo);

        //【step6】check the lot info screem have control job or not.
        ObjectIdentifier controlJobID = testUtils.getControlJobIDByLotID(lotID);
        Validations.check(null == controlJobID, "null == controlJobID");

        //【step7】move out
        Params.MoveOutForIBReqParams params = new Params.MoveOutForIBReqParams();
        params.setUser(testUtils.getUser());
        params.setEquipmentID(equipmentID);
        params.setControlJobID(controlJobID);
        params.setSpcResultRequiredFlag(false);
        Response response = this.moveOut(params);
        return response;
    }

    public Response moveOut_WithCarrierExchange() {
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

        //【step3】find one empty cassette
        String cassetteCategory = "FOUP";
        String cassetteStatus = "AVAILABLE";
        ObjectIdentifier emptyCassette = testUtils.findEmptyCassette(cassetteCategory, cassetteStatus);

        List<TestInfos.StartReservationForInternalBufferInfo> StartReservationForInternalBufferInfos = new ArrayList<>();
        StartReservationForInternalBufferInfos.add(new TestInfos.StartReservationForInternalBufferInfo(lotID, null, "Process Lot", new ObjectIdentifier("P1"),  new ObjectIdentifier("P1")));
        StartReservationForInternalBufferInfos.add(new TestInfos.StartReservationForInternalBufferInfo(null, emptyCassette, "Empty Cassette", new ObjectIdentifier("P2"),  new ObjectIdentifier("P2")));
        startReservationForInternalBufferCase.startReservation(equipmentID, StartReservationForInternalBufferInfos);

        //【step4】loading
        TestInfos.LoadForInternalBufferInfo loadForInternalBufferInfo = new TestInfos.LoadForInternalBufferInfo(
                lotID, cassetteID,"Process Lot",  equipmentID, new ObjectIdentifier("P1"), false);
        loadForInternalBufferCase.load(loadForInternalBufferInfo);

        TestInfos.LoadForInternalBufferInfo loadForInternalBufferInfo2 = new TestInfos.LoadForInternalBufferInfo(
                null, emptyCassette,"Empty Cassette",  equipmentID, new ObjectIdentifier("P2"), false);
        loadForInternalBufferCase.load(loadForInternalBufferInfo2);


        //【step5】move to self
        TestInfos.MoveToSelfInfo moveToSelfInfo = new TestInfos.MoveToSelfInfo(cassetteID, new ObjectIdentifier("P1"), equipmentID);
        moveToSelfCase.movetoSelf(moveToSelfInfo);

        TestInfos.MoveToSelfInfo moveToSelfInfo2 = new TestInfos.MoveToSelfInfo(emptyCassette, new ObjectIdentifier("P2"), equipmentID);
        moveToSelfCase.movetoSelf(moveToSelfInfo2);

        //【step6】move in
        TestInfos.MoveInForInternalBufferInfo moveInForInternalBufferInfo = new TestInfos.MoveInForInternalBufferInfo();
        moveInForInternalBufferInfo.setEquipmentID(equipmentID);
        moveInForInternalBufferInfo.setProcessJobPauseFlag(false); // default it's false
        List<ObjectIdentifier> cassetteIDList = new ArrayList<>();
        cassetteIDList.add(cassetteID);
        cassetteIDList.add(emptyCassette);
        moveInForInternalBufferInfo.setCassetteIDList(cassetteIDList);
        moveInForInternalBufferCase.moveIn(moveInForInternalBufferInfo);

        //【step7】check the lot info screem have control job or not.
        ObjectIdentifier controlJobID = testUtils.getControlJobIDByLotID(lotID);
        Validations.check(null == controlJobID, "null == controlJobID");

        //【step8】call einfo/lot_list_in_cassette/inq
        Results.LotListByCarrierInqResult lotListByCarrierInqResult = (Results.LotListByCarrierInqResult) electronicInformationTestCase.lotListByCarrierInqCase(cassetteID).getBody();

        //【step9】carrier exchange
        List<Infos.WaferTransfer> waferXferList = new ArrayList<>();
        for (Infos.WaferMapInCassetteInfo waferMapInCassetteInfo : lotListByCarrierInqResult.getWaferMapInCassetteInfoList()) {
            Infos.WaferTransfer waferTransfer = new Infos.WaferTransfer();
            waferTransfer.setBDestinationCassetteManagedByOM(true);
            waferTransfer.setBOriginalCassetteManagedByOM(true);
            waferTransfer.setDestinationCassetteID(emptyCassette);
            waferTransfer.setOriginalCassetteID(cassetteID);
            waferTransfer.setOriginalSlotNumber(waferMapInCassetteInfo.getSlotNumber());
            waferTransfer.setDestinationSlotNumber(waferMapInCassetteInfo.getSlotNumber());
            waferTransfer.setWaferID(waferMapInCassetteInfo.getWaferID());
            waferXferList.add(waferTransfer);
        }

        //【step9-1】call cassette exchange
        Params.CarrierExchangeReqParams carrierExchangeReqParams = new Params.CarrierExchangeReqParams();
        carrierExchangeReqParams.setUser(testUtils.getUser());
        carrierExchangeReqParams.setEquipmentID(equipmentID);
        carrierExchangeReqParams.setWaferXferList(waferXferList);
        sortController.carrierExchangeReq(carrierExchangeReqParams);

        //【step9-2】check the result
        lotListByCarrierInqResult = (Results.LotListByCarrierInqResult) electronicInformationTestCase.lotListByCarrierInqCase(cassetteID).getBody();
        Validations.check(!CimArrayUtils.isEmpty(lotListByCarrierInqResult.getWaferMapInCassetteInfoList()), "cassette is not null!");
        lotListByCarrierInqResult = (Results.LotListByCarrierInqResult) electronicInformationTestCase.lotListByCarrierInqCase(emptyCassette).getBody();
        Validations.check(CimArrayUtils.isEmpty(lotListByCarrierInqResult.getWaferMapInCassetteInfoList()), "cassette is null!");

        //【step10】move out
        Params.MoveOutForIBReqParams params = new Params.MoveOutForIBReqParams();
        params.setUser(testUtils.getUser());
        params.setEquipmentID(equipmentID);
        params.setControlJobID(controlJobID);
        params.setSpcResultRequiredFlag(false);
        Response response = this.moveOut(params);
        return response;
    }
}