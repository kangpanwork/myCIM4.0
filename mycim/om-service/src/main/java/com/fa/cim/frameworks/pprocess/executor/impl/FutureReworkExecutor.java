package com.fa.cim.frameworks.pprocess.executor.impl;

import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.utils.CimStringUtils;
import com.fa.cim.common.utils.Validations;
import com.fa.cim.config.RetCodeConfigEx;
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

/**
 * <p>FutureReworkExecutor .
 * change history:
 * date               defect            person            comments
 * -------------------------------------------------------------------------------------------------------------------
 * 2021/6/10 11:18    ********          ZQI               create file.
 *
 * @author ZQI
 * @version 1.0
 * @date 2021/6/10 11:18
 * @copyright 2021, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 * @since JDK1.8
 */
@Slf4j
@PostProcessTaskHandler(isNextOperationRequired = true)
public class FutureReworkExecutor implements PostProcessExecutor {

    private final ILotMethod lotMethod;
    private final IProcessControlService processControlService;
    private final RetCodeConfigEx retCodeConfigEx;

    @Autowired
    public FutureReworkExecutor(ILotMethod lotMethod, IProcessControlService processControlService,
                                RetCodeConfigEx retCodeConfigEx) {
        this.lotMethod = lotMethod;
        this.processControlService = processControlService;
        this.retCodeConfigEx = retCodeConfigEx;
    }


    /**
     * Future rework action
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

        // Check lot state
        final String lotState = lotMethod.lotStateGet(objCommon, lotID);
        if (CimStringUtils.equals(lotState, BizConstant.CIMFW_LOT_STATE_ACTIVE)) {
            if (log.isDebugEnabled()) log.debug("lot_state = Active. - N/A -");

            //--------------------------------
            // Call txFutureReworkActionDoReq
            //--------------------------------
            try {
                processControlService.sxFutureReworkActionDoReq(objCommon, lotID, "FutureReworkExecutor");
            } catch (ServiceException e) {
                if (Validations.isEquals(retCodeConfigEx.getFtrwkNotFound(), e.getCode())) {
                    if (log.isDebugEnabled()) log.debug(">>> Not found Future Rework in PostProcess.Do nothing...");
                } else {
                    throw e;
                }
            }
            return PostProcessTask.success();
        } else {
            if (log.isDebugEnabled()) log.debug("lot_state != Active. - N/A -");
            return PostProcessTask.success();
        }
    }
}
