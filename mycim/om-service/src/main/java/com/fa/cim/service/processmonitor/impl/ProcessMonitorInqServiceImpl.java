package com.fa.cim.service.processmonitor.impl;

import com.fa.cim.annotaion.OmService;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.method.IMonitorGroupMethod;
import com.fa.cim.service.processmonitor.IProcessMonitorInqService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@OmService
@Slf4j
public class ProcessMonitorInqServiceImpl implements IProcessMonitorInqService {

    @Autowired
    private IMonitorGroupMethod monitorGroupMethod;

    @Override
    public List<Infos.MonitorGroups> sxMonitorBatchRelationInq(Infos.ObjCommon objCommon, Params.MonitorBatchRelationInqParams monitorBatchRelationInqParams) {
        /*------------------------------------------------------------------------*/
        /*   Set Inquired Data                                                    */
        /*------------------------------------------------------------------------*/

        /*------------------------------------------------------------------------*/
        /*   Return                                                               */
        /*------------------------------------------------------------------------*/
        return monitorGroupMethod.monitorGroupGetDR(objCommon, monitorBatchRelationInqParams.getLotID());
    }
}
