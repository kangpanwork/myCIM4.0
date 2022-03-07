package com.fa.cim.service.psm.impl;

import com.fa.cim.annotaion.OmService;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.utils.CimArrayUtils;
import com.fa.cim.common.utils.CimBooleanUtils;
import com.fa.cim.common.utils.CimObjectUtils;
import com.fa.cim.common.utils.Validations;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Results;
import com.fa.cim.method.IExperimentalMethod;
import com.fa.cim.service.psm.IPlannedSplitMergeInqService;
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
public class PlannedSplitMergeInqService implements IPlannedSplitMergeInqService {
    @Autowired
    private RetCodeConfig retCodeConfig;
    @Autowired
    private IExperimentalMethod experimentalMethod;
    @Override
    public Results.PSMLotInfoInqResult sxPSMLotInfoInq(Infos.ObjCommon objCommon, ObjectIdentifier lotFamilyID, ObjectIdentifier splitRouteID, String splitOperationNumber, ObjectIdentifier originalRouteID, String originalOperationNumber) {

        //step0: initialization
        Results.PSMLotInfoInqResult out = new Results.PSMLotInfoInqResult();
        //step1: Add Start
        Validations.check(ObjectIdentifier.isEmpty(lotFamilyID) || ObjectIdentifier.isEmpty(splitRouteID) || CimObjectUtils.isEmpty(splitOperationNumber) || ObjectIdentifier.isEmpty(originalRouteID) || CimObjectUtils.isEmpty(originalOperationNumber),
                retCodeConfig.getInvalidInputParam());

        //step2:Get Experimental Lot Info
        Boolean execCheckFlag = false;
        Boolean detailRequireFlag = true;
        List<Infos.ExperimentalLotInfo> experimentalLotListResult = experimentalMethod.experimentalLotListGetDR(objCommon, lotFamilyID.getValue(), splitRouteID.getValue(), splitOperationNumber, originalRouteID.getValue(), originalOperationNumber, execCheckFlag, detailRequireFlag);
        if (CimArrayUtils.getSize(experimentalLotListResult) > 1) {
            log.error("sxPSMLotInfoInq(): More than two PSM definitions are found. The key items of target PSM are invalid");
            throw new ServiceException(retCodeConfig.getInvalidActionCode());
        } else if (CimArrayUtils.getSize(experimentalLotListResult) < 1) {
            log.error("sxPSMLotInfoInq():No PSM definition is found. The key items of target PSM are invalid");
        }
        Infos.ExperimentalLotInfo experimentalLotInfo = experimentalLotListResult.get(0);

        Boolean workedFlag = false;
        int detailsLen = CimArrayUtils.getSize(experimentalLotInfo.getStrExperimentalLotDetailInfoSeq());
        for (int detailsCnt = 0; detailsCnt < detailsLen; detailsCnt++) {
            if (CimBooleanUtils.isTrue(experimentalLotInfo.getStrExperimentalLotDetailInfoSeq().get(detailsCnt).getExecFlag())) {
                workedFlag = true;
                break;
            }
        }
        //step3: Set output buffer
        out.setEditFlag(!workedFlag);
        out.setStrExperimentalLotInfo(experimentalLotInfo);
        return out;
    }

    @Override
    public List<Infos.ExperimentalLotInfo> sxPSMLotDefinitionListInq(Infos.ObjCommon objCommon, ObjectIdentifier lotFamilyID, Boolean detailRequireFlag) {

        //【step1】:initialization;

        //【step2】:Get Experimental Lot List;
        Boolean execCheckFlag = false;
        return experimentalMethod.experimentalLotListGetDR(objCommon, lotFamilyID.getValue(), "", "",
                "", "", execCheckFlag, detailRequireFlag);
    }
}