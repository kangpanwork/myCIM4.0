package com.fa.cim.service.fsm.impl;

import com.fa.cim.annotaion.OmService;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.utils.CimArrayUtils;
import com.fa.cim.common.utils.CimBooleanUtils;
import com.fa.cim.common.utils.CimObjectUtils;
import com.fa.cim.common.utils.Validations;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.dto.Infos;
import com.fa.cim.method.IExperimentalForFutureMethod;
import com.fa.cim.service.fsm.IFutureSplitMergeInqService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/9/8        ********            Nyx                create file
 *
 * @author: Nyx
 * @date: 2020/9/8 16:31
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@OmService
public class FutureSplitMergeInqService implements IFutureSplitMergeInqService {
    @Autowired
    private RetCodeConfig retCodeConfig;
    @Autowired
    private IExperimentalForFutureMethod experimentalForFutureMethod;
    @Override
    public com.fa.cim.fsm.Results.FSMLotInfoInqResult sxFSMLotInfoInq(Infos.ObjCommon objCommon, ObjectIdentifier lotFamilyID, ObjectIdentifier splitRouteID, String splitOperationNumber, ObjectIdentifier originalRouteID, String originalOperationNumber) {

        //step0: initialization
        com.fa.cim.fsm.Results.FSMLotInfoInqResult out = new com.fa.cim.fsm.Results.FSMLotInfoInqResult();
        //step1: Add Start
        Validations.check(ObjectIdentifier.isEmpty(lotFamilyID) || ObjectIdentifier.isEmpty(splitRouteID) || CimObjectUtils.isEmpty(splitOperationNumber) || ObjectIdentifier.isEmpty(originalRouteID) || CimObjectUtils.isEmpty(originalOperationNumber),
                retCodeConfig.getInvalidInputParam());

        //step2:Get Experimental Lot Info
        Boolean execCheckFlag = false;
        Boolean detailRequireFlag = true;
        List<com.fa.cim.fsm.Infos.ExperimentalFutureLotInfo> experimentalFutureLotListResult = experimentalForFutureMethod.experimentalFutureLotListGetDR(objCommon, lotFamilyID.getValue(), splitRouteID.getValue(), splitOperationNumber, originalRouteID.getValue(), originalOperationNumber, execCheckFlag, detailRequireFlag);
        if (CimArrayUtils.getSize(experimentalFutureLotListResult) > 1) {
            log.error("sxPSMLotInfoInq(): More than two PSM definitions are found. The key items of target PSM are invalid");
            throw new ServiceException(retCodeConfig.getInvalidActionCode());
        } else if (CimArrayUtils.getSize(experimentalFutureLotListResult) < 1) {
            log.error("sxPSMLotInfoInq():No PSM definition is found. The key items of target PSM are invalid");
        }
        com.fa.cim.fsm.Infos.ExperimentalFutureLotInfo experimentalLotInfo = experimentalFutureLotListResult.get(0);

        Boolean workedFlag = false;
        int detailsLen = CimArrayUtils.getSize(experimentalLotInfo.getStrExperimentalFutureLotDetailInfoSeq());
        for (int detailsCnt = 0; detailsCnt < detailsLen; detailsCnt++) {
            if (CimBooleanUtils.isTrue(experimentalLotInfo.getStrExperimentalFutureLotDetailInfoSeq().get(detailsCnt).getExecFlag())) {
                workedFlag = true;
                break;
            }
        }
        //step3: Set output buffer
        out.setEditFlag(!workedFlag);
        out.setStrExperimentalFutureLotInfo(experimentalLotInfo);
        return out;
    }

    @Override
    public List<com.fa.cim.fsm.Infos.ExperimentalFutureLotInfo> sxFSMLotDefinitionListInq(Infos.ObjCommon objCommon, ObjectIdentifier lotFamilyID, Boolean detailRequireFlag) {

        //【step1】:initialization;

        //【step2】:Get Experimental Lot List;
        Boolean execCheckFlag = false;
        return experimentalForFutureMethod.experimentalFutureLotListGetDR(objCommon, lotFamilyID.getValue(), "", "",
                "", "", execCheckFlag, detailRequireFlag);
    }
}