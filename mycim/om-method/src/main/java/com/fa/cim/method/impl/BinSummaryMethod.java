package com.fa.cim.method.impl;

import com.fa.cim.annotaion.OmMethod;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.OmCode;
import com.fa.cim.common.utils.*;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.dto.Infos;
import com.fa.cim.entity.nonruntime.bin.CimBinCountDO;
import com.fa.cim.entity.nonruntime.bin.CimBinSumDO;
import com.fa.cim.jpa.CimJpaRepository;
import com.fa.cim.method.IBinSummaryMethod;
import com.fa.cim.newcore.bo.product.CimLot;
import com.fa.cim.newcore.dto.product.ProductDTO;
import com.fa.cim.newcore.factory.BaseCoreFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/10/15          ********            Nyx                create file
 *
 * @author Nyx
 * @since 2019/10/15 17:35
 * Copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@OmMethod
@Slf4j
public class BinSummaryMethod implements IBinSummaryMethod {

    @Autowired
    private BaseCoreFactory baseCoreFactory;

    @Autowired
    private RetCodeConfig retCodeConfig;

    @Autowired
    private CimJpaRepository cimJpaRepository;

    @Override
    public void binSummaryBinRptCountSetDR(Infos.ObjCommon objCommon, ObjectIdentifier lotID, String testTypeID, String value) {

        CimLot aLot = baseCoreFactory.getBO(CimLot.class, lotID);
        Validations.check(CimObjectUtils.isEmpty(aLot), retCodeConfig.getNotFoundLot());

        List<ProductDTO.WaferInfo> aWaferInfoSeq = aLot.getAllWaferInfo();
        if (CimArrayUtils.isEmpty(aWaferInfoSeq)) return;
        aWaferInfoSeq.stream().map(waferInfo -> {
            CimBinSumDO cimBinSumExam = new CimBinSumDO();
            cimBinSumExam.setWaferID(ObjectIdentifier.fetchValue(waferInfo.getWaferID()));
            cimBinSumExam.setTestTypeID(testTypeID);
            return cimJpaRepository.findOne(Example.of(cimBinSumExam)).orElse(null);
        }).filter(Objects::nonNull).forEach(cimBinSumDO -> {
            cimBinSumDO.setBinReportCount(CimNumberUtils.intValue(value));
            cimJpaRepository.save(cimBinSumDO);
        });
    }

    @Override
    public List<Infos.WaferBinSummary> binSummaryGetByTestTypeDR(Infos.ObjCommon objCommon, ObjectIdentifier lotID, ObjectIdentifier testTypeID) {
        CimLot aLot = baseCoreFactory.getBO(CimLot.class, lotID);
        Validations.check(aLot == null, new OmCode(retCodeConfig.getNotFoundLot(), lotID.getValue()));
        List<ProductDTO.WaferInfo> aWaferInfoSeq = aLot.getAllWaferInfo();
        if (CimArrayUtils.isEmpty(aWaferInfoSeq)) return Collections.emptyList();
        return aWaferInfoSeq.stream().map(waferInfo -> {
            CimBinSumDO cimBinSumExam = new CimBinSumDO();
            cimBinSumExam.setTestTypeID(ObjectIdentifier.fetchValue(testTypeID));
            cimBinSumExam.setWaferID(ObjectIdentifier.fetchValue(waferInfo.getWaferID()));
            return cimJpaRepository.findOne(Example.of(cimBinSumExam)).orElse(null);
        }).filter(Objects::nonNull).map(cimBinSumDO -> {
            Infos.WaferBinSummary waferBinSummary = new Infos.WaferBinSummary();
            waferBinSummary.setWaferId(ObjectIdentifier.buildWithValue(cimBinSumDO.getWaferID()));
            waferBinSummary.setTestTypeId(ObjectIdentifier.buildWithValue(cimBinSumDO.getTestTypeID()));
            waferBinSummary.setLotId(ObjectIdentifier.buildWithValue(cimBinSumDO.getLotID()));
            waferBinSummary.setProcessGroupID(ObjectIdentifier.buildWithValue(cimBinSumDO.getProdGrpID()));
            waferBinSummary.setProductId(ObjectIdentifier.buildWithValue(cimBinSumDO.getProdSpecID()));
            waferBinSummary.setEquipmentId(ObjectIdentifier.buildWithValue(cimBinSumDO.getEquipmentID()));
            waferBinSummary.setUserId(ObjectIdentifier.buildWithValue(cimBinSumDO.getClaimUserID()));
            waferBinSummary.setTestSpecId(ObjectIdentifier.buildWithValue(cimBinSumDO.getTestSpecID()));
            waferBinSummary.setTestProgramId(cimBinSumDO.getTestPgmID());
            waferBinSummary.setBinDefinitionId(ObjectIdentifier.buildWithValue(cimBinSumDO.getBinDefID()));
            waferBinSummary.setGoodUnitCount(cimBinSumDO.getGoodUnitCount());
            waferBinSummary.setRepairUnitCount(cimBinSumDO.getRepairUnitCount());
            waferBinSummary.setFailUnitCount(cimBinSumDO.getFailUnitCount());
            waferBinSummary.setWaferCheckResult(cimBinSumDO.getWaferChkResult() > 0);
            waferBinSummary.setLotActionCode(cimBinSumDO.getLotActionCode());
            waferBinSummary.setLotActionParameter(cimBinSumDO.getLotActionParm());
            waferBinSummary.setWaferActionCode(cimBinSumDO.getWaferActionCode());
            waferBinSummary.setWaferActionParameter(cimBinSumDO.getWaferActionParm());
            waferBinSummary.setTestStartTimeStamp(CimDateUtils.convertToSpecString(cimBinSumDO.getTestStartTime()));
            waferBinSummary.setTestFinishTimeStamp(CimDateUtils.convertToSpecString(cimBinSumDO.getTestCompTime()));
            waferBinSummary.setBinReportCount(cimBinSumDO.getBinReportCount());

            CimBinCountDO cimBinCountExam = new CimBinCountDO();
            cimBinCountExam.setWaferID(cimBinSumDO.getWaferID());
            cimBinCountExam.setTestType(cimBinSumDO.getTestTypeID());
            waferBinSummary.setBinCounts(cimJpaRepository.findAll(Example.of(cimBinCountExam)).stream().map(cimBinCountDO -> {
                Infos.BinCount binCount = new Infos.BinCount();
                binCount.setBinNumber(cimBinCountDO.getBinNumber());
                binCount.setDieCount(String.valueOf(cimBinCountDO.getDieCount()));
                binCount.setBinSpecID(new ObjectIdentifier(cimBinCountDO.getBinSpecID()));
                binCount.setBinSpecCodeDes(cimBinCountDO.getDescription());
                binCount.setBinpassCriteriaCode(cimBinCountDO.getBinPassCode());
                return binCount;
            }).collect(Collectors.toList()));

            return waferBinSummary;
        }).collect(Collectors.toList());
    }
}