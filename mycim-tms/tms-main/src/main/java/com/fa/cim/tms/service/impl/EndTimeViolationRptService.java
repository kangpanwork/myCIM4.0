package com.fa.cim.tms.service.impl;

import com.fa.cim.tms.common.constant.Constant;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.tms.config.MsgRetCodeConfig;
import com.fa.cim.tms.dto.Params;
import com.fa.cim.tms.dto.Results;
import com.fa.cim.tms.manager.IOMSManager;
import com.fa.cim.tms.method.IEquipmentMethod;
import com.fa.cim.tms.pojo.Infos;
import com.fa.cim.tms.pojo.ObjectIdentifier;
import com.fa.cim.tms.service.IEndTimeViolationRptService;
import com.fa.cim.tms.utils.ArrayUtils;
import com.fa.cim.tms.utils.BooleanUtils;
import com.fa.cim.tms.utils.DateUtils;
import com.fa.cim.tms.utils.Validations;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * description:
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/10/20                               Neyo                create file
 *
 * @author: Neyo
 * @date: 2020/10/20 13:39
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@Service
public class EndTimeViolationRptService implements IEndTimeViolationRptService {
    @Autowired
    private IOMSManager omsManager;
    @Autowired
    private IEquipmentMethod equipmentMethod;
    @Autowired
    private MsgRetCodeConfig msgRetCodeConfig;

    public Results.EndTimeViolationReportResult sxEndTimeViolationReport(Infos.ObjCommon objCommon, Params.EndTimeViolationReportParam endTimeViolationReportParam) {
        Results.EndTimeViolationReportResult result = new Results.EndTimeViolationReportResult();
        List<Results.EndTimeViolationResult> endTimeViolationResultDatas = new ArrayList<>();
        Params.EndTimeViolationReportParam tempEndTimeViolationReport = new Params.EndTimeViolationReportParam();
        tempEndTimeViolationReport.setJobID(endTimeViolationReportParam.getJobID());
        List<Params.EndTimeViolation> tempEndTimeViolationResultDatas = new ArrayList<>();
        tempEndTimeViolationReport.setEndTimeViolationReport(tempEndTimeViolationResultDatas);
        if (ArrayUtils.isNotEmpty(endTimeViolationReportParam.getEndTimeViolationReport())) {
            for (Params.EndTimeViolation endTimeViolation : endTimeViolationReportParam.getEndTimeViolationReport()) {
                Params.EndTimeViolation tempData = new Params.EndTimeViolation();
                BeanUtils.copyProperties(endTimeViolation, tempData);
                tempEndTimeViolationResultDatas.add(tempData);
            }
        }

        /*--------------------------------------------*/
        /* If jobRemoveFlag== TRUE,                   */
        /* then delete all record from OTXFERREQ       */
        /*--------------------------------------------*/
        if (ArrayUtils.isNotEmpty(tempEndTimeViolationReport.getEndTimeViolationReport())) {
            for (Params.EndTimeViolation endTimeViolation : tempEndTimeViolationReport.getEndTimeViolationReport()) {
                Results.EndTimeViolationResult seqEndTimeViolationResult = new Results.EndTimeViolationResult();
                seqEndTimeViolationResult.setCarrierJobID(endTimeViolation.getCarrierJobID());
                seqEndTimeViolationResult.setCarrierID(endTimeViolation.getCarrierID());
                endTimeViolationResultDatas.add(seqEndTimeViolationResult);
                log.info("【step1】 - equipmentMethod.checkEqpTransfer");
                Boolean eqpFlag = true;
                try {
                    equipmentMethod.checkEqpTransfer(objCommon, endTimeViolation.getToMachineID(),true);
                } catch (ServiceException e) {
                    if (!Validations.isEquals(msgRetCodeConfig.getMsgRecordNotFound(), e.getCode())) {
                        throw e;
                    }
                    eqpFlag = false;
                }
                if (BooleanUtils.isTrue(eqpFlag)) {
                    ObjectIdentifier cassettID = endTimeViolation.getCarrierID();
                    log.info("【step2】 - omsManager.sendCassetteStatusInq");
                    Results.CarrierDetailInfoInqResult cassetteStatusInqResult = omsManager.sendCassetteStatusInq(objCommon, cassettID);
                    if (null != cassetteStatusInqResult && null != cassetteStatusInqResult.getCassetteStatusInfo() && ArrayUtils.isNotEmpty(cassetteStatusInqResult.getCassetteStatusInfo().getStrContainedLotInfo())) {
                        log.info("【step3】 - omsManager.sendSystemMsgRpt");
                        /*------------------------------------------------------*/
                        /*  Send sendSystemMsgRpt to OMS                        */
                        /*      Set Data as following :                         */
                        /*        in.requestUserID  -->  requestUserID;         */
                        /*        in.requestUserID  -->  subSystemID;           */
                        /*        in.alarmCode      -->  systemMessageCode;     */
                        /*        in.alarmText      -->  systemMessageText;     */
                        /*        TRUE              -->  notifyFlag;            */
                        /*        in.toMachineID    -->  equipmentID;           */
                        /*        Blank             -->  equipmentStatus;       */
                        /*        Blank             -->  stockerID;             */
                        /*        Blank             -->  stockerStatus;         */
                        /*        Blank             -->  AGVID;                 */
                        /*        Blank             -->  AGVStatus;             */
                        /*        out.lotID         -->  lotID;                 */
                        /*        Blank             -->  lotStatus;             */
                        /*        Blank             -->  routeID;               */
                        /*        Blank             -->  routeIDVersion;        */
                        /*        Blank             -->  operationID;           */
                        /*        Blank             -->  operationIDVersion;    */
                        /*        Blank             -->  operationNumber;       */
                        /*        System Time       -->  systemMessageTimeStamp;*/
                        /*        Blank             -->  claimMemo;             */
                        /*------------------------------------------------------*/
                        Results.AlertMessageRptResult alertMessageRptResult = omsManager.sendSystemMsgRpt(objCommon,
                                ObjectIdentifier.fetchValue(objCommon.getUser().getUserID()),
                                Constant.TM_ALARM_CODE_XM01,
                                Constant.TM_ALARM_TEXT_XM01,
                                true,
                                endTimeViolation.getToMachineID(),
                                "",
                                null,
                                "",
                                null,
                                "",
                                cassetteStatusInqResult.getCassetteStatusInfo().getStrContainedLotInfo().get(0).getLotID(),
                                "",
                                null,
                                null,
                                "",
                                DateUtils.getCurrentTimeStamp().toString(),
                                "");
                    }
                }
            }
        }
        result.setJobID(endTimeViolationReportParam.getJobID());
        result.setEndTimeViolationResultData(endTimeViolationResultDatas);
        return result;
    }

    @Override
    public Results.EndTimeViolationReportResult sxRtmsEndTimeViolationReport(Infos.ObjCommon objCommon, Params.EndTimeViolationReportParam endTimeViolationReportParam) {
        Results.EndTimeViolationReportResult result = new Results.EndTimeViolationReportResult();
        List<Results.EndTimeViolationResult> endTimeViolationResultDatas = new ArrayList<>();
        /*--------------------------------------------*/
        /* If jobRemoveFlag== TRUE,                   */
        /* then delete all record from OTXFERREQ       */
        /*--------------------------------------------*/
        if (ArrayUtils.isNotEmpty(endTimeViolationReportParam.getEndTimeViolationReport())) {
            for (Params.EndTimeViolation endTimeViolation : endTimeViolationReportParam.getEndTimeViolationReport()) {
                Results.EndTimeViolationResult seqEndTimeViolationResult = new Results.EndTimeViolationResult();
                seqEndTimeViolationResult.setCarrierJobID(endTimeViolation.getCarrierJobID());
                seqEndTimeViolationResult.setCarrierID(endTimeViolation.getCarrierID());
                endTimeViolationResultDatas.add(seqEndTimeViolationResult);
                log.info("【step1】 - equipmentMethod.checkEqpTransfer");
                Boolean eqpFlag = true;
                try {
                    equipmentMethod.checkEqpTransfer(objCommon, endTimeViolation.getToMachineID(),false);
                } catch (ServiceException e) {
                    if (!Validations.isEquals(msgRetCodeConfig.getMsgRecordNotFound(), e.getCode())) {
                        throw e;
                    }
                    eqpFlag = false;
                }
                if (BooleanUtils.isTrue(eqpFlag)) {
                    ObjectIdentifier cassettID = endTimeViolation.getCarrierID();
                    log.info("【step2】 - omsManager.sendCassetteStatusInq");
                    Results.ReticlePodStatusInqResult reticlePodStatusInqResult = omsManager.sendReticlePodStatusInq(objCommon, cassettID);
                    if (null != reticlePodStatusInqResult && null != reticlePodStatusInqResult.getReticlePodStatusInfo() && ArrayUtils.isNotEmpty(reticlePodStatusInqResult.getReticlePodStatusInfo().getStrContainedReticleInfo())) {
                        log.info("【step3】 - omsManager.sendSystemMsgRpt");
                        /*------------------------------------------------------*/
                        /*  Send sendSystemMsgRpt to OMS                        */
                        /*      Set Data as following :                         */
                        /*        in.requestUserID  -->  requestUserID;         */
                        /*        in.requestUserID  -->  subSystemID;           */
                        /*        in.alarmCode      -->  systemMessageCode;     */
                        /*        in.alarmText      -->  systemMessageText;     */
                        /*        TRUE              -->  notifyFlag;            */
                        /*        in.toMachineID    -->  equipmentID;           */
                        /*        Blank             -->  equipmentStatus;       */
                        /*        Blank             -->  stockerID;             */
                        /*        Blank             -->  stockerStatus;         */
                        /*        Blank             -->  AGVID;                 */
                        /*        Blank             -->  AGVStatus;             */
                        /*        out.lotID         -->  lotID;                 */
                        /*        Blank             -->  lotStatus;             */
                        /*        Blank             -->  routeID;               */
                        /*        Blank             -->  routeIDVersion;        */
                        /*        Blank             -->  operationID;           */
                        /*        Blank             -->  operationIDVersion;    */
                        /*        Blank             -->  operationNumber;       */
                        /*        System Time       -->  systemMessageTimeStamp;*/
                        /*        Blank             -->  claimMemo;             */
                        /*------------------------------------------------------*/
                        Results.AlertMessageRptResult alertMessageRptResult = omsManager.sendSystemMsgRpt(objCommon,
                                ObjectIdentifier.fetchValue(objCommon.getUser().getUserID()),
                                Constant.TM_ALARM_CODE_RXM01,
                                Constant.TM_ALARM_TEXT_RXM01,
                                true,
                                endTimeViolation.getToMachineID(),
                                "",
                                null,
                                "",
                                null,
                                "",
                                reticlePodStatusInqResult.getReticlePodStatusInfo().getStrContainedReticleInfo().get(0).getReticleID(),
                                "",
                                null,
                                null,
                                "",
                                DateUtils.getCurrentTimeStamp().toString(),
                                "");
                    }
                }
            }
        }
        result.setJobID(endTimeViolationReportParam.getJobID());
        result.setEndTimeViolationResultData(endTimeViolationResultDatas);
        return result;
    }
}
