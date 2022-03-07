package com.fa.cim.newIntegration.processControl.scase;

import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.Response;
import com.fa.cim.common.support.User;
import com.fa.cim.common.utils.CimArrayUtils;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.controller.constraint.ConstraintController;
import com.fa.cim.controller.constraint.ConstraintInqController;
import com.fa.cim.controller.durable.DurableController;
import com.fa.cim.controller.einfo.ElectronicInformationInqController;
import com.fa.cim.controller.equipment.EquipmentController;
import com.fa.cim.controller.equipment.EquipmentInqController;
import com.fa.cim.controller.flowbatch.FlowBatchController;
import com.fa.cim.controller.lot.LotController;
import com.fa.cim.controller.system.SystemInqController;
import com.fa.cim.controller.tms.TransferManagementSystemController;
import com.fa.cim.core.TestCommonData;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;
import com.fa.cim.entity.runtime.eqp.CimEquipmentDO;
import com.fa.cim.jpa.CimJpaRepository;
import com.fa.cim.newIntegration.common.TestUtils;
import com.fa.cim.newIntegration.stb.scase.STBCase;
import com.fa.cim.newIntegration.tcase.LotGeneralTestCase;
import com.fa.cim.newcore.bo.machine.CimMachine;
import com.fa.cim.newcore.factory.BaseCoreFactory;
import com.fa.cim.newcore.standard.mchnmngm.ProcessResource;
import com.fa.cim.jpa.SearchCondition;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * description:
 * <p>EntityInhibityCase .<br/></p>
 * <p>
 * change history:
 * date      defect#       person     comments
 * ------------------------------------------------------------
 * ---------------------------------------------------------
 * 2019/11/26/026   ********     Decade     create file
 *
 * @author: Decade
 * @date: 2019/11/26/026 16:54
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@Service
public class EntityInhibityCase {
    @Autowired
    private STBCase stbCase;

    @Autowired
    private ConstraintController constraintController;

    @Autowired
    private EquipmentController equipmentController;

    @Autowired
    private SystemInqController systemInqController;

    @Autowired
    private ElectronicInformationInqController electronicInformationInqController;

    @Autowired
    private EquipmentInqController equipmentInqController;

    @Autowired
    private LotGeneralTestCase lotGeneralTestCase;

    @Autowired
    private ConstraintInqController constraintInqController;

    @Autowired
    private LotController lotController;

    @Autowired
    private DurableController durableController;

    @Autowired
    private TransferManagementSystemController transferManagementSystemController;

    @Autowired
    private FlowBatchController flowBatchController;

    @Autowired
    private TestUtils testUtils;

    @Autowired
    @Qualifier("testCommonDataWithDB")
    private TestCommonData testCommonData;

    @Autowired
    private RetCodeConfig retCodeConfig;

    private User getUser() {
        User user = new User();
        user.setUserID(new ObjectIdentifier("ADMIN"));
        user.setPassword("b51fa595e692d53739b69131cdc73440");
        //return testCommonData.getUSER();
        return user;
    }

    public void Inhibited_Equipment_Chamber_EnvironmentVariableIs0_ToLoad(){
        //【STEP1】STB a PoC.Product01
        ObjectIdentifier lotID = ((Results.WaferLotStartReqResult) stbCase.STB_PoC_Product01().getBody()).getLotID();
        List<ObjectIdentifier> lotIDList = new ArrayList<>();
        lotIDList.add(lotID);
        Results.LotInfoInqResult lotInfoInqResult = (Results.LotInfoInqResult) lotGeneralTestCase.getLotInfoCase(lotIDList).getBody();
        Infos.LotInfo lotInfo = lotInfoInqResult.getLotInfoList().get(0);
        ObjectIdentifier cassetteID = lotInfo.getLotLocationInfo().getCassetteID();
        //【STEP2】SKIP TO 7000.0100
        testUtils.skip(lotID,"7000.0100");
        //【STEP3】entityInhibit the eqp and chamber
        Params.MfgRestrictReqParams mfgRestrictReqParams = new Params.MfgRestrictReqParams();
        mfgRestrictReqParams.setUser(getUser());
        Infos.EntityInhibitDetailAttributes entityInhibitDetailAttributes = this.getNormalEIDAParamas();
        List<Infos.EntityIdentifier> entityIdentifiers = new ArrayList<>();
        Infos.EntityIdentifier entityIdentifier = new Infos.EntityIdentifier();
        entityIdentifier.setClassName("Chamber");
        entityIdentifier.setAttribution("ChamberA");
        ObjectIdentifier aMachineID = ObjectIdentifier.buildWithValue("2CHB01");
        entityIdentifier.setObjectID(aMachineID);
        entityIdentifiers.add(entityIdentifier);
        entityInhibitDetailAttributes.setEntities(entityIdentifiers);
        mfgRestrictReqParams.setEntityInhibitDetailAttributes(entityInhibitDetailAttributes);
        Response mfgRestrictReq = constraintController.mfgRestrictReq(mfgRestrictReqParams);
        Infos.EntityInhibitDetailAttributes attributes = (Infos.EntityInhibitDetailAttributes)mfgRestrictReq.getBody();
        //【STEP4】Try Load
        Params.loadOrUnloadLotRptParams loadOrUnloadLotRptParams = new Params.loadOrUnloadLotRptParams();
        loadOrUnloadLotRptParams.setUser(getUser());
        loadOrUnloadLotRptParams.setEquipmentID(aMachineID);
        loadOrUnloadLotRptParams.setCassetteID(cassetteID);
        loadOrUnloadLotRptParams.setPortID(ObjectIdentifier.buildWithValue("P1"));
        loadOrUnloadLotRptParams.setLoadPurposeType("Process Lot");
        try {
            equipmentController.carrierLoadingRpt(loadOrUnloadLotRptParams);
            throw new ServiceException(retCodeConfig.getError());
        }catch (ServiceException e){
            if (e.getCode()!=2800){
                throw new ServiceException(retCodeConfig.getError());
            }
        }
        //【STEP5】Restriction Cancel and retry
        this.restriction_Cancel(attributes);
        equipmentController.carrierLoadingRpt(loadOrUnloadLotRptParams);
    }

    public void Inhibited_Equipment_Chamber_EnvironmentVariableIs0_MoveIn(){
        //【STEP1】STB a PoC.Product01
        ObjectIdentifier lotID = ((Results.WaferLotStartReqResult) stbCase.STB_PoC_Product01().getBody()).getLotID();
        List<ObjectIdentifier> lotIDList = new ArrayList<>();
        lotIDList.add(lotID);
        Results.LotInfoInqResult lotInfoInqResult = (Results.LotInfoInqResult) lotGeneralTestCase.getLotInfoCase(lotIDList).getBody();
        Infos.LotInfo lotInfo = lotInfoInqResult.getLotInfoList().get(0);
        ObjectIdentifier cassetteID = lotInfo.getLotLocationInfo().getCassetteID();
        List<ObjectIdentifier> cassetteIDs = new ArrayList<>();
        cassetteIDs.add(cassetteID);
        //【STEP2】SKIP TO 7000.0100
        testUtils.skip(lotID,"7000.0100");
        //【STEP3】LOAD
        Params.loadOrUnloadLotRptParams loadOrUnloadLotRptParams = new Params.loadOrUnloadLotRptParams();
        ObjectIdentifier aMachineID = ObjectIdentifier.buildWithValue("2CHB01");
        loadOrUnloadLotRptParams.setUser(getUser());
        loadOrUnloadLotRptParams.setEquipmentID(aMachineID);
        loadOrUnloadLotRptParams.setCassetteID(cassetteID);
        loadOrUnloadLotRptParams.setPortID(ObjectIdentifier.buildWithValue("P1"));
        loadOrUnloadLotRptParams.setLoadPurposeType("Process Lot");
        equipmentController.carrierLoadingRpt(loadOrUnloadLotRptParams);
        //【STEP4】entityInhibit the eqp and chamber
        Params.MfgRestrictReqParams mfgRestrictReqParams = new Params.MfgRestrictReqParams();
        mfgRestrictReqParams.setUser(getUser());
        Infos.EntityInhibitDetailAttributes entityInhibitDetailAttributes = this.getNormalEIDAParamas();
        List<Infos.EntityIdentifier> entityIdentifiers = new ArrayList<>();
        Infos.EntityIdentifier entityIdentifier = new Infos.EntityIdentifier();
        entityIdentifier.setClassName("Chamber");
        entityIdentifier.setAttribution("ChamberA");
        entityIdentifier.setObjectID(aMachineID);
        entityIdentifiers.add(entityIdentifier);
        entityInhibitDetailAttributes.setEntities(entityIdentifiers);
        mfgRestrictReqParams.setEntityInhibitDetailAttributes(entityInhibitDetailAttributes);
        Response mfgRestrictReq = constraintController.mfgRestrictReq(mfgRestrictReqParams);
        Infos.EntityInhibitDetailAttributes attributes = (Infos.EntityInhibitDetailAttributes)mfgRestrictReq.getBody();
        //【STEP5】get cassette list
        Params.LotsMoveInInfoInqParams lotsMoveInInfoInqParams = new Params.LotsMoveInInfoInqParams();
        lotsMoveInInfoInqParams.setUser(getUser());
        lotsMoveInInfoInqParams.setEquipmentID(aMachineID);
        lotsMoveInInfoInqParams.setCassetteIDs(cassetteIDs);
        Response response = equipmentInqController.LotsMoveInInfoInq(lotsMoveInInfoInqParams);
        Results.LotsMoveInInfoInqResult lotsMoveInInfoInqResult = (Results.LotsMoveInInfoInqResult)response.getBody();
        //【STEP6】try move in
        Params.MoveInReqParams moveInReqParams = new Params.MoveInReqParams();
        moveInReqParams.setUser(getUser());
        moveInReqParams.setProcessJobPauseFlag(false);
        moveInReqParams.setEquipmentID(aMachineID);
        moveInReqParams.setPortGroupID("PG1");
        moveInReqParams.setStartCassetteList(lotsMoveInInfoInqResult.getStartCassetteList());
        try {
            equipmentController.moveInReq(moveInReqParams);
            throw new ServiceException(retCodeConfig.getError());
        }catch (ServiceException e){
            if (e.getCode()!=979){
                throw new ServiceException(retCodeConfig.getError());
            }
        }
        //【STEP7】Restriction Cancel and retry
        this.restriction_Cancel(attributes);
        equipmentController.moveInReq(moveInReqParams);
    }

    public void Inhibited_Equipment_MachineRecipe_EnvironmentVariableIs0_ToMoveIn(){
        //【STEP1】STB a PoC.Product01
        ObjectIdentifier lotID = ((Results.WaferLotStartReqResult) stbCase.STB_PoC_Product01().getBody()).getLotID();
        List<ObjectIdentifier> lotIDList = new ArrayList<>();
        lotIDList.add(lotID);
        Results.LotInfoInqResult lotInfoInqResult = (Results.LotInfoInqResult) lotGeneralTestCase.getLotInfoCase(lotIDList).getBody();
        Infos.LotInfo lotInfo = lotInfoInqResult.getLotInfoList().get(0);
        ObjectIdentifier cassetteID = lotInfo.getLotLocationInfo().getCassetteID();
        List<ObjectIdentifier> cassetteIDs = new ArrayList<>();
        cassetteIDs.add(cassetteID);
        //【STEP2】SKIP TO 7000.0100
        testUtils.skip(lotID,"7000.0100");
        //【STEP3】LOAD
        Params.loadOrUnloadLotRptParams loadOrUnloadLotRptParams = new Params.loadOrUnloadLotRptParams();
        ObjectIdentifier aMachineID = ObjectIdentifier.buildWithValue("2CHB01");
        loadOrUnloadLotRptParams.setUser(getUser());
        loadOrUnloadLotRptParams.setEquipmentID(aMachineID);
        loadOrUnloadLotRptParams.setCassetteID(cassetteID);
        loadOrUnloadLotRptParams.setPortID(ObjectIdentifier.buildWithValue("P1"));
        loadOrUnloadLotRptParams.setLoadPurposeType("Process Lot");
        equipmentController.carrierLoadingRpt(loadOrUnloadLotRptParams);
        //【STEP4】entityInhibit the eqp and recipe
        Params.MfgRestrictReqParams mfgRestrictReqParams = new Params.MfgRestrictReqParams();
        mfgRestrictReqParams.setUser(getUser());
        Infos.EntityInhibitDetailAttributes entityInhibitDetailAttributes = this.getNormalEIDAParamas();
        List<Infos.EntityIdentifier> entityIdentifiers = new ArrayList<>();
        Infos.EntityIdentifier entityIdentifier1 = new Infos.EntityIdentifier();
        entityIdentifier1.setClassName("Equipment");
        entityIdentifier1.setObjectID(aMachineID);
        Infos.EntityIdentifier entityIdentifier2 = new Infos.EntityIdentifier();
        entityIdentifier2.setClassName("Machine Recipe");
        entityIdentifier2.setObjectID(ObjectIdentifier.buildWithValue("POC.CHB02.01"));
        entityIdentifiers.add(entityIdentifier1);
        entityIdentifiers.add(entityIdentifier2);
        entityInhibitDetailAttributes.setEntities(entityIdentifiers);
        mfgRestrictReqParams.setEntityInhibitDetailAttributes(entityInhibitDetailAttributes);
        Response mfgRestrictReq = constraintController.mfgRestrictReq(mfgRestrictReqParams);
        Infos.EntityInhibitDetailAttributes attributes = (Infos.EntityInhibitDetailAttributes)mfgRestrictReq.getBody();
        //【STEP5】get cassette list
        Params.LotsMoveInInfoInqParams lotsMoveInInfoInqParams = new Params.LotsMoveInInfoInqParams();
        lotsMoveInInfoInqParams.setUser(getUser());
        lotsMoveInInfoInqParams.setEquipmentID(aMachineID);
        lotsMoveInInfoInqParams.setCassetteIDs(cassetteIDs);
        Response response = equipmentInqController.LotsMoveInInfoInq(lotsMoveInInfoInqParams);
        Results.LotsMoveInInfoInqResult lotsMoveInInfoInqResult = (Results.LotsMoveInInfoInqResult)response.getBody();
        //【STEP6】try move in
        Params.MoveInReqParams moveInReqParams = new Params.MoveInReqParams();
        moveInReqParams.setUser(getUser());
        moveInReqParams.setProcessJobPauseFlag(false);
        moveInReqParams.setEquipmentID(aMachineID);
        moveInReqParams.setPortGroupID("PG1");
        moveInReqParams.setStartCassetteList(lotsMoveInInfoInqResult.getStartCassetteList());
        try {
            equipmentController.moveInReq(moveInReqParams);
            throw new ServiceException(retCodeConfig.getError());
        }catch (ServiceException e){
            if (e.getCode()!=979){
                throw new ServiceException(retCodeConfig.getError());
            }
        }
        //【STEP7】Restriction Cancel and retry
        this.restriction_Cancel(attributes);
        equipmentController.moveInReq(moveInReqParams);
    }

    public void Inhibited_Equipment_Chamber_MachineRecipe_EnvironmentVariableIs0_ToMoveIn(){
        //【STEP1】STB a PoC.Product01
        ObjectIdentifier lotID = ((Results.WaferLotStartReqResult) stbCase.STB_PoC_Product01().getBody()).getLotID();
        List<ObjectIdentifier> lotIDList = new ArrayList<>();
        lotIDList.add(lotID);
        Results.LotInfoInqResult lotInfoInqResult = (Results.LotInfoInqResult) lotGeneralTestCase.getLotInfoCase(lotIDList).getBody();
        Infos.LotInfo lotInfo = lotInfoInqResult.getLotInfoList().get(0);
        ObjectIdentifier cassetteID = lotInfo.getLotLocationInfo().getCassetteID();
        List<ObjectIdentifier> cassetteIDs = new ArrayList<>();
        cassetteIDs.add(cassetteID);
        //【STEP2】SKIP TO 7000.0100
        testUtils.skip(lotID,"7000.0100");
        //【STEP3】LOAD
        Params.loadOrUnloadLotRptParams loadOrUnloadLotRptParams = new Params.loadOrUnloadLotRptParams();
        ObjectIdentifier aMachineID = ObjectIdentifier.buildWithValue("2CHB01");
        loadOrUnloadLotRptParams.setUser(getUser());
        loadOrUnloadLotRptParams.setEquipmentID(aMachineID);
        loadOrUnloadLotRptParams.setCassetteID(cassetteID);
        loadOrUnloadLotRptParams.setPortID(ObjectIdentifier.buildWithValue("P1"));
        loadOrUnloadLotRptParams.setLoadPurposeType("Process Lot");
        equipmentController.carrierLoadingRpt(loadOrUnloadLotRptParams);
        //【STEP4】entityInhibit the eqp and recipe
        Params.MfgRestrictReqParams mfgRestrictReqParams = new Params.MfgRestrictReqParams();
        mfgRestrictReqParams.setUser(getUser());
        Infos.EntityInhibitDetailAttributes entityInhibitDetailAttributes = this.getNormalEIDAParamas();
        List<Infos.EntityIdentifier> entityIdentifiers = new ArrayList<>();
        Infos.EntityIdentifier entityIdentifier1 = new Infos.EntityIdentifier();
        entityIdentifier1.setClassName("Equipment");
        entityIdentifier1.setObjectID(aMachineID);
        Infos.EntityIdentifier entityIdentifier2 = new Infos.EntityIdentifier();
        entityIdentifier2.setClassName("Machine Recipe");
        entityIdentifier2.setObjectID(ObjectIdentifier.buildWithValue("POC.CHB02.01"));
        Infos.EntityIdentifier entityIdentifier3 = new Infos.EntityIdentifier();
        entityIdentifier3.setClassName("Chamber");
        entityIdentifier3.setAttribution("ChamberA");
        entityIdentifier3.setObjectID(aMachineID);
        entityIdentifiers.add(entityIdentifier1);
        entityIdentifiers.add(entityIdentifier2);
        entityIdentifiers.add(entityIdentifier3);
        entityInhibitDetailAttributes.setEntities(entityIdentifiers);
        mfgRestrictReqParams.setEntityInhibitDetailAttributes(entityInhibitDetailAttributes);
        Response mfgRestrictReq = constraintController.mfgRestrictReq(mfgRestrictReqParams);
        Infos.EntityInhibitDetailAttributes attributes = (Infos.EntityInhibitDetailAttributes)mfgRestrictReq.getBody();
        //【STEP5】get cassette list
        Params.LotsMoveInInfoInqParams lotsMoveInInfoInqParams = new Params.LotsMoveInInfoInqParams();
        lotsMoveInInfoInqParams.setUser(getUser());
        lotsMoveInInfoInqParams.setEquipmentID(aMachineID);
        lotsMoveInInfoInqParams.setCassetteIDs(cassetteIDs);
        Response response = equipmentInqController.LotsMoveInInfoInq(lotsMoveInInfoInqParams);
        Results.LotsMoveInInfoInqResult lotsMoveInInfoInqResult = (Results.LotsMoveInInfoInqResult)response.getBody();
        //【STEP6】try move in
        Params.MoveInReqParams moveInReqParams = new Params.MoveInReqParams();
        moveInReqParams.setUser(getUser());
        moveInReqParams.setProcessJobPauseFlag(false);
        moveInReqParams.setEquipmentID(aMachineID);
        moveInReqParams.setPortGroupID("PG1");
        moveInReqParams.setStartCassetteList(lotsMoveInInfoInqResult.getStartCassetteList());
        try {
            equipmentController.moveInReq(moveInReqParams);
            throw new ServiceException(retCodeConfig.getError());
        }catch (ServiceException e){
            if (e.getCode()!=979){
                throw new ServiceException(retCodeConfig.getError());
            }
        }
        //【STEP7】Restriction Cancel and retry
        this.restriction_Cancel(attributes);
        equipmentController.moveInReq(moveInReqParams);
    }

    public void Inhibited_Product_EnvironmentVariableIs1_CheckRestrictionFlag(){
        //【STEP1】STB
        ObjectIdentifier lotID = ((Results.WaferLotStartReqResult) stbCase.STB_NotPreparedLot().getBody()).getLotID();
        List<ObjectIdentifier> lotIDList = new ArrayList<>();
        lotIDList.add(lotID);
        Results.LotInfoInqResult lotInfoInqResult = (Results.LotInfoInqResult) lotGeneralTestCase.getLotInfoCase(lotIDList).getBody();
        Infos.LotInfo lotInfo = lotInfoInqResult.getLotInfoList().get(0);
        ObjectIdentifier cassetteID = lotInfo.getLotLocationInfo().getCassetteID();
        ObjectIdentifier productSpecificationID = lotInfo.getLotProductInfo().getProductID();
        //【STEP2】SET mfgRestrictReqParams
        Params.MfgRestrictReqParams mfgRestrictReqParams = new Params.MfgRestrictReqParams();
        Infos.EntityInhibitDetailAttributes entityInhibitDetailAttributes = this.getNormalEIDAParamas();
        List<Infos.EntityIdentifier> entityIdentifiers = new ArrayList<>();
        Infos.EntityIdentifier entityIdentifier = new Infos.EntityIdentifier();
        entityIdentifier.setClassName("Product Specification");
        entityIdentifier.setObjectID(productSpecificationID);
        entityIdentifiers.add(entityIdentifier);
        entityInhibitDetailAttributes.setEntities(entityIdentifiers);
        mfgRestrictReqParams.setUser(getUser());
        mfgRestrictReqParams.setEntityInhibitDetailAttributes(entityInhibitDetailAttributes);
        //【STEP3】entityInhibit the product
        Response mfgRestrictReq = constraintController.mfgRestrictReq(mfgRestrictReqParams);
        Infos.EntityInhibitDetailAttributes attributes = (Infos.EntityInhibitDetailAttributes)mfgRestrictReq.getBody();
        //【STEP4】Get Eqp
        ObjectIdentifier aMachineID = testCommonData.getEQUIPMENTID();
        ObjectIdentifier aPortID = testCommonData.getPROTID();
        //【STEP5】Try Load
        Params.loadOrUnloadLotRptParams loadOrUnloadLotRptParams = new Params.loadOrUnloadLotRptParams();
        loadOrUnloadLotRptParams.setUser(getUser());
        loadOrUnloadLotRptParams.setEquipmentID(aMachineID);
        loadOrUnloadLotRptParams.setCassetteID(cassetteID);
        loadOrUnloadLotRptParams.setPortID(aPortID);
        loadOrUnloadLotRptParams.setLoadPurposeType("Process Lot");
        try {
            equipmentController.carrierLoadingRpt(loadOrUnloadLotRptParams);
            throw new ServiceException(retCodeConfig.getError());
        }catch (ServiceException e){
            if (e.getCode()!=2800){
                throw new ServiceException(retCodeConfig.getError());
            }
        }
        //【STEP6】Restriction Cancel and retry
        this.restriction_Cancel(attributes);
        equipmentController.carrierLoadingRpt(loadOrUnloadLotRptParams);
    }

    public void Inhibited_ProcessFlow_EnvironmentVariableIs1_CheckRestrictionFlag(){
        //【STEP1】STB
        ObjectIdentifier lotID = ((Results.WaferLotStartReqResult) stbCase.STB_NotPreparedLot().getBody()).getLotID();
        List<ObjectIdentifier> lotIDList = new ArrayList<>();
        lotIDList.add(lotID);
        Results.LotInfoInqResult lotInfoInqResult = (Results.LotInfoInqResult) lotGeneralTestCase.getLotInfoCase(lotIDList).getBody();
        Infos.LotInfo lotInfo = lotInfoInqResult.getLotInfoList().get(0);
        ObjectIdentifier cassetteID = lotInfo.getLotLocationInfo().getCassetteID();
        ObjectIdentifier routeID = lotInfo.getLotOperationInfo().getRouteID();
        //【STEP2】SET mfgRestrictReqParams
        Params.MfgRestrictReqParams mfgRestrictReqParams = new Params.MfgRestrictReqParams();
        Infos.EntityInhibitDetailAttributes entityInhibitDetailAttributes = this.getNormalEIDAParamas();
        List<Infos.EntityIdentifier> entityIdentifiers = new ArrayList<>();
        Infos.EntityIdentifier entityIdentifier = new Infos.EntityIdentifier();
        entityIdentifier.setClassName("Route");
        entityIdentifier.setObjectID(routeID);
        entityIdentifiers.add(entityIdentifier);
        entityInhibitDetailAttributes.setEntities(entityIdentifiers);
        mfgRestrictReqParams.setUser(getUser());
        mfgRestrictReqParams.setEntityInhibitDetailAttributes(entityInhibitDetailAttributes);
        //【STEP3】entityInhibit the processFlow
        Response mfgRestrictReq = constraintController.mfgRestrictReq(mfgRestrictReqParams);
        Infos.EntityInhibitDetailAttributes attributes = (Infos.EntityInhibitDetailAttributes)mfgRestrictReq.getBody();
        //【STEP4】Get Eqp
        ObjectIdentifier aMachineID = testCommonData.getEQUIPMENTID();
        ObjectIdentifier aPortID = testCommonData.getPROTID();
        //【STEP5】Try Load
        Params.loadOrUnloadLotRptParams loadOrUnloadLotRptParams = new Params.loadOrUnloadLotRptParams();
        loadOrUnloadLotRptParams.setUser(getUser());
        loadOrUnloadLotRptParams.setEquipmentID(aMachineID);
        loadOrUnloadLotRptParams.setCassetteID(cassetteID);
        loadOrUnloadLotRptParams.setPortID(aPortID);
        loadOrUnloadLotRptParams.setLoadPurposeType("Process Lot");
        try {
            equipmentController.carrierLoadingRpt(loadOrUnloadLotRptParams);
            throw new ServiceException(retCodeConfig.getError());
        }catch (ServiceException e){
            if (e.getCode()!=2800){
                throw new ServiceException(retCodeConfig.getError());
            }
        }
        //【STEP6】Restriction Cancel and retry
        this.restriction_Cancel(attributes);
        equipmentController.carrierLoadingRpt(loadOrUnloadLotRptParams);
    }

    public void Inhibited_StartOperation_EnvironmentVariableIs1_CheckRestrictionFlag(){
        //【STEP1】STB
        ObjectIdentifier lotID = ((Results.WaferLotStartReqResult) stbCase.STB_NotPreparedLot().getBody()).getLotID();
        List<ObjectIdentifier> lotIDList = new ArrayList<>();
        lotIDList.add(lotID);
        Results.LotInfoInqResult lotInfoInqResult = (Results.LotInfoInqResult) lotGeneralTestCase.getLotInfoCase(lotIDList).getBody();
        Infos.LotInfo lotInfo = lotInfoInqResult.getLotInfoList().get(0);
        ObjectIdentifier cassetteID = lotInfo.getLotLocationInfo().getCassetteID();
        ObjectIdentifier routeID = lotInfo.getLotOperationInfo().getRouteID();
        String operationNumber = lotInfo.getLotOperationInfo().getOperationNumber();
        //【STEP2】SET mfgRestrictReqParams
        Params.MfgRestrictReqParams mfgRestrictReqParams = new Params.MfgRestrictReqParams();
        Infos.EntityInhibitDetailAttributes entityInhibitDetailAttributes = this.getNormalEIDAParamas();
        List<Infos.EntityIdentifier> entityIdentifiers = new ArrayList<>();
        Infos.EntityIdentifier entityIdentifier = new Infos.EntityIdentifier();
        entityIdentifier.setClassName("Operation");
        entityIdentifier.setObjectID(routeID);
        entityIdentifier.setAttribution(operationNumber);
        entityIdentifiers.add(entityIdentifier);
        entityInhibitDetailAttributes.setEntities(entityIdentifiers);
        mfgRestrictReqParams.setUser(getUser());
        mfgRestrictReqParams.setEntityInhibitDetailAttributes(entityInhibitDetailAttributes);
        //【STEP3】entityInhibit the StartOperation
        Response mfgRestrictReq = constraintController.mfgRestrictReq(mfgRestrictReqParams);
        Infos.EntityInhibitDetailAttributes attributes = (Infos.EntityInhibitDetailAttributes)mfgRestrictReq.getBody();
        //【STEP4】Get Eqp
        ObjectIdentifier aMachineID = testCommonData.getEQUIPMENTID();
        ObjectIdentifier aPortID = testCommonData.getPROTID();
        //【STEP5】Try Load
        Params.loadOrUnloadLotRptParams loadOrUnloadLotRptParams = new Params.loadOrUnloadLotRptParams();
        loadOrUnloadLotRptParams.setUser(getUser());
        loadOrUnloadLotRptParams.setEquipmentID(aMachineID);
        loadOrUnloadLotRptParams.setCassetteID(cassetteID);
        loadOrUnloadLotRptParams.setPortID(aPortID);
        loadOrUnloadLotRptParams.setLoadPurposeType("Process Lot");
        try {
            equipmentController.carrierLoadingRpt(loadOrUnloadLotRptParams);
            throw new ServiceException(retCodeConfig.getError());
        }catch (ServiceException e){
            if (e.getCode()!=2800){
                throw new ServiceException(retCodeConfig.getError());
            }
        }
        //【STEP6】Restriction Cancel and retry
        this.restriction_Cancel(attributes);
        equipmentController.carrierLoadingRpt(loadOrUnloadLotRptParams);
    }

    public  void Inhibited_StartOperation_Product_EnvironmentVariableIs1_CheckRestrictionFlag(){
        //【STEP1】STB
        ObjectIdentifier lotID = ((Results.WaferLotStartReqResult) stbCase.STB_NotPreparedLot().getBody()).getLotID();
        List<ObjectIdentifier> lotIDList = new ArrayList<>();
        lotIDList.add(lotID);
        Results.LotInfoInqResult lotInfoInqResult = (Results.LotInfoInqResult) lotGeneralTestCase.getLotInfoCase(lotIDList).getBody();
        Infos.LotInfo lotInfo = lotInfoInqResult.getLotInfoList().get(0);
        ObjectIdentifier cassetteID = lotInfo.getLotLocationInfo().getCassetteID();
        ObjectIdentifier routeID = lotInfo.getLotOperationInfo().getRouteID();
        String operationNumber = lotInfo.getLotOperationInfo().getOperationNumber();
        ObjectIdentifier productSpecificationID = lotInfo.getLotProductInfo().getProductID();
        //【STEP2】SET mfgRestrictReqParams
        Params.MfgRestrictReqParams mfgRestrictReqParams = new Params.MfgRestrictReqParams();
        Infos.EntityInhibitDetailAttributes entityInhibitDetailAttributes = this.getAllTypeEIDAParamas();
        List<Infos.EntityIdentifier> entities = new ArrayList<>();
        Infos.EntityIdentifier entity1 = new Infos.EntityIdentifier();
        Infos.EntityIdentifier entity2 = new Infos.EntityIdentifier();
        entity1.setClassName("Operation");
        entity1.setObjectID(routeID);
        entity1.setAttribution(operationNumber);
        entity2.setClassName("Product Specification");
        entity2.setObjectID(productSpecificationID);
        entities.add(entity1);
        entities.add(entity2);
        entityInhibitDetailAttributes.setEntities(entities);
        mfgRestrictReqParams.setUser(getUser());
        mfgRestrictReqParams.setEntityInhibitDetailAttributes(entityInhibitDetailAttributes);
        //【STEP3】entityInhibit the StartOperation and Product
        Response mfgRestrictReq = constraintController.mfgRestrictReq(mfgRestrictReqParams);
        Infos.EntityInhibitDetailAttributes attributes = (Infos.EntityInhibitDetailAttributes)mfgRestrictReq.getBody();
        //【STEP4】Get Eqp
        ObjectIdentifier aMachineID = testCommonData.getEQUIPMENTID();
        ObjectIdentifier aPortID = testCommonData.getPROTID();
        //【STEP5】Try Load
        Params.loadOrUnloadLotRptParams loadOrUnloadLotRptParams = new Params.loadOrUnloadLotRptParams();
        loadOrUnloadLotRptParams.setUser(getUser());
        loadOrUnloadLotRptParams.setEquipmentID(aMachineID);
        loadOrUnloadLotRptParams.setCassetteID(cassetteID);
        loadOrUnloadLotRptParams.setPortID(aPortID);
        loadOrUnloadLotRptParams.setLoadPurposeType("Process Lot");
        try {
            equipmentController.carrierLoadingRpt(loadOrUnloadLotRptParams);
            throw new ServiceException(retCodeConfig.getError());
        }catch (ServiceException e){
            if (e.getCode()!=2800){
                throw new ServiceException(retCodeConfig.getError());
            }
        }
        //【STEP6】Restriction Cancel and retry
        this.restriction_Cancel(attributes);
        equipmentController.carrierLoadingRpt(loadOrUnloadLotRptParams);
    }

    public void Inhibited_StartOperation_Product_Equipment_EnvironmentVariableIs1_CheckRestrictionFlag(){
        //【STEP1】STB
        ObjectIdentifier lotID = ((Results.WaferLotStartReqResult) stbCase.STB_NotPreparedLot().getBody()).getLotID();
        List<ObjectIdentifier> lotIDList = new ArrayList<>();
        lotIDList.add(lotID);
        Results.LotInfoInqResult lotInfoInqResult = (Results.LotInfoInqResult) lotGeneralTestCase.getLotInfoCase(lotIDList).getBody();
        Infos.LotInfo lotInfo = lotInfoInqResult.getLotInfoList().get(0);
        ObjectIdentifier cassetteID = lotInfo.getLotLocationInfo().getCassetteID();
        ObjectIdentifier routeID = lotInfo.getLotOperationInfo().getRouteID();
        String operationNumber = lotInfo.getLotOperationInfo().getOperationNumber();
        ObjectIdentifier productSpecificationID = lotInfo.getLotProductInfo().getProductID();
        ObjectIdentifier aMachineID = testCommonData.getEQUIPMENTID();
        //【STEP2】SET mfgRestrictReqParams
        Params.MfgRestrictReqParams mfgRestrictReqParams = new Params.MfgRestrictReqParams();
        Infos.EntityInhibitDetailAttributes entityInhibitDetailAttributes = this.getAllTypeEIDAParamas();
        List<Infos.EntityIdentifier> entities = new ArrayList<>();
        Infos.EntityIdentifier entity1 = new Infos.EntityIdentifier();
        Infos.EntityIdentifier entity2 = new Infos.EntityIdentifier();
        Infos.EntityIdentifier entity3 = new Infos.EntityIdentifier();
        entity1.setClassName("Operation");
        entity1.setObjectID(routeID);
        entity1.setAttribution(operationNumber);
        entity2.setClassName("Product Specification");
        entity2.setObjectID(productSpecificationID);
        entity3.setClassName("Equipment");
        entity3.setObjectID(aMachineID);
        entities.add(entity1);
        entities.add(entity2);
        entities.add(entity3);
        entityInhibitDetailAttributes.setEntities(entities);
        mfgRestrictReqParams.setUser(getUser());
        mfgRestrictReqParams.setEntityInhibitDetailAttributes(entityInhibitDetailAttributes);
        //【STEP3】entityInhibit the StartOperation , Product and Equipment
        Response mfgRestrictReq = constraintController.mfgRestrictReq(mfgRestrictReqParams);
        Infos.EntityInhibitDetailAttributes attributes = (Infos.EntityInhibitDetailAttributes)mfgRestrictReq.getBody();
        //【STEP4】Get Eqp and Port
        ObjectIdentifier aPortID = testCommonData.getPROTID();
        //【STEP5】Try Load
        Params.loadOrUnloadLotRptParams loadOrUnloadLotRptParams = new Params.loadOrUnloadLotRptParams();
        loadOrUnloadLotRptParams.setUser(getUser());
        loadOrUnloadLotRptParams.setEquipmentID(aMachineID);
        loadOrUnloadLotRptParams.setCassetteID(cassetteID);
        loadOrUnloadLotRptParams.setPortID(aPortID);
        loadOrUnloadLotRptParams.setLoadPurposeType("Process Lot");
        try {
            equipmentController.carrierLoadingRpt(loadOrUnloadLotRptParams);
            throw new ServiceException(retCodeConfig.getError());
        }catch (ServiceException e){
            if (e.getCode()!=2800){
                throw new ServiceException(retCodeConfig.getError());
            }
        }
        //【STEP6】Restriction Cancel and retry
        this.restriction_Cancel(attributes);
        equipmentController.carrierLoadingRpt(loadOrUnloadLotRptParams);
    }

    public void Inhibited_Recipe_EnvironmentVariableIs1_CheckRestrictionFlag(){
        //【STEP1】STB
        ObjectIdentifier lotID = ((Results.WaferLotStartReqResult) stbCase.STB_NotPreparedLot().getBody()).getLotID();
        List<ObjectIdentifier> lotIDList = new ArrayList<>();
        lotIDList.add(lotID);
        Results.LotInfoInqResult lotInfoInqResult = (Results.LotInfoInqResult) lotGeneralTestCase.getLotInfoCase(lotIDList).getBody();
        Infos.LotInfo lotInfo = lotInfoInqResult.getLotInfoList().get(0);
        ObjectIdentifier cassetteID = lotInfo.getLotLocationInfo().getCassetteID();
        ObjectIdentifier MachineReceipe = testCommonData.getMACHINERECEIPE();
        ObjectIdentifier aMachineID = testCommonData.getEQUIPMENTID();
        //【STEP2】SET mfgRestrictReqParams
        Params.MfgRestrictReqParams mfgRestrictReqParams = new Params.MfgRestrictReqParams();
        Infos.EntityInhibitDetailAttributes entityInhibitDetailAttributes = this.getAllTypeEIDAParamas();
        List<Infos.EntityIdentifier> entities = new ArrayList<>();
        Infos.EntityIdentifier entity1 = new Infos.EntityIdentifier();
        entity1.setClassName("Machine Recipe");
        entity1.setObjectID(MachineReceipe);
        entities.add(entity1);
        entityInhibitDetailAttributes.setEntities(entities);
        mfgRestrictReqParams.setUser(getUser());
        mfgRestrictReqParams.setEntityInhibitDetailAttributes(entityInhibitDetailAttributes);
        //【STEP3】entityInhibit the Recipe
        Response mfgRestrictReq = constraintController.mfgRestrictReq(mfgRestrictReqParams);
        Infos.EntityInhibitDetailAttributes attributes = (Infos.EntityInhibitDetailAttributes)mfgRestrictReq.getBody();
        //【STEP4】Get Eqp and Port
        ObjectIdentifier aPortID = testCommonData.getPROTID();
        //【STEP5】Try Load
        Params.loadOrUnloadLotRptParams loadOrUnloadLotRptParams = new Params.loadOrUnloadLotRptParams();
        loadOrUnloadLotRptParams.setUser(getUser());
        loadOrUnloadLotRptParams.setEquipmentID(aMachineID);
        loadOrUnloadLotRptParams.setCassetteID(cassetteID);
        loadOrUnloadLotRptParams.setPortID(aPortID);
        loadOrUnloadLotRptParams.setLoadPurposeType("Process Lot");
        try {
            equipmentController.carrierLoadingRpt(loadOrUnloadLotRptParams);
            throw new ServiceException(retCodeConfig.getError());
        }catch (ServiceException e){
            if (e.getCode()!=2800){
                throw new ServiceException(retCodeConfig.getError());
            }
        }
        //【STEP6】Restriction Cancel and retry
        this.restriction_Cancel(attributes);
        equipmentController.carrierLoadingRpt(loadOrUnloadLotRptParams);
    }

    public void Inhibited_Reticle_EnvironmentVariableIs1_CheckRestrictionFlag(){
        //【STEP1】STB a Product1.01
        ObjectIdentifier lotID = stbCase.STB_PRODUCT101(false);
        List<ObjectIdentifier> lotIDList = new ArrayList<>();
        lotIDList.add(lotID);
        Results.LotInfoInqResult lotInfoInqResult = (Results.LotInfoInqResult) lotGeneralTestCase.getLotInfoCase(lotIDList).getBody();
        Infos.LotInfo lotInfo = lotInfoInqResult.getLotInfoList().get(0);
        ObjectIdentifier cassetteID = lotInfo.getLotLocationInfo().getCassetteID();
        ObjectIdentifier machineID = ObjectIdentifier.buildWithValue("1TKD01_EXD01");
        ObjectIdentifier reticleID = ObjectIdentifier.buildWithValue("Reticle_A02");
        ObjectIdentifier reticlePodID = ObjectIdentifier.buildWithValue("RPOD25");
        ObjectIdentifier aPortID = testCommonData.getPROTID();
        //【STEP2】SKIP TO 7000.0100
        testUtils.skip(lotID,"7000.0100");
        //【STEP3】SET mfgRestrictReqParams
        Params.MfgRestrictReqParams mfgRestrictReqParams = new Params.MfgRestrictReqParams();
        Infos.EntityInhibitDetailAttributes entityInhibitDetailAttributes = this.getAllTypeEIDAParamas();
        List<Infos.EntityIdentifier> entityIdentifiers = new ArrayList<>();
        Infos.EntityIdentifier entityIdentifier = new Infos.EntityIdentifier();
        entityIdentifier.setClassName("Reticle");
        entityIdentifier.setObjectID(reticleID);
        entityIdentifiers.add(entityIdentifier);
        entityInhibitDetailAttributes.setEntities(entityIdentifiers);
        mfgRestrictReqParams.setUser(getUser());
        mfgRestrictReqParams.setEntityInhibitDetailAttributes(entityInhibitDetailAttributes);
        //【STEP4】entityInhibit the Reticle
        Response mfgRestrictReq = constraintController.mfgRestrictReq(mfgRestrictReqParams);
        Infos.EntityInhibitDetailAttributes attributes = (Infos.EntityInhibitDetailAttributes)mfgRestrictReq.getBody();
        //【STEP5】Just In
        Params.ReticleAllInOutRptParams reticleAllInOutRptParams1 = this.getReticleJustInRptParams();
        durableController.reticleAllInOutRpt(reticleAllInOutRptParams1);
        //【STEP6】EQP In
        Params.ReticlePodTransferStatusChangeRptParams reticlePodTransferStatusChangeRptParams = new Params.ReticlePodTransferStatusChangeRptParams();
        String claimMemo = null;
        reticlePodTransferStatusChangeRptParams.setUser(getUser());
        reticlePodTransferStatusChangeRptParams.setEquipmentID(machineID);
        reticlePodTransferStatusChangeRptParams.setStockerID(ObjectIdentifier.build(null,null));
        List<Infos.XferReticlePod> xferReticlePods = new ArrayList<>();
        Infos.XferReticlePod xferReticlePod = new Infos.XferReticlePod();
        xferReticlePod.setReticlePodID(reticlePodID);
        xferReticlePod.setTransferStatus("EI");
        xferReticlePods.add(xferReticlePod);
        reticlePodTransferStatusChangeRptParams.setXferReticlePodList(xferReticlePods);
        reticlePodTransferStatusChangeRptParams.setClaimMemo(claimMemo);
        durableController.reticlePodTransferStatusChangeRpt(reticlePodTransferStatusChangeRptParams);
        //【STEP7】Just Out
        Params.ReticleAllInOutRptParams reticleAllInOutRptParams2 = this.getReticleJustOutRptParams();
        durableController.reticleAllInOutRpt(reticleAllInOutRptParams2);
        //【STEP8】Try Load
        Params.loadOrUnloadLotRptParams loadOrUnloadLotRptParams = new Params.loadOrUnloadLotRptParams();
        loadOrUnloadLotRptParams.setUser(getUser());
        loadOrUnloadLotRptParams.setEquipmentID(machineID);
        loadOrUnloadLotRptParams.setCassetteID(cassetteID);
        loadOrUnloadLotRptParams.setPortID(aPortID);
        loadOrUnloadLotRptParams.setLoadPurposeType("Process Lot");
        try {
            equipmentController.carrierLoadingRpt(loadOrUnloadLotRptParams);
            throw new ServiceException(retCodeConfig.getError());
        }catch (ServiceException e){
            if (e.getCode()!=979){
                throw new ServiceException(retCodeConfig.getError());
            }
        }
        //【STEP9】Restriction Cancel and retry
        this.restriction_Cancel(attributes);
        equipmentController.carrierLoadingRpt(loadOrUnloadLotRptParams);
    }

    public void Inhibited_ReticleGroup_EnvironmentVariableIs1_CheckRestrictionFlag(){
        //【STEP1】STB a Product1.01
        ObjectIdentifier lotID = stbCase.STB_PRODUCT101(false);
        List<ObjectIdentifier> lotIDList = new ArrayList<>();
        lotIDList.add(lotID);
        Results.LotInfoInqResult lotInfoInqResult = (Results.LotInfoInqResult) lotGeneralTestCase.getLotInfoCase(lotIDList).getBody();
        Infos.LotInfo lotInfo = lotInfoInqResult.getLotInfoList().get(0);
        ObjectIdentifier cassetteID = lotInfo.getLotLocationInfo().getCassetteID();
        ObjectIdentifier machineID = ObjectIdentifier.buildWithValue("1TKD01_EXD01");
        ObjectIdentifier reticleGroupID = ObjectIdentifier.buildWithValue("RTCL_Grp01");
        ObjectIdentifier reticlePodID = ObjectIdentifier.buildWithValue("RPOD25");
        ObjectIdentifier aPortID = testCommonData.getPROTID();
        //【STEP2】SKIP TO 7000.0100
        testUtils.skip(lotID,"7000.0100");
        //【STEP3】SET mfgRestrictReqParams
        Params.MfgRestrictReqParams mfgRestrictReqParams = new Params.MfgRestrictReqParams();
        Infos.EntityInhibitDetailAttributes entityInhibitDetailAttributes = this.getAllTypeEIDAParamas();
        List<Infos.EntityIdentifier> entityIdentifiers = new ArrayList<>();
        Infos.EntityIdentifier entityIdentifier = new Infos.EntityIdentifier();
        entityIdentifier.setClassName("Reticle Group");
        entityIdentifier.setObjectID(reticleGroupID);
        entityIdentifiers.add(entityIdentifier);
        entityInhibitDetailAttributes.setEntities(entityIdentifiers);
        mfgRestrictReqParams.setUser(getUser());
        mfgRestrictReqParams.setEntityInhibitDetailAttributes(entityInhibitDetailAttributes);
        //【STEP4】entityInhibit the Reticle
        Response mfgRestrictReq = constraintController.mfgRestrictReq(mfgRestrictReqParams);
        Infos.EntityInhibitDetailAttributes attributes = (Infos.EntityInhibitDetailAttributes)mfgRestrictReq.getBody();
        //【STEP5】Just In
        Params.ReticleAllInOutRptParams reticleAllInOutRptParams1 = this.getReticleJustInRptParams();
        durableController.reticleAllInOutRpt(reticleAllInOutRptParams1);
        //【STEP6】EQP In
        Params.ReticlePodTransferStatusChangeRptParams reticlePodTransferStatusChangeRptParams = new Params.ReticlePodTransferStatusChangeRptParams();
        String claimMemo = null;
        reticlePodTransferStatusChangeRptParams.setUser(getUser());
        reticlePodTransferStatusChangeRptParams.setEquipmentID(machineID);
        reticlePodTransferStatusChangeRptParams.setStockerID(ObjectIdentifier.build(null,null));
        List<Infos.XferReticlePod> xferReticlePods = new ArrayList<>();
        Infos.XferReticlePod xferReticlePod = new Infos.XferReticlePod();
        xferReticlePod.setReticlePodID(reticlePodID);
        xferReticlePod.setTransferStatus("EI");
        xferReticlePods.add(xferReticlePod);
        reticlePodTransferStatusChangeRptParams.setXferReticlePodList(xferReticlePods);
        reticlePodTransferStatusChangeRptParams.setClaimMemo(claimMemo);
        durableController.reticlePodTransferStatusChangeRpt(reticlePodTransferStatusChangeRptParams);
        //【STEP7】Just Out
        Params.ReticleAllInOutRptParams reticleAllInOutRptParams2 = this.getReticleJustOutRptParams();
        durableController.reticleAllInOutRpt(reticleAllInOutRptParams2);
        //【STEP8】Try Load
        Params.loadOrUnloadLotRptParams loadOrUnloadLotRptParams = new Params.loadOrUnloadLotRptParams();
        loadOrUnloadLotRptParams.setUser(getUser());
        loadOrUnloadLotRptParams.setEquipmentID(machineID);
        loadOrUnloadLotRptParams.setCassetteID(cassetteID);
        loadOrUnloadLotRptParams.setPortID(aPortID);
        loadOrUnloadLotRptParams.setLoadPurposeType("Process Lot");
        try {
            equipmentController.carrierLoadingRpt(loadOrUnloadLotRptParams);
            throw new ServiceException(retCodeConfig.getError());
        }catch (ServiceException e){
            if (e.getCode()!=2800){
                throw new ServiceException(retCodeConfig.getError());
            }
        }
        //【STEP9】Restriction Cancel and retry
        this.restriction_Cancel(attributes);
        equipmentController.carrierLoadingRpt(loadOrUnloadLotRptParams);
    }

    public void Inhibited_ProcessDefinition_Equipment_EnvironmentVariableIs1_CheckRestrictionFlag(){
        //【STEP1】STB a PoC.Product01
        ObjectIdentifier lotID = ((Results.WaferLotStartReqResult) stbCase.STB_PoC_Product01().getBody()).getLotID();
        List<ObjectIdentifier> lotIDList = new ArrayList<>();
        lotIDList.add(lotID);
        Results.LotInfoInqResult lotInfoInqResult = (Results.LotInfoInqResult) lotGeneralTestCase.getLotInfoCase(lotIDList).getBody();
        Infos.LotInfo lotInfo = lotInfoInqResult.getLotInfoList().get(0);
        ObjectIdentifier cassetteID = lotInfo.getLotLocationInfo().getCassetteID();
        ObjectIdentifier machineID = ObjectIdentifier.buildWithValue("2DC02");
        ObjectIdentifier opeID =ObjectIdentifier.buildWithValue("2DC02.01");
        ObjectIdentifier aPortID = testCommonData.getPROTID();
        //【STEP2】Skip to 8000.0400
        testUtils.skip(lotID,"8000.0400");
        //【STEP3】SET mfgRestrictReqParams
        Params.MfgRestrictReqParams mfgRestrictReqParams = new Params.MfgRestrictReqParams();
        Infos.EntityInhibitDetailAttributes entityInhibitDetailAttributes = this.getAllTypeEIDAParamas();
        List<Infos.EntityIdentifier> entityIdentifiers = new ArrayList<>();
        Infos.EntityIdentifier entityIdentifier1 = new Infos.EntityIdentifier();
        Infos.EntityIdentifier entityIdentifier2 = new Infos.EntityIdentifier();
        entityIdentifier1.setClassName("Process Definition");
        entityIdentifier1.setObjectID(opeID);
        entityIdentifier2.setClassName("Equipment");
        entityIdentifier2.setObjectID(machineID);
        entityIdentifiers.add(entityIdentifier1);
        entityIdentifiers.add(entityIdentifier2);
        entityInhibitDetailAttributes.setEntities(entityIdentifiers);
        mfgRestrictReqParams.setUser(getUser());
        mfgRestrictReqParams.setEntityInhibitDetailAttributes(entityInhibitDetailAttributes);
        //【STEP4】entityInhibit the Process Definition and Equipment
        Response mfgRestrictReq = constraintController.mfgRestrictReq(mfgRestrictReqParams);
        Infos.EntityInhibitDetailAttributes attributes = (Infos.EntityInhibitDetailAttributes)mfgRestrictReq.getBody();
        //【STEP5】Try Load
        Params.loadOrUnloadLotRptParams loadOrUnloadLotRptParams = new Params.loadOrUnloadLotRptParams();
        loadOrUnloadLotRptParams.setUser(getUser());
        loadOrUnloadLotRptParams.setEquipmentID(machineID);
        loadOrUnloadLotRptParams.setCassetteID(cassetteID);
        loadOrUnloadLotRptParams.setPortID(aPortID);
        loadOrUnloadLotRptParams.setLoadPurposeType("Process Lot");
        try {
            equipmentController.carrierLoadingRpt(loadOrUnloadLotRptParams);
            throw new ServiceException(retCodeConfig.getError());
        }catch (ServiceException e){
            if (e.getCode()!=2800){
                throw new ServiceException(retCodeConfig.getError());
            }
        }
        //【STEP6】Restriction Cancel and retry
        this.restriction_Cancel(attributes);
        equipmentController.carrierLoadingRpt(loadOrUnloadLotRptParams);
    }

    public void Inhibited_Module_Process_Definition_Equipment_EnvironmentVariableIs1_CheckRestrictionFlag(){
        //【STEP1】STB a PoC.Product01
        ObjectIdentifier lotID = ((Results.WaferLotStartReqResult) stbCase.STB_PoC_Product01().getBody()).getLotID();
        List<ObjectIdentifier> lotIDList = new ArrayList<>();
        lotIDList.add(lotID);
        Results.LotInfoInqResult lotInfoInqResult = (Results.LotInfoInqResult) lotGeneralTestCase.getLotInfoCase(lotIDList).getBody();
        Infos.LotInfo lotInfo = lotInfoInqResult.getLotInfoList().get(0);
        ObjectIdentifier cassetteID = lotInfo.getLotLocationInfo().getCassetteID();
        ObjectIdentifier machineID = ObjectIdentifier.buildWithValue("2DC02");
        ObjectIdentifier MPDID =ObjectIdentifier.buildWithValue("2DC.01");
        ObjectIdentifier aPortID = testCommonData.getPROTID();
        //【STEP2】Skip to 8000.0400
        testUtils.skip(lotID,"8000.0400");
        //【STEP3】SET mfgRestrictReqParams
        Params.MfgRestrictReqParams mfgRestrictReqParams = new Params.MfgRestrictReqParams();
        Infos.EntityInhibitDetailAttributes entityInhibitDetailAttributes = this.getAllTypeEIDAParamas();
        List<Infos.EntityIdentifier> entityIdentifiers = new ArrayList<>();
        Infos.EntityIdentifier entityIdentifier = new Infos.EntityIdentifier();
        entityIdentifier.setClassName("Module Process Definition");
        entityIdentifier.setObjectID(MPDID);
        entityIdentifiers.add(entityIdentifier);
        entityInhibitDetailAttributes.setEntities(entityIdentifiers);
        mfgRestrictReqParams.setUser(getUser());
        mfgRestrictReqParams.setEntityInhibitDetailAttributes(entityInhibitDetailAttributes);
        //【STEP4】entityInhibit the Process Definition and Equipment
        Response mfgRestrictReq = constraintController.mfgRestrictReq(mfgRestrictReqParams);
        Infos.EntityInhibitDetailAttributes attributes = (Infos.EntityInhibitDetailAttributes)mfgRestrictReq.getBody();
        //【STEP5】Try Load
        Params.loadOrUnloadLotRptParams loadOrUnloadLotRptParams = new Params.loadOrUnloadLotRptParams();
        loadOrUnloadLotRptParams.setUser(getUser());
        loadOrUnloadLotRptParams.setEquipmentID(machineID);
        loadOrUnloadLotRptParams.setCassetteID(cassetteID);
        loadOrUnloadLotRptParams.setPortID(aPortID);
        loadOrUnloadLotRptParams.setLoadPurposeType("Process Lot");
        try {
            equipmentController.carrierLoadingRpt(loadOrUnloadLotRptParams);
            throw new ServiceException(retCodeConfig.getError());
        }catch (ServiceException e){
            if (e.getCode()!=2800){
                throw new ServiceException(retCodeConfig.getError());
            }
        }
        //【STEP6】Restriction Cancel and retry
        this.restriction_Cancel(attributes);
        equipmentController.carrierLoadingRpt(loadOrUnloadLotRptParams);
    }

    public void PRC_5_1_29_Inhibited_Product_EnvironmentVariableIs1_Which_LotIn_FlowBatch(){
        List<ObjectIdentifier> lotIDs = stbCase.stb_NLots_NotPreparedCase(2, true);
        List<ObjectIdentifier> lotIDList1 = new ArrayList<>();
        List<ObjectIdentifier> lotIDList2 = new ArrayList<>();
        ObjectIdentifier lotID1 = lotIDs.get(0);
        ObjectIdentifier lotID2 = lotIDs.get(1);
        lotIDList1.add(lotID1);
        lotIDList2.add(lotID2);
        Results.LotInfoInqResult lotInfoInqResult = (Results.LotInfoInqResult) lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();
        Infos.LotInfo lotInfo1 = lotInfoInqResult.getLotInfoList().get(0);
        Infos.LotInfo lotInfo2 = lotInfoInqResult.getLotInfoList().get(1);
        ObjectIdentifier cassetteID1 = lotInfo1.getLotLocationInfo().getCassetteID();
        ObjectIdentifier cassetteID2 = lotInfo2.getLotLocationInfo().getCassetteID();
        ObjectIdentifier stationID = ObjectIdentifier.buildWithValue("TSP0101");
        ObjectIdentifier machineID = ObjectIdentifier.buildWithValue("FB105");
        //【STEP2】Skip to 4000.0100
        testUtils.skip(lotID1,"4000.0100");
        testUtils.skip(lotID2,"4000.0100");
        //【STEP3】XFer state change to MI
        Params.CarrierTransferStatusChangeRptParams carrierTransferStatusChangeRptParams1 = new Params.CarrierTransferStatusChangeRptParams();
        Params.CarrierTransferStatusChangeRptParams carrierTransferStatusChangeRptParams2 = new Params.CarrierTransferStatusChangeRptParams();
        carrierTransferStatusChangeRptParams1.setUser(getUser());
        carrierTransferStatusChangeRptParams2.setUser(getUser());
        carrierTransferStatusChangeRptParams1.setCarrierID(cassetteID1);
        carrierTransferStatusChangeRptParams2.setCarrierID(cassetteID2);
        carrierTransferStatusChangeRptParams1.setXferStatus("MI");
        carrierTransferStatusChangeRptParams2.setXferStatus("MI");
        carrierTransferStatusChangeRptParams1.setManualInFlag(true);
        carrierTransferStatusChangeRptParams2.setManualInFlag(true);
        carrierTransferStatusChangeRptParams1.setPortID(ObjectIdentifier.build(null,null));
        carrierTransferStatusChangeRptParams2.setPortID(ObjectIdentifier.build(null,null));
        carrierTransferStatusChangeRptParams1.setMachineID(stationID);
        carrierTransferStatusChangeRptParams2.setMachineID(stationID);
        transferManagementSystemController.carrierTransferStatusChangeRpt(carrierTransferStatusChangeRptParams1);
        transferManagementSystemController.carrierTransferStatusChangeRpt(carrierTransferStatusChangeRptParams2);
        //【STEP4】create a batch
        Params.FlowBatchByManualActionReqParam flowBatchByManualActionReqParam = new Params.FlowBatchByManualActionReqParam();
        flowBatchByManualActionReqParam.setUser(getUser());
        flowBatchByManualActionReqParam.setEquipmentID(machineID);
        List<Infos.FlowBatchByManualActionReqCassette> flowBatchByManualActionReqCassettes = new ArrayList<>();
        Infos.FlowBatchByManualActionReqCassette flowBatchByManualActionReqCassette1 = new Infos.FlowBatchByManualActionReqCassette();
        Infos.FlowBatchByManualActionReqCassette flowBatchByManualActionReqCassette2 = new Infos.FlowBatchByManualActionReqCassette();
        flowBatchByManualActionReqCassette1.setCassetteID(cassetteID1);
        flowBatchByManualActionReqCassette1.setLotID(lotIDList1);
        flowBatchByManualActionReqCassette2.setCassetteID(cassetteID2);
        flowBatchByManualActionReqCassette2.setLotID(lotIDList2);
        flowBatchByManualActionReqCassettes.add(flowBatchByManualActionReqCassette1);
        flowBatchByManualActionReqCassettes.add(flowBatchByManualActionReqCassette2);
        flowBatchByManualActionReqParam.setStrFlowBatchByManualActionReqCassette(flowBatchByManualActionReqCassettes);
        flowBatchController.flowBatchByManualActionReq(flowBatchByManualActionReqParam);
        //【STEP5】check the flag
    }

    public void restriction_Cancel(Infos.EntityInhibitDetailAttributes attributes){
        Params.MfgRestrictCancelReqParams mfgRestrictCancelReqParams = new Params.MfgRestrictCancelReqParams();
        mfgRestrictCancelReqParams.setUser(getUser());
        mfgRestrictCancelReqParams.setReasonCode(ObjectIdentifier.buildWithValue("APBE"));
        List<Infos.EntityInhibitDetailInfo> entityInhibitions = new ArrayList<>();
        Infos.EntityInhibitDetailInfo entityInhibition = new Infos.EntityInhibitDetailInfo();
        //get Inhibity ID
        Params.MfgRestrictListInqParams mfgRestrictListInqParams = new Params.MfgRestrictListInqParams();
        mfgRestrictListInqParams.setUser(getUser());
         SearchCondition searchCondition = new SearchCondition();
        searchCondition.setSize(10);
        searchCondition.setPage(1);
        mfgRestrictListInqParams.setSearchCondition(searchCondition);
        mfgRestrictListInqParams.setEntityInhibitDetailAttributes(attributes);
        Response inhibitList = constraintInqController.mfgRestrictListInq(mfgRestrictListInqParams);
        Results.NewMfgRestrictListInqResult mfgRestrictListInq = (Results.NewMfgRestrictListInqResult) inhibitList.getBody();
        Infos.EntityInhibitDetailInfo entityInhibitDetailInfo = mfgRestrictListInq.getStrEntityInhibitions().getContent().get(0);
        entityInhibition.setEntityInhibitID(entityInhibitDetailInfo.getEntityInhibitID());
        entityInhibitions.add(entityInhibition);
        mfgRestrictCancelReqParams.setEntityInhibitions(entityInhibitions);
        //Restriction Cancel
        constraintController.mfgRestrictCancelReq(mfgRestrictCancelReqParams);
    }

    public Infos.EntityInhibitDetailAttributes getNormalEIDAParamas(){
        Infos.EntityInhibitDetailAttributes entityInhibitDetailAttributes = new Infos.EntityInhibitDetailAttributes();
        entityInhibitDetailAttributes.setReasonCode("SOOR");
        Date date = new Date();
        SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        String dateStr=format.format(date);
        entityInhibitDetailAttributes.setStartTimeStamp(dateStr);
        entityInhibitDetailAttributes.setEndTimeStamp("2099-11-29 00:00:00");
        List<String> subLotTypes = new ArrayList<>();
        subLotTypes.add("Normal");
        entityInhibitDetailAttributes.setSubLotTypes(subLotTypes);
        entityInhibitDetailAttributes.setReasonDesc("Reason: Inhibit by SPC Check");
        entityInhibitDetailAttributes.setOwnerID(ObjectIdentifier.build("ADMIN",null));
        return entityInhibitDetailAttributes;
    }

    private Infos.EntityInhibitDetailAttributes getAllTypeEIDAParamas(){
        Infos.EntityInhibitDetailAttributes entityInhibitDetailAttributes = new Infos.EntityInhibitDetailAttributes();
        entityInhibitDetailAttributes.setReasonCode("SOOR");
        Date date = new Date();
        SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        String dateStr=format.format(date);
        entityInhibitDetailAttributes.setStartTimeStamp(dateStr);
        entityInhibitDetailAttributes.setEndTimeStamp("2099-11-29 00:00:00");
        List<String> subLotTypes = new ArrayList<>();
        entityInhibitDetailAttributes.setSubLotTypes(subLotTypes);
        entityInhibitDetailAttributes.setReasonDesc("Reason: Inhibit by SPC Check");
        entityInhibitDetailAttributes.setOwnerID(ObjectIdentifier.build("ADMIN",null));
        return entityInhibitDetailAttributes;
    }

    public Infos.EntityInhibitDetailInfo inhibitSpecCondition(ObjectIdentifier equipmentID){
        return this.inhibitSpecCondtition(equipmentID, null);
    }

    public Infos.EntityInhibitDetailInfo inhibitSpecCondtition(ObjectIdentifier equipmentID, ObjectIdentifier productID){
        Params.ReasonCodeListByCategoryInqParams reasonCodeListByCategoryInqParams = new Params.ReasonCodeListByCategoryInqParams();
        reasonCodeListByCategoryInqParams.setUser(getUser());
        reasonCodeListByCategoryInqParams.setCodeCategory("MfgRestrict");
        List<Infos.ReasonCodeAttributes> reasonCodeAttributesList = (List<Infos.ReasonCodeAttributes>) systemInqController.reasonCodeListByCategoryInq(reasonCodeListByCategoryInqParams).getBody();
        Params.SubLotTypeIDListExInqParams subLotTypeIDListExInqParams = new Params.SubLotTypeIDListExInqParams();
        subLotTypeIDListExInqParams.setUser(getUser());
        List<Infos.LotTypeInfo> lotTypeInfoList = (List<Infos.LotTypeInfo>) electronicInformationInqController.subLotTypeIdListEx(subLotTypeIDListExInqParams).getBody();
        //SET mfgRestrictReqParams
        Params.MfgRestrictReqParams mfgRestrictReqParams = new Params.MfgRestrictReqParams();
        Infos.EntityInhibitDetailAttributes entityInhibitDetailAttributes = new Infos.EntityInhibitDetailAttributes();
        entityInhibitDetailAttributes.setReasonCode(reasonCodeAttributesList.get(0).getReasonCodeID().getValue());
        Date date = new Date();
        SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        String dateStr=format.format(date);
        entityInhibitDetailAttributes.setStartTimeStamp(dateStr);
        entityInhibitDetailAttributes.setEndTimeStamp("2099-11-29 00:00:00");
        entityInhibitDetailAttributes.setReasonDesc(reasonCodeAttributesList.get(0).getCodeDescription());
        entityInhibitDetailAttributes.setOwnerID(ObjectIdentifier.build("ADMIN",null));
        List<Infos.EntityIdentifier> entityIdentifiers = new ArrayList<>();
        if (equipmentID != null){
            Infos.EntityIdentifier entityIdentifier = new Infos.EntityIdentifier();
            entityIdentifier.setClassName("Equipment");
            entityIdentifier.setObjectID(equipmentID);
            entityIdentifiers.add(entityIdentifier);
        }
        if (productID != null){
            Infos.EntityIdentifier entityIdentifier = new Infos.EntityIdentifier();
            entityIdentifier.setClassName("Product Specification");
            entityIdentifier.setObjectID(productID);
            entityIdentifiers.add(entityIdentifier);
        }
        entityInhibitDetailAttributes.setEntities(entityIdentifiers);
        mfgRestrictReqParams.setUser(getUser());
        mfgRestrictReqParams.setEntityInhibitDetailAttributes(entityInhibitDetailAttributes);
        Response mfgRestrictReq = constraintController.mfgRestrictReq(mfgRestrictReqParams);
        return (Infos.EntityInhibitDetailInfo)mfgRestrictReq.getBody();
    }

    public Params.ReticleAllInOutRptParams getReticleJustInRptParams(){
        ObjectIdentifier reticleID = ObjectIdentifier.buildWithValue("Reticle_A02");
        ObjectIdentifier reticlePodID = ObjectIdentifier.buildWithValue("RPOD25");
        Params.ReticleAllInOutRptParams reticleAllInOutRptParams = new Params.ReticleAllInOutRptParams();
        reticleAllInOutRptParams.setUser(getUser());
        reticleAllInOutRptParams.setMoveDirection("Just-In");
        reticleAllInOutRptParams.setReticlePodID(reticlePodID);
        List<Infos.MoveReticles> moveReticles = new ArrayList<>();
        Infos.MoveReticles moveReticle = new Infos.MoveReticles();
        moveReticle.setReticleID(reticleID);
        moveReticle.setSlotNumber(1);
        moveReticles.add(moveReticle);
        reticleAllInOutRptParams.setMoveReticles(moveReticles);
        return reticleAllInOutRptParams;
    }

    public Params.ReticleAllInOutRptParams getReticleJustOutRptParams(){
        ObjectIdentifier reticleID = ObjectIdentifier.buildWithValue("Reticle_A02");
        ObjectIdentifier reticlePodID = ObjectIdentifier.buildWithValue("RPOD25");
        Params.ReticleAllInOutRptParams reticleAllInOutRptParams = new Params.ReticleAllInOutRptParams();
        reticleAllInOutRptParams.setUser(getUser());
        reticleAllInOutRptParams.setMoveDirection("Just-Out");
        reticleAllInOutRptParams.setReticlePodID(reticlePodID);
        List<Infos.MoveReticles> moveReticles = new ArrayList<>();
        Infos.MoveReticles moveReticle = new Infos.MoveReticles();
        moveReticle.setReticleID(reticleID);
        moveReticle.setSlotNumber(1);
        moveReticles.add(moveReticle);
        reticleAllInOutRptParams.setMoveReticles(moveReticles);
        return reticleAllInOutRptParams;
    }

    @Autowired
    private CimJpaRepository cimJpaRepository;

    @Autowired
    private BaseCoreFactory baseCoreFactory;

    public void MFG_1000_Regist(){
        String sql = "SELECT * FROM OMEQP";
        List<CimEquipmentDO> cimEquipmentDOList = cimJpaRepository.query(sql, CimEquipmentDO.class);
        for (CimEquipmentDO cimEquipmentDO : cimEquipmentDOList){
            ObjectIdentifier eqpID = ObjectIdentifier.build(cimEquipmentDO.getEquipmentID(),cimEquipmentDO.getId());
            CimMachine cimMachine = baseCoreFactory.getBO(CimMachine.class, eqpID);
            List<ProcessResource> processResources = cimMachine.allProcessResources();
            if (!CimArrayUtils.isEmpty(processResources)){
                for (ProcessResource chamber : processResources){
                    Params.MfgRestrictReqParams mfgRestrictReqParams = new Params.MfgRestrictReqParams();
                    mfgRestrictReqParams.setUser(getUser());
                    Infos.EntityInhibitDetailAttributes entityInhibitDetailAttributes = new Infos.EntityInhibitDetailAttributes();
                    mfgRestrictReqParams.setEntityInhibitDetailAttributes(entityInhibitDetailAttributes);
                    entityInhibitDetailAttributes.setReasonCode("SPCT");
                    entityInhibitDetailAttributes.setEndTimeStamp("2022-09-01 00:00:00");
                    entityInhibitDetailAttributes.setStartTimeStamp("2020-09-01 10:39:41");
                    entityInhibitDetailAttributes.setOwnerID(ObjectIdentifier.buildWithValue("decade"));
                    entityInhibitDetailAttributes.setReasonDesc("Reason: Inhibit by SPC Timer Watchdog");
                    entityInhibitDetailAttributes.setSubLotTypes(new ArrayList<>());
                    List<Infos.EntityIdentifier> entities = new ArrayList<>();
                    Infos.EntityIdentifier entity = new Infos.EntityIdentifier();
                    entities.add(entity);
                    entity.setAttribution(cimEquipmentDO.getEquipmentID());
                    entity.setClassName("Chamber");
                    entity.setObjectID(ObjectIdentifier.build(chamber.getIdentifier(),chamber.getPrimaryKey()));
                    entityInhibitDetailAttributes.setEntities(entities);
                    constraintController.mfgRestrictReq(mfgRestrictReqParams);
                }
            }else {
                Params.MfgRestrictReqParams mfgRestrictReqParams = new Params.MfgRestrictReqParams();
                mfgRestrictReqParams.setUser(getUser());
                Infos.EntityInhibitDetailAttributes entityInhibitDetailAttributes = new Infos.EntityInhibitDetailAttributes();
                mfgRestrictReqParams.setEntityInhibitDetailAttributes(entityInhibitDetailAttributes);
                entityInhibitDetailAttributes.setReasonCode("SPCT");
                entityInhibitDetailAttributes.setEndTimeStamp("2022-09-01 00:00:00");
                entityInhibitDetailAttributes.setStartTimeStamp("2020-09-01 10:39:41");
                entityInhibitDetailAttributes.setOwnerID(ObjectIdentifier.buildWithValue("decade"));
                entityInhibitDetailAttributes.setReasonDesc("Reason: Inhibit by SPC Timer Watchdog");
                entityInhibitDetailAttributes.setSubLotTypes(new ArrayList<>());
                List<Infos.EntityIdentifier> entities = new ArrayList<>();
                Infos.EntityIdentifier entity = new Infos.EntityIdentifier();
                entities.add(entity);
                entity.setClassName("Equipment");
                entity.setObjectID(ObjectIdentifier.build(eqpID.getValue(),eqpID.getReferenceKey()));
                entityInhibitDetailAttributes.setEntities(entities);
                constraintController.mfgRestrictReq(mfgRestrictReqParams);
            }
        }
    }

    public void MFG_1000_Cancle(){
        Params.MfgRestrictListInqParams mfgRestrictListInqParams = new Params.MfgRestrictListInqParams();
        mfgRestrictListInqParams.setUser(getUser());
        SearchCondition searchCondition = new SearchCondition();
        searchCondition.setSize(9999);
        mfgRestrictListInqParams.setSearchCondition(searchCondition);
        Infos.EntityInhibitDetailAttributes entityInhibitDetailAttributes = new Infos.EntityInhibitDetailAttributes();
        mfgRestrictListInqParams.setEntityInhibitDetailAttributes(entityInhibitDetailAttributes);
        entityInhibitDetailAttributes.setOwnerID(ObjectIdentifier.emptyIdentifier());
        List<Infos.EntityIdentifier> entities = new ArrayList<>();
        Infos.EntityIdentifier entity = new Infos.EntityIdentifier();
        entities.add(entity);
        entity.setClassName("Equipment");
        entityInhibitDetailAttributes.setEntities(entities);
        Response mfgRestrictList1 = constraintInqController.mfgRestrictListInq(mfgRestrictListInqParams);
        Results.NewMfgRestrictListInqResult result1 = (Results.NewMfgRestrictListInqResult) mfgRestrictList1.getBody();
        entity.setClassName("Chamber");
        Response mfgRestrictList2 = constraintInqController.mfgRestrictListInq(mfgRestrictListInqParams);
        Results.NewMfgRestrictListInqResult result2 = (Results.NewMfgRestrictListInqResult) mfgRestrictList1.getBody();
        List<Infos.EntityInhibitDetailInfo> content1 = result1.getStrEntityInhibitions().getContent();
        List<Infos.EntityInhibitDetailInfo> content2 = result2.getStrEntityInhibitions().getContent();
        Params.MfgRestrictCancelReqParams mfgRestrictCancelReqParams = new Params.MfgRestrictCancelReqParams();
        mfgRestrictCancelReqParams.setUser(getUser());
        List<Infos.EntityInhibitDetailInfo> entityInhibitions = new ArrayList<>();
        mfgRestrictCancelReqParams.setEntityInhibitions(entityInhibitions);
        mfgRestrictCancelReqParams.setReasonCode(ObjectIdentifier.buildWithValue("APBE"));
        for (Infos.EntityInhibitDetailInfo entityInhibitDetailInfo: content1){
            Infos.EntityInhibitDetailInfo entityInhibit = new Infos.EntityInhibitDetailInfo();
            entityInhibit.setEntityInhibitID(entityInhibitDetailInfo.getEntityInhibitID());
            entityInhibitions.add(entityInhibit);
        }
        for (Infos.EntityInhibitDetailInfo entityInhibitDetailInfo: content2){
            Infos.EntityInhibitDetailInfo entityInhibit = new Infos.EntityInhibitDetailInfo();
            entityInhibit.setEntityInhibitID(entityInhibitDetailInfo.getEntityInhibitID());
            entityInhibitions.add(entityInhibit);
        }
        constraintController.mfgRestrictCancelReq(mfgRestrictCancelReqParams);
    }
}
