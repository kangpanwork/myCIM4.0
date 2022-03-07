package com.fa.cim.tms.service.impl;

import com.fa.cim.common.utils.CimDateUtils;
import com.fa.cim.tms.common.constant.Constant;
import com.fa.cim.tms.dto.Params;
import com.fa.cim.tms.dto.Results;
import com.fa.cim.tms.manager.IMCSManager;
import com.fa.cim.tms.manager.IOMSManager;
import com.fa.cim.tms.method.ITransferJobMethod;
import com.fa.cim.tms.pojo.Infos;
import com.fa.cim.tms.pojo.ObjectIdentifier;
import com.fa.cim.tms.service.ITransportJobInqService;
import com.fa.cim.tms.utils.ArrayUtils;
import com.fa.cim.tms.utils.BooleanUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * description:
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/10/21                               Neyo                create file
 *
 * @author: Neyo
 * @date: 2020/10/21 11:18
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@Service
public class TransportJobInqService implements ITransportJobInqService {
    @Autowired
    private IMCSManager mcsManager;
    @Autowired
    private ITransferJobMethod transferJobMethod;
    @Autowired
    private IOMSManager omsManager;

    public Results.TransportJobInqResult sxTransportJobInq(Infos.ObjCommon objCommon, Params.TransportJobInqParams transportJobInqParams) {
        Results.TransportJobInqResult result = new Results.TransportJobInqResult();
        Params.TransportJobInqParams transportJobInq = new Params.TransportJobInqParams();
        transportJobInq.setInquiryType(transportJobInqParams.getInquiryType());
        transportJobInq.setCarrierID(transportJobInqParams.getCarrierID());
        transportJobInq.setFromMachineID(transportJobInqParams.getFromMachineID());
        transportJobInq.setToMachineID(transportJobInqParams.getToMachineID());

        /*-----------------------------------------------------------*/
        /*   Send Transfer Data to MCS                              */
        /*-----------------------------------------------------------*/
        if (Constant.TM_TRANSFER_JOB_INQ_FUNCTION_IDINQUIRY.equals(transportJobInqParams.getFunctionID())) {
            log.info("functionID == INQUIRY");
            log.info("【step1】 - mcsManager.sendTransportJobInq");
            result = mcsManager.sendTransportJobInq(objCommon, transportJobInq);
        } else if (Constant.TM_TRANSFER_JOB_INQ_FUNCTION_UPDATE.equals(transportJobInqParams.getFunctionID())) {
            log.info("functionID == UPDATE");
            /* Get data from MCS */
            log.info("【step2】 - mcsManager.sendTransportJobInq");
            result = mcsManager.sendTransportJobInq(objCommon, transportJobInq);

            /* Insert or Update to OTXFERREQ by getting data from MCS */
            List<Infos.TransferJobInfo> saveTransferJobList = new ArrayList<>();
            if (null != result && ArrayUtils.isNotEmpty(result.getJobInqData())) {
                log.info("find mcs transferJob list");
                for (Infos.AmhsTransportJobInqData jobInqData : result.getJobInqData()) {
                    if (ArrayUtils.isNotEmpty(jobInqData.getCarrierJobInqInfo())) {
                        jobInqData.getCarrierJobInqInfo().forEach(transferJobInfo -> {
                            Infos.TransferJobInfo saveTransferInfo = new Infos.TransferJobInfo();
                            saveTransferJobList.add(saveTransferInfo);
                            BeanUtils.copyProperties(transferJobInfo, saveTransferInfo);
                            saveTransferInfo.setJobStatus(jobInqData.getJobStatus());
                            saveTransferInfo.setJobID(jobInqData.getJobID());
                            saveTransferInfo.setTransportType(jobInqData.getTransportType());
                            saveTransferInfo.setToStockerGroup("");
                            saveTransferInfo.setTimeStamp(CimDateUtils.getCurrentTimeStamp().toString());
                        });
                    }
                }

            }
            log.info("【step3】 - transferJobMethod.transferJobPut");
            transferJobMethod.transferJobPut(objCommon, saveTransferJobList);
        } else {
            log.info("functionID != INQUIRY, UPDATE");
            log.info("【step4】 - transferJobMethod.transferJobGet");
            List<Infos.TransferJobInfo> transferJobInfoList = transferJobMethod.transferJobGet(objCommon,
                    transportJobInq.getInquiryType(),
                    Arrays.asList(transportJobInq.getCarrierID()),
                    transportJobInq.getToMachineID(),
                    transportJobInq.getFromMachineID(),
                    null);
            log.info("sort transferJobList");
            if (ArrayUtils.isNotEmpty(transferJobInfoList)) {
                transferJobInfoList = transferJobInfoList.stream().sorted(Comparator.comparing(transferJobInfo -> ObjectIdentifier.fetchValue(transferJobInfo.getCarrierID()))).collect(Collectors.toList());
            }
            result.setInquiryType(transportJobInq.getInquiryType());
            List<Infos.AmhsTransportJobInqData> transportJobInqDataList = new ArrayList<>();
            result.setJobInqData(transportJobInqDataList);
            if (ArrayUtils.isNotEmpty(transferJobInfoList)) {
                for (Infos.TransferJobInfo transferJobInfo : transferJobInfoList) {
                    Infos.AmhsTransportJobInqData amhsTransportJobInqData = new Infos.AmhsTransportJobInqData();
                    transportJobInqDataList.add(amhsTransportJobInqData);
                    amhsTransportJobInqData.setJobID(transferJobInfo.getJobID());
                    amhsTransportJobInqData.setTransportType(transferJobInfo.getTransportType());
                    amhsTransportJobInqData.setJobStatus(transferJobInfo.getJobStatus());

                    List<Infos.TransferJobInfo> carrierJobInqInfo = new ArrayList<>();
                    amhsTransportJobInqData.setCarrierJobInqInfo(carrierJobInqInfo);
                    Infos.TransferJobInfo transferJobInfoInner = new Infos.TransferJobInfo();
                    carrierJobInqInfo.add(transferJobInfoInner);

                    BeanUtils.copyProperties(transferJobInfo, transferJobInfoInner);
                }
            }
        }
        return result;
    }

    @Override
    public Results.TransportJobInqResult sxRtmsTransportJobInq(Infos.ObjCommon objCommon, Params.TransportJobInqParams transportJobInqParams) {
        Results.TransportJobInqResult result = new Results.TransportJobInqResult();
        Params.TransportJobInqParams transportJobInq = new Params.TransportJobInqParams();
        transportJobInq.setInquiryType(transportJobInqParams.getInquiryType());
        transportJobInq.setCarrierID(transportJobInqParams.getCarrierID());
        transportJobInq.setFromMachineID(transportJobInqParams.getFromMachineID());
        transportJobInq.setToMachineID(transportJobInqParams.getToMachineID());

        /*-----------------------------------------------------------*/
        /*   Send Transfer Data to MCS                              */
        /*-----------------------------------------------------------*/
        if (Constant.TM_TRANSFER_JOB_INQ_FUNCTION_IDINQUIRY.equals(transportJobInqParams.getFunctionID())) {
            log.info("functionID == INQUIRY");
            log.info("【step1】 - mcsManager.sendTransportJobInq");
            result = mcsManager.sendTransportJobInq(objCommon, transportJobInq);
        } else if (Constant.TM_TRANSFER_JOB_INQ_FUNCTION_UPDATE.equals(transportJobInqParams.getFunctionID())) {
            log.info("functionID == UPDATE");
            /* Get data from MCS */
            log.info("【step2】 - mcsManager.sendTransportJobInq");
            result = mcsManager.sendTransportJobInq(objCommon, transportJobInq);

            /* Insert or Update to OTXFERREQ by getting data from MCS */
            List<Infos.TransferJobInfo> saveTransferJobList = new ArrayList<>();
            if (null != result && ArrayUtils.isNotEmpty(result.getJobInqData())) {
                log.info("find mcs transferJob list");
                for (Infos.AmhsTransportJobInqData jobInqData : result.getJobInqData()) {
                    if (ArrayUtils.isNotEmpty(jobInqData.getCarrierJobInqInfo())) {
                        saveTransferJobList.forEach(transferJobInfo -> {
                            Infos.TransferJobInfo saveTransferInfo = new Infos.TransferJobInfo();
                            saveTransferJobList.add(saveTransferInfo);
                            saveTransferInfo.setCarrierID(transferJobInfo.getCarrierID());
                            saveTransferInfo.setJobID(jobInqData.getJobID());
                            saveTransferInfo.setCarrierJobID(transferJobInfo.getCarrierJobID());
                            saveTransferInfo.setTransportType(jobInqData.getTransportType());
                            //=================================================//
                            // call sendReticlePodStatusInq             //
                            //=================================================//
                            log.info("【step3】 - omsManager.sendReticlePodStatusInq");
                            Results.ReticlePodStatusInqResult reticlePodStatusInqResult = omsManager.sendReticlePodStatusInq(objCommon,
                                    transportJobInqParams.getCarrierID());

                            //=================================================//
                            // Check ReticlePod is empty                       //
                            //=================================================//
                            if (null != reticlePodStatusInqResult && null != reticlePodStatusInqResult.getReticlePodStatusInfo() && BooleanUtils.isTrue(reticlePodStatusInqResult.getReticlePodStatusInfo().getEmptyFlag())){
                                log.info("emptyFlag == TRUE");
                                saveTransferInfo.setZoneType(Constant.TM_ZONE_TYPE_EMP);
                            }else {
                                log.info("emptyFlag != TRUE");
                                saveTransferInfo.setZoneType(Constant.TM_ZONE_TYPE_RSP);
                            }
                            saveTransferInfo.setN2PurgeFlag(transferJobInfo.getN2PurgeFlag());
                            saveTransferInfo.setFromMachineID(transferJobInfo.getFromMachineID());
                            saveTransferInfo.setFromPortID(transferJobInfo.getFromPortID());
                            saveTransferInfo.setToStockerGroup("");
                            saveTransferInfo.setToMachineID(transferJobInfo.getToMachineID());
                            saveTransferInfo.setToPortID(transferJobInfo.getToPortID());
                            saveTransferInfo.setExpectedStartTime(transferJobInfo.getExpectedStartTime());
                            saveTransferInfo.setExpectedEndTime(transferJobInfo.getExpectedEndTime());
                            saveTransferInfo.setEstimatedStartTime(transferJobInfo.getEstimatedStartTime());
                            saveTransferInfo.setEstimatedEndTime(transferJobInfo.getEstimatedEndTime());
                            saveTransferInfo.setMandatoryFlag(transferJobInfo.getMandatoryFlag());
                            saveTransferInfo.setPriority(Constant.TM_TRANSFER_JOB_PRIORITY_NINETY_NINE);
                            saveTransferInfo.setJobStatus(jobInqData.getJobStatus());
                            saveTransferInfo.setCarrierJobStatus(transferJobInfo.getCarrierJobStatus());
                            saveTransferInfo.setTimeStamp(CimDateUtils.getCurrentTimeStamp().toString());
                        });
                    }
                }
            }
            log.info("【step4】 - transferJobMethod.transferJobPut");
            transferJobMethod.transferJobPut(objCommon, saveTransferJobList);
        } else {
            log.info("functionID != INQUIRY, UPDATE");
            log.info("【step5】 - transferJobMethod.transferJobGet");
            List<Infos.TransferJobInfo> transferJobInfoList = transferJobMethod.transferJobGet(objCommon,
                    transportJobInq.getInquiryType(),
                    Arrays.asList(transportJobInq.getCarrierID()),
                    transportJobInq.getToMachineID(),
                    transportJobInq.getFromMachineID(),
                    null);
            log.info("sort transferJobList");
            if (ArrayUtils.isNotEmpty(transferJobInfoList)) {
                transferJobInfoList = transferJobInfoList.stream().sorted(Comparator.comparing(transferJobInfo -> ObjectIdentifier.fetchValue(transferJobInfo.getCarrierID()))).collect(Collectors.toList());
            }
            result.setInquiryType(transportJobInq.getInquiryType());
            List<Infos.AmhsTransportJobInqData> transportJobInqDataList = new ArrayList<>();
            result.setJobInqData(transportJobInqDataList);
            if (ArrayUtils.isNotEmpty(transferJobInfoList)) {
                for (Infos.TransferJobInfo transferJobInfo : transferJobInfoList) {
                    Infos.AmhsTransportJobInqData amhsTransportJobInqData = new Infos.AmhsTransportJobInqData();
                    transportJobInqDataList.add(amhsTransportJobInqData);
                    amhsTransportJobInqData.setJobID(transferJobInfo.getJobID());
                    amhsTransportJobInqData.setTransportType(transportJobInq.getInquiryType());
                    amhsTransportJobInqData.setJobStatus(transferJobInfo.getJobStatus());

                    List<Infos.TransferJobInfo> carrierJobInqInfo = new ArrayList<>();
                    amhsTransportJobInqData.setCarrierJobInqInfo(carrierJobInqInfo);
                    Infos.TransferJobInfo transferJobInfoInner = new Infos.TransferJobInfo();
                    carrierJobInqInfo.add(transferJobInfoInner);

                    BeanUtils.copyProperties(transferJobInfo, transferJobInfoInner);
                }
            }
        }
        return result;
    }
}
