package com.fa.cim.newIntegration.LotOperation.scase;

import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.utils.CimArrayUtils;
import com.fa.cim.common.utils.CimObjectUtils;
import com.fa.cim.common.utils.Validations;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Results;
import com.fa.cim.newIntegration.tcase.DynamicBranchTestCase;
import com.fa.cim.newIntegration.tcase.FutureHoldTestCase;
import com.fa.cim.newIntegration.tcase.LotGeneralTestCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/12/20          ********            lightyh                create file
 *
 * @author: light
 * @date: 2019/12/20 10:33
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@Service
public class DynamicBranchCase {

    @Autowired
    private DynamicBranchTestCase dynamicBranchTestCase;

    @Autowired
    private OperationSkipCase operationSkipCase;

    @Autowired
    private BranchCase branchCase;

    @Autowired
    private LotGeneralTestCase lotGeneralTestCase;

    @Autowired
    private FutureHoldTestCase futureHoldTestCase;

    @Autowired
    private RetCodeConfig retCodeConfig;

    public void dynamicBranchNormalCase(){
        //【step1】stb and skip
        List<Infos.LotInfo> lotInfoList = operationSkipCase.stbNLotsAndSkipSpecificStep(1, true, "1000.0200", true);
        Infos.LotInfo lotInfo = lotInfoList.get(0);
        //【step2】branc with the return point is current step
        dynamicBranchTestCase.dynmicBranchReq(lotInfo.getLotOperationInfo().getOperationNumber(), lotInfo.getLotOperationInfo().getRouteID(), lotInfo.getLotBasicInfo().getLotID(),
                lotInfo.getLotOperationInfo().getOperationNumber());
        //【step3】get lot info
        List<Infos.LotInfo> lotInfoList2 = lotGeneralTestCase.getLotInfos(Arrays.asList(lotInfo.getLotBasicInfo().getLotID()));
        //【step4】get lot operation list backward
        Results.LotOperationSelectionInqResult lotOperationSelectionInqResult1 = (Results.LotOperationSelectionInqResult) lotGeneralTestCase.getLotOperationSelectionInq(lotInfoList.get(0).getLotBasicInfo().getLotID(), false, true, false).getBody();
        Assert.isTrue(!CimArrayUtils.isEmpty(lotOperationSelectionInqResult1.getOperationNameAttributesAttributes().getContent()), "test fail");
        List<ObjectIdentifier> routeIDList = lotOperationSelectionInqResult1.getOperationNameAttributesAttributes().getContent().stream().map(Infos.OperationNameAttributes::getRouteID).collect(Collectors.toList());
        Assert.isTrue(!routeIDList.contains(lotInfoList2.get(0).getLotOperationInfo().getRouteID()), "test fail") ;
        //【step5】get lot operation list forward
        Results.LotOperationSelectionInqResult lotOperationSelectionInqResult2 = (Results.LotOperationSelectionInqResult) lotGeneralTestCase.getLotOperationSelectionInq(lotInfoList.get(0).getLotBasicInfo().getLotID(), false, true, true).getBody();
        Assert.isTrue(!CimArrayUtils.isEmpty(lotOperationSelectionInqResult2.getOperationNameAttributesAttributes().getContent()), "test fail");
        List<Infos.OperationNameAttributes> perationNameAttributesList = lotOperationSelectionInqResult2.getOperationNameAttributesAttributes().stream().filter(operationNameAttributes -> CimObjectUtils.equalsWithValue(operationNameAttributes.getRouteID(), lotInfo.getLotOperationInfo().getRouteID())).collect(Collectors.toList());
        Assert.isTrue(perationNameAttributesList.get(0).getOperationNumber().equals(lotInfo.getLotOperationInfo().getOperationNumber()), "test fail");
        //【step6】branch cancel
        branchCase.branchCancelReq(lotInfoList2.get(0));
        //【step7】skip to 2000.0400
        List<Infos.LotInfo> lotInfoList3 = operationSkipCase.skipSpecificStep(Arrays.asList(lotInfo.getLotBasicInfo().getLotID()),"2000.0400",true);
        //【step8】branch to branch route
        branchCase.subRouteBranchReq(lotInfoList3.get(0));
        //【step9】lot info
        List<Infos.LotInfo> lotInfoList4 = lotGeneralTestCase.getLotInfos(Arrays.asList(lotInfoList3.get(0).getLotBasicInfo().getLotID()));
        //【step10】branc with the return point is current step
        dynamicBranchTestCase.dynmicBranchReq(lotInfoList4.get(0).getLotOperationInfo().getOperationNumber(), lotInfoList4.get(0).getLotOperationInfo().getRouteID(), lotInfoList4.get(0).getLotBasicInfo().getLotID(),
                lotInfoList4.get(0).getLotOperationInfo().getOperationNumber());
        //【step11】get lot operation list forward
        Results.LotOperationSelectionInqResult lotOperationSelectionInqResult3 = (Results.LotOperationSelectionInqResult) lotGeneralTestCase.getLotOperationSelectionInq(lotInfoList.get(0).getLotBasicInfo().getLotID(), false, true, true).getBody();
        Assert.isTrue(!CimArrayUtils.isEmpty(lotOperationSelectionInqResult3.getOperationNameAttributesAttributes().getContent()), "test fail");
        List<Infos.OperationNameAttributes> operationNameAttributesList3 = lotOperationSelectionInqResult3.getOperationNameAttributesAttributes().getContent();
        Map<ObjectIdentifier, List<Infos.OperationNameAttributes>> map = operationNameAttributesList3.stream().collect(Collectors.groupingBy(Infos.OperationNameAttributes::getRouteID));
        Assert.isTrue(map.keySet().size() == 3, "test fail");
    }

    public void dynamicBranchFutureHoldInTheProductionRoute(){
        //【step1】stb and skip
        List<Infos.LotInfo> lotInfoList = operationSkipCase.stbNLotsAndSkipSpecificStep(1, true, "3000.0100", true);
        //【step2】register future hold
        futureHoldTestCase.futureHoldRegisterBySpecLot(lotInfoList.get(0).getLotBasicInfo().getLotID(), "3000.0300", lotInfoList.get(0).getLotOperationInfo().getRouteID(), false);
        //【step3】dynamic branch
        dynamicBranchTestCase.dynmicBranchReq(lotInfoList.get(0).getLotOperationInfo().getOperationNumber(), lotInfoList.get(0).getLotOperationInfo().getRouteID(), lotInfoList.get(0).getLotBasicInfo().getLotID(), lotInfoList.get(0).getLotOperationInfo().getOperationNumber());
        //【step4】skip
        try {
            operationSkipCase.skipSpecificStep(Arrays.asList(lotInfoList.get(0).getLotBasicInfo().getLotID()), "4000.0100", true);
            Assert.isTrue(false, "need throw exception");
        } catch (ServiceException e) {
            Assert.isTrue(Validations.isEquals(retCodeConfig.getNotFutureholdInLocate(), e.getCode()), e.getMessage());
        }
        //【step5】skip
        List<Infos.LotInfo> lotInfoList2 = operationSkipCase.skipSpecificStep(Arrays.asList(lotInfoList.get(0).getLotBasicInfo().getLotID()), "3000.0300", true);
        Assert.isTrue(lotInfoList2.get(0).getLotBasicInfo().getLotStatus().equals("ONHOLD"), " the lot status not correct");
    }

    public void dynamicBranchFutureHoldInSubRoute(){
        //【step1】stb and skip
        List<Infos.LotInfo> lotInfoList = operationSkipCase.stbNLotsAndSkipSpecificStep(1, true, "1000.0200", true);
        //【step2】dynamic branch
        dynamicBranchTestCase.dynmicBranchReq(lotInfoList.get(0).getLotOperationInfo().getOperationNumber(), lotInfoList.get(0).getLotOperationInfo().getRouteID(),
                lotInfoList.get(0).getLotBasicInfo().getLotID(), lotInfoList.get(0).getLotOperationInfo().getOperationNumber());
        //【step3】lot info
        List<Infos.LotInfo> lotInfoList2 = lotGeneralTestCase.getLotInfos(Arrays.asList(lotInfoList.get(0).getLotBasicInfo().getLotID()));
        //【step4】get lot operation list forward
        Results.LotOperationSelectionInqResult lotOperationSelectionInqResult = (Results.LotOperationSelectionInqResult) lotGeneralTestCase.getLotOperationSelectionInq(lotInfoList.get(0).getLotBasicInfo().getLotID(), false, true, true).getBody();
        Assert.isTrue(!CimArrayUtils.isEmpty(lotOperationSelectionInqResult.getOperationNameAttributesAttributes().getContent()), "test fail");
        Map<ObjectIdentifier, List<Infos.OperationNameAttributes>> map = lotOperationSelectionInqResult.getOperationNameAttributesAttributes().stream().collect(Collectors.groupingBy(Infos.OperationNameAttributes::getRouteID));
        List<Infos.OperationNameAttributes> operationNameAttributesListSelected = map.get(lotInfoList2.get(0).getLotOperationInfo().getRouteID());
        //【step5】register future hold
        futureHoldTestCase.futureHoldRegisterBySpecLot(lotInfoList2.get(0).getLotBasicInfo().getLotID(),operationNameAttributesListSelected.get(0).getOperationNumber(), lotInfoList2.get(0).getLotOperationInfo().getRouteID(), false);
        //【step6】branch cancel
        try {
            branchCase.branchCancelReq(lotInfoList2.get(0));
            Assert.isTrue(false, "need throw exception");
        } catch (ServiceException e) {
            Assert.isTrue(Validations.isEquals(retCodeConfig.getFutureholdOnBranch(), e.getCode()), e.getMessage());
        }
        try {
            operationSkipCase.skipSpecificStep(Arrays.asList(lotInfoList.get(0).getLotBasicInfo().getLotID()), "2000.0100", true);
            Assert.isTrue(false, "need throw exception");
        } catch (ServiceException e) {
            Assert.isTrue(Validations.isEquals(retCodeConfig.getNotFutureholdInLocate(), e.getCode()), e.getMessage());
        }
    }
}