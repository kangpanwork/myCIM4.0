package com.fa.cim.service.edc.impl;

import com.fa.cim.annotaion.OmService;
import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.utils.CimArrayUtils;
import com.fa.cim.common.utils.CimStringUtils;
import com.fa.cim.common.utils.Validations;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.dto.*;
import com.fa.cim.method.*;
import com.fa.cim.service.edc.IEngineerDataCollectionInqService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * description:
 * <p>EngineerDataCollectionInqService .<br/></p>
 * <p>
 * change history:
 * date      defect#       person     comments
 * ------------------------------------------------------------
 * ---------------------------------------------------------
 * 2020/9/8/008   ********     Decade     create file
 *
 * @author: Decade
 * @date: 2020/9/8/008 16:30
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@OmService
@Slf4j
public class EngineerDataCollectionInqServiceImpl implements IEngineerDataCollectionInqService {
    @Autowired
    private RetCodeConfig retCodeConfig;

    @Autowired
    private IControlJobMethod controlJobMethod;

    @Autowired
    private IProcessMethod processMethod;

    @Autowired
    private IDataCollectionMethod dataCollectionMethod;

    @Autowired
    private IEquipmentMethod equipmentMethod;

    @Autowired
    private ILotMethod lotMethod;


    @Override
    public Results.EDCSpecCheckActionResultInqResult sxEDCSpecCheckActionResultInq(Infos.ObjCommon objCommon, Params.EDCSpecCheckActionResultInqInParms edcSpecCheckActionResultInqInParms) {
        if(ObjectIdentifier.isEmpty(edcSpecCheckActionResultInqInParms.getControlJobID())){
            Validations.check(true,retCodeConfig.getInvalidInputParam());
        }

        Inputs.ObjControlJobProcessOperationListGetDRIn objControlJobProcessOperationListGetDRIn=new Inputs.ObjControlJobProcessOperationListGetDRIn();
        objControlJobProcessOperationListGetDRIn.setControlJobID(edcSpecCheckActionResultInqInParms.getControlJobID());
        objControlJobProcessOperationListGetDRIn.setLotID(edcSpecCheckActionResultInqInParms.getLotID());

        // step1 - controlJob_ProcessOperationList_GetDR

        List<Infos.DCActionResult> strDCActionResult=new ArrayList<>();

        List<Infos.ProcessOperationLot> processOperationLotList = controlJobMethod.controlJobProcessOperationListGetDR(objCommon, objControlJobProcessOperationListGetDRIn);

        for(Infos.ProcessOperationLot processOperationLot:processOperationLotList){
            String poObj = processOperationLot.getPoObj();
            ObjectIdentifier lotID=processOperationLot.getLotID();

            // step2 - processOperation_dcActionInfo_GetDR
            Results.EDCSpecCheckActionResultInqResult edcSpecCheckActionResultInqResultRetCode = processMethod.processOperationDCActionInfoGetDR(objCommon, poObj, lotID);

            strDCActionResult.addAll(edcSpecCheckActionResultInqResultRetCode.getStrDCActionResult());

        }

        Results.EDCSpecCheckActionResultInqResult edcSpecCheckActionResultInqResult=new Results.EDCSpecCheckActionResultInqResult();
        edcSpecCheckActionResultInqResult.setStrDCActionResult(strDCActionResult);

        return edcSpecCheckActionResultInqResult;
    }

    @Override
    public Results.EDCDataItemListByKeyInqResult sxEDCDataItemListByKeyInq(Infos.ObjCommon objCommon, String searchKeyPattern, List<Infos.HashedInfo> searchKeys) {
        Results.EDCDataItemListByKeyInqResult data = new Results.EDCDataItemListByKeyInqResult();
        //Step1 - In-Parameter Trace
        //Step2 - Call dataCollectionItem_FillInTxDCQ011DR
        Outputs.ObjDataCollectionItemFillInEDCDataItemListByKeyInqOut fillInOut = dataCollectionMethod.dataCollectionItemFillInTxDCQ011DR(objCommon, searchKeyPattern, searchKeys);

        Outputs.ObjDataCollectionItemFillInEDCDataItemListByKeyInqOut outData = fillInOut;
        data.setDcDefDataItems(outData.getDcDefDataItems());
        data.setDcSpecDataItems(outData.getDcSpecDataItems());
        data.setOperationDataCollectionSetting(outData.getOperationDataCollectionSetting());
        //Step3 - Return
        return data;
    }

    @Override
    public List<Infos.DataCollection> sxEDCConfigListInq(Infos.ObjCommon objCommon, Infos.EDCConfigListInqInParm parm) {
        List<Infos.DataCollection>  listRetCode=null;
        if (CimStringUtils.equals(parm.getObjectType(), BizConstant.SP_DCDEFINITION)) {
            log.info("Specified objectType is SP_DCDefinition.");
            if (CimStringUtils.equals(parm.getDcSearchCriteria(), BizConstant.SP_DC_SEARCHCRITERIA_ALL)) {
                log.info("dcSearchCriteria is ALL. Get DC Def/SpecID list from objectID.");
                // Step1- Call dcDefListGetDR(...)
                listRetCode = dataCollectionMethod.dcDefListGetDR(objCommon, parm.getObjectID(), parm.getDcType(), parm.getWhiteDefSearchCriteria(), parm.getMaxCount(), parm.getFPCCategory());
            } else if (CimStringUtils.equals(parm.getDcSearchCriteria(), BizConstant.SP_DC_SEARCHCRITERIA_PD)) {
                log.info("Specified objectType is SP_DCDefinition.");
                listRetCode = dataCollectionMethod.dcDefListGetFromPD(objCommon, parm.getLotID(), parm.getEquipmentID(), parm.getMachineRecipeID(), parm.getPdID(), parm.getWhiteDefSearchCriteria(), parm.getFPCCategory());
            } else {
                log.info("DC serach criteria is invalid.");
                Validations.check(true,retCodeConfig.getInvalidInputParam());
            }
        } else if (CimStringUtils.equals(parm.getObjectType(), BizConstant.SP_DCSPECIFICATION)) {
            // Step3- Call dcSpecListGetDR(...)
            listRetCode = dataCollectionMethod.dcSpecListGetDR(objCommon, parm.getDcDefID(), parm.getObjectID(), parm.getWhiteDefSearchCriteria(), parm.getMaxCount(), parm.getFPCCategory());
        } else {
            log.info("Specified objectType is invalid.");
            Validations.check(true,retCodeConfig.getInvalidInputParam());
        }
        return listRetCode;
    }

    @Override
    public Results.EDCDataItemWithTransitDataInqResult sxEDCDataItemWithTransitDataInq(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, ObjectIdentifier controlJobID) {
        boolean processJobControlFlag = false;

        boolean pjLevelCtrlFlagCheck = true;
        boolean equipmentCategoryCheck = false;
        boolean onlineModeCheck = false;
        boolean multipleRecipeCheck = false;
        //	Step1 - equipment_processJobLevelControlCheck
        try {
            equipmentMethod.equipmentProcessJobLevelControlCheck(objCommon, equipmentID,
                    pjLevelCtrlFlagCheck, equipmentCategoryCheck, onlineModeCheck, multipleRecipeCheck);
            processJobControlFlag = true;
        } catch (ServiceException ex){
            if (!Validations.isEquals(ex.getCode(),retCodeConfig.getProcessJobCtrlNotAvailable())){
                throw ex;
            }
            processJobControlFlag = false;
        }

        boolean processJobItemFlag = true;
        if (processJobControlFlag && CimStringUtils.equals(objCommon.getUser().getUserID().getValue(), BizConstant.SP_TCS_PERSON)) {
            processJobItemFlag = false;
        }
        //	Step2 - dataCollectionDefinition_FillInTxDCQ002DR__120
        Results.EDCDataItemWithTransitDataInqResult  fillInOut=null;
        try {
            fillInOut = dataCollectionMethod.dataCollectionDefinitionFillInTxDCQ002DR(objCommon, equipmentID, processJobItemFlag, controlJobID);
        } catch (ServiceException ex){
            if (!Validations.isEquals(ex.getCode(),retCodeConfig.getNoNeedToDataCollect())){
                throw ex;
            }
        }
        return fillInOut;
    }

    @Override
    public Results.SpecCheckResultInqResult sxSpecCheckResultInq(Infos.ObjCommon objCommon, Params.SpecCheckResultInqInParms specCheckResultInqInParms) {
        Results.SpecCheckResultInqResult edcSpecCheckActionResultInqResult=new Results.SpecCheckResultInqResult();

        if(ObjectIdentifier.isEmpty(specCheckResultInqInParms.getControlJobID())){
            Validations.check(true,retCodeConfig.getInvalidInputParam());
        }

        Inputs.ObjControlJobProcessOperationListGetDRIn objControlJobProcessOperationListGetDRIn=new Inputs.ObjControlJobProcessOperationListGetDRIn();
        objControlJobProcessOperationListGetDRIn.setControlJobID(specCheckResultInqInParms.getControlJobID());
        objControlJobProcessOperationListGetDRIn.setLotID(specCheckResultInqInParms.getLotID());

        // step1 - controlJob_ProcessOperationList_GetDR

        List<Infos.DataSpecCheckResult> strDataSpecCheckResult=new ArrayList<>();
        List<Infos.ProcessOperationLot> processOperationLotList = controlJobMethod.controlJobProcessOperationListGetDR(objCommon, objControlJobProcessOperationListGetDRIn);

        List<Infos.DCDef> specCheckResultInqResultRetCode=null;

        int poCnt=0;
        for(Infos.ProcessOperationLot processOperationLot : processOperationLotList){
            String poObj = processOperationLot.getPoObj();
            ObjectIdentifier lotID = processOperationLot.getLotID();

            // step2 - lot_cassette_Get
            ObjectIdentifier objGetLotCassetteOut = null;
            Infos.DataSpecCheckResult dataSpecCheckResult=new Infos.DataSpecCheckResult();
            dataSpecCheckResult.setLotID(processOperationLot.getLotID());
            strDataSpecCheckResult.add(dataSpecCheckResult);
            try {
                objGetLotCassetteOut = lotMethod.lotCassetteGet(objCommon, lotID);
            } catch (ServiceException e) {
                if(Validations.isEquals(retCodeConfig.getNotFoundCst(),e.getCode())){
                    dataSpecCheckResult.setCassetteID(ObjectIdentifier.buildWithValue(""));
                }else{
                    throw e;
                }
            }

            dataSpecCheckResult.setCassetteID(objGetLotCassetteOut);
            // step3 - processOperation_dataCollectionInfo_GetDR
            try{
                specCheckResultInqResultRetCode = processMethod.processOperationDataCollectionInfoGetDR(objCommon, true, poObj);
            }catch (ServiceException ex){
                if(retCodeConfig.getNotFoundCdata().getCode()==ex.getCode()){
                    dataSpecCheckResult.setStrDCDefResult(new ArrayList<>());
                    continue;
                }else if(retCodeConfig.getCdataDeleted().getCode()==ex.getCode()){
                    dataSpecCheckResult.setStrDCDefResult(new ArrayList<>());
                    continue;
                }else{
                    throw ex;
                }
            }
            dataSpecCheckResult.setStrDCDefResult(specCheckResultInqResultRetCode);
            poCnt++;
        }

        if (poCnt == 0) {
            return edcSpecCheckActionResultInqResult;
        }

        edcSpecCheckActionResultInqResult.setStrDataSpecCheckResult(strDataSpecCheckResult);

        return edcSpecCheckActionResultInqResult;
    }

    @Override
    public Results.EDCPlanInfoInqResult sxEDCPlanInfoInq(Infos.ObjCommon objCommon, Params.EDCPlanInfoInqParms edcPlanInfoInqParms) {
        if( CimStringUtils.isEmpty(edcPlanInfoInqParms.getDcDefID().getValue()) )
        {
            Validations.check(true,retCodeConfig.getInvalidInputParam());
        }

        // step1 - dcDef_detailInfo_GetDR
        Results.EDCPlanInfoInqResult  strDcDef_detailInfo_GetDR_out = dataCollectionMethod.dcDefDetailInfoGetDR(objCommon, edcPlanInfoInqParms.getDcDefID());


        return strDcDef_detailInfo_GetDR_out ;
    }

    @Override
    public Results.EDCSpecInfoInqResult sxEDCSpecInfoInq(Infos.ObjCommon objCommon, Params.EDCSpecInfoInqParms edcSpecInfoInqParms) {
        if ( CimStringUtils.isEmpty( edcSpecInfoInqParms.getDcSpecID().getValue() ) )
        {
            Validations.check(true,retCodeConfig.getInvalidInputParam());
        }

        // step1 - dcSpec_detailInfo_GetDR__101
        return dataCollectionMethod.dcSpecDetailInfoGetDR(objCommon, edcSpecInfoInqParms.getDcSpecID());

    }

    @Override
    public Results.EDCDataShowForUpdateInqResult sxEDCDataShowForUpdateInq(Infos.ObjCommon objCommon, ObjectIdentifier lotID) {
        //------------------------------------------------------
        // Get Lot Previous PO information
        //------------------------------------------------------
        Outputs.ObjLotPreviousOperationInfoGetOut lotPreviousOperationInfoGet = lotMethod.lotPreviousOperationInfoGet(objCommon, lotID);

        Outputs.ObjLotPreviousOperationInfoGetOut objLotPreviousOperationInfoGetOut = lotPreviousOperationInfoGet;

        //------------------------------------------------------
        // Set output result
        //------------------------------------------------------
        Results.EDCDataShowForUpdateInqResult result = new Results.EDCDataShowForUpdateInqResult();
        result.setLotID(lotID);
        result.setRouteID(objLotPreviousOperationInfoGetOut.getRouteID());
        result.setOperationNumber(objLotPreviousOperationInfoGetOut.getOperationNumber());
        result.setControlJobID(objLotPreviousOperationInfoGetOut.getControlJobID());
        result.setOperationPass(objLotPreviousOperationInfoGetOut.getOperationPass());

        //-----------------------------------------------------------------------//
        //   Get Started Lot information which is specified with ControlJob ID   //
        //-----------------------------------------------------------------------//
        Outputs.ObjLotPreviousOperationDataCollectionInformationGetOut dataCollectionInformationGetOut = lotMethod.lotPreviousOperationDataCollectionInformationGet(objCommon, null, Arrays.asList(lotID));
        List<Infos.StartCassette> strStartCassette = dataCollectionInformationGetOut.getStrStartCassette();
        if (CimArrayUtils.getSize(strStartCassette) == 1 && CimArrayUtils.getSize(strStartCassette.get(0).getLotInCassetteList()) == 1) {
            result.setStrDCDef(strStartCassette.get(0).getLotInCassetteList().get(0).getStartRecipe().getDcDefList());
        }
        return result;
    }

    @Override
    public Results.EDCDataItemListByCJInqResult sxEDCDataItemListByCJInq(Infos.ObjCommon objCommon, Params.EDCDataItemListByCJInqParams params) {
        Results.EDCDataItemListByCJInqResult data = new Results.EDCDataItemListByCJInqResult();

        //【Step1】Check Control Job Existence;
        Outputs.ObjControlJobStatusGetOut controlJobStatusRetCode = controlJobMethod.controlJobStatusGet(objCommon, params.getControlJobID());

        //【Step2】processJobID specified case - Check Process Job Existence in CJ;
        if (!CimStringUtils.isEmpty(params.getProcessJobID())) {
            List<Infos.ProcessJob> processWafersRetCode = processMethod.processOperationProcessWafersGet(objCommon, params.getControlJobID());

            int pjLen = CimArrayUtils.getSize(processWafersRetCode);
            Boolean bFoundPJ = false;
            for (int i = 0; i < pjLen; i++) {
                String processJobID = processWafersRetCode.get(i).getProcessJobID();
                if (CimStringUtils.equals(params.getProcessJobID(), processJobID)) {
                    bFoundPJ = true;
                    break;
                }
            }

            if (false == bFoundPJ) {
                Validations.check(true,retCodeConfig.getNotFoundProcessJob());
            }
        }

        Inputs.ObjProcessOperationRawDCItemsGetDR inputParams = new Inputs.ObjProcessOperationRawDCItemsGetDR();
        inputParams.setControlJobID(params.getControlJobID());
        inputParams.setEquipmentID(params.getEquipmentID());
        inputParams.setProcessJobID(params.getProcessJobID());
        inputParams.setExpandFlag(params.getExpandFlag());

        List<Infos.CollectedDataItem> rawDCItemsRetCode = processMethod.processOperationRawDCItemsGetDR(objCommon, inputParams);
        //【Step3】Set out structure;
        data.setControlJobID(params.getControlJobID());
        data.setEquipmentID(params.getEquipmentID());
        data.setCollectedDataItemList(rawDCItemsRetCode);

        return data;
    }
}
