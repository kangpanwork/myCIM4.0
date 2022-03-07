package com.fa.cim.newIntegration.LotOperation.scase;

import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.Response;
import com.fa.cim.common.utils.*;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.controller.einfo.ElectronicInformationController;
import com.fa.cim.controller.einfo.ElectronicInformationInqController;
import com.fa.cim.controller.lot.LotController;
import com.fa.cim.controller.lot.LotInqController;
import com.fa.cim.controller.processcontrol.ProcessInqController;
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
import com.fa.cim.newIntegration.equipment.scase.StartLotsReservationCase;
import com.fa.cim.newIntegration.stb.scase.STBCase;
import com.fa.cim.newIntegration.tcase.FutureHoldTestCase;
import com.fa.cim.newIntegration.tcase.LotGeneralTestCase;
import com.fa.cim.newIntegration.tcase.LotSplitTestCase;
import com.fa.cim.jpa.SearchCondition;
import com.fa.cim.utils.GenerateVendorlot;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/9/11       ********             lightyh             create file
 *
 * @author: lightyh
 * @date: 2019/9/11 17:48
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Service
@Slf4j
public class LotSplitCase {

    @Autowired
    private STBCase stbCase;

    @Autowired
    private LotGeneralTestCase lotGeneralTestCase;

    @Autowired
    private LotHoldCase lotHoldCase;

    @Autowired
    private TestUtils testUtils;

    @Autowired
    private LotController lotController;

    @Autowired
    private RetCodeConfig retCodeConfig;

    @Autowired
    private VendorLotReceiveCase vendorLotReceiveCase;

    @Autowired
    private VendorLotPrepareCase vendorLotPrepareCase;

    @Autowired
    private LotInqController lotInqController;

    @Autowired
    private LotSplitTestCase lotSplitTestCase;

    @Autowired
    private LotLoadToEquipmentCase lotLoadToEquipmentCase;

    @Autowired
    private MoveInCase moveInCase;

    @Autowired
    private LotMergeCase lotMergeCase;

    @Autowired
    private FutureHoldTestCase futureHoldTestCase;

    @Autowired
    private ProcessInqController processInqController;

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

    @Autowired
    private StartLotsReservationCase startLotsReservationCase;

    public ObjectIdentifier splitBySpecificLotID(ObjectIdentifier lotID) {
        //【step2】lot info
        List<ObjectIdentifier> lotIDList = new ArrayList<>();
        lotIDList.add(lotID);
        Results.LotInfoInqResult lotInfoInqResult = (Results.LotInfoInqResult) lotGeneralTestCase.getLotInfoCase(lotIDList).getBody();
        Infos.LotInfo lotInfo = lotInfoInqResult.getLotInfoList().get(0);
        List<Infos.LotWaferAttributes> lotWaferAttributesList = lotInfo.getLotWaferAttributesList();
        // the count selected
        //【step3】lot split
        List<Infos.LotWaferAttributes> subLotWaferAttributesList = new ArrayList<>();
        int size = new Random().nextInt(lotWaferAttributesList.size()) + 1;
        for (int i = 0; i < size; i++) {
            subLotWaferAttributesList.add(lotWaferAttributesList.get(i));
        }
        List<ObjectIdentifier> childWaferIDs = subLotWaferAttributesList.stream().map(Infos.LotWaferAttributes::getWaferID).collect(Collectors.toList());
        ObjectIdentifier childLot = (ObjectIdentifier) lotSplitTestCase.splitLotReq(childWaferIDs, false, false,
                "", new ObjectIdentifier(), lotID, new ObjectIdentifier()).getBody();
        //【step4】lot info
        Results.LotInfoInqResult lotInfoInqResult2 = (Results.LotInfoInqResult) lotGeneralTestCase.getLotInfoCase(lotIDList).getBody();
        List<ObjectIdentifier> lotIDInCassetteList = lotInfoInqResult2.getLotListInCassetteInfo().getLotIDList();
        Assert.isTrue(lotIDInCassetteList.size() == 2, "the lot count in cassette must be 2");
        return childLot;
    }

    public ObjectIdentifier splitWithoutMergePoint() {
        TestInfos.SplitInfo splitInfo = new TestInfos.SplitInfo();
        //【step1】stb one product lot
        String bankID = "BNK-0S";
        String sourceProductID = "RAW-2000.01";
        String productID = "PRODUCT0.01";
        TestInfos.StbInfo stbInfo = new TestInfos.StbInfo(bankID, sourceProductID, productID, 25L, false);
        splitInfo.setStbInfo(stbInfo);

        //【step2】set child wafer size
        splitInfo.setChildLotWaferSize(3);
        return this.split_norm(splitInfo);
    }


    public ObjectIdentifier splitWithMergePoint() {
        TestInfos.SplitInfo splitInfo = new TestInfos.SplitInfo();
        //【step1】stb one product lot
        String bankID = "BNK-0S";
        String sourceProductID = "RAW-2000.01";
        String productID = "PRODUCT0.01";
        TestInfos.StbInfo stbInfo = new TestInfos.StbInfo(bankID, sourceProductID, productID, 25L, false);
        splitInfo.setStbInfo(stbInfo);

        //【step2】set merge point info
        TestInfos.SplitMergePointInfo splitMergePointInfo = new TestInfos.SplitMergePointInfo("1000.0100", new ObjectIdentifier("LAYER0MA.01"));
        splitInfo.setSplitMergePointInfo(splitMergePointInfo);

        //【step3】set child wafer size
        splitInfo.setChildLotWaferSize(3);
        return this.split_norm(splitInfo);
    }

    public ObjectIdentifier splitWithSplitedAlready() {
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
        ObjectIdentifier childLotID = this.split_norm(splitInfo);

        //【step3】check the result， if the wafer size of child lot is not 3, then throw exception.
        // in UI,just the wafer of child lot can be selected to split again when current lot is child lot.
        List<ObjectIdentifier> lotIDList = new ArrayList<>();
        lotIDList.add(childLotID);
        Results.LotInfoInqResult lotInfoInqResult = (Results.LotInfoInqResult) lotGeneralTestCase.getLotInfoCase(lotIDList).getBody();
        for (int i = 0; i < CimArrayUtils.getSize(lotInfoInqResult.getLotInfoList()); i++) {
            Infos.LotInfo lotInfo = lotInfoInqResult.getLotInfoList().get(i);
            if (CimObjectUtils.equalsWithValue(lotInfo.getLotBasicInfo().getLotID(), childLotID)) {
                Validations.check(3 != CimArrayUtils.getSize(lotInfo.getLotWaferAttributesList()), "the child lot size is not 3！");
            }
        }
        return lotID;
    }

    public ObjectIdentifier splitWithHoldRelease() {
        //【step1】stb one product lot
        String bankID = "BNK-0S";
        String sourceProductID = "RAW-2000.01";
        String productID = "PRODUCT0.01";
        TestInfos.StbInfo stbInfo = new TestInfos.StbInfo(bankID, sourceProductID, productID, 25L, false);
        ObjectIdentifier lotID = stbCase.STB_Normal(stbInfo);

        //【step2】hold lot
        String reasonCode = "SOHL";
        String reasonableOperation = "C";
       /* try {
            // because of current step is 1000.0100, so it will throw exception when reasonableOperation is 'P'
            lotHoldCase.lotHold(lotID, reasonCode, "P");
        } catch (ServiceException e) {
            System.out.println(e);
            Validations.check(!Validations.isEquals(e.getCode(), retCodeConfig.getNotFoundProcessOperation()) , e.getMessage());
        }*/
        lotHoldCase.lotHold(lotID, reasonCode, reasonableOperation);

        //【step3】split lot with hold release
        TestInfos.SplitWithHoldInfo splitWithHoldReleaseInfo = new TestInfos.SplitWithHoldInfo();
        splitWithHoldReleaseInfo.setLotID(lotID);
        splitWithHoldReleaseInfo.setChildLotWaferSize(3);
        splitWithHoldReleaseInfo.setReleaseReasonCodeID(new ObjectIdentifier("LOHR"));
        List<Infos.LotHoldReq> lotHoldReqList = new ArrayList<>();
        Infos.LotHoldReq lotHoldReq = new Infos.LotHoldReq();
        lotHoldReq.setResponsibleOperationMark(reasonableOperation);
        lotHoldReq.setHoldType("HoldLot");
        lotHoldReq.setHoldUserID(testUtils.getUser().getUserID());
        lotHoldReq.setHoldReasonCodeID(new ObjectIdentifier(reasonCode));
        lotHoldReqList.add(lotHoldReq);
        splitWithHoldReleaseInfo.setLotHoldReqList(lotHoldReqList);
        this.splitWithHold(splitWithHoldReleaseInfo);
        return lotID;
    }


    public ObjectIdentifier split_norm(TestInfos.SplitInfo splitInfo) {
        //【step1】if lot id is null, then stb one product lot
        ObjectIdentifier parentLotID = CimObjectUtils.isNotEmpty(splitInfo.getLotID())
                ? splitInfo.getLotID() : stbCase.STB_Normal(splitInfo.getStbInfo());

        //【step2】skip to specific step, if it won't skip if skipOperationNumber is null.
        testUtils.skip(parentLotID, splitInfo.getSkipOperationNumber());

        //【step3】select the split wafer info
        //【step3-1】get lot info
        List<ObjectIdentifier> lotIDList = new ArrayList<>();
        lotIDList.add(parentLotID);
        Results.LotInfoInqResult lotInfoInqResult = (Results.LotInfoInqResult) lotGeneralTestCase.getLotInfoCase(lotIDList).getBody();
        Infos.LotInfo lotInfo = lotInfoInqResult.getLotInfoList().get(0);
        List<Infos.LotWaferAttributes> lotWaferAttributesList = lotInfo.getLotWaferAttributesList();
        int beforeSplitLotSize = CimArrayUtils.getSize(lotInfoInqResult.getLotListInCassetteInfo().getLotIDList());

        //【step3-2】store the split wafer info
        int childLotWaferSize = splitInfo.getChildLotWaferSize() > 0
                ? splitInfo.getChildLotWaferSize() : new Random().nextInt(lotWaferAttributesList.size()) + 1;
        List<ObjectIdentifier> childWaferIDs = new ArrayList<>();
        for (int i = 0; i < childLotWaferSize; i++) {
            childWaferIDs.add(lotWaferAttributesList.get(splitInfo.getStartPosition() + i).getWaferID());
        }

        //【step4】split
        Params.SplitLotReqParams splitLotReqParams = new Params.SplitLotReqParams();
        splitLotReqParams.setChildWaferIDs(childWaferIDs);
        if (null != splitInfo.getSplitMergePointInfo()) {
            splitLotReqParams.setMergedOperationNumber(splitInfo.getSplitMergePointInfo().getMergedOperationNumber());
            splitLotReqParams.setMergedRouteID(splitInfo.getSplitMergePointInfo().getMergeRouteID());
            splitLotReqParams.setFutureMergeFlag(true);
        }
        splitLotReqParams.setSubRouteID(splitInfo.getSubRouteID());
        splitLotReqParams.setReturnOperationNumber(splitInfo.getReturnOperationNumber());
        splitLotReqParams.setBranchingRouteSpecifyFlag(CimObjectUtils.isNotEmpty(splitInfo.getSubRouteID()));
        splitLotReqParams.setParentLotID(parentLotID);
        splitLotReqParams.setUser(testUtils.getUser());
        ObjectIdentifier childLotID = (ObjectIdentifier) lotController.splitLotReq(splitLotReqParams).getBody();

        //【step5】check the result
        Results.LotInfoInqResult lotInfoInqResult2 = (Results.LotInfoInqResult) lotGeneralTestCase.getLotInfoCase(lotIDList).getBody();
        int afterSplitLotSize = CimArrayUtils.getSize(lotInfoInqResult2.getLotListInCassetteInfo().getLotIDList());
        Assert.isTrue((beforeSplitLotSize + 1) == afterSplitLotSize, "split fail!");
        return childLotID;
    }

    public ObjectIdentifier splitWithHold(TestInfos.SplitWithHoldInfo splitWithHoldReleaseInfo) {
        //【step1】select the split wafer info
        //【step1-1】get lot info
        List<ObjectIdentifier> lotIDList = new ArrayList<>();
        lotIDList.add(splitWithHoldReleaseInfo.getLotID());
        Results.LotInfoInqResult lotInfoInqResult = (Results.LotInfoInqResult) lotGeneralTestCase.getLotInfoCase(lotIDList).getBody();
        Infos.LotInfo lotInfo = lotInfoInqResult.getLotInfoList().get(0);
        List<Infos.LotWaferAttributes> lotWaferAttributesList = lotInfo.getLotWaferAttributesList();
        int beforeSplitLotSize = CimArrayUtils.getSize(lotInfoInqResult.getLotListInCassetteInfo().getLotIDList());

        //【step1-2】store the split wafer info
        int childLotWaferSize = splitWithHoldReleaseInfo.getChildLotWaferSize() > 0
                ? splitWithHoldReleaseInfo.getChildLotWaferSize() : new Random().nextInt(lotWaferAttributesList.size()) + 1;
        List<ObjectIdentifier> childWaferIDs = new ArrayList<>();
        for (int i = 0; i < childLotWaferSize; i++) {
            childWaferIDs.add(lotWaferAttributesList.get(i).getWaferID());
        }

        //【step2】split with hold info
        ObjectIdentifier childLotID = null;
        if (null != splitWithHoldReleaseInfo.getReleaseReasonCodeID()) {
            //【step2-1】split with hold release
            Params.SplitLotWithHoldReleaseReqParams params = new Params.SplitLotWithHoldReleaseReqParams();
            params.setUser(testUtils.getUser());
            params.setParentLotID(splitWithHoldReleaseInfo.getLotID());
            params.setSubRouteID(splitWithHoldReleaseInfo.getSubRouteID());
            params.setBranchingRouteSpecifyFlag(null != splitWithHoldReleaseInfo.getSubRouteID());
            params.setStrLotHoldReleaseReqList(splitWithHoldReleaseInfo.getLotHoldReqList());
            params.setReleaseReasonCodeID(splitWithHoldReleaseInfo.getReleaseReasonCodeID());
            params.setChildWaferIDs(childWaferIDs);
            if (null != splitWithHoldReleaseInfo.getSplitMergePointInfo()) {
                params.setMergedRouteID(splitWithHoldReleaseInfo.getSplitMergePointInfo().getMergeRouteID());
                params.setMergedOperationNumber(splitWithHoldReleaseInfo.getSplitMergePointInfo().getMergedOperationNumber());
                params.setFutureMergeFlag(true);
            }

            childLotID = (ObjectIdentifier) lotController.splitLotWithHoldReleaseReq(params).getBody();
        } else {
            //【step2-1】split without hold release
            Params.SplitLotWithoutHoldReleaseReqParams params = new Params.SplitLotWithoutHoldReleaseReqParams();
            params.setUser(testUtils.getUser());
            params.setParentLotID(splitWithHoldReleaseInfo.getLotID());
            params.setSubRouteID(splitWithHoldReleaseInfo.getSubRouteID());
            params.setBranchingRouteSpecifyFlag(null != splitWithHoldReleaseInfo.getSubRouteID());
            params.setStrLotHoldReleaseReqList(splitWithHoldReleaseInfo.getLotHoldReqList());
            params.setReleaseReasonCodeID(null);
            params.setChildWaferIDs(childWaferIDs);
            if (null != splitWithHoldReleaseInfo.getSplitMergePointInfo()) {
                params.setMergedRouteID(splitWithHoldReleaseInfo.getSplitMergePointInfo().getMergeRouteID());
                params.setMergedOperationNumber(splitWithHoldReleaseInfo.getSplitMergePointInfo().getMergedOperationNumber());
                params.setFutureMergeFlag(true);
            }
            childLotID = (ObjectIdentifier) lotController.splitLotWithoutHoldReleaseReq(params).getBody();
        }

        //【step3】check the result
        Results.LotInfoInqResult lotInfoInqResult2 = (Results.LotInfoInqResult) lotGeneralTestCase.getLotInfoCase(lotIDList).getBody();
        int afterSplitLotSize = CimArrayUtils.getSize(lotInfoInqResult2.getLotListInCassetteInfo().getLotIDList());
        Assert.isTrue((beforeSplitLotSize + 1) == afterSplitLotSize, "split fail!");
        for (int i = 0; i < CimArrayUtils.getSize(lotInfoInqResult2.getLotInfoList()); i++) {
            Infos.LotInfo lotInfo1 = lotInfoInqResult2.getLotInfoList().get(i);
            String correntLotStatus = (null != splitWithHoldReleaseInfo.getReleaseReasonCodeID()) ? "Waiting" : "ONHOLD";
            Validations.check(!CimStringUtils.equals(lotInfo1.getLotBasicInfo().getLotStatus(), correntLotStatus), String.format("the lot status is not %s...", correntLotStatus));
        }

        return childLotID;
    }

    public ObjectIdentifier splitWithOutHoldRelease() {
        //【step1】stb one product lot
        String bankID = "BNK-0S";
        String sourceProductID = "RAW-2000.01";
        String productID = "PRODUCT0.01";
        TestInfos.StbInfo stbInfo = new TestInfos.StbInfo(bankID, sourceProductID, productID, 25L, false);
        ObjectIdentifier lotID = stbCase.STB_Normal(stbInfo);

        //【step2】hold lot
        String reasonCode = "SOHL";
        String reasonableOperation = "C";
        /*try {
            // because of current step is 1000.0100, so it will throw exception when reasonableOperation is 'P'
            lotHoldCase.lotHold(lotID, reasonCode, "P");
        } catch (ServiceException e) {
            Validations.check(!Validations.isEquals(e.getCode(), retCodeConfig.getNotFoundProcessOperation()) , e.getMessage());
        }*/
        lotHoldCase.lotHold(lotID, reasonCode, reasonableOperation);

        //【step3】split lot without hold release
        TestInfos.SplitWithHoldInfo splitWithHoldReleaseInfo = new TestInfos.SplitWithHoldInfo();
        splitWithHoldReleaseInfo.setLotID(lotID);
        splitWithHoldReleaseInfo.setChildLotWaferSize(3);
        splitWithHoldReleaseInfo.setReleaseReasonCodeID(null);
        List<Infos.LotHoldReq> lotHoldReqList = new ArrayList<>();
        Infos.LotHoldReq lotHoldReq = new Infos.LotHoldReq();
        lotHoldReq.setResponsibleOperationMark(reasonableOperation);
        lotHoldReq.setHoldType("HoldLot");
        lotHoldReq.setHoldUserID(testUtils.getUser().getUserID());
        lotHoldReq.setHoldReasonCodeID(new ObjectIdentifier(reasonCode));
        lotHoldReqList.add(lotHoldReq);
        splitWithHoldReleaseInfo.setLotHoldReqList(lotHoldReqList);
        this.splitWithHold(splitWithHoldReleaseInfo);
        return lotID;
    }


    public ObjectIdentifier splitVendorLotWithNotPrepared() {
        //【step1】recevie one vendor lot
        String vendorLotID = GenerateVendorlot.getVendorLot();;
        String bankID = "BNK-0S";
        String sourceProductID = "RAW-2000.01";
        vendorLotReceiveCase.VendorLotReceive(bankID, vendorLotID, sourceProductID, 100);

        //【step2】split
        int parentLotWaferCount = 90;
        int childLotWaferCount = 10;
        boolean isNeedPrepared = false;
        TestInfos.SplitWithNotOnRouteInfo splitWithNotOnRouteInfo = new TestInfos.SplitWithNotOnRouteInfo(
                new ObjectIdentifier(vendorLotID), parentLotWaferCount, childLotWaferCount, isNeedPrepared);
        return this.splitWithNotOnRoute(splitWithNotOnRouteInfo);
    }

    public ObjectIdentifier splitVendorLotWithPrepared() {
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

        int parentLotWaferCount = 8;
        int childLotWaferCount = 2;
        boolean isPrepared = true;
        TestInfos.SplitWithNotOnRouteInfo splitWithNotOnRouteInfo = new TestInfos.SplitWithNotOnRouteInfo(
                vendorLotID, parentLotWaferCount, childLotWaferCount, isPrepared);
        return this.splitWithNotOnRoute(splitWithNotOnRouteInfo);
    }


    public ObjectIdentifier splitWithNotOnRoute(TestInfos.SplitWithNotOnRouteInfo splitWithNotOnRouteInfo) {
        Params.SplitLotNotOnPfReqParams params = new Params.SplitLotNotOnPfReqParams();

        //【step1】select some child wafer to split if the parent had been prepared.
        if (CimBooleanUtils.isTrue(splitWithNotOnRouteInfo.isPrepared())) {
            //【step1-1】get lot info
            List<ObjectIdentifier> lotIDList = new ArrayList<>();
            lotIDList.add(splitWithNotOnRouteInfo.getParentID());
            Results.LotInfoInqResult lotInfoInqResult = (Results.LotInfoInqResult) lotGeneralTestCase.getLotInfoCase(lotIDList).getBody();
            Infos.LotInfo lotInfo = lotInfoInqResult.getLotInfoList().get(0);
            List<Infos.LotWaferAttributes> lotWaferAttributesList = lotInfo.getLotWaferAttributesList();
            int beforeSplitLotSize = CimArrayUtils.getSize(lotInfoInqResult.getLotListInCassetteInfo().getLotIDList());

            //【step1-2】store the split wafer info
            int childLotWaferSize = splitWithNotOnRouteInfo.getChildLotWaferSize() > 0
                    ? splitWithNotOnRouteInfo.getChildLotWaferSize() : new Random().nextInt(lotWaferAttributesList.size()) + 1;
            List<ObjectIdentifier> childWaferIDs = new ArrayList<>();
            for (int i = 0; i < childLotWaferSize; i++) {
                childWaferIDs.add(lotWaferAttributesList.get(i).getWaferID());
            }
            params.setChildWaferIDs(childWaferIDs);
        }

        //【step2】split
        params.setUser(testUtils.getUser());
        params.setParentLotID(splitWithNotOnRouteInfo.getParentID());
        params.setParentLotWaferCount(splitWithNotOnRouteInfo.getParentLotWaferCount());
        params.setChildLotWaferCount(splitWithNotOnRouteInfo.getChildLotWaferSize());

        return (ObjectIdentifier) lotController.splitLotNotOnPfReq(params).getBody();
    }

    public ObjectIdentifier splitToSubRoute() {
        //【step1】stb one product lot and skip the step which has sub sub route
        String bankID = "BNK-0S";
        String sourceProductID = "RAW-2000.01";
        String productID = "PRODUCT0.01";
        String skipOperationNumber = "2000.0400";
        TestInfos.StbInfo stbInfo = new TestInfos.StbInfo(bankID, sourceProductID, productID, 25L, false);
        ObjectIdentifier lotID = testUtils.stbAndSkip(stbInfo, skipOperationNumber);

        //【step2】get cassetteID
        ObjectIdentifier cassetteID = testUtils.getCassetteIDByLotID(lotID);

        //【step2】get the sub route info
        Params.MultiPathListInqParams multiPathListInqParams = new Params.MultiPathListInqParams();
        multiPathListInqParams.setUser(testUtils.getUser());
        multiPathListInqParams.setCassetteID(cassetteID);
        multiPathListInqParams.setRouteType("Branch");
        multiPathListInqParams.setLotID(lotID);
        List<Infos.ConnectedRouteList> body = (List<Infos.ConnectedRouteList>) lotInqController.multiPathListInq(multiPathListInqParams).getBody();
        Validations.check(CimArrayUtils.isEmpty(body), "the lot has sub route on 2000.0400 ...");
        ObjectIdentifier subRouteID = body.get(0).getRouteID();
        String returnOperationNumber = body.get(0).getReturnOperationNumber();

        //【step3】make split
        TestInfos.SplitInfo splitInfo = new TestInfos.SplitInfo();
        splitInfo.setLotID(lotID);
        splitInfo.setSubRouteID(subRouteID);
        splitInfo.setReturnOperationNumber(returnOperationNumber);
        splitInfo.setChildLotWaferSize(3);
        return this.split_norm(splitInfo);
    }

    public ObjectIdentifier splitWithEIStatus() {
        //【step1】stb one product lot
        String bankID = "BNK-0S";
        String sourceProductID = "RAW-2000.01";
        String productID = "PRODUCT0.01";
        TestInfos.StbInfo stbInfo = new TestInfos.StbInfo(bankID, sourceProductID, productID, 25L, false);
        ObjectIdentifier lotID = stbCase.STB_Normal(stbInfo);

        //【step2】load the cassette to make the Cassette Xfer Status is EI
        lotLoadToEquipmentCase.manual_Loading_Without_StartLotsReservation(lotID, new ObjectIdentifier("1SRT03"), new ObjectIdentifier("P1"));

        //【step3】make split
        TestInfos.SplitInfo splitInfo = new TestInfos.SplitInfo();
        splitInfo.setLotID(lotID);
        splitInfo.setChildLotWaferSize(3);
        try {
            this.split_norm(splitInfo);
        } catch (ServiceException ex) {
            Validations.check(!Validations.isEquals(ex.getCode(), retCodeConfig.getInvalidCassetteTransferState()), ex.getMessage());
        }

        return lotID;
    }

    public ObjectIdentifier splitWithProcessingStatus() {
        //【step1】stb one product lot
        String bankID = "BNK-0S";
        String sourceProductID = "RAW-2000.01";
        String productID = "PRODUCT0.01";
        TestInfos.StbInfo stbInfo = new TestInfos.StbInfo(bankID, sourceProductID, productID, 25L, false);
        ObjectIdentifier lotID = stbCase.STB_Normal(stbInfo);

        //【step2】make the lot is in-processing
        ObjectIdentifier equipmentID = new ObjectIdentifier("1SRT03");
        ObjectIdentifier portID = new ObjectIdentifier("P1");
        //【step2-1】load the lot
        lotLoadToEquipmentCase.manual_Loading_Without_StartLotsReservation(lotID, equipmentID, portID);

        //【step2-2】get the lot cassette
        ObjectIdentifier cassetteID = testUtils.getCassetteIDByLotID(lotID);

        //【step2-3】move in the lot
        List<ObjectIdentifier> cassetteIDList = new ArrayList<>();
        cassetteIDList.add(cassetteID);
        moveInCase.onlyMoveIn(cassetteIDList, equipmentID);

        //【step3】make split
        TestInfos.SplitInfo splitInfo = new TestInfos.SplitInfo();
        splitInfo.setLotID(lotID);
        splitInfo.setChildLotWaferSize(3);
        try {
            this.split_norm(splitInfo);
        } catch (ServiceException ex) {
            Validations.check(!Validations.isEquals(ex.getCode(), retCodeConfig.getInvalidLotProcessState()), ex.getMessage());
        }

        return lotID;
    }

    public ObjectIdentifier splitWithSecondTime() {
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
        ObjectIdentifier childLotID1 = this.split_norm(splitInfo);

        splitInfo.setStartPosition(10);
        ObjectIdentifier childLotID2 = this.split_norm(splitInfo);


        //【step4】make split from child lot
        TestInfos.SplitInfo splitInfo1 = new TestInfos.SplitInfo();
        splitInfo1.setLotID(childLotID1);
        splitInfo1.setChildLotWaferSize(2);
        ObjectIdentifier childLotID3 = this.split_norm(splitInfo1);

        //【step5】merge the child lot 1 & 2 & 3
        lotMergeCase.merge_norm(lotID, childLotID1);
        lotMergeCase.merge_norm(lotID, childLotID2);
        lotMergeCase.merge_norm(lotID, childLotID3);

        //【step6】split once again
        //-------------------------------------------------------------------------
        splitInfo = new TestInfos.SplitInfo();
        splitInfo.setLotID(lotID);
        splitInfo.setChildLotWaferSize(5);
        //【step6-1】split the parent lot
        ObjectIdentifier childLotID4 = this.split_norm(splitInfo);

        //【step6-2】split the parent lot once again
        splitInfo.setStartPosition(10);
        ObjectIdentifier childLotID5 = this.split_norm(splitInfo);

        //【step6-3】split the child lot
        TestInfos.SplitInfo splitInfo2 = new TestInfos.SplitInfo();
        splitInfo2.setLotID(childLotID4);
        splitInfo2.setChildLotWaferSize(2);
        this.split_norm(splitInfo2);
        //-------------------------------------------------------------------------
        return lotID;
    }

    public ObjectIdentifier splitWithFutureHoldRegistrated() {
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


        //【step3】make split from lot which has future hold
        TestInfos.SplitInfo splitInfo = new TestInfos.SplitInfo();
        splitInfo.setLotID(lotID);
        splitInfo.setChildLotWaferSize(5);
        ObjectIdentifier childLotID = this.split_norm(splitInfo);

        //【step4】check the child lot, the future hold should be inherit
        Params.FutureHoldListInqParams params = new Params.FutureHoldListInqParams();
        params.setCount(10);
        Infos.FutureHoldSearchKey futureHoldSearchKey = new Infos.FutureHoldSearchKey();
        futureHoldSearchKey.setLotID(childLotID);

        params.setFutureHoldSearchKey(futureHoldSearchKey);
        params.setUser(testUtils.getUser());
        SearchCondition searchCondition = new SearchCondition();
        searchCondition.setPage(0);
        searchCondition.setSize(10);
        searchCondition.setSortDirection(false);
        params.setSearchCondition(searchCondition);
        Page<Infos.FutureHoldListAttributes> body = (Page<Infos.FutureHoldListAttributes>) processInqController.futureHoldListInq(params).getBody();
        List<Infos.FutureHoldListAttributes> content = body.getContent();
        Validations.check(CimArrayUtils.isEmpty(content), "the child lot has no future hold, but it should be inherited from parent lot");
        return lotID;
    }

    public ObjectIdentifier splitWithLotNote() {
        //【step1】stb one product lot
        String bankID = "BNK-0S";
        String sourceProductID = "RAW-2000.01";
        String productID = "PRODUCT0.01";
        TestInfos.StbInfo stbInfo = new TestInfos.StbInfo(bankID, sourceProductID, productID, 25L, false);
        ObjectIdentifier lotID = stbCase.STB_Normal(stbInfo);


        //【step2】make lot note
        Infos.LotNoteInfo lotNoteInfo = new Infos.LotNoteInfo();
        lotNoteInfo.setReportUserID(lotID);
        lotNoteInfo.setLotNoteDescription("this is lot note description");
        lotNoteInfo.setLotNoteTitle("Lot Note Tile");
        lotNoteCase.lotNote_norm(lotNoteInfo);

        //【step3】make split from lot which has lot note
        TestInfos.SplitInfo splitInfo = new TestInfos.SplitInfo();
        splitInfo.setLotID(lotID);
        splitInfo.setChildLotWaferSize(5);
        ObjectIdentifier childLotID = this.split_norm(splitInfo);

        //【step4】check the child lot, the lot note should be inherit
        Params.LotMemoInfoInqParams lotMemoInfoInqParams = new Params.LotMemoInfoInqParams();
        lotMemoInfoInqParams.setUser(testUtils.getUser());
        lotMemoInfoInqParams.setLotID(childLotID);
        Results.LotMemoInfoInqResult body = (Results.LotMemoInfoInqResult) electronicInformationInqController.lotMemoInfoInq(lotMemoInfoInqParams).getBody();
        Validations.check(CimArrayUtils.isEmpty(body.getLotNoteInfos()), "the child lot has no future hold, but it should be inherited from parent lot");
        return lotID;
    }

    public ObjectIdentifier splitWithLotComment() {
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
        ObjectIdentifier childLotID = this.split_norm(splitInfo);

        //【step4】check the child lot, the lot comment should be inherit
        lotAnnotationInqParams = new Params.LotAnnotationInqParams();
        lotAnnotationInqParams.setUser(testUtils.getUser());
        lotAnnotationInqParams.setLotID(childLotID);
        Results.LotAnnotationInqResult childLotComment = (Results.LotAnnotationInqResult) electronicInformationInqController.lotAnnotationInq(lotAnnotationInqParams).getBody();
        int childLotCommentSize = CimArrayUtils.getSize(parentLotComment.getLotCommentInfos());

        Validations.check(parentLotCommentSize != childLotCommentSize, "the child lot don't inherit lot comment from parent lot");
        return lotID;
    }

    public ObjectIdentifier splitWithMonitorGroup() {
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
        ObjectIdentifier childLotID = this.split_norm(splitInfo);

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

        //【step6】make split from monitor lot which has monitor group
        splitInfo = new TestInfos.SplitInfo();
        splitInfo.setLotID(monitorLotID);
        splitInfo.setChildLotWaferSize(1);
        ObjectIdentifier childMonitorLotID = this.split_norm(splitInfo);

        //【step7】check the child monitor lot has monitor group or not, the child monitor lot shoud not inherit monitor group info from parent lot
        //【step7-1】get the child monitor lot - cassette ID
        ObjectIdentifier childMonitorCassetteID = testUtils.getCassetteIDByLotID(childMonitorLotID);

        //【step7-2】get the child lot monitor group info
        monitorBatchRelationInqParams = new Params.MonitorBatchRelationInqParams();
        monitorBatchRelationInqParams.setUser(testUtils.getUser());
        monitorBatchRelationInqParams.setCassetteID(childMonitorCassetteID);
        monitorBatchRelationInqParams.setLotID(childMonitorLotID);
        body = (List<Infos.MonitorGroups>) processMonitorInqController.monitorBatchRelationInq(monitorBatchRelationInqParams).getBody();
        Validations.check(!CimArrayUtils.isEmpty(body), "the child monitor lot needn't inherit monitor group info");

        return lotID;
    }

    public ObjectIdentifier splitWithLotOperationNote() {
        //【step1】stb one product lot
        String bankID = "BNK-0S";
        String sourceProductID = "RAW-2000.01";
        String productID = "PRODUCT0.01";
        TestInfos.StbInfo stbInfo = new TestInfos.StbInfo(bankID, sourceProductID, productID, 25L, false);
        ObjectIdentifier lotID = stbCase.STB_Normal(stbInfo);

        //【step2】make lot operation note
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

        //【step2-3】check whether parent lot has lot operation note or not
        Params.LotOpeMemoListInqParams lotOpeMemoListInqParams = new Params.LotOpeMemoListInqParams();
        lotOpeMemoListInqParams.setUser(testUtils.getUser());
        lotOpeMemoListInqParams.setLotID(lotID);
        Results.LotOpeMemoListInqResult body = (Results.LotOpeMemoListInqResult) electronicInformationInqController.lotOpeMemoListInq(lotOpeMemoListInqParams).getBody();
        Assert.isTrue(CimArrayUtils.isNotEmpty(body.getLotOperationNotesList()), "the parent lot don't have lot operation note");


        //【step3】make split from lot which has lot operation note
        TestInfos.SplitInfo splitInfo = new TestInfos.SplitInfo();
        splitInfo.setLotID(lotID);
        splitInfo.setChildLotWaferSize(5);
        ObjectIdentifier childLotID = this.split_norm(splitInfo);

        //【step4】check the child lot, the lot operaion note should be inherit
        lotOpeMemoListInqParams = new Params.LotOpeMemoListInqParams();
        lotOpeMemoListInqParams.setUser(testUtils.getUser());
        lotOpeMemoListInqParams.setLotID(childLotID);
        body = (Results.LotOpeMemoListInqResult) electronicInformationInqController.lotOpeMemoListInq(lotOpeMemoListInqParams).getBody();
        Assert.isTrue(CimArrayUtils.isNotEmpty(body.getLotOperationNotesList()), "the child lot don't inherit lot operation noted from parent lot");

        return lotID;
    }

    public ObjectIdentifier splitWithControlJob() {
        //【step1】stb one product lot
        String bankID = "BNK-0S";
        String sourceProductID = "RAW-2000.01";
        String productID = "PRODUCT0.01";
        TestInfos.StbInfo stbInfo = new TestInfos.StbInfo(bankID, sourceProductID, productID, 25L, false);
        ObjectIdentifier lotID = stbCase.STB_Normal(stbInfo);


        //【step2】make start reservation
        ObjectIdentifier equipmentID = new ObjectIdentifier("1SRT03");
        startLotsReservationCase.startLotsReserve(lotID, equipmentID);

        //【step3】make split from lot which has control job， it will throw (1299, The lot %s has control job ID %s.)
        TestInfos.SplitInfo splitInfo = new TestInfos.SplitInfo();
        splitInfo.setLotID(lotID);
        splitInfo.setChildLotWaferSize(5);
        try {
            ObjectIdentifier childLotID = this.split_norm(splitInfo);
        } catch (ServiceException e) {
            Validations.check(!Validations.isEquals(e.getCode(), retCodeConfig.getLotControlJobidFilled()), e.getMessage());
        }
        return lotID;
    }

}