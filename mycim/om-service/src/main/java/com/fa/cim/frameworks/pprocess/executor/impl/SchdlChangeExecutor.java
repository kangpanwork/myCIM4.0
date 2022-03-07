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
import com.fa.cim.service.plan.IPlanService;
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
 * 2021/8/14        ********            AOKI                create file
 *
 * @author: AOKI
 * @date: 2021/8/14 10:43
 * @copyright: 2021, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@PostProcessTaskHandler
public class SchdlChangeExecutor implements PostProcessExecutor {

    private final IPlanService planService;

    private final ILotMethod lotMethod;

    @Autowired
    public SchdlChangeExecutor(IPlanService planService,ILotMethod lotMethod) {
        this.planService = planService;
        this.lotMethod = lotMethod;
    }

    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public PostProcessTask.Result doExecute(PostProcessTask.Param param) {

        // ----------------------------------
        // check lot status is active        |
        // ----------------------------------
        ObjectIdentifier lotID = param.getEntityID();
        Infos.ObjCommon objCommon = param.getObjCommon();
        String lotStatus = lotMethod.lotStateGet(objCommon, lotID);
        boolean isActive = CimStringUtils.equals(lotStatus, CIMStateConst.CIM_LOT_STATE_ACTIVE);
        if (isActive){
            // -------------------------------------
            // call schedule change reserved        |
            // -------------------------------------
            planService.sxSchdlChangeReservationExecuteByPostProcReq(objCommon,lotID);
            if (log.isDebugEnabled()) {
                log.debug("call SchdlChangeExecutor sucess, call sxSchdlChangeReservationExecuteByPostProcReq");
            }
            return PostProcessTask.success();
        } else {
            if (log.isDebugEnabled()) {
                log.debug("lot_state != Active. - N/A -");
            }
            return PostProcessTask.success();
        }

    }
}