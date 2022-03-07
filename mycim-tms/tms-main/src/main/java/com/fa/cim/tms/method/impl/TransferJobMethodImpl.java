package com.fa.cim.tms.method.impl;


import com.fa.cim.tms.common.constant.Constant;
import com.fa.cim.tms.config.MsgRetCodeConfig;
import com.fa.cim.tms.entity.FxtrnreqEntity;
import com.fa.cim.tms.manager.IOMSManager;
import com.fa.cim.tms.method.ITransferJobMethod;
import com.fa.cim.tms.pojo.Infos;
import com.fa.cim.tms.pojo.ObjectIdentifier;
import com.fa.cim.tms.support.CustomizeSupport;
import com.fa.cim.tms.utils.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * description:
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/10/14                              Neyo                create file
 *
 * @author: Neyo
 * @date: 2020/10/14 17:58
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Component
@Slf4j
public class TransferJobMethodImpl implements ITransferJobMethod {
    @Autowired
    private CustomizeSupport customizeSupport;
    @Autowired
    private MsgRetCodeConfig msgRetCodeConfig;
    @Autowired
    private IOMSManager omsManager;


    @Override
    public List<Infos.TransferJobInfo> transferJobGet(Infos.ObjCommon objCommon, String inquiryType, List<ObjectIdentifier> seqCarrierID, ObjectIdentifier toMachineID, ObjectIdentifier fromMachineID, List<String> seqJobID) {
        //Get Total Count
        long totalCount = customizeSupport.count("SELECT COUNT(*) FROM OTXFERREQ");
        Validations.check(totalCount <= 0, msgRetCodeConfig.getMsgRecordNotFound());
        Validations.check(StringUtils.isEmpty(inquiryType), msgRetCodeConfig.getMsgInvalidType());

        List<FxtrnreqEntity> dataList;
        FxtrnreqEntity fxtrnreqEntityExam = new FxtrnreqEntity();
        switch (inquiryType) {
            case Constant.TM_INQUIRY_TYPE_BY_CARRIER:
                dataList = seqCarrierID.stream()
                        .flatMap(id -> {
                            fxtrnreqEntityExam.setCarrierID(ObjectIdentifier.fetchValue(id));
                            return customizeSupport.findAll(fxtrnreqEntityExam).stream();
                        }).collect(Collectors.toList());
                break;
            case Constant.TM_INQUIRY_TYPE_BY_TO_MACHINE:
                fxtrnreqEntityExam.setToMachineID(ObjectIdentifier.fetchValue(toMachineID));
                dataList = customizeSupport.findAll(fxtrnreqEntityExam);
                break;
            case Constant.TM_INQUIRY_TYPE_BY_FROM_MACHINE:
                fxtrnreqEntityExam.setFromMachineID(ObjectIdentifier.fetchValue(fromMachineID));
                dataList = customizeSupport.findAll(fxtrnreqEntityExam);
                break;
            case Constant.TM_INQUIRY_TYPE_BY_JOB:
                dataList = seqJobID.stream()
                        .flatMap(id -> {
                            fxtrnreqEntityExam.setCarrierID(id);
                            return customizeSupport.findAll(fxtrnreqEntityExam).stream();
                        }).collect(Collectors.toList());
                break;
            default:
                Validations.check(true, msgRetCodeConfig.getMsgInvalidType());
                dataList = Collections.emptyList();
                break;
        }
        List<Infos.TransferJobInfo> result = dataList.stream().map(entity -> {
            Infos.TransferJobInfo transferJobInfo = new Infos.TransferJobInfo();
            transferJobInfo.setCarrierID(ObjectIdentifier.buildWithValue(entity.getCarrierID()));
            transferJobInfo.setJobID(entity.getJobID());
            transferJobInfo.setCarrierJobID(entity.getCarrierJobID());
            transferJobInfo.setTransportType(entity.getTransportType());
            transferJobInfo.setZoneType(entity.getZoneType());
            transferJobInfo.setN2PurgeFlag(BooleanUtils.isTrue(entity.getN2PurgeFlag()));
            transferJobInfo.setFromMachineID(ObjectIdentifier.buildWithValue(entity.getFromMachineID()));
            transferJobInfo.setFromPortID(ObjectIdentifier.buildWithValue(entity.getFromPortID()));
            transferJobInfo.setToStockerGroup(entity.getToStockerGroup());
            transferJobInfo.setToMachineID(ObjectIdentifier.buildWithValue(entity.getToMachineID()));
            transferJobInfo.setToPortID(ObjectIdentifier.buildWithValue(entity.getToPortID()));
            transferJobInfo.setExpectedStartTime(entity.getExpectedStrtTime());
            transferJobInfo.setExpectedEndTime(entity.getExpectedEndTime());
            transferJobInfo.setEstimatedStartTime(entity.getEstimateStrtTime());
            transferJobInfo.setEstimatedEndTime(entity.getEstimateEndTime());
            transferJobInfo.setMandatoryFlag(BooleanUtils.isTrue(entity.getMandatoryFlag()));
            transferJobInfo.setPriority(entity.getPriority());
            transferJobInfo.setJobStatus(entity.getJobStatus());
            transferJobInfo.setCarrierJobStatus(entity.getCarrierJobStatus());
            transferJobInfo.setTimeStamp(entity.getTimestamp().toString());
            return transferJobInfo;
        }).collect(Collectors.toList());
        Validations.check(ArrayUtils.isEmpty(result), msgRetCodeConfig.getMsgRecordNotFound());
        return result;
    }

    @Override
    public void transferJobDel(Infos.ObjCommon objCommon, String deleteType, Infos.TransferJobDeleteInfo transferJobDeleteInfo) {
        log.info("transferJobDel deleteType: {}", deleteType);
        if (null != transferJobDeleteInfo) {
            if (Constant.TM_TRANSFER_JOB_DELETE_TYPE_JOBID.equals(deleteType)) {
                FxtrnreqEntity example = new FxtrnreqEntity();
                example.setJobID(transferJobDeleteInfo.getJobID());
                customizeSupport.removeNonRuntimeEntityForExample(example);
            } else if (Constant.TM_TRANSFER_JOB_DELETE_TYPE_CARRIERJOBID.equals(deleteType)) {
                if (ArrayUtils.isEmpty(transferJobDeleteInfo.getCarrierJobData())) return;
                transferJobDeleteInfo.getCarrierJobData().forEach(info -> {
                    FxtrnreqEntity example = new FxtrnreqEntity();
                    example.setCarrierJobID(info.getCarrierJobID());
                    customizeSupport.removeNonRuntimeEntityForExample(example);
                });
            } else if (Constant.TM_TRANSFER_JOB_DELETE_TYPE_CARRIERID.equals(deleteType)) {
                if (ArrayUtils.isEmpty(transferJobDeleteInfo.getCarrierJobData())) return;
                transferJobDeleteInfo.getCarrierJobData().forEach(info -> {
                    FxtrnreqEntity example = new FxtrnreqEntity();
                    example.setCarrierID(ObjectIdentifier.fetchValue(info.getCarrierID()));
                    customizeSupport.removeNonRuntimeEntityForExample(example);
                });
            } else {
                Validations.check(true, msgRetCodeConfig.getMsgInvalidType());
            }
        }
    }

    @Override
    public void transferJobPut(Infos.ObjCommon objCommon, List<Infos.TransferJobInfo> transferJobInfoList) {
        if (ArrayUtils.isEmpty(transferJobInfoList)) return;
        transferJobInfoList.forEach(info -> {
            //check exsit
            FxtrnreqEntity example = new FxtrnreqEntity();
            example.setCarrierID(ObjectIdentifier.fetchValue(info.getCarrierID()));
            example.setJobID(info.getJobID());
            example.setCarrierJobID(info.getCarrierJobID());
            FxtrnreqEntity entity = Optional.ofNullable(customizeSupport.findOne(example)).orElseGet(FxtrnreqEntity::new);
            entity.setCarrierID(ObjectIdentifier.fetchValue(info.getCarrierID()));
            entity.setJobID(info.getJobID());
            entity.setCarrierJobID(info.getCarrierJobID());
            entity.setTransportType(info.getTransportType());
            entity.setZoneType(info.getZoneType());
            entity.setN2PurgeFlag(BooleanUtils.isTrue(info.getN2PurgeFlag()) ? 1 : 0);
            entity.setFromMachineID(ObjectIdentifier.fetchValue(info.getFromMachineID()));
            entity.setFromPortID(ObjectIdentifier.fetchValue(info.getFromPortID()));
            entity.setToStockerGroup(info.getToStockerGroup());
            entity.setToMachineID(ObjectIdentifier.fetchValue(info.getToMachineID()));
            entity.setToPortID(ObjectIdentifier.fetchValue(info.getToPortID()));
            entity.setExpectedStrtTime(info.getExpectedStartTime());
            entity.setExpectedEndTime(info.getExpectedEndTime());
            entity.setEstimateStrtTime(info.getEstimatedStartTime());
            entity.setEstimateEndTime(info.getEstimatedEndTime());
            entity.setMandatoryFlag(BooleanUtils.isTrue(info.getMandatoryFlag()) ? 1 : 0);
            entity.setPriority(info.getPriority());
            entity.setJobStatus(info.getJobStatus());
            entity.setCarrierJobStatus(info.getCarrierJobStatus());
            entity.setTimestamp(DateUtils.convertTo(info.getTimeStamp()));
            customizeSupport.saveNonRuntimeEntity(entity);
        });

        log.info("【step1】 - omsManager.sendDurableXferStatusChangeRpt");
        omsManager.sendDurableXferStatusChangeRpt(objCommon,
                transferJobInfoList,
                Constant.TM_DURABLE_XFER_JOB_STATUS_CREATED,
                "",true);

    }

    @Override
    public List<Infos.TransferJobInfo> transferCarrierGet(Infos.ObjCommon objCommon, String jobID, String carrierJobID) {
        FxtrnreqEntity example = new FxtrnreqEntity();
        example.setJobID(jobID);
        example.setCarrierJobID(carrierJobID);
        List<Infos.TransferJobInfo> result = customizeSupport.findAll(example).stream().map(data -> {
            Infos.TransferJobInfo transferJobInfo = new Infos.TransferJobInfo();
            transferJobInfo.setCarrierID(ObjectIdentifier.buildWithValue(data.getCarrierID()));
            transferJobInfo.setJobID(data.getJobID());
            transferJobInfo.setCarrierJobID(data.getCarrierJobID());
            transferJobInfo.setTransportType(data.getTransportType());
            transferJobInfo.setZoneType(data.getZoneType());
            transferJobInfo.setN2PurgeFlag(BooleanUtils.convert(data.getN2PurgeFlag()));
            transferJobInfo.setFromMachineID(ObjectIdentifier.buildWithValue(data.getFromMachineID()));
            transferJobInfo.setFromPortID(ObjectIdentifier.buildWithValue(data.getFromPortID()));
            transferJobInfo.setToStockerGroup(data.getToStockerGroup());
            transferJobInfo.setToMachineID(ObjectIdentifier.buildWithValue(data.getToMachineID()));
            transferJobInfo.setToPortID(ObjectIdentifier.buildWithValue(data.getToPortID()));
            transferJobInfo.setExpectedStartTime(data.getExpectedStrtTime());
            transferJobInfo.setExpectedEndTime(data.getExpectedEndTime());
            transferJobInfo.setEstimatedStartTime(data.getEstimateStrtTime());
            transferJobInfo.setEstimatedEndTime(data.getEstimateEndTime());
            transferJobInfo.setMandatoryFlag(BooleanUtils.convert(data.getMandatoryFlag()));
            transferJobInfo.setPriority(data.getPriority());
            transferJobInfo.setJobStatus(data.getJobStatus());
            transferJobInfo.setCarrierJobStatus(data.getCarrierJobStatus());
            transferJobInfo.setTimeStamp(DateUtils.convertToSpecString(data.getTimestamp()));
            return transferJobInfo;
        }).collect(Collectors.toList());
        Validations.check(ArrayUtils.isEmpty(result),msgRetCodeConfig.getMsgRecordNotFound());
        return result;
    }

    @Override
    public void transferJobMod(Infos.ObjCommon objCommon, String jobID, String jobStatus, String carrierJobID, ObjectIdentifier carrierID, String carrierJobStatus, Boolean carrierJobRemoveFlag) {
        if (BooleanUtils.isTrue(carrierJobRemoveFlag)) {
            log.info("carrierJobRemoveFlag == TRUE");
            FxtrnreqEntity example = new FxtrnreqEntity();
            example.setCarrierJobID(carrierJobID);
            customizeSupport.removeNonRuntimeEntityForExample(example);
        } else {
            log.info("carrierJobRemoveFlag != TRUE");
            FxtrnreqEntity example = new FxtrnreqEntity();
            example.setCarrierID(ObjectIdentifier.fetchValue(carrierID));
            example.setJobID(jobID);
            example.setCarrierJobID(carrierJobID);
            List<FxtrnreqEntity> fxtrnreqEntities = customizeSupport.findAll(example);
            Validations.check(ArrayUtils.isEmpty(fxtrnreqEntities), msgRetCodeConfig.getMsgRecordNotFound());
            fxtrnreqEntities.forEach(fxtrnreqEntity -> {
                fxtrnreqEntity.setJobStatus(jobStatus);
                fxtrnreqEntity.setCarrierJobStatus(carrierJobStatus);
                customizeSupport.saveNonRuntimeEntity(fxtrnreqEntity);
            });
        }
    }

}
