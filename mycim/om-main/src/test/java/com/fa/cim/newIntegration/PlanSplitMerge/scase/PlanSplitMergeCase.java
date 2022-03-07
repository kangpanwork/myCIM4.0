package com.fa.cim.newIntegration.PlanSplitMerge.scase;

import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.utils.CimArrayUtils;
import com.fa.cim.common.utils.CimObjectUtils;
import com.fa.cim.common.utils.CimStringUtils;
import com.fa.cim.common.utils.Validations;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;
import com.fa.cim.newIntegration.LotOperation.scase.BranchCase;
import com.fa.cim.newIntegration.LotOperation.scase.LotHoldCase;
import com.fa.cim.newIntegration.LotOperation.scase.ScrapCase;
import com.fa.cim.newIntegration.common.TestUtils;
import com.fa.cim.newIntegration.dto.TestInfos;
import com.fa.cim.newIntegration.equipment.scase.GatePassCase;
import com.fa.cim.newIntegration.sps.scase.LotScheduleCase;
import com.fa.cim.newIntegration.stb.scase.STBCase;
import com.fa.cim.newIntegration.tcase.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * description: PSM test case
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/12/4                              Neyo                create file
 *
 * @author: Neyo
 * @date: 2019/12/4 14:04
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@Service
public class PlanSplitMergeCase {

    @Autowired
    private RetCodeConfig retCodeConfig;

    @Autowired
    private STBCase stbCase;

    @Autowired
    private LotGeneralTestCase lotGeneralTestCase;

    @Autowired
    private PlanSplitMergeTestCase planSplitMergeTestCase;

    @Autowired
    private ElectronicInformationTestCase electronicInformationTestCase;

    @Autowired
    private OperationSkipTestCase operationSkipTestCase;

    @Autowired
    private LotMergeTestCase lotMergeTestCase;

    @Autowired
    private LotHoldTestCase lotHoldTestCase;

    @Autowired
    private TestUtils testUtils;

    @Autowired
    private BranchCase branchCase;

    @Autowired
    private ScrapCase scrapCase;

    @Autowired
    private FutureHoldTestCase futureHoldTestCase;

    @Autowired
    private EquipmentTestCase equipmentTestCase;

    @Autowired
    private GatePassCase gatePassCase;

    @Autowired
    private LotHoldCase lotHoldCase;

    @Autowired
    private CommonTestCase commonTestCase;

    @Autowired
    private LotScheduleCase lotScheduleCase;

    private static String PSM_TEST_EQUIPMENT_ONE = "1SRT03";

    private static String PSM_TEST_EQUIPMENT_TWO = "1PWP01";

    private static String LOAD_PORT_ONE = "P1";

    private static String LOAD_PORT_TWO = "P2";

    public void PlannedSplit_With_DynamicRouteID_And_NonAction_OrAction() {

        //【step1】STB a product lot 25 wafer
        String bankID = "BNK-0S";
        String sourceProductID = "RAW-2000.01";
        String productID = "PRODUCT0.01";
        TestInfos.StbInfo stbInfo = new TestInfos.StbInfo(bankID, sourceProductID, productID);
        ObjectIdentifier lotID = stbCase.STB_Normal(stbInfo);

        //【step2】chose param to set up psm
        String splitOperationNum = "2000.0100";
        String returnOperationNum = "2000.0200";
        String mergeOperationNum = "2000.0200";
        Boolean dynamicFlag = true;

        //【step3】get wafer list
        List<ObjectIdentifier> lotIDs = new ArrayList<>();
        lotIDs.add(lotID);
        Results.LotInfoInqResult lotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();
        ObjectIdentifier familyLotID = lotInfoCase.getLotInfoList().get(0).getLotBasicInfo().getFamilyLotID();
        List<Infos.WaferListInLotFamilyInfo> waferListInLotFamilyInfos = (List<Infos.WaferListInLotFamilyInfo>) lotGeneralTestCase.waferListInLotFamilyInfoInqCase(familyLotID).getBody();
        List<ObjectIdentifier> waferList = new ArrayList<>();
        waferList.add(waferListInLotFamilyInfos.get(0).getWaferID());
        waferList.add(waferListInLotFamilyInfos.get(1).getWaferID());

        //【step4】psm relation
        this.planedSplitWithSplitPointAndReturnPointAndMergePointAndExecSkipAndMerge(null, lotID,splitOperationNum,returnOperationNum,mergeOperationNum,dynamicFlag,waferList);

        //【step5】skip back to split point
        lotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();
        //【step1-1】 skip to the split point
        Infos.LotInfo lotInfo = lotInfoCase.getLotInfoList().get(0);
        Results.LotOperationSelectionInqResult lotOperationSelectionInqResult1 = (Results.LotOperationSelectionInqResult) lotGeneralTestCase.getLotOperationSelectionInq(lotID, false, true, false).getBody();
        List<Infos.OperationNameAttributes> operationNameAttributesList1 = lotOperationSelectionInqResult1.getOperationNameAttributesAttributes().getContent();

        Infos.OperationNameAttributes operationNameAttributes1 = lotGeneralTestCase.getOperationAttributesByOperationNumber(operationNameAttributesList1,splitOperationNum);

        Params.SkipReqParams skipReqParams = new Params.SkipReqParams();
        skipReqParams.setCurrentOperationNumber(lotInfo.getLotOperationInfo().getOperationNumber());
        skipReqParams.setCurrentRouteID(lotInfo.getLotOperationInfo().getRouteID());
        skipReqParams.setLocateDirection(false);
        skipReqParams.setLotID(lotID);
        skipReqParams.setOperationID(operationNameAttributes1.getOperationID());
        skipReqParams.setOperationNumber(operationNameAttributes1.getOperationNumber());
        skipReqParams.setProcessRef(operationNameAttributes1.getProcessRef());
        skipReqParams.setRouteID(operationNameAttributes1.getRouteID());
        skipReqParams.setSeqno(-1);
        skipReqParams.setSequenceNumber(0);
        operationSkipTestCase.operationSkip(skipReqParams);

        lotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();
        List<ObjectIdentifier> lotIDList = lotInfoCase.getLotListInCassetteInfo().getLotIDList();
        Validations.assertCheck(CimArrayUtils.getSize(lotIDList) == 1,"the parent lot can't split again");
    }

    public void PlannedSplit_With_SubRouteID_And_NonAction_OrAction() {
        //【step1】STB a product lot 25 wafer
        String bankID = "BNK-0S";
        String sourceProductID = "RAW-2000.01";
        String productID = "PRODUCT0.01";
        TestInfos.StbInfo stbInfo = new TestInfos.StbInfo(bankID, sourceProductID, productID);
        ObjectIdentifier lotID = stbCase.STB_Normal(stbInfo);

        //【step2】chose param to set up psm
        String splitOperationNum = "2000.0400";
        String returnOperationNum = "2000.0200";
        String mergeOperationNum = "2000.0500";
        Boolean dynamicFlag = false;

        //【step3】get wafer list
        List<ObjectIdentifier> lotIDs = new ArrayList<>();
        lotIDs.add(lotID);
        Results.LotInfoInqResult lotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();
        ObjectIdentifier familyLotID = lotInfoCase.getLotInfoList().get(0).getLotBasicInfo().getFamilyLotID();
        List<Infos.WaferListInLotFamilyInfo> waferListInLotFamilyInfos = (List<Infos.WaferListInLotFamilyInfo>) lotGeneralTestCase.waferListInLotFamilyInfoInqCase(familyLotID).getBody();
        List<ObjectIdentifier> waferList = new ArrayList<>();
        waferList.add(waferListInLotFamilyInfos.get(0).getWaferID());
        waferList.add(waferListInLotFamilyInfos.get(1).getWaferID());


        //【step4】psm relation
        this.planedSplitWithSplitPointAndReturnPointAndMergePointAndExecSkipAndMerge(null, lotID, splitOperationNum,returnOperationNum,mergeOperationNum, dynamicFlag,waferList);
    }

    public void PlannedSplit_With_Branch_Cancel() {
        //【step1】STB a product lot 25 wafer
        String bankID = "BNK-0S";
        String sourceProductID = "RAW-2000.01";
        String productID = "PRODUCT0.01";
        String skipOperationNumber = "2000.0400";
        TestInfos.StbInfo stbInfo = new TestInfos.StbInfo(bankID, sourceProductID, productID);
        ObjectIdentifier lotID = testUtils.stbAndSkip(stbInfo,skipOperationNumber);

        //【step2】branch at skipOperationNumber
        List<ObjectIdentifier> lotIDs = new ArrayList<>();
        lotIDs.add(lotID);
        Results.LotInfoInqResult firstLotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();
        ObjectIdentifier backRouteID = firstLotInfoCase.getLotInfoList().get(0).getLotOperationInfo().getRouteID();
        branchCase.subRouteBranchReq(firstLotInfoCase.getLotInfoList().get(0));

        //【step3】chose param to set up psm
        String splitOperationNum = "2000.0400";
        String returnOperationNum = "2000.0200";
        String mergeOperationNum = "2000.0500";
        Boolean dynamicFlag = false;

        //【step4】get wafer list
        ObjectIdentifier familyLotID = firstLotInfoCase.getLotInfoList().get(0).getLotBasicInfo().getFamilyLotID();
        List<Infos.WaferListInLotFamilyInfo> waferListInLotFamilyInfos = (List<Infos.WaferListInLotFamilyInfo>) lotGeneralTestCase.waferListInLotFamilyInfoInqCase(familyLotID).getBody();
        List<ObjectIdentifier> waferList = new ArrayList<>();
        waferList.add(waferListInLotFamilyInfos.get(0).getWaferID());
        waferList.add(waferListInLotFamilyInfos.get(1).getWaferID());

        //【step5】psm relation
        this.planedSplitWithSplitPointAndReturnPointAndMergePoint(backRouteID,lotID,splitOperationNum,returnOperationNum,mergeOperationNum,dynamicFlag,waferList);

        //【step6】step lot info branch cancel
        Results.LotInfoInqResult secondLotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();
        branchCase.branchCancelReq(secondLotInfoCase.getLotInfoList().get(0));
    }

    public void PlannedSplit_With_Scrapped_Wafers_Reserved_For_ChildLot() {
        //【step1】STB a product lot 25 wafer
        String bankID = "BNK-0S";
        String sourceProductID = "RAW-2000.01";
        String productID = "PRODUCT0.01";
        TestInfos.StbInfo stbInfo = new TestInfos.StbInfo(bankID, sourceProductID, productID);
        ObjectIdentifier lotID = stbCase.STB_Normal(stbInfo);

        //【step2】scrap a wafer
        ObjectIdentifier reasonRouteID = new ObjectIdentifier("LAYER0MA.01");
        ObjectIdentifier reasonCodeID = new ObjectIdentifier("MISC");
        String reasonOperationNumber = "1000.0100";
        int scrapWaferCount = 1;
        TestInfos.ScrapInfo scrapInfo = new TestInfos.ScrapInfo(lotID, reasonRouteID, reasonCodeID, reasonOperationNumber, scrapWaferCount);
        scrapCase.scrap(scrapInfo);

        //【step3】get scrap waferList
        List<ObjectIdentifier> lotIDs = new ArrayList<>();
        List<ObjectIdentifier> scrapWaferList = new ArrayList<>();
        lotIDs.add(lotID);
        Results.LotInfoInqResult lotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();

        Params.WaferScrappedHistoryInqParams params = new Params.WaferScrappedHistoryInqParams();
        params.setUser(testUtils.getUser());
        params.setLotID(lotID);
        params.setCassetteID(lotInfoCase.getLotListInCassetteInfo().getCassetteID());
        List<Infos.ScrapHistories> scrapList = (List<Infos.ScrapHistories>) scrapCase.waferScrappedHistoryInqCase(params).getBody();
        if (CimArrayUtils.getSize(scrapList) > 0){
            scrapList.forEach(x -> scrapWaferList.add(x.getWaferID()));
        }

        //【step4】psm relation choice 3 wafer and have 1 crap
        String splitOperationNum = "2000.0400";
        String returnOperationNum = "2000.0200";
        String mergeOperationNum = "2000.0500";
        Boolean dynamicFlag = false;
        ObjectIdentifier familyLotID = lotInfoCase.getLotInfoList().get(0).getLotBasicInfo().getFamilyLotID();
        List<Infos.WaferListInLotFamilyInfo> waferListInLotFamilyInfos = (List<Infos.WaferListInLotFamilyInfo>) lotGeneralTestCase.waferListInLotFamilyInfoInqCase(familyLotID).getBody();
        List<ObjectIdentifier> choiceWaferList = new ArrayList<>();
        choiceWaferList.add(waferListInLotFamilyInfos.get(1).getWaferID());
        choiceWaferList.add(waferListInLotFamilyInfos.get(2).getWaferID());
        choiceWaferList.add(scrapWaferList.get(0));
        ObjectIdentifier childLotID = this.planedSplitWithSplitPointAndReturnPointAndMergePointAndExitSkipSplitPoint(null,lotID,splitOperationNum,returnOperationNum,mergeOperationNum,dynamicFlag,choiceWaferList);

        //【step5】 check child lot have only 2 wafers
        lotIDs = new ArrayList<>();
        lotIDs.add(childLotID);
        Results.LotInfoInqResult childLotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();
        int childWaferSize = CimArrayUtils.getSize(childLotInfoCase.getLotInfoList().get(0).getLotWaferAttributesList());
        Validations.assertCheck(childWaferSize == 2,"child lot have must done not have scrap wafer");

    }

    public void PlannedSplit_With_Scrapped_Wafers_Reserved_For_ParentLot() {
        //【step1】STB a product lot 25 wafer
        String bankID = "BNK-0S";
        String sourceProductID = "RAW-2000.01";
        String productID = "PRODUCT0.01";
        TestInfos.StbInfo stbInfo = new TestInfos.StbInfo(bankID, sourceProductID, productID);
        ObjectIdentifier lotID = stbCase.STB_Normal(stbInfo);

        //【step2】scrap a wafer
        ObjectIdentifier reasonRouteID = new ObjectIdentifier("LAYER0MA.01");
        ObjectIdentifier reasonCodeID = new ObjectIdentifier("MISC");
        String reasonOperationNumber = "1000.0100";
        int scrapWaferCount = 1;
        TestInfos.ScrapInfo scrapInfo = new TestInfos.ScrapInfo(lotID, reasonRouteID, reasonCodeID, reasonOperationNumber, scrapWaferCount);
        scrapCase.scrap(scrapInfo);

        //【step3】get scrap waferList
        List<ObjectIdentifier> lotIDs = new ArrayList<>();
        List<ObjectIdentifier> scrapWaferList = new ArrayList<>();
        lotIDs.add(lotID);
        Results.LotInfoInqResult lotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();

        Params.WaferScrappedHistoryInqParams params = new Params.WaferScrappedHistoryInqParams();
        params.setUser(testUtils.getUser());
        params.setLotID(lotID);
        params.setCassetteID(lotInfoCase.getLotListInCassetteInfo().getCassetteID());
        List<Infos.ScrapHistories> scrapList = (List<Infos.ScrapHistories>) scrapCase.waferScrappedHistoryInqCase(params).getBody();
        if (CimArrayUtils.getSize(scrapList) > 0){
            scrapList.forEach(x -> scrapWaferList.add(x.getWaferID()));
        }

        //【step4】psm relation choice 3 wafer and have 1 crap
        String splitOperationNum = "2000.0400";
        String returnOperationNum = "2000.0200";
        String mergeOperationNum = "2000.0500";
        Boolean dynamicFlag = false;
        ObjectIdentifier familyLotID = lotInfoCase.getLotInfoList().get(0).getLotBasicInfo().getFamilyLotID();
        List<Infos.WaferListInLotFamilyInfo> waferListInLotFamilyInfos = (List<Infos.WaferListInLotFamilyInfo>) lotGeneralTestCase.waferListInLotFamilyInfoInqCase(familyLotID).getBody();
        List<ObjectIdentifier> choiceWaferList = new ArrayList<>();
        choiceWaferList.add(waferListInLotFamilyInfos.get(1).getWaferID());
        choiceWaferList.add(waferListInLotFamilyInfos.get(2).getWaferID());
        ObjectIdentifier childLotID = this.planedSplitWithSplitPointAndReturnPointAndMergePointAndExitSkipSplitPoint(null,lotID,splitOperationNum,returnOperationNum,mergeOperationNum,dynamicFlag,choiceWaferList);

        //【step5】 psm must don not split
        lotIDs = new ArrayList<>();
        lotIDs.add(childLotID);
        Results.LotInfoInqResult childLotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();
        int childWaferSize = CimArrayUtils.getSize(childLotInfoCase.getLotInfoList().get(0).getLotWaferAttributesList());
        Validations.assertCheck(childWaferSize == 2,"child lot have must done not have scrap wafer");
    }

    public void PlannedSplit_With_Scrapped_Wafers_Reserved_Except_ScrappedWafer() {
        //【step1】STB a product lot 25 wafer
        String bankID = "BNK-0S";
        String sourceProductID = "RAW-2000.01";
        String productID = "PRODUCT0.01";
        TestInfos.StbInfo stbInfo = new TestInfos.StbInfo(bankID, sourceProductID, productID);
        ObjectIdentifier lotID = stbCase.STB_Normal(stbInfo);

        //【step2】scrap a wafer
        ObjectIdentifier reasonRouteID = new ObjectIdentifier("LAYER0MA.01");
        ObjectIdentifier reasonCodeID = new ObjectIdentifier("MISC");
        String reasonOperationNumber = "1000.0100";
        int scrapWaferCount = 1;
        TestInfos.ScrapInfo scrapInfo = new TestInfos.ScrapInfo(lotID, reasonRouteID, reasonCodeID, reasonOperationNumber, scrapWaferCount);
        scrapCase.scrap(scrapInfo);

        //【step3】get scrap waferList
        List<ObjectIdentifier> lotIDs = new ArrayList<>();
        List<ObjectIdentifier> scrapWaferList = new ArrayList<>();
        lotIDs.add(lotID);
        Results.LotInfoInqResult lotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();

        Params.WaferScrappedHistoryInqParams params = new Params.WaferScrappedHistoryInqParams();
        params.setUser(testUtils.getUser());
        params.setLotID(lotID);
        params.setCassetteID(lotInfoCase.getLotListInCassetteInfo().getCassetteID());
        List<Infos.ScrapHistories> scrapList = (List<Infos.ScrapHistories>) scrapCase.waferScrappedHistoryInqCase(params).getBody();
        if (CimArrayUtils.getSize(scrapList) > 0){
            scrapList.forEach(x -> scrapWaferList.add(x.getWaferID()));
        }

        //【step4】psm relation choice 3 wafer and have 1 crap
        String splitOperationNum = "2000.0400";
        String returnOperationNum = "2000.0200";
        String mergeOperationNum = "2000.0500";
        Boolean dynamicFlag = false;
        ObjectIdentifier familyLotID = lotInfoCase.getLotInfoList().get(0).getLotBasicInfo().getFamilyLotID();
        List<Infos.WaferListInLotFamilyInfo> waferListInLotFamilyInfos = (List<Infos.WaferListInLotFamilyInfo>) lotGeneralTestCase.waferListInLotFamilyInfoInqCase(familyLotID).getBody();
        List<ObjectIdentifier> choiceWaferList = new ArrayList<>();
        waferListInLotFamilyInfos.forEach(x -> {
            if (!CimObjectUtils.equalsWithValue(x.getWaferID(),scrapWaferList.get(0))){
                choiceWaferList.add(x.getWaferID());
            }
        });
        ObjectIdentifier childLotID = this.planedSplitWithSplitPointAndReturnPointAndMergePointAndExitSkipSplitPoint(null,lotID,splitOperationNum,returnOperationNum,mergeOperationNum,dynamicFlag,choiceWaferList);

        //【step5】 psm must don not split and parent lot branch to subRoute
        Validations.assertCheck(CimObjectUtils.isEmpty(childLotID),"parent lot can not split");
        lotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();
        String branchRouteID = lotInfoCase.getLotInfoList().get(0).getLotOperationInfo().getRouteID().getValue();
        String operationNumber = lotInfoCase.getLotInfoList().get(0).getLotOperationInfo().getOperationNumber();
        Validations.assertCheck("DEV_BRCH.01".equals(branchRouteID) && "1000.0100".equals(operationNumber),"parent lot must change to branch route");

    }

    public void PlannedSplit_With_Scrapped_Wafers_Reserved_Only_ScrappedWafer() {
        //【step1】STB a product lot 25 wafer
        String bankID = "BNK-0S";
        String sourceProductID = "RAW-2000.01";
        String productID = "PRODUCT0.01";
        TestInfos.StbInfo stbInfo = new TestInfos.StbInfo(bankID, sourceProductID, productID);
        ObjectIdentifier lotID = stbCase.STB_Normal(stbInfo);

        //【step2】scrap a wafer
        ObjectIdentifier reasonRouteID = new ObjectIdentifier("LAYER0MA.01");
        ObjectIdentifier reasonCodeID = new ObjectIdentifier("MISC");
        String reasonOperationNumber = "1000.0100";
        int scrapWaferCount = 1;
        TestInfos.ScrapInfo scrapInfo = new TestInfos.ScrapInfo(lotID, reasonRouteID, reasonCodeID, reasonOperationNumber, scrapWaferCount);
        scrapCase.scrap(scrapInfo);

        //【step3】get scrap waferList
        List<ObjectIdentifier> lotIDs = new ArrayList<>();
        List<ObjectIdentifier> scrapWaferList = new ArrayList<>();
        lotIDs.add(lotID);
        Results.LotInfoInqResult lotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();

        Params.WaferScrappedHistoryInqParams params = new Params.WaferScrappedHistoryInqParams();
        params.setUser(testUtils.getUser());
        params.setLotID(lotID);
        params.setCassetteID(lotInfoCase.getLotListInCassetteInfo().getCassetteID());
        List<Infos.ScrapHistories> scrapList = (List<Infos.ScrapHistories>) scrapCase.waferScrappedHistoryInqCase(params).getBody();
        if (CimArrayUtils.getSize(scrapList) > 0){
            scrapList.forEach(x -> scrapWaferList.add(x.getWaferID()));
        }

        //【step4】psm relation choice 3 wafer and have 1 crap
        String splitOperationNum = "2000.0400";
        String returnOperationNum = "2000.0200";
        String mergeOperationNum = "2000.0500";
        Boolean dynamicFlag = false;
        ObjectIdentifier familyLotID = lotInfoCase.getLotInfoList().get(0).getLotBasicInfo().getFamilyLotID();
        List<Infos.WaferListInLotFamilyInfo> waferListInLotFamilyInfos = (List<Infos.WaferListInLotFamilyInfo>) lotGeneralTestCase.waferListInLotFamilyInfoInqCase(familyLotID).getBody();
        List<ObjectIdentifier> choiceWaferList = new ArrayList<>();
        choiceWaferList.add(scrapWaferList.get(0));
        ObjectIdentifier childLotID = this.planedSplitWithSplitPointAndReturnPointAndMergePointAndExitSkipSplitPoint(null,lotID,splitOperationNum,returnOperationNum,mergeOperationNum,dynamicFlag,choiceWaferList);

        //【step5】 psm must don not split and parent lot branch to subRoute
        Validations.assertCheck(CimObjectUtils.isEmpty(childLotID),"parent lot can not split");
    }

    public void Register_PSM_With_FutureHold_Between_SplitPoint_And_MergePoint() {
        //【step1】STB a product lot 25 wafer
        String bankID = "BNK-0S";
        String sourceProductID = "RAW-2000.01";
        String productID = "PRODUCT0.01";
        TestInfos.StbInfo stbInfo = new TestInfos.StbInfo(bankID, sourceProductID, productID);
        ObjectIdentifier lotID = stbCase.STB_Normal(stbInfo);

        //【step2】register a future hold at 2000.0400
        List<ObjectIdentifier> lotIDs = new ArrayList<>();
        lotIDs.add(lotID);
        Results.LotInfoInqResult lotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();
        String futureHoldOpeNo = "2000.0400";
        Results.LotOperationSelectionInqResult lotOperationSelectionInqResult = (Results.LotOperationSelectionInqResult) lotGeneralTestCase.getLotOperationSelectionInq(lotID, true, true, true).getBody();

        Infos.OperationNameAttributes operationNameAttributes = lotGeneralTestCase.getOperationAttributesByOperationNumber(lotOperationSelectionInqResult.getOperationNameAttributesAttributes().getContent(),futureHoldOpeNo);
        String operationNumber = operationNameAttributes.getOperationNumber();
        ObjectIdentifier routeID = operationNameAttributes.getRouteID();

        futureHoldTestCase.futureHoldRegisterBySpecLot(lotID, operationNumber, routeID, false,false);

        //【step3】set up psm at split point 2000.0350,merge point 2000.0500,return point 2000.0500
        String splitOperationNum = "2000.0350";
        String returnOperationNum = "2000.0500";
        String mergeOperationNum = "2000.0500";
        Boolean dynamicFlag = true;
        ObjectIdentifier familyLotID = lotInfoCase.getLotInfoList().get(0).getLotBasicInfo().getFamilyLotID();
        List<Infos.WaferListInLotFamilyInfo> waferListInLotFamilyInfos = (List<Infos.WaferListInLotFamilyInfo>) lotGeneralTestCase.waferListInLotFamilyInfoInqCase(familyLotID).getBody();
        List<ObjectIdentifier> choiceWaferList = new ArrayList<>();
        choiceWaferList.add(waferListInLotFamilyInfos.get(0).getWaferID());

        //【step2】step the Engineering PSM Setup
        //【step2-1】psm list information
        Boolean detailRequireFlag = true;
        List<Infos.ExperimentalLotInfo> experimentalLotInfoList = (List<Infos.ExperimentalLotInfo>) planSplitMergeTestCase.experimentalLotList(familyLotID, detailRequireFlag).getBody();
        Validations.assertCheck(CimArrayUtils.getSize(experimentalLotInfoList) == 0,"psm list must be null in fist");
        //【step2-2】get route operation nest list
        Infos.RouteInfo routeInfo = (Infos.RouteInfo) electronicInformationTestCase.processFlowOpeListWithNestInqCase(routeID).getBody();

        //【step3】get subRouteID when in branch
        ObjectIdentifier subRouteID = planSplitMergeTestCase.getSubRouteID(routeInfo,splitOperationNum,routeID);
        TestInfos.PsmUpdateInfo psmUpdateInfo = new TestInfos.PsmUpdateInfo();
        psmUpdateInfo.setActionEMail(true);
        psmUpdateInfo.setActionHold(true);
        psmUpdateInfo.setDynamicFlag(dynamicFlag);
        psmUpdateInfo.setSplitOperationNumber(splitOperationNum);
        psmUpdateInfo.setSplitRouteID(routeID);
        psmUpdateInfo.setOriginalRouteID(routeID);
        psmUpdateInfo.setOriginalOperationNumber(splitOperationNum);
        psmUpdateInfo.setWaferList(choiceWaferList);
        psmUpdateInfo.setLotFamilyID(familyLotID);
        psmUpdateInfo.setReturnOperationNumber(returnOperationNum);
        psmUpdateInfo.setMergeOperationNumber(mergeOperationNum);
        psmUpdateInfo.setSubRouteID(subRouteID);

        //【step3-2】add psm
        try {
            planSplitMergeTestCase.psmLotInfoSetReqCase(psmUpdateInfo);
        } catch (ServiceException e) {
            if (!Validations.isEquals(retCodeConfig.getNotFutureholdInSplit(),e.getCode())){
                throw e;
            }
        }
    }

    public void PlannedSplit_With_FutureHold_At_SplitPoint_Pos() {
        //【step1】STB a product lot 25 wafer
        String bankID = "BNK-0S";
        String sourceProductID = "RAW-2000.01";
        String productID = "PRODUCT0.01";
        TestInfos.StbInfo stbInfo = new TestInfos.StbInfo(bankID, sourceProductID, productID);
        ObjectIdentifier lotID = stbCase.STB_Normal(stbInfo);

        //【step2】register a future hold at 2000.0400
        List<ObjectIdentifier> lotIDs = new ArrayList<>();
        lotIDs.add(lotID);
        Results.LotInfoInqResult lotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();
        String futureHoldOpeNo = "2000.0400";
        Results.LotOperationSelectionInqResult lotOperationSelectionInqResult = (Results.LotOperationSelectionInqResult) lotGeneralTestCase.getLotOperationSelectionInq(lotID, true, true, true).getBody();

        Infos.OperationNameAttributes operationNameAttributes = lotGeneralTestCase.getOperationAttributesByOperationNumber(lotOperationSelectionInqResult.getOperationNameAttributesAttributes().getContent(),futureHoldOpeNo);
        String operationNumber = operationNameAttributes.getOperationNumber();
        ObjectIdentifier routeID = operationNameAttributes.getRouteID();

        futureHoldTestCase.futureHoldRegisterBySpecLot(lotID, operationNumber, routeID, true,true);

        //【step3】set up psm at split point 2000.0400,merge point 2000.0500,return point 2000.0500
        String splitOperationNum = "2000.0400";
        String returnOperationNum = "2000.0500";
        String mergeOperationNum = "2000.0500";
        Boolean dynamicFlag = true;
        ObjectIdentifier familyLotID = lotInfoCase.getLotInfoList().get(0).getLotBasicInfo().getFamilyLotID();
        List<Infos.WaferListInLotFamilyInfo> waferListInLotFamilyInfos = (List<Infos.WaferListInLotFamilyInfo>) lotGeneralTestCase.waferListInLotFamilyInfoInqCase(familyLotID).getBody();
        List<ObjectIdentifier> choiceWaferList = new ArrayList<>();
        choiceWaferList.add(waferListInLotFamilyInfos.get(0).getWaferID());

        //【step2】step the Engineering PSM Setup
        //【step2-1】psm list information
        Boolean detailRequireFlag = true;
        List<Infos.ExperimentalLotInfo> experimentalLotInfoList = (List<Infos.ExperimentalLotInfo>) planSplitMergeTestCase.experimentalLotList(familyLotID, detailRequireFlag).getBody();
        Validations.assertCheck(CimArrayUtils.getSize(experimentalLotInfoList) == 0,"psm list must be null in fist");
        //【step2-2】get route operation nest list
        Infos.RouteInfo routeInfo = (Infos.RouteInfo) electronicInformationTestCase.processFlowOpeListWithNestInqCase(routeID).getBody();

        //【step3】get subRouteID when in branch
        ObjectIdentifier subRouteID = planSplitMergeTestCase.getSubRouteID(routeInfo,splitOperationNum,routeID);
        TestInfos.PsmUpdateInfo psmUpdateInfo = new TestInfos.PsmUpdateInfo();
        psmUpdateInfo.setActionEMail(true);
        psmUpdateInfo.setActionHold(true);
        psmUpdateInfo.setDynamicFlag(dynamicFlag);
        psmUpdateInfo.setSplitOperationNumber(splitOperationNum);
        psmUpdateInfo.setSplitRouteID(routeID);
        psmUpdateInfo.setOriginalRouteID(routeID);
        psmUpdateInfo.setOriginalOperationNumber(splitOperationNum);
        psmUpdateInfo.setWaferList(choiceWaferList);
        psmUpdateInfo.setLotFamilyID(familyLotID);
        psmUpdateInfo.setReturnOperationNumber(returnOperationNum);
        psmUpdateInfo.setMergeOperationNumber(mergeOperationNum);
        psmUpdateInfo.setSubRouteID(subRouteID);

        //【step3-2】add psm
        try {
            planSplitMergeTestCase.psmLotInfoSetReqCase(psmUpdateInfo);
        } catch (ServiceException e) {
            if (!Validations.isEquals(retCodeConfig.getNotFutureholdInSplit(),e.getCode())){
                throw e;
            }
        }
    }

    public void PlannedSplit_With_FutureHold_At_SplitPoint_Pre() {
        //【step1】STB a product lot 25 wafer
        String bankID = "BNK-0S";
        String sourceProductID = "RAW-2000.01";
        String productID = "PRODUCT0.01";
        TestInfos.StbInfo stbInfo = new TestInfos.StbInfo(bankID, sourceProductID, productID);
        ObjectIdentifier lotID = stbCase.STB_Normal(stbInfo);

        //【step2】register a future hold at 2000.0400
        List<ObjectIdentifier> lotIDs = new ArrayList<>();
        lotIDs.add(lotID);
        Results.LotInfoInqResult lotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();
        String futureHoldOpeNo = "2000.0400";
        Results.LotOperationSelectionInqResult lotOperationSelectionInqResult = (Results.LotOperationSelectionInqResult) lotGeneralTestCase.getLotOperationSelectionInq(lotID, true, true, true).getBody();

        Infos.OperationNameAttributes operationNameAttributes = lotGeneralTestCase.getOperationAttributesByOperationNumber(lotOperationSelectionInqResult.getOperationNameAttributesAttributes().getContent(),futureHoldOpeNo);
        String operationNumber = operationNameAttributes.getOperationNumber();
        ObjectIdentifier routeID = operationNameAttributes.getRouteID();

        futureHoldTestCase.futureHoldRegisterBySpecLot(lotID, operationNumber, routeID, false,false);

        //【step2】chose param to set up psm
        String splitOperationNum = "2000.0400";
        String returnOperationNum = "2000.0200";
        String mergeOperationNum = "2000.0500";
        Boolean dynamicFlag = false;

        //【step3】get wafer list
        lotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();
        ObjectIdentifier familyLotID = lotInfoCase.getLotInfoList().get(0).getLotBasicInfo().getFamilyLotID();
        List<Infos.WaferListInLotFamilyInfo> waferListInLotFamilyInfos = (List<Infos.WaferListInLotFamilyInfo>) lotGeneralTestCase.waferListInLotFamilyInfoInqCase(familyLotID).getBody();
        List<ObjectIdentifier> waferList = new ArrayList<>();
        waferList.add(waferListInLotFamilyInfos.get(0).getWaferID());
        waferList.add(waferListInLotFamilyInfos.get(1).getWaferID());


        //【step4】psm relation
        ObjectIdentifier childLotID = this.planedSplitWithSplitPointAndReturnPointAndMergePointAndExitSkipSplitPoint(null, lotID, splitOperationNum, returnOperationNum, mergeOperationNum, dynamicFlag, waferList);

        //【step5】check childLot and parentLot holdState
        Validations.assertCheck(CimObjectUtils.isEmpty(childLotID),"first time the parent lot can not split because lot is onHold(FutureHold)");
        Results.LotInfoInqResult LotInfoCase1 = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();
        String lotStatusAction = LotInfoCase1.getLotInfoList().get(0).getLotBasicInfo().getLotStatus();
        Validations.assertCheck(CimStringUtils.equals(lotStatusAction, BizConstant.SP_DURABLE_HOLDSTATE_ONHOLD),"the parent lot must be onHold");

        //【step6】hold release the parent lot
        lotHoldTestCase.lotHoldReleaseReqCase(lotID);

        //【step7】check the parent lot split to a child lot
        Results.LotInfoInqResult lastLotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();
        Validations.assertCheck(CimArrayUtils.getSize(lastLotInfoCase.getLotListInCassetteInfo().getLotIDList()) == 2,"parent lot split must have a child lot");
    }

    public void PlannedSplit_With_Operation_LocateTo_NextStep_Then_BackwardTo_Current_SplitStep() {

        //【step1】STB a product lot 25 wafer
        String bankID = "BNK-0S";
        String sourceProductID = "RAW-2000.01";
        String productID = "PRODUCT0.01";
        TestInfos.StbInfo stbInfo = new TestInfos.StbInfo(bankID, sourceProductID, productID);
        ObjectIdentifier lotID = stbCase.STB_Normal(stbInfo);

        //【step2】chose param to set up psm
        String splitOperationNum = "2000.0400";
        String returnOperationNum = "2000.0200";
        String mergeOperationNum = "2000.0500";
        Boolean dynamicFlag = false;

        //【step3】get wafer list
        List<ObjectIdentifier> lotIDs = new ArrayList<>();
        lotIDs.add(lotID);
        Results.LotInfoInqResult lotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();
        ObjectIdentifier familyLotID = lotInfoCase.getLotInfoList().get(0).getLotBasicInfo().getFamilyLotID();
        List<Infos.WaferListInLotFamilyInfo> waferListInLotFamilyInfos = (List<Infos.WaferListInLotFamilyInfo>) lotGeneralTestCase.waferListInLotFamilyInfoInqCase(familyLotID).getBody();
        List<ObjectIdentifier> waferList = new ArrayList<>();
        waferList.add(waferListInLotFamilyInfos.get(0).getWaferID());
        waferList.add(waferListInLotFamilyInfos.get(1).getWaferID());

        //【step4】psm relation
        this.planedSplitWithSplitPointAndReturnPointAndMergePoint(null, lotID,splitOperationNum,returnOperationNum,mergeOperationNum,dynamicFlag,waferList);

        //【step5】skip to 3000.0200;
        String skipOperationNum = "3000.0200";
        lotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();
        Infos.LotInfo lotInfo = lotInfoCase.getLotInfoList().get(0);
        Results.LotOperationSelectionInqResult lotOperationSelectionInqResult1 = (Results.LotOperationSelectionInqResult) lotGeneralTestCase.getLotOperationSelectionInq(lotID, false, true, true).getBody();
        List<Infos.OperationNameAttributes> operationNameAttributesList1 = lotOperationSelectionInqResult1.getOperationNameAttributesAttributes().getContent();

        Infos.OperationNameAttributes operationNameAttributes1 = lotGeneralTestCase.getOperationAttributesByOperationNumber(operationNameAttributesList1,skipOperationNum);

        Params.SkipReqParams skipReqParams = new Params.SkipReqParams();
        skipReqParams.setCurrentOperationNumber(lotInfo.getLotOperationInfo().getOperationNumber());
        skipReqParams.setCurrentRouteID(lotInfo.getLotOperationInfo().getRouteID());
        skipReqParams.setLocateDirection(true);
        skipReqParams.setLotID(lotID);
        skipReqParams.setOperationID(operationNameAttributes1.getOperationID());
        skipReqParams.setOperationNumber(operationNameAttributes1.getOperationNumber());
        skipReqParams.setProcessRef(operationNameAttributes1.getProcessRef());
        skipReqParams.setRouteID(operationNameAttributes1.getRouteID());
        skipReqParams.setSeqno(-1);
        skipReqParams.setSequenceNumber(0);
        operationSkipTestCase.operationSkip(skipReqParams);

        //【step6】 check the parent lot split?
        lotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();
        Validations.assertCheck(CimArrayUtils.getSize(lotInfoCase.getLotListInCassetteInfo().getLotIDList()) == 1,"parent lot split must not have a child lot");

        //【step5】skip to splitPoint;
        lotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();
        lotInfo = lotInfoCase.getLotInfoList().get(0);
        lotOperationSelectionInqResult1 = (Results.LotOperationSelectionInqResult) lotGeneralTestCase.getLotOperationSelectionInq(lotID, false, true, false).getBody();
        operationNameAttributesList1 = lotOperationSelectionInqResult1.getOperationNameAttributesAttributes().getContent();

        operationNameAttributes1 = lotGeneralTestCase.getOperationAttributesByOperationNumber(operationNameAttributesList1,splitOperationNum);

        skipReqParams = new Params.SkipReqParams();
        skipReqParams.setCurrentOperationNumber(lotInfo.getLotOperationInfo().getOperationNumber());
        skipReqParams.setCurrentRouteID(lotInfo.getLotOperationInfo().getRouteID());
        skipReqParams.setLocateDirection(false);
        skipReqParams.setLotID(lotID);
        skipReqParams.setOperationID(operationNameAttributes1.getOperationID());
        skipReqParams.setOperationNumber(operationNameAttributes1.getOperationNumber());
        skipReqParams.setProcessRef(operationNameAttributes1.getProcessRef());
        skipReqParams.setRouteID(operationNameAttributes1.getRouteID());
        skipReqParams.setSeqno(-1);
        skipReqParams.setSequenceNumber(0);
        operationSkipTestCase.operationSkip(skipReqParams);

        //【step6】 check the parent lot split?
        lotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();
        Validations.assertCheck(CimArrayUtils.getSize(lotInfoCase.getLotListInCassetteInfo().getLotIDList()) == 2,"parent lot split must  have a child lot");
    }

    public void Specify_SplitPoints_Or_MergePoints_On_The_Operations_In_FlowBatch_Section() {
        //【step1】STB a product lot 25 wafer
        String bankID = "BNK-0S";
        String sourceProductID = "RAW-2000.01";
        String productID = "PRODUCT0.01";
        TestInfos.StbInfo stbInfo = new TestInfos.StbInfo(bankID, sourceProductID, productID);
        ObjectIdentifier lotID = stbCase.STB_Normal(stbInfo);

        //【step2】chose param to set up psm
        String splitOperationNum = "4000.0200";
        String returnOperationNum = "5000.0100";
        String mergeOperationNum = "5000.0100";
        Boolean dynamicFlag = true;

        //【step3】get wafer list
        List<ObjectIdentifier> lotIDs = new ArrayList<>();
        lotIDs.add(lotID);
        Results.LotInfoInqResult lotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();
        ObjectIdentifier familyLotID = lotInfoCase.getLotInfoList().get(0).getLotBasicInfo().getFamilyLotID();
        List<Infos.WaferListInLotFamilyInfo> waferListInLotFamilyInfos = (List<Infos.WaferListInLotFamilyInfo>) lotGeneralTestCase.waferListInLotFamilyInfoInqCase(familyLotID).getBody();
        List<ObjectIdentifier> waferList = new ArrayList<>();
        waferList.add(waferListInLotFamilyInfos.get(0).getWaferID());
        waferList.add(waferListInLotFamilyInfos.get(1).getWaferID());

        //【step4】psm relation
        try {
            this.planedSplitWithSplitPointAndReturnPointAndMergePoint(null, lotID,splitOperationNum,returnOperationNum,mergeOperationNum,dynamicFlag,waferList);
        } catch (ServiceException e) {
            if (!Validations.isEquals(retCodeConfig.getProcessInBatchSection(),e.getCode())){
                throw e;
            }
        }
    }

    public void PlannedSplit_After_MoveOut() {
        //【step1】STB a product lot 25 wafer
        String bankID = "BNK-0S";
        String sourceProductID = "RAW-2000.01";
        String productID = "PRODUCT0.01";
        TestInfos.StbInfo stbInfo = new TestInfos.StbInfo(bankID, sourceProductID, productID);
        ObjectIdentifier lotID = stbCase.STB_Normal(stbInfo);

        //【step2】chose param to set up psm
        String splitOperationNum = "1000.0200";
        String returnOperationNum = "2000.0100";
        String mergeOperationNum = "2000.0100";
        Boolean dynamicFlag = true;

        //【step3】get wafer list
        List<ObjectIdentifier> lotIDs = new ArrayList<>();
        lotIDs.add(lotID);
        Results.LotInfoInqResult lotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();
        ObjectIdentifier familyLotID = lotInfoCase.getLotInfoList().get(0).getLotBasicInfo().getFamilyLotID();
        ObjectIdentifier cassetteID = lotInfoCase.getLotListInCassetteInfo().getCassetteID();
        List<Infos.WaferListInLotFamilyInfo> waferListInLotFamilyInfos = (List<Infos.WaferListInLotFamilyInfo>) lotGeneralTestCase.waferListInLotFamilyInfoInqCase(familyLotID).getBody();
        List<ObjectIdentifier> waferList = new ArrayList<>();
        waferList.add(waferListInLotFamilyInfos.get(0).getWaferID());
        waferList.add(waferListInLotFamilyInfos.get(1).getWaferID());

        //【step4】psm relation
        this.planedSplitWithSplitPointAndReturnPointAndMergePoint(null, lotID,splitOperationNum,returnOperationNum,mergeOperationNum,dynamicFlag,waferList);

        //【step5】load to psm test equipment 1SRT01
        List<String> purposeList = (List<String>) equipmentTestCase.getLoadPurposeTypeCase(BizConstant.SP_LOADPURPOSETYPE_PROCESSLOT).getBody();
        equipmentTestCase.carrierLoadingRpt(new ObjectIdentifier(PSM_TEST_EQUIPMENT_ONE),cassetteID,new ObjectIdentifier(LOAD_PORT_ONE) , purposeList.get(0));

        //【step6】move in the product lot and monitor lot
        // step6-1】get lotsInfoForOpeStart info
        List<ObjectIdentifier> cassetteIDs = new ArrayList<>();
        cassetteIDs.add(cassetteID);
        Results.LotsMoveInInfoInqResult lotsMoveInInfoInqResult = (Results.LotsMoveInInfoInqResult) equipmentTestCase.lotsMoveInInfoInq(new ObjectIdentifier(PSM_TEST_EQUIPMENT_ONE), cassetteIDs).getBody();
        // 【step6-2】move in
        Results.MoveInReqResult moveInResult = (Results.MoveInReqResult) equipmentTestCase.movInReq(lotsMoveInInfoInqResult.getControlJobID(), lotsMoveInInfoInqResult.getEquipmentID(), lotsMoveInInfoInqResult.getPortGroupID(), false, lotsMoveInInfoInqResult.getStartCassetteList()).getBody();

        //【step7】move out the  lot
        Params.OpeComWithDataReqParams opeComWithDataReqParams = new Params.OpeComWithDataReqParams();
        opeComWithDataReqParams.setControlJobID(moveInResult.getControlJobID());
        opeComWithDataReqParams.setUser(testUtils.getUser());
        opeComWithDataReqParams.setEquipmentID(new ObjectIdentifier(PSM_TEST_EQUIPMENT_ONE));
        equipmentTestCase.moveOut(opeComWithDataReqParams);

        //【step8】check parent lot split a child lot
        Results.LotInfoInqResult lastLotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();
        Validations.assertCheck(CimArrayUtils.getSize(lastLotInfoCase.getLotListInCassetteInfo().getLotIDList()) == 2,"parent lot split must have a child lot");
    }

    public void Specify_SplitPoints_On_The_EntryPoint_Of_A_FlowBatch_Section() {
        //【step1】STB a product lot 25 wafer
        String bankID = "BNK-0S";
        String sourceProductID = "RAW-2000.01";
        String productID = "PRODUCT0.01";
        TestInfos.StbInfo stbInfo = new TestInfos.StbInfo(bankID, sourceProductID, productID);
        ObjectIdentifier lotID = stbCase.STB_Normal(stbInfo);

        //【step2】chose param to set up psm
        String splitOperationNum = "4000.0100";
        String returnOperationNum = "5000.0100";
        String mergeOperationNum = "5000.0100";
        Boolean dynamicFlag = true;

        //【step3】get wafer list
        List<ObjectIdentifier> lotIDs = new ArrayList<>();
        lotIDs.add(lotID);
        Results.LotInfoInqResult lotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();
        ObjectIdentifier familyLotID = lotInfoCase.getLotInfoList().get(0).getLotBasicInfo().getFamilyLotID();
        List<Infos.WaferListInLotFamilyInfo> waferListInLotFamilyInfos = (List<Infos.WaferListInLotFamilyInfo>) lotGeneralTestCase.waferListInLotFamilyInfoInqCase(familyLotID).getBody();
        List<ObjectIdentifier> waferList = new ArrayList<>();
        waferList.add(waferListInLotFamilyInfos.get(0).getWaferID());
        waferList.add(waferListInLotFamilyInfos.get(1).getWaferID());

        //【step4】psm relation
        this.planedSplitWithSplitPointAndReturnPointAndMergePoint(null, lotID,splitOperationNum,returnOperationNum,mergeOperationNum,dynamicFlag,waferList);
    }

    public void PlannedMerge_With_OnHold_With_ChildLot_Or_ParentLot() {
        //【step1】STB a product lot 25 wafer
        String bankID = "BNK-0S";
        String sourceProductID = "RAW-2000.01";
        String productID = "PRODUCT0.01";
        TestInfos.StbInfo stbInfo = new TestInfos.StbInfo(bankID, sourceProductID, productID);
        ObjectIdentifier lotID = stbCase.STB_Normal(stbInfo);

        //【step2】chose param to set up psm
        String splitOperationNum = "2000.0200";
        String returnOperationNum = "2000.0200";
        String mergeOperationNum = "3000.0300";
        Boolean dynamicFlag = true;

        //【step3】get wafer list
        List<ObjectIdentifier> lotIDs = new ArrayList<>();
        lotIDs.add(lotID);
        Results.LotInfoInqResult lotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();
        ObjectIdentifier familyLotID = lotInfoCase.getLotInfoList().get(0).getLotBasicInfo().getFamilyLotID();
        List<Infos.WaferListInLotFamilyInfo> waferListInLotFamilyInfos = (List<Infos.WaferListInLotFamilyInfo>) lotGeneralTestCase.waferListInLotFamilyInfoInqCase(familyLotID).getBody();
        List<ObjectIdentifier> waferList = new ArrayList<>();
        waferList.add(waferListInLotFamilyInfos.get(0).getWaferID());
        waferList.add(waferListInLotFamilyInfos.get(1).getWaferID());

        //【step4】psm relation skip to split point and merge point
        ObjectIdentifier childLotID = this.planedSplitWithSplitPointAndReturnPointAndMergePointAndExitSkipSplitPointAndSkipMergePoint(null, lotID, splitOperationNum, returnOperationNum, mergeOperationNum, dynamicFlag, waferList);

        //【step5】check child lot exist
        Validations.assertCheck(!CimObjectUtils.isEmpty(childLotID),"parent lot split must have a child lot");


        //【step6】switch case
        //【step6-1】case 1 Planned Merge with OHold Child-Lot but not OnHold with Parent-Lot
        lotHoldTestCase.lotHoldReleaseReqCase(lotID);
        try {
            lotMergeTestCase.mergeLotReq(lotID,childLotID);
        } catch (ServiceException e) {
            if (!Validations.isEquals(retCodeConfig.getHoldRecordOfChildOwn(),e.getCode())){
                throw e;
            }
        }
        //【step6-2】case 2 Planned Merge with not Onhold Child-Lot and Parent-Lot
        lotHoldTestCase.lotHoldReleaseReqCase(childLotID);
        try {
            lotMergeTestCase.mergeLotReq(lotID,childLotID);
        } catch (ServiceException e) {
            if (!Validations.isEquals(retCodeConfig.getLotNotHeld(),e.getCode())){
                throw e;
            }
        }
    }

    public void PlannedMerge_With_OnHold_With_ChildLot_NotOnHold_And_ParentLot_OnHold() {
        //【step1】STB a product lot 25 wafer
        String bankID = "BNK-0S";
        String sourceProductID = "RAW-2000.01";
        String productID = "PRODUCT0.01";
        TestInfos.StbInfo stbInfo = new TestInfos.StbInfo(bankID, sourceProductID, productID);
        ObjectIdentifier lotID = stbCase.STB_Normal(stbInfo);

        //【step2】chose param to set up psm
        String splitOperationNum = "2000.0200";
        String returnOperationNum = "2000.0200";
        String mergeOperationNum = "3000.0300";
        Boolean dynamicFlag = true;

        //【step3】get wafer list
        List<ObjectIdentifier> lotIDs = new ArrayList<>();
        lotIDs.add(lotID);
        Results.LotInfoInqResult lotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();
        ObjectIdentifier familyLotID = lotInfoCase.getLotInfoList().get(0).getLotBasicInfo().getFamilyLotID();
        List<Infos.WaferListInLotFamilyInfo> waferListInLotFamilyInfos = (List<Infos.WaferListInLotFamilyInfo>) lotGeneralTestCase.waferListInLotFamilyInfoInqCase(familyLotID).getBody();
        List<ObjectIdentifier> waferList = new ArrayList<>();
        waferList.add(waferListInLotFamilyInfos.get(0).getWaferID());
        waferList.add(waferListInLotFamilyInfos.get(1).getWaferID());

        //【step4】psm relation skip to split point and merge point
        ObjectIdentifier childLotID = this.planedSplitWithSplitPointAndReturnPointAndMergePointAndExitSkipSplitPointAndSkipMergePoint(null, lotID, splitOperationNum, returnOperationNum, mergeOperationNum, dynamicFlag, waferList);

        //【step5】check child lot exist
        Validations.assertCheck(!CimObjectUtils.isEmpty(childLotID),"parent lot split must have a child lot");

        //【step6】switch case
        lotHoldTestCase.lotHoldReleaseReqCase(childLotID);
        try {
            lotMergeTestCase.mergeLotReq(lotID,childLotID);
        } catch (ServiceException e) {
            if (!Validations.isEquals(retCodeConfig.getLotNotHeld(),e.getCode())){
                throw e;
            }
        }
    }

    public void The_MergePoint_IsBefore_ReturnPoint_Of_SubRoute() {
        //【step1】STB a product lot 25 wafer
        String bankID = "BNK-0S";
        String sourceProductID = "RAW-2000.01";
        String productID = "PRODUCT0.01";
        TestInfos.StbInfo stbInfo = new TestInfos.StbInfo(bankID, sourceProductID, productID);
        ObjectIdentifier lotID = stbCase.STB_Normal(stbInfo);

        //【step2】chose param to set up psm
        String splitOperationNum = "2000.0400";
        String returnOperationNum = "2000.0200";
        String mergeOperationNum = "2000.0400";
        Boolean dynamicFlag = false;

        //【step3】get wafer list
        List<ObjectIdentifier> lotIDs = new ArrayList<>();
        lotIDs.add(lotID);
        Results.LotInfoInqResult lotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();
        ObjectIdentifier familyLotID = lotInfoCase.getLotInfoList().get(0).getLotBasicInfo().getFamilyLotID();
        List<Infos.WaferListInLotFamilyInfo> waferListInLotFamilyInfos = (List<Infos.WaferListInLotFamilyInfo>) lotGeneralTestCase.waferListInLotFamilyInfoInqCase(familyLotID).getBody();
        List<ObjectIdentifier> waferList = new ArrayList<>();
        waferList.add(waferListInLotFamilyInfos.get(0).getWaferID());
        waferList.add(waferListInLotFamilyInfos.get(1).getWaferID());

        try {
            this.planedSplitWithSplitPointAndReturnPointAndMergePoint(null, lotID,splitOperationNum,returnOperationNum,mergeOperationNum,dynamicFlag,waferList);
        } catch (ServiceException e) {
            if (!Validations.isEquals(retCodeConfig.getInvalidExperimentalLotMergePoint(),e.getCode())){
                throw e;
            }
        }

        mergeOperationNum = "2000.0100";

        try {
            this.planedSplitWithSplitPointAndReturnPointAndMergePoint(null, lotID,splitOperationNum,returnOperationNum,mergeOperationNum,dynamicFlag,waferList);
        } catch (ServiceException e) {
            if (!Validations.isEquals(retCodeConfig.getInvalidExperimentalLotMergePoint(),e.getCode())){
                throw e;
            }
        }
    }

    public void Add_SplitRecord_At_SubBranch_Which_Wafer_Did_Owned_By_Branch_Or_Not() {
        //【step1】STB a product lot 25 wafer
        String bankID = "BNK-0S";
        String sourceProductID = "RAW-2000.01";
        String productID = "PRODUCT0.01";
        TestInfos.StbInfo stbInfo = new TestInfos.StbInfo(bankID, sourceProductID, productID);
        ObjectIdentifier lotID = stbCase.STB_Normal(stbInfo);

        //【step2】chose param to set up psm
        String splitOperationNum = "2000.0400";
        String returnOperationNum = "2000.0200";
        String mergeOperationNum = "2000.0500";
        Boolean dynamicFlag = false;

        //【step3】get wafer list
        List<ObjectIdentifier> lotIDs = new ArrayList<>();
        lotIDs.add(lotID);
        Results.LotInfoInqResult lotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();
        ObjectIdentifier familyLotID = lotInfoCase.getLotInfoList().get(0).getLotBasicInfo().getFamilyLotID();
        List<Infos.WaferListInLotFamilyInfo> waferListInLotFamilyInfos = (List<Infos.WaferListInLotFamilyInfo>) lotGeneralTestCase.waferListInLotFamilyInfoInqCase(familyLotID).getBody();
        List<ObjectIdentifier> waferList = new ArrayList<>();
        //wafer 1,2,3
        waferList.add(waferListInLotFamilyInfos.get(0).getWaferID());
        waferList.add(waferListInLotFamilyInfos.get(1).getWaferID());
        waferList.add(waferListInLotFamilyInfos.get(2).getWaferID());

        this.planedSplitWithSplitPointAndReturnPointAndMergePoint(null, lotID,splitOperationNum,returnOperationNum,mergeOperationNum,dynamicFlag,waferList);

        //【step4】case 1:step 2000.0400 and click next subRoute and choice other wafer
        String newSplitOperationNum = "1000.0200";
        String newReturnOperationNum = "1000.0300";
        String newMergeOperationNum = "1000.0300";
        ObjectIdentifier newRouteID = new ObjectIdentifier("DEV_BRCH.01");
        ObjectIdentifier backRouteID = new ObjectIdentifier("LAYER0MA.01");
        waferList = new ArrayList<>();
        waferList.add(waferListInLotFamilyInfos.get(3).getWaferID());
        waferList.add(waferListInLotFamilyInfos.get(4).getWaferID());
        try {
            this.planedSplitWithSplitPointAndReturnPointAndMergePointForNextBranch(newRouteID,backRouteID,lotID,newSplitOperationNum,splitOperationNum,newReturnOperationNum,newMergeOperationNum,dynamicFlag,waferList);
        } catch (ServiceException e) {
            if (!Validations.isEquals(retCodeConfig.getPsmWaferInvalid(),e.getCode())){
                throw e;
            }
        }
        //【step5】case 2: step 2000.0400 and click next subRoute and choice the wafer from first waferList
        waferList = new ArrayList<>();
        waferList.add(waferListInLotFamilyInfos.get(0).getWaferID());
        waferList.add(waferListInLotFamilyInfos.get(1).getWaferID());
        this.planedSplitWithSplitPointAndReturnPointAndMergePointForNextBranch(newRouteID,backRouteID,lotID,newSplitOperationNum,splitOperationNum,newReturnOperationNum,newMergeOperationNum,dynamicFlag,waferList);


        //【step6】skip the parent lot to split point
        lotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();
        //【step6-1】 skip to the split point
        Infos.LotInfo lotInfo = lotInfoCase.getLotInfoList().get(0);
        Results.LotOperationSelectionInqResult lotOperationSelectionInqResult1 = (Results.LotOperationSelectionInqResult) lotGeneralTestCase.getLotOperationSelectionInq(lotID, false, true, true).getBody();
        List<Infos.OperationNameAttributes> operationNameAttributesList1 = lotOperationSelectionInqResult1.getOperationNameAttributesAttributes().getContent();

        Infos.OperationNameAttributes operationNameAttributes1 = lotGeneralTestCase.getOperationAttributesByOperationNumber(operationNameAttributesList1,splitOperationNum);

        Params.SkipReqParams skipReqParams = new Params.SkipReqParams();
        skipReqParams.setCurrentOperationNumber(lotInfo.getLotOperationInfo().getOperationNumber());
        skipReqParams.setCurrentRouteID(lotInfo.getLotOperationInfo().getRouteID());
        skipReqParams.setLocateDirection(true);
        skipReqParams.setLotID(lotID);
        skipReqParams.setOperationID(operationNameAttributes1.getOperationID());
        skipReqParams.setOperationNumber(operationNameAttributes1.getOperationNumber());
        skipReqParams.setProcessRef(operationNameAttributes1.getProcessRef());
        skipReqParams.setRouteID(operationNameAttributes1.getRouteID());
        skipReqParams.setSeqno(-1);
        skipReqParams.setSequenceNumber(0);
        operationSkipTestCase.operationSkip(skipReqParams);

        //【step7】check the child lot;
        lotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();
        List<ObjectIdentifier> lotIDList = lotInfoCase.getLotListInCassetteInfo().getLotIDList();
        Validations.assertCheck(CimArrayUtils.getSize(lotIDList) == 2,"parent lot split a child lot");
        lotIDList.sort((x,y)->x.getValue().compareTo(y.getValue()));
        lotID = lotIDList.get(0);
        ObjectIdentifier childLotID = lotIDList.get(1);

        //【step7-1】lot hold release the psm hold
        lotHoldTestCase.lotHoldReleaseReqCase(lotID);
        lotHoldTestCase.lotHoldReleaseReqCase(childLotID);

        //【step8】skip the child lot to second psm
        lotIDs = new ArrayList<>();
        lotIDs.add(childLotID);
        lotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();
        Infos.LotInfo lotInfo1 = lotInfoCase.getLotInfoList().get(0);
        Results.LotOperationSelectionInqResult lotOperationSelectionInqResult2 = (Results.LotOperationSelectionInqResult) lotGeneralTestCase.getLotOperationSelectionInq(childLotID, false, true, true).getBody();
        List<Infos.OperationNameAttributes> operationNameAttributesList2 = lotOperationSelectionInqResult2.getOperationNameAttributesAttributes().getContent();

        Infos.OperationNameAttributes operationNameAttributes2 = lotGeneralTestCase.getOperationAttributesByOperationNumber(operationNameAttributesList2,newSplitOperationNum);

        skipReqParams = new Params.SkipReqParams();
        skipReqParams.setCurrentOperationNumber(lotInfo1.getLotOperationInfo().getOperationNumber());
        skipReqParams.setCurrentRouteID(lotInfo1.getLotOperationInfo().getRouteID());
        skipReqParams.setLocateDirection(true);
        skipReqParams.setLotID(childLotID);
        skipReqParams.setOperationID(operationNameAttributes2.getOperationID());
        skipReqParams.setOperationNumber(operationNameAttributes2.getOperationNumber());
        skipReqParams.setProcessRef(operationNameAttributes2.getProcessRef());
        skipReqParams.setRouteID(operationNameAttributes2.getRouteID());
        skipReqParams.setSeqno(-1);
        skipReqParams.setSequenceNumber(0);
        operationSkipTestCase.operationSkip(skipReqParams);

        lotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();
        List<ObjectIdentifier> newLotIDList = lotInfoCase.getLotListInCassetteInfo().getLotIDList();
        newLotIDList.sort((x,y) -> x.getValue().compareTo(y.getValue()));
        Validations.assertCheck(CimArrayUtils.getSize(newLotIDList) == 3,"must be have three lot");
        childLotID = newLotIDList.get(1);
        ObjectIdentifier thirdChildLotID = newLotIDList.get(2);

        //【step10】skip the merge point and merge lot
        skipLotToMergePointAndMergeThem(childLotID,thirdChildLotID,newMergeOperationNum,newRouteID.getValue());

        //【step10】skip the merge point and merge lot
        skipLotToMergePointAndMergeThem(lotID,childLotID,mergeOperationNum,backRouteID.getValue());

        //【step11】check the only parent lot
        lotIDs = new ArrayList<>();
        lotIDs.add(lotID);
        lotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();
        newLotIDList = lotInfoCase.getLotListInCassetteInfo().getLotIDList();
        Validations.assertCheck(CimArrayUtils.getSize(newLotIDList) == 1,"the parent have none child lot");
    }


    public void Add_splitRecord_At_SubBranch_Which_WaferOwned_By_Branch_All_Wafers_Are_Branch_To_Sub_Route() {
        //【step1】STB a product lot 25 wafer
        String bankID = "BNK-0S";
        String sourceProductID = "RAW-2000.01";
        String productID = "PRODUCT0.01";
        TestInfos.StbInfo stbInfo = new TestInfos.StbInfo(bankID, sourceProductID, productID);
        ObjectIdentifier lotID = stbCase.STB_Normal(stbInfo);

        //【step2】chose param to set up psm
        String splitOperationNum = "2000.0400";
        String returnOperationNum = "2000.0200";
        String mergeOperationNum = "2000.0500";
        Boolean dynamicFlag = false;

        //【step3】get wafer list
        List<ObjectIdentifier> lotIDs = new ArrayList<>();
        lotIDs.add(lotID);
        Results.LotInfoInqResult lotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();
        ObjectIdentifier familyLotID = lotInfoCase.getLotInfoList().get(0).getLotBasicInfo().getFamilyLotID();
        List<Infos.WaferListInLotFamilyInfo> waferListInLotFamilyInfos = (List<Infos.WaferListInLotFamilyInfo>) lotGeneralTestCase.waferListInLotFamilyInfoInqCase(familyLotID).getBody();
        List<ObjectIdentifier> waferList = new ArrayList<>();
        //wafer 1,2,3
        waferList.add(waferListInLotFamilyInfos.get(0).getWaferID());
        waferList.add(waferListInLotFamilyInfos.get(1).getWaferID());
        waferList.add(waferListInLotFamilyInfos.get(2).getWaferID());

        this.planedSplitWithSplitPointAndReturnPointAndMergePointAction(null, lotID,splitOperationNum,returnOperationNum,mergeOperationNum,dynamicFlag,false,waferList);

        //【step4】case 1:step 2000.0400 and click next subRoute and choice other wafer
        String newSplitOperationNum = "1000.0200";
        String newReturnOperationNum = "1000.0300";
        String newMergeOperationNum = "1000.0300";
        ObjectIdentifier newRouteID = new ObjectIdentifier("DEV_BRCH.01");
        ObjectIdentifier backRouteID = new ObjectIdentifier("LAYER0MA.01");

        this.planedSplitWithSplitPointAndReturnPointAndMergePointForNextBranch(newRouteID,backRouteID,lotID,newSplitOperationNum,splitOperationNum,newReturnOperationNum,newMergeOperationNum,dynamicFlag,waferList);

        //【step6】skip the parent lot to split point
        lotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();
        //【step6-1】 skip to the split point
        Infos.LotInfo lotInfo = lotInfoCase.getLotInfoList().get(0);
        Results.LotOperationSelectionInqResult lotOperationSelectionInqResult1 = (Results.LotOperationSelectionInqResult) lotGeneralTestCase.getLotOperationSelectionInq(lotID, false, true, true).getBody();
        List<Infos.OperationNameAttributes> operationNameAttributesList1 = lotOperationSelectionInqResult1.getOperationNameAttributesAttributes().getContent();

        Infos.OperationNameAttributes operationNameAttributes1 = lotGeneralTestCase.getOperationAttributesByOperationNumber(operationNameAttributesList1,splitOperationNum);

        Params.SkipReqParams skipReqParams = new Params.SkipReqParams();
        skipReqParams.setCurrentOperationNumber(lotInfo.getLotOperationInfo().getOperationNumber());
        skipReqParams.setCurrentRouteID(lotInfo.getLotOperationInfo().getRouteID());
        skipReqParams.setLocateDirection(true);
        skipReqParams.setLotID(lotID);
        skipReqParams.setOperationID(operationNameAttributes1.getOperationID());
        skipReqParams.setOperationNumber(operationNameAttributes1.getOperationNumber());
        skipReqParams.setProcessRef(operationNameAttributes1.getProcessRef());
        skipReqParams.setRouteID(operationNameAttributes1.getRouteID());
        skipReqParams.setSeqno(-1);
        skipReqParams.setSequenceNumber(0);
        operationSkipTestCase.operationSkip(skipReqParams);

        //【step7】check the child lot;
        lotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();
        List<ObjectIdentifier> lotIDList = lotInfoCase.getLotListInCassetteInfo().getLotIDList();
        Validations.assertCheck(CimArrayUtils.getSize(lotIDList) == 2,"parent lot split a child lot");
        lotIDList.sort((x,y)->x.getValue().compareTo(y.getValue()));
        lotID = lotIDList.get(0);
        ObjectIdentifier childLotID = lotIDList.get(1);

        //【step8】skip the child lot to second psm
        lotIDs = new ArrayList<>();
        lotIDs.add(childLotID);
        lotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();
        Infos.LotInfo lotInfo1 = lotInfoCase.getLotInfoList().get(0);
        Results.LotOperationSelectionInqResult lotOperationSelectionInqResult2 = (Results.LotOperationSelectionInqResult) lotGeneralTestCase.getLotOperationSelectionInq(childLotID, false, true, true).getBody();
        List<Infos.OperationNameAttributes> operationNameAttributesList2 = lotOperationSelectionInqResult2.getOperationNameAttributesAttributes().getContent();

        Infos.OperationNameAttributes operationNameAttributes2 = lotGeneralTestCase.getOperationAttributesByOperationNumber(operationNameAttributesList2,newSplitOperationNum);

        skipReqParams = new Params.SkipReqParams();
        skipReqParams.setCurrentOperationNumber(lotInfo1.getLotOperationInfo().getOperationNumber());
        skipReqParams.setCurrentRouteID(lotInfo1.getLotOperationInfo().getRouteID());
        skipReqParams.setLocateDirection(true);
        skipReqParams.setLotID(childLotID);
        skipReqParams.setOperationID(operationNameAttributes2.getOperationID());
        skipReqParams.setOperationNumber(operationNameAttributes2.getOperationNumber());
        skipReqParams.setProcessRef(operationNameAttributes2.getProcessRef());
        skipReqParams.setRouteID(operationNameAttributes2.getRouteID());
        skipReqParams.setSeqno(-1);
        skipReqParams.setSequenceNumber(0);
        operationSkipTestCase.operationSkip(skipReqParams);

        lotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();
        List<ObjectIdentifier> newLotIDList = lotInfoCase.getLotListInCassetteInfo().getLotIDList();
        Validations.assertCheck(CimArrayUtils.getSize(newLotIDList) == 2,"child lot can not split");
        newLotIDList.sort((x,y)->x.getValue().compareTo(y.getValue()));
        lotID = newLotIDList.get(0);
        childLotID = newLotIDList.get(1);

        //【step10】skip the merge point and merge lot
        skipLotToMergePointAndMergeThem(lotID,childLotID,mergeOperationNum,backRouteID.getValue());

        //【step11】check the only parent lot
        lotIDs = new ArrayList<>();
        lotIDs.add(lotID);
        lotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();
        newLotIDList = lotInfoCase.getLotListInCassetteInfo().getLotIDList();
        Validations.assertCheck(CimArrayUtils.getSize(newLotIDList) == 1,"the parent have none child lot");
    }

    public void Delete_A_PSM_Record() {
        //【step1】STB a product lot 25 wafer
        String bankID = "BNK-0S";
        String sourceProductID = "RAW-2000.01";
        String productID = "PRODUCT0.01";
        TestInfos.StbInfo stbInfo = new TestInfos.StbInfo(bankID, sourceProductID, productID);
        ObjectIdentifier lotID = stbCase.STB_Normal(stbInfo);

        //【step2】chose param to set up psm
        String splitOperationNum = "2000.0400";
        String returnOperationNum = "2000.0200";
        String mergeOperationNum = "2000.0500";
        Boolean dynamicFlag = false;

        //【step3】get wafer list
        List<ObjectIdentifier> lotIDs = new ArrayList<>();
        lotIDs.add(lotID);
        Results.LotInfoInqResult lotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();
        ObjectIdentifier familyLotID = lotInfoCase.getLotInfoList().get(0).getLotBasicInfo().getFamilyLotID();
        List<Infos.WaferListInLotFamilyInfo> waferListInLotFamilyInfos = (List<Infos.WaferListInLotFamilyInfo>) lotGeneralTestCase.waferListInLotFamilyInfoInqCase(familyLotID).getBody();
        List<ObjectIdentifier> waferList = new ArrayList<>();
        //wafer 1,2,3
        waferList.add(waferListInLotFamilyInfos.get(0).getWaferID());
        waferList.add(waferListInLotFamilyInfos.get(1).getWaferID());
        waferList.add(waferListInLotFamilyInfos.get(2).getWaferID());

        this.planedSplitWithSplitPointAndReturnPointAndMergePoint(null, lotID,splitOperationNum,returnOperationNum,mergeOperationNum,dynamicFlag,waferList);

        //【step4】get the psm list
        Boolean detailRequireFlag = true;
        List<Infos.ExperimentalLotInfo> experimentalLotInfoList = (List<Infos.ExperimentalLotInfo>) planSplitMergeTestCase.experimentalLotList(familyLotID, detailRequireFlag).getBody();
        Validations.assertCheck(CimArrayUtils.getSize(experimentalLotInfoList) == 1,"the psm list must have one");

        //【step5】delete the psm record just add
        ObjectIdentifier splitRouteID = experimentalLotInfoList.get(0).getSplitRouteID();
        String splitOperationNumber = experimentalLotInfoList.get(0).getSplitOperationNumber();
        ObjectIdentifier originalRouteID = experimentalLotInfoList.get(0).getOriginalRouteID();
        String originalOperationNumber = experimentalLotInfoList.get(0).getOriginalOperationNumber();
        planSplitMergeTestCase.psmLotRemoveReqCase(familyLotID,splitRouteID,splitOperationNumber,originalRouteID,originalOperationNumber);

        //【step6】check psm record
        experimentalLotInfoList = (List<Infos.ExperimentalLotInfo>) planSplitMergeTestCase.experimentalLotList(familyLotID, detailRequireFlag).getBody();
        Validations.assertCheck(CimArrayUtils.getSize(experimentalLotInfoList) == 0,"the psm list must have one");

        //【step7】add a psm list too
        this.planedSplitWithSplitPointAndReturnPointAndMergePoint(null, lotID,splitOperationNum,returnOperationNum,mergeOperationNum,dynamicFlag,waferList);

        //【step8】step to psm list
        detailRequireFlag = false;
        experimentalLotInfoList = (List<Infos.ExperimentalLotInfo>) planSplitMergeTestCase.experimentalLotList(familyLotID, detailRequireFlag).getBody();
        Validations.assertCheck(CimArrayUtils.getSize(experimentalLotInfoList) == 1,"the psm list must have one");

        //【step9】delete the psm list in psm list
        splitRouteID = experimentalLotInfoList.get(0).getSplitRouteID();
        splitOperationNumber = experimentalLotInfoList.get(0).getSplitOperationNumber();
        originalRouteID = experimentalLotInfoList.get(0).getOriginalRouteID();
        originalOperationNumber = experimentalLotInfoList.get(0).getOriginalOperationNumber();
        planSplitMergeTestCase.psmLotRemoveReqCase(familyLotID,splitRouteID,splitOperationNumber,originalRouteID,originalOperationNumber);
    }

    public void Delete_A_PSM_Record_After_It_Executed_Or_In_PSM_List() {
        //【step1】STB a product lot 25 wafer
        String bankID = "BNK-0S";
        String sourceProductID = "RAW-2000.01";
        String productID = "PRODUCT0.01";
        TestInfos.StbInfo stbInfo = new TestInfos.StbInfo(bankID, sourceProductID, productID);
        ObjectIdentifier lotID = stbCase.STB_Normal(stbInfo);

        //【step2】chose param to set up psm
        String splitOperationNum = "2000.0400";
        String returnOperationNum = "2000.0200";
        String mergeOperationNum = "2000.0500";
        Boolean dynamicFlag = false;

        //【step3】get wafer list
        List<ObjectIdentifier> lotIDs = new ArrayList<>();
        lotIDs.add(lotID);
        Results.LotInfoInqResult lotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();
        ObjectIdentifier familyLotID = lotInfoCase.getLotInfoList().get(0).getLotBasicInfo().getFamilyLotID();
        List<Infos.WaferListInLotFamilyInfo> waferListInLotFamilyInfos = (List<Infos.WaferListInLotFamilyInfo>) lotGeneralTestCase.waferListInLotFamilyInfoInqCase(familyLotID).getBody();
        List<ObjectIdentifier> waferList = new ArrayList<>();
        //wafer 1,2,3
        waferList.add(waferListInLotFamilyInfos.get(0).getWaferID());
        waferList.add(waferListInLotFamilyInfos.get(1).getWaferID());
        waferList.add(waferListInLotFamilyInfos.get(2).getWaferID());

        //【step4】register the psm record and exec skip and merge
        this.planedSplitWithSplitPointAndReturnPointAndMergePointAndExecSkipAndMerge(null, lotID,splitOperationNum,returnOperationNum,mergeOperationNum,dynamicFlag,waferList);

        //【step5】get the psm list
        Boolean detailRequireFlag = true;
        List<Infos.ExperimentalLotInfo> experimentalLotInfoList = (List<Infos.ExperimentalLotInfo>) planSplitMergeTestCase.experimentalLotList(familyLotID, detailRequireFlag).getBody();
        Validations.assertCheck(CimArrayUtils.getSize(experimentalLotInfoList) == 1,"the psm list must have one");

        //【step6】delete the psm record just add
        ObjectIdentifier splitRouteID = experimentalLotInfoList.get(0).getSplitRouteID();
        String splitOperationNumber = experimentalLotInfoList.get(0).getSplitOperationNumber();
        ObjectIdentifier originalRouteID = experimentalLotInfoList.get(0).getOriginalRouteID();
        String originalOperationNumber = experimentalLotInfoList.get(0).getOriginalOperationNumber();
        planSplitMergeTestCase.psmLotRemoveReqCase(familyLotID,splitRouteID,splitOperationNumber,originalRouteID,originalOperationNumber);

        //【step7】skip to splitPoint
        Results.LotInfoInqResult secondLotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();
        //【step7-1】 skip to the start point
        Infos.LotInfo lotInfo = secondLotInfoCase.getLotInfoList().get(0);
        Results.LotOperationSelectionInqResult lotOperationSelectionInqResult1 = (Results.LotOperationSelectionInqResult) lotGeneralTestCase.getLotOperationSelectionInq(lotID, false, true, false).getBody();
        List<Infos.OperationNameAttributes> operationNameAttributesList1 = lotOperationSelectionInqResult1.getOperationNameAttributesAttributes().getContent();

        Infos.OperationNameAttributes operationNameAttributes1 = lotGeneralTestCase.getOperationAttributesByOperationNumber(operationNameAttributesList1,"1000.0100");

        Params.SkipReqParams skipReqParams = new Params.SkipReqParams();
        skipReqParams.setCurrentOperationNumber(lotInfo.getLotOperationInfo().getOperationNumber());
        skipReqParams.setCurrentRouteID(lotInfo.getLotOperationInfo().getRouteID());
        skipReqParams.setLocateDirection(false);
        skipReqParams.setLotID(lotID);
        skipReqParams.setOperationID(operationNameAttributes1.getOperationID());
        skipReqParams.setOperationNumber(operationNameAttributes1.getOperationNumber());
        skipReqParams.setProcessRef(operationNameAttributes1.getProcessRef());
        skipReqParams.setRouteID(operationNameAttributes1.getRouteID());
        skipReqParams.setSeqno(-1);
        skipReqParams.setSequenceNumber(0);
        operationSkipTestCase.operationSkip(skipReqParams);

        //【step8】register the psm record and exec skip and merge second time
        this.planedSplitWithSplitPointAndReturnPointAndMergePointAndExecSkipAndMerge(null, lotID,splitOperationNum,returnOperationNum,mergeOperationNum,dynamicFlag,waferList);

        detailRequireFlag = false;
        experimentalLotInfoList = (List<Infos.ExperimentalLotInfo>) planSplitMergeTestCase.experimentalLotList(familyLotID, detailRequireFlag).getBody();
        Validations.assertCheck(CimArrayUtils.getSize(experimentalLotInfoList) == 1,"the psm list must have one");

        //【step9】delete the psm list in psm list
        splitRouteID = experimentalLotInfoList.get(0).getSplitRouteID();
        splitOperationNumber = experimentalLotInfoList.get(0).getSplitOperationNumber();
        originalRouteID = experimentalLotInfoList.get(0).getOriginalRouteID();
        originalOperationNumber = experimentalLotInfoList.get(0).getOriginalOperationNumber();
        planSplitMergeTestCase.psmLotRemoveReqCase(familyLotID,splitRouteID,splitOperationNumber,originalRouteID,originalOperationNumber);
    }

    public void The_MergePoint_Is_At_Or_After_The_Next_Already_Defined_SplitPoint() {
        String bankID = "BNK-0S";
        String sourceProductID = "RAW-2000.01";
        String productID = "PRODUCT0.01";
        TestInfos.StbInfo stbInfo = new TestInfos.StbInfo(bankID, sourceProductID, productID);
        ObjectIdentifier lotID = stbCase.STB_Normal(stbInfo);

        //【step2】chose param to set up psm
        String splitOperationNum = "2000.0400";
        String returnOperationNum = "2000.0200";
        String mergeOperationNum = "2000.0500";
        Boolean dynamicFlag = false;

        //【step3】get wafer list
        List<ObjectIdentifier> lotIDs = new ArrayList<>();
        lotIDs.add(lotID);
        Results.LotInfoInqResult lotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();
        ObjectIdentifier familyLotID = lotInfoCase.getLotInfoList().get(0).getLotBasicInfo().getFamilyLotID();
        List<Infos.WaferListInLotFamilyInfo> waferListInLotFamilyInfos = (List<Infos.WaferListInLotFamilyInfo>) lotGeneralTestCase.waferListInLotFamilyInfoInqCase(familyLotID).getBody();
        List<ObjectIdentifier> waferList = new ArrayList<>();
        waferList.add(waferListInLotFamilyInfos.get(0).getWaferID());
        waferList.add(waferListInLotFamilyInfos.get(1).getWaferID());

        this.planedSplitWithSplitPointAndReturnPointAndMergePoint(null, lotID,splitOperationNum,returnOperationNum,mergeOperationNum,dynamicFlag,waferList);

        splitOperationNum = "2000.0200";
        returnOperationNum = "2000.0400";
        mergeOperationNum = "2000.0400";
        dynamicFlag = true;
        try {
            this.planedSplitWithSplitPointAndReturnPointAndMergePoint(null, lotID,splitOperationNum,returnOperationNum,mergeOperationNum,dynamicFlag,waferList);
        } catch (ServiceException e) {
            if (!Validations.isEquals(retCodeConfig.getInvalidExperimentalLotMergePoint(),e.getCode())){
                throw e;
            }
        }
    }

    public void Delete_PSM_In_Detail_Which_Has_Two_Records_With_Same_Split_And_MergePoint_But_Different_SubRoute_And_Wafers() {
        //【step1】STB a product lot 25 wafer
        String bankID = "BNK-0S";
        String sourceProductID = "RAW-2000.01";
        String productID = "PRODUCT0.01";
        TestInfos.StbInfo stbInfo = new TestInfos.StbInfo(bankID, sourceProductID, productID);
        ObjectIdentifier lotID = stbCase.STB_Normal(stbInfo);

        //【step2】chose param to set up psm
        String splitOperationNum = "2000.0400";
        String returnOperationNum = "2000.0200";
        String mergeOperationNum = "2000.0500";
        Boolean dynamicFlag = false;

        //【step3】get wafer list
        List<ObjectIdentifier> lotIDs = new ArrayList<>();
        lotIDs.add(lotID);
        Results.LotInfoInqResult lotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();
        ObjectIdentifier familyLotID = lotInfoCase.getLotInfoList().get(0).getLotBasicInfo().getFamilyLotID();
        List<Infos.WaferListInLotFamilyInfo> waferListInLotFamilyInfos = (List<Infos.WaferListInLotFamilyInfo>) lotGeneralTestCase.waferListInLotFamilyInfoInqCase(familyLotID).getBody();
        List<ObjectIdentifier> waferList = new ArrayList<>();
        //wafer 1,2,3
        waferList.add(waferListInLotFamilyInfos.get(0).getWaferID());
        waferList.add(waferListInLotFamilyInfos.get(1).getWaferID());
        waferList.add(waferListInLotFamilyInfos.get(2).getWaferID());

        this.planedSplitWithSplitPointAndReturnPointAndMergePoint(null, lotID,splitOperationNum,returnOperationNum,mergeOperationNum,dynamicFlag,waferList);

        //【ste4】register other psm with same split point and merge point but different subRoute and wafers
        waferList = new ArrayList<>();
        waferList.add(waferListInLotFamilyInfos.get(3).getWaferID());
        waferList.add(waferListInLotFamilyInfos.get(4).getWaferID());
        dynamicFlag = true;
        this.planedSplitWithSplitPointAndReturnPointAndMergePoint(null, lotID,splitOperationNum,returnOperationNum,mergeOperationNum,dynamicFlag,waferList);

        //【step5】skip the split point and split
        lotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();
        Infos.LotInfo lotInfo1 = lotInfoCase.getLotInfoList().get(0);
        Results.LotOperationSelectionInqResult lotOperationSelectionInqResult2 = (Results.LotOperationSelectionInqResult) lotGeneralTestCase.getLotOperationSelectionInq(lotID, false, true, true).getBody();
        List<Infos.OperationNameAttributes> operationNameAttributesList2 = lotOperationSelectionInqResult2.getOperationNameAttributesAttributes().getContent();

        Infos.OperationNameAttributes operationNameAttributes2 = lotGeneralTestCase.getOperationAttributesByOperationNumber(operationNameAttributesList2,splitOperationNum);

        Params.SkipReqParams skipReqParams = new Params.SkipReqParams();
        skipReqParams.setCurrentOperationNumber(lotInfo1.getLotOperationInfo().getOperationNumber());
        skipReqParams.setCurrentRouteID(lotInfo1.getLotOperationInfo().getRouteID());
        skipReqParams.setLocateDirection(true);
        skipReqParams.setLotID(lotID);
        skipReqParams.setOperationID(operationNameAttributes2.getOperationID());
        skipReqParams.setOperationNumber(operationNameAttributes2.getOperationNumber());
        skipReqParams.setProcessRef(operationNameAttributes2.getProcessRef());
        skipReqParams.setRouteID(operationNameAttributes2.getRouteID());
        skipReqParams.setSeqno(-1);
        skipReqParams.setSequenceNumber(0);
        operationSkipTestCase.operationSkip(skipReqParams);

        lotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();
        List<ObjectIdentifier> newLotIDList = lotInfoCase.getLotListInCassetteInfo().getLotIDList();
        Validations.assertCheck(CimArrayUtils.getSize(newLotIDList) == 3,"parent lot must split three child lot");

        //【step6】delete the psm record
        Boolean detailRequireFlag = true;
        List<Infos.ExperimentalLotInfo> experimentalLotInfoList = (List<Infos.ExperimentalLotInfo>) planSplitMergeTestCase.experimentalLotList(familyLotID, detailRequireFlag).getBody();
        Validations.assertCheck(CimArrayUtils.getSize(experimentalLotInfoList.get(0).getStrExperimentalLotDetailInfoSeq()) == 2,"the psm list must have two");

        //【step7】delete the psm record just add
        for (Infos.ExperimentalLotInfo experimentalLotInfo : experimentalLotInfoList) {
            ObjectIdentifier splitRouteID = experimentalLotInfo.getSplitRouteID();
            String splitOperationNumber = experimentalLotInfo.getSplitOperationNumber();
            ObjectIdentifier originalRouteID = experimentalLotInfo.getOriginalRouteID();
            String originalOperationNumber = experimentalLotInfo.getOriginalOperationNumber();
            try {
                planSplitMergeTestCase.psmLotRemoveReqCase(familyLotID,splitRouteID,splitOperationNumber,originalRouteID,originalOperationNumber);
            } catch (ServiceException e) {
                if (!Validations.isEquals(retCodeConfig.getExplotAlreadyDone(),e.getCode())){
                    throw e;
                }
            }
        }
    }

    public void Modify_PSM_Record() {
        //【step1】STB a product lot 25 wafer
        String bankID = "BNK-0S";
        String sourceProductID = "RAW-2000.01";
        String productID = "PRODUCT0.01";
        TestInfos.StbInfo stbInfo = new TestInfos.StbInfo(bankID, sourceProductID, productID);
        ObjectIdentifier lotID = stbCase.STB_Normal(stbInfo);

        //【step2】chose param to set up psm
        String splitOperationNum = "2000.0400";
        String returnOperationNum = "2000.0200";
        String mergeOperationNum = "2000.0500";
        Boolean dynamicFlag = false;

        //【step3】get wafer list
        List<ObjectIdentifier> lotIDs = new ArrayList<>();
        lotIDs.add(lotID);
        Results.LotInfoInqResult lotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();
        ObjectIdentifier familyLotID = lotInfoCase.getLotInfoList().get(0).getLotBasicInfo().getFamilyLotID();
        List<Infos.WaferListInLotFamilyInfo> waferListInLotFamilyInfos = (List<Infos.WaferListInLotFamilyInfo>) lotGeneralTestCase.waferListInLotFamilyInfoInqCase(familyLotID).getBody();
        List<ObjectIdentifier> waferList = new ArrayList<>();
        //wafer 1,2,3
        waferList.add(waferListInLotFamilyInfos.get(0).getWaferID());
        waferList.add(waferListInLotFamilyInfos.get(1).getWaferID());
        waferList.add(waferListInLotFamilyInfos.get(2).getWaferID());

        this.planedSplitWithSplitPointAndReturnPointAndMergePoint(null, lotID,splitOperationNum,returnOperationNum,mergeOperationNum,dynamicFlag,waferList);
        Boolean detailRequireFlag = true;
        List<Infos.ExperimentalLotInfo> experimentalLotInfoList = (List<Infos.ExperimentalLotInfo>) planSplitMergeTestCase.experimentalLotList(familyLotID, detailRequireFlag).getBody();
        Validations.assertCheck(CimArrayUtils.getSize(experimentalLotInfoList) == 1,"modify the psm list must have one");

        //【step4】 modify the psm record add wafers
        waferList.add(waferListInLotFamilyInfos.get(3).getWaferID());
        waferList.add(waferListInLotFamilyInfos.get(4).getWaferID());
        experimentalLotInfoList.forEach(x->{x.getStrExperimentalLotDetailInfoSeq().get(0).setWaferIDs(waferList);});
        this.modifyThePsmRecord(experimentalLotInfoList);
        experimentalLotInfoList = (List<Infos.ExperimentalLotInfo>) planSplitMergeTestCase.experimentalLotList(familyLotID, detailRequireFlag).getBody();
        Validations.assertCheck(CimArrayUtils.getSize(experimentalLotInfoList) == 1,"modify the psm list must have one");

        //【step5】modify the psm record subRoute to dynamic route
        Boolean newDynamicFlag = true;
        experimentalLotInfoList.forEach(x->{
            x.getStrExperimentalLotDetailInfoSeq().get(0).setDynamicFlag(newDynamicFlag);
            x.getStrExperimentalLotDetailInfoSeq().get(0).setSubRouteID(((List<Infos.DynamicRouteList>)commonTestCase.dynamicPathListInqCase("Branch","").getBody()).get(0).getRouteID());
        });
        this.modifyThePsmRecord(experimentalLotInfoList);
        experimentalLotInfoList = (List<Infos.ExperimentalLotInfo>) planSplitMergeTestCase.experimentalLotList(familyLotID, detailRequireFlag).getBody();
        Validations.assertCheck(CimArrayUtils.getSize(experimentalLotInfoList) == 1,"modify the psm list must have one");

        //【step6】modify the psm record returnPoint and mergePoint
        String newReturnOperationNum = "2000.0300";
        String newMergeOperationNum = "3000.0100";
        experimentalLotInfoList.forEach(x->{
            x.getStrExperimentalLotDetailInfoSeq().get(0).setReturnOperationNumber(newReturnOperationNum);
            x.getStrExperimentalLotDetailInfoSeq().get(0).setMergeOperationNumber(newMergeOperationNum);
        });
        this.modifyThePsmRecord(experimentalLotInfoList);
        experimentalLotInfoList = (List<Infos.ExperimentalLotInfo>) planSplitMergeTestCase.experimentalLotList(familyLotID, detailRequireFlag).getBody();
        Validations.assertCheck(CimArrayUtils.getSize(experimentalLotInfoList) == 1,"modify the psm list must have one");

        //【step7】modify the psm record action
        Boolean actionFlag = false;
        experimentalLotInfoList.forEach(x->{
            x.setActionEMail(actionFlag);
            x.setActionHold(actionFlag);
        });
        this.modifyThePsmRecord(experimentalLotInfoList);
        experimentalLotInfoList = (List<Infos.ExperimentalLotInfo>) planSplitMergeTestCase.experimentalLotList(familyLotID, detailRequireFlag).getBody();
        Validations.assertCheck(CimArrayUtils.getSize(experimentalLotInfoList) == 1,"modify the psm list must have one");
    }

    public void During_TheLot_Is_OnHold_Register_A_PSM_Record_In_CurrentOperation_Then_HoldRelease() {
        //【step1】STB a product lot 25 wafer
        String bankID = "BNK-0S";
        String sourceProductID = "RAW-2000.01";
        String productID = "PRODUCT0.01";
        TestInfos.StbInfo stbInfo = new TestInfos.StbInfo(bankID, sourceProductID, productID);
        String skipOperationMum = "2000.0400";
        ObjectIdentifier lotID = testUtils.stbAndSkip(stbInfo,skipOperationMum);

        //【step2】onHold the lot on current operation
        String reasonCode = "SOHL";
        String reasonableOperation = "C";
        lotHoldCase.lotHold(lotID.getValue(),reasonCode,reasonableOperation);

        //【step3】chose param to set up psm
        String splitOperationNum = "2000.0400";
        String returnOperationNum = "2000.0200";
        String mergeOperationNum = "2000.0500";
        Boolean dynamicFlag = false;

        //【step4】get wafer list
        List<ObjectIdentifier> lotIDs = new ArrayList<>();
        lotIDs.add(lotID);
        Results.LotInfoInqResult lotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();
        ObjectIdentifier familyLotID = lotInfoCase.getLotInfoList().get(0).getLotBasicInfo().getFamilyLotID();
        List<Infos.WaferListInLotFamilyInfo> waferListInLotFamilyInfos = (List<Infos.WaferListInLotFamilyInfo>) lotGeneralTestCase.waferListInLotFamilyInfoInqCase(familyLotID).getBody();
        List<ObjectIdentifier> waferList = new ArrayList<>();
        waferList.add(waferListInLotFamilyInfos.get(0).getWaferID());
        waferList.add(waferListInLotFamilyInfos.get(1).getWaferID());

        //【step5】psm relation
        this.planedSplitWithSplitPointAndReturnPointAndMergePointAction(null, lotID,splitOperationNum,returnOperationNum,mergeOperationNum,dynamicFlag,false,waferList);

        //【step6】execute lot hold release
        lotHoldTestCase.lotHoldReleaseReqCase(lotID);

        //【step7】check psm exec
        lotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();
        Validations.assertCheck(CimArrayUtils.getSize(lotInfoCase.getLotListInCassetteInfo().getLotIDList()) == 2,"parent lot must have a child lot");
    }

    public void One_SplitPoint_Linked_To_Multiple_SubRoutes() {
        //【step1】STB a product lot 25 wafer
        String bankID = "BNK-0S";
        String sourceProductID = "RAW-2000.01";
        String productID = "PRODUCT0.01";
        TestInfos.StbInfo stbInfo = new TestInfos.StbInfo(bankID, sourceProductID, productID);
        ObjectIdentifier lotID = stbCase.STB_Normal(stbInfo);

        //【step2】chose param to set up psm
        String splitOperationNum = "2000.0400";
        String returnOperationNum = "2000.0200";
        String mergeOperationNum = "2000.0500";
        Boolean dynamicFlag = false;

        //【step3】get wafer list
        List<ObjectIdentifier> lotIDs = new ArrayList<>();
        lotIDs.add(lotID);
        Results.LotInfoInqResult lotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();
        ObjectIdentifier familyLotID = lotInfoCase.getLotInfoList().get(0).getLotBasicInfo().getFamilyLotID();
        List<Infos.WaferListInLotFamilyInfo> waferListInLotFamilyInfos = (List<Infos.WaferListInLotFamilyInfo>) lotGeneralTestCase.waferListInLotFamilyInfoInqCase(familyLotID).getBody();
        List<ObjectIdentifier> waferList = new ArrayList<>();
        waferList.add(waferListInLotFamilyInfos.get(0).getWaferID());

        //【step4】psm relation 1
        this.planedSplitWithSplitPointAndReturnPointAndMergePointAction(null, lotID,splitOperationNum,returnOperationNum,mergeOperationNum,dynamicFlag,false,waferList);

        //【step5】psm relation 2
        waferList = new ArrayList<>();
        waferList.add(waferListInLotFamilyInfos.get(1).getWaferID());
        waferList.add(waferListInLotFamilyInfos.get(2).getWaferID());
        dynamicFlag = true;
        this.planedSplitWithSplitPointAndReturnPointAndMergePointAction(null, lotID,splitOperationNum,returnOperationNum,mergeOperationNum,dynamicFlag,false,waferList);

        //【step6】psm relation 3
        waferList = new ArrayList<>();
        waferList.add(waferListInLotFamilyInfos.get(3).getWaferID());
        this.planedSplitWithSplitPointAndReturnPointAndMergePointAction(null, lotID,splitOperationNum,returnOperationNum,mergeOperationNum,dynamicFlag,false,waferList);

        //【step7】skip to the split operation
        lotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();
        Infos.LotInfo lotInfo1 = lotInfoCase.getLotInfoList().get(0);
        Results.LotOperationSelectionInqResult lotOperationSelectionInqResult2 = (Results.LotOperationSelectionInqResult) lotGeneralTestCase.getLotOperationSelectionInq(lotID, false, true, true).getBody();
        List<Infos.OperationNameAttributes> operationNameAttributesList2 = lotOperationSelectionInqResult2.getOperationNameAttributesAttributes().getContent();

        Infos.OperationNameAttributes operationNameAttributes2 = lotGeneralTestCase.getOperationAttributesByOperationNumber(operationNameAttributesList2,splitOperationNum);

        Params.SkipReqParams skipReqParams = new Params.SkipReqParams();
        skipReqParams.setCurrentOperationNumber(lotInfo1.getLotOperationInfo().getOperationNumber());
        skipReqParams.setCurrentRouteID(lotInfo1.getLotOperationInfo().getRouteID());
        skipReqParams.setLocateDirection(true);
        skipReqParams.setLotID(lotID);
        skipReqParams.setOperationID(operationNameAttributes2.getOperationID());
        skipReqParams.setOperationNumber(operationNameAttributes2.getOperationNumber());
        skipReqParams.setProcessRef(operationNameAttributes2.getProcessRef());
        skipReqParams.setRouteID(operationNameAttributes2.getRouteID());
        skipReqParams.setSeqno(-1);
        skipReqParams.setSequenceNumber(0);
        operationSkipTestCase.operationSkip(skipReqParams);

        lotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();
        List<ObjectIdentifier> newLotIDList = lotInfoCase.getLotListInCassetteInfo().getLotIDList();
        Validations.assertCheck(CimArrayUtils.getSize(newLotIDList) == 4,"parent lot must split three child lot");
    }

    public void After_Successful_GatePass_On_CurrentStep_And_The_Lot_MoveTo_The_NextStep_Which_Has_A_PSM_And_PSM_Will_Execute() {
        //【step1】STB a product lot 25 wafer
        String bankID = "BNK-0S";
        String sourceProductID = "RAW-2000.01";
        String productID = "PRODUCT0.01";
        TestInfos.StbInfo stbInfo = new TestInfos.StbInfo(bankID, sourceProductID, productID);
        String skipOperationMum = "2000.0400";
        ObjectIdentifier lotID = testUtils.stbAndSkip(stbInfo,skipOperationMum);

        //【step2】chose param to set up psm
        String splitOperationNum = "2000.0500";
        String returnOperationNum = "2000.0500";
        String mergeOperationNum = "3000.0100";
        Boolean dynamicFlag = true;

        //【step3】get wafer list
        List<ObjectIdentifier> lotIDs = new ArrayList<>();
        lotIDs.add(lotID);
        Results.LotInfoInqResult lotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();
        ObjectIdentifier familyLotID = lotInfoCase.getLotInfoList().get(0).getLotBasicInfo().getFamilyLotID();
        List<Infos.WaferListInLotFamilyInfo> waferListInLotFamilyInfos = (List<Infos.WaferListInLotFamilyInfo>) lotGeneralTestCase.waferListInLotFamilyInfoInqCase(familyLotID).getBody();
        List<ObjectIdentifier> waferList = new ArrayList<>();
        waferList.add(waferListInLotFamilyInfos.get(0).getWaferID());
        waferList.add(waferListInLotFamilyInfos.get(1).getWaferID());

        //【step4】psm relation
        this.planedSplitWithSplitPointAndReturnPointAndMergePointAction(null, lotID,splitOperationNum,returnOperationNum,mergeOperationNum,dynamicFlag,false,waferList);

        //【step5】
        electronicInformationTestCase.eqpInfoInqCase(new ObjectIdentifier(PSM_TEST_EQUIPMENT_TWO));

        //【step6】 start reserve the two lot
        //【step6 -1】dispatch/what_next_lot_list/inq
        Results.WhatNextLotListResult whatNextLotListResult = (Results.WhatNextLotListResult) equipmentTestCase.whatNextInqCase(new ObjectIdentifier(PSM_TEST_EQUIPMENT_TWO)).getBody();
        List<Infos.WhatNextAttributes> whatNextAttributesContent = (List<Infos.WhatNextAttributes> ) whatNextLotListResult.getWhatNextAttributesPage().getContent();
        Infos.WhatNextAttributes whatNextAttributesMonitor = whatNextAttributesContent.stream().filter(x -> CimObjectUtils.equalsWithValue(x.getLotID(), lotID)).findFirst().orElse(null);

        //【step7】gate pass the lot
        Params.PassThruReqParams passReqParams = new Params.PassThruReqParams();
        passReqParams.setUser(testUtils.getUser());
        passReqParams.setClaimMemo("");
        List<Infos.GatePassLotInfo> gatePassLotInfos = new ArrayList<>();
        Infos.GatePassLotInfo gatePassLotInfo = new Infos.GatePassLotInfo();
        gatePassLotInfo.setCurrentOperationNumber(lotInfoCase.getLotInfoList().get(0).getLotOperationInfo().getOperationNumber());
        gatePassLotInfo.setCurrentRouteID(lotInfoCase.getLotInfoList().get(0).getLotOperationInfo().getRouteID());
        gatePassLotInfo.setLotID(lotID);
        gatePassLotInfos.add(gatePassLotInfo);
        passReqParams.setGatePassLotInfos(gatePassLotInfos);
        gatePassCase.passThruReq(passReqParams);

        //【step8】check trigger the psm record
        lotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();
        Validations.assertCheck(CimArrayUtils.getSize(lotInfoCase.getLotListInCassetteInfo().getLotIDList()) == 2,"trigger the added psm record and split a child lot");
    }


    private void modifyThePsmRecord(List<Infos.ExperimentalLotInfo> experimentalLotInfoList) {
        if (CimArrayUtils.isNotEmpty(experimentalLotInfoList)){
            planSplitMergeTestCase.modifyPsmRecord(experimentalLotInfoList);
        }
    }

    private void skipLotToMergePointAndMergeThem(ObjectIdentifier parentLot,ObjectIdentifier childLot,String mergePoint,String routeID){
        //【step1】skip the parent lot
        List<ObjectIdentifier> lotIDs = new ArrayList<>();
        lotIDs.add(parentLot);
        Results.LotInfoInqResult lotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();
        //【step1-1】 skip to the split point
        Infos.LotInfo lotInfo = lotInfoCase.getLotInfoList().get(0);
        Results.LotOperationSelectionInqResult lotOperationSelectionInqResult1 = (Results.LotOperationSelectionInqResult) lotGeneralTestCase.getLotOperationSelectionInq(parentLot, false, true, true).getBody();
        List<Infos.OperationNameAttributes> operationNameAttributesList1 = lotOperationSelectionInqResult1.getOperationNameAttributesAttributes().getContent();

        Infos.OperationNameAttributes operationNameAttributes1 = lotGeneralTestCase.getOperationAttributesByOperationNumberAndRouteID(operationNameAttributesList1,mergePoint,routeID);

        Params.SkipReqParams skipReqParams = new Params.SkipReqParams();
        skipReqParams.setCurrentOperationNumber(lotInfo.getLotOperationInfo().getOperationNumber());
        skipReqParams.setCurrentRouteID(lotInfo.getLotOperationInfo().getRouteID());
        skipReqParams.setLocateDirection(true);
        skipReqParams.setLotID(parentLot);
        skipReqParams.setOperationID(operationNameAttributes1.getOperationID());
        skipReqParams.setOperationNumber(operationNameAttributes1.getOperationNumber());
        skipReqParams.setProcessRef(operationNameAttributes1.getProcessRef());
        skipReqParams.setRouteID(operationNameAttributes1.getRouteID());
        skipReqParams.setSeqno(-1);
        skipReqParams.setSequenceNumber(0);
        operationSkipTestCase.operationSkip(skipReqParams);

        lotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();
        String lotStatusAction = lotInfoCase.getLotInfoList().get(0).getLotBasicInfo().getLotStatus();
        Validations.assertCheck(CimStringUtils.equals(lotStatusAction, BizConstant.SP_DURABLE_HOLDSTATE_ONHOLD),"the parent lot must be onHold");

        //【step2】skip the parent lot
        lotIDs = new ArrayList<>();
        lotIDs.add(childLot);
        lotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();
        //【step2-1】 skip to the split point
        lotInfo = lotInfoCase.getLotInfoList().get(0);
        lotOperationSelectionInqResult1 = (Results.LotOperationSelectionInqResult) lotGeneralTestCase.getLotOperationSelectionInq(childLot, false, true, true).getBody();
        operationNameAttributesList1 = lotOperationSelectionInqResult1.getOperationNameAttributesAttributes().getContent();

        operationNameAttributes1 = lotGeneralTestCase.getOperationAttributesByOperationNumberAndRouteID(operationNameAttributesList1,mergePoint,routeID);

        skipReqParams = new Params.SkipReqParams();
        skipReqParams.setCurrentOperationNumber(lotInfo.getLotOperationInfo().getOperationNumber());
        skipReqParams.setCurrentRouteID(lotInfo.getLotOperationInfo().getRouteID());
        skipReqParams.setLocateDirection(true);
        skipReqParams.setLotID(childLot);
        skipReqParams.setOperationID(operationNameAttributes1.getOperationID());
        skipReqParams.setOperationNumber(operationNameAttributes1.getOperationNumber());
        skipReqParams.setProcessRef(operationNameAttributes1.getProcessRef());
        skipReqParams.setRouteID(operationNameAttributes1.getRouteID());
        skipReqParams.setSeqno(-1);
        skipReqParams.setSequenceNumber(0);
        operationSkipTestCase.operationSkip(skipReqParams);

        lotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();
        lotStatusAction = lotInfoCase.getLotInfoList().get(0).getLotBasicInfo().getLotStatus();
        Validations.assertCheck(CimStringUtils.equals(lotStatusAction, BizConstant.SP_DURABLE_HOLDSTATE_ONHOLD),"the parent lot must be onHold");

        //【step3】merge parent lot and child lot
        lotMergeTestCase.mergeLotReq(parentLot,childLot);
    }

    private ObjectIdentifier planedSplitWithSplitPointAndReturnPointAndMergePointAndExitSkipSplitPoint(ObjectIdentifier backRouteID, ObjectIdentifier lotID, String splitOperationNum, String returnOperationNum, String mergeOperationNum, Boolean dynamicFlag,List<ObjectIdentifier> waferList){
        //【step1】lot info for lotID
        List<ObjectIdentifier> lotIDs = new ArrayList<>();
        lotIDs.add(lotID);
        Results.LotInfoInqResult firstLotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();

        //【step2】step the Engineering PSM Setup
        //【step2-1】psm list information
        Boolean detailRequireFlag = true;
        ObjectIdentifier familyLotID = firstLotInfoCase.getLotInfoList().get(0).getLotBasicInfo().getFamilyLotID();
        List<Infos.ExperimentalLotInfo> experimentalLotInfoList = (List<Infos.ExperimentalLotInfo>) planSplitMergeTestCase.experimentalLotList(familyLotID, detailRequireFlag).getBody();
        //【step2-2】get route operation nest list
        ObjectIdentifier routeID = firstLotInfoCase.getLotInfoList().get(0).getLotOperationInfo().getRouteID();
        if (CimObjectUtils.isNotEmpty(backRouteID) && !CimObjectUtils.equalsWithValue(backRouteID,routeID)){
            routeID = backRouteID;
        }
        Infos.RouteInfo routeInfo = (Infos.RouteInfo) electronicInformationTestCase.processFlowOpeListWithNestInqCase(routeID).getBody();

        //【step3】get subRouteID when in branch
        ObjectIdentifier subRouteID = planSplitMergeTestCase.getSubRouteID(routeInfo,splitOperationNum,routeID);
        TestInfos.PsmUpdateInfo psmUpdateInfo = new TestInfos.PsmUpdateInfo();
        psmUpdateInfo.setActionEMail(true);
        psmUpdateInfo.setActionHold(true);
        psmUpdateInfo.setDynamicFlag(dynamicFlag);
        psmUpdateInfo.setSplitOperationNumber(splitOperationNum);
        psmUpdateInfo.setSplitRouteID(routeID);
        psmUpdateInfo.setOriginalRouteID(routeID);
        psmUpdateInfo.setOriginalOperationNumber(splitOperationNum);
        psmUpdateInfo.setWaferList(waferList);
        psmUpdateInfo.setLotFamilyID(familyLotID);
        psmUpdateInfo.setReturnOperationNumber(returnOperationNum);
        psmUpdateInfo.setMergeOperationNumber(mergeOperationNum);
        psmUpdateInfo.setSubRouteID(subRouteID);

        //【step3-2】add psm
        planSplitMergeTestCase.psmLotInfoSetReqCase(psmUpdateInfo);

        //【step4】flush the psm page
        //【step4-1】lot info for lotID
        lotIDs.add(lotID);
        Results.LotInfoInqResult secondLotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();
        //【step4-2】psm list information
        familyLotID = secondLotInfoCase.getLotInfoList().get(0).getLotBasicInfo().getFamilyLotID();
        experimentalLotInfoList = (List<Infos.ExperimentalLotInfo>) planSplitMergeTestCase.experimentalLotList(familyLotID, detailRequireFlag).getBody();
        Validations.assertCheck(CimArrayUtils.getSize(experimentalLotInfoList) == 1,"psm list have one record");
        //【step4-3】get route operation nest list
        routeID = secondLotInfoCase.getLotInfoList().get(0).getLotOperationInfo().getRouteID();
        routeInfo = (Infos.RouteInfo) electronicInformationTestCase.processFlowOpeListWithNestInqCase(routeID).getBody();

        //【step5】step lot info for parent lot and skip to the split point
        secondLotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();
        //【step5-1】 skip to the split point
        Infos.LotInfo lotInfo = secondLotInfoCase.getLotInfoList().get(0);
        Results.LotOperationSelectionInqResult lotOperationSelectionInqResult1 = (Results.LotOperationSelectionInqResult) lotGeneralTestCase.getLotOperationSelectionInq(lotID, false, true, true).getBody();
        List<Infos.OperationNameAttributes> operationNameAttributesList1 = lotOperationSelectionInqResult1.getOperationNameAttributesAttributes().getContent();

        Infos.OperationNameAttributes operationNameAttributes1 = lotGeneralTestCase.getOperationAttributesByOperationNumber(operationNameAttributesList1,splitOperationNum);

        Params.SkipReqParams skipReqParams = new Params.SkipReqParams();
        skipReqParams.setCurrentOperationNumber(lotInfo.getLotOperationInfo().getOperationNumber());
        skipReqParams.setCurrentRouteID(lotInfo.getLotOperationInfo().getRouteID());
        skipReqParams.setLocateDirection(true);
        skipReqParams.setLotID(lotID);
        skipReqParams.setOperationID(operationNameAttributes1.getOperationID());
        skipReqParams.setOperationNumber(operationNameAttributes1.getOperationNumber());
        skipReqParams.setProcessRef(operationNameAttributes1.getProcessRef());
        skipReqParams.setRouteID(operationNameAttributes1.getRouteID());
        skipReqParams.setSeqno(-1);
        skipReqParams.setSequenceNumber(0);
        operationSkipTestCase.operationSkip(skipReqParams);

        //【step6】check lot info check if auto split a child lot
        secondLotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();
        ObjectIdentifier childLotID = null;
        for (ObjectIdentifier lotId : secondLotInfoCase.getLotListInCassetteInfo().getLotIDList()) {
            if (CimStringUtils.equals(lotID,lotId)){
                lotID = lotId;
            }else {
                childLotID = lotId;
            }
        }
        return childLotID;
    }

    private void planedSplitWithSplitPointAndReturnPointAndMergePoint(ObjectIdentifier backRouteID, ObjectIdentifier lotID, String splitOperationNum, String returnOperationNum, String mergeOperationNum, Boolean dynamicFlag,List<ObjectIdentifier> waferList){
        //【step1】lot info for lotID
        List<ObjectIdentifier> lotIDs = new ArrayList<>();
        lotIDs.add(lotID);
        Results.LotInfoInqResult firstLotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();

        //【step2】step the Engineering PSM Setup
        //【step2-1】psm list information
        Boolean detailRequireFlag = true;
        ObjectIdentifier familyLotID = firstLotInfoCase.getLotInfoList().get(0).getLotBasicInfo().getFamilyLotID();
        List<Infos.ExperimentalLotInfo> experimentalLotInfoList = (List<Infos.ExperimentalLotInfo>) planSplitMergeTestCase.experimentalLotList(familyLotID, detailRequireFlag).getBody();
        //【step2-2】get route operation nest list
        ObjectIdentifier routeID = firstLotInfoCase.getLotInfoList().get(0).getLotOperationInfo().getRouteID();
        if (CimObjectUtils.isNotEmpty(backRouteID) && !CimObjectUtils.equalsWithValue(backRouteID,routeID)){
            routeID = backRouteID;
        }
        Infos.RouteInfo routeInfo = (Infos.RouteInfo) electronicInformationTestCase.processFlowOpeListWithNestInqCase(routeID).getBody();

        //【step3】get subRouteID when in branch
        ObjectIdentifier subRouteID = planSplitMergeTestCase.getSubRouteID(routeInfo,splitOperationNum,routeID);
        TestInfos.PsmUpdateInfo psmUpdateInfo = new TestInfos.PsmUpdateInfo();
        psmUpdateInfo.setActionEMail(true);
        psmUpdateInfo.setActionHold(true);
        psmUpdateInfo.setDynamicFlag(dynamicFlag);
        psmUpdateInfo.setSplitOperationNumber(splitOperationNum);
        psmUpdateInfo.setSplitRouteID(routeID);
        psmUpdateInfo.setOriginalRouteID(routeID);
        psmUpdateInfo.setOriginalOperationNumber(splitOperationNum);
        psmUpdateInfo.setWaferList(waferList);
        psmUpdateInfo.setLotFamilyID(familyLotID);
        psmUpdateInfo.setReturnOperationNumber(returnOperationNum);
        psmUpdateInfo.setMergeOperationNumber(mergeOperationNum);
        psmUpdateInfo.setSubRouteID(subRouteID);

        //【step3-2】add psm
        planSplitMergeTestCase.psmLotInfoSetReqCase(psmUpdateInfo);

        //【step4】flush the psm page
        //【step4-1】lot info for lotID
        lotIDs.add(lotID);
        Results.LotInfoInqResult secondLotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();
        //【step4-2】psm list information
        familyLotID = secondLotInfoCase.getLotInfoList().get(0).getLotBasicInfo().getFamilyLotID();
        experimentalLotInfoList = (List<Infos.ExperimentalLotInfo>) planSplitMergeTestCase.experimentalLotList(familyLotID, detailRequireFlag).getBody();
        Validations.assertCheck(CimArrayUtils.getSize(experimentalLotInfoList) == 1,"psm list have one record");
        //【step4-3】get route operation nest list
        routeID = secondLotInfoCase.getLotInfoList().get(0).getLotOperationInfo().getRouteID();
        routeInfo = (Infos.RouteInfo) electronicInformationTestCase.processFlowOpeListWithNestInqCase(routeID).getBody();
    }

    private void planedSplitWithSplitPointAndReturnPointAndMergePointAction(ObjectIdentifier backRouteID, ObjectIdentifier lotID, String splitOperationNum, String returnOperationNum, String mergeOperationNum, Boolean dynamicFlag,Boolean actionFlag ,List<ObjectIdentifier> waferList){
        //【step1】lot info for lotID
        List<ObjectIdentifier> lotIDs = new ArrayList<>();
        lotIDs.add(lotID);
        Results.LotInfoInqResult firstLotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();

        //【step2】step the Engineering PSM Setup
        //【step2-1】psm list information
        Boolean detailRequireFlag = true;
        ObjectIdentifier familyLotID = firstLotInfoCase.getLotInfoList().get(0).getLotBasicInfo().getFamilyLotID();
        List<Infos.ExperimentalLotInfo> experimentalLotInfoList = (List<Infos.ExperimentalLotInfo>) planSplitMergeTestCase.experimentalLotList(familyLotID, detailRequireFlag).getBody();
        //【step2-2】get route operation nest list
        ObjectIdentifier routeID = firstLotInfoCase.getLotInfoList().get(0).getLotOperationInfo().getRouteID();
        if (CimObjectUtils.isNotEmpty(backRouteID) && !CimObjectUtils.equalsWithValue(backRouteID,routeID)){
            routeID = backRouteID;
        }
        Infos.RouteInfo routeInfo = (Infos.RouteInfo) electronicInformationTestCase.processFlowOpeListWithNestInqCase(routeID).getBody();

        //【step3】get subRouteID when in branch
        ObjectIdentifier subRouteID = planSplitMergeTestCase.getSubRouteID(routeInfo,splitOperationNum,routeID);
        TestInfos.PsmUpdateInfo psmUpdateInfo = new TestInfos.PsmUpdateInfo();
        psmUpdateInfo.setActionEMail(actionFlag);
        psmUpdateInfo.setActionHold(actionFlag);
        psmUpdateInfo.setDynamicFlag(dynamicFlag);
        psmUpdateInfo.setSplitOperationNumber(splitOperationNum);
        psmUpdateInfo.setSplitRouteID(routeID);
        psmUpdateInfo.setOriginalRouteID(routeID);
        psmUpdateInfo.setOriginalOperationNumber(splitOperationNum);
        psmUpdateInfo.setWaferList(waferList);
        psmUpdateInfo.setLotFamilyID(familyLotID);
        psmUpdateInfo.setReturnOperationNumber(returnOperationNum);
        psmUpdateInfo.setMergeOperationNumber(mergeOperationNum);
        psmUpdateInfo.setSubRouteID(subRouteID);

        //【step3-2】add psm
        planSplitMergeTestCase.psmLotInfoSetReqCase(psmUpdateInfo);

        //【step4】flush the psm page
        //【step4-1】lot info for lotID
        lotIDs.add(lotID);
        Results.LotInfoInqResult secondLotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();
        //【step4-2】psm list information
        familyLotID = secondLotInfoCase.getLotInfoList().get(0).getLotBasicInfo().getFamilyLotID();
        experimentalLotInfoList = (List<Infos.ExperimentalLotInfo>) planSplitMergeTestCase.experimentalLotList(familyLotID, detailRequireFlag).getBody();
        Validations.assertCheck(CimArrayUtils.getSize(experimentalLotInfoList) == 1,"psm list have one record");
        //【step4-3】get route operation nest list
        routeID = secondLotInfoCase.getLotInfoList().get(0).getLotOperationInfo().getRouteID();
        routeInfo = (Infos.RouteInfo) electronicInformationTestCase.processFlowOpeListWithNestInqCase(routeID).getBody();
    }

    private void planedSplitWithSplitPointAndReturnPointAndMergePointForNextBranch(ObjectIdentifier newRouteID, ObjectIdentifier backRouteID,ObjectIdentifier lotID, String splitOperationNum, String backSplitOperationNum,String returnOperationNum, String mergeOperationNum, Boolean dynamicFlag,List<ObjectIdentifier> waferList){
        //【step1】lot info for lotID
        List<ObjectIdentifier> lotIDs = new ArrayList<>();
        lotIDs.add(lotID);
        Results.LotInfoInqResult firstLotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();

        //【step2】step the Engineering PSM Setup
        //【step2-1】psm list information
        Boolean detailRequireFlag = true;
        ObjectIdentifier familyLotID = firstLotInfoCase.getLotInfoList().get(0).getLotBasicInfo().getFamilyLotID();
        List<Infos.ExperimentalLotInfo> experimentalLotInfoList = (List<Infos.ExperimentalLotInfo>) planSplitMergeTestCase.experimentalLotList(familyLotID, detailRequireFlag).getBody();
        //【step2-2】get route operation nest list

        Infos.RouteInfo routeInfo = (Infos.RouteInfo) electronicInformationTestCase.processFlowOpeListWithNestInqCase(newRouteID).getBody();

        //【step3】get subRouteID when in branch
        ObjectIdentifier subRouteID = planSplitMergeTestCase.getSubRouteID(routeInfo,splitOperationNum,newRouteID);
        TestInfos.PsmUpdateInfo psmUpdateInfo = new TestInfos.PsmUpdateInfo();
        psmUpdateInfo.setActionEMail(false);
        psmUpdateInfo.setActionHold(false);
        psmUpdateInfo.setDynamicFlag(dynamicFlag);

        psmUpdateInfo.setSplitOperationNumber(splitOperationNum);
        psmUpdateInfo.setSplitRouteID(newRouteID);

        psmUpdateInfo.setOriginalRouteID(backRouteID);
        psmUpdateInfo.setOriginalOperationNumber(backSplitOperationNum);
        psmUpdateInfo.setWaferList(waferList);
        psmUpdateInfo.setLotFamilyID(familyLotID);
        psmUpdateInfo.setReturnOperationNumber(returnOperationNum);
        psmUpdateInfo.setMergeOperationNumber(mergeOperationNum);
        psmUpdateInfo.setSubRouteID(subRouteID);

        //【step3-2】add psm
        planSplitMergeTestCase.psmLotInfoSetReqCase(psmUpdateInfo);

        //【step4】flush the psm page
        //【step4-1】lot info for lotID
        lotIDs.add(lotID);
        Results.LotInfoInqResult secondLotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();
        //【step4-2】psm list information
        familyLotID = secondLotInfoCase.getLotInfoList().get(0).getLotBasicInfo().getFamilyLotID();
        experimentalLotInfoList = (List<Infos.ExperimentalLotInfo>) planSplitMergeTestCase.experimentalLotList(familyLotID, detailRequireFlag).getBody();
        //【step4-3】get route operation nest list
        newRouteID = secondLotInfoCase.getLotInfoList().get(0).getLotOperationInfo().getRouteID();
        routeInfo = (Infos.RouteInfo) electronicInformationTestCase.processFlowOpeListWithNestInqCase(newRouteID).getBody();
    }

    private void planedSplitWithSplitPointAndReturnPointAndMergePointAndExecSkipAndMerge(ObjectIdentifier backRouteID, ObjectIdentifier lotID, String splitOperationNum, String returnOperationNum, String mergeOperationNum, Boolean dynamicFlag,List<ObjectIdentifier> waferList){
        //【step1】lot info for lotID
        List<ObjectIdentifier> lotIDs = new ArrayList<>();
        lotIDs.add(lotID);
        Results.LotInfoInqResult firstLotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();

        //【step2】step the Engineering PSM Setup
        //【step2-1】psm list information
        Boolean detailRequireFlag = true;
        ObjectIdentifier familyLotID = firstLotInfoCase.getLotInfoList().get(0).getLotBasicInfo().getFamilyLotID();
        List<Infos.ExperimentalLotInfo> experimentalLotInfoList = (List<Infos.ExperimentalLotInfo>) planSplitMergeTestCase.experimentalLotList(familyLotID, detailRequireFlag).getBody();
        //【step2-2】get route operation nest list
        ObjectIdentifier routeID = firstLotInfoCase.getLotInfoList().get(0).getLotOperationInfo().getRouteID();
        if (CimObjectUtils.isNotEmpty(backRouteID) && !CimObjectUtils.equalsWithValue(backRouteID,routeID)){
            routeID = backRouteID;
        }
        Infos.RouteInfo routeInfo = (Infos.RouteInfo) electronicInformationTestCase.processFlowOpeListWithNestInqCase(routeID).getBody();

        //【step3】get subRouteID when in branch
        ObjectIdentifier subRouteID = planSplitMergeTestCase.getSubRouteID(routeInfo,splitOperationNum,routeID);
        TestInfos.PsmUpdateInfo psmUpdateInfo = new TestInfos.PsmUpdateInfo();
        psmUpdateInfo.setActionEMail(true);
        psmUpdateInfo.setActionHold(true);
        psmUpdateInfo.setDynamicFlag(dynamicFlag);
        psmUpdateInfo.setSplitOperationNumber(splitOperationNum);
        psmUpdateInfo.setSplitRouteID(routeID);
        psmUpdateInfo.setOriginalRouteID(routeID);
        psmUpdateInfo.setOriginalOperationNumber(splitOperationNum);
        psmUpdateInfo.setWaferList(waferList);
        psmUpdateInfo.setLotFamilyID(familyLotID);
        psmUpdateInfo.setReturnOperationNumber(returnOperationNum);
        psmUpdateInfo.setMergeOperationNumber(mergeOperationNum);
        psmUpdateInfo.setSubRouteID(subRouteID);

        //【step3-2】add psm
        planSplitMergeTestCase.psmLotInfoSetReqCase(psmUpdateInfo);

        //【step4】flush the psm page
        //【step4-1】lot info for lotID
        lotIDs.add(lotID);
        Results.LotInfoInqResult secondLotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();
        //【step4-2】psm list information
        familyLotID = secondLotInfoCase.getLotInfoList().get(0).getLotBasicInfo().getFamilyLotID();
        experimentalLotInfoList = (List<Infos.ExperimentalLotInfo>) planSplitMergeTestCase.experimentalLotList(familyLotID, detailRequireFlag).getBody();
        Validations.assertCheck(CimArrayUtils.getSize(experimentalLotInfoList) == 1,"psm list have one record");
        //【step4-3】get route operation nest list
        routeID = secondLotInfoCase.getLotInfoList().get(0).getLotOperationInfo().getRouteID();
        routeInfo = (Infos.RouteInfo) electronicInformationTestCase.processFlowOpeListWithNestInqCase(routeID).getBody();

        //【step5】step lot info for parent lot and skip to the split point
        secondLotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();
        //【step5-1】 skip to the split point
        Infos.LotInfo lotInfo = secondLotInfoCase.getLotInfoList().get(0);
        Results.LotOperationSelectionInqResult lotOperationSelectionInqResult1 = (Results.LotOperationSelectionInqResult) lotGeneralTestCase.getLotOperationSelectionInq(lotID, false, true, true).getBody();
        List<Infos.OperationNameAttributes> operationNameAttributesList1 = lotOperationSelectionInqResult1.getOperationNameAttributesAttributes().getContent();

        Infos.OperationNameAttributes operationNameAttributes1 = lotGeneralTestCase.getOperationAttributesByOperationNumber(operationNameAttributesList1,splitOperationNum);

        Params.SkipReqParams skipReqParams = new Params.SkipReqParams();
        skipReqParams.setCurrentOperationNumber(lotInfo.getLotOperationInfo().getOperationNumber());
        skipReqParams.setCurrentRouteID(lotInfo.getLotOperationInfo().getRouteID());
        skipReqParams.setLocateDirection(true);
        skipReqParams.setLotID(lotID);
        skipReqParams.setOperationID(operationNameAttributes1.getOperationID());
        skipReqParams.setOperationNumber(operationNameAttributes1.getOperationNumber());
        skipReqParams.setProcessRef(operationNameAttributes1.getProcessRef());
        skipReqParams.setRouteID(operationNameAttributes1.getRouteID());
        skipReqParams.setSeqno(-1);
        skipReqParams.setSequenceNumber(0);
        operationSkipTestCase.operationSkip(skipReqParams);

        //【step6】check lot info check if auto split a child lot
        secondLotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();
        Validations.assertCheck(CimArrayUtils.getSize(secondLotInfoCase.getLotListInCassetteInfo().getLotIDList()) == 2,"parent lot split must have a child lot");
        ObjectIdentifier childLotID = null;
        for (ObjectIdentifier lotId : secondLotInfoCase.getLotListInCassetteInfo().getLotIDList()) {
            if (CimStringUtils.equals(lotID,lotId)){
                lotID = lotId;
            }else {
                childLotID = lotId;
            }
        }
        //【step6-1】check action hold for parent lot and child lot;
        String lotStatusAction = secondLotInfoCase.getLotInfoList().get(0).getLotBasicInfo().getLotStatus();
        Validations.assertCheck(CimStringUtils.equals(lotStatusAction, BizConstant.SP_DURABLE_HOLDSTATE_ONHOLD),"the parent lot must be onHold");

        lotIDs = new ArrayList<>();
        lotIDs.add(childLotID);
        Results.LotInfoInqResult LotInfoCase1 = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();
        lotStatusAction = LotInfoCase1.getLotInfoList().get(0).getLotBasicInfo().getLotStatus();
        Validations.assertCheck(CimStringUtils.equals(lotStatusAction, BizConstant.SP_DURABLE_HOLDSTATE_ONHOLD),"the child lot must be onHold");

        //【step6-2】lotHold release PSMH
        lotHoldTestCase.lotHoldReleaseReqCase(lotID);
        lotHoldTestCase.lotHoldReleaseReqCase(childLotID);


        //【step7】skip the parent lot to the merge point
        lotIDs = new ArrayList<>();
        lotIDs.add(lotID);
        secondLotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();
        lotInfo = secondLotInfoCase.getLotInfoList().get(0);
        lotOperationSelectionInqResult1 = (Results.LotOperationSelectionInqResult) lotGeneralTestCase.getLotOperationSelectionInq(lotID, false, true, true).getBody();
        operationNameAttributesList1 = lotOperationSelectionInqResult1.getOperationNameAttributesAttributes().getContent();

        Infos.OperationNameAttributes operationNameAttributes2 = lotGeneralTestCase.getOperationAttributesByOperationNumber(operationNameAttributesList1,mergeOperationNum);

        skipReqParams = new Params.SkipReqParams();
        skipReqParams.setCurrentOperationNumber(lotInfo.getLotOperationInfo().getOperationNumber());
        skipReqParams.setCurrentRouteID(lotInfo.getLotOperationInfo().getRouteID());
        skipReqParams.setLocateDirection(true);
        skipReqParams.setLotID(lotID);
        skipReqParams.setOperationID(operationNameAttributes2.getOperationID());
        skipReqParams.setOperationNumber(operationNameAttributes2.getOperationNumber());
        skipReqParams.setProcessRef(operationNameAttributes2.getProcessRef());
        skipReqParams.setRouteID(operationNameAttributes2.getRouteID());
        skipReqParams.setSeqno(-1);
        skipReqParams.setSequenceNumber(0);
        operationSkipTestCase.operationSkip(skipReqParams);

        //【step8】check the parent lot is onHold
        Results.LotInfoInqResult productInfoCaseLast = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();
        String lotStatus = productInfoCaseLast.getLotInfoList().get(0).getLotBasicInfo().getLotStatus();
        Validations.assertCheck(CimStringUtils.equals(lotStatus, BizConstant.SP_DURABLE_HOLDSTATE_ONHOLD),"the parent lot must be onHold");

        //【step9】skip the child lot to the merge point
        lotIDs = new ArrayList<>();
        lotIDs.add(childLotID);
        Results.LotInfoInqResult childLotInfoCase = (Results.LotInfoInqResult) lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();

        lotInfo = childLotInfoCase.getLotInfoList().get(0);
        Results.LotOperationSelectionInqResult lotOperationSelectionInqResult3 = (Results.LotOperationSelectionInqResult) lotGeneralTestCase.getLotOperationSelectionInq(childLotID, false, true, true).getBody();
        List<Infos.OperationNameAttributes> operationNameAttributesList3 = lotOperationSelectionInqResult3.getOperationNameAttributesAttributes().getContent();

        Infos.OperationNameAttributes operationNameAttributes3 = lotGeneralTestCase.getOperationAttributesByOperationNumber(operationNameAttributesList3,mergeOperationNum);

        skipReqParams = new Params.SkipReqParams();
        skipReqParams.setCurrentOperationNumber(lotInfo.getLotOperationInfo().getOperationNumber());
        skipReqParams.setCurrentRouteID(lotInfo.getLotOperationInfo().getRouteID());
        skipReqParams.setLocateDirection(true);
        skipReqParams.setLotID(childLotID);
        skipReqParams.setOperationID(operationNameAttributes3.getOperationID());
        skipReqParams.setOperationNumber(operationNameAttributes3.getOperationNumber());
        skipReqParams.setProcessRef(operationNameAttributes3.getProcessRef());
        skipReqParams.setRouteID(operationNameAttributes3.getRouteID());
        skipReqParams.setSeqno(-1);
        skipReqParams.setSequenceNumber(0);
        operationSkipTestCase.operationSkip(skipReqParams);

        //【step10】check the child lot is onHold
        productInfoCaseLast = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();
        lotStatus = productInfoCaseLast.getLotInfoList().get(0).getLotBasicInfo().getLotStatus();
        Validations.assertCheck(CimStringUtils.equals(lotStatus, BizConstant.SP_DURABLE_HOLDSTATE_ONHOLD),"the child lot must be onHold");

        //【step11】merge the parent lot and child lot
        lotMergeTestCase.mergeLotReq(lotID, childLotID);

        //【step12】check the merge is sucess
        lotIDs = new ArrayList<>();
        lotIDs.add(lotID);
        secondLotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();
        Validations.assertCheck(CimArrayUtils.getSize(secondLotInfoCase.getLotListInCassetteInfo().getLotIDList()) == 1,"parent lot merge must have not child lot");

    }

    private ObjectIdentifier planedSplitWithSplitPointAndReturnPointAndMergePointAndExitSkipSplitPointAndSkipMergePoint(ObjectIdentifier backRouteID, ObjectIdentifier lotID, String splitOperationNum, String returnOperationNum, String mergeOperationNum, Boolean dynamicFlag,List<ObjectIdentifier> waferList){
        //【step1】lot info for lotID
        List<ObjectIdentifier> lotIDs = new ArrayList<>();
        lotIDs.add(lotID);
        Results.LotInfoInqResult firstLotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();

        //【step2】step the Engineering PSM Setup
        //【step2-1】psm list information
        Boolean detailRequireFlag = true;
        ObjectIdentifier familyLotID = firstLotInfoCase.getLotInfoList().get(0).getLotBasicInfo().getFamilyLotID();
        List<Infos.ExperimentalLotInfo> experimentalLotInfoList = (List<Infos.ExperimentalLotInfo>) planSplitMergeTestCase.experimentalLotList(familyLotID, detailRequireFlag).getBody();
        //【step2-2】get route operation nest list
        ObjectIdentifier routeID = firstLotInfoCase.getLotInfoList().get(0).getLotOperationInfo().getRouteID();
        if (CimObjectUtils.isNotEmpty(backRouteID) && !CimObjectUtils.equalsWithValue(backRouteID,routeID)){
            routeID = backRouteID;
        }
        Infos.RouteInfo routeInfo = (Infos.RouteInfo) electronicInformationTestCase.processFlowOpeListWithNestInqCase(routeID).getBody();

        //【step3】get subRouteID when in branch
        ObjectIdentifier subRouteID = planSplitMergeTestCase.getSubRouteID(routeInfo,splitOperationNum,routeID);
        TestInfos.PsmUpdateInfo psmUpdateInfo = new TestInfos.PsmUpdateInfo();
        psmUpdateInfo.setActionEMail(true);
        psmUpdateInfo.setActionHold(true);
        psmUpdateInfo.setDynamicFlag(dynamicFlag);
        psmUpdateInfo.setSplitOperationNumber(splitOperationNum);
        psmUpdateInfo.setSplitRouteID(routeID);
        psmUpdateInfo.setOriginalRouteID(routeID);
        psmUpdateInfo.setOriginalOperationNumber(splitOperationNum);
        psmUpdateInfo.setWaferList(waferList);
        psmUpdateInfo.setLotFamilyID(familyLotID);
        psmUpdateInfo.setReturnOperationNumber(returnOperationNum);
        psmUpdateInfo.setMergeOperationNumber(mergeOperationNum);
        psmUpdateInfo.setSubRouteID(subRouteID);

        //【step3-2】add psm
        planSplitMergeTestCase.psmLotInfoSetReqCase(psmUpdateInfo);

        //【step4】flush the psm page
        //【step4-1】lot info for lotID
        lotIDs.add(lotID);
        Results.LotInfoInqResult secondLotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();
        //【step4-2】psm list information
        familyLotID = secondLotInfoCase.getLotInfoList().get(0).getLotBasicInfo().getFamilyLotID();
        experimentalLotInfoList = (List<Infos.ExperimentalLotInfo>) planSplitMergeTestCase.experimentalLotList(familyLotID, detailRequireFlag).getBody();
        Validations.assertCheck(CimArrayUtils.getSize(experimentalLotInfoList) == 1,"psm list have one record");
        //【step4-3】get route operation nest list
        routeID = secondLotInfoCase.getLotInfoList().get(0).getLotOperationInfo().getRouteID();
        routeInfo = (Infos.RouteInfo) electronicInformationTestCase.processFlowOpeListWithNestInqCase(routeID).getBody();

        //【step5】step lot info for parent lot and skip to the split point
        secondLotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();
        //【step5-1】 skip to the split point
        Infos.LotInfo lotInfo = secondLotInfoCase.getLotInfoList().get(0);
        Results.LotOperationSelectionInqResult lotOperationSelectionInqResult1 = (Results.LotOperationSelectionInqResult) lotGeneralTestCase.getLotOperationSelectionInq(lotID, false, true, true).getBody();
        List<Infos.OperationNameAttributes> operationNameAttributesList1 = lotOperationSelectionInqResult1.getOperationNameAttributesAttributes().getContent();

        Infos.OperationNameAttributes operationNameAttributes1 = lotGeneralTestCase.getOperationAttributesByOperationNumber(operationNameAttributesList1,splitOperationNum);

        Params.SkipReqParams skipReqParams = new Params.SkipReqParams();
        skipReqParams.setCurrentOperationNumber(lotInfo.getLotOperationInfo().getOperationNumber());
        skipReqParams.setCurrentRouteID(lotInfo.getLotOperationInfo().getRouteID());
        skipReqParams.setLocateDirection(true);
        skipReqParams.setLotID(lotID);
        skipReqParams.setOperationID(operationNameAttributes1.getOperationID());
        skipReqParams.setOperationNumber(operationNameAttributes1.getOperationNumber());
        skipReqParams.setProcessRef(operationNameAttributes1.getProcessRef());
        skipReqParams.setRouteID(operationNameAttributes1.getRouteID());
        skipReqParams.setSeqno(-1);
        skipReqParams.setSequenceNumber(0);
        operationSkipTestCase.operationSkip(skipReqParams);

        //【step6】check lot info check if auto split a child lot
        secondLotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();
        Validations.assertCheck(CimArrayUtils.getSize(secondLotInfoCase.getLotListInCassetteInfo().getLotIDList()) == 2,"parent lot split must have a child lot");
        ObjectIdentifier childLotID = null;
        for (ObjectIdentifier lotId : secondLotInfoCase.getLotListInCassetteInfo().getLotIDList()) {
            if (CimStringUtils.equals(lotID,lotId)){
                lotID = lotId;
            }else {
                childLotID = lotId;
            }
        }
        //【step6-1】check action hold for parent lot and child lot;
        String lotStatusAction = secondLotInfoCase.getLotInfoList().get(0).getLotBasicInfo().getLotStatus();
        Validations.assertCheck(CimStringUtils.equals(lotStatusAction, BizConstant.SP_DURABLE_HOLDSTATE_ONHOLD),"the parent lot must be onHold");

        lotIDs = new ArrayList<>();
        lotIDs.add(childLotID);
        Results.LotInfoInqResult LotInfoCase1 = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();
        lotStatusAction = LotInfoCase1.getLotInfoList().get(0).getLotBasicInfo().getLotStatus();
        Validations.assertCheck(CimStringUtils.equals(lotStatusAction, BizConstant.SP_DURABLE_HOLDSTATE_ONHOLD),"the child lot must be onHold");

        //【step6-2】lotHold release PSMH
        lotHoldTestCase.lotHoldReleaseReqCase(lotID);
        lotHoldTestCase.lotHoldReleaseReqCase(childLotID);


        //【step7】skip the parent lot to the merge point
        lotIDs = new ArrayList<>();
        lotIDs.add(lotID);
        secondLotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();
        lotInfo = secondLotInfoCase.getLotInfoList().get(0);
        lotOperationSelectionInqResult1 = (Results.LotOperationSelectionInqResult) lotGeneralTestCase.getLotOperationSelectionInq(lotID, false, true, true).getBody();
        operationNameAttributesList1 = lotOperationSelectionInqResult1.getOperationNameAttributesAttributes().getContent();

        Infos.OperationNameAttributes operationNameAttributes2 = lotGeneralTestCase.getOperationAttributesByOperationNumber(operationNameAttributesList1,mergeOperationNum);

        skipReqParams = new Params.SkipReqParams();
        skipReqParams.setCurrentOperationNumber(lotInfo.getLotOperationInfo().getOperationNumber());
        skipReqParams.setCurrentRouteID(lotInfo.getLotOperationInfo().getRouteID());
        skipReqParams.setLocateDirection(true);
        skipReqParams.setLotID(lotID);
        skipReqParams.setOperationID(operationNameAttributes2.getOperationID());
        skipReqParams.setOperationNumber(operationNameAttributes2.getOperationNumber());
        skipReqParams.setProcessRef(operationNameAttributes2.getProcessRef());
        skipReqParams.setRouteID(operationNameAttributes2.getRouteID());
        skipReqParams.setSeqno(-1);
        skipReqParams.setSequenceNumber(0);
        operationSkipTestCase.operationSkip(skipReqParams);

        //【step8】check the parent lot is onHold
        Results.LotInfoInqResult productInfoCaseLast = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();
        String lotStatus = productInfoCaseLast.getLotInfoList().get(0).getLotBasicInfo().getLotStatus();
        Validations.assertCheck(CimStringUtils.equals(lotStatus, BizConstant.SP_DURABLE_HOLDSTATE_ONHOLD),"the parent lot must be onHold");

        //【step9】skip the child lot to the merge point
        lotIDs = new ArrayList<>();
        lotIDs.add(childLotID);
        Results.LotInfoInqResult childLotInfoCase = (Results.LotInfoInqResult) lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();

        lotInfo = childLotInfoCase.getLotInfoList().get(0);
        Results.LotOperationSelectionInqResult lotOperationSelectionInqResult3 = (Results.LotOperationSelectionInqResult) lotGeneralTestCase.getLotOperationSelectionInq(childLotID, false, true, true).getBody();
        List<Infos.OperationNameAttributes> operationNameAttributesList3 = lotOperationSelectionInqResult3.getOperationNameAttributesAttributes().getContent();

        Infos.OperationNameAttributes operationNameAttributes3 = lotGeneralTestCase.getOperationAttributesByOperationNumber(operationNameAttributesList3,mergeOperationNum);

        skipReqParams = new Params.SkipReqParams();
        skipReqParams.setCurrentOperationNumber(lotInfo.getLotOperationInfo().getOperationNumber());
        skipReqParams.setCurrentRouteID(lotInfo.getLotOperationInfo().getRouteID());
        skipReqParams.setLocateDirection(true);
        skipReqParams.setLotID(childLotID);
        skipReqParams.setOperationID(operationNameAttributes3.getOperationID());
        skipReqParams.setOperationNumber(operationNameAttributes3.getOperationNumber());
        skipReqParams.setProcessRef(operationNameAttributes3.getProcessRef());
        skipReqParams.setRouteID(operationNameAttributes3.getRouteID());
        skipReqParams.setSeqno(-1);
        skipReqParams.setSequenceNumber(0);
        operationSkipTestCase.operationSkip(skipReqParams);

        //【step10】check the child lot is onHold
        productInfoCaseLast = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(lotIDs).getBody();
        lotStatus = productInfoCaseLast.getLotInfoList().get(0).getLotBasicInfo().getLotStatus();
        Validations.assertCheck(CimStringUtils.equals(lotStatus, BizConstant.SP_DURABLE_HOLDSTATE_ONHOLD),"the child lot must be onHold");
        return childLotID;
    }

    public void A_Lot_Was_Moved_To_A_Step_Which_Has_PSM_Using_Schedule_Change_SPS() {
        //【step1】stb
        String bankID = "BNK-0S";
        String sourceProductID = "RAW-2000.01";
        String productID = "PRODUCT0.01";
        TestInfos.StbInfo stbInfo = new TestInfos.StbInfo(bankID, sourceProductID, productID);
        ObjectIdentifier lotID = stbCase.STB_Normal(stbInfo);

        //【step2】set psm at 2000.0400;
        String splitOperationNum = "2000.0400";
        String returnOperationNum = "2000.0200";
        String mergeOperationNum = "2000.0500";
        Boolean dynamicFlag = false;

        Results.LotInfoInqResult lotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(Arrays.asList(lotID)).getBody();
        ObjectIdentifier routeID = lotInfoCase.getLotInfoList().get(0).getLotOperationInfo().getRouteID();
        ObjectIdentifier familyLotID = lotInfoCase.getLotInfoList().get(0).getLotBasicInfo().getFamilyLotID();
        List<Infos.WaferListInLotFamilyInfo> waferListInLotFamilyInfos = (List<Infos.WaferListInLotFamilyInfo>) lotGeneralTestCase.waferListInLotFamilyInfoInqCase(familyLotID).getBody();
        List<ObjectIdentifier> waferList = new ArrayList<>();
        //wafer 1,2,3
        waferList.add(waferListInLotFamilyInfos.get(0).getWaferID());
        waferList.add(waferListInLotFamilyInfos.get(1).getWaferID());
        waferList.add(waferListInLotFamilyInfos.get(2).getWaferID());

        this.planedSplitWithSplitPointAndReturnPointAndMergePoint(null, lotID,splitOperationNum,returnOperationNum,mergeOperationNum,dynamicFlag,waferList);
        Boolean detailRequireFlag = true;
        List<Infos.ExperimentalLotInfo> experimentalLotInfoList = (List<Infos.ExperimentalLotInfo>) planSplitMergeTestCase.experimentalLotList(familyLotID, detailRequireFlag).getBody();
        Validations.assertCheck(CimArrayUtils.getSize(experimentalLotInfoList) == 1,"modify the psm list must have one");

        //【step3】set schedule change and skip to 2000.0400
        String manufacturingLayer = "A";
        String lotStatus = "WIP";
        String selectProcessFlowID = "LAYER0MA.01";
        String selectOperationNum = "2000.0400";
        lotScheduleCase.lotSchdlChangeByParameter(routeID,lotID,lotStatus,ObjectIdentifier.buildWithValue(productID),manufacturingLayer,
                false,true,false,selectProcessFlowID,null,null,selectOperationNum);

        //【step4】check lot information and trigger psm
        lotInfoCase = (Results.LotInfoInqResult)lotGeneralTestCase.getLotInfoCase(Arrays.asList(lotID)).getBody();
        List<ObjectIdentifier> lotIDList = lotInfoCase.getLotListInCassetteInfo().getLotIDList();
        Validations.assertCheck(CimArrayUtils.getSize(lotIDList) == 2,"parent lot must split a child lot from psm");
    }
}
