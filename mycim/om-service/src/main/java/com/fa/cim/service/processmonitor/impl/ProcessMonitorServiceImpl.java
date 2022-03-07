package com.fa.cim.service.processmonitor.impl;

import com.fa.cim.annotaion.OmService;
import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.OmCode;
import com.fa.cim.common.support.RetCode;
import com.fa.cim.common.utils.CimArrayUtils;
import com.fa.cim.common.utils.CimBooleanUtils;
import com.fa.cim.common.utils.CimStringUtils;
import com.fa.cim.common.utils.Validations;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.dto.*;
import com.fa.cim.lmg.LotMonitorGroupParams;
import com.fa.cim.lmg.LotMonitorGroupResults;
import com.fa.cim.method.*;
import com.fa.cim.newcore.bo.durable.CimCassette;
import com.fa.cim.newcore.bo.product.CimControlJob;
import com.fa.cim.newcore.bo.product.CimLot;
import com.fa.cim.service.lot.ILotService;
import com.fa.cim.service.processcontrol.IProcessControlService;
import com.fa.cim.service.processmonitor.IProcessMonitorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@OmService
@Slf4j
public class ProcessMonitorServiceImpl implements IProcessMonitorService {

    @Autowired
    private RetCodeConfig retCodeConfig;

    @Autowired
    private ILotMethod lotMethod;

    @Autowired
    private IMonitorGroupMethod monitorGroupMethod;

    @Autowired
    private ICassetteMethod cassetteMethod;

    @Autowired
    private IProductMethod productMethod;

    @Autowired
    private IEquipmentMethod equipmentMethod;

    @Autowired
    private IWaferMethod waferMethod;

    @Autowired
    private IContaminationMethod contaminationMethod;

    @Autowired
    private IControlJobMethod controlJobMethod;

    @Autowired
    private IProcessControlService processControlService;

    @Autowired
    private IEventMethod eventMethod;

    @Autowired
    private IObjectLockMethod objectLockMethod;

    @Autowired
    private IObjectMethod objectMethod;

    @Autowired
    private ILotService lotService;

    @Override
    public Results.MonitorBatchDeleteReqResult sxMonitorBatchDeleteReq(Infos.ObjCommon objCommon, ObjectIdentifier monitorLotID) {
        Results.MonitorBatchDeleteReqResult out = new Results.MonitorBatchDeleteReqResult();
        //-----------------------------------------------------------
        // Check lot interFabXferState
        //-----------------------------------------------------------
        String XferState = lotMethod.lotInterFabXferStateGet(objCommon, monitorLotID);

        Validations.check(CimStringUtils.equals(BizConstant.SP_INTERFAB_XFERSTATE_TRANSFERRING, XferState),retCodeConfig.getInterfabInvalidLotXferstateForReq());
        /*------------------------------------------------------------------------*/
        /*   Change State                                                         */
        /*------------------------------------------------------------------------*/
        List<Infos.MonitoredLots> retCode = monitorGroupMethod.monitorGroupDelete(objCommon, monitorLotID);
        List<Infos.MonRelatedProdLots> monRelatedProdLots = new ArrayList<>();
        for (int i = 0; i < CimArrayUtils.getSize(monRelatedProdLots); i++) {
            Infos.MonRelatedProdLots monRelatedProdLot = new Infos.MonRelatedProdLots();
            monRelatedProdLot.setProductLotID(monRelatedProdLots.get(i).getProductLotID());
            monRelatedProdLots.add(monRelatedProdLot);
        }
        out.setStrMonRelatedProdLots(monRelatedProdLots);
        return out;
    }

    @Override
    public void sxMonitorBatchCreateReq(Infos.ObjCommon objCommon, Params.MonitorBatchCreateReqParams monitorBatchCreateReqParams) {
        String lotType = lotMethod.lotTypeGet(objCommon, monitorBatchCreateReqParams.getMonitorLotID());

        /*------------------------------------------------------------------------*/
        /*   Check Condition                                                      */
        /*------------------------------------------------------------------------*/
        if (!BizConstant.SP_LOT_TYPE_PRODUCTIONMONITORLOT.equals(lotType)) {
            boolean routeFlagGetOut = lotMethod.lotMonitorRouteFlagGet(objCommon,
                    monitorBatchCreateReqParams.getMonitorLotID());
            Validations.check(CimBooleanUtils.isFalse(routeFlagGetOut), new OmCode(retCodeConfig.getInvalidLotType(),
                    lotType, monitorBatchCreateReqParams.getMonitorLotID().getValue()));
        }

        String lotProcessStateGet = lotMethod.lotProcessStateGet(objCommon,
                monitorBatchCreateReqParams.getMonitorLotID());
        Validations.check(BizConstant.SP_LOT_PROCSTATE_PROCESSING.equals(lotProcessStateGet),
                retCodeConfig.getInvalidLotProcessState(),
                ObjectIdentifier.fetchValue(monitorBatchCreateReqParams.getMonitorLotID()), lotProcessStateGet);

        /*------------------------------------------------------------------------*/
        /*   Check lot Finished State                                             */
        /*------------------------------------------------------------------------*/
        String lotFinishedStateGet = lotMethod.lotFinishedStateGet(objCommon,
                monitorBatchCreateReqParams.getMonitorLotID());
        Validations.check(
                BizConstant.CIMFW_LOT_FINISHEDSTATE_SCRAPPED.equals(lotFinishedStateGet)
                        || BizConstant.CIMFW_LOT_FINISHEDSTATE_EMPTIED.equals(lotFinishedStateGet)
                        || BizConstant.CIMFW_LOT_FINISHEDSTATE_COMPLETED.equals(lotFinishedStateGet)
                        || BizConstant.SP_LOT_FINISHED_STATE_STACKED.equals(lotFinishedStateGet),
                new OmCode(retCodeConfig.getInvalidLotFinishStat(), lotFinishedStateGet));

        monitorGroupMethod.monitorGroupCheckExistance(objCommon, monitorBatchCreateReqParams.getMonitorLotID());
        // For Production lots
        List<Infos.MonRelatedProdLots> strMonRelatedProdLots = new ArrayList<>();
        for (int i = 0; i < CimArrayUtils.getSize(monitorBatchCreateReqParams.getStrMonRelatedProdLots()); i++) {
            // Initialize strMonRelatedProdLots
            strMonRelatedProdLots.add(monitorBatchCreateReqParams.getStrMonRelatedProdLots().get(i));
            String lotTypeGet = lotMethod.lotTypeGet(objCommon,
                    monitorBatchCreateReqParams.getStrMonRelatedProdLots().get(i).getProductLotID());

            Validations.check(
                    !BizConstant.SP_LOT_TYPE_PRODUCTIONLOT.equals(lotTypeGet)
                            && !BizConstant.SP_LOT_TYPE_ENGINEERINGLOT.equals(lotTypeGet)
                            && !BizConstant.SP_LOT_TYPE_PRODUCTIONMONITORLOT.equals(lotTypeGet)
                            && !BizConstant.SP_LOT_TYPE_EQUIPMENTMONITORLOT.equals(lotTypeGet)
                            && !BizConstant.SP_LOT_TYPE_RECYCLELOT.equals(lotTypeGet)
                            && !BizConstant.SP_LOT_TYPE_DUMMYLOT.equals(lotTypeGet),
                    new OmCode(retCodeConfig.getInvalidLotType(), lotTypeGet, ObjectIdentifier.fetchValue(
                            monitorBatchCreateReqParams.getStrMonRelatedProdLots().get(i).getProductLotID())));

            String lotProcessState = lotMethod.lotProcessStateGet(objCommon,
                    monitorBatchCreateReqParams.getStrMonRelatedProdLots().get(i).getProductLotID());
            Validations.check(BizConstant.SP_LOT_PROCSTATE_PROCESSING.equals(lotProcessState),
                    retCodeConfig.getInvalidLotProcessState(),
                    ObjectIdentifier.fetchValue(
                            monitorBatchCreateReqParams.getStrMonRelatedProdLots().get(i).getProductLotID()),
                    lotProcessState);

            String lotFinishedState = lotMethod.lotFinishedStateGet(objCommon,
                    monitorBatchCreateReqParams.getStrMonRelatedProdLots().get(i).getProductLotID());
            Validations.check(
                    BizConstant.CIMFW_LOT_FINISHEDSTATE_SCRAPPED.equals(lotFinishedState)
                            || BizConstant.SP_LOT_FINISHED_STATE_STACKED.equals(lotFinishedState)
                            || BizConstant.CIMFW_LOT_FINISHEDSTATE_EMPTIED.equals(lotFinishedState),
                    new OmCode(retCodeConfig.getInvalidLotFinishStat(), lotFinishedState));

        }
        /*------------------------------------------------------------------------*/
        /* Change State                                                           */
        /*------------------------------------------------------------------------*/
        LotMonitorGroupResults.LotMonitorGroupHistoryResults lotMonitorGroupHistoryResults = monitorGroupMethod
                .monitorGroupMake(objCommon, monitorBatchCreateReqParams.getMonitorLotID(), strMonRelatedProdLots,
                        false);

        /*------------------------------------------------------------------------*/
        /*   history make                                                         */
        /*------------------------------------------------------------------------*/
        lotMonitorGroupHistoryResults.getLotResult().forEach(lot -> {
            LotMonitorGroupParams.LotMonitorGroupEventParams eventParams
                    = new LotMonitorGroupParams.LotMonitorGroupEventParams();
            BeanUtils.copyProperties(lot, eventParams);
            eventParams.setMonitorGroupId(lotMonitorGroupHistoryResults.getMonitorGroupId());
            eventMethod.lotMonitorGroupEventMake(objCommon, eventParams);
        });
    }

    @Override
    public Results.AutoCreateMonitorForInProcessLotReqResult sxAutoCreateMonitorForInProcessLotReq(Infos.ObjCommon objCommon, Params.AutoCreateMonitorForInProcessLotReqParams autoCreateMonitorForInProcessLotReqParams) {
        Results.AutoCreateMonitorForInProcessLotReqResult out = new Results.AutoCreateMonitorForInProcessLotReqResult();
        //--------------------------------
        // Prepare Temporary local structure of tmpNewLotAttributes
        //--------------------------------
        List<ObjectIdentifier> productLotIDs = autoCreateMonitorForInProcessLotReqParams.getProductLotIDs();
        Validations.check(0 == productLotIDs.size(),retCodeConfig.getInvalidInputParam());
        Infos.NewLotAttributes tmpNewLotAttributes = autoCreateMonitorForInProcessLotReqParams.getStrNewLotAttributes();
        ObjectIdentifier cassetteID = tmpNewLotAttributes.getCassetteID();
        // Get required eqp lock mode
        // object_lockMode_Get
        Inputs.ObjLockModeIn objLockModeIn = new Inputs.ObjLockModeIn();
        objLockModeIn.setObjectID(autoCreateMonitorForInProcessLotReqParams.getProcessEquipmentID());
        objLockModeIn.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
        objLockModeIn.setFunctionCategory(TransactionIDEnum.MONITOR_LOT_STB_AFTER_PROCESS_REQ.getValue());
        objLockModeIn.setUserDataUpdateFlag(false);
        Outputs.ObjLockModeOut objLockModeOut = objectMethod.objectLockModeGet(objCommon, objLockModeIn);
        Long lockMode = objLockModeOut.getLockMode();
        if (!lockMode.equals(BizConstant.SP_EQP_LOCK_MODE_WRITE)){
            // Lock eqp Main Object
            // advanced_object_Lock
            objectLockMethod.advancedObjectLock(objCommon, new Inputs.ObjAdvanceLockIn(autoCreateMonitorForInProcessLotReqParams.getProcessEquipmentID(),
                    BizConstant.SP_CLASSNAME_POSMACHINE,
                    BizConstant.SP_OBJECTLOCK_OBJECTTYPE_MAINOBJECT,
                    objLockModeOut.getRequiredLockForMainObject(),
                    new ArrayList<>()));
            // Lock eqp LoadCassette Element (Write)
            // advanced_object_Lock
            objectLockMethod.advancedObjectLock(objCommon, new Inputs.ObjAdvanceLockIn(autoCreateMonitorForInProcessLotReqParams.getProcessEquipmentID(),
                    BizConstant.SP_CLASSNAME_POSMACHINE,
                    BizConstant.SP_OBJECTLOCK_OBJECTTYPE_LOADCASSETTE,
                    (long)BizConstant.SP_OBJECTLOCK_LOCKTYPE_WRITE,
                    Collections.singletonList(cassetteID.getValue())));
            /*---------------------------------*/
            /*   Get cassette's ControlJobID   */
            /*---------------------------------*/
            ObjectIdentifier controlJobID = cassetteMethod.cassetteControlJobIDGet(objCommon, cassetteID);
            // object_Lock
            if (!ObjectIdentifier.isEmptyWithValue(controlJobID)){
                objectLockMethod.objectLock(objCommon, CimControlJob.class, controlJobID);
            }

        }
        //--------------------------------
        //   Lock objects of cassette to be updated
        //--------------------------------
        //object_Lock
        objectLockMethod.objectLock(objCommon, CimCassette.class, cassetteID);
        //--------------------------------
        //   Lock objects of lot to be updated
        //--------------------------------
        List<Infos.NewWaferAttributes> newWaferAttributesList = tmpNewLotAttributes.getNewWaferAttributesList();
        int nLen = CimArrayUtils.getSize(newWaferAttributesList);
        for (int i = 0; i < nLen; i++){
            if (i > 0 && ObjectIdentifier.equalsWithValue(newWaferAttributesList.get(i - 1).getSourceLotID(), newWaferAttributesList.get(i).getSourceLotID())){
                continue;
            }
            objectLockMethod.objectLock(objCommon, CimLot.class, newWaferAttributesList.get(i).getSourceLotID());
        }

        /*-------------------------------*/
        /*   Check SorterJob existence   */
        /*-------------------------------*/
        // waferSorter_sorterJob_CheckForOperation
        List<ObjectIdentifier> dummyLotIDs = new ArrayList<>();
        List<ObjectIdentifier> cassetteIds = new ArrayList<>();
        cassetteIds.add(cassetteID);
        Inputs.ObjWaferSorterJobCheckForOperation objWaferSorterJobCheckForOperation = new Inputs.ObjWaferSorterJobCheckForOperation();
        Infos.EquipmentLoadPortAttribute dummyEquipmentLoadPortAttribute = new Infos.EquipmentLoadPortAttribute();
        objWaferSorterJobCheckForOperation.setLotIDList(dummyLotIDs);
        objWaferSorterJobCheckForOperation.setOperation(BizConstant.SP_OPERATION_FOR_CAST);
        objWaferSorterJobCheckForOperation.setCassetteIDList(cassetteIds);
        objWaferSorterJobCheckForOperation.setEquipmentLoadPortAttribute(dummyEquipmentLoadPortAttribute);
        waferMethod.waferSorterSorterJobCheckForOperation(objCommon, objWaferSorterJobCheckForOperation);
        //----------------------
        // Get information in order to create productrequest
        //----------------------
        Outputs.ObjMonitorLotSTBInfoGetOut objMonitorLotSTBInfoGetOut = lotMethod.monitorLotSTBInfoGet(objCommon, productLotIDs.get(0));

        //----------------------
        // Get bank ID for lot Generation function
        //----------------------
        ObjectIdentifier objProductSpecificationStartBankOut =  productMethod.productSpecificationStartBankGet(objCommon,objMonitorLotSTBInfoGetOut.getMonitorLotProductID());
        //----------------------
        // Prepare input parameter of productRequest_forControlLot_Release()
        //----------------------
        ObjectIdentifier productID = objMonitorLotSTBInfoGetOut.getMonitorLotProductID();
        Integer waferCount = autoCreateMonitorForInProcessLotReqParams.getStrNewLotAttributes().getNewWaferAttributesList().size();
        String lotType = objMonitorLotSTBInfoGetOut.getLotType();
        String subLotType;
        if (!CimStringUtils.isEmpty(autoCreateMonitorForInProcessLotReqParams.getStbLotSubLotType())) {
            subLotType = autoCreateMonitorForInProcessLotReqParams.getStbLotSubLotType();
        } else {
            subLotType = objMonitorLotSTBInfoGetOut.getSubLotType();
        }
        //----------------------
        // Create Product request here
        //----------------------
        ObjectIdentifier producyRequestForControlLotReleaseOut =  productMethod.productRequestForControlLotRelease(objCommon, productID, waferCount, lotType, subLotType);
        for (int i = 0; i < waferCount; i++) {
            if (ObjectIdentifier.isEmpty(autoCreateMonitorForInProcessLotReqParams.getStrNewLotAttributes().getNewWaferAttributesList().get(i).getNewLotID())) {
                autoCreateMonitorForInProcessLotReqParams.getStrNewLotAttributes().getNewWaferAttributesList().get(i).setNewLotID(producyRequestForControlLotReleaseOut);
            }
        }
        //-------------------------------
        //   Check Condition
        //-------------------------------
        equipmentMethod.equipmentLotSTBCheck(objCommon, autoCreateMonitorForInProcessLotReqParams.getProcessEquipmentID(), productLotIDs, producyRequestForControlLotReleaseOut, autoCreateMonitorForInProcessLotReqParams.getStrNewLotAttributes());

        //--------------------------------
        //   Get monitor wafer current carrier
        //--------------------------------
        List<ObjectIdentifier> monitorWaferSourceCassettes = new ArrayList<>();
        for (int i = 0; i < autoCreateMonitorForInProcessLotReqParams.getStrNewLotAttributes().getNewWaferAttributesList().size(); i++) {
            ObjectIdentifier waferLotRetCode = waferMethod.waferLotGet(objCommon,autoCreateMonitorForInProcessLotReqParams.getStrNewLotAttributes().getNewWaferAttributesList().get(i).getSourceWaferID());
            ObjectIdentifier objGetLotCassetteOut = lotMethod.lotCassetteGet(objCommon,waferLotRetCode);

            Boolean bCasFound = false;
            int monWaferSrcCasLen = CimArrayUtils.getSize(monitorWaferSourceCassettes);
            for (int j = 0; j < monWaferSrcCasLen; j++) {
                if (CimStringUtils.equals(monitorWaferSourceCassettes.get(j).getValue(),objGetLotCassetteOut.getValue())){
                    bCasFound = true;
                    break;
                }
            }
            if (CimBooleanUtils.isFalse(bCasFound)){
                monitorWaferSourceCassettes.add(monWaferSrcCasLen,objGetLotCassetteOut);
                monWaferSrcCasLen++;
            }
        }
        //--------------------------------
        //   Move wafer position
        //--------------------------------
        RetCode<Object> waferMaterialContainerChangeOut;
        for (int i = 0; i < autoCreateMonitorForInProcessLotReqParams.getStrNewLotAttributes().getNewWaferAttributesList().size(); i++) {
            Infos.Wafer strWafer = new Infos.Wafer();
            strWafer.setWaferID(autoCreateMonitorForInProcessLotReqParams.getStrNewLotAttributes().getNewWaferAttributesList().get(i).getSourceWaferID());
            strWafer.setSlotNumber(autoCreateMonitorForInProcessLotReqParams.getStrNewLotAttributes().getNewWaferAttributesList().get(i).getNewSlotNumber());
            waferMethod.waferMaterialContainerChange(objCommon, autoCreateMonitorForInProcessLotReqParams.getStrNewLotAttributes().getCassetteID(), strWafer);
        }
        //----------------------
        // Check input parameter (wafer ID)
        //----------------------
        Outputs.ObjLotParameterForLotGenerationCheckOut lotGenerationCheckOutRetCode = lotMethod.lotParameterForLotGenerationCheck(objCommon,
                objProductSpecificationStartBankOut,
                autoCreateMonitorForInProcessLotReqParams.getStrNewLotAttributes());
        if (lotGenerationCheckOutRetCode.isWaferIDAssignRequiredFlag()) {
            Outputs.ObjLotWaferIDGenerateOut lotWaferIDGenerateOutRetCode = lotMethod.lotWaferIDGenerate(objCommon,autoCreateMonitorForInProcessLotReqParams.getStrNewLotAttributes());
            autoCreateMonitorForInProcessLotReqParams.setStrNewLotAttributes(lotWaferIDGenerateOutRetCode.getNewLotAttributes());
        }
        for (int i = 0; i < autoCreateMonitorForInProcessLotReqParams.getStrNewLotAttributes().getNewWaferAttributesList().size(); i++) {
            if ((ObjectIdentifier.isEmpty(autoCreateMonitorForInProcessLotReqParams.getStrNewLotAttributes().getNewWaferAttributesList().get(i).getNewWaferID()))
                    && (!ObjectIdentifier.isEmpty(autoCreateMonitorForInProcessLotReqParams.getStrNewLotAttributes().getNewWaferAttributesList().get(i).getSourceWaferID()))){
                autoCreateMonitorForInProcessLotReqParams.getStrNewLotAttributes().getNewWaferAttributesList().get(i).setNewWaferID(autoCreateMonitorForInProcessLotReqParams.getStrNewLotAttributes().getNewWaferAttributesList().get(i).getSourceWaferID());
            }
        }
        //------------------------------------------------------------------------
        //   lot STB
        //------------------------------------------------------------------------
        Outputs.ObjLotSTBOut lotSTBOut = lotMethod.lotSTB(objCommon, producyRequestForControlLotReleaseOut, autoCreateMonitorForInProcessLotReqParams.getStrNewLotAttributes());
        out.setMonitorLotID(lotSTBOut.getCreatedLotID());

        //check if the cast usage type match the product usage type
        ObjectIdentifier LotID = lotSTBOut.getCreatedLotID();
        contaminationMethod.carrierProductUsageTypeCheck(ObjectIdentifier.emptyIdentifier(), LotID,cassetteID);

        //------------------------------------------------------
        // Copy stringified object reference of created lot
        //------------------------------------------------------
        for (int i = 0; i < autoCreateMonitorForInProcessLotReqParams.getStrNewLotAttributes().getNewWaferAttributesList().size(); i++) {
            autoCreateMonitorForInProcessLotReqParams.getStrNewLotAttributes().getNewWaferAttributesList().get(i).setNewLotID(lotSTBOut.getCreatedLotID());
        }
        //------------------------------------------------------------------------
        //   Create Monitor Group
        //------------------------------------------------------------------------
        ObjectIdentifier monitorGroupMakeByAutoOut = monitorGroupMethod.monitorGroupMakeByAuto(objCommon, lotSTBOut.getCreatedLotID(), productLotIDs);
        //----------------------
        // Update control Job Info and
        // Machine cassette info if information exist
        //----------------------
        List<ObjectIdentifier> cassetteIDs = new ArrayList<>();
        cassetteIDs.add(autoCreateMonitorForInProcessLotReqParams.getStrNewLotAttributes().getCassetteID());
        controlJobMethod.controlJobRelatedInfoUpdate(objCommon,cassetteIDs);
        //----------------------
        // Update cassette multi lot type
        //----------------------
        for (int i = 0; i < monitorWaferSourceCassettes.size(); i++) {
            cassetteMethod.cassetteMultiLotTypeUpdate(objCommon, monitorWaferSourceCassettes.get(i));
        }
        //--------------------------------------------------------------------------------------------------
        // UpDate RequiredCassetteCategory
        //--------------------------------------------------------------------------------------------------
        lotMethod.lotCassetteCategoryUpdateForContaminationControl(objCommon, lotSTBOut.getCreatedLotID());
        //-----------------------//
        //     Process Hold      //
        //-----------------------//
        processControlService.sxProcessHoldDoActionReq(objCommon,lotSTBOut.getCreatedLotID(), autoCreateMonitorForInProcessLotReqParams.getClaimMemo());

        //------------------------------------------------------------------------
        //   Make History
        //------------------------------------------------------------------------
        // lot_waferLotHistoryPointer_Update
        lotMethod.lotWaferLotHistoryPointerUpdate(objCommon, lotSTBOut.getCreatedLotID());
        //lotWaferMoveEvent_Make
        String txID = TransactionIDEnum.MONITOR_LOT_STB_AFTER_PROCESS_REQ.getValue();
        eventMethod.lotWaferMoveEventMake(objCommon, autoCreateMonitorForInProcessLotReqParams.getStrNewLotAttributes(), txID, autoCreateMonitorForInProcessLotReqParams.getClaimMemo());
        //lot_waferLotHistoryPointer_Update
        for (int i = 0; i < autoCreateMonitorForInProcessLotReqParams.getStrNewLotAttributes().getNewWaferAttributesList().size(); i++) {
            if (i > 0 && (ObjectIdentifier.equals(autoCreateMonitorForInProcessLotReqParams.getStrNewLotAttributes().getNewWaferAttributesList().get(i).getSourceLotID(),
                    autoCreateMonitorForInProcessLotReqParams.getStrNewLotAttributes().getNewWaferAttributesList().get(i - 1).getSourceLotID()))){
                continue;
            }
            lotMethod.lotWaferLotHistoryPointerUpdate(objCommon, autoCreateMonitorForInProcessLotReqParams.getStrNewLotAttributes().getNewWaferAttributesList().get(i).getSourceLotID());
        }

        return out;
    }

    @Override
    public RetCode<Object> sxMonitorHoldDoActionByPostTaskReq(Infos.ObjCommon objCommon, ObjectIdentifier lotID) {
        //init
        RetCode<Object> result = new RetCode<>();
        result.setReturnCode(retCodeConfig.getSucc());

        //Trace and check input parameter
        if (CimStringUtils.isEmpty(lotID.getValue())){
            result.setReturnCode(retCodeConfig.getInvalidInputParam());
            return result;
        }
        //Effect PO's Direction for TEL Furnace
        //【step-1】 lot_holdRecord_EffectMonitorIssueForPostProc
        List<Infos.LotHoldEffectList> lotHoldRecordEffectMonitorIssueForPostProcRetCode = lotMethod.lotHoldRecordEffectMonitorIssueForPostProc(objCommon,lotID);

        int nMonitorHoldEffectLen = 0;
        nMonitorHoldEffectLen = CimArrayUtils.getSize(lotHoldRecordEffectMonitorIssueForPostProcRetCode);
        if (nMonitorHoldEffectLen > 0){
            RetCode<Object> strHoldLotReqResult =  new RetCode<>();
            List<Infos.LotHoldReq> strLotHoldReqList = new ArrayList<>();
            for (int i = 0; i < nMonitorHoldEffectLen; i++) {
                Infos.LotHoldReq lotHoldReq = new Infos.LotHoldReq();
                strLotHoldReqList.add(lotHoldReq);
                lotHoldReq.setHoldType(lotHoldRecordEffectMonitorIssueForPostProcRetCode.get(i).getHoldType());
                lotHoldReq.setHoldReasonCodeID(lotHoldRecordEffectMonitorIssueForPostProcRetCode.get(i).getReasonCodeID());
                lotHoldReq.setHoldUserID(lotHoldRecordEffectMonitorIssueForPostProcRetCode.get(i).getUserID());
                lotHoldReq.setResponsibleOperationMark(lotHoldRecordEffectMonitorIssueForPostProcRetCode.get(i).getResponsibleOperationMark());
                lotHoldReq.setRouteID(lotHoldRecordEffectMonitorIssueForPostProcRetCode.get(i).getRouteID());
                lotHoldReq.setOperationNumber(lotHoldRecordEffectMonitorIssueForPostProcRetCode.get(i).getOperationNumber());
                lotHoldReq.setRelatedLotID(lotHoldRecordEffectMonitorIssueForPostProcRetCode.get(i).getRelatedLotID());
                lotHoldReq.setClaimMemo(lotHoldRecordEffectMonitorIssueForPostProcRetCode.get(i).getClaimMemo());
                //【step-2】 txHoldLotReq
                lotService.sxHoldLotReq(objCommon,lotID,strLotHoldReqList);
            }
        }
        //Return to caller
        result.setReturnCode(retCodeConfig.getSucc());
        return result;
    }

}
