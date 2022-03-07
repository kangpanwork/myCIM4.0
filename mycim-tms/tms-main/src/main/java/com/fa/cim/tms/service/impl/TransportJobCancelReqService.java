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
import com.fa.cim.tms.service.ITransportJobCancelReqService;
import com.fa.cim.tms.utils.ArrayUtils;
import com.fa.cim.tms.utils.BooleanUtils;
import com.fa.cim.tms.utils.StringUtils;
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
 * @date: 2020/10/20 16:37
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@Service
public class TransportJobCancelReqService implements ITransportJobCancelReqService {
    @Autowired
    private IOMSManager omsManager;
    @Autowired
    private IMCSManager mcsManager;
    @Autowired
    private IEnvMethod envMethod;
    @Autowired
    private MsgRetCodeConfig msgRetCodeConfig;
    @Autowired
    private ITransferJobMethod transferJobMethod;
    @Autowired
    private IEquipmentMethod equipmentMethod;

    public Results.TransportJobCancelReqResult sxTransportJobCancelReq(Infos.ObjCommon strObjCommonIn, Params.TransportJobCancelReqParams transportJobCancelReqParams) {
        Results.TransportJobCancelReqResult result = new Results.TransportJobCancelReqResult();

        Boolean castTxNoSendWarning = false;

        String specialRc = envMethod.getEnvFromOmsDB(EnvCodeEnum.OM_FORCE_DELETE_RC.getValue());
        boolean rcOkFlag = false;
        String ignoreFlag = envMethod.getEnvFromOmsDB(EnvCodeEnum.OM_IGNORE_DELETE_RC.getValue());//"ON"
        log.info("env OM_FORCE_DELETE_RC: {}", specialRc);
        log.info("env OM_IGNORE_DELETE_RC: {}", ignoreFlag);

        Params.TransportJobCancelReqParams tempTranJobCancelReq = new Params.TransportJobCancelReqParams();
        tempTranJobCancelReq.setJobID(transportJobCancelReqParams.getJobID());
        List<Infos.AmhsCarrierJob> tempCarrierJobList = new ArrayList<>();
        tempTranJobCancelReq.setCarrierJobData(tempCarrierJobList);

        //carrierIDList
        List<ObjectIdentifier> carrierIDList = new ArrayList<>();
        //transferJobDeleteReq
        Infos.TransferJobDeleteInfo transferJobDeleteInfo = new Infos.TransferJobDeleteInfo();
        List<Infos.CarrierJobInfo> carrierJobInfoList = new ArrayList<>();
        transferJobDeleteInfo.setCarrierJobData(carrierJobInfoList);
        if (ArrayUtils.isNotEmpty(transportJobCancelReqParams.getCarrierJobData())) {
            for (Infos.AmhsCarrierJob amhsCarrierJob : transportJobCancelReqParams.getCarrierJobData()) {
                Infos.AmhsCarrierJob tempAmhsCarrierJob = new Infos.AmhsCarrierJob();
                Infos.CarrierJobInfo carrierJobInfo = new Infos.CarrierJobInfo();
                carrierJobInfo.setCarrierJobID(amhsCarrierJob.getCarrierJobID());
                carrierJobInfo.setCarrierID(amhsCarrierJob.getCarrierID());
                BeanUtils.copyProperties(amhsCarrierJob, tempAmhsCarrierJob);
                tempCarrierJobList.add(tempAmhsCarrierJob);
                carrierIDList.add(amhsCarrierJob.getCarrierID());
                carrierJobInfoList.add(carrierJobInfo);
            }
        }
        /*-----------------------------------------------------------*/
        /*   Send Transfer Data to MCS                               */
        /*-----------------------------------------------------------*/
        Params.TransportJobCancelReqParams amhsTranJobCancelReq = new Params.TransportJobCancelReqParams();
        amhsTranJobCancelReq.setJobID(transportJobCancelReqParams.getJobID());
        amhsTranJobCancelReq.setCarrierJobData(transportJobCancelReqParams.getCarrierJobData());
        log.info("【step1】 - mcsManager.sendTransportJobCancelReq");
        try {
            result = mcsManager.sendTransportJobCancelReq(strObjCommonIn, amhsTranJobCancelReq);
        } catch (ServiceException e) {
            //Integration error
            if (Validations.isEquals(e.getCode(),msgRetCodeConfig.getMsgGrpcTimeOut())) throw e;
            if (!Constant.TM_ON.equals(ignoreFlag)) {
                if (!Validations.isEquals(msgRetCodeConfig.getMsgUnknownJobid(), e.getCode())
                        && !Validations.isEquals(msgRetCodeConfig.getMsgMcsUknownJobid(), e.getCode())
                        && !StringUtils.equals(specialRc, e.getCode().toString())) {
                    throw e;
                }
            }
            if (Constant.TM_ON.equals(ignoreFlag)
                    || Validations.isEquals(msgRetCodeConfig.getMsgUnknownJobid(), e.getCode())
                    || Validations.isEquals(msgRetCodeConfig.getMsgMcsUknownJobid(), e.getCode())) {
                log.info("ignoreFlag: {}", ignoreFlag);
                rcOkFlag = true;
            }
        }

        /* Set inquiryType = 'C'        */
        /* Get records from OTXFERREQ    */
        String inquiryType = Constant.TM_INQUIRY_TYPE_BY_CARRIER;
        log.info("【step2】 - transferJobMethod.transferJobGet");
        List<Infos.TransferJobInfo> transferJobInfoList = null;
        try {
            transferJobInfoList = transferJobMethod.transferJobGet(strObjCommonIn, inquiryType, carrierIDList, null, null, null);
        } catch (Exception e) {
            if (BooleanUtils.isFalse(rcOkFlag)) {
                throw e;
            }
        }

        /* Check to_location,it isn't for Equipment                         */
        /* If it's for Equipment,send TxLotCassetteXferJobCompRpt to OMS    */
        /* Set transferJobStatus = "XERR"                                   */
        String transferJobStatus = Constant.TM_TRANSFER_JOB_STATUS_XERR;
        log.info("【step3】 - equipmentMethod.checkEqpSendCOMP");
        try {
            equipmentMethod.checkEqpSendCOMP(strObjCommonIn, transferJobInfoList, transferJobStatus);
        } catch (ServiceException e) {
            if (Validations.isEquals(msgRetCodeConfig.getMsgOmsCastTxNoSend(), e.getCode())) {
                log.error("oms cast no send or shutdown");
                castTxNoSendWarning = true;
            }else {
                throw e;
            }
        }

        /* Set DeleteType = 'CJ'        */
        /* Delete records from OTXFERREQ */
        String deleteType = Constant.TM_TRANSFER_JOB_DELETE_TYPE_CARRIERJOBID;
        log.info("【step3】 - transferJobMethod.transferJobDel");
        transferJobMethod.transferJobDel(strObjCommonIn, deleteType, transferJobDeleteInfo);

        /* Report XferJob status to OMS */
        log.info("【step4】 - omsManager.sendDurableXferStatusChangeRpt");
        omsManager.sendDurableXferStatusChangeRpt(strObjCommonIn, transferJobInfoList, Constant.TM_DURABLE_XFER_JOB_STATUS_CANCELLED, "",true);
        Validations.check(BooleanUtils.isTrue(castTxNoSendWarning),result,msgRetCodeConfig.getMsgOmsCastTxNoSend());
        return result;
    }

    @Override
    public Results.TransportJobCancelReqResult sxRtmsTransportJobCancelReq(Infos.ObjCommon strObjCommonIn, Params.TransportJobCancelReqParams transportJobCancelReqParams) {
        Results.TransportJobCancelReqResult result = new Results.TransportJobCancelReqResult();

        String specialRc = envMethod.getEnvFromOmsDB(EnvCodeEnum.OM_FORCE_DELETE_RC.getValue());
        boolean rcOkFlag = false;
        String ignoreFlag = envMethod.getEnvFromOmsDB(EnvCodeEnum.OM_IGNORE_DELETE_RC.getValue());//"ON"
        log.info("env OM_FORCE_DELETE_RC: {}", specialRc);
        log.info("env OM_IGNORE_DELETE_RC: {}", ignoreFlag);

        Params.TransportJobCancelReqParams tempTranJobCancelReq = new Params.TransportJobCancelReqParams();
        tempTranJobCancelReq.setJobID(transportJobCancelReqParams.getJobID());
        List<Infos.AmhsCarrierJob> tempCarrierJobList = new ArrayList<>();
        tempTranJobCancelReq.setCarrierJobData(tempCarrierJobList);

        //carrierIDList
        List<ObjectIdentifier> carrierIDList = new ArrayList<>();
        //transferJobDeleteReq
        Infos.TransferJobDeleteInfo transferJobDeleteInfo = new Infos.TransferJobDeleteInfo();
        List<Infos.CarrierJobInfo> carrierJobInfoList = new ArrayList<>();
        transferJobDeleteInfo.setCarrierJobData(carrierJobInfoList);
        if (ArrayUtils.isNotEmpty(transportJobCancelReqParams.getCarrierJobData())) {
            for (Infos.AmhsCarrierJob amhsCarrierJob : transportJobCancelReqParams.getCarrierJobData()) {
                Infos.AmhsCarrierJob tempAmhsCarrierJob = new Infos.AmhsCarrierJob();
                Infos.CarrierJobInfo carrierJobInfo = new Infos.CarrierJobInfo();
                carrierJobInfo.setCarrierJobID(amhsCarrierJob.getCarrierJobID());
                carrierJobInfo.setCarrierID(amhsCarrierJob.getCarrierID());
                BeanUtils.copyProperties(amhsCarrierJob, tempAmhsCarrierJob);
                tempCarrierJobList.add(tempAmhsCarrierJob);
                carrierIDList.add(amhsCarrierJob.getCarrierID());
                carrierJobInfoList.add(carrierJobInfo);
            }
        }
        /*-----------------------------------------------------------*/
        /*   Send Transfer Data to MCS                               */
        /*-----------------------------------------------------------*/
        Params.TransportJobCancelReqParams amhsTranJobCancelReq = new Params.TransportJobCancelReqParams();
        amhsTranJobCancelReq.setJobID(transportJobCancelReqParams.getJobID());
        amhsTranJobCancelReq.setCarrierJobData(transportJobCancelReqParams.getCarrierJobData());
        log.info("【step1】 - mcsManager.sendTransportJobCancelReq");
        try {
            result = mcsManager.sendTransportJobCancelReq(strObjCommonIn, amhsTranJobCancelReq);
        } catch (ServiceException e) {
            //Integration error
            if (Validations.isEquals(e.getCode(),msgRetCodeConfig.getMsgGrpcTimeOut())) throw e;
            if (!Constant.TM_ON.equals(ignoreFlag)) {
                if (!Validations.isEquals(msgRetCodeConfig.getMsgUnknownJobid(), e.getCode())
                        && !Validations.isEquals(msgRetCodeConfig.getMsgMcsUknownJobid(), e.getCode())
                        && !StringUtils.equals(specialRc, e.getCode().toString())) {
                    throw e;
                }
            }
            if (Constant.TM_ON.equals(ignoreFlag)
                    || Validations.isEquals(msgRetCodeConfig.getMsgUnknownJobid(), e.getCode())
                    || Validations.isEquals(msgRetCodeConfig.getMsgMcsUknownJobid(), e.getCode())) {
                log.info("ignoreFlag: {}", ignoreFlag);
                rcOkFlag = true;
            }
        }

        /* Set inquiryType = 'C'        */
        /* Get records from OTXFERREQ    */
        String inquiryType = Constant.TM_INQUIRY_TYPE_BY_CARRIER;
        log.info("【step2】 - transferJobMethod.transferJobGet");
        List<Infos.TransferJobInfo> transferJobInfoList = null;
        try {
            transferJobInfoList = transferJobMethod.transferJobGet(strObjCommonIn, inquiryType, carrierIDList, null, null, null);
        } catch (Exception e) {
            if (BooleanUtils.isFalse(rcOkFlag)) {
                throw e;
            }
        }

        /* Check to_location,it isn't for Equipment                         */
        /* If it's for Equipment,send TxLotCassetteXferJobCompRpt to OMS    */
        /* Set transferJobStatus = "XERR"                                   */
        String transferJobStatus = Constant.TM_TRANSFER_JOB_STATUS_XERR;
        log.info("【step3】 - equipmentMethod.rtmsCheckEqpSendCOMP");

        equipmentMethod.rtmsCheckEqpSendCOMP(strObjCommonIn, transferJobInfoList, transferJobStatus);

        /* Set DeleteType = 'CJ'        */
        /* Delete records from OTXFERREQ */
        String deleteType = Constant.TM_TRANSFER_JOB_DELETE_TYPE_CARRIERJOBID;
        log.info("【step3】 - transferJobMethod.transferJobDel");
        transferJobMethod.transferJobDel(strObjCommonIn, deleteType, transferJobDeleteInfo);

        /* Report XferJob status to OMS */
        log.info("【step4】 - omsManager.sendDurableXferStatusChangeRpt");
        omsManager.sendDurableXferStatusChangeRpt(strObjCommonIn, transferJobInfoList, Constant.TM_DURABLE_XFER_JOB_STATUS_CANCELLED, "",false);
        return result;
    }
}