package com.fa.cim.service.einfo.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.fa.cim.annotaion.OmService;
import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.User;
import com.fa.cim.common.utils.CimStringUtils;
import com.fa.cim.common.utils.Validations;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Outputs;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;
import com.fa.cim.lmg.LotMonitorGroupParams;
import com.fa.cim.lmg.LotMonitorGroupResults;
import com.fa.cim.method.*;
import com.fa.cim.service.einfo.IElectronicInformationInqService;
import com.fa.cim.service.equipment.IEquipmentInqService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Objects;

/**
 * description:
 * <p>ElectronicInformationInqService .<br/></p>
 * <p>
 * change history:
 * date      defect#       person     comments
 * ------------------------------------------------------------
 * ---------------------------------------------------------
 * 2020/9/8/008   ********     Decade     create file
 *
 * @author: Decade
 * @date: 2020/9/8/008 16:24
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@OmService
@Slf4j
public class ElectronicInformationInqServiceImpl implements IElectronicInformationInqService {
    @Autowired
    private ILotFamilyMethod lotFamilyMethod;

    @Autowired
    private IFactoryNoteMethod factoryNoteMethod;

    @Autowired
    private IProcessMethod processMethod;

    @Autowired
    private IOperationMethod operationMethod;

    @Autowired
    private RetCodeConfig retCodeConfig;

    @Autowired
    private IEquipmentMethod equipmentMethod;

    @Autowired
    private IDurableMethod durableMethod;

    @Autowired
    private ILotMethod lotMethod;

    @Autowired
    private IRecipeMethod recipeMethod;

    @Autowired
    private IEquipmentInqService eqpInfoInqService;

    @Autowired
    private IReticleMethod reticleMethod;

    @Autowired
    private IEquipmentAreaMethod equipmentAreaMethod;

    @Override
    public Results.EboardInfoInqResult sxEboardInfoInq(Infos.ObjCommon objCommon, User reqestUserID) {
        return factoryNoteMethod.factoryNoteFillInTxPLQ001DR(objCommon);
    }

    @Override
    public Results.OpeGuideInqResult sxEqpOperationManualInq(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID) {
        /*-----------------------------------------------------------*/
        /*   Check PosMachine, get PosMachineOperationProcedure      */
        /*-----------------------------------------------------------*/
        Results.OpeGuideInqResult strEquipmentOperationProcedureFillInTxPLQ007DROut = null;
        try {
            strEquipmentOperationProcedureFillInTxPLQ007DROut = equipmentMethod.equipmentOperationProcedureFillInTxPLQ007DR(objCommon, equipmentID);
        } catch (ServiceException e) {
            if(Validations.isEquals(retCodeConfig.getNotFoundEntry(), e.getCode())){
                Validations.check(true, retCodeConfig.getNotFoundEntry());
            } else if(Validations.isEquals(retCodeConfig.getSomeOpGuideDataError(), e.getCode())){
                Validations.check(true, retCodeConfig.getSomeOpGuideDataError());

            } else {
                throw e;
            }
        }
        return strEquipmentOperationProcedureFillInTxPLQ007DROut;
    }

    @Override
    public Results.HistoryInformationInqResult sxHistoryInformationInq(Infos.ObjCommon strObjCommonIn, Params.HistoryInformationInqParams strHistoryInformationInqInParm) {
        Results.HistoryInformationInqResult strHistoryInformationInqResult=new Results.HistoryInformationInqResult();
        if( CimStringUtils.equals( strHistoryInformationInqInParm.getHistoryCategory(), BizConstant.SP_HISTORYCATEGORY_DURABLE ) ) {
            Infos.DurableHistoryGetDRIn strDurableHistory_GetDR_in=new Infos.DurableHistoryGetDRIn();
            strDurableHistory_GetDR_in.setFromTimeStamp         (strHistoryInformationInqInParm.getFromTimeStamp());
            strDurableHistory_GetDR_in.setToTimeStamp           (strHistoryInformationInqInParm.getToTimeStamp());
            strDurableHistory_GetDR_in.setStrTargetTableInfoSeq (strHistoryInformationInqInParm.getStrTargetTableInfoSeq());
            strDurableHistory_GetDR_in.setMaxRecordCount        (strHistoryInformationInqInParm.getMaxRecordCount());
            strDurableHistory_GetDR_in.setSearchCondition(strHistoryInformationInqInParm.getSearchCondition());
            Infos.DurableHistoryGetDROut  strDurableHistory_GetDR_out;
            // step1 - durableHistory_GetDR
            strDurableHistory_GetDR_out=durableMethod.durableHistoryGetDR(strObjCommonIn,
                    strDurableHistory_GetDR_in);

            strHistoryInformationInqResult.setStrTableRecordInfoSeq   (strDurableHistory_GetDR_out.getStrTableRecordInfoSeq());
            strHistoryInformationInqResult.setStrTableRecordValueSeq  (strDurableHistory_GetDR_out.getStrTableRecordValueSeq());
            strHistoryInformationInqResult.setStrTableRecordValuePage  (strDurableHistory_GetDR_out.getStrTableRecordValuePage());
        } else if( CimStringUtils.equals( strHistoryInformationInqInParm.getHistoryCategory(), BizConstant.SP_HISTORYCATEGORY_RETICLE ) ) {
            Infos.ReticleHistoryGetDRIn reticleHistoryGetDRIn=new Infos.ReticleHistoryGetDRIn();
            reticleHistoryGetDRIn.setFromTimeStamp         (strHistoryInformationInqInParm.getFromTimeStamp());
            reticleHistoryGetDRIn.setToTimeStamp           (strHistoryInformationInqInParm.getToTimeStamp());
            reticleHistoryGetDRIn.setStrTargetTableInfoSeq (strHistoryInformationInqInParm.getStrTargetTableInfoSeq());
            reticleHistoryGetDRIn.setMaxRecordCount        (strHistoryInformationInqInParm.getMaxRecordCount());
            reticleHistoryGetDRIn.setSearchCondition       (strHistoryInformationInqInParm.getSearchCondition());
            Infos.ReticleHistoryGetDROut reticleHistoryGetDROut;

            // step2 - reticleHistory_GetDR
            reticleHistoryGetDROut = reticleMethod.reticleHistoryGetDR(strObjCommonIn,
                    reticleHistoryGetDRIn);
            strHistoryInformationInqResult.setStrTableRecordInfoSeq   (reticleHistoryGetDROut.getStrTableRecordInfoSeq());
            strHistoryInformationInqResult.setStrTableRecordValueSeq  (reticleHistoryGetDROut.getStrTableRecordValueSeq());
            strHistoryInformationInqResult.setStrTableRecordValuePage (reticleHistoryGetDROut.getStrTableRecordValuePage());
        }else if( CimStringUtils.equals( strHistoryInformationInqInParm.getHistoryCategory(), BizConstant.SP_HISTORYCATEGORY_EQUIPMENT ) ) {
            Infos.EquipmentHistoryGetDRIn strEquipmentHistory_GetDR_in=new Infos.EquipmentHistoryGetDRIn();
            strEquipmentHistory_GetDR_in.setFromTimeStamp         (strHistoryInformationInqInParm.getFromTimeStamp());
            strEquipmentHistory_GetDR_in.setToTimeStamp           (strHistoryInformationInqInParm.getToTimeStamp());
            strEquipmentHistory_GetDR_in.setStrTargetTableInfoSeq (strHistoryInformationInqInParm.getStrTargetTableInfoSeq());
            strEquipmentHistory_GetDR_in.setMaxRecordCount        (strHistoryInformationInqInParm.getMaxRecordCount());
            strEquipmentHistory_GetDR_in.setSearchCondition       (strHistoryInformationInqInParm.getSearchCondition());
            Infos.EquipmentHistoryGetDROut strEquipmentHistory_GetDR_out;

            // step2 - equipmentHistory_GetDR
            strEquipmentHistory_GetDR_out=equipmentMethod.equipmentHistoryGetDR(strObjCommonIn,
                    strEquipmentHistory_GetDR_in);
            strHistoryInformationInqResult.setStrTableRecordInfoSeq   (strEquipmentHistory_GetDR_out.getStrTableRecordInfoSeq());
            strHistoryInformationInqResult.setStrTableRecordValueSeq  (strEquipmentHistory_GetDR_out.getStrTableRecordValueSeq());
            strHistoryInformationInqResult.setStrTableRecordValuePage (strEquipmentHistory_GetDR_out.getStrTableRecordValuePage());
        } else if( CimStringUtils.equals( strHistoryInformationInqInParm.getHistoryCategory(), BizConstant.SP_HISTORYCATEGORY_PROCESSWAFERINCJ )
                || CimStringUtils.equals( strHistoryInformationInqInParm.getHistoryCategory(), BizConstant.SP_HISTORYCATEGORY_PROCESSJOBINCJ ) ) {
            Infos.LotControlJobHistoryGetDRIn strLotControlJobHistory_GetDR_in=new Infos.LotControlJobHistoryGetDRIn();
            strLotControlJobHistory_GetDR_in.setFromTimeStamp         (strHistoryInformationInqInParm.getFromTimeStamp());
            strLotControlJobHistory_GetDR_in.setToTimeStamp           (strHistoryInformationInqInParm.getToTimeStamp());
            strLotControlJobHistory_GetDR_in.setHistoryCategory       (strHistoryInformationInqInParm.getHistoryCategory());
            strLotControlJobHistory_GetDR_in.setStrTargetTableInfoSeq (strHistoryInformationInqInParm.getStrTargetTableInfoSeq());
            strLotControlJobHistory_GetDR_in.setMaxRecordCount        (strHistoryInformationInqInParm.getMaxRecordCount());
            strLotControlJobHistory_GetDR_in.setSearchCondition       (strHistoryInformationInqInParm.getSearchCondition());

            // step3 - lotControlJobHistory_GetDR
            Infos.LotControlJobHistoryGetDROut strLotControlJobHistory_GetDR_out= lotMethod.lotControlJobHistoryGetDR(strObjCommonIn, strLotControlJobHistory_GetDR_in);
            strHistoryInformationInqResult.setStrTableRecordInfoSeq   (strLotControlJobHistory_GetDR_out.getStrTableRecordInfoSeq());
            strHistoryInformationInqResult.setStrTableRecordValueSeq  (strLotControlJobHistory_GetDR_out.getStrTableRecordValueSeq());
            strHistoryInformationInqResult.setStrTableRecordValuePage  (strLotControlJobHistory_GetDR_out.getStrTableRecordValuePage());
        } else if( CimStringUtils.equals( strHistoryInformationInqInParm.getHistoryCategory(), BizConstant.SP_HISTORYCATEGORY_PROCESS )) {
            Infos.LotProcessHistoryGetDROut strLotProcessHistory_GetDR_out;
            Infos.LotProcessHistoryGetDRIn strLotProcessHistory_GetDR_in=new Infos.LotProcessHistoryGetDRIn();
            strLotProcessHistory_GetDR_in.setFromTimeStamp         (strHistoryInformationInqInParm.getFromTimeStamp());
            strLotProcessHistory_GetDR_in.setToTimeStamp           (strHistoryInformationInqInParm.getToTimeStamp());
            strLotProcessHistory_GetDR_in.setHistoryCategory       (strHistoryInformationInqInParm.getHistoryCategory());
            strLotProcessHistory_GetDR_in.setStrTargetTableInfoSeq (strHistoryInformationInqInParm.getStrTargetTableInfoSeq());
            strLotProcessHistory_GetDR_in.setMaxRecordCount        (strHistoryInformationInqInParm.getMaxRecordCount());
            strLotProcessHistory_GetDR_in.setSearchCondition       (strHistoryInformationInqInParm.getSearchCondition());

            // step4 - lotProcessHistory_GetDR
            strLotProcessHistory_GetDR_out = lotMethod.lotProcessHistoryGetDR ( strObjCommonIn, strLotProcessHistory_GetDR_in);
            strHistoryInformationInqResult.setStrTableRecordInfoSeq   (strLotProcessHistory_GetDR_out.getStrTableRecordInfoSeq());
            strHistoryInformationInqResult.setStrTableRecordValueSeq  (strLotProcessHistory_GetDR_out.getStrTableRecordValueSeq());
            strHistoryInformationInqResult.setStrTableRecordValuePage (strLotProcessHistory_GetDR_out.getStrTableRecordValuePage());
        } else if( CimStringUtils.equals( strHistoryInformationInqInParm.getHistoryCategory(), BizConstant.SP_HISTORYCATEGORY_RECIPEPARAMETERADJUST ) ) {
            Infos.RecipeParameterAdjustHistoryGetDRIn strRecipeParameterAdjustHistory_GetDR_in=new Infos.RecipeParameterAdjustHistoryGetDRIn();
            strRecipeParameterAdjustHistory_GetDR_in.setFromTimeStamp         (strHistoryInformationInqInParm.getFromTimeStamp());
            strRecipeParameterAdjustHistory_GetDR_in.setToTimeStamp           (strHistoryInformationInqInParm.getToTimeStamp());
            strRecipeParameterAdjustHistory_GetDR_in.setHistoryCategory       (strHistoryInformationInqInParm.getHistoryCategory());
            strRecipeParameterAdjustHistory_GetDR_in.setStrTargetTableInfoSeq (strHistoryInformationInqInParm.getStrTargetTableInfoSeq());
            strRecipeParameterAdjustHistory_GetDR_in.setMaxRecordCount        (strHistoryInformationInqInParm.getMaxRecordCount());
            strRecipeParameterAdjustHistory_GetDR_in.setSearchCondition        (strHistoryInformationInqInParm.getSearchCondition());

            // step5 - recipeParameterAdjustHistory_GetDR
            Outputs.RecipeParameterAdjustHistoryGetDROut recipeParameterAdjustHistoryGetDROut = recipeMethod.recipeParameterAdjustHistoryGetDR (  strObjCommonIn, strRecipeParameterAdjustHistory_GetDR_in);
            strHistoryInformationInqResult.setStrTableRecordInfoSeq   (recipeParameterAdjustHistoryGetDROut.getStrTableRecordInfoSeq());
            strHistoryInformationInqResult.setStrTableRecordValueSeq  (recipeParameterAdjustHistoryGetDROut.getStrTableRecordValueSeq());
            strHistoryInformationInqResult.setStrTableRecordValuePage  (recipeParameterAdjustHistoryGetDROut.getStrTableRecordValuePage());
        } else if( CimStringUtils.equals( strHistoryInformationInqInParm.getHistoryCategory(), BizConstant.SP_HISTORYCATEGORY_EQPMONITORJOB ) ) {
            Infos.EquipmentMonitorJobLotHistoryGetDROut strEquipmentMonitorJobLotHistory_GetDR_out;
            Infos.EquipmentMonitorJobLotHistoryGetDRIn strEquipmentMonitorJobLotHistory_GetDR_in=new Infos.EquipmentMonitorJobLotHistoryGetDRIn();
            strEquipmentMonitorJobLotHistory_GetDR_in.setFromTimeStamp         (strHistoryInformationInqInParm.getFromTimeStamp());
            strEquipmentMonitorJobLotHistory_GetDR_in.setToTimeStamp           (strHistoryInformationInqInParm.getToTimeStamp());
            strEquipmentMonitorJobLotHistory_GetDR_in.setHistoryCategory       (strHistoryInformationInqInParm.getHistoryCategory());
            strEquipmentMonitorJobLotHistory_GetDR_in.setStrTargetTableInfoSeq (strHistoryInformationInqInParm.getStrTargetTableInfoSeq());
            strEquipmentMonitorJobLotHistory_GetDR_in.setMaxRecordCount        (strHistoryInformationInqInParm.getMaxRecordCount());
            strEquipmentMonitorJobLotHistory_GetDR_in.setSearchCondition        (strHistoryInformationInqInParm.getSearchCondition());

            // step6 - equipmentMonitorJobLotHistory_GetDR
            strEquipmentMonitorJobLotHistory_GetDR_out = equipmentMethod.equipmentMonitorJobLotHistoryGetDR( strObjCommonIn, strEquipmentMonitorJobLotHistory_GetDR_in);
            strHistoryInformationInqResult.setStrTableRecordInfoSeq   (strEquipmentMonitorJobLotHistory_GetDR_out.getStrTableRecordInfoSeq());
            strHistoryInformationInqResult.setStrTableRecordValueSeq  (strEquipmentMonitorJobLotHistory_GetDR_out.getStrTableRecordValueSeq());
            strHistoryInformationInqResult.setStrTableRecordValuePage  (strEquipmentMonitorJobLotHistory_GetDR_out.getStrTableRecordValuePage());
        }else if ( CimStringUtils.equals(BizConstant.SP_HISTORYCATEGORY_RUNCARD,strHistoryInformationInqInParm.getHistoryCategory())){
            Infos.RunCardHistoryGetDRIn runCardHistoryGetDRIn = new Infos.RunCardHistoryGetDRIn();
            Infos.RunCardHistoryGetDROut runCardHistoryGetDROut = new Infos.RunCardHistoryGetDROut();
            runCardHistoryGetDRIn.setFromTimeStamp(strHistoryInformationInqInParm.getFromTimeStamp());
            runCardHistoryGetDRIn.setToTimeStamp(strHistoryInformationInqInParm.getToTimeStamp());
            runCardHistoryGetDRIn.setHistoryCategory(strHistoryInformationInqInParm.getHistoryCategory());
            runCardHistoryGetDRIn.setStrTargetTableInfoSeq(strHistoryInformationInqInParm.getStrTargetTableInfoSeq());
            runCardHistoryGetDRIn.setMaxRecordCount(strHistoryInformationInqInParm.getMaxRecordCount());
            runCardHistoryGetDRIn.setSearchCondition(strHistoryInformationInqInParm.getSearchCondition());

            //strp7 - runCardPsmDocHisotryGetDR
            runCardHistoryGetDROut = lotMethod.runCardPsmDocHisotryGetDR(strObjCommonIn,runCardHistoryGetDRIn);
            strHistoryInformationInqResult.setStrTableRecordInfoSeq(runCardHistoryGetDROut.getStrTableRecordInfoSeq());
            strHistoryInformationInqResult.setStrTableRecordValueSeq(runCardHistoryGetDROut.getStrTableRecordValueSeq());
            strHistoryInformationInqResult.setStrTableRecordValuePage(runCardHistoryGetDROut.getStrTableRecordValuePage());
        }
        return strHistoryInformationInqResult;
    }

    @Override
    public Results.LotAnnotationInqResult sxLotAnnotationInq(Infos.ObjCommon objCommon, ObjectIdentifier lotID) {
        /*-----------------------------------------------------------*/
        /*   Check PosLot, get PosLotComment                         */
        /*-----------------------------------------------------------*/
        Results.LotAnnotationInqResult out = lotMethod.lotCommentFillInTxPLQ002DR(objCommon, lotID);
        return out;
    }

    @Override
    public Results.LotMemoInfoInqResult sxLotMemoInfoInq(Infos.ObjCommon objCommon, ObjectIdentifier lotID) {
        return lotMethod.lotNoteFillInTxPLQ003DR(objCommon, lotID);
    }

    @Override
    public Results.LotOperationSelectionFromHistoryInqResult sxLotOperationSelectionFromHistoryInq(Infos.ObjCommon objCommon, Params.LotOperationSelectionFromHistoryInqParams params) {
        Results.LotOperationSelectionFromHistoryInqResult data = new Results.LotOperationSelectionFromHistoryInqResult();

        // Check Input condition ;
        List<Infos.OperationNameAttributesFromHistory> operationNameAttributesRetCode = processMethod.processOperationListFromHistoryDR(objCommon,params.getSearchCount(),params.getLotID(),params.getFromTimeStamp(),params.getToTimeStamp());
        data.setOperationNameAttributesList(operationNameAttributesRetCode);

        return data;
    }

    @Override
    public Results.LotOpeMemoInfoInqResult sxLotOpeMemoInfoInq(Params.LotOpeMemoInfoInqParams lotOpeMemoInfoInqParams, Infos.ObjCommon objCommon) {
        log.info("【Method Entry】sxLotOpeMemoInfoInq()");

        //Check PosLot, get PosProcessOperation and PosLotNote;
        return lotMethod.lotOperationNoteFillInTxPLQ005DR(objCommon, lotOpeMemoInfoInqParams);
    }

    @Override
    public Results.LotOpeMemoListInqResult sxLotOpeMemoListInq(Params.LotOpeMemoListInqParams params, Infos.ObjCommon objCommon) {
        log.info("【Method Entry】sxLotOpeMemoListInq()");

        //Check PosLot, get PosProcessOperation and PosLotNote ;
        Results.LotOpeMemoListInqResult resultRetCode = lotMethod.lotOperationNoteFillInTxPLQ004DR(objCommon, params);

        log.info("【Method Exit】sxLotOpeMemoListInq()");

        return resultRetCode;
    }

    @Override
    public Results.LotOperationHistoryInqResult sxLotOperationHistoryInq(Infos.ObjCommon strObjCommonIn, ObjectIdentifier lotID, ObjectIdentifier routeID, ObjectIdentifier operationID, String operationNumber, String operationPass, String operationCategory, Boolean pinPointFlag, String fromTimeStamp, String toTimeStamp) {
        Infos.OperationHistoryFillInTxPLQ008DROut strOperationHistory_FillInTxPLQ008DR_out ;
        Infos.OperationHistoryFillInTxPLQ008DRIn strOperationHistory_FillInTxPLQ008DR_in=new Infos.OperationHistoryFillInTxPLQ008DRIn();
        strOperationHistory_FillInTxPLQ008DR_in.setLotID ( lotID);
        strOperationHistory_FillInTxPLQ008DR_in.setRouteID ( routeID);
        strOperationHistory_FillInTxPLQ008DR_in.setOperationID ( operationID);
        strOperationHistory_FillInTxPLQ008DR_in.setOperationNumber ( (operationNumber));
        strOperationHistory_FillInTxPLQ008DR_in.setOperationPass ( (operationPass));
        strOperationHistory_FillInTxPLQ008DR_in.setOperationCategory ( (operationCategory));
        strOperationHistory_FillInTxPLQ008DR_in.setPinPointFlag ( pinPointFlag);
        //add fromTimeStamp and toTimeStamp for runcard history
        strOperationHistory_FillInTxPLQ008DR_in.setFromTimeStamp(fromTimeStamp);
        strOperationHistory_FillInTxPLQ008DR_in.setToTimeStamp(toTimeStamp);
        // step1 - operationHistory_FillInTxPLQ008DR__160
        strOperationHistory_FillInTxPLQ008DR_out = operationMethod.operationHistoryFillInTxPLQ008DR(strObjCommonIn, strOperationHistory_FillInTxPLQ008DR_in);
        Results.LotOperationHistoryInqResult strOperationHisInfo=new Results.LotOperationHistoryInqResult();
        strOperationHisInfo.setStrOperationHisInfo(strOperationHistory_FillInTxPLQ008DR_out.getStrOperationHisInfo());

        return(strOperationHisInfo);
    }

    @Override
    public Results.SubLotTypeListInqResult sxSubLotTypeListInq(Infos.ObjCommon objCommon, Params.SubLotTypeListInqParams subLotTypeListInqParams) {
        Results.SubLotTypeListInqResult subLotTypeListInqResult = new Results.SubLotTypeListInqResult();

        List<Infos.LotTypeInfo> objLotTypeSubLotTypeInfoOut = lotMethod.lotTypeSubLotTypeInfoGet(objCommon, subLotTypeListInqParams.getLotType());

        subLotTypeListInqResult.setStrLotTypes(objLotTypeSubLotTypeInfoOut);

        return subLotTypeListInqResult;
    }

    @Override
    public List<String> sxEqpSpecialControlsGet(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID) {
        return equipmentMethod.getEquipmentSpecialControls(objCommon,equipmentID);
    }

    @Override
    public Infos.EqpPortInfo sxPortInfoInq(Infos.ObjCommon objCommon, Params.PortInfoInqParams portInfoInqParams) {
        Params.EqpInfoInqParams eqpInfoInqParams = new Params.EqpInfoInqParams();
        eqpInfoInqParams.setUser(portInfoInqParams.getUser());
        eqpInfoInqParams.setEquipmentID(portInfoInqParams.getEquipmentID());
        eqpInfoInqParams.setRequestFlagForPortInfo(true);
        Results.EqpInfoInqResult eqpInfoInqResult = eqpInfoInqService.sxEqpInfoInq(objCommon, eqpInfoInqParams);
        return eqpInfoInqResult.getEquipmentPortInfo();
    }

    @Override
    public List<Infos.ScrapHistories> sxWaferScrappedHistoryInq(Infos.ObjCommon objCommon, ObjectIdentifier lotID, ObjectIdentifier cassetteID) {
        if (ObjectIdentifier.isEmpty(lotID)) {
            throw new ServiceException(retCodeConfig.getInvalidInputParam());
        }
        /*--------------------------------------------------------------------*/
        /*   Search PosLotFamily, PosWafer to get scrapped WaferID/LotID list */
        /*--------------------------------------------------------------------*/
        return lotFamilyMethod.lotFamilyFillInTxDFQ003DR(objCommon, lotID);
    }

    @Override
    public List<Infos.EquipmentAlarm> sxEqpAlarmHistInq(Infos.ObjCommon objCommon, Params.EqpAlarmHistInqParams params) {
        if (ObjectIdentifier.isEmpty(params.getObjectID()) || CimStringUtils.isEmpty(params.getInquireType())
                || CimStringUtils.isEmpty(params.getFromTimeStamp()) || CimStringUtils.isEmpty(params.getToTimeStamp())) {
            Validations.check(retCodeConfig.getInvalidParameter());
        }
        return equipmentMethod.equipmentAlarmHistoryFillInTxEQQ011DR(objCommon, params.getObjectID(), params.getInquireType(), params.getFromTimeStamp(), params.getToTimeStamp());
    }

    @Override
    public Results.EDCHistoryInqResult sxEDCHistoryInq(Infos.ObjCommon objCommon, Params.EDCHistoryInqParams params) {
        log.info("【Method Entry】sxEDCHistoryInq()");
        // Get DCDef Information in PO ;

        log.info("【Method Exit】sxEDCHistoryInq()");

        return lotMethod.lotDataCollectionInformationGetDR(objCommon,params);
    }

    @Override
    public List<Results.EqpSearchForSettingEqpBoardResult> sxEqpSearchForSettingEqpBoardInq(Infos.ObjCommon objCommon, Params.EqpSearchForSettingEqpBoardParams eqpSearchForSettingEqpBoardParams) {
        log.info("eqpSearchForSettingEqpBoardInq()->info: call eqpSearchForSettingEqpBoard");
        return equipmentAreaMethod.eqpSearchForSettingEqpBoard(objCommon, eqpSearchForSettingEqpBoardParams);
    }

    @Override
    public List<Results.EqpAreaBoardListResult> sxEqpAreaBoardListInq(Infos.ObjCommon objCommon, Params.EqpAreaBoardListParams eqpAreaBoardListParams) {
        log.info("sxEqpAreaBoardListInq()->info: call eqpAreaBoardList");
        return equipmentAreaMethod.eqpAreaBoardList(objCommon, eqpAreaBoardListParams);
    }

    @Override
    public List<String> sxEqpWorkZoneListInq(Infos.ObjCommon objCommon, Params.EqpWorkZoneListParams eqpWorkZoneListParams) {
        log.info("sxEqpWorkZoneListInq()->info: call sxEqpAreaBoardListInq");
        return equipmentAreaMethod.eqpWorkZoneList(objCommon, eqpWorkZoneListParams);
    }

    @Override
    public Results.EDCHistoryInqResult sxMonitorLotDataCollectionInq(
            Infos.ObjCommon objCommon, LotMonitorGroupParams.MonitorDataCollectionParams monitorDataCollectionParams) {
        if (log.isInfoEnabled()) {
            log.info("sxMonitorLotDataCollectionInq()-> info : monitor = {}", monitorDataCollectionParams.toString());
        }

        // step.1 验证monitor group id 不能为空
        Validations.check(StrUtil.isBlank(monitorDataCollectionParams.getMonitorGroupId()),
                retCodeConfig.getInvalidParameter());

        // step2. 通过 monitor group id 获取 monitor lot Id
        List<LotMonitorGroupResults.MonitorLotDataCollectionQueryResults> resultList = lotMethod
                .monitorGroupLotGet(objCommon, monitorDataCollectionParams.getMonitorGroupId());
        if (log.isInfoEnabled()) {
            log.info("sxMonitorLotDataCollectionInq()->info : monitorGroupLotGet result data = {}",
                    resultList);
        }
        // step3. 通过monitor lot Id + monitor Group Id , 拼接查询EDC History 数据
        Results.EDCHistoryInqResult edcHistoryInqResult = new Results.EDCHistoryInqResult();
        List<Infos.DCDefResult> strDCDefResult = Lists.newArrayList();
        edcHistoryInqResult.setStrDCDefResult(strDCDefResult);
        resultList.forEach(monitorLotDataCollectionQueryResults -> {
            if (ObjectIdentifier.isNotEmpty(monitorLotDataCollectionQueryResults.getMonitorLotId())) {
                // step4. 获取monitor lot 的信息
                Params.EDCHistoryInqParams edcHistoryInqParams = new Params.EDCHistoryInqParams();
                edcHistoryInqParams.setLotID(monitorLotDataCollectionQueryResults.getMonitorLotId());
                edcHistoryInqParams.setOperationNumber(monitorLotDataCollectionQueryResults.getOperationNumber());
                edcHistoryInqParams.setOperationPass(monitorLotDataCollectionQueryResults.getOperationPassCount());
                edcHistoryInqParams.setRouteID(
                        ObjectIdentifier.build(monitorLotDataCollectionQueryResults.getRouteId(), BizConstant.EMPTY));
                edcHistoryInqParams.setGetSpecFlag(true);
                Results.EDCHistoryInqResult edcHistoryInq = sxEDCHistoryInq(objCommon, edcHistoryInqParams);
                if (Objects.nonNull(edcHistoryInq) && CollectionUtil.isNotEmpty(edcHistoryInq.getStrDCDefResult())) {
                    strDCDefResult.addAll(edcHistoryInq.getStrDCDefResult());
                }
            }
        });
        return edcHistoryInqResult;

    }
}
