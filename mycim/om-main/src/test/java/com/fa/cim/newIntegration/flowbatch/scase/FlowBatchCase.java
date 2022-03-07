package com.fa.cim.newIntegration.flowbatch.scase;

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
import com.fa.cim.newIntegration.LotOperation.scase.LotHoldCase;
import com.fa.cim.newIntegration.LotOperation.scase.LotSplitCase;
import com.fa.cim.newIntegration.LotOperation.scase.OperationSkipCase;
import com.fa.cim.newIntegration.LotOperation.scase.ReworkCase;
import com.fa.cim.newIntegration.dto.TestInfos;
import com.fa.cim.newIntegration.equipment.scase.*;
import com.fa.cim.newIntegration.processControl.scase.EntityInhibityCase;
import com.fa.cim.newIntegration.stb.scase.STBCase;
import com.fa.cim.newIntegration.tcase.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/11/13       ********             lightyh             create file
 *
 * @author: lightyh
 * @date: 2019/11/13 12:57
 * @copyright: 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@Service
public class FlowBatchCase {

    @Autowired
    private FlowBatchTestCase flowBatchTestCase;

    @Autowired
    private CommonTestCase commonTestCase;

    @Autowired
    private STBCase stbCase;

    @Autowired
    private LotGeneralTestCase lotGeneralTestCase;

    @Autowired
    private OperationSkipTestCase operationSkipTestCase;

    @Autowired
    private StartLotsReservationCase startLotsReservationCase;

    @Autowired
    private LotLoadToEquipmentCase lotLoadToEquipmentCase;

    @Autowired
    private LotUnloadFromEquipmentCase lotUnloadFromEquipmentCase;

    @Autowired
    private ElectronicInformationTestCase electronicInformationTestCase;

    @Autowired
    private MoveInCase moveInCase;

    @Autowired
    private MoveInCancelCase moveInCancelCase;

    @Autowired
    private MoveOutCase moveOutCase;

    @Autowired
    private LotHoldCase lotHoldCase;

    @Autowired
    private EntityInhibityCase entityInhibityCase;

    @Autowired
    private ReworkCase reworkCase;

    @Autowired
    private LotSplitCase lotSplitCase;

    @Autowired
    private OperationSkipCase operationSkipCase;

    @Autowired
    private RetCodeConfig retCodeConfig;

    private static String FLOWBATCH_EQUIPMENTID = "FB105";

    private List<Infos.LotInfo> produceLotsAndSkipToTheFlowBatch(int number, boolean isFirst){
        // the cassette that do flowbatch
        List<Infos.LotInfo> lotInfoList = new ArrayList<>();
        // stb two lots,the skip to 4000.0100
        //【step1】 product n stb lots
        log.info("【step1】produce {} stb lots", number);
        List<ObjectIdentifier> lots = stbCase.stb_NLots_NotPreparedCase(number, isFirst);
        for (ObjectIdentifier lot : lots){
            //【step2】lot info
            log.info("【step2】lot info");
            List<ObjectIdentifier> lotIDList = new ArrayList<>();
            lotIDList.add(lot);
            Results.LotInfoInqResult lotInfoInqResult = (Results.LotInfoInqResult) lotGeneralTestCase.getLotInfoCase(lotIDList).getBody();
            Infos.LotInfo lotInfo = lotInfoInqResult.getLotInfoList().get(0);
            //【step3】get lot operation
            log.info("【step3】get lot operation");
            Results.LotOperationSelectionInqResult lotOperationSelectionInqResult1 = (Results.LotOperationSelectionInqResult) lotGeneralTestCase.getLotOperationSelectionInq(lot, false, true, true).getBody();
            List<Infos.OperationNameAttributes> operationNameAttributesList1 = lotOperationSelectionInqResult1.getOperationNameAttributesAttributes().getContent();
            Infos.OperationNameAttributes operationNameAttributes1 = null;
            for (Infos.OperationNameAttributes tmpOperationNameAttributes : operationNameAttributesList1){
                if (CimStringUtils.equals(tmpOperationNameAttributes.getOperationNumber(), "4000.0100")){
                    operationNameAttributes1 = tmpOperationNameAttributes;
                    break;
                }
            }
            Assert.isTrue(operationNameAttributes1 != null, "not find operationnumber");
            //【step4】skip
            log.info("【step4】skip");
            Params.SkipReqParams skipReqParams = new Params.SkipReqParams();
            skipReqParams.setCurrentOperationNumber(lotInfo.getLotOperationInfo().getOperationNumber());
            skipReqParams.setCurrentRouteID(lotInfo.getLotOperationInfo().getRouteID());
            skipReqParams.setLocateDirection(true);
            skipReqParams.setLotID(lot);
            skipReqParams.setOperationID(operationNameAttributes1.getOperationID());
            skipReqParams.setOperationNumber(operationNameAttributes1.getOperationNumber());
            skipReqParams.setProcessRef(operationNameAttributes1.getProcessRef());
            skipReqParams.setRouteID(operationNameAttributes1.getRouteID());
            skipReqParams.setSeqno(-1);
            skipReqParams.setSequenceNumber(0);
            operationSkipTestCase.operationSkip(skipReqParams);
            Results.LotInfoInqResult lotInfoInqResult2 = (Results.LotInfoInqResult) lotGeneralTestCase.getLotInfoCase(lotIDList).getBody();
            Infos.LotInfo lotInfo2 = lotInfoInqResult.getLotInfoList().get(0);
            lotInfoList.add(lotInfo2);
        }
        return lotInfoList;
    }

    private TestInfos.FlowBatchResultInfo productLotsAndDoFlowBatch(int number, int flowBatchSize){
        TestInfos.FlowBatchResultInfo flowBatchResultInfo = new TestInfos.FlowBatchResultInfo();
        //【step1】get stocker list
        Infos.StockerInfo stockerInfo = commonTestCase.getStocker();
        // 【step2】produce number lot
        List<Infos.LotInfo> lotInfoList = produceLotsAndSkipToTheFlowBatch(number, true);
        List<ObjectIdentifier> cassetteList = lotInfoList.stream().map(lotInfo -> lotInfo.getLotLocationInfo().getCassetteID()).collect(Collectors.toList());
        List<ObjectIdentifier> lots = lotInfoList.stream().map(lotInfo -> lotInfo.getLotBasicInfo().getLotID()).collect(Collectors.toList());
        flowBatchResultInfo.setCassetteList(cassetteList);
        flowBatchResultInfo.setLotList(lots);
        // 【step3】change transfer status
        for (ObjectIdentifier cassette : cassetteList){
            commonTestCase.lotCassetteXferStatusChange(cassette, "MI");
        }
        //【step4】candidate lots list again
        Params.FlowBatchLotSelectionInqParam flowBatchLotSelectionInqParam2 = new Params.FlowBatchLotSelectionInqParam();
        flowBatchLotSelectionInqParam2.setEquipmentID(new ObjectIdentifier(FLOWBATCH_EQUIPMENTID));
        Results.FlowBatchLotSelectionInqResult flowBatchLotSelectionInqResult2 = flowBatchTestCase.getCandidateLotsList(flowBatchLotSelectionInqParam2);
        List<Infos.TempFlowBatch> strTempFlowBatch2 = flowBatchLotSelectionInqResult2.getStrTempFlowBatch();
        List<ObjectIdentifier> candidateCassettes = strTempFlowBatch2.stream().map(Infos.TempFlowBatch::getCassetteID).collect(Collectors.toList());
        Assert.isTrue(candidateCassettes.containsAll(cassetteList), "not found cassette");

        flowBatchResultInfo.setTempFlowBatchList(strTempFlowBatch2);
        // 【step5】do flowbatch
        List<Infos.FlowBatchByManualActionReqCassette> flowBatchByManualActionReqCassette1 = this.getFlowBatchByManualActionReqCassette(strTempFlowBatch2, cassetteList.subList(0, flowBatchSize));
        Params.FlowBatchByManualActionReqParam flowBatchByManualActionReqParam1 = new Params.FlowBatchByManualActionReqParam();
        flowBatchByManualActionReqParam1.setEquipmentID(new ObjectIdentifier(FLOWBATCH_EQUIPMENTID));
        flowBatchByManualActionReqParam1.setStrFlowBatchByManualActionReqCassette(flowBatchByManualActionReqCassette1);
        Results.FlowBatchByManualActionReqResult flowBatchByManualActionReqResult = (Results.FlowBatchByManualActionReqResult)flowBatchTestCase.flowBatchByManualActionReq(flowBatchByManualActionReqParam1).getBody();
        flowBatchResultInfo.setFlowBatchID(flowBatchByManualActionReqResult.getFlowBatchID());
        return flowBatchResultInfo;
    }


    private List<Infos.LotInfo> forceSkipSpecificStep(List<ObjectIdentifier> lots, String step, boolean locateDirection){
        boolean searchDirection = locateDirection;
        List<Infos.LotInfo> lotInfoList = new ArrayList<>();
        for (ObjectIdentifier lot : lots) {
            List<ObjectIdentifier> lotIDList = new ArrayList<>();
            lotIDList.add(lot);
            Results.LotInfoInqResult lotInfoInqResult = (Results.LotInfoInqResult) lotGeneralTestCase.getLotInfoCase(lotIDList).getBody();
            Infos.LotInfo lotInfo = lotInfoInqResult.getLotInfoList().get(0);
            Results.LotOperationSelectionInqResult lotOperationSelectionInqResult = (Results.LotOperationSelectionInqResult) lotGeneralTestCase.getLotOperationSelectionInq(lot, false, true, searchDirection).getBody();
            List<Infos.OperationNameAttributes> operationNameAttributesList1 = lotOperationSelectionInqResult.getOperationNameAttributesAttributes().getContent();
            Infos.OperationNameAttributes operationNameAttributes1 = null;
            for (Infos.OperationNameAttributes tmpOperationNameAttributes : operationNameAttributesList1) {
                if (CimStringUtils.equals(tmpOperationNameAttributes.getOperationNumber(), step)) {
                    operationNameAttributes1 = tmpOperationNameAttributes;
                    break;
                }
            }
            Assert.isTrue(operationNameAttributes1 != null, "not find operationnumber");
            Params.SkipReqParams skipReqParams = new Params.SkipReqParams();
            skipReqParams.setCurrentOperationNumber(lotInfo.getLotOperationInfo().getOperationNumber());
            skipReqParams.setCurrentRouteID(lotInfo.getLotOperationInfo().getRouteID());
            skipReqParams.setLocateDirection(locateDirection);
            skipReqParams.setLotID(lot);
            skipReqParams.setOperationID(operationNameAttributes1.getOperationID());
            skipReqParams.setOperationNumber(operationNameAttributes1.getOperationNumber());
            skipReqParams.setProcessRef(operationNameAttributes1.getProcessRef());
            skipReqParams.setRouteID(operationNameAttributes1.getRouteID());
            skipReqParams.setSeqno(-1);
            skipReqParams.setSequenceNumber(0);
            operationSkipTestCase.forceOperationSkip(skipReqParams);
            Results.LotInfoInqResult lotInfoInqResult2 = (Results.LotInfoInqResult) lotGeneralTestCase.getLotInfoCase(lotIDList).getBody();
            Infos.LotInfo lotInfo2 = lotInfoInqResult2.getLotInfoList().get(0);
            lotInfoList.add(lotInfo2);
        }
        return lotInfoList;
    }

    private void checkFlowBatchSkip(ObjectIdentifier lot, String step, boolean locateDirection){
        boolean searchDirection = locateDirection;
        List<ObjectIdentifier> lotIDList = Arrays.asList(lot);
        Results.LotInfoInqResult lotInfoInqResult = (Results.LotInfoInqResult) lotGeneralTestCase.getLotInfoCase(lotIDList).getBody();

        Results.LotOperationSelectionInqResult lotOperationSelectionInqResult = (Results.LotOperationSelectionInqResult) lotGeneralTestCase.getLotOperationSelectionInq(lot, false, true, searchDirection).getBody();
        List<Infos.OperationNameAttributes> operationNameAttributesList1 = lotOperationSelectionInqResult.getOperationNameAttributesAttributes().getContent();
        Infos.OperationNameAttributes operationNameAttributes1 = null;
        for (Infos.OperationNameAttributes tmpOperationNameAttributes : operationNameAttributesList1) {
            if (CimStringUtils.equals(tmpOperationNameAttributes.getOperationNumber(), step)) {
                operationNameAttributes1 = tmpOperationNameAttributes;
                break;
            }
        }
        Assert.isTrue(operationNameAttributes1 != null, "not find operationnumber");
        Params.FlowBatchCheckForLotSkipReqParams flowBatchCheckForLotSkipReqParams = new Params.FlowBatchCheckForLotSkipReqParams();
        flowBatchCheckForLotSkipReqParams.setLocateDirection(locateDirection);
        flowBatchCheckForLotSkipReqParams.setLotID(lot);
        flowBatchCheckForLotSkipReqParams.setOperationID(operationNameAttributes1.getOperationID());
        flowBatchCheckForLotSkipReqParams.setOperationNumber(operationNameAttributes1.getOperationNumber());
        flowBatchCheckForLotSkipReqParams.setProcessRef(operationNameAttributes1.getProcessRef());
        flowBatchCheckForLotSkipReqParams.setRouteID(operationNameAttributes1.getRouteID());
        flowBatchCheckForLotSkipReqParams.setSeqno(-1L);
        try {
            flowBatchTestCase.flowBatchCheckForLotSkipReq(flowBatchCheckForLotSkipReqParams);
            Assert.isTrue(false, "test fail");
        } catch (ServiceException e) {
            Assert.isTrue(Validations.isEquals(retCodeConfig.getLotRemoveFromBatch(), e.getCode()), e.getMessage());
        }
    }

    private List<Infos.FlowBatchByManualActionReqCassette> getFlowBatchByManualActionReqCassette(List<Infos.TempFlowBatch>  tempFlowBatchList, List<ObjectIdentifier> cassetteListSelected){
        List<Infos.FlowBatchByManualActionReqCassette> strFlowBatchByManualActionReqCassette = new ArrayList<>();
        tempFlowBatchList.forEach(tempFlowBatch -> {
            if (cassetteListSelected.contains(tempFlowBatch.getCassetteID())){
                Assert.isTrue(tempFlowBatch.getStrTempFlowBatchLot().get(0).getTransferStatus().equals("MI"), "the transfer status must be MI");
                Infos.FlowBatchByManualActionReqCassette flowBatchByManualActionReqCassette = new Infos.FlowBatchByManualActionReqCassette();
                strFlowBatchByManualActionReqCassette.add(flowBatchByManualActionReqCassette);
                flowBatchByManualActionReqCassette.setCassetteID(tempFlowBatch.getCassetteID());
                List<ObjectIdentifier> lotIDs = new ArrayList<>();
                flowBatchByManualActionReqCassette.setLotID(lotIDs);
                tempFlowBatch.getStrTempFlowBatchLot().forEach(tempFlowBatchLot -> {
                    lotIDs.add(tempFlowBatchLot.getLotID());
                });
            }
        });
        return strFlowBatchByManualActionReqCassette;
    }

    public void flowBatch_HappyPath(){

        ObjectIdentifier tmpFlowBatchID = null;
        // stb two lots,the skip to 4000.0100
        //【step1】product 2 stb lots
        log.info("【step1】product 2 stb lots");
        //【step2】get stocker list
        Infos.StockerInfo stockerInfo = commonTestCase.getStocker();
        List<Infos.LotInfo> lotInfoList = produceLotsAndSkipToTheFlowBatch(2, true);
        List<ObjectIdentifier> cassettList = lotInfoList.stream().map(lotInfo -> lotInfo.getLotLocationInfo().getCassetteID()).collect(Collectors.toList());
        List<ObjectIdentifier> lots = lotInfoList.stream().map(lotInfo -> lotInfo.getLotBasicInfo().getLotID()).collect(Collectors.toList());
        // the cassette that do flowbatch
        for (ObjectIdentifier cassette : cassettList){
            //【step6】change Carrier Transfer Status
            log.info("【step6】change Carrier Transfer Status");
            commonTestCase.lotCassetteXferStatusChange(cassette, "MI");
        }
        //【step7】candidate lots list
        log.info("【step7】get candidate lots list");
        Params.FlowBatchLotSelectionInqParam flowBatchLotSelectionInqParam = new Params.FlowBatchLotSelectionInqParam();
        flowBatchLotSelectionInqParam.setEquipmentID(new ObjectIdentifier(FLOWBATCH_EQUIPMENTID));
        Results.FlowBatchLotSelectionInqResult flowBatchLotSelectionInqResult = flowBatchTestCase.getCandidateLotsList(flowBatchLotSelectionInqParam);
        List<Infos.TempFlowBatch> strTempFlowBatch = flowBatchLotSelectionInqResult.getStrTempFlowBatch();
        List<ObjectIdentifier> candidateCassettes = strTempFlowBatch.stream().map(Infos.TempFlowBatch::getCassetteID).collect(Collectors.toList());
        Assert.isTrue(candidateCassettes.containsAll(cassettList), "not found cassette");
        List<Infos.FlowBatchByManualActionReqCassette> strFlowBatchByManualActionReqCassette = new ArrayList<>();
        strTempFlowBatch.forEach(tempFlowBatch -> {
            if (cassettList.contains(tempFlowBatch.getCassetteID())){
                Assert.isTrue(tempFlowBatch.getStrTempFlowBatchLot().get(0).getTransferStatus().equals("MI"), "the transfer status must be MI");
                Infos.FlowBatchByManualActionReqCassette flowBatchByManualActionReqCassette = new Infos.FlowBatchByManualActionReqCassette();
                strFlowBatchByManualActionReqCassette.add(flowBatchByManualActionReqCassette);
                flowBatchByManualActionReqCassette.setCassetteID(tempFlowBatch.getCassetteID());
                List<ObjectIdentifier> lotIDs = new ArrayList<>();
                flowBatchByManualActionReqCassette.setLotID(lotIDs);
                tempFlowBatch.getStrTempFlowBatchLot().forEach(tempFlowBatchLot -> {
                    lotIDs.add(tempFlowBatchLot.getLotID());
                });
            }
        });
        //【step8】do flow batch
        log.info("【step8】do flow batch");
        Params.FlowBatchByManualActionReqParam flowBatchByManualActionReqParam = new Params.FlowBatchByManualActionReqParam();
        flowBatchByManualActionReqParam.setEquipmentID(new ObjectIdentifier(FLOWBATCH_EQUIPMENTID));
        flowBatchByManualActionReqParam.setStrFlowBatchByManualActionReqCassette(strFlowBatchByManualActionReqCassette);
        flowBatchTestCase.flowBatchByManualActionReq(flowBatchByManualActionReqParam);
        //【step9】get Flow batch info
        log.info("【step9】get Flow batch info");
        Params.FlowBatchInfoInqParams flowBatchInfoInqParams = new Params.FlowBatchInfoInqParams();
        flowBatchInfoInqParams.setEquipmentID(new ObjectIdentifier(FLOWBATCH_EQUIPMENTID));
        Results.FlowBatchInfoInqResult flowBatchInfo = flowBatchTestCase.getFlowBatchInfo(flowBatchInfoInqParams);
        Assert.isTrue(!CimArrayUtils.isEmpty(flowBatchInfo.getStrFlowBatchInfoList()), "flow batch information should be not null");
        //【step10】batch reserve cancel
        log.info("【step10】batch reserve cancel");
        Params.EqpReserveCancelForflowBatchReqParams eqpReserveCancelForflowBatchReqParams = new Params.EqpReserveCancelForflowBatchReqParams();
        eqpReserveCancelForflowBatchReqParams.setEquipmentID(flowBatchInfo.getReservedEquipmentID());
        eqpReserveCancelForflowBatchReqParams.setFlowBatchID(flowBatchInfo.getStrFlowBatchInfoList().get(0).getFlowBatchID());
        flowBatchTestCase.eqpReserveCancelForflowBatchReq(eqpReserveCancelForflowBatchReqParams);
        //【step11】get Floating batch info
        log.info("【step11】get Floating batch info");
        Results.FloatingBatchListInqResult floatingBatchInfo = flowBatchTestCase.getFloatingBatchInfo(new ObjectIdentifier(FLOWBATCH_EQUIPMENTID));
        Assert.isTrue(!CimArrayUtils.isEmpty(floatingBatchInfo.getFloatBatches()), "the floating batch info should not be null");
        //【step12】get Flow batch info
        log.info("【step12】get Flow batch info");
        Params.FlowBatchInfoInqParams flowBatchInfoInqParams2 = new Params.FlowBatchInfoInqParams();
        flowBatchInfoInqParams2.setFlowBatchID(floatingBatchInfo.getFloatBatches().get(0).getFlowBatchID());
        Results.FlowBatchInfoInqResult flowBatchInfo2 = flowBatchTestCase.getFlowBatchInfo(flowBatchInfoInqParams2);
        Assert.isTrue(!CimArrayUtils.isEmpty(flowBatchInfo2.getStrFlowBatchInfoList()), "flow batch information should be not null");
        //【step13】batch reserve
        Params.EqpReserveForFlowBatchReqParam eqpReserveForFlowBatchReqParam = new Params.EqpReserveForFlowBatchReqParam();
        eqpReserveForFlowBatchReqParam.setEquipmentID(new ObjectIdentifier(FLOWBATCH_EQUIPMENTID));
        eqpReserveForFlowBatchReqParam.setFlowBatchID(floatingBatchInfo.getFloatBatches().get(0).getFlowBatchID());
        flowBatchTestCase.eqpReserveForFlowBatchReq(eqpReserveForFlowBatchReqParam);
        //【step14】batch reserve cancel
        log.info("【step14】batch reserve cancel");
        Params.EqpReserveCancelForflowBatchReqParams eqpReserveCancelForflowBatchReqParams2 = new Params.EqpReserveCancelForflowBatchReqParams();
        eqpReserveCancelForflowBatchReqParams2.setEquipmentID(new ObjectIdentifier(FLOWBATCH_EQUIPMENTID));
        eqpReserveCancelForflowBatchReqParams2.setFlowBatchID(floatingBatchInfo.getFloatBatches().get(0).getFlowBatchID());
        flowBatchTestCase.eqpReserveCancelForflowBatchReq(eqpReserveCancelForflowBatchReqParams2);
        //【step15】get Floating batch info
        log.info("【step15】get Floating batch info");
        Results.FloatingBatchListInqResult floatingBatchInfo2 = flowBatchTestCase.getFloatingBatchInfo(new ObjectIdentifier(FLOWBATCH_EQUIPMENTID));
        Assert.isTrue(!CimArrayUtils.isEmpty(floatingBatchInfo2.getFloatBatches()), "the floating batch info should not be null");
        //【step16】re FlowBatching
        log.info("【step16】re FlowBatching");
        Params.ReFlowBatchByManualActionReqParam reFlowBatchByManualActionReqParam = new Params.ReFlowBatchByManualActionReqParam();
        reFlowBatchByManualActionReqParam.setEquipmentID(new ObjectIdentifier(FLOWBATCH_EQUIPMENTID));
        reFlowBatchByManualActionReqParam.setStrFlowBatchByManualActionReqCassette(floatingBatchInfo2.getFloatBatches().stream().map(floatBatch -> {
            Infos.ReFlowBatchByManualActionReqCassette reFlowBatchByManualActionReqCassette = new Infos.ReFlowBatchByManualActionReqCassette();
            reFlowBatchByManualActionReqCassette.setFromFlowBatchID(floatBatch.getFlowBatchID());
            reFlowBatchByManualActionReqCassette.setCassetteID(floatBatch.getCassetteID());
            reFlowBatchByManualActionReqCassette.setLotID(floatBatch.getFlowBatchedLotInfos().stream().map(Infos.FlowBatchedLotInfo::getLotID).collect(Collectors.toList()));
            return reFlowBatchByManualActionReqCassette;
        }).collect(Collectors.toList()));
        flowBatchTestCase.reFlowBatchByManualActionReq(reFlowBatchByManualActionReqParam);
        //【step17】get Flow batch info
        log.info("【step17】get Flow batch info");
        Params.FlowBatchInfoInqParams flowBatchInfoInqParams3 = new Params.FlowBatchInfoInqParams();
        flowBatchInfoInqParams3.setEquipmentID(new ObjectIdentifier(FLOWBATCH_EQUIPMENTID));
        Results.FlowBatchInfoInqResult flowBatchInfo3 = flowBatchTestCase.getFlowBatchInfo(flowBatchInfoInqParams3);
        Assert.isTrue(!CimArrayUtils.isEmpty(flowBatchInfo3.getStrFlowBatchInfoList()), "flow batch information should be not null");
        //【step18】remove the batch
        Params.FlowBatchLotRemoveReq flowBatchLotRemoveReqParams1 = new Params.FlowBatchLotRemoveReq();
        flowBatchLotRemoveReqParams1.setFlowBatchID(flowBatchInfo3.getStrFlowBatchInfoList().get(0).getFlowBatchID());
        Infos.FlowBatchedCassetteInfoExtend flowBatchedCassetteInfoExtend1 = flowBatchInfo3.getStrFlowBatchInfoList().get(0).getFlowBatchedCassetteInfoList().get(0);
        Infos.RemoveCassette removeCassette1 = new Infos.RemoveCassette();
        removeCassette1.setCassetteID(flowBatchedCassetteInfoExtend1.getCassetteID());
        removeCassette1.setLotID(flowBatchedCassetteInfoExtend1.getFlowBatchedLotInfoList().stream().map(Infos.FlowBatchedLotInfoExtend::getLotID).collect(Collectors.toList()));
        flowBatchLotRemoveReqParams1.setStrRemoveCassette(Arrays.asList(removeCassette1));
        try {
            flowBatchTestCase.flowBatchLotRemoveReq(flowBatchLotRemoveReqParams1);
            Assert.isTrue(false, "test fail");
        } catch (ServiceException e) {
            Assert.isTrue(Validations.isEquals(retCodeConfig.getInvalidCassetteCountForBatch(), e.getCode()), e.getMessage());
        }
        Params.FlowBatchLotRemoveReq flowBatchLotRemoveReqParams2 = new Params.FlowBatchLotRemoveReq();
        flowBatchLotRemoveReqParams2.setFlowBatchID(flowBatchInfo3.getStrFlowBatchInfoList().get(0).getFlowBatchID());
        flowBatchLotRemoveReqParams2.setStrRemoveCassette(flowBatchInfo3.getStrFlowBatchInfoList().get(0).getFlowBatchedCassetteInfoList().stream().map(flowBatchedCassetteInfoExtend -> {
            Infos.RemoveCassette removeCassette2 = new Infos.RemoveCassette();
            removeCassette2.setCassetteID(flowBatchedCassetteInfoExtend.getCassetteID());
            removeCassette2.setLotID(flowBatchedCassetteInfoExtend.getFlowBatchedLotInfoList().stream().map(Infos.FlowBatchedLotInfoExtend::getLotID).collect(Collectors.toList()));
            return removeCassette2;
        }).collect(Collectors.toList()));
        flowBatchTestCase.flowBatchLotRemoveReq(flowBatchLotRemoveReqParams2);
        //【step19】candidate lots list
        Params.FlowBatchLotSelectionInqParam flowBatchLotSelectionInqParam2 = new Params.FlowBatchLotSelectionInqParam();
        flowBatchLotSelectionInqParam2.setEquipmentID(new ObjectIdentifier(FLOWBATCH_EQUIPMENTID));
        Results.FlowBatchLotSelectionInqResult flowBatchLotSelectionInqResult2 = flowBatchTestCase.getCandidateLotsList(flowBatchLotSelectionInqParam2);
        List<Infos.TempFlowBatch> strTempFlowBatch2 = flowBatchLotSelectionInqResult2.getStrTempFlowBatch();
        List<ObjectIdentifier> candidateCassettes2 = strTempFlowBatch2.stream().map(Infos.TempFlowBatch::getCassetteID).collect(Collectors.toList());
        Assert.isTrue(candidateCassettes2.containsAll(cassettList), "not found cassette");
        List<Infos.FlowBatchByManualActionReqCassette> strFlowBatchByManualActionReqCassette2 = new ArrayList<>();
        strTempFlowBatch2.forEach(tempFlowBatch -> {
            if (cassettList.contains(tempFlowBatch.getCassetteID())){
                Assert.isTrue(tempFlowBatch.getStrTempFlowBatchLot().get(0).getTransferStatus().equals("MI"), "the transfer status must be MI");
                Infos.FlowBatchByManualActionReqCassette flowBatchByManualActionReqCassette = new Infos.FlowBatchByManualActionReqCassette();
                strFlowBatchByManualActionReqCassette2.add(flowBatchByManualActionReqCassette);
                flowBatchByManualActionReqCassette.setCassetteID(tempFlowBatch.getCassetteID());
                List<ObjectIdentifier> lotIDs = new ArrayList<>();
                flowBatchByManualActionReqCassette.setLotID(lotIDs);
                tempFlowBatch.getStrTempFlowBatchLot().forEach(tempFlowBatchLot -> {
                    lotIDs.add(tempFlowBatchLot.getLotID());
                });
            }
        });
        //【step20】do flow batch
        Params.FlowBatchByManualActionReqParam flowBatchByManualActionReqParam2 = new Params.FlowBatchByManualActionReqParam();
        flowBatchByManualActionReqParam2.setEquipmentID(new ObjectIdentifier(FLOWBATCH_EQUIPMENTID));
        flowBatchByManualActionReqParam2.setStrFlowBatchByManualActionReqCassette(strFlowBatchByManualActionReqCassette2);
        Results.FlowBatchByManualActionReqResult flowBatchByManualActionReqResult = (Results.FlowBatchByManualActionReqResult) flowBatchTestCase.flowBatchByManualActionReq(flowBatchByManualActionReqParam2).getBody();
        operationSkipCase.skipSpecificStep(lots, "4000.0200", true);
        //【step23】eqp reserve
        startLotsReservationCase.moveInReserveReqWhithSpecifiedLotsAndEqp(lots, new ObjectIdentifier(FLOWBATCH_EQUIPMENTID));
        //【step24】loading
        try {
            lotLoadToEquipmentCase.loading_Without_StartLotsReservationSpecifiedEqpAndCassettes(new ObjectIdentifier(FLOWBATCH_EQUIPMENTID), cassettList);
            Assert.isTrue(false, "test fail");
        } catch (ServiceException e) {
            Assert.isTrue(Validations.isEquals(retCodeConfig.getInvalidCassetteTransferState(), e.getCode()), e.getMessage());
        }
        for(ObjectIdentifier cassetteID : cassettList){
            //【step25】change Carrier Transfer Status
            commonTestCase.lotCassetteXferStatusChange(cassetteID, "MO");
        }
        //【step26】load again
        lotLoadToEquipmentCase.loading_Without_StartLotsReservationSpecifiedEqpAndCassettes(new ObjectIdentifier(FLOWBATCH_EQUIPMENTID), cassettList);
        //【step27】unload
        lotUnloadFromEquipmentCase.unLoad_WithSpecifiedEqpAndCassettes(new ObjectIdentifier(FLOWBATCH_EQUIPMENTID), cassettList);

    }

    public void autoFlowBatch_HappyPath (){
        // the cassette that do flowbatch
        List<Infos.LotInfo> lotInfoList = produceLotsAndSkipToTheFlowBatch(4, true);
        List<ObjectIdentifier> cassettList = lotInfoList.stream().map(lotInfo -> lotInfo.getLotLocationInfo().getCassetteID()).collect(Collectors.toList());
        //【step7】candidate lots list
        log.info("【step7】get candidate lots list");
        Params.FlowBatchLotSelectionInqParam flowBatchLotSelectionInqParam = new Params.FlowBatchLotSelectionInqParam();
        flowBatchLotSelectionInqParam.setEquipmentID(new ObjectIdentifier(FLOWBATCH_EQUIPMENTID));
        Results.FlowBatchLotSelectionInqResult flowBatchLotSelectionInqResult = flowBatchTestCase.getCandidateLotsList(flowBatchLotSelectionInqParam);
        List<Infos.TempFlowBatch> strTempFlowBatch = flowBatchLotSelectionInqResult.getStrTempFlowBatch();
        List<ObjectIdentifier> candidateCassettes = strTempFlowBatch.stream().map(Infos.TempFlowBatch::getCassetteID).collect(Collectors.toList());
        Assert.isTrue(candidateCassettes.containsAll(cassettList), "not found cassette");
        //【step8】get stocker list
        Infos.StockerInfo stockerInfo = commonTestCase.getStocker();
        //【step9】auto flow batch（less than max flow batch size）
        int maxFlowBatchLotSize = strTempFlowBatch.get(0).getStrTempFlowBatchLot().get(0).getFlowBatchLotSize().intValue();
        int countMILots = 0;
        for (Infos.TempFlowBatch tempFlowBatch : strTempFlowBatch){
            if (tempFlowBatch.getStrTempFlowBatchLot().get(0).getTransferStatus().equals("MI")){
                countMILots++;
            }
        }
        int indexHaveChangeStatus = -1;
        if (countMILots < maxFlowBatchLotSize){
            if (cassettList.size() > maxFlowBatchLotSize - countMILots - 1){
                for (int i = 0; i < maxFlowBatchLotSize - countMILots - 1; i++){
                    ObjectIdentifier cassetteID = cassettList.get(i);
                    //【step14】change Carrier Transfer Status
                    log.info("【step14】change Carrier Transfer Status");
                    commonTestCase.lotCassetteXferStatusChange(cassetteID, "MI");
                    indexHaveChangeStatus = i;
                }
            }
        }
        //【step10】call auto flowbatch
        Params.FlowBatchByAutoActionReqParams autoFlowBatchByManualActionReqParams = new Params.FlowBatchByAutoActionReqParams();
        autoFlowBatchByManualActionReqParams.setEquipmentID(new ObjectIdentifier(FLOWBATCH_EQUIPMENTID));
        try {
            flowBatchTestCase.autoFlowBatchByManualActionReq(autoFlowBatchByManualActionReqParams);
            Assert.isTrue(false, "test fail");
        } catch (ServiceException e) {
            Assert.isTrue(Validations.isEquals(retCodeConfig.getNotEnoughLotForFlowBatch(), e.getCode()), e.getMessage());
        }
        if (maxFlowBatchLotSize > cassettList.size()){
            //【step11】 product maxFlowBatchLotSize-cassettList.size() stb lots
            log.info("【step11】product {} stb lots", maxFlowBatchLotSize-cassettList.size());
            List<ObjectIdentifier> lots2 = stbCase.stb_NLots_NotPreparedCase(maxFlowBatchLotSize-cassettList.size(), false);
            for (ObjectIdentifier lot : lots2){
                //【step12】lot info
                log.info("【step12】lot info");
                List<ObjectIdentifier> lotIDList = new ArrayList<>();
                lotIDList.add(lot);
                Results.LotInfoInqResult lotInfoInqResult = (Results.LotInfoInqResult) lotGeneralTestCase.getLotInfoCase(lotIDList).getBody();
                Infos.LotInfo lotInfo = lotInfoInqResult.getLotInfoList().get(0);
                cassettList.add(lotInfo.getLotLocationInfo().getCassetteID());
                //【step13】get lot operation
                log.info("【step13】get lot operation");
                Results.LotOperationSelectionInqResult lotOperationSelectionInqResult1 = (Results.LotOperationSelectionInqResult) lotGeneralTestCase.getLotOperationSelectionInq(lot, false, true, true).getBody();
                List<Infos.OperationNameAttributes> operationNameAttributesList1 = lotOperationSelectionInqResult1.getOperationNameAttributesAttributes().getContent();
                Infos.OperationNameAttributes operationNameAttributes1 = null;
                for (Infos.OperationNameAttributes tmpOperationNameAttributes : operationNameAttributesList1){
                    if (CimStringUtils.equals(tmpOperationNameAttributes.getOperationNumber(), "4000.0100")){
                        operationNameAttributes1 = tmpOperationNameAttributes;
                        break;
                    }
                }
                Assert.isTrue(operationNameAttributes1 != null, "not find operationnumber");
                //【step14】skip
                log.info("【step14】skip");
                Params.SkipReqParams skipReqParams = new Params.SkipReqParams();
                skipReqParams.setCurrentOperationNumber(lotInfo.getLotOperationInfo().getOperationNumber());
                skipReqParams.setCurrentRouteID(lotInfo.getLotOperationInfo().getRouteID());
                skipReqParams.setLocateDirection(true);
                skipReqParams.setLotID(lot);
                skipReqParams.setOperationID(operationNameAttributes1.getOperationID());
                skipReqParams.setOperationNumber(operationNameAttributes1.getOperationNumber());
                skipReqParams.setProcessRef(operationNameAttributes1.getProcessRef());
                skipReqParams.setRouteID(operationNameAttributes1.getRouteID());
                skipReqParams.setSeqno(-1);
                skipReqParams.setSequenceNumber(0);
                operationSkipTestCase.operationSkip(skipReqParams);
            }
        }
        //【step15】change Carrier Transfer Status
        log.info("【step15】change Carrier Transfer Status");
        for (int i = indexHaveChangeStatus + 1; i < cassettList.size(); i++){
            ObjectIdentifier cassetteID = cassettList.get(i);
            commonTestCase.lotCassetteXferStatusChange(cassetteID, "MI");
        }
        //【step16】call auto flowbatch
        flowBatchTestCase.autoFlowBatchByManualActionReq(autoFlowBatchByManualActionReqParams);
    }

    public void flowBatch_BatchSize(){
        //【step1】get stocker list
        Infos.StockerInfo stockerInfo = commonTestCase.getStocker();
        // 【step2】produce 3 lot
        List<Infos.LotInfo> lotInfoList = produceLotsAndSkipToTheFlowBatch(3, true);
        // 【step3】change transfer status
        List<ObjectIdentifier> cassetteList = lotInfoList.stream().map(lotInfo -> lotInfo.getLotLocationInfo().getCassetteID()).collect(Collectors.toList());
        for (ObjectIdentifier cassette : cassetteList){
            //【step6】change Carrier Transfer Status
            log.info("【step6】change Carrier Transfer Status");
            commonTestCase.lotCassetteXferStatusChange(cassette, "MI");
        }
        //【step4】candidate lots list
        Params.FlowBatchLotSelectionInqParam flowBatchLotSelectionInqParam = new Params.FlowBatchLotSelectionInqParam();
        flowBatchLotSelectionInqParam.setEquipmentID(new ObjectIdentifier(FLOWBATCH_EQUIPMENTID));
        Results.FlowBatchLotSelectionInqResult flowBatchLotSelectionInqResult = flowBatchTestCase.getCandidateLotsList(flowBatchLotSelectionInqParam);
        List<Infos.TempFlowBatch> strTempFlowBatch = flowBatchLotSelectionInqResult.getStrTempFlowBatch();
        List<ObjectIdentifier> candidateCassettes = strTempFlowBatch.stream().map(Infos.TempFlowBatch::getCassetteID).collect(Collectors.toList());
        Assert.isTrue(candidateCassettes.containsAll(cassetteList), "not found cassette");
        int maxFlowBatchLotSize = strTempFlowBatch.get(0).getStrTempFlowBatchLot().get(0).getFlowBatchLotSize().intValue();
        int minFlowBatchLotSize = strTempFlowBatch.get(0).getStrTempFlowBatchLot().get(0).getFlowBatchLotMinSize().intValue();
        Assert.isTrue(minFlowBatchLotSize > 1, "min flow batch size must greater than 1");
        // 【step5】flow batching when batch size less than minimum batch size
        List<ObjectIdentifier> cassetteListSelectedMin = new ArrayList<>();
        if (cassetteList.size() < minFlowBatchLotSize){
            cassetteListSelectedMin = cassetteList;
        } else {
            for (int i = 0; i < minFlowBatchLotSize - 1; i++){
                cassetteListSelectedMin.add(cassetteList.get(i));
            }
        }
        List<Infos.FlowBatchByManualActionReqCassette> flowBatchByManualActionReqCassette = getFlowBatchByManualActionReqCassette(strTempFlowBatch, cassetteListSelectedMin);
        Params.FlowBatchByManualActionReqParam flowBatchByManualActionReqParam = new Params.FlowBatchByManualActionReqParam();
        flowBatchByManualActionReqParam.setEquipmentID(new ObjectIdentifier(FLOWBATCH_EQUIPMENTID));
        flowBatchByManualActionReqParam.setStrFlowBatchByManualActionReqCassette(flowBatchByManualActionReqCassette);
        try {
            flowBatchTestCase.flowBatchByManualActionReq(flowBatchByManualActionReqParam);
            Assert.isTrue(false, "test fail");
        } catch (ServiceException e) {
            Assert.isTrue(Validations.isEquals(e.getCode(), retCodeConfig.getInvalidCassetteCountForBatch()), e.getMessage());
        }
        // 【step5】flow batching when batch size greater than maximum batch size
        List<ObjectIdentifier> cassetteListSelectedMax = cassetteList;
        if (cassetteList.size() < maxFlowBatchLotSize){
            // produce maxFlowBatchLotSize + 1 - cassetteList.size() lot
            List<Infos.LotInfo> lotInfoList2 = produceLotsAndSkipToTheFlowBatch(maxFlowBatchLotSize + 1 - cassetteList.size(), false);
            for (Infos.LotInfo lotInfo : lotInfoList2){
                cassetteList.add(lotInfo.getLotLocationInfo().getCassetteID());
                Params.CarrierTransferStatusChangeRptParams carrierTransferStatusChangeRptParams = new Params.CarrierTransferStatusChangeRptParams();
                commonTestCase.lotCassetteXferStatusChange(lotInfo.getLotLocationInfo().getCassetteID(), "MI");
            }
        }
        Results.FlowBatchLotSelectionInqResult flowBatchLotSelectionInqResult2 = flowBatchTestCase.getCandidateLotsList(flowBatchLotSelectionInqParam);
        List<Infos.TempFlowBatch> strTempFlowBatch2 = flowBatchLotSelectionInqResult2.getStrTempFlowBatch();
        List<Infos.FlowBatchByManualActionReqCassette> flowBatchByManualActionReqCassette2 = getFlowBatchByManualActionReqCassette(strTempFlowBatch2, cassetteListSelectedMax);
        Params.FlowBatchByManualActionReqParam flowBatchByManualActionReqParam2 = new Params.FlowBatchByManualActionReqParam();
        flowBatchByManualActionReqParam2.setEquipmentID(new ObjectIdentifier(FLOWBATCH_EQUIPMENTID));
        flowBatchByManualActionReqParam2.setStrFlowBatchByManualActionReqCassette(flowBatchByManualActionReqCassette2);
        try {
            flowBatchTestCase.flowBatchByManualActionReq(flowBatchByManualActionReqParam2);
            Assert.isTrue(false, "test fail");
        } catch (ServiceException e) {
            Assert.isTrue(Validations.isEquals(e.getCode(), retCodeConfig.getInvalidCassetteCountForBatch()), e.getMessage());
        }
    }

    public void flowBatch_SomeBatchesExist(){
        //【step1】get stocker list
        Infos.StockerInfo stockerInfo = commonTestCase.getStocker();
        // 【step2】produce 4 lot
        List<Infos.LotInfo> lotInfoList = produceLotsAndSkipToTheFlowBatch(4, true);
        // 【step3】change transfer status
        List<ObjectIdentifier> cassetteList = lotInfoList.stream().map(lotInfo -> lotInfo.getLotLocationInfo().getCassetteID()).collect(Collectors.toList());
        for (ObjectIdentifier cassette : cassetteList){
            Params.CarrierTransferStatusChangeRptParams carrierTransferStatusChangeRptParams = new Params.CarrierTransferStatusChangeRptParams();
            commonTestCase.lotCassetteXferStatusChange(cassette, "MI");
        }
        //【step4】candidate lots list
        Params.FlowBatchLotSelectionInqParam flowBatchLotSelectionInqParam = new Params.FlowBatchLotSelectionInqParam();
        flowBatchLotSelectionInqParam.setEquipmentID(new ObjectIdentifier(FLOWBATCH_EQUIPMENTID));
        Results.FlowBatchLotSelectionInqResult flowBatchLotSelectionInqResult = flowBatchTestCase.getCandidateLotsList(flowBatchLotSelectionInqParam);
        List<Infos.TempFlowBatch> strTempFlowBatch = flowBatchLotSelectionInqResult.getStrTempFlowBatch();
        List<ObjectIdentifier> candidateCassettes = strTempFlowBatch.stream().map(Infos.TempFlowBatch::getCassetteID).collect(Collectors.toList());
        Assert.isTrue(candidateCassettes.containsAll(cassetteList), "not found cassette");
        int minFlowBatchLotSize = strTempFlowBatch.get(0).getStrTempFlowBatchLot().get(0).getFlowBatchLotMinSize().intValue();
        Assert.isTrue(minFlowBatchLotSize > 1, "min flow batch size must greater than 1");
        int maxCountForFlowBatch = flowBatchLotSelectionInqResult.getMaxCountForFlowBatch().intValue();
        if (maxCountForFlowBatch != 1){
            Params.EqpMaxFlowbCountModifyReqParams eqpMaxFlowbCountModifyReqParams = new Params.EqpMaxFlowbCountModifyReqParams();
            eqpMaxFlowbCountModifyReqParams.setEquipmentID(new ObjectIdentifier(FLOWBATCH_EQUIPMENTID));
            eqpMaxFlowbCountModifyReqParams.setFlowBatchMaxCount(1);
            flowBatchTestCase.eqpMaxFlowbCountModifyReq(eqpMaxFlowbCountModifyReqParams);
        }
        if (cassetteList.size() < minFlowBatchLotSize * 2){
            List<Infos.LotInfo> lotInfoList2 = produceLotsAndSkipToTheFlowBatch(minFlowBatchLotSize * 2 - candidateCassettes.size(), false);
            for (Infos.LotInfo lotInfo : lotInfoList2){
                ObjectIdentifier cassetteID = lotInfo.getLotLocationInfo().getCassetteID();
                cassetteList.add(cassetteID);
                commonTestCase.lotCassetteXferStatusChange(cassetteID, "MI");
            }
        }
        // 【step5】do flowbatch first
        List<ObjectIdentifier> cassetteSubList = cassetteList.subList(0, cassetteList.size() /2);
        List<Infos.FlowBatchByManualActionReqCassette> flowBatchByManualActionReqCassette = getFlowBatchByManualActionReqCassette(strTempFlowBatch, cassetteSubList);
        Params.FlowBatchByManualActionReqParam flowBatchByManualActionReqParam = new Params.FlowBatchByManualActionReqParam();
        flowBatchByManualActionReqParam.setEquipmentID(new ObjectIdentifier(FLOWBATCH_EQUIPMENTID));
        flowBatchByManualActionReqParam.setStrFlowBatchByManualActionReqCassette(flowBatchByManualActionReqCassette);
        flowBatchTestCase.flowBatchByManualActionReq(flowBatchByManualActionReqParam);
        // 【step6】do flowbatch second
        List<ObjectIdentifier> cassetteSubList2 = cassetteList.subList(cassetteList.size()/2, cassetteList.size());
        List<Infos.FlowBatchByManualActionReqCassette> flowBatchByManualActionReqCassette2 = getFlowBatchByManualActionReqCassette(strTempFlowBatch, cassetteSubList2);
        Params.FlowBatchByManualActionReqParam flowBatchByManualActionReqParam2 = new Params.FlowBatchByManualActionReqParam();
        flowBatchByManualActionReqParam2.setEquipmentID(new ObjectIdentifier(FLOWBATCH_EQUIPMENTID));
        flowBatchByManualActionReqParam2.setStrFlowBatchByManualActionReqCassette(flowBatchByManualActionReqCassette2);
        try {
            flowBatchTestCase.flowBatchByManualActionReq(flowBatchByManualActionReqParam2);
            Assert.isTrue(false, "test fail");
        } catch (ServiceException e) {
            Assert.isTrue(Validations.isEquals(retCodeConfig.getEquipmentReservedForOtherFlowBatch(), e.getCode()), e.getMessage());
        }

    }

    public List<Infos.LotInfo> flowBatch_FirstTargetEquipment_MoveOut(AtomicReference<ObjectIdentifier> temporaryFlowBatchID){
        List<Infos.LotInfo> lotInfoListReturn = new ArrayList<>();
        ObjectIdentifier tmpFlowBatchID = null;
        //【step1】candidate lots list
        Params.FlowBatchLotSelectionInqParam flowBatchLotSelectionInqParam = new Params.FlowBatchLotSelectionInqParam();
        flowBatchLotSelectionInqParam.setEquipmentID(new ObjectIdentifier(FLOWBATCH_EQUIPMENTID));
        Results.FlowBatchLotSelectionInqResult flowBatchLotSelectionInqResult = flowBatchTestCase.getCandidateLotsList(flowBatchLotSelectionInqParam);
        List<Infos.TempFlowBatch> strTempFlowBatch = flowBatchLotSelectionInqResult.getStrTempFlowBatch();
        int flowBatchLotMinSize = strTempFlowBatch.get(0).getStrTempFlowBatchLot().get(0).getFlowBatchLotMinSize().intValue();
        // 【step2】produce lot and do flowbatch
        int produceNumber = flowBatchLotMinSize;
        int flowBatchSize = flowBatchLotMinSize;
        TestInfos.FlowBatchResultInfo flowBatchResultInfo = this.productLotsAndDoFlowBatch(produceNumber, flowBatchSize);
        List<ObjectIdentifier> lots = flowBatchResultInfo.getLotList();
        List<ObjectIdentifier> cassetteList = flowBatchResultInfo.getCassetteList();
        List<Infos.TempFlowBatch> strTempFlowBatch2 = flowBatchResultInfo.getTempFlowBatchList();
        for (Infos.TempFlowBatch tempFlowBatch : strTempFlowBatch2){
            if (CimObjectUtils.equalsWithValue(tempFlowBatch.getCassetteID(), cassetteList.get(0))){
                temporaryFlowBatchID.set(tempFlowBatch.getTemporaryFlowBatchID());
                break;
            }
        }
        tmpFlowBatchID = flowBatchResultInfo.getFlowBatchID();
        // 【step3】skip
        operationSkipCase.skipSpecificStep(lots, "4000.0200", true);

        //【step4】eqp reserve
        log.info("【step23】eqp reserve");
        startLotsReservationCase.moveInReserveReqWhithSpecifiedLotsAndEqp(lots, new ObjectIdentifier(FLOWBATCH_EQUIPMENTID));
        // 【step5】change transfer status
        for (ObjectIdentifier cassette : cassetteList){
            commonTestCase.lotCassetteXferStatusChange(cassette, "MO");
        }
        //【step6】loading
        lotLoadToEquipmentCase.loading_Without_StartLotsReservationSpecifiedEqpAndCassettes(new ObjectIdentifier(FLOWBATCH_EQUIPMENTID), cassetteList);
        Results.EqpInfoInqResult eqpInfo = electronicInformationTestCase.getEqpInfo(new ObjectIdentifier((FLOWBATCH_EQUIPMENTID)));
        ObjectIdentifier reservedFlowBatchID = eqpInfo.getEquipmentStatusInfo().getReservedFlowBatchID();
        Assert.isTrue(reservedFlowBatchID != null, "test fail");
        //【step7】lotInfo
        for (ObjectIdentifier lot : lots){
            List<ObjectIdentifier> lotIDList = Arrays.asList(lot);
            Results.LotInfoInqResult lotInfoInqResult = (Results.LotInfoInqResult) lotGeneralTestCase.getLotInfoCase(lotIDList).getBody();
            Infos.LotFlowBatchInfo lotFlowBatchInfo = lotInfoInqResult.getLotInfoList().get(0).getLotFlowBatchInfo();
            Assert.isTrue(!CimObjectUtils.isEmptyWithValue(lotFlowBatchInfo.getFlowBatchID()) &&  !CimObjectUtils.isEmptyWithValue(lotFlowBatchInfo.getFlowBatchReserveEquipmentID()), "test fail");
        }
        //【step8】move in
        Results.MoveInReqResult moveInReqResult = (Results.MoveInReqResult) moveInCase.onlyMoveIn(cassetteList, new ObjectIdentifier(FLOWBATCH_EQUIPMENTID)).getBody();
        Results.EqpInfoInqResult eqpInfo2 = electronicInformationTestCase.getEqpInfo(new ObjectIdentifier((FLOWBATCH_EQUIPMENTID)));
        ObjectIdentifier reservedFlowBatchID2 = eqpInfo2.getEquipmentStatusInfo().getReservedFlowBatchID();
        Assert.isTrue(CimObjectUtils.isEmptyWithValue(reservedFlowBatchID2), "test fail");
        //【step9】lotInfo
        for (ObjectIdentifier lot : lots){
            List<ObjectIdentifier> lotIDList = Arrays.asList(lot);
            Results.LotInfoInqResult lotInfoInqResult = (Results.LotInfoInqResult) lotGeneralTestCase.getLotInfoCase(lotIDList).getBody();
            Infos.LotFlowBatchInfo lotFlowBatchInfo = lotInfoInqResult.getLotInfoList().get(0).getLotFlowBatchInfo();
            Assert.isTrue(!CimObjectUtils.isEmptyWithValue(lotFlowBatchInfo.getFlowBatchID()) &&  CimObjectUtils.isEmptyWithValue(lotFlowBatchInfo.getFlowBatchReserveEquipmentID()), "test fail");
        }
        //【step10】move in cancel
        moveInCancelCase.moveInCancel_Normal(new ObjectIdentifier(FLOWBATCH_EQUIPMENTID), moveInReqResult);
        Results.EqpInfoInqResult eqpInfo3 = electronicInformationTestCase.getEqpInfo(new ObjectIdentifier((FLOWBATCH_EQUIPMENTID)));
        ObjectIdentifier reservedFlowBatchID3 = eqpInfo3.getEquipmentStatusInfo().getReservedFlowBatchID();
        Assert.isTrue(CimObjectUtils.isEmptyWithValue(reservedFlowBatchID3), "test fail");
        //【step11】lotInfo
        for (ObjectIdentifier lot : lots){
            List<ObjectIdentifier> lotIDList = Arrays.asList(lot);
            Results.LotInfoInqResult lotInfoInqResult = (Results.LotInfoInqResult) lotGeneralTestCase.getLotInfoCase(lotIDList).getBody();
            Infos.LotFlowBatchInfo lotFlowBatchInfo = lotInfoInqResult.getLotInfoList().get(0).getLotFlowBatchInfo();
            Assert.isTrue(!CimObjectUtils.isEmptyWithValue(lotFlowBatchInfo.getFlowBatchID()) &&  CimObjectUtils.isEmptyWithValue(lotFlowBatchInfo.getFlowBatchReserveEquipmentID()), "test fail");
        }
        //【step12】move in
        try {
            moveInCase.onlyMoveIn(cassetteList, new ObjectIdentifier(FLOWBATCH_EQUIPMENTID));
            Assert.isTrue(false, "test fail");
        } catch (ServiceException e) {
            Assert.isTrue(Validations.isEquals(retCodeConfig.getNotCorrectEqpForOperationStart(), e.getCode()), e.getMessage());
        }
        //【step13】unLoad
        lotUnloadFromEquipmentCase.unLoad_WithSpecifiedEqpAndCassettes(new ObjectIdentifier(FLOWBATCH_EQUIPMENTID), cassetteList);
        // 【step14】change transfer status
        for (ObjectIdentifier cassette : cassetteList){
            commonTestCase.lotCassetteXferStatusChange(cassette, "MI");
        }
        //【step15】get Floating batch info
        log.info("【step15】get Floating batch info");
        Results.FloatingBatchListInqResult floatingBatchInfo = flowBatchTestCase.getFloatingBatchInfo(new ObjectIdentifier(FLOWBATCH_EQUIPMENTID));
        List<Infos.FloatBatch> floatBatches = floatingBatchInfo.getFloatBatches();
        Assert.isTrue(!CimArrayUtils.isEmpty(floatBatches)
                && floatBatches.stream().map(Infos.FloatBatch::getCassetteID).collect(Collectors.toList()).containsAll(cassetteList), "the floating batch info should not be null");
        //【step16】re FlowBatching
        log.info("【step16】re FlowBatching");
        Params.ReFlowBatchByManualActionReqParam reFlowBatchByManualActionReqParam = new Params.ReFlowBatchByManualActionReqParam();
        reFlowBatchByManualActionReqParam.setEquipmentID(new ObjectIdentifier(FLOWBATCH_EQUIPMENTID));
        reFlowBatchByManualActionReqParam.setStrFlowBatchByManualActionReqCassette(floatingBatchInfo.getFloatBatches().stream().map(floatBatch -> {
            Infos.ReFlowBatchByManualActionReqCassette reFlowBatchByManualActionReqCassette = new Infos.ReFlowBatchByManualActionReqCassette();
            reFlowBatchByManualActionReqCassette.setFromFlowBatchID(floatBatch.getFlowBatchID());
            reFlowBatchByManualActionReqCassette.setCassetteID(floatBatch.getCassetteID());
            reFlowBatchByManualActionReqCassette.setLotID(floatBatch.getFlowBatchedLotInfos().stream().map(Infos.FlowBatchedLotInfo::getLotID).collect(Collectors.toList()));
            return reFlowBatchByManualActionReqCassette;
        }).collect(Collectors.toList()));
        flowBatchTestCase.reFlowBatchByManualActionReq(reFlowBatchByManualActionReqParam);
        Params.FlowBatchInfoInqParams flowBatchInfoInqParams = new Params.FlowBatchInfoInqParams();
        flowBatchInfoInqParams.setEquipmentID(new ObjectIdentifier(FLOWBATCH_EQUIPMENTID));
        Results.FlowBatchInfoInqResult flowBatchInfo = flowBatchTestCase.getFlowBatchInfo(flowBatchInfoInqParams);
        tmpFlowBatchID = flowBatchInfo.getStrFlowBatchInfoList().get(0).getFlowBatchID();
        // 【step17】change transfer status
        for (ObjectIdentifier cassette : cassetteList){
            commonTestCase.lotCassetteXferStatusChange(cassette, "MO");
        }
        //【step8】loading
        lotLoadToEquipmentCase.loading_Without_StartLotsReservationSpecifiedEqpAndCassettes(new ObjectIdentifier(FLOWBATCH_EQUIPMENTID), cassetteList);
        //【step19】move in
        Results.MoveInReqResult moveInReqResult2 = (Results.MoveInReqResult) moveInCase.onlyMoveIn(cassetteList, new ObjectIdentifier(FLOWBATCH_EQUIPMENTID)).getBody();
        Results.EqpInfoInqResult eqpInfo5 = electronicInformationTestCase.getEqpInfo(new ObjectIdentifier((FLOWBATCH_EQUIPMENTID)));
        ObjectIdentifier reservedFlowBatchID5 = eqpInfo5.getEquipmentStatusInfo().getReservedFlowBatchID();
        Assert.isTrue(CimObjectUtils.isEmptyWithValue(reservedFlowBatchID5), "test fail");
        //【step20】move out
        moveOutCase.onlyMoveOut(moveInReqResult2.getControlJobID(), new ObjectIdentifier(FLOWBATCH_EQUIPMENTID));
        //【step21】lotInfo
        for (ObjectIdentifier lot : lots){
            List<ObjectIdentifier> lotIDList = Arrays.asList(lot);
            Results.LotInfoInqResult lotInfoInqResult = (Results.LotInfoInqResult) lotGeneralTestCase.getLotInfoCase(lotIDList).getBody();
            Infos.LotFlowBatchInfo lotFlowBatchInfo = lotInfoInqResult.getLotInfoList().get(0).getLotFlowBatchInfo();
            Assert.isTrue(tmpFlowBatchID.equals(lotFlowBatchInfo.getFlowBatchID()) &&  CimObjectUtils.isEmptyWithValue(lotFlowBatchInfo.getFlowBatchReserveEquipmentID()), "test fail");
            lotInfoListReturn.add(lotInfoInqResult.getLotInfoList().get(0));
        }
        //【step22】unLoad
        lotUnloadFromEquipmentCase.unLoad_WithSpecifiedEqpAndCassettes(new ObjectIdentifier(FLOWBATCH_EQUIPMENTID), cassetteList);
        return lotInfoListReturn;
    }
    public void flowBatch_LastTargetEquipment_MoveOut(){
        AtomicReference<ObjectIdentifier> tempFlowBathcID = new AtomicReference<>();
        //【step1】get stocker list
        Infos.StockerInfo stockerInfo = commonTestCase.getStocker();
        //【step2】flowBatch_FirstTargetEquipment_MoveOut
        List<Infos.LotInfo> lotInfoList = this.flowBatch_FirstTargetEquipment_MoveOut(tempFlowBathcID);
        List<ObjectIdentifier> cassetteList = lotInfoList.stream().map(lotInfo -> lotInfo.getLotLocationInfo().getCassetteID()).collect(Collectors.toList());
        List<ObjectIdentifier> lots = lotInfoList.stream().map(lotInfo -> lotInfo.getLotBasicInfo().getLotID()).collect(Collectors.toList());
        //【step3】last target candidate lots list
        Params.FlowBatchLotSelectionInqParam flowBatchLotSelectionInqParam = new Params.FlowBatchLotSelectionInqParam();
        flowBatchLotSelectionInqParam.setEquipmentID(new ObjectIdentifier("FB101"));
        Results.FlowBatchLotSelectionInqResult flowBatchLotSelectionInqResult = flowBatchTestCase.getCandidateLotsList(flowBatchLotSelectionInqParam);
        List<Infos.TempFlowBatch> strTempFlowBatch = flowBatchLotSelectionInqResult.getStrTempFlowBatch();
        List<ObjectIdentifier> candidateCassettes = strTempFlowBatch.stream().map(Infos.TempFlowBatch::getCassetteID).collect(Collectors.toList());
        Assert.isTrue(!candidateCassettes.containsAll(cassetteList), "test fail");
        //【step4】get last target floatingbatch information
        Results.FloatingBatchListInqResult floatingBatchInfo = flowBatchTestCase.getFloatingBatchInfo(new ObjectIdentifier("FB101"));
        Assert.isTrue(!CimArrayUtils.isEmpty(floatingBatchInfo.getFloatBatches()), "the floating batch info should not be null");
        List<ObjectIdentifier> tmpCassettes = floatingBatchInfo.getFloatBatches().stream().map(floatBatch -> floatBatch.getCassetteID()).collect(Collectors.toList());
        Assert.isTrue(tmpCassettes.containsAll(cassetteList), "test fail");
        // 【step5】change transfer status
        for (ObjectIdentifier cassette : cassetteList){
            commonTestCase.lotCassetteXferStatusChange(cassette, "MI");
        }

        //【step6】re FlowBatching
        Params.ReFlowBatchByManualActionReqParam reFlowBatchByManualActionReqParam = new Params.ReFlowBatchByManualActionReqParam();
        reFlowBatchByManualActionReqParam.setEquipmentID(new ObjectIdentifier("FB101"));
        reFlowBatchByManualActionReqParam.setStrFlowBatchByManualActionReqCassette(floatingBatchInfo.getFloatBatches().stream().map(floatBatch -> {
            Infos.ReFlowBatchByManualActionReqCassette reFlowBatchByManualActionReqCassette = new Infos.ReFlowBatchByManualActionReqCassette();
            reFlowBatchByManualActionReqCassette.setFromFlowBatchID(floatBatch.getFlowBatchID());
            reFlowBatchByManualActionReqCassette.setCassetteID(floatBatch.getCassetteID());
            reFlowBatchByManualActionReqCassette.setLotID(floatBatch.getFlowBatchedLotInfos().stream().map(Infos.FlowBatchedLotInfo::getLotID).collect(Collectors.toList()));
            return reFlowBatchByManualActionReqCassette;
        }).collect(Collectors.toList()));
        flowBatchTestCase.reFlowBatchByManualActionReq(reFlowBatchByManualActionReqParam);
        // 【step7】skip
        operationSkipCase.skipSpecificStep(lots, "4000.0400", true);
        // 【step8】change transfer status
        for (ObjectIdentifier cassette : cassetteList){
            commonTestCase.lotCassetteXferStatusChange(cassette, "MO");
        }
        //【step9】loading
        lotLoadToEquipmentCase.loading_Without_StartLotsReservationSpecifiedEqpAndCassettes(new ObjectIdentifier("FB101"), cassetteList);
        //【step10】move in
        Results.MoveInReqResult moveInReqResult = (Results.MoveInReqResult) moveInCase.onlyMoveIn(cassetteList, new ObjectIdentifier("FB101")).getBody();
        //【step11】move out
        moveOutCase.onlyMoveOut(moveInReqResult.getControlJobID(), new ObjectIdentifier("FB101"));
        //【step12】lotInfo
        for (ObjectIdentifier lot : lots){
            List<ObjectIdentifier> lotIDList = Arrays.asList(lot);
            Results.LotInfoInqResult lotInfoInqResult = (Results.LotInfoInqResult) lotGeneralTestCase.getLotInfoCase(lotIDList).getBody();
            Infos.LotFlowBatchInfo lotFlowBatchInfo = lotInfoInqResult.getLotInfoList().get(0).getLotFlowBatchInfo();
            Assert.isTrue(CimObjectUtils.isEmptyWithValue(lotFlowBatchInfo.getFlowBatchID()) &&  CimObjectUtils.isEmptyWithValue(lotFlowBatchInfo.getFlowBatchReserveEquipmentID()), "test fail");
        }
        //【step13】unload
        lotUnloadFromEquipmentCase.unLoad_WithSpecifiedEqpAndCassettes(new ObjectIdentifier("FB101"), cassetteList);
        // 【step14】skip
        operationSkipCase.skipSpecificStep(lots, "4000.0600", true);
        //【step15】candidate lots list again
        ObjectIdentifier temporaryFlowBatchID = null;
        Params.FlowBatchLotSelectionInqParam flowBatchLotSelectionInqParam2 = new Params.FlowBatchLotSelectionInqParam();
        flowBatchLotSelectionInqParam2.setEquipmentID(new ObjectIdentifier(FLOWBATCH_EQUIPMENTID));
        Results.FlowBatchLotSelectionInqResult flowBatchLotSelectionInqResult2 = flowBatchTestCase.getCandidateLotsList(flowBatchLotSelectionInqParam2);
        List<Infos.TempFlowBatch> strTempFlowBatch2 = flowBatchLotSelectionInqResult2.getStrTempFlowBatch();
        List<ObjectIdentifier> candidateCassettes2 = strTempFlowBatch2.stream().map(Infos.TempFlowBatch::getCassetteID).collect(Collectors.toList());
        Assert.isTrue(candidateCassettes2.containsAll(cassetteList), "not found cassette");
        for (Infos.TempFlowBatch tempFlowBatch : strTempFlowBatch2){
            if (CimObjectUtils.equalsWithValue(tempFlowBatch.getCassetteID(), cassetteList.get(0))){
                temporaryFlowBatchID = tempFlowBatch.getTemporaryFlowBatchID();
                break;
            }
        }
        log.info(tempFlowBathcID.get().getValue());
        if (temporaryFlowBatchID != null) {
            log.info(temporaryFlowBatchID.getValue());
        }
        Assert.isTrue(!CimObjectUtils.equalsWithValue(tempFlowBathcID.get(), temporaryFlowBatchID), "test fail");
    }

    public void flowBatch_LocateToOutside_LeftLotSize_BetweenMaxAndMin(){
        //【step1】candidate lots list
        Params.FlowBatchLotSelectionInqParam flowBatchLotSelectionInqParam = new Params.FlowBatchLotSelectionInqParam();
        flowBatchLotSelectionInqParam.setEquipmentID(new ObjectIdentifier(FLOWBATCH_EQUIPMENTID));
        Results.FlowBatchLotSelectionInqResult flowBatchLotSelectionInqResult = flowBatchTestCase.getCandidateLotsList(flowBatchLotSelectionInqParam);
        List<Infos.TempFlowBatch> strTempFlowBatch = flowBatchLotSelectionInqResult.getStrTempFlowBatch();
        int flowBatchLotMinSize = strTempFlowBatch.get(0).getStrTempFlowBatchLot().get(0).getFlowBatchLotMinSize().intValue();
        // 【step2】produce lot and do flowbatch
        int produceNumber = flowBatchLotMinSize + 1;
        int flowBatchSize = flowBatchLotMinSize + 1;
        TestInfos.FlowBatchResultInfo flowBatchResultInfo = this.productLotsAndDoFlowBatch(produceNumber, flowBatchSize);
        List<ObjectIdentifier> lots = flowBatchResultInfo.getLotList();
        List<ObjectIdentifier> cassetteList = flowBatchResultInfo.getCassetteList();
        // 【step3】flowbatch information
        Params.FlowBatchInfoInqParams flowBatchInfoInqParams = new Params.FlowBatchInfoInqParams();
        flowBatchInfoInqParams.setEquipmentID(new ObjectIdentifier(FLOWBATCH_EQUIPMENTID));
        Results.FlowBatchInfoInqResult flowBatchInfo = flowBatchTestCase.getFlowBatchInfo(flowBatchInfoInqParams);
        Assert.isTrue(!CimArrayUtils.isEmpty(flowBatchInfo.getStrFlowBatchInfoList()), "flow batch information should be not null");
        Assert.isTrue(flowBatchInfo.getStrFlowBatchInfoList().get(0).getFlowBatchedCassetteInfoList().size() == flowBatchLotMinSize + 1, "test fail");
        // 【step4】check flow batch skip
        this.checkFlowBatchSkip(lots.get(0), "1000.0100", false);
        operationSkipCase.skipSpecificStep(Arrays.asList(lots.get(0)),"1000.0100", false);
        // 【step5】flowbatch information
        Params.FlowBatchInfoInqParams flowBatchInfoInqParams2 = new Params.FlowBatchInfoInqParams();
        flowBatchInfoInqParams2.setEquipmentID(new ObjectIdentifier(FLOWBATCH_EQUIPMENTID));
        Results.FlowBatchInfoInqResult flowBatchInfo2 = flowBatchTestCase.getFlowBatchInfo(flowBatchInfoInqParams2);
        Assert.isTrue(!CimArrayUtils.isEmpty(flowBatchInfo2.getStrFlowBatchInfoList()), "flow batch information should be not null");
        Assert.isTrue(flowBatchInfo2.getStrFlowBatchInfoList().get(0).getFlowBatchedCassetteInfoList().size() == flowBatchLotMinSize, "test fail");
        operationSkipCase.skipSpecificStep(Arrays.asList(lots.get(0)),"4000.0100", true);
        //【step6】remove the batch
        Params.FlowBatchLotRemoveReq flowBatchLotRemoveReqParams = new Params.FlowBatchLotRemoveReq();
        flowBatchLotRemoveReqParams.setFlowBatchID(flowBatchInfo2.getStrFlowBatchInfoList().get(0).getFlowBatchID());
        List<Infos.FlowBatchedCassetteInfoExtend> flowBatchedCassetteInfoList = flowBatchInfo2.getStrFlowBatchInfoList().get(0).getFlowBatchedCassetteInfoList();
        List<Infos.RemoveCassette> removeCassetteList = new ArrayList<>();
        flowBatchedCassetteInfoList.forEach(flowBatchedCassetteInfoExtend -> {
            Infos.RemoveCassette removeCassette = new Infos.RemoveCassette();
            removeCassetteList.add(removeCassette);
            removeCassette.setCassetteID(flowBatchedCassetteInfoExtend.getCassetteID());
            removeCassette.setLotID(flowBatchedCassetteInfoExtend.getFlowBatchedLotInfoList().stream().map(Infos.FlowBatchedLotInfoExtend::getLotID).collect(Collectors.toList()));
        });
        flowBatchLotRemoveReqParams.setStrRemoveCassette(removeCassetteList);
        flowBatchTestCase.flowBatchLotRemoveReq(flowBatchLotRemoveReqParams);
        //【step7】candidate lots list again
        Params.FlowBatchLotSelectionInqParam flowBatchLotSelectionInqParam3 = new Params.FlowBatchLotSelectionInqParam();
        flowBatchLotSelectionInqParam3.setEquipmentID(new ObjectIdentifier(FLOWBATCH_EQUIPMENTID));
        Results.FlowBatchLotSelectionInqResult flowBatchLotSelectionInqResult3 = flowBatchTestCase.getCandidateLotsList(flowBatchLotSelectionInqParam3);
        List<Infos.TempFlowBatch> strTempFlowBatch3 = flowBatchLotSelectionInqResult3.getStrTempFlowBatch();
        List<ObjectIdentifier> candidateCassettes2 = strTempFlowBatch3.stream().map(Infos.TempFlowBatch::getCassetteID).collect(Collectors.toList());
        Assert.isTrue(candidateCassettes2.containsAll(cassetteList), "not found cassette");
        // 【step8】do flowbatch
        List<Infos.FlowBatchByManualActionReqCassette> flowBatchByManualActionReqCassette2 = this.getFlowBatchByManualActionReqCassette(strTempFlowBatch3, cassetteList);
        Params.FlowBatchByManualActionReqParam flowBatchByManualActionReqParam2 = new Params.FlowBatchByManualActionReqParam();
        flowBatchByManualActionReqParam2.setEquipmentID(new ObjectIdentifier(FLOWBATCH_EQUIPMENTID));
        flowBatchByManualActionReqParam2.setStrFlowBatchByManualActionReqCassette(flowBatchByManualActionReqCassette2);
        flowBatchTestCase.flowBatchByManualActionReq(flowBatchByManualActionReqParam2);
        // 【step9】lot Hold
        String reasonCode = "SOHL";
        String reasonableOperation = "C";
        lotHoldCase.lotHold(lots.get(0).getValue(), reasonCode, reasonableOperation);
        // 【step10】check flow batch skip
        this.checkFlowBatchSkip(lots.get(0), "1000.0100", false);
        List<Infos.LotInfo> lotInfoList1 = this.forceSkipSpecificStep(Arrays.asList(lots.get(0)), "1000.0100", false);
        Assert.isTrue(lotInfoList1.get(0).getLotBasicInfo().getLotStatus().equals("ONHOLD"), "test fail");
    }

    public void flowBatch_LocateToOutside_LeftLotSize_LessThanMin(){
        //【step1】candidate lots list
        Params.FlowBatchLotSelectionInqParam flowBatchLotSelectionInqParam = new Params.FlowBatchLotSelectionInqParam();
        flowBatchLotSelectionInqParam.setEquipmentID(new ObjectIdentifier(FLOWBATCH_EQUIPMENTID));
        Results.FlowBatchLotSelectionInqResult flowBatchLotSelectionInqResult = flowBatchTestCase.getCandidateLotsList(flowBatchLotSelectionInqParam);
        List<Infos.TempFlowBatch> strTempFlowBatch = flowBatchLotSelectionInqResult.getStrTempFlowBatch();
        int flowBatchLotMinSize = strTempFlowBatch.get(0).getStrTempFlowBatchLot().get(0).getFlowBatchLotMinSize().intValue();
        // 【step2】produce lot and do flowbatch
        int produceNumber = flowBatchLotMinSize;
        int flowBatchSize = flowBatchLotMinSize;
        TestInfos.FlowBatchResultInfo flowBatchResultInfo = this.productLotsAndDoFlowBatch(produceNumber, flowBatchSize);
        List<ObjectIdentifier> lots = flowBatchResultInfo.getLotList();
        // 【step3】flowbatch information
        Params.FlowBatchInfoInqParams flowBatchInfoInqParams = new Params.FlowBatchInfoInqParams();
        flowBatchInfoInqParams.setEquipmentID(new ObjectIdentifier(FLOWBATCH_EQUIPMENTID));
        Results.FlowBatchInfoInqResult flowBatchInfo = flowBatchTestCase.getFlowBatchInfo(flowBatchInfoInqParams);
        Assert.isTrue(!CimArrayUtils.isEmpty(flowBatchInfo.getStrFlowBatchInfoList()), "flow batch information should be not null");
        Assert.isTrue(flowBatchInfo.getStrFlowBatchInfoList().get(0).getFlowBatchedCassetteInfoList().size() == flowBatchLotMinSize, "test fail");
        // 【step4】check flow batch skip
        this.checkFlowBatchSkip(lots.get(0), "1000.0100", false);
        try {
            operationSkipCase.skipSpecificStep(Arrays.asList(lots.get(0)),"1000.0100", false);
            Assert.isTrue(false, "test fail");
        } catch (ServiceException e) {
            Assert.isTrue(Validations.isEquals(retCodeConfig.getInvalidCassetteCountForBatch(), e.getCode()), e.getMessage());
        }
        // 【step5】skip over target operation
        operationSkipCase.skipSpecificStep(lots, "4000.0200", true);
        try {
            operationSkipCase.skipSpecificStep(Arrays.asList(lots.get(0)), "4000.0300", true);
            Assert.isTrue(false, "test fail");
        } catch (ServiceException e) {
            Assert.isTrue(Validations.isEquals(retCodeConfig.getNotLocateToOverTarget(), e.getCode()), e.getMessage());
        }
    }

    public void two_floatingBatch_to_consist_ofOneBatch(){
        ObjectIdentifier tmpFlowBatchID1 = null;
        ObjectIdentifier tmpFlowBatchID2 = null;
        //【step1】get stocker list
        Infos.StockerInfo stockerInfo = commonTestCase.getStocker();
        //【step2】candidate lots list
        Params.FlowBatchLotSelectionInqParam flowBatchLotSelectionInqParam = new Params.FlowBatchLotSelectionInqParam();
        flowBatchLotSelectionInqParam.setEquipmentID(new ObjectIdentifier(FLOWBATCH_EQUIPMENTID));
        Results.FlowBatchLotSelectionInqResult flowBatchLotSelectionInqResult = flowBatchTestCase.getCandidateLotsList(flowBatchLotSelectionInqParam);
        List<Infos.TempFlowBatch> strTempFlowBatch = flowBatchLotSelectionInqResult.getStrTempFlowBatch();
        int flowBatchLotMinSize = strTempFlowBatch.get(0).getStrTempFlowBatchLot().get(0).getFlowBatchLotMinSize().intValue();
        // 【step3】produce flowBatchLotMinSize lot
        List<Infos.LotInfo> lotInfoList = produceLotsAndSkipToTheFlowBatch(flowBatchLotMinSize * 2, true);
        List<ObjectIdentifier> cassetteList = lotInfoList.stream().map(lotInfo -> lotInfo.getLotLocationInfo().getCassetteID()).collect(Collectors.toList());
        List<ObjectIdentifier> lots = lotInfoList.stream().map(lotInfo -> lotInfo.getLotBasicInfo().getLotID()).collect(Collectors.toList());
        // 【step4】change transfer status
        for (ObjectIdentifier cassette : cassetteList){
            commonTestCase.lotCassetteXferStatusChange(cassette, "MI");
        }
        //【step5】candidate lots list again
        Params.FlowBatchLotSelectionInqParam flowBatchLotSelectionInqParam2 = new Params.FlowBatchLotSelectionInqParam();
        flowBatchLotSelectionInqParam2.setEquipmentID(new ObjectIdentifier(FLOWBATCH_EQUIPMENTID));
        Results.FlowBatchLotSelectionInqResult flowBatchLotSelectionInqResult2 = flowBatchTestCase.getCandidateLotsList(flowBatchLotSelectionInqParam2);
        List<Infos.TempFlowBatch> strTempFlowBatch2 = flowBatchLotSelectionInqResult2.getStrTempFlowBatch();
        List<ObjectIdentifier> candidateCassettes = strTempFlowBatch2.stream().map(Infos.TempFlowBatch::getCassetteID).collect(Collectors.toList());
        Assert.isTrue(candidateCassettes.containsAll(cassetteList), "not found cassette");

        //【step6】change the max count
        int maxCountForFlowBatch = flowBatchLotSelectionInqResult2.getMaxCountForFlowBatch().intValue();
        if (maxCountForFlowBatch < 2){
            Params.EqpMaxFlowbCountModifyReqParams eqpMaxFlowbCountModifyReqParams = new Params.EqpMaxFlowbCountModifyReqParams();
            eqpMaxFlowbCountModifyReqParams.setEquipmentID(new ObjectIdentifier(FLOWBATCH_EQUIPMENTID));
            eqpMaxFlowbCountModifyReqParams.setFlowBatchMaxCount(2);
            flowBatchTestCase.eqpMaxFlowbCountModifyReq(eqpMaxFlowbCountModifyReqParams);
        }
        // 【step7】do flowbatch
        List<Infos.FlowBatchByManualActionReqCassette> flowBatchByManualActionReqCassette1 = this.getFlowBatchByManualActionReqCassette(strTempFlowBatch2, cassetteList.subList(0, flowBatchLotMinSize));
        Params.FlowBatchByManualActionReqParam flowBatchByManualActionReqParam1 = new Params.FlowBatchByManualActionReqParam();
        flowBatchByManualActionReqParam1.setEquipmentID(new ObjectIdentifier(FLOWBATCH_EQUIPMENTID));
        flowBatchByManualActionReqParam1.setStrFlowBatchByManualActionReqCassette(flowBatchByManualActionReqCassette1);
        Results.FlowBatchByManualActionReqResult flowBatchByManualActionReqResult1 = (Results.FlowBatchByManualActionReqResult) flowBatchTestCase.flowBatchByManualActionReq(flowBatchByManualActionReqParam1).getBody();
        tmpFlowBatchID1 = flowBatchByManualActionReqResult1.getFlowBatchID();

        List<Infos.FlowBatchByManualActionReqCassette> flowBatchByManualActionReqCassette2 = this.getFlowBatchByManualActionReqCassette(strTempFlowBatch2, cassetteList.subList(flowBatchLotMinSize, cassetteList.size()));
        Params.FlowBatchByManualActionReqParam flowBatchByManualActionReqParam2 = new Params.FlowBatchByManualActionReqParam();
        flowBatchByManualActionReqParam2.setEquipmentID(new ObjectIdentifier(FLOWBATCH_EQUIPMENTID));
        flowBatchByManualActionReqParam2.setStrFlowBatchByManualActionReqCassette(flowBatchByManualActionReqCassette2);
        Results.FlowBatchByManualActionReqResult flowBatchByManualActionReqResult2 = (Results.FlowBatchByManualActionReqResult) flowBatchTestCase.flowBatchByManualActionReq(flowBatchByManualActionReqParam2).getBody();
        tmpFlowBatchID2 = flowBatchByManualActionReqResult2.getFlowBatchID();

        // 【step8】flowbatch information
        Params.FlowBatchInfoInqParams flowBatchInfoInqParams = new Params.FlowBatchInfoInqParams();
        flowBatchInfoInqParams.setEquipmentID(new ObjectIdentifier(FLOWBATCH_EQUIPMENTID));
        Results.FlowBatchInfoInqResult flowBatchInfo = flowBatchTestCase.getFlowBatchInfo(flowBatchInfoInqParams);
        Assert.isTrue(!CimArrayUtils.isEmpty(flowBatchInfo.getStrFlowBatchInfoList()), "flow batch information should be not null");

        //【step9】batch reserve cancel
        Params.EqpReserveCancelForflowBatchReqParams eqpReserveCancelForflowBatchReqParams = new Params.EqpReserveCancelForflowBatchReqParams();
        eqpReserveCancelForflowBatchReqParams.setEquipmentID(flowBatchInfo.getReservedEquipmentID());
        List<Infos.FlowBatchInfo> strFlowBatchInfoList = flowBatchInfo.getStrFlowBatchInfoList();
        for (Infos.FlowBatchInfo flowBatchInfo1 : strFlowBatchInfoList){
            eqpReserveCancelForflowBatchReqParams.setFlowBatchID(flowBatchInfo1.getFlowBatchID());
            flowBatchTestCase.eqpReserveCancelForflowBatchReq(eqpReserveCancelForflowBatchReqParams);
        }

        //【step10】get Floating batch info
        Results.FloatingBatchListInqResult floatingBatchInfo = flowBatchTestCase.getFloatingBatchInfo(new ObjectIdentifier(FLOWBATCH_EQUIPMENTID));
        Assert.isTrue(!CimArrayUtils.isEmpty(floatingBatchInfo.getFloatBatches()), "the floating batch info should not be null");
        Assert.isTrue(CimArrayUtils.getSize(floatingBatchInfo.getFloatBatches()) == flowBatchLotMinSize * 2, "test fail");
        //【step11】re FlowBatching
        Params.ReFlowBatchByManualActionReqParam reFlowBatchByManualActionReqParam = new Params.ReFlowBatchByManualActionReqParam();
        reFlowBatchByManualActionReqParam.setEquipmentID(new ObjectIdentifier(FLOWBATCH_EQUIPMENTID));
        reFlowBatchByManualActionReqParam.setStrFlowBatchByManualActionReqCassette(floatingBatchInfo.getFloatBatches().stream().map(floatBatch -> {
            Infos.ReFlowBatchByManualActionReqCassette reFlowBatchByManualActionReqCassette = new Infos.ReFlowBatchByManualActionReqCassette();
            reFlowBatchByManualActionReqCassette.setFromFlowBatchID(floatBatch.getFlowBatchID());
            reFlowBatchByManualActionReqCassette.setCassetteID(floatBatch.getCassetteID());
            reFlowBatchByManualActionReqCassette.setLotID(floatBatch.getFlowBatchedLotInfos().stream().map(Infos.FlowBatchedLotInfo::getLotID).collect(Collectors.toList()));
            return reFlowBatchByManualActionReqCassette;
        }).collect(Collectors.toList()));
        Results.ReFlowBatchByManualActionReqResult reFlowBatchByManualActionReqResult = flowBatchTestCase.reFlowBatchByManualActionReq(reFlowBatchByManualActionReqParam);
        Assert.isTrue(!CimObjectUtils.equalsWithValue(reFlowBatchByManualActionReqResult.getFlowBatchID(), tmpFlowBatchID1) && !CimObjectUtils.equalsWithValue(reFlowBatchByManualActionReqResult.getFlowBatchID(), tmpFlowBatchID2), "test fail");
    }

    public void someOfRelated_FlowBatchInhibited(){
        //【step1】candidate lots list
        Params.FlowBatchLotSelectionInqParam flowBatchLotSelectionInqParam = new Params.FlowBatchLotSelectionInqParam();
        flowBatchLotSelectionInqParam.setEquipmentID(new ObjectIdentifier(FLOWBATCH_EQUIPMENTID));
        Results.FlowBatchLotSelectionInqResult flowBatchLotSelectionInqResult = flowBatchTestCase.getCandidateLotsList(flowBatchLotSelectionInqParam);
        List<Infos.TempFlowBatch> strTempFlowBatch = flowBatchLotSelectionInqResult.getStrTempFlowBatch();
        int flowBatchLotMinSize = strTempFlowBatch.get(0).getStrTempFlowBatchLot().get(0).getFlowBatchLotMinSize().intValue();
        // 【step2】produce lot and do flowbatch
        int produceNumber = flowBatchLotMinSize;
        int flowBatchSize = flowBatchLotMinSize;
        TestInfos.FlowBatchResultInfo flowBatchResultInfo = this.productLotsAndDoFlowBatch(produceNumber, flowBatchSize);
        List<ObjectIdentifier> lots = flowBatchResultInfo.getLotList();
        // 【step3】inhibit
        entityInhibityCase.inhibitSpecCondition(new ObjectIdentifier(FLOWBATCH_EQUIPMENTID));
        // 【step4】get lot info
        List<ObjectIdentifier> lotIDList = new ArrayList<>();
        lotIDList.add(lots.get(0));
        Results.LotInfoInqResult lotInfoInqResult = (Results.LotInfoInqResult) lotGeneralTestCase.getLotInfoCase(lotIDList).getBody();
        List<Infos.EntityInhibitAttributes> entityInhibitAttributesList = lotInfoInqResult.getLotInfoList().get(0).getEntityInhibitAttributesList();
        Assert.isTrue(!CimArrayUtils.isEmpty(entityInhibitAttributesList), "test fail");
        //【step5】einfo/eqp_info/inq
        Results.EqpInfoInqResult eqpInfoInqResult = (Results.EqpInfoInqResult) electronicInformationTestCase.eqpInfoInqCase(new ObjectIdentifier(FLOWBATCH_EQUIPMENTID)).getBody();
        Assert.isTrue(!CimArrayUtils.isEmpty(eqpInfoInqResult.getConstraintList()), "test fail");
    }

    public void flowBatched_ReworkBranch(){
        //【step1】candidate lots list
        Params.FlowBatchLotSelectionInqParam flowBatchLotSelectionInqParam = new Params.FlowBatchLotSelectionInqParam();
        flowBatchLotSelectionInqParam.setEquipmentID(new ObjectIdentifier(FLOWBATCH_EQUIPMENTID));
        Results.FlowBatchLotSelectionInqResult flowBatchLotSelectionInqResult = flowBatchTestCase.getCandidateLotsList(flowBatchLotSelectionInqParam);
        List<Infos.TempFlowBatch> strTempFlowBatch = flowBatchLotSelectionInqResult.getStrTempFlowBatch();
        int flowBatchLotMinSize = strTempFlowBatch.get(0).getStrTempFlowBatchLot().get(0).getFlowBatchLotMinSize().intValue();
        // 【step2】produce lot and do flowbatch
        int produceNumber = flowBatchLotMinSize;
        int flowBatchSize = flowBatchLotMinSize;
        TestInfos.FlowBatchResultInfo flowBatchResultInfo = this.productLotsAndDoFlowBatch(produceNumber, flowBatchSize);

        List<ObjectIdentifier> cassetteList = flowBatchResultInfo.getCassetteList();
        List<ObjectIdentifier> lots = flowBatchResultInfo.getLotList();

        // 【step3】get lot info
        List<ObjectIdentifier> lotIDList = new ArrayList<>();
        lotIDList.add(lots.get(0));
        List<Infos.LotInfo> lotInfoList1 = operationSkipCase.skipSpecificStep(lotIDList, "4000.0160", true);

        // 【step4】full rework
        reworkCase.reworkWholeLotReq(lotInfoList1.get(0));
        // 【step5】get lot info
        List<ObjectIdentifier> lotIDList2 = new ArrayList<>();
        lotIDList2.add(lots.get(0));
        Results.LotInfoInqResult lotInfoInqResult2 = (Results.LotInfoInqResult) lotGeneralTestCase.getLotInfoCase(lotIDList2).getBody();
        Assert.isTrue(lotInfoInqResult2.getLotInfoList().get(0).getLotOperationInfo().getOperationNumber().equals("1000.0100"), "test fail");
        try {
            operationSkipCase.skipSpecificStep(lotIDList2, "4000.0160", true);
            Assert.isTrue(false, "test fail");
        } catch (ServiceException e) {
           Assert.isTrue(Validations.isEquals(retCodeConfig.getNotLocateToBatchOpe(), e.getCode()), e.getMessage());
        }
    }

    public void flowBatch_Reserve_TwoLots_In_TargetEquipment(){
        //【step1】candidate lots list
        Params.FlowBatchLotSelectionInqParam flowBatchLotSelectionInqParam = new Params.FlowBatchLotSelectionInqParam();
        flowBatchLotSelectionInqParam.setEquipmentID(new ObjectIdentifier(FLOWBATCH_EQUIPMENTID));
        Results.FlowBatchLotSelectionInqResult flowBatchLotSelectionInqResult = flowBatchTestCase.getCandidateLotsList(flowBatchLotSelectionInqParam);
        List<Infos.TempFlowBatch> strTempFlowBatch = flowBatchLotSelectionInqResult.getStrTempFlowBatch();
        int flowBatchLotMinSize = strTempFlowBatch.get(0).getStrTempFlowBatchLot().get(0).getFlowBatchLotMinSize().intValue();
        // 【step2】produce lot and do flowbatch
        int produceNumber = flowBatchLotMinSize;
        int flowBatchSize = flowBatchLotMinSize;
        TestInfos.FlowBatchResultInfo flowBatchResultInfo = this.productLotsAndDoFlowBatch(produceNumber, flowBatchSize);
        List<ObjectIdentifier> lots = flowBatchResultInfo.getLotList();
        // 【step3】skip
        operationSkipCase.skipSpecificStep(lots, "4000.0200", true);
        //【step4】eqp reserve
        try {
            startLotsReservationCase.moveInReserveReqWhithSpecifiedLotsAndEqp(Collections.singletonList(lots.get(0)), new ObjectIdentifier(FLOWBATCH_EQUIPMENTID));
            Assert.isTrue(false, "test fail");
        } catch (ServiceException e) {
            Assert.isTrue(Validations.isEquals(retCodeConfig.getFlowBatchLotsMissing(), e.getCode()), e.getMessage());
        }
    }

    public void flowBatch_ChangeMaxCount(){
        // 【step1】eqp info
        Results.EqpInfoInqResult eqpInfoInqResult = (Results.EqpInfoInqResult) electronicInformationTestCase.eqpInfoInqCase(new ObjectIdentifier(FLOWBATCH_EQUIPMENTID)).getBody();
        Integer maxCountForFlowBatch = eqpInfoInqResult.getEquipmentStatusInfo().getMaxCountForFlowBatch();
        // 【step2】change max count
        Params.EqpMaxFlowbCountModifyReqParams eqpMaxFlowbCountModifyReqParams = new Params.EqpMaxFlowbCountModifyReqParams();
        eqpMaxFlowbCountModifyReqParams.setEquipmentID(new ObjectIdentifier(FLOWBATCH_EQUIPMENTID));
        eqpMaxFlowbCountModifyReqParams.setFlowBatchMaxCount(maxCountForFlowBatch + 1);
        flowBatchTestCase.eqpMaxFlowbCountModifyReq(eqpMaxFlowbCountModifyReqParams);
        //【step2】candidate lots list
        Params.FlowBatchLotSelectionInqParam flowBatchLotSelectionInqParam = new Params.FlowBatchLotSelectionInqParam();
        flowBatchLotSelectionInqParam.setEquipmentID(new ObjectIdentifier(FLOWBATCH_EQUIPMENTID));
        Results.FlowBatchLotSelectionInqResult flowBatchLotSelectionInqResult = flowBatchTestCase.getCandidateLotsList(flowBatchLotSelectionInqParam);
        // 【step3】change max count again
        Params.EqpMaxFlowbCountModifyReqParams eqpMaxFlowbCountModifyReqParams2 = new Params.EqpMaxFlowbCountModifyReqParams();
        eqpMaxFlowbCountModifyReqParams2.setEquipmentID(new ObjectIdentifier(FLOWBATCH_EQUIPMENTID));
        eqpMaxFlowbCountModifyReqParams2.setFlowBatchMaxCount(maxCountForFlowBatch);
        flowBatchTestCase.eqpMaxFlowbCountModifyReq(eqpMaxFlowbCountModifyReqParams2);

        //【step4】get Flow batch info
        Params.FlowBatchInfoInqParams flowBatchInfoInqParams = new Params.FlowBatchInfoInqParams();
        flowBatchInfoInqParams.setEquipmentID(new ObjectIdentifier(FLOWBATCH_EQUIPMENTID));
        try {
            flowBatchTestCase.getFlowBatchInfo(flowBatchInfoInqParams);
        } catch (ServiceException e) {
            if (!Validations.isEquals(retCodeConfig.getNotFoundFlowBatch(), e.getCode())){
                throw e;
            }
        }
        // 【step5】change max count again
        Params.EqpMaxFlowbCountModifyReqParams eqpMaxFlowbCountModifyReqParams3 = new Params.EqpMaxFlowbCountModifyReqParams();
        eqpMaxFlowbCountModifyReqParams3.setEquipmentID(new ObjectIdentifier(FLOWBATCH_EQUIPMENTID));
        eqpMaxFlowbCountModifyReqParams3.setFlowBatchMaxCount(maxCountForFlowBatch + 1);
        flowBatchTestCase.eqpMaxFlowbCountModifyReq(eqpMaxFlowbCountModifyReqParams3);

        // 【step4】produce lot and do flowbatch
        List<Infos.TempFlowBatch> strTempFlowBatch = flowBatchLotSelectionInqResult.getStrTempFlowBatch();
        int flowBatchLotMinSize = strTempFlowBatch.get(0).getStrTempFlowBatchLot().get(0).getFlowBatchLotMinSize().intValue();
        int produceNumber = flowBatchLotMinSize;
        int flowBatchSize = flowBatchLotMinSize;
        this.productLotsAndDoFlowBatch(produceNumber, flowBatchSize);

        // 【step5】change max count again
        Params.EqpMaxFlowbCountModifyReqParams eqpMaxFlowbCountModifyReqParams4 = new Params.EqpMaxFlowbCountModifyReqParams();
        eqpMaxFlowbCountModifyReqParams4.setEquipmentID(new ObjectIdentifier(FLOWBATCH_EQUIPMENTID));
        eqpMaxFlowbCountModifyReqParams4.setFlowBatchMaxCount(maxCountForFlowBatch);
        try {
            flowBatchTestCase.eqpMaxFlowbCountModifyReq(eqpMaxFlowbCountModifyReqParams4);
            Assert.isTrue(false, "test fail");
        } catch (ServiceException e) {
            Assert.isTrue(Validations.isEquals(retCodeConfig.getEqpReservedForSomeFlowBatch(), e.getCode()), e.getMessage());
        }
    }

    public void batchInforSearchInLotInformationPageWhichLotHasNoBatchInformation(){
        //【step1】stb
        List<ObjectIdentifier> lots = stbCase.stb_NLots_NotPreparedCase(1, true);
        //【step2】get batch information
        Params.FlowBatchInfoInqParams flowBatchInfoInqParams = new Params.FlowBatchInfoInqParams();
        flowBatchInfoInqParams.setLotID(lots.get(0));
        try {
            flowBatchTestCase.getFlowBatchInfo(flowBatchInfoInqParams);
            Assert.isTrue(false, "need throw exception");
        } catch (ServiceException e) {
            Assert.isTrue(Validations.isEquals(retCodeConfig.getNotFoundFlowBatch(), e.getCode()), e.getMessage());
        }
    }

    public void batchInforSearchInLotInformationPageWhichLotExistBatchID(){
        //【step1】candidate lots list
        Params.FlowBatchLotSelectionInqParam flowBatchLotSelectionInqParam = new Params.FlowBatchLotSelectionInqParam();
        flowBatchLotSelectionInqParam.setEquipmentID(new ObjectIdentifier(FLOWBATCH_EQUIPMENTID));
        Results.FlowBatchLotSelectionInqResult flowBatchLotSelectionInqResult = flowBatchTestCase.getCandidateLotsList(flowBatchLotSelectionInqParam);
        List<Infos.TempFlowBatch> strTempFlowBatch = flowBatchLotSelectionInqResult.getStrTempFlowBatch();
        int flowBatchLotMinSize = strTempFlowBatch.get(0).getStrTempFlowBatchLot().get(0).getFlowBatchLotMinSize().intValue();
        // 【step2】produce lot and do flowbatch
        TestInfos.FlowBatchResultInfo flowBatchResultInfo = this.productLotsAndDoFlowBatch(flowBatchLotMinSize, flowBatchLotMinSize);
        // 【step3】lot info
        List<Infos.LotInfo> lotInfoList = lotGeneralTestCase.getLotInfos(Arrays.asList(flowBatchResultInfo.getLotList().get(0)));
        Assert.isTrue(lotInfoList.get(0).getLotFlowBatchInfo().getFlowBatchID() != null, "test fail");
        // 【step4】get batch information
        Params.FlowBatchInfoInqParams flowBatchInfoInqParams = new Params.FlowBatchInfoInqParams();
        flowBatchInfoInqParams.setEquipmentID(lotInfoList.get(0).getLotFlowBatchInfo().getFlowBatchReserveEquipmentID());
        Results.FlowBatchInfoInqResult flowBatchInfo = flowBatchTestCase.getFlowBatchInfo(flowBatchInfoInqParams);
        Assert.isTrue(CimArrayUtils.getSize(flowBatchInfo.getStrFlowBatchInfoList().get(0).getFlowBatchedCassetteInfoList()) == flowBatchLotMinSize, "test fail");
    }

    public void flowBatchLostLotsList(){
        //【step1】candidate lots list
        Params.FlowBatchLotSelectionInqParam flowBatchLotSelectionInqParam = new Params.FlowBatchLotSelectionInqParam();
        flowBatchLotSelectionInqParam.setEquipmentID(new ObjectIdentifier(FLOWBATCH_EQUIPMENTID));
        Results.FlowBatchLotSelectionInqResult flowBatchLotSelectionInqResult = flowBatchTestCase.getCandidateLotsList(flowBatchLotSelectionInqParam);
        List<Infos.TempFlowBatch> strTempFlowBatch = flowBatchLotSelectionInqResult.getStrTempFlowBatch();
        int flowBatchLotMinSize = strTempFlowBatch.get(0).getStrTempFlowBatchLot().get(0).getFlowBatchLotMinSize().intValue();
        int produceNumber = flowBatchLotMinSize + 1;
        int flowBatchSize = flowBatchLotMinSize + 1;
        // 【step2】produce lot and do flowbatch
        TestInfos.FlowBatchResultInfo flowBatchResultInfo = this.productLotsAndDoFlowBatch(produceNumber, flowBatchSize);
        // 【step3】skip
        List<Infos.LotInfo> lotInfoList = operationSkipCase.skipSpecificStep(Arrays.asList(flowBatchResultInfo.getLotList().get(0)), "4000.0200", true);
        // 【step4】get Flow batch info
        Params.FlowBatchInfoInqParams flowBatchInfoInqParams = new Params.FlowBatchInfoInqParams();
        flowBatchInfoInqParams.setEquipmentID(new ObjectIdentifier(FLOWBATCH_EQUIPMENTID));
        Results.FlowBatchInfoInqResult flowBatchInfo = flowBatchTestCase.getFlowBatchInfo(flowBatchInfoInqParams);
        Assert.isTrue(!CimArrayUtils.isEmpty(flowBatchInfo.getStrFlowBatchInfoList()), "flow batch information should be not null");
        // 【step5】remove one lot from the batch
        Params.FlowBatchLotRemoveReq flowBatchLotRemoveReqParams = new Params.FlowBatchLotRemoveReq();
        flowBatchLotRemoveReqParams.setFlowBatchID(flowBatchInfo.getStrFlowBatchInfoList().get(0).getFlowBatchID());
        Infos.RemoveCassette removeCassette = new Infos.RemoveCassette();
        removeCassette.setCassetteID(lotInfoList.get(0).getLotLocationInfo().getCassetteID());
        removeCassette.setLotID(Arrays.asList(lotInfoList.get(0).getLotBasicInfo().getLotID()));
        flowBatchLotRemoveReqParams.setStrRemoveCassette(Arrays.asList(removeCassette));
        flowBatchTestCase.flowBatchLotRemoveReq(flowBatchLotRemoveReqParams);
        // 【step6】get lost lots
        List<Infos.FlowBatchLostLotInfo> flowBatchLostLotInfos = flowBatchTestCase.getflowBatchLostLots();
        List<ObjectIdentifier> lostLots = flowBatchLostLotInfos.stream().map(Infos.FlowBatchLostLotInfo::getLotID).collect(Collectors.toList());
        List<ObjectIdentifier> notLostLots = flowBatchResultInfo.getLotList().stream().filter(lotID -> !CimObjectUtils.equalsWithValue(lotID, flowBatchResultInfo.getLotList().get(0))).collect(Collectors.toList());
        boolean isNotContains = true;
        for (ObjectIdentifier notLostLot : notLostLots){
            if (lostLots.contains(notLostLot)){
                isNotContains = false;
                break;
            }
        }
        Assert.isTrue(lostLots.contains(flowBatchResultInfo.getLotList().get(0)) && isNotContains, "validate fail");
    }

    public void autoFlowBatchWhenSomeOfLotsHasBeenSplited(){
        //【step1】candidate lots list
        Params.FlowBatchLotSelectionInqParam flowBatchLotSelectionInqParam = new Params.FlowBatchLotSelectionInqParam();
        flowBatchLotSelectionInqParam.setEquipmentID(new ObjectIdentifier(FLOWBATCH_EQUIPMENTID));
        Results.FlowBatchLotSelectionInqResult flowBatchLotSelectionInqResult = flowBatchTestCase.getCandidateLotsList(flowBatchLotSelectionInqParam);
        List<Infos.TempFlowBatch> strTempFlowBatch = flowBatchLotSelectionInqResult.getStrTempFlowBatch();
        int flowBatchLotMaxSize = strTempFlowBatch.get(0).getStrTempFlowBatchLot().get(0).getFlowBatchLotSize().intValue();
        List<Infos.LotInfo> lotInfoList = operationSkipCase.stbNLotsAndSkipSpecificStep(flowBatchLotMaxSize, true, "4000.0100", true);
        // 【step3】split
        lotSplitCase.splitBySpecificLotID(lotInfoList.get(0).getLotBasicInfo().getLotID());
        // 【step4】candidate lots list
        List<ObjectIdentifier> cassetteList = lotInfoList.stream().map(lotInfo -> lotInfo.getLotLocationInfo().getCassetteID()).collect(Collectors.toList());
        Params.FlowBatchLotSelectionInqParam flowBatchLotSelectionInqParam2 = new Params.FlowBatchLotSelectionInqParam();
        flowBatchLotSelectionInqParam2.setEquipmentID(new ObjectIdentifier(FLOWBATCH_EQUIPMENTID));
        Results.FlowBatchLotSelectionInqResult flowBatchLotSelectionInqResult2 = flowBatchTestCase.getCandidateLotsList(flowBatchLotSelectionInqParam2);
        List<Infos.TempFlowBatch> strTempFlowBatch2 = flowBatchLotSelectionInqResult2.getStrTempFlowBatch();
        List<ObjectIdentifier> candidateCassettes2 = strTempFlowBatch2.stream().map(Infos.TempFlowBatch::getCassetteID).collect(Collectors.toList());
        Assert.isTrue(candidateCassettes2.containsAll(cassetteList), "not found cassette");
        // 【step5】change cassettes xfer status
        for (ObjectIdentifier cassetteID : cassetteList){
            commonTestCase.lotCassetteXferStatusChange(cassetteID, "MI");
        }
        // 【step6】auto dispatch
        Params.FlowBatchByAutoActionReqParams autoFlowBatchByManualActionReqParams = new Params.FlowBatchByAutoActionReqParams();
        autoFlowBatchByManualActionReqParams.setEquipmentID(new ObjectIdentifier(FLOWBATCH_EQUIPMENTID));
        flowBatchTestCase.autoFlowBatchByManualActionReq(autoFlowBatchByManualActionReqParams);
    }
    
}