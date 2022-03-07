package com.fa.cim.newIntegration.tcase;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.Response;
import com.fa.cim.common.utils.CimArrayUtils;
import com.fa.cim.common.utils.CimObjectUtils;
import com.fa.cim.controller.interfaces.dynamicOperationControl.IDynamicOperationController;
import com.fa.cim.controller.interfaces.dynamicOperationControl.IDynamicOperationInqController;
import com.fa.cim.core.TestCommonData;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Outputs;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/12/9          ********            Nyx                create file
 *
 * @author: Nyx
 * @date: 2019/12/9 16:44
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@Service
public class DOCTestCase {

    @Autowired
    @Qualifier("testCommonDataWithDB")
    private TestCommonData testCommonData;
    @Autowired
    private IDynamicOperationController dynamicOperationController;
    @Autowired
    private IDynamicOperationInqController dynamicOperationInqController;

    @Data
    public static class DOCRegistrationParams {
        ObjectIdentifier equipmentID;
        ObjectIdentifier machineRecipeID;
        boolean restrictEquipmentFlag;

        String recipeParameterChangeType;
        List<Infos.RecipeParameterInfo> recipeParameterInfos;

        private ObjectIdentifier dcDefineID;
        private ObjectIdentifier dcSpecID;
        private List<Infos.DCSpecDetailInfo> dcSpecList;         // sequence of DC Spec Item

        List<Infos.FoundReticle> foundReticles;
        boolean skipFlag;
        boolean sendEmailFlag;
        boolean holdLotFlag;
        List<Infos.CorrespondingOperationInfo> correspondingOperationInfos;

        public DOCRegistrationParams() {
        }

        public DOCRegistrationParams(ObjectIdentifier equipmentID, ObjectIdentifier machineRecipeID) {
            this.equipmentID = equipmentID;
            this.machineRecipeID = machineRecipeID;
        }
    }

    public Response fpcRegistrationCase(Infos.LotInfo lotInfo, ObjectIdentifier routeID, Infos.OperationInformation operationInformation, List<Infos.WaferListInLotFamilyInfo> waferListInLotFamilyInfos,
                                        DOCRegistrationParams docRegistrationParams) {

        ObjectIdentifier equipmentID = docRegistrationParams.getEquipmentID();
        ObjectIdentifier machineRecipeID = docRegistrationParams.getMachineRecipeID();
        boolean restrictEquipmentFlag = docRegistrationParams.isRestrictEquipmentFlag();
        String recipeParameterChangeType = docRegistrationParams.getRecipeParameterChangeType();
        List<Infos.RecipeParameterInfo> recipeParameterInfos = docRegistrationParams.getRecipeParameterInfos();

        ObjectIdentifier dcDefineID = docRegistrationParams.getDcDefineID();
        ObjectIdentifier dcSpecID = docRegistrationParams.getDcSpecID();
        List<Infos.DCSpecDetailInfo> dcSpecList = docRegistrationParams.getDcSpecList();

        List<Infos.FoundReticle> foundReticles = docRegistrationParams.getFoundReticles();
        boolean skipFlag = docRegistrationParams.isSkipFlag();
        boolean sendEmailFlag = docRegistrationParams.isSendEmailFlag();
        boolean holdLotFlag = docRegistrationParams.isHoldLotFlag();
        List<Infos.CorrespondingOperationInfo> correspondingOperationInfos = docRegistrationParams.getCorrespondingOperationInfos();

        Params.DOCLotInfoSetReqParams docLotInfoSetReqParams = new Params.DOCLotInfoSetReqParams();
        docLotInfoSetReqParams.setUser(testCommonData.getUSER());
        List<Infos.FPCInfoAction> fpcInfoActions = new ArrayList<>();
        Infos.FPCInfoAction fpcInfoAction = new Infos.FPCInfoAction();
        fpcInfoAction.setActionType("Create");
        Infos.FPCInfo fpcInfo = new Infos.FPCInfo();
        fpcInfo.setCorrespondingOperationInfoList(correspondingOperationInfos);
        fpcInfo.setDcDefineID(dcDefineID);
        fpcInfo.setDcSpecID(dcSpecID);
        fpcInfo.setDcSpecList(dcSpecList);
        fpcInfo.setEquipmentID(equipmentID);
        fpcInfo.setFpcGroupNumber(0);
        fpcInfo.setLotFamilyID(lotInfo.getLotBasicInfo().getFamilyLotID());

        List<Infos.LotWaferInfo> lotWaferInfos = new ArrayList<>();
        for (Infos.WaferListInLotFamilyInfo waferListInLotFamilyInfo : waferListInLotFamilyInfos) {
            Infos.LotWaferInfo lotWaferInfo = new Infos.LotWaferInfo();
            if (!CimObjectUtils.isEmpty(recipeParameterChangeType) && !CimObjectUtils.isEmpty(recipeParameterInfos)) {
                lotWaferInfo.setRecipeParameterInfoList(recipeParameterInfos);
            }
            lotWaferInfo.setWaferID(waferListInLotFamilyInfo.getWaferID());
            lotWaferInfos.add(lotWaferInfo);
        }
        if (CimArrayUtils.getSize(lotWaferInfos) == CimArrayUtils.getSize(lotInfo.getLotWaferAttributesList())) {
            fpcInfo.setFpcType("ByLot");
        } else {
            fpcInfo.setFpcType("ByWafer");
        }
        fpcInfo.setLotWaferInfoList(lotWaferInfos);
        fpcInfo.setMachineRecipeID(machineRecipeID);
        fpcInfo.setMainProcessDefinitionID(routeID);
        fpcInfo.setOperationNumber(operationInformation.getOperationNumber());
        fpcInfo.setProcessDefinitionID(operationInformation.getOperationID());
        fpcInfo.setProcessDefinitionType(operationInformation.getOperationPDType());
        fpcInfo.setRecipeParameterChangeType(recipeParameterChangeType);

        if (!CimObjectUtils.isEmpty(foundReticles)) {
            List<Infos.ReticleInfo> reticleInfos = new ArrayList<>();
            for (Infos.FoundReticle foundReticle : foundReticles) {
                Infos.ReticleInfo reticleInfo = new Infos.ReticleInfo();
                reticleInfos.add(reticleInfo);
                reticleInfo.setReticleID(foundReticle.getReticleID());
                reticleInfo.setReticleGroup(foundReticle.getReticleGroupID());
                reticleInfo.setSequenceNumber(Integer.valueOf(String.valueOf(foundReticle.getReticleGroupSequenceNumber())));
            }
            fpcInfo.setReticleInfoList(reticleInfos);
        }

        fpcInfo.setRestrictEquipmentFlag(restrictEquipmentFlag);
        fpcInfo.setHoldLotFlag(holdLotFlag);
        fpcInfo.setSendEmailFlag(sendEmailFlag);
        fpcInfo.setSkipFalg(skipFlag);
        fpcInfoAction.setStrFPCInfo(fpcInfo);
        fpcInfoActions.add(fpcInfoAction);
        docLotInfoSetReqParams.setStrFPCInfoActionList(fpcInfoActions);
        return dynamicOperationController.docLotInfoSetReq(docLotInfoSetReqParams);
    }

    public Response fpcUpdateCase(Infos.FPCInfo fpcInfo) {
        Params.DOCLotInfoSetReqParams docLotInfoSetReqParams = new Params.DOCLotInfoSetReqParams();
        docLotInfoSetReqParams.setUser(testCommonData.getUSER());
        List<Infos.FPCInfoAction> fpcInfoActions = new ArrayList<>();
        Infos.FPCInfoAction fpcInfoAction = new Infos.FPCInfoAction();
        fpcInfoAction.setActionType("Update");
        fpcInfoAction.setStrFPCInfo(fpcInfo);
        fpcInfoActions.add(fpcInfoAction);
        docLotInfoSetReqParams.setStrFPCInfoActionList(fpcInfoActions);
        return dynamicOperationController.docLotInfoSetReq(docLotInfoSetReqParams);
    }

    public Response docLotRemoveReqCase(List<String> fpcIDs, ObjectIdentifier lotFamilyID) {
        Params.DOCLotRemoveReqParams docLotRemoveReqParams = new Params.DOCLotRemoveReqParams();
        docLotRemoveReqParams.setUser(testCommonData.getUSER());
        docLotRemoveReqParams.setFpcIDs(fpcIDs);
        docLotRemoveReqParams.setLotFamilyID(lotFamilyID);
        return dynamicOperationController.docLotRemoveReq(docLotRemoveReqParams);
    }

    public static final String GET_RECIPE_BY_OPERATION = "Get recipe by operation";
    public static final String GET_RECIPE_BY_EQUIPMENT = "Get recipe by equipment";
    public static final String GET_RECIPE_BY_ALL = "ALL";

    public List<Outputs.MachineRecipe> recipeIdListForDOCInqCase(recipeIdListForDOCInqCaseParams params) {
        Params.RecipeIdListForDOCInqParams recipeIdListForDOCInqParams = new Params.RecipeIdListForDOCInqParams();
        recipeIdListForDOCInqParams.setUser(testCommonData.getUSER());
        Infos.RecipeIdListForDOCInqInParm recipeIdListForDOCInqParam = new Infos.RecipeIdListForDOCInqInParm();
        recipeIdListForDOCInqParams.setStrRecipeIdListForDOCInqInParm(recipeIdListForDOCInqParam);

        String recipeCondition = params.getRecipeCondition();
        ObjectIdentifier equipmentID = params.getEquipmentID();
        ObjectIdentifier lotID = params.getLotID();
        ObjectIdentifier pdID = params.getPdID();
        if (GET_RECIPE_BY_OPERATION.equals(recipeCondition)) {
            recipeIdListForDOCInqParam.setLotID(lotID);
            recipeIdListForDOCInqParam.setPdID(pdID);
            recipeIdListForDOCInqParam.setRecipeSearchCriteria("PD");
            recipeIdListForDOCInqParam.setWhiteDefSearchCriteria("All");

        } else if (GET_RECIPE_BY_EQUIPMENT.equals(recipeCondition)) {
            recipeIdListForDOCInqParam.setLotID(lotID);
            recipeIdListForDOCInqParam.setEquipmentID(equipmentID);
            recipeIdListForDOCInqParam.setPdID(pdID);
            recipeIdListForDOCInqParam.setRecipeSearchCriteria("PD");
            recipeIdListForDOCInqParam.setWhiteDefSearchCriteria("All");

        } else {
            recipeIdListForDOCInqParam.setMachineRecipeID(new ObjectIdentifier("%"));
            recipeIdListForDOCInqParam.setRecipeSearchCriteria("All");
            recipeIdListForDOCInqParam.setWhiteDefSearchCriteria("NonWhite");
        }

        return ((Results.RecipeIdListForDOCInqResult) dynamicOperationInqController.recipeIdListForDOCInq(recipeIdListForDOCInqParams).getBody()).getStrMachineRecipeList();
    }

    public Results.DOCLotInfoInqResult docLotInfoInqCase(ObjectIdentifier lotID, ObjectIdentifier routeID, String operationNubmer) {
        Params.DOCLotInfoInqParams docLotInfoInqParams = new Params.DOCLotInfoInqParams();
        docLotInfoInqParams.setUser(testCommonData.getUSER());
        docLotInfoInqParams.setLotID(lotID);
        docLotInfoInqParams.setMainOperNo(operationNubmer);
        docLotInfoInqParams.setMainPDID(routeID);
        docLotInfoInqParams.setDcSpecItemInfoGetFlag(true);
        docLotInfoInqParams.setReticleInfoGetFlag(true);
        docLotInfoInqParams.setWaferIDInfoGetFlag(true);
        return (Results.DOCLotInfoInqResult) dynamicOperationInqController.DOCLotInfoInq(docLotInfoInqParams).getBody();
    }

    @Data
    public static class recipeIdListForDOCInqCaseParams {
        private String recipeCondition;
        private ObjectIdentifier lotID;
        private ObjectIdentifier equipmentID;
        private ObjectIdentifier pdID;

        public recipeIdListForDOCInqCaseParams(String recipeCondition) {
            this.recipeCondition = recipeCondition;
        }
    }
}
