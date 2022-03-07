package com.fa.cim.tms.method.impl;

import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.tms.common.constant.Constant;
import com.fa.cim.tms.config.MsgRetCodeConfig;
import com.fa.cim.tms.dto.Results;
import com.fa.cim.tms.manager.IOMSManager;
import com.fa.cim.tms.method.IEquipmentMethod;
import com.fa.cim.tms.pojo.Infos;
import com.fa.cim.tms.pojo.ObjectIdentifier;
import com.fa.cim.tms.support.CustomizeSupport;
import com.fa.cim.tms.utils.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * description:
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/10/14                              Neyo                create file
 *
 * @author: Neyo
 * @date: 2020/10/14 17:54
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Component
@Slf4j
public class EquipmentMethodImpl implements IEquipmentMethod {

    @Autowired
    private CustomizeSupport customizeSupport;
    @Autowired
    private IOMSManager omsManager;
    @Autowired
    private MsgRetCodeConfig msgRetCodeConfig;


    @Override
    public void checkEqpSendCOMPM3(Infos.ObjCommon strObjCommonIn, List<Infos.TransferJobInfo> seqTransferJobInfo, String transferJobStatus) {
        List<Infos.XferJobComp> strXferJob = new ArrayList<>();
        List<String> seqJobID = new ArrayList<>();
        List<String> seqCarrierJobID = new ArrayList<>();
        AtomicReference<String> serverName = new AtomicReference<>();
        AtomicReference<Boolean> castTxNoSendFlag = new AtomicReference<>(false);
        Optional.of(seqTransferJobInfo).ifPresent(list -> list.forEach(data -> {
            if (ObjectUtils.isNotEmptyWithValue(data.getToPortID()) && ObjectUtils.isNotEmptyWithValue(data.getToMachineID())) {
                log.info("toPortID != NULL or BLANK");
                log.info("toPortID: {}", ObjectIdentifier.fetchValue(data.getToPortID()));
                log.info("toMachineID: {}", ObjectIdentifier.fetchValue(data.getToMachineID()));
                List<Object[]> queryList = customizeSupport.query("SELECT EQP_ID,EAP_HOST FROM OMEQP WHERE EQP_ID = ?1", ObjectIdentifier.fetchValue(data.getToMachineID()));
                if (ArrayUtils.isNotEmpty(queryList)) {
                    /* make TxLotCassetteXferJobCompRpt data */
                    serverName.set(StringUtils.toString(queryList.get(0)[1]));
                    Infos.XferJobComp xferJob = new Infos.XferJobComp();
                    xferJob.setCarrierID(data.getCarrierID());
                    xferJob.setToMachineID(data.getToMachineID());
                    xferJob.setToPortID(data.getToPortID());
                    xferJob.setTransferJobStatus(transferJobStatus);
                    strXferJob.add(xferJob);
                    seqJobID.add(data.getJobID());
                    seqCarrierJobID.add(data.getCarrierJobID());
                }

                Infos.RsvCanLotCarrier rsvCanLotCarrier = new Infos.RsvCanLotCarrier();
                rsvCanLotCarrier.setLotID(ObjectIdentifier.buildWithValue(""));
                rsvCanLotCarrier.setCarrierID(data.getCarrierID());
                rsvCanLotCarrier.setForEquipmentID(ObjectIdentifier.buildWithValue(""));

                log.info("【step1】 - omsManager.sendReserveCancelReq");
                try {
                    omsManager.sendReserveCancelReq(strObjCommonIn, Arrays.asList(rsvCanLotCarrier), null);
                } catch (ServiceException e) {
                    if (Validations.isEquals(msgRetCodeConfig.getMsgOmsCastTxNoSend(), e.getCode())) {
                        log.error("oms cast no send or shutdown");
                        castTxNoSendFlag.set(true);
                    }
                }
            }
        }));

        if (ArrayUtils.isNotEmpty(strXferJob) && StringUtils.isNotEmpty(serverName.get())) {
            log.info("call Oms sendLotCassetteXferJobCompRpt");
            try {
                omsManager.sendLotCassetteXferJobCompRpt(strObjCommonIn, strXferJob, null, seqJobID, seqCarrierJobID);
            } catch (ServiceException e) {
                if (Validations.isEquals(msgRetCodeConfig.getMsgOmsCastTxNoSend(), e.getCode()) || Validations.isEquals(msgRetCodeConfig.getMsgEapNoResponse(),e.getCode())) {
                    log.error("oms cast no send or shutdown");
                    castTxNoSendFlag.set(true);
                }
            }
        }
        Validations.check(BooleanUtils.isTrue(castTxNoSendFlag.get()),msgRetCodeConfig.getMsgOmsCastTxNoSend());
    }

    @Override
    public void checkEqpSendCOMP(Infos.ObjCommon objCommon, List<Infos.TransferJobInfo> transferJobGetOut, String transferJobStatus) {
        List<Infos.XferJobComp> xferJob = new ArrayList<>();
        List seqJobID = new ArrayList<>();
        List seqCarrierJobID = new ArrayList<>();
        /* Cusor Condition is as following: */
        AtomicReference<String> serverName = new AtomicReference<>();
        AtomicReference<Boolean> castTxNoSendFlag = new AtomicReference<>(false);
        Optional.ofNullable(transferJobGetOut).ifPresent(list -> list.forEach(data -> {
            if (ObjectUtils.isNotEmptyWithValue(data.getToPortID()) && ObjectUtils.isNotEmptyWithValue(data.getToMachineID())) {
                log.info("toPortID: {}", ObjectIdentifier.fetchValue(data.getToPortID()));
                log.info("toMachineID: {}", ObjectIdentifier.fetchValue(data.getToMachineID()));
                log.info("equipmentID; {}", ObjectIdentifier.fetchValue(data.getToMachineID()));
                log.info("select EQP and ServerName Exsit");
                List<Object[]> equipmentDOList = customizeSupport.query("SELECT EQP_ID,EAP_HOST FROM OMEQP WHERE EQP_ID = ?", ObjectIdentifier.fetchValue(data.getToMachineID()));
                if (ArrayUtils.isNotEmpty(equipmentDOList)) {
                    /* make TxLotCassetteXferJobCompRpt data */
                    serverName.set(StringUtils.toString(equipmentDOList.get(0)[1]));
                    Infos.XferJobComp xferJobComp = new Infos.XferJobComp();
                    xferJobComp.setCarrierID(data.getCarrierID());
                    xferJobComp.setToMachineID(data.getToMachineID());
                    xferJobComp.setToPortID(data.getToPortID());
                    xferJobComp.setTransferJobStatus(transferJobStatus);
                    xferJob.add(xferJobComp);
                }
            } else {
                log.info("toPortID is NULL or BLANK");
                log.info("equipmentID; {}", ObjectIdentifier.fetchValue(data.getToMachineID()));
                Object resultQuery = customizeSupport.oneResultQuery("SELECT EQP_ID FROM OMEQP WHERE EQP_ID = ?", ObjectIdentifier.fetchValue(data.getToMachineID()));
                if (ObjectUtils.isEmpty(resultQuery)) {
                    /* make TxLotCassetteXferJobCompRpt data */
                    Infos.RsvCanLotCarrier rsvCanLotCarrier = new Infos.RsvCanLotCarrier();
                    rsvCanLotCarrier.setLotID(ObjectIdentifier.buildWithValue(""));
                    rsvCanLotCarrier.setCarrierID(data.getCarrierID());
                    rsvCanLotCarrier.setForEquipmentID(ObjectIdentifier.buildWithValue(""));
                    log.info("call Oms sendReserveCancelReq");
                    try {
                        omsManager.sendReserveCancelReq(objCommon, Arrays.asList(rsvCanLotCarrier), null);
                    } catch (ServiceException e) {
                        if (Validations.isEquals(msgRetCodeConfig.getMsgOmsCastTxNoSend(), e.getCode())) {
                            log.error("oms cast no send or shutdown");
                            castTxNoSendFlag.set(true);
                        }
                    }
                }
            }
        }));

        if (ArrayUtils.isNotEmpty(xferJob) && StringUtils.isNotEmpty(serverName.get())) {
            log.info("call Oms sendLotCassetteXferJobCompRpt");
            try {
                omsManager.sendLotCassetteXferJobCompRpt(objCommon, xferJob, "", seqJobID, seqCarrierJobID);
            } catch (ServiceException e) {
                if (Validations.isEquals(msgRetCodeConfig.getMsgOmsCastTxNoSend(), e.getCode()) || Validations.isEquals(msgRetCodeConfig.getMsgEapNoResponse(),e.getCode())) {
                    log.error("oms cast no send or shutdown");
                    castTxNoSendFlag.set(true);
                }
            }
        }
        Validations.check(BooleanUtils.isTrue(castTxNoSendFlag.get()),msgRetCodeConfig.getMsgOmsCastTxNoSend());
    }

    @Override
    public void checkEqpTransfer(Infos.ObjCommon objCommon, ObjectIdentifier toMachineID,Boolean tmsFlag) {
        Object resultQuery = customizeSupport.oneResultQuery("SELECT EQP_ID FROM OMEQP WHERE EQP_ID = ?1", ObjectIdentifier.fetchValue(toMachineID));
        if (BooleanUtils.isTrue(tmsFlag)){
            Validations.check(null == resultQuery, msgRetCodeConfig.getMsgRecordNotFound());
        }else {
            if (null == resultQuery){
                Object oneResultQuery = customizeSupport.oneResultQuery("SELECT STOCKER_ID FROM OMSTOCKER WHERE STOCKER_ID = ?1 AND STOCKER_TYPE = ?2", ObjectIdentifier.fetchValue(toMachineID), Constant.TM_STOCKER_TYPE_BARERETICLE);
                Validations.check(null == oneResultQuery,msgRetCodeConfig.getMsgRecordNotFound());
            }
        }
    }

    @Override
    public void rtmsCheckEqpSendCOMP(Infos.ObjCommon objCommon, List<Infos.TransferJobInfo> transferJobGetOut, String transferJobStatus) {
        List<Infos.ReticlePodXferJobCompInfo> strXferJob = new ArrayList<>();
        List seqJobID = new ArrayList<>();
        List seqCarrierJobID = new ArrayList<>();
        Optional.ofNullable(transferJobGetOut).ifPresent(list -> list.forEach(data -> {
            Infos.ReticlePodXferJobCompInfo xferReticlePod = new Infos.ReticlePodXferJobCompInfo();
            xferReticlePod.setReticlePodID(data.getCarrierID());
            xferReticlePod.setToMachineID(data.getToMachineID());
            xferReticlePod.setToPortID(data.getToPortID());
            String tmpTransferJobStatus = Constant.TM_TRANSFER_JOB_STATUS_XCMP;
            if (Constant.TM_TRANSFER_JOB_STATUS_THREE.equals(data.getJobStatus()) && Constant.TM_CARRIER_JOB_STATUS.equals(data.getCarrierJobStatus())){
                tmpTransferJobStatus = Constant.TM_TRANSFER_JOB_STATUS_XERR;
            }
            xferReticlePod.setTransferJobStatus(tmpTransferJobStatus);
            strXferJob.add(xferReticlePod);
            seqJobID.add(data.getJobID());
            seqCarrierJobID.add(data.getCarrierJobID());

            log.info("【step1】 - omsManager.sendReticlePodXferJobCompRpt");
            try {
                Results.ReticlePodXferCompRptResult reticlePodTransferJobCompRptResult = omsManager.sendReticlePodXferJobCompRpt(objCommon,
                        strXferJob,
                        null,
                        seqJobID,
                        seqCarrierJobID
                        );
            } catch (ServiceException e) {
                //do Noting
            }
        }));
    }

    @Override
    public void rtmsCheckEqpSendCOMPM3(Infos.ObjCommon objCommon, List<Infos.TransferJobInfo> transferJobGetOut, String transferJobStatus) {
        List<Infos.ReticlePodXferJobCompInfo> strXferJob = new ArrayList<>();
        List seqJobID = new ArrayList<>();
        List seqCarrierJobID = new ArrayList<>();
        Optional.ofNullable(transferJobGetOut).ifPresent(list -> list.forEach(data -> {
            Infos.ReticlePodXferJobCompInfo xferReticlePod = new Infos.ReticlePodXferJobCompInfo();
            xferReticlePod.setReticlePodID(data.getCarrierID());
            xferReticlePod.setToMachineID(data.getToMachineID());
            xferReticlePod.setToPortID(data.getToPortID());
            String tmpTransferJobStatus = Constant.TM_TRANSFER_JOB_STATUS_XCMP;
            if (Constant.TM_TRANSFER_JOB_STATUS_THREE.equals(data.getJobStatus()) && Constant.TM_CARRIER_JOB_STATUS.equals(data.getCarrierJobStatus())){
                tmpTransferJobStatus = Constant.TM_TRANSFER_JOB_STATUS_XERR;
            }
            xferReticlePod.setTransferJobStatus(tmpTransferJobStatus);
            strXferJob.add(xferReticlePod);
            seqJobID.add(data.getJobID());
            seqCarrierJobID.add(data.getCarrierJobID());

            log.info("【step1】 - omsManager.sendReticlePodXferJobCompRpt");
            try {
                Results.ReticlePodXferCompRptResult reticlePodTransferJobCompRptResult = omsManager.sendReticlePodXferJobCompRpt(objCommon,
                        strXferJob,
                        null,
                        seqJobID,
                        seqCarrierJobID
                );
            } catch (ServiceException e) {
                //do Noting
            }
        }));
    }

    @Override
    public Infos.MachineTypeGetDR machineTypeGetDR(Infos.ObjCommon objCommon, ObjectIdentifier currMachineID) {
        Infos.MachineTypeGetDR result = new Infos.MachineTypeGetDR();
        //=========================================================================
        // Get reticle list into output structure
        //=========================================================================
        Object[] queryResult = customizeSupport.queryOne("SELECT EQP_ID,EQP_GRP_ID,BAY_ID FROM OMEQP WHERE EQP_ID = ?1", ObjectIdentifier.fetchValue(currMachineID));
        if (null != queryResult){
            result.setBStorageMachineFlag(false);
            result.setEquipmentID(ObjectIdentifier.buildWithValue(StringUtils.toString(queryResult[0])));
            result.setEquipmentType(StringUtils.toString(queryResult[1]));
            result.setAreaID(ObjectIdentifier.buildWithValue(StringUtils.toString(queryResult[2])));
        }else {
            Object[] queryResultStk = customizeSupport.queryOne("SELECT STOCKER_ID,STOCKER_TYPE,BAY_ID FROM OMSTOCKER WHERE STOCKER_ID = ?1", ObjectIdentifier.fetchValue(currMachineID));
            if (null != queryResultStk){
                result.setBStorageMachineFlag(true);
                result.setStockerID(ObjectIdentifier.buildWithValue(StringUtils.toString(queryResultStk[0])));
                result.setStockerType(StringUtils.toString(queryResultStk[1]));
                result.setAreaID(ObjectIdentifier.buildWithValue(StringUtils.toString(queryResultStk[2])));
            }else {
                Validations.check(true,msgRetCodeConfig.getMsgNotFoundStocker());
            }
        }
        return result;
    }
}
