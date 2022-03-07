package com.fa.cim.service.plan.Impl;

import com.fa.cim.annotaion.OmService;
import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.constant.CIMStateConst;
import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ErrorCode;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.OmCode;
import com.fa.cim.common.support.RetCode;
import com.fa.cim.common.utils.*;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.dto.*;
import com.fa.cim.idp.tms.api.TmsService;
import com.fa.cim.method.*;
import com.fa.cim.newcore.bo.durable.CimCassette;
import com.fa.cim.newcore.bo.planning.CimProductRequest;
import com.fa.cim.newcore.bo.product.CimLot;
import com.fa.cim.newcore.impl.bo.env.StandardProperties;
import com.fa.cim.service.lot.ILotService;
import com.fa.cim.service.plan.IPlanService;
import com.fa.cim.service.processcontrol.IProcessControlService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.fa.cim.common.constant.BizConstant.SP_ENTRYTYPE_CANCEL;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/9/8        ********            Nyx                create file
 *
 * @author: Nyx
 * @date: 2020/9/8 16:06
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@OmService
public class PlanService implements IPlanService {
    @Autowired
    private RetCodeConfig retCodeConfig;

    @Autowired
    private ICassetteMethod cassetteMethod;

    @Autowired
    private ILotMethod lotMethod;

    @Autowired
    private IPersonMethod personMethod;

    @Autowired
    private IEventMethod eventMethod;

    @Autowired
    private IObjectLockMethod objectLockMethod;
    @Autowired
    private IQTimeMethod qTimeMethod;
    @Autowired
    private ILotService lotService;
    @Autowired
    private IProcessControlService processControlService;
    @Autowired
    private TmsService tmsService;

    @Autowired
    private IScheduleChangeReservationMethod scheduleChangeReservationMethod;

    @Autowired
    private IProductMethod productMethod;
    @Autowired
    private IProcessMethod processMethod;
    @Autowired
    private IMessageMethod messageMethod;

    @Autowired
    private IContaminationMethod contaminationMethod;

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param strProdOrderChangeReqResult
     * @param objCommon
     * @param seqIx
     * @param strChangedLotAttributes
     * @param claimMemo
     * @return com.fa.cim.dto.RetCode<java.util.List<com.fa.cim.pojo.Infos.ChangeLotReturn>>
     * @author Ho
     * @date 2018/11/23 13:35:46
     */
    public List<Infos.ChangeLotReturn> sxProdOrderChangeReq(List<Infos.ChangeLotReturn> strProdOrderChangeReqResult, Infos.ObjCommon objCommon, Integer seqIx, List<Infos.ChangedLotAttributes> strChangedLotAttributes, String claimMemo) {


        List<Infos.ChangeLotReturn> strChangeLotReturn = strProdOrderChangeReqResult;
        Infos.ChangeLotReturn changeLotReturn = CimArrayUtils.get(strChangeLotReturn, seqIx, Infos.ChangeLotReturn.class);
        if (strChangeLotReturn==null){
            strChangeLotReturn=new ArrayList<>();
        }

        if(seqIx >= CimArrayUtils.getSize(strChangedLotAttributes) || seqIx >= CimArrayUtils.getSize(strChangeLotReturn)) {
            changeLotReturn.setLotID(strChangedLotAttributes.get(seqIx).getLotID());
            changeLotReturn.setReturnCode(CimObjectUtils.toString(retCodeConfig.getInvalidParameter().getCode()));
            throw new ServiceException(retCodeConfig.getInvalidParameter(), strChangeLotReturn);
        }

        // step1 - object_Lock
        objectLockMethod.objectLock(objCommon, CimLot.class, strChangedLotAttributes.get(seqIx).getLotID());

        String methodName = null;

        Boolean bUpdateSubLotType = false;
        Infos.ChangedLotAttributes changedLotAttributes = strChangedLotAttributes.get(seqIx);
        if( !CimStringUtils.isEmpty(changedLotAttributes.getSubLotType()) ) {
            bUpdateSubLotType = true;
        }

        if( bUpdateSubLotType ) {
            String lotOperationEIcheck = StandardProperties.OM_CARRIER_CHK_EI_FOR_LOT_OPERATION.getValue();
            if ( CimStringUtils.equals(lotOperationEIcheck, BizConstant.VALUE_ZERO)) {
                // step2 - lot_controlJobID_Get
                ObjectIdentifier strLotControlJobIDGetOut = lotMethod.lotControlJobIDGet(objCommon, changedLotAttributes.getLotID());
                Validations.check(ObjectIdentifier.isNotEmptyWithValue(strLotControlJobIDGetOut), new OmCode(retCodeConfig.getLotControlJobidFilled()
                        , ObjectIdentifier.fetchValue(changedLotAttributes.getLotID()), ObjectIdentifier.fetchValue(strLotControlJobIDGetOut)));
            } else {
                // step3 - lot_cassette_Get
                ObjectIdentifier cassetteID = lotMethod.lotCassetteGet(objCommon, changedLotAttributes.getLotID());

                // step4 - cassette_transferState_Get
                String strCassetteXferState = null;
                try {
                    strCassetteXferState = cassetteMethod.cassetteTransferStateGet(objCommon, cassetteID);
                } catch (ServiceException e){
                    changeLotReturn.setLotID(changedLotAttributes.getLotID());
                    changeLotReturn.setReturnCode(CimObjectUtils.toString(e.getCode()));
                    throw new ServiceException(new OmCode(e.getCode(), e.getMessage()), strChangeLotReturn);
                }

                if( CimStringUtils.equals(strCassetteXferState, BizConstant.SP_TRANSSTATE_EQUIPMENTIN) ) {
                    changeLotReturn.setLotID(changedLotAttributes.getLotID());
                    changeLotReturn.setReturnCode(CimObjectUtils.toString(retCodeConfig.getInvalidCassetteState().getCode()));
                    throw new ServiceException(retCodeConfig.getInvalidCassetteState(), strChangeLotReturn);
                }

                // step5 - cassette_controlJobID_Get
                ObjectIdentifier strCassetteControlJobIDGetOut = null;
                try {
                    strCassetteControlJobIDGetOut = cassetteMethod.cassetteControlJobIDGet(objCommon, cassetteID);
                } catch (ServiceException e){
                    changeLotReturn.setLotID(changedLotAttributes.getLotID());
                    changeLotReturn.setReturnCode(CimObjectUtils.toString(e.getCode()));
                    throw e;
                }
                if (ObjectIdentifier.isNotEmptyWithValue(strCassetteControlJobIDGetOut)) {
                    changeLotReturn.setLotID(changedLotAttributes.getLotID());
                    changeLotReturn.setReturnCode(CimObjectUtils.toString(retCodeConfig.getCassetteControlJobFilled().getCode()));
                    throw new ServiceException(retCodeConfig.getCassetteControlJobFilled(), strChangeLotReturn);
                }
            }
        }

        // step6 - lot_inPostProcessFlag_Get
        Outputs.ObjLotInPostProcessFlagOut strLotInPostProcessFlagGetOut = lotMethod.lotInPostProcessFlagGet(objCommon, changedLotAttributes.getLotID());

        // step7 - lot_interFabXferState_Get
        String strLotInterFabXferStateGetOut = lotMethod.lotInterFabXferStateGet(objCommon, changedLotAttributes.getLotID());
        if( CimStringUtils.equals(strLotInterFabXferStateGetOut, BizConstant.SP_INTERFAB_XFERSTATE_TRANSFERRING) ) {
            changeLotReturn.setLotID(changedLotAttributes.getLotID());
            changeLotReturn.setReturnCode(CimObjectUtils.toString(retCodeConfig.getInterfabInvalidLotXferstateForReq().getCode()));
            throw new ServiceException(retCodeConfig.getInterfabInvalidLotXferstateForReq(), strChangeLotReturn);
        }

        if( CimBooleanUtils.isTrue(strLotInPostProcessFlagGetOut.getInPostProcessFlagOfLot()) ) {

            if ( !CimStringUtils.equals(strLotInterFabXferStateGetOut, BizConstant.SP_INTERFAB_XFERSTATE_REQUIRED) ) {
                // step8 - person_userGroupList_GetDR
                List<ObjectIdentifier> userGroupIDs = null;
                try{
                    userGroupIDs = personMethod.personUserGroupListGetDR(objCommon, objCommon.getUser().getUserID());
                }catch (ServiceException ex){
                    changeLotReturn.setLotID(changedLotAttributes.getLotID());
                    changeLotReturn.setReturnCode(CimObjectUtils.toString(retCodeConfig.getInterfabInvalidLotXferstateForReq().getCode()));
                    throw new ServiceException(new OmCode(ex.getCode(),ex.getMessage()), strChangeLotReturn);
                }
                int userGroupIDsLen = CimArrayUtils.getSize(userGroupIDs);
                int nCnt = 0;
                for (nCnt = 0; nCnt < userGroupIDsLen; nCnt++) {
                    ObjectIdentifier userGroupID = userGroupIDs.get(nCnt);
                }
                if (nCnt == userGroupIDsLen) {
                    changeLotReturn.setLotID(changedLotAttributes.getLotID());
                    changeLotReturn.setReturnCode(CimObjectUtils.toString(retCodeConfig.getLotInPostProcess().getCode()));
                    throw new ServiceException(retCodeConfig.getLotInPostProcess(), strChangeLotReturn);
                }
            }
        }

        // step9 - lot_ChangeOrder
        lotMethod.lotChangeOrder(objCommon, changedLotAttributes);

        // step10 - lotChangeEvent_Make
        Inputs.LotChangeEventMakeParams lotChangeEventMakeParams = new Inputs.LotChangeEventMakeParams();
        lotChangeEventMakeParams.setTransactionID(TransactionIDEnum.LOT_MFG_ORDER_CHANGE_REQ.getValue());
        lotChangeEventMakeParams.setLotID(changedLotAttributes.getLotID().getValue());
        lotChangeEventMakeParams.setExternalPriority(changedLotAttributes.getExternalPriority());
        lotChangeEventMakeParams.setLotOwnerID(changedLotAttributes.getLotOwner().getValue());
        lotChangeEventMakeParams.setLotComment(changedLotAttributes.getManufacturingOrderNumber());
        lotChangeEventMakeParams.setCustomerCodeID(changedLotAttributes.getCustomerCode());
        lotChangeEventMakeParams.setLotComment(changedLotAttributes.getLotComment());
        lotChangeEventMakeParams.setPriorityClass("0");
        lotChangeEventMakeParams.setProductID("");
        lotChangeEventMakeParams.setPreviousProductID("");
        lotChangeEventMakeParams.setPlanStartTime("");
        lotChangeEventMakeParams.setPlanCompTime("");
        lotChangeEventMakeParams.setClaimMemo(claimMemo);
        eventMethod.lotChangeEventMake(objCommon, lotChangeEventMakeParams);

        changeLotReturn.setLotID(changedLotAttributes.getLotID());
        changeLotReturn.setReturnCode(CimObjectUtils.toString(retCodeConfig.getSucc().getCode()));

        // check contamination level whether match, if the not match then on hold
        contaminationMethod.lotCheckContaminationLevelStepOut(objCommon, changedLotAttributes.getLotID());

        return strProdOrderChangeReqResult;
    }

    @Override
    public List<Results.LotExternalPriorityResult> sxLotExtPriorityModifyReq(Infos.ObjCommon objCommon, List<Infos.LotExternalPriority> lotExternalPriorityList, Boolean commitByLotFlag, int seqIx) {
        List<Results.LotExternalPriorityResult> data = new ArrayList<>();
        // Check length of In-Parameter
        if (lotExternalPriorityList == null || seqIx >= lotExternalPriorityList.size()) {
            throw new ServiceException(retCodeConfig.getInvalidParameter());
        }
        objectLockMethod.objectLock(objCommon, CimLot.class, lotExternalPriorityList.get(seqIx).getLotID());

        ObjectIdentifier lotID = lotExternalPriorityList.get(seqIx).getLotID();
        String interFabXferState = lotMethod.lotInterFabXferStateGet(objCommon, lotID);
        Validations.check(CimStringUtils.equals(interFabXferState, BizConstant.SP_INTERFAB_XFERSTATE_TRANSFERRING), new OmCode(
                retCodeConfig.getInterfabInvalidLotXferstateForReq(), ObjectIdentifier.fetchValue(lotID), interFabXferState));

        Results.LotExternalPriorityResult lotExternalPriorityResult = new Results.LotExternalPriorityResult();
        lotExternalPriorityResult.setLotID(lotID);
        data.add(lotExternalPriorityResult);

        //Step3 - Update lot External Priority
        lotMethod.lotExternalPriorityUpdate(objCommon, lotID, lotExternalPriorityList.get(seqIx).getExternalPriority());
        return data;
    }

    @Override
    public void lotExternalPriorityChangeByLot(Infos.ObjCommon objCommon, List<Infos.LotExternalPriority> lotExternalPriorityList, AtomicInteger errorLotCount, List<Results.LotExternalPriorityResult> resultList) {
        for (int index = 0; index < lotExternalPriorityList.size(); index++) {
            Infos.LotExternalPriority lotExternalPriority = lotExternalPriorityList.get(index);
            OmCode returnCode = null;
            try {
                //transaction only control this service method
                List<Results.LotExternalPriorityResult> changeOut = sxLotExtPriorityModifyReq(objCommon, lotExternalPriorityList, true, index);
            } catch (Exception e) {
                log.error("LotExtPriorityModifyReq failed for {}", lotExternalPriority.getLotID().getValue());
                errorLotCount.incrementAndGet();
                if (e instanceof ServiceException) {
                    ServiceException se = (ServiceException) e;
                    returnCode = new OmCode(se.getCode(), se.getMessage());
                } else {
                    returnCode = new ErrorCode(e.getLocalizedMessage());
                }
            }
            Results.LotExternalPriorityResult lotExternalPriorityResult = new Results.LotExternalPriorityResult();
            lotExternalPriorityResult.setResultCode(returnCode);
            lotExternalPriorityResult.setLotID(lotExternalPriority.getLotID());
            resultList.add(lotExternalPriorityResult);
        }
    }

    @Override
    public void lotExternalPriorityChangeByAll(Infos.ObjCommon objCommon, List<Infos.LotExternalPriority> lotExternalPriorityList, AtomicInteger errorLotCount, List<Results.LotExternalPriorityResult> resultList) {
        for (int index = 0; index < lotExternalPriorityList.size(); index++) {
            Infos.LotExternalPriority lotExternalPriority = lotExternalPriorityList.get(index);
            OmCode returnCode = null;
            try {
                //transaction only control this service method
                List<Results.LotExternalPriorityResult> changeOut = sxLotExtPriorityModifyReq(objCommon, lotExternalPriorityList, false, index);
            } catch (Exception e) {
                log.error("LotExtPriorityModifyReq failed for {}", lotExternalPriority.getLotID().getValue());
                errorLotCount.incrementAndGet();
                if (e instanceof ServiceException) {
                    ServiceException se = (ServiceException) e;
                    returnCode = new OmCode(se.getCode(), se.getMessage());
                } else {
                    returnCode = new ErrorCode(e.getLocalizedMessage());
                }
                //set other lot result
                for (int i = 0; i < lotExternalPriorityList.size(); i++) {
                    Results.LotExternalPriorityResult lotExternalPriorityResult = new Results.LotExternalPriorityResult();
                    resultList.add(lotExternalPriorityResult);
                    if (i == index) {
                        lotExternalPriorityResult.setResultCode(returnCode);
                        lotExternalPriorityResult.setLotID(lotExternalPriority.getLotID());
                    } else if (i < index) {
                        resultList.get(index).setResultCode(retCodeConfig.getRolledBackByOtherLotError());
                    } else {
                        lotExternalPriorityResult.setResultCode(retCodeConfig.getNotProcessedByOtherLotError());
                        lotExternalPriorityResult.setLotID(lotExternalPriorityList.get(i).getLotID());
                    }
                }
                //if exist one lot failed ,then break
                return;
            }
            Results.LotExternalPriorityResult lotExternalPriorityResult = new Results.LotExternalPriorityResult();
            lotExternalPriorityResult.setResultCode(returnCode);
            lotExternalPriorityResult.setLotID(lotExternalPriority.getLotID());
            resultList.add(lotExternalPriorityResult);
        }
    }

    @Override
    public Results.LotCurrentQueueReactivateReqResult sxLotRequeueReq(Infos.ObjCommon objCommon,
                                                                      List<Infos.LotReQueueAttributes> lotReQueueAttributesList) {
        Results.LotCurrentQueueReactivateReqResult lotCurrentQueueReactivateReqResult = new Results.LotCurrentQueueReactivateReqResult();
        List<Infos.LotReQueueReturn> lotReQueueReturns = new ArrayList<>();
        lotCurrentQueueReactivateReqResult.setLotReQueueReturnList(lotReQueueReturns);
        //TODO:Lock objects to be updated

        Long lotOperationEIcheck = StandardProperties.OM_CARRIER_CHK_EI_FOR_LOT_OPERATION.getLongValue();
        ObjectIdentifier lotID;
        ObjectIdentifier cassetteID;
        for (Infos.LotReQueueAttributes lotReQueueAttributes : lotReQueueAttributesList){
            lotID = lotReQueueAttributes.getLotID();
            log.info("PPTManager_i::txLotCurrentQueueReactivateReq,lotID = ",lotID.getValue());
            if ( 0 == lotOperationEIcheck){
                //Check Lot's Control Job ID
                ObjectIdentifier lotControlJobIDGet = lotMethod.lotControlJobIDGet(objCommon, lotID);
                if (ObjectIdentifier.isEmpty(lotControlJobIDGet)){
                    log.info("strLot_controlJobID_Get_out.controlJobID.identifier == 0");
                }else {
                    throw new ServiceException(new OmCode(retCodeConfig.getLotControlJobidFilled(),lotID.getValue(),lotControlJobIDGet.getValue()));
                }
            }else {
                cassetteID = lotMethod.lotCassetteGet(objCommon, lotID);
                // Check Cassette Xfer Stat
                String strCassetteXferState = cassetteMethod.cassetteTransferStateGet(objCommon, cassetteID);
                log.info("CassetteXferStatus id = ",strCassetteXferState);
                if (CimStringUtils.equals(strCassetteXferState,BizConstant.SP_TRANSSTATE_EQUIPMENTIN)){
                    throw new ServiceException(new OmCode(retCodeConfig.getInvalidCassetteTransferState(),strCassetteXferState,cassetteID.getValue()));
                }
                // Get Cassette's ControlJobID
                ObjectIdentifier cassetteControlJobIDGet = cassetteMethod.cassetteControlJobIDGet(objCommon, cassetteID);
                if (ObjectIdentifier.isEmpty(cassetteControlJobIDGet)){
                    log.info("strCassette_controlJobID_Get_out.controlJobID.identifier is nil");
                }else {
                    log.info("strCassette_controlJobID_Get_out.controlJobID.identifier is ",cassetteControlJobIDGet.getValue());
                    throw new ServiceException(retCodeConfig.getInvalidCassetteTransferState());
                }
            }
        }

        // Check LOCK Hold.

        for (Infos.LotReQueueAttributes lotReQueueAttributes : lotReQueueAttributesList){
            //  Check lot InterFabXfer state
            lotID = lotReQueueAttributes.getLotID();
            String lotInterFabXferStateGet = lotMethod.lotInterFabXferStateGet(objCommon, lotID);
            if (CimStringUtils.equals(lotInterFabXferStateGet,BizConstant.SP_INTERFAB_XFERSTATE_REQUIRED)){
                log.info("The Lot interFabXfer state is required... No need to check LOCK Hold. ");
                continue;
            }
            List<ObjectIdentifier> lotIDSeq = new ArrayList<>();
            lotIDSeq.add(lotID);
            lotMethod.lotCheckLockHoldConditionForOperation(objCommon,lotIDSeq);
        }

        //Check InPostProcessFlag
        List<ObjectIdentifier> userGroupIDs = new ArrayList<>();
        for (Infos.LotReQueueAttributes lotReQueueAttributes : lotReQueueAttributesList){
            //  Get InPostProcessFlag of Lot
            lotID = lotReQueueAttributes.getLotID();
            Outputs.ObjLotInPostProcessFlagOut objLotInPostProcessFlagOut = lotMethod.lotInPostProcessFlagGet(objCommon, lotID);
            //  Check lot InterFabXfer state
            log.info("call lot_interFabXferState_Get()");
            String lotInterFabXferStateGet = lotMethod.lotInterFabXferStateGet(objCommon, lotID);
            //  If Lot is in post process, returns error
            if (objLotInPostProcessFlagOut.getInPostProcessFlagOfLot()){
                log.info("Lot is in post process.");
                if (CimStringUtils.equals(lotInterFabXferStateGet,BizConstant.SP_INTERFAB_XFERSTATE_REQUIRED)){
                    log.info(" The Lot interFabXfer state is required... No need to check post process flag. ");
                    continue;
                }
                //Get UserGroupID By UserID
                userGroupIDs = personMethod.personUserGroupListGetDR(objCommon, objCommon.getUser().getUserID());
                Integer count = 0;
                for (ObjectIdentifier userGroupID : userGroupIDs){
                    count++;
                }
                if (count == userGroupIDs.size()){
                    log.info("NOT External Post Process User!");
                    throw new ServiceException(retCodeConfig.getLotInPostProcess());
                }
            }
        }

        for (Infos.LotReQueueAttributes lotReQueueAttributes : lotReQueueAttributesList){
            lotID = lotReQueueAttributes.getLotID();
            Infos.LotReQueueReturn lotReQueueReturn = new Infos.LotReQueueReturn();
            lotReQueueReturn.setLotID(lotID);
            //Check Lot Process State
            String lotProcessStateGet = lotMethod.lotProcessStateGet(objCommon, lotID);
            if (!CimStringUtils.equals(lotProcessStateGet,BizConstant.SP_LOT_PROCSTATE_WAITING)){
                log.info("CIMFWStrCmp(strLot_processState_Get_out.theLotProcessState,SP_Lot_ProcState_Waiting) != 0");
                throw new ServiceException(retCodeConfig.getInvalidLotProcstat());
            }
            //Check Finished State of lot
            String lotFinishedStateGet = lotMethod.lotFinishedStateGet(objCommon, lotID);
            if (CimStringUtils.equals(lotFinishedStateGet,BizConstant.SP_LOT_FINISHED_STATE_STACKED)){
                log.info("CIMFWStrCmp(strLot_finishedState_Get_out.lotFinishedState,SP_LOT_FINISHED_STATE_STACKED) == 0");
                continue;
            }
            //Check Lot Inventory State
            if (!ObjectIdentifier.isEmpty(lotReQueueAttributes.getRouteID()) && !CimObjectUtils.isEmpty(lotReQueueAttributes.getCurrentOperationNumber())){
                String lotInventoryStateGet = lotMethod.lotInventoryStateGet(objCommon, lotID);
                if (!CimStringUtils.equals(lotInventoryStateGet,BizConstant.SP_LOT_INVENTORYSTATE_ONFLOOR)){
                    Validations.check(retCodeConfig.getInvalidLotInventoryStat(),lotID, lotInventoryStateGet);
                }
            }

            //Get RouteID,OperationID,ProductID for Lot
            Infos.LotInfoInqFlag lotInfoInqFlag = new Infos.LotInfoInqFlag();
            lotInfoInqFlag.setLotBasicInfoFlag(false);
            lotInfoInqFlag.setLotControlUseInfoFlag(false);
            lotInfoInqFlag.setLotFlowBatchInfoFlag(false);
            lotInfoInqFlag.setLotNoteFlagInfoFlag(false);
            lotInfoInqFlag.setLotOperationInfoFlag(true);
            lotInfoInqFlag.setLotOrderInfoFlag(false);
            lotInfoInqFlag.setLotControlJobInfoFlag(false);
            lotInfoInqFlag.setLotProductInfoFlag(true);
            lotInfoInqFlag.setLotRecipeInfoFlag(false);
            lotInfoInqFlag.setLotLocationInfoFlag(false);
            lotInfoInqFlag.setLotWipOperationInfoFlag(false);
            lotInfoInqFlag.setLotWaferAttributesFlag(false);
            lotInfoInqFlag.setLotBackupInfoFlag(false);
            Infos.LotInfo lotDetailInfoGetDR = lotMethod.lotDetailInfoGetDR(objCommon, lotInfoInqFlag, lotID);
            if (!ObjectIdentifier.equalsWithValue(lotReQueueAttributes.getRouteID(), lotDetailInfoGetDR.getLotOperationInfo().getRouteID())){
                log.info("INVALID_CURRENT_ROUTE");
                throw new ServiceException(retCodeConfig.getInvalidCurrentRoute());
            }

            if (!CimStringUtils.equals(lotReQueueAttributes.getCurrentOperationNumber(),lotDetailInfoGetDR.getLotOperationInfo().getOperationNumber())){
                log.info("INVALID_CURRENT_OPERATION");
                throw new ServiceException(retCodeConfig.getInvalidCurrentOperation());
            }

            if (!ObjectIdentifier.equalsWithValue(lotReQueueAttributes.getProductID(), lotDetailInfoGetDR.getLotProductInfo().getProductID())){
                log.info("INVALID_CURRENT_PRODUCT");
                throw new ServiceException(retCodeConfig.getInvalidCurrentProduct());
            }

            //Register Product Request
            Outputs.ObjLotChangeScheduleOut objLotChangeScheduleOut = new Outputs.ObjLotChangeScheduleOut();
            objLotChangeScheduleOut.setOpehsMoveFlag(false);
            Infos.ReScheduledLotAttributes strRescheduledLotAttributes = new Infos.ReScheduledLotAttributes();
            strRescheduledLotAttributes.setLotID(lotID);
            strRescheduledLotAttributes.setProductID(lotReQueueAttributes.getProductID());
            strRescheduledLotAttributes.setRouteID(lotReQueueAttributes.getRouteID());
            strRescheduledLotAttributes.setCurrentOperationNumber(lotReQueueAttributes.getCurrentOperationNumber());
            strRescheduledLotAttributes.setOriginalRouteID(lotReQueueAttributes.getRouteID().getValue());
            strRescheduledLotAttributes.setOriginalOperationNumber(lotReQueueAttributes.getCurrentOperationNumber());
            strRescheduledLotAttributes.setSubLotType("");
            objLotChangeScheduleOut = lotMethod.lotChangeSchedule(objCommon, strRescheduledLotAttributes);

            //Delete all qtime restrictions for previous route
            if (objLotChangeScheduleOut.getOpehsMoveFlag()){
                log.info("strLot_ChangeSchedule_out.opehsMoveFlag == TRUE");
                String keepQTime = StandardProperties.OM_QT_KEEP_ON_LOT_PLAN_CHG.getValue();
                if (CimStringUtils.equals(keepQTime,BizConstant.SP_FUNCTION_AVAILABLE_TRUE)){
                    //Not clear Q-Time information
                    log.info("OM_QT_KEEP_ON_LOT_PLAN_CHG=1");
                    log.info("Not clear Q-Time info");
                }else {
                    log.info("OM_QT_KEEP_ON_LOT_PLAN_CHG=0");
                    log.info("Clear Q-Time info");
                    Outputs.ObjQtimeAllClearByRouteChangeOut objQtimeAllClearByRouteChangeOut = qTimeMethod.qtimeAllClearByRouteChange(objCommon, lotID);
                    // Reset Q-Time actions
                    ObjectIdentifier resetReasonCodeID = new ObjectIdentifier(BizConstant.SP_REASON_QTIMECLEAR);
                    //----- Lot Hold Actions -------//
                    if (!CimObjectUtils.isEmpty(objQtimeAllClearByRouteChangeOut.getStrLotHoldReleaseList())){
                        log.info("The lot hold actions to reset was found.");
                        Params.HoldLotReleaseReqParams holdLotReleaseReqParams = new Params.HoldLotReleaseReqParams();
                        holdLotReleaseReqParams.setUser(objCommon.getUser());
                        holdLotReleaseReqParams.setLotID(lotID);
                        holdLotReleaseReqParams.setReleaseReasonCodeID(resetReasonCodeID);
                        holdLotReleaseReqParams.setHoldReqList(objQtimeAllClearByRouteChangeOut.getStrLotHoldReleaseList());
                        lotService.sxHoldLotReleaseReq(objCommon, holdLotReleaseReqParams);
                    }

                    //Future Hold Actions
                    if (!CimObjectUtils.isEmpty(objQtimeAllClearByRouteChangeOut.getStrFutureHoldCancelList())){
                        log.info("The future hold actions to cancel was found.");
                        processControlService.sxFutureHoldCancelReq(objCommon, lotID, resetReasonCodeID,
                                SP_ENTRYTYPE_CANCEL, objQtimeAllClearByRouteChangeOut.getStrFutureHoldCancelList());

                    }

                    //Future Rework Actions
                    if (!CimObjectUtils.isEmpty(objQtimeAllClearByRouteChangeOut.getStrFutureReworkCancelList())){
                        log.info("The future rework actions to cancel was found.");
                        List<Infos.FutureReworkInfo> strFutureReworkCancelList = objQtimeAllClearByRouteChangeOut.getStrFutureReworkCancelList();
                        for (Infos.FutureReworkInfo futureReworkInfo : strFutureReworkCancelList){
                            processControlService.sxFutureReworkCancelReq(objCommon, futureReworkInfo.getLotID(),
                                    futureReworkInfo.getRouteID(), futureReworkInfo.getOperationNumber(),
                                    futureReworkInfo.getFutureReworkDetailInfoList(), "" );
                        }
                    }
                }
            }
            // UpDate RequiredCassetteCategory
            lotMethod.lotCassetteCategoryUpdateForContaminationControl(objCommon, lotID);
            //Make History
            if (objLotChangeScheduleOut.getOpehsAddFlag()){
                log.info("strLot_ChangeSchedule_out.opehsAddFlag == TRUE");
                eventMethod.lotOperationMoveEventMakeChangeRoute(objCommon, TransactionIDEnum.LOT_REQUEUE_REQ.getValue(),
                        lotID, objLotChangeScheduleOut.getOldCurrentPOData(), "");
            }
            Inputs.LotChangeEventMakeParams lotChangeEventMakeParams = new Inputs.LotChangeEventMakeParams();
            lotChangeEventMakeParams.setTransactionID(TransactionIDEnum.LOT_REQUEUE_REQ.getValue());
            lotChangeEventMakeParams.setLotID(lotID.getValue());
            lotChangeEventMakeParams.setExternalPriority("0");
            lotChangeEventMakeParams.setLotOwnerID("");
            lotChangeEventMakeParams.setOrderNumber("");
            lotChangeEventMakeParams.setCustomerCodeID("");
            lotChangeEventMakeParams.setLotComment("");
            lotChangeEventMakeParams.setPriorityClass("0");
            lotChangeEventMakeParams.setProductID(lotReQueueAttributes.getProductID().getValue());
            lotChangeEventMakeParams.setPreviousProductID((ObjectIdentifier.isEmpty(objLotChangeScheduleOut.getPreviousProductID())) ? null :objLotChangeScheduleOut.getPreviousProductID().getValue());
            lotChangeEventMakeParams.setPlanStartTime(strRescheduledLotAttributes.getPlannedStartTime());
            lotChangeEventMakeParams.setPlanCompTime(strRescheduledLotAttributes.getPlannedFinishTime());
            lotChangeEventMakeParams.setClaimMemo("");
            eventMethod.lotChangeEventMake(objCommon, lotChangeEventMakeParams);
            Inputs.LotReticleSetChangeEventMakeParams lotReticleSetChangeEventMakeParams = new Inputs.LotReticleSetChangeEventMakeParams();
            lotReticleSetChangeEventMakeParams.setTransactionID(TransactionIDEnum.LOT_REQUEUE_REQ.getValue());
            lotReticleSetChangeEventMakeParams.setLotID(lotID);
            lotReticleSetChangeEventMakeParams.setClaimMemo("");
            eventMethod.lotReticleSetChangeEventMake(objCommon, lotReticleSetChangeEventMakeParams);
        }

        //AMHS I/F for Priority Change Request
        log.info("Start AMHS I/F");
        Infos.PriorityChangeReq priorityChangeReq = new Infos.PriorityChangeReq();
        List<Infos.PriorityInfo> priorityInfoList = new ArrayList<>();
        ObjectIdentifier aLotID ;
        ObjectIdentifier aCassetteID ;

        for (Infos.LotReQueueAttributes lotReQueueAttributes : lotReQueueAttributesList){
            //Get cassette / lot connection
            aLotID = lotReQueueAttributes.getLotID();
            try {
                aCassetteID = lotMethod.lotCassetteGet(objCommon, aLotID);
                //Check the representative lotID
                Outputs.ObjCassetteLotListGetWithPriorityOrderOut objCassetteLotListGetWithPriorityOrderOut = cassetteMethod.cassetteLotListGetWithPriorityOrder(objCommon, aCassetteID);
                if (!CimObjectUtils.isEmpty(objCassetteLotListGetWithPriorityOrderOut)){
                    ObjectIdentifier tempLotId = objCassetteLotListGetWithPriorityOrderOut.getLotStatusInfos().get(0).getLotID();
                    log.info("cassette_lotList_GetWithPriorityOrder.lotID ", objCassetteLotListGetWithPriorityOrderOut.getLotStatusInfos().get(0).getLotID());
                    if (CimObjectUtils.equals(aLotID,tempLotId)){
                        Infos.PriorityInfo priorityInfo =  new Infos.PriorityInfo();
                        priorityInfo.setPriority("");
                        priorityInfo.setCarrierID(aCassetteID);
                        priorityInfoList.add(priorityInfo);
                        priorityChangeReq.setPriorityInfoData(priorityInfoList);
                    }
                }
            } catch (ServiceException e) {
                if (!Validations.isEquals(retCodeConfig.getNotFoundCst(), e.getCode())) {
                    throw e;
                }
            }
        }

        if (!CimObjectUtils.isEmpty(priorityInfoList)) {
            //TMSMgr_SendPriorityChangeReq
            try {
                tmsService.priorityChangeReq(objCommon, objCommon.getUser(), priorityChangeReq);
            } catch (Exception ex) {

            }
        }

        for (Infos.LotReQueueAttributes lotReQueueAttributes : lotReQueueAttributesList){
            Infos.LotReQueueReturn lotReQueueReturn = new Infos.LotReQueueReturn();
            lotReQueueReturn.setLotID(lotReQueueAttributes.getLotID());
            lotReQueueReturns.add(lotReQueueReturn);
        }

        return lotCurrentQueueReactivateReqResult;
    }

    @Override
    public List<Infos.ChangeLotScheduleReturn> sxLotScheduleChangeReq(Infos.ObjCommon objCommon, Params.LotScheduleChangeReqParams params) {
        List<Infos.ChangeLotScheduleReturn> ChangeLotScheduleReturn = new ArrayList<>();

        boolean errorFlag = false;
        boolean schChgRsvFlag = false;
        List<Infos.ReScheduledLotAttributes> reScheduledLotAttributesList = params.getReScheduledLotAttributesList();
        String lotOperationEIcheck = StandardProperties.OM_CARRIER_CHK_EI_FOR_LOT_OPERATION.getValue();
        List<ObjectIdentifier> userGroupIDs = new ArrayList<>();
        for (Infos.ReScheduledLotAttributes reScheduledLotAttributes : reScheduledLotAttributesList) {
            /*--------------------------------*/
            /*   Lock objects to be updated   */
            /*--------------------------------*/
            objectLockMethod.objectLock(objCommon, CimLot.class, reScheduledLotAttributes.getLotID());


            //------------------------------------
            // Check LOCK Hold.
            //------------------------------------
            List<ObjectIdentifier> lotIDs = new ArrayList<>();
            lotIDs.add(reScheduledLotAttributes.getLotID());
            lotMethod.lotCheckLockHoldConditionForOperation(objCommon, lotIDs);

            //----------------------------------
            //  Get InPostProcessFlag of lot
            //----------------------------------
            //lot_inPostProcessFlag_Get
            Outputs.ObjLotInPostProcessFlagOut lotInPostProcessFlagOut = lotMethod.lotInPostProcessFlagGet(objCommon, reScheduledLotAttributes.getLotID());
            if (CimBooleanUtils.isTrue(lotInPostProcessFlagOut.getInPostProcessFlagOfLot())) {
                log.info("lot is in post process");
                if (CimArrayUtils.isEmpty(userGroupIDs)) {
                    userGroupIDs = personMethod.personUserGroupListGetDR(objCommon, objCommon.getUser().getUserID());
                }

                int i = 0;
                for (i = 0; i < CimArrayUtils.getSize(userGroupIDs); i++) {

                }
                if (i == CimArrayUtils.getSize(userGroupIDs)) {
                    log.info("Not External Post Process User!");
                    throw new ServiceException(new OmCode(retCodeConfig.getLotInPostProcess(), ObjectIdentifier.fetchValue(reScheduledLotAttributes.getLotID())));
                }
            }

            //---------------------------------------
            //   Check interFabXferPlan existence
            //---------------------------------------
            //process_CheckInterFabXferPlanSkip
            if (ObjectIdentifier.equalsWithValue(reScheduledLotAttributes.getOriginalRouteID(), reScheduledLotAttributes.getRouteID())) {
                Inputs.ObjProcessCheckInterFabXferPlanSkipIn objProcessCheckInterFabXferPlanSkipIn = new Inputs.ObjProcessCheckInterFabXferPlanSkipIn();
                objProcessCheckInterFabXferPlanSkipIn.setLotID(reScheduledLotAttributes.getLotID());
                objProcessCheckInterFabXferPlanSkipIn.setCurrentRouteID(new ObjectIdentifier(reScheduledLotAttributes.getOriginalRouteID()));
                objProcessCheckInterFabXferPlanSkipIn.setCurrentOpeNo(reScheduledLotAttributes.getOriginalOperationNumber());
                objProcessCheckInterFabXferPlanSkipIn.setJumpingRouteID(reScheduledLotAttributes.getRouteID());
                objProcessCheckInterFabXferPlanSkipIn.setJumpingOpeNo(reScheduledLotAttributes.getCurrentOperationNumber());
                processMethod.processCheckInterFabXferPlanSkip(objCommon, objProcessCheckInterFabXferPlanSkipIn);
            }

            ObjectIdentifier lotID = reScheduledLotAttributes.getLotID();
            ObjectIdentifier productID = reScheduledLotAttributes.getProductID();
            ObjectIdentifier routeID = reScheduledLotAttributes.getRouteID();
            String currentOperationNumber = reScheduledLotAttributes.getCurrentOperationNumber();
            String subLotType = reScheduledLotAttributes.getSubLotType();
            String shedulingMode = reScheduledLotAttributes.getShedulingMode();
            String priorityClass = reScheduledLotAttributes.getPriorityClass();
            String plannedStartTime = reScheduledLotAttributes.getPlannedStartTime();
            String plannedFinishTime = reScheduledLotAttributes.getPlannedFinishTime();
            List<Infos.ChangedLotSchedule> changedLotScheduleList = reScheduledLotAttributes.getChangedLotScheduleList();

            ObjectIdentifier cassetteID = lotMethod.lotCassetteGet(objCommon, lotID);
            ObjectIdentifier controlJobID = lotMethod.lotControlJobIDGet(objCommon, lotID);
            boolean bOpeUpdateFlag = false;
            boolean bSchUpdateFlag = false;
            if ((!ObjectIdentifier.isEmpty(productID) && !ObjectIdentifier.isEmpty(routeID) && !CimObjectUtils.isEmpty(currentOperationNumber)) || !CimObjectUtils.isEmpty(subLotType)) {
                bOpeUpdateFlag = true;
            }
            if (!CimObjectUtils.isEmpty(shedulingMode) || !CimObjectUtils.isEmpty(priorityClass) || !CimObjectUtils.isEmpty(plannedStartTime)
                    || !CimObjectUtils.isEmpty(plannedFinishTime) || !CimObjectUtils.isEmpty(changedLotScheduleList)) {
                bSchUpdateFlag = true;
            }

            //------------------------------
            // lot Control Job ID Check
            //------------------------------
            if (!ObjectIdentifier.isEmpty(controlJobID)) {
                if (bSchUpdateFlag) {
                    /*------------------------------------------------------------------------*/
                    /*   Check Bonding Group                                                  */
                    /*------------------------------------------------------------------------*/
                    //lot_bondingGroupID_GetDR
                    String lotBondingGroupID = lotMethod.lotBondingGroupIDGetDR(objCommon, lotID);
                    Validations.check(CimStringUtils.isNotEmpty(lotBondingGroupID), new OmCode(retCodeConfig.getLotHasBondingGroup(), ObjectIdentifier.fetchValue(lotID), lotBondingGroupID));

                    /*------------------------------------------------------------------------*/
                    /*   Register Product Request                                             */
                    /*------------------------------------------------------------------------*/
                    Infos.ReScheduledLotAttributes lotAttributes = reScheduledLotAttributes.clone();
                    lotAttributes.setProductID(null);
                    lotAttributes.setOriginalRouteID(null);
                    lotAttributes.setRouteID(null);
                    lotAttributes.setOriginalOperationNumber(null);
                    lotAttributes.setCurrentOperationNumber(null);
                    lotAttributes.setSubLotType(null);
                    Outputs.ObjLotChangeScheduleOut lotChangeScheduleOut = lotMethod.lotChangeSchedule(objCommon, lotAttributes);

                    /*------------------------------------------*/
                    /*   AMHS I/F for Priority Change Request   */
                    /*------------------------------------------*/
                    if (CimStringUtils.isNotEmpty(priorityClass)) {
                        Infos.PriorityChangeReq priorityChangeReq = new Infos.PriorityChangeReq();

                        Outputs.ObjCassetteLotListGetWithPriorityOrderOut cassetteLotListGetWithPriorityOrderOut = cassetteMethod.cassetteLotListGetWithPriorityOrder(objCommon, cassetteID);
                        if (CimArrayUtils.isNotEmpty(cassetteLotListGetWithPriorityOrderOut.getLotStatusInfos())) {
                            if (ObjectIdentifier.equalsWithValue(lotID, cassetteLotListGetWithPriorityOrderOut.getLotStatusInfos().get(0).getLotID())) {
                                List<Infos.PriorityInfo> priorityInfoList = new ArrayList<>();
                                Infos.PriorityInfo priorityInfo =  new Infos.PriorityInfo();
                                priorityInfo.setPriority(priorityClass);
                                priorityInfo.setCarrierID(cassetteID);
                                priorityInfoList.add(priorityInfo);
                                priorityChangeReq.setPriorityInfoData(priorityInfoList);
                            }
                        }

                        //XMSMgr_SendPriorityChangeReq
                        if (CimArrayUtils.isNotEmpty(priorityChangeReq.getPriorityInfoData())) {
                            try {
                                tmsService.priorityChangeReq(objCommon, objCommon.getUser(), priorityChangeReq);
                            } catch (Exception ex) {

                            }
                        }
                    }

                    /*------------------------------------------------------------------------*/
                    /*   Make History                                                         */
                    /*------------------------------------------------------------------------*/
                    //lotChangeEvent_Make
                    Inputs.LotChangeEventMakeParams lotChangeEventMakeParams = new Inputs.LotChangeEventMakeParams();
                    lotChangeEventMakeParams.setTransactionID(TransactionIDEnum.LOT_SCHEDULE_CHANGE_REQ.getValue());
                    lotChangeEventMakeParams.setLotID(reScheduledLotAttributes.getLotID().getValue());
                    lotChangeEventMakeParams.setExternalPriority("0");
                    lotChangeEventMakeParams.setLotOwnerID("");
                    lotChangeEventMakeParams.setOrderNumber("");
                    lotChangeEventMakeParams.setCustomerCodeID("");
                    lotChangeEventMakeParams.setLotComment("");
                    lotChangeEventMakeParams.setPriorityClass(reScheduledLotAttributes.getPriorityClass());
                    lotChangeEventMakeParams.setProductID(reScheduledLotAttributes.getProductID().getValue());
                    lotChangeEventMakeParams.setPreviousProductID(ObjectIdentifier.fetchValue(lotChangeScheduleOut.getPreviousProductID()));
                    lotChangeEventMakeParams.setPlanStartTime(reScheduledLotAttributes.getPlannedStartTime());
                    lotChangeEventMakeParams.setPlanCompTime(reScheduledLotAttributes.getPlannedFinishTime());
                    lotChangeEventMakeParams.setClaimMemo(params.getClaimMemo());
                    eventMethod.lotChangeEventMake(objCommon, lotChangeEventMakeParams);


                    /*------------------------------------------------------------------------*/
                    /*   Flag of some data update ON                                          */
                    /*------------------------------------------------------------------------*/
                    errorFlag = true;
                }
                if (bOpeUpdateFlag) {
                    String lotShdchange = StandardProperties.OM_LOT_PLAN_CHG_RESERVE.getValue();
                    if (CimStringUtils.equals(lotShdchange, BizConstant.ENV_ENABLE)) {
                        /*----------------------------*/
                        /*   Future Action Data Set   */
                        /*----------------------------*/
                        Outputs.ObjLotCurrentOperationInfoGetOut lotCurrentOperationInfoOut = lotMethod.lotCurrentOperationInfoGet(objCommon, lotID);

                        ObjectIdentifier targetRouteID = lotCurrentOperationInfoOut.getRouteID();
                        String targetOperationNumber = lotCurrentOperationInfoOut.getOperationNumber();

                        /*-----------------------------*/
                        /*   Future Action Procedure   */
                        /*-----------------------------*/
                        Infos.SchdlChangeReservation scheduleChangeReservation = new Infos.SchdlChangeReservation();
                        scheduleChangeReservation.setObjectID(lotID);
                        scheduleChangeReservation.setObjectType(BizConstant.SP_SCHDL_CHG_OBJTYPE_LOT);
                        scheduleChangeReservation.setTargetRouteID(targetRouteID);
                        scheduleChangeReservation.setTargetOperationNumber(targetOperationNumber);
                        scheduleChangeReservation.setProductID(productID);
                        scheduleChangeReservation.setRouteID(routeID);
                        scheduleChangeReservation.setOperationNumber(currentOperationNumber);
                        scheduleChangeReservation.setSubLotType(subLotType);
                        String currentTime = String.valueOf(objCommon.getTimeStamp().getReportTimeStamp());
                        String[] currnetTimeArray = currentTime.split(" ");
                        scheduleChangeReservation.setStartDate(currnetTimeArray[0]);
                        scheduleChangeReservation.setStartTime(currnetTimeArray[1]);
                        scheduleChangeReservation.setEndDate("9999-12-31");
                        scheduleChangeReservation.setEndTime("23:59:59");
                        scheduleChangeReservation.setEraseAfterUsedFlag(true);
                        scheduleChangeReservation.setMaxLotCnt(1L);
                        scheduleChangeReservation.setLotInfoChangeFlag(true);
                        //schdlChangeReservation_CreateDR__110
                        scheduleChangeReservationMethod.scheduleChangeReservationCreateDR(objCommon, scheduleChangeReservation);
                        errorFlag = true;
                        schChgRsvFlag = true;
                        continue;
                    } else if (CimStringUtils.equals("0", StandardProperties.OM_LOT_PLAN_CHG_RESERVE.getValue())) {
                        throw new ServiceException(new OmCode(retCodeConfig.getLotControlJobidFilled(), ObjectIdentifier.fetchValue(lotID), ObjectIdentifier.fetchValue(controlJobID)));
                    }
                }
                continue;
            }
            if (bOpeUpdateFlag) {
                if (CimStringUtils.equals(BizConstant.ENV_ENABLE, lotOperationEIcheck)) {
                    /*----------------------*/
                    /*   check XferStetus   */
                    /*----------------------*/
                    String cassetteTransferStateGetOut = cassetteMethod.cassetteTransferStateGet(objCommon, cassetteID);
                    String strCassetteXferState = cassetteTransferStateGetOut;
                    if (CimStringUtils.equals(BizConstant.SP_TRANSSTATE_EQUIPMENTIN, strCassetteXferState)) {
                        throw new ServiceException(new OmCode(retCodeConfig.getInvalidCassetteTransferState(),
                                strCassetteXferState, ObjectIdentifier.fetchValue(cassetteID)));
                    }
                }
            }
        }
        if (errorFlag) {
            if (!schChgRsvFlag) {
                throw new ServiceException(retCodeConfig.getInprocessLotinfoUpdate());
            } else {
                throw new ServiceException(retCodeConfig.getSomeLotidDataError());
            }
        }
        for (Infos.ReScheduledLotAttributes reScheduledLotAttributes : reScheduledLotAttributesList) {
            boolean bOpeUpdateFlag = false;
            ObjectIdentifier productID = reScheduledLotAttributes.getProductID();
            ObjectIdentifier routeID = reScheduledLotAttributes.getRouteID();
            String currentOperationNumber = reScheduledLotAttributes.getCurrentOperationNumber();
            ObjectIdentifier lotID = reScheduledLotAttributes.getLotID();

            if (!ObjectIdentifier.isEmptyWithValue(productID) && !ObjectIdentifier.isEmptyWithValue(routeID) && !CimStringUtils.isEmpty(currentOperationNumber)) {
                /*------------------------------------------------------------------------*/
                /*   Check lot Process State                                              */
                /*------------------------------------------------------------------------*/
                String lotProcessState = lotMethod.lotProcessStateGet(objCommon, lotID);
                if (!CimStringUtils.equals(BizConstant.SP_LOT_PROCSTATE_WAITING, lotProcessState)) {
                    throw new ServiceException(new OmCode(retCodeConfig.getInvalidLotProcessState(), ObjectIdentifier.fetchValue(lotID), lotProcessState));
                }
            }

            /*------------------------------------------------------------------------*/
            /*   Check lot Finished State                                             */
            /*------------------------------------------------------------------------*/
            String lotFinshedState = lotMethod.lotFinishedStateGet(objCommon, lotID);
            if (CimStringUtils.equals(BizConstant.SP_LOT_FINISHED_STATE_STACKED, lotFinshedState)) {
                throw new ServiceException(new OmCode(retCodeConfig.getInvalidLotFinishStat(), lotFinshedState));
            }

            /*------------------------------------------------------------------------*/
            /*   Check Bonding Group                                                  */
            /*------------------------------------------------------------------------*/
            //lot_bondingGroupID_GetDR
            String lotBondingGroupID = lotMethod.lotBondingGroupIDGetDR(objCommon, lotID);
            if (CimStringUtils.isNotEmpty(lotBondingGroupID)) {
                throw new ServiceException(new OmCode(retCodeConfig.getLotHasBondingGroup(), ObjectIdentifier.fetchValue(lotID), lotBondingGroupID));
            }

            /*------------------------------------------------------------------------*/
            /*   Check lot Inventory State                                            */
            /*------------------------------------------------------------------------*/
            if (!ObjectIdentifier.isEmptyWithValue(routeID) && !CimStringUtils.isEmpty(currentOperationNumber)) {
                String lotInventoryState = lotMethod.lotInventoryStateGet(objCommon, lotID);
                if (!CimStringUtils.equals(BizConstant.SP_LOT_INVENTORYSTATE_ONFLOOR, lotInventoryState)
                        && !CimStringUtils.equals(BizConstant.SP_LOT_INVENTORYSTATE_NONPROBANK, lotInventoryState)) {
                    Validations.check(retCodeConfig.getInvalidLotInventoryStat(), ObjectIdentifier.fetchValue(lotID), lotInventoryState);
                }
            }

            /*------------------------------------------------------------------------*/
            /*   Check lot's backup State                                             */
            /*------------------------------------------------------------------------*/
            Infos.LotBackupInfo lotBackupInfoOut = lotMethod.lotBackupInfoGet(objCommon, lotID);
            if (CimBooleanUtils.isTrue(lotBackupInfoOut.getBackupProcessingFlag())) {
                if (!ObjectIdentifier.isEmptyWithValue(productID)) {
                    throw new ServiceException(new OmCode(retCodeConfig.getLotBackupOnBackupsite(), ObjectIdentifier.fetchValue(lotID)));
                }
                ObjectIdentifier lotCurrentRouteIDOut = lotMethod.lotCurrentRouteIDGet(objCommon, lotID);
                if (!ObjectIdentifier.equalsWithValue(lotCurrentRouteIDOut, routeID)) {
                    throw new ServiceException(new OmCode(retCodeConfig.getLotBackupOnBackupsite(), ObjectIdentifier.fetchValue(lotID)));
                }
            }

            /*------------------------------------------------------------------------*/
            /*   Check specified Product validity                                     */
            /*------------------------------------------------------------------------*/
            if (!ObjectIdentifier.isEmpty(productID)) {
                Outputs.ObjLotProductIDGetOut lotProductIDOut = lotMethod.lotProductIDGet(objCommon, lotID);

                /*-----------------------------------------------------------------------------------*/
                /*   If lots product and specified product are different, then check product status  */
                /*-----------------------------------------------------------------------------------*/
                if (!ObjectIdentifier.equalsWithValue(lotProductIDOut.getProductID(), productID)) {
                    /*------------------------------------------------------------------------------------------------------*/
                    /*   In this method, the existence of specified Product and validity of Production State are checked.   */
                    /*   Following statuses are rejected.                                                                   */
                    /*    - SP_ProductSpecification_State_Obsolete                                                          */
                    /*    - SP_ProductSpecification_State_Draft                                                             */
                    /*------------------------------------------------------------------------------------------------------*/
                    productMethod.productExistenceCheck(objCommon, productID);
                }
            }

            /*------------------------------------------------------------------------*/
            /*   Register Product Request                                             */
            /*------------------------------------------------------------------------*/
            Outputs.ObjLotChangeScheduleOut lotChangeScheduleOut = lotMethod.lotChangeSchedule(objCommon, reScheduledLotAttributes);

            Boolean opehsMoveFlag = lotChangeScheduleOut.getOpehsMoveFlag();

            /*------------------------------------------------------------------------*/
            /*   Delete all qtime restrictions for previous route                     */
            /*------------------------------------------------------------------------*/
            if (CimBooleanUtils.isTrue(lotChangeScheduleOut.getOpehsMoveFlag())) {
                boolean bSameRouteFlag = false;
                if (ObjectIdentifier.equalsWithValue(routeID, reScheduledLotAttributes.getOriginalRouteID())) {
                    bSameRouteFlag = true;
                }
                String keepQTime = StandardProperties.OM_QT_KEEP_ON_LOT_PLAN_CHG.getValue();
                if (bSameRouteFlag && (CimStringUtils.equals(BizConstant.SP_FUNCTION_AVAILABLE_TRUE, keepQTime))) {

                } else {
                    Outputs.ObjQtimeAllClearByRouteChangeOut qtimeChangeOut = qTimeMethod.qtimeAllClearByRouteChange(objCommon, lotID);
                    Outputs.ObjQtimeAllClearByRouteChangeOut qtimeAllClearByRouteChangeOut = qtimeChangeOut;
                    //--------------------------------------------------
                    // Reset Q-Time actions
                    //--------------------------------------------------
                    if (!CimObjectUtils.isEmpty(qtimeAllClearByRouteChangeOut)) {
                        ObjectIdentifier resetReasonCodeID = new ObjectIdentifier(BizConstant.SP_REASON_QTIMECLEAR);
                        //----- lot Hold Actions -------//
                        List<Infos.LotHoldReq> strLotHoldReleaseList = qtimeAllClearByRouteChangeOut.getStrLotHoldReleaseList();
                        if (!CimObjectUtils.isEmpty(strLotHoldReleaseList)) {
                            Params.HoldLotReleaseReqParams holdLotReleaseReqParams = new Params.HoldLotReleaseReqParams();
                            holdLotReleaseReqParams.setLotID(lotID);
                            holdLotReleaseReqParams.setReleaseReasonCodeID(resetReasonCodeID);
                            holdLotReleaseReqParams.setHoldReqList(qtimeChangeOut.getStrLotHoldReleaseList());
                            lotService.sxHoldLotReleaseReq(objCommon, holdLotReleaseReqParams);
                        }

                        //----- Future Hold Actions -------//
                        List<Infos.LotHoldReq> strFutureHoldCancelList = qtimeAllClearByRouteChangeOut.getStrFutureHoldCancelList();
                        if (!CimObjectUtils.isEmpty(strFutureHoldCancelList)) {
                            processControlService.sxFutureHoldCancelReq(objCommon, lotID, resetReasonCodeID, BizConstant.SP_ENTRYTYPE_CANCEL, strFutureHoldCancelList);
                        }

                        //----- Future Rework Actions -------//
                        List<Infos.FutureReworkInfo> futureReworkInfos = qtimeAllClearByRouteChangeOut.getStrFutureReworkCancelList();
                        if (!CimObjectUtils.isEmpty(futureReworkInfos)) {
                            for (Infos.FutureReworkInfo futureReworkInfo : futureReworkInfos) {
                                processControlService.sxFutureReworkCancelReq(objCommon, futureReworkInfo.getLotID(), futureReworkInfo.getRouteID()
                                        ,futureReworkInfo.getOperationNumber(), futureReworkInfo.getFutureReworkDetailInfoList(), "");
                            }
                        }
                    }
                }
            }
            //----------------------------------------------------
            // Future Hold
            //----------------------------------------------------
            if (CimBooleanUtils.isTrue(opehsMoveFlag)
                    && ObjectIdentifier.equalsWithValue(routeID, reScheduledLotAttributes.getOriginalRouteID())
                    || CimStringUtils.equals(currentOperationNumber, reScheduledLotAttributes.getOriginalOperationNumber())) {

                Outputs.ObjLotFutureHoldRequestsEffectByConditionOut lotFutureHoldRequestsEffectByConditionOut
                        = lotMethod.lotFutureHoldRequestsEffectByCondition(objCommon, lotID, new Infos.EffectCondition(BizConstant.SP_FUTUREHOLD_PRE, BizConstant.SP_FUTUREHOLD_ALL));

                List<Infos.LotHoldReq> strLotHoldReqList = lotFutureHoldRequestsEffectByConditionOut.getStrLotHoldReqList();
                if (!CimObjectUtils.isEmpty(strLotHoldReqList)) {
                    lotService.sxHoldLotReq(objCommon, lotID, strLotHoldReqList);
                }

                Outputs.ObjLotFutureHoldRequestsDeleteEffectedByConditionOut lotFutureHoldRequestsDeleteEffectedByConditionOut
                        = lotMethod.lotFutureHoldRequestsDeleteEffectedByCondition(objCommon, lotID, new Infos.EffectCondition(BizConstant.SP_FUTUREHOLD_PRE
                        , BizConstant.SP_FUTUREHOLD_SINGLE));

                List<Infos.LotHoldReq> strFutureHoldReleaseReqList = lotFutureHoldRequestsDeleteEffectedByConditionOut.getStrFutureHoldReleaseReqList();
                if (!CimObjectUtils.isEmpty(strFutureHoldReleaseReqList)) {
                    processControlService.sxFutureHoldCancelReq(objCommon, lotID, null, BizConstant.SP_ENTRYTYPE_REMOVE, strFutureHoldReleaseReqList);
                }
            }
            //--------------------------------------------------------------------------------------------------
            // UpDate RequiredCassetteCategory
            //--------------------------------------------------------------------------------------------------
            lotMethod.lotCassetteCategoryUpdateForContaminationControl(objCommon, lotID);

            //-----------------------//
            //     Process Hold      //
            //-----------------------//
            if (CimBooleanUtils.isTrue(opehsMoveFlag)) {
                for (Infos.ReScheduledLotAttributes attributes : reScheduledLotAttributesList) {
                    if (!ObjectIdentifier.isEmpty(attributes.getRouteID()) && !CimObjectUtils.isEmpty(attributes.getCurrentOperationNumber())) {
                        processControlService.sxProcessHoldDoActionReq(objCommon, attributes.getLotID(), params.getClaimMemo());
                    }
                }
            }

            /*------------------------------------------------------------------------*/
            /*   Make History                                                         */
            /*------------------------------------------------------------------------*/
            //lotOperationMoveEvent_MakeChangeRoute/lotChangeEvent_Make/lotReticleSetChangeEvent_Make
            if (lotChangeScheduleOut.getOpehsAddFlag()){
                eventMethod.lotOperationMoveEventMakeChangeRoute(objCommon, TransactionIDEnum.LOT_SCHEDULE_CHANGE_REQ.getValue(),
                        reScheduledLotAttributes.getLotID(), lotChangeScheduleOut.getOldCurrentPOData(), params.getClaimMemo());
            }
            Inputs.LotChangeEventMakeParams lotChangeEventMakeParams = new Inputs.LotChangeEventMakeParams();
            lotChangeEventMakeParams.setTransactionID(TransactionIDEnum.LOT_SCHEDULE_CHANGE_REQ.getValue());
            lotChangeEventMakeParams.setLotID(reScheduledLotAttributes.getLotID().getValue());
            lotChangeEventMakeParams.setExternalPriority("0");
            lotChangeEventMakeParams.setLotOwnerID("");
            lotChangeEventMakeParams.setOrderNumber("");
            lotChangeEventMakeParams.setCustomerCodeID("");
            lotChangeEventMakeParams.setLotComment("");
            lotChangeEventMakeParams.setPriorityClass(reScheduledLotAttributes.getPriorityClass());
            lotChangeEventMakeParams.setProductID(reScheduledLotAttributes.getProductID().getValue());
            lotChangeEventMakeParams.setPreviousProductID(ObjectIdentifier.fetchValue(lotChangeScheduleOut.getPreviousProductID()));
            lotChangeEventMakeParams.setPlanStartTime(reScheduledLotAttributes.getPlannedStartTime());
            lotChangeEventMakeParams.setPlanCompTime(reScheduledLotAttributes.getPlannedFinishTime());
            lotChangeEventMakeParams.setClaimMemo(params.getClaimMemo());
            eventMethod.lotChangeEventMake(objCommon, lotChangeEventMakeParams);
            Inputs.LotReticleSetChangeEventMakeParams lotReticleSetChangeEventMakeParams = new Inputs.LotReticleSetChangeEventMakeParams();
            lotReticleSetChangeEventMakeParams.setTransactionID(TransactionIDEnum.LOT_SCHEDULE_CHANGE_REQ.getValue());
            lotReticleSetChangeEventMakeParams.setLotID(reScheduledLotAttributes.getLotID());
            lotReticleSetChangeEventMakeParams.setClaimMemo(params.getClaimMemo());
            eventMethod.lotReticleSetChangeEventMake(objCommon, lotReticleSetChangeEventMakeParams);
        }

        /*------------------------------------------*/
        /*   AMHS I/F for Priority Change Request   */
        /*------------------------------------------*/
        //TODO-NOTIMPL: AMHS I/F for Priority Change Request
        ObjectIdentifier aLotID = null;
        ObjectIdentifier aCassetteID = null;
        Infos.PriorityChangeReq priorityChangeReq = new Infos.PriorityChangeReq();
        List<Infos.PriorityInfo> priorityInfoData = new ArrayList<>();
        priorityChangeReq.setPriorityInfoData(priorityInfoData);
        for (Infos.ReScheduledLotAttributes reScheduledLotAttributes : reScheduledLotAttributesList) {
            /*------------------------------------------------------------------------*/
            /*   Get cassette / lot connection                                        */
            /*------------------------------------------------------------------------*/
            aLotID = reScheduledLotAttributes.getLotID();
            try {
                aCassetteID = lotMethod.lotCassetteGet(objCommon, reScheduledLotAttributes.getLotID());
                /*----------------------------------------------*/
                /*   Check the representative lotID             */
                /*----------------------------------------------*/
                //cassette_lotList_GetWithPriorityOrder
                Outputs.ObjCassetteLotListGetWithPriorityOrderOut orderOut = cassetteMethod.cassetteLotListGetWithPriorityOrder(objCommon, aCassetteID);
                if (!CimObjectUtils.isEmpty(orderOut)) {
                    List<Infos.LotStatusInfo> lotStatusInfos = orderOut.getLotStatusInfos();
                    if (!CimObjectUtils.isEmpty(lotStatusInfos)) {
                        if (ObjectIdentifier.equalsWithValue(aLotID, lotStatusInfos.get(0).getLotID())) {
                            Infos.PriorityInfo priorityInfo = new Infos.PriorityInfo();
                            priorityInfoData.add(priorityInfo);
                            priorityInfo.setPriority(reScheduledLotAttributes.getPriorityClass());
                            priorityInfo.setCarrierID(aCassetteID);
                        }
                    }
                }
            } catch (ServiceException e) {
                log.info("lot cassette Get some error");
            }
        }
        //XMSMgr_SendPriorityChangeReq
        if (CimArrayUtils.isNotEmpty(priorityInfoData)) {
            try {
                tmsService.priorityChangeReq(objCommon, objCommon.getUser(), priorityChangeReq);
            } catch (Exception ex) {

            }
        }

        // contamination level whether on hold
        contaminationMethod.lotCheckContaminationLevelStepOut(objCommon, reScheduledLotAttributesList.parallelStream()
                .map(Infos.ReScheduledLotAttributes::getLotID).collect(Collectors.toList()));

        return ChangeLotScheduleReturn;
    }

    @Override
    public List<Infos.ReleaseCancelLotReturn> sxNewProdOrderCancelReq(Params.NewProdOrderCancelReqParams newProdOrderCancelReqParams, Infos.ObjCommon objCommon) {
        log.info("【Method Entry】sxNewProdOrderCancelReq");

        int lotIDsCount = CimArrayUtils.getSize(newProdOrderCancelReqParams.getLotIDs());
        Validations.check(0 == lotIDsCount, "Input Invalid LotIDs...");

        List<Infos.ReleaseCancelLotReturn> releaseCancelLotReturnList = new ArrayList<>();
        for (int i = 0; i < lotIDsCount; i++) {
            ObjectIdentifier lotID = newProdOrderCancelReqParams.getLotIDs().get(i);

            // Lock objects of product request;
            log.info("Lock objects of product request to be updated");
            try {
                objectLockMethod.objectLock(objCommon, CimProductRequest.class, lotID);
            } catch (ServiceException e){
                Infos.ReleaseCancelLotReturn releaseCancelLotReturn = new Infos.ReleaseCancelLotReturn();
                releaseCancelLotReturn.setLotID(lotID);
                releaseCancelLotReturn.setReturnCode(String.valueOf(e.getCode()));
                releaseCancelLotReturnList.add(releaseCancelLotReturn);
                e.setData(releaseCancelLotReturn);
                throw e;
            }

            //Make History;
            String productionState =null;
            try{
                productionState = productMethod.productRequestProductionStateGet(objCommon, lotID);
            }catch (ServiceException ex){
                log.info("txNewProdOrderCancelReq(): productRequestProductionStateGet method return an error.");

                Infos.ReleaseCancelLotReturn releaseCancelLotReturn = new Infos.ReleaseCancelLotReturn();
                releaseCancelLotReturn.setLotID(lotID);
                releaseCancelLotReturn.setReturnCode(String.valueOf(ex.getCode()));
                releaseCancelLotReturnList.add(releaseCancelLotReturn);
                throw new ServiceException(new OmCode(ex.getCode(),ex.getMessage()));
            }

            if (!CimStringUtils.equals(CIMStateConst.CIM_PRRQ_PROD_STATE_NOTINRELEASE, productionState)) {
                log.info("Invalid Product Request Production State : {}", productionState);

                Infos.ReleaseCancelLotReturn releaseCancelLotReturn = new Infos.ReleaseCancelLotReturn();
                releaseCancelLotReturn.setLotID(lotID);
                releaseCancelLotReturn.setReturnCode(String.valueOf(retCodeConfig.getInvalidProductStat().getCode()));
                releaseCancelLotReturnList.add(releaseCancelLotReturn);
                throw new ServiceException(retCodeConfig.getInvalidProductStat());
            }

            eventMethod.productRequestEventMakeReleaseCancel(objCommon, "OPLNW002", lotID, newProdOrderCancelReqParams.getClaimMemo());

            //Remove Product Request ;
            try{
                productMethod.productRequestReleaseCancel(objCommon, lotID);
            }catch (ServiceException ex){

                Infos.ReleaseCancelLotReturn releaseCancelLotReturn = new Infos.ReleaseCancelLotReturn();
                releaseCancelLotReturn.setReturnCode(String.valueOf(ex.getCode()));
                releaseCancelLotReturn.setLotID(lotID);
                releaseCancelLotReturnList.add(releaseCancelLotReturn);
                throw new ServiceException(new OmCode(ex.getCode(),ex.getMessage()));
            }
            //Return ;

            Infos.ReleaseCancelLotReturn releaseCancelLotReturn = new Infos.ReleaseCancelLotReturn();
            releaseCancelLotReturn.setLotID(lotID);
            releaseCancelLotReturn.setReturnCode(String.valueOf(retCodeConfig.getSucc().getCode()));
            releaseCancelLotReturnList.add(releaseCancelLotReturn);
        }

        log.info("【Method Exit】sxNewProdOrderCancelReq()");

        return releaseCancelLotReturnList;
    }

    @Override
    public Results.NewProdOrderCreateReqResult sxNewProdOrderCreateReq(Infos.ObjCommon objCommon, Infos.ReleaseLotAttributes releaseLotAttributes) {

        //【step1】check product request release.
        log.debug("【step1】check product request release.");
        productMethod.productRequestCheckForRelease(objCommon, releaseLotAttributes);

        //【step2】register product request
        log.debug("【step2】register product request");
        Outputs.ObjProductRequestReleaseOut requestReleaseOut = productMethod.productRequestRelease(objCommon, releaseLotAttributes);
        //【step3】make history
        log.debug("make history");
        Inputs.ProductRequestEventMakeReleaseParams productRequestEventMakeReleaseParams = new Inputs.ProductRequestEventMakeReleaseParams();
        productRequestEventMakeReleaseParams.setTransactionID(TransactionIDEnum.NEW_LOT_RELEASE_REQ.getValue());
        productRequestEventMakeReleaseParams.setReleaseLotAttributes(releaseLotAttributes);
        productRequestEventMakeReleaseParams.setClaimMemo("");
        eventMethod.productRequestEventMakeRelease(objCommon, productRequestEventMakeReleaseParams);

        Results.NewProdOrderCreateReqResult releaseReqResult = new Results.NewProdOrderCreateReqResult();
        List<Infos.ReleasedLotReturn> releasedLotReturnList = new ArrayList<>();
        Infos.ReleasedLotReturn releasedLotReturn = new Infos.ReleasedLotReturn();
        releasedLotReturn.setLotID(requestReleaseOut.getCreateProductRequest());
        releasedLotReturn.setReturnCode(0);
        releasedLotReturnList.add(releasedLotReturn);
        releaseReqResult.setReleasedLotReturnList(releasedLotReturnList);
        return releaseReqResult;
    }

    @Override
    public Infos.ReleasedLotReturn sxNewProdOrderModifyReq(Infos.ObjCommon objCommon, Infos.UpdateLotAttributes updateLotAttributes) {
        Infos.ReleasedLotReturn out = new Infos.ReleasedLotReturn();

        //【step1】check length of in-parameter
        log.debug("【step1】check length of in-parameter");
        if (null == updateLotAttributes) {
            throw new ServiceException(retCodeConfig.getInvalidParameter());
        }

        objectLockMethod.objectLock(objCommon, CimProductRequest.class, updateLotAttributes.getLotID());
        log.debug("【TODO】【TODO - NOTIMPL】【step2】lcock objects of product request.");

        //【step3】get updated product request information.
        Outputs.ObjProductRequestGetDetailOut productOrderInqResult = productMethod.productRequestGetDetail(objCommon, updateLotAttributes.getLotID());

        int scLen = CimArrayUtils.getSize(productOrderInqResult.getProdReqInq().getStrSourceLotsAttributes());
        if (0 < scLen) {
            for (int i = 0; i < scLen; i++) {
                objectLockMethod.objectLock(objCommon, CimLot.class, productOrderInqResult.getProdReqInq().getStrSourceLotsAttributes().get(i).getLotID());
            }
        }
        log.debug("【TODO】【TODO - NOTIMPL】【step4】lock objects to be updated.");

        Infos.ReleaseLotAttributes releaseLotAttributes = new Infos.ReleaseLotAttributes();
        releaseLotAttributes.setLotID(updateLotAttributes.getLotID());
        releaseLotAttributes.setProductID(updateLotAttributes.getProductID());
        releaseLotAttributes.setCustomerCode(updateLotAttributes.getCustomerCode());
        releaseLotAttributes.setManufacturingOrderNumber(updateLotAttributes.getManufacturingOrderNumber());
        releaseLotAttributes.setLotOwner(updateLotAttributes.getLotOwner());
        releaseLotAttributes.setLotType(productOrderInqResult.getProdReqInq().getLotType());
        releaseLotAttributes.setSubLotType(updateLotAttributes.getSubLotType());
        releaseLotAttributes.setRouteID(updateLotAttributes.getRouteID());
        releaseLotAttributes.setLotGenerationType(updateLotAttributes.getLotGenerationType());
        releaseLotAttributes.setSchedulingMode("");
        releaseLotAttributes.setProductDefinitionMode(updateLotAttributes.getProductDefinitionMode());
        releaseLotAttributes.setPriorityClass(updateLotAttributes.getPriorityClass());
        releaseLotAttributes.setExternalPriority(updateLotAttributes.getExternalPriority());
        releaseLotAttributes.setPlannedStartTime(updateLotAttributes.getPlannedStartTime());
        releaseLotAttributes.setPlannedFinishTime(updateLotAttributes.getPlannedFinishTime());
        releaseLotAttributes.setLotComment(updateLotAttributes.getLotComment());
        releaseLotAttributes.setProductQuantity(updateLotAttributes.getProductQuantity());
        releaseLotAttributes.setDirectedSourceLotList(updateLotAttributes.getDirectedSourceLotList());
        releaseLotAttributes.setReleaseLotScheduleList(updateLotAttributes.getUpdateLotScheduleList());
        productMethod.productRequestCheckForRelease(objCommon, releaseLotAttributes);

        //【step5】register product request
        log.debug("【step5】register product request");
        ObjectIdentifier updateRetCode = productMethod.productRequestUpdate(objCommon, updateLotAttributes);

        //【step6】make history
        log.debug("【step6】make history");
        Inputs.ProductRequestEventMakeUpdateParams params = new Inputs.ProductRequestEventMakeUpdateParams();
        params.setClaimMemo("");
        params.setUpdateLotAttributes(updateLotAttributes);
        params.setTransactionID(TransactionIDEnum.NEW_LOT_UPDATE_REQ.getValue());
        eventMethod.productRequestEventMakeUpdate(objCommon, params);

        out.setLotID(updateLotAttributes.getLotID());
        out.setReturnCode(0);
        return out;
    }

    @Override
    public void sxLotPlanChangeReserveCreateReq(Infos.ObjCommon objCommon, Infos.SchdlChangeReservation schdlChangeReservation, String claimMemo){
        log.info("Step1 - schdlChangeReservation_CheckForRegistration__110");
        scheduleChangeReservationMethod.schdlChangeReservationCheckForRegistration(objCommon, schdlChangeReservation);

        log.info("Step2 - schdlChangeReservation_CreateDR__110");
        scheduleChangeReservationMethod.scheduleChangeReservationCreateDR(objCommon, schdlChangeReservation);
    }

    @Override
    public void sxLotPlanChangeReserveModifyReq(Infos.ObjCommon objCommon, Infos.SchdlChangeReservation strCurrentSchdlChangeReservation, Infos.SchdlChangeReservation strNewSchdlChangeReservation, String claimMemo){
        log.info("Step1 - schdlChangeReservation_CheckForRegistration__110");
        scheduleChangeReservationMethod.schdlChangeReservationCheckForRegistration(objCommon, strNewSchdlChangeReservation);

        log.info("Step2 - schdlChangeReservation_ChangeDR");
        scheduleChangeReservationMethod.schdlChangeReservationChangeDR(objCommon, strCurrentSchdlChangeReservation, strNewSchdlChangeReservation);
    }

    @Override
    public List<Infos.SchdlChangeReservation> sxLotPlanChangeReserveCancelReq(Infos.ObjCommon objCommon, List<Infos.SchdlChangeReservation> strSchdlChangeReservations, String claimMemo){
        List<Infos.SchdlChangeReservation> schdlChangeReservationList = new ArrayList<>();
        int schdlLen = CimArrayUtils.getSize(strSchdlChangeReservations);
        for (int iCnt=0 ; iCnt<schdlLen; iCnt++){
            /*-------------------------------------------------------------*/
            /*   Send e-mail to lot owner if Schedule Change Reservation   */
            /*   is created in Lot Information Change                      */
            /*-------------------------------------------------------------*/
            Infos.SchdlChangeReservation schdlChangeReservation = strSchdlChangeReservations.get(iCnt);
            if (CimBooleanUtils.isTrue(schdlChangeReservation.getLotInfoChangeFlag())){
                Outputs.ObjLotCurrentOperationInfoGetOut objLotCurrentOperationInfoGetOut = lotMethod.lotCurrentOperationInfoGet(objCommon, schdlChangeReservation.getObjectID());
                StringBuffer messageSb = new StringBuffer();
                messageSb.append("This message was sent because reservation for \"Lot Information Change\" is canceled.\n")
                        .append("LotID      : ").append(schdlChangeReservation.getObjectID().getValue()).append("\n")
                        .append("Product ID : ").append(schdlChangeReservation.getProductID().getValue()).append("\n")
                        .append("Route ID   : ").append(schdlChangeReservation.getRouteID().getValue()).append("\n")
                        .append("Ope.No     : ").append(schdlChangeReservation.getOperationNumber()).append("\n");
                messageMethod.messageDistributionMgrPutMessage(objCommon, new ObjectIdentifier(BizConstant.SP_SYSTEMMSGCODE_SCRNOTICE), schdlChangeReservation.getObjectID(), "",
                        null, objLotCurrentOperationInfoGetOut.getRouteID(), objLotCurrentOperationInfoGetOut.getOperationNumber(), "", messageSb.toString());
            }

            /*------------------------------------------------------------------------*/
            /*               Call    schdlChangeReservation_DeleteDR                  */
            /*------------------------------------------------------------------------*/
            scheduleChangeReservationMethod.schdlChangeReservationDeleteDR(objCommon, schdlChangeReservation);
            schdlChangeReservationList.add(schdlChangeReservation);
        }
        return schdlChangeReservationList;
    }

    @Override
    public List<Infos.WIPLotResetResult> sxStepContentResetByLotReq(Infos.ObjCommon objCommon, Infos.StepContentResetByLotReqInParm stepContentResetByLotReqInParm, String claimMemo) {
        List<Infos.WIPLotResetResult> wipLotResetResults = new ArrayList<>();
        wipLotResetResults.add(new Infos.WIPLotResetResult(new RetCode(objCommon.getTransactionID(), retCodeConfig.getSucc(), null)));
        //--------------------------------
        // get updateLevel
        //--------------------------------
        long updateLevel = stepContentResetByLotReqInParm.getUpdateLevel();
        if (-1 == updateLevel) {
            String value = StandardProperties.OM_STEP_CONTENT_RESET_LEVEL_FOR_LOT.getValue();
            updateLevel = CimObjectUtils.isEmpty(value) ? 0 : Long.valueOf(value);
        }
        Validations.check((0 != updateLevel) && (1 != updateLevel), retCodeConfig.getInvalidParameter());

        //--------------------------------
        // Lock objects to be updated
        //--------------------------------
        List<Infos.WIPLotResetAttribute> strWIPLotResetAttributes = stepContentResetByLotReqInParm.getStrWIPLotResetAttributes();
        for (Infos.WIPLotResetAttribute strWIPLotResetAttribute : strWIPLotResetAttributes) {
            ObjectIdentifier lotID = strWIPLotResetAttribute.getLotID();
            //--------------------------------
            // Get cassette / lot connection
            //--------------------------------
            ObjectIdentifier cassetteID = null;
            try {
                cassetteID = lotMethod.lotCassetteGet(objCommon, lotID);
            } catch (ServiceException ex) {
                if (!Validations.isEquals(retCodeConfig.getNotFoundCassette(), ex.getCode())) {
                    throw ex;
                }
            }
            //Lock cassette
            objectLockMethod.objectLock(objCommon, CimCassette.class, cassetteID);

            //Lock Lot
            objectLockMethod.objectLock(objCommon, CimLot.class, lotID);
        }

        //--------------------------------
        // Check Lot/Cassette conditions
        //--------------------------------
        for (Infos.WIPLotResetAttribute strWIPLotResetAttribute : strWIPLotResetAttributes) {
            ObjectIdentifier lotID = strWIPLotResetAttribute.getLotID();
            //----------------------------------
            // candidate lot should have CJ
            //----------------------------------
            ObjectIdentifier controlJobID = lotMethod.lotControlJobIDGet(objCommon, lotID);
            Validations.check(!ObjectIdentifier.isEmpty(controlJobID), retCodeConfig.getLotControlJobidFilled(), lotID, controlJobID);

            //------------------------------------
            // Check LOCK Hold.
            //------------------------------------
            lotMethod.lotCheckLockHoldConditionForOperation(objCommon, Arrays.asList(lotID));
            //----------------------------------
            //  Get InPostProcessFlag of Lot
            //----------------------------------
            Outputs.ObjLotInPostProcessFlagOut objLotInPostProcessFlagOut = lotMethod.lotInPostProcessFlagGet(objCommon, lotID);
            //----------------------------------------------
            //  If Lot is in post process, returns error
            //----------------------------------------------
            Validations.check(objLotInPostProcessFlagOut.getInPostProcessFlagOfLot(), retCodeConfig.getLotInPostProcess(), lotID);

            //----------------------------------------------
            //  Check Lot Process State
            //----------------------------------------------
            String theLotProcessState = lotMethod.lotProcessStateGet(objCommon, lotID);
            Validations.check(!CimStringUtils.equals(BizConstant.SP_LOT_PROCSTATE_WAITING, theLotProcessState), retCodeConfig.getInvalidLotProcstat(), lotID, theLotProcessState);
            //----------------------------------------------
            //  Check Finished State of lot
            //----------------------------------------------
            String lotFinishedState = lotMethod.lotFinishedStateGet(objCommon, lotID);
            Validations.check(CimStringUtils.equals(lotFinishedState, BizConstant.CIMFW_LOT_FINISHEDSTATE_SCRAPPED) ||
                    CimStringUtils.equals(lotFinishedState, BizConstant.CIMFW_LOT_FINISHEDSTATE_EMPTIED) ||
                    CimStringUtils.equals(lotFinishedState, BizConstant.CIMFW_LOT_FINISHEDSTATE_COMPLETED) ||
                    CimStringUtils.equals(lotFinishedState, BizConstant.SP_LOT_FINISHED_STATE_STACKED), retCodeConfig.getInvalidLotFinishStat(), lotFinishedState);

            //----------------------------------------------
            //  Check inventory State of lot
            //----------------------------------------------
            String lotInventoryState = lotMethod.lotInventoryStateGet(objCommon, lotID);
            Validations.check(CimStringUtils.equals(lotInventoryState, BizConstant.SP_LOT_INVENTORYSTATE_INBANK),
                    retCodeConfig.getInvalidLotInventoryStat(), lotID, lotInventoryState);

            //----------------------------------------------
            //  Check backup State of lot
            //----------------------------------------------
            Infos.LotBackupInfo lotBackupInfo = lotMethod.lotBackupInfoGet(objCommon, lotID);
            Validations.check(CimBooleanUtils.isFalse(lotBackupInfo.getCurrentLocationFlag()) ||
                    CimBooleanUtils.isTrue(lotBackupInfo.getTransferFlag()), retCodeConfig.getLotInOthersite(), lotID);

            //-------------------------------
            // Check Lot interFabXferState
            //-------------------------------
            String interFabXferState = lotMethod.lotInterFabXferStateGet(objCommon, lotID);
            Validations.check(CimStringUtils.equals(interFabXferState, BizConstant.SP_INTERFAB_XFERSTATE_TRANSFERRING)
                    || CimStringUtils.equals(interFabXferState, BizConstant.SP_INTERFAB_XFERSTATE_REQUIRED), retCodeConfig.getInterfabInvalidLotXferstateForReq(), lotID, interFabXferState);

            //-------------------------------
            //  Check FlowBatch Condition
            //-------------------------------
            try {
                lotMethod.lotFlowBatchIDGet(objCommon, lotID);
            } catch (ServiceException e) {
                if (Validations.isEquals(retCodeConfig.getLotFlowBatchIdFilled(), e.getCode())) {
                    Validations.check(retCodeConfig.getFlowBatchLimitation());
                } else if (Validations.isEquals(retCodeConfig.getLotFlowBatchIdBlank(), e.getCode())) {
                    log.info("lot_flowBatchID_Get() == RC_LOT_BATCH_ID_BLANK");
                } else {
                    throw e;
                }
            }

            //---------------------------------------------------------
            //   Check whether Lot is on the specified Route/Operation
            //---------------------------------------------------------
            Outputs.ObjLotCurrentOperationInfoGetOut objLotCurrentOperationInfoGetOut = lotMethod.lotCurrentOperationInfoGet(objCommon, lotID);
            ObjectIdentifier routeID = objLotCurrentOperationInfoGetOut.getRouteID();
            String operationNumber = objLotCurrentOperationInfoGetOut.getOperationNumber();
            log.info("Lot's currentRouteID         : {}", routeID);
            log.info("Lot's currentOperationNumber : {}", operationNumber);

            if (ObjectIdentifier.equalsWithValue(strWIPLotResetAttribute.getRouteID(), routeID) &&
                    CimStringUtils.equals(strWIPLotResetAttribute.getOperationNumber(), operationNumber)) {
                log.info("Route/Operation check OK. Go ahead...");
            } else {
                Validations.check(true, retCodeConfig.getNotSameRoute(), "Input parameter's currentRouteID/currentOperationNumber", "lot's current currentRouteID/currentOperationNumber");
            }

            //-------------------------------
            //  call lot_WIPInfoReset
            //-------------------------------
            Inputs.ObjLotWIPInfoResetIn objLotWIPInfoResetIn = new Inputs.ObjLotWIPInfoResetIn();
            objLotWIPInfoResetIn.setUpdateLevel(updateLevel);
            objLotWIPInfoResetIn.setLotID(strWIPLotResetAttribute.getLotID());
            objLotWIPInfoResetIn.setRouteID(strWIPLotResetAttribute.getRouteID());
            objLotWIPInfoResetIn.setOperationNumber(strWIPLotResetAttribute.getOperationNumber());
            Outputs.ObjLotWIPInfoResetOut objLotWIPInfoResetOut = lotMethod.lotWIPInfoReset(objCommon, objLotWIPInfoResetIn);

            //---------------------------------
            // UpDate RequiredCassetteCategory
            //---------------------------------
            if (1 == updateLevel) {
                lotMethod.lotCassetteCategoryUpdateForContaminationControl(objCommon, lotID);
            }
            //---------------------------------
            //  Update Cassette's MultiLotType
            //---------------------------------
            ObjectIdentifier cassetteID = null;
            try {
                cassetteID = lotMethod.lotCassetteGet(objCommon, lotID);
            } catch (ServiceException e) {
                if (!Validations.isEquals(retCodeConfig.getNotFoundCassette(), e.getCode())) {
                    throw e;
                }
            }

            cassetteMethod.cassetteMultiLotTypeUpdate(objCommon, cassetteID);

            //---------------------------------
            // Make History
            //---------------------------------
            if (objLotWIPInfoResetOut.isOpehsAddFlag()) {
                eventMethod.lotOperationMoveEventMakeChangeRoute(objCommon, objCommon.getTransactionID(), lotID, objLotWIPInfoResetOut.getOldCurrentPOData(), claimMemo);

            }
        }

        return wipLotResetResults;
    }

    @Override
    public List<Infos.ChangeLotSchdlReturn> sxLotPlanChangeReserveDoActionReq(Infos.ObjCommon objCommon, List<Infos.ReScheduledLotAttributes> reScheduledLotAttributesList, String claimMemo){
        List<Infos.ChangeLotSchdlReturn> changeLotSchdlReturnList = new ArrayList<>();
        int nLen = CimArrayUtils.getSize(reScheduledLotAttributesList);
        //------------------------------------
        // Check LOCK Hold.
        //------------------------------------
        for (int loopCnt = 0; loopCnt < nLen; loopCnt++){
            List<ObjectIdentifier> lotIDSeq = Collections.singletonList(reScheduledLotAttributesList.get(loopCnt).getLotID());
            lotMethod.lotCheckLockHoldConditionForOperation(objCommon, lotIDSeq);
        }
        //-----------------------------
        //  Check InPostProcessFlag
        //-----------------------------
        List<ObjectIdentifier> userGroupIDs = new ArrayList<>();
        int userGroupIDsLen = 0;
        for (int loopCnt = 0; loopCnt < nLen; loopCnt++){
            //----------------------------------
            //  Get InPostProcessFlag of Lot
            //----------------------------------
            Outputs.ObjLotInPostProcessFlagOut objLotInPostProcessFlagOut = lotMethod.lotInPostProcessFlagGet(objCommon, reScheduledLotAttributesList.get(loopCnt).getLotID());
            //----------------------------------------------
            //  If Lot is in post process, returns error
            //----------------------------------------------
            if (objLotInPostProcessFlagOut.getInPostProcessFlagOfLot()){
                if (CimArrayUtils.getSize(userGroupIDs) == 0){
                    userGroupIDs = personMethod.personUserGroupListGetDR(objCommon, objCommon.getUser().getUserID());
                    userGroupIDsLen = CimArrayUtils.getSize(userGroupIDs);
                }
                int nCnt = 0;
                for (nCnt = 0; nCnt < userGroupIDsLen; nCnt++){
                }
                if (nCnt == userGroupIDsLen){
                    throw new ServiceException(new OmCode(retCodeConfig.getLotInPostProcess(), reScheduledLotAttributesList.get(loopCnt).getLotID().getValue()));
                }
            }
        }
        for (int seqIx = 0; seqIx < nLen; seqIx++){
            Infos.ReScheduledLotAttributes reScheduledLotAttributes = reScheduledLotAttributesList.get(seqIx);
            Infos.ChangeLotSchdlReturn changeLotSchdlReturn = new Infos.ChangeLotSchdlReturn();
            changeLotSchdlReturnList.add(changeLotSchdlReturn);
            changeLotSchdlReturn.setLotID(reScheduledLotAttributes.getLotID());
            /*------------------------------------------------------------------------*/
            /*   Check Lot Process State                                              */
            /*------------------------------------------------------------------------*/
            String theLotProcessState = lotMethod.lotProcessStateGet(objCommon, reScheduledLotAttributes.getLotID());
            if (!CimStringUtils.equals(theLotProcessState, BizConstant.SP_LOT_PROCSTATE_WAITING)){
                throw new ServiceException(new OmCode(retCodeConfig.getInvalidLotProcstat(), reScheduledLotAttributes.getLotID().getValue(), theLotProcessState));
            }
            /*------------------------------------------------------------------------*/
            /*   Check Lot Inventory State                                            */
            /*------------------------------------------------------------------------*/
            if (!ObjectIdentifier.isEmptyWithValue(reScheduledLotAttributes.getRouteID())
                    && !CimStringUtils.isEmpty(reScheduledLotAttributes.getCurrentOperationNumber())){
                String lotInventoryState = lotMethod.lotInventoryStateGet(objCommon, reScheduledLotAttributes.getLotID());
                if (!CimStringUtils.equals(lotInventoryState, BizConstant.SP_LOT_INVENTORYSTATE_ONFLOOR)
                        && !CimStringUtils.equals(lotInventoryState, BizConstant.SP_LOT_INVENTORYSTATE_NONPROBANK)){
                    Validations.check(retCodeConfig.getInvalidLotInventoryStat(), ObjectIdentifier.fetchValue(reScheduledLotAttributes.getLotID()),
                            lotInventoryState);
                }
            }
            /*------------------------------------------------------------------------*/
            /*   Check Lot Inventory State                                            */
            /*------------------------------------------------------------------------*/
            String lotFinishedState = lotMethod.lotFinishedStateGet(objCommon, reScheduledLotAttributes.getLotID());
            if (CimStringUtils.equals(lotFinishedState, BizConstant.SP_LOT_FINISHED_STATE_STACKED)){
                throw new ServiceException(new OmCode(retCodeConfig.getInvalidLotFinishStat(), lotFinishedState));
            }
            if (ObjectIdentifier.equalsWithValue(reScheduledLotAttributes.getOriginalRouteID(), reScheduledLotAttributes.getRouteID())){
                //---------------------------------------
                // Check interFabXferPlan existence
                //---------------------------------------
                Inputs.ObjProcessCheckInterFabXferPlanSkipIn objProcessCheckInterFabXferPlanSkipIn = new Inputs.ObjProcessCheckInterFabXferPlanSkipIn();
                objProcessCheckInterFabXferPlanSkipIn.setLotID(reScheduledLotAttributes.getLotID());
                objProcessCheckInterFabXferPlanSkipIn.setCurrentRouteID(ObjectIdentifier.buildWithValue(reScheduledLotAttributes.getOriginalRouteID()));
                objProcessCheckInterFabXferPlanSkipIn.setCurrentOpeNo(reScheduledLotAttributes.getOriginalOperationNumber());
                objProcessCheckInterFabXferPlanSkipIn.setJumpingRouteID(reScheduledLotAttributes.getRouteID());
                objProcessCheckInterFabXferPlanSkipIn.setJumpingOpeNo(reScheduledLotAttributes.getCurrentOperationNumber());
                processMethod.processCheckInterFabXferPlanSkip(objCommon, objProcessCheckInterFabXferPlanSkipIn);
            }
            /*------------------------------------------------------------------------*/
            /*   Check Lot's backup State                                             */
            /*------------------------------------------------------------------------*/
            Infos.LotBackupInfo lotBackupInfo = lotMethod.lotBackupInfoGet(objCommon, reScheduledLotAttributes.getLotID());
            if (lotBackupInfo.getBackupProcessingFlag()){
                Validations.check(!ObjectIdentifier.isEmptyWithValue(reScheduledLotAttributes.getProductID()), new OmCode(retCodeConfig.getLotBackupOnBackupsite(), reScheduledLotAttributes.getLotID().getValue()));
                ObjectIdentifier currentRouteID = lotMethod.lotCurrentRouteIDGet(objCommon, reScheduledLotAttributes.getLotID());
                Validations.check(!ObjectIdentifier.equalsWithValue(currentRouteID, reScheduledLotAttributes.getRouteID()), new OmCode(retCodeConfig.getLotBackupOnBackupsite(), reScheduledLotAttributes.getLotID().getValue()));
            }
            /*------------------------------------------------------------------------*/
            /*   Register Product Request                                             */
            /*------------------------------------------------------------------------*/
            Outputs.ObjLotChangeScheduleOut objLotChangeScheduleOut = lotMethod.lotChangeSchedule(objCommon, reScheduledLotAttributes);
            /*------------------------------------------------------------------------*/
            /*   Delete all QTime restrictions for previous route                     */
            /*------------------------------------------------------------------------*/
            if (CimBooleanUtils.isTrue(objLotChangeScheduleOut.getOpehsMoveFlag())){
                boolean bSameRouteFlag = false;
                if (ObjectIdentifier.equalsWithValue(reScheduledLotAttributes.getRouteID(), reScheduledLotAttributes.getOriginalRouteID())){
                    bSameRouteFlag = true;
                }
                String keepQTime = StandardProperties.OM_QT_KEEP_ON_LOT_PLAN_CHG.getValue();
                if (bSameRouteFlag && CimStringUtils.equals(keepQTime, BizConstant.SP_FUNCTION_AVAILABLE_TRUE)){
                    log.info("Not clear Q-Time info");
                } else {
                    log.info("Clear Q-Time info");
                    Outputs.ObjQtimeAllClearByRouteChangeOut objQtimeAllClearByRouteChangeOut = qTimeMethod.qtimeAllClearByRouteChange(objCommon, reScheduledLotAttributes.getLotID());
                    //--------------------------------------------------
                    // Reset Q-Time actions
                    //--------------------------------------------------
                    ObjectIdentifier resetReasonCodeID = ObjectIdentifier.buildWithValue(BizConstant.SP_REASON_QTIMECLEAR);
                    List<Infos.LotHoldReq> strLotHoldReleaseList = objQtimeAllClearByRouteChangeOut.getStrLotHoldReleaseList();
                    //----- Lot Hold Actions -------//
                    if (!CimArrayUtils.isEmpty(strLotHoldReleaseList)){
                        Params.HoldLotReleaseReqParams holdLotReleaseReqParams = new Params.HoldLotReleaseReqParams();
                        holdLotReleaseReqParams.setHoldReqList(strLotHoldReleaseList);
                        holdLotReleaseReqParams.setLotID(reScheduledLotAttributes.getLotID());
                        holdLotReleaseReqParams.setReleaseReasonCodeID(resetReasonCodeID);
                        lotService.sxHoldLotReleaseReq(objCommon, holdLotReleaseReqParams);
                    }
                    //----- Future Hold Actions -------//
                    if (!CimArrayUtils.isEmpty(objQtimeAllClearByRouteChangeOut.getStrFutureHoldCancelList())){
                        Params.FutureHoldCancelReqParams futureHoldCancelReqParams = new Params.FutureHoldCancelReqParams();
                        futureHoldCancelReqParams.setEntryType(BizConstant.SP_ENTRYTYPE_CANCEL);
                        futureHoldCancelReqParams.setLotID(reScheduledLotAttributes.getLotID());
                        futureHoldCancelReqParams.setReleaseReasonCodeID(resetReasonCodeID);
                        futureHoldCancelReqParams.setLotHoldList(objQtimeAllClearByRouteChangeOut.getStrFutureHoldCancelList());
                        processControlService.sxFutureHoldCancelReq(objCommon, futureHoldCancelReqParams);
                    }
                    //----- Future Rework Actions -------//
                    List<Infos.FutureReworkInfo> strFutureReworkCancelList = objQtimeAllClearByRouteChangeOut.getStrFutureReworkCancelList();
                    int cancelLen = CimArrayUtils.getSize(strFutureReworkCancelList);
                    if (cancelLen > 0){
                        for (int cancelCnt = 0; cancelCnt < cancelLen; cancelCnt++){
                            Infos.FutureReworkInfo futureReworkInfo = strFutureReworkCancelList.get(cancelCnt);
                            processControlService.sxFutureReworkCancelReq(objCommon, futureReworkInfo.getLotID(), futureReworkInfo.getRouteID(),
                                    futureReworkInfo.getOperationNumber(), futureReworkInfo.getFutureReworkDetailInfoList(), "");
                        }
                    }
                }
            }
            //--------------------------------------------------------------------------------------------------
            // UpDate RequiredCassetteCategory
            //--------------------------------------------------------------------------------------------------
            lotMethod.lotCassetteCategoryUpdateForContaminationControl(objCommon, reScheduledLotAttributes.getLotID());
            /*------------------------------------------------------------------------*/
            /*   Make History                                                         */
            /*------------------------------------------------------------------------*/
            if (CimBooleanUtils.isTrue(objLotChangeScheduleOut.getOpehsAddFlag())){
                eventMethod.lotOperationMoveEventMakeChangeRoute(objCommon, TransactionIDEnum.LOT_SCHEDULE_CHANGE_REQ.getValue(), reScheduledLotAttributes.getLotID(), objLotChangeScheduleOut.getOldCurrentPOData(), claimMemo);
            }
            Inputs.LotChangeEventMakeParams lotChangeEventMakeParams = new Inputs.LotChangeEventMakeParams();
            lotChangeEventMakeParams.setTransactionID(TransactionIDEnum.SCHEDULE_CHANGERESERVATION_EXECUTE_REQ.getValue());
            lotChangeEventMakeParams.setLotID(reScheduledLotAttributes.getLotID().getValue());
            lotChangeEventMakeParams.setExternalPriority("0");
            lotChangeEventMakeParams.setPriorityClass(reScheduledLotAttributes.getPriorityClass());
            lotChangeEventMakeParams.setProductID(reScheduledLotAttributes.getProductID().getValue());
            lotChangeEventMakeParams.setPreviousProductID(objLotChangeScheduleOut.getPreviousProductID().getValue());
            lotChangeEventMakeParams.setPlanStartTime(reScheduledLotAttributes.getPlannedStartTime());
            lotChangeEventMakeParams.setPlanCompTime(reScheduledLotAttributes.getPlannedFinishTime());
            lotChangeEventMakeParams.setClaimMemo(claimMemo);
            eventMethod.lotChangeEventMake(objCommon, lotChangeEventMakeParams);

            Inputs.LotReticleSetChangeEventMakeParams lotReticleSetChangeEventMakeParams = new Inputs.LotReticleSetChangeEventMakeParams();
            lotReticleSetChangeEventMakeParams.setTransactionID(TransactionIDEnum.LOT_SCHEDULE_CHANGE_REQ.getValue());
            lotReticleSetChangeEventMakeParams.setLotID(reScheduledLotAttributes.getLotID());
            lotReticleSetChangeEventMakeParams.setClaimMemo(claimMemo);
            eventMethod.lotReticleSetChangeEventMake(objCommon, lotReticleSetChangeEventMakeParams);
        }
        /*------------------------------------------*/
        /*   AMHS I/F for Priority Change Request   */
        /*------------------------------------------*/
        int cLen=0;
        Infos.PriorityChangeReq priorityChangeReq = new Infos.PriorityChangeReq();
        List<Infos.PriorityInfo> priorityInfoData = new ArrayList<>();
        priorityChangeReq.setPriorityInfoData(priorityInfoData);
        ObjectIdentifier aLotID = null;
        ObjectIdentifier aCassetteID = null;
        for (int i = 0; i < nLen; i++){
            /*------------------------------------------------------------------------*/
            /*   Get cassette / lot connection                                        */
            /*------------------------------------------------------------------------*/
            aLotID = reScheduledLotAttributesList.get(i).getLotID();
            int retCode = -100;
            ObjectIdentifier cassetteID = null;
            try {
                cassetteID = lotMethod.lotCassetteGet(objCommon, aLotID);
                retCode = 0;
            } catch (ServiceException e) {

            }
            if (retCode == 0){
                aCassetteID = cassetteID;
                Outputs.ObjCassetteLotListGetWithPriorityOrderOut objCassetteLotListGetWithPriorityOrderOut = null;
                int retCode2 = -100;
                try {
                    objCassetteLotListGetWithPriorityOrderOut = cassetteMethod.cassetteLotListGetWithPriorityOrder(objCommon, aCassetteID);
                    retCode2 = 0;
                } catch (ServiceException e) {
                }
                if (retCode2 == 0){
                    List<Infos.LotStatusInfo> lotStatusInfos = objCassetteLotListGetWithPriorityOrderOut.getLotStatusInfos();
                    if (!CimArrayUtils.isEmpty(lotStatusInfos)){
                        for (Infos.LotStatusInfo lotStatusInfo : lotStatusInfos){
                            if (ObjectIdentifier.equalsWithValue(aLotID, lotStatusInfo.getLotID())){
                                Infos.PriorityInfo priorityInfo = new Infos.PriorityInfo();
                                priorityInfoData.add(priorityInfo);
                                priorityInfo.setPriority(reScheduledLotAttributesList.get(i).getPriorityClass());
                                priorityInfo.setCarrierID(aCassetteID);
                            }
                        }
                    }
                }
            }
        }
        int xLen = CimArrayUtils.getSize(priorityInfoData);
        if (xLen > 0) {
            //XMSMgr_SendPriorityChangeReq
            try {
                tmsService.priorityChangeReq(objCommon, objCommon.getUser(), priorityChangeReq);
            } catch (Exception ex) {

            }
        }
        /*------------------------------------------------------------------------*/
        /*   Return                                                               */
        /*------------------------------------------------------------------------*/
        for (int i = 0; i < nLen; i++){
            changeLotSchdlReturnList.get(i).setReturnCode(String.valueOf(retCodeConfig.getSucc().getCode()));
            changeLotSchdlReturnList.get(i).setLotID(reScheduledLotAttributesList.get(i).getLotID());
        }
        return changeLotSchdlReturnList;
    }


    @Override
    public void sxSchdlChangeReservationExecuteByPostProcReq(Infos.ObjCommon objCommon, ObjectIdentifier lotID) {
        // ----------------------------
        // trace and check input param |
        // ----------------------------
        Validations.check(ObjectIdentifier.isEmpty(lotID),retCodeConfig.getInvalidInputParam(),lotID);

        ObjectIdentifier routeID = ObjectIdentifier.emptyIdentifier();
        String operationNumber = null;
        // -------------------------------------------------------
        // check if step has bean moved                           |
        // -------------------------------------------------------
        final boolean isCurrentPO = lotMethod.lotCheckConditionForPO(objCommon, lotID);

        if (isCurrentPO) {
            Outputs.ObjLotCurrentOperationInfoGetOut objLotCurrentOperationInfo
                    = lotMethod.lotCurrentOperationInfoGet(objCommon, lotID);
            routeID = objLotCurrentOperationInfo.getRouteID();
            operationNumber = objLotCurrentOperationInfo.getOperationNumber();

        } else {
            Outputs.ObjLotPreviousOperationInfoGetOut objLotPreviousOperationInfo
                    = lotMethod.lotPreviousOperationInfoGet(objCommon, lotID);
            routeID = objLotPreviousOperationInfo.getRouteID();
            operationNumber = objLotPreviousOperationInfo.getOperationNumber();
        }

        // -------------------------------------------------------------
        // build input param for schdlChangeReservationCheckForActionDR |
        // -------------------------------------------------------------
        Inputs.ObjSchdlChangeReservationCheckForActionDRIn objSchdlChangeReservationCheckForActionDRIn
                = new Inputs.ObjSchdlChangeReservationCheckForActionDRIn();
        objSchdlChangeReservationCheckForActionDRIn.setLotID(lotID);
        objSchdlChangeReservationCheckForActionDRIn.setRouteID(ObjectIdentifier.fetchValue(routeID));
        objSchdlChangeReservationCheckForActionDRIn.setOperationNumber(operationNumber);
        // --------------------------------------------------
        //  call schdlChangeReservationCheckForActionDR      |
        //  for get schdlChange Reservation action parameter |
        // --------------------------------------------------
        Outputs.ObjSchdlChangeReservationCheckForActionDROut schdlChangeReservationCheckForActionDROut
                = scheduleChangeReservationMethod
                .schdlChangeReservationCheckForActionDR(objCommon, objSchdlChangeReservationCheckForActionDRIn);

        boolean existFlag = schdlChangeReservationCheckForActionDROut.isExistFlag();
        if (existFlag){
            // ----------------------------------
            // call lotCurrentOperationInfoGetDR |
            // ----------------------------------
            if (log.isDebugEnabled())  {
                log.debug("find the exist SchdlChange Reservation plan for current lot");
            }

            ObjectIdentifier lotCurrentRouteID = lotMethod.lotCurrentRouteIDGet(objCommon, lotID);
            String lotCurrentOpeNo = lotMethod.lotCurrentOpeNoGet(objCommon, lotID);
            // --------------------------------------------------
            //  build ReScheduledLotAttributes constructor param |
            // --------------------------------------------------
            List<Infos.ReScheduledLotAttributes> reScheduledLotAttributes = new ArrayList<>(1);
            reScheduledLotAttributes.add(new Infos.ReScheduledLotAttributes());
            reScheduledLotAttributes.get(0).setLotID(lotID);
            reScheduledLotAttributes.get(0).setProductID(
                    schdlChangeReservationCheckForActionDROut.getStrSchdlChangeReservation().getProductID());
            reScheduledLotAttributes.get(0).setRouteID(
                    schdlChangeReservationCheckForActionDROut.getStrSchdlChangeReservation().getRouteID());
            reScheduledLotAttributes.get(0).setCurrentOperationNumber(
                    schdlChangeReservationCheckForActionDROut.getStrSchdlChangeReservation().getOperationNumber());
            reScheduledLotAttributes.get(0).setSubLotType(
                    schdlChangeReservationCheckForActionDROut.getStrSchdlChangeReservation().getSubLotType());
            reScheduledLotAttributes.get(0).setOriginalRouteID(ObjectIdentifier
                    .fetchValue(lotCurrentRouteID));
            reScheduledLotAttributes.get(0).setOriginalOperationNumber(lotCurrentOpeNo);

            if(log.isDebugEnabled()) {
                log.debug("step86 - change lot plan reserve do action");
            }
            this.sxLotPlanChangeReserveDoActionReq(objCommon, reScheduledLotAttributes,
                    schdlChangeReservationCheckForActionDROut.getStrSchdlChangeReservation().getEventID());
            if(log.isDebugEnabled()) {
                log.debug("step87 - change schedule reservation apply count increase");
            }
            scheduleChangeReservationMethod.schdlChangeReservationApplyCountIncreaseDR(objCommon,
                    schdlChangeReservationCheckForActionDROut.getStrSchdlChangeReservation());
        }
    }

}
