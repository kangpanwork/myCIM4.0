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
import com.fa.cim.service.lot.ILotService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>NPWLotSkipExecutor .
 * change history:
 * date               defect            person            comments
 * -------------------------------------------------------------------------------------------------------------------
 * 2021/6/10 15:59    ********          ZQI               create file.
 *
 * @author ZQI
 * @version 1.0
 * @date 2021/6/10 15:59
 * @copyright 2021, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 * @since JDK1.8
 */
@Slf4j
@PostProcessTaskHandler(isNextOperationRequired = true)
public class NPWLotSkipExecutor implements PostProcessExecutor {

    private final ILotMethod lotMethod;
    private final ILotService lotService;

    @Autowired
    public NPWLotSkipExecutor(ILotMethod lotMethod, ILotService lotService) {
        this.lotMethod = lotMethod;
        this.lotService = lotService;
    }

    /**
     * Post Actions for npw lot final step skip.
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
            if (log.isDebugEnabled()) log.debug("lot_state == Active. - N/A -");

            //------------------------------------------
            // call sxNPWLotAutoSkipCheckAndExcute
            //------------------------------------------
            if (log.isDebugEnabled()) log.debug("call sxNPWLotAutoSkipCheckAndExcute(...)");
            lotService.sxNPWLotAutoSkipCheckAndExcute(objCommon, lotID);

            return PostProcessTask.success();
        } else {
            if (log.isDebugEnabled()) log.debug("lot_state != Active. - N/A -");
            return PostProcessTask.success();
        }
    }
}
