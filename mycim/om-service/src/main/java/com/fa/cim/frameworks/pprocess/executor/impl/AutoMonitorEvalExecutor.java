package com.fa.cim.frameworks.pprocess.executor.impl;

import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.constant.CIMStateConst;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.utils.CimStringUtils;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.frameworks.dto.pp.PostProcessTask;
import com.fa.cim.frameworks.pprocess.api.annotations.PostProcessTaskHandler;
import com.fa.cim.frameworks.pprocess.api.definition.PostProcessExecutor;
import com.fa.cim.method.ILotMethod;
import com.fa.cim.newcore.impl.bo.env.StandardProperties;
import com.fa.cim.service.automonitor.IAutoMonitorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>AutoMonitorEvalExecutor .
 * change history:
 * date               defect            person            comments
 * -------------------------------------------------------------------------------------------------------------------
 * 2021/6/10 15:17    ********          ZQI               create file.
 *
 * @author ZQI
 * @version 1.0
 * @date 2021/6/10 15:17
 * @copyright 2021, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 * @since JDK1.8
 */
@Slf4j
@PostProcessTaskHandler
public class AutoMonitorEvalExecutor implements PostProcessExecutor {

    private final ILotMethod lotMethod;
    private final IAutoMonitorService autoMonitorService;

    @Autowired
    public AutoMonitorEvalExecutor(ILotMethod lotMethod, IAutoMonitorService autoMonitorService) {
        this.lotMethod = lotMethod;
        this.autoMonitorService = autoMonitorService;
    }

    /**
     * Auto Monitor Eval
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
        final ObjectIdentifier equipmentID = param.getEquipmentID();
        final ObjectIdentifier controlJobID = param.getControlJobID();

        String lotState = lotMethod.lotStateGet(objCommon, lotID);
        if (CimStringUtils.equals(lotState, CIMStateConst.CIM_LOT_STATE_ACTIVE)) {
            if (log.isDebugEnabled()) log.debug("lot_state = Active. - N/A -");
            final int eqpmonitorSwitch = StandardProperties.OM_AUTOMON_FLAG.getIntValue();
            if (eqpmonitorSwitch == 1) {
                //Check Lot type
                String lotType = lotMethod.lotTypeGet(objCommon, lotID);
                if (CimStringUtils.equals(lotType, BizConstant.SP_LOT_TYPE_EQUIPMENTMONITORLOT)
                        || CimStringUtils.equals(lotType, BizConstant.SP_LOT_TYPE_DUMMYLOT)) {
                    Infos.EqpMonitorJobLotInfo eqpMonitorJobLotInfo = lotMethod.lotEqpMonitorJobGet(objCommon, lotID);
                    if (ObjectIdentifier.isEmptyWithValue(eqpMonitorJobLotInfo.getEqpMonitorJobID())) {
                        // Post process action "AutoMonitorEval" can be skipped
                        //  - Lot isn't involved in EqpMonitor job
                        //  - No violation in Spec/SPC check
                        if (log.isDebugEnabled()) log.debug("AutoMonitorEval can be skipped");
                    } else {
                        if (log.isDebugEnabled()) log.debug("AutoMonitorEval can not be skipped");
                        //Call txAMVerifyReq to the object Lot.
                        Params.AMVerifyReqInParams amVerifyReqInParams = new Params.AMVerifyReqInParams();
                        amVerifyReqInParams.setEquipmentID(equipmentID);
                        amVerifyReqInParams.setControlJobID(controlJobID);
                        amVerifyReqInParams.setLotID(lotID);
                        autoMonitorService.sxAMVerifyReq(objCommon, amVerifyReqInParams);
                    }
                }
            }
            return PostProcessTask.success();
        } else {
            if (log.isDebugEnabled()) log.debug("lot_state != Active. - N/A -");
            return PostProcessTask.success();
        }
    }
}
