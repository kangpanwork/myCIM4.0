package com.fa.cim.tms.service.impl;

import com.fa.cim.tms.common.constant.Constant;
import com.fa.cim.tms.dto.Params;
import com.fa.cim.tms.dto.Results;
import com.fa.cim.tms.manager.IMCSManager;
import com.fa.cim.tms.manager.IOMSManager;
import com.fa.cim.tms.method.ITransferJobMethod;
import com.fa.cim.tms.pojo.Infos;
import com.fa.cim.tms.service.ITransportJobResumeReqService;
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
 * @date: 2020/10/21 13:37
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@Service
public class TransportJobResumeReqService implements ITransportJobResumeReqService {
    @Autowired
    private IMCSManager mcsManager;
    @Autowired
    private IOMSManager omsManager;
    @Autowired
    private ITransferJobMethod transferJobMethod;

    public Results.TransportJobResumeReqResult sxTransportJobResumeReq(Infos.ObjCommon objCommon, Params.TransportJobResumeReqParams transportJobResumeReqParams) {
        Results.TransportJobResumeReqResult result = new Results.TransportJobResumeReqResult();
        /*-----------------------------------------------------------*/
        /*   Send Transfer Data to MCS                               */
        /*-----------------------------------------------------------*/
        log.info("【step1】 - mcsManager.sendTransportJobResumeReq");
        result = mcsManager.sendTransportJobResumeReq(objCommon, transportJobResumeReqParams);

        log.info("【step2】 - transferJobMethod.transferJobGet");
        List<Infos.TransferJobInfo> transferJobInfoList = transferJobMethod.transferJobGet(objCommon,
                Constant.TM_INQUIRY_TYPE_BY_JOB,
                null,
                null,
                null,
                Arrays.asList(transportJobResumeReqParams.getJobID()));

        log.info("found CarrierJob!");
        /* Report XferJob status to OMS */
        log.info("【step3】 - omsManager.sendDurableXferStatusChangeRpt");
        omsManager.sendDurableXferStatusChangeRpt(objCommon,
                transferJobInfoList,
                Constant.TM_DURABLE_XFER_JOB_STATUS_UPDATED,
                "",true);
        return result;
    }

    @Override
    public Results.TransportJobResumeReqResult sxRtmsTransportJobResumeReq(Infos.ObjCommon objCommon, Params.TransportJobResumeReqParams transportJobResumeReqParams) {
        Results.TransportJobResumeReqResult result = new Results.TransportJobResumeReqResult();
        /*-----------------------------------------------------------*/
        /*   Send Transfer Data to MCS                               */
        /*-----------------------------------------------------------*/
        log.info("【step1】 - mcsManager.sendTransportJobResumeReq");
        result = mcsManager.sendTransportJobResumeReq(objCommon, transportJobResumeReqParams);

        log.info("【step2】 - transferJobMethod.transferJobGet");
        List<Infos.TransferJobInfo> transferJobInfoList = transferJobMethod.transferJobGet(objCommon,
                Constant.TM_INQUIRY_TYPE_BY_JOB,
                null,
                null,
                null,
                Arrays.asList(transportJobResumeReqParams.getJobID()));

        log.info("found CarrierJob!");
        /* Report XferJob status to OMS */
        log.info("【step3】 - omsManager.sendDurableXferStatusChangeRpt");
        omsManager.sendDurableXferStatusChangeRpt(objCommon,
                transferJobInfoList,
                Constant.TM_DURABLE_XFER_JOB_STATUS_UPDATED,
                "",false);
        return result;
    }
}
