package com.fa.cim.service.equipment.impl;

import com.alibaba.fastjson.JSON;
import com.fa.cim.annotaion.OmService;
import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.OmCode;
import com.fa.cim.common.utils.*;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.config.RetCodeConfigEx;
import com.fa.cim.dto.*;
import com.fa.cim.entity.runtime.customer.CimCustomerDO;
import com.fa.cim.entity.runtime.dcdef.CimDataCollectionDefItemDO;
import com.fa.cim.entity.runtime.eqp.CimEquipmentDO;
import com.fa.cim.entity.runtime.lottype.CimLotTypeDO;
import com.fa.cim.entity.runtime.person.CimPersonDO;
import com.fa.cim.entity.runtime.processdefinition.CimProcessDefinitionDO;
import com.fa.cim.entity.runtime.processflow.CimPFDefinitionListDO;
import com.fa.cim.entity.runtime.processflow.CimPFPosListDO;
import com.fa.cim.entity.runtime.processflow.CimProcessFlowDO;
import com.fa.cim.entity.runtime.productrequest.CimProductRequestDO;
import com.fa.cim.eqp.IBFurnaceEQPBatchInfo;
import com.fa.cim.eqp.IBFurnaceEQPBatchInfoInqParams;
import com.fa.cim.jpa.CimJpaRepository;
import com.fa.cim.layoutrecipe.LayoutRecipeParams;
import com.fa.cim.layoutrecipe.LayoutRecipeResults;
import com.fa.cim.method.*;
import com.fa.cim.newcore.impl.bo.env.StandardProperties;
import com.fa.cim.remote.IEAPRemoteManager;
import com.fa.cim.service.durable.IDurableInqService;
import com.fa.cim.service.equipment.IEquipmentInqService;
import com.fa.cim.service.system.ISystemService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * description:
 * <p>IEquipmentInqServiceImpl .<br/></p>
 * <p>
 * change history:
 * date      defect#       person     comments
 * ------------------------------------------------------------
 * ---------------------------------------------------------
 * 2020/9/9/009   ********     Decade     create file
 *
 * @author: Decade
 * @date: 2020/9/9/009 15:26
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@OmService
public class EquipmentInqServiceImpl implements IEquipmentInqService {

    @Autowired
    private IEquipmentMethod equipmentMethod;

    @Autowired
    private IPortMethod portMethod;

    @Autowired
    private RetCodeConfig retCodeConfig;

    @Autowired
    private ITCSMethod tcsMethod;

    @Autowired
    private IControlJobMethod controlJobMethod;

    @Autowired
    private ILogicalRecipeMethod logicalRecipeMethod;

    @Autowired
    private IRecipeMethod recipeMethod;

    @Autowired
    private ILotMethod lotMethod;

    @Autowired
    private IAPCMethod apcMethod;

    @Autowired
    private ISystemService systemService;

    @Autowired
    private ICassetteMethod cassetteMethod;

    @Autowired
    private IProcessMethod processMethod;

    @Autowired
    private IStartCassetteMethod startCassetteMethod;

    @Autowired
    private IDurableInqService durableInqService;

    @Autowired
    private IAreaMethod areaMethod;

    @Autowired
    private CimJpaRepository cimJpaRepository;

    @Autowired
    private IEAPMethod eapMethod;

    @Autowired
    private RetCodeConfigEx retCodeConfigEx;


    @Override
    public List<Infos.CandidateChamberStatusInfo> sxChamberStatusSelectionInq(Infos.ObjCommon objCommon,
                                                                              ObjectIdentifier equipmentID) {
        //Step1 - equipment_FillInTxEQQ015DR
        return equipmentMethod.equipmentFillInTxEQQ015DRV2(objCommon, equipmentID);
    }

    @Override
    public Results.EquipmentModeSelectionInqResult sxEquipmentModeSelectionInq(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, String modeChangeType, List<ObjectIdentifier> portIDs) {
        {

            Results.EquipmentModeSelectionInqResult equipmentModeSelectionInqResult = new Results.EquipmentModeSelectionInqResult();
            equipmentModeSelectionInqResult.setEquipmentID(equipmentID);
            List<Infos.CandidatePortMode> candidatePortModes = new ArrayList<>();
            equipmentModeSelectionInqResult.setCandidatePortMode(candidatePortModes);

            log.debug("Step1 - Check requested port combination - equipmentPortCombinationCheck");
            Outputs.ObjEquipmentPortCombinationCheck combinationCheck = portMethod.equipmentPortCombinationCheck(objCommon, equipmentID, portIDs);

            int nEqipmentPortGroupLen = CimArrayUtils.getSize(combinationCheck.getEquipmentPortGroup());
            int nInputPortGroupLen = CimArrayUtils.getSize(combinationCheck.getInputPortGroup());

            log.trace("nEqipmentPortGroupLen is 【{}】，nInputPortGroupLen is 【{}】",nEqipmentPortGroupLen,nInputPortGroupLen);
            if (nEqipmentPortGroupLen == nInputPortGroupLen) {
                log.trace("nEqipmentPortGroupLen == nInputPortGroupLen");
            } else if (nEqipmentPortGroupLen > nInputPortGroupLen) {
                log.trace("nEqipmentPortGroupLen > nInputPortGroupLen");
                Validations.check(CimStringUtils.equals(modeChangeType, BizConstant.SP_EQP_OPERATION_ONLINEMODE_CHANGE),
                        retCodeConfig.getInvalidModeChangeType());
            } else {
                log.trace("nEqipmentPortGroupLen < nInputPortGroupLen");
                Validations.check(retCodeConfig.getSystemError());
            }

            for (ObjectIdentifier portID : portIDs) {
                log.debug("step2 - Select candidate mode for each request port");
                candidatePortModes.add(portMethod.portResourceCandidateOperationModeGet(objCommon, equipmentID, modeChangeType, portID));
            }

            return equipmentModeSelectionInqResult;
        }
    }

    @Override
    public Results.EqpStatusSelectionInqResult sxEqpStatusSelectionInq(Infos.ObjCommon objCommon,
                                                                       ObjectIdentifier equipmentID,
                                                                       boolean allInquiryFlag) {
        log.debug("sxEqpStatusSelectionInq(): enter sxEqpStatusSelectionInq");
        //Step1 - equipment_FillInTxEQQ002DR
        return equipmentMethod.equipmentFillInTxEQQ002DRV2(objCommon, equipmentID, allInquiryFlag);
    }

    @Override
    public Results.EqpEAPInfoInqResult sxEqpEAPInfoInq(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID) {
        Results.EqpEAPInfoInqResult eqpEapInfoInqResult = null;
        if (log.isDebugEnabled()) {
            log.debug("step1 - set params");
        }
        Inputs.SendEqpEAPInfoInqInput eapParams = new Inputs.SendEqpEAPInfoInqInput();
        eapParams.setUser(objCommon.getUser());
        eapParams.setEquipmentID(equipmentID);
        log.debug("step2 - send EAP info");
        long sleepTimeValue = StandardProperties.OM_EAP_CONNECT_SLEEP_TIME.getLongValue() == 0L ?
                BizConstant.SP_DEFAULT_SLEEP_TIME_TCS : StandardProperties.OM_EAP_CONNECT_SLEEP_TIME.getLongValue();

        long retryCountValue = StandardProperties.OM_EAP_CONNECT_RETRY_COUNT.getLongValue() == 0L ?
                BizConstant.SP_DEFAULT_RETRY_COUNT_TCS : StandardProperties.OM_EAP_CONNECT_SLEEP_TIME.getLongValue();
        for (int retryNum = 0; retryNum < (retryCountValue + 1); retryNum++) {
            if (log.isDebugEnabled()) {
                log.debug("{} loop to retryCountValue + 1", retryNum);
            }
            /*--------------------------*/
            /*    Send Request to EAP   */
            /*--------------------------*/
            IEAPRemoteManager eapRemoteManager = eapMethod.eapRemoteManager(objCommon, objCommon.getUser(),
                    equipmentID, null, false);
            if (null == eapRemoteManager) {
                log.error("MES not configure EAP host");
                Validations.check(retCodeConfigEx.getNotFoundEap());
            }
            Object eapOut = null;
            try {
                eapOut = eapRemoteManager.sendEqpEAPInfoInq(eapParams);
                eqpEapInfoInqResult = JSON.parseObject(eapOut.toString(),Results.EqpEAPInfoInqResult.class);
                log.debug("Now EAP subSystem is alive!! Go ahead");
                break;
            } catch (ServiceException ex) {
                if (Validations.isEquals(ex.getCode(), retCodeConfig.getTcsNoResponse())) {
                    if (log.isDebugEnabled()) {
                        log.debug("EAP subsystem has return NO_RESPONSE!just retry now!! now count...{}", retryNum);
                        log.debug("now sleeping... {}", sleepTimeValue);
                    }
                    if (retryNum != retryCountValue) {
                        try {
                            Thread.sleep(sleepTimeValue);
                            continue;
                        } catch (InterruptedException e) {
                            ex.addSuppressed(e);
                            Thread.currentThread().interrupt();
                            throw ex;
                        }
                    } else {
                        Validations.check(true, retCodeConfig.getTcsNoResponse());
                    }
                } else {
                    Validations.check(true, new OmCode(ex.getCode(), ex.getMessage()));
                }
            }
        }

        if (null != eqpEapInfoInqResult) {
            List<Infos.EqpControlJobInfo> strEqpControlJobInfo = eqpEapInfoInqResult.getEqpControlJobInfoList();
            if (!CimArrayUtils.isEmpty(strEqpControlJobInfo)) {
                for (Infos.EqpControlJobInfo eqpControlJobInfo : strEqpControlJobInfo) {
                    try {
                        if (log.isDebugEnabled()) {
                            log.debug("step3 - get control job info");
                        }
                        Outputs.ObjControlJobStatusGetOut controlJobStatusGetOut =
                                controlJobMethod.controlJobStatusGet(objCommon, eqpControlJobInfo.getControlJobID());
                        eqpControlJobInfo.setOmsControlJobStatus(controlJobStatusGetOut.getControlJobStatus());
                    } catch (ServiceException e) {
                        if (Validations.isEquals(retCodeConfig.getNotFoundControlJob(), e.getCode())) {
                            eqpControlJobInfo.setOmsControlJobStatus(BizConstant.EMPTY);
                        } else {
                            throw e;
                        }
                    }
                }
            }
        }
        return eqpEapInfoInqResult;
    }

    @Override
    public Results.EqpMemoInfoInqResult sxEqpMemoInfoInq(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID) {
        log.debug("sxEqpMemoInfoInq(): enter here");

        Results.EqpMemoInfoInqResult eqpMemoInfoInqResult = new Results.EqpMemoInfoInqResult();

        log.debug("step1 - get equipment note by equipment");
        List<Infos.EqpNote> objEqpNoteGetByEqpIDDrOut = equipmentMethod.equipmentNoteGetByEqpIDDR(objCommon, equipmentID);

        eqpMemoInfoInqResult.setEquipmentID(equipmentID);
        eqpMemoInfoInqResult.setStrEqpNote(objEqpNoteGetByEqpIDDrOut);

        return eqpMemoInfoInqResult;
    }

    @Override
    public Results.EqpRecipeParameterListInqResult sxEqpRecipeParameterListInq(Infos.ObjCommon objCommon, Params.EqpRecipeParameterListInq params) {
        Results.EqpRecipeParameterListInqResult eqpRecipeParameterListInqResult = new Results.EqpRecipeParameterListInqResult();

        log.trace("RParmSearchCriteria is 【{}】",params.getRParmSearchCriteria());
        if(CimStringUtils.equals(params.getRParmSearchCriteria(), BizConstant.SP_RPARM_SEARCHCRITERIA_LOGICALRECIPE)){
            log.debug("step1 - get logical recipe parameter info");
            List<Infos.RecipeParameterInfo> recipeParameterInfo = logicalRecipeMethod.logicalRecipeRecipeParameterInfoGetByPD(objCommon, params);

            eqpRecipeParameterListInqResult.setStrRecipeParameterInfoList(recipeParameterInfo);
        }else if(CimStringUtils.equals(params.getRParmSearchCriteria(),BizConstant.SP_RPARM_SEARCHCRITERIA_EQUIPMENT)){
            log.debug("step2 - get recipe parameter info");
            List<Infos.RecipeParameterInfo> recipeParameterInfo = recipeMethod.recipeParameterInfoGetDR(objCommon, params.getEquipmentID());

            eqpRecipeParameterListInqResult.setStrRecipeParameterInfoList(recipeParameterInfo);
        } else {
            throw new ServiceException(retCodeConfig.getInvalidParameter());
        }

        return eqpRecipeParameterListInqResult;
    }

    @Override
    public Results.EqpRecipeSelectionInqResult sxEqpRecipeSelectionInq(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID) {
        log.trace("equipmentID is 【{}】",equipmentID);
        if (ObjectIdentifier.isEmpty(equipmentID)) {
            throw new ServiceException(retCodeConfig.getInvalidInputParam());
        }
        Results.EqpRecipeSelectionInqResult resultPara = new Results.EqpRecipeSelectionInqResult();

        /*------------------------------------------*/
        /*   Get Equipment_multiRecipeCapability    */
        /*------------------------------------------*/
        log.debug("step1 - get equipment multi recipe capability");
        String equipmentMultiRecipeCapabilityGet = equipmentMethod.equipmentMultiRecipeCapabilityGet(objCommon, equipmentID);
        resultPara.setMultiRecipeCapability(equipmentMultiRecipeCapabilityGet);

        /*--------------------------------------------------------------------*/
        /*   Search PosLotFamily, PosWafer to get scrapped WaferID/LotID list */
        /*--------------------------------------------------------------------*/
        log.debug("step2 - Search PosLotFamily, PosWafer to get scrapped WaferID/LotID list ");
        List<Infos.MandPRecipeInfo> mandprecipeinfoOut = equipmentMethod.equipmentMachineAndPhysicalRecipeIDGetDR(objCommon, equipmentID);
        resultPara.setMandPRecipeInfo(mandprecipeinfoOut);

        return resultPara;
    }

    @Override
    public Results.LotsMoveInInfoInqResult sxLotsMoveInInfoInq(Infos.ObjCommon objCommon, Params.LotsMoveInInfoInqParams params) {
        List<ObjectIdentifier> cassetteIDList = params.getCassetteIDs();
        ObjectIdentifier equipmentID = params.getEquipmentID();

        Results.LotsMoveInInfoInqResult lotsMoveInInfoInqResult = new Results.LotsMoveInInfoInqResult();
        Validations.check(CimArrayUtils.isEmpty(cassetteIDList), retCodeConfig.getInvalidParameter());

        /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/
        /*                                                                       */
        /*   Object Lock Process                                                 */
        /*                                                                       */
        /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/

        /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/
        /*                                                                       */
        /*   Check Process                                                       */
        /*                                                                       */
        /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/
        log.debug("step1 - Transaction ID and eqp Category Consistency Check");
        equipmentMethod.equipmentCategoryVsTxIDCheckCombination(objCommon, equipmentID);

        /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/
        /*                                                                       */
        /*   Main Process                                                        */
        /*                                                                       */
        /*=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/

        /*--------------------------------------------------*/
        /*                                                  */
        /*   Get eqp's Start Reserved controljob ID   */
        /*                                                  */
        /*--------------------------------------------------*/
        log.debug("step2 - Get eqp's Start Reserved controljob ID");
        List<Infos.StartReservedControlJobInfo> startReservedControlJobInfos = equipmentMethod.equipmentReservedControlJobIDGetDR(objCommon, equipmentID);

        /*-------------------------------------------------*/
        /*                                                 */
        /*   Get cassette's Start Reserved controljob ID   */
        /*                                                 */
        /*-------------------------------------------------*/
        log.debug("step3 - Get cassette's Start Reserved controljob ID");
        ObjectIdentifier saveControlJobID = null;
        for (int i = 0; i < cassetteIDList.size(); i++) {
            ObjectIdentifier cassetteID = cassetteIDList.get(i);
            ObjectIdentifier cassetteControlJobResult = cassetteMethod.cassetteControlJobIDGet(objCommon, cassetteID);
            Boolean findFlag = false;
            for (int j = 0; j < CimArrayUtils.getSize(startReservedControlJobInfos); j++) {
                if (ObjectIdentifier.equalsWithValue(startReservedControlJobInfos.get(j).getControlJobID(), cassetteControlJobResult)) {
                    findFlag = true;
                    break;
                }
            }
            log.trace("cassetteControlJobResult is 【{}】",cassetteControlJobResult);
            if (ObjectIdentifier.isNotEmptyWithValue(cassetteControlJobResult)) {
                saveControlJobID = cassetteControlJobResult;
                Validations.check(!findFlag, retCodeConfig.getUnmatchControljobEqpVsCast());
            } else {
                Validations.check(findFlag, retCodeConfig.getUnmatchControljobEqpVsCast());
            }
        }

        /*----------------------------------------------------*/
        /*                                                    */
        /*   Get Start Information for each cassette / lot    */
        /*                                                    */
        /*----------------------------------------------------*/
        boolean slotmapConflictWarnFlag = false;
        String capacityStr = StandardProperties.OM_CARRIER_STORE_SIZE.getValue();
        Integer capacity = CimObjectUtils.isEmpty(capacityStr) ? 0 : Integer.parseInt(capacityStr);
        log.trace("saveControlJobID is 【{}】",saveControlJobID);
        if (ObjectIdentifier.isNotEmptyWithValue(saveControlJobID)) {
            log.debug("step4 - get control job start reserve information");
            Outputs.ObjControlJobStartReserveInformationOut reserveInformationResult =
                    controlJobMethod.controlJobStartReserveInformationGet(objCommon,
                            saveControlJobID,
                            false);
            lotsMoveInInfoInqResult.setStartCassetteList(reserveInformationResult.getStartCassetteList());
        } else {
            log.debug("step5 - get process start reserve information by cassette");
            List<Infos.StartCassette> objProcessStartReserveInformationGetByCassetteOut = processMethod.processStartReserveInformationGetByCassette(
                    objCommon,
                    params.getEquipmentID(),
                    params.getCassetteIDs(),
                    false);
            lotsMoveInInfoInqResult.setStartCassetteList(objProcessStartReserveInformationGetByCassetteOut);
            String tmpFPCAdoptFlagStr = StandardProperties.OM_DOC_ENABLE_FLAG.getValue();
            int tmpFPCAdoptFlag = CimObjectUtils.isEmpty(tmpFPCAdoptFlagStr) ? 0 : Integer.parseInt(tmpFPCAdoptFlagStr);
            log.trace("tmpFPCAdoptFlag is 【{}】",tmpFPCAdoptFlag);
            if (1 == tmpFPCAdoptFlag) {
                for (int i = 0; i < CimArrayUtils.getSize(objProcessStartReserveInformationGetByCassetteOut); i++) {
                    for (int j = 0; j < CimArrayUtils.getSize(objProcessStartReserveInformationGetByCassetteOut.get(i).getLotInCassetteList()); j++) {
                        if (CimBooleanUtils.isFalse(objProcessStartReserveInformationGetByCassetteOut.get(i).getLotInCassetteList().get(j).getMoveInFlag())) {
                            continue;
                        }
                        ObjectIdentifier lotID = objProcessStartReserveInformationGetByCassetteOut.get(i).getLotInCassetteList().get(j).getLotID();
                        log.debug("step6 - get lot current FPC info");
                        List<Infos.FPCInfo> lotCurrentFPCInfoGet = lotMethod.lotCurrentFPCInfoGet(objCommon, lotID, params.getEquipmentID(), false, false, false, false);

                        Validations.check(!CimArrayUtils.isEmpty(lotCurrentFPCInfoGet), retCodeConfig.getFpcRequireStartReserve());
                    }
                }
            }
        }
        // TODO: 2018/8/10
        log.debug("step7 - startCassetteProcessJobExecFlagSet");
        Outputs.ObjStartCassetteProcessJobExecFlagSetOut startCassetteProcessJobExecFlagSetOutRetCode = null;
        Integer retCode = 0;
        try {
            startCassetteProcessJobExecFlagSetOutRetCode = startCassetteMethod.startCassetteProcessJobExecFlagSet(objCommon, lotsMoveInInfoInqResult.getStartCassetteList(), params.getEquipmentID());
        }catch (ServiceException ex){
            if (!Validations.isEquals(retCodeConfig.getSmplSlotmapConflictWarn(), ex.getCode())){
                // When errors have occurred, hold all the error lot and send e-mail to notify that.
                throw ex;
            }
            slotmapConflictWarnFlag = true;
            startCassetteProcessJobExecFlagSetOutRetCode = (Outputs.ObjStartCassetteProcessJobExecFlagSetOut)ex.getData();
            retCode = ex.getCode();
        }
        //-------------------------------------------------------
        // When RC_SMPL_SLOTMAP_CONFLICT_WARN is returned,
        // return RC_SMPL_SLOTMAP_CONFLICT_WARN at the last of TX process.
        //-------------------------------------------------------
        lotsMoveInInfoInqResult.setStartCassetteList(startCassetteProcessJobExecFlagSetOutRetCode.getStartCassettes());

        //-------------------------------------------------------
        // create return message or e-mail text.
        //-------------------------------------------------------
        // txAlertMessageRpt
        StringBuilder smplMessage = new StringBuilder();
        List<Infos.ObjSamplingMessageAttribute> samplingMessageList = startCassetteProcessJobExecFlagSetOutRetCode.getSamplingMessage();
        log.trace("samplingMessageList is 【{}】",samplingMessageList);
        if (!CimObjectUtils.isEmpty(samplingMessageList)){
            int count = 0;
            for (Infos.ObjSamplingMessageAttribute objSamplingMessageAttribute : samplingMessageList){
                log.trace("MessageType is 【{}】",objSamplingMessageAttribute.getMessageType());
                if (objSamplingMessageAttribute.getMessageType() == BizConstant.SP_SAMPLING_WARN_MAIL){
                    if (count > 0){
                        smplMessage.append(",");
                    }
                    smplMessage.append(objSamplingMessageAttribute.getMessageText());
                } else if (objSamplingMessageAttribute.getMessageType() == BizConstant.SP_SAMPLING_IGNORED_MAIL){
                    Params.AlertMessageRptParams alertMessageRptParams = new Params.AlertMessageRptParams();
                    alertMessageRptParams.setSubSystemID(BizConstant.SP_SUBSYSTEMID_MM);
                    alertMessageRptParams.setSystemMessageCode(BizConstant.SP_SYSTEMMSGCODE_SMPLERR);
                    alertMessageRptParams.setSystemMessageText(objSamplingMessageAttribute.getMessageText());
                    alertMessageRptParams.setNotifyFlag(true);
                    alertMessageRptParams.setLotID(objSamplingMessageAttribute.getLotID());
                    alertMessageRptParams.setSystemMessageTimeStamp(CimDateUtils.convertToSpecString(objCommon.getTimeStamp().getReportTimeStamp()));
                    systemService.sxAlertMessageRpt(objCommon, alertMessageRptParams);
                }
            }
        }

        /*------------------------------------------*/
        /*                                          */
        /*   Set EqpID, ControlJobID, PortGroupID   */
        /*                                          */
        /*------------------------------------------*/
        lotsMoveInInfoInqResult.setEquipmentID(params.getEquipmentID());
        log.trace("saveControlJobID is 【{}】",saveControlJobID);
        if (!ObjectIdentifier.isEmpty(saveControlJobID)) {
            lotsMoveInInfoInqResult.setControlJobID(saveControlJobID);
        }
        log.debug("step8 - get equipment port info");
        Infos.EqpPortInfo eqpPortInfo = equipmentMethod.equipmentPortInfoGet(objCommon, params.getEquipmentID());
        for (int i = 0; i < eqpPortInfo.getEqpPortStatuses().size(); i++) {
            ObjectIdentifier ePSIdent = eqpPortInfo.getEqpPortStatuses().get(i).getLoadedCassetteID();
            ObjectIdentifier inCIDIdent = params.getCassetteIDs().get(0);
            if (ObjectIdentifier.equalsWithValue(inCIDIdent, ePSIdent)) {
                lotsMoveInInfoInqResult.setPortGroupID(eqpPortInfo.getEqpPortStatuses().get(i).getPortGroup());
            }
        }
        /*------------------------------------------------------*/
        /*   Get Info for StartReservation use APC I/F or not   */
        /*------------------------------------------------------*/
        log.debug("step9 - check apc interface flag");
        boolean apcInterfaceFlag = apcMethod.apcInterfaceFlagCheck(objCommon, lotsMoveInInfoInqResult.getStartCassetteList());
        log.trace("apcInterfaceFlag is 【{}】",apcInterfaceFlag);
        if (apcInterfaceFlag){
            /*-------------------------------------------------*/
            /*   Get from APCInterface AdjustRecipeParameter   */
            /*-------------------------------------------------*/
            log.debug("step10 -Get from APCInterface AdjustRecipeParameter ");
            List<Infos.StartCassette> apcStartCassettes = apcMethod.APCMgrSendRecipeParamInq(objCommon, lotsMoveInInfoInqResult.getEquipmentID(), lotsMoveInInfoInqResult.getPortGroupID(), lotsMoveInInfoInqResult.getControlJobID(), lotsMoveInInfoInqResult.getStartCassetteList(),BizConstant.SP_OPERATION_OPESTART);
            lotsMoveInInfoInqResult.setStartCassetteList(apcStartCassettes);
        }
        log.trace("slotmapConflictWarnFlag is 【{}】",slotmapConflictWarnFlag);
        if (CimBooleanUtils.isTrue(slotmapConflictWarnFlag)) {
            throw new ServiceException(new OmCode(retCodeConfig.getSmplSlotmapConflictWarn(), smplMessage.toString()));
        }
        //add MES-EAP Integration cassetteChangeFlag
        log.debug("step11 - ctrl process job level");
        lotsMoveInInfoInqResult.setProcessJobLevelCtrl(equipmentMethod.equipmentBRInfoGetDR(objCommon, equipmentID).isProcessJobLevelCtrl());
        return lotsMoveInInfoInqResult;
    }

    @Override
    public Results.LotsMoveInInfoInqResult sxLotsMoveInInfoForIBInq(Infos.ObjCommon objCommon, Params.LotsMoveInInfoForIBInqParams params) {

        Results.LotsMoveInInfoInqResult out = new Results.LotsMoveInInfoInqResult();

        //=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
        //   Check Process
        //=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
        log.debug("step1 - Transaction ID and eqp Category Consistency Check");
        equipmentMethod.equipmentCategoryVsTxIDCheckCombination(objCommon, params.getEquipmentID());
        //=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
        //   Main Process
        //=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=

        //--------------------------------------------------
        //   Get eqp's Start Reserved controljob IDs
        //--------------------------------------------------
        log.debug("step2 - Get eqp's Start Reserved controljob IDs");
        List<Infos.StartReservedControlJobInfo> startReservedControlJobInfos = equipmentMethod.equipmentReservedControlJobIDGetDR(objCommon, params.getEquipmentID());
        //--------------------------------------------------
        //   Get Input Cassettes' Start Reserved controljob IDs
        //--------------------------------------------------
        log.debug("step3 - Get Input Cassettes' Start Reserved controljob IDs");
        List<ObjectIdentifier> cassetteControlJobIDs = new ArrayList<>();
        log.trace("CassetteID is 【{}】",params.getCassetteIDs());
        if (!CimArrayUtils.isEmpty(params.getCassetteIDs())) {
            for (ObjectIdentifier Cassette : params.getCassetteIDs()) {
                ObjectIdentifier cassetteControlJobID = cassetteMethod.cassetteControlJobIDGet(objCommon, Cassette);
                cassetteControlJobIDs.add(cassetteControlJobID);
            }
        }
        //--------------------------------------------------
        //   Check all control job of input cassette is the same or not
        //--------------------------------------------------
        for (int i = 1; i < cassetteControlJobIDs.size(); i++) {
            if (!ObjectIdentifier.equalsWithValue(cassetteControlJobIDs.get(0), cassetteControlJobIDs.get(i))) {
                throw new ServiceException(retCodeConfig.getUnmatchCassetteCombination());
            }
        }
        //--------------------------------------------------
        //   Check cassette's control job id is also assigned for eqp or not.
        //--------------------------------------------------
        if (!CimStringUtils.isEmpty(ObjectIdentifier.fetchValue(cassetteControlJobIDs.get(0)))) {
            Boolean bControlJobFound = false;
            log.trace("startReservedControlJobInfos is 【{}】",startReservedControlJobInfos);
            if (!CimArrayUtils.isEmpty(startReservedControlJobInfos)) {
                for (Infos.StartReservedControlJobInfo startReservedControlJobInfo : startReservedControlJobInfos) {
                    if (ObjectIdentifier.equalsWithValue(cassetteControlJobIDs.get(0), startReservedControlJobInfo.getControlJobID())) {
                        bControlJobFound = true;
                        break;
                    }
                }
            }
            Validations.check(!bControlJobFound, retCodeConfig.getUnmatchControljobEqpVsCast());
        }
        ObjectIdentifier saveControlJobID = cassetteControlJobIDs.get(0);
        //----------------------------------------------------
        //   Get Start Information for each cassette / lot
        //----------------------------------------------------
        Boolean slotmapConflictWarnFlag = false;
        StringBuilder smplMessage = new StringBuilder();
        log.trace("saveControlJobID is 【{}】",saveControlJobID);
        if (!ObjectIdentifier.isEmpty(saveControlJobID)) {
            log.debug("step5 - Get Started lot information which is sepcified with controljob ID");
            Outputs.ObjControlJobStartReserveInformationOut controlJobStartReserveInformationOut =
                    controlJobMethod.controlJobStartReserveInformationGet(objCommon,
                            saveControlJobID,
                            false);

            out.setStartCassetteList(controlJobStartReserveInformationOut.getStartCassetteList());
        } else {
            log.debug("step6 - get process start reserve information by cassette");
            Outputs.ObjProcessStartReserveInformationGetByCassetteOut processStartReserveInformationGetByCassetteOutRetCode =
                    processMethod.processStartReserveInformationGetByCassetteForInternalBuffer(objCommon,
                            params.getEquipmentID(),
                            params.getCassetteIDs(),
                            false);
            List<Infos.StartCassette> startCassetteList = processStartReserveInformationGetByCassetteOutRetCode.getStartCassetteList();
            out.setStartCassetteList(startCassetteList);
            //【done】- FPCAdoptFlag
            log.trace("SP_FPC_ADAPTATION_FLAG is 【{}】",StandardProperties.OM_DOC_ENABLE_FLAG.getValue());
            if (StandardProperties.OM_DOC_ENABLE_FLAG.isTrue()) {
                for (Infos.StartCassette cassette : startCassetteList) {
                    List<Infos.LotInCassette> lotInCassetteList = cassette.getLotInCassetteList();
                    log.trace("lotInCassetteList is 【{}】",lotInCassetteList);
                    if (!CimObjectUtils.isEmpty(lotInCassetteList)) {
                        for (Infos.LotInCassette lotInCassette : lotInCassetteList) {
                            log.trace("MoveInFlag is 【{}】",lotInCassette.getMoveInFlag());
                            if (CimBooleanUtils.isFalse(lotInCassette.getMoveInFlag())) {
                                continue;
                            }
                            ObjectIdentifier lotID = lotInCassette.getLotID();

                            log.debug("step7 - get lot current FPC info");
                            List<Infos.FPCInfo> lotCurrentFPCInfoGet = lotMethod.lotCurrentFPCInfoGet(objCommon, lotID, params.getEquipmentID(), false, false, false, false);
                            Validations.check(!CimArrayUtils.isEmpty(lotCurrentFPCInfoGet), retCodeConfig.getFpcRequireStartReserve());
                        }
                    }
                }
            }

            Outputs.ObjStartCassetteProcessJobExecFlagSetOut startCassetteOut = null;
            int retCode = 0;
            log.debug("step8 - startCassetteProcessJobExecFlagSet");
            try {
                startCassetteOut = startCassetteMethod.startCassetteProcessJobExecFlagSet(objCommon, out.getStartCassetteList(), params.getEquipmentID());
            } catch (ServiceException e) {
                retCode = e.getCode();
                if (!Validations.isEquals(retCodeConfig.getSmplSlotmapConflictWarn(), e.getCode())){
                    throw e;
                }
            }
            log.trace("SmplSlotmapConflictWarn is 【{}】，retCode is 【{}】",retCodeConfig.getSmplSlotmapConflictWarn(), retCode);
            if (Validations.isEquals(retCodeConfig.getSmplSlotmapConflictWarn(), retCode)){
                slotmapConflictWarnFlag = true;
            }
            //-------------------------------------------------------
            // create return message or e-mail text.
            //-------------------------------------------------------
            log.trace("startCassetteOut is 【{}】",startCassetteOut);
            if (!CimObjectUtils.isEmpty(startCassetteOut)) {
                List<Infos.ObjSamplingMessageAttribute> samplingMessageList = startCassetteOut.getSamplingMessage();
                log.trace("samplingMessageList is 【{}】",samplingMessageList);
                if (!CimObjectUtils.isEmpty(samplingMessageList)) {
                    int messageCount = 0;
                    for (Infos.ObjSamplingMessageAttribute samplingMessageAttribute : samplingMessageList) {
                        log.trace("MessageType is 【{}】",samplingMessageAttribute.getMessageType());
                        if (samplingMessageAttribute.getMessageType() == BizConstant.SP_SAMPLING_WARN_MAIL) {
                            log.trace("messageCount is 【{}】",messageCount);
                            if (messageCount > 0) {
                                smplMessage.append(",");
                            }
                            smplMessage.append(samplingMessageAttribute.getMessageText());
                            messageCount++;
                        } else if (samplingMessageAttribute.getMessageType() == BizConstant.SP_SAMPLING_IGNORED_MAIL) {
                            log.debug("step9 - Send E-mail when recycle sampling setting is ignored.");
                            Params.AlertMessageRptParams alertMessageRptParams = new Params.AlertMessageRptParams();
                            alertMessageRptParams.setSubSystemID(BizConstant.SP_SUBSYSTEMID_MM);
                            alertMessageRptParams.setSystemMessageCode(BizConstant.SP_SYSTEMMSGCODE_SMPLERR);
                            alertMessageRptParams.setSystemMessageText(samplingMessageAttribute.getMessageText());
                            alertMessageRptParams.setNotifyFlag(true);
                            alertMessageRptParams.setLotID(samplingMessageAttribute.getLotID());
                            alertMessageRptParams.setSystemMessageTimeStamp(CimDateUtils.convertToSpecString(objCommon.getTimeStamp().getReportTimeStamp()));
                            systemService.sxAlertMessageRpt(objCommon, alertMessageRptParams);
                        }
                    }
                }
            }
        }

        //----------------------------------------
        // Change processJobExecFlag to True.
        //----------------------------------------

        //------------------------------------------
        //   Check all carrier of control job is input or not
        //------------------------------------------
        for (Infos.StartCassette startCassette : out.getStartCassetteList()) {
            Boolean bFound = false;
            for (ObjectIdentifier cassetteID : params.getCassetteIDs()) {
                log.trace("startCassette CassetteID is 【{}】，cassetteID is 【{}】",startCassette.getCassetteID(), cassetteID);
                if (ObjectIdentifier.equalsWithValue(startCassette.getCassetteID(), cassetteID)) {
                    bFound = true;
                    break;
                }
            }
            log.trace("bFound is 【{}】",bFound);
            if (!bFound) {
                throw new ServiceException(retCodeConfig.getMissingCassetteOfControljob());
            }
        }
        //------------------------------------------
        //   Set EqpID, ControlJobID, PortGroupID
        //------------------------------------------
        out.setEquipmentID(params.getEquipmentID());
        out.setControlJobID(saveControlJobID);
        //------------------------------------------------------
        //   Get Info for StartReservation use APC I/F or not
        //------------------------------------------------------
        log.debug("step10 - Get Info for StartReservation use APC I/F or not");
        boolean apcInterfaceFlag = apcMethod.apcInterfaceFlagCheck(objCommon, out.getStartCassetteList());
        log.trace("apcInterfaceFlag is 【{}】",apcInterfaceFlag);
        if (apcInterfaceFlag){
            //-------------------------------------------------
            //   Get from APCInterface AdjustRecipeParameter
            //-------------------------------------------------
            log.debug("step11 - Get from APCInterface AdjustRecipeParameter");
            List<Infos.StartCassette> apcStartCassettes = apcMethod.APCMgrSendRecipeParamInq(objCommon, out.getEquipmentID(), out.getPortGroupID(), out.getControlJobID(), out.getStartCassetteList(),BizConstant.SP_OPERATION_OPESTART);
            out.setStartCassetteList(apcStartCassettes);
        }
        //-----------------------
        //   Set out structure
        //-----------------------
        Validations.check(slotmapConflictWarnFlag, new OmCode(retCodeConfig.getSmplSlotmapConflictWarn(), smplMessage.toString()));
        //QianDao add MES-EAP Integration cassetteChangeFlag
        log.debug("step12 - get equipment BR info");
        out.setProcessJobLevelCtrl(equipmentMethod.equipmentBRInfoGetDR(objCommon, params.getEquipmentID()).isProcessJobLevelCtrl());
        return out;
    }

    @Override
    public Results.EqpInfoForIBInqResult sxEqpInfoForIBInq(Infos.ObjCommon objCommon, Params.EqpInfoForIBInqParams params) {
        Results.EqpInfoForIBInqResult internalBufferInqResult = new Results.EqpInfoForIBInqResult();

        ObjectIdentifier equipmentID = params.getEquipmentID();
        boolean requestFlagForBRInfo = params.getRequestFlagForBasicInfo() != null ? params.getRequestFlagForBasicInfo() : false;
        boolean requestFlagForStatusInfo = params.getRequestFlagForStatusInfo() != null ? params.getRequestFlagForStatusInfo() : false;
        boolean requestFlagForPMInfo = params.getRequestFlagForPMInfo() != null ? params.getRequestFlagForPMInfo() : false;
        boolean requestFlagForPortInfo = params.getRequestFlagForPortInfo() != null ? params.getRequestFlagForPortInfo() : false;
        boolean requestFlagForChamberInfo = params.getRequestFlagForChamberInfo() != null ? params.getRequestFlagForChamberInfo() : false;
        boolean requestFlagForStockerInfo = params.getRequestFlagForStockerInfo() != null ? params.getRequestFlagForStockerInfo() : false;
        boolean requestFlagForInprocessingLotInfo = params.getRequestFlagForInprocessingLotInfo() != null ? params.getRequestFlagForInprocessingLotInfo() : false;
        boolean requestFlagForReservedControlJobInfo = params.getRequestFlagForReservedControlJobInfo() != null ? params.getRequestFlagForReservedControlJobInfo() : false;
        boolean requestFlagForRSPPortInfo = params.getRequestFlagForRSPPortInfo() != null ? params.getRequestFlagForRSPPortInfo() : false;
        boolean requestFlagForInternalBufferInfo = params.getRequestFlagForInternalBufferInfo() != null ? params.getRequestFlagForInternalBufferInfo() : false;

        log.debug("step1 - Transaction ID and eqp Category Consistency Check");
        equipmentMethod.equipmentCategoryVsTxIDCheckCombination(objCommon, equipmentID);

        log.debug("step2 - Query equipment and controlLotBank data to add value to eqpBasicInfoForInternalBuffer");
        log.trace("requestFlagForBRInfo is {}",requestFlagForBRInfo);
        if (requestFlagForBRInfo) {
            Infos.EqpBrInfoForInternalBuffer brInfoForInternalBufferRetCode = equipmentMethod.equipmentBrInfoForInternalBufferGetDR(objCommon, equipmentID);
            internalBufferInqResult.setEqpBasicInfoForInternalBuffer(brInfoForInternalBufferRetCode);
        }


        log.debug("step3 - query equipment for status related data to add value to equipmentStatusInfo");
        log.trace("requestFlagForStatusInfo is {}",requestFlagForStatusInfo);
        if (requestFlagForStatusInfo) {
            Infos.EqpStatusInfo eqpStatusInfo = equipmentMethod.equipmentStatusInfoGetDR(objCommon, equipmentID);
            internalBufferInqResult.setEquipmentStatusInfo(eqpStatusInfo);
        }

        log.debug("step4 - query PM info after check equipment current state and e10 state");
        log.trace("requestFlagForPMInfo is {}",requestFlagForPMInfo);
        if (requestFlagForPMInfo) {
            Infos.EqpPMInfo eqpPMInfo = equipmentMethod.equipmentPMInfoGetDR(objCommon, equipmentID);
            internalBufferInqResult.setEquipmentPMInfo(eqpPMInfo);
        }

        log.debug("step5 - query equipment port info");
        log.trace("requestFlagForPortInfo is {}",requestFlagForPortInfo);
        if (requestFlagForPortInfo) {
            Infos.EqpPortInfo  portInfoRetCode = equipmentMethod.equipmentPortInfoForInternalBufferGetDR(objCommon, equipmentID);
            internalBufferInqResult.setEquipmentPortInfo(portInfoRetCode);
        }

        log.debug("step6 - query chamber data from equipment");
        log.trace("requestFlagForChamberInfo is {}",requestFlagForChamberInfo);
        if (requestFlagForChamberInfo) {
            Infos.EqpChamberInfo eqpChamberInfo = equipmentMethod.equipmentChamberInfoGetDR(objCommon, equipmentID);
            internalBufferInqResult.setEquipmentChamberInfo(eqpChamberInfo);
        }

        log.debug("step7 - query internal buffer info from equipment");
        log.trace("requestFlagForInternalBufferInfo is {}",requestFlagForInternalBufferInfo);
        if (requestFlagForInternalBufferInfo) {
            List<Infos.EqpInternalBufferInfo> internalBufferInfoRetCode = equipmentMethod.equipmentInternalBufferInfoGet(objCommon, equipmentID);
            internalBufferInqResult.setEqpInternalBufferInfos(internalBufferInfoRetCode);
        }

        log.debug("step8 - query stocker info and UTS info from equipment");
        log.trace("requestFlagForStockerInfo is {}",requestFlagForStockerInfo);
        if (requestFlagForStockerInfo) {
            Infos.EqpStockerInfo eqpStockerInfo = equipmentMethod.equipmentStockerInfoGetDR(objCommon, equipmentID);
            internalBufferInqResult.setEquipmentStockerInfo(eqpStockerInfo);

            Infos.EqpStockerInfo objEquipmentUTSInfoGetDROut = equipmentMethod.equipmentUTSInfoGetDR(objCommon, equipmentID);
            internalBufferInqResult.setEquipmentOHBInfo(objEquipmentUTSInfoGetDROut);
        }

        log.debug("step9 - query inprocessing control job info from equipment");
        log.trace("requestFlagForInprocessingLotInfo is {}",requestFlagForInprocessingLotInfo);
        if (requestFlagForInprocessingLotInfo) {
            List<Infos.EqpInprocessingControlJob> eqpInprocessingControlJobs = equipmentMethod.equipmentInprocessingControlJobInfoGetDR(objCommon, equipmentID);
            internalBufferInqResult.setEquipmentInprocessingControlJobList(eqpInprocessingControlJobs);
        }

        log.debug("step10 - query reserved control job info from equipment");
        log.trace("requestFlagForReservedControlJobInfo is {}",requestFlagForReservedControlJobInfo);
        if (requestFlagForReservedControlJobInfo) {
            List<Infos.StartReservedControlJobInfo> startReservedControlJobInfos = equipmentMethod.equipmentReservedControlJobIDGetDR(objCommon, equipmentID);
            Infos.EqpReservedControlJobInfo equipmentReservedControlJobInfo = new Infos.EqpReservedControlJobInfo();
            equipmentReservedControlJobInfo.setMoveInReservedControlJobInfoList(startReservedControlJobInfos);
            internalBufferInqResult.setEquipmentReservedControlJobInfo(equipmentReservedControlJobInfo);
        }

        log.debug("step11 - query inprocessing durable control job from equipment");
        internalBufferInqResult.setEquipmentInprocessingDurableControlJobs(equipmentMethod.equipmentInprocessingDurableControlJobIDGetDR(objCommon, equipmentID));

        log.debug("step12 - query reserved durable control job from equipment");
        internalBufferInqResult.setEquipmentReservedDurableControlJobs(equipmentMethod.equipmentReservedDurableControlJobIDGetDR(objCommon, equipmentID));

        log.debug("step13 - query additional reticle attribute from equipment");
        log.trace("requestFlagForRSPPortInfo is {}",requestFlagForRSPPortInfo);
        if (requestFlagForRSPPortInfo) {
            log.trace("SP_ARMS_FUNC_ENABLED : {}", StandardProperties.OM_ARHS_FLAG.getValue());
            if(CimStringUtils.equals("1", StandardProperties.OM_ARHS_FLAG.getValue())){
                //Reticle Pod port information is required.
                Outputs.ObjEquipmentReticlePodPortInfoGetDROut reticlePodPortInfoRetCode = null;
                try {
                    reticlePodPortInfoRetCode = equipmentMethod.equipmentReticlePodPortInfoGetDR(objCommon, equipmentID);
                    Infos.EquipmentAdditionalReticleAttribute equipmentAdditionalReticleAttribute = new Infos.EquipmentAdditionalReticleAttribute();
                    internalBufferInqResult.setEquipmentAdditionalReticleAttribute(equipmentAdditionalReticleAttribute);
                    equipmentAdditionalReticleAttribute.setReticlePodPortIDList(reticlePodPortInfoRetCode.getReticlePodPortInfoList());
                    equipmentAdditionalReticleAttribute.setReticleStoreMaxCount(reticlePodPortInfoRetCode.getReticleStoreMaxCount());
                    equipmentAdditionalReticleAttribute.setReticleStoreLimitCount(reticlePodPortInfoRetCode.getReticleStoreLimitCount());
                }catch (ServiceException e) {
                    if (!Validations.isEquals(retCodeConfig.getRspportNotFound(), e.getCode())) {
                        throw e;
                    }
                }

                //-------------------------------------------------
                // get reticle list in eqp.
                // input eqp ID and get reticle ID list
                //-------------------------------------------------

                //Step13 - txReticleListInq__170
                Params.ReticleListInqParams reticleListInqParams = new Params.ReticleListInqParams();
                /*
                reticleListInqParams.setReticleGroupID(null);
                reticleListInqParams.setReticlePartNumber(null);
                reticleListInqParams.setReticleID(null);
                reticleListInqParams.setLotID(null);
                reticleListInqParams.setReticleStatus(null);
                reticleListInqParams.setDurableSubStatus(null);
                reticleListInqParams.setFlowStatus(null);
                reticleListInqParams.setFPCCategory(null);
                reticleListInqParams.setBankID(null);
                */
                reticleListInqParams.setMaxRetrieveCount(0);
                reticleListInqParams.setEquipmentID(equipmentID);
                reticleListInqParams.setWhiteDefSearchCriteria(BizConstant.SP_WHITEDEF_SEARCHCRITERIA_ALL);

                Results.ReticleListInqResult reticleListInqResult = null;
                try {
                    log.debug("step13.1 - query reticle list");
                    reticleListInqResult = durableInqService.sxReticleListInq(objCommon,reticleListInqParams);
                    List<Infos.FoundReticle> foundReticles = reticleListInqResult.getStrFoundReticle();
                    List<Infos.StoredReticle> storedReticleList = new ArrayList<>();
                    for (Infos.FoundReticle foundReticle : foundReticles) {
                        if (!CimStringUtils.equals(BizConstant.SP_TRANSSTATE_EQUIPMENTIN, foundReticle.getReticleStatusInfo().getTransferStatus())) {
                            continue;
                        }

                        if (ObjectIdentifier.isNotEmptyWithValue(foundReticle.getReticleStatusInfo().getReticlePodID())) {
                            continue;
                        }

                        Infos.StoredReticle storedReticle = new Infos.StoredReticle();
                        storedReticle.setReticleID(foundReticle.getReticleID());
                        storedReticle.setDescription(foundReticle.getDescription());
                        storedReticle.setReticleGroupID(foundReticle.getReticleGroupID());
                        storedReticle.setStatus(foundReticle.getReticleStatusInfo().getReticleStatus());
                        storedReticleList.add(storedReticle);
                    }
                    internalBufferInqResult.getEquipmentAdditionalReticleAttribute().setStoredReticleList(storedReticleList);
                }catch (ServiceException ex){
                    Validations.check(ex.getCode()!=retCodeConfig.getNoNeedReticle().getCode() && ex.getCode()!=retCodeConfig.getNotFoundReticle().getCode(),
                            retCodeConfig.getNotFoundReticlePod());
                }

            }
        }

        List<ObjectIdentifier> chamberIDs = new ArrayList<>();
        log.trace("requestFlagForChamberInfo is {}",requestFlagForChamberInfo);
        if (requestFlagForChamberInfo) {
            List<Infos.EqpChamberStatusInfo> chamberStatuses = internalBufferInqResult.getEquipmentChamberInfo().getEqpChamberStatuses();
            for (Infos.EqpChamberStatusInfo eqpChamberStatusInfo : chamberStatuses) {
                chamberIDs.add(eqpChamberStatusInfo.getChamberID());
            }
        }

        log.debug("step14 - Use PosMachine and related CLASS");
        List<Infos.EntityInhibitAttributes> entityInhibitAttributes = equipmentMethod.equipmentFillInTxEQQ004(objCommon, equipmentID, chamberIDs);
        internalBufferInqResult.setConstraintList(entityInhibitAttributes);

        internalBufferInqResult.setEquipmentID(equipmentID);

        return internalBufferInqResult;
    }

    @Override
    public Results.EqpInfoInqResult sxEqpInfoInq(Infos.ObjCommon objCommon, Params.EqpInfoInqParams eqpInfoInqParams) {
        Results.EqpInfoInqResult result = new Results.EqpInfoInqResult();

        ObjectIdentifier equipmentID = eqpInfoInqParams.getEquipmentID();
        boolean requestFlagForBRInfo = eqpInfoInqParams.isRequestFlagForBasicInfo();
        boolean requestFlagForStatusInfo = eqpInfoInqParams.isRequestFlagForStatusInfo();
        boolean requestFlagForPMInfo = eqpInfoInqParams.isRequestFlagForPMInfo();
        boolean requestFlagForPortInfo = eqpInfoInqParams.isRequestFlagForPortInfo();
        boolean requestFlagForChamberInfo = eqpInfoInqParams.isRequestFlagForChamberInfo();
        boolean requestFlagForStockerInfo = eqpInfoInqParams.isRequestFlagForStockerInfo();
        boolean requestFlagForInprocessingLotInfo = eqpInfoInqParams.isRequestFlagForInprocessingLotInfo();
        boolean requestFlagForReservedControlJobInfo = eqpInfoInqParams.isRequestFlagForReservedControlJobInfo();
        boolean requestFlagForRSPPortInfo = eqpInfoInqParams.isRequestFlagForRSPPortInfo();
        boolean requestFlagForEqpContainerInfo = eqpInfoInqParams.isRequestFlagForEqpContainerInfo();

        // add season, 此处无法检查recipe_group
//         result.setSeasonList(seasonMethod.checkTheActiveSeason(equipmentID,null));

        log.debug("step1 - Transaction ID and eqp Category Consistency Check");
        equipmentMethod.equipmentCategoryVsTxIDCheckCombination(objCommon, equipmentID);

        log.debug("step2 - get equipment BR info");
        log.trace("requestFlagForBRInfo is 【{}】",requestFlagForBRInfo);
        if (requestFlagForBRInfo) {
            result.setEquipmentBasicInfo(equipmentMethod.equipmentBRInfoGetDR(objCommon, equipmentID));
        }

        log.debug("step3 - get equipment status info");
        log.trace("requestFlagForStatusInfo is 【{}】",requestFlagForStatusInfo);
        if (requestFlagForStatusInfo) {
            result.setEquipmentStatusInfo(equipmentMethod.equipmentStatusInfoGetDR(objCommon, equipmentID));
        }

        log.debug("step4 - get equipment PM info");
        log.trace("requestFlagForPMInfo is 【{}】",requestFlagForPMInfo);
        if (requestFlagForPMInfo) {
            result.setEquipmentPMInfo(equipmentMethod.equipmentPMInfoGetDR(objCommon, equipmentID));
        }

        log.debug("step5 - get equipment port info");
        log.trace("requestFlagForPortInfo is 【{}】",requestFlagForPortInfo);
        if (requestFlagForPortInfo) {
            result.setEquipmentPortInfo(equipmentMethod.equipmentPortInfoGet(objCommon, equipmentID));
        }

        log.debug("step6 - get equipment chamber info");
        log.trace("requestFlagForChamberInfo is 【{}】",requestFlagForChamberInfo);
        if (requestFlagForChamberInfo) {
            result.setEquipmentChamberInfo(equipmentMethod.equipmentChamberInfoGetDR(objCommon, equipmentID));
        }

        log.trace("requestFlagForStockerInfo is 【{}】",requestFlagForStockerInfo);
        if (requestFlagForStockerInfo) {
            log.debug("step7 - get equipment stocker info");
            result.setEquipmentStockerInfo(equipmentMethod.equipmentStockerInfoGetDR(objCommon, equipmentID));

            log.debug("step8 - get equipment UTS info");
            result.setEquipmentOHBInfo(equipmentMethod.equipmentUTSInfoGetDR(objCommon, equipmentID));

            log.debug("step9 - get equipment SLMUTS info");
            result.setEquipmentFMCOHBInfo(equipmentMethod.equipmentSLMUTSInfoGetDR(objCommon, equipmentID));
        }

        log.debug("step10 - get equipment in processing control job info");
        log.trace("requestFlagForInprocessingLotInfo is 【{}】",requestFlagForInprocessingLotInfo);
        if (requestFlagForInprocessingLotInfo) {
            result.setEquipmentInprocessingControlJobList(equipmentMethod.equipmentInprocessingControlJobInfoGetDR(objCommon, equipmentID));
        }

        log.debug("step11 - get equipment reserved control job");
        log.trace("requestFlagForReservedControlJobInfo is 【{}】",requestFlagForReservedControlJobInfo);
        if (requestFlagForReservedControlJobInfo) {
            List<Infos.StartReservedControlJobInfo> startReservedControlJobInfos = equipmentMethod.equipmentReservedControlJobIDGetDR(objCommon, equipmentID);
            log.trace("startReservedControlJobInfos is 【{}】",startReservedControlJobInfos);
            if (!CimObjectUtils.isEmpty(startReservedControlJobInfos)) {
                result.setEquipmentReservedControlJobInfo(new Infos.EqpReservedControlJobInfo());
                result.getEquipmentReservedControlJobInfo().setMoveInReservedControlJobInfoList(startReservedControlJobInfos);
            }
        }

        log.debug("step12 - get equipment in processing durable control job");
        result.setEquipmentInprocessingDurableControlJobs(equipmentMethod.equipmentInprocessingDurableControlJobIDGetDR(objCommon, equipmentID));

        log.debug("step13 - get equipment reserved durable control job");
        result.setEquipmentReservedDurableControlJobs(equipmentMethod.equipmentReservedDurableControlJobIDGetDR(objCommon, equipmentID));

        log.trace("requestFlagForRSPPortInfo is 【{}】",requestFlagForRSPPortInfo);
        if (requestFlagForRSPPortInfo) {
            if (BizConstant.equalsIgnoreCase(BizConstant.CONSTANT_QUANTITY_ONE, StandardProperties.OM_ARHS_FLAG.getValue())) {
                //-------------------------------------------------
                // Reticle Pod Port information is required.
                //-------------------------------------------------
                log.debug("step14 - get equipment reticle pod port info");
                Infos.EquipmentAdditionalReticleAttribute equipmentAdditionalReticleAttribute = new Infos.EquipmentAdditionalReticleAttribute();
                result.setEquipmentAdditionalReticleAttribute(equipmentAdditionalReticleAttribute);
                try {
                    Outputs.ObjEquipmentReticlePodPortInfoGetDROut objEquipmentReticlePodPortInfoGetDROut = equipmentMethod.equipmentReticlePodPortInfoGetDR(objCommon, equipmentID);
                    equipmentAdditionalReticleAttribute.setReticlePodPortIDList(objEquipmentReticlePodPortInfoGetDROut.getReticlePodPortInfoList());
                    equipmentAdditionalReticleAttribute.setReticleStoreMaxCount(objEquipmentReticlePodPortInfoGetDROut.getReticleStoreMaxCount());
                    equipmentAdditionalReticleAttribute.setReticleStoreLimitCount(objEquipmentReticlePodPortInfoGetDROut.getReticleStoreLimitCount());

                } catch (ServiceException ex) {
                    OmCode returnCode = new OmCode(ex.getCode(), ex.getMessage());
                    Validations.check(!Validations.isEquals(returnCode, retCodeConfig.getSucc()) && !Validations.isEquals(returnCode, retCodeConfig.getRspportNotFound()), returnCode);
                    if (!Validations.isEquals(returnCode, retCodeConfig.getRspportNotFound())) {
                    }
                }

                //-------------------------------------------------
                // get reticle list in equipment.
                // input equipment ID and get reticle ID list
                //-------------------------------------------------
                log.debug("step15 - get reticle list in equipment");
                Params.ReticleListInqParams reticleListInqParams = new Params.ReticleListInqParams();
                reticleListInqParams.setEquipmentID(equipmentID);
                reticleListInqParams.setMaxRetrieveCount(0);
                reticleListInqParams.setWhiteDefSearchCriteria(BizConstant.SP_DP_CONTROLLOTCAT_ALL);
                Results.ReticleListInqResult reticleListInqResult = null;
                try {
                    reticleListInqResult = durableInqService.sxReticleListInq(objCommon, reticleListInqParams);
                    List<Infos.FoundReticle> strFoundReticle = reticleListInqResult.getStrFoundReticle();
                    List<Infos.StoredReticle> storedReticleList = new ArrayList<>();
                    strFoundReticle.forEach(x -> {
                        if (CimStringUtils.equals(x.getReticleStatusInfo().getTransferStatus(),BizConstant.SP_TRANSSTATE_EQUIPMENTIN)
                                && ObjectIdentifier.isEmpty(x.getReticleStatusInfo().getReticlePodID())){
                            Infos.StoredReticle storedReticle = new Infos.StoredReticle();
                            storedReticle.setReticleID(x.getReticleID());
                            storedReticle.setDescription(x.getDescription());
                            storedReticle.setReticleGroupID(x.getReticleGroupID());
                            storedReticle.setStatus(x.getReticleStatusInfo().getReticleStatus());
                            storedReticleList.add(storedReticle);
                        }
                    });
                    equipmentAdditionalReticleAttribute.setStoredReticleList(storedReticleList);
                } catch (ServiceException ex) {
                    if (!Validations.isEquals(retCodeConfig.getNoNeedReticle(), ex.getCode()) && !Validations.isEquals(ex.getCode(), retCodeConfig.getNotFoundReticle())) {
                        throw ex;
                    }
                    if (Validations.isEquals(retCodeConfig.getNoNeedReticle(), ex.getCode()) || Validations.isEquals(ex.getCode(), retCodeConfig.getNotFoundReticle())) {

                    }
                }
            }
        }

        log.debug("step16 - get equipment container info");
        log.trace("requestFlagForEqpContainerInfo is 【{}】",requestFlagForEqpContainerInfo);
        if (requestFlagForEqpContainerInfo) {
            Infos.EqpContainerInfo objEquipmentContainerInfoGetDROut = equipmentMethod.equipmentContainerInfoGetDR(objCommon, equipmentID);
            log.trace("objEquipmentContainerInfoGetDROut is 【{}】",objEquipmentContainerInfoGetDROut);
            if (!CimObjectUtils.isEmpty(objEquipmentContainerInfoGetDROut)) {
                result.setEqpContainerList(objEquipmentContainerInfoGetDROut.getEqpContainerList());
            }
        }

        List<ObjectIdentifier> chamberIDs = new ArrayList<>();
        log.trace("requestFlagForChamberInfo is 【{}】",requestFlagForChamberInfo);
        if (requestFlagForChamberInfo) {
            Infos.EqpChamberInfo equipmentChamberInfo = result.getEquipmentChamberInfo();
            if (!CimObjectUtils.isEmpty(equipmentChamberInfo)) {
                List<Infos.EqpChamberStatusInfo> chamberStatuses = equipmentChamberInfo.getEqpChamberStatuses();
                chamberIDs = chamberStatuses.stream().map(Infos.EqpChamberStatusInfo::getChamberID).collect(Collectors.toList());
            }
        }
        log.debug("step17 - equipmentFillInTxEQQ004");
        result.setConstraintList(equipmentMethod.equipmentFillInTxEQQ004(objCommon, equipmentID, chamberIDs));
        result.setEquipmentID(equipmentID);
        return result;
    }

    @Override
    public List<Infos.AreaEqp> sxEqpListByStepInq(Infos.ObjCommon objCommon, Params.EqpListByStepInqParm strEqpListByStepInqInParm) {
        log.debug("step1 - get process dispatch equipment");
        List<ObjectIdentifier> equipmentIDs = processMethod.processDispatchEquipmentsGetDR(objCommon,
                strEqpListByStepInqInParm.getProductID(),
                strEqpListByStepInqInParm.getOperationID());

        int nIndex = 0;
        int nLen = CimArrayUtils.getSize(equipmentIDs);
        List<Infos.AreaEqp> strEqpInfoList=new ArrayList<>();
        for( int i = 0; i < nLen; i++ ) {
            ObjectIdentifier workArea=new ObjectIdentifier();
            Infos.EquipmentListInfoGetDROut strEquipmentListInfoGetDROut;
            Infos.EquipmentListInfoGetDRIn strEquipmentListInfoGetDRIn=new Infos.EquipmentListInfoGetDRIn();
            strEquipmentListInfoGetDRIn.setWorkArea(workArea);
            strEquipmentListInfoGetDRIn.setEquipmentID(equipmentIDs.get(i));
            strEquipmentListInfoGetDRIn.setStockerType("");
            strEquipmentListInfoGetDRIn.setFpcCategories(strEqpListByStepInqInParm.getFpcCategories());
            strEquipmentListInfoGetDRIn.setWhiteDefSearchCriteria(strEqpListByStepInqInParm.getWhiteDefSearchCriteria());
            strEquipmentListInfoGetDRIn.setSpecialControl("");

            log.debug("step2 - get equipment list info");
            strEquipmentListInfoGetDROut=equipmentMethod.equipmentListInfoGetDR(objCommon,strEquipmentListInfoGetDRIn);

            List<Infos.AreaEqp> strAreaEqp = strEquipmentListInfoGetDROut.getStrAreaEqp();
            if( CimArrayUtils.getSize(strAreaEqp) > 0 ) {
                log.trace("strAreaEqp size > 0");
                strEqpInfoList.add(strAreaEqp.get(0));
                nIndex++;
            }
        }


        return strEqpInfoList;
    }

    @Override
    public Results.EqpListByBayInqResult sxEqpListByBayInq(Infos.ObjCommon objCommon, Params.EqpListByBayInqInParm paramIn) {
        log.debug("step1 - check input params");
        Validations.check(null == objCommon || null == paramIn, retCodeConfig.getInvalidInputParam());
        Results.EqpListByBayInqResult retVal = null;
        Infos.EquipmentListInfoGetDRIn strEquipmentListInfoGetDRIn = new Infos.EquipmentListInfoGetDRIn();
        strEquipmentListInfoGetDRIn.setWhiteDefSearchCriteria(paramIn.getWhiteDefSearchCriteria());
        strEquipmentListInfoGetDRIn.setEquipmentID(paramIn.getEquipmentID());
        strEquipmentListInfoGetDRIn.setFpcCategories(paramIn.getFpcCategories());
        strEquipmentListInfoGetDRIn.setSpecialControl(paramIn.getSpecialControl());
        strEquipmentListInfoGetDRIn.setStockerType(paramIn.getEquipmentCategory());
        strEquipmentListInfoGetDRIn.setWorkArea(paramIn.getWorkArea());
        strEquipmentListInfoGetDRIn.setSiInfo(paramIn.getSiInfo());
        log.debug("step2 - get Stocker and state info from equipment - equipmentListInfoGetDR");
        Infos.EquipmentListInfoGetDROut equipmentListInfoGetDROut = equipmentMethod.equipmentListInfoGetDR(objCommon, strEquipmentListInfoGetDRIn);
        log.trace("equipmentListInfoGetDROut is 【{}】",equipmentListInfoGetDROut);
        if (null != equipmentListInfoGetDROut) {
            retVal = new Results.EqpListByBayInqResult();
            retVal.setStrAreaEqp(equipmentListInfoGetDROut.getStrAreaEqp());
            retVal.setStrAreaStocker(equipmentListInfoGetDROut.getStrAreaStocker());
            retVal.setWorkArea(equipmentListInfoGetDROut.getWorkArea());
            retVal.setSiInfo(equipmentListInfoGetDROut.getSiInfo());
        }
        return retVal;
    }

    @Override
    public List<ObjectIdentifier> sxAllEqpListByBayInq(Infos.ObjCommon objCommon) {
        log.debug("sxAllEqpListByBayInq start");
        return equipmentMethod.equipmentIDGetDR(objCommon);
    }

    @Override
    public List<Infos.WorkArea> sxBayListInq(Infos.ObjCommon objCommon, ObjectIdentifier userID) {
        log.debug("sxBayListInq start");
        List<Infos.WorkArea> result = null;
        try {
            result = areaMethod.areaFillInTxTRQ014DR(objCommon);
        } catch (ServiceException e) {
            if (!Validations.isEquals(retCodeConfig.getNotFoundEntry(), e.getCode())) {
                throw e;
            }
        }
        return result;
    }

    @Override
    public Results.EqpBufferInfoInqResult sxEqpBufferInfoInq(Infos.ObjCommon objCommon, Params.EqpBufferInfoInqInParm params) {
        log.info("【Entry】 the sxEqpBufferInfoInq() ");

        Results.EqpBufferInfoInqResult eqpBufferInfoInqResult = new Results.EqpBufferInfoInqResult();


        //------------------------------------
        //   Get Buffer Resource Info
        //------------------------------------

        log.debug("step1 -  Get Buffer Resource Info");
        List<Infos.BufferResourceInfo> bufferResourceInfo = equipmentMethod.equipmentBufferResourceInfoGet(objCommon, params.getEquipmentID());


        eqpBufferInfoInqResult.setStrBufferResourceInfoSeq(bufferResourceInfo);

        //------------------------------------
        //   Return to Caller
        //------------------------------------
        log.debug("step2 - Return to Caller");
        return eqpBufferInfoInqResult;
    }

    @Override
    public Results.SpcCheckInfoInqResult spcCheckInfoInq(Params.SpcCheckInfoInqParams spcCheckInfoInqParams) {
        Results.SpcCheckInfoInqResult spcCheckInfoInqResult = new Results.SpcCheckInfoInqResult();
        log.trace("spcCheckInfoInqParams flag is 【{}】",spcCheckInfoInqParams.getFlag());
        if (CimStringUtils.equals(spcCheckInfoInqParams.getFlag(), "Department")){
            String sql = "select DEPARTMENT from fruser";
            List<CimPersonDO> personDOList = cimJpaRepository.query(sql, CimPersonDO.class);
            Set<String> departmentSet = new HashSet<>();
            if (!CimArrayUtils.isEmpty(personDOList)){
                for (CimPersonDO personDO : personDOList){
                    if (!CimStringUtils.isEmpty(personDO.getDepartment())){
                        departmentSet.add(personDO.getDepartment());
                    }
                }
            }
            List<String> departmentList = new ArrayList<>(departmentSet);
            spcCheckInfoInqResult.setDepartment(departmentList);
        } else if (CimStringUtils.equals(spcCheckInfoInqParams.getFlag(), "Process") || CimStringUtils.equals(spcCheckInfoInqParams.getFlag(), "Route")){
            String sql = "select PRP_ID from OMPRP";
            List<CimProcessDefinitionDO> processDefinitionDOList = cimJpaRepository.query(sql, CimProcessDefinitionDO.class);
            List<String> routeList = new ArrayList<>();
            List<String> processList = new ArrayList<>();
            spcCheckInfoInqResult.setRoute(routeList);
            spcCheckInfoInqResult.setProcess(processList);
            if (!CimArrayUtils.isEmpty(processDefinitionDOList)){
                for (CimProcessDefinitionDO cimProcessDefinitionDO : processDefinitionDOList){
                    if (!CimStringUtils.isEmpty(cimProcessDefinitionDO.getProcessDefinitionID())){
                        if (CimStringUtils.equals(spcCheckInfoInqParams.getFlag(), "Process")){
                            processList.add(cimProcessDefinitionDO.getProcessDefinitionID());
                        }
                        if (CimStringUtils.equals(spcCheckInfoInqParams.getFlag(),"Route")){
                            routeList.add(cimProcessDefinitionDO.getProcessDefinitionID());
                        }
                    }
                }
            }
        } else if (CimStringUtils.equals(spcCheckInfoInqParams.getFlag(), "Operation")){
            Set<String> operationSet = new HashSet<>();
            String sql = "select ID from OMPRF where PRP_LEVEL='Main_Mod' AND ACTIVE_FLAG = 1";
            List<CimProcessFlowDO> processFlowDOList = cimJpaRepository.query(sql, CimProcessFlowDO.class);
            if (!CimArrayUtils.isEmpty(processFlowDOList)){
                for (CimProcessFlowDO cimProcessFlowDO : processFlowDOList){
                    sql = "select * from OMPRF_ROUTESEQ where REFKEY=?";
                    List<CimPFDefinitionListDO> pfDefinitionListDOList = cimJpaRepository.query(sql, CimPFDefinitionListDO.class, new Object[]{cimProcessFlowDO.getId()});
                    if (!CimArrayUtils.isEmpty(pfDefinitionListDOList)){
                        for (CimPFDefinitionListDO pfDefinitionListDO : pfDefinitionListDOList){
                            String moduleNo = pfDefinitionListDO.getModuleNO();
                            sql = "select * from OMPRP where id=?";
                            List<CimProcessDefinitionDO> processDefinitionDOList = cimJpaRepository.query(sql, CimProcessDefinitionDO.class, new Object[]{pfDefinitionListDO.getProcessDefinitionObj()});
                            if (!CimArrayUtils.isEmpty(processDefinitionDOList)){
                                for (CimProcessDefinitionDO cimProcessDefinitionDO : processDefinitionDOList){
                                    sql = "select * from OMPRF where ID=?";
                                    List<CimProcessFlowDO> processFlowDOList2 = cimJpaRepository.query(sql, CimProcessFlowDO.class, new Object[]{cimProcessDefinitionDO.getActiveProcessFlowObj()});
                                    if (!CimArrayUtils.isEmpty(processFlowDOList2)){
                                        for (CimProcessFlowDO cimProcessFlowDO2 : processFlowDOList2){
                                            sql = "select * from OMPRF_PRSSSEQ where refkey = ?";
                                            List<CimPFPosListDO> pfPosListDOList = cimJpaRepository.query(sql, CimPFPosListDO.class, new Object[]{cimProcessFlowDO2.getId()});
                                            if (!CimArrayUtils.isEmpty(pfPosListDOList)){
                                                for (CimPFPosListDO cimPFPosListDO : pfPosListDOList){
                                                    String operationNumber = BaseStaticMethod.convertModuleOpeNoToOpeNo(moduleNo, cimPFPosListDO.getDKey());
                                                    operationSet.add(operationNumber);
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
            List<String> operationList = new ArrayList<>(operationSet);
            spcCheckInfoInqResult.setOperation(operationList);
        } else if (CimStringUtils.equals(spcCheckInfoInqParams.getFlag(), "Operation Seq")){
            String sql = "select LINK_KEY from OMPRF_PRSSSEQ";
            List<CimPFPosListDO> pfPosListDOList = cimJpaRepository.query(sql, CimPFPosListDO.class);
            List<String> operationSeq = new ArrayList<>();
            spcCheckInfoInqResult.setOperationSeq(operationSeq);
            if (!CimArrayUtils.isEmpty(pfPosListDOList)){
                for (CimPFPosListDO pfPosListDO : pfPosListDOList){
                    if (!CimStringUtils.isEmpty(pfPosListDO.getDKey())){
                        operationSeq.add(pfPosListDO.getDKey());
                    }
                }
            }
        } else if (CimStringUtils.equals(spcCheckInfoInqParams.getFlag(), "Customer")){
            String sql = "select * from frcustomer";
            List<CimCustomerDO> customerDOList = cimJpaRepository.query(sql, CimCustomerDO.class);
            List<String> customer = new ArrayList<>();
            spcCheckInfoInqResult.setCustomer(customer);
            if (!CimArrayUtils.isEmpty(customerDOList)){
                for (CimCustomerDO cimCustomerDO : customerDOList){
                    if (!CimStringUtils.isEmpty(cimCustomerDO.getCustomerID())){
                        customer.add(cimCustomerDO.getCustomerID());
                    }
                }
            }
        } else if (CimStringUtils.equals(spcCheckInfoInqParams.getFlag(), "LotType")){
            String sql = "select * from frlottype";
            List<CimLotTypeDO> lotTypeDOList = cimJpaRepository.query(sql, CimLotTypeDO.class);
            List<String> lotType = new ArrayList<>();
            spcCheckInfoInqResult.setLotType(lotType);
            if (!CimArrayUtils.isEmpty(lotTypeDOList)){
                for (CimLotTypeDO cimLotTypeDO : lotTypeDOList){
                    if (!CimStringUtils.isEmpty(cimLotTypeDO.getLotType())){
                        lotType.add(cimLotTypeDO.getLotType());
                    }
                }
            }
        } else if (CimStringUtils.equals(spcCheckInfoInqParams.getFlag(), "Product")){
            String sql = "select PROD_ORDER_ID from OMPRORDER";
            List<CimProductRequestDO> productRequestDOList = cimJpaRepository.query(sql, CimProductRequestDO.class);
            List<String> product = new ArrayList<>();
            spcCheckInfoInqResult.setProduct(product);
            if (!CimArrayUtils.isEmpty(productRequestDOList)){
                for (CimProductRequestDO cimProductRequestDO : productRequestDOList){
                    if (!CimStringUtils.isEmpty(cimProductRequestDO.getProductRequestID())){
                        product.add(cimProductRequestDO.getProductRequestID());
                    }
                }
            }
        } else if (CimStringUtils.equals(spcCheckInfoInqParams.getFlag(), "Equipment")){
            String sql = "select EQP_ID from freqp";
            List<CimEquipmentDO> equipmentDOList = cimJpaRepository.query(sql, CimEquipmentDO.class);
            List<String> equipment = new ArrayList<>();
            spcCheckInfoInqResult.setEquipment(equipment);
            if (!CimArrayUtils.isEmpty(equipmentDOList)){
                for (CimEquipmentDO equipmentDO  : equipmentDOList){
                    if (!CimStringUtils.isEmpty(equipmentDO.getEquipmentID())){
                        equipment.add(equipmentDO.getEquipmentID());
                    }
                }
            }
        } else if (CimStringUtils.equals(spcCheckInfoInqParams.getFlag(), "Parameter")){
            String sql = "select NAME from OMEDCPLAN_ITEM";
            List<CimDataCollectionDefItemDO> dataCollectionDefItemDOList = cimJpaRepository.query(sql, CimDataCollectionDefItemDO.class);
            List<String> parameter = new ArrayList<>();
            spcCheckInfoInqResult.setParameter(parameter);
            if (!CimArrayUtils.isEmpty(dataCollectionDefItemDOList)){
                for (CimDataCollectionDefItemDO cimDataCollectionDefItemDO : dataCollectionDefItemDOList){
                    if (!CimStringUtils.isEmpty(cimDataCollectionDefItemDO.getDcItemName())){
                        parameter.add(cimDataCollectionDefItemDO.getDcItemName());
                    }
                }
            }
        }
        return spcCheckInfoInqResult;
    }

    @Override
    public Results.GeneralEqpInfoInqResult sxGeneralEqpInfoInq(Infos.ObjCommon objCommon, Params.CommonEqpInfoParam params) {
        Results.GeneralEqpInfoInqResult generalEqpInfoInqResult = new Results.GeneralEqpInfoInqResult();

        ObjectIdentifier equipmentID = params.getEquipmentID();
        boolean requestFlagForBRInfo = CimBooleanUtils.isTrue(params.getRequestFlagForBRInfo());
        boolean requestFlagForStatusInfo = CimBooleanUtils.isTrue(params.getRequestFlagForStatusInfo());
        boolean requestFlagForPMInfo = CimBooleanUtils.isTrue(params.getRequestFlagForPMInfo());
        boolean requestFlagForPortInfo = CimBooleanUtils.isTrue(params.getRequestFlagForPortInfo());
        boolean requestFlagForChamberInfo = CimBooleanUtils.isTrue(params.getRequestFlagForChamberInfo());
        boolean requestFlagForInternalBufferInfo = CimBooleanUtils.isTrue(params.getRequestFlagForInternalBufferInfo());
        boolean requestFlagForStockerInfo = CimBooleanUtils.isTrue(params.getRequestFlagForStockerInfo());
        boolean requestFlagForInprocessingLotInfo = CimBooleanUtils.isTrue(params.getRequestFlagForInprocessingLotInfo());
        boolean requestFlagForReservedControlJobInfo = CimBooleanUtils.isTrue(params.getRequestFlagForReservedControlJobInfo());
        boolean requestFlagForRSPPortInfo = CimBooleanUtils.isTrue(params.getRequestFlagForRSPPortInfo());
        boolean requestFlagForEqpContainerInfo = CimBooleanUtils.isTrue(params.getRequestFlagForEqpContainerInfo());

        log.debug("step1 - get equipment category");
        String categoryType = equipmentMethod.equipmentCategoryGet(objCommon, equipmentID);

        log.trace("categoryType is 【{}】",categoryType);
        if (BizConstant.SP_MC_CATEGORY_INTERNALBUFFER.equals(categoryType)) {
            Params.EqpInfoForIBInqParams bufferInqParams = new Params.EqpInfoForIBInqParams();
            bufferInqParams.setEquipmentID(equipmentID);
            bufferInqParams.setRequestFlagForBasicInfo(requestFlagForBRInfo);
            bufferInqParams.setRequestFlagForStatusInfo(requestFlagForStatusInfo);
            bufferInqParams.setRequestFlagForPMInfo(requestFlagForPMInfo);
            bufferInqParams.setRequestFlagForPortInfo(requestFlagForPortInfo);
            bufferInqParams.setRequestFlagForChamberInfo(requestFlagForChamberInfo);
            bufferInqParams.setRequestFlagForInternalBufferInfo(requestFlagForInternalBufferInfo);
            bufferInqParams.setRequestFlagForStockerInfo(requestFlagForStockerInfo);
            bufferInqParams.setRequestFlagForInprocessingLotInfo(requestFlagForInprocessingLotInfo);
            bufferInqParams.setRequestFlagForReservedControlJobInfo(requestFlagForReservedControlJobInfo);
            bufferInqParams.setRequestFlagForRSPPortInfo(requestFlagForRSPPortInfo);
            Results.EqpInfoForIBInqResult eqpInfoForIBInqResult = sxEqpInfoForIBInq(objCommon, bufferInqParams);
            Infos.EqpBrInfoForInternalBuffer eqpBrInfoForInternalBuffer = eqpInfoForIBInqResult.getEqpBasicInfoForInternalBuffer();
            //----------------------------------------
            //  Set Equipment Information
            //----------------------------------------
            generalEqpInfoInqResult.setEquipmentID(equipmentID);

            Infos.CommonEqpBrInfo commonEqpBrInfo = new Infos.CommonEqpBrInfo();
            commonEqpBrInfo.setEquipmentName(eqpBrInfoForInternalBuffer.getEquipmentName());
            commonEqpBrInfo.setWorkArea(eqpBrInfoForInternalBuffer.getWorkBay());
            commonEqpBrInfo.setEquipmentOwner(eqpBrInfoForInternalBuffer.getEquipmentOwner());
            commonEqpBrInfo.setTcsResourceName(eqpBrInfoForInternalBuffer.getEqpResourceName());
            commonEqpBrInfo.setEquipmentCategory(eqpBrInfoForInternalBuffer.getEquipmentCategory());
            commonEqpBrInfo.setReticleUseFlag(eqpBrInfoForInternalBuffer.isReticleUseFlag());
            commonEqpBrInfo.setFixtureUseFlag(eqpBrInfoForInternalBuffer.isFixtureUseFlag());
            commonEqpBrInfo.setCassetteChangeFlag(eqpBrInfoForInternalBuffer.isCassetteChangeFlag());
            commonEqpBrInfo.setStartLotsNotifyRequiredFlag(eqpBrInfoForInternalBuffer.isStartLotsNotifyRequiredFlag());
            commonEqpBrInfo.setMonitorCreationFlag(eqpBrInfoForInternalBuffer.isMonitorCreationFlag());
            commonEqpBrInfo.setEqpToEqpTransferFlag(eqpBrInfoForInternalBuffer.isEqpToEqpTransferFlag());
            commonEqpBrInfo.setTakeInOutTransferFlag(eqpBrInfoForInternalBuffer.isTakeInOutTransferFlag());
            commonEqpBrInfo.setEmptyCassetteRequireFlag(eqpBrInfoForInternalBuffer.isEmptyCassetteRequireFlag());
            commonEqpBrInfo.setSlmSwitch("OFF");
            commonEqpBrInfo.setControlBanks(eqpBrInfoForInternalBuffer.getControlBanks());
            commonEqpBrInfo.setSpecialControl(eqpBrInfoForInternalBuffer.getSpecialControl());
            commonEqpBrInfo.setMultiRecipeCapability(eqpBrInfoForInternalBuffer.getMultiRecipeCapability());
            commonEqpBrInfo.setMaxBatchSize(eqpBrInfoForInternalBuffer.getMaxBatchSize());
            commonEqpBrInfo.setMinBatchSize(eqpBrInfoForInternalBuffer.getMinBatchSize());
            commonEqpBrInfo.setMinWaferCount(eqpBrInfoForInternalBuffer.getMinWaferCount());
            commonEqpBrInfo.setProcessJobLevelCtrl(eqpBrInfoForInternalBuffer.isProcessJobLevelCtrl());

            generalEqpInfoInqResult.setEquipmentBRInfo(commonEqpBrInfo);
            generalEqpInfoInqResult.setEquipmentStatusInfo(eqpInfoForIBInqResult.getEquipmentStatusInfo());
            generalEqpInfoInqResult.setEquipmentPMInfo(eqpInfoForIBInqResult.getEquipmentPMInfo());
            generalEqpInfoInqResult.setEquipmentPortInfo(eqpInfoForIBInqResult.getEquipmentPortInfo());
            generalEqpInfoInqResult.setEquipmentChamberInfo(eqpInfoForIBInqResult.getEquipmentChamberInfo());
            generalEqpInfoInqResult.setEquipmentInternalBufferInfo(eqpInfoForIBInqResult.getEqpInternalBufferInfos());
            generalEqpInfoInqResult.setEquipmentInprocessingControlJobList(eqpInfoForIBInqResult.getEquipmentInprocessingControlJobList());
            generalEqpInfoInqResult.setEquipmentReservedControlJobInfo(eqpInfoForIBInqResult.getEquipmentReservedControlJobInfo());
            generalEqpInfoInqResult.setEquipmentStockerInfo(eqpInfoForIBInqResult.getEquipmentStockerInfo());
            generalEqpInfoInqResult.setEquipmentUTSInfo(eqpInfoForIBInqResult.getEquipmentOHBInfo());
            generalEqpInfoInqResult.setEntityInhibitionList(eqpInfoForIBInqResult.getConstraintList());
            generalEqpInfoInqResult.setEquipmentAdditionalReticleAttribute(eqpInfoForIBInqResult.getEquipmentAdditionalReticleAttribute());

        } else {
            //Step3 - txEqpInfoInq__160

            Params.EqpInfoInqParams eqpInfoInqParams = new Params.EqpInfoInqParams();
            eqpInfoInqParams.setUser(params.getUser());
            eqpInfoInqParams.setEquipmentID(equipmentID);
            eqpInfoInqParams.setRequestFlagForBasicInfo(requestFlagForBRInfo);
            eqpInfoInqParams.setRequestFlagForStatusInfo(requestFlagForStatusInfo);
            eqpInfoInqParams.setRequestFlagForPMInfo(requestFlagForPMInfo);
            eqpInfoInqParams.setRequestFlagForChamberInfo(requestFlagForChamberInfo);
            eqpInfoInqParams.setRequestFlagForStockerInfo(requestFlagForStockerInfo);
            eqpInfoInqParams.setRequestFlagForInprocessingLotInfo(requestFlagForInprocessingLotInfo);
            eqpInfoInqParams.setRequestFlagForReservedControlJobInfo(requestFlagForReservedControlJobInfo);
            eqpInfoInqParams.setRequestFlagForRSPPortInfo(requestFlagForRSPPortInfo);
            eqpInfoInqParams.setRequestFlagForEqpContainerInfo(requestFlagForEqpContainerInfo);

            eqpInfoInqParams.setRequestFlagForPortInfo(requestFlagForPortInfo);
            Results.EqpInfoInqResult eqpInfoInqResult = sxEqpInfoInq(objCommon, eqpInfoInqParams);

            generalEqpInfoInqResult.setEquipmentID(equipmentID);

            Infos.CommonEqpBrInfo commonEqpBrInfo = new Infos.CommonEqpBrInfo();
            Infos.EqpBrInfo eqpBrInfo = eqpInfoInqResult.getEquipmentBasicInfo();
            commonEqpBrInfo.setEquipmentName(eqpBrInfo.getEquipmentName());
            commonEqpBrInfo.setWorkArea(eqpBrInfo.getWorkBay());
            commonEqpBrInfo.setEquipmentOwner(eqpBrInfo.getEquipmentOwner());
            commonEqpBrInfo.setTcsResourceName(eqpBrInfo.getEapResourceName());
            commonEqpBrInfo.setEquipmentCategory(eqpBrInfo.getEquipmentCategory());
            commonEqpBrInfo.setReticleUseFlag(eqpBrInfo.isReticleUseFlag());
            commonEqpBrInfo.setFixtureUseFlag(eqpBrInfo.isFixtureUseFlag());
            commonEqpBrInfo.setCassetteChangeFlag(eqpBrInfo.isCassetteChangeFlag());
            commonEqpBrInfo.setStartLotsNotifyRequiredFlag(eqpBrInfo.isStartLotsNotifyRequiredFlag());
            commonEqpBrInfo.setMonitorCreationFlag(eqpBrInfo.isMonitorCreationFlag());
            commonEqpBrInfo.setEqpToEqpTransferFlag(eqpBrInfo.isEqpToEqpTransferFlag());
            commonEqpBrInfo.setTakeInOutTransferFlag(eqpBrInfo.isTakeInOutTransferFlag());
            commonEqpBrInfo.setEmptyCassetteRequireFlag(eqpBrInfo.isEmptyCassetteRequireFlag());
            commonEqpBrInfo.setSlmSwitch(eqpBrInfo.getFmcSwitch());
            commonEqpBrInfo.setSlmCapabilityFlag(eqpBrInfo.isFmcCapabilityFlag());
            commonEqpBrInfo.setMonitorBank(eqpBrInfo.getMonitorBank());
            commonEqpBrInfo.setDummyBank(eqpBrInfo.getDummyBank());
            commonEqpBrInfo.setSpecialControl(eqpBrInfo.getSpecialControl());
            commonEqpBrInfo.setMultiRecipeCapability(eqpBrInfo.getMultiRecipeCapability());
            commonEqpBrInfo.setMaxBatchSize(eqpBrInfo.getMaxBatchSize());
            commonEqpBrInfo.setMinBatchSize(eqpBrInfo.getMinBatchSize());
            commonEqpBrInfo.setMinWaferCount(eqpBrInfo.getMinWaferCount());
            commonEqpBrInfo.setProcessJobLevelCtrl(eqpBrInfo.isProcessJobLevelCtrl());

            generalEqpInfoInqResult.setEquipmentBRInfo(commonEqpBrInfo);
            generalEqpInfoInqResult.setEquipmentStatusInfo(eqpInfoInqResult.getEquipmentStatusInfo());
            generalEqpInfoInqResult.setEquipmentPMInfo(eqpInfoInqResult.getEquipmentPMInfo());
            generalEqpInfoInqResult.setEquipmentChamberInfo(eqpInfoInqResult.getEquipmentChamberInfo());
            generalEqpInfoInqResult.setEquipmentPortInfo(eqpInfoInqResult.getEquipmentPortInfo());
            generalEqpInfoInqResult.setEquipmentInprocessingControlJobList(eqpInfoInqResult.getEquipmentInprocessingControlJobList());
            generalEqpInfoInqResult.setEquipmentReservedControlJobInfo(eqpInfoInqResult.getEquipmentReservedControlJobInfo());
            generalEqpInfoInqResult.setEquipmentInprocessingDurableControlJobs(eqpInfoInqResult.getEquipmentInprocessingDurableControlJobs());
            generalEqpInfoInqResult.setEquipmentReservedDurableControlJobs(eqpInfoInqResult.getEquipmentInprocessingDurableControlJobs());
            generalEqpInfoInqResult.setEquipmentStockerInfo(eqpInfoInqResult.getEquipmentStockerInfo());
            generalEqpInfoInqResult.setEquipmentUTSInfo(eqpInfoInqResult.getEquipmentOHBInfo());
            generalEqpInfoInqResult.setEquipmentSLMUTSInfo(eqpInfoInqResult.getEquipmentFMCOHBInfo());
            generalEqpInfoInqResult.setEntityInhibitionList(eqpInfoInqResult.getConstraintList());
            generalEqpInfoInqResult.setEquipmentAdditionalReticleAttribute(eqpInfoInqResult.getEquipmentAdditionalReticleAttribute());
            generalEqpInfoInqResult.setEqpContainerList(eqpInfoInqResult.getEqpContainerList());
        }

        return generalEqpInfoInqResult;
    }

    @Override
    public IBFurnaceEQPBatchInfo sxIBFurnaceEQPBatchInfoInq(Infos.ObjCommon objCommon, IBFurnaceEQPBatchInfoInqParams params) {
        ObjectIdentifier eqpID = params.getEqpID();
        log.debug("step1 - check if this is IB and furance eqp and if has lot processing in");
        equipmentMethod.ibFurnaceEQPBatchinqCheck(objCommon,eqpID);

        log.debug("step2 - find last processed cj info");
        IBFurnaceEQPBatchInfo ibFurnaceEQPBatchInfo = equipmentMethod.ibFurnaceEQPBatchInfoGet(objCommon,eqpID);

        return ibFurnaceEQPBatchInfo;
    }

    @Override
    public List<LayoutRecipeResults.EquipmentFurnaceResult> sxEquipmentFurnaceSearchInq(Infos.ObjCommon objCommon, LayoutRecipeParams.EquipmentFurnaceSearchParams equipmentFurnaceSearchParams) {
        //step1 call equipment method ... equipment furnace search
        log.info("call equipment method ... equipment furnace search");
        return equipmentMethod.equipmentFurnaceSearch(objCommon, equipmentFurnaceSearchParams);
    }
}
