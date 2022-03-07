package com.fa.cim.newIntegration.internalBuffer.scase;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.Response;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.controller.dispatch.DispatchController;
import com.fa.cim.controller.einfo.ElectronicInformationInqController;
import com.fa.cim.controller.equipment.EquipmentController;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;
import com.fa.cim.newIntegration.common.TestUtils;
import com.fa.cim.newIntegration.dto.TestInfos;
import com.fa.cim.newIntegration.tcase.LotGeneralTestCase;
import lombok.extern.slf4j.Slf4j;
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
 * 2019/12/9        ********             Bear               create file
 *
 * @author: Bear
 * @date: 2019/12/9 14:42
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@Service
public class CarrierOutForInternalBufferCase {
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
    private LoadForInternalBufferCase loadForInternalBufferCase;

    @Autowired
    private MoveToSelfCase moveToSelfCase;

    @Autowired
    private MoveInForInternalBufferCase moveInForInternalBufferCase;

    @Autowired
    private MoveFromSelfCase moveFromSelfCase;

    @Autowired
    private UnloadForInternalBufferCase unloadForInternalBufferCase;

    @Autowired
    private StartReservationCancelForInternalBufferCase startReservationCancelForInternalBufferCase;

    @Autowired
    private DispatchController dispatchController;

    @Autowired
    private LotGeneralTestCase lotGeneralTestCase;

    public Response carrierOut_WithOutMoveIn() {
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

        //【step5】move from self
        Params.CarrierMoveFromIBRptParams carrierMoveFromIBRptParams = new Params.CarrierMoveFromIBRptParams();
        carrierMoveFromIBRptParams.setCarrierID(cassetteID);
        carrierMoveFromIBRptParams.setDestinationPortID(new ObjectIdentifier("P1"));
        carrierMoveFromIBRptParams.setEquipmentID(equipmentID);
        carrierMoveFromIBRptParams.setUser(testUtils.getUser());
        moveFromSelfCase.moveFromSelf(carrierMoveFromIBRptParams);

        //【step6】unloading
        TestInfos.UnloadForInternalBufferInfo unloadForInternalBufferInfo = new TestInfos.UnloadForInternalBufferInfo();
        unloadForInternalBufferInfo.setCassetteID(cassetteID);
        unloadForInternalBufferInfo.setPortID(new ObjectIdentifier("P1"));
        unloadForInternalBufferInfo.setEquipmentID(equipmentID);
        return unloadForInternalBufferCase.unload(unloadForInternalBufferInfo);
    }

    public Response carrierOut_AfterMoveInCancel() {
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
        Response response = moveInForInternalBufferCase.moveIn(moveInForInternalBufferInfo);
        Results.MoveInReqResult body = (Results.MoveInReqResult) response.getBody();
        ObjectIdentifier controlJobID = body.getControlJobID();

        //【step6】move in cancel
        Params.MoveInCancelForIBReqParams params = new Params.MoveInCancelForIBReqParams();
        params.setUser(testUtils.getUser());
        params.setEquipmentID(equipmentID);
        params.setControlJobID(controlJobID);
        equipmentController.moveInCancelForIBReq(params);

        //【step7】move from self
        Params.CarrierMoveFromIBRptParams carrierMoveFromIBRptParams = new Params.CarrierMoveFromIBRptParams();
        carrierMoveFromIBRptParams.setCarrierID(cassetteID);
        carrierMoveFromIBRptParams.setDestinationPortID(new ObjectIdentifier("P1"));
        carrierMoveFromIBRptParams.setEquipmentID(equipmentID);
        carrierMoveFromIBRptParams.setUser(testUtils.getUser());
        moveFromSelfCase.moveFromSelf(carrierMoveFromIBRptParams);

        //【step8】unloading
        TestInfos.UnloadForInternalBufferInfo unloadForInternalBufferInfo = new TestInfos.UnloadForInternalBufferInfo();
        unloadForInternalBufferInfo.setCassetteID(cassetteID);
        unloadForInternalBufferInfo.setPortID(new ObjectIdentifier("P1"));
        unloadForInternalBufferInfo.setEquipmentID(equipmentID);
        return unloadForInternalBufferCase.unload(unloadForInternalBufferInfo);
    }

    public Response carrierOut_AfterStartReservationCancel() {
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

        //【step5】start reservation cancel
        //【step5-1】call lot info to get the control job id
        ObjectIdentifier controlJobID =  testUtils.getControlJobIDByLotID(lotID);

        //【step5-2】call start reservation
        Params.MoveInReserveCancelForIBReqParams moveInReserveCancelForIBReqParams = new Params.MoveInReserveCancelForIBReqParams();
        moveInReserveCancelForIBReqParams.setUser(testUtils.getUser());
        moveInReserveCancelForIBReqParams.setControlJobID(controlJobID);
        moveInReserveCancelForIBReqParams.setEquipmentID(equipmentID);
        dispatchController.moveInReserveCancelForIBReq(moveInReserveCancelForIBReqParams);

        //【step6】move from self
        Params.CarrierMoveFromIBRptParams carrierMoveFromIBRptParams = new Params.CarrierMoveFromIBRptParams();
        carrierMoveFromIBRptParams.setCarrierID(cassetteID);
        carrierMoveFromIBRptParams.setDestinationPortID(new ObjectIdentifier("P1"));
        carrierMoveFromIBRptParams.setEquipmentID(equipmentID);
        carrierMoveFromIBRptParams.setUser(testUtils.getUser());
        moveFromSelfCase.moveFromSelf(carrierMoveFromIBRptParams);

        //【step7】unloading
        TestInfos.UnloadForInternalBufferInfo unloadForInternalBufferInfo = new TestInfos.UnloadForInternalBufferInfo();
        unloadForInternalBufferInfo.setCassetteID(cassetteID);
        unloadForInternalBufferInfo.setPortID(new ObjectIdentifier("P1"));
        unloadForInternalBufferInfo.setEquipmentID(equipmentID);
        return unloadForInternalBufferCase.unload(unloadForInternalBufferInfo);
    }
}