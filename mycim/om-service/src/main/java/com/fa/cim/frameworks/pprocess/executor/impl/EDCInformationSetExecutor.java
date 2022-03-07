package com.fa.cim.frameworks.pprocess.executor.impl;

import com.fa.cim.common.constant.CIMStateConst;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.utils.CimStringUtils;
import com.fa.cim.common.utils.Validations;
import com.fa.cim.dto.Infos;
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
 * <p>EDCInformactinSetExecutor .
 * change history:
 * date               defect            person            comments
 * -------------------------------------------------------------------------------------------------------------------
 * 2021/6/24 14:32    ********          ZQI               create file.
 *
 * @author ZQI
 * @version 1.0
 * @date 2021/6/24 14:32
 * @copyright 2021, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 * @since JDK1.8
 */
@Slf4j
@PostProcessTaskHandler
public class EDCInformationSetExecutor implements PostProcessExecutor {

    private final ILotMethod lotMethod;
    private final IEngineerDataCollectionService engineerDataCollectionService;

    @Autowired
    public EDCInformationSetExecutor(ILotMethod lotMethod,
                                     IEngineerDataCollectionService engineerDataCollectionService) {
        this.lotMethod = lotMethod;
        this.engineerDataCollectionService = engineerDataCollectionService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public PostProcessTask.Result doExecute(PostProcessTask.Param param) {
        final Infos.ObjCommon objCommon = param.getObjCommon();
        final ObjectIdentifier lotID = param.getEntityID();
        final ObjectIdentifier controlJobID = param.getControlJobID();
        final ObjectIdentifier equipmentID = param.getEquipmentID();

        Validations.check(ObjectIdentifier.isEmpty(lotID), "The LotID is null.");
        Validations.check(ObjectIdentifier.isEmpty(equipmentID), "The EquipmentID is null.");

        //-------------------------
        // Get Lot Information
        //-------------------------
        String lotState = lotMethod.lotStateGet(objCommon, lotID);
        if (CimStringUtils.equals(lotState, CIMStateConst.CIM_LOT_STATE_ACTIVE)) {
            if (log.isDebugEnabled()) log.debug("lot_state = Active. - N/A -");

            //-----------------------------------------------------------------
            // Call txFutureHoldPreByPostProcReq
            //-----------------------------------------------------------------
            /*engineerDataCollectionService.sxEDCInformationSetByPostProcReq(objCommon, controlJobID, lotID);*/
            engineerDataCollectionService.sxEDCInformationSetByPostProcReq_2(objCommon, equipmentID, lotID);
            return PostProcessTask.success();
        } else {
            if (log.isDebugEnabled()) log.debug("lot_state != Active. - N/A -");
            return PostProcessTask.success();
        }
    }
}
