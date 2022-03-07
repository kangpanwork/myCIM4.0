package com.fa.cim.tms.service.impl;

import com.fa.cim.tms.common.constant.Constant;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.tms.config.MsgRetCodeConfig;
import com.fa.cim.tms.dto.Params;
import com.fa.cim.tms.dto.Results;
import com.fa.cim.tms.manager.IMCSManager;
import com.fa.cim.tms.manager.IOMSManager;
import com.fa.cim.tms.method.IEquipmentMethod;
import com.fa.cim.tms.method.ITransferJobMethod;
import com.fa.cim.tms.pojo.Infos;
import com.fa.cim.tms.service.ITransportJobAbortReqService;
import com.fa.cim.tms.utils.BooleanUtils;
import com.fa.cim.tms.utils.Validations;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * description:
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/10/20                               Neyo                create file
 *
 * @author: Neyo
 * @date: 2020/10/20 15:59
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@Service
public class TransportJobAbortReqService implements ITransportJobAbortReqService {
    @Autowired
    private IMCSManager mcsManager;
    @Autowired
    private IOMSManager omsManager;
    @Autowired
    private ITransferJobMethod transferJobMethod;
    @Autowired
    private IEquipmentMethod equipmentMethod;
    @Autowired
    private MsgRetCodeConfig msgRetCodeConfig;

    public Results.TransportJobAbortReqResult sxTransportJobAbortReq(Infos.ObjCommon objCommon, Params.TransportJobAbortReqParams transportJobAbortReqParams) {
        Results.TransportJobAbortReqResult result = new Results.TransportJobAbortReqResult();
        Boolean castTxNoSendWarning = false;
        /*-----------------------------------------------------------*/
        /*   Send Transfer Data to MCS                               */
        /*-----------------------------------------------------------*/
        log.info("【step1】 - mcsManager.sendTransportJobAbortReq");
        result = mcsManager.sendTransportJobAbortReq(objCommon, transportJobAbortReqParams);

        /* Set inquiryType = 'J'        */
        /* Get records from OTXFERREQ    */
        String inquiryType = Constant.TM_INQUIRY_TYPE_BY_JOB;
        log.info("【step2】 - transferJobMethod.transferJobGet");
        List<Infos.TransferJobInfo> transferJobInfoList = transferJobMethod.transferJobGet(objCommon, inquiryType, null, null, null, Arrays.asList(transportJobAbortReqParams.getJobID()));

        /* Check to_location,it isn't for Equipment                        */
        /* If it's for Equipment,send TxCarrierTransferJobEndRpt to OMS    */
        String transferJobStatus = Constant.TM_TRANSFER_JOB_STATUS_XERR;
        log.info("【step3】 - equipmentMethod.checkEqpSendCOMP");
        try {
            equipmentMethod.checkEqpSendCOMP(objCommon, transferJobInfoList, transferJobStatus);
        } catch (ServiceException e) {
            if (Validations.isEquals(msgRetCodeConfig.getMsgOmsCastTxNoSend(), e.getCode())) {
                log.error("oms cast no send or shutdown");
                castTxNoSendWarning = true;
            }else {
                throw e;
            }
        }

        /* Set DeleteType = 'JB'        */
        /* Delete records from OTXFERREQ */
        String deleteType = Constant.TM_TRANSFER_JOB_DELETE_TYPE_JOBID;
        Infos.TransferJobDeleteInfo strTranJobDeleteReq = new Infos.TransferJobDeleteInfo();
        strTranJobDeleteReq.setJobID(transportJobAbortReqParams.getJobID());
        log.info("【step4】 - transferJobMethod.transferJobDel");
        transferJobMethod.transferJobDel(objCommon, deleteType, strTranJobDeleteReq);

        /* Report XferJob status to OMS */
        log.info("【step5】 - omsManager.sendDurableXferStatusChangeRpt");
        omsManager.sendDurableXferStatusChangeRpt(objCommon, transferJobInfoList, Constant.TM_DURABLE_XFER_JOB_STATUS_CANCELLED, "",true);
        Validations.check(BooleanUtils.isTrue(castTxNoSendWarning),result,msgRetCodeConfig.getMsgOmsCastTxNoSend());
        return result;
    }

    @Override
    public Results.TransportJobAbortReqResult sxRtmsTransportJobAbortReq(Infos.ObjCommon objCommon, Params.TransportJobAbortReqParams transportJobAbortReqParams) {
        Results.TransportJobAbortReqResult result = new Results.TransportJobAbortReqResult();
        /*-----------------------------------------------------------*/
        /*   Send Transfer Data to MCS                               */
        /*-----------------------------------------------------------*/
        log.info("【step1】 - mcsManager.sendTransportJobAbortReq");
        result = mcsManager.sendTransportJobAbortReq(objCommon, transportJobAbortReqParams);

        /* Set inquiryType = 'J'        */
        /* Get records from OTXFERREQ    */
        String inquiryType = Constant.TM_INQUIRY_TYPE_BY_JOB;
        log.info("【step2】 - transferJobMethod.transferJobGet");
        List<Infos.TransferJobInfo> transferJobInfoList = transferJobMethod.transferJobGet(objCommon, inquiryType, null, null, null, Arrays.asList(transportJobAbortReqParams.getJobID()));

        /* Check to_location,it isn't for Equipment                        */
        /* If it's for Equipment,send TxCarrierTransferJobEndRpt to OMS    */
        String transferJobStatus = Constant.TM_TRANSFER_JOB_STATUS_XERR;
        log.info("【step3】 - equipmentMethod.rtmsCheckEqpSendCOMP");
        equipmentMethod.rtmsCheckEqpSendCOMP(objCommon, transferJobInfoList, transferJobStatus);

        /* Set DeleteType = 'JB'        */
        /* Delete records from OTXFERREQ */
        String deleteType = Constant.TM_TRANSFER_JOB_DELETE_TYPE_JOBID;
        Infos.TransferJobDeleteInfo strTranJobDeleteReq = new Infos.TransferJobDeleteInfo();
        strTranJobDeleteReq.setJobID(transportJobAbortReqParams.getJobID());
        log.info("【step4】 - transferJobMethod.transferJobDel");
        transferJobMethod.transferJobDel(objCommon, deleteType, strTranJobDeleteReq);

        /* Report XferJob status to OMS */
        log.info("【step5】 - omsManager.sendDurableXferStatusChangeRpt");
        omsManager.sendDurableXferStatusChangeRpt(objCommon, transferJobInfoList, Constant.TM_DURABLE_XFER_JOB_STATUS_CANCELLED, "",false);
        return result;
    }
}
