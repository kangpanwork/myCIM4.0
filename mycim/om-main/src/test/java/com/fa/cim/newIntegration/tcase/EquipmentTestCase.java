package com.fa.cim.newIntegration.tcase;

import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.Response;
import com.fa.cim.common.utils.CimArrayUtils;
import com.fa.cim.common.utils.CimObjectUtils;
import com.fa.cim.common.utils.CimStringUtils;
import com.fa.cim.controller.dispatch.DispatchController;
import com.fa.cim.controller.dispatch.DispatchInqController;
import com.fa.cim.controller.equipment.EquipmentController;
import com.fa.cim.controller.equipment.EquipmentInqController;
import com.fa.cim.core.TestCommonData;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;
import com.fa.cim.jpa.SearchCondition;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/9/10          ********            Nyx                create file
 *
 * @author: Nyx
 * @date: 2019/9/10 16:33
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Service
@Slf4j
public class EquipmentTestCase {

    @Autowired
    @Qualifier("testCommonDataWithDB")
    private TestCommonData testCommonData;

    @Autowired
    private DispatchInqController dispatchInqController;
    @Autowired
    private DispatchController dispatchController;
    @Autowired
    private EquipmentController equipmentController;

    @Autowired
    private EquipmentInqController equipmentInqController;

    @Autowired
    private ElectronicInformationTestCase electronicInformationTestCase;

    @Autowired
    private LotGeneralTestCase lotGeneralTestCase;

    @Autowired
    private CommonTestCase commonTestCase;

    @Autowired
    private BankTestCase bankTestCase;

    public Response getLoadPurposeTypeCase(String portPurposeType) {
        List<String> loadPurposeTypes = new ArrayList<>();
        loadPurposeTypes.add(portPurposeType);
        return Response.createSucc(TransactionIDEnum.LOADING_LOT_RPT.getValue(),loadPurposeTypes);
    }

    public Response whatNextInqCase(ObjectIdentifier equipmentID) {
        Params.WhatNextLotListParams whatNextLotListParams = new Params.WhatNextLotListParams();
        whatNextLotListParams.setUser(testCommonData.getUSER());
        whatNextLotListParams.setEquipmentID(equipmentID);
        SearchCondition searchCondition = new SearchCondition();
        searchCondition.setSize(9999);
        whatNextLotListParams.setSearchCondition(searchCondition);
        return dispatchInqController.whatNextInq(whatNextLotListParams);
    }

    public Results.MoveInReserveCancelReqResult startLotsReservationCancel(Params.MoveInReserveCancelReqParams params){
        return (Results.MoveInReserveCancelReqResult) dispatchController.moveInReserveCancelReq(params).getBody();
    }

    public Response lotsMoveInReserveInfoInqCase(Results.LotInfoInqResult lotInfoInqResult) {
        return lotsMoveInReserveInfoInqCase(lotInfoInqResult,testCommonData.getEQUIPMENTID());
    }

    public Response lotsMoveInReserveInfoInqCase(Results.LotInfoInqResult lotInfoInqResult, ObjectIdentifier equipmentID, ObjectIdentifier lotID) {
        List<Infos.LotInfo> lotInfoList = lotInfoInqResult.getLotInfoList();
        Infos.LotListInCassetteInfo lotListInCassetteInfo = lotInfoInqResult.getLotListInCassetteInfo();
        List<Infos.WaferMapInCassetteInfo> waferMapInCassetteInfoList = lotInfoInqResult.getWaferMapInCassetteInfoList();

        Params.LotsMoveInReserveInfoInqParams lotsMoveInReserveInfoInqParams = new Params.LotsMoveInReserveInfoInqParams();
        lotsMoveInReserveInfoInqParams.setUser(testCommonData.getUSER());
        lotsMoveInReserveInfoInqParams.setEquipmentID(equipmentID);
        List<Infos.StartCassette> startCassettes = new ArrayList<>();
        Infos.StartCassette startCassette = new Infos.StartCassette();
        startCassette.setCassetteID(lotListInCassetteInfo.getCassetteID());
        List<Infos.LotInCassette> lotInCassetteList = new ArrayList<>();
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
                lotWafer.setProcessJobExecFlag(true);
                lotWafer.setProcessJobStatus("");     //the default value is ""
                lotWafer.setControlWaferFlag(false); // the default value is false
                lotWafer.setParameterUpdateFlag(false); // the default value is falsed
                lotWaferList.add(lotWafer);
            }
            lotInCassette.setLotWaferList(lotWaferList);
            lotInCassette.setMonitorLotFlag(false);
            lotInCassette.setMoveInFlag(true);
            if (!CimObjectUtils.isEmptyWithValue(lotID)){
                if (CimObjectUtils.equalsWithValue(lotInfo.getLotBasicInfo().getLotID(), lotID)){
                    lotInCassette.setMoveInFlag(true);
                } else {
                    lotInCassette.setMoveInFlag(false);
                }

            }

            lotInCassette.setProductID(lotInfo.getLotProductInfo().getProductID());
            lotInCassette.setStartOperationInfo(new Infos.StartOperationInfo());
            lotInCassette.setStartRecipe(new Infos.StartRecipe());
            lotInCassette.setSubLotType(lotInfo.getLotBasicInfo().getSubLotType());
            lotInCassetteList.add(lotInCassette);
        }
        startCassette.setLotInCassetteList(lotInCassetteList);
        startCassettes.add(startCassette);
        lotsMoveInReserveInfoInqParams.setStartCassettes(startCassettes);
        return dispatchInqController.lotsMoveInReserveInfoInq(lotsMoveInReserveInfoInqParams);
    }

    public Response lotsMoveInReserveInfoInqCase(Results.LotInfoInqResult lotInfoInqResult,ObjectIdentifier equipmentID) {
        return this.lotsMoveInReserveInfoInqCase(lotInfoInqResult, equipmentID, null);
    }

    public Response lotsMoveInReserveInfoInqCase(List<Results.LotInfoInqResult> lotInfoInqResultList,ObjectIdentifier equipmentID) {
        if (CimArrayUtils.isEmpty(lotInfoInqResultList)){
            return null;
        }
        Params.LotsMoveInReserveInfoInqParams lotsMoveInReserveInfoInqParams = new Params.LotsMoveInReserveInfoInqParams();
        lotsMoveInReserveInfoInqParams.setUser(testCommonData.getUSER());
        lotsMoveInReserveInfoInqParams.setEquipmentID(equipmentID);
        List<Infos.StartCassette> startCassettes = new ArrayList<>();
        for (Results.LotInfoInqResult lotInfoInqResult : lotInfoInqResultList) {
            List<Infos.LotInCassette> lotInCassetteList = new ArrayList<>();
            Infos.StartCassette startCassette = new Infos.StartCassette();
            List<Infos.LotInfo> lotInfoList = lotInfoInqResult.getLotInfoList();
            Infos.LotListInCassetteInfo lotListInCassetteInfo = lotInfoInqResult.getLotListInCassetteInfo();
            List<Infos.WaferMapInCassetteInfo> waferMapInCassetteInfoList = lotInfoInqResult.getWaferMapInCassetteInfoList();
            startCassette.setCassetteID(lotListInCassetteInfo.getCassetteID());
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
                    lotWafer.setProcessJobExecFlag(true); // the default value is true
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
                startCassette.setLotInCassetteList(lotInCassetteList);
                startCassettes.add(startCassette);
            }
        }
        lotsMoveInReserveInfoInqParams.setStartCassettes(startCassettes);
        return dispatchInqController.lotsMoveInReserveInfoInq(lotsMoveInReserveInfoInqParams);
    }

    public Response lotsMoveInReserveInfoForIBInqCase(List<Results.LotInfoInqResult> lotInfoInqResultList,ObjectIdentifier equipmentID) {
        if (CimArrayUtils.isEmpty(lotInfoInqResultList)){
            return null;
        }
        Params.LotsMoveInReserveInfoForIBInqParams lotsMoveInReserveInfoInqParams = new Params.LotsMoveInReserveInfoForIBInqParams();
        lotsMoveInReserveInfoInqParams.setUser(testCommonData.getUSER());
        lotsMoveInReserveInfoInqParams.setEquipmentID(equipmentID);
        List<Infos.StartCassette> startCassettes = new ArrayList<>();
        for (Results.LotInfoInqResult lotInfoInqResult : lotInfoInqResultList) {
            List<Infos.LotInCassette> lotInCassetteList = new ArrayList<>();
            Infos.StartCassette startCassette = new Infos.StartCassette();
            List<Infos.LotInfo> lotInfoList = lotInfoInqResult.getLotInfoList();
            Infos.LotListInCassetteInfo lotListInCassetteInfo = lotInfoInqResult.getLotListInCassetteInfo();
            List<Infos.WaferMapInCassetteInfo> waferMapInCassetteInfoList = lotInfoInqResult.getWaferMapInCassetteInfoList();
            startCassette.setCassetteID(lotListInCassetteInfo.getCassetteID());
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
                    lotWafer.setProcessJobExecFlag(true); // the default value is true
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
                startCassette.setLotInCassetteList(lotInCassetteList);
                startCassettes.add(startCassette);
            }
        }
        lotsMoveInReserveInfoInqParams.setStartCassettes(startCassettes);
        return dispatchInqController.lotsMoveInReserveInfoForIBInq(lotsMoveInReserveInfoInqParams);
    }

    public Response lotsMoveInReserveInfoInqCaseWithSpecifiedEqp(Results.LotInfoInqResult lotInfoInqResult, ObjectIdentifier equipmentID) {
        List<Infos.LotInfo> lotInfoList = lotInfoInqResult.getLotInfoList();
        Infos.LotListInCassetteInfo lotListInCassetteInfo = lotInfoInqResult.getLotListInCassetteInfo();
        List<Infos.WaferMapInCassetteInfo> waferMapInCassetteInfoList = lotInfoInqResult.getWaferMapInCassetteInfoList();

        Params.LotsMoveInReserveInfoInqParams lotsMoveInReserveInfoInqParams = new Params.LotsMoveInReserveInfoInqParams();
        lotsMoveInReserveInfoInqParams.setUser(testCommonData.getUSER());
        lotsMoveInReserveInfoInqParams.setEquipmentID(equipmentID);
        List<Infos.StartCassette> startCassettes = new ArrayList<>();
        Infos.StartCassette startCassette = new Infos.StartCassette();
        startCassette.setCassetteID(lotListInCassetteInfo.getCassetteID());
        List<Infos.LotInCassette> lotInCassetteList = new ArrayList<>();
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
                lotWafer.setProcessJobExecFlag(true); // the default value is true
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
        lotsMoveInReserveInfoInqParams.setStartCassettes(startCassettes);
        return dispatchInqController.lotsMoveInReserveInfoInq(lotsMoveInReserveInfoInqParams);
    }


    public Response carrierLoadingRpt(ObjectIdentifier equipmentID, ObjectIdentifier cassetteID, ObjectIdentifier portID, String lotPurposeType){
        Params.loadOrUnloadLotRptParams params = new Params.loadOrUnloadLotRptParams();
        params.setUser(testCommonData.getUSER());
        params.setEquipmentID(equipmentID);
        params.setCassetteID(cassetteID);
        params.setPortID(portID);
        params.setLoadPurposeType(lotPurposeType);
        return equipmentController.carrierLoadingRpt(params);
    }

    public Response moveInReserveReqCase(Results.LotsMoveInReserveInfoInqResult lotsMoveInReserveInfoInqResult, List<String> portPurposts) {
        return this.moveInReserveReqCase(lotsMoveInReserveInfoInqResult, portPurposts, null,null);
    }

    public Response moveInReserveReqCase(Results.LotsMoveInReserveInfoInqResult lotsMoveInReserveInfoInqResult, List<String> portPurposts, ObjectIdentifier portID, String portGroup) {
        Params.MoveInReserveReqParams moveInReserveReqParams = new Params.MoveInReserveReqParams();
        moveInReserveReqParams.setUser(testCommonData.getUSER());
        moveInReserveReqParams.setControlJobID(null);
        moveInReserveReqParams.setEquipmentID(lotsMoveInReserveInfoInqResult.getEquipmentID());
        moveInReserveReqParams.setPortGroupID(testCommonData.getPROTGROUPID());
        if (!CimStringUtils.isEmpty(portGroup)){
            moveInReserveReqParams.setPortGroupID(portGroup);
        }
        List<Infos.StartCassette> strStartCassettes = lotsMoveInReserveInfoInqResult.getStrStartCassette();
        //single cassette
        Infos.StartCassette strStartCassette = strStartCassettes.get(0);
        strStartCassette.setLoadPortID(testCommonData.getPROTID());
        strStartCassette.setUnloadPortID(testCommonData.getPROTID());
        if (!CimObjectUtils.isEmptyWithValue(portID)){
            strStartCassette.setLoadPortID(portID);
            strStartCassette.setUnloadPortID(portID);
        }
        strStartCassette.setLoadSequenceNumber(testCommonData.getLOADSEQUENCENUMBER());
        strStartCassette.setLoadPurposeType(portPurposts.get(0));
        moveInReserveReqParams.setStartCassetteList(strStartCassettes);
        return dispatchController.moveInReserveReq(moveInReserveReqParams);
    }

    public Response moveInReserveReqForMonitorLotAndProductLotCase(Results.LotsMoveInReserveInfoInqResult lotsMoveInReserveInfoInqResult, ObjectIdentifier monitorCassetteID, ObjectIdentifier productCassetteID, List<String> portPurposts, ObjectIdentifier port1, ObjectIdentifier port2) {
        Params.MoveInReserveReqParams moveInReserveReqParams = new Params.MoveInReserveReqParams();
        moveInReserveReqParams.setUser(testCommonData.getUSER());
        moveInReserveReqParams.setControlJobID(null);
        moveInReserveReqParams.setEquipmentID(lotsMoveInReserveInfoInqResult.getEquipmentID());
        moveInReserveReqParams.setPortGroupID(testCommonData.getPROTGROUPID());
        List<Infos.StartCassette> strStartCassettes = lotsMoveInReserveInfoInqResult.getStrStartCassette();
        //muilty cassette
        List<Infos.StartCassette> startCassetteList = new ArrayList<>();
        for (Infos.StartCassette strStartCassette : strStartCassettes) {
            if (CimObjectUtils.equalsWithValue(strStartCassette.getCassetteID(),monitorCassetteID)){
                //monitorCassette
                strStartCassette.setLoadPortID(port1);
                strStartCassette.setUnloadPortID(port1);
                strStartCassette.setLoadSequenceNumber(1L);
                strStartCassette.setLoadPurposeType(portPurposts.get(1));//process monitor lot
                strStartCassette.getLotInCassetteList().get(0).setMonitorLotFlag(true);
                startCassetteList.add(strStartCassette);
            }else {
                //productCassette
                strStartCassette.setLoadPortID(port2);
                strStartCassette.setUnloadPortID(port2);
                strStartCassette.setLoadSequenceNumber(2L);
                strStartCassette.setLoadPurposeType(portPurposts.get(0));//process lot
                startCassetteList.add(strStartCassette);
            }
        }
        moveInReserveReqParams.setStartCassetteList(strStartCassettes);
        return dispatchController.moveInReserveReq(moveInReserveReqParams);
    }

    public Response moveInReserveReqForMonitorLotAndProductLotForInternalBufferCase(Results.LotsMoveInReserveInfoInqResult lotsMoveInReserveInfoInqResult, ObjectIdentifier monitorCassetteID, ObjectIdentifier productCassetteID, List<String> portPurposts, ObjectIdentifier port1, ObjectIdentifier port2, ObjectIdentifier emptyCarrier) {
        Params.MoveInReserveForIBReqParams moveInReserveReqParams = new Params.MoveInReserveForIBReqParams();
        moveInReserveReqParams.setUser(testCommonData.getUSER());
        moveInReserveReqParams.setControlJobID(null);
        moveInReserveReqParams.setEquipmentID(lotsMoveInReserveInfoInqResult.getEquipmentID());
        List<Infos.StartCassette> strStartCassettes = lotsMoveInReserveInfoInqResult.getStrStartCassette();
        //muilty cassette
        List<Infos.StartCassette> startCassetteList = new ArrayList<>();
        if (!CimObjectUtils.isEmptyWithValue(emptyCarrier)){
            Infos.StartCassette emptyStartCassette = new Infos.StartCassette();
            emptyStartCassette.setCassetteID(emptyCarrier);
            emptyStartCassette.setLoadPortID(port1);
            emptyStartCassette.setLoadPurposeType(portPurposts.get(2));
            emptyStartCassette.setLoadSequenceNumber(1L);
            emptyStartCassette.setUnloadPortID(port1);
            startCassetteList.add(emptyStartCassette);
        }
        for (Infos.StartCassette strStartCassette : strStartCassettes) {
            if (CimObjectUtils.equalsWithValue(strStartCassette.getCassetteID(),monitorCassetteID)){
                //monitorCassette
                strStartCassette.setLoadPortID(port1);
                strStartCassette.setUnloadPortID(port1);
                strStartCassette.setLoadSequenceNumber(1L);
                strStartCassette.setLoadPurposeType(portPurposts.get(1));//process monitor lot
                strStartCassette.getLotInCassetteList().get(0).setMonitorLotFlag(true);
                startCassetteList.add(strStartCassette);
            }else {
                //productCassette
                strStartCassette.setLoadPortID(port2);
                strStartCassette.setUnloadPortID(port2);
                strStartCassette.setLoadSequenceNumber(2L);
                strStartCassette.setLoadPurposeType(portPurposts.get(0));//process lot
                startCassetteList.add(strStartCassette);
            }
        }
        moveInReserveReqParams.setStartCassetteList(startCassetteList);
        return dispatchController.moveInReserveForIBReq(moveInReserveReqParams);
    }

    public Response moveInReserveReqCaseWhithSpecifiedLotsAndEqp(List<Infos.StartCassette> strStartCassette, ObjectIdentifier equipmentID, String portGroup) {
        Params.MoveInReserveReqParams moveInReserveReqParams = new Params.MoveInReserveReqParams();
        moveInReserveReqParams.setUser(testCommonData.getUSER());
        moveInReserveReqParams.setControlJobID(null);
        moveInReserveReqParams.setEquipmentID(equipmentID);
        moveInReserveReqParams.setPortGroupID(portGroup);
        moveInReserveReqParams.setStartCassetteList(strStartCassette);
        return dispatchController.moveInReserveReq(moveInReserveReqParams);
    }

    public Response uncarrierLoadingRpt(ObjectIdentifier equipmentID, ObjectIdentifier cassetteID, ObjectIdentifier portID, String lotPurposeType) {
        Params.loadOrUnloadLotRptParams params = new Params.loadOrUnloadLotRptParams();
        params.setUser(testCommonData.getUSER());
        params.setEquipmentID(equipmentID);
        params.setCassetteID(cassetteID);
        params.setPortID(portID);
        params.setLoadPurposeType(lotPurposeType);
        return equipmentController.uncarrierLoadingRpt(params);
    }

    public Response lotsMoveInInfoInq(ObjectIdentifier equipmentID, List<ObjectIdentifier> cassetteIDs){
        Params.LotsMoveInInfoInqParams lotsMoveInInfoInqParams = new Params.LotsMoveInInfoInqParams();
        lotsMoveInInfoInqParams.setUser(testCommonData.getUSER());
        lotsMoveInInfoInqParams.setEquipmentID(equipmentID);
        lotsMoveInInfoInqParams.setCassetteIDs(cassetteIDs);
        return equipmentInqController.LotsMoveInInfoInq(lotsMoveInInfoInqParams);
    }

    public Response movInReq(ObjectIdentifier controlJobID, ObjectIdentifier equipmentID, String portGroupID, boolean processJobPauseFlag, List<Infos.StartCassette> startCassetteList){
        Params.MoveInReqParams moveInReqParams = new Params.MoveInReqParams();
        moveInReqParams.setControlJobID(controlJobID);
        moveInReqParams.setEquipmentID(equipmentID);
        moveInReqParams.setPortGroupID(portGroupID);
        moveInReqParams.setProcessJobPauseFlag(processJobPauseFlag);
        moveInReqParams.setStartCassetteList(startCassetteList);
        moveInReqParams.setUser(testCommonData.getUSER());
        return equipmentController.moveInReq(moveInReqParams);
    }

    public Response moveInCancel(ObjectIdentifier equipmentID, ObjectIdentifier controlJobID){
        Params.MoveInCancelReqParams params = new Params.MoveInCancelReqParams();
        params.setEquipmentID(equipmentID);
        params.setControlJobID(controlJobID);
        params.setUser(testCommonData.getUSER());
        return equipmentController.moveInCancelReq(params);
    }

    public Response moveOut(Params.OpeComWithDataReqParams opeComWithDataReqParams){
        return equipmentController.moveOutReq(opeComWithDataReqParams);
    }

    public ObjectIdentifier loadingLotWithOutStartReserve(ObjectIdentifier equipmentID,ObjectIdentifier lotID) {
        //【step1】einfo/eqp_info/inq
        electronicInformationTestCase.eqpInfoInqCase(equipmentID);
        //【step2】lot info and get cassette id
        List<ObjectIdentifier> lotIDList = new ArrayList<>();
        lotIDList.add(lotID);
        Results.LotInfoInqResult lotInfoInqResult = (Results.LotInfoInqResult) lotGeneralTestCase.getLotInfoCase(lotIDList).getBody();
        Infos.LotListInCassetteInfo lotListInCassetteInfo = lotInfoInqResult.getLotListInCassetteInfo();
        ObjectIdentifier cassetteID = lotListInCassetteInfo.getCassetteID();
        //【step3】load purpose
        ObjectIdentifier portID = new ObjectIdentifier("P1");
        List<String> purposeList = (List<String>) this.getLoadPurposeTypeCase(BizConstant.SP_LOADPURPOSETYPE_PROCESSLOT).getBody();
        this.carrierLoadingRpt(equipmentID, cassetteID,portID, purposeList.get(0));
        return cassetteID;
    }

    public void changeOperationModeToAuto(ObjectIdentifier equipmentID, String autoType){
        //【step1】einfo/eqp_info/inq
        Results.EqpInfoInqResult eqpInfoInqResult = (Results.EqpInfoInqResult) electronicInformationTestCase.eqpInfoInqCase(equipmentID).getBody();
        List<Infos.EqpPortStatus> eqpPortStatuses = eqpInfoInqResult.getEquipmentPortInfo().getEqpPortStatuses();
        String tmpOperationMode = eqpPortStatuses.get(0).getOperationMode();
        if (!CimStringUtils.equals(tmpOperationMode, autoType)){
            //【step2】candidate equipment mode info
            Results.EquipmentModeSelectionInqResult equipmentModeSelectionInqResult = this.getCandidateEqpMode(equipmentID, "OnlineModeChange");
            //【step3】change eqp mode
            Infos.OperationMode theSelectedPortMode = null;
            String portUsage = null;
            for (Infos.CandidatePortMode candidatePortMode : equipmentModeSelectionInqResult.getCandidatePortMode()){
                if (CimStringUtils.equals(candidatePortMode.getPortGroup(), eqpInfoInqResult.getEquipmentPortInfo().getEqpPortStatuses().get(0).getPortGroup())){
                    portUsage = candidatePortMode.getPortUsage();
                    List<Infos.OperationMode> strOperationMode = candidatePortMode.getStrOperationMode();
                    for (Infos.OperationMode operationMode : strOperationMode){
                        if (CimObjectUtils.equalsWithValue(operationMode.getOperationMode(), autoType)){
                            theSelectedPortMode = operationMode;
                            break;
                        }
                    }
                }
            }
            Assert.isTrue(theSelectedPortMode != null, "test fail");
            List<Infos.PortOperationMode> strPortOperationMode = new ArrayList<>();
            for (Infos.EqpPortStatus eqpPortStatus : eqpPortStatuses){
                if (CimStringUtils.equals(eqpPortStatus.getPortGroup(), eqpInfoInqResult.getEquipmentPortInfo().getEqpPortStatuses().get(0).getPortGroup())){
                    Infos.PortOperationMode portOperationMode = new Infos.PortOperationMode();
                    strPortOperationMode.add(portOperationMode);
                    portOperationMode.setOperationMode(theSelectedPortMode);
                    portOperationMode.setPortGroup(eqpPortStatus.getPortGroup());
                    portOperationMode.setPortUsage(portUsage);
                    portOperationMode.setPortID(eqpPortStatus.getAssociatedPortID());
                }
            }
            this.eqpModeChange(equipmentID, strPortOperationMode);
        }
    }

    public void changeOperationModeToOffLine1(ObjectIdentifier equipmentID){
        this.changeOperationModeToOffLine1(equipmentID, null);
    }

    public void changeOperationModeToOffLine1(ObjectIdentifier equipmentID, String equipmentCategory){
        //【step1】einfo/eqp_info/inq
        List<Infos.EqpPortStatus> eqpPortStatuses = null;
        if (CimStringUtils.equals(equipmentCategory, BizConstant.SP_MC_CATEGORY_INTERNALBUFFER)){
            Results.EqpInfoForIBInqResult eqpInfoForIBInqResult = (Results.EqpInfoForIBInqResult) electronicInformationTestCase.EqpInfoForInternalBufferCase(equipmentID.getValue()).getBody();
            eqpPortStatuses = eqpInfoForIBInqResult.getEquipmentPortInfo().getEqpPortStatuses();
        } else {
            Results.EqpInfoInqResult eqpInfoInqResult = (Results.EqpInfoInqResult) electronicInformationTestCase.eqpInfoInqCase(equipmentID).getBody();
            eqpPortStatuses = eqpInfoInqResult.getEquipmentPortInfo().getEqpPortStatuses();
        }
        String tmpOnlineMode = eqpPortStatuses.get(0).getOnlineMode();
        if (!CimStringUtils.equals(tmpOnlineMode, "Off-Line")){
            //【step2】candidate equipment mode info
            Results.EquipmentModeSelectionInqResult equipmentModeSelectionInqResult = this.getCandidateEqpMode(equipmentID, "OnlineModeChange", equipmentCategory);
            //【step3】change eqp mode
            Infos.OperationMode theSelectedPortMode = null;
            String portUsage = null;
            for (Infos.CandidatePortMode candidatePortMode : equipmentModeSelectionInqResult.getCandidatePortMode()){
                if (CimStringUtils.equals(candidatePortMode.getPortGroup(), eqpPortStatuses.get(0).getPortGroup())){
                    portUsage = candidatePortMode.getPortUsage();
                    List<Infos.OperationMode> strOperationMode = candidatePortMode.getStrOperationMode();
                    for (Infos.OperationMode operationMode : strOperationMode){
                        if (CimObjectUtils.equalsWithValue(operationMode.getOperationMode(), "Off-Line-1")){
                            theSelectedPortMode = operationMode;
                            break;
                        }
                    }
                }
            }
            Assert.isTrue(theSelectedPortMode != null, "test fail");
            List<Infos.PortOperationMode> strPortOperationMode = new ArrayList<>();
            for (Infos.EqpPortStatus eqpPortStatus : eqpPortStatuses){
                Infos.PortOperationMode portOperationMode = new Infos.PortOperationMode();
                strPortOperationMode.add(portOperationMode);
                portOperationMode.setOperationMode(theSelectedPortMode);
                portOperationMode.setPortGroup(eqpPortStatus.getPortGroup());
                portOperationMode.setPortUsage(portUsage);
                portOperationMode.setPortID(eqpPortStatus.getAssociatedPortID());
            }
            this.eqpModeChange(equipmentID, strPortOperationMode);
        }
    }

    public Results.EquipmentModeSelectionInqResult getCandidateEqpMode(ObjectIdentifier equipmentID, String modeChangeType){
        return this.getCandidateEqpMode(equipmentID, modeChangeType, null);
    }

    public Results.EquipmentModeSelectionInqResult getCandidateEqpMode(ObjectIdentifier equipmentID, String modeChangeType, String equipmentCategory){
        //【step1】einfo/eqp_info/inq
        List<Infos.EqpPortStatus> eqpPortStatuses = null;
        if (CimStringUtils.equals(equipmentCategory, BizConstant.SP_MC_CATEGORY_INTERNALBUFFER)){
            Results.EqpInfoForIBInqResult eqpInfoForIBInqResult = (Results.EqpInfoForIBInqResult) electronicInformationTestCase.EqpInfoForInternalBufferCase(equipmentID.getValue()).getBody();
            eqpPortStatuses = eqpInfoForIBInqResult.getEquipmentPortInfo().getEqpPortStatuses();
        } else {
            Results.EqpInfoInqResult eqpInfoInqResult = (Results.EqpInfoInqResult) electronicInformationTestCase.eqpInfoInqCase(equipmentID).getBody();
            eqpPortStatuses = eqpInfoInqResult.getEquipmentPortInfo().getEqpPortStatuses();
        }
        List<ObjectIdentifier> portList = eqpPortStatuses.stream().map(Infos.EqpPortStatus::getAssociatedPortID).collect(Collectors.toList());
        Params.EquipmentModeSelectionInqParams equipmentModeSelectionInqParams = new Params.EquipmentModeSelectionInqParams();
        equipmentModeSelectionInqParams.setUser(testCommonData.getUSER());
        equipmentModeSelectionInqParams.setPortID(portList);
        equipmentModeSelectionInqParams.setModeChangeType(modeChangeType);
        equipmentModeSelectionInqParams.setEquipmentID(equipmentID);
        return (Results.EquipmentModeSelectionInqResult) equipmentInqController.equipmentModeSelectionInq(equipmentModeSelectionInqParams).getBody();
    }

    public void eqpModeChange(ObjectIdentifier equipmentID, List<Infos.PortOperationMode> strPortOperationMode){
        Params.EqpModeChangeReqPrams eqpModeChangeReqPrams = new Params.EqpModeChangeReqPrams();
        eqpModeChangeReqPrams.setUser(testCommonData.getUSER());
        eqpModeChangeReqPrams.setNotifyToEAPFlag(false);
        eqpModeChangeReqPrams.setNotifyToEqpFlag(false);
        eqpModeChangeReqPrams.setPortOperationModeList(strPortOperationMode);
        eqpModeChangeReqPrams.setEquipmentID(equipmentID);
        equipmentController.EqpModeChangeReq(eqpModeChangeReqPrams);
    }

    public void changePortStatus(ObjectIdentifier equipmentID, String portStatus){
        this.changePortStatus(equipmentID, null, portStatus);
    }
    public void changePortStatus(ObjectIdentifier equipmentID, List<ObjectIdentifier> portIDs, String portStatus){
        Results.EqpInfoInqResult eqpInfo = electronicInformationTestCase.getEqpInfo(equipmentID);
        if (CimArrayUtils.isEmpty(portIDs)){
            List<Infos.EqpPortStatus> eqpPortStatuses = eqpInfo.getEquipmentPortInfo().getEqpPortStatuses();
            String portGroup = eqpPortStatuses.get(0).getPortGroup();
            portIDs = eqpPortStatuses.stream().filter(eqpPortStatus -> eqpPortStatus.getPortGroup().equals(portGroup)).map(Infos.EqpPortStatus::getPortID).collect(Collectors.toList());
        }
        Params.PortStatusChangeRptParam portStatusChangeRptParam = new Params.PortStatusChangeRptParam();
        portStatusChangeRptParam.setUser(testCommonData.getUSER());
        portStatusChangeRptParam.setEquipmentID(equipmentID);
        List<Infos.EqpPortEventOnTCS> eqpPortEventOnTCSes = new ArrayList<>();
        portStatusChangeRptParam.setEqpPortEventOnEAPesList(eqpPortEventOnTCSes);
        for (ObjectIdentifier portID : portIDs){
            Infos.EqpPortEventOnTCS eqpPortEventOnTCS = new Infos.EqpPortEventOnTCS();
            eqpPortEventOnTCSes.add(eqpPortEventOnTCS);
            eqpPortEventOnTCS.setPortID(portID);
            eqpPortEventOnTCS.setPortStatus(portStatus);
        }
        equipmentController.portStatusChangeRpt(portStatusChangeRptParam);
    }

    public void eqpStatusChange(ObjectIdentifier equipmentID, String e10Status, String equipmentStatusCode){
        Results.EqpStatusSelectionInqResult eqpStatusSelectionInqResult = this.eqpStatusSelectionInqResult(equipmentID, true);
        List<Infos.CandidateE10Status> candidateOtherE10List = eqpStatusSelectionInqResult.getCandidateOtherE10List();
        Map<String, List<Infos.CandidateE10Status>> map = candidateOtherE10List.stream().collect(Collectors.groupingBy(candidateE10Status -> candidateE10Status.getE10Status().getValue()));
        List<Infos.CandidateE10Status> candidateE10StatusListSeleted = map.get(e10Status);
        Assert.isTrue(CimArrayUtils.getSize(candidateE10StatusListSeleted) == 1, "test fail");
        Infos.CandidateE10Status candidateE10Status = candidateE10StatusListSeleted.get(0);
        List<Infos.CandidateEqpStatus> candidateEqpStatusList = candidateE10Status.getCandidateEqpStatusList();
        ObjectIdentifier equipmentStatusCodeSelected = null;
        for (Infos.CandidateEqpStatus candidateEqpStatus : candidateEqpStatusList){
            if (CimObjectUtils.equalsWithValue(candidateEqpStatus.getEquipmentStatusCode(), equipmentStatusCode)){
                equipmentStatusCodeSelected = candidateEqpStatus.getEquipmentStatusCode();
                break;
            }
        }
        Assert.isTrue(equipmentStatusCodeSelected != null, "test fail");
        Params.EqpStatusChangeReqParams eqpStatusChangeReqParams = new Params.EqpStatusChangeReqParams();
        eqpStatusChangeReqParams.setUser(testCommonData.getUSER());
        eqpStatusChangeReqParams.setEquipmentID(equipmentID);
        eqpStatusChangeReqParams.setEquipmentStatusCode(equipmentStatusCodeSelected);
        equipmentController.eqpStatusChangeReq(eqpStatusChangeReqParams);
    }

    public Results.EqpStatusSelectionInqResult eqpStatusSelectionInqResult(ObjectIdentifier equipmentID, boolean allInquiryFlag){
        Params.EqpStatusSelectionInqParams eqpStatusSelectionInqParams = new Params.EqpStatusSelectionInqParams();
        eqpStatusSelectionInqParams.setUser(testCommonData.getUSER());
        eqpStatusSelectionInqParams.setAllInquiryFlag(allInquiryFlag);
        eqpStatusSelectionInqParams.setEquipmentID(equipmentID);
        return (Results.EqpStatusSelectionInqResult) equipmentInqController.eqpStatusSelectionInq(eqpStatusSelectionInqParams).getBody();
    }

    public Results.EqpRecipeParameterListInqResult eqpRecipeParameterListInqCase(Params.EqpRecipeParameterListInq eqpRecipeParameterListInq) {
        eqpRecipeParameterListInq.setUser(testCommonData.getUSER());
        return (Results.EqpRecipeParameterListInqResult) equipmentInqController.eqpRecipeParameterListInq(eqpRecipeParameterListInq).getBody();
    }

    public List<Infos.StoredReticle> keepEquipmentReticleAvailable(ObjectIdentifier equipmentID, int number){
        Results.EqpInfoInqResult eqpInfo = electronicInformationTestCase.getEqpInfo(equipmentID);
        List<Infos.StoredReticle> storedReticleList = eqpInfo.getEquipmentAdditionalReticleAttribute().getStoredReticleList();
        Assert.isTrue(CimArrayUtils.getSize(storedReticleList) > 0, "the equipment you selected is not proper");
        for (Infos.StoredReticle storedReticle : storedReticleList){
            commonTestCase.changeDurableStatus(storedReticle.getReticleID(), "AVAILABLE", "Reticle");
            storedReticle.setStatus("AVAILABLE");
        }
        int needEquipmentInCount = number - CimArrayUtils.getSize(storedReticleList);
        if (needEquipmentInCount > 0) {
            ObjectIdentifier reticleGroupID = storedReticleList.get(0).getReticleGroupID();
            List<Infos.StoredReticle> storedReticleList2 = commonTestCase.reticleEquipmentIn(reticleGroupID, equipmentID, needEquipmentInCount);
            for (Infos.StoredReticle storedReticle : storedReticleList2){
                if (!storedReticle.getStatus().equals("AVAILABLE")){
                    commonTestCase.changeDurableStatus(storedReticle.getReticleID(), "AVAILABLE", "Reticle");
                    storedReticle.setStatus("AVAILABLE");
                }
            }
            storedReticleList.addAll(storedReticleList2);
        }
        return storedReticleList;
    }

    public void runningHold(ObjectIdentifier controlJobID, ObjectIdentifier equipmentID){
        Params.RunningHoldReqParams runningHoldReqParams = new Params.RunningHoldReqParams();
        runningHoldReqParams.setUser(testCommonData.getUSER());
        runningHoldReqParams.setControlJobID(controlJobID);
        runningHoldReqParams.setEquipmentID(equipmentID);
        runningHoldReqParams.setHoldReasonCodeID(new ObjectIdentifier("RNHL"));
        equipmentController.runningHoldReq(runningHoldReqParams);
    }

}