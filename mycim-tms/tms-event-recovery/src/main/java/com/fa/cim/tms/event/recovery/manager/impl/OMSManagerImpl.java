package com.fa.cim.tms.event.recovery.manager.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.Response;
import com.fa.cim.common.utils.CimDateUtils;
import com.fa.cim.tms.event.recovery.dto.CimRequest;
import com.fa.cim.tms.event.recovery.dto.OMSParams;
import com.fa.cim.tms.event.recovery.dto.Results;
import com.fa.cim.tms.event.recovery.dto.User;
import com.fa.cim.tms.event.recovery.enums.TransactionIDEnum;
import com.fa.cim.tms.event.recovery.manager.IOMSManager;
import com.fa.cim.tms.event.recovery.pojo.Infos;
import com.fa.cim.tms.event.recovery.pojo.ObjectIdentifier;
import com.fa.cim.tms.event.recovery.remote.IToOmsRemoteManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/2/20        ********             Miner               create file
 *
 * @author: Miner
 * @date: 2020/2/20 12:31
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Service
@Slf4j
public class OMSManagerImpl implements IOMSManager {

    @Autowired
    private IToOmsRemoteManager toOmsRemoteManager;


    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/11/2                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2020/11/2 17:28
     * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Override
    public Results.CarrierTransferStatusChangeRptResult sendCarrierTransferStatusChangeRpt(Infos.ObjCommon objCommon, ObjectIdentifier carrierID, String xferStatus, Boolean manualInFlag, ObjectIdentifier machineID, ObjectIdentifier portID, String zoneID, String shelfType, String transferStatusChangeTimeStamp, String claimMemo) {
        Results.CarrierTransferStatusChangeRptResult carrierTransferStatusChangeRptResult = new Results.CarrierTransferStatusChangeRptResult();
        String functionId = TransactionIDEnum.EQP_LOT_CASSETTE_XFER_STATUS_CHANGE_RPT.getValue();
        OMSParams.CarrierTransferStatusChangeRptParams carrierTransferStatusChangeRptParams = new OMSParams.CarrierTransferStatusChangeRptParams();
        carrierTransferStatusChangeRptParams.setCarrierID(carrierID);
        carrierTransferStatusChangeRptParams.setXferStatus(xferStatus);
        carrierTransferStatusChangeRptParams.setMachineID(machineID);
        carrierTransferStatusChangeRptParams.setManualInFlag(manualInFlag);
        carrierTransferStatusChangeRptParams.setPortID(portID);
        carrierTransferStatusChangeRptParams.setShelfType(shelfType);
        carrierTransferStatusChangeRptParams.setZoneID(zoneID);
        carrierTransferStatusChangeRptParams.setTransferStatusChangeTimeStamp(CimDateUtils.convertTo(transferStatusChangeTimeStamp));
        carrierTransferStatusChangeRptParams.setClaimMemo(claimMemo);
        objCommon.getUser().setFunctionID(functionId);
        carrierTransferStatusChangeRptParams.setUser(objCommon.getUser());
        CimRequest cimRequest = CimRequest.create(objCommon.getUser(), functionId, carrierTransferStatusChangeRptParams);
        String request = JSONObject.toJSONString(cimRequest);
        log.info("ERM->OMS mq sendCarrierTransferStatusChangeRpt request {}", request);
        Response response = null;
        try {
            response = toOmsRemoteManager.sendCarrierTransferStatusChangeRpt(carrierTransferStatusChangeRptParams);
        } catch (ServiceException e) {
            e.printStackTrace();
            log.error("oms retrun some error code");
        }
        log.info("ERM->OMS mq sendCarrierTransferStatusChangeRpt response {}", response);
        if (null != response && null != response.getBody()) {
            carrierTransferStatusChangeRptResult = JSON.parseObject(response.getBody().toString(), Results.CarrierTransferStatusChangeRptResult.class);
        }
        return carrierTransferStatusChangeRptResult;
    }

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/11/2                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2020/11/2 17:10
     * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Override
    public void sendDurableXferStatusRpt(Infos.ObjCommon objCommon, OMSParams.DurableTransferJobStatusRptParams durableTransferJobStatusRptParams) {
        String functionId = TransactionIDEnum.DUARBLE_XFER_JOB_STATUS_RPT.getValue();
        User user = objCommon.getUser();
        user.setFunctionID(functionId);
        durableTransferJobStatusRptParams.setUser(user);
        objCommon.getUser().setFunctionID(functionId);
        CimRequest cimRequest = CimRequest.create(objCommon.getUser(), functionId, durableTransferJobStatusRptParams);
        String request = JSONObject.toJSONString(cimRequest);
        log.info("ERM->OMS mq sendDurableXferStatusChangeRpt request {}", request);
        Response response = null;
        try {
            response = toOmsRemoteManager.sendDurableXferStatusChangeRpt(durableTransferJobStatusRptParams);
        } catch (ServiceException e) {
            e.printStackTrace();
            log.error("oms retrun some error code");
        }
        log.info("ERM->OMS mq sendDurableXferStatusChangeRpt response {}", response);
    }


    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/11/2                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2020/11/2 17:29
     * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Override
    public void sendLotCassetteXferJobCompRpt(Infos.ObjCommon objCommon, List<Infos.XferJobComp> xferJob, String claimMemo) {
        // need to do carrier_que_Put
        String functionId = TransactionIDEnum.LOT_ASSETTE_XFER_JOB_COMP_RPT.getValue();
        OMSParams.CarrierTransferJobEndRptParams carrierTransferJobEndRptParams = new OMSParams.CarrierTransferJobEndRptParams();
        objCommon.getUser().setFunctionID(functionId);
        carrierTransferJobEndRptParams.setUser(objCommon.getUser());
        carrierTransferJobEndRptParams.setClaimMemo(claimMemo);
        carrierTransferJobEndRptParams.setStrXferJob(xferJob);
        CimRequest cimRequest = CimRequest.create(objCommon.getUser(), functionId, carrierTransferJobEndRptParams);
        String request = JSONObject.toJSONString(cimRequest);
        log.info("ERM->OMS mq sendLotCassetteXferJobCompRpt request {}", request);
        Response response = null;//simulatorRabbitMQ.callOMS(request);
        try {
            response = toOmsRemoteManager.sendLotCassetteXferJobCompRpt(carrierTransferJobEndRptParams);
        } catch (ServiceException e) {
            e.printStackTrace();
            log.error("oms retrun some error code");
        }
        log.info("ERM->OMS mq sendLotCassetteXferJobCompRpt response {}", response);
    }


    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/11/2                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2020/11/2 17:31
     * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Override
    public Results.ReserveCancelReqResult sendReserveCancelReq(Infos.ObjCommon objCommon, List<Infos.RsvCanLotCarrier> rsvCanLotCarriers, String claimMemo) {
        //carrier_que_Put
        Results.ReserveCancelReqResult reserveCancelReqResult = new Results.ReserveCancelReqResult();
        String functionId = TransactionIDEnum.LOT_CASSETTE_RESERVE_CANCEL_REQ.getValue();
        OMSParams.LotCassetteReserveCancelParams lotCassetteReserveCancelParams = new OMSParams.LotCassetteReserveCancelParams();
        objCommon.getUser().setFunctionID(functionId);
        lotCassetteReserveCancelParams.setUser(objCommon.getUser());
        lotCassetteReserveCancelParams.setClaimMemo("");
        lotCassetteReserveCancelParams.setReserveCancelLotCarriers(rsvCanLotCarriers);
        CimRequest cimRequest = CimRequest.create(objCommon.getUser(), functionId, lotCassetteReserveCancelParams);
        String request = JSONObject.toJSONString(cimRequest);
        log.info("ERM->OMS mq sendReserveCancelReq request {}", request);
        Response response = null;
        try {
            response = toOmsRemoteManager.sendReserveCancelReq(lotCassetteReserveCancelParams);
        } catch (ServiceException e) {
            e.printStackTrace();
            log.error("oms retrun some error code");
        }
        log.info("ERM->OMS mq sendReserveCancelReq response {}", response);
        if (null != response && null != response.getBody()) {
            reserveCancelReqResult = JSON.parseObject(response.getBody().toString(), Results.ReserveCancelReqResult.class);
        }
        return reserveCancelReqResult;
    }

    @Override
    public Results.RSPXferStatusChangeRptResult sendRSPXferStatusChangeRpt(Infos.ObjCommon objCommon, ObjectIdentifier reticlePodID, String xferStatus, Boolean manualInFlag, ObjectIdentifier machineID, ObjectIdentifier portID) {
        Results.RSPXferStatusChangeRptResult result = new Results.RSPXferStatusChangeRptResult();

        String functionId = TransactionIDEnum.RSPXFER_STATUS_CHANGE_RPT.getValue();
        OMSParams.RSPXferStatusChangeRptParams rspXferStatusChangeRptParams = new OMSParams.RSPXferStatusChangeRptParams();
        objCommon.getUser().setFunctionID(functionId);
        rspXferStatusChangeRptParams.setUser(objCommon.getUser());
        rspXferStatusChangeRptParams.setReticlePodID(reticlePodID);
        rspXferStatusChangeRptParams.setXferStatus(xferStatus);
        rspXferStatusChangeRptParams.setManualInFlag(manualInFlag);
        rspXferStatusChangeRptParams.setMachineID(machineID);
        rspXferStatusChangeRptParams.setPortID(portID);
        rspXferStatusChangeRptParams.setClaimMemo("");
        log.info("ERM->OMS mq sendRSPXferStatusChangeRpt request {}", JSONObject.toJSONString(rspXferStatusChangeRptParams));
        Response response = null;
        try {
            response = toOmsRemoteManager.sendRSPXferStatusChangeRpt(rspXferStatusChangeRptParams);
        } catch (ServiceException e) {
            log.error("oms service fetch some serviceExcption");
            e.printStackTrace();
        }
        log.info("ERM->OMS mq sendRSPXferStatusChangeRpt response {}", JSONObject.toJSONString(response));
        if (null != response && null != response.getBody()) {
            result = JSON.parseObject(response.getBody().toString(), Results.RSPXferStatusChangeRptResult.class);
        }
        return result;
    }

    @Override
    public Results.ReticlePodXferCompRptResult sendReticlePodXferJobCompRpt(Infos.ObjCommon objCommon, List<Infos.ReticlePodXferJobCompInfo> strXferJob, String claimMemo) {
        Results.ReticlePodXferCompRptResult reuslt = new Results.ReticlePodXferCompRptResult();
        //request to OMS
        String functionId = TransactionIDEnum.RETICLE_POD_XFER_JOB_COMP_RPT.getValue();
        OMSParams.ReticlePodXferJobCompRptParams reticlePodXferJobCompRptParams = new OMSParams.ReticlePodXferJobCompRptParams();
        objCommon.getUser().setFunctionID(functionId);
        reticlePodXferJobCompRptParams.setUser(objCommon.getUser());
        reticlePodXferJobCompRptParams.setStrReticlePodXferJobCompInfo(strXferJob);
        log.info("ERM->OMS mq sendReticlePodXferJobCompRpt request {}", JSONObject.toJSONString(reticlePodXferJobCompRptParams));
        Response response = null;
        try {
            response = toOmsRemoteManager.sendReticlePodXferJobCompRpt(reticlePodXferJobCompRptParams);
        } catch (ServiceException e) {
            log.error("oms service fetch some serviceExcption");
            e.printStackTrace();
        }
        log.info("ERM->OMS mq sendReticlePodXferJobCompRpt response {}", JSONObject.toJSONString(response));
        if (null != response && null != response.getBody()) {
            reuslt = JSON.parseObject(response.getBody().toString(), Results.ReticlePodXferCompRptResult.class);
        }
        return reuslt;
    }
}
