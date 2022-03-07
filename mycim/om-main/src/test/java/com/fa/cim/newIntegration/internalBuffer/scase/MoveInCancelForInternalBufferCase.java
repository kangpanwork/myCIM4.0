package com.fa.cim.newIntegration.internalBuffer.scase;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.Response;
import com.fa.cim.common.utils.CimObjectUtils;
import com.fa.cim.common.utils.Validations;
import com.fa.cim.controller.einfo.ElectronicInformationInqController;
import com.fa.cim.controller.equipment.EquipmentController;
import com.fa.cim.dto.Params;
import com.fa.cim.newIntegration.common.TestUtils;
import com.fa.cim.newIntegration.dto.TestInfos;
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
 * @date: 2019/12/9 15:00
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@Service
public class MoveInCancelForInternalBufferCase {
    @Autowired
    private EquipmentController equipmentController;

    @Autowired
    private TestUtils testUtils;

    @Autowired
    ElectronicInformationInqController electronicInformationInqController;

    @Autowired
    private LoadForInternalBufferCase loadForInternalBufferCase;

    @Autowired
    private MoveToSelfCase moveToSelfCase;

    @Autowired
    private MoveInForInternalBufferCase moveInForInternalBufferCase;

    public Response moveInCancel_WithStartReservation() {
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
        Response response = moveInForInternalBufferCase.moveIn(moveInForInternalBufferInfo);

        //【step6】check the lot info screem have control job or not.
        ObjectIdentifier controlJobIDByLotID = testUtils.getControlJobIDByLotID(lotID);
        Validations.check(CimObjectUtils.isEmptyWithValue(controlJobIDByLotID), "null == controlJobIDByLotID");

        //【step7】move in cancel
        Params.MoveInCancelForIBReqParams params = new Params.MoveInCancelForIBReqParams();
        params.setUser(testUtils.getUser());
        params.setEquipmentID(equipmentID);
        params.setControlJobID(controlJobIDByLotID);
        equipmentController.moveInCancelForIBReq(params);

        //【step8】check the lot info screem have control job or not.
        controlJobIDByLotID = testUtils.getControlJobIDByLotID(lotID);
        Validations.check(!CimObjectUtils.isEmptyWithValue(controlJobIDByLotID), "null != controlJobIDByLotID");
        return response;
    }


    public Response moveInCancel_WithOutStartReservation() {
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

        //【step6】check the lot info screem have control job or not.
        ObjectIdentifier controlJobIDByLotID = testUtils.getControlJobIDByLotID(lotID);
        Validations.check(CimObjectUtils.isEmptyWithValue(controlJobIDByLotID), "null == controlJobIDByLotID");

        //【step7】move in cancel
        Params.MoveInCancelForIBReqParams params = new Params.MoveInCancelForIBReqParams();
        params.setUser(testUtils.getUser());
        params.setEquipmentID(equipmentID);
        params.setControlJobID(controlJobIDByLotID);
        equipmentController.moveInCancelForIBReq(params);

        //【step8】check the lot info screem have control job or not.
        controlJobIDByLotID = testUtils.getControlJobIDByLotID(lotID);
        Validations.check(!CimObjectUtils.isEmptyWithValue(controlJobIDByLotID), "null != controlJobIDByLotID");

        return response;
    }
}