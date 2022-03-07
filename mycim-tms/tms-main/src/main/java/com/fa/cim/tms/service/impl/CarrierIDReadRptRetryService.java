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
import com.fa.cim.tms.method.ITransferJobMethod;
import com.fa.cim.tms.pojo.Infos;
import com.fa.cim.tms.pojo.ObjectIdentifier;
import com.fa.cim.tms.service.ICarrierIDReadRptRetryService;
import com.fa.cim.tms.utils.ArrayUtils;
import com.fa.cim.tms.utils.BooleanUtils;
import com.fa.cim.tms.utils.StringUtils;
import com.fa.cim.tms.utils.Validations;
import lombok.extern.slf4j.Slf4j;
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
@Service
@Slf4j
public class CarrierIDReadRptRetryService implements ICarrierIDReadRptRetryService {

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

    public Results.CarrierIDReadReportResult sxCarrierIDReadRptRetry(Infos.ObjCommon objCommon, Params.CarrierIDReadReportParmas carrierIDReadReportParmas){

        Results.CarrierIDReadReportResult result = new Results.CarrierIDReadReportResult();
        result.setCarrierID(carrierIDReadReportParmas.getCarrierID());
        String errorOutFlag = envMethod.getEnvFromOmsDB(EnvCodeEnum.OM_TM06_ERROR_OUT.getValue());
        String fromPortFlag = envMethod.getEnvFromOmsDB(EnvCodeEnum.OM_TM06_SEND_FROM_PORTID.getValue());
        String noFromMachineIDForSTKFlag = envMethod.getEnvFromOmsDB(EnvCodeEnum.OM_OM01_NO_FROM_MACHINEID_FOR_STK.getValue());//"OFF"
        log.info("env OM_TM06_ERROR_OUT: {}", errorOutFlag);
        log.info("env TMS_OM_TM06_SEND_FROM_PORTID: {}", fromPortFlag);
        log.info("env OM_OM01_NO_FROM_MACHINEID_FOR_STK: {}", noFromMachineIDForSTKFlag);

        Boolean reRouteFlag = false;
        Boolean abnormalStockFlag = false;

        String deliveryReqOn = envMethod.getEnvFromOmsDB(EnvCodeEnum.OM_DELIVERY_REQ_ON.getValue());//may be we need change to NO we can join whereNext
        log.info("env TMS_OM_DELIVERY_REQ_ON: {}", deliveryReqOn);

        /********************************/
        /* Set inquiryType = 'C'        */
        /* Get records from OTXFERREQ    */
        /********************************/
        if (!Constant.TM_YES.equals(deliveryReqOn)) {
            String inquiryType = Constant.TM_INQUIRY_TYPE_BY_CARRIER;
            ObjectIdentifier toMachineID = null;
            ObjectIdentifier fromMachineID = null;
            List<String> seqJobID = null;
            List<ObjectIdentifier> seqCarrierID = Collections.singletonList(ObjectIdentifier.buildWithValue(ObjectIdentifier.fetchValue(carrierIDReadReportParmas.getCarrierID())));
            log.info("【step1】- transferJobGet");
            List<Infos.TransferJobInfo> transferJobInfoList = null;
            Boolean okFlag = true;
            try {
                transferJobInfoList = transferJobMethod.transferJobGet(objCommon, inquiryType, seqCarrierID, toMachineID, fromMachineID, seqJobID);
            } catch (ServiceException e) {
                if (!Validations.isEquals(msgRetCodeConfig.getMsgRecordNotFound(), e.getCode())) {
                    throw e;
                }
                okFlag = false;
            }
            if (BooleanUtils.isTrue(okFlag)) {
                reRouteFlag = true;
            }
            log.info("reRouteFlag: {}", reRouteFlag);
            log.info("deliveryReqOn = NO");

            log.info("【step1】- omsManager.sendWhereNextInterBay");
            Results.WhereNextInterBayResult whereNextInterBayResult = null;
            try {
                whereNextInterBayResult = omsManager.sendWhereNextInterBay(objCommon, null, carrierIDReadReportParmas.getCarrierID());
            } catch (ServiceException e) {
                if (Validations.isEquals(msgRetCodeConfig.getMsgInvalidLotProcstat(), e.getCode())) {
                    abnormalStockFlag = true;
                } else if (!Validations.isEquals(msgRetCodeConfig.getMsgLotNotFoundOM(), e.getCode())) {
                    throw e;
                }
            }
            if (Constant.TM_LOT_STATE_SHIPPED.equals(whereNextInterBayResult.getLotState())) {
                log.info("lot state is SHIPPED");
                abnormalStockFlag = true;
            }
            if (Constant.TM_TRANSSTATE_EQUIPMENTIN.equals(whereNextInterBayResult.getTransferStatus())) {
                log.info("found IN-PROCESS Lot");
                abnormalStockFlag = true;
            }

            if (BooleanUtils.isFalse(abnormalStockFlag)) {
                /*-----------------------------------------------------------*/
                /*   Send Transfer Data to MCS                               */
                /*-----------------------------------------------------------*/
                Params.TransportJobCreateReqParams tempTranJobCreateReq = new Params.TransportJobCreateReqParams();
                tempTranJobCreateReq.setTransportType(Constant.TM_TRANSPORT_TYPE_S);
                List<ObjectIdentifier> tempMachineIDs = new ArrayList<>();
                Optional.ofNullable(whereNextInterBayResult.getWhereNextEqpStatus()).ifPresent(list -> list.forEach(data -> {
                    Optional.ofNullable(data.getEqpStockerStatus()).ifPresent(eqpStockerStatuses -> eqpStockerStatuses.forEach(eqpStockerStatus -> {
                        tempMachineIDs.add(eqpStockerStatus.getStockerID());
                    }));
                }));
                ArrayUtils.removeDuplicate(tempMachineIDs);
                List<ObjectIdentifier> machineIDs = new ArrayList<>(tempMachineIDs);
                log.info("Next Socker Count: {}", ArrayUtils.getSize(machineIDs));
                //Copy machineIDs except duplicated IDs 0.04
                List<Infos.AmhsJobCreateArray> jobCreateData = new ArrayList<>();
                tempTranJobCreateReq.setJobCreateData(jobCreateData);
                Infos.AmhsJobCreateArray amhsJobCreateArray = new Infos.AmhsJobCreateArray();
                jobCreateData.add(amhsJobCreateArray);
                List<Infos.AmhsToDestination> toMachine = new ArrayList<>();
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
                log.info("【step2】- omsManager.sendCassetteStatusInq");
                Results.CarrierDetailInfoInqResult cassetteStatusInqResult = omsManager.sendCassetteStatusInq(objCommon, carrierIDReadReportParmas.getCarrierID());

                amhsJobCreateArray.setZoneType(cassetteStatusInqResult.getCassetteStatusInfo().getZoneType());
                amhsJobCreateArray.setPriority(cassetteStatusInqResult.getCassetteStatusInfo().getPriority());
                if (StringUtils.isEmpty(amhsJobCreateArray.getZoneType())) {
                    amhsJobCreateArray.setZoneType(Constant.TM_STRING_DEFAULT);
                }
                if (StringUtils.isEmpty(amhsJobCreateArray.getPriority())) {
                    amhsJobCreateArray.setPriority(Constant.TM_TRANSFER_JOB_PRIORITY_FOUR);
                }
                amhsJobCreateArray.setCarrierID(carrierIDReadReportParmas.getCarrierID());

                if (Constant.TM_ON.equals(fromPortFlag)) {
                    amhsJobCreateArray.setFromMachineID(carrierIDReadReportParmas.getMachineID());
                    amhsJobCreateArray.setFromPortID(carrierIDReadReportParmas.getPortID());
                } else if (!Constant.TM_ON.equals(noFromMachineIDForSTKFlag)) {
                    amhsJobCreateArray.setFromMachineID(carrierIDReadReportParmas.getMachineID());
                }

                amhsJobCreateArray.setExpectedEndTime(Constant.TM_TRANSFER_JOB_EXPECTED_END_TIME);
                amhsJobCreateArray.setMandatoryFlag(false);
                tempTranJobCreateReq.setRerouteFlag(reRouteFlag);
                if (BooleanUtils.isTrue(reRouteFlag)) {
                    tempTranJobCreateReq.setJobID(transferJobInfoList.get(0).getJobID());
                    amhsJobCreateArray.setCarrierJobID(transferJobInfoList.get(0).getCarrierJobID());
                }

                tempTranJobCreateReq.getJobCreateData().add(amhsJobCreateArray);
                log.info("【step3】- mcsManager.sendTransportJobCreateReq");
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
                            log.info("【step4】- mcsManager.sendTransportJobInq");
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
                            log.info("【step5】- mcsManager.sendTransportJobCreateReq");
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
                        log.info("【step6】- mcsManager.sendTransportJobCreateReq");
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
                log.info("【step6】- transferJobMethod.transferJobPut");
                transferJobMethod.transferJobPut(objCommon, Collections.singletonList(transferJobInfo));
            }
        } else {
            log.info("deliveryReqOn = YES");
        }
        if (BooleanUtils.isTrue(abnormalStockFlag)) {
            log.info("abnormalStockFlag == TRUE");
            if (Constant.TM_ON.equals(errorOutFlag)) {
                log.info("errorOutFlag is ON");
                ObjectIdentifier currStockerID = ObjectIdentifier.buildWithValue(ObjectIdentifier.fetchValue(carrierIDReadReportParmas.getMachineID()));
                ObjectIdentifier toPortID = null;

                Boolean detailFlag = false;
                log.info("【step7】- omsManager.sendStockerInfoInq");
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
                Params.TransportJobCreateReqParams tempTranJobCreateReq = new Params.TransportJobCreateReqParams();
                tempTranJobCreateReq.setTransportType(Constant.TM_TRANSPORT_TYPE_S);
                List<Infos.AmhsToDestination> seqToMachine = new ArrayList<>();
                Infos.AmhsToDestination amhsToDestination = new Infos.AmhsToDestination();
                amhsToDestination.setToPortID(toPortID);
                amhsToDestination.setToMachineID(carrierIDReadReportParmas.getMachineID());
                seqToMachine.add(amhsToDestination);
                Infos.AmhsJobCreateArray amhsJobCreateArray = new Infos.AmhsJobCreateArray();
                amhsJobCreateArray.setToMachine(seqToMachine);
                amhsJobCreateArray.setZoneType(Constant.TM_STRING_DEFAULT);
                amhsJobCreateArray.setCarrierID(carrierIDReadReportParmas.getCarrierID());

                if (Constant.TM_ON.equals(fromPortFlag)) {
                    amhsJobCreateArray.setFromMachineID(carrierIDReadReportParmas.getMachineID());
                    amhsJobCreateArray.setFromPortID(carrierIDReadReportParmas.getPortID());
                } else if (!Constant.TM_ON.equals(noFromMachineIDForSTKFlag)) {
                    amhsJobCreateArray.setFromPortID(carrierIDReadReportParmas.getMachineID());
                }
                amhsJobCreateArray.setExpectedEndTime(Constant.TM_TRANSFER_JOB_EXPECTED_END_TIME);
                amhsJobCreateArray.setPriority(Constant.TM_TRANSFER_JOB_PRIORITY_FOUR);
                amhsJobCreateArray.setMandatoryFlag(false);
                tempTranJobCreateReq.setRerouteFlag(false);
                tempTranJobCreateReq.getJobCreateData().add(amhsJobCreateArray);
                log.info("【step8】- omsManager.sendTransportJobCreateReq");
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
                log.info("【step9】- transferJobMethod.transferJobPut");
                transferJobMethod.transferJobPut(objCommon, Collections.singletonList(transferJobInfo));
            } else {
                log.info("errorOutFlag != ON");
            }
            Validations.check(true, msgRetCodeConfig.getMsgAbnormalStock(),
                    ObjectIdentifier.fetchValue(result.getCarrierID()));
        }
        return result;
    }
}
