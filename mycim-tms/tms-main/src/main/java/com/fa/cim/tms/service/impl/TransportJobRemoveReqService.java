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
import com.fa.cim.tms.service.ITransportJobRemoveReqService;
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
 * 2020/10/21                               Neyo                create file
 *
 * @author: Neyo
 * @date: 2020/10/21 13:14
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@Service
public class TransportJobRemoveReqService implements ITransportJobRemoveReqService {
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

    public Results.TransportJobRemoveReqResult sxTransportJobRemoveReq(Infos.ObjCommon objCommon, Params.TransportJobRemoveReqParams transportJobRemoveReqParams) {
        Results.TransportJobRemoveReqResult result = new Results.TransportJobRemoveReqResult();
        Boolean castTxNoSendWarning = false;
        /*-----------------------------------------------------------*/
        /*   Send Transfer Data to MCS                             */
        /*-----------------------------------------------------------*/
        log.info("【step1】 - mcsManager.sendTransportJobRemoveReq");
        result = mcsManager.sendTransportJobRemoveReq(objCommon, transportJobRemoveReqParams);

        /* Set inquiryType = 'J'        */
        /* Get records from OTXFERREQ    */
        log.info("【step2】 - transferJobMethod.transferJobGet");
        String inquiryType = Constant.TM_INQUIRY_TYPE_BY_JOB;
        List<Infos.TransferJobInfo> transferJobInfoList = transferJobMethod.transferJobGet(objCommon,
                inquiryType,
                null,
                null,
                null,
                Arrays.asList(transportJobRemoveReqParams.getJobID()));

        /* Check to_location,it isn't for Equipment                        */
        /* If it's for Equipment,send TxLotCassetteXferJobCompRpt to OMS   */
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
        Infos.TransferJobDeleteInfo transferJobDeleteInfo = new Infos.TransferJobDeleteInfo();
        transferJobDeleteInfo.setJobID(transportJobRemoveReqParams.getJobID());
        log.info("【step4】 - transferJobMethod.transferJobDel");
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
    public Results.TransportJobRemoveReqResult sxRtmsTransportJobRemoveReq(Infos.ObjCommon objCommon, Params.TransportJobRemoveReqParams transportJobRemoveReqParams) {
        Results.TransportJobRemoveReqResult result = new Results.TransportJobRemoveReqResult();
        /*-----------------------------------------------------------*/
        /*   Send Transfer Data to MCS                             */
        /*-----------------------------------------------------------*/
        log.info("【step1】 - mcsManager.sendTransportJobRemoveReq");
        result = mcsManager.sendTransportJobRemoveReq(objCommon, transportJobRemoveReqParams);

        /* Set inquiryType = 'J'        */
        /* Get records from OTXFERREQ    */
        log.info("【step2】 - transferJobMethod.transferJobGet");
        String inquiryType = Constant.TM_INQUIRY_TYPE_BY_JOB;
        List<Infos.TransferJobInfo> transferJobInfoList = transferJobMethod.transferJobGet(objCommon,
                inquiryType,
                null,
                null,
                null,
                Arrays.asList(transportJobRemoveReqParams.getJobID()));

        /* Check to_location,it isn't for Equipment                        */
        /* If it's for Equipment,send TxLotCassetteXferJobCompRpt to OMS   */
        log.info("【step3】 - equipmentMethod.rtmsCheckEqpSendCOMP");
        equipmentMethod.rtmsCheckEqpSendCOMP(objCommon,
                    transferJobInfoList,
                    Constant.TM_TRANSFER_JOB_STATUS_XERR);
        /* Set DeleteType = 'JB'        */
        /* Delete records from OTXFERREQ */
        Infos.TransferJobDeleteInfo transferJobDeleteInfo = new Infos.TransferJobDeleteInfo();
        transferJobDeleteInfo.setJobID(transportJobRemoveReqParams.getJobID());
        log.info("【step4】 - transferJobMethod.transferJobDel");
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
