package com.fa.cim.frameworks.pprocess.executor.impl;

import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.constant.CIMStateConst;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.utils.CimStringUtils;
import com.fa.cim.common.utils.ThreadContextHolder;
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
 * <p>QTimeActionExecutor .
 * change history:
 * date               defect            person            comments
 * -------------------------------------------------------------------------------------------------------------------
 * 2021/6/10 13:21    ********          ZQI               create file.
 *
 * @author ZQI
 * @version 1.0
 * @date 2021/6/10 13:21
 * @copyright 2021, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 * @since JDK1.8
 */
@Slf4j
@PostProcessTaskHandler
public class QTimeActionExecutor implements PostProcessExecutor {

    private final ILotMethod lotMethod;
    private final IProcessControlService processControlService;

    @Autowired
    public QTimeActionExecutor(ILotMethod lotMethod, IProcessControlService processControlService) {
        this.lotMethod = lotMethod;
        this.processControlService = processControlService;
    }

    /**
     * Q-Time action executor
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
            param.getDetails().forEach(detail -> {
                if (CimStringUtils.equals(detail.getName(), BizConstant.SP_THREADSPECIFICDATA_KEY_PREVIOUSBRANCHINFO)) {
                    // Store PreviousBranchInfo as thread specific data
                    ThreadContextHolder.setThreadSpecificDataString(BizConstant.SP_THREADSPECIFICDATA_KEY_PREVIOUSBRANCHINFO,
                            detail.getValue());
                } else if (CimStringUtils.equals(detail.getName(), BizConstant.SP_THREADSPECIFICDATA_KEY_PREVIOUSRETURNINFO)) {
                    // Store PreviousReturnInfo as thread specific data
                    ThreadContextHolder.setThreadSpecificDataString(BizConstant.SP_THREADSPECIFICDATA_KEY_PREVIOUSRETURNINFO,
                            detail.getValue());
                } else if (CimStringUtils.equals(detail.getName(), BizConstant.SP_THREADSPECIFICDATA_KEY_PREVIOUSREWORKOUTKEY)) {
                    // Store PreviousReworkOutKey as thread specific data
                    ThreadContextHolder.setThreadSpecificDataString(BizConstant.SP_THREADSPECIFICDATA_KEY_PREVIOUSREWORKOUTKEY,
                            detail.getValue());
                }
            });

            //-----------------------------------------------------------------
            // call sxQtimeManageActionByPostTaskReq
            //-----------------------------------------------------------------
            Params.QtimeManageActionByPostTaskReqInParm strQtimeManageActionByPostTaskReqInParm =
                    new Params.QtimeManageActionByPostTaskReqInParm();
            strQtimeManageActionByPostTaskReqInParm.setLotID(lotID);
            if (log.isDebugEnabled()) log.debug("call sxQtimeManageActionByPostTaskReq()...");
            processControlService.sxQtimeManageActionByPostTaskReq(objCommon,
                    strQtimeManageActionByPostTaskReqInParm, "QTimeActionExecutor");

            return PostProcessTask.success();
        } else {
            if (log.isDebugEnabled()) log.debug("lot_state != Active. - N/A -");
            return PostProcessTask.success();
        }
    }
}
