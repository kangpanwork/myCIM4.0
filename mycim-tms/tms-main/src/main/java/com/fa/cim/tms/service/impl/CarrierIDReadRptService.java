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
import com.fa.cim.tms.service.ICarrierIDReadRptService;
import com.fa.cim.tms.utils.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.*;

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
public class CarrierIDReadRptService implements ICarrierIDReadRptService {

    @Autowired
    private IOMSManager omsManager;
    @Autowired
    private IMCSManager mcsManager;
    @Autowired
    private MsgRetCodeConfig msgRetCodeConfig;
    @Autowired
    private IEnvMethod envMethod;
    @Autowired
    private ITransferJobMethod transferJobMethod;
    @Autowired
    private IEquipmentMethod equipmentMethod;

    public Results.CarrierIDReadReportResult sxCarrierIDReadReport(Infos.ObjCommon objCommon, Params.CarrierIDReadReportParmas carrierIDReadReportParmas) {
        Results.CarrierIDReadReportResult result = new Results.CarrierIDReadReportResult();
        result.setCarrierID(carrierIDReadReportParmas.getCarrierID());
        String switchQue = envMethod.getEnvFromOmsDB(EnvCodeEnum.OM_QUE_SWITCH.getValue());
        String errorOutFlag = envMethod.getEnvFromOmsDB(EnvCodeEnum.OM_TM06_ERROR_OUT.getValue());
        String errorRetFlag = envMethod.getEnvFromOmsDB(EnvCodeEnum.OM_TM06_ERROR_RETURN.getValue());
        String reticleXferFlag = envMethod.getEnvFromOmsDB(EnvCodeEnum.OM_RETICLE_LOGIC_AVAILABLE.getValue());
        log.info("env OM_QUE_SWITCH: {}", switchQue);
        log.info("env OM_TM06_ERROR_OUT: {}", errorOutFlag);
        log.info("env OM_TM06_ERROR_RETURN: {}", errorRetFlag);

        Boolean castTxNoSendWarning = false;

        if (BooleanUtils.isTrue(reticleXferFlag)) {
            reticleXferFlag = Constant.TM_YES;
        }
        log.info("env OM_RETICLE_LOGIC_AVAILABLE: {}", reticleXferFlag);

        String fromPortFlag = envMethod.getEnvFromOmsDB(EnvCodeEnum.OM_TM06_SEND_FROM_PORTID.getValue());
        String noFromMachineIDForSTKFlag = envMethod.getEnvFromOmsDB(EnvCodeEnum.OM_OM01_NO_FROM_MACHINEID_FOR_STK.getValue());
        log.info("env TMS_OM_TM06_SEND_FROM_PORTID: {}", fromPortFlag);
        log.info("env OM_OM01_NO_FROM_MACHINEID_FOR_STK: {}", noFromMachineIDForSTKFlag);

        /********************************/
        /* Set inquiryType = 'C'        */
        /* Get records from OTXFERREQ    */
        /********************************/
        String inquiryType = Constant.TM_INQUIRY_TYPE_BY_CARRIER;
        ObjectIdentifier toMachineID = null;
        ObjectIdentifier fromMachineID = null;
        List<String> seqJobID = null;
        List<ObjectIdentifier> seqCarrierID = Collections.singletonList(ObjectIdentifier.buildWithValue(ObjectIdentifier.fetchValue(carrierIDReadReportParmas.getCarrierID())));
        log.info("【step1】- transferJobMethod.transferJobGet");
        List<Infos.TransferJobInfo> transferJobGetOut = null;
        try {
            transferJobGetOut = transferJobMethod.transferJobGet(objCommon, inquiryType, seqCarrierID, toMachineID, fromMachineID, seqJobID);
        } catch (ServiceException e) {
            //do nothing
        }

        /*******************************************************************/
        /* Check to_location,it isn't for Equipment                        */
        /* If it's for Equipment,send TxLotCassetteXferJobCompRpt to MM    */
        /* Set transferJobStatus = "XERR"                                  */
        /*******************************************************************/
        String transferJobStatus = Constant.TM_TRANSFER_JOB_STATUS_XERR;
        log.info("【step2】- equipmentMethod.checkEqpSendCOMP");
        try {
            equipmentMethod.checkEqpSendCOMP(objCommon, transferJobGetOut, transferJobStatus);
        } catch (ServiceException e) {
            if (Validations.isEquals(msgRetCodeConfig.getMsgOmsCastTxNoSend(), e.getCode())) {
                log.error("oms cast no send or shutdown");
                castTxNoSendWarning = true;
            }else {
                throw e;
            }
        }

        /********************************/
        /* Set DeleteType = 'JB'        */
        /* Delete records from OTXFERREQ */
        /********************************/
        String deleteType = Constant.TM_TRANSFER_JOB_DELETE_TYPE_CARRIERJOBID;
        String jobDeleteFlag = envMethod.getEnvFromOmsDB(EnvCodeEnum.OM_TM06_JOB_DELETE.getValue());
        log.info("env OM_TM06_JOB_DELETE: {}", jobDeleteFlag);

        if (Constant.TM_TRANSFER_JOB_DELETE_TYPE_JOBID.equals(jobDeleteFlag)) {
            deleteType = Constant.TM_TRANSFER_JOB_DELETE_TYPE_JOBID;
        }

        if (ArrayUtils.isNotEmpty(transferJobGetOut)) {
            for (Infos.TransferJobInfo transferJobInfo : transferJobGetOut) {
                Infos.TransferJobDeleteInfo transferJobDeleteInfo = new Infos.TransferJobDeleteInfo();
                Infos.CarrierJobInfo carrierJobInfo = new Infos.CarrierJobInfo();
                transferJobDeleteInfo.setJobID(transferJobInfo.getJobID());
                carrierJobInfo.setCarrierID(transferJobInfo.getCarrierID());
                carrierJobInfo.setCarrierJobID(transferJobInfo.getCarrierJobID());
                transferJobDeleteInfo.setCarrierJobData(Collections.singletonList(carrierJobInfo));
                log.info("【step3】- transferJobMethod.transferJobDel");
                transferJobMethod.transferJobDel(objCommon, deleteType, transferJobDeleteInfo);

                /***************************************/
                /* Report to MM if any XferJob deleted */
                /***************************************/
                log.info("【step4】- omsManager.sendDurableXferStatusChangeRpt");
                omsManager.sendDurableXferStatusChangeRpt(objCommon, Collections.singletonList(transferJobInfo), Constant.TM_DURABLE_XFER_JOB_STATUS_CANCELLED, "",true);
            }
        }

        Boolean reticleFlag = false;
        Boolean abnormalStockFlag = false;
        /*-----------------------------------------------------------*/
        /*  Send Location Report to OMS                              */
        /*  If SP_RETICLE_XFER_AVAILABLE != "NO" (by D5100250) and   */
        /*  heading byte is equal to 'R',then it is Reticle event    */
        /*  In Reticle Case,carrierID=PodID(8byte)+ReticleID(20byte) */
        /*-----------------------------------------------------------*/
        Params.CarrierIDReadReportParmas tempCarrierIDReadReport = new Params.CarrierIDReadReportParmas();
        BeanUtils.copyProperties(carrierIDReadReportParmas, tempCarrierIDReadReport);

        if (Constant.TM_YES.equals(reticleXferFlag) && ObjectUtils.isNotEmptyWithValue(tempCarrierIDReadReport.getCarrierID()) && tempCarrierIDReadReport.getCarrierID().getValue().startsWith(Constant.TM_STRING_R)) {
            // TODO: 2020/10/19 Confrim the Reticle/ReticlePod must be the first is 'R'?
            reticleFlag = true;
            /*** ReticlePod****/
            String reticlePodIDOnly = envMethod.getEnvFromOmsDB(EnvCodeEnum.OM_RETICLE_POD_ONLY.getValue()); //No Use
            ObjectIdentifier stockekrID = tempCarrierIDReadReport.getMachineID();
            Infos.XferReticlePod xferReticlePod = new Infos.XferReticlePod();
            xferReticlePod.setTransferStatus(Constant.TM_TRANSSTATE_MANUALIN);
            if (Constant.TM_YES.equals(reticlePodIDOnly)) {
                if (StringUtils.length(carrierIDReadReportParmas.getCarrierID().getValue()) < 65) {
                    xferReticlePod.setReticlePodID(ObjectIdentifier.buildWithValue(ObjectIdentifier.fetchValue(carrierIDReadReportParmas.getCarrierID())));
                }
            } else {
                xferReticlePod.setReticlePodID(ObjectIdentifier.buildWithValue(carrierIDReadReportParmas.getCarrierID().getValue().substring(0, 8)));
            }
            List<Infos.XferReticlePod> xferReticlePodList = Collections.singletonList(xferReticlePod);
            log.info("stockerID: {}", ObjectIdentifier.fetchValue(stockekrID));
            log.info("reticlePodID: {}", ObjectIdentifier.fetchValue(xferReticlePodList.get(0).getReticlePodID()));
            log.info("transferStatus: {}", xferReticlePodList.get(0).getTransferStatus());

            log.info("【step5】- omsManager.sendReticlePodTransferStatusChangeRpt");
            Results.ReticlePodTransferStatusChangeRptResult reticlePodTransferStatusChangeRptResult = omsManager.sendReticlePodTransferStatusChangeRpt(objCommon, stockekrID,
                    null, xferReticlePodList);

        } else {
            log.info("carrierID[0] != 'R'");
            log.info("【step6】- omsManager.sendCarrierTransferStatusChangeRpt");
            try {
                omsManager.sendCarrierTransferStatusChangeRpt(objCommon, carrierIDReadReportParmas.getCarrierID(),
                        Constant.TM_TRANSSTATE_MANUALIN, true, carrierIDReadReportParmas.getMachineID(), carrierIDReadReportParmas.getPortID(), Constant.TM_STRING_DEFAULT, "", "", "");
            } catch (ServiceException e) {
                if (Validations.isEquals(msgRetCodeConfig.getMsgOmsCastTxNoSend(), e.getCode())) {
                    log.error("oms cast no send or shutdown");
                    castTxNoSendWarning = true;
                } else {
                    if (!Constant.TM_ON.equals(errorOutFlag)) {
                        throw e;
                    }
                    abnormalStockFlag = true;
                }
            }
        }

        String deliveryReqOn = envMethod.getEnvFromOmsDB(EnvCodeEnum.OM_DELIVERY_REQ_ON.getValue());//may be we need change to NO we can join whereNext
        if (!Constant.TM_YES.equals(deliveryReqOn) && BooleanUtils.isFalse(reticleFlag) && BooleanUtils.isTrue(carrierIDReadReportParmas.getJobRequestFlag())) {
            log.info("env TMS_OM_DELIVERY_REQ_ON = NO");
            log.info("【step7】- omsManager.sendWhereNextInterBay");
            Results.WhereNextInterBayResult whereNextInterBayResult = null;
            try {
                whereNextInterBayResult = omsManager.sendWhereNextInterBay(objCommon, null, carrierIDReadReportParmas.getCarrierID());
            } catch (Exception e) {
                if (e instanceof ServiceException){
                    if (Validations.isEquals(msgRetCodeConfig.getMsgInvalidLotProcstat(), ((ServiceException)e).getCode())) {
                        abnormalStockFlag = true;
                    } else if (!Validations.isEquals(msgRetCodeConfig.getMsgLotNotFoundOM(), ((ServiceException)e).getCode())) {
                        if (!Constant.TM_ON.equals(errorOutFlag)) {
                            if (Constant.TM_ON.equals(errorRetFlag)) {
                                throw e;
                            }
                        }else {
                            if (!Constant.TM_ON.equals(switchQue)) {
                                abnormalStockFlag = true;
                            }
                        }
                    }
                }else {
                    if (!Constant.TM_ON.equals(errorOutFlag)) {
                        if (Constant.TM_ON.equals(errorRetFlag)) {
                            throw e;
                        }
                    }else {
                        if (!Constant.TM_ON.equals(switchQue)) {
                            abnormalStockFlag = true;
                        }
                    }
                }
            }
            if (null != whereNextInterBayResult && Constant.TM_LOT_STATE_SHIPPED.equals(whereNextInterBayResult.getLotState())) {
                log.info("log state is SHIPPED");
                abnormalStockFlag = true;
            }

            if (BooleanUtils.isFalse(abnormalStockFlag)) {
                /*-----------------------------------------------------------*/
                /*   Send Transfer Data to MCS                               */
                /*-----------------------------------------------------------*/
                Params.TransportJobCreateReqParams tempTranJobCreateReq = new Params.TransportJobCreateReqParams();
                tempTranJobCreateReq.setTransportType(Constant.TM_TRANSPORT_TYPE_S);
                List<ObjectIdentifier> tempMachineIDs = new ArrayList<>();
                if (null != whereNextInterBayResult){
                    Optional.ofNullable(whereNextInterBayResult.getWhereNextEqpStatus()).ifPresent(list -> list.forEach(data -> {
                        Optional.ofNullable(data.getEqpStockerStatus()).ifPresent(eqpStockerStatuses -> eqpStockerStatuses.forEach(eqpStockerStatus -> {
                            tempMachineIDs.add(eqpStockerStatus.getStockerID());
                        }));
                    }));
                }
                ArrayUtils.removeDuplicate(tempMachineIDs);
                List<ObjectIdentifier> machineIDs = new ArrayList<>(tempMachineIDs);
                log.info("Next Socker Count: {}", ArrayUtils.getSize(machineIDs));
                //Copy machineIDs except duplicated IDs 0.04
                List<Infos.AmhsJobCreateArray> jobCreateData = new ArrayList<>();
                tempTranJobCreateReq.setJobCreateData(jobCreateData);
                Infos.AmhsJobCreateArray amhsJobCreateArray = new Infos.AmhsJobCreateArray();
                jobCreateData.add(amhsJobCreateArray);
                List<Infos.AmhsToDestination> toMachine = new ArrayList<>();
                amhsJobCreateArray.setToMachine(toMachine);
                //if machineIDs is not empty
                if (ArrayUtils.isNotEmpty(machineIDs)) {
                    log.info("WhereNext Returned has Data!");
                    machineIDs.forEach(machineID -> {
                        Infos.AmhsToDestination amhsToDestination = new Infos.AmhsToDestination();
                        amhsToDestination.setToMachineID(machineID);
                        toMachine.add(amhsToDestination);
                    });
                } else {
                    log.info("WhereNext Returned No Data!");
                    Infos.AmhsToDestination amhsToDestination = new Infos.AmhsToDestination();
                    amhsToDestination.setToMachineID(carrierIDReadReportParmas.getMachineID());
                    toMachine.add(amhsToDestination);
                }

                /*-----------------------------------------------------------*/
                /*   Send sendCassetteStatusInq to OMS                       */
                /*-----------------------------------------------------------*/
                log.info("【step8】- omsManager.sendCassetteStatusInq");
                Results.CarrierDetailInfoInqResult cassetteStatusInqResult = null;
                try {
                    cassetteStatusInqResult = omsManager.sendCassetteStatusInq(objCommon, carrierIDReadReportParmas.getCarrierID());
                    amhsJobCreateArray.setZoneType(cassetteStatusInqResult.getCassetteStatusInfo().getZoneType());
                    amhsJobCreateArray.setPriority(cassetteStatusInqResult.getCassetteStatusInfo().getPriority());
                } catch (Exception e) {
                    //doNothind
                }

                if (StringUtils.isEmpty(amhsJobCreateArray.getZoneType())) {
                    amhsJobCreateArray.setZoneType(Constant.TM_STRING_DEFAULT);
                }
                if (StringUtils.isEmpty(amhsJobCreateArray.getPriority())) {
                    amhsJobCreateArray.setPriority(Constant.TM_TRANSFER_JOB_PRIORITY_FOUR);
                }
                amhsJobCreateArray.setCarrierID(tempCarrierIDReadReport.getCarrierID());

                if (Constant.TM_ON.equals(fromPortFlag)) {
                    amhsJobCreateArray.setFromMachineID(tempCarrierIDReadReport.getMachineID());
                    amhsJobCreateArray.setFromPortID(tempCarrierIDReadReport.getPortID());
                } else if (!Constant.TM_ON.equals(noFromMachineIDForSTKFlag)) {
                    amhsJobCreateArray.setFromMachineID(tempCarrierIDReadReport.getMachineID());
                }

                amhsJobCreateArray.setExpectedEndTime(Constant.TM_TRANSFER_JOB_EXPECTED_END_TIME);
                amhsJobCreateArray.setMandatoryFlag(false);
                tempTranJobCreateReq.setRerouteFlag(false);

                log.info("【step9】- mcsManager.sendTransportJobCreateReq");
                Results.TransportJobCreateReqResult transportJobCreateReqResult = null;
                try {
                    transportJobCreateReqResult = mcsManager.sendTransportJobCreateReq(objCommon, tempTranJobCreateReq);
                    Validations.check(ArrayUtils.getSize(transportJobCreateReqResult.getJobCreateResultSequenceData()) != 1, msgRetCodeConfig.getMsgMismatchInoutLength());
                } catch (ServiceException e) {
                    //Integration error
                    if (Validations.isEquals(e.getCode(),msgRetCodeConfig.getMsgGrpcTimeOut())) throw e;
                    //MCS Return error case
                    if (Validations.isEquals(e.getCode(), msgRetCodeConfig.getMsgUnknownJobid()) || Validations.isEquals(e.getCode(), msgRetCodeConfig.getMsgDifferrentJobID())) {
                        log.info("MCS Return is RC_UNKNOWN_JOBID or RC_DIFFERRENT_JOBID");
                        Validations.check(ArrayUtils.getSize(transportJobCreateReqResult.getJobCreateResultSequenceData()) != 1, msgRetCodeConfig.getMsgMismatchInoutLength());
                        if (Constant.TM_TRANSPORT_TYPE_S.equals(tempTranJobCreateReq.getTransportType())) {
                            log.info("transportType  == 'S'");
                            Validations.check(ArrayUtils.getSize(transportJobCreateReqResult.getJobCreateResultSequenceData()) != 1, msgRetCodeConfig.getMsgNeedSingleData());
                            /* Inquiry JobID to MCS by inquiryType = 'C'*/
                            Params.TransportJobInqParams transportJobInq = new Params.TransportJobInqParams();
                            transportJobInq.setInquiryType(Constant.TM_INQUIRY_TYPE_BY_CARRIER);
                            transportJobInq.setCarrierID(tempTranJobCreateReq.getJobCreateData().get(0).getCarrierID());
                            Results.TransportJobInqResult transportJobInqResult = null;
                            log.info("【step10】- mcsManager.sendTransportJobInq");
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
                            log.info("【step11】- mcsManager.sendTransportJobCreateReq");
                            Results.TransportJobCreateReqResult transportJobCreateReqResultExcption = mcsManager.sendTransportJobCreateReq(objCommon, tempTranJobCreateReq);
                        } else {
                            log.info("transportType != 'S'");
                        }
                    } else {
                        /********************************/
                        /* Add Retry to current stocker */
                        /********************************/
                        log.info("Retry to store into current stocker!");
                        Infos.AmhsToDestination toDestinationToSTK = new Infos.AmhsToDestination();
                        toDestinationToSTK.setToMachineID(tempCarrierIDReadReport.getMachineID());
                        tempTranJobCreateReq.getJobCreateData().get(0).setToMachine(Collections.singletonList(toDestinationToSTK));
                        log.info("【step12】- mcsManager.sendTransportJobCreateReq");
                        Results.TransportJobCreateReqResult transportJobCreateReqResultCurrentSTK = mcsManager.sendTransportJobCreateReq(objCommon, tempTranJobCreateReq);
                    }
                }
                /*-----------------------------------------------------------*/
                /* Insert & Update TrabsferData by Return Structure from MCS*/
                /*-----------------------------------------------------------*/
                log.info("Insert & Update TrabsferData by Return Structure from MCS");
                Infos.TransferJobInfo transferJobInfo = new Infos.TransferJobInfo();
                transferJobInfo.setCarrierID(carrierIDReadReportParmas.getCarrierID());
                transferJobInfo.setJobID(transportJobCreateReqResult.getJobID());
                transferJobInfo.setCarrierJobID(transportJobCreateReqResult.getJobCreateResultSequenceData().get(0).getCarrierJobID());
                transferJobInfo.setTransportType(tempTranJobCreateReq.getTransportType());
                transferJobInfo.setZoneType(Constant.TM_STRING_DEFAULT);
                transferJobInfo.setN2PurgeFlag(tempTranJobCreateReq.getJobCreateData().get(0).getN2PurgeFlag());
                transferJobInfo.setFromMachineID(tempTranJobCreateReq.getJobCreateData().get(0).getFromMachineID());
                transferJobInfo.setFromPortID(tempTranJobCreateReq.getJobCreateData().get(0).getFromPortID());
                transferJobInfo.setToStockerGroup(tempTranJobCreateReq.getJobCreateData().get(0).getToStockerGroup());
                transferJobInfo.setToMachineID(transportJobCreateReqResult.getJobCreateResultSequenceData().get(0).getToMachineID());
                transferJobInfo.setToPortID(ObjectIdentifier.buildWithValue(tempTranJobCreateReq.getJobCreateData().get(0).getToMachine().size() <= 0 ? "" : ObjectIdentifier.fetchValue(tempTranJobCreateReq.
                        getJobCreateData().get(0).getToMachine().get(0).getToPortID())));
                transferJobInfo.setExpectedStartTime(tempTranJobCreateReq.getJobCreateData().get(0).getExpectedStartTime());
                transferJobInfo.setExpectedEndTime(tempTranJobCreateReq.getJobCreateData().get(0).getExpectedEndTime());
                transferJobInfo.setEstimatedStartTime(transportJobCreateReqResult.getJobCreateResultSequenceData().get(0).getEstimatedStartTime());
                transferJobInfo.setEstimatedEndTime(transportJobCreateReqResult.getJobCreateResultSequenceData().get(0).getEstimatedEndTime());
                transferJobInfo.setMandatoryFlag(tempTranJobCreateReq.getJobCreateData().get(0).getMandatoryFlag());
                transferJobInfo.setPriority(tempTranJobCreateReq.getJobCreateData().get(0).getPriority());
                transferJobInfo.setJobStatus("");
                transferJobInfo.setCarrierJobStatus("");
                transferJobInfo.setTimeStamp(new Timestamp(System.currentTimeMillis()).toString());
                log.info("【step13】- transferJobMethod.transferJobPut");
                transferJobMethod.transferJobPut(objCommon, Collections.singletonList(transferJobInfo));
            }
        } else {
            log.info("deliveryReqOn = YES");
        }
        if (BooleanUtils.isTrue(abnormalStockFlag)) {
            log.info("abnormalStockFlag == TRUE");
            if (Constant.TM_ON.equals(errorOutFlag)) {
                log.info("errorOutFlag is ON");
                ObjectIdentifier currStockerID = ObjectIdentifier.buildWithValue(ObjectIdentifier.fetchValue(tempCarrierIDReadReport.getMachineID()));
                ObjectIdentifier toPortID = null;

                Boolean detailFlag = false;
                log.info("【step14】- omsManager.sendStockerInfoInq");
                Results.StockerInfoInqResult stockerInfoInqResult = omsManager.sendStockerInfoInq(objCommon, currStockerID, detailFlag);
                if (null != stockerInfoInqResult && ArrayUtils.isNotEmpty(stockerInfoInqResult.getResourceInfoData())) {
                    for (Results.ResourceInfo data : stockerInfoInqResult.getResourceInfoData()) {
                        if (Constant.TM_RESOURCE_TYPE_MANUAL.equals(data.getResourceID())
                                && (data.getResourceID().startsWith(Constant.TM_STRING_O)
                                || data.getResourceID().startsWith(Constant.TM_STRING_W))) {
                            toPortID = ObjectIdentifier.buildWithValue(data.getResourceID());
                            break;
                        }
                    }
                }
                /*-----------------------------------------------------------*/
                /*   Send Transfer Data to MCS                              */
                /*-----------------------------------------------------------*/
                log.info("【step11】- sendStockerInfoInq");
                Params.TransportJobCreateReqParams tempTranJobCreateReq = new Params.TransportJobCreateReqParams();
                tempTranJobCreateReq.setTransportType(Constant.TM_TRANSPORT_TYPE_S);
                List<Infos.AmhsToDestination> seqToMachine = new ArrayList<>();
                Infos.AmhsToDestination amhsToDestination = new Infos.AmhsToDestination();
                amhsToDestination.setToPortID(toPortID);
                amhsToDestination.setToMachineID(tempCarrierIDReadReport.getMachineID());
                seqToMachine.add(amhsToDestination);
                Infos.AmhsJobCreateArray amhsJobCreateArray = new Infos.AmhsJobCreateArray();
                amhsJobCreateArray.setToMachine(seqToMachine);
                amhsJobCreateArray.setZoneType(Constant.TM_STRING_DEFAULT);
                amhsJobCreateArray.setCarrierID(tempCarrierIDReadReport.getCarrierID());

                if (Constant.TM_ON.equals(fromPortFlag)) {
                    amhsJobCreateArray.setFromMachineID(tempCarrierIDReadReport.getMachineID());
                    amhsJobCreateArray.setFromPortID(tempCarrierIDReadReport.getPortID());
                } else if (!Constant.TM_ON.equals(noFromMachineIDForSTKFlag)) {
                    amhsJobCreateArray.setFromMachineID(tempCarrierIDReadReport.getMachineID());
                }
                amhsJobCreateArray.setExpectedEndTime(Constant.TM_TRANSFER_JOB_EXPECTED_END_TIME);
                amhsJobCreateArray.setPriority(Constant.TM_TRANSFER_JOB_PRIORITY_FOUR);
                amhsJobCreateArray.setMandatoryFlag(false);
                tempTranJobCreateReq.setRerouteFlag(false);
                tempTranJobCreateReq.setJobCreateData(Collections.singletonList(amhsJobCreateArray));
                log.info("【step15】- mcsManager.sendTransportJobCreateReq");
                Results.TransportJobCreateReqResult transportJobCreateReqResult = mcsManager.sendTransportJobCreateReq(objCommon, tempTranJobCreateReq);
                /*-----------------------------------------------------------*/
                /* Insert & Update TrabsferData by Return Structure from MCS */
                /*-----------------------------------------------------------*/
                Validations.check(ArrayUtils.getSize(transportJobCreateReqResult.getJobCreateResultSequenceData()) != 1, msgRetCodeConfig.getMsgMismatchInoutLength());
                log.info("Insert & Update TrabsferData by Return Structure from MCS");
                Infos.TransferJobInfo transferJobInfo = new Infos.TransferJobInfo();
                transferJobInfo.setCarrierID(carrierIDReadReportParmas.getCarrierID());
                transferJobInfo.setJobID(transportJobCreateReqResult.getJobID());
                transferJobInfo.setCarrierJobID(transportJobCreateReqResult.getJobCreateResultSequenceData().get(0).getCarrierJobID());
                transferJobInfo.setTransportType(tempTranJobCreateReq.getTransportType());
                transferJobInfo.setZoneType(Constant.TM_STRING_DEFAULT);
                transferJobInfo.setN2PurgeFlag(tempTranJobCreateReq.getJobCreateData().get(0).getN2PurgeFlag());
                transferJobInfo.setFromMachineID(tempTranJobCreateReq.getJobCreateData().get(0).getFromMachineID());
                transferJobInfo.setFromPortID(tempTranJobCreateReq.getJobCreateData().get(0).getFromPortID());
                transferJobInfo.setToStockerGroup(tempTranJobCreateReq.getJobCreateData().get(0).getToStockerGroup());
                transferJobInfo.setToMachineID(transportJobCreateReqResult.getJobCreateResultSequenceData().get(0).getToMachineID());
                transferJobInfo.setToPortID(ObjectIdentifier.buildWithValue(tempTranJobCreateReq.getJobCreateData().get(0).getToMachine().size() <= 0 ? "" : ObjectIdentifier.fetchValue(tempTranJobCreateReq.
                        getJobCreateData().get(0).getToMachine().get(0).getToPortID())));
                transferJobInfo.setExpectedStartTime(tempTranJobCreateReq.getJobCreateData().get(0).getExpectedStartTime());
                transferJobInfo.setExpectedEndTime(tempTranJobCreateReq.getJobCreateData().get(0).getExpectedEndTime());
                transferJobInfo.setEstimatedStartTime(transportJobCreateReqResult.getJobCreateResultSequenceData().get(0).getEstimatedStartTime());
                transferJobInfo.setEstimatedEndTime(transportJobCreateReqResult.getJobCreateResultSequenceData().get(0).getEstimatedEndTime());
                transferJobInfo.setMandatoryFlag(tempTranJobCreateReq.getJobCreateData().get(0).getMandatoryFlag());
                transferJobInfo.setPriority(tempTranJobCreateReq.getJobCreateData().get(0).getPriority());
                transferJobInfo.setJobStatus("");
                transferJobInfo.setCarrierJobStatus("");
                transferJobInfo.setTimeStamp(new Timestamp(System.currentTimeMillis()).toString());
                log.info("【step16】- transferJobMethod.transferJobPut");
                transferJobMethod.transferJobPut(objCommon, Collections.singletonList(transferJobInfo));
            } else {
                log.info("errorOutFlag != ON");
            }
            Validations.check(true,msgRetCodeConfig.getMsgAbnormalStock(),
                    ObjectIdentifier.fetchValue(result.getCarrierID()));
        }
        Validations.check(BooleanUtils.isTrue(castTxNoSendWarning),result,msgRetCodeConfig.getMsgOmsCastTxNoSend());
        return result;
    }


    @Override
    public Results.CarrierIDReadReportResult sxRtmsCarrierIDReadReport(Infos.ObjCommon objCommon, Params.CarrierIDReadReportParmas carrierIDReadReportParmas) {
        Results.CarrierIDReadReportResult result = new Results.CarrierIDReadReportResult();
        result.setCarrierID(carrierIDReadReportParmas.getCarrierID());
        String switchQue = envMethod.getEnvFromOmsDB(EnvCodeEnum.OM_QUE_SWITCH.getValue());
        String errorOutFlag = envMethod.getEnvFromOmsDB(EnvCodeEnum.OM_TM06_ERROR_OUT.getValue());
        String strResourceType = envMethod.getEnvFromOmsDB(EnvCodeEnum.OM_MO_RESOURCE_TYPE.getValue());
        log.info("env OM_QUE_SWITCH: {}", switchQue);
        log.info("env OM_TM06_ERROR_OUT: {}", errorOutFlag);
        log.info("env OM_MO_RESOURCE_TYPE: {}", strResourceType);

        /********************************/
        /* Set inquiryType = 'C'        */
        /* Get records from OTXFERREQ    */
        /********************************/
        String inquiryType = Constant.TM_INQUIRY_TYPE_BY_CARRIER;
        ObjectIdentifier toMachineID = null;
        ObjectIdentifier fromMachineID = null;
        List<String> seqJobID = null;
        List<ObjectIdentifier> seqCarrierID = Collections.singletonList(ObjectIdentifier.buildWithValue(ObjectIdentifier.fetchValue(carrierIDReadReportParmas.getCarrierID())));
        log.info("【step1】- transferJobMethod.transferJobGet");
        List<Infos.TransferJobInfo> transferJobGetOut = null;
        try {
            transferJobGetOut = transferJobMethod.transferJobGet(objCommon, inquiryType, seqCarrierID, toMachineID, fromMachineID, seqJobID);
        } catch (ServiceException e) {
            //do nothing
        }

        /*******************************************************************/
        /* Check to_location,it isn't for Equipment                        */
        /* If it's for Equipment,send TxLotCassetteXferJobCompRpt to MM    */
        /* Set transferJobStatus = "XERR"                                  */
        /*******************************************************************/
        String transferJobStatus = Constant.TM_TRANSFER_JOB_STATUS_XERR;
        log.info("【step2】- equipmentMethod.rtmsCheckEqpSendCOMP");
        try {
            equipmentMethod.rtmsCheckEqpSendCOMP(objCommon, transferJobGetOut, transferJobStatus);
        } catch (ServiceException e) {
            log.error("equipmentMethod.rtmsCheckEqpSendCOMP is not OK");
        }


        /********************************/
        /* Set DeleteType = 'JB'        */
        /* Delete records from OTXFERREQ */
        /********************************/
        if (ArrayUtils.isNotEmpty(transferJobGetOut)) {
            for (Infos.TransferJobInfo transferJobInfo : transferJobGetOut) {
                String deleteType = null;
                String jobDeleteFlag = envMethod.getEnvFromOmsDB(EnvCodeEnum.OM_TM06_JOB_DELETE.getValue());
                log.info("env OM_TM06_JOB_DELETE: {}", jobDeleteFlag);

                if (Constant.TM_TRANSFER_JOB_DELETE_TYPE_JOBID.equals(jobDeleteFlag)) {
                    deleteType = Constant.TM_TRANSFER_JOB_DELETE_TYPE_JOBID;
                } else {
                    deleteType = Constant.TM_TRANSFER_JOB_DELETE_TYPE_CARRIERJOBID;
                }
                Infos.TransferJobDeleteInfo transferJobDeleteInfo = new Infos.TransferJobDeleteInfo();
                Infos.CarrierJobInfo carrierJobInfo = new Infos.CarrierJobInfo();
                transferJobDeleteInfo.setJobID(transferJobInfo.getJobID());
                carrierJobInfo.setCarrierID(transferJobInfo.getCarrierID());
                carrierJobInfo.setCarrierJobID(transferJobInfo.getCarrierJobID());
                transferJobDeleteInfo.setCarrierJobData(Collections.singletonList(carrierJobInfo));
                log.info("【step3】- transferJobMethod.transferJobDel");
                try {
                    transferJobMethod.transferJobDel(objCommon, deleteType, transferJobDeleteInfo);
                } catch (ServiceException e) {
                    log.error("transferJobMethod.transferJobDel is not OK");
                }

                /***************************************/
                /* Report to OMS if any XferJob deleted*/
                /***************************************/
                log.info("【step4】- omsManager.sendDurableXferStatusChangeRpt");
                omsManager.sendDurableXferStatusChangeRpt(objCommon, Collections.singletonList(transferJobInfo), Constant.TM_DURABLE_XFER_JOB_STATUS_CANCELLED, "",false);
            }
        }

        //==========================================================
        //  Send Location Report to OMS
        //==========================================================
        ObjectIdentifier reticlePodID = carrierIDReadReportParmas.getCarrierID();
        String xferStatus = Constant.TM_TRANSSTATE_MANUALIN;
        Boolean manualInFlag = true;
        ObjectIdentifier portID = null;
        ObjectIdentifier machineID = carrierIDReadReportParmas.getMachineID();
        log.info("machineID: {}", ObjectIdentifier.fetchValue(machineID));
        log.info("reticlePodID: {}", ObjectIdentifier.fetchValue(reticlePodID));
        log.info("xferStatus: {}", xferStatus);
        Boolean abnormalStockFlag = false;
        log.info("【step5】- omsManager.sendRSPXferStatusChangeRpt");
        try {
            Results.RSPXferStatusChangeRptResult sendRSPXferStatusChangeRptResult = omsManager.sendRSPXferStatusChangeRpt(objCommon,
                    reticlePodID,
                    xferStatus,
                    manualInFlag,
                    machineID,
                    portID);
        } catch (ServiceException e) {
            if (!Constant.TM_ON.equals(errorOutFlag)) {
                log.info("errorOutFlag = ON");
                throw e;
            }
            log.info("abnormalStockFlag = TRUE");
            abnormalStockFlag = true;
        }

        String zoneType = "";
        if (BooleanUtils.isTrue(carrierIDReadReportParmas.getJobRequestFlag())) {
            log.info("carrierIDReadReportParmas.getJobRequestFlag() = TRUE");
            //==========================================================
            //Abnormal Stock
            //==========================================================
            if (BooleanUtils.isFalse(abnormalStockFlag)) {
                log.info("AbnormalStockFlag != TRUE ");
                //==========================================================
                //   Send Transfer Data to MCS
                //==========================================================
                Params.TransportJobCreateReqParams tempTranJobCreateReq = new Params.TransportJobCreateReqParams();
                tempTranJobCreateReq.setTransportType(Constant.TM_TRANSPORT_TYPE_S);

                //=================================================//
                // Check ReticlePod is empty                       //
                //=================================================//
                Results.ReticlePodStatusInqResult reticlePodStatusInqResult = null;
                log.info("【step6】- mcsManager.sendReticlePodStatusInq");
                try {
                    reticlePodStatusInqResult = omsManager.sendReticlePodStatusInq(objCommon,
                            carrierIDReadReportParmas.getCarrierID());
                } catch (Exception e) {
                    zoneType = Constant.TM_ZONE_TYPE_UNKNOWN;
                }
                if (null != reticlePodStatusInqResult && BooleanUtils.isTrue(reticlePodStatusInqResult.getReticlePodStatusInfo().getEmptyFlag())) {
                    log.info("emptyFlag == TRUE ");
                    zoneType = Constant.TM_ZONE_TYPE_EMP;
                } else {
                    log.info("emptyFlag != TRUE ");
                    zoneType = Constant.TM_ZONE_TYPE_RSP;
                }
                Infos.AmhsJobCreateArray amhsJobCreateArray = new Infos.AmhsJobCreateArray();
                tempTranJobCreateReq.setJobCreateData(Collections.singletonList(amhsJobCreateArray));

                amhsJobCreateArray.setZoneType(zoneType);
                amhsJobCreateArray.setPriority(Constant.TM_TRANSFER_JOB_PRIORITY_NINETY_NINE);
                amhsJobCreateArray.setCarrierID(carrierIDReadReportParmas.getCarrierID());
                amhsJobCreateArray.setFromMachineID(carrierIDReadReportParmas.getMachineID());
                amhsJobCreateArray.setFromPortID(carrierIDReadReportParmas.getPortID());

                List<Infos.AmhsToDestination> toMathines = new ArrayList<>();
                Infos.AmhsToDestination toDestination = new Infos.AmhsToDestination();
                toDestination.setToMachineID(carrierIDReadReportParmas.getMachineID());

                toMathines.add(toDestination);
                amhsJobCreateArray.setToMachine(toMathines);
                amhsJobCreateArray.setExpectedEndTime("");
                amhsJobCreateArray.setMandatoryFlag(false);

                tempTranJobCreateReq.setRerouteFlag(false);
                Results.TransportJobCreateReqResult transportJobCreateReqResult = null;
                log.info("【step7】- mcsManager.sendTransportJobCreateReq");
                try {
                    transportJobCreateReqResult = mcsManager.sendTransportJobCreateReq(objCommon, tempTranJobCreateReq);
                } catch (ServiceException e) {
                    //Integration error
                    if (Validations.isEquals(e.getCode(),msgRetCodeConfig.getMsgGrpcTimeOut())) throw e;
                    //MCS Return error case
                    if (Validations.isEquals(e.getCode(), msgRetCodeConfig.getMsgUnknownJobid()) || Validations.isEquals(e.getCode(), msgRetCodeConfig.getMsgDifferrentJobID())) {
                        log.info("MCS Return is RC_UNKNOWN_JOBID or RC_DIFFERRENT_JOBID");
                        Validations.check(ArrayUtils.getSize(transportJobCreateReqResult.getJobCreateResultSequenceData()) != 1, msgRetCodeConfig.getMsgMismatchInoutLength());
                        if (Constant.TM_TRANSPORT_TYPE_S.equals(tempTranJobCreateReq.getTransportType())) {
                            log.info("transportType  == 'S'");
                            Validations.check(ArrayUtils.getSize(transportJobCreateReqResult.getJobCreateResultSequenceData()) != 1, msgRetCodeConfig.getMsgNeedSingleData());
                            //==========================================================
                            // Inquiry JobID to MCS by inquiryType = 'C'
                            //==========================================================
                            Params.TransportJobInqParams transportJobInq = new Params.TransportJobInqParams();
                            transportJobInq.setInquiryType(Constant.TM_INQUIRY_TYPE_BY_CARRIER);
                            transportJobInq.setCarrierID(tempTranJobCreateReq.getJobCreateData().get(0).getCarrierID());
                            Results.TransportJobInqResult transportJobInqResult = null;
                            log.info("【step8】- mcsManager.sendTransportJobInq");
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
                            log.info("【step9】- mcsManager.sendTransportJobCreateReq");
                            Results.TransportJobCreateReqResult transportJobCreateReqResultExcption = mcsManager.sendTransportJobCreateReq(objCommon, tempTranJobCreateReq);
                        } else {
                            log.info("transportType != 'S'");
                        }
                    } else {
                        /********************************/
                        /* Add Retry to current stocker */
                        /********************************/
                        log.info("Retry to store into current stocker!");
                        Infos.AmhsToDestination toDestinationToSTK = new Infos.AmhsToDestination();
                        toDestinationToSTK.setToMachineID(carrierIDReadReportParmas.getMachineID());
                        tempTranJobCreateReq.getJobCreateData().get(0).setToMachine(Collections.singletonList(toDestinationToSTK));
                        log.info("【step10】- mcsManager.sendTransportJobCreateReq");
                        Results.TransportJobCreateReqResult transportJobCreateReqResultCurrentSTK = mcsManager.sendTransportJobCreateReq(objCommon, tempTranJobCreateReq);
                    }
                }
                Validations.check(ArrayUtils.getSize(transportJobCreateReqResult.getJobCreateResultSequenceData()) != 1, msgRetCodeConfig.getMsgMismatchInoutLength());
                /*-----------------------------------------------------------*/
                /* Insert & Update TrabsferData by Return Structure from MCS*/
                /*-----------------------------------------------------------*/
                log.info("Insert & Update TrabsferData by Return Structure from MCS");
                Infos.TransferJobInfo transferJobInfo = new Infos.TransferJobInfo();
                transferJobInfo.setCarrierID(carrierIDReadReportParmas.getCarrierID());
                transferJobInfo.setJobID(transportJobCreateReqResult.getJobID());
                transferJobInfo.setCarrierJobID(transportJobCreateReqResult.getJobCreateResultSequenceData().get(0).getCarrierJobID());
                transferJobInfo.setTransportType(tempTranJobCreateReq.getTransportType());
                transferJobInfo.setZoneType(tempTranJobCreateReq.getJobCreateData().get(0).getZoneType());
                transferJobInfo.setN2PurgeFlag(tempTranJobCreateReq.getJobCreateData().get(0).getN2PurgeFlag());
                transferJobInfo.setFromMachineID(tempTranJobCreateReq.getJobCreateData().get(0).getFromMachineID());
                transferJobInfo.setFromPortID(tempTranJobCreateReq.getJobCreateData().get(0).getFromPortID());
                transferJobInfo.setToStockerGroup(tempTranJobCreateReq.getJobCreateData().get(0).getToStockerGroup());
                transferJobInfo.setToMachineID(transportJobCreateReqResult.getJobCreateResultSequenceData().get(0).getToMachineID());
                transferJobInfo.setToPortID(ObjectIdentifier.buildWithValue(tempTranJobCreateReq.getJobCreateData().get(0).getToMachine().size() <= 0 ? "" : ObjectIdentifier.fetchValue(tempTranJobCreateReq.
                        getJobCreateData().get(0).getToMachine().get(0).getToPortID())));
                transferJobInfo.setExpectedStartTime(tempTranJobCreateReq.getJobCreateData().get(0).getExpectedStartTime());
                transferJobInfo.setExpectedEndTime(tempTranJobCreateReq.getJobCreateData().get(0).getExpectedEndTime());
                transferJobInfo.setEstimatedStartTime(transportJobCreateReqResult.getJobCreateResultSequenceData().get(0).getEstimatedStartTime());
                transferJobInfo.setEstimatedEndTime(transportJobCreateReqResult.getJobCreateResultSequenceData().get(0).getEstimatedEndTime());
                transferJobInfo.setMandatoryFlag(tempTranJobCreateReq.getJobCreateData().get(0).getMandatoryFlag());
                transferJobInfo.setPriority(tempTranJobCreateReq.getJobCreateData().get(0).getPriority());
                transferJobInfo.setJobStatus("");
                transferJobInfo.setCarrierJobStatus("");
                transferJobInfo.setTimeStamp(new Timestamp(System.currentTimeMillis()).toString());
                log.info("【step11】- transferJobMethod.transferJobPut");
                transferJobMethod.transferJobPut(objCommon, Collections.singletonList(transferJobInfo));
            }
        } else {
            log.info("carrierIDReadReportParmas.getJobRequestFlag() != TRUE");
        }

        //=====================================================================
        // Stock-In failed... Force Stock Out requred case
        //=====================================================================
        if (BooleanUtils.isTrue(abnormalStockFlag) && Constant.TM_ON.equals(errorOutFlag)) {
            log.info("AbnormalStockFlag == TRUE && ErrorOutFlag == ON");
            //=====================================================================
            // Get stocker information in order to get stocker manual output info.
            //=====================================================================
            Boolean detailFlag = false;
            ObjectIdentifier currStockerID = carrierIDReadReportParmas.getMachineID();
            log.info("【step12】- omsManager.sendReticlePodStockerInfoInq");
            Results.ReticlePodStockerInfoInqResult reticlePodStockerInfoInqResult = omsManager.sendReticlePodStockerInfoInq(objCommon,
                    currStockerID);

            //=====================================================================
            //   Prepare input parameter for Stock-Out Request
            //=====================================================================
            List<Infos.AmhsResourceInfo> tmpResourceInfoData = new ArrayList<>();
            ObjectIdentifier toPortID = null;
            if (null != reticlePodStockerInfoInqResult) {
                tmpResourceInfoData = reticlePodStockerInfoInqResult.getResourceInfoData();
            }
            log.info("length of stocker resource: {}", ArrayUtils.getSize(tmpResourceInfoData));
            if (ArrayUtils.isNotEmpty(tmpResourceInfoData)) {
                for (Infos.AmhsResourceInfo resourceInfo : tmpResourceInfoData) {
                    log.info("resourceID: {}", resourceInfo.getResourceID());
                    if (StringUtils.isEmpty(strResourceType)) {
                        log.info("OM_MO_RESOURCE_TYPE is not defined, user default");
                        strResourceType = Constant.TM_RESOURCE_TYPE_MANUAL_OUT;
                    }
                    log.info("strResourceType: {}", strResourceType);
                    if (StringUtils.equals(resourceInfo.getResourceType(), strResourceType)) {
                        log.info("toPortID: {}", resourceInfo.getResourceID());
                        toPortID = ObjectIdentifier.buildWithValue(resourceInfo.getResourceID());
                        break;
                    }
                }
            }
            Params.TransportJobCreateReqParams tempTranJobCreateReq = new Params.TransportJobCreateReqParams();
            tempTranJobCreateReq.setTransportType(Constant.TM_TRANSPORT_TYPE_S);

            Infos.AmhsJobCreateArray amhsJobCreateArray = new Infos.AmhsJobCreateArray();
            tempTranJobCreateReq.setJobCreateData(Collections.singletonList(amhsJobCreateArray));

            Infos.AmhsToDestination toMachine = new Infos.AmhsToDestination();
            toMachine.setToMachineID(carrierIDReadReportParmas.getMachineID());
            toMachine.setToPortID(toPortID);

            amhsJobCreateArray.setToMachine(Collections.singletonList(toMachine));
            //=================================================//
            // call sendReticlePodStatusInq             //
            //=================================================//
            Results.ReticlePodStatusInqResult reticlePodStatusInqResult = null;
            log.info("【step12】- mcsManager.sendReticlePodStatusInq");
            try {
                reticlePodStatusInqResult = omsManager.sendReticlePodStatusInq(objCommon,
                        carrierIDReadReportParmas.getCarrierID());
            } catch (Exception e) {
                zoneType = Constant.TM_ZONE_TYPE_UNKNOWN;
            }

            //=================================================//
            // Check ReticlePod is empty                       //
            //=================================================//
            if (null != reticlePodStatusInqResult && BooleanUtils.isTrue(reticlePodStatusInqResult.getReticlePodStatusInfo().getEmptyFlag())) {
                log.info("emptyFlag == TRUE ");
                zoneType = Constant.TM_ZONE_TYPE_EMP;
            } else {
                log.info("emptyFlag != TRUE ");
                zoneType = Constant.TM_ZONE_TYPE_RSP;
            }

            amhsJobCreateArray.setZoneType(zoneType);
            amhsJobCreateArray.setCarrierID(carrierIDReadReportParmas.getCarrierID());
            amhsJobCreateArray.setFromMachineID(carrierIDReadReportParmas.getMachineID());
            amhsJobCreateArray.setFromPortID(carrierIDReadReportParmas.getPortID());
            amhsJobCreateArray.setExpectedEndTime("");
            amhsJobCreateArray.setPriority(Constant.TM_TRANSFER_JOB_PRIORITY_NINETY_NINE);
            amhsJobCreateArray.setMandatoryFlag(false);
            tempTranJobCreateReq.setRerouteFlag(false);

            //=====================================================================
            //   Send Transfer Data to MCS
            //=====================================================================
            Results.TransportJobCreateReqResult transportJobCreateReqResult = null;
            log.info("【step13】- mcsManager.sendTransportJobCreateReq");

            transportJobCreateReqResult = mcsManager.sendTransportJobCreateReq(objCommon, tempTranJobCreateReq);
            Validations.check(ArrayUtils.getSize(transportJobCreateReqResult.getJobCreateResultSequenceData()) != 1, msgRetCodeConfig.getMsgMismatchInoutLength());
            /*-----------------------------------------------------------*/
            /* Insert & Update TrabsferData by Return Structure from MCS*/
            /*-----------------------------------------------------------*/
            log.info("Insert & Update TrabsferData by Return Structure from MCS");
            Infos.TransferJobInfo transferJobInfo = new Infos.TransferJobInfo();
            transferJobInfo.setCarrierID(carrierIDReadReportParmas.getCarrierID());
            transferJobInfo.setJobID(transportJobCreateReqResult.getJobID());
            transferJobInfo.setCarrierJobID(transportJobCreateReqResult.getJobCreateResultSequenceData().get(0).getCarrierJobID());
            transferJobInfo.setTransportType(tempTranJobCreateReq.getTransportType());
            transferJobInfo.setZoneType(zoneType);
            transferJobInfo.setN2PurgeFlag(tempTranJobCreateReq.getJobCreateData().get(0).getN2PurgeFlag());
            transferJobInfo.setFromMachineID(tempTranJobCreateReq.getJobCreateData().get(0).getFromMachineID());
            transferJobInfo.setFromPortID(tempTranJobCreateReq.getJobCreateData().get(0).getFromPortID());
            transferJobInfo.setToStockerGroup(tempTranJobCreateReq.getJobCreateData().get(0).getToStockerGroup());
            transferJobInfo.setToMachineID(transportJobCreateReqResult.getJobCreateResultSequenceData().get(0).getToMachineID());
            transferJobInfo.setToPortID(ObjectIdentifier.buildWithValue(tempTranJobCreateReq.getJobCreateData().get(0).getToMachine().size() <= 0 ? "" : ObjectIdentifier.fetchValue(tempTranJobCreateReq.
                    getJobCreateData().get(0).getToMachine().get(0).getToPortID())));
            transferJobInfo.setExpectedStartTime(tempTranJobCreateReq.getJobCreateData().get(0).getExpectedStartTime());
            transferJobInfo.setExpectedEndTime(tempTranJobCreateReq.getJobCreateData().get(0).getExpectedEndTime());
            transferJobInfo.setEstimatedStartTime(transportJobCreateReqResult.getJobCreateResultSequenceData().get(0).getEstimatedStartTime());
            transferJobInfo.setEstimatedEndTime(transportJobCreateReqResult.getJobCreateResultSequenceData().get(0).getEstimatedEndTime());
            transferJobInfo.setMandatoryFlag(tempTranJobCreateReq.getJobCreateData().get(0).getMandatoryFlag());
            transferJobInfo.setPriority(Constant.TM_TRANSFER_JOB_PRIORITY_NINETY_NINE);
            transferJobInfo.setJobStatus("");
            transferJobInfo.setCarrierJobStatus("");
            transferJobInfo.setTimeStamp(new Timestamp(System.currentTimeMillis()).toString());
            log.info("【step14】- transferJobMethod.transferJobPut");
            transferJobMethod.transferJobPut(objCommon, Collections.singletonList(transferJobInfo));
            Validations.check(true,msgRetCodeConfig.getMsgAbnormalStock(),
                    ObjectIdentifier.fetchValue(result.getCarrierID()));
        }
        return result;
    }
}
