package com.fa.cim.newIntegration.equipment.scase;

import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.Response;
import com.fa.cim.common.utils.CimObjectUtils;
import com.fa.cim.common.utils.Validations;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.core.TestCommonData;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Results;
import com.fa.cim.newIntegration.tcase.ElectronicInformationTestCase;
import com.fa.cim.newIntegration.tcase.EquipmentTestCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.List;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/9/14       ********             lightyh             create file
 *
 * @author: lightyh
 * @date: 2019/9/14 11:39
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Service
@Slf4j
public class LotUnloadFromEquipmentCase {

    @Autowired
    private LotLoadToEquipmentCase lotLoadToEquipmentCase;

    @Autowired
    private EquipmentTestCase equipmentTestCase;

    @Autowired
    private ElectronicInformationTestCase electronicInformationTestCase;

    @Autowired
    private MoveOutCase moveOutCase;

    @Autowired
    private MoveInCase moveInCase;

    @Autowired
    private MoveInCancelCase moveInCancelCase;

    @Autowired
    private RetCodeConfig retCodeConfig;

    @Autowired
    @Qualifier("testCommonDataWithDB")
    private TestCommonData testCommonData;

    public Response manualUnload_Normal(){
        //【step1】load
        ObjectIdentifier cassetteID = lotLoadToEquipmentCase.manual_Loading_Without_StartLotsReservation();
        //【step2】load purpose
        List<String> purposeList = (List<String>) equipmentTestCase.getLoadPurposeTypeCase(BizConstant.SP_LOADPURPOSETYPE_PROCESSLOT).getBody();
        return equipmentTestCase.uncarrierLoadingRpt(testCommonData.getEQUIPMENTID(), cassetteID, testCommonData.getPROTID(), purposeList.get(0));
    }

    public void unLoad_WithSpecifiedEqpAndCassettes(ObjectIdentifier equipmentID, List<ObjectIdentifier> cassetteIDList){
        //【step1】load purpose
        List<String> purposeList = (List<String>) equipmentTestCase.getLoadPurposeTypeCase(BizConstant.SP_LOADPURPOSETYPE_PROCESSLOT).getBody();
        //【step2】einfo/eqp_info/inq
        Results.EqpInfoInqResult eqpInfoInqResult = (Results.EqpInfoInqResult) electronicInformationTestCase.eqpInfoInqCase(equipmentID).getBody();
        int count = 0;
        for (ObjectIdentifier cassetteID : cassetteIDList){
            equipmentTestCase.uncarrierLoadingRpt(equipmentID, cassetteID, eqpInfoInqResult.getEquipmentPortInfo().getEqpPortStatuses().get(count).getPortID(), purposeList.get(0));
            count++;
        }
    }

    public void manualUnloadAferMoveOut(){
        //【step1】move out
        List<Infos.LotInfo> lotInfoList = moveOutCase.moveOut_Normal();
        //【step2】einfo/eqp_info/inq
        Results.EqpInfoInqResult eqpInfoInqResult = (Results.EqpInfoInqResult) electronicInformationTestCase.eqpInfoInqCase(testCommonData.getEQUIPMENTID()).getBody();
        ObjectIdentifier loadedCassetteID = eqpInfoInqResult.getEquipmentPortInfo().getEqpPortStatuses().get(0).getLoadedCassetteID();
        Assert.isTrue(CimObjectUtils.equalsWithValue(loadedCassetteID, lotInfoList.get(0).getLotLocationInfo().getCassetteID()), "test fail");
        // 【step3】unload
        List<String> purposeList = (List<String>) equipmentTestCase.getLoadPurposeTypeCase(BizConstant.SP_LOADPURPOSETYPE_PROCESSLOT).getBody();
        equipmentTestCase.uncarrierLoadingRpt(testCommonData.getEQUIPMENTID(), lotInfoList.get(0).getLotLocationInfo().getCassetteID(), testCommonData.getPROTID(), purposeList.get(0));

    }

    public void manualUnloadAferMoveInCancel(){
        //【step1】move in cancel
        List<Infos.StartCassette> startCassetteList = moveInCancelCase.moveInCancel_Normal();
        //【step2】einfo/eqp_info/inq
        Results.EqpInfoInqResult eqpInfoInqResult = (Results.EqpInfoInqResult) electronicInformationTestCase.eqpInfoInqCase(testCommonData.getEQUIPMENTID()).getBody();
        ObjectIdentifier loadedCassetteID = eqpInfoInqResult.getEquipmentPortInfo().getEqpPortStatuses().get(0).getLoadedCassetteID();
        Assert.isTrue(CimObjectUtils.equalsWithValue(loadedCassetteID, startCassetteList.get(0).getCassetteID()), "test fail");
        // 【step3】unload
        List<String> purposeList = (List<String>) equipmentTestCase.getLoadPurposeTypeCase(BizConstant.SP_LOADPURPOSETYPE_PROCESSLOT).getBody();
        equipmentTestCase.uncarrierLoadingRpt(testCommonData.getEQUIPMENTID(), startCassetteList.get(0).getCassetteID(), testCommonData.getPROTID(), purposeList.get(0));

    }

    public void manualUnloadWhenCarrierInProcessing(){
        //【step1】move in
        Results.MoveInReqResult moveInReqResult = moveInCase.moveIn_GenerateControlJob_Without_StartReservation();
        Assert.isTrue(moveInReqResult.getControlJobID() != null, "test fail");
        //【step2】einfo/eqp_info/inq
        Results.EqpInfoInqResult eqpInfoInqResult = (Results.EqpInfoInqResult) electronicInformationTestCase.eqpInfoInqCase(testCommonData.getEQUIPMENTID()).getBody();
        ObjectIdentifier loadedCassetteID = eqpInfoInqResult.getEquipmentPortInfo().getEqpPortStatuses().get(0).getLoadedCassetteID();
        Assert.isTrue(CimObjectUtils.equalsWithValue(loadedCassetteID, moveInReqResult.getStartCassetteList().get(0).getCassetteID()), "test fail");
        // 【step3】unload
        List<String> purposeList = (List<String>) equipmentTestCase.getLoadPurposeTypeCase(BizConstant.SP_LOADPURPOSETYPE_PROCESSLOT).getBody();
        try {
            equipmentTestCase.uncarrierLoadingRpt(testCommonData.getEQUIPMENTID(), moveInReqResult.getStartCassetteList().get(0).getCassetteID(), testCommonData.getPROTID(), purposeList.get(0));
            Assert.isTrue(false, "test fail");
        } catch (ServiceException e) {
            Assert.isTrue(Validations.isEquals(retCodeConfig.getInvalidLotProcessState(), e.getCode()), e.getMessage());
        }
    }

    public void unLoadCarrierWithAuto1OperationMode(){
        //【step1】einfo/eqp_info/inq
        Results.EqpInfoInqResult eqpInfoInqResult = (Results.EqpInfoInqResult) electronicInformationTestCase.eqpInfoInqCase(testCommonData.getEQUIPMENTID()).getBody();
        //【step2】change eqp mode
        equipmentTestCase.changeOperationModeToAuto(testCommonData.getEQUIPMENTID(), "Auto-1");
        //【step3】change port status
        equipmentTestCase.changePortStatus(testCommonData.getEQUIPMENTID(), Arrays.asList(eqpInfoInqResult.getEquipmentPortInfo().getEqpPortStatuses().get(0).getAssociatedPortID()), "LoadComp");
        //【step4】load
        ObjectIdentifier cassetteID = lotLoadToEquipmentCase.manual_Loading_Without_StartLotsReservation();
        // 【step5】unload
        List<String> purposeList = (List<String>) equipmentTestCase.getLoadPurposeTypeCase(BizConstant.SP_LOADPURPOSETYPE_PROCESSLOT).getBody();
        equipmentTestCase.uncarrierLoadingRpt(testCommonData.getEQUIPMENTID(), cassetteID, testCommonData.getPROTID(), purposeList.get(0));
    }
}