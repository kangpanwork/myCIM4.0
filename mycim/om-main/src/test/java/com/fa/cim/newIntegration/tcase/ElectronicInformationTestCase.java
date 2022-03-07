package com.fa.cim.newIntegration.tcase;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.Response;
import com.fa.cim.common.utils.Validations;
import com.fa.cim.controller.einfo.ElectronicInformationInqController;
import com.fa.cim.controller.interfaces.dynamicOperationControl.IDynamicOperationInqController;
import com.fa.cim.controller.interfaces.equipment.IEquipmentInqController;
import com.fa.cim.controller.interfaces.lot.ILotInqController;
import com.fa.cim.core.TestCommonData;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/9/11          ********            Nyx                create file
 *
 * @author: Nyx
 * @date: 2019/9/11 9:54
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Service
@Slf4j
public class ElectronicInformationTestCase {

    @Autowired
    @Qualifier("testCommonDataWithDB")
    private TestCommonData testCommonData;

    @Autowired
    private ElectronicInformationInqController electronicInformationInqController;
    @Autowired
    private ILotInqController lotInqController;
    @Autowired
    private IEquipmentInqController equipmentInqController;
    @Autowired
    private IDynamicOperationInqController dynamicOperationInqController;

    public Response lotListByCarrierInqCase(ObjectIdentifier cassetteID) {
        Params.LotListByCarrierInqParams lotListByCarrierInqParams = new Params.LotListByCarrierInqParams();
        lotListByCarrierInqParams.setUser(testCommonData.getUSER());
        lotListByCarrierInqParams.setCassetteID(cassetteID);
        return lotInqController.lotListByCarrierInq(lotListByCarrierInqParams);
    }

    public List<Infos.ControlJobInfo> lotListByCJInqCase(Params.LotListByCJInqParams lotListByCJInqParams){
        return (List<Infos.ControlJobInfo>) lotInqController.lotListByCJInq(lotListByCJInqParams).getBody();
    }

    public Results.EqpInfoInqResult getEqpInfo(ObjectIdentifier equipmentID){
        return (Results.EqpInfoInqResult) this.eqpInfoInqCase(equipmentID).getBody();
    }

    public Response eqpInfoInqCase(ObjectIdentifier equipmentID) {
        Params.EqpInfoInqParams eqpInfoInqParams = new Params.EqpInfoInqParams();
        eqpInfoInqParams.setUser(testCommonData.getUSER());
        eqpInfoInqParams.setEquipmentID(equipmentID);
        eqpInfoInqParams.setRequestFlagForBasicInfo(true);
        eqpInfoInqParams.setRequestFlagForChamberInfo(true);
        eqpInfoInqParams.setRequestFlagForEqpContainerInfo(true);
        eqpInfoInqParams.setRequestFlagForInprocessingLotInfo(true);
        eqpInfoInqParams.setRequestFlagForPMInfo(true);
        eqpInfoInqParams.setRequestFlagForPortInfo(true);
        eqpInfoInqParams.setRequestFlagForRSPPortInfo(true);
        eqpInfoInqParams.setRequestFlagForReservedControlJobInfo(true);
        eqpInfoInqParams.setRequestFlagForStatusInfo(true);
        eqpInfoInqParams.setRequestFlagForStockerInfo(true);
        return equipmentInqController.eqpInfoInq(eqpInfoInqParams);
    }

    public Response EqpInfoForInternalBufferCase(String eqpID) {
        Params.EqpInfoForIBInqParams eqpInfoForIBInqParams = new Params.EqpInfoForIBInqParams();
        eqpInfoForIBInqParams.setUser(testCommonData.getUSER());
        eqpInfoForIBInqParams.setRequestFlagForBasicInfo(true);
        eqpInfoForIBInqParams.setRequestFlagForStatusInfo(true);
        eqpInfoForIBInqParams.setRequestFlagForPMInfo(true);
        eqpInfoForIBInqParams.setRequestFlagForPortInfo(true);
        eqpInfoForIBInqParams.setRequestFlagForChamberInfo(true);
        eqpInfoForIBInqParams.setRequestFlagForInternalBufferInfo(true);
        eqpInfoForIBInqParams.setRequestFlagForStockerInfo(true);
        eqpInfoForIBInqParams.setRequestFlagForInprocessingLotInfo(true);
        eqpInfoForIBInqParams.setRequestFlagForReservedControlJobInfo(true);
        eqpInfoForIBInqParams.setRequestFlagForRSPPortInfo(true);
        eqpInfoForIBInqParams.setEquipmentID(new ObjectIdentifier(eqpID));
        Response response = equipmentInqController.eqpInfoForIBInq(eqpInfoForIBInqParams);
        Assert.isTrue(Validations.isSuccess(response.getCode()),"Eqp info Error");
        return response;
    }

    public Response processFlowOpeListWithNestInqCase(ObjectIdentifier routeID){
        Params.ProcessFlowOpeListWithNestInqParam param = new Params.ProcessFlowOpeListWithNestInqParam();
        param.setFromOperationNumber("");
        param.setNestLevel(0L);
        param.setRouteID(routeID);
        param.setUser(testCommonData.getUSER());
        return dynamicOperationInqController.processFlowOpeListWithNestInq(param);
    }

    public static final String EQUIPMENT_CATEGORY_PROCESS = "Process";
    public static final String EQUIPMENT_CATEGORY_MEASUREMENT = "Measurement";
    public List<Infos.AreaEqp> eqpListByBayInqCase(String equipmentCategory) {
        Params.EqpListByBayInqInParm eqpListByBayInqInParm = new Params.EqpListByBayInqInParm();
        eqpListByBayInqInParm.setUser(testCommonData.getUSER());
        if (EQUIPMENT_CATEGORY_PROCESS.equals(equipmentCategory)) {
            eqpListByBayInqInParm.setEquipmentCategory(EQUIPMENT_CATEGORY_PROCESS);
        } else if (EQUIPMENT_CATEGORY_MEASUREMENT.equals(equipmentCategory)) {
            eqpListByBayInqInParm.setEquipmentCategory(EQUIPMENT_CATEGORY_MEASUREMENT);
        }
        return ((Results.EqpListByBayInqResult) equipmentInqController.eqpListByBayInq(eqpListByBayInqInParm).getBody()).getStrAreaEqp();
    }

    public List<Infos.AreaEqp> eqpListByStepInqCase(ObjectIdentifier operationID, ObjectIdentifier productID) {
        Params.EqpListByStepInqParm eqpListByStepInqParm = new Params.EqpListByStepInqParm();
        eqpListByStepInqParm.setUser(testCommonData.getUSER());
        eqpListByStepInqParm.setOperationID(operationID);
        eqpListByStepInqParm.setProductID(productID);
        return (List<Infos.AreaEqp>) equipmentInqController.eqpListByStepInq(eqpListByStepInqParm).getBody();
    }

}