package com.fa.cim.tms.service.impl;

import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.tms.common.constant.Constant;
import com.fa.cim.tms.common.enums.EnvCodeEnum;
import com.fa.cim.tms.config.MsgRetCodeConfig;
import com.fa.cim.tms.dto.Params;
import com.fa.cim.tms.dto.Results;
import com.fa.cim.tms.manager.IMCSManager;
import com.fa.cim.tms.manager.IOMSManager;
import com.fa.cim.tms.method.IEnvMethod;
import com.fa.cim.tms.method.IEquipmentMethod;
import com.fa.cim.tms.method.ITransferJobMethod;
import com.fa.cim.tms.pojo.Infos;
import com.fa.cim.tms.pojo.ObjectIdentifier;
import com.fa.cim.tms.service.ITransportJobStatusRptService;
import com.fa.cim.tms.utils.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * description:
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/10/21                               Neyo                create file
 *
 * @author: Neyo
 * @date: 2020/10/21 13:44
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Service
@Slf4j
public class TransportJobStatusRptService implements ITransportJobStatusRptService {
    @Autowired
    private IOMSManager omsManager;
    @Autowired
    private IMCSManager mcsManager;
    @Autowired
    private IEnvMethod envMethod;
    @Autowired
    private ITransferJobMethod transferJobMethod;
    @Autowired
    private IEquipmentMethod equipmentMethod;
    @Autowired
    private MsgRetCodeConfig msgRetCodeConfig;

    public Results.TransportJobStatusReportResult sxTransportJobStatusReport(Infos.ObjCommon objCommon, Params.TransportJobStatusReportParams transportJobStatusReportParams) {
        Results.TransportJobStatusReportResult result = new Results.TransportJobStatusReportResult();

        String statusRptFlagTM3 = envMethod.getEnvFromOmsDB(EnvCodeEnum.OM_TM03_STATUS_FLAG.getValue());//"ON"
        String errorOutFlagTM3 = envMethod.getEnvFromOmsDB(EnvCodeEnum.OM_TM03_ERROR_OUT.getValue());
        String moReportFlagTM3 = envMethod.getEnvFromOmsDB(EnvCodeEnum.OM_TM03_MO_REPORT_FLAG.getValue());//"ON"
        String getCarrierFlag = envMethod.getEnvFromOmsDB(EnvCodeEnum.OM_GET_CARRIER_FLAG.getValue());//"OFF"

        log.info("env OM_TM03_STATUS_FLAG: {}", statusRptFlagTM3);
        log.info("env OM_TM03_ERROR_OUT: {}", errorOutFlagTM3);
        log.info("env OM_TM03_MO_REPORT_FLAG: {}", moReportFlagTM3);
        log.info("env OM_GET_CARRIER_FLAG: {}", getCarrierFlag);

        Boolean castTxNoSendWarning = false;

        String jobID = null;
        String carrierJobID = null;

        /*-----------------------------------------*/
        /* Extra Xfer Status Change report 0.01    */
        /*-----------------------------------------*/
        List<ObjectIdentifier> carrierIDs = new ArrayList<>();
        List<ObjectIdentifier> machineIDs = new ArrayList<>();
        List<ObjectIdentifier> portIDs = new ArrayList<>();
        List<ObjectIdentifier> carrierJobIDs = new ArrayList<>();
        Boolean reportFailFlag = false;

        if (Constant.TM_ON.equals(statusRptFlagTM3)) {
            log.info("xferStatusChangeRpt by M3");
            if (ArrayUtils.isNotEmpty(transportJobStatusReportParams.getJobStatusReportData())) {
                for (Infos.JobStatusReportArray jobStatusReportArray : transportJobStatusReportParams.getJobStatusReportData()) {
                    if (BooleanUtils.isTrue(transportJobStatusReportParams.getJobRemoveFlag())
                            || BooleanUtils.isTrue(jobStatusReportArray.getCarrierJobRemoveFlag())) {
                        log.info("jobRemoveFlag == TRUE || carrierJobRemoveFlag == TRUE");
                        /*-----------------------------------------*/
                        /* Check toMachineID is EqpID              */
                        /*-----------------------------------------*/
                        ObjectIdentifier toMachineID = jobStatusReportArray.getToMachineID();
                        log.info("【step1】 - equipmentMethod.checkEqpTransfer");
                        Boolean eqpFlag = true;
                        try {
                            equipmentMethod.checkEqpTransfer(objCommon, toMachineID,true);
                        } catch (ServiceException e) {
                            if (!Validations.isEquals(msgRetCodeConfig.getMsgRecordNotFound(), e.getCode())) {
                                throw e;
                            }
                            log.info("checkEqp is record not found");
                            eqpFlag = false;
                        }
                        if (BooleanUtils.isTrue(eqpFlag)) {
                            log.info("checkEqp is record is OK");
                            continue;
                        } else {
                            ObjectIdentifier carrierID = jobStatusReportArray.getCarrierID();
                            ObjectIdentifier machineID = jobStatusReportArray.getToMachineID();
                            ObjectIdentifier portID = jobStatusReportArray.getToPortID();
                            String xferStatus = "";
                            Boolean manualInFlag = false;
                            String zoneID = Constant.TM_STRING_DEFAULT;
                            String shelfType = "";

                            //If toMachine is STK and toPort is blank then xferStatus <- MO
                            if (Constant.TM_ON.equals(moReportFlagTM3)) {
                                log.info("moReportFlagTM3 = ON");
                                //Get OTXFERREQ by carrierID
                                List<Infos.TransferJobInfo> transferJobInfoList = null;
                                Boolean recordFoundFlag = true;
                                log.info("【step2】 - transferJobMethod.transferJobGet");
                                try {
                                    transferJobInfoList = transferJobMethod.transferJobGet(objCommon,
                                            Constant.TM_INQUIRY_TYPE_BY_CARRIER,
                                            Arrays.asList(jobStatusReportArray.getCarrierID()),
                                            null,
                                            null,
                                            null);
                                } catch (ServiceException e) {
                                    if (!Validations.isEquals(msgRetCodeConfig.getMsgRecordNotFound(), e.getCode())) {
                                        throw e;
                                    }
                                    recordFoundFlag = false;
                                }
                                if (BooleanUtils.isTrue(recordFoundFlag)) {
                                    log.info("transferJobGet != RC_RECORD_NOT_FOUND");
                                    //Check equal carrierJobID
                                    Boolean bFoundFlag = false;
                                    if (ArrayUtils.isNotEmpty(transferJobInfoList)) {
                                        for (Infos.TransferJobInfo transferJobInfo : transferJobInfoList) {
                                            log.info("transferJobGet carrierJobID: {}", transferJobInfo.getCarrierJobID());
                                            log.info("inputParam carrierJobID: {}", jobStatusReportArray.getCarrierJobID());
                                            if (StringUtils.equals(transferJobInfo.getCarrierJobID(), jobStatusReportArray.getCarrierJobID())) {
                                                log.info("Same carrierJobID Found !");
                                                bFoundFlag = true;
                                                break;
                                            }
                                        }
                                    }
                                    if (BooleanUtils.isTrue(bFoundFlag)) {
                                        log.info("carrierJobId is found");
                                        //Check toPortID not BLANK
                                        for (Infos.TransferJobInfo transferJobInfo : transferJobInfoList) {
                                            if (ObjectUtils.isNotEmptyWithValue(transferJobInfo.getToPortID())) {
                                                log.info("toPortID != NULL && toPortID != BLANK");
                                                //Check toMachineID is STK
                                                Boolean eqpCheckFlag = true;
                                                log.info("【step3】 - equipmentMethod.checkEqpTransfer");
                                                try {
                                                    equipmentMethod.checkEqpTransfer(objCommon, transferJobInfo.getToMachineID(),true);
                                                } catch (ServiceException e) {
                                                    if (Validations.isEquals(msgRetCodeConfig.getMsgRecordNotFound(), e.getCode())) {
                                                        xferStatus = Constant.TM_TRANSSTATE_MANUALOUT;
                                                    } else {
                                                        throw e;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            log.info("【step4】 - omsManager.sendCarrierTransferStatusChangeRpt");
                            Results.CarrierTransferStatusChangeRptResult carrierTransferStatusChangeRptResult = null;
                            try {
                                carrierTransferStatusChangeRptResult = omsManager.sendCarrierTransferStatusChangeRpt(objCommon,
                                        carrierID,
                                        xferStatus,
                                        manualInFlag,
                                        machineID,
                                        portID,
                                        zoneID,
                                        shelfType,
                                        transportJobStatusReportParams.getJobID(),
                                        jobStatusReportArray.getCarrierJobID());
                            } catch (Exception e) {
                                if (e instanceof ServiceException && Validations.isEquals(msgRetCodeConfig.getMsgOmsCastTxNoSend(), ((ServiceException)e).getCode())) {
                                    //do nothing
                                    log.error("oms cast no send or shutdown");
                                    castTxNoSendWarning = true;
                                } else {
                                    if (Constant.TM_ON.equals(errorOutFlagTM3)) {
                                        carrierIDs.add(jobStatusReportArray.getCarrierID());
                                        machineIDs.add(jobStatusReportArray.getToMachineID());
                                        portIDs.add(jobStatusReportArray.getToPortID());
                                        carrierJobIDs.add(ObjectIdentifier.buildWithValue(jobStatusReportArray.getCarrierJobID()));
                                    }
                                    reportFailFlag = true;
                                }
                            }
                        }
                    }
                }
            }
        }
        /*--------------------------------------------*/
        /* If jobRemoveFlag== TRUE,                   */
        /* then delete all record from OTXFERREQ       */
        /*--------------------------------------------*/
        if (BooleanUtils.isTrue(transportJobStatusReportParams.getJobRemoveFlag())) {
            log.info("jobRemoveFlag == TRUE");
            /*------------------------------------------------*/
            /* Check to_location,To equipment case            */
            /* Set transferJobStatus = "XCMP"                 */
            /* send sendCarrierTransferStatusChangeRpt to OMS */
            /*------------------------------------------------*/
            String transferJobStatus = Constant.TM_TRANSFER_JOB_STATUS_XCMP;
            List<Infos.TransferJobInfo> seqTransferJobInfo = new ArrayList<>();
            if (ArrayUtils.isNotEmpty(transportJobStatusReportParams.getJobStatusReportData())) {
                for (Infos.JobStatusReportArray jobStatusReportArray : transportJobStatusReportParams.getJobStatusReportData()) {
                    carrierJobID = jobStatusReportArray.getCarrierJobID();
                    jobID = transportJobStatusReportParams.getJobID();
                    log.info("carrierJobID: {}", carrierJobID);
                    log.info("jobID: {}", jobID);
                    log.info("【step5】 - transferJobMethod.transferCarrierGet");
                    List<Infos.TransferJobInfo> transferJobInfoList = transferJobMethod.transferCarrierGet(objCommon, jobID, carrierJobID);
                    Infos.TransferJobInfo transferJobInfo = transferJobInfoList.get(0);
                    seqTransferJobInfo.add(transferJobInfo);
                    /**************************************/
                    /* If MCS  can't send carrierID,      */
                    /* TMS read OTXFERREQ for getting it.  */
                    /**************************************/
                    if (Constant.TM_ON.equals(getCarrierFlag)) {
                        transferJobInfo.setCarrierID(transferJobInfoList.get(0).getCarrierID());
                        transferJobInfo.setToMachineID(transferJobInfoList.get(0).getToMachineID());
                        if (Constant.TM_TRANSFER_JOB_STATUS_TWO.equals(transportJobStatusReportParams.getJobStatus())) {
                            transferJobStatus = Constant.TM_TRANSFER_JOB_STATUS_XERR;
                        }
                    } else {
                        transferJobInfo.setCarrierID(jobStatusReportArray.getCarrierID());
                        transferJobInfo.setToMachineID(jobStatusReportArray.getToMachineID());
                    }
                    transferJobInfo.setJobID(transportJobStatusReportParams.getJobID());
                    transferJobInfo.setCarrierJobID(jobStatusReportArray.getCarrierJobID());

                    /*-----------------------------------------*/                                                           //D3000129
                    /* Check toMachineID is EqpID              */                                                           //D3000129
                    /*-----------------------------------------*/
                    Boolean eqpFlag = true;
                    log.info("【step6】 - equipmentMethod.checkEqpTransfer");
                    try {
                        equipmentMethod.checkEqpTransfer(objCommon, transferJobInfo.getToMachineID(),true);
                    } catch (ServiceException e) {
                        eqpFlag = false;
                        transferJobInfo.setToPortID(jobStatusReportArray.getToPortID());
                    }
                    if (BooleanUtils.isTrue(eqpFlag)) {
                        log.info("toMachine is Eqp");
                        transferJobInfo.setToPortID(transferJobInfoList.get(0).getToPortID());
                    }
                    transferJobInfo.setJobStatus(transportJobStatusReportParams.getJobStatus());
                    transferJobInfo.setCarrierJobStatus(jobStatusReportArray.getCarrierjobStatus());
                }
            }
            log.info("【step7】 - equipmentMethod.checkEqpSendCOMPM3");
            try {
                equipmentMethod.checkEqpSendCOMPM3(objCommon, seqTransferJobInfo, transferJobStatus);
            } catch (ServiceException e) {
                if (Validations.isEquals(msgRetCodeConfig.getMsgOmsCastTxNoSend(), e.getCode())) {
                    log.error("oms cast no send or shutdown");
                    castTxNoSendWarning = true;
                }else {
                    throw e;
                }
            }
            /*------------------------------*/
            /* Set DeleteType = 'JB'       */
            /* Delete records from OTXFERREQ */
            /*------------------------------*/
            Infos.TransferJobDeleteInfo transferJobDeleteInfo = new Infos.TransferJobDeleteInfo();
            transferJobDeleteInfo.setJobID(transportJobStatusReportParams.getJobID());
            log.info("【step8】 - transferJobMethod.transferJobDel");
            transferJobMethod.transferJobDel(objCommon,
                    Constant.TM_TRANSFER_JOB_DELETE_TYPE_JOBID,
                    transferJobDeleteInfo);

            /* Report XferJob status to OMS */
            log.info("【step9】 - omsManager.sendDurableXferStatusChangeRpt");
            omsManager.sendDurableXferStatusChangeRpt(objCommon,
                    seqTransferJobInfo,
                    Constant.TM_DURABLE_XFER_JOB_STATUS_COMPLETED,
                    "",true);
        } else {
            log.info("jobRemoveFlag != TRUE");
            /*--------------------------------------------------------------*/
            /* Delete or Update TrabsferData by Return Structure from MCS  */
            /*--------------------------------------------------------------*/
            List<Infos.TransferJobInfo> seqTransferJobInfo = new ArrayList<>();
            if (ArrayUtils.isNotEmpty(transportJobStatusReportParams.getJobStatusReportData())) {
                for (Infos.JobStatusReportArray jobStatusReportArray : transportJobStatusReportParams.getJobStatusReportData()) {
                    if (BooleanUtils.isTrue(jobStatusReportArray.getCarrierJobRemoveFlag())) {
                        log.info("carrierJobRemoveFlag == TRUE");
                        /* set toMachineID and toPortID  */
                        String transferJobStatus = Constant.TM_TRANSFER_JOB_STATUS_XCMP;
                        carrierJobID = jobStatusReportArray.getCarrierJobID();
                        jobID = transportJobStatusReportParams.getJobID();
                        log.info("【step10】 - transferJobMethod.transferCarrierGet");
                        List<Infos.TransferJobInfo> transferJobInfoList = null;
                        try {
                            transferJobInfoList = transferJobMethod.transferCarrierGet(objCommon,
                                    jobID,
                                    carrierJobID);
                        } catch (ServiceException e) {
                            throw e;
                        }
                        Infos.TransferJobInfo transferJobInfo = transferJobInfoList.get(0);
                        seqTransferJobInfo.add(transferJobInfo);

                        /**************************************/
                        /* If MCS can't send carrierID,       */
                        /* TMS read OTXFERREQ for getting it.  */
                        /**************************************/
                        if (Constant.TM_ON.equals(getCarrierFlag)) {
                            transferJobInfo.setCarrierID(transferJobInfoList.get(0).getCarrierID());
                            transferJobInfo.setToMachineID(transferJobInfoList.get(0).getToMachineID());
                            if (Constant.TM_TRANSFER_JOB_STATUS_TWO.equals(transportJobStatusReportParams.getJobStatus())) {
                                transferJobStatus = Constant.TM_TRANSFER_JOB_STATUS_XERR;
                            }
                        } else {
                            transferJobInfo.setCarrierID(jobStatusReportArray.getCarrierID());
                            transferJobInfo.setToMachineID(jobStatusReportArray.getToMachineID());
                        }

                        transferJobInfo.setJobID(transportJobStatusReportParams.getJobID());
                        transferJobInfo.setCarrierJobID(jobStatusReportArray.getCarrierJobID());

                        /*-----------------------------------------*/                                                  //D3000129
                        /* Check toMachineID is EqpID              */                                                  //D3000129
                        /*-----------------------------------------*/
                        Boolean eqpFlag = true;
                        log.info("【step11】 - equipmentMethod.checkEqpTransfer");
                        try {
                            equipmentMethod.checkEqpTransfer(objCommon, transferJobInfo.getToMachineID(),true);
                        } catch (ServiceException e) {
                            eqpFlag = false;
                            transferJobInfo.setToPortID(jobStatusReportArray.getToPortID());
                        }
                        if (BooleanUtils.isTrue(eqpFlag)) {
                            log.info("toMachine is Eqp");
                            transferJobInfo.setToPortID(transferJobInfoList.get(0).getToPortID());
                        }

                        transferJobInfo.setJobStatus(transportJobStatusReportParams.getJobStatus());
                        transferJobInfo.setCarrierJobStatus(jobStatusReportArray.getCarrierjobStatus());

                        log.info("【step12】 - equipmentMethod.checkEqpSendCOMPM3");
                        try {
                            equipmentMethod.checkEqpSendCOMPM3(objCommon,
                                    seqTransferJobInfo,
                                    transferJobStatus);
                        } catch (ServiceException e) {
                            if (Validations.isEquals(msgRetCodeConfig.getMsgOmsCastTxNoSend(), e.getCode())) {
                                log.error("oms cast no send or shutdown");
                                castTxNoSendWarning = true;
                            }else {
                                throw e;
                            }
                        }
                    } else {
                        log.info("carrierJobRemoveFlag != TRUE");
                        log.info("Get xferjob info");
                        log.info("【step13】 - transferJobMethod.transferCarrierGet");
                        List<Infos.TransferJobInfo> transferJobInfoList = transferJobMethod.transferCarrierGet(objCommon,
                                transportJobStatusReportParams.getJobID(),
                                jobStatusReportArray.getCarrierJobID());
                        seqTransferJobInfo.add(transferJobInfoList.get(0));

                        seqTransferJobInfo.get(0).setJobStatus(transportJobStatusReportParams.getJobStatus());
                        seqTransferJobInfo.get(0).setCarrierJobStatus(jobStatusReportArray.getCarrierjobStatus());
                    }

                    // Update Transfer Request Data
                    log.info("【step14】 - transferJobMethod.transferJobMod");
                    transferJobMethod.transferJobMod(objCommon,
                            transportJobStatusReportParams.getJobID(),
                            transportJobStatusReportParams.getJobStatus(),
                            jobStatusReportArray.getCarrierJobID(),
                            jobStatusReportArray.getCarrierID(),
                            jobStatusReportArray.getCarrierjobStatus(),
                            jobStatusReportArray.getCarrierJobRemoveFlag());

                    /* Report XferJob status to OMS */
                    String strXferJobStatus = null;
                    if (BooleanUtils.isTrue(jobStatusReportArray.getCarrierJobRemoveFlag())) {
                        strXferJobStatus = Constant.TM_DURABLE_XFER_JOB_STATUS_COMPLETED;
                    } else {
                        strXferJobStatus = Constant.TM_DURABLE_XFER_JOB_STATUS_UPDATED;
                    }

                    log.info("strXferJobStatus: {}", strXferJobStatus);
                    log.info("【step15】 - omsManager.sendDurableXferStatusChangeRpt");
                    omsManager.sendDurableXferStatusChangeRpt(objCommon,
                            seqTransferJobInfo,
                            strXferJobStatus,
                            "",true);

                }
            }
        }
        if (Constant.TM_ON.equals(statusRptFlagTM3) && Constant.TM_ON.equals(errorOutFlagTM3)) {
            if (ArrayUtils.isNotEmpty(machineIDs)) {
                for (int q = 0; q < ArrayUtils.getSize(machineIDs); q++) {
                    ObjectIdentifier currStockerID = machineIDs.get(q);
                    Boolean detailFlag = false;

                    log.info("【step16】 - omsManager.sendStockerInfoInq");
                    Results.StockerInfoInqResult stockerInfoInqResult = null;
                    try {
                        stockerInfoInqResult = omsManager.sendStockerInfoInq(objCommon,
                                currStockerID,
                                detailFlag);
                    } catch (ServiceException e) {
                        continue;
                    }

                    List<Results.ResourceInfo> tmpResourceInfoData = new ArrayList<>();
                    ObjectIdentifier toPortID = null;
                    if (null != stockerInfoInqResult && ArrayUtils.isEmpty(stockerInfoInqResult.getResourceInfoData())) {
                        log.info("No resourceInfoData.");
                        continue;
                    }
                    tmpResourceInfoData = stockerInfoInqResult.getResourceInfoData();
                    for (Results.ResourceInfo resourceInfo : stockerInfoInqResult.getResourceInfoData()) {
                        if (Constant.TM_RESOURCE_TYPE_MANUAL.equals(resourceInfo.getResourceType())
                                && (Constant.TM_STRING_O.equals(resourceInfo.getResourceID().substring(0, 1))
                                || Constant.TM_STRING_W.equals(resourceInfo.getResourceID().substring(0, 1)))) {
                            toPortID = ObjectIdentifier.buildWithValue(resourceInfo.getResourceID());
                            log.info("toPortID: {}", ObjectIdentifier.fetchValue(toPortID));
                            break;
                        }
                    }
                    /*-----------------------------------------------------------*/
                    /*   Send Transfer Data to MCS                               */
                    /*-----------------------------------------------------------*/
                    Params.TransportJobCreateReqParams tempTranJobCreateReq = new Params.TransportJobCreateReqParams();
                    tempTranJobCreateReq.setTransportType(Constant.TM_TRANSPORT_TYPE_S);
                    tempTranJobCreateReq.setJobID("");

                    log.info("Send S1 for Error Out!");
                    log.info("TranJobCreateReq.transportType: {}", tempTranJobCreateReq.getTransportType());

                    Infos.AmhsToDestination toDestination = new Infos.AmhsToDestination();
                    toDestination.setToMachineID(machineIDs.get(q));
                    List<Infos.AmhsToDestination> seqToMahchine = Arrays.asList(toDestination);

                    Infos.AmhsJobCreateArray jobCreateArray = new Infos.AmhsJobCreateArray();
                    jobCreateArray.setToMachine(seqToMahchine);
                    List<Infos.AmhsJobCreateArray> jobCreateArrayList = Arrays.asList(jobCreateArray);

                    tempTranJobCreateReq.setJobCreateData(jobCreateArrayList);

                    if (ObjectUtils.isNotEmptyWithValue(tempTranJobCreateReq.getJobCreateData().get(0).getToMachine().get(0).getToMachineID())) {
                        log.info("toMachineID: {}", ObjectIdentifier.fetchValue(tempTranJobCreateReq.getJobCreateData().get(0).getToMachine().get(0).getToMachineID()));
                    }
                    toDestination.setToPortID(toPortID);
                    jobCreateArray.setZoneType(Constant.TM_STRING_DEFAULT);
                    jobCreateArray.setCarrierJobID("");
                    jobCreateArray.setCarrierID(carrierIDs.get(q));

                    String noFromMachineIDForSTKFlag = envMethod.getEnvFromOmsDB(EnvCodeEnum.OM_OM01_NO_FROM_MACHINEID_FOR_STK.getValue());
                    log.info("env OM_OM01_NO_FROM_MACHINEID_FOR_STK: {}", noFromMachineIDForSTKFlag);
                    if (Constant.TM_ON.equals(noFromMachineIDForSTKFlag)) {
                        log.info("OM_OM01_NO_FROM_MACHINEID_FOR_STK = ON");
                        jobCreateArray.setFromMachineID(ObjectIdentifier.buildWithValue(""));
                    } else {
                        log.info("OM_OM01_NO_FROM_MACHINEID_FOR_STK = OFF");
                        jobCreateArray.setFromMachineID(machineIDs.get(q));
                    }
                    jobCreateArray.setExpectedEndTime(Constant.TM_TRANSFER_JOB_EXPECTED_END_TIME);
                    jobCreateArray.setPriority(Constant.TM_TRANSFER_JOB_PRIORITY_FOUR);
                    jobCreateArray.setMandatoryFlag(false);
                    tempTranJobCreateReq.setRerouteFlag(false);

                    log.info("【step17】 - mcsManager.sendTransportJobCreateReq");
                    Results.TransportJobCreateReqResult transportJobCreateReqResult = mcsManager.sendTransportJobCreateReq(objCommon, tempTranJobCreateReq);
                    Validations.check(null != transportJobCreateReqResult && ArrayUtils.getSize(transportJobCreateReqResult.getJobCreateResultSequenceData()) != 1, msgRetCodeConfig.getMsgMismatchInoutLength());
                    /*-----------------------------------------------------------*/
                    /* Insert & Update TrabsferData by Return Structure from MCS */
                    /*-----------------------------------------------------------*/
                    Infos.TransferJobInfo transferJobInfo = new Infos.TransferJobInfo();
                    transferJobInfo.setCarrierID(transportJobCreateReqResult.getJobCreateResultSequenceData().get(0).getCarrierID());
                    transferJobInfo.setJobID(transportJobCreateReqResult.getJobID());
                    transferJobInfo.setCarrierJobID(transportJobCreateReqResult.getJobCreateResultSequenceData().get(0).getCarrierJobID());
                    transferJobInfo.setTransportType(tempTranJobCreateReq.getTransportType());
                    transferJobInfo.setZoneType(Constant.TM_STRING_DEFAULT);
                    transferJobInfo.setN2PurgeFlag(tempTranJobCreateReq.getJobCreateData().get(0).getN2PurgeFlag());
                    transferJobInfo.setFromMachineID(tempTranJobCreateReq.getJobCreateData().get(0).getFromMachineID());
                    transferJobInfo.setFromPortID(tempTranJobCreateReq.getJobCreateData().get(0).getFromPortID());
                    transferJobInfo.setToStockerGroup(tempTranJobCreateReq.getJobCreateData().get(0).getToStockerGroup());
                    transferJobInfo.setToMachineID(transportJobCreateReqResult.getJobCreateResultSequenceData().get(0).getToMachineID());
                    transferJobInfo.setToPortID(ArrayUtils.isEmpty(tempTranJobCreateReq.getJobCreateData().get(0).getToMachine()) ? ObjectIdentifier.buildWithValue("") : tempTranJobCreateReq.getJobCreateData().get(0).getToMachine().get(0).getToPortID());
                    transferJobInfo.setExpectedStartTime(tempTranJobCreateReq.getJobCreateData().get(0).getExpectedStartTime());
                    transferJobInfo.setExpectedEndTime(tempTranJobCreateReq.getJobCreateData().get(0).getExpectedEndTime());
                    transferJobInfo.setEstimatedStartTime(transportJobCreateReqResult.getJobCreateResultSequenceData().get(0).getEstimatedStartTime());
                    transferJobInfo.setEstimatedEndTime(transportJobCreateReqResult.getJobCreateResultSequenceData().get(0).getEstimatedEndTime());
                    transferJobInfo.setMandatoryFlag(tempTranJobCreateReq.getJobCreateData().get(0).getMandatoryFlag());
                    transferJobInfo.setPriority(tempTranJobCreateReq.getJobCreateData().get(0).getPriority());
                    transferJobInfo.setJobStatus("");
                    transferJobInfo.setCarrierJobStatus("");
                    transferJobInfo.setTimeStamp(DateUtils.getCurrentTimeStamp().toString());

                    log.info("【step18】 - transferJobMethod.transferJobPut(");
                    transferJobMethod.transferJobPut(objCommon, Arrays.asList(transferJobInfo));

                }
            }
        }
        result.setJobID(transportJobStatusReportParams.getJobID());
        Validations.check(BooleanUtils.isTrue(reportFailFlag), result,msgRetCodeConfig.getMsgReportFail());
        Validations.check(BooleanUtils.isTrue(castTxNoSendWarning), result,msgRetCodeConfig.getMsgOmsCastTxNoSend());

        return result;
    }

    @Override
    public Results.TransportJobStatusReportResult sxRtmsTransportJobStatusReport(Infos.ObjCommon objCommon, Params.TransportJobStatusReportParams transportJobStatusReportParams) {
        Results.TransportJobStatusReportResult result = new Results.TransportJobStatusReportResult();

        String statusRptFlagTM3 = envMethod.getEnvFromOmsDB(EnvCodeEnum.OM_TM03_STATUS_FLAG.getValue());//"ON"
        String errorOutFlagTM3 = envMethod.getEnvFromOmsDB(EnvCodeEnum.OM_TM03_ERROR_OUT.getValue());
        String moReportFlagTM3 = envMethod.getEnvFromOmsDB(EnvCodeEnum.OM_TM03_MO_REPORT_FLAG.getValue());//"ON"
        String bGetCarrierFlag = envMethod.getEnvFromOmsDB(EnvCodeEnum.OM_GET_CARRIER_FLAG.getValue());//"OFF"
        String strResourceType = envMethod.getEnvFromOmsDB(EnvCodeEnum.OM_MO_RESOURCE_TYPE.getValue());

        log.info("env OM_TM03_STATUS_FLAG: {}", statusRptFlagTM3);
        log.info("env OM_TM03_ERROR_OUT: {}", errorOutFlagTM3);
        log.info("env OM_TM03_MO_REPORT_FLAG: {}", moReportFlagTM3);
        log.info("env OM_GET_CARRIER_FLAG: {}", bGetCarrierFlag);
        log.info("env OM_MO_RESOURCE_TYPE: {}", strResourceType);

        String jobID = null;
        String carrierJobID = null;

        /*-----------------------------------------*/
        /* Extra Xfer Status Change report 0.01    */
        /*-----------------------------------------*/
        List<ObjectIdentifier> carrierIDs = new ArrayList<>();
        List<ObjectIdentifier> machineIDs = new ArrayList<>();
        List<ObjectIdentifier> portIDs = new ArrayList<>();
        List<ObjectIdentifier> carrierJobIDs = new ArrayList<>();
        Boolean reportFailFlag = false;
        //============================================================
        // Extra Xfer Status Change report
        //============================================================
        if (Constant.TM_ON.equals(statusRptFlagTM3)) {
            log.info("xferStatusChangeRpt by M3");
            //============================================================
            // For each job report
            //============================================================
            if (ArrayUtils.isNotEmpty(transportJobStatusReportParams.getJobStatusReportData())) {
                for (Infos.JobStatusReportArray jobStatusReportArray : transportJobStatusReportParams.getJobStatusReportData()) {
                    //============================================================
                    // Job Remove Flag == True !! or
                    // carrier job remove flag == True !!
                    //============================================================
                    if (BooleanUtils.isTrue(transportJobStatusReportParams.getJobRemoveFlag())
                            || BooleanUtils.isTrue(jobStatusReportArray.getCarrierJobRemoveFlag())) {
                        log.info("jobRemoveFlag == TRUE || carrierJobRemoveFlag == TRUE");
                        //============================================================
                        // Check toMachineID is equipment or not
                        //============================================================
                        ObjectIdentifier toMachineID = jobStatusReportArray.getToMachineID();
                        log.info("【step1】 - equipmentMethod.checkEqpTransfer");
                        Boolean eqpFlag = true;
                        try {
                            equipmentMethod.checkEqpTransfer(objCommon, toMachineID,false);
                        } catch (ServiceException e) {
                            if (!Validations.isEquals(msgRetCodeConfig.getMsgRecordNotFound(), e.getCode())) {
                                throw e;
                            }
                            log.info("checkEqp is record not found");
                            eqpFlag = false;
                        }
                        //============================================================
                        // toMachineID is equipment case
                        //============================================================
                        if (BooleanUtils.isTrue(eqpFlag)) {
                            log.info("checkEqp is record is OK");
                            continue;
                        } else {
                            //============================================================
                            // toMachineID is stocker case
                            //============================================================

                            //============================================================
                            // check recorded tranreq table original destination.
                            // If original destination is stocker && port ID is filled
                            //      ->it means "Not StockIn" but "StockOut" request was made
                            //          -> it means this report is equal to "Manual Out"
                            //============================================================
                            Boolean manualOutFlag = false;
                            if (Constant.TM_ON.equals(moReportFlagTM3)) {
                                log.info("moReportFlagTM3 = ON");
                                //============================================================
                                //Get FXPODTRNREQ by carrierID
                                //============================================================
                                List<Infos.TransferJobInfo> transferJobInfoList = null;
                                Boolean recordFoundFlag = false;
                                log.info("【step2】 - transferJobMethod.transferJobGet");
                                try {
                                    transferJobInfoList = transferJobMethod.transferJobGet(objCommon,
                                            Constant.TM_INQUIRY_TYPE_BY_CARRIER,
                                            Arrays.asList(jobStatusReportArray.getCarrierID()),
                                            null,
                                            null,
                                            null);
                                } catch (ServiceException e) {
                                    if (!Validations.isEquals(msgRetCodeConfig.getMsgRecordNotFound(), e.getCode())) {
                                        throw e;
                                    }
                                    recordFoundFlag = true;
                                }
                                if (BooleanUtils.isTrue(recordFoundFlag)) {
                                    log.info("transferJobGet != RC_RECORD_NOT_FOUND");
                                    //Check equal carrierJobID
                                    Boolean bFoundFlag = false;
                                    if (ArrayUtils.isNotEmpty(transferJobInfoList)) {
                                        for (Infos.TransferJobInfo transferJobInfo : transferJobInfoList) {
                                            log.info("transferJobGet carrierJobID: {}", transferJobInfo.getCarrierJobID());
                                            log.info("inputParam carrierJobID: {}", jobStatusReportArray.getCarrierJobID());
                                            if (StringUtils.equals(transferJobInfo.getCarrierJobID(), jobStatusReportArray.getCarrierJobID())) {
                                                log.info("Same carrierJobID Found !");
                                                bFoundFlag = true;
                                                break;
                                            }
                                        }
                                    }
                                    if (BooleanUtils.isTrue(bFoundFlag)) {
                                        log.info("carrierJobId is found");
                                        //============================================================
                                        //Check planned toMachineID (tranreq toMachineID) was STK or not
                                        //============================================================
                                        for (Infos.TransferJobInfo transferJobInfo : transferJobInfoList) {
                                            if (ObjectUtils.isNotEmptyWithValue(transferJobInfo.getToPortID())) {
                                                log.info("toPortID != NULL && toPortID != BLANK");
                                                //Check toMachineID is STK
                                                Boolean eqpCheckFlag = true;
                                                log.info("【step3】 - equipmentMethod.checkEqpTransfer");
                                                try {
                                                    equipmentMethod.checkEqpTransfer(objCommon, transferJobInfo.getToMachineID(),false);
                                                } catch (ServiceException e) {
                                                    //============================================================
                                                    // planned toMachineID was stocker !
                                                    //============================================================
                                                    if (Validations.isEquals(msgRetCodeConfig.getMsgRecordNotFound(), e.getCode())) {
                                                        if (ObjectUtils.isNotEmptyWithValue(transferJobInfo.getToPortID())){
                                                            //============================================================
                                                            // port id is not blank !
                                                            // xferStatus <- MO
                                                            //============================================================
                                                            manualOutFlag = true;
                                                        }
                                                    } else {
                                                        throw e;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            Boolean manualInFlag = false;
                            ObjectIdentifier reticlePodID = jobStatusReportArray.getCarrierID();
                            ObjectIdentifier machineID = jobStatusReportArray.getToMachineID();
                            ObjectIdentifier portID = null;
                            String xferStatus = null;
                            if (BooleanUtils.isTrue(manualOutFlag)){
                                log.info("ManualOutFlag = TRUE");
                                xferStatus = Constant.TM_TRANSSTATE_MANUALOUT;
                            }
                            log.info("【step4】 - omsManager.sendRSPXferStatusChangeRpt");
                            Results.RSPXferStatusChangeRptResult rspXferStatusChangeRptResult = null;
                            try {
                                 rspXferStatusChangeRptResult = omsManager.sendRSPXferStatusChangeRpt(objCommon,
                                        reticlePodID,
                                        xferStatus,
                                        manualInFlag,
                                        machineID,
                                        portID);
                            } catch (Exception e) {
                                if (Constant.TM_ON.equals(errorOutFlagTM3)){
                                    if (Constant.TM_ON.equals(errorOutFlagTM3)) {
                                        carrierIDs.add(jobStatusReportArray.getCarrierID());
                                        machineIDs.add(jobStatusReportArray.getToMachineID());
                                        portIDs.add(jobStatusReportArray.getToPortID());
                                        carrierJobIDs.add(ObjectIdentifier.buildWithValue(jobStatusReportArray.getCarrierJobID()));
                                    }
                                    reportFailFlag = true;
                                }
                            }
                        }
                    }
                }
            }
        }
        /*--------------------------------------------*/
        /* If jobRemoveFlag== TRUE,                   */
        /* then delete all record from OTXFERREQ       */
        /*--------------------------------------------*/
        if (BooleanUtils.isTrue(transportJobStatusReportParams.getJobRemoveFlag())) {
            log.info("jobRemoveFlag == TRUE");
            //============================================================
            // Check to_location,To equipment case
            // Set transferJobStatus = "XCMP"
            // sendReticlePodXferJobCompRpt to OMS
            //============================================================
            String transferJobStatus = Constant.TM_TRANSFER_JOB_STATUS_XCMP;
            if (Constant.TM_TRANSFER_JOB_STATUS_TWO.equals(transferJobStatus)){
                transferJobStatus = Constant.TM_TRANSFER_JOB_STATUS_XERR;
            }
            //============================================================
            // Prepare input parameter of check_EQP_SendCOMP_M3()
            //============================================================
            List<Infos.TransferJobInfo> seqTransferJobInfo = new ArrayList<>();
            if (ArrayUtils.isNotEmpty(transportJobStatusReportParams.getJobStatusReportData())) {
                for (Infos.JobStatusReportArray jobStatusReportArray : transportJobStatusReportParams.getJobStatusReportData()) {
                    carrierJobID = jobStatusReportArray.getCarrierJobID();
                    jobID = transportJobStatusReportParams.getJobID();
                    log.info("carrierJobID: {}", carrierJobID);
                    log.info("jobID: {}", jobID);
                    log.info("【step5】 - transferJobMethod.transferCarrierGet");
                    List<Infos.TransferJobInfo> transferJobInfoList = transferJobMethod.transferCarrierGet(objCommon, jobID, carrierJobID);
                    Infos.TransferJobInfo transferJobInfo = transferJobInfoList.get(0);
                    seqTransferJobInfo.add(transferJobInfo);
                    transferJobInfo.setJobID(transportJobStatusReportParams.getJobID());
                    transferJobInfo.setJobStatus(transportJobStatusReportParams.getJobStatus());
                    transferJobInfo.setCarrierID(jobStatusReportArray.getCarrierID());
                    transferJobInfo.setToMachineID(jobStatusReportArray.getToMachineID());
                    transferJobInfo.setCarrierJobID(jobStatusReportArray.getCarrierJobID());
                    transferJobInfo.setCarrierJobStatus(jobStatusReportArray.getCarrierjobStatus());
                    transferJobInfo.setToPortID(jobStatusReportArray.getToPortID());
                    /**************************************/
                    /* If MCS  can't send carrierID,      */
                    /* TMS read OTXFERREQ for getting it.  */
                    /**************************************/
                    if (Constant.TM_ON.equals(bGetCarrierFlag)) {
                        log.info("bGetCarrierFlag == ON");
                        transferJobInfo.setCarrierID(transferJobInfoList.get(0).getCarrierID());
                        transferJobInfo.setToMachineID(transferJobInfoList.get(0).getToMachineID());
                        //============================================================
                        // Check toMachineID is EqpID
                        //============================================================
                        /*-----------------------------------------*/                                                           //D3000129
                        /* Check toMachineID is EqpID              */                                                           //D3000129
                        /*-----------------------------------------*/
                        Boolean eqpFlag = true;
                        log.info("【step6】 - equipmentMethod.checkEqpTransfer");
                        try {
                            equipmentMethod.checkEqpTransfer(objCommon, transferJobInfo.getToMachineID(),false);
                        } catch (ServiceException e) {
                            eqpFlag = false;
                        }
                        if (BooleanUtils.isTrue(eqpFlag)) {
                            log.info("toMachine is Eqp");
                            transferJobInfo.setToPortID(transferJobInfoList.get(0).getToPortID());
                        }
                    }
                }
            }
            //============================================================
            // call checkEqpSendCOMPM3()
            //============================================================
            log.info("【step7】 - equipmentMethod.rtmsCheckEqpSendCOMPM3");
            equipmentMethod.rtmsCheckEqpSendCOMPM3(objCommon, seqTransferJobInfo, transferJobStatus);
            /*------------------------------*/
            /* Set DeleteType = 'JB'       */
            /* Delete records from OTXFERREQ */
            /*------------------------------*/
            Infos.TransferJobDeleteInfo transferJobDeleteInfo = new Infos.TransferJobDeleteInfo();
            transferJobDeleteInfo.setJobID(transportJobStatusReportParams.getJobID());
            log.info("【step8】 - transferJobMethod.transferJobDel");
            transferJobMethod.transferJobDel(objCommon,
                    Constant.TM_TRANSFER_JOB_DELETE_TYPE_JOBID,
                    transferJobDeleteInfo);

            /* Report XferJob status to OMS */
            log.info("【step9】 - omsManager.sendDurableXferStatusChangeRpt");
            omsManager.sendDurableXferStatusChangeRpt(objCommon,
                    seqTransferJobInfo,
                    Constant.TM_DURABLE_XFER_JOB_STATUS_COMPLETED,
                    "",false);
        } else {
            log.info("jobRemoveFlag != TRUE");
            /*--------------------------------------------------------------*/
            /* Delete or Update TrabsferData by Return Structure from MCS  */
            /*--------------------------------------------------------------*/
            List<Infos.TransferJobInfo> seqTransferJobInfo = new ArrayList<>();
            if (ArrayUtils.isNotEmpty(transportJobStatusReportParams.getJobStatusReportData())) {
                for (Infos.JobStatusReportArray jobStatusReportArray : transportJobStatusReportParams.getJobStatusReportData()) {
                    if (BooleanUtils.isTrue(jobStatusReportArray.getCarrierJobRemoveFlag())) {
                        log.info("carrierJobRemoveFlag == TRUE");
                        //============================================================
                        // set toMachineID and toPortID
                        //============================================================
                        carrierJobID = jobStatusReportArray.getCarrierJobID();
                        jobID = transportJobStatusReportParams.getJobID();
                        log.info("【step10】 - transferJobMethod.transferCarrierGet");
                        List<Infos.TransferJobInfo> transferJobInfoList = transferJobMethod.transferCarrierGet(objCommon,
                                jobID,
                                carrierJobID);

                        /**************************************/
                        /* If MCS can't send carrierID,       */
                        /* TMS read OTXFERREQ for getting it.  */
                        /**************************************/
                        String transferJobStatus = Constant.TM_TRANSFER_JOB_STATUS_XCMP;
                        Infos.TransferJobInfo transferJobInfo = transferJobInfoList.get(0);
                        seqTransferJobInfo.add(transferJobInfo);
                        transferJobInfo.setJobID(transportJobStatusReportParams.getJobID());
                        transferJobInfo.setCarrierJobID(jobStatusReportArray.getCarrierJobID());
                        transferJobInfo.setCarrierID(jobStatusReportArray.getCarrierID());
                        transferJobInfo.setToMachineID(jobStatusReportArray.getToMachineID());
                        transferJobInfo.setJobStatus(transportJobStatusReportParams.getJobStatus());
                        transferJobInfo.setCarrierJobStatus(jobStatusReportArray.getCarrierjobStatus());
                        if (Constant.TM_TRANSFER_JOB_STATUS_TWO.equals(transportJobStatusReportParams.getJobStatus())){
                            log.info("jobStatus = 2");
                            transferJobStatus = Constant.TM_TRANSFER_JOB_STATUS_XERR;
                        }
                        if (Constant.TM_ON.equals(bGetCarrierFlag)) {
                            log.info("bGetCarrierFlag = ON");
                            transferJobInfo.setCarrierID(transferJobInfoList.get(0).getCarrierID());
                            transferJobInfo.setToMachineID(transferJobInfoList.get(0).getToMachineID());

                        }
                        /*-----------------------------------------*/                                                  //D3000129
                        /* Check toMachineID is EqpID              */                                                  //D3000129
                        /*-----------------------------------------*/
                        Boolean eqpFlag = true;
                        log.info("【step11】 - equipmentMethod.checkEqpTransfer");
                        try {
                            equipmentMethod.checkEqpTransfer(objCommon, transferJobInfo.getToMachineID(),false);
                        } catch (ServiceException e) {
                            eqpFlag = false;
                            transferJobInfo.setToPortID(jobStatusReportArray.getToPortID());
                        }
                        //============================================================
                        // When toMachine is Equipment.
                        //============================================================
                        if (BooleanUtils.isTrue(eqpFlag)) {
                            log.info("toMachine is Eqp");
                            transferJobInfo.setToPortID(transferJobInfoList.get(0).getToPortID());
                        }
                        log.info("【step12】 - equipmentMethod.rtmsCheckEqpSendCOMPM3");
                        equipmentMethod.rtmsCheckEqpSendCOMPM3(objCommon,
                                seqTransferJobInfo,
                                transferJobStatus);

                    } else {
                        log.info("Get xferjob info");
                        log.info("【step13】 - transferJobMethod.transferCarrierGet");
                        List<Infos.TransferJobInfo> transferJobInfoList = transferJobMethod.transferCarrierGet(objCommon,
                                transportJobStatusReportParams.getJobID(),
                                jobStatusReportArray.getCarrierJobID());
                        seqTransferJobInfo.add(transferJobInfoList.get(0));

                        seqTransferJobInfo.get(0).setJobStatus(transportJobStatusReportParams.getJobStatus());
                        seqTransferJobInfo.get(0).setCarrierJobStatus(jobStatusReportArray.getCarrierjobStatus());
                    }
                    //============================================================
                    // Update Transfer Request Data
                    //============================================================
                    log.info("【step14】 - transferJobMethod.transferJobMod");
                    transferJobMethod.transferJobMod(objCommon,
                            transportJobStatusReportParams.getJobID(),
                            transportJobStatusReportParams.getJobStatus(),
                            jobStatusReportArray.getCarrierJobID(),
                            jobStatusReportArray.getCarrierID(),
                            jobStatusReportArray.getCarrierjobStatus(),
                            jobStatusReportArray.getCarrierJobRemoveFlag());

                    /* Report XferJob status to OMS */
                    String strXferJobStatus = null;
                    if (BooleanUtils.isTrue(jobStatusReportArray.getCarrierJobRemoveFlag())) {
                        strXferJobStatus = Constant.TM_DURABLE_XFER_JOB_STATUS_COMPLETED;
                    } else {
                        strXferJobStatus = Constant.TM_DURABLE_XFER_JOB_STATUS_UPDATED;
                    }

                    log.info("strXferJobStatus: {}", strXferJobStatus);
                    log.info("【step15】 - omsManager.sendDurableXferStatusChangeRpt");
                    omsManager.sendDurableXferStatusChangeRpt(objCommon,
                            seqTransferJobInfo,
                            strXferJobStatus,
                            "",false);

                }
            }
        }
        if (Constant.TM_ON.equals(statusRptFlagTM3) && Constant.TM_ON.equals(errorOutFlagTM3)) {
            log.info("statusRptFlagTM3 = ON & errorOutFlagTM3 = ON");
            if (ArrayUtils.isNotEmpty(machineIDs)) {
                for (int q = 0; q < ArrayUtils.getSize(machineIDs); q++) {
                    //============================================================
                    // Check toMachineID is EqpID
                    //============================================================
                    ObjectIdentifier currStockerID = machineIDs.get(q);
                    log.info("【step16】 - omsManager.sendStockerInfoInq");
                    Results.ReticlePodStockerInfoInqResult stockerInfoInqResult = null;
                    try {
                        stockerInfoInqResult = omsManager.sendReticlePodStockerInfoInq(objCommon,
                                currStockerID);
                    } catch (ServiceException e) {
                        continue;
                    }

                    List<Infos.AmhsResourceInfo> tmpResourceInfoData = new ArrayList<>();
                    ObjectIdentifier toPortID = null;
                    if (null != stockerInfoInqResult && ArrayUtils.isEmpty(stockerInfoInqResult.getResourceInfoData())) {
                        log.info("No resourceInfoData.");
                        continue;
                    }
                    tmpResourceInfoData = stockerInfoInqResult.getResourceInfoData();
                    //============================================================
                    // Look for "Manual Out" port
                    //============================================================
                    if (StringUtils.isEmpty(strResourceType)){
                        log.info("strResourceType is Null");
                        strResourceType = Constant.TM_RESOURCE_TYPE_MANUAL_OUT;
                    }
                    for (Infos.AmhsResourceInfo resourceInfo : stockerInfoInqResult.getResourceInfoData()) {
                        if (StringUtils.equals(resourceInfo.getResourceType(),strResourceType)) {
                            toPortID = ObjectIdentifier.buildWithValue(resourceInfo.getResourceID());
                            log.info("toPortID: {}", ObjectIdentifier.fetchValue(toPortID));
                            break;
                        }
                    }
                    /*-----------------------------------------------------------*/
                    /*   Send Transfer Data to MCS                               */
                    /*-----------------------------------------------------------*/
                    Params.TransportJobCreateReqParams tempTranJobCreateReq = new Params.TransportJobCreateReqParams();
                    tempTranJobCreateReq.setTransportType(Constant.TM_TRANSPORT_TYPE_S);
                    tempTranJobCreateReq.setJobID("");
                    tempTranJobCreateReq.setRerouteFlag(false);

                    log.info("Send S1 for Error Out!");
                    log.info("TranJobCreateReq.transportType: {}", tempTranJobCreateReq.getTransportType());

                    Infos.AmhsToDestination toDestination = new Infos.AmhsToDestination();
                    toDestination.setToPortID(toPortID);
                    List<Infos.AmhsToDestination> seqToMahchine = Arrays.asList(toDestination);

                    Infos.AmhsJobCreateArray jobCreateArray = new Infos.AmhsJobCreateArray();
                    jobCreateArray.setToMachine(seqToMahchine);
                    List<Infos.AmhsJobCreateArray> jobCreateArrayList = Arrays.asList(jobCreateArray);

                    tempTranJobCreateReq.setJobCreateData(jobCreateArrayList);

                    //=================================================//
                    // call sendReticlePodStatusInq                    //
                    //=================================================//
                    log.info("【step17】 - omsManager.sendReticlePodStatusInq");
                    Results.ReticlePodStatusInqResult reticlePodStatusInqResult = omsManager.sendReticlePodStatusInq(objCommon,
                            carrierIDs.get(q));
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
                    jobCreateArray.setCarrierJobID("");
                    jobCreateArray.setCarrierID(carrierIDs.get(q));
                    jobCreateArray.setFromMachineID(machineIDs.get(q));
                    jobCreateArray.setExpectedEndTime(Constant.TM_TRANSFER_JOB_EXPECTED_END_TIME);
                    jobCreateArray.setPriority(Constant.TM_TRANSFER_JOB_PRIORITY_NINETY_NINE);
                    jobCreateArray.setMandatoryFlag(false);
                    toDestination.setToMachineID(machineIDs.get(q));

                    if (ObjectUtils.isNotEmptyWithValue(toDestination.getToMachineID())){
                        log.info("toMachineID: {}",ObjectUtils.getObjectValue(toDestination.getToMachineID()));
                    }

                    log.info("【step18】 - mcsManager.sendTransportJobCreateReq");
                    Results.TransportJobCreateReqResult transportJobCreateReqResult = mcsManager.sendTransportJobCreateReq(objCommon, tempTranJobCreateReq);
                    Validations.check(null != transportJobCreateReqResult && ArrayUtils.getSize(transportJobCreateReqResult.getJobCreateResultSequenceData()) != 1, msgRetCodeConfig.getMsgMismatchInoutLength());
                    /*-----------------------------------------------------------*/
                    /* Insert & Update TrabsferData by Return Structure from MCS */
                    /*-----------------------------------------------------------*/
                    Infos.TransferJobInfo transferJobInfo = new Infos.TransferJobInfo();
                    transferJobInfo.setCarrierID(transportJobCreateReqResult.getJobCreateResultSequenceData().get(0).getCarrierID());
                    transferJobInfo.setJobID(transportJobCreateReqResult.getJobID());
                    transferJobInfo.setCarrierJobID(transportJobCreateReqResult.getJobCreateResultSequenceData().get(0).getCarrierJobID());
                    transferJobInfo.setTransportType(tempTranJobCreateReq.getTransportType());
                    //=================================================//
                    // call sendReticlePodStatusInq                    //
                    //=================================================//
                    log.info("【step18】 - omsManager.sendReticlePodStatusInq");
                    Results.ReticlePodStatusInqResult reticlePodStatusInqResult1 = omsManager.sendReticlePodStatusInq(objCommon,
                            carrierIDs.get(q));

                    //=================================================//
                    // Check ReticlePod is empty                       //
                    //=================================================//
                    if (null != reticlePodStatusInqResult1 && null != reticlePodStatusInqResult1.getReticlePodStatusInfo().getEmptyFlag()){
                        log.info("emptyFlag == TRUE");
                        jobCreateArray.setZoneType(Constant.TM_ZONE_TYPE_EMP);
                    }else {
                        log.info("emptyFlag != TRUE");
                        jobCreateArray.setZoneType(Constant.TM_ZONE_TYPE_RSP);
                    }


                    transferJobInfo.setN2PurgeFlag(tempTranJobCreateReq.getJobCreateData().get(0).getN2PurgeFlag());
                    transferJobInfo.setFromMachineID(tempTranJobCreateReq.getJobCreateData().get(0).getFromMachineID());
                    transferJobInfo.setFromPortID(tempTranJobCreateReq.getJobCreateData().get(0).getFromPortID());
                    transferJobInfo.setToStockerGroup(tempTranJobCreateReq.getJobCreateData().get(0).getToStockerGroup());
                    transferJobInfo.setToMachineID(transportJobCreateReqResult.getJobCreateResultSequenceData().get(0).getToMachineID());
                    transferJobInfo.setToPortID(ArrayUtils.isEmpty(tempTranJobCreateReq.getJobCreateData().get(0).getToMachine()) ? ObjectIdentifier.buildWithValue("") : tempTranJobCreateReq.getJobCreateData().get(0).getToMachine().get(0).getToPortID());
                    transferJobInfo.setExpectedStartTime(tempTranJobCreateReq.getJobCreateData().get(0).getExpectedStartTime());
                    transferJobInfo.setExpectedEndTime(tempTranJobCreateReq.getJobCreateData().get(0).getExpectedEndTime());
                    transferJobInfo.setEstimatedStartTime(transportJobCreateReqResult.getJobCreateResultSequenceData().get(0).getEstimatedStartTime());
                    transferJobInfo.setEstimatedEndTime(transportJobCreateReqResult.getJobCreateResultSequenceData().get(0).getEstimatedEndTime());
                    transferJobInfo.setMandatoryFlag(tempTranJobCreateReq.getJobCreateData().get(0).getMandatoryFlag());
                    transferJobInfo.setPriority(Constant.TM_TRANSFER_JOB_PRIORITY_NINETY_NINE);
                    transferJobInfo.setJobStatus("");
                    transferJobInfo.setCarrierJobStatus("");
                    transferJobInfo.setTimeStamp(DateUtils.getCurrentTimeStamp().toString());

                    log.info("【step19】 - transferJobMethod.transferJobPut(");
                    transferJobMethod.transferJobPut(objCommon, Arrays.asList(transferJobInfo));
                }
            }
        }
        result.setJobID(transportJobStatusReportParams.getJobID());
        Validations.check(BooleanUtils.isTrue(reportFailFlag), msgRetCodeConfig.getMsgReportFail());
        return result;
    }
}
