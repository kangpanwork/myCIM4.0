package com.fa.cim.frameworks.pprocess.executor.impl;

import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.constant.CIMStateConst;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.utils.CimStringUtils;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.frameworks.dto.pp.PostProcessTask;
import com.fa.cim.frameworks.dto.pp.mode.AvailablePhase;
import com.fa.cim.frameworks.pprocess.api.annotations.PostProcessTaskHandler;
import com.fa.cim.frameworks.pprocess.api.definition.PostProcessExecutor;
import com.fa.cim.method.ILotMethod;
import com.fa.cim.service.processcontrol.IProcessControlService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>LagTimeActionExecutor .
 * change history:
 * date               defect            person            comments
 * -------------------------------------------------------------------------------------------------------------------
 * 2021/6/10 13:37    ********          ZQI               create file.
 *
 * @author ZQI
 * @version 1.0
 * @date 2021/6/10 13:37
 * @copyright 2021, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 * @since JDK1.8
 */
@Slf4j
@PostProcessTaskHandler
public class LagTimeActionExecutor implements PostProcessExecutor {

    private final ILotMethod lotMethod;
    private final IProcessControlService processControlService;

    @Autowired
    public LagTimeActionExecutor(ILotMethod lotMethod, IProcessControlService processControlService) {
        this.lotMethod = lotMethod;
        this.processControlService = processControlService;
    }

    /**
     * Process Lag Time action executor
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
        if (CimStringUtils.equals(lotState, CIMStateConst.CIM_LOT_STATE_ACTIVE)) {
            if (log.isDebugEnabled()) log.debug("lot_state = Active. - N/A -");
            //-----------------------------------------------------------------
            // call sxProcessLagTimeUpdate
            //-----------------------------------------------------------------
            Params.LagTimeActionReqParams lagTimeActionReqParams = new Params.LagTimeActionReqParams();
            lagTimeActionReqParams.setLotID(lotID);
            lagTimeActionReqParams.setAction(BizConstant.SP_PROCESSLAGTIME_ACTION_SET);
            lagTimeActionReqParams.setClaimMemo("LagTimeActionExecutor");
            if (log.isDebugEnabled()) log.debug("call sxProcessLagTimeUpdate()...");
            processControlService.sxProcessLagTimeUpdate(objCommon, lagTimeActionReqParams);

            return PostProcessTask.success();
        } else {
            if (log.isDebugEnabled()) log.debug("lot_state != Active. - N/A -");
            return PostProcessTask.success();
        }
    }
}
