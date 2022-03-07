package com.fa.cim.frameworks.pprocess.executor.impl;

import com.fa.cim.common.constant.CIMStateConst;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.utils.CimStringUtils;
import com.fa.cim.dto.Infos;
import com.fa.cim.frameworks.dto.pp.PostProcessTask;
import com.fa.cim.frameworks.dto.pp.mode.AvailablePhase;
import com.fa.cim.frameworks.pprocess.api.annotations.PostProcessTaskHandler;
import com.fa.cim.frameworks.pprocess.api.definition.PostProcessExecutor;
import com.fa.cim.method.ILotMethod;
import com.fa.cim.service.processmonitor.IProcessMonitorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>MonitorLotHoldExecutor .
 * change history:
 * date               defect            person            comments
 * -------------------------------------------------------------------------------------------------------------------
 * 2021/6/10 13:43    ********          ZQI               create file.
 *
 * @author ZQI
 * @version 1.0
 * @date 2021/6/10 13:43
 * @copyright 2021, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 * @since JDK1.8
 */
@Slf4j
@PostProcessTaskHandler
public class MonitorLotHoldExecutor implements PostProcessExecutor {

    private final ILotMethod lotMethod;
    private final IProcessMonitorService processMonitorService;

    @Autowired
    public MonitorLotHoldExecutor(ILotMethod lotMethod, IProcessMonitorService processMonitorService) {
        this.lotMethod = lotMethod;
        this.processMonitorService = processMonitorService;
    }

    /**
     * If the lot is Monitoring Lot, and if it’s operation is monitor process,
     * system holds the lots which are grouped by monitor group in this post process.
     *
     * @param param the necessary params for task handling
     * @return post process result
     */
    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public PostProcessTask.Result doExecute(PostProcessTask.Param param) {
        if (null == param) return PostProcessTask.error(this);
        final Infos.ObjCommon objCommon = param.getObjCommon();
        final ObjectIdentifier lotID = param.getEntityID();

        String lotState = lotMethod.lotStateGet(objCommon, lotID);
        if (CimStringUtils.equals(CIMStateConst.CIM_LOT_STATE_ACTIVE, lotState)) {
            if (log.isDebugEnabled()) log.debug("lot_state = Active. - N/A -");

            //------------------------------------------------
            // call sxMonitorHoldDoActionByPostTaskReq
            //------------------------------------------------
            if (log.isDebugEnabled()) log.debug("call sxMonitorHoldDoActionByPostTaskReq()...");
            processMonitorService.sxMonitorHoldDoActionByPostTaskReq(objCommon, lotID);

            return PostProcessTask.success();
        } else {
            if (log.isDebugEnabled()) log.debug("lot_state != Active. - N/A -");
            return PostProcessTask.success();
        }
    }
}
