package com.fa.cim.newIntegration.equipment.scase;

import com.alibaba.fastjson.JSONObject;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.Response;
import com.fa.cim.common.utils.CimArrayUtils;
import com.fa.cim.controller.edc.EngineerDataCollectionController;
import com.fa.cim.controller.edc.EngineerDataCollectionInqController;
import com.fa.cim.core.TestCommonData;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;
import com.fa.cim.newIntegration.stb.scase.STBCase;
import com.fa.cim.newIntegration.tcase.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/9/15       ********             lightyh             create file
 *
 * @author: lightyh
 * @date: 2019/9/15 8:52
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Service
@Slf4j
public class MoveOutCase {

    @Autowired
    private MoveInCase moveInCase;

    @Autowired
    private EquipmentTestCase equipmentTestCase;

    @Autowired
    private LotGeneralTestCase lotGeneralTestCase;

    @Autowired
    @Qualifier("testCommonDataWithDB")
    private TestCommonData testCommonData;

    @Autowired
    private FutureHoldTestCase futureHoldTestCase;

    @Autowired
    private STBCase stbCase;

    @Autowired
    private OperationSkipTestCase operationSkipTestCase;

    @Autowired
    private ElectronicInformationTestCase electronicInformationTestCase;

    @Autowired
    private EngineerDataCollectionInqController dataCollectionInqController;

    @Autowired
    private EngineerDataCollectionController dataCollectionController;

    public List<Infos.LotInfo> moveOut_Normal(){
        //【step1】move in
        Results.MoveInReqResult moveInReqResult = moveInCase.moveIn_GenerateControlJob_Without_StartReservation();
        //【step2】get lot info
        List<Infos.StartCassette> startCassetteList = moveInReqResult.getStartCassetteList();
        List<ObjectIdentifier> lotIDs = new ArrayList<>();
        for (Infos.StartCassette startCassette : startCassetteList){
            for (Infos.LotInCassette lotInCassette : startCassette.getLotInCassetteList()){
                lotIDs.add(lotInCassette.getLotID());
            }
        }
        Results.LotInfoInqResult lotInfoInqResult = (Results.LotInfoInqResult) lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();
        //【step3】move out
        Params.OpeComWithDataReqParams opeComWithDataReqParams = new Params.OpeComWithDataReqParams();
        opeComWithDataReqParams.setControlJobID(moveInReqResult.getControlJobID());
        opeComWithDataReqParams.setUser(testCommonData.getUSER());
        opeComWithDataReqParams.setEquipmentID(testCommonData.getEQUIPMENTID());
        equipmentTestCase.moveOut(opeComWithDataReqParams);
        return lotInfoInqResult.getLotInfoList();
    }

    public void moveOut_withSingleDataCollection(){
        // 1. STB Product0.01
        Results.WaferLotStartReqResult waferLotStartReqResult= (Results.WaferLotStartReqResult) stbCase.STB_NotPreparedLot().getBody();

        // 2. skip 2000.0300
        Results.LotInfoInqResult lotInfoInqResult= (Results.LotInfoInqResult) lotGeneralTestCase.getLotInfoCase(CimArrayUtils.generateList(waferLotStartReqResult.getLotID())).getBody();
        Results.LotOperationSelectionInqResult lotOperationSelectionInqResult= (Results.LotOperationSelectionInqResult) lotGeneralTestCase.getLotOperationSelectionInq(waferLotStartReqResult.getLotID(),false,true,true).getBody();
        Infos.LotOperationInfo lotOperationInfo = lotInfoInqResult.getLotInfoList().get(0).getLotOperationInfo();
        Infos.OperationNameAttributes operationNameAttributes = lotOperationSelectionInqResult.getOperationNameAttributesAttributes().getContent().get(3);
        Params.SkipReqParams skipReqParams= JSONObject.parseObject("{\"currentOperationNumber\":\"1000.0100\",\"currentRouteID\":{\"referenceKey\":\"OMPRP.49867291127402983\",\"value\":\"LAYER0MA.01\"},\"locateDirection\":true,\"lotID\":{\"referenceKey\":\"FRLOT.252734301664182272\",\"value\":\"NP000318.00A\"},\"operationID\":{\"referenceKey\":\"OMPRP.88321534610297170\",\"value\":\"FST010.01\"},\"operationNumber\":\"2000.0300\",\"processRef\":{\"mainProcessFlow\":\"OMPRF.66369402815281944\",\"moduleNumber\":\"2000\",\"modulePOS\":\"OMPRSS.24145333485278681\",\"moduleProcessFlow\":\"OMPRF.54939159889810491\",\"processFlow\":\"OMPRF.49867748958157252\",\"processOperationSpecification\":\"OMPRSS.19491816993363067\",\"siInfo\":null},\"routeID\":{\"referenceKey\":\"OMPRP.49867291127402983\",\"value\":\"LAYER0MA.01\"},\"seqno\":-1,\"sequenceNumber\":0,\"user\":{\"userID\":{\"value\":\"ADMIN\",\"referenceKey\":\"\"},\"password\":\"b51fa595e692d53739b69131cdc73440\",\"functionID\":\"OLOTW003\"}}",
                Params.SkipReqParams.class);
        skipReqParams.setCurrentRouteID(lotOperationInfo.getRouteID());
        skipReqParams.setLotID(waferLotStartReqResult.getLotID());
        skipReqParams.setOperationID(lotOperationInfo.getOperationID());
        skipReqParams.setProcessRef(operationNameAttributes.getProcessRef());
        skipReqParams.setRouteID(operationNameAttributes.getRouteID());
        operationSkipTestCase.operationSkip(skipReqParams);

        // 3. move in
        ObjectIdentifier equipmentID= ObjectIdentifier.buildWithValue("1THK01");
        Results.MoveInReqResult moveInReqResult= (Results.MoveInReqResult) moveInCase.moveIn_GenerateControlJob_Without_StartReservation(waferLotStartReqResult.getLotID(),equipmentID).getBody();

        // 4. data item with temp data
        Params.EDCDataItemWithTransitDataInqParams edcDataItemWithTransitDataInqParams=new Params.EDCDataItemWithTransitDataInqParams();
        edcDataItemWithTransitDataInqParams.setControlJobID(moveInReqResult.getControlJobID());
        edcDataItemWithTransitDataInqParams.setEquipmentID(equipmentID);
        edcDataItemWithTransitDataInqParams.setUser(testCommonData.getUSER());
        Results.EDCDataItemWithTransitDataInqResult edcDataItemWithTransitDataInqResult= (Results.EDCDataItemWithTransitDataInqResult) dataCollectionInqController.edcDataItemWithTransitDataInq(edcDataItemWithTransitDataInqParams).getBody();

        // 5. spec check
        Params.SpecCheckReqParams specCheckReqParams=new Params.SpecCheckReqParams();
        specCheckReqParams.setControlJobID(edcDataItemWithTransitDataInqResult.getControlJobID());
        specCheckReqParams.setEquipmentID(edcDataItemWithTransitDataInqResult.getEquipmentID());
        specCheckReqParams.setUser(testCommonData.getUSER());
        List<Infos.StartCassette> startCassetteList = edcDataItemWithTransitDataInqResult.getStartCassetteList();
        Infos.LotInCassette lotInCassette = startCassetteList.get(0).getLotInCassetteList().get(0);
        List<Infos.LotWafer> lotWaferList = lotInCassette.getLotWaferList();
        String[] dataValues={"100.2","50.2","80.2"};
        int i=0;
        for (Infos.DataCollectionItemInfo dataCollectionItemInfo : lotInCassette.getStartRecipe().getDcDefList().get(0).getDcItems()) {
            if (!"Mean".equalsIgnoreCase(dataCollectionItemInfo.getCalculationType())){
                dataCollectionItemInfo.setDataValue(dataValues[i++]);
                dataCollectionItemInfo.setWaferID(lotWaferList.get(i).getWaferID());
            }
        }
        specCheckReqParams.setStartCassetteList(startCassetteList);
        Results.SpecCheckReqResult specCheckReqResult= (Results.SpecCheckReqResult) dataCollectionController.specCheckReq(specCheckReqParams).getBody();

        // 6. move out
        Params.OpeComWithDataReqParams opeComWithDataReqParams=new Params.OpeComWithDataReqParams();
        opeComWithDataReqParams.setControlJobID(edcDataItemWithTransitDataInqResult.getControlJobID());
        opeComWithDataReqParams.setEquipmentID(edcDataItemWithTransitDataInqResult.getEquipmentID());
        opeComWithDataReqParams.setUser(testCommonData.getUSER());
        equipmentTestCase.moveOut(opeComWithDataReqParams);

    }

    public Response onlyMoveOut(ObjectIdentifier controlJobID, ObjectIdentifier equipmentID){
        Params.OpeComWithDataReqParams opeComWithDataReqParams = new Params.OpeComWithDataReqParams();
        opeComWithDataReqParams.setControlJobID(controlJobID);
        opeComWithDataReqParams.setUser(testCommonData.getUSER());
        opeComWithDataReqParams.setEquipmentID(equipmentID);
        return equipmentTestCase.moveOut(opeComWithDataReqParams);
    }

    public void clearAnyFutureHoldAlreadyTriggeredAfterOperationCompletion(){
        //【step1】stb
        Results.WaferLotStartReqResult waferLotStartReqResult = (Results.WaferLotStartReqResult) stbCase.STB_NotPreparedLot().getBody();
        //【step2】getOperationNumber and RouteID
        Results.LotOperationSelectionInqResult lotOperationSelectionInqResult = (Results.LotOperationSelectionInqResult) lotGeneralTestCase.getLotOperationSelectionInq(waferLotStartReqResult.getLotID(), true, true, true).getBody();
        Infos.OperationNameAttributes operationNameAttributes = lotOperationSelectionInqResult.getOperationNameAttributesAttributes().getContent().get(1);
        String operationNumber = operationNameAttributes.getOperationNumber();
        ObjectIdentifier routeID = operationNameAttributes.getRouteID();
        futureHoldTestCase.futureHoldRegisterBySpecLot(waferLotStartReqResult.getLotID(), operationNumber, routeID, false);

        // move in
        Results.MoveInReqResult moveInReqResult= (Results.MoveInReqResult) moveInCase.moveIn_GenerateControlJob_Without_StartReservation(waferLotStartReqResult.getLotID(),testCommonData.getEQUIPMENTID()).getBody();

        // move out
        Results.MoveOutReqResult moveOutReqResult= (Results.MoveOutReqResult) onlyMoveOut(moveInReqResult.getControlJobID(),testCommonData.getEQUIPMENTID()).getBody();

        // query Future Hold List
        Page<Infos.FutureHoldListAttributes> futureHoldListAttributesPage= (Page<Infos.FutureHoldListAttributes>) futureHoldTestCase.getFutureHoldListByKey(waferLotStartReqResult.getLotID()).getBody();

        Assert.isTrue("ONHOLD".equalsIgnoreCase(moveOutReqResult.getMoveOutLot().get(0).getLotStatus())
                && CimArrayUtils.isEmpty(futureHoldListAttributesPage.getContent()),"The Future Hold record will be cleared");

    }

    public void FutureHoldOfPOSTTypeWasRegisteredForTheProcessOfOperationComplete(){
        //【step1】stb
        Results.WaferLotStartReqResult waferLotStartReqResult = (Results.WaferLotStartReqResult) stbCase.STB_NotPreparedLot().getBody();
        //【step2】getOperationNumber and RouteID
        Results.LotOperationSelectionInqResult lotOperationSelectionInqResult = (Results.LotOperationSelectionInqResult) lotGeneralTestCase.getLotOperationSelectionInq(waferLotStartReqResult.getLotID(), true, true, true).getBody();
        Infos.OperationNameAttributes operationNameAttributes = lotOperationSelectionInqResult.getOperationNameAttributesAttributes().getContent().get(0);
        String operationNumber = operationNameAttributes.getOperationNumber();
        ObjectIdentifier routeID = operationNameAttributes.getRouteID();
        futureHoldTestCase.futureHoldRegisterBySpecLot(waferLotStartReqResult.getLotID(), operationNumber, routeID, true);

        // move in
        Results.MoveInReqResult moveInReqResult= (Results.MoveInReqResult) moveInCase.moveIn_GenerateControlJob_Without_StartReservation(waferLotStartReqResult.getLotID(),testCommonData.getEQUIPMENTID()).getBody();

        // move out
        Results.MoveOutReqResult moveOutReqResult= (Results.MoveOutReqResult) onlyMoveOut(moveInReqResult.getControlJobID(),testCommonData.getEQUIPMENTID()).getBody();

        // query Future Hold List
        Page<Infos.FutureHoldListAttributes> futureHoldListAttributesPage= (Page<Infos.FutureHoldListAttributes>) futureHoldTestCase.getFutureHoldListByKey(waferLotStartReqResult.getLotID()).getBody();

        Assert.isTrue("ONHOLD".equalsIgnoreCase(moveOutReqResult.getMoveOutLot().get(0).getLotStatus())
                && CimArrayUtils.isEmpty(futureHoldListAttributesPage.getContent()),"The Future Hold record will be cleared");

    }

    public void clearTheLotInformationFromInProcessLotInformationAfterOperationComplete(){
        //【step1】stb
        Results.WaferLotStartReqResult waferLotStartReqResult = (Results.WaferLotStartReqResult) stbCase.STB_NotPreparedLot().getBody();

        // move in
        Results.MoveInReqResult moveInReqResult= (Results.MoveInReqResult) moveInCase.moveIn_GenerateControlJob_Without_StartReservation(waferLotStartReqResult.getLotID(),testCommonData.getEQUIPMENTID()).getBody();

        // move out
        Results.MoveOutReqResult moveOutReqResult= (Results.MoveOutReqResult) onlyMoveOut(moveInReqResult.getControlJobID(),testCommonData.getEQUIPMENTID()).getBody();

        // query control Job List
        Results.EqpInfoInqResult eqpInfoInqResult= (Results.EqpInfoInqResult) electronicInformationTestCase.eqpInfoInqCase(testCommonData.getEQUIPMENTID()).getBody();
        Optional.ofNullable(eqpInfoInqResult.getEquipmentInprocessingControlJobList()).orElse(new ArrayList<>()).forEach(
                eqpInprocessingControlJob ->
                        Assert.isTrue(!moveInReqResult.getControlJobID().equals(eqpInprocessingControlJob.getControlJobID()),"Clear the Lot Information from In-Process Lot Information after Operation Complete")
        );

    }

    public void ifTheequipmentIsOffLineTheEquipmentStatusIsChangedFromInProcessPRDToWaitingSBYAutomaticallyWithinOperationCompLogic(){
        //【step1】stb
        Results.WaferLotStartReqResult waferLotStartReqResult = (Results.WaferLotStartReqResult) stbCase.STB_NotPreparedLot().getBody();

        // move in
        Results.MoveInReqResult moveInReqResult= (Results.MoveInReqResult) moveInCase.moveIn_GenerateControlJob_Without_StartReservation(waferLotStartReqResult.getLotID(),testCommonData.getEQUIPMENTID()).getBody();

        // move out
        Results.MoveOutReqResult moveOutReqResult= (Results.MoveOutReqResult) onlyMoveOut(moveInReqResult.getControlJobID(),testCommonData.getEQUIPMENTID()).getBody();

        Results.EqpInfoInqResult eqpInfoInqResult= (Results.EqpInfoInqResult) electronicInformationTestCase.eqpInfoInqCase(testCommonData.getEQUIPMENTID()).getBody();
        Assert.isTrue("SBY".equalsIgnoreCase(eqpInfoInqResult.getEquipmentStatusInfo().getE10Status()),"If the equipment is OffLine, the equipment status is changed from In Process (PRD) to Waiting (SBY)\n" +
                "automatically within Operation Comp Logic ");

    }




}