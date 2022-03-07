package com.fa.cim.frameworks.pprocess.executor.impl;

import com.fa.cim.common.constant.CIMStateConst;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.utils.CimStringUtils;
import com.fa.cim.common.utils.Validations;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Inputs;
import com.fa.cim.dto.pp.EntityType;
import com.fa.cim.frameworks.dto.pp.PostProcessTask;
import com.fa.cim.frameworks.dto.pp.mode.AvailablePhase;
import com.fa.cim.frameworks.pprocess.api.annotations.PostProcessTaskHandler;
import com.fa.cim.frameworks.pprocess.api.definition.PostProcessExecutor;
import com.fa.cim.method.ILotMethod;
import com.fa.cim.method.IMessageMethod;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>MessageQueueExector .
 * change history:
 * date               defect            person            comments
 * -------------------------------------------------------------------------------------------------------------------
 * 2021/6/10 10:30    ********          ZQI               create file.
 *
 * @author ZQI
 * @version 1.0
 * @date 2021/6/10 10:30
 * @copyright 2021, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 * @since JDK1.8
 */
@Slf4j
@PostProcessTaskHandler(isNextOperationRequired = true)
public class MessageQueueExecutor implements PostProcessExecutor {

    private final IMessageMethod messageMethod;
    private final ILotMethod lotMethod;

    @Autowired
    public MessageQueueExecutor(IMessageMethod messageMethod, ILotMethod lotMethod) {
        this.messageMethod = messageMethod;
        this.lotMethod = lotMethod;
    }

    /**
     * Message Queue Put
     *
     * @param param the necessary params for task handling
     * @return post process result
     */
    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public PostProcessTask.Result doExecute(PostProcessTask.Param param) {
        if (null == param) return PostProcessTask.error(this);
        final EntityType entityType = param.getEntityType();
        final Infos.ObjCommon objCommon = param.getObjCommon();
        final ObjectIdentifier lotID = param.getEntityID();
        final ObjectIdentifier equipmentID = param.getEquipmentID();

        Inputs.MessageQueuePutIn messageQueuePutIn = new Inputs.MessageQueuePutIn();
        switch (entityType) {
            case Equipment:
                if (log.isDebugEnabled()) log.debug("PostProcess target type: Equipment[{}]", equipmentID.getValue());
                // Call messageQueue_Put
                messageQueuePutIn.setEquipmentID(equipmentID);
                messageQueuePutIn.setCassetteTransferReserveFlag(false);
                messageQueuePutIn.setCassetteDispatchReserveFlag(false);
                messageMethod.messageQueuePut(objCommon, messageQueuePutIn);
                break;
            case Lot:
                Validations.check(null == lotID, "Cannot found Lot in PostProcess.");
                if (log.isDebugEnabled()) log.debug("PostProcess target Type: Lot[{}]", lotID.getValue());
                final String lotState = lotMethod.lotStateGet(objCommon, lotID);
                //Check lot state
                if (CimStringUtils.equals(lotState, CIMStateConst.CIM_LOT_STATE_ACTIVE)) {
                    //Check lot Hold state
                    final String lotHoldState = lotMethod.lotHoldStateGet(objCommon, lotID);
                    // If Lot's Hold status is ONHOLD, messageQueuePut is not performed.
                    if (CimStringUtils.equals(lotHoldState, CIMStateConst.CIM_LOT_HOLD_STATE_NOTONHOLD)) {
                        if (log.isDebugEnabled()) log.debug("Lot[{}] hold state [{}]", lotID.getValue(), lotHoldState);
                        messageQueuePutIn.setLotID(lotID);
                        messageQueuePutIn.setLotHoldState(CIMStateConst.CIM_LOT_HOLD_STATE_NOTONHOLD);
                        messageQueuePutIn.setCassetteDispatchReserveFlag(false);
                        messageQueuePutIn.setCassetteTransferReserveFlag(false);
                        messageMethod.messageQueuePut(objCommon, messageQueuePutIn);
                    }
                }
                break;
            default:
                if (log.isDebugEnabled())
                    log.debug("PostProcess target type is not Lot or Equipment , so do not executor MessagePut!!!");
                break;
        }
        return PostProcessTask.success();
    }
}
