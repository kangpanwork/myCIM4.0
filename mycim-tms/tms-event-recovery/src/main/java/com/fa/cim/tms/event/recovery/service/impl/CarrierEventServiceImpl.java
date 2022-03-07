package com.fa.cim.tms.event.recovery.service.impl;

import com.fa.cim.tms.event.recovery.constant.Constant;
import com.fa.cim.tms.event.recovery.dto.Params;
import com.fa.cim.tms.event.recovery.dto.Results;
import com.fa.cim.tms.event.recovery.enums.EnvCodeEnum;
import com.fa.cim.tms.event.recovery.enums.TransactionIDEnum;
import com.fa.cim.tms.event.recovery.manager.IOMSManager;
import com.fa.cim.tms.event.recovery.method.IEnvMethod;
import com.fa.cim.tms.event.recovery.pojo.Infos;
import com.fa.cim.tms.event.recovery.pojo.ObjectIdentifier;
import com.fa.cim.tms.event.recovery.remote.IToTmsRemoteManager;
import com.fa.cim.tms.event.recovery.service.ICarrierEventService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * description:
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/11/2                              Neyo                create file
 *
 * @author: Neyo
 * @date: 2020/11/2 14:27
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@Service
public class CarrierEventServiceImpl implements ICarrierEventService {

    @Autowired
    private IOMSManager omsManager;
    @Autowired
    private IEnvMethod envMethod;
    @Autowired
    private IToTmsRemoteManager toTmsRemoteManager;

    @Override
    public void tmsCarrierEventRetry(Infos.ObjCommon objCommon, Infos.CarrierQueGetData carrierQueGetData) {
        //-------------------------------------
        //  Recover M1:CarrierLocationRpt
        //-------------------------------------
        if (Constant.TM_EVENTTYPE_M0.equals(carrierQueGetData.getEventType())) {
            log.info("carrierQueGetData.getEventType() is: {}", Constant.TM_EVENTTYPE_M0);

            /*-------------------------*/
            /*   Send Request to OMS   */
            /*-------------------------*/
            log.info("【step1】 - omsManager.sendReserveCancelReq");
            List<Infos.RsvCanLotCarrier> rsvCanLotCarriers = new ArrayList<>();
            Infos.RsvCanLotCarrier strRsvCanLotCarrier = new Infos.RsvCanLotCarrier();
            strRsvCanLotCarrier.setCarrierID(carrierQueGetData.getCarrierID());
            rsvCanLotCarriers.add(strRsvCanLotCarrier);
            Results.ReserveCancelReqResult results = omsManager.sendReserveCancelReq(objCommon, rsvCanLotCarriers, "");
        }

        //-------------------------------------
        //  Recover M1:TxCarrierLocationRpt
        //-------------------------------------
        else if (Constant.TM_EVENTTYPE_M1.equals(carrierQueGetData.getEventType())) {
            log.info("carrierQueGetData.getEventType() is: {}", Constant.TM_EVENTTYPE_M1);
            /*-------------------------*/
            /*   Send Request to OMS   */
            /*-------------------------*/
            log.info("【step2】 - omsManager.sendCarrierTransferStatusChangeRpt");
            Results.CarrierTransferStatusChangeRptResult results = omsManager.sendCarrierTransferStatusChangeRpt(objCommon,
                    carrierQueGetData.getCarrierID(),
                    carrierQueGetData.getTransferStatus(),
                    false,
                    carrierQueGetData.getMachineID(),
                    carrierQueGetData.getPortID(),
                    null,
                    null,
                    carrierQueGetData.getTimeStamp(),
                    "");
        }
        //-------------------------------------
        //  Recover M3:TransportJobStatusRpt
        //-------------------------------------
        else if (Constant.TM_EVENTTYPE_M3.equals(carrierQueGetData.getEventType())) {
            log.info("carrierQueGetData.getEventType() is: {}", Constant.TM_EVENTTYPE_M3);
            Infos.XferJobComp xferJob = new Infos.XferJobComp();
            xferJob.setToMachineID(carrierQueGetData.getMachineID());
            xferJob.setToPortID(carrierQueGetData.getPortID());
            xferJob.setTransferJobStatus(carrierQueGetData.getTransferStatus());
            log.info("【step3】 - omsManager.sendLotCassetteXferJobCompRpt");
            omsManager.sendLotCassetteXferJobCompRpt(objCommon, Collections.singletonList(xferJob), "");

            String tm3StatusFlag = envMethod.getEnvFromOmsDB(EnvCodeEnum.OM_TM03_STATUS_FLAG.getValue());
            log.info("env OM_TM03_STATUS_FLAG: {}", tm3StatusFlag);
            if (Constant.TM_ON.equals(tm3StatusFlag)) {
                /*-------------------------*/
                /*   Send Request to OMS   */
                /*-------------------------*/
                log.info("【step4】 - omsManager.sendCarrierTransferStatusChangeRpt");
                Results.CarrierTransferStatusChangeRptResult results = omsManager.sendCarrierTransferStatusChangeRpt(objCommon,
                        carrierQueGetData.getCarrierID(),
                        carrierQueGetData.getTransferStatus(),
                        false,
                        carrierQueGetData.getMachineID(),
                        carrierQueGetData.getPortID(),
                        null,
                        null,
                        carrierQueGetData.getTimeStamp(),
                        "");

            }
        }
        //-------------------------------------
        //  Recover M6:CarrierIDReadReport
        //-------------------------------------
        else if (Constant.TM_EVENTTYPE_M6.equals(carrierQueGetData.getEventType())) {
            log.info("carrierQueGetData.getEventType() is: {}", Constant.TM_EVENTTYPE_M6);
            /*-------------------------*/
            /*   Send Request to OMS   */
            /*-------------------------*/
            String functionId = TransactionIDEnum.EQP_LOT_CASSETTE_XFER_STATUS_CHANGE_RPT.getValue();
            log.info("【step5】 - omsManager.sendCarrierTransferStatusChangeRpt");
            Results.CarrierTransferStatusChangeRptResult carrierTransferStatusChangeRptResult = omsManager.sendCarrierTransferStatusChangeRpt(objCommon,
                    carrierQueGetData.getCarrierID(),
                    carrierQueGetData.getTransferStatus(),
                    true,
                    carrierQueGetData.getMachineID(),
                    carrierQueGetData.getPortID(),
                    null,
                    null,
                    carrierQueGetData.getTimeStamp(),
                    "");

            /*-------------------------------------------*/
            /*   Call XferReq to TMS                     */
            /*-------------------------------------------*/
            String fromPortFlag = envMethod.getEnvFromOmsDB(EnvCodeEnum.OM_TM06_SEND_FROM_PORTID.getValue());
            log.info("env OM_TM06_SEND_FROM_PORTID: {}", fromPortFlag);

            Params.CarrierIDReadReportParmas carrierIDReadReportParmas = new Params.CarrierIDReadReportParmas();
            if (Constant.TM_ON.equals(fromPortFlag)) {
                carrierIDReadReportParmas.setPortID(carrierQueGetData.getPortID());
            } else {
                carrierIDReadReportParmas.setPortID(ObjectIdentifier.buildWithValue(""));
            }
            carrierIDReadReportParmas.setCarrierID(carrierQueGetData.getCarrierID());
            carrierIDReadReportParmas.setMachineID(carrierQueGetData.getMachineID());
            carrierIDReadReportParmas.setInOutType("");
            carrierIDReadReportParmas.setPortType(ObjectIdentifier.buildWithValue(""));
            carrierIDReadReportParmas.setJobRequestFlag(true);
            carrierIDReadReportParmas.setRequestUserID(objCommon.getUser());
//            CimRequest cimRequest = CimRequest.create(objCommon.getUser(), functionId, carrierIDReadReportParmas);

            log.info("【step6】 - toTmsRemoteManager.callTmsCarrierIDReadRptRetry");
            toTmsRemoteManager.callTmsCarrierIDReadRptRetry(carrierIDReadReportParmas);
        } else {
            log.info("Found Unknown Event.");
        }
    }

    @Override
    public void rtmsCarrierEventRetry(Infos.ObjCommon objCommon, Infos.CarrierQueGetData carrierQueGetData) {
        //-------------------------------------
        //  Recover M1:CarrierLocationRpt
        //-------------------------------------
        if (Constant.TM_EVENTTYPE_M1.equals(carrierQueGetData.getEventType())) {
            log.info("carrierQueGetData.getEventType() is: {}", Constant.TM_EVENTTYPE_M1);

            /*-------------------------*/
            /*   Send Request to OMS   */
            /*-------------------------*/
            log.info("【step1】 - omsManager.sendRSPXferStatusChangeRpt");
            Results.RSPXferStatusChangeRptResult results = omsManager.sendRSPXferStatusChangeRpt(objCommon,
                    carrierQueGetData.getCarrierID(),
                    carrierQueGetData.getTransferStatus(),
                    false,
                    carrierQueGetData.getMachineID(),
                    carrierQueGetData.getPortID());
        }

        //-------------------------------------
        //  Recover M3:TransportJobStatusRpt
        //-------------------------------------
        else if (Constant.TM_EVENTTYPE_M3.equals(carrierQueGetData.getEventType())) {
            log.info("carrierQueGetData.getEventType() is: {}", Constant.TM_EVENTTYPE_M3);
            /*-------------------------*/
            /*   Send Request to OMS   */
            /*-------------------------*/
            log.info("【step2】 - omsManager.sendCarrierTransferStatusChangeRpt");
            Infos.ReticlePodXferJobCompInfo xferJobComp = new Infos.ReticlePodXferJobCompInfo();
            xferJobComp.setToMachineID(carrierQueGetData.getMachineID());
            xferJobComp.setToPortID(carrierQueGetData.getPortID());
            xferJobComp.setTransferJobStatus(carrierQueGetData.getTransferStatus());
            Results.ReticlePodXferCompRptResult reticlePodXferCompRptResult = omsManager.sendReticlePodXferJobCompRpt(objCommon,
                    Collections.singletonList(xferJobComp),
                    null);

            String statusRptFlagTm03 = envMethod.getEnvFromOmsDB(EnvCodeEnum.OM_TM03_STATUS_FLAG.getValue());
            log.info("env OM_TM03_STATUS_FLAG: {}", statusRptFlagTm03);
            if (Constant.TM_ON.equals(statusRptFlagTm03)) {
                /*-------------------------*/
                /*   Send Request to OMS   */
                /*-------------------------*/
                log.info("【step3】 - omsManager.sendRSPXferStatusChangeRpt");
                Results.RSPXferStatusChangeRptResult rspXferStatusChangeRptResult = omsManager.sendRSPXferStatusChangeRpt(objCommon,
                        carrierQueGetData.getCarrierID(),
                        carrierQueGetData.getTransferStatus(),
                        false,
                        carrierQueGetData.getMachineID(),
                        carrierQueGetData.getPortID());
            }
        }
        //-------------------------------------
        //  Recover M6:CarrierIDReadRpt
        //-------------------------------------
        else if (Constant.TM_EVENTTYPE_M6.equals(carrierQueGetData.getEventType())) {
            log.info("carrierQueGetData.getEventType() is: {}", Constant.TM_EVENTTYPE_M6);
            /*-------------------------*/
            /*   Send Request to OMS   */
            /*-------------------------*/
            String functionId = TransactionIDEnum.EQP_LOT_CASSETTE_XFER_STATUS_CHANGE_RPT.getValue();
            log.info("【step4】 - omsManager.sendRSPXferStatusChangeRpt");
            Results.RSPXferStatusChangeRptResult rspXferStatusChangeRptResult = omsManager.sendRSPXferStatusChangeRpt(objCommon,
                    carrierQueGetData.getCarrierID(),
                    carrierQueGetData.getTransferStatus(),
                    false,
                    carrierQueGetData.getMachineID(),
                    carrierQueGetData.getPortID());

        } else {
            log.info("Found Unknown Event.");
        }
    }
}
