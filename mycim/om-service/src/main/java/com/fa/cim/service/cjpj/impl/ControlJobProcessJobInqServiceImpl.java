package com.fa.cim.service.cjpj.impl;

import com.alibaba.fastjson.JSON;
import com.fa.cim.annotaion.OmService;
import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.constant.TCSReqEnum;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.OmCode;
import com.fa.cim.common.utils.CimArrayUtils;
import com.fa.cim.common.utils.CimStringUtils;
import com.fa.cim.common.utils.Validations;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.dto.*;
import com.fa.cim.method.*;
import com.fa.cim.newcore.impl.bo.env.StandardProperties;
import com.fa.cim.remote.IEAPRemoteManager;
import com.fa.cim.service.cjpj.IControlJobProcessJobInqService;
import com.fa.cim.service.lot.ILotInqService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/9/8        ********             Bear               create file
 *
 * @author: LiaoYunChuan
 * @date: 2020/9/8 17:38
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@OmService
public class ControlJobProcessJobInqServiceImpl implements IControlJobProcessJobInqService {

    @Autowired
    private IRecipeMethod recipeMethod;

    @Autowired
    private ITCSMethod tcsMethod;

    @Autowired
    private RetCodeConfig retCodeConfig;

    @Autowired
    private IEquipmentMethod equipmentMethod;

    @Autowired
    private IControlJobMethod controlJobMethod;

    @Autowired
    private IProcessMethod processMethod;

    @Autowired
    private ILotMethod lotMethod;

    @Autowired
    private ILotInqService lotInqService;

    @Autowired
    private IEAPMethod eapMethod;

    public Results.CJPJProgressInfoInqResult sxCJPJProgressInfoInq(Infos.ObjCommon objCommon, Params.CJPJProgressInfoInqParams params) {

        Results.CJPJProgressInfoInqResult cjpjProgressInfoInqResult = new Results.CJPJProgressInfoInqResult();
        /*-----------------------------------------*/
        /*   Check Condition for CJPJProgressInfoInq   */
        /*-----------------------------------------*/
        log.info("Check Condition for CJPJProgressInfoInq");
        recipeMethod.recipeParameterCJPjConditionCheck(objCommon, params.getEquipmentID(), params.getControlJobID());
        /*-------------------------*/
        /*   Send Request to EAP   */
        /*-------------------------*/
        Inputs.SendCJPJProgressIn sendCJPJProgressIn = new Inputs.SendCJPJProgressIn();
        sendCJPJProgressIn.setObjCommonIn(objCommon);
        sendCJPJProgressIn.setEquipmentID(params.getEquipmentID());
        sendCJPJProgressIn.setControlJobID(params.getControlJobID());
        Outputs.SendCJPJProgressOut sendCJPJProgressInfoInqOut = (Outputs.SendCJPJProgressOut) tcsMethod.sendTCSReq(TCSReqEnum.sendCJPJProgressInfoInq,sendCJPJProgressIn);

        /*-----------------------------------------------*/
        /*   The result of EAP is set to StartCassette   */
        /*-----------------------------------------------*/
        List<Infos.StartCassette> startCassettes = recipeMethod.recipeParameterFillInTxTRC041(objCommon, params.getEquipmentID(), params.getControlJobID(), sendCJPJProgressInfoInqOut.getCjpjProgressInqResult().getStrStartCassette());
        cjpjProgressInfoInqResult.setStrStartCassette(startCassettes);
        return cjpjProgressInfoInqResult;
    }

    public Results.CJPJOnlineInfoInqResult sxCJPJOnlineInfoInq(Infos.ObjCommon objCommon,
                                                               Params.CJPJOnlineInfoInqInParams params) {
        // Initialize
        Results.CJPJOnlineInfoInqResult cjpjOnlineInfoInqResult = new Results.CJPJOnlineInfoInqResult();
        List<Infos.ProcessJobInfo> strProcessJobInfoSeq = new ArrayList<>();
        //-------------------------------
        // Check Function Availability
        //-------------------------------
        if (log.isDebugEnabled()){
            log.debug("check Function Availability");
        }
        String PJCtrlFunc = StandardProperties.OM_PJ_CONTROL_ENABLE_FLAG.getValue();
        Validations.check(!CimStringUtils.equals(PJCtrlFunc, BizConstant.SP_FUNCTION_AVAILABLE_TRUE),
                new OmCode(retCodeConfig.getFunctionNotAvailable(), "Process Job Level Control"));
        //------------------------------------------------------
        // Check Equipment
        //------------------------------------------------------
        if (log.isDebugEnabled()){
            log.debug("check Equipment");
        }
        equipmentMethod.equipmentProcessJobLevelControlCheck(objCommon,
                params.getEquipmentID(),
                true,
                true,
                false,
                false);
        //--------------------------------------------------------------------------
        // Get Equipment on-line mode
        //--------------------------------------------------------------------------
        if (log.isDebugEnabled()){
            log.debug("Get Equipment on-line mode");
        }
        String equipmentOnlineMode = equipmentMethod.equipmentOnlineModeGet(objCommon, params.getEquipmentID());
        //------------------------------------------------------
        // Check Control Job
        //------------------------------------------------------
        if (log.isDebugEnabled()){
            log.debug("check Control Job");
        }
        Outputs.ObjControlJobStatusGetOut objControlJobStatusGet = null;
        try{
            objControlJobStatusGet = controlJobMethod.controlJobStatusGet(objCommon, params.getControlJobID());
        }catch (ServiceException e) {
            if (Validations.isEquals(retCodeConfig.getNotFoundControlJob(), e.getCode())) {
                if (log.isDebugEnabled()){
                    log.debug("getNotFoundControlJob");
                }
                return cjpjOnlineInfoInqResult;
            } else {
                throw e;
            }
        }
        //------------------------------------------------------
        // Call EAP to get detailed informamation
        //------------------------------------------------------
        if (log.isDebugEnabled()){
            log.debug("Call EAP to get detailed information");
        }
        if (!CimStringUtils.equals(BizConstant.SP_EQP_ONLINEMODE_OFFLINE,  equipmentOnlineMode)) {
            long sleepTimeValue = StandardProperties.OM_EAP_CONNECT_SLEEP_TIME.getLongValue() == 0L ?
                    BizConstant.SP_DEFAULT_SLEEP_TIME_TCS : StandardProperties.OM_EAP_CONNECT_SLEEP_TIME.getLongValue();

            long retryCountValue = StandardProperties.OM_EAP_CONNECT_RETRY_COUNT.getLongValue() == 0L ?
                    BizConstant.SP_DEFAULT_RETRY_COUNT_TCS : StandardProperties.OM_EAP_CONNECT_SLEEP_TIME.getLongValue();
            for (int retryNum = 0; retryNum < (retryCountValue + 1); retryNum++) {
                if (log.isDebugEnabled()){
                    log.debug("{} loop to retryCountValue + 1", retryNum);
                }
                /*--------------------------*/
                /*    Send Request to EAP   */
                /*--------------------------*/
                IEAPRemoteManager eapRemoteManager = eapMethod.eapRemoteManager(objCommon,params.getUser(),
                        params.getEquipmentID(),null,false);
                if (null == eapRemoteManager) {
                    if (log.isDebugEnabled()){
                        log.debug("MES not configure EAP host");
                    }
                    break;
                }
                Results.CJPJOnlineInfoInqResult sendCJPJOnlineInfoInqResult = null;
                Object eapOut = null;
                try {
                    eapOut = eapRemoteManager.sendCJPJOnlineInfoInq(params);
                    sendCJPJOnlineInfoInqResult = JSON.parseObject(eapOut.toString(),Results.CJPJOnlineInfoInqResult.class);
                    log.debug("Now EAP subSystem is alive!! Go ahead");
                    if (sendCJPJOnlineInfoInqResult != null
                            && sendCJPJOnlineInfoInqResult.getProcessJobInfoList() != null) {
                        strProcessJobInfoSeq = sendCJPJOnlineInfoInqResult.getProcessJobInfoList();
                        cjpjOnlineInfoInqResult.setProcessJobInfoList(strProcessJobInfoSeq);
                        cjpjOnlineInfoInqResult.setControlJobID(params.getControlJobID());
                        cjpjOnlineInfoInqResult.setEquipmentID(params.getEquipmentID());
                    }
                    break;
                } catch (ServiceException ex) {
                    if (Validations.isEquals(ex.getCode(), retCodeConfig.getTcsNoResponse())) {
                        if (log.isDebugEnabled()){
                            log.debug("EAP subsystem has return NO_RESPONSE!just retry now!! now count...{}", retryNum);
                            log.debug("now sleeping... {}", sleepTimeValue);
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
                        Validations.check(true,new OmCode(ex.getCode(),ex.getMessage()));
                    }
                }
            }
        }
        int lenPJInfo = CimArrayUtils.getSize(strProcessJobInfoSeq);
        if (CimStringUtils.isEmpty(params.getProcessJobID())) {
            //------------------------------------------------------
            // Set non-wafer sampling wafers
            //------------------------------------------------------
            if (log.isDebugEnabled()){
                log.debug("Set non-wafer sampling wafers");
                log.debug("Get process wafers");
            }
            List<Infos.ProcessJob> processWafersRetCode =
                    processMethod.processOperationProcessWafersGet(objCommon, params.getControlJobID());
            String listLotIDKeyName;
            String listWaferIDKeyName;
            List<String> aSamplingLotIDList = new ArrayList<>();
            List<String> aSmplWaferIDList = new ArrayList<>();
            Optional.ofNullable(processWafersRetCode).ifPresent(list -> list.forEach(processJob -> {
                Optional.ofNullable(processJob.getProcessWaferList()).ifPresent(waferList -> waferList.forEach(
                        processWafer -> {
                            boolean samplingFlag = processWafer.getSamplingWaferFlag();
                            if (samplingFlag) {
                                if (!aSamplingLotIDList.contains(ObjectIdentifier.fetchValue(processWafer.getLotID()))){
                                    aSamplingLotIDList.add(ObjectIdentifier.fetchValue(processWafer.getLotID()));
                                }
                                if (!aSmplWaferIDList.contains(ObjectIdentifier.fetchValue(processWafer.getWaferID()))){
                                    aSmplWaferIDList.add(ObjectIdentifier.fetchValue(processWafer.getWaferID()));
                                }
                            }
                        }
                ));
            }));

            // Wafer Sampling or Offline mode
            if (log.isDebugEnabled()){
                log.debug("Wafer Sampling or Offline mode");
            }
            if (CimArrayUtils.isNotEmpty(aSamplingLotIDList) || 0 == lenPJInfo) {
                if (log.isDebugEnabled()){
                    log.debug("0 < aSamplingLotIDList.GetCount() or lenPJInfo = 0");
                    log.debug("create non-sampling wafer process job");
                }
                // create non-sampling wafer process job
                Infos.ProcessJobInfo strProcessJobInfo = new Infos.ProcessJobInfo();
                strProcessJobInfo.setProcessStartFlag(false);
                strProcessJobInfo.setProcessJobState(BizConstant.SP_PROCESSJOBSTATUS_NONPROCESS);
                strProcessJobInfo.setActionRequestTime(BizConstant.SP_TIMESTAMP_NIL_OBJECT_STRING);
                int waferSeqLen = 0;
                List<Infos.ProcessWafer> strProcessWaferSeq = new ArrayList<>();
                strProcessJobInfo.setProcessWaferList(strProcessWaferSeq);
                if (log.isDebugEnabled()){
                    log.debug("create unknown process job");
                }
                // create unknown process job
                Infos.ProcessJobInfo strUnknownProcessJobInfo = new Infos.ProcessJobInfo();
                BeanUtils.copyProperties(strProcessJobInfo, strUnknownProcessJobInfo);
                strUnknownProcessJobInfo.setProcessJobState(BizConstant.SP_PROCESSJOBSTATUS_UNKNOWN);
                List<Infos.ProcessWafer> strUnknownProcessWaferSeq = new ArrayList<>();
                strUnknownProcessJobInfo.setProcessWaferList(strUnknownProcessWaferSeq);
                int unknownWaferSeqlen = 0;
                if (log.isErrorEnabled()){
                    log.debug("Get wafer list from lotID");
                }
                //Get wafer list from lotID
                List<Infos.ControlJobCassette> strControlJobContainedLotGetOut =
                        controlJobMethod.controlJobContainedLotGet(objCommon, params.getControlJobID());
                int lenCtrlJobCassette = CimArrayUtils.getSize(strControlJobContainedLotGetOut);
                if (log.isDebugEnabled()){
                    log.debug("strControlJobContainedLotGetOut.strControlJobCassette[i].strControlJobLot.size() {}",
                            lenCtrlJobCassette);
                }
                for (int i = 0; i < lenCtrlJobCassette; i++) {
                    Infos.ControlJobCassette controlJobCassette = strControlJobContainedLotGetOut.get(i);
                    int lenCtrlJobLot = CimArrayUtils.getSize(controlJobCassette.getControlJobLotList());
                    if (0 == lenCtrlJobLot) {
                        continue;
                    }
                    for (int j = 0; j < lenCtrlJobLot; j++) {
                        Infos.ControlJobLot controlJobLot = controlJobCassette.getControlJobLotList().get(j);
                        if (!controlJobLot.getOperationStartFlag()) {
                            continue;
                        }
                        Inputs.ObjLotWaferIDListGetDRIn input = new Inputs.ObjLotWaferIDListGetDRIn();
                        input.setLotID(controlJobLot.getLotID());
                        input.setScrapCheckFlag(true);
                        List<ObjectIdentifier> waferIDs = lotMethod.lotWaferIDListGetDR(objCommon, input);

                        int lenWafer = CimArrayUtils.getSize(waferIDs);
                        for (int k = 0; k < lenWafer; k++) {
                            boolean foundSFFlag = false;
                            for (int m = 0; m < lenPJInfo; m++) {
                                int lenProcessWaferSeq = CimArrayUtils.getSize(
                                        cjpjOnlineInfoInqResult.getProcessJobInfoList().get(m).getProcessWaferList());
                                for (int n = 0; n < lenProcessWaferSeq; n++) {
                                    if (ObjectIdentifier.equalsWithValue(
                                            cjpjOnlineInfoInqResult.getProcessJobInfoList()
                                                    .get(m).getProcessWaferList().get(n).getWaferID(),
                                            waferIDs.get(k))) {
                                        foundSFFlag = true;
                                        break;
                                    }
                                }
                            }
                            if (!foundSFFlag) {
                                listLotIDKeyName = ObjectIdentifier.fetchValue(controlJobLot.getLotID());
                                listWaferIDKeyName = ObjectIdentifier.fetchValue(waferIDs.get(k));
                                // non-sampled wafer
                                if (aSamplingLotIDList.contains(listLotIDKeyName)
                                        && !aSmplWaferIDList.contains(listWaferIDKeyName)) {
                                    Infos.ProcessWafer processWafer = new Infos.ProcessWafer();
                                    processWafer.setWaferID(waferIDs.get(k));
                                    processWafer.setLotID(controlJobLot.getLotID());
                                    strProcessWaferSeq.add(processWafer);
                                    waferSeqLen++;
                                } else {
                                    Infos.ProcessWafer processWafer = new Infos.ProcessWafer();
                                    processWafer.setWaferID(waferIDs.get(k));
                                    processWafer.setLotID(controlJobLot.getLotID());
                                    strUnknownProcessWaferSeq.add(processWafer);
                                    unknownWaferSeqlen++;
                                }
                            }
                        }
                    }
                }
                if (unknownWaferSeqlen > 0) {
                    //add strUnknownProcessJobInfo to strCJPJOnlineInfoInqResult.strProcessJobInfoSeq
                    strProcessJobInfoSeq.add(strUnknownProcessJobInfo);
                }
                if (waferSeqLen > 0) {
                    //add pptProcessJobInfo to strCJPJOnlineInfoInqResult.strProcessJobInfoSeq
                    strProcessJobInfoSeq.add(strProcessJobInfo);
                }
                cjpjOnlineInfoInqResult.setProcessJobInfoList(strProcessJobInfoSeq);
            }
        }
        //------------------------------------------------------
        // Set aliasWaferName  (call sxWaferAliasInfoInq)
        //------------------------------------------------------
        if (log.isDebugEnabled()){
            log.debug("Set aliasWaferName  (call sxWaferAliasInfoInq)");
        }
        lenPJInfo = CimArrayUtils.getSize(cjpjOnlineInfoInqResult.getProcessJobInfoList());
        List<ObjectIdentifier> waferIDs = new ArrayList<>();
        for (int i = 0; i < lenPJInfo; i++) {
            Infos.ProcessJobInfo processJobInfo = cjpjOnlineInfoInqResult.getProcessJobInfoList().get(i);
            int lenProcessWaferSeq = CimArrayUtils.getSize(processJobInfo.getProcessWaferList());
            for (int j = 0; j < lenProcessWaferSeq; j++) {
                Infos.ProcessWafer processWafer = processJobInfo.getProcessWaferList().get(j);
                waferIDs.add(processWafer.getWaferID());
            }
        }

        if (log.isDebugEnabled()){
            log.debug("Call txWaferAliasInfoInq");
        }
        Params.WaferAliasInfoInqParams waferAliasInfoInqParams = new Params.WaferAliasInfoInqParams();
        waferAliasInfoInqParams.setUser(objCommon.getUser());
        waferAliasInfoInqParams.setWaferIDSeq(waferIDs);
        List<Infos.AliasWaferName> waferAliasInfoInqResult =
                lotInqService.sxWaferAliasInfoInq(objCommon, waferAliasInfoInqParams);

        //-----------------------
        // Set output result
        //-----------------------
        if (log.isDebugEnabled()){
            log.debug("lenPJInfo : {}", lenPJInfo);
        }
        for (int i = 0; i < lenPJInfo; i++) {
            Infos.ProcessJobInfo processJobInfo = cjpjOnlineInfoInqResult.getProcessJobInfoList().get(i);
            int lenProcessWaferSeq = CimArrayUtils.getSize(processJobInfo.getProcessWaferList());
            if (log.isDebugEnabled()){
                log.debug("procWaferLen {}", lenProcessWaferSeq);
            }
            for (int j = 0; j < lenProcessWaferSeq; j++) {
                Infos.ProcessWafer processWafer = processJobInfo.getProcessWaferList().get(j);
                int aliasWaferLen = CimArrayUtils.getSize(waferAliasInfoInqResult);
                if (log.isDebugEnabled()){
                    log.debug("aliasWaferLen {}", aliasWaferLen);
                }
                for (int k = 0; k < aliasWaferLen; k++) {
                    Infos.AliasWaferName aliasWaferName = waferAliasInfoInqResult.get(k);
                    if (ObjectIdentifier.equalsWithValue(processWafer.getWaferID(), aliasWaferName.getWaferID())) {
                        if (log.isDebugEnabled()){
                            log.debug("set aliasWafer name");
                        }
                        processWafer.setAliasWaferName(aliasWaferName.getAliasWaferName());
                        break;
                    }
                }
            }
        }
        return cjpjOnlineInfoInqResult;
    }
}
