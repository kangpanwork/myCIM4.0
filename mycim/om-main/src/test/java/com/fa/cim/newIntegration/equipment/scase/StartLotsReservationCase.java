package com.fa.cim.newIntegration.equipment.scase;

import com.alibaba.fastjson.JSONObject;
import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.Response;
import com.fa.cim.common.support.User;
import com.fa.cim.common.utils.CimArrayUtils;
import com.fa.cim.common.utils.CimObjectUtils;
import com.fa.cim.common.utils.CimStringUtils;
import com.fa.cim.common.utils.Validations;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.controller.dispatch.DispatchController;
import com.fa.cim.controller.durable.DurableInqController;
import com.fa.cim.controller.einfo.ElectronicInformationInqController;
import com.fa.cim.controller.equipment.EquipmentController;
import com.fa.cim.controller.equipment.EquipmentInqController;
import com.fa.cim.core.TestCommonData;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;
import com.fa.cim.newIntegration.LotOperation.scase.LotHoldCase;
import com.fa.cim.newIntegration.LotOperation.scase.ScrapCase;
import com.fa.cim.newIntegration.dto.TestInfos;
import com.fa.cim.newIntegration.stb.scase.STBCase;
import com.fa.cim.newIntegration.tcase.ElectronicInformationTestCase;
import com.fa.cim.newIntegration.tcase.EquipmentTestCase;
import com.fa.cim.newIntegration.tcase.LotGeneralTestCase;
import com.fa.cim.newIntegration.tcase.OperationSkipTestCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/9/4          ********            Nyx                create file
 *
 * @author: Nyx
 * @date: 2019/9/4 16:17
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Service
@Slf4j
public class StartLotsReservationCase {

    @Autowired
    @Qualifier("testCommonDataWithDB")
    private TestCommonData testCommonData;
    @Autowired
    private STBCase stbCase;
    @Autowired
    private EquipmentTestCase equipmentTestCase;
    @Autowired
    private LotGeneralTestCase lotGeneralTestCase;
    @Autowired
    private ElectronicInformationTestCase electronicInformationTestCase;

    @Autowired
    private DispatchController dispatchController;

    @Autowired
    private LotHoldCase lotHoldCase;

    @Autowired
    private DurableInqController durableInqController;

    @Autowired
    private MoveInCase moveInCase;

    @Autowired
    private EquipmentInqController equipmentInqController;

    @Autowired
    private RetCodeConfig retCodeConfig;

    @Autowired
    private EquipmentController equipmentController;

    @Autowired
    private OperationSkipTestCase operationSkipTestCase;

    @Autowired
    private ScrapCase scrapCase;

    @Autowired
    private ElectronicInformationInqController electronicInformationInqController;

    public ObjectIdentifier startLotsReservation() {
        //1. lotstart/stb_released_lot/req
        ObjectIdentifier lotID = ((Results.WaferLotStartReqResult) stbCase.STB_NotPreparedLot().getBody()).getLotID();

        //2. einfo/eqp_info/inq
        electronicInformationTestCase.eqpInfoInqCase(testCommonData.getEQUIPMENTID());

        //3. dispatch/what_next_lot_list/inq
        Results.WhatNextLotListResult whatNextLotListResult = (Results.WhatNextLotListResult) equipmentTestCase.whatNextInqCase(testCommonData.getEQUIPMENTID()).getBody();
        List<Infos.WhatNextAttributes> whatNextAttributesContent = whatNextLotListResult.getWhatNextAttributesPage().getContent();
        Infos.WhatNextAttributes whatNextAttributes = whatNextAttributesContent.stream().filter(x -> CimObjectUtils.equalsWithValue(x.getLotID(), lotID)).findFirst().orElse(null);
        if (whatNextAttributes == null) {
            return null;
        }
        //4. einfo/lot_list_in_cassette/inq
        Results.LotListByCarrierInqResult lotListByCarrierInqResult = (Results.LotListByCarrierInqResult) electronicInformationTestCase.lotListByCarrierInqCase(whatNextAttributes.getCassetteID()).getBody();

        //5.einfo/eqp_info/inq
        electronicInformationTestCase.eqpInfoInqCase(testCommonData.getEQUIPMENTID());

        //6.einfo/lot_info/inq
        Results.LotInfoInqResult lotInfoInqResult = (Results.LotInfoInqResult) lotGeneralTestCase.getLotInfoCase(lotListByCarrierInqResult.getLotListInCassetteInfo().getLotIDList()).getBody();

        //7.dispatch/lots_info_for_start_reservation/inq
        Results.LotsMoveInReserveInfoInqResult lotsMoveInReserveInfoInqResult = (Results.LotsMoveInReserveInfoInqResult) equipmentTestCase.lotsMoveInReserveInfoInqCase(lotInfoInqResult).getBody();

        //8. eqp/load_purpose_type/inq
        List<String> portPurposts = (List<String>) equipmentTestCase.getLoadPurposeTypeCase(BizConstant.SP_LOADPURPOSETYPE_PROCESSLOT).getBody();

        //9. dispatch/start_lots_reservation/req
        equipmentTestCase.moveInReserveReqCase(lotsMoveInReserveInfoInqResult, portPurposts);

        return whatNextAttributes.getCassetteID();
    }

    public Response startLotsReserve(ObjectIdentifier lotID) {
        return startLotsReserve(lotID,testCommonData.getEQUIPMENTID());
    }

    public Response startLotsReserve(ObjectIdentifier lotID, ObjectIdentifier equipmentID) {
        //2. einfo/eqp_info/inq
        electronicInformationTestCase.eqpInfoInqCase(equipmentID);

        //3. dispatch/what_next_lot_list/inq
        Results.WhatNextLotListResult whatNextLotListResult = (Results.WhatNextLotListResult) equipmentTestCase.whatNextInqCase(equipmentID).getBody();
        List<Infos.WhatNextAttributes> whatNextAttributesContent = whatNextLotListResult.getWhatNextAttributesPage().getContent();
        Infos.WhatNextAttributes whatNextAttributes = whatNextAttributesContent.stream().filter(x -> CimObjectUtils.equalsWithValue(x.getLotID(), lotID)).findFirst().orElse(null);
        if (whatNextAttributes == null) {
            return null;
        }
        //4. einfo/lot_list_in_cassette/inq
        Results.LotListByCarrierInqResult lotListByCarrierInqResult = (Results.LotListByCarrierInqResult) electronicInformationTestCase.lotListByCarrierInqCase(whatNextAttributes.getCassetteID()).getBody();

        //5.einfo/eqp_info/inq
        electronicInformationTestCase.eqpInfoInqCase(equipmentID);

        //6.einfo/lot_info/inq
        Results.LotInfoInqResult lotInfoInqResult = (Results.LotInfoInqResult) lotGeneralTestCase.getLotInfoCase(lotListByCarrierInqResult.getLotListInCassetteInfo().getLotIDList()).getBody();

        //7.dispatch/lots_info_for_start_reservation/inq
        Results.LotsMoveInReserveInfoInqResult lotsMoveInReserveInfoInqResult = (Results.LotsMoveInReserveInfoInqResult) equipmentTestCase.lotsMoveInReserveInfoInqCase(lotInfoInqResult,equipmentID, lotID).getBody();

        //8. eqp/load_purpose_type/inq
        List<String> portPurposts = (List<String>) equipmentTestCase.getLoadPurposeTypeCase(BizConstant.SP_LOADPURPOSETYPE_PROCESSLOT).getBody();

        //9. dispatch/start_lots_reservation/req
        return equipmentTestCase.moveInReserveReqCase(lotsMoveInReserveInfoInqResult, portPurposts);
    }

    public void reserveFoupToNotIncludeFoupPort(){
        String bankID = "BNK-0S";
        String sourceProductID = "RAW-2000.01";
        String productID = "PRD-VCT2.01";
        Results.WaferLotStartReqResult waferLotStartReqResult= (Results.WaferLotStartReqResult) stbCase.STB_PreparedLotForFoup(bankID, sourceProductID, productID).getBody();
        // 2. skip 1000.0300
        Results.LotInfoInqResult lotInfoInqResult= (Results.LotInfoInqResult) lotGeneralTestCase.getLotInfoCase(CimArrayUtils.generateList(waferLotStartReqResult.getLotID())).getBody();
        Results.LotOperationSelectionInqResult lotOperationSelectionInqResult= (Results.LotOperationSelectionInqResult) lotGeneralTestCase.getLotOperationSelectionInq(waferLotStartReqResult.getLotID(),false,true,true).getBody();
        Infos.LotOperationInfo lotOperationInfo = lotInfoInqResult.getLotInfoList().get(0).getLotOperationInfo();
        Infos.OperationNameAttributes operationNameAttributes = lotOperationSelectionInqResult.getOperationNameAttributesAttributes().getContent().stream().filter(operationNameAttributes1 -> "1000.0300".equals(operationNameAttributes1.getOperationNumber())).findFirst().get();
        Params.SkipReqParams skipReqParams= JSONObject.parseObject("{\"currentOperationNumber\":\"1000.0100\",\"currentRouteID\":{\"referenceKey\":\"OMPRP.49867291127402983\",\"value\":\"LAYER0MA.01\"},\"locateDirection\":true,\"lotID\":{\"referenceKey\":\"FRLOT.252734301664182272\",\"value\":\"NP000318.00A\"},\"operationID\":{\"referenceKey\":\"OMPRP.88321534610297170\",\"value\":\"FST010.01\"},\"operationNumber\":\"1000.0300\",\"processRef\":{\"mainProcessFlow\":\"OMPRF.66369402815281944\",\"moduleNumber\":\"2000\",\"modulePOS\":\"OMPRSS.24145333485278681\",\"moduleProcessFlow\":\"OMPRF.54939159889810491\",\"processFlow\":\"OMPRF.49867748958157252\",\"processOperationSpecification\":\"OMPRSS.19491816993363067\",\"siInfo\":null},\"routeID\":{\"referenceKey\":\"OMPRP.49867291127402983\",\"value\":\"LAYER0MA.01\"},\"seqno\":-1,\"sequenceNumber\":0,\"user\":{\"userID\":{\"value\":\"ADMIN\",\"referenceKey\":\"\"},\"password\":\"b51fa595e692d53739b69131cdc73440\",\"functionID\":\"OLOTW003\"}}",
                Params.SkipReqParams.class);
        skipReqParams.setCurrentRouteID(lotOperationInfo.getRouteID());
        skipReqParams.setLotID(waferLotStartReqResult.getLotID());
        skipReqParams.setOperationID(lotOperationInfo.getOperationID());
        skipReqParams.setProcessRef(operationNameAttributes.getProcessRef());
        skipReqParams.setRouteID(operationNameAttributes.getRouteID());
        operationSkipTestCase.operationSkip(skipReqParams);

        try {
            startLotsReserve(waferLotStartReqResult.getLotID(),ObjectIdentifier.buildWithValue("1CMS03-CU"));
            Assert.isTrue(false,"Error: Reserve a carrier with Carrier Type \"FOUP\" to a port whose compatible carrier cagetory does not include \"FOUP\"");
        }catch (ServiceException ex){
            Assert.isTrue(Validations.isEquals(retCodeConfig.getInvalidCategoryCheck(),ex.getCode()),ex.getMessage());
        }
    }

    public List<ObjectIdentifier> moveInReserveReqWhithSpecifiedLotsAndEqp(List<ObjectIdentifier> lots, ObjectIdentifier equipmentID) {
        return this.moveInReserveReqWhithSpecifiedLotsAndEqp(lots, equipmentID, 0);
    }

    public List<ObjectIdentifier> moveInReserveReqWhithSpecifiedLotsAndEqp(List<ObjectIdentifier> lots, ObjectIdentifier equipmentID, int emptyCarrieNumber){
        //1. einfo/eqp_info/inq
        Results.EqpInfoInqResult eqpInfoInqResult = (Results.EqpInfoInqResult) electronicInformationTestCase.eqpInfoInqCase(equipmentID).getBody();

        //2. dispatch/what_next_lot_list/inq
        Results.WhatNextLotListResult whatNextLotListResult = (Results.WhatNextLotListResult) equipmentTestCase.whatNextInqCase(equipmentID).getBody();
        List<Infos.WhatNextAttributes> whatNextAttributesContent = (List<Infos.WhatNextAttributes> ) whatNextLotListResult.getWhatNextAttributesPage().getContent();
        List<ObjectIdentifier> whatNextAttributesLots = new ArrayList<>();
        List<ObjectIdentifier> cassettes = new ArrayList<>();
        whatNextAttributesContent.forEach(whatNextAttributes -> {
            whatNextAttributesLots.add(whatNextAttributes.getLotID());
            if (lots.contains(whatNextAttributes.getLotID())){
                cassettes.add(whatNextAttributes.getCassetteID());
            }
        });
        Assert.isTrue(whatNextAttributesLots.containsAll(lots), "don't found specified lots");
        //3. eqp/load_purpose_type/inq
        List<String> portPurposts = (List<String>) equipmentTestCase.getLoadPurposeTypeCase(BizConstant.SP_LOADPURPOSETYPE_PROCESSLOT).getBody();
        List<Infos.StartCassette> strStartCassette = new ArrayList<>();
        int count = 0;
        for (ObjectIdentifier cassette : cassettes){
            //4. einfo/lot_list_in_cassette/inq
            Results.LotListByCarrierInqResult lotListByCarrierInqResult = (Results.LotListByCarrierInqResult) electronicInformationTestCase.lotListByCarrierInqCase(cassette).getBody();

            //5.einfo/lot_info/inq
            Results.LotInfoInqResult lotInfoInqResult = (Results.LotInfoInqResult) lotGeneralTestCase.getLotInfoCase(lotListByCarrierInqResult.getLotListInCassetteInfo().getLotIDList()).getBody();

            //6.dispatch/lots_info_for_start_reservation/inq
            Results.LotsMoveInReserveInfoInqResult lotsMoveInReserveInfoInqResult = (Results.LotsMoveInReserveInfoInqResult) equipmentTestCase.lotsMoveInReserveInfoInqCaseWithSpecifiedEqp(lotInfoInqResult, equipmentID).getBody();
            Infos.StartCassette tmpStartCassette = lotsMoveInReserveInfoInqResult.getStrStartCassette().get(0);
            tmpStartCassette.setLoadPortID(eqpInfoInqResult.getEquipmentPortInfo().getEqpPortStatuses().get(count).getPortID());
            tmpStartCassette.setUnloadPortID(eqpInfoInqResult.getEquipmentPortInfo().getEqpPortStatuses().get(count).getPortID());
            tmpStartCassette.setLoadSequenceNumber(count + 1L);
            tmpStartCassette.setLoadPurposeType(portPurposts.get(0));
            strStartCassette.add(tmpStartCassette);
            count++;
        }
        if (emptyCarrieNumber > 0){
            Params.CarrierListInqParams carrierListInqParams=JSONObject.parseObject("{\"maxRetrieveCount\":10,\"cassetteID\":{},\"bankID\":{},\"emptyFlag\":true,\"searchCondition\":{\"size\":10,\"page\":1,\"conditions\":[]},\"user\":{\"userID\":{\"value\":\"ADMIN\",\"referenceKey\":\"\"},\"password\":\"b51fa595e692d53739b69131cdc73440\",\"functionID\":\"OBNKW001\"}}", Params.CarrierListInqParams.class);
            Results.CarrierListInq170Result carrierListInq170Result= (Results.CarrierListInq170Result) durableInqController.carrierListInq(carrierListInqParams).getBody();
            for (int i = 0; i < emptyCarrieNumber; i++){
                Infos.StartCassette startCassette=new Infos.StartCassette();
                startCassette.setCassetteID(carrierListInq170Result.getFoundCassette().getContent().get(i).getCassetteID());
                startCassette.setLoadPurposeType(BizConstant.SP_LOADPURPOSETYPE_EMPTYCASSETTE);
                startCassette.setLoadPortID(eqpInfoInqResult.getEquipmentPortInfo().getEqpPortStatuses().get(count).getPortID());
                startCassette.setUnloadPortID(eqpInfoInqResult.getEquipmentPortInfo().getEqpPortStatuses().get(count).getPortID());
                startCassette.setLoadSequenceNumber(count + 1L);
                strStartCassette.add(startCassette);
                count++;
            }
        }
        List<Infos.EqpPortStatus> eqpPortStatuses = eqpInfoInqResult.getEquipmentPortInfo().getEqpPortStatuses();
        Map<String, List<Infos.EqpPortStatus>> portGroupMap = eqpPortStatuses.stream().collect(Collectors.groupingBy(Infos.EqpPortStatus::getPortGroup));
        AtomicReference<String> portGroupSeletced = new AtomicReference<>();
        portGroupMap.forEach((k, v) ->{
            if (v.size() >= lots.size()){
                portGroupSeletced.set(k);
            }
        });
        //7. dispatch/start_lots_reservation/req
        equipmentTestCase.moveInReserveReqCaseWhithSpecifiedLotsAndEqp(strStartCassette, equipmentID, portGroupSeletced.get());
        return strStartCassette.stream().map(Infos.StartCassette::getCassetteID).collect(Collectors.toList());
    }

    public void startReserve_twoLotsAtOneTime(){
        ObjectIdentifier eqpID = testCommonData.getEQUIPMENTID();
        startReserve_TwoLots(eqpID);
    }

    public void startReserve_TwoLots(ObjectIdentifier eqpID){
        int n=2;
        // 1. stb 2 lots
        List<ObjectIdentifier> lots = stbCase.stb_NLots_NotPreparedCase(n, true);

        // 2. eqp_info
        electronicInformationTestCase.eqpInfoInqCase(eqpID);

        // 3. what's next List

        Results.WhatNextLotListResult whatNextLotListResult= (Results.WhatNextLotListResult) equipmentTestCase.whatNextInqCase(eqpID).getBody();
        List<ObjectIdentifier> cassettes=whatNextLotListResult.getWhatNextAttributesPage().getContent().stream().filter(whatNextAttributes -> {
            return lots.stream().anyMatch(lot -> lot.equals(whatNextAttributes.getLotID().getValue()));
        }).map(whatNextAttributes -> whatNextAttributes.getCassetteID()).collect(Collectors.toList());

        // 4. lot list in cassette inq
        cassettes.stream().map(cassette-> electronicInformationTestCase.lotListByCarrierInqCase(cassette)).collect(Collectors.toList());

        // 5. eqp info inq
        Results.EqpInfoInqResult eqpInfoInqResult= (Results.EqpInfoInqResult) electronicInformationTestCase.eqpInfoInqCase(eqpID).getBody();

        // 6. lot info inq
        Results.LotInfoInqResult lotInfoInqResult= (Results.LotInfoInqResult) lotGeneralTestCase.getLotInfoCase(CimArrayUtils.generateList(lots.get(0))).getBody();
        Results.LotInfoInqResult lotInfoInqResult2= (Results.LotInfoInqResult) lotGeneralTestCase.getLotInfoCase(CimArrayUtils.generateList(lots.get(1))).getBody();

        // 7. start reserve inq
        Results.LotsMoveInReserveInfoInqResult lotsMoveInReserveInfoInqResult= (Results.LotsMoveInReserveInfoInqResult) equipmentTestCase.lotsMoveInReserveInfoInqCase(lotInfoInqResult).getBody();
        Results.LotsMoveInReserveInfoInqResult lotsMoveInReserveInfoInqResult2= (Results.LotsMoveInReserveInfoInqResult) equipmentTestCase.lotsMoveInReserveInfoInqCase(lotInfoInqResult2).getBody();

        // 8. start reserve req
        Params.MoveInReserveReqParams moveInReserveReqParams=new Params.MoveInReserveReqParams();
        moveInReserveReqParams.setUser(testCommonData.getUSER());
        moveInReserveReqParams.setEquipmentID(eqpID);
        moveInReserveReqParams.setPortGroupID(testCommonData.getPROTGROUPID());
        List<Infos.EqpPortStatus> eqpPortStatuses = eqpInfoInqResult.getEquipmentPortInfo().getEqpPortStatuses()/*.stream().filter(eqpPortStatus -> {
            return eqpPortStatus.getPortGroup().equals(testCommonData.getPROTGROUPID());
        }).collect(Collectors.toList())*/;
        List<Infos.StartCassette> strStartCassetteList=new ArrayList<>();
        Infos.EqpPortStatus eqpPortStatuse = eqpPortStatuses.get(0);
        for (Infos.StartCassette strStartCassette:lotsMoveInReserveInfoInqResult.getStrStartCassette()){
            strStartCassetteList.add(strStartCassette);
            strStartCassette.setLoadPortID(eqpPortStatuse.getPortID());
            strStartCassette.setUnloadPortID(eqpPortStatuse.getPortID());
            strStartCassette.setLoadSequenceNumber(eqpPortStatuse.getLoadSequenceNumber());
            strStartCassette.setLoadPurposeType(BizConstant.SP_LOADPURPOSETYPE_PROCESSLOT);
        }
        eqpPortStatuse = eqpPortStatuses.get(1);
        for (Infos.StartCassette strStartCassette:lotsMoveInReserveInfoInqResult2.getStrStartCassette()){
            strStartCassetteList.add(strStartCassette);
            strStartCassette.setLoadPortID(eqpPortStatuse.getPortID());
            strStartCassette.setUnloadPortID(eqpPortStatuse.getPortID());
            strStartCassette.setLoadSequenceNumber(eqpPortStatuse.getLoadSequenceNumber());
            strStartCassette.setLoadPurposeType(BizConstant.SP_LOADPURPOSETYPE_PROCESSLOT);
        }
        moveInReserveReqParams.setStartCassetteList(strStartCassetteList);
        dispatchController.moveInReserveReq(moveInReserveReqParams);
    }

    public void reserveAlreadyReserved(){
        // 1. lotstart/stb_released_lot/req
        ObjectIdentifier lotID = ((Results.WaferLotStartReqResult) stbCase.STB_NotPreparedLot().getBody()).getLotID();

        // 2. first reserve
        startLotsReserve(lotID);

        // 3. two reserve
        try {
            startLotsReserve(lotID);
        } catch (ServiceException ex){
            Assert.isTrue(Validations.isEquals(ex.getCode(),retCodeConfig.getCassetteControlJobFilled()),ex.getMessage());
        }

    }

    public void reserveDifferentPortGroup(){
        ObjectIdentifier eqpID = ObjectIdentifier.buildWithValue("1BKD01");
        try {
            startReserve_TwoLots(eqpID);
            Assert.isTrue(false,"not should success!!");
        } catch (ServiceException ex){
            Assert.isTrue(Validations.isEquals(retCodeConfig.getInvalidLoadingSequence(),ex.getCode())||
                    Validations.isEquals(retCodeConfig.getPortGroupMixed(),ex.getCode()),ex.getMessage());
        }
    }

    public void reserveCarrierToPortWhichIsNotUnderLoadReqOrLoadAvailStatus(){
        ObjectIdentifier equipmentID=testCommonData.getEQUIPMENTID();
        User user = testCommonData.getUSER();

        // 0. change eqp mode
        Results.EqpInfoInqResult eqpInfoInqResult = (Results.EqpInfoInqResult) electronicInformationTestCase.eqpInfoInqCase(equipmentID).getBody();
        List<ObjectIdentifier> portIDs = eqpInfoInqResult.getEquipmentPortInfo().getEqpPortStatuses().stream().map(eqpPortStatus -> eqpPortStatus.getPortID()).collect(Collectors.toList());

        Params.EquipmentModeSelectionInqParams equipmentModeSelectionInqParams=new Params.EquipmentModeSelectionInqParams();
        equipmentModeSelectionInqParams.setEquipmentID(equipmentID);
        equipmentModeSelectionInqParams.setModeChangeType("OnlineModeChange");
        equipmentModeSelectionInqParams.setPortID(portIDs);
        equipmentModeSelectionInqParams.setUser(user);
        Results.EquipmentModeSelectionInqResult equipmentModeSelectionInqResult= (Results.EquipmentModeSelectionInqResult) equipmentInqController.equipmentModeSelectionInq(equipmentModeSelectionInqParams).getBody();

        List<Infos.PortOperationMode> operationModes = equipmentModeSelectionInqResult.getCandidatePortMode().stream().map(candidatePortMode -> {
            Infos.OperationMode operationMode1 = candidatePortMode.getStrOperationMode().stream().filter(operationMode -> operationMode.getOperationMode().equals("Manual-Comp-1")).findFirst().get();
            Infos.PortOperationMode portOperationMode=new Infos.PortOperationMode();
            portOperationMode.setPortID(candidatePortMode.getPortID());
            portOperationMode.setOperationMode(operationMode1);
            portOperationMode.setPortGroup(candidatePortMode.getPortGroup());
            portOperationMode.setPortUsage(candidatePortMode.getPortUsage());
            return portOperationMode;
        }).collect(Collectors.toList());

        Params.EqpModeChangeReqPrams eqpModeChangeReqPrams=new Params.EqpModeChangeReqPrams();
        eqpModeChangeReqPrams.setEquipmentID(equipmentID);
        eqpModeChangeReqPrams.setPortOperationModeList(operationModes);
        eqpModeChangeReqPrams.setNotifyToEqpFlag(false);
        eqpModeChangeReqPrams.setNotifyToEAPFlag(false);
        eqpModeChangeReqPrams.setUser(user);
        equipmentController.EqpModeChangeReq(eqpModeChangeReqPrams);

        // 1. change eqp Port state loadComp
        Params.PortStatusChangeRptParam portStatusChangeRptParam=new Params.PortStatusChangeRptParam();
        portStatusChangeRptParam.setEquipmentID(equipmentID);
        portStatusChangeRptParam.setUser(user);
        portStatusChangeRptParam.setOpeMemo("false");
        Infos.EqpPortEventOnTCS eqpPortEvnet=new Infos.EqpPortEventOnTCS();
        eqpPortEvnet.setPortID(ObjectIdentifier.buildWithValue("P1"));
        eqpPortEvnet.setPortStatus("LoadComp");
        eqpPortEvnet.setCassetteID(new ObjectIdentifier());
        eqpPortEvnet.setLotID(new ObjectIdentifier());
        portStatusChangeRptParam.setEqpPortEventOnEAPesList(CimArrayUtils.generateList(eqpPortEvnet));
        equipmentController.portStatusChangeRpt(portStatusChangeRptParam);

        // 2. lotstart/stb_released_lot/req
        ObjectIdentifier lotID = ((Results.WaferLotStartReqResult) stbCase.STB_NotPreparedLot().getBody()).getLotID();

        // 3.  start reserve
        try {
            startLotsReserve(lotID);
            Assert.isTrue(false,"DIS4-1-10 Reserve carrier to port which is not under LoadReq or LoadAvail status");
        } catch (ServiceException ex){
            Assert.isTrue(Validations.isEquals(retCodeConfig.getInvalidPortState(),ex.getCode()),ex.getMessage());
        }
    }

    public void ReserveALotWhoseLotHoldStateIsOnHold(){
        //【step1】stb
        Response response = stbCase.STB_NotPreparedLot();
        Results.WaferLotStartReqResult body = (Results.WaferLotStartReqResult) response.getBody();
        ObjectIdentifier lotID = body.getLotID();

        final String reasonCode = "SOHL";
        final String reasonableOperation = "C";
        //【step2】lotHold
        lotHoldCase.lotHold(lotID.getValue(),reasonCode,reasonableOperation);
        // start Reserve
        try {
            startLotsReserve(lotID);
            Assert.isTrue(false,"The lot with onhold status does not display");
        }catch (NoSuchElementException ex){
        }
    }

    public void ReserveALotWhoseLotProcessStateIsNotWaiting(){
        Results.WaferLotStartReqResult waferLotStartReqResult= (Results.WaferLotStartReqResult) stbCase.STB_NotPreparedLot().getBody();
        ObjectIdentifier lotID=waferLotStartReqResult.getLotID();
        moveInCase.moveIn_GenerateControlJob_Without_StartReservation(lotID,testCommonData.getEQUIPMENTID());
        try {
            startLotsReserve(lotID);
            Assert.isTrue(false,"The lot did not show there");
        }catch (NoSuchElementException ex){
        }
    }

    public void whenMonitorCreationFlagOfEquipmentIsTrueReserveCarrierWithAnotherLoadedEmptyCarrier(){
        // 1. stb a lot
        Results.WaferLotStartReqResult waferLotStartReqResult= (Results.WaferLotStartReqResult) stbCase.STB_NotPreparedLot().getBody();

        ObjectIdentifier equipmentID = ObjectIdentifier.buildWithValue("1FHI02_NORM");

        // 2. skip 2000.0200
        Results.LotInfoInqResult lotInfoInqResult= (Results.LotInfoInqResult) lotGeneralTestCase.getLotInfoCase(CimArrayUtils.generateList(waferLotStartReqResult.getLotID())).getBody();
        Results.LotOperationSelectionInqResult lotOperationSelectionInqResult= (Results.LotOperationSelectionInqResult) lotGeneralTestCase.getLotOperationSelectionInq(waferLotStartReqResult.getLotID(),false,true,true).getBody();
        Infos.LotOperationInfo lotOperationInfo = lotInfoInqResult.getLotInfoList().get(0).getLotOperationInfo();
        Infos.OperationNameAttributes operationNameAttributes = lotOperationSelectionInqResult.getOperationNameAttributesAttributes().getContent().stream().filter(operationNameAttributes1 -> "2000.0200".equals(operationNameAttributes1.getOperationNumber())).findFirst().get();
        Params.SkipReqParams skipReqParams= JSONObject.parseObject("{\"currentOperationNumber\":\"1000.0100\",\"currentRouteID\":{\"referenceKey\":\"OMPRP.49867291127402983\",\"value\":\"LAYER0MA.01\"},\"locateDirection\":true,\"lotID\":{\"referenceKey\":\"FRLOT.252734301664182272\",\"value\":\"NP000318.00A\"},\"operationID\":{\"referenceKey\":\"OMPRP.88321534610297170\",\"value\":\"FST010.01\"},\"operationNumber\":\"1000.0300\",\"processRef\":{\"mainProcessFlow\":\"OMPRF.66369402815281944\",\"moduleNumber\":\"2000\",\"modulePOS\":\"OMPRSS.24145333485278681\",\"moduleProcessFlow\":\"OMPRF.54939159889810491\",\"processFlow\":\"OMPRF.49867748958157252\",\"processOperationSpecification\":\"OMPRSS.19491816993363067\",\"siInfo\":null},\"routeID\":{\"referenceKey\":\"OMPRP.49867291127402983\",\"value\":\"LAYER0MA.01\"},\"seqno\":-1,\"sequenceNumber\":0,\"user\":{\"userID\":{\"value\":\"ADMIN\",\"referenceKey\":\"\"},\"password\":\"b51fa595e692d53739b69131cdc73440\",\"functionID\":\"OLOTW003\"}}",
                Params.SkipReqParams.class);
        skipReqParams.setCurrentRouteID(lotOperationInfo.getRouteID());
        skipReqParams.setLotID(waferLotStartReqResult.getLotID());
        skipReqParams.setOperationID(lotOperationInfo.getOperationID());
        skipReqParams.setProcessRef(operationNameAttributes.getProcessRef());
        skipReqParams.setRouteID(operationNameAttributes.getRouteID());
        skipReqParams.setOperationNumber(operationNameAttributes.getOperationNumber());
        skipReqParams.setCurrentOperationNumber(lotOperationInfo.getOperationNumber());
        operationSkipTestCase.operationSkip(skipReqParams);

        // 3. start Reserve
        Results.LotsMoveInReserveInfoInqResult lotsMoveInReserveInfoInqResult= (Results.LotsMoveInReserveInfoInqResult) equipmentTestCase.lotsMoveInReserveInfoInqCase(lotInfoInqResult,equipmentID).getBody();

        Params.CarrierListInqParams carrierListInqParams=JSONObject.parseObject("{\"maxRetrieveCount\":10,\"cassetteID\":{},\"bankID\":{},\"emptyFlag\":true,\"searchCondition\":{\"size\":10,\"page\":1,\"conditions\":[]},\"user\":{\"userID\":{\"value\":\"ADMIN\",\"referenceKey\":\"\"},\"password\":\"b51fa595e692d53739b69131cdc73440\",\"functionID\":\"OBNKW001\"}}", Params.CarrierListInqParams.class);
        Results.CarrierListInq170Result carrierListInq170Result= (Results.CarrierListInq170Result) durableInqController.carrierListInq(carrierListInqParams).getBody();

        Infos.StartCassette startCassette=new Infos.StartCassette();
        startCassette.setCassetteID(carrierListInq170Result.getFoundCassette().getContent().get(0).getCassetteID());
        startCassette.setLoadPurposeType("Empty Cassette");
        startCassette.setLotInCassetteList(new ArrayList<>());

        Results.EqpInfoInqResult eqpInfoInqResult= (Results.EqpInfoInqResult) electronicInformationTestCase.eqpInfoInqCase(equipmentID).getBody();

        List<Infos.StartCassette> startCassetteList=lotsMoveInReserveInfoInqResult.getStrStartCassette();

        List<ObjectIdentifier> portIDs=eqpInfoInqResult.getEquipmentPortInfo().getEqpPortStatuses().stream().map(
                        eqpPortStatus -> eqpPortStatus.getPortID()).collect(Collectors.toList());

        startCassetteList.get(0).setLoadPurposeType(BizConstant.SP_LOADPURPOSETYPE_PROCESSLOT);

        startCassetteList.add(startCassette);

        for (int i = 0; i < startCassetteList.size(); i++) {
            startCassette=startCassetteList.get(i);
            startCassette.setLoadSequenceNumber(i+1L);
            startCassette.setLoadPortID(portIDs.get(i));
            startCassette.setUnloadPortID(portIDs.get(i));
        }

        Params.MoveInReserveReqParams moveInReserveReqParams=new Params.MoveInReserveReqParams();
        moveInReserveReqParams.setEquipmentID(equipmentID);
        moveInReserveReqParams.setPortGroupID("PG1");
        moveInReserveReqParams.setUser(testCommonData.getUSER());
        moveInReserveReqParams.setStartCassetteList(startCassetteList);
        dispatchController.moveInReserveReq(moveInReserveReqParams);
        this.whenMonitorCreationFlagOfEquipmentIsTrueReserveCarrierWithAnotherLoadedEmptyCarrier(equipmentID, lotInfoInqResult);
    }

    public List<ObjectIdentifier> whenMonitorCreationFlagOfEquipmentIsTrueReserveCarrierWithAnotherLoadedEmptyCarrier(ObjectIdentifier equipmentID, Results.LotInfoInqResult lotInfoInqResult){
        Results.LotsMoveInReserveInfoInqResult lotsMoveInReserveInfoInqResult= (Results.LotsMoveInReserveInfoInqResult) equipmentTestCase.lotsMoveInReserveInfoInqCase(lotInfoInqResult,equipmentID).getBody();

        Params.CarrierListInqParams carrierListInqParams=JSONObject.parseObject("{\"maxRetrieveCount\":10,\"cassetteID\":{},\"bankID\":{},\"emptyFlag\":true,\"searchCondition\":{\"size\":10,\"page\":1,\"conditions\":[]},\"user\":{\"userID\":{\"value\":\"ADMIN\",\"referenceKey\":\"\"},\"password\":\"b51fa595e692d53739b69131cdc73440\",\"functionID\":\"OBNKW001\"}}", Params.CarrierListInqParams.class);
        Results.CarrierListInq170Result carrierListInq170Result= (Results.CarrierListInq170Result) durableInqController.carrierListInq(carrierListInqParams).getBody();

        Infos.StartCassette startCassette=new Infos.StartCassette();
        startCassette.setCassetteID(carrierListInq170Result.getFoundCassette().getContent().get(0).getCassetteID());
        startCassette.setLoadPurposeType("Empty Cassette");
        startCassette.setLotInCassetteList(new ArrayList<>());

        Results.EqpInfoInqResult eqpInfoInqResult= (Results.EqpInfoInqResult) electronicInformationTestCase.eqpInfoInqCase(equipmentID).getBody();

        List<Infos.StartCassette> startCassetteList=lotsMoveInReserveInfoInqResult.getStrStartCassette();

        List<ObjectIdentifier> portIDs=eqpInfoInqResult.getEquipmentPortInfo().getEqpPortStatuses().stream().map(
                eqpPortStatus -> eqpPortStatus.getPortID()).collect(Collectors.toList());

        startCassetteList.get(0).setLoadPurposeType(BizConstant.SP_LOADPURPOSETYPE_PROCESSLOT);

        startCassetteList.add(startCassette);

        for (int i = 0; i < startCassetteList.size(); i++) {
            startCassette=startCassetteList.get(i);
            startCassette.setLoadSequenceNumber(i+1L);
            startCassette.setLoadPortID(portIDs.get(i));
            startCassette.setUnloadPortID(portIDs.get(i));
        }
        List<Infos.EqpPortStatus> eqpPortStatuses = eqpInfoInqResult.getEquipmentPortInfo().getEqpPortStatuses();
        Map<String, List<Infos.EqpPortStatus>> portGroupMap = eqpPortStatuses.stream().collect(Collectors.groupingBy(Infos.EqpPortStatus::getPortGroup));
        AtomicReference<String> portGroupSeletced = new AtomicReference<>();
        portGroupMap.forEach((k, v) ->{
            if (v.size() > 1){
                portGroupSeletced.set(k);
            }
        });
        Assert.isTrue(!CimStringUtils.isEmpty(portGroupSeletced.get()), "test fail");
        Params.MoveInReserveReqParams moveInReserveReqParams=new Params.MoveInReserveReqParams();
        moveInReserveReqParams.setEquipmentID(equipmentID);
        moveInReserveReqParams.setPortGroupID(portGroupSeletced.get());
        moveInReserveReqParams.setUser(testCommonData.getUSER());
        moveInReserveReqParams.setStartCassetteList(startCassetteList);
        dispatchController.moveInReserveReq(moveInReserveReqParams);
        return startCassetteList.stream().map(Infos.StartCassette::getCassetteID).collect(Collectors.toList());
    }

    public void whenMonitorCreationFlagOfEquipmentIsTrueReserveCarrierWithoutAnotherLoadedEmptyCarrier(){
        // 1. stb a lot
        Results.WaferLotStartReqResult waferLotStartReqResult= (Results.WaferLotStartReqResult) stbCase.STB_NotPreparedLot().getBody();

        ObjectIdentifier equipmentID = ObjectIdentifier.buildWithValue("1FHI02_NORM");

        // 2. skip 2000.0200
        Results.LotInfoInqResult lotInfoInqResult= (Results.LotInfoInqResult) lotGeneralTestCase.getLotInfoCase(CimArrayUtils.generateList(waferLotStartReqResult.getLotID())).getBody();
        Results.LotOperationSelectionInqResult lotOperationSelectionInqResult= (Results.LotOperationSelectionInqResult) lotGeneralTestCase.getLotOperationSelectionInq(waferLotStartReqResult.getLotID(),false,true,true).getBody();
        Infos.LotOperationInfo lotOperationInfo = lotInfoInqResult.getLotInfoList().get(0).getLotOperationInfo();
        Infos.OperationNameAttributes operationNameAttributes = lotOperationSelectionInqResult.getOperationNameAttributesAttributes().getContent().stream().filter(operationNameAttributes1 -> "2000.0200".equals(operationNameAttributes1.getOperationNumber())).findFirst().get();
        Params.SkipReqParams skipReqParams= JSONObject.parseObject("{\"currentOperationNumber\":\"1000.0100\",\"currentRouteID\":{\"referenceKey\":\"OMPRP.49867291127402983\",\"value\":\"LAYER0MA.01\"},\"locateDirection\":true,\"lotID\":{\"referenceKey\":\"FRLOT.252734301664182272\",\"value\":\"NP000318.00A\"},\"operationID\":{\"referenceKey\":\"OMPRP.88321534610297170\",\"value\":\"FST010.01\"},\"operationNumber\":\"1000.0300\",\"processRef\":{\"mainProcessFlow\":\"OMPRF.66369402815281944\",\"moduleNumber\":\"2000\",\"modulePOS\":\"OMPRSS.24145333485278681\",\"moduleProcessFlow\":\"OMPRF.54939159889810491\",\"processFlow\":\"OMPRF.49867748958157252\",\"processOperationSpecification\":\"OMPRSS.19491816993363067\",\"siInfo\":null},\"routeID\":{\"referenceKey\":\"OMPRP.49867291127402983\",\"value\":\"LAYER0MA.01\"},\"seqno\":-1,\"sequenceNumber\":0,\"user\":{\"userID\":{\"value\":\"ADMIN\",\"referenceKey\":\"\"},\"password\":\"b51fa595e692d53739b69131cdc73440\",\"functionID\":\"OLOTW003\"}}",
                Params.SkipReqParams.class);
        skipReqParams.setCurrentRouteID(lotOperationInfo.getRouteID());
        skipReqParams.setLotID(waferLotStartReqResult.getLotID());
        skipReqParams.setOperationID(lotOperationInfo.getOperationID());
        skipReqParams.setProcessRef(operationNameAttributes.getProcessRef());
        skipReqParams.setRouteID(operationNameAttributes.getRouteID());
        skipReqParams.setOperationNumber(operationNameAttributes.getOperationNumber());
        skipReqParams.setCurrentOperationNumber(lotOperationInfo.getOperationNumber());
        operationSkipTestCase.operationSkip(skipReqParams);

        // 3. start Reserve
        Results.LotsMoveInReserveInfoInqResult lotsMoveInReserveInfoInqResult= (Results.LotsMoveInReserveInfoInqResult) equipmentTestCase.lotsMoveInReserveInfoInqCase(lotInfoInqResult,equipmentID).getBody();

        Results.EqpInfoInqResult eqpInfoInqResult= (Results.EqpInfoInqResult) electronicInformationTestCase.eqpInfoInqCase(equipmentID).getBody();

        List<Infos.StartCassette> startCassetteList=lotsMoveInReserveInfoInqResult.getStrStartCassette();

        List<ObjectIdentifier> portIDs=eqpInfoInqResult.getEquipmentPortInfo().getEqpPortStatuses().stream().map(
                eqpPortStatus -> eqpPortStatus.getPortID()).collect(Collectors.toList());

        startCassetteList.get(0).setLoadPurposeType(BizConstant.SP_LOADPURPOSETYPE_PROCESSLOT);

        for (int i = 0; i < startCassetteList.size(); i++) {
            Infos.StartCassette startCassette = startCassetteList.get(i);
            startCassette.setLoadSequenceNumber(i+1L);
            startCassette.setLoadPortID(portIDs.get(i));
            startCassette.setUnloadPortID(portIDs.get(i));
        }

        Params.MoveInReserveReqParams moveInReserveReqParams=new Params.MoveInReserveReqParams();
        moveInReserveReqParams.setEquipmentID(equipmentID);
        moveInReserveReqParams.setPortGroupID("PG1");
        moveInReserveReqParams.setUser(testCommonData.getUSER());
        moveInReserveReqParams.setStartCassetteList(startCassetteList);
        dispatchController.moveInReserveReq(moveInReserveReqParams);
    }

    public void reserveACarrierWithScrappedWafersInside(){
        // 1. stb a lot
        Results.WaferLotStartReqResult waferLotStartReqResult= (Results.WaferLotStartReqResult) stbCase.STB_NotPreparedLot().getBody();
        ObjectIdentifier lotID=waferLotStartReqResult.getLotID();

        // 2. scape lot
        ObjectIdentifier reasonRouteID = new ObjectIdentifier("LAYER0MA.01");
        ObjectIdentifier reasonCodeID = new ObjectIdentifier("MISC");
        String reasonOperationNumber = "1000.0100";
        int scrapWaferCount = 2;
        TestInfos.ScrapInfo scrapInfo = new TestInfos.ScrapInfo(lotID, reasonRouteID, reasonCodeID, reasonOperationNumber, scrapWaferCount);
        scrapCase.scrap(scrapInfo);

        // 3. start reserve
        startLotsReserve(lotID);
    }

}