package com.fa.cim.tms.service.impl;

import com.fa.cim.tms.common.constant.Constant;
import com.fa.cim.tms.dto.Params;
import com.fa.cim.tms.dto.Results;
import com.fa.cim.tms.manager.IMCSManager;
import com.fa.cim.tms.manager.IOMSManager;
import com.fa.cim.tms.method.ITransferJobMethod;
import com.fa.cim.tms.pojo.Infos;
import com.fa.cim.tms.service.ITransportJobPauseReqService;
import com.fa.cim.tms.utils.ArrayUtils;
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
 * @date: 2020/10/20 15:47
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@Service
public class TransportJobPauseReqService implements ITransportJobPauseReqService {
    @Autowired
    private IMCSManager mcsManager;
    @Autowired
    private IOMSManager omsManager;
    @Autowired
    private ITransferJobMethod transferJobMethod;

    public Results.TransportJobPauseReqResult sxTransportJobPauseReq(Infos.ObjCommon objCommon, Params.TransportJobPauseReqParams transportJobPauseReqParams) {
        Results.TransportJobPauseReqResult result = new Results.TransportJobPauseReqResult();
        /*-----------------------------------------------------------*/
        /*   Send Transfer Data to MCS                               */
        /*-----------------------------------------------------------*/
        log.info("【step1】 - mcsManager.sendTransportJobPauseReq");
        result = mcsManager.sendTransportJobPauseReq(objCommon, transportJobPauseReqParams);
        String inquiryType = Constant.TM_INQUIRY_TYPE_BY_JOB;

        log.info("【step2】 - transferJobMethod.transferJobGet");
        List<Infos.TransferJobInfo> transferJobInfoList = transferJobMethod.transferJobGet(objCommon, inquiryType, null, null, null, Arrays.asList(transportJobPauseReqParams.getJobID()));

        if (ArrayUtils.isNotEmpty(transferJobInfoList)) {
            log.info("founc transferJobInfo");
            /* Report XferJob status to OM */
            log.info("【step3】 - omsManager.sendDurableXferStatusChangeRpt");
            omsManager.sendDurableXferStatusChangeRpt(objCommon, transferJobInfoList, Constant.TM_DURABLE_XFER_JOB_STATUS_UPDATED, "",true);
        }
        return result;
    }

    @Override
    public Results.TransportJobPauseReqResult sxRtmsTransportJobPauseReq(Infos.ObjCommon objCommon, Params.TransportJobPauseReqParams transportJobPauseReqParams) {
        Results.TransportJobPauseReqResult result = new Results.TransportJobPauseReqResult();
        /*-----------------------------------------------------------*/
        /*   Send Transfer Data to MCS                               */
        /*-----------------------------------------------------------*/
        log.info("【step1】 - mcsManager.sendTransportJobPauseReq");
        result = mcsManager.sendTransportJobPauseReq(objCommon, transportJobPauseReqParams);
        String inquiryType = Constant.TM_INQUIRY_TYPE_BY_JOB;

        log.info("【step2】 - transferJobMethod.transferJobGet");
        List<Infos.TransferJobInfo> transferJobInfoList = transferJobMethod.transferJobGet(objCommon, inquiryType, null, null, null, Arrays.asList(transportJobPauseReqParams.getJobID()));

        if (ArrayUtils.isNotEmpty(transferJobInfoList)) {
            log.info("founc transferJobInfo");
            /* Report XferJob status to OM */
            log.info("【step3】 - omsManager.sendDurableXferStatusChangeRpt");
            omsManager.sendDurableXferStatusChangeRpt(objCommon, transferJobInfoList, Constant.TM_DURABLE_XFER_JOB_STATUS_UPDATED, "",false);
        }
        return result;
    }
}
