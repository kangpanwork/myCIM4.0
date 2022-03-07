package com.fa.cim.tms.service.impl;

import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.tms.common.constant.Constant;
import com.fa.cim.tms.common.enums.EnvCodeEnum;
import com.fa.cim.tms.config.MsgRetCodeConfig;
import com.fa.cim.tms.dto.Params;
import com.fa.cim.tms.dto.Results;
import com.fa.cim.tms.manager.IOMSManager;
import com.fa.cim.tms.manager.impl.MCSManagerImpl;
import com.fa.cim.tms.method.IEnvMethod;
import com.fa.cim.tms.method.IEquipmentMethod;
import com.fa.cim.tms.method.ITransferJobMethod;
import com.fa.cim.tms.pojo.Infos;
import com.fa.cim.tms.pojo.ObjectIdentifier;
import com.fa.cim.tms.service.ITransportJobCreateReqService;
import com.fa.cim.tms.utils.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * description:
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/10/20                               Neyo                create file
 *
 * @author: Neyo
 * @date: 2020/10/20 17:28
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@Service
public class TransportJobCreateReqService implements ITransportJobCreateReqService {
    @Autowired
    private MCSManagerImpl mcsManager;
    @Autowired
    private IEnvMethod envMethod;
    @Autowired
    private ITransferJobMethod transferJobMethod;
    @Autowired
    private IEquipmentMethod equipmentMethod;
    @Autowired
    private MsgRetCodeConfig msgRetCodeConfig;
    @Autowired
    private IOMSManager omsManager;

    public Results.TransportJobCreateReqResult sxTransportJobCreateReq(Infos.ObjCommon objCommon, Params.TransportJobCreateReqParams transportJobCreateReqParams) {
        Results.TransportJobCreateReqResult result = new Results.TransportJobCreateReqResult();
        String noFromMachineIDForSTKFlag = envMethod.getEnvFromOmsDB(EnvCodeEnum.OM_OM01_NO_FROM_MACHINEID_FOR_STK.getValue());//"OFF"
        log.info("env OM_OM01_NO_FROM_MACHINEID_FOR_STK: {}", noFromMachineIDForSTKFlag);
        /*-----------------------------------------------------------*/
        /*   get Transfer job from Transfer Table by Carrier         */
        /*-----------------------------------------------------------*/
        int nLen = ArrayUtils.getSize(transportJobCreateReqParams.getJobCreateData());
        log.info("param JobCreateData size: {}", nLen);

        Params.TransportJobCreateReqParams tempTranJobCreateReq = new Params.TransportJobCreateReqParams();
        tempTranJobCreateReq.setJobID(transportJobCreateReqParams.getJobID());
        tempTranJobCreateReq.setRerouteFlag(transportJobCreateReqParams.getRerouteFlag());
        tempTranJobCreateReq.setTransportType(transportJobCreateReqParams.getTransportType());

        List<Infos.AmhsJobCreateArray> jobCreateArrayList = new ArrayList<>();
        tempTranJobCreateReq.setJobCreateData(jobCreateArrayList);

        if (ArrayUtils.isNotEmpty(transportJobCreateReqParams.getJobCreateData())) {
            for (Infos.AmhsJobCreateArray data : transportJobCreateReqParams.getJobCreateData()) {
                Infos.AmhsJobCreateArray jobCreateArray = new Infos.AmhsJobCreateArray();
                jobCreateArray.setCarrierJobID(data.getCarrierJobID());
                jobCreateArray.setCarrierID(data.getCarrierID());
                jobCreateArray.setZoneType(data.getZoneType());
                jobCreateArray.setN2PurgeFlag(data.getN2PurgeFlag());
                jobCreateArrayList.add(jobCreateArray);

                if (Constant.TM_ON.equals(noFromMachineIDForSTKFlag)) {
                    log.info("【step1】 - equipmentMethod.checkEqpTransfer");
                    Boolean eqpFlag = true;
                    try {
                        equipmentMethod.checkEqpTransfer(objCommon, data.getFromMachineID(),true);
                    } catch (ServiceException e) {
                        if (Validations.isEquals(msgRetCodeConfig.getMsgRecordNotFound(), e.getCode())) {
                            eqpFlag = false;
                            log.info("fromMachine is a stocker");
                            jobCreateArray.setFromMachineID(ObjectIdentifier.buildWithValue(""));
                            jobCreateArray.setFromPortID(ObjectIdentifier.buildWithValue(""));
                        } else {
                            throw e;
                        }
                    }
                    if (BooleanUtils.isTrue(eqpFlag)) {
                        log.info("fromMachine is equipment");
                        jobCreateArray.setFromMachineID(data.getFromMachineID());
                        jobCreateArray.setFromPortID(data.getFromPortID());
                    }
                } else {
                    jobCreateArray.setFromMachineID(data.getFromMachineID());
                    jobCreateArray.setFromPortID(data.getFromPortID());
                }

                List<Infos.AmhsToDestination> tempToMachine = new ArrayList<>();
                jobCreateArray.setToMachine(tempToMachine);
                if (ArrayUtils.isNotEmpty(data.getToMachine())) {
                    for (Infos.AmhsToDestination toDestination : data.getToMachine()) {
                        Infos.AmhsToDestination tempDestination = new Infos.AmhsToDestination();
                        tempToMachine.add(tempDestination);
                        tempDestination.setToMachineID(toDestination.getToMachineID());
                        /*-----------------------------------------*/
                        /* Check toMachineID is EqpID              */
                        /*-----------------------------------------*/
                        log.info("【step2】 - equipmentMethod.checkEqpTransfer");
                        try {
                            equipmentMethod.checkEqpTransfer(objCommon, tempDestination.getToMachineID(),true);
                        } catch (ServiceException e) {
                            //doNothing
                        }
                        // When toMachine is Equipment.
                        log.info("toMachine is Equipment");
                        tempDestination.setToPortID(toDestination.getToPortID());
                    }
                }
                jobCreateArray.setExpectedStartTime(data.getExpectedStartTime());
                jobCreateArray.setExpectedEndTime(data.getExpectedEndTime());
                jobCreateArray.setMandatoryFlag(data.getMandatoryFlag());

                if (StringUtils.isEmpty(data.getPriority())) {
                    jobCreateArray.setPriority(Constant.TM_TRANSFER_JOB_PRIORITY_FOUR);
                } else {
                    jobCreateArray.setPriority(data.getPriority());
                }
            }
        }
        tempTranJobCreateReq.setRerouteFlag(false);
        /*  Set Inquiry_Type = 'C' for Inquiry                                      */
        /*  if Transfer requested carrier had jobID with Transport Job type='S'     */
        /*  then must be embeded it to Transfer request message by each carrier.    */
        /*  In this case rerouteFlag must be 'TRUE'                                 */
        String inquiryType = Constant.TM_INQUIRY_TYPE_BY_CARRIER;
        List<ObjectIdentifier> carrierIDList = new ArrayList<>();
        if (ArrayUtils.isNotEmpty(transportJobCreateReqParams.getJobCreateData())) {
            carrierIDList = transportJobCreateReqParams.getJobCreateData().stream().map(Infos.AmhsJobCreateArray::getCarrierID).collect(Collectors.toList());
        }
        log.info("【step3】 - transferJobMethod.transferJobGet");
        List<Infos.TransferJobInfo> transferJobInfoList = null;
        try {
            transferJobInfoList = transferJobMethod.transferJobGet(objCommon, inquiryType, carrierIDList, null, null, null);
            log.info("exist records in OTXFERREQ");
            tempTranJobCreateReq.setRerouteFlag(true);
            tempTranJobCreateReq.setJobID(transferJobInfoList.get(0).getJobID());
        } catch (ServiceException e) {
            if (!Validations.isEquals(msgRetCodeConfig.getMsgRecordNotFound(), e.getCode())) {
                throw e;
            }
        }

        if (ArrayUtils.isNotEmpty(transferJobInfoList)) {
            for (Infos.TransferJobInfo transferJobInfo : transferJobInfoList) {
                for (int m = 0; m < nLen; m++) {
                    if (ObjectUtils.equalsWithValue(tempTranJobCreateReq.getJobCreateData().get(m).getCarrierID(), transferJobInfo.getCarrierID())) {
                        log.info("Current Carrier Job: {}", transferJobInfo.getCarrierJobID());
                        tempTranJobCreateReq.getJobCreateData().get(m).setCarrierJobID(transferJobInfo.getCarrierJobID());
                        break;
                    }
                }
            }
        }
        Boolean chkEqpXferFlag = null;
        if (Constant.TM_ON.equals(envMethod.getEnvFromOmsDB(EnvCodeEnum.OM_REROUTE_EQP_XER.getValue()))) {
            //No need to EQP check
            chkEqpXferFlag = false;
            log.info("chkEqpXferFlag = false");
        } else {
            // Need to EQP check
            chkEqpXferFlag = true;
            log.info("chkEqpXferFlag = true");
        }
        if (BooleanUtils.isTrue(chkEqpXferFlag)) {
            /*-----------------------------------------------------------*/
            /*   check TransportType                                     */
            /*-----------------------------------------------------------*/
            Boolean eqpXferFlag = true;
            if (ArrayUtils.isNotEmpty(transferJobInfoList)) {
                for (Infos.TransferJobInfo transferJobInfo : transferJobInfoList) {
                    /*-----------------------------------------*/
                    /* Check toMachineID is EqpID              */
                    /*-----------------------------------------*/
                    eqpXferFlag = true;
                    log.info("【step4】 - equipmentMethod.checkEqpTransfer");
                    try {
                        equipmentMethod.checkEqpTransfer(objCommon, transferJobInfo.getToMachineID(),true);
                    } catch (ServiceException e) {
                        if (!Validations.isEquals(msgRetCodeConfig.getMsgRecordNotFound(), e.getCode())) {
                            throw e;
                        }
                        eqpXferFlag = false;
                    }
                    log.info("Found a EqpXfer Record in OTXFERREQ");
                    if (BooleanUtils.isTrue(eqpXferFlag)) {
                        log.info("toPortID != NULL && toPortID != BLANK");
                        log.info("toPortID:{}", ObjectIdentifier.fetchValue(transferJobInfo.getToPortID()));
                        log.info("【step5】 - omsManager.sendSystemMsgRpt");
                        /*------------------------------------------------------*/
                        /*  Send sendSystemMsgRpt to OMS                           */
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
                        try {
                            Results.AlertMessageRptResult alertMessageRptResult = omsManager.sendSystemMsgRpt(objCommon,
                                    "",
                                    Constant.TM_DISPATCH_ERROR,
                                    "",
                                    true,
                                    null,
                                    "",
                                    null,
                                    "",
                                    null,
                                    "",
                                    null,
                                    "",
                                    null,
                                    null,
                                    "",
                                    DateUtils.getCurrentTimeStamp().toString(),
                                    "");
                        } catch (Exception e) {
                            //noNothing
                            e.printStackTrace();
                        }
                        Validations.check(true, msgRetCodeConfig.getMsgExistEqp());
                    }
                }
            }
        }
        /*-----------------------------------------------------------*/
        /*   Send Transfer Data to MCS                              */
        /*-----------------------------------------------------------*/
        log.info("【step6】 - mcsManager.sendTransportJobCreateReq");
        try {
            result = mcsManager.sendTransportJobCreateReq(
                    objCommon,
                    tempTranJobCreateReq);
            Validations.check(ArrayUtils.getSize(result.getJobCreateResultSequenceData()) != nLen, msgRetCodeConfig.getMsgMismatchInoutLength());
        } catch (ServiceException e) {
            //Integration error
            if (Validations.isEquals(e.getCode(),msgRetCodeConfig.getMsgGrpcTimeOut())) throw e;
            //MCS Return error case
            if (Validations.isEquals(e.getCode(), msgRetCodeConfig.getMsgUnknownJobid()) || Validations.isEquals(e.getCode(), msgRetCodeConfig.getMsgDifferrentJobID())) {
                log.info("MCS Return is RC_UNKNOWN_JOBID or RC_DIFFERRENT_JOBID");
                Validations.check(ArrayUtils.getSize(result.getJobCreateResultSequenceData()) != nLen, msgRetCodeConfig.getMsgMismatchInoutLength());
                if (Constant.TM_TRANSPORT_TYPE_S.equals(tempTranJobCreateReq.getTransportType())) {
                    log.info("transportType  == 'S'");
                    Validations.check(nLen != 1, msgRetCodeConfig.getMsgNeedSingleData());
                    /* Inquiry JobID to MCS by inquiryType = 'C'*/
                    Params.TransportJobInqParams transportJobInq = new Params.TransportJobInqParams();
                    transportJobInq.setInquiryType(Constant.TM_INQUIRY_TYPE_BY_CARRIER);
                    transportJobInq.setCarrierID(tempTranJobCreateReq.getJobCreateData().get(0).getCarrierID());
                    Results.TransportJobInqResult transportJobInqResult = null;
                    log.info("【step7】 - mcsManager.sendTransportJobInq");
                    try {
                        transportJobInqResult = mcsManager.sendTransportJobInq(objCommon, transportJobInq);
                    } catch (ServiceException ex) {
                        if (Validations.isEquals(msgRetCodeConfig.getMsgJobNotFound(), ex.getCode())) {
                            tempTranJobCreateReq.setJobID("");
                            tempTranJobCreateReq.setRerouteFlag(false);
                        } else {
                            throw ex;
                        }
                    }
                    Validations.check(ArrayUtils.getSize(transportJobInqResult.getJobInqData()) != 1, msgRetCodeConfig.getMsgNeedSingleData());
                    log.info("【step8】- mcsManager.sendTransportJobCreateReq");
                    Results.TransportJobCreateReqResult transportJobCreateReqResultExcption = mcsManager.sendTransportJobCreateReq(objCommon, tempTranJobCreateReq);
                } else {
                    log.info("transportType != 'S'");
                }
            }
        }
        /*-----------------------------------------------------------*/
        /* Insert & Update TrabsferData by Return Structure from MCS*/
        /*-----------------------------------------------------------*/
        /*-- Delete the older job using carrier id as a key --*/
        Infos.TransferJobDeleteInfo transferJobDeleteInfo = new Infos.TransferJobDeleteInfo();
        List<Infos.CarrierJobInfo> carrierJobData = new ArrayList<>();
        transferJobDeleteInfo.setCarrierJobData(carrierJobData);

        //RerouteFunction
        if (BooleanUtils.isTrue(tempTranJobCreateReq.getRerouteFlag())) {
            if (ArrayUtils.isNotEmpty(result.getJobCreateResultSequenceData())) {
                for (Infos.AmhsJobCreateResult createdJob : result.getJobCreateResultSequenceData()) {
                    if (ArrayUtils.isNotEmpty(transferJobInfoList)) {
                        for (Infos.TransferJobInfo existingJob : transferJobInfoList) {
                            if (ObjectUtils.equalsWithValue(createdJob.getCarrierID(), existingJob.getCarrierID())) {
                                if (!ObjectUtils.equalsWithValue(createdJob.getCarrierJobID(), existingJob.getCarrierJobID())
                                        || !StringUtils.equals(result.getJobID(), existingJob.getJobID())) {
                                    log.info("Deleting older Carrier Job: {}", existingJob.getJobID());
                                    Infos.CarrierJobInfo carrierJobInfo = new Infos.CarrierJobInfo();
                                    carrierJobInfo.setCarrierJobID(existingJob.getCarrierJobID());
                                    carrierJobData.add(carrierJobInfo);
                                }
                                break;
                            }
                        }
                    }
                }
            }
            if (ArrayUtils.isNotEmpty(transferJobDeleteInfo.getCarrierJobData())) {
                String deleteTpye = Constant.TM_TRANSFER_JOB_DELETE_TYPE_CARRIERJOBID;
                log.info("【step9】 - transferJobMethod.transferJobDel");
                transferJobMethod.transferJobDel(objCommon, deleteTpye, transferJobDeleteInfo);
            }
        }
        log.info("Insert & Update TrabsferData by Return Structure from MCS");
        List<Infos.TransferJobInfo> saveTransferJobInfo = new ArrayList<>();
        for (int i = 0; i < nLen; i++) {
            Infos.TransferJobInfo transferJobInfo = new Infos.TransferJobInfo();
            transferJobInfo.setCarrierID(tempTranJobCreateReq.getJobCreateData().get(i).getCarrierID());
            transferJobInfo.setJobID(result.getJobID());
            transferJobInfo.setCarrierJobID(result.getJobCreateResultSequenceData().get(i).getCarrierJobID());
            transferJobInfo.setTransportType(tempTranJobCreateReq.getTransportType());
            transferJobInfo.setZoneType(tempTranJobCreateReq.getJobCreateData().get(i).getZoneType());
            transferJobInfo.setN2PurgeFlag(tempTranJobCreateReq.getJobCreateData().get(i).getN2PurgeFlag());
            transferJobInfo.setFromMachineID(tempTranJobCreateReq.getJobCreateData().get(i).getFromMachineID());
            transferJobInfo.setFromPortID(tempTranJobCreateReq.getJobCreateData().get(i).getFromPortID());
            transferJobInfo.setToStockerGroup(tempTranJobCreateReq.getJobCreateData().get(i).getToStockerGroup());
            transferJobInfo.setToMachineID(result.getJobCreateResultSequenceData().get(i).getToMachineID());
            transferJobInfo.setToPortID(transportJobCreateReqParams.getJobCreateData().get(i).getToMachine().size() > 0 ? transportJobCreateReqParams.getJobCreateData().get(i).getToMachine().get(0).getToPortID() : ObjectIdentifier.buildWithValue(""));
            transferJobInfo.setExpectedStartTime(tempTranJobCreateReq.getJobCreateData().get(i).getExpectedStartTime());
            transferJobInfo.setExpectedEndTime(tempTranJobCreateReq.getJobCreateData().get(i).getExpectedEndTime());
            transferJobInfo.setEstimatedStartTime(result.getJobCreateResultSequenceData().get(i).getEstimatedStartTime());
            transferJobInfo.setEstimatedEndTime(result.getJobCreateResultSequenceData().get(i).getEstimatedEndTime());
            transferJobInfo.setMandatoryFlag(tempTranJobCreateReq.getJobCreateData().get(i).getMandatoryFlag());
            transferJobInfo.setPriority(tempTranJobCreateReq.getJobCreateData().get(i).getPriority());
            transferJobInfo.setJobStatus("");
            transferJobInfo.setCarrierJobStatus("");
            transferJobInfo.setTimeStamp(DateUtils.getCurrentTimeStamp().toString());
            saveTransferJobInfo.add(transferJobInfo);
        }
        log.info("【step10】- transferJobPut");
        transferJobMethod.transferJobPut(objCommon, saveTransferJobInfo);

        return result;
    }

    @Override
    public Results.TransportJobCreateReqResult sxRtmsTransportJobCreateReq(Infos.ObjCommon objCommon, Params.TransportJobCreateReqParams transportJobCreateReqParams) {
        Results.TransportJobCreateReqResult result = new Results.TransportJobCreateReqResult();
        /*-----------------------------------------------------------*/
        /*   get Transfer job from Transfer Table by Carrier         */
        /*-----------------------------------------------------------*/
        int nLen = ArrayUtils.getSize(transportJobCreateReqParams.getJobCreateData());
        log.info("param JobCreateData size: {}", nLen);

        Params.TransportJobCreateReqParams tempTranJobCreateReq = new Params.TransportJobCreateReqParams();
        tempTranJobCreateReq.setJobID(transportJobCreateReqParams.getJobID());
        tempTranJobCreateReq.setRerouteFlag(transportJobCreateReqParams.getRerouteFlag());
        tempTranJobCreateReq.setTransportType(Constant.TM_TRANSPORT_TYPE_S);
        log.info("transportType: {}",Constant.TM_TRANSPORT_TYPE_S);

        List<Infos.AmhsJobCreateArray> jobCreateArrayList = new ArrayList<>();
        tempTranJobCreateReq.setJobCreateData(jobCreateArrayList);

        if (ArrayUtils.isNotEmpty(transportJobCreateReqParams.getJobCreateData())) {
            for (Infos.AmhsJobCreateArray data : transportJobCreateReqParams.getJobCreateData()) {
                Infos.AmhsJobCreateArray jobCreateArray = new Infos.AmhsJobCreateArray();
                jobCreateArray.setCarrierJobID(data.getCarrierJobID());
                jobCreateArray.setCarrierID(data.getCarrierID());
                jobCreateArrayList.add(jobCreateArray);

                //=================================================//
                // call sendReticlePodStatusInq                    //
                //=================================================//
                log.info("【step1】 - omsManager.sendReticlePodStatusInq");
                Results.ReticlePodStatusInqResult reticlePodStatusInqResult = omsManager.sendReticlePodStatusInq(objCommon,
                        data.getCarrierID());

                //=================================================//
                // Check ReticlePod is empty                       //
                //=================================================//
                if (null != reticlePodStatusInqResult && null != reticlePodStatusInqResult.getReticlePodStatusInfo() && BooleanUtils.isTrue(reticlePodStatusInqResult.getReticlePodStatusInfo().getEmptyFlag())){
                    log.info("emptyFlag == TRUE");
                    jobCreateArray.setZoneType(Constant.TM_ZONE_TYPE_EMP);
                }else {
                    log.info("emptyFlag != TRUE");
                    jobCreateArray.setZoneType(Constant.TM_ZONE_TYPE_RSP);
                }
                jobCreateArray.setN2PurgeFlag(data.getN2PurgeFlag());
                jobCreateArray.setFromMachineID(data.getFromMachineID());
                jobCreateArray.setFromPortID(data.getFromPortID());
                jobCreateArray.setToStockerGroup(data.getToStockerGroup());

                List<Infos.AmhsToDestination> tempToMachine = new ArrayList<>();
                jobCreateArray.setToMachine(tempToMachine);
                if (ArrayUtils.isNotEmpty(data.getToMachine())) {
                    for (Infos.AmhsToDestination toDestination : data.getToMachine()) {
                        Infos.AmhsToDestination tempDestination = new Infos.AmhsToDestination();
                        tempToMachine.add(tempDestination);
                        tempDestination.setToMachineID(toDestination.getToMachineID());
                        /*-----------------------------------------*/
                        /* Check toMachineID is EqpID              */
                        /*-----------------------------------------*/
                        log.info("【step2】 - equipmentMethod.checkEqpTransfer");
                        try {
                            equipmentMethod.checkEqpTransfer(objCommon, tempDestination.getToMachineID(),false);
                        } catch (ServiceException e) {
                            //doNothing
                        }
                        // When toMachine is Equipment.
                        log.info("toMachine is Equipment");
                        tempDestination.setToPortID(toDestination.getToPortID());
                    }
                }
                jobCreateArray.setExpectedStartTime(data.getExpectedStartTime());
                jobCreateArray.setExpectedEndTime(data.getExpectedEndTime());
                jobCreateArray.setMandatoryFlag(data.getMandatoryFlag());

                jobCreateArray.setPriority(Constant.TM_TRANSFER_JOB_PRIORITY_NINETY_NINE);
            }
        }
        tempTranJobCreateReq.setRerouteFlag(false);
        /*  Set Inquiry_Type = 'C' for Inquiry                                      */
        /*  if Transfer requested carrier had jobID with Transport Job type='S'     */
        /*  then must be embeded it to Transfer request message by each carrier.    */
        /*  In this case rerouteFlag must be 'TRUE'                                 */
        String inquiryType = Constant.TM_INQUIRY_TYPE_BY_CARRIER;
        List<ObjectIdentifier> carrierIDList = new ArrayList<>();
        if (ArrayUtils.isNotEmpty(transportJobCreateReqParams.getJobCreateData())) {
            carrierIDList = transportJobCreateReqParams.getJobCreateData().stream().map(Infos.AmhsJobCreateArray::getCarrierID).collect(Collectors.toList());
        }
        log.info("【step3】 - transferJobMethod.transferJobGet");
        List<Infos.TransferJobInfo> transferJobInfoList = null;
        try {
            transferJobInfoList = transferJobMethod.transferJobGet(objCommon, inquiryType, carrierIDList, null, null, null);
            log.info("exist records in OTXFERREQ");
            tempTranJobCreateReq.setRerouteFlag(true);
            tempTranJobCreateReq.setJobID(transferJobInfoList.get(0).getJobID());
        } catch (ServiceException e) {
            if (!Validations.isEquals(msgRetCodeConfig.getMsgRecordNotFound(), e.getCode())) {
                throw e;
            }
        }

        /*-----------------------------------------------------------*/
        /*   Send Transfer Data to MCS                              */
        /*-----------------------------------------------------------*/
        log.info("【step4】 - mcsManager.sendTransportJobCreateReq");
        try {
            result = mcsManager.sendTransportJobCreateReq(
                    objCommon,
                    tempTranJobCreateReq);
            Validations.check(ArrayUtils.getSize(result.getJobCreateResultSequenceData()) != nLen, msgRetCodeConfig.getMsgMismatchInoutLength());
        } catch (ServiceException e) {
            //Integration error
            if (Validations.isEquals(e.getCode(),msgRetCodeConfig.getMsgGrpcTimeOut())) throw e;
            //MCS Return error case
            if (Validations.isEquals(e.getCode(), msgRetCodeConfig.getMsgUnknownJobid()) || Validations.isEquals(e.getCode(), msgRetCodeConfig.getMsgDifferrentJobID())) {
                log.info("MCS Return is RC_UNKNOWN_JOBID or RC_DIFFERRENT_JOBID");
                Validations.check(ArrayUtils.getSize(result.getJobCreateResultSequenceData()) != nLen, msgRetCodeConfig.getMsgMismatchInoutLength());
                if (Constant.TM_TRANSPORT_TYPE_S.equals(tempTranJobCreateReq.getTransportType())) {
                    log.info("transportType  == 'S'");
                    Validations.check(nLen != 1, msgRetCodeConfig.getMsgNeedSingleData());
                    /* Inquiry JobID to MCS by inquiryType = 'C'*/
                    Params.TransportJobInqParams transportJobInq = new Params.TransportJobInqParams();
                    transportJobInq.setInquiryType(Constant.TM_INQUIRY_TYPE_BY_CARRIER);
                    transportJobInq.setCarrierID(tempTranJobCreateReq.getJobCreateData().get(0).getCarrierID());
                    Results.TransportJobInqResult transportJobInqResult = null;
                    log.info("【step5】 - mcsManager.sendTransportJobInq");
                    try {
                        transportJobInqResult = mcsManager.sendTransportJobInq(objCommon, transportJobInq);
                    } catch (ServiceException ex) {
                        if (Validations.isEquals(msgRetCodeConfig.getMsgJobNotFound(), ex.getCode())) {
                            tempTranJobCreateReq.setJobID("");
                            tempTranJobCreateReq.setRerouteFlag(false);
                        } else {
                            throw ex;
                        }
                    }
                    Validations.check(ArrayUtils.getSize(transportJobInqResult.getJobInqData()) != 1, msgRetCodeConfig.getMsgNeedSingleData());
                    log.info("【step6】- mcsManager.sendTransportJobCreateReq");
                    Results.TransportJobCreateReqResult transportJobCreateReqResultExcption = mcsManager.sendTransportJobCreateReq(objCommon, tempTranJobCreateReq);
                } else {
                    log.info("transportType != 'S'");
                }
            }
        }
        /*-----------------------------------------------------------*/
        /* Insert & Update TrabsferData by Return Structure from MCS*/
        /*-----------------------------------------------------------*/
        /*-- Delete the older job using carrier id as a key --*/
        Infos.TransferJobDeleteInfo transferJobDeleteInfo = new Infos.TransferJobDeleteInfo();
        List<Infos.CarrierJobInfo> carrierJobData = new ArrayList<>();
        transferJobDeleteInfo.setCarrierJobData(carrierJobData);

        //RerouteFunction
        if (BooleanUtils.isTrue(tempTranJobCreateReq.getRerouteFlag())) {
            if (ArrayUtils.isNotEmpty(result.getJobCreateResultSequenceData())) {
                for (Infos.AmhsJobCreateResult createdJob : result.getJobCreateResultSequenceData()) {
                    if (ArrayUtils.isNotEmpty(transferJobInfoList)) {
                        for (Infos.TransferJobInfo existingJob : transferJobInfoList) {
                            if (ObjectUtils.equalsWithValue(createdJob.getCarrierID(), existingJob.getCarrierID())) {
                                if (!ObjectUtils.equalsWithValue(createdJob.getCarrierJobID(), existingJob.getCarrierJobID())
                                        || !StringUtils.equals(result.getJobID(), existingJob.getJobID())) {
                                    log.info("Deleting older Carrier Job: {}", existingJob.getJobID());
                                    Infos.CarrierJobInfo carrierJobInfo = new Infos.CarrierJobInfo();
                                    carrierJobInfo.setCarrierJobID(existingJob.getCarrierJobID());
                                    carrierJobData.add(carrierJobInfo);
                                }
                                break;
                            }
                        }
                    }
                }
            }
            if (ArrayUtils.isNotEmpty(transferJobDeleteInfo.getCarrierJobData())) {
                String deleteTpye = Constant.TM_TRANSFER_JOB_DELETE_TYPE_CARRIERJOBID;
                log.info("【step7】 - transferJobMethod.transferJobDel");
                transferJobMethod.transferJobDel(objCommon, deleteTpye, transferJobDeleteInfo);
            }
        }
        log.info("Insert & Update TrabsferData by Return Structure from MCS");
        List<Infos.TransferJobInfo> saveTransferJobInfo = new ArrayList<>();
        for (int i = 0; i < nLen; i++) {
            Infos.TransferJobInfo transferJobInfo = new Infos.TransferJobInfo();
            transferJobInfo.setCarrierID(tempTranJobCreateReq.getJobCreateData().get(i).getCarrierID());
            transferJobInfo.setJobID(result.getJobID());
            transferJobInfo.setCarrierJobID(result.getJobCreateResultSequenceData().get(i).getCarrierJobID());
            transferJobInfo.setTransportType(tempTranJobCreateReq.getTransportType());
            transferJobInfo.setZoneType(tempTranJobCreateReq.getJobCreateData().get(i).getZoneType());
            transferJobInfo.setN2PurgeFlag(tempTranJobCreateReq.getJobCreateData().get(i).getN2PurgeFlag());
            transferJobInfo.setFromMachineID(tempTranJobCreateReq.getJobCreateData().get(i).getFromMachineID());
            transferJobInfo.setFromPortID(tempTranJobCreateReq.getJobCreateData().get(i).getFromPortID());
            transferJobInfo.setToStockerGroup(tempTranJobCreateReq.getJobCreateData().get(i).getToStockerGroup());
            transferJobInfo.setToMachineID(result.getJobCreateResultSequenceData().get(i).getToMachineID());
            transferJobInfo.setToPortID(transportJobCreateReqParams.getJobCreateData().get(i).getToMachine().size() > 0 ? transportJobCreateReqParams.getJobCreateData().get(i).getToMachine().get(0).getToPortID() : ObjectIdentifier.buildWithValue(""));
            transferJobInfo.setExpectedStartTime(tempTranJobCreateReq.getJobCreateData().get(i).getExpectedStartTime());
            transferJobInfo.setExpectedEndTime(tempTranJobCreateReq.getJobCreateData().get(i).getExpectedEndTime());
            transferJobInfo.setEstimatedStartTime(result.getJobCreateResultSequenceData().get(i).getEstimatedStartTime());
            transferJobInfo.setEstimatedEndTime(result.getJobCreateResultSequenceData().get(i).getEstimatedEndTime());
            transferJobInfo.setMandatoryFlag(tempTranJobCreateReq.getJobCreateData().get(i).getMandatoryFlag());
            transferJobInfo.setPriority(Constant.TM_TRANSFER_JOB_PRIORITY_NINETY_NINE);
            transferJobInfo.setJobStatus("");
            transferJobInfo.setCarrierJobStatus("");
            transferJobInfo.setTimeStamp(DateUtils.getCurrentTimeStamp().toString());
            saveTransferJobInfo.add(transferJobInfo);
        }
        log.info("【step8】- transferJobPut");
        transferJobMethod.transferJobPut(objCommon, saveTransferJobInfo);

        return result;
    }
}




