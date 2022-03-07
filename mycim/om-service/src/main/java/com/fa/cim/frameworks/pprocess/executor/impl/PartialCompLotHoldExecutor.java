package com.fa.cim.frameworks.pprocess.executor.impl;

import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.constant.CIMStateConst;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.utils.CimArrayUtils;
import com.fa.cim.common.utils.CimStringUtils;
import com.fa.cim.common.utils.Validations;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Inputs;
import com.fa.cim.dto.Outputs;
import com.fa.cim.frameworks.dto.pp.PostProcessTask;
import com.fa.cim.frameworks.pprocess.api.annotations.PostProcessTaskHandler;
import com.fa.cim.frameworks.pprocess.api.definition.PostProcessExecutor;
import com.fa.cim.method.IControlJobMethod;
import com.fa.cim.method.ILotMethod;
import com.fa.cim.service.lot.ILotService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>PartialCompLotHoldExecutor .
 * change history:
 * date               defect            person            comments
 * -------------------------------------------------------------------------------------------------------------------
 * 2021/6/10 14:10    ********          ZQI               create file.
 *
 * @author ZQI
 * @version 1.0
 * @date 2021/6/10 14:10
 * @copyright 2021, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 * @since JDK1.8
 */
@Slf4j
@PostProcessTaskHandler
public class PartialCompLotHoldExecutor implements PostProcessExecutor {


    private final ILotMethod lotMethod;

    private final IControlJobMethod controlJobMethod;

    private final ILotService lotService;

    @Autowired
    private RetCodeConfig retCodeConfig;

    @Autowired
    public PartialCompLotHoldExecutor(ILotMethod lotMethod, IControlJobMethod controlJobMethod,
                                      ILotService lotService) {
        this.lotMethod = lotMethod;
        this.controlJobMethod = controlJobMethod;
        this.lotService = lotService;
    }


    /**
     * Partial move out lot hold
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
        final ObjectIdentifier controlJobID = param.getControlJobID();

        String lotState = lotMethod.lotStateGet(objCommon, lotID);
        if (CimStringUtils.equals(lotState, CIMStateConst.CIM_LOT_STATE_ACTIVE)) {
            if (log.isDebugEnabled()) log.debug("lot_state = Active. - N/A -");
            //--------------------------------------------------------------------
            // Check if hold target lot is OpeComped lot or OpeStartCancelled lot
            //--------------------------------------------------------------------
            boolean previousPOFlag = false;
            Inputs.ObjControlJobProcessOperationListGetDRIn objControlJobProcessOperationListGetDRIn =
                    new Inputs.ObjControlJobProcessOperationListGetDRIn();
            objControlJobProcessOperationListGetDRIn.setControlJobID(controlJobID);
            List<Infos.ProcessOperationLot> processOperationLots = null;
            try {
                processOperationLots = controlJobMethod
                        .controlJobProcessOperationListGetDR(objCommon, objControlJobProcessOperationListGetDRIn);
            } catch (ServiceException e) {
                if (Validations.isEquals(retCodeConfig.getNotFoundLotInControlJob(), e.getCode())) {
                    // There is no PO which have input control job -> lot is OpeStartCancelled
                    // Get PO from Current PO
                    if(log.isDebugEnabled()) log.debug("There is no PO which have input control job.");
                } else {
                    throw e;
                }
            }
            if (CimArrayUtils.isNotEmpty(processOperationLots)) {
                for (Infos.ProcessOperationLot processOperationLot : processOperationLots) {
                    if (ObjectIdentifier.equalsWithValue(lotID, processOperationLot.getLotID())
                    && !lotMethod.checkLotMoveNextRequired(objCommon, lotID)) {
                        // PO which have input control job and lot ID is found
                        previousPOFlag = true;
                    }
                }
            }

            //-----------------------------------------------------------------
            // Call sxHoldLotReq
            //-----------------------------------------------------------------
            List<Infos.LotHoldReq> strHoldListSeq2 = new ArrayList<>();
            if (previousPOFlag) {
                Outputs.ObjLotPreviousOperationInfoGetOut objLotPreviousOperationInfoGetOut = lotMethod
                        .lotPreviousOperationInfoGet(objCommon, lotID);
                Infos.LotHoldReq holdList = new Infos.LotHoldReq();
                strHoldListSeq2.add(holdList);
                holdList.setHoldType(BizConstant.SP_HOLDTYPE_LOTHOLD);
                holdList.setHoldReasonCodeID(new ObjectIdentifier(BizConstant.SP_REASON_PARTIALOPECOMPHOLD));
                holdList.setHoldUserID(objCommon.getUser().getUserID());
                holdList.setResponsibleOperationMark(BizConstant.SP_RESPONSIBLEOPERATION_PREVIOUS);
                holdList.setRouteID(objLotPreviousOperationInfoGetOut.getRouteID());
                holdList.setOperationNumber(objLotPreviousOperationInfoGetOut.getOperationNumber());
                holdList.setClaimMemo("");
            } else {
                Outputs.ObjLotCurrentOperationInfoGetOut objLotCurrentOperationInfoGetOut = lotMethod
                        .lotCurrentOperationInfoGet(objCommon, lotID);
                Infos.LotHoldReq holdList = new Infos.LotHoldReq();
                strHoldListSeq2.add(holdList);
                holdList.setHoldType(BizConstant.SP_HOLDTYPE_LOTHOLD);
                holdList.setHoldReasonCodeID(new ObjectIdentifier(BizConstant.SP_REASON_PARTIALOPECOMPHOLD));
                holdList.setHoldUserID(objCommon.getUser().getUserID());
                holdList.setResponsibleOperationMark(BizConstant.SP_RESPONSIBLEOPERATION_CURRENT);
                holdList.setRouteID(objLotCurrentOperationInfoGetOut.getRouteID());
                holdList.setOperationNumber(objLotCurrentOperationInfoGetOut.getOperationNumber());
                holdList.setClaimMemo("");
            }
            if (log.isDebugEnabled()) log.debug("call sxHoldLotReq()...");
            lotService.sxHoldLotReq(objCommon, lotID, strHoldListSeq2);

            return PostProcessTask.success();
        } else {
            if (log.isDebugEnabled()) log.debug("lot_state != Active. - N/A -");
            return PostProcessTask.success();
        }
    }
}
