package com.fa.cim.method.impl;

import com.fa.cim.annotaion.OmMethod;
import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.OmCode;
import com.fa.cim.common.utils.*;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.config.RetCodeConfigEx;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Inputs;
import com.fa.cim.dto.Outputs;
import com.fa.cim.dto.Results;
import com.fa.cim.entity.runtime.dcdef.CimDataCollectionDefDO;
import com.fa.cim.entity.runtime.dcdef.CimDataCollectionDefItemDO;
import com.fa.cim.jpa.CimJpaRepository;
import com.fa.cim.method.*;
import com.fa.cim.newcore.bo.dc.CimDataCollectionDefinition;
import com.fa.cim.newcore.bo.machine.CimMachine;
import com.fa.cim.newcore.bo.pd.CimProcessOperation;
import com.fa.cim.newcore.bo.product.CimLot;
import com.fa.cim.newcore.dto.dc.EDCDTO;
import com.fa.cim.newcore.dto.pd.ProcessDTO;
import com.fa.cim.newcore.dto.product.ProductDTO;
import com.fa.cim.newcore.exceptions.IllegalParameterException;
import com.fa.cim.newcore.exceptions.NotFoundRecordException;
import com.fa.cim.newcore.factory.BaseCoreFactory;
import com.fa.cim.newcore.impl.bo.env.StandardProperties;
import com.fa.cim.remote.IEAPRemoteManager;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;


/**
 * description:
 * <p>DataCollectionMethod .<br/></p>
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2018/10/12        ********             PlayBoy               create file
 *
 * @author PlayBoy
 * @since 2018/10/12 11:28
 * Copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@OmMethod
public class DataCollectionMethod implements IDataCollectionMethod {

    @Autowired
    private RetCodeConfig retCodeConfig;

    @Autowired
    private RetCodeConfigEx retCodeConfigEx;

    @Autowired
    private IPostProcessMethod postProcessMethod;

    @Autowired
    private CimJpaRepository cimJpaRepository;

    @Autowired
    private IControlJobMethod controlJobMethod;

    @Autowired
    private IEquipmentMethod equipmentMethod;

    @Autowired
    private ILogicalRecipeMethod logicalRecipeMethod;

    @Autowired
    private IProcessMethod processMethod;


    @Autowired
    private ILotMethod lotMethod;

    @Autowired
    private IEventMethod eventMethod;

    @Autowired
    private BaseCoreFactory baseCoreFactory;

    @Autowired
    private IEAPMethod eapMethod;

    @Override
    public Results.EDCDataItemWithTransitDataInqResult dataCollectionDefinitionFillInTxDCQ002DR(Infos.ObjCommon objCommon,
                                                                                                ObjectIdentifier equipmentID,
                                                                                                boolean processJobControlFlag,
                                                                                                ObjectIdentifier controlJobID) {
        Results.EDCDataItemWithTransitDataInqResult retVal = new Results.EDCDataItemWithTransitDataInqResult();

        //Set Output Values from Input Parameters without any changes
        retVal.setEquipmentID(equipmentID);
        retVal.setControlJobID(controlJobID);
        //Get controljob Object
        Outputs.ObjControlJobStartReserveInformationOut startReserveInformationOut =
                controlJobMethod.controlJobStartReserveInformationGet(objCommon,
                        controlJobID,
                        true);

        List<Infos.StartCassette> tmpStartCassette = startReserveInformationOut.getStartCassetteList();


        int nCastLen = 0;
        int nCastNum = 0;
        int nLotLen = 0;
        int nLotNum = 0;

        Map<String, Integer> srcIndexListByCJ = new HashMap<>();
        Map<String, Integer> srcIndexListByPJWS = new HashMap<>();
        Map<String, Integer> srcIndexListByPJPJ = new HashMap<>();
        String listKeyName = null;

        //--------------------------------------------------------------------------
        // Check whether need call TCS to get value
        //--------------------------------------------------------------------------
        boolean bTCSInqFlag = false;
        List<Infos.CollectedDataItem> strTCSInqDataItemSeq = new ArrayList<>();
        int nTCSInqDataItemCnt = 0;
        if (!ObjectIdentifier.equalsWithValue(BizConstant.SP_TCS_PERSON, objCommon.getUser().getUserID())) {
            //--------------------------------------------------------------------------
            // Get eqp on-line mode
            //--------------------------------------------------------------------------
            String equipmentOnlineModeOut = equipmentMethod.equipmentOnlineModeGet(objCommon, equipmentID);

            //------------------------------------------------------
            // Get Function Availability
            //------------------------------------------------------

            String pjCtrlFunc = StandardProperties.OM_PJ_CONTROL_ENABLE_FLAG.getValue();
            //--------------------------------------------------------------------------
            // Check eqp on-line mode
            //--------------------------------------------------------------------------
            if (!CimStringUtils.equals(equipmentOnlineModeOut, BizConstant.SP_EQP_ONLINEMODE_OFFLINE)
                    && CimStringUtils.equals(pjCtrlFunc, BizConstant.SP_FUNCTION_AVAILABLE_TRUE)) {
                bTCSInqFlag = true;
            }
        }

        boolean bSortFlag = false;
        nCastLen = CimArrayUtils.getSize(tmpStartCassette);
        for (nCastNum = 0; nCastNum < nCastLen; nCastNum++) {
            log.info("nCastNum : {}", nCastNum);
            nLotLen = CimArrayUtils.getSize(tmpStartCassette.get(nCastNum).getLotInCassetteList());
            for (nLotNum = 0; nLotNum < nLotLen; nLotNum++) {
                log.info("nLotNum ： {}", nLotNum);
                if (tmpStartCassette.get(nCastNum).getLotInCassetteList().get(nLotNum).getMoveInFlag()) {
                    log.info("operationStartFlag is TRUE");

                    ObjectIdentifier lotID = tmpStartCassette.get(nCastNum).getLotInCassetteList().get(nLotNum).getLotID();

                    //--------------------------------------------------------------------------
                    // Check the existence of the ByPJ retVal item
                    //--------------------------------------------------------------------------

                    boolean bExistByPJFlag = false;
                    String category = BizConstant.SP_DATACONDITIONCATEGORY_BYPJDATAITEM;
                    log.info("Call processOperation_DataCondition_GetDR()");
                    Outputs.ObjProcessOperationDataConditionGetDrOut processOperationDataConditionOut = processMethod.processOperationDataConditionGetDR(objCommon, lotID, category);

                    if (processOperationDataConditionOut != null && processOperationDataConditionOut.getCount() != null && 0 < processOperationDataConditionOut.getCount()) {
                        bExistByPJFlag = true;
                    }
                    //--------------------------------------------------------------------------
                    // Check of the retVal item is unnecessary
                    //--------------------------------------------------------------------------
                    if (!bTCSInqFlag && !bExistByPJFlag) {
                        continue;
                    }


                    List<ProcessDTO.PosProcessWafer> strWaferList = new ArrayList<>();
                    List<ProcessDTO.PosProcessWafer> strPJList = new ArrayList<>();

                    if (processJobControlFlag && bExistByPJFlag) {
                        //--------------------------------------------------------------------------
                        // Get lot Object
                        //--------------------------------------------------------------------------
                        CimLot lot = baseCoreFactory.getBO(CimLot.class, lotID);
                        Validations.check(null == lot, retCodeConfig.getNotFoundLot());

                        //--------------------------------------------------------------------------
                        // Get Process Operation Object
                        //--------------------------------------------------------------------------
                        CimProcessOperation po = lot.getProcessOperation();
                        Validations.check(null == po, new OmCode(retCodeConfig.getNotFoundProcessOperation(), "*****", lotID.getValue()));

                        //---------------------------------------------------------------------------------
                        //   Create wafer and ProcessJob list for return items
                        //---------------------------------------------------------------------------------
                        List<ProcessDTO.PosProcessWafer> strPWaferSeq = po.getProcessWafers();
                        int nPWaferLen = CimArrayUtils.getSize(strPWaferSeq);
                        if (nPWaferLen > 0) {
                            int nPJListCnt = 0;
                            for (int nPWaferNum = 0; nPWaferNum < nPWaferLen; nPWaferNum++) {
                                strWaferList.add(strPWaferSeq.get(nPWaferNum));
                                boolean bPJExistFlag = false;
                                for (int nPJListNum = 0; nPJListNum < nPJListCnt; nPJListNum++) {
                                    if (CimStringUtils.equals(strPWaferSeq.get(nPWaferNum).getPrcsJob(), strPJList.get(nPJListNum).getPrcsJob())) {
                                        bPJExistFlag = true;
                                        break;
                                    }
                                }
                                if (!bPJExistFlag) {
                                    strPJList.add(strPWaferSeq.get(nPWaferNum));
                                    nPJListCnt++;
                                }
                            }
                        } else {
                            Inputs.ObjLotWaferIDListGetDRIn objLotWaferIDListGetDRIn = new Inputs.ObjLotWaferIDListGetDRIn();
                            objLotWaferIDListGetDRIn.setLotID(lotID);
                            objLotWaferIDListGetDRIn.setScrapCheckFlag(true);
                            List<ObjectIdentifier> waferIDs = lotMethod.lotWaferIDListGetDR(objCommon, objLotWaferIDListGetDRIn);

                            nPWaferLen = CimArrayUtils.getSize(waferIDs);
                            if (0 < nPWaferLen) {
                                for (ObjectIdentifier waferID : waferIDs) {
                                    ProcessDTO.PosProcessWafer processWafer = new ProcessDTO.PosProcessWafer();
                                    processWafer.setWaferID(waferID.getValue());
                                    processWafer.setPrcsJob("");
                                    processWafer.setPrcsJobPosition("");
                                    strWaferList.add(processWafer);
                                }
                            }
                        }
                    }

                    //--------------------------------------------------------------------------
                    // Check retVal items
                    //--------------------------------------------------------------------------
                    int nDCDefLen = CimArrayUtils.getSize(tmpStartCassette.get(nCastNum).getLotInCassetteList().get(nLotNum).getStartRecipe().getDcDefList());
                    for (int nDCDefNum = 0; nDCDefNum < nDCDefLen; nDCDefNum++) {
                        Infos.DataCollectionInfo strDCDef = tmpStartCassette.get(nCastNum).getLotInCassetteList().get(nLotNum).getStartRecipe().getDcDefList().get(nDCDefNum);
                        int nDCItemLen = CimArrayUtils.getSize(strDCDef.getDcItems());

                        List<Infos.DataCollectionItemInfo> strEnabledDataItemSeq = new ArrayList<>();
                        int nEnabledDataItemCnt = 0;
                        List<Infos.DataCollectionItemInfo> strByCJDataItemSeq = new ArrayList<>();
                        int nByCJDataItemCnt = 0;
                        List<Infos.CollectedDataItem> strByPJDataItemSeq = new ArrayList<>();
                        int nByPJDataItemCnt = 0;
                        List<Integer> strByPJDataItemSrcIndexSeq = new ArrayList<>();


                        for (int nDCItemNum = 0; nDCItemNum < nDCItemLen; nDCItemNum++) {
                            Infos.DataCollectionItemInfo strDCItem = strDCDef.getDcItems().get(nDCItemNum);
                            if (CimArrayUtils.generateList(BizConstant.SP_DCDEF_MEAS_PJWAFER,
                                    BizConstant.SP_DCDEF_MEAS_PJWAFERSITE,
                                    BizConstant.SP_DCDEF_MEAS_PJ).contains(strDCItem.getMeasurementType())) {
                                if (!processJobControlFlag) {
                                    continue;
                                }

                                List<ProcessDTO.PosProcessWafer> pPJWaferList;
                                if (CimStringUtils.equals(BizConstant.SP_DCDEF_MEAS_PJ, strDCItem.getMeasurementType())) {
                                    pPJWaferList = strPJList;
                                } else {
                                    pPJWaferList = strWaferList;
                                }
                                if (CimArrayUtils.isEmpty(pPJWaferList)) {
                                    continue;
                                }
                                strEnabledDataItemSeq.add(nEnabledDataItemCnt++, strDCItem);
                                String sDataValue = strDCItem.getDataValue();
                                if (CimStringUtils.isEmpty(strDCItem.getDataValue()) && !CimStringUtils.isEmpty(strDCItem.getSpecCheckResult())) {
                                    sDataValue = "*";
                                }

                                boolean bPJExistFlag = false;
                                for (int nByPJDataItemNum = 0; nByPJDataItemNum < nByPJDataItemCnt; nByPJDataItemNum++) {
                                    if (CimStringUtils.equals(strByPJDataItemSeq.get(nByPJDataItemNum).getDataCollectionItemName(), strDCItem.getDataCollectionItemName())
                                            && CimStringUtils.equals(strByPJDataItemSeq.get(nByPJDataItemNum).getSitePosition(), strDCItem.getSitePosition())
                                            && CimStringUtils.equals(strByPJDataItemSeq.get(nByPJDataItemNum).getMeasurementType(), strDCItem.getMeasurementType())) {
                                        bPJExistFlag = true;

                                        int seqLen = CimArrayUtils.getSize(strByPJDataItemSeq.get(nByPJDataItemNum).getCollectedDataList());
                                        for (int seqNo = 0; seqNo < seqLen; seqNo++) {
                                            if (CimStringUtils.equals(BizConstant.SP_DCDEF_MEAS_PJ, strDCItem.getMeasurementType())) {
                                                if (CimStringUtils.equals(strByPJDataItemSeq.get(nByPJDataItemNum).getCollectedDataList().get(seqNo).getProcessJobPosition(), strDCItem.getWaferPosition())) {
                                                    Infos.CollectedData collectedData = strByPJDataItemSeq.get(nByPJDataItemNum).getCollectedDataList().get(seqNo);
                                                    collectedData.setDataValue(sDataValue);
                                                }
                                            } else {
                                                if (ObjectIdentifier.equalsWithValue(strByPJDataItemSeq.get(nByPJDataItemNum).getCollectedDataList().get(seqNo).getWaferID(), strDCItem.getWaferID())) {
                                                    Infos.CollectedData collectedData = strByPJDataItemSeq.get(nByPJDataItemNum).getCollectedDataList().get(seqNo);
                                                    collectedData.setWaferPosition(strDCItem.getWaferPosition());
                                                    collectedData.setDataValue(sDataValue);
                                                }
                                            }
                                        }
                                        break;
                                    }
                                }  // nByPJDataItemNum loop end
                                if (!bPJExistFlag) {
                                    //--------------------------------------------------------------------------
                                    // Memorize index of StartCassetteItem
                                    //--------------------------------------------------------------------------
                                    strByPJDataItemSrcIndexSeq.add(nEnabledDataItemCnt - 1);

                                    //--------------------------------------------------------------------------
                                    // Create temporary retVal item list of all ByPJ
                                    //--------------------------------------------------------------------------
                                    Infos.CollectedDataItem collectedDataItem = new Infos.CollectedDataItem();
                                    collectedDataItem.setLotID(tmpStartCassette.get(nCastNum).getLotInCassetteList().get(nLotNum).getLotID());
                                    collectedDataItem.setDataCollectionDefinitionID(strDCDef.getDataCollectionDefinitionID());
                                    collectedDataItem.setDataCollectionItemName(strDCItem.getDataCollectionItemName());
                                    collectedDataItem.setDataCollectionMode(strDCItem.getDataCollectionMode());
                                    collectedDataItem.setDataType(strDCItem.getDataType());
                                    collectedDataItem.setItemType(strDCItem.getItemType());
                                    collectedDataItem.setMeasurementType(strDCItem.getMeasurementType());
                                    collectedDataItem.setSitePosition(strDCItem.getSitePosition());
                                    collectedDataItem.setEdcSettingType(strDCDef.getEdcSettingType());
                                    collectedDataItem.setWaferCount(strDCItem.getWaferCount());
                                    collectedDataItem.setSiteCount(strDCItem.getSiteCount());

                                    int seqLen = CimArrayUtils.getSize(pPJWaferList);
                                    List<Infos.CollectedData> collectedDatas = new ArrayList<>();
                                    for (int seqNo = 0; seqNo < seqLen; seqNo++) {
                                        Infos.CollectedData collectedData = new Infos.CollectedData();
                                        collectedData.setProcessJobID(pPJWaferList.get(seqNo).getPrcsJob());
                                        collectedData.setProcessJobPosition(pPJWaferList.get(seqNo).getPrcsJobPosition());
                                        if (CimStringUtils.equals(BizConstant.SP_DCDEF_MEAS_PJ, strDCItem.getMeasurementType())) {
                                            if (!CimStringUtils.equals(strDCItem.getWaferPosition(), "*")
                                                    && CimStringUtils.equals(strWaferList.get(seqNo).getPrcsJobPosition(), strDCItem.getWaferPosition())) {
                                                log.info("Reported Data Item");
                                                collectedData.setDataValue(sDataValue);
                                            }
                                        } else {
                                            log.info("PJ Wafer/Site type");
                                            collectedData.setWaferID(new ObjectIdentifier(pPJWaferList.get(seqNo).getWaferID(), ""));
                                            if (!CimStringUtils.equals(strDCItem.getWaferPosition(), "*")
                                                    && ObjectIdentifier.equalsWithValue(strWaferList.get(seqNo).getWaferID(), strDCItem.getWaferID())) {
                                                log.info("Reported Data Item");
                                                collectedData.setWaferPosition(strDCItem.getWaferPosition());
                                                collectedData.setDataValue(sDataValue);
                                            }
                                        }
                                        collectedDatas.add(collectedData);
                                    }
                                    collectedDataItem.setCollectedDataList(collectedDatas);
                                    strByPJDataItemSeq.add(collectedDataItem);
                                    nByPJDataItemCnt++;
                                }
                            } else {
                                strEnabledDataItemSeq.add(strDCItem);
                                nEnabledDataItemCnt++;
                                if (!processJobControlFlag && bExistByPJFlag) {
                                    log.info("TRUE == bExistByPJFlag");
                                    strByCJDataItemSeq.add(strDCItem);
                                }

                                if (0 != nDCDefNum) {
                                    continue;
                                }

                                if (bTCSInqFlag && CimStringUtils.isEmpty(strDCItem.getDataValue())) {
                                    log.info("TRUE == bTCSInqFlag && 0 == CIMFWStrLen( strDCItem.dataValue )");

                                    //--------------------------------------------------------------------------
                                    // Add retVal item for inquiry to TCS
                                    //--------------------------------------------------------------------------
                                    Infos.CollectedDataItem collectedDataItem = new Infos.CollectedDataItem();
                                    collectedDataItem.setLotID(tmpStartCassette.get(nCastNum).getLotInCassetteList().get(nLotNum).getLotID());
                                    collectedDataItem.setDataCollectionDefinitionID(strDCDef.getDataCollectionDefinitionID());
                                    collectedDataItem.setDataCollectionItemName(strDCItem.getDataCollectionItemName());
                                    collectedDataItem.setDataCollectionMode(strDCItem.getDataCollectionMode());
                                    collectedDataItem.setDataType(strDCItem.getDataType());
                                    collectedDataItem.setItemType(strDCItem.getItemType());
                                    collectedDataItem.setMeasurementType(strDCItem.getMeasurementType());
                                    collectedDataItem.setSitePosition(strDCItem.getSitePosition());
                                    collectedDataItem.setEdcSettingType(strDCDef.getEdcSettingType());
                                    collectedDataItem.setWaferCount(strDCItem.getWaferCount());
                                    collectedDataItem.setSiteCount(strDCItem.getSiteCount());

                                    List<Infos.CollectedData> collectedDataList = new ArrayList<>();
                                    Infos.CollectedData collectedData = new Infos.CollectedData();
                                    collectedData.setWaferID(strDCItem.getWaferID());
                                    collectedData.setWaferPosition(strDCItem.getWaferPosition());
                                    collectedDataList.add(collectedData);
                                    if (CimArrayUtils.isNotEmpty(collectedDataItem.getCollectedDataList())){
                                        collectedDataItem.getCollectedDataList().clear();
                                    }
                                    collectedDataItem.setCollectedDataList(collectedDataList);
                                    strTCSInqDataItemSeq.add(nTCSInqDataItemCnt,collectedDataItem);
                                    nTCSInqDataItemCnt++;

                                    //--------------------------------------------------------------------------
                                    // Memorize index of StartCassetteItem to ByCJ list
                                    //--------------------------------------------------------------------------
                                    listKeyName = String.format("%s#%s#%s#%s#%s", tmpStartCassette.get(nCastNum).getLotInCassetteList().get(nLotNum).getLotID(), strDCItem.getDataCollectionItemName(),
                                            strDCItem.getMeasurementType(),
                                            CimStringUtils.getValueOrEmptyString(strDCItem.getWaferPosition()),
                                            CimStringUtils.getValueOrEmptyString(strDCItem.getSitePosition()));
                                    if (!srcIndexListByCJ.containsKey(listKeyName)) {
                                        srcIndexListByCJ.put((listKeyName), nDCItemNum);
                                    }
                                }
                            }
                        }  // nDCItemNum loop end

                        if (bExistByPJFlag) {
                            log.info("TRUE == bExistByPJFlag");
                            if (!processJobControlFlag) {
                                strDCDef.setDcItems(strByCJDataItemSeq);
                            } else {
                                log.info("TRUE == collectPJItemFlag");
                                if (CimArrayUtils.getSize(strDCDef.getDcItems()) != CimArrayUtils.getSize(strEnabledDataItemSeq)) {
                                    strDCDef.setDcItems(strEnabledDataItemSeq);
                                }

                                int nStartCassetteDCItemCnt = CimArrayUtils.getSize(strDCDef.getDcItems());

                                //--------------------------------------------------------------------------
                                // Check unreported PJ retVal item
                                //--------------------------------------------------------------------------
                                nByPJDataItemCnt = CimArrayUtils.getSize(strByPJDataItemSeq);
                                for (int nByPJDataItemNum = 0; nByPJDataItemNum < nByPJDataItemCnt; nByPJDataItemNum++) {
                                    log.info("nByPJDataItemNum : {}", nByPJDataItemNum);
                                    int seqLen = CimArrayUtils.getSize(strByPJDataItemSeq.get(nByPJDataItemNum).getCollectedDataList());

                                    List<Infos.CollectedData> strUnreportRawDataSeq = new ArrayList<>();
                                    int nUnreportRawDataCnt = 0;
                                    for (int seqNo = 0; seqNo < seqLen; seqNo++) {
                                        log.info("sequenceNumber : {}", seqNo);

                                        //--------------------------------------------------------------------------
                                        // Unreported PJ retVal item
                                        //--------------------------------------------------------------------------
                                        if (null == strByPJDataItemSeq.get(nByPJDataItemNum).getCollectedDataList().get(seqNo).getDataValue()) {
                                            log.info("0 == CIMFWStrLen( strByPJDataItemSeq[nByPJDataItemNum].strCollectedDataSeq[sequenceNumber].dataValue )");

                                            //--------------------------------------------------------------------------
                                            // Merge Unreport item and StartCassette item
                                            //--------------------------------------------------------------------------
                                            int sourceIndex = strByPJDataItemSrcIndexSeq.get(nByPJDataItemNum);

                                            if (0 == seqNo && CimArrayUtils.isNotEmpty(strDCDef.getDcItems()) && CimStringUtils.equals(strDCDef.getDcItems().get(sourceIndex).getWaferPosition(), "*")) {
                                                log.info("All PJ retVal is unreported");
                                                //--------------------------------------------------------------------------
                                                // All PJ retVal is unreported
                                                //--------------------------------------------------------------------------
                                                if (CimStringUtils.equals(BizConstant.SP_DCDEF_MEAS_PJ, strDCDef.getDcItems().get(sourceIndex).getMeasurementType())) {
                                                    Infos.DataCollectionItemInfo dataCollectionItemInfo = strDCDef.getDcItems().get(sourceIndex);
                                                    dataCollectionItemInfo.setWaferPosition(strByPJDataItemSeq.get(nByPJDataItemNum).getCollectedDataList().get(seqNo).getProcessJobPosition());
                                                } else {
                                                    Infos.DataCollectionItemInfo dataCollectionItemInfo = strDCDef.getDcItems().get(sourceIndex);
                                                    dataCollectionItemInfo.setWaferID(strByPJDataItemSeq.get(nByPJDataItemNum).getCollectedDataList().get(seqNo).getWaferID());
                                                    dataCollectionItemInfo.setWaferPosition(strByPJDataItemSeq.get(nByPJDataItemNum).getCollectedDataList().get(seqNo).getWaferPosition());
                                                }
                                            } else {
                                                //--------------------------------------------------------------------------
                                                // Some PJ retVal is reported
                                                //--------------------------------------------------------------------------
                                                Infos.DataCollectionItemInfo dataCollectionItemInfo = new Infos.DataCollectionItemInfo();
                                                BeanUtils.copyProperties(strDCDef.getDcItems().get(sourceIndex), dataCollectionItemInfo);
                                                strDCDef.getDcItems().add(dataCollectionItemInfo);
                                                dataCollectionItemInfo.setWaferID(strByPJDataItemSeq.get(nByPJDataItemNum).getCollectedDataList().get(seqNo).getWaferID());
                                                dataCollectionItemInfo.setDataValue(strByPJDataItemSeq.get(nByPJDataItemNum).getCollectedDataList().get(seqNo).getDataValue());
                                                dataCollectionItemInfo.setActionCodes(null);
                                                if (CimStringUtils.equals(BizConstant.SP_DCDEF_MEAS_PJ, strByPJDataItemSeq.get(nByPJDataItemNum).getMeasurementType())) {
                                                    dataCollectionItemInfo.setWaferPosition(strByPJDataItemSeq.get(nByPJDataItemNum).getCollectedDataList().get(seqNo).getProcessJobPosition());
                                                } else {
                                                    dataCollectionItemInfo.setWaferPosition(strByPJDataItemSeq.get(nByPJDataItemNum).getCollectedDataList().get(seqNo).getWaferPosition());
                                                }
                                                dataCollectionItemInfo.setDataCollectionItemName(strDCDef.getDcItems().get(sourceIndex).getDataCollectionItemName());
                                                dataCollectionItemInfo.setSpecCheckResult(strDCDef.getDcItems().get(sourceIndex).getSpecCheckResult());
                                                dataCollectionItemInfo.setTargetValue(strDCDef.getDcItems().get(sourceIndex).getTargetValue());
                                                dataCollectionItemInfo.setMeasurementType(strDCDef.getDcItems().get(sourceIndex).getMeasurementType());
                                                dataCollectionItemInfo.setSitePosition(strDCDef.getDcItems().get(sourceIndex).getSitePosition());
                                                dataCollectionItemInfo.setCalculationExpression(strDCDef.getDcItems().get(sourceIndex).getCalculationExpression());
                                                dataCollectionItemInfo.setDataType(strDCDef.getDcItems().get(sourceIndex).getDataType());
                                                dataCollectionItemInfo.setCalculationType(strDCDef.getDcItems().get(sourceIndex).getCalculationType());
                                                dataCollectionItemInfo.setItemType(strDCDef.getDcItems().get(sourceIndex).getItemType());
                                                dataCollectionItemInfo.setDataCollectionMode(strDCDef.getDcItems().get(sourceIndex).getDataCollectionMode());
                                                dataCollectionItemInfo.setDataCollectionUnit(strDCDef.getDcItems().get(sourceIndex).getDataCollectionUnit());
                                                dataCollectionItemInfo.setHistoryRequiredFlag(strDCDef.getDcItems().get(sourceIndex).getHistoryRequiredFlag());

                                                sourceIndex = nStartCassetteDCItemCnt;
                                                nStartCassetteDCItemCnt++;
                                                bSortFlag = true;
                                            }

                                            if (bTCSInqFlag && 0 == nDCDefNum) {
                                                if (CimStringUtils.equals(BizConstant.SP_DCDEF_MEAS_PJ, strByPJDataItemSeq.get(nByPJDataItemNum).getMeasurementType())) {
                                                    //--------------------------------------------------------------------------
                                                    // Memorize index of StartCassetteItem to ByPJ ProcessJon list
                                                    //--------------------------------------------------------------------------
                                                    listKeyName = String.format("%s#%s#%s#%s", strByPJDataItemSeq.get(nByPJDataItemNum).getLotID().getValue(), strByPJDataItemSeq.get(nByPJDataItemNum).getDataCollectionItemName(),
                                                            strByPJDataItemSeq.get(nByPJDataItemNum).getMeasurementType(),
                                                            CimStringUtils.getValueOrEmptyString(strByPJDataItemSeq.get(nByPJDataItemNum).getCollectedDataList().get(seqNo).getProcessJobPosition()));
                                                    if (!srcIndexListByPJPJ.containsKey(listKeyName)) {
                                                        srcIndexListByPJPJ.put(listKeyName, sourceIndex);
                                                    }
                                                } else {
                                                    //--------------------------------------------------------------------------
                                                    // Memorize index of StartCassetteItem to ByPJ Wafer/Site list
                                                    //--------------------------------------------------------------------------
                                                    listKeyName = String.format("%s#%s#%s#%s#%s", strByPJDataItemSeq.get(nByPJDataItemNum).getLotID().getValue(), strByPJDataItemSeq.get(nByPJDataItemNum).getDataCollectionItemName(), strByPJDataItemSeq.get(nByPJDataItemNum).getMeasurementType(),
                                                            CimStringUtils.getValueOrEmptyString(strByPJDataItemSeq.get(nByPJDataItemNum).getSitePosition()),
                                                            ObjectIdentifier.fetchValue(strByPJDataItemSeq.get(nByPJDataItemNum).getCollectedDataList().get(seqNo).getWaferID()));
                                                    if (!srcIndexListByPJWS.containsKey(listKeyName)) {
                                                        srcIndexListByPJWS.put(listKeyName, sourceIndex);
                                                    }
                                                }
                                                strUnreportRawDataSeq.add(nUnreportRawDataCnt,strByPJDataItemSeq.get(nByPJDataItemNum).getCollectedDataList().get(seqNo));
                                                nUnreportRawDataCnt++;
                                            }
                                        }
                                    }  // sequenceNumber loop end


                                    if (bTCSInqFlag) {
                                        //--------------------------------------------------------------------------
                                        // Exist unreported PJ raw retVal item
                                        //--------------------------------------------------------------------------
                                        if (0 < nUnreportRawDataCnt) {

                                            //--------------------------------------------------------------------------
                                            // Add retVal item for inquiry to TCS
                                            //--------------------------------------------------------------------------
                                            strTCSInqDataItemSeq.add(strByPJDataItemSeq.get(nByPJDataItemNum));
                                            Infos.CollectedDataItem collectedDataItem = strTCSInqDataItemSeq.get(nTCSInqDataItemCnt);
                                            collectedDataItem.setCollectedDataList(strUnreportRawDataSeq);
                                            nTCSInqDataItemCnt++;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }


        //--------------------------------------------------------------------------
        // Get retVal items which need TCS I/F. (Value is blank)
        //--------------------------------------------------------------------------
        if (0 < CimArrayUtils.getSize(strTCSInqDataItemSeq)) {
            List<Infos.CollectedDataItem> eapOut = new ArrayList<>();
            Long sleepTimeValue = CimStringUtils.isEmpty(StandardProperties.OM_EAP_CONNECT_SLEEP_TIME.getValue()) ?
                    BizConstant.SP_DEFAULT_SLEEP_TIME_TCS : StandardProperties.OM_EAP_CONNECT_SLEEP_TIME.getLongValue();

            Long retryCountValue = CimStringUtils.isEmpty(StandardProperties.OM_EAP_CONNECT_RETRY_COUNT.getValue()) ?
                    BizConstant.SP_DEFAULT_RETRY_COUNT_TCS : StandardProperties.OM_EAP_CONNECT_SLEEP_TIME.getLongValue();

            for (int retryNum = 0; retryNum < (retryCountValue + 1); retryNum++) {
                /*--------------------------*/
                /*    Send Request to EAP   */
                /*--------------------------*/
                IEAPRemoteManager eapRemoteManager = eapMethod.eapRemoteManager(objCommon,
                        objCommon.getUser(),
                        equipmentID,
                        null,
                        false);
                if (null == eapRemoteManager) {
                    if (log.isDebugEnabled()){
                        log.debug("MES not configure EAP host");
                    }
                    break;
                }
                Infos.SendEDCDataItemWithTransitDataInqInParm sendEAPParam =
                        new Infos.SendEDCDataItemWithTransitDataInqInParm();
                sendEAPParam.setEquipmentID(equipmentID);
                sendEAPParam.setControlJobID(controlJobID);
                sendEAPParam.setCollectedDataItemList(strTCSInqDataItemSeq);
                Object out = null;
                try {
                    // TODO: 2021/9/7 todo add EAP support
//                    out = eapRemoteManager.sendEDCDataItemWithTransitDataInq(sendEAPParam);
//                    eapOut = JSON.parseArray(out.toString(),Infos.CollectedDataItem.class);
                    break;
                } catch (ServiceException ex) {
                    if (Validations.isEquals(ex.getCode(), retCodeConfig.getTcsNoResponse())) {
                        if (log.isDebugEnabled()){
                            log.debug("{} EAP subsystem has return NO_RESPONSE!just retry now!!  now count.", retryNum);
                            log.debug("{} now sleeping.", sleepTimeValue);
                        }
                        if (retryNum != retryCountValue){
                            try {
                                Thread.sleep(sleepTimeValue);
                                continue;
                            } catch (InterruptedException e) {
                                ex.addSuppressed(e);
                                Thread.currentThread().interrupt();
                                throw ex;
                            }
                        }else {
                            Validations.check(true,retCodeConfig.getTcsNoResponse());
                        }
                    } else {
                        Validations.check(true,new OmCode((int) ex.getCode(),ex.getMessage()));
                    }
                }
            }

            String prvLotID = "";
            nCastNum = 0;
            nLotNum = 0;
            //--------------------------------------------------------------------------
            // Set dataValue get from EAP
            //--------------------------------------------------------------------------
            int nGetDataItemLen = CimArrayUtils.getSize(eapOut);
            for (int nGetDataItemNum = 0; nGetDataItemNum < nGetDataItemLen; nGetDataItemNum++) {
                Infos.CollectedDataItem strCollectedDataItem = eapOut.get(nGetDataItemNum);
                if (ObjectIdentifier.equalsWithValue(strCollectedDataItem.getLotID(), prvLotID)) {
                    boolean bLotFoundFlag = false;
                    for (nCastNum = 0; nCastNum < nCastLen; nCastNum++) {
                        nLotLen = CimArrayUtils.getSize(tmpStartCassette.get(nCastNum).getLotInCassetteList());
                        for (nLotNum = 0; nLotNum < nLotLen; nLotNum++) {
                            if (ObjectIdentifier.equalsWithValue(tmpStartCassette.get(nCastNum).getLotInCassetteList().get(nLotNum).getLotID(),
                                    strCollectedDataItem.getLotID())) {
                                bLotFoundFlag = true;
                                break;
                            }
                        }// nLotNum loop end
                        if (CimBooleanUtils.isTrue(bLotFoundFlag)) {
                            bLotFoundFlag = true;
                            break;
                        }
                    } // nCastNum loop end
                    if (CimBooleanUtils.isFalse(bLotFoundFlag)) {
                        log.info("Invalid LotID item!!");
                        continue;
                    }
                }
                prvLotID = strCollectedDataItem.getLotID().getValue();
                int seqLen = CimArrayUtils.getSize(strCollectedDataItem.getCollectedDataList());
                for (int seqNo = 0; seqNo < seqLen; seqNo++) {
                    int sourceIndex = 0;
                    if (CimStringUtils.equals(BizConstant.SP_DCDEF_MEAS_PJ, strCollectedDataItem.getMeasurementType())) {
                        log.info("PJ type");
                        //--------------------------------------------------------------------------
                        // Get index of StartCassetteItem from ByPJ ProcessJon list
                        //--------------------------------------------------------------------------
                        listKeyName = String.format("%s#%s#%s#%s", strCollectedDataItem.getLotID().getValue(), strCollectedDataItem.getDataCollectionItemName(),
                                strCollectedDataItem.getMeasurementType(), strCollectedDataItem.getCollectedDataList().get(seqNo).getProcessJobPosition());
                        if (!srcIndexListByPJPJ.containsKey(listKeyName)) {
                            log.info("Unknown item!!");
                            continue;
                        }
                        sourceIndex = srcIndexListByPJPJ.get(listKeyName);
                    } else if (CimStringUtils.equals(BizConstant.SP_DCDEF_MEAS_PJWAFER, strCollectedDataItem.getMeasurementType())
                            || CimStringUtils.equals(BizConstant.SP_DCDEF_MEAS_PJWAFERSITE, strCollectedDataItem.getMeasurementType())) {
                        log.info("PJ Wafer/Site type");
                        //--------------------------------------------------------------------------
                        // Get index of StartCassetteItem from ByPJ Wafer/Site list
                        //--------------------------------------------------------------------------
                        listKeyName = String.format("%s#%s#%s#%s#%s", strCollectedDataItem.getLotID().getValue(), strCollectedDataItem.getDataCollectionItemName(), strCollectedDataItem.getMeasurementType(),
                                CimStringUtils.getValueOrEmptyString(strCollectedDataItem.getSitePosition()),
                                ObjectIdentifier.fetchValue(strCollectedDataItem.getCollectedDataList().get(seqNo).getWaferID()));
                        if (!srcIndexListByPJWS.containsKey(listKeyName)) {
                            log.info("Unknown item!!");
                            continue;
                        }
                        sourceIndex = srcIndexListByPJWS.get(listKeyName);
                    } else {
                        log.info("CJ type");
                        //--------------------------------------------------------------------------
                        // Get index of StartCassetteItem from ByCJ list
                        //--------------------------------------------------------------------------
                        listKeyName = String.format("%s#%s#%s#%s#%s", strCollectedDataItem.getLotID(), strCollectedDataItem.getDataCollectionItemName(),
                                strCollectedDataItem.getMeasurementType(),
                                CimStringUtils.getValueOrEmptyString(strCollectedDataItem.getCollectedDataList().get(seqNo).getWaferPosition()),
                                CimStringUtils.getValueOrEmptyString(strCollectedDataItem.getSitePosition()));
                        if (!srcIndexListByCJ.containsKey(listKeyName)) {
                            log.info("Unknown item!!");
                            continue;
                        }
                        sourceIndex = srcIndexListByCJ.get(listKeyName);
                    }
                    List<Infos.DataCollectionItemInfo> strDCItemSeq = tmpStartCassette.get(nCastNum).getLotInCassetteList().get(nLotNum).getStartRecipe().getDcDefList().get(0).getDcItems();
                    if (sourceIndex >= CimArrayUtils.getSize(strDCItemSeq)) {
                        log.info("sourceIndex >= strDCItemSeq.length()");
                        Validations.check(true, retCodeConfig.getSystemError());
                    }
                    //--------------------------------------------------------------------------
                    // Update item retVal value
                    //--------------------------------------------------------------------------
                    strDCItemSeq.get(sourceIndex).setDataValue(strCollectedDataItem.getCollectedDataList().get(seqNo).getDataValue());
                    if (CimStringUtils.equals(BizConstant.SP_DCDEF_MEAS_PJWAFER, strCollectedDataItem.getMeasurementType())
                            || CimStringUtils.equals(BizConstant.SP_DCDEF_MEAS_PJWAFERSITE, strCollectedDataItem.getMeasurementType())) {
                        log.info("PJ Wafer/Site type");
                        strDCItemSeq.get(sourceIndex).setWaferPosition(strCollectedDataItem.getCollectedDataList().get(seqNo).getWaferPosition());
                    } else if (!CimStringUtils.equals(BizConstant.SP_DCDEF_MEAS_PJ, strCollectedDataItem.getMeasurementType())) {
                        log.info( "CJ type");
                        strDCItemSeq.get(sourceIndex).setWaferID(strCollectedDataItem.getCollectedDataList().get(seqNo).getWaferID());
                    }
                }
            }
        }

        if (bSortFlag) {
            for (nCastNum = 0; nCastNum < nCastLen; nCastNum++) {
                nLotLen = CimArrayUtils.getSize(tmpStartCassette.get(nCastNum).getLotInCassetteList());
                for (nLotNum = 0; nLotNum < nLotLen; nLotNum++) {
                    log.info("nLotLen/nLotNum : {}/{}", nLotLen, nLotNum);
                    final Infos.LotInCassette lotInCassette = tmpStartCassette.get(nCastNum).getLotInCassetteList().get(nLotNum);
                    if (null == lotInCassette.getStartRecipe()) {
                        continue;
                    }
                    int nDCDefLen = CimArrayUtils.getSize(lotInCassette.getStartRecipe().getDcDefList());
                    for (int nDCDefNum = 0; nDCDefNum < nDCDefLen; nDCDefNum++) {
                        log.info("nDCDefLen/nDCDefNum : {}/{}", nDCDefLen, nDCDefNum);
                        List<Infos.DataCollectionItemInfo> strDCItemSeq = lotInCassette.getStartRecipe().getDcDefList().get(nDCDefNum).getDcItems();
                        int nDCItemLen = CimArrayUtils.getSize(strDCItemSeq);

                        List<Infos.DataCollectionItemInfo> strSortDCItemSeq = new ArrayList<>();
                        int nSortDCItemSeqCnt = 0;
                        for (int nDCItemNum = 0; nDCItemNum < nDCItemLen; nDCItemNum++) {
                            if (null == strDCItemSeq.get(nDCItemNum).getDataCollectionItemName()) {
                                continue;
                            }
                            strSortDCItemSeq.add(nSortDCItemSeqCnt, strDCItemSeq.get(nDCItemNum));
                            if (CimArrayUtils.generateList(BizConstant.SP_DCDEF_MEAS_PJWAFER,
                                    BizConstant.SP_DCDEF_MEAS_PJWAFERSITE,
                                    BizConstant.SP_DCDEF_MEAS_PJ).contains(strDCItemSeq.get(nDCItemNum).getMeasurementType())) {
                                for (int nSubItemNum = nDCItemNum + 1; nSubItemNum < nDCItemLen; nSubItemNum++) {
                                    if (null == strDCItemSeq.get(nDCItemNum).getDataCollectionItemName()) {
                                        continue;
                                    }

                                    if (CimStringUtils.equals(strDCItemSeq.get(nDCItemNum).getDataCollectionItemName(), strDCItemSeq.get(nDCItemNum).getDataCollectionItemName())
                                            && CimStringUtils.equals(strDCItemSeq.get(nDCItemNum).getMeasurementType(), strDCItemSeq.get(nDCItemNum).getMeasurementType())
                                            && CimStringUtils.equals(strDCItemSeq.get(nDCItemNum).getSitePosition(), strDCItemSeq.get(nDCItemNum).getSitePosition())) {
//                                        strSortDCItemSeq.add(nSortDCItemSeqCnt++, strDCItemSeq.get(nDCItemNum));
                                    }
                                }
                            } // nSubItemNum loop end
                        } // nDCItemNum loop end

                        Infos.DataCollectionInfo dcDefList = lotInCassette.getStartRecipe().getDcDefList().get(nDCDefNum);
                        dcDefList.setDcItems(strSortDCItemSeq);
                    } // nDCDefNum loop end
                } // nLotNum loop end
            } // nCastNum loop end
        }


        //============================
        //===  P3000422 add start  ===
        //============================
        //--- set strStartCassette info ---
        retVal.setStartCassetteList(tmpStartCassette);

        //--- clear specCheckResult ---
        int cLen = CimArrayUtils.getSize(retVal.getStartCassetteList());
        for (int cCnt = 0; cCnt < cLen; cCnt++) {
            int lLen = CimArrayUtils.getSize(retVal.getStartCassetteList().get(cCnt).getLotInCassetteList());
            for (int lCnt = 0; lCnt < lLen; lCnt++) {
                if (retVal.getStartCassetteList().get(cCnt).getLotInCassetteList().get(lCnt).getMoveInFlag()) {
                    int dcLen = CimArrayUtils.getSize(retVal.getStartCassetteList().get(cCnt).getLotInCassetteList().get(lCnt).getStartRecipe().getDcDefList());
                    for (int dcCnt = 0; dcCnt < dcLen; dcCnt++) {
                        int diLen = CimArrayUtils.getSize(retVal.getStartCassetteList().get(cCnt).getLotInCassetteList().get(lCnt).getStartRecipe().getDcDefList().get(dcCnt).getDcItems());
                        for (int diCnt = 0; diCnt < diLen; diCnt++) {
                            Infos.DataCollectionItemInfo dataCollectionItemInfo = retVal.getStartCassetteList().get(cCnt).getLotInCassetteList().get(lCnt).getStartRecipe().getDcDefList().get(dcCnt).getDcItems().get(diCnt);
                            dataCollectionItemInfo.setSpecCheckResult("");
                        }
                    }
                }
            }
        }

        return retVal;
    }

    /**
     * description: dcDef_detailInfo_GetDR
     * <p><br/></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param dcDefID
     * @return com.fa.cim.dto.RetCode<com.fa.cim.dto.Results.EDCPlanInfoInqResult>
     * @author Ho
     * @date 2018/10/16 13:55:24
     */
    @Override
    public Results.EDCPlanInfoInqResult dcDefDetailInfoGetDR(Infos.ObjCommon objCommon, ObjectIdentifier dcDefID) {

        CimDataCollectionDefDO example = new CimDataCollectionDefDO();
        example.setDataCollectionDefID(dcDefID.getValue());
        CimDataCollectionDefDO dataCollectionDef = cimJpaRepository.findOne(Example.of(example)).orElse(null);
        Validations.check(null == dataCollectionDef, retCodeConfig.getNotFoundDcdef());

        Results.EDCPlanInfoInqResult strDcDef_detailInfo_GetDR_out = new Results.EDCPlanInfoInqResult();

        ObjectIdentifier _dcDefID = new ObjectIdentifier();
        strDcDef_detailInfo_GetDR_out.setDcDefID(_dcDefID);
        _dcDefID.setValue(dataCollectionDef.getDataCollectionDefID());
        _dcDefID.setReferenceKey(dataCollectionDef.getId());
        strDcDef_detailInfo_GetDR_out.setDcType(dataCollectionDef.getDcType());
        strDcDef_detailInfo_GetDR_out.setWhiteDefFlag(dataCollectionDef.getWhiteFlag());
        strDcDef_detailInfo_GetDR_out.setFpcCategory(dataCollectionDef.getFpcCategory());

        List<CimDataCollectionDefItemDO> dataCollectionDefItems = cimJpaRepository.query("SELECT NAME,\n" +
                "                   UNIT,\n" +
                "                   DATA_TYPE,\n" +
                "                   ITEM_TYPE,\n" +
                "                   EDCITEM_MODE,\n" +
                "                   MEAS_TYPE,\n" +
                "                   WAFER_POS,\n" +
                "                   SITE_POS,\n" +
                "                   CALC_TYPE,\n" +
                "                   CALC_EXPR\n" +
                "              FROM OMEDCPLAN_ITEM\n" +
                "             WHERE REFKEY = ?\n" +
                "             ORDER BY IDX_NO ASC",CimDataCollectionDefItemDO.class,dataCollectionDef.getId());

        List<Infos.DCItem> strDCItems = new ArrayList<>();

        strDcDef_detailInfo_GetDR_out.setStrDCItemList(strDCItems);

        for (CimDataCollectionDefItemDO dataCollectionDefItem : dataCollectionDefItems) {
            Infos.DCItem dcItem = new Infos.DCItem();
            strDCItems.add(dcItem);
            dcItem.setDataCollectionItemName(dataCollectionDefItem.getDcItemName());
            dcItem.setDataCollectionMode(dataCollectionDefItem.getDcItemMode());
            dcItem.setDataCollectionUnit(dataCollectionDefItem.getDcItemUnit());
            dcItem.setDataType(dataCollectionDefItem.getDcItemValType());
            dcItem.setItemType(dataCollectionDefItem.getDcItemType());
            dcItem.setMeasurementType(dataCollectionDefItem.getDcItemMeasType());
            dcItem.setWaferPosition(dataCollectionDefItem.getDcItemWaferPos());
            dcItem.setSitePosition(dataCollectionDefItem.getDcItemSitePos());
            dcItem.setCalculationType(dataCollectionDefItem.getDcItemCalType());
            dcItem.setCalculationExpression(dataCollectionDefItem.getDcItemCalExpr());
        }

        return strDcDef_detailInfo_GetDR_out;
    }

    /**
     * description: dcSpec_detailInfo_GetDR__101
     * <p><br/></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param dcSpecID
     * @return com.fa.cim.dto.RetCode<com.fa.cim.dto.Results.EDCSpecInfoInqResult>
     * @author Ho
     * @date 2018/10/16 16:18:51
     */
    @Override
    public Results.EDCSpecInfoInqResult dcSpecDetailInfoGetDR(Infos.ObjCommon objCommon, ObjectIdentifier dcSpecID) {

        String hFRDCSPECDCSPEC_ID = dcSpecID.getValue();

        Object[] dataCollectionSpec = cimJpaRepository.queryOne("SELECT ID,\n" +
                "                   EDC_SPEC_ID,\n" +
                "                   DESCRIPTION,\n" +
                "                   TEMP_MODE_FLAG,\n" +
                "                   DOC_CATEGORY\n" +
                "              FROM OMEDCSPEC\n" +
                "             WHERE EDC_SPEC_ID = ?",hFRDCSPECDCSPEC_ID);
        Validations.check(null == dataCollectionSpec, retCodeConfig.getNotFoundDcspec());

        Results.EDCSpecInfoInqResult strDcSpec_detailInfo_GetDR_out = new Results.EDCSpecInfoInqResult();
        ObjectIdentifier _dcSpecID = new ObjectIdentifier();
        strDcSpec_detailInfo_GetDR_out.setDcSpecID(_dcSpecID);
        _dcSpecID.setValue(hFRDCSPECDCSPEC_ID);
        _dcSpecID.setReferenceKey(CimObjectUtils.toString(dataCollectionSpec[0]));
        strDcSpec_detailInfo_GetDR_out.setDescription(CimObjectUtils.toString(dataCollectionSpec[2]));
        strDcSpec_detailInfo_GetDR_out.setWhiteDefFlag(CimBooleanUtils.convert(dataCollectionSpec[3]));
        strDcSpec_detailInfo_GetDR_out.setFpcCategory(CimObjectUtils.toString(dataCollectionSpec[4]));

        List<Object[]> dataCollectionSpecItemList = cimJpaRepository.query("SELECT SPEC_ITEM_NAME\n" +
                "                                       ,SCRN_UP_FLAG\n" +
                "                                       ,SCRN_UP_LIMIT\n" +
                "                                       ,SCRN_UP_ACT\n" +
                "                                       ,SCRN_LO_FLAG\n" +
                "                                       ,SCRN_LO_LIMIT\n" +
                "                                       ,SCRN_LO_ACT\n" +
                "                                       ,SPEC_UP_FLAG\n" +
                "                                       ,SPEC_UP_LIMIT\n" +
                "                                       ,SPEC_UP_ACT\n" +
                "                                       ,SPEC_LO_FLAG\n" +
                "                                       ,SPEC_LO_LIMIT\n" +
                "                                       ,SPEC_LO_ACT\n" +
                "                                       ,CTRL_UP_FLAG\n" +
                "                                       ,CTRL_UP_LIMIT\n" +
                "                                       ,CTRL_UP_ACT\n" +
                "                                       ,CTRL_LO_FLAG\n" +
                "                                       ,CTRL_LO_LIMIT\n" +
                "                                       ,CTRL_LO_ACT\n" +
                "                                       ,SPEC_ITEM_TARGET\n" +
                "                                       ,SPEC_ITEM_TAG\n" +
                "                                       ,EDC_SPEC_GROUP\n" +
                "                                 FROM OMEDCSPEC_ITEM\n" +
                "                                 WHERE REFKEY = ?",dataCollectionSpec[0]);

        List<Infos.DCSpecDetailInfo> strDCSpecs = new ArrayList<>();
        strDcSpec_detailInfo_GetDR_out.setStrDCSpecList(strDCSpecs);

        setDCSpecDetailInfo(dataCollectionSpecItemList, strDCSpecs);

        return strDcSpec_detailInfo_GetDR_out;
    }

    @Override
    public Outputs.ObjDataCollectionItemFillInEDCDataItemListByKeyInqOut dataCollectionItemFillInTxDCQ011DR(Infos.ObjCommon objCommon, String searchKeyPattern, List<Infos.HashedInfo> searchKeys) {
        Outputs.ObjDataCollectionItemFillInEDCDataItemListByKeyInqOut data = new Outputs.ObjDataCollectionItemFillInEDCDataItemListByKeyInqOut();

        AtomicBoolean routeOpeFlag = new AtomicBoolean(true);
        AtomicReference<String> operationNumber = new AtomicReference<>("");
        AtomicReference<String> routeID = new AtomicReference<>("");
        AtomicReference<String> productID = new AtomicReference<>("");
        AtomicReference<String> productGroupID = new AtomicReference<>("");
        AtomicReference<String> technologyID = new AtomicReference<>("");
        AtomicReference<String> versionID = new AtomicReference<>("");
        AtomicReference<String> dcDefID = new AtomicReference<>("");
        AtomicReference<String> dcSpecID = new AtomicReference<>("");
        AtomicReference<String> lotID = new AtomicReference<>("");
        AtomicReference<String> dataItemName = new AtomicReference<>("");
        AtomicReference<String> dcSpecGroupID = new AtomicReference<>("");
        AtomicReference<String> recipeType = new AtomicReference<>("");

        ObjectIdentifier measurementDCSpecID = new ObjectIdentifier();
        List<Infos.DCDefDataItem> dcDefDataItems = new ArrayList<>();
        List<Infos.DCSpecDataItem> dcSpecDataItems = new ArrayList<>();
        data.setDcDefDataItems(dcDefDataItems);
        data.setDcSpecDataItems(dcSpecDataItems);

        Infos.OperationDataCollectionSetting operationDataCollectionSetting = new Infos.OperationDataCollectionSetting();
        data.setOperationDataCollectionSetting(operationDataCollectionSetting);
        List<Infos.CorrespondingOperationInfo> correspondingOperations = new ArrayList<>();
        List<Infos.DefaultRecipeWithDCID> defaultRecipeSettings = new ArrayList<>();
        List<Infos.EqpSpecificRecipeWithDCID> eqpSpecificRecipeSettings = new ArrayList<>();
        List<Infos.DeltaDCSetting> deltaDCSettings = new ArrayList<>();
        operationDataCollectionSetting.setCorrespondingOperations(correspondingOperations);
        operationDataCollectionSetting.setDefaultRecipeSettings(defaultRecipeSettings);
        operationDataCollectionSetting.setEqpSpecificRecipeSettings(eqpSpecificRecipeSettings);
        operationDataCollectionSetting.setDeltaDCSettings(deltaDCSettings);
        //Step1 - Check input parameters
        Map<String, Runnable> searchKeyPatternMapping = new HashMap<>();
        searchKeyPatternMapping.put(BizConstant.SP_DCITEM_SEARCHKEYPATTERN_DCDEF, () -> {
            routeOpeFlag.set(false);
            searchKeyPatternProcess(searchKeys,  new SearchKeyPatternParam(false, BizConstant.SP_DCITEM_SEARCHKEY_DCDEFID, dcDefID));
        });
        searchKeyPatternMapping.put(BizConstant.SP_DCITEM_SEARCHKEYPATTERN_DCSPEC, () -> {
            routeOpeFlag.set(false);
            searchKeyPatternProcess(searchKeys, new SearchKeyPatternParam(false, BizConstant.SP_DCITEM_SEARCHKEY_DCSPECID, dcSpecID));
        });
        searchKeyPatternMapping.put(BizConstant.SP_DCITEM_SEARCHKEYPATTERN_LOT_ROUTE_OPENO, () -> searchKeyPatternProcess(searchKeys,
                new SearchKeyPatternParam(false, BizConstant.SP_DCITEM_SEARCHKEY_LOTID, lotID),
                new SearchKeyPatternParam(false, BizConstant.SP_DCITEM_SEARCHKEY_ROUTEID, routeID),
                new SearchKeyPatternParam(false, BizConstant.SP_DCITEM_SEARCHKEY_OPENO, operationNumber)));
        searchKeyPatternMapping.put(BizConstant.SP_DCITEM_SEARCHKEYPATTERN_PROD_ROUTE_OPENO, () -> searchKeyPatternProcess(searchKeys,
                new SearchKeyPatternParam(false, BizConstant.SP_DCITEM_SEARCHKEY_PRODUCTID, productID),
                new SearchKeyPatternParam(false, BizConstant.SP_DCITEM_SEARCHKEY_ROUTEID, routeID),
                new SearchKeyPatternParam(false, BizConstant.SP_DCITEM_SEARCHKEY_OPENO, operationNumber)));
        searchKeyPatternMapping.put(BizConstant.SP_DCITEM_SEARCHKEYPATTERN_PRODGRP_ROUTE_OPENO, () -> searchKeyPatternProcess(searchKeys,
                new SearchKeyPatternParam(false, BizConstant.SP_DCITEM_SEARCHKEY_PRODUCTGROUPID, productGroupID),
                new SearchKeyPatternParam(false, BizConstant.SP_DCITEM_SEARCHKEY_ROUTEID, routeID),
                new SearchKeyPatternParam(false, BizConstant.SP_DCITEM_SEARCHKEY_OPENO, operationNumber)));
        searchKeyPatternMapping.put(BizConstant.SP_DCITEM_SEARCHKEYPATTERN_TECH_ROUTE_OPENO, () -> searchKeyPatternProcess(searchKeys,
                new SearchKeyPatternParam(false, BizConstant.SP_DCITEM_SEARCHKEY_TECHNOLOGYID, technologyID),
                new SearchKeyPatternParam(false, BizConstant.SP_DCITEM_SEARCHKEY_ROUTEID, routeID),
                new SearchKeyPatternParam(false, BizConstant.SP_DCITEM_SEARCHKEY_OPENO, operationNumber)));
        searchKeyPatternMapping.put(BizConstant.SP_DCITEM_SEARCHKEYPATTERN_ROUTE_OPENO, () -> searchKeyPatternProcess(searchKeys,
                new SearchKeyPatternParam(false, BizConstant.SP_DCITEM_SEARCHKEY_ROUTEID, routeID),
                new SearchKeyPatternParam(false, BizConstant.SP_DCITEM_SEARCHKEY_OPENO, operationNumber)));
        Validations.check(!searchKeyPatternMapping.containsKey(searchKeyPattern), retCodeConfig.getUnknownSearchKeyPattern());

        if (!CimArrayUtils.isEmpty(searchKeys)) {
            for (Infos.HashedInfo searchKey : searchKeys) {
                if (CimStringUtils.equals(searchKey.getHashKey(), BizConstant.SP_DCITEM_SEARCHKEY_DATAITEMNAME)) {
                    dataItemName.set(searchKey.getHashData());
                } else if (CimStringUtils.equals(searchKey.getHashKey(), BizConstant.SP_DCITEM_SEARCHKEY_DCSPECGROUPID)) {
                    dcSpecGroupID.set(searchKey.getHashData());
                }
            }
        }
        String pfMainPDId = null;
        String pdLevel = null;
        Boolean pfState = true;
        String posOpeNo = null;
        //Step2 - Specified route operation
        if (routeOpeFlag.get()) {
            pfMainPDId = routeID.get();
            //Get mainPOS
            String mainPOSKey = CimStringUtils.firstToString(cimJpaRepository.queryOne("SELECT pos.id，pf.PRP_ID FROM OMPRF pf, OMPRSS pos WHERE pf.PRP_ID = ?1 AND pf.PRP_LEVEL = ?2 AND pf.ACTIVE_FLAG = ?3 AND pos.PRF_RKEY = pf.id AND pos.OPE_NO = ?4",
                    pfMainPDId, BizConstant.SP_PD_FLOWLEVEL_MAIN_FOR_OPERATION, pfState, operationNumber.get()));
            pdLevel = BizConstant.SP_PD_FLOWLEVEL_MAIN_FOR_OPERATION;
            posOpeNo = operationNumber.get();
            //Get modulePDID
            String pdListModuleNo = BaseStaticMethod.convertOpeNoToModuleNo(posOpeNo);
            String modulePDID = CimStringUtils.firstToString(cimJpaRepository.queryOne("SELECT pdList.ROUTE_ID, pf.PRP_ID FROM OMPRF pf, OMPRF_ROUTESEQ pdList WHERE pf.PRP_ID = ?1 AND pf.PRP_LEVEL = ?2 AND pf.ACTIVE_FLAG = ?3 AND pdList.REFKEY = pf.ID AND pdList.ROUTE_NO = ?4",
                    pfMainPDId, BizConstant.SP_PD_FLOWLEVEL_MAIN_FOR_MODULE, pfState, pdListModuleNo));
            //If module PDID's version is "##", convert it to real PDID
            versionID.set(BaseStaticMethod.extractVersionFromID(modulePDID));
            if (CimStringUtils.equals(versionID.get(),BizConstant.SP_ACTIVE_VERSION)) {
                // Get active version of modulePD
                Object[] active=cimJpaRepository.queryOne("SELECT ACTIVE_VER_ID,ACTIVE_VER_RKEY \n" +
                        "                         FROM   OMPRP \n" +
                        "                         WHERE  PRP_ID    = ? \n" +
                        "                         AND    PRP_LEVEL = ?",modulePDID, pdLevel);
                ObjectIdentifier activeID = ObjectIdentifier.build(CimObjectUtils.toString(active[0]), CimObjectUtils.toString(active[1]));
                modulePDID = activeID.getValue();
            }
            //Get modulePOS and PD
            final int posIdIndex = 0;
            final int pdIDIndex = 1;
            final int pdObjIndex = 2;
            List<String> modulePOSAndPDData = CimArrayUtils.convertStringList(cimJpaRepository.queryOne("SELECT pos.id,pos.STEP_ID,pos.STEP_RKEY " +
                    "FROM OMPRF pf, OMPRSS pos " +
                    "WHERE pf.PRP_ID = ?1 " +
                    "AND pf.PRP_LEVEL = ?2 " +
                    "AND pf.ACTIVE_FLAG = ?3 " +
                    "AND pos.PRF_RKEY = pf.ID " +
                    "AND pos.OPE_NO = ?4 ",pfMainPDId, pdLevel, pfState, posOpeNo));
            String modulePOSKey = modulePOSAndPDData.get(posIdIndex);
            String pdObjValue = modulePOSAndPDData.get(pdObjIndex);
            //If operation PDID's version is "##", convert it to real PDID
            String operationPDID = modulePOSAndPDData.get(pdIDIndex);
            versionID.set(BaseStaticMethod.extractVersionFromID(operationPDID));
            if (CimStringUtils.equals(versionID.get(), BizConstant.SP_ACTIVE_VERSION)) {
                log.info("dataCollectionItemFillInEDCDataItemListByKeyInqDr(): operation PDID's version is ##");
                // Get active version object of PD
                pdLevel = BizConstant.SP_PD_FLOWLEVEL_OPERATION;
                Object[] active=cimJpaRepository.queryOne("SELECT ACTIVE_VER_ID,ACTIVE_VER_RKEY \n" +
                        "                         FROM   OMPRP \n" +
                        "                         WHERE  PRP_ID    = ? \n" +
                        "                         AND    PRP_LEVEL = ?",operationPDID, pdLevel);
                ObjectIdentifier activeID = ObjectIdentifier.build(CimObjectUtils.toString(active[0]), CimObjectUtils.toString(active[1]));
                pdObjValue = activeID.getValue();
            }
            // Get D_THESYSTEMKEY of PD from PDObj
            com.fa.cim.newcore.bo.pd.CimProcessDefinition pdOut = baseCoreFactory.getBO(com.fa.cim.newcore.bo.pd.CimProcessDefinition.class,pdObjValue);
            //Get Corresponding Operations
            String correspondingOpeModeEnv = StandardProperties.OM_EDC_MULTI_CORRESPOND_FLAG.getValue();
            if (!CimStringUtils.isEmpty(mainPOSKey)) {
                //Get all Corresponding Operations in mainPOS
                if (CimStringUtils.equals(correspondingOpeModeEnv, "1")) {
                    //Check record count of Multi Corresponding definition

                    long count = cimJpaRepository.count("SELECT COUNT(REFKEY)\n" +
                            "                             FROM   OMPRSS_MEASRELATED\n" +
                            "                             WHERE  REFKEY = ?",mainPOSKey);
                    if (count > 0) {
                        // Get Multi Corresponding definition
                        List<Infos.CorrespondingOperationInfo> multiCorrespondingDefinitionOut= new ArrayList<>();
                        List<Object[]> data1= cimJpaRepository.query("SELECT CORR_OPE_NO, EDC_SPEC_GROUP " +
                                "FROM OMPRSS_MEASRELATED " +
                                "WHERE REFKEY = ?1 " +
                                "ORDER BY IDX_NO",mainPOSKey);
                        if (CimArrayUtils.isEmpty(data1)) {
                            multiCorrespondingDefinitionOut=null;
                        }
                        for (Object[] item : data1) {
                            Infos.CorrespondingOperationInfo correspondingOperationInfo = new Infos.CorrespondingOperationInfo();
                            multiCorrespondingDefinitionOut.add(correspondingOperationInfo);
                            correspondingOperationInfo.setCorrespondingOperationNumber(CimObjectUtils.toString(item[0]));
                            correspondingOperationInfo.setDcSpecGroup(CimObjectUtils.toString(item[1]));
                        }
                        if (multiCorrespondingDefinitionOut != null) {
                            correspondingOperations.addAll(multiCorrespondingDefinitionOut);
                        }
                    }
                }
                if (CimArrayUtils.isEmpty(correspondingOperations)) {
                    com.fa.cim.newcore.bo.pd.CimProcessOperationSpecification posEntity =
                            baseCoreFactory.getBO(com.fa.cim.newcore.bo.pd.CimProcessOperationSpecification.class,mainPOSKey);
                    if (posEntity != null && !CimStringUtils.isEmpty(posEntity.getCorrespondingProcessOperationNumber())) {
                        Infos.CorrespondingOperationInfo correspondingOperationInfo = new Infos.CorrespondingOperationInfo();
                        correspondingOperations.add(correspondingOperationInfo);
                        correspondingOperationInfo.setCorrespondingOperationNumber(posEntity.getCorrespondingProcessOperationNumber());
                    }
                }
            }
            //Get all Corresponding Operations in modulePOS
            String correspondingMergeRuleEnv = StandardProperties.OM_EDC_MULTI_CORRESPOND_MULTI_MERGE_FLAG.getValue();
            log.info("dataCollectionItemFillInEDCDataItemListByKeyInqDr(): OM_EDC_MULTI_CORRESPOND_MULTI_MERGE_FLAG is {}", correspondingMergeRuleEnv);
            if (CimArrayUtils.isEmpty(correspondingOperations) || CimStringUtils.equals(correspondingMergeRuleEnv, "1")) {
                if (CimStringUtils.equals(correspondingOpeModeEnv, "1")) {
                    // Check record count of Multi Corresponding definition
                    long count = cimJpaRepository.count("SELECT COUNT(REFKEY) \n" +
                            "                             FROM   OMPRSS_MEASRELATED\n" +
                            "                             WHERE  REFKEY = ?",
                            mainPOSKey);
                    if (count > 0) {
                        // Get Multi Corresponding definition
                        List<Object[]> data1=cimJpaRepository.query("SELECT CORR_OPE_NO, EDC_SPEC_GROUP " +
                                "FROM OMPRSS_MEASRELATED " +
                                "WHERE REFKEY = ?1 " +
                                "ORDER BY IDX_NO",mainPOSKey);
                        List<Infos.CorrespondingOperationInfo> multiCorrespondingDefinitionOut=new ArrayList<>();
                        if (CimArrayUtils.isEmpty(data1)) {
                            multiCorrespondingDefinitionOut=null;
                        }
                        for (Object[] item : data1) {
                            Infos.CorrespondingOperationInfo correspondingOperationInfo = new Infos.CorrespondingOperationInfo();
                            multiCorrespondingDefinitionOut.add(correspondingOperationInfo);
                            correspondingOperationInfo.setCorrespondingOperationNumber(CimObjectUtils.toString(item[0]));
                            correspondingOperationInfo.setDcSpecGroup(CimObjectUtils.toString(item[1]));
                        }
                        String opeNo;
                        if (multiCorrespondingDefinitionOut != null) {
                            for (Infos.CorrespondingOperationInfo item : multiCorrespondingDefinitionOut) {
                                // Get operationNumber from moduleNumber and moduleOpeNumber(CORRESPOND_OPE_NO)
                                opeNo = BaseStaticMethod.convertModuleOpeNoToOpeNo(pdListModuleNo, item.getCorrespondingOperationNumber());
                                boolean bFoundSameDef = false;
                                for (Infos.CorrespondingOperationInfo correspondingOperation : correspondingOperations) {
                                    if (CimStringUtils.equals(opeNo, correspondingOperation.getCorrespondingOperationNumber())) {
                                        bFoundSameDef = true;
                                        break;
                                    }
                                }
                                if (!bFoundSameDef) {
                                    correspondingOperations.add(item);
                                }
                            }
                        }
                    }
                }
            }
            //Get logicalRecipe
            ObjectIdentifier logicalRecipe = new ObjectIdentifier();
            if (CimStringUtils.equals(searchKeyPattern, BizConstant.SP_DCITEM_SEARCHKEYPATTERN_LOT_ROUTE_OPENO)) {
                // Get ProductID
                Object[] one=cimJpaRepository.queryOne("SELECT PROD_ID, LOT_ID\n" +
                        "                         FROM   OMLOT\n" +
                        "                         WHERE  LOT_ID = ?",lotID.get());
                if (one != null) {
                    productID.set(CimObjectUtils.toString(one[0]));
                }
            }
            // Get logicalRecipe by Product
            if (CimStringUtils.equals(searchKeyPattern, BizConstant.SP_DCITEM_SEARCHKEYPATTERN_LOT_ROUTE_OPENO)
                    || CimStringUtils.equals(searchKeyPattern, BizConstant.SP_DCITEM_SEARCHKEYPATTERN_PROD_ROUTE_OPENO)) {
                log.info("dataCollectionItemFillInEDCDataItemListByKeyInqDr(): Get logicalRecipe by Product");
                List<String> outData = CimArrayUtils.convertStringList(cimJpaRepository.queryOne("SELECT LRCP_ID,LRCP_RKEY FROM OMPRP_LRPRD WHERE  REFKEY = ?1 AND PROD_ID = ?2",
                        pdOut.getPrimaryKey(), productID.get()));
                if (CimArrayUtils.isEmpty(outData)) {
                    // Get ProductGroupID
                    Object[] one=cimJpaRepository.queryOne("SELECT PRODFMLY_ID,PROD_ID\n" +
                            "                             FROM   OMPRODINFO\n" +
                            "                             WHERE  PROD_ID = ?",productID.get());
                    productGroupID.set(CimObjectUtils.toString(one[0]));
                } else {
                    logicalRecipe.setValue(outData.get(0));
                    logicalRecipe.setReferenceKey(outData.get(1));
                }
            }

            // Get logicalRecipe by productgroup
            boolean searchKeyPatternFlag4ProductGroup = CimStringUtils.equals(searchKeyPattern, BizConstant.SP_DCITEM_SEARCHKEYPATTERN_LOT_ROUTE_OPENO)
                    || CimStringUtils.equals(searchKeyPattern, BizConstant.SP_DCITEM_SEARCHKEYPATTERN_PROD_ROUTE_OPENO)
                    || CimStringUtils.equals(searchKeyPattern, BizConstant.SP_DCITEM_SEARCHKEYPATTERN_PRODGRP_ROUTE_OPENO);
            if (CimStringUtils.isEmpty(logicalRecipe.getValue()) && searchKeyPatternFlag4ProductGroup) {
                log.info("dataCollectionItemFillInEDCDataItemListByKeyInqDr(): Get logicalRecipe by productgroup");
                List<String> outData = CimArrayUtils.convertStringList(cimJpaRepository.queryOne("SELECT LRCP_ID,LRCP_RKEY FROM OMPRP_LRPRODFMLY WHERE  REFKEY = ?1 AND PRODFMLY_ID = ?2",
                        pdOut.getPrimaryKey(), productGroupID.get()));
                if (CimArrayUtils.isEmpty(outData)) {
                    // Get Technology
                    Object[] productGroup = cimJpaRepository.queryOne("SELECT TECH_ID,PRODFMLY_ID\n" +
                            "                             FROM   OMPRODFMLY\n" +
                            "                             WHERE  PRODFMLY_ID = ?",productGroupID.get());
                    technologyID.set(CimObjectUtils.toString(productGroup[0]));
                } else {
                    logicalRecipe.setValue(outData.get(0));
                    logicalRecipe.setReferenceKey(outData.get(1));
                }
            }
            // Get logicalRecipe by Technology
            boolean searchKeyPatternFlag4Technology = CimStringUtils.equals(searchKeyPattern, BizConstant.SP_DCITEM_SEARCHKEYPATTERN_LOT_ROUTE_OPENO)
                    || CimStringUtils.equals(searchKeyPattern, BizConstant.SP_DCITEM_SEARCHKEYPATTERN_TECH_ROUTE_OPENO)
                    || CimStringUtils.equals(searchKeyPattern, BizConstant.SP_DCITEM_SEARCHKEYPATTERN_PROD_ROUTE_OPENO)
                    || CimStringUtils.equals(searchKeyPattern, BizConstant.SP_DCITEM_SEARCHKEYPATTERN_PRODGRP_ROUTE_OPENO);
            if (CimStringUtils.isEmpty(logicalRecipe.getValue()) && searchKeyPatternFlag4Technology) {
                log.info("dataCollectionItemFillInEDCDataItemListByKeyInqDr(): Get logicalRecipe by Technology");
                Object[] processDefinitionLcRecipeTech = cimJpaRepository.queryOne("SELECT LRCP_ID,LRCP_OBJ\n" +
                        "                         FROM   OMPRP_LRTECH\n" +
                        "                         WHERE  REFKEY = ?\n" +
                        "                         AND    TECH_ID        = ?", pdOut.getPrimaryKey(), technologyID.get());
                if (processDefinitionLcRecipeTech != null) {
                    logicalRecipe.setValue(CimObjectUtils.toString(processDefinitionLcRecipeTech[0]));
                    logicalRecipe.setReferenceKey(CimObjectUtils.toString(processDefinitionLcRecipeTech[1]));
                }
            }
            // Get default logicalRecipe
            if (CimStringUtils.isEmpty(logicalRecipe.getValue())) {
                log.info("dataCollectionItemFillInEDCDataItemListByKeyInqDr(): Get default Technology");
                com.fa.cim.newcore.bo.pd.CimProcessDefinition processDefinition = baseCoreFactory.getBO(com.fa.cim.newcore.bo.pd.CimProcessDefinition.class,pdOut.getPrimaryKey());
                if (processDefinition != null) {
                    logicalRecipe.setValue(processDefinition.getDefaultLogicalRecipe().getIdentifier());
                    logicalRecipe.setReferenceKey(processDefinition.getPrimaryKey());
                }
            }
            versionID.set(BaseStaticMethod.extractVersionFromID(logicalRecipe.getValue()));
            if (CimStringUtils.equals(versionID.get(), BizConstant.SP_ACTIVE_VERSION)) {
                // Get active version object of logicalRecipe
                Object[] tmpLRecipe = cimJpaRepository.queryOne("SELECT ACTIVE_RKEY,LRCP_ID \n" +
                        "                         FROM   OMLRCP \n" +
                        "                         WHERE  LRCP_ID = ?", logicalRecipe.getValue());
                if (tmpLRecipe != null) {
                    logicalRecipe.setReferenceKey(CimObjectUtils.toString(tmpLRecipe[0]));
                }
            }
            // Get RecipeType of logicalRecipe
            com.fa.cim.newcore.bo.recipe.CimLogicalRecipe tmpLRecipe = baseCoreFactory.getBO(com.fa.cim.newcore.bo.recipe.CimLogicalRecipe.class,logicalRecipe);
            recipeType.set(tmpLRecipe.getRecipeType());

            //Check record count of all default Recipe
            long count = cimJpaRepository.count("SELECT COUNT(REFKEY) \n" +
                    "                     FROM   OMLRCP_DFLT\n" +
                    "                     WHERE  REFKEY = ?",tmpLRecipe.getPrimaryKey());
            if (count > 0) {
                // --- Get all default Recipe with DCDef ID and DCSpec ID -----------
                List<Object[]> logicalRecipeDefaultSettings = cimJpaRepository.query("SELECT IDX_NO\n" +
                        "                               ,RECIPE_ID\n" +
                        "                               ,RECIPE_RKEY\n" +
                        "                               ,EDC_PLAN_ID\n" +
                        "                               ,EDC_PLAN_RKEY\n" +
                        "                               ,EDC_SPEC_ID\n" +
                        "                               ,EDC_SPEC_RKEY\n" +
                        "                         FROM   OMLRCP_DFLT\n" +
                        "                         WHERE  REFKEY = ?\n" +
                        "                         ORDER BY IDX_NO", tmpLRecipe.getPrimaryKey());
                if (!CimArrayUtils.isEmpty(logicalRecipeDefaultSettings)) {
                    for (Object[] item : logicalRecipeDefaultSettings) {
                        String theTableMarker = String.format("%s%d", BizConstant.SP_LRCP_SSET_PRST_TABLEMARKER, CimNumberUtils.intValue(item[0]));
                        // Check record count of Chamber information(Process Resource)
                        count = cimJpaRepository.count("SELECT COUNT(REFKEY) \n" +
                                "                             FROM   OMLRCP_DFLT_PRST\n" +
                                "                             WHERE  REFKEY = ?\n" +
                                "                             AND   (LINK_MARKER = ? OR LINK_MARKER = ?)",
                                tmpLRecipe.getPrimaryKey(), item[0], theTableMarker);
                        Infos.DefaultRecipeWithDCID defaultRecipeWithDCID = new Infos.DefaultRecipeWithDCID();
                        defaultRecipeSettings.add(defaultRecipeWithDCID);
                        if (count > 0) {
                            // Get Chamber infomation(Process Resource)
                            List<Object[]> chamberInfomations=cimJpaRepository.query("SELECT PROCRSC_ID\n" +
                                    "                                       ,STATE\n" +
                                    "                                 FROM   OMLRCP_DFLT_PRST\n" +
                                    "                                 WHERE  REFKEY = ?\n" +
                                    "                                 AND   (LINK_MARKER = ? OR LINK_MARKER = ?)",tmpLRecipe.getPrimaryKey(), item[0], theTableMarker);
                            if (!CimArrayUtils.isEmpty(chamberInfomations)) {
                                List<Infos.Chamber> chambers = new ArrayList<>();
                                defaultRecipeWithDCID.setChamberSeq(chambers);
                                for (Object[] chamberInfomation : chamberInfomations) {
                                    Infos.Chamber chamber = new Infos.Chamber();
                                    chamber.setChamberID(new ObjectIdentifier(CimObjectUtils.toString(chamberInfomation[0])));
                                    chamber.setState(CimBooleanUtils.convert(chamberInfomation[1]));
                                    chambers.add(chamber);
                                }
                            }
                        }
                        defaultRecipeWithDCID.setRecipeID(new ObjectIdentifier(CimObjectUtils.toString(item[1]), CimObjectUtils.toString(item[2])));
                        defaultRecipeWithDCID.setDcDefID(new ObjectIdentifier(CimObjectUtils.toString(item[3]), CimObjectUtils.toString(item[4])));
                        defaultRecipeWithDCID.setDcSpecID(new ObjectIdentifier(CimObjectUtils.toString(item[5]), CimObjectUtils.toString(item[6])));
                    }
                }
            }
            //Get DCSpec ID for Measurement
            String dcSpecForProcess = StandardProperties.OM_EDC_SPEC_FOR_PROCESS.getValue();
            if (CimStringUtils.equals(dcSpecForProcess, "1") || CimStringUtils.equals(recipeType.get(), BizConstant.SP_RECIPE_MEASUREMENT)) {
                log.info("dataCollectionItemFillInEDCDataItemListByKeyInqDr(): Get DCSpec ID for Measurement");
                // Get DCSpec by Product
                if (CimStringUtils.equals(searchKeyPattern, BizConstant.SP_DCITEM_SEARCHKEYPATTERN_LOT_ROUTE_OPENO)
                        || CimStringUtils.equals(searchKeyPattern, BizConstant.SP_DCITEM_SEARCHKEYPATTERN_PROD_ROUTE_OPENO)) {
                    log.info("dataCollectionItemFillInEDCDataItemListByKeyInqDr(): Get DCSpec by Product");
                    Object[] measDCSpecByProduct = cimJpaRepository.queryOne("SELECT EDC_SPEC_ID,EDC_SPEC_RKEY\n" +
                            "                             FROM   OMPRSS_MEASSPECPROD\n" +
                            "                             WHERE  REFKEY = ?\n" +
                            "                             AND    PROD_ID    = ?",modulePOSKey, productID.get());
                    if (measDCSpecByProduct != null) {
                        measurementDCSpecID.setValue(CimObjectUtils.toString(measDCSpecByProduct[0]));
                        measurementDCSpecID.setReferenceKey(CimObjectUtils.toString(measDCSpecByProduct[1]));
                    } else {
                        if (CimStringUtils.isEmpty(productGroupID.get())) {
                            // Get ProductGroupID
                            Object[] productSpecification = cimJpaRepository.queryOne("SELECT PRODFMLY_ID,PROD_ID\n" +
                                    "                                     FROM   OMPRODINFO\n" +
                                    "                                     WHERE  PROD_ID = ?",productID.get());
                            productGroupID.set(productSpecification == null ? null : CimObjectUtils.toString(productSpecification[0]));
                        }
                    }
                }
                // Get DCSpec by productgroup
                boolean searchKeyPatternByProductGroup = CimStringUtils.equals(searchKeyPattern, BizConstant.SP_DCITEM_SEARCHKEYPATTERN_LOT_ROUTE_OPENO)
                        || CimStringUtils.equals(searchKeyPattern, BizConstant.SP_DCITEM_SEARCHKEYPATTERN_PROD_ROUTE_OPENO)
                        || CimStringUtils.equals(searchKeyPattern, BizConstant.SP_DCITEM_SEARCHKEYPATTERN_PRODGRP_ROUTE_OPENO);
                if (CimStringUtils.isEmpty(measurementDCSpecID.getValue()) && searchKeyPatternByProductGroup) {
                    log.info("dataCollectionItemFillInEDCDataItemListByKeyInqDr(): Get DCSpec by productgroup");
                    Object[] dcSpecProdGroup = cimJpaRepository.queryOne("SELECT EDC_SPEC_ID,EDC_SPEC_RKEY\n" +
                            "                             FROM   OMPRSS_MEASSPECPRODFMLY\n" +
                            "                             WHERE  REFKEY = ?\n" +
                            "                             AND    PRODFMLY_ID     = ?",modulePOSKey, productGroupID.get());
                    if (dcSpecProdGroup != null) {
                        measurementDCSpecID.setValue(CimObjectUtils.toString(dcSpecProdGroup[0]));
                        measurementDCSpecID.setReferenceKey(CimObjectUtils.toString(dcSpecProdGroup[1]));
                    } else {
                        if (CimStringUtils.isEmpty(technologyID.get())) {
                            // Get Technology
                            Object[] productGroup = cimJpaRepository.queryOne("SELECT TECH_ID,PRODFMLY_ID\n" +
                                    "                                     FROM   OMPRODFMLY\n" +
                                    "                                     WHERE  PRODFMLY_ID = ?1 ",productGroupID.get());
                            technologyID.set(productGroup == null ? null : CimObjectUtils.toString(productGroup[0]));
                            log.info("dataCollectionItemFillInEDCDataItemListByKeyInqDr(): Get Technology by ProductGroupID {}", technologyID.get());
                        }
                    }
                }
                // Get DCSpec by Technology
                boolean searchKeyPatternByTechnology = CimStringUtils.equals(searchKeyPattern, BizConstant.SP_DCITEM_SEARCHKEYPATTERN_LOT_ROUTE_OPENO)
                        || CimStringUtils.equals(searchKeyPattern, BizConstant.SP_DCITEM_SEARCHKEYPATTERN_PROD_ROUTE_OPENO)
                        || CimStringUtils.equals(searchKeyPattern, BizConstant.SP_DCITEM_SEARCHKEYPATTERN_PRODGRP_ROUTE_OPENO)
                        || CimStringUtils.equals(searchKeyPattern, BizConstant.SP_DCITEM_SEARCHKEYPATTERN_TECH_ROUTE_OPENO);
                if (CimStringUtils.isEmpty(measurementDCSpecID.getValue()) && searchKeyPatternByTechnology) {
                    log.info("dataCollectionItemFillInEDCDataItemListByKeyInqDr(): Get DCSpec by Technology");
                    Object[] dcSpecTech = cimJpaRepository.queryOne("SELECT EDC_SPEC_ID,EDC_SPEC_RKEY\n" +
                            "                             FROM   OMPRSS_MEASSPECTECH\n" +
                            "                             WHERE  REFKEY = ?\n" +
                            "                             AND    TECH_ID        = ?",modulePOSKey, technologyID.get());
                    if (dcSpecTech != null) {
                        measurementDCSpecID.setValue(CimObjectUtils.toString(dcSpecTech[0]));
                        measurementDCSpecID.setReferenceKey(CimObjectUtils.toString(dcSpecTech[1]));
                    }
                }
                // Get default DCSpec
                if (CimStringUtils.isEmpty(measurementDCSpecID.getValue())) {
                    log.info("dataCollectionItemFillInEDCDataItemListByKeyInqDr(): Get default DCSpec");
                    Object[] pos = cimJpaRepository.queryOne("SELECT EDC_SPEC_ID,ID\n" +
                            "                             FROM   OMPRSS\n" +
                            "                             WHERE  ID = ?",modulePOSKey);
                    if (pos != null) {
                        measurementDCSpecID.setValue(CimObjectUtils.toString(pos[0]));
                        measurementDCSpecID.setReferenceKey(CimObjectUtils.toString(pos[1]));
                    }
                }
            }
            //Get DCSpec ID for Process
            if (CimStringUtils.equals(recipeType.get(), BizConstant.SP_RECIPE_PROCESS)) {
                log.info("dataCollectionItemFillInEDCDataItemListByKeyInqDr(): Get DCSpec ID for Process");
                // Check record count of all eqp specific Recipe
                count = cimJpaRepository.count("SELECT COUNT(ID) \n" +
                        "                         FROM   OMLRCP_EQSP\n" +
                        "                         WHERE  REFKEY = ?",tmpLRecipe.getPrimaryKey());
                if (count > 0) {
                    // Get all eqp specific Recipe with DCSpec
                    List<Object[]> logicalSpecificRecipeSettings = cimJpaRepository.query("SELECT IDX_NO\n" +
                            "                                   ,EQP_ID\n" +
                            "                                   ,EQP_RKEY\n" +
                            "                                   ,RECIPE_ID\n" +
                            "                                   ,RECIPE_RKEY\n" +
                            "                                   ,EDC_SPEC_ID\n" +
                            "                                   ,EDC_SPEC_RKEY\n" +
                            "                             FROM   OMLRCP_EQSP\n" +
                            "                             WHERE  REFKEY = ?\n" +
                            "                             ORDER BY IDX_NO",tmpLRecipe.getPrimaryKey());
                    if (CimArrayUtils.isNotEmpty(logicalSpecificRecipeSettings)) {
                        Infos.EqpSpecificRecipeWithDCID eqpSpecificRecipeWithDCID = new Infos.EqpSpecificRecipeWithDCID();
                        eqpSpecificRecipeSettings.add(eqpSpecificRecipeWithDCID);
                        for (Object[] item : logicalSpecificRecipeSettings) {
                            String theTableMarker = String.format("%s%s", BizConstant.SP_LRCP_SSET_PRST_TABLEMARKER, item[0]);
                            // Check record count of Chamber information(Process Resource)
                            count = cimJpaRepository.count("SELECT COUNT(ID) INTO :hCount\n" +
                                    "                                 FROM   OMLRCP_EQSP_PRST\n" +
                                    "                                 WHERE  REFKEY = ?\n" +
                                    "                                 AND   (LINK_MARKER = ? OR LINK_MARKER = ?)",
                                    tmpLRecipe.getPrimaryKey(), item[0], theTableMarker);
                            if (count > 0) {
                                // Get Chamber infomation(Process Resource)
                                List<Object[]> chamberInformations = cimJpaRepository.query("SELECT PROCRSC_ID,STATE\n" +
                                        "                                     FROM   OMLRCP_EQSP_PRST\n" +
                                        "                                     WHERE  REFKEY = ?\n" +
                                        "                                     AND   (LINK_MARKER = ? OR LINK_MARKER = ?)",
                                        tmpLRecipe.getPrimaryKey(), item[0], theTableMarker);
                                if (CimArrayUtils.isEmpty(chamberInformations)) {
                                    continue;
                                }
                                List<Infos.Chamber> chambers = new ArrayList<>();
                                eqpSpecificRecipeWithDCID.setChamberSeq(chambers);
                                for (Object[] chamberInformation : chamberInformations) {
                                    Infos.Chamber chamber = new Infos.Chamber();
                                    chambers.add(chamber);
                                    chamber.setChamberID(new ObjectIdentifier(CimObjectUtils.toString(chamberInformation[0])));
                                    chamber.setState(CimBooleanUtils.convert(chamberInformation[1]));
                                }
                            }
                            eqpSpecificRecipeWithDCID.setEquipmentID(new ObjectIdentifier(CimObjectUtils.toString(item[1]), CimObjectUtils.toString(item[2])));
                            eqpSpecificRecipeWithDCID.setRecipeID(new ObjectIdentifier(CimObjectUtils.toString(item[3]), CimObjectUtils.toString(item[4])));
                            eqpSpecificRecipeWithDCID.setDcSpecID(new ObjectIdentifier(CimObjectUtils.toString(item[5]), CimObjectUtils.toString(item[6])));
                        }
                    }
                }
            }
            //Get all DeltaDCDef ID
            // Get all DeltaDCDef in mainPOS
            if (CimStringUtils.isNotEmpty(mainPOSKey)) {
                log.info("dataCollectionItemFillInEDCDataItemListByKeyInqDr(): Get all DeltaDCDef in mainPOS");
                // Check record count of all DeltaDCDef
                count = cimJpaRepository.count("SELECT COUNT(REFKEY) \n" +
                        "                         FROM   OMPRSS_DELTAEDCPLAN\n" +
                        "                         WHERE  REFKEY = ?",mainPOSKey);
                if (count > 0) {
                    List<Object[]> allDeltaDcDefs = cimJpaRepository.query("SELECT PREV_OPE_NO\n" +
                            "                                   ,PREV_EDC_PLAN_ID\n" +
                            "                                   ,PREV_EDC_PLAN_RKEY\n" +
                            "                                   ,CUR_EDC_PLAN_ID\n" +
                            "                                   ,CUR_EDC_PLAN_RKEY\n" +
                            "                                   ,DELTA_EDC_PLAN_ID\n" +
                            "                                   ,DELTA_EDC_PLAN_RKEY\n" +
                            "                             FROM   OMPRSS_DELTAEDCPLAN\n" +
                            "                             WHERE  REFKEY = ?",mainPOSKey);
                    if (CimArrayUtils.isNotEmpty(allDeltaDcDefs)) {
                        for (Object[] item : allDeltaDcDefs) {
                            Infos.DeltaDCSetting deltaDCSetting = new Infos.DeltaDCSetting();
                            deltaDCSettings.add(deltaDCSetting);
                            deltaDCSetting.setPreviousOperationNumber(CimObjectUtils.toString(item[0]));
                            deltaDCSetting.setPreviousDCDefID(new ObjectIdentifier(CimObjectUtils.toString(item[1]), CimObjectUtils.toString(item[2])));
                            deltaDCSetting.setPostDCDefID(new ObjectIdentifier(CimObjectUtils.toString(item[3]), CimObjectUtils.toString(item[4])));
                            deltaDCSetting.setDeltaDCDefID(new ObjectIdentifier(CimObjectUtils.toString(item[5]), CimObjectUtils.toString(item[6])));
                        }
                    }
                }
            }
            // Get all DeltaDCDef in modulePOS
            // Check record count of all DeltaDCDef
            count = cimJpaRepository.count("SQL SELECT COUNT(REFKEY) \n" +
                    "                         FROM   OMPRSS_DELTAEDCPLAN\n" +
                    "                         WHERE  REFKEY = ?",modulePOSKey);
            if (count > 0) {
                List<Object[]> allDeltaDcDefs = cimJpaRepository.query("SELECT PREV_OPE_NO\n" +
                        "                                   ,PREV_EDC_PLAN_ID\n" +
                        "                                   ,PREV_EDC_PLAN_RKEY\n" +
                        "                                   ,CUR_EDC_PLAN_ID\n" +
                        "                                   ,CUR_EDC_PLAN_RKEY\n" +
                        "                                   ,DELTA_EDC_PLAN_ID\n" +
                        "                                   ,DELTA_EDC_PLAN_RKEY\n" +
                        "                             FROM   OMPRSS_DELTAEDCPLAN\n" +
                        "                             WHERE  REFKEY = ?",
                        mainPOSKey);
                if (CimArrayUtils.isNotEmpty(allDeltaDcDefs)) {
                    String previousOperationNumber = null;
                    for (Object[] item : allDeltaDcDefs) {
                        // Get previousOperationNumber from moduleNumber and moduleOpeNumber(PREV_OPE_NO)
                        boolean foundSameDef = false;
                        previousOperationNumber = BaseStaticMethod.convertModuleOpeNoToOpeNo(pdListModuleNo, CimObjectUtils.toString(item[0]));
                        for (Infos.DeltaDCSetting deltaDCSetting : deltaDCSettings) {
                            if (CimStringUtils.equals(previousOperationNumber, deltaDCSetting.getPreviousOperationNumber())
                                    && CimStringUtils.equals(CimObjectUtils.toString(item[1]), deltaDCSetting.getPreviousDCDefID().getValue())
                                    && CimStringUtils.equals(CimObjectUtils.toString(item[3]), deltaDCSetting.getPostDCDefID().getValue())) {
                                foundSameDef = true;
                                break;
                            }
                        }
                        if (!foundSameDef) {
                            Infos.DeltaDCSetting deltaDCSetting = new Infos.DeltaDCSetting();
                            deltaDCSettings.add(deltaDCSetting);
                            deltaDCSetting.setPreviousOperationNumber(previousOperationNumber);
                            deltaDCSetting.setPreviousDCDefID(new ObjectIdentifier(CimObjectUtils.toString(item[1]), CimObjectUtils.toString(item[2])));
                            deltaDCSetting.setPostDCDefID(new ObjectIdentifier(CimObjectUtils.toString(item[3]), CimObjectUtils.toString(item[4])));
                            deltaDCSetting.setDeltaDCDefID(new ObjectIdentifier(CimObjectUtils.toString(item[5]), CimObjectUtils.toString(item[6])));
                        }
                    }
                }
            }
            // Get all previousOperationID
            log.info("dataCollectionItemFillInEDCDataItemListByKeyInqDr(): Get all previousOperationID");
            Map<String, ObjectIdentifier> previousOperationIDList = new HashMap<>();
            Map<String, String> modulePDIDList = new HashMap<>();
            for (Infos.DeltaDCSetting deltaDCSetting : deltaDCSettings) {
                ObjectIdentifier previousOperationID = null;
                if (previousOperationIDList.containsKey(deltaDCSetting.getPreviousOperationNumber())) {
                    previousOperationID = previousOperationIDList.get(deltaDCSetting.getPreviousOperationNumber());
                    deltaDCSetting.setPreviousOperationID(previousOperationID);
                    continue;
                }
                // Get preOpeNo's moduleNumber and moduleOpeNumber
                String[] moduleNoArray = BaseStaticMethod.convertOpeNoToModuleNoAndModuleOpeNo(deltaDCSetting.getPreviousOperationNumber());
                pdListModuleNo = moduleNoArray[0];
                posOpeNo = moduleNoArray[1];
                // Get modulePDID
                String tmpModulePDID = null;
                if (!modulePDIDList.containsKey(pdListModuleNo)) {
                    String sql = "SELECT pdlist.PD_ID FROM OMPRF pf, OMPRF_ROUTESEQ pdlist WHERE pf.PRP_ID = ?1 AND pf.PRP_LEVEL = ?2 AND pf.ACTIVE_FLAG = ?3 AND pdlist.REFKEY = pf.ID AND pdlist.MODULE_NO = ?4";
                    List<?> resultList = cimJpaRepository.query(sql,routeID.get(), BizConstant.SP_PD_FLOWLEVEL_MAIN_FOR_MODULE,true,pdListModuleNo);
                    //--- If modulePD's version is "##"
                    if (CimArrayUtils.isNotEmpty(resultList)) {
                        tmpModulePDID = (String) resultList.get(0);
                        versionID.set(BaseStaticMethod.extractVersionFromID(tmpModulePDID));
                        if (CimStringUtils.equals(versionID.get(), BizConstant.SP_ACTIVE_VERSION)) {
                            // Get active version of modulePD
                            Object[] activeID = cimJpaRepository.queryOne("SELECT ACTIVE_VER_ID,PRP_ID\n" +
                                    "                                 FROM   OMPRP\n" +
                                    "                                 WHERE  PRP_ID    = ?\n" +
                                    "                                 AND    PRP_LEVEL = ?",tmpModulePDID, pdLevel);
                            tmpModulePDID = CimObjectUtils.toString(activeID[0]);
                        }
                        modulePDIDList.put(pdListModuleNo, tmpModulePDID);
                    }
                }
                // --- Get modulePOS and PD -----------
                String sql = "SELECT pos.id ,pos.STEP_ID ,pos.STEP_RKEY FROM OMPRF pf, OMPRSS pos WHERE pf.PRP_ID = ? AND pf.PRP_LEVEL = ? AND pf.ACTIVE_FLAG = ? AND pos.PRF_RKEY = pf.ID AND pos.OPE_NO = ?";
                List<?> resultList = cimJpaRepository.query(sql,tmpModulePDID,BizConstant.SP_PD_FLOWLEVEL_MODULE,true,posOpeNo);
                //--- If PD_ID's version is "##"
                if (CimArrayUtils.isNotEmpty(resultList)) {
                    String posKeyId = (String) resultList.get(0);
                    String tmpPDID = (String) resultList.get(1);
                    String posPDObj = (String) resultList.get(2);
                    versionID.set(BaseStaticMethod.extractVersionFromID(tmpPDID));
                    if (CimStringUtils.equals(versionID.get(), BizConstant.SP_ACTIVE_VERSION)) {
                        // Get active version object of PD
                        Object[] activeID = cimJpaRepository.queryOne("SELECT ACTIVE_VER_ID,ACTIVE_VER_RKEY\n" +
                                "                             FROM   OMPRP\n" +
                                "                             WHERE  PRP_ID    = ?1 \n" +
                                "                             AND    PRP_LEVEL = ?2 ",tmpPDID, BizConstant.SP_PD_FLOWLEVEL_OPERATION);
                        deltaDCSetting.setPreviousOperationID(ObjectIdentifier.build(CimObjectUtils.toString(activeID[0]), CimObjectUtils.toString(activeID[1])));
                    } else {
                        deltaDCSetting.setPreviousOperationID(new ObjectIdentifier(tmpPDID, posPDObj));
                    }
                    previousOperationIDList.put(deltaDCSetting.getPreviousOperationNumber(), deltaDCSetting.getPreviousOperationID());
                }
            }
            //Get all DeltaDCSpec ID
            Map<String, ObjectIdentifier> deltaDCSpecIDList = new HashMap<>();
            for (Infos.DeltaDCSetting deltaDCSetting : deltaDCSettings) {
                ObjectIdentifier deltaDCSpecID = new ObjectIdentifier();
                if (deltaDCSpecIDList.containsKey(deltaDCSetting.getDeltaDCDefID().getValue())) {
                    deltaDCSpecID = deltaDCSpecIDList.get(deltaDCSetting.getDeltaDCDefID().getValue());
                    deltaDCSetting.setDeltaDCSpecID(deltaDCSpecID);
                    continue;
                }
                // Get DeltaDCSpec by Product in mainPOS
                boolean searchKeyPatternByProductInMainPOS = CimStringUtils.equals(searchKeyPattern, BizConstant.SP_DCITEM_SEARCHKEYPATTERN_LOT_ROUTE_OPENO)
                        || CimStringUtils.equals(searchKeyPattern, BizConstant.SP_DCITEM_SEARCHKEYPATTERN_PROD_ROUTE_OPENO);
                if (CimStringUtils.isNotEmpty(mainPOSKey) && searchKeyPatternByProductInMainPOS) {
                    log.info("dataCollectionItemFillInEDCDataItemListByKeyInqDr(): Get DeltaDCSpec by Product in mainPOS");
                    Object[] deltaDCSpec = cimJpaRepository.queryOne("SELECT DELTA_EDC_SPEC_ID,DELTA_EDC_SPEC_RKEY\n" +
                            "                             FROM   OMPRSS_DELTAEDCPROD\n" +
                            "                             WHERE  REFKEY = ?\n" +
                            "                             AND    PROD_ID    = ?\n" +
                            "                             AND    DELTA_EDC_PLAN_ID = ?",mainPOSKey,productID.get(),deltaDCSetting.getDeltaDCDefID().getValue());
                    if (deltaDCSpec != null) {
                        deltaDCSpecID.setValue(CimObjectUtils.toString(deltaDCSpec[0]));
                        deltaDCSpecID.setReferenceKey(CimObjectUtils.toString(deltaDCSpec[1]));
                    }

                }
                // Get DeltaDCSpec by Product in modulePOS
                boolean searchKeyPatternByProductInModulePos = CimStringUtils.equals(searchKeyPattern, BizConstant.SP_DCITEM_SEARCHKEYPATTERN_LOT_ROUTE_OPENO)
                        || CimStringUtils.equals(searchKeyPattern, BizConstant.SP_DCITEM_SEARCHKEYPATTERN_PROD_ROUTE_OPENO);
                if (CimStringUtils.isEmpty(deltaDCSpecID.getValue()) && searchKeyPatternByProductInModulePos) {
                    log.info("dataCollectionItemFillInEDCDataItemListByKeyInqDr(): Get DeltaDCSpec by Product in modulePOS");
                    Object[] deltaDCSpec = cimJpaRepository.queryOne("SELECT DELTA_EDC_SPEC_ID,DELTA_EDC_SPEC_RKEY\n" +
                            "                             FROM   OMPRSS_DELTAEDCPROD\n" +
                            "                             WHERE  REFKEY = ?\n" +
                            "                             AND    PROD_ID    = ?\n" +
                            "                             AND    DELTA_EDC_PLAN_ID = ?",modulePOSKey,productID.get(),deltaDCSetting.getDeltaDCDefID().getValue());
                    if (deltaDCSpec != null) {
                        deltaDCSpecID.setValue(CimObjectUtils.toString(deltaDCSpec[0]));
                        deltaDCSpecID.setReferenceKey(CimObjectUtils.toString(deltaDCSpec[1]));
                    } else {
                        if (CimStringUtils.isEmpty(productGroupID.get())) {
                            // Get ProductGroupID
                            Object[] productSpecification = cimJpaRepository.queryOne("SELECT PRODFMLY_ID,PROD_ID\n" +
                                    "                                     FROM   OMPRODINFO\n" +
                                    "                                     WHERE  PROD_ID = ?",productID.get());
                            productGroupID.set(productSpecification == null ? null : CimObjectUtils.toString(productSpecification[0]));
                        }
                    }
                }
                // Get DeltaDCSpec by productgroup in mainPOS
                boolean searchKeyPatternByProductGroupInMainPos = CimStringUtils.equals(searchKeyPattern, BizConstant.SP_DCITEM_SEARCHKEYPATTERN_LOT_ROUTE_OPENO)
                        || CimStringUtils.equals(searchKeyPattern, BizConstant.SP_DCITEM_SEARCHKEYPATTERN_PROD_ROUTE_OPENO)
                        || CimStringUtils.equals(searchKeyPattern, BizConstant.SP_DCITEM_SEARCHKEYPATTERN_PRODGRP_ROUTE_OPENO);
                if (CimStringUtils.isEmpty(deltaDCSpecID.getValue()) && CimStringUtils.isNotEmpty(mainPOSKey) && searchKeyPatternByProductGroupInMainPos) {
                    log.info("dataCollectionItemFillInEDCDataItemListByKeyInqDr(): Get DeltaDCSpec by productgroup in mainPOS");
                    Object[] posDeltaDCSpecProdGroup = cimJpaRepository.queryOne("SELECT DELTA_EDC_SPEC_ID,DELTA_EDC_SPEC_RKEY\n" +
                            "                             FROM   OMPRSS_DELTAEDCPRODFMLY\n" +
                            "                             WHERE  REFKEY = ?\n" +
                            "                             AND    PRODFMLY_ID     = ?\n" +
                            "                             AND    DELTA_EDC_PLAN_ID = ?",mainPOSKey,
                            productGroupID.get(),deltaDCSetting.getDeltaDCDefID().getValue());
                    if (posDeltaDCSpecProdGroup != null) {
                        deltaDCSpecID.setValue(CimObjectUtils.toString(posDeltaDCSpecProdGroup[0]));
                        deltaDCSpecID.setReferenceKey(CimObjectUtils.toString(posDeltaDCSpecProdGroup[1]));
                    }
                }
                // Get DeltaDCSpec by productgroup in modulePOS
                boolean searchKeyPatternByProductGroupInModulePOS = CimStringUtils.equals(searchKeyPattern, BizConstant.SP_DCITEM_SEARCHKEYPATTERN_LOT_ROUTE_OPENO)
                        || CimStringUtils.equals(searchKeyPattern, BizConstant.SP_DCITEM_SEARCHKEYPATTERN_PROD_ROUTE_OPENO)
                        || CimStringUtils.equals(searchKeyPattern, BizConstant.SP_DCITEM_SEARCHKEYPATTERN_PRODGRP_ROUTE_OPENO);
                if (CimStringUtils.isEmpty(deltaDCSpecID.getValue()) && searchKeyPatternByProductGroupInModulePOS) {
                    log.info("dataCollectionItemFillInEDCDataItemListByKeyInqDr(): Get DeltaDCSpec by productgroup in modulePOS");
                    Object[] posDeltaDCSpecProdGroup = cimJpaRepository.queryOne("SELECT DELTA_EDC_SPEC_ID,DELTA_EDC_SPEC_RKEY\n" +
                            "                             FROM   OMPRSS_DELTAEDCPRODFMLY\n" +
                            "                             WHERE  REFKEY = ?\n" +
                            "                             AND    PRODFMLY_ID     = ?\n" +
                            "                             AND    DELTA_EDC_PLAN_ID = ?",
                            modulePOSKey,
                            productGroupID.get(),
                            deltaDCSetting.getDeltaDCDefID().getValue());
                    if (posDeltaDCSpecProdGroup != null) {
                        deltaDCSpecID.setValue(CimObjectUtils.toString(posDeltaDCSpecProdGroup[0]));
                        deltaDCSpecID.setReferenceKey(CimObjectUtils.toString(posDeltaDCSpecProdGroup[1]));
                    } else {
                        if (CimStringUtils.isEmpty(technologyID.get())) {
                            // Get Technology
                            Object[] productGroup = cimJpaRepository.queryOne("SELECT TECH_ID,PRODFMLY_ID\n" +
                                    "                                     FROM   OMPRODFMLY\n" +
                                    "                                     WHERE  PRODFMLY_ID = ?1 ",productGroupID.get());
                            technologyID.set(productGroup == null ? null : CimObjectUtils.toString(productGroup[0]));
                        }
                    }
                }
                // Get DeltaDCSpec by Technology in mainPOS
                    boolean searchKeyPatternbyTechnologyInMainPos = CimStringUtils.equals(searchKeyPattern, BizConstant.SP_DCITEM_SEARCHKEYPATTERN_LOT_ROUTE_OPENO)
                        || CimStringUtils.equals(searchKeyPattern, BizConstant.SP_DCITEM_SEARCHKEYPATTERN_PROD_ROUTE_OPENO)
                        || CimStringUtils.equals(searchKeyPattern, BizConstant.SP_DCITEM_SEARCHKEYPATTERN_PRODGRP_ROUTE_OPENO)
                        || CimStringUtils.equals(searchKeyPattern, BizConstant.SP_DCITEM_SEARCHKEYPATTERN_TECH_ROUTE_OPENO);
                if (CimStringUtils.isEmpty(deltaDCSpecID.getValue()) && CimStringUtils.isNotEmpty(mainPOSKey) && searchKeyPatternbyTechnologyInMainPos) {
                    log.info("dataCollectionItemFillInEDCDataItemListByKeyInqDr(): Get DeltaDCSpec by Technology in mainPOS");
                    Object[] posDeltaDCSpecTech = cimJpaRepository.queryOne("SELECT DELTA_EDC_SPEC_ID,DELTA_EDC_SPEC_RKEY\n" +
                            "                             FROM   OMPRSS_DELTAEDCTECH\n" +
                            "                             WHERE  REFKEY = ?\n" +
                            "                             AND    TECH_ID        = ?\n" +
                            "                             AND    DELTA_EDC_PLAN_ID = ?",
                            mainPOSKey,
                            technologyID.get(),
                            deltaDCSetting.getDeltaDCDefID().getValue());
                    if (posDeltaDCSpecTech != null) {
                        deltaDCSpecID.setValue(CimObjectUtils.toString(posDeltaDCSpecTech[0]));
                        deltaDCSpecID.setReferenceKey(CimObjectUtils.toString(posDeltaDCSpecTech[1]));
                    }
                }
                // Get DeltaDCSpec by Technology in modulePOS
                boolean searchKeyPatternByTechnologyInModulePos = CimStringUtils.equals(searchKeyPattern, BizConstant.SP_DCITEM_SEARCHKEYPATTERN_LOT_ROUTE_OPENO)
                        || CimStringUtils.equals(searchKeyPattern, BizConstant.SP_DCITEM_SEARCHKEYPATTERN_PROD_ROUTE_OPENO)
                        || CimStringUtils.equals(searchKeyPattern, BizConstant.SP_DCITEM_SEARCHKEYPATTERN_PRODGRP_ROUTE_OPENO)
                        || CimStringUtils.equals(searchKeyPattern, BizConstant.SP_DCITEM_SEARCHKEYPATTERN_TECH_ROUTE_OPENO);
                if (CimStringUtils.isEmpty(deltaDCSpecID.getValue()) && searchKeyPatternByTechnologyInModulePos) {
                    log.info("dataCollectionItemFillInEDCDataItemListByKeyInqDr(): Get DeltaDCSpec by Technology in modulePOS");
                    Object[] posDeltaDCSpecTech = cimJpaRepository.queryOne("SELECT DELTA_EDC_SPEC_ID,DELTA_EDC_SPEC_RKEY\n" +
                            "                             FROM   OMPRSS_DELTAEDCTECH\n" +
                            "                             WHERE  REFKEY = ?\n" +
                            "                             AND    TECH_ID        = ?\n" +
                            "                             AND    DELTA_EDC_PLAN_ID = ?",
                            modulePOSKey,
                            technologyID.get(),
                            deltaDCSetting.getDeltaDCDefID().getValue());
                    if (posDeltaDCSpecTech != null) {
                        deltaDCSpecID.setValue(CimObjectUtils.toString(posDeltaDCSpecTech[0]));
                        deltaDCSpecID.setReferenceKey(CimObjectUtils.toString(posDeltaDCSpecTech[1]));
                    }
                }
                // Get default DeltaDCSpec in mainPOS
                if (CimStringUtils.isEmpty(deltaDCSpecID.getValue()) && CimStringUtils.isNotEmpty(mainPOSKey)) {
                    log.info("dataCollectionItemFillInEDCDataItemListByKeyInqDr(): Get default DeltaDCSpec in mainPOS");
                    Object[] posDeltaDCSpecDefault = cimJpaRepository.queryOne("SELECT DELTA_EDC_SPEC_ID,DELTA_EDC_SPEC_RKEY\n" +
                            "                             FROM   OMPRSS_DELTAEDC\n" +
                            "                             WHERE  REFKEY = ?\n" +
                            "                             AND    DELTA_EDC_PLAN_ID = ?",
                            mainPOSKey,
                            deltaDCSetting.getDeltaDCDefID().getValue());
                    if (posDeltaDCSpecDefault != null) {
                        deltaDCSpecID.setValue(CimObjectUtils.toString(posDeltaDCSpecDefault[0]));
                        deltaDCSpecID.setReferenceKey(CimObjectUtils.toString(posDeltaDCSpecDefault[1]));
                    }
                }
                // Get default DeltaDCSpec in modulePOS
                if (CimStringUtils.isEmpty(deltaDCSpecID.getValue())) {
                    log.info("dataCollectionItemFillInEDCDataItemListByKeyInqDr(): Get default DeltaDCSpec in modulePOS");
                    Object[] posDeltaDCSpecDefault = cimJpaRepository.queryOne("SELECT DELTA_EDC_SPEC_ID,DELTA_EDC_SPEC_RKEY\n" +
                            "                             FROM   OMPRSS_DELTAEDC\n" +
                            "                             WHERE  REFKEY = ?\n" +
                            "                             AND    DELTA_EDC_PLAN_ID = ?",modulePOSKey,
                            deltaDCSetting.getDeltaDCDefID().getValue());
                    if (posDeltaDCSpecDefault != null) {
                        deltaDCSpecID.setValue(CimObjectUtils.toString(posDeltaDCSpecDefault[0]));
                        deltaDCSpecID.setReferenceKey(CimObjectUtils.toString(posDeltaDCSpecDefault[1]));
                    }
                }
                deltaDCSetting.setDeltaDCSpecID(deltaDCSpecID);
            }
        }
        //Step3 - Get all DCDef and DeltaDCDef informations
        // dataItemName
        String dcItemName = null;
        if (CimStringUtils.isNotEmpty(dataItemName.get())) {
            dcItemName = dataItemName.get();
        } else {
            log.info("dataCollectionItemFillInEDCDataItemListByKeyInqDr(): dataItemName is empty, retrieve all dataItemName");
            dcItemName = "%";
        }
        // Specified DCDefID
        if (CimStringUtils.equals(searchKeyPattern, BizConstant.SP_DCITEM_SEARCHKEYPATTERN_DCDEF)) {
            log.info("dataCollectionItemFillInEDCDataItemListByKeyInqDr(): Specified DCDefID {}", dcDefID.get());
            // Check record count of all DCDef and DeltaDCDef
            long count = cimJpaRepository.count("SELECT COUNT(EDC_PLAN_ID) \n" +
                    "                     FROM    EDCPLAN\n" +
                    "                     WHERE   EDC_PLAN_ID LIKE ?",dcDefID.get());
            if (count > 0) {
                List<Object[]> allDcDefAndDeltaDcDef = cimJpaRepository.query("SELECT  ID,EDC_PLAN_ID\n" +
                        "                         FROM    EDCPLAN\n" +
                        "                         WHERE   EDC_PLAN_ID LIKE ?\n" +
                        "                         ORDER BY EDC_PLAN_ID",dcDefID.get());
                //DCDefDataItems
                if (CimArrayUtils.isNotEmpty(allDcDefAndDeltaDcDef)) {
                    for (Object[] item : allDcDefAndDeltaDcDef) {
                        Infos.DCDefDataItem dcDefDataItem = new Infos.DCDefDataItem();
                        dcDefDataItems.add(dcDefDataItem);
                        // Check record count of DCItem
                        count = cimJpaRepository.count("SELECT COUNT(ID) \n" +
                                "                             FROM OMEDCPLAN_ITEM\n" +
                                "                             WHERE REFKEY = ?\n" +
                                "                             AND   NAME LIKE ?",item[0], dcItemName);
                        setDCDefDataItemDetailInfos(dcItemName, count, item, dcDefDataItem);
                        dcDefDataItem.setDcDefID(new ObjectIdentifier(CimObjectUtils.toString(item[1]), CimObjectUtils.toString(item[0])));
                    }
                }
            }
        } else if (routeOpeFlag.get()) {
            // Specified route operation
            log.info("dataCollectionItemFillInEDCDataItemListByKeyInqDr(): Specified route operation");
            Map<String, ObjectIdentifier> dcDefIDList = new HashMap<>();
            for (Infos.DefaultRecipeWithDCID defaultRecipeSetting : defaultRecipeSettings) {
                if (CimStringUtils.isNotEmpty(defaultRecipeSetting.getDcDefID().getValue())) {
                    dcDefIDList.computeIfAbsent(defaultRecipeSetting.getDcDefID().getValue(), k -> defaultRecipeSetting.getDcDefID());
                }
            }
            for (Infos.DeltaDCSetting deltaDCSetting : deltaDCSettings) {
                if (CimStringUtils.isNotEmpty(deltaDCSetting.getPreviousDCDefID().getValue())) {
                    dcDefIDList.computeIfAbsent(deltaDCSetting.getPreviousDCDefID().getValue(), k -> deltaDCSetting.getPreviousDCDefID());
                }
                if (CimStringUtils.isNotEmpty(deltaDCSetting.getPostDCDefID().getValue())) {
                    dcDefIDList.computeIfAbsent(deltaDCSetting.getPostDCDefID().getValue(), k -> deltaDCSetting.getPostDCDefID());
                }
                if (CimStringUtils.isNotEmpty(deltaDCSetting.getDeltaDCDefID().getValue())) {
                    dcDefIDList.computeIfAbsent(deltaDCSetting.getDeltaDCDefID().getValue(), k -> deltaDCSetting.getDeltaDCDefID());
                }
            }
            for (Map.Entry<String, ObjectIdentifier> dcDefIDEntry : dcDefIDList.entrySet()) {
                ObjectIdentifier dcDefIDValue = dcDefIDEntry.getValue();
                Object[] dcDef = cimJpaRepository.queryOne("SELECT ID,EDC_PLAN_ID\n" +
                        "                         FROM   EDCPLAN\n" +
                        "                         WHERE  EDC_PLAN_ID = ?",dcDefIDValue.getValue());
                if (dcDef == null) {
                    continue;
                }
                // Check record count of DCItem
                long count = cimJpaRepository.count("SELECT COUNT(ID) \n" +
                        "                         FROM OMEDCPLAN_ITEM\n" +
                        "                         WHERE REFKEY = ?\n" +
                        "                         AND   NAME LIKE ?",dcDef[0], dcItemName);
                Infos.DCDefDataItem dcDefDataItem = new Infos.DCDefDataItem();
                dcDefDataItems.add(dcDefDataItem);
                setDCDefDataItemDetailInfos(dcItemName, count, dcDef, dcDefDataItem);
                dcDefDataItem.setDcDefID(new ObjectIdentifier(CimObjectUtils.toString(dcDef[1]), CimObjectUtils.toString(dcDef[0])));
            }
        }
        // --- Get all DCSpec and DeltaDCSpec informations
        // dataItemName
        if (CimStringUtils.isNotEmpty(dataItemName.get())) {
            dcItemName = dataItemName.get();
        } else {
            log.info("dataCollectionItemFillInEDCDataItemListByKeyInqDr(): dataItemName is empty, retrieve all dataItemName");
            dcItemName = "%";
        }
        //dcSpecGroupID
        String specGroup = null;
        if (CimStringUtils.isNotEmpty(dcSpecGroupID.get())) {
            specGroup = dcSpecGroupID.get();
        } else {
            log.info("dataCollectionItemFillInEDCDataItemListByKeyInqDr(): dcSpecGroupID is empty, retrieve all dcSpecGroupID");
            specGroup = "%";
        }

        // Specified DCSpecID
        if (CimStringUtils.equals(searchKeyPattern, BizConstant.SP_DCITEM_SEARCHKEYPATTERN_DCSPEC)) {
            log.info("dataCollectionItemFillInEDCDataItemListByKeyInqDr(): Specified DCSpecID {}", dcSpecID.get());
            // Check record count of all DCSpec and DeltaDCSpec
            long count = cimJpaRepository.count("SELECT COUNT(EDC_SPEC_ID) \n" +
                    "                     FROM    OMEDCSPEC\n" +
                    "                     WHERE   EDC_SPEC_ID LIKE ?",dcSpecID.get());
            if (count > 0) {
                List<Object[]> allDcSpecAndDeltaDcSpec = cimJpaRepository.query("SELECT  ID,EDC_SPEC_ID\n" +
                        "                         FROM    OMEDCSPEC\n" +
                        "                         WHERE   EDC_SPEC_ID LIKE ?\n" +
                        "                         ORDER BY EDC_SPEC_ID",dcSpecID.get());
                if (CimArrayUtils.isNotEmpty(allDcSpecAndDeltaDcSpec)) {
                    for (Object[] item : allDcSpecAndDeltaDcSpec) {
                        Infos.DCSpecDataItem dcSpecDataItem = new Infos.DCSpecDataItem();
                        dcSpecDataItems.add(dcSpecDataItem);
                        // Check record count of DCItem
                        count = cimJpaRepository.count("SELECT COUNT(REFKEY) \n" +
                                "                             FROM OMEDCSPEC_ITEM\n" +
                                "                             WHERE REFKEY = ?\n" +
                                "                             AND   SPEC_ITEM_NAME LIKE ?\n" +
                                "                             AND   EDC_SPEC_GROUP LIKE ?",item[0], specGroup, dcItemName);
                        setDCSpecDataItemDetailInfos(specGroup, dcItemName, count, item, dcSpecDataItem);
                        dcSpecDataItem.setDcSpecID(new ObjectIdentifier(CimObjectUtils.toString(item[1]), CimObjectUtils.toString(item[0])));
                    }
                }
            }
        } else if (routeOpeFlag.get()) {// Specified route operation
            log.info("dataCollectionItemFillInEDCDataItemListByKeyInqDr(): Specified route operation");
            Map<String, ObjectIdentifier> dcSpecIDList = new HashMap<>();
            for (Infos.DefaultRecipeWithDCID defaultRecipeSetting : defaultRecipeSettings) {
                if (CimStringUtils.isNotEmpty(defaultRecipeSetting.getDcSpecID().getValue())) {
                    dcSpecIDList.computeIfAbsent(defaultRecipeSetting.getDcSpecID().getValue(), k -> defaultRecipeSetting.getDcSpecID());
                }
            }
            for (Infos.EqpSpecificRecipeWithDCID eqpSpecificRecipeSetting : eqpSpecificRecipeSettings) {
                if (CimStringUtils.isNotEmpty(eqpSpecificRecipeSetting.getDcSpecID().getValue())) {
                    dcSpecIDList.computeIfAbsent(eqpSpecificRecipeSetting.getDcSpecID().getValue(), k -> eqpSpecificRecipeSetting.getDcSpecID());
                }
            }
            if (CimStringUtils.isNotEmpty(measurementDCSpecID.getValue())) {
                dcSpecIDList.putIfAbsent(measurementDCSpecID.getValue(), measurementDCSpecID);
            }
            for (Infos.DeltaDCSetting deltaDCSetting : deltaDCSettings) {
                if (CimStringUtils.isNotEmpty(deltaDCSetting.getDeltaDCSpecID().getValue())) {
                    dcSpecIDList.computeIfAbsent(deltaDCSetting.getDeltaDCSpecID().getValue(), k -> deltaDCSetting.getDeltaDCSpecID());
                }
            }
            for (Map.Entry<String, ObjectIdentifier> dcSpecIDEntry : dcSpecIDList.entrySet()) {
                ObjectIdentifier dcSpecIDValue = dcSpecIDEntry.getValue();
                Object[] dcSpec = cimJpaRepository.queryOne("SELECT ID,EDC_SPEC_ID\n" +
                        "                         FROM   OMEDCSPEC\n" +
                        "                         WHERE  EDC_SPEC_ID = ?",dcSpecIDValue.getValue());
                if (dcSpec == null) {
                    continue;
                }
                // Check record count of DCItem
                long count = cimJpaRepository.count("SELECT COUNT(REFKEY) \n" +
                        "                         FROM OMEDCSPEC_ITEM\n" +
                        "                         WHERE REFKEY = ?\n" +
                        "                         AND   SPEC_ITEM_NAME LIKE ?\n" +
                        "                         AND   EDC_SPEC_GROUP LIKE ?",
                        dcSpec[0], specGroup, dcItemName);
                Infos.DCSpecDataItem dcSpecDataItem = new Infos.DCSpecDataItem();
                dcSpecDataItems.add(dcSpecDataItem);
                setDCSpecDataItemDetailInfos(specGroup, dcItemName, count, dcSpec, dcSpecDataItem);
                dcSpecDataItem.setDcSpecID(new ObjectIdentifier(CimObjectUtils.toString(dcSpec[1]), CimObjectUtils.toString(dcSpec[0])));
            }
        }
        //Step4 - Return to Caller
        operationDataCollectionSetting.setRecipeType(recipeType.get());
        operationDataCollectionSetting.setMeasurementDCSpecID(measurementDCSpecID);
        return data;
    }

    private void setDCDefDataItemDetailInfos(String dcItemName, long count, Object[] item, Infos.DCDefDataItem dcDefDataItem) {
        if (count > 0) {
            List<Object[]> dataCollectionDefItems = cimJpaRepository.query("SELECT NAME\n" +
                    "                                       ,DESCRIPTION\n" +
                    "                                       ,EDCITEM_MODE\n" +
                    "                                       ,UNIT\n" +
                    "                                       ,DATA_TYPE\n" +
                    "                                       ,ITEM_TYPE\n" +
                    "                                       ,MEAS_TYPE\n" +
                    "                                       ,WAFER_POS\n" +
                    "                                       ,SITE_POS\n" +
                    "                                       ,HIST_REQD\n" +
                    "                                       ,CALC_TYPE\n" +
                    "                                       ,CALC_EXPR\n" +
                    "                                       ,TAG\n" +
                    "                                 FROM OMEDCPLAN_ITEM\n" +
                    "                                 WHERE REFKEY = ?\n" +
                    "                                 AND   NAME LIKE ?\n" +
                    "                                 ORDER BY IDX_NO",item[0],dcItemName);
            if (CimArrayUtils.isEmpty(dataCollectionDefItems)) {
                return;
            }
            List<Infos.DCDefDataItemDetailInfo> dcDefDataItemDetailInfos = new ArrayList<>();
            dcDefDataItem.setDcDefDataItemDetailInfoSeq(dcDefDataItemDetailInfos);
            for (Object[] dataCollectionDefItem : dataCollectionDefItems) {
                Infos.DCDefDataItemDetailInfo dcDefDataItemDetailInfo = new Infos.DCDefDataItemDetailInfo();
                dcDefDataItemDetailInfos.add(dcDefDataItemDetailInfo);
                dcDefDataItemDetailInfo.setDataItemName(CimObjectUtils.toString(dataCollectionDefItem[0]));
                dcDefDataItemDetailInfo.setDescription(CimObjectUtils.toString(dataCollectionDefItem[1]));
                dcDefDataItemDetailInfo.setDataCollectionMode(CimObjectUtils.toString(dataCollectionDefItem[2]));
                dcDefDataItemDetailInfo.setDataCollectionUnit(CimObjectUtils.toString(dataCollectionDefItem[3]));
                dcDefDataItemDetailInfo.setDataType(CimObjectUtils.toString(dataCollectionDefItem[4]));
                dcDefDataItemDetailInfo.setItemType(CimObjectUtils.toString(dataCollectionDefItem[5]));
                dcDefDataItemDetailInfo.setMeasurementType(CimObjectUtils.toString(dataCollectionDefItem[6]));
                dcDefDataItemDetailInfo.setWaferPosition(CimObjectUtils.toString(dataCollectionDefItem[7]));
                dcDefDataItemDetailInfo.setSitePosition(CimObjectUtils.toString(dataCollectionDefItem[8]));
                dcDefDataItemDetailInfo.setHistoryRequiredFlag(CimBooleanUtils.convert(dataCollectionDefItem[9]));
                dcDefDataItemDetailInfo.setCalculationType(CimObjectUtils.toString(dataCollectionDefItem[10]));
                dcDefDataItemDetailInfo.setCalculationExpression(CimObjectUtils.toString(dataCollectionDefItem[11]));
                dcDefDataItemDetailInfo.setTag(CimObjectUtils.toString(dataCollectionDefItem[12]));
            }
        }
    }

    private void setDCSpecDataItemDetailInfos(String specGroup, String dcItemName, long count, Object[] item, Infos.DCSpecDataItem dcSpecDataItem) {
        if (count > 0) {
            List<Object[]> dataCollectionSpecItems = cimJpaRepository.query("SELECT SPEC_ITEM_NAME\n" +
                    "                                       ,SCRN_UP_FLAG\n" +
                    "                                       ,SCRN_UP_LIMIT\n" +
                    "                                       ,SCRN_UP_ACT\n" +
                    "                                       ,SCRN_LO_FLAG\n" +
                    "                                       ,SCRN_LO_LIMIT\n" +
                    "                                       ,SCRN_LO_ACT\n" +
                    "                                       ,SPEC_UP_FLAG\n" +
                    "                                       ,SPEC_UP_LIMIT\n" +
                    "                                       ,SPEC_UP_ACT\n" +
                    "                                       ,SPEC_LO_FLAG\n" +
                    "                                       ,SPEC_LO_LIMIT\n" +
                    "                                       ,SPEC_LO_ACT\n" +
                    "                                       ,CTRL_UP_FLAG\n" +
                    "                                       ,CTRL_UP_LIMIT\n" +
                    "                                       ,CTRL_UP_ACT\n" +
                    "                                       ,CTRL_LO_FLAG\n" +
                    "                                       ,CTRL_LO_LIMIT\n" +
                    "                                       ,CTRL_LO_ACT\n" +
                    "                                       ,SPEC_ITEM_TARGET\n" +
                    "                                       ,SPEC_ITEM_TAG\n" +
                    "                                       ,EDC_SPEC_GROUP\n" +
                    "                                 FROM OMEDCSPEC_ITEM\n" +
                    "                                 WHERE REFKEY = ?\n" +
                    "                                 AND   SPEC_ITEM_NAME LIKE ?\n" +
                    "                                 AND   EDC_SPEC_GROUP LIKE ?\n" +
                    "                                 ORDER BY LINK_KEY",item[0], specGroup, dcItemName);
            if (CimArrayUtils.isEmpty(dataCollectionSpecItems)) {
                return;
            }
            List<Infos.DCSpecDetailInfo> dcSpecDetailInfos = new ArrayList<>();
            dcSpecDataItem.setDcSpecDetailInfoSeq(dcSpecDetailInfos);
            setDCSpecDetailInfo(dataCollectionSpecItems, dcSpecDetailInfos);
        }
    }

    private void setDCSpecDetailInfo(List<Object[]> dataCollectionSpecItems, List<Infos.DCSpecDetailInfo> dcSpecDetailInfos) {
        for (Object[] dataCollectionSpecItem : dataCollectionSpecItems) {
            Infos.DCSpecDetailInfo dcSpecDetailInfo = new Infos.DCSpecDetailInfo();
            dcSpecDetailInfos.add(dcSpecDetailInfo);
            dcSpecDetailInfo.setDataItemName(CimObjectUtils.toString(dataCollectionSpecItem[0]));
            dcSpecDetailInfo.setScreenLimitUpperRequired(CimBooleanUtils.convert(dataCollectionSpecItem[1]));
            dcSpecDetailInfo.setScreenLimitUpper(CimDoubleUtils.doubleValue(dataCollectionSpecItem[2]));
            dcSpecDetailInfo.setActionCodes_uscrn(CimObjectUtils.toString(dataCollectionSpecItem[3]));
            dcSpecDetailInfo.setScreenLimitLowerRequired(CimBooleanUtils.convert(dataCollectionSpecItem[4]));
            dcSpecDetailInfo.setScreenLimitLower(CimDoubleUtils.doubleValue(dataCollectionSpecItem[5]));
            dcSpecDetailInfo.setActionCodes_lscrn(CimObjectUtils.toString(dataCollectionSpecItem[6]));
            dcSpecDetailInfo.setSpecLimitUpperRequired(CimBooleanUtils.convert(dataCollectionSpecItem[7]));
            dcSpecDetailInfo.setSpecLimitUpper(CimDoubleUtils.doubleValue(dataCollectionSpecItem[8]));
            dcSpecDetailInfo.setActionCodes_usl(CimObjectUtils.toString(dataCollectionSpecItem[9]));
            dcSpecDetailInfo.setSpecLimitLowerRequired(CimBooleanUtils.convert(dataCollectionSpecItem[10]));
            dcSpecDetailInfo.setSpecLimitLower(CimDoubleUtils.doubleValue(dataCollectionSpecItem[11]));
            dcSpecDetailInfo.setActionCodes_lsl(CimObjectUtils.toString(dataCollectionSpecItem[12]));
            dcSpecDetailInfo.setControlLimitUpperRequired(CimBooleanUtils.convert(dataCollectionSpecItem[13]));
            dcSpecDetailInfo.setControlLimitUpper(CimDoubleUtils.doubleValue(dataCollectionSpecItem[14]));
            dcSpecDetailInfo.setActionCodes_ucl(CimObjectUtils.toString(dataCollectionSpecItem[15]));
            dcSpecDetailInfo.setControlLimitLowerRequired(CimBooleanUtils.convert(dataCollectionSpecItem[16]));
            dcSpecDetailInfo.setControlLimitLower(CimDoubleUtils.doubleValue(dataCollectionSpecItem[17]));
            dcSpecDetailInfo.setActionCodes_lcl(CimObjectUtils.toString(dataCollectionSpecItem[18]));
            dcSpecDetailInfo.setTarget(CimDoubleUtils.doubleValue(dataCollectionSpecItem[19]));
            dcSpecDetailInfo.setTag(CimObjectUtils.toString(dataCollectionSpecItem[20]));
            dcSpecDetailInfo.setDcSpecGroup(CimObjectUtils.toString(dataCollectionSpecItem[21]));
        }
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * -------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon              objCommon
     * @param objectID               objectID
     * @param maxCount               maxCount
     * @param dcType                 dcType
     * @param FPCCategory            FPCCategory
     * @param whiteDefSearchCriteria whiteDefSearchCriteria
     * @return RetCode<List                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               <                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               Infos.DataCollection>>
     * @author ZQI
     * @date 2018/12/12 16:39:42
     */
    @Override
    public List<Infos.DataCollection> dcDefListGetDR(Infos.ObjCommon objCommon, ObjectIdentifier objectID, String dcType, String whiteDefSearchCriteria, Long maxCount, String FPCCategory) {
        log.info("dcDefListGetDR: DataCollectionMethod dcDefListGetDR(...)");

        log.info("in para objectID={}",               objectID.getValue());
        log.info("in para dcType={}",                 dcType);
        log.info("in para whiteDefSearchCriteria={}", whiteDefSearchCriteria);
        log.info("in para maxCount={}",               maxCount);
        log.info("in para FPCCategory={}",            FPCCategory);

        String HV_BUFFER            ="";
        String HV_TMPBUFFER         ="";
        String hFRDCDEFDCDEF_ID     ="";
        String hFRDCDEFDCDEF_OBJ    ="";
        String hFRDCDEFDESCRIPTION  ="";
        String hFRDCDEFDC_TYPE      ="";
        Boolean hFRDCDEFWHITE_FLAG = false;
        String hFRDCDEFFPC_CATEGORY="";

        Boolean firstCondition = true;

        HV_BUFFER="SELECT EDC_PLAN_ID, ID, DESCRIPTION, EDC_TYPE, "+
                "TEMP_MODE_FLAG, DOC_CATEGORY FROM OMEDCPLAN ";

        if( CimStringUtils.length(objectID.getValue()) != 0 ) {
            if( firstCondition ) {
                HV_BUFFER+= "WHERE ";
                firstCondition = false;
            } else {
                HV_BUFFER+="AND ";
            }

            HV_TMPBUFFER=String.format("EDC_PLAN_ID LIKE '%s' ",
                    objectID.getValue());
            HV_BUFFER+= HV_TMPBUFFER;
        }

        if( CimStringUtils.length(dcType)!= 0 &&
            (CimStringUtils.equals(dcType, BizConstant.SP_DCTYPE_PROCESS) ||
            CimStringUtils.equals(dcType, BizConstant.SP_DCTYPE_MEASUREMENT)) ) {
            if( firstCondition ) {
                HV_BUFFER+= "WHERE ";
                firstCondition = false;
            } else {
                HV_BUFFER+= "AND ";
            }

            HV_TMPBUFFER=String.format("EDC_TYPE = '%s' ",dcType);
            HV_BUFFER+= HV_TMPBUFFER;
        }

        if( CimStringUtils.equals(whiteDefSearchCriteria, BizConstant.SP_WHITEDEF_SEARCHCRITERIA_WHITE) ) {
            if( firstCondition ) {
                HV_BUFFER+= "WHERE ";
                firstCondition = false;
            } else {
                HV_BUFFER+= "AND ";
            }

            HV_BUFFER+= "TEMP_MODE_FLAG = 1 ";
        } else if( CimStringUtils.equals(whiteDefSearchCriteria, BizConstant.SP_WHITEDEF_SEARCHCRITERIA_NONWHITE) ) {
            if( firstCondition ) {
                HV_BUFFER+= "WHERE " ;
                firstCondition = false;
            } else {
                HV_BUFFER+= "AND ";
            }

            HV_BUFFER+= "TEMP_MODE_FLAG != 1 ";
        }

        if( CimStringUtils.length(FPCCategory) != 0 ) {
            if( firstCondition ) {
                HV_BUFFER+= "WHERE ";
                firstCondition = false;
            } else {
                HV_BUFFER+= "AND ";
            }

            HV_TMPBUFFER=String.format("(DOC_CATEGORY = '%s' OR DOC_CATEGORY = '') ", FPCCategory);
            HV_BUFFER+= HV_TMPBUFFER;
        }

        String originalSQL;
        originalSQL="";
        originalSQL= HV_BUFFER;

        log.info("SQL :", HV_BUFFER);

        List<Object[]> DL1=cimJpaRepository.query(HV_BUFFER);

        int DCDEFLEN = 0;
        int count    = 0;

        log.info("maxCount = {}", maxCount);
        int limitCount          = 0;
        boolean bExceedFlag      = false;
        if( maxCount < 1 || maxCount > 9999 ) {
            DCDEFLEN   = 100;
            limitCount = 100;
        } else {
            DCDEFLEN   = 1000;
            limitCount = maxCount.intValue();
        }

        log.info("limitCount {}", limitCount);

        List<Infos.DataCollection> strDataCollectionList;
        strDataCollectionList=new ArrayList<>( DCDEFLEN );

        for (Object[] dl:DL1) {
            hFRDCDEFDCDEF_ID     ="";
            hFRDCDEFDCDEF_OBJ    ="";
            hFRDCDEFDESCRIPTION  ="";
            hFRDCDEFDC_TYPE      ="";
            hFRDCDEFWHITE_FLAG = false;
            hFRDCDEFFPC_CATEGORY ="";

            hFRDCDEFDCDEF_ID= CimObjectUtils.toString(dl[0]);
            hFRDCDEFDCDEF_OBJ= CimObjectUtils.toString(dl[1]);
            hFRDCDEFDESCRIPTION= CimObjectUtils.toString(dl[2]);
            hFRDCDEFDC_TYPE= CimObjectUtils.toString(dl[3]);
            hFRDCDEFWHITE_FLAG= CimBooleanUtils.convert(dl[4]);
            hFRDCDEFFPC_CATEGORY= CimObjectUtils.toString(dl[5]);

            if( count >= DCDEFLEN && count <= limitCount ) {
                DCDEFLEN += 1000;
                strDataCollectionList=new ArrayList<>( DCDEFLEN );
            }

            strDataCollectionList.add(new Infos.DataCollection());
            strDataCollectionList.get(count).setObjectID(ObjectIdentifier.build(hFRDCDEFDCDEF_ID,hFRDCDEFDCDEF_OBJ));
            strDataCollectionList.get(count).setObjectType                          ( BizConstant.SP_DCDEFINITION);
            strDataCollectionList.get(count).setDcType                              ( hFRDCDEFDC_TYPE);
            strDataCollectionList.get(count).setDescription                         ( hFRDCDEFDESCRIPTION);
            strDataCollectionList.get(count).setWhiteDefFlag                        ( hFRDCDEFWHITE_FLAG);
            strDataCollectionList.get(count).setFPCCategory                         ( hFRDCDEFFPC_CATEGORY);

            count++;

            if( count > limitCount ) {
                log.info("count exceeded limit count={}", count);
                bExceedFlag = true;
                count = limitCount;
                break;
            }
        }

        Validations.check(bExceedFlag,retCodeConfigEx.getNoticeMaxcountOver());

        return strDataCollectionList;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * -------------------------------------------------------------------------------------------------------------------
     * 2018/12/16       ###                ZQI                update input params(add the param of logicalRecipe).
     *                                                        findDataCollectionDefinitionForSubLotType(logicalRecipe,,,)
     *                                                        findMachineRecipeFor(logicalRecipe, lot, eqp)
     *                                                        findMachineRecipeForSubLotTypeOld(logicalRecipe, eqp, subLotType)
     * @param objCommon              objCommon
     * @param pdID                   pdID
     * @param lotID                  lotID
     * @param machineRecipeID        machineRecipeID
     * @param equipmentID            equipmentID
     * @param fpcCategory            fpcCategory
     * @param whiteDefSearchCriteria whiteDefSearchCriteria
     * @return RetCode<List                               <                               Infos.DataCollection>>
     * @author ZQI
     * @date 2018/12/12 16:39:48
     */
    @Override
    public List<Infos.DataCollection> dcDefListGetFromPD(Infos.ObjCommon objCommon, ObjectIdentifier lotID, ObjectIdentifier equipmentID, ObjectIdentifier machineRecipeID, ObjectIdentifier pdID, String whiteDefSearchCriteria, String fpcCategory) {
        log.info("DataCollectionMethod: dcDefListGetFromPD(...)");

        /*----------------*/
        /*   Initialize   */
        /*----------------*/
        int searchCondition = 0;
        String searchCondition_var = StandardProperties.OM_CONSTRAINT_CHK_WITH_SEARCH.getValue();
        if (!CimStringUtils.isEmpty(searchCondition_var)) {
            searchCondition = Integer.parseInt(searchCondition_var);
        }
        Validations.check(ObjectIdentifier.isEmptyWithValue(lotID) || ObjectIdentifier.isEmptyWithValue(pdID), retCodeConfig.getInvalidInputParam());

        // Get the lot.
        com.fa.cim.newcore.bo.product.CimLot lot = null;
        lot = baseCoreFactory.getBO(com.fa.cim.newcore.bo.product.CimLot.class,lotID);
        // Get the processdefinition.
        com.fa.cim.newcore.bo.pd.CimProcessDefinition processDefinition = null;
        processDefinition = baseCoreFactory.getBO(com.fa.cim.newcore.bo.pd.CimProcessDefinition.class,pdID);
        // Get the mrecipe.
        Boolean bMRcpSpeFlag = false;
        com.fa.cim.newcore.bo.recipe.CimMachineRecipe machineRecipe = null;
        if (!ObjectIdentifier.isEmpty(machineRecipeID)) {
            bMRcpSpeFlag = true;
            machineRecipe = baseCoreFactory.getBO(com.fa.cim.newcore.bo.recipe.CimMachineRecipe.class,machineRecipeID);
        }
        // Get the eqp.
        Boolean bEqpSpeFlag = false;
        CimMachine equipment = null;
        if (!ObjectIdentifier.isEmpty(equipmentID)) {
            bEqpSpeFlag = true;
            equipment = baseCoreFactory.getBO(CimMachine.class,equipmentID);
        }
        // Get SubLotType
        String subLotType = null;
        if (lot!=null) {
            subLotType = lot.getSubLotType();
        }
        // Get productspec
        com.fa.cim.newcore.bo.prodspec.CimProductSpecification productSpecification = null;
        if (lot!=null) {
            productSpecification = lot.getProductSpecification();
        }
        Validations.check(null == productSpecification, retCodeConfig.getNotFoundProductSpec());

        // Get logicalrecipe
        // Get logicalrecipe by productspec
        com.fa.cim.newcore.bo.recipe.CimLogicalRecipe logicalRecipe =processDefinition.findLogicalRecipeFor(productSpecification);
        Validations.check(null == logicalRecipe, retCodeConfig.getNotFoundLogicalRecipe());

        // new a input param.
        ObjectIdentifier logicalRecipeID = new ObjectIdentifier();
        logicalRecipeID.setValue(logicalRecipe.getIdentifier());
        logicalRecipeID.setReferenceKey(logicalRecipe.getPrimaryKey());
        /*----------------------*/
        /*   Main process       */
        /*----------------------*/
        CimDataCollectionDefinition dataCollectionDef = null;
        List<Infos.DataCollection> strDataCollectionList = new ArrayList<>();
        List<Infos.DataCollection> tmpDataCollectionList = new ArrayList<>();

        List<Infos.DefaultRecipeSetting> defaultRecipeSetting = null;
        ObjectIdentifier mRcpID = new ObjectIdentifier();

        if (bEqpSpeFlag && bMRcpSpeFlag) {
            // Get DC info by machineRecipe, machine and subLotType
            log.info("equipmentID and machineRecipeID is specified.");
            // Get PosDataCollection
            // update: add the logicalRecipe as a param into the method of findDataCollectionDefinitionForSubLotType.
            try {
                dataCollectionDef = logicalRecipe.findDataCollectionDefinitionForSubLotType(equipment, machineRecipe, subLotType);
            } catch (NotFoundRecordException ex) {
                if (Validations.isEquals(ex.getCoreCode(), retCodeConfig.getNotFoundDcdef())) {
                    Validations.check(retCodeConfigEx.getCheckDcdefAndRecipeSetting());
                }
                throw ex;
            }
            if (dataCollectionDef == null){
                return null;
            }
            Infos.DataCollection dataCollection = new Infos.DataCollection();
            dataCollection.setObjectID(new ObjectIdentifier(dataCollectionDef.getIdentifier(), dataCollectionDef.getPrimaryKey()));
            strDataCollectionList.add(dataCollection);
        } else {
            // Get all DC info which is related to the logicalrecipe
            log.info("equipmentID or machineRecipeID or both is not specified.");
            // Get all DefaultRecipeSetting
            defaultRecipeSetting = logicalRecipeMethod.logicalRecipeAllDefaultRecipeSettingGetDR(objCommon, logicalRecipeID, false, false, false);

            if (bEqpSpeFlag) {
                log.info("EquipmentID is specified");
                if (searchCondition == 1) {
                    // update: add the logicalRecipe as a param into the method of findMachineRecipeFor.
                    com.fa.cim.newcore.bo.recipe.CimMachineRecipe machineRecipeFor = logicalRecipe.findMachineRecipeFor( lot, equipment);
                    machineRecipe = machineRecipeFor;
                } else {
                    // update: add the logicalRecipe as a param into the method of findMachineRecipeForSubLotTypeOld.
                    com.fa.cim.newcore.bo.recipe.CimMachineRecipe machineRecipeFor = logicalRecipe.findMachineRecipeForSubLotType( equipment, subLotType);
                    machineRecipe = machineRecipeFor;
                }
                Validations.check(CimObjectUtils.isEmpty(machineRecipe), retCodeConfig.getNotFoundMachineRecipe());

                mRcpID.setValue(machineRecipe.getIdentifier());
                mRcpID.setReferenceKey(machineRecipe.getPrimaryKey());
            }

            List<Infos.DefaultRecipeSetting> strDefaultRecipeSettings = defaultRecipeSetting;
            for (Infos.DefaultRecipeSetting d : strDefaultRecipeSettings) {
                if (bMRcpSpeFlag) {
                    log.info("If machineRecipeID is specified, get appropriate DCDef only.");
                    if (CimStringUtils.equals(d.getRecipe().getValue(), machineRecipeID.getValue())) {
                        Infos.DataCollection dataCollection = new Infos.DataCollection();
                        dataCollection.setObjectID(d.getDcDefinition());
                        tmpDataCollectionList.add(dataCollection);
                    }
                } else if (bEqpSpeFlag) {
                    log.info("If equipmentID is specified, get appropriate DCDef only.");
                    if (CimStringUtils.equals(d.getRecipe().getValue(), mRcpID.getValue())) {
                        Infos.DataCollection dataCollection = new Infos.DataCollection();
                        dataCollection.setObjectID(d.getDcDefinition());
                        tmpDataCollectionList.add(dataCollection);
                    }
                } else {
                    Infos.DataCollection dataCollection = new Infos.DataCollection();
                    dataCollection.setObjectID(d.getDcDefinition());
                    strDataCollectionList.add(dataCollection);
                }
            }

            if (bMRcpSpeFlag || bEqpSpeFlag) {
                // Replace strDataCollectionList
                strDataCollectionList = new ArrayList<>(tmpDataCollectionList);
            }
            //duplicate check
            log.info("Check duplicate. current data collection count is", strDataCollectionList.size());
            tmpDataCollectionList.clear();
            Boolean firstCondition = true;
            Boolean bFindFlag = false;
            for (Infos.DataCollection d : strDataCollectionList) {
                if (firstCondition) {
                    firstCondition = false;
                    tmpDataCollectionList.add(d);
                } else {
                    for (Infos.DataCollection dataCollection : tmpDataCollectionList) {
                        if (CimStringUtils.equals(d.getObjectID().getValue(), dataCollection.getObjectID().getValue())) {
                            log.info("This data was already gotten.");
                            bFindFlag = true;
                            break;
                        }
                    }
                    if (!bFindFlag) {
                        tmpDataCollectionList.add(d);
                    }
                }
            }
            strDataCollectionList = new ArrayList<>(tmpDataCollectionList);
        }
        /*----------------------*/
        /*   Set Out Structure  */
        /*----------------------*/
        log.info("default recipe settings's length.", CimArrayUtils.getSize(strDataCollectionList));
        String tmpFPCCategory = null;
        Boolean tmpWhiteFlag = false;
        Boolean tmpFoundFlag = false;
        int count = 0;
        tmpDataCollectionList.clear();
        for (Infos.DataCollection d : strDataCollectionList) {
            tmpFoundFlag = false;
            dataCollectionDef = baseCoreFactory.getBO(CimDataCollectionDefinition.class, d.getObjectID());
            tmpFPCCategory = dataCollectionDef.getFPCCategory();
            log.info("DCDef ID  and tmpFPCCategory",d.getObjectID().getValue(),tmpFPCCategory);
            tmpWhiteFlag = dataCollectionDef.isWhiteFlagOn();
            if (CimStringUtils.equals(whiteDefSearchCriteria, BizConstant.SP_WHITEDEF_SEARCHCRITERIA_NONWHITE)) {
                //Not get it whiteDefFlag is TRUE.
                log.info("whiteDefCriteria is NoWhite. not get it whiteDefFlag is TRUE.");
                if (tmpWhiteFlag) {
                    log.info("DCDef white Definition Flag is TRUE. ");
                    continue;
                }
            } else if (CimStringUtils.equals(whiteDefSearchCriteria, BizConstant.SP_WHITEDEF_SEARCHCRITERIA_WHITE)) {
                //Get it whiteDefFlag is TRUE only.
                log.info("whiteDefCriteria is White. get it whiteDefFlag is TRUE only.");
                if (!tmpWhiteFlag) {
                    log.info("DCDef white Definition Flag is FALSE. ");
                    continue;
                }
            } else if (CimStringUtils.equals(whiteDefSearchCriteria, BizConstant.SP_WHITEDEF_SEARCHCRITERIA_ALL)) {
                log.info("whiteDefCriteria is All. get it without regards to whiteDefFlag.");
            } else {
                log.info("Unexpected Criteria. continue...", whiteDefSearchCriteria);
                continue;
            }
            // Check FPC Category matching.
            if (!CimStringUtils.isEmpty(fpcCategory) && !CimStringUtils.isEmpty(tmpFPCCategory)) {
                if (!CimStringUtils.equals(tmpFPCCategory, fpcCategory)) {
                    // Not get it FPC Category is mismatch.
                    continue;
                }
            }

            log.info("Equal specified FPCCategory, or specified FPCCategory is null");
            Infos.DataCollection dataCollection = new Infos.DataCollection();
            dataCollection.setObjectID(d.getObjectID());
            dataCollection.setFPCCategory(tmpFPCCategory);
            dataCollection.setWhiteDefFlag(tmpWhiteFlag);
            dataCollection.setObjectType(BizConstant.SP_DCDEFINITION);
            dataCollection.setDcType(dataCollectionDef.getCollectionType());
            dataCollection.setDescription(dataCollectionDef.getDescription());
            tmpDataCollectionList.add(count, dataCollection);
            count ++;
        }
        return tmpDataCollectionList;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * -------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon              objCommon
     * @param dcDefID                dcDefID
     * @param objectID               objectID
     * @param maxCount               maxCount
     * @param fpcCategory            fpcCategory
     * @param whiteDefSearchCriteria whiteDefSearchCriteria
     * @return RetCode<List                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               <                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               Infos.DataCollection>>
     * @author ZQI
     * @date 2018/12/12 16:39:55
     */
    @Override
    public List<Infos.DataCollection> dcSpecListGetDR(Infos.ObjCommon objCommon, ObjectIdentifier dcDefID, ObjectIdentifier objectID, String whiteDefSearchCriteria, Long maxCount, String fpcCategory) {
        log.info("dcSpecListGetDR: DataCollectionMethod dcSpecListGetDR(...)");
        List<Infos.DataCollection> resultList = new ArrayList<>();
        Boolean tmpFlag = false;
        Boolean firstCondition = true;
        String sql;
        if (CimStringUtils.isEmpty(dcDefID.getValue())) {
            sql = "SELECT EDC_SPEC_ID, OMEDCSPEC.ID, DESCRIPTION, TEMP_MODE_FLAG, DOC_CATEGORY FROM OMEDCSPEC ";
            if (!CimStringUtils.isEmpty(objectID.getValue())) {
                firstCondition = false;
                sql += "WHERE ";
                sql += String.format("EDC_SPEC_ID LIKE '%s' ", objectID.getValue());
            }
        } else {
            tmpFlag = true;
            sql = "SELECT EDC_SPEC_ID, OMEDCSPEC.ID, DESCRIPTION, TEMP_MODE_FLAG, DOC_CATEGORY " +
                    "FROM OMEDCSPEC, OMEDCSPEC_EDCPLAN " +
                    "WHERE OMEDCSPEC.ID = OMEDCSPEC_EDCPLAN.REFKEY ";
            if (!CimStringUtils.isEmpty(objectID.getValue())) {
                sql += String.format("and OMEDCSPEC.EDC_SPEC_ID LIKE '%s' ", objectID.getValue());
            }
            sql += String.format("and OMEDCSPEC_EDCPLAN.EDC_PLAN_ID = '%s' ", dcDefID.getValue());
        }

        if (tmpFlag) {
            if (CimStringUtils.equals(whiteDefSearchCriteria, BizConstant.SP_WHITEDEF_SEARCHCRITERIA_WHITE)) {
                sql += "and OMEDCSPEC.TEMP_MODE_FLAG = 1 ";
            } else if (CimStringUtils.equals(whiteDefSearchCriteria, BizConstant.SP_WHITEDEF_SEARCHCRITERIA_NONWHITE)) {
                sql += "and OMEDCSPEC.TEMP_MODE_FLAG != 1 ";
            }
            if (!CimStringUtils.isEmpty(fpcCategory)) {
                sql += String.format("AND (OMEDCSPEC.DOC_CATEGORY = '%s' OR OMEDCSPEC.DOC_CATEGORY IS NULL) ", fpcCategory);
            }
        } else {
            if (CimStringUtils.equals(whiteDefSearchCriteria, BizConstant.SP_WHITEDEF_SEARCHCRITERIA_WHITE)) {
                if (firstCondition) {
                    firstCondition = false;
                    sql += "WHERE OMEDCSPEC.TEMP_MODE_FLAG = 1 ";
                } else {
                    sql += "AND OMEDCSPEC.TEMP_MODE_FLAG = 1 ";
                }
            } else if (CimStringUtils.equals(whiteDefSearchCriteria, BizConstant.SP_WHITEDEF_SEARCHCRITERIA_NONWHITE)) {
                if (firstCondition) {
                    firstCondition = false;
                    sql += "WHERE OMEDCSPEC.TEMP_MODE_FLAG != 1 ";
                } else {
                    sql += "AND OMEDCSPEC.TEMP_MODE_FLAG != 1 ";
                }
            }
            if (!CimStringUtils.isEmpty(fpcCategory)) {
                if (firstCondition) {
                    firstCondition = false;
                    sql += String.format(" WHERE (OMEDCSPEC.DOC_CATEGORY = '%s' OR OMEDCSPEC.DOC_CATEGORY IS NULL) ", fpcCategory);
                } else {
                    sql += String.format(" AND (OMEDCSPEC.DOC_CATEGORY = '%s' OR OMEDCSPEC.DOC_CATEGORY IS NULL) ", fpcCategory);
                }
            }
        }
        log.info("CimDataCollectionImpl - getDCSpecList - the SQL is: " + sql);

        List<Object[]> list = cimJpaRepository.query(sql);
        if (CimArrayUtils.isEmpty(list)) {
            log.info("CimDataCollectionImpl - getDCSpecList: the result is null.");
            return resultList;
        }
        for (Object obj[] : list) {
            Infos.DataCollection dataCollection = new Infos.DataCollection();
            dataCollection.setObjectID(new ObjectIdentifier((String) obj[0], (String) obj[1]));
            dataCollection.setObjectType(BizConstant.SP_DCSPECIFICATION);
            dataCollection.setDescription((String) obj[2]);
            dataCollection.setWhiteDefFlag(CimBooleanUtils.getBoolean(obj[3] == null ? null : obj[3].toString()));
            dataCollection.setFPCCategory((String) obj[4]);
            resultList.add(dataCollection);
        }
        return resultList;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param searchKeys searchKeys
     * @param params     params
     * @author PlayBoy
     * @date 2018/11/21 15:20:30
     */
    private void searchKeyPatternProcess(List<Infos.HashedInfo> searchKeys, SearchKeyPatternParam... params) {
        if (params == null) {
            return;
        }
        List<SearchKeyPatternParam> searchKeyPatternParams = new ArrayList<>(Arrays.asList(params));
        searchKeyPatternValidation(searchKeyPatternParams, searchKeys);
    }

    /**
     * description:
     * <p>Search Key Pattern Validation</p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param params     params
     * @param searchKeys searchKeys
     * @author PlayBoy
     * @date 2018/11/21 15:19:57
     */
    private void searchKeyPatternValidation(List<SearchKeyPatternParam> params, List<Infos.HashedInfo> searchKeys) {
        if (params == null || params.isEmpty()) {
            return;
        }
        if (!CimArrayUtils.isEmpty(searchKeys)) {
            searchKeyLoop:
            for (Infos.HashedInfo searchKey : searchKeys) {
                for (SearchKeyPatternParam param : params) {
                    if (CimStringUtils.equals(searchKey.getHashKey(), param.getHashKey())) {
                        param.setFlag(true);
                        param.getStrValue().set(searchKey.getHashData());
                        if (params.size() == 1) {
                            break searchKeyLoop;
                        }
                    }
                }
            }
        }
        for (SearchKeyPatternParam param : params) {
            Validations.check(!param.isFlag(), retCodeConfig.getSearchKeyMismatch());
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    private class SearchKeyPatternParam {
        private boolean flag;
        private String hashKey;
        private AtomicReference<String> strValue;

        SearchKeyPatternParam(boolean pFlag, String pHashKey, AtomicReference<String> pStrValue) {
            this.flag = pFlag;
            this.hashKey = pHashKey;
            this.strValue = pStrValue;
        }
    }

    @Override
    public void collectedDataCheckConditionForDataStore(Infos.ObjCommon objCommon, Inputs.CollectedDataCheckConditionForDataStoreIn inputs) {
        //put input parameter;
        ObjectIdentifier controlJobID = inputs.getControlJobID();
        List<Infos.CollectedDataItemStruct> strCDItemSeq = inputs.getCollectedDataItemList();
        //Check DataValue length(Pick up lot IDs in the same time);
        int lenDCItem = CimArrayUtils.getSize(strCDItemSeq);
        List<Infos.LotInCassette> strTmpLotIDSeq = new ArrayList<>();
        String longStringValue = StandardProperties.OM_EXPAND_STRING_VALUE.getValue();
        int count = 0;
        int lenTempLotSeq = 0;
        boolean bFindFlag = false;
        int lenData = 0;
        for (int i = 0; i < lenDCItem; i++){
            bFindFlag = false;
            List<Infos.CollectedDataStruct> collectedDataList = strCDItemSeq.get(i).getCollectedDataList();
            lenData = CimArrayUtils.getSize(collectedDataList);
            if (lenData > 0){
                // search the lot in existing strTmpLotIDSeq
                lenTempLotSeq = CimArrayUtils.getSize(strTmpLotIDSeq);
                int nTmpLotIndex = 0;
                for (int iCnt1 = 0; iCnt1 < count; iCnt1++){
                    if (ObjectIdentifier.equalsWithValue(strCDItemSeq.get(i).getLotID(), strTmpLotIDSeq.get(iCnt1).getLotID())){
                        bFindFlag = true;
                        nTmpLotIndex = iCnt1;
                        break;
                    }
                }
                if (!bFindFlag){
                    //-----------------------
                    // Add lot ID to strTmpLotIDSeq
                    //-----------------------
                    Infos.LotInCassette lotInCassette = new Infos.LotInCassette();
                    strTmpLotIDSeq.add(lotInCassette);
                    lotInCassette.setLotID(strCDItemSeq.get(i).getLotID());
                    Infos.StartRecipe startRecipe = new Infos.StartRecipe();
                    lotInCassette.setStartRecipe(startRecipe);
                    List<Infos.DataCollectionInfo> dcDefList = new ArrayList<>();
                    startRecipe.setDcDefList(dcDefList);
                    Infos.DataCollectionInfo dataCollectionInfo = new Infos.DataCollectionInfo();
                    dcDefList.add(dataCollectionInfo);
                    dataCollectionInfo.setDataCollectionDefinitionID(strCDItemSeq.get(i).getDataCollectionDefinitionID());
                    count++;
                } else {
                    boolean bDCDefFindFlag = false;
                    List<Infos.DataCollectionInfo> dcDefList = strTmpLotIDSeq.get(nTmpLotIndex).getStartRecipe().getDcDefList();
                    int nDCDefLen = CimArrayUtils.getSize(dcDefList);
                    for (int nDCDefNum = 0; nDCDefNum < nDCDefLen; nDCDefNum++){
                        if (ObjectIdentifier.equalsWithValue(strCDItemSeq.get(i).getDataCollectionDefinitionID(),
                                strTmpLotIDSeq.get(nTmpLotIndex).getStartRecipe().getDcDefList().get(nDCDefNum).getDataCollectionDefinitionID())){
                            bDCDefFindFlag = true;
                            break;
                        }
                    }
                    if (!bDCDefFindFlag){
                        Infos.DataCollectionInfo dataCollectionInfo = new Infos.DataCollectionInfo();
                        dcDefList.add(dataCollectionInfo);
                        dataCollectionInfo.setDataCollectionDefinitionID(strCDItemSeq.get(i).getDataCollectionDefinitionID());
                    }
                }
            }
            for (int j = 0; j < lenData; j++){
                String dataValue = strCDItemSeq.get(i).getCollectedDataList().get(j).getDataValue();
                int lenDataVal = CimStringUtils.length(dataValue);
                if (!CimStringUtils.isEmpty(longStringValue)
                        && CimStringUtils.equals(longStringValue, "ON")
                        && CimStringUtils.equals(strCDItemSeq.get(i).getDataType(), BizConstant.SP_DCDEF_VAL_STRING)){
                    Validations.check(lenDataVal > 128, retCodeConfig.getInvalidInputParam());
//                } else if (lenDataVal > 12){
                //MES qiandao-dev integration  start (update integer and flaot length 20 size because the Double  accuracy is round 15 ~ 16 size)
                } else if (lenDataVal > 20){
                    throw new ServiceException(retCodeConfig.getInvalidInputParam());
                }
            }
        }
        //-----------------------
        // Pick up data collection item from strCDItemSeq.
        //-----------------------
        //-----------------------
        // dataCollectionItemName, sitePosition combination should exist in lotID fs PO DC item.
        //-----------------------
        for (int v = 0; v < strTmpLotIDSeq.size(); v++){
            //get Lot Object (aPosPO)
            /*--------------------*/
            /*   Get Lot Object   */
            /*--------------------*/
            CimLot aLot = baseCoreFactory.getBO(CimLot.class, strTmpLotIDSeq.get(v).getLotID());
            Validations.check(aLot == null, new OmCode(retCodeConfig.getNotFoundLot(), ObjectIdentifier.fetchValue(strTmpLotIDSeq.get(v).getLotID())));
            /*--------------------------------*/
            /*   Get Lot's ProcessOperation   */
            /*--------------------------------*/
            CimProcessOperation aPosPO = aLot.getProcessOperation();
            Validations.check(aPosPO == null, new OmCode(retCodeConfig.getNotFoundProcessOperation(), "", ObjectIdentifier.fetchValue(strTmpLotIDSeq.get(v).getLotID())));
            List<Infos.DataCollectionInfo> dcDefList = strTmpLotIDSeq.get(v).getStartRecipe().getDcDefList();
            int lenDCDefSeq = CimArrayUtils.getSize(dcDefList);
            for (int w = 0; w < lenDCDefSeq; w++){
                Infos.DataCollectionInfo dataCollectionInfo = dcDefList.get(w);
                List<ProcessDTO.DataCollectionItemInfo> strDataCollectionItemInfoSeq = aPosPO.getAssignedDataCollectionItemInfo(dataCollectionInfo.getDataCollectionDefinitionID().getValue());
                int lenDCItemInfoSeq = CimArrayUtils.getSize(strDataCollectionItemInfoSeq);
                List<Infos.DataCollectionItemInfo> dcItems = new ArrayList<>();
                dataCollectionInfo.setDcItems(dcItems);
                for (int x = 0; x < lenDCItemInfoSeq; x++){
                    Infos.DataCollectionItemInfo dataCollectionItemInfo = new Infos.DataCollectionItemInfo();
                    dcItems.add(dataCollectionItemInfo);
                    dataCollectionItemInfo.setDataCollectionItemName(strDataCollectionItemInfoSeq.get(x).getDataCollectionItemName());
                    dataCollectionItemInfo.setDataCollectionMode(strDataCollectionItemInfoSeq.get(x).getDataCollectionMode());
                    dataCollectionItemInfo.setDataCollectionUnit(strDataCollectionItemInfoSeq.get(x).getDataCollectionUnit());
                    dataCollectionItemInfo.setDataType(strDataCollectionItemInfoSeq.get(x).getDataType());
                    dataCollectionItemInfo.setItemType(strDataCollectionItemInfoSeq.get(x).getItemType());
                    dataCollectionItemInfo.setMeasurementType(strDataCollectionItemInfoSeq.get(x).getMeasurementType());
                    dataCollectionItemInfo.setWaferID(strDataCollectionItemInfoSeq.get(x).getWaferID());
                    dataCollectionItemInfo.setWaferPosition(strDataCollectionItemInfoSeq.get(x).getWaferPosition());
                    dataCollectionItemInfo.setSitePosition(strDataCollectionItemInfoSeq.get(x).getSitePosition());
                    dataCollectionItemInfo.setHistoryRequiredFlag(strDataCollectionItemInfoSeq.get(x).getHistoryRequiredFlag());
                    dataCollectionItemInfo.setCalculationType(strDataCollectionItemInfoSeq.get(x).getCalculationType());
                    dataCollectionItemInfo.setCalculationExpression(strDataCollectionItemInfoSeq.get(x).getCalculationExpression());
                    dataCollectionItemInfo.setDataValue(strDataCollectionItemInfoSeq.get(x).getDataValue());
                    dataCollectionItemInfo.setTargetValue(strDataCollectionItemInfoSeq.get(x).getTargetValue());
                    dataCollectionItemInfo.setSpecCheckResult(strDataCollectionItemInfoSeq.get(x).getSpecCheckResult());
                    dataCollectionItemInfo.setActionCodes(strDataCollectionItemInfoSeq.get(x).getActionCodes());
                }
            }
        }
        //-----------------------
        // Check if reported data items[PJ site and PJ wafer site] exist in PO's collected data
        //-----------------------
        for (int i = 0; i <lenDCItem; i++){
            if (CimStringUtils.equals(BizConstant.SP_DCDEF_MEAS_PJWAFER, strCDItemSeq.get(i).getMeasurementType())
                    || CimStringUtils.equals(BizConstant.SP_DCDEF_MEAS_PJWAFERSITE, strCDItemSeq.get(i).getMeasurementType())){
                boolean bFoundFlag = false;
                int lenLotIDSeq = CimArrayUtils.getSize(strTmpLotIDSeq);
                for (int v = 0; v < lenLotIDSeq; v++){
                    if (ObjectIdentifier.equalsWithValue(strCDItemSeq.get(i).getLotID(), strTmpLotIDSeq.get(v).getLotID())){
                        List<Infos.DataCollectionInfo> dcDefList = strTmpLotIDSeq.get(v).getStartRecipe().getDcDefList();
                        int lenDCDefSeq = CimArrayUtils.getSize(dcDefList);
                        for (int w = 0; w < lenDCDefSeq; w++){
                            List<Infos.DataCollectionItemInfo> dcItems = dcDefList.get(w).getDcItems();
                            int lenDCItemSeq = CimArrayUtils.getSize(dcItems);
                            for (int x = 0; x < lenDCItemSeq; x++){
                                if (CimStringUtils.equals(strCDItemSeq.get(i).getDataCollectionItemName(), dcItems.get(x).getDataCollectionItemName())
                                        && CimStringUtils.equals(strCDItemSeq.get(i).getSitePosition(), dcItems.get(x).getSitePosition())){
                                    bFoundFlag = true;
                                    break;
                                }
                            }
                            if (bFoundFlag){
                                break;
                            }
                        }
                        if (bFoundFlag){
                            break;
                        }
                    }
                }
                Validations.check(!bFoundFlag, new OmCode(retCodeConfig.getLotInvalidCollectData(),
                        ObjectIdentifier.fetchValue(strCDItemSeq.get(i).getLotID()), strCDItemSeq.get(i).getDataCollectionItemName(), strCDItemSeq.get(i).getMeasurementType()));
            }
        }
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param strObjCommonIn
     * @param strProcessCollectedDataUpdateIn
     * @return com.fa.cim.common.support.RetCode<java.lang.Object>
     * @exception
     * @author Ho
     * @date 2019/8/20 13:55
     */
    public void processCollectedDataUpdate(Infos.ObjCommon strObjCommonIn, Infos.ProcessCollectedDataUpdateIn strProcessCollectedDataUpdateIn){
        log.info("processCollectedDataUpdate");

        List<Infos.DCDef> strDCDef = strProcessCollectedDataUpdateIn.getStrDCDef();
        int strDCDefLen = CimArrayUtils.getSize(strDCDef);

        int nCnt1 = 0;
        int nCnt2 = 0;
        ObjectIdentifier rawDataCollectionDefinitionID=null;

        com.fa.cim.newcore.bo.product.CimLot aLot=baseCoreFactory.getBO(com.fa.cim.newcore.bo.product.CimLot.class,
                strProcessCollectedDataUpdateIn.getLotID());

        Boolean inPostProcessFlagOfLot = aLot.isPostProcessFlagOn();
        if (CimBooleanUtils.isFalse(inPostProcessFlagOfLot)){
            log.info(""+ "inPostProcessFlagOfLot == FALSE");

            List<ProductDTO.HoldRecord> strHoldRecords =  aLot.allHoldRecords();

            int holdLen = CimArrayUtils.getSize(strHoldRecords );
            if (holdLen > 0){
                for (nCnt1 = 0 ; nCnt1 < holdLen; nCnt1++){
                    if (CimStringUtils.equals ((strHoldRecords).get(nCnt1).getReasonCode().getValue(), BizConstant.SP_REASON_LOTLOCK) ){
                        inPostProcessFlagOfLot = true;
                        break;
                    }
                }

            }

            Validations.check(CimBooleanUtils.isFalse(inPostProcessFlagOfLot),new OmCode(retCodeConfigEx.getLotNotInpostprocess(),aLot.getIdentifier()));

        }else{
            log.info(""+ "inPostProcessFlagOfLot == TRUE");
        }

        Infos.PostProcessTargetObject strPostProcessTargetObject=new Infos.PostProcessTargetObject();
        strPostProcessTargetObject.setLotID ( strProcessCollectedDataUpdateIn.getLotID());

        ObjectIdentifier dummyID=new ObjectIdentifier();
        dummyID.setValue ( "");

        Outputs.ObjPostProcessQueListDROut strPostProcessQueueListDROut;

        Inputs.PostProcessQueueListDRIn postProcessQueueListDRIn=new Inputs.PostProcessQueueListDRIn();
        postProcessQueueListDRIn.setKey("");
        postProcessQueueListDRIn.setSeqNo(-1L);
        postProcessQueueListDRIn.setWatchDogName("");
        postProcessQueueListDRIn.setPostProcId("");
        postProcessQueueListDRIn.setSyncFlag(-1L);
        postProcessQueueListDRIn.setTxId("");
        postProcessQueueListDRIn.setTargetType("");
        postProcessQueueListDRIn.setStrPostProcessTargetObject(strPostProcessTargetObject);
        postProcessQueueListDRIn.setStatus("");
        postProcessQueueListDRIn.setPassedTime(-1L);
        postProcessQueueListDRIn.setClaimUserId(dummyID.getValue());
        postProcessQueueListDRIn.setStartCreateTimeStamp("");
        postProcessQueueListDRIn.setEndCreateTieStamp("");
        postProcessQueueListDRIn.setStartUpdateTimeStamp("");
        postProcessQueueListDRIn.setEndUpdateTimeStamp("");
        postProcessQueueListDRIn.setMaxCount(1L);
        postProcessQueueListDRIn.setCommittedReadFlag        (true);

        // step1 - postProcessQueue_ListDR__130
        strPostProcessQueueListDROut = postProcessMethod.postProcessQueueListDR(
                strObjCommonIn,postProcessQueueListDRIn);


        Validations.check(CimArrayUtils.getSize(strPostProcessQueueListDROut.getStrActionInfoSeq()) <= 0 ||
                !CimStringUtils.equals(strPostProcessQueueListDROut.getStrActionInfoSeq().get(0).getPostProcessID(),
                        BizConstant.SP_POSTPROCESS_ACTIONID_COLLECTEDDATAACTION),new OmCode(retCodeConfigEx.getLotNotInDataCollectionAction(),aLot.getIdentifier()));


        com.fa.cim.newcore.bo.pd.CimProcessOperation aPreviousOperation;
        com.fa.cim.newcore.bo.pd.CimProcessOperation aTmpPreviousPO;
        aTmpPreviousPO = aLot.getPreviousProcessOperation();
        aPreviousOperation = aTmpPreviousPO;
        Validations.check(null == aPreviousOperation,retCodeConfig.getNotFoundOperation());


        ObjectIdentifier controlJobID=new ObjectIdentifier();
        controlJobID.setValue(aPreviousOperation.getAssignedControlJobID());
        Validations.check(!CimStringUtils.equals (controlJobID.getValue(), strProcessCollectedDataUpdateIn.getControlJobID().getValue()),retCodeConfig.getInvalidParameterWithMsg());


        ProcessDTO.ActualStartInformationForPO actualStartInfo = aPreviousOperation.getActualStartInfo(true);

        Validations.check(!CimBooleanUtils.isTrue(actualStartInfo.getAssignedDataCollectionFlag()),retCodeConfig.getNoNeedToDataCollect());


        int strPreviousDCDefLen = CimArrayUtils.getSize(actualStartInfo.getAssignedDataCollections());
        log.info(""+ "strPreviousDCDefLen = "+ strPreviousDCDefLen);
        Validations.check(strDCDefLen != strPreviousDCDefLen,retCodeConfig.getInvalidParameterWithMsg());


        List<ProcessDTO.DataCollectionInfo> strDataCollectionInfo ;
        strDataCollectionInfo=new ArrayList<>();

        List<Infos.DCItem> strPreviousRawDCItem;
        List<Infos.DCItem> strCurrentRawDCItem;
        int changedRawDCItemLen = 0;
        strPreviousRawDCItem=new ArrayList<>();
        strCurrentRawDCItem=new ArrayList<>();

        for (nCnt1 = 0 ; nCnt1 < strDCDefLen; nCnt1++){
            log.info(""+ "# nCnt1+ dataCollectionDefinitionID"+ nCnt1+ strDCDef.get(nCnt1).getDataCollectionDefinitionID().getValue());

            Validations.check(!CimStringUtils.equals ((actualStartInfo.getAssignedDataCollections()).get(nCnt1).getDataCollectionDefinitionID().getValue(),
                    strDCDef.get(nCnt1).getDataCollectionDefinitionID().getValue()),retCodeConfig.getInvalidParameterWithMsg());


            int strPreviousDCItemLen = CimArrayUtils.getSize((actualStartInfo.getAssignedDataCollections()).get(nCnt1).getDcItems());
            int strDCItemLen = CimArrayUtils.getSize(strDCDef.get(nCnt1).getDcItemList());
            Validations.check(strDCItemLen != strPreviousDCItemLen,retCodeConfig.getInvalidParameterWithMsg());

            strDataCollectionInfo.add(new ProcessDTO.DataCollectionInfo());
            strDataCollectionInfo.get(nCnt1).setDataCollectionDefinitionID         ( strDCDef.get(nCnt1).getDataCollectionDefinitionID());
            strDataCollectionInfo.get(nCnt1).setDescription                        ( strDCDef.get(nCnt1).getDescription());
            strDataCollectionInfo.get(nCnt1).setDataCollectionType                 ( strDCDef.get(nCnt1).getDataCollectionType());
            strDataCollectionInfo.get(nCnt1).setCalculationRequiredFlag            ( strDCDef.get(nCnt1).getCalculationRequiredFlag());
            strDataCollectionInfo.get(nCnt1).setSpecCheckRequiredFlag              ( strDCDef.get(nCnt1).getSpecCheckRequiredFlag());
            strDataCollectionInfo.get(nCnt1).setDataCollectionSpecificationID      ( strDCDef.get(nCnt1).getDataCollectionSpecificationID());
            strDataCollectionInfo.get(nCnt1).setPreviousDataCollectionDefinitionID ( strDCDef.get(nCnt1).getPreviousDataCollectionDefinitionID());
            strDataCollectionInfo.get(nCnt1).setPreviousOperationID                ( strDCDef.get(nCnt1).getPreviousOperationID());
            strDataCollectionInfo.get(nCnt1).setPreviousOperationNumber            ( strDCDef.get(nCnt1).getPreviousOperationNumber());
            strDataCollectionInfo.get(nCnt1).setDcItems(new ArrayList<>());

            List<EDCDTO.DCItemData> strDCItemData=new ArrayList<>();

            if (nCnt1 == 0){
                log.info(""+ "# nCnt1 == 0");
                rawDataCollectionDefinitionID = strDCDef.get(nCnt1).getDataCollectionDefinitionID();
                strPreviousRawDCItem=new ArrayList<>();
                strCurrentRawDCItem=new ArrayList<>();
                strDCItemData=new ArrayList<>();

            }

            for (nCnt2 = 0 ; nCnt2 < strDCItemLen; nCnt2++){
                Validations.check(!CimStringUtils.equals ((actualStartInfo.getAssignedDataCollections()).get(nCnt1).getDcItems().get(nCnt2).getDataCollectionItemName(),
                        strDCDef.get(nCnt1).getDcItemList().get(nCnt2).getDataCollectionItemName()),retCodeConfig.getInvalidParameterWithMsg());


                if (nCnt1 == 0){
                    log.info(""+ "## nCnt1 == 0+ Check Raw Data");

                    if (CimStringUtils.equals((actualStartInfo.getAssignedDataCollections()).get(nCnt1).getDcItems().get(nCnt2).getItemType()
                            , BizConstant.SP_DCDEF_ITEM_RAW)){
                        log.info(""+ "## itemType = SP_DCDEF_ITEM_RAW");
                        Validations.check(0 == CimStringUtils.length (strDCDef.get(nCnt1).getDcItemList().get(nCnt2).getDataValue()),retCodeConfig.getSomeDataValueBlank());


                        if (!CimStringUtils.equals (( actualStartInfo.getAssignedDataCollections()).get(nCnt1).getDcItems().get(nCnt2).getDataValue(),
                                strDCDef.get(nCnt1).getDcItemList().get(nCnt2).getDataValue())){
                            log.info(""+ "## Value is Changed" + (actualStartInfo.getAssignedDataCollections()).get(nCnt1).getDcItems().get(nCnt2).getDataValue() + strDCDef.get(nCnt1).getDcItemList().get(nCnt2).getDataValue());
                            strPreviousRawDCItem.add(new Infos.DCItem());
                            strPreviousRawDCItem.get(changedRawDCItemLen).setDataCollectionItemName ( (actualStartInfo.getAssignedDataCollections()).get(nCnt1).getDcItems().get(nCnt2).getDataCollectionItemName());
                            strCurrentRawDCItem.get(changedRawDCItemLen).setDataCollectionItemName ( strDCDef.get(nCnt1).getDcItemList().get(nCnt2).getDataCollectionItemName());
                            strPreviousRawDCItem.get(changedRawDCItemLen).setDataValue ( (actualStartInfo.getAssignedDataCollections()).get(nCnt1).getDcItems().get(nCnt2).getDataValue());
                            strCurrentRawDCItem.get(changedRawDCItemLen).setDataValue ( strDCDef.get(nCnt1).getDcItemList().get(nCnt2).getDataValue());
                            changedRawDCItemLen++;
                        }
                        log.info(""+ "## changedRawDCItemLen"+ changedRawDCItemLen);

                    }

                    strDCItemData.add(new EDCDTO.DCItemData());
                    strDCItemData.get(nCnt2).setDataItemName           ( strDCDef.get(nCnt1).getDcItemList().get(nCnt2).getDataCollectionItemName());
                    strDCItemData.get(nCnt2).setWaferPosition          ( strDCDef.get(nCnt1).getDcItemList().get(nCnt2).getWaferPosition());
                    strDCItemData.get(nCnt2).setSitePosition           ( strDCDef.get(nCnt1).getDcItemList().get(nCnt2).getSitePosition());
                    strDCItemData.get(nCnt2).setValType                ( strDCDef.get(nCnt1).getDcItemList().get(nCnt2).getDataType());
                    strDCItemData.get(nCnt2).setItemType               ( strDCDef.get(nCnt1).getDcItemList().get(nCnt2).getItemType());
                    strDCItemData.get(nCnt2).setCalculationType        ( strDCDef.get(nCnt1).getDcItemList().get(nCnt2).getCalculationType());
                    strDCItemData.get(nCnt2).setCalculationExpression  ( strDCDef.get(nCnt1).getDcItemList().get(nCnt2).getCalculationExpression());
                    strDCItemData.get(nCnt2).setInputValue             ( strDCDef.get(nCnt1).getDcItemList().get(nCnt2).getDataValue());

                }

                strDataCollectionInfo.get(nCnt1).getDcItems().get(nCnt2).setDataCollectionItemName ( strDCDef.get(nCnt1).getDcItemList().get(nCnt2).getDataCollectionItemName());
                strDataCollectionInfo.get(nCnt1).getDcItems().get(nCnt2).setDataCollectionMode     ( strDCDef.get(nCnt1).getDcItemList().get(nCnt2).getDataCollectionMode());
                strDataCollectionInfo.get(nCnt1).getDcItems().get(nCnt2).setDataCollectionUnit     ( strDCDef.get(nCnt1).getDcItemList().get(nCnt2).getDataCollectionUnit());
                strDataCollectionInfo.get(nCnt1).getDcItems().get(nCnt2).setDataType               ( strDCDef.get(nCnt1).getDcItemList().get(nCnt2).getDataType());
                strDataCollectionInfo.get(nCnt1).getDcItems().get(nCnt2).setItemType               ( strDCDef.get(nCnt1).getDcItemList().get(nCnt2).getItemType());
                strDataCollectionInfo.get(nCnt1).getDcItems().get(nCnt2).setMeasurementType        ( strDCDef.get(nCnt1).getDcItemList().get(nCnt2).getMeasurementType());
                strDataCollectionInfo.get(nCnt1).getDcItems().get(nCnt2).setWaferID                ( strDCDef.get(nCnt1).getDcItemList().get(nCnt2).getWaferID());
                strDataCollectionInfo.get(nCnt1).getDcItems().get(nCnt2).setWaferPosition          ( strDCDef.get(nCnt1).getDcItemList().get(nCnt2).getWaferPosition());
                strDataCollectionInfo.get(nCnt1).getDcItems().get(nCnt2).setSitePosition           ( strDCDef.get(nCnt1).getDcItemList().get(nCnt2).getSitePosition());
                strDataCollectionInfo.get(nCnt1).getDcItems().get(nCnt2).setHistoryRequiredFlag    ( strDCDef.get(nCnt1).getDcItemList().get(nCnt2).getHistoryRequiredFlag());
                strDataCollectionInfo.get(nCnt1).getDcItems().get(nCnt2).setCalculationType        ( strDCDef.get(nCnt1).getDcItemList().get(nCnt2).getCalculationType());
                strDataCollectionInfo.get(nCnt1).getDcItems().get(nCnt2).setCalculationExpression  ( strDCDef.get(nCnt1).getDcItemList().get(nCnt2).getCalculationExpression());
                strDataCollectionInfo.get(nCnt1).getDcItems().get(nCnt2).setDataValue              ( strDCDef.get(nCnt1).getDcItemList().get(nCnt2).getDataValue());
                strDataCollectionInfo.get(nCnt1).getDcItems().get(nCnt2).setTargetValue            ( strDCDef.get(nCnt1).getDcItemList().get(nCnt2).getTargetValue());
                strDataCollectionInfo.get(nCnt1).getDcItems().get(nCnt2).setSpecCheckResult        ( strDCDef.get(nCnt1).getDcItemList().get(nCnt2).getSpecCheckResult());
            }

            if (nCnt1 == 0){
                log.info(""+ "# nCnt1 == 0+ doValidCheck");

                CimDataCollectionDefinition aDataCollectionDefinition = baseCoreFactory.getBO(CimDataCollectionDefinition.class, strDCDef.get(nCnt1).getDataCollectionDefinitionID());
                try {
                    aDataCollectionDefinition.doValidCheck( strDCItemData );
                }catch (IllegalParameterException e) {
                    Validations.check(true,retCodeConfig.getDatavalCannotConvToInt());
                }

            }

            strDataCollectionInfo.get(nCnt1).setDcSpecs ( (actualStartInfo.getAssignedDataCollections()).get(nCnt1).getDcSpecs());

        }

        aPreviousOperation.setDataCollectionInfo(strDataCollectionInfo );

        if (changedRawDCItemLen > 0){
            log.info(""+ "changedRawDCItemLen"+ changedRawDCItemLen);

            Infos.CollectedDataChangeEventMakeIn strCollectedDataChangeEventMakeIn=new Infos.CollectedDataChangeEventMakeIn();
            strCollectedDataChangeEventMakeIn.setTransactionID ( strObjCommonIn.getTransactionID());
            strCollectedDataChangeEventMakeIn.setLotID ( strProcessCollectedDataUpdateIn.getLotID());
            strCollectedDataChangeEventMakeIn.setControlJobID ( strProcessCollectedDataUpdateIn.getControlJobID());
            strCollectedDataChangeEventMakeIn.setDataCollectionDefinitionID ( rawDataCollectionDefinitionID);
            strCollectedDataChangeEventMakeIn.setStrPreviousRawDCItem ( strPreviousRawDCItem);
            strCollectedDataChangeEventMakeIn.setStrCurrentRawDCItem ( strCurrentRawDCItem);
            strCollectedDataChangeEventMakeIn.setClaimMemo ( strProcessCollectedDataUpdateIn.getClaimMemo());

            // step2 - collectedDataChangeEvent_Make
            eventMethod.collectedDataChangeEventMake(
                    strObjCommonIn,
                    strCollectedDataChangeEventMakeIn );

        }

        log.info("processCollectedDataUpdate");

    }

}
