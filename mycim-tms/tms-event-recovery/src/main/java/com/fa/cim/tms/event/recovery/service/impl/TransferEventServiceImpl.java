package com.fa.cim.tms.event.recovery.service.impl;

import com.fa.cim.tms.event.recovery.constant.Constant;
import com.fa.cim.tms.event.recovery.dto.OMSParams;
import com.fa.cim.tms.event.recovery.manager.IOMSManager;
import com.fa.cim.tms.event.recovery.pojo.Infos;
import com.fa.cim.tms.event.recovery.service.ITransferEventService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;

/**
 * description:
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/11/2                              Neyo                create file
 *
 * @author: Neyo
 * @date: 2020/11/2 16:25
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@Service
public class TransferEventServiceImpl implements ITransferEventService {

    @Autowired
    private IOMSManager omsManager;


    @Override
    public void tmsXferJobEventRetry(Infos.ObjCommon objCommon, Infos.XferJobEventQueData xferJobEventQueData) {
        OMSParams.DurableTransferJobStatusRptParams durableTransferJobStatusRptParams = new OMSParams.DurableTransferJobStatusRptParams();
        durableTransferJobStatusRptParams.setDurableType(Constant.TM_DURABLE_TYPE_CARRIER);
        durableTransferJobStatusRptParams.setEventTime(xferJobEventQueData.getTimestamp());
        durableTransferJobStatusRptParams.setClaimUserID(xferJobEventQueData.getClaimUserID());
        durableTransferJobStatusRptParams.setOperationCategory(xferJobEventQueData.getOperationCategory());
        durableTransferJobStatusRptParams.setJobID(xferJobEventQueData.getJobID());
        durableTransferJobStatusRptParams.setJobStatus(xferJobEventQueData.getJobStatus());
        durableTransferJobStatusRptParams.setTransportType(xferJobEventQueData.getTransportType());
        OMSParams.CarrierJobResult carrierJobResult = new OMSParams.CarrierJobResult();
        carrierJobResult.setCarrierJobID(xferJobEventQueData.getCarrierJobID());
        carrierJobResult.setCarrierJobStatus(xferJobEventQueData.getCarrierJobStatus());
        carrierJobResult.setCarrierID(xferJobEventQueData.getCarrierID());
        carrierJobResult.setZoneType(xferJobEventQueData.getZoneType());
        carrierJobResult.setN2PurgeFlag(xferJobEventQueData.getN2PurgeFlag());
        carrierJobResult.setFromMachineID(xferJobEventQueData.getFromMachineID());
        carrierJobResult.setFromPortID(xferJobEventQueData.getFromPortID());
        carrierJobResult.setToStockerGroup(xferJobEventQueData.getToStockerGroup());
        carrierJobResult.setToMachine(xferJobEventQueData.getToMachineID());
        carrierJobResult.setToPortID(xferJobEventQueData.getToPortID());
        carrierJobResult.setEstimatedStartTime(xferJobEventQueData.getEstimatedStartTime());
        carrierJobResult.setEstimatedEndTime(xferJobEventQueData.getEstimatedEndTime());
        carrierJobResult.setMandatoryFlag(xferJobEventQueData.getMandatoryFlag());
        carrierJobResult.setPriority(xferJobEventQueData.getPriority());
        carrierJobResult.setExpectedEndTime(xferJobEventQueData.getExpectedEndTime());
        carrierJobResult.setExpectedStartTime(xferJobEventQueData.getExpectedStartTime());

        durableTransferJobStatusRptParams.setClaimMemo("");
        durableTransferJobStatusRptParams.setStrCarrierJobResult(Arrays.asList(carrierJobResult));

        log.info("【step1】 - omsManager.sendDurableXferStatusRpt");
        omsManager.sendDurableXferStatusRpt(objCommon, durableTransferJobStatusRptParams);
    }

    @Override
    public void rtmsXferJobEventRetry(Infos.ObjCommon objCommon, Infos.XferJobEventQueData xferJobEventQueData) {
        OMSParams.DurableTransferJobStatusRptParams durableTransferJobStatusRptParams = new OMSParams.DurableTransferJobStatusRptParams();
        durableTransferJobStatusRptParams.setDurableType(Constant.TM_STOCKER_TYPE_RETICLEPOD);
        durableTransferJobStatusRptParams.setEventTime(xferJobEventQueData.getTimestamp());
        durableTransferJobStatusRptParams.setClaimUserID(xferJobEventQueData.getClaimUserID());
        durableTransferJobStatusRptParams.setOperationCategory(xferJobEventQueData.getOperationCategory());
        durableTransferJobStatusRptParams.setJobID(xferJobEventQueData.getJobID());
        durableTransferJobStatusRptParams.setJobStatus(xferJobEventQueData.getJobStatus());
        durableTransferJobStatusRptParams.setTransportType(xferJobEventQueData.getTransportType());
        OMSParams.CarrierJobResult carrierJobResult = new OMSParams.CarrierJobResult();
        carrierJobResult.setCarrierJobID(xferJobEventQueData.getCarrierJobID());
        carrierJobResult.setCarrierJobStatus(xferJobEventQueData.getCarrierJobStatus());
        carrierJobResult.setCarrierID(xferJobEventQueData.getCarrierID());
        carrierJobResult.setZoneType(xferJobEventQueData.getZoneType());
        carrierJobResult.setN2PurgeFlag(xferJobEventQueData.getN2PurgeFlag());
        carrierJobResult.setFromMachineID(xferJobEventQueData.getFromMachineID());
        carrierJobResult.setFromPortID(xferJobEventQueData.getFromPortID());
        carrierJobResult.setToStockerGroup(xferJobEventQueData.getToStockerGroup());
        carrierJobResult.setToMachine(xferJobEventQueData.getToMachineID());
        carrierJobResult.setToPortID(xferJobEventQueData.getToPortID());
        carrierJobResult.setEstimatedStartTime(xferJobEventQueData.getEstimatedStartTime());
        carrierJobResult.setEstimatedEndTime(xferJobEventQueData.getEstimatedEndTime());
        carrierJobResult.setMandatoryFlag(xferJobEventQueData.getMandatoryFlag());
        carrierJobResult.setPriority(xferJobEventQueData.getPriority());
        carrierJobResult.setExpectedEndTime(xferJobEventQueData.getExpectedEndTime());
        carrierJobResult.setExpectedStartTime(xferJobEventQueData.getExpectedStartTime());

        durableTransferJobStatusRptParams.setClaimMemo("");
        durableTransferJobStatusRptParams.setStrCarrierJobResult(Arrays.asList(carrierJobResult));

        log.info("【step1】 - omsManager.sendDurableXferStatusRpt");
        omsManager.sendDurableXferStatusRpt(objCommon, durableTransferJobStatusRptParams);
    }
}
