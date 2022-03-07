package com.fa.cim.newIntegration.LotOperation.scase;

import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.utils.CimArrayUtils;
import com.fa.cim.common.utils.CimObjectUtils;
import com.fa.cim.common.utils.CimStringUtils;
import com.fa.cim.common.utils.Validations;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.core.TestCommonData;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Results;
import com.fa.cim.newIntegration.equipment.scase.LotLoadToEquipmentCase;
import com.fa.cim.newIntegration.equipment.scase.MoveInCase;
import com.fa.cim.newIntegration.equipment.scase.MoveOutCase;
import com.fa.cim.newIntegration.equipment.scase.StartLotsReservationCase;
import com.fa.cim.newIntegration.stb.scase.STBCase;
import com.fa.cim.newIntegration.tcase.AutoDipatchControlTestCase;
import com.fa.cim.newIntegration.tcase.CommonTestCase;
import com.fa.cim.newIntegration.tcase.EquipmentTestCase;
import com.fa.cim.newIntegration.tcase.LotGeneralTestCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/12/19          ********            lightyh                create file
 *
 * @author: light
 * @date: 2019/12/19 12:37
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@Service
public class AutoDispatchControlCase {

    @Autowired
    private AutoDipatchControlTestCase autoDipatchControlTestCase;

    @Autowired
    private STBCase stbCase;

    @Autowired
    private OperationSkipCase operationSkipCase;

    @Autowired
    private LotGeneralTestCase lotGeneralTestCase;

    @Autowired
    private EquipmentTestCase equipmentTestCase;

    @Autowired
    private LotSplitCase lotSplitCase;

    @Autowired
    private CommonTestCase commonTestCase;

    @Autowired
    private StartLotsReservationCase startLotsReservationCase;

    @Autowired
    private MoveInCase moveInCase;

    @Autowired
    private MoveOutCase moveOutCase;

    @Autowired
    private LotLoadToEquipmentCase lotLoadToEquipmentCase;

    @Autowired
    @Qualifier("testCommonDataWithDB")
    private TestCommonData testCommonData;

    @Autowired
    private RetCodeConfig retCodeConfig;

    public void addingAutoDispatchControlThenCheckTheAutoDispatchDisabledFlag(){
        //【step1】stb
        List<ObjectIdentifier> lots = stbCase.stb_NLots_NotPreparedCase(1, true);
        //【step2】get lot info
        List<Infos.LotInfo> lotInfoList = lotGeneralTestCase.getLotInfos(lots);
        // 【step3】check the same process flow and same operation number
        // add auto dispatch control(same process flow and same operation number)
        this.updateAutoDispatchControl("Create", lotInfoList.get(0).getLotOperationInfo().getRouteID(), lotInfoList.get(0).getLotOperationInfo().getOperationNumber(), lotInfoList.get(0).getLotBasicInfo().getLotID(), true);
        // CheckTheAutoDispatchDisabledFlag
        this.checkAutoDispatchDisabledFlag(lots.get(0), true);
        // delete auto dispatch control
        this.updateAutoDispatchControl("Delete", lotInfoList.get(0).getLotOperationInfo().getRouteID(), lotInfoList.get(0).getLotOperationInfo().getOperationNumber(), lotInfoList.get(0).getLotBasicInfo().getLotID(), true);
        // 【step4】check the same process flow and different operation number
        Results.LotOperationSelectionInqResult lotOperationSelectionInqResult = (Results.LotOperationSelectionInqResult) lotGeneralTestCase.getLotOperationSelectionInq(lots.get(0), false, true, true).getBody();
        // add auto dispatch control
        this.updateAutoDispatchControl("Create", lotInfoList.get(0).getLotOperationInfo().getRouteID(), lotOperationSelectionInqResult.getOperationNameAttributesAttributes().getContent().get(0).getOperationNumber(), lotInfoList.get(0).getLotBasicInfo().getLotID(), true);
        // CheckTheAutoDispatchDisabledFlag
        this.checkAutoDispatchDisabledFlag(lots.get(0),false);
        // delete auto dispatch control
        this.updateAutoDispatchControl("Delete", lotInfoList.get(0).getLotOperationInfo().getRouteID(), lotOperationSelectionInqResult.getOperationNameAttributesAttributes().getContent().get(0).getOperationNumber(), lotInfoList.get(0).getLotBasicInfo().getLotID(), true);
        //【step5】check process flow is different from current process flow
        List<Infos.RouteIndexInformation> routeIndexInformationList = commonTestCase.mainProcessFlowListInq();
        List<Infos.RouteIndexInformation> routeIndexInformationListSelected = routeIndexInformationList.stream().
                filter(routeIndexInformation -> !CimObjectUtils.equalsWithValue(routeIndexInformation.getRouteID(), lotInfoList.get(0).getLotOperationInfo().getRouteID())).collect(Collectors.toList());
        Infos.RouteIndexInformation routeIndexInformationSelected = routeIndexInformationListSelected.get(0);
        List<Infos.OperationNameAttributes> routeOperationList = (List<Infos.OperationNameAttributes>) commonTestCase.processFlowOperationListInq(routeIndexInformationSelected.getRouteID()).getBody();
        // add auto dispatch control
        this.updateAutoDispatchControl("Create", routeIndexInformationSelected.getRouteID(), routeOperationList.get(0).getOperationNumber(), lotInfoList.get(0).getLotBasicInfo().getLotID(), true);
        // CheckTheAutoDispatchDisabledFlag
        this.checkAutoDispatchDisabledFlag(lots.get(0), false);
        this.updateAutoDispatchControl("Delete", routeIndexInformationSelected.getRouteID(), routeOperationList.get(0).getOperationNumber(), lotInfoList.get(0).getLotBasicInfo().getLotID(), true);
        //【step6】check Single Trigger flag is "Multiple"
        // add auto dispatch control
        this.updateAutoDispatchControl("Create", lotInfoList.get(0).getLotOperationInfo().getRouteID(), lotInfoList.get(0).getLotOperationInfo().getOperationNumber(), lotInfoList.get(0).getLotBasicInfo().getLotID(), false);
        // CheckTheAutoDispatchDisabledFlag
        this.checkAutoDispatchDisabledFlag(lots.get(0), true);
        // delete auto dispatch control
        this.updateAutoDispatchControl("Delete", lotInfoList.get(0).getLotOperationInfo().getRouteID(), lotInfoList.get(0).getLotOperationInfo().getOperationNumber(), lotInfoList.get(0).getLotBasicInfo().getLotID(), false);
        //【step7】check no auto dispatch control
        this.checkAutoDispatchDisabledFlag(lots.get(0), false);
    }

    private void updateAutoDispatchControl(String updateMode,ObjectIdentifier routeID, String operationNumber, ObjectIdentifier lotID ,boolean isSingleTrigger){
        Infos.AutoDispatchControlUpdateInfo autoDispatchControlUpdateInfo = new Infos.AutoDispatchControlUpdateInfo();
        autoDispatchControlUpdateInfo.setOperationNumber(operationNumber);
        autoDispatchControlUpdateInfo.setRouteID(routeID);
        autoDispatchControlUpdateInfo.setSingleTriggerFlag(isSingleTrigger);
        autoDispatchControlUpdateInfo.setUpdateMode(updateMode);
        autoDipatchControlTestCase.autoDispatchControlUpdate(Arrays.asList(autoDispatchControlUpdateInfo), lotID);
    }

    private void checkAutoDispatchDisabledFlag(ObjectIdentifier lotID, ObjectIdentifier equipmentID, boolean autoDispatchDisabledFlag){
        if (CimObjectUtils.isEmptyWithValue(equipmentID)){
            equipmentID = testCommonData.getEQUIPMENTID();
        }
        Results.WhatNextLotListResult whatNextLotListResult = (Results.WhatNextLotListResult) equipmentTestCase.whatNextInqCase(equipmentID).getBody();
        List<Infos.WhatNextAttributes> whatNextAttributesContent = whatNextLotListResult.getWhatNextAttributesPage().getContent();
        Infos.WhatNextAttributes whatNextAttributes = whatNextAttributesContent.stream().filter(x -> CimObjectUtils.equalsWithValue(x.getLotID(), lotID)).findFirst().orElse(null);
        if (whatNextAttributes == null) {
            return;
        }
        Assert.isTrue(whatNextAttributes.getAutoDispatchDisableFlag() == autoDispatchDisabledFlag, "test fail, the autodispatchDisableFlag shoud be " + autoDispatchDisabledFlag);
    }
    private void checkAutoDispatchDisabledFlag(ObjectIdentifier lotID, boolean autoDispatchDisabledFlag) {
        this.checkAutoDispatchDisabledFlag(lotID, null, autoDispatchDisabledFlag);
    }
    public void addingAutoDispatchControlWhichLotStateIsFINISHED(){
        //【step1】stb and bank in
        List<Infos.LotInfo> lotInfoList = operationSkipCase.skipAndBankIn();
        List<Infos.LotStatusList> lotStatusList = lotInfoList.get(0).getLotBasicInfo().getLotStatusList();
        Map<String, List<Infos.LotStatusList>> map = lotStatusList.stream().collect(Collectors.groupingBy(Infos.LotStatusList::getStateName));
        Assert.isTrue(CimStringUtils.equals("FINISHED", map.get("Lot State").get(0).getStateValue()), "test fail");
        try {
            this.updateAutoDispatchControl("Create", lotInfoList.get(0).getLotOperationInfo().getRouteID(), lotInfoList.get(0).getLotOperationInfo().getOperationNumber(), lotInfoList.get(0).getLotBasicInfo().getLotID(), true);
            Assert.isTrue(false, "need throw exception");
        } catch (ServiceException e) {
            Assert.isTrue(Validations.isEquals(retCodeConfig.getInvalidLotStat(), e.getCode()), e.getMessage());
        }
    }

    public void addingAutoDispatchControlWhichLotPostProcessStateIsInPostProcessing(){

    }

    public void updateAutoDispatchControlChangeSingleTriggerFlagFromMultipleToSingle(){
        //【step1】stb
        List<ObjectIdentifier> lots = stbCase.stb_NLots_NotPreparedCase(1, true);
        //【step2】get lot info
        List<Infos.LotInfo> lotInfoList = lotGeneralTestCase.getLotInfos(lots);
        // 【step3】 add auto dispatch control
        this.updateAutoDispatchControl("Create", lotInfoList.get(0).getLotOperationInfo().getRouteID(), lotInfoList.get(0).getLotOperationInfo().getOperationNumber(), lotInfoList.get(0).getLotBasicInfo().getLotID(), false);
        // 【step4】 update auto dispatch control
        this.updateAutoDispatchControl("Update", lotInfoList.get(0).getLotOperationInfo().getRouteID(), lotInfoList.get(0).getLotOperationInfo().getOperationNumber(), lotInfoList.get(0).getLotBasicInfo().getLotID(), true);
    }

    public void executeAutoDispatchControlWhichSingleTriggerFlagIsMultiple(){
        //【step1】stb
        List<ObjectIdentifier> lots = stbCase.stb_NLots_NotPreparedCase(1, true);
        //【step2】get lot info
        List<Infos.LotInfo> lotInfoList = lotGeneralTestCase.getLotInfos(lots);
        //【step3】add multiple auto dispatch control
        this.updateAutoDispatchControl("Create", lotInfoList.get(0).getLotOperationInfo().getRouteID(), lotInfoList.get(0).getLotOperationInfo().getOperationNumber(), lotInfoList.get(0).getLotBasicInfo().getLotID(), false);
        //【step4】get lot info
        List<Infos.LotInfo> lotInfoList2 = lotGeneralTestCase.getLotInfos(lots);
        Assert.isTrue(lotInfoList2.get(0).getLotBasicInfo().getAutoDispatchControlFlag(), "test fail");
        //【step5】execute auto dispatch control
        this.executeAutoDispatchControl(lotInfoList.get(0));
        //【step6】get lot info again
        List<Infos.LotInfo> lotInfoList3 = lotGeneralTestCase.getLotInfos(lots);
        Assert.isTrue(lotInfoList3.get(0).getLotBasicInfo().getAutoDispatchControlFlag(), "test fail");
    }

    public void executeAutoDispatchControlWhichSingleTriggerFlagIsSingle(){
        //【step1】stb
        List<ObjectIdentifier> lots = stbCase.stb_NLots_NotPreparedCase(1, true);
        //【step2】get lot info
        List<Infos.LotInfo> lotInfoList = lotGeneralTestCase.getLotInfos(lots);
        //【step3】add single auto dispatch control
        this.updateAutoDispatchControl("Create", lotInfoList.get(0).getLotOperationInfo().getRouteID(), lotInfoList.get(0).getLotOperationInfo().getOperationNumber(), lotInfoList.get(0).getLotBasicInfo().getLotID(), true);
        //【step4】get lot info
        List<Infos.LotInfo> lotInfoList2 = lotGeneralTestCase.getLotInfos(lots);
        Assert.isTrue(lotInfoList2.get(0).getLotBasicInfo().getAutoDispatchControlFlag(), "test fail");
        //【step5】execute auto dispatch control
        this.executeAutoDispatchControl(lotInfoList.get(0));
        //【step6】get lot info again
        List<Infos.LotInfo> lotInfoList3 = lotGeneralTestCase.getLotInfos(lots);
        Assert.isTrue(!lotInfoList3.get(0).getLotBasicInfo().getAutoDispatchControlFlag(), "test fail");
    }

    private void executeAutoDispatchControl(Infos.LotInfo lotInfo){
        //【step1】change online mode to auto3
        equipmentTestCase.changeOperationModeToAuto(testCommonData.getEQUIPMENTID(), "Auto-3");
        //【step2】change port status
        equipmentTestCase.changePortStatus(testCommonData.getEQUIPMENTID(), "LoadReq");
        //【step3】change cassette xfer status
        commonTestCase.lotCassetteXferStatusChange(lotInfo.getLotLocationInfo().getCassetteID(), "MI");
        //【step4】reserve
        startLotsReservationCase.startLotsReserve(lotInfo.getLotBasicInfo().getLotID(), testCommonData.getEQUIPMENTID());
        //【step5】change port status
        equipmentTestCase.changePortStatus(testCommonData.getEQUIPMENTID(), "LoadComp");
        //【step6】change cassette xfer status
        commonTestCase.lotCassetteXferStatusChange(lotInfo.getLotLocationInfo().getCassetteID(), "MO");
        //【step7】load and move in
        Results.MoveInReqResult moveInReqResult = (Results.MoveInReqResult) moveInCase.moveIn_GenerateControlJob_Without_StartReservation(lotInfo.getLotBasicInfo().getLotID(), testCommonData.getEQUIPMENTID()).getBody();
        //【step8】move out
        moveOutCase.onlyMoveOut(moveInReqResult.getControlJobID(), testCommonData.getEQUIPMENTID());
    }

    public void executeAutoDispatchControlWhichLoadCarrierToPortWithoutReserve(){
        //【step1】stb
        List<ObjectIdentifier> lots = stbCase.stb_NLots_NotPreparedCase(1, true);
        //【step2】get lot info
        List<Infos.LotInfo> lotInfoList = lotGeneralTestCase.getLotInfos(lots);
        //【step3】add single auto dispatch control
        this.updateAutoDispatchControl("Create", lotInfoList.get(0).getLotOperationInfo().getRouteID(), lotInfoList.get(0).getLotOperationInfo().getOperationNumber(), lotInfoList.get(0).getLotBasicInfo().getLotID(), true);
        //【step4】get lot info
        List<Infos.LotInfo> lotInfoList2 = lotGeneralTestCase.getLotInfos(lots);
        Assert.isTrue(lotInfoList2.get(0).getLotBasicInfo().getAutoDispatchControlFlag(), "test fail");
        //【step5】change online mode to auto3
        equipmentTestCase.changeOperationModeToAuto(testCommonData.getEQUIPMENTID(), "Auto-3");
        //【step6】change port status
        equipmentTestCase.changePortStatus(testCommonData.getEQUIPMENTID(), "LoadComp");
        //【step7】load
        try {
            lotLoadToEquipmentCase.manual_Loading_Without_StartLotsReservation(lots.get(0), testCommonData.getEQUIPMENTID());
            Assert.isTrue(false, "need throw exception");
        } catch (ServiceException e) {
            Assert.isTrue(Validations.isEquals(retCodeConfig.getCastControlJobIdBlank(), e.getCode()), e.getMessage());
        }
    }

    public void executeAutoDispatchControlWhichStartReservationWithoutChangeCarrierXferStatus(){
        //【step1】stb
        List<ObjectIdentifier> lots = stbCase.stb_NLots_NotPreparedCase(1, true);
        //【step2】get lot info
        List<Infos.LotInfo> lotInfoList = lotGeneralTestCase.getLotInfos(lots);
        //【step3】add single auto dispatch control
        this.updateAutoDispatchControl("Create", lotInfoList.get(0).getLotOperationInfo().getRouteID(), lotInfoList.get(0).getLotOperationInfo().getOperationNumber(), lotInfoList.get(0).getLotBasicInfo().getLotID(), true);
        //【step4】get lot info
        List<Infos.LotInfo> lotInfoList2 = lotGeneralTestCase.getLotInfos(lots);
        Assert.isTrue(lotInfoList2.get(0).getLotBasicInfo().getAutoDispatchControlFlag(), "test fail");
        //【step5】change online mode to auto3
        equipmentTestCase.changeOperationModeToAuto(testCommonData.getEQUIPMENTID(), "Auto-3");
        //【step6】change port status
        equipmentTestCase.changePortStatus(testCommonData.getEQUIPMENTID(), "LoadReq");
        //【step7】reserve
        try {
            startLotsReservationCase.startLotsReserve(lots.get(0), testCommonData.getEQUIPMENTID());
            Assert.isTrue(false, "need throw exception");
        } catch (ServiceException e) {
            Assert.isTrue(Validations.isEquals(retCodeConfig.getInvalidCassetteTransferState(), e.getCode()), e.getMessage());
        }
    }

    public void executeAutoDispatchControlWhichLoadLotToPortWithoutChangeCarrierXferStatus(){
        //【step1】stb
        List<ObjectIdentifier> lots = stbCase.stb_NLots_NotPreparedCase(1, true);
        //【step2】get lot info
        List<Infos.LotInfo> lotInfoList = lotGeneralTestCase.getLotInfos(lots);
        //【step3】add single auto dispatch control
        this.updateAutoDispatchControl("Create", lotInfoList.get(0).getLotOperationInfo().getRouteID(), lotInfoList.get(0).getLotOperationInfo().getOperationNumber(), lotInfoList.get(0).getLotBasicInfo().getLotID(), true);
        //【step4】get lot info
        List<Infos.LotInfo> lotInfoList2 = lotGeneralTestCase.getLotInfos(lots);
        Assert.isTrue(lotInfoList2.get(0).getLotBasicInfo().getAutoDispatchControlFlag(), "test fail");
        //【step5】change online mode to auto3
        equipmentTestCase.changeOperationModeToAuto(testCommonData.getEQUIPMENTID(), "Auto-3");
        //【step6】change port status
        equipmentTestCase.changePortStatus(testCommonData.getEQUIPMENTID(), "LoadReq");
        //【step7】change cassette xfer status
        commonTestCase.lotCassetteXferStatusChange(lotInfoList.get(0).getLotLocationInfo().getCassetteID(), "MI");
        //【step8】reserve
        startLotsReservationCase.startLotsReserve(lotInfoList.get(0).getLotBasicInfo().getLotID(), testCommonData.getEQUIPMENTID());
        //【step9】change port status
        equipmentTestCase.changePortStatus(testCommonData.getEQUIPMENTID(), "LoadComp");
        //【step10】load
        try {
            lotLoadToEquipmentCase.manual_Loading_Without_StartLotsReservation(lots.get(0), testCommonData.getEQUIPMENTID());
            Assert.isTrue(false, "need throw exception");
        } catch (ServiceException e) {
            Assert.isTrue(Validations.isEquals(retCodeConfig.getInvalidCassetteTransferState(), e.getCode()), e.getMessage());
        }
    }

    public void executeAutoDispatchControlWhichLoadPortStateShouldBeLoadcomp(){
        //【step1】stb
        List<ObjectIdentifier> lots = stbCase.stb_NLots_NotPreparedCase(1, true);
        //【step2】get lot info
        List<Infos.LotInfo> lotInfoList = lotGeneralTestCase.getLotInfos(lots);
        //【step3】add single auto dispatch control
        this.updateAutoDispatchControl("Create", lotInfoList.get(0).getLotOperationInfo().getRouteID(), lotInfoList.get(0).getLotOperationInfo().getOperationNumber(), lotInfoList.get(0).getLotBasicInfo().getLotID(), true);
        //【step4】get lot info
        List<Infos.LotInfo> lotInfoList2 = lotGeneralTestCase.getLotInfos(lots);
        Assert.isTrue(lotInfoList2.get(0).getLotBasicInfo().getAutoDispatchControlFlag(), "test fail");
        //【step5】change online mode to auto3
        equipmentTestCase.changeOperationModeToAuto(testCommonData.getEQUIPMENTID(), "Auto-3");
        //【step6】change port status
        equipmentTestCase.changePortStatus(testCommonData.getEQUIPMENTID(), "LoadReq");
        //【step7】change cassette xfer status
        commonTestCase.lotCassetteXferStatusChange(lotInfoList.get(0).getLotLocationInfo().getCassetteID(), "MI");
        //【step8】reserve
        startLotsReservationCase.startLotsReserve(lotInfoList.get(0).getLotBasicInfo().getLotID(), testCommonData.getEQUIPMENTID());
        //【step9】change cassette xfer status
        commonTestCase.lotCassetteXferStatusChange(lotInfoList.get(0).getLotLocationInfo().getCassetteID(), "MO");
        //【step10】load
        try {
            lotLoadToEquipmentCase.manual_Loading_Without_StartLotsReservation(lots.get(0), testCommonData.getEQUIPMENTID());
            Assert.isTrue(false, "need throw exception");
        } catch (ServiceException e) {
            Assert.isTrue(Validations.isEquals(retCodeConfig.getInvalidPortState(), e.getCode()), e.getMessage());
        }
        //【step11】change port status
        equipmentTestCase.changePortStatus(testCommonData.getEQUIPMENTID(), "LoadComp");
        //【step12】load again, will be success
        lotLoadToEquipmentCase.manual_Loading_Without_StartLotsReservation(lots.get(0), testCommonData.getEQUIPMENTID());
    }

    public void autoDispatchInquiry(){
        //【step1】stb
        List<ObjectIdentifier> lots = stbCase.stb_NLots_NotPreparedCase(1, true);
        //【step2】get lot info
        List<Infos.LotInfo> lotInfoList = lotGeneralTestCase.getLotInfos(lots);
        //【step3】 add auto dispatch control
        this.updateAutoDispatchControl("Create", lotInfoList.get(0).getLotOperationInfo().getRouteID(), lotInfoList.get(0).getLotOperationInfo().getOperationNumber(), lotInfoList.get(0).getLotBasicInfo().getLotID(), false);
        //【step4】get lot info again
        List<Infos.LotInfo> lotInfoList2 = lotGeneralTestCase.getLotInfos(lots);
        Assert.isTrue(lotInfoList2.get(0).getLotBasicInfo().getAutoDispatchControlFlag(), "test fail");
        // 【step5】lot list page
        List<Infos.LotListAttributes> lotListAttributes = lotGeneralTestCase.lotListInq(lots.get(0));
        Assert.isTrue(lotListAttributes.get(0).getAutoDispatchControlFlag(), "test fail");
        // 【step6】What’s Next List Inquiry
        this.checkAutoDispatchDisabledFlag(lots.get(0), true);
        // 【step7】get cassette info
        Results.CarrierDetailInfoInqResult carrierDetailInfoInqResult = commonTestCase.carrierDetailInfoInq(lotInfoList.get(0).getLotLocationInfo().getCassetteID());
        Assert.isTrue(carrierDetailInfoInqResult.getCassetteStatusInfo().getStrContainedLotInfo().get(0).isAutoDispatchDisableFlag(), "test fail");
        // 【step8】get auto dispatch control info
        List<Infos.LotAutoDispatchControlInfo> lotAutoDispatchControlInfo = autoDipatchControlTestCase.getLotAutoDispatchControlInfo(lotInfoList.get(0).getLotBasicInfo().getLotID(), null, null);
        Assert.isTrue(!CimArrayUtils.isEmpty(lotAutoDispatchControlInfo), "test fail");
    }

    public void autoDispathControlInSplittedLots(){
        //【step1】stb
        List<ObjectIdentifier> lots = stbCase.stb_NLots_NotPreparedCase(1, true);
        //【step2】get lot info
        List<Infos.LotInfo> lotInfoList = lotGeneralTestCase.getLotInfos(lots);
        //【step3】 add auto dispatch control
        this.updateAutoDispatchControl("Create", lotInfoList.get(0).getLotOperationInfo().getRouteID(), lotInfoList.get(0).getLotOperationInfo().getOperationNumber(), lotInfoList.get(0).getLotBasicInfo().getLotID(), false);
        //【step4】get lot info again
        List<Infos.LotInfo> lotInfoList2 = lotGeneralTestCase.getLotInfos(lots);
        Assert.isTrue(lotInfoList2.get(0).getLotBasicInfo().getAutoDispatchControlFlag(), "test fail");
        //【step5】get lot info again
        ObjectIdentifier childLot = lotSplitCase.splitBySpecificLotID(lots.get(0));
        //【step6】get parent lot info
        Infos.LotInfo parentLotInfo = lotGeneralTestCase.getLotInfos(lots).get(0);
        Assert.isTrue(parentLotInfo.getLotBasicInfo().getAutoDispatchControlFlag(), "test fail");
        //【step7】get child lot info
        Infos.LotInfo childLotInfo = lotGeneralTestCase.getLotInfos(Arrays.asList(childLot)).get(0);
        Assert.isTrue(childLotInfo.getLotBasicInfo().getAutoDispatchControlFlag(), "test fail");
    }
}