package com.fa.cim.tms.status.recovery.manager;

import com.fa.cim.tms.status.recovery.dto.Results;
import com.fa.cim.tms.status.recovery.pojo.Infos;
import com.fa.cim.tms.status.recovery.pojo.ObjectIdentifier;

public interface IOMSManager {
    Results.E10StatusReportResult sendStockerStatusChangeRpt(Infos.ObjCommon objCommon, ObjectIdentifier stockerID, ObjectIdentifier sotckerStatusCode, String claimMemo);

}