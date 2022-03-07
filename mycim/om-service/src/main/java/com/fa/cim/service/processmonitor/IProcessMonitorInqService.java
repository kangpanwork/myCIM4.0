package com.fa.cim.service.processmonitor;

import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;

import java.util.List;

public interface IProcessMonitorInqService {
    List<Infos.MonitorGroups> sxMonitorBatchRelationInq(Infos.ObjCommon objCommon, Params.MonitorBatchRelationInqParams monitorBatchRelationInqParams);
}
