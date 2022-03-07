package com.fa.cim.frameworks.pprocess.executor.impl;

import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.constant.CIMStateConst;
import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.utils.CimArrayUtils;
import com.fa.cim.common.utils.CimObjectUtils;
import com.fa.cim.common.utils.CimStringUtils;
import com.fa.cim.common.utils.Validations;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Outputs;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;
import com.fa.cim.dto.pp.CheckEDCLotStatus;
import com.fa.cim.frameworks.dto.pp.PostProcessContext;
import com.fa.cim.frameworks.dto.pp.PostProcessTask;
import com.fa.cim.frameworks.pprocess.api.annotations.PostProcessTaskHandler;
import com.fa.cim.frameworks.pprocess.api.definition.PostProcessExecutor;
import com.fa.cim.frameworks.pprocess.manager.PostProcessTraceManager;
import com.fa.cim.method.IEventMethod;
import com.fa.cim.method.ILotMethod;
import com.fa.cim.newcore.bo.product.CimLot;
import com.fa.cim.newcore.factory.BaseCoreFactory;
import com.fa.cim.service.edc.IEngineerDataCollectionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * <p>EDCActionExecutor .
 * change history:
 * date               defect            person            comments
 * -------------------------------------------------------------------------------------------------------------------
 * 2021/6/10 12:56    ********          ZQI               create file.
 *
 * @author ZQI
 * @version 1.0
 * @date 2021/6/10 12:56
 * @copyright 2021, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 * @since JDK1.8
 */
@Slf4j
@PostProcessTaskHandler
public class EDCActionExecutor implements PostProcessExecutor {

    private final PostProcessTraceManager traceManager;
    private final ILotMethod lotMethod;
    private final IEventMethod eventMethod;
    private final IEngineerDataCollectionService engineerDataCollectionService;

    @Autowired
    public EDCActionExecutor(PostProcessTraceManager traceManager, ILotMethod lotMethod, IEventMethod eventMethod,
                             IEngineerDataCollectionService engineerDataCollectionService) {
        this.traceManager = traceManager;
        this.lotMethod = lotMethod;
        this.eventMethod = eventMethod;
        this.engineerDataCollectionService = engineerDataCollectionService;
    }

    /**
     * CollectedData / SPC Check / Spec Check executor
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

        //-------------------------------------------------
        //   Make Event for Collected Data History
        //-------------------------------------------------
        Boolean isConditionForPO = lotMethod.lotCheckConditionForPO(objCommon, lotID);
        List<Infos.StartCassette> tmpStartCassette;
        //================================================//
        // 如果 conditionForPO = false, 拿 previous 的 PO  //
        // 如果 conditionForPO = true, 拿 current 的 PO    //
        //================================================//
        if (isConditionForPO) {
            // Get information from current ProcessOperation
            final Outputs.ObjLotCurrentOperationDataCollectionInformationGetOut collectionInformationGetOut =
                    lotMethod.lotCurrentOperationDataCollectionInformationGet(objCommon, equipmentID, Collections.singletonList(lotID));
            tmpStartCassette = collectionInformationGetOut.getStrStartCassette();
        } else {
            final Outputs.ObjLotPreviousOperationDataCollectionInformationGetOut collectionInformationGetOut =
                    lotMethod.lotPreviousOperationDataCollectionInformationGet(objCommon, equipmentID, Collections.singletonList(lotID));
            tmpStartCassette = collectionInformationGetOut.getStrStartCassette();
        }

        Validations.check(CimArrayUtils.isEmpty(tmpStartCassette), "Cannot found cassette information in PostProcess");

        if (isConditionForPO) {
            eventMethod.collectedDataEventMake(objCommon,
                    TransactionIDEnum.POST_PROCESS_EXEC_REQ.getValue(),
                    tmpStartCassette,
                    param.getControlJobID(),
                    equipmentID,
                    "EDCActionExecutor");
        } else {
            eventMethod.collectedDataEventForPreviousOperationMake(objCommon,
                    TransactionIDEnum.POST_PROCESS_EXEC_REQ.getValue(),
                    tmpStartCassette,
                    param.getControlJobID(),
                    equipmentID,
                    "EDCActionExecutor");
        }

        //Check lot state
        String lotState = lotMethod.lotStateGet(objCommon, lotID);
        if (CimStringUtils.equals(lotState, CIMStateConst.CIM_LOT_STATE_ACTIVE)) {
            if (log.isDebugEnabled()) log.debug("lot_state = Active. - N/A -");
            //-----------------------------------------------------------------
            // Call sxEDCWithSpecCheckActionByPostTaskReq
            //-----------------------------------------------------------------
            Params.EDCWithSpecCheckActionByPostTaskReqParams checkActionByPostTaskReqParams =
                    new Params.EDCWithSpecCheckActionByPostTaskReqParams();
            checkActionByPostTaskReqParams.setEquipmentID(equipmentID);
            checkActionByPostTaskReqParams.setControlJobID(param.getControlJobID());
            checkActionByPostTaskReqParams.setLotID(lotID);
            if (log.isDebugEnabled()) log.debug("call sxEDCWithSpecCheckActionByPostTaskReq()...");
            engineerDataCollectionService.sxEDCWithSpecCheckActionByPostTaskReq(objCommon, checkActionByPostTaskReqParams);
           //Bug-【PostProcess】空串: 普通模式下Lot 状态显示不对
            PostProcessContext context = traceManager.findPostProcessContextForTaskId(param.getTaskId());
            Object mainResult = context.getMainResult();
            if(mainResult instanceof CheckEDCLotStatus){
                CheckEDCLotStatus checkEDCLotStatus = (CheckEDCLotStatus) mainResult;
                checkEDCLotStatus.overrideLotStatus(lotID,lotMethod.getLotStatus(objCommon, lotID));
            }
            return PostProcessTask.success();
        } else {
            if (log.isDebugEnabled()) log.debug("lot_state != Active. - N/A -");
            return PostProcessTask.success();
        }
    }


}
