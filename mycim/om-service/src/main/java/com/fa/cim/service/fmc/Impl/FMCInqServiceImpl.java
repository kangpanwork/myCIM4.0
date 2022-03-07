package com.fa.cim.service.fmc.Impl;

import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.OmCode;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.config.RetCodeConfigEx;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;
import com.fa.cim.method.*;
import com.fa.cim.service.fmc.IFMCInqService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/9/8          ********            lightyh                create file
 *
 * @author: lightyh
 * @date: 2020/9/8 17:15
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Service
@Slf4j
public class FMCInqServiceImpl implements IFMCInqService {

    @Autowired
    private RetCodeConfig retCodeConfig;

    @Autowired
    private RetCodeConfigEx retCodeConfigEx;

    @Autowired
    private IEquipmentMethod equipmentMethod;

    @Autowired
    private IObjectMethod objectMethod;

    @Autowired
    private IEquipmentContainerPositionMethod equipmentContainerPositionMethod;

    @Autowired
    private IObjectLockMethod objectLockMethod;

    @Autowired
    private ILotMethod lotMethod;

    @Autowired
    private ISLMMethod slmMethod;

    @Override
    public Results.SLMCandidateCassetteForRetrievingInqResult sxSLMCandidateCassetteForRetrievingInq(Infos.ObjCommon objCommon , Params.SLMCandidateCassetteForRetrievingInqInParams slmCandidateCassetteForRetrievingInqInParams){
        ObjectIdentifier equipmentID = slmCandidateCassetteForRetrievingInqInParams.getEquipmentID();
        ObjectIdentifier lotID = slmCandidateCassetteForRetrievingInqInParams.getLotID();
        ObjectIdentifier portID = slmCandidateCassetteForRetrievingInqInParams.getPortID();

        Infos.EqpBrInfo eqpBrInfo = equipmentMethod.equipmentBRInfoGetDR(objCommon, equipmentID);
        if (!eqpBrInfo.isFmcCapabilityFlag()){
            throw new ServiceException(new OmCode(retCodeConfigEx.getEqpSlmCapabilityOff(),equipmentID.getValue()));
        }
        //------------------------------------------------------
        // Ask RTD server
        //------------------------------------------------------
        List<String> additParams = new ArrayList<>();
        additParams.add(lotID.getValue());
        if (!ObjectIdentifier.isEmpty(portID)){
            additParams.add(",");
            additParams.add(portID.getValue());
        }
        //TODO:CALL txRTDDispatchListInq
        //txRTDDispatchListInq(BizConstant.SP_RTD_FUNCTION_CODE_SLM,equipmentID,additParams);
        //strRTDDispatchListInqResult
        Results.RTDDispatchListInqResult rtdDispatchListInqResult = new Results.RTDDispatchListInqResult();
        List<Infos.DispatchResult> strDispatchResult = rtdDispatchListInqResult.getStrDispatchResult();
        //------------------------------------------------------
        // Convert RTD result to cassetteIDs
        //------------------------------------------------------
        //TODO:CALL RTD_dispatchDataToSLMInfo_Convert
        //RTD_dispatchDataToSLMInfo_Convert(OBJCOMMON,strDispatchResult);
        //return RTDDispatchDataToSLMInfoConvertoutResult
        Results.RTDDispatchDataToSLMInfoConvertoutResult result = new Results.RTDDispatchDataToSLMInfoConvertoutResult();

        Results.SLMCandidateCassetteForRetrievingInqResult retrievingInqResult = new Results.SLMCandidateCassetteForRetrievingInqResult();
//        retrievingInqResult.setCassetteIDs(result.getCassetteIDs());
        retrievingInqResult.setCassetteIDs(new ArrayList<>());
        return retrievingInqResult;
    }

}