package com.fa.cim.service.flowbatch.impl;

import com.fa.cim.annotaion.OmService;
import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.OmCode;
import com.fa.cim.common.utils.*;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.dto.*;
import com.fa.cim.method.*;
import com.fa.cim.newcore.bo.machine.CimMachine;
import com.fa.cim.newcore.impl.bo.env.StandardProperties;
import com.fa.cim.service.flowbatch.IFlowBatchInqService;
import com.fa.cim.service.flowbatch.IFlowBatchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

/**
 * description:
 * <p>FlowBatchService .<br/></p>
 * <p>
 * change history:
 * date      defect#       person     comments
 * ------------------------------------------------------------
 * ---------------------------------------------------------
 * 2020/9/8/008   ********     Decade     create file
 *
 * @author: Decade
 * @date: 2020/9/8/008 16:46
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@OmService
@Slf4j
public class FlowBatchServiceImpl implements IFlowBatchService {

    @Autowired
    private IFlowBatchMethod flowBatchMethod;

    @Autowired
    private RetCodeConfig retCodeConfig;

    @Autowired
    private IEquipmentMethod equipmentMethod;

    @Autowired
    private ICassetteMethod cassetteMethod;

    @Autowired
    private ILotMethod lotMethod;

    @Autowired
    private IPersonMethod personMethod;

    @Autowired
    private IEventMethod eventMethod;

    @Autowired
    private IProcessMethod processMethod;

    @Autowired
    private IObjectMethod objectMethod;

    @Autowired
    private IObjectLockMethod objectLockMethod;

    @Autowired
    private IFlowBatchInqService flowBatchInqService;

    @Autowired
    private IContaminationMethod contaminationMethod;

    @Autowired
    private IWaferMethod waferMethod;

    @Override
    public Results.FlowBatchByManualActionReqResult sxFlowBatchByManualActionReq(Infos.ObjCommon objCommon, Params.FlowBatchByManualActionReqParam param) {
        ObjectIdentifier equipmentID = param.getEquipmentID();
        Results.FlowBatchByManualActionReqResult out = new Results.FlowBatchByManualActionReqResult();
        // step1 object_lockMode_Get
        Inputs.ObjLockModeIn objLockModeIn = new Inputs.ObjLockModeIn();
        objLockModeIn.setObjectID(equipmentID);
        objLockModeIn.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
        objLockModeIn.setFunctionCategory(TransactionIDEnum.FLOW_BATCHING_REQ.getValue());
        objLockModeIn.setUserDataUpdateFlag(false);
        Outputs.ObjLockModeOut objLockModeOut = objectMethod.objectLockModeGet(objCommon, objLockModeIn);
        Long lockMode = objLockModeOut.getLockMode();

        // 增加拦截，避免出现脏数据
        List<Infos.FlowBatchByManualActionReqCassette> strFlowBatchByManualActionReqCassette = param.getStrFlowBatchByManualActionReqCassette();
        List<ObjectIdentifier> cassetteIDs =new ArrayList<>();
        for (Infos.FlowBatchByManualActionReqCassette flowBatchByManualActionReqCassette : strFlowBatchByManualActionReqCassette){
            if (ObjectIdentifier.isEmpty(flowBatchByManualActionReqCassette.getCassetteID())){
                throw new ServiceException(retCodeConfig.getInvalidParameter());
            }
            cassetteIDs.add(flowBatchByManualActionReqCassette.getCassetteID());
            List<ObjectIdentifier> lotIDs = flowBatchByManualActionReqCassette.getLotID();
            if (CimArrayUtils.isEmpty(lotIDs)){
                throw new ServiceException(retCodeConfig.getInvalidParameter());
            }
        }

        if (!lockMode.equals(BizConstant.SP_EQP_LOCK_MODE_WRITE)){
            // step2 advanced_object_Lock
            objectLockMethod.advancedObjectLock(objCommon, new Inputs.ObjAdvanceLockIn(equipmentID,
                    BizConstant.SP_CLASSNAME_POSMACHINE,
                    BizConstant.SP_OBJECTLOCK_OBJECTTYPE_MAINOBJECT,
                    objLockModeOut.getRequiredLockForMainObject(), new ArrayList<>()));
        } else {
            // step3 object_Lock
            objectLockMethod.objectLock(objCommon, CimMachine.class, equipmentID);
        }
        //Check SorterJob existence;
        if (log.isDebugEnabled()) {
            log.debug("【Step17】Check SorterJob existence;");

        }
        Inputs.ObjWaferSorterJobCheckForOperation operation = new Inputs.ObjWaferSorterJobCheckForOperation();
        operation.setCassetteIDList(cassetteIDs);
        operation.setOperation(BizConstant.SP_OPERATION_FOR_CAST);
        waferMethod.waferSorterSorterJobCheckForOperation(objCommon,operation);


        //------------------------------------------------------
        //   Collect information on the same cassette
        //
        //   This Process may be unnecessary.
        //   But, make it insurance because it isn't useless.
        //------------------------------------------------------
        // step4 flowBatch_infoSortByCassette
        log.info("step4 flowBatch_infoSortByCassette");
        Outputs.ObjFlowBatchInfoSortByCassetteOut flowBatchInfoSortByCassetteOut = flowBatchMethod.flowBatchInfoSortByCassette(objCommon, param.getStrFlowBatchByManualActionReqCassette());
        List<Infos.FlowBatchByManualActionReqCassette> flowBatchByManualActionReqCassetteList = flowBatchInfoSortByCassetteOut.getFlowBatchByManualActionReqCassetteList();
        //------------------------------------
        //   Do Check of various Conditions
        //------------------------------------
        int nLen = CimArrayUtils.getSize(flowBatchByManualActionReqCassetteList);
        if (nLen > 0){
            for (Infos.FlowBatchByManualActionReqCassette flowBatchByManualActionReqCassette : flowBatchByManualActionReqCassetteList) {
                // step5 equipment_CheckAvailForLot
                log.info("step5 equipment_CheckAvailForLot");
                equipmentMethod.equipmentCheckAvailForLot(objCommon, param.getEquipmentID(), flowBatchByManualActionReqCassette.getLotID());
                //------------------------------------
                //   Check Entity Inhibit for Eqp/lot
                //------------------------------------
                // step6 - equipment_CheckInhibitForLot
                log.info("step6 - equipment_CheckInhibitForLot");
                equipmentMethod.equipmentCheckInhibitForLot(objCommon, param.getEquipmentID(), flowBatchByManualActionReqCassette.getLotID());
            }
        }
        // step7 equipment_reserveFlowBatchID_Get__090
        log.info("step7 equipment_reserveFlowBatchID_Get__090");
        Outputs.ObjEquipmentReserveFlowBatchIDGetOut objEquipmentReserveFlowBatchIDGetOut = equipmentMethod.equipmentReserveFlowBatchIDGet(objCommon, param.getEquipmentID());

        Boolean bFlowBatchFilled = false;
        int rsvFBCount = CimArrayUtils.getSize(objEquipmentReserveFlowBatchIDGetOut.getFlowBatchIDs());
        long maxFBCount = objEquipmentReserveFlowBatchIDGetOut.getFlowBatchMaxCount();
        if(maxFBCount == 0){
            if(rsvFBCount >= 1) {
                bFlowBatchFilled = true;
            }
        }else if(maxFBCount > 0){
            if(rsvFBCount >= maxFBCount){
                bFlowBatchFilled = true;
            }
        }
        Validations.check(CimBooleanUtils.isTrue(bFlowBatchFilled), retCodeConfig.getEquipmentReservedForOtherFlowBatch());
        //check if the lot match the contamination request
        List<ObjectIdentifier> lotList = new ArrayList<>();
        for (Infos.FlowBatchByManualActionReqCassette flowBatchByManualActionReqCassette : strFlowBatchByManualActionReqCassette){
            List<ObjectIdentifier> lotIDs = flowBatchByManualActionReqCassette.getLotID();
            lotList.addAll(lotIDs);
        }
        //----------------------------------------------
        //   Check cassette infromation for Flow Batch
        //----------------------------------------------
        List<ObjectIdentifier> reservedFlowBatchIDs = new ArrayList<>();
        if(!CimArrayUtils.isEmpty(objEquipmentReserveFlowBatchIDGetOut.getFlowBatchIDs())) {
            reservedFlowBatchIDs = objEquipmentReserveFlowBatchIDGetOut.getFlowBatchIDs();
        }
        for (ObjectIdentifier reservedFlowBatchID : reservedFlowBatchIDs) {
            // step8 cassette_CheckConditionForFlowBatch
            log.info("step8 cassette_CheckConditionForFlowBatch");
            cassetteMethod.cassetteCheckConditionForFlowBatch(objCommon, param.getEquipmentID(), reservedFlowBatchID,
                    flowBatchByManualActionReqCassetteList, BizConstant.SP_OPERATION_FLOWBATCHING);
        }
        //----------------------------------------
        //   Check cassette Count for Flow Batch
        //----------------------------------------
        // step9 cassette_CheckCountForFlowBatch
        log.info("step9 cassette_CheckCountForFlowBatch");
        cassetteMethod.cassetteCheckCountForFlowBatch(objCommon, param.getEquipmentID(), flowBatchByManualActionReqCassetteList,
                BizConstant.SP_OPERATION_FLOWBATCHING, param.getClaimMemo());
        String eqpMonitorSwitchStr = StandardProperties.OM_AUTOMON_FLAG.getValue();
        int eqpMonitorSwitch = CimStringUtils.isEmpty(eqpMonitorSwitchStr) ? 0 : Integer.parseInt(eqpMonitorSwitchStr);
        if (eqpMonitorSwitch == 1) {
            int eqpMonJobLen = 5;
            List<Infos.StartSeqNo> strStartSeqNoSeq = new ArrayList<>(eqpMonJobLen);
            int eqpMonJobCnt = 0;
            for(int iCnt1=0;iCnt1<flowBatchByManualActionReqCassetteList.size();iCnt1++){
                for (int iCnt2=0;iCnt2<flowBatchByManualActionReqCassetteList.get(iCnt1).getLotID().size();iCnt2++){
                    // step10 lot_lotType_Get
                    log.info("step10 lot_lotType_Get");
                    String lotLotType = lotMethod.lotTypeGet(objCommon, flowBatchByManualActionReqCassetteList.get(iCnt1).getLotID().get(iCnt2));
                    if (CimStringUtils.equals(BizConstant.SP_LOT_TYPE_EQUIPMENTMONITORLOT, lotLotType)
                            || CimStringUtils.equals(BizConstant.SP_LOT_TYPE_DUMMYLOT, lotLotType)) {
                        //Acquire EqpMonitor Job Information on lot
                        // step11 - lot_eqpMonitorJob_Get
                        log.info("step11 - lot_eqpMonitorJob_Get");
                        Infos.EqpMonitorJobLotInfo eqpMonitorJob = lotMethod.lotEqpMonitorJobGet(objCommon,flowBatchByManualActionReqCassetteList.get(iCnt1).getLotID().get(iCnt2));

                        if(ObjectIdentifier.isEmptyWithValue(eqpMonitorJob.getEqpMonitorJobID())){
                            continue;
                        }
                        if(!ObjectIdentifier.equalsWithValue(eqpMonitorJob.getEquipmentID(), param.getEquipmentID())){
                            continue;
                        }
                        Boolean bNewEqpMonJob = true;
                        for(int iCnt3=0;iCnt3<eqpMonJobCnt;iCnt3++){
                            log.info("loop to eqpMonJobCnt");
                            if(ObjectIdentifier.equalsWithValue(strStartSeqNoSeq.get(iCnt3).getKey(), eqpMonitorJob.getEqpMonitorJobID())){
                                Validations.check(strStartSeqNoSeq.get(iCnt3).getStartSeqNo().intValue() != eqpMonitorJob.getStartSeqNo().intValue(),
                                        new OmCode(retCodeConfig.getInvalidParameterWithMsg(), "The target lots have different Start Seq No in Auto Monitor job"));
                                bNewEqpMonJob = false;
                                break;
                            }
                        }
                        if(bNewEqpMonJob){
                            if ( eqpMonJobCnt > eqpMonJobLen ){
                                eqpMonJobLen = eqpMonJobLen + 5;
                            }
                            Infos.StartSeqNo startSeqNoInfo = new Infos.StartSeqNo();
                            startSeqNoInfo.setKey(eqpMonitorJob.getEqpMonitorJobID().getValue());
                            startSeqNoInfo.setStartSeqNo(eqpMonitorJob.getStartSeqNo().intValue());
                            strStartSeqNoSeq.add(startSeqNoInfo);
                            eqpMonJobCnt++;
                        }

                    }

                }
            }
        }
        //-------------------------
        //   Make new flow batch
        //-------------------------
        long nLotCount = 0;
        for (int i=0;i<nLen;i++){
            nLotCount = nLotCount + flowBatchByManualActionReqCassetteList.get(i).getLotID().size();
        }
        List<Infos.BatchingReqLot> strBatchingReqLot = new ArrayList<>();
        for(int i=0;i<nLen;i++){
            int nnLen = flowBatchByManualActionReqCassetteList.get(i).getLotID().size();
            for(int j=0;j<nnLen;j++){
                Infos.BatchingReqLot batchingReqLot = new Infos.BatchingReqLot();
                batchingReqLot.setCassetteID(flowBatchByManualActionReqCassetteList.get(i).getCassetteID());
                batchingReqLot.setLotID(flowBatchByManualActionReqCassetteList.get(i).getLotID().get(j));
                strBatchingReqLot.add(batchingReqLot);
            }
        }
        // step12 - flowBatch_Make
        log.info("step12 - flowBatch_Make");
        Outputs.ObjFlowBatchMakeOut objFlowBatchMakeOut = flowBatchMethod.flowBatchMake(objCommon, strBatchingReqLot);

        //---------------------
        //   txEqpReserveForFlowBatchReq
        //---------------------
        // step13 - txEqpReserveForFlowBatchReq
        log.info("step13 - txEqpReserveForFlowBatchReq");
        List<Infos.FlowBatchedLot> flowBatchedLotList = sxEqpReserveForFlowBatchReq(objCommon,param.getEquipmentID(),objFlowBatchMakeOut.getBatchID(),param.getClaimMemo());
        //------------------------------------------------------------------------
        //   Make Event Record
        //------------------------------------------------------------------------
        // step14 - lotFlowBatchEvent_Make
        log.info("step14 - lotFlowBatchEvent_Make");
        for (Infos.FlowBatchByManualActionReqCassette flowBatchByManualActionReqCassette : flowBatchByManualActionReqCassetteList){
            List<ObjectIdentifier> lotIDs = flowBatchByManualActionReqCassette.getLotID();
            for (ObjectIdentifier lotID : lotIDs){
                eventMethod.lotFlowBatchEventMake(objCommon, TransactionIDEnum.FLOW_BATCHING_REQ.getValue(), lotID, objFlowBatchMakeOut.getBatchID(), param.getClaimMemo());
            }
        }

        out.setEquipmentID(param.getEquipmentID());
        out.setFlowBatchID(objFlowBatchMakeOut.getBatchID());
        out.setStrFlowBatchedLot(flowBatchedLotList);
        return out;
    }

    @Override
    public Results.FlowBatchLotRemoveReqResult sxFlowBatchLotRemoveReq(Infos.ObjCommon objCommon, Params.FlowBatchLotRemoveReq param) {
        List<Infos.RemoveCassette> strRemoveCassette = param.getStrRemoveCassette();
        Validations.check(CimArrayUtils.isEmpty(strRemoveCassette), retCodeConfig.getInvalidParameter());
        Validations.check(CimArrayUtils.isEmpty(strRemoveCassette.get(0).getLotID()),retCodeConfig.getInvalidParameter() );
        //Get flowbatch ReserveEQPID
        //step1 - flowBatch_reserveEquipmentID_Get
        log.info("step1 - flowBatch_reserveEquipmentID_Get");
        ObjectIdentifier reserveEquipmentID = null;
        try {
            flowBatchMethod.flowBatchReserveEquipmentIDGet(objCommon, param.getFlowBatchID());
        } catch (ServiceException e) {
            if(!Validations.isEquals(retCodeConfig.getFlowBatchReservedEqpIdFilled(), e.getCode())
                    && !Validations.isEquals(retCodeConfig.getFlowBatchReservedEqpIdBlank(), e.getCode())){
                throw e;
            }
            reserveEquipmentID = e.getData(ObjectIdentifier.class);
        }
        //Check Condition For Flow Batch(eqp)
        if(!ObjectIdentifier.isEmptyWithValue(reserveEquipmentID)){
            // step2 - object_lockMode_Get
            Inputs.ObjLockModeIn objLockModeIn = new Inputs.ObjLockModeIn();
            objLockModeIn.setObjectID(reserveEquipmentID);
            objLockModeIn.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
            objLockModeIn.setFunctionCategory(TransactionIDEnum.LOT_REMOVE_FROM_FLOW_BATCH_REQ.getValue());
            objLockModeIn.setUserDataUpdateFlag(false);
            Outputs.ObjLockModeOut objLockModeOut = objectMethod.objectLockModeGet(objCommon, objLockModeIn);
            Long lockMode = objLockModeOut.getLockMode();
            Inputs.ObjAdvanceLockIn strAdvancedobjecLockin = new Inputs.ObjAdvanceLockIn();
            if (!lockMode.equals(BizConstant.SP_EQP_LOCK_MODE_WRITE)){
                // step3 - advanced_object_Lock
                List<String> dummySeq;
                dummySeq = new ArrayList<>(0);
                strAdvancedobjecLockin.setObjectID(reserveEquipmentID);
                strAdvancedobjecLockin.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
                strAdvancedobjecLockin.setObjectType(BizConstant.SP_OBJECTLOCK_OBJECTTYPE_MAINOBJECT);
                strAdvancedobjecLockin.setLockType(objLockModeOut.getRequiredLockForMainObject());
                strAdvancedobjecLockin.setKeyList(dummySeq);

                objectLockMethod.advancedObjectLock(objCommon, strAdvancedobjecLockin);
            }
            //step4 - equipment_CheckConditionForFlowBatch
            log.info("step4 - equipment_CheckConditionForFlowBatch");
            equipmentMethod.equipmentCheckConditionForFlowBatch(objCommon, reserveEquipmentID, param.getFlowBatchID(), BizConstant.SP_OPERATION_FLOWBATCH_LOTREMOVE);
        }
        /*--------------------------------------------------------------*/
        /*   Check minimumSize after removing lots from FlowBatch.      */
        /*   If remaining lot count is less than minimumSize            */
        /*   then LotRemove is not allowed.                             */
        /*   If all lots are removed, then LotRemove is allowed.        */
        /*--------------------------------------------------------------*/
        //step5 - flowBatch_cassette_Get
        log.info("step5 - flowBatch_cassette_Get");
        List<Infos.ContainedCassettesInFlowBatch> containedCassettesInFlowBatches = flowBatchMethod.flowBatchCassetteGet(objCommon, param.getFlowBatchID());
        //step6 - process_GetFlowBatchDefinition
        log.info("step6 - process_GetFlowBatchDefinition");
        Outputs.ObjProcessGetFlowBatchDefinitionOut objProcessGetFlowBatchDefinitionOut = processMethod.processGetFlowBatchDefinition(objCommon, param.getStrRemoveCassette().get(0).getLotID().get(0));
        //Make remain cassettes and lots infomation.
        List<Infos.FlowBatchByManualActionReqCassette> remainFlowBatchInfo = new ArrayList<>();
        int lenCassette = CimArrayUtils.getSize(containedCassettesInFlowBatches);
        for (int i = 0; i < lenCassette; i++){
            List<Infos.ContainedLotInCassetteInFlowBatch> strContainedLotInCassetteInFlowBatch = containedCassettesInFlowBatches.get(i).getStrContainedLotInCassetteInFlowBatch();
            int lenLot = CimArrayUtils.getSize(strContainedLotInCassetteInFlowBatch);
            for (int j = 0; j < lenLot; j++){
                boolean bRemainLot = true;
                int lenRemoveCas = CimArrayUtils.getSize(strRemoveCassette);
                for (int k  = 0; k < lenRemoveCas; k++){
                    boolean bBreak = false;
                    List<ObjectIdentifier> lotIDList = strRemoveCassette.get(k).getLotID();
                    int lenRemoveLot = CimArrayUtils.getSize(lotIDList);
                    for (int l = 0; l < lenRemoveLot; l++){
                        if (ObjectIdentifier.equalsWithValue(containedCassettesInFlowBatches.get(i).getCassetteID(), strRemoveCassette.get(k).getCassetteID())
                                && ObjectIdentifier.equalsWithValue(strContainedLotInCassetteInFlowBatch.get(j).getLotID(), lotIDList.get(l))){
                            // This CassetteID, LotID is not added to remain information.
                            bRemainLot = false;
                            bBreak = true;
                            break;
                        }
                    }
                    if (bBreak){
                        break;
                    }
                }
                if (bRemainLot){
                    Infos.FlowBatchByManualActionReqCassette flowBatchByManualActionReqCassette = new Infos.FlowBatchByManualActionReqCassette();
                    // It is made not to add the same CassetteID twice.
                    int nCasIdx = -1;
                    int lenRemainCas = CimArrayUtils.getSize(remainFlowBatchInfo);
                    for (int k = 0; k < lenRemainCas; k++){
                        if (ObjectIdentifier.equalsWithValue(containedCassettesInFlowBatches.get(i).getCassetteID(), remainFlowBatchInfo.get(k).getCassetteID())){
                            nCasIdx = k;
                            break;
                        }
                    }
                    if (nCasIdx < 0){
                        // Add CassetteID to remain information.
                        nCasIdx = lenRemainCas;
                        flowBatchByManualActionReqCassette.setCassetteID(containedCassettesInFlowBatches.get(i).getCassetteID());
                    }
                    // It is made not to add the same LotID twice.
                    int nLotIdx = -1;
                    List<ObjectIdentifier> lotIDList2 = flowBatchByManualActionReqCassette.getLotID();
                    int lenRemainLot = CimArrayUtils.getSize(lotIDList2);
                    for (int k = 0; k < lenRemainLot; k++){
                        if (ObjectIdentifier.equalsWithValue(strContainedLotInCassetteInFlowBatch.get(j).getLotID(), lotIDList2.get(k))){
                            nLotIdx = k;
                            break;
                        }
                    }
                    if (0 > nLotIdx){
                        // Add LotID to remain information.
                        List<ObjectIdentifier> tmpLotIDList = new ArrayList<>();
                        flowBatchByManualActionReqCassette.setLotID(tmpLotIDList);
                        tmpLotIDList.add(strContainedLotInCassetteInFlowBatch.get(j).getLotID());
                    }
                    if (flowBatchByManualActionReqCassette != null){
                        remainFlowBatchInfo.add(flowBatchByManualActionReqCassette);
                    }
                }
            }
        }
        //Check count about EQP's MaxSize, MinSize, MinWaferSize and FlowBatchArea's MaxSize, MinSize, MinWaferSize.
        if (CimArrayUtils.isNotEmpty(remainFlowBatchInfo)) {
            //step7 - cassette_CheckCountForFlowBatch
            log.info("step7 - cassette_CheckCountForFlowBatch");
            cassetteMethod.cassetteCheckCountForFlowBatch(objCommon, reserveEquipmentID, remainFlowBatchInfo, BizConstant.SP_OPERATION_FLOWBATCH_LOTREMOVE, param.getClaimMemo());
        }
        long nRemoveCassetteLen = param.getStrRemoveCassette().size();
        long removeLotCount = 0;
        for(int i=0; i < nRemoveCassetteLen; i++){
            removeLotCount = removeLotCount + param.getStrRemoveCassette().get(i).getLotID().size();
        }
        /*----------------------------------------------*/
        /*   Check Condition For Flow Batch(Cassette)   */
        /*----------------------------------------------*/
        List<Infos.FlowBatchByManualActionReqCassette> strFlowBatchByManualActionReqCassette = new ArrayList<>();
        for (int i = 0; i < nRemoveCassetteLen; i++) {
            Infos.FlowBatchByManualActionReqCassette flowBatchByManualActionReqCassette = new Infos.FlowBatchByManualActionReqCassette();
            flowBatchByManualActionReqCassette.setCassetteID(param.getStrRemoveCassette().get(i).getCassetteID());
            List<ObjectIdentifier> lotID = param.getStrRemoveCassette().get(i).getLotID();
            if (CimArrayUtils.isNotEmpty(lotID)) {
                List<ObjectIdentifier> lotIdList = new ArrayList<>(lotID);
                flowBatchByManualActionReqCassette.setLotID(lotIdList);
            }
            strFlowBatchByManualActionReqCassette.add(flowBatchByManualActionReqCassette);
        }

        //step8 - cassette_CheckConditionForFlowBatch
        log.info("step8 - cassette_CheckConditionForFlowBatch");
        try {
            cassetteMethod.cassetteCheckConditionForFlowBatch(objCommon, reserveEquipmentID, param.getFlowBatchID(),
                    strFlowBatchByManualActionReqCassette, BizConstant.SP_OPERATION_FLOWBATCH_LOTREMOVE);
        } catch (ServiceException e) {
            if (!Validations.isEquals(retCodeConfig.getLotFlowBatchIdFilled(), e.getCode())){
                throw e;
            }
        }
        if(CimArrayUtils.isEmpty(remainFlowBatchInfo) && !ObjectIdentifier.isEmptyWithValue(reserveEquipmentID)){
            /*--------------------------------------------------------------*/
            /*   If remove all lots then EqpReserveCancel is carried out.   */
            /*--------------------------------------------------------------*/
            //step9 - txEqpReserveCancelForflowBatchReq
            log.info("step9 - txEqpReserveCancelForflowBatchReq");
            sxEqpReserveCancelForflowBatchReq(objCommon, reserveEquipmentID, param.getFlowBatchID(), param.getClaimMemo());
        }
        /*--------------------------------*/
        /*   Remove lot from flow batch   */
        /*--------------------------------*/
        List<Infos.RemoveLot> strRemoveLot = new ArrayList<>();
        for (Infos.RemoveCassette removeCassette : strRemoveCassette) {
            List<ObjectIdentifier> lotID = removeCassette.getLotID();
            for (ObjectIdentifier lotId : lotID) {
                Infos.RemoveLot removeLot = new Infos.RemoveLot();
                removeLot.setCassetteID(removeCassette.getCassetteID());
                removeLot.setLotID(lotId);
                strRemoveLot.add(removeLot);
            }
        }
        //step10 - flowBatch_lot_Remove
        log.info("step10 - flowBatch_lot_Remove");
        int retCode = 0;
        try {
            flowBatchMethod.flowBatchLotRemove(objCommon, param.getFlowBatchID(), strRemoveLot);
        } catch (ServiceException e) {
            retCode = e.getCode();
            if (!Validations.isEquals(retCodeConfig.getFlowBatchRemoved(), e.getCode())){
                throw e;
            }
        }
        /*-----------------------*/
        /*   Set out structure   */
        /*-----------------------*/
        //step11 - flowBatch_FillInTxDSC005
        log.info("step11 - flowBatch_FillInTxDSC005");
        Results.FlowBatchLotRemoveReqResult flowBatchLotRemoveReqResult = new Results.FlowBatchLotRemoveReqResult();
        if (!Validations.isEquals(retCodeConfig.getFlowBatchRemoved(), retCode)){
            flowBatchLotRemoveReqResult = flowBatchMethod.flowBatchFillInTxDSC005(objCommon, param.getFlowBatchID());
        }
        /*---------------------*/
        /*   For Removed Lot   */
        /*---------------------*/
        for (int i = 0; i < removeLotCount; i++){
            // step12 - lotFlowBatchEvent_Make
            log.info("step12 - lotFlowBatchEvent_Make");
            eventMethod.lotFlowBatchEventMake(objCommon, TransactionIDEnum.LOT_REMOVE_FROM_FLOW_BATCH_REQ.getValue(), strRemoveLot.get(i).getLotID(), param.getFlowBatchID(), param.getClaimMemo());
        }
        return flowBatchLotRemoveReqResult;
    }

    @Override
    public Results.FlowBatchByAutoActionReqResult sxFlowBatchByAutoActionReq(Infos.ObjCommon objCommon, Params.FlowBatchByAutoActionReqParams params) {
        ObjectIdentifier equipmentID = params.getEquipmentID();
        Results.FlowBatchByAutoActionReqResult autoFlowBatchByManualActionReqResult = new Results.FlowBatchByAutoActionReqResult();
        // step1 - object_lockMode_Get
        Inputs.ObjLockModeIn objLockModeIn = new Inputs.ObjLockModeIn();
        objLockModeIn.setObjectID(equipmentID);
        objLockModeIn.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
        objLockModeIn.setFunctionCategory(TransactionIDEnum.AUTO_FLOW_BATCHING_REQ.getValue());
        objLockModeIn.setUserDataUpdateFlag(false);
        Outputs.ObjLockModeOut objLockModeOut = objectMethod.objectLockModeGet(objCommon, objLockModeIn);
        Long lockMode = objLockModeOut.getLockMode();
        Inputs.ObjAdvanceLockIn strAdvancedobjecLockin = new Inputs.ObjAdvanceLockIn();
        if (!lockMode.equals(BizConstant.SP_EQP_LOCK_MODE_WRITE)){
            // step2 - advanced_object_Lock
            List<String> dummySeq;
            dummySeq = new ArrayList<>(0);
            strAdvancedobjecLockin.setObjectID(equipmentID);
            strAdvancedobjecLockin.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
            strAdvancedobjecLockin.setObjectType(BizConstant.SP_OBJECTLOCK_OBJECTTYPE_MAINOBJECT);
            strAdvancedobjecLockin.setLockType(objLockModeOut.getRequiredLockForMainObject());
            strAdvancedobjecLockin.setKeyList(dummySeq);

            objectLockMethod.advancedObjectLock(objCommon, strAdvancedobjecLockin);
        } else {
            // step3 - object_Lock
            objectLockMethod.objectLock(objCommon, CimMachine.class, equipmentID);
        }

        //step4 - equipment_reserveFlowBatchID_Get__090
        Outputs.ObjEquipmentReserveFlowBatchIDGetOut reserveFlowBatchIDGet = equipmentMethod.equipmentReserveFlowBatchIDGet(objCommon, params.getEquipmentID());

        long rsvFBCount = CimArrayUtils.getSize(reserveFlowBatchIDGet.getFlowBatchIDs());
        long maxFBCount = reserveFlowBatchIDGet.getFlowBatchMaxCount();
        Boolean bFlowBatchFilled = false;
        if (maxFBCount == 0) {
            if (rsvFBCount >= 1) {
                bFlowBatchFilled = true;
            }
        } else if (maxFBCount > 0) {
            if (rsvFBCount >= maxFBCount) {
                bFlowBatchFilled = true;
            }
        }
        Validations.check(CimBooleanUtils.isTrue(bFlowBatchFilled), retCodeConfig.getEqpReservedForSomeFlowBatch());
        //step5 - txFloatingBatchListInq
        Results.FloatingBatchListInqResult floatingBatchListInqResultRetCode = null;
        int retCode = 0;
        try{
            floatingBatchListInqResultRetCode = flowBatchInqService.sxFloatingBatchListInq(objCommon, params.getEquipmentID());
        }catch (ServiceException e) {
            retCode = e.getCode();
            if (!Validations.isEquals(retCodeConfig.getNotFoundFlowBatch(), e.getCode())) {
                throw e;
            }
        }
        /*--------------------------------------*/
        /*   When Floating Batch is found ...   */
        /*--------------------------------------*/
        Boolean bAssignFloatingBatch = false;
        List<Infos.FlowBatchByManualActionReqCassette> strFlowBatchByManualActionReqCassette = new ArrayList<>();
        Results.FlowBatchByManualActionReqResult strFlowBatchByManualActionReqResult = null;
        if (retCode == 0){
            //step6 - flowBatch_SelectForEquipmentDR
            ObjectIdentifier flowBatchSelectForEquipmentDR = null;
            try {
                flowBatchSelectForEquipmentDR = flowBatchMethod.flowBatchSelectForEquipmentDR(objCommon, params.getEquipmentID(), floatingBatchListInqResultRetCode);
            } catch (ServiceException e) {
                if (!Validations.isEquals(retCodeConfig.getNotFoundFlowBatch(), e.getCode())) {
                    throw e;
                }
            }
            //step7 - txEqpReserveForFlowBatchReq
            sxEqpReserveForFlowBatchReq(objCommon, params.getEquipmentID(), flowBatchSelectForEquipmentDR, params.getClaimMemo());
            bAssignFloatingBatch = true;
        }
        if(CimBooleanUtils.isFalse(bAssignFloatingBatch)){
            //step8 - txFlowBatchLotSelectionInq__090
            Results.FlowBatchLotSelectionInqResult strFlowBatchLotSelectionInqResult = flowBatchInqService.sxFlowBatchLotSelectionInq(objCommon, params.getEquipmentID());
            Inputs.ObjTempFlowBatchSelectForEquipmentDRIn tempFlowBatch = new Inputs.ObjTempFlowBatchSelectForEquipmentDRIn();
            tempFlowBatch.setEquipmentID(params.getEquipmentID());
            tempFlowBatch.setStrFlowBatchLotSelectionInqResult(strFlowBatchLotSelectionInqResult);

            //step9 - tempFlowBatch_SelectForEquipmentDR__090
            List<Infos.FlowBatchByManualActionReqCassette> flowBatchByManualActionReqCassettes = flowBatchMethod.tempFlowBatchSelectForEquipmentDR(objCommon, tempFlowBatch);
            if(CimArrayUtils.isNotEmpty(flowBatchByManualActionReqCassettes)){
                for (Infos.FlowBatchByManualActionReqCassette flowBatchByManualActionReqCassette : flowBatchByManualActionReqCassettes) {
                    Infos.FlowBatchByManualActionReqCassette flowBatching = new Infos.FlowBatchByManualActionReqCassette();
                    strFlowBatchByManualActionReqCassette.add(flowBatching);

                    List<ObjectIdentifier> lotID = new ArrayList<>();
                    flowBatching.setCassetteID(flowBatchByManualActionReqCassette.getCassetteID());
                    List<ObjectIdentifier> lotIDList = flowBatchByManualActionReqCassette.getLotID();
                    if (CimArrayUtils.isNotEmpty(lotIDList)) {
                        lotID.addAll(lotIDList);
                    }
                    flowBatching.setLotID(lotID);
                }
            }
            Params.FlowBatchByManualActionReqParam reFlowBatchByManualActionReqParam = new Params.FlowBatchByManualActionReqParam();
            reFlowBatchByManualActionReqParam.setStrFlowBatchByManualActionReqCassette(flowBatchByManualActionReqCassettes);
            reFlowBatchByManualActionReqParam.setUser(params.getUser());
            reFlowBatchByManualActionReqParam.setEquipmentID(params.getEquipmentID());
            reFlowBatchByManualActionReqParam.setClaimMemo(params.getClaimMemo());

            //step10 - txFlowBatchByManualActionReq
            strFlowBatchByManualActionReqResult = sxFlowBatchByManualActionReq(objCommon, reFlowBatchByManualActionReqParam);
        }
        autoFlowBatchByManualActionReqResult.setEquipmentID(strFlowBatchByManualActionReqResult == null ? null : strFlowBatchByManualActionReqResult.getEquipmentID());
        autoFlowBatchByManualActionReqResult.setFlowBatchID(strFlowBatchByManualActionReqResult == null ? null : strFlowBatchByManualActionReqResult.getFlowBatchID());

        if (CimArrayUtils.isNotEmpty(strFlowBatchByManualActionReqCassette)) {
            List<Infos.FlowBatchedCassette> strBatchedCassette = new ArrayList<>();
            autoFlowBatchByManualActionReqResult.setStrBatchedCassette(strBatchedCassette);
            long i = 0;
            for (Infos.FlowBatchByManualActionReqCassette flowBatchByManualActionReqCassette : strFlowBatchByManualActionReqCassette) {
                i++;
                Infos.FlowBatchedCassette flowBatchedCassette = new Infos.FlowBatchedCassette();
                strBatchedCassette.add(flowBatchedCassette);
                flowBatchedCassette.setCassetteID(flowBatchByManualActionReqCassette.getCassetteID());
                List<ObjectIdentifier> lotID = flowBatchByManualActionReqCassette.getLotID();
                if (CimArrayUtils.isNotEmpty(lotID)) {
                    List<ObjectIdentifier> lotIDList = new ArrayList<>(lotID);
                    flowBatchedCassette.setLotID(lotIDList);
                }
                flowBatchedCassette.setProcessSequenceNumber(i);
            }
        }

        return autoFlowBatchByManualActionReqResult;
    }

    @Override
    public void sxEqpMaxFlowbCountModifyReq(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, Integer flowBatchMaxCount, String claimMemo) {
        log.info("input sxEqpMaxFlowbCountModifyReq...");
        /*--------------------*/
        /*   Initialization   */
        /*--------------------*/
        // Step1 - object_lockMode_Get(...)
        Inputs.ObjLockModeIn objLockModeIn = new Inputs.ObjLockModeIn();
        objLockModeIn.setObjectID(equipmentID);
        objLockModeIn.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
        objLockModeIn.setFunctionCategory(TransactionIDEnum.EQP_FLOW_BATCH_MAX_COUNT_CHANGE_REQ.getValue());
        objLockModeIn.setUserDataUpdateFlag(false);
        Outputs.ObjLockModeOut objLockModeOut = objectMethod.objectLockModeGet(objCommon, objLockModeIn);
        Long lockMode = objLockModeOut.getLockMode();
        Inputs.ObjAdvanceLockIn strAdvancedobjecLockin = new Inputs.ObjAdvanceLockIn();
        if (!lockMode.equals(BizConstant.SP_EQP_LOCK_MODE_WRITE)){
            // Step2 - advanced_object_Lock(...)
            List<String> dummySeq;
            dummySeq = new ArrayList<>(0);
            strAdvancedobjecLockin.setObjectID(equipmentID);
            strAdvancedobjecLockin.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
            strAdvancedobjecLockin.setObjectType(BizConstant.SP_OBJECTLOCK_OBJECTTYPE_MAINOBJECT);
            strAdvancedobjecLockin.setLockType(objLockModeOut.getRequiredLockForMainObject());
            strAdvancedobjecLockin.setKeyList(dummySeq);

            objectLockMethod.advancedObjectLock(objCommon, strAdvancedobjecLockin);
        } else {
            // Step3 - object_Lock(...)
            objectLockMethod.objectLock(objCommon, CimMachine.class, equipmentID);
        }
        //------------------------------------------//
        //  Update the Maximum Count of flowbatch   //
        //------------------------------------------//
        // Step4 -  call equipment_flowBatchMaxCount_Change(...)
        log.info("Step4 -  call equipment_flowBatchMaxCount_Change(...)");
        Inputs.ObjEquipmentFlowBatchMaxCountChangeIn  objChangeIn = new Inputs.ObjEquipmentFlowBatchMaxCountChangeIn();
        objChangeIn.setEquipmentID(equipmentID);
        objChangeIn.setFlowBatchMaxCount(flowBatchMaxCount);
        equipmentMethod.equipmentFlowBatchMaxCountChange(objCommon,objChangeIn);
        //------------------//
        //    Make Event    //
        //------------------//
        // Step5 - equipment_flowBatchMaxCount_ChangeEvent_Make(...)
        log.info("Step5 - equipment_flowBatchMaxCount_ChangeEvent_Make(...)");
        Inputs.ObjEquipmentFlowBatchMaxCountChangeEventMakeIn objEquipmentFlowBatchMaxCountChangeEventMakeIn = new Inputs.ObjEquipmentFlowBatchMaxCountChangeEventMakeIn();
        objEquipmentFlowBatchMaxCountChangeEventMakeIn.setTransactionID(objCommon.getTransactionID());
        objEquipmentFlowBatchMaxCountChangeEventMakeIn.setEquipmentID(equipmentID);
        objEquipmentFlowBatchMaxCountChangeEventMakeIn.setFlowBatchMaxCount(flowBatchMaxCount);
        objEquipmentFlowBatchMaxCountChangeEventMakeIn.setClaimMemo(claimMemo);
        eventMethod.equipmentFlowBatchMaxCountChangeEventMake(objCommon,objEquipmentFlowBatchMaxCountChangeEventMakeIn);
    }

    @Override
    public void sxEqpReserveCancelForflowBatchReq(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, ObjectIdentifier flowBatchID, String claimMemo) {
        log.info("sxEqpReserveCancelForflowBatchReq():come in EqpReserveCancelForflowBatchReqService class ---> sxEqpReserveCancelForflowBatchReq method.");
        // Step1 - object_lockMode_Get(...)
        Inputs.ObjLockModeIn objLockModeIn = new Inputs.ObjLockModeIn();
        objLockModeIn.setObjectID(equipmentID);
        objLockModeIn.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
        objLockModeIn.setFunctionCategory(TransactionIDEnum.EQP_RESERVE_CANCEL_REQ.getValue());
        objLockModeIn.setUserDataUpdateFlag(false);
        Outputs.ObjLockModeOut objLockModeOut = objectMethod.objectLockModeGet(objCommon, objLockModeIn);
        Long lockMode = objLockModeOut.getLockMode();
        Inputs.ObjAdvanceLockIn strAdvancedobjecLockin = new Inputs.ObjAdvanceLockIn();
        if (!lockMode.equals(BizConstant.SP_EQP_LOCK_MODE_WRITE)){
            // Step2 - advanced_object_Lock(...)
            List<String> dummySeq;
            dummySeq = new ArrayList<>(0);
            strAdvancedobjecLockin.setObjectID(equipmentID);
            strAdvancedobjecLockin.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
            strAdvancedobjecLockin.setObjectType(BizConstant.SP_OBJECTLOCK_OBJECTTYPE_MAINOBJECT);
            strAdvancedobjecLockin.setLockType(objLockModeOut.getRequiredLockForMainObject());
            strAdvancedobjecLockin.setKeyList(dummySeq);

            objectLockMethod.advancedObjectLock(objCommon, strAdvancedobjecLockin);
        } else {
            // Step3 - object_Lock(...)
            objectLockMethod.objectLock(objCommon, CimMachine.class, equipmentID);
        }
        /*---------------------------------------------*/
        /*   Check Condition For Flow Batch(eqp) */
        /*---------------------------------------------*/
        // Step4 - equipment_CheckConditionForFlowBatch
        log.info("Step4 - equipment_CheckConditionForFlowBatch");
        equipmentMethod.equipmentCheckConditionForFlowBatch(objCommon, equipmentID, flowBatchID, BizConstant.SP_OPERATION_FLOWBATCH_EQPRESERVECANCEL);
        /*----------------------------*/
        /*   Get contained cassettes   */
        /*----------------------------*/
        // Step5 - flowBatch_cassette_Get
        log.info("Step5 - flowBatch_cassette_Get");
        List<Infos.ContainedCassettesInFlowBatch> containedCassettesInFlowBatch = flowBatchMethod.flowBatchCassetteGet(objCommon, flowBatchID);
        /*--------------------------------------------*/
        /*   Check Condition For Flow Batch(cassette) */
        /*--------------------------------------------*/
        List<Infos.FlowBatchByManualActionReqCassette> strFlowBatchByManualActionReqCassette = new ArrayList<>();
        if (!CimArrayUtils.isEmpty(containedCassettesInFlowBatch)) {
            log.info("sxEqpReserveCancelForflowBatchReq():containedCassettesInFlowBatch.strContainedCassettesInFlowBatch.size() = " + containedCassettesInFlowBatch.size());
            for (Infos.ContainedCassettesInFlowBatch flowBatch : containedCassettesInFlowBatch) {
                Infos.FlowBatchByManualActionReqCassette flowBatchByManualActionReqCassette = new Infos.FlowBatchByManualActionReqCassette();
                // get the CassetteID
                flowBatchByManualActionReqCassette.setCassetteID(flowBatch.getCassetteID());
                // get LotIDs.
                List<ObjectIdentifier> objectIdentifiers = new ArrayList<>();
                List<Infos.ContainedLotInCassetteInFlowBatch> inFlowBatchList = flowBatch.getStrContainedLotInCassetteInFlowBatch();
                if (!CimArrayUtils.isEmpty(inFlowBatchList)) {
                    log.info("sxEqpReserveCancelForflowBatchReq():inFlowBatchList length is : " + inFlowBatchList.size());
                    for (Infos.ContainedLotInCassetteInFlowBatch inFlowBatch : inFlowBatchList) {
                        objectIdentifiers.add(inFlowBatch.getLotID());
                        flowBatchByManualActionReqCassette.setLotID(objectIdentifiers);
                    }
                }
                strFlowBatchByManualActionReqCassette.add(flowBatchByManualActionReqCassette);
            }
        }

        // Step6 - cassette_CheckConditionForFlowBatch
        log.info("Step6 - cassette_CheckConditionForFlowBatch");
        cassetteMethod.cassetteCheckConditionForFlowBatch(objCommon, equipmentID, flowBatchID, strFlowBatchByManualActionReqCassette, BizConstant.SP_OPERATION_FLOWBATCH_EQPRESERVECANCEL);

        // Step7 - flowBatch_reserveEquipmentID_Clear
        log.info("Step7 - flowBatch_reserveEquipmentID_Clear");
        flowBatchMethod.flowBatchReserveEquipmentIDClear(objCommon,equipmentID,flowBatchID);

        // Step8 - lotFlowBatchEvent_Make
        log.info("Step8 - lotFlowBatchEvent_Make");
        for (Infos.ContainedCassettesInFlowBatch containedCassettesInFlowBatch2 : containedCassettesInFlowBatch){
            for (Infos.ContainedLotInCassetteInFlowBatch containedLotInCassetteInFlowBatch : containedCassettesInFlowBatch2.getStrContainedLotInCassetteInFlowBatch()){
                ObjectIdentifier lotID = containedLotInCassetteInFlowBatch.getLotID();
                eventMethod.lotFlowBatchEventMake(objCommon, TransactionIDEnum.EQP_RESERVE_CANCEL_REQ.getValue(), lotID, flowBatchID, claimMemo);
            }
        }
        log.info("sxEqpReserveCancelForflowBatchReq():exit EqpReserveCancelForflowBatchReqService class ---> sxEqpReserveCancelForflowBatchReq method.");
    }

    @Override
    public List<Infos.FlowBatchedLot> sxEqpReserveForFlowBatchReq(Infos.ObjCommon objCommon, ObjectIdentifier equipmentID, ObjectIdentifier flowBatchID, String claimMemo) {
        log.info("sxEqpReserveForFlowBatchReq():come in the sxEqpReserveForFlowBatchReq method...");
        // Step1 - object_lockMode_Get(...)
        Inputs.ObjLockModeIn objLockModeIn = new Inputs.ObjLockModeIn();
        objLockModeIn.setObjectID(equipmentID);
        objLockModeIn.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
        objLockModeIn.setFunctionCategory(TransactionIDEnum.EQP_RESERVE_REQ.getValue());
        objLockModeIn.setUserDataUpdateFlag(false);
        Outputs.ObjLockModeOut objLockModeOut = objectMethod.objectLockModeGet(objCommon, objLockModeIn);
        Long lockMode = objLockModeOut.getLockMode();
        Inputs.ObjAdvanceLockIn strAdvancedobjecLockin = new Inputs.ObjAdvanceLockIn();
        if (!lockMode.equals(BizConstant.SP_EQP_LOCK_MODE_WRITE)){
            // Step2 - advanced_object_Lock(...)
            List<String> dummySeq;
            dummySeq = new ArrayList<>(0);
            strAdvancedobjecLockin.setObjectID(equipmentID);
            strAdvancedobjecLockin.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
            strAdvancedobjecLockin.setObjectType(BizConstant.SP_OBJECTLOCK_OBJECTTYPE_MAINOBJECT);
            strAdvancedobjecLockin.setLockType(objLockModeOut.getRequiredLockForMainObject());
            strAdvancedobjecLockin.setKeyList(dummySeq);

            objectLockMethod.advancedObjectLock(objCommon, strAdvancedobjecLockin);
        } else {
            // Step3 - object_Lock(...)
            objectLockMethod.objectLock(objCommon, CimMachine.class, equipmentID);
        }
        /*---------------------------------------------*/
        /*   Check Condition For Flow Batch(eqp) */
        /*---------------------------------------------*/
        // Step4 - equipment_CheckConditionForFlowBatch
        log.info("Step4 - equipment_CheckConditionForFlowBatch");
        equipmentMethod.equipmentCheckConditionForFlowBatch(objCommon, equipmentID, flowBatchID, BizConstant.SP_OPERATION_FLOWBATCH_EQPRESERVE);
        /*--------------------------*/
        /* Get FLow Batched lot     */
        /*--------------------------*/
        // Step5 - flowBatch_lot_Get
        log.info("Step5 - flowBatch_lot_Get");
        List<Infos.ContainedLotsInFlowBatch> containedLotsInFlowBatchList = flowBatchMethod.flowBatchLotGet(objCommon, flowBatchID);
        /*------------------------------------------------------------------------------------------------------------*/
        /* Check count about EQP's MaxSize, MinSize, MinWaferSize and FlowBatchArea's MaxSize, MinSize, MinWaferSize. */
        /*------------------------------------------------------------------------------------------------------------*/
        log.info("sxEqpReserveForFlowBatchReq():Check count about EQP's BatchSize and FlowBatchArea Size");
        List<Infos.FlowBatchByManualActionReqCassette> flowBatchByManualActionReqCassetteList = new ArrayList<>();
        int lenFlowBatchLot = CimArrayUtils.getSize(containedLotsInFlowBatchList);
        for (int j = 0; j < lenFlowBatchLot; j++) {
            int nCasIdx = -1;
            int lenFlowBatchCas = CimArrayUtils.getSize(flowBatchByManualActionReqCassetteList);
            for (int k = 0; k < lenFlowBatchCas; k++) {
                if (ObjectIdentifier.equalsWithValue(containedLotsInFlowBatchList.get(j).getCassetteID(), flowBatchByManualActionReqCassetteList.get(k).getCassetteID())){
                    nCasIdx = k;
                    break;
                }
            }
            if (nCasIdx < 0) {
                // Add CassetteID to remain information.
                nCasIdx = lenFlowBatchCas;
                Infos.FlowBatchByManualActionReqCassette flowBatchByManualActionReqCassette = new Infos.FlowBatchByManualActionReqCassette();
                flowBatchByManualActionReqCassetteList.add(flowBatchByManualActionReqCassette);
                flowBatchByManualActionReqCassette.setCassetteID(containedLotsInFlowBatchList.get(j).getCassetteID());
            }
            Infos.FlowBatchByManualActionReqCassette flowBatchByManualActionReqCassette = flowBatchByManualActionReqCassetteList.get(nCasIdx);
            List<ObjectIdentifier> lotIDList = flowBatchByManualActionReqCassetteList.get(nCasIdx).getLotID();
            if (CimArrayUtils.isEmpty(lotIDList)){
                lotIDList = new ArrayList<>();
                flowBatchByManualActionReqCassette.setLotID(lotIDList);
            }
            lotIDList.add(containedLotsInFlowBatchList.get(j).getLotID());
        }
        // Step6 - cassette_CheckCountForFlowBatch
        log.info("Step6 - cassette_CheckCountForFlowBatch");
        cassetteMethod.cassetteCheckCountForFlowBatch(objCommon, equipmentID, flowBatchByManualActionReqCassetteList, BizConstant.SP_OPERATION_FLOWBATCH_EQPRESERVE, claimMemo);
        /*------------------------------------*/
        /*   Check Eqp Availability for lot   */
        /*------------------------------------*/
        List<ObjectIdentifier> lotIDs = new ArrayList<>();
        for (Infos.ContainedLotsInFlowBatch flowBatch : containedLotsInFlowBatchList) {
            lotIDs.add(flowBatch.getLotID());
        }
        // Step7 - equipment_CheckAvailForLot
        log.info("Step7 - equipment_CheckAvailForLot");
        equipmentMethod.equipmentCheckAvailForLot(objCommon, equipmentID, lotIDs);
        /*------------------------------------*/
        /*   Check Entity Inhibit for Eqp/lot */
        /*------------------------------------*/
        // Step8 - equipment_CheckInhibitForLot
        log.info("Step8 - equipment_CheckInhibitForLot");
        equipmentMethod.equipmentCheckInhibitForLot(objCommon, equipmentID, lotIDs);
        //------------------------------------
        // Check LOCK Hold.
        //------------------------------------
        // Step9 - lot_CheckLockHoldConditionForOperation
        log.info("Step9 - lot_CheckLockHoldConditionForOperation");
        lotMethod.lotCheckLockHoldConditionForOperation(objCommon, lotIDs);
        //-----------------------------
        //  Check InPostProcessFlag
        //-----------------------------
        // Step10 - Check InPostProcessFlag
        log.info("Step10 - Check InPostProcessFlag");
        int lenLot = CimArrayUtils.getSize(containedLotsInFlowBatchList);
        for (int i = 0; i < lenLot; i++) {
            //----------------------------------
            //  Get InPostProcessFlag of lot
            //----------------------------------
            // Step10_1 - lot_inPostProcessFlag_Get
            log.info("Step10_1 - lot_inPostProcessFlag_Get");
            Outputs.ObjLotInPostProcessFlagOut strLotInPostProcessFlagGetOut = lotMethod.lotInPostProcessFlagGet(objCommon, lotIDs.get(i));
            //----------------------------------------------
            //  If lot is in post process, returns error
            //----------------------------------------------
            if (CimBooleanUtils.isTrue(strLotInPostProcessFlagGetOut.getInPostProcessFlagOfLot())) {
                log.info("sxEqpReserveForFlowBatchReq():lot is in post process.");
                /*---------------------------*/
                /* Get UserGroupID By UserID */
                /*---------------------------*/
                // Step10_2 - person_userGroupList_GetDR
                log.info("Step10_2 - person_userGroupList_GetDR");
                List<ObjectIdentifier> userGroupIDs = personMethod.personUserGroupListGetDR(objCommon, objCommon.getUser().getUserID());
                int userGroupIDsLen = CimArrayUtils.getSize(userGroupIDs);
                int nCnt = 0;
                for (nCnt = 0; nCnt < userGroupIDsLen; nCnt++){
                }
                if (nCnt == userGroupIDsLen){
                    throw new ServiceException(new OmCode(retCodeConfig.getLotInPostProcess(), lotIDs.get(i).getValue()));
                }
            }
        }
        /*--------------------------------*/
        /*   Make eqp reservation   */
        /*--------------------------------*/
        // Step11 - flowBatch_reserveEquipmentID_Set
        log.info("Step11 - flowBatch_reserveEquipmentID_Set");
        flowBatchMethod.flowBatchReserveEquipmentIDSet(objCommon, equipmentID, flowBatchID);

        /*--------------------------*/
        /*   Set return structure   */
        /*--------------------------*/
        // Step12 - flowBatch_FillInTxDSC003
        log.info("Step12 - flowBatch_FillInTxDSC003");
        List<Infos.FlowBatchedLot> strFlowBatchFillInTxDSC003Out = flowBatchMethod.flowBatchFillInTxDSC003(objCommon, equipmentID, flowBatchID);

        // Step13 - lotFlowBatchEvent_Make
        log.info("Step13 - lotFlowBatchEvent_Make");
        for (Infos.ContainedLotsInFlowBatch containedLotsInFlowBatch : containedLotsInFlowBatchList){
            eventMethod.lotFlowBatchEventMake(objCommon, TransactionIDEnum.EQP_RESERVE_REQ.getValue(), containedLotsInFlowBatch.getLotID(), flowBatchID, claimMemo);
        }
        // set return structure.
        log.info("sxEqpReserveForFlowBatchReq():exit the sxEqpReserveForFlowBatchReq method...");
        return strFlowBatchFillInTxDSC003Out;
    }

    @Override
    public void sxFlowBatchCheckForLotSkipReq(Infos.ObjCommon objCommon, Params.FlowBatchCheckForLotSkipReqParams params) {
        //step1 - lot_inPostProcessFlag_Get
        log.info("step1 - lot_inPostProcessFlag_Get");
        ObjectIdentifier lotID = params.getLotID();
        Outputs.ObjLotInPostProcessFlagOut strLotInPostProcessFlagGetOut = lotMethod.lotInPostProcessFlagGet(objCommon, lotID);
        if(CimBooleanUtils.isTrue(strLotInPostProcessFlagGetOut.getInPostProcessFlagOfLot())){
            //step2- person_userGroupList_GetDR
            log.info("step2- person_userGroupList_GetDR");
            List<ObjectIdentifier>  userGroupIDs = personMethod.personUserGroupListGetDR(objCommon, objCommon.getUser().getUserID());
            int nCnt = 0;
            if(CimArrayUtils.isNotEmpty(userGroupIDs)){
                for (ObjectIdentifier userGroupID : userGroupIDs) {
                    nCnt++;
                }
            }
            Validations.check(nCnt == userGroupIDs.size(), new OmCode(retCodeConfig.getLotInPostProcess(), lotID.getValue()));
        }
        //step3 - lot_flowBatch_CheckLocate
        log.info("step3 - lot_flowBatch_CheckLocate");
        lotMethod.lotFlowBatchCheckLocate(objCommon, params.getLocateDirection(), params.getLotID(), params.getProcessRef());
    }

    @Override
    public Results.ReFlowBatchByManualActionReqResult sxReFlowBatchByManualActionReq(Infos.ObjCommon objCommon, Params.ReFlowBatchByManualActionReqParam param) {
        ObjectIdentifier equipmentID = param.getEquipmentID();
        Results.ReFlowBatchByManualActionReqResult reFlowBatchByManualActionReq = new Results.ReFlowBatchByManualActionReqResult();
        // step1 - object_lockMode_Get
        Inputs.ObjLockModeIn objLockModeIn = new Inputs.ObjLockModeIn();
        objLockModeIn.setObjectID(equipmentID);
        objLockModeIn.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
        objLockModeIn.setFunctionCategory(TransactionIDEnum.REFLOW_BATCHING_REQ.getValue());
        objLockModeIn.setUserDataUpdateFlag(false);
        Outputs.ObjLockModeOut objLockModeOut = objectMethod.objectLockModeGet(objCommon, objLockModeIn);
        Long lockMode = objLockModeOut.getLockMode();
        Inputs.ObjAdvanceLockIn strAdvancedobjecLockin = new Inputs.ObjAdvanceLockIn();
        if (!lockMode.equals(BizConstant.SP_EQP_LOCK_MODE_WRITE)){
            // step2 - advanced_object_Lock
            List<String> dummySeq;
            dummySeq = new ArrayList<>(0);
            strAdvancedobjecLockin.setObjectID(equipmentID);
            strAdvancedobjecLockin.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
            strAdvancedobjecLockin.setObjectType(BizConstant.SP_OBJECTLOCK_OBJECTTYPE_MAINOBJECT);
            strAdvancedobjecLockin.setLockType(objLockModeOut.getRequiredLockForMainObject());
            strAdvancedobjecLockin.setKeyList(dummySeq);

            objectLockMethod.advancedObjectLock(objCommon, strAdvancedobjecLockin);
        } else {
            // step3 - object_Lock
            objectLockMethod.objectLock(objCommon, CimMachine.class, equipmentID);
        }
        /*---------------------------------------------------------------------------------------------------*/
        /*                                                                                                   */
        /*   Make FromFlowBatchID Sequence from strReFlowBatchByManualActionReqCassette(InParam)                        */
        /*                                                                                                   */
        /*---------------------------------------------------------------------------------------------------*/
        List<Infos.ReFlowBatchByManualActionReqCassette> strFlowBatchByManualActionReqCassette = param.getStrFlowBatchByManualActionReqCassette();
        if(!CimArrayUtils.isEmpty(strFlowBatchByManualActionReqCassette)){
            List<ObjectIdentifier> fromBatchIDs = new ArrayList<>();
            for (Infos.ReFlowBatchByManualActionReqCassette flowBatchByManualActionReqCassette : strFlowBatchByManualActionReqCassette) {
                boolean bAlreadyExist = false;
                if(!CimArrayUtils.isEmpty(fromBatchIDs)){
                    for (ObjectIdentifier fromBatchID : fromBatchIDs) {
                        Validations.check(ObjectIdentifier.isEmptyWithValue(flowBatchByManualActionReqCassette.getFromFlowBatchID()), retCodeConfig.getNotFoundFlowBatch());
                        if(CimStringUtils.equals(flowBatchByManualActionReqCassette.getFromFlowBatchID().getValue(), fromBatchID.getValue())){
                            bAlreadyExist = true;
                            break;
                        }
                    }
                }
                if(CimBooleanUtils.isFalse(bAlreadyExist)){
                    fromBatchIDs.add(flowBatchByManualActionReqCassette.getFromFlowBatchID());
                }
            }

            //Remove ReBatch Cassettes from FloatingBatch
            for (ObjectIdentifier fromBatchID : fromBatchIDs) {
                //step4 - flowBatch_cassette_Get
                log.info("step4 - flowBatch_cassette_Get");
                List<Infos.ContainedCassettesInFlowBatch> containedCassettesInFlowBatcheList = flowBatchMethod.flowBatchCassetteGet(objCommon, fromBatchID);
                int nRemainCassette = 0;
                ObjectIdentifier foundFirstLotID = new ObjectIdentifier();
                if (!CimArrayUtils.isEmpty(containedCassettesInFlowBatcheList)) {
                    for (Infos.ContainedCassettesInFlowBatch containedCassettesInFlowBatchs : containedCassettesInFlowBatcheList) {
                        boolean bRebatch = false;
                        for (Infos.ReFlowBatchByManualActionReqCassette flowBatchByManualActionReqCassette : strFlowBatchByManualActionReqCassette) {
                            if(ObjectIdentifier.isEmptyWithValue(foundFirstLotID)){
                                int lenLot = CimArrayUtils.getSize(containedCassettesInFlowBatchs.getStrContainedLotInCassetteInFlowBatch());
                                if(0 < lenLot){
                                    foundFirstLotID = containedCassettesInFlowBatchs.getStrContainedLotInCassetteInFlowBatch().get(0).getLotID();
                                }
                            }
                            if(ObjectIdentifier.equalsWithValue(containedCassettesInFlowBatchs.getCassetteID(), flowBatchByManualActionReqCassette.getCassetteID())){
                                bRebatch = true;
                                break;
                            }
                        }
                        if(CimBooleanUtils.isFalse(bRebatch)){
                            nRemainCassette++;
                        }
                    }

                    //step5 - process_GetFlowBatchDefinition
                    log.info("step5 - process_GetFlowBatchDefinition");
                    Outputs.ObjProcessGetFlowBatchDefinitionOut objProcessGetFlowBatchDefinitionOut = processMethod.processGetFlowBatchDefinition(objCommon, foundFirstLotID);
                    List<Infos.RemoveCassette> strRemoveCassette = new ArrayList<>();
                    if(objProcessGetFlowBatchDefinitionOut.getMinimumSize() > nRemainCassette){
                        for (Infos.ContainedCassettesInFlowBatch cassettesInFlowBatch : containedCassettesInFlowBatcheList) {
                            Infos.RemoveCassette removeCassette = new Infos.RemoveCassette();
                            removeCassette.setCassetteID(cassettesInFlowBatch.getCassetteID());
                            List<Infos.ContainedLotInCassetteInFlowBatch> strContainedLotInCassetteInFlowBatch = cassettesInFlowBatch.getStrContainedLotInCassetteInFlowBatch();
                            List<ObjectIdentifier> lotID = new ArrayList<>();
                            removeCassette.setLotID(lotID);
                            for (Infos.ContainedLotInCassetteInFlowBatch containedLotInCassetteInFlowBatch : strContainedLotInCassetteInFlowBatch) {
                                lotID.add(containedLotInCassetteInFlowBatch.getLotID());
                            }
                            strRemoveCassette.add(removeCassette);
                        }
                    } else {
                        // Remove Rebatch Cassettes Only
                        for (Infos.ReFlowBatchByManualActionReqCassette flowBatchByManualActionReqCassette : strFlowBatchByManualActionReqCassette) {
                            if(CimStringUtils.equals(fromBatchID.getValue(), flowBatchByManualActionReqCassette.getFromFlowBatchID().getValue())){
                                Infos.RemoveCassette removeCassette = new Infos.RemoveCassette();
                                removeCassette.setCassetteID(flowBatchByManualActionReqCassette.getCassetteID());
                                removeCassette.setLotID(flowBatchByManualActionReqCassette.getLotID());
                                strRemoveCassette.add(removeCassette);
                            }
                        }
                    }
                    Params.FlowBatchLotRemoveReq flowBatchLotRemoveReq = new Params.FlowBatchLotRemoveReq();
                    flowBatchLotRemoveReq.setStrRemoveCassette(strRemoveCassette);
                    flowBatchLotRemoveReq.setFlowBatchID(fromBatchID);
                    flowBatchLotRemoveReq.setClaimMemo(param.getClaimMemo());

                    //step6 - txFlowBatchLotRemoveReq
                    log.info("step6 - txFlowBatchLotRemoveReq");
                    sxFlowBatchLotRemoveReq(objCommon,flowBatchLotRemoveReq);
                }
            }
            //ReBatch Cassettes of FloationgBatch
            //Check Eqp Availability for lot
            for (Infos.ReFlowBatchByManualActionReqCassette flowBatchByManualActionReqCassette : strFlowBatchByManualActionReqCassette) {
                //step7 - equipment_CheckAvailForLot
                log.info("step7 - equipment_CheckAvailForLot");
                equipmentMethod.equipmentCheckAvailForLot(objCommon, param.getEquipmentID(), flowBatchByManualActionReqCassette.getLotID());
                // Check Entity Inhibit for Eqp/lot
                //step8 - equipment_CheckInhibitForLot
                log.info("step8 - equipment_CheckInhibitForLot");
                equipmentMethod.equipmentCheckInhibitForLot(objCommon, param.getEquipmentID(), flowBatchByManualActionReqCassette.getLotID());
            }
            // Check LOCK Hold.
            //step9 - lot_CheckLockHoldConditionForOperation
            log.info("step9 - lot_CheckLockHoldConditionForOperation");
            for (Infos.ReFlowBatchByManualActionReqCassette flowBatchByManualActionReqCassette : strFlowBatchByManualActionReqCassette) {
                List<ObjectIdentifier> lotIDList = flowBatchByManualActionReqCassette.getLotID();
                lotMethod.lotCheckLockHoldConditionForOperation(objCommon, lotIDList);
            }
            //Check InPostProcessFlag
            List<ObjectIdentifier> userGroupIDs = new ArrayList<>();
            for (Infos.ReFlowBatchByManualActionReqCassette flowBatchByManualActionReqCassette : strFlowBatchByManualActionReqCassette) {
                List<ObjectIdentifier> lotIDs = flowBatchByManualActionReqCassette.getLotID();
                for (ObjectIdentifier lotId : lotIDs) {
                    //Get InPostProcessFlag of lot
                    //step10 - lot_inPostProcessFlag_Get
                    log.info("step10 - lot_inPostProcessFlag_Get");
                    Outputs.ObjLotInPostProcessFlagOut strLotInPostProcessFlagGetOut = lotMethod.lotInPostProcessFlagGet(objCommon, lotId);
                    //If lot is in post process, returns error
                    if(CimBooleanUtils.isTrue(strLotInPostProcessFlagGetOut.getInPostProcessFlagOfLot())){
                        if(CimArrayUtils.isEmpty(userGroupIDs)){
                            //Get UserGroupID By UserID
                            //step11 - person_userGroupList_GetDR
                            log.info("step11 - person_userGroupList_GetDR");
                            userGroupIDs = personMethod.personUserGroupListGetDR(objCommon, objCommon.getUser().getUserID());
                        }
                        if(!CimArrayUtils.isEmpty(userGroupIDs)){
                            int nCnt = 0;
                            for (ObjectIdentifier userGroupID : userGroupIDs) {
                                nCnt++;
                            }
                            Validations.check(nCnt == userGroupIDs.size(), new OmCode(retCodeConfig.getLotInPostProcess(), lotId.getValue()));
                        }
                    }
                }
            }

            //step12 - equipment_reserveFlowBatchID_Get__090
            log.info("step12 - equipment_reserveFlowBatchID_Get__090");
            Outputs.ObjEquipmentReserveFlowBatchIDGetOut objEquipmentReserveFlowBatchIDGetOut = equipmentMethod.equipmentReserveFlowBatchIDGet(objCommon, param.getEquipmentID());
            int rsvFBCount = objEquipmentReserveFlowBatchIDGetOut.getFlowBatchIDs().size();
            long maxFBCount = objEquipmentReserveFlowBatchIDGetOut.getFlowBatchMaxCount();
            boolean bFlowBatchFilled = false;
            if (maxFBCount == 0) {
                if (rsvFBCount >= 1) {
                    bFlowBatchFilled = true;
                }
            } else if (maxFBCount > 0) {
                if (rsvFBCount >= maxFBCount) {
                    bFlowBatchFilled = true;
                }
            }
            Validations.check(CimBooleanUtils.isTrue(bFlowBatchFilled), retCodeConfig.getEquipmentReservedForOtherFlowBatch());
            // Check cassette infromation for Flow Batch
            List<Infos.FlowBatchByManualActionReqCassette> strFlowBatchByManualActionReqCassettes = new ArrayList<>();
            for (Infos.ReFlowBatchByManualActionReqCassette reFlowBatchByManualActionReqCassette : strFlowBatchByManualActionReqCassette) {
                Infos.FlowBatchByManualActionReqCassette flowBatchByManualActionReqCassette = new Infos.FlowBatchByManualActionReqCassette();
                flowBatchByManualActionReqCassette.setCassetteID(reFlowBatchByManualActionReqCassette.getCassetteID());
                flowBatchByManualActionReqCassette.setLotID(reFlowBatchByManualActionReqCassette.getLotID());
                strFlowBatchByManualActionReqCassettes.add(flowBatchByManualActionReqCassette);
            }
            List<ObjectIdentifier> reservedFlowBatchIDs = null;
            if (!CimArrayUtils.isEmpty(objEquipmentReserveFlowBatchIDGetOut.getFlowBatchIDs())) {
                reservedFlowBatchIDs = objEquipmentReserveFlowBatchIDGetOut.getFlowBatchIDs();
            }  else {
                // must add null
                reservedFlowBatchIDs = new ArrayList<>();
                reservedFlowBatchIDs.add(new ObjectIdentifier());
            }
            for (ObjectIdentifier reservedFlowBatchID : reservedFlowBatchIDs) {
                //step13 - cassette_CheckConditionForFlowBatch
                log.info("step13 - cassette_CheckConditionForFlowBatch");
                cassetteMethod.cassetteCheckConditionForFlowBatch(objCommon, param.getEquipmentID(), reservedFlowBatchID, strFlowBatchByManualActionReqCassettes, BizConstant.SP_OPERATION_FLOWBATCH_REBATCH);
            }
            // Check cassette Count for Flow Batch
            //step14 - cassette_CheckCountForFlowBatch
            log.info("step14 - cassette_CheckCountForFlowBatch");
            cassetteMethod.cassetteCheckCountForFlowBatch(objCommon, param.getEquipmentID(), strFlowBatchByManualActionReqCassettes, BizConstant.SP_OPERATION_FLOWBATCH_REBATCH, param.getClaimMemo());
            long eqpMonitorSwitch = StandardProperties.OM_AUTOMON_FLAG.getLongValue();
            if (eqpMonitorSwitch == 1) {
                List<Infos.StartSeqNo> strStartSeqNoSeq = new ArrayList<>();
                int eqpMonJobCnt = 0;
                for (Infos.FlowBatchByManualActionReqCassette flowBatchByManualActionReqCassette : strFlowBatchByManualActionReqCassettes) {
                    List<ObjectIdentifier> lotID = flowBatchByManualActionReqCassette.getLotID();
                    if (CimArrayUtils.isNotEmpty(lotID)) {
                        for (ObjectIdentifier lotId : lotID) {
                            //step15 - lot_lotType_Get
                            log.info("step15 - lot_lotType_Get");
                            String lotLotType = lotMethod.lotTypeGet(objCommon, lotId);
                            if (CimStringUtils.equals(BizConstant.SP_LOT_TYPE_EQUIPMENTMONITORLOT, lotLotType)
                                    || CimStringUtils.equals(BizConstant.SP_LOT_TYPE_DUMMYLOT, lotLotType)) {
                                //Acquire EqpMonitor Job Information on Lot
                                //step16 - lot_eqpMonitorJob_Get
                                log.info("step16 - lot_eqpMonitorJob_Get");
                                Infos.EqpMonitorJobLotInfo eqpMonitorJob = lotMethod.lotEqpMonitorJobGet(objCommon, lotId);
                                if (ObjectIdentifier.isEmptyWithValue(eqpMonitorJob.getEqpMonitorJobID())) {
                                    continue;
                                }
                                if (!ObjectIdentifier.equalsWithValue(eqpMonitorJob.getEquipmentID(), param.getEquipmentID())) {
                                    continue;
                                }
                                boolean bNewEqpMonJob = true;
                                for (int i = 0; i < eqpMonJobCnt; i++) {
                                    if (CimStringUtils.equals(strStartSeqNoSeq.get(i).getKey(), eqpMonitorJob.getEqpMonitorJobID().getValue())) {
                                        if (!eqpMonitorJob.getStartSeqNo().equals(strStartSeqNoSeq.get(i).getStartSeqNo())) {
                                            throw new ServiceException(retCodeConfig.getInvalidParameterWithMsg());
                                        }
                                        bNewEqpMonJob = false;
                                        break;
                                    }
                                }
                                if (CimBooleanUtils.isTrue(bNewEqpMonJob)) {
                                    Infos.StartSeqNo startSeqNo = new Infos.StartSeqNo();
                                    startSeqNo.setKey(eqpMonitorJob.getEqpMonitorJobID().getValue());
                                    startSeqNo.setStartSeqNo(eqpMonitorJob.getStartSeqNo().intValue());
                                    strStartSeqNoSeq.add(startSeqNo);
                                    eqpMonJobCnt++;
                                }
                            }
                        }
                    }
                }
            }
            //Make new flow batch
            List<Infos.BatchingReqLot> strBatchingReqLot = new ArrayList<>();
            for (Infos.FlowBatchByManualActionReqCassette flowBatchByManualActionReqCassette : strFlowBatchByManualActionReqCassettes) {
                List<ObjectIdentifier> lotID = flowBatchByManualActionReqCassette.getLotID();
                for (ObjectIdentifier lotId : lotID) {
                    Infos.BatchingReqLot batchingReqLot = new Infos.BatchingReqLot();
                    batchingReqLot.setCassetteID(flowBatchByManualActionReqCassette.getCassetteID());
                    batchingReqLot.setLotID(lotId);
                    strBatchingReqLot.add(batchingReqLot);
                }
            }
            //step17 - flowBatch_Make
            log.info("step17 - flowBatch_Make");
            Outputs.ObjFlowBatchMakeOut objFlowBatchMakeOut = flowBatchMethod.flowBatchMake(objCommon, strBatchingReqLot);
            //step18 - txEqpReserveForFlowBatchReq
            log.info("step18 - txEqpReserveForFlowBatchReq");
            List<Infos.FlowBatchedLot> flowBatchedLotList = sxEqpReserveForFlowBatchReq(objCommon,param.getEquipmentID(), objFlowBatchMakeOut.getBatchID(), param.getClaimMemo());
            //Make Event Record
            // step19 - lotFlowBatchEvent_Make
            log.info("step19 - lotFlowBatchEvent_Make");
            if (!CimObjectUtils.isEmpty(param.getStrFlowBatchByManualActionReqCassette())){
                for (Infos.ReFlowBatchByManualActionReqCassette reFlowBatchByManualActionReqCassette : param.getStrFlowBatchByManualActionReqCassette()){
                    for (ObjectIdentifier lotID : reFlowBatchByManualActionReqCassette.getLotID()){
                        eventMethod.lotFlowBatchEventMake(objCommon, TransactionIDEnum.REFLOW_BATCHING_REQ.getValue(), lotID, objFlowBatchMakeOut.getBatchID(), param.getClaimMemo());
                    }
                }
            }
            reFlowBatchByManualActionReq.setEquipmentID(param.getEquipmentID());
            reFlowBatchByManualActionReq.setFlowBatchID(objFlowBatchMakeOut.getBatchID());
            reFlowBatchByManualActionReq.setStrFlowBatchedLot(flowBatchedLotList);
        }

        return reFlowBatchByManualActionReq;
    }
}
