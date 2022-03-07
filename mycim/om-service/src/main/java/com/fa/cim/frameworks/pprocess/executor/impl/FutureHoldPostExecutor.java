package com.fa.cim.frameworks.pprocess.executor.impl;

import com.fa.cim.common.constant.CIMStateConst;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.utils.CimStringUtils;
import com.fa.cim.dto.Infos;
import com.fa.cim.frameworks.dto.pp.PostProcessTask;
import com.fa.cim.frameworks.pprocess.api.annotations.PostProcessTaskHandler;
import com.fa.cim.frameworks.pprocess.api.definition.PostProcessExecutor;
import com.fa.cim.method.ILotMethod;
import com.fa.cim.service.processcontrol.IProcessControlService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@PostProcessTaskHandler
public class FutureHoldPostExecutor implements PostProcessExecutor {

    private final ILotMethod lotMethod;
    private final IProcessControlService processControlService;

    @Autowired
    public FutureHoldPostExecutor(ILotMethod lotMethod, IProcessControlService processControlService) {
        this.lotMethod = lotMethod;
        this.processControlService = processControlService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public PostProcessTask.Result doExecute(PostProcessTask.Param param) {
        ObjectIdentifier lotID = param.getEntityID();
        Infos.ObjCommon objCommon = param.getObjCommon();

        String lotState = lotMethod.lotStateGet(objCommon, lotID);
        if (CimStringUtils.equals(lotState, CIMStateConst.CIM_LOT_STATE_ACTIVE)) {
            if (log.isDebugEnabled()) log.debug("lot_state = Active. - N/A -");

            //-----------------------------------------------------------------
            // Call txFutureHoldPreByPostProcReq
            //-----------------------------------------------------------------
            processControlService.sxFutureHoldPostByPostProcessReq(objCommon, lotID, "");
            return PostProcessTask.success();
        } else {
            if (log.isDebugEnabled()) log.debug("lot_state != Active. - N/A -");
            return PostProcessTask.success();
        }
    }

}
