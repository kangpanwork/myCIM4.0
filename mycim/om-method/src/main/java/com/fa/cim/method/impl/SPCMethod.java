package com.fa.cim.method.impl;

import com.alibaba.fastjson.JSONObject;
import com.fa.cim.annotaion.OmMethod;
import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.OmCode;
import com.fa.cim.common.utils.*;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Inputs;
import com.fa.cim.dto.Outputs;
import com.fa.cim.dto.Results;
import com.fa.cim.jpa.CimJpaRepository;
import com.fa.cim.method.*;
import com.fa.cim.middleware.standard.core.exception.base.CimIntegrationException;
import com.fa.cim.newcore.bo.machine.CimMachine;
import com.fa.cim.newcore.bo.pd.CimProcessDefinition;
import com.fa.cim.newcore.bo.pd.CimProcessFlowContext;
import com.fa.cim.newcore.bo.pd.CimProcessOperation;
import com.fa.cim.newcore.bo.person.CimPerson;
import com.fa.cim.newcore.bo.prodspec.CimProductGroup;
import com.fa.cim.newcore.bo.prodspec.CimProductSpecification;
import com.fa.cim.newcore.bo.prodspec.CimTechnology;
import com.fa.cim.newcore.bo.product.CimLot;
import com.fa.cim.newcore.bo.product.CimMonitorGroup;
import com.fa.cim.newcore.bo.recipe.CimMachineRecipe;
import com.fa.cim.newcore.dto.pd.ProcessDTO;
import com.fa.cim.newcore.dto.product.ProductDTO;
import com.fa.cim.newcore.factory.BaseCoreFactory;
import com.fa.cim.newcore.standard.prcssdfn.ProcessOperation;
import com.fa.cim.remote.ISPCRemoteManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * description:
 * <p>SPCMethod .<br/></p>
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2018/11/6        ********             PlayBoy               create file
 *
 * @author: PlayBoy
 * @date: 2018/11/6 18:17
 * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@OmMethod
@Slf4j
public class SPCMethod implements ISPCMethod {

    @Autowired
    private IBondingGroupMethod bondingGroupMethod;

    @Autowired
    private BaseCoreFactory baseCoreFactory;

    @Autowired
    private RetCodeConfig retCodeConfig;

    @Autowired
    private ILotMethod lotMethod;

    @Autowired
    private IFPCMethod fpcMethod;

    @Autowired
    private IFabMethod fabMethod;

    @Autowired
    private IProcessMethod processMethod;
    
    @Autowired
    private CimJpaRepository cimJpaRepository;

    @Autowired
    private ILogicalRecipeMethod logicalRecipeMethod;

    @Autowired
    private ISPCRemoteManager spcRemoteManager;

    @Autowired
    private IOcapMethod ocapMethod;

    @Override
    public Outputs.ObjSPCMgrSendSPCCheckReqOut spcMgrSendSPCCheckReq(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, ObjectIdentifier controlJobID, List<Infos.StartCassette> strStartCassetteList) {
//        old version
//        Outputs.ObjSPCMgrSendSPCCheckReqOut out = new Outputs.ObjSPCMgrSendSPCCheckReqOut();
//
//        /*------------------------------------------------------------------------*/
//        /*                                                                        */
//        /*   Set Output Values from Input Parameters without any changes          */
//        /*                                                                        */
//        /*------------------------------------------------------------------------*/
//        log.info("in param equipmentID = {}", ObjectIdentifier.fetchValue(equipmentID));
//        log.info("in param controlJobID = {}", ObjectIdentifier.fetchValue(controlJobID));
//        int totalCount = 0;
//        Results.SPCCheckReqResult spcCheckReqResult = new Results.SPCCheckReqResult();
//        List<Infos.SpcIFParm> spcIFParmList = new ArrayList<>();
//        out.setSpcCheckReqResult(spcCheckReqResult);
//        out.setSpcIFParmList(spcIFParmList);
//        spcCheckReqResult.setControlJobID(controlJobID);
//        spcCheckReqResult.setEquipmentID(equipmentID);
//        List<Infos.SpcCheckLot> spcCheckLots = new ArrayList<>();
//        spcCheckReqResult.setSpcCheckLot(spcCheckLots);
//
//        /*--------------------------------------------------*/
//        /*                                                  */
//        /*  Bonding Map Condition Check for Wafer Stacking  */
//        /*                                                  */
//        /*--------------------------------------------------*/
//        // 【step-1】 - bondingGroup_infoByEqp_GetDR
//        log.debug("calling bondingGroupInfoByEqpGetDR()");
//        Outputs.ObjBondingGroupInfoByEqpGetDROut objBondingGroupInfoByEqpGetDROut = bondingGroupMethod.bondingGroupInfoByEqpGetDR(objCommon, equipmentID,
//                controlJobID, true);
//        int topLotLen = ArrayUtils.getSize(objBondingGroupInfoByEqpGetDROut.getTopLotIDSeq());
//        log.info("topLotLen:{}", topLotLen);
//        int cassetteLen = ArrayUtils.getSize(strStartCassetteList);
//        for (int cCnt = 0; cCnt < cassetteLen; cCnt++) {
//            int lotLen = ArrayUtils.getSize(strStartCassetteList.get(cCnt).getLotInCassetteList());
//            for (int lCnt = 0; lCnt < lotLen; lCnt++) {
//                /*---------------------------------*/
//                /*   Omit Not OperationStart Lot   */
//                /*---------------------------------*/
//                Boolean operationStartFlag = strStartCassetteList.get(cCnt).getLotInCassetteList().get(lCnt).getMoveInFlag();
//                if (BooleanUtils.isFalse(operationStartFlag)) {
//                    log.info("operationStartFlag  == FALSE");
//                    continue;
//                }
//                boolean isTopLot = false;
//                for (int lotCnt = 0; lotCnt < topLotLen; lotCnt++) {
//                    if (ObjectUtils.equalsWithValue(objBondingGroupInfoByEqpGetDROut.getTopLotIDSeq().get(lotCnt),
//                            strStartCassetteList.get(cCnt).getLotInCassetteList().get(lCnt).getLotID())) {
//                        log.info("isTopLot:{}", lotCnt);
//                        isTopLot = true;
//                        break;
//                    }
//                }
//                if (BooleanUtils.isTrue(isTopLot)) {
//                    log.info("Lot is Top Lot:{}", ObjectIdentifier.fetchValue(strStartCassetteList.get(cCnt).getLotInCassetteList().get(lCnt).getLotID()));
//                    continue;
//                }
//                /*------------------------------------------------------------------------*/
//                /*                                                                        */
//                /*   Set Input Values for doSpcCheck() - 1                                */
//                /*                                                                        */
//                /*------------------------------------------------------------------------*/
//                Inputs.SpcInput spcInput = new Inputs.SpcInput();
//                Inputs.SpcInput tmpSpcInput = new Inputs.SpcInput();
//                Outputs.SpcOutput spcOutput = null;
//                List<Infos.DataCollectionInfo> strDCDef = strStartCassetteList.get(cCnt).getLotInCassetteList().get(lCnt).getStartRecipe().getDcDefList();
//                //-----------------------
//                //   set requestUserID
//                //-----------------------
//                spcInput.setRequestUserID(objCommon.getUser().getUserID());
//
//                //---------------
//                //   set lotID
//                //---------------
//                spcInput.setLotID(strStartCassetteList.get(cCnt).getLotInCassetteList().get(lCnt).getLotID());
//
//                //---------------------------------------------
//                //   set mainProcessDefinitionID & productID
//                //---------------------------------------------
//                CimLot aLot = baseCoreFactory.getBO(CimLot.class, spcInput.getLotID());
//                Validations.check(null == aLot, retCodeConfig.getNotFoundLot());
//
//                //【step2】lot_CheckConditionForPO
//                ProcessOperation aTmpProcessOperation = null;
//                CimProcessOperation aPO = null;
//                CimProcessOperation aCheckPO = null;
//                Boolean lotCheckConditionForPOOut = lotMethod.lotCheckConditionForPO(objCommon, spcInput.getLotID());
//
//                if (BooleanUtils.isTrue(lotCheckConditionForPOOut)) {
//                    //--------------------------------------------------------------------------
//                    // Get PO from Current Operation.
//                    //--------------------------------------------------------------------------
//                    log.info("Get PO from the current Operation.");
//                    aTmpProcessOperation = aLot.getProcessOperation();
//                } else {
//                    //--------------------------------------------------------------------------
//                    // Get PO from Previous Operation.
//                    //--------------------------------------------------------------------------
//                    log.info("Get PO from the previous Operation.");
//                    aTmpProcessOperation = aLot.getPreviousProcessOperation();
//                }
//                aPO = (CimProcessOperation) aTmpProcessOperation;
//                if (null == aPO) {
//                    continue;
//                }
//                aCheckPO = aPO;
//                CimProcessDefinition aMainPD = aPO.getMainProcessDefinition();
//                Validations.check(null == aMainPD, retCodeConfig.getNotFoundMainRoute());
//                spcInput.setMainProcessDefinitionID(ObjectIdentifier.build(aMainPD.getIdentifier(), aMainPD.getPrimaryKey()));
//                CimProductSpecification aProdSpec = aLot.getProductSpecification();
//                if (null == aProdSpec) {
//                    continue;
//                }
//                spcInput.setProductID(ObjectIdentifier.buildWithValue(aProdSpec.getIdentifier()));
//                int dcDefLen = ArrayUtils.getSize(strDCDef);
//                log.info("strDCDef size :{}", dcDefLen);
//                if (dcDefLen < 1) {
//                    log.info("Not need SPCCheck.");
//                    continue;
//                }
//                //----------------------------------------------------------------------------------------
//                //   set Corresponding Operation Number
//                //----------------------------------------------------------------------------------------
//                CimProcessFlowContext aPFX = null;
//                int fpcListNum = 0;
//                String singleCorrOpeFpc = null;
//                int coFPCLen = 0;
//                int foundFPCCnt = 0;
//                int waferCntFromStartCassette = 0;
//                int poLen = 0;
//                List<CimProcessOperation> aCorrespondingPOs = new ArrayList<>();
//                CimProcessOperation aCorrespondingPO = null;
//                Boolean correspondingPOFlag = false;
//                List<Infos.FPCInfo> fpcInfoGetDROut = null;
//
//                if (StringUtils.equals(BizConstant.SP_DCDEF_COLLECTION_MEASUREMENT, strDCDef.get(0).getDataCollectionType())) {
//                    log.info("dataCollectionType == SP_DCDEF_COLLECTION_MEASUREMENT");
//                    aPFX = aLot.getProcessFlowContext();
//                    Validations.check(null == aPFX, new OmCode(retCodeConfig.getNotFoundPfx(), ObjectIdentifier.fetchValue(spcInput.getLotID())));
//                    List<String> dummyFPCIDs = new ArrayList<>();
//                    ObjectIdentifier dummyID = new ObjectIdentifier();
//                    String dummy = null;
//                    String opeNum = aPO.getOperationNumber();
//
//                    //【step-3】FPC_info_GetDR__101
//                    Inputs.ObjFPCInfoGetDRIn objFPCInfoGetDRIn = new Inputs.ObjFPCInfoGetDRIn();
//                    objFPCInfoGetDRIn.setLotID(spcInput.getLotID());
//                    objFPCInfoGetDRIn.setLotFamilyID(dummyID);
//                    objFPCInfoGetDRIn.setMainPDID(spcInput.getMainProcessDefinitionID());
//                    objFPCInfoGetDRIn.setMainOperNo(opeNum);
//                    objFPCInfoGetDRIn.setOrgMainPDID(dummyID);
//                    objFPCInfoGetDRIn.setOrgOperNo(dummy);
//                    objFPCInfoGetDRIn.setSubMainPDID(dummyID);
//                    objFPCInfoGetDRIn.setSubOperNo(dummy);
//                    objFPCInfoGetDRIn.setFPCIDs(dummyFPCIDs);
//                    objFPCInfoGetDRIn.setEquipmentID(equipmentID);
//                    objFPCInfoGetDRIn.setWaferIDInfoGetFlag(true);
//                    objFPCInfoGetDRIn.setRecipeParmInfoGetFlag(false);
//                    objFPCInfoGetDRIn.setReticleInfoGetFlag(false);
//                    objFPCInfoGetDRIn.setDcSpecItemInfoGetFlag(true);
//                    //【step-4】 - FPC_info_GetDR__101
//                    fpcInfoGetDROut = fpcMethod.fpcInfoGetDR(objCommon, objFPCInfoGetDRIn);
//                    fpcListNum = ArrayUtils.getSize(fpcInfoGetDROut);
//                    if (0 != fpcListNum) {
//                        waferCntFromStartCassette = ArrayUtils.getSize(strStartCassetteList.get(cCnt).getLotInCassetteList().get(lCnt).getLotWaferList());
//                        for (int numFPC = 0; numFPC < fpcListNum; numFPC++) {
//                            log.info("wafer key for FPC :{}", ObjectIdentifier.fetchValue(fpcInfoGetDROut.get(numFPC).getLotWaferInfoList().get(0).getWaferID()));
//                            for (int inParaWCnt = 0; inParaWCnt < waferCntFromStartCassette; inParaWCnt++) {
//                                if (ObjectUtils.equalsWithValue(fpcInfoGetDROut.get(numFPC).getLotWaferInfoList().get(0).getWaferID(),
//                                        strStartCassetteList.get(cCnt).getLotInCassetteList().get(lCnt).getLotWaferList().get(inParaWCnt).getWaferID())) {
//                                    coFPCLen = ArrayUtils.getSize(fpcInfoGetDROut.get(numFPC).getCorrespondingOperationInfoList());
//                                    singleCorrOpeFpc = fpcInfoGetDROut.get(numFPC).getCorrespondOperationNumber();
//                                    log.info("correspondingOperationInfo size : {} from FPC", coFPCLen);
//                                    foundFPCCnt = numFPC;
//                                    break;
//                                }
//                            }
//                        }
//                        log.info("singleCorrOpeFPC : {}", singleCorrOpeFpc);
//                        if (coFPCLen == 0 && StringUtils.isEmpty(singleCorrOpeFpc)) {
//                            aCorrespondingPOs = aPFX.getCorrespondingProcessOperationsFor(aPO);
//                            poLen = ArrayUtils.getSize(aCorrespondingPOs);
//                        } else if (0 < coFPCLen) {
//                            poLen = coFPCLen;
//                        } else {
//                            poLen = 1;
//                        }
//                    } else {
//                        aCorrespondingPOs = aPFX.getCorrespondingProcessOperationsFor(aPO);
//                        poLen = ArrayUtils.getSize(aCorrespondingPOs);
//                    }
//                    log.info("aCorrespondingPOs size = {}", poLen);
//                    if (poLen > 0) {
//                        log.info("aCorrespondingPOs size > 0 ...");
//                        correspondingPOFlag = true;
//                    } else {
//                        log.info("aCorrespondingPOs size = 0 ...");
//                        CimMonitorGroup monGrp = aLot.getControlMonitorGroup();
//                        if (null != monGrp) {
//                            List<Infos.MonitoredLot> monitoredLots = monGrp.allLots();
//                            if (ArrayUtils.getSize(monitoredLots) > 0) {
//                                log.info("monitoredLots size > 0 ...");
//                                if (StringUtils.isNotEmpty(monitoredLots.get(0).getProcessOperation())) {
//                                    log.info("monitoredLots[0]'s PO is {} ...",monitoredLots.get(0).getProcessOperation());
//                                    poLen = 1;
//                                    aCorrespondingPO = baseCoreFactory.getBO(CimProcessOperation.class,monitoredLots.get(0).getProcessOperation());
//                                }else {
//                                    log.info("monitoredLots[0]'s PO is null...");
//                                    continue;
//                                }
//                            }else {
//                                log.info("monitoredLots size is 0...");
//                                continue;
//                            }
//                        }else {
//                            log.info("monGrp is null...");
//                            continue;
//                        }
//                    }
//                }else {
//                    log.info("dataCollectionType != SP_DCDEF_COLLECTION_MEASUREMENT");
//                    poLen = 1;
//                }
//                //------------------------
//                //   set collectionType
//                //------------------------
//                spcInput.setCollectionType(strDCDef.get(0).getDataCollectionType());
//                int dcLen = ArrayUtils.getSize(strDCDef);
//                int actionCnt = 0;
//                int k = 0;
//                log.info("strDCDef size = {}",dcLen);
//                for (int m = 0; m < dcLen; m++) {
//                    //-----------------
//                    //   set dcDefID
//                    //-----------------
//                    spcInput.setDcDefID(strDCDef.get(m).getDataCollectionDefinitionID());
//                    //----------------------------------------------------------------------------------------
//                    //   Store the base spcInput
//                    //----------------------------------------------------------------------------------------
//                    tmpSpcInput = spcInput;
//                    //----------------------------------------------------------------------------------------
//                    //   Get wafer list of the target lot
//                    //----------------------------------------------------------------------------------------
//                    List<ObjectIdentifier> pWaferIDs = new ArrayList<>();
//                    List<Infos.LotWaferAttributes> lotMaterialsGetWafersOut = null;
//                    List<ObjectIdentifier> waferIDList = null;
//                    if (StringUtils.equals(TransactionIDEnum.PARTIAL_OPERATION_COMP_WITH_DATA_REQ.getValue(),objCommon.getTransactionID())
//                            || StringUtils.equals(TransactionIDEnum.PARTIAL_OPERATION_COMP_FOR_INTERNAL_BUFFER_REQ.getValue(),objCommon.getTransactionID())){
//                        //Call METHOD lot_materials_GetWafers
//                        log.info("call lot_materials_GetWafers()");
//                        //【step-5】 lot_materials_GetWafers
//                        lotMaterialsGetWafersOut = lotMethod.lotMaterialsGetWafers(objCommon, spcInput.getLotID());
//                    }else {
//                        //【step-6】lot_waferIDList_GetDR
//                        Inputs.ObjLotWaferIDListGetDRIn objLotWaferIDListGetDRIn = new Inputs.ObjLotWaferIDListGetDRIn();
//                        objLotWaferIDListGetDRIn.setLotID(spcInput.getLotID());
//                        objLotWaferIDListGetDRIn.setScrapCheckFlag(true);
//                        waferIDList = lotMethod.lotWaferIDListGetDR(objCommon, objLotWaferIDListGetDRIn);
//                        pWaferIDs = waferIDList;
//                    }
//                    List<ObjectIdentifier> curLotWaferIDs = pWaferIDs;
//                    int nCurPOWfrLen = ArrayUtils.getSize(curLotWaferIDs);
//                    log.info("nCurPOWfrLen : {}",nCurPOWfrLen);
//                    //----------------------------------------------------------------------------------------
//                    //   set processEquipmentID & processRecipeID & processRouteID & processOperaitonNumber
//                    //----------------------------------------------------------------------------------------
//                    for (int pCnt = 0; pCnt < poLen; pCnt++) {
//                        // Flag for where processed wafer information is stored in PO or not
//                        Boolean bCorPOWaferInfoExist = false;
//                        List<String> strPOObjs = new ArrayList<>();
//                        List<Integer> waferCnt = new ArrayList<>();
//                        List<Infos.WaferIDByChamber> strWaferIDSeq = new ArrayList<>();
//                        strWaferIDSeq.add(new Infos.WaferIDByChamber());
//                        strWaferIDSeq.get(0).setWaferIDs(new ArrayList<>(nCurPOWfrLen));
//                        waferCnt.add(0);
//                        String corrOpeNum = null;
//                        if (StringUtils.equals(BizConstant.SP_DCDEF_COLLECTION_MEASUREMENT,strDCDef.get(0).getDataCollectionType())){
//                            log.info("strDCDef[0].dataCollectionType==SP_DCDEF_COLLECTION_MEASUREMENT");
//                            if (BooleanUtils.isTrue(correspondingPOFlag) && (coFPCLen != 0 || StringUtils.isNotEmpty(singleCorrOpeFpc))){
//                                log.info("correspondingPOFlag==TRUE and FPC has updated corresponding Operation.");
//                                if (coFPCLen > 0){
//                                    corrOpeNum = fpcInfoGetDROut.get(foundFPCCnt).getCorrespondingOperationInfoList().get(pCnt).getCorrespondingOperationNumber();
//                                    aCorrespondingPO = aPFX.findProcessOperationForOperationNumberBefore(corrOpeNum);
//                                }else {
//                                    aCorrespondingPO = aPFX.findProcessOperationForOperationNumberBefore(singleCorrOpeFpc);
//                                }
//                                Validations.check(null == aCorrespondingPO, retCodeConfig.getNotFoundProcessOperation(),ObjectIdentifier.fetchValue(spcInput.getLotID()));
//                            }else {
//                                //--------------------------------------------------------------------------
//                                // Get Corresponding PO list from process wafer
//                                //--------------------------------------------------------------------------
//                                //--------------------------------------------------------------------------
//                                // Get process wafers of corresponding PO
//                                //--------------------------------------------------------------------------
//                                if (BooleanUtils.isTrue(correspondingPOFlag)){
//                                    log.info("correspondingPOFlag==TRUE");
//                                    aCorrespondingPO = aCorrespondingPOs.get(pCnt);
//                                }
//                                if (null == aCorrespondingPO){
//                                    log.info("aCorrespondingPO is null");
//                                    continue;
//                                }
//                            }
//                            // Set default PO obj to strPOObjs[0]
//                            strPOObjs.add(aCorrespondingPO.getPrimaryKey());//po obj ? id?
//                            int nPoCnt = 1 ;
//                            log.info("strPOObjs[0] = {}",strPOObjs.get(0));
//                            List<Infos.PosProcessWafer> strCorrespondingProcessWaferSeq = aCorrespondingPO.getProcessWafers();
//                            int nCorPOWaferLen = ArrayUtils.getSize(strCorrespondingProcessWaferSeq);
//                            if (nCorPOWaferLen > 0){
//                                log.info("nCorPOWaferLen > 0");
//                                //Processed wafer information is stored in FRPO_SMPL
//                                bCorPOWaferInfoExist = true;
//                                CimProcessDefinition aCorrespondingMainPD = aCorrespondingPO.getMainProcessDefinition();
//                                Validations.check(null == aCorrespondingMainPD, new OmCode(retCodeConfig.getNotFoundProcessDefinition(), ""));
//                                ObjectIdentifier mainPDID = ObjectIdentifier.build(aCorrespondingMainPD.getIdentifier(), aCorrespondingMainPD.getPrimaryKey());
//                                log.info("mainPDID: {}",mainPDID.getValue());
//                                String correspondOpeNumber = aCorrespondingPO.getOperationNumber();
//                                log.info("correspondOpeNumber: {}",correspondOpeNumber);
//                                log.info("nCurPOWfrLen: {}",nCurPOWfrLen);
//                                log.info("nCorPOWaferLen: {}",nCorPOWaferLen);
//
//                                for (int nCurPOWfrCnt = 0; nCurPOWfrCnt < nCurPOWfrLen; nCurPOWfrCnt++) {
//                                    boolean bWaferFoundFlag = false;
//                                    log.info("counter nCurPOWfrCnt: {}",nCurPOWfrCnt);
//                                    log.info("waferID: {}",ObjectIdentifier.fetchValue(curLotWaferIDs.get(nCurPOWfrCnt)));
//                                    for (int nCorPOWfrCnt = 0; nCorPOWfrCnt < nCorPOWaferLen; nCorPOWfrCnt++) {
//                                        log.info("counter nCorPOWfrCnt: {}",nCorPOWfrCnt);
//                                        if (ObjectUtils.equalsWithValue(curLotWaferIDs.get(nCurPOWfrCnt), strCorrespondingProcessWaferSeq.get(nCorPOWfrCnt).getWaferID())){
//                                            bWaferFoundFlag = true;
//                                            log.info("bWaferFoundFlag = TRUE");
//                                            break;
//                                        }
//                                    }
//                                    if (BooleanUtils.isTrue(bWaferFoundFlag)){
//                                        // This wafer information exist in default corresponding PO
//                                        log.info("TRUE == bWaferFoundFlag");
//                                        strWaferIDSeq.get(0).getWaferIDs().add(ObjectIdentifier.fetchValue(curLotWaferIDs.get(nCurPOWfrCnt)));
//                                        Integer count = waferCnt.get(0);
//                                        waferCnt.set(0, count++);
//                                    }else {
//                                        //--------------------------------------------------------------------------
//                                        // Wafer isn't found in process wafers of default PO
//                                        // Get PO from
//                                        //--------------------------------------------------------------------------
//                                        CimProcessOperation aPOtmp = aPO.findProcessOperationForOperationNumberAndWafer(mainPDID.getValue(),
//                                                correspondOpeNumber,
//                                                curLotWaferIDs.get(nCurPOWfrCnt).getValue());
//                                        if (null == aPOtmp){
//                                            log.info("aPOtmp is null");
//                                            continue;
//                                        }
//                                        String strTmpPOObj = aPOtmp.getPrimaryKey();
//                                        log.info("strTmpPOObj: {}",strTmpPOObj);
//                                        boolean bPOFoundFlag = false;
//                                        for (k = 0; k < nPoCnt; k++) {
//                                            log.info("counter k: {}",k);
//                                            if (StringUtils.equals(strPOObjs.get(k),strTmpPOObj)){
//                                                // PO of wafer was already added in PO list(strPOObjs[]). Add wafer ID to strWaferIDSeq[].
//                                                log.info("strPOObjs[k] == strTmpPOObj: {}",strTmpPOObj);
//                                                log.info("waferID: {}",curLotWaferIDs.get(nCurPOWfrCnt).getValue());
//                                                bPOFoundFlag = true;
//                                                strWaferIDSeq.get(k).getWaferIDs().set(waferCnt.get(k),ObjectIdentifier.fetchValue(curLotWaferIDs.get(nCurPOWfrCnt)));
//                                                Integer count = waferCnt.get(k);
//                                                waferCnt.set(k,count++);
//                                                break;
//                                            }
//                                        }
//                                        if (BooleanUtils.isFalse(bPOFoundFlag)){
//                                            // PO of wafer doesn't exit in PO list(strPOObjs[]). Add PO to strPOObjs[] and wafer ID to strWaferIDSeq[].
//                                            log.info("FALSE == bPOFoundFlag");
//                                            log.info("nPoCnt: {}",nPoCnt);
//                                            strPOObjs.set(nPoCnt,strTmpPOObj);
//                                            waferCnt.set(nPoCnt,0);
//                                            strWaferIDSeq.get(nPoCnt).getWaferIDs().set(waferCnt.get(nPoCnt), ObjectIdentifier.fetchValue(curLotWaferIDs.get(nCurPOWfrCnt)));
//                                            Integer count = waferCnt.get(nPoCnt);
//                                            waferCnt.set(nPoCnt,count++);
//                                            nPoCnt++;
//                                        }
//                                    }
//                                }
//                            }
//                            for (int i = 0; i < nPoCnt; i++) {
//                                //nothin
//                            }
//                        }else {
//                            strPOObjs.add(0,aPO.getPrimaryKey());
//                            log.info("strPOObjs[0]: {}",strPOObjs.get(0));
//                        }
//                        int nPOLenInCorrPO = ArrayUtils.getSize(strPOObjs);
//                        for (int nCorPOCnt = 0; nCorPOCnt < nPOLenInCorrPO; nCorPOCnt++) {
//                            Infos.SpcCheckLot spcCheckLot = new Infos.SpcCheckLot();
//                            spcCheckLots.add(spcCheckLot);
//                            spcCheckLot.setProductID(ObjectIdentifier.build(spcInput.getProductID().getValue(),aProdSpec.getPrimaryKey()));
//                            log.info("strPOObjs[nCorPOCnt]: {}",strPOObjs.get(nCorPOCnt));
//                            //----------------------------------------------------------------------------------------
//                            //   Initialize spcInput with original spcInput(tmpSpcInput)
//                            //----------------------------------------------------------------------------------------
//                            spcInput = tmpSpcInput;
//                            aCheckPO = baseCoreFactory.getBO(CimProcessOperation.class,strPOObjs.get(nCorPOCnt));
//
//                            //-----------------
//                            //   set dcItems
//                            //-----------------
//                            int len = ArrayUtils.getSize(strDCDef.get(m).getDcItems());
//                            int nItemCnt = 0;
//                            List<Infos.SpcDcItem> dcItems = new ArrayList<>();
//                            spcInput.setDcItems(dcItems);
//                            for (int iCnt = 0; iCnt < len; iCnt++) {
//                                Boolean bSetToInput = false;
//                                if (BooleanUtils.isTrue(bCorPOWaferInfoExist)){
//                                    log.info("TRUE == bCorPOWaferInfoExist");
//                                    if (!ObjectUtils.isEmptyWithValue(strDCDef.get(m).getDcItems().get(iCnt).getWaferID())){
//                                        log.info("strDCDef[m].strDCItem[iCnt].measurementType: {}",strDCDef.get(m).getDcItems().get(iCnt).getMeasurementType());
//                                        int wfrLen = ArrayUtils.getSize(strWaferIDSeq.get(nCorPOCnt).getWaferIDs());
//                                        for (int wfrCnt = 0; wfrCnt < wfrLen; wfrCnt++) {
//                                            if (ObjectUtils.equalsWithValue(strDCDef.get(m).getDcItems().get(iCnt).getWaferID(), strWaferIDSeq.get(nCorPOCnt).getWaferIDs().get(wfrCnt))){
//                                                bSetToInput = true;
//                                                break;
//                                            }
//                                        }
//                                    }else {
//                                        if (0 == nCorPOCnt){
//                                            log.info("0 == nCorPOCnt");
//                                            bSetToInput = true;
//                                        }else {
//                                            log.info("1 < nCorPOCnt");
//                                            bSetToInput = false;
//                                        }
//                                    }
//                                }else {
//                                    log.info("else");
//                                    bSetToInput = true;
//                                }
//                                if (bSetToInput){
//                                    log.info("TRUE == bSetToInput");
//                                    String aString = strDCDef.get(m).getDcItems().get(iCnt).getDataValue();
//                                    Infos.SpcDcItem spcDcItem = new Infos.SpcDcItem();
//                                    dcItems.add(nItemCnt,spcDcItem);
//                                    spcDcItem.setDataItemName(strDCDef.get(m).getDcItems().get(iCnt).getDataCollectionItemName());
//                                    if (StringUtils.equals(aString,"*") ||
//                                            StringUtils.equals(BizConstant.SP_DCDEF_VAL_STRING,strDCDef.get(m).getDcItems().get(iCnt).getDataType())){
//                                        spcDcItem.setDataValue("");
//                                    }else {
//                                        spcDcItem.setDataValue(strDCDef.get(m).getDcItems().get(iCnt).getDataValue());
//                                    }
//                                    spcDcItem.setWaferID(ObjectIdentifier.fetchValue(strDCDef.get(m).getDcItems().get(iCnt).getWaferID()));
//                                    spcDcItem.setWaferPosition(strDCDef.get(m).getDcItems().get(iCnt).getWaferPosition());
//                                    spcDcItem.setSitePosition(strDCDef.get(m).getDcItems().get(iCnt).getSitePosition());
//                                    if (StringUtils.isNotEmpty(strDCDef.get(m).getDcItems().get(iCnt).getTargetValue())){
//                                        spcDcItem.setTargetValue(Double.valueOf(strDCDef.get(m).getDcItems().get(iCnt).getTargetValue()));
//                                    }else {
//                                        spcDcItem.setTargetValue(0.0);
//                                    }
//                                    spcDcItem.setSpecCheckResult(strDCDef.get(m).getDcItems().get(iCnt).getSpecCheckResult());
//                                    nItemCnt++;
//                                }
//                            }
//                            if (null == aCheckPO){
//                                log.info("acheckPO is null");
//                                continue;
//                            }
//                            CimMachine aMachine = aCheckPO.getAssignedMachine();
//                            CimMachineRecipe aRecipe = aCheckPO.getAssignedMachineRecipe();
//                            CimProcessDefinition aProcessMainPD = aCheckPO.getMainProcessDefinition();
//                            spcInput.setProcessOperationNumber(aCheckPO.getOperationNumber());
//                            spcInput.setProcessTimestamp(DateUtils.convertToSpecString(aCheckPO.getActualCompTimeStamp()));
//                            spcInput.setMeasurementTimestamp(DateUtils.convertToSpecString(objCommon.getTimeStamp().getReportTimeStamp()));
//
//                            CimProcessDefinition aProcessPD = aCheckPO.getProcessDefinition();
//                            if (null != aProcessPD){
//                                spcInput.setProcessProcessDefinitionID(ObjectIdentifier.build(aProcessPD.getIdentifier(),aProcessPD.getPrimaryKey()));
//                            }
//                            spcCheckLot.setProcessOperationNumber(spcInput.getProcessOperationNumber());
//                            /*------------------------------------*/
//                            /*   Get FabID of current operation   */
//                            /*------------------------------------*/
//                            String currentFabID = aPO.getFabID();
//                            /*------------------------------------------*/
//                            /*   Get FabID of corresponding operation   */
//                            /*------------------------------------------*/
//                            String correspondingFabID = aCheckPO.getFabID();
//                            log.info("FabID(current): {}", currentFabID);
//                            log.info("FabID(corrpnd): {}", correspondingFabID);
//
//                            Boolean requestOtherFabFlag = false;
//                            List<Infos.InterFabDestinationInfo> objFabInfoOut = null;
//                            // Compare the both FabID
//                            if (StringUtils.isNotEmpty(currentFabID) && StringUtils.isNotEmpty(correspondingFabID)
//                                 && !StringUtils.equals(currentFabID,correspondingFabID)) {
//                                requestOtherFabFlag = true;
//                                /*----------------------------------------------------*/
//                                /*   Get SPCServer information of corresponding Fab   */
//                                /*----------------------------------------------------*/
//                                //【step-7】 fabInfo_GetDR
//                                objFabInfoOut = fabMethod.fabInfoGetDR(objCommon,correspondingFabID);
//                            }
//                            if (BooleanUtils.isFalse(requestOtherFabFlag)){
//                                log.info("requestOtherFabFlag is FALSE.");
//                                if (null == aMachine){
//                                    log.info("aMachine is null");
//                                    continue;
//                                }
//                                if (aRecipe == null){
//                                    continue;
//                                }
//                                Validations.check(null == aProcessMainPD,retCodeConfig.getNotFoundMainRoute());
//                                spcInput.setProcessEquipmentID(ObjectIdentifier.build(aMachine.getIdentifier(),aMachine.getPrimaryKey()));
//                                spcInput.setProcessRecipeID(ObjectIdentifier.build(aRecipe.getIdentifier(),aRecipe.getPrimaryKey()));
//                                spcInput.setProcessMainProcessDefinitionID(ObjectIdentifier.build(aProcessMainPD.getIdentifier(),aProcessMainPD.getPrimaryKey()));
//                            }else {
//                                log.info("requestOtherFabFlag is TRUE.");
//                                // correspondingPO's machine/recipe/mainPD is nil.
//                                // there info get by processOperation_Info_GetDR
//                                //【step-8】processOperation_Info_GetDR
//                                Inputs.ObjProcessOperationInfoGetDrIn param = new Inputs.ObjProcessOperationInfoGetDrIn();
//                                param.setPoObj(aCheckPO.getPrimaryKey());
//                                Infos.ProcessOperationInfo processOperationInfoOut = processMethod.processOperationInfoGetDr(objCommon, param);
//                                spcInput.setProcessEquipmentID(processOperationInfoOut.getAsgnEqpID());
//                                spcInput.setProcessRecipeID(processOperationInfoOut.getAsgnRecipeID());
//                                spcInput.setProcessMainProcessDefinitionID(processOperationInfoOut.getMainPDID());
//                                log.info("eqpID   {}", ObjectIdentifier.fetchValue(spcInput.getProcessEquipmentID()));
//                                log.info("recipeID {}", ObjectIdentifier.fetchValue(spcInput.getProcessRecipeID()));
//                                log.info("mainPDID {}", ObjectIdentifier.fetchValue(spcInput.getMainProcessDefinitionID()));
//                            }
//                            spcCheckLot.setLotID(spcInput.getLotID());
//                            spcCheckLot.setProcessEquipmentID(spcInput.getProcessEquipmentID());
//                            spcCheckLot.setProcessMachineRecipeID(spcInput.getProcessRecipeID());
//                            spcCheckLot.setProcessRouteID(spcInput.getProcessMainProcessDefinitionID());
//                            spcCheckLot.setProcessFabID(correspondingFabID);
//                            spcCheckLot.setProcessObjrefPO(aCheckPO.getPrimaryKey());
//                            spcCheckLot.setRequestOtherFabFlag(requestOtherFabFlag);
//                            spcCheckLot.setDcDefID(spcInput.getDcDefID());
//                            spcCheckLot.setDcSpecID(strDCDef.get(m).getDataCollectionSpecificationID());
//                            spcInput.setProcessFabID(correspondingFabID);
//                            if (StringUtils.isEmpty(correspondingFabID)){
//                                spcInput.setProcessFabID("MEMS");
//                            }
//
//                            /*------------------------------------------------------------------------*/
//                            /*                                                                        */
//                            /*   Check SPC Check is Required or Not                                   */
//                            /*                                                                        */
//                            /*------------------------------------------------------------------------*/
//                            String hFRSPC_MEQP_ID = ObjectIdentifier.fetchValue(spcInput.getProcessEquipmentID());
//                            String hFRSPC_MRECIPE_ID = ObjectIdentifier.fetchValue(spcInput.getProcessRecipeID());
//                            String hFRSPC_PEQP_ID = ObjectIdentifier.fetchValue(spcInput.getProcessEquipmentID());
//                            String hFRSPC_PRECIPE_ID = ObjectIdentifier.fetchValue(spcInput.getProcessRecipeID());
//
//                            // Make searching criteria of process recipe ID with wildcard for recipe version.
//                            String  hFRSPC_PRECIPE_ID_WITH_WILDCARD = null;
//                            int orgLen = StringUtils.length(hFRSPC_PRECIPE_ID);
//                            int index = 0;
//                            for (int i = orgLen - 1; i > 0; i--){
//                                if (hFRSPC_PRECIPE_ID.charAt(i) == '.'){
//                                    index = i;
//                                    break;
//                                }
//                            }
//                            hFRSPC_PRECIPE_ID_WITH_WILDCARD = hFRSPC_PRECIPE_ID.substring(0,index);
//                            hFRSPC_PRECIPE_ID_WITH_WILDCARD = hFRSPC_PRECIPE_ID_WITH_WILDCARD.concat(".*");
//
//                            log.info( "processEquipmentID {}", hFRSPC_MEQP_ID   );
//                            log.info( "processRecipeID   {}", hFRSPC_MRECIPE_ID);
//                            log.info( "processRecipeID with wildcard for version {}", hFRSPC_PRECIPE_ID_WITH_WILDCARD);
//
//                            spcInput.setCollectionType(strDCDef.get(0).getDataCollectionType());
//                            int hCount = 0;
//                            if (StringUtils.equals(BizConstant.SP_DCDEF_COLLECTION_MEASUREMENT,strDCDef.get(0).getDataCollectionType())){
//                                log.info("EXEC SQL SELECT FROM FRSPC_M");
//                                List<Object[]> queryFirst = cimJpaRepository.query("SELECT * FROM FRSPC_M WHERE EQP_ID = ?1 AND RECIPE_ID = ?2 AND RECIPE_ID IN (?3, ?4)", hFRSPC_PEQP_ID, hFRSPC_MRECIPE_ID, hFRSPC_MRECIPE_ID, hFRSPC_PRECIPE_ID_WITH_WILDCARD);
//                                hCount = ArrayUtils.getSize(queryFirst);
//                            }else {
//                                log.info("EXEC SQL SELECT FROM FRSPC_P");
//                                List<Object[]> querySecond = cimJpaRepository.query("SELECT * FROM FRSPC_P WHERE EQP_ID = ?1 AND RECIPE_ID = ?2 AND RECIPE_ID IN (?3, ?4)", hFRSPC_PEQP_ID, hFRSPC_PRECIPE_ID, hFRSPC_PRECIPE_ID, hFRSPC_PRECIPE_ID_WITH_WILDCARD);
//                                hCount = ArrayUtils.getSize(querySecond);
//                            }
//                            if (hCount == 0){
//                                log.info("hCount == 0");
//                                if (StringUtils.isEmpty(spcCheckLot.getSpcCheckResult())){
//                                    spcCheckLot.setSpcCheckResult("N");
//                                }
//                                continue;
//                            }
//                            /*------------------------------------------------------------------------*/
//                            /*                                                                        */
//                            /*   Set Input Values for doSpcCheck() - 2 (continued from - 1)           */
//                            /*                                                                        */
//                            /*------------------------------------------------------------------------*/
//
//                            //-------------------------
//                            //   set operationNumber
//                            //-------------------------
//                            spcInput.setOperationNumber(aPO.getOperationNumber());
//
//                            //------------------------
//                            //   set operationName
//                            //------------------------
//                            spcInput.setOperationName(aPO.getName());
//
//                            //---------------------
//                            //   set ownerUserID
//                            //---------------------
//                            CimPerson aLotOwner = aLot.getLotOwner();
//                            if (null == aLotOwner){
//                                log.info("lot owner is null");
//                                spcInput.setOwnerUserID(ObjectIdentifier.build(BizConstant.SP_PPTSVCMGR_PERSON,""));
//                            }else {
//                                log.info("lot owner is not null");
//                                spcInput.setOwnerUserID(ObjectIdentifier.build(aLotOwner.getIdentifier(),aLotOwner.getPrimaryKey()));
//                            }
//
//                            //------------------------
//                            //   set collectionType
//                            //------------------------
//                            boolean lotHoldActionExistFlag = false;
//                            boolean processHoldActionExistFlag = false;
//                            boolean equipmentHoldActionExistFlag = false;
//                            boolean recipeHoldActionExistFlag = false;
//                            boolean routeHoldActionExistFlag = false;
//                            boolean reworkBranchActionExistFlag = false;
//                            boolean mailSendActionExistFlag = false;
//                            boolean bankMoveActionExistFlag = false;
//                            boolean equipmentAndRecipeHoldActionExistFlag  = false;
//                            Boolean processAndEquipmentHoldActionExistFlag = false;
//                            boolean chamberHoldActionExistFlag = false;
//                            Boolean chamberRecipeHoldActionExistFlag = false;
//
//                            //-------------------------------------------------------------------------------
//                            //  Get ChamberIDs and WaferIDs from PO.
//                            //  At first, Collect ChamberIDs to spcInput.waferIDByChambers[chamCnt].procChamber
//                            //  And then collect WaferIDs which processed at each Chamber to
//                            //  spcInput.waferIDByChambers[chamCnt].waferIDs[wCnt]
//                            //-------------------------------------------------------------------------------
//                            List<Infos.ProcessResourceInfoGroupByProcessResource> aPosProcResrcInfoByResrces = null;
//                            if (StringUtils.equals(BizConstant.SP_DCDEF_COLLECTION_MEASUREMENT,strDCDef.get(0).getDataCollectionType())){
//                                log.info("dataCollectionType is Measurement");
//                                if (null != aCheckPO){
//                                    log.info("aCheckPO is not null");
//                                    aPosProcResrcInfoByResrces = aCheckPO.allAssignedProcessResourceInfoGroupByProcessResource();
//                                }
//                            }else {
//                                log.info("dataCollectionType is not Measurement");
//                                if (null != aPO){
//                                    log.info("aPO is not null");
//                                    aPosProcResrcInfoByResrces = aPO.allAssignedProcessResourceInfoGroupByProcessResource();
//                                }
//                            }
//                            int chamberLen = ArrayUtils.getSize(aPosProcResrcInfoByResrces);
//                            List<Infos.WaferIDByChamber> waferIDByChambers = new ArrayList<>();
//                            spcInput.setWaferIDByChambers(waferIDByChambers);
//                            for (int chamCnt = 0; chamCnt < chamberLen; chamCnt++) {
//                                Infos.WaferIDByChamber waferIDByChamber = new Infos.WaferIDByChamber();
//                                waferIDByChambers.add(waferIDByChamber);
//                                waferIDByChamber.setProcChamber(ObjectIdentifier.fetchValue(aPosProcResrcInfoByResrces.get(chamCnt).getProcessResourceID()));
//                                log.info("Process Chamber ID: {}",spcInput.getWaferIDByChambers().get(chamCnt).getProcChamber());
//                                int waferLen = ArrayUtils.getSize(aPosProcResrcInfoByResrces.get(chamCnt).getProcessWafers());
//                                List<String> waferIDs = new ArrayList<>();
//                                waferIDByChamber.setWaferIDs(waferIDs);
//                                for (int wCnt = 0; wCnt < waferLen; wCnt++) {
//                                    waferIDs.add(aPosProcResrcInfoByResrces.get(chamCnt).getProcessWafers().get(wCnt));
//                                    log.info("Procee WaferID: {}",spcInput.getWaferIDByChambers().get(chamCnt).getWaferIDs().get(wCnt));
//                                }
//                            }
//                            /*------------------------------------------------------------------------*/
//                            /*                                                                        */
//                            /*   Send SPC Check Request to SPC service Manager                        */
//                            /*                                                                        */
//                            /*------------------------------------------------------------------------*/
//
//                            //---------------------------------
//                            //   Bind to SPC Service Manager
//                            //---------------------------------
//                            String tmpSPCServerName = serverName;
//                            if (BooleanUtils.isTrue(requestOtherFabFlag)){
//                                log.info("replace SPCServerName(Corresponding operation is other Fab)");
//                                tmpSPCServerName = objFabInfoOut.get(0).getSPCServerName();
//                                log.info("replaced SPCServerName: {}",objFabInfoOut.get(0).getSPCServerName());
//                            }
//                            String SPCServerName = null;
//                            if (!StringUtils.isEmpty(serverName)){
//                                if (!serverName.contains(":")){
//                                    SPCServerName = ":" + tmpSPCServerName;
//                                } else {
//                                    SPCServerName = tmpSPCServerName;
//                                }
//                            }
//                            if (requestOtherFabFlag){
//                                SPCHostName = objFabInfoOut.get(0).getSPCHostName();
//                            }
//                            //---------------------------------
//                            //   Bind to TCS Service Manager
//                            //---------------------------------
//
//                            //-------------------------
//                            //   Send Request to SPC
//                            //-------------------------
//                            // 【step-9】 doSpcCheck( spcInput, spcOutput, envTimeOut );
//                            spcInput.setObjCommonIn(objCommon);
//                            spcInput.setEquipmentID(equipmentID);
//                            spcOutput = this.spcCheck(spcInput);
//                            Infos.SpcIFParm tmpSPcIFParm = new Infos.SpcIFParm();
//                            tmpSPcIFParm.setSpcInput(new Inputs.SpcInput());
//                            tmpSPcIFParm.getSpcInput().setRequestUserID(spcInput.getRequestUserID());
//                            tmpSPcIFParm.getSpcInput().setLotID(spcInput.getLotID());
//                            tmpSPcIFParm.getSpcInput().setProcessEquipmentID(spcInput.getProcessEquipmentID());
//                            tmpSPcIFParm.getSpcInput().setProcessRecipeID(spcInput.getProcessRecipeID());
//                            tmpSPcIFParm.getSpcInput().setProcessMainProcessDefinitionID(spcInput.getProcessMainProcessDefinitionID());
//                            tmpSPcIFParm.getSpcInput().setProcessProcessDefinitionID(spcInput.getProcessProcessDefinitionID());
//                            tmpSPcIFParm.getSpcInput().setProcessOperationNumber(spcInput.getProcessOperationNumber());
//                            tmpSPcIFParm.getSpcInput().setMeasurementEquipmentID(spcInput.getMeasurementEquipmentID());
//                            tmpSPcIFParm.getSpcInput().setTechnologyID(spcInput.getTechnologyID());
//                            tmpSPcIFParm.getSpcInput().setProductGroupID(spcInput.getProductGroupID());
//                            tmpSPcIFParm.getSpcInput().setProductID(spcInput.getProductID());
//                            tmpSPcIFParm.getSpcInput().setReticleID(spcInput.getReticleID());
//                            tmpSPcIFParm.getSpcInput().setFixtureID(spcInput.getFixtureID());
//                            tmpSPcIFParm.getSpcInput().setMainProcessDefinitionID(spcInput.getMainProcessDefinitionID());
//                            tmpSPcIFParm.getSpcInput().setOperationNumber(spcInput.getOperationNumber());
//                            tmpSPcIFParm.getSpcInput().setOperationName(spcInput.getOperationName());
//                            tmpSPcIFParm.getSpcInput().setOwnerUserID(spcInput.getOwnerUserID());
//                            tmpSPcIFParm.getSpcInput().setCollectionType(spcInput.getCollectionType());
//                            tmpSPcIFParm.getSpcInput().setDcDefID(spcInput.getDcDefID());
//                            tmpSPcIFParm.getSpcInput().setLotComment(spcInput.getLotComment());
//
//                            int iSize = ArrayUtils.getSize(spcInput.getDcItems());
//                            tmpSPcIFParm.getSpcInput().setDcItems(new ArrayList<>());
//                            for (int iSizeCnt = 0; iSizeCnt < iSize; iSizeCnt++) {
//                                Infos.SpcDcItem spcDcItem = new Infos.SpcDcItem();
//                                tmpSPcIFParm.getSpcInput().getDcItems().add(spcDcItem);
//                                spcDcItem.setDataItemName(spcInput.getDcItems().get(iSizeCnt).getDataItemName());
//                                spcDcItem.setWaferID(spcInput.getDcItems().get(iSizeCnt).getWaferID());
//                                spcDcItem.setWaferPosition(spcInput.getDcItems().get(iSizeCnt).getWaferPosition());
//                                spcDcItem.setSitePosition(spcInput.getDcItems().get(iSizeCnt).getSitePosition());
//                                spcDcItem.setDataValue(spcInput.getDcItems().get(iSizeCnt).getDataValue());
//                                spcDcItem.setTargetValue(spcInput.getDcItems().get(iSizeCnt).getTargetValue());
//                                spcDcItem.setSpecCheckResult(spcInput.getDcItems().get(iSizeCnt).getSpecCheckResult());
//                                spcDcItem.setComment(spcInput.getDcItems().get(iSizeCnt).getComment());
//                            }
//                            iSize = ArrayUtils.getSize(spcInput.getWaferIDByChambers());
//                            tmpSPcIFParm.getSpcInput().setWaferIDByChambers(new ArrayList<>());
//                            for (int iSizeCnt = 0; iSizeCnt < iSize; iSizeCnt++) {
//                                Infos.WaferIDByChamber waferIDByChamber = new Infos.WaferIDByChamber();
//                                tmpSPcIFParm.getSpcInput().getWaferIDByChambers().add(waferIDByChamber);
//                                waferIDByChamber.setProcChamber(spcInput.getWaferIDByChambers().get(iSizeCnt).getProcChamber());
//                                waferIDByChamber.setWaferIDs(spcInput.getWaferIDByChambers().get(iSizeCnt).getWaferIDs());
//                            }
//
//                            //---------------------
//                            // Set SPC Output Data
//                            //---------------------
//                            tmpSPcIFParm.setSpcOutput(new Outputs.SpcOutput());
//                            tmpSPcIFParm.getSpcOutput().setTxRC(spcOutput.getTxRC());
//                            tmpSPcIFParm.getSpcOutput().setLotID(spcOutput.getLotID());
//                            tmpSPcIFParm.getSpcOutput().setLotRC(spcOutput.getLotRC());
//                            tmpSPcIFParm.getSpcOutput().setLotHoldAction(spcOutput.getLotHoldAction());
//                            tmpSPcIFParm.getSpcOutput().setEquipmentHoldAction(spcOutput.getEquipmentHoldAction());
//                            tmpSPcIFParm.getSpcOutput().setProcessHoldAction(spcOutput.getProcessHoldAction());
//                            tmpSPcIFParm.getSpcOutput().setRecipeHoldAction(spcOutput.getRecipeHoldAction());
//                            tmpSPcIFParm.getSpcOutput().setReworkBranchAction(spcOutput.getReworkBranchAction());
//                            tmpSPcIFParm.getSpcOutput().setMailSendAction(spcOutput.getMailSendAction());
//                            tmpSPcIFParm.getSpcOutput().setBankMoveAction(spcOutput.getBankMoveAction());
//                            tmpSPcIFParm.getSpcOutput().setBankID(spcOutput.getBankID());
//                            tmpSPcIFParm.getSpcOutput().setReworkRouteID(spcOutput.getReworkRouteID());
//                            tmpSPcIFParm.getSpcOutput().setOtherAction(spcOutput.getOtherAction());
//
//                            iSize = ArrayUtils.getSize(spcOutput.getChamberHoldActions());
//                            tmpSPcIFParm.getSpcOutput().setChamberHoldActions(new ArrayList<>());
//                            for (int iSizeCnt = 0; iSizeCnt < iSize; iSizeCnt++) {
//                                Infos.ChamberHoldAction chamberHoldAction = new Infos.ChamberHoldAction();
//                                tmpSPcIFParm.getSpcOutput().getChamberHoldActions().add(chamberHoldAction);
//                                chamberHoldAction.setChamberID(spcOutput.getChamberHoldActions().get(iSizeCnt).getChamberID());
//                                chamberHoldAction.setChamberHoldAction(spcOutput.getChamberHoldActions().get(iSizeCnt).getChamberHoldAction());
//                                chamberHoldAction.setChamberRecipeHoldAction(spcOutput.getChamberHoldActions().get(iSizeCnt).getChamberRecipeHoldAction());
//                            }
//                            iSize = ArrayUtils.getSize(spcOutput.getItemResults());
//                            tmpSPcIFParm.getSpcOutput().setItemResults(new ArrayList<>());
//                            for (int iSizeCnt = 0; iSizeCnt < iSize; iSizeCnt++) {
//                                Results.SpcItemResult spcItemResult = new Results.SpcItemResult();
//                                tmpSPcIFParm.getSpcOutput().getItemResults().add(spcItemResult);
//                                spcItemResult.setDataItemName(spcOutput.getItemResults().get(iSizeCnt).getDataItemName());
//
//                                int jSize = ArrayUtils.getSize(spcOutput.getItemResults().get(iSizeCnt).getChartResults());
//                                tmpSPcIFParm.getSpcOutput().getItemResults().get(iSizeCnt).setChartResults(new ArrayList<>());
//                                for (int jSizeCnt = 0; jSizeCnt < jSize; jSizeCnt++) {
//                                    Results.SpcChartResult spcChartResult = new Results.SpcChartResult();
//                                    tmpSPcIFParm.getSpcOutput().getItemResults().get(iSizeCnt).getChartResults().add(spcChartResult);
//                                    spcChartResult.setChartGroupID(spcOutput.getItemResults().get(iSizeCnt).getChartResults().get(jSizeCnt).getChartGroupID());
//                                    spcChartResult.setChartID(spcOutput.getItemResults().get(iSizeCnt).getChartResults().get(jSizeCnt).getChartID());
//                                    spcChartResult.setChartType(spcOutput.getItemResults().get(iSizeCnt).getChartResults().get(jSizeCnt).getChartType());
//                                    spcChartResult.setChartRC(spcOutput.getItemResults().get(iSizeCnt).getChartResults().get(jSizeCnt).getChartRC());
//                                    spcChartResult.setChartOwnerMailAddress(spcOutput.getItemResults().get(iSizeCnt).getChartResults().get(jSizeCnt).getChartOwnerMailAddress());
//                                    spcChartResult.setChartSubOwnerMailAddresses(spcOutput.getItemResults().get(iSizeCnt).getChartResults().get(jSizeCnt).getChartSubOwnerMailAddresses());
//
//                                    int kSize = ArrayUtils.getSize(spcOutput.getItemResults().get(iSizeCnt).getChartResults().get(jSizeCnt).getReturnCodes());
//                                    tmpSPcIFParm.getSpcOutput().getItemResults().get(iSizeCnt).getChartResults().get(jSizeCnt).setReturnCodes(new ArrayList<>());
//                                    for (int kSizeCnt = 0; kSizeCnt < kSize; kSizeCnt++) {
//                                        Results.SpcCheckResultByRule spcCheckResultByRule = new Results.SpcCheckResultByRule();
//                                        tmpSPcIFParm.getSpcOutput().getItemResults().get(iSizeCnt).getChartResults().get(jSizeCnt).getReturnCodes().add(spcCheckResultByRule);
//                                        spcCheckResultByRule.setRule(spcOutput.getItemResults().get(iSizeCnt).getChartResults().get(jSizeCnt).getReturnCodes().get(kSizeCnt).getRule());
//                                        spcCheckResultByRule.setDescription(spcOutput.getItemResults().get(iSizeCnt).getChartResults().get(jSizeCnt).getReturnCodes().get(kSizeCnt).getDescription());
//                                        spcCheckResultByRule.setReturnCodeStatus(spcOutput.getItemResults().get(iSizeCnt).getChartResults().get(jSizeCnt).getReturnCodes().get(kSizeCnt).getReturnCodeStatus());
//                                    }
//                                }
//                            }
//                            //-------------------------------------
//                            // Set Result
//                            //-------------------------------------
//                            spcIFParmList.add(tmpSPcIFParm);
//
//                            /*------------------------------------------------------------------------*/
//                            /*                                                                        */
//                            /*   Set the result                                                       */
//                            /*                                                                        */
//                            /*------------------------------------------------------------------------*/
//                            log.info("spcOutput->lotRC = {}",spcOutput.getLotRC());
//                            if (StringUtils.isEmpty(spcCheckLot.getSpcCheckResult())){
//                                log.info("Update spcCheckResult ! {}",spcOutput.getLotRC());
//                                spcCheckLot.setSpcCheckResult(spcOutput.getLotRC());
//                            }else {
//                                // Check priority of SPC Check Result
//                                // The order of priorities is as follows.
//                                //   H : 1 (highest)
//                                //   W : 2
//                                //   O : 3
//                                //   N : 4  //DSIV00001021
//                                int prioritySpcCheckResult = 0;
//                                int priorityLotRC = 0;
//                                if (StringUtils.equals(BizConstant.SP_SPCCHECK_HOLDLIMITOFF, spcCheckLot.getSpcCheckResult())){
//                                    prioritySpcCheckResult = 1;
//                                }else if (StringUtils.equals(BizConstant.SP_SPCCHECK_WARNINGLIMITOFF, spcCheckLot.getSpcCheckResult())){
//                                    prioritySpcCheckResult = 2;
//                                }else if (StringUtils.equals(BizConstant.SP_SPCCHECK_NOTDEFINED, spcCheckLot.getSpcCheckResult())){
//                                    prioritySpcCheckResult = 4;
//                                }else {
//                                    prioritySpcCheckResult = 3;
//                                }
//                                log.info("priority of spcCheckResult : {}",prioritySpcCheckResult);
//                                if (StringUtils.equals(BizConstant.SP_SPCCHECK_HOLDLIMITOFF, spcOutput.getLotRC())){
//                                    priorityLotRC = 1;
//                                }else if (StringUtils.equals(BizConstant.SP_SPCCHECK_WARNINGLIMITOFF, spcOutput.getLotRC())){
//                                    priorityLotRC = 2;
//                                }else if (StringUtils.equals(BizConstant.SP_SPCCHECK_NOTDEFINED, spcOutput.getLotRC())){
//                                    priorityLotRC = 4;
//                                }else {
//                                    priorityLotRC = 3;
//                                }
//                                log.info("priority of spcOutput.lotRC: {}",priorityLotRC);
//                                if (priorityLotRC < prioritySpcCheckResult){
//                                    log.info("Update spcCheckResult ! {}",spcOutput.getLotRC());
//                                    spcCheckLot.setSpcCheckResult(spcOutput.getLotRC());
//                                }
//                            }
//                            Infos.SpcResult spcResult = new Infos.SpcResult();
//                            spcCheckLot.setSpcResult(spcResult);
//                            List<Infos.SpcDcItemAndChart> spcDcItem = new ArrayList<>();
//                            spcResult.setSpcDcItem(spcDcItem);
//                            if (StringUtils.equals(BizConstant.SP_SPCCHECK_HOLDLIMITOFF, spcOutput.getLotRC())){
//                                log.info("lotRC == SP_SPCCHECK_HOLDLIMITOFF");
//                                spcCheckLot.setSpcActionCode(new ArrayList<>());
//                                if (StringUtils.equals(BizConstant.SP_ACTION_DONE, spcOutput.getLotHoldAction())){
//                                    log.info("lotHoldAction == SP_ACTION_DONE");
//                                    if (BooleanUtils.isFalse(lotHoldActionExistFlag)){
//                                        log.info("lotHoldActionExistFlag == FALSE");
//                                        spcCheckLot.getSpcActionCode().add(BizConstant.SP_ACTIONCODE_LOTHOLD);
//                                        lotHoldActionExistFlag = true;
//                                        actionCnt++;
//                                    }
//                                }
//                                if (StringUtils.equals(BizConstant.SP_ACTION_DONE,spcOutput.getProcessHoldAction())){
//                                    log.info("processHoldAction == SP_ACTION_DONE");
//                                    if (BooleanUtils.isFalse(processHoldActionExistFlag)){
//                                        log.info("processHoldActionExistFlag == FALSE");
//                                        spcCheckLot.getSpcActionCode().add(BizConstant.SP_ACTIONCODE_PROCESSHOLD);
//                                        processHoldActionExistFlag = true;
//                                        actionCnt++;
//                                    }
//                                }
//                                if (StringUtils.equals(BizConstant.SP_ACTION_DONE,spcOutput.getEquipmentHoldAction())){
//                                    log.info("equipmentHoldAction == SP_ACTION_DONE");
//                                    if (BooleanUtils.isFalse(equipmentHoldActionExistFlag)){
//                                        log.info("equipmentHoldActionExistFlag == FALSE");
//                                        spcCheckLot.getSpcActionCode().add(BizConstant.SP_ACTIONCODE_EQUIPMENTHOLD);
//                                        equipmentHoldActionExistFlag = true;
//                                        actionCnt++;
//                                    }
//                                }
//                                if (StringUtils.equals(BizConstant.SP_ACTION_DONE,spcOutput.getRecipeHoldAction())){
//                                    log.info("recipeHoldAction == SP_Action_Done");
//                                    if (BooleanUtils.isFalse(recipeHoldActionExistFlag)){
//                                        log.info("recipeHoldActionExistFlag == FALSE");
//                                        spcCheckLot.getSpcActionCode().add(BizConstant.SP_ACTIONCODE_RECIPEHOLD);
//                                        recipeHoldActionExistFlag = true;
//                                        actionCnt++;
//                                    }
//                                }
//                                if (StringUtils.equals(BizConstant.SP_ACTION_DONE,spcOutput.getReworkBranchAction())){
//                                    log.info("reworkBranchAction == SP_Action_Done");
//                                    if (BooleanUtils.isFalse(reworkBranchActionExistFlag)){
//                                        log.info("reworkBranchActionExistFlag == FALSE");
//                                        spcCheckLot.getSpcActionCode().add(BizConstant.SP_ACTIONCODE_REWORKBRANCH);
//                                        spcCheckLot.getSpcResult().setReworkRouteID(spcOutput.getReworkRouteID());
//                                        reworkBranchActionExistFlag = true;
//                                        actionCnt++;
//                                    }
//                                }
//                                if (StringUtils.equals(BizConstant.SP_ACTION_DONE,spcOutput.getMailSendAction())){
//                                    log.info("mailSendAction == SP_Action_Done");
//                                    if (BooleanUtils.isFalse(mailSendActionExistFlag)){
//                                        log.info("mailSendActionExistFlag == FALSE");
//                                        spcCheckLot.getSpcActionCode().add(BizConstant.SP_ACTIONCODE_MAILSEND);
//                                        mailSendActionExistFlag = true;
//                                        actionCnt++;
//                                    }
//                                }
//                                if (StringUtils.equals(BizConstant.SP_ACTION_DONE,spcOutput.getBankMoveAction())){
//                                    log.info("bankMoveAction == SP_Action_Done");
//                                    if (BooleanUtils.isFalse(bankMoveActionExistFlag)){
//                                        log.info("bankMoveActionExistFlag == FALSE");
//                                        spcCheckLot.getSpcActionCode().add(BizConstant.SP_ACTIONCODE_BANKMOVE);
//                                        spcCheckLot.getSpcResult().setBankID(spcOutput.getBankID());
//                                        bankMoveActionExistFlag = true;
//                                        actionCnt++;
//                                    }
//                                }
//                                if (StringUtils.isNotEmpty(spcOutput.getOtherAction())){
//                                    List<String> otherActions = new ArrayList<>();
//                                    String str = spcOutput.getOtherAction();
//                                    int totalActionCount = 0;
//                                    String[] split = str.split(";");
//                                    for (int i = 0; i < split.length; i++) {
//                                        otherActions.add(split[i]);
//                                        totalActionCount++;
//                                        if (totalActionCount >= 3){
//                                            break;
//                                        }
//                                    }
//                                    for (int actionCount = 0; actionCount < totalActionCount; actionCount++) {
//                                        if (StringUtils.equals(BizConstant.SP_ACTIONCODE_ROUTEHOLD,otherActions.get(actionCount))){
//                                            if (BooleanUtils.isFalse(routeHoldActionExistFlag)){
//                                                spcCheckLot.getSpcActionCode().add(BizConstant.SP_ACTIONCODE_ROUTEHOLD);
//                                                routeHoldActionExistFlag = true;
//                                                actionCnt++;
//                                            }
//                                        }
//                                        if (StringUtils.equals(BizConstant.SP_ACTIONCODE_EQUIPMENT_AND_RECIPEHOLD,otherActions.get(actionCount))){
//                                            if (BooleanUtils.isFalse(equipmentAndRecipeHoldActionExistFlag)){
//                                                spcCheckLot.getSpcActionCode().add(BizConstant.SP_ACTIONCODE_EQUIPMENT_AND_RECIPEHOLD);
//                                                equipmentAndRecipeHoldActionExistFlag = true;
//                                                actionCnt++;
//                                            }
//                                        }
//                                        if (StringUtils.equals(BizConstant.SP_ACTIONCODE_PROCESS_AND_EQUIPMENTHOLD,otherActions.get(actionCount))){
//                                            if (BooleanUtils.isFalse(equipmentAndRecipeHoldActionExistFlag)){
//                                                spcCheckLot.getSpcActionCode().add(BizConstant.SP_ACTIONCODE_PROCESS_AND_EQUIPMENTHOLD);
//                                                equipmentAndRecipeHoldActionExistFlag = true;
//                                                actionCnt++;
//                                            }
//                                        }
//                                    }
//                                }
//                                //---------------------------------------
//                                // Chamber Hold Action Check
//                                //---------------------------------------
//                                int chamberLenOp = ArrayUtils.getSize(spcOutput.getChamberHoldActions());
//                                if (chamberLenOp > 0){
//                                    log.info("spcOutPut.chamberHoldActions.length: {}",chamberLenOp);
//                                    //--------------------------------------
//                                    // Chamber Hold Action Loop
//                                    //--------------------------------------
//                                    for (int chamCnt = 0; chamCnt < chamberLenOp; chamCnt++) {
//                                        //-------------------------------
//                                        // Chamber Hold Action
//                                        //-------------------------------
//                                        if (StringUtils.equals(BizConstant.SP_ACTION_DONE, spcOutput.getChamberHoldActions().get(chamCnt).getChamberHoldAction())){
//                                            log.info("ChamberHoldAction is Done!");
//                                            //------------------------------------------
//                                            // Check Chamber and Action Code duplication
//                                            // Because EQP has some Chambers.
//                                            //-------------------------------------------
//                                            boolean bDupricateFlag = false;
//                                            for (int i = 0; i < totalCount; i++) {
//                                                int spcActCodeLen = ArrayUtils.getSize(spcCheckReqResult.getSpcCheckLot().get(i).getSpcActionCode());
//                                                for (int j = 0; j < spcActCodeLen; j++) {
//                                                    if (ObjectUtils.equalsWithValue(spcCheckReqResult.getSpcCheckLot().get(i).getProcessChamberID(), spcOutput.getChamberHoldActions().get(chamCnt).getChamberID())
//                                                        && StringUtils.equals(BizConstant.SP_ACTIONCODE_CHAMBERHOLD, spcCheckReqResult.getSpcCheckLot().get(i).getSpcActionCode().get(j))){
//                                                        log.info("Duplicate record exist.");
//                                                        bDupricateFlag = true;
//                                                        break;
//                                                    }
//                                                }
//                                                if (BooleanUtils.isTrue(bDupricateFlag)){
//                                                    break;
//                                                }
//                                            }
//                                            //-------------------------------------
//                                            // Set ChemberID and Action Code
//                                            //-------------------------------------
//                                            if (BooleanUtils.isFalse(bDupricateFlag)){
//                                                spcCheckLot.setProcessChamberID(new ObjectIdentifier(spcOutput.getChamberHoldActions().get(chamCnt).getChamberID()));
//                                                spcCheckLot.getSpcActionCode().add(actionCnt,BizConstant.SP_ACTIONCODE_CHAMBERHOLD);
//                                                chamberHoldActionExistFlag = true;
//
//                                                int iLen = ArrayUtils.getSize(spcOutput.getItemResults());
//                                                spcCheckLot.getSpcResult().setSpcDcItem(new ArrayList<>());
//                                                for (int i = 0; i < iLen; i++) {
//                                                    Infos.SpcDcItemAndChart spcDcItemAndChart = new Infos.SpcDcItemAndChart();
//                                                    spcCheckLot.getSpcResult().getSpcDcItem().add(k,spcDcItemAndChart);
//                                                    spcDcItemAndChart.setDataCollectionItemName(spcOutput.getItemResults().get(i).getDataItemName());
//                                                    int jLen = ArrayUtils.getSize(spcOutput.getItemResults().get(i).getChartResults());
//                                                    spcCheckLot.getSpcResult().getSpcDcItem().get(k).setSpcChartList(new ArrayList<>());
//                                                    for (int j = 0; j < jLen; j++) {
//                                                        Infos.SpcChart spcChart = new Infos.SpcChart();
//                                                        spcCheckLot.getSpcResult().getSpcDcItem().get(k).getSpcChartList().add(j,spcChart);
//                                                        spcChart.setChartGroupID(spcOutput.getItemResults().get(i).getChartResults().get(j).getChartGroupID());
//                                                        spcChart.setChartID(spcOutput.getItemResults().get(i).getChartResults().get(j).getChartID());
//                                                        spcChart.setChartType(spcOutput.getItemResults().get(i).getChartResults().get(j).getChartType());
//                                                        spcChart.setChartOwnerMailAddress(spcOutput.getItemResults().get(i).getChartResults().get(j).getChartOwnerMailAddress());
//                                                        spcChart.setChartSubOwnerMailAddress(spcOutput.getItemResults().get(i).getChartResults().get(j).getChartSubOwnerMailAddresses());
//                                                        int mLen = ArrayUtils.getSize(spcOutput.getItemResults().get(i).getChartResults().get(j).getReturnCodes());
//                                                        spcCheckLot.getSpcResult().getSpcDcItem().get(k).getSpcChartList().get(j).setSpcReturnCodeList(new ArrayList<>());
//                                                        for (int mm = 0; mm < mLen; mm++) {
//                                                            Infos.SpcReturnCode spcReturnCode = new Infos.SpcReturnCode();
//                                                            spcCheckLot.getSpcResult().getSpcDcItem().get(k).getSpcChartList().get(j).getSpcReturnCodeList().add(mm,spcReturnCode);
//                                                            spcReturnCode.setReturnCodeStatus(spcOutput.getItemResults().get(i).getChartResults().get(j).getReturnCodes().get(mm).getReturnCodeStatus());
//                                                            spcReturnCode.setRule(spcOutput.getItemResults().get(i).getChartResults().get(j).getReturnCodes().get(mm).getRule());
//                                                        }
//                                                    }
//                                                    k++;
//                                                }
//                                                k = 0;
//                                                actionCnt = 0;
//                                                totalCount++;
//                                                Infos.SpcCheckLot spcCheckLot1 = new Infos.SpcCheckLot();
//                                                spcCheckReqResult.getSpcCheckLot().add(spcCheckLot1);
//                                                spcCheckLot1.setLotID(spcInput.getLotID());
//                                                spcCheckLot1.setProcessEquipmentID(spcInput.getProcessEquipmentID());
//                                                spcCheckLot1.setProcessMachineRecipeID(spcInput.getProcessMainProcessDefinitionID());
//                                                spcCheckLot1.setProcessRouteID(spcInput.getProcessMainProcessDefinitionID());
//                                                spcCheckLot1.setProductID(ObjectIdentifier.build(spcInput.getProductID().getValue(),aProdSpec.getPrimaryKey()));
//                                                spcCheckLot1.setProcessOperationNumber(spcInput.getProcessOperationNumber());
//                                                spcCheckLot1.setProcessChamberID(new ObjectIdentifier("",""));
//                                                spcCheckLot1.setProcessFabID(correspondingFabID);
//                                                spcCheckLot1.setProcessObjrefPO(aCheckPO.getPrimaryKey());
//                                                spcCheckLot1.setRequestOtherFabFlag(requestOtherFabFlag);
//                                                spcCheckLot1.setDcDefID(spcInput.getDcDefID());
//                                                spcCheckLot1.setDcSpecID(strDCDef.get(m).getDataCollectionSpecificationID());
//                                            }
//                                        }
//                                        //-------------------------------
//                                        // Chamber and Recipe Hold Action
//                                        //-------------------------------
//                                        if (StringUtils.equals(BizConstant.SP_ACTION_DONE,spcOutput.getChamberHoldActions().get(chamCnt).getChamberRecipeHoldAction())){
//                                            log.info("ChamberHoldAction is Done!");
//                                            //------------------------------------------
//                                            // Check Chamber and Action Code duplication
//                                            // Because EQP has some Chambers.
//                                            //-------------------------------------------
//                                            boolean bDupricateFlag = false;
//                                            for (int i = 0; i < totalCount; i++) {
//                                                int spcActCodeLen = ArrayUtils.getSize(spcCheckReqResult.getSpcCheckLot().get(i).getSpcActionCode());
//                                                for (int j = 0; j < spcActCodeLen; j++) {
//                                                    if (ObjectUtils.equalsWithValue(spcCheckLot.getProcessChamberID(),spcOutput.getChamberHoldActions().get(chamCnt).getChamberID())
//                                                            && StringUtils.equals(BizConstant.SP_ACTIONCODE_CHAMBERANDRECIPEHOLD,spcCheckLot.getSpcActionCode().get(j))){
//                                                        log.info("Duplicate record exist.");
//                                                        bDupricateFlag = true;
//                                                        break;
//                                                    }
//                                                }
//                                                if (BooleanUtils.isTrue(bDupricateFlag)){
//                                                    break;
//                                                }
//                                            }
//                                            //-------------------------------------
//                                            // Set ChemberID and Action Code
//                                            //-------------------------------------
//                                            if (BooleanUtils.isFalse(bDupricateFlag)){
//                                                spcCheckLot.getSpcActionCode().add(actionCnt,BizConstant.SP_ACTIONCODE_CHAMBERANDRECIPEHOLD);
//                                                chamberHoldActionExistFlag = true;
//                                                int iLen = ArrayUtils.getSize(spcOutput.getItemResults());
//                                                spcCheckLot.getSpcResult().setSpcDcItem(new ArrayList<>());
//                                                for (int i = 0; i < iLen; i++) {
//                                                    Infos.SpcDcItemAndChart spcDcItemAndChart = new Infos.SpcDcItemAndChart();
//                                                    spcCheckLot.getSpcResult().getSpcDcItem().add(k,spcDcItemAndChart);
//                                                    spcDcItemAndChart.setDataCollectionItemName(spcOutput.getItemResults().get(i).getDataItemName());
//                                                    int jLen = ArrayUtils.getSize(spcOutput.getItemResults().get(i).getChartResults());
//                                                    spcCheckLot.getSpcResult().getSpcDcItem().get(k).setSpcChartList(new ArrayList<>());
//                                                    for (int j = 0; j < jLen; j++) {
//                                                        Infos.SpcChart spcChart = new Infos.SpcChart();
//                                                        spcCheckLot.getSpcResult().getSpcDcItem().get(k).getSpcChartList().add(j,spcChart);
//                                                        spcChart.setChartGroupID(spcOutput.getItemResults().get(i).getChartResults().get(j).getChartGroupID());
//                                                        spcChart.setChartID(spcOutput.getItemResults().get(i).getChartResults().get(j).getChartID());
//                                                        spcChart.setChartType(spcOutput.getItemResults().get(i).getChartResults().get(j).getChartType());
//                                                        spcChart.setChartOwnerMailAddress(spcOutput.getItemResults().get(i).getChartResults().get(j).getChartOwnerMailAddress());
//                                                        spcChart.setChartSubOwnerMailAddress(spcOutput.getItemResults().get(i).getChartResults().get(j).getChartSubOwnerMailAddresses());
//                                                        int mLen = ArrayUtils.getSize(spcOutput.getItemResults().get(i).getChartResults().get(j).getReturnCodes());
//                                                        spcCheckLot.getSpcResult().getSpcDcItem().get(k).getSpcChartList().get(j).setSpcReturnCodeList(new ArrayList<>());
//                                                        for (int mm = 0; mm < mLen; mm++) {
//                                                            Infos.SpcReturnCode spcReturnCode = new Infos.SpcReturnCode();
//                                                            spcCheckLot.getSpcResult().getSpcDcItem().get(k).getSpcChartList().get(j).getSpcReturnCodeList().add(mm,spcReturnCode);
//                                                            spcReturnCode.setReturnCodeStatus(spcOutput.getItemResults().get(i).getChartResults().get(j).getReturnCodes().get(mm).getReturnCodeStatus());
//                                                            spcReturnCode.setRule(spcOutput.getItemResults().get(i).getChartResults().get(j).getReturnCodes().get(mm).getRule());
//                                                        }
//                                                    }
//                                                    k++;
//                                                }
//                                                k = 0;
//                                                actionCnt = 0;
//                                                totalCount++;
//                                                Infos.SpcCheckLot spcCheckLot1 = new Infos.SpcCheckLot();
//                                                spcCheckReqResult.getSpcCheckLot().add(totalCount,spcCheckLot1);
//                                                spcCheckLot1.setLotID(spcInput.getLotID());
//                                                spcCheckLot1.setProcessEquipmentID(spcInput.getProcessEquipmentID());
//                                                spcCheckLot1.setProcessMachineRecipeID(spcInput.getProcessMainProcessDefinitionID());
//                                                spcCheckLot1.setProcessRouteID(spcInput.getProcessMainProcessDefinitionID());
//                                                spcCheckLot1.setProductID(ObjectIdentifier.build(spcInput.getProductID().getValue(),aProdSpec.getPrimaryKey()));
//                                                spcCheckLot1.setProcessOperationNumber(spcInput.getProcessOperationNumber());
//                                                spcCheckLot1.setProcessChamberID(new ObjectIdentifier("",""));
//                                                spcCheckLot1.setProcessFabID(correspondingFabID);
//                                                spcCheckLot1.setProcessObjrefPO(aCheckPO.getPrimaryKey());
//                                                spcCheckLot1.setRequestOtherFabFlag(requestOtherFabFlag);
//                                                spcCheckLot1.setDcDefID(spcInput.getDcDefID());
//                                                spcCheckLot1.setDcSpecID(strDCDef.get(m).getDataCollectionSpecificationID());
//                                            }
//                                        }
//                                    }
//                                }
//                            }
//                            int nILen = ArrayUtils.getSize(spcOutput.getItemResults());
//                            for (int i = 0; i < nILen; i++) {
//                                Infos.SpcDcItemAndChart spcDcItemAndChart = new Infos.SpcDcItemAndChart();
//                                spcCheckLot.getSpcResult().getSpcDcItem().add(spcDcItemAndChart);
//                                spcDcItemAndChart.setDataCollectionItemName(spcOutput.getItemResults().get(i).getDataItemName());
//                                int nJLen = ArrayUtils.getSize(spcOutput.getItemResults().get(i).getChartResults());
//                                for (int j = 0; j < nJLen; j++) {
//                                    Infos.SpcChart spcChart = new Infos.SpcChart();
//                                    spcCheckLot.getSpcResult().getSpcDcItem().get(k).getSpcChartList().add(j,spcChart);
//                                    spcChart.setChartGroupID(spcOutput.getItemResults().get(i).getChartResults().get(j).getChartGroupID());
//                                    spcChart.setChartID(spcOutput.getItemResults().get(i).getChartResults().get(j).getChartID());
//                                    spcChart.setChartType(spcOutput.getItemResults().get(i).getChartResults().get(j).getChartType());
//
//                                    int nMLen = ArrayUtils.getSize(spcOutput.getItemResults().get(i).getChartResults().get(j).getReturnCodes());
//                                    for (int mm = 0; m < nMLen; m++) {
//                                        Infos.SpcReturnCode spcReturnCode = new Infos.SpcReturnCode();
//                                        spcCheckLot.getSpcResult().getSpcDcItem().get(k).getSpcChartList().get(j).getSpcReturnCodeList().add(mm,spcReturnCode);
//                                        spcReturnCode.setReturnCodeStatus(spcOutput.getItemResults().get(i).getChartResults().get(j).getReturnCodes().get(mm).getReturnCodeStatus());
//                                        spcReturnCode.setRule(spcOutput.getItemResults().get(i).getChartResults().get(j).getReturnCodes().get(mm).getRule());
//                                        spcCheckLot.getSpcResult().getSpcDcItem().get(k).getSpcChartList().get(j).setChartOwnerMailAddress(spcOutput.getItemResults().get(i).getChartResults().get(j).getChartOwnerMailAddress());
//                                        spcCheckLot.getSpcResult().getSpcDcItem().get(k).getSpcChartList().get(j).setChartSubOwnerMailAddress(spcOutput.getItemResults().get(i).getChartResults().get(j).getChartSubOwnerMailAddresses());
//                                    }
//                                }
//                                k++;
//                            }
//                            totalCount++;
//                        }
//                    }
//                }
//            }
//        }
//        return out;

        /**
         * description:
         * change history: version 0.1 qiandao mes - spc integration update
         * date             defect#             person             comments
         * ---------------------------------------------------------------------------------------------------------------------
         * 2021/1/5                               Neyo                create file
         *
         * @author: Neyo
         * @date: 2021/1/5 18:47
         * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
         */
        Outputs.ObjSPCMgrSendSPCCheckReqOut out = new Outputs.ObjSPCMgrSendSPCCheckReqOut();

        /*------------------------------------------------------------------------*/
        /*                                                                        */
        /*   Set Output Values from Input Parameters without any changes          */
        /*                                                                        */
        /*------------------------------------------------------------------------*/
        log.info("in param equipmentID = {}", ObjectIdentifier.fetchValue(equipmentID));
        log.info("in param controlJobID = {}", ObjectIdentifier.fetchValue(controlJobID));
        int totalCount = 0;
        Results.SPCCheckReqResult spcCheckReqResult = new Results.SPCCheckReqResult();
        List<Infos.SpcIFParm> spcIFParmList = new ArrayList<>();
        out.setSpcCheckReqResult(spcCheckReqResult);
        out.setSpcIFParmList(spcIFParmList);
        spcCheckReqResult.setControlJobID(controlJobID);
        spcCheckReqResult.setEquipmentID(equipmentID);
        List<Infos.SpcCheckLot> spcCheckLots = new ArrayList<>();
        spcCheckReqResult.setSpcCheckLot(spcCheckLots);

        /*--------------------------------------------------*/
        /*                                                  */
        /*  Bonding Map Condition Check for Wafer Stacking  */
        /*                                                  */
        /*--------------------------------------------------*/
        // 【step-1】 - bondingGroup_infoByEqp_GetDR
        log.debug("calling bondingGroupInfoByEqpGetDR()");
        Outputs.ObjBondingGroupInfoByEqpGetDROut objBondingGroupInfoByEqpGetDROut = bondingGroupMethod.bondingGroupInfoByEqpGetDR(objCommon, equipmentID,
                controlJobID, true);
        int topLotLen = CimArrayUtils.getSize(objBondingGroupInfoByEqpGetDROut.getTopLotIDSeq());
        log.info("topLotLen:{}", topLotLen);
        int cassetteLen = CimArrayUtils.getSize(strStartCassetteList);
        for (int cCnt = 0; cCnt < cassetteLen; cCnt++) {
            int lotLen = CimArrayUtils.getSize(strStartCassetteList.get(cCnt).getLotInCassetteList());
            for (int lCnt = 0; lCnt < lotLen; lCnt++) {
                /*---------------------------------*/
                /*   Omit Not OperationStart Lot   */
                /*---------------------------------*/
                Boolean operationStartFlag = strStartCassetteList.get(cCnt).getLotInCassetteList().get(lCnt).getMoveInFlag();
                if (CimBooleanUtils.isFalse(operationStartFlag)) {
                    log.info("operationStartFlag  == FALSE");
                    continue;
                }
                boolean isTopLot = false;
                for (int lotCnt = 0; lotCnt < topLotLen; lotCnt++) {
                    if (ObjectIdentifier.equalsWithValue(objBondingGroupInfoByEqpGetDROut.getTopLotIDSeq().get(lotCnt),
                            strStartCassetteList.get(cCnt).getLotInCassetteList().get(lCnt).getLotID())) {
                        log.info("isTopLot:{}", lotCnt);
                        isTopLot = true;
                        break;
                    }
                }
                if (CimBooleanUtils.isTrue(isTopLot)) {
                    log.info("Lot is Top Lot:{}", ObjectIdentifier.fetchValue(strStartCassetteList.get(cCnt).getLotInCassetteList().get(lCnt).getLotID()));
                    continue;
                }
                /*------------------------------------------------------------------------*/
                /*                                                                        */
                /*   Set Input Values for doSpcCheck() - 1                                */
                /*                                                                        */
                /*------------------------------------------------------------------------*/
                Inputs.SpcInput spcInput = new Inputs.SpcInput();
                Inputs.SpcInput tmpSpcInput = new Inputs.SpcInput();
                Outputs.SpcOutput spcOutput = null;
                List<Infos.DataCollectionInfo> strDCDef = strStartCassetteList.get(cCnt).getLotInCassetteList().get(lCnt).getStartRecipe().getDcDefList();
                //-----------------------
                //   set requestUserID
                //-----------------------
                spcInput.setRequestUserID(objCommon.getUser().getUserID());

                //---------------
                //   set lotID
                //---------------
                spcInput.setLotID(strStartCassetteList.get(cCnt).getLotInCassetteList().get(lCnt).getLotID());

                //---------------------------------------------
                //   set mainProcessDefinitionID & productID
                //---------------------------------------------
                CimLot aLot = baseCoreFactory.getBO(CimLot.class, spcInput.getLotID());
                Validations.check(null == aLot, retCodeConfig.getNotFoundLot());

                //qiandao-dev mes-spc integration add start
                spcInput.setLotType(aLot.getLotType());
                spcInput.setSubLotType(aLot.getSubLotType());
                Infos.OcapInfo ocapInfo = ocapMethod.ocapInformationGetByLotID(ObjectIdentifier.build(aLot.getIdentifier(), aLot.getPrimaryKey()));
                if (null != ocapInfo && CimStringUtils.isNotEmpty(ocapInfo.getOcapNo())){
                    if (CimBooleanUtils.isFalse(ocapInfo.getAddMeasureFlag()) && CimBooleanUtils.isTrue(ocapInfo.getReMeasureFlag())){
                        spcInput.setMeasureType(ocapInfo.getOcapNo()+":"+BizConstant.OCAP_RE_MEASURE);
                    }else if(CimBooleanUtils.isTrue(ocapInfo.getAddMeasureFlag())){
                        spcInput.setMeasureType(ocapInfo.getOcapNo()+":"+BizConstant.OCAP_ADD_MEASURE);
                    }
                }
                //qiandao-dev mes-spc integration add end

                //【step2】lot_CheckConditionForPO
                ProcessOperation aTmpProcessOperation = null;
                CimProcessOperation aPO = null;
                CimProcessOperation aCheckPO = null;
                Boolean lotCheckConditionForPOOut = lotMethod.lotCheckConditionForPO(objCommon, spcInput.getLotID());

                if (CimBooleanUtils.isTrue(lotCheckConditionForPOOut)) {
                    //--------------------------------------------------------------------------
                    // Get PO from Current Operation.
                    //--------------------------------------------------------------------------
                    log.info("Get PO from the current Operation.");
                    aTmpProcessOperation = aLot.getProcessOperation();
                } else {
                    //--------------------------------------------------------------------------
                    // Get PO from Previous Operation.
                    //--------------------------------------------------------------------------
                    log.info("Get PO from the previous Operation.");
                    aTmpProcessOperation = aLot.getPreviousProcessOperation();
                }
                aPO = (CimProcessOperation) aTmpProcessOperation;
                if (null == aPO) {
                    continue;
                }
                aCheckPO = aPO;
                CimProcessDefinition aMainPD = aPO.getMainProcessDefinition();
                Validations.check(null == aMainPD, retCodeConfig.getNotFoundMainRoute());
                spcInput.setProcessFlowID(ObjectIdentifier.build(aMainPD.getIdentifier(), aMainPD.getPrimaryKey()));
                //qiandao-dev mes-spc integration add start (route[modulePD])
                CimProcessDefinition measurementRoute = aPO.getModuleProcessDefinition();
                Validations.check(null == measurementRoute,retCodeConfig.getNotFoundProcessDefinition());
                spcInput.setRoute(ObjectIdentifier.build(measurementRoute.getIdentifier(),measurementRoute.getPrimaryKey()));
                //qiandao-dev mes-spc integration add end (route[modulePD])
                CimProductSpecification aProdSpec = aLot.getProductSpecification();
                if (null == aProdSpec) {
                    continue;
                }
                spcInput.setProductID(ObjectIdentifier.buildWithValue(aProdSpec.getIdentifier()));
                //qiandao-dev mes-spc integration add start
                CimProductGroup productGroup = aProdSpec.getProductGroup();
                Validations.check(null == productGroup, retCodeConfig.getNotFoundProductGroup(), "");
                spcInput.setProductGroupID(ObjectIdentifier.build(productGroup.getIdentifier(),productGroup.getPrimaryKey()));

                CimTechnology technology = productGroup.getTechnology();
                Validations.check(null == technology,retCodeConfig.getNotFoundTechnology(),"*****");
                spcInput.setTechnologyID(ObjectIdentifier.build(technology.getIdentifier(),technology.getPrimaryKey()));
                //qiandao-dev mes-spc integration add end

                int dcDefLen = CimArrayUtils.getSize(strDCDef);
                log.info("strDCDef size :{}", dcDefLen);
                if (dcDefLen < 1) {
                    log.info("Not need SPCCheck.");
                    continue;
                }
                //----------------------------------------------------------------------------------------
                //   set Corresponding Operation Number
                //----------------------------------------------------------------------------------------
                CimProcessFlowContext aPFX = null;
                int fpcListNum = 0;
                String singleCorrOpeFpc = null;
                int coFPCLen = 0;
                int foundFPCCnt = 0;
                int waferCntFromStartCassette = 0;
                int poLen = 0;
                List<CimProcessOperation> aCorrespondingPOs = new ArrayList<>();
                CimProcessOperation aCorrespondingPO = null;
                Boolean correspondingPOFlag = false;
                List<Infos.FPCInfo> fpcInfoGetDROut = null;

                if (CimStringUtils.equals(BizConstant.SP_DCDEF_COLLECTION_MEASUREMENT, strDCDef.get(0).getDataCollectionType())) {
                    log.info("dataCollectionType == SP_DCDEF_COLLECTION_MEASUREMENT");
                    aPFX = aLot.getProcessFlowContext();
                    Validations.check(null == aPFX, new OmCode(retCodeConfig.getNotFoundPfx(), ObjectIdentifier.fetchValue(spcInput.getLotID())));
                    List<String> dummyFPCIDs = new ArrayList<>();
                    ObjectIdentifier dummyID = new ObjectIdentifier();
                    String dummy = null;
                    String opeNum = aPO.getOperationNumber();

                    //【step-3】FPC_info_GetDR__101
                    Inputs.ObjFPCInfoGetDRIn objFPCInfoGetDRIn = new Inputs.ObjFPCInfoGetDRIn();
                    objFPCInfoGetDRIn.setLotID(spcInput.getLotID());
                    objFPCInfoGetDRIn.setLotFamilyID(dummyID);
                    objFPCInfoGetDRIn.setMainPDID(spcInput.getProcessFlowID());
                    objFPCInfoGetDRIn.setMainOperNo(opeNum);
                    objFPCInfoGetDRIn.setOrgMainPDID(dummyID);
                    objFPCInfoGetDRIn.setOrgOperNo(dummy);
                    objFPCInfoGetDRIn.setSubMainPDID(dummyID);
                    objFPCInfoGetDRIn.setSubOperNo(dummy);
                    objFPCInfoGetDRIn.setFPCIDs(dummyFPCIDs);
                    objFPCInfoGetDRIn.setEquipmentID(equipmentID);
                    objFPCInfoGetDRIn.setWaferIDInfoGetFlag(true);
                    objFPCInfoGetDRIn.setRecipeParmInfoGetFlag(false);
                    objFPCInfoGetDRIn.setReticleInfoGetFlag(false);
                    objFPCInfoGetDRIn.setDcSpecItemInfoGetFlag(true);
                    //【step-4】 - FPC_info_GetDR__101
                    fpcInfoGetDROut = fpcMethod.fpcInfoGetDR(objCommon, objFPCInfoGetDRIn);
                    fpcListNum = CimArrayUtils.getSize(fpcInfoGetDROut);
                    if (0 != fpcListNum) {
                        waferCntFromStartCassette = CimArrayUtils.getSize(strStartCassetteList.get(cCnt).getLotInCassetteList().get(lCnt).getLotWaferList());
                        for (int numFPC = 0; numFPC < fpcListNum; numFPC++) {
                            log.info("wafer key for FPC :{}", ObjectIdentifier.fetchValue(fpcInfoGetDROut.get(numFPC).getLotWaferInfoList().get(0).getWaferID()));
                            for (int inParaWCnt = 0; inParaWCnt < waferCntFromStartCassette; inParaWCnt++) {
                                if (ObjectIdentifier.equalsWithValue(fpcInfoGetDROut.get(numFPC).getLotWaferInfoList().get(0).getWaferID(),
                                        strStartCassetteList.get(cCnt).getLotInCassetteList().get(lCnt).getLotWaferList().get(inParaWCnt).getWaferID())) {
                                    coFPCLen = CimArrayUtils.getSize(fpcInfoGetDROut.get(numFPC).getCorrespondingOperationInfoList());
                                    singleCorrOpeFpc = fpcInfoGetDROut.get(numFPC).getCorrespondOperationNumber();
                                    log.info("correspondingOperationInfo size : {} from FPC", coFPCLen);
                                    foundFPCCnt = numFPC;
                                    break;
                                }
                            }
                        }
                        log.info("singleCorrOpeFPC : {}", singleCorrOpeFpc);
                        if (coFPCLen == 0 && CimStringUtils.isEmpty(singleCorrOpeFpc)) {
                            aCorrespondingPOs = aPFX.getCorrespondingProcessOperationsFor(aPO);
                            poLen = CimArrayUtils.getSize(aCorrespondingPOs);
                        } else if (0 < coFPCLen) {
                            poLen = coFPCLen;
                        } else {
                            poLen = 1;
                        }
                    } else {
                        aCorrespondingPOs = aPFX.getCorrespondingProcessOperationsFor(aPO);
                        poLen = CimArrayUtils.getSize(aCorrespondingPOs);
                    }
                    log.info("aCorrespondingPOs size = {}", poLen);
                    if (poLen > 0) {
                        log.info("aCorrespondingPOs size > 0 ...");
                        correspondingPOFlag = true;
                    } else {
                        log.info("aCorrespondingPOs size = 0 ...");
                        CimMonitorGroup monGrp = aLot.getControlMonitorGroup();
                        if (null != monGrp) {
                            List<ProductDTO.MonitoredLot> monitoredLots = monGrp.allLots();
                            if (CimArrayUtils.getSize(monitoredLots) > 0) {
                                log.info("monitoredLots size > 0 ...");
                                if (CimStringUtils.isNotEmpty(monitoredLots.get(0).getProcessOperation())) {
                                    log.info("monitoredLots[0]'s PO is {} ...",monitoredLots.get(0).getProcessOperation());
                                    poLen = 1;
                                    aCorrespondingPO = baseCoreFactory.getBO(CimProcessOperation.class,monitoredLots.get(0).getProcessOperation());
                                }else {
                                    log.info("monitoredLots[0]'s PO is null...");
                                    continue;
                                }
                            }else {
                                log.info("monitoredLots size is 0...");
                                continue;
                            }
                        }else {
                            log.info("monGrp is null...");
                            continue;
                        }
                    }
                }else {
                    log.info("dataCollectionType != SP_DCDEF_COLLECTION_MEASUREMENT");
                    poLen = 1;
                }
                //------------------------
                //   set collectionType
                //------------------------
                spcInput.setEdcType(strDCDef.get(0).getDataCollectionType());
                int dcLen = CimArrayUtils.getSize(strDCDef);
                int actionCnt = 0;
                int k = 0;
                log.info("strDCDef size = {}",dcLen);
                for (int m = 0; m < dcLen; m++) {
                    //-----------------
                    //   set dcDefID
                    //-----------------
                    spcInput.setEdcID(strDCDef.get(m).getDataCollectionDefinitionID());
                    //----------------------------------------------------------------------------------------
                    //   Store the base spcInput
                    //----------------------------------------------------------------------------------------
                    tmpSpcInput = spcInput;
                    //----------------------------------------------------------------------------------------
                    //   Get wafer list of the target lot
                    //----------------------------------------------------------------------------------------
                    List<ObjectIdentifier> pWaferIDs = new ArrayList<>();
                    List<Infos.LotWaferAttributes> lotMaterialsGetWafersOut = null;
                    List<ObjectIdentifier> waferIDList = null;
                    if (CimStringUtils.equals(TransactionIDEnum.PARTIAL_OPERATION_COMP_WITH_DATA_REQ.getValue(),objCommon.getTransactionID())
                            || CimStringUtils.equals(TransactionIDEnum.PARTIAL_OPERATION_COMP_FOR_INTERNAL_BUFFER_REQ.getValue(),objCommon.getTransactionID())){
                        //Call METHOD lot_materials_GetWafers
                        log.info("call lot_materials_GetWafers()");
                        //【step-5】 lot_materials_GetWafers
                        lotMaterialsGetWafersOut = lotMethod.lotMaterialsGetWafers(objCommon, spcInput.getLotID());
                    }else {
                        //【step-6】lot_waferIDList_GetDR
                        Inputs.ObjLotWaferIDListGetDRIn objLotWaferIDListGetDRIn = new Inputs.ObjLotWaferIDListGetDRIn();
                        objLotWaferIDListGetDRIn.setLotID(spcInput.getLotID());
                        objLotWaferIDListGetDRIn.setScrapCheckFlag(true);
                        waferIDList = lotMethod.lotWaferIDListGetDR(objCommon, objLotWaferIDListGetDRIn);
                        pWaferIDs = waferIDList;
                    }
                    List<ObjectIdentifier> curLotWaferIDs = pWaferIDs;
                    int nCurPOWfrLen = CimArrayUtils.getSize(curLotWaferIDs);
                    log.info("nCurPOWfrLen : {}",nCurPOWfrLen);
                    //----------------------------------------------------------------------------------------
                    //   set processEquipmentID & processRecipeID & processRouteID & processOperaitonNumber
                    //----------------------------------------------------------------------------------------
                    for (int pCnt = 0; pCnt < poLen; pCnt++) {
                        // Flag for where processed wafer information is stored in PO or not
                        Boolean bCorPOWaferInfoExist = false;
                        List<String> strPOObjs = new ArrayList<>();
                        List<Integer> waferCnt = new ArrayList<>();
                        List<Infos.WaferIDByChamber> strWaferIDSeq = new ArrayList<>();
                        strWaferIDSeq.add(new Infos.WaferIDByChamber());
                        strWaferIDSeq.get(0).setWaferIDs(new ArrayList<>(nCurPOWfrLen));
                        waferCnt.add(0);
                        String corrOpeNum = null;
                        if (CimStringUtils.equals(BizConstant.SP_DCDEF_COLLECTION_MEASUREMENT,strDCDef.get(0).getDataCollectionType())){
                            log.info("strDCDef[0].dataCollectionType==SP_DCDEF_COLLECTION_MEASUREMENT");
                            if (CimBooleanUtils.isTrue(correspondingPOFlag) && (coFPCLen != 0 || CimStringUtils.isNotEmpty(singleCorrOpeFpc))){
                                log.info("correspondingPOFlag==TRUE and FPC has updated corresponding Operation.");
                                if (coFPCLen > 0){
                                    corrOpeNum = fpcInfoGetDROut.get(foundFPCCnt).getCorrespondingOperationInfoList().get(pCnt).getCorrespondingOperationNumber();
                                    aCorrespondingPO = aPFX.findProcessOperationForOperationNumberBefore(corrOpeNum);
                                }else {
                                    aCorrespondingPO = aPFX.findProcessOperationForOperationNumberBefore(singleCorrOpeFpc);
                                }
                                Validations.check(null == aCorrespondingPO, retCodeConfig.getNotFoundProcessOperation(),ObjectIdentifier.fetchValue(spcInput.getLotID()));
                            }else {
                                //--------------------------------------------------------------------------
                                // Get Corresponding PO list from process wafer
                                //--------------------------------------------------------------------------
                                //--------------------------------------------------------------------------
                                // Get process wafers of corresponding PO
                                //--------------------------------------------------------------------------
                                if (CimBooleanUtils.isTrue(correspondingPOFlag)){
                                    log.info("correspondingPOFlag==TRUE");
                                    aCorrespondingPO = aCorrespondingPOs.get(pCnt);
                                }
                                if (null == aCorrespondingPO){
                                    log.info("aCorrespondingPO is null");
                                    continue;
                                }
                            }
                            // Set default PO obj to strPOObjs[0]
                            strPOObjs.add(aCorrespondingPO.getPrimaryKey());//po obj ? id?
                            int nPoCnt = 1 ;
                            log.info("strPOObjs[0] = {}",strPOObjs.get(0));
                            List<ProcessDTO.PosProcessWafer> strCorrespondingProcessWaferSeq = aCorrespondingPO.getProcessWafers();
                            int nCorPOWaferLen = CimArrayUtils.getSize(strCorrespondingProcessWaferSeq);
                            if (nCorPOWaferLen > 0){
                                log.info("nCorPOWaferLen > 0");
                                //Processed wafer information is stored in FRPO_SMPL
                                bCorPOWaferInfoExist = true;
                                CimProcessDefinition aCorrespondingMainPD = aCorrespondingPO.getMainProcessDefinition();
                                Validations.check(null == aCorrespondingMainPD, new OmCode(retCodeConfig.getNotFoundProcessDefinition(), ""));
                                ObjectIdentifier mainPDID = ObjectIdentifier.build(aCorrespondingMainPD.getIdentifier(), aCorrespondingMainPD.getPrimaryKey());
                                log.info("mainPDID: {}",mainPDID.getValue());
                                String correspondOpeNumber = aCorrespondingPO.getOperationNumber();
                                log.info("correspondOpeNumber: {}",correspondOpeNumber);
                                log.info("nCurPOWfrLen: {}",nCurPOWfrLen);
                                log.info("nCorPOWaferLen: {}",nCorPOWaferLen);

                                for (int nCurPOWfrCnt = 0; nCurPOWfrCnt < nCurPOWfrLen; nCurPOWfrCnt++) {
                                    boolean bWaferFoundFlag = false;
                                    log.info("counter nCurPOWfrCnt: {}",nCurPOWfrCnt);
                                    log.info("waferID: {}",ObjectIdentifier.fetchValue(curLotWaferIDs.get(nCurPOWfrCnt)));
                                    for (int nCorPOWfrCnt = 0; nCorPOWfrCnt < nCorPOWaferLen; nCorPOWfrCnt++) {
                                        log.info("counter nCorPOWfrCnt: {}",nCorPOWfrCnt);
                                        if (ObjectIdentifier.equalsWithValue(curLotWaferIDs.get(nCurPOWfrCnt), strCorrespondingProcessWaferSeq.get(nCorPOWfrCnt).getWaferID())){
                                            bWaferFoundFlag = true;
                                            log.info("bWaferFoundFlag = TRUE");
                                            break;
                                        }
                                    }
                                    if (CimBooleanUtils.isTrue(bWaferFoundFlag)){
                                        // This wafer information exist in default corresponding PO
                                        log.info("TRUE == bWaferFoundFlag");
                                        strWaferIDSeq.get(0).getWaferIDs().add(ObjectIdentifier.fetchValue(curLotWaferIDs.get(nCurPOWfrCnt)));
                                        Integer count = waferCnt.get(0);
                                        waferCnt.set(0, count++);
                                    }else {
                                        //--------------------------------------------------------------------------
                                        // Wafer isn't found in process wafers of default PO
                                        // Get PO from
                                        //--------------------------------------------------------------------------
                                        CimProcessOperation aPOtmp = aPO.findProcessOperationForOperationNumberAndWafer(mainPDID.getValue(),
                                                correspondOpeNumber,
                                                curLotWaferIDs.get(nCurPOWfrCnt).getValue());
                                        if (null == aPOtmp){
                                            log.info("aPOtmp is null");
                                            continue;
                                        }
                                        String strTmpPOObj = aPOtmp.getPrimaryKey();
                                        log.info("strTmpPOObj: {}",strTmpPOObj);
                                        boolean bPOFoundFlag = false;
                                        for (k = 0; k < nPoCnt; k++) {
                                            log.info("counter k: {}",k);
                                            if (CimStringUtils.equals(strPOObjs.get(k),strTmpPOObj)){
                                                // PO of wafer was already added in PO list(strPOObjs[]). Add wafer ID to strWaferIDSeq[].
                                                log.info("strPOObjs[k] == strTmpPOObj: {}",strTmpPOObj);
                                                log.info("waferID: {}",curLotWaferIDs.get(nCurPOWfrCnt).getValue());
                                                bPOFoundFlag = true;
                                                strWaferIDSeq.get(k).getWaferIDs().set(waferCnt.get(k),ObjectIdentifier.fetchValue(curLotWaferIDs.get(nCurPOWfrCnt)));
                                                Integer count = waferCnt.get(k);
                                                waferCnt.set(k,count++);
                                                break;
                                            }
                                        }
                                        if (CimBooleanUtils.isFalse(bPOFoundFlag)){
                                            // PO of wafer doesn't exit in PO list(strPOObjs[]). Add PO to strPOObjs[] and wafer ID to strWaferIDSeq[].
                                            log.info("FALSE == bPOFoundFlag");
                                            log.info("nPoCnt: {}",nPoCnt);
                                            strPOObjs.set(nPoCnt,strTmpPOObj);
                                            waferCnt.set(nPoCnt,0);
                                            strWaferIDSeq.get(nPoCnt).getWaferIDs().set(waferCnt.get(nPoCnt), ObjectIdentifier.fetchValue(curLotWaferIDs.get(nCurPOWfrCnt)));
                                            Integer count = waferCnt.get(nPoCnt);
                                            waferCnt.set(nPoCnt,count++);
                                            nPoCnt++;
                                        }
                                    }
                                }
                            }
                            for (int i = 0; i < nPoCnt; i++) {
                                //nothin
                            }
                        }else {
                            strPOObjs.add(0,aPO.getPrimaryKey());
                            log.info("strPOObjs[0]: {}",strPOObjs.get(0));
                        }
                        int nPOLenInCorrPO = CimArrayUtils.getSize(strPOObjs);
                        for (int nCorPOCnt = 0; nCorPOCnt < nPOLenInCorrPO; nCorPOCnt++) {
                            Infos.SpcCheckLot spcCheckLot = new Infos.SpcCheckLot();
                            spcCheckLots.add(spcCheckLot);
                            spcCheckLot.setProductID(ObjectIdentifier.build(spcInput.getProductID().getValue(),aProdSpec.getPrimaryKey()));
                            log.info("strPOObjs[nCorPOCnt]: {}",strPOObjs.get(nCorPOCnt));
                            //----------------------------------------------------------------------------------------
                            //   Initialize spcInput with original spcInput(tmpSpcInput)
                            //----------------------------------------------------------------------------------------
                            spcInput = tmpSpcInput;
                            aCheckPO = baseCoreFactory.getBO(CimProcessOperation.class,strPOObjs.get(nCorPOCnt));

                            //-----------------
                            //   set dcItems
                            //-----------------
                            int len = CimArrayUtils.getSize(strDCDef.get(m).getDcItems());
                            int nItemCnt = 0;
                            List<Infos.SpcDcItem> dcItems = new ArrayList<>();
                            spcInput.setDcItems(dcItems);
                            for (int iCnt = 0; iCnt < len; iCnt++) {
                                Boolean bSetToInput = false;
                                if (CimBooleanUtils.isTrue(bCorPOWaferInfoExist)){
                                    log.info("TRUE == bCorPOWaferInfoExist");
                                    if (!ObjectIdentifier.isEmptyWithValue(strDCDef.get(m).getDcItems().get(iCnt).getWaferID())){
                                        log.info("strDCDef[m].strDCItem[iCnt].measurementType: {}",strDCDef.get(m).getDcItems().get(iCnt).getMeasurementType());
                                        int wfrLen = CimArrayUtils.getSize(strWaferIDSeq.get(nCorPOCnt).getWaferIDs());
                                        for (int wfrCnt = 0; wfrCnt < wfrLen; wfrCnt++) {
                                            if (ObjectIdentifier.equalsWithValue(strDCDef.get(m).getDcItems().get(iCnt).getWaferID(), strWaferIDSeq.get(nCorPOCnt).getWaferIDs().get(wfrCnt))){
                                                bSetToInput = true;
                                                break;
                                            }
                                        }
                                    }else {
                                        if (0 == nCorPOCnt){
                                            log.info("0 == nCorPOCnt");
                                            bSetToInput = true;
                                        }else {
                                            log.info("1 < nCorPOCnt");
                                            bSetToInput = false;
                                        }
                                    }
                                }else {
                                    log.info("else");
                                    bSetToInput = true;
                                }
                                if (bSetToInput){
                                    log.info("TRUE == bSetToInput");
                                    String aString = strDCDef.get(m).getDcItems().get(iCnt).getDataValue();
                                    Infos.SpcDcItem spcDcItem = new Infos.SpcDcItem();
                                    dcItems.add(nItemCnt,spcDcItem);
                                    //qiandao-dev mes-spc integration add start (specLimitUpper/specLimitLower/controlLimitUpper/controlLimitLower)
                                    if (CimArrayUtils.isNotEmpty(strDCDef.get(m).getDcSpecs())){
                                        for (Infos.DataCollectionSpecInfo dcSpecInfo : strDCDef.get(m).getDcSpecs()) {
                                            if (CimStringUtils.isNotEmpty(strDCDef.get(m).getDcItems().get(iCnt).getSpecCheckResult())
                                                    && !CimStringUtils.equalsIn(strDCDef.get(m).getDcItems().get(iCnt).getSpecCheckResult(), "*", "#")) {
                                                if (CimStringUtils.equals(dcSpecInfo.getDataItemName(),strDCDef.get(m).getDcItems().get(iCnt).getDataCollectionItemName())){
                                                    if (CimBooleanUtils.isTrue(dcSpecInfo.getSpecLimitLowerRequired())){
                                                        spcDcItem.setSpecLimitLower(dcSpecInfo.getSpecLimitLower());
                                                    }
                                                    if (CimBooleanUtils.isTrue(dcSpecInfo.getSpecLimitUpperRequired())){
                                                        spcDcItem.setSpecLimitUpper(dcSpecInfo.getSpecLimitUpper());
                                                    }
                                                    if (CimBooleanUtils.isTrue(dcSpecInfo.getControlLimitLowerRequired())){
                                                        spcDcItem.setControlLimitLower(dcSpecInfo.getControlLimitLower());
                                                    }
                                                    if (CimBooleanUtils.isTrue(dcSpecInfo.getControlLimitUpperRequired())){
                                                        spcDcItem.setControlLimitUpper(dcSpecInfo.getControlLimitUpper());
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    //qiandao-dev mes-spc integration add end (specLimitUpper/specLimitLower/controlLimitUpper/controlLimitLower)
                                    spcDcItem.setDataItemName(strDCDef.get(m).getDcItems().get(iCnt).getDataCollectionItemName());
                                    if (CimStringUtils.equals(aString,"*") ||
                                            CimStringUtils.equals(BizConstant.SP_DCDEF_VAL_STRING,strDCDef.get(m).getDcItems().get(iCnt).getDataType())){
                                        spcDcItem.setDataValue("");
                                    }else {
                                        spcDcItem.setDataValue(strDCDef.get(m).getDcItems().get(iCnt).getDataValue());
                                    }
                                    spcDcItem.setWaferID(ObjectIdentifier.fetchValue(strDCDef.get(m).getDcItems().get(iCnt).getWaferID()));
                                    spcDcItem.setWaferPosition(strDCDef.get(m).getDcItems().get(iCnt).getWaferPosition());
                                    spcDcItem.setSitePosition(strDCDef.get(m).getDcItems().get(iCnt).getSitePosition());
                                    if (CimStringUtils.isNotEmpty(strDCDef.get(m).getDcItems().get(iCnt).getTargetValue())){
                                        spcDcItem.setTargetValue(Double.valueOf(strDCDef.get(m).getDcItems().get(iCnt).getTargetValue()));
                                    }else {
                                        spcDcItem.setTargetValue(0.0);
                                    }
                                    spcDcItem.setSpecCheckResult(strDCDef.get(m).getDcItems().get(iCnt).getSpecCheckResult());
                                    nItemCnt++;
                                }
                            }
                            if (null == aCheckPO){
                                log.info("acheckPO is null");
                                continue;
                            }
                            CimMachine aMachine = aCheckPO.getAssignedMachine();
                            CimMachineRecipe aRecipe = aCheckPO.getAssignedMachineRecipe();
                            CimProcessDefinition aProcessMainPD = aCheckPO.getMainProcessDefinition();
                            spcInput.setProcessOperationNumber(aCheckPO.getOperationNumber());
                            spcInput.setProcessTimestamp(CimDateUtils.convertToSpecString(aCheckPO.getActualCompTimeStamp()));
                            spcInput.setMeasurementTimestamp(CimDateUtils.convertToSpecString(objCommon.getTimeStamp().getReportTimeStamp()));

                            CimProcessDefinition aProcessPD = aCheckPO.getProcessDefinition();
                            if (null != aProcessPD){
                                spcInput.setProcessStepID(ObjectIdentifier.build(aProcessPD.getIdentifier(),aProcessPD.getPrimaryKey()));
                                //qiandao-dev mes-spc integration add start
                                ObjectIdentifier processLogicalRecipeID = logicalRecipeMethod.processLogicalRecipeGetDR(objCommon,
                                        ObjectIdentifier.buildWithValue(aProdSpec.getIdentifier()),
                                        ObjectIdentifier.build(aProcessPD.getIdentifier(), aProcessPD.getPrimaryKey()));
                                spcInput.setProcessLogicalRecipeID(processLogicalRecipeID);
                                spcInput.setProcessDepartment(ObjectIdentifier.buildWithValue(aProcessPD.getResponsibleDepartment()));
                                //qiandao-dev mes-spc integration add end
                            }
                            spcCheckLot.setProcessOperationNumber(spcInput.getProcessOperationNumber());
                            /*------------------------------------*/
                            /*   Get FabID of current operation   */
                            /*------------------------------------*/
                            String currentFabID = aPO.getFabID();
                            /*------------------------------------------*/
                            /*   Get FabID of corresponding operation   */
                            /*------------------------------------------*/
                            String correspondingFabID = aCheckPO.getFabID();
                            log.info("FabID(current): {}", currentFabID);
                            log.info("FabID(corrpnd): {}", correspondingFabID);

                            Boolean requestOtherFabFlag = false;
                            List<Infos.InterFabDestinationInfo> objFabInfoOut = null;
                            // Compare the both FabID
                            if (CimStringUtils.isNotEmpty(currentFabID) && CimStringUtils.isNotEmpty(correspondingFabID)
                                    && !CimStringUtils.equals(currentFabID,correspondingFabID)) {
                                requestOtherFabFlag = true;
                                /*----------------------------------------------------*/
                                /*   Get SPCServer information of corresponding Fab   */
                                /*----------------------------------------------------*/
                                //【step-7】 fabInfo_GetDR
                                objFabInfoOut = fabMethod.fabInfoGetDR(objCommon,correspondingFabID);
                            }
                            if (CimBooleanUtils.isFalse(requestOtherFabFlag)){
                                log.info("requestOtherFabFlag is FALSE.");
                                if (null == aMachine){
                                    log.info("aMachine is null");
                                    continue;
                                }
                                if (aRecipe == null){
                                    continue;
                                }
                                Validations.check(null == aProcessMainPD,retCodeConfig.getNotFoundMainRoute());
                                spcInput.setProcessEquipmentID(ObjectIdentifier.build(aMachine.getIdentifier(),aMachine.getPrimaryKey()));
                                spcInput.setProcessRecipeID(ObjectIdentifier.build(aRecipe.getIdentifier(),aRecipe.getPrimaryKey()));
                                //qiandao-dev mes-spc integration add start
                                spcInput.setProcessEquipmentGroup(aMachine.getMachineType());
                                spcInput.setProcessProcessFlowID(ObjectIdentifier.build(aProcessMainPD.getIdentifier(),aProcessMainPD.getPrimaryKey()));
                                CimProcessDefinition processRoute = aCheckPO.getModuleProcessDefinition();
                                Validations.check(null == processRoute,retCodeConfig.getNotFoundProcessDefinition());
                                spcInput.setProcessRoute(ObjectIdentifier.build(processRoute.getIdentifier(),processRoute.getPrimaryKey()));
                                //qiandao-dev mes-spc integration add end
                            }else {
                                log.info("requestOtherFabFlag is TRUE.");
                                // correspondingPO's machine/recipe/mainPD is nil.
                                // there info get by processOperation_Info_GetDR
                                //【step-8】processOperation_Info_GetDR
                                Inputs.ObjProcessOperationInfoGetDrIn param = new Inputs.ObjProcessOperationInfoGetDrIn();
                                param.setPoObj(aCheckPO.getPrimaryKey());
                                Infos.ProcessOperationInfo processOperationInfoOut = processMethod.processOperationInfoGetDr(objCommon, param);
                                spcInput.setProcessEquipmentID(processOperationInfoOut.getAsgnEqpID());
                                CimMachine machine = baseCoreFactory.getBO(CimMachine.class, processOperationInfoOut.getAsgnEqpID());
                                spcInput.setProcessEquipmentGroup(null == machine ? null : machine.getMachineType());
                                spcInput.setProcessRecipeID(processOperationInfoOut.getAsgnRecipeID());
                                spcInput.setProcessProcessFlowID(processOperationInfoOut.getMainPDID());
                                //qiandao-dev mes-spc integration add start
                                spcInput.setProcessRoute(processOperationInfoOut.getModulePDID());
                                //qiandao-dev mes-spc integration add end
                                log.info("eqpID   {}", ObjectIdentifier.fetchValue(spcInput.getProcessEquipmentID()));
                                log.info("recipeID {}", ObjectIdentifier.fetchValue(spcInput.getProcessRecipeID()));
                                log.info("mainPDID {}", ObjectIdentifier.fetchValue(spcInput.getProcessFlowID()));
                            }
                            //qiandao-dev mes-spc integration add start
                            String sql = "SELECT STAGE_ID,STAGE_RKEY FROM OMPRF_ROUTESEQ WHERE REFKEY = ?1 AND LINK_KEY =?2 ";
                            Object[] processStageArray = cimJpaRepository.queryOne(sql,aCheckPO.getMainProcessFlow().getPrimaryKey(), aCheckPO.getModuleNumber());
                            Validations.check(CimObjectUtils.isEmpty(processStageArray),retCodeConfig.getNotFoundPfForDurable());
                            String currentStageID = String.valueOf(processStageArray[0]) ;
                            String currentStageObj = String.valueOf(processStageArray[1]);
                            spcInput.setProcessStage(ObjectIdentifier.build(currentStageID,currentStageObj));
                            //qiandao-dev mes-spc integration add end

                            spcCheckLot.setLotID(spcInput.getLotID());
                            spcCheckLot.setProcessEquipmentID(spcInput.getProcessEquipmentID());
                            spcCheckLot.setProcessMachineRecipeID(spcInput.getProcessRecipeID());
                            spcCheckLot.setProcessRouteID(spcInput.getProcessProcessFlowID());
                            spcCheckLot.setProcessFabID(correspondingFabID);
                            spcCheckLot.setProcessObjrefPO(aCheckPO.getPrimaryKey());
                            spcCheckLot.setRequestOtherFabFlag(requestOtherFabFlag);
                            spcCheckLot.setDcDefID(spcInput.getEdcID());
                            spcCheckLot.setDcSpecID(strDCDef.get(m).getDataCollectionSpecificationID());
                            spcInput.setProcessFabID(ObjectIdentifier.buildWithValue(correspondingFabID));
                            if (CimStringUtils.isEmpty(correspondingFabID)){
                                spcInput.setProcessFabID(ObjectIdentifier.buildWithValue("MEMS"));
                            }

                            /*------------------------------------------------------------------------*/
                            /*                                                                        */
                            /*   Check SPC Check is Required or Not                                   */
                            /*                                                                        */
                            /*------------------------------------------------------------------------*/
                            String hFRSPC_MEQP_ID = ObjectIdentifier.fetchValue(spcInput.getProcessEquipmentID());
                            String hFRSPC_MRECIPE_ID = ObjectIdentifier.fetchValue(spcInput.getProcessRecipeID());
                            String hFRSPC_PEQP_ID = ObjectIdentifier.fetchValue(spcInput.getProcessEquipmentID());
                            String hFRSPC_PRECIPE_ID = ObjectIdentifier.fetchValue(spcInput.getProcessRecipeID());

                            // Make searching criteria of process recipe ID with wildcard for recipe version.
                            String  hFRSPC_PRECIPE_ID_WITH_WILDCARD = null;
                            int orgLen = CimStringUtils.length(hFRSPC_PRECIPE_ID);
                            int index = 0;
                            for (int i = orgLen - 1; i > 0; i--){
                                if (hFRSPC_PRECIPE_ID.charAt(i) == '.'){
                                    index = i;
                                    break;
                                }
                            }
                            hFRSPC_PRECIPE_ID_WITH_WILDCARD = hFRSPC_PRECIPE_ID.substring(0,index);
                            hFRSPC_PRECIPE_ID_WITH_WILDCARD = hFRSPC_PRECIPE_ID_WITH_WILDCARD.concat(".*");

                            log.info( "processEquipmentID {}", hFRSPC_MEQP_ID   );
                            log.info( "processRecipeID   {}", hFRSPC_MRECIPE_ID);
                            log.info( "processRecipeID with wildcard for version {}", hFRSPC_PRECIPE_ID_WITH_WILDCARD);

                            spcInput.setEdcType(strDCDef.get(0).getDataCollectionType());
                            // TODO: 2021/1/16 confrim doSPCCheck not by  FRSPC_M and FRSPC_P. later designed a spcFlag in logicalRecipe
                            int hCount = 0;
                            if (CimStringUtils.equals(BizConstant.SP_DCDEF_COLLECTION_MEASUREMENT,strDCDef.get(0).getDataCollectionType())){
                                log.info("EXEC SQL SELECT FROM FRSPC_M");
                                List<Object[]> queryFirst = cimJpaRepository.query("SELECT * FROM OMSPC_M WHERE EQP_ID = ?1", hFRSPC_PEQP_ID);
                                hCount = CimArrayUtils.getSize(queryFirst);
                            }else {
                                log.info("EXEC SQL SELECT FROM FRSPC_P");
                                List<Object[]> querySecond = cimJpaRepository.query("SELECT * FROM OMSPC_P WHERE EQP_ID = ?1", hFRSPC_PEQP_ID);
                                hCount = CimArrayUtils.getSize(querySecond);
                            }
                            if (hCount == 0){
                                log.info("hCount == 0");
                                if (CimStringUtils.isEmpty(spcCheckLot.getSpcCheckResult())){
                                    spcCheckLot.setSpcCheckResult("N");
                                }
                                continue;
                            }
                            /*------------------------------------------------------------------------*/
                            /*                                                                        */
                            /*   Set Input Values for doSpcCheck() - 2 (continued from - 1)           */
                            /*                                                                        */
                            /*------------------------------------------------------------------------*/

                            //-------------------------
                            //   set operationNumber
                            //-------------------------
                            spcInput.setOperationNumber(aPO.getOperationNumber());

                            //------------------------
                            //   set operationName
                            //------------------------
                            spcInput.setOperationName(aPO.getName());

                            //---------------------
                            //   set ownerUserID
                            //---------------------
                            CimPerson aLotOwner = aLot.getLotOwner();
                            if (null == aLotOwner){
                                log.info("lot owner is null");
                                spcInput.setOwnerUserID(ObjectIdentifier.build(BizConstant.SP_PPTSVCMGR_PERSON,""));
                            }else {
                                log.info("lot owner is not null");
                                spcInput.setOwnerUserID(ObjectIdentifier.build(aLotOwner.getIdentifier(),aLotOwner.getPrimaryKey()));
                            }

                            //------------------------
                            //   set collectionType
                            //------------------------
                            boolean lotHoldActionExistFlag = false;
                            boolean processHoldActionExistFlag = false;
                            boolean equipmentHoldActionExistFlag = false;
                            boolean recipeHoldActionExistFlag = false;
                            boolean routeHoldActionExistFlag = false;
                            boolean reworkBranchActionExistFlag = false;
                            boolean mailSendActionExistFlag = false;
                            boolean bankMoveActionExistFlag = false;
                            boolean equipmentAndRecipeHoldActionExistFlag  = false;
                            Boolean processAndEquipmentHoldActionExistFlag = false;
                            boolean chamberHoldActionExistFlag = false;
                            Boolean chamberRecipeHoldActionExistFlag = false;

                            //-------------------------------------------------------------------------------
                            //  Get ChamberIDs and WaferIDs from PO.
                            //  At first, Collect ChamberIDs to spcInput.waferIDByChambers[chamCnt].procChamber
                            //  And then collect WaferIDs which processed at each Chamber to
                            //  spcInput.waferIDByChambers[chamCnt].waferIDs[wCnt]
                            //-------------------------------------------------------------------------------
                            List<ProcessDTO.ProcessResourceInfoGroupByProcessResource> aPosProcResrcInfoByResrces = null;
                            if (CimStringUtils.equals(BizConstant.SP_DCDEF_COLLECTION_MEASUREMENT,strDCDef.get(0).getDataCollectionType())){
                                log.info("dataCollectionType is Measurement");
                                if (null != aCheckPO){
                                    log.info("aCheckPO is not null");
                                    aPosProcResrcInfoByResrces = aCheckPO.allAssignedProcessResourceInfoGroupByProcessResource();
                                }
                            }else {
                                log.info("dataCollectionType is not Measurement");
                                if (null != aPO){
                                    log.info("aPO is not null");
                                    aPosProcResrcInfoByResrces = aPO.allAssignedProcessResourceInfoGroupByProcessResource();
                                }
                            }
                            int chamberLen = CimArrayUtils.getSize(aPosProcResrcInfoByResrces);
                            List<Infos.WaferIDByChamber> waferIDByChambers = new ArrayList<>();
                            spcInput.setWaferIDByChambers(waferIDByChambers);
                            for (int chamCnt = 0; chamCnt < chamberLen; chamCnt++) {
                                Infos.WaferIDByChamber waferIDByChamber = new Infos.WaferIDByChamber();
                                waferIDByChambers.add(waferIDByChamber);
                                waferIDByChamber.setProcChamber(ObjectIdentifier.fetchValue(aPosProcResrcInfoByResrces.get(chamCnt).getProcessResourceID()));
                                log.info("Process Chamber ID: {}",spcInput.getWaferIDByChambers().get(chamCnt).getProcChamber());
                                int waferLen = CimArrayUtils.getSize(aPosProcResrcInfoByResrces.get(chamCnt).getProcessWafers());
                                List<String> waferIDs = new ArrayList<>();
                                waferIDByChamber.setWaferIDs(waferIDs);
                                for (int wCnt = 0; wCnt < waferLen; wCnt++) {
                                    waferIDs.add(aPosProcResrcInfoByResrces.get(chamCnt).getProcessWafers().get(wCnt));
                                    log.info("Procee WaferID: {}",spcInput.getWaferIDByChambers().get(chamCnt).getWaferIDs().get(wCnt));
                                }
                            }
                            /*------------------------------------------------------------------------*/
                            /*                                                                        */
                            /*   Send SPC Check Request to SPC service Manager                        */
                            /*                                                                        */
                            /*------------------------------------------------------------------------*/


                            //-------------------------
                            //   Send Request to SPC
                            //-------------------------
                            // 【step-9】 doSpcCheck( spcInput, spcOutput, envTimeOut );
                            spcInput.setEquipmentID(equipmentID);
                            //qiandao-dev mes-spc integration add start
                            spcInput.setMeasurementEquipmentID(equipmentID);
                            CimMachineRecipe measurementRecipe = aPO.getAssignedMachineRecipe();
                            CimProcessDefinition measurementStep = aPO.getProcessDefinition();
                            CimMachine machine = baseCoreFactory.getBO(CimMachine.class, equipmentID);
                            Validations.check(null == machine,retCodeConfig.getNotFoundMachine(),ObjectIdentifier.fetchValue(equipmentID));
                            Validations.check(null == measurementRecipe,retCodeConfig.getNotFoundMachineRecipe());
                            Validations.check(null == measurementStep,retCodeConfig.getNotFoundProcessDefinition(),"");

                            spcInput.setMeasurementRecipeID(ObjectIdentifier.build(measurementRecipe.getIdentifier(),measurementRecipe.getPrimaryKey()));
                            spcInput.setMeasurementEquipmentGroup(machine.getMachineType());
                            spcInput.setStepID(ObjectIdentifier.build(measurementStep.getIdentifier(),measurementStep.getPrimaryKey()));
                            spcInput.setDepartment(ObjectIdentifier.buildWithValue(measurementStep.getResponsibleDepartment()));

                            String sql1 = "SELECT STAGE_ID,STAGE_RKEY FROM OMPRF_ROUTESEQ WHERE REFKEY = ?1 AND LINK_KEY =?2 ";
                            Object[] stageArray = cimJpaRepository.queryOne(sql1,aPO.getMainProcessFlow().getPrimaryKey(), aPO.getModuleNumber());
                            Validations.check(CimObjectUtils.isEmpty(stageArray),retCodeConfig.getNotFoundPfForDurable());
                            String stageID = String.valueOf(stageArray[0]) ;
                            String stageObj = String.valueOf(stageArray[1]);
                            spcInput.setStage(ObjectIdentifier.build(stageID,stageObj));

                            ObjectIdentifier measurementLogicalRecipeID = logicalRecipeMethod.processLogicalRecipeGetDR(objCommon,
                                    ObjectIdentifier.buildWithValue(aProdSpec.getIdentifier()),
                                    ObjectIdentifier.build(measurementStep.getIdentifier(), measurementStep.getPrimaryKey()));
                            spcInput.setLogicalRecipeID(measurementLogicalRecipeID);

                            ProcessDTO.ActualStartInformationForPO actualStartInfo = aPO.getActualStartInfo(false);
                            List<ObjectIdentifier> reticleIDs = new ArrayList<>();
                            if (null != actualStartInfo){
                                if (CimArrayUtils.isNotEmpty(actualStartInfo.getAssignedReticles())){
                                    reticleIDs = actualStartInfo.getAssignedReticles().stream().map(ProcessDTO.StartReticleInfo::getReticleID).collect(Collectors.toList());
                                }
                            }
                            spcInput.setReticleIDs(reticleIDs);
                            //qiandao-dev mes-spc integration add end

                            spcOutput = this.spcCheck(spcInput);
                            Infos.SpcIFParm tmpSPcIFParm = new Infos.SpcIFParm();
                            tmpSPcIFParm.setSpcInput(new Inputs.SpcInput());
                            tmpSPcIFParm.getSpcInput().setRequestUserID(spcInput.getRequestUserID());
                            tmpSPcIFParm.getSpcInput().setLotID(spcInput.getLotID());
                            tmpSPcIFParm.getSpcInput().setProcessEquipmentID(spcInput.getProcessEquipmentID());
                            tmpSPcIFParm.getSpcInput().setProcessRecipeID(spcInput.getProcessRecipeID());
                            tmpSPcIFParm.getSpcInput().setProcessProcessFlowID(spcInput.getProcessProcessFlowID());
                            tmpSPcIFParm.getSpcInput().setProcessStepID(spcInput.getProcessStepID());
                            tmpSPcIFParm.getSpcInput().setProcessOperationNumber(spcInput.getProcessOperationNumber());
                            tmpSPcIFParm.getSpcInput().setMeasurementEquipmentID(spcInput.getMeasurementEquipmentID());
                            tmpSPcIFParm.getSpcInput().setTechnologyID(spcInput.getTechnologyID());
                            tmpSPcIFParm.getSpcInput().setProductGroupID(spcInput.getProductGroupID());
                            tmpSPcIFParm.getSpcInput().setProductID(spcInput.getProductID());
                            tmpSPcIFParm.getSpcInput().setReticleID(spcInput.getReticleID());
                            tmpSPcIFParm.getSpcInput().setFixtureID(spcInput.getFixtureID());
                            tmpSPcIFParm.getSpcInput().setProcessFlowID(spcInput.getProcessFlowID());
                            tmpSPcIFParm.getSpcInput().setOperationNumber(spcInput.getOperationNumber());
                            tmpSPcIFParm.getSpcInput().setOperationName(spcInput.getOperationName());
                            tmpSPcIFParm.getSpcInput().setOwnerUserID(spcInput.getOwnerUserID());
                            tmpSPcIFParm.getSpcInput().setEdcType(spcInput.getEdcType());
                            tmpSPcIFParm.getSpcInput().setEdcID(spcInput.getEdcID());
                            tmpSPcIFParm.getSpcInput().setLotComment(spcInput.getLotComment());

                            int iSize = CimArrayUtils.getSize(spcInput.getDcItems());
                            tmpSPcIFParm.getSpcInput().setDcItems(new ArrayList<>());
                            for (int iSizeCnt = 0; iSizeCnt < iSize; iSizeCnt++) {
                                Infos.SpcDcItem spcDcItem = new Infos.SpcDcItem();
                                tmpSPcIFParm.getSpcInput().getDcItems().add(spcDcItem);
                                spcDcItem.setDataItemName(spcInput.getDcItems().get(iSizeCnt).getDataItemName());
                                spcDcItem.setWaferID(spcInput.getDcItems().get(iSizeCnt).getWaferID());
                                spcDcItem.setWaferPosition(spcInput.getDcItems().get(iSizeCnt).getWaferPosition());
                                spcDcItem.setSitePosition(spcInput.getDcItems().get(iSizeCnt).getSitePosition());
                                spcDcItem.setDataValue(spcInput.getDcItems().get(iSizeCnt).getDataValue());
                                spcDcItem.setTargetValue(spcInput.getDcItems().get(iSizeCnt).getTargetValue());
                                spcDcItem.setSpecCheckResult(spcInput.getDcItems().get(iSizeCnt).getSpecCheckResult());
                                spcDcItem.setComment(spcInput.getDcItems().get(iSizeCnt).getComment());
                            }
                            iSize = CimArrayUtils.getSize(spcInput.getWaferIDByChambers());
                            tmpSPcIFParm.getSpcInput().setWaferIDByChambers(new ArrayList<>());
                            for (int iSizeCnt = 0; iSizeCnt < iSize; iSizeCnt++) {
                                Infos.WaferIDByChamber waferIDByChamber = new Infos.WaferIDByChamber();
                                tmpSPcIFParm.getSpcInput().getWaferIDByChambers().add(waferIDByChamber);
                                waferIDByChamber.setProcChamber(spcInput.getWaferIDByChambers().get(iSizeCnt).getProcChamber());
                                waferIDByChamber.setWaferIDs(spcInput.getWaferIDByChambers().get(iSizeCnt).getWaferIDs());
                            }

                            //---------------------
                            // Set SPC Output Data
                            //---------------------
                            tmpSPcIFParm.setSpcOutput(new Outputs.SpcOutput());
                            tmpSPcIFParm.getSpcOutput().setTxRC(spcOutput.getTxRC());
                            tmpSPcIFParm.getSpcOutput().setLotID(spcOutput.getLotID());
                            tmpSPcIFParm.getSpcOutput().setLotRC(spcOutput.getLotRC());
                            tmpSPcIFParm.getSpcOutput().setLotHoldAction(spcOutput.getLotHoldAction());
                            tmpSPcIFParm.getSpcOutput().setEquipmentHoldAction(spcOutput.getEquipmentHoldAction());
                            tmpSPcIFParm.getSpcOutput().setProcessHoldAction(spcOutput.getProcessHoldAction());
                            tmpSPcIFParm.getSpcOutput().setRecipeHoldAction(spcOutput.getRecipeHoldAction());
                            tmpSPcIFParm.getSpcOutput().setReworkBranchAction(spcOutput.getReworkBranchAction());
                            tmpSPcIFParm.getSpcOutput().setMailSendAction(spcOutput.getMailSendAction());
                            tmpSPcIFParm.getSpcOutput().setBankMoveAction(spcOutput.getBankMoveAction());
                            tmpSPcIFParm.getSpcOutput().setBankID(spcOutput.getBankID());
                            tmpSPcIFParm.getSpcOutput().setReworkRouteID(spcOutput.getReworkRouteID());
                            tmpSPcIFParm.getSpcOutput().setOtherAction(spcOutput.getOtherAction());
                            /**
                             * Task-331 add ocapNo
                             */
                            if (CimStringUtils.isNotEmpty(spcOutput.getOcapNo())){
                                spcCheckLot.setOcapNo(spcOutput.getOcapNo());
                            }

                            iSize = CimArrayUtils.getSize(spcOutput.getChamberHoldActions());
                            tmpSPcIFParm.getSpcOutput().setChamberHoldActions(new ArrayList<>());
                            for (int iSizeCnt = 0; iSizeCnt < iSize; iSizeCnt++) {
                                Infos.ChamberHoldAction chamberHoldAction = new Infos.ChamberHoldAction();
                                tmpSPcIFParm.getSpcOutput().getChamberHoldActions().add(chamberHoldAction);
                                chamberHoldAction.setChamberID(spcOutput.getChamberHoldActions().get(iSizeCnt).getChamberID());
                                chamberHoldAction.setChamberHoldAction(spcOutput.getChamberHoldActions().get(iSizeCnt).getChamberHoldAction());
                                chamberHoldAction.setChamberRecipeHoldAction(spcOutput.getChamberHoldActions().get(iSizeCnt).getChamberRecipeHoldAction());
                            }
                            iSize = CimArrayUtils.getSize(spcOutput.getItemResults());
                            tmpSPcIFParm.getSpcOutput().setItemResults(new ArrayList<>());
                            for (int iSizeCnt = 0; iSizeCnt < iSize; iSizeCnt++) {
                                Results.SpcItemResult spcItemResult = new Results.SpcItemResult();
                                tmpSPcIFParm.getSpcOutput().getItemResults().add(spcItemResult);
                                spcItemResult.setDataItemName(spcOutput.getItemResults().get(iSizeCnt).getDataItemName());

                                int jSize = CimArrayUtils.getSize(spcOutput.getItemResults().get(iSizeCnt).getChartResults());
                                tmpSPcIFParm.getSpcOutput().getItemResults().get(iSizeCnt).setChartResults(new ArrayList<>());
                                for (int jSizeCnt = 0; jSizeCnt < jSize; jSizeCnt++) {
                                    Results.SpcChartResult spcChartResult = new Results.SpcChartResult();
                                    tmpSPcIFParm.getSpcOutput().getItemResults().get(iSizeCnt).getChartResults().add(spcChartResult);
                                    spcChartResult.setChartGroupID(spcOutput.getItemResults().get(iSizeCnt).getChartResults().get(jSizeCnt).getChartGroupID());
                                    spcChartResult.setChartID(spcOutput.getItemResults().get(iSizeCnt).getChartResults().get(jSizeCnt).getChartID());
                                    spcChartResult.setChartType(spcOutput.getItemResults().get(iSizeCnt).getChartResults().get(jSizeCnt).getChartType());
                                    spcChartResult.setChartRC(spcOutput.getItemResults().get(iSizeCnt).getChartResults().get(jSizeCnt).getChartRC());
                                    spcChartResult.setChartOwnerMailAddress(spcOutput.getItemResults().get(iSizeCnt).getChartResults().get(jSizeCnt).getChartOwnerMailAddress());
                                    spcChartResult.setChartSubOwnerMailAddresses(spcOutput.getItemResults().get(iSizeCnt).getChartResults().get(jSizeCnt).getChartSubOwnerMailAddresses());

                                    int kSize = CimArrayUtils.getSize(spcOutput.getItemResults().get(iSizeCnt).getChartResults().get(jSizeCnt).getReturnCodes());
                                    tmpSPcIFParm.getSpcOutput().getItemResults().get(iSizeCnt).getChartResults().get(jSizeCnt).setReturnCodes(new ArrayList<>());
                                    for (int kSizeCnt = 0; kSizeCnt < kSize; kSizeCnt++) {
                                        Results.SpcCheckResultByRule spcCheckResultByRule = new Results.SpcCheckResultByRule();
                                        tmpSPcIFParm.getSpcOutput().getItemResults().get(iSizeCnt).getChartResults().get(jSizeCnt).getReturnCodes().add(spcCheckResultByRule);
                                        spcCheckResultByRule.setRule(spcOutput.getItemResults().get(iSizeCnt).getChartResults().get(jSizeCnt).getReturnCodes().get(kSizeCnt).getRule());
                                        spcCheckResultByRule.setDescription(spcOutput.getItemResults().get(iSizeCnt).getChartResults().get(jSizeCnt).getReturnCodes().get(kSizeCnt).getDescription());
                                        spcCheckResultByRule.setReturnCodeStatus(spcOutput.getItemResults().get(iSizeCnt).getChartResults().get(jSizeCnt).getReturnCodes().get(kSizeCnt).getReturnCodeStatus());
                                    }
                                }
                            }
                            //-------------------------------------
                            // Set Result
                            //-------------------------------------
                            spcIFParmList.add(tmpSPcIFParm);

                            /*------------------------------------------------------------------------*/
                            /*                                                                        */
                            /*   Set the result                                                       */
                            /*                                                                        */
                            /*------------------------------------------------------------------------*/
                            log.info("spcOutput->lotRC = {}",spcOutput.getLotRC());
                            if (CimStringUtils.isEmpty(spcCheckLot.getSpcCheckResult())){
                                log.info("Update spcCheckResult ! {}",spcOutput.getLotRC());
                                spcCheckLot.setSpcCheckResult(spcOutput.getLotRC());
                            }else {
                                // Check priority of SPC Check Result
                                // The order of priorities is as follows.
                                //   H : 1 (highest)
                                //   W : 2
                                //   O : 3
                                //   N : 4  //DSIV00001021
                                int prioritySpcCheckResult = 0;
                                int priorityLotRC = 0;
                                if (CimStringUtils.equals(BizConstant.SP_SPCCHECK_HOLDLIMITOFF, spcCheckLot.getSpcCheckResult())){
                                    prioritySpcCheckResult = 1;
                                }else if (CimStringUtils.equals(BizConstant.SP_SPCCHECK_WARNINGLIMITOFF, spcCheckLot.getSpcCheckResult())){
                                    prioritySpcCheckResult = 2;
                                }else if (CimStringUtils.equals(BizConstant.SP_SPCCHECK_NOTDEFINED, spcCheckLot.getSpcCheckResult())){
                                    prioritySpcCheckResult = 4;
                                }else {
                                    prioritySpcCheckResult = 3;
                                }
                                log.info("priority of spcCheckResult : {}",prioritySpcCheckResult);
                                if (CimStringUtils.equals(BizConstant.SP_SPCCHECK_HOLDLIMITOFF, spcOutput.getLotRC())){
                                    priorityLotRC = 1;
                                }else if (CimStringUtils.equals(BizConstant.SP_SPCCHECK_WARNINGLIMITOFF, spcOutput.getLotRC())){
                                    priorityLotRC = 2;
                                }else if (CimStringUtils.equals(BizConstant.SP_SPCCHECK_NOTDEFINED, spcOutput.getLotRC())){
                                    priorityLotRC = 4;
                                }else {
                                    priorityLotRC = 3;
                                }
                                log.info("priority of spcOutput.lotRC: {}",priorityLotRC);
                                if (priorityLotRC < prioritySpcCheckResult){
                                    log.info("Update spcCheckResult ! {}",spcOutput.getLotRC());
                                    spcCheckLot.setSpcCheckResult(spcOutput.getLotRC());
                                }
                            }
                            Infos.SpcResult spcResult = new Infos.SpcResult();
                            spcCheckLot.setSpcResult(spcResult);
                            List<Infos.SpcDcItemAndChart> spcDcItem = new ArrayList<>();
                            spcResult.setSpcDcItem(spcDcItem);
                            if (CimStringUtils.equals(BizConstant.SP_SPCCHECK_HOLDLIMITOFF, spcOutput.getLotRC())){
                                log.info("lotRC == SP_SPCCHECK_HOLDLIMITOFF");
                                spcCheckLot.setSpcActionCode(new ArrayList<>());
                                if (CimStringUtils.equals(BizConstant.SP_ACTION_DONE, spcOutput.getLotHoldAction())){
                                    log.info("lotHoldAction == SP_ACTION_DONE");
                                    if (CimBooleanUtils.isFalse(lotHoldActionExistFlag)){
                                        log.info("lotHoldActionExistFlag == FALSE");
                                        spcCheckLot.getSpcActionCode().add(BizConstant.SP_ACTIONCODE_LOTHOLD);
                                        lotHoldActionExistFlag = true;
                                        actionCnt++;
                                    }
                                }
                                if (CimStringUtils.equals(BizConstant.SP_ACTION_DONE,spcOutput.getProcessHoldAction())){
                                    log.info("processHoldAction == SP_ACTION_DONE");
                                    if (CimBooleanUtils.isFalse(processHoldActionExistFlag)){
                                        log.info("processHoldActionExistFlag == FALSE");
                                        spcCheckLot.getSpcActionCode().add(BizConstant.SP_ACTIONCODE_PROCESSHOLD);
                                        processHoldActionExistFlag = true;
                                        actionCnt++;
                                    }
                                }
                                if (CimStringUtils.equals(BizConstant.SP_ACTION_DONE,spcOutput.getEquipmentHoldAction())){
                                    log.info("equipmentHoldAction == SP_ACTION_DONE");
                                    if (CimBooleanUtils.isFalse(equipmentHoldActionExistFlag)){
                                        log.info("equipmentHoldActionExistFlag == FALSE");
                                        spcCheckLot.getSpcActionCode().add(BizConstant.SP_ACTIONCODE_EQUIPMENTHOLD);
                                        equipmentHoldActionExistFlag = true;
                                        actionCnt++;
                                    }
                                }
                                if (CimStringUtils.equals(BizConstant.SP_ACTION_DONE,spcOutput.getRecipeHoldAction())){
                                    log.info("recipeHoldAction == SP_Action_Done");
                                    if (CimBooleanUtils.isFalse(recipeHoldActionExistFlag)){
                                        log.info("recipeHoldActionExistFlag == FALSE");
                                        spcCheckLot.getSpcActionCode().add(BizConstant.SP_ACTIONCODE_RECIPEHOLD);
                                        recipeHoldActionExistFlag = true;
                                        actionCnt++;
                                    }
                                }
                                if (CimStringUtils.equals(BizConstant.SP_ACTION_DONE,spcOutput.getReworkBranchAction())){
                                    log.info("reworkBranchAction == SP_Action_Done");
                                    if (CimBooleanUtils.isFalse(reworkBranchActionExistFlag)){
                                        log.info("reworkBranchActionExistFlag == FALSE");
                                        spcCheckLot.getSpcActionCode().add(BizConstant.SP_ACTIONCODE_REWORKBRANCH);
                                        spcCheckLot.getSpcResult().setReworkRouteID(spcOutput.getReworkRouteID());
                                        reworkBranchActionExistFlag = true;
                                        actionCnt++;
                                    }
                                }
                                if (CimStringUtils.equals(BizConstant.SP_ACTION_DONE,spcOutput.getMailSendAction())){
                                    log.info("mailSendAction == SP_Action_Done");
                                    if (CimBooleanUtils.isFalse(mailSendActionExistFlag)){
                                        log.info("mailSendActionExistFlag == FALSE");
                                        spcCheckLot.getSpcActionCode().add(BizConstant.SP_ACTIONCODE_MAILSEND);
                                        mailSendActionExistFlag = true;
                                        actionCnt++;
                                    }
                                }
                                if (CimStringUtils.equals(BizConstant.SP_ACTION_DONE,spcOutput.getBankMoveAction())){
                                    log.info("bankMoveAction == SP_Action_Done");
                                    if (CimBooleanUtils.isFalse(bankMoveActionExistFlag)){
                                        log.info("bankMoveActionExistFlag == FALSE");
                                        spcCheckLot.getSpcActionCode().add(BizConstant.SP_ACTIONCODE_BANKMOVE);
                                        spcCheckLot.getSpcResult().setBankID(spcOutput.getBankID());
                                        bankMoveActionExistFlag = true;
                                        actionCnt++;
                                    }
                                }
                                if (CimStringUtils.isNotEmpty(spcOutput.getOtherAction())){
                                    List<String> otherActions = new ArrayList<>();
                                    String str = spcOutput.getOtherAction();
                                    int totalActionCount = 0;
                                    String[] split = str.split(";");
                                    for (int i = 0; i < split.length; i++) {
                                        otherActions.add(split[i]);
                                        totalActionCount++;
                                        if (totalActionCount >= 3){
                                            break;
                                        }
                                    }
                                    for (int actionCount = 0; actionCount < totalActionCount; actionCount++) {
                                        if (CimStringUtils.equals(BizConstant.SP_ACTIONCODE_ROUTEHOLD,otherActions.get(actionCount))){
                                            if (CimBooleanUtils.isFalse(routeHoldActionExistFlag)){
                                                spcCheckLot.getSpcActionCode().add(BizConstant.SP_ACTIONCODE_ROUTEHOLD);
                                                routeHoldActionExistFlag = true;
                                                actionCnt++;
                                            }
                                        }
                                        if (CimStringUtils.equals(BizConstant.SP_ACTIONCODE_EQUIPMENT_AND_RECIPEHOLD,otherActions.get(actionCount))){
                                            if (CimBooleanUtils.isFalse(equipmentAndRecipeHoldActionExistFlag)){
                                                spcCheckLot.getSpcActionCode().add(BizConstant.SP_ACTIONCODE_EQUIPMENT_AND_RECIPEHOLD);
                                                equipmentAndRecipeHoldActionExistFlag = true;
                                                actionCnt++;
                                            }
                                        }
                                        if (CimStringUtils.equals(BizConstant.SP_ACTIONCODE_PROCESS_AND_EQUIPMENTHOLD,otherActions.get(actionCount))){
                                            if (CimBooleanUtils.isFalse(equipmentAndRecipeHoldActionExistFlag)){
                                                spcCheckLot.getSpcActionCode().add(BizConstant.SP_ACTIONCODE_PROCESS_AND_EQUIPMENTHOLD);
                                                equipmentAndRecipeHoldActionExistFlag = true;
                                                actionCnt++;
                                            }
                                        }
                                    }
                                }
                                //---------------------------------------
                                // Chamber Hold Action Check
                                //---------------------------------------
                                int chamberLenOp = CimArrayUtils.getSize(spcOutput.getChamberHoldActions());
                                if (chamberLenOp > 0){
                                    log.info("spcOutPut.chamberHoldActions.length: {}",chamberLenOp);
                                    //--------------------------------------
                                    // Chamber Hold Action Loop
                                    //--------------------------------------
                                    for (int chamCnt = 0; chamCnt < chamberLenOp; chamCnt++) {
                                        //-------------------------------
                                        // Chamber Hold Action
                                        //-------------------------------
                                        if (CimStringUtils.equals(BizConstant.SP_ACTION_DONE, spcOutput.getChamberHoldActions().get(chamCnt).getChamberHoldAction())){
                                            log.info("ChamberHoldAction is Done!");
                                            //------------------------------------------
                                            // Check Chamber and Action Code duplication
                                            // Because EQP has some Chambers.
                                            //-------------------------------------------
                                            boolean bDupricateFlag = false;
                                            for (int i = 0; i < totalCount; i++) {
                                                int spcActCodeLen = CimArrayUtils.getSize(spcCheckReqResult.getSpcCheckLot().get(i).getSpcActionCode());
                                                for (int j = 0; j < spcActCodeLen; j++) {
                                                    if (ObjectIdentifier.equalsWithValue(spcCheckReqResult.getSpcCheckLot().get(i).getProcessChamberID(), spcOutput.getChamberHoldActions().get(chamCnt).getChamberID())
                                                            && CimStringUtils.equals(BizConstant.SP_ACTIONCODE_CHAMBERHOLD, spcCheckReqResult.getSpcCheckLot().get(i).getSpcActionCode().get(j))){
                                                        log.info("Duplicate record exist.");
                                                        bDupricateFlag = true;
                                                        break;
                                                    }
                                                }
                                                if (CimBooleanUtils.isTrue(bDupricateFlag)){
                                                    break;
                                                }
                                            }
                                            //-------------------------------------
                                            // Set ChemberID and Action Code
                                            //-------------------------------------
                                            if (CimBooleanUtils.isFalse(bDupricateFlag)){
                                                spcCheckLot.setProcessChamberID(new ObjectIdentifier(spcOutput.getChamberHoldActions().get(chamCnt).getChamberID()));
                                                spcCheckLot.getSpcActionCode().add(actionCnt,BizConstant.SP_ACTIONCODE_CHAMBERHOLD);
                                                chamberHoldActionExistFlag = true;

                                                int iLen = CimArrayUtils.getSize(spcOutput.getItemResults());
                                                spcCheckLot.getSpcResult().setSpcDcItem(new ArrayList<>());
                                                for (int i = 0; i < iLen; i++) {
                                                    Infos.SpcDcItemAndChart spcDcItemAndChart = new Infos.SpcDcItemAndChart();
                                                    spcCheckLot.getSpcResult().getSpcDcItem().add(k,spcDcItemAndChart);
                                                    spcDcItemAndChart.setDataCollectionItemName(spcOutput.getItemResults().get(i).getDataItemName());
                                                    int jLen = CimArrayUtils.getSize(spcOutput.getItemResults().get(i).getChartResults());
                                                    spcCheckLot.getSpcResult().getSpcDcItem().get(k).setSpcChartList(new ArrayList<>());
                                                    for (int j = 0; j < jLen; j++) {
                                                        Infos.SpcChart spcChart = new Infos.SpcChart();
                                                        spcCheckLot.getSpcResult().getSpcDcItem().get(k).getSpcChartList().add(j,spcChart);
                                                        spcChart.setChartGroupID(spcOutput.getItemResults().get(i).getChartResults().get(j).getChartGroupID());
                                                        spcChart.setChartID(spcOutput.getItemResults().get(i).getChartResults().get(j).getChartID());
                                                        spcChart.setChartType(spcOutput.getItemResults().get(i).getChartResults().get(j).getChartType());
                                                        spcChart.setChartOwnerMailAddress(spcOutput.getItemResults().get(i).getChartResults().get(j).getChartOwnerMailAddress());
                                                        spcChart.setChartSubOwnerMailAddress(spcOutput.getItemResults().get(i).getChartResults().get(j).getChartSubOwnerMailAddresses());
                                                        int mLen = CimArrayUtils.getSize(spcOutput.getItemResults().get(i).getChartResults().get(j).getReturnCodes());
                                                        spcCheckLot.getSpcResult().getSpcDcItem().get(k).getSpcChartList().get(j).setSpcReturnCodeList(new ArrayList<>());
                                                        for (int mm = 0; mm < mLen; mm++) {
                                                            Infos.SpcReturnCode spcReturnCode = new Infos.SpcReturnCode();
                                                            spcCheckLot.getSpcResult().getSpcDcItem().get(k).getSpcChartList().get(j).getSpcReturnCodeList().add(mm,spcReturnCode);
                                                            spcReturnCode.setReturnCodeStatus(spcOutput.getItemResults().get(i).getChartResults().get(j).getReturnCodes().get(mm).getReturnCodeStatus());
                                                            spcReturnCode.setRule(spcOutput.getItemResults().get(i).getChartResults().get(j).getReturnCodes().get(mm).getRule());
                                                        }
                                                    }
                                                    k++;
                                                }
                                                k = 0;
                                                actionCnt = 0;
                                                totalCount++;
                                                Infos.SpcCheckLot spcCheckLot1 = new Infos.SpcCheckLot();
                                                spcCheckReqResult.getSpcCheckLot().add(spcCheckLot1);
                                                spcCheckLot1.setLotID(spcInput.getLotID());
                                                spcCheckLot1.setProcessEquipmentID(spcInput.getProcessEquipmentID());
                                                spcCheckLot1.setProcessMachineRecipeID(spcInput.getProcessRecipeID());
                                                spcCheckLot1.setProcessRouteID(spcInput.getProcessProcessFlowID());
                                                spcCheckLot1.setProductID(ObjectIdentifier.build(spcInput.getProductID().getValue(),aProdSpec.getPrimaryKey()));
                                                spcCheckLot1.setProcessOperationNumber(spcInput.getProcessOperationNumber());
                                                spcCheckLot1.setProcessChamberID(new ObjectIdentifier("",""));
                                                spcCheckLot1.setProcessFabID(correspondingFabID);
                                                spcCheckLot1.setProcessObjrefPO(aCheckPO.getPrimaryKey());
                                                spcCheckLot1.setRequestOtherFabFlag(requestOtherFabFlag);
                                                spcCheckLot1.setDcDefID(spcInput.getEdcID());
                                                spcCheckLot1.setDcSpecID(strDCDef.get(m).getDataCollectionSpecificationID());
                                            }
                                        }
                                        //-------------------------------
                                        // Chamber and Recipe Hold Action
                                        //-------------------------------
                                        if (CimStringUtils.equals(BizConstant.SP_ACTION_DONE,spcOutput.getChamberHoldActions().get(chamCnt).getChamberRecipeHoldAction())){
                                            log.info("ChamberHoldAction is Done!");
                                            //------------------------------------------
                                            // Check Chamber and Action Code duplication
                                            // Because EQP has some Chambers.
                                            //-------------------------------------------
                                            boolean bDupricateFlag = false;
                                            for (int i = 0; i < totalCount; i++) {
                                                int spcActCodeLen = CimArrayUtils.getSize(spcCheckReqResult.getSpcCheckLot().get(i).getSpcActionCode());
                                                for (int j = 0; j < spcActCodeLen; j++) {
                                                    if (ObjectIdentifier.equalsWithValue(spcCheckLot.getProcessChamberID(), spcOutput.getChamberHoldActions().get(chamCnt).getChamberID())
                                                            && CimStringUtils.equals(BizConstant.SP_ACTIONCODE_CHAMBERANDRECIPEHOLD,spcCheckLot.getSpcActionCode().get(j))){
                                                        log.info("Duplicate record exist.");
                                                        bDupricateFlag = true;
                                                        break;
                                                    }
                                                }
                                                if (CimBooleanUtils.isTrue(bDupricateFlag)){
                                                    break;
                                                }
                                            }
                                            //-------------------------------------
                                            // Set ChemberID and Action Code
                                            //-------------------------------------
                                            if (CimBooleanUtils.isFalse(bDupricateFlag)){
                                                spcCheckLot.getSpcActionCode().add(actionCnt,BizConstant.SP_ACTIONCODE_CHAMBERANDRECIPEHOLD);
                                                chamberHoldActionExistFlag = true;
                                                int iLen = CimArrayUtils.getSize(spcOutput.getItemResults());
                                                spcCheckLot.getSpcResult().setSpcDcItem(new ArrayList<>());
                                                for (int i = 0; i < iLen; i++) {
                                                    Infos.SpcDcItemAndChart spcDcItemAndChart = new Infos.SpcDcItemAndChart();
                                                    spcCheckLot.getSpcResult().getSpcDcItem().add(k,spcDcItemAndChart);
                                                    spcDcItemAndChart.setDataCollectionItemName(spcOutput.getItemResults().get(i).getDataItemName());
                                                    int jLen = CimArrayUtils.getSize(spcOutput.getItemResults().get(i).getChartResults());
                                                    spcCheckLot.getSpcResult().getSpcDcItem().get(k).setSpcChartList(new ArrayList<>());
                                                    for (int j = 0; j < jLen; j++) {
                                                        Infos.SpcChart spcChart = new Infos.SpcChart();
                                                        spcCheckLot.getSpcResult().getSpcDcItem().get(k).getSpcChartList().add(j,spcChart);
                                                        spcChart.setChartGroupID(spcOutput.getItemResults().get(i).getChartResults().get(j).getChartGroupID());
                                                        spcChart.setChartID(spcOutput.getItemResults().get(i).getChartResults().get(j).getChartID());
                                                        spcChart.setChartType(spcOutput.getItemResults().get(i).getChartResults().get(j).getChartType());
                                                        spcChart.setChartOwnerMailAddress(spcOutput.getItemResults().get(i).getChartResults().get(j).getChartOwnerMailAddress());
                                                        spcChart.setChartSubOwnerMailAddress(spcOutput.getItemResults().get(i).getChartResults().get(j).getChartSubOwnerMailAddresses());
                                                        int mLen = CimArrayUtils.getSize(spcOutput.getItemResults().get(i).getChartResults().get(j).getReturnCodes());
                                                        spcCheckLot.getSpcResult().getSpcDcItem().get(k).getSpcChartList().get(j).setSpcReturnCodeList(new ArrayList<>());
                                                        for (int mm = 0; mm < mLen; mm++) {
                                                            Infos.SpcReturnCode spcReturnCode = new Infos.SpcReturnCode();
                                                            spcCheckLot.getSpcResult().getSpcDcItem().get(k).getSpcChartList().get(j).getSpcReturnCodeList().add(mm,spcReturnCode);
                                                            spcReturnCode.setReturnCodeStatus(spcOutput.getItemResults().get(i).getChartResults().get(j).getReturnCodes().get(mm).getReturnCodeStatus());
                                                            spcReturnCode.setRule(spcOutput.getItemResults().get(i).getChartResults().get(j).getReturnCodes().get(mm).getRule());
                                                        }
                                                    }
                                                    k++;
                                                }
                                                k = 0;
                                                actionCnt = 0;
                                                totalCount++;
                                                Infos.SpcCheckLot spcCheckLot1 = new Infos.SpcCheckLot();
                                                spcCheckReqResult.getSpcCheckLot().add(totalCount,spcCheckLot1);
                                                spcCheckLot1.setLotID(spcInput.getLotID());
                                                spcCheckLot1.setProcessEquipmentID(spcInput.getProcessEquipmentID());
                                                spcCheckLot1.setProcessMachineRecipeID(spcInput.getProcessRecipeID());
                                                spcCheckLot1.setProcessRouteID(spcInput.getProcessProcessFlowID());
                                                spcCheckLot1.setProductID(ObjectIdentifier.build(spcInput.getProductID().getValue(),aProdSpec.getPrimaryKey()));
                                                spcCheckLot1.setProcessOperationNumber(spcInput.getProcessOperationNumber());
                                                spcCheckLot1.setProcessChamberID(new ObjectIdentifier("",""));
                                                spcCheckLot1.setProcessFabID(correspondingFabID);
                                                spcCheckLot1.setProcessObjrefPO(aCheckPO.getPrimaryKey());
                                                spcCheckLot1.setRequestOtherFabFlag(requestOtherFabFlag);
                                                spcCheckLot1.setDcDefID(spcInput.getEdcID());
                                                spcCheckLot1.setDcSpecID(strDCDef.get(m).getDataCollectionSpecificationID());
                                            }
                                        }
                                    }
                                }
                            }
                            int nILen = CimArrayUtils.getSize(spcOutput.getItemResults());
                            for (int i = 0; i < nILen; i++) {
                                Infos.SpcDcItemAndChart spcDcItemAndChart = new Infos.SpcDcItemAndChart();
                                spcCheckLot.getSpcResult().getSpcDcItem().add(spcDcItemAndChart);
                                spcDcItemAndChart.setDataCollectionItemName(spcOutput.getItemResults().get(i).getDataItemName());
                                int nJLen = CimArrayUtils.getSize(spcOutput.getItemResults().get(i).getChartResults());
                                for (int j = 0; j < nJLen; j++) {
                                    Infos.SpcChart spcChart = new Infos.SpcChart();
                                    spcCheckLot.getSpcResult().getSpcDcItem().get(k).getSpcChartList().add(j,spcChart);
                                    spcChart.setChartGroupID(spcOutput.getItemResults().get(i).getChartResults().get(j).getChartGroupID());
                                    spcChart.setChartID(spcOutput.getItemResults().get(i).getChartResults().get(j).getChartID());
                                    spcChart.setChartType(spcOutput.getItemResults().get(i).getChartResults().get(j).getChartType());

                                    int nMLen = CimArrayUtils.getSize(spcOutput.getItemResults().get(i).getChartResults().get(j).getReturnCodes());
                                    for (int mm = 0; m < nMLen; m++) {
                                        Infos.SpcReturnCode spcReturnCode = new Infos.SpcReturnCode();
                                        spcCheckLot.getSpcResult().getSpcDcItem().get(k).getSpcChartList().get(j).getSpcReturnCodeList().add(mm,spcReturnCode);
                                        spcReturnCode.setReturnCodeStatus(spcOutput.getItemResults().get(i).getChartResults().get(j).getReturnCodes().get(mm).getReturnCodeStatus());
                                        spcReturnCode.setRule(spcOutput.getItemResults().get(i).getChartResults().get(j).getReturnCodes().get(mm).getRule());
                                        spcCheckLot.getSpcResult().getSpcDcItem().get(k).getSpcChartList().get(j).setChartOwnerMailAddress(spcOutput.getItemResults().get(i).getChartResults().get(j).getChartOwnerMailAddress());
                                        spcCheckLot.getSpcResult().getSpcDcItem().get(k).getSpcChartList().get(j).setChartSubOwnerMailAddress(spcOutput.getItemResults().get(i).getChartResults().get(j).getChartSubOwnerMailAddresses());
                                    }
                                }
                                k++;
                            }
                            totalCount++;
                        }
                    }
                }
            }
        }
        return out;
    }

    private Outputs.SpcOutput spcCheck(Inputs.SpcInput spcInput){
        Outputs.SpcOutput output = new Outputs.SpcOutput();
        log.info("MES -> SPC Integration request: {}", JSONObject.toJSONString(spcInput));
        List<Results.SpcCheckResult> spcCheckResultsList = null;
        try {
            spcCheckResultsList = spcRemoteManager.doSPCCheckReq(spcInput);
        } catch(CimIntegrationException e) {
            Validations.check(true,new OmCode((int) e.getCode(),e.getMessage()));
        }
        //SPC check ocap
        if (CimArrayUtils.isNotEmpty(spcCheckResultsList)){
            for (Results.SpcCheckResult spcCheckResult : spcCheckResultsList) {
                if (CimStringUtils.isNotEmpty(spcCheckResult.getOcapNo())
                        && CimStringUtils.isNotEmpty(spcCheckResult.getLotID())){
                    //init update ocapNum and lotID
                    ocapMethod.ocapInfomationSave(ObjectIdentifier.buildWithValue(spcCheckResult.getLotID()),
                            spcCheckResult.getOcapNo(),
                            null);
                    output.setOcapNo(spcCheckResult.getOcapNo());
                }
            }
        }
        //SPC check result
        output.setLotID(spcInput.getLotID());
        boolean isOk = true;
        if (!CimArrayUtils.isEmpty(spcCheckResultsList)){
            for (Results.SpcCheckResult spcCheckResults : spcCheckResultsList){
                if (CimStringUtils.equals(spcCheckResults.getErrorCode(),
                        BizConstant.SPC_INVALID_INPUT_PARAM_ERROR_CODE)){
                    output.setLotRC(BizConstant.SP_SPCCHECK_IF_EXCEPTION);
                    isOk = false;
                    break;
                }
                if (CimBooleanUtils.isTrue(spcCheckResults.getLotHoldAction())){
                    output.setLotRC(BizConstant.SP_SPCCHECK_HOLDLIMITOFF);
                    output.setLotHoldAction(BizConstant.VALUE_ONE);
                    isOk = false;
                }
                List<Infos.HoldEquipmentModel> holdEquipmentModelList = spcCheckResults.getHoldEquipmentModel();
                if (!CimArrayUtils.isEmpty(holdEquipmentModelList)){
                    for (Infos.HoldEquipmentModel holdEquipmentModel : holdEquipmentModelList){
                        if (CimBooleanUtils.isTrue(holdEquipmentModel.getEquipmentHold())){
                            output.setLotRC(BizConstant.SP_SPCCHECK_HOLDLIMITOFF);
                            output.setEquipmentHoldAction(BizConstant.VALUE_ONE);
                            isOk = false;
                        }
                    }
                }
                List<Infos.InhibitRecipeResultModel> inhibitRecipeResultModelList =
                        spcCheckResults.getInhibitRecipeResultModel();
                if (!CimArrayUtils.isEmpty(inhibitRecipeResultModelList)){
                    for (Infos.InhibitRecipeResultModel inhibitRecipeResultModel : inhibitRecipeResultModelList){
                        if (CimBooleanUtils.isTrue(inhibitRecipeResultModel.getRecipeHold())){
                            output.setLotRC(BizConstant.SP_SPCCHECK_HOLDLIMITOFF);
                            output.setRecipeHoldAction(BizConstant.VALUE_ONE);
                            isOk = false;
                        }
                    }
                }
            }
            if (CimBooleanUtils.isTrue(isOk)){
                output.setLotRC(BizConstant.SP_SPCCHECK_OK);
            }
        }
        return output;
    }

//    private String doSpcCheck(String jsonString){
//        String wlURL = "http://118.123.246.35:58080/RTService/Service.asmx?wsdl";
//        String soapPrototol = "ServiceSoap";
//        InitialContext ic = null;
//        String spcCheckResultStr = null;
//        try {
//            ic = new InitialContext();
//        } catch (NamingException e) {
//            log.info("edc2SpcCheck InitialContext ");
//        }
//        // server
//        try {
//            if (ic != null) {
//                wlURL = (String) ic.lookup("java:/comp/env/wsURL");
//            }
//            log.info("edc2SpcCheck wlURL = " + wlURL);
//        } catch (NamingException ne) {
//            log.error("edc2SpcCheck InitialContext ");
//        }
//
//        try {
//            log.info("edc2SpcCheck DataCollection: " + jsonString);
//
//            ServiceLocator service = new ServiceLocator();
//            service.setEndpointAddress(soapPrototol, wlURL);
//            ServiceSoap_PortType portType = service.getServiceSoap();
//            spcCheckResultStr = portType.spcCheck(jsonString);
//
//            log.info("edc2SpcCheck Result: " + spcCheckResultStr);
//
//        } catch (Exception e) {
//            log.error("edc2SpcCheck ", e);
//            e.printStackTrace();
//        }
//        return spcCheckResultStr;
//    }

}
