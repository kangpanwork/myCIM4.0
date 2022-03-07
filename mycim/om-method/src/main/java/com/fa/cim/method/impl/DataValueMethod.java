package com.fa.cim.method.impl;

import com.fa.cim.annotaion.OmMethod;
import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.utils.*;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Inputs;
import com.fa.cim.dto.Outputs;
import com.fa.cim.method.IBondingGroupMethod;
import com.fa.cim.method.IDataValueMethod;
import com.fa.cim.method.IEquipmentMethod;
import com.fa.cim.method.ILotMethod;
import com.fa.cim.newcore.bo.dc.CimDataCollectionDefinition;
import com.fa.cim.newcore.bo.globalfunc.CimFrameWorkGlobals;
import com.fa.cim.newcore.bo.pd.CimProcessOperation;
import com.fa.cim.newcore.bo.product.CimLot;
import com.fa.cim.newcore.dto.dc.EDCDTO;
import com.fa.cim.newcore.dto.pd.ProcessDTO;
import com.fa.cim.newcore.exceptions.IllegalParameterException;
import com.fa.cim.newcore.factory.BaseCoreFactory;
import com.fa.cim.newcore.impl.bo.env.StandardProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;


/**
 * description:
 * <p>DataValueMethod .<br/></p>
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2018/10/19        ********             PlayBoy               create file
 *
 * @author PlayBoy
 * @since 2018/10/19 11:20
 * Copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@OmMethod
@Slf4j
public class DataValueMethod implements IDataValueMethod {

    @Autowired
    private IEquipmentMethod equipmentMethod;

    @Autowired
    private ILotMethod lotMethod;

    @Autowired
    private RetCodeConfig retCodeConfig;

    @Autowired
    private IBondingGroupMethod bondingGroupMethod;

    @Autowired
    private BaseCoreFactory baseCoreFactory;

    @Autowired
    private CimFrameWorkGlobals cimFrameWorkGlobals;

    @Override
    public Outputs.ObjDataValueCheckValidityForSpecCheckDrOut dataValueCheckValidityForSpecCheckDR(Infos.ObjCommon objCommon,
                                                                                                   ObjectIdentifier equipmentID,
                                                                                                   ObjectIdentifier controlJobID,
                                                                                                   List<Infos.StartCassette> startCassetteList) {

        List<Infos.DataCollectionItemInfo> checkDCItems;

        Outputs.ObjDataValueCheckValidityForSpecCheckDrOut retVal = new Outputs.ObjDataValueCheckValidityForSpecCheckDrOut();
        boolean dataValueExist = false;
        boolean dataBlankCheck = false;
        //Bonding Map Info Get
        boolean topLotFlag;

        //----------------------------------------------------------------
        // Bonding Map Info Get
        //----------------------------------------------------------------
        Outputs.ObjBondingGroupInfoByEqpGetDROut strBondingGroupInfoByEqpGetDROut;

        log.info("calling bondingGroup_infoByEqp_GetDR()");
        // bondingGroup_infoByEqp_GetDR
        strBondingGroupInfoByEqpGetDROut = bondingGroupMethod.bondingGroupInfoByEqpGetDR(
                objCommon,
                equipmentID,
                controlJobID,
                true);

        int topLotLen = CimArrayUtils.getSize(strBondingGroupInfoByEqpGetDROut.getTopLotIDSeq());
        log.info("topLotLen={}", topLotLen);

        retVal.setControlJobID(controlJobID);
        retVal.setEquipmentID(equipmentID);
        retVal.setStartCassetteList(startCassetteList);

        int cassetteLen = CimArrayUtils.getSize(startCassetteList);
        for (int cCnt = 0; cCnt < cassetteLen; cCnt++) {

            Infos.StartCassette startCassette = startCassetteList.get(cCnt);
            List<Infos.LotInCassette> lotInCassetteList = startCassette.getLotInCassetteList();
            for (Infos.LotInCassette lotInCassette : lotInCassetteList) {
                if (CimBooleanUtils.isFalse(lotInCassette.getMoveInFlag())) {
                    continue;
                }
                boolean inPostProcessFlagOfLot = false;
                ObjectIdentifier lotControlJobIDOut = lotMethod.lotControlJobIDGet(objCommon, lotInCassette.getLotID());
                if (ObjectIdentifier.isEmptyWithValue(lotControlJobIDOut)) {
                    //lot is not in processing
                    inPostProcessFlagOfLot = true;
                }
                if (!inPostProcessFlagOfLot) {
                    // lot is in processing
                    dataBlankCheck = true;

                    topLotFlag = false;
                    for (int topLotCnt = 0; topLotCnt < topLotLen; topLotCnt++) {
                        log.info("topLotLen/topLotCnt={}/{}", topLotLen, topLotCnt);
                        if (CimStringUtils.equals(lotInCassette.getLotID().getValue(),
                                strBondingGroupInfoByEqpGetDROut.getTopLotIDSeq().get(topLotCnt).getValue())) {
                            log.info("Top Lot");
                            topLotFlag = true;
                            break;
                        }
                    }

                    List<Infos.DataCollectionInfo> dcDefList = lotInCassette.getStartRecipe().getDcDefList();
                    final int dcDefListSize = CimArrayUtils.getSize(dcDefList);
                    for (int i = 0; i < dcDefListSize; i++) {
                        Infos.DataCollectionInfo dcDef = dcDefList.get(i);
                        //[1] Set Input Value for doValidCheck()
                        List<EDCDTO.DCItemData> dcItemDataList = new ArrayList<>();
                        List<Infos.DataCollectionItemInfo> dcItemList = dcDef.getDcItems();
                        if (CimArrayUtils.isEmpty(dcItemList)) {
                            continue;
                        }
                        for (Infos.DataCollectionItemInfo dcItem : dcItemList) {
                            EDCDTO.DCItemData dcItemData = new EDCDTO.DCItemData();
                            dcItemDataList.add(dcItemData);
                            dcItemData.setDataItemName(dcItem.getDataCollectionItemName());
                            dcItemData.setWaferPosition(dcItem.getWaferPosition());
                            dcItemData.setSitePosition(dcItem.getSitePosition());
                            dcItemData.setValType(dcItem.getDataType());
                            dcItemData.setItemType(dcItem.getItemType());
                            dcItemData.setCalculationType(dcItem.getCalculationType());
                            dcItemData.setCalculationExpression(dcItem.getCalculationExpression());
                            dcItemData.setInputValue(dcItem.getDataValue());
                        }
                        //[2] Check validity
                        CimDataCollectionDefinition dataCollectionDefinition =
                                baseCoreFactory.getBO(CimDataCollectionDefinition.class, dcDef.getDataCollectionDefinitionID());
                        try {
                            dataCollectionDefinition.doValidCheck(dcItemDataList);
                        } catch (IllegalParameterException e) {
                            Validations.check( retCodeConfig.getDatavalCannotConvToInt());
                        }
                        //[3] Get eqp's operation phase
                        /*CimMachine equipment = baseCoreFactory.getBO(CimMachine.class, equipmentID);
                        String machineCategory = equipment.getCategory();*/
                        //Get Oparation Mode
                        String operationMode = equipmentMethod.equipmentOnlineModeGet(objCommon, equipmentID);

                        //-----------------------------------------------------------------------------------------
                        // [4] Check value by equipment's operation phase
                        //-----------------------------------------------------------------------------------------
                        int totalDataCount = 0, asteriskDataCount = 0, blankDataCount = 0, reportedDataCount = 0;
                        List<Infos.DCItem> strTmpPJDCItemSeq = new ArrayList<>();
                        int tmpPJDCCnt = 0;

                        int dcItemsSize = CimArrayUtils.getSize(dcItemList);
                        if (CimStringUtils.equals(operationMode, BizConstant.SP_EQP_ONLINEMODE_OFFLINE)) {

                            for (i = 0; i < dcItemsSize; i++) {
                                final Infos.DataCollectionItemInfo itemInfo = dcItemList.get(i);
                                if (CimStringUtils.equals(itemInfo.getItemType(), BizConstant.SP_DCDEF_ITEM_RAW)) {

                                    // Collection Mode is = Manual
                                    if (CimStringUtils.equals(itemInfo.getDataCollectionMode(), BizConstant.SP_DCDEF_MODE_MANUAL)) {
                                        reportedDataCount++;
                                        //----------------------------------------------------------
                                        // Measurement type of retVal item
                                        // PJ Wafer or PJ Wafer Site: Pick up retVal item
                                        // PJ: Pick up retVal item
                                        // Otherwise: Count retVal item
                                        //----------------------------------------------------------
                                        // Proc Wafer or Proc Site
                                        if (CimStringUtils.equals(BizConstant.SP_DCDEF_MEAS_PJWAFER, itemInfo.getMeasurementType())
                                                || CimStringUtils.equals(BizConstant.SP_DCDEF_MEAS_PJWAFERSITE, itemInfo.getMeasurementType())) {
                                            boolean bFoundFlag = FALSE;
                                            for (int v = 0; v < tmpPJDCCnt; v++) {
                                                if (CimStringUtils.equals(strTmpPJDCItemSeq.get(v).getDataCollectionItemName(), itemInfo.getDataCollectionItemName())
                                                        && CimStringUtils.equals(strTmpPJDCItemSeq.get(v).getSitePosition(), itemInfo.getSitePosition())
                                                        && CimStringUtils.equals(strTmpPJDCItemSeq.get(v).getMeasurementType(), itemInfo.getMeasurementType())) {
                                                    //Already picked up
                                                    bFoundFlag = TRUE;
                                                    break;
                                                }
                                            }
                                            if (CimBooleanUtils.isFalse(bFoundFlag)) {
                                                strTmpPJDCItemSeq.add(new Infos.DCItem());
                                                strTmpPJDCItemSeq.get(tmpPJDCCnt).setDataCollectionItemName(itemInfo.getDataCollectionItemName());
                                                strTmpPJDCItemSeq.get(tmpPJDCCnt).setSitePosition(itemInfo.getSitePosition());
                                                strTmpPJDCItemSeq.get(tmpPJDCCnt).setMeasurementType(itemInfo.getMeasurementType());
                                                tmpPJDCCnt++;
                                            }
                                        }
                                        // Process Job
                                        else if (CimStringUtils.equals(BizConstant.SP_DCDEF_MEAS_PJ, itemInfo.getMeasurementType())) {
                                            boolean bFoundFlag = FALSE;
                                            for (int v = 0; v < tmpPJDCCnt; v++) {
                                                if (CimStringUtils.equals(strTmpPJDCItemSeq.get(v).getDataCollectionItemName(), itemInfo.getDataCollectionItemName())
                                                        && CimStringUtils.equals(strTmpPJDCItemSeq.get(v).getMeasurementType(), itemInfo.getMeasurementType())) {
                                                    //Already picked up
                                                    bFoundFlag = TRUE;
                                                    break;
                                                }
                                            }
                                            if (CimBooleanUtils.isFalse(bFoundFlag)) {
                                                strTmpPJDCItemSeq.add(new Infos.DCItem());
                                                strTmpPJDCItemSeq.get(tmpPJDCCnt).setDataCollectionItemName(itemInfo.getDataCollectionItemName());
                                                strTmpPJDCItemSeq.get(tmpPJDCCnt).setMeasurementType(itemInfo.getMeasurementType());
                                                tmpPJDCCnt++;
                                            }
                                        }
                                        // other
                                        else {
                                            totalDataCount++;
                                        }

                                        if (CimStringUtils.equals(itemInfo.getDataValue(), "*")) {
                                            asteriskDataCount++;
                                        }

                                        if (CimStringUtils.equals(itemInfo.getDataValue(), "")) {
                                            blankDataCount++;
                                        }
                                    }
                                    // Collection Mode is = Auto or Derived
                                    else {

                                        if (CimStringUtils.equals(itemInfo.getDataValue(), "*")) {
                                            itemInfo.setSpecCheckResult("*");
                                        }

                                        if (CimStringUtils.equals(itemInfo.getDataValue(), "")) {
                                            itemInfo.setSpecCheckResult("*");
                                        }
                                    }
                                }
                            }
                        }
                        // online mode is not Off-Line
                        else {
                            for (i = 0; i < dcItemsSize; i++) {
                                final Infos.DataCollectionItemInfo itemInfo = dcItemList.get(i);
                                if (CimStringUtils.equals(itemInfo.getItemType(), BizConstant.SP_DCDEF_ITEM_RAW)) {
                                    reportedDataCount++;
                                    //----------------------------------------------------------
                                    // Measurement type of retVal item
                                    // PJ Wafer or PJ Wafer Site: Pick up retVal item
                                    // PJ: Pick up retVal item
                                    // Otherwise: Count retVal item
                                    //----------------------------------------------------------
                                    if (CimStringUtils.equals(BizConstant.SP_DCDEF_MEAS_PJWAFER, itemInfo.getMeasurementType())
                                            || CimStringUtils.equals(BizConstant.SP_DCDEF_MEAS_PJWAFERSITE, itemInfo.getMeasurementType())) {
                                        boolean bFoundFlag = FALSE;
                                        for (int v = 0; v < tmpPJDCCnt; v++) {
                                            if (CimStringUtils.equals(strTmpPJDCItemSeq.get(v).getDataCollectionItemName(), itemInfo.getDataCollectionItemName())
                                                    && CimStringUtils.equals(strTmpPJDCItemSeq.get(v).getSitePosition(), itemInfo.getSitePosition())
                                                    && CimStringUtils.equals(strTmpPJDCItemSeq.get(v).getMeasurementType(), itemInfo.getMeasurementType())) {
                                                //Already picked up
                                                bFoundFlag = TRUE;
                                                break;
                                            }
                                        }
                                        if (CimBooleanUtils.isFalse(bFoundFlag)) {
                                            strTmpPJDCItemSeq.add(new Infos.DCItem());
                                            strTmpPJDCItemSeq.get(tmpPJDCCnt).setDataCollectionItemName(itemInfo.getDataCollectionItemName());
                                            strTmpPJDCItemSeq.get(tmpPJDCCnt).setSitePosition(itemInfo.getSitePosition());
                                            strTmpPJDCItemSeq.get(tmpPJDCCnt).setMeasurementType(itemInfo.getMeasurementType());
                                            tmpPJDCCnt++;
                                        }
                                    } else if (CimStringUtils.equals(BizConstant.SP_DCDEF_MEAS_PJ, itemInfo.getMeasurementType())) {
                                        boolean bFoundFlag = FALSE;
                                        for (int v = 0; v < tmpPJDCCnt; v++) {
                                            if (CimStringUtils.equals(strTmpPJDCItemSeq.get(v).getDataCollectionItemName(), itemInfo.getDataCollectionItemName())
                                                    && CimStringUtils.equals(strTmpPJDCItemSeq.get(v).getMeasurementType(), itemInfo.getMeasurementType())) {
                                                //Already picked up
                                                bFoundFlag = TRUE;
                                                break;
                                            }
                                        }
                                        if (CimBooleanUtils.isFalse(bFoundFlag)) {
                                            strTmpPJDCItemSeq.add(new Infos.DCItem());
                                            strTmpPJDCItemSeq.get(tmpPJDCCnt).setDataCollectionItemName(itemInfo.getDataCollectionItemName());
                                            strTmpPJDCItemSeq.get(tmpPJDCCnt).setMeasurementType(itemInfo.getMeasurementType());
                                            tmpPJDCCnt++;
                                        }
                                    } else {
                                        totalDataCount++;
                                    }

                                    if (CimStringUtils.equals(itemInfo.getDataValue(), "*")) {
                                        itemInfo.setSpecCheckResult("*");
                                        asteriskDataCount++;
                                    }

                                    if (CimStringUtils.equals(itemInfo.getDataValue(), "")) {
                                        itemInfo.setSpecCheckResult("*");
                                        blankDataCount++;
                                    }
                                }
                            }
                        }

                        //--- Get Lot Object -------------------------------------------------------------------------//
                        CimLot aLot = baseCoreFactory.getBO(CimLot.class, lotInCassette.getLotID());

                        //--- Get Process Operation Object -----------------------------------------------------------//
                        CimProcessOperation aProcessOperation;

                        //--------------------------------------------------------------
                        // Current PO or Previous PO
                        //--------------------------------------------------------------
                        Boolean strLotCheckConditionForPOOut = lotMethod.lotCheckConditionForPO(
                                objCommon,
                                lotInCassette.getLotID());

                        if (CimBooleanUtils.isTrue(strLotCheckConditionForPOOut)) {
                            //--------------------------------------------------------------------------
                            // Get PO from Current Operation.
                            //--------------------------------------------------------------------------
                            aProcessOperation = aLot.getProcessOperation();
                        } else {
                            //--------------------------------------------------------------------------
                            // Get PO from Previous Operation.
                            //--------------------------------------------------------------------------
                            aProcessOperation = aLot.getPreviousProcessOperation();
                        }

                        Validations.check(aProcessOperation == null, retCodeConfig.getNotFoundProcessOperation());

                        //--------------------------------------------
                        // Get process wafer information
                        //--------------------------------------------
                        List<ProcessDTO.PosProcessWafer> strProcessWaferSeq;
                        List<Infos.ProcessRecipeParameter> strPJWaferInfo = new ArrayList<>();

                        if (0 < CimArrayUtils.getSize(strTmpPJDCItemSeq)) {
                            strProcessWaferSeq = aProcessOperation.getProcessWafers();

                            int procWfrLen = CimArrayUtils.getSize(strProcessWaferSeq);
                            if (0 < procWfrLen) {
                                strPJWaferInfo = new ArrayList<>(procWfrLen);
                                int PJWaferCnt = 0;
                                for (int v = 0; v < procWfrLen; v++) {
                                    boolean bFoundFlag = FALSE;
                                    for (int w = 0; w < PJWaferCnt; w++) {
                                        if (CimStringUtils.equals(strProcessWaferSeq.get(v).getPrcsJob(), strPJWaferInfo.get(w).getProcessJobID())) {
                                            bFoundFlag = TRUE;
                                            break;
                                        }
                                    }
                                    if (CimBooleanUtils.isFalse(bFoundFlag)) {
                                        strPJWaferInfo.add(new Infos.ProcessRecipeParameter());
                                        strPJWaferInfo.get(PJWaferCnt).setProcessJobID(strProcessWaferSeq.get(v).getPrcsJob());
                                        PJWaferCnt++;
                                    }
                                }
                            }
                            // 0 >= procWfrLen
                            else {
                                if (TransactionIDEnum.PARTIAL_OPERATION_COMP_WITH_DATA_REQ.equals(objCommon.getTransactionID())
                                        || TransactionIDEnum.PARTIAL_OPERATION_COMP_FOR_INTERNAL_BUFFER_REQ.equals(objCommon.getTransactionID())) {
                                    //Call METHOD lot_materials_GetWafers
                                    log.info("call lot_materials_GetWafers()");
                                    List<Infos.LotWaferAttributes> strLotMaterialsGetWafersOut;
                                    strLotMaterialsGetWafersOut = lotMethod.lotMaterialsGetWafers(
                                            objCommon,
                                            lotInCassette.getLotID());
                                    procWfrLen = CimArrayUtils.getSize(strLotMaterialsGetWafersOut);
                                } else {
                                    //Call METHOD lot_waferIDList_GetDR
                                    Inputs.ObjLotWaferIDListGetDRIn lotWaferIDListParams = new Inputs.ObjLotWaferIDListGetDRIn();
                                    lotWaferIDListParams.setLotID(lotInCassette.getLotID());
                                    lotWaferIDListParams.setScrapCheckFlag(true);
                                    List<ObjectIdentifier> waferIDs = lotMethod.lotWaferIDListGetDR(objCommon, lotWaferIDListParams);

                                    procWfrLen = waferIDs == null ? 0 : waferIDs.size();
                                }
                            }

                            //--------------------------------------------
                            // Count retVal item count (defined count)
                            //     - SP_DCDef_Meas_PJWafer
                            //     - SP_DCDef_Meas_PJWaferSite
                            //     - SP_DCDef_Meas_PJ
                            //--------------------------------------------
                            int TmpPJDCItemSeqLen = CimArrayUtils.getSize(strTmpPJDCItemSeq);
                            for (int y = 0; y < TmpPJDCItemSeqLen; y++) {
                                if (CimStringUtils.equals(BizConstant.SP_DCDEF_MEAS_PJWAFER, strTmpPJDCItemSeq.get(y).getMeasurementType())
                                        || CimStringUtils.equals(BizConstant.SP_DCDEF_MEAS_PJWAFERSITE, strTmpPJDCItemSeq.get(y).getMeasurementType())) {
                                    // measurementType is SP_DCDEF_MEAS_PJWAFER or SP_DCDEF_MEAS_PJWAFERSITE
                                    totalDataCount = totalDataCount + procWfrLen;
                                } else {
                                    // measurementType is SP_DCDEF_MEAS_PJ
                                    totalDataCount = totalDataCount + CimArrayUtils.getSize(strPJWaferInfo);
                                }
                            }
                        }

                        if (totalDataCount == 0) {
                            if (log.isDebugEnabled()) {
                                log.debug("totalDataCount == 0");
                            }
                        }
                        else if (asteriskDataCount == totalDataCount) {
                            //---------------------------------------------------------------------------------------------------------------
                            // Set '*' to specCheckResult of all item including delta/deribed when all of raw retVal value is '*'
                            //---------------------------------------------------------------------------------------------------------------
                            lotInCassette.getStartRecipe().getDcDefList()
                                    .forEach(innerDCDef -> innerDCDef.getDcItems().forEach(innerItem -> {
                                        innerItem.setDataValue("*");
                                        innerItem.setSpecCheckResult("*");
                                    }));
                            //------------------------------------------------------------------------
                            // Get collection retVal in PosProcessOperation object
                            //------------------------------------------------------------------------
                            ProcessDTO.ActualStartInformationForPO actualStartInfo = aProcessOperation.getActualStartInfo(true);
                            List<ProcessDTO.DataCollectionInfo> strDataCollectionInfoList = actualStartInfo.getAssignedDataCollections();
                            //------------------------------------------------------------------------
                            // Set '*' to specCheckResult for all of DC items
                            //------------------------------------------------------------------------
                            if (CimArrayUtils.isNotEmpty(strDataCollectionInfoList)) {
                                strDataCollectionInfoList
                                        .forEach(dataCollectionInfo -> dataCollectionInfo.getDcItems()
                                                .forEach(dataCollectionItemInfo -> dataCollectionItemInfo.setSpecCheckResult("*"))
                                        );
                            }
                            //------------------------------------------------------------------------
                            // zqi 注意：当Item数量过多时，此处性能会变差
                            // Update collection retVal in PosProcessOperation object
                            //------------------------------------------------------------------------
                            aProcessOperation.setDataCollectionInfo(strDataCollectionInfoList);
                        }
                        else {
                            Validations.check(blankDataCount == totalDataCount, retCodeConfig.getAllDataValueBlank());
                            Validations.check(!topLotFlag && reportedDataCount != totalDataCount, retCodeConfig.getProcessJobDataNotReported());
                            Validations.check(blankDataCount > 0, retCodeConfig.getSomeDataValueBlank());
                            dataValueExist = true;
                        }
                    }
                }

                String dcCheckLevel = StandardProperties.OM_EDC_CHK_LEVEL.getValue();
                String dcCheckPhase = StandardProperties.OM_EDC_ASTERISK_VALUE_CHK_TIME.getValue();
                if (CimStringUtils.unEqual(dcCheckLevel, BizConstant.LEVEL_ONE)
                        && CimStringUtils.unEqual(dcCheckLevel, BizConstant.LEVEL_TWO)) {
                    dcCheckLevel = BizConstant.LEVEL_ZERO;
                }
                if (CimStringUtils.unEqual(dcCheckPhase, BizConstant.PHASE_ONE)) {
                    dcCheckPhase = BizConstant.PHASE_ZERO;
                }

                if (CimStringUtils.unEqual(dcCheckLevel, BizConstant.LEVEL_ZERO)) {
                    if (CimBooleanUtils.isFalse(lotInCassette.getMoveInFlag())) {
                        continue;
                    }
                    boolean phaseCheckOne = CimStringUtils.equals(dcCheckPhase, BizConstant.PHASE_ZERO) && !inPostProcessFlagOfLot;
                    boolean phaseCheckTwo = CimStringUtils.equals(dcCheckPhase, BizConstant.PHASE_ONE) && inPostProcessFlagOfLot;
                    if (phaseCheckOne || phaseCheckTwo) {
                        log.debug("checkValidityForSpecCheckDR(): Do Asterisk retVal value check");
                        List<Infos.DataCollectionInfo> dcDefList = lotInCassette.getStartRecipe().getDcDefList();
                        checkDCItems = new ArrayList<>();
                        if (CimArrayUtils.isNotEmpty(dcDefList)) {
                            // Get Processed wafer count here
                            AtomicLong processWaferCnt = new AtomicLong(0L);
                            List<Infos.LotWafer> lotWaferList = lotInCassette.getLotWaferList();
                            if (CimArrayUtils.isNotEmpty(lotWaferList)) {
                                lotWaferList.forEach(lotWafer -> {
                                    if (CimBooleanUtils.isTrue(lotWafer.getProcessJobExecFlag())) {
                                        processWaferCnt.incrementAndGet();
                                    }
                                });
                            }
                            /*
                             Prepare target check DCItem/Value (checkDCItems)
                             Remove unrelative DCItems from DCDef
                              - Only processed wafer items should be checked. "Auto" mode item is not checked because
                                ~ 1. it cannot be input from MMC before opeComp,
                                ~ 2. no information for lot processed eqp mode
                              - Remove Auto mode of items if eqp is offline
                             */
                            for (Infos.DataCollectionInfo dcDef : dcDefList) {
                                List<Infos.DataCollectionItemInfo> dcItemList = dcDef.getDcItems();
                                if (CimArrayUtils.isEmpty(dcItemList)) {
                                    continue;
                                }
                                for (Infos.DataCollectionItemInfo dcItem : dcItemList) {
                                    long waferPos = CimNumberUtils.longValue(dcItem.getWaferPosition());
                                    if (waferPos == 0L || waferPos <= processWaferCnt.get()) {
                                        // if not Auto items
                                        if (CimStringUtils.unEqual(dcItem.getDataCollectionMode(), BizConstant.SP_DCDEF_MODE_AUTO)) {
                                            checkDCItems.add(dcItem);
                                        }
                                    }
                                }
                            }
                            // Start main checking
                            for (Infos.DataCollectionInfo dcDef : dcDefList) {
                                //Get DCSpec
                                if (CimBooleanUtils.isTrue(dcDef.getSpecCheckRequiredFlag())
                                        && CimStringUtils.isNotEmpty(dcDef.getDataCollectionSpecificationID().getValue())) {
                                    List<Infos.DataCollectionSpecInfo> dcSpecList = dcDef.getDcSpecs();
                                    if (CimArrayUtils.isEmpty(dcSpecList)) {
                                        continue;
                                    }
                                    for (Infos.DataCollectionSpecInfo dcSpec : dcSpecList) {
                                        // No check if theSpecItem dosen't require limit
                                        boolean notRequiredLimit = CimBooleanUtils.isFalse(dcSpec.getScreenLimitUpperRequired())
                                                && CimBooleanUtils.isFalse(dcSpec.getScreenLimitLowerRequired())
                                                && CimBooleanUtils.isFalse(dcSpec.getSpecLimitUpperRequired())
                                                && CimBooleanUtils.isFalse(dcSpec.getSpecLimitLowerRequired())
                                                && CimBooleanUtils.isFalse(dcSpec.getControlLimitUpperRequired())
                                                && CimBooleanUtils.isFalse(dcSpec.getControlLimitLowerRequired());
                                        if (notRequiredLimit) {
                                            log.debug("checkValidityForSpecCheckDR(): LimitUpperRequired is FALSE, No check");
                                            continue;
                                        }
                                        //Search this item in checkDCItems
                                        Infos.DataCollectionItemInfo dcItem = getDCItem(checkDCItems, dcSpec.getDataItemName());
                                        if (dcItem == null || CimStringUtils.isEmpty(dcItem.getDataCollectionItemName())) {
                                            // No item is found. This item dosen't require any retVal
                                            log.debug("checkValidityForSpecCheckDR(): ## This Item is not required by processed wafer");
                                            continue;
                                        }
                                        // Set up default
                                        boolean okFlag = false;
                                        if (CimStringUtils.equals(dcCheckLevel, BizConstant.LEVEL_TWO)) {
                                            okFlag = true;
                                        }
                                        if (CimStringUtils.equals(dcItem.getItemType(), BizConstant.SP_DCDEF_ITEM_RAW)) {
                                            for (Infos.DataCollectionItemInfo checkDCItem : checkDCItems) {
                                                if (CimStringUtils.equals(dcItem.getDataCollectionItemName(), checkDCItem.getDataCollectionItemName())) {
                                                    if (CimStringUtils.unEqual(checkDCItem.getDataValue(), "*")) {
                                                        if (CimStringUtils.equals(dcCheckLevel, BizConstant.LEVEL_ONE)) {
                                                            okFlag = true;
                                                            break;
                                                        }
                                                    } else if (CimStringUtils.equals(dcCheckLevel, BizConstant.LEVEL_TWO)) {
                                                        okFlag = false;
                                                        break;
                                                    }
                                                }
                                            }
                                            Validations.check(!okFlag, retCodeConfig.getInvalidDataValue(), dcItem.getDataCollectionItemName());
                                        }
                                        //derived dataItem
                                        else {
                                            List<Infos.DataCollectionItemInfo> rawItems = getRawItems(checkDCItems, dcItem, processWaferCnt.get());
                                            /*
                                             compare rawItems and checkDCItems.
                                             for dcCheckLevel1 check, at least one raw item should have valid value
                                             for dcCheckLevel2 check, all raw items should have valid value
                                             */
                                            if (CimArrayUtils.isNotEmpty(rawItems)) {
                                                for (Infos.DataCollectionItemInfo rawItem : rawItems) {
                                                    for (Infos.DataCollectionItemInfo checkDCItem : checkDCItems) {
                                                        if (CimStringUtils.equals(rawItem.getDataCollectionItemName(), checkDCItem.getDataCollectionItemName())) {
                                                            if (CimStringUtils.unEqual(checkDCItem.getDataValue(), "*")) {
                                                                if (CimStringUtils.equals(dcCheckLevel, BizConstant.LEVEL_ONE)) {
                                                                    okFlag = true;
                                                                    break;
                                                                }
                                                            } else {
                                                                if (CimStringUtils.equals(dcCheckLevel, BizConstant.LEVEL_TWO)) {
                                                                    okFlag = false;
                                                                    break;
                                                                }
                                                            }
                                                        }
                                                    }
                                                    Validations.check(!okFlag, retCodeConfig.getInvalidDataValue(), rawItem.getDataCollectionItemName());
                                                    if (CimStringUtils.equals(dcCheckLevel, BizConstant.LEVEL_ONE)) {
                                                        break;
                                                    } else {
                                                        // reset default
                                                        okFlag = true;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        Validations.check(CimBooleanUtils.isFalse(dataValueExist) && CimBooleanUtils.isTrue(dataBlankCheck), retCodeConfig.getAllDataValAsterisk());

        return retVal;
    }


    @Override
    public List<Infos.DataCollectionInfo> dataValueCheckValidityForSpecCheckByPJ(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, ObjectIdentifier controlJob, ObjectIdentifier lotID, List<Infos.DataCollectionInfo> strDCDef) {
        List<Infos.DataCollectionInfo> dataCollectionInfos = null;
        dataCollectionInfos = strDCDef;
        int dcDefLen = CimArrayUtils.getSize(dataCollectionInfos);
        log.info("dcDefLen : {}", dcDefLen);
        for (int i = 0; i < dcDefLen; i++) {
            //-----------------------------------------------------------------------------------------
            // [1] Set Input Value for doValidCheck()
            //-----------------------------------------------------------------------------------------
            List<Infos.DataCollectionItemInfo> dcItems = dataCollectionInfos.get(i).getDcItems();
            List<EDCDTO.DCItemData> strDCItemData = new ArrayList<>();
            int length = CimArrayUtils.getSize(dcItems);
            for (int j = 0; j < length; j++) {
                EDCDTO.DCItemData data = new EDCDTO.DCItemData();
                strDCItemData.add(data);
                data.setDataItemName(dcItems.get(j).getDataCollectionItemName());
                data.setWaferPosition(dcItems.get(j).getWaferPosition());
                data.setSitePosition(dcItems.get(j).getSitePosition());
                data.setValType(dcItems.get(j).getDataType());
                data.setItemType(dcItems.get(j).getItemType());
                data.setCalculationType(dcItems.get(j).getCalculationType());
                data.setCalculationExpression(dcItems.get(j).getCalculationExpression());
                data.setInputValue(dcItems.get(j).getDataValue());
            }
            //-----------------------------------------------------------------------------------------
            // [2] Check validity
            //-----------------------------------------------------------------------------------------
            CimDataCollectionDefinition aDataCollectionDefinition = baseCoreFactory.getBO(CimDataCollectionDefinition.class, dataCollectionInfos.get(i).getDataCollectionDefinitionID());
            try {
                aDataCollectionDefinition.doValidCheck(strDCItemData);
            } catch (IllegalParameterException e) {
                Validations.check(true, retCodeConfig.getDatavalCannotConvToInt());
            }
        }
        return dataCollectionInfos;
    }

    private void measurementTypeCheck(AtomicLong reportedDataCount, AtomicLong totalDataCount, Infos.DataCollectionItemInfo dcItem, List<Infos.DataCollectionItemInfo> strTmpPJDCItemList) {
        reportedDataCount.incrementAndGet();
        /*
         Measurement type of data item
         PJ wafer or PJ wafer Site: Pick up data item
         PJ: Pick up data item
         Otherwise: Count data item
         */
        if (CimStringUtils.equals(BizConstant.SP_DCDEF_MEAS_PJWAFER, dcItem.getMeasurementType())
                || CimStringUtils.equals(BizConstant.SP_DCDEF_MEAS_PJWAFERSITE, dcItem.getMeasurementType())) {
            boolean foundFlag = false;
            for (Infos.DataCollectionItemInfo pjDCitem : strTmpPJDCItemList) {
                if (CimStringUtils.equals(pjDCitem.getDataCollectionItemName(), dcItem.getDataCollectionItemName())
                        && CimStringUtils.equals(pjDCitem.getSitePosition(), dcItem.getSitePosition())
                        && CimStringUtils.equals(pjDCitem.getMeasurementType(), dcItem.getMeasurementType())) {
                    //Already picked up
                    log.debug("checkValidityForSpecCheckDR(): foundFlag = true");
                    foundFlag = true;
                    break;
                }
            }
            if (!foundFlag) {
                Infos.DataCollectionItemInfo pjDCitem = new Infos.DataCollectionItemInfo();
                strTmpPJDCItemList.add(pjDCitem);
                pjDCitem.setDataCollectionItemName(dcItem.getDataCollectionItemName());
                pjDCitem.setSitePosition(dcItem.getSitePosition());
                pjDCitem.setMeasurementType(dcItem.getMeasurementType());
            }
        } else if (CimStringUtils.equals(BizConstant.SP_DCDEF_MEAS_PJ, dcItem.getMeasurementType())) {
            log.debug("checkValidityForSpecCheckDR(): measurementType is SP_DCDEF_MEAS_PJ");
            boolean foundFlag = false;
            for (Infos.DataCollectionItemInfo pjDCitem : strTmpPJDCItemList) {
                if (CimStringUtils.equals(pjDCitem.getDataCollectionItemName(), dcItem.getDataCollectionItemName())
                        && CimStringUtils.equals(pjDCitem.getMeasurementType(), dcItem.getMeasurementType())) {
                    //Already picked up
                    log.debug("checkValidityForSpecCheckDR(): foundFlag = true");
                    foundFlag = true;
                    break;
                }
            }
            if (!foundFlag) {
                Infos.DataCollectionItemInfo pjDCitem = new Infos.DataCollectionItemInfo();
                strTmpPJDCItemList.add(pjDCitem);
                pjDCitem.setDataCollectionItemName(dcItem.getDataCollectionItemName());
                pjDCitem.setMeasurementType(dcItem.getMeasurementType());
            }
        } else {
            totalDataCount.incrementAndGet();
        }
    }

    private List<Infos.DataCollectionItemInfo> getRawItems(List<Infos.DataCollectionItemInfo> checkDCItems, Infos.DataCollectionItemInfo theDCItem, long processedWaferCount) {
        List<Infos.DataCollectionItemInfo> rawItems = new ArrayList<>();
        Infos.DataCollectionItemInfo dcItem = null;
        if (theDCItem == null) {
            return rawItems;
        }
        // if item is a Delta, the post item should be checked
        if (CimStringUtils.equals(theDCItem.getCalculationType(), BizConstant.SP_DCDEF_CALC_DELTA)) {
            List<EDCDTO.CalculationExpressionInfo> calcExpresInfoList = cimFrameWorkGlobals.convertFromCalculationExpression(theDCItem.getCalculationExpression(), true);
            String postDCItemName = null;
            String postWaferPos = null;
            if (CimArrayUtils.getSize(calcExpresInfoList) >= 2) {
                if (calcExpresInfoList.get(0).getWhichDef() == 1) {
                    postDCItemName = calcExpresInfoList.get(1).getItemName();
                    postWaferPos = calcExpresInfoList.get(1).getWaferPosition();
                } else {
                    postDCItemName = calcExpresInfoList.get(0).getItemName();
                    postWaferPos = calcExpresInfoList.get(0).getWaferPosition();
                }
            }
            if (!CimStringUtils.equals(postWaferPos, "*")) {
                long waferPos = CimNumberUtils.longValue(postWaferPos);
                if (waferPos > processedWaferCount) {
                    // this item doesn't require check
                    return rawItems;
                }
            }
            dcItem = getDCItem(checkDCItems, postDCItemName);
            if (dcItem == null || CimStringUtils.isEmpty(dcItem.getDataCollectionItemName())) {
                // couldn't find this item. this item dosen't require check
                return rawItems;
            }

        } else {
            dcItem = theDCItem;
        }
        // If this item is a DeltaItem, its post Item could be Raw
        if (CimStringUtils.equals(dcItem.getItemType(), BizConstant.SP_DCDEF_ITEM_RAW)) {
            rawItems.add(dcItem);
        } else {
            // derived
            //get calculationExpression
            List<EDCDTO.CalculationExpressionInfo> calcExpresInfoList = cimFrameWorkGlobals.convertFromCalculationExpression(theDCItem.getCalculationExpression(), true);
            String dcItemName = null;
            String waferPosStr = null;
            if (CimArrayUtils.isEmpty(calcExpresInfoList)) {
                return rawItems;
            }
            for (EDCDTO.CalculationExpressionInfo calculationExpressionInfo : calcExpresInfoList) {
                dcItemName = calculationExpressionInfo.getItemName();
                waferPosStr = calculationExpressionInfo.getWaferPosition();
                if (!CimStringUtils.equals(waferPosStr, "*")) {
                    long waferPos = CimNumberUtils.longValue(waferPosStr);
                    if (waferPos > processedWaferCount) {
                        // this item doesn't require check
                        continue;
                    }
                }
                Infos.DataCollectionItemInfo tmpDCItem = getDCItem(checkDCItems, dcItemName);
                if (tmpDCItem == null || !CimStringUtils.equals(tmpDCItem.getItemType(), BizConstant.SP_DCDEF_ITEM_RAW)) {
                    //recursion invoke
                    List<Infos.DataCollectionItemInfo> dcItems = getRawItems(checkDCItems, tmpDCItem, processedWaferCount);
                    if (!CimArrayUtils.isEmpty(dcItems)) {
                        rawItems.addAll(dcItems);
                    }
                } else {
                    rawItems.add(tmpDCItem);
                }
            }
        }
        return rawItems;
    }

    private Infos.DataCollectionItemInfo getDCItem(List<Infos.DataCollectionItemInfo> checkDCItems, String dcItemName) {
        Infos.DataCollectionItemInfo dcItem = null;
        for (Infos.DataCollectionItemInfo checkDCItem : checkDCItems) {
            if (CimStringUtils.equals(checkDCItem.getDataCollectionItemName(), dcItemName)) {
                dcItem = checkDCItem;
                break;
            }
        }
        return dcItem;
    }

}
