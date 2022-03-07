package com.fa.cim.service.doc.impl;

import com.fa.cim.annotaion.OmService;
import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.utils.CimArrayUtils;
import com.fa.cim.common.utils.CimObjectUtils;
import com.fa.cim.common.utils.CimStringUtils;
import com.fa.cim.common.utils.Validations;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.dto.*;
import com.fa.cim.method.*;
import com.fa.cim.service.doc.IDynamicOperationInqService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

/**
 * description:
 * <p>
 * change history:
 * date             defect             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/9/8        ********             Jack Luo            create file
 *
 * @author: Jack Luo
 * @date: 2020/9/8 20:19
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@OmService
public class DynamicOperationInqServiceImpl implements IDynamicOperationInqService {

    @Autowired
    private ILotMethod lotMethod;

    @Autowired
    private IFPCMethod fpcMethod;

    @Autowired
    private RetCodeConfig retCodeConfig;

    @Autowired
    private IProcessMethod processMethod;

    @Autowired
    private IMachineRecipeMethod machineRecipeMethod;

    @Autowired
    private ILogicalRecipeMethod logicalRecipeMethod;

    @Autowired
    private IDataCollectionMethod dataCollectionMethod;

    @Override
    public Results.DOCLotInfoInqResult sxDOCLotInfoInq(Infos.ObjCommon objCommon, Params.DOCLotInfoInqParams params) {
        log.info("DOCLotInfoInqParams = {}", params);
        Results.DOCLotInfoInqResult resultObj = new Results.DOCLotInfoInqResult();

        ObjectIdentifier lotID = params.getLotID();
        //------------------------------------------------------------------------
        // Get DOC Available Flag
        //------------------------------------------------------------------------
        log.info("Get DOC Available Flag");
        Boolean FPCAvailFlagOut = lotMethod.lotFPCAvailFlagGet(objCommon, lotID);
        resultObj.setFPCAvailableFlag(FPCAvailFlagOut);

        /*--------------------------------*/
        /*   Get DOC detail information   */
        /*--------------------------------*/
        log.info("Get DOC detail information");
        Inputs.ObjFPCInfoGetDRIn in = new Inputs.ObjFPCInfoGetDRIn();
        in.setFPCIDs(params.getFPCIDs());
        in.setLotID(params.getLotID());
        in.setLotFamilyID(null);
        in.setMainPDID(params.getMainPDID());
        in.setMainOperNo(params.getMainOperNo());
        in.setOrgMainPDID(params.getOrgMainPDID());
        in.setOrgOperNo(params.getOrgOperNo());
        in.setSubMainPDID(params.getSubMainPDID());
        in.setSubOperNo(params.getSubOperNo());
        in.setEquipmentID(null);
        in.setWaferIDInfoGetFlag(params.isWaferIDInfoGetFlag());
        in.setRecipeParmInfoGetFlag(params.isRecipeParmInfoGetFlag());
        in.setReticleInfoGetFlag(params.isReticleInfoGetFlag());
        in.setDcSpecItemInfoGetFlag(params.isDcSpecItemInfoGetFlag());
        List<Infos.FPCInfo> fpcInfoOut = fpcMethod.fpcInfoGetDR(objCommon, in);

        resultObj.setFPCInfoList(fpcInfoOut);
        return resultObj;
    }

    @Override
    public Results.DOCStepListInProcessFlowInqResult sxDOCStepListInProcessFlowInq(Infos.ObjCommon objCommon, ObjectIdentifier lotID) {
        log.info("DOCStepListInProcessFlowInq()::lotID {}", lotID);
        Results.DOCStepListInProcessFlowInqResult resultObj = new Results.DOCStepListInProcessFlowInqResult();

        //------------------------------------------------------------------------
        // Get MainPD(mainRouteID) by lotID
        //------------------------------------------------------------------------
        log.info("Get MainPD(mainRouteID) by lotID");
        ObjectIdentifier mainRouteOut = lotMethod.lotMainRouteIDGet(objCommon, lotID);

        ObjectIdentifier mainRouteID = mainRouteOut;
        log.info("The target route {}", mainRouteID);
        String processExistenceCheck = processMethod.processExistenceCheck(objCommon, mainRouteID, BizConstant.SP_PD_FLOWLEVEL_MAIN);
        if (processExistenceCheck.equals(BizConstant.SP_MAINPDTYPE_BACKUP)
                || processExistenceCheck.equals(BizConstant.SP_MAINPDTYPE_BRANCH)
                || processExistenceCheck.equals(BizConstant.SP_MAINPDTYPE_REWORK)) {
            log.error("The target route must not be Backup/Branch/Rework route.");
            Validations.check(true, retCodeConfig.getInvalidPDType(), processExistenceCheck, mainRouteID);
        }

        //------------------------------------------------------------------------
        // Get DOC Available Flag
        //------------------------------------------------------------------------
        log.info("Get DOC Available Flag");
        Boolean pfcAvailFlagOut = lotMethod.lotFPCAvailFlagGet(objCommon, lotID);

        resultObj.setFPCAvailableFlag(pfcAvailFlagOut);

        Infos.LotInfoInqFlag lotInfoInqFlag = new Infos.LotInfoInqFlag();
        lotInfoInqFlag.setLotBasicInfoFlag(Boolean.TRUE);
        Infos.LotInfo lotDetailInfoOut = lotMethod.lotDetailInfoGetDR(objCommon, lotInfoInqFlag, lotID);

        ObjectIdentifier lotFamilyID = lotDetailInfoOut.getLotBasicInfo().getFamilyLotID();

        //------------------------------------------------------------------------
        // Get Main Process List In Route
        //------------------------------------------------------------------------
        log.info("Get Main Process List In Route");
        //===== Get Main Route's information =======//
        Inputs.ObjProcessOperationListInRouteForFpcGetDRIn in = new Inputs.ObjProcessOperationListInRouteForFpcGetDRIn();
        in.setRouteID(mainRouteID);
        in.setRouteRequirePattern(BizConstant.SP_MAINPDTYPE_REWORK);
        in.setLotFamilyID(lotFamilyID);
        in.setFPCCountGetFlag(Boolean.TRUE);
        List<Infos.ConnectedSubRouteOperationInfo> routeForFPCOut = processMethod.processOperationListInRouteForFPCGetDR(objCommon, in);
        List<Infos.ConnectedSubRouteOperationInfo> mainOpeList = routeForFPCOut;

        //===== Get Current Operation's information =======//
        Outputs.ObjLotCurrentOperationInfoGetOut lotCurrentOperationInfoOut = lotMethod.lotCurrentOperationInfoGet(objCommon, lotID);

        Outputs.ObjLotCurrentOperationInfoGetOut currentOperationObj = lotCurrentOperationInfoOut;

        //===== Get Lot's branch information =======//
        Infos.ProcessOriginalOperationGetOut processOriginalOperationOut = processMethod.processOriginalOperationGet(objCommon, lotID);
        Infos.ProcessOriginalOperationGetOut originalOperationObj = processOriginalOperationOut;

        Infos.LotRouteInfoForFPC lotRouteInfoForFPC = new Infos.LotRouteInfoForFPC();
        lotRouteInfoForFPC.setLotID(lotID);
        if (null != currentOperationObj) {
            lotRouteInfoForFPC.setMainPDID(lotCurrentOperationInfoOut.getRouteID());
            lotRouteInfoForFPC.setOperationNumber(lotCurrentOperationInfoOut.getOperationNumber());
        }
        if (null != originalOperationObj) {
            lotRouteInfoForFPC.setOriginalMainPDID(new ObjectIdentifier(originalOperationObj.getOriginalMainPDID()));
            lotRouteInfoForFPC.setOriginalOperationNumber(originalOperationObj.getOriginalOpeNo());
            lotRouteInfoForFPC.setSubMainPDID(new ObjectIdentifier(originalOperationObj.getSubOrigMainPDID()));
            lotRouteInfoForFPC.setSubOperationNumber(originalOperationObj.getSubOrigOpeNo());
        }
        resultObj.setLotRouteInfoForFPC(lotRouteInfoForFPC);

        //----- Return Data -------//
        List<Infos.MainRouteOperationInfo> mainsObj = new ArrayList<>();
        for (Infos.ConnectedSubRouteOperationInfo mainOpe : mainOpeList) {
            Infos.MainRouteOperationInfo mainObj = new Infos.MainRouteOperationInfo();
            mainsObj.add(mainObj);
            //----- Return Data : Main Route's Operations -------//
            mainObj.setMainPDID(mainOpe.getOperationID());
            mainObj.setOperationNumber(mainOpe.getOperationNumber());
            mainObj.setOperationPDType(mainOpe.getOperationPDType());
            mainObj.setMandatoryFlag(mainOpe.getMandatoryFlag());
            mainObj.setWhiteDefFlag(mainOpe.getWhiteDefFlag());
            mainObj.setFPCCategory(mainOpe.getFPCCategory());
            mainObj.setFPCInfoCount(mainOpe.getFPCInfoCount());

            //===== Get Connected Routes' information =======//
            List<Infos.ConnectedSub2RouteInfo> connectedSub2RouteInfos = mainOpe.getOperationInfoList();
            List<Infos.ConnectedSubRouteInfo> connectedSubRouteInfosObj = new ArrayList<>();
            mainObj.setConnectedSubRouteInfos(connectedSubRouteInfosObj);
            if (!CimObjectUtils.isEmpty(connectedSub2RouteInfos)) {
                for (Infos.ConnectedSub2RouteInfo connectedSub2RouteInfo : connectedSub2RouteInfos) {
                    Infos.ConnectedSubRouteInfo connectedSubRouteInfoObj = new Infos.ConnectedSubRouteInfo();
                    connectedSubRouteInfosObj.add(connectedSubRouteInfoObj);
                    //----- Return Data : Connected Routes -------//
                    connectedSubRouteInfoObj.setRouteID(connectedSub2RouteInfo.getRouteID());
                    connectedSubRouteInfoObj.setReturnOperationNumber(connectedSub2RouteInfo.getReturnOperationNumber());
                    connectedSubRouteInfoObj.setRoutePDType(connectedSub2RouteInfo.getRoutePDType());

                    // Operations, which relate to mainsObj route's connected route, and its branch routes are gotten.
                    Inputs.ObjProcessOperationListInRouteForFpcGetDRIn in1 = new Inputs.ObjProcessOperationListInRouteForFpcGetDRIn();
                    in1.setRouteID(connectedSub2RouteInfo.getRouteID());
                    in1.setOrgRouteID(mainRouteID);
                    in1.setOperationNumber(mainObj.getOperationNumber());
                    in1.setRouteRequirePattern(BizConstant.SP_MAINPDTYPE_BRANCH);
                    in1.setLotFamilyID(lotFamilyID);
                    in1.setFPCCountGetFlag(Boolean.TRUE);
                    List<Infos.ConnectedSubRouteOperationInfo> routeForFPCOut1 = processMethod.processOperationListInRouteForFPCGetDR(objCommon, in1);
                    List<Infos.ConnectedSubRouteOperationInfo> opeList = routeForFPCOut1;
                    List<Infos.ConnectedSubRouteOperationInfo> connectedSubRouteOperationInfosObj = new ArrayList<>();
                    connectedSubRouteInfoObj.setConnectedSubRouteOperationInfos(connectedSubRouteOperationInfosObj);
                    for (Infos.ConnectedSubRouteOperationInfo ope : opeList) {
                        Infos.ConnectedSubRouteOperationInfo connectedSubRouteOperationInfoObj = new Infos.ConnectedSubRouteOperationInfo();
                        connectedSubRouteOperationInfosObj.add(connectedSubRouteOperationInfoObj);
                        //----- Return Data : Connected Route's Operations -------//
                        connectedSubRouteOperationInfoObj.setOperationID(ope.getOperationID());
                        connectedSubRouteOperationInfoObj.setOperationNumber(ope.getOperationNumber());
                        connectedSubRouteOperationInfoObj.setOperationPDType(ope.getOperationPDType());
                        connectedSubRouteOperationInfoObj.setMandatoryFlag(ope.getMandatoryFlag());
                        connectedSubRouteOperationInfoObj.setWhiteDefFlag(ope.getWhiteDefFlag());
                        connectedSubRouteOperationInfoObj.setFPCCategory(ope.getFPCCategory());
                        connectedSubRouteOperationInfoObj.setFPCInfoCount(ope.getFPCInfoCount());

                        //===== Get Branch Routes' information =======//
                        List<Infos.ConnectedSub2RouteInfo> connectedSub2RouteInfos1 = ope.getOperationInfoList();
                        List<Infos.ConnectedSub2RouteInfo> connectedSub2RouteInfosObj = new ArrayList<>();
                        connectedSubRouteOperationInfoObj.setOperationInfoList(connectedSub2RouteInfosObj);
                        for (Infos.ConnectedSub2RouteInfo connectedSub2RouteInfo1 : connectedSub2RouteInfos1) {
                            Infos.ConnectedSub2RouteInfo connectedSubRouteInfo1Obj = new Infos.ConnectedSub2RouteInfo();
                            connectedSub2RouteInfosObj.add(connectedSubRouteInfo1Obj);
                            //----- Return Data : Connected Routes -------//
                            connectedSubRouteInfo1Obj.setRouteID(connectedSub2RouteInfo1.getRouteID());
                            connectedSubRouteInfo1Obj.setReturnOperationNumber(connectedSub2RouteInfo1.getReturnOperationNumber());
                            connectedSubRouteInfo1Obj.setRoutePDType(connectedSub2RouteInfo1.getRoutePDType());

                            // Operations, which relate to mainsObj route's connected route, and its branch routes are gotten.
                            Inputs.ObjProcessOperationListInRouteForFpcGetDRIn in2 = new Inputs.ObjProcessOperationListInRouteForFpcGetDRIn();
                            in2.setRouteID(connectedSub2RouteInfo1.getRouteID());
                            in2.setOrgRouteID(connectedSubRouteInfo1Obj.getRouteID());
                            in2.setOperationNumber(ope.getOperationNumber());
                            in2.setOrgRouteID(mainRouteID);
                            in2.setOrgOperationNumber(mainObj.getOperationNumber());
                            in2.setLotFamilyID(lotFamilyID);
                            in2.setFPCCountGetFlag(Boolean.TRUE);
                            List<Infos.ConnectedSubRouteOperationInfo> routeForFPCOut2 = processMethod.processOperationListInRouteForFPCGetDR(objCommon, in2);

                            List<Infos.ConnectedSubRouteOperationInfo> branchOpeList = routeForFPCOut2;
                            List<Infos.ConnectedSub2RouteOperationInfo> connectedSub2RouteOperationInfosObj = new ArrayList<>();
                            connectedSubRouteInfo1Obj.setConnectedSub2RouteOperationInfos(connectedSub2RouteOperationInfosObj);
                            for (Infos.ConnectedSubRouteOperationInfo branchOpe : branchOpeList) {
                                Infos.ConnectedSub2RouteOperationInfo connectedSubRouteOperationInfo1Obj = new Infos.ConnectedSub2RouteOperationInfo();
                                connectedSub2RouteOperationInfosObj.add(connectedSubRouteOperationInfo1Obj);
                                //----- Return Data : Branch Route's Operations -------//
                                connectedSubRouteOperationInfo1Obj.setOperationID(branchOpe.getOperationID());
                                connectedSubRouteOperationInfo1Obj.setOperationNumber(branchOpe.getOperationNumber());
                                connectedSubRouteOperationInfo1Obj.setOperationPDType(branchOpe.getOperationPDType());
                                connectedSubRouteOperationInfo1Obj.setMandatoryFlag(branchOpe.getMandatoryFlag());
                                connectedSubRouteOperationInfo1Obj.setWhiteDefFlag(branchOpe.getWhiteDefFlag());
                                connectedSubRouteOperationInfo1Obj.setFPCCategory(branchOpe.getFPCCategory());
                                connectedSubRouteOperationInfo1Obj.setFPCInfoCount(branchOpe.getFPCInfoCount());
                            }
                        }
                    }
                }
            }
        }
        Infos.MainRouteInfo mainRouteInfo = new Infos.MainRouteInfo();
        mainRouteInfo.setMainRouteID(mainRouteID);
        mainRouteInfo.setMainRouteOperationInfos(mainsObj);
        resultObj.setMainRouteInfo(mainRouteInfo);
        return resultObj;
    }

    @Override
    public Infos.FPCInfo sxCopyFromInq(Infos.ObjCommon objCommon, Infos.FPCInfo fpcInfo, ObjectIdentifier productID, ObjectIdentifier lotID) {
        String whiteDefSearchCriteria = "All";

        //equipment
        ObjectIdentifier equipmentID = null;
        List<ObjectIdentifier> eqps = processMethod.processDispatchEquipmentsGetDR(objCommon, productID, fpcInfo.getProcessDefinitionID());
        if (!CimObjectUtils.isEmpty(eqps)) {
            equipmentID = eqps.get(0);
            fpcInfo.setEquipmentID(eqps.get(0));
        }

        //recipe
        ObjectIdentifier machineRecipeID = null;
        List<Outputs.MachineRecipe> machineRecipes = machineRecipeMethod.machineRecipeGetListByPDForFPC(objCommon, lotID, new ObjectIdentifier(), fpcInfo.getProcessDefinitionID(), fpcInfo.getFpcCategory(), whiteDefSearchCriteria);
        if (!CimObjectUtils.isEmpty(machineRecipes)) {
            machineRecipeID = machineRecipes.get(0).getMachineRecipeID();
            fpcInfo.setMachineRecipeID(machineRecipeID);
        }

        if (!ObjectIdentifier.isEmpty(equipmentID) && !ObjectIdentifier.isEmpty(machineRecipeID)) {
            //recipe parameter
            Params.EqpRecipeParameterListInq eqpRecipeParameterListInqParams = new Params.EqpRecipeParameterListInq();
            eqpRecipeParameterListInqParams.setEquipmentID(equipmentID);
            eqpRecipeParameterListInqParams.setLotID(lotID);
            eqpRecipeParameterListInqParams.setMachineRecipeID(machineRecipeID);
            eqpRecipeParameterListInqParams.setPdID(fpcInfo.getProcessDefinitionID());
            eqpRecipeParameterListInqParams.setRParmSearchCriteria("LogicalRecipe");
            List<Infos.RecipeParameterInfo> recipeParameterInfo = logicalRecipeMethod.logicalRecipeRecipeParameterInfoGetByPD(objCommon, eqpRecipeParameterListInqParams);
            if (!CimObjectUtils.isEmpty(recipeParameterInfo)) {
                List<Infos.LotWaferInfo> lotWaferInfoList = fpcInfo.getLotWaferInfoList();
                if (CimArrayUtils.isNotEmpty(lotWaferInfoList)) {
                    lotWaferInfoList.forEach(x -> x.setRecipeParameterInfoList(recipeParameterInfo));
                }
            }

            //dc
            List<Infos.DataCollection> dataCollections = dataCollectionMethod.dcDefListGetFromPD(objCommon, lotID, equipmentID, machineRecipeID, fpcInfo.getProcessDefinitionID(), whiteDefSearchCriteria, fpcInfo.getFpcCategory());
            //dc detail
            if (!CimObjectUtils.isEmpty(dataCollections)) {
                ObjectIdentifier dcDefineID = dataCollections.get(0).getObjectID();
                fpcInfo.setDcDefineID(dcDefineID);
//                Results.EDCPlanInfoInqResult edcPlanInfoInqResult = dataCollectionMethod.dcDefDetailInfoGetDR(objCommon, dcDefineID); //dc define list

                //dc spec
                List<Infos.DataCollection> dataCollectionSpecs = dataCollectionMethod.dcSpecListGetDR(objCommon, dcDefineID, new ObjectIdentifier("%"), whiteDefSearchCriteria, 9999L, fpcInfo.getFpcCategory());
                if (!CimObjectUtils.isEmpty(dataCollectionSpecs)) {
                    fpcInfo.setDcSpecID(dataCollectionSpecs.get(0).getObjectID());
                    //dc spec detail
                    Results.EDCSpecInfoInqResult edcSpecInfoInqResult = dataCollectionMethod.dcSpecDetailInfoGetDR(objCommon, dataCollectionSpecs.get(0).getObjectID());
                    fpcInfo.setDcSpecList(edcSpecInfoInqResult.getStrDCSpecList());
                }
            }
        }
        return fpcInfo;
    }

    @Override
    public Infos.RouteInfo sxProcessFlowOpeListWithNestInq(Infos.ObjCommon objCommon, Params.ProcessFlowOpeListWithNestInqParam param){
        ObjectIdentifier routeID = param.getRouteID();
        //Check In-Parameter
        Validations.check(ObjectIdentifier.isEmptyWithValue(routeID), retCodeConfig.getLotIsNotAtRoute());

        String routeRequirePattern = null;
        //Process Existence Check
        //step1 - process_existence_Check
        log.info("step1 - process_existence_Check");
        String processDefinitionType = processMethod.processExistenceCheck(objCommon, routeID, BizConstant.SP_PD_FLOWLEVEL_MAIN);
        if(!CimStringUtils.equals(processDefinitionType, BizConstant.SP_MAINPDTYPE_BACKUP) &&
                !CimStringUtils.equals(processDefinitionType, BizConstant.SP_MAINPDTYPE_BRANCH) &&
                !CimStringUtils.equals(processDefinitionType, BizConstant.SP_MAINPDTYPE_REWORK)){
            routeRequirePattern = BizConstant.SP_MAINPDTYPE_REWORK;
        } else {
            routeRequirePattern = BizConstant.SP_MAINPDTYPE_BRANCH;
        }
        // Get Process List In Route
        Inputs.ObjProcessOperationNestListGetDRIn objProcessOperationNestListGetDRIn = new Inputs.ObjProcessOperationNestListGetDRIn();
        objProcessOperationNestListGetDRIn.setRouteID(param.getRouteID());
        objProcessOperationNestListGetDRIn.setFromOperationNumber(param.getFromOperationNumber());
        objProcessOperationNestListGetDRIn.setNestLevel(param.getNestLevel());
        objProcessOperationNestListGetDRIn.setRouteRequirePattern(routeRequirePattern);

        //step2 - process_operationNestList_GetDR
        log.info("step2 - process_operationNestList_GetDR");
        return processMethod.processOperationNestListGetDR(objCommon, objProcessOperationNestListGetDRIn);
    }

}
