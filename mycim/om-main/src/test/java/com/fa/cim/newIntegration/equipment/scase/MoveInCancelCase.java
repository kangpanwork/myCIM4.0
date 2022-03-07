package com.fa.cim.newIntegration.equipment.scase;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.utils.CimArrayUtils;
import com.fa.cim.common.utils.CimObjectUtils;
import com.fa.cim.controller.processmonitor.ProcessMonitorController;
import com.fa.cim.controller.processmonitor.ProcessMonitorInqController;
import com.fa.cim.core.TestCommonData;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;
import com.fa.cim.newIntegration.LotOperation.scase.OperationSkipCase;
import com.fa.cim.newIntegration.common.TestUtils;
import com.fa.cim.newIntegration.dto.TestInfos;
import com.fa.cim.newIntegration.stb.scase.STBCase;
import com.fa.cim.newIntegration.tcase.CommonTestCase;
import com.fa.cim.newIntegration.tcase.ElectronicInformationTestCase;
import com.fa.cim.newIntegration.tcase.EquipmentTestCase;
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
 * 2019/9/15       ********             lightyh             create file
 *
 * @author: lightyh
 * @date: 2019/9/15 9:07
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Service
@Slf4j
public class MoveInCancelCase {

    @Autowired
    private MoveInCase moveInCase;
    @Autowired
    private EquipmentTestCase equipmentTestCase;

    @Autowired
    private ElectronicInformationTestCase electronicInformationTestCase;

    @Autowired
    private STBCase stbCase;

    @Autowired
    private OperationSkipCase operationSkipCase;

    @Autowired
    private CommonTestCase commonTestCase;

    @Autowired
    private TestUtils testUtils;

    @Autowired
    private ProcessMonitorController processMonitorController;

    @Autowired
    private ProcessMonitorInqController processMonitorInqController;

    @Autowired
    @Qualifier("testCommonDataWithDB")
    private TestCommonData testCommonData;

    private static final String RETICLE_EQUIPMENTID = "1TKD01_EXD01";

    public List<Infos.StartCassette> moveInCancel_Normal(){
        //【step1】move in
        Results.MoveInReqResult moveInReqResult = moveInCase.moveIn_GenerateControlJob_Without_StartReservation();
        return this.moveInCancel_Normal(testCommonData.getEQUIPMENTID(), moveInReqResult);
    }

    public List<Infos.StartCassette> moveInCancel_Normal(ObjectIdentifier equipmentID, Results.MoveInReqResult moveInReqResult){
        //【step1】eqp info
        Results.EqpInfoInqResult eqpInfo = electronicInformationTestCase.getEqpInfo(equipmentID);
        List<Infos.EqpInprocessingControlJob> equipmentInprocessingControlJobList = eqpInfo.getEquipmentInprocessingControlJobList();
        Assert.isTrue(!CimArrayUtils.isEmpty(equipmentInprocessingControlJobList), "test fail");
        //【step2】move in cancel
        List<Infos.StartCassette>  startCassetteList = (List<Infos.StartCassette>) equipmentTestCase.moveInCancel(equipmentID, moveInReqResult.getControlJobID()).getBody();
        //【step3】eqp info
        Results.EqpInfoInqResult eqpInfo2 = electronicInformationTestCase.getEqpInfo(equipmentID);
        List<Infos.EqpInprocessingControlJob> equipmentInprocessingControlJobList2 = eqpInfo2.getEquipmentInprocessingControlJobList();
        Assert.isTrue(CimArrayUtils.isEmpty(equipmentInprocessingControlJobList2), "test fail");
        return startCassetteList;
    }

    public void checkEQPE10StatusAfterOperStartCancel(){
        moveInCancel_Normal();
        Results.EqpInfoInqResult eqpInfoInqResult= (Results.EqpInfoInqResult) electronicInformationTestCase.eqpInfoInqCase(testCommonData.getEQUIPMENTID()).getBody();
        Assert.isTrue("SBY".equalsIgnoreCase(eqpInfoInqResult.getEquipmentStatusInfo().getE10Status()),"E10 Status should be  \"SBY\"");
    }

    public void checkProcessStatusAfterOperStartCancel(){
        Results.MoveInReqResult moveInReqResult = (Results.MoveInReqResult) moveInCase.moveIn_GenerateControlJob_Without_StartReservation();
        Results.EqpInfoInqResult eqpInfoInqResult= (Results.EqpInfoInqResult) electronicInformationTestCase.eqpInfoInqCase(testCommonData.getEQUIPMENTID()).getBody();
        Assert.isTrue(eqpInfoInqResult.getEquipmentStatusInfo().getEquipmentStatusCode().equals("PRD"),"process status should be \"Processing \"");
        List<Infos.StartCassette>  startCassetteList = (List<Infos.StartCassette>) equipmentTestCase.moveInCancel(testCommonData.getEQUIPMENTID(), moveInReqResult.getControlJobID()).getBody();
        eqpInfoInqResult= (Results.EqpInfoInqResult) electronicInformationTestCase.eqpInfoInqCase(testCommonData.getEQUIPMENTID()).getBody();
        Assert.isTrue(eqpInfoInqResult.getEquipmentStatusInfo().getEquipmentStatusCode().equals("WLOT"),"process status should be \"Waiting \"");
    }

    public void checkUsageCountOfEquipmentAfterOperStartCancel(){
        Results.MoveInReqResult moveInReqResult = (Results.MoveInReqResult) moveInCase.moveIn_GenerateControlJob_Without_StartReservation();
        Results.EqpInfoInqResult eqpInfoInqResult= (Results.EqpInfoInqResult) electronicInformationTestCase.eqpInfoInqCase(testCommonData.getEQUIPMENTID()).getBody();
        Integer operationStartCount1 = eqpInfoInqResult.getEquipmentPMInfo().getMoveInCount();
        List<Infos.StartCassette>  startCassetteList = (List<Infos.StartCassette>) equipmentTestCase.moveInCancel(testCommonData.getEQUIPMENTID(), moveInReqResult.getControlJobID()).getBody();
        eqpInfoInqResult= (Results.EqpInfoInqResult) electronicInformationTestCase.eqpInfoInqCase(testCommonData.getEQUIPMENTID()).getBody();
        Assert.isTrue(operationStartCount1==eqpInfoInqResult.getEquipmentPMInfo().getMoveInCount()+1,"\"oper start count\" is N-1\n");
    }

    public void checkUsageCountOfEmployedReticleAfterOperStartCancel(){
        ObjectIdentifier equipmentID=ObjectIdentifier.buildWithValue(RETICLE_EQUIPMENTID);

        // 1. stb
        ObjectIdentifier lotID = stbCase.STB_PRODUCT101(false);

        // 2. rtc move in
        List<Infos.StoredReticle> storedReticleList = equipmentTestCase.keepEquipmentReticleAvailable(equipmentID, 1);

        // 3. move in
        Results.MoveInReqResult moveInReqResult= (Results.MoveInReqResult) moveInCase.moveIn_GenerateControlJob_Without_StartReservation(lotID,equipmentID).getBody();

        ObjectIdentifier reticleID = storedReticleList.get(0).getReticleID();

        // 4. query rtc start count
        Results.ReticleDetailInfoInqResult reticleDetailInfoInqResult = commonTestCase.reticleDetailInfoInq(reticleID);
        Integer operationStartCount = reticleDetailInfoInqResult.getReticlePMInfo().getOperationStartCount();

        // 5. move in cancel
        ObjectIdentifier controlJobID=moveInReqResult.getControlJobID();
        equipmentTestCase.moveInCancel(equipmentID,controlJobID);

        // 6. query rtc start count
        reticleDetailInfoInqResult = commonTestCase.reticleDetailInfoInq(reticleID);
        Assert.isTrue(operationStartCount==reticleDetailInfoInqResult.getReticlePMInfo().getOperationStartCount()+1,"\"current reticle count\" is N-1\n");

    }

    public void checkMonitorGroupAfterMoveInCancel(){
        //【step1】stb one product lot
        String bankID = "BNK-0S";
        String sourceProductID = "RAW-2000.01";
        String productID = "PRODUCT0.01";
        TestInfos.StbInfo stbInfo = new TestInfos.StbInfo(bankID, sourceProductID, productID, 25L, false);
        ObjectIdentifier lotID = stbCase.STB_Normal(stbInfo);

        //【step2】stb one monitor lot
        TestInfos.StbMonitorLotInfo stbMonitorLotInfo = new TestInfos.StbMonitorLotInfo();
        stbMonitorLotInfo.setBankID("BK-CTRL");
        stbMonitorLotInfo.setProductCount(5);
        stbMonitorLotInfo.setSourceProductID("RAW-2000.01");
        ObjectIdentifier monitorLotID = stbCase.STB_MonitorLot(stbMonitorLotInfo);

        //【step3】make monitor grouping
        Params.MonitorBatchCreateReqParams monitorProdLotsReqParams = new Params.MonitorBatchCreateReqParams();
        monitorProdLotsReqParams.setUser(testUtils.getUser());
        monitorProdLotsReqParams.setMonitorLotID(monitorLotID);   // yes, the product lot will be setted here.
        List<Infos.MonRelatedProdLots> monRelatedProdLotsList = new ArrayList<>();
        Infos.MonRelatedProdLots monRelatedProdLots = new Infos.MonRelatedProdLots();
        monRelatedProdLots.setProductLotID(lotID);
        monRelatedProdLotsList.add(monRelatedProdLots);
        monitorProdLotsReqParams.setStrMonRelatedProdLots(monRelatedProdLotsList);
        processMonitorController.monitorBatchCreateReq(monitorProdLotsReqParams);

        //【step4】load and move in
        Results.MoveInReqResult moveInReqResult = (Results.MoveInReqResult) moveInCase.moveIn_GenerateControlJob_Without_StartReservation(lotID, testCommonData.getEQUIPMENTID()).getBody();
        //【step5】check group info
        Params.MonitorBatchRelationInqParams monitorBatchRelationInqParams = new Params.MonitorBatchRelationInqParams();
        monitorBatchRelationInqParams.setUser(testUtils.getUser());
        monitorBatchRelationInqParams.setCassetteID(moveInReqResult.getStartCassetteList().get(0).getCassetteID());
        monitorBatchRelationInqParams.setLotID(moveInReqResult.getStartCassetteList().get(0).getLotInCassetteList().get(0).getLotID());
        List<Infos.MonitorGroups> monitorGroupsList = (List<Infos.MonitorGroups>) processMonitorInqController.monitorBatchRelationInq(monitorBatchRelationInqParams).getBody();
        Assert.isTrue(!CimArrayUtils.isEmpty(monitorGroupsList), "the monitor lot needn't inherit monitor group info");
        //【step6】move in cancel
        this.moveInCancel_Normal(testCommonData.getEQUIPMENTID(), moveInReqResult);
        //【step7】check group info again
        List<Infos.MonitorGroups> monitorGroupsList2 = (List<Infos.MonitorGroups>) processMonitorInqController.monitorBatchRelationInq(monitorBatchRelationInqParams).getBody();
        Assert.isTrue(!CimArrayUtils.isEmpty(monitorGroupsList2), "the monitor lot needn't inherit monitor group info");
    }

    public void moveInCancelLotWhichEquipmentHasTwoDifferentControlJobs(){
        //【step1】eqp info
        Results.EqpInfoInqResult eqpInfo = electronicInformationTestCase.getEqpInfo(new ObjectIdentifier("1LMK02"));
        List<Infos.EqpPortStatus> eqpPortStatuses = eqpInfo.getEquipmentPortInfo().getEqpPortStatuses();
        Map<String, List<Infos.EqpPortStatus>> map = eqpPortStatuses.stream().collect(Collectors.groupingBy(Infos.EqpPortStatus::getPortGroup));
        Assert.isTrue(map.keySet().size() > 1, "test fail, you should select the equipment that the count of port group greater than 1");
        List<String> portGroupList = new ArrayList<>(map.keySet());
        ObjectIdentifier portSelected1 = map.get(portGroupList.get(0)).get(0).getPortID();
        ObjectIdentifier portSelected2 = map.get(portGroupList.get(1)).get(0).getPortID();
        //【step2】stb to lot
        List<Infos.LotInfo> lotInfoList = operationSkipCase.stbNLotsAndSkipSpecificStep(2, true, "1000.0200", true);
        //【step3】move in separately
        Results.MoveInReqResult moveInReqResult1 = (Results.MoveInReqResult) moveInCase.moveIn_GenerateControlJob_Without_StartReservation(lotInfoList.get(0).getLotBasicInfo().getLotID(), new ObjectIdentifier("1LMK02"), portSelected1).getBody();
        Results.MoveInReqResult moveInReqResult2 = (Results.MoveInReqResult) moveInCase.moveIn_GenerateControlJob_Without_StartReservation(lotInfoList.get(1).getLotBasicInfo().getLotID(), new ObjectIdentifier("1LMK02"), portSelected2).getBody();
        //【step3】eqp info
        Results.EqpInfoInqResult eqpInfo2 = electronicInformationTestCase.getEqpInfo(new ObjectIdentifier("1LMK02"));
        List<Infos.EqpInprocessingControlJob> equipmentInprocessingControlJobList = eqpInfo2.getEquipmentInprocessingControlJobList();
        Assert.isTrue(CimArrayUtils.getSize(equipmentInprocessingControlJobList) == 2
                && !CimObjectUtils.equalsWithValue(equipmentInprocessingControlJobList.get(0).getControlJobID(), equipmentInprocessingControlJobList.get(1).getControlJobID()), "test fail");
        //【step4】move in cancel one
        List<Infos.StartCassette>  startCassetteList = (List<Infos.StartCassette>) equipmentTestCase.moveInCancel(new ObjectIdentifier("1LMK02"), moveInReqResult1.getControlJobID()).getBody();
        //【step5】eqp info
        Results.EqpInfoInqResult eqpInfo3 = electronicInformationTestCase.getEqpInfo(new ObjectIdentifier("1LMK02"));
        List<Infos.EqpInprocessingControlJob> equipmentInprocessingControlJobList2 = eqpInfo3.getEquipmentInprocessingControlJobList();
        Assert.isTrue(CimArrayUtils.getSize(equipmentInprocessingControlJobList2) == 1, "test fail");

    }

}