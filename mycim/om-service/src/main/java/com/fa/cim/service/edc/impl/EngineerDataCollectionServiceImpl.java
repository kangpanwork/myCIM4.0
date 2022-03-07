package com.fa.cim.service.edc.impl;

import com.fa.cim.annotaion.OmService;
import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.constant.CIMStateConst;
import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.OmCode;
import com.fa.cim.common.support.User;
import com.fa.cim.common.utils.*;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.config.RetCodeConfigEx;
import com.fa.cim.dto.*;
import com.fa.cim.method.*;
import com.fa.cim.newcore.bo.durable.CimCassette;
import com.fa.cim.newcore.bo.machine.CimMachine;
import com.fa.cim.newcore.bo.pd.CimProcessOperation;
import com.fa.cim.newcore.bo.product.CimLot;
import com.fa.cim.newcore.bo.product.CimMonitorGroup;
import com.fa.cim.newcore.dto.pd.ProcessDTO;
import com.fa.cim.newcore.factory.BaseCoreFactory;
import com.fa.cim.newcore.impl.bo.env.StandardProperties;
import com.fa.cim.service.bank.IBankService;
import com.fa.cim.service.cjpj.IControlJobProcessJobService;
import com.fa.cim.service.constraint.IConstraintService;
import com.fa.cim.service.edc.IEngineerDataCollectionService;
import com.fa.cim.service.lot.ILotService;
import com.fa.cim.service.post.IPostService;
import com.fa.cim.service.pr.IPilotRunInqService;
import com.fa.cim.service.pr.IPilotRunService;
import com.fa.cim.service.processcontrol.IProcessControlService;
import com.rits.cloning.Cloner;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.stream.Collectors;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

/**
 * description:
 * <p>EngineerDataCollectionService .<br/></p>
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
public class EngineerDataCollectionServiceImpl implements IEngineerDataCollectionService {

    @Autowired
    private RetCodeConfig retCodeConfig;

    @Autowired
    private RetCodeConfigEx retCodeConfigEx;

    @Autowired
    private IControlJobMethod controlJobMethod;

    @Autowired
    private IVirtualOperationMethod virtualOperationMethod;

    @Autowired
    private ILotMethod lotMethod;

    @Autowired
    private IProcessMethod processMethod;

    @Autowired
    private IDataValueMethod dataValueMethod;

    @Autowired
    private ISpecCheckMethod specCheckMethod;

    @Autowired
    private IBondingGroupMethod bondingGroupMethod;

    @Autowired
    private IObjectLockMethod objectLockMethod;

    @Autowired
    private IPortMethod portMethod;

    @Autowired
    private IStartLotMethod startLotMethod;

    @Autowired
    private IConstraintService constraintService;

    @Autowired
    private IEquipmentMethod equipmentMethod;

    @Autowired
    private ISPCMethod spcMethod;

    //todo
    @Autowired
    private ILotService lotService;

    //todo
    @Autowired
    private IControlJobProcessJobService controlJobProcessJobService;

    @Autowired
    private IProcessControlService processControlService;

    @Autowired
    private IMessageMethod messageMethod;

    @Autowired
    private IMonitorGroupMethod monitorGroupMethod;

    @Autowired
    private IPilotRunMethod pilotRunMethod;

    @Autowired
    private IBankService bankService;

    @Autowired
    private Cloner cloner;

    @Autowired
    private IDataCollectionMethod dataCollectionMethod;

    @Autowired
    private IConstraintMethod constraintMethod;

    @Autowired
    private IPilotRunInqService pilotRunInqService;

    @Autowired
    private IPilotRunService pilotRunService;

    @Autowired
    private IPostService postService;

    @Autowired
    private ILogicalRecipeMethod logicalRecipeMethod;

    @Autowired
    private BaseCoreFactory baseCoreFactory;

    @Autowired
    private IFPCMethod docMethod;

    @Getter
    @Setter
    private static class TmpStartCassetteIndex {
        private List<TmpLotInCassetteIndex> lotInCassetteIndexList;
        private int lotInCassetteLen;
    }

    @Getter
    @Setter
    private static class TmpLotInCassetteIndex {
        private List<List<Integer>> specCheckIndexList;
        private int specCheckIndexLen;
    }

    @Override
    public Results.EDCWithSpecCheckActionReqResult sxEDCWithSpecCheckActionReq(Infos.ObjCommon strObjCommonIn, Infos.EDCWithSpecCheckActionReqInParm strEDCWithSpecCheckActionReqInParm, String claimMemo) {
        log.info(" txEDCWithSpecCheckActionReq ");

        int i = 0, j = 0, k = 0, l = 0;
        int nCastLen = 0;
        int nLotCastLen = 0;
        int nDCDefLen = 0;
        int nDCitemLen = 0;
        List<Infos.StartCassette> tmpStartCassette;

        if (0 < CimArrayUtils.getSize(strEDCWithSpecCheckActionReqInParm.getStrStartCassette())) {
            log.info("" + "There is StartCassette length of inparmeter.");
            tmpStartCassette = strEDCWithSpecCheckActionReqInParm.getStrStartCassette();
        } else {
            log.info("" + "There is no StartCassette length of inparmeter. getTry() to get StartCassette with controlJob.");

            Outputs.ObjControlJobStartReserveInformationOut strControlJobStartReserveInformationGetOut = controlJobMethod.controlJobStartReserveInformationGet(strObjCommonIn,
                    strEDCWithSpecCheckActionReqInParm.getControlJobID(), true);

            tmpStartCassette = strControlJobStartReserveInformationGetOut.getStartCassetteList();
        }

        Validations.check(0 >= CimArrayUtils.getSize(tmpStartCassette), retCodeConfig.getNotFoundCassette());

        log.info("" + "Object Lock Process....");
        //--------------------------------//
        //   Lock Machine object          //
        //--------------------------------//
        objectLockMethod.objectLock(strObjCommonIn, CimMachine.class, strEDCWithSpecCheckActionReqInParm.getEquipmentID());
        nCastLen = CimArrayUtils.getSize(tmpStartCassette);
        log.info("" + "tmpStartCassette.getLength()" + nCastLen);

        for (i = 0; i < nCastLen; i++) {
            //--------------------------//
            //   Lock Cassette Object   //
            //--------------------------//
            objectLockMethod.objectLock(strObjCommonIn, CimCassette.class, tmpStartCassette.get(i).getCassetteID());
            nLotCastLen = CimArrayUtils.getSize(tmpStartCassette.get(i).getLotInCassetteList());
            log.info("" + "tmpStartCassette.get(i).getStrLotInCassette().getLength()" + nLotCastLen);

            for (j = 0; j < nLotCastLen; j++) {

                if (!Objects.equals(tmpStartCassette.get(i).getLotInCassetteList().get(j).getMoveInFlag(), TRUE)) {
                    log.info("" + "tmpStartCassette.get(i).getStrLotInCassette().get(j).getOperationStartFlag() == FALSE");
                    continue;
                }
                //---------------------//
                //   Lock Lot Object   //
                //---------------------//
                objectLockMethod.objectLock(strObjCommonIn, CimLot.class, tmpStartCassette.get(i).getLotInCassetteList().get(j).getLotID());

            }
        }

        boolean virtualOperationFlag;
        boolean strVirtualOperationCheckByStartCassetteOut;

        strVirtualOperationCheckByStartCassetteOut = virtualOperationMethod.virtualOperationCheckByStartCassette(strObjCommonIn, tmpStartCassette);

        log.info("" + "virtualOperationFlag = " + strVirtualOperationCheckByStartCassetteOut);
        virtualOperationFlag = strVirtualOperationCheckByStartCassetteOut;

        Outputs.ObjPortResourceCurrentOperationModeGetOut strPortResourceCurrentOperationModeGetOut = new Outputs.ObjPortResourceCurrentOperationModeGetOut();
        strPortResourceCurrentOperationModeGetOut.init();
        if (TransactionIDEnum.TEMP_DATA_RPT.equals(strObjCommonIn.getTransactionID())
                || TransactionIDEnum.COLLECTED_DATA_ACTION_REQ.equals(strObjCommonIn.getTransactionID())) {
            log.info("" + "OperationMode is set to Auto forcedly.");
            strPortResourceCurrentOperationModeGetOut.getOperationMode().setMoveOutMode(BizConstant.SP_EQP_COMPMODE_AUTO);
        } else {
            if (!virtualOperationFlag) {
                log.info("" + "Check whether OperationMode is Auto or other.");

                strPortResourceCurrentOperationModeGetOut = portMethod.portResourceCurrentOperationModeGet(strObjCommonIn,
                        strEDCWithSpecCheckActionReqInParm.getEquipmentID(),
                        tmpStartCassette.get(0).getUnloadPortID());
            }
        }

        Boolean bPJDataExistFlag = FALSE;
        TmpStartCassetteIndex[] strStartCassetteIndexSeq = null;
        int strStartCassetteIndexLen = 0;

        Infos.StartCassette[] strExpandStartCassetteSeq;

        if (CimStringUtils.equals(strPortResourceCurrentOperationModeGetOut.getOperationMode().getMoveOutMode(), BizConstant.SP_EQP_COMPMODE_AUTO)) {
            log.info("" + "CIMFWStrCmp(strPortResourceCurrentOperationModeGetOut.getStrOperationMode().getOperationCompMode()+ SP_EQP_COMPMODE_AUTO ) == 0");

            Boolean bExistAsteriskItemFlag = FALSE;
            nCastLen = CimArrayUtils.getSize(tmpStartCassette);
            strExpandStartCassetteSeq = new Infos.StartCassette[nCastLen];
            for (i = 0; i < nCastLen; i++) {
                log.info("" + "i" + i);

                nLotCastLen = CimArrayUtils.getSize(tmpStartCassette.get(i).getLotInCassetteList());
                log.info("" + "tmpStartCassette.get(i).getStrLotInCassette().getLength()" + nLotCastLen);
                strExpandStartCassetteSeq[i] = new Infos.StartCassette();
                strExpandStartCassetteSeq[i].setLotInCassetteList(new ArrayList<>());

                for (j = 0; j < nLotCastLen; j++) {
                    log.info("" + "j" + j);

                    log.info("" + "operationStartFlag " + tmpStartCassette.get(i).getLotInCassetteList().get(j).getMoveInFlag());
                    if (!TRUE.equals(tmpStartCassette.get(i).getLotInCassetteList().get(j).getMoveInFlag())) {
                        continue;
                    }

                    log.info("" + "calling processOperationDataConditionGetDR()");
                    Outputs.ObjProcessOperationDataConditionGetDrOut strProcessOperationDataConditionGetDROut = processMethod.processOperationDataConditionGetDR(
                            strObjCommonIn,
                            tmpStartCassette.get(i).getLotInCassetteList().get(j).getLotID(),
                            BizConstant.SP_DATACONDITIONCATEGORY_SPECCHECKRESULT);

                    if (!bPJDataExistFlag && 0 < strProcessOperationDataConditionGetDROut.getCount()) {
                        log.info("" + "0 < strProcessOperationDataConditionGetDROut.getCount()");
                        bPJDataExistFlag = TRUE;
                    }
                    log.info("" + "bPJDataExistFlag" + bPJDataExistFlag);

                    List<Infos.DataCollectionInfo> strInitDCDefSeq = tmpStartCassette.get(i).getLotInCassetteList().get(j).getStartRecipe().getDcDefList();
                    nDCDefLen = CimArrayUtils.getSize(strInitDCDefSeq);
                    strExpandStartCassetteSeq[i].setLotInCassetteList(new ArrayList<>());

                    log.info("" + "calling processOperationDataConditionGetDR()");
                    strProcessOperationDataConditionGetDROut = processMethod.processOperationDataConditionGetDR(
                            strObjCommonIn,
                            tmpStartCassette.get(i).getLotInCassetteList().get(j).getLotID(),
                            BizConstant.SP_DATACONDITIONCATEGORY_EXPANDDERIVEDDATA);

                    List<Infos.DataCollectionInfo> strExpandDerivedDCDef = strProcessOperationDataConditionGetDROut.getExpandDerivedDCDefList();
                    int nExpandDCDefLen = CimArrayUtils.getSize(strExpandDerivedDCDef);
                    log.info("" + "nExpandDCDefLen" + nExpandDCDefLen);

                    for (int nDCDefCnt = 0; nDCDefCnt < nDCDefLen; nDCDefCnt++) {
                        log.info("" + "nDCDefCnt" + nDCDefCnt);
                        for (int nExpandDCDefNum = 0; nExpandDCDefNum < nExpandDCDefLen; nExpandDCDefNum++) {
                            log.info("" + "nExpandDCDefNum" + nExpandDCDefNum);
                            if (CimStringUtils.equals(strInitDCDefSeq.get(nDCDefCnt).getDataCollectionDefinitionID().getValue(), strExpandDerivedDCDef.get(nExpandDCDefNum).getDataCollectionDefinitionID().getValue())) {
                                log.info("" + "strInitDCDefSeq.get(nDCDefCnt).getDataCollectionDefinitionID() == strExpandDerivedDCDef.get(nExpandDCDefNum).getDataCollectionDefinitionID()");
                                strExpandStartCassetteSeq[i].getLotInCassetteList().get(j).getStartRecipe().getDcDefList().get(nDCDefCnt).setDcItems(strExpandDerivedDCDef.get(nExpandDCDefNum).getDcItems());
                                break;
                            }
                        }
                    }
                    if (!bExistAsteriskItemFlag && 0 < strProcessOperationDataConditionGetDROut.getCount()) {
                        log.info("" + "0 < strProcessOperationDataConditionGetDROut.getCount()");
                        bExistAsteriskItemFlag = TRUE;
                    }
                    log.info("" + "bExistAsteriskItemFlag" + bExistAsteriskItemFlag);
                }
            }

            if (bPJDataExistFlag || bExistAsteriskItemFlag) {
                log.info("" + "TRUE == bPJDataExistFlag || TRUE == bExistAsteriskItemFlag");

                nCastLen = CimArrayUtils.getSize(tmpStartCassette);
                log.info("" + "nCastLen" + nCastLen);
                strStartCassetteIndexLen = nCastLen;
                strStartCassetteIndexSeq = new TmpStartCassetteIndex[strStartCassetteIndexLen];
                for (i = 0; i < nCastLen; i++) {
                    log.info("" + "i" + i);
                    nLotCastLen = CimArrayUtils.getSize(tmpStartCassette.get(i).getLotInCassetteList());
                    log.info("" + "tmpStartCassette.get(i).getArrayUtils().getSize(strLotInCassette) " + nLotCastLen);
                    strStartCassetteIndexSeq[i].setLotInCassetteLen(nLotCastLen);
                    if (0 < strStartCassetteIndexSeq[i].getLotInCassetteLen()) {
                        log.info("" + "0 < strStartCassetteIndexSeq.get(i).getLotInCassetteLen()");
                        strStartCassetteIndexSeq[i].setLotInCassetteIndexList(
                                new ArrayList<>(strStartCassetteIndexSeq[i].getLotInCassetteLen()));
                    }
                    for (j = 0; j < nLotCastLen; j++) {
                        log.info("" + "j" + j);
                        strStartCassetteIndexSeq[i].getLotInCassetteIndexList().get(j).setSpecCheckIndexLen(0);
                        if (!TRUE.equals(tmpStartCassette.get(i).getLotInCassetteList().get(j).getMoveInFlag())) {
                            continue;
                        }

                        nDCDefLen = CimArrayUtils.getSize(tmpStartCassette.get(i).getLotInCassetteList().get(j).getStartRecipe().getDcDefList());
                        log.info("" + "nDCDefLen " + nDCDefLen);
                        strStartCassetteIndexSeq[i].getLotInCassetteIndexList().get(j).setSpecCheckIndexLen(nDCDefLen);
                        if (0 < strStartCassetteIndexSeq[i].getLotInCassetteIndexList().get(j).getSpecCheckIndexLen()) {
                            log.info("" + "0 < strStartCassetteIndexSeq.get(i).getLotInCassetteSeq().get(j).getSpecCheckIndexLen()");
                            strStartCassetteIndexSeq[i].getLotInCassetteIndexList().get(j).setSpecCheckIndexList(new ArrayList<>(
                                    strStartCassetteIndexSeq[i].getLotInCassetteIndexList().get(j).getSpecCheckIndexLen()));
                        }
                        for (k = 0; k < nDCDefLen; k++) {
                            log.info("" + "k " + k);
                            strStartCassetteIndexSeq[i].setLotInCassetteIndexList(new ArrayList<>());

                            Infos.DataCollectionInfo strTmpDCDef = tmpStartCassette.get(i).getLotInCassetteList().get(j).getStartRecipe().getDcDefList().get(k);
                            if (0 == CimStringUtils.length(strTmpDCDef.getDataCollectionSpecificationID().getValue())) {
                                log.info("" + "dataCollectionSpecificationID length is 0");
                                continue;
                            }
                            log.info("" + "strTmpDCDef.getDataCollectionDefinitionID() " + strTmpDCDef.getDataCollectionDefinitionID().getValue());

                            int nExpandDCItemLen = CimArrayUtils.getSize(strExpandStartCassetteSeq[i].getLotInCassetteList().get(j).getStartRecipe().getDcDefList().get(k).getDcItems());
                            log.info("" + "nExpandDCItemLen " + nExpandDCItemLen);

                            nDCitemLen = CimArrayUtils.getSize(strTmpDCDef.getDcItems());
                            log.info("" + "nDCitemLen " + nDCitemLen);
                            int nChkd = 0;

                            Infos.DataCollectionItemInfo[] strNewDCItemSeq;
                            strNewDCItemSeq = new Infos.DataCollectionItemInfo[nDCitemLen + nExpandDCItemLen];

                            int nDCItemCnt = 0;
                            int nExpandDCItemCurrPos = 0;
                            String expandCurrDCItemName = "";
                            String expandCurrMeasType = "";
                            String expandCurrSitePos = "";
                            for (l = 0; l < nDCitemLen; l++) {
                                log.info("" + "l " + l);
                                Boolean bCheckedFlag = FALSE;

                                log.info("" + "strTmpDCDef.getStrDCItem().get(l).getMeasurementType()" + strTmpDCDef.getDcItems().get(l).getMeasurementType());
                                if (CimStringUtils.equals(BizConstant.SP_DCDEF_MEAS_PJ, strTmpDCDef.getDcItems().get(l).getMeasurementType())
                                        || CimStringUtils.equals(BizConstant.SP_DCDEF_MEAS_PJWAFER, strTmpDCDef.getDcItems().get(l).getMeasurementType())
                                        || CimStringUtils.equals(BizConstant.SP_DCDEF_MEAS_PJWAFERSITE, strTmpDCDef.getDcItems().get(l).getMeasurementType())) {
                                    log.info("" + "DCItem by PJ");

                                    if (0 < nExpandDCItemLen
                                            && (CimStringUtils.equals(BizConstant.SP_DCDEF_ITEM_DERIVED, strTmpDCDef.getDcItems().get(l).getItemType())
                                            || CimStringUtils.equals(BizConstant.SP_DCDEF_ITEM_USERFUNCTION, strTmpDCDef.getDcItems().get(l).getItemType()))) {
                                        log.info("" + "0 < nExpandDCItemLen and itemType == Derived");

                                        if (0 == CimStringUtils.length(expandCurrDCItemName)) {
                                            log.info("" + "0 == StringUtils.length( expandCurrDCItemName )");
                                            expandCurrDCItemName = strTmpDCDef.getDcItems().get(l).getDataCollectionItemName();
                                            expandCurrMeasType = strTmpDCDef.getDcItems().get(l).getMeasurementType();
                                            expandCurrSitePos = strTmpDCDef.getDcItems().get(l).getSitePosition();
                                        }

                                        if (CimStringUtils.equals(expandCurrDCItemName, strTmpDCDef.getDcItems().get(l).getDataCollectionItemName())
                                                && CimStringUtils.equals(expandCurrMeasType, strTmpDCDef.getDcItems().get(l).getMeasurementType())
                                                && CimStringUtils.equals(expandCurrSitePos, strTmpDCDef.getDcItems().get(l).getSitePosition())) {
                                            log.info("" + "Start of the expansion");

                                            int nExpandDCItemCnt = 0;
                                            for (nExpandDCItemCnt = nExpandDCItemCurrPos; nExpandDCItemCnt < nExpandDCItemLen; nExpandDCItemCnt++) {
                                                log.info("" + "nExpandDCItemLen/nExpandDCItemCnt" + nExpandDCItemLen + nExpandDCItemCnt);

                                                if (CimStringUtils.equals(expandCurrDCItemName, strExpandStartCassetteSeq[i].getLotInCassetteList().get(j).getStartRecipe().getDcDefList().get(k).getDcItems().get(nExpandDCItemCnt).getDataCollectionItemName())
                                                        && CimStringUtils.equals(expandCurrMeasType, strExpandStartCassetteSeq[i].getLotInCassetteList().get(j).getStartRecipe().getDcDefList().get(k).getDcItems().get(nExpandDCItemCnt).getMeasurementType())
                                                        && CimStringUtils.equals(expandCurrSitePos, strExpandStartCassetteSeq[i].getLotInCassetteList().get(j).getStartRecipe().getDcDefList().get(k).getDcItems().get(nExpandDCItemCnt).getSitePosition())) {
                                                    log.info("" + "Add Expand Item" + expandCurrDCItemName + expandCurrMeasType + expandCurrSitePos);
                                                    strNewDCItemSeq[nDCItemCnt] = strExpandStartCassetteSeq[i].getLotInCassetteList().get(j).getStartRecipe().getDcDefList().get(k).getDcItems().get(nExpandDCItemCnt);
                                                    strStartCassetteIndexSeq[i].getLotInCassetteIndexList().get(j).getSpecCheckIndexList().get(k).set(nChkd, nDCItemCnt);
                                                    nChkd++;
                                                    nDCItemCnt++;
                                                } else {
                                                    log.info("" + "End of the expansion");
                                                    expandCurrDCItemName = strExpandStartCassetteSeq[i].getLotInCassetteList().get(j).getStartRecipe().getDcDefList().get(k).getDcItems().get(nExpandDCItemCnt).getDataCollectionItemName();
                                                    expandCurrMeasType = strExpandStartCassetteSeq[i].getLotInCassetteList().get(j).getStartRecipe().getDcDefList().get(k).getDcItems().get(nExpandDCItemCnt).getMeasurementType();
                                                    expandCurrSitePos = strExpandStartCassetteSeq[i].getLotInCassetteList().get(j).getStartRecipe().getDcDefList().get(k).getDcItems().get(nExpandDCItemCnt).getSitePosition();
                                                    break;
                                                }
                                            }
                                            nExpandDCItemCurrPos = nExpandDCItemCnt;
                                        }
                                    }

                                    if ((CimStringUtils.equals(BizConstant.SP_DCDEF_ITEM_DERIVED, strTmpDCDef.getDcItems().get(l).getItemType())
                                            || CimStringUtils.equals(BizConstant.SP_DCDEF_ITEM_USERFUNCTION, strTmpDCDef.getDcItems().get(l).getItemType()))
                                            && CimStringUtils.equals(strTmpDCDef.getDcItems().get(l).getWaferPosition(), "*")) {
                                        log.info("" + "itemType == Derived and waferPosition == '*'");
                                        continue;
                                    }
                                    strNewDCItemSeq[nDCItemCnt] = strTmpDCDef.getDcItems().get(l);

                                    log.info("" + "strTmpDCDef.getStrDCItem().get(l).getSpecCheckResult()" + strTmpDCDef.getDcItems().get(l).getSpecCheckResult());
                                    if ((CimStringUtils.equals(BizConstant.SP_SPECCHECKRESULT_OK, strTmpDCDef.getDcItems().get(l).getSpecCheckResult()))
                                            || (CimStringUtils.equals(BizConstant.SP_SPECCHECKRESULT_UPPERCONTROLLIMIT, strTmpDCDef.getDcItems().get(l).getSpecCheckResult()))
                                            || (CimStringUtils.equals(BizConstant.SP_SPECCHECKRESULT_LOWERCONTROLLIMIT, strTmpDCDef.getDcItems().get(l).getSpecCheckResult()))
                                            || (CimStringUtils.equals(BizConstant.SP_SPECCHECKRESULT_UPPERSPECLIMIT, strTmpDCDef.getDcItems().get(l).getSpecCheckResult()))
                                            || (CimStringUtils.equals(BizConstant.SP_SPECCHECKRESULT_LOWERSPECLIMIT, strTmpDCDef.getDcItems().get(l).getSpecCheckResult()))
                                            || (CimStringUtils.equals(BizConstant.SP_SPECCHECKRESULT_UPPERSCREENLIMIT, strTmpDCDef.getDcItems().get(l).getSpecCheckResult()))
                                            || (CimStringUtils.equals(BizConstant.SP_SPECCHECKRESULT_LOWERSCREENLIMIT, strTmpDCDef.getDcItems().get(l).getSpecCheckResult()))
                                            || (CimStringUtils.equals(BizConstant.SP_SPECCHECKRESULT_APCERROR, strTmpDCDef.getDcItems().get(l).getSpecCheckResult()))) {
                                        log.info("" + "Exist PJ report specCheckResult");
                                        bCheckedFlag = TRUE;
                                    }
                                } else {
                                    log.info("" + "DCItem by CJ");
                                    strNewDCItemSeq[nDCItemCnt] = strTmpDCDef.getDcItems().get(l);
                                }
                                if (!bCheckedFlag) {
                                    log.info("" + "FALSE == bCheckedFlag");
                                    strStartCassetteIndexSeq[i].getLotInCassetteIndexList().get(j).getSpecCheckIndexList().get(k).set(nChkd, nDCItemCnt);
                                    nChkd++;
                                }
                                nDCItemCnt++;
                            }
                            strTmpDCDef.setDcItems(CimArrayUtils.generateList(strNewDCItemSeq));

                            strStartCassetteIndexSeq[i].setLotInCassetteIndexList(new ArrayList<>());
                        }
                    }
                }
            }
        }

        Boolean spcCheckRequiredFlag = FALSE;

        if (CimStringUtils.equals(strPortResourceCurrentOperationModeGetOut.getOperationMode().getMoveOutMode(), BizConstant.SP_EQP_COMPMODE_AUTO)) {
            log.info("" + "CIMFWStrCmp(strPortResourceCurrentOperationModeGetOut.getStrOperationMode().getOperationCompMode()+ SP_EQP_COMPMODE_AUTO ) == 0");

            Results.SpecCheckReqResult strSpecCheckReqResult;
            strSpecCheckReqResult = this.sxSpecCheckReq(strObjCommonIn,
                    strEDCWithSpecCheckActionReqInParm.getEquipmentID(),
                    strEDCWithSpecCheckActionReqInParm.getControlJobID(),
                    tmpStartCassette);

            OmCode rc = retCodeConfig.getNoNeedSpecCheck();
            nCastLen = CimArrayUtils.getSize(strSpecCheckReqResult.getStartCassetteList());
            for (i = 0; i < nCastLen; i++) {
                nLotCastLen = CimArrayUtils.getSize(strSpecCheckReqResult.getStartCassetteList().get(i).getLotInCassetteList());
                for (j = 0; j < nLotCastLen; j++) {
                    if (!Objects.equals(strSpecCheckReqResult.getStartCassetteList().get(i).getLotInCassetteList().get(j).getMoveInFlag(), TRUE)) {
                        continue;
                    }

                    nDCDefLen = CimArrayUtils.getSize(strSpecCheckReqResult.getStartCassetteList().get(i).getLotInCassetteList().get(j).getStartRecipe().getDcDefList());
                    for (k = 0; k < nDCDefLen; k++) {
                        if (CimStringUtils.equals(strSpecCheckReqResult.getStartCassetteList().get(i).getLotInCassetteList().get(j).getStartRecipe().getDcDefList().get(k).getDataCollectionSpecificationID().getValue(), "")) {
                            continue;
                        }

                        nDCitemLen = CimArrayUtils.getSize(strSpecCheckReqResult.getStartCassetteList().get(i).getLotInCassetteList().get(j).getStartRecipe().getDcDefList().get(k).getDcItems());
                        for (l = 0; l < nDCitemLen; l++) {
                            if (!CimStringUtils.equals(strSpecCheckReqResult.getStartCassetteList().get(i).getLotInCassetteList().get(j).getStartRecipe().getDcDefList().get(k).getDcItems().get(l).getSpecCheckResult(), "*") &&
                                    !CimStringUtils.equals(strSpecCheckReqResult.getStartCassetteList().get(i).getLotInCassetteList().get(j).getStartRecipe().getDcDefList().get(k).getDcItems().get(l).getSpecCheckResult(), "")) {
                                rc = retCodeConfig.getSucc();
                                break;
                            }
                        }
                        if (rc == retCodeConfig.getSucc()) {
                            break;
                        }
                    }
                    if (rc == retCodeConfig.getSucc()) {
                        break;
                    }
                }
                if (rc == retCodeConfig.getSucc()) {
                    break;
                }
            }

            if (rc == retCodeConfig.getSucc()) {
                log.info("" + "txSpecCheckReq() == RcOk");

                Outputs.ObjControlJobStartReserveInformationOut strControlJobStartReserveInformationGetOut;
                strControlJobStartReserveInformationGetOut = controlJobMethod.controlJobStartReserveInformationGet(strObjCommonIn,
                        strEDCWithSpecCheckActionReqInParm.getControlJobID(), true);

                tmpStartCassette = strControlJobStartReserveInformationGetOut.getStartCassetteList();
                spcCheckRequiredFlag = TRUE;
            } else if (rc == retCodeConfig.getNoNeedSpecCheck()) {
                log.info("" + "txSpecCheckReq() == RcNoNeedToSpeccheck");

                Outputs.ObjControlJobStartReserveInformationOut strControlJobStartReserveInformationGetOut;
                strControlJobStartReserveInformationGetOut = controlJobMethod.controlJobStartReserveInformationGet(strObjCommonIn,
                        strEDCWithSpecCheckActionReqInParm.getControlJobID(), true);
                tmpStartCassette = strControlJobStartReserveInformationGetOut.getStartCassetteList();
            }
        } else {
            log.info("" + "OperationMode is not Auto.");

            Boolean bReplaceExistFlag = FALSE;
            nCastLen = CimArrayUtils.getSize(tmpStartCassette);
            log.info("" + "nCastLen" + nCastLen);
            for (i = 0; i < nCastLen; i++) {
                log.info("" + "i  " + i);
                nLotCastLen = CimArrayUtils.getSize(tmpStartCassette.get(i).getLotInCassetteList());
                log.info("" + "nLotCastLen  " + nLotCastLen);
                for (j = 0; j < nLotCastLen; j++) {
                    log.info("" + "j  " + j);
                    if (Objects.equals(tmpStartCassette.get(i).getLotInCassetteList().get(j).getMoveInFlag(), FALSE)) {
                        log.info("" + "operationStartFlag == FALSE");
                        continue;
                    }

                    nDCDefLen = CimArrayUtils.getSize(tmpStartCassette.get(i).getLotInCassetteList().get(j).getStartRecipe().getDcDefList());
                    log.info("" + "nDCDefLen " + nDCDefLen);
                    for (k = 0; k < nDCDefLen; k++) {
                        log.info("" + "k  " + k);
                        nDCitemLen = CimArrayUtils.getSize(tmpStartCassette.get(i).getLotInCassetteList().get(j).getStartRecipe().getDcDefList().get(k).getDcItems());
                        log.info("" + "nDCitemLen " + nDCitemLen);
                        for (l = 0; l < nDCitemLen; l++) {
                            log.info("" + "l  " + l);
                            Infos.DataCollectionItemInfo strTmpDCItem = tmpStartCassette.get(i).getLotInCassetteList().get(j).getStartRecipe().getDcDefList().get(k).getDcItems().get(l);
                            log.info("" + "strTmpDCItem.getSpecCheckResult() " + strTmpDCItem.getSpecCheckResult());
                            if (CimStringUtils.equals(BizConstant.SP_SPECCHECKRESULT_1X_OK, strTmpDCItem.getSpecCheckResult())) {
                                log.info("" + "SP_SPECCHECKRESULT_1X_OK");
                                strTmpDCItem.setSpecCheckResult(BizConstant.SP_SPECCHECKRESULT_OK);
                                bReplaceExistFlag = TRUE;
                            } else if (CimStringUtils.equals(BizConstant.SP_SPECCHECKRESULT_1X_UPPERCONTROLLIMIT, strTmpDCItem.getSpecCheckResult())) {
                                log.info("" + "SP_SPECCHECKRESULT_1X_UPPERCONTROLLIMIT");
                                strTmpDCItem.setSpecCheckResult(BizConstant.SP_SPECCHECKRESULT_UPPERCONTROLLIMIT);
                                bReplaceExistFlag = TRUE;
                            } else if (CimStringUtils.equals(BizConstant.SP_SPECCHECKRESULT_1X_LOWERCONTROLLIMIT, strTmpDCItem.getSpecCheckResult())) {
                                log.info("" + "SP_SPECCHECKRESULT_1X_LOWERCONTROLLIMIT");
                                strTmpDCItem.setSpecCheckResult(BizConstant.SP_SPECCHECKRESULT_LOWERCONTROLLIMIT);
                                bReplaceExistFlag = TRUE;
                            } else if (CimStringUtils.equals(BizConstant.SP_SPECCHECKRESULT_1X_UPPERSPECLIMIT, strTmpDCItem.getSpecCheckResult())) {
                                log.info("" + "SP_SPECCHECKRESULT_1X_UPPERSPECLIMIT");
                                strTmpDCItem.setSpecCheckResult(BizConstant.SP_SPECCHECKRESULT_UPPERSPECLIMIT);
                                bReplaceExistFlag = TRUE;
                            } else if (CimStringUtils.equals(BizConstant.SP_SPECCHECKRESULT_1X_LOWERSPECLIMIT, strTmpDCItem.getSpecCheckResult())) {
                                log.info("" + "SP_SPECCHECKRESULT_1X_LOWERSPECLIMIT");
                                strTmpDCItem.setSpecCheckResult(BizConstant.SP_SPECCHECKRESULT_LOWERSPECLIMIT);
                                bReplaceExistFlag = TRUE;
                            } else if (CimStringUtils.equals(BizConstant.SP_SPECCHECKRESULT_1X_UPPERSCREENLIMIT, strTmpDCItem.getSpecCheckResult())) {
                                log.info("" + "SP_SPECCHECKRESULT_1X_UPPERSCREENLIMIT");
                                strTmpDCItem.setSpecCheckResult(BizConstant.SP_SPECCHECKRESULT_UPPERSCREENLIMIT);
                                bReplaceExistFlag = TRUE;
                            } else if (CimStringUtils.equals(BizConstant.SP_SPECCHECKRESULT_1X_LOWERSCREENLIMIT, strTmpDCItem.getSpecCheckResult())) {
                                log.info("" + "SP_SPECCHECKRESULT_1X_LOWERSCREENLIMIT");
                                strTmpDCItem.setSpecCheckResult(BizConstant.SP_SPECCHECKRESULT_LOWERSCREENLIMIT);
                                bReplaceExistFlag = TRUE;
                            } else if (CimStringUtils.equals(BizConstant.SP_SPECCHECKRESULT_1X_APCERROR, strTmpDCItem.getSpecCheckResult())) {
                                log.info("" + "SP_SPECCHECKRESULT_1X_APCERROR");
                                strTmpDCItem.setSpecCheckResult(BizConstant.SP_SPECCHECKRESULT_APCERROR);
                                bReplaceExistFlag = TRUE;
                            } else if (CimStringUtils.equals(BizConstant.SP_SPECCHECKRESULT_1X_ASTERISK, strTmpDCItem.getSpecCheckResult())) {
                                log.info("" + "SP_SPECCHECKRESULT_1X_ASTERISK");
                                strTmpDCItem.setSpecCheckResult("*");
                                bReplaceExistFlag = TRUE;
                            } else if (CimStringUtils.equals(BizConstant.SP_SPECCHECKRESULT_1X_POUND, strTmpDCItem.getSpecCheckResult())) {
                                log.info("" + "SP_SPECCHECKRESULT_1X_POUND");
                                strTmpDCItem.setSpecCheckResult("#");
                                bReplaceExistFlag = TRUE;
                            } else {

                                log.info("" + "Not replace specCheckResult");
                            }
                        }
                    }
                }
            }

            if (bReplaceExistFlag) {
                log.info("" + "calling processOperationTempDataSet()");
                processMethod.processOperationTempDataSet(
                        strObjCommonIn,
                        strEDCWithSpecCheckActionReqInParm.getControlJobID(),
                        tmpStartCassette);

            }
            nCastLen = CimArrayUtils.getSize(tmpStartCassette);

            for (i = 0; i < nCastLen; i++) {
                nLotCastLen = CimArrayUtils.getSize(tmpStartCassette.get(i).getLotInCassetteList());
                for (j = 0; j < nLotCastLen; j++) {
                    if (Objects.equals(tmpStartCassette.get(i).getLotInCassetteList().get(j).getMoveInFlag(), FALSE)) {
                        continue;
                    }

                    nDCDefLen = CimArrayUtils.getSize(tmpStartCassette.get(i).getLotInCassetteList().get(j).getStartRecipe().getDcDefList());
                    for (k = 0; k < nDCDefLen; k++) {
                        if (Objects.equals(tmpStartCassette.get(i).getLotInCassetteList().get(j).getStartRecipe().getDcDefList().get(k).getSpecCheckRequiredFlag(), TRUE)) {
                            spcCheckRequiredFlag = TRUE;
                            break;
                        }
                    }

                    if (spcCheckRequiredFlag) {
                        break;
                    }
                }

                if (spcCheckRequiredFlag) {
                    break;
                }
            }
        }

        Outputs.ObjBondingGroupInfoByEqpGetDROut strBondingGroupInfoByEqpGetDROut;

        log.info("" + "calling bondingGroupInfoByEqpGetDR()");
        strBondingGroupInfoByEqpGetDROut = bondingGroupMethod.bondingGroupInfoByEqpGetDR(
                strObjCommonIn,
                strEDCWithSpecCheckActionReqInParm.getEquipmentID(),
                strEDCWithSpecCheckActionReqInParm.getControlJobID(),
                TRUE);

        int topLotLen = CimArrayUtils.getSize(strBondingGroupInfoByEqpGetDROut.getTopLotIDSeq());
        log.info("" + "topLotLen" + topLotLen);

        if (topLotLen > 0) {
            log.info("" + "There is Top Lot.");

            nCastLen = CimArrayUtils.getSize(tmpStartCassette);

            for (i = 0; i < nCastLen; i++) {
                nLotCastLen = CimArrayUtils.getSize(tmpStartCassette.get(i).getLotInCassetteList());
                for (j = 0; j < nLotCastLen; j++) {
                    if (!Objects.equals(tmpStartCassette.get(i).getLotInCassetteList().get(j).getMoveInFlag(), TRUE)) {
                        continue;
                    }

                    Boolean isTopLot = FALSE;
                    for (int topLotCnt = 0; topLotCnt < topLotLen; topLotCnt++) {
                        if (CimStringUtils.equals(tmpStartCassette.get(i).getLotInCassetteList().get(j).getLotID().getValue(),
                                strBondingGroupInfoByEqpGetDROut.getTopLotIDSeq().get(topLotCnt).getValue())) {
                            log.info("" + "isTopLot" + topLotCnt);
                            isTopLot = TRUE;
                            break;
                        }
                    }
                    if (isTopLot) {
                        log.info("" + "Lot is Top Lot. getClearing() Action." + tmpStartCassette.get(i).getLotInCassetteList().get(j).getLotID().getValue());
                        List<Infos.DataCollectionInfo> strDCDef = tmpStartCassette.get(i).getLotInCassetteList().get(j).getStartRecipe().getDcDefList();
                        int defLen = CimArrayUtils.getSize(strDCDef);
                        for (int defCnt = 0; defCnt < defLen; defCnt++) {

                            int itmLen = CimArrayUtils.getSize(strDCDef.get(defCnt).getDcItems());
                            for (int itmCnt = 0; itmCnt < itmLen; itmCnt++) {
                                strDCDef.get(defCnt).getDcItems().get(itmCnt).setSpecCheckResult("");
                            }
                        }
                    }
                }
            }
        }

        log.info("" + "spcCheckRequiredFlag is " + spcCheckRequiredFlag);

        Results.SPCCheckReqResult strSPCCheckReqResult = new Results.SPCCheckReqResult();
        Outputs.ObjSPCMgrSendSPCCheckReqOut strSPCMgrSendSPCCheckReqOut = new Outputs.ObjSPCMgrSendSPCCheckReqOut();
        if (spcCheckRequiredFlag) {

            try {
                strSPCMgrSendSPCCheckReqOut = spcMethod.spcMgrSendSPCCheckReq(
                        strObjCommonIn,
                        strEDCWithSpecCheckActionReqInParm.getEquipmentID(),
                        strEDCWithSpecCheckActionReqInParm.getControlJobID(),
                        tmpStartCassette);
            } catch (ServiceException ex) {
                if (!Validations.isEquals(retCodeConfigEx.getNoNeedToSpcCheck(), ex.getCode())) {
                    throw ex;
                }
            }
            if (strSPCMgrSendSPCCheckReqOut != null && strSPCMgrSendSPCCheckReqOut.getSpcCheckReqResult() != null) {
                strSPCCheckReqResult = strSPCMgrSendSPCCheckReqOut.getSpcCheckReqResult();
            }
        }

        List<Infos.StartCassette> tmpStartCassetteNewAction;
        tmpStartCassetteNewAction = tmpStartCassette;

        if (bPJDataExistFlag) {
            log.info("" + "TRUE == bPJDataExistFlag");

            for (int nStartCassetteNum = 0; nStartCassetteNum < strStartCassetteIndexLen; nStartCassetteNum++) {
                log.info("" + "nStartCassetteNum" + nStartCassetteNum);
                for (int nLotInCassetteNum = 0; nLotInCassetteNum < strStartCassetteIndexSeq[nStartCassetteNum].getLotInCassetteLen(); nLotInCassetteNum++) {
                    log.info("" + "nLotInCassetteNum" + nLotInCassetteNum);
                    for (int dcDefIndexNum = 0; dcDefIndexNum < strStartCassetteIndexSeq[nStartCassetteNum].getLotInCassetteIndexList().get(nLotInCassetteNum).getSpecCheckIndexLen(); dcDefIndexNum++) {
                        log.info("" + "dcDefIndexNum" + dcDefIndexNum);
                        List<Integer> specCheckIndex = strStartCassetteIndexSeq[nStartCassetteNum].getLotInCassetteIndexList().get(nLotInCassetteNum).getSpecCheckIndexList().get(dcDefIndexNum);
                        int specCheckIndexCnt = CimArrayUtils.getSize(specCheckIndex);
                        List<Infos.DataCollectionItemInfo> strSourceDCItemSeq = tmpStartCassette.get(nStartCassetteNum).getLotInCassetteList().get(nLotInCassetteNum).getStartRecipe().getDcDefList().get(dcDefIndexNum).getDcItems();
                        List<Infos.DataCollectionItemInfo> strTargetDCItemSeq = tmpStartCassetteNewAction.get(nStartCassetteNum).getLotInCassetteList().get(nLotInCassetteNum).getStartRecipe().getDcDefList().get(dcDefIndexNum).getDcItems();
                        // strTargetDCItemSeq.getLength()(specCheckIndexCnt);
                        for (int dcItemIndexNum = 0; dcItemIndexNum < specCheckIndexCnt; dcItemIndexNum++) {
                            log.info("" + "dcItemIndexNum" + dcItemIndexNum);
                            int sourceIndex = specCheckIndex.get(dcItemIndexNum);
                            log.info("" + "sourceIndex" + sourceIndex);
                            if (sourceIndex >= CimArrayUtils.getSize(strSourceDCItemSeq)) {
                                log.info("" + "index >= ArrayUtils.getSize(strDCItem)");
                                Validations.check(true, retCodeConfig.getSystemError());
                            }
                            strTargetDCItemSeq.set(dcItemIndexNum, strSourceDCItemSeq.get(sourceIndex));
                        }
                    }
                }
            }
        }
        Outputs.ObjStartLotActionListEffectSpecCheckOut strStartLotActionListEffectSpecCheckOut = new Outputs.ObjStartLotActionListEffectSpecCheckOut();

        List<Infos.InterFabMonitorGroupActionInfo> strInterFabMonitorGroupActionInfoSequence = new ArrayList<>();
        List<Results.DCActionLotResult> strDCActionLotResult = new ArrayList<>();
        strStartLotActionListEffectSpecCheckOut = startLotMethod.startLotActionListEffectSpecCheck(strStartLotActionListEffectSpecCheckOut, strObjCommonIn,
                tmpStartCassetteNewAction, strEDCWithSpecCheckActionReqInParm.getEquipmentID(),
                strInterFabMonitorGroupActionInfoSequence,
                strDCActionLotResult);

        int nCheckLen = CimArrayUtils.getSize(strStartLotActionListEffectSpecCheckOut.getEntityInhibitions());
        log.info("" + "ArrayUtils.getSize(strStartLotActionListEffectSpecCheckOut.getStrEntityInhibitions())" + nCheckLen);

        Infos.EntityInhibitDetailInfo strMfgRestrictReqResult;

        for (i = 0; i < nCheckLen; i++) {

            Params.MfgRestrictReqParams mfgRestrictReqParams = new Params.MfgRestrictReqParams();
            mfgRestrictReqParams.setEntityInhibitDetailAttributes(strStartLotActionListEffectSpecCheckOut.getEntityInhibitions().get(i));
            mfgRestrictReqParams.setClaimMemo(claimMemo);
            try {
//                strMfgRestrictReqResult = constraintService.sxMfgRestrictReq(mfgRestrictReqParams,strObjCommonIn);

                //Step5 - txMfgRestrictReq__110
                Params.MfgRestrictReq_110Params mfgRestrictReq_110Params = new Params.MfgRestrictReq_110Params();
                List<Infos.EntityInhibitDetailAttributes> entityInhibitDetailAttributeList = new ArrayList<>();
                entityInhibitDetailAttributeList.add(strStartLotActionListEffectSpecCheckOut.getEntityInhibitions().get(i));
                mfgRestrictReq_110Params.setClaimMemo(claimMemo);
                mfgRestrictReq_110Params.setUser(strObjCommonIn.getUser());
                mfgRestrictReq_110Params.setEntityInhibitDetailAttributeList(entityInhibitDetailAttributeList);
                constraintService.sxMfgRestrictReq_110(mfgRestrictReq_110Params, strObjCommonIn);
            } catch (ServiceException ex) {
                if (!Validations.isEquals(retCodeConfig.getDuplicateInhibit(), ex.getCode())) {
                    throw ex;
                }
            }

        }

        int nMsgLen = CimArrayUtils.getSize(strStartLotActionListEffectSpecCheckOut.getMessageList());
        log.info("" + "ArrayUtils.getSize(strStartLotActionListEffectSpecCheckOut.getStrMessageList())" + nMsgLen);

        for (i = 0; i < nMsgLen; i++) {
            messageMethod.messageDistributionMgrPutMessage(strObjCommonIn,
                    strStartLotActionListEffectSpecCheckOut.getMessageList().get(i).getMessageID(),
                    strStartLotActionListEffectSpecCheckOut.getMessageList().get(i).getLotID(),
                    strStartLotActionListEffectSpecCheckOut.getMessageList().get(i).getLotStatus(),
                    strStartLotActionListEffectSpecCheckOut.getMessageList().get(i).getEquipmentID(),
                    strStartLotActionListEffectSpecCheckOut.getMessageList().get(i).getRouteID(),
                    strStartLotActionListEffectSpecCheckOut.getMessageList().get(i).getOperationNumber(),
                    strStartLotActionListEffectSpecCheckOut.getMessageList().get(i).getReasonCode(),
                    strStartLotActionListEffectSpecCheckOut.getMessageList().get(i).getMessageText());
        }

        Infos.MfgRestrictRequestForMultiFabIn strMfgRestrictRequestForMultiFabIn = new Infos.MfgRestrictRequestForMultiFabIn();
        strMfgRestrictRequestForMultiFabIn.setStrEntityInhibitionsWithFabInfo(
                strStartLotActionListEffectSpecCheckOut.getEntityInhibitionsWithFabInfoList());
        strMfgRestrictRequestForMultiFabIn.setClaimMemo("");

        constraintMethod.constraintRequestForMultiFab(strObjCommonIn,
                strMfgRestrictRequestForMultiFabIn.getStrEntityInhibitionsWithFabInfo(), strMfgRestrictRequestForMultiFabIn.getClaimMemo());

        Outputs.ObjStartLotActionListEffectSPCCheckOut strStartLotActionListEffectSPCCheckOut;
        strStartLotActionListEffectSPCCheckOut = startLotMethod.startLotActionListEffectSPCCheck(strObjCommonIn,
                strSPCCheckReqResult.getSpcCheckLot(),
                strSPCMgrSendSPCCheckReqOut.getSpcIFParmList(),
                strStartLotActionListEffectSpecCheckOut.getInterFabMonitorGroupActionInfoList(),
                strEDCWithSpecCheckActionReqInParm.getEquipmentID(),
                strStartLotActionListEffectSpecCheckOut.getDcActionLotResultList());

        nCheckLen = 0;
        nCheckLen = CimArrayUtils.getSize(strStartLotActionListEffectSPCCheckOut.getStrEntityInhibitions());
        log.info("" + "ArrayUtils.getSize(strStartLotActionListEffectSPCCheckOut.getStrEntityInhibitions())" + nCheckLen);

        Infos.EntityInhibitDetailInfo strSpcMfgRestrictReqResult;

        for (i = 0; i < nCheckLen; i++) {

            Params.MfgRestrictReqParams mfgRestrictReqParams = new Params.MfgRestrictReqParams();
            mfgRestrictReqParams.setEntityInhibitDetailAttributes(strStartLotActionListEffectSPCCheckOut.getStrEntityInhibitions().get(i));
            mfgRestrictReqParams.setClaimMemo(claimMemo);
            try {
//                strSpcMfgRestrictReqResult = constraintService.sxMfgRestrictReq( mfgRestrictReqParams,strObjCommonIn);

                //Step5 - txMfgRestrictReq__110
                Params.MfgRestrictReq_110Params mfgRestrictReq_110Params = new Params.MfgRestrictReq_110Params();
                List<Infos.EntityInhibitDetailAttributes> entityInhibitDetailAttributeList = new ArrayList<>();
                entityInhibitDetailAttributeList.add(strStartLotActionListEffectSPCCheckOut.getStrEntityInhibitions().get(i));
                mfgRestrictReq_110Params.setClaimMemo(claimMemo);
                mfgRestrictReq_110Params.setUser(strObjCommonIn.getUser());
                mfgRestrictReq_110Params.setEntityInhibitDetailAttributeList(entityInhibitDetailAttributeList);
                constraintService.sxMfgRestrictReq_110(mfgRestrictReq_110Params, strObjCommonIn);
            } catch (ServiceException ex) {
                if (!Validations.isEquals(retCodeConfig.getDuplicateInhibit(), ex.getCode())) {
                    throw ex;
                }
            }

        }

        nMsgLen = 0;
        nMsgLen = CimArrayUtils.getSize(strStartLotActionListEffectSPCCheckOut.getStrMessageList());
        log.info("" + "ArrayUtils.getSize(strStartLotActionListEffectSPCCheckOut.getStrMessageList())" + nMsgLen);

        for (i = 0; i < nMsgLen; i++) {
            log.info("" + "messageText" + strStartLotActionListEffectSPCCheckOut.getStrMessageList().get(i).getMessageText());

            messageMethod.messageDistributionMgrPutMessage(strObjCommonIn,
                    strStartLotActionListEffectSPCCheckOut.getStrMessageList().get(i).getMessageID(),
                    strStartLotActionListEffectSPCCheckOut.getStrMessageList().get(i).getLotID(),
                    strStartLotActionListEffectSPCCheckOut.getStrMessageList().get(i).getLotStatus(),
                    strStartLotActionListEffectSPCCheckOut.getStrMessageList().get(i).getEquipmentID(),
                    strStartLotActionListEffectSPCCheckOut.getStrMessageList().get(i).getRouteID(),
                    strStartLotActionListEffectSPCCheckOut.getStrMessageList().get(i).getOperationNumber(),
                    strStartLotActionListEffectSPCCheckOut.getStrMessageList().get(i).getReasonCode(),
                    strStartLotActionListEffectSPCCheckOut.getStrMessageList().get(i).getMessageText());
        }

        Infos.MfgRestrictRequestForMultiFabIn strMfgRestrictRequestForMultiFabInForSPC = new Infos.MfgRestrictRequestForMultiFabIn();
        strMfgRestrictRequestForMultiFabInForSPC.setStrEntityInhibitionsWithFabInfo(
                strStartLotActionListEffectSPCCheckOut.getEntityInhibitionsWithFabInfo());
        strMfgRestrictRequestForMultiFabInForSPC.setClaimMemo("");

        constraintMethod.constraintRequestForMultiFab(strObjCommonIn,
                strMfgRestrictRequestForMultiFabInForSPC.getStrEntityInhibitionsWithFabInfo(), strMfgRestrictRequestForMultiFabInForSPC.getClaimMemo());

        Infos.SystemMessageRequestForMultiFabIn strSystemMessageRequestForMultiFabIn;
        Infos.MessageAttributesWithFabInfo[] tmpMessageAttributesWithFabInfoSeq;
        int mailLen = CimArrayUtils.getSize(strStartLotActionListEffectSPCCheckOut.getMailSendListWithFabInfo());
        tmpMessageAttributesWithFabInfoSeq = new Infos.MessageAttributesWithFabInfo[1];
        for (i = 0; i < mailLen; i++) {
            tmpMessageAttributesWithFabInfoSeq[0].setFabID(strStartLotActionListEffectSPCCheckOut.getMailSendListWithFabInfo().get(i).getFabID());
            tmpMessageAttributesWithFabInfoSeq[0].setStrMessageAttributes(strStartLotActionListEffectSPCCheckOut.getMailSendListWithFabInfo().get(i).getStrMailSend().getMessageAttributes());
            Infos.ObjCommon tmpObjCommonIn = new Infos.ObjCommon();
            tmpObjCommonIn.setUser(new User());
            tmpObjCommonIn.getUser().setUserID(ObjectIdentifier.buildWithValue(
                    strStartLotActionListEffectSPCCheckOut.getMailSendListWithFabInfo().get(i).getStrMailSend().getChartMailAddress()
            ));

            strSystemMessageRequestForMultiFabIn = new Infos.SystemMessageRequestForMultiFabIn();
            strSystemMessageRequestForMultiFabIn.setStrMessageListWithFabInfo(CimArrayUtils.generateList(tmpMessageAttributesWithFabInfoSeq));
            messageMethod.systemMessageRequestForMultiFab(strObjCommonIn,
                    strSystemMessageRequestForMultiFabIn);
        }

        Outputs.ObjLotHoldRecordEffectSpecCheckResultOut strLotHoldRecordEffectSpecCheckResultOut = new Outputs.ObjLotHoldRecordEffectSpecCheckResultOut();
        strLotHoldRecordEffectSpecCheckResultOut = lotMethod.lotHoldRecordEffectSpecCheckResult(strLotHoldRecordEffectSpecCheckResultOut, strObjCommonIn,
                tmpStartCassette,
                strStartLotActionListEffectSpecCheckOut.getInterFabMonitorGroupActionInfoList(),
                strStartLotActionListEffectSpecCheckOut.getDcActionLotResultList());

        int nSpecLotHoldEffectListLen = 0;
        nSpecLotHoldEffectListLen = CimArrayUtils.getSize(strLotHoldRecordEffectSpecCheckResultOut.getLotHoldEffectList());
        if (nSpecLotHoldEffectListLen > 0) {
            log.info("" + "ArrayUtils.getSize(strLotHoldRecordEffectSpecCheckResultOut.getStrLotHoldEffectList())" + nSpecLotHoldEffectListLen);

            Infos.LotHoldReq[] strLotHoldReqList;
            strLotHoldReqList = new Infos.LotHoldReq[1];

            for (i = 0; i < nSpecLotHoldEffectListLen; i++) {
                strLotHoldReqList[0] = new Infos.LotHoldReq();
                strLotHoldReqList[0].setHoldType(strLotHoldRecordEffectSpecCheckResultOut.getLotHoldEffectList().get(i).getHoldType());
                strLotHoldReqList[0].setHoldReasonCodeID(strLotHoldRecordEffectSpecCheckResultOut.getLotHoldEffectList().get(i).getReasonCodeID());
                strLotHoldReqList[0].setHoldUserID(strLotHoldRecordEffectSpecCheckResultOut.getLotHoldEffectList().get(i).getUserID());
                strLotHoldReqList[0].setResponsibleOperationMark(strLotHoldRecordEffectSpecCheckResultOut.getLotHoldEffectList().get(i).getResponsibleOperationMark());
                strLotHoldReqList[0].setRouteID(strLotHoldRecordEffectSpecCheckResultOut.getLotHoldEffectList().get(i).getRouteID());
                strLotHoldReqList[0].setOperationNumber(strLotHoldRecordEffectSpecCheckResultOut.getLotHoldEffectList().get(i).getOperationNumber());
                strLotHoldReqList[0].setRelatedLotID(strLotHoldRecordEffectSpecCheckResultOut.getLotHoldEffectList().get(i).getRelatedLotID());
                strLotHoldReqList[0].setClaimMemo(strLotHoldRecordEffectSpecCheckResultOut.getLotHoldEffectList().get(i).getClaimMemo());

                lotService.sxHoldLotReq(strObjCommonIn, strLotHoldRecordEffectSpecCheckResultOut.getLotHoldEffectList().get(i).getLotID(), CimArrayUtils.generateList(strLotHoldReqList));
            }
        }

        int nSpecFutureHoldEffectLen = 0;
        nSpecFutureHoldEffectLen = CimArrayUtils.getSize(strLotHoldRecordEffectSpecCheckResultOut.getFutureHoldEffectList());
        if (nSpecFutureHoldEffectLen > 0) {
            log.info("" + "ArrayUtils.getSize(strLotHoldRecordEffectSpecCheckResultOut.getStrFutureHoldEffectList())" + nSpecFutureHoldEffectLen);

            for (i = 0; i < nSpecFutureHoldEffectLen; i++) {
                Params.FutureHoldReqParams params = new Params.FutureHoldReqParams();
                params.setHoldType(strLotHoldRecordEffectSpecCheckResultOut.getFutureHoldEffectList().get(i).getHoldType());
                params.setLotID(strLotHoldRecordEffectSpecCheckResultOut.getFutureHoldEffectList().get(i).getLotID());
                params.setRouteID(strLotHoldRecordEffectSpecCheckResultOut.getFutureHoldEffectList().get(i).getRouteID());
                params.setOperationNumber(strLotHoldRecordEffectSpecCheckResultOut.getFutureHoldEffectList().get(i).getOperationNumber());
                params.setReasonCodeID(strLotHoldRecordEffectSpecCheckResultOut.getFutureHoldEffectList().get(i).getReasonCodeID());
                params.setRelatedLotID(strLotHoldRecordEffectSpecCheckResultOut.getFutureHoldEffectList().get(i).getRelatedLotID());
                params.setPostFlag(TRUE);
                params.setSingleTriggerFlag(TRUE);
                params.setClaimMemo(strLotHoldRecordEffectSpecCheckResultOut.getFutureHoldEffectList().get(i).getClaimMemo());
                try {
                    processControlService.sxFutureHoldReq(strObjCommonIn,
                            params);
                } catch (ServiceException ex) {
                    if (!Validations.isEquals(retCodeConfig.getDuplicateFtholdEntry(), ex.getCode())) {
                        throw ex;
                    }
                }
            }
        }

        Outputs.ObjLotHoldRecordEffectSPCCheckResultOut strLotHoldRecordEffectSPCCheckResultOut;
        strLotHoldRecordEffectSPCCheckResultOut = lotMethod.lotHoldRecordEffectSPCCheckResult(strObjCommonIn,
                strSPCCheckReqResult.getSpcCheckLot(),
                strLotHoldRecordEffectSpecCheckResultOut.getInterFabMonitorGroupActionInfoList(),
                strLotHoldRecordEffectSpecCheckResultOut.getDcActionLotResultList());

        int nSPCLotHoldEffectListLen = 0;
        nSPCLotHoldEffectListLen = CimArrayUtils.getSize(strLotHoldRecordEffectSPCCheckResultOut.getLotHoldEffectList());
        if (nSPCLotHoldEffectListLen > 0) {
            log.info("" + "ArrayUtils.getSize(strLotHoldRecordEffectSPCCheckResultOut.getStrLotHoldEffectList())" + nSPCLotHoldEffectListLen);

            List<Infos.LotHoldReq> strLotHoldReqList;
            strLotHoldReqList = new ArrayList<>(1);
            strLotHoldReqList.add(new Infos.LotHoldReq());
            for (i = 0; i < nSPCLotHoldEffectListLen; i++) {
                strLotHoldReqList.get(0).setHoldType(strLotHoldRecordEffectSPCCheckResultOut.getLotHoldEffectList().get(i).getHoldType());
                strLotHoldReqList.get(0).setHoldReasonCodeID(strLotHoldRecordEffectSPCCheckResultOut.getLotHoldEffectList().get(i).getReasonCodeID());
                strLotHoldReqList.get(0).setHoldUserID(strLotHoldRecordEffectSPCCheckResultOut.getLotHoldEffectList().get(i).getUserID());
                strLotHoldReqList.get(0).setResponsibleOperationMark(strLotHoldRecordEffectSPCCheckResultOut.getLotHoldEffectList().get(i).getResponsibleOperationMark());
                strLotHoldReqList.get(0).setRouteID(strLotHoldRecordEffectSPCCheckResultOut.getLotHoldEffectList().get(i).getRouteID());
                strLotHoldReqList.get(0).setOperationNumber(strLotHoldRecordEffectSPCCheckResultOut.getLotHoldEffectList().get(i).getOperationNumber());
                strLotHoldReqList.get(0).setRelatedLotID(strLotHoldRecordEffectSPCCheckResultOut.getLotHoldEffectList().get(i).getRelatedLotID());
                strLotHoldReqList.get(0).setClaimMemo(strLotHoldRecordEffectSPCCheckResultOut.getLotHoldEffectList().get(i).getClaimMemo());

                try {
                    lotService.sxHoldLotReq(strObjCommonIn, strLotHoldRecordEffectSPCCheckResultOut.getLotHoldEffectList().get(i).getLotID(), strLotHoldReqList);
                } catch (ServiceException ex) {
                    if (!Validations.isEquals(retCodeConfig.getExistSameHold(), ex.getCode())) {
                        throw ex;
                    }
                }
            }
        }

        int nSPCFutureHoldEffectLen = 0;
        nSPCFutureHoldEffectLen = CimArrayUtils.getSize(strLotHoldRecordEffectSPCCheckResultOut.getFutureHoldEffectList());
        if (nSPCFutureHoldEffectLen > 0) {
            log.info("" + "ArrayUtils.getSize(strLotHoldRecordEffectSPCCheckResultOut.getStrFutureHoldEffectList())" + nSPCFutureHoldEffectLen);

            for (i = 0; i < nSPCFutureHoldEffectLen; i++) {
                Params.FutureHoldReqParams params = new Params.FutureHoldReqParams();
                params.setHoldType(strLotHoldRecordEffectSPCCheckResultOut.getFutureHoldEffectList().get(i).getHoldType());
                params.setLotID(strLotHoldRecordEffectSPCCheckResultOut.getFutureHoldEffectList().get(i).getLotID());
                params.setRouteID(strLotHoldRecordEffectSPCCheckResultOut.getFutureHoldEffectList().get(i).getRouteID());
                params.setOperationNumber(strLotHoldRecordEffectSPCCheckResultOut.getFutureHoldEffectList().get(i).getOperationNumber());
                params.setReasonCodeID(strLotHoldRecordEffectSPCCheckResultOut.getFutureHoldEffectList().get(i).getReasonCodeID());
                params.setRelatedLotID(strLotHoldRecordEffectSPCCheckResultOut.getFutureHoldEffectList().get(i).getRelatedLotID());
                params.setPostFlag(TRUE);
                params.setSingleTriggerFlag(TRUE);
                params.setClaimMemo(strLotHoldRecordEffectSPCCheckResultOut.getFutureHoldEffectList().get(i).getClaimMemo());

                try {
                    processControlService.sxFutureHoldReq(strObjCommonIn,
                            params);
                } catch (ServiceException ex) {
                    if (!Validations.isEquals(retCodeConfig.getDuplicateFtholdEntry(), ex.getCode())) {
                        throw ex;
                    }
                }
            }
        }

        List<Infos.InterFabMonitorGroupActionInfo> tmpInterFabMonitorGroupActionInfoSequence;
        tmpInterFabMonitorGroupActionInfoSequence = strLotHoldRecordEffectSPCCheckResultOut.getInterFabMonitorGroupActionInfoList();

        nCastLen = 0;
        nCastLen = CimArrayUtils.getSize(tmpStartCassette);
        int holdReleasedLotCount = 0;
        List<ObjectIdentifier> holdReleasedLotIDs = null;

        ObjectIdentifier monitorLotID;
        for (i = 0; i < nCastLen; i++) {
            nLotCastLen = CimArrayUtils.getSize(tmpStartCassette.get(i).getLotInCassetteList());
            for (j = 0; j < nLotCastLen; j++) {

                if (!Objects.equals(tmpStartCassette.get(i).getLotInCassetteList().get(j).getMoveInFlag(), TRUE)) {
                    continue;
                }

                Infos.MonitorGroupDeleteCompOut strMonitorGroupDeleteCompOut;
                Infos.MonitorGroupDeleteCompIn strMonitorGroupDeleteCompIn = new Infos.MonitorGroupDeleteCompIn();
                strMonitorGroupDeleteCompIn.setLotID(tmpStartCassette.get(i).getLotInCassetteList().get(j).getLotID());
                strMonitorGroupDeleteCompIn.setStrInterFabMonitorGroupActionInfoSequence(tmpInterFabMonitorGroupActionInfoSequence);
                strMonitorGroupDeleteCompOut = monitorGroupMethod.monitorGroupDeleteComp100(strObjCommonIn,
                        strMonitorGroupDeleteCompIn);

                tmpInterFabMonitorGroupActionInfoSequence = strMonitorGroupDeleteCompOut.getStrInterFabMonitorGroupActionInfoSequence();

                int nMonitorCompLotsLen = CimArrayUtils.getSize(strMonitorGroupDeleteCompOut.getStrMonitoredCompLots());

                if (nMonitorCompLotsLen != 0) {

                    ObjectIdentifier aReasonCodeID = new ObjectIdentifier();
                    aReasonCodeID.setValue(BizConstant.SP_REASON_WAITINGMONITORHOLDRELEASE);

                    holdReleasedLotIDs = CimArrayUtils.expandCapacity(holdReleasedLotIDs, holdReleasedLotCount + nMonitorCompLotsLen);

                    for (k = 0; k < nMonitorCompLotsLen; k++) {
                        int nLotHoldLen = CimArrayUtils.getSize(strMonitorGroupDeleteCompOut.getStrMonitoredCompLots().get(k).getStrLotHoldReleaseReqList());
                        if (nLotHoldLen > 0) {

                            Params.HoldLotReleaseReqParams holdLotReleaseReqParams = new Params.HoldLotReleaseReqParams();
                            holdLotReleaseReqParams.setLotID(strMonitorGroupDeleteCompOut.getStrMonitoredCompLots().get(k).getProductLotID());
                            holdLotReleaseReqParams.setReleaseReasonCodeID(aReasonCodeID);
                            holdLotReleaseReqParams.setHoldReqList(strMonitorGroupDeleteCompOut.getStrMonitoredCompLots().get(k).getStrLotHoldReleaseReqList());
                            lotService.sxHoldLotReleaseReq(strObjCommonIn,
                                    holdLotReleaseReqParams);

                            holdReleasedLotIDs.set(holdReleasedLotCount, strMonitorGroupDeleteCompOut.getStrMonitoredCompLots().get(k).getProductLotID());

                            log.info("" + "Hold Released LotID" + holdReleasedLotCount + holdReleasedLotIDs.get(holdReleasedLotCount).getValue());
                            holdReleasedLotCount++;

                            if (CimStringUtils.equals(BizConstant.SP_LOT_TYPE_PRODUCTIONLOT, tmpStartCassette.get(i).getLotInCassetteList().get(j).getLotType())) {
                                while (true) {
                                    Boolean strRepeatGatePassCheckConditionOut = processMethod.repeatGatePassCheckCondition(strObjCommonIn,
                                            tmpStartCassette.get(i).getLotInCassetteList().get(j).getLotID(),
                                            strMonitorGroupDeleteCompOut.getStrMonitoredCompLots().get(k).getProductLotID());
                                    if (CimBooleanUtils.isFalse(strRepeatGatePassCheckConditionOut)) {
                                        log.info("" + "passThruRequiredFlag == FALSE");
                                        break;
                                    }

                                    Outputs.ObjLotCurrentOperationInfoGetOut strLotCurrentOperationInfoGetOut;
                                    strLotCurrentOperationInfoGetOut = lotMethod.lotCurrentOperationInfoGet(strObjCommonIn,
                                            strMonitorGroupDeleteCompOut.getStrMonitoredCompLots().get(k).getProductLotID());

                                    final Infos.GatePassLotInfo gatePassLotInfo = new Infos.GatePassLotInfo();
                                    gatePassLotInfo.setLotID(strMonitorGroupDeleteCompOut.getStrMonitoredCompLots().get(k).getProductLotID());
                                    gatePassLotInfo.setCurrentRouteID(strLotCurrentOperationInfoGetOut.getRouteID());
                                    gatePassLotInfo.setCurrentOperationNumber(strLotCurrentOperationInfoGetOut.getOperationNumber());

                                    lotService.sxPassThruReq(strObjCommonIn, gatePassLotInfo, claimMemo);
                                }
                            } else {

                                continue;
                            }
                        } else {
                            holdReleasedLotIDs = new ArrayList<>();
                        }
                    }
                }
            }
        }

        int monRelLen = CimArrayUtils.getSize(tmpInterFabMonitorGroupActionInfoSequence);
        log.info("" + "monitoringLots count" + monRelLen);
        for (i = 0; i < monRelLen; i++) {
            log.info("" + "monitorGroupRelease required flag for other fab is TRUE.");
            /*MonitorGroupReleaseRequestForMultiFabOut strMonitorGroupReleaseRequestForMultiFabOut;
            MonitorGroupReleaseRequestForMultiFabIn  strMonitorGroupReleaseRequestForMultiFabIn;
            strMonitorGroupReleaseRequestForMultiFabIn.setStrInterFabMonitorGroupActionInfo ( tmpInterFabMonitorGroupActionInfoSequence.get(i));
            rc = monitorGroupMethod.monitorGroupReleaseRequestForMultiFab( strMonitorGroupReleaseRequestForMultiFabOut,
                    strObjCommonIn,
                    strMonitorGroupReleaseRequestForMultiFabIn );*/
        }

        log.info("" + "HoldReleased Lot Count is" + holdReleasedLotCount);

        Results.EDCWithSpecCheckActionReqResult strEDCWithSpecCheckActionReqResult = new Results.EDCWithSpecCheckActionReqResult();

        strEDCWithSpecCheckActionReqResult.setHoldReleasedLotIDs(holdReleasedLotIDs);

        log.info("" + "Update PO's collected data information finally.");

        processMethod.processDataCollectionInformationUpdate(strObjCommonIn, tmpStartCassette, strLotHoldRecordEffectSPCCheckResultOut.getDcActionLotResultList());

        if (!CimStringUtils.equals(strObjCommonIn.getTransactionID(), TransactionIDEnum.PARTIAL_OPERATION_COMP_WITH_DATA_REQ.getValue())
                && !CimStringUtils.equals(strObjCommonIn.getTransactionID(), TransactionIDEnum.PARTIAL_OPERATION_COMP_FOR_INTERNAL_BUFFER_REQ.getValue())) {

            log.info("" + "Delete controlJob from Lot and Cassette.");
            Results.CJStatusChangeReqResult strCJStatusChangeReqResult;
            Infos.ControlJobCreateRequest dummyControlJobCreateRequest = null;
            Params.CJStatusChangeReqParams cjStatusChangeReqParams = new Params.CJStatusChangeReqParams();
            cjStatusChangeReqParams.setControlJobID(strEDCWithSpecCheckActionReqInParm.getControlJobID());
            cjStatusChangeReqParams.setControlJobAction(BizConstant.SP_CONTROLJOBACTION_TYPE_DELETE_FROM_LOTANDCASSETTE);
            cjStatusChangeReqParams.setControlJobCreateRequest(dummyControlJobCreateRequest);
            cjStatusChangeReqParams.setClaimMemo(claimMemo);
            strCJStatusChangeReqResult = controlJobProcessJobService.sxCJStatusChangeReqService(strObjCommonIn,
                    cjStatusChangeReqParams);

        }

        sxSPCDoActionReq(strObjCommonIn,
                strStartLotActionListEffectSPCCheckOut.getBankMoveList(),
                strStartLotActionListEffectSPCCheckOut.getMailSendList(),
                strStartLotActionListEffectSPCCheckOut.getReworkBranchList()/*,
                claimMemo*/);

        // equipmentFillInTxTRC004_101
        Results.MoveOutReqResult strEquipmentFillInTxTRC004Out = equipmentMethod.equipmentFillInTxTRC004(strObjCommonIn, tmpStartCassette, strSPCCheckReqResult.getSpcCheckLot());

        int eqpMonitorSwitch = StandardProperties.OM_AUTOMON_FLAG.getIntValue();
        if (1 == eqpMonitorSwitch) {
            log.info("" + "1 == SP_EQPMONITOR_SWITCH");
            for (int iCnt = 0; iCnt < CimArrayUtils.getSize(strEquipmentFillInTxTRC004Out.getMoveOutLot()); iCnt++) {
                log.info("" + "ArrayUtils.getSize(loop to strEquipmentFillInTxTRC004Out.getStrMoveOutReqResult().getStrOpeCompLot())" + iCnt);
                String strLotLotTypeGetOut;
                strLotLotTypeGetOut = lotMethod.lotTypeGet(
                        strObjCommonIn,
                        strEquipmentFillInTxTRC004Out.getMoveOutLot().get(iCnt).getLotID());

                if (CimStringUtils.equals(strLotLotTypeGetOut, BizConstant.SP_LOT_TYPE_EQUIPMENTMONITORLOT)
                        || CimStringUtils.equals(strLotLotTypeGetOut, BizConstant.SP_LOT_TYPE_DUMMYLOT)) {
                    log.info("" + "strLotLotTypeGetOut.getLotType() is Equipment Monitor or Dummy");
                    if ((!CimStringUtils.equals(strEquipmentFillInTxTRC004Out.getMoveOutLot().get(iCnt).getSpecificationCheckResult(), BizConstant.SP_SPECCHECKRESULT_OK)
                            && 0 < CimStringUtils.length(strEquipmentFillInTxTRC004Out.getMoveOutLot().get(iCnt).getSpecificationCheckResult())
                            && !CimStringUtils.equals(strEquipmentFillInTxTRC004Out.getMoveOutLot().get(iCnt).getSpecificationCheckResult(), BizConstant.SP_SPECCHECKRESULT_1ASTERISK))
                            || CimStringUtils.equals(strEquipmentFillInTxTRC004Out.getMoveOutLot().get(iCnt).getSpcCheckResult(), BizConstant.SP_SPCCHECK_HOLDLIMITOFF)
                            || CimStringUtils.equals(strEquipmentFillInTxTRC004Out.getMoveOutLot().get(iCnt).getSpcCheckResult(), BizConstant.SP_SPCCHECK_WARNINGLIMITOFF)) {
                        log.info("" + "Call eqpMonitorJobLotUpdate");

                        equipmentMethod.eqpMonitorJobLotUpdate(
                                strObjCommonIn,
                                strEquipmentFillInTxTRC004Out.getMoveOutLot().get(iCnt).getLotID(),
                                BizConstant.SP_EQPMONITORJOB_OPECATEGORY_SPECCHECK);
                    }
                }
            }
        }

        // add pilot run
        pilotRunMethod.checkPilotRunFail(strObjCommonIn, strEquipmentFillInTxTRC004Out.getMoveOutLot());

        strEDCWithSpecCheckActionReqResult.setMoveOutReqResult(strEquipmentFillInTxTRC004Out);
        return strEDCWithSpecCheckActionReqResult;
    }

    @Override
    public Results.SpecCheckReqResult sxSpecCheckReq(Infos.ObjCommon objCommon,
                                                     ObjectIdentifier equipmentID,
                                                     ObjectIdentifier controlJobID,
                                                     List<Infos.StartCassette> startCassetteList) {
        Results.SpecCheckReqResult retVal = new Results.SpecCheckReqResult();

        //	Step1 - Check controlJob validity between input CJ and lot actual CJ.
        log.debug("sxSpecCheckReq(): Check controlJob validity between input CJ and lot actual CJ.");
        startCassetteList = startCassetteList == null ? new ArrayList<>() : startCassetteList;
        for (Infos.StartCassette startCassette : startCassetteList) {
            List<Infos.LotInCassette> lotInCassetteList = startCassette.getLotInCassetteList();
            if (CimArrayUtils.isEmpty(lotInCassetteList)) {
                continue;
            }
            for (Infos.LotInCassette lotInCassette : lotInCassetteList) {
                //Omit Not-Start lot
                if (!CimBooleanUtils.isTrue(lotInCassette.getMoveInFlag())) {
                    continue;
                }
                // Lock lot Object
                objectLockMethod.objectLock(objCommon, CimLot.class, lotInCassette.getLotID());
                //Check Control Job validity between input controlJob and input lot actual controlJob
                ObjectIdentifier controlJobIDGetOut = lotMethod.lotControlJobIDGet(objCommon, lotInCassette.getLotID());

                if (!ObjectIdentifier.equalsWithValue(controlJobIDGetOut, controlJobID)) {
                    throw new ServiceException(retCodeConfig.getInvalidInputParam());
                }
            }
        }

        //Initialize Logic
        boolean pjDataExistFlag = false;
        Map<String, ObjectIdentifier> waferIDMap = new HashMap<>();

        for (Infos.StartCassette startCassette : startCassetteList) {
            List<Infos.LotInCassette> lotInCassetteList = startCassette.getLotInCassetteList();
            if (CimArrayUtils.isEmpty(lotInCassetteList)) {
                continue;
            }
            for (Infos.LotInCassette lotInCassette : lotInCassetteList) {
                //Omit Not-Start lot
                if (CimBooleanUtils.isFalse(lotInCassette.getMoveInFlag())) {
                    continue;
                }
                //Omit Non-DataClooection lot
                if (CimBooleanUtils.isFalse(lotInCassette.getStartRecipe().getDataCollectionFlag())) {
                    continue;
                }
                //Check Reported PJ Data Item
                //	Step2 - processOperation_DataCondition_GetDR
                Outputs.ObjProcessOperationDataConditionGetDrOut processOperationDataConditionOut =
                        processMethod.processOperationDataConditionGetDR(objCommon,
                                lotInCassette.getLotID(),
                                BizConstant.SP_DATACONDITIONCATEGORY_SPECCHECKRESULT);

                if (!pjDataExistFlag && processOperationDataConditionOut.getCount() > 0) {
                    pjDataExistFlag = true;
                }
                /*
                 lot Status should be Processing when Large-TxSpecCheckReq is called.
                 But when the other transactions call it,it is not always Processing.
                 So, checking lot Process Status is bypassed.
                 */
                if (CimStringUtils.equals(TransactionIDEnum.DATA_SPEC_CHECK_REQ.getValue(), objCommon.getTransactionID())) {
                    //Step3 - lot_processState_Get ,Get lot's Process State
                    String theLotProcessState = lotMethod.lotProcessStateGet(objCommon, lotInCassette.getLotID());
                    if (CimStringUtils.unEqual(theLotProcessState, BizConstant.SP_LOT_PROCSTATE_PROCESSING)) {
                        Validations.check(new OmCode(retCodeConfig.getInvalidLotProcessState(),
                                lotInCassette.getLotID().getValue(), theLotProcessState));
                    }
                }

                //Clear
                List<Infos.DataCollectionInfo> dcDefList = lotInCassette.getStartRecipe().getDcDefList();
                if (CimArrayUtils.isEmpty(dcDefList)) {
                    continue;
                }
                for (Infos.DataCollectionInfo dcDef : dcDefList) {
                    if (dcDefList.indexOf(dcDef) == 0 && !waferIDMap.isEmpty()) {
                        waferIDMap.clear();
                    }
                    List<Infos.DataCollectionItemInfo> dcItemList = dcDef.getDcItems();
                    if (CimArrayUtils.isEmpty(dcItemList)) {
                        continue;
                    }
                    for (Infos.DataCollectionItemInfo dcItem : dcItemList) {
                        boolean clearFlag = true;
                        boolean transactionCheck = CimArrayUtils.generateList(TransactionIDEnum.OPERATION_COMP_WITH_DATA_REQ.getValue(),
                                TransactionIDEnum.OPE_COMP_FOR_INTERNAL_BUFFER_REQ.getValue(),
                                TransactionIDEnum.PARTIAL_OPERATION_COMP_WITH_DATA_REQ.getValue(),
                                TransactionIDEnum.PARTIAL_OPERATION_COMP_FOR_INTERNAL_BUFFER_REQ.getValue(),
                                TransactionIDEnum.FORCE__OPE_COMP_REQ.getValue(),
                                TransactionIDEnum.FORCE_OPE_COMP_FOR_INTERNAL_BUFFER_REQ.getValue()).contains(objCommon.getTransactionID());
                        boolean measurementTypeCheck = CimArrayUtils.generateList(BizConstant.SP_DCDEF_MEAS_PJ,
                                BizConstant.SP_DCDEF_MEAS_PJWAFER,
                                BizConstant.SP_DCDEF_MEAS_PJWAFERSITE).contains(dcItem.getMeasurementType());
                        if (transactionCheck && measurementTypeCheck && CimStringUtils.isNotEmpty(dcItem.getSpecCheckResult())) {
                            boolean specCheckResultCheck = CimArrayUtils.generateList(
                                    BizConstant.SP_SPECCHECKRESULT_1X_OK,
                                    BizConstant.SP_SPECCHECKRESULT_1X_UPPERCONTROLLIMIT,
                                    BizConstant.SP_SPECCHECKRESULT_1X_LOWERCONTROLLIMIT,
                                    BizConstant.SP_SPECCHECKRESULT_1X_UPPERSPECLIMIT,
                                    BizConstant.SP_SPECCHECKRESULT_1X_LOWERSPECLIMIT,
                                    BizConstant.SP_SPECCHECKRESULT_1X_UPPERSCREENLIMIT,
                                    BizConstant.SP_SPECCHECKRESULT_1X_LOWERSCREENLIMIT,
                                    BizConstant.SP_SPECCHECKRESULT_1X_APCERROR,
                                    BizConstant.SP_SPECCHECKRESULT_1X_ASTERISK,
                                    BizConstant.SP_SPECCHECKRESULT_1X_POUND,
                                    BizConstant.SP_SPECCHECKRESULT_ERROR).contains(dcItem.getSpecCheckResult());
                            if (!specCheckResultCheck) {
                                clearFlag = false;
                            }
                        }
                        if (measurementTypeCheck && CimStringUtils.equals(dcItem.getWaferPosition(), "*")) {
                            dcItem.setWaferPosition("");
                        }
                        if (clearFlag) {
                            //Clear specCheckResult / actionCode
                            dcItem.setSpecCheckResult("");
                            dcItem.setActionCodes("");
                            //Clear dataValue
                            if (CimStringUtils.equals(dcItem.getItemType(), BizConstant.SP_DCDEF_ITEM_RAW)) {
                                continue;
                            }
                            dcItem.setDataValue("");
                        }
                        if (CimStringUtils.isNotEmpty(dcItem.getWaferPosition())
                                && !CimStringUtils.equals(BizConstant.SP_DCDEF_MEAS_PJ, dcItem.getMeasurementType())) {
                            if (dcDefList.indexOf(dcDef) == 0) {
                                boolean itemTypeCheck = CimStringUtils.equals(BizConstant.SP_DCDEF_ITEM_RAW, dcItem.getItemType())
                                        && !CimStringUtils.isEmpty(dcItem.getWaferID().getValue());
                                if (itemTypeCheck) {
                                    waferIDMap.putIfAbsent(dcItem.getWaferPosition(), dcItem.getWaferID());
                                }
                            } else if (CimArrayUtils.generateList(BizConstant.SP_DCDEF_ITEM_DERIVED,
                                    BizConstant.SP_DCDEF_ITEM_USERFUNCTION).contains(dcItem.getItemType())
                                    && ObjectIdentifier.isEmptyWithValue(dcItem.getWaferID())
                            ) {
                                if (waferIDMap.get(dcItem.getWaferPosition()) != null) {
                                    dcItem.setWaferID(waferIDMap.get(dcItem.getWaferPosition()));
                                }
                            }
                        }
                    }
                }
            }
        }
        // Set Initialized Data into PO
        //	Step4 - processOperation_tempData_Set
        // todo: 该方法调用逻辑需要确认，是否的确需要更新PO_EDC数据，该方法会浪费性能
        processMethod.processOperationTempDataSet(objCommon, controlJobID, startCassetteList);

        //************************************************//
        //  Set DC Spec's detailed information into PO    //
        //************************************************//
        //	Step5 - process_dataCollectionSpecification_Set
        List<Infos.StartCassette> setProcessDataCollectionSpecificationOut = processMethod.processDataCollectionSpecificationSet(objCommon, startCassetteList);
        retVal.setStartCassetteList(setProcessDataCollectionSpecificationOut);

        //************************************//
        //  Validity Check                    //
        //************************************//
        //	Step6 - dataValue_CheckValidityForSpecCheckDR
        Outputs.ObjDataValueCheckValidityForSpecCheckDrOut checkValidityForSpecCheckDROut;
        try {
            checkValidityForSpecCheckDROut = dataValueMethod.dataValueCheckValidityForSpecCheckDR(objCommon, equipmentID, controlJobID, startCassetteList);
        } catch (ServiceException e) {
            if (Validations.isEquals(retCodeConfig.getAllDataValAsterisk(), e.getCode())) {
                return retVal;
            } else {
                throw e;
            }
        }

        //************************************//
        //********   Do Spec Check  **********//
        //************************************//
        //	Step7 - specCheck_FillInTxDCC001DR
        /*Results.SpecCheckReqResult specCheckFillInOut = specCheckMethod.specCheckFillInTxDCC001DR(objCommon,
                equipmentID,
                controlJobID,
                startCassetteList);*/

        Results.SpecCheckReqResult specCheckFillInOut = specCheckMethod.specCheckFillInDR(objCommon,
                equipmentID,
                controlJobID,
                startCassetteList);

        //Merge already checked retVal item into StartCassette
        List<Infos.StartCassette> startCassetteSpecCheckResult = specCheckFillInOut.getStartCassetteList();
        /*List<Infos.StartCassette> startCassetteSource = checkValidityForSpecCheckDROut.getStartCassetteList();*/

        /*if (pjDataExistFlag) {
            //->note: below code just copy from c code logic, maybe can replace or remove in the future.
            for (int i = 0; i < startCassetteSpecCheckResult.size(); i++) {
                Infos.StartCassette startCassette = startCassetteSpecCheckResult.get(i);
                for (int j = 0; j < startCassette.getLotInCassetteList().size(); j++) {
                    List<Infos.DataCollectionInfo> resultDCDefList = startCassette.getLotInCassetteList().get(j).getStartRecipe().getDcDefList();
                    List<Infos.DataCollectionInfo> sourceDCDefList = startCassetteSource.get(i).getLotInCassetteList().get(j).getStartRecipe().getDcDefList();
                    for (int k = 0; k < resultDCDefList.size(); k++) {
                        List<Infos.DataCollectionItemInfo> resultDCItemList = resultDCDefList.get(k).getDcItems();
                        List<Infos.DataCollectionItemInfo> sourceDCItemList = sourceDCDefList.get(k).getDcItems();
                        final int resultDCItemLen = resultDCItemList.size();
                        for (int dcItemIndexNum = 0; dcItemIndexNum < resultDCItemLen; dcItemIndexNum++) {
                            if (sourceDCItemList.size() >= resultDCItemLen) {
                                Validations.check( retCodeConfig.getSystemError());
                            }
                            sourceDCItemList.set(dcItemIndexNum, resultDCItemList.get(dcItemIndexNum));
                        }
                    }
                }
            }
            startCassetteSpecCheckResult = startCassetteSource;
        }*/
        //Set out structure
        retVal.setStartCassetteList(startCassetteSpecCheckResult);
        retVal.setControlJobID(specCheckFillInOut.getControlJobID());
        retVal.setEquipmentID(specCheckFillInOut.getEquipmentID());

        //TODO:	Step8 - txAPCDataDerivationInq__120, APC Derived Data
        //----------------------------------------------------------------
        // Bonding Map Info Get
        //----------------------------------------------------------------
        Outputs.ObjBondingGroupInfoByEqpGetDROut strBondingGroupInfoByEqpGetDROut;

        // Step9 - bondingGroup_infoByEqp_GetDR
        strBondingGroupInfoByEqpGetDROut = bondingGroupMethod.bondingGroupInfoByEqpGetDR(
                objCommon,
                equipmentID,
                controlJobID,
                TRUE);

        int topLotLen = CimArrayUtils.getSize(strBondingGroupInfoByEqpGetDROut.getTopLotIDSeq());
        log.info("topLotLen={}", topLotLen);

        if (topLotLen > 0) {
            log.info("There is Top Lot.");

            int scLen = CimArrayUtils.getSize(retVal.getStartCassetteList());
            for (int i = 0; i < scLen; i++) {
                final Infos.StartCassette startCassette = retVal.getStartCassetteList().get(i);
                int ncLen = CimArrayUtils.getSize(startCassette.getLotInCassetteList());
                log.info("strSpecCheckReqResult.strStartCassette[i].strLotInCassette.length = {}", ncLen);

                for (int j = 0; j < ncLen; j++) {
                    //---------------------------//
                    //   Omit Not-OpeStart Lot   //
                    //---------------------------//
                    if (CimBooleanUtils.isFalse(startCassette.getLotInCassetteList().get(j).getMoveInFlag())) {
                        log.info("strSpecCheckReqResult.strStartCassette[i].strLotInCassette[j].operationStartFlag == FALSE");
                        continue;
                    }

                    boolean isTopLot = FALSE;
                    for (int topLotCnt = 0; topLotCnt < topLotLen; topLotCnt++) {
                        if (CimStringUtils.equals(startCassette.getLotInCassetteList().get(j).getLotID().getValue(),
                                strBondingGroupInfoByEqpGetDROut.getTopLotIDSeq().get(topLotCnt).getValue())) {
                            log.info("isTopLot={}", topLotCnt);
                            isTopLot = TRUE;
                            break;
                        }
                    }
                    if (isTopLot) {
                        log.info("Lot[{}] is Top Lot. Clearing Action.", startCassette.getLotInCassetteList().get(j).getLotID().getValue());
                        List<Infos.DataCollectionInfo> strDCDef = startCassette.getLotInCassetteList().get(j).getStartRecipe().getDcDefList();
                        int defLen = CimArrayUtils.getSize(strDCDef);
                        for (int defCnt = 0; defCnt < defLen; defCnt++) {
                            int itmLen = CimArrayUtils.getSize(strDCDef.get(defCnt).getDcItems());
                            for (int itmCnt = 0; itmCnt < itmLen; itmCnt++) {
                                strDCDef.get(defCnt).getDcItems().get(itmCnt).setActionCodes("");
                                strDCDef.get(defCnt).getDcItems().get(itmCnt).setSpecCheckResult("");
                            }
                        }
                    }
                }
            }
        }

        //Step10 - processOperation_tempData_Set, Set APCDerived Data into PO
        // todo: txAPCDataDerivationInq__120 apc 没有做，暂时注释下面代码
        /*processMethod.processOperationTempDataSet(objCommon, controlJobID, retVal.getStartCassetteList());*/
        if (TransactionIDEnum.DATA_SPEC_CHECK_REQ.equals(objCommon.getTransactionID())) {

            Map<String, String> specCheckResultMapping = new HashMap<>();
            specCheckResultMapping.put(BizConstant.SP_SPECCHECKRESULT_1X_OK, BizConstant.SP_SPECCHECKRESULT_OK);
            specCheckResultMapping.put(BizConstant.SP_SPECCHECKRESULT_1X_UPPERCONTROLLIMIT, BizConstant.SP_SPECCHECKRESULT_UPPERCONTROLLIMIT);
            specCheckResultMapping.put(BizConstant.SP_SPECCHECKRESULT_1X_LOWERCONTROLLIMIT, BizConstant.SP_SPECCHECKRESULT_LOWERCONTROLLIMIT);
            specCheckResultMapping.put(BizConstant.SP_SPECCHECKRESULT_1X_UPPERSPECLIMIT, BizConstant.SP_SPECCHECKRESULT_UPPERSPECLIMIT);
            specCheckResultMapping.put(BizConstant.SP_SPECCHECKRESULT_1X_LOWERSPECLIMIT, BizConstant.SP_SPECCHECKRESULT_LOWERSPECLIMIT);
            specCheckResultMapping.put(BizConstant.SP_SPECCHECKRESULT_1X_UPPERSCREENLIMIT, BizConstant.SP_SPECCHECKRESULT_UPPERSCREENLIMIT);
            specCheckResultMapping.put(BizConstant.SP_SPECCHECKRESULT_1X_LOWERSCREENLIMIT, BizConstant.SP_SPECCHECKRESULT_LOWERSCREENLIMIT);
            specCheckResultMapping.put(BizConstant.SP_SPECCHECKRESULT_1X_APCERROR, BizConstant.SP_SPECCHECKRESULT_APCERROR);
            specCheckResultMapping.put(BizConstant.SP_SPECCHECKRESULT_1X_POUND, BizConstant.SP_SPECCHECKRESULT_1POUND);
            specCheckResultMapping.put(BizConstant.SP_SPECCHECKRESULT_1X_ASTERISK, BizConstant.SP_SPECCHECKRESULT_1ASTERISK);

            for (Infos.StartCassette startCassette : retVal.getStartCassetteList()) {
                for (Infos.LotInCassette lotInCassette : startCassette.getLotInCassetteList()) {
                    //Omit Not-OpeStart lot
                    if (CimBooleanUtils.isFalse(lotInCassette.getMoveInFlag())) {
                        continue;
                    }
                    for (Infos.DataCollectionInfo dcDef : lotInCassette.getStartRecipe().getDcDefList()) {
                        for (Infos.DataCollectionItemInfo dcItem : dcDef.getDcItems()) {
                            String specCheckResult = dcItem.getSpecCheckResult();
                            String checkResultMapping = specCheckResultMapping.get(specCheckResult);
                            if (checkResultMapping != null) {
                                dcItem.setSpecCheckResult(checkResultMapping);
                            }
                        }
                    }
                }
            }
        }
        return retVal;
    }

    @Override
    public ObjectIdentifier sxEDCTransitDataRpt(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, String portGroupID,
                                                ObjectIdentifier controlJobID, List<Infos.StartCassette> startCassetteList) {
        Validations.check(CimArrayUtils.isEmpty(startCassetteList), "startCassetteList can not be empty");

        //Step1 - Check controlJob validity between input CJ and lot actrual CJ.
        for (Infos.StartCassette startCassette : startCassetteList) {
            List<Infos.LotInCassette> lotInCassetteList = startCassette.getLotInCassetteList();
            if (CimArrayUtils.isEmpty(lotInCassetteList)) {
                continue;
            }
            for (Infos.LotInCassette lotInCassette : lotInCassetteList) {
                if (CimBooleanUtils.isFalse(lotInCassette.getMoveInFlag())) {
                    continue;
                }
                // Lock lot Object
                objectLockMethod.objectLock(objCommon, CimLot.class, lotInCassette.getLotID());
                //Check Control Job validity between input controlJob and input lot actual controlJob
                //Step1-1 lot_controlJobID_Get
                ObjectIdentifier lotControlJobIDOut = lotMethod.lotControlJobIDGet(objCommon, lotInCassette.getLotID());
                if (!ObjectIdentifier.equalsWithValue(lotControlJobIDOut, controlJobID)) {
                    throw new ServiceException(retCodeConfig.getInvalidInputParam());
                }
            }
        }

        final int dataValueStrMaxLength = 128;
//        final int dataValueOtherMaxLength = 12;
        //MES qiandao-dev integration  start (update integer and flaot length 20 size because the Double  accuracy is round 15 ~ 16 size)
        final int dataValueOtherMaxLength = 20;
        //MES qiandao-dev integration  end (update integer and flaot length 20 size because the Double  accuracy is round 15 ~ 16 size)
        //Check DataValue length
        String longStringValue = StandardProperties.OM_EXPAND_STRING_VALUE.getValue();

        startCassetteList.stream()
                .flatMap(startCassette -> startCassette.getLotInCassetteList().stream())
                .flatMap(lotInCassette -> lotInCassette.getStartRecipe().getDcDefList().stream())
                .flatMap(dataCollectionInfo -> dataCollectionInfo.getDcItems().stream())
                .filter(dcItem -> CimStringUtils.isNotEmpty(dcItem.getDataValue()))
                .forEach(dcItem -> {
                    String dataValue = dcItem.getDataValue();
                    if (CimStringUtils.equals(longStringValue, "ON") && CimStringUtils.equals(dcItem.getDataType(), BizConstant.SP_DCDEF_VAL_STRING)) {
                        Validations.check(dataValue.length() > dataValueStrMaxLength, retCodeConfig.getInvalidInputParam());
                    } else if (dataValue.length() > dataValueOtherMaxLength) {
                        Validations.check(retCodeConfig.getInvalidInputParam());
                    }
                });

        //	Step2 - Check if lot is in processing with equipmentID, equipment_inprocessingControlJobInfo_Get
        List<Infos.EqpInprocessingControlJob> eqpInprocessingControlJobs = equipmentMethod.equipmentInprocessingControlJobInfoGetDR(objCommon, equipmentID);
        if (CimArrayUtils.isNotEmpty(eqpInprocessingControlJobs)) {
            List<ObjectIdentifier> inProcessingLotIDs = new ArrayList<>();
            for (Infos.EqpInprocessingControlJob eqpInprocessingControlJob : eqpInprocessingControlJobs) {
                List<Infos.EqpInprocessingLot> strEqpInprocessingLot = eqpInprocessingControlJob.getEqpInprocessingLotList();
                if (CimArrayUtils.isEmpty(strEqpInprocessingLot)) {
                    continue;
                }
                for (Infos.EqpInprocessingLot eqpInprocessingLot : strEqpInprocessingLot) {
                    inProcessingLotIDs.add(eqpInprocessingLot.getLotID());
                }
            }
            List<ObjectIdentifier> startCassetteLotIDs = new ArrayList<>();
            for (Infos.StartCassette startCassette : startCassetteList) {
                List<Infos.LotInCassette> lotInCassetteList = startCassette.getLotInCassetteList();
                if (CimArrayUtils.isEmpty(lotInCassetteList)) {
                    continue;
                }
                for (Infos.LotInCassette lotInCassette : lotInCassetteList) {
                    startCassetteLotIDs.add(lotInCassette.getLotID());
                }
            }
            boolean foundFlag = false;
            for (ObjectIdentifier inProcessingLotID : inProcessingLotIDs) {
                for (ObjectIdentifier startCassetteLotID : startCassetteLotIDs) {
                    if (CimStringUtils.equals(inProcessingLotID.getValue(), startCassetteLotID.getValue())) {
                        foundFlag = true;
                        break;
                    }
                }
                if (foundFlag) {
                    break;
                }
            }
            Validations.check(!foundFlag, retCodeConfig.getNotFoundInProcessingLot());
        }


        startCassetteList.stream()
                .flatMap(startCassette -> startCassette.getLotInCassetteList().stream())
                .filter(Infos.LotInCassette::getMoveInFlag)
                .forEach(lotInCassette -> {
                    //Step3 - Check if PJ Level data is defined for lot, processOperation_DataCondition_GetDR
                    Outputs.ObjProcessOperationDataConditionGetDrOut processOperationDataConditionOut =
                            processMethod.processOperationDataConditionGetDR(objCommon,
                                    lotInCassette.getLotID(),
                                    BizConstant.SP_DATACONDITIONCATEGORY_BYPJDATAITEM);

                    if (processOperationDataConditionOut.getCount() > 0) {
                        List<Infos.DataCollectionInfo> dcDefList = lotInCassette.getStartRecipe().getDcDefList();
                        if (CimArrayUtils.isNotEmpty(dcDefList)) {
                            for (Infos.DataCollectionInfo dcDef : dcDefList) {
                                //Step4 - Get already collected Data, processOperation_DCItems_Get
                                List<Infos.DataCollectionItemInfo> reportedDCItemList = dcDef.getDcItems();
                                if (CimArrayUtils.isEmpty(reportedDCItemList)) {
                                    continue;
                                }
                                List<Infos.DataCollectionItemInfo> processOperationDCItemsOut = processMethod.processOperationDCItemsGet(objCommon,
                                        lotInCassette.getLotID(),
                                        dcDef.getDataCollectionDefinitionID());

                                if (dcDefList.indexOf(dcDef) == 0) {
                                    // only merge first dcdef
                                    List<Infos.DataCollectionItemInfo> resultDCItemList = processOperationDCItemsOut;
                                    if (CimArrayUtils.isEmpty(resultDCItemList)) {
                                        continue;
                                    }
                                    // Start to merge data
                                    boolean addPJLvlItemFlag = false;
                                    final int resultDCItemLen = resultDCItemList.size();

                                    for (Infos.DataCollectionItemInfo reportedDCItem : reportedDCItemList) {
                                        if (CimArrayUtils.generateList(BizConstant.SP_DCDEF_MEAS_PJ,
                                                BizConstant.SP_DCDEF_MEAS_PJWAFER,
                                                BizConstant.SP_DCDEF_MEAS_PJWAFERSITE).contains(reportedDCItem.getMeasurementType())) {
                                            if (CimStringUtils.equals(reportedDCItem.getWaferPosition(), "*")) {
                                                reportedDCItem.setWaferPosition("");
                                            }
                                            // PJ Level
                                            for (int i = 0; i < resultDCItemLen; i++) {
                                                Infos.DataCollectionItemInfo resultDCItem = resultDCItemList.get(i);
                                                if (CimStringUtils.equals(resultDCItem.getDataCollectionItemName(), reportedDCItem.getDataCollectionItemName())
                                                        && CimStringUtils.equals(resultDCItem.getSitePosition(), reportedDCItem.getSitePosition())
                                                        && CimStringUtils.equals(resultDCItem.getMeasurementType(), reportedDCItem.getMeasurementType())) {
                                                    if (CimStringUtils.equals(resultDCItem.getWaferPosition(), "*")) {
                                                        // overwrite it
                                                        resultDCItemList.set(i, reportedDCItem);
                                                    } else {
                                                        // add to the bottom
                                                        resultDCItemList.add(reportedDCItem);
                                                        addPJLvlItemFlag = true;
                                                    }
                                                    break;
                                                }
                                            }
                                        } else {
                                            // CJ level
                                            for (int i = 0; i < resultDCItemLen; i++) {
                                                Infos.DataCollectionItemInfo resultDCItem = resultDCItemList.get(i);
                                                // always overwrite
                                                if (CimStringUtils.equals(reportedDCItem.getDataCollectionItemName(), resultDCItem.getDataCollectionItemName())
                                                        && CimStringUtils.equals(reportedDCItem.getSitePosition(), resultDCItem.getSitePosition())
                                                        && CimStringUtils.equals(reportedDCItem.getMeasurementType(), resultDCItem.getMeasurementType())
                                                        && CimStringUtils.equals(reportedDCItem.getWaferPosition(), resultDCItem.getWaferPosition())) {
                                                    resultDCItemList.set(i, reportedDCItem);
                                                }
                                            }
                                        }
                                    }
                                    if (addPJLvlItemFlag) {
                                        List<Infos.DataCollectionItemInfo> sortDCItemList = new ArrayList<>();
                                        for (int i = 0; i < resultDCItemLen; i++) {
                                            Infos.DataCollectionItemInfo dcItem = resultDCItemList.get(i);
                                            if (CimStringUtils.isEmpty(dcItem.getDataCollectionItemName())) {
                                                continue;
                                            }
                                            sortDCItemList.add(dcItem);
                                            if (CimStringUtils.equalsIn(dcItem.getMeasurementType(),
                                                    BizConstant.SP_DCDEF_MEAS_PJ,
                                                    BizConstant.SP_DCDEF_MEAS_PJWAFER,
                                                    BizConstant.SP_DCDEF_MEAS_PJWAFERSITE)
                                            ) {
                                                for (int subItemNum = i + 1; subItemNum < resultDCItemLen; subItemNum++) {
                                                    Infos.DataCollectionItemInfo subItem = resultDCItemList.get(subItemNum);
                                                    if (CimStringUtils.isEmpty(subItem.getDataCollectionItemName())) {
                                                        continue;
                                                    }
                                                    if (CimStringUtils.equals(dcItem.getDataCollectionItemName(), subItem.getDataCollectionItemName())
                                                            && CimStringUtils.equals(dcItem.getSitePosition(), subItem.getSitePosition())
                                                            && CimStringUtils.equals(dcItem.getMeasurementType(), subItem.getMeasurementType())) {
                                                        sortDCItemList.add(subItem);
                                                        dcItem.setDataCollectionItemName("");
                                                    }
                                                }
                                            }
                                        }
                                        resultDCItemList = sortDCItemList;
                                    }
                                    //Set updated PO stored data into start cassette
                                    dcDef.setDcItems(resultDCItemList);
                                } else {
                                    // deltaDC, use the one from database
                                    dcDef.setDcItems(processOperationDCItemsOut);
                                }
                            }
                        }
                    }
                });

        //	Step5 - Store reported Data, processOperation_tempData_Set
        /*---------------------------------------------------*/
        // author: zqi                                       */
        // date: 2021/7/9 14:00:00                           */
        // edcTransitDataRpt 性能优化                         */
        /*---------------------------------------------------*/
        /*processMethod.processOperationTempDataSet(objCommon, controlJobID, startCassetteList);*/
        processMethod.processOperationTempDataEDCItemsSet(objCommon, controlJobID, startCassetteList);
        return equipmentID;
    }

    @Override
    public Results.SPCCheckReqResult sxSPCCheckReq(Infos.ObjCommon objCommon, Params.SPCCheckReqParams spcCheckReqParams) {
        Outputs.ObjSPCMgrSendSPCCheckReqOut objSPCMgrSendSPCCheckReqOut = spcMethod.spcMgrSendSPCCheckReq(objCommon, spcCheckReqParams.getEquipmentID(), spcCheckReqParams.getControlJobID(), spcCheckReqParams.getStrStartCassette());
        return objSPCMgrSendSPCCheckReqOut.getSpcCheckReqResult();
    }

    @Override
    public void sxSPCDoActionReq(Infos.ObjCommon objCommon, List<Infos.BankMove> bankMoveList, List<Infos.MailSend> mailSendList, List<Infos.ReworkBranch> reworkBranchList) {
        /*-----------------------------------------------------------*/
        /*   Send E-Mail                                             */
        /*-----------------------------------------------------------*/
        int nLen = CimArrayUtils.getSize(mailSendList);
        for (int i = 0; i < nLen; i++) {
            messageMethod.messageDistributionMgrPutMessage(objCommon, mailSendList.get(i).getMessageAttributes().getMessageID(), mailSendList.get(i).getMessageAttributes().getLotID(),
                    mailSendList.get(i).getMessageAttributes().getLotStatus(), mailSendList.get(i).getMessageAttributes().getEquipmentID(), mailSendList.get(i).getMessageAttributes().getRouteID(),
                    mailSendList.get(i).getMessageAttributes().getOperationNumber(), mailSendList.get(i).getMessageAttributes().getReasonCode(), mailSendList.get(i).getMessageAttributes().getMessageText());
        }
        /*-----------------------------------------------------------*/
        /*   Rework                                                  */
        /*-----------------------------------------------------------*/
        nLen = CimArrayUtils.getSize(reworkBranchList);
        for (int j = 0; j < nLen; j++) {
            // Check Hold state
            Outputs.ObjLotAllStateGetOut objLotAllStateGetOut = lotMethod.lotAllStateGet(objCommon, reworkBranchList.get(j).getLotID());
            if (CimStringUtils.equals(objLotAllStateGetOut.getHoldState(), CIMStateConst.CIM_LOT_HOLD_STATE_NOTONHOLD)) {
                Outputs.ObjLotCurrentOperationInfoGetOut objLotCurrentOperationInfoGetOut = lotMethod.lotCurrentOperationInfoGet(objCommon, reworkBranchList.get(j).getLotID());
                ObjectIdentifier reasonCode = ObjectIdentifier.buildWithValue(BizConstant.SP_REASON_SPCOUTOFRANGEHOLD);
                Params.ReworkWholeLotReqParams reworkWholeLotReqParams = new Params.ReworkWholeLotReqParams();
                reworkWholeLotReqParams.setLotID(reworkBranchList.get(j).getLotID());
                reworkWholeLotReqParams.setCurrentRouteID(objLotCurrentOperationInfoGetOut.getRouteID());
                reworkWholeLotReqParams.setCurrentOperationNumber(objLotCurrentOperationInfoGetOut.getOperationNumber());
                reworkWholeLotReqParams.setSubRouteID(reworkBranchList.get(j).getReworkRouteID());
                reworkWholeLotReqParams.setReturnOperationNumber(reworkBranchList.get(j).getReturnOperationNumber());
                reworkWholeLotReqParams.setReasonCodeID(reasonCode);
                try {
                    lotService.sxReworkWholeLotReq(objCommon, reworkWholeLotReqParams);
                } catch (ServiceException e) {
                    if (!Validations.isEquals(retCodeConfig.getNotFoundOperation(), e.getCode())) {
                        throw e;
                    }
                }
            } else {
                log.info("HoldState == OnHold");
            }
        }
        /*-----------------------------------------------------------*/
        /*   Bank In                                                 */
        /*-----------------------------------------------------------*/
        nLen = CimArrayUtils.getSize(bankMoveList);
        for (int i = 0; i < nLen; i++) {
            // Check Hold state
            Outputs.ObjLotAllStateGetOut objLotAllStateGetOut = lotMethod.lotAllStateGet(objCommon, bankMoveList.get(i).getLotID());
            if (CimStringUtils.equals(objLotAllStateGetOut.getHoldState(), CIMStateConst.CIM_LOT_HOLD_STATE_NOTONHOLD)) {
                // NotOnHold ===> Do Bank In
                Params.NonProdBankStoreReqParams nonProdBankStoreReqParams = new Params.NonProdBankStoreReqParams();
                nonProdBankStoreReqParams.setLotID(bankMoveList.get(i).getLotID());
                nonProdBankStoreReqParams.setBankID(bankMoveList.get(i).getBankID());
                bankService.sxNonProdBankStoreReq(objCommon, nonProdBankStoreReqParams);
            } else {
                log.info("HoldState == OnHold");
            }
        }
    }

    @Override
    public Results.EDCWithSpecCheckActionByPJReqResult sxEDCWithSpecCheckActionByPJReq(Infos.ObjCommon objCommon, Params.EDCWithSpecCheckActionByPJReqParams params) {
        ObjectIdentifier equipmentID = params.getEquipmentID();
        ObjectIdentifier controlJobID = params.getControlJobID();
        ObjectIdentifier lotID = params.getLotID();
        //----------------------------------------------------------------
        //  In-Parameter Trace
        //----------------------------------------------------------------
        log.info("{}equipmentID  ", equipmentID);
        log.info("{}controlJobID ", controlJobID);
        log.info("{}lotID        ", lotID);

        //---------------------------------//
        // Initialize variable             //
        //---------------------------------//
        int nDCItemLen = 0;

        boolean bSpecCheckRequiredFlag = false;   // for Spec Check

        //--------------------------------------------------
        // check input parameter
        //--------------------------------------------------
        if (ObjectIdentifier.isEmptyWithValue(equipmentID)) {
            log.info("{} The input parameter is not specified.", "equipmentID is Blank");
            Validations.check(true, retCodeConfig.getInvalidInputParam());
        }
        if (ObjectIdentifier.isEmptyWithValue(controlJobID)) {
            log.info("{} The input parameter is not specified.", "controlJobID is Blank");
            Validations.check(true, retCodeConfig.getInvalidInputParam());
        }
        if (ObjectIdentifier.isEmptyWithValue(lotID)) {
            log.info("{} The input parameter is not specified.", "lotID is Blank");
            Validations.check(true, retCodeConfig.getInvalidInputParam());
        }

        /*=============================================================================*/
        /* Object Lock Process                                                         */
        /*=============================================================================*/
        //---------------------//
        //   Lock Lot Object   //
        //---------------------//
        //object_Lock
        objectLockMethod.objectLock(objCommon, CimLot.class, params.getLotID());

        /*=============================================================================*/
        /* Check Process                                                               */
        /*=============================================================================*/
        //-----------------------------------------------------------------------//
        //   Get Started Lot information which is specified with ControlJob ID   //
        //-----------------------------------------------------------------------//
        List<Infos.DataCollectionInfo> strInitDCDefSeq = lotMethod.lotCurrentOperationDataCollectionInformationGet(objCommon, lotID);
        if (CimObjectUtils.isEmpty(strInitDCDefSeq)) {
            log.info("strDCDefSeq.length == 0");
            return null;
        }
        int nDCDefLen = CimArrayUtils.getSize(strInitDCDefSeq);
        log.info("{}nDCDefLen", nDCDefLen);
        //----------------------------------------------------------------
        // Bonding Map Info Get
        //----------------------------------------------------------------
        // bondingGroup_infoByEqp_GetDR
        Outputs.ObjBondingGroupInfoByEqpGetDROut objBondingGroupInfoByEqpGetDROut = bondingGroupMethod.bondingGroupInfoByEqpGetDR(objCommon, equipmentID, controlJobID, true);
        List<ObjectIdentifier> topLotIDSeq = objBondingGroupInfoByEqpGetDROut.getTopLotIDSeq();
        int topLotLen = CimArrayUtils.getSize(topLotIDSeq);
        for (int topLotCnt = 0; topLotCnt < topLotLen; topLotCnt++) {
            if (ObjectIdentifier.equalsWithValue(lotID, topLotIDSeq.get(topLotCnt))) {
                return null;
            }
        }
        /*=============================================================================*/
        /* Get Expand Derived Data Item                                                */
        /*=============================================================================*/
        Outputs.ObjProcessOperationDataConditionGetDrOut getProcessOperationDataCondition = processMethod.processOperationDataConditionGetDR(objCommon, lotID, BizConstant.SP_DATACONDITIONCATEGORY_EXPANDDERIVEDDATA);
        List<Infos.DataCollectionInfo> strExpandDerivedDCDef = getProcessOperationDataCondition.getExpandDerivedDCDefList();
        int nExpandDCDefLen = CimArrayUtils.getSize(strExpandDerivedDCDef);
        log.info("{}nExpandDCDefLen", nExpandDCDefLen);

        /*=============================================================================*/
        /* Separate data items into 2 groups                                           */
        /*    - Already spec checked data items                                        */
        /*    - Not spec checked data items                                            */
        /*=============================================================================*/
        List<Infos.DataCollectionInfo> strDCDefSeq = cloner.deepClone(strInitDCDefSeq);
        List<Infos.DataCollectionInfo> strSpecCheckDCDef = cloner.deepClone(strInitDCDefSeq);

        //-------------------------------//
        //  Set Spec Check Required Flag //
        //-------------------------------//

        List<List<Integer>> specCheckIndexSeq = new ArrayList<>();
        int specCheckIndexLen = strSpecCheckDCDef.size();

        for (int nDCDefNum = 0; nDCDefNum < specCheckIndexLen; nDCDefNum++) {
            log.info("{} nDCDefNum", nDCDefNum);
            boolean bCheckItemExistFlag = false;
            int nChkd = 0;

            int nExpandDCItemLen = 0;
            int nExpandDCDefNum = 0;
            for (nExpandDCDefNum = 0; nExpandDCDefNum < nExpandDCDefLen; nExpandDCDefNum++) {
                Infos.DataCollectionInfo dataCollectionInfo = strExpandDerivedDCDef.get(nExpandDCDefNum);
                log.info("{} nExpandDCDefNum", nExpandDCDefNum);
                if (ObjectIdentifier.equalsWithValue(strInitDCDefSeq.get(nDCDefNum).getDataCollectionDefinitionID(), dataCollectionInfo.getDataCollectionDefinitionID())) {
                    log.info("strInitDCDefSeq[nDCDefNum].dataCollectionDefinitionID == strExpandDerivedDCDef[nExpandDCDefNum].dataCollectionDefinitionID");
                    nExpandDCItemLen = dataCollectionInfo.getDcItems().size();
                    break;
                }
            }

            nDCItemLen = CimArrayUtils.getSize(strInitDCDefSeq.get(nDCDefNum).getDcItems());
            int nDCItemCnt = 0;
            int nExpandDCItemCurrPos = 0;
            String expandCurrDCItemName = null;
            String expandCurrMeasType = null;
            String expandCurrSitePos = null;
            List<Integer> specCheckIndexSeqInner = new ArrayList<>();
            specCheckIndexSeq.add(specCheckIndexSeqInner);
            for (int nDCItemNum = 0; nDCItemNum < nDCItemLen; nDCItemNum++) {
                Infos.DataCollectionItemInfo dcItem = strInitDCDefSeq.get(nDCDefNum).getDcItems().get(nDCItemNum);
                log.info("{} nDCItemNum", nDCItemNum);
                String measurementType = dcItem.getMeasurementType();
                if (CimStringUtils.equals(measurementType, BizConstant.SP_DCDEF_MEAS_PJ)
                        || CimStringUtils.equals(measurementType, BizConstant.SP_DCDEF_MEAS_PJWAFER)
                        || CimStringUtils.equals(measurementType, BizConstant.SP_DCDEF_MEAS_PJWAFERSITE)) {
                    log.info("DCItem by PJ");

                    /*=============================================================================*/
                    /* Expand Derived Data Item                                                    */
                    /*=============================================================================*/
                    String itemType = dcItem.getItemType();
                    if (0 < nExpandDCItemLen
                            && (CimStringUtils.equals(itemType, BizConstant.SP_DCDEF_ITEM_DERIVED)
                            || CimStringUtils.equals(itemType, BizConstant.SP_DCDEF_ITEM_USERFUNCTION))) {
                        log.info("0 < nExpandDCItemLen and itemType == Derived");

                        if (CimStringUtils.isEmpty(expandCurrDCItemName)) {
                            log.info("ObjectUtils.isEmpty( expandCurrDCItemName )");
                            expandCurrDCItemName = dcItem.getDataCollectionItemName();
                            expandCurrMeasType = dcItem.getMeasurementType();
                            expandCurrSitePos = dcItem.getSitePosition();
                        }
                        if (CimStringUtils.equals(expandCurrDCItemName, dcItem.getDataCollectionItemName())
                                && CimStringUtils.equals(expandCurrMeasType, dcItem.getMeasurementType())
                                && CimStringUtils.equals(expandCurrSitePos, dcItem.getSitePosition())) {
                            log.info("Start of the expansion");
                            int nExpandDCItemCnt = 0;
                            for (nExpandDCItemCnt = nExpandDCItemCurrPos; nExpandDCItemCnt < nExpandDCItemLen; nExpandDCItemCnt++) {
                                log.info("{} {} nExpandDCItemLen/nExpandDCItemCnt", nExpandDCItemLen, nExpandDCItemCnt);
                                Infos.DataCollectionItemInfo dataCollectionItemInfo = strExpandDerivedDCDef.get(nExpandDCDefNum).getDcItems().get(nExpandDCItemCnt);
                                if (CimStringUtils.equals(expandCurrDCItemName, dataCollectionItemInfo.getDataCollectionItemName())
                                        && CimStringUtils.equals(expandCurrMeasType, dataCollectionItemInfo.getMeasurementType())
                                        && CimStringUtils.equals(expandCurrSitePos, dataCollectionItemInfo.getSitePosition())) {
                                    log.info("{} {} {} Add Expand Item", expandCurrDCItemName, expandCurrMeasType, expandCurrSitePos);
                                    strDCDefSeq.get(nDCDefNum).getDcItems().set(nDCItemCnt, dataCollectionItemInfo);
                                    bCheckItemExistFlag = true;
                                    specCheckIndexSeqInner.add(nDCItemCnt);
                                    strSpecCheckDCDef.get(nDCDefNum).getDcItems().set(nChkd, strDCDefSeq.get(nDCDefNum).getDcItems().get(nDCItemCnt));
                                    nChkd++;
                                    nDCItemCnt++;
                                } else {
                                    log.info("End of the expansion");
                                    expandCurrDCItemName = dataCollectionItemInfo.getDataCollectionItemName();
                                    expandCurrMeasType = dataCollectionItemInfo.getMeasurementType();
                                    expandCurrSitePos = dataCollectionItemInfo.getSitePosition();
                                    break;
                                }
                            } // nExpandDCItemCnt loop end
                            nExpandDCItemCurrPos = nExpandDCItemCnt;
                        }
                    }
                    Infos.DataCollectionItemInfo initDcDefSeqDcItem = strInitDCDefSeq.get(nDCDefNum).getDcItems().get(nDCItemNum);
                    if ((CimStringUtils.equals(initDcDefSeqDcItem.getItemType(), BizConstant.SP_DCDEF_ITEM_DERIVED)
                            || CimStringUtils.equals(initDcDefSeqDcItem.getItemType(), BizConstant.SP_DCDEF_ITEM_USERFUNCTION))
                            && CimStringUtils.equals(initDcDefSeqDcItem.getWaferPosition(), "*")) {
                        log.info("itemType == Derived and waferPosition == '*'");
                        continue;
                    }
                    strDCDefSeq.get(nDCDefNum).getDcItems().set(nDCItemCnt, initDcDefSeqDcItem);
                    Infos.DataCollectionItemInfo dcDefSeqDcItem = strDCDefSeq.get(nDCDefNum).getDcItems().get(nDCItemCnt);
                    if (CimStringUtils.equals(dcDefSeqDcItem.getItemType(), BizConstant.SP_DCDEF_ITEM_RAW)
                            && (CimStringUtils.isEmpty(dcDefSeqDcItem.getDataValue())
                            || CimStringUtils.equals(dcDefSeqDcItem.getDataValue(), "*"))) {
                        log.info("itemType == Raw and ( dataValue == '' or '*' )");
                        strDCDefSeq.get(nDCDefNum).getDcItems().get(nDCItemCnt).setSpecCheckResult(BizConstant.SP_SPECCHECKRESULT_ERROR);
                    } else if (CimStringUtils.isEmpty(strDCDefSeq.get(nDCDefNum).getDcItems().get(nDCItemCnt).getSpecCheckResult())) {
                        log.info("specCheckResult.length == 0");
                        bCheckItemExistFlag = true;
                        specCheckIndexSeqInner.add(nDCItemCnt);
                        strSpecCheckDCDef.get(nDCDefNum).getDcItems().set(nChkd, strDCDefSeq.get(nDCDefNum).getDcItems().get(nDCItemCnt));
                        nChkd++;
                    }
                } else {
                    log.info("DCItem by CJ");
                    strDCDefSeq.get(nDCDefNum).getDcItems().set(nDCItemCnt, strInitDCDefSeq.get(nDCDefNum).getDcItems().get(nDCItemNum));
                }
                nDCItemCnt++;
            }
            if (strInitDCDefSeq.get(nDCDefNum).getSpecCheckRequiredFlag() && bCheckItemExistFlag) {
                log.info("specCheckRequiredFlag");
                bSpecCheckRequiredFlag = true;
            }
        }

        //----------------------------------------------------------------------
        //  Main Process
        //----------------------------------------------------------------------
        List<Infos.StartCassette> strLotStartCassetteForAction = new ArrayList<>();
        log.info("{} bSpecCheckRequiredFlag is ", bSpecCheckRequiredFlag);
        //----------------------------------------------//
        //   If specCheckRequiredFlag is true           //
        //   Call txSpecCheckDataReq                    //
        //----------------------------------------------//
        if (bSpecCheckRequiredFlag) {
            log.info("true == bSpecCheckRequiredFlag");
            //Create start cassette for action decision
            Infos.StartCassette startCassette = new Infos.StartCassette();
            strLotStartCassetteForAction.add(startCassette);
            List<Infos.LotInCassette> lotInCassetteList = new ArrayList<>();
            startCassette.setLotInCassetteList(lotInCassetteList);
            Infos.LotInCassette lotInCassette = new Infos.LotInCassette();
            lotInCassetteList.add(lotInCassette);
            lotInCassette.setLotID(lotID);
            lotInCassette.setMoveInFlag(true);
            Infos.StartRecipe startRecipe = new Infos.StartRecipe();
            startRecipe.setDcDefList(strSpecCheckDCDef);
            lotInCassette.setStartRecipe(startRecipe);

            log.info("Do Spec Check");
            Results.SpecCheckReqResult strSpecCheckReqResult = new Results.SpecCheckReqResult();
            boolean bErrorInSpecCheckFlag = false;
            //-------------------------------------------------------------------------------------------//
            //   Do Spec Check                                                                           //
            //      - Calculate the derived value and Set them to PO if calculationRequiredFlag is true  //
            //      - Set spec  check result to PO if speckCheckRequiredFlag is true.                    //
            //-------------------------------------------------------------------------------------------//
            log.info("calling dataValue_CheckValidityForSpecCheckByPJ()");
            List<Infos.DataCollectionInfo> dataValueCheckValidityForSpecCheckByPJ = null;
            try {
                dataValueCheckValidityForSpecCheckByPJ = dataValueMethod.dataValueCheckValidityForSpecCheckByPJ(objCommon, equipmentID, controlJobID, lotID, strSpecCheckDCDef);
            } catch (ServiceException e) {
                if (Validations.isEquals(retCodeConfig.getDatavalCannotConvToInt(), e.getCode())) {
                    bErrorInSpecCheckFlag = true;
                } else {
                    throw e;
                }
            }

            /*---------------------------------------*/
            /*   Skip if error occurs in SpecCheck   */
            /*---------------------------------------*/
            if (!bErrorInSpecCheckFlag) {
                log.info("false == bErrorInSpecCheckFlag");
                /*---------------------------------*/
                /*   Create input start cassette   */
                /*---------------------------------*/
                List<Infos.StartCassette> strLotStartCassette = new ArrayList<>();
                Infos.StartCassette startCassette1 = new Infos.StartCassette();
                strLotStartCassette.add(startCassette1);
                List<Infos.LotInCassette> lotInCassetteList1 = new ArrayList<>();
                startCassette1.setLotInCassetteList(lotInCassetteList1);
                Infos.LotInCassette lotInCassette1 = new Infos.LotInCassette();
                lotInCassetteList1.add(lotInCassette1);
                lotInCassette1.setLotID(lotID);
                lotInCassette1.setMoveInFlag(true);
                Infos.StartRecipe startRecipe1 = new Infos.StartRecipe();
                startRecipe1.setDcDefList(dataValueCheckValidityForSpecCheckByPJ);
                lotInCassette1.setStartRecipe(startRecipe);
                /*------------------------------------------------*/
                /*   Set DC Spec's detailed information into PO   */
                /*------------------------------------------------*/
                strLotStartCassette = processMethod.processDataCollectionSpecificationSet(objCommon, strLotStartCassette);

                /*-------------------*/
                /*   Do Spec Check   */
                /*-------------------*/
                try {
                    strSpecCheckReqResult = specCheckMethod.specCheckFillInDR(objCommon, null, null, strLotStartCassette);
                } catch (ServiceException ex) {
                    Integer code = ex.getCode();
                    if (Validations.isEquals(retCodeConfig.getNotFoundProcessOperation(), code)
                            || Validations.isEquals(retCodeConfig.getNotFoundDcdef(), code)
                            || Validations.isEquals(retCodeConfig.getNotFoundPfx(), code)
                            || Validations.isEquals(retCodeConfig.getNotFoundProcessDefinition(), code)
                            || Validations.isEquals(retCodeConfig.getNotFoundDeltaProcessOperation(), code)) {
                        bErrorInSpecCheckFlag = true;
                    } else {
                        throw ex;
                    }
                }

                /*---------------------------------------*/
                /*   Skip if error occurs in SpecCheck   */
                /*---------------------------------------*/
                if (!bErrorInSpecCheckFlag) {
                    log.info("false == bErrorInSpecCheckFlag");
                    /*--------------------------*/
                    /*   APC Derived Data       */
                    /*--------------------------*/
                    //TODO-NOTIMPL: txAPCDataDerivationInq__120
                }
            }

            int dcDefIndexNum = 0;
            int dcItemIndexNum = 0;

            if (bErrorInSpecCheckFlag) {
                /*--------------------------------------------------------------------------*/
                /*   Error detected during Spec check.                                      */
                /*   Set 'E' to specCheckResult of data items for Spec check target ones.   */
                /*--------------------------------------------------------------------------*/
                for (dcDefIndexNum = 0; dcDefIndexNum < specCheckIndexLen; dcDefIndexNum++) {
                    log.info("{} dcDefIndexNum", dcDefIndexNum);
                    int specCheckIndexCnt = CimArrayUtils.getSize(specCheckIndexSeq.get(dcDefIndexNum));
                    for (dcItemIndexNum = 0; dcItemIndexNum < specCheckIndexCnt; dcItemIndexNum++) {
                        log.info("{} dcItemIndexNum", dcItemIndexNum);
                        int sourceIndex = specCheckIndexSeq.get(dcDefIndexNum).get(dcItemIndexNum);
                        if (sourceIndex >= strDCDefSeq.get(dcDefIndexNum).getDcItems().size()) {
                            log.info("index >= strDCItem.length()");
                            Validations.check(true, retCodeConfig.getSystemError());
                        }
                        log.info("{} sourceIndex", sourceIndex);
                        strDCDefSeq.get(dcDefIndexNum).getDcItems().get(sourceIndex).setSpecCheckResult(BizConstant.SP_SPECCHECKRESULT_ERROR);
                    }
                }
                //Create strSpecCheckReqResult structure temporally to use as input value of processOperation_tempData_Set and DebugOutStartCassette
                strSpecCheckReqResult.setStartCassetteList(strLotStartCassetteForAction);
                strSpecCheckReqResult.getStartCassetteList().get(0).getLotInCassetteList().get(0).getStartRecipe().setDcDefList(strDCDefSeq);
            } else {
                /*--------------------------------------------------------------------*/
                /*   Merge Spec Checked Data with Previously Spec Checked Data        */
                /*--------------------------------------------------------------------*/
                for (dcDefIndexNum = 0; dcDefIndexNum < specCheckIndexLen; dcDefIndexNum++) {
                    log.info("{} dcDefIndexNum", dcDefIndexNum);
                    int specCheckIndexCnt = CimArrayUtils.getSize(specCheckIndexSeq.get(dcDefIndexNum));
                    List<Infos.DataCollectionItemInfo> strResultDCItemSeq = strSpecCheckReqResult.getStartCassetteList().get(0).getLotInCassetteList().get(0).getStartRecipe().getDcDefList().get(dcDefIndexNum).getDcItems();
                    List<Infos.DataCollectionItemInfo> strSourceDCItemSeq = strDCDefSeq.get(dcDefIndexNum).getDcItems();
                    for (dcItemIndexNum = 0; dcItemIndexNum < specCheckIndexCnt; dcItemIndexNum++) {
                        log.info("{} dcItemIndexNum", dcItemIndexNum);
                        int sourceIndex = specCheckIndexSeq.get(dcDefIndexNum).get(dcItemIndexNum);
                        if (sourceIndex >= strSourceDCItemSeq.size()) {
                            log.info("index >= strDCItem.length()");
                            Validations.check(true, retCodeConfig.getSystemError());
                        }
                        strSourceDCItemSeq.set(sourceIndex, strResultDCItemSeq.get(dcItemIndexNum));
                    }
                }
                strSpecCheckReqResult.getStartCassetteList().get(0).getLotInCassetteList().get(0).getStartRecipe().setDcDefList(strDCDefSeq);
            }

            /*--------------------------------------------------------------------*/
            /*   Set Latest Collected Data into PO                                */
            /*--------------------------------------------------------------------*/
            processMethod.processOperationTempDataSet(objCommon, controlJobID, strSpecCheckReqResult.getStartCassetteList());

            //----------------------------------------------------------//
            // End of Data Spec Check                                   //
            //----------------------------------------------------------//

            /*-------------------------------------------------------------------*/
            /*   Set error occurs in spec check, return with RC_SPECCHECK_ERROR  */
            /*-------------------------------------------------------------------*/
            if (bErrorInSpecCheckFlag) {
                log.info("false == bErrorInSpecCheckFlag");
                Validations.check(true, retCodeConfig.getSpeccheckError());
            }

            //-------------------------------------------------------------------------------//
            //  Get Started Lot information in order to get the derived / delta data values  //
            //-------------------------------------------------------------------------------//
            log.info("Call lot_currentOperation_DataCollectionInformation_Get()");
            List<Infos.DataCollectionInfo> dataCollectionInfos = lotMethod.lotCurrentOperationDataCollectionInformationGet(objCommon, lotID);
            //---------------------
            // Remove Already checked item from strLotStartCassetteForAction
            //---------------------
            strLotStartCassetteForAction.get(0).getLotInCassetteList().get(0).getStartRecipe().setDcDefList(dataCollectionInfos);
            for (dcDefIndexNum = 0; dcDefIndexNum < specCheckIndexLen; dcDefIndexNum++) {
                log.info("dcDefIndexNum : {}", dcDefIndexNum);
                int specCheckeIndexCnt = CimArrayUtils.getSize(specCheckIndexSeq.get(dcDefIndexNum));
                List<Infos.DataCollectionItemInfo> strSourceDCItemSeq = dataCollectionInfos.get(dcDefIndexNum).getDcItems();
                List<Infos.DataCollectionItemInfo> strTargetDCItemSeq = strLotStartCassetteForAction.get(0).getLotInCassetteList().get(0).getStartRecipe().getDcDefList().get(dcDefIndexNum).getDcItems();
                for (dcItemIndexNum = 0; dcItemIndexNum < specCheckeIndexCnt; dcItemIndexNum++) {
                    log.info("dcItemIndexNum: {}", dcItemIndexNum);
                    Integer sourceIndex = specCheckIndexSeq.get(dcDefIndexNum).get(dcItemIndexNum);
                    if (sourceIndex.longValue() >= CimArrayUtils.getSize(strSourceDCItemSeq)) {
                        log.info("index >= strDCItem.length()");
                        Validations.check(true, retCodeConfig.getSystemError());
                    }
                    strTargetDCItemSeq.set(dcItemIndexNum, strSourceDCItemSeq.get(sourceIndex));
                }
            }
            specCheckIndexSeq.clear();
            //-----------------------------------------------------------------//
            //   Summary Action Items to Lot based on the Spec Check Result    //
            //-----------------------------------------------------------------//
            List<Infos.InterFabMonitorGroupActionInfo> interFabMonitorGroupActionInfoList = new ArrayList<>();
            List<Results.DCActionLotResult> dcActionLotResultList = new ArrayList<>();
            Outputs.ObjStartLotActionListEffectSpecCheckOut outData = new Outputs.ObjStartLotActionListEffectSpecCheckOut();
            log.info("Call startLot_actionList_EffectSpecCheck");
            Outputs.ObjStartLotActionListEffectSpecCheckOut startLotActionListEffectSpecCheckOut = startLotMethod.startLotActionListEffectSpecCheck(outData, objCommon, strLotStartCassetteForAction, equipmentID, interFabMonitorGroupActionInfoList, dcActionLotResultList);

            //-----------------------------------------------------------------//
            //   Register Entity Inhibition as the result of SPEC Check        //
            //-----------------------------------------------------------------//
            int nCheckLen = CimArrayUtils.getSize(outData.getEntityInhibitions());
            log.info("strStartLot_actionList_EffectSpecCheck_out.strEntityInhibitions.length() : {}", nCheckLen);
            Infos.EntityInhibitDetailInfo strMfgRestrictReqResult = null;
            for (int nCheckNum = 0; nCheckNum < nCheckLen; nCheckNum++) {
                log.info("Call txMfgRestrictReq()");
                Infos.EntityInhibitDetailAttributes entityInhibitDetailAttributes = startLotActionListEffectSpecCheckOut.getEntityInhibitions().get(nCheckNum);
                Params.MfgRestrictReqParams mfgRestrictReqParams = new Params.MfgRestrictReqParams();
                mfgRestrictReqParams.setEntityInhibitDetailAttributes(entityInhibitDetailAttributes);
                mfgRestrictReqParams.setClaimMemo(params.getClaimMemo());
                try {
//                    strMfgRestrictReqResult = constraintService.sxMfgRestrictReq(mfgRestrictReqParams, objCommon);

                    //Step5 - txMfgRestrictReq__110
                    Params.MfgRestrictReq_110Params mfgRestrictReq_110Params = new Params.MfgRestrictReq_110Params();
                    List<Infos.EntityInhibitDetailAttributes> entityInhibitDetailAttributeList = new ArrayList<>();
                    entityInhibitDetailAttributeList.add(entityInhibitDetailAttributes);
                    mfgRestrictReq_110Params.setClaimMemo(params.getClaimMemo());
                    mfgRestrictReq_110Params.setUser(objCommon.getUser());
                    mfgRestrictReq_110Params.setEntityInhibitDetailAttributeList(entityInhibitDetailAttributeList);
                    constraintService.sxMfgRestrictReq_110(mfgRestrictReq_110Params, objCommon);
                } catch (ServiceException e) {
                    if (!Validations.isEquals(retCodeConfig.getDuplicatedEntityInhibit(), e.getCode())) {
                        log.error("txMfgRestrictReq__101() != RC_OK: {}", e.getCode());
                        throw e;
                    }
                }
            }
            //-----------------------------------------------//
            //   Send a message as the result of SPEC Check  //
            //-----------------------------------------------//
            // MessageDistributionMgr_PutMessage
            List<Infos.MessageAttributes> messageList = startLotActionListEffectSpecCheckOut.getMessageList();
            int nMsgLen = CimArrayUtils.getSize(messageList);
            for (int nMsgNum = 0; nMsgNum < nMsgLen; nMsgNum++) {
                Infos.MessageAttributes messageAttributes = messageList.get(nMsgNum);
                messageMethod.messageDistributionMgrPutMessage(objCommon, messageAttributes.getMessageID(), messageAttributes.getLotID(), messageAttributes.getLotStatus(),
                        messageAttributes.getEquipmentID(), messageAttributes.getRouteID(), messageAttributes.getOperationNumber(), messageAttributes.getReasonCode(), messageAttributes.getMessageText());
            }

            //-------------------------------------------------------------------------//
            //   Register Entity Inhibition as the result of Spec Check(SaR request)   //
            //-------------------------------------------------------------------------//
            //【TODO】【TODO - NOTIMPL】-  entityInhibit_RequestForMultiFab__101

            //----------------------------------------------------//
            //   Update PO's collected data information finally   //
            //----------------------------------------------------//
            //processOperation_actionResult_Set
            Inputs.ObjProcessOperationActionResultSetIn objProcessOperationActionResultSetIn = new Inputs.ObjProcessOperationActionResultSetIn();
            objProcessOperationActionResultSetIn.setOverwriteFlag(false);
            objProcessOperationActionResultSetIn.setStrStartCassette(strLotStartCassetteForAction);
            objProcessOperationActionResultSetIn.setStrDCActionLotResult(startLotActionListEffectSpecCheckOut.getDcActionLotResultList());
            processMethod.processOperationActionResultSet(objCommon, objProcessOperationActionResultSetIn);

        }
        //--------------------------//
        //   Set Return Structure   //
        //--------------------------//
        List<Infos.StartCassette> tmpStartCassette = new ArrayList<>();
        List<Infos.SpcCheckLot> tmpSpcCheckLotList = new ArrayList<>();
        if (bSpecCheckRequiredFlag) {
            log.info("specCheckRequiredFlag == TRUE");
            tmpStartCassette = strLotStartCassetteForAction;
        } else {
            log.info(" specCheckRequiredFlag == FALSE ");
            Infos.StartCassette startCassette = new Infos.StartCassette();
            Infos.LotInCassette lotInCassette = new Infos.LotInCassette();
            lotInCassette.setLotID(params.getLotID());
            lotInCassette.setMoveInFlag(true);
            Infos.StartRecipe startRecipe = new Infos.StartRecipe();
            startRecipe.setDcDefList(strSpecCheckDCDef);
            lotInCassette.setStartRecipe(startRecipe);
            startCassette.setLotInCassetteList(Arrays.asList(lotInCassette));
            tmpStartCassette.add(startCassette);
        }
        log.info("Call equipment_FillInTxTRC004__101()");
        Results.MoveOutReqResult fillInOperationCompOut = equipmentMethod.equipmentFillInTxTRC004(objCommon, tmpStartCassette, tmpSpcCheckLotList);

        int opeCompLotSize = CimArrayUtils.getSize(fillInOperationCompOut.getMoveOutLot());
        log.info(" OperationCompleteLot.size : {}", opeCompLotSize);
        List<Infos.OpeCompLot> operationCompleteLot = fillInOperationCompOut.getMoveOutLot();
        String eqpMonitorSwitchStr = StandardProperties.OM_AUTOMON_FLAG.getValue();
        if (CimStringUtils.equals("1", eqpMonitorSwitchStr)) {
            log.info("1 == SP_EQPMONITOR_SWITCH");
            for (int iCnt = 0; iCnt < opeCompLotSize; iCnt++) {
                log.info("loop to strEquipment_FillInTxTRC004_out.strMoveOutReqResult.strOpeCompLot.size() ： {}", iCnt);
                String lotLotTypeOut = lotMethod.lotTypeGet(objCommon, operationCompleteLot.get(iCnt).getLotID());

                if (CimStringUtils.equals(BizConstant.SP_LOT_TYPE_EQUIPMENTMONITORLOT, lotLotTypeOut)
                        || CimStringUtils.equals(BizConstant.SP_LOT_TYPE_DUMMYLOT, lotLotTypeOut)) {
                    log.info("strLot_lotType_Get_out.lotType is Auto Monitor or Dummy");
                    if (!CimStringUtils.equals(BizConstant.SP_SPECCHECKRESULT_OK, operationCompleteLot.get(iCnt).getSpcCheckResult())
                            && CimStringUtils.isNotEmpty(operationCompleteLot.get(iCnt).getSpcCheckResult())
                            && !CimStringUtils.equals(BizConstant.SP_SPECCHECKRESULT_1ASTERISK, operationCompleteLot.get(iCnt).getSpcCheckResult())
                            && !CimStringUtils.equals(BizConstant.SP_SPECCHECKRESULT_ERROR, operationCompleteLot.get(iCnt).getSpcCheckResult())) {
                        log.info("Call eqpMonitorJob_lot_Update");
                        equipmentMethod.eqpMonitorJobLotUpdate(objCommon, operationCompleteLot.get(iCnt).getLotID(), BizConstant.SP_EQPMONITORJOB_OPECATEGORY_SPECCHECK);
                    }
                }
            }
        }

        // add pilot run
        pilotRunMethod.checkPilotRunFail(objCommon, operationCompleteLot);
        return null;
    }

    @Override
    public Results.EDCByPJRptResult sxEDCByPJRpt(Infos.ObjCommon objCommon, Params.EDCByPJRptInParms params) {
        Results.EDCByPJRptResult outObject = new Results.EDCByPJRptResult();

        ObjectIdentifier controlJobID = (params != null) ? params.getControlJobID() : null;
        ObjectIdentifier equipmentID = (params != null) ? params.getEquipmentID() : null;

        //Input Data Check;
        int dcItemListCount = (params != null) ? CimArrayUtils.getSize(params.getCollectedDataItemList()) : 0;
        if (dcItemListCount == 0) {
            return outObject;
        }
        //Pick up lot IDs;
        List<ObjectIdentifier> lotIDs = new ArrayList<>();
        int nLotCount = 0;
        for (int i = 0; i < dcItemListCount; i++) {
            Infos.CollectedDataItemStruct collectedDataItem = params.getCollectedDataItemList().get(i);
            boolean findFlag = false;
            for (int j = 0; j < nLotCount; j++) {
                ObjectIdentifier lotID = lotIDs.get(j);
                if (ObjectIdentifier.equalsWithValue(collectedDataItem.getLotID(), lotID)) {
                    findFlag = true;
                    break;
                }
            }
            if (!findFlag) {
                lotIDs.add(collectedDataItem.getLotID());
                nLotCount++;
            }
        }

        for (int i = 0; i < nLotCount; i++) {
            ObjectIdentifier lotID = lotIDs.get(i);
            if (lotID == null) {
                continue;
            }
            //Lock Lot Object;
            objectLockMethod.objectLock(objCommon, CimLot.class, lotID);
            //Check controlJob validity between input CJ and Lot actual CJ.;
            ObjectIdentifier lotControlJobIDRetCode = lotMethod.lotControlJobIDGet(objCommon, lotID);
            Validations.check(!ObjectIdentifier.equalsWithValue(lotControlJobIDRetCode, controlJobID), retCodeConfig.getInvalidInputParam());
        }

        //Check if lot is in processing with equipmentID ;
        List<Infos.EqpInprocessingControlJob> eqpInprocessingControlJobs = equipmentMethod.equipmentInprocessingControlJobInfoGetDR(objCommon, equipmentID);
        int nInprocessingCJCount = CimArrayUtils.getSize(eqpInprocessingControlJobs);
        boolean foundFlag = false;
        for (int i = 0; i < nLotCount; i++) {
            ObjectIdentifier lotID = lotIDs.get(i);
            foundFlag = false;
            for (int j = 0; j < nInprocessingCJCount; j++) {
                Infos.EqpInprocessingControlJob inprocessingControlJob = eqpInprocessingControlJobs.get(j);
                if (inprocessingControlJob == null) {
                    continue;
                }
                List<Infos.EqpInprocessingLot> eqpInprocessingLots = inprocessingControlJob.getEqpInprocessingLotList();
                int nInprocessingLotCount = CimArrayUtils.getSize(eqpInprocessingLots);
                for (int k = 0; k < nInprocessingLotCount; k++) {
                    Infos.EqpInprocessingLot eqpInprocessingLot = eqpInprocessingLots.get(k);
                    if (eqpInprocessingLot == null) {
                        continue;
                    }
                    ObjectIdentifier leqpInprocessingLotID = eqpInprocessingLot.getLotID();
                    if (ObjectIdentifier.equalsWithValue(leqpInprocessingLotID, lotID)) {
                        foundFlag = true;
                        break;
                    }
                }
                if (foundFlag) {
                    break;
                }
            }
            if (!foundFlag) {
                break;
            }
        }

        Validations.check(!foundFlag, retCodeConfig.getInvalidInputParam());

        //Check Control Job Status;
        Outputs.ObjControlJobStatusGetOut controlJobStatusOut = controlJobMethod.controlJobStatusGet(objCommon, controlJobID);
        if (!CimStringUtils.equals(controlJobStatusOut.getControlJobStatus(), BizConstant.SP_CONTROLJOBSTATUS_EXECUTING)) {
            throw new ServiceException(new OmCode(retCodeConfig.getInvalidControlJobActionForCJStatus(), controlJobStatusOut.getControlJobStatus(), ObjectIdentifier.fetchValue(controlJobID)));
        }

        Inputs.CollectedDataCheckConditionForDataStoreIn inputs = new Inputs.CollectedDataCheckConditionForDataStoreIn();
        ObjectIdentifier inputControlJobID = new ObjectIdentifier(controlJobID.getValue(), controlJobID.getReferenceKey());
        inputs.setControlJobID(inputControlJobID);
        List<Infos.CollectedDataItemStruct> collectedDataItemList = new ArrayList<>();
        if (params != null) {
            if (CimArrayUtils.getSize(params.getCollectedDataItemList()) > 0) {
                collectedDataItemList.addAll(params.getCollectedDataItemList());
            }
        }
        inputs.setCollectedDataItemList(collectedDataItemList);
        dataCollectionMethod.collectedDataCheckConditionForDataStore(objCommon, inputs);

        //Check measurement type ;
        for (int i = 0; i < dcItemListCount; i++) {
            Infos.CollectedDataItemStruct dcItem = params.getCollectedDataItemList().get(i);
            if (dcItem == null) {
                continue;
            }

            //Measurement type should be "Process Wafer", "Process Wafer Site" or "Process Job".
            if (!CimStringUtils.equals(dcItem.getMeasurementType(), BizConstant.SP_DCDEF_MEAS_PJ) &&
                    !CimStringUtils.equals(dcItem.getMeasurementType(), BizConstant.SP_DCDEF_MEAS_PJWAFER) &&
                    !CimStringUtils.equals(dcItem.getMeasurementType(), BizConstant.SP_DCDEF_MEAS_PJWAFERSITE)) {
                throw new ServiceException(new OmCode(retCodeConfig.getLotInvalidCollectData(), dcItem.getLotID().getValue(), dcItem.getDataCollectionItemName(), dcItem.getMeasurementType()));
            }
        }

        processMethod.processOperationRawDCItemsSet(objCommon, params.getCollectedDataItemList());
        //Set out structure;
        outObject.setLotIDs(lotIDs);
        return outObject;
    }

    @Override
    public void sxDChubDataSendCompleteRpt(Infos.ObjCommon strObjCommonIn, Params.DChubDataSendCompleteRptInParam strDChubDataSendCompleteRptInParam) {
        if (0 == CimStringUtils.length(strDChubDataSendCompleteRptInParam.getEquipmentID().getValue()) ||
                0 == CimStringUtils.length(strDChubDataSendCompleteRptInParam.getControlJobID().getValue())) {
            log.info("" + "Input parameter equipmentID or controlJobID is blank");
            Validations.check(true, retCodeConfigEx.getBlankInputParameter());
        }

        log.info("" + "call equipmentInprocessingControlJobInfoGet()");
        Infos.EqpInprocessingControlJobInfo strEquipemtInprocessingControlJobInfoGetOut;
        strEquipemtInprocessingControlJobInfoGetOut = equipmentMethod.equipmentInprocessingControlJobInfoGet(
                strObjCommonIn,
                strDChubDataSendCompleteRptInParam.getEquipmentID());

        int nLen = CimArrayUtils.getSize(strEquipemtInprocessingControlJobInfoGetOut.getStrEqpInprocessingControlJob());
        Boolean existFlag = false;
        for (int i = 0; i < nLen; i++) {
            log.info("" + "controlJobID " + strEquipemtInprocessingControlJobInfoGetOut.getStrEqpInprocessingControlJob().get(i).getControlJobID().getValue());
            if (CimStringUtils.equals(strDChubDataSendCompleteRptInParam.getControlJobID().getValue(),
                    strEquipemtInprocessingControlJobInfoGetOut.getStrEqpInprocessingControlJob().get(i).getControlJobID().getValue())) {
                existFlag = true;
                break;
            }
        }
        if (!existFlag) {
            log.info("" + "The combination of control job / equipment is not valid. ");

            Validations.check(true, retCodeConfig.getControlJobEqpUnmatch());
        }


    }

    @Override
    public void sxEDCDataUpdateForLotReq(Infos.ObjCommon strObjCommonIn, Params.EDCDataUpdateForLotReqInParm strEDCDataUpdateForLotReqInParm, String claimMemo) {
        Infos.ProcessCollectedDataUpdateIn strProcessCollectedDataUpdateIn = new Infos.ProcessCollectedDataUpdateIn();
        strProcessCollectedDataUpdateIn.setLotID(strEDCDataUpdateForLotReqInParm.getLotID());
        strProcessCollectedDataUpdateIn.setControlJobID(strEDCDataUpdateForLotReqInParm.getControlJobID());
        strProcessCollectedDataUpdateIn.setStrDCDef(strEDCDataUpdateForLotReqInParm.getStrDCDef());
        strProcessCollectedDataUpdateIn.setClaimMemo(claimMemo);
        // step1 - object_Lock
        objectLockMethod.objectLock(strObjCommonIn, CimLot.class, strEDCDataUpdateForLotReqInParm.getLotID());

        // step2 - process_collectedData_Update
        dataCollectionMethod.processCollectedDataUpdate(
                strObjCommonIn,
                strProcessCollectedDataUpdateIn);

    }

    @Override
    public Results.CollectedDataActionByPostProcReqResult sxEDCWithSpecCheckActionByPostTaskReq(Infos.ObjCommon objCommon,
                                                                                                Params.EDCWithSpecCheckActionByPostTaskReqParams params) {
        Results.CollectedDataActionByPostProcReqResult result = new Results.CollectedDataActionByPostProcReqResult();
        //---------------------------------//
        // Initialize variable             //
        //---------------------------------//
        int i = 0, j = 0;   // For loop counter.
        int nCastLen = 0;      // For StartCassetteLen
        int nLotCastLen = 0;      // For LotInCassetteLen
        int nDCDefLen = 0;      // For DCDefLen
        int nDCitemLen = 0;      // For DCItemLen
        int dcDefIndexNum = 0;
        int dcItemIndexNum = 0;

        List<Infos.StartCassette> tmpStartCassette = new ArrayList<>();
        boolean specCheckRequiredFlag = false;   // for Spec Check
        boolean spcCheckRequiredFlag = false;   // for SPC  Check

        /*---------------------------------------------------------------------------------*/
        /* author: zqi                                                                     */
        /* date: 2021/7/3 16:00:00                                                         */
        /* 添加specConfigSaveRequiredFlag，来判断在做DC Check之前是否需要保存DC Spec数据        */
        /*---------------------------------------------------------------------------------*/
        boolean specConfigSaveRequiredFlag = false;

        //--------------------------------------------------
        // check input parameter
        //--------------------------------------------------
        if (ObjectIdentifier.isEmptyWithValue(params.getEquipmentID())) {
            log.info("The input parameter is not specified.equipmentID is Blank");
            Validations.check(retCodeConfig.getInvalidInputParam());
        }
        if (ObjectIdentifier.isEmptyWithValue(params.getControlJobID())) {
            log.info("The input parameter is not specified.controlJobID is Blank");
            Validations.check(retCodeConfig.getInvalidInputParam());
        }
        if (ObjectIdentifier.isEmptyWithValue(params.getLotID())) {
            log.info("The input parameter is not specified.lotID is Blank");
            Validations.check(retCodeConfig.getInvalidInputParam());
        }

        /*=============================================================================*/
        /*                                                                             */
        /* Check Process                                                               */
        /*                                                                             */
        /*=============================================================================*/
        log.info("There is no StartCassette length of inparmeter. Try to get StartCassette with previous operation.");
        //-----------------------------------------------------------------------//
        //   Get Started Lot information which is specified with ControlJob ID   //
        //-----------------------------------------------------------------------//

        // 判断 EDC 信息是取当前工步还是前一个工步的数据
        final boolean checkConditionForPO = lotMethod.lotCheckConditionForPO(objCommon, params.getLotID());
        if (checkConditionForPO) {
            // 调用 lotCurrentOperationDataCollectionInformationGet 方法
            final Outputs.ObjLotCurrentOperationDataCollectionInformationGetOut dataCollectionInformationGetOut =
                    lotMethod.lotCurrentOperationDataCollectionInformationGet(objCommon,
                            params.getEquipmentID(),
                            Collections.singletonList(params.getLotID()));
            tmpStartCassette = null != dataCollectionInformationGetOut ?
                    dataCollectionInformationGetOut.getStrStartCassette() :
                    tmpStartCassette;
        } else {
            Outputs.ObjLotPreviousOperationDataCollectionInformationGetOut dataCollectionInformationGetOutForPrevious =
                    lotMethod.lotPreviousOperationDataCollectionInformationGet(objCommon,
                            params.getEquipmentID(),
                            Collections.singletonList(params.getLotID()));
            tmpStartCassette = null != dataCollectionInformationGetOutForPrevious ?
                    dataCollectionInformationGetOutForPrevious.getStrStartCassette() :
                    tmpStartCassette;
        }

        nCastLen = tmpStartCassette.size();

        //Recheck StartCassette length.
        Validations.check(CimArrayUtils.isEmpty(tmpStartCassette), retCodeConfig.getNotFoundCassette(), "*****");

        //Get PostProcForLotFlag from thread specific data
        Boolean bParallelPostProcFlag = false;

        String strParallelPostProcFlag = ThreadContextHolder.getThreadSpecificDataString(
                BizConstant.SP_THREADSPECIFICDATA_KEY_POSTPROCPARALLELFLAG);
        if (CimStringUtils.equals(strParallelPostProcFlag, BizConstant.SP_POSTPROCESS_PARALLELEXECUTION_ON)) {
            bParallelPostProcFlag = true;
        }
        /*=============================================================================*/
        /*                                                                             */
        /* Object Lock Process                                                         */
        /*                                                                             */
        /*=============================================================================*/
        for (Infos.StartCassette startCassette : tmpStartCassette) {
            if (CimArrayUtils.isNotEmpty(startCassette.getLotInCassetteList())) {
                for (Infos.LotInCassette lotInCassette : startCassette.getLotInCassetteList()) {
                    //---------------------------//
                    //   Omit Not-OpeStart Lot   //
                    //---------------------------//
                    if (CimBooleanUtils.isFalse(lotInCassette.getMoveInFlag())) {
                        log.info("lotInCassette.getMoveInFlag() == FALSE");
                        continue;
                    }

                    //---------------------//
                    //   Lock Lot Object   //
                    //---------------------//
                    objectLockMethod.objectLock(objCommon, CimLot.class, lotInCassette.getLotID());

                    //-------------------------------//
                    //  Set Spec Check Required Flag //
                    //-------------------------------//
                    if (CimBooleanUtils.isFalse(specCheckRequiredFlag)) {
                        nDCDefLen = CimArrayUtils.getSize(lotInCassette.getStartRecipe().getDcDefList());
                        if (nDCDefLen > 0) {
                            for (Infos.DataCollectionInfo dataCollectionInfo : lotInCassette.getStartRecipe().getDcDefList()) {
                                if (CimBooleanUtils.isTrue(dataCollectionInfo.getSpecCheckRequiredFlag())) {
                                    specCheckRequiredFlag = true;
                                }
                                /*---------------------------------------------------------------------------------*/
                                /* author: zqi                                                                     */
                                /* date: 2021/7/3 16:00:00                                                         */
                                /* 添加specConfigSaveRequiredFlag，来判断在做DC Check之前是否需要保存DC Spec数据        */
                                /*---------------------------------------------------------------------------------*/
                                if (CimArrayUtils.isEmpty(dataCollectionInfo.getDcSpecs())) {
                                    specConfigSaveRequiredFlag = true;
                                }
                                break;
                            }
                        }
                    }

                    if (CimBooleanUtils.isTrue(bParallelPostProcFlag)) {
                        //PostProcess parallel execution
                        log.info("Check monitor group existence.");
                        List<Infos.MonitorGroups> monitorGroups = monitorGroupMethod.monitorGroupGetDR(objCommon, lotInCassette.getLotID());

                        //--------------------------------------------------//
                        //   Lock MonitorGroup to keep data inconsistency   //
                        //--------------------------------------------------//
                        if (CimArrayUtils.isNotEmpty(monitorGroups)) {
                            for (Infos.MonitorGroups monitorGroup : monitorGroups) {
                                if (ObjectIdentifier.equalsWithValue(lotInCassette.getLotID(), monitorGroup.getMonitorLotID())) {
                                    log.info("lotID matches.");
                                    objectLockMethod.objectLock(objCommon, CimMonitorGroup.class, monitorGroup.getMonitorLotID());

                                    List<Infos.MonitoredLots> strMonitoredLots = monitorGroup.getStrMonitoredLots();
                                    //--------------------------------------------//
                                    //   Lock Monitored lots to avoid dead lock   //
                                    //--------------------------------------------//
                                    if (CimArrayUtils.isNotEmpty(strMonitoredLots)) {
                                        for (Infos.MonitoredLots strMonitoredLot : strMonitoredLots) {
                                            log.info("Monitored LotID: {}", ObjectIdentifier.fetchValue(strMonitoredLot.getLotID()));
                                            objectLockMethod.objectLock(objCommon, CimLot.class, strMonitoredLot.getLotID());
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        /*=============================================================================*/
        /*                                                                             */
        /* Check Reported PJ Data Item                                                 */
        /*                                                                             */
        /*=============================================================================*/
        Boolean bPJDataExistFlag = false;
        log.info("calling processOperationDataConditionGetDR()");
        Outputs.ObjProcessOperationDataConditionGetDrOut processOperationDataConditionGetDrOut =
                processMethod.processOperationDataConditionGetDR(objCommon,
                        params.getLotID(),
                        BizConstant.SP_DATACONDITIONCATEGORY_SPECCHECKRESULT);

        if (null != processOperationDataConditionGetDrOut && 0 < processOperationDataConditionGetDrOut.getCount()) {
            log.info("0 < processOperationDataConditionGetDrOut.count");
            bPJDataExistFlag = true;
        }
        log.info("bPJDataExistFlag: {}", bPJDataExistFlag);

        List<Infos.DataCollectionInfo> strInitDCDefSeq = tmpStartCassette
                .get(0).getLotInCassetteList()
                .get(0).getStartRecipe()
                .getDcDefList();
        nDCDefLen = CimArrayUtils.getSize(strInitDCDefSeq);

        /*=============================================================================*/
        /*                                                                             */
        /* Get Expand Derived Data Item                                                */
        /*                                                                             */
        /*=============================================================================*/
        log.info("calling processOperationDataConditionGetDR()");
        Outputs.ObjProcessOperationDataConditionGetDrOut processOperationExpandDerivedDataConditionGetDrOut =
                processMethod.processOperationDataConditionGetDR(objCommon,
                        params.getLotID(),
                        BizConstant.SP_DATACONDITIONCATEGORY_EXPANDDERIVEDDATA);

        List<Infos.DataCollectionInfo> strExpandDerivedDCDef = processOperationExpandDerivedDataConditionGetDrOut.getExpandDerivedDCDefList();
        int nExpandDCDefLen = CimArrayUtils.getSize(strExpandDerivedDCDef);
        log.info("nExpandDCDefLen: {}", nExpandDCDefLen);

        List<List<Integer>> specCheckIndexSeq = new ArrayList<>();
        int specCheckIndexLen = nDCDefLen;

        //-------------------------------//
        //  Set Spec Check Required Flag //
        //-------------------------------//

        //List of WaferID
        Map<String, ObjectIdentifier> waferIDList = new HashMap<>();
        for (int nDCDefNum = 0; nDCDefNum < specCheckIndexLen; nDCDefNum++) {
            int nChkd = 0;
            int nExpandDCItemLen = 0;
            int nExpandDCDefNum = 0;
            for (nExpandDCDefNum = 0; nExpandDCDefNum < nExpandDCDefLen; nExpandDCDefNum++) {
                Infos.DataCollectionInfo dataCollectionInfo = strExpandDerivedDCDef.get(nExpandDCDefNum);
                log.info("{} nExpandDCDefNum", nExpandDCDefNum);
                if (ObjectIdentifier.equalsWithValue(strInitDCDefSeq.get(nDCDefNum).getDataCollectionDefinitionID(),
                        dataCollectionInfo.getDataCollectionDefinitionID())) {
                    log.info("strInitDCDefSeq[nDCDefNum].dataCollectionDefinitionID == strExpandDerivedDCDef[nExpandDCDefNum].dataCollectionDefinitionID");
                    nExpandDCItemLen = dataCollectionInfo.getDcItems().size();
                    break;
                }
            }

            nDCitemLen = CimArrayUtils.getSize(strInitDCDefSeq.get(nDCDefNum).getDcItems());

            int derivedIndexCnt = 0;
            List<Integer> derivedIndexSeq = new ArrayList<>();

            int nDCItemCnt = 0;
            int nExpandDCItemCurrPos = 0;
            String expandCurrDCItemName = "";
            String expandCurrMeasType = "";
            String expandCurrSitePos = "";
            List<Integer> specCheckIndexSeqInner = new ArrayList<>();
            specCheckIndexSeq.add(specCheckIndexSeqInner);
            for (int nDCItemNum = 0; nDCItemNum < nDCitemLen; nDCItemNum++) {
                boolean bCheckedFlag = false;
                Infos.DataCollectionItemInfo dcItem = strInitDCDefSeq.get(nDCDefNum).getDcItems().get(nDCItemNum);
                if (CimStringUtils.equals(BizConstant.SP_DCDEF_MEAS_PJ, dcItem.getMeasurementType())
                        || CimStringUtils.equals(BizConstant.SP_DCDEF_MEAS_PJWAFER, dcItem.getMeasurementType())
                        || CimStringUtils.equals(BizConstant.SP_DCDEF_MEAS_PJWAFERSITE, dcItem.getMeasurementType())) {
                    log.info("DCItem by PJ");
                    /*=============================================================================*/
                    /*                                                                             */
                    /* Expand Derived Data Item                                                    */
                    /*                                                                             */
                    /*=============================================================================*/
                    if (0 < nExpandDCItemLen
                            && (CimStringUtils.equals(BizConstant.SP_DCDEF_ITEM_DERIVED, dcItem.getItemType())
                            || CimStringUtils.equals(BizConstant.SP_DCDEF_ITEM_USERFUNCTION, dcItem.getItemType()))) {
                        log.info("0 < nExpandDCItemLen and itemType == Derived");

                        if (CimStringUtils.isEmpty(expandCurrDCItemName)) {
                            log.info("expandCurrDCItemName is empty");
                            expandCurrDCItemName = dcItem.getDataCollectionItemName();
                            expandCurrMeasType = dcItem.getMeasurementType();
                            expandCurrSitePos = dcItem.getSitePosition();
                        }

                        if (CimStringUtils.equals(expandCurrDCItemName, dcItem.getDataCollectionItemName())
                                && CimStringUtils.equals(expandCurrMeasType, dcItem.getMeasurementType())
                                && CimStringUtils.equals(expandCurrSitePos, dcItem.getSitePosition())) {
                            log.info("Start of the expansion");

                            int nExpandDCItemCnt = 0;
                            for (nExpandDCItemCnt = nExpandDCItemCurrPos; nExpandDCItemCnt < nExpandDCItemLen; nExpandDCItemCnt++) {
                                log.info("nExpandDCItemLen: {}/nExpandDCItemCnt: {}", nExpandDCItemLen, nExpandDCItemCnt);
                                Infos.DataCollectionItemInfo dataCollectionItemInfo = strExpandDerivedDCDef.get(nExpandDCDefNum).getDcItems().get(nExpandDCItemCnt);
                                if (CimStringUtils.equals(expandCurrDCItemName, dataCollectionItemInfo.getDataCollectionItemName())
                                        && CimStringUtils.equals(expandCurrMeasType, dataCollectionItemInfo.getMeasurementType())
                                        && CimStringUtils.equals(expandCurrSitePos, dataCollectionItemInfo.getSitePosition())) {
                                    log.info("Add Expand Item {} , {} , {}", expandCurrDCItemName, expandCurrMeasType, expandCurrSitePos);
                                    strInitDCDefSeq.get(nDCDefNum).getDcItems().set(nDCItemCnt, dataCollectionItemInfo);
                                    nChkd++;
                                    nDCItemCnt++;
                                } else {
                                    log.info("End of the expansion");
                                    expandCurrDCItemName = dataCollectionItemInfo.getDataCollectionItemName();
                                    expandCurrMeasType = dataCollectionItemInfo.getMeasurementType();
                                    expandCurrSitePos = dataCollectionItemInfo.getSitePosition();
                                    break;
                                }
                            } // nExpandDCItemCnt loop end
                            nExpandDCItemCurrPos = nExpandDCItemCnt;
                        }
                    }

                    Infos.DataCollectionItemInfo initDcDefSeqDcItem = strInitDCDefSeq.get(nDCDefNum).getDcItems().get(nDCItemNum);
                    if ((CimStringUtils.equals(BizConstant.SP_DCDEF_ITEM_DERIVED, initDcDefSeqDcItem.getItemType())
                            || CimStringUtils.equals(BizConstant.SP_DCDEF_ITEM_USERFUNCTION, initDcDefSeqDcItem.getItemType()))
                            && CimStringUtils.equals(initDcDefSeqDcItem.getWaferPosition(), "*")) {
                        log.info("itemType == Derived and waferPosition == '*'");
                        continue;
                    }
                    // todo:zqi 当Items过多后，该代码逻辑会耗性能，考虑该代码是否可以移除
                    strInitDCDefSeq.get(nDCDefNum).getDcItems().set(nDCItemCnt, strInitDCDefSeq.get(nDCDefNum).getDcItems().get(nDCItemNum));

                    if (CimStringUtils.isNotEmpty(strInitDCDefSeq.get(nDCDefNum).getDcItems().get(nDCItemCnt).getSpecCheckResult())) {
                        log.info("specCheckResult.length > 0");
                        bCheckedFlag = true;
                    }
                }
                // DCItem by CJ
                else {
                    log.info("DCItem by CJ");
                    // todo:zqi 当Items过多后，该代码逻辑会耗性能，考虑该代码是否可以移除
                    strInitDCDefSeq.get(nDCDefNum).getDcItems().set(nDCItemCnt, strInitDCDefSeq.get(nDCDefNum).getDcItems().get(nDCItemNum));
                }
                if (CimBooleanUtils.isFalse(bCheckedFlag)) {
                    log.info("FALSE == bCheckedFlag");
                    specCheckIndexSeqInner.add(nDCItemCnt);
                    nChkd++;
                }

                Infos.DataCollectionItemInfo dataCollectionItemInfo = strInitDCDefSeq.get(nDCDefNum).getDcItems().get(nDCItemCnt);
                if (CimStringUtils.isNotEmpty(dataCollectionItemInfo.getWaferPosition())
                        && CimStringUtils.unEqual(BizConstant.SP_DCDEF_MEAS_PJ, dataCollectionItemInfo.getMeasurementType())) {
                    log.info("0 < waferPosition and measurementType != SP_DCDef_Meas_PJ");

                    // DCDef
                    if (0 == nDCDefNum) {
                        log.info("0 == nDCDefNum");
                        if ((CimStringUtils.equals(BizConstant.SP_DCDEF_ITEM_DERIVED, dataCollectionItemInfo.getItemType())
                                || CimStringUtils.equals(BizConstant.SP_DCDEF_ITEM_USERFUNCTION, dataCollectionItemInfo.getItemType()))
                                && ObjectIdentifier.isEmptyWithValue(dataCollectionItemInfo.getWaferID())) {
                            log.info("Add derivedIndexSeq: {}", nDCItemNum);
                            derivedIndexSeq.add(derivedIndexCnt++, nDCItemNum);
                        } else if (CimStringUtils.equals(BizConstant.SP_DCDEF_ITEM_RAW, dataCollectionItemInfo.getItemType())
                                && ObjectIdentifier.isNotEmptyWithValue(dataCollectionItemInfo.getWaferID())) {
                            log.info("itemType == Raw && 0 < waferID");
                            if (ObjectIdentifier.isEmptyWithValue(waferIDList.get(dataCollectionItemInfo.getWaferPosition()))) {
                                log.info("Add aProcessWaferDeltaPOList: {} {}", dataCollectionItemInfo.getWaferPosition(),
                                        ObjectIdentifier.fetchValue(dataCollectionItemInfo.getWaferID()));
                                waferIDList.putIfAbsent(dataCollectionItemInfo.getWaferPosition(), dataCollectionItemInfo.getWaferID());
                            }
                        }
                    }

                    //Delta
                    else {
                        log.info("0 != nDCDefNum");
                        if (CimStringUtils.equals(BizConstant.SP_DCDEF_ITEM_DERIVED, dataCollectionItemInfo.getItemType())
                                || CimStringUtils.equals(BizConstant.SP_DCDEF_ITEM_USERFUNCTION, dataCollectionItemInfo.getItemType())
                                && ObjectIdentifier.isEmptyWithValue(dataCollectionItemInfo.getWaferID())) {
                            log.info("itemType == Derived && null == waferID");
                            String aWaferID;
                            if (ObjectIdentifier.isEmptyWithValue(waferIDList.get(dataCollectionItemInfo.getWaferPosition()))) {
                                aWaferID = ObjectIdentifier.fetchValue(waferIDList.get(dataCollectionItemInfo.getWaferPosition()));
                                log.info("Set WaferID: {} {} {}", dataCollectionItemInfo.getDataCollectionItemName(),
                                        dataCollectionItemInfo.getWaferPosition(), aWaferID);
                                dataCollectionItemInfo.setWaferID(ObjectIdentifier.buildWithValue(aWaferID));
                            }
                        }
                    }
                }
                nDCItemCnt++;
            }

            // DCDef
            if (0 == nDCDefNum) {
                log.info("0 == nDCDefNum");
                for (int derivedIndexNum = 0; derivedIndexNum < derivedIndexCnt; derivedIndexNum++) {
                    log.info("derivedIndexCnt: {}/ derivedIndexNum: {}", derivedIndexCnt, derivedIndexNum);
                    nDCItemCnt = derivedIndexSeq.get(derivedIndexNum);
                    String aWaferID;
                    Infos.DataCollectionItemInfo dataCollectionItemInfo = strInitDCDefSeq.get(nDCDefNum).getDcItems().get(nDCItemCnt);
                    if (ObjectIdentifier.isNotEmptyWithValue(waferIDList.get(dataCollectionItemInfo.getWaferPosition()))) {
                        aWaferID = ObjectIdentifier.fetchValue(waferIDList.get(dataCollectionItemInfo.getWaferPosition()));
                        log.info("Set WaferID: {} {} {}",
                                dataCollectionItemInfo.getDataCollectionItemName(),
                                dataCollectionItemInfo.getWaferPosition(),
                                aWaferID);
                        dataCollectionItemInfo.setWaferID(ObjectIdentifier.buildWithValue(aWaferID));
                    }
                }
            }
        }

        /*=============================================================================*/
        /*                                                                             */
        /* Main Process                                                                */
        /*                                                                             */
        /*=============================================================================*/

        log.info("specCheckRequiredFlag is {}", specCheckRequiredFlag);
        //----------------------------------------------//
        //   If specCheckRequiredFlag is TRUE           //
        //   Call SpecCheck                             //
        //----------------------------------------------//
        if (CimBooleanUtils.isTrue(specCheckRequiredFlag)) {
            log.info("Do Spec Check");
            Results.SpecCheckReqResult strDataSpecCheckReqResult = new Results.SpecCheckReqResult();
            //-------------------------------------------------------------------------------------------//
            //   Do Spec Check                                                                           //
            //      - Calculate the derived value and Set them to PO if calculationRequiredFlag is TRUE  //
            //      - Set spec  check result to PO if speckCheckRequiredFlag is TRUE.                    //
            //-------------------------------------------------------------------------------------------//
            for (i = 0; i < nCastLen; i++) {
                nLotCastLen = CimArrayUtils.getSize(tmpStartCassette.get(i).getLotInCassetteList());
                log.info("tmpStartCassette.get(i).getLotInCassetteList() size: {}", nLotCastLen);

                for (j = 0; j < nLotCastLen; j++) {
                    //---------------------------//
                    //   Omit Not-OpeStart Lot   //
                    //---------------------------//
                    Infos.LotInCassette lotInCassette = tmpStartCassette.get(i).getLotInCassetteList().get(j);
                    if (CimBooleanUtils.isFalse(lotInCassette.getMoveInFlag())) {
                        log.info("moveInFlag == FALSE");
                        continue;
                    }

                    //--------------------------------------------------------------------------------------//
                    // Control Job is deleted for the logic of new post-processing by OpeComp.              //
                    // Therefore, Control Job cannot be obtained from Lot.                                  //
                    // In this case, Control Job of equipment is compared with Control Job of PreviousPO.   //
                    //--------------------------------------------------------------------------------------//
                    Outputs.ObjLotPreviousOperationInfoGetOut lotPreviousOperationInfoGetOut;
                    Outputs.ObjLotCurrentOperationInfoGetOut lotCurrentOperationInfoGetOut;
                    ObjectIdentifier tmpControlJobID;
                    try {
                        if (checkConditionForPO) {
                            lotCurrentOperationInfoGetOut = lotMethod.lotCurrentOperationInfoGet(objCommon, lotInCassette.getLotID());
                            tmpControlJobID = lotCurrentOperationInfoGetOut.getControlJobID();
                        } else {
                            lotPreviousOperationInfoGetOut = lotMethod.lotPreviousOperationInfoGet(objCommon, lotInCassette.getLotID());
                            tmpControlJobID = lotPreviousOperationInfoGetOut.getControlJobID();
                        }
                    } catch (ServiceException e) {
                        if (CimArrayUtils.isNotEmpty(specCheckIndexSeq)) {
                            specCheckIndexSeq.clear();
                        }
                        throw e;
                    }
                    if (ObjectIdentifier.isEmpty(tmpControlJobID)) {
                        log.info("Previous PO controlJobID = {}", ObjectIdentifier.fetchValue(tmpControlJobID));
                        log.info("Equipment   controlJobID = {}", ObjectIdentifier.fetchValue(params.getControlJobID()));

                        if (!ObjectIdentifier.equalsWithValue(tmpControlJobID,
                                params.getControlJobID())) {
                            if (CimArrayUtils.isNotEmpty(specCheckIndexSeq)) {
                                specCheckIndexSeq.clear();
                            }
                            Validations.check(retCodeConfig.getControlJobLotUnmatch(),
                                    ObjectIdentifier.fetchValue(tmpControlJobID),
                                    ObjectIdentifier.fetchValue(params.getControlJobID()));
                        }
                    }
                }
            }


            String dcCheckLevel = StandardProperties.OM_EDC_CHK_LEVEL.getValue();
            String dcCheckPhase = StandardProperties.OM_EDC_ASTERISK_VALUE_CHK_TIME.getValue();
            log.info("dcCheckLevel = {}", dcCheckLevel);
            log.info("dcCheckPhase = {}", dcCheckPhase);
            if (CimStringUtils.unEqual(dcCheckLevel, BizConstant.VALUE_ONE)
                    && CimStringUtils.unEqual(dcCheckLevel, BizConstant.VALUE_TWO)) {
                dcCheckLevel = BizConstant.VALUE_ZERO;
            }
            if (CimStringUtils.unEqual(dcCheckPhase, BizConstant.VALUE_ONE)) {
                dcCheckPhase = BizConstant.VALUE_ZERO;
            }
            log.info("dcCheckLevel = {}", dcCheckLevel);
            log.info("dcCheckPhase = {}", dcCheckPhase);

            /*---------------------------------------------------------------------------------*/
            /* author: zqi                                                                     */
            /* date: 2021/7/3 16:00:00                                                         */
            /* 添加EDC Spec 数据保存逻辑                                                         */
            /* 当 specConfigSaveRequiredFlag == true 时，保存Spec数据到 OMPROPE_EDC_SPECS 表中    */
            /*---------------------------------------------------------------------------------*/
            if (specConfigSaveRequiredFlag) {
                processMethod.processDataCollectionSpecificationSet(
                        objCommon,
                        tmpStartCassette);
            }

            Outputs.ObjDataValueCheckValidityForSpecCheckDrOut dataValueCheckValidityForSpecCheckDrOut = new Outputs.ObjDataValueCheckValidityForSpecCheckDrOut();
            dataValueCheckValidityForSpecCheckDrOut.setEquipmentID(params.getEquipmentID());
            dataValueCheckValidityForSpecCheckDrOut.setControlJobID(params.getControlJobID());
            dataValueCheckValidityForSpecCheckDrOut.setStartCassetteList(tmpStartCassette);
            if (CimStringUtils.unEqual(dcCheckLevel, BizConstant.VALUE_ZERO)          // OM_EDC_CHK_LEVEL is not 0
                    && CimStringUtils.equals(dcCheckPhase, BizConstant.VALUE_ONE))   //  OM_EDC_ASTERISK_VALUE_CHK_TIME is set to 1
            {
                log.info("Do dataValue_CheckValidityForSpecCheck");
                /*--------------------*/
                /*   Validity Check   */
                /*--------------------*/
                try {
                    dataValueCheckValidityForSpecCheckDrOut = dataValueMethod.dataValueCheckValidityForSpecCheckDR(objCommon,
                            params.getEquipmentID(),
                            params.getControlJobID(),
                            tmpStartCassette);
                } catch (ServiceException e) {
                    if (!Validations.isEquals(e.getCode(), retCodeConfig.getAllDataValAsterisk())) {
                        if (CimArrayUtils.isNotEmpty(specCheckIndexSeq)) {
                            specCheckIndexSeq.clear();
                        }
                        throw e;
                    }

                }
            }

            /*-------------------*/
            /*   Do Spec Check   */
            /*-------------------*/
            Results.SpecCheckReqResult specCheckReqResult;
            try {
                specCheckReqResult = specCheckMethod.specCheckFillInDR(objCommon,
                        dataValueCheckValidityForSpecCheckDrOut.getEquipmentID(),
                        dataValueCheckValidityForSpecCheckDrOut.getControlJobID(),
                        dataValueCheckValidityForSpecCheckDrOut.getStartCassetteList());
            } catch (ServiceException e) {
                if (CimArrayUtils.isNotEmpty(specCheckIndexSeq)) {
                    specCheckIndexSeq.clear();
                }
                throw e;
            }

            /*--------------------------------------------------------*/
            /*   Merge already checked data item into StartCassette   */
            /*--------------------------------------------------------*/
            List<Infos.StartCassette> strStartCassetteSpecCheckResult = specCheckReqResult.getStartCassetteList();
            List<Infos.StartCassette> strStartCassetteSource = dataValueCheckValidityForSpecCheckDrOut.getStartCassetteList();

            /*---------------------------------------------------------------------------------*/
            /* author: zqi                                                                     */
            /* date: 2021/7/3 16:00:00                                                         */
            /* 添加 CimStringUtils.unEqual(dcCheckLevel, BizConstant.VALUE_ZERO)               */
            /*        && CimStringUtils.equals(dcCheckPhase, BizConstant.VALUE_ONE) 逻辑判断    */
            /*---------------------------------------------------------------------------------*/
            if (CimBooleanUtils.isTrue(bPJDataExistFlag)
                    && CimStringUtils.unEqual(dcCheckLevel, BizConstant.VALUE_ZERO)
                    && CimStringUtils.equals(dcCheckPhase, BizConstant.VALUE_ONE)) {
                log.info("TRUE == bPJDataExistFlag");
                for (dcDefIndexNum = 0; dcDefIndexNum < specCheckIndexLen; dcDefIndexNum++) {
                    int specCheckIndexCnt = CimArrayUtils.getSize(specCheckIndexSeq.get(dcDefIndexNum));
                    List<Infos.DataCollectionItemInfo> strResultDCItemSeq = strStartCassetteSpecCheckResult
                            .get(0).getLotInCassetteList()
                            .get(0).getStartRecipe().getDcDefList()
                            .get(dcDefIndexNum).getDcItems();
                    List<Infos.DataCollectionItemInfo> strSourceDCItemSeq = strStartCassetteSource
                            .get(0).getLotInCassetteList()
                            .get(0).getStartRecipe().getDcDefList()
                            .get(dcDefIndexNum).getDcItems();
                    for (dcItemIndexNum = 0; dcItemIndexNum < specCheckIndexCnt; dcItemIndexNum++) {
                        Integer sourceIndex = specCheckIndexSeq.get(dcDefIndexNum).get(dcItemIndexNum);
                        if (sourceIndex >= CimArrayUtils.getSize(strSourceDCItemSeq)) {
                            log.info("index >= strDCItem.length()");
                            specCheckIndexSeq.clear();
                            Validations.check(retCodeConfig.getSystemError());
                        }
                        strSourceDCItemSeq.set(sourceIndex, strResultDCItemSeq.get(sourceIndex));
                    }
                }
                strStartCassetteSpecCheckResult = strStartCassetteSource;
            }

            /*-----------------------*/
            /*   Set out structure   */
            /*-----------------------*/
            strDataSpecCheckReqResult.setStartCassetteList(strStartCassetteSpecCheckResult);
            strDataSpecCheckReqResult.setEquipmentID(specCheckReqResult.getEquipmentID());
            strDataSpecCheckReqResult.setControlJobID(specCheckReqResult.getControlJobID());

            /*--------------------------*/
            /*   APC Derived Data       */
            /*--------------------------*/
            //TODO:	APCDataDerivationInq__120, APC Derived Data
            // zqi: 添加此flag判断APC是否执行
            boolean apcExectionRequiredFlag = false;

            //----------------------------------------------------------------
            // Bonding Map Info Get
            //----------------------------------------------------------------
            Outputs.ObjBondingGroupInfoByEqpGetDROut strBondingGroupInfoByEqpGetDROut;
            try {
                strBondingGroupInfoByEqpGetDROut = bondingGroupMethod.bondingGroupInfoByEqpGetDR(
                        objCommon,
                        params.getEquipmentID(), params.getControlJobID(), true);
            } catch (ServiceException e) {
                if (CimArrayUtils.isNotEmpty(specCheckIndexSeq)) {
                    specCheckIndexSeq.clear();
                }
                throw e;
            }

            int topLotLen = CimArrayUtils.getSize(strBondingGroupInfoByEqpGetDROut.getTopLotIDSeq());
            log.info("topLotLen={}", topLotLen);

            if (topLotLen > 0) {
                log.info("There is Top Lot.");
                if (CimArrayUtils.isNotEmpty(strDataSpecCheckReqResult.getStartCassetteList())) {
                    for (Infos.StartCassette startCassette : strDataSpecCheckReqResult.getStartCassetteList()) {
                        if (CimArrayUtils.isNotEmpty(startCassette.getLotInCassetteList())) {
                            for (Infos.LotInCassette lotInCassette : startCassette.getLotInCassetteList()) {
                                //---------------------------//
                                //   Omit Not-OpeStart Lot   //
                                //---------------------------//
                                if (CimBooleanUtils.isFalse(lotInCassette.getMoveInFlag())) {
                                    continue;
                                }

                                boolean isTopLot = false;
                                for (ObjectIdentifier topLot : strBondingGroupInfoByEqpGetDROut.getTopLotIDSeq()) {
                                    if (ObjectIdentifier.equalsWithValue(lotInCassette.getLotID(), topLot)) {
                                        isTopLot = true;
                                        break;
                                    }
                                }
                                if (CimBooleanUtils.isTrue(isTopLot)) {
                                    log.info("Lot is Top Lot {}. Clearing Action.", ObjectIdentifier.fetchValue(lotInCassette.getLotID()));
                                    List<Infos.DataCollectionInfo> strDCDef = lotInCassette.getStartRecipe().getDcDefList();
                                    if (CimArrayUtils.isNotEmpty(strDCDef)) {
                                        for (Infos.DataCollectionInfo dataCollectionInfo : strDCDef) {
                                            if (CimArrayUtils.isNotEmpty(dataCollectionInfo.getDcItems())) {
                                                for (Infos.DataCollectionItemInfo dcItem : dataCollectionInfo.getDcItems()) {
                                                    dcItem.setActionCodes("");
                                                    dcItem.setSpecCheckResult("");
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

            /*----------------------------------*/
            /*   Set APCDerived Data into PO    */
            /*----------------------------------*/
            log.info("Set APCDerived Data into PO...");
            //Step10 - processOperation_tempData_Set, Set APCDerived Data into PO

            /*------------------------------------------------------------------------------------------------*/
            /* author: zqi                                                                                    */
            /* date: 2021/7/3 16:00:00                                                                        */
            /* 添加下面逻辑判断是否需要更新 OMPROPE_EDC/OMPROPE_EDC_ITEMS/OMPROPE_EDC_SPECS信息，该方法消耗性能)     */
            /*------------------------------------------------------------------------------------------------*/
            if (apcExectionRequiredFlag && topLotLen > 0) {
                try {
                    processMethod.processOperationTempDataSet(objCommon,
                            params.getControlJobID(),
                            strDataSpecCheckReqResult.getStartCassetteList());
                } catch (ServiceException e) {
                    if (CimArrayUtils.isNotEmpty(specCheckIndexSeq)) {
                        specCheckIndexSeq.clear();
                    }
                    throw e;
                }
            }

            //----------------------------------------------------------//
            // End of Data Spec Check                                   //
            //----------------------------------------------------------//

            //----------------------------------------------//
            //  Replace ReturnCode for SPCCheckRequired.    //
            //----------------------------------------------//
            OmCode rc = retCodeConfig.getNoNeedSpecCheck();
            if (CimArrayUtils.isNotEmpty(strDataSpecCheckReqResult.getStartCassetteList())) {
                for (Infos.StartCassette startCassette : strDataSpecCheckReqResult.getStartCassetteList()) {
                    if (CimArrayUtils.isNotEmpty(startCassette.getLotInCassetteList())) {
                        for (Infos.LotInCassette lotInCassette : startCassette.getLotInCassetteList()) {
                            //-------------------------
                            // Omit is not MoveIn Lot
                            //-------------------------
                            if (CimBooleanUtils.isFalse(lotInCassette.getMoveInFlag())) {
                                continue;
                            }

                            if (CimArrayUtils.isNotEmpty(lotInCassette.getStartRecipe().getDcDefList())) {
                                for (Infos.DataCollectionInfo dataCollectionInfo : lotInCassette.getStartRecipe().getDcDefList()) {
                                    if (ObjectIdentifier.isEmpty(dataCollectionInfo.getDataCollectionSpecificationID())) {
                                        continue;
                                    }

                                    if (CimArrayUtils.isNotEmpty(dataCollectionInfo.getDcItems())) {
                                        for (Infos.DataCollectionItemInfo dcItem : dataCollectionInfo.getDcItems()) {
                                            if (CimStringUtils.unEqualIn(dcItem.getSpecCheckResult(), "*", "")) {
                                                rc = retCodeConfig.getSucc();
                                                break;
                                            }
                                        }
                                    }
                                    if (rc == retCodeConfig.getSucc()) {
                                        break;
                                    }
                                }
                            }
                            if (rc == retCodeConfig.getSucc()) {
                                break;
                            }
                        }
                    }
                    if (rc == retCodeConfig.getSucc()) {
                        break;
                    }
                }
            }

            if (rc == retCodeConfig.getSucc()) {
                log.info("Data Spec Check = OK");
                spcCheckRequiredFlag = true;
            } else if (rc == retCodeConfig.getNoNeedSpecCheck()) {
                log.info("Data Spec Check == NO_NEED_TO_SPECCHECK");
            } else {
                log.info("Data Spec Check != OK");
                if (CimArrayUtils.isNotEmpty(specCheckIndexSeq)) {
                    specCheckIndexSeq.clear();
                }
                Validations.check(rc);
            }

            //-------------------------------------------------------------------------------//
            //  Get Started Lot information in order to get the derived / delta data values  //
            //-------------------------------------------------------------------------------//
            try {
                // EDC Exectuor 执行位置改动，不调用 lotPreviousOperationDataCollectionInformationGet 方法
                if (checkConditionForPO) {
                    Outputs.ObjLotCurrentOperationDataCollectionInformationGetOut currentEDCInformationGetOut =
                            lotMethod.lotCurrentOperationDataCollectionInformationGet(objCommon,
                                    params.getEquipmentID(),
                                    Collections.singletonList(params.getLotID()));
                    //Reset StartCassette.
                    tmpStartCassette = currentEDCInformationGetOut.getStrStartCassette();
                } else {
                    Outputs.ObjLotPreviousOperationDataCollectionInformationGetOut previousEDCInformationGetOut =
                            lotMethod.lotPreviousOperationDataCollectionInformationGet(objCommon,
                                    params.getEquipmentID(),
                                    Collections.singletonList(params.getLotID()));
                    //Reset StartCassette.
                    tmpStartCassette = previousEDCInformationGetOut.getStrStartCassette();
                }
            } catch (ServiceException e) {
                if (CimArrayUtils.isNotEmpty(specCheckIndexSeq)) {
                    specCheckIndexSeq.clear();
                }
                throw e;
            }
        }


        //-------------------------//
        //                         //
        //   SPC Check Procedure   //
        //                         //
        //-------------------------//
        log.info("spcCheckRequiredFlag is : {}", spcCheckRequiredFlag);
        Results.SPCCheckReqResult strSPCCheckReqResult = new Results.SPCCheckReqResult();
        Outputs.ObjSPCMgrSendSPCCheckReqOut strSPCMgrSendSPCCheckReqOut = new Outputs.ObjSPCMgrSendSPCCheckReqOut();
        if (CimBooleanUtils.isTrue(spcCheckRequiredFlag)) {
            //----------------------------//
            //   Send SPC Check Request   //
            //----------------------------//
            try {
                strSPCMgrSendSPCCheckReqOut = spcMethod.spcMgrSendSPCCheckReq(
                        objCommon,
                        params.getEquipmentID(),
                        params.getControlJobID(),
                        tmpStartCassette);
            } catch (ServiceException e) {
                if (!Validations.isEquals(retCodeConfigEx.getNoNeedToSpcCheck(), e.getCode())) {
                    if (CimArrayUtils.isNotEmpty(specCheckIndexSeq)) {
                        specCheckIndexSeq.clear();
                    }
                    throw e;
                }
            }
            strSPCCheckReqResult = strSPCMgrSendSPCCheckReqOut.getSpcCheckReqResult();
        }

        //---------------------
        // Remove Already checked item from tmpStartCassette
        //---------------------
        List<Infos.StartCassette> strStartCassetteForActionList = new ArrayList<>();

        if (CimBooleanUtils.isTrue(bPJDataExistFlag)) {
            log.info("TRUE == bPJDataExistFlag");
            //---------------------
            // Remove Already checked item from tmpStartCassette
            //---------------------
            strStartCassetteForActionList = tmpStartCassette;
            for (dcDefIndexNum = 0; dcDefIndexNum < specCheckIndexLen; dcDefIndexNum++) {
                log.info("dcDefIndexNum: {}", dcDefIndexNum);
                int specCheckIndexCnt = CimArrayUtils.getSize(specCheckIndexSeq.get(dcDefIndexNum));
                List<Infos.DataCollectionItemInfo> strSourceDCItemSeq = tmpStartCassette
                        .get(0).getLotInCassetteList()
                        .get(0).getStartRecipe().getDcDefList()
                        .get(dcDefIndexNum).getDcItems();
                List<Infos.DataCollectionItemInfo> strTargetDCItemSeq = strStartCassetteForActionList
                        .get(0).getLotInCassetteList()
                        .get(0).getStartRecipe().getDcDefList()
                        .get(dcDefIndexNum).getDcItems();
                for (dcItemIndexNum = 0; dcItemIndexNum < specCheckIndexCnt; dcItemIndexNum++) {
                    log.info("dcItemIndexNum: {}", dcItemIndexNum);
                    int sourceIndex = specCheckIndexSeq.get(dcDefIndexNum).get(dcItemIndexNum);
                    if (sourceIndex >= CimArrayUtils.getSize(strSourceDCItemSeq)) {
                        log.info("index >= strDCItem.size");
                        if (CimArrayUtils.isNotEmpty(specCheckIndexSeq)) {
                            specCheckIndexSeq.clear();
                        }
                        Validations.check(retCodeConfig.getSystemError());
                    }
                    strTargetDCItemSeq.set(dcItemIndexNum, strSourceDCItemSeq.get(sourceIndex));
                }
            }
        }

        if (CimArrayUtils.isNotEmpty(specCheckIndexSeq)) {
            log.info("NULL != specCheckIndexSeq");
            specCheckIndexSeq.clear();
        }

        //-----------------------------------------------------------------//
        //   Summary Action Items to Lot based on the Spec Check Result    //
        //-----------------------------------------------------------------//
        List<Infos.StartCassette> tmpStartCassetteNewAction;
        if (CimBooleanUtils.isTrue(bPJDataExistFlag)) {
            log.info("TRUE == bPJDataExistFlag");
            tmpStartCassetteNewAction = strStartCassetteForActionList;
        } else {
            log.info("TRUE != bPJDataExistFlag");
            tmpStartCassetteNewAction = tmpStartCassette;
        }
        Outputs.ObjStartLotActionListEffectSpecCheckOut strStartLotActionListEffectSpecCheckOut = new Outputs.ObjStartLotActionListEffectSpecCheckOut();

        List<Infos.InterFabMonitorGroupActionInfo> strInterFabMonitorGroupActionInfoSequence = new ArrayList<>();
        List<Results.DCActionLotResult> strDCActionLotResult = new ArrayList<>();
        strStartLotActionListEffectSpecCheckOut = startLotMethod.startLotActionListEffectSpecCheck(strStartLotActionListEffectSpecCheckOut,
                objCommon,
                tmpStartCassetteNewAction,
                params.getEquipmentID(),
                strInterFabMonitorGroupActionInfoSequence,
                strDCActionLotResult);
        if (null != strStartLotActionListEffectSpecCheckOut) {
            //-----------------------------------------------------------------//
            //   Register Entity Inhibition as the result of SPEC Check        //
            //-----------------------------------------------------------------//
            final List<Infos.EntityInhibitDetailAttributes> entityInhibitions =
                    strStartLotActionListEffectSpecCheckOut.getEntityInhibitions();
            if (CimArrayUtils.isNotEmpty(entityInhibitions)) {
                for (Infos.EntityInhibitDetailAttributes entityInhibition : entityInhibitions) {
                    try {
                        Params.MfgRestrictReq_110Params mfgRestrictReq110Params = new Params.MfgRestrictReq_110Params();
                        List<Infos.EntityInhibitDetailAttributes> entityInhibitDetailAttributeList = new ArrayList<>();
                        entityInhibitDetailAttributeList.add(entityInhibition);
                        mfgRestrictReq110Params.setClaimMemo(params.getOpeMemo());
                        mfgRestrictReq110Params.setUser(objCommon.getUser());
                        mfgRestrictReq110Params.setEntityInhibitDetailAttributeList(entityInhibitDetailAttributeList);
                        constraintService.sxMfgRestrictReq_110(mfgRestrictReq110Params, objCommon);
                    } catch (ServiceException e) {
                        if (!Validations.isEquals(retCodeConfig.getDuplicateInhibit(), e.getCode())) {
                            throw e;
                        }
                    }
                }
            }

            //-----------------------------------------------//
            //   Send a message as the result of SPEC Check  //
            //-----------------------------------------------//
            if (CimArrayUtils.isNotEmpty(strStartLotActionListEffectSpecCheckOut.getMessageList())) {
                for (Infos.MessageAttributes messageAttributes : strStartLotActionListEffectSpecCheckOut.getMessageList()) {
                    messageMethod.messageDistributionMgrPutMessage(objCommon,
                            messageAttributes.getMessageID(),
                            messageAttributes.getLotID(),
                            messageAttributes.getLotStatus(),
                            messageAttributes.getEquipmentID(),
                            messageAttributes.getRouteID(),
                            messageAttributes.getOperationNumber(),
                            messageAttributes.getReasonCode(),
                            messageAttributes.getMessageText());
                }
            }

            //-------------------------------------------------------------------------//
            //   Register Entity Inhibition as the result of Spec Check(SaR request)   //
            //-------------------------------------------------------------------------//
            Infos.MfgRestrictRequestForMultiFabIn strMfgRestrictRequestForMultiFabIn = new Infos.MfgRestrictRequestForMultiFabIn();
            strMfgRestrictRequestForMultiFabIn.setStrEntityInhibitionsWithFabInfo(strStartLotActionListEffectSpecCheckOut.getEntityInhibitionsWithFabInfoList());
            strMfgRestrictRequestForMultiFabIn.setClaimMemo("");

            constraintMethod.constraintRequestForMultiFab(objCommon,
                    strMfgRestrictRequestForMultiFabIn.getStrEntityInhibitionsWithFabInfo(),
                    strMfgRestrictRequestForMultiFabIn.getClaimMemo());
        }


        //-----------------------------------------------------------------//
        //   Summary Action Items to Lot based on the SPC Check Result     //
        //-----------------------------------------------------------------//
        Outputs.ObjStartLotActionListEffectSPCCheckOut strStartLotActionListEffectSPCCheckOut;
        strStartLotActionListEffectSPCCheckOut = startLotMethod.startLotActionListEffectSPCCheck(objCommon,
                strSPCCheckReqResult.getSpcCheckLot(),
                strSPCMgrSendSPCCheckReqOut.getSpcIFParmList(),
                strStartLotActionListEffectSpecCheckOut.getInterFabMonitorGroupActionInfoList(),
                params.getEquipmentID(),
                strStartLotActionListEffectSpecCheckOut.getDcActionLotResultList());


        if (null != strStartLotActionListEffectSPCCheckOut) {
            //-----------------------------------------------------------------------//
            //   Register Entity Inhibition as the result of SPC Check               //
            //-----------------------------------------------------------------------//
            if (CimArrayUtils.isNotEmpty(strStartLotActionListEffectSPCCheckOut.getStrEntityInhibitions())) {
                for (Infos.EntityInhibitDetailAttributes strEntityInhibition : strStartLotActionListEffectSPCCheckOut.getStrEntityInhibitions()) {
                    try {
                        Params.MfgRestrictReq_110Params mfgRestrictReq_110Params = new Params.MfgRestrictReq_110Params();
                        List<Infos.EntityInhibitDetailAttributes> entityInhibitDetailAttributeList = new ArrayList<>();
                        entityInhibitDetailAttributeList.add(strEntityInhibition);
                        mfgRestrictReq_110Params.setClaimMemo(params.getOpeMemo());
                        mfgRestrictReq_110Params.setUser(objCommon.getUser());
                        mfgRestrictReq_110Params.setEntityInhibitDetailAttributeList(entityInhibitDetailAttributeList);
                        constraintService.sxMfgRestrictReq_110(mfgRestrictReq_110Params, objCommon);
                    } catch (ServiceException e) {
                        if (!Validations.isEquals(retCodeConfig.getDuplicateInhibit(), e.getCode())) {
                            throw e;
                        }
                    }
                }
            }

            //-----------------------------------------------//
            //   Send a message as the result of SPC Check   //
            //-----------------------------------------------//
            if (CimArrayUtils.isNotEmpty(strStartLotActionListEffectSPCCheckOut.getStrMessageList())) {
                for (Infos.MessageAttributes messageAttributes : strStartLotActionListEffectSPCCheckOut.getStrMessageList()) {
                    messageMethod.messageDistributionMgrPutMessage(objCommon,
                            messageAttributes.getMessageID(),
                            messageAttributes.getLotID(),
                            messageAttributes.getLotStatus(),
                            messageAttributes.getEquipmentID(),
                            messageAttributes.getRouteID(),
                            messageAttributes.getOperationNumber(),
                            messageAttributes.getReasonCode(),
                            messageAttributes.getMessageText());
                }
            }
            //------------------------------------------------------------------------//
            //   Register Entity Inhibition as the result of SPC Check(SaR request)   //
            //------------------------------------------------------------------------//
            Infos.MfgRestrictRequestForMultiFabIn strMfgRestrictRequestForMultiFabInForSPC = new Infos.MfgRestrictRequestForMultiFabIn();
            strMfgRestrictRequestForMultiFabInForSPC.setStrEntityInhibitionsWithFabInfo(strStartLotActionListEffectSPCCheckOut.getEntityInhibitionsWithFabInfo());
            strMfgRestrictRequestForMultiFabInForSPC.setClaimMemo("");

            constraintMethod.constraintRequestForMultiFab(objCommon,
                    strMfgRestrictRequestForMultiFabInForSPC.getStrEntityInhibitionsWithFabInfo(),
                    strMfgRestrictRequestForMultiFabInForSPC.getClaimMemo());

            //----------------------------------------------------------------------------//
            //   Send a message per chart owner as the result of SPC Check(SaR request)   //
            //----------------------------------------------------------------------------//
            Infos.SystemMessageRequestForMultiFabIn strSystemMessageRequestForMultiFabIn;
            Infos.MessageAttributesWithFabInfo[] tmpMessageAttributesWithFabInfoSeq;
            tmpMessageAttributesWithFabInfoSeq = new Infos.MessageAttributesWithFabInfo[1];
            if (CimArrayUtils.isNotEmpty(strStartLotActionListEffectSPCCheckOut.getMailSendListWithFabInfo())) {
                for (Infos.MailSendWithFabInfo mailSendWithFabInfo : strStartLotActionListEffectSPCCheckOut.getMailSendListWithFabInfo()) {
                    tmpMessageAttributesWithFabInfoSeq[0].setFabID(mailSendWithFabInfo.getFabID());
                    tmpMessageAttributesWithFabInfoSeq[0].setStrMessageAttributes(mailSendWithFabInfo.getStrMailSend().getMessageAttributes());
                    Infos.ObjCommon tmpObjCommonIn = new Infos.ObjCommon();
                    tmpObjCommonIn.setUser(new User());
                    tmpObjCommonIn.getUser().setUserID(ObjectIdentifier.buildWithValue(
                            mailSendWithFabInfo.getStrMailSend().getChartMailAddress()
                    ));

                    strSystemMessageRequestForMultiFabIn = new Infos.SystemMessageRequestForMultiFabIn();
                    strSystemMessageRequestForMultiFabIn.setStrMessageListWithFabInfo(CimArrayUtils.generateList(tmpMessageAttributesWithFabInfoSeq));
                    messageMethod.systemMessageRequestForMultiFab(objCommon,
                            strSystemMessageRequestForMultiFabIn);
                }
            }
        }


        //---------------------------------------------------------------------------
        //   Execute Lot Hold Action based on SpecCheck and also SPC Check result.
        //---------------------------------------------------------------------------

        //-------------------------------------//
        //   Effect The Result of SPEC Check   //
        //-------------------------------------//
        Outputs.ObjLotHoldRecordEffectSpecCheckResultOut strLotHoldRecordEffectSpecCheckResultOut = new Outputs.ObjLotHoldRecordEffectSpecCheckResultOut();
        strLotHoldRecordEffectSpecCheckResultOut = lotMethod.lotHoldRecordEffectSpecCheckResult(strLotHoldRecordEffectSpecCheckResultOut, objCommon,
                tmpStartCassette,
                strStartLotActionListEffectSpecCheckOut.getInterFabMonitorGroupActionInfoList(),
                strStartLotActionListEffectSpecCheckOut.getDcActionLotResultList());

        if (CimArrayUtils.isNotEmpty(strLotHoldRecordEffectSpecCheckResultOut.getLotHoldEffectList())) {
            Infos.LotHoldReq[] strLotHoldReqList;
            strLotHoldReqList = new Infos.LotHoldReq[1];
            for (Infos.LotHoldEffectList lotHoldEffectList : strLotHoldRecordEffectSpecCheckResultOut.getLotHoldEffectList()) {
                strLotHoldReqList[0] = new Infos.LotHoldReq();
                strLotHoldReqList[0].setHoldType(lotHoldEffectList.getHoldType());
                strLotHoldReqList[0].setHoldReasonCodeID(lotHoldEffectList.getReasonCodeID());
                strLotHoldReqList[0].setHoldUserID(lotHoldEffectList.getUserID());
                strLotHoldReqList[0].setResponsibleOperationMark(lotHoldEffectList.getResponsibleOperationMark());
                strLotHoldReqList[0].setRouteID(lotHoldEffectList.getRouteID());
                strLotHoldReqList[0].setOperationNumber(lotHoldEffectList.getOperationNumber());
                strLotHoldReqList[0].setRelatedLotID(lotHoldEffectList.getRelatedLotID());
                strLotHoldReqList[0].setClaimMemo(lotHoldEffectList.getClaimMemo());

                try {
                    lotService.sxHoldLotReq(objCommon, lotHoldEffectList.getLotID(), CimArrayUtils.generateList(strLotHoldReqList));
                } catch (ServiceException e) {
                    if (!Validations.isEquals(e.getCode(), retCodeConfig.getExistSameHold())) {
                        throw e;
                    }
                }
            }
        }


        //--------------//
        // Future Hold  //
        //--------------//
        if (CimArrayUtils.isNotEmpty(strLotHoldRecordEffectSpecCheckResultOut.getFutureHoldEffectList())) {
            for (Infos.LotHoldEffectList lotHoldEffectList : strLotHoldRecordEffectSpecCheckResultOut.getFutureHoldEffectList()) {
                Params.FutureHoldReqParams futureHoldReqParams = new Params.FutureHoldReqParams();
                futureHoldReqParams.setHoldType(lotHoldEffectList.getHoldType());
                futureHoldReqParams.setLotID(lotHoldEffectList.getLotID());
                futureHoldReqParams.setRouteID(lotHoldEffectList.getRouteID());
                futureHoldReqParams.setOperationNumber(lotHoldEffectList.getOperationNumber());
                futureHoldReqParams.setReasonCodeID(lotHoldEffectList.getReasonCodeID());
                futureHoldReqParams.setRelatedLotID(lotHoldEffectList.getRelatedLotID());
                futureHoldReqParams.setPostFlag(true);
                futureHoldReqParams.setSingleTriggerFlag(true);
                futureHoldReqParams.setClaimMemo(lotHoldEffectList.getClaimMemo());
                try {
                    processControlService.sxFutureHoldReq(objCommon, futureHoldReqParams);
                } catch (ServiceException e) {
                    if (!Validations.isEquals(retCodeConfig.getDuplicateFtholdEntry(), e.getCode())) {
                        throw e;
                    }
                }
            }
        }


        //------------------------------------//
        //   Effect The Result of SPC Check   //
        //------------------------------------//
        Outputs.ObjLotHoldRecordEffectSPCCheckResultOut strLotHoldRecordEffectSPCCheckResultOut;
        strLotHoldRecordEffectSPCCheckResultOut = lotMethod.lotHoldRecordEffectSPCCheckResult(objCommon,
                strSPCCheckReqResult.getSpcCheckLot(),
                strLotHoldRecordEffectSpecCheckResultOut.getInterFabMonitorGroupActionInfoList(),
                strLotHoldRecordEffectSpecCheckResultOut.getDcActionLotResultList());

        if (CimArrayUtils.isNotEmpty(strLotHoldRecordEffectSPCCheckResultOut.getLotHoldEffectList())) {
            for (Infos.LotHoldEffectList lotHoldEffectList : strLotHoldRecordEffectSPCCheckResultOut.getLotHoldEffectList()) {
                List<Infos.LotHoldReq> strLotHoldReqList;
                strLotHoldReqList = new ArrayList<>(1);
                strLotHoldReqList.add(new Infos.LotHoldReq());
                strLotHoldReqList.get(0).setHoldType(lotHoldEffectList.getHoldType());
                strLotHoldReqList.get(0).setHoldReasonCodeID(lotHoldEffectList.getReasonCodeID());
                strLotHoldReqList.get(0).setHoldUserID(lotHoldEffectList.getUserID());
                strLotHoldReqList.get(0).setResponsibleOperationMark(lotHoldEffectList.getResponsibleOperationMark());
                strLotHoldReqList.get(0).setRouteID(lotHoldEffectList.getRouteID());
                strLotHoldReqList.get(0).setOperationNumber(lotHoldEffectList.getOperationNumber());
                strLotHoldReqList.get(0).setRelatedLotID(lotHoldEffectList.getRelatedLotID());
                strLotHoldReqList.get(0).setClaimMemo(lotHoldEffectList.getClaimMemo());

                try {
                    lotService.sxHoldLotReq(objCommon, lotHoldEffectList.getLotID(), strLotHoldReqList);
                } catch (ServiceException e) {
                    if (!Validations.isEquals(retCodeConfig.getExistSameHold(), e.getCode())) {
                        throw e;
                    }
                }
            }
        }

        if (CimArrayUtils.isNotEmpty(strLotHoldRecordEffectSPCCheckResultOut.getFutureHoldEffectList())) {
            for (Infos.LotHoldEffectList lotHoldEffectList : strLotHoldRecordEffectSPCCheckResultOut.getFutureHoldEffectList()) {
                Params.FutureHoldReqParams futureHoldReqParams = new Params.FutureHoldReqParams();
                futureHoldReqParams.setHoldType(lotHoldEffectList.getHoldType());
                futureHoldReqParams.setLotID(lotHoldEffectList.getLotID());
                futureHoldReqParams.setRouteID(lotHoldEffectList.getRouteID());
                futureHoldReqParams.setOperationNumber(lotHoldEffectList.getOperationNumber());
                futureHoldReqParams.setReasonCodeID(lotHoldEffectList.getReasonCodeID());
                futureHoldReqParams.setRelatedLotID(lotHoldEffectList.getRelatedLotID());
                futureHoldReqParams.setPostFlag(TRUE);
                futureHoldReqParams.setSingleTriggerFlag(TRUE);
                futureHoldReqParams.setClaimMemo(lotHoldEffectList.getClaimMemo());

                try {
                    processControlService.sxFutureHoldReq(objCommon, futureHoldReqParams);
                } catch (ServiceException e) {
                    if (!Validations.isEquals(retCodeConfig.getDuplicateFtholdEntry(), e.getCode())) {
                        throw e;
                    }
                }
            }
        }

        List<Infos.InterFabMonitorGroupActionInfo> tmpInterFabMonitorGroupActionInfoSequence =
                strLotHoldRecordEffectSPCCheckResultOut.getInterFabMonitorGroupActionInfoList();

        //---------------------------------------------------------------//
        //   Cut Monitor Relation for ProcessMonitorLot and ProcessLot   //
        //---------------------------------------------------------------//
        int holdReleasedLotCount = 0;
        List<ObjectIdentifier> holdReleasedLotIDs = new ArrayList<>();
        if (CimArrayUtils.isNotEmpty(tmpStartCassette)) {
            for (Infos.StartCassette startCassette : tmpStartCassette) {
                if (CimArrayUtils.isNotEmpty(startCassette.getLotInCassetteList())) {
                    for (Infos.LotInCassette lotInCassette : startCassette.getLotInCassetteList()) {
                        //---------------------------//
                        //   Omit Not-OpeStart Lot   //
                        //---------------------------//
                        if (CimBooleanUtils.isFalse(lotInCassette.getMoveInFlag())) {
                            continue;
                        }
                        Infos.MonitorGroupDeleteCompOut strMonitorGroupDeleteCompOut;
                        Infos.MonitorGroupDeleteCompIn strMonitorGroupDeleteCompIn = new Infos.MonitorGroupDeleteCompIn();
                        strMonitorGroupDeleteCompIn.setLotID(lotInCassette.getLotID());
                        strMonitorGroupDeleteCompIn.setStrInterFabMonitorGroupActionInfoSequence(tmpInterFabMonitorGroupActionInfoSequence);
                        strMonitorGroupDeleteCompOut = monitorGroupMethod.monitorGroupDeleteComp100(objCommon,
                                strMonitorGroupDeleteCompIn);

                        tmpInterFabMonitorGroupActionInfoSequence = strMonitorGroupDeleteCompOut.getStrInterFabMonitorGroupActionInfoSequence();
                        if (CimArrayUtils.isNotEmpty(strMonitorGroupDeleteCompOut.getStrMonitoredCompLots())) {
                            // Preparing for HoldRelease.
                            ObjectIdentifier aReasonCodeID = ObjectIdentifier.buildWithValue(BizConstant.SP_REASON_WAITINGMONITORHOLDRELEASE);
                            for (Infos.MonitoredCompLots strMonitoredCompLot : strMonitorGroupDeleteCompOut.getStrMonitoredCompLots()) {
                                Params.HoldLotReleaseReqParams holdLotReleaseReqParams = new Params.HoldLotReleaseReqParams();
                                holdLotReleaseReqParams.setLotID(strMonitoredCompLot.getProductLotID());
                                holdLotReleaseReqParams.setReleaseReasonCodeID(aReasonCodeID);
                                List<Infos.LotHoldReq> strLotHoldReleaseReqList = strMonitoredCompLot.getStrLotHoldReleaseReqList();
                                if (strLotHoldReleaseReqList == null || strLotHoldReleaseReqList.size() == 0) {
                                    break;
                                }
                                holdLotReleaseReqParams.setHoldReqList(strLotHoldReleaseReqList);
                                lotService.sxHoldLotReleaseReq(objCommon, holdLotReleaseReqParams);

                                holdReleasedLotIDs.add(holdReleasedLotCount, strMonitoredCompLot.getProductLotID());
                                holdReleasedLotCount++;

                                //------------------------------------------
                                //  Check Lot Type of Monitoring Lot
                                //------------------------------------------
                                if (CimStringUtils.equals(BizConstant.SP_LOT_TYPE_PRODUCTIONLOT, lotInCassette.getLotType())) {
                                    while (true) {
                                        Boolean strRepeatGatePassCheckConditionOut = processMethod.repeatGatePassCheckCondition(objCommon,
                                                lotInCassette.getLotID(),
                                                strMonitoredCompLot.getProductLotID());
                                        if (CimBooleanUtils.isFalse(strRepeatGatePassCheckConditionOut)) {
                                            log.info("passThruRequiredFlag == FALSE");
                                            break;
                                        }

                                        Outputs.ObjLotCurrentOperationInfoGetOut strLotCurrentOperationInfoGetOut;
                                        strLotCurrentOperationInfoGetOut = lotMethod.lotCurrentOperationInfoGet(objCommon, strMonitoredCompLot.getProductLotID());

                                        final Infos.GatePassLotInfo gatePassLotInfo = new Infos.GatePassLotInfo();
                                        gatePassLotInfo.setLotID(strMonitoredCompLot.getProductLotID());
                                        gatePassLotInfo.setCurrentRouteID(strLotCurrentOperationInfoGetOut.getRouteID());
                                        gatePassLotInfo.setCurrentOperationNumber(strLotCurrentOperationInfoGetOut.getOperationNumber());

                                        //Bug 2612 Lot history数据Ope Category显示空,此时的TransactionID为OEQPW006,需要替换成
                                        //OLOTW005
                                        if (objCommon.getTransactionID().equals(TransactionIDEnum.OPERATION_COMP_WITH_DATA_REQ.getValue())) {
                                            objCommon.setTransactionID(TransactionIDEnum.GATE_PASS_REQ.getValue());
                                        }
                                        lotService.sxPassThruReq(objCommon, gatePassLotInfo, params.getOpeMemo());
                                        //passThruReq执行完后,需要由OLOTW005变会OEQPW006,不然在3658之后的逻辑会出现错误
                                        if (objCommon.getTransactionID().equals(TransactionIDEnum.GATE_PASS_REQ.getValue())) {
                                            objCommon.setTransactionID(TransactionIDEnum.OPERATION_COMP_WITH_DATA_REQ.getValue());
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        //------------------------------------------------------------------------------------//
        //   MonitorGroupRelease, Hold and SpecOver/SPCViolation mail Request for other Fab   //
        //------------------------------------------------------------------------------------//

        if (CimArrayUtils.isNotEmpty(tmpInterFabMonitorGroupActionInfoSequence)) {
            for (Infos.InterFabMonitorGroupActionInfo interFabMonitorGroupActionInfo : tmpInterFabMonitorGroupActionInfoSequence) {
//            MonitorGroupReleaseRequestForMultiFabOut strMonitorGroupReleaseRequestForMultiFabOut;
//            MonitorGroupReleaseRequestForMultiFabIn  strMonitorGroupReleaseRequestForMultiFabIn;
//            strMonitorGroupReleaseRequestForMultiFabIn.setStrInterFabMonitorGroupActionInfo ( tmpInterFabMonitorGroupActionInfoSequence.get(i));
//            rc = monitorGroupMethod.monitorGroupReleaseRequestForMultiFab( strMonitorGroupReleaseRequestForMultiFabOut,
//                    strObjCommonIn,
//                    strMonitorGroupReleaseRequestForMultiFabIn );
//            }
            }

        }
        // Resize HoldReleasedLotIDs length.
        log.info("HoldReleased Lot Count is {}", holdReleasedLotCount);

        // Set HoldReleasedLots to return structure.
        Results.EDCWithSpecCheckActionReqResult strEDCWithSpecCheckActionReqResult = new Results.EDCWithSpecCheckActionReqResult();

        strEDCWithSpecCheckActionReqResult.setHoldReleasedLotIDs(holdReleasedLotIDs);
        //----------------------------------------------------//
        //   Update PO's collected data information finally   //
        //----------------------------------------------------//
        log.info("Update PO's collected data information finally.");
        processMethod.processDataCollectionInformationUpdate(objCommon, tmpStartCassette, strLotHoldRecordEffectSPCCheckResultOut.getDcActionLotResultList());

        //---------------------------------------------------------------//
        //   New Action by SPC Check result                              //
        //---------------------------------------------------------------//

        this.sxSPCDoActionReq(objCommon,
                strStartLotActionListEffectSPCCheckOut.getBankMoveList(),
                strStartLotActionListEffectSPCCheckOut.getMailSendList(),
                strStartLotActionListEffectSPCCheckOut.getReworkBranchList());

        //----------------------------------------------------------------------------
        // Post-Processing Registration for Hold Released Lot IDs (Monitored Lots)
        //----------------------------------------------------------------------------
        Results.PostTaskRegisterReqResult strPostProcessActionRegistReqResult = new Results.PostTaskRegisterReqResult();
        if (CimArrayUtils.isNotEmpty(holdReleasedLotIDs)) {
            log.info("Registration of Post-Processng for HoldReleasedLot");

            //HoldReleased LotID Trace
            holdReleasedLotIDs.forEach(holdReleasedLotID -> log.info("HoldRelesedLotIDs: {}", ObjectIdentifier.fetchValue(holdReleasedLotID)));
            //Set input parameter
            String txIDHoldReleasedLot = "";
            Infos.PostProcessRegistrationParam strPostProcessRegistrationParm = new Infos.PostProcessRegistrationParam();
            strPostProcessRegistrationParm.setLotIDs(holdReleasedLotIDs);
            strPostProcessRegistrationParm.setEquipmentID(params.getEquipmentID());
            strPostProcessRegistrationParm.setControlJobID(params.getControlJobID());


            log.info("MoveOutedLot   : FunctionID: {} ", objCommon.getTransactionID());
            //Set FunctionID for Hold Released Lot
            // OEQPW006 is MoveOutReq.
            // OEQPW008 is MoveOutForIBReq.
            // OEQPW012 is MoveOutWithRunningSplitReq.
            // OEQPW024 is MoveOutWithRunningSplitForIBReq.
            // OEQPW014 is ForceMoveOutReq.
            // OEQPW023 is ForceMoveOutForIBReq.
            if (CimStringUtils.equals(objCommon.getTransactionID(), TransactionIDEnum.OPERATION_COMP_WITH_DATA_REQ.getValue())) {
                txIDHoldReleasedLot = "ThTRC_04";
            } else if (CimStringUtils.equals(objCommon.getTransactionID(), TransactionIDEnum.OPERATION_COMP_FOR_INTERNAL_BUFFER_REQ.getValue())) {
                txIDHoldReleasedLot = "ThTRC_55";
            } else if (CimStringUtils.equals(objCommon.getTransactionID(), TransactionIDEnum.FORCE_OPERATION_COMP_REQ.getValue())) {
                txIDHoldReleasedLot = "ThEWC_10";
            } else if (CimStringUtils.equals(objCommon.getTransactionID(), TransactionIDEnum.FORCE_OPERATION_COMP_FOR_INTERNAL_BUFFER_REQ.getValue())) {
                txIDHoldReleasedLot = "ThEWC_11";
            } else if (CimStringUtils.equals(objCommon.getTransactionID(), TransactionIDEnum.PARTIAL_OPERATION_COMP_WITH_DATA_REQ.getValue())) {
                txIDHoldReleasedLot = "ThEWC_15";
            } else if (CimStringUtils.equals(objCommon.getTransactionID(), TransactionIDEnum.PARTIAL_OPERATION_COMP_FOR_INTERNAL_BUFFER_REQ.getValue())) {
                txIDHoldReleasedLot = "ThEWC_16";
            } else {
                Validations.check(true, retCodeConfig.getCalledFromInvalidTransaction(), objCommon.getTransactionID());
            }
            log.info("HoldReleasedLot: FunctionID: {} ", txIDHoldReleasedLot);

            //Post-Processing Registration for Released Lot

            Params.PostTaskRegisterReqParams postTaskRegisterReqParams = new Params.PostTaskRegisterReqParams();
            postTaskRegisterReqParams.setTransactionID(txIDHoldReleasedLot);
            postTaskRegisterReqParams.setPatternID(null);
            postTaskRegisterReqParams.setKey(null);
            postTaskRegisterReqParams.setSequenceNumber(-1);
            postTaskRegisterReqParams.setPostProcessRegistrationParm(strPostProcessRegistrationParm);
            postTaskRegisterReqParams.setClaimMemo(params.getOpeMemo());

            strPostProcessActionRegistReqResult = postService.sxPostTaskRegisterReq(objCommon, postTaskRegisterReqParams);

            result.setRelatedQueuekey(strPostProcessActionRegistReqResult.getDKey());
            log.info("HoldReleasedLot: dKey: {} ", result.getRelatedQueuekey());
        }

        //--------------------------//
        //                          //
        //   Set Return Structure   //
        //                          //
        //--------------------------//
        Results.MoveOutReqResult moveOutReqResult = equipmentMethod.equipmentFillInTxTRC004(objCommon, tmpStartCassette, strSPCCheckReqResult.getSpcCheckLot());

        long eqpMonitorSwitch = StandardProperties.OM_AUTOMON_FLAG.getLongValue();
        if (1 == eqpMonitorSwitch) {
            log.info("1 == SP_EQPMONITOR_SWITCH");
            if (null != moveOutReqResult && CimArrayUtils.isNotEmpty(moveOutReqResult.getMoveOutLot())) {
                for (Infos.OpeCompLot opeCompLot : moveOutReqResult.getMoveOutLot()) {
                    String lotType = lotMethod.lotTypeGet(objCommon, opeCompLot.getLotID());

                    if (CimStringUtils.equals(lotType, BizConstant.SP_LOT_TYPE_EQUIPMENTMONITORLOT)
                            || CimStringUtils.equals(lotType, BizConstant.SP_LOT_TYPE_DUMMYLOT)) {
                        log.info("lotType is Equipment Monitor or Dummy");
                        if ((CimStringUtils.unEqual(opeCompLot.getSpecificationCheckResult(), BizConstant.SP_SPECCHECKRESULT_OK)
                                && CimStringUtils.isNotEmpty(opeCompLot.getSpecificationCheckResult())
                                && CimStringUtils.unEqual(opeCompLot.getSpecificationCheckResult(), BizConstant.SP_SPECCHECKRESULT_1ASTERISK))
                                || CimStringUtils.equals(opeCompLot.getSpcCheckResult(), BizConstant.SP_SPCCHECK_HOLDLIMITOFF)
                                || CimStringUtils.equals(opeCompLot.getSpcCheckResult(), BizConstant.SP_SPCCHECK_WARNINGLIMITOFF)) {
                            log.info("Call eqpMonitorJobLotUpdate");
                            //Update information of EqpMonitor job lot
                            equipmentMethod.eqpMonitorJobLotUpdate(objCommon,
                                    opeCompLot.getLotID(),
                                    BizConstant.SP_EQPMONITORJOB_OPECATEGORY_SPECCHECK);
                        }
                    }
                }
            }
        }
        result.setMoveOutReqResult(moveOutReqResult);
        return result;
    }

    @Override
    public void sxEDCInformationSetByPostProcReq(Infos.ObjCommon objCommon, ObjectIdentifier controlJobID, ObjectIdentifier lotID) {
        Validations.check(null == controlJobID, retCodeConfig.getInvalidParameter());
        //---------------------
        // State Check
        //---------------------

        //-----------------------------------------------
        // Get the Lot actual OperationStart information
        // Needn't obtain EDC information.
        //------------------------------------------------
        final Outputs.ObjControlJobStartReserveInformationOut startReserveInformationOut = controlJobMethod
                .controlJobStartReserveInformationGet(objCommon, controlJobID, false);

        final ObjectIdentifier equipmentID = startReserveInformationOut.getEquipmentID();
        final List<Infos.StartCassette> startCassetteList = startReserveInformationOut.getStartCassetteList();


        //---------------------
        // Object Lock
        //---------------------
        Optional.ofNullable(startCassetteList)
                .map(startCassettes -> startCassettes
                        .stream()
                        // ignore the lots that is in a carrier with EMPTY as load purpose type
                        .filter(startCassette ->
                                !CimStringUtils.equals(startCassette.getLoadPurposeType(), BizConstant.SP_LOADPURPOSETYPE_EMPTYCASSETTE)
                        )
                        .flatMap(startCassette -> startCassette.getLotInCassetteList().stream())
                        //-----------------------
                        // Omit not MoveIn Lot
                        //-----------------------
                        .filter(Infos.LotInCassette::getMoveInFlag)
                        .map(Infos.LotInCassette::getLotID)
                        //-----------------------
                        // Omit other lot
                        //-----------------------
                        .filter(lot -> ObjectIdentifier.equalsWithValue(lot, lotID))
                        .collect(Collectors.toList())
                )
                .ifPresent(lots -> lots.forEach(lot ->
                        objectLockMethod.objectLock(objCommon, CimLot.class, lot))
                );


        //-----------------------
        // EDC Information sets
        //-----------------------
        Optional.ofNullable(startCassetteList)
                .ifPresent(startCassettes -> startCassettes
                        .stream()
                        //-------------------------------------------------------------------------
                        // Ignore the lots that is in a carrier with EMPTY as load purpose type
                        //-------------------------------------------------------------------------
                        .filter(startCassette ->
                                !CimStringUtils.equals(startCassette.getLoadPurposeType(), BizConstant.SP_LOADPURPOSETYPE_EMPTYCASSETTE)
                        )
                        .forEach(startCassette -> Optional.ofNullable(startCassette.getLotInCassetteList())
                                .ifPresent(lotInCassettes -> lotInCassettes
                                        .stream()
                                        //-----------------------
                                        // Omit not MoveIn Lot
                                        //-----------------------
                                        .filter(Infos.LotInCassette::getMoveInFlag)
                                        //-----------------------
                                        // Omit other lot
                                        //-----------------------
                                        .filter(lotInCassette -> ObjectIdentifier.equalsWithValue(lotInCassette.getLotID(), lotID))
                                        //--------------------------------------------------------
                                        // Set EDC information for each Lot's ProcessOperation
                                        //--------------------------------------------------------
                                        .forEach(lotInCassette -> {
                                            // Get LotID
                                            ObjectIdentifier tmpLotID = lotInCassette.getLotID();
                                            final CimLot aLot = baseCoreFactory.getBO(CimLot.class, tmpLotID);
                                            Validations.check(null == aLot, retCodeConfig.getNotFoundLot());

                                            // Get Recipe ID
                                            Infos.StartRecipe startRecipe = lotInCassette.getStartRecipe();
                                            ObjectIdentifier logicalRecipeID = startRecipe.getLogicalRecipeID();
                                            ObjectIdentifier machineRecipeID = startRecipe.getMachineRecipeID();

                                            //-------------------------------------------
                                            // Gets EDC items information dose is exits
                                            //-------------------------------------------
                                            final boolean edcItemsInformationExist =
                                                    processMethod.edcItemsInformationExist(objCommon, tmpLotID);

                                            if (edcItemsInformationExist && startRecipe.getDataCollectionFlag()) {
                                                // if exit... then return
                                                return;
                                            }

                                            //---------------------------
                                            // Get EDC information
                                            //---------------------------
                                            final Outputs.ObjProcessDataCollectionDefinitionGetOut edcInformation =
                                                    processMethod.processDataCollectionDefinitionGet(
                                                            objCommon,
                                                            equipmentID,
                                                            tmpLotID,
                                                            logicalRecipeID,
                                                            machineRecipeID);

                                            //---------------------------
                                            // Get ProcessOperation
                                            //---------------------------
                                            final CimProcessOperation processOperation = aLot.getProcessOperation();
                                            Validations.check(null == processOperation,
                                                    retCodeConfig.getNotFoundProcessOperation());

                                            if (edcInformation.isDataCollectionFlag()) {
                                                processOperation.makeAssignedDataCollectionFlagOn();
                                            } else {
                                                // EDC information is null, return.
                                                processOperation.makeAssignedDataCollectionFlagOff();
                                                return;
                                            }

                                            // data type convert
                                            List<ProcessDTO.DataCollectionInfo> dataCollectionInfos =
                                                    Optional.ofNullable(edcInformation.getDataCollectionDefList())
                                                            .map(data -> data.stream()
                                                                    .map(Infos.DataCollectionInfo::convert)
                                                                    .collect(Collectors.toList())
                                                            )
                                                            .orElseGet(Collections::emptyList);

                                            //---------------------------------------------------
                                            // Set EDC and item information to ProcessOperation
                                            //---------------------------------------------------
                                            processOperation.setDataCollectionInfo(dataCollectionInfos);
                                        })
                                )
                        )
                );
    }

    @Override
    public void sxEDCInformationSetByPostProcReq_2(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, ObjectIdentifier lotID) {
        Validations.check(null == equipmentID, retCodeConfig.getInvalidParameter());
        //-----------------------------------------------
        // Get the Lot actual OperationStart information
        // Needn't obtain EDC information.
        //------------------------------------------------
        final Infos.StartRecipe startRecipe = lotMethod.lotActualOperationStartRecipeGet(objCommon, lotID);

        if (CimBooleanUtils.isFalse(startRecipe.getDataCollectionFlag())) {
            return;
        }

        //---------------------
        // Object Lock
        //---------------------
        objectLockMethod.objectLock(objCommon, CimLot.class, lotID);

        //-----------------------
        // EDC Information sets
        //-----------------------
        // Get LotID
        final CimLot aLot = baseCoreFactory.getBO(CimLot.class, lotID);
        Validations.check(null == aLot, retCodeConfig.getNotFoundLot());

        // Get Recipe ID
        ObjectIdentifier logicalRecipeID = startRecipe.getLogicalRecipeID();

        // Get DOC setting about machineRecipe and recipeParameter
        Outputs.ObjLotEffectiveFPCInfoGetOut fpcInfoGetOut = lotMethod.lotEffectiveFPCInfoGet(
                objCommon,
                BizConstant.SP_FPC_EXCHANGETYPE_STARTRESERVEINFO,
                equipmentID, lotID);

        //Get BaseMachineRecipe ID because "Dynamic Recipe Change function"
        ObjectIdentifier baseMachineRecipeID = null;
        boolean skipEDCGetFlag = false;
        try {
            baseMachineRecipeID = logicalRecipeMethod.findBaseMachineRecipe(objCommon, logicalRecipeID,
                    lotID, equipmentID);
        } catch (ServiceException e) {
            if (Validations.isEquals(retCodeConfig.getNotFoundMachineRecipe(), e.getCode())) {
                // Check DOC exist machineRecipeRequired
                if (null != fpcInfoGetOut && fpcInfoGetOut.isMachineRecipeActionRequiredFlag()) {
                    if (log.isDebugEnabled()) {
                        log.debug("skip edcGet function");
                    }
                    skipEDCGetFlag = true;
                } else {
                    throw e;
                }
            } else {
                throw e;
            }
        }
        //-------------------------------------------
        // Gets EDC items information dose is exits
        //-------------------------------------------
        final boolean edcItemsInformationExist = processMethod.edcItemsInformationExist(objCommon, lotID);
        if (startRecipe.getDataCollectionFlag() && edcItemsInformationExist) {
            // if exit... then return
            if (log.isDebugEnabled()) {
                log.debug("Lot:[{}] has exist DC data in step:[{}], process operation:[{}]",
                        aLot.getIdentifier(),
                        aLot.getOperationNumber(),
                        aLot.getProcessOperationObj());
            }
            return;
        }

        //---------------------------
        // Get EDC information
        //---------------------------
        Outputs.ObjProcessDataCollectionDefinitionGetOut edcInformation =
                new Outputs.ObjProcessDataCollectionDefinitionGetOut();
        edcInformation.setDataCollectionFlag(false);
        edcInformation.setDataCollectionDefList(new ArrayList<>());
        if (!skipEDCGetFlag) {
            edcInformation = processMethod.processDataCollectionDefinitionGet(
                    objCommon,
                    equipmentID,
                    lotID,
                    logicalRecipeID,
                    baseMachineRecipeID);
        }

        //---------------------------
        // Get ProcessOperation
        //---------------------------
        final CimProcessOperation processOperation = aLot.getProcessOperation();
        Validations.check(null == processOperation, retCodeConfig.getNotFoundProcessOperation());

        //set DOC edc if exist DOC edc setting
        long tmpFPCAdoptFlag = StandardProperties.OM_DOC_ENABLE_FLAG.getLongValue();
        if (1 == tmpFPCAdoptFlag) {
            if (log.isDebugEnabled()) {
                log.debug("DOC Adopt Flag is ON. Now apply DOC edc section.");
            }
            Outputs.ObjProcessDataCollectionDefinitionGetOut dcoEdcInfo = docMethod.fpcDCInfoExchangeByEDCSet(objCommon,
                    BizConstant.SP_FPC_EXCHANGETYPE_STARTRESERVEREQ,
                    equipmentID,
                    lotID,
                    edcInformation);
            edcInformation.setDataCollectionFlag(dcoEdcInfo.isDataCollectionFlag());
            edcInformation.setDataCollectionDefList(dcoEdcInfo.getDataCollectionDefList());
        } else {
            if (log.isDebugEnabled()) {
                log.debug("DOC Adopt Flag is OFF,skip.");
            }
        }

        if (edcInformation.isDataCollectionFlag()) {
            processOperation.makeAssignedDataCollectionFlagOn();
        } else {
            // EDC information is null, return.
            processOperation.makeAssignedDataCollectionFlagOff();
            return;
        }

        // data type convert
        List<ProcessDTO.DataCollectionInfo> dataCollectionInfos =
                Optional.ofNullable(edcInformation.getDataCollectionDefList())
                        .map(data -> data.stream()
                                .map(Infos.DataCollectionInfo::convert)
                                .collect(Collectors.toList())
                        )
                        .orElseGet(Collections::emptyList);

        //---------------------------------------------------
        // Set EDC and item information to ProcessOperation
        //---------------------------------------------------
        processOperation.setDataCollectionInfo(dataCollectionInfos);
    }
}
