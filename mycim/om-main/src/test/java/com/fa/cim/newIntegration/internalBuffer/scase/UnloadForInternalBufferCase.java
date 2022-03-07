package com.fa.cim.newIntegration.internalBuffer.scase;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.Response;
import com.fa.cim.controller.equipment.EquipmentController;
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
 * @date: 2019/12/6 16:39
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@Service
public class UnloadForInternalBufferCase {
    @Autowired
    private TestUtils testUtils;

    @Autowired
    private LoadForInternalBufferCase loadForInternalBufferCase;

    @Autowired
    private EquipmentController equipmentController;

    @Autowired
    private MoveToSelfCase moveToSelfCase;

    @Autowired
    private MoveInForInternalBufferCase moveInForInternalBufferCase;

    @Autowired
    private MoveOutForInternalBufferCase moveOutForInternalBufferCase;

    @Autowired
    private MoveFromSelfCase moveFromSelfCase;

    public Response unload_BeforeMoveOut () {
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

        //【step4】unloading
        TestInfos.UnloadForInternalBufferInfo unloadForInternalBufferInfo = new TestInfos.UnloadForInternalBufferInfo();
        unloadForInternalBufferInfo.setCassetteID(cassetteID);
        unloadForInternalBufferInfo.setPortID(new ObjectIdentifier("P1"));
        unloadForInternalBufferInfo.setEquipmentID(equipmentID);
        return this.unload(unloadForInternalBufferInfo);
    }

    public Response unload(TestInfos.UnloadForInternalBufferInfo unloadForInternalBufferInfo) {
        Params.CarrierUnloadingForIBRptParams uncarrierLoadingForIBRptParams = new Params.CarrierUnloadingForIBRptParams();
        uncarrierLoadingForIBRptParams.setCassetteID(unloadForInternalBufferInfo.getCassetteID());
        uncarrierLoadingForIBRptParams.setEquipmentID(unloadForInternalBufferInfo.getEquipmentID());
        uncarrierLoadingForIBRptParams.setPortID(unloadForInternalBufferInfo.getPortID());
        uncarrierLoadingForIBRptParams.setUser(testUtils.getUser());
        return equipmentController.uncarrierLoadingForIBRpt(uncarrierLoadingForIBRptParams);
    }
    public Response unload_AfterMoveOut () {
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
        Results.MoveInReqResult body = (Results.MoveInReqResult) response.getBody();
        ObjectIdentifier controlJobID = body.getControlJobID();

        //【step6】move out
        Params.MoveOutForIBReqParams params = new Params.MoveOutForIBReqParams();
        params.setUser(testUtils.getUser());
        params.setEquipmentID(equipmentID);
        params.setControlJobID(controlJobID);
        params.setSpcResultRequiredFlag(false);
        moveOutForInternalBufferCase.moveOut(params);

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
        return this.unload(unloadForInternalBufferInfo);
    }
}