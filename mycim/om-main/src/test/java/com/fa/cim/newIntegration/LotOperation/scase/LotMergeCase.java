package com.fa.cim.newIntegration.LotOperation.scase;

import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.Response;
import com.fa.cim.common.utils.CimArrayUtils;
import com.fa.cim.common.utils.CimObjectUtils;
import com.fa.cim.common.utils.CimStringUtils;
import com.fa.cim.common.utils.Validations;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.controller.einfo.ElectronicInformationController;
import com.fa.cim.controller.einfo.ElectronicInformationInqController;
import com.fa.cim.controller.lot.LotController;
import com.fa.cim.controller.processmonitor.ProcessMonitorController;
import com.fa.cim.controller.processmonitor.ProcessMonitorInqController;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;
import com.fa.cim.newIntegration.bank.scase.VendorLotPrepareCase;
import com.fa.cim.newIntegration.bank.scase.VendorLotReceiveCase;
import com.fa.cim.newIntegration.common.TestUtils;
import com.fa.cim.newIntegration.dto.TestInfos;
import com.fa.cim.newIntegration.equipment.scase.LotLoadToEquipmentCase;
import com.fa.cim.newIntegration.equipment.scase.MoveInCase;
import com.fa.cim.newIntegration.stb.scase.STBCase;
import com.fa.cim.newIntegration.tcase.*;
import com.fa.cim.utils.GenerateVendorlot;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/9/12       ********             lightyh             create file
 *
 * @author: lightyh
 * @date: 2019/9/12 9:47
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Service
@Slf4j
public class LotMergeCase {

    @Autowired
    private LotSplitCase lotSplitCase;

    @Autowired
    private LotGeneralTestCase lotGeneralTestCase;

    @Autowired
    private LotMergeTestCase lotMergeTestCase;

    @Autowired
    private TestUtils testUtils;

    @Autowired
    private LotController lotController;

    @Autowired
    private STBCase stbCase;

    @Autowired
    private RetCodeConfig retCodeConfig;

    @Autowired
    private LotHoldCase lotHoldCase;

    @Autowired
    private VendorLotReceiveCase vendorLotReceiveCase;

    @Autowired
    private VendorLotPrepareCase vendorLotPrepareCase;

    @Autowired
    private ProcessFlowTestCase processFlowTestCase;

    @Autowired
    private LotSubRouteBranchTestCase lotSubRouteBranchTestCase;

    @Autowired
    private LotLoadToEquipmentCase lotLoadToEquipmentCase;

    @Autowired
    private MoveInCase moveInCase;

    @Autowired
    private LotMergeCase lotMergeCase;

    @Autowired
    private FutureHoldTestCase futureHoldTestCase;

    @Autowired
    private LotNoteCase lotNoteCase;

    @Autowired
    private ProcessMonitorController processMonitorController;

    @Autowired
    private ProcessMonitorInqController processMonitorInqController;

    @Autowired
    private ElectronicInformationInqController electronicInformationInqController;

    @Autowired
    private ElectronicInformationController electronicInformationController;

    public Response merge_norm(ObjectIdentifier parentLotID, ObjectIdentifier childLotID) {
        Params.MergeLotReqParams mergeLotReqParams = new Params.MergeLotReqParams();
        mergeLotReqParams.setParentLotID(parentLotID);
        mergeLotReqParams.setChildLotID(childLotID);
        mergeLotReqParams.setUser(testUtils.getUser());
        return lotController.mergeLotReq(mergeLotReqParams);
    }
    public void mergeWithoutMergePoint(){
        //【step1】split with out merge point
        //【step1-1】stb one product lot
        String bankID = "BNK-0S";
        String sourceProductID = "RAW-2000.01";
        String productID = "PRODUCT0.01";
        TestInfos.StbInfo stbInfo = new TestInfos.StbInfo(bankID, sourceProductID, productID, 25L, false);
        ObjectIdentifier lotID = stbCase.STB_Normal(stbInfo);

        //【step1-2】set child wafer size
        TestInfos.SplitInfo splitInfo = new TestInfos.SplitInfo();
        splitInfo.setLotID(lotID);
        splitInfo.setChildLotWaferSize(3);
        ObjectIdentifier childLotID = lotSplitCase.split_norm(splitInfo);

        //【step2】merge
        lotMergeTestCase.mergeLotReq(lotID, childLotID);

        //【step3】check the result
        List<ObjectIdentifier> lotIDList = new ArrayList<>();
        lotIDList.add(lotID);
        Results.LotInfoInqResult lotInfoInqResult3 = (Results.LotInfoInqResult) lotGeneralTestCase.getLotInfoCase(lotIDList).getBody();
        List<ObjectIdentifier> lotIDInCassetteList3 = lotInfoInqResult3.getLotListInCassetteInfo().getLotIDList();
        Assert.isTrue(lotIDInCassetteList3.size() == 1 ,"the lot count in cassette must be 1 after merge");
    }

    public void mergeWithMergePoint(){
        //【step1】split with out merge point
        //【step1-1】stb one product lot
        String bankID = "BNK-0S";
        String sourceProductID = "RAW-2000.01";
        String productID = "PRODUCT0.01";
        TestInfos.StbInfo stbInfo = new TestInfos.StbInfo(bankID, sourceProductID, productID, 25L, false);
        ObjectIdentifier lotID = stbCase.STB_Normal(stbInfo);

        //【step1-2】set child wafer size
        TestInfos.SplitInfo splitInfo = new TestInfos.SplitInfo();
        splitInfo.setLotID(lotID);
        splitInfo.setChildLotWaferSize(3);

        //【step1-3】set merge point info
        TestInfos.SplitMergePointInfo splitMergePointInfo = new TestInfos.SplitMergePointInfo("1000.0100", new ObjectIdentifier("LAYER0MA.01"));
        splitInfo.setSplitMergePointInfo(splitMergePointInfo);
        ObjectIdentifier childLotID = lotSplitCase.split_norm(splitInfo);

        //【step2】merge
        lotMergeTestCase.mergeLotReq(lotID, childLotID);

        //【step3】check the result
        List<ObjectIdentifier> lotIDList = new ArrayList<>();
        lotIDList.add(lotID);
        Results.LotInfoInqResult lotInfoInqResult3 = (Results.LotInfoInqResult) lotGeneralTestCase.getLotInfoCase(lotIDList).getBody();
        List<ObjectIdentifier> lotIDInCassetteList3 = lotInfoInqResult3.getLotListInCassetteInfo().getLotIDList();
        Assert.isTrue(lotIDInCassetteList3.size() == 1 ,"the lot count in cassette must be 1 after merge");
    }

    public void mergeIsNotMergePoint(){
        //【step1】split with out merge point
        //【step1-1】stb one product lot
        String bankID = "BNK-0S";
        String sourceProductID = "RAW-2000.01";
        String productID = "PRODUCT0.01";
        TestInfos.StbInfo stbInfo = new TestInfos.StbInfo(bankID, sourceProductID, productID, 25L, false);
        ObjectIdentifier lotID = stbCase.STB_Normal(stbInfo);

        //【step1-2】set child wafer size
        TestInfos.SplitInfo splitInfo = new TestInfos.SplitInfo();
        splitInfo.setLotID(lotID);
        splitInfo.setChildLotWaferSize(3);

        //【step1-3】set merge point info
        TestInfos.SplitMergePointInfo splitMergePointInfo = new TestInfos.SplitMergePointInfo("2000.0100", new ObjectIdentifier("LAYER0MA.01"));
        splitInfo.setSplitMergePointInfo(splitMergePointInfo);
        ObjectIdentifier childLotID = lotSplitCase.split_norm(splitInfo);

        //【step2】merge
        try {
            lotMergeTestCase.mergeLotReq(lotID, childLotID);
        }catch (ServiceException e) {
            Assert.isTrue(Validations.isEquals(e.getCode(), retCodeConfig.getLotNotHeld()), e.getMessage());
        }
    }
    public void mergeWithParentLotOnHold(){
        //【step1】split with out merge point
        String bankID = "BNK-0S";
        String sourceProductID = "RAW-2000.01";
        String productID = "PRODUCT0.01";
        TestInfos.StbInfo stbInfo = new TestInfos.StbInfo(bankID, sourceProductID, productID, 25L, false);
        ObjectIdentifier lotID = stbCase.STB_Normal(stbInfo);

        //【step2】hold child lot
        String reasonCode = "SOHL";
        String reasonableOperation = "C";
        lotHoldCase.lotHold(lotID, reasonCode, reasonableOperation);

        //【step3】split lot without hold release
        TestInfos.SplitWithHoldInfo splitWithHoldReleaseInfo = new TestInfos.SplitWithHoldInfo();
        splitWithHoldReleaseInfo.setLotID(lotID);
        splitWithHoldReleaseInfo.setChildLotWaferSize(3);
        splitWithHoldReleaseInfo.setReleaseReasonCodeID(null);
        List<Infos.LotHoldReq>  lotHoldReqList = new ArrayList<>();
        Infos.LotHoldReq lotHoldReq = new Infos.LotHoldReq();
        lotHoldReq.setResponsibleOperationMark(reasonableOperation);
        lotHoldReq.setHoldType("HoldLot");
        lotHoldReq.setHoldUserID(testUtils.getUser().getUserID());
        lotHoldReq.setHoldReasonCodeID(new ObjectIdentifier(reasonCode));
        lotHoldReqList.add(lotHoldReq);
        splitWithHoldReleaseInfo.setLotHoldReqList(lotHoldReqList);
        ObjectIdentifier childLotID = lotSplitCase.splitWithHold(splitWithHoldReleaseInfo);

        //【step4】merge
        lotMergeTestCase.mergeLotReq(lotID, childLotID);

        //【step5】check the result, the parent lot status should be still onhold.
        Results.LotInfoInqResult lotInfoByLotID = testUtils.getLotInfoByLotID(lotID);
        for (int i = 0; i < CimArrayUtils.getSize(lotInfoByLotID.getLotInfoList()); i++) {
            Infos.LotInfo lotInfo = lotInfoByLotID.getLotInfoList().get(i);
            if (CimObjectUtils.equalsWithValue(lotInfo.getLotBasicInfo().getLotID(), lotID)) {
                Assert.isTrue(CimStringUtils.equals(lotInfo.getLotBasicInfo().getLotStatus(), "ONHOLD"), "the parent lot status should be 'ONHOLD'");
            }
        }
        return;
    }

    public void mergeWithChildLotOnHold(){
        //【step1】split with out merge point
        String bankID = "BNK-0S";
        String sourceProductID = "RAW-2000.01";
        String productID = "PRODUCT0.01";
        TestInfos.StbInfo stbInfo = new TestInfos.StbInfo(bankID, sourceProductID, productID, 25L, false);
        ObjectIdentifier lotID = stbCase.STB_Normal(stbInfo);

        //【step2】make split
        TestInfos.SplitInfo splitInfo = new TestInfos.SplitInfo();
        splitInfo.setLotID(lotID);
        splitInfo.setChildLotWaferSize(3);
        ObjectIdentifier childLotID = lotSplitCase.split_norm(splitInfo);

        //【step3】hold child lot
        String reasonCode = "SOHL";
        String reasonableOperation = "C";
        lotHoldCase.lotHold(childLotID, reasonCode, reasonableOperation);

        //【step4】merge lot, it will throw exception.(802, "All hold records of Child Lot has to be owned by Parent Lot too.")
        try {
            lotMergeTestCase.mergeLotReq(lotID, childLotID);
        }catch (ServiceException e) {
            Assert.isTrue(Validations.isEquals(e.getCode(), retCodeConfig.getHoldRecordOfChildOwn()), e.getMessage());
        }

        return;
    }

    public void mergeWithDifferentHoldReson(){
        //【step1】split with out merge point
        String bankID = "BNK-0S";
        String sourceProductID = "RAW-2000.01";
        String productID = "PRODUCT0.01";
        TestInfos.StbInfo stbInfo = new TestInfos.StbInfo(bankID, sourceProductID, productID, 25L, false);
        ObjectIdentifier lotID = stbCase.STB_Normal(stbInfo);

        //【step2】make split
        TestInfos.SplitInfo splitInfo = new TestInfos.SplitInfo();
        splitInfo.setLotID(lotID);
        splitInfo.setChildLotWaferSize(3);
        ObjectIdentifier childLotID = lotSplitCase.split_norm(splitInfo);

        //【step3】hold child lot
        String reasonCode = "SOHL";
        String reasonableOperation = "C";
        lotHoldCase.lotHold(childLotID, reasonCode, reasonableOperation);

        lotHoldCase.lotHold(lotID, "SOOR", reasonableOperation);

        //【step4】merge lot, it will throw exception.(802, "All hold records of Child Lot has to be owned by Parent Lot too.")
        try {
            lotMergeTestCase.mergeLotReq(lotID, childLotID);
        }catch (ServiceException e) {
            Assert.isTrue(Validations.isEquals(e.getCode(), retCodeConfig.getHoldRecordOfChildOwn()), e.getMessage());
        }

        return;
    }

    public void mergeWithVendorLotJustReceived(){
        //【step1】recevie one vendor lot
        String vendorLotID = GenerateVendorlot.getVendorLot();;
        String bankID = "BNK-0S";
        String sourceProductID = "RAW-2000.01";
        vendorLotReceiveCase.VendorLotReceive(bankID,  vendorLotID, sourceProductID, 100);

        //【step2】split
        int parentLotWaferCount = 90;
        int childLotWaferCount = 10;
        boolean isNeedPrepared = false;
        TestInfos.SplitWithNotOnRouteInfo splitWithNotOnRouteInfo = new TestInfos.SplitWithNotOnRouteInfo(
                new ObjectIdentifier(vendorLotID), parentLotWaferCount, childLotWaferCount, isNeedPrepared);
        ObjectIdentifier childLotID = lotSplitCase.splitWithNotOnRoute(splitWithNotOnRouteInfo);

        //【step4】merge lot
        lotMergeTestCase.mergeLotNotOnPfReq(new ObjectIdentifier(vendorLotID), childLotID);
        return;
    }

    public void mergeWithVendorLotHasPrepared(){
        //【step1】find one empty cassette
        String cassetteCategory = "FOUP";
        String cassetteStatus = "AVAILABLE";
        ObjectIdentifier emptyCassette = testUtils.findEmptyCassette(cassetteCategory, cassetteStatus);

        //【step2】make prepared
        String bankID = "BNK-0S";
        String sourceProductID = "RAW-2000.01";
        int waferSize = 10;
        Response response = vendorLotPrepareCase.VendorLotPrepare(bankID, sourceProductID, emptyCassette, waferSize);
        Results.MaterialPrepareReqResult body = (Results.MaterialPrepareReqResult) response.getBody();
        ObjectIdentifier vendorLotID = body.getLotID();

        //【step3】split the lot which not on route
        int parentLotWaferCount = 8;
        int childLotWaferCount = 2;
        boolean isPrepared = true;
        TestInfos.SplitWithNotOnRouteInfo splitWithNotOnRouteInfo = new TestInfos.SplitWithNotOnRouteInfo(
                vendorLotID, parentLotWaferCount, childLotWaferCount, isPrepared);
        ObjectIdentifier childLotID = lotSplitCase.splitWithNotOnRoute(splitWithNotOnRouteInfo);

        //【step4】merge lot
        lotMergeTestCase.mergeLotNotOnPfReq(vendorLotID, childLotID);
        return;
    }

    public void mergeWithChildLotOnSubRoute(){
        //【step1】split with out merge point
        String bankID = "BNK-0S";
        String sourceProductID = "RAW-2000.01";
        String productID = "PRODUCT0.01";
        String operationNumber = "2000.0400";
        TestInfos.StbInfo stbInfo = new TestInfos.StbInfo(bankID, sourceProductID, productID, 25L, false);
        ObjectIdentifier lotID = testUtils.stbAndSkip(stbInfo, operationNumber);

        //【step2】make split
        TestInfos.SplitInfo splitInfo = new TestInfos.SplitInfo();
        splitInfo.setLotID(lotID);
        splitInfo.setChildLotWaferSize(3);
        ObjectIdentifier childLotID = lotSplitCase.split_norm(splitInfo);

        //【step3】skip child lot to sub route
        //【step3-1】get the cassette id by lotID
        ObjectIdentifier cassetteID = testUtils.getCassetteIDByLotID(childLotID);

        //【step3-2】pflow/connected_route_list/inq
        List<Infos.ConnectedRouteList> connectedRouteLists = processFlowTestCase.multiPathListInqCase(cassetteID, childLotID);
        Infos.ConnectedRouteList connectedRouteList = connectedRouteLists.get(0);

        //【sttep3-3】lot/sub_route_branch/req
        lotSubRouteBranchTestCase.subRouteBranchReq(childLotID, connectedRouteList.getReturnRouteID(), operationNumber,
                connectedRouteList.getRouteID(), connectedRouteList.getReturnOperationNumber());

        //【step4】merge lot, it will throw exception.(2003, "Lots are not at the same responsible operation.")
        try {
            lotMergeTestCase.mergeLotReq(lotID, childLotID);
        }catch (ServiceException e) {
            Assert.isTrue(Validations.isEquals(e.getCode(), retCodeConfig.getSamePreOperation()), e.getMessage());
        }

        return;
    }

    public void mergeWithEIStatus(){

        //【step1】stb one product lot
        String bankID = "BNK-0S";
        String sourceProductID = "RAW-2000.01";
        String productID = "PRODUCT0.01";
        TestInfos.StbInfo stbInfo = new TestInfos.StbInfo(bankID, sourceProductID, productID, 25L, false);
        ObjectIdentifier lotID = stbCase.STB_Normal(stbInfo);

        //【step2】make split
        TestInfos.SplitInfo splitInfo = new TestInfos.SplitInfo();
        splitInfo.setLotID(lotID);
        splitInfo.setChildLotWaferSize(3);
        ObjectIdentifier childLotID = lotSplitCase.split_norm(splitInfo);

        //【step3】load the cassette to make the Cassette Xfer Status is EI
        lotLoadToEquipmentCase.manual_Loading_Without_StartLotsReservation(lotID, new ObjectIdentifier("1SRT03"), new ObjectIdentifier("P1"));

        //【step4】merge lot, it will throw exception.(905, "The transfer status [%s] of cassette [%s] is invalid, Or carrier has another transporting reservation.")
        try {
            lotMergeTestCase.mergeLotReq(lotID, childLotID);
        }catch (ServiceException e) {
            Assert.isTrue(Validations.isEquals(e.getCode(), retCodeConfig.getInvalidCassetteTransferState()), e.getMessage());
        }

        return;
    }

    public void mergeWithProcessingStatus(){

        //【step1】stb one product lot
        String bankID = "BNK-0S";
        String sourceProductID = "RAW-2000.01";
        String productID = "PRODUCT0.01";
        TestInfos.StbInfo stbInfo = new TestInfos.StbInfo(bankID, sourceProductID, productID, 25L, false);
        ObjectIdentifier lotID = stbCase.STB_Normal(stbInfo);

        //【step2】make split
        TestInfos.SplitInfo splitInfo = new TestInfos.SplitInfo();
        splitInfo.setLotID(lotID);
        splitInfo.setChildLotWaferSize(3);
        ObjectIdentifier childLotID = lotSplitCase.split_norm(splitInfo);

        //【step3】make the lot status is in-processing
        ObjectIdentifier equipmentID = new ObjectIdentifier("1SRT03");
        //【step3-1】load the cassette
        lotLoadToEquipmentCase.manual_Loading_Without_StartLotsReservation(lotID, equipmentID, new ObjectIdentifier("P1"));

        //【step3-2】get the lot cassette
        ObjectIdentifier cassetteID = testUtils.getCassetteIDByLotID(lotID);

        //【step3-3】move in the lot
        List<ObjectIdentifier> cassetteIDList = new ArrayList<>();
        cassetteIDList.add(cassetteID);
        moveInCase.onlyMoveIn(cassetteIDList, equipmentID);

        //【step4】merge lot, it will throw exception.(905, "The transfer status [%s] of cassette [%s] is invalid, Or carrier has another transporting reservation.")
        try {
            lotMergeTestCase.mergeLotReq(lotID, childLotID);
        }catch (ServiceException e) {
            Assert.isTrue(Validations.isEquals(e.getCode(), retCodeConfig.getInvalidCassetteTransferState()), e.getMessage());
        }

        return;
    }

    public void mergeWithChildLotWithGrandChildLot(){

        //【step1】stb one product lot
        String bankID = "BNK-0S";
        String sourceProductID = "RAW-2000.01";
        String productID = "PRODUCT0.01";
        TestInfos.StbInfo stbInfo = new TestInfos.StbInfo(bankID, sourceProductID, productID, 25L, false);
        ObjectIdentifier lotID = stbCase.STB_Normal(stbInfo);

        //【step3】make split from parent lot
        TestInfos.SplitInfo splitInfo = new TestInfos.SplitInfo();
        splitInfo.setLotID(lotID);
        splitInfo.setChildLotWaferSize(5);
        ObjectIdentifier childLotID1 = lotSplitCase.split_norm(splitInfo);

        splitInfo.setStartPosition(10);
        ObjectIdentifier childLotID2 = lotSplitCase.split_norm(splitInfo);


        //【step4】make split from child lot
        TestInfos.SplitInfo splitInfo1 = new TestInfos.SplitInfo();
        splitInfo1.setLotID(childLotID1);
        splitInfo1.setChildLotWaferSize(2);
        ObjectIdentifier childLotID3 = lotSplitCase.split_norm(splitInfo1);

        //【step5】merge the child lot 2 & 3
        lotMergeCase.merge_norm(childLotID2, childLotID3);
        return;
    }

    public void mergeWithLotWithGrandChildLot(){

        //【step1】stb one product lot
        String bankID = "BNK-0S";
        String sourceProductID = "RAW-2000.01";
        String productID = "PRODUCT0.01";
        TestInfos.StbInfo stbInfo = new TestInfos.StbInfo(bankID, sourceProductID, productID, 25L, false);
        ObjectIdentifier lotID = stbCase.STB_Normal(stbInfo);

        //【step3】make split from parent lot
        TestInfos.SplitInfo splitInfo = new TestInfos.SplitInfo();
        splitInfo.setLotID(lotID);
        splitInfo.setChildLotWaferSize(5);
        ObjectIdentifier childLotID1 = lotSplitCase.split_norm(splitInfo);

        splitInfo.setStartPosition(10);
        ObjectIdentifier childLotID2 = lotSplitCase.split_norm(splitInfo);


        //【step4】make split from child lot
        TestInfos.SplitInfo splitInfo1 = new TestInfos.SplitInfo();
        splitInfo1.setLotID(childLotID1);
        splitInfo1.setChildLotWaferSize(2);
        ObjectIdentifier childLotID3 = lotSplitCase.split_norm(splitInfo1);

        //【step5】merge the child lot 0 & 3
        lotMergeCase.merge_norm(lotID, childLotID3);
        return;
    }

    public void mergeWithChildLotWithChildLot(){
        //【step1】stb one product lot
        String bankID = "BNK-0S";
        String sourceProductID = "RAW-2000.01";
        String productID = "PRODUCT0.01";
        TestInfos.StbInfo stbInfo = new TestInfos.StbInfo(bankID, sourceProductID, productID, 25L, false);
        ObjectIdentifier lotID = stbCase.STB_Normal(stbInfo);

        //【step3】make split from parent lot
        TestInfos.SplitInfo splitInfo = new TestInfos.SplitInfo();
        splitInfo.setLotID(lotID);
        splitInfo.setChildLotWaferSize(5);
        ObjectIdentifier childLotID1 = lotSplitCase.split_norm(splitInfo);

        splitInfo.setStartPosition(10);
        ObjectIdentifier childLotID2 = lotSplitCase.split_norm(splitInfo);


        //【step4】make split from child lot
        TestInfos.SplitInfo splitInfo1 = new TestInfos.SplitInfo();
        splitInfo1.setLotID(childLotID1);
        splitInfo1.setChildLotWaferSize(2);
        ObjectIdentifier childLotID3 = lotSplitCase.split_norm(splitInfo1);

        //【step5】merge the child lot 0 & 3
        lotMergeCase.merge_norm(childLotID2, childLotID1);
        return;
    }

    public void mergeWithFutureHoldWithSameHoldStep(){
        //【step1】stb one product lot
        String bankID = "BNK-0S";
        String sourceProductID = "RAW-2000.01";
        String productID = "PRODUCT0.01";
        TestInfos.StbInfo stbInfo = new TestInfos.StbInfo(bankID, sourceProductID, productID, 25L, false);
        ObjectIdentifier lotID = stbCase.STB_Normal(stbInfo);

        //【step2】future hold
        String reasonCode = "SOHL";
        String reasonableOperation = "C";
        //【step2-1】getOperationNumber and RouteID
        Results.LotOperationSelectionInqResult lotOperationSelectionInqResult = (Results.LotOperationSelectionInqResult) lotGeneralTestCase.getLotOperationSelectionInq(lotID, true, true, true).getBody();
        Infos.OperationNameAttributes operationNameAttributes = lotOperationSelectionInqResult.getOperationNameAttributesAttributes().getContent().get(1);
        String operationNumber = operationNameAttributes.getOperationNumber();
        ObjectIdentifier routeID = operationNameAttributes.getRouteID();

        //【step2-2】make future hold
        futureHoldTestCase.futureHoldRegisterBySpecLot(lotID, operationNumber, routeID, false, false);

        //【step3】make split from lot which has future hold, the child lot will inherited the future hold record.
        TestInfos.SplitInfo splitInfo = new TestInfos.SplitInfo();
        splitInfo.setLotID(lotID);
        splitInfo.setChildLotWaferSize(5);
        ObjectIdentifier childLotID = lotSplitCase.split_norm(splitInfo);

        //【step4】merge lot
        lotMergeCase.merge_norm(lotID, childLotID);
        return;
    }

    public void mergeWithFutureHoldWithDifferentHoldStep(){
        //【step1】stb one product lot
        String bankID = "BNK-0S";
        String sourceProductID = "RAW-2000.01";
        String productID = "PRODUCT0.01";
        TestInfos.StbInfo stbInfo = new TestInfos.StbInfo(bankID, sourceProductID, productID, 25L, false);
        ObjectIdentifier lotID = testUtils.stbAndSkip(stbInfo, "2000.0400");

        //【step2】split lot
        TestInfos.SplitInfo splitInfo = new TestInfos.SplitInfo();
        splitInfo.setLotID(lotID);
        splitInfo.setChildLotWaferSize(5);
        ObjectIdentifier childLotID = lotSplitCase.split_norm(splitInfo);

        //【step3】make future hold to lot
        String reasonCode = "SOHL";
        String reasonableOperation = "C";
        //【step3-1】getOperationNumber and RouteID
        Results.LotOperationSelectionInqResult lotOperationSelectionInqResult = (Results.LotOperationSelectionInqResult) lotGeneralTestCase.getLotOperationSelectionInq(lotID, true, true, true).getBody();
        Infos.OperationNameAttributes operationNameAttributes = lotOperationSelectionInqResult.getOperationNameAttributesAttributes().getContent().get(1);
        String operationNumber = operationNameAttributes.getOperationNumber();
        ObjectIdentifier routeID = operationNameAttributes.getRouteID();

        //【step3-2】make future hold
        futureHoldTestCase.futureHoldRegisterBySpecLot(lotID, operationNumber, routeID, false, false);

        //【step4】skip child lot to different step
        testUtils.skip(childLotID, "1000.0100");

        //【step5】make futute hold to child lot with same reason code
        //【step5-1】getOperationNumber and RouteID
        lotOperationSelectionInqResult = (Results.LotOperationSelectionInqResult) lotGeneralTestCase.getLotOperationSelectionInq(lotID, true, true, true).getBody();
        operationNameAttributes = lotOperationSelectionInqResult.getOperationNameAttributesAttributes().getContent().get(1);
        operationNumber = operationNameAttributes.getOperationNumber();
        routeID = operationNameAttributes.getRouteID();

        //【step5-2】make future hold
        futureHoldTestCase.futureHoldRegisterBySpecLot(childLotID, operationNumber, routeID, false, false);

        //【step6】merge lot, it will throw exception.(2003, "Lots are not at the same responsible operation.")
        try {
            lotMergeTestCase.mergeLotReq(lotID, childLotID);
        }catch (ServiceException e) {
            Assert.isTrue(Validations.isEquals(e.getCode(), retCodeConfig.getSamePreOperation()), e.getMessage());
        }
        return;
    }

    public void mergeWithFutureHoldWithDifferentReasonCode(){
        //【step1】stb one product lot
        String bankID = "BNK-0S";
        String sourceProductID = "RAW-2000.01";
        String productID = "PRODUCT0.01";
        TestInfos.StbInfo stbInfo = new TestInfos.StbInfo(bankID, sourceProductID, productID, 25L, false);
        ObjectIdentifier lotID = testUtils.stbAndSkip(stbInfo, "2000.0400");

        //【step2】split lot
        TestInfos.SplitInfo splitInfo = new TestInfos.SplitInfo();
        splitInfo.setLotID(lotID);
        splitInfo.setChildLotWaferSize(5);
        ObjectIdentifier childLotID = lotSplitCase.split_norm(splitInfo);

        //【step3】make future hold to lot & child lot
        //【step3-1】getOperationNumber and RouteID
        Results.LotOperationSelectionInqResult lotOperationSelectionInqResult = (Results.LotOperationSelectionInqResult) lotGeneralTestCase.getLotOperationSelectionInq(lotID, true, true, true).getBody();
        Infos.OperationNameAttributes operationNameAttributes = lotOperationSelectionInqResult.getOperationNameAttributesAttributes().getContent().get(1);
        String operationNumber = operationNameAttributes.getOperationNumber();
        ObjectIdentifier routeID = operationNameAttributes.getRouteID();

        //【step3-2】make future hold for parent lot
        futureHoldTestCase.futureHoldRegisterBySpecLotWithReasonCode(lotID, operationNumber, routeID, false, false, "SOHL");

        //【step3-3】make futute hold for child lot with different reason code
        futureHoldTestCase.futureHoldRegisterBySpecLotWithReasonCode(childLotID, operationNumber, routeID, false, false, "SOOR");

        //【step4】merge lot, it will throw exception.(301, "A child lot has valid FutureHold and you cannot merge it.")
        try {
            lotMergeTestCase.mergeLotReq(lotID, childLotID);
        }catch (ServiceException e) {
            Assert.isTrue(Validations.isEquals(e.getCode(), retCodeConfig.getConnotMergeFuturehold()), e.getMessage());
        }
        return;
    }

    public void mergeWithLotNotes(){
        //【step1】stb one product lot
        String bankID = "BNK-0S";
        String sourceProductID = "RAW-2000.01";
        String productID = "PRODUCT0.01";
        TestInfos.StbInfo stbInfo = new TestInfos.StbInfo(bankID, sourceProductID, productID, 25L, false);
        ObjectIdentifier lotID = stbCase.STB_Normal(stbInfo);

        //【step2】make lot notes for parent lot
        Infos.LotNoteInfo lotNoteInfo = new Infos.LotNoteInfo();
        lotNoteInfo.setReportUserID(lotID);
        lotNoteInfo.setLotNoteDescription("this is lot note description");
        lotNoteInfo.setLotNoteTitle("Lot Note Tile");
        lotNoteCase.lotNote_norm(lotNoteInfo);

        //【step3】make split from parent lot
        TestInfos.SplitInfo splitInfo = new TestInfos.SplitInfo();
        splitInfo.setLotID(lotID);
        splitInfo.setChildLotWaferSize(5);
        ObjectIdentifier childLotID1 = lotSplitCase.split_norm(splitInfo);

        //【step4】merge the child lot 0 & 3
        lotMergeCase.merge_norm(lotID, childLotID1);
        return;
    }

    public void mergeWithLotOperationNotes(){
        //【step1】stb one product lot
        String bankID = "BNK-0S";
        String sourceProductID = "RAW-2000.01";
        String productID = "PRODUCT0.01";
        TestInfos.StbInfo stbInfo = new TestInfos.StbInfo(bankID, sourceProductID, productID, 25L, false);
        ObjectIdentifier lotID = stbCase.STB_Normal(stbInfo);

        //【step2】make lot operation note for parent lot
        //【step2-1】get lot info
        Results.LotInfoInqResult lotInfoResult = testUtils.getLotInfoByLotID(lotID);

        //【step2-2】make the lot operation note
        String currrentOperationNumber = lotInfoResult.getLotInfoList().get(0).getLotOperationInfo().getOperationNumber();
        ObjectIdentifier operationID = lotInfoResult.getLotInfoList().get(0).getLotOperationInfo().getOperationID();
        ObjectIdentifier routeID = lotInfoResult.getLotInfoList().get(0).getLotOperationInfo().getRouteID();
        String lotOperationDescription = "this is lot operation note decription";
        String lotOperationTitle = "lot operation note title";
        Params.LotOperationNoteInfoRegisterReqParams params = new Params.LotOperationNoteInfoRegisterReqParams();
        params.setUser(testUtils.getUser());
        params.setLotID(lotID);
        params.setLotOperationNoteTitle(lotOperationTitle);
        params.setLotOperationNoteDescription(lotOperationDescription);
        params.setRouteID(routeID);
        params.setOperationID(operationID);
        params.setOperationNumber(currrentOperationNumber);
        electronicInformationController.lotOpeMemoAddReq(params);

        //【step3】make split from parent lot
        TestInfos.SplitInfo splitInfo = new TestInfos.SplitInfo();
        splitInfo.setLotID(lotID);
        splitInfo.setChildLotWaferSize(5);
        ObjectIdentifier childLotID1 = lotSplitCase.split_norm(splitInfo);

        //【step4】merge lot
        lotMergeCase.merge_norm(lotID, childLotID1);
        return;
    }
    public ObjectIdentifier mergeWithBothHaveMonitorGroupInfo() {
        //【step1】stb one product lot
        String bankID = "BNK-0S";
        String sourceProductID = "RAW-2000.01";
        String productID = "PRODUCT0.01";
        TestInfos.StbInfo stbInfo = new TestInfos.StbInfo(bankID, sourceProductID, productID, 25L, false);
        ObjectIdentifier lotID = stbCase.STB_Normal(stbInfo);

        //【step2】stb one monitor lot
        TestInfos.StbMonitorLotInfo stbMonitorLotInfo = new TestInfos.StbMonitorLotInfo();
        stbMonitorLotInfo.setBankID("BK-CTRL");
        stbMonitorLotInfo.setProductCount(5);
        stbMonitorLotInfo.setSourceProductID("RAW-2000.01");
        ObjectIdentifier monitorLotID = stbCase.STB_MonitorLot(stbMonitorLotInfo);


        //【step3】make monitor grouping
        Params.MonitorBatchCreateReqParams monitorProdLotsReqParams = new Params.MonitorBatchCreateReqParams();
        monitorProdLotsReqParams.setUser(testUtils.getUser());
        monitorProdLotsReqParams.setMonitorLotID(monitorLotID);   // yes, the product lot will be setted here.
        List<Infos.MonRelatedProdLots> monRelatedProdLotsList = new ArrayList<>();
        Infos.MonRelatedProdLots monRelatedProdLots = new Infos.MonRelatedProdLots();
        monRelatedProdLots.setProductLotID(lotID);
        monRelatedProdLotsList.add(monRelatedProdLots);
        monitorProdLotsReqParams.setStrMonRelatedProdLots(monRelatedProdLotsList);
        processMonitorController.monitorBatchCreateReq(monitorProdLotsReqParams);

        //【step4】make split from production lot which has monitor group
        TestInfos.SplitInfo splitInfo = new TestInfos.SplitInfo();
        splitInfo.setLotID(lotID);
        splitInfo.setChildLotWaferSize(5);
        ObjectIdentifier childLotID = lotSplitCase.split_norm(splitInfo);

        //【step5】check the child production lot has monitor group or not, the child production lot shoud inherit monitor group info from parent lot
        //【step5-1】get the child lot - cassette ID
        ObjectIdentifier childCassetteID = testUtils.getCassetteIDByLotID(childLotID);

        //【step5-2】get the child lot monitor group info
        Params.MonitorBatchRelationInqParams monitorBatchRelationInqParams = new Params.MonitorBatchRelationInqParams();
        monitorBatchRelationInqParams.setUser(testUtils.getUser());
        monitorBatchRelationInqParams.setCassetteID(childCassetteID);
        monitorBatchRelationInqParams.setLotID(childLotID);
        List<Infos.MonitorGroups> body = (List<Infos.MonitorGroups>) processMonitorInqController.monitorBatchRelationInq(monitorBatchRelationInqParams).getBody();
        Validations.check(CimArrayUtils.isEmpty(body), "the child lot don't inherit monitor group info");

        //【step6】make merge
        lotMergeCase.merge_norm(lotID, childLotID);

        return lotID;
    }

    public ObjectIdentifier mergeWithParentLotHasMonitorGroupInfo() {
        //【step1】stb one product lot
        String bankID = "BNK-0S";
        String sourceProductID = "RAW-2000.01";
        String productID = "PRODUCT0.01";
        TestInfos.StbInfo stbInfo = new TestInfos.StbInfo(bankID, sourceProductID, productID, 25L, false);
        ObjectIdentifier lotID = stbCase.STB_Normal(stbInfo);

        //【step2】stb one monitor lot
        TestInfos.StbMonitorLotInfo stbMonitorLotInfo = new TestInfos.StbMonitorLotInfo();
        stbMonitorLotInfo.setBankID("BK-CTRL");
        stbMonitorLotInfo.setProductCount(5);
        stbMonitorLotInfo.setSourceProductID("RAW-2000.01");
        ObjectIdentifier monitorLotID = stbCase.STB_MonitorLot(stbMonitorLotInfo);

        //【step3】make monitor grouping
        Params.MonitorBatchCreateReqParams monitorProdLotsReqParams = new Params.MonitorBatchCreateReqParams();
        monitorProdLotsReqParams.setUser(testUtils.getUser());
        monitorProdLotsReqParams.setMonitorLotID(monitorLotID);   // yes, the product lot will be setted here.
        List<Infos.MonRelatedProdLots> monRelatedProdLotsList = new ArrayList<>();
        Infos.MonRelatedProdLots monRelatedProdLots = new Infos.MonRelatedProdLots();
        monRelatedProdLots.setProductLotID(lotID);
        monRelatedProdLotsList.add(monRelatedProdLots);
        monitorProdLotsReqParams.setStrMonRelatedProdLots(monRelatedProdLotsList);
        processMonitorController.monitorBatchCreateReq(monitorProdLotsReqParams);

        //【step4】make split from monitor lot which has monitor group
        TestInfos.SplitInfo splitInfo = new TestInfos.SplitInfo();
        splitInfo.setLotID(monitorLotID);
        splitInfo.setChildLotWaferSize(1);
        ObjectIdentifier childMonitorLotID = lotSplitCase.split_norm(splitInfo);

        //【step4】check the child monitor lot has monitor group or not, the child monitor lot shoud not inherit monitor group info from parent lot
        //【step4-1】get the child monitor lot - cassette ID
        ObjectIdentifier childMonitorCassetteID = testUtils.getCassetteIDByLotID(childMonitorLotID);

        //【step4-2】get the child lot monitor group info
        Params.MonitorBatchRelationInqParams monitorBatchRelationInqParams = new Params.MonitorBatchRelationInqParams();
        monitorBatchRelationInqParams.setUser(testUtils.getUser());
        monitorBatchRelationInqParams.setCassetteID(childMonitorCassetteID);
        monitorBatchRelationInqParams.setLotID(childMonitorLotID);
        List<Infos.MonitorGroups> body = (List<Infos.MonitorGroups>) processMonitorInqController.monitorBatchRelationInq(monitorBatchRelationInqParams).getBody();
        Validations.check(!CimArrayUtils.isEmpty(body), "the child monitor lot needn't inherit monitor group info");

        //【step5】make merge
        lotMergeCase.merge_norm(monitorLotID, childMonitorLotID);

        return lotID;
    }

    public void mergeWithLotComment(){
        //【step1】stb one product lot
        String bankID = "BNK-0S";
        String sourceProductID = "RAW-2000.01";
        String productID = "PRODUCT0.01";
        TestInfos.StbInfo stbInfo = new TestInfos.StbInfo(bankID, sourceProductID, productID, 25L, false);
        ObjectIdentifier lotID = stbCase.STB_Normal(stbInfo);

        //【step2】after stb, the parent lot should has lot comment by default (setting)
        Params.LotAnnotationInqParams lotAnnotationInqParams = new Params.LotAnnotationInqParams();
        lotAnnotationInqParams.setUser(testUtils.getUser());
        lotAnnotationInqParams.setLotID(lotID);
        Results.LotAnnotationInqResult parentLotComment = (Results.LotAnnotationInqResult) electronicInformationInqController.lotAnnotationInq(lotAnnotationInqParams).getBody();
        int parentLotCommentSize = CimArrayUtils.getSize(parentLotComment.getLotCommentInfos());

        //【step3】make split from lot which has lot comment
        TestInfos.SplitInfo splitInfo = new TestInfos.SplitInfo();
        splitInfo.setLotID(lotID);
        splitInfo.setChildLotWaferSize(5);
        ObjectIdentifier childLotID = lotSplitCase.split_norm(splitInfo);

        //【step4】check the child lot, the lot comment should be inherit
        lotAnnotationInqParams = new Params.LotAnnotationInqParams();
        lotAnnotationInqParams.setUser(testUtils.getUser());
        lotAnnotationInqParams.setLotID(childLotID);
        Results.LotAnnotationInqResult childLotComment = (Results.LotAnnotationInqResult) electronicInformationInqController.lotAnnotationInq(lotAnnotationInqParams).getBody();
        int childLotCommentSize = CimArrayUtils.getSize(parentLotComment.getLotCommentInfos());
        Validations.check(parentLotCommentSize != childLotCommentSize, "the child lot don't inherit lot comment from parent lot");

        //【step5】merge lot
        lotMergeCase.merge_norm(lotID, childLotID);
        return;
    }

}