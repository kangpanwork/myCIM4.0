package com.fa.cim.newIntegration.internalBuffer.scase;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.Response;
import com.fa.cim.common.utils.Validations;
import com.fa.cim.controller.einfo.ElectronicInformationInqController;
import com.fa.cim.controller.equipment.EquipmentController;
import com.fa.cim.controller.equipment.EquipmentInqController;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;
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
 * 2019/12/6        ********             Bear               create file
 *
 * @author: Bear
 * @date: 2019/12/6 17:07
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@Service
public class MoveInForInternalBufferCase {
    @Autowired
    private TestUtils testUtils;

    @Autowired
    private EquipmentInqController equipmentInqController;

    @Autowired
    private EquipmentController equipmentController;

    @Autowired
    ElectronicInformationInqController electronicInformationInqController;

    @Autowired
    private LoadForInternalBufferCase loadForInternalBufferCase;

    @Autowired
    private MoveToSelfCase moveToSelfCase;

    public Response moveIn(TestInfos.MoveInForInternalBufferInfo moveInForInternalBufferInfo) {

        //【step1】eqp/lot_info_for_ope_start_for_internal_buffer/inq
        Params.LotsMoveInInfoForIBInqParams params = new Params.LotsMoveInInfoForIBInqParams();
        params.setCassetteIDs(moveInForInternalBufferInfo.getCassetteIDList());
        params.setEquipmentID(moveInForInternalBufferInfo.getEquipmentID());
        params.setUser(testUtils.getUser());
        Results.LotsMoveInInfoInqResult body = (Results.LotsMoveInInfoInqResult) equipmentInqController.LotsMoveInInfoForIBInq(params).getBody();


        //【step2】eqp/move_in_for_ib/req
        Params.MoveInForIBReqParams moveInForIBReqParams = new Params.MoveInForIBReqParams();
        moveInForIBReqParams.setUser(testUtils.getUser());
        moveInForIBReqParams.setControlJobID(body.getControlJobID());
        moveInForIBReqParams.setProcessJobPauseFlag(moveInForInternalBufferInfo.isProcessJobPauseFlag());
        moveInForIBReqParams.setStartCassetteList(body.getStartCassetteList());
        moveInForIBReqParams.setEquipmentID(moveInForInternalBufferInfo.getEquipmentID());
        return equipmentController.moveInForIBReq(moveInForIBReqParams);
    }

    public Response moveIn_WithStartReservation() {
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
        Response response = this.moveIn(moveInForInternalBufferInfo);

        //【step6】check the lot info screem have control job or not.
        ObjectIdentifier controlJobIDByLotID = testUtils.getControlJobIDByLotID(lotID);
        Validations.check(null == controlJobIDByLotID, "null == controlJobIDByLotID");
        return response;
    }


    public Response moveIn_WithOutStartReservation() {
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
        Response response = this.moveIn(moveInForInternalBufferInfo);

        //【step6】check the lot info screem have control job or not.
        ObjectIdentifier controlJobIDByLotID = testUtils.getControlJobIDByLotID(lotID);
        Validations.check(null == controlJobIDByLotID, "null == controlJobIDByLotID");
        return response;
    }
}