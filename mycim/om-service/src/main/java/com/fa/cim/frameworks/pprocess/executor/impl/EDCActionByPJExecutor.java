package com.fa.cim.frameworks.pprocess.executor.impl;

import com.fa.cim.common.constant.CIMStateConst;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.utils.CimStringUtils;
import com.fa.cim.common.utils.Validations;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.frameworks.dto.pp.PostProcessTask;
import com.fa.cim.frameworks.dto.pp.mode.AvailablePhase;
import com.fa.cim.frameworks.pprocess.api.annotations.PostProcessTaskHandler;
import com.fa.cim.frameworks.pprocess.api.definition.PostProcessExecutor;
import com.fa.cim.method.ILotMethod;
import com.fa.cim.service.edc.IEngineerDataCollectionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>EDCActionByPJExecutor .
 * change history:
 * date               defect            person            comments
 * -------------------------------------------------------------------------------------------------------------------
 * 2021/6/10 14:25    ********          ZQI               create file.
 *
 * @author ZQI
 * @version 1.0
 * @date 2021/6/10 14:25
 * @copyright 2021, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 * @since JDK1.8
 */
@Slf4j
@PostProcessTaskHandler
public class EDCActionByPJExecutor implements PostProcessExecutor {

    private final ILotMethod lotMethod;
    private final IEngineerDataCollectionService engineerDataCollectionService;
    private final RetCodeConfig retCodeConfig;

    @Autowired
    public EDCActionByPJExecutor(ILotMethod lotMethod, IEngineerDataCollectionService engineerDataCollectionService,
                                 RetCodeConfig retCodeConfig) {
        this.lotMethod = lotMethod;
        this.engineerDataCollectionService = engineerDataCollectionService;
        this.retCodeConfig = retCodeConfig;
    }


    /**
     * CollectedData action by process job
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

            //-----------------------------------------------------------------
            // Call sxEDCWithSpecCheckActionByPJReq
            //-----------------------------------------------------------------
            Params.EDCWithSpecCheckActionByPJReqParams edcWithSpecCheckActionByPJReqParams = new Params.EDCWithSpecCheckActionByPJReqParams();
            edcWithSpecCheckActionByPJReqParams.setEquipmentID(equipmentID);
            edcWithSpecCheckActionByPJReqParams.setControlJobID(controlJobID);
            edcWithSpecCheckActionByPJReqParams.setLotID(lotID);
            edcWithSpecCheckActionByPJReqParams.setClaimMemo("EDCActionByPJExecutor");
            try {
                engineerDataCollectionService.sxEDCWithSpecCheckActionByPJReq(objCommon, edcWithSpecCheckActionByPJReqParams);
            } catch (ServiceException e) {
                if (Validations.isEquals(retCodeConfig.getSpeccheckError(), e.getCode())) {
                    if (log.isDebugEnabled()) log.debug(e.getMessage());
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
