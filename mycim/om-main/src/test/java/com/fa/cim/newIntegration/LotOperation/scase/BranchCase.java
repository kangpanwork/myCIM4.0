package com.fa.cim.newIntegration.LotOperation.scase;

import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.Response;
import com.fa.cim.common.utils.Validations;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;
import com.fa.cim.newIntegration.common.TestUtils;
import com.fa.cim.newIntegration.dto.TestInfos;
import com.fa.cim.newIntegration.equipment.scase.MoveInCase;
import com.fa.cim.newIntegration.stb.scase.STBCase;
import com.fa.cim.newIntegration.tcase.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * description:
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/12/5                              Neyo                create file
 *
 * @author: Neyo
 * @date: 2019/12/5 11:18
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@Service
public class BranchCase {

    @Autowired
    private RetCodeConfig retCodeConfig;
    @Autowired
    private CommonTestCase commonTestCase;
    @Autowired
    private BranchTestCase branchTestCase;
    @Autowired
    private TestUtils testUtils;
    @Autowired
    private ProcessFlowTestCase processFlowTestCase;
    @Autowired
    private LotSubRouteBranchTestCase lotSubRouteBranchTestCase;
    @Autowired
    private LotHoldCase lotHoldCase;
    @Autowired
    private LotTestCase lotTestCase;
    @Autowired
    private LotGeneralTestCase lotGeneralTestCase;
    @Autowired
    private FutureHoldTestCase futureHoldTestCase;
    @Autowired
    private STBCase stbCase;
    @Autowired
    private EquipmentTestCase equipmentTestCase;
    @Autowired
    private MoveInCase moveInCase;


    public Response subRouteBranchReq(Infos.LotInfo lotInfo) {
        List<Infos.ConnectedRouteList> connectedRouteList = commonTestCase.getConnectedRouteList(lotInfo.getLotBasicInfo().getLotID(), lotInfo.getLotLocationInfo().getCassetteID(), "");
        Params.SubRouteBranchReqParams params = new Params.SubRouteBranchReqParams();
        params.setClaimMemo("");
        params.setCurrentOperationNumber(lotInfo.getLotOperationInfo().getOperationNumber());
        params.setCurrentRouteID(lotInfo.getLotOperationInfo().getRouteID());
        params.setLotID(lotInfo.getLotBasicInfo().getLotID());
        params.setReturnOperationNumber(connectedRouteList.get(0).getReturnOperationNumber());
        params.setSubRouteID(connectedRouteList.get(0).getRouteID());
        params.setUser(testUtils.getUser());
        return branchTestCase.subRouteBranchReqCase(params);
    }

    public Response branchCancelReq(Infos.LotInfo lotInfo) {
        Params.BranchCancelReqParams params = new Params.BranchCancelReqParams();
        params.setClaimMemo("");
        params.setCurrentOperationNumber(lotInfo.getLotOperationInfo().getOperationNumber());
        params.setCurrentRouteID(lotInfo.getLotOperationInfo().getRouteID());
        params.setLotID(lotInfo.getLotBasicInfo().getLotID());
        params.setUser(testUtils.getUser());
        return branchTestCase.branchCancelReqCase(params);
    }

    /**
     * description:Branch from Production route to Branch route
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param -
     * @return void
     * @author Nyx
     * @date 2019/12/12 16:41
     */
    public void branchFromProductionRouteToBranchRoute() {
        String bankID = "BNK-0S";
        String sourceProductID = "RAW-2000.01";
        String productID = "PRODUCT0.01";
        String operationNumber = "2000.0400";
        TestInfos.StbInfo stbInfo = new TestInfos.StbInfo(bankID, sourceProductID, productID, 25L, false);
        ObjectIdentifier lotID = testUtils.stbAndSkip(stbInfo, operationNumber);

        //【step2】get the cassette id by lotID
        ObjectIdentifier cassetteID = testUtils.getCassetteIDByLotID(lotID);

        //pflow/connected_route_list/inq
        List<Infos.ConnectedRouteList> connectedRouteLists = processFlowTestCase.multiPathListInqCase(cassetteID, lotID);
        Infos.ConnectedRouteList connectedRouteList = connectedRouteLists.get(0);

        //lot/sub_route_branch/req
        lotSubRouteBranchTestCase.subRouteBranchReq(lotID, connectedRouteList.getReturnRouteID(), operationNumber,
                connectedRouteList.getRouteID(), connectedRouteList.getReturnOperationNumber());
    }

    /**
     * description:Branch from Branch route to Branch route
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param -
     * @return void
     * @author Nyx
     * @date 2019/12/12 16:41
     */
    public void branchFromBranchRouteToBranchRoute() {
        String bankID = "BNK-0S";
        String sourceProductID = "RAW-2000.01";
        String productID = "PRODUCT0.01";
        String operationNumber = "2000.0400";
        TestInfos.StbInfo stbInfo = new TestInfos.StbInfo(bankID, sourceProductID, productID, 25L, false);
        ObjectIdentifier lotID = testUtils.stbAndSkip(stbInfo, operationNumber);

        //【step2】get the cassette id by lotID
        ObjectIdentifier cassetteID = testUtils.getCassetteIDByLotID(lotID);

        //pflow/connected_route_list/inq
        List<Infos.ConnectedRouteList> connectedRouteLists = processFlowTestCase.multiPathListInqCase(cassetteID, lotID);
        Infos.ConnectedRouteList connectedRouteList = connectedRouteLists.get(0);

        //lot/sub_route_branch/req
        lotSubRouteBranchTestCase.subRouteBranchReq(lotID, connectedRouteList.getReturnRouteID(), operationNumber,
                connectedRouteList.getRouteID(), connectedRouteList.getReturnOperationNumber());

        //lot/ope_locate/req
        testUtils.skip(lotID, "1000.0200");

        //einfo/lot_info/inq
        Results.LotInfoInqResult lotInfoInqResult = (Results.LotInfoInqResult) lotGeneralTestCase.getLotInfoCase(Arrays.asList(lotID)).getBody();

        //pflow/connected_route_list/inq
        List<Infos.ConnectedRouteList> connectedRouteLists1 = processFlowTestCase.multiPathListInqCase(cassetteID, lotID);
        Infos.ConnectedRouteList connectedRouteList1 = connectedRouteLists1.get(0);

        //lot/sub_route_branch/req
        Infos.LotOperationInfo lotOperationInfo = lotInfoInqResult.getLotInfoList().get(0).getLotOperationInfo();
        lotSubRouteBranchTestCase.subRouteBranchReq(lotID, lotOperationInfo.getRouteID(), lotOperationInfo.getOperationNumber(),
                connectedRouteList1.getRouteID(), connectedRouteList1.getReturnOperationNumber());

        //lot/ope_locate/req
        testUtils.skip(lotID, "2000.0200");
    }

    /**
     * description:Branch a onhold lot with hold release
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param -
     * @return void
     * @author Nyx
     * @date 2019/12/12 17:05
     */
    public void branchAOnholdLotWithHoldRelease() {
        String bankID = "BNK-0S";
        String sourceProductID = "RAW-2000.01";
        String productID = "PRODUCT0.01";
        String operationNumber = "2000.0400";
        TestInfos.StbInfo stbInfo = new TestInfos.StbInfo(bankID, sourceProductID, productID, 25L, false);
        ObjectIdentifier lotID = testUtils.stbAndSkip(stbInfo, operationNumber);

        //【step2】get the cassette id by lotID
        ObjectIdentifier cassetteID = testUtils.getCassetteIDByLotID(lotID);

        //lot/hold_lot/req
        lotHoldCase.lotHoldOnCurrent(cassetteID);

        //pflow/connected_route_list/inq
        List<Infos.ConnectedRouteList> connectedRouteLists = processFlowTestCase.multiPathListInqCase(cassetteID, lotID);
        Infos.ConnectedRouteList connectedRouteList = connectedRouteLists.get(0);

        //pflow/branch_with_hold_release/req
        Infos.BranchReq branchReq = new Infos.BranchReq();
        branchReq.setCurrentRouteID(connectedRouteList.getReturnRouteID());
        branchReq.setCurrentOperationNumber(operationNumber);
        branchReq.setLotID(lotID);
        branchReq.setSubRouteID(connectedRouteList.getRouteID());
        branchReq.setReturnOperationNumber(connectedRouteList.getReturnOperationNumber());

        ObjectIdentifier releaseReasonCodeID = new ObjectIdentifier("QTCL", "FRCODE.72092590579781889");

        Results.HoldLotListInqResult holdLotListInqResult = lotTestCase.HoldLotListInqCase(lotID);
        Infos.LotHoldListAttributes lotHoldListAttributes = holdLotListInqResult.getLotHoldListAttributes().getContent().get(0);

        List<Infos.LotHoldReq> lotHoldReqs = new ArrayList<>();
        Infos.LotHoldReq lotHoldReq = new Infos.LotHoldReq();
        lotHoldReq.setHoldReasonCodeID(lotHoldListAttributes.getReasonCodeID());
        lotHoldReq.setHoldType(lotHoldListAttributes.getHoldType());
        lotHoldReq.setHoldUserID(lotHoldListAttributes.getUserID());
        lotHoldReq.setResponsibleOperationMark(lotHoldListAttributes.getResponsibleOperationMark());
        lotHoldReqs.add(lotHoldReq);
        processFlowTestCase.branchWithHoldReleaseReqCase(branchReq, releaseReasonCodeID, lotHoldReqs);

    }

    public void branchFromProductionFutureRouteToBranchRouteOutOfTheJumpStep() {
        String bankID = "BNK-0S";
        String sourceProductID = "RAW-2000.01";
        String productID = "PRODUCT0.01";
        String operationNumber = "2000.0400";
        TestInfos.StbInfo stbInfo = new TestInfos.StbInfo(bankID, sourceProductID, productID);
        ObjectIdentifier lotID = stbCase.STB_Normal(stbInfo);

        //pctl/enhanced_future_hold/req
        ObjectIdentifier reasonCodeID = new ObjectIdentifier("SHLS");
        futureHoldTestCase.futureHoldRegister(lotID, operationNumber, false, false, reasonCodeID, new ObjectIdentifier(), new ObjectIdentifier("LAYER0MA.01"), "FutureHold");

        //【step2】get the cassette id by lotID
        ObjectIdentifier cassetteID = testUtils.getCassetteIDByLotID(lotID);

        //lot/ope_locate/req
        testUtils.skip(lotID, operationNumber);

        //pflow/connected_route_list/inq
        List<Infos.ConnectedRouteList> connectedRouteLists = processFlowTestCase.multiPathListInqCase(cassetteID, lotID);
        Infos.ConnectedRouteList connectedRouteList = connectedRouteLists.get(0);

        //pflow/branch_with_hold_release/req
        Infos.BranchReq branchReq = new Infos.BranchReq();
        branchReq.setCurrentRouteID(connectedRouteList.getReturnRouteID());
        branchReq.setCurrentOperationNumber(operationNumber);
        branchReq.setLotID(lotID);
        branchReq.setSubRouteID(connectedRouteList.getRouteID());
        branchReq.setReturnOperationNumber(connectedRouteList.getReturnOperationNumber());

        ObjectIdentifier releaseReasonCodeID = new ObjectIdentifier("QTCL", "FRCODE.72092590579781889");

        Results.HoldLotListInqResult holdLotListInqResult = lotTestCase.HoldLotListInqCase(lotID);
        Infos.LotHoldListAttributes lotHoldListAttributes = holdLotListInqResult.getLotHoldListAttributes().getContent().get(0);

        List<Infos.LotHoldReq> lotHoldReqs = new ArrayList<>();
        Infos.LotHoldReq lotHoldReq = new Infos.LotHoldReq();
        lotHoldReq.setHoldReasonCodeID(lotHoldListAttributes.getReasonCodeID());
        lotHoldReq.setHoldType(lotHoldListAttributes.getHoldType());
        lotHoldReq.setHoldUserID(lotHoldListAttributes.getUserID());
        lotHoldReq.setResponsibleOperationMark(lotHoldListAttributes.getResponsibleOperationMark());
        lotHoldReqs.add(lotHoldReq);
        processFlowTestCase.branchWithHoldReleaseReqCase(branchReq, releaseReasonCodeID, lotHoldReqs);

    }

    public void branchFromProductionFutureRouteToBranchRouteInTheJumpStep_exception() {
        String bankID = "BKP-RMAT";
        String sourceProductID = "DEV-1000.01";
        String productID = "DEV-12SOIAA001.01";
        TestInfos.StbInfo stbInfo = new TestInfos.StbInfo(bankID, sourceProductID, productID);
        ObjectIdentifier lotID = stbCase.STB_Normal(stbInfo);

        //pctl/enhanced_future_hold/req
        ObjectIdentifier reasonCodeID = new ObjectIdentifier("SHLS");
        futureHoldTestCase.futureHoldRegister(lotID, "2000.0200", false, false, reasonCodeID, new ObjectIdentifier(), new ObjectIdentifier("DEV_SOI.01"), "FutureHold");

        //【step2】get the cassette id by lotID
        ObjectIdentifier cassetteID = testUtils.getCassetteIDByLotID(lotID);

        //lot/ope_locate/req
        testUtils.skip(lotID, "2000.0100");

        //einfo/lot_info/inq
        Results.LotInfoInqResult lotInfoInqResult = (Results.LotInfoInqResult) lotGeneralTestCase.getLotInfoCase(Arrays.asList(lotID)).getBody();
        Infos.LotOperationInfo lotOperationInfo = lotInfoInqResult.getLotInfoList().get(0).getLotOperationInfo();

        //pflow/connected_route_list/inq
        List<Infos.ConnectedRouteList> connectedRouteLists = processFlowTestCase.multiPathListInqCase(cassetteID, lotID);
        Infos.ConnectedRouteList connectedRouteList = connectedRouteLists.get(0);

        //lot/sub_route_branch/req
        try {
            lotSubRouteBranchTestCase.subRouteBranchReq(lotID, lotOperationInfo.getRouteID(), lotOperationInfo.getOperationNumber(),
                    connectedRouteList.getRouteID(), connectedRouteList.getReturnOperationNumber());
        } catch (ServiceException e) {
            if (!Validations.isEquals(e.getCode(), retCodeConfig.getFutureHoldInBranch())) {
                throw e;
            }
        }

    }

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param -
     * @return void
     * @author Nyx
     * @date 2019/12/13 16:45
     */
    public void branchCancelAtTheFirstStepFromBranchRoute() {
        String bankID = "BNK-0S";
        String sourceProductID = "RAW-2000.01";
        String productID = "PRODUCT0.01";
        String operationNumber = "2000.0400";
        TestInfos.StbInfo stbInfo = new TestInfos.StbInfo(bankID, sourceProductID, productID, 25L, false);
        ObjectIdentifier lotID = testUtils.stbAndSkip(stbInfo, operationNumber);

        //【step2】get the cassette id by lotID
        ObjectIdentifier cassetteID = testUtils.getCassetteIDByLotID(lotID);

        //pflow/connected_route_list/inq
        List<Infos.ConnectedRouteList> connectedRouteLists = processFlowTestCase.multiPathListInqCase(cassetteID, lotID);
        Infos.ConnectedRouteList connectedRouteList = connectedRouteLists.get(0);

        //lot/sub_route_branch/req
        lotSubRouteBranchTestCase.subRouteBranchReq(lotID, connectedRouteList.getReturnRouteID(), operationNumber,
                connectedRouteList.getRouteID(), connectedRouteList.getReturnOperationNumber());

        //einfo/lot_info/inq
        Results.LotInfoInqResult lotInfoInqResult = (Results.LotInfoInqResult) lotGeneralTestCase.getLotInfoCase(Arrays.asList(lotID)).getBody();
        Infos.LotOperationInfo lotOperationInfo = lotInfoInqResult.getLotInfoList().get(0).getLotOperationInfo();

        //lot/sub_route_branch_cancel/req
        lotSubRouteBranchTestCase.branchCancelReq(lotID, lotOperationInfo.getRouteID(), lotOperationInfo.getOperationNumber());
    }

    public void branchCancelAtTheFirstStepFromBranchRouteMoveIn_exception() {
        String bankID = "BNK-0S";
        String sourceProductID = "RAW-2000.01";
        String productID = "PRODUCT0.01";
        String operationNumber = "2000.0400";
        TestInfos.StbInfo stbInfo = new TestInfos.StbInfo(bankID, sourceProductID, productID, 25L, false);
        ObjectIdentifier lotID = testUtils.stbAndSkip(stbInfo, operationNumber);

        //【step2】get the cassette id by lotID
        ObjectIdentifier cassetteID = testUtils.getCassetteIDByLotID(lotID);

        //pflow/connected_route_list/inq
        List<Infos.ConnectedRouteList> connectedRouteLists = processFlowTestCase.multiPathListInqCase(cassetteID, lotID);
        Infos.ConnectedRouteList connectedRouteList = connectedRouteLists.get(0);

        //lot/sub_route_branch/req
        lotSubRouteBranchTestCase.subRouteBranchReq(lotID, connectedRouteList.getReturnRouteID(), operationNumber,
                connectedRouteList.getRouteID(), connectedRouteList.getReturnOperationNumber());

        //eqp/carrier_loading/rpt
        //-----------load---------------
        ObjectIdentifier equipmentID = new ObjectIdentifier("CMP0102");
        equipmentTestCase.carrierLoadingRpt(equipmentID, cassetteID, new ObjectIdentifier("P1"), BizConstant.SP_LOADPURPOSETYPE_PROCESSLOT);

        //eqp/move_in/req
        //-----------moveIn---------------
        moveInCase.onlyMoveIn(Arrays.asList(cassetteID), equipmentID).getBody();

        //einfo/lot_info/inq
        Results.LotInfoInqResult lotInfoInqResult = (Results.LotInfoInqResult) lotGeneralTestCase.getLotInfoCase(Arrays.asList(lotID)).getBody();
        Infos.LotOperationInfo lotOperationInfo = lotInfoInqResult.getLotInfoList().get(0).getLotOperationInfo();

        //lot/sub_route_branch_cancel/req
        try {
            lotSubRouteBranchTestCase.branchCancelReq(lotID, lotOperationInfo.getRouteID(), lotOperationInfo.getOperationNumber());
        } catch (ServiceException e) {
            if (!Validations.isEquals(e.getCode(), retCodeConfig.getInvalidLotProcessState())) {
                throw e;
            }
        }
    }

    public void branchCancelNotAtTheFirstStepFromBranchRoute_exception() {
        String bankID = "BNK-0S";
        String sourceProductID = "RAW-2000.01";
        String productID = "PRODUCT0.01";
        String operationNumber = "2000.0400";
        TestInfos.StbInfo stbInfo = new TestInfos.StbInfo(bankID, sourceProductID, productID, 25L, false);
        ObjectIdentifier lotID = testUtils.stbAndSkip(stbInfo, operationNumber);

        //【step2】get the cassette id by lotID
        ObjectIdentifier cassetteID = testUtils.getCassetteIDByLotID(lotID);

        //pflow/connected_route_list/inq
        List<Infos.ConnectedRouteList> connectedRouteLists = processFlowTestCase.multiPathListInqCase(cassetteID, lotID);
        Infos.ConnectedRouteList connectedRouteList = connectedRouteLists.get(0);

        //lot/sub_route_branch/req
        lotSubRouteBranchTestCase.subRouteBranchReq(lotID, connectedRouteList.getReturnRouteID(), operationNumber,
                connectedRouteList.getRouteID(), connectedRouteList.getReturnOperationNumber());

        //lot/ope_locate/req
        testUtils.skip(lotID, "1000.0200");

        //einfo/lot_info/inq
        Results.LotInfoInqResult lotInfoInqResult = (Results.LotInfoInqResult) lotGeneralTestCase.getLotInfoCase(Arrays.asList(lotID)).getBody();
        Infos.LotOperationInfo lotOperationInfo = lotInfoInqResult.getLotInfoList().get(0).getLotOperationInfo();

        //lot/sub_route_branch_cancel/req
        try {
            lotSubRouteBranchTestCase.branchCancelReq(lotID, lotOperationInfo.getRouteID(), lotOperationInfo.getOperationNumber());
        } catch (ServiceException e) {
            if (!Validations.isEquals(e.getCode(), retCodeConfig.getAllAlreadyProcessed())) {
                throw e;
            }
        }
    }

    public void branchCancelWhenAFutureHoldInBranchRoute_exception() {
        String bankID = "BNK-0S";
        String sourceProductID = "RAW-2000.01";
        String productID = "PRODUCT0.01";
        String operationNumber = "2000.0400";
        TestInfos.StbInfo stbInfo = new TestInfos.StbInfo(bankID, sourceProductID, productID, 25L, false);
        ObjectIdentifier lotID = testUtils.stbAndSkip(stbInfo, operationNumber);

        //【step2】get the cassette id by lotID
        ObjectIdentifier cassetteID = testUtils.getCassetteIDByLotID(lotID);

        //pflow/connected_route_list/inq
        List<Infos.ConnectedRouteList> connectedRouteLists = processFlowTestCase.multiPathListInqCase(cassetteID, lotID);
        Infos.ConnectedRouteList connectedRouteList = connectedRouteLists.get(0);

        //lot/sub_route_branch/req
        lotSubRouteBranchTestCase.subRouteBranchReq(lotID, connectedRouteList.getReturnRouteID(), operationNumber,
                connectedRouteList.getRouteID(), connectedRouteList.getReturnOperationNumber());

        //pctl/enhanced_future_hold/req
        ObjectIdentifier reasonCodeID = new ObjectIdentifier("SHLS");
        futureHoldTestCase.futureHoldRegister(lotID, "1000.0200", false, false, reasonCodeID, new ObjectIdentifier(), new ObjectIdentifier("DEV_BRCH.01"), "FutureHold");

        //einfo/lot_info/inq
        Results.LotInfoInqResult lotInfoInqResult = (Results.LotInfoInqResult) lotGeneralTestCase.getLotInfoCase(Arrays.asList(lotID)).getBody();
        Infos.LotOperationInfo lotOperationInfo = lotInfoInqResult.getLotInfoList().get(0).getLotOperationInfo();

        //lot/sub_route_branch_cancel/req
        try {
            lotSubRouteBranchTestCase.branchCancelReq(lotID, lotOperationInfo.getRouteID(), lotOperationInfo.getOperationNumber());
        } catch (ServiceException e) {
            if (!Validations.isEquals(e.getCode(), retCodeConfig.getFutureholdOnBranch())) {
                throw e;
            }
        }
    }

    public void branchCancelFromProductionFutureRouteToBranchRoute() {
        String bankID = "BNK-0S";
        String sourceProductID = "RAW-2000.01";
        String productID = "PRODUCT0.01";
        String operationNumber = "2000.0400";
        TestInfos.StbInfo stbInfo = new TestInfos.StbInfo(bankID, sourceProductID, productID);
        ObjectIdentifier lotID = stbCase.STB_Normal(stbInfo);

        //pctl/enhanced_future_hold/req
        ObjectIdentifier reasonCodeID = new ObjectIdentifier("SHLS");
        futureHoldTestCase.futureHoldRegister(lotID, operationNumber, false, false, reasonCodeID, new ObjectIdentifier(), new ObjectIdentifier("LAYER0MA.01"), "FutureHold");

        //【step2】get the cassette id by lotID
        ObjectIdentifier cassetteID = testUtils.getCassetteIDByLotID(lotID);

        //lot/ope_locate/req
        testUtils.skip(lotID, operationNumber);

        //pflow/connected_route_list/inq
        List<Infos.ConnectedRouteList> connectedRouteLists = processFlowTestCase.multiPathListInqCase(cassetteID, lotID);
        Infos.ConnectedRouteList connectedRouteList = connectedRouteLists.get(0);

        //pflow/branch_with_hold_release/req
        Infos.BranchReq branchReq = new Infos.BranchReq();
        branchReq.setCurrentRouteID(connectedRouteList.getReturnRouteID());
        branchReq.setCurrentOperationNumber(operationNumber);
        branchReq.setLotID(lotID);
        branchReq.setSubRouteID(connectedRouteList.getRouteID());
        branchReq.setReturnOperationNumber(connectedRouteList.getReturnOperationNumber());

        ObjectIdentifier releaseReasonCodeID = new ObjectIdentifier("QTCL", "FRCODE.72092590579781889");

        Results.HoldLotListInqResult holdLotListInqResult = lotTestCase.HoldLotListInqCase(lotID);
        Infos.LotHoldListAttributes lotHoldListAttributes = holdLotListInqResult.getLotHoldListAttributes().getContent().get(0);

        List<Infos.LotHoldReq> lotHoldReqs = new ArrayList<>();
        Infos.LotHoldReq lotHoldReq = new Infos.LotHoldReq();
        lotHoldReq.setHoldReasonCodeID(lotHoldListAttributes.getReasonCodeID());
        lotHoldReq.setHoldType(lotHoldListAttributes.getHoldType());
        lotHoldReq.setHoldUserID(lotHoldListAttributes.getUserID());
        lotHoldReq.setResponsibleOperationMark(lotHoldListAttributes.getResponsibleOperationMark());
        lotHoldReqs.add(lotHoldReq);
        processFlowTestCase.branchWithHoldReleaseReqCase(branchReq, releaseReasonCodeID, lotHoldReqs);

        //einfo/lot_info/inq
        Results.LotInfoInqResult lotInfoInqResult = (Results.LotInfoInqResult) lotGeneralTestCase.getLotInfoCase(Arrays.asList(lotID)).getBody();
        Infos.LotOperationInfo lotOperationInfo = lotInfoInqResult.getLotInfoList().get(0).getLotOperationInfo();

        //lot/sub_route_branch_cancel/req
        lotSubRouteBranchTestCase.branchCancelReq(lotID, lotOperationInfo.getRouteID(), lotOperationInfo.getOperationNumber());
    }
}
