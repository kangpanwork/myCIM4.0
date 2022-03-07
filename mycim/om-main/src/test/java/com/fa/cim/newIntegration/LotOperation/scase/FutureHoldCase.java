package com.fa.cim.newIntegration.LotOperation.scase;

import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.Response;
import com.fa.cim.common.utils.Validations;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;
import com.fa.cim.newIntegration.bank.scase.VendorLotReceiveCase;
import com.fa.cim.newIntegration.stb.scase.STBCase;
import com.fa.cim.newIntegration.tcase.EquipmentTestCase;
import com.fa.cim.newIntegration.tcase.FutureHoldTestCase;
import com.fa.cim.newIntegration.tcase.LotGeneralTestCase;
import com.fa.cim.newIntegration.tcase.OperationSkipTestCase;
import com.fa.cim.newcore.exceptions.DuplicateRecordException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/9/10       ********             lightyh             create file
 *
 * @author: lightyh
 * @date: 2019/9/10 15:01
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Service
@Slf4j
public class FutureHoldCase {
    @Autowired
    private STBCase stbCase;

    @Autowired
    private FutureHoldTestCase futureHoldTestCase;

    @Autowired
    private LotGeneralTestCase lotGeneralTestCase;


    @Autowired
    private OperationSkipTestCase operationSkipTestCase;

    @Autowired
    private RetCodeConfig retCodeConfig;

    @Autowired
    private VendorLotReceiveCase vendorLotReceiveCase;


    @Autowired
    private EquipmentTestCase equipmentTestCase;


    public Response futureHold_Normal(){
        //【step1】stb
        Results.WaferLotStartReqResult waferLotStartReqResult = (Results.WaferLotStartReqResult) stbCase.STB_PreparedLot().getBody();
        //【step2】getOperationNumber and RouteID
        Results.LotOperationSelectionInqResult lotOperationSelectionInqResult = (Results.LotOperationSelectionInqResult) lotGeneralTestCase.getLotOperationSelectionInq(waferLotStartReqResult.getLotID(), true, true, true).getBody();
        Infos.OperationNameAttributes operationNameAttributes = lotOperationSelectionInqResult.getOperationNameAttributesAttributes().getContent().get(0);
        String operationNumber = operationNameAttributes.getOperationNumber();
        ObjectIdentifier routeID = operationNameAttributes.getRouteID();
        return futureHoldTestCase.futureHoldRegisterBySpecLot(waferLotStartReqResult.getLotID(), operationNumber, routeID, true);
    }

    public Response futureHold_With_HoldTimingPre_And_HoldTriggerSingle() {

        //input param
        final Boolean postFlag = false;
        final Boolean singleTriggerFlag = true;

        //【step1】stb
        Results.WaferLotStartReqResult waferLotStartReqResult = (Results.WaferLotStartReqResult) stbCase.STB_PreparedLot().getBody();
        //【step2】getOperationNumber and RouteID
        Results.LotOperationSelectionInqResult lotOperationSelectionInqResult = (Results.LotOperationSelectionInqResult) lotGeneralTestCase.getLotOperationSelectionInq(waferLotStartReqResult.getLotID(), true, true, true).getBody();
        Infos.OperationNameAttributes operationNameAttributes = lotOperationSelectionInqResult.getOperationNameAttributesAttributes().getContent().get(1);
        String operationNumber = operationNameAttributes.getOperationNumber();
        ObjectIdentifier routeID = operationNameAttributes.getRouteID();

        return futureHoldTestCase.futureHoldRegisterBySpecLot(waferLotStartReqResult.getLotID(), operationNumber, routeID, postFlag,singleTriggerFlag);
    }

    public Response futureHold_With_HoldTimingPre_And_HoldTriggerMultiple() {

        //input param
        final Boolean postFlag = false;
        final Boolean singleTriggerFlag = false;

        //【step1】stb
        Results.WaferLotStartReqResult waferLotStartReqResult = (Results.WaferLotStartReqResult) stbCase.STB_PreparedLot().getBody();
        //【step2】getOperationNumber and RouteID
        Results.LotOperationSelectionInqResult lotOperationSelectionInqResult = (Results.LotOperationSelectionInqResult) lotGeneralTestCase.getLotOperationSelectionInq(waferLotStartReqResult.getLotID(), true, true, true).getBody();
        Infos.OperationNameAttributes operationNameAttributes = lotOperationSelectionInqResult.getOperationNameAttributesAttributes().getContent().get(1);
        String operationNumber = operationNameAttributes.getOperationNumber();
        ObjectIdentifier routeID = operationNameAttributes.getRouteID();

        return futureHoldTestCase.futureHoldRegisterBySpecLot(waferLotStartReqResult.getLotID(), operationNumber, routeID, postFlag,singleTriggerFlag);
    }

    public Response futureHold_With_HoldTimingPost() {
        //input param
        final Boolean postFlag = true;
        final Boolean singleTriggerFlag = true;

        //【step1】stb
        Results.WaferLotStartReqResult waferLotStartReqResult = (Results.WaferLotStartReqResult) stbCase.STB_PreparedLot().getBody();
        //【step2】getOperationNumber and RouteID
        Results.LotOperationSelectionInqResult lotOperationSelectionInqResult = (Results.LotOperationSelectionInqResult) lotGeneralTestCase.getLotOperationSelectionInq(waferLotStartReqResult.getLotID(), true, true, true).getBody();
        Infos.OperationNameAttributes operationNameAttributes = lotOperationSelectionInqResult.getOperationNameAttributesAttributes().getContent().get(0);
        String operationNumber = operationNameAttributes.getOperationNumber();
        ObjectIdentifier routeID = operationNameAttributes.getRouteID();

        return futureHoldTestCase.futureHoldRegisterBySpecLot(waferLotStartReqResult.getLotID(), operationNumber, routeID, postFlag,singleTriggerFlag);
    }

    public void trigger_FutureHold_By_OperationLocate_When_HoldTimingPre() {
        //input param
        final Boolean postFlag = false;
        final Boolean singleTriggerFlag = true;

        //【step1】stb
        Results.WaferLotStartReqResult waferLotStartReqResult = (Results.WaferLotStartReqResult) stbCase.STB_PreparedLot().getBody();

        //【step2】get lot info
        List<ObjectIdentifier> lotIds = new ArrayList<>();
        lotIds.add(waferLotStartReqResult.getLotID());
        Results.LotInfoInqResult lotInfo1 = (Results.LotInfoInqResult) lotGeneralTestCase.getLotInfoCase(lotIds).getBody();

        //【step3】getOperationNumber and RouteID
        Results.LotOperationSelectionInqResult lotOperationSelectionInqResult = (Results.LotOperationSelectionInqResult) lotGeneralTestCase.getLotOperationSelectionInq(waferLotStartReqResult.getLotID(), true, true, true).getBody();
        Infos.OperationNameAttributes operationNameAttributes = lotOperationSelectionInqResult.getOperationNameAttributesAttributes().getContent().get(1);
        String operationNumber = operationNameAttributes.getOperationNumber();
        ObjectIdentifier routeID = operationNameAttributes.getRouteID();

        //【step3】future hold
        Response response = futureHoldTestCase.futureHoldRegisterBySpecLot(waferLotStartReqResult.getLotID(), operationNumber, routeID, postFlag, singleTriggerFlag);

        //【step4】lotInfo lot state is waiting
        Results.LotInfoInqResult lotInfoInqResult2 = (Results.LotInfoInqResult) lotGeneralTestCase.getLotInfoCase(lotIds).getBody();
        Infos.LotInfo lotInfo2 = lotInfoInqResult2.getLotInfoList().get(0);
        Assert.isTrue(lotInfo2.getLotBasicInfo().getLotStatus().equals("Waiting"), "the lot status must be waiting");

        //【step5】skip to future hold point

        Params.SkipReqParams skipReqParams = new Params.SkipReqParams();
        skipReqParams.setCurrentOperationNumber(lotInfo2.getLotOperationInfo().getOperationNumber());
        skipReqParams.setCurrentRouteID(lotInfo2.getLotOperationInfo().getRouteID());
        skipReqParams.setLocateDirection(true);
        skipReqParams.setLotID(waferLotStartReqResult.getLotID());
        skipReqParams.setOperationID(operationNameAttributes.getOperationID());
        skipReqParams.setOperationNumber(operationNameAttributes.getOperationNumber());
        skipReqParams.setProcessRef(operationNameAttributes.getProcessRef());
        skipReqParams.setRouteID(operationNameAttributes.getRouteID());
        skipReqParams.setSeqno(-1);
        skipReqParams.setSequenceNumber(0);
        Response response1 = operationSkipTestCase.operationSkip(skipReqParams);

        //【step6】lotInfo lot state is onhold
        Results.LotInfoInqResult lotInfoInqResult3 = (Results.LotInfoInqResult) lotGeneralTestCase.getLotInfoCase(lotIds).getBody();
        Infos.LotInfo lotInfo3 = lotInfoInqResult3.getLotInfoList().get(0);
        Assert.isTrue(lotInfo3.getLotBasicInfo().getLotStatus().equals("ONHOLD"), "the lot status must be onhold");
    }

    public void vender_Lot_Todo_FutureHold() {
        //【step1】vendor lot receive to get new lot id
        Response response = vendorLotReceiveCase.VendorLotReceive_AssignLotID();

        Results.VendorLotReceiveReqResult body = (Results.VendorLotReceiveReqResult) response.getBody();
        String lotID = body.getCreatedLotID();//lotID

        //【step2】get lot info
        List<ObjectIdentifier> lotIds = new ArrayList<>();
        lotIds.add(new ObjectIdentifier(lotID));
        Results.LotInfoInqResult lotInfo1 = (Results.LotInfoInqResult) lotGeneralTestCase.getLotInfoCase(lotIds).getBody();

        //【step3】getOperationNumber and RouteID
        Response lotOperationSelectionInqResponse = null;
        try {
            lotOperationSelectionInqResponse = lotGeneralTestCase.getLotOperationSelectionInq(new ObjectIdentifier(lotID), true, true, true);
        } catch (ServiceException serviceException) {
            Validations.assertCheck(retCodeConfig.getNotFoundProcessOperation().getCode() == serviceException.getCode(),serviceException.getMessage());
        }
    }

    public void same_Lot_FutureHold_Using_Different_ReasonCode_For_Twice() {
        //input param
        final Boolean postFlag_Pre = false;
        final Boolean singleTriggerFlag_Single = true;
        final Boolean singleTriggerFlag_Multiple = false;

        //【step1】stb
        Results.WaferLotStartReqResult waferLotStartReqResult = (Results.WaferLotStartReqResult) stbCase.STB_PreparedLot().getBody();

        //【step2】get lot info
        List<ObjectIdentifier> lotIds = new ArrayList<>();
        lotIds.add(waferLotStartReqResult.getLotID());
        Results.LotInfoInqResult lotInfo1 = (Results.LotInfoInqResult) lotGeneralTestCase.getLotInfoCase(lotIds).getBody();

        //【step3】getOperationNumber and RouteID
        Results.LotOperationSelectionInqResult lotOperationSelectionInqResult = (Results.LotOperationSelectionInqResult) lotGeneralTestCase.getLotOperationSelectionInq(waferLotStartReqResult.getLotID(), true, true, true).getBody();
        Infos.OperationNameAttributes operationNameAttributes = lotOperationSelectionInqResult.getOperationNameAttributesAttributes().getContent().get(1);
        String operationNumber = operationNameAttributes.getOperationNumber();
        ObjectIdentifier routeID = operationNameAttributes.getRouteID();

        //【step3】future hold first time
        final String inputReasonCode1 = "MGHL";
        Response response = futureHoldTestCase.futureHoldRegisterBySpecLotWithReasonCode(waferLotStartReqResult.getLotID(), operationNumber, routeID, postFlag_Pre, singleTriggerFlag_Single,inputReasonCode1);

        //【step4】futrue hold second time
        final String inputReasonCode2 = "PLMG";
        Infos.OperationNameAttributes operationNameAttributes2 = lotOperationSelectionInqResult.getOperationNameAttributesAttributes().getContent().get(2);
        String operationNumber2 = operationNameAttributes2.getOperationNumber();
        ObjectIdentifier routeID2 = operationNameAttributes2.getRouteID();
        Response response2 = futureHoldTestCase.futureHoldRegisterBySpecLotWithReasonCode(waferLotStartReqResult.getLotID(), operationNumber2, routeID2, postFlag_Pre, singleTriggerFlag_Multiple,inputReasonCode2);
    }

    public void same_Lot_FutureHold_Using_Same_ReasonCode_For_Twice() {
        //input param
        final Boolean postFlag_Pre = false;
        final Boolean singleTriggerFlag_Single = true;

        //【step1】stb
        Results.WaferLotStartReqResult waferLotStartReqResult = (Results.WaferLotStartReqResult) stbCase.STB_PreparedLot().getBody();

        //【step2】get lot info
        List<ObjectIdentifier> lotIds = new ArrayList<>();
        lotIds.add(waferLotStartReqResult.getLotID());
        Results.LotInfoInqResult lotInfo1 = (Results.LotInfoInqResult) lotGeneralTestCase.getLotInfoCase(lotIds).getBody();

        //【step3】getOperationNumber and RouteID
        Results.LotOperationSelectionInqResult lotOperationSelectionInqResult = (Results.LotOperationSelectionInqResult) lotGeneralTestCase.getLotOperationSelectionInq(waferLotStartReqResult.getLotID(), true, true, true).getBody();
        Infos.OperationNameAttributes operationNameAttributes = lotOperationSelectionInqResult.getOperationNameAttributesAttributes().getContent().get(1);
        String operationNumber = operationNameAttributes.getOperationNumber();
        ObjectIdentifier routeID = operationNameAttributes.getRouteID();

        //【step3】future hold first time
        final String inputReasonCode = "MGHL";
        Response response = futureHoldTestCase.futureHoldRegisterBySpecLotWithReasonCode(waferLotStartReqResult.getLotID(), operationNumber, routeID, postFlag_Pre, singleTriggerFlag_Single,inputReasonCode);

        //【step4】futrue hold second time
        Response response2 = null;
        try {
            response2 = futureHoldTestCase.futureHoldRegisterBySpecLotWithReasonCode(waferLotStartReqResult.getLotID(), operationNumber, routeID, postFlag_Pre, singleTriggerFlag_Single,inputReasonCode);
        } catch (Exception ex) {
            if (ex instanceof ServiceException){
                ServiceException ex1 = (ServiceException) ex;
                Validations.assertCheck(ex1.getCode() == retCodeConfig.getDuplicateFtholdEntry().getCode(),ex1.getMessage());
            }
            if (ex instanceof  DuplicateRecordException){
                DuplicateRecordException ex1 = (DuplicateRecordException) ex;
                Validations.assertCheck(ex1.getCoreCode().getCode() == retCodeConfig.getDuplicateRecord().getCode(),ex1.getMessage());
            }
        }
    }

    public void trigger_FutureHold_By_Operation_Complete_When_The_HoldTimingPre() {

        //input param
        final Boolean postFlag_Pre = false;
        final Boolean singleTriggerFlag_Single = true;

        //【step1】stb
        Results.WaferLotStartReqResult waferLotStartReqResult = (Results.WaferLotStartReqResult) stbCase.STB_PreparedLot().getBody();

        //【step2】get lot info
        List<ObjectIdentifier> lotIds = new ArrayList<>();
        lotIds.add(waferLotStartReqResult.getLotID());
        Results.LotInfoInqResult lotInfo1 = (Results.LotInfoInqResult) lotGeneralTestCase.getLotInfoCase(lotIds).getBody();

        //【step3】getOperationNumber and RouteID in the next step
        Results.LotOperationSelectionInqResult lotOperationSelectionInqResult = (Results.LotOperationSelectionInqResult) lotGeneralTestCase.getLotOperationSelectionInq(waferLotStartReqResult.getLotID(), true, true, true).getBody();
        Infos.OperationNameAttributes operationNameAttributes = lotOperationSelectionInqResult.getOperationNameAttributesAttributes().getContent().get(1);
        String operationNumber = operationNameAttributes.getOperationNumber();
        ObjectIdentifier routeID = operationNameAttributes.getRouteID();

        //【step3】future hold
        final String inputReasonCode = "MGHL";
        futureHoldTestCase.futureHoldRegisterBySpecLotWithReasonCode(waferLotStartReqResult.getLotID(), operationNumber, routeID, postFlag_Pre, singleTriggerFlag_Single,inputReasonCode);

        //【step4】load lot on eqp
        ObjectIdentifier equipmentID = lotInfo1.getLotInfoList().get(0).getLotOperationInfo().getLotEquipmentList().get(1).getEquipmentID();
        ObjectIdentifier cassetteID = equipmentTestCase.loadingLotWithOutStartReserve(equipmentID, waferLotStartReqResult.getLotID());

        //【step5】move in
        List<ObjectIdentifier> cassetteIDs = new ArrayList<>();
        cassetteIDs.add(cassetteID);
        Results.LotsMoveInInfoInqResult lotsMoveInInfoInqResult = (Results.LotsMoveInInfoInqResult) equipmentTestCase.lotsMoveInInfoInq(equipmentID, cassetteIDs).getBody();
        Response response = equipmentTestCase.movInReq(lotsMoveInInfoInqResult.getControlJobID(), lotsMoveInInfoInqResult.getEquipmentID(), lotsMoveInInfoInqResult.getPortGroupID(), false, lotsMoveInInfoInqResult.getStartCassetteList());

        //【step6】move out
        Results.MoveInReqResult moveInReqResult = (Results.MoveInReqResult) response.getBody();
        List<Infos.StartCassette> startCassetteList = moveInReqResult.getStartCassetteList();
        List<ObjectIdentifier> lotIDs = new ArrayList<>();
        for (Infos.StartCassette startCassette : startCassetteList){
            for (Infos.LotInCassette lotInCassette : startCassette.getLotInCassetteList()){
                lotIDs.add(lotInCassette.getLotID());
            }
        }
        lotGeneralTestCase.getLotInfoCase(lotIDs);

        Params.OpeComWithDataReqParams opeComWithDataReqParams = new Params.OpeComWithDataReqParams();
        opeComWithDataReqParams.setControlJobID(moveInReqResult.getControlJobID());
        opeComWithDataReqParams.setUser(lotGeneralTestCase.getUser());
        opeComWithDataReqParams.setEquipmentID(equipmentID);
        equipmentTestCase.moveOut(opeComWithDataReqParams);
        //【step7】get lot info check the lot status

        lotIds.add(waferLotStartReqResult.getLotID());
        Results.LotInfoInqResult lotInfo2 = (Results.LotInfoInqResult) lotGeneralTestCase.getLotInfoCase(lotIds).getBody();
        Infos.LotInfo lotInfo = lotInfo2.getLotInfoList().get(0);
        Assert.isTrue(lotInfo.getLotBasicInfo().getLotStatus().equals("ONHOLD"), "the lot status must be onhold");
    }



    public void trigger_TwiceFutureHold_By_Operation_Complete_When_Register_TwoStep() {

        //input param
        final Boolean postFlag_Pre = false;
        final Boolean postFlag_Post = true;
        final Boolean singleTriggerFlag_Single = true;

        //【step1】stb
        Results.WaferLotStartReqResult waferLotStartReqResult = (Results.WaferLotStartReqResult) stbCase.STB_PreparedLot().getBody();

        //【step2】get lot info
        List<ObjectIdentifier> lotIds = new ArrayList<>();
        lotIds.add(waferLotStartReqResult.getLotID());
        Results.LotInfoInqResult lotInfoInqResult1 = (Results.LotInfoInqResult) lotGeneralTestCase.getLotInfoCase(lotIds).getBody();
        Infos.LotInfo lotInfo1 = lotInfoInqResult1.getLotInfoList().get(0);

        //【step3】getOperationNumber and RouteID in the next step
        Results.LotOperationSelectionInqResult lotOperationSelectionInqResult = (Results.LotOperationSelectionInqResult) lotGeneralTestCase.getLotOperationSelectionInq(waferLotStartReqResult.getLotID(), true, true, true).getBody();
        Infos.OperationNameAttributes operationNameAttributes1 = lotOperationSelectionInqResult.getOperationNameAttributesAttributes().getContent().get(1);
        String operationNumber1 = operationNameAttributes1.getOperationNumber();
        ObjectIdentifier routeID1 = operationNameAttributes1.getRouteID();

        Infos.OperationNameAttributes operationNameAttributes2 = lotOperationSelectionInqResult.getOperationNameAttributesAttributes().getContent().get(2);
        String operationNumber2 = operationNameAttributes2.getOperationNumber();
        ObjectIdentifier routeID2 = operationNameAttributes2.getRouteID();

        //【step3】future hold step1
        final String inputReasonCode1 = "SOHL";
        futureHoldTestCase.futureHoldRegisterBySpecLotWithReasonCode(waferLotStartReqResult.getLotID(), operationNumber1, routeID1, postFlag_Post, singleTriggerFlag_Single,inputReasonCode1);

        //【step4】future hold step2
        final String inputReasonCode2 = "SHLC";
        futureHoldTestCase.futureHoldRegisterBySpecLotWithReasonCode(waferLotStartReqResult.getLotID(), operationNumber2, routeID2, postFlag_Pre, singleTriggerFlag_Single,inputReasonCode2);

        //【step5】skip th step1
        Params.SkipReqParams skipReqParams = new Params.SkipReqParams();
        skipReqParams.setCurrentOperationNumber(lotInfo1.getLotOperationInfo().getOperationNumber());
        skipReqParams.setCurrentRouteID(lotInfo1.getLotOperationInfo().getRouteID());
        skipReqParams.setLocateDirection(true);
        skipReqParams.setLotID(waferLotStartReqResult.getLotID());
        skipReqParams.setOperationID(operationNameAttributes1.getOperationID());
        skipReqParams.setOperationNumber(operationNameAttributes1.getOperationNumber());
        skipReqParams.setProcessRef(operationNameAttributes1.getProcessRef());
        skipReqParams.setRouteID(operationNameAttributes1.getRouteID());
        skipReqParams.setSeqno(-1);
        skipReqParams.setSequenceNumber(0);
        Response response1 = operationSkipTestCase.operationSkip(skipReqParams);

        Results.LotInfoInqResult lotInfoInqResult2 = (Results.LotInfoInqResult) lotGeneralTestCase.getLotInfoCase(lotIds).getBody();
        Infos.LotInfo lotInfo2 = lotInfoInqResult2.getLotInfoList().get(0);

        //【step6】load lot when step1
        ObjectIdentifier equipmentID = lotInfo2.getLotOperationInfo().getLotEquipmentList().get(0).getEquipmentID();
        ObjectIdentifier cassetteID = equipmentTestCase.loadingLotWithOutStartReserve(equipmentID, waferLotStartReqResult.getLotID());


        //【step7】move in lot when step1
        List<ObjectIdentifier> cassetteIDs = new ArrayList<>();
        cassetteIDs.add(cassetteID);
        Results.LotsMoveInInfoInqResult lotsMoveInInfoInqResult = (Results.LotsMoveInInfoInqResult) equipmentTestCase.lotsMoveInInfoInq(equipmentID, cassetteIDs).getBody();
        Response response = equipmentTestCase.movInReq(lotsMoveInInfoInqResult.getControlJobID(), lotsMoveInInfoInqResult.getEquipmentID(), lotsMoveInInfoInqResult.getPortGroupID(), false, lotsMoveInInfoInqResult.getStartCassetteList());

        //【step8】move out
        Results.MoveInReqResult moveInReqResult = (Results.MoveInReqResult) response.getBody();
        List<Infos.StartCassette> startCassetteList = moveInReqResult.getStartCassetteList();
        List<ObjectIdentifier> lotIDs = new ArrayList<>();
        for (Infos.StartCassette startCassette : startCassetteList){
            for (Infos.LotInCassette lotInCassette : startCassette.getLotInCassetteList()){
                lotIDs.add(lotInCassette.getLotID());
            }
        }
        lotGeneralTestCase.getLotInfoCase(lotIDs);

        Params.OpeComWithDataReqParams opeComWithDataReqParams = new Params.OpeComWithDataReqParams();
        opeComWithDataReqParams.setControlJobID(moveInReqResult.getControlJobID());
        opeComWithDataReqParams.setUser(lotGeneralTestCase.getUser());
        opeComWithDataReqParams.setEquipmentID(equipmentID);
        equipmentTestCase.moveOut(opeComWithDataReqParams);

        //【step9】check future hold list
        Page<Infos.FutureHoldListAttributes> result = (Page<Infos.FutureHoldListAttributes>) futureHoldTestCase.getFutureHoldListByKey(waferLotStartReqResult.getLotID()).getBody();
        Assert.isTrue(result.getContent().size() == 0,"now don not have any future hold list");
    }
}
