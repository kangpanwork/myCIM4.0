package com.fa.cim.tms.manager.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.middleware.standard.core.exception.base.CimIntegrationException;
import com.fa.cim.tms.common.constant.Constant;
import com.fa.cim.tms.common.enums.EnvCodeEnum;
import com.fa.cim.tms.common.enums.TransactionIDEnum;
import com.fa.cim.tms.config.MsgRetCodeConfig;
import com.fa.cim.tms.dto.*;
import com.fa.cim.tms.manager.IOMSManager;
import com.fa.cim.tms.method.IEnvMethod;
import com.fa.cim.tms.method.IQueneStatusMethod;
import com.fa.cim.tms.method.IQueneTransferJobMethod;
import com.fa.cim.tms.method.IQueueCassetteMethod;
import com.fa.cim.tms.pojo.Infos;
import com.fa.cim.tms.pojo.ObjectIdentifier;
import com.fa.cim.tms.remote.IToOmsRemoteManager;
import com.fa.cim.tms.utils.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * description:
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/10/22                               Neyo                create file
 *
 * @author: Neyo
 * @date: 2020/10/22 14:45
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Service
@Slf4j
public class OMSManagerImpl implements IOMSManager {
    @Autowired
    private IToOmsRemoteManager toOmsRemoteManager;
    @Autowired
    private IEnvMethod envMethod;
    @Autowired
    private IQueneTransferJobMethod queneTransferJobMethod;
    @Autowired
    private IQueueCassetteMethod queueCassetteMethod;
    @Autowired
    private IQueneStatusMethod queneStatusMethod;
    @Autowired
    private MsgRetCodeConfig msgRetCodeConfig;


    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/10/22                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2020/10/22 14:44
     * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Override
    public Results.CarrierTransferStatusChangeRptResult sendCarrierTransferStatusChangeRpt(Infos.ObjCommon objCommon, ObjectIdentifier carrierID, String xferStatus, Boolean manualInFlag, ObjectIdentifier machineID, ObjectIdentifier portID, String zoneID, String shelfType, String jobID, String carrierJobID) {
        //Queuing Func
        String switchQue = envMethod.getEnvFromOmsDB(EnvCodeEnum.OM_QUE_SWITCH.getValue());//"ON"
        Boolean updateFlag = false;
        String queUpdate = envMethod.getEnvFromOmsDB(EnvCodeEnum.OM_QUE_UPDATE.getValue());//"ON"
        String eventType = Constant.TM_EVENTTYPE_M1;//"M1";

        log.info("env OM_QUE_SWITCH: {}", switchQue);
        log.info("env OM_QUE_UPDATE: {}", queUpdate);

        ObjectIdentifier dummy = ObjectIdentifier.buildWithValue("");
        Timestamp timestamp = null;

        if (BooleanUtils.isTrue(manualInFlag)) {
            eventType = Constant.TM_EVENTTYPE_M6;
        }
        if (Constant.TM_ON.equals(queUpdate)) {
            updateFlag = true;
        }
        log.info("eventType: {}", eventType);
        log.info("queUpdate: {}", queUpdate);

        if (Constant.TM_ON.equals(switchQue)) {
            /*-------------------------------------------------------*/
            /*  Get system time                                      */
            /*-------------------------------------------------------*/
            timestamp = DateUtils.getCurrentTimeStamp();
            /*-------------------------------------------*/
            /*   Check carrier event                     */
            /*-------------------------------------------*/
            log.info("【step1】 - queueCassetteMethod.carrierQueCheck");
            Long recordCount = queueCassetteMethod.carrierQueCheck(objCommon, carrierID);
            if (recordCount > 0) {
                /*-------------------------------------------*/
                /*   Put carrier event for sequence          */
                /*-------------------------------------------*/
                log.info("【step2】 - queueCassetteMethod.carrierQuePut");
                queueCassetteMethod.carrierQuePut(objCommon, timestamp, carrierID, jobID, carrierJobID, eventType, "", machineID, portID, xferStatus, updateFlag,true);
                // TODO: 2020/7/10 need throw msg cast tx no send exception and start erm system to reserve the message first
                Validations.check(true, msgRetCodeConfig.getMsgOmsCastTxNoSend());
            }
        }

        //Send Request to OMS
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
        objCommon.getUser().setFunctionID(functionId);
        carrierTransferStatusChangeRptParams.setUser(objCommon.getUser());
        CimRequest cimRequest = CimRequest.create(objCommon.getUser(), functionId, carrierTransferStatusChangeRptParams);
        String request = JSONObject.toJSONString(cimRequest);
        log.info("TMS->OMS mq sendCarrierTransferStatusChangeRpt request {}", request);
        log.info("【step3】 - toOmsRemoteManager.sendCarrierTransferStatusChangeRpt");
        Response response = null;
        try {
            response = toOmsRemoteManager.sendCarrierTransferStatusChangeRpt(carrierTransferStatusChangeRptParams);
        } catch (Exception e) {
            if (e instanceof ServiceException) {
                e.printStackTrace();
                log.error("oms service fetch some serviceExcption");
                throw e;
            } else if (e instanceof CimIntegrationException) {
                if (Constant.TM_ON.equals(switchQue)){
                    //oms shut down
                    //put carrier event for sequence
                    queueCassetteMethod.carrierQuePut(objCommon, timestamp, carrierID, jobID, carrierJobID, eventType, "", machineID, portID, xferStatus, updateFlag,true);
                    // TODO: 2020/7/10 need throw msg cast tx no send exception and start erm system to reserve the message first
                    Validations.check(true, msgRetCodeConfig.getMsgOmsCastTxNoSend());
                }
            }
        }
        log.info("TMS->OMS mq sendCarrierTransferStatusChangeRpt response {}", response);
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
     * 2020/10/22                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2020/10/22 14:57
     * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Override
    public void sendDurableXferStatusChangeRpt(Infos.ObjCommon objCommon, List<Infos.TransferJobInfo> transferJobInfoList, String operationCategory, String claimMemo,Boolean tmsFlag) {
        /*----------------*/
        /*   Initialize   */
        /*----------------*/
        String xJStatusChgRptFlg = envMethod.getEnvFromOmsDB(EnvCodeEnum.OM_XFERJOB_STATUS_RPT_FLAG.getValue());//"ON"
        log.info("env OM_XFERJOB_STATUS_RPT_FLAG: {}", xJStatusChgRptFlg);
        if (!Constant.TM_ON.equals(xJStatusChgRptFlg)) {
            log.info("env OM_XFERJOB_STATUS_RPT_FLAG == OFF Return");
            return;
        }
        if (ArrayUtils.isEmpty(transferJobInfoList)) {
            log.info("transferJobDeleteInfoList size == 0 Return");
            return;
        }

        String switchQue = envMethod.getEnvFromOmsDB(EnvCodeEnum.OM_QUE_XFERJOB_SWITCH.getValue());//"ON"
        log.info("env OM_QUE_XFERJOB_SWITCH: {}", switchQue);

        /**********************/
        /*   Convert data     */
        /**********************/
        String functionId = TransactionIDEnum.DUARBLE_XFER_JOB_STATUS_RPT.getValue();
        OMSParams.DurableTransferJobStatusRptParams durableTransferJobStatusRptParams = new OMSParams.DurableTransferJobStatusRptParams();
        User user = objCommon.getUser();
        user.setFunctionID(functionId);
        durableTransferJobStatusRptParams.setUser(user);
        durableTransferJobStatusRptParams.setDurableType(BooleanUtils.isTrue(tmsFlag) ? Constant.TM_DURABLE_TYPE_CARRIER : Constant.TM_STOCKER_TYPE_RETICLEPOD);
        durableTransferJobStatusRptParams.setClaimMemo(claimMemo);
        durableTransferJobStatusRptParams.setClaimUserID(objCommon.getUser().getUserID().getValue());
        durableTransferJobStatusRptParams.setEventTime(DateUtils.convertToSpecString(DateUtils.getCurrentTimeStamp()));
        durableTransferJobStatusRptParams.setJobID(transferJobInfoList.get(0).getJobID());
        durableTransferJobStatusRptParams.setJobStatus(transferJobInfoList.get(0).getJobStatus());
        durableTransferJobStatusRptParams.setTransportType(transferJobInfoList.get(0).getTransportType());
        durableTransferJobStatusRptParams.setOperationCategory(operationCategory);
        List<OMSParams.CarrierJobResult> carrierJobResults = new ArrayList<OMSParams.CarrierJobResult>();
        OMSParams.CarrierJobResult carrierJobResult = new OMSParams.CarrierJobResult();
        Optional.ofNullable(transferJobInfoList).ifPresent(list -> list.forEach(transferJobInfo -> {
            carrierJobResult.setCarrierID(transferJobInfo.getCarrierID());
            carrierJobResult.setCarrierJobID(transferJobInfo.getCarrierJobID());
            carrierJobResult.setCarrierJobStatus(transferJobInfo.getCarrierJobStatus());
            carrierJobResult.setEstimatedEndTime(transferJobInfo.getEstimatedEndTime());
            carrierJobResult.setEstimatedStartTime(transferJobInfo.getEstimatedStartTime());
            carrierJobResult.setExpectedEndTime(transferJobInfo.getExpectedEndTime());
            carrierJobResult.setExpectedStartTime(transferJobInfo.getExpectedStartTime());
            carrierJobResult.setFromMachineID(transferJobInfo.getFromMachineID());
            carrierJobResult.setFromPortID(transferJobInfo.getFromPortID());
            carrierJobResult.setToMachine(transferJobInfo.getToMachineID());
            carrierJobResult.setToPortID(transferJobInfo.getToPortID());
            carrierJobResult.setMandatoryFlag(transferJobInfo.getMandatoryFlag());
            carrierJobResult.setPriority(transferJobInfo.getPriority());
            carrierJobResult.setZoneType(transferJobInfo.getZoneType());
            carrierJobResult.setToStockerGroup(transferJobInfo.getToStockerGroup());
            carrierJobResult.setN2PurgeFlag(transferJobInfo.getN2PurgeFlag());
            carrierJobResults.add(carrierJobResult);
        }));

        durableTransferJobStatusRptParams.setStrCarrierJobResult(carrierJobResults);
        objCommon.getUser().setFunctionID(functionId);
        CimRequest cimRequest = CimRequest.create(objCommon.getUser(), functionId, durableTransferJobStatusRptParams);
        String request = JSONObject.toJSONString(cimRequest);
        log.info("TMS->OMS mq sendDurableXferStatusChangeRpt request {}", request);
        Response response = null;
        log.info("【step1】 - toOmsRemoteManager.sendDurableXferStatusChangeRpt");
        try {
            response = toOmsRemoteManager.sendDurableXferStatusChangeRpt(durableTransferJobStatusRptParams);
        } catch (Exception e) {
            if (e instanceof ServiceException) {
                log.error("oms service fetch some serviceExcption");
                e.printStackTrace();
                throw e;
            } else if (e instanceof CimIntegrationException) {
                if (Constant.TM_ON.equals(switchQue)) {
                    queneTransferJobMethod.xferJobEventQuePut(objCommon, operationCategory, transferJobInfoList);
                }
            }
        }
        log.info("TMS->OMS mq sendDurableXferStatusChangeRpt response {}", response);
    }

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/10/22                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2020/10/22 14:38
     * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Override
    public Results.ReticlePodTransferStatusChangeRptResult sendReticlePodTransferStatusChangeRpt(Infos.ObjCommon objCommon, ObjectIdentifier stockerID, ObjectIdentifier equipmentID, List<Infos.XferReticlePod> strXferReticlePod) {
        Results.ReticlePodTransferStatusChangeRptResult reticlePodTransferStatusChangeRptResult = new Results.ReticlePodTransferStatusChangeRptResult();
        String functionId = TransactionIDEnum.RETICLE_POD_XFER_STAUES_CHANGE_RPT.getValue();
        OMSParams.ReticlePodTransferStatusChangeRptParams reticlePodTransferStatusChangeRptParams = new OMSParams.ReticlePodTransferStatusChangeRptParams();
        objCommon.getUser().setFunctionID(functionId);
        reticlePodTransferStatusChangeRptParams.setUser(objCommon.getUser());
        reticlePodTransferStatusChangeRptParams.setStockerID(stockerID);
        reticlePodTransferStatusChangeRptParams.setEquipmentID(equipmentID);
        reticlePodTransferStatusChangeRptParams.setXferReticlePodList(strXferReticlePod);
        log.info("TMS->OMS mq sendReticlePodTransferStatusChangeRpt request {}", JSONObject.toJSONString(reticlePodTransferStatusChangeRptParams));
        log.info("【step1】 - toOmsRemoteManager.sendReticlePodTransferStatusChangeRpt");
        Response response = null;
        try {
            response = toOmsRemoteManager.sendReticlePodTransferStatusChangeRpt(reticlePodTransferStatusChangeRptParams);
        } catch(CimIntegrationException e) {
            Validations.check(true,new Code((int) e.getCode(),e.getMessage()));
        }
        log.info("TMS->OMS mq sendReticlePodTransferStatusChangeRpt response {}", response);
        if (null != response && null != response.getBody()) {
            reticlePodTransferStatusChangeRptResult = JSON.parseObject(response.getBody().toString(), Results.ReticlePodTransferStatusChangeRptResult.class);
        }
        return reticlePodTransferStatusChangeRptResult;
    }

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/10/22                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2020/10/22 14:38
     * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Override
    public Results.E10StatusReportResult sendStockerStatusChangeRpt(Infos.ObjCommon objCommon, ObjectIdentifier stockerID, ObjectIdentifier sotckerStatusCode, String claimMemo) {
        /*----------------*/
        /*  Queuing Func. */
        /*----------------*/
        String switchQue = envMethod.getEnvFromOmsDB(EnvCodeEnum.OM_QUE_SWITCH.getValue());//"ON"
        Boolean updateFlag = false;
        String queUpdate = envMethod.getEnvFromOmsDB(EnvCodeEnum.OM_QUE_UPDATE.getValue());//"ON"
        String forceKeep = envMethod.getEnvFromOmsDB(EnvCodeEnum.OM_TM05_FORCE_KEEP.getValue());//"ON"
        log.info("env OM_QUE_SWITCH: {}", switchQue);
        log.info("env OM_QUE_UPDATE: {}", queUpdate);
        log.info("env OM_TM05_FORCE_KEEP: {}", forceKeep);

        Timestamp timestamp = null;
        if (Constant.TM_ON.equals(queUpdate)) {
            updateFlag = true;
        }
        if (Constant.TM_ON.equals(switchQue)) {
            timestamp = DateUtils.getCurrentTimeStamp();
            /*-------------------------------------------*/
            /*   Check status event                      */
            /*-------------------------------------------*/
            log.info("【step1】 - queneStatusMethod.statQueCheck");
            Long recordCount = queneStatusMethod.statQueCheck(objCommon, stockerID);
            if (recordCount > 0) {
                if (Constant.TM_ON.equals(forceKeep)) {
                    /*-------------------------------------------*/
                    /*   Put carrier event for sequence          */
                    /*-------------------------------------------*/
                    log.info("Found another Event: {}", ObjectIdentifier.fetchValue(stockerID));
                    log.info("【step2】 - queneStatusMethod.statQuePut");
                    queneStatusMethod.statQuePut(objCommon, timestamp, stockerID, ObjectIdentifier.fetchValue(sotckerStatusCode), updateFlag);
                    // TODO: 2020/7/10 need throw msg cast tx no send exception and start erm system to reserve the message first
                    Validations.check(true, msgRetCodeConfig.getMsgOmsCastTxNoSend());
                }
            }
        }

        //Send Request to OMS
        Results.E10StatusReportResult e10StatusReportResult = new Results.E10StatusReportResult();
        String functionId = TransactionIDEnum.STOCKER_STATUS_CHANGE_RPT.getValue();
        OMSParams.StockerStatusChangeRptParams stockerStatusChangeRptParams = new OMSParams.StockerStatusChangeRptParams();
        objCommon.getUser().setFunctionID(functionId);
        stockerStatusChangeRptParams.setUser(objCommon.getUser());
        stockerStatusChangeRptParams.setStockerID(stockerID);
        stockerStatusChangeRptParams.setStockerStatusCode(sotckerStatusCode);
        stockerStatusChangeRptParams.setClaimMemo(claimMemo);
        CimRequest cimRequest = CimRequest.create(objCommon.getUser(), functionId, stockerStatusChangeRptParams);
        String request = JSONObject.toJSONString(cimRequest);
        log.info("TMS->OMS mq sendStockerStatusChangeRpt request {}", request);
        Response response = null;
        log.info("【step3】- toOmsRemoteManager.sendStockerStatusChangeRpt");
        try {
            response = toOmsRemoteManager.sendStockerStatusChangeRpt(stockerStatusChangeRptParams);
        } catch (Exception e) {
            if (e instanceof ServiceException) {
                log.error("oms service fetch some serviceExcption");
                e.printStackTrace();
                throw e;
            } else if (e instanceof CimIntegrationException) {
                if (StringUtils.equals(switchQue, "ON")) {
                    //oms shut down
                    //any other exception
                    //put carrier event for sequence
                    log.info("【step4】- queneStatusMethod.statQuePut");
                    queneStatusMethod.statQuePut(objCommon, timestamp, stockerID, ObjectIdentifier.fetchValue(sotckerStatusCode), updateFlag);
                    //TODO: 2020/7/10 need throw msg cast tx no send exception and start erm system to reserve the message first
                    Validations.check(true, msgRetCodeConfig.getMsgOmsCastTxNoSend());
                }
            }
        }
        log.info("TMS->OMS mq sendStockerStatusChangeRpt response {}", response);
        if (null != response && null != response.getBody()) {
            ObjectIdentifier objectIdentifier = JSON.parseObject(response.getBody().toString(), ObjectIdentifier.class);
            e10StatusReportResult.setMachineID(objectIdentifier);
        }
        return e10StatusReportResult;
    }

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/10/22                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2020/10/22 14:37
     * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Override
    public Results.WhereNextInterBayResult sendWhereNextInterBay(Infos.ObjCommon objCommon, ObjectIdentifier lotID, ObjectIdentifier carrierID) {
        Results.WhereNextInterBayResult whereNextInterBayResult = new Results.WhereNextInterBayResult();
        String functionId = TransactionIDEnum.WHERE_NEXT_INTER_BAY_INQ.getValue();
        OMSParams.WhereNextStockerInqParams whereNextStockerInqParams = new OMSParams.WhereNextStockerInqParams();
        whereNextStockerInqParams.setCassetteID(carrierID);
        whereNextStockerInqParams.setLotID(lotID);
        objCommon.getUser().setFunctionID(functionId);
        whereNextStockerInqParams.setUser(objCommon.getUser());
        CimRequest cimRequest = CimRequest.create(objCommon.getUser(), functionId, whereNextStockerInqParams);
        String request = JSONObject.toJSONString(cimRequest);
        log.info("TMS->OMS mq sendWhereNextInterBay request {}", request);
        log.info("【step1】 - toOmsRemoteManager.sendWhereNextInterBay");
        Response response = null;
        try {
            response = toOmsRemoteManager.sendWhereNextInterBay(whereNextStockerInqParams);
        } catch(CimIntegrationException e) {
            Validations.check(true,new Code((int) e.getCode(),e.getMessage()));
        }
        log.info("TMS->OMS mq sendWhereNextInterBay response {}", response);
        if (null != response && null != response.getBody()) {
            whereNextInterBayResult = JSON.parseObject(response.getBody().toString(), Results.WhereNextInterBayResult.class);
        }
        return whereNextInterBayResult;
    }

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/10/22                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2020/10/22 14:37
     * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Override
    public Results.CarrierDetailInfoInqResult sendCassetteStatusInq(Infos.ObjCommon objCommon, ObjectIdentifier cassetteID) {
        Results.CarrierDetailInfoInqResult cassetteStatusInqResult = new Results.CarrierDetailInfoInqResult();
        String functionId = TransactionIDEnum.CASSETTE_STATUS_INQ.getValue();
        OMSParams.CarrierDetailInfoInqParams carrierDetailInfoInqParams = new OMSParams.CarrierDetailInfoInqParams();
        carrierDetailInfoInqParams.setCassetteID(cassetteID);
        objCommon.getUser().setFunctionID(functionId);
        carrierDetailInfoInqParams.setUser(objCommon.getUser());
        CimRequest cimRequest = CimRequest.create(objCommon.getUser(), functionId, carrierDetailInfoInqParams);
        String request = JSONObject.toJSONString(cimRequest);
        log.info("TMS->OMS mq sendCassetteStatusInq request {}", request);
        log.info("【step1】 - toOmsRemoteManager.sendCassetteStatusInq");
        Response response = null;
        try {
            response = toOmsRemoteManager.sendCassetteStatusInq(carrierDetailInfoInqParams);
        } catch(CimIntegrationException e) {
            Validations.check(true,new Code((int) e.getCode(),e.getMessage()));
        }
        log.info("TMS->OMS mq sendCassetteStatusInq response {}", response);
        if (null != response && null != response.getBody()) {
            cassetteStatusInqResult = JSON.parseObject(response.getBody().toString(), Results.CarrierDetailInfoInqResult.class);
        }
        return cassetteStatusInqResult;
    }

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/10/22                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2020/10/22 14:37
     * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Override
    public Results.StockerInfoInqResult sendStockerInfoInq(Infos.ObjCommon objCommon, ObjectIdentifier currStockerID, Boolean detailFlag) {
        Results.StockerInfoInqResult stockerInfoInqResult = new Results.StockerInfoInqResult();
        String functionId = TransactionIDEnum.STOCKER_INFO_INQ.getValue();
        OMSParams.StockerInfoInqInParams stockerInfoInqInParams = new OMSParams.StockerInfoInqInParams();
        stockerInfoInqInParams.setMachineID(currStockerID);
        objCommon.getUser().setFunctionID(functionId);
        stockerInfoInqInParams.setUser(objCommon.getUser());
        stockerInfoInqInParams.setDetailFlag(detailFlag);
        CimRequest cimRequest = CimRequest.create(objCommon.getUser(), functionId, stockerInfoInqInParams);
        String request = JSONObject.toJSONString(cimRequest);
        log.info("TMS->OMS mq sendCassetteStatusInq request {}", request);
        log.info("【step1】 - toOmsRemoteManager.sendStockerInfoInq");
        Response response = null;
        try {
            response = toOmsRemoteManager.sendStockerInfoInq(stockerInfoInqInParams);
        } catch(CimIntegrationException e) {
            Validations.check(true,new Code((int) e.getCode(),e.getMessage()));
        }
        log.info("TMS->OMS mq sendCassetteStatusInq response {}", response);
        if (null != response && null != response.getBody()) {
            stockerInfoInqResult = JSON.parseObject(response.getBody().toString(), Results.StockerInfoInqResult.class);
        }
        return stockerInfoInqResult;
    }

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/10/22                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2020/10/22 14:37
     * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Override
    public Results.AlertMessageRptResult sendSystemMsgRpt(Infos.ObjCommon objCommon, String subSystemID, String systemMessageCode, String systemMessageText, Boolean notifyFlag, ObjectIdentifier equipmentID, String equipmentStatus, ObjectIdentifier stockerID, String stockerStatus, ObjectIdentifier AGVID, String AGVStatus, ObjectIdentifier lotID, String lotStatus, ObjectIdentifier routeID, ObjectIdentifier operationID, String operationNumber, String systemMessageTimeStamp, String claimMemo) {
        Results.AlertMessageRptResult alertMessageRptResult = new Results.AlertMessageRptResult();
        String functionId = TransactionIDEnum.SYSTEM_MSG_RPT.getValue();
        OMSParams.AlertMessageRptParams alertMessageRptParams = new OMSParams.AlertMessageRptParams();
        objCommon.getUser().setFunctionID(functionId);
        alertMessageRptParams.setUser(objCommon.getUser());
        alertMessageRptParams.setAGVID(AGVID);
        alertMessageRptParams.setAGVStatus(AGVStatus);
        alertMessageRptParams.setClaimMemo(claimMemo);
        alertMessageRptParams.setEquipmentID(equipmentID);
        alertMessageRptParams.setLotID(lotID);
        alertMessageRptParams.setLotStatus(lotStatus);
        alertMessageRptParams.setEquipmentStatus(equipmentStatus);
        alertMessageRptParams.setNotifyFlag(notifyFlag);
        alertMessageRptParams.setOperationID(operationID);
        alertMessageRptParams.setRouteID(routeID);
        alertMessageRptParams.setOperationNumber(operationNumber);
        alertMessageRptParams.setSubSystemID(subSystemID);
        alertMessageRptParams.setSystemMessageCode(systemMessageCode);
        alertMessageRptParams.setSystemMessageTimeStamp(systemMessageTimeStamp);
        CimRequest cimRequest = CimRequest.create(objCommon.getUser(), functionId, alertMessageRptParams);
        String request = JSONObject.toJSONString(cimRequest);
        log.info("TMS->OMS mq sendSystemMsgRpt request {}", request);
        log.info("【step1】 - toOmsRemoteManager.sendSystemMsgRpt");
        Response response = null;
        try {
            response = toOmsRemoteManager.sendSystemMsgRpt(alertMessageRptParams);
        } catch(CimIntegrationException e) {
            Validations.check(true,new Code((int) e.getCode(),e.getMessage()));
        }
        log.info("TMS->OMS mq sendSystemMsgRpt response {}", response);
        if (null != response && null != response.getBody()) {
            alertMessageRptResult = JSON.parseObject(response.getBody().toString(), Results.AlertMessageRptResult.class);
        }
        return alertMessageRptResult;
    }

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/10/22                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2020/10/22 14:37
     * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Override
    public Results.StockerListInqResult sendStockerListInq(Infos.ObjCommon objCommon, String stockerType,Boolean avalibleFlag) {
        Results.StockerListInqResult stockerListInqResult = new Results.StockerListInqResult();
        String functionId = TransactionIDEnum.STOCKER_LIST_INQ.getValue();
        OMSParams.StockerListInqInParams stockerListInqInParams = new OMSParams.StockerListInqInParams();
        objCommon.getUser().setFunctionID(functionId);
        stockerListInqInParams.setUser(objCommon.getUser());
        stockerListInqInParams.setStockerType(stockerType);
        stockerListInqInParams.setAvailFlag(avalibleFlag);
        CimRequest cimRequest = CimRequest.create(objCommon.getUser(), functionId, stockerListInqInParams);
        String request = JSONObject.toJSONString(cimRequest);
        log.info("TMS->OMS mq sendStockerListInq request {}", request);
        log.info("【step1】 - toOmsRemoteManager.sendStockerListInq");
        Response response = null;
        try {
            response = toOmsRemoteManager.sendStockerListInq(stockerListInqInParams);
        } catch(CimIntegrationException e) {
            Validations.check(true,new Code((int) e.getCode(),e.getMessage()));
        }
        log.info("TMS->OMS mq sendStockerListInq response {}", response);
        if (null != response && null != response.getBody()) {
            stockerListInqResult = JSON.parseObject(response.getBody().toString(), Results.StockerListInqResult.class);
        }
        return stockerListInqResult;
    }

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/10/22                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2020/10/22 14:37
     * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Override
    public Results.StockerForAutoTransferInqResult sendStockerForAutoTransferInq(Infos.ObjCommon objCommon) {
        Results.StockerForAutoTransferInqResult stockerForAutoTransferInqResult = new Results.StockerForAutoTransferInqResult();
        String functionId = TransactionIDEnum.AVAILABLE_STOCKER_INQ.getValue();
        OMSParams.StockerForAutoTransferInqParams stockerForAutoTransferInqParams = new OMSParams.StockerForAutoTransferInqParams();
        objCommon.getUser().setFunctionID(functionId);
        stockerForAutoTransferInqParams.setUser(objCommon.getUser());
        CimRequest cimRequest = CimRequest.create(objCommon.getUser(), functionId, stockerForAutoTransferInqParams);
        String request = JSONObject.toJSONString(cimRequest);
        log.info("TMS->OMS mq sendStockerForAutoTransferInq request {}", request);
        log.info("【step1】 - toOmsRemoteManager.sendStockerForAutoTransferInq");
        Response response = null;
        try {
            response = toOmsRemoteManager.sendStockerForAutoTransferInq(stockerForAutoTransferInqParams);
        } catch(CimIntegrationException e) {
            Validations.check(true,new Code((int) e.getCode(),e.getMessage()));
        }
        log.info("TMS->OMS mq sendStockerForAutoTransferInq response {}", response);
        if (null != response && null != response.getBody()) {
            stockerForAutoTransferInqResult = JSON.parseObject(response.getBody().toString(), Results.StockerForAutoTransferInqResult.class);
        }
        return stockerForAutoTransferInqResult;
    }

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/10/22                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2020/10/22 14:37
     * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Override
    public Results.StockerInventoryRptResult sendStockerInventoryRpt(Infos.ObjCommon objCommon, ObjectIdentifier stockerID, List<Infos.InventoryLotInfo> strInventoryLotInfo, String claimMemo) {
        Results.StockerInventoryRptResult stockerInventoryRptResult = new Results.StockerInventoryRptResult();
        String functionId = TransactionIDEnum.STOCKER_INVENTORY_RPT.getValue();
        OMSParams.StockerInventoryRptParam stockerInventoryRptParam = new OMSParams.StockerInventoryRptParam();
        objCommon.getUser().setFunctionID(functionId);
        stockerInventoryRptParam.setUser(objCommon.getUser());
        stockerInventoryRptParam.setStockerID(stockerID);
        stockerInventoryRptParam.setInventoryLotInfos(strInventoryLotInfo);
        stockerInventoryRptParam.setClaimMemo(claimMemo);
        CimRequest cimRequest = CimRequest.create(objCommon.getUser(), functionId, stockerInventoryRptParam);
        String request = JSONObject.toJSONString(cimRequest);
        log.info("TMS->OMS mq sendStockerInventoryRpt request {}", request);
        log.info("【step1】 - toOmsRemoteManager.sendStockerInventoryRpt");
        Response response = null;
        try {
            response = toOmsRemoteManager.sendStockerInventoryRpt(stockerInventoryRptParam);
        } catch(CimIntegrationException e) {
            Validations.check(true,new Code((int) e.getCode(),e.getMessage()));
        }
        log.info("TMS->OMS mq sendStockerInventoryRpt response {}", response);
        if (null != response && null != response.getBody()) {
            stockerInventoryRptResult = JSON.parseObject(response.getBody().toString(), Results.StockerInventoryRptResult.class);
        }
        return stockerInventoryRptResult;
    }

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/10/22                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2020/10/22 14:21
     * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Override
    public void sendLotCassetteXferJobCompRpt(Infos.ObjCommon objCommon, List<Infos.XferJobComp> xferJob, String claimMemo, List<String> seqJobID, List<String> seqCarrierJobID) {
        /*----------------*/
        /*  Queuing Func. */
        /*----------------*/
        String switchQue = envMethod.getEnvFromOmsDB(EnvCodeEnum.OM_QUE_SWITCH.getValue());//"ON"
        Boolean updateFlag = false;
        Boolean objFound = false;
        String queUpdate = envMethod.getEnvFromOmsDB(EnvCodeEnum.OM_QUE_UPDATE.getValue());//"ON"
        log.info("env OM_QUE_SWITCH: {}", switchQue);
        log.info("env OM_QUE_UPDATE: {}", queUpdate);
        Timestamp timestamp = null;
        if (Constant.TM_ON.equals(queUpdate)) {
            updateFlag = true;
        }
        if (Constant.TM_ON.equals(switchQue)) {
            timestamp = DateUtils.getCurrentTimeStamp();
            if (ArrayUtils.isNotEmpty(xferJob)) {
                for (Infos.XferJobComp xferJobComp : xferJob) {
                    log.info("【step1】 - queueCassetteMethod.carrierQueCheck");
                    Long recordCount = queueCassetteMethod.carrierQueCheck(objCommon, xferJobComp.getCarrierID());
                    if (recordCount > 0) {
                        objFound = true;
                        break;
                    }
                }
            }
            /*-------------------------------------------*/
            /*   Put carrier event for sequence          */
            /*-------------------------------------------*/
            if (BooleanUtils.isFalse(objFound)) {
                log.info("Found another Event");
                for (int j = 0; j < ArrayUtils.getSize(xferJob); j++) {
                    log.info("【step2】 - queueCassetteMethod.carrierQuePut");
                    queueCassetteMethod.carrierQuePut(objCommon,
                            timestamp,
                            xferJob.get(j).getCarrierID(),
                            seqJobID.get(j),
                            seqCarrierJobID.get(j),
                            Constant.TM_EVENTTYPE_M3,
                            "",
                            xferJob.get(j).getToMachineID(),
                            xferJob.get(j).getToPortID(),
                            xferJob.get(j).getTransferJobStatus(),
                            updateFlag,
                            true);
                }
                Validations.check(true, msgRetCodeConfig.getMsgOmsCastTxNoSend());
            }
        }

        String functionId = TransactionIDEnum.LOT_ASSETTE_XFER_JOB_COMP_RPT.getValue();
        OMSParams.CarrierTransferJobEndRptParams carrierTransferJobEndRptParams = new OMSParams.CarrierTransferJobEndRptParams();
        objCommon.getUser().setFunctionID(functionId);
        carrierTransferJobEndRptParams.setUser(objCommon.getUser());
        carrierTransferJobEndRptParams.setClaimMemo(claimMemo);
        carrierTransferJobEndRptParams.setStrXferJob(xferJob);
        CimRequest cimRequest = CimRequest.create(objCommon.getUser(), functionId, carrierTransferJobEndRptParams);
        String request = JSONObject.toJSONString(cimRequest);
        log.info("TMS->OMS mq sendLotCassetteXferJobCompRpt request {}", request);
        Response response = null;
        log.info("【step3】 - toOmsRemoteManager.sendLotCassetteXferJobCompRpt");
        try {
            response = toOmsRemoteManager.sendLotCassetteXferJobCompRpt(carrierTransferJobEndRptParams);
        } catch (Exception e) {
            if (e instanceof ServiceException) {
                log.error("oms service fetch some serviceExcption");
                e.printStackTrace();
                throw e;
            } else if (e instanceof CimIntegrationException) {
                if (Constant.TM_ON.equals(switchQue)) {
                    //oms shut down
                    //put carrier event for sequence
                    if (ArrayUtils.isNotEmpty(xferJob)) {
                        for (int i = 0; i < xferJob.size(); i++) {
                            log.info("【step4】 - queueCassetteMethod.carrierQuePut");
                            queueCassetteMethod.carrierQuePut(objCommon,
                                    timestamp,
                                    xferJob.get(i).getCarrierID(),
                                    seqJobID.get(i),
                                    seqCarrierJobID.get(i),
                                    Constant.TM_EVENTTYPE_M3,
                                    "",
                                    xferJob.get(i).getToMachineID(),
                                    xferJob.get(i).getToPortID(),
                                    xferJob.get(i).getTransferJobStatus(),
                                    updateFlag,
                                    true);
                        }
                    }
                }
                Validations.check(true, msgRetCodeConfig.getMsgOmsCastTxNoSend());
            }
        }
        log.info("TMS->OMS mq sendLotCassetteXferJobCompRpt response {}", response);
    }

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/10/22                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2020/10/22 14:38
     * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Override
    public Results.LoginCheckInqResult sendLoginCheckInq(Infos.ObjCommon objCommon, String subSystemID, String categoryID) {
        Results.LoginCheckInqResult loginCheckInqResult = new Results.LoginCheckInqResult();
        String functionId = TransactionIDEnum.LOG_ON_CHECK_REQ.getValue();
        OMSParams.LoginCheckInqParams loginCheckInqParams = new OMSParams.LoginCheckInqParams();
        objCommon.getUser().setFunctionID(functionId);
        loginCheckInqParams.setUser(objCommon.getUser());
        loginCheckInqParams.setCategoryID(categoryID);
        loginCheckInqParams.setProductRequestFlag(false);
        loginCheckInqParams.setRecipeRequestFlag(false);
        loginCheckInqParams.setSubSystemID(subSystemID);
        String request = JSONObject.toJSONString(loginCheckInqParams);
        log.info("TMS->OMS mq sendLoginCheckInq request {}", request);
        log.info("【step1】 - toOmsRemoteManager.sendLoginCheckInq");
        Response response = null;
        try {
            response = toOmsRemoteManager.sendLoginCheckInq(loginCheckInqParams);
        } catch(CimIntegrationException e) {
            Validations.check(true,new Code((int) e.getCode(),e.getMessage()));
        }
        log.info("TMS->OMS mq sendLoginCheckInq response {}", response);
        if (null != response && null != response.getBody()) {
            loginCheckInqResult = JSON.parseObject(response.getBody().toString(), Results.LoginCheckInqResult.class);
        }
        return loginCheckInqResult;
    }

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/10/22                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2020/10/22 14:22
     * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Override
    public Results.EqpAlarmRptResult sendEqpAlarmRpt(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, ObjectIdentifier stockerID, ObjectIdentifier AGVID, Infos.EquipmentAlarm equipmentAlarm, String claimMemo) {
        Results.EqpAlarmRptResult eqpAlarmRptResult = new Results.EqpAlarmRptResult();
        String functionId = TransactionIDEnum.EQP_ALARM_RPT.getValue();
        OMSParams.EqpAlarmRptParams eqpAlarmRptParams = new OMSParams.EqpAlarmRptParams();
        objCommon.getUser().setFunctionID(functionId);
        eqpAlarmRptParams.setUser(objCommon.getUser());
        eqpAlarmRptParams.setClaimMemo(claimMemo);
        eqpAlarmRptParams.setAGVID(AGVID);
        eqpAlarmRptParams.setEquipmentID(equipmentID);
        eqpAlarmRptParams.setStockerID(stockerID);
        eqpAlarmRptParams.setStrEquipmentAlarm(equipmentAlarm);
        CimRequest cimRequest = CimRequest.create(objCommon.getUser(), functionId, eqpAlarmRptParams);
        String request = JSONObject.toJSONString(cimRequest);
        log.info("TMS->OMS mq sendEqpAlarmRpt request {}", request);
        log.info("【step1】 - toOmsRemoteManager.sendEqpAlarmRpt");
        Response response = null;
        try {
            response = toOmsRemoteManager.sendEqpAlarmRpt(eqpAlarmRptParams);
        } catch(CimIntegrationException e) {
            Validations.check(true,new Code((int) e.getCode(),e.getMessage()));
        }
        log.info("TMS->OMS mq sendEqpAlarmRpt response {}", response);
        if (null != response && null != response.getBody()) {
            eqpAlarmRptResult = JSON.parseObject(response.getBody().toString(), Results.EqpAlarmRptResult.class);
        }
        return eqpAlarmRptResult;
    }

    /**
     * description:
     * change history:
     * date             defect#             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2020/10/22                               Neyo                create file
     *
     * @author: Neyo
     * @date: 2020/10/22 14:22
     * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
     */
    @Override
    public Results.ReserveCancelReqResult sendReserveCancelReq(Infos.ObjCommon objCommon, List<Infos.RsvCanLotCarrier> strRsvCanLotCarrier, String claimMemo) {
        /*----------------*/
        /*  Queuing Func. */
        /*----------------*/
        String switchQue = envMethod.getEnvFromOmsDB(EnvCodeEnum.OM_QUE_SWITCH.getValue());//"ON"
        Boolean putQueFlag = false;
        Boolean updateFlag = false;
        String queUpdate = envMethod.getEnvFromOmsDB(EnvCodeEnum.OM_QUE_UPDATE.getValue());//"ON"
        String eventType = Constant.TM_EVENTTYPE_M0;
        Timestamp timestamp = null;
        if (Constant.TM_ON.equals(queUpdate)) {
            updateFlag = true;
        }
        if (Constant.TM_ON.equals(switchQue)) {
            timestamp = DateUtils.getCurrentTimeStamp();
            /*-------------------------------------------*/
            /*   Check carrier event                     */
            /*-------------------------------------------*/
            if (ArrayUtils.isNotEmpty(strRsvCanLotCarrier)) {
                for (Infos.RsvCanLotCarrier rsvCanLotCarrier : strRsvCanLotCarrier) {
                    log.info("【step1】 - queueCassetteMethod.carrierQueCheck");
                    Long recordCount = queueCassetteMethod.carrierQueCheck(objCommon, rsvCanLotCarrier.getCarrierID());
                    if (recordCount > 0) {
                        log.info("Found another Event");
                        putQueFlag = true;
                    }
                }
            }
            /*-------------------------------------------*/
            /*   Put carrier event for sequence          */
            /*-------------------------------------------*/
            if (BooleanUtils.isTrue(putQueFlag)) {
                for (int j = 0; j < ArrayUtils.getSize(strRsvCanLotCarrier); j++) {
                    log.info("【step2】 - queueCassetteMethod.carrierQuePut");
                    queueCassetteMethod.carrierQuePut(objCommon,
                            timestamp,
                            strRsvCanLotCarrier.get(j).getCarrierID(),
                            "",
                            "",
                            eventType,
                            "",
                            null,
                            null,
                            "",
                            updateFlag,
                            true);
                }
                Validations.check(true, msgRetCodeConfig.getMsgOmsCastTxNoSend());
            }
        }
        //Send Request to OMS
        Results.ReserveCancelReqResult reserveCancelReqResult = new Results.ReserveCancelReqResult();
        String functionId = TransactionIDEnum.LOT_CASSETTE_RESERVE_CANCEL_REQ.getValue();
        OMSParams.LotCassetteReserveCancelParams lotCassetteReserveCancelParams = new OMSParams.LotCassetteReserveCancelParams();
        objCommon.getUser().setFunctionID(functionId);
        lotCassetteReserveCancelParams.setUser(objCommon.getUser());
        lotCassetteReserveCancelParams.setClaimMemo("");
        lotCassetteReserveCancelParams.setReserveCancelLotCarriers(strRsvCanLotCarrier);
        CimRequest cimRequest = CimRequest.create(objCommon.getUser(), functionId, lotCassetteReserveCancelParams);
        String request = JSONObject.toJSONString(cimRequest);
        log.info("TMS->OMS mq sendReserveCancelReq request {}", request);
        Response response = null;
        log.info("【step3】 - toOmsRemoteManager.sendReserveCancelReq");
        try {
            response = toOmsRemoteManager.sendReserveCancelReq(lotCassetteReserveCancelParams);
        } catch (Exception e) {
            if (e instanceof ServiceException) {
                log.error("oms service fetch some serviceExcption");
                e.printStackTrace();
                throw e;
            } else if (e instanceof CimIntegrationException) {
                if (Constant.TM_ON.equals(switchQue)) {
                    //oms shut down
                    //put carrier event for sequence
                    for (int j = 0; j < ArrayUtils.getSize(strRsvCanLotCarrier); j++) {
                        log.info("【step4】 - queueCassetteMethod.carrierQuePut");
                        queueCassetteMethod.carrierQuePut(objCommon,
                                timestamp,
                                strRsvCanLotCarrier.get(j).getCarrierID(),
                                "",
                                "",
                                eventType,
                                "",
                                null,
                                null,
                                "",
                                updateFlag,
                                true);
                    }
                }
                // TODO: 2020/7/10 need throw msg cast tx no send exception and start erm system to reserve the message first
                Validations.check(true, msgRetCodeConfig.getMsgOmsCastTxNoSend());
            }
        }
        //oms return success
        log.info("TMS->OMS mq sendReserveCancelReq response {}", response);
        if (null != response && null != response.getBody()) {
            reserveCancelReqResult = JSON.parseObject(response.getBody().toString(), Results.ReserveCancelReqResult.class);
        }
        return reserveCancelReqResult;
    }

    @Override
    public Results.ReticlePodXferCompRptResult sendReticlePodXferJobCompRpt(Infos.ObjCommon objCommon, List<Infos.ReticlePodXferJobCompInfo> strXferJob, String claimMemo, List<String> seqJobID, List<String> seqCarrierJobID) {
        Results.ReticlePodXferCompRptResult reuslt = new Results.ReticlePodXferCompRptResult();
        //==========================================
        //  Queuing Func.
        //==========================================
        Boolean updateFlag = false;
        Boolean objFound = false;
        String switchQue = envMethod.getEnvFromOmsDB(EnvCodeEnum.OM_QUE_SWITCH.getValue());
        String queUpdate = envMethod.getEnvFromOmsDB(EnvCodeEnum.OM_QUE_UPDATE.getValue());

        log.info("env OM_QUE_SWITCH: {}",switchQue);
        log.info("env OM_QUE_UPDATE: {}",queUpdate);

        Timestamp timestamp = null;
        if (Constant.TM_ON.equals(queUpdate)){
            updateFlag = true;
        }
        if (Constant.TM_ON.equals(switchQue)){
            timestamp = DateUtils.getCurrentTimeStamp();
            //==========================================
            //   Check carrier event
            //==========================================
            if (ArrayUtils.isNotEmpty(strXferJob)){
                for (Infos.ReticlePodXferJobCompInfo reticlePodXferJobCompInfo : strXferJob) {
                    Long recordCount = queueCassetteMethod.carrierQueCheck(objCommon, reticlePodXferJobCompInfo.getReticlePodID());
                    if (recordCount > 0){
                        objFound = true;
                        break;
                    }
                }
                //==========================================
                //   Put carrier event for sequence
                //==========================================
                if (BooleanUtils.isTrue(objFound)){
                    for (int j = 0; j < ArrayUtils.getSize(strXferJob); j++) {
                        queueCassetteMethod.carrierQuePut(objCommon,
                                timestamp,
                                strXferJob.get(j).getReticlePodID(),
                                seqJobID.get(j),
                                seqCarrierJobID.get(j),
                                Constant.TM_EVENTTYPE_M3,
                                "",
                                strXferJob.get(j).getToMachineID(),
                                strXferJob.get(j).getToPortID(),
                                strXferJob.get(j).getTransferJobStatus(),
                                updateFlag,
                                false);
                        log.info("The queued data is: {}",ObjectIdentifier.fetchValue(strXferJob.get(j).getReticlePodID()));
                    }
                    return reuslt;
                }
            }
        }
        //request to OMS
        String functionId = TransactionIDEnum.RETICLE_POD_XFER_JOB_COMP_RPT.getValue();
        OMSParams.ReticlePodXferJobCompRptParams reticlePodXferJobCompRptParams = new OMSParams.ReticlePodXferJobCompRptParams();
        objCommon.getUser().setFunctionID(functionId);
        reticlePodXferJobCompRptParams.setUser(objCommon.getUser());
        reticlePodXferJobCompRptParams.setStrReticlePodXferJobCompInfo(strXferJob);
        log.info("TMS->OMS mq sendReticlePodXferJobCompRpt request {}", JSONObject.toJSONString(reticlePodXferJobCompRptParams));
        log.info("【step1】 - toOmsRemoteManager.sendReticlePodXferJobCompRpt");
        Response response = null;
        try {
            response = toOmsRemoteManager.sendReticlePodXferJobCompRpt(reticlePodXferJobCompRptParams);
        } catch (Exception e) {
            if (e instanceof ServiceException) {
                log.error("oms service fetch some serviceExcption");
                e.printStackTrace();
                throw e;
            } else if (e instanceof CimIntegrationException) {
                if (Constant.TM_ON.equals(switchQue)) {
                    //oms shut down
                    //put carrier event for sequence
                    for (int j = 0; j < ArrayUtils.getSize(strXferJob); j++) {
                        log.info("【step4】 - queueCassetteMethod.carrierQuePut");
                        queueCassetteMethod.carrierQuePut(objCommon,
                                timestamp,
                                strXferJob.get(j).getReticlePodID(),
                                seqJobID.get(j),
                                seqCarrierJobID.get(j),
                                Constant.TM_EVENTTYPE_M3,
                                "",
                                strXferJob.get(j).getToMachineID(),
                                strXferJob.get(j).getToPortID(),
                                strXferJob.get(j).getTransferJobStatus(),
                                updateFlag,
                                false);
                    }
                }
            }
        }
        log.info("TMS->OMS mq sendReticlePodXferJobCompRpt response {}", response);
        if (null != response && null != response.getBody()) {
            reuslt = JSON.parseObject(response.getBody().toString(), Results.ReticlePodXferCompRptResult.class);
        }
        return reuslt;
    }

    @Override
    public Results.RSPXferStatusChangeRptResult sendRSPXferStatusChangeRpt(Infos.ObjCommon objCommon, ObjectIdentifier reticlePodID, String xferStatus, Boolean manualInFlag, ObjectIdentifier machineID, ObjectIdentifier portID) {
        Results.RSPXferStatusChangeRptResult result = new Results.RSPXferStatusChangeRptResult();
        //==========================================
        //  Queuing Func.
        //==========================================
        Boolean updateFlag = false;
        String switchQue = envMethod.getEnvFromOmsDB(EnvCodeEnum.OM_QUE_SWITCH.getValue());
        String queUpdate = envMethod.getEnvFromOmsDB(EnvCodeEnum.OM_QUE_UPDATE.getValue());
        String eventType = Constant.TM_EVENTTYPE_M1;

        log.info("env OM_QUE_SWITCH: {}",switchQue);
        log.info("env OM_QUE_UPDATE: {}",queUpdate);

        Timestamp timestamp = null;
        if (BooleanUtils.isTrue(manualInFlag)){
            eventType = Constant.TM_EVENTTYPE_M6;
        }
        if (Constant.TM_ON.equals(queUpdate)){
            updateFlag = true;
        }
        if (Constant.TM_ON.equals(switchQue)){
            timestamp = DateUtils.getCurrentTimeStamp();
            //==========================================
            //   Check carrier event
            //==========================================
            log.info("【step1】 - queueCassetteMethod.carrierQueCheck");
            Long recordCnt = queueCassetteMethod.carrierQueCheck(objCommon, reticlePodID);

            //==========================================
            //   Put carrier event for sequence
            //==========================================
            if (recordCnt > 0){
                log.info("Found another Event");
                log.info("【step2】 - queueCassetteMethod.carrierQuePut");
                queueCassetteMethod.carrierQuePut(objCommon,
                        timestamp,
                        reticlePodID,
                        "",
                        "",
                        eventType,
                        "",
                        machineID,
                        portID,
                        xferStatus,
                        updateFlag,
                        false);
            }
            return result;
        }

        //request to OMS
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
        log.info("TMS->OMS mq sendRSPXferStatusChangeRpt request {}", JSONObject.toJSONString(rspXferStatusChangeRptParams));
        log.info("【step3】 - toOmsRemoteManager.sendRSPXferStatusChangeRpt");
        Response response = null;
        try {
            response = toOmsRemoteManager.sendRSPXferStatusChangeRpt(rspXferStatusChangeRptParams);
        } catch (Exception e) {
            if (e instanceof ServiceException) {
                log.error("oms service fetch some serviceExcption");
                e.printStackTrace();
                throw e;
            } else if (e instanceof CimIntegrationException) {
                if (Constant.TM_ON.equals(switchQue)) {
                    //oms shut down
                    //put carrier event for sequence
                    log.info("【step4】 - queueCassetteMethod.carrierQuePut");
                    queueCassetteMethod.carrierQuePut(objCommon,
                            timestamp,
                            reticlePodID,
                            "",
                            "",
                            eventType,
                            "",
                            machineID,
                            portID,
                            xferStatus,
                            updateFlag,
                            false);
                }
            }
        }
        log.info("TMS->OMS mq sendRSPXferStatusChangeRpt response {}", response);
        if (null != response && null != response.getBody()) {
            result = JSON.parseObject(response.getBody().toString(), Results.RSPXferStatusChangeRptResult.class);
        }
        return result;
    }

    @Override
    public Results.ReticlePodStockerInfoInqResult sendReticlePodStockerInfoInq(Infos.ObjCommon objCommon, ObjectIdentifier currStockerID) {
        Results.ReticlePodStockerInfoInqResult reticlePodStockerInfoInqResult = new Results.ReticlePodStockerInfoInqResult();
        String functionId = TransactionIDEnum.RETICLE_POD_STOCKER_INFO_INQ.getValue();
        OMSParams.ReticlePodStockerInfoInqParams reticlePodStockerInfoInqParams = new OMSParams.ReticlePodStockerInfoInqParams();
        objCommon.getUser().setFunctionID(functionId);
        reticlePodStockerInfoInqParams.setUser(objCommon.getUser());
        reticlePodStockerInfoInqParams.setStockerID(currStockerID);
        CimRequest cimRequest = CimRequest.create(objCommon.getUser(), functionId, reticlePodStockerInfoInqParams);
        String request = JSONObject.toJSONString(cimRequest);
        log.info("TMS->OMS mq sendReticlePodStockerInfoInq request {}", request);
        log.info("【step1】 - toOmsRemoteManager.sendReticlePodStockerInfoInq");
        Response response = null;
        try {
            response = toOmsRemoteManager.sendReticlePodStockerInfoInq(reticlePodStockerInfoInqParams);
        } catch(CimIntegrationException e) {
            Validations.check(true,new Code((int) e.getCode(),e.getMessage()));
        }
        log.info("TMS->OMS mq sendEqpAlarmRpt response {}", response);
        if (null != response && null != response.getBody()) {
            reticlePodStockerInfoInqResult = JSON.parseObject(response.getBody().toString(), Results.ReticlePodStockerInfoInqResult.class);
        }
        return reticlePodStockerInfoInqResult;
    }

    @Override
    public Results.ReticlePodStatusInqResult sendReticlePodStatusInq(Infos.ObjCommon objCommon, ObjectIdentifier reticlePodID) {
        Results.ReticlePodStatusInqResult reticlePodStockerInfoInqResult = new Results.ReticlePodStatusInqResult();
        String functionId = TransactionIDEnum.RETICLE_POD_STATUS_INQ.getValue();
        OMSParams.ReticlePodStatusInqParams reticlePodStatusInqParams = new OMSParams.ReticlePodStatusInqParams();
        objCommon.getUser().setFunctionID(functionId);
        reticlePodStatusInqParams.setUser(objCommon.getUser());
        reticlePodStatusInqParams.setReticlePodID(reticlePodID);
        CimRequest cimRequest = CimRequest.create(objCommon.getUser(), functionId, reticlePodStatusInqParams);
        String request = JSONObject.toJSONString(cimRequest);
        log.info("TMS->OMS mq sendReticlePodStockerInfoInq request {}", request);
        log.info("【step1】 - toOmsRemoteManager.sendReticlePodStockerInfoInq");
        Response response = null;
        try {
            response = toOmsRemoteManager.sendReticlePodStatusInq(reticlePodStatusInqParams);
        } catch(CimIntegrationException e) {
            Validations.check(true,new Code((int) e.getCode(),e.getMessage()));
        }
        log.info("TMS->OMS mq sendEqpAlarmRpt response {}", response);
        if (null != response && null != response.getBody()) {
            reticlePodStockerInfoInqResult = JSON.parseObject(response.getBody().toString(), Results.ReticlePodStatusInqResult.class);
        }
        return reticlePodStockerInfoInqResult;
    }

    @Override
    public Results.ReticlePodInventoryRptResult sendReticlePodInventoryRpt(Infos.ObjCommon objCommon, ObjectIdentifier stockerID, List<Infos.InventoryReticlePodInfo> strInventoryReticlePodInfo, String claimMemo) {
        Results.ReticlePodInventoryRptResult reticlePodInventoryRptResult = new Results.ReticlePodInventoryRptResult();
        String functionId = TransactionIDEnum.RETICLE_POD_INVENTORY_RPT.getValue();
        OMSParams.ReticlePodInventoryRptParams reticlePodInventoryRptParams = new OMSParams.ReticlePodInventoryRptParams();
        objCommon.getUser().setFunctionID(functionId);
        reticlePodInventoryRptParams.setUser(objCommon.getUser());
        reticlePodInventoryRptParams.setStockerID(stockerID);
        reticlePodInventoryRptParams.setStrInventoryReticlePodInfo(strInventoryReticlePodInfo);
        reticlePodInventoryRptParams.setClaimMemo(claimMemo);
        CimRequest cimRequest = CimRequest.create(objCommon.getUser(), functionId, reticlePodInventoryRptParams);
        String request = JSONObject.toJSONString(cimRequest);
        log.info("TMS->OMS mq sendReticlePodInventoryRpt request {}", request);
        log.info("【step1】 - toOmsRemoteManager.sendReticlePodInventoryRpt");
        Response response = null;
        try {
            response = toOmsRemoteManager.sendReticlePodInventoryRpt(reticlePodInventoryRptParams);
        } catch(CimIntegrationException e) {
            Validations.check(true,new Code((int) e.getCode(),e.getMessage()));
        }
        log.info("TMS->OMS mq sendReticlePodInventoryRpt response {}", response);
        if (null != response && null != response.getBody()) {
            reticlePodInventoryRptResult = JSON.parseObject(response.getBody().toString(), Results.ReticlePodInventoryRptResult.class);
        }
        return reticlePodInventoryRptResult;
    }
}
