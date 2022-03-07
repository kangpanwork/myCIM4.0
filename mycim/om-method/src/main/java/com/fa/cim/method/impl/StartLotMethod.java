package com.fa.cim.method.impl;

import com.fa.cim.annotaion.OmMethod;
import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.OmCode;
import com.fa.cim.common.utils.*;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.dto.*;
import com.fa.cim.jpa.SearchCondition;
import com.fa.cim.method.*;
import com.fa.cim.newcore.bo.machine.CimMachine;
import com.fa.cim.newcore.bo.pd.*;
import com.fa.cim.newcore.bo.prodspec.CimProductSpecification;
import com.fa.cim.newcore.bo.product.CimLot;
import com.fa.cim.newcore.bo.product.CimMonitorGroup;
import com.fa.cim.newcore.bo.recipe.CimMachineRecipe;
import com.fa.cim.newcore.dto.pd.ProcessDTO;
import com.fa.cim.newcore.dto.product.ProductDTO;
import com.fa.cim.newcore.factory.BaseCoreFactory;
import com.fa.cim.newcore.impl.bo.env.StandardProperties;
import com.fa.cim.newcore.standard.prcssdfn.ProcessDefinition;
import com.fa.cim.newcore.standard.prcssdfn.ProcessFlow;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

/**
 * description:
 * <p>StartLotMethod .<br/></p>
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2018/11/5        ********             PlayBoy               create file
 *
 * @author: PlayBoy
 * @date: 2018/11/5 16:51
 * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@OmMethod
@Slf4j
public class StartLotMethod  implements IStartLotMethod {

    @Autowired
    private RetCodeConfig retCodeConfig;

    @Autowired
    private ILotMethod lotMethod;

    @Autowired
    private IProcessMethod processMethod;

    @Autowired
    private IFPCMethod fpcMethod;

    @Autowired
    private BaseCoreFactory baseCoreFactory;

    @Autowired
    private IConstraintMethod constraintMethod;

    @Override
    public Outputs.ObjStartLotActionListEffectSpecCheckOut startLotActionListEffectSpecCheck(Outputs.ObjStartLotActionListEffectSpecCheckOut outData,
                                                                                             Infos.ObjCommon objCommon,
                                                                                             List<Infos.StartCassette> startCassetteList,
                                                                                             ObjectIdentifier equipmentID,
                                                                                             List<Infos.InterFabMonitorGroupActionInfo> interFabMonitorGroupActionInfoList,
                                                                                             List<Results.DCActionLotResult> dcActionLotResultList) {
        Outputs.ObjStartLotActionListEffectSpecCheckOut retVal = outData == null ? new Outputs.ObjStartLotActionListEffectSpecCheckOut() : outData;

        String correspondingOpeMode = StandardProperties.OM_EDC_MULTI_CORRESPOND_FLAG.getValue();
        String correspondingDefaltMode = StandardProperties.OM_EDC_CORRESPOND_OPE_DEFAULT_FLAG.getValue();
        List<ProductDTO.MonitoredLot> effectedLots = new ArrayList<>();
        AtomicBoolean findFlag = new AtomicBoolean(false);
        AtomicBoolean registeredFlag = new AtomicBoolean(false);
        boolean findLotFlag = false;
        boolean findMsgFlag = false;
        boolean findMonitoringLotFlag = false;

        int relMongCount = CimArrayUtils.isEmpty(interFabMonitorGroupActionInfoList) ? 0 : interFabMonitorGroupActionInfoList.size();

        Validations.check(CimArrayUtils.isEmpty(startCassetteList), retCodeConfig.getInvalidParameter(), objCommon.getTransactionID());
        for (Infos.StartCassette startCassette : startCassetteList) {
            List<Infos.LotInCassette> lotInCassetteList = startCassette.getLotInCassetteList();
            if (CimArrayUtils.isEmpty(lotInCassetteList)) {
                continue;
            }
            for (Infos.LotInCassette lotInCassette : lotInCassetteList) {
                /*---------------------------*/
                /*   Omit Not-OpeStart Lot   */
                /*---------------------------*/
                if (CimBooleanUtils.isFalse(lotInCassette.getMoveInFlag())) {
                    continue;
                }

                /*---------------------------*/
                /*   Omit Non-DataCollection */
                /*---------------------------*/
                List<Infos.DataCollectionInfo> dcDefList = lotInCassette.getStartRecipe().getDcDefList();
                if (CimArrayUtils.isEmpty(dcDefList)) {
                    continue;
                }

                //-----------------------------------------
                //  Get Lot ojbect
                //-----------------------------------------
                CimLot lot = baseCoreFactory.getBO(CimLot.class, lotInCassette.getLotID());
                CimProcessOperation po = null;
                //Current PO or Previous PO ?, lot_CheckConditionForPO
                Boolean lotCheckConditionForPoOut = lotMethod.lotCheckConditionForPO(objCommon, lotInCassette.getLotID());

                boolean currentProcessOperationFlag = CimBooleanUtils.isTrue(lotCheckConditionForPoOut);
                if (currentProcessOperationFlag) {
                    //Get PO from Current Operation.
                    po = lot.getProcessOperation();
                } else {
                    //Get PO from Previous Operation.
                    po = lot.getPreviousProcessOperation();
                }
                Validations.check (po == null, retCodeConfig.getNotFoundProcessOperation());
                CimProcessDefinition mainPD = po.getMainProcessDefinition();
                Validations.check (mainPD == null, retCodeConfig.getNotFoundProcessDefinition());

                ObjectIdentifier mainPDID = ObjectIdentifier.build(mainPD.getIdentifier(), mainPD.getPrimaryKey());
                String currentFabID = po.getFabID();
                String controlJobID = po.getAssignedControlJobID();
                List<ProcessDTO.ActionResultInfo> actionResultInfoList = po.getActionResultInfo();
                String operationNoforFPC = po.getOperationNumber();
                /*
                 Set always the input lot to effectedLots[0]
                 even if lot is monitor lot or not.
                 */
                ProductDTO.MonitoredLot monitoredLot = new ProductDTO.MonitoredLot();
                effectedLots.add(monitoredLot);
                monitoredLot.setLotID(lotInCassette.getLotID());
                monitoredLot.setProcessOperation(po.getPrimaryKey());

                CimProcessFlowContext pfx = lot.getProcessFlowContext();
                Validations.check (pfx == null, retCodeConfig.getNotFoundPfx());

                boolean requestOtherFabFlagForMonitor = false;
                String generatedFabID = null;
                /*
                 Check Monitor Grouping existnce
                 If not existed, the input lot is not monitor lot.
                 If existed, the input lot is monitor lot.
                 */
                CimMonitorGroup monitorGroup = lot.getControlMonitorGroup();
                if (monitorGroup != null) {
                    //When Monitor Grouping is existed get all related lots.
                    List<ProductDTO.MonitoredLot> monitoredLots = monitorGroup.allLots();
                    if (CimArrayUtils.isNotEmpty(monitoredLots)) {
                        effectedLots.addAll(monitoredLots);
                    }
                    //Get GeneratedFabID
                    generatedFabID = monitorGroup.getGeneratedFabID();
                    if (CimStringUtils.isNotEmpty(generatedFabID)
                            && CimStringUtils.isNotEmpty(currentFabID)
                            && CimStringUtils.unEqual(generatedFabID, currentFabID)) {
                        requestOtherFabFlagForMonitor = true;
                    }
                }
                boolean findMeasurementLotFlag = false;
                boolean findCorrespondingInfoFlag = false;
                int dcActionLotIndex = 0;
                int fpcCnt=0;
                int coFPCLen=0;
                int dcItemLen=0;
                int foundFPCCnt=0;
                int coLen=0;
                int checkOpeCnt = 0;
                List<Infos.CorrespondingOperationInfo> tmpCorrOperations = new ArrayList<>();
                List<Infos.OperationNameAttributes> opeNoList = new ArrayList<>();
                List<Infos.FPCInfo> strFPCInfoGetDROut=null;

                for (Infos.DataCollectionInfo dcDef : dcDefList) {
                    //Check actionCode
                    String reasonCode;
                    String messageReason;
                    List<String> dcItemNames = new ArrayList<>();
                    String singleCorrOpeFPC = null;
                    List<Infos.DataCollectionItemInfo> dcItemList = dcDef.getDcItems();

                    if (CimArrayUtils.isEmpty(dcItemList)) {
                        continue;
                    }
                    for (Infos.DataCollectionItemInfo dcItem : dcItemList) {
                        AtomicBoolean equipmentHoldFlag = new AtomicBoolean(false);
                        AtomicBoolean recipeHoldFlag = new AtomicBoolean(false);
                        AtomicBoolean routeHoldFlag = new AtomicBoolean(false);
                        AtomicBoolean processHoldFlag = new AtomicBoolean(false);
                        AtomicBoolean mailFlag = new AtomicBoolean(false);

                        Map<String, AtomicBoolean> flagMapping = new HashMap<>(16);
                        flagMapping.put(BizConstant.SP_ACTIONCODE_EQUIPMENTHOLD, equipmentHoldFlag);
                        flagMapping.put(BizConstant.SP_ACTIONCODE_RECIPEHOLD, recipeHoldFlag);
                        flagMapping.put(BizConstant.SP_ACTIONCODE_ROUTEHOLD, routeHoldFlag);
                        flagMapping.put(BizConstant.SP_ACTIONCODE_PROCESSHOLD, processHoldFlag);
                        flagMapping.put(BizConstant.SP_ACTIONCODE_MAIL, mailFlag);

                        String actionCodes = dcItem.getActionCodes();
                        if (CimStringUtils.isNotEmpty(actionCodes)) {
                            String[] actionCodeArray = actionCodes.split(",");
                            if (actionCodeArray != null || actionCodeArray.length > 0) {
                                for (String actionCode : actionCodeArray) {
                                    if (flagMapping.containsKey(actionCode)) {
                                        log.debug("startLotActionListEffectSpecCheck(): actionCode is {}", actionCode);
                                        flagMapping.get(actionCode).set(true);
                                    }
                                }
                            }
                        }

                        if (!equipmentHoldFlag.get()
                                && !recipeHoldFlag.get()
                                && !routeHoldFlag.get()
                                && !processHoldFlag.get()
                                && !mailFlag.get()) {
                            if (log.isDebugEnabled()) {
                                log.debug("Flag all FALSE, continue...");
                            }
                            continue;
                        }

                        Map<String, String> specCheckResultMapping = new HashMap<>(16);
                        specCheckResultMapping.put(BizConstant.SP_SPECCHECKRESULT_UPPERCONTROLLIMIT, BizConstant.SP_REASON_SPECOVERHOLD_UPPERCONTROL);
                        specCheckResultMapping.put(BizConstant.SP_SPECCHECKRESULT_LOWERCONTROLLIMIT, BizConstant.SP_REASON_SPECOVERHOLD_LOWERCONTROL);
                        specCheckResultMapping.put(BizConstant.SP_SPECCHECKRESULT_UPPERSPECLIMIT, BizConstant.SP_REASON_SPECOVERHOLD_UPPERSPEC);
                        specCheckResultMapping.put(BizConstant.SP_SPECCHECKRESULT_LOWERSPECLIMIT, BizConstant.SP_REASON_SPECOVERHOLD_LOWERSPEC);
                        specCheckResultMapping.put(BizConstant.SP_SPECCHECKRESULT_UPPERSCREENLIMIT, BizConstant.SP_REASON_SPECOVERHOLD_UPPERSCREEN);
                        specCheckResultMapping.put(BizConstant.SP_SPECCHECKRESULT_LOWERSCREENLIMIT, BizConstant.SP_REASON_SPECOVERHOLD_LOWERSCREEN);
                        specCheckResultMapping.put(BizConstant.SP_SPECCHECKRESULT_APCERROR, BizConstant.SP_REASON_APCERRORHOLD);

                        // Check specCheckResult and set reasonCode / messageReason
                        if (specCheckResultMapping.containsKey(dcItem.getSpecCheckResult())) {
                            String reasonCodeValue = specCheckResultMapping.get(dcItem.getSpecCheckResult());
                            reasonCode = reasonCodeValue;
                            messageReason = reasonCodeValue;
                        } else {
                            reasonCode = BizConstant.SP_REASON_SPECOVERHOLD;
                            messageReason = BizConstant.SP_REASON_SPECOVERHOLD;
                        }

                        //------------------------------------
                        // Other Fab
                        //------------------------------------
                        int interFabMonIdx = 0;
                        if (requestOtherFabFlagForMonitor) {
                            if (CimArrayUtils.isNotEmpty(effectedLots) && effectedLots.size() > 1) {
                                if (relMongCount > 0) {
                                    // if monitoringLot already checked, no add
                                    for (Infos.InterFabMonitorGroupActionInfo interFabMonitorGroupActionInfo : interFabMonitorGroupActionInfoList) {
                                        if (CimStringUtils.equals(effectedLots.get(0).getLotID().getValue(), interFabMonitorGroupActionInfo.getMonitoringLotID().getValue())) {
                                            interFabMonIdx = interFabMonitorGroupActionInfoList.indexOf(interFabMonitorGroupActionInfo);
                                            findMonitoringLotFlag = true;
                                            break;
                                        }
                                    }
                                }
                                if (findMonitoringLotFlag) {
                                    interFabMonIdx = interFabMonitorGroupActionInfoList.size();
                                    //create retVal for mailSend of monitoredLots
                                    Infos.InterFabMonitorGroupActionInfo interFabMonitorGroupActionInfo = new Infos.InterFabMonitorGroupActionInfo();
                                    interFabMonitorGroupActionInfoList.add(interFabMonitorGroupActionInfo);
                                    interFabMonitorGroupActionInfo.setFabID(generatedFabID);
                                    interFabMonitorGroupActionInfo.setMonitoringLotID(effectedLots.get(0).getLotID());
                                    //initialize
                                    Infos.MonitorGroupReleaseInfo monitorGroupReleaseInfo = new Infos.MonitorGroupReleaseInfo();
                                    monitorGroupReleaseInfo.setGroupReleaseFlag(false);
                                    interFabMonitorGroupActionInfo.setMonitorGroupReleaseInfo(monitorGroupReleaseInfo);
                                }
                            }
                        }
                        if (!findMeasurementLotFlag) {
                            //Get index of dcActionLot
                            if (CimArrayUtils.isNotEmpty(dcActionLotResultList)) {
                                for (Results.DCActionLotResult dcActionLotResult : dcActionLotResultList) {
                                    if (CimStringUtils.equals(effectedLots.get(0).getLotID().getValue(), dcActionLotResult.getMeasurementLotID().getValue())) {
                                        dcActionLotIndex = dcActionLotResultList.indexOf(dcActionLotResult);
                                        findMeasurementLotFlag = true;
                                        break;
                                    }
                                }
                            }
                            if (!findMeasurementLotFlag) {
                                log.debug("startLotActionListEffectSpecCheck(): this lot(measurement) has not been checked for Action Result.");
                                //create retVal for DCActionLotResults
                                dcActionLotIndex = dcActionLotResultList.size();
                                Results.DCActionLotResult dcActionLotResult = new Results.DCActionLotResult();
                                dcActionLotResult.setMeasurementLotID(effectedLots.get(0).getLotID());
                                dcActionLotResultList.add(dcActionLotResult);
                                findMeasurementLotFlag = true;
                            }
                            //Get Corresponding Operation Info
                            // FPC_info_GetDR__101
                            Inputs.ObjFPCInfoGetDRIn objFPCInfoGetDRIn=new Inputs.ObjFPCInfoGetDRIn();
                            objFPCInfoGetDRIn.setFPCIDs(Collections.emptyList());
                            objFPCInfoGetDRIn.setLotID(lotInCassette.getLotID());
                            objFPCInfoGetDRIn.setLotFamilyID(null);
                            objFPCInfoGetDRIn.setMainPDID(mainPDID);
                            objFPCInfoGetDRIn.setMainOperNo(operationNoforFPC);
                            objFPCInfoGetDRIn.setOrgMainPDID(null);
                            objFPCInfoGetDRIn.setOrgOperNo(null);
                            objFPCInfoGetDRIn.setSubMainPDID(null);
                            objFPCInfoGetDRIn.setSubOperNo(null);
                            objFPCInfoGetDRIn.setEquipmentID(equipmentID);
                            objFPCInfoGetDRIn.setWaferIDInfoGetFlag(true);
                            objFPCInfoGetDRIn.setRecipeParmInfoGetFlag(false);
                            objFPCInfoGetDRIn.setReticleInfoGetFlag(false);
                            objFPCInfoGetDRIn.setDcSpecItemInfoGetFlag(true );

                            strFPCInfoGetDROut = fpcMethod.fpcInfoGetDR(objCommon, objFPCInfoGetDRIn);
                            fpcCnt = CimArrayUtils.getSize(strFPCInfoGetDROut);
                            if( fpcCnt > 0 ) {
                                int waferCntFromStartCassette = CimArrayUtils.getSize(lotInCassette.getLotWaferList());
                                boolean foundFPCFlag = false;
                                int numFPC;
                                for(numFPC=0; numFPC < fpcCnt; numFPC++) {
                                    final Infos.FPCInfo fpcInfo = strFPCInfoGetDROut.get(numFPC);
                                    int waferCntFromFPC = CimArrayUtils.getSize(fpcInfo.getLotWaferInfoList());
                                    for ( int inFPCWCnt = 0; inFPCWCnt < waferCntFromFPC; inFPCWCnt++) {
                                        for(int inParaWCnt =0 ; inParaWCnt < waferCntFromStartCassette; inParaWCnt++) {
                                            if(CimStringUtils.equals(fpcInfo.getLotWaferInfoList().get(inFPCWCnt).getWaferID().getValue(),
                                                    lotInCassette.getLotWaferList().get(inParaWCnt).getWaferID().getValue()) &&
                                                    CimBooleanUtils.isTrue(lotInCassette.getMoveInFlag())) {
                                                coFPCLen = CimArrayUtils.getSize(fpcInfo.getCorrespondingOperationInfoList());
                                                singleCorrOpeFPC = fpcInfo.getCorrespondOperationNumber();
                                                dcItemLen = CimArrayUtils.getSize(fpcInfo.getDcSpecList());
                                                foundFPCCnt = numFPC;
                                                foundFPCFlag = true;
                                                break;
                                            }
                                        }
                                        if( foundFPCFlag ) {
                                            break;
                                        }
                                    }
                                    if( foundFPCFlag ) {
                                        break;
                                    }
                                }
                            }
                            if ( fpcCnt == 0 || (fpcCnt > 0 && (CimStringUtils.length(singleCorrOpeFPC) == 0 && coFPCLen== 0 ))) {
                                List<ProcessDTO.CorrespondingOperationInfo> correspondingOperationInfo;
                                correspondingOperationInfo = po.getCorrespondingProcessOperationInfo();

                                coLen = CimArrayUtils.getSize(correspondingOperationInfo);
                                if ( coLen == 0 ) {
                                } else {
                                    tmpCorrOperations=new ArrayList<>(coLen );
                                    for ( int q=0; q < coLen; q++ ) {
                                        tmpCorrOperations.add(new Infos.CorrespondingOperationInfo());
                                        tmpCorrOperations.get(q).setDcSpecGroup                   (correspondingOperationInfo.get(q).getDcSpecGroup());
                                        tmpCorrOperations.get(q).setCorrespondingOperationNumber  (correspondingOperationInfo.get(q).getCorrespondingOperationNumber());
                                    }
                                    findCorrespondingInfoFlag = true;
                                }
                            } else {
                                if(coFPCLen > 0) {
                                    tmpCorrOperations=new ArrayList<>(coFPCLen);
                                    for ( int qFPC=0; qFPC < coFPCLen; qFPC++ ) {
                                        tmpCorrOperations.add(new Infos.CorrespondingOperationInfo());
                                        tmpCorrOperations.get(qFPC).setDcSpecGroup                  (strFPCInfoGetDROut.get(foundFPCCnt).getCorrespondingOperationInfoList().get(qFPC).getDcSpecGroup());
                                        tmpCorrOperations.get(qFPC).setCorrespondingOperationNumber (strFPCInfoGetDROut.get(foundFPCCnt).getCorrespondingOperationInfoList().get(qFPC).getCorrespondingOperationNumber());
                                    }
                                    findCorrespondingInfoFlag = true;
                                } else if ( CimStringUtils.length(singleCorrOpeFPC) > 0 ) {
                                    coFPCLen = 1;
                                    tmpCorrOperations=new ArrayList<>(coFPCLen);
                                    tmpCorrOperations.add(new Infos.CorrespondingOperationInfo());
                                    tmpCorrOperations.get(0).setCorrespondingOperationNumber(singleCorrOpeFPC);
                                    findCorrespondingInfoFlag = true;
                                } else {
                                    //----------------------------------------------------
                                    //   Corresponding operation is NOT specified in FPC
                                    //   In this case, current PO can be used for the
                                    //   inhibit action.
                                    //----------------------------------------------------
                                }
                            }

                        }
                        List<String> correspondingOperaionNumbers = new ArrayList<>();
                        if (findCorrespondingInfoFlag) {
                            //Get dcSpecGroup from PO
                            int corrOpeLen=0;
                            if ( ( CimStringUtils.equals(correspondingOpeMode,"1") ) &&
                                    ( ( CimStringUtils.equals(dcDef.getDataCollectionType(), BizConstant.SP_DCDEF_COLLECTION_MEASUREMENT)) ||
                                            ( 0 == CimStringUtils.length(dcDef.getDataCollectionType()) ) ) ) {
                                String dcSpecGroup=null;
                                if( 0 != coFPCLen ) {
                                    if( 0 != CimArrayUtils.getSize(strFPCInfoGetDROut.get(foundFPCCnt).getDcSpecList())) {
                                        for ( int dcItemCnt=0; dcItemCnt < dcItemLen; dcItemCnt++ ) {
                                            if ( CimStringUtils.equals( strFPCInfoGetDROut.get(foundFPCCnt).getDcSpecList().get(dcItemCnt).getDataItemName(),
                                                    dcItem.getDataCollectionItemName() ) ) {
                                                dcSpecGroup = strFPCInfoGetDROut.get(foundFPCCnt).getDcSpecList().get(dcItemCnt).getDcSpecGroup();
                                            }
                                        }
                                    } else {
                                        dcSpecGroup = po.findDCSpecGroup(dcDef.getDataCollectionSpecificationID().getValue(), dcItem.getDataCollectionItemName());
                                    }
                                    correspondingOperaionNumbers=new ArrayList<>(coFPCLen);
                                    for ( int coFPCCnt = 0 ; coFPCCnt < coFPCLen ; coFPCCnt++ ) {
                                        if ( CimStringUtils.equals(tmpCorrOperations.get(coFPCCnt).getDcSpecGroup(), dcSpecGroup)) {
                                            correspondingOperaionNumbers.add(tmpCorrOperations.get(coFPCCnt).getCorrespondingOperationNumber());
                                            corrOpeLen++;
                                        }
                                    }
                                } else {
                                    if( fpcCnt > 0 && 0 != CimArrayUtils.getSize(strFPCInfoGetDROut.get(foundFPCCnt).getDcSpecList())) {
                                        for ( int dcItemCnt=0; dcItemCnt < dcItemLen; dcItemCnt++ ) {
                                            if ( CimStringUtils.equals( strFPCInfoGetDROut.get(foundFPCCnt).getDcSpecList().get(dcItemCnt).getDataItemName(), dcItem.getDataCollectionItemName() ) ) {
                                                dcSpecGroup = strFPCInfoGetDROut.get(foundFPCCnt).getDcSpecList().get(dcItemCnt).getDcSpecGroup();
                                            }
                                        }
                                    } else {
                                        dcSpecGroup = po.findDCSpecGroup(dcDef.getDataCollectionSpecificationID().getValue(), dcItem.getDataCollectionItemName());
                                    }
                                    //------------------------------------------//
                                    // Get CorrespondingOperationNumber         //
                                    //------------------------------------------//
                                    for ( int coCnt = 0 ; coCnt < coLen ; coCnt++ ) {
                                        if ( CimStringUtils.equals(tmpCorrOperations.get(coCnt).getDcSpecGroup(), dcSpecGroup)) {
                                            correspondingOperaionNumbers.add(tmpCorrOperations.get(coCnt).getCorrespondingOperationNumber());
                                            corrOpeLen++;
                                        }
                                    }
                                }

                                //----------------------------------------------------
                                //   Corresponding operation is NOT specified
                                //----------------------------------------------------
                                if ( corrOpeLen == 0 ) {
                                    if ( CimStringUtils.equals(correspondingDefaltMode,"1") ) {
                                        if ( 0 != coFPCLen ) {
                                            for ( int coFPCCnt = 0 ; coFPCCnt < coFPCLen ; coFPCCnt++) {
                                                if ( CimStringUtils.equals(tmpCorrOperations.get(coFPCCnt).getDcSpecGroup(), "") ) {
                                                    correspondingOperaionNumbers.add(tmpCorrOperations.get(coFPCCnt).getCorrespondingOperationNumber());
                                                    corrOpeLen++;
                                                }
                                            }
                                        } else {
                                            for ( int coCnt = 0 ; coCnt < coLen ; coCnt++) {
                                                if ( CimStringUtils.equals(tmpCorrOperations.get(coCnt).getDcSpecGroup(), "") ) {
                                                    correspondingOperaionNumbers.add(tmpCorrOperations.get(coCnt).getCorrespondingOperationNumber());
                                                    corrOpeLen++;
                                                }
                                            }
                                        }
                                    }
                                }
                            } else {
                                corrOpeLen = 1;
                                correspondingOperaionNumbers=new ArrayList<>(1);
                                correspondingOperaionNumbers.add(tmpCorrOperations.get(0).getCorrespondingOperationNumber());
                            }
                        }
                        for (ProductDTO.MonitoredLot effectedLot : effectedLots) {
                            boolean monitorLotFlag = false;
                            int poLen = 0;
                            if (correspondingOperaionNumbers.size() > 0) {
                                poLen = correspondingOperaionNumbers.size();
                            } else {
                                poLen = 1;
                            }
                            if (effectedLots.indexOf(effectedLot) != 0) {
                                monitorLotFlag = true;
                            }
                            for (int poCnt = 0; poCnt < poLen; poCnt++) {
                                String corrFabID = null;
                                CimProcessOperation poTmp = null;
                                Infos.ProcessOperationInfo processOperationInfo = null;
                                boolean requestOtherFabFlag = false;
                                if (monitorLotFlag) {
                                    //Get requestOtherFabFlag
                                    corrFabID = generatedFabID;
                                    if (CimStringUtils.isNotEmpty(currentFabID)
                                            && CimStringUtils.isNotEmpty(corrFabID)
                                            && CimStringUtils.equals(currentFabID, corrFabID)) {
                                        requestOtherFabFlag = true;
                                    } else {
                                        if (poCnt > 0) {
                                            break;
                                        }
                                        poTmp = baseCoreFactory.getBO(CimProcessOperation.class, effectedLot.getProcessOperation());
                                        Validations.check (poTmp == null, new OmCode(retCodeConfig.getNotFoundProcessOperation(), "",effectedLot.getLotID().getValue()));
                                    }
                                }
                                boolean monitorCheck = !monitorLotFlag || (monitorLotFlag && requestOtherFabFlag);
                                if (monitorCheck) {
                                    log.debug("startLotActionListEffectSpecCheck(): Corresponding Operation Get");
                                    if (CimArrayUtils.isNotEmpty(correspondingOperaionNumbers)) {
                                        effectedLots.get(0).setProcessOperation("");
                                        for ( int q = 0 ; q < checkOpeCnt ; q++ ) {
                                            if ( CimStringUtils.equals( opeNoList.get(q).getOperationNumber(), correspondingOperaionNumbers.get(poCnt) ) ) {
                                                effectedLots.get(0).setProcessOperation(opeNoList.get(q).getObjrefPO());
                                                break;
                                            }
                                        }

                                        if( CimStringUtils.length(effectedLots.get(0).getProcessOperation()) == 0 ) {
                                            //------------------------------------------//
                                            // Get CorrespondingProcessOperation        //
                                            //------------------------------------------//
                                            CimProcessOperation aCorrPO;
                                            aCorrPO = pfx.findProcessOperationForRouteOperationNumberBefore(ObjectIdentifier.fetchValue(mainPDID), correspondingOperaionNumbers.get(poCnt));

                                            if ( aCorrPO!=null ) {
                                                //------------------------------------------------
                                                //   Corresponding operation is Specified and
                                                //   that operation was processed.
                                                //------------------------------------------------
                                                effectedLots.get(0).setProcessOperation( aCorrPO.getPrimaryKey() );
                                                poTmp = aCorrPO;

                                                // opeNoList.length(checkOpeCnt + 1);
                                                opeNoList.add(new Infos.OperationNameAttributes());
                                                opeNoList.get(checkOpeCnt).setOperationNumber  (correspondingOperaionNumbers.get(poCnt));
                                                opeNoList.get(checkOpeCnt).setObjrefPO         (effectedLots.get(0).getProcessOperation());
                                                checkOpeCnt++;

                                                if ( 0 < CimStringUtils.length(ObjectIdentifier.fetchValue(dcItem.getWaferID())) ) {
                                                    List<ProcessDTO.PosProcessWafer> strProcessWaferSeq = aCorrPO.getProcessWafers();

                                                    int nWaferLen = CimArrayUtils.getSize(strProcessWaferSeq);
                                                    if ( nWaferLen > 0 ) {
                                                        Boolean bWaferFoundFlag = FALSE;
                                                        for ( int w_i = 0 ; w_i < nWaferLen ; w_i++ ) {
                                                            if ( CimStringUtils.equals( strProcessWaferSeq.get(w_i).getWaferID(), dcItem.getWaferID().getValue()) ) {
                                                                bWaferFoundFlag = TRUE;
                                                            }
                                                        }
                                                        if ( !bWaferFoundFlag ) {
                                                            poTmp = po.findProcessOperationForOperationNumberAndWafer( ObjectIdentifier.fetchValue(mainPDID),
                                                                    correspondingOperaionNumbers.get(poCnt),
                                                                    dcItem.getWaferID().getValue());

                                                            if ( poTmp==null ) {
                                                                continue;
                                                            }
                                                        }
                                                    }
                                                }
                                            } else {
                                                poTmp = po.findProcessOperationForOperationNumberAndWafer( ObjectIdentifier.fetchValue(mainPDID),
                                                        correspondingOperaionNumbers.get(poCnt),
                                                        ObjectIdentifier.fetchValue(dcItem.getWaferID()));

                                                if ( poTmp==null ) {
                                                    //----------------------------------------------------
                                                    //   Corresponding operation is specified in SM,
                                                    //   however its operation's PO is not existing.
                                                    //   That means the corresponding operation is NOT
                                                    //   processed (using Locate Func.).
                                                    //   In this case, current PO can NOT be used for
                                                    //   the inhibit action.
                                                    //----------------------------------------------------
                                                    continue;
                                                }
                                            }
                                        } else {
                                            poTmp=baseCoreFactory.getBO(CimProcessOperation.class, effectedLots.get(0).getProcessOperation());
                                            Validations.check(poTmp==null,retCodeConfig.getNotFoundProcessOperation(),effectedLots.get(0).getProcessOperation(),
                                                    effectedLots.get(0).getLotID().getValue());
                                            if (ObjectIdentifier.isNotEmptyWithValue(dcItem.getWaferID())) {
                                                List<ProcessDTO.PosProcessWafer> strProcessWaferSeq = poTmp.getProcessWafers();

                                                int nWaferLen = CimArrayUtils.getSize(strProcessWaferSeq);
                                                Boolean bWaferFoundFlag = FALSE;
                                                if ( nWaferLen > 0 ) {
                                                    for ( int w_i = 0 ; w_i < nWaferLen ; w_i++ ) {
                                                        if ( CimStringUtils.equals( strProcessWaferSeq.get(w_i).getWaferID(), dcItem.getWaferID().getValue() )) {
                                                            bWaferFoundFlag = TRUE;
                                                        }
                                                    }
                                                    if ( !bWaferFoundFlag ) {
                                                        poTmp = po.findProcessOperationForOperationNumberAndWafer( ObjectIdentifier.fetchValue(mainPDID),
                                                                correspondingOperaionNumbers.get(poCnt),
                                                                dcItem.getWaferID().getValue());

                                                        if ( poTmp==null ) {
                                                            continue;
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        //------------------------------------------//
                                        // Get requestOtherFabFlag                  //
                                        //------------------------------------------//
                                        corrFabID = poTmp.getFabID();

                                        if( CimStringUtils.length(corrFabID) == 0 ) {
                                        }

                                        if( CimStringUtils.length(currentFabID) != 0 && CimStringUtils.length(corrFabID) != 0 &&
                                                !CimStringUtils.equals(currentFabID, corrFabID) ) {
                                            requestOtherFabFlag = TRUE;
                                        }
                                    } else {
                                        /*
                                         Corresponding operation is not specified in SM,
                                         In this case, current PO can be used for
                                         the inhibit action.
                                         */
                                        effectedLots.get(0).setProcessOperation(po.getPrimaryKey());
                                        poTmp = po;
                                    }
                                }

                                if (requestOtherFabFlag) {
                                    // Get correspondingPO information, processOperation_Info_GetDR
                                    Inputs.ObjProcessOperationInfoGetDrIn param = new Inputs.ObjProcessOperationInfoGetDrIn();
                                    param.setPoObj(effectedLots.get(0).getProcessOperation());
                                    processOperationInfo = processMethod.processOperationInfoGetDr(objCommon, param);
                                }
                                if (equipmentHoldFlag.get()) {
                                    if (!requestOtherFabFlag) {
                                        log.debug("startLotActionListEffectSpecCheck(): correspondingPO is currentFab");
                                        CimMachine equipment = poTmp.getAssignedMachine();
                                        if (equipment != null) {
                                            String machineID = equipment.getIdentifier();
                                            findFlag.set(false);
                                            /*
                                             Check equipment_hold_inhibition is already registed or not.
                                             If only not existed, entry new inhibition.
                                             */
                                            log.debug("startLotActionListEffectSpecCheck(): Check equipment_hold_inhibition is already registed or not");
                                            List<Infos.EntityInhibitDetailAttributes> entityInhibitions = retVal.getEntityInhibitions();
                                            if (!CimArrayUtils.isEmpty(entityInhibitions)) {
                                                for (Infos.EntityInhibitDetailAttributes entityInhibition : entityInhibitions) {
                                                    if (CimStringUtils.equals(entityInhibition.getEntities().get(0).getClassName(), BizConstant.SP_INHIBITCLASSID_EQUIPMENT)
                                                            && CimStringUtils.equals(entityInhibition.getEntities().get(0).getObjectID().getValue(), machineID)) {
                                                        findFlag.set(true);
                                                        break;
                                                    }
                                                }
                                            }
                                            if (!findFlag.get()) {
                                                //Check FRENTINHBT for entitiy inhibit
                                                //entityInhibit_attributes_GetDR__101
                                                Params.MfgRestrictListInqParams mfgRestrictListInqParams = prepareMfgRestrictListInqParams(ObjectIdentifier.build(equipment.getIdentifier(), equipment.getPrimaryKey()), lotInCassette.getLotID().getValue(), controlJobID,
                                                        BizConstant.SP_INHIBITCLASSID_EQUIPMENT, BizConstant.SP_REASON_SPECOVERINHIBIT);
                                                mfgRestrictListInqParams.setSearchCondition(new SearchCondition());
                                                List<Infos.EntityInhibitDetailInfo> entityInhibitInfoList = constraintMethod.constraintAttributesGetDR(objCommon, mfgRestrictListInqParams.getEntityInhibitDetailAttributes(), mfgRestrictListInqParams.getEntityInhibitReasonDetailInfoFlag());
                                                if (entityInhibitInfoList != null && !CimArrayUtils.isEmpty(entityInhibitInfoList)) {
                                                    findFlag.set(true);
                                                    registeredFlag.set(true);
                                                }

                                                //set out retVal
                                                setOutData(objCommon, retVal, ObjectIdentifier.build(equipment.getIdentifier(), equipment.getPrimaryKey()), BizConstant.SP_INHIBITCLASSID_EQUIPMENT, BizConstant.SP_REASON_SPECOVERINHIBIT);
                                            }
                                            findFlag.set(false);
                                            Results.DCActionLotResult dcActionLotResult = dcActionLotResultList.get(dcActionLotIndex);
                                            resultInfoProcess(registeredFlag, findFlag, dcActionLotResult, actionResultInfoList, ObjectIdentifier.build(equipment.getIdentifier(), equipment.getPrimaryKey()),
                                                    effectedLot.getProcessOperation(), effectedLot.getLotID(), monitorLotFlag, dcDef, BizConstant.SP_ACTIONCODE_EQUIPMENTHOLD, BizConstant.SP_INHIBITCLASSID_EQUIPMENT);
                                        }
                                    } else if (!ObjectIdentifier.isEmptyWithValue(processOperationInfo.getAsgnEqpID())) {
                                        log.debug("startLotActionListEffectSpecCheck(): correspondingPO is other Fab. eqp hold request to other Fab.");
                                        findFlag.set(false);
                                        List<Infos.EntityInhibitAttributesWithFabInfo> entityInhibitionsWithFabInfoList = retVal.getEntityInhibitionsWithFabInfoList();
                                        if (!CimArrayUtils.isEmpty(entityInhibitionsWithFabInfoList)) {
                                            for (Infos.EntityInhibitAttributesWithFabInfo entityInhibitAttributesWithFabInfo : entityInhibitionsWithFabInfoList) {
                                                List<Infos.EntityIdentifier> entities = entityInhibitAttributesWithFabInfo.getEntityInhibitAttributes().getEntities();
                                                if (CimArrayUtils.isEmpty(entities)) {
                                                    continue;
                                                }
                                                if (CimStringUtils.equals(entities.get(0).getClassName(), BizConstant.SP_INHIBITCLASSID_EQUIPMENT)
                                                        && ObjectIdentifier.equalsWithValue(entities.get(0).getObjectID(), processOperationInfo.getAsgnEqpID())) {
                                                    findFlag.set(true);
                                                    break;
                                                }
                                            }
                                        }
                                        if (!findFlag.get()) {
                                            if (entityInhibitionsWithFabInfoList == null) {
                                                entityInhibitionsWithFabInfoList = new ArrayList<>();
                                                retVal.setEntityInhibitionsWithFabInfoList(entityInhibitionsWithFabInfoList);
                                            }
                                            Infos.EntityInhibitAttributesWithFabInfo entityInhibitAttributesWithFabInfo = new Infos.EntityInhibitAttributesWithFabInfo();
                                            entityInhibitionsWithFabInfoList.add(entityInhibitAttributesWithFabInfo);
                                            entityInhibitAttributesWithFabInfo.setFabID(corrFabID);

                                            Infos.EntityInhibitAttributes entityInhibitAttributes = new Infos.EntityInhibitAttributes();
                                            entityInhibitAttributesWithFabInfo.setEntityInhibitAttributes(entityInhibitAttributes);
                                            entityInhibitAttributes.setEntities(new ArrayList<>());
                                            Infos.EntityIdentifier entityIdentifier = new Infos.EntityIdentifier();
                                            entityInhibitAttributes.getEntities().add(entityIdentifier);
                                            entityIdentifier.setClassName(BizConstant.SP_INHIBITCLASSID_EQUIPMENT);
                                            entityIdentifier.setObjectID(processOperationInfo.getAsgnEqpID());
                                            entityIdentifier.setAttribution(BizConstant.EMPTY);

                                            entityInhibitAttributes.setSubLotTypes(new ArrayList<>());

                                            entityInhibitAttributes.setStartTimeStamp(objCommon.getTimeStamp().getReportTimeStamp().toString());
                                            entityInhibitAttributes.setEndTimeStamp(BizConstant.SP_TIMESTAMP_NIL_OBJECT_STRING);
                                            entityInhibitAttributes.setReasonCode(BizConstant.SP_REASON_SPECOVERINHIBIT);
                                            entityInhibitAttributes.setOwnerID(objCommon.getUser().getUserID());
                                            entityInhibitAttributes.setClaimedTimeStamp(objCommon.getTimeStamp().getReportTimeStamp().toString());
                                        }
                                        findFlag.set(false);
                                        Results.DCActionLotResult dcActionLotResult = dcActionLotResultList.get(dcActionLotIndex);
                                        List<Infos.DCActionResultInfo> dcActionResultInfoList = dcActionLotResult.getDcActionResultInfo();
                                        if (!CimArrayUtils.isEmpty(dcActionResultInfoList)) {
                                            for (Infos.DCActionResultInfo dcActionResultInfo : dcActionResultInfoList) {
                                                List<Infos.EntityIdentifier> entities = dcActionResultInfo.getEntities();
                                                if (CimArrayUtils.getSize(entities) != 1) {
                                                    continue;
                                                }
                                                boolean findFlagCheck = CimStringUtils.equals(entities.get(0).getClassName(), BizConstant.SP_INHIBITCLASSID_EQUIPMENT)
                                                        && ObjectIdentifier.equalsWithValue(entities.get(0).getObjectID(), processOperationInfo.getAsgnEqpID())
                                                        && CimStringUtils.equals(dcActionResultInfo.getReasonCode(), BizConstant.SP_REASON_SPECOVERINHIBIT)
                                                        && CimStringUtils.equals(dcActionResultInfo.getCorrespondingObjRefPO(), effectedLots.get(0).getProcessOperation())
                                                        && CimObjectUtils.equals(dcActionResultInfo.getLotID(), effectedLot.getLotID());
                                                if (findFlagCheck) {
                                                    findFlag.set(true);
                                                    break;
                                                }
                                            }
                                        }
                                        if (!findFlag.get()) {
                                            //Add ActionResultInfo
                                            if (dcActionResultInfoList == null) {
                                                dcActionLotResult.setDcActionResultInfo(new ArrayList<>());
                                                dcActionResultInfoList = dcActionLotResult.getDcActionResultInfo();
                                            }
                                            addActionResultInfo(dcActionResultInfoList, effectedLot.getLotID(), effectedLots.get(0).getProcessOperation(), monitorLotFlag,
                                                    dcDef, processOperationInfo.getAsgnEqpID(), BizConstant.SP_ACTIONCODE_EQUIPMENTHOLD, BizConstant.SP_INHIBITCLASSID_EQUIPMENT);
                                        }
                                    }
                                }
                                if (recipeHoldFlag.get()) {
                                    if (!requestOtherFabFlag) {
                                        CimMachineRecipe recipe = poTmp.getAssignedMachineRecipe();
                                        if (recipe != null) {
                                            String recipeID = recipe.getIdentifier();
                                            findFlag.set(false);
                                            // Check recipe_hold_inhibition is already registed or not. If only not existed, entry new inhibition.
                                            List<Infos.EntityInhibitDetailAttributes> entityInhibitions = retVal.getEntityInhibitions();
                                            if (!CimArrayUtils.isEmpty(entityInhibitions)) {
                                                for (Infos.EntityInhibitDetailAttributes entityInhibition : entityInhibitions) {
                                                    if (CimStringUtils.equals(entityInhibition.getEntities().get(0).getClassName(), BizConstant.SP_INHIBITCLASSID_MACHINERECIPE)
                                                            && CimStringUtils.equals(entityInhibition.getEntities().get(0).getObjectID().getValue(), recipeID)) {
                                                        findFlag.set(true);
                                                        break;
                                                    }
                                                }
                                            }
                                            if (!findFlag.get()) {
                                                //Check FRENTINHBT for entitiy inhibit
                                                Params.MfgRestrictListInqParams mfgRestrictListInqParams = prepareMfgRestrictListInqParams(ObjectIdentifier.build(recipe.getIdentifier(), recipe.getPrimaryKey()), lotInCassette.getLotID().getValue(), controlJobID,
                                                        BizConstant.SP_INHIBITCLASSID_MACHINERECIPE, BizConstant.SP_REASON_SPECOVERINHIBIT);

                                                List<Infos.EntityInhibitDetailInfo> entityInhibitInfoList = constraintMethod.constraintAttributesGetDR(objCommon, mfgRestrictListInqParams.getEntityInhibitDetailAttributes(), mfgRestrictListInqParams.getEntityInhibitReasonDetailInfoFlag());

                                                if (entityInhibitInfoList != null && !CimArrayUtils.isEmpty(entityInhibitInfoList)) {
                                                    findFlag.set(true);
                                                    registeredFlag.set(true);
                                                }

                                                //set out retVal
                                                setOutData(objCommon, retVal, ObjectIdentifier.build(recipe.getIdentifier(), recipe.getPrimaryKey()), BizConstant.SP_INHIBITCLASSID_MACHINERECIPE, BizConstant.SP_REASON_SPECOVERINHIBIT);
                                            }
                                            findFlag.set(false);
                                            Results.DCActionLotResult dcActionLotResult = dcActionLotResultList.get(dcActionLotIndex);
                                            resultInfoProcess(registeredFlag, findFlag, dcActionLotResult, actionResultInfoList, ObjectIdentifier.build(recipe.getIdentifier(), recipe.getPrimaryKey()),
                                                    effectedLot.getProcessOperation(), effectedLot.getLotID(), monitorLotFlag, dcDef, BizConstant.SP_ACTIONCODE_RECIPEHOLD, BizConstant.SP_INHIBITCLASSID_MACHINERECIPE);
                                        }
                                    }
                                }
                                if (routeHoldFlag.get()) {
                                    if (!requestOtherFabFlag) {
                                        CimProcessDefinition tmpMainPD = poTmp.getMainProcessDefinition();
                                        if (mainPD != null) {
                                            String tmpMainPDID = mainPD.getIdentifier();
                                            findFlag.set(false);
                                            //Check route_hold_inhibition is already registed or not. If only not existed, entry new inhibition.
                                            List<Infos.EntityInhibitDetailAttributes> entityInhibitions = retVal.getEntityInhibitions();
                                            if (!CimArrayUtils.isEmpty(entityInhibitions)) {
                                                for (Infos.EntityInhibitDetailAttributes entityInhibition : entityInhibitions) {
                                                    if (CimStringUtils.equals(entityInhibition.getEntities().get(0).getClassName(), BizConstant.SP_INHIBITCLASSID_ROUTE)
                                                            && CimStringUtils.equals(entityInhibition.getEntities().get(0).getObjectID().getValue(), tmpMainPDID)
                                                            && CimStringUtils.equals(entityInhibition.getEntities().get(0).getAttribution(), BizConstant.EMPTY)) {
                                                        findFlag.set(true);
                                                        break;
                                                    }
                                                }
                                            }
                                            if (!findFlag.get()) {
                                                //Check FRENTINHBT for entitiy inhibit
                                                Params.MfgRestrictListInqParams mfgRestrictListInqParams = prepareMfgRestrictListInqParams(ObjectIdentifier.build(tmpMainPD.getIdentifier(), tmpMainPD.getPrimaryKey()), lotInCassette.getLotID().getValue(), controlJobID,
                                                        BizConstant.SP_INHIBITCLASSID_ROUTE, BizConstant.SP_REASON_SPECOVERINHIBIT);

                                                List<Infos.EntityInhibitDetailInfo> entityInhibitInfoList = constraintMethod.constraintAttributesGetDR(objCommon, mfgRestrictListInqParams.getEntityInhibitDetailAttributes(), mfgRestrictListInqParams.getEntityInhibitReasonDetailInfoFlag());

                                                if (entityInhibitInfoList != null && !CimArrayUtils.isEmpty(entityInhibitInfoList)) {
                                                    findFlag.set(true);
                                                    registeredFlag.set(true);
                                                }

                                                //set out retVal
                                                setOutData(objCommon, retVal, ObjectIdentifier.build(tmpMainPD.getIdentifier(), tmpMainPD.getPrimaryKey()), BizConstant.SP_INHIBITCLASSID_ROUTE, BizConstant.SP_REASON_SPECOVERINHIBIT);
                                            }
                                            findFlag.set(false);
                                            Results.DCActionLotResult dcActionLotResult = dcActionLotResultList.get(dcActionLotIndex);
                                            resultInfoProcess(registeredFlag, findFlag, dcActionLotResult, actionResultInfoList, ObjectIdentifier.build(tmpMainPD.getIdentifier(), tmpMainPD.getPrimaryKey()),
                                                    effectedLot.getProcessOperation(), effectedLot.getLotID(), monitorLotFlag, dcDef, BizConstant.SP_ACTIONCODE_ROUTEHOLD, BizConstant.SP_INHIBITCLASSID_ROUTE);
                                        }
                                    } else {
                                        log.info("startLotActionListEffectSpecCheck(): correspondingPO is other Fab. route hold request to other Fab.");
                                        findFlag.set(false);
                                        List<Infos.EntityInhibitAttributesWithFabInfo> entityInhibitionsWithFabInfoList = retVal.getEntityInhibitionsWithFabInfoList();
                                        if (!CimArrayUtils.isEmpty(entityInhibitionsWithFabInfoList)) {
                                            for (Infos.EntityInhibitAttributesWithFabInfo entityInhibitAttributesWithFabInfo : entityInhibitionsWithFabInfoList) {
                                                Infos.EntityIdentifier entityIdentifier = entityInhibitAttributesWithFabInfo.getEntityInhibitAttributes().getEntities().get(0);
                                                if (CimStringUtils.equals(entityIdentifier.getClassName(), BizConstant.SP_INHIBITCLASSID_ROUTE)
                                                        && CimObjectUtils.equals(entityIdentifier.getObjectID(), processOperationInfo.getMainPDID())
                                                        && CimStringUtils.equals(entityIdentifier.getAttribution(), BizConstant.EMPTY)) {
                                                    findFlag.set(true);
                                                    break;
                                                }
                                            }
                                        }
                                        if (!findFlag.get()) {
                                            List<Infos.EntityInhibitAttributesWithFabInfo> tmpEntityInhibitionsWithFabInfoList = retVal.getEntityInhibitionsWithFabInfoList();
                                            if (tmpEntityInhibitionsWithFabInfoList == null) {
                                                tmpEntityInhibitionsWithFabInfoList = new ArrayList<>();
                                                retVal.setEntityInhibitionsWithFabInfoList(tmpEntityInhibitionsWithFabInfoList);
                                            }
                                            Infos.EntityInhibitAttributesWithFabInfo entityInhibitAttributesWithFabInfo = new Infos.EntityInhibitAttributesWithFabInfo();
                                            tmpEntityInhibitionsWithFabInfoList.add(entityInhibitAttributesWithFabInfo);
                                            entityInhibitAttributesWithFabInfo.setFabID(corrFabID);

                                            Infos.EntityInhibitAttributes entityInhibitAttributes = new Infos.EntityInhibitAttributes();
                                            entityInhibitAttributesWithFabInfo.setEntityInhibitAttributes(entityInhibitAttributes);
                                            entityInhibitAttributes.setEntities(new ArrayList<>());
                                            Infos.EntityIdentifier entityIdentifier = new Infos.EntityIdentifier();
                                            entityInhibitAttributes.getEntities().add(entityIdentifier);
                                            entityIdentifier.setClassName(BizConstant.SP_INHIBITCLASSID_ROUTE);
                                            entityIdentifier.setObjectID(processOperationInfo.getMainPDID());
                                            entityIdentifier.setAttribution(BizConstant.EMPTY);

                                            entityInhibitAttributes.setSubLotTypes(new ArrayList<>());

                                            entityInhibitAttributes.setStartTimeStamp(objCommon.getTimeStamp().getReportTimeStamp().toString());
                                            entityInhibitAttributes.setEndTimeStamp(BizConstant.SP_TIMESTAMP_NIL_OBJECT_STRING);
                                            entityInhibitAttributes.setReasonCode(BizConstant.SP_REASON_SPECOVERINHIBIT);
                                            entityInhibitAttributes.setOwnerID(objCommon.getUser().getUserID());
                                            entityInhibitAttributes.setClaimedTimeStamp(objCommon.getTimeStamp().getReportTimeStamp().toString());
                                        }
                                        findFlag.set(false);
                                        Results.DCActionLotResult dcActionLotResult = dcActionLotResultList.get(dcActionLotIndex);
                                        List<Infos.DCActionResultInfo> dcActionResultInfoList = dcActionLotResult.getDcActionResultInfo();
                                        if (!CimArrayUtils.isEmpty(dcActionResultInfoList)) {
                                            for (Infos.DCActionResultInfo dcActionResultInfo : dcActionResultInfoList) {
                                                List<Infos.EntityIdentifier> entities = dcActionResultInfo.getEntities();
                                                if (CimArrayUtils.getSize(entities) != 1) {
                                                    continue;
                                                }
                                                boolean findFlagCheck = CimStringUtils.equals(entities.get(0).getClassName(), BizConstant.SP_INHIBITCLASSID_ROUTE)
                                                        && ObjectIdentifier.equalsWithValue(entities.get(0).getObjectID(), processOperationInfo.getMainPDID())
                                                        && CimStringUtils.equals(dcActionResultInfo.getReasonCode(), BizConstant.SP_REASON_SPECOVERINHIBIT)
                                                        && CimStringUtils.equals(dcActionResultInfo.getCorrespondingObjRefPO(), effectedLots.get(0).getProcessOperation())
                                                        && CimStringUtils.equals(dcActionResultInfo.getLotID().getValue(), effectedLot.getLotID().getValue());
                                                if (findFlagCheck) {
                                                    findFlag.set(true);
                                                    break;
                                                }
                                            }
                                        }
                                        if (!findFlag.get()) {
                                            //Add ActionResultInfo
                                            if (dcActionResultInfoList == null) {
                                                dcActionLotResult.setDcActionResultInfo(new ArrayList<>());
                                                dcActionResultInfoList = dcActionLotResult.getDcActionResultInfo();
                                            }
                                            addActionResultInfo(dcActionResultInfoList, effectedLot.getLotID(), effectedLots.get(0).getProcessOperation(), monitorLotFlag,
                                                    dcDef, processOperationInfo.getMainPDID(), BizConstant.SP_ACTIONCODE_ROUTEHOLD, BizConstant.SP_INHIBITCLASSID_ROUTE);
                                        }
                                    }
                                }
                                if (processHoldFlag.get()) {
                                    if (!requestOtherFabFlag) {
                                        CimProcessDefinition tmpMainPD = poTmp.getMainProcessDefinition();
                                        if (mainPD != null) {
                                            String tmpMainPDID = mainPD.getIdentifier();
                                            findFlag.set(false);
                                            //Check route_hold_inhibition is already registed or not. If only not existed, entry new inhibition.
                                            List<Infos.EntityInhibitDetailAttributes> entityInhibitions = retVal.getEntityInhibitions();
                                            if (!CimArrayUtils.isEmpty(entityInhibitions)) {
                                                for (Infos.EntityInhibitDetailAttributes entityInhibition : entityInhibitions) {
                                                    if (CimStringUtils.equals(entityInhibition.getEntities().get(0).getClassName(), BizConstant.SP_INHIBITCLASSID_OPERATION)
                                                            && CimStringUtils.equals(entityInhibition.getEntities().get(0).getObjectID().getValue(), tmpMainPDID)
                                                            && CimStringUtils.equals(entityInhibition.getEntities().get(0).getAttribution(), BizConstant.EMPTY)) {
                                                        findFlag.set(true);
                                                        break;
                                                    }
                                                }
                                            }
                                            if (!findFlag.get()) {
                                                //Check FRENTINHBT for entitiy inhibit
                                                Params.MfgRestrictListInqParams mfgRestrictListInqParams = prepareMfgRestrictListInqParams(ObjectIdentifier.build(tmpMainPD.getIdentifier(), tmpMainPD.getPrimaryKey()), lotInCassette.getLotID().getValue(), controlJobID,
                                                        BizConstant.SP_INHIBITCLASSID_OPERATION, BizConstant.SP_REASON_SPECOVERINHIBIT);

                                                List<Infos.EntityInhibitDetailInfo> entityInhibitInfoList = constraintMethod.constraintAttributesGetDR(objCommon, mfgRestrictListInqParams.getEntityInhibitDetailAttributes(), mfgRestrictListInqParams.getEntityInhibitReasonDetailInfoFlag());
                                                if (entityInhibitInfoList != null && !CimArrayUtils.isEmpty(entityInhibitInfoList)) {
                                                    findFlag.set(true);
                                                    registeredFlag.set(true);
                                                }

                                                //set out retVal
                                                setOutData(objCommon, retVal, ObjectIdentifier.build(tmpMainPD.getIdentifier(), tmpMainPD.getPrimaryKey()), BizConstant.SP_INHIBITCLASSID_OPERATION, BizConstant.SP_REASON_SPECOVERINHIBIT,poTmp.getOperationNumber());
                                            }
                                            findFlag.set(false);
                                            Results.DCActionLotResult dcActionLotResult = dcActionLotResultList.get(dcActionLotIndex);
                                            resultInfoProcess(registeredFlag, findFlag, dcActionLotResult, actionResultInfoList, ObjectIdentifier.build(tmpMainPD.getIdentifier(), tmpMainPD.getPrimaryKey()),
                                                    effectedLot.getProcessOperation(), effectedLot.getLotID(), monitorLotFlag, dcDef, BizConstant.SP_ACTIONCODE_PROCESSHOLD, BizConstant.SP_INHIBITCLASSID_OPERATION,poTmp.getOperationNumber());
                                        }
                                    } else {
                                        log.info("startLotActionListEffectSpecCheck(): correspondingPO is other Fab. process hold request to other Fab.");
                                        findFlag.set(false);
                                        List<Infos.EntityInhibitAttributesWithFabInfo> entityInhibitionsWithFabInfoList = retVal.getEntityInhibitionsWithFabInfoList();
                                        if (!CimArrayUtils.isEmpty(entityInhibitionsWithFabInfoList)) {
                                            for (Infos.EntityInhibitAttributesWithFabInfo entityInhibitAttributesWithFabInfo : entityInhibitionsWithFabInfoList) {
                                                Infos.EntityIdentifier entityIdentifier = entityInhibitAttributesWithFabInfo.getEntityInhibitAttributes().getEntities().get(0);
                                                if (CimStringUtils.equals(entityIdentifier.getClassName(), BizConstant.SP_INHIBITCLASSID_OPERATION)
                                                        && CimObjectUtils.equals(entityIdentifier.getObjectID(), processOperationInfo.getMainPDID())
                                                        && CimStringUtils.equals(entityIdentifier.getAttribution(), BizConstant.EMPTY)) {
                                                    findFlag.set(true);
                                                    break;
                                                }
                                            }
                                        }
                                        if (!findFlag.get()) {
                                            if (entityInhibitionsWithFabInfoList == null) {
                                                entityInhibitionsWithFabInfoList = new ArrayList<>();
                                                retVal.setEntityInhibitionsWithFabInfoList(entityInhibitionsWithFabInfoList);
                                            }
                                            Infos.EntityInhibitAttributesWithFabInfo entityInhibitAttributesWithFabInfo = new Infos.EntityInhibitAttributesWithFabInfo();
                                            entityInhibitionsWithFabInfoList.add(entityInhibitAttributesWithFabInfo);
                                            entityInhibitAttributesWithFabInfo.setFabID(corrFabID);

                                            Infos.EntityInhibitAttributes entityInhibitAttributes = new Infos.EntityInhibitAttributes();
                                            entityInhibitAttributesWithFabInfo.setEntityInhibitAttributes(entityInhibitAttributes);
                                            entityInhibitAttributes.setEntities(new ArrayList<>());
                                            Infos.EntityIdentifier entityIdentifier = new Infos.EntityIdentifier();
                                            entityInhibitAttributes.getEntities().add(entityIdentifier);
                                            entityIdentifier.setClassName(BizConstant.SP_INHIBITCLASSID_OPERATION);
                                            entityIdentifier.setObjectID(processOperationInfo.getMainPDID());
                                            entityIdentifier.setAttribution(BizConstant.EMPTY);

                                            entityInhibitAttributes.setSubLotTypes(new ArrayList<>());

                                            entityInhibitAttributes.setStartTimeStamp(objCommon.getTimeStamp().getReportTimeStamp().toString());
                                            entityInhibitAttributes.setEndTimeStamp(BizConstant.SP_TIMESTAMP_NIL_OBJECT_STRING);
                                            entityInhibitAttributes.setReasonCode(BizConstant.SP_REASON_SPECOVERINHIBIT);
                                            entityInhibitAttributes.setOwnerID(objCommon.getUser().getUserID());
                                            entityInhibitAttributes.setClaimedTimeStamp(objCommon.getTimeStamp().getReportTimeStamp().toString());
                                        }
                                        findFlag.set(false);
                                        Results.DCActionLotResult dcActionLotResult = dcActionLotResultList.get(dcActionLotIndex);
                                        List<Infos.DCActionResultInfo> dcActionResultInfoList = dcActionLotResult.getDcActionResultInfo();
                                        if (!CimArrayUtils.isEmpty(dcActionResultInfoList)) {
                                            for (Infos.DCActionResultInfo dcActionResultInfo : dcActionResultInfoList) {
                                                List<Infos.EntityIdentifier> entities = dcActionResultInfo.getEntities();
                                                if (CimArrayUtils.getSize(entities) != 1) {
                                                    continue;
                                                }
                                                boolean findFlagCheck = CimStringUtils.equals(entities.get(0).getClassName(), BizConstant.SP_INHIBITCLASSID_OPERATION)
                                                        && CimObjectUtils.equals(entities.get(0).getObjectID(), processOperationInfo.getMainPDID())
                                                        && CimStringUtils.equals(dcActionResultInfo.getReasonCode(), BizConstant.SP_REASON_SPECOVERINHIBIT)
                                                        && CimStringUtils.equals(dcActionResultInfo.getCorrespondingObjRefPO(), effectedLots.get(0).getProcessOperation())
                                                        && CimStringUtils.equals(dcActionResultInfo.getLotID().getValue(), effectedLot.getLotID().getValue());
                                                if (findFlagCheck) {
                                                    findFlag.set(true);
                                                    break;
                                                }
                                            }
                                        }
                                        if (!findFlag.get()) {
                                            //Add ActionResultInfo
                                            if (dcActionResultInfoList == null) {
                                                dcActionLotResult.setDcActionResultInfo(new ArrayList<>());
                                                dcActionResultInfoList = dcActionLotResult.getDcActionResultInfo();
                                            }
                                            addActionResultInfo(dcActionResultInfoList, effectedLot.getLotID(), effectedLots.get(0).getProcessOperation(), monitorLotFlag,
                                                    dcDef, processOperationInfo.getMainPDID(), BizConstant.SP_ACTIONCODE_PROCESSHOLD, BizConstant.SP_INHIBITCLASSID_OPERATION);
                                        }
                                    }
                                }
                                /*
                                Make strMessageAttributes of out parameter.
                                check actionCode of all strDCDef.dataCollectoinItemName.
                                    ( actionCode are filled only when specCheckResult is SP_ActionCode_Mail. )
                                 */
                                if (mailFlag.get()) {
                                    if (!requestOtherFabFlag || effectedLots.indexOf(effectedLot) == 0) {
                                        log.info("startLotActionListEffectSpecCheck(): correspondingPO is current Fab");
                                        CimMachine machine = poTmp.getAssignedMachine();
                                        CimProcessDefinition tmpMainPD = poTmp.getMainProcessDefinition();
                                        String operationNo = poTmp.getOperationNumber();
                                        ObjectIdentifier eqpID = ObjectIdentifier.build(machine.getIdentifier(), machine.getPrimaryKey());
                                        ObjectIdentifier routeID = ObjectIdentifier.build(tmpMainPD.getIdentifier(), tmpMainPD.getPrimaryKey());
                                        findFlag.set(false);
                                        /*
                                        Check if strMessageAttributes is already registed with the same lotID & reasonCode & routeID & operationNo or not.
                                        If only not existed, entry new inhibition.
                                         */
                                        List<Infos.MessageAttributes> messageList = retVal.getMessageList();
                                        if (!CimArrayUtils.isEmpty(messageList)) {
                                            for (Infos.MessageAttributes messageAttributes : messageList) {
                                                if (CimStringUtils.equals(messageAttributes.getReasonCode(), reasonCode)
                                                        && CimStringUtils.equals(messageAttributes.getLotID().getValue(), effectedLot.getLotID().getValue())
                                                        && CimStringUtils.equals(messageAttributes.getRouteID().getValue(), routeID.getValue())
                                                        && CimStringUtils.equals(messageAttributes.getOperationNumber(), operationNo)) {
                                                    findFlag.set(true);
                                                }
                                            }
                                        }
                                        if (!findFlag.get()) {
                                            //set messageList
                                            if (messageList == null) {
                                                messageList = new ArrayList<>();
                                                retVal.setMessageList(messageList);
                                            }
                                            Infos.MessageAttributes messageAttributes = new Infos.MessageAttributes();
                                            messageList.add(messageAttributes);
                                            messageAttributes.setMessageID(ObjectIdentifier.build(BizConstant.SP_MESSAGEID_SPECCHECKOVER, null));
                                            messageAttributes.setLotID(effectedLot.getLotID());
                                            messageAttributes.setLotStatus(BizConstant.EMPTY);
                                            messageAttributes.setEquipmentID(eqpID);
                                            messageAttributes.setRouteID(routeID);
                                            messageAttributes.setOperationNumber(operationNo);
                                            messageAttributes.setReasonCode(messageReason);
                                            messageAttributes.setMessageText(BizConstant.EMPTY);

                                            //Add ActionResultInfo
                                            Results.DCActionLotResult dcActionLotResult = dcActionLotResultList.get(dcActionLotIndex);
                                            List<Infos.DCActionResultInfo> dcActionResultInfoList = dcActionLotResult.getDcActionResultInfo();
                                            if (dcActionResultInfoList == null) {
                                                dcActionResultInfoList = new ArrayList<>();
                                                dcActionLotResult.setDcActionResultInfo(dcActionResultInfoList);
                                            }
                                            Infos.DCActionResultInfo dcActionResultInfo = new Infos.DCActionResultInfo();
                                            dcActionResultInfoList.add(dcActionResultInfo);
                                            setDCActionResultInfoWithOutEntities(dcActionResultInfo, effectedLot.getLotID(), effectedLots.get(0).getProcessOperation(), monitorLotFlag, dcDef, BizConstant.SP_ACTIONCODE_MAIL);
                                            dcActionResultInfo.setEntities(new ArrayList<>());
                                        }
                                    } else {
                                        findLotFlag = false;
                                        findMsgFlag = false;
                                        int mailIndex = 0;
                                        if (!CimArrayUtils.isEmpty(interFabMonitorGroupActionInfoList)) {
                                            List<Infos.MonitoredLotMailInfo> monitoredLotMailInfoList = interFabMonitorGroupActionInfoList.get(interFabMonIdx).getMonitoredLotMailInfoList();
                                            if (!CimArrayUtils.isEmpty(monitoredLotMailInfoList)) {
                                                for (Infos.MonitoredLotMailInfo monitoredLotMailInfo : monitoredLotMailInfoList) {
                                                    if (CimStringUtils.equals(monitoredLotMailInfo.getLotID().getValue(), effectedLot.getLotID().getValue())
                                                            && ObjectIdentifier.equalsWithValue(monitoredLotMailInfo.getRouteID(), processOperationInfo.getMainPDID())
                                                            && CimStringUtils.equals(monitoredLotMailInfo.getOpeNo(), processOperationInfo.getOpeNo())) {
                                                        mailIndex = monitoredLotMailInfoList.indexOf(monitoredLotMailInfo);
                                                        findLotFlag = true;
                                                        break;
                                                    }
                                                }
                                            }
                                            if (!findLotFlag) {
                                                Infos.MonitoredLotMailInfo monitoredLotMailInfo = new Infos.MonitoredLotMailInfo();
                                                monitoredLotMailInfoList.add(monitoredLotMailInfo);
                                                monitoredLotMailInfo.setLotID(effectedLot.getLotID());
                                                monitoredLotMailInfo.setEquipmentID(processOperationInfo.getAsgnEqpID());
                                                monitoredLotMailInfo.setRouteID(processOperationInfo.getMainPDID());
                                                monitoredLotMailInfo.setOpeNo(processOperationInfo.getOpeNo());

                                                //Add ActionResultInfo
                                                Results.DCActionLotResult dcActionLotResult = dcActionLotResultList.get(dcActionLotIndex);
                                                List<Infos.DCActionResultInfo> dcActionResultInfoList = dcActionLotResult.getDcActionResultInfo();
                                                if (dcActionResultInfoList == null) {
                                                    dcActionResultInfoList = new ArrayList<>();
                                                    dcActionLotResult.setDcActionResultInfo(dcActionResultInfoList);
                                                }
                                                Infos.DCActionResultInfo dcActionResultInfo = new Infos.DCActionResultInfo();
                                                dcActionResultInfoList.add(dcActionResultInfo);
                                                setDCActionResultInfoWithOutEntities(dcActionResultInfo, effectedLot.getLotID(), effectedLots.get(0).getProcessOperation(), monitorLotFlag, dcDef, BizConstant.SP_ACTIONCODE_MAIL);
                                                dcActionResultInfo.setEntities(new ArrayList<>());
                                            }
                                            //check same message exist?
                                            if (!CimArrayUtils.isEmpty(monitoredLotMailInfoList)) {
                                                Infos.MonitoredLotMailInfo monitoredLotMailInfo = monitoredLotMailInfoList.get(mailIndex);
                                                List<String> reasonCodeSeq = monitoredLotMailInfo.getReasonCodeSeq();
                                                if (!CimArrayUtils.isEmpty(reasonCodeSeq)) {
                                                    for (String item : reasonCodeSeq) {
                                                        if (CimStringUtils.equals(item, reasonCode)) {
                                                            findMsgFlag = true;
                                                            break;
                                                        }
                                                    }
                                                }

                                                if (!findMsgFlag) {
                                                    List<ObjectIdentifier> messageIDSeq = monitoredLotMailInfo.getMessageIDSeq();
                                                    if (messageIDSeq == null) {
                                                        messageIDSeq = new ArrayList<>();
                                                        monitoredLotMailInfo.setMessageIDSeq(messageIDSeq);
                                                    }
                                                    messageIDSeq.add(ObjectIdentifier.build(BizConstant.SP_MESSAGEID_SPECCHECKOVER, null));

                                                    if (reasonCodeSeq == null) {
                                                        reasonCodeSeq = new ArrayList<>();
                                                        monitoredLotMailInfo.setReasonCodeSeq(reasonCodeSeq);
                                                    }
                                                    reasonCodeSeq.add(messageReason);
                                                }
                                            }
                                        }

                                    }
                                }
                                //Set Entity Inhibit Reason Detail Information, processOperation_Info_GetDR
                                Inputs.ObjProcessOperationInfoGetDrIn param = new Inputs.ObjProcessOperationInfoGetDrIn();
                                if (monitorLotFlag && requestOtherFabFlag) {
                                    param.setPoObj(effectedLots.get(0).getProcessOperation());
                                } else {
                                    param.setPoObj(effectedLot.getProcessOperation());
                                }
                                Infos.ProcessOperationInfo processOperationInfoGetDrOut = processMethod.processOperationInfoGetDr(objCommon, param);
                                List<Infos.EntityInhibitDetailAttributes> entityInhibitions = retVal.getEntityInhibitions();
                                if (entityInhibitions == null) {
                                    entityInhibitions = new ArrayList<>();
                                    retVal.setEntityInhibitions(entityInhibitions);
                                }
                                for (Infos.EntityInhibitDetailAttributes entityInhibition : entityInhibitions) {
                                    if (entityInhibition.getEntityInhibitReasonDetailInfos()!=null){
                                        continue;
                                    }
                                    List<Infos.EntityInhibitReasonDetailInfo> entityInhibitReasonDetailInfos = new ArrayList<>();
                                    entityInhibition.setEntityInhibitReasonDetailInfos(entityInhibitReasonDetailInfos);

                                    Infos.EntityInhibitReasonDetailInfo entityInhibitReasonDetailInfo = new Infos.EntityInhibitReasonDetailInfo();
                                    entityInhibitReasonDetailInfos.add(entityInhibitReasonDetailInfo);

                                    entityInhibitReasonDetailInfo.setRelatedLotID(lotInCassette.getLotID().getValue());
                                    entityInhibitReasonDetailInfo.setRelatedControlJobID(controlJobID);
                                    entityInhibitReasonDetailInfo.setRelatedFabID(BizConstant.EMPTY);
                                    entityInhibitReasonDetailInfo.setRelatedRouteID(processOperationInfoGetDrOut.getMainPDID().getValue());
                                    entityInhibitReasonDetailInfo.setRelatedProcessDefinitionID(processOperationInfoGetDrOut.getPdID().getValue());
                                    entityInhibitReasonDetailInfo.setRelatedOperationNumber(processOperationInfoGetDrOut.getOpeNo());
                                    entityInhibitReasonDetailInfo.setRelatedOperationPassCount(processOperationInfoGetDrOut.getPassCount() == null ? BizConstant.EMPTY : processOperationInfoGetDrOut.getPassCount().toString());
                                }
                                // For OTHER Fab
                                List<Infos.EntityInhibitAttributesWithFabInfo> entityInhibitionsWithFabInfoList = retVal.getEntityInhibitionsWithFabInfoList();
                                if (entityInhibitionsWithFabInfoList == null) {
                                    entityInhibitionsWithFabInfoList = new ArrayList<>();
                                    retVal.setEntityInhibitionsWithFabInfoList(entityInhibitionsWithFabInfoList);
                                }
                                for (Infos.EntityInhibitAttributesWithFabInfo entityInhibitAttributesWithFabInfo : entityInhibitionsWithFabInfoList) {
                                    List<Infos.EntityInhibitReasonDetailInfo> entityInhibitReasonDetailInfos = new ArrayList<>();
                                    entityInhibitAttributesWithFabInfo.setEntityInhibitReasonDetailInfos(entityInhibitReasonDetailInfos);

                                    Infos.EntityInhibitReasonDetailInfo entityInhibitReasonDetailInfo = new Infos.EntityInhibitReasonDetailInfo();
                                    entityInhibitReasonDetailInfos.add(entityInhibitReasonDetailInfo);

                                    entityInhibitReasonDetailInfo.setRelatedLotID(lotInCassette.getLotID().getValue());
                                    entityInhibitReasonDetailInfo.setRelatedControlJobID(controlJobID);
                                    entityInhibitReasonDetailInfo.setRelatedFabID(currentFabID);
                                    entityInhibitReasonDetailInfo.setRelatedRouteID(processOperationInfoGetDrOut.getMainPDID().getValue());
                                    entityInhibitReasonDetailInfo.setRelatedProcessDefinitionID(processOperationInfoGetDrOut.getPdID().getValue());
                                    entityInhibitReasonDetailInfo.setRelatedOperationNumber(processOperationInfoGetDrOut.getOpeNo());
                                    entityInhibitReasonDetailInfo.setRelatedOperationPassCount(processOperationInfoGetDrOut.getPassCount() == null ? BizConstant.EMPTY : processOperationInfoGetDrOut.getPassCount().toString());
                                }
                            }
                        }
                    }
                }
            }
        }
        retVal.setDcActionLotResultList(dcActionLotResultList);
        retVal.setInterFabMonitorGroupActionInfoList(interFabMonitorGroupActionInfoList);
        return retVal;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objectID            objectID
     * @param relatedLotID        relatedLotID
     * @param relatedControlJobID relatedControlJobID
     * @param className           className
     * @param reasonCode          reasonCode
     * @return MfgRestrictListInqParams
     * @author PlayBoy
     * @date 2018/11/13 15:39:01
     */
    private Params.MfgRestrictListInqParams prepareMfgRestrictListInqParams(ObjectIdentifier objectID, String relatedLotID, String relatedControlJobID, String className, String reasonCode) {
        String inhibitDcururationDays = StandardProperties.OM_CONSTRAINT_DURATION.getValue();

        Params.MfgRestrictListInqParams mfgRestrictListInqParams = new Params.MfgRestrictListInqParams();
        Infos.EntityInhibitDetailAttributes entityInhibitDetailAttributes = new Infos.EntityInhibitDetailAttributes();
        mfgRestrictListInqParams.setEntityInhibitDetailAttributes(entityInhibitDetailAttributes);
        mfgRestrictListInqParams.setEntityInhibitReasonDetailInfoFlag(false);

        entityInhibitDetailAttributes.setEntities(new ArrayList<>());
        Infos.EntityIdentifier entityIdentifier = new Infos.EntityIdentifier();
        entityIdentifier.setClassName(className);
        entityIdentifier.setObjectID(objectID);
        entityInhibitDetailAttributes.getEntities().add(entityIdentifier);

        entityInhibitDetailAttributes.setEntityInhibitReasonDetailInfos(new ArrayList<>());
        Infos.EntityInhibitReasonDetailInfo entityInhibitReasonDetailInfo = new Infos.EntityInhibitReasonDetailInfo();
        entityInhibitReasonDetailInfo.setRelatedLotID(relatedLotID);
        entityInhibitReasonDetailInfo.setRelatedControlJobID(relatedControlJobID);
        entityInhibitDetailAttributes.getEntityInhibitReasonDetailInfos().add(entityInhibitReasonDetailInfo);

        entityInhibitDetailAttributes.setEndTimeStamp(inhibitDcururationDays);
        entityInhibitDetailAttributes.setReasonCode(reasonCode);

        return mfgRestrictListInqParams;
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param dcActionResultInfoList dcActionResultInfoList
     * @param lotID                  lotID
     * @param correspondingObjRefPO  correspondingObjRefPO
     * @param monitorLotFlag         monitorLotFlag
     * @param dcDef                  dcDef
     * @param objectID               objectID
     * @author PlayBoy
     * @date 2018/11/13 14:26:00
     */
    private void addActionResultInfo(List<Infos.DCActionResultInfo> dcActionResultInfoList, ObjectIdentifier lotID, String correspondingObjRefPO, boolean monitorLotFlag,
                                     Infos.DataCollectionInfo dcDef, ObjectIdentifier objectID, String actionCode, String className,String...attr) {
        Infos.DCActionResultInfo dcActionResultInfo = new Infos.DCActionResultInfo();
        dcActionResultInfoList.add(dcActionResultInfo);

        setDCActionResultInfoWithOutEntities(dcActionResultInfo, lotID, correspondingObjRefPO, monitorLotFlag, dcDef, actionCode);

        dcActionResultInfo.setEntities(new ArrayList<>());
        Infos.EntityIdentifier entityIdentifier = new Infos.EntityIdentifier();
        dcActionResultInfo.getEntities().add(entityIdentifier);
        entityIdentifier.setClassName(className);
        entityIdentifier.setObjectID(objectID);
        entityIdentifier.setAttribution(BizConstant.EMPTY);
        if (CimArrayUtils.length(attr) !=0){
            entityIdentifier.setAttribution(attr[0]);
        }
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param dcActionResultInfo    dcActionResultInfo
     * @param lotID                 lotID
     * @param correspondingObjRefPO correspondingObjRefPO
     * @param monitorLotFlag        monitorLotFlag
     * @param dcDef                 dcDef
     * @return
     * @author PlayBoy
     * @date 2018/11/14 11:31:12
     */
    private void setDCActionResultInfoWithOutEntities(Infos.DCActionResultInfo dcActionResultInfo, ObjectIdentifier lotID, String correspondingObjRefPO, boolean monitorLotFlag,
                                                      Infos.DataCollectionInfo dcDef, String actionCode) {
        dcActionResultInfo.setLotID(lotID);
        dcActionResultInfo.setMonitorLotFlag(monitorLotFlag);
        dcActionResultInfo.setDcDefID(dcDef.getDataCollectionDefinitionID());
        dcActionResultInfo.setDcSpecID(dcDef.getDataCollectionSpecificationID());
        dcActionResultInfo.setCheckType(BizConstant.SP_ACTIONRESULT_CHECKTYPE_SPEC);
        dcActionResultInfo.setReasonCode(BizConstant.SP_REASON_SPECOVERINHIBIT);
        dcActionResultInfo.setActionCode(actionCode);
        dcActionResultInfo.setCorrespondingObjRefPO(correspondingObjRefPO);
        dcActionResultInfo.setBankID(BizConstant.EMPTY);
        dcActionResultInfo.setReworkRouteID(BizConstant.EMPTY);
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon  objCommon
     * @param data       data
     * @param objectID   objectID
     * @param className  className
     * @param reasonCode reasonCode
     * @author PlayBoy
     * @date 2018/11/13 18:47:31
     */
    private void setOutData(Infos.ObjCommon objCommon, Outputs.ObjStartLotActionListEffectSpecCheckOut data, ObjectIdentifier objectID, String className, String reasonCode,String... attr) {
        List<Infos.EntityInhibitDetailAttributes> entityInhibitionList = data.getEntityInhibitions() == null ? new ArrayList<>() : data.getEntityInhibitions();
        data.setEntityInhibitions(entityInhibitionList);
        Infos.EntityInhibitDetailAttributes inhibitDetailAttributes = new Infos.EntityInhibitDetailAttributes();
        entityInhibitionList.add(inhibitDetailAttributes);

        List<Infos.EntityIdentifier> entityIdentifiers = new ArrayList<>();
        Infos.EntityIdentifier entityIdentifier = new Infos.EntityIdentifier();
        entityIdentifier.setClassName(className);
        entityIdentifier.setObjectID(objectID);
        entityIdentifier.setAttribution(BizConstant.EMPTY);
        if (CimArrayUtils.length(attr) !=0){
            entityIdentifier.setAttribution(attr[0]);
        }
        entityIdentifiers.add(entityIdentifier);
        inhibitDetailAttributes.setEntities(entityIdentifiers);

        inhibitDetailAttributes.setSubLotTypes(new ArrayList<>());
        inhibitDetailAttributes.setStartTimeStamp(objCommon.getTimeStamp().getReportTimeStamp().toString());
        inhibitDetailAttributes.setEndTimeStamp(BizConstant.SP_TIMESTAMP_NIL_OBJECT_STRING);
        inhibitDetailAttributes.setReasonCode(reasonCode);
        inhibitDetailAttributes.setOwnerID(objCommon.getUser().getUserID());
        inhibitDetailAttributes.setClaimedTimeStamp(objCommon.getTimeStamp().getReportTimeStamp().toString());
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param registeredFlag        registeredFlag
     * @param findFlag              findFlag
     * @param dcActionLotResult     dcActionLotResult
     * @param actionResultInfoList  actionResultInfoList
     * @param objectID              objectID
     * @param correspondingObjRefPO correspondingObjRefPO
     * @param lotID                 lotID
     * @param monitorLotFlag        monitorLotFlag
     * @param dcDef                 dcDef
     * @param actionCode            actionCode
     * @param className             className
     * @author PlayBoy
     * @date 2018/11/13 19:19:20
     */
    private void resultInfoProcess(AtomicBoolean registeredFlag, AtomicBoolean findFlag, Results.DCActionLotResult dcActionLotResult, List<ProcessDTO.ActionResultInfo> actionResultInfoList, ObjectIdentifier objectID,
                                   String correspondingObjRefPO, ObjectIdentifier lotID, boolean monitorLotFlag, Infos.DataCollectionInfo dcDef, String actionCode, String className, String...attr) {
        if (!registeredFlag.get()) {
            List<Infos.DCActionResultInfo> dcActionResultInfoList = dcActionLotResult.getDcActionResultInfo();
            if (!CimArrayUtils.isEmpty(dcActionResultInfoList)) {
                for (Infos.DCActionResultInfo dcActionResultInfo : dcActionResultInfoList) {
                    List<Infos.EntityIdentifier> entities = dcActionResultInfo.getEntities();
                    if (CimArrayUtils.getSize(entities) == 1) {
                        boolean findFlagCheck = CimStringUtils.equals(entities.get(0).getClassName(), className)
                                && CimStringUtils.equals(entities.get(0).getObjectID().getValue(), objectID.getValue())
                                && CimStringUtils.equals(dcActionResultInfo.getReasonCode(), BizConstant.SP_REASON_SPECOVERINHIBIT)
                                && CimStringUtils.equals(dcActionResultInfo.getCorrespondingObjRefPO(), correspondingObjRefPO)
                                && CimStringUtils.equals(dcActionResultInfo.getLotID().getValue(), lotID.getValue());
                        if (findFlagCheck) {
                            findFlag.set(true);
                            break;
                        }
                    }
                }
            }
        } else {
            if (!CimArrayUtils.isEmpty(actionResultInfoList)) {
                for (ProcessDTO.ActionResultInfo actionResultInfo : actionResultInfoList) {
                    List<ProcessDTO.ActionEntityInfo> entities = actionResultInfo.getEntities();
                    if (CimArrayUtils.getSize(entities) == 1) {
                        boolean findFlagCheck = CimStringUtils.equals(entities.get(0).getClassName(), className)
                                && CimStringUtils.equals(entities.get(0).getObjectID().getValue(), objectID.getValue())
                                && CimStringUtils.equals(actionResultInfo.getReasonCode(), BizConstant.SP_REASON_SPECOVERINHIBIT)
                                && CimStringUtils.equals(actionResultInfo.getCorrespondingObjRefPO(), correspondingObjRefPO)
                                && CimStringUtils.equals(actionResultInfo.getLotID().getValue(), lotID.getValue());
                        if (findFlagCheck) {
                            findFlag.set(true);
                            break;
                        }
                    }
                }
            }
        }
        if (!findFlag.get()) {
            // Add ActionResultInfo
            List<Infos.DCActionResultInfo> dcActionResultInfoList = dcActionLotResult.getDcActionResultInfo();
            if (dcActionResultInfoList == null) {
                dcActionLotResult.setDcActionResultInfo(new ArrayList<>());
                dcActionResultInfoList = dcActionLotResult.getDcActionResultInfo();
            }
            addActionResultInfo(dcActionResultInfoList, lotID, correspondingObjRefPO, monitorLotFlag, dcDef, objectID, actionCode, className,attr);
        }
    }

    @Override
    public Outputs.ObjStartLotActionListEffectSPCCheckOut startLotActionListEffectSPCCheck(Infos.ObjCommon objCommon, List<Infos.SpcCheckLot> strSpcCheckLot, List<Infos.SpcIFParm> spcIFParmList, List<Infos.InterFabMonitorGroupActionInfo> interFabMonitorGroupActionInfoList, ObjectIdentifier equipmentID, List<Results.DCActionLotResult> dcActionLotResultList) {
        Outputs.ObjStartLotActionListEffectSPCCheckOut strStartLot_actionList_EffectSPCCheck_out = new Outputs.ObjStartLotActionListEffectSPCCheckOut();

        List<Infos.MonitoredLot> effectedLots = new ArrayList<>();

        boolean findFlag;
        int entity_count = 0;
        int msg_count = 0;
        int bank_move_count = 0;         //D4200115
        int rework_branch_count = 0;     //D4200115
        int mail_send_count = 0;         //D4200115
        int entity_countForOtherFab  = 0;     //DSIV00000214
        int p, msg_countPerMonLot    = 0;     //DSIV00000214
        boolean findLotFlag = false;           //DSIV00000214
        boolean findMsgFlag = false;           //DSIV00000214
        boolean findMonitoringLotFlag = false; //DSIV00000214
        boolean requestOtherFabFlag   = false; //DSIV00000214

        int eCnt = 0;                          //DSIV00001365
        int entity_count_keep = 0;             //DSIV00001365
        int entity_countForOtherFab_keep  = 0; //DSIV00001365
        int dcActionLot_count = CimArrayUtils.getSize(dcActionLotResultList);

        List<Infos.EntityInhibitAttributesWithFabInfo> entityInhibitAttributesWithFabInfosInit = new ArrayList<>();
        strStartLot_actionList_EffectSPCCheck_out.setEntityInhibitionsWithFabInfo(entityInhibitAttributesWithFabInfosInit);
        List<Infos.EntityInhibitDetailAttributes> strEntityInhibitionsInit = new ArrayList<>();
        strStartLot_actionList_EffectSPCCheck_out.setStrEntityInhibitions(strEntityInhibitionsInit);
        List<Infos.MessageAttributes> strMessageListInit = new ArrayList<>();
        strStartLot_actionList_EffectSPCCheck_out.setStrMessageList(strMessageListInit);
        List<Infos.MailSend> mailSendListInit = new ArrayList<>();
        strStartLot_actionList_EffectSPCCheck_out.setMailSendList(mailSendListInit);
        List<Infos.MailSendWithFabInfo> mailSendListWithFabInfoInit = new ArrayList<>();
        strStartLot_actionList_EffectSPCCheck_out.setMailSendListWithFabInfo(mailSendListWithFabInfoInit);

        if(!CimObjectUtils.isEmpty(strSpcCheckLot)){
            for (Infos.SpcCheckLot spcCheckLot: strSpcCheckLot){
                /**
                 * Task-311 add ocapNo for constraint memo
                 */
                String ocapMemo = spcCheckLot.getOcapNo();
                /*---------------------------*/
                /*   Omit Non-DataCollection */
                /*---------------------------*/
                ObjectIdentifier tempLotID = spcCheckLot.getLotID();
                int lenActionCode = CimArrayUtils.getSize(spcCheckLot.getSpcActionCode());
                if (lenActionCode == 0 ){
                    log.info("lenActionCode.length = 0");
                }
                //-----------------------------------------
                //  Get Lot ojbect
                //-----------------------------------------
                if (ObjectIdentifier.isEmptyWithValue(tempLotID)){
                    log.info("lotID is 0 Length: CONTINUE");
                    continue;
                }

                CimLot aPosLot = baseCoreFactory.getBO(CimLot.class,tempLotID);
                Validations.check(aPosLot == null, new OmCode(retCodeConfig.getNotFoundLot(), ObjectIdentifier.fetchValue(tempLotID)));
                CimProcessOperation aTmpProcessOperation;
                CimProcessOperation aPO;

                //------------------------------------------
                // Current PO or Previous PO ?
                //------------------------------------------
                Boolean strLot_CheckConditionForPO_out = lotMethod.lotCheckConditionForPO(objCommon,tempLotID);
                if (strLot_CheckConditionForPO_out){
                    //--------------------------------------------------------------------------
                    // Get PO from Current Operation.
                    //--------------------------------------------------------------------------
                    log.info("Get PO from the current Operation.");
                    aPO = aPosLot.getProcessOperation();
                }else {
                    //--------------------------------------------------------------------------
                    // Get PO from Previous Operation.
                    //--------------------------------------------------------------------------
                    log.info("Get PO from the previous Operation.");
                    aPO = aPosLot.getPreviousProcessOperation();
                }
                Validations.check(aPO == null, new OmCode(retCodeConfig.getNotFoundOperation(), ""));
                String currentFabID = aPO.getFabID();
                if (CimStringUtils.isEmpty(currentFabID)){
                    log.info("currentFabID is null");
                }
                //-------------------------------------------------------
                //  Set always the input Lot to effectedLots[0]
                //  even if lot is monitor lot or not.
                //-------------------------------------------------------
                Infos.MonitoredLot effectedLot = new Infos.MonitoredLot();
                effectedLot.setLotID(tempLotID);
                effectedLot.setProcessOperation(aPO.getPrimaryKey());
                effectedLots.clear();//size = 1
                effectedLots.add(effectedLot);

                String correspondingFabID = spcCheckLot.getProcessFabID();
                requestOtherFabFlag = spcCheckLot.getRequestOtherFabFlag();

                int interFabMonIdx = 0 ;
                int lot_countForMailOtherFab = 0;
                Infos.ProcessOperationInfo strProcessOperationInfo = new Infos.ProcessOperationInfo();

                if ( requestOtherFabFlag){
                    findMonitoringLotFlag = false;
                    if (!CimArrayUtils.isEmpty(interFabMonitorGroupActionInfoList)){
                        // if monitoringLot already checked, no add
                        for (Infos.InterFabMonitorGroupActionInfo interFabMonitorGroupActionInfo : interFabMonitorGroupActionInfoList){
                            if (ObjectIdentifier.equalsWithValue(tempLotID, interFabMonitorGroupActionInfo.getMonitoringLotID())){
                                log.info("this monitoringLot already checked.");
                                lot_countForMailOtherFab = CimArrayUtils.getSize(interFabMonitorGroupActionInfo.getMonitoredLotMailInfoList());
                                findMonitoringLotFlag = true;
                                interFabMonIdx = interFabMonitorGroupActionInfoList.indexOf(interFabMonitorGroupActionInfo);
                                break;
                            }
                        }
                    }
                    if (!findMonitoringLotFlag){
                        log.info("this lot(monitoring) has not been checked");
                        //-------------------------------------------//
                        // create data for mailSend of monitoredLots //
                        //-------------------------------------------//
                        Infos.InterFabMonitorGroupActionInfo interFabMonitorGroupActionInfo = new Infos.InterFabMonitorGroupActionInfo();
                        interFabMonitorGroupActionInfoList.add(interFabMonitorGroupActionInfo);
                        interFabMonitorGroupActionInfo.setFabID(correspondingFabID);
                        interFabMonitorGroupActionInfo.setMonitoringLotID(effectedLots.get(0).getLotID());
                        //initialize
                        Infos.MonitorGroupReleaseInfo monitorGroupReleaseInfo = new Infos.MonitorGroupReleaseInfo();
                        monitorGroupReleaseInfo.setGroupReleaseFlag(false);
                        interFabMonitorGroupActionInfo.setMonitorGroupReleaseInfo(monitorGroupReleaseInfo);
                        interFabMonIdx = CimArrayUtils.getSize(interFabMonitorGroupActionInfoList) - 1;
                        lot_countForMailOtherFab = 0;
                    }
                    //---------------------------------//
                    // Get correspondingPO information //
                    //---------------------------------//
                    Inputs.ObjProcessOperationInfoGetDrIn strProcessOperation_Info_GetDR_in = new Inputs.ObjProcessOperationInfoGetDrIn();
                    strProcessOperation_Info_GetDR_in.setPoObj(spcCheckLot.getProcessObjrefPO());
                    strProcessOperationInfo = processMethod.processOperationInfoGetDr(objCommon, strProcessOperation_Info_GetDR_in);
                }
                //-----------------------------------------------------------
                //  Check Monitor Grouping existnce
                //  If not existed, the input lot is not monitor lot.
                //  If existed, the input lot is monitor lot.
                //-----------------------------------------------------------
                Boolean requestOtherFabFlagForMonitor = false;
                String generatedFabID;
                //-----------------------------------------
                //  Check Monitor Grouping existnce
                //-----------------------------------------
                log.info("Check Monitor Grouping existnce");
                CimMonitorGroup aMonitorGroup = aPosLot.getControlMonitorGroup();
                if (aMonitorGroup != null){
                    //----------------------------------------------------------------
                    //  When Monitor Grouping is existed get get all related lots.
                    //----------------------------------------------------------------
                    List<ProductDTO.MonitoredLot> monitoredLots = aMonitorGroup.allLots();
                    if (!CimArrayUtils.isEmpty(monitoredLots)){
                        for (ProductDTO.MonitoredLot monitoredLot : monitoredLots){
                            Infos.MonitoredLot tempEffectedLot = new Infos.MonitoredLot();
                            tempEffectedLot.setLotID(monitoredLot.getLotID());
                            tempEffectedLot.setProcessOperation(monitoredLot.getProcessOperation());
                            effectedLots.add(tempEffectedLot);
                        }
                    }
                    //-------------------------------------------------
                    //  Get GeneratedFabID
                    //-------------------------------------------------
                    generatedFabID = aMonitorGroup.getGeneratedFabID();
                    if (CimStringUtils.isEmpty(generatedFabID)){
                        log.info("generatedFabID is null");
                    }
                    if (!CimStringUtils.isEmpty(generatedFabID) && !CimStringUtils.isEmpty(currentFabID) && !CimStringUtils.equals(generatedFabID,currentFabID)){
                        log.info("currentFabID and generatedFabID is not same");
                        requestOtherFabFlagForMonitor = true;
                    }
                }

                boolean equipment_hold_flag = false;
                boolean recipe_hold_flag    = false;
                boolean route_hold_flag     = false;
                boolean process_hold_flag   = false;
                boolean mail_flag           = false;
                boolean bank_move_flag      = false;    //D4200115
                boolean rework_branch_flag  = false;    //D4200115
                boolean equipment_and_recipe_hold_flag  = false;    //D5000147
                boolean process_and_equipment_hold_flag = false;    //D5000147
                boolean chamber_hold_flag        = false;           //D5100227
                boolean chamber_recipe_hold_flag = false;           //D5100227

                boolean findMeasurementLotFlag = false;      //DSIV00001021
                int dcActionLot_idx = 0;                    //DSIV00001021
                int dcActionInfo_count = 0;
                //=========================================================================================
                // Check actionCode
                //=========================================================================================
                List<String> spcActionCodes = spcCheckLot.getSpcActionCode();
                if (CimArrayUtils.isNotEmpty(spcActionCodes)) {
                    for (String spcActionCode : spcActionCodes){
                        if (CimStringUtils.equals(spcActionCode,BizConstant.SP_ACTIONCODE_EQUIPMENTHOLD)){
                            log.info("actionCode == SP_ActionCode_EquipmentHold");
                            equipment_hold_flag = true;
                            mail_flag = true;
                        }
                        if (CimStringUtils.equals(spcActionCode,BizConstant.SP_ACTIONCODE_RECIPEHOLD)){
                            log.info("actionCode == SP_ActionCode_RecipeHold");
                            recipe_hold_flag = true;
                            mail_flag = true;
                        }
                        if (CimStringUtils.equals(spcActionCode,BizConstant.SP_ACTIONCODE_ROUTEHOLD)){
                            log.info("actionCode == SP_ActionCode_RouteHold");
                            route_hold_flag = true;
                            mail_flag = true;
                        }
                        if (CimStringUtils.equals(spcActionCode,BizConstant.SP_ACTIONCODE_PROCESSHOLD)){
                            log.info("actionCode == SP_ActionCode_ProcessHold");
                            process_hold_flag = true;
                            mail_flag = true;
                        }
                        if (CimStringUtils.equals(spcActionCode,BizConstant.SP_ACTIONCODE_BANKMOVE)) {
                            log.info("actionCode == SP_ActionCode_BankMove");
                            bank_move_flag = true;
                            mail_flag = true;
                        }
                        if (CimStringUtils.equals(spcActionCode,BizConstant.SP_ACTIONCODE_MAILSEND)){
                            log.info("actionCode == SP_ActionCode_MailSend");
                            mail_flag = true;
                        }
                        if (CimStringUtils.equals(spcActionCode,BizConstant.SP_ACTIONCODE_REWORKBRANCH)){
                            log.info("actionCode == SP_ActionCode_ReworkBranch");
                            rework_branch_flag = true;
                            mail_flag = true;
                        }
                        if (CimStringUtils.equals(spcActionCode,BizConstant.SP_ACTIONCODE_EQUIPMENT_AND_RECIPEHOLD)){
                            log.info("actionCode == SP_ActionCode_Equipment_and_RecipeHold");
                            equipment_and_recipe_hold_flag = true;
                            mail_flag = true;
                        }
                        if (CimStringUtils.equals(spcActionCode,BizConstant.SP_ACTIONCODE_PROCESS_AND_EQUIPMENTHOLD)){
                            log.info("actionCode == SP_ActionCode_Process_and_EquipmentHold");
                            process_and_equipment_hold_flag = true;
                            mail_flag = true;
                        }
                        if (CimStringUtils.equals(spcActionCode,BizConstant.SP_ACTIONCODE_CHAMBERHOLD)){
                            log.info("actionCode == SP_ActionCode_ChamberHold");
                            chamber_hold_flag = true;
                            mail_flag = true;
                        }
                        if (CimStringUtils.equals(spcActionCode,BizConstant.SP_ACTIONCODE_CHAMBERANDRECIPEHOLD)){
                            log.info("actionCode == SP_ActionCode_ChamberAndRecipeHold");
                            chamber_recipe_hold_flag = true;
                            mail_flag = true;
                        }
                    }
                }

                //=========================================================================================
                //   Check spcCheckResult and set messageID
                //=========================================================================================
                String messageID = null;
                if (CimStringUtils.equals(spcCheckLot.getSpcCheckResult(),BizConstant.SP_SPCCHECK_HOLDLIMITOFF)){
                    log.info("messageID = CIMFWStrDup(SP_MessageID_SPCCheckOver)");
                    messageID = BizConstant.SP_MESSAGEID_SPCCHECKOVER;
                    mail_flag = true;
                } else if(CimStringUtils.equals(spcCheckLot.getSpcCheckResult(),BizConstant.SP_SPCCHECK_WARNINGLIMITOFF)){
                    log.info("messageID = CIMFWStrDup(SP_MessageID_SPCCheckWarning)");
                    messageID = BizConstant.SP_MESSAGEID_SPCCHECKWARNING;
                    mail_flag = true;
                }
                //------------------------------------------------------------------------------------------------
                //  Make strEntityInhibitions of out parameter with SP_Reason_SpecOverInhibit reasonCode.
                //
                //    check actionCode of strSpcCheckLot.lot.
                //    ( actionCode are filled only when spcCheckResult is out of spec. )
                //       If actionCode is SP_ActionCode_Equipment,
                //       If actionCode is SP_ActionCode_Recipe,
                //       If actionCode is SP_ActionCode_Route,
                //       If actionCode is SP_ActionCode_ProcessHold,
                //       If actionCode is SP_ActionCode_Equipment_and_RecipeHold,                                          //D5000147
                //       If actionCode is SP_ActionCode_Process_and_EquipmentHold,                                         //D5000147
                //
                //       if actionCode has "ProcessHold", set the process hold record.
                //       If the processed lot is monitored lot, set the monitored operation to output parameter.
                //       If the processed lot is not monitored lot and it has a corresponding process operation,
                //          set it to output parameter.
                //       If the processed lot is not monitored lot and it doesn't have a corresponding process operation,
                //          set the current operation to output parameter.
                //
                //------------------------------------------------------------------------------------------------
                if (!equipment_hold_flag && !recipe_hold_flag && !route_hold_flag && !process_hold_flag && !equipment_and_recipe_hold_flag &&
                        !process_and_equipment_hold_flag && !chamber_hold_flag && !chamber_recipe_hold_flag && !mail_flag){
                    log.info("Flag all FALSE");
                    continue;
                }
                //------------------------------------------//
                // Get index of dcActionLot                 //
                //------------------------------------------//
                if (!findMeasurementLotFlag){
                    if (!CimArrayUtils.isEmpty(dcActionLotResultList)){
                        for (Results.DCActionLotResult dcActionLotResult : dcActionLotResultList){
                            if (ObjectIdentifier.equalsWithValue(effectedLot.getLotID(), dcActionLotResult.getMeasurementLotID())){
                                log.info( "this measurementLot already checked for Action Result.");
                                dcActionLot_idx = dcActionLotResultList.indexOf(dcActionLotResult);
                                dcActionInfo_count = CimArrayUtils.getSize(dcActionLotResult.getDcActionResultInfo());
                                findMeasurementLotFlag=true;
                                break;
                            }
                        }
                    }
                    if (!findMeasurementLotFlag){
                        log.info("this lot(measurement) has not been checked for Action Result.");
                        //------------------------------------------//
                        // create data for DCActionLotResults       //
                        //------------------------------------------//
                        Results.DCActionLotResult dcActionLotResult = new Results.DCActionLotResult();
                        List<Infos.DCActionResultInfo> dcActionResultInfoInit = new ArrayList<>();
                        dcActionLotResult.setMeasurementLotID(effectedLots.get(0).getLotID());
                        dcActionLotResult.setDcActionResultInfo(dcActionResultInfoInit);
                        dcActionLotResultList.add(dcActionLotResult);
                        dcActionLot_idx = dcActionLot_count;
                        dcActionLot_count ++;
                        findMeasurementLotFlag = true;
                    }
                }
                /*-----------------------------*/
                /*    For Representative Lot   */
                /*-----------------------------*/
                entity_count_keep = entity_count;
                entity_countForOtherFab_keep = entity_countForOtherFab;
                if (equipment_hold_flag){
                    log.info("equipment_hold_flag == TRUE");
                    if (!requestOtherFabFlag){
                        findFlag= false;
                        //-------------------------------------------------------------------
                        //  Check equipment_hold_inhibition is already registed or not.
                        //  If only not existed, entry new inhibition.
                        //-------------------------------------------------------------------
                        log.debug("startLotActionListEffectSpecCheck(): Check equipment_hold_inhibition is already registed or not");
                        List<Infos.EntityInhibitDetailAttributes> entityInhibitions = strStartLot_actionList_EffectSPCCheck_out.getStrEntityInhibitions();
                        for (Infos.EntityInhibitDetailAttributes entityInhibition : entityInhibitions) {
                            if (CimStringUtils.equals(entityInhibition.getEntities().get(0).getClassName(), BizConstant.SP_INHIBITCLASSID_EQUIPMENT)
                                    && ObjectIdentifier.equalsWithValue(entityInhibition.getEntities().get(0).getObjectID(), spcCheckLot.getProcessEquipmentID())) {
                                findFlag = true;
                                break;
                            }
                        }
                        if (!findFlag){
                            log.info("findFlag == FALSE");
                            entity_count++;
                            Infos.EntityInhibitDetailAttributes entityInhibitDetailAttribute = new Infos.EntityInhibitDetailAttributes();
                            entityInhibitions.add(entityInhibitDetailAttribute);
                            List<Infos.EntityIdentifier> entities = new ArrayList<>();
                            entityInhibitDetailAttribute.setEntities(entities);
                            Infos.EntityIdentifier entityIdentifier = new Infos.EntityIdentifier();
                            entities.add(entityIdentifier);
                            entityIdentifier.setClassName(BizConstant.SP_INHIBITCLASSID_EQUIPMENT);
                            entityIdentifier.setAttribution("");
                            entityIdentifier.setObjectID(spcCheckLot.getProcessEquipmentID());
                            entityInhibitDetailAttribute.setStartTimeStamp(CimDateUtils.getTimestampAsString(objCommon.getTimeStamp().getReportTimeStamp()));
                            entityInhibitDetailAttribute.setEndTimeStamp(BizConstant.SP_TIMESTAMP_NIL_OBJECT_STRING);
                            entityInhibitDetailAttribute.setReasonCode(BizConstant.SP_REASON_SPCOUTOFRANGEINHIBIT);
                            entityInhibitDetailAttribute.setOwnerID(objCommon.getUser().getUserID());
                            entityInhibitDetailAttribute.setClaimedTimeStamp(CimDateUtils.getTimestampAsString(objCommon.getTimeStamp().getReportTimeStamp()));
                            /**
                             * Task-311 add ocapNo for constraint memo
                             */
                            entityInhibitDetailAttribute.setMemo(ocapMemo);
                        }
                        findFlag = false;
                        for (int n = 0 ; n < dcActionInfo_count ; n++ ) {
                            if (CimArrayUtils.getSize(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getEntities()) == 1 ){
                                if (CimStringUtils.equals(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getEntities().get(0).getClassName(),BizConstant.SP_INHIBITCLASSID_EQUIPMENT)
                                        && ObjectIdentifier.equalsWithValue(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getEntities().get(0).getObjectID(), spcCheckLot.getProcessEquipmentID())
                                        && CimStringUtils.equals(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getReasonCode(),BizConstant.SP_REASON_SPCOUTOFRANGEINHIBIT)
                                        && CimStringUtils.equals(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getCorrespondingObjRefPO(),spcCheckLot.getProcessObjrefPO())
                                        && ObjectIdentifier.equalsWithValue(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getLotID(), tempLotID)){
                                    log.info("set findFlag = TRUE");
                                    findFlag = true;
                                    break;
                                }
                            }
                        }
                        if (!findFlag){
                            log.info("findFlag == FALSE");
                            // Add ActionResultInfo
                            List<Infos.DCActionResultInfo> dcActionLotResult = dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo();
                            Infos.DCActionResultInfo  dcActionResultInfo = new Infos.DCActionResultInfo();
                            dcActionResultInfo.setLotID(tempLotID);
                            dcActionResultInfo.setMonitorLotFlag(false);
                            dcActionResultInfo.setDcDefID(spcCheckLot.getDcDefID());
                            dcActionResultInfo.setDcSpecID(spcCheckLot.getDcSpecID());
                            dcActionResultInfo.setCheckType(BizConstant.SP_ACTIONRESULT_CHECKTYPE_SPC);
                            dcActionResultInfo.setReasonCode(BizConstant.SP_REASON_SPCOUTOFRANGEINHIBIT);
                            dcActionResultInfo.setActionCode(BizConstant.SP_ACTIONCODE_EQUIPMENTHOLD);
                            dcActionResultInfo.setCorrespondingObjRefPO(spcCheckLot.getProcessObjrefPO());
                            dcActionResultInfo.setBankID("");
                            dcActionResultInfo.setReworkRouteID("");
                            List<Infos.EntityIdentifier> entityIdentifiers = new ArrayList<>();
                            Infos.EntityIdentifier entityIdentifier = new Infos.EntityIdentifier();
                            entityIdentifier.setClassName(BizConstant.SP_INHIBITCLASSID_EQUIPMENT);
                            entityIdentifier.setAttribution("");
                            entityIdentifier.setObjectID(spcCheckLot.getProcessEquipmentID());
                            entityIdentifiers.add(entityIdentifier);
                            dcActionResultInfo.setEntities(entityIdentifiers);
                            dcActionLotResult.add(dcActionResultInfo);
                            dcActionInfo_count++;
                        }
                    }else{
                        log.info("correspondingPO is other Fab");
                        //-------------------------------------------------------------------
                        //  Check equipment_hold_inhibition is already registed or not.
                        //  If only not existed, entry new inhibition.
                        //-------------------------------------------------------------------
                        findFlag = false;
                        log.debug("startLotActionListEffectSpecCheck(): Check equipment_hold_inhibition is already registed or not");
                        List<Infos.EntityInhibitAttributesWithFabInfo> entityInhibitAttributesWithFabInfos = strStartLot_actionList_EffectSPCCheck_out.getEntityInhibitionsWithFabInfo();
                        for (Infos.EntityInhibitAttributesWithFabInfo entityInhibitAttributesWithFabInfo : entityInhibitAttributesWithFabInfos) {
                            if (CimStringUtils.equals(entityInhibitAttributesWithFabInfo.getEntityInhibitAttributes().getEntities().get(0).getClassName(), BizConstant.SP_INHIBITCLASSID_EQUIPMENT)
                                    && ObjectIdentifier.equalsWithValue(entityInhibitAttributesWithFabInfo.getEntityInhibitAttributes().getEntities().get(0).getObjectID(), spcCheckLot.getProcessEquipmentID())) {
                                findFlag = true;
                                break;
                            }
                        }
                        if (!findFlag){
                            log.info("findFlag == FALSE");
                            entity_countForOtherFab++;
                            Infos.EntityInhibitAttributesWithFabInfo entityInhibitAttributesWithFabInfo = new Infos.EntityInhibitAttributesWithFabInfo();
                            entityInhibitAttributesWithFabInfos.add(entityInhibitAttributesWithFabInfo);
                            entityInhibitAttributesWithFabInfo.setFabID(correspondingFabID);
                            Infos.EntityInhibitAttributes entityInhibitAttributes = new Infos.EntityInhibitAttributes();
                            entityInhibitAttributesWithFabInfo.setEntityInhibitAttributes(entityInhibitAttributes);
                            List<Infos.EntityIdentifier> entities = new ArrayList<>();
                            entityInhibitAttributes.setEntities(entities);
                            Infos.EntityIdentifier entityIdentifier = new Infos.EntityIdentifier();
                            entities.add(entityIdentifier);
                            entityIdentifier.setClassName(BizConstant.SP_INHIBITCLASSID_EQUIPMENT);
                            entityIdentifier.setObjectID(spcCheckLot.getProcessEquipmentID());
                            entityIdentifier.setAttribution("");
                            entityInhibitAttributes.setStartTimeStamp(CimDateUtils.getTimestampAsString(objCommon.getTimeStamp().getReportTimeStamp()));
                            entityInhibitAttributes.setEndTimeStamp(BizConstant.SP_TIMESTAMP_NIL_OBJECT_STRING);
                            entityInhibitAttributes.setReasonCode(BizConstant.SP_REASON_SPCOUTOFRANGEINHIBIT);
                            entityInhibitAttributes.setOwnerID(objCommon.getUser().getUserID());
                            entityInhibitAttributes.setClaimedTimeStamp(CimDateUtils.getTimestampAsString(objCommon.getTimeStamp().getReportTimeStamp()));
                            /**
                             * Task-311 add ocapNo for constraint memo
                             */
                            entityInhibitAttributes.setMemo(ocapMemo);
                        }
                        findFlag = false;
                        for (int n = 0 ; n < dcActionInfo_count ; n++ ) {
                            if (CimArrayUtils.getSize(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getEntities()) == 1 ){
                                if (CimStringUtils.equals(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getEntities().get(0).getClassName(),BizConstant.SP_INHIBITCLASSID_EQUIPMENT)
                                        && ObjectIdentifier.equalsWithValue(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getEntities().get(0).getObjectID(), spcCheckLot.getProcessEquipmentID())
                                        && CimStringUtils.equals(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getReasonCode(),BizConstant.SP_REASON_SPCOUTOFRANGEINHIBIT)
                                        && CimStringUtils.equals(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getCorrespondingObjRefPO(),spcCheckLot.getProcessObjrefPO())
                                        && ObjectIdentifier.equalsWithValue(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getLotID(), tempLotID)){
                                    log.info("break!!");
                                    findFlag = TRUE;
                                    break;
                                }
                            }
                        }
                        if (!findFlag){
                            log.info("findFlag == FALSE  [machineID]");
                            // Add ActionResultInfo
                            List<Infos.DCActionResultInfo> dcActionLotResult = dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo();
                            Infos.DCActionResultInfo  dcActionResultInfo = new Infos.DCActionResultInfo();
                            dcActionResultInfo.setLotID(tempLotID);
                            dcActionResultInfo.setMonitorLotFlag(false);
                            dcActionResultInfo.setDcDefID(spcCheckLot.getDcDefID());
                            dcActionResultInfo.setDcSpecID(spcCheckLot.getDcSpecID());
                            dcActionResultInfo.setCheckType(BizConstant.SP_ACTIONRESULT_CHECKTYPE_SPC);
                            dcActionResultInfo.setReasonCode(BizConstant.SP_REASON_SPCOUTOFRANGEINHIBIT);
                            dcActionResultInfo.setActionCode(BizConstant.SP_ACTIONCODE_EQUIPMENTHOLD);
                            dcActionResultInfo.setCorrespondingObjRefPO(spcCheckLot.getProcessObjrefPO());
                            dcActionResultInfo.setBankID("");
                            dcActionResultInfo.setReworkRouteID("");
                            List<Infos.EntityIdentifier> entityIdentifiers = new ArrayList<>();
                            Infos.EntityIdentifier entityIdentifier = new Infos.EntityIdentifier();
                            entityIdentifier.setClassName(BizConstant.SP_INHIBITCLASSID_EQUIPMENT);
                            entityIdentifier.setAttribution("");
                            entityIdentifier.setObjectID(spcCheckLot.getProcessEquipmentID());
                            entityIdentifiers.add(entityIdentifier);
                            dcActionResultInfo.setEntities(entityIdentifiers);
                            dcActionLotResult.add(dcActionResultInfo);
                            dcActionInfo_count++;
                        }
                    }
                }

                if (recipe_hold_flag){
                    log.info("recipe_hold_flag == TRUE");
                    if (!requestOtherFabFlag){
                        //-------------------------------------------------------------------
                        //  Check recipe_hold_inhibition is already registed or not.
                        //  If only not existed, entry new inhibition.
                        //-------------------------------------------------------------------
                        findFlag = false;
                        List<Infos.EntityInhibitDetailAttributes> entityInhibitions = strStartLot_actionList_EffectSPCCheck_out.getStrEntityInhibitions();
                        for (Infos.EntityInhibitDetailAttributes entityInhibition : entityInhibitions) {
                            if (CimStringUtils.equals(entityInhibition.getEntities().get(0).getClassName(), BizConstant.SP_INHIBITCLASSID_MACHINERECIPE)
                                    && ObjectIdentifier.equalsWithValue(entityInhibition.getEntities().get(0).getObjectID(), spcCheckLot.getProcessMachineRecipeID())) {
                                findFlag = true;
                                break;
                            }
                        }
                        if (!findFlag){
                            log.info("findFlag == FALSE");
                            entity_count++;
                            Infos.EntityInhibitDetailAttributes entityInhibitDetailAttribute = new Infos.EntityInhibitDetailAttributes();
                            entityInhibitions.add(entityInhibitDetailAttribute);
                            List<Infos.EntityIdentifier> entities = new ArrayList<>();
                            entityInhibitDetailAttribute.setEntities(entities);
                            Infos.EntityIdentifier entityIdentifier = new Infos.EntityIdentifier();
                            entities.add(entityIdentifier);
                            entityIdentifier.setClassName(BizConstant.SP_INHIBITCLASSID_MACHINERECIPE);
                            entityIdentifier.setAttribution("");
                            entityIdentifier.setObjectID(spcCheckLot.getProcessMachineRecipeID());
                            entityInhibitDetailAttribute.setStartTimeStamp(CimDateUtils.getTimestampAsString(objCommon.getTimeStamp().getReportTimeStamp()));
                            entityInhibitDetailAttribute.setEndTimeStamp(BizConstant.SP_TIMESTAMP_NIL_OBJECT_STRING);
                            entityInhibitDetailAttribute.setReasonCode(BizConstant.SP_REASON_SPCOUTOFRANGEINHIBIT);
                            entityInhibitDetailAttribute.setOwnerID(objCommon.getUser().getUserID());
                            entityInhibitDetailAttribute.setClaimedTimeStamp(CimDateUtils.getTimestampAsString(objCommon.getTimeStamp().getReportTimeStamp()));
                            /**
                             * Task-311 add ocapNo for constraint memo
                             */
                            entityInhibitDetailAttribute.setMemo(ocapMemo);
                        }
                        findFlag = false;
                        for (int n = 0 ; n < dcActionInfo_count ; n++ ) {
                            if (CimArrayUtils.getSize(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getEntities()) == 1 ){
                                if (CimStringUtils.equals(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getEntities().get(0).getClassName(),BizConstant.SP_INHIBITCLASSID_MACHINERECIPE)
                                        && ObjectIdentifier.equalsWithValue(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getEntities().get(0).getObjectID(), spcCheckLot.getProcessMachineRecipeID())
                                        && CimStringUtils.equals(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getReasonCode(),BizConstant.SP_REASON_SPCOUTOFRANGEINHIBIT)
                                        && CimStringUtils.equals(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getCorrespondingObjRefPO(),spcCheckLot.getProcessObjrefPO())
                                        && ObjectIdentifier.equalsWithValue(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getLotID(), tempLotID)){
                                    log.info("break!!");
                                    findFlag = TRUE;
                                    break;
                                }
                            }
                        }
                        if (!findFlag){
                            log.info("findFlag == FALSE  [machineID]");
                            // Add ActionResultInfo
                            List<Infos.DCActionResultInfo> dcActionLotResult = dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo();
                            Infos.DCActionResultInfo dcActionResultInfo = new Infos.DCActionResultInfo();
                            dcActionResultInfo.setLotID(tempLotID);
                            dcActionResultInfo.setMonitorLotFlag(false);
                            dcActionResultInfo.setDcDefID(spcCheckLot.getDcDefID());
                            dcActionResultInfo.setDcSpecID(spcCheckLot.getDcSpecID());
                            dcActionResultInfo.setCheckType(BizConstant.SP_ACTIONRESULT_CHECKTYPE_SPC);
                            dcActionResultInfo.setReasonCode(BizConstant.SP_REASON_SPCOUTOFRANGEINHIBIT);
                            dcActionResultInfo.setActionCode(BizConstant.SP_ACTIONCODE_RECIPEHOLD);
                            dcActionResultInfo.setCorrespondingObjRefPO(spcCheckLot.getProcessObjrefPO());
                            dcActionResultInfo.setBankID("");
                            dcActionResultInfo.setReworkRouteID("");
                            List<Infos.EntityIdentifier> entityIdentifiers = new ArrayList<>();
                            Infos.EntityIdentifier entityIdentifier = new Infos.EntityIdentifier();
                            entityIdentifier.setClassName(BizConstant.SP_INHIBITCLASSID_MACHINERECIPE);
                            entityIdentifier.setAttribution("");
                            entityIdentifier.setObjectID(spcCheckLot.getProcessMachineRecipeID());
                            entityIdentifiers.add(entityIdentifier);
                            dcActionResultInfo.setEntities(entityIdentifiers);
                            dcActionLotResult.add(dcActionResultInfo);
                            dcActionInfo_count++;
                        }
                    }else {
                        log.info("correspondingPO is other Fab");
                        //-------------------------------------------------------------------
                        //  Check equipment_hold_inhibition is already registed or not.
                        //  If only not existed, entry new inhibition.
                        //-------------------------------------------------------------------
                        findFlag = false;
                        log.debug("startLotActionListEffectSpecCheck(): Check equipment_hold_inhibition is already registed or not");
                        List<Infos.EntityInhibitAttributesWithFabInfo> entityInhibitAttributesWithFabInfos = strStartLot_actionList_EffectSPCCheck_out.getEntityInhibitionsWithFabInfo();
                        for (Infos.EntityInhibitAttributesWithFabInfo entityInhibitAttributesWithFabInfo : entityInhibitAttributesWithFabInfos) {
                            if (CimStringUtils.equals(entityInhibitAttributesWithFabInfo.getEntityInhibitAttributes().getEntities().get(0).getClassName(), BizConstant.SP_INHIBITCLASSID_MACHINERECIPE)
                                    && ObjectIdentifier.equalsWithValue(entityInhibitAttributesWithFabInfo.getEntityInhibitAttributes().getEntities().get(0).getObjectID(), spcCheckLot.getProcessMachineRecipeID())) {
                                findFlag = true;
                                break;
                            }
                        }
                        if (!findFlag){
                            log.info("findFlag == FALSE");
                            entity_countForOtherFab++;
                            Infos.EntityInhibitAttributesWithFabInfo entityInhibitAttributesWithFabInfo = new Infos.EntityInhibitAttributesWithFabInfo();
                            entityInhibitAttributesWithFabInfos.add(entityInhibitAttributesWithFabInfo);
                            Infos.EntityInhibitAttributes entityInhibitAttributes = new Infos.EntityInhibitAttributes();
                            entityInhibitAttributesWithFabInfo.setEntityInhibitAttributes(entityInhibitAttributes);
                            List<Infos.EntityIdentifier> entities = new ArrayList<>();
                            entityInhibitAttributes.setEntities(entities);
                            Infos.EntityIdentifier entityIdentifier = new Infos.EntityIdentifier();
                            entities.add(entityIdentifier);
                            entityIdentifier.setClassName(BizConstant.SP_INHIBITCLASSID_MACHINERECIPE);
                            entityIdentifier.setAttribution("");
                            entityIdentifier.setObjectID(spcCheckLot.getProcessMachineRecipeID());
                            entityInhibitAttributes.setStartTimeStamp(CimDateUtils.getTimestampAsString(objCommon.getTimeStamp().getReportTimeStamp()));
                            entityInhibitAttributes.setEndTimeStamp(BizConstant.SP_TIMESTAMP_NIL_OBJECT_STRING);
                            entityInhibitAttributes.setReasonCode(BizConstant.SP_REASON_SPCOUTOFRANGEINHIBIT);
                            entityInhibitAttributes.setOwnerID(objCommon.getUser().getUserID());
                            entityInhibitAttributes.setClaimedTimeStamp(CimDateUtils.getTimestampAsString(objCommon.getTimeStamp().getReportTimeStamp()));
                            /**
                             * Task-311 add ocapNo for constraint memo
                             */
                            entityInhibitAttributes.setMemo(ocapMemo);
                        }
                        findFlag = false;
                        for (int n = 0 ; n < dcActionInfo_count ; n++ ) {
                            if (CimArrayUtils.getSize(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getEntities()) == 1){
                                if (CimStringUtils.equals(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getEntities().get(0).getClassName(), BizConstant.SP_INHIBITCLASSID_MACHINERECIPE)
                                        && ObjectIdentifier.equalsWithValue(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getEntities().get(0).getObjectID(), spcCheckLot.getProcessMachineRecipeID())
                                        && CimStringUtils.equals(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getReasonCode(),BizConstant.SP_REASON_SPCOUTOFRANGEINHIBIT)
                                        && CimStringUtils.equals(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getCorrespondingObjRefPO(),spcCheckLot.getProcessObjrefPO())
                                        && ObjectIdentifier.equalsWithValue(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getLotID(), tempLotID)){
                                    log.info("break!!");
                                    findFlag = true;
                                    break;
                                }
                            }
                        }
                        if (!findFlag){
                            log.info("findFlag == FALSE  [machineID]");
                            // Add ActionResultInfo
                            List<Infos.DCActionResultInfo> dcActionLotResult = dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo();
                            Infos.DCActionResultInfo dcActionResultInfo = new Infos.DCActionResultInfo();
                            dcActionResultInfo.setLotID(tempLotID);
                            dcActionResultInfo.setMonitorLotFlag(false);
                            dcActionResultInfo.setDcDefID(spcCheckLot.getDcDefID());
                            dcActionResultInfo.setDcSpecID(spcCheckLot.getDcSpecID());
                            dcActionResultInfo.setCheckType(BizConstant.SP_ACTIONRESULT_CHECKTYPE_SPC);
                            dcActionResultInfo.setReasonCode(BizConstant.SP_REASON_SPCOUTOFRANGEINHIBIT);
                            dcActionResultInfo.setActionCode(BizConstant.SP_ACTIONCODE_RECIPEHOLD);
                            dcActionResultInfo.setCorrespondingObjRefPO(spcCheckLot.getProcessObjrefPO());
                            dcActionResultInfo.setBankID("");
                            dcActionResultInfo.setReworkRouteID("");
                            List<Infos.EntityIdentifier> entityIdentifiers = new ArrayList<>();
                            Infos.EntityIdentifier entityIdentifier = new Infos.EntityIdentifier();
                            entityIdentifier.setClassName(BizConstant.SP_INHIBITCLASSID_MACHINERECIPE);
                            entityIdentifier.setAttribution("");
                            entityIdentifier.setObjectID(spcCheckLot.getProcessMachineRecipeID());
                            entityIdentifiers.add(entityIdentifier);
                            dcActionResultInfo.setEntities(entityIdentifiers);
                            dcActionLotResult.add(dcActionResultInfo);
                            dcActionInfo_count++;
                        }
                    }
                }

                if (route_hold_flag){
                    log.info("route_hold_flag == TRUE");
                    if (!requestOtherFabFlag){
                        //-------------------------------------------------------------------
                        //  Check recipe_hold_inhibition is already registed or not.
                        //  If only not existed, entry new inhibition.
                        //-------------------------------------------------------------------
                        findFlag = false;
                        List<Infos.EntityInhibitDetailAttributes> entityInhibitions = strStartLot_actionList_EffectSPCCheck_out.getStrEntityInhibitions();
                        for (Infos.EntityInhibitDetailAttributes entityInhibition : entityInhibitions) {
                            if (CimStringUtils.equals(entityInhibition.getEntities().get(0).getClassName(), BizConstant.SP_INHIBITCLASSID_OPERATION)
                                    && ObjectIdentifier.equalsWithValue(entityInhibition.getEntities().get(0).getObjectID(), spcCheckLot.getProcessRouteID())) {
                                findFlag = true;
                                break;
                            }
                        }
                        if (!findFlag){
                            log.info("findFlag == FALSE");
                            entity_count++;
                            Infos.EntityInhibitDetailAttributes entityInhibitDetailAttribute = new Infos.EntityInhibitDetailAttributes();
                            entityInhibitions.add(entityInhibitDetailAttribute);
                            List<Infos.EntityIdentifier> entities = new ArrayList<>();
                            entityInhibitDetailAttribute.setEntities(entities);
                            Infos.EntityIdentifier entityIdentifier = new Infos.EntityIdentifier();
                            entities.add(entityIdentifier);
                            entityIdentifier.setClassName(BizConstant.SP_INHIBITCLASSID_ROUTE);
                            entityIdentifier.setAttribution("");
                            entityIdentifier.setObjectID(spcCheckLot.getProcessRouteID());
                            entityInhibitDetailAttribute.setStartTimeStamp(CimDateUtils.getTimestampAsString(objCommon.getTimeStamp().getReportTimeStamp()));
                            entityInhibitDetailAttribute.setEndTimeStamp(BizConstant.SP_TIMESTAMP_NIL_OBJECT_STRING);
                            entityInhibitDetailAttribute.setReasonCode(BizConstant.SP_REASON_SPCOUTOFRANGEINHIBIT);
                            entityInhibitDetailAttribute.setOwnerID(objCommon.getUser().getUserID());
                            entityInhibitDetailAttribute.setClaimedTimeStamp(CimDateUtils.getTimestampAsString(objCommon.getTimeStamp().getReportTimeStamp()));
                            /**
                             * Task-311 add ocapNo for constraint memo
                             */
                            entityInhibitDetailAttribute.setMemo(ocapMemo);
                        }
                        findFlag = false;
                        for (int n = 0 ; n < dcActionInfo_count ; n++ ) {
                            if (CimArrayUtils.getSize(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getEntities()) == 1 ){
                                if (CimStringUtils.equals(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getEntities().get(0).getClassName(), BizConstant.SP_INHIBITCLASSID_ROUTE)
                                        && ObjectIdentifier.equalsWithValue(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getEntities().get(0).getObjectID(), spcCheckLot.getProcessRouteID())
                                        && CimStringUtils.equals(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getReasonCode(),BizConstant.SP_REASON_SPCOUTOFRANGEINHIBIT)
                                        && CimStringUtils.equals(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getCorrespondingObjRefPO(),spcCheckLot.getProcessObjrefPO())
                                        && CimObjectUtils.equals(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getLotID(), tempLotID)){
                                    log.info("break!!");
                                    findFlag = TRUE;
                                    break;
                                }
                            }
                        }
                        if (!findFlag){
                            log.info("findFlag == FALSE  [machineID]");
                            // Add ActionResultInfo
                            List<Infos.DCActionResultInfo> dcActionLotResult = dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo();
                            Infos.DCActionResultInfo  dcActionResultInfo = new Infos.DCActionResultInfo();
                            dcActionResultInfo.setLotID(tempLotID);
                            dcActionResultInfo.setMonitorLotFlag(false);
                            dcActionResultInfo.setDcDefID(spcCheckLot.getDcDefID());
                            dcActionResultInfo.setDcSpecID(spcCheckLot.getDcSpecID());
                            dcActionResultInfo.setCheckType(BizConstant.SP_ACTIONRESULT_CHECKTYPE_SPC);
                            dcActionResultInfo.setReasonCode(BizConstant.SP_REASON_SPCOUTOFRANGEINHIBIT);
                            dcActionResultInfo.setActionCode(BizConstant.SP_ACTIONCODE_ROUTEHOLD);
                            dcActionResultInfo.setCorrespondingObjRefPO(spcCheckLot.getProcessObjrefPO());
                            dcActionResultInfo.setBankID("");
                            dcActionResultInfo.setReworkRouteID("");
                            List<Infos.EntityIdentifier> entityIdentifiers = new ArrayList<>();
                            Infos.EntityIdentifier entityIdentifier = new Infos.EntityIdentifier();
                            entityIdentifier.setClassName(BizConstant.SP_INHIBITCLASSID_ROUTE);
                            entityIdentifier.setAttribution("");
                            entityIdentifier.setObjectID(spcCheckLot.getProcessRouteID());
                            entityIdentifiers.add(entityIdentifier);
                            dcActionResultInfo.setEntities(entityIdentifiers);
                            dcActionLotResult.add(dcActionResultInfo);
                            dcActionInfo_count++;
                        }
                    } else {
                        log.info("correspondingPO is other Fab");
                        findFlag = false;
                        List<Infos.EntityInhibitAttributesWithFabInfo> entityInhibitAttributesWithFabInfos = strStartLot_actionList_EffectSPCCheck_out.getEntityInhibitionsWithFabInfo();
                        for (Infos.EntityInhibitAttributesWithFabInfo entityInhibitAttributesWithFabInfo : entityInhibitAttributesWithFabInfos) {
                            if (CimStringUtils.equals(entityInhibitAttributesWithFabInfo.getEntityInhibitAttributes().getEntities().get(0).getClassName(), BizConstant.SP_INHIBITCLASSID_OPERATION)
                                    && ObjectIdentifier.equalsWithValue(entityInhibitAttributesWithFabInfo.getEntityInhibitAttributes().getEntities().get(0).getObjectID(), spcCheckLot.getProcessRouteID())) {
                                findFlag = true;
                                break;
                            }
                        }
                        if (!findFlag){
                            log.info("findFlag == FALSE");
                            entity_countForOtherFab++;
                            Infos.EntityInhibitAttributesWithFabInfo entityInhibitAttributesWithFabInfo = new Infos.EntityInhibitAttributesWithFabInfo();
                            entityInhibitAttributesWithFabInfos.add(entityInhibitAttributesWithFabInfo);
                            Infos.EntityInhibitAttributes entityInhibitAttributes = new Infos.EntityInhibitAttributes();
                            entityInhibitAttributesWithFabInfo.setEntityInhibitAttributes(entityInhibitAttributes);
                            List<Infos.EntityIdentifier> entities = new ArrayList<>();
                            entityInhibitAttributes.setEntities(entities);
                            Infos.EntityIdentifier entityIdentifier = new Infos.EntityIdentifier();
                            entities.add(entityIdentifier);
                            entityIdentifier.setClassName(BizConstant.SP_INHIBITCLASSID_ROUTE);
                            entityIdentifier.setAttribution("");
                            entityIdentifier.setObjectID(spcCheckLot.getProcessRouteID());
                            entityInhibitAttributes.setStartTimeStamp(CimDateUtils.getTimestampAsString(objCommon.getTimeStamp().getReportTimeStamp()));
                            entityInhibitAttributes.setEndTimeStamp(BizConstant.SP_TIMESTAMP_NIL_OBJECT_STRING);
                            entityInhibitAttributes.setReasonCode(BizConstant.SP_REASON_SPCOUTOFRANGEINHIBIT);
                            entityInhibitAttributes.setOwnerID(objCommon.getUser().getUserID());
                            entityInhibitAttributes.setClaimedTimeStamp(CimDateUtils.getTimestampAsString(objCommon.getTimeStamp().getReportTimeStamp()));
                            /**
                             * Task-311 add ocapNo for constraint memo
                             */
                            entityInhibitAttributes.setMemo(ocapMemo);
                        }
                        findFlag = false;
                        for (int n = 0 ; n < dcActionInfo_count ; n++ ) {
                            if (CimArrayUtils.getSize(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getEntities()) == 1 ){
                                if (CimStringUtils.equals(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getEntities().get(0).getClassName(), BizConstant.SP_INHIBITCLASSID_ROUTE)
                                        && ObjectIdentifier.equalsWithValue(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getEntities().get(0).getObjectID(), spcCheckLot.getProcessRouteID())
                                        && CimStringUtils.equals(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getReasonCode(), BizConstant.SP_REASON_SPCOUTOFRANGEINHIBIT)
                                        && CimStringUtils.equals(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getCorrespondingObjRefPO(),spcCheckLot.getProcessObjrefPO())
                                        && ObjectIdentifier.equalsWithValue(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getLotID(), tempLotID)){
                                    log.info("break!!");
                                    findFlag = TRUE;
                                    break;
                                }
                            }
                        }
                        if (!findFlag){
                            log.info("findFlag == FALSE  [machineID]");
                            // Add ActionResultInfo
                            List<Infos.DCActionResultInfo> dcActionLotResult = dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo();
                            Infos.DCActionResultInfo  dcActionResultInfo = new Infos.DCActionResultInfo();
                            dcActionResultInfo.setLotID(tempLotID);
                            dcActionResultInfo.setMonitorLotFlag(false);
                            dcActionResultInfo.setDcDefID(spcCheckLot.getDcDefID());
                            dcActionResultInfo.setDcSpecID(spcCheckLot.getDcSpecID());
                            dcActionResultInfo.setCheckType(BizConstant.SP_ACTIONRESULT_CHECKTYPE_SPC);
                            dcActionResultInfo.setReasonCode(BizConstant.SP_REASON_SPCOUTOFRANGEINHIBIT);
                            dcActionResultInfo.setActionCode(BizConstant.SP_ACTIONCODE_ROUTEHOLD);
                            dcActionResultInfo.setCorrespondingObjRefPO(spcCheckLot.getProcessObjrefPO());
                            dcActionResultInfo.setBankID("");
                            dcActionResultInfo.setReworkRouteID("");
                            List<Infos.EntityIdentifier> entityIdentifiers = new ArrayList<>();
                            Infos.EntityIdentifier entityIdentifier = new Infos.EntityIdentifier();
                            entityIdentifier.setClassName(BizConstant.SP_INHIBITCLASSID_ROUTE);
                            entityIdentifier.setAttribution("");
                            entityIdentifier.setObjectID(spcCheckLot.getProcessRouteID());
                            entityIdentifiers.add(entityIdentifier);
                            dcActionResultInfo.setEntities(entityIdentifiers);
                            dcActionLotResult.add(dcActionResultInfo);
                            dcActionInfo_count++;
                        }
                    }
                }

                if (process_hold_flag){
                    log.info("process_hold_flag == TRUE");
                    if (!requestOtherFabFlag){
                        findFlag = false;
                        List<Infos.EntityInhibitDetailAttributes> entityInhibitions = strStartLot_actionList_EffectSPCCheck_out.getStrEntityInhibitions();
                        for (Infos.EntityInhibitDetailAttributes entityInhibition : entityInhibitions) {
                            if (CimStringUtils.equals(entityInhibition.getEntities().get(0).getClassName(), BizConstant.SP_INHIBITCLASSID_OPERATION)
                                    && ObjectIdentifier.equalsWithValue(entityInhibition.getEntities().get(0).getObjectID(), spcCheckLot.getProcessRouteID())
                                    && CimStringUtils.equals(entityInhibition.getEntities().get(0).getAttribution(), spcCheckLot.getProcessOperationNumber())) {
                                findFlag = true;
                                break;
                            }
                        }
                        if (!findFlag){
                            log.info("findFlag == FALSE");
                            entity_count++;
                            Infos.EntityInhibitDetailAttributes entityInhibitDetailAttribute = new Infos.EntityInhibitDetailAttributes();
                            entityInhibitions.add(entityInhibitDetailAttribute);
                            List<Infos.EntityIdentifier> entities = new ArrayList<>();
                            entityInhibitDetailAttribute.setEntities(entities);
                            Infos.EntityIdentifier entityIdentifier = new Infos.EntityIdentifier();
                            entities.add(entityIdentifier);
                            entityIdentifier.setClassName(BizConstant.SP_INHIBITCLASSID_OPERATION);
                            entityIdentifier.setAttribution(spcCheckLot.getProcessOperationNumber());
                            entityIdentifier.setObjectID(spcCheckLot.getProcessRouteID());
                            entityInhibitDetailAttribute.setStartTimeStamp(CimDateUtils.getTimestampAsString(objCommon.getTimeStamp().getReportTimeStamp()));
                            entityInhibitDetailAttribute.setEndTimeStamp(BizConstant.SP_TIMESTAMP_NIL_OBJECT_STRING);
                            entityInhibitDetailAttribute.setReasonCode(BizConstant.SP_REASON_SPCOUTOFRANGEINHIBIT);
                            entityInhibitDetailAttribute.setOwnerID(objCommon.getUser().getUserID());
                            entityInhibitDetailAttribute.setClaimedTimeStamp(CimDateUtils.getTimestampAsString(objCommon.getTimeStamp().getReportTimeStamp()));
                            /**
                             * Task-311 add ocapNo for constraint memo
                             */
                            entityInhibitDetailAttribute.setMemo(ocapMemo);
                        }
                        findFlag = false;
                        for (int n = 0 ; n < dcActionInfo_count ; n++ ) {
                            if (CimArrayUtils.getSize(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getEntities()) == 1 ){
                                if (CimStringUtils.equals(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getEntities().get(0).getClassName(),BizConstant.SP_INHIBITCLASSID_OPERATION)
                                        && ObjectIdentifier.equalsWithValue(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getEntities().get(0).getObjectID(), spcCheckLot.getProcessRouteID())
                                        && CimStringUtils.equals(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getReasonCode(),BizConstant.SP_REASON_SPCOUTOFRANGEINHIBIT)
                                        && CimStringUtils.equals(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getCorrespondingObjRefPO(),spcCheckLot.getProcessObjrefPO())
                                        && ObjectIdentifier.equalsWithValue(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getLotID(), tempLotID)
                                        && CimStringUtils.equals(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getEntities().get(0).getAttribution(), spcCheckLot.getProcessOperationNumber())){
                                    log.info("break!!");
                                    findFlag = true;
                                    break;
                                }
                            }
                        }
                        if (!findFlag){
                            log.info("findFlag == FALSE  [machineID]");
                            // Add ActionResultInfo
                            List<Infos.DCActionResultInfo> dcActionLotResult = dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo();
                            Infos.DCActionResultInfo  dcActionResultInfo = new Infos.DCActionResultInfo();
                            dcActionResultInfo.setLotID(tempLotID);
                            dcActionResultInfo.setMonitorLotFlag(false);
                            dcActionResultInfo.setDcDefID(spcCheckLot.getDcDefID());
                            dcActionResultInfo.setDcSpecID(spcCheckLot.getDcSpecID());
                            dcActionResultInfo.setCheckType(BizConstant.SP_ACTIONRESULT_CHECKTYPE_SPC);
                            dcActionResultInfo.setReasonCode(BizConstant.SP_REASON_SPCOUTOFRANGEINHIBIT);
                            dcActionResultInfo.setActionCode(BizConstant.SP_ACTIONCODE_PROCESSHOLD);
                            dcActionResultInfo.setCorrespondingObjRefPO(spcCheckLot.getProcessObjrefPO());
                            dcActionResultInfo.setBankID("");
                            dcActionResultInfo.setReworkRouteID("");
                            List<Infos.EntityIdentifier> entityIdentifiers = new ArrayList<>();
                            Infos.EntityIdentifier entityIdentifier = new Infos.EntityIdentifier();
                            entityIdentifier.setClassName(BizConstant.SP_INHIBITCLASSID_OPERATION);
                            entityIdentifier.setAttribution(spcCheckLot.getProcessOperationNumber());
                            entityIdentifier.setObjectID(spcCheckLot.getProcessRouteID());
                            entityIdentifiers.add(entityIdentifier);
                            dcActionResultInfo.setEntities(entityIdentifiers);
                            dcActionLotResult.add(dcActionResultInfo);
                            dcActionInfo_count++;
                        }
                    }else {
                        log.info("correspondingPO is other Fab");
                        findFlag = false;
                        List<Infos.EntityInhibitAttributesWithFabInfo> entityInhibitAttributesWithFabInfos = strStartLot_actionList_EffectSPCCheck_out.getEntityInhibitionsWithFabInfo();
                        for (Infos.EntityInhibitAttributesWithFabInfo entityInhibitAttributesWithFabInfo : entityInhibitAttributesWithFabInfos) {
                            if (CimStringUtils.equals(entityInhibitAttributesWithFabInfo.getEntityInhibitAttributes().getEntities().get(0).getClassName(), BizConstant.SP_INHIBITCLASSID_OPERATION)
                                    && ObjectIdentifier.equalsWithValue(entityInhibitAttributesWithFabInfo.getEntityInhibitAttributes().getEntities().get(0).getObjectID(), spcCheckLot.getProcessRouteID())
                                    && CimStringUtils.equals(entityInhibitAttributesWithFabInfo.getEntityInhibitAttributes().getEntities().get(0).getAttribution(), spcCheckLot.getProcessOperationNumber())) {
                                findFlag = true;
                                break;
                            }
                        }
                        if (!findFlag){
                            log.info("findFlag == FALSE");
                            entity_countForOtherFab++;
                            Infos.EntityInhibitAttributesWithFabInfo entityInhibitAttributesWithFabInfo = new Infos.EntityInhibitAttributesWithFabInfo();
                            entityInhibitAttributesWithFabInfos.add(entityInhibitAttributesWithFabInfo);
                            Infos.EntityInhibitAttributes entityInhibitAttributes = new Infos.EntityInhibitAttributes();
                            entityInhibitAttributesWithFabInfo.setEntityInhibitAttributes(entityInhibitAttributes);
                            List<Infos.EntityIdentifier> entities = new ArrayList<>();
                            entityInhibitAttributes.setEntities(entities);
                            Infos.EntityIdentifier entityIdentifier = new Infos.EntityIdentifier();
                            entities.add(entityIdentifier);
                            entityIdentifier.setClassName(BizConstant.SP_INHIBITCLASSID_OPERATION);
                            entityIdentifier.setObjectID(spcCheckLot.getProcessRouteID());
                            entityIdentifier.setAttribution(spcCheckLot.getProcessOperationNumber());
                            entityInhibitAttributes.setStartTimeStamp(CimDateUtils.getTimestampAsString(objCommon.getTimeStamp().getReportTimeStamp()));
                            entityInhibitAttributes.setEndTimeStamp(BizConstant.SP_TIMESTAMP_NIL_OBJECT_STRING);
                            entityInhibitAttributes.setReasonCode(BizConstant.SP_REASON_SPCOUTOFRANGEINHIBIT);
                            entityInhibitAttributes.setOwnerID(objCommon.getUser().getUserID());
                            entityInhibitAttributes.setClaimedTimeStamp(CimDateUtils.getTimestampAsString(objCommon.getTimeStamp().getReportTimeStamp()));
                            /**
                             * Task-311 add ocapNo for constraint memo
                             */
                            entityInhibitAttributes.setMemo(ocapMemo);
                        }
                        findFlag = false;
                        for (int n = 0 ; n < dcActionInfo_count ; n++ ) {
                            if (CimArrayUtils.getSize(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getEntities()) == 1 ){
                                if (CimStringUtils.equals(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getEntities().get(0).getClassName(),BizConstant.SP_INHIBITCLASSID_OPERATION)
                                        && ObjectIdentifier.equalsWithValue(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getEntities().get(0).getObjectID(), spcCheckLot.getProcessRouteID())
                                        && CimStringUtils.equals(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getReasonCode(), BizConstant.SP_REASON_SPCOUTOFRANGEINHIBIT)
                                        && CimStringUtils.equals(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getCorrespondingObjRefPO(), spcCheckLot.getProcessObjrefPO())
                                        && ObjectIdentifier.equalsWithValue(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getLotID(), tempLotID)
                                        && CimStringUtils.equals(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getEntities().get(0).getAttribution(), spcCheckLot.getProcessOperationNumber())){
                                    log.info("break!!");
                                    findFlag = TRUE;
                                    break;
                                }
                            }
                        }
                        if (!findFlag){
                            log.info("findFlag == FALSE  [machineID]");
                            // Add ActionResultInfo
                            List<Infos.DCActionResultInfo> dcActionLotResult = dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo();
                            Infos.DCActionResultInfo  dcActionResultInfo = new Infos.DCActionResultInfo();
                            dcActionResultInfo.setLotID(tempLotID);
                            dcActionResultInfo.setMonitorLotFlag(false);
                            dcActionResultInfo.setDcDefID(spcCheckLot.getDcDefID());
                            dcActionResultInfo.setDcSpecID(spcCheckLot.getDcSpecID());
                            dcActionResultInfo.setCheckType(BizConstant.SP_ACTIONRESULT_CHECKTYPE_SPC);
                            dcActionResultInfo.setReasonCode(BizConstant.SP_REASON_SPCOUTOFRANGEINHIBIT);
                            dcActionResultInfo.setActionCode(BizConstant.SP_ACTIONCODE_PROCESSHOLD);
                            dcActionResultInfo.setCorrespondingObjRefPO(spcCheckLot.getProcessObjrefPO());
                            dcActionResultInfo.setBankID("");
                            dcActionResultInfo.setReworkRouteID("");
                            List<Infos.EntityIdentifier> entityIdentifiers = new ArrayList<>();
                            Infos.EntityIdentifier entityIdentifier = new Infos.EntityIdentifier();
                            entityIdentifier.setClassName(BizConstant.SP_INHIBITCLASSID_OPERATION);
                            entityIdentifier.setAttribution(spcCheckLot.getProcessOperationNumber());
                            entityIdentifier.setObjectID(spcCheckLot.getProcessRouteID());
                            entityIdentifiers.add(entityIdentifier);
                            dcActionResultInfo.setEntities(entityIdentifiers);
                            dcActionLotResult.add(dcActionResultInfo);
                            dcActionInfo_count++;
                        }
                    }
                }

                // Inhibit Equipment and Recipe
                if ( equipment_and_recipe_hold_flag ){
                    log.info("equipment_and_recipe_hold_flag == TRUE");
                    if (!requestOtherFabFlag){
                        //-------------------------------------------------------------------
                        //  Check quipment_and_recipe_hold_inhibition is already registed or not.
                        //  If only not existed, entry new inhibition.
                        //-------------------------------------------------------------------
                        findFlag = false;
                        List<Infos.EntityInhibitDetailAttributes> entityInhibitions = strStartLot_actionList_EffectSPCCheck_out.getStrEntityInhibitions();
                        for (Infos.EntityInhibitDetailAttributes entityInhibition : entityInhibitions) {
                            if (CimStringUtils.equals(entityInhibition.getEntities().get(0).getClassName(), BizConstant.SP_INHIBITCLASSID_EQUIPMENT)
                                    && ObjectIdentifier.equalsWithValue(entityInhibition.getEntities().get(0).getObjectID(), spcCheckLot.getProcessEquipmentID())
                                    && CimStringUtils.equals(entityInhibition.getEntities().get(1).getClassName(), BizConstant.SP_INHIBITCLASSID_MACHINERECIPE)
                                    && ObjectIdentifier.equalsWithValue(entityInhibition.getEntities().get(1).getObjectID(), spcCheckLot.getProcessMachineRecipeID())) {
                                findFlag = true;
                                break;
                            }
                        }
                        if (!findFlag){
                            log.info("findFlag == FALSE");
                            entity_count++;
                            Infos.EntityInhibitDetailAttributes entityInhibitDetailAttribute = new Infos.EntityInhibitDetailAttributes();
                            entityInhibitions.add(entityInhibitDetailAttribute);
                            List<Infos.EntityIdentifier> entities = new ArrayList<>();
                            entityInhibitDetailAttribute.setEntities(entities);
                            Infos.EntityIdentifier entityIdentifier1 = new Infos.EntityIdentifier();
                            Infos.EntityIdentifier entityIdentifier2 = new Infos.EntityIdentifier();
                            entities.add(entityIdentifier1);
                            entities.add(entityIdentifier2);
                            entityIdentifier1.setClassName(BizConstant.SP_INHIBITCLASSID_EQUIPMENT);
                            entityIdentifier1.setAttribution("");
                            entityIdentifier1.setObjectID(spcCheckLot.getProcessEquipmentID());
                            entityIdentifier2.setClassName(BizConstant.SP_INHIBITCLASSID_MACHINERECIPE);
                            entityIdentifier2.setAttribution("");
                            entityIdentifier2.setObjectID(spcCheckLot.getProcessMachineRecipeID());
                            entityInhibitDetailAttribute.setStartTimeStamp(CimDateUtils.getTimestampAsString(objCommon.getTimeStamp().getReportTimeStamp()));
                            entityInhibitDetailAttribute.setEndTimeStamp(BizConstant.SP_TIMESTAMP_NIL_OBJECT_STRING);
                            entityInhibitDetailAttribute.setReasonCode(BizConstant.SP_REASON_SPCOUTOFRANGEINHIBIT);
                            entityInhibitDetailAttribute.setOwnerID(objCommon.getUser().getUserID());
                            entityInhibitDetailAttribute.setClaimedTimeStamp(CimDateUtils.getTimestampAsString(objCommon.getTimeStamp().getReportTimeStamp()));
                            /**
                             * Task-311 add ocapNo for constraint memo
                             */
                            entityInhibitDetailAttribute.setMemo(ocapMemo);
                        }
                        findFlag = false;
                        for (int n = 0 ; n < dcActionInfo_count ; n++ ) {
                            if (CimArrayUtils.getSize(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getEntities()) == 2){
                                if (CimStringUtils.equals(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getEntities().get(0).getClassName(),BizConstant.SP_INHIBITCLASSID_EQUIPMENT)
                                        && ObjectIdentifier.equalsWithValue(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getEntities().get(0).getObjectID(), spcCheckLot.getProcessEquipmentID())
                                        && CimStringUtils.equals(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getEntities().get(0).getClassName(),BizConstant.SP_INHIBITCLASSID_MACHINERECIPE)
                                        && ObjectIdentifier.equalsWithValue(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getEntities().get(0).getObjectID(), spcCheckLot.getProcessMachineRecipeID())
                                        && CimStringUtils.equals(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getReasonCode(),BizConstant.SP_REASON_SPCOUTOFRANGEINHIBIT)
                                        && CimStringUtils.equals(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getCorrespondingObjRefPO(),spcCheckLot.getProcessObjrefPO())
                                        && ObjectIdentifier.equalsWithValue(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getLotID(), tempLotID)){
                                    log.info("break!!");
                                    findFlag = TRUE;
                                    break;
                                }
                            }
                        }
                        if (!findFlag){
                            log.info("findFlag == FALSE  [machineID]");
                            // Add ActionResultInfo
                            List<Infos.DCActionResultInfo> dcActionLotResult = dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo();
                            Infos.DCActionResultInfo  dcActionResultInfo = new Infos.DCActionResultInfo();
                            dcActionResultInfo.setLotID(tempLotID);
                            dcActionResultInfo.setMonitorLotFlag(false);
                            dcActionResultInfo.setDcDefID(spcCheckLot.getDcDefID());
                            dcActionResultInfo.setDcSpecID(spcCheckLot.getDcSpecID());
                            dcActionResultInfo.setCheckType(BizConstant.SP_ACTIONRESULT_CHECKTYPE_SPC);
                            dcActionResultInfo.setReasonCode(BizConstant.SP_REASON_SPCOUTOFRANGEINHIBIT);
                            dcActionResultInfo.setActionCode(BizConstant.SP_ACTIONCODE_EQUIPMENT_AND_RECIPEHOLD);
                            dcActionResultInfo.setCorrespondingObjRefPO(spcCheckLot.getProcessObjrefPO());
                            dcActionResultInfo.setBankID("");
                            dcActionResultInfo.setReworkRouteID("");
                            List<Infos.EntityIdentifier> entityIdentifiers = new ArrayList<>();
                            Infos.EntityIdentifier entityIdentifier1 = new Infos.EntityIdentifier();
                            entityIdentifier1.setClassName(BizConstant.SP_INHIBITCLASSID_EQUIPMENT);
                            entityIdentifier1.setAttribution("");
                            entityIdentifier1.setObjectID(spcCheckLot.getProcessEquipmentID());
                            entityIdentifiers.add(entityIdentifier1);
                            Infos.EntityIdentifier entityIdentifier2 = new Infos.EntityIdentifier();
                            entityIdentifier2.setClassName(BizConstant.SP_INHIBITCLASSID_MACHINERECIPE);
                            entityIdentifier2.setAttribution("");
                            entityIdentifier2.setObjectID(spcCheckLot.getProcessMachineRecipeID());
                            entityIdentifiers.add(entityIdentifier2);
                            dcActionResultInfo.setEntities(entityIdentifiers);
                            dcActionLotResult.add(dcActionResultInfo);
                            dcActionInfo_count++;
                        }
                    }else {
                        log.info("correspondingPO is other Fab");
                        //-------------------------------------------------------------------
                        //  Check equipment_and_recipe_hold_inhibition is already registed or not.
                        //  If only not existed, entry new inhibition.
                        //-------------------------------------------------------------------
                        findFlag = false;
                        List<Infos.EntityInhibitAttributesWithFabInfo> entityInhibitAttributesWithFabInfos = strStartLot_actionList_EffectSPCCheck_out.getEntityInhibitionsWithFabInfo();
                        for (Infos.EntityInhibitAttributesWithFabInfo entityInhibitAttributesWithFabInfo : entityInhibitAttributesWithFabInfos) {
                            if (CimArrayUtils.getSize(entityInhibitAttributesWithFabInfo.getEntityInhibitAttributes().getEntities()) == 2){
                                if (CimStringUtils.equals(entityInhibitAttributesWithFabInfo.getEntityInhibitAttributes().getEntities().get(0).getClassName(), BizConstant.SP_INHIBITCLASSID_EQUIPMENT)
                                        && ObjectIdentifier.equalsWithValue(entityInhibitAttributesWithFabInfo.getEntityInhibitAttributes().getEntities().get(0).getObjectID(), spcCheckLot.getProcessEquipmentID())
                                        && CimStringUtils.equals(entityInhibitAttributesWithFabInfo.getEntityInhibitAttributes().getEntities().get(1).getClassName(), BizConstant.SP_INHIBITCLASSID_MACHINERECIPE)
                                        && ObjectIdentifier.equalsWithValue(entityInhibitAttributesWithFabInfo.getEntityInhibitAttributes().getEntities().get(1).getObjectID(), spcCheckLot.getProcessMachineRecipeID())) {
                                    findFlag = true;
                                    break;
                                }
                            }
                        }
                        if (!findFlag){
                            log.info("findFlag == FALSE");
                            entity_countForOtherFab++;
                            Infos.EntityInhibitAttributesWithFabInfo entityInhibitAttributesWithFabInfo = new Infos.EntityInhibitAttributesWithFabInfo();
                            entityInhibitAttributesWithFabInfos.add(entityInhibitAttributesWithFabInfo);
                            Infos.EntityInhibitAttributes entityInhibitAttributes = new Infos.EntityInhibitAttributes();
                            entityInhibitAttributesWithFabInfo.setEntityInhibitAttributes(entityInhibitAttributes);
                            List<Infos.EntityIdentifier> entities = new ArrayList<>();
                            entityInhibitAttributes.setEntities(entities);
                            Infos.EntityIdentifier entityIdentifier1 = new Infos.EntityIdentifier();
                            Infos.EntityIdentifier entityIdentifier2 = new Infos.EntityIdentifier();
                            entities.add(entityIdentifier1);
                            entities.add(entityIdentifier2);
                            entityIdentifier1.setClassName(BizConstant.SP_INHIBITCLASSID_EQUIPMENT);
                            entityIdentifier1.setAttribution("");
                            entityIdentifier1.setObjectID(spcCheckLot.getProcessEquipmentID());
                            entityIdentifier2.setClassName(BizConstant.SP_INHIBITCLASSID_MACHINERECIPE);
                            entityIdentifier2.setAttribution("");
                            entityIdentifier2.setObjectID(spcCheckLot.getProcessMachineRecipeID());
                            entityInhibitAttributes.setStartTimeStamp(CimDateUtils.getTimestampAsString(objCommon.getTimeStamp().getReportTimeStamp()));
                            entityInhibitAttributes.setEndTimeStamp(BizConstant.SP_TIMESTAMP_NIL_OBJECT_STRING);
                            entityInhibitAttributes.setReasonCode(BizConstant.SP_REASON_SPCOUTOFRANGEINHIBIT);
                            entityInhibitAttributes.setOwnerID(objCommon.getUser().getUserID());
                            entityInhibitAttributes.setClaimedTimeStamp(CimDateUtils.getTimestampAsString(objCommon.getTimeStamp().getReportTimeStamp()));
                            /**
                             * Task-311 add ocapNo for constraint memo
                             */
                            entityInhibitAttributes.setMemo(ocapMemo);
                        }
                        findFlag = false;
                        for (int n = 0 ; n < dcActionInfo_count ; n++ ) {
                            if (CimArrayUtils.getSize(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getEntities()) == 2){
                                if (CimStringUtils.equals(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getEntities().get(0).getClassName(),BizConstant.SP_INHIBITCLASSID_EQUIPMENT)
                                        && ObjectIdentifier.equalsWithValue(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getEntities().get(0).getObjectID(), spcCheckLot.getProcessEquipmentID())
                                        && CimStringUtils.equals(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getEntities().get(1).getClassName(),BizConstant.SP_INHIBITCLASSID_MACHINERECIPE)
                                        && ObjectIdentifier.equalsWithValue(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getEntities().get(1).getObjectID(), spcCheckLot.getProcessMachineRecipeID())
                                        && CimStringUtils.equals(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getReasonCode(),BizConstant.SP_REASON_SPCOUTOFRANGEINHIBIT)
                                        && CimStringUtils.equals(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getCorrespondingObjRefPO(),spcCheckLot.getProcessObjrefPO())
                                        && ObjectIdentifier.equalsWithValue(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getLotID(), tempLotID)){
                                    log.info("break!!");
                                    findFlag = TRUE;
                                    break;
                                }
                            }
                        }
                        if (!findFlag){
                            log.info("findFlag == FALSE  [machineID]");
                            // Add ActionResultInfo
                            List<Infos.DCActionResultInfo> dcActionLotResult = dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo();
                            Infos.DCActionResultInfo  dcActionResultInfo = new Infos.DCActionResultInfo();
                            dcActionResultInfo.setLotID(tempLotID);
                            dcActionResultInfo.setMonitorLotFlag(false);
                            dcActionResultInfo.setDcDefID(spcCheckLot.getDcDefID());
                            dcActionResultInfo.setDcSpecID(spcCheckLot.getDcSpecID());
                            dcActionResultInfo.setCheckType(BizConstant.SP_ACTIONRESULT_CHECKTYPE_SPC);
                            dcActionResultInfo.setReasonCode(BizConstant.SP_REASON_SPCOUTOFRANGEINHIBIT);
                            dcActionResultInfo.setActionCode(BizConstant.SP_ACTIONCODE_EQUIPMENT_AND_RECIPEHOLD);
                            dcActionResultInfo.setCorrespondingObjRefPO(spcCheckLot.getProcessObjrefPO());
                            dcActionResultInfo.setBankID("");
                            dcActionResultInfo.setReworkRouteID("");
                            List<Infos.EntityIdentifier> entityIdentifiers = new ArrayList<>();
                            Infos.EntityIdentifier entityIdentifier1 = new Infos.EntityIdentifier();
                            entityIdentifier1.setClassName(BizConstant.SP_INHIBITCLASSID_EQUIPMENT);
                            entityIdentifier1.setAttribution("");
                            entityIdentifier1.setObjectID(spcCheckLot.getProcessEquipmentID());
                            entityIdentifiers.add(entityIdentifier1);
                            Infos.EntityIdentifier entityIdentifier2 = new Infos.EntityIdentifier();
                            entityIdentifier2.setClassName(BizConstant.SP_INHIBITCLASSID_MACHINERECIPE);
                            entityIdentifier2.setAttribution("");
                            entityIdentifier2.setObjectID(spcCheckLot.getProcessMachineRecipeID());
                            entityIdentifiers.add(entityIdentifier2);
                            dcActionResultInfo.setEntities(entityIdentifiers);
                            dcActionLotResult.add(dcActionResultInfo);
                            dcActionInfo_count++;
                        }
                    }
                }

                // Inhibit Process Definition and Equipment
                if (process_and_equipment_hold_flag){
                    if (!requestOtherFabFlag){
                        CimProcessDefinition aMainProcessDefinition = baseCoreFactory.getBO(CimProcessDefinition.class,spcCheckLot.getProcessRouteID());
                        Validations.check(aMainProcessDefinition == null ,new OmCode(retCodeConfig.getNotFoundProcessDefinition(), ObjectIdentifier.fetchValue(spcCheckLot.getProcessRouteID())));
                        CimProcessFlow aProcessFlow = aMainProcessDefinition.getActiveMainProcessFlow();
                        Validations.check(aProcessFlow == null, new OmCode(retCodeConfig.getNotFoundProcessFlow(), ""));
                        if (aProcessFlow.isNewlyCreated()){
                            log.info("aProcessFlow->isNewlyCreated() is TRUE");
                            if (aPosLot != null){
                                log.info("aLot is not nil");
                                ProcessFlow aPF = aPosLot.getProcessFlow();
                                aProcessFlow = (CimProcessFlow)aPF;
                                Validations.check(aProcessFlow == null, new OmCode(retCodeConfig.getNotFoundProcessFlow(),""));
                            }else {
                                throw new ServiceException(new OmCode(retCodeConfig.getNotFoundProcessFlow(), ""));
                            }
                        }

                        CimProcessOperationSpecification aPos;
                        AtomicReference<CimProcessFlow> aOutMainPF = new AtomicReference<>();
                        AtomicReference<CimProcessFlow> aOutModulePF = new AtomicReference<>();
                        aPos = aProcessFlow.getProcessOperationSpecificationFor(spcCheckLot.getProcessOperationNumber(),aOutMainPF,aOutModulePF);
                        CimProcessDefinition aPosPD = null;
                        if (aPos != null){
                            log.info("aPOS is not nil");
                            List<ProcessDefinition> aProcessDefSeq = aPos.getProcessDefinitions();
                            aPosPD = (CimProcessDefinition)aProcessDefSeq.get(0);
                            Validations.check(aPosPD == null, new OmCode(retCodeConfig.getNotFoundProcessDefinition(), ""));
                        }
                        ObjectIdentifier processPDID = ObjectIdentifier.build(aPosPD.getIdentifier(), aPosPD.getPrimaryKey());
                        findFlag = false;
                        //-------------------------------------------------------------------
                        //  Check process_and_equipment_hold_inhibition is already registed or not.
                        //  If only not existed, entry new inhibition.
                        //-------------------------------------------------------------------
                        List<Infos.EntityInhibitDetailAttributes> entityInhibitions = strStartLot_actionList_EffectSPCCheck_out.getStrEntityInhibitions();
                        for (Infos.EntityInhibitDetailAttributes entityInhibition : entityInhibitions) {
                            if (CimArrayUtils.getSize(entityInhibition.getEntities()) == 2){
                                if (CimStringUtils.equals(entityInhibition.getEntities().get(0).getClassName(), BizConstant.SP_INHIBITCLASSID_OPERATION)
                                        && ObjectIdentifier.equalsWithValue(entityInhibition.getEntities().get(0).getObjectID(), processPDID)
                                        && CimStringUtils.equals(entityInhibition.getEntities().get(1).getClassName(), BizConstant.SP_INHIBITCLASSID_EQUIPMENT)
                                        && ObjectIdentifier.equalsWithValue(entityInhibition.getEntities().get(1).getObjectID(), spcCheckLot.getProcessEquipmentID())) {
                                    findFlag = true;
                                    break;
                                }
                            }
                        }
                        if (!findFlag){
                            log.info("findFlag == FALSE");
                            entity_count++;
                            Infos.EntityInhibitDetailAttributes entityInhibitDetailAttribute = new Infos.EntityInhibitDetailAttributes();
                            entityInhibitions.add(entityInhibitDetailAttribute);
                            List<Infos.EntityIdentifier> entities = new ArrayList<>();
                            entityInhibitDetailAttribute.setEntities(entities);
                            Infos.EntityIdentifier entityIdentifier1 = new Infos.EntityIdentifier();
                            Infos.EntityIdentifier entityIdentifier2 = new Infos.EntityIdentifier();
                            entities.add(entityIdentifier1);
                            entities.add(entityIdentifier2);
                            entityIdentifier1.setClassName(BizConstant.SP_INHIBITCLASSID_PROCESS);
                            entityIdentifier1.setAttribution("");
                            entityIdentifier1.setObjectID(processPDID);
                            entityIdentifier2.setClassName(BizConstant.SP_INHIBITCLASSID_EQUIPMENT);
                            entityIdentifier2.setAttribution("");
                            entityIdentifier2.setObjectID(spcCheckLot.getProcessEquipmentID());
                            entityInhibitDetailAttribute.setStartTimeStamp(CimDateUtils.getTimestampAsString(objCommon.getTimeStamp().getReportTimeStamp()));
                            entityInhibitDetailAttribute.setEndTimeStamp(BizConstant.SP_TIMESTAMP_NIL_OBJECT_STRING);
                            entityInhibitDetailAttribute.setReasonCode(BizConstant.SP_REASON_SPCOUTOFRANGEINHIBIT);
                            entityInhibitDetailAttribute.setOwnerID(objCommon.getUser().getUserID());
                            entityInhibitDetailAttribute.setClaimedTimeStamp(CimDateUtils.getTimestampAsString(objCommon.getTimeStamp().getReportTimeStamp()));
                            /**
                             * Task-311 add ocapNo for constraint memo
                             */
                            entityInhibitDetailAttribute.setMemo(ocapMemo);
                        }
                        findFlag = false;
                        for (int n = 0 ; n < dcActionInfo_count ; n++ ) {
                            if (CimArrayUtils.getSize(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getEntities()) == 2 ){
                                if (CimStringUtils.equals(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getEntities().get(0).getClassName(),BizConstant.SP_INHIBITCLASSID_PROCESS)
                                        && ObjectIdentifier.equalsWithValue(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getEntities().get(0).getObjectID(), processPDID)
                                        && CimStringUtils.equals(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getEntities().get(1).getClassName(),BizConstant.SP_INHIBITCLASSID_EQUIPMENT)
                                        && ObjectIdentifier.equalsWithValue(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getEntities().get(1).getObjectID(), spcCheckLot.getProcessEquipmentID())
                                        && CimStringUtils.equals(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getReasonCode(),BizConstant.SP_REASON_SPCOUTOFRANGEINHIBIT)
                                        && CimStringUtils.equals(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getCorrespondingObjRefPO(),spcCheckLot.getProcessObjrefPO())
                                        && ObjectIdentifier.equalsWithValue(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getLotID(), tempLotID)){
                                    log.info("break!!");
                                    findFlag = TRUE;
                                    break;
                                }
                            }
                        }
                        if (!findFlag){
                            log.info("findFlag == FALSE  [machineID]");
                            // Add ActionResultInfo
                            List<Infos.DCActionResultInfo> dcActionLotResult = dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo();
                            Infos.DCActionResultInfo dcActionResultInfo = new Infos.DCActionResultInfo();
                            dcActionResultInfo.setLotID(tempLotID);
                            dcActionResultInfo.setMonitorLotFlag(false);
                            dcActionResultInfo.setDcDefID(spcCheckLot.getDcDefID());
                            dcActionResultInfo.setDcSpecID(spcCheckLot.getDcSpecID());
                            dcActionResultInfo.setCheckType(BizConstant.SP_ACTIONRESULT_CHECKTYPE_SPC);
                            dcActionResultInfo.setReasonCode(BizConstant.SP_REASON_SPCOUTOFRANGEINHIBIT);
                            dcActionResultInfo.setActionCode(BizConstant.SP_ACTIONCODE_PROCESS_AND_EQUIPMENTHOLD);
                            dcActionResultInfo.setCorrespondingObjRefPO(spcCheckLot.getProcessObjrefPO());
                            dcActionResultInfo.setBankID("");
                            dcActionResultInfo.setReworkRouteID("");
                            List<Infos.EntityIdentifier> entityIdentifiers = new ArrayList<>();
                            Infos.EntityIdentifier entityIdentifier1 = new Infos.EntityIdentifier();
                            entityIdentifier1.setClassName(BizConstant.SP_INHIBITCLASSID_PROCESS);
                            entityIdentifier1.setAttribution("");
                            entityIdentifier1.setObjectID(processPDID);
                            entityIdentifiers.add(entityIdentifier1);
                            Infos.EntityIdentifier entityIdentifier2 = new Infos.EntityIdentifier();
                            entityIdentifier2.setClassName(BizConstant.SP_INHIBITCLASSID_EQUIPMENT);
                            entityIdentifier2.setAttribution("");
                            entityIdentifier2.setObjectID(spcCheckLot.getProcessEquipmentID());
                            entityIdentifiers.add(entityIdentifier2);
                            dcActionResultInfo.setEntities(entityIdentifiers);
                            dcActionLotResult.add(dcActionResultInfo);
                            dcActionInfo_count++;
                        }
                    }else {
                        log.info("correspondingPO is other Fab");
                        //-------------------------------------------------------------------
                        //  Check process_and_equipment_hold_inhibition is already registed or not.
                        //  If only not existed, entry new inhibition.
                        //-------------------------------------------------------------------
                        findFlag = false;
                        List<Infos.EntityInhibitAttributesWithFabInfo> entityInhibitAttributesWithFabInfos = strStartLot_actionList_EffectSPCCheck_out.getEntityInhibitionsWithFabInfo();
                        for (Infos.EntityInhibitAttributesWithFabInfo entityInhibitAttributesWithFabInfo : entityInhibitAttributesWithFabInfos) {
                            if (CimArrayUtils.getSize(entityInhibitAttributesWithFabInfo.getEntityInhibitAttributes().getEntities()) == 2){
                                if (CimStringUtils.equals(entityInhibitAttributesWithFabInfo.getEntityInhibitAttributes().getEntities().get(0).getClassName(), BizConstant.SP_INHIBITCLASSID_OPERATION)
                                        && ObjectIdentifier.equalsWithValue(entityInhibitAttributesWithFabInfo.getEntityInhibitAttributes().getEntities().get(0).getObjectID(), strProcessOperationInfo.getPdID())
                                        && CimStringUtils.equals(entityInhibitAttributesWithFabInfo.getEntityInhibitAttributes().getEntities().get(1).getClassName(), BizConstant.SP_INHIBITCLASSID_EQUIPMENT)
                                        && ObjectIdentifier.equalsWithValue(entityInhibitAttributesWithFabInfo.getEntityInhibitAttributes().getEntities().get(1).getObjectID(), spcCheckLot.getProcessEquipmentID())) {
                                    findFlag = true;
                                    break;
                                }
                            }
                        }
                        if (!findFlag){
                            log.info("findFlag == FALSE");
                            entity_countForOtherFab++;
                            Infos.EntityInhibitAttributesWithFabInfo entityInhibitAttributesWithFabInfo = new Infos.EntityInhibitAttributesWithFabInfo();
                            entityInhibitAttributesWithFabInfos.add(entityInhibitAttributesWithFabInfo);
                            Infos.EntityInhibitAttributes entityInhibitAttributes = new Infos.EntityInhibitAttributes();
                            entityInhibitAttributesWithFabInfo.setEntityInhibitAttributes(entityInhibitAttributes);
                            List<Infos.EntityIdentifier> entities = new ArrayList<>();
                            entityInhibitAttributes.setEntities(entities);
                            Infos.EntityIdentifier entityIdentifier1 = new Infos.EntityIdentifier();
                            Infos.EntityIdentifier entityIdentifier2 = new Infos.EntityIdentifier();
                            entities.add(entityIdentifier1);
                            entities.add(entityIdentifier2);
                            entityIdentifier1.setClassName(BizConstant.SP_INHIBITCLASSID_PROCESS);
                            entityIdentifier1.setAttribution("");
                            entityIdentifier1.setObjectID(strProcessOperationInfo.getPdID());
                            entityIdentifier2.setClassName(BizConstant.SP_INHIBITCLASSID_EQUIPMENT);
                            entityIdentifier2.setAttribution("");
                            entityIdentifier2.setObjectID(spcCheckLot.getProcessEquipmentID());
                            entityInhibitAttributes.setStartTimeStamp(CimDateUtils.getTimestampAsString(objCommon.getTimeStamp().getReportTimeStamp()));
                            entityInhibitAttributes.setEndTimeStamp(BizConstant.SP_TIMESTAMP_NIL_OBJECT_STRING);
                            entityInhibitAttributes.setReasonCode(BizConstant.SP_REASON_SPCOUTOFRANGEINHIBIT);
                            entityInhibitAttributes.setOwnerID(objCommon.getUser().getUserID());
                            entityInhibitAttributes.setClaimedTimeStamp(CimDateUtils.getTimestampAsString(objCommon.getTimeStamp().getReportTimeStamp()));
                            /**
                             * Task-311 add ocapNo for constraint memo
                             */
                            entityInhibitAttributes.setMemo(ocapMemo);
                        }
                        findFlag = false;
                        for (int n = 0 ; n < dcActionInfo_count ; n++ ) {
                            if (CimArrayUtils.getSize(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getEntities()) == 2){
                                if (CimStringUtils.equals(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getEntities().get(0).getClassName(),BizConstant.SP_INHIBITCLASSID_PROCESS)
                                        && ObjectIdentifier.equalsWithValue(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getEntities().get(0).getObjectID(), strProcessOperationInfo.getPdID())
                                        && CimStringUtils.equals(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getEntities().get(1).getClassName(),BizConstant.SP_INHIBITCLASSID_EQUIPMENT)
                                        && ObjectIdentifier.equalsWithValue(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getEntities().get(1).getObjectID(), spcCheckLot.getProcessEquipmentID())
                                        && CimStringUtils.equals(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getReasonCode(),BizConstant.SP_REASON_SPCOUTOFRANGEINHIBIT)
                                        && CimStringUtils.equals(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getCorrespondingObjRefPO(),spcCheckLot.getProcessObjrefPO())
                                        && ObjectIdentifier.equalsWithValue(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getLotID(), tempLotID)){
                                    log.info("break!!");
                                    findFlag = TRUE;
                                    break;
                                }
                            }
                        }
                        if (!findFlag){
                            log.info("findFlag == FALSE ");
                            // Add ActionResultInfo
                            List<Infos.DCActionResultInfo> dcActionLotResult = dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo();
                            Infos.DCActionResultInfo dcActionResultInfo = new Infos.DCActionResultInfo();
                            dcActionResultInfo.setLotID(tempLotID);
                            dcActionResultInfo.setMonitorLotFlag(false);
                            dcActionResultInfo.setDcDefID(spcCheckLot.getDcDefID());
                            dcActionResultInfo.setDcSpecID(spcCheckLot.getDcSpecID());
                            dcActionResultInfo.setCheckType(BizConstant.SP_ACTIONRESULT_CHECKTYPE_SPC);
                            dcActionResultInfo.setReasonCode(BizConstant.SP_REASON_SPCOUTOFRANGEINHIBIT);
                            dcActionResultInfo.setActionCode(BizConstant.SP_ACTIONCODE_PROCESS_AND_EQUIPMENTHOLD);
                            dcActionResultInfo.setCorrespondingObjRefPO(spcCheckLot.getProcessObjrefPO());
                            dcActionResultInfo.setBankID("");
                            dcActionResultInfo.setReworkRouteID("");
                            List<Infos.EntityIdentifier> entityIdentifiers = new ArrayList<>();
                            Infos.EntityIdentifier entityIdentifier1 = new Infos.EntityIdentifier();
                            entityIdentifier1.setClassName(BizConstant.SP_INHIBITCLASSID_PROCESS);
                            entityIdentifier1.setAttribution("");
                            entityIdentifier1.setObjectID(strProcessOperationInfo.getPdID());
                            entityIdentifiers.add(entityIdentifier1);
                            Infos.EntityIdentifier entityIdentifier2 = new Infos.EntityIdentifier();
                            entityIdentifier2.setClassName(BizConstant.SP_INHIBITCLASSID_EQUIPMENT);
                            entityIdentifier2.setAttribution("");
                            entityIdentifier2.setObjectID(spcCheckLot.getProcessEquipmentID());
                            entityIdentifiers.add(entityIdentifier2);
                            dcActionResultInfo.setEntities(entityIdentifiers);
                            dcActionLotResult.add(dcActionResultInfo);
                            dcActionInfo_count++;
                        }
                    }
                }

                // Inhibit Chamber
                if (chamber_hold_flag){
                    log.info("equipment_hold_flag == TRUE");
                    if (!requestOtherFabFlag){
                        findFlag= false;
                        List<Infos.EntityInhibitDetailAttributes> entityInhibitions = strStartLot_actionList_EffectSPCCheck_out.getStrEntityInhibitions();
                        for (Infos.EntityInhibitDetailAttributes entityInhibition : entityInhibitions) {
                            if (CimStringUtils.equals(entityInhibition.getEntities().get(0).getClassName(), BizConstant.SP_INHIBITCLASSID_CHAMBER)
                                    && ObjectIdentifier.equalsWithValue(entityInhibition.getEntities().get(0).getObjectID(), spcCheckLot.getProcessEquipmentID())
                                    && ObjectIdentifier.equalsWithValue(entityInhibition.getEntities().get(0).getAttribution(), spcCheckLot.getProcessChamberID())) {
                                findFlag = true;
                                break;
                            }
                        }
                        if (!findFlag){
                            log.info("findFlag == FALSE");
                            entity_count++;
                            Infos.EntityInhibitDetailAttributes entityInhibitDetailAttribute = new Infos.EntityInhibitDetailAttributes();
                            entityInhibitions.add(entityInhibitDetailAttribute);
                            List<Infos.EntityIdentifier> entities = new ArrayList<>();
                            entityInhibitDetailAttribute.setEntities(entities);
                            Infos.EntityIdentifier entityIdentifier = new Infos.EntityIdentifier();
                            entities.add(entityIdentifier);
                            entityIdentifier.setClassName(BizConstant.SP_INHIBITCLASSID_CHAMBER);
                            entityIdentifier.setAttribution(spcCheckLot.getProcessChamberID().getValue());
                            entityIdentifier.setObjectID(spcCheckLot.getProcessEquipmentID());
                            entityInhibitDetailAttribute.setStartTimeStamp(CimDateUtils.getTimestampAsString(objCommon.getTimeStamp().getReportTimeStamp()));
                            entityInhibitDetailAttribute.setEndTimeStamp(BizConstant.SP_TIMESTAMP_NIL_OBJECT_STRING);
                            entityInhibitDetailAttribute.setReasonCode(BizConstant.SP_REASON_SPCOUTOFRANGEINHIBIT);
                            entityInhibitDetailAttribute.setOwnerID(objCommon.getUser().getUserID());
                            entityInhibitDetailAttribute.setClaimedTimeStamp(CimDateUtils.getTimestampAsString(objCommon.getTimeStamp().getReportTimeStamp()));
                            /**
                             * Task-311 add ocapNo for constraint memo
                             */
                            entityInhibitDetailAttribute.setMemo(ocapMemo);
                        }
                        findFlag = false;
                        for (int n = 0 ; n < dcActionInfo_count ; n++ ) {
                            if (CimArrayUtils.getSize(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getEntities()) == 1){
                                if (CimStringUtils.equals(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getEntities().get(0).getClassName(),BizConstant.SP_INHIBITCLASSID_CHAMBER)
                                        && ObjectIdentifier.equalsWithValue(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getEntities().get(0).getObjectID(), spcCheckLot.getProcessEquipmentID())
                                        && ObjectIdentifier.equalsWithValue(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getEntities().get(0).getAttribution(), spcCheckLot.getProcessChamberID())
                                        && CimStringUtils.equals(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getReasonCode(),BizConstant.SP_REASON_SPCOUTOFRANGEINHIBIT)
                                        && CimStringUtils.equals(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getCorrespondingObjRefPO(),spcCheckLot.getProcessObjrefPO())
                                        && ObjectIdentifier.equalsWithValue(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getLotID(), tempLotID)){
                                    log.info("set findFlag = TRUE");
                                    findFlag = true;
                                    break;
                                }
                            }
                        }
                        if (!findFlag){
                            log.info("findFlag == FALSE");
                            // Add ActionResultInfo
                            List<Infos.DCActionResultInfo> dcActionLotResult = dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo();
                            Infos.DCActionResultInfo dcActionResultInfo = new Infos.DCActionResultInfo();
                            dcActionResultInfo.setLotID(tempLotID);
                            dcActionResultInfo.setMonitorLotFlag(false);
                            dcActionResultInfo.setDcDefID(spcCheckLot.getDcDefID());
                            dcActionResultInfo.setDcSpecID(spcCheckLot.getDcSpecID());
                            dcActionResultInfo.setCheckType(BizConstant.SP_ACTIONRESULT_CHECKTYPE_SPC);
                            dcActionResultInfo.setReasonCode(BizConstant.SP_REASON_SPCOUTOFRANGEINHIBIT);
                            dcActionResultInfo.setActionCode(BizConstant.SP_ACTIONCODE_CHAMBERHOLD);
                            dcActionResultInfo.setCorrespondingObjRefPO(spcCheckLot.getProcessObjrefPO());
                            dcActionResultInfo.setBankID("");
                            dcActionResultInfo.setReworkRouteID("");
                            List<Infos.EntityIdentifier> entityIdentifiers = new ArrayList<>();
                            Infos.EntityIdentifier entityIdentifier = new Infos.EntityIdentifier();
                            entityIdentifier.setClassName(BizConstant.SP_INHIBITCLASSID_CHAMBER);
                            entityIdentifier.setAttribution(spcCheckLot.getProcessChamberID().getValue());
                            entityIdentifier.setObjectID(spcCheckLot.getProcessEquipmentID());
                            entityIdentifiers.add(entityIdentifier);
                            dcActionResultInfo.setEntities(entityIdentifiers);
                            dcActionLotResult.add(dcActionResultInfo);
                            dcActionInfo_count++;
                        }
                    }else{
                        log.info("correspondingPO is other Fab");
                        //-------------------------------------------------------------------
                        //  Check equipment_hold_inhibition is already registed or not.
                        //  If only not existed, entry new inhibition.
                        //-------------------------------------------------------------------
                        findFlag = false;
                        List<Infos.EntityInhibitAttributesWithFabInfo> entityInhibitAttributesWithFabInfos = strStartLot_actionList_EffectSPCCheck_out.getEntityInhibitionsWithFabInfo();
                        for (Infos.EntityInhibitAttributesWithFabInfo entityInhibitAttributesWithFabInfo : entityInhibitAttributesWithFabInfos) {
                            if (CimStringUtils.equals(entityInhibitAttributesWithFabInfo.getEntityInhibitAttributes().getEntities().get(0).getClassName(), BizConstant.SP_INHIBITCLASSID_CHAMBER)
                                    && ObjectIdentifier.equalsWithValue(entityInhibitAttributesWithFabInfo.getEntityInhibitAttributes().getEntities().get(0).getObjectID(), spcCheckLot.getProcessEquipmentID())
                                    && ObjectIdentifier.equalsWithValue(entityInhibitAttributesWithFabInfo.getEntityInhibitAttributes().getEntities().get(0).getAttribution(), spcCheckLot.getProcessChamberID())) {
                                findFlag = true;
                                break;
                            }
                        }
                        if (!findFlag){
                            log.info("findFlag == FALSE");
                            entity_countForOtherFab++;
                            Infos.EntityInhibitAttributesWithFabInfo entityInhibitAttributesWithFabInfo = new Infos.EntityInhibitAttributesWithFabInfo();
                            entityInhibitAttributesWithFabInfos.add(entityInhibitAttributesWithFabInfo);
                            Infos.EntityInhibitAttributes entityInhibitAttributes = new Infos.EntityInhibitAttributes();
                            entityInhibitAttributesWithFabInfo.setEntityInhibitAttributes(entityInhibitAttributes);
                            List<Infos.EntityIdentifier> entities = new ArrayList<>();
                            entityInhibitAttributes.setEntities(entities);
                            Infos.EntityIdentifier entityIdentifier = new Infos.EntityIdentifier();
                            entities.add(entityIdentifier);
                            entityIdentifier.setClassName(BizConstant.SP_INHIBITCLASSID_CHAMBER);
                            entityIdentifier.setAttribution(spcCheckLot.getProcessChamberID().getValue());
                            entityIdentifier.setObjectID(spcCheckLot.getProcessEquipmentID());
                            entityInhibitAttributes.setStartTimeStamp(CimDateUtils.getTimestampAsString(objCommon.getTimeStamp().getReportTimeStamp()));
                            entityInhibitAttributes.setEndTimeStamp(BizConstant.SP_TIMESTAMP_NIL_OBJECT_STRING);
                            entityInhibitAttributes.setReasonCode(BizConstant.SP_REASON_SPCOUTOFRANGEINHIBIT);
                            entityInhibitAttributes.setOwnerID(objCommon.getUser().getUserID());
                            entityInhibitAttributes.setClaimedTimeStamp(CimDateUtils.getTimestampAsString(objCommon.getTimeStamp().getReportTimeStamp()));
                            /**
                             * Task-311 add ocapNo for constraint memo
                             */
                            entityInhibitAttributes.setMemo(ocapMemo);
                        }
                        findFlag = false;
                        for (int n = 0 ; n < dcActionInfo_count ; n++ ) {
                            if (CimArrayUtils.getSize(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getEntities()) == 1 ){
                                if (CimStringUtils.equals(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getEntities().get(0).getClassName(),BizConstant.SP_INHIBITCLASSID_CHAMBER)
                                        && ObjectIdentifier.equalsWithValue(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getEntities().get(0).getObjectID(), spcCheckLot.getProcessEquipmentID())
                                        && ObjectIdentifier.equalsWithValue(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getEntities().get(0).getAttribution(), spcCheckLot.getProcessChamberID())
                                        && CimStringUtils.equals(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getReasonCode(),BizConstant.SP_REASON_SPCOUTOFRANGEINHIBIT)
                                        && CimStringUtils.equals(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getCorrespondingObjRefPO(),spcCheckLot.getProcessObjrefPO())
                                        && ObjectIdentifier.equalsWithValue(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getLotID(), tempLotID)){
                                    log.info("break!!");
                                    findFlag = TRUE;
                                    break;
                                }
                            }
                        }
                        if (!findFlag){
                            log.info("findFlag == FALSE  [machineID]");
                            // Add ActionResultInfo
                            List<Infos.DCActionResultInfo> dcActionLotResult = dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo();
                            Infos.DCActionResultInfo  dcActionResultInfo = new Infos.DCActionResultInfo();
                            dcActionResultInfo.setLotID(tempLotID);
                            dcActionResultInfo.setMonitorLotFlag(false);
                            dcActionResultInfo.setDcDefID(spcCheckLot.getDcDefID());
                            dcActionResultInfo.setDcSpecID(spcCheckLot.getDcSpecID());
                            dcActionResultInfo.setCheckType(BizConstant.SP_ACTIONRESULT_CHECKTYPE_SPC);
                            dcActionResultInfo.setReasonCode(BizConstant.SP_REASON_SPCOUTOFRANGEINHIBIT);
                            dcActionResultInfo.setActionCode(BizConstant.SP_ACTIONCODE_CHAMBERHOLD);
                            dcActionResultInfo.setCorrespondingObjRefPO(spcCheckLot.getProcessObjrefPO());
                            dcActionResultInfo.setBankID("");
                            dcActionResultInfo.setReworkRouteID("");
                            List<Infos.EntityIdentifier> entityIdentifiers = new ArrayList<>();
                            Infos.EntityIdentifier entityIdentifier = new Infos.EntityIdentifier();
                            entityIdentifier.setClassName(BizConstant.SP_INHIBITCLASSID_CHAMBER);
                            entityIdentifier.setAttribution(spcCheckLot.getProcessChamberID().getValue());
                            entityIdentifier.setObjectID(spcCheckLot.getProcessEquipmentID());
                            entityIdentifiers.add(entityIdentifier);
                            dcActionResultInfo.setEntities(entityIdentifiers);
                            dcActionLotResult.add(dcActionResultInfo);
                            dcActionInfo_count++;
                        }
                    }
                }

                // Inhibit Chamber And Recipe
                if (chamber_recipe_hold_flag){
                    if (!requestOtherFabFlag){
                        findFlag = false;
                        //-------------------------------------------------------------------
                        //  Check process_and_equipment_hold_inhibition is already registed or not.
                        //  If only not existed, entry new inhibition.
                        //-------------------------------------------------------------------
                        List<Infos.EntityInhibitDetailAttributes> entityInhibitions = strStartLot_actionList_EffectSPCCheck_out.getStrEntityInhibitions();
                        for (Infos.EntityInhibitDetailAttributes entityInhibition : entityInhibitions) {
                            if (CimArrayUtils.getSize(entityInhibition.getEntities()) == 2){
                                if (CimStringUtils.equals(entityInhibition.getEntities().get(0).getClassName(), BizConstant.SP_INHIBITCLASSID_CHAMBER)
                                        && ObjectIdentifier.equalsWithValue(entityInhibition.getEntities().get(0).getObjectID(), spcCheckLot.getProcessEquipmentID())
                                        && ObjectIdentifier.equalsWithValue(entityInhibition.getEntities().get(0).getAttribution(), spcCheckLot.getProcessChamberID())
                                        && CimStringUtils.equals(entityInhibition.getEntities().get(1).getClassName(), BizConstant.SP_INHIBITCLASSID_MACHINERECIPE)
                                        && ObjectIdentifier.equalsWithValue(entityInhibition.getEntities().get(1).getObjectID(), spcCheckLot.getProcessMachineRecipeID())) {
                                    findFlag = true;
                                    break;
                                }
                            }
                        }
                        if (!findFlag){
                            log.info("findFlag == FALSE");
                            entity_count++;
                            Infos.EntityInhibitDetailAttributes entityInhibitDetailAttribute = new Infos.EntityInhibitDetailAttributes();
                            entityInhibitions.add(entityInhibitDetailAttribute);
                            List<Infos.EntityIdentifier> entities = new ArrayList<>();
                            entityInhibitDetailAttribute.setEntities(entities);
                            Infos.EntityIdentifier entityIdentifier1 = new Infos.EntityIdentifier();
                            Infos.EntityIdentifier entityIdentifier2 = new Infos.EntityIdentifier();
                            entities.add(entityIdentifier1);
                            entities.add(entityIdentifier2);
                            entityIdentifier1.setClassName(BizConstant.SP_INHIBITCLASSID_CHAMBER);
                            entityIdentifier1.setAttribution(spcCheckLot.getProcessChamberID().getValue());
                            entityIdentifier1.setObjectID(spcCheckLot.getProcessEquipmentID());
                            entityIdentifier2.setClassName(BizConstant.SP_INHIBITCLASSID_MACHINERECIPE);
                            entityIdentifier2.setAttribution("");
                            entityIdentifier2.setObjectID(spcCheckLot.getProcessMachineRecipeID());
                            entityInhibitDetailAttribute.setStartTimeStamp(CimDateUtils.getTimestampAsString(objCommon.getTimeStamp().getReportTimeStamp()));
                            entityInhibitDetailAttribute.setEndTimeStamp(BizConstant.SP_TIMESTAMP_NIL_OBJECT_STRING);
                            entityInhibitDetailAttribute.setReasonCode(BizConstant.SP_REASON_SPCOUTOFRANGEINHIBIT);
                            entityInhibitDetailAttribute.setOwnerID(objCommon.getUser().getUserID());
                            entityInhibitDetailAttribute.setClaimedTimeStamp(CimDateUtils.getTimestampAsString(objCommon.getTimeStamp().getReportTimeStamp()));
                            /**
                             * Task-311 add ocapNo for constraint memo
                             */
                            entityInhibitDetailAttribute.setMemo(ocapMemo);
                        }
                        findFlag = false;
                        for (int n = 0 ; n < dcActionInfo_count ; n++ ) {
                            if (CimArrayUtils.getSize(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getEntities()) == 2 ){
                                if (CimStringUtils.equals(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getEntities().get(0).getClassName(),BizConstant.SP_INHIBITCLASSID_CHAMBER)
                                        && ObjectIdentifier.equalsWithValue(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getEntities().get(0).getObjectID(), spcCheckLot.getProcessEquipmentID())
                                        && ObjectIdentifier.equalsWithValue(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getEntities().get(0).getAttribution(), spcCheckLot.getProcessChamberID())
                                        && CimStringUtils.equals(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getEntities().get(1).getClassName(),BizConstant.SP_INHIBITCLASSID_MACHINERECIPE)
                                        && ObjectIdentifier.equalsWithValue(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getEntities().get(1).getObjectID(), spcCheckLot.getProcessMachineRecipeID())
                                        && CimStringUtils.equals(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getReasonCode(),BizConstant.SP_REASON_SPCOUTOFRANGEINHIBIT)
                                        && CimStringUtils.equals(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getCorrespondingObjRefPO(),spcCheckLot.getProcessObjrefPO())
                                        && ObjectIdentifier.equalsWithValue(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getLotID(), tempLotID)){
                                    log.info("break!!");
                                    findFlag = true;
                                    break;
                                }
                            }
                        }
                        if (!findFlag){
                            log.info("findFlag == FALSE  [machineID]");
                            // Add ActionResultInfo
                            List<Infos.DCActionResultInfo> dcActionLotResult = dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo();
                            Infos.DCActionResultInfo dcActionResultInfo = new Infos.DCActionResultInfo();
                            dcActionResultInfo.setLotID(tempLotID);
                            dcActionResultInfo.setMonitorLotFlag(false);
                            dcActionResultInfo.setDcDefID(spcCheckLot.getDcDefID());
                            dcActionResultInfo.setDcSpecID(spcCheckLot.getDcSpecID());
                            dcActionResultInfo.setCheckType(BizConstant.SP_ACTIONRESULT_CHECKTYPE_SPC);
                            dcActionResultInfo.setReasonCode(BizConstant.SP_REASON_SPCOUTOFRANGEINHIBIT);
                            dcActionResultInfo.setActionCode(BizConstant.SP_ACTIONCODE_CHAMBERANDRECIPEHOLD);
                            dcActionResultInfo.setCorrespondingObjRefPO(spcCheckLot.getProcessObjrefPO());
                            dcActionResultInfo.setBankID("");
                            dcActionResultInfo.setReworkRouteID("");
                            List<Infos.EntityIdentifier> entityIdentifiers = new ArrayList<>();
                            Infos.EntityIdentifier entityIdentifier1 = new Infos.EntityIdentifier();
                            entityIdentifier1.setClassName(BizConstant.SP_INHIBITCLASSID_CHAMBER);
                            entityIdentifier1.setAttribution(spcCheckLot.getProcessChamberID().getValue());
                            entityIdentifier1.setObjectID(spcCheckLot.getProcessEquipmentID());
                            entityIdentifiers.add(entityIdentifier1);
                            Infos.EntityIdentifier entityIdentifier2 = new Infos.EntityIdentifier();
                            entityIdentifier2.setClassName(BizConstant.SP_INHIBITCLASSID_MACHINERECIPE);
                            entityIdentifier2.setAttribution("");
                            entityIdentifier2.setObjectID(spcCheckLot.getProcessMachineRecipeID());
                            entityIdentifiers.add(entityIdentifier2);
                            dcActionResultInfo.setEntities(entityIdentifiers);
                            dcActionLotResult.add(dcActionResultInfo);
                            dcActionInfo_count++;
                        }
                    } else {
                        log.info("correspondingPO is other Fab");
                        //-------------------------------------------------------------------
                        //  Check process_and_equipment_hold_inhibition is already registed or not.
                        //  If only not existed, entry new inhibition.
                        //-------------------------------------------------------------------
                        findFlag = false;
                        List<Infos.EntityInhibitAttributesWithFabInfo> entityInhibitAttributesWithFabInfos = strStartLot_actionList_EffectSPCCheck_out.getEntityInhibitionsWithFabInfo();
                        for (Infos.EntityInhibitAttributesWithFabInfo entityInhibitAttributesWithFabInfo : entityInhibitAttributesWithFabInfos) {
                            if (CimArrayUtils.getSize(entityInhibitAttributesWithFabInfo.getEntityInhibitAttributes().getEntities()) == 2){
                                if (CimStringUtils.equals(entityInhibitAttributesWithFabInfo.getEntityInhibitAttributes().getEntities().get(0).getClassName(), BizConstant.SP_INHIBITCLASSID_CHAMBER)
                                        && ObjectIdentifier.equalsWithValue(entityInhibitAttributesWithFabInfo.getEntityInhibitAttributes().getEntities().get(0).getObjectID(), spcCheckLot.getProcessEquipmentID())
                                        && ObjectIdentifier.equalsWithValue(entityInhibitAttributesWithFabInfo.getEntityInhibitAttributes().getEntities().get(0).getAttribution(), spcCheckLot.getProcessChamberID())
                                        && CimStringUtils.equals(entityInhibitAttributesWithFabInfo.getEntityInhibitAttributes().getEntities().get(1).getClassName(), BizConstant.SP_INHIBITCLASSID_MACHINERECIPE)
                                        && ObjectIdentifier.equals(entityInhibitAttributesWithFabInfo.getEntityInhibitAttributes().getEntities().get(1).getObjectID(), spcCheckLot.getProcessMachineRecipeID())) {
                                    findFlag = true;
                                    break;
                                }
                            }
                        }
                        if (!findFlag){
                            log.info("findFlag == FALSE");
                            entity_countForOtherFab++;
                            Infos.EntityInhibitAttributesWithFabInfo entityInhibitAttributesWithFabInfo = new Infos.EntityInhibitAttributesWithFabInfo();
                            entityInhibitAttributesWithFabInfos.add(entityInhibitAttributesWithFabInfo);
                            Infos.EntityInhibitAttributes entityInhibitAttributes = new Infos.EntityInhibitAttributes();
                            entityInhibitAttributesWithFabInfo.setEntityInhibitAttributes(entityInhibitAttributes);
                            List<Infos.EntityIdentifier> entities = new ArrayList<>();
                            entityInhibitAttributes.setEntities(entities);
                            Infos.EntityIdentifier entityIdentifier1 = new Infos.EntityIdentifier();
                            Infos.EntityIdentifier entityIdentifier2 = new Infos.EntityIdentifier();
                            entities.add(entityIdentifier1);
                            entities.add(entityIdentifier2);
                            entityIdentifier1.setClassName(BizConstant.SP_INHIBITCLASSID_CHAMBER);
                            entityIdentifier1.setAttribution(spcCheckLot.getProcessChamberID().getValue());
                            entityIdentifier1.setObjectID(spcCheckLot.getProcessEquipmentID());
                            entityIdentifier2.setClassName(BizConstant.SP_INHIBITCLASSID_MACHINERECIPE);
                            entityIdentifier2.setAttribution("");
                            entityIdentifier2.setObjectID(spcCheckLot.getProcessMachineRecipeID());
                            entityInhibitAttributes.setStartTimeStamp(CimDateUtils.getTimestampAsString(objCommon.getTimeStamp().getReportTimeStamp()));
                            entityInhibitAttributes.setEndTimeStamp(BizConstant.SP_TIMESTAMP_NIL_OBJECT_STRING);
                            entityInhibitAttributes.setReasonCode(BizConstant.SP_REASON_SPCOUTOFRANGEINHIBIT);
                            entityInhibitAttributes.setOwnerID(objCommon.getUser().getUserID());
                            entityInhibitAttributes.setClaimedTimeStamp(CimDateUtils.getTimestampAsString(objCommon.getTimeStamp().getReportTimeStamp()));
                            /**
                             * Task-311 add ocapNo for constraint memo
                             */
                            entityInhibitAttributes.setMemo(ocapMemo);
                        }
                        findFlag = false;
                        for (int n = 0 ; n < dcActionInfo_count ; n++ ) {
                            if (CimArrayUtils.getSize(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getEntities()) == 2 ){
                                if (CimStringUtils.equals(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getEntities().get(0).getClassName(),BizConstant.SP_INHIBITCLASSID_CHAMBER)
                                        && CimObjectUtils.equals(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getEntities().get(0).getObjectID(),spcCheckLot.getProcessEquipmentID())
                                        && ObjectIdentifier.equalsWithValue(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getEntities().get(0).getAttribution(), spcCheckLot.getProcessChamberID())
                                        && CimStringUtils.equals(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getEntities().get(1).getClassName(),BizConstant.SP_INHIBITCLASSID_MACHINERECIPE)
                                        && ObjectIdentifier.equalsWithValue(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getEntities().get(1).getObjectID(), spcCheckLot.getProcessMachineRecipeID())
                                        && CimStringUtils.equals(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getReasonCode(),BizConstant.SP_REASON_SPCOUTOFRANGEINHIBIT)
                                        && CimStringUtils.equals(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getCorrespondingObjRefPO(),spcCheckLot.getProcessObjrefPO())
                                        && ObjectIdentifier.equalsWithValue(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getLotID(), tempLotID)){
                                    log.info("break!!");
                                    findFlag = true;
                                    break;
                                }
                            }
                        }
                        if (!findFlag){
                            log.info("findFlag == FALSE ");
                            // Add ActionResultInfo
                            List<Infos.DCActionResultInfo> dcActionLotResult = dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo();
                            Infos.DCActionResultInfo dcActionResultInfo = new Infos.DCActionResultInfo();
                            dcActionResultInfo.setLotID(tempLotID);
                            dcActionResultInfo.setMonitorLotFlag(false);
                            dcActionResultInfo.setDcDefID(spcCheckLot.getDcDefID());
                            dcActionResultInfo.setDcSpecID(spcCheckLot.getDcSpecID());
                            dcActionResultInfo.setCheckType(BizConstant.SP_ACTIONRESULT_CHECKTYPE_SPC);
                            dcActionResultInfo.setReasonCode(BizConstant.SP_REASON_SPCOUTOFRANGEINHIBIT);
                            dcActionResultInfo.setActionCode(BizConstant.SP_ACTIONCODE_CHAMBERANDRECIPEHOLD);
                            dcActionResultInfo.setCorrespondingObjRefPO(spcCheckLot.getProcessObjrefPO());
                            dcActionResultInfo.setBankID("");
                            dcActionResultInfo.setReworkRouteID("");
                            List<Infos.EntityIdentifier> entityIdentifiers = new ArrayList<>();
                            Infos.EntityIdentifier entityIdentifier1 = new Infos.EntityIdentifier();
                            entityIdentifier1.setClassName(BizConstant.SP_INHIBITCLASSID_CHAMBER);
                            entityIdentifier1.setAttribution(spcCheckLot.getProcessChamberID().getValue());
                            entityIdentifier1.setObjectID(spcCheckLot.getProcessEquipmentID());
                            entityIdentifiers.add(entityIdentifier1);
                            Infos.EntityIdentifier entityIdentifier2 = new Infos.EntityIdentifier();
                            entityIdentifier2.setClassName(BizConstant.SP_INHIBITCLASSID_MACHINERECIPE);
                            entityIdentifier2.setAttribution("");
                            entityIdentifier2.setObjectID(spcCheckLot.getProcessMachineRecipeID());
                            entityIdentifiers.add(entityIdentifier2);
                            dcActionResultInfo.setEntities(entityIdentifiers);
                            dcActionLotResult.add(dcActionResultInfo);
                            dcActionInfo_count++;
                        }
                    }
                }
                // Set Entity Inhibit Reason Detail Information
                // For Representive Lot
                for(eCnt = entity_count_keep ; eCnt < entity_count ; eCnt++ ){
                    Infos.EntityInhibitDetailAttributes entityInhibitDetailAttributes = strStartLot_actionList_EffectSPCCheck_out.getStrEntityInhibitions().get(eCnt);
                    List<Infos.EntityInhibitReasonDetailInfo> entityInhibitReasonDetailInfos = entityInhibitDetailAttributes.getEntityInhibitReasonDetailInfos();
                    if (CimArrayUtils.isEmpty(entityInhibitReasonDetailInfos)){
                        entityInhibitReasonDetailInfos = new ArrayList<>();
                        entityInhibitDetailAttributes.setEntityInhibitReasonDetailInfos(entityInhibitReasonDetailInfos);
                    }
                    this.setEntityInhibitReasonDetailinfoFromSpcResultAndDctype(entityInhibitReasonDetailInfos, spcCheckLot.getSpcResult(), spcIFParmList.get(0).getSpcInput().getEdcType());
                }

                // For Other Fab
                for(eCnt = entity_countForOtherFab_keep; eCnt < entity_countForOtherFab; eCnt++ ){
                    Infos.EntityInhibitAttributesWithFabInfo entityInhibitAttributesWithFabInfo = strStartLot_actionList_EffectSPCCheck_out.getEntityInhibitionsWithFabInfo().get(eCnt);
                    List<Infos.EntityInhibitReasonDetailInfo> entityInhibitReasonDetailInfos = entityInhibitAttributesWithFabInfo.getEntityInhibitReasonDetailInfos();
                    if (CimArrayUtils.isEmpty(entityInhibitReasonDetailInfos)){
                        entityInhibitReasonDetailInfos = new ArrayList<>();
                        entityInhibitAttributesWithFabInfo.setEntityInhibitReasonDetailInfos(entityInhibitReasonDetailInfos);
                    }
                    this.setEntityInhibitReasonDetailinfoFromSpcResultAndDctype(entityInhibitReasonDetailInfos, spcCheckLot.getSpcResult(), spcIFParmList.get(0).getSpcInput().getEdcType());
                    if (!CimArrayUtils.isEmpty(entityInhibitReasonDetailInfos)){
                        entityInhibitReasonDetailInfos.get(0).setRelatedFabID(currentFabID);
                    }
                }

                //------------------------------------------------------------------------------------------------
                //  Make strMessageAttributes of out parameter.
                //
                //    ( actionCode are filled only when spcCheckResult is SP_ActionCode_Mail. )
                //------------------------------------------------------------------------------------------------

                if (mail_flag){
                    log.info( "mail_flag == TRUE");
                    if (CimStringUtils.equals(spcCheckLot.getSpcCheckResult(),BizConstant.SP_SPCCHECK_HOLDLIMITOFF)
                            || CimStringUtils.equals(spcCheckLot.getSpcCheckResult(),BizConstant.SP_SPCCHECK_WARNINGLIMITOFF)){
                        log.info("strSpcCheckLot[i].spcCheckResult == SP_SPCCheck_HoldLimitOff or SP_SPCCheck_WarningLimitOff");
                        String tmpMsg = "=============================================\n"+
                                "===       SPC Violation Information       ===\n"+
                                "=============================================\n"+
                                "LotID                  : "+ObjectIdentifier.fetchValue(spcCheckLot.getLotID())+" \n"+
                                "ProductID              : "+ObjectIdentifier.fetchValue(spcCheckLot.getProductID())+" \n"+
                                "ProcessRouteID         : "+ObjectIdentifier.fetchValue(spcCheckLot.getProcessRouteID())+" \n"+
                                "ProcessOperationNumber : "+spcCheckLot.getProcessOperationNumber()+" \n"+
                                "ProcessEquipmentID     : "+ObjectIdentifier.fetchValue(spcCheckLot.getProcessEquipmentID())+" \n"+
                                "ProcessChamberID       : "+ObjectIdentifier.fetchValue(spcCheckLot.getProcessChamberID())+" \n"+
                                "ProcessMachineRecipeID : "+ObjectIdentifier.fetchValue(spcCheckLot.getProcessMachineRecipeID());

                        findFlag = false;
                        //  Check if strMessageAttributes is already registed with the same lotID & reasonCode or not.
                        //  If only not existed, entry new inhibition.
                        List<Infos.MessageAttributes> messageList = strStartLot_actionList_EffectSPCCheck_out.getStrMessageList();
                        for (Infos.MessageAttributes messageAttributes : messageList) {
                            if (CimStringUtils.equals(messageAttributes.getReasonCode(), spcCheckLot.getSpcCheckResult())
                                    && ObjectIdentifier.equalsWithValue(messageAttributes.getLotID(), spcCheckLot.getLotID())
                                    && ObjectIdentifier.equalsWithValue(messageAttributes.getRouteID(), spcCheckLot.getProcessRouteID())
                                    && CimStringUtils.equals(messageAttributes.getOperationNumber(), spcCheckLot.getProcessOperationNumber())) {
                                log.info("set findFlag = TRUE");
                                findFlag = true;
                            }
                        }
                        if (!findFlag){
                            log.info("findFlag == FALSE");
                            msg_count++;
                            Infos.MessageAttributes strMessage = new Infos.MessageAttributes();
                            messageList.add(strMessage);
                            strMessage.setMessageID(ObjectIdentifier.buildWithValue(messageID));
                            strMessage.setLotID(tempLotID);
                            strMessage.setLotStatus("");
                            strMessage.setRouteID(spcCheckLot.getProcessRouteID());
                            strMessage.setOperationNumber(spcCheckLot.getProcessOperationNumber());
                            strMessage.setReasonCode(spcCheckLot.getSpcCheckResult());
                            strMessage.setMessageText(tmpMsg);

                            // Add ActionResultInfo
                            List<Infos.DCActionResultInfo> dcActionLotResult = dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo();
                            Infos.DCActionResultInfo  dcActionResultInfo = new Infos.DCActionResultInfo();
                            dcActionResultInfo.setLotID(tempLotID);
                            dcActionResultInfo.setMonitorLotFlag(false);
                            dcActionResultInfo.setDcDefID(spcCheckLot.getDcDefID());
                            dcActionResultInfo.setDcSpecID(spcCheckLot.getDcSpecID());
                            dcActionResultInfo.setCheckType(BizConstant.SP_ACTIONRESULT_CHECKTYPE_SPC);
                            dcActionResultInfo.setReasonCode(BizConstant.SP_REASON_SPCOUTOFRANGEINHIBIT);
                            dcActionResultInfo.setActionCode(BizConstant.SP_ACTIONCODE_MAILSEND);
                            dcActionResultInfo.setCorrespondingObjRefPO(spcCheckLot.getProcessObjrefPO());
                            dcActionResultInfo.setBankID("");
                            dcActionResultInfo.setReworkRouteID("");
                            dcActionLotResult.add(dcActionResultInfo);
                            dcActionInfo_count++;

                        }
                    }
                }

                //  Make strBankMoveList of out parameter. (DCR4200115)
                //
                //  ( actionCode are filled only when spcCheckResult is SP_ActionCode_BankMove. )
                if (bank_move_flag){
                    log.info("bankMoveFlag == TRUE");
                    List<Infos.BankMove> bankMoveList = strStartLot_actionList_EffectSPCCheck_out.getBankMoveList();
                    if (CimArrayUtils.isEmpty(bankMoveList)){
                        bankMoveList = new ArrayList<>();
                        strStartLot_actionList_EffectSPCCheck_out.setBankMoveList(bankMoveList);
                    }
                    Infos.BankMove bankMove = new Infos.BankMove();
                    bankMoveList.add(bankMove);
                    bankMove.setLotID(tempLotID);
                    bankMove.setBankID(ObjectIdentifier.buildWithValue(spcCheckLot.getSpcResult().getBankID()));
                    // Add ActionResultInfo
                    List<Infos.DCActionResultInfo> dcActionLotResult = dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo();
                    Infos.DCActionResultInfo  dcActionResultInfo = new Infos.DCActionResultInfo();
                    dcActionResultInfo.setLotID(tempLotID);
                    dcActionResultInfo.setMonitorLotFlag(false);
                    dcActionResultInfo.setDcDefID(spcCheckLot.getDcDefID());
                    dcActionResultInfo.setDcSpecID(spcCheckLot.getDcSpecID());
                    dcActionResultInfo.setCheckType(BizConstant.SP_ACTIONRESULT_CHECKTYPE_SPC);
                    dcActionResultInfo.setReasonCode(BizConstant.SP_REASON_SPCOUTOFRANGEINHIBIT);
                    dcActionResultInfo.setActionCode(BizConstant.SP_ACTIONCODE_BANKMOVE);
                    dcActionResultInfo.setCorrespondingObjRefPO(spcCheckLot.getProcessObjrefPO());
                    dcActionResultInfo.setBankID(spcCheckLot.getSpcResult().getBankID());
                    dcActionResultInfo.setReworkRouteID("");
                    dcActionLotResult.add(dcActionResultInfo);
                    dcActionInfo_count++;
                }

                //  Make strReworkBranchList of out parameter. (DCR4200115)
                //
                //    ( actionCode are filled only when spcCheckResult is SP_ActionCode_ReworkBranch. )

                if (rework_branch_flag){
                    log.info("reworkBranchFlag == TRUE");
                    List<Infos.ConnectedRouteList> strProcess_connectedRouteList_out = processMethod.processConnectedRouteList(objCommon, BizConstant.SP_MAINPDTYPE_REWORK, tempLotID);
                    int rework_route_found = 0;
                    if (!CimArrayUtils.isEmpty(strProcess_connectedRouteList_out)){
                        for (Infos.ConnectedRouteList connectedRouteList : strProcess_connectedRouteList_out){
                            if (ObjectIdentifier.equalsWithValue(spcCheckLot.getSpcResult().getReworkRouteID(), connectedRouteList.getRouteID())){
                                log.info("processRouteID == routeID");
                                List<Infos.ReworkBranch> reworkBranchList = strStartLot_actionList_EffectSPCCheck_out.getReworkBranchList();
                                if (CimArrayUtils.isEmpty(reworkBranchList)){
                                    reworkBranchList = new ArrayList<>();
                                    strStartLot_actionList_EffectSPCCheck_out.setReworkBranchList(reworkBranchList);
                                }
                                Infos.ReworkBranch reworkBranch = new Infos.ReworkBranch();
                                reworkBranchList.add(reworkBranch);
                                reworkBranch.setLotID(tempLotID);
                                reworkBranch.setReworkRouteID(ObjectIdentifier.buildWithValue(spcCheckLot.getSpcResult().getReworkRouteID()));
                                reworkBranch.setReturnOperationNumber(connectedRouteList.getReturnOperationNumber());
                                rework_route_found = 1;

                                // Add ActionResultInfo
                                List<Infos.DCActionResultInfo> dcActionLotResult = dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo();
                                Infos.DCActionResultInfo  dcActionResultInfo = new Infos.DCActionResultInfo();
                                dcActionResultInfo.setLotID(tempLotID);
                                dcActionResultInfo.setMonitorLotFlag(false);
                                dcActionResultInfo.setDcDefID(spcCheckLot.getDcDefID());
                                dcActionResultInfo.setDcSpecID(spcCheckLot.getDcSpecID());
                                dcActionResultInfo.setCheckType(BizConstant.SP_ACTIONRESULT_CHECKTYPE_SPC);
                                dcActionResultInfo.setReasonCode(BizConstant.SP_REASON_SPCOUTOFRANGEINHIBIT);
                                dcActionResultInfo.setActionCode(BizConstant.SP_ACTIONCODE_REWORKBRANCH);
                                dcActionResultInfo.setCorrespondingObjRefPO(spcCheckLot.getProcessObjrefPO());
                                dcActionResultInfo.setBankID("");
                                dcActionResultInfo.setReworkRouteID(spcCheckLot.getSpcResult().getReworkRouteID());
                                dcActionLotResult.add(dcActionResultInfo);
                                dcActionInfo_count++;
                            }
                        }
                    }
                    Validations.check(rework_route_found == 0, new OmCode(retCodeConfig.getNotFoundRoute(), spcCheckLot.getSpcResult().getReworkRouteID()));
                }

                //  Make strMailSendList of out parameter. (DCR4200115)
                //
                //    ( actionCode are filled only when spcCheckResult is SP_ActionCode_MailSend. )

                /*------------------------*/
                /*   For Monitored Lots   */
                /*------------------------*/
                entity_count_keep = entity_count;
                entity_countForOtherFab_keep = entity_countForOtherFab;

                for (Infos.MonitoredLot effectLot : effectedLots){
                    if (CimArrayUtils.getSize(effectedLots) == 1){
                        //only effectLots size > 1 can join in monitored lot logical
                        continue;
                    }
                    if (!requestOtherFabFlag){
                        log.info("correspondingPO is current Fab");
                        if (requestOtherFabFlagForMonitor){
                            log.info("Monitored Lots are in another Fab");
                            break;
                        }
                        CimLot aLot = baseCoreFactory.getBO(CimLot.class, effectedLot.getLotID());
                        Validations.check(aLot == null, new OmCode(retCodeConfig.getNotFoundLot(), ObjectIdentifier.fetchValue(effectedLot.getLotID())));
                        CimProcessOperation aPOtmp = baseCoreFactory.getBO(CimProcessOperation.class,effectLot.getProcessOperation());
                        Validations.check(aPOtmp == null, new OmCode(retCodeConfig.getNotFoundProcessOperation(), effectedLot.getProcessOperation(), effectedLot.getLotID().getValue()));
                        CimMachine aMachine = aPOtmp.getAssignedMachine();
                        CimMachineRecipe aRecipe = aPOtmp.getAssignedMachineRecipe();
                        CimProcessDefinition aMainPD = aPOtmp.getMainProcessDefinition();
                        String operationNo = aPOtmp.getOperationNumber();
                        if (equipment_hold_flag){
                            log.info("equipment_hold_flag == TRUE");
                            if (aMachine != null){
                                String machineID = aMachine.getIdentifier();
                                findFlag = false;
                                //  Check equipment_hold_inhibition is already registed or not.
                                //  If only not existed, entry new inhibition.
                                List<Infos.EntityInhibitDetailAttributes> entityInhibitions = strStartLot_actionList_EffectSPCCheck_out.getStrEntityInhibitions();
                                for (Infos.EntityInhibitDetailAttributes entityInhibition : entityInhibitions) {
                                    if (CimStringUtils.equals(entityInhibition.getEntities().get(0).getClassName(), BizConstant.SP_INHIBITCLASSID_EQUIPMENT)
                                            && ObjectIdentifier.equalsWithValue(entityInhibition.getEntities().get(0).getObjectID(), machineID)) {
                                        findFlag = true;
                                        break;
                                    }
                                }
                                if (!findFlag){
                                    log.info("findFlag == FALSE");
                                    entity_count++;
                                    Infos.EntityInhibitDetailAttributes entityInhibitDetailAttributes = new Infos.EntityInhibitDetailAttributes();
                                    entityInhibitions.add(entityInhibitDetailAttributes);
                                    entityInhibitDetailAttributes.setStartTimeStamp(CimDateUtils.getTimestampAsString(objCommon.getTimeStamp().getReportTimeStamp()));
                                    entityInhibitDetailAttributes.setEndTimeStamp(BizConstant.SP_TIMESTAMP_NIL_OBJECT_STRING);
                                    entityInhibitDetailAttributes.setReasonCode(BizConstant.SP_REASON_SPCOUTOFRANGEINHIBIT);
                                    entityInhibitDetailAttributes.setOwnerID(objCommon.getUser().getUserID());
                                    entityInhibitDetailAttributes.setClaimedTimeStamp(CimDateUtils.getTimestampAsString(objCommon.getTimeStamp().getReportTimeStamp()));
                                    List<Infos.EntityIdentifier> entities = new ArrayList<>();
                                    entityInhibitDetailAttributes.setEntities(entities);
                                    Infos.EntityIdentifier entityIdentifier = new Infos.EntityIdentifier();
                                    entities.add(entityIdentifier);
                                    entityIdentifier.setClassName(BizConstant.SP_INHIBITCLASSID_EQUIPMENT);
                                    entityIdentifier.setObjectID(ObjectIdentifier.build(aMachine.getIdentifier(), aMachine.getPrimaryKey()));
                                    entityIdentifier.setAttribution("");
                                    /**
                                     * Task-311 add ocapNo for constraint memo
                                     */
                                    entityInhibitDetailAttributes.setMemo(ocapMemo);
                                }
                                findFlag = false;
                                for (int n = 0; n < dcActionInfo_count; n++){
                                    if (CimArrayUtils.getSize(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getEntities()) == 1){
                                        if (CimStringUtils.equals(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getEntities().get(0).getClassName(),BizConstant.SP_INHIBITCLASSID_EQUIPMENT)
                                                && ObjectIdentifier.equalsWithValue(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getEntities().get(0).getObjectID(), machineID)
                                                && CimStringUtils.equals(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getReasonCode(),BizConstant.SP_REASON_SPCOUTOFRANGEINHIBIT)
                                                && CimStringUtils.equals(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getCorrespondingObjRefPO(),effectLot.getProcessOperation())
                                                && ObjectIdentifier.equalsWithValue(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getLotID(), effectLot.getLotID())){
                                            findFlag = true;
                                            break;
                                        }
                                    }
                                }
                                if(!findFlag){
                                    log.info("findFlag == FALSE ");
                                    // Add ActionResultInfo
                                    List<Infos.DCActionResultInfo> dcActionLotResult = dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo();
                                    Infos.DCActionResultInfo dcActionResultInfo = new Infos.DCActionResultInfo();
                                    dcActionResultInfo.setLotID(effectLot.getLotID());
                                    dcActionResultInfo.setMonitorLotFlag(true);
                                    dcActionResultInfo.setDcDefID(spcCheckLot.getDcDefID());
                                    dcActionResultInfo.setDcSpecID(spcCheckLot.getDcSpecID());
                                    dcActionResultInfo.setCheckType(BizConstant.SP_ACTIONRESULT_CHECKTYPE_SPC);
                                    dcActionResultInfo.setReasonCode(BizConstant.SP_REASON_SPCOUTOFRANGEINHIBIT);
                                    dcActionResultInfo.setActionCode(BizConstant.SP_ACTIONCODE_EQUIPMENTHOLD);
                                    dcActionResultInfo.setCorrespondingObjRefPO(effectLot.getProcessOperation());
                                    dcActionResultInfo.setBankID("");
                                    dcActionResultInfo.setReworkRouteID("");
                                    dcActionLotResult.add(dcActionResultInfo);
                                    List<Infos.EntityIdentifier> entityIdentifiers = new ArrayList<>();
                                    dcActionResultInfo.setEntities(entityIdentifiers);
                                    Infos.EntityIdentifier entityIdentifier1 = new Infos.EntityIdentifier();
                                    entityIdentifier1.setClassName(BizConstant.SP_INHIBITCLASSID_EQUIPMENT);
                                    entityIdentifier1.setAttribution("");
                                    entityIdentifier1.setObjectID(ObjectIdentifier.build(aMachine.getIdentifier(), aMachine.getPrimaryKey()));
                                    entityIdentifiers.add(entityIdentifier1);
                                    dcActionInfo_count++;
                                }
                            }
                        }

                        if (recipe_hold_flag){
                            log.info("recipe_hold_flag == TRUE");
                            if (aRecipe != null){
                                log.info("aRecipe is not nil");
                                String recipeID = aRecipe.getIdentifier();
                                findFlag = false;
                                //  Check recipe_hold_inhibition is already registed or not.
                                //  If only not existed, entry new inhibition.
                                List<Infos.EntityInhibitDetailAttributes> entityInhibitions = strStartLot_actionList_EffectSPCCheck_out.getStrEntityInhibitions();
                                for (Infos.EntityInhibitDetailAttributes entityInhibition : entityInhibitions) {
                                    if (CimStringUtils.equals(entityInhibition.getEntities().get(0).getClassName(), BizConstant.SP_INHIBITCLASSID_MACHINERECIPE)
                                            && ObjectIdentifier.equalsWithValue(entityInhibition.getEntities().get(0).getObjectID(), recipeID)) {
                                        findFlag = true;
                                        break;
                                    }
                                }
                                if (!findFlag){
                                    log.info("findFlag == FALSE");
                                    entity_count++;
                                    Infos.EntityInhibitDetailAttributes entityInhibitDetailAttributes = new Infos.EntityInhibitDetailAttributes();
                                    entityInhibitions.add(entityInhibitDetailAttributes);
                                    entityInhibitDetailAttributes.setStartTimeStamp(CimDateUtils.getTimestampAsString(objCommon.getTimeStamp().getReportTimeStamp()));
                                    entityInhibitDetailAttributes.setEndTimeStamp(BizConstant.SP_TIMESTAMP_NIL_OBJECT_STRING);
                                    entityInhibitDetailAttributes.setReasonCode(BizConstant.SP_REASON_SPCOUTOFRANGEINHIBIT);
                                    entityInhibitDetailAttributes.setOwnerID(objCommon.getUser().getUserID());
                                    entityInhibitDetailAttributes.setClaimedTimeStamp(CimDateUtils.getTimestampAsString(objCommon.getTimeStamp().getReportTimeStamp()));
                                    List<Infos.EntityIdentifier> entities = new ArrayList<>();
                                    entityInhibitDetailAttributes.setEntities(entities);
                                    Infos.EntityIdentifier entityIdentifier = new Infos.EntityIdentifier();
                                    entities.add(entityIdentifier);
                                    entityIdentifier.setClassName(BizConstant.SP_INHIBITCLASSID_MACHINERECIPE);
                                    entityIdentifier.setObjectID(ObjectIdentifier.build(aRecipe.getIdentifier(), aRecipe.getPrimaryKey()));
                                    entityIdentifier.setAttribution("");
                                    /**
                                     * Task-311 add ocapNo for constraint memo
                                     */
                                    entityInhibitDetailAttributes.setMemo(ocapMemo);
                                }
                                findFlag = false;
                                for (int n = 0; n < dcActionInfo_count; n++){
                                    if (CimArrayUtils.getSize(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getEntities()) == 1){
                                        if (CimStringUtils.equals(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getEntities().get(0).getClassName(),BizConstant.SP_INHIBITCLASSID_MACHINERECIPE)
                                                && ObjectIdentifier.equalsWithValue(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getEntities().get(0).getObjectID(), recipeID)
                                                && CimStringUtils.equals(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getReasonCode(),BizConstant.SP_REASON_SPCOUTOFRANGEINHIBIT)
                                                && CimStringUtils.equals(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getCorrespondingObjRefPO(),effectLot.getProcessOperation())
                                                && ObjectIdentifier.equalsWithValue(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getLotID(), effectLot.getLotID())){
                                            findFlag = true;
                                            break;
                                        }
                                    }
                                }
                                if(!findFlag){
                                    log.info("findFlag == FALSE ");
                                    // Add ActionResultInfo
                                    List<Infos.DCActionResultInfo> dcActionLotResult = dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo();
                                    Infos.DCActionResultInfo  dcActionResultInfo = new Infos.DCActionResultInfo();
                                    dcActionResultInfo.setLotID(effectLot.getLotID());
                                    dcActionResultInfo.setMonitorLotFlag(true);
                                    dcActionResultInfo.setDcDefID(spcCheckLot.getDcDefID());
                                    dcActionResultInfo.setDcSpecID(spcCheckLot.getDcSpecID());
                                    dcActionResultInfo.setCheckType(BizConstant.SP_ACTIONRESULT_CHECKTYPE_SPC);
                                    dcActionResultInfo.setReasonCode(BizConstant.SP_REASON_SPCOUTOFRANGEINHIBIT);
                                    dcActionResultInfo.setActionCode(BizConstant.SP_ACTIONCODE_RECIPEHOLD);
                                    dcActionResultInfo.setCorrespondingObjRefPO(effectLot.getProcessOperation());
                                    dcActionResultInfo.setBankID("");
                                    dcActionResultInfo.setReworkRouteID("");
                                    dcActionLotResult.add(dcActionResultInfo);
                                    List<Infos.EntityIdentifier> entityIdentifiers = new ArrayList<>();
                                    dcActionResultInfo.setEntities(entityIdentifiers);
                                    Infos.EntityIdentifier entityIdentifier1 = new Infos.EntityIdentifier();
                                    entityIdentifier1.setClassName(BizConstant.SP_INHIBITCLASSID_MACHINERECIPE);
                                    entityIdentifier1.setAttribution("");
                                    entityIdentifier1.setObjectID(ObjectIdentifier.build(aRecipe.getIdentifier(), aRecipe.getPrimaryKey()));
                                    entityIdentifiers.add(entityIdentifier1);
                                    dcActionInfo_count++;
                                }
                            }
                        }
                        if (route_hold_flag){
                            log.info("route_hold_flag == TRUE");
                            if (aMainPD != null){
                                log.info("aMainPD is not nil");
                                String mainPDID = aMainPD.getIdentifier();
                                findFlag = false;
                                //  Check recipe_hold_inhibition is already registed or not.
                                //  If only not existed, entry new inhibition.
                                List<Infos.EntityInhibitDetailAttributes> entityInhibitions = strStartLot_actionList_EffectSPCCheck_out.getStrEntityInhibitions();
                                for (Infos.EntityInhibitDetailAttributes entityInhibition : entityInhibitions) {
                                    if (CimStringUtils.equals(entityInhibition.getEntities().get(0).getClassName(), BizConstant.SP_INHIBITCLASSID_ROUTE)
                                            && ObjectIdentifier.equalsWithValue(entityInhibition.getEntities().get(0).getObjectID(), mainPDID)
                                            && CimStringUtils.equals(entityInhibition.getEntities().get(0).getAttribution(), "")) {
                                        findFlag = true;
                                        break;
                                    }
                                }
                                if (!findFlag){
                                    log.info("findFlag == FALSE");
                                    entity_count++;
                                    Infos.EntityInhibitDetailAttributes entityInhibitDetailAttributes = new Infos.EntityInhibitDetailAttributes();
                                    entityInhibitions.add(entityInhibitDetailAttributes);
                                    entityInhibitDetailAttributes.setStartTimeStamp(CimDateUtils.getTimestampAsString(objCommon.getTimeStamp().getReportTimeStamp()));
                                    entityInhibitDetailAttributes.setEndTimeStamp(BizConstant.SP_TIMESTAMP_NIL_OBJECT_STRING);
                                    entityInhibitDetailAttributes.setReasonCode(BizConstant.SP_REASON_SPCOUTOFRANGEINHIBIT);
                                    entityInhibitDetailAttributes.setOwnerID(objCommon.getUser().getUserID());
                                    entityInhibitDetailAttributes.setClaimedTimeStamp(CimDateUtils.getTimestampAsString(objCommon.getTimeStamp().getReportTimeStamp()));
                                    List<Infos.EntityIdentifier> entities = new ArrayList<>();
                                    entityInhibitDetailAttributes.setEntities(entities);
                                    Infos.EntityIdentifier entityIdentifier = new Infos.EntityIdentifier();
                                    entities.add(entityIdentifier);
                                    entityIdentifier.setClassName(BizConstant.SP_INHIBITCLASSID_ROUTE);
                                    entityIdentifier.setObjectID(ObjectIdentifier.build(aMainPD.getIdentifier(), aMachine.getPrimaryKey()));
                                    entityIdentifier.setAttribution("");
                                    /**
                                     * Task-311 add ocapNo for constraint memo
                                     */
                                    entityInhibitDetailAttributes.setMemo(ocapMemo);
                                }
                                findFlag = false;
                                for (int n = 0; n < dcActionInfo_count; n++){
                                    if (CimArrayUtils.getSize(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getEntities()) == 1 ){
                                        if (CimStringUtils.equals(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getEntities().get(0).getClassName(),BizConstant.SP_INHIBITCLASSID_ROUTE)
                                                && ObjectIdentifier.equalsWithValue(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getEntities().get(0).getObjectID(), mainPDID)
                                                && CimStringUtils.equals(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getEntities().get(0).getAttribution(), "")
                                                && CimStringUtils.equals(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getReasonCode(),BizConstant.SP_REASON_SPCOUTOFRANGEINHIBIT)
                                                && CimStringUtils.equals(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getCorrespondingObjRefPO(),effectLot.getProcessOperation())
                                                && ObjectIdentifier.equalsWithValue(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getLotID(), effectLot.getLotID())){
                                            findFlag = true;
                                            break;
                                        }
                                    }
                                }
                                if(!findFlag){
                                    log.info("findFlag == FALSE ");
                                    // Add ActionResultInfo
                                    List<Infos.DCActionResultInfo> dcActionLotResult = dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo();
                                    Infos.DCActionResultInfo  dcActionResultInfo = new Infos.DCActionResultInfo();
                                    dcActionResultInfo.setLotID(effectLot.getLotID());
                                    dcActionResultInfo.setMonitorLotFlag(true);
                                    dcActionResultInfo.setDcDefID(spcCheckLot.getDcDefID());
                                    dcActionResultInfo.setDcSpecID(spcCheckLot.getDcSpecID());
                                    dcActionResultInfo.setCheckType(BizConstant.SP_ACTIONRESULT_CHECKTYPE_SPC);
                                    dcActionResultInfo.setReasonCode(BizConstant.SP_REASON_SPCOUTOFRANGEINHIBIT);
                                    dcActionResultInfo.setActionCode(BizConstant.SP_ACTIONCODE_ROUTEHOLD);
                                    dcActionResultInfo.setCorrespondingObjRefPO(effectLot.getProcessOperation());
                                    dcActionResultInfo.setBankID("");
                                    dcActionResultInfo.setReworkRouteID("");
                                    dcActionLotResult.add(dcActionResultInfo);
                                    List<Infos.EntityIdentifier> entityIdentifiers = new ArrayList<>();
                                    dcActionResultInfo.setEntities(entityIdentifiers);
                                    Infos.EntityIdentifier entityIdentifier1 = new Infos.EntityIdentifier();
                                    entityIdentifier1.setClassName(BizConstant.SP_INHIBITCLASSID_ROUTE);
                                    entityIdentifier1.setAttribution("");
                                    entityIdentifier1.setObjectID(ObjectIdentifier.build(aMainPD.getIdentifier(), aMachine.getPrimaryKey()));
                                    entityIdentifiers.add(entityIdentifier1);
                                    dcActionInfo_count++;
                                }
                            }
                        }

                        if (process_hold_flag){
                            log.info("process_hold_flag == TRUE");
                            if (aMainPD != null){
                                log.info("aMainPD is not nil");
                                String mainPDID = aMainPD.getIdentifier();
                                findFlag = false;
                                //  Check recipe_hold_inhibition is already registed or not.
                                //  If only not existed, entry new inhibition.
                                List<Infos.EntityInhibitDetailAttributes> entityInhibitions = strStartLot_actionList_EffectSPCCheck_out.getStrEntityInhibitions();
                                for (Infos.EntityInhibitDetailAttributes entityInhibition : entityInhibitions) {
                                    if (CimStringUtils.equals(entityInhibition.getEntities().get(0).getClassName(), BizConstant.SP_INHIBITCLASSID_OPERATION)
                                            && CimObjectUtils.equals(entityInhibition.getEntities().get(0).getObjectID(), mainPDID)
                                            && CimStringUtils.equals(entityInhibition.getEntities().get(0).getAttribution(), operationNo)) {
                                        findFlag = true;
                                        break;
                                    }
                                }
                                if (!findFlag){
                                    log.info("findFlag == FALSE");
                                    entity_count++;
                                    Infos.EntityInhibitDetailAttributes entityInhibitDetailAttributes = new Infos.EntityInhibitDetailAttributes();
                                    entityInhibitions.add(entityInhibitDetailAttributes);
                                    entityInhibitDetailAttributes.setStartTimeStamp(CimDateUtils.getTimestampAsString(objCommon.getTimeStamp().getReportTimeStamp()));
                                    entityInhibitDetailAttributes.setEndTimeStamp(BizConstant.SP_TIMESTAMP_NIL_OBJECT_STRING);
                                    entityInhibitDetailAttributes.setReasonCode(BizConstant.SP_REASON_SPCOUTOFRANGEINHIBIT);
                                    entityInhibitDetailAttributes.setOwnerID(objCommon.getUser().getUserID());
                                    entityInhibitDetailAttributes.setClaimedTimeStamp(CimDateUtils.getTimestampAsString(objCommon.getTimeStamp().getReportTimeStamp()));
                                    List<Infos.EntityIdentifier> entities = new ArrayList<>();
                                    entityInhibitDetailAttributes.setEntities(entities);
                                    Infos.EntityIdentifier entityIdentifier = new Infos.EntityIdentifier();
                                    entities.add(entityIdentifier);
                                    entityIdentifier.setClassName(BizConstant.SP_INHIBITCLASSID_OPERATION);
                                    entityIdentifier.setObjectID(ObjectIdentifier.build(aMainPD.getIdentifier(), aMainPD.getPrimaryKey()));
                                    entityIdentifier.setAttribution(operationNo);
                                    /**
                                     * Task-311 add ocapNo for constraint memo
                                     */
                                    entityInhibitDetailAttributes.setMemo(ocapMemo);
                                }
                                findFlag = false;
                                for (int n = 0; n < dcActionInfo_count; n++){
                                    if (CimArrayUtils.getSize(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getEntities()) == 1 ){
                                        if (CimStringUtils.equals(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getEntities().get(0).getClassName(),BizConstant.SP_INHIBITCLASSID_OPERATION)
                                                && ObjectIdentifier.equalsWithValue(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getEntities().get(0).getObjectID(), mainPDID)
                                                && CimStringUtils.equals(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getEntities().get(0).getAttribution(), operationNo)
                                                && CimStringUtils.equals(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getReasonCode(),BizConstant.SP_REASON_SPCOUTOFRANGEINHIBIT)
                                                && CimStringUtils.equals(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getCorrespondingObjRefPO(),effectLot.getProcessOperation())
                                                && ObjectIdentifier.equalsWithValue(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getLotID(), effectLot.getLotID())){
                                            findFlag = true;
                                            break;
                                        }
                                    }
                                }
                                if(!findFlag){
                                    log.info("findFlag == FALSE ");
                                    // Add ActionResultInfo
                                    List<Infos.DCActionResultInfo> dcActionLotResult = dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo();
                                    Infos.DCActionResultInfo  dcActionResultInfo = new Infos.DCActionResultInfo();
                                    dcActionResultInfo.setLotID(effectLot.getLotID());
                                    dcActionResultInfo.setMonitorLotFlag(true);
                                    dcActionResultInfo.setDcDefID(spcCheckLot.getDcDefID());
                                    dcActionResultInfo.setDcSpecID(spcCheckLot.getDcSpecID());
                                    dcActionResultInfo.setCheckType(BizConstant.SP_ACTIONRESULT_CHECKTYPE_SPC);
                                    dcActionResultInfo.setReasonCode(BizConstant.SP_REASON_SPCOUTOFRANGEINHIBIT);
                                    dcActionResultInfo.setActionCode(BizConstant.SP_ACTIONCODE_PROCESSHOLD);
                                    dcActionResultInfo.setCorrespondingObjRefPO(effectLot.getProcessOperation());
                                    dcActionResultInfo.setBankID("");
                                    dcActionResultInfo.setReworkRouteID("");
                                    dcActionLotResult.add(dcActionResultInfo);
                                    List<Infos.EntityIdentifier> entityIdentifiers = new ArrayList<>();
                                    dcActionResultInfo.setEntities(entityIdentifiers);
                                    Infos.EntityIdentifier entityIdentifier1 = new Infos.EntityIdentifier();
                                    entityIdentifier1.setClassName(BizConstant.SP_INHIBITCLASSID_OPERATION);
                                    entityIdentifier1.setAttribution(operationNo);
                                    entityIdentifier1.setObjectID(ObjectIdentifier.build(aMainPD.getIdentifier(), aMainPD.getPrimaryKey()));
                                    entityIdentifiers.add(entityIdentifier1);
                                    dcActionInfo_count++;
                                }
                            }
                        }

                        // Inhibit Equipment and Recipe
                        if (equipment_and_recipe_hold_flag){
                            log.info("equipment_and_recipe_hold_flag == TRUE");
                            if (aMainPD != null && aRecipe != null){
                                log.info("aMainPD and aRecipe is not nil");
                                ObjectIdentifier processEquipmentID = ObjectIdentifier.build(aMachine.getIdentifier(), aMachine.getPrimaryKey());
                                ObjectIdentifier processMachineRecipeID = ObjectIdentifier.build(aRecipe.getIdentifier(), aRecipe.getPrimaryKey());
                                findFlag = false;
                                //  Check recipe_hold_inhibition is already registed or not.
                                //  If only not existed, entry new inhibition.
                                List<Infos.EntityInhibitDetailAttributes> entityInhibitions = strStartLot_actionList_EffectSPCCheck_out.getStrEntityInhibitions();
                                for (Infos.EntityInhibitDetailAttributes entityInhibition : entityInhibitions) {
                                    if (CimArrayUtils.getSize(entityInhibition.getEntities()) == 2){
                                        if (CimStringUtils.equals(entityInhibition.getEntities().get(0).getClassName(), BizConstant.SP_INHIBITCLASSID_EQUIPMENT)
                                                && ObjectIdentifier.equalsWithValue(entityInhibition.getEntities().get(0).getObjectID(), processEquipmentID)
                                                && CimStringUtils.equals(entityInhibition.getEntities().get(1).getClassName(), BizConstant.SP_INHIBITCLASSID_MACHINERECIPE)
                                                && ObjectIdentifier.equalsWithValue(entityInhibition.getEntities().get(1).getObjectID(), processMachineRecipeID)) {
                                            findFlag = true;
                                            break;
                                        }
                                    }
                                }
                                if (!findFlag){
                                    log.info("findFlag == FALSE");
                                    entity_count++;
                                    Infos.EntityInhibitDetailAttributes entityInhibitDetailAttributes = new Infos.EntityInhibitDetailAttributes();
                                    entityInhibitions.add(entityInhibitDetailAttributes);
                                    entityInhibitDetailAttributes.setStartTimeStamp(CimDateUtils.getTimestampAsString(objCommon.getTimeStamp().getReportTimeStamp()));
                                    entityInhibitDetailAttributes.setEndTimeStamp(BizConstant.SP_TIMESTAMP_NIL_OBJECT_STRING);
                                    entityInhibitDetailAttributes.setReasonCode(BizConstant.SP_REASON_SPCOUTOFRANGEINHIBIT);
                                    entityInhibitDetailAttributes.setOwnerID(objCommon.getUser().getUserID());
                                    entityInhibitDetailAttributes.setClaimedTimeStamp(CimDateUtils.getTimestampAsString(objCommon.getTimeStamp().getReportTimeStamp()));
                                    List<Infos.EntityIdentifier> entities = new ArrayList<>();
                                    entityInhibitDetailAttributes.setEntities(entities);
                                    Infos.EntityIdentifier entityIdentifier1 = new Infos.EntityIdentifier();
                                    entities.add(entityIdentifier1);
                                    entityIdentifier1.setClassName(BizConstant.SP_INHIBITCLASSID_EQUIPMENT);
                                    entityIdentifier1.setObjectID(processEquipmentID);
                                    entityIdentifier1.setAttribution("");
                                    Infos.EntityIdentifier entityIdentifier2 = new Infos.EntityIdentifier();
                                    entities.add(entityIdentifier2);
                                    entityIdentifier2.setClassName(BizConstant.SP_INHIBITCLASSID_MACHINERECIPE);
                                    entityIdentifier2.setObjectID(processMachineRecipeID);
                                    entityIdentifier2.setAttribution("");
                                    /**
                                     * Task-311 add ocapNo for constraint memo
                                     */
                                    entityInhibitDetailAttributes.setMemo(ocapMemo);
                                }
                                findFlag = false;
                                for (int n = 0; n < dcActionInfo_count; n++){
                                    if (CimArrayUtils.getSize(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getEntities()) == 2){
                                        if (CimStringUtils.equals(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getEntities().get(0).getClassName(),BizConstant.SP_INHIBITCLASSID_EQUIPMENT)
                                                && ObjectIdentifier.equalsWithValue(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getEntities().get(0).getObjectID(), processEquipmentID)
                                                && CimStringUtils.equals(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getEntities().get(1).getClassName(),BizConstant.SP_INHIBITCLASSID_MACHINERECIPE)
                                                && ObjectIdentifier.equalsWithValue(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getEntities().get(1).getObjectID(), processMachineRecipeID)
                                                && CimStringUtils.equals(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getReasonCode(),BizConstant.SP_REASON_SPCOUTOFRANGEINHIBIT)
                                                && CimStringUtils.equals(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getCorrespondingObjRefPO(),effectLot.getProcessOperation())
                                                && ObjectIdentifier.equalsWithValue(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getLotID(), effectLot.getLotID())){
                                            findFlag = true;
                                            break;
                                        }
                                    }
                                }
                                if(!findFlag){
                                    log.info("findFlag == FALSE ");
                                    // Add ActionResultInfo
                                    List<Infos.DCActionResultInfo> dcActionLotResult = dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo();
                                    Infos.DCActionResultInfo  dcActionResultInfo = new Infos.DCActionResultInfo();
                                    dcActionResultInfo.setLotID(effectLot.getLotID());
                                    dcActionResultInfo.setMonitorLotFlag(true);
                                    dcActionResultInfo.setDcDefID(spcCheckLot.getDcDefID());
                                    dcActionResultInfo.setDcSpecID(spcCheckLot.getDcSpecID());
                                    dcActionResultInfo.setCheckType(BizConstant.SP_ACTIONRESULT_CHECKTYPE_SPC);
                                    dcActionResultInfo.setReasonCode(BizConstant.SP_REASON_SPCOUTOFRANGEINHIBIT);
                                    dcActionResultInfo.setActionCode(BizConstant.SP_ACTIONCODE_RECIPEHOLD);
                                    dcActionResultInfo.setCorrespondingObjRefPO(effectLot.getProcessOperation());
                                    dcActionResultInfo.setBankID("");
                                    dcActionResultInfo.setReworkRouteID("");
                                    dcActionLotResult.add(dcActionResultInfo);
                                    List<Infos.EntityIdentifier> entityIdentifiers = new ArrayList<>();
                                    dcActionResultInfo.setEntities(entityIdentifiers);
                                    Infos.EntityIdentifier entityIdentifier1 = new Infos.EntityIdentifier();
                                    entityIdentifier1.setClassName(BizConstant.SP_INHIBITCLASSID_EQUIPMENT);
                                    entityIdentifier1.setAttribution("");
                                    entityIdentifier1.setObjectID(processEquipmentID);
                                    entityIdentifiers.add(entityIdentifier1);
                                    Infos.EntityIdentifier entityIdentifier2 = new Infos.EntityIdentifier();
                                    entityIdentifier2.setClassName(BizConstant.SP_INHIBITCLASSID_MACHINERECIPE);
                                    entityIdentifier2.setAttribution("");
                                    entityIdentifier2.setObjectID(processMachineRecipeID);
                                    entityIdentifiers.add(entityIdentifier2);
                                    dcActionInfo_count++;
                                }
                            }
                        }
                        //----------------------------------------------------------------------------                                                                                     //D5000147
                        // Inhibit Process Definition and Equipment                                                                                                                        //D5000147
                        //----------------------------------------------------------------------------
                        if (process_and_equipment_hold_flag){
                            log.info("process_and_equipment_hold_flag == TRUE");
                            if (aMachine != null){
                                log.info("aMainPD and aRecipe is not nil");
                                ObjectIdentifier processEquipmentID = ObjectIdentifier.build(aMachine.getIdentifier(), aMachine.getPrimaryKey());
                                CimProcessDefinition aProcessPD = aPOtmp.getProcessDefinition();
                                Validations.check(aProcessPD == null, new OmCode(retCodeConfig.getNotFoundPfx(), ""));
                                ObjectIdentifier processPDID = ObjectIdentifier.build(aProcessPD.getIdentifier(), aProcessPD.getPrimaryKey());
                                findFlag = false;
                                //  Check recipe_hold_inhibition is already registed or not.
                                //  If only not existed, entry new inhibition.
                                List<Infos.EntityInhibitDetailAttributes> entityInhibitions = strStartLot_actionList_EffectSPCCheck_out.getStrEntityInhibitions();
                                for (Infos.EntityInhibitDetailAttributes entityInhibition : entityInhibitions) {
                                    if (CimArrayUtils.getSize(entityInhibition.getEntities()) == 2){
                                        if (CimStringUtils.equals(entityInhibition.getEntities().get(0).getClassName(), BizConstant.SP_INHIBITCLASSID_OPERATION)
                                                && ObjectIdentifier.equalsWithValue(entityInhibition.getEntities().get(0).getObjectID(), processPDID)
                                                && CimStringUtils.equals(entityInhibition.getEntities().get(1).getClassName(), BizConstant.SP_INHIBITCLASSID_EQUIPMENT)
                                                && ObjectIdentifier.equalsWithValue(entityInhibition.getEntities().get(1).getObjectID(), processEquipmentID)) {
                                            findFlag = true;
                                            break;
                                        }
                                    }
                                }
                                if (!findFlag){
                                    log.info("findFlag == FALSE");
                                    entity_count++;
                                    Infos.EntityInhibitDetailAttributes entityInhibitDetailAttributes = new Infos.EntityInhibitDetailAttributes();
                                    entityInhibitions.add(entityInhibitDetailAttributes);
                                    entityInhibitDetailAttributes.setStartTimeStamp(CimDateUtils.getTimestampAsString(objCommon.getTimeStamp().getReportTimeStamp()));
                                    entityInhibitDetailAttributes.setEndTimeStamp(BizConstant.SP_TIMESTAMP_NIL_OBJECT_STRING);
                                    entityInhibitDetailAttributes.setReasonCode(BizConstant.SP_REASON_SPCOUTOFRANGEINHIBIT);
                                    entityInhibitDetailAttributes.setOwnerID(objCommon.getUser().getUserID());
                                    entityInhibitDetailAttributes.setClaimedTimeStamp(CimDateUtils.getTimestampAsString(objCommon.getTimeStamp().getReportTimeStamp()));
                                    List<Infos.EntityIdentifier> entities = new ArrayList<>();
                                    entityInhibitDetailAttributes.setEntities(entities);
                                    Infos.EntityIdentifier entityIdentifier1 = new Infos.EntityIdentifier();
                                    entities.add(entityIdentifier1);
                                    entityIdentifier1.setClassName(BizConstant.SP_INHIBITCLASSID_PROCESS);
                                    entityIdentifier1.setObjectID(processPDID);
                                    entityIdentifier1.setAttribution("");
                                    Infos.EntityIdentifier entityIdentifier2 = new Infos.EntityIdentifier();
                                    entities.add(entityIdentifier2);
                                    entityIdentifier2.setClassName(BizConstant.SP_INHIBITCLASSID_EQUIPMENT);
                                    entityIdentifier2.setObjectID(processEquipmentID);
                                    entityIdentifier2.setAttribution("");
                                    /**
                                     * Task-311 add ocapNo for constraint memo
                                     */
                                    entityInhibitDetailAttributes.setMemo(ocapMemo);
                                }
                                findFlag = false;
                                for (int n = 0; n < dcActionInfo_count; n++){
                                    if (CimArrayUtils.getSize(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getEntities()) == 2 ){
                                        if (CimStringUtils.equals(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getEntities().get(0).getClassName(),BizConstant.SP_INHIBITCLASSID_PROCESS)
                                                && ObjectIdentifier.equalsWithValue(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getEntities().get(0).getObjectID(), processPDID)
                                                && CimStringUtils.equals(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getEntities().get(1).getClassName(),BizConstant.SP_INHIBITCLASSID_EQUIPMENT)
                                                && ObjectIdentifier.equalsWithValue(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getEntities().get(1).getObjectID(), processEquipmentID)
                                                && CimStringUtils.equals(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getReasonCode(),BizConstant.SP_REASON_SPCOUTOFRANGEINHIBIT)
                                                && CimStringUtils.equals(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getCorrespondingObjRefPO(),effectLot.getProcessOperation())
                                                && ObjectIdentifier.equalsWithValue(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getLotID(), effectLot.getLotID())){
                                            findFlag = true;
                                            break;
                                        }
                                    }
                                }
                                if(!findFlag){
                                    log.info("findFlag == FALSE ");
                                    // Add ActionResultInfo
                                    List<Infos.DCActionResultInfo> dcActionLotResult = dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo();
                                    Infos.DCActionResultInfo  dcActionResultInfo = new Infos.DCActionResultInfo();
                                    dcActionResultInfo.setLotID(effectLot.getLotID());
                                    dcActionResultInfo.setMonitorLotFlag(true);
                                    dcActionResultInfo.setDcDefID(spcCheckLot.getDcDefID());
                                    dcActionResultInfo.setDcSpecID(spcCheckLot.getDcSpecID());
                                    dcActionResultInfo.setCheckType(BizConstant.SP_ACTIONRESULT_CHECKTYPE_SPC);
                                    dcActionResultInfo.setReasonCode(BizConstant.SP_REASON_SPCOUTOFRANGEINHIBIT);
                                    dcActionResultInfo.setActionCode(BizConstant.SP_ACTIONCODE_PROCESS_AND_EQUIPMENTHOLD);
                                    dcActionResultInfo.setCorrespondingObjRefPO(effectLot.getProcessOperation());
                                    dcActionResultInfo.setBankID("");
                                    dcActionResultInfo.setReworkRouteID("");
                                    dcActionLotResult.add(dcActionResultInfo);
                                    List<Infos.EntityIdentifier> entityIdentifiers = new ArrayList<>();
                                    dcActionResultInfo.setEntities(entityIdentifiers);
                                    Infos.EntityIdentifier entityIdentifier1 = new Infos.EntityIdentifier();
                                    entityIdentifier1.setClassName(BizConstant.SP_INHIBITCLASSID_PROCESS);
                                    entityIdentifier1.setAttribution("");
                                    entityIdentifier1.setObjectID(processPDID);
                                    entityIdentifiers.add(entityIdentifier1);
                                    Infos.EntityIdentifier entityIdentifier2 = new Infos.EntityIdentifier();
                                    entityIdentifier2.setClassName(BizConstant.SP_INHIBITCLASSID_EQUIPMENT);
                                    entityIdentifier2.setAttribution("");
                                    entityIdentifier2.setObjectID(processEquipmentID);
                                    entityIdentifiers.add(entityIdentifier2);
                                    dcActionInfo_count++;
                                }
                            }
                        }

                        //-------------------------------
                        // Chamber Inhibition
                        //-------------------------------
                        if (chamber_hold_flag){
                            log.info("chamber_hold_flag == TRUE");
                            ObjectIdentifier processEquipmentID = ObjectIdentifier.build(aMachine.getIdentifier(), aMachine.getPrimaryKey());
                            List<ProcessDTO.ProcessResourceInfoGroupByProcessResource> aPosProcResrcInfoByResrcs = aPOtmp.allAssignedProcessResourceInfoGroupByProcessResource();
                            for (ProcessDTO.ProcessResourceInfoGroupByProcessResource processResource : aPosProcResrcInfoByResrcs){
                                if (!ObjectIdentifier.isEmptyWithValue(processResource.getProcessResourceID())){
                                    String strProcessChamberID = processResource.getProcessResourceID().getValue();
                                    log.info("strProcessChamberID is not Blank");
                                    findFlag = false;
                                    //  Check recipe_hold_inhibition is already registed or not.
                                    //  If only not existed, entry new inhibition.
                                    List<Infos.EntityInhibitDetailAttributes> entityInhibitions = strStartLot_actionList_EffectSPCCheck_out.getStrEntityInhibitions();
                                    for (Infos.EntityInhibitDetailAttributes entityInhibition : entityInhibitions) {
                                        if (CimStringUtils.equals(entityInhibition.getEntities().get(0).getClassName(), BizConstant.SP_INHIBITCLASSID_CHAMBER)
                                                && ObjectIdentifier.equalsWithValue(entityInhibition.getEntities().get(0).getObjectID(), processEquipmentID)
                                                && CimStringUtils.equals(entityInhibition.getEntities().get(0).getAttribution(), strProcessChamberID)) {
                                            findFlag = true;
                                            break;
                                        }
                                    }
                                    if (!findFlag){
                                        log.info("findFlag == FALSE");
                                        entity_count++;
                                        Infos.EntityInhibitDetailAttributes entityInhibitDetailAttributes = new Infos.EntityInhibitDetailAttributes();
                                        entityInhibitions.add(entityInhibitDetailAttributes);
                                        entityInhibitDetailAttributes.setStartTimeStamp(CimDateUtils.getTimestampAsString(objCommon.getTimeStamp().getReportTimeStamp()));
                                        entityInhibitDetailAttributes.setEndTimeStamp(BizConstant.SP_TIMESTAMP_NIL_OBJECT_STRING);
                                        entityInhibitDetailAttributes.setReasonCode(BizConstant.SP_REASON_SPCOUTOFRANGEINHIBIT);
                                        entityInhibitDetailAttributes.setOwnerID(objCommon.getUser().getUserID());
                                        entityInhibitDetailAttributes.setClaimedTimeStamp(CimDateUtils.getTimestampAsString(objCommon.getTimeStamp().getReportTimeStamp()));
                                        List<Infos.EntityIdentifier> entities = new ArrayList<>();
                                        entityInhibitDetailAttributes.setEntities(entities);
                                        Infos.EntityIdentifier entityIdentifier = new Infos.EntityIdentifier();
                                        entities.add(entityIdentifier);
                                        entityIdentifier.setClassName(BizConstant.SP_INHIBITCLASSID_CHAMBER);
                                        entityIdentifier.setObjectID(processEquipmentID);
                                        entityIdentifier.setAttribution(strProcessChamberID);
                                        /**
                                         * Task-311 add ocapNo for constraint memo
                                         */
                                        entityInhibitDetailAttributes.setMemo(ocapMemo);
                                    }
                                    findFlag = false;
                                    for (int n = 0; n < dcActionInfo_count; n++){
                                        if (CimArrayUtils.getSize(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getEntities()) == 1 ){
                                            if (CimStringUtils.equals(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getEntities().get(0).getClassName(),BizConstant.SP_INHIBITCLASSID_CHAMBER)
                                                    && ObjectIdentifier.equalsWithValue(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getEntities().get(0).getObjectID(), processEquipmentID)
                                                    && CimStringUtils.equals(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getEntities().get(0).getAttribution(), strProcessChamberID)
                                                    && CimStringUtils.equals(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getReasonCode(),BizConstant.SP_REASON_SPCOUTOFRANGEINHIBIT)
                                                    && CimStringUtils.equals(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getCorrespondingObjRefPO(),effectLot.getProcessOperation())
                                                    && ObjectIdentifier.equalsWithValue(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getLotID(), effectLot.getLotID())){
                                                findFlag = true;
                                                break;
                                            }
                                        }
                                    }
                                    if(!findFlag){
                                        log.info("findFlag == FALSE ");
                                        // Add ActionResultInfo
                                        List<Infos.DCActionResultInfo> dcActionLotResult = dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo();
                                        Infos.DCActionResultInfo  dcActionResultInfo = new Infos.DCActionResultInfo();
                                        dcActionResultInfo.setLotID(effectLot.getLotID());
                                        dcActionResultInfo.setMonitorLotFlag(true);
                                        dcActionResultInfo.setDcDefID(spcCheckLot.getDcDefID());
                                        dcActionResultInfo.setDcSpecID(spcCheckLot.getDcSpecID());
                                        dcActionResultInfo.setCheckType(BizConstant.SP_ACTIONRESULT_CHECKTYPE_SPC);
                                        dcActionResultInfo.setReasonCode(BizConstant.SP_REASON_SPCOUTOFRANGEINHIBIT);
                                        dcActionResultInfo.setActionCode(BizConstant.SP_ACTIONCODE_CHAMBERHOLD);
                                        dcActionResultInfo.setCorrespondingObjRefPO(effectLot.getProcessOperation());
                                        dcActionResultInfo.setBankID("");
                                        dcActionResultInfo.setReworkRouteID("");
                                        dcActionLotResult.add(dcActionResultInfo);
                                        List<Infos.EntityIdentifier> entityIdentifiers = new ArrayList<>();
                                        dcActionResultInfo.setEntities(entityIdentifiers);
                                        Infos.EntityIdentifier entityIdentifier1 = new Infos.EntityIdentifier();
                                        entityIdentifier1.setClassName(BizConstant.SP_INHIBITCLASSID_CHAMBER);
                                        entityIdentifier1.setAttribution(strProcessChamberID);
                                        entityIdentifier1.setObjectID(processEquipmentID);
                                        entityIdentifiers.add(entityIdentifier1);
                                        dcActionInfo_count++;
                                    }
                                }
                            }
                        }

                        // Chamber and Recipe Inhibition
                        if (chamber_recipe_hold_flag){
                            log.info("chamber_recipe_hold_flag == TRUE");
                            ObjectIdentifier processEquipmentID = ObjectIdentifier.build(aMachine.getIdentifier(), aMachine.getPrimaryKey());
                            ObjectIdentifier processMachineRecipeID = ObjectIdentifier.build(aRecipe.getIdentifier(), aRecipe.getPrimaryKey());
                            List<ProcessDTO.ProcessResourceInfoGroupByProcessResource> aPosProcResrcInfoByResrcs = aPOtmp.allAssignedProcessResourceInfoGroupByProcessResource();
                            for (ProcessDTO.ProcessResourceInfoGroupByProcessResource processResource : aPosProcResrcInfoByResrcs){
                                if (!ObjectIdentifier.isEmptyWithValue(processResource.getProcessResourceID())){
                                    String strProcessChamberID = processResource.getProcessResourceID().getValue();
                                    findFlag = false;
                                    //  Check recipe_hold_inhibition is already registed or not.
                                    //  If only not existed, entry new inhibition.
                                    List<Infos.EntityInhibitDetailAttributes> entityInhibitions = strStartLot_actionList_EffectSPCCheck_out.getStrEntityInhibitions();
                                    for (Infos.EntityInhibitDetailAttributes entityInhibition : entityInhibitions) {
                                        if (CimArrayUtils.getSize(entityInhibition.getEntities()) == 2){
                                            if (CimStringUtils.equals(entityInhibition.getEntities().get(0).getClassName(), BizConstant.SP_INHIBITCLASSID_CHAMBER)
                                                    && ObjectIdentifier.equalsWithValue(entityInhibition.getEntities().get(0).getObjectID(), processEquipmentID)
                                                    && CimStringUtils.equals(entityInhibition.getEntities().get(0).getAttribution(), strProcessChamberID)
                                                    && CimStringUtils.equals(entityInhibition.getEntities().get(1).getClassName(), BizConstant.SP_INHIBITCLASSID_MACHINERECIPE)
                                                    && ObjectIdentifier.equalsWithValue(entityInhibition.getEntities().get(1).getObjectID(), processMachineRecipeID)) {
                                                findFlag = true;
                                                break;
                                            }
                                        }
                                    }
                                    if (!findFlag){
                                        log.info("findFlag == FALSE");
                                        entity_count++;
                                        Infos.EntityInhibitDetailAttributes entityInhibitDetailAttributes = new Infos.EntityInhibitDetailAttributes();
                                        entityInhibitions.add(entityInhibitDetailAttributes);
                                        entityInhibitDetailAttributes.setStartTimeStamp(CimDateUtils.getTimestampAsString(objCommon.getTimeStamp().getReportTimeStamp()));
                                        entityInhibitDetailAttributes.setEndTimeStamp(BizConstant.SP_TIMESTAMP_NIL_OBJECT_STRING);
                                        entityInhibitDetailAttributes.setReasonCode(BizConstant.SP_REASON_SPCOUTOFRANGEINHIBIT);
                                        entityInhibitDetailAttributes.setOwnerID(objCommon.getUser().getUserID());
                                        entityInhibitDetailAttributes.setClaimedTimeStamp(CimDateUtils.getTimestampAsString(objCommon.getTimeStamp().getReportTimeStamp()));
                                        List<Infos.EntityIdentifier> entities = new ArrayList<>();
                                        entityInhibitDetailAttributes.setEntities(entities);
                                        Infos.EntityIdentifier entityIdentifier1 = new Infos.EntityIdentifier();
                                        entities.add(entityIdentifier1);
                                        entityIdentifier1.setClassName(BizConstant.SP_INHIBITCLASSID_CHAMBER);
                                        entityIdentifier1.setObjectID(processEquipmentID);
                                        entityIdentifier1.setAttribution(strProcessChamberID);
                                        Infos.EntityIdentifier entityIdentifier2 = new Infos.EntityIdentifier();
                                        entities.add(entityIdentifier2);
                                        entityIdentifier2.setClassName(BizConstant.SP_INHIBITCLASSID_MACHINERECIPE);
                                        entityIdentifier2.setObjectID(processMachineRecipeID);
                                        entityIdentifier2.setAttribution("");
                                        /**
                                         * Task-311 add ocapNo for constraint memo
                                         */
                                        entityInhibitDetailAttributes.setMemo(ocapMemo);
                                    }
                                    findFlag = false;
                                    for (int n = 0; n < dcActionInfo_count; n++){
                                        if (CimArrayUtils.getSize(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getEntities()) == 2 ){
                                            if (CimStringUtils.equals(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getEntities().get(0).getClassName(),BizConstant.SP_INHIBITCLASSID_CHAMBER)
                                                    && ObjectIdentifier.equalsWithValue(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getEntities().get(0).getObjectID(), processEquipmentID)
                                                    && CimStringUtils.equals(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getEntities().get(0).getAttribution(), strProcessChamberID)
                                                    && CimStringUtils.equals(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getEntities().get(1).getClassName(),BizConstant.SP_INHIBITCLASSID_MACHINERECIPE)
                                                    && ObjectIdentifier.equalsWithValue(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getEntities().get(1).getObjectID(), processMachineRecipeID)
                                                    && CimStringUtils.equals(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getReasonCode(),BizConstant.SP_REASON_SPCOUTOFRANGEINHIBIT)
                                                    && CimStringUtils.equals(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getCorrespondingObjRefPO(),effectLot.getProcessOperation())
                                                    && ObjectIdentifier.equalsWithValue(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getLotID(), effectLot.getLotID())){
                                                findFlag = true;
                                                break;
                                            }
                                        }
                                    }
                                    if(!findFlag){
                                        log.info("findFlag == FALSE ");
                                        // Add ActionResultInfo
                                        List<Infos.DCActionResultInfo> dcActionLotResult = dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo();
                                        Infos.DCActionResultInfo  dcActionResultInfo = new Infos.DCActionResultInfo();
                                        dcActionResultInfo.setLotID(effectLot.getLotID());
                                        dcActionResultInfo.setMonitorLotFlag(true);
                                        dcActionResultInfo.setDcDefID(spcCheckLot.getDcDefID());
                                        dcActionResultInfo.setDcSpecID(spcCheckLot.getDcSpecID());
                                        dcActionResultInfo.setCheckType(BizConstant.SP_ACTIONRESULT_CHECKTYPE_SPC);
                                        dcActionResultInfo.setReasonCode(BizConstant.SP_REASON_SPCOUTOFRANGEINHIBIT);
                                        dcActionResultInfo.setActionCode(BizConstant.SP_ACTIONCODE_CHAMBERANDRECIPEHOLD);
                                        dcActionResultInfo.setCorrespondingObjRefPO(effectLot.getProcessOperation());
                                        dcActionResultInfo.setBankID("");
                                        dcActionResultInfo.setReworkRouteID("");
                                        dcActionLotResult.add(dcActionResultInfo);
                                        List<Infos.EntityIdentifier> entityIdentifiers = new ArrayList<>();
                                        dcActionResultInfo.setEntities(entityIdentifiers);
                                        Infos.EntityIdentifier entityIdentifier1 = new Infos.EntityIdentifier();
                                        entityIdentifier1.setClassName(BizConstant.SP_INHIBITCLASSID_CHAMBER);
                                        entityIdentifier1.setAttribution(strProcessChamberID);
                                        entityIdentifier1.setObjectID(processEquipmentID);
                                        entityIdentifiers.add(entityIdentifier1);
                                        Infos.EntityIdentifier entityIdentifier2 = new Infos.EntityIdentifier();
                                        entityIdentifier2.setClassName(BizConstant.SP_INHIBITCLASSID_MACHINERECIPE);
                                        entityIdentifier2.setAttribution("");
                                        entityIdentifier2.setObjectID(processMachineRecipeID);
                                        entityIdentifiers.add(entityIdentifier2);
                                        dcActionInfo_count++;
                                    }
                                }
                            }
                        }

                        //  Make strMessageAttributes of out parameter.
                        //
                        //    ( actionCode are filled only when spcCheckResult is SP_ActionCode_Mail. )
                        if (mail_flag){
                            log.info( "mail_flag == TRUE");
                            if (CimStringUtils.equals(spcCheckLot.getSpcCheckResult(),BizConstant.SP_SPCCHECK_HOLDLIMITOFF)
                                    || CimStringUtils.equals(spcCheckLot.getSpcCheckResult(),BizConstant.SP_SPCCHECK_WARNINGLIMITOFF)){

                                //===   P3000383 add start   ===
                                /*----- get Lot Object -----*/
                                aLot = baseCoreFactory.getBO(CimLot.class,effectLot.getLotID());
                                Validations.check(aLot == null, new OmCode(retCodeConfig.getNotFoundLot(), ObjectIdentifier.fetchValue(effectedLot.getLotID())));
                                /*----- get Product ID -----*/
                                CimProductSpecification aProductSpec = aLot.getProductSpecification();
                                String productID = null;
                                if (aProductSpec != null){
                                    productID = aProductSpec.getIdentifier();
                                }
                                /*----- get equipment ID -----*/
                                String machineID = null;
                                if (aMachine != null){
                                    machineID = aMachine.getIdentifier();
                                }
                                /*----- get machine recipe ID -----*/
                                String recipeID = null;
                                if (aRecipe != null){
                                    recipeID = aRecipe.getIdentifier();
                                }
                                /*----- get route ID -----*/
                                String mainPDID = null;
                                if (aMainPD != null){
                                    mainPDID = aMainPD.getIdentifier();
                                }
                                /*----- make e-mail text -----*/

                                String tmpMsg = "=============================================\n"+
                                        "===       SPC Violation Information       ===\n"+
                                        "=============================================\n"+
                                        "Monitor LotID          : "+ ObjectIdentifier.fetchValue(effectedLots.get(0).getLotID()) + " \n"+
                                        "LotID                  : "+ ObjectIdentifier.fetchValue(effectLot.getLotID()) +" \n"+
                                        "ProductID              : "+ productID +" \n"+
                                        "ProcessRouteID         : "+ mainPDID +" \n"+
                                        "ProcessOperationNumber : "+ operationNo +" \n"+
                                        "ProcessEquipmentID     : "+ machineID +" \n"+
                                        "ProcessMachineRecipeID : "+ recipeID;

                                findFlag = false;
                                //  Check if strMessageAttributes is already registed with the same lotID & reasonCode or not.
                                //  If only not existed, entry new inhibition.
                                List<Infos.MessageAttributes> messageList = strStartLot_actionList_EffectSPCCheck_out.getStrMessageList();
                                for (Infos.MessageAttributes messageAttributes : messageList) {
                                    if (CimStringUtils.equals(messageAttributes.getReasonCode(), spcCheckLot.getSpcCheckResult())
                                            && ObjectIdentifier.equalsWithValue(messageAttributes.getLotID(), effectLot.getLotID())
                                            && ObjectIdentifier.equalsWithValue(messageAttributes.getRouteID(), spcCheckLot.getProcessRouteID())
                                            && CimStringUtils.equals(messageAttributes.getOperationNumber(), spcCheckLot.getProcessOperationNumber())) {
                                        log.info("set findFlag = TRUE");
                                        findFlag = true;
                                    }
                                }
                                if (!findFlag){
                                    log.info("findFlag == FALSE");
                                    //  Get Main PD ID :重复取值？？？略过
                                    ObjectIdentifier eqpID = ObjectIdentifier.build(aMachine.getIdentifier(), aMachine.getPrimaryKey());
                                    ObjectIdentifier routeID = ObjectIdentifier.build(aMainPD.getIdentifier(), aMainPD.getPrimaryKey());
                                    msg_count++;

                                    List<Infos.MessageAttributes> strMessageList = strStartLot_actionList_EffectSPCCheck_out.getStrMessageList();
                                    Infos.MessageAttributes strMessage = new Infos.MessageAttributes();
                                    strMessageList.add(strMessage);
                                    strMessage.setMessageID(ObjectIdentifier.buildWithValue(messageID));
                                    strMessage.setLotID(effectLot.getLotID());
                                    strMessage.setLotStatus("");
                                    strMessage.setEquipmentID(eqpID);
                                    strMessage.setRouteID(routeID);
                                    strMessage.setOperationNumber(operationNo);
                                    strMessage.setReasonCode(spcCheckLot.getSpcCheckResult());
                                    strMessage.setMessageText(tmpMsg);

                                    // Add ActionResultInfo
                                    List<Infos.DCActionResultInfo> dcActionLotResult = dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo();
                                    Infos.DCActionResultInfo  dcActionResultInfo = new Infos.DCActionResultInfo();
                                    dcActionResultInfo.setLotID(effectLot.getLotID());
                                    dcActionResultInfo.setMonitorLotFlag(true);
                                    dcActionResultInfo.setDcDefID(spcCheckLot.getDcDefID());
                                    dcActionResultInfo.setDcSpecID(spcCheckLot.getDcSpecID());
                                    dcActionResultInfo.setCheckType(BizConstant.SP_ACTIONRESULT_CHECKTYPE_SPC);
                                    dcActionResultInfo.setReasonCode(BizConstant.SP_REASON_SPCOUTOFRANGEINHIBIT);
                                    dcActionResultInfo.setActionCode(BizConstant.SP_ACTIONCODE_MAILSEND);
                                    dcActionResultInfo.setCorrespondingObjRefPO(effectLot.getProcessOperation());
                                    dcActionResultInfo.setBankID("");
                                    dcActionResultInfo.setReworkRouteID("");
                                    dcActionLotResult.add(dcActionResultInfo);
                                    dcActionInfo_count++;

                                }
                            }
                        }
                    }else {
                        log.info("correspondingPO is other Fab");
                        if (equipment_hold_flag) {
                            log.info("equipment_hold_flag == TRUE");
                            //-------------------------------------------------------------------
                            //  Check equipment_hold_inhibition is already registed or not.
                            //  If only not existed, entry new inhibition.
                            //-------------------------------------------------------------------
                            log.info("Check equipment_hold_inhibition is already registed or not");
                            findFlag = false;
                            List<Infos.EntityInhibitAttributesWithFabInfo> entityInhibitAttributesWithFabInfos = strStartLot_actionList_EffectSPCCheck_out.getEntityInhibitionsWithFabInfo();
                            for (Infos.EntityInhibitAttributesWithFabInfo entityInhibitAttributesWithFabInfo : entityInhibitAttributesWithFabInfos) {
                                if (CimStringUtils.equals(entityInhibitAttributesWithFabInfo.getEntityInhibitAttributes().getEntities().get(0).getClassName(), BizConstant.SP_INHIBITCLASSID_EQUIPMENT)
                                        && ObjectIdentifier.equalsWithValue(entityInhibitAttributesWithFabInfo.getEntityInhibitAttributes().getEntities().get(0).getObjectID(), strProcessOperationInfo.getAsgnEqpID())) {
                                    findFlag = true;
                                    break;
                                }
                            }
                            if (!findFlag){
                                log.info("findFlag == FALSE");
                                entity_countForOtherFab++;
                                Infos.EntityInhibitAttributesWithFabInfo entityInhibitAttributesWithFabInfo = new Infos.EntityInhibitAttributesWithFabInfo();
                                entityInhibitAttributesWithFabInfos.add(entityInhibitAttributesWithFabInfo);
                                entityInhibitAttributesWithFabInfo.setFabID(correspondingFabID);
                                Infos.EntityInhibitAttributes entityInhibitAttributes = new Infos.EntityInhibitAttributes();
                                entityInhibitAttributesWithFabInfo.setEntityInhibitAttributes(entityInhibitAttributes);
                                List<Infos.EntityIdentifier> entities = new ArrayList<>();
                                entityInhibitAttributes.setEntities(entities);
                                Infos.EntityIdentifier entityIdentifier = new Infos.EntityIdentifier();
                                entities.add(entityIdentifier);
                                entityIdentifier.setClassName(BizConstant.SP_INHIBITCLASSID_EQUIPMENT);
                                entityIdentifier.setAttribution("");
                                entityIdentifier.setObjectID(strProcessOperationInfo.getAsgnEqpID());
                                entityInhibitAttributes.setSubLotTypes(new ArrayList<>());
                                entityInhibitAttributes.setStartTimeStamp(CimDateUtils.getTimestampAsString(objCommon.getTimeStamp().getReportTimeStamp()));
                                entityInhibitAttributes.setEndTimeStamp(BizConstant.SP_TIMESTAMP_NIL_OBJECT_STRING);
                                entityInhibitAttributes.setReasonCode(BizConstant.SP_REASON_SPCOUTOFRANGEINHIBIT);
                                entityInhibitAttributes.setOwnerID(objCommon.getUser().getUserID());
                                entityInhibitAttributes.setClaimedTimeStamp(CimDateUtils.getTimestampAsString(objCommon.getTimeStamp().getReportTimeStamp()));
                                /**
                                 * Task-311 add ocapNo for constraint memo
                                 */
                                entityInhibitAttributes.setMemo(ocapMemo);
                            }
                            findFlag = false;
                            for (int n = 0 ; n < dcActionInfo_count ; n++ ) {
                                if (CimArrayUtils.getSize(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getEntities()) == 1 ){
                                    if (CimStringUtils.equals(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getEntities().get(0).getClassName(),BizConstant.SP_INHIBITCLASSID_EQUIPMENT)
                                            && ObjectIdentifier.equalsWithValue(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getEntities().get(0).getObjectID(), strProcessOperationInfo.getAsgnEqpID())
                                            && CimStringUtils.equals(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getReasonCode(),BizConstant.SP_REASON_SPCOUTOFRANGEINHIBIT)
                                            && CimStringUtils.equals(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getCorrespondingObjRefPO(),spcCheckLot.getProcessObjrefPO())
                                            && ObjectIdentifier.equalsWithValue(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getLotID(), effectLot.getLotID())){
                                        log.info("break!!");
                                        findFlag = TRUE;
                                        break;
                                    }
                                }
                            }
                            if (!findFlag){
                                log.info("findFlag == FALSE  [machineID]");
                                // Add ActionResultInfo
                                List<Infos.DCActionResultInfo> dcActionLotResult = dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo();
                                Infos.DCActionResultInfo dcActionResultInfo = new Infos.DCActionResultInfo();
                                dcActionResultInfo.setLotID(effectLot.getLotID());
                                dcActionResultInfo.setMonitorLotFlag(true);
                                dcActionResultInfo.setDcDefID(spcCheckLot.getDcDefID());
                                dcActionResultInfo.setDcSpecID(spcCheckLot.getDcSpecID());
                                dcActionResultInfo.setCheckType(BizConstant.SP_ACTIONRESULT_CHECKTYPE_SPC);
                                dcActionResultInfo.setReasonCode(BizConstant.SP_REASON_SPCOUTOFRANGEINHIBIT);
                                dcActionResultInfo.setActionCode(BizConstant.SP_ACTIONCODE_EQUIPMENTHOLD);
                                dcActionResultInfo.setCorrespondingObjRefPO(spcCheckLot.getProcessObjrefPO());
                                dcActionResultInfo.setBankID("");
                                dcActionResultInfo.setReworkRouteID("");
                                List<Infos.EntityIdentifier> entityIdentifiers = new ArrayList<>();
                                dcActionResultInfo.setEntities(entityIdentifiers);
                                Infos.EntityIdentifier entityIdentifier = new Infos.EntityIdentifier();
                                entityIdentifier.setClassName(BizConstant.SP_INHIBITCLASSID_EQUIPMENT);
                                entityIdentifier.setAttribution("");
                                entityIdentifier.setObjectID(strProcessOperationInfo.getAsgnEqpID());
                                entityIdentifiers.add(entityIdentifier);
                                dcActionLotResult.add(dcActionResultInfo);
                                dcActionInfo_count++;
                            }
                        }
                        if (recipe_hold_flag){
                            //-------------------------------------------------------------------
                            //  Check recipe_hold_inhibition is already registed or not.
                            //  If only not existed, entry new inhibition.
                            //-------------------------------------------------------------------
                            findFlag = false;
                            List<Infos.EntityInhibitAttributesWithFabInfo> entityInhibitAttributesWithFabInfos = strStartLot_actionList_EffectSPCCheck_out.getEntityInhibitionsWithFabInfo();
                            for (Infos.EntityInhibitAttributesWithFabInfo entityInhibitAttributesWithFabInfo : entityInhibitAttributesWithFabInfos) {
                                if (CimStringUtils.equals(entityInhibitAttributesWithFabInfo.getEntityInhibitAttributes().getEntities().get(0).getClassName(), BizConstant.SP_INHIBITCLASSID_MACHINERECIPE)
                                        && ObjectIdentifier.equalsWithValue(entityInhibitAttributesWithFabInfo.getEntityInhibitAttributes().getEntities().get(0).getObjectID(), strProcessOperationInfo.getAsgnRecipeID())) {
                                    findFlag = true;
                                    break;
                                }
                            }
                            if (!findFlag){
                                log.info("findFlag == FALSE");
                                entity_countForOtherFab++;
                                Infos.EntityInhibitAttributesWithFabInfo entityInhibitAttributesWithFabInfo = new Infos.EntityInhibitAttributesWithFabInfo();
                                entityInhibitAttributesWithFabInfo.setFabID(correspondingFabID);
                                entityInhibitAttributesWithFabInfos.add(entityInhibitAttributesWithFabInfo);
                                Infos.EntityInhibitAttributes entityInhibitAttributes = new Infos.EntityInhibitAttributes();
                                entityInhibitAttributesWithFabInfo.setEntityInhibitAttributes(entityInhibitAttributes);
                                List<Infos.EntityIdentifier> entities = new ArrayList<>();
                                entityInhibitAttributes.setEntities(entities);
                                Infos.EntityIdentifier entityIdentifier = new Infos.EntityIdentifier();
                                entities.add(entityIdentifier);
                                entityIdentifier.setClassName(BizConstant.SP_INHIBITCLASSID_MACHINERECIPE);
                                entityIdentifier.setAttribution("");
                                entityIdentifier.setObjectID(strProcessOperationInfo.getAsgnRecipeID());
                                entityInhibitAttributes.setSubLotTypes(new ArrayList<>());
                                entityInhibitAttributes.setStartTimeStamp(CimDateUtils.getTimestampAsString(objCommon.getTimeStamp().getReportTimeStamp()));
                                entityInhibitAttributes.setEndTimeStamp(BizConstant.SP_TIMESTAMP_NIL_OBJECT_STRING);
                                entityInhibitAttributes.setReasonCode(BizConstant.SP_REASON_SPCOUTOFRANGEINHIBIT);
                                entityInhibitAttributes.setOwnerID(objCommon.getUser().getUserID());
                                entityInhibitAttributes.setClaimedTimeStamp(CimDateUtils.getTimestampAsString(objCommon.getTimeStamp().getReportTimeStamp()));
                                /**
                                 * Task-311 add ocapNo for constraint memo
                                 */
                                entityInhibitAttributes.setMemo(ocapMemo);
                            }
                            findFlag = false;
                            for (int n = 0 ; n < dcActionInfo_count ; n++ ) {
                                if (CimArrayUtils.getSize(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getEntities()) == 1 ){
                                    if (CimStringUtils.equals(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getEntities().get(0).getClassName(),BizConstant.SP_INHIBITCLASSID_MACHINERECIPE)
                                            && ObjectIdentifier.equalsWithValue(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getEntities().get(0).getObjectID(), strProcessOperationInfo.getAsgnRecipeID())
                                            && CimStringUtils.equals(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getReasonCode(),BizConstant.SP_REASON_SPCOUTOFRANGEINHIBIT)
                                            && CimStringUtils.equals(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getCorrespondingObjRefPO(),spcCheckLot.getProcessObjrefPO())
                                            && ObjectIdentifier.equalsWithValue(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getLotID(), effectLot.getLotID())){
                                        log.info("break!!");
                                        findFlag = true;
                                        break;
                                    }
                                }
                            }
                            if (!findFlag){
                                log.info("findFlag == FALSE  [machineID]");
                                // Add ActionResultInfo
                                List<Infos.DCActionResultInfo> dcActionLotResult = dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo();
                                Infos.DCActionResultInfo  dcActionResultInfo = new Infos.DCActionResultInfo();
                                dcActionResultInfo.setLotID(effectLot.getLotID());
                                dcActionResultInfo.setMonitorLotFlag(true);
                                dcActionResultInfo.setDcDefID(spcCheckLot.getDcDefID());
                                dcActionResultInfo.setDcSpecID(spcCheckLot.getDcSpecID());
                                dcActionResultInfo.setCheckType(BizConstant.SP_ACTIONRESULT_CHECKTYPE_SPC);
                                dcActionResultInfo.setReasonCode(BizConstant.SP_REASON_SPCOUTOFRANGEINHIBIT);
                                dcActionResultInfo.setActionCode(BizConstant.SP_ACTIONCODE_RECIPEHOLD);
                                dcActionResultInfo.setCorrespondingObjRefPO(spcCheckLot.getProcessObjrefPO());
                                dcActionResultInfo.setBankID("");
                                dcActionResultInfo.setReworkRouteID("");
                                List<Infos.EntityIdentifier> entityIdentifiers = new ArrayList<>();
                                Infos.EntityIdentifier entityIdentifier = new Infos.EntityIdentifier();
                                entityIdentifier.setClassName(BizConstant.SP_INHIBITCLASSID_MACHINERECIPE);
                                entityIdentifier.setAttribution("");
                                entityIdentifier.setObjectID(strProcessOperationInfo.getAsgnRecipeID());
                                entityIdentifiers.add(entityIdentifier);
                                dcActionResultInfo.setEntities(entityIdentifiers);
                                dcActionLotResult.add(dcActionResultInfo);
                                dcActionInfo_count++;
                            }
                        }

                        if (route_hold_flag){
                            //-------------------------------------------------------------------
                            //  Check route_hold_inhibition is already registed or not.
                            //  If only not existed, entry new inhibition.
                            //-------------------------------------------------------------------
                            findFlag = false;
                            List<Infos.EntityInhibitAttributesWithFabInfo> entityInhibitAttributesWithFabInfos = strStartLot_actionList_EffectSPCCheck_out.getEntityInhibitionsWithFabInfo();
                            for (Infos.EntityInhibitAttributesWithFabInfo entityInhibitAttributesWithFabInfo : entityInhibitAttributesWithFabInfos) {
                                if (CimStringUtils.equals(entityInhibitAttributesWithFabInfo.getEntityInhibitAttributes().getEntities().get(0).getClassName(), BizConstant.SP_INHIBITCLASSID_ROUTE)
                                        && ObjectIdentifier.equalsWithValue(entityInhibitAttributesWithFabInfo.getEntityInhibitAttributes().getEntities().get(0).getObjectID(), strProcessOperationInfo.getMainPDID())
                                        && CimStringUtils.equals(entityInhibitAttributesWithFabInfo.getEntityInhibitAttributes().getEntities().get(0).getAttribution(),"")) {
                                    findFlag = true;
                                    break;
                                }
                            }
                            if (!findFlag){
                                log.info("findFlag == FALSE");
                                entity_countForOtherFab++;
                                Infos.EntityInhibitAttributesWithFabInfo entityInhibitAttributesWithFabInfo = new Infos.EntityInhibitAttributesWithFabInfo();
                                entityInhibitAttributesWithFabInfo.setFabID(correspondingFabID);
                                entityInhibitAttributesWithFabInfos.add(entityInhibitAttributesWithFabInfo);
                                Infos.EntityInhibitAttributes entityInhibitAttributes = new Infos.EntityInhibitAttributes();
                                entityInhibitAttributesWithFabInfo.setEntityInhibitAttributes(entityInhibitAttributes);
                                List<Infos.EntityIdentifier> entities = new ArrayList<>();
                                entityInhibitAttributes.setEntities(entities);
                                Infos.EntityIdentifier entityIdentifier = new Infos.EntityIdentifier();
                                entities.add(entityIdentifier);
                                entityIdentifier.setClassName(BizConstant.SP_INHIBITCLASSID_ROUTE);
                                entityIdentifier.setAttribution("");
                                entityIdentifier.setObjectID(strProcessOperationInfo.getMainPDID());
                                entityInhibitAttributes.setSubLotTypes(new ArrayList<>());
                                entityInhibitAttributes.setStartTimeStamp(CimDateUtils.getTimestampAsString(objCommon.getTimeStamp().getReportTimeStamp()));
                                entityInhibitAttributes.setEndTimeStamp(BizConstant.SP_TIMESTAMP_NIL_OBJECT_STRING);
                                entityInhibitAttributes.setReasonCode(BizConstant.SP_REASON_SPCOUTOFRANGEINHIBIT);
                                entityInhibitAttributes.setOwnerID(objCommon.getUser().getUserID());
                                entityInhibitAttributes.setClaimedTimeStamp(CimDateUtils.getTimestampAsString(objCommon.getTimeStamp().getReportTimeStamp()));
                                /**
                                 * Task-311 add ocapNo for constraint memo
                                 */
                                entityInhibitAttributes.setMemo(ocapMemo);
                            }
                            findFlag = false;
                            for (int n = 0 ; n < dcActionInfo_count ; n++ ) {
                                if (CimArrayUtils.getSize(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getEntities()) == 1 ){
                                    if (CimStringUtils.equals(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getEntities().get(0).getClassName(),BizConstant.SP_INHIBITCLASSID_ROUTE)
                                            && ObjectIdentifier.equalsWithValue(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getEntities().get(0).getObjectID(), strProcessOperationInfo.getMainPDID())
                                            && CimStringUtils.equals(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getEntities().get(0).getAttribution(),"")
                                            && CimStringUtils.equals(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getReasonCode(),BizConstant.SP_REASON_SPCOUTOFRANGEINHIBIT)
                                            && CimStringUtils.equals(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getCorrespondingObjRefPO(),spcCheckLot.getProcessObjrefPO())
                                            && ObjectIdentifier.equalsWithValue(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getLotID(), effectLot.getLotID())){
                                        log.info("break!!");
                                        findFlag = true;
                                        break;
                                    }
                                }
                            }
                            if (!findFlag){
                                log.info("findFlag == FALSE  [machineID]");
                                // Add ActionResultInfo
                                List<Infos.DCActionResultInfo> dcActionLotResult = dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo();
                                Infos.DCActionResultInfo  dcActionResultInfo = new Infos.DCActionResultInfo();
                                dcActionResultInfo.setLotID(effectLot.getLotID());
                                dcActionResultInfo.setMonitorLotFlag(true);
                                dcActionResultInfo.setDcDefID(spcCheckLot.getDcDefID());
                                dcActionResultInfo.setDcSpecID(spcCheckLot.getDcSpecID());
                                dcActionResultInfo.setCheckType(BizConstant.SP_ACTIONRESULT_CHECKTYPE_SPC);
                                dcActionResultInfo.setReasonCode(BizConstant.SP_REASON_SPCOUTOFRANGEINHIBIT);
                                dcActionResultInfo.setActionCode(BizConstant.SP_ACTIONCODE_ROUTEHOLD);
                                dcActionResultInfo.setCorrespondingObjRefPO(spcCheckLot.getProcessObjrefPO());
                                dcActionResultInfo.setBankID("");
                                dcActionResultInfo.setReworkRouteID("");
                                List<Infos.EntityIdentifier> entityIdentifiers = new ArrayList<>();
                                Infos.EntityIdentifier entityIdentifier = new Infos.EntityIdentifier();
                                entityIdentifier.setClassName(BizConstant.SP_INHIBITCLASSID_ROUTE);
                                entityIdentifier.setAttribution("");
                                entityIdentifier.setObjectID(strProcessOperationInfo.getMainPDID());
                                entityIdentifiers.add(entityIdentifier);
                                dcActionResultInfo.setEntities(entityIdentifiers);
                                dcActionLotResult.add(dcActionResultInfo);
                                dcActionInfo_count++;
                            }
                        }

                        if (process_hold_flag){
                            //-------------------------------------------------------------------
                            //  Check process_hold_inhibition is already registed or not.
                            //  If only not existed, entry new inhibition.
                            //-------------------------------------------------------------------
                            findFlag = false;
                            List<Infos.EntityInhibitAttributesWithFabInfo> entityInhibitAttributesWithFabInfos = strStartLot_actionList_EffectSPCCheck_out.getEntityInhibitionsWithFabInfo();
                            for (Infos.EntityInhibitAttributesWithFabInfo entityInhibitAttributesWithFabInfo : entityInhibitAttributesWithFabInfos) {
                                if (CimStringUtils.equals(entityInhibitAttributesWithFabInfo.getEntityInhibitAttributes().getEntities().get(0).getClassName(), BizConstant.SP_INHIBITCLASSID_OPERATION)
                                        && ObjectIdentifier.equalsWithValue(entityInhibitAttributesWithFabInfo.getEntityInhibitAttributes().getEntities().get(0).getObjectID(), strProcessOperationInfo.getMainPDID())
                                        && CimStringUtils.equals(entityInhibitAttributesWithFabInfo.getEntityInhibitAttributes().getEntities().get(0).getAttribution(),strProcessOperationInfo.getOpeNo())) {
                                    findFlag = true;
                                    break;
                                }
                            }
                            if (!findFlag){
                                log.info("findFlag == FALSE");
                                entity_countForOtherFab++;
                                Infos.EntityInhibitAttributesWithFabInfo entityInhibitAttributesWithFabInfo = new Infos.EntityInhibitAttributesWithFabInfo();
                                entityInhibitAttributesWithFabInfo.setFabID(correspondingFabID);
                                entityInhibitAttributesWithFabInfos.add(entityInhibitAttributesWithFabInfo);
                                Infos.EntityInhibitAttributes entityInhibitAttributes = new Infos.EntityInhibitAttributes();
                                entityInhibitAttributesWithFabInfo.setEntityInhibitAttributes(entityInhibitAttributes);
                                List<Infos.EntityIdentifier> entities = new ArrayList<>();
                                entityInhibitAttributes.setEntities(entities);
                                Infos.EntityIdentifier entityIdentifier = new Infos.EntityIdentifier();
                                entities.add(entityIdentifier);
                                entityIdentifier.setClassName(BizConstant.SP_INHIBITCLASSID_OPERATION);
                                entityIdentifier.setAttribution(strProcessOperationInfo.getOpeNo());
                                entityIdentifier.setObjectID(strProcessOperationInfo.getMainPDID());
                                entityInhibitAttributes.setSubLotTypes(new ArrayList<>());
                                entityInhibitAttributes.setStartTimeStamp(CimDateUtils.getTimestampAsString(objCommon.getTimeStamp().getReportTimeStamp()));
                                entityInhibitAttributes.setEndTimeStamp(BizConstant.SP_TIMESTAMP_NIL_OBJECT_STRING);
                                entityInhibitAttributes.setReasonCode(BizConstant.SP_REASON_SPCOUTOFRANGEINHIBIT);
                                entityInhibitAttributes.setOwnerID(objCommon.getUser().getUserID());
                                entityInhibitAttributes.setClaimedTimeStamp(CimDateUtils.getTimestampAsString(objCommon.getTimeStamp().getReportTimeStamp()));
                                /**
                                 * Task-311 add ocapNo for constraint memo
                                 */
                                entityInhibitAttributes.setMemo(ocapMemo);
                            }
                            findFlag = false;
                            for (int n = 0 ; n < dcActionInfo_count ; n++ ) {
                                if (CimArrayUtils.getSize(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getEntities()) == 1 ){
                                    if (CimStringUtils.equals(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getEntities().get(0).getClassName(),BizConstant.SP_INHIBITCLASSID_OPERATION)
                                            && ObjectIdentifier.equalsWithValue(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getEntities().get(0).getObjectID(), strProcessOperationInfo.getMainPDID())
                                            && CimStringUtils.equals(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getEntities().get(0).getAttribution(),strProcessOperationInfo.getOpeNo())
                                            && CimStringUtils.equals(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getReasonCode(),BizConstant.SP_REASON_SPCOUTOFRANGEINHIBIT)
                                            && CimStringUtils.equals(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getCorrespondingObjRefPO(),spcCheckLot.getProcessObjrefPO())
                                            && ObjectIdentifier.equalsWithValue(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getLotID(), effectLot.getLotID())){
                                        log.info("break!!");
                                        findFlag = true;
                                        break;
                                    }
                                }
                            }
                            if (!findFlag){
                                log.info("findFlag == FALSE  [machineID]");
                                // Add ActionResultInfo
                                List<Infos.DCActionResultInfo> dcActionLotResult = dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo();
                                Infos.DCActionResultInfo  dcActionResultInfo = new Infos.DCActionResultInfo();
                                dcActionResultInfo.setLotID(effectLot.getLotID());
                                dcActionResultInfo.setMonitorLotFlag(true);
                                dcActionResultInfo.setDcDefID(spcCheckLot.getDcDefID());
                                dcActionResultInfo.setDcSpecID(spcCheckLot.getDcSpecID());
                                dcActionResultInfo.setCheckType(BizConstant.SP_ACTIONRESULT_CHECKTYPE_SPC);
                                dcActionResultInfo.setReasonCode(BizConstant.SP_REASON_SPCOUTOFRANGEINHIBIT);
                                dcActionResultInfo.setActionCode(BizConstant.SP_ACTIONCODE_PROCESSHOLD);
                                dcActionResultInfo.setCorrespondingObjRefPO(spcCheckLot.getProcessObjrefPO());
                                dcActionResultInfo.setBankID("");
                                dcActionResultInfo.setReworkRouteID("");
                                List<Infos.EntityIdentifier> entityIdentifiers = new ArrayList<>();
                                Infos.EntityIdentifier entityIdentifier = new Infos.EntityIdentifier();
                                entityIdentifier.setClassName(BizConstant.SP_INHIBITCLASSID_OPERATION);
                                entityIdentifier.setAttribution(strProcessOperationInfo.getOpeNo());
                                entityIdentifier.setObjectID(strProcessOperationInfo.getMainPDID());
                                entityIdentifiers.add(entityIdentifier);
                                dcActionResultInfo.setEntities(entityIdentifiers);
                                dcActionLotResult.add(dcActionResultInfo);
                                dcActionInfo_count++;
                            }
                        }

                        if (equipment_and_recipe_hold_flag){
                            //-------------------------------------------------------------------
                            //  Check equipment_and_recipe_hold_inhibition is already registed or not.
                            //  If only not existed, entry new inhibition.
                            //-------------------------------------------------------------------
                            findFlag = false;
                            List<Infos.EntityInhibitAttributesWithFabInfo> entityInhibitAttributesWithFabInfos = strStartLot_actionList_EffectSPCCheck_out.getEntityInhibitionsWithFabInfo();
                            for (Infos.EntityInhibitAttributesWithFabInfo entityInhibitAttributesWithFabInfo : entityInhibitAttributesWithFabInfos) {
                                if (CimArrayUtils.getSize(entityInhibitAttributesWithFabInfo.getEntityInhibitAttributes().getEntities()) == 2){
                                    if (CimStringUtils.equals(entityInhibitAttributesWithFabInfo.getEntityInhibitAttributes().getEntities().get(0).getClassName(), BizConstant.SP_INHIBITCLASSID_EQUIPMENT)
                                            && ObjectIdentifier.equalsWithValue(entityInhibitAttributesWithFabInfo.getEntityInhibitAttributes().getEntities().get(0).getObjectID(), strProcessOperationInfo.getAsgnEqpID())
                                            && CimStringUtils.equals(entityInhibitAttributesWithFabInfo.getEntityInhibitAttributes().getEntities().get(1).getClassName(), BizConstant.SP_INHIBITCLASSID_MACHINERECIPE)
                                            && ObjectIdentifier.equalsWithValue(entityInhibitAttributesWithFabInfo.getEntityInhibitAttributes().getEntities().get(1).getObjectID(), strProcessOperationInfo.getAsgnRecipeID())) {
                                        findFlag = true;
                                        break;
                                    }
                                }
                            }
                            if (!findFlag){
                                log.info("findFlag == FALSE");
                                entity_countForOtherFab++;
                                Infos.EntityInhibitAttributesWithFabInfo entityInhibitAttributesWithFabInfo = new Infos.EntityInhibitAttributesWithFabInfo();
                                entityInhibitAttributesWithFabInfo.setFabID(correspondingFabID);
                                entityInhibitAttributesWithFabInfos.add(entityInhibitAttributesWithFabInfo);
                                Infos.EntityInhibitAttributes entityInhibitAttributes = new Infos.EntityInhibitAttributes();
                                entityInhibitAttributesWithFabInfo.setEntityInhibitAttributes(entityInhibitAttributes);
                                List<Infos.EntityIdentifier> entities = new ArrayList<>();
                                entityInhibitAttributes.setEntities(entities);
                                Infos.EntityIdentifier entityIdentifier1 = new Infos.EntityIdentifier();
                                entities.add(entityIdentifier1);
                                entityIdentifier1.setClassName(BizConstant.SP_INHIBITCLASSID_EQUIPMENT);
                                entityIdentifier1.setAttribution("");
                                entityIdentifier1.setObjectID(strProcessOperationInfo.getAsgnEqpID());
                                Infos.EntityIdentifier entityIdentifier2 = new Infos.EntityIdentifier();
                                entities.add(entityIdentifier2);
                                entityIdentifier2.setClassName(BizConstant.SP_INHIBITCLASSID_MACHINERECIPE);
                                entityIdentifier2.setAttribution("");
                                entityIdentifier2.setObjectID(strProcessOperationInfo.getAsgnRecipeID());
                                entityInhibitAttributes.setSubLotTypes(new ArrayList<>());
                                entityInhibitAttributes.setStartTimeStamp(CimDateUtils.getTimestampAsString(objCommon.getTimeStamp().getReportTimeStamp()));
                                entityInhibitAttributes.setEndTimeStamp(BizConstant.SP_TIMESTAMP_NIL_OBJECT_STRING);
                                entityInhibitAttributes.setReasonCode(BizConstant.SP_REASON_SPCOUTOFRANGEINHIBIT);
                                entityInhibitAttributes.setOwnerID(objCommon.getUser().getUserID());
                                entityInhibitAttributes.setClaimedTimeStamp(CimDateUtils.getTimestampAsString(objCommon.getTimeStamp().getReportTimeStamp()));
                                /**
                                 * Task-311 add ocapNo for constraint memo
                                 */
                                entityInhibitAttributes.setMemo(ocapMemo);
                            }
                            findFlag = false;
                            for (int n = 0 ; n < dcActionInfo_count ; n++ ) {
                                if (CimArrayUtils.getSize(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getEntities()) == 2 ){
                                    if (CimStringUtils.equals(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getEntities().get(0).getClassName(),BizConstant.SP_INHIBITCLASSID_EQUIPMENT)
                                            && ObjectIdentifier.equalsWithValue(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getEntities().get(0).getObjectID(), strProcessOperationInfo.getAsgnEqpID())
                                            && CimStringUtils.equals(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getEntities().get(1).getClassName(),BizConstant.SP_INHIBITCLASSID_MACHINERECIPE)
                                            && ObjectIdentifier.equalsWithValue(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getEntities().get(1).getObjectID(), strProcessOperationInfo.getAsgnRecipeID())
                                            && CimStringUtils.equals(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getReasonCode(),BizConstant.SP_REASON_SPCOUTOFRANGEINHIBIT)
                                            && CimStringUtils.equals(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getCorrespondingObjRefPO(),spcCheckLot.getProcessObjrefPO())
                                            && ObjectIdentifier.equalsWithValue(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getLotID(), effectLot.getLotID())){
                                        log.info("break!!");
                                        findFlag = true;
                                        break;
                                    }
                                }
                            }
                            if (!findFlag){
                                log.info("findFlag == FALSE  [machineID]");
                                // Add ActionResultInfo
                                List<Infos.DCActionResultInfo> dcActionLotResult = dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo();
                                Infos.DCActionResultInfo  dcActionResultInfo = new Infos.DCActionResultInfo();
                                dcActionResultInfo.setLotID(effectLot.getLotID());
                                dcActionResultInfo.setMonitorLotFlag(true);
                                dcActionResultInfo.setDcDefID(spcCheckLot.getDcDefID());
                                dcActionResultInfo.setDcSpecID(spcCheckLot.getDcSpecID());
                                dcActionResultInfo.setCheckType(BizConstant.SP_ACTIONRESULT_CHECKTYPE_SPC);
                                dcActionResultInfo.setReasonCode(BizConstant.SP_REASON_SPCOUTOFRANGEINHIBIT);
                                dcActionResultInfo.setActionCode(BizConstant.SP_ACTIONCODE_EQUIPMENT_AND_RECIPEHOLD);
                                dcActionResultInfo.setCorrespondingObjRefPO(spcCheckLot.getProcessObjrefPO());
                                dcActionResultInfo.setBankID("");
                                dcActionResultInfo.setReworkRouteID("");
                                List<Infos.EntityIdentifier> entityIdentifiers = new ArrayList<>();
                                Infos.EntityIdentifier entityIdentifier1 = new Infos.EntityIdentifier();
                                entityIdentifier1.setClassName(BizConstant.SP_INHIBITCLASSID_EQUIPMENT);
                                entityIdentifier1.setAttribution("");
                                entityIdentifier1.setObjectID(strProcessOperationInfo.getAsgnEqpID());
                                entityIdentifiers.add(entityIdentifier1);
                                Infos.EntityIdentifier entityIdentifier2 = new Infos.EntityIdentifier();
                                entityIdentifier2.setClassName(BizConstant.SP_INHIBITCLASSID_MACHINERECIPE);
                                entityIdentifier2.setAttribution("");
                                entityIdentifier2.setObjectID(strProcessOperationInfo.getAsgnRecipeID());
                                entityIdentifiers.add(entityIdentifier2);
                                dcActionResultInfo.setEntities(entityIdentifiers);
                                dcActionLotResult.add(dcActionResultInfo);
                                dcActionInfo_count++;
                            }
                        }

                        if (process_and_equipment_hold_flag){
                            //-------------------------------------------------------------------
                            //  Check process_and_equipment_hold_inhibition is already registed or not.
                            //  If only not existed, entry new inhibition.
                            //-------------------------------------------------------------------
                            findFlag = false;
                            List<Infos.EntityInhibitAttributesWithFabInfo> entityInhibitAttributesWithFabInfos = strStartLot_actionList_EffectSPCCheck_out.getEntityInhibitionsWithFabInfo();
                            for (Infos.EntityInhibitAttributesWithFabInfo entityInhibitAttributesWithFabInfo : entityInhibitAttributesWithFabInfos) {
                                if (CimArrayUtils.getSize(entityInhibitAttributesWithFabInfo.getEntityInhibitAttributes().getEntities()) == 2){
                                    if (CimStringUtils.equals(entityInhibitAttributesWithFabInfo.getEntityInhibitAttributes().getEntities().get(0).getClassName(), BizConstant.SP_INHIBITCLASSID_OPERATION)
                                            && ObjectIdentifier.equalsWithValue(entityInhibitAttributesWithFabInfo.getEntityInhibitAttributes().getEntities().get(0).getObjectID().getValue(), strProcessOperationInfo.getPdID())
                                            && CimStringUtils.equals(entityInhibitAttributesWithFabInfo.getEntityInhibitAttributes().getEntities().get(1).getClassName(), BizConstant.SP_INHIBITCLASSID_EQUIPMENT)
                                            && ObjectIdentifier.equalsWithValue(entityInhibitAttributesWithFabInfo.getEntityInhibitAttributes().getEntities().get(1).getObjectID(), strProcessOperationInfo.getAsgnEqpID())) {
                                        findFlag = true;
                                        break;
                                    }
                                }
                            }
                            if (!findFlag){
                                log.info("findFlag == FALSE");
                                entity_countForOtherFab++;
                                Infos.EntityInhibitAttributesWithFabInfo entityInhibitAttributesWithFabInfo = new Infos.EntityInhibitAttributesWithFabInfo();
                                entityInhibitAttributesWithFabInfo.setFabID(correspondingFabID);
                                entityInhibitAttributesWithFabInfos.add(entityInhibitAttributesWithFabInfo);
                                Infos.EntityInhibitAttributes entityInhibitAttributes = new Infos.EntityInhibitAttributes();
                                entityInhibitAttributesWithFabInfo.setEntityInhibitAttributes(entityInhibitAttributes);
                                List<Infos.EntityIdentifier> entities = new ArrayList<>();
                                entityInhibitAttributes.setEntities(entities);
                                Infos.EntityIdentifier entityIdentifier1 = new Infos.EntityIdentifier();
                                entities.add(entityIdentifier1);
                                entityIdentifier1.setClassName(BizConstant.SP_INHIBITCLASSID_PROCESS);
                                entityIdentifier1.setAttribution("");
                                entityIdentifier1.setObjectID(strProcessOperationInfo.getPdID());
                                Infos.EntityIdentifier entityIdentifier2 = new Infos.EntityIdentifier();
                                entities.add(entityIdentifier2);
                                entityIdentifier2.setClassName(BizConstant.SP_INHIBITCLASSID_EQUIPMENT);
                                entityIdentifier2.setAttribution("");
                                entityIdentifier2.setObjectID(strProcessOperationInfo.getAsgnEqpID());
                                entityInhibitAttributes.setSubLotTypes(new ArrayList<>());
                                entityInhibitAttributes.setStartTimeStamp(CimDateUtils.getTimestampAsString(objCommon.getTimeStamp().getReportTimeStamp()));
                                entityInhibitAttributes.setEndTimeStamp(BizConstant.SP_TIMESTAMP_NIL_OBJECT_STRING);
                                entityInhibitAttributes.setReasonCode(BizConstant.SP_REASON_SPCOUTOFRANGEINHIBIT);
                                entityInhibitAttributes.setOwnerID(objCommon.getUser().getUserID());
                                entityInhibitAttributes.setClaimedTimeStamp(CimDateUtils.getTimestampAsString(objCommon.getTimeStamp().getReportTimeStamp()));
                                /**
                                 * Task-311 add ocapNo for constraint memo
                                 */
                                entityInhibitAttributes.setMemo(ocapMemo);
                            }
                            findFlag = false;
                            for (int n = 0 ; n < dcActionInfo_count ; n++ ) {
                                if (CimArrayUtils.getSize(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getEntities()) == 2){
                                    if (CimStringUtils.equals(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getEntities().get(0).getClassName(),BizConstant.SP_INHIBITCLASSID_PROCESS)
                                            && ObjectIdentifier.equalsWithValue(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getEntities().get(0).getObjectID(), strProcessOperationInfo.getPdID())
                                            && CimStringUtils.equals(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getEntities().get(1).getClassName(),BizConstant.SP_INHIBITCLASSID_EQUIPMENT)
                                            && ObjectIdentifier.equalsWithValue(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getEntities().get(1).getObjectID(), strProcessOperationInfo.getAsgnEqpID())
                                            && CimStringUtils.equals(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getReasonCode(),BizConstant.SP_REASON_SPCOUTOFRANGEINHIBIT)
                                            && CimStringUtils.equals(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getCorrespondingObjRefPO(),spcCheckLot.getProcessObjrefPO())
                                            && ObjectIdentifier.equalsWithValue(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getLotID(), effectLot.getLotID())){
                                        log.info("break!!");
                                        findFlag = TRUE;
                                        break;
                                    }
                                }
                            }
                            if (!findFlag){
                                log.info("findFlag == FALSE  [machineID]");
                                // Add ActionResultInfo
                                List<Infos.DCActionResultInfo> dcActionLotResult = dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo();
                                Infos.DCActionResultInfo  dcActionResultInfo = new Infos.DCActionResultInfo();
                                dcActionResultInfo.setLotID(effectLot.getLotID());
                                dcActionResultInfo.setMonitorLotFlag(true);
                                dcActionResultInfo.setDcDefID(spcCheckLot.getDcDefID());
                                dcActionResultInfo.setDcSpecID(spcCheckLot.getDcSpecID());
                                dcActionResultInfo.setCheckType(BizConstant.SP_ACTIONRESULT_CHECKTYPE_SPC);
                                dcActionResultInfo.setReasonCode(BizConstant.SP_REASON_SPCOUTOFRANGEINHIBIT);
                                dcActionResultInfo.setActionCode(BizConstant.SP_ACTIONCODE_PROCESS_AND_EQUIPMENTHOLD);
                                dcActionResultInfo.setCorrespondingObjRefPO(spcCheckLot.getProcessObjrefPO());
                                dcActionResultInfo.setBankID("");
                                dcActionResultInfo.setReworkRouteID("");
                                List<Infos.EntityIdentifier> entityIdentifiers = new ArrayList<>();
                                Infos.EntityIdentifier entityIdentifier1 = new Infos.EntityIdentifier();
                                entityIdentifier1.setClassName(BizConstant.SP_INHIBITCLASSID_PROCESS);
                                entityIdentifier1.setAttribution("");
                                entityIdentifier1.setObjectID(strProcessOperationInfo.getPdID());
                                entityIdentifiers.add(entityIdentifier1);
                                Infos.EntityIdentifier entityIdentifier2 = new Infos.EntityIdentifier();
                                entityIdentifier2.setClassName(BizConstant.SP_INHIBITCLASSID_EQUIPMENT);
                                entityIdentifier2.setAttribution("");
                                entityIdentifier2.setObjectID(strProcessOperationInfo.getAsgnEqpID());
                                entityIdentifiers.add(entityIdentifier2);
                                dcActionResultInfo.setEntities(entityIdentifiers);
                                dcActionLotResult.add(dcActionResultInfo);
                                dcActionInfo_count++;
                            }
                        }

                        if (chamber_hold_flag) {
                            //-------------------------------------------------------------------
                            //  Check chamber_hold_inhibition is already registed or not.
                            //  If only not existed, entry new inhibition.
                            //-------------------------------------------------------------------
                            findFlag = false;
                            List<Infos.EntityInhibitAttributesWithFabInfo> entityInhibitAttributesWithFabInfos = strStartLot_actionList_EffectSPCCheck_out.getEntityInhibitionsWithFabInfo();
                            for (Infos.EntityInhibitAttributesWithFabInfo entityInhibitAttributesWithFabInfo : entityInhibitAttributesWithFabInfos) {
                                if (CimStringUtils.equals(entityInhibitAttributesWithFabInfo.getEntityInhibitAttributes().getEntities().get(0).getClassName(), BizConstant.SP_INHIBITCLASSID_CHAMBER)
                                        && ObjectIdentifier.equalsWithValue(entityInhibitAttributesWithFabInfo.getEntityInhibitAttributes().getEntities().get(0).getObjectID(), strProcessOperationInfo.getAsgnEqpID())
                                        && ObjectIdentifier.equalsWithValue(entityInhibitAttributesWithFabInfo.getEntityInhibitAttributes().getEntities().get(0).getAttribution(), spcCheckLot.getProcessChamberID())) {
                                    findFlag = true;
                                    break;
                                }
                            }
                            if (!findFlag){
                                log.info("findFlag == FALSE");
                                entity_countForOtherFab++;
                                Infos.EntityInhibitAttributesWithFabInfo entityInhibitAttributesWithFabInfo = new Infos.EntityInhibitAttributesWithFabInfo();
                                entityInhibitAttributesWithFabInfo.setFabID(correspondingFabID);
                                entityInhibitAttributesWithFabInfos.add(entityInhibitAttributesWithFabInfo);
                                Infos.EntityInhibitAttributes entityInhibitAttributes = new Infos.EntityInhibitAttributes();
                                entityInhibitAttributesWithFabInfo.setEntityInhibitAttributes(entityInhibitAttributes);
                                List<Infos.EntityIdentifier> entities = new ArrayList<>();
                                entityInhibitAttributes.setEntities(entities);
                                Infos.EntityIdentifier entityIdentifier = new Infos.EntityIdentifier();
                                entities.add(entityIdentifier);
                                entityIdentifier.setClassName(BizConstant.SP_INHIBITCLASSID_CHAMBER);
                                entityIdentifier.setAttribution(spcCheckLot.getProcessChamberID().getValue());
                                entityIdentifier.setObjectID(strProcessOperationInfo.getAsgnEqpID());
                                entityInhibitAttributes.setSubLotTypes(new ArrayList<>());
                                entityInhibitAttributes.setStartTimeStamp(CimDateUtils.getTimestampAsString(objCommon.getTimeStamp().getReportTimeStamp()));
                                entityInhibitAttributes.setEndTimeStamp(BizConstant.SP_TIMESTAMP_NIL_OBJECT_STRING);
                                entityInhibitAttributes.setReasonCode(BizConstant.SP_REASON_SPCOUTOFRANGEINHIBIT);
                                entityInhibitAttributes.setOwnerID(objCommon.getUser().getUserID());
                                entityInhibitAttributes.setClaimedTimeStamp(CimDateUtils.getTimestampAsString(objCommon.getTimeStamp().getReportTimeStamp()));
                                /**
                                 * Task-311 add ocapNo for constraint memo
                                 */
                                entityInhibitAttributes.setMemo(ocapMemo);
                            }
                            findFlag = false;
                            for (int n = 0 ; n < dcActionInfo_count ; n++ ) {
                                if (CimArrayUtils.getSize(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getEntities()) == 1 ){
                                    if (CimStringUtils.equals(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getEntities().get(0).getClassName(),BizConstant.SP_INHIBITCLASSID_CHAMBER)
                                            && ObjectIdentifier.equalsWithValue(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getEntities().get(0).getObjectID(), strProcessOperationInfo.getAsgnEqpID())
                                            && ObjectIdentifier.equalsWithValue(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getEntities().get(0).getAttribution(), spcCheckLot.getProcessChamberID())
                                            && CimStringUtils.equals(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getReasonCode(),BizConstant.SP_REASON_SPCOUTOFRANGEINHIBIT)
                                            && CimStringUtils.equals(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getCorrespondingObjRefPO(),spcCheckLot.getProcessObjrefPO())
                                            && ObjectIdentifier.equalsWithValue(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getLotID(), effectLot.getLotID())){
                                        log.info("break!!");
                                        findFlag = true;
                                        break;
                                    }
                                }
                            }
                            if (!findFlag){
                                log.info("findFlag == FALSE  [machineID]");
                                // Add ActionResultInfo
                                List<Infos.DCActionResultInfo> dcActionLotResult = dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo();
                                Infos.DCActionResultInfo  dcActionResultInfo = new Infos.DCActionResultInfo();
                                dcActionResultInfo.setLotID(effectLot.getLotID());
                                dcActionResultInfo.setMonitorLotFlag(true);
                                dcActionResultInfo.setDcDefID(spcCheckLot.getDcDefID());
                                dcActionResultInfo.setDcSpecID(spcCheckLot.getDcSpecID());
                                dcActionResultInfo.setCheckType(BizConstant.SP_ACTIONRESULT_CHECKTYPE_SPC);
                                dcActionResultInfo.setReasonCode(BizConstant.SP_REASON_SPCOUTOFRANGEINHIBIT);
                                dcActionResultInfo.setActionCode(BizConstant.SP_ACTIONCODE_CHAMBERHOLD);
                                dcActionResultInfo.setCorrespondingObjRefPO(spcCheckLot.getProcessObjrefPO());
                                dcActionResultInfo.setBankID("");
                                dcActionResultInfo.setReworkRouteID("");
                                List<Infos.EntityIdentifier> entityIdentifiers = new ArrayList<>();
                                Infos.EntityIdentifier entityIdentifier = new Infos.EntityIdentifier();
                                entityIdentifier.setClassName(BizConstant.SP_INHIBITCLASSID_CHAMBER);
                                entityIdentifier.setAttribution(spcCheckLot.getProcessChamberID().getValue());
                                entityIdentifier.setObjectID(strProcessOperationInfo.getAsgnEqpID());
                                entityIdentifiers.add(entityIdentifier);
                                dcActionResultInfo.setEntities(entityIdentifiers);
                                dcActionLotResult.add(dcActionResultInfo);
                                dcActionInfo_count++;
                            }
                        }

                        if (chamber_recipe_hold_flag){
                            //-------------------------------------------------------------------
                            //  Check chamber_recipe_hold_inhibition is already registed or not.
                            //  If only not existed, entry new inhibition.
                            //-------------------------------------------------------------------
                            findFlag = false;
                            List<Infos.EntityInhibitAttributesWithFabInfo> entityInhibitAttributesWithFabInfos = strStartLot_actionList_EffectSPCCheck_out.getEntityInhibitionsWithFabInfo();
                            for (Infos.EntityInhibitAttributesWithFabInfo entityInhibitAttributesWithFabInfo : entityInhibitAttributesWithFabInfos) {
                                if (CimArrayUtils.getSize(entityInhibitAttributesWithFabInfo.getEntityInhibitAttributes().getEntities()) == 2){
                                    if (CimStringUtils.equals(entityInhibitAttributesWithFabInfo.getEntityInhibitAttributes().getEntities().get(0).getClassName(), BizConstant.SP_INHIBITCLASSID_CHAMBER)
                                            && ObjectIdentifier.equalsWithValue(entityInhibitAttributesWithFabInfo.getEntityInhibitAttributes().getEntities().get(0).getObjectID(), strProcessOperationInfo.getAsgnEqpID())
                                            && ObjectIdentifier.equalsWithValue(entityInhibitAttributesWithFabInfo.getEntityInhibitAttributes().getEntities().get(0).getAttribution(), spcCheckLot.getProcessChamberID())
                                            && CimStringUtils.equals(entityInhibitAttributesWithFabInfo.getEntityInhibitAttributes().getEntities().get(1).getClassName(), BizConstant.SP_INHIBITCLASSID_MACHINERECIPE)
                                            && ObjectIdentifier.equalsWithValue(entityInhibitAttributesWithFabInfo.getEntityInhibitAttributes().getEntities().get(1).getObjectID(), strProcessOperationInfo.getAsgnRecipeID())) {
                                        findFlag = true;
                                        break;
                                    }
                                }
                            }
                            if (!findFlag){
                                log.info("findFlag == FALSE");
                                entity_countForOtherFab++;
                                Infos.EntityInhibitAttributesWithFabInfo entityInhibitAttributesWithFabInfo = new Infos.EntityInhibitAttributesWithFabInfo();
                                entityInhibitAttributesWithFabInfo.setFabID(correspondingFabID);
                                entityInhibitAttributesWithFabInfos.add(entityInhibitAttributesWithFabInfo);
                                Infos.EntityInhibitAttributes entityInhibitAttributes = new Infos.EntityInhibitAttributes();
                                entityInhibitAttributesWithFabInfo.setEntityInhibitAttributes(entityInhibitAttributes);
                                List<Infos.EntityIdentifier> entities = new ArrayList<>();
                                entityInhibitAttributes.setEntities(entities);
                                Infos.EntityIdentifier entityIdentifier1 = new Infos.EntityIdentifier();
                                entities.add(entityIdentifier1);
                                entityIdentifier1.setClassName(BizConstant.SP_INHIBITCLASSID_CHAMBER);
                                entityIdentifier1.setAttribution(spcCheckLot.getProcessChamberID().getValue());
                                entityIdentifier1.setObjectID(strProcessOperationInfo.getAsgnEqpID());
                                Infos.EntityIdentifier entityIdentifier2 = new Infos.EntityIdentifier();
                                entities.add(entityIdentifier2);
                                entityIdentifier2.setClassName(BizConstant.SP_INHIBITCLASSID_MACHINERECIPE);
                                entityIdentifier2.setAttribution("");
                                entityIdentifier2.setObjectID(strProcessOperationInfo.getAsgnRecipeID());
                                entityInhibitAttributes.setSubLotTypes(new ArrayList<>());
                                entityInhibitAttributes.setStartTimeStamp(CimDateUtils.getTimestampAsString(objCommon.getTimeStamp().getReportTimeStamp()));
                                entityInhibitAttributes.setEndTimeStamp(BizConstant.SP_TIMESTAMP_NIL_OBJECT_STRING);
                                entityInhibitAttributes.setReasonCode(BizConstant.SP_REASON_SPCOUTOFRANGEINHIBIT);
                                entityInhibitAttributes.setOwnerID(objCommon.getUser().getUserID());
                                entityInhibitAttributes.setClaimedTimeStamp(CimDateUtils.getTimestampAsString(objCommon.getTimeStamp().getReportTimeStamp()));
                                /**
                                 * Task-311 add ocapNo for constraint memo
                                 */
                                entityInhibitAttributes.setMemo(ocapMemo);
                            }
                            findFlag = false;
                            for (int n = 0 ; n < dcActionInfo_count ; n++ ) {
                                if (CimArrayUtils.getSize(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getEntities()) == 2 ){
                                    if (CimStringUtils.equals(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getEntities().get(0).getClassName(),BizConstant.SP_INHIBITCLASSID_CHAMBER)
                                            && ObjectIdentifier.equalsWithValue(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getEntities().get(0).getObjectID(), strProcessOperationInfo.getAsgnEqpID())
                                            && ObjectIdentifier.equalsWithValue(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getEntities().get(0).getAttribution(), spcCheckLot.getProcessChamberID())
                                            && CimStringUtils.equals(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getEntities().get(1).getClassName(),BizConstant.SP_INHIBITCLASSID_MACHINERECIPE)
                                            && ObjectIdentifier.equalsWithValue(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getEntities().get(1).getObjectID(), strProcessOperationInfo.getAsgnRecipeID())
                                            && CimStringUtils.equals(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getReasonCode(),BizConstant.SP_REASON_SPCOUTOFRANGEINHIBIT)
                                            && CimStringUtils.equals(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getCorrespondingObjRefPO(),spcCheckLot.getProcessObjrefPO())
                                            && ObjectIdentifier.equalsWithValue(dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo().get(n).getLotID(), effectLot.getLotID())){
                                        log.info("break!!");
                                        findFlag = true;
                                        break;
                                    }
                                }
                            }
                            if (!findFlag){
                                log.info("findFlag == FALSE  [machineID]");
                                // Add ActionResultInfo
                                List<Infos.DCActionResultInfo> dcActionLotResult = dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo();
                                Infos.DCActionResultInfo dcActionResultInfo = new Infos.DCActionResultInfo();
                                dcActionResultInfo.setLotID(effectLot.getLotID());
                                dcActionResultInfo.setMonitorLotFlag(true);
                                dcActionResultInfo.setDcDefID(spcCheckLot.getDcDefID());
                                dcActionResultInfo.setDcSpecID(spcCheckLot.getDcSpecID());
                                dcActionResultInfo.setCheckType(BizConstant.SP_ACTIONRESULT_CHECKTYPE_SPC);
                                dcActionResultInfo.setReasonCode(BizConstant.SP_REASON_SPCOUTOFRANGEINHIBIT);
                                dcActionResultInfo.setActionCode(BizConstant.SP_ACTIONCODE_CHAMBERANDRECIPEHOLD);
                                dcActionResultInfo.setCorrespondingObjRefPO(spcCheckLot.getProcessObjrefPO());
                                dcActionResultInfo.setBankID("");
                                dcActionResultInfo.setReworkRouteID("");
                                List<Infos.EntityIdentifier> entityIdentifiers = new ArrayList<>();
                                Infos.EntityIdentifier entityIdentifier1 = new Infos.EntityIdentifier();
                                entityIdentifier1.setClassName(BizConstant.SP_INHIBITCLASSID_CHAMBER);
                                entityIdentifier1.setAttribution(spcCheckLot.getProcessChamberID().getValue());
                                entityIdentifier1.setObjectID(strProcessOperationInfo.getAsgnEqpID());
                                entityIdentifiers.add(entityIdentifier1);
                                Infos.EntityIdentifier entityIdentifier2 = new Infos.EntityIdentifier();
                                entityIdentifier2.setClassName(BizConstant.SP_INHIBITCLASSID_MACHINERECIPE);
                                entityIdentifier2.setAttribution("");
                                entityIdentifier2.setObjectID(strProcessOperationInfo.getAsgnRecipeID());
                                entityIdentifiers.add(entityIdentifier2);
                                dcActionResultInfo.setEntities(entityIdentifiers);
                                dcActionLotResult.add(dcActionResultInfo);
                                dcActionInfo_count++;
                            }
                        }
                        if (mail_flag){
                            findLotFlag = false;
                            findMsgFlag = false;
                            msg_countPerMonLot = 0;
                            int mailIdx = 0;
                            //  Check if strMessageAttributes is already registed with the same lotID & reasonCode or not.
                            //  If only not existed, entry new inhibition.

                            for (int n = 0; n < lot_countForMailOtherFab; n++){
                                if (ObjectIdentifier.equalsWithValue(interFabMonitorGroupActionInfoList.get(interFabMonIdx).getMonitoredLotMailInfoList().get(n).getLotID(), effectLot.getLotID())
                                        && ObjectIdentifier.equalsWithValue(interFabMonitorGroupActionInfoList.get(interFabMonIdx).getMonitoredLotMailInfoList().get(n).getRouteID(), strProcessOperationInfo.getMainPDID())
                                        && CimStringUtils.equals(interFabMonitorGroupActionInfoList.get(interFabMonIdx).getMonitoredLotMailInfoList().get(n).getOpeNo(),strProcessOperationInfo.getOpeNo())){
                                    log.info("set!! findFlag = TRUE");
                                    mailIdx = n;
                                    msg_countPerMonLot = CimArrayUtils.getSize(interFabMonitorGroupActionInfoList.get(interFabMonIdx).getMonitoredLotMailInfoList().get(n).getReasonCodeSeq());
                                    findLotFlag = true;
                                    break;
                                }
                            }
                            if (!findLotFlag){
                                log.info("findLotFlag == FALSE");
                                mailIdx = lot_countForMailOtherFab++;
                                msg_countPerMonLot = 0;
                                List<Infos.MonitoredLotMailInfo> monitoredLotMailInfoList = interFabMonitorGroupActionInfoList.get(interFabMonIdx).getMonitoredLotMailInfoList();
                                Infos.MonitoredLotMailInfo monitoredLotMailInfo = new Infos.MonitoredLotMailInfo();
                                monitoredLotMailInfoList.add(monitoredLotMailInfo);
                                monitoredLotMailInfo.setLotID(effectLot.getLotID());
                                monitoredLotMailInfo.setEquipmentID(strProcessOperationInfo.getAsgnEqpID());
                                monitoredLotMailInfo.setRouteID(strProcessOperationInfo.getMainPDID());
                                monitoredLotMailInfo.setOpeNo(strProcessOperationInfo.getOpeNo());
                                // Add ActionResultInfo
                                List<Infos.DCActionResultInfo> dcActionLotResult = dcActionLotResultList.get(dcActionLot_idx).getDcActionResultInfo();
                                Infos.DCActionResultInfo dcActionResultInfo = new Infos.DCActionResultInfo();
                                dcActionResultInfo.setLotID(effectLot.getLotID());
                                dcActionResultInfo.setMonitorLotFlag(true);
                                dcActionResultInfo.setDcDefID(spcCheckLot.getDcDefID());
                                dcActionResultInfo.setDcSpecID(spcCheckLot.getDcSpecID());
                                dcActionResultInfo.setCheckType(BizConstant.SP_ACTIONRESULT_CHECKTYPE_SPC);
                                dcActionResultInfo.setReasonCode(BizConstant.SP_REASON_SPCOUTOFRANGEINHIBIT);
                                dcActionResultInfo.setActionCode(BizConstant.SP_ACTIONCODE_MAILSEND);
                                dcActionResultInfo.setCorrespondingObjRefPO(spcCheckLot.getProcessObjrefPO());
                                dcActionResultInfo.setBankID("");
                                dcActionResultInfo.setReworkRouteID("");
                                List<Infos.EntityIdentifier> entityIdentifiers = new ArrayList<>();
                                Infos.EntityIdentifier entityIdentifier1 = new Infos.EntityIdentifier();
                                entityIdentifiers.add(entityIdentifier1);
                                dcActionResultInfo.setEntities(entityIdentifiers);
                                dcActionLotResult.add(dcActionResultInfo);
                                dcActionInfo_count++;
                            }
                            //check same message exist?
                            for (int q = 0; q < msg_countPerMonLot; q++){
                                if (CimStringUtils.equals(interFabMonitorGroupActionInfoList.get(interFabMonIdx).getMonitoredLotMailInfoList().get(mailIdx).getReasonCodeSeq().get(q),spcCheckLot.getSpcCheckResult())){
                                    findMsgFlag = true;
                                    break;
                                }
                            }
                            if (!findMsgFlag){
                                log.info("findMsgFlag == FALSE");
                                msg_countPerMonLot++;
                                Infos.MonitoredLotMailInfo monitoredLotMailInfo = interFabMonitorGroupActionInfoList.get(interFabMonIdx).getMonitoredLotMailInfoList().get(mailIdx);
                                List<ObjectIdentifier> messageIDSeq = monitoredLotMailInfo.getMessageIDSeq();
                                if (CimArrayUtils.isEmpty(messageIDSeq)){
                                    messageIDSeq = new ArrayList<>();
                                    monitoredLotMailInfo.setMessageIDSeq(messageIDSeq);
                                }
                                messageIDSeq.add(ObjectIdentifier.buildWithValue(messageID));
                                List<String> reasonCodeSeq = monitoredLotMailInfo.getReasonCodeSeq();
                                if (CimArrayUtils.isEmpty(reasonCodeSeq)){
                                    reasonCodeSeq = new ArrayList<>();
                                    monitoredLotMailInfo.setReasonCodeSeq(reasonCodeSeq);
                                }
                                reasonCodeSeq.add(spcCheckLot.getSpcCheckResult());
                            }
                        }
                    }
                }

                // Set Entity Inhibit Reason Detail Information
                // For Monitored Lot
                for(eCnt = entity_count_keep; eCnt < entity_count; eCnt++ ){
                    Infos.EntityInhibitDetailAttributes entityInhibitDetailAttributes = strStartLot_actionList_EffectSPCCheck_out.getStrEntityInhibitions().get(eCnt);
                    List<Infos.EntityInhibitReasonDetailInfo> entityInhibitReasonDetailInfos = entityInhibitDetailAttributes.getEntityInhibitReasonDetailInfos();
                    if (CimArrayUtils.isEmpty(entityInhibitReasonDetailInfos)){
                        entityInhibitReasonDetailInfos = new ArrayList<>();
                        entityInhibitDetailAttributes.setEntityInhibitReasonDetailInfos(entityInhibitReasonDetailInfos);
                    }
                    this.setEntityInhibitReasonDetailinfoFromSpcResultAndDctype(entityInhibitReasonDetailInfos, spcCheckLot.getSpcResult(), spcIFParmList.get(0).getSpcInput().getEdcType());
                    if (!CimArrayUtils.isEmpty(entityInhibitReasonDetailInfos)){
                        entityInhibitReasonDetailInfos.get(0).setRelatedLotID(spcCheckLot.getLotID().getValue());
                    }
                }

                // For Other Fab
                for(eCnt = entity_countForOtherFab_keep; eCnt < entity_countForOtherFab; eCnt++ ){
                    Infos.EntityInhibitAttributesWithFabInfo entityInhibitAttributesWithFabInfo = strStartLot_actionList_EffectSPCCheck_out.getEntityInhibitionsWithFabInfo().get(eCnt);
                    List<Infos.EntityInhibitReasonDetailInfo> entityInhibitReasonDetailInfos = entityInhibitAttributesWithFabInfo.getEntityInhibitReasonDetailInfos();
                    if (CimArrayUtils.isEmpty(entityInhibitReasonDetailInfos)){
                        entityInhibitReasonDetailInfos = new ArrayList<>();
                        entityInhibitAttributesWithFabInfo.setEntityInhibitReasonDetailInfos(entityInhibitReasonDetailInfos);
                    }
                    this.setEntityInhibitReasonDetailinfoFromSpcResultAndDctype(entityInhibitReasonDetailInfos, spcCheckLot.getSpcResult(), spcIFParmList.get(0).getSpcInput().getEdcType());
                    if (!CimArrayUtils.isEmpty(entityInhibitReasonDetailInfos)){
                        entityInhibitReasonDetailInfos.get(0).setRelatedLotID(spcCheckLot.getLotID().getValue());
                        entityInhibitReasonDetailInfos.get(0).setRelatedFabID(currentFabID);;
                    }
                }
            }
        }

        // reset requestOtherFabFlag for chart owner mail.
        requestOtherFabFlag = FALSE;
        // Set e-mail data for chart
        int nSpcOutputLen = CimArrayUtils.getSize(spcIFParmList);
        int iSpcOutputCnt,mCount = 0;
        List<String> tmpChartMailAdderess = new ArrayList<>();
        for (iSpcOutputCnt = 0; iSpcOutputCnt < nSpcOutputLen; iSpcOutputCnt++){
            // Get requestOtherFabFlag
            String corrFabID = null;
            for (int i = 0; i < iSpcOutputCnt; i++){
                if (ObjectIdentifier.equalsWithValue(spcIFParmList.get(iSpcOutputCnt).getSpcOutput().getLotID(), strSpcCheckLot.get(i).getLotID())
                        && ObjectIdentifier.equalsWithValue(spcIFParmList.get(iSpcOutputCnt).getSpcInput().getProcessProcessFlowID(), strSpcCheckLot.get(i).getProcessRouteID())
                        && CimStringUtils.equals(spcIFParmList.get(iSpcOutputCnt).getSpcInput().getProcessOperationNumber(), strSpcCheckLot.get(i).getProcessOperationNumber())){
                    requestOtherFabFlag = strSpcCheckLot.get(i).getRequestOtherFabFlag();
                    corrFabID = strSpcCheckLot.get(i).getProcessFabID();
                    break;
                }
            }
            //Temp data to Check Chart is duplicate or not
            List<String> tmpItemNam = new ArrayList<>();
            List<String> tmpChartGr = new ArrayList<>();
            List<String> tmpChartId = new ArrayList<>();
            List<Results.SpcItemResult> itemResults = spcIFParmList.get(iSpcOutputCnt).getSpcOutput().getItemResults();
            int nItem = CimArrayUtils.getSize(itemResults);
            for (int jItemCnt = 0; jItemCnt < nItem; jItemCnt++){
                List<Results.SpcChartResult> chartResults = spcIFParmList.get(iSpcOutputCnt).getSpcOutput().getItemResults().get(jItemCnt).getChartResults();
                int nChart = CimArrayUtils.getSize(chartResults);
                for (int kChartCnt = 0 ; kChartCnt < nChart; kChartCnt++){
                    // Duplicate Check
                   boolean bDupricateFlag = false;
                   int nTmpChart = tmpChartGr.size();
                   for ( int mTempChartCnt = 0; mTempChartCnt < nTmpChart; mTempChartCnt++){
                       if (CimStringUtils.equals(itemResults.get(jItemCnt).getDataItemName(), tmpItemNam.get(mTempChartCnt))
                            && CimStringUtils.equals(chartResults.get(kChartCnt).getChartGroupID(),tmpChartGr.get(mTempChartCnt))
                            && CimStringUtils.equals(chartResults.get(kChartCnt).getChartID(),tmpChartId.get(mTempChartCnt))){
                           bDupricateFlag = true;
                           break;
                       }
                   }
                   if (bDupricateFlag){
                       log.info("chart is duplicate -> continue");
                       continue;
                   }
                   int nMailOrg = tmpChartMailAdderess.size();
                   if (!CimStringUtils.isEmpty(chartResults.get(kChartCnt).getChartOwnerMailAddress())){
                       tmpChartMailAdderess.add(chartResults.get(kChartCnt).getChartOwnerMailAddress());
                       mail_send_count++;
                       tmpItemNam.add(itemResults.get(jItemCnt).getDataItemName());
                       tmpChartGr.add(itemResults.get(jItemCnt).getChartResults().get(kChartCnt).getChartGroupID());
                       tmpChartId.add(itemResults.get(jItemCnt).getChartResults().get(kChartCnt).getChartID());
                       nTmpChart ++ ;
                   }
                   int nSubOwner = CimArrayUtils.getSize(chartResults.get(kChartCnt).getChartSubOwnerMailAddresses());
                   for (int mSubOwnerCnt = 0; mSubOwnerCnt < nSubOwner; mSubOwnerCnt++ ){
                       String subOwner = chartResults.get(kChartCnt).getChartSubOwnerMailAddresses().get(mSubOwnerCnt);
                       if (!CimStringUtils.isEmpty(subOwner)){
                           tmpChartMailAdderess.add(subOwner);
                           mail_send_count++;
                           tmpItemNam.add(itemResults.get(jItemCnt).getDataItemName());
                           tmpChartGr.add(chartResults.get(kChartCnt).getChartGroupID());
                           tmpChartId.add(chartResults.get(kChartCnt).getChartID());
                           nTmpChart ++ ;
                       }
                   }
                   if (nMailOrg == mail_send_count){
                       log.info( "Not Found MailAddress");
                       continue;
                   }
                   // Set Process ChamberID
                   StringBuilder tmpChamberID = new StringBuilder();
                   int nTmpChamber = 0;
                   List<Infos.WaferIDByChamber> waferIDByChambers = spcIFParmList.get(iSpcOutputCnt).getSpcInput().getWaferIDByChambers();
                   int nWaferChamber = CimArrayUtils.getSize(waferIDByChambers);
                   for (int mWaferChamberCnt = 0; mWaferChamberCnt < nWaferChamber; mWaferChamberCnt++ ){
                       if (CimArrayUtils.isEmpty(waferIDByChambers.get(mWaferChamberCnt).getWaferIDs())){
                           continue;
                       }
                       //Max ChamberID to write out is 6
                       if (nTmpChamber == 6 ){
                           tmpChamberID.append("...");
                           break;
                       }
                       if (nTmpChamber > 0){
                           tmpChamberID.append(",");
                       }
                       tmpChamberID.append(waferIDByChambers.get(mWaferChamberCnt).getProcChamber());
                       nTmpChamber ++;
                   }

                   // Set e-mail Message
                   Outputs.SpcOutput spcOutput = spcIFParmList.get(iSpcOutputCnt).getSpcOutput();
                   Inputs.SpcInput spcInput = spcIFParmList.get(iSpcOutputCnt).getSpcInput();
                   StringBuilder tmpMsg = new StringBuilder();
                   tmpMsg.append("SPC Violation Notice <").append(spcOutput.getLotID().getValue()).append("> \n\n");
                   tmpMsg.append("Violation Timestamp               : ").append(CimDateUtils.getTimestampAsString(objCommon.getTimeStamp().getReportTimeStamp())).append(" \n");
                   tmpMsg.append("SPC Result                        : ").append(spcOutput.getLotRC()).append(" \n");
                   tmpMsg.append("Action Taken                      : ").append(BizConstant.SP_ACTIONCODE_MAILSEND).append(" \n");
                   tmpMsg.append("Lot ID                            : ").append(ObjectIdentifier.fetchValue(spcOutput.getLotID())).append(" \n");
                   tmpMsg.append("Product ID                        : ").append(ObjectIdentifier.fetchValue(spcInput.getProductID())).append(" \n");
                   tmpMsg.append("Route ID                          : ").append(ObjectIdentifier.fetchValue(spcInput.getProcessProcessFlowID())).append(" \n");
                   tmpMsg.append("Operation Number                  : ").append(spcInput.getProcessOperationNumber()).append(" \n");
                   tmpMsg.append("Process Equipment ID              : ").append(ObjectIdentifier.fetchValue(spcInput.getProcessEquipmentID())).append(" \n");
                   tmpMsg.append("Process Chamber ID                : ").append(tmpChamberID.toString()).append(" \n");
                   tmpMsg.append("Process Recipe ID                 : ").append(ObjectIdentifier.fetchValue(spcInput.getProcessRecipeID()))   .append(" \n");
                   tmpMsg.append("Chart Group ID                    : ").append(chartResults.get(kChartCnt).getChartGroupID()).append(" \n");
                   tmpMsg.append("Chart Type                        : ").append(chartResults.get(kChartCnt).getChartType()).append(" \n");
                   tmpMsg.append("Chart ID                          : ").append(chartResults.get(kChartCnt).getChartID()).append(" \n");
                   tmpMsg.append("Item Name                         : ").append(itemResults.get(jItemCnt).getDataItemName());

                   int mMailOrgCnt;
                   for (mMailOrgCnt = nMailOrg; mMailOrgCnt < mail_send_count; mMailOrgCnt++){
                       if (!requestOtherFabFlag){
                           log.info("correspondingPO is current Fab");
                           List<Infos.MailSend> mailSendList = strStartLot_actionList_EffectSPCCheck_out.getMailSendList();
                           Infos.MailSend mailSend = new Infos.MailSend();
                           mailSendList.add(mailSend);
                           mailSend.setChartMailAddress(tmpChartMailAdderess.get(mMailOrgCnt));
                           Infos.MessageAttributes messageAttributes = new Infos.MessageAttributes();
                           mailSend.setMessageAttributes(messageAttributes);
                           messageAttributes.setMessageID(ObjectIdentifier.buildWithValue(BizConstant.SP_MESSAGEID_SPCCHARTOVER));
                           messageAttributes.setLotID(spcOutput.getLotID());
                           messageAttributes.setLotStatus("");
                           messageAttributes.setEquipmentID(spcInput.getProcessEquipmentID());
                           messageAttributes.setRouteID(spcInput.getProcessProcessFlowID());
                           messageAttributes.setOperationNumber(spcInput.getProcessOperationNumber());
                           messageAttributes.setReasonCode("");
                           messageAttributes.setMessageText(tmpMsg.toString());
                       }else {
                           log.info("correspondingPO is other Fab. MailSend request to other Fab");
                           List<Infos.MailSendWithFabInfo> mailSendListWithFabInfo = strStartLot_actionList_EffectSPCCheck_out.getMailSendListWithFabInfo();
                           Infos.MailSendWithFabInfo mailSendWithFabInfo = new Infos.MailSendWithFabInfo();
                           mailSendListWithFabInfo.add(mailSendWithFabInfo);
                           mailSendWithFabInfo.setFabID(corrFabID);
                           Infos.MailSend mailSend = new Infos.MailSend();
                           mailSendWithFabInfo.setStrMailSend(mailSend);
                           Infos.MessageAttributes messageAttributes = new Infos.MessageAttributes();
                           mailSend.setMessageAttributes(messageAttributes);
                           messageAttributes.setMessageID(ObjectIdentifier.buildWithValue(BizConstant.SP_MESSAGEID_SPCCHARTOVER));
                           messageAttributes.setLotID(spcOutput.getLotID());
                           messageAttributes.setLotStatus("");
                           messageAttributes.setEquipmentID(spcInput.getProcessEquipmentID());
                           messageAttributes.setRouteID(spcInput.getProcessProcessFlowID());
                           messageAttributes.setOperationNumber(spcInput.getProcessOperationNumber());
                           messageAttributes.setReasonCode("");
                           messageAttributes.setMessageText(tmpMsg.toString());
                       }
                   }
               }
            }
        }
        strStartLot_actionList_EffectSPCCheck_out.setInterFabMonitorGroupActionInfoSequence(interFabMonitorGroupActionInfoList);
        strStartLot_actionList_EffectSPCCheck_out.setDcActionLotResult(dcActionLotResultList);
        return strStartLot_actionList_EffectSPCCheck_out;
    }

    private void setEntityInhibitReasonDetailinfoFromSpcResultAndDctype(List<Infos.EntityInhibitReasonDetailInfo> entityInhibitReasonDetailInfos, Infos.SpcResult spcResult, String collectionType){
        Infos.EntityInhibitReasonDetailInfo entityInhibitReasonDetailInfo = null;
        if (CimArrayUtils.isEmpty(entityInhibitReasonDetailInfos)){
            entityInhibitReasonDetailInfos = new ArrayList<>();
            entityInhibitReasonDetailInfo = new Infos.EntityInhibitReasonDetailInfo();
            entityInhibitReasonDetailInfos.add(entityInhibitReasonDetailInfo);
        } else {
            entityInhibitReasonDetailInfo = entityInhibitReasonDetailInfos.get(0);
        }
        List<Infos.EntityInhibitSpcChartInfo> strEntityInhibitSpcChartInfos = entityInhibitReasonDetailInfo.getStrEntityInhibitSpcChartInfos();
        if (CimArrayUtils.isEmpty(strEntityInhibitSpcChartInfos)){
            strEntityInhibitSpcChartInfos = new ArrayList<>();
            entityInhibitReasonDetailInfo.setStrEntityInhibitSpcChartInfos(strEntityInhibitSpcChartInfos);
        }
        List<Infos.SpcDcItemAndChart> strSpcDcItems = spcResult.getSpcDcItem();
        for (Infos.SpcDcItemAndChart strSpcDcItem : strSpcDcItems){
            List<Infos.SpcChart> spcChartList = strSpcDcItem.getSpcChartList();
            if (!CimArrayUtils.isEmpty(spcChartList)){
                for (Infos.SpcChart spcChart : spcChartList){
                    Infos.EntityInhibitSpcChartInfo entityInhibitSpcChartInfo = new Infos.EntityInhibitSpcChartInfo();
                    strEntityInhibitSpcChartInfos.add(entityInhibitSpcChartInfo);
                    entityInhibitSpcChartInfo.setRelatedSpcDcType(collectionType);
                    entityInhibitSpcChartInfo.setRelatedSpcChartGroupID(spcChart.getChartGroupID());
                    entityInhibitSpcChartInfo.setRelatedSpcChartID(spcChart.getChartID());
                    entityInhibitSpcChartInfo.setRelatedSpcChartType(spcChart.getChartType());
                }
            }
        }
    }
}
