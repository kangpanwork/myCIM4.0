package com.fa.cim.frameworks.pprocess.executor.impl;

import com.fa.cim.common.constant.CIMStateConst;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.utils.CimStringUtils;
import com.fa.cim.dto.Infos;
import com.fa.cim.frameworks.dto.pp.PostProcessTask;
import com.fa.cim.frameworks.dto.pp.mode.AvailablePhase;
import com.fa.cim.frameworks.pprocess.api.annotations.PostProcessTaskHandler;
import com.fa.cim.frameworks.pprocess.api.definition.PostProcessExecutor;
import com.fa.cim.method.IContaminationMethod;
import com.fa.cim.method.ILotMethod;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2021/7/10        ********              Decade               create file
 * * @author: Decade
 *
 * @date: 2021/7/10 17:25
 * @copyright: 2021, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@PostProcessTaskHandler(isNextOperationRequired = true)
public class ContaminationOpeCheckExecutor implements PostProcessExecutor {

    private final ILotMethod lotMethod;
    private final IContaminationMethod contaminationMethod;

    @Autowired
    public ContaminationOpeCheckExecutor(ILotMethod lotMethod, IContaminationMethod contaminationMethod) {
        this.lotMethod = lotMethod;
        this.contaminationMethod = contaminationMethod;
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
            contaminationMethod.lotCheckContaminationLevelAndPrFlagStepOut(objCommon, lotID,null);

            return PostProcessTask.success();
        } else {
            if (log.isDebugEnabled()) log.debug("lot_state != Active. - N/A -");
            return PostProcessTask.success();
        }
    }
}