package com.fa.cim.tms.manager;

import com.fa.cim.tms.dto.Results;
import com.fa.cim.tms.pojo.Infos;
import com.fa.cim.tms.pojo.ObjectIdentifier;

import java.util.List;

/**
 * description: To Oms Manager Interface
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/10/12                               Neyo                create file
 *
 * @author: Neyo
 * @date: 2020/10/12 13:19
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IOMSManager {
    Results.CarrierTransferStatusChangeRptResult sendCarrierTransferStatusChangeRpt(Infos.ObjCommon objCommon, ObjectIdentifier carrierID, String xferStatus, Boolean manualInFlag, ObjectIdentifier machineID, ObjectIdentifier portID, String zoneID, String shelfType, String jobID, String carrierJobID);

    void sendDurableXferStatusChangeRpt(Infos.ObjCommon objCommon, List<Infos.TransferJobInfo> transferJobDeleteInfoList, String operationCategory, String claimMemo,Boolean tmsFlag);

    Results.ReticlePodTransferStatusChangeRptResult sendReticlePodTransferStatusChangeRpt(Infos.ObjCommon objCommon, ObjectIdentifier stockerID, ObjectIdentifier equipmentID, List<Infos.XferReticlePod> strXferReticlePod);

    Results.E10StatusReportResult sendStockerStatusChangeRpt(Infos.ObjCommon objCommon, ObjectIdentifier stockerID, ObjectIdentifier sotckerStatusCode, String claimMemo);

    Results.WhereNextInterBayResult sendWhereNextInterBay(Infos.ObjCommon objCommon, ObjectIdentifier lotID, ObjectIdentifier carrierID);

    Results.CarrierDetailInfoInqResult sendCassetteStatusInq(Infos.ObjCommon objCommon, ObjectIdentifier cassetteID);

    Results.StockerInfoInqResult sendStockerInfoInq(Infos.ObjCommon objCommon, ObjectIdentifier currStockerID, Boolean detailFlag);

    Results.AlertMessageRptResult sendSystemMsgRpt(Infos.ObjCommon objCommon, String subSystemID, String systemMessageCode, String systemMessageText, Boolean notifyFlag, ObjectIdentifier equipmentID, String equipmentStatus, ObjectIdentifier stockerID, String stockerStatus, ObjectIdentifier AGVID, String AGVStatus, ObjectIdentifier lotID, String lotStatus, ObjectIdentifier routeID, ObjectIdentifier operationID, String operationNumber, String systemMessageTimeStamp, String claimMemo);

    Results.StockerListInqResult sendStockerListInq(Infos.ObjCommon objCommon, String stockerType,Boolean avalibleFlag);

    Results.StockerForAutoTransferInqResult sendStockerForAutoTransferInq(Infos.ObjCommon objCommon);

    Results.StockerInventoryRptResult sendStockerInventoryRpt(Infos.ObjCommon objCommon, ObjectIdentifier stockerID, List<Infos.InventoryLotInfo> strInventoryLotInfo, String claimMemo);

    void sendLotCassetteXferJobCompRpt(Infos.ObjCommon objCommon, List<Infos.XferJobComp> xferJob, String claimMemo, List<String> seqJobID, List<String> seqCarrierJobID);

    Results.LoginCheckInqResult sendLoginCheckInq(Infos.ObjCommon objCommon, String subSystemID, String categoryID);

    Results.EqpAlarmRptResult sendEqpAlarmRpt(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, ObjectIdentifier stockerID, ObjectIdentifier AGVID, Infos.EquipmentAlarm equipmentAlarm, String claimMemo);

    Results.ReserveCancelReqResult sendReserveCancelReq(Infos.ObjCommon objCommon, List<Infos.RsvCanLotCarrier> strRsvCanLotCarrier, String claimMemo);

    Results.ReticlePodXferCompRptResult sendReticlePodXferJobCompRpt(Infos.ObjCommon objCommon, List<Infos.ReticlePodXferJobCompInfo> strXferJob, String claimMemo, List<String> seqJobID, List<String> seqCarrierJobID);

    Results.RSPXferStatusChangeRptResult sendRSPXferStatusChangeRpt(Infos.ObjCommon objCommon, ObjectIdentifier reticlePodID, String xferStatus, Boolean manualInFlag, ObjectIdentifier machineID, ObjectIdentifier portID);

    Results.ReticlePodStockerInfoInqResult sendReticlePodStockerInfoInq(Infos.ObjCommon objCommon, ObjectIdentifier currStockerID);

    Results.ReticlePodStatusInqResult sendReticlePodStatusInq(Infos.ObjCommon objCommon, ObjectIdentifier reticlePodID);

    Results.ReticlePodInventoryRptResult sendReticlePodInventoryRpt(Infos.ObjCommon objCommon, ObjectIdentifier stockerID, List<Infos.InventoryReticlePodInfo> strInventoryReticlePodInfo, String  claimMemo);
}