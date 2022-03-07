package com.fa.cim.method.impl;

import com.fa.cim.annotaion.OmMethod;
import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.utils.*;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Results;
import com.fa.cim.method.ILotMethod;
import com.fa.cim.method.ISpecCheckMethod;
import com.fa.cim.newcore.bo.dc.CimDataCollectionDefinition;
import com.fa.cim.newcore.bo.pd.CimProcessDefinition;
import com.fa.cim.newcore.bo.pd.CimProcessFlowContext;
import com.fa.cim.newcore.bo.pd.CimProcessOperation;
import com.fa.cim.newcore.bo.product.CimLot;
import com.fa.cim.newcore.dto.dc.EDCDTO;
import com.fa.cim.newcore.dto.pd.ProcessDTO;
import com.fa.cim.newcore.factory.BaseCoreFactory;
import com.fa.cim.newcore.impl.bo.env.StandardProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * description:
 * <p>SpecCheckMethod .<br/></p>
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2018/10/19        ********             PlayBoy               create file
 *
 * @author: PlayBoy
 * @date: 2018/10/19 12:27
 * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@OmMethod
public class SpecCheckMethod implements ISpecCheckMethod {

    @Autowired
    private RetCodeConfig retCodeConfig;

    @Autowired
    private ILotMethod lotMethod;

    @Autowired
    private BaseCoreFactory baseCoreFactory;

    @Override
    @Deprecated
    public Results.SpecCheckReqResult specCheckFillInTxDCC001DR(Infos.ObjCommon objCommon,
                                                                ObjectIdentifier equipmentID,
                                                                ObjectIdentifier controlJobID,
                                                                List<Infos.StartCassette> startCassetteList) {

        Results.SpecCheckReqResult retVal = new Results.SpecCheckReqResult();
        retVal.setControlJobID(controlJobID);
        retVal.setEquipmentID(equipmentID);
        retVal.setStartCassetteList(startCassetteList);

        boolean addCalculationAndSpecCheckFalseFlag = true;
        String notCalculateFlag = StandardProperties.OM_EDC_SCREEN_LIMIT_OVER_NO_CALC.getValue();

        if (CimArrayUtils.isNotEmpty(startCassetteList)) {
            for (Infos.StartCassette startCassette : startCassetteList) {
                for (Infos.LotInCassette lotInCassette : startCassette.getLotInCassetteList()) {
                    //-----------------------------
                    // Omit is not MoveIn Lot
                    //-----------------------------
                    if (CimBooleanUtils.isFalse(lotInCassette.getMoveInFlag())) {
                        continue;
                    }
                    for (Infos.DataCollectionInfo dcDef : lotInCassette.getStartRecipe().getDcDefList()) {
                        if (CimBooleanUtils.isTrue(dcDef.getCalculationRequiredFlag())
                                || CimBooleanUtils.isTrue(dcDef.getSpecCheckRequiredFlag())) {
                            addCalculationAndSpecCheckFalseFlag = false;
                            break;
                        }
                    }
                    if (CimBooleanUtils.isFalse(addCalculationAndSpecCheckFalseFlag)) {
                        break;
                    }
                }
                if (CimBooleanUtils.isFalse(addCalculationAndSpecCheckFalseFlag)) {
                    break;
                }
            }
        }
        if (CimBooleanUtils.isTrue(addCalculationAndSpecCheckFalseFlag)) {
            return retVal;
        }

        CimProcessOperation processOperation;
        if (CimArrayUtils.isNotEmpty(startCassetteList)) {
            for (Infos.StartCassette startCassette : startCassetteList) {
                List<Infos.LotInCassette> lotInCassetteList = startCassette.getLotInCassetteList();
                if (CimArrayUtils.isNotEmpty(lotInCassetteList)) {
                    for (Infos.LotInCassette lotInCassette : lotInCassetteList) {
                        //-----------------------------
                        // Omit is not MoveIn Lot
                        //-----------------------------
                        if (CimBooleanUtils.isFalse(lotInCassette.getMoveInFlag())) {
                            continue;
                        }
                        /*--------------------*/
                        /*   Get lot Object   */
                        /*--------------------*/
                        //PPT_CONVERT_LOTID_TO_LOT_OR()
                        CimLot lot = baseCoreFactory.getBO(CimLot.class, lotInCassette.getLotID());
                        //------------------------------------------
                        // Current PO or Previous PO ?
                        //------------------------------------------
                        //step1 - lotCheckConditionForPO
                        Boolean objLotCheckConditionForPO = lotMethod.lotCheckConditionForPO(objCommon, lotInCassette.getLotID());

                        if (CimBooleanUtils.isTrue(objLotCheckConditionForPO)) {
                            //--------------------------------------------------------------------------
                            // Get PO from Current Operation.
                            //--------------------------------------------------------------------------
                            processOperation = lot.getProcessOperation();
                        } else {
                            //--------------------------------------------------------------------------
                            // Get PO from Previous Operation.
                            //--------------------------------------------------------------------------
                            processOperation = lot.getPreviousProcessOperation();
                        }
                        Validations.check(processOperation == null, retCodeConfig.getNotFoundProcessOperation());

                        List<ProcessDTO.DataCollectionInfo> dcDataCollectionInfoSeq = new ArrayList<>();
                        boolean dataCollectionExistFlag = false;
                        String corresPondingOpeMode = StandardProperties.OM_EDC_MULTI_CORRESPOND_FLAG.getValue();

                        if (CimStringUtils.equals(corresPondingOpeMode, BizConstant.VALUE_ONE)) {
                            ProcessDTO.ActualStartInformationForPO actualStartInfo = processOperation.getActualStartInfo(true);
                            if (CimBooleanUtils.isTrue(actualStartInfo.getAssignedDataCollectionFlag())) {
                                dcDataCollectionInfoSeq = actualStartInfo.getAssignedDataCollections();
                                dataCollectionExistFlag = true;
                            }
                        }
                        if (CimStringUtils.unEqual(notCalculateFlag, BizConstant.VALUE_ZERO)) {
                            Infos.DataCollectionInfo strDCDef = lotInCassette.getStartRecipe().getDcDefList().get(0);
                            if (CimBooleanUtils.isTrue(strDCDef.getSpecCheckRequiredFlag())) {
                                //-----------------------------------------------------------------------------------------
                                // [1] Set Input Value for doValueConversion()
                                //-----------------------------------------------------------------------------------------
                                List<EDCDTO.DCItemData> strRawDataDCItemData = new ArrayList<>();
                                List<Infos.DataCollectionItemInfo> dcItemList = strDCDef.getDcItems();
                                for (Infos.DataCollectionItemInfo dcItem : dcItemList) {
                                    EDCDTO.DCItemData dcItemData = new EDCDTO.DCItemData();
                                    dcItemData.setDataItemName(dcItem.getDataCollectionItemName());
                                    dcItemData.setWaferPosition(dcItem.getWaferPosition());
                                    dcItemData.setSitePosition(dcItem.getSitePosition());
                                    dcItemData.setValType(dcItem.getDataType());
                                    dcItemData.setItemType(dcItem.getItemType());
                                    dcItemData.setCalculationType(dcItem.getCalculationType());
                                    dcItemData.setCalculationExpression(dcItem.getCalculationExpression());
                                    dcItemData.setInputValue(dcItem.getDataValue());
                                    dcItemData.setSpecCheckResult("");
                                    dcItemData.setWaferID(dcItem.getWaferID().getValue());
                                    dcItemData.setMeasurementType(dcItem.getMeasurementType());

                                    strRawDataDCItemData.add(dcItemData);
                                }

                                //-----------------------------------------------------------------------------------------
                                // [2] Convert value
                                //-----------------------------------------------------------------------------------------
                                CimDataCollectionDefinition aDataCollectionDefinition =
                                        baseCoreFactory.getBO(CimDataCollectionDefinition.class, strDCDef.getDataCollectionDefinitionID());
                                Validations.check(aDataCollectionDefinition == null, retCodeConfig.getNotFoundDcdef());
                                aDataCollectionDefinition.doValueConversion(strRawDataDCItemData);

                                //-----------------------------------------------------------------------------------------
                                // [3] Spec Check
                                //-----------------------------------------------------------------------------------------
                                processOperation.doSpecCheck(strRawDataDCItemData, strDCDef.getDataCollectionSpecificationID().getValue());

                                //-----------------------------------------------------------------------------------------
                                // [4] Set data done spec check
                                //-----------------------------------------------------------------------------------------
                                dcItemList = new ArrayList<>();
                                int j = 0;
                                for (EDCDTO.DCItemData dcItemData : strRawDataDCItemData) {
                                    Infos.DataCollectionItemInfo dataCollectionItemInfo = strDCDef.getDcItems().get(j++);
                                    dataCollectionItemInfo.setDataCollectionItemName(dcItemData.getDataItemName());
                                    dataCollectionItemInfo.setWaferPosition(dcItemData.getWaferPosition());
                                    dataCollectionItemInfo.setSitePosition(dcItemData.getSitePosition());
                                    dataCollectionItemInfo.setDataType(dcItemData.getValType());
                                    dataCollectionItemInfo.setItemType(dcItemData.getItemType());
                                    dataCollectionItemInfo.setCalculationType(dcItemData.getCalculationType());
                                    dataCollectionItemInfo.setCalculationExpression(dcItemData.getCalculationExpression());
                                    dataCollectionItemInfo.setDataValue(dcItemData.getInputValue());
                                    dataCollectionItemInfo.setSpecCheckResult(dcItemData.getSpecCheckResult());
                                    dataCollectionItemInfo.setWaferID(BaseStaticMethod.getObjectIdentifier(dcItemData.getWaferID()));
                                    dataCollectionItemInfo.setMeasurementType(dcItemData.getMeasurementType());
                                    dcItemList.add(dataCollectionItemInfo);
                                }
                                strDCDef.setDcItems(dcItemList);
                                lotInCassette.getStartRecipe().getDcDefList().set(0, strDCDef);
                            }
                        }

                        //-------------------------------------------------------------------------------------
                        // Delta DC Defs
                        //-------------------------------------------------------------------------------------
                        List<ProcessDTO.DeltaDcDefInfo> deltaDCDefs = processOperation.getAssignedDeltaDCDefs();
                        List<EDCDTO.DCItemData> strRawDCItemData = new ArrayList<>();

                        int dataCollectionLen = lotInCassette.getStartRecipe().getDcDefList().size();

                        List<ProcessDTO.DataCollectionInfo> dataCollectionInfos = new ArrayList<>();
                        ProcessDTO.DataCollectionInfo dataCollectionInfo = new ProcessDTO.DataCollectionInfo();
                        List<ProcessDTO.DataCollectionItemInfo> dcItems = new ArrayList<>();
                        List<ProcessDTO.DataCollectionSpecInfo> dcSpecs = new ArrayList<>();


                        for (int m = 0; m < dataCollectionLen; m++) {
                            Infos.DataCollectionInfo dcDef = lotInCassette.getStartRecipe().getDcDefList().get(m);
                            int dcItemslen = dcDef.getDcItems().size();
                            CimProcessOperation aDeltaProcessOperation = null;

                            //-----------------------------------------------------------------------------------------
                            // [1] Get Delta Process Operation if Delta Calculation exists
                            //-----------------------------------------------------------------------------------------
                            if (m != 0) {
                                for (int i = 0; i < dcItemslen; i++) {
                                    if (CimStringUtils.equals(dcDef.getDcItems().get(i).getCalculationType(), BizConstant.SP_DCDEF_CALC_DELTA)) {
                                        //-----------------------------------------------------------------------------------------
                                        // Get pfx from lot
                                        //-----------------------------------------------------------------------------------------
                                        CimProcessFlowContext processFlowContext = lot.getProcessFlowContext();
                                        Validations.check(processFlowContext == null, retCodeConfig.getNotFoundPfx());
                                        //-----------------------------------------------------------------------------------------
                                        // Get RouteID from Process Operation
                                        //-----------------------------------------------------------------------------------------
                                        CimProcessDefinition mainProcessDefinition = processOperation.getMainProcessDefinition();
                                        Validations.check(mainProcessDefinition == null, retCodeConfig.getNotFoundProcessDefinition());
                                        String routeID = mainProcessDefinition.getIdentifier();
                                        //-----------------------------------------------------------------------------------------
                                        // Get ProcesOperation from pfx by RouteID and Operation Number
                                        //-----------------------------------------------------------------------------------------
                                        aDeltaProcessOperation = processFlowContext.findProcessOperationForRouteOperationNumberBefore(routeID,
                                                deltaDCDefs.get(m).getProcessOperation());
                                        Validations.check(aDeltaProcessOperation == null, retCodeConfig.getNotFoundDcspec());
                                        break;
                                    }
                                }
                            }

                            //-----------------------------------------------------------------------------------------
                            // [2] Set Input Value for doValueConversion()
                            //-----------------------------------------------------------------------------------------
                            List<EDCDTO.DCItemData> strDCItemData = new ArrayList<>();
                            for (int i = 0; i < dcItemslen; i++) {
                                Infos.DataCollectionItemInfo dcItem = dcDef.getDcItems().get(i);

                                EDCDTO.DCItemData dcItemData = new EDCDTO.DCItemData();
                                strDCItemData.add(dcItemData);

                                dcItemData.setDataItemName(dcItem.getDataCollectionItemName());
                                dcItemData.setWaferPosition(dcItem.getWaferPosition());
                                dcItemData.setSitePosition(dcItem.getSitePosition());
                                dcItemData.setValType(dcItem.getDataType());
                                dcItemData.setItemType(dcItem.getItemType());
                                dcItemData.setCalculationType(dcItem.getCalculationType());
                                dcItemData.setCalculationExpression(dcItem.getCalculationExpression());
                                dcItemData.setInputValue(dcItem.getDataValue());
                                dcItemData.setMeasurementType(dcItem.getMeasurementType());

                                dcItemData.setWaferCount(dcItem.getWaferCount());
                                dcItemData.setSiteCount(dcItem.getSiteCount());

                                if (CimStringUtils.equals(BizConstant.SP_DCDEF_MEAS_PJ, dcItem.getMeasurementType())
                                        || CimStringUtils.equals(BizConstant.SP_DCDEF_MEAS_PJWAFER, dcItem.getMeasurementType())
                                        || CimStringUtils.equals(BizConstant.SP_DCDEF_MEAS_PJWAFERSITE, dcItem.getMeasurementType())) {
                                    dcItemData.setSpecCheckResult(dcItem.getSpecCheckResult());
                                } else {
                                    dcItemData.setSpecCheckResult("");
                                }
                                if (CimStringUtils.unEqual(notCalculateFlag, BizConstant.VALUE_ZERO)) {
                                    if (CimStringUtils.equals(dcItem.getItemType(), BizConstant.SP_DCDEF_ITEM_RAW)) {
                                        if (CimStringUtils.equals(dcItem.getSpecCheckResult(), BizConstant.SP_SPECCHECKRESULT_UPPERSCREENLIMIT)
                                                || CimStringUtils.equals(dcItem.getSpecCheckResult(), BizConstant.SP_SPECCHECKRESULT_LOWERSCREENLIMIT)) {
                                            dcItemData.setSpecCheckResult(dcItem.getSpecCheckResult());
                                        }
                                    }
                                }
                                if (CimStringUtils.equals(dcItem.getItemType(), BizConstant.SP_DCDEF_ITEM_DERIVED)) {
                                    if (CimStringUtils.unEqual(dcItem.getMeasurementType(), BizConstant.SP_DCDEF_MEAS_PJWAFER)
                                            && CimStringUtils.unEqual(dcItem.getMeasurementType(), BizConstant.SP_DCDEF_MEAS_PJWAFERSITE)
                                            && CimStringUtils.unEqual(dcItem.getMeasurementType(), BizConstant.SP_DCDEF_MEAS_WAFER)
                                            && CimStringUtils.unEqual(dcItem.getMeasurementType(), BizConstant.SP_DCDEF_MEAS_SITE)) {
                                        dcItem.setWaferID(ObjectIdentifier.emptyIdentifier());
                                    }
                                }
                                dcItemData.setWaferID(ObjectIdentifier.fetchValue(dcItem.getWaferID()));
                            }
                            //--- Keep raw data item
                            if (m == 0) {
                                strRawDCItemData = new ArrayList<>();
                                final int size = CimArrayUtils.getSize(strDCItemData);
                                for (int i1 = 0; i1 < size; i1++) {
                                    strRawDCItemData.add(new EDCDTO.DCItemData());
                                    BeanUtils.copyProperties(strDCItemData.get(i1), strRawDCItemData.get(i1));
                                }
                            }

                            //-----------------------------------------------------------------------------------------
                            // [3] Convert & Clalculate value
                            //-----------------------------------------------------------------------------------------
                            CimDataCollectionDefinition aDataCollectionDefinition =
                                    baseCoreFactory.getBO(CimDataCollectionDefinition.class, dcDef.getDataCollectionDefinitionID());
                            Validations.check(aDataCollectionDefinition == null, retCodeConfig.getNotFoundDcdef());

                            // doValueConversion
                            aDataCollectionDefinition.doValueConversion(strDCItemData);

                            for (EDCDTO.DCItemData strDCItemDatum : strDCItemData) {
                                if (CimStringUtils.equals(strDCItemDatum.getItemType(), BizConstant.SP_DCDEF_ITEM_USERFUNCTION)) {
                                    strDCItemDatum.setSpecCheckResult("#");
                                }
                            }
                            String strObjrefPO = processOperation.getPrimaryKey();
                            // doCalculation
                            aDataCollectionDefinition.doCalculation(strDCItemData, aDeltaProcessOperation, strRawDCItemData, strObjrefPO);


                            //--- Keep raw data item after calcuration of derived
                            if (m == 0) {
                                strRawDCItemData = new ArrayList<>();
                                final int size = CimArrayUtils.getSize(strDCItemData);
                                for (int i1 = 0; i1 < size; i1++) {
                                    strRawDCItemData.add(new EDCDTO.DCItemData());
                                    BeanUtils.copyProperties(strDCItemData.get(i1), strRawDCItemData.get(i1));
                                }
                            }

                            //----------------------------------------------------------------------------------
                            // Set derived value to return value
                            //----------------------------------------------------------------------------------
                            String tmpStr = "";
                            for (int i = 0; i < dcItemslen; i++) {
                                final String specCheckResult = strDCItemData.get(i).getSpecCheckResult();
                                final String dataMode = dcDef.getDcItems().get(i).getDataCollectionMode();
                                final String valType = strDCItemData.get(i).getValType();

                                if (CimStringUtils.unEqual(specCheckResult, "*")
                                        && CimStringUtils.unEqual(specCheckResult, "#")
                                        && CimStringUtils.equals(dataMode, BizConstant.SP_DCDEF_ITEM_DERIVED)) {
                                    // Number Value
                                    final Double numValue = strDCItemData.get(i).getNumValue();

                                    if (CimStringUtils.equals(valType, BizConstant.SP_DCDEF_VAL_FLOAT)) {
                                        if (-1.0 < numValue && numValue < 1.0) {
                                            tmpStr = String.format("%.9G", numValue);
                                            if (tmpStr.length() > 12) {
                                                tmpStr = String.format("%G", numValue);
                                            }
                                        } else if (-100000000000.0 < numValue && numValue < 1000000000000.0) {
                                            tmpStr = String.format("%.1f", numValue);
                                            if (tmpStr.length() > 12) {
                                                tmpStr = tmpStr.substring(0, 12);
                                            }
                                        } else {
                                            tmpStr = String.format("%G", numValue);
                                        }
                                    }
                                    if (CimStringUtils.equals(valType, BizConstant.SP_DCDEF_VAL_INTEGER)) {
                                        if (numValue == Integer.MIN_VALUE || numValue == Integer.MAX_VALUE) {
                                            tmpStr = String.format("%s", "*");
                                        } else {
                                            tmpStr = String.format("%s", numValue);
                                        }
                                    }
                                    if (tmpStr.length() > 12 || CimStringUtils.equals(tmpStr, "*")) {
                                        dcDef.getDcItems().get(i).setDataValue("*");
                                        dcDef.getDcItems().get(i).setSpecCheckResult("*");
                                    } else {
                                        dcDef.getDcItems().get(i).setDataValue(tmpStr);
                                    }
                                }
                                if (CimStringUtils.equals(specCheckResult, "#")
                                        && CimStringUtils.equals(dataMode, BizConstant.SP_DCDEF_ITEM_DERIVED)) {
                                    dcDef.getDcItems().get(i).setDataValue("*");
                                }
                            }

                            //-----------------------------------------------------------------------------------------
                            //  SpecCheck is required only when the DCSpec exists.
                            //-----------------------------------------------------------------------------------------
                            if (CimBooleanUtils.isTrue(dcDef.getSpecCheckRequiredFlag())) {
                                //-----------------------------------------------------------------------------------------
                                // [4] Spec Check
                                //-----------------------------------------------------------------------------------------
                                processOperation.doSpecCheck(strDCItemData, dcDef.getDataCollectionSpecificationID().getValue());

                                if (CimStringUtils.unEqual(notCalculateFlag, BizConstant.VALUE_ZERO)) {
                                    for (EDCDTO.DCItemData strDCItemDatum : strDCItemData) {
                                        if (CimStringUtils.equals(strDCItemDatum.getItemType(), BizConstant.SP_DCDEF_ITEM_USERFUNCTION)) {
                                            strDCItemDatum.setSpecCheckResult("#");
                                        }
                                    }

                                    // doCalculation
                                    aDataCollectionDefinition.doCalculation(strDCItemData,
                                            aDeltaProcessOperation,
                                            strRawDCItemData,
                                            processOperation.getPrimaryKey());

                                    //--- Keep raw data item after calcuration of derived
                                    if (m == 0) {
                                        strRawDCItemData = new ArrayList<>();
                                        for (int i1 = 0; i1 < CimArrayUtils.getSize(strDCItemData); i1++) {
                                            strRawDCItemData.add(new EDCDTO.DCItemData());
                                            BeanUtils.copyProperties(strDCItemData.get(i1), strRawDCItemData.get(i1));
                                        }
                                    }
                                    processOperation.doSpecCheck(strDCItemData, dcDef.getDataCollectionSpecificationID().getValue());
                                }

                                //-----------------------------------------------------------------------------------------
                                // [5] Set output data
                                //-----------------------------------------------------------------------------------------
                                for (int i = 0; i < dcItemslen; i++) {
                                    dcDef.getDcItems().get(i).setSpecCheckResult(strDCItemData.get(i).getSpecCheckResult());

                                    StringBuilder sb = new StringBuilder();
                                    if (CimArrayUtils.isNotEmpty(strDCItemData.get(i).getActionCodes())) {
                                        for (String actionCode : strDCItemData.get(i).getActionCodes()) {
                                            sb.append(actionCode).append(",");
                                        }
                                        String actionCodes = sb.substring(0, sb.lastIndexOf(","));
                                        dcDef.getDcItems().get(i).setActionCodes(actionCodes);
                                    }

                                    tmpStr = strDCItemData.get(i).getInputValue();
                                    if (CimStringUtils.equals(strDCItemData.get(i).getSpecCheckResult(), "*")) {
                                        dcDef.getDcItems().get(i).setDataValue(strDCItemData.get(i).getInputValue());
                                    } else if (CimStringUtils.equals(strDCItemData.get(i).getSpecCheckResult(), "#")) {
                                        dcDef.getDcItems().get(i).setDataValue("*");
                                        dcDef.getDcItems().get(i).setSpecCheckResult("*");
                                    } else {
                                        if (CimStringUtils.equals(strDCItemData.get(i).getValType(), BizConstant.SP_DCDEF_VAL_FLOAT)) {
                                            if (-1.0 < strDCItemData.get(i).getNumValue() && strDCItemData.get(i).getNumValue() < 1.0) {
                                                tmpStr = String.format("%.9G", strDCItemData.get(i).getNumValue());
                                                if (tmpStr.length() > 12) {
                                                    tmpStr = String.format("%G", strDCItemData.get(i).getNumValue());
                                                }
                                            } else if (-100000000000.0 < strDCItemData.get(i).getNumValue() && strDCItemData.get(i).getNumValue() < 1000000000000.0) {
                                                tmpStr = String.format("%.1f", strDCItemData.get(i).getNumValue());
                                                if (tmpStr.length() > 12) {
                                                    tmpStr = tmpStr.substring(0, 12);
                                                }
                                            } else {
                                                tmpStr = String.format("%G", strDCItemData.get(i).getNumValue());
                                            }
                                        } else if (CimStringUtils.equals(strDCItemData.get(i).getValType(), BizConstant.SP_DCDEF_VAL_INTEGER)) {
                                            if (strDCItemData.get(i).getNumValue() == Integer.MIN_VALUE || strDCItemData.get(i).getNumValue() == Integer.MAX_VALUE) {
                                                tmpStr = String.format("%s", "*");
                                            } else {
                                                tmpStr = String.format("%s", strDCItemData.get(i).getNumValue().intValue());
                                            }
                                        }
                                        if (tmpStr.length() > 12 || CimStringUtils.equals(tmpStr, "*")) {
                                            dcDef.getDcItems().get(i).setDataValue("*");
                                            dcDef.getDcItems().get(i).setSpecCheckResult("*");
                                        } else {
                                            dcDef.getDcItems().get(i).setDataValue(tmpStr);
                                        }
                                    }
                                }

                                // SpecCheckResult covert
                                if (TransactionIDEnum.DATA_SPEC_CHECK_REQ.equals(objCommon.getTransactionID())) {
                                    if (log.isDebugEnabled()) {
                                        log.debug("Transaction ID = {}, so need to convert the check result.", objCommon.getTransactionID());
                                    }
                                    for (int i = 0; i < dcItemslen; i++) {
                                        if (CimStringUtils.equals(dcDef.getDcItems().get(i).getSpecCheckResult(), BizConstant.SP_SPECCHECKRESULT_OK)) {
                                            if (log.isDebugEnabled()) {
                                                log.debug("== Convert specCheckResult: {} -> {}",
                                                        BizConstant.SP_SPECCHECKRESULT_OK,
                                                        BizConstant.SP_SPECCHECKRESULT_1X_OK);
                                            }
                                            dcDef.getDcItems().get(i).setSpecCheckResult(BizConstant.SP_SPECCHECKRESULT_1X_OK);
                                        } else if (CimStringUtils.equals(dcDef.getDcItems().get(i).getSpecCheckResult(), BizConstant.SP_SPECCHECKRESULT_UPPERCONTROLLIMIT)) {
                                            if (log.isDebugEnabled()) {
                                                log.debug("== Convert specCheckResult: {} -> {}",
                                                        BizConstant.SP_SPECCHECKRESULT_UPPERCONTROLLIMIT,
                                                        BizConstant.SP_SPECCHECKRESULT_1X_UPPERCONTROLLIMIT);
                                            }
                                            dcDef.getDcItems().get(i).setSpecCheckResult(BizConstant.SP_SPECCHECKRESULT_1X_UPPERCONTROLLIMIT);
                                        } else if (CimStringUtils.equals(dcDef.getDcItems().get(i).getSpecCheckResult(), BizConstant.SP_SPECCHECKRESULT_LOWERCONTROLLIMIT)) {
                                            if (log.isDebugEnabled()) {
                                                log.debug("== Convert specCheckResult: {} -> {}",
                                                        BizConstant.SP_SPECCHECKRESULT_LOWERCONTROLLIMIT,
                                                        BizConstant.SP_SPECCHECKRESULT_1X_LOWERCONTROLLIMIT);
                                            }
                                            dcDef.getDcItems().get(i).setSpecCheckResult(BizConstant.SP_SPECCHECKRESULT_1X_LOWERCONTROLLIMIT);
                                        } else if (CimStringUtils.equals(dcDef.getDcItems().get(i).getSpecCheckResult(), BizConstant.SP_SPECCHECKRESULT_UPPERSPECLIMIT)) {
                                            if (log.isDebugEnabled()) {
                                                log.debug("== Convert specCheckResult: {} -> {}",
                                                        BizConstant.SP_SPECCHECKRESULT_UPPERSPECLIMIT,
                                                        BizConstant.SP_SPECCHECKRESULT_1X_UPPERSPECLIMIT);
                                            }
                                            dcDef.getDcItems().get(i).setSpecCheckResult(BizConstant.SP_SPECCHECKRESULT_1X_UPPERSPECLIMIT);
                                        } else if (CimStringUtils.equals(dcDef.getDcItems().get(i).getSpecCheckResult(), BizConstant.SP_SPECCHECKRESULT_LOWERSPECLIMIT)) {
                                            if (log.isDebugEnabled()) {
                                                log.debug("== Convert specCheckResult: {} -> {}",
                                                        BizConstant.SP_SPECCHECKRESULT_LOWERSPECLIMIT,
                                                        BizConstant.SP_SPECCHECKRESULT_1X_LOWERSPECLIMIT);
                                            }
                                            dcDef.getDcItems().get(i).setSpecCheckResult(BizConstant.SP_SPECCHECKRESULT_1X_LOWERSPECLIMIT);
                                        } else if (CimStringUtils.equals(dcDef.getDcItems().get(i).getSpecCheckResult(), BizConstant.SP_SPECCHECKRESULT_UPPERSCREENLIMIT)) {
                                            if (log.isDebugEnabled()) {
                                                log.debug("== Convert specCheckResult: {} -> {}",
                                                        BizConstant.SP_SPECCHECKRESULT_UPPERSCREENLIMIT,
                                                        BizConstant.SP_SPECCHECKRESULT_1X_UPPERSCREENLIMIT);
                                            }
                                            dcDef.getDcItems().get(i).setSpecCheckResult(BizConstant.SP_SPECCHECKRESULT_1X_UPPERSCREENLIMIT);
                                        } else if (CimStringUtils.equals(dcDef.getDcItems().get(i).getSpecCheckResult(), BizConstant.SP_SPECCHECKRESULT_LOWERSCREENLIMIT)) {
                                            if (log.isDebugEnabled()) {
                                                log.debug("== Convert specCheckResult: {} -> {}",
                                                        BizConstant.SP_SPECCHECKRESULT_LOWERSCREENLIMIT,
                                                        BizConstant.SP_SPECCHECKRESULT_1X_LOWERSCREENLIMIT);
                                            }
                                            dcDef.getDcItems().get(i).setSpecCheckResult(BizConstant.SP_SPECCHECKRESULT_1X_LOWERSCREENLIMIT);
                                        } else if (CimStringUtils.equals(dcDef.getDcItems().get(i).getSpecCheckResult(), BizConstant.SP_SPECCHECKRESULT_1POUND)) {
                                            if (log.isDebugEnabled()) {
                                                log.debug("== Convert specCheckResult: {} -> {}",
                                                        BizConstant.SP_SPECCHECKRESULT_1POUND,
                                                        BizConstant.SP_SPECCHECKRESULT_1X_POUND);
                                            }
                                            dcDef.getDcItems().get(i).setSpecCheckResult(BizConstant.SP_SPECCHECKRESULT_1X_POUND);
                                        } else if (CimStringUtils.equals(dcDef.getDcItems().get(i).getSpecCheckResult(), BizConstant.SP_SPECCHECKRESULT_1ASTERISK)) {
                                            if (log.isDebugEnabled()) {
                                                log.debug("== Convert specCheckResult: {} -> {}",
                                                        BizConstant.SP_SPECCHECKRESULT_1ASTERISK,
                                                        BizConstant.SP_SPECCHECKRESULT_1X_ASTERISK);
                                            }
                                            dcDef.getDcItems().get(i).setSpecCheckResult(BizConstant.SP_SPECCHECKRESULT_1X_ASTERISK);
                                        } else {
                                            // do Nothing
                                            log.debug("Do nothing");
                                        }
                                    }
                                }
                            }
                            dataCollectionInfos.add(dataCollectionInfo);
                            dataCollectionInfo.setDataCollectionDefinitionID(dcDef.getDataCollectionDefinitionID());
                            dataCollectionInfo.setDescription(dcDef.getDescription());
                            dataCollectionInfo.setDataCollectionType(dcDef.getDataCollectionType());

                            dataCollectionInfo.setDcItems(dcItems);
                            for (int i = 0; i < dcItemslen; i++) {
                                ProcessDTO.DataCollectionItemInfo dataCollectionItemInfo = new ProcessDTO.DataCollectionItemInfo();
                                dcItems.add(dataCollectionItemInfo);

                                dataCollectionItemInfo.setDataCollectionItemName(dcDef.getDcItems().get(i).getDataCollectionItemName());
                                dataCollectionItemInfo.setDataCollectionMode(dcDef.getDcItems().get(i).getDataCollectionMode());
                                dataCollectionItemInfo.setDataCollectionUnit(dcDef.getDcItems().get(i).getDataCollectionUnit());
                                dataCollectionItemInfo.setDataType(dcDef.getDcItems().get(i).getDataType());
                                dataCollectionItemInfo.setItemType(dcDef.getDcItems().get(i).getItemType());
                                dataCollectionItemInfo.setMeasurementType(dcDef.getDcItems().get(i).getMeasurementType());
                                dataCollectionItemInfo.setWaferID(dcDef.getDcItems().get(i).getWaferID());
                                dataCollectionItemInfo.setWaferPosition(dcDef.getDcItems().get(i).getWaferPosition());
                                dataCollectionItemInfo.setSitePosition(dcDef.getDcItems().get(i).getSitePosition());
                                dataCollectionItemInfo.setHistoryRequiredFlag(dcDef.getDcItems().get(i).getHistoryRequiredFlag());
                                dataCollectionItemInfo.setCalculationType(dcDef.getDcItems().get(i).getCalculationType());
                                dataCollectionItemInfo.setCalculationExpression(dcDef.getDcItems().get(i).getCalculationExpression());
                                dataCollectionItemInfo.setDataValue(dcDef.getDcItems().get(i).getDataValue());
                                dataCollectionItemInfo.setTargetValue(dcDef.getDcItems().get(i).getTargetValue());
                                dataCollectionItemInfo.setSpecCheckResult(dcDef.getDcItems().get(i).getSpecCheckResult());

                                dataCollectionItemInfo.setActionCodes(dcDef.getDcItems().get(i).getActionCodes());
                                dataCollectionItemInfo.setWaferCount(dcDef.getDcItems().get(i).getWaferCount());
                                dataCollectionItemInfo.setSiteCount(dcDef.getDcItems().get(i).getSiteCount());
                            }
                            dataCollectionInfo.setCalculationRequiredFlag(dcDef.getCalculationRequiredFlag());
                            dataCollectionInfo.setSpecCheckRequiredFlag(dcDef.getSpecCheckRequiredFlag());
                            dataCollectionInfo.setDataCollectionSpecificationID(dcDef.getDataCollectionSpecificationID());
                            dataCollectionInfo.setDcSpecDescription(dcDef.getDcSpecDescription());

                            if (CimBooleanUtils.isTrue(dataCollectionExistFlag)) {
                                int dcInfoLen = CimArrayUtils.getSize(dcDataCollectionInfoSeq);
                                for (int k = 0; k < dcInfoLen; k++) {
                                    if (CimObjectUtils.equalsWithValue(dataCollectionInfo.getDataCollectionSpecificationID(),
                                            dcDataCollectionInfoSeq.get(k).getDataCollectionSpecificationID())) {

                                        int dcSpecsLen = CimArrayUtils.getSize(dcDataCollectionInfoSeq.get(k).getDcSpecs());
                                        dataCollectionInfo.setDcSpecs(dcDataCollectionInfoSeq.get(k).getDcSpecs());

                                        for (int j = 0; j < dcSpecsLen; j++) {
                                            for (int i = 0; i < dcItemslen; i++) {
                                                if (CimStringUtils.equals(dcDef.getDcItems().get(i).getDataCollectionItemName(),
                                                        dcDataCollectionInfoSeq.get(k).getDcSpecs().get(j).getDataItemName())) {
                                                    String dataType = dataCollectionInfo.getDcItems().get(i).getDataType();
                                                    Double targetValue = dcDataCollectionInfoSeq.get(k).getDcSpecs().get(j).getTarget();
                                                    String tmpTarget = "0";
                                                    if (CimStringUtils.equals(dataType, BizConstant.SP_DCDEF_VAL_INTEGER)) {
                                                        tmpTarget = String.format("%s", targetValue.intValue());
                                                    } else if (CimStringUtils.equals(dataType, BizConstant.SP_DCDEF_VAL_FLOAT)) {
                                                        tmpTarget = String.format("%.1f", targetValue);
                                                    }
                                                    log.info("Target value is updated. ItemName: {} ,TargetValue: {}",
                                                            dcDataCollectionInfoSeq.get(k).getDcSpecs().get(j).getDataItemName(),
                                                            tmpTarget);
                                                    dataCollectionInfo.getDcItems().get(i).setTargetValue(tmpTarget);
                                                    dcDef.getDcItems().get(i).setTargetValue(tmpTarget);
                                                }
                                            }
                                        }
                                        break;
                                    }
                                }
                            } else {
                                int dcSpecsLen = dcDef.getDcSpecs().size();
                                dataCollectionInfo.setDcSpecs(dcSpecs);
                                for (int j = 0; j < dcSpecsLen; j++) {
                                    ProcessDTO.DataCollectionSpecInfo dataCollectionSpecInfo = new ProcessDTO.DataCollectionSpecInfo();
                                    if (CimArrayUtils.getSize(dcSpecs) > j) {
                                        dataCollectionSpecInfo = dcSpecs.get(j);
                                    } else {
                                        dcSpecs.add(dataCollectionSpecInfo);
                                    }
                                    dataCollectionSpecInfo.setDataItemName(dcDef.getDcSpecs().get(j).getDataItemName());
                                    dataCollectionSpecInfo.setScreenLimitUpperRequired(dcDef.getDcSpecs().get(j).getScreenLimitUpperRequired());
                                    dataCollectionSpecInfo.setScreenLimitUpper(dcDef.getDcSpecs().get(j).getScreenLimitUpper());
                                    dataCollectionSpecInfo.setActionCodesUscrn(dcDef.getDcSpecs().get(j).getActionCodesUscrn());
                                    dataCollectionSpecInfo.setScreenLimitLowerRequired(dcDef.getDcSpecs().get(j).getScreenLimitLowerRequired());
                                    dataCollectionSpecInfo.setScreenLimitLower(dcDef.getDcSpecs().get(j).getScreenLimitLower());
                                    dataCollectionSpecInfo.setActionCodesLscrn(dcDef.getDcSpecs().get(j).getActionCodesLscrn());
                                    dataCollectionSpecInfo.setSpecLimitUpperRequired(dcDef.getDcSpecs().get(j).getSpecLimitUpperRequired());
                                    dataCollectionSpecInfo.setSpecLimitUpper(dcDef.getDcSpecs().get(j).getSpecLimitUpper());
                                    dataCollectionSpecInfo.setActionCodesUsl(dcDef.getDcSpecs().get(j).getActionCodesUsl());
                                    dataCollectionSpecInfo.setSpecLimitLowerRequired(dcDef.getDcSpecs().get(j).getSpecLimitLowerRequired());
                                    dataCollectionSpecInfo.setSpecLimitLower(dcDef.getDcSpecs().get(j).getSpecLimitLower());
                                    dataCollectionSpecInfo.setActionCodesLsl(dcDef.getDcSpecs().get(j).getActionCodesLsl());
                                    dataCollectionSpecInfo.setControlLimitUpperRequired(dcDef.getDcSpecs().get(j).getControlLimitUpperRequired());
                                    dataCollectionSpecInfo.setControlLimitUpper(dcDef.getDcSpecs().get(j).getControlLimitUpper());
                                    dataCollectionSpecInfo.setActionCodesUcl(dcDef.getDcSpecs().get(j).getActionCodesUcl());
                                    dataCollectionSpecInfo.setControlLimitLowerRequired(dcDef.getDcSpecs().get(j).getControlLimitLowerRequired());
                                    dataCollectionSpecInfo.setControlLimitLower(dcDef.getDcSpecs().get(j).getControlLimitLower());
                                    dataCollectionSpecInfo.setActionCodesLcl(dcDef.getDcSpecs().get(j).getActionCodesLcl());
                                    dataCollectionSpecInfo.setTarget(dcDef.getDcSpecs().get(j).getTarget());
                                    dataCollectionSpecInfo.setTag(dcDef.getDcSpecs().get(j).getTag());
                                    dataCollectionSpecInfo.setDcSpecGroup("");

                                    for (int i = 0; i < dcItemslen; i++) {
                                        if (CimStringUtils.equals(dcDef.getDcItems().get(i).getDataCollectionItemName(),
                                                dcDef.getDcSpecs().get(j).getDataItemName())) {
                                            log.info("Target value is updated. ItemName: {}", dcDef.getDcSpecs().get(j).getDataItemName());
                                            //String tmpTarget = String.format("%.1f", dcDef.getDcSpecs().get(j).getTarget());
                                            String dataType = dataCollectionInfo.getDcItems().get(i).getDataType();
                                            Double targetValue = dcDef.getDcSpecs().get(j).getTarget();
                                            String tmpTarget = "0";
                                            if (CimStringUtils.equals(dataType, BizConstant.SP_DCDEF_VAL_INTEGER)) {
                                                tmpTarget = String.format("%s", targetValue.intValue());
                                            } else if (CimStringUtils.equals(dataType, BizConstant.SP_DCDEF_VAL_FLOAT)) {
                                                tmpTarget = String.format("%.1f", targetValue);
                                            }
                                            dataCollectionInfo.getDcItems().get(i).setTargetValue(tmpTarget);
                                            dcDef.getDcItems().get(i).setTargetValue(tmpTarget);
                                        }
                                    }
                                }
                            }
                            dataCollectionInfo.setPreviousDataCollectionDefinitionID(dcDef.getPreviousDataCollectionDefinitionID());
                            dataCollectionInfo.setPreviousOperationID(dcDef.getPreviousOperationID());
                            dataCollectionInfo.setPreviousOperationNumber(dcDef.getPreviousOperationNumber());
                            dataCollectionInfo.setEdcSettingType(dcDef.getEdcSettingType());
                            lotInCassette.getStartRecipe().getDcDefList().set(m, dcDef);
                        }
                        // todo:zqi 是否需要更改为只更新 items 信息，后续性能测试看结果后在优化
                        processOperation.setDataCollectionInfo(dataCollectionInfos);
                    }
                }
            }
        }

        return retVal;
    }


    @Override
    public Results.SpecCheckReqResult specCheckFillInDR(Infos.ObjCommon objCommon,
                                                        ObjectIdentifier equipmentID,
                                                        ObjectIdentifier controlJobID,
                                                        List<Infos.StartCassette> startCassetteList) {
        Results.SpecCheckReqResult retVal = new Results.SpecCheckReqResult();
        retVal.setControlJobID(controlJobID);
        retVal.setEquipmentID(equipmentID);
        retVal.setStartCassetteList(startCassetteList);
        //-------------------------------------
        // If startCassette is empty, return
        //-------------------------------------
        if (CimArrayUtils.isEmpty(startCassetteList)) {
            return retVal;
        }

        boolean allCalculationAndSpecCheckFalseFlag = true;

        // Set 1 for not to use data over screen limits for Delta/Derived calculation.
        // The default value is 0.
        int notCalculateFlag = StandardProperties.OM_EDC_SCREEN_LIMIT_OVER_NO_CALC.getIntValue();

        //------------------------------------------------------
        // Check DCInfo is need to do SpecChek or Calculation
        //------------------------------------------------------
        for (Infos.StartCassette startCassette : startCassetteList) {
            for (Infos.LotInCassette lotInCassette : startCassette.getLotInCassetteList()) {
                //-----------------------------
                // Omit is not MoveIn Lot
                //-----------------------------
                if (CimBooleanUtils.isFalse(lotInCassette.getMoveInFlag())) {
                    continue;
                }
                List<Infos.DataCollectionInfo> dcDefList = lotInCassette.getStartRecipe().getDcDefList();
                for (Infos.DataCollectionInfo dcDef : dcDefList) {
                    if (CimBooleanUtils.isTrue(dcDef.getCalculationRequiredFlag())
                            || CimBooleanUtils.isTrue(dcDef.getSpecCheckRequiredFlag())) {
                        allCalculationAndSpecCheckFalseFlag = false;
                        break;
                    }
                }
                if (CimBooleanUtils.isFalse(allCalculationAndSpecCheckFalseFlag)) {
                    break;
                }
            }
            if (CimBooleanUtils.isFalse(allCalculationAndSpecCheckFalseFlag)) {
                break;
            }
        }

        if (CimBooleanUtils.isTrue(allCalculationAndSpecCheckFalseFlag)) {
            return retVal;
        }

        //------------------------------------//
        // Main Process                       //
        //------------------------------------//
        CimProcessOperation processOperation;
        for (Infos.StartCassette startCassette : startCassetteList) {
            List<Infos.LotInCassette> lotInCassetteList = startCassette.getLotInCassetteList();
            if (CimArrayUtils.isNotEmpty(lotInCassetteList)) {
                // Lots Loop in Cassette
                for (Infos.LotInCassette lotInCassette : lotInCassetteList) {
                    //-----------------------------
                    // Omit is not MoveIn Lot
                    //-----------------------------
                    if (CimBooleanUtils.isFalse(lotInCassette.getMoveInFlag())) {
                        continue;
                    }
                    /*--------------------*/
                    /*   Get lot Object   */
                    /*--------------------*/
                    CimLot lot = baseCoreFactory.getBO(CimLot.class, lotInCassette.getLotID());

                    //------------------------------------------
                    // Current PO or Previous PO ?
                    //------------------------------------------
                    //step1 - lotCheckConditionForPO
                    Boolean objLotCheckConditionForPO = lotMethod.lotCheckConditionForPO(objCommon, lotInCassette.getLotID());
                    if (CimBooleanUtils.isTrue(objLotCheckConditionForPO)) {
                        //--------------------------------------------------------------------------
                        // Get PO from Current Operation.
                        //--------------------------------------------------------------------------
                        processOperation = lot.getProcessOperation();
                    } else {
                        //--------------------------------------------------------------------------
                        // Get PO from Previous Operation.
                        //--------------------------------------------------------------------------
                        processOperation = lot.getPreviousProcessOperation();
                    }
                    Validations.check(processOperation == null, retCodeConfig.getNotFoundProcessOperation());

                    List<ProcessDTO.DataCollectionInfo> dcDataCollectionInfoSeqFormDB = null;
                    boolean dataCollectionExistFlag = false;


                    /*-------------------------------------------------------------------------------------------*/
                    // If “0”, OMS supports single corresponding operation.
                    // If “1” is specified, OMS supports multiple corresponding operations for Spec/SPC check.
                    // The corresponding operation is specified by DC Spec Group which categorizes DC Spec.
                    /*-------------------------------------------------------------------------------------------*/
                    int corresPondingOpeMode = StandardProperties.OM_EDC_MULTI_CORRESPOND_FLAG.getIntValue();
                    if (corresPondingOpeMode == 1) {
                        ProcessDTO.ActualStartInformationForPO actualStartInfo = processOperation.getActualStartInfo(true);
                        if (CimBooleanUtils.isTrue(actualStartInfo.getAssignedDataCollectionFlag())) {
                            dcDataCollectionInfoSeqFormDB = actualStartInfo.getAssignedDataCollections();
                            dataCollectionExistFlag = true;
                        }
                    }
                    if (notCalculateFlag != 0) {
                        if (log.isDebugEnabled()) {
                            log.debug("OM_EDC_SCREEN_LIMIT_OVER_NO_CALC != 0");
                        }
                        Infos.DataCollectionInfo strDCDef = lotInCassette.getStartRecipe().getDcDefList().get(0);
                        if (CimBooleanUtils.isTrue(strDCDef.getSpecCheckRequiredFlag())) {
                            //-----------------------------------------------------------------------------------------
                            // [1] Set Input Value for doValueConversion()
                            //-----------------------------------------------------------------------------------------
                            if (log.isDebugEnabled()) {
                                log.debug("[1] Set Input Value for doValueConversion()");
                            }
                            List<EDCDTO.DCItemData> strRawDataDCItemData = new ArrayList<>();
                            strDCDef.getDcItems().forEach(dcItem -> {
                                EDCDTO.DCItemData dcItemData = new EDCDTO.DCItemData();
                                dcItemData.setDataItemName(dcItem.getDataCollectionItemName());
                                dcItemData.setWaferPosition(dcItem.getWaferPosition());
                                dcItemData.setSitePosition(dcItem.getSitePosition());
                                dcItemData.setValType(dcItem.getDataType());
                                dcItemData.setItemType(dcItem.getItemType());
                                dcItemData.setCalculationExpression(dcItem.getCalculationExpression());
                                dcItemData.setCalculationType(dcItem.getCalculationType());
                                dcItemData.setInputValue(dcItem.getDataValue());
                                dcItemData.setSpecCheckResult("");
                                dcItemData.setWaferID(dcItem.getWaferID().getValue());
                                dcItemData.setMeasurementType(dcItem.getMeasurementType());
                                dcItemData.setWaferCount(dcItem.getWaferCount());
                                dcItemData.setSiteCount(dcItem.getSiteCount());
                                dcItemData.setSeqNo(dcItem.getSeqNo());

                                strRawDataDCItemData.add(dcItemData);
                            });

                            //-----------------------------------------------------------------------------------------
                            // [2] Convert value
                            //-----------------------------------------------------------------------------------------
                            if (log.isDebugEnabled()) {
                                log.debug("[2] Convert value, call doValueConversion(...)");
                            }
                            CimDataCollectionDefinition aDataCollectionDefinition =
                                    baseCoreFactory.getBO(CimDataCollectionDefinition.class, strDCDef.getDataCollectionDefinitionID());
                            Validations.check(aDataCollectionDefinition == null, retCodeConfig.getNotFoundDcdef());
                            aDataCollectionDefinition.doValueConversion(strRawDataDCItemData);

                            //-----------------------------------------------------------------------------------------
                            // [3] Spec Check
                            //-----------------------------------------------------------------------------------------
                            if (log.isDebugEnabled()) {
                                log.debug("[3] Spec Check, call doSpecCheck(...)");
                            }
                            processOperation.doSpecCheck(strRawDataDCItemData, strDCDef.getDataCollectionSpecificationID().getValue());

                            //-----------------------------------------------------------------------------------------
                            // [4] Set data done spec check
                            //-----------------------------------------------------------------------------------------
                            if (log.isDebugEnabled()) {
                                log.debug("[4] Set data done spec check");
                            }
                            strDCDef.setDcItems(new ArrayList<>());
                            int index = 0;
                            for (EDCDTO.DCItemData dcItemData : strRawDataDCItemData) {
                                strDCDef.getDcItems().add(new Infos.DataCollectionItemInfo());
                                Infos.DataCollectionItemInfo dataCollectionItemInfo = strDCDef.getDcItems().get(index++);

                                dataCollectionItemInfo.setDataCollectionItemName(dcItemData.getDataItemName());
                                dataCollectionItemInfo.setWaferPosition(dcItemData.getWaferPosition());
                                dataCollectionItemInfo.setSitePosition(dcItemData.getSitePosition());
                                dataCollectionItemInfo.setDataType(dcItemData.getValType());
                                dataCollectionItemInfo.setItemType(dcItemData.getItemType());
                                dataCollectionItemInfo.setCalculationType(dcItemData.getCalculationType());
                                dataCollectionItemInfo.setCalculationExpression(dcItemData.getCalculationExpression());
                                dataCollectionItemInfo.setDataValue(dcItemData.getInputValue());
                                dataCollectionItemInfo.setSpecCheckResult(dcItemData.getSpecCheckResult());
                                dataCollectionItemInfo.setWaferID(ObjectIdentifier.buildWithValue(dcItemData.getWaferID()));
                                dataCollectionItemInfo.setMeasurementType(dcItemData.getMeasurementType());

                                dataCollectionItemInfo.setWaferCount(dcItemData.getWaferCount());
                                dataCollectionItemInfo.setSiteCount(dcItemData.getSiteCount());
                                dataCollectionItemInfo.setSeqNo(dcItemData.getSeqNo());
                            }
                        }
                    }

                    //--------------------------------------------------------------------------------------------
                    // Delta DC Defs
                    //--------------------------------------------------------------------------------------------
                    List<ProcessDTO.DeltaDcDefInfo> deltaDCDefs = processOperation.getAssignedDeltaDCDefs();

                    int dataCollectionLen = CimArrayUtils.getSize(lotInCassette.getStartRecipe().getDcDefList());
                    List<ProcessDTO.DataCollectionInfo> dataCollectionInfos = new ArrayList<>();

                    //--- Keep raw data item
                    List<EDCDTO.DCItemData> strRawDCItemData = null;

                    for (int dcLoopIndex = 0; dcLoopIndex < dataCollectionLen; dcLoopIndex++) {
                        Infos.DataCollectionInfo dcDef = lotInCassette.getStartRecipe().getDcDefList().get(dcLoopIndex);
                        int dcItemslen = dcDef.getDcItems().size();
                        if (log.isDebugEnabled()) {
                            log.debug("EDC item count : {}.", dcItemslen);
                        }

                        CimProcessOperation aDeltaProcessOperation = null;

                        //-----------------------------------------------------------------------------------------
                        // [1] Get Delta Process Operation if Delta Calculation exists
                        //-----------------------------------------------------------------------------------------
                        if (dcLoopIndex != 0) {
                            //-----------------------------------------------------------------------------------------
                            // [1] Get Delta Process Operation if Delta Calculation exists
                            //-----------------------------------------------------------------------------------------
                            if (log.isDebugEnabled()) {
                                log.debug("dcLoopIndex == 0");
                                log.debug("[1] Get Delta Process Operation if Delta Calculation exists");
                            }

                            List<Infos.DataCollectionItemInfo> dataCollectionItemInfos = dcDef.getDcItems();
                            for (Infos.DataCollectionItemInfo itemInfo : dataCollectionItemInfos) {
                                if (CimStringUtils.equals(itemInfo.getCalculationType(), BizConstant.SP_DCDEF_CALC_DELTA)) {
                                    //-----------------------------------------------------------------------------------------
                                    // Get ProcessFlowContext from lot
                                    //-----------------------------------------------------------------------------------------
                                    CimProcessFlowContext processFlowContext = lot.getProcessFlowContext();
                                    Validations.check(processFlowContext == null, retCodeConfig.getNotFoundPfx(), "***");

                                    //-----------------------------------------------------------------------------------------
                                    // Get RouteID from Process Operation
                                    //-----------------------------------------------------------------------------------------
                                    CimProcessDefinition mainProcessDefinition = processOperation.getMainProcessDefinition();
                                    Validations.check(mainProcessDefinition == null, retCodeConfig.getNotFoundProcessDefinition(), "***");

                                    assert mainProcessDefinition != null;
                                    String routeID = mainProcessDefinition.getIdentifier();
                                    //-----------------------------------------------------------------------------------------
                                    // Get ProcesOperation from pfx by RouteID and Operation Number
                                    //-----------------------------------------------------------------------------------------
                                    assert processFlowContext != null;
                                    aDeltaProcessOperation = processFlowContext.findProcessOperationForRouteOperationNumberBefore(routeID,
                                            deltaDCDefs.get(dcLoopIndex).getProcessOperation());
                                    Validations.check(aDeltaProcessOperation == null, retCodeConfig.getNotFoundDcspec());
                                    break;
                                }
                            }
                        }

                        //-----------------------------------------------------------------------------------------
                        // [2] Set Input Value for doValueConversion()
                        //-----------------------------------------------------------------------------------------
                        if (log.isDebugEnabled()) {
                            log.debug("[2] Set Input Value for doValueConversion()");
                        }

                        // zqi: 此处改为并发转换数据可能会导致集合顺序发生变化，是否对后续测试有影响？
                        List<EDCDTO.DCItemData> strDCItemData = dcDef.getDcItems().parallelStream().map(dcItem -> {
                            EDCDTO.DCItemData dcItemData = new EDCDTO.DCItemData();

                            dcItemData.setDataItemName(dcItem.getDataCollectionItemName());
                            dcItemData.setItemType(dcItem.getItemType());
                            dcItemData.setWaferPosition(dcItem.getWaferPosition());
                            dcItemData.setSitePosition(dcItem.getSitePosition());
                            dcItemData.setValType(dcItem.getDataType());
                            dcItemData.setCalculationType(dcItem.getCalculationType());
                            dcItemData.setCalculationExpression(dcItem.getCalculationExpression());
                            dcItemData.setInputValue(dcItem.getDataValue());
                            dcItemData.setMeasurementType(dcItem.getMeasurementType());

                            dcItemData.setWaferCount(dcItem.getWaferCount());
                            dcItemData.setSiteCount(dcItem.getSiteCount());
                            dcItemData.setSeqNo(dcItem.getSeqNo());

                            // MeasurementType() = Process Job / Proc Wafer / Proc Site
                            if (CimStringUtils.equals(BizConstant.SP_DCDEF_MEAS_PJ, dcItem.getMeasurementType())
                                    || CimStringUtils.equals(BizConstant.SP_DCDEF_MEAS_PJWAFER, dcItem.getMeasurementType())
                                    || CimStringUtils.equals(BizConstant.SP_DCDEF_MEAS_PJWAFERSITE, dcItem.getMeasurementType())) {
                                dcItemData.setSpecCheckResult(dcItem.getSpecCheckResult());
                            } else {
                                dcItemData.setSpecCheckResult("");
                            }

                            // ItemType = Raw
                            if (notCalculateFlag != 0) {
                                if (CimStringUtils.equals(dcItem.getItemType(), BizConstant.SP_DCDEF_ITEM_RAW)) {
                                    if (CimStringUtils.equals(dcItem.getSpecCheckResult(), BizConstant.SP_SPECCHECKRESULT_UPPERSCREENLIMIT)
                                            || CimStringUtils.equals(dcItem.getSpecCheckResult(), BizConstant.SP_SPECCHECKRESULT_LOWERSCREENLIMIT)) {
                                        dcItemData.setSpecCheckResult(dcItem.getSpecCheckResult());
                                    }
                                }
                            }

                            // ItemType = Derived
                            if (CimStringUtils.equals(dcItem.getItemType(), BizConstant.SP_DCDEF_ITEM_DERIVED)) {
                                if (CimStringUtils.unEqual(dcItem.getMeasurementType(), BizConstant.SP_DCDEF_MEAS_PJWAFER)
                                        && CimStringUtils.unEqual(dcItem.getMeasurementType(), BizConstant.SP_DCDEF_MEAS_PJWAFERSITE)
                                        && CimStringUtils.unEqual(dcItem.getMeasurementType(), BizConstant.SP_DCDEF_MEAS_WAFER)
                                        && CimStringUtils.unEqual(dcItem.getMeasurementType(), BizConstant.SP_DCDEF_MEAS_SITE)) {
                                    dcItem.setWaferID(ObjectIdentifier.emptyIdentifier());
                                }
                            }

                            dcItemData.setWaferID(ObjectIdentifier.fetchValue(dcItem.getWaferID()));
                            return dcItemData;
                        }).collect(Collectors.toList());

                        // zqi: 耗时操作
                        if (dcLoopIndex == 0) {
                            strRawDCItemData = new ArrayList<>();
                            for (EDCDTO.DCItemData itemData : strDCItemData) {
                                strRawDCItemData.add(itemData.copy());
                            }
                        }

                        //-----------------------------------------------------------------------------------------
                        // [3] Convert & Calculate value
                        //-----------------------------------------------------------------------------------------
                        if (log.isDebugEnabled()) {
                            log.debug("[3] Convert & Calculate value");
                        }
                        CimDataCollectionDefinition aDataCollectionDefinition =
                                baseCoreFactory.getBO(CimDataCollectionDefinition.class, dcDef.getDataCollectionDefinitionID());
                        Validations.check(aDataCollectionDefinition == null, retCodeConfig.getNotFoundDcdef());

                        // doValueConversion
                        aDataCollectionDefinition.doValueConversion(strDCItemData);

                        /*----------------------------------------------------------------------------------------*/
                        // author: zqi                                                                            */
                        // date: 2021/7/7 20:00:00                                                                */
                        // 性能优化,将下面逻辑移入到 CimDataCollectionDefinitionBO#doValueConversion 方法中           */
                        /*----------------------------------------------------------------------------------------*/
                        //-------------------------------------------------------------------
                        // if DCItemType = User Func, set SpecCheckResult to "#"
                        //-------------------------------------------------------------------
                        /*strDCItemData.stream().parallel().forEach(dcItemData -> {
                            if (CimStringUtils.equals(dcItemData.getItemType(), BizConstant.SP_DCDEF_ITEM_USERFUNCTION)) {
                                dcItemData.setSpecCheckResult("#");
                            }
                        });*/

                        // doCalculation
                        aDataCollectionDefinition.doCalculation(strDCItemData,
                                aDeltaProcessOperation,
                                strRawDCItemData,
                                processOperation.getPrimaryKey());


                        //--- Keep raw data item after calculation of derived
                        // zqi: 耗时操作
                        if (dcLoopIndex == 0) {
                            strRawDCItemData = new ArrayList<>();
                            for (EDCDTO.DCItemData itemData : strDCItemData) {
                                strRawDCItemData.add(itemData.copy());
                            }
                        }

                        //----------------------------------------------------------------------------------
                        // Set derived value to return value
                        // 下面代码逻辑只会影响 EDC_MODE = Derived 的数据，性能消耗不大
                        //----------------------------------------------------------------------------------
                        for (int i = 0; i < dcItemslen; i++) {
                            final String valType = strDCItemData.get(i).getValType();
                            final String specCheckResult = strDCItemData.get(i).getSpecCheckResult();
                            final String dataMode = dcDef.getDcItems().get(i).getDataCollectionMode();

                            if (CimStringUtils.unEqual(specCheckResult, "*")
                                    && CimStringUtils.unEqual(specCheckResult, "#")
                                    && CimStringUtils.equals(dataMode, BizConstant.SP_DCDEF_ITEM_DERIVED)) {
                                // Number Value
                                final Double numValue = strDCItemData.get(i).getNumValue();

                                String tmpStr = "";
                                if (CimStringUtils.equals(valType, BizConstant.SP_DCDEF_VAL_FLOAT)) {
                                    if (-1.0 < numValue && numValue < 1.0) {
                                        tmpStr = String.format("%.9G", numValue);
                                        if (tmpStr.length() > 12) {
                                            tmpStr = String.format("%G", numValue);
                                        }
                                    } else if (-100000000000.0 < numValue && numValue < 1000000000000.0) {
                                        tmpStr = String.format("%.1f", numValue);
                                        if (tmpStr.length() > 12) {
                                            tmpStr = tmpStr.substring(0, 12);
                                        }
                                    } else {
                                        tmpStr = String.format("%G", numValue);
                                    }
                                }
                                if (CimStringUtils.equals(valType, BizConstant.SP_DCDEF_VAL_INTEGER)) {
                                    if (numValue == Integer.MIN_VALUE || numValue == Integer.MAX_VALUE) {
                                        tmpStr = String.format("%s", "*");
                                    } else {
                                        tmpStr = String.format("%s", CimNumberUtils.intValue(numValue));
                                    }
                                }
                                if (tmpStr.length() > 12 || CimStringUtils.equals(tmpStr, "*")) {
                                    dcDef.getDcItems().get(i).setDataValue("*");
                                    dcDef.getDcItems().get(i).setSpecCheckResult("*");
                                } else {
                                    dcDef.getDcItems().get(i).setDataValue(tmpStr);
                                }
                            }

                            if (CimStringUtils.equals(specCheckResult, "#")
                                    && CimStringUtils.equals(dataMode, BizConstant.SP_DCDEF_ITEM_DERIVED)) {
                                dcDef.getDcItems().get(i).setDataValue("*");
                            }
                        }

                        //-----------------------------------------------------------------------------------------
                        //  SpecCheck is required only when the DCSpec exists.
                        //-----------------------------------------------------------------------------------------
                        if (CimBooleanUtils.isTrue(dcDef.getSpecCheckRequiredFlag())) {
                            if (log.isDebugEnabled()) {
                                log.debug("SpecCheckRequiredFlag : {}", dcDef.getSpecCheckRequiredFlag());
                            }

                            //-----------------------------------------------------------------------------------------
                            // [4] Spec Check
                            //-----------------------------------------------------------------------------------------
                            if (log.isDebugEnabled()) {
                                log.debug("[4] Spec Check, call doSpecCheck(...)");
                            }
                            processOperation.doSpecCheck(strDCItemData, dcDef.getDataCollectionSpecificationID().getValue());

                            if (notCalculateFlag != 0) {
                                if (log.isDebugEnabled()) {
                                    log.debug("OM_EDC_SCREEN_LIMIT_OVER_NO_CALC != 0");
                                }
                                /*strDCItemData.stream().parallel().forEach(dcItemData -> {
                                    if (CimStringUtils.equals(dcItemData.getItemType(), BizConstant.SP_DCDEF_ITEM_USERFUNCTION)) {
                                        dcItemData.setSpecCheckResult("#");
                                    }
                                });*/

                                // doCalculation
                                aDataCollectionDefinition.doCalculation(strDCItemData,
                                        aDeltaProcessOperation,
                                        strRawDCItemData,
                                        processOperation.getPrimaryKey());

                                //--- Keep raw data item after calcuration of derived
                                if (dcLoopIndex == 0) {
                                    strRawDCItemData = new ArrayList<>();
                                    for (EDCDTO.DCItemData itemData : strDCItemData) {
                                        strRawDCItemData.add(itemData.copy());
                                    }
                                }
                                processOperation.doSpecCheck(strDCItemData, dcDef.getDataCollectionSpecificationID().getValue());
                            }

                            //-----------------------------------------------------------------------------------------
                            // [5] Set check result to output data
                            //-----------------------------------------------------------------------------------------
                            if (log.isDebugEnabled()) {
                                log.debug("[5] Set check result to output data");
                            }
                            for (int i = 0; i < dcItemslen; i++) {
                                final String specCheckResult = strDCItemData.get(i).getSpecCheckResult();
                                Infos.DataCollectionItemInfo returnItemInfo = dcDef.getDcItems().get(i);

                                // CheckResult
                                returnItemInfo.setSpecCheckResult(specCheckResult);

                                /*----------------------------------------*/
                                // author: zqi                            */
                                // date: 2021/7/7 20:00:00                */
                                // 性能优化添加 setTargetValue 值           */
                                /*----------------------------------------*/
                                returnItemInfo.setTargetValue(strDCItemData.get(i).getTargetValue());

                                // Action Code
                                final List<String> actionCodes = strDCItemData.get(i).getActionCodes();
                                if (CimArrayUtils.isNotEmpty(actionCodes)) {
                                    returnItemInfo.setActionCodes(CimArrayUtils.toString(actionCodes, ","));
                                }

                                String tmpStr = strDCItemData.get(i).getInputValue();
                                // specCheckResult == *
                                if (CimStringUtils.equals(specCheckResult, "*")) {
                                    returnItemInfo.setDataValue(strDCItemData.get(i).getInputValue());
                                }
                                // specCheckResult == #
                                else if (CimStringUtils.equals(specCheckResult, "#")) {
                                    returnItemInfo.setDataValue("*");
                                    returnItemInfo.setSpecCheckResult("*");
                                }
                                // others
                                else {
                                    final String valType = strDCItemData.get(i).getValType();
                                    final Double numValue = strDCItemData.get(i).getNumValue();

                                    // ValueType = Float
                                    if (CimStringUtils.equals(valType, BizConstant.SP_DCDEF_VAL_FLOAT)) {
                                        if (null != numValue){
                                            if (-1.0 < numValue && numValue < 1.0) {
                                                tmpStr = String.format("%.9G", numValue);
                                                if (tmpStr.length() > 12) {
                                                    tmpStr = String.format("%G", numValue);
                                                }
                                            } else if (-100000000000.0 < numValue && numValue < 1000000000000.0) {
                                                tmpStr = String.format("%.1f", numValue);
                                                if (tmpStr.length() > 12) {
                                                    tmpStr = tmpStr.substring(0, 12);
                                                }
                                            } else {
                                                tmpStr = String.format("%G", numValue);
                                            }
                                        }
                                    }
                                    // ValueType = Integer
                                    else if (CimStringUtils.equals(valType, BizConstant.SP_DCDEF_VAL_INTEGER)) {
                                        if (null != numValue){
                                            if (numValue == Integer.MIN_VALUE || numValue == Integer.MAX_VALUE) {
                                                tmpStr = String.format("%s", "*");
                                            } else {
                                                tmpStr = String.format("%s", numValue.intValue());
                                            }
                                        }
                                    }
                                    if (CimStringUtils.length(tmpStr) > 12 || CimStringUtils.equals(tmpStr, "*")) {
                                        returnItemInfo.setDataValue("*");
                                        returnItemInfo.setSpecCheckResult("*");
                                    } else {
                                        returnItemInfo.setDataValue(tmpStr);
                                    }
                                }
                            }

                            // If Tx is specCheckReq, the SpecCheckResult need convert to 1x state.
                            if (TransactionIDEnum.DATA_SPEC_CHECK_REQ.equals(objCommon.getTransactionID())) {
                                if (log.isDebugEnabled()) {
                                    log.debug("Transaction ID = {}, so need to convert the check result.", objCommon.getTransactionID());
                                }
                                dcDef.getDcItems().parallelStream().forEach(itemInfo -> {
                                    // SpecCheckResult = 0
                                    if (CimStringUtils.equals(itemInfo.getSpecCheckResult(), BizConstant.SP_SPECCHECKRESULT_OK)) {
                                        if (log.isDebugEnabled()) {
                                            log.debug("== Convert specCheckResult: {} -> {}",
                                                    BizConstant.SP_SPECCHECKRESULT_OK,
                                                    BizConstant.SP_SPECCHECKRESULT_1X_OK);
                                        }
                                        itemInfo.setSpecCheckResult(BizConstant.SP_SPECCHECKRESULT_1X_OK);
                                    }
                                    // SpecCheckResult = 1
                                    else if (CimStringUtils.equals(itemInfo.getSpecCheckResult(), BizConstant.SP_SPECCHECKRESULT_UPPERCONTROLLIMIT)) {
                                        if (log.isDebugEnabled()) {
                                            log.debug("== Convert specCheckResult: {} -> {}",
                                                    BizConstant.SP_SPECCHECKRESULT_UPPERCONTROLLIMIT,
                                                    BizConstant.SP_SPECCHECKRESULT_1X_UPPERCONTROLLIMIT);
                                        }
                                        itemInfo.setSpecCheckResult(BizConstant.SP_SPECCHECKRESULT_1X_UPPERCONTROLLIMIT);
                                    }
                                    // SpecCheckResult = 2
                                    else if (CimStringUtils.equals(itemInfo.getSpecCheckResult(), BizConstant.SP_SPECCHECKRESULT_LOWERCONTROLLIMIT)) {
                                        if (log.isDebugEnabled()) {
                                            log.debug("== Convert specCheckResult: {} -> {}",
                                                    BizConstant.SP_SPECCHECKRESULT_LOWERCONTROLLIMIT,
                                                    BizConstant.SP_SPECCHECKRESULT_1X_LOWERCONTROLLIMIT);
                                        }
                                        itemInfo.setSpecCheckResult(BizConstant.SP_SPECCHECKRESULT_1X_LOWERCONTROLLIMIT);
                                    }
                                    // SpecCheckResult = 3
                                    else if (CimStringUtils.equals(itemInfo.getSpecCheckResult(), BizConstant.SP_SPECCHECKRESULT_UPPERSPECLIMIT)) {
                                        if (log.isDebugEnabled()) {
                                            log.debug("== Convert specCheckResult: {} -> {}",
                                                    BizConstant.SP_SPECCHECKRESULT_UPPERSPECLIMIT,
                                                    BizConstant.SP_SPECCHECKRESULT_1X_UPPERSPECLIMIT);
                                        }
                                        itemInfo.setSpecCheckResult(BizConstant.SP_SPECCHECKRESULT_1X_UPPERSPECLIMIT);
                                    }
                                    // SpecCheckResult = 4
                                    else if (CimStringUtils.equals(itemInfo.getSpecCheckResult(), BizConstant.SP_SPECCHECKRESULT_LOWERSPECLIMIT)) {
                                        if (log.isDebugEnabled()) {
                                            log.debug("== Convert specCheckResult: {} -> {}",
                                                    BizConstant.SP_SPECCHECKRESULT_LOWERSPECLIMIT,
                                                    BizConstant.SP_SPECCHECKRESULT_1X_LOWERSPECLIMIT);
                                        }
                                        itemInfo.setSpecCheckResult(BizConstant.SP_SPECCHECKRESULT_1X_LOWERSPECLIMIT);
                                    }
                                    // SpecCheckResult = 5
                                    else if (CimStringUtils.equals(itemInfo.getSpecCheckResult(), BizConstant.SP_SPECCHECKRESULT_UPPERSCREENLIMIT)) {
                                        if (log.isDebugEnabled()) {
                                            log.debug("== Convert specCheckResult: {} -> {}",
                                                    BizConstant.SP_SPECCHECKRESULT_UPPERSCREENLIMIT,
                                                    BizConstant.SP_SPECCHECKRESULT_1X_UPPERSCREENLIMIT);
                                        }
                                        itemInfo.setSpecCheckResult(BizConstant.SP_SPECCHECKRESULT_1X_UPPERSCREENLIMIT);
                                    }
                                    // SpecCheckResult = 6
                                    else if (CimStringUtils.equals(itemInfo.getSpecCheckResult(), BizConstant.SP_SPECCHECKRESULT_LOWERSCREENLIMIT)) {
                                        if (log.isDebugEnabled()) {
                                            log.debug("== Convert specCheckResult: {} -> {}",
                                                    BizConstant.SP_SPECCHECKRESULT_LOWERSCREENLIMIT,
                                                    BizConstant.SP_SPECCHECKRESULT_1X_LOWERSCREENLIMIT);
                                        }
                                        itemInfo.setSpecCheckResult(BizConstant.SP_SPECCHECKRESULT_1X_LOWERSCREENLIMIT);
                                    }
                                    // SpecCheckResult = #
                                    else if (CimStringUtils.equals(itemInfo.getSpecCheckResult(), BizConstant.SP_SPECCHECKRESULT_1POUND)) {
                                        if (log.isDebugEnabled()) {
                                            log.debug("== Convert specCheckResult: {} -> {}",
                                                    BizConstant.SP_SPECCHECKRESULT_1POUND,
                                                    BizConstant.SP_SPECCHECKRESULT_1X_POUND);
                                        }
                                        itemInfo.setSpecCheckResult(BizConstant.SP_SPECCHECKRESULT_1X_POUND);
                                    }
                                    // SpecCheckResult = *
                                    else if (CimStringUtils.equals(itemInfo.getSpecCheckResult(), BizConstant.SP_SPECCHECKRESULT_1ASTERISK)) {
                                        if (log.isDebugEnabled()) {
                                            log.debug("== Convert specCheckResult: {} -> {}",
                                                    BizConstant.SP_SPECCHECKRESULT_1ASTERISK,
                                                    BizConstant.SP_SPECCHECKRESULT_1X_ASTERISK);
                                        }
                                        itemInfo.setSpecCheckResult(BizConstant.SP_SPECCHECKRESULT_1X_ASTERISK);
                                    }
                                    else {
                                        // do Nothing
                                        log.debug("Do nothing");
                                    }
                                });
                            }
                        }

                        ProcessDTO.DataCollectionInfo tmpDCInfo = new ProcessDTO.DataCollectionInfo();
                        dataCollectionInfos.add(tmpDCInfo);
                        tmpDCInfo.setDataCollectionDefinitionID(dcDef.getDataCollectionDefinitionID());
                        tmpDCInfo.setDescription(dcDef.getDescription());
                        tmpDCInfo.setDataCollectionType(dcDef.getDataCollectionType());

                        //-----------------------------------------------------------------------------------
                        // Collection spec item check result aggregate, need save to
                        // OMPROPE_EDC
                        // OMPROPE_EDC_ITEMS
                        //-----------------------------------------------------------------------------------
                        List<ProcessDTO.DataCollectionItemInfo> dcItems = dcDef.getDcItems()
                                .parallelStream()
                                .map(Infos.DataCollectionItemInfo::convert)
                                .collect(Collectors.toList());

                        tmpDCInfo.setDcItems(dcItems);
                        tmpDCInfo.setCalculationRequiredFlag(dcDef.getCalculationRequiredFlag());
                        tmpDCInfo.setSpecCheckRequiredFlag(dcDef.getSpecCheckRequiredFlag());
                        tmpDCInfo.setDataCollectionSpecificationID(dcDef.getDataCollectionSpecificationID());
                        tmpDCInfo.setDcSpecDescription(dcDef.getDcSpecDescription());

                        // EDC Spec Info
                        if (CimBooleanUtils.isTrue(dataCollectionExistFlag) && CimArrayUtils.isNotEmpty(dcDataCollectionInfoSeqFormDB)) {
                            for (ProcessDTO.DataCollectionInfo dataCollectionInfo : dcDataCollectionInfoSeqFormDB) {
                                if (ObjectIdentifier.equalsWithValue(tmpDCInfo.getDataCollectionSpecificationID(),
                                        dataCollectionInfo.getDataCollectionSpecificationID())) {
                                    tmpDCInfo.setDcSpecs(dataCollectionInfo.getDcSpecs());
                                    break;
                                }
                            }
                        }
                        // DataCollection is not exist
                        else {
                            List<ProcessDTO.DataCollectionSpecInfo> dcSpecs = dcDef.getDcSpecs()
                                    .stream()
                                    .map(Infos.DataCollectionSpecInfo::convert)
                                    .collect(Collectors.toList());
                            tmpDCInfo.setDcSpecs(dcSpecs);
                        }
                        tmpDCInfo.setPreviousDataCollectionDefinitionID(dcDef.getPreviousDataCollectionDefinitionID());
                        tmpDCInfo.setPreviousOperationID(dcDef.getPreviousOperationID());
                        tmpDCInfo.setPreviousOperationNumber(dcDef.getPreviousOperationNumber());
                        tmpDCInfo.setEdcSettingType(dcDef.getEdcSettingType());
                        lotInCassette.getStartRecipe().getDcDefList().set(dcLoopIndex, dcDef);
                    }
                    // todo:zqi 是否需要更改为只更新 items 信息，后续性能测试看结果后在优化
                    /*processOperation.setEDCItemsInfo(dataCollectionInfos);*/
                    processOperation.setDataCollectionInfo(dataCollectionInfos);
                }
            }
        }

        return retVal;
    }
}
