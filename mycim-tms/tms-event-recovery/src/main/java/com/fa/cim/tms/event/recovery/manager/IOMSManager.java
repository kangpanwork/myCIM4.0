package com.fa.cim.tms.event.recovery.manager;

import com.fa.cim.tms.event.recovery.dto.OMSParams;
import com.fa.cim.tms.event.recovery.dto.Results;
import com.fa.cim.tms.event.recovery.pojo.Infos;
import com.fa.cim.tms.event.recovery.pojo.ObjectIdentifier;

import java.util.List;

public interface IOMSManager {
    Results.CarrierTransferStatusChangeRptResult sendCarrierTransferStatusChangeRpt(Infos.ObjCommon objCommon, ObjectIdentifier carrierID, String xferStatus, Boolean manualInFlag, ObjectIdentifier machineID, ObjectIdentifier portID, String zoneID, String shelfType, String transferStatusChangeTimeStamp, String claimMemo);

    void sendDurableXferStatusRpt(Infos.ObjCommon objCommon, OMSParams.DurableTransferJobStatusRptParams durableTransferJobStatusRptParams);

    void sendLotCassetteXferJobCompRpt(Infos.ObjCommon objCommon, List<Infos.XferJobComp> xferJob, String claimMemo);

    Results.ReserveCancelReqResult sendReserveCancelReq(Infos.ObjCommon objCommon, List<Infos.RsvCanLotCarrier> rsvCanLotCarriers, String claimMemo);

    Results.RSPXferStatusChangeRptResult sendRSPXferStatusChangeRpt(Infos.ObjCommon objCommon, ObjectIdentifier reticlePodID, String xferStatus, Boolean manualInFlag, ObjectIdentifier machineID, ObjectIdentifier portID);

    Results.ReticlePodXferCompRptResult sendReticlePodXferJobCompRpt(Infos.ObjCommon objCommon, List<Infos.ReticlePodXferJobCompInfo> strXferJob, String claimMemo);

}