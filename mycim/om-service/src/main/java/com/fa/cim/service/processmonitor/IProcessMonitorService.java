package com.fa.cim.service.processmonitor;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.RetCode;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;

public interface IProcessMonitorService {
    Results.MonitorBatchDeleteReqResult sxMonitorBatchDeleteReq(Infos.ObjCommon objCommon, ObjectIdentifier monitorLotID);
    void sxMonitorBatchCreateReq(Infos.ObjCommon objCommon, Params.MonitorBatchCreateReqParams monitorBatchCreateReqParams);

    Results.AutoCreateMonitorForInProcessLotReqResult sxAutoCreateMonitorForInProcessLotReq(Infos.ObjCommon objCommon, Params.AutoCreateMonitorForInProcessLotReqParams autoCreateMonitorForInProcessLotReqParams);
    RetCode<Object> sxMonitorHoldDoActionByPostTaskReq(Infos.ObjCommon objCommon, ObjectIdentifier lotID);
}
