package com.fa.cim.newIntegration.tcase;

import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.Response;
import com.fa.cim.common.support.User;
import com.fa.cim.common.utils.Validations;
import com.fa.cim.controller.dispatch.DispatchInqController;
import com.fa.cim.controller.einfo.ElectronicInformationInqController;
import com.fa.cim.controller.equipment.EquipmentController;
import com.fa.cim.controller.interfaces.equipment.IEquipmentInqController;
import com.fa.cim.controller.interfaces.lot.ILotInqController;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.jpa.SearchCondition;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/9/9       ********              Jerry             create file
 *
 * @author: Jerry
 * @date: 2019/9/9 16:10
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */

@Service
@Slf4j
public class EqpInfoForInternalBufferTestCase {


    @Autowired
    private EquipmentController equipmentController;
    @Autowired
    private IEquipmentInqController equipmentInqController;
    @Autowired
    private ILotInqController lotInqController;
    @Autowired
    private ElectronicInformationInqController electronicInformationInqController;

    @Autowired
    private DispatchInqController dispatchInqController;

    private User getUser() {
        User user = new User();
        user.setUserID(new ObjectIdentifier("ADMIN"));
        user.setPassword("b51fa595e692d53739b69131cdc73440");
        return user;
    }

    public Response WhatNextForInternalBufferCase(String eqpID) {
        Params.WhatNextLotListParams whatNextLotListParams = new Params.WhatNextLotListParams();
        whatNextLotListParams.setUser(getUser());
        whatNextLotListParams.setEquipmentID(new ObjectIdentifier(eqpID));
        SearchCondition searchCondition = new SearchCondition();
        searchCondition.setPage(1);
        searchCondition.setSize(10);
        whatNextLotListParams.setSearchCondition(searchCondition);
        Response response = dispatchInqController.whatNextInq(whatNextLotListParams);
        Assert.isTrue(Validations.isSuccess(response.getCode()),"What's Next Error");
        return response;
    }


    public Response lotForStartReservtionForInternalBufferCase(String eqpID, List<Infos.StartCassette> startCassettes) {
        Params.LotsMoveInReserveInfoForIBInqParams startReservationForInternalBufferInqParams = new Params.LotsMoveInReserveInfoForIBInqParams();
        startReservationForInternalBufferInqParams.setUser(getUser());
        startReservationForInternalBufferInqParams.setEquipmentID(new ObjectIdentifier(eqpID));
        startReservationForInternalBufferInqParams.setStartCassettes(startCassettes);
        Response response = dispatchInqController.lotsMoveInReserveInfoForIBInq(startReservationForInternalBufferInqParams);
        Assert.isTrue(Validations.isSuccess(response.getCode()),"lot for startReservtion Error");
        return response;
    }

    public Response startReservtionForInternalBufferCase(String eqpID, List<Infos.StartCassette> startCassettes) {
        Params.LotsMoveInReserveInfoForIBInqParams startReservationForInternalBufferInqParams = new Params.LotsMoveInReserveInfoForIBInqParams();
        startReservationForInternalBufferInqParams.setUser(getUser());
        startReservationForInternalBufferInqParams.setEquipmentID(new ObjectIdentifier(eqpID));
        startReservationForInternalBufferInqParams.setStartCassettes(startCassettes);
        Response response = dispatchInqController.lotsMoveInReserveInfoForIBInq(startReservationForInternalBufferInqParams);
        Assert.isTrue(Validations.isSuccess(response.getCode()),"startReservtion Error");
        return response;
    }

    public Response loadingForInternalBufferCase(String eqpID, ObjectIdentifier cassette, String portID) {
        Params.CarrierLoadingForIBRptParams carrierLoadingForIBRptParams = new Params.CarrierLoadingForIBRptParams();
        carrierLoadingForIBRptParams.setUser(getUser());
        carrierLoadingForIBRptParams.setEquipmentID(new ObjectIdentifier(eqpID));
        carrierLoadingForIBRptParams.setPortID(new ObjectIdentifier(portID));
        carrierLoadingForIBRptParams.setCassetteID(cassette);
        carrierLoadingForIBRptParams.setLoadPurposeType(BizConstant.SP_LOADPURPOSETYPE_PROCESSLOT);
        Response response = equipmentController.carrierLoadingForIBRpt(carrierLoadingForIBRptParams);
        Assert.isTrue(Validations.isSuccess(response.getCode()),"loading Error : " + response.getCode());
        return response;
    }

    public Response moveToSelfForInternalBufferCase(String eqpID, ObjectIdentifier cassette, String portID) {
        Params.CarrierMoveFromIBRptParams carrierMoveFromIBRptParams = new Params.CarrierMoveFromIBRptParams();
        carrierMoveFromIBRptParams.setUser(getUser());
        carrierMoveFromIBRptParams.setEquipmentID(new ObjectIdentifier(eqpID));
        carrierMoveFromIBRptParams.setDestinationPortID(new ObjectIdentifier(portID));
        carrierMoveFromIBRptParams.setCarrierID(cassette);
        Response response = equipmentController.carrierMoveFromIBRpt(carrierMoveFromIBRptParams);
        Assert.isTrue(Validations.isSuccess(response.getCode()),"move to self Error");
        return response;
    }


    public Response opeStartForInternalBufferCase(String eqpID, List<Infos.StartCassette> startCassettes, String controlJobID, Boolean processJobPauseFlag) {
        Params.MoveInForIBReqParams moveInForIBReqParams = new Params.MoveInForIBReqParams();
        moveInForIBReqParams.setUser(getUser());
        moveInForIBReqParams.setEquipmentID(new ObjectIdentifier(eqpID));
        moveInForIBReqParams.setControlJobID(new ObjectIdentifier(controlJobID));
        moveInForIBReqParams.setStartCassetteList(startCassettes);
        moveInForIBReqParams.setProcessJobPauseFlag(processJobPauseFlag);
        Response response = equipmentController.moveInForIBReq(moveInForIBReqParams);
        Assert.isTrue(Validations.isSuccess(response.getCode()),"opestart Error");
        return response;
    }

    public Response opeCompForInternalBufferCase(String eqpID, String controlJobID) {
        Params.MoveOutForIBReqParams moveOutForIBReqParams = new Params.MoveOutForIBReqParams();
        moveOutForIBReqParams.setUser(getUser());
        moveOutForIBReqParams.setEquipmentID(new ObjectIdentifier(eqpID));
        moveOutForIBReqParams.setControlJobID(new ObjectIdentifier(controlJobID));
        Response response = equipmentController.moveOutForIBReq(moveOutForIBReqParams);
        Assert.isTrue(Validations.isSuccess(response.getCode()),"opecomp Error");
        return response;
    }

    public Response unLoadingForInternalBufferCase(String eqpID, ObjectIdentifier cassette, String portID) {
        Params.CarrierUnloadingForIBRptParams uncarrierLoadingForIBRptParams = new Params.CarrierUnloadingForIBRptParams();
        uncarrierLoadingForIBRptParams.setUser(getUser());
        uncarrierLoadingForIBRptParams.setEquipmentID(new ObjectIdentifier(eqpID));
        uncarrierLoadingForIBRptParams.setCassetteID(cassette);
        uncarrierLoadingForIBRptParams.setPortID(new ObjectIdentifier(portID));
        Response response = equipmentController.uncarrierLoadingForIBRpt(uncarrierLoadingForIBRptParams);
        Assert.isTrue(Validations.isSuccess(response.getCode()),"unloading Error");
        return response;
    }

    public Response LotListByCarrierInqCase( ObjectIdentifier cassette) {
        Params.LotListByCarrierInqParams lotListByCarrierInqParams = new Params.LotListByCarrierInqParams();
        lotListByCarrierInqParams.setUser(getUser());
        lotListByCarrierInqParams.setCassetteID(cassette);
        Response response = lotInqController.lotListByCarrierInq(lotListByCarrierInqParams);
        Assert.isTrue(Validations.isSuccess(response.getCode()),"LotListByCarrierInq Error");
        return response;
    }

    public Response LotInfoInqCase(List<ObjectIdentifier> lotIDs) {
        Params.LotInfoInqParams lotInfoInqParams = new Params.LotInfoInqParams();
        lotInfoInqParams.setUser(getUser());
        lotInfoInqParams.setLotIDs(lotIDs);
        Infos.LotInfoInqFlag lotInfoInqFlag = new Infos.LotInfoInqFlag();
        lotInfoInqFlag.setLotBasicInfoFlag(true);
        lotInfoInqFlag.setLotControlUseInfoFlag(true);
        lotInfoInqFlag.setLotFlowBatchInfoFlag(true);
        lotInfoInqFlag.setLotNoteFlagInfoFlag(true);
        lotInfoInqFlag.setLotOperationInfoFlag(true);
        lotInfoInqFlag.setLotOrderInfoFlag(true);
        lotInfoInqFlag.setLotControlJobInfoFlag(true);
        lotInfoInqFlag.setLotProductInfoFlag(true);
        lotInfoInqFlag.setLotRecipeInfoFlag(true);
        lotInfoInqFlag.setLotLocationInfoFlag(true);
        lotInfoInqFlag.setLotWipOperationInfoFlag(true);
        lotInfoInqFlag.setLotWaferAttributesFlag(true);
        lotInfoInqFlag.setLotBackupInfoFlag(true);
        lotInfoInqFlag.setLotListInCassetteInfoFlag(true);
        lotInfoInqFlag.setLotWaferMapInCassetteInfoFlag(true);
        lotInfoInqParams.setLotInfoInqFlag(lotInfoInqFlag);
        Response response = lotInqController.lotInfoInq(lotInfoInqParams);
        Assert.isTrue(Validations.isSuccess(response.getCode()),"lot info Error");
        return response;
    }

}