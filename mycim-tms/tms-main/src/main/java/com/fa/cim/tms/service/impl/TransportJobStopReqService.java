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
import com.fa.cim.tms.service.ITransportJobStopReqService;
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
 * 2020/10/22                               Neyo                create file
 *
 * @author: Neyo
 * @date: 2020/10/22 12:53
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@Service
public class TransportJobStopReqService implements ITransportJobStopReqService {
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

    public Results.TransportJobStopReqResult sxTransportJobStopReq(Infos.ObjCommon objCommon, Params.TransportJobStopReqParams transportJobStopReqParams) {
        Results.TransportJobStopReqResult result = new Results.TransportJobStopReqResult();
        Boolean castTxNoSendWarning = false;
        /*-----------------------------------------------------------*/
        /*   Send Transfer Data to MCS                               */
        /*-----------------------------------------------------------*/
        log.info("【step1】 - mcsManager.sendTransportJobStopReq");
        result = mcsManager.sendTransportJobStopReq(objCommon, transportJobStopReqParams);

        /* Set inquiryType = 'J'        */
        /* Get records from OTXFERREQ    */
        log.info("【step2】 - transferJobMethod.transferJobGet");
        List<Infos.TransferJobInfo> transferJobInfoList = transferJobMethod.transferJobGet(objCommon,
                Constant.TM_INQUIRY_TYPE_BY_JOB,
                null,
                null,
                null,
                Arrays.asList(transportJobStopReqParams.getJobID()));

        /* Check to_location,it isn't for Equipment                        */
        /* If it's for Equipment, sendLotCassetteXferJobCompRpt to OMS     */
        /* Set transferJobStatus = "XERR"                                  */
        log.info("【step3】 - equipmentMethod.checkEqpSendCOMP");
        try {
            equipmentMethod.checkEqpSendCOMP(objCommon,
                    transferJobInfoList,
                    Constant.TM_TRANSFER_JOB_STATUS_XERR);
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
        log.info("【step4】 - transferJobMethod.transferJobDel");
        Infos.TransferJobDeleteInfo transferJobDeleteInfo = new Infos.TransferJobDeleteInfo();
        transferJobDeleteInfo.setJobID(transportJobStopReqParams.getJobID());
        transferJobMethod.transferJobDel(objCommon,
                Constant.TM_TRANSFER_JOB_DELETE_TYPE_JOBID,
                transferJobDeleteInfo);

        /* Report XferJob status to OMS */
        log.info("【step5】 - omsManager.sendDurableXferStatusChangeRpt");
        omsManager.sendDurableXferStatusChangeRpt(objCommon,
                transferJobInfoList,
                Constant.TM_DURABLE_XFER_JOB_STATUS_CANCELLED,
                "",true);

        Validations.check(BooleanUtils.isTrue(castTxNoSendWarning),result,msgRetCodeConfig.getMsgOmsCastTxNoSend());
        return result;
    }

    @Override
    public Results.TransportJobStopReqResult sxRtmsTransportJobStopReq(Infos.ObjCommon objCommon, Params.TransportJobStopReqParams transportJobStopReqParams) {
        Results.TransportJobStopReqResult result = new Results.TransportJobStopReqResult();
        /*-----------------------------------------------------------*/
        /*   Send Transfer Data to MCS                               */
        /*-----------------------------------------------------------*/
        log.info("【step1】 - mcsManager.sendTransportJobStopReq");
        result = mcsManager.sendTransportJobStopReq(objCommon, transportJobStopReqParams);

        /* Set inquiryType = 'J'        */
        /* Get records from OTXFERREQ    */
        log.info("【step2】 - transferJobMethod.transferJobGet");
        List<Infos.TransferJobInfo> transferJobInfoList = transferJobMethod.transferJobGet(objCommon,
                Constant.TM_INQUIRY_TYPE_BY_JOB,
                null,
                null,
                null,
                Arrays.asList(transportJobStopReqParams.getJobID()));

        /* Check to_location,it isn't for Equipment                        */
        /* If it's for Equipment, sendLotCassetteXferJobCompRpt to OMS     */
        /* Set transferJobStatus = "XERR"                                  */
        log.info("【step3】 - equipmentMethod.rtmsCheckEqpSendCOMP");
        equipmentMethod.rtmsCheckEqpSendCOMP(objCommon,
                transferJobInfoList,
                Constant.TM_TRANSFER_JOB_STATUS_XERR);


        /* Set DeleteType = 'JB'        */
        /* Delete records from OTXFERREQ */
        log.info("【step4】 - transferJobMethod.transferJobDel");
        Infos.TransferJobDeleteInfo transferJobDeleteInfo = new Infos.TransferJobDeleteInfo();
        transferJobDeleteInfo.setJobID(transportJobStopReqParams.getJobID());
        transferJobMethod.transferJobDel(objCommon,
                Constant.TM_TRANSFER_JOB_DELETE_TYPE_JOBID,
                transferJobDeleteInfo);

        /* Report XferJob status to OMS */
        log.info("【step5】 - omsManager.sendDurableXferStatusChangeRpt");
        omsManager.sendDurableXferStatusChangeRpt(objCommon,
                transferJobInfoList,
                Constant.TM_DURABLE_XFER_JOB_STATUS_CANCELLED,
                "",false);

        return result;
    }
}
