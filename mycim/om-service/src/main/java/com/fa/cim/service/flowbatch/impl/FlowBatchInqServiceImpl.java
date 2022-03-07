package com.fa.cim.service.flowbatch.impl;

import com.fa.cim.annotaion.OmService;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.OmCode;
import com.fa.cim.common.utils.Validations;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Outputs;
import com.fa.cim.dto.Results;
import com.fa.cim.method.IEquipmentMethod;
import com.fa.cim.method.IFlowBatchMethod;
import com.fa.cim.method.ILotMethod;
import com.fa.cim.service.flowbatch.IFlowBatchInqService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * description:
 * <p>FlowBatchInqService .<br/></p>
 * <p>
 * change history:
 * date      defect#       person     comments
 * ------------------------------------------------------------
 * ---------------------------------------------------------
 * 2020/9/8/008   ********     Decade     create file
 *
 * @author: Decade
 * @date: 2020/9/8/008 16:46
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@OmService
@Slf4j
public class FlowBatchInqServiceImpl implements IFlowBatchInqService {

    @Autowired
    private ILotMethod lotMethod;

    @Autowired
    private IFlowBatchMethod flowBatchMethod;

    @Autowired
    private IEquipmentMethod equipmentMethod;

    @Autowired
    private RetCodeConfig retCodeConfig;

    @Override
    public Results.FloatingBatchListInqResult sxFloatingBatchListInq(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID) {
        log.info("enter the method[sxFlowBatchInfoInq]");
        Results.FloatingBatchListInqResult flowBatchData = flowBatchMethod.flowBatchFillInTxDSQ002DR(objCommon, equipmentID);
        flowBatchData = lotMethod.lotFillInTxDSQ002DR(flowBatchData, objCommon);
        log.info("exit the method[sxFlowBatchInfoInq]");
        return flowBatchData;
    }

    @Override
    public Results.FlowBatchLotSelectionInqResult sxFlowBatchLotSelectionInq(Infos.ObjCommon strObjCommonIn, ObjectIdentifier equipmentID) {
        // step1 - equipment_flowBatchWaitLots_GetDR__090
        Results.FlowBatchLotSelectionInqResult flowBatchLotSelectionInqResult = null;
        try {
            flowBatchLotSelectionInqResult = equipmentMethod.equipmentFlowBatchWaitLotsGetDR(strObjCommonIn, equipmentID);
        } catch (ServiceException e){
            if (!Validations.isEquals(retCodeConfig.getNotFoundFlowbatchCandLot(), e.getCode())){
                throw e;
            }
            flowBatchLotSelectionInqResult = e.getData(Results.FlowBatchLotSelectionInqResult.class);
        }

        return flowBatchLotSelectionInqResult;
    }

    @Override
    public Results.FlowBatchInfoInqResult sxFlowBatchInfoInq(Infos.ObjCommon objCommon, ObjectIdentifier flowBatchID, ObjectIdentifier lotID, ObjectIdentifier equipmentID) {
        Results.FlowBatchInfoInqResult outObject = new Results.FlowBatchInfoInqResult();
        outObject.setMaxCountForFlowBatch(0L);

        int inputCount = 0;
        if(!ObjectIdentifier.isEmptyWithValue(lotID)){
            inputCount++;
        }
        if(!ObjectIdentifier.isEmptyWithValue(equipmentID)){
            inputCount++;
        }
        if(!ObjectIdentifier.isEmptyWithValue(flowBatchID)){
            inputCount++;
        }
        if (inputCount != 1){
            StringBuffer errorMsg = new StringBuffer();
            errorMsg.append("Only one parameter should be specified among the input parameters: lotID, equipmentID or flowBatchID.");
            throw new ServiceException(new OmCode(retCodeConfig.getInvalidParameterWithMsg(), errorMsg.toString()));
        }

        Infos.FlowBatchInformation flowBatchInformation = new Infos.FlowBatchInformation();
        flowBatchInformation.setEquipmentID(equipmentID);
        flowBatchInformation.setLotID(lotID);
        flowBatchInformation.setFlowBatchID(flowBatchID);
        Outputs.ObjFlowBatchInformationGetOut out = flowBatchMethod.flowBatchInformationGet(objCommon, flowBatchInformation);

        outObject.setMaxCountForFlowBatch(out.getMaxCountForFlowBatch());
        outObject.setReservedEquipmentID(out.getReservedEquipmentID());
        outObject.setStrFlowBatchInfoList(out.getFlowBatchInfoList());
        return outObject;
    }

    @Override
    public Results.FlowBatchStrayLotsListInqResult sxFlowBatchStrayLotsListInq(Infos.ObjCommon objCommon) {
        Results.FlowBatchStrayLotsListInqResult result = new Results.FlowBatchStrayLotsListInqResult();
        // step1 flowBatch_LostLotsList_GetDR
        Outputs.ObjFlowBatchLostLotsListGetDRout objProcessFutureQrestTimeInfoGetDROut = flowBatchMethod.flowBatchLostLotsListGetDR(objCommon);
        result.setFlowBatchLostLotsList(objProcessFutureQrestTimeInfoGetDROut.getFlowBatchedCassetteInfoList());
        return  result;
    }
}
