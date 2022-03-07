package com.fa.cim.tms.service.impl;

import com.fa.cim.tms.dto.Params;
import com.fa.cim.tms.dto.Results;
import com.fa.cim.tms.manager.IOMSManager;
import com.fa.cim.tms.pojo.Infos;
import com.fa.cim.tms.pojo.ObjectIdentifier;
import com.fa.cim.tms.service.IAlarmRptService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * description:
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/10/20                               Neyo                create file
 *
 * @author: Neyo
 * @date: 2020/10/20 13:39
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Service
@Slf4j
public class AlarmRptService implements IAlarmRptService {

    @Autowired
    private IOMSManager omsManager;

    public Results.AlarmReportResult sxAlarmReport(Infos.ObjCommon objCommon, Params.AlarmReportParam alarmReportParam) {
        Results.AlarmReportResult result = new Results.AlarmReportResult();
        /*-------------------------------------------------------*/
        /*  Send TxAlertMessageRpt to OMS                            */
        /*       Set Data as following :                         */
        /*         in.requestUserID  -->  requestUserID;         */
        /*         in.requestUserID  -->  subSystemID;           */
        /*         in.alarmCode      -->  systemMessageCode;     */
        /*         in.alarmText      -->  systemMessageText;     */
        /*         TRUE              -->  notifyFlag;            */
        /*         in.equipmentID    -->  equipmentID;           */
        /*         Blank             -->  equipmentStatus;       */
        /*         in.stockerID      -->  stockerID;             */
        /*         Blank             -->  stockerStatus;         */
        /*         in.vehicleID      -->  AGVID;                 */
        /*         Blank             -->  AGVStatus;             */
        /*         Blank             -->  lotID;                 */
        /*         Blank             -->  lotStatus;             */
        /*         Blank             -->  routeID;               */
        /*         Blank             -->  routeIDVersion;        */
        /*         Blank             -->  operationID;           */
        /*         Blank             -->  operationIDVersion;    */
        /*         Blank             -->  operationNumber;       */
        /*         in.alarmTimestamp -->  systemMessageTimeStamp;*/
        /*         Blank             -->  claimMemo;             */
        /*-------------------------------------------------------*/
        ObjectIdentifier lotID = new ObjectIdentifier();
        ObjectIdentifier routeID = new ObjectIdentifier();
        ObjectIdentifier operationID = new ObjectIdentifier();
        Results.AlertMessageRptResult alertMessageRptResult = new Results.AlertMessageRptResult();

        log.info("【step1】 - omsManager.sendSystemMsgRpt");
        alertMessageRptResult = omsManager.sendSystemMsgRpt(objCommon,
                ObjectIdentifier.fetchValue(objCommon.getUser().getUserID()),
                alarmReportParam.getAlarmCode(),
                alarmReportParam.getAlarmText(),
                true,
                alarmReportParam.getEquipmentID(),
                "",
                alarmReportParam.getStockerID(),
                "",
                alarmReportParam.getVehicleID(),
                "",
                lotID,
                "",
                routeID,
                operationID,
                "",
                alarmReportParam.getAlarmTimestamp(),
                ""
        );
        Infos.EquipmentAlarm strEquipmentAlarm = new Infos.EquipmentAlarm();
        strEquipmentAlarm.setAlarmCode(alarmReportParam.getAlarmCode());
        strEquipmentAlarm.setAlarmCategory(alarmReportParam.getAlarmCategory());
        strEquipmentAlarm.setAlarmID(alarmReportParam.getAlarmID());
        strEquipmentAlarm.setAlarmText(alarmReportParam.getAlarmText());
        strEquipmentAlarm.setAlarmTimeStamp(alarmReportParam.getAlarmTimestamp());
        log.info("【step2】 -  omsManager.sendEqpAlarmRpt");
        Results.EqpAlarmRptResult eqpAlarmRptResult = omsManager.sendEqpAlarmRpt(objCommon, alarmReportParam.getEquipmentID(), alarmReportParam.getStockerID(),
                alarmReportParam.getVehicleID(), strEquipmentAlarm, "");
        return result;

    }
}
