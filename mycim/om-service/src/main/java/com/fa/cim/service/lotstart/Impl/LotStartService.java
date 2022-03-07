package com.fa.cim.service.lotstart.Impl;

import com.fa.cim.annotaion.OmService;
import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.OmCode;
import com.fa.cim.common.utils.*;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.config.RetCodeConfigEx;
import com.fa.cim.dto.*;
import com.fa.cim.lot.LotStbUsageRecycleLimitParams;
import com.fa.cim.method.*;
import com.fa.cim.newcore.bo.durable.CimCassette;
import com.fa.cim.newcore.bo.machine.CimMachine;
import com.fa.cim.newcore.bo.planning.CimProductRequest;
import com.fa.cim.newcore.bo.product.CimControlJob;
import com.fa.cim.newcore.bo.product.CimLot;
import com.fa.cim.newcore.impl.bo.env.StandardProperties;
import com.fa.cim.service.lot.ILotService;
import com.fa.cim.service.lotstart.ILotStartService;
import com.fa.cim.service.processcontrol.IProcessControlService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

import static java.lang.Boolean.FALSE;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/9/8          ********            lightyh                create file
 *
 * @author: lightyh
 * @date: 2020/9/8 17:31
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@OmService
@Slf4j
public class LotStartService implements ILotStartService {

    @Autowired
    private RetCodeConfig retCodeConfig;

    @Autowired
    private RetCodeConfigEx retCodeConfigEx;

    @Autowired
    private IControlJobMethod controlJobMethod;

    @Autowired
    private ILotMethod lotMethod;

    @Autowired
    private IContaminationMethod contaminationMethod;

    @Autowired
    private ICassetteMethod cassetteMethod;

    @Autowired
    private IEquipmentMethod equipmentMethod;

    @Autowired
    private IProductMethod productMethod;

    @Autowired
    private IObjectMethod objectMethod;

    @Autowired
    private IObjectLockMethod objectLockMethod;

    @Autowired
    private IEventMethod eventMethod;

    @Autowired
    private IWaferMethod waferMethod;

    @Autowired
    private IBankMethod bankMethod;

    @Autowired
    private IInterFabMethod interFabMethod;

    @Autowired
    private IProcessControlService processControlService;

    @Autowired
    private ILotService lotService;

    @Override
    public ObjectIdentifier sxNPWLotStartReq(Infos.ObjCommon objCommon, Params.NPWLotStartReqParams params) {
        ObjectIdentifier productID = params.getProductID();
        Integer waferCount = params.getWaferCount();
        String lotType = params.getLotType();
        String subLotType = params.getSubLotType();
        Infos.NewLotAttributes tmpNewLotAttributes = params.getNewLotAttributes();
        ObjectIdentifier cassetteID = tmpNewLotAttributes.getCassetteID();
        //----------------------------------------------------
        // Copy input parameter and use tmpNewLotAttributes
        // inside of this method
        //----------------------------------------------------
        int lotOperationEIcheck = StandardProperties.OM_CARRIER_CHK_EI_FOR_LOT_OPERATION.getIntValue();
        boolean updateControlJobFlag = false;
        Long lockMode = 0L;
        String transferState = null;
        if (lotOperationEIcheck == 0) {
            //-------------------------------
            // Get carrier transfer status
            //-------------------------------
            transferState = cassetteMethod.cassetteTransferStateGet(objCommon, cassetteID);

            /*------------------------------------*/
            /*   Get eqp ID in cassette     */
            /*------------------------------------*/
            Outputs.ObjCassetteEquipmentIDGetOut strCassetteEquipmentIDGetOut = cassetteMethod.cassetteEquipmentIDGet(objCommon, cassetteID);

            //-------------------------------
            // Get required eqp lock mode
            //-------------------------------
            // object_lockMode_Get
            // lockMode = strObjectLockModeGetOut.lockMode;

            //-------------------------------
            // Get required equipment lock mode
            //-------------------------------
            Outputs.ObjLockModeOut strObjectLockModeGetOut;
            Inputs.ObjLockModeIn strObjectLockModeGetIn=new Inputs.ObjLockModeIn();
            strObjectLockModeGetIn.setObjectID           ( strCassetteEquipmentIDGetOut.getEquipmentID());
            strObjectLockModeGetIn.setClassName          ( BizConstant.SP_CLASSNAME_POSMACHINE );
            strObjectLockModeGetIn.setFunctionCategory   ( "OLSTW004" );
            strObjectLockModeGetIn.setUserDataUpdateFlag ( FALSE);

            strObjectLockModeGetOut = objectMethod.objectLockModeGet(
                    objCommon,
                    strObjectLockModeGetIn );

            lockMode = strObjectLockModeGetOut.getLockMode();

            if (CimStringUtils.equals(BizConstant.SP_TRANSSTATE_EQUIPMENTIN, transferState)) {
                updateControlJobFlag = true;
                if (lockMode != BizConstant.SP_EQP_LOCK_MODE_WRITE.intValue()) {
                    // advanced_object_Lock
                    Inputs.ObjAdvanceLockIn strAdvancedObjectLockIn=new Inputs.ObjAdvanceLockIn();

                    // Lock Equipment Main Object
                    List<String> dummySeq=new ArrayList<>();
                    strAdvancedObjectLockIn.setObjectID   (strCassetteEquipmentIDGetOut.getEquipmentID());
                    strAdvancedObjectLockIn.setClassName  (BizConstant.SP_CLASSNAME_POSMACHINE );
                    strAdvancedObjectLockIn.setObjectType (BizConstant.SP_OBJECTLOCK_OBJECTTYPE_MAINOBJECT );
                    strAdvancedObjectLockIn.setLockType   (strObjectLockModeGetOut.getRequiredLockForMainObject());
                    strAdvancedObjectLockIn.setKeyList    (dummySeq);

                    objectLockMethod.advancedObjectLock(objCommon, strAdvancedObjectLockIn );

                    // Lock Equipment LoadCassette Element (Write)
                    List<String> loadCastSeq=new ArrayList<>();
                    loadCastSeq.add(tmpNewLotAttributes.getCassetteID().getValue());
                    strAdvancedObjectLockIn.setObjectID   (strCassetteEquipmentIDGetOut.getEquipmentID());
                    strAdvancedObjectLockIn.setClassName  (BizConstant.SP_CLASSNAME_POSMACHINE );
                    strAdvancedObjectLockIn.setObjectType (BizConstant.SP_OBJECTLOCK_OBJECTTYPE_LOADCASSETTE );
                    strAdvancedObjectLockIn.setLockType   (BizConstant.SP_OBJECTLOCK_LOCKTYPE_WRITE*1L);
                    strAdvancedObjectLockIn.setKeyList     (loadCastSeq);

                    objectLockMethod.advancedObjectLock(
                            objCommon, strAdvancedObjectLockIn );
                } else {
                    // object_Lock
                    objectLockMethod.objectLock(objCommon, CimMachine.class,strCassetteEquipmentIDGetOut.getEquipmentID());
                }
            }
        } else {
            updateControlJobFlag = true;
        }

        //--------------------------------
        //   Lock objects of cassette to be updated
        // for tmpNewLotAttributes.cassetteID
        //--------------------------------
        // object_Lock
        objectLockMethod.objectLock(objCommon, CimCassette.class,tmpNewLotAttributes.getCassetteID());

        if (lotOperationEIcheck == 0) {
            if (!updateControlJobFlag || lockMode != BizConstant.SP_EQP_LOCK_MODE_WRITE.intValue()) {
                //---------------------------------
                //   Get cassette's ControlJobID
                //---------------------------------
                ObjectIdentifier controlJobID = cassetteMethod.cassetteControlJobIDGet(objCommon, cassetteID);
                if (!ObjectIdentifier.isEmptyWithValue(controlJobID)) {
                    updateControlJobFlag = true;
                    if (lockMode != BizConstant.SP_EQP_LOCK_MODE_WRITE.intValue()) {
                        /*------------------------------*/
                        /*   Lock controljob Object     */
                        /*------------------------------*/
                        // object_Lock
                        objectLockMethod.objectLock(objCommon, CimControlJob.class,controlJobID);
                    }
                }
            }
        }
        //--------------------------------
        //   Lock objects of lot to be updated
        //--------------------------------
        List<Infos.NewWaferAttributes> newWaferAttributesList = tmpNewLotAttributes.getNewWaferAttributesList();
        int nLen = CimArrayUtils.getSize(newWaferAttributesList);
        int i = 0;
        for (i = 0; i < nLen; i++){
            Infos.NewWaferAttributes newWaferAttributes = newWaferAttributesList.get(i);
            if (i > 0 && ObjectIdentifier.equalsWithValue(newWaferAttributesList.get(i - 1).getSourceLotID(), newWaferAttributes.getSourceLotID())){
                continue;
            }
            // object_Lock
            objectLockMethod.objectLock(objCommon, CimLot.class,tmpNewLotAttributes.getNewWaferAttributesList().get(i).getSourceLotID());
            if (lotOperationEIcheck == 0){
                //----------------------------------
                //   Check Lot's Control Job ID
                //----------------------------------
                ObjectIdentifier controlJobID = lotMethod.lotControlJobIDGet(objCommon, newWaferAttributes.getSourceLotID());
                if (!ObjectIdentifier.isEmptyWithValue(controlJobID)){
                    throw new ServiceException(new OmCode(retCodeConfig.getLotControlJobidFilled(), newWaferAttributes.getSourceLotID().getValue(), controlJobID.getValue()));
                }
                //----------------------------------
                //  Get InPostProcessFlag of Lot
                //----------------------------------
                Outputs.ObjLotInPostProcessFlagOut objLotInPostProcessFlagOut = lotMethod.lotInPostProcessFlagGet(objCommon, newWaferAttributes.getSourceLotID());
                //----------------------------------------------
                //  If Lot is in post process, returns error
                //----------------------------------------------
                Validations.check(objLotInPostProcessFlagOut.getInPostProcessFlagOfLot(), new OmCode(retCodeConfig.getLotInPostProcess(), newWaferAttributes.getSourceLotID().getValue()));
            }
        }
        if (lotOperationEIcheck == 0 && !CimStringUtils.equals(BizConstant.SP_TRANSSTATE_EQUIPMENTIN, transferState)) {
            //-------------------------------
            // Check carrier transfer status
            //-------------------------------
            String objCassetteTransferStateGetOut = cassetteMethod.cassetteTransferStateGet(objCommon, cassetteID);
            Validations.check(CimStringUtils.equals(BizConstant.SP_TRANSSTATE_EQUIPMENTIN, objCassetteTransferStateGetOut),
                    new OmCode(retCodeConfig.getChangedToEiByOtherOperation(), tmpNewLotAttributes.getCassetteID().getValue()));
        }

        //----------------------
        // Check input parameter
        //----------------------
        List<ObjectIdentifier> cassetteIDSeq = Arrays.asList(cassetteID);
        List<Infos.LotWaferMap> lotWaferMaps = cassetteMethod.cassetteScrapWaferSelectDR(objCommon, cassetteIDSeq);
        int scrapCount = CimArrayUtils.getSize(lotWaferMaps);
        Validations.check(scrapCount > 0, retCodeConfig.getFoundScrap());

        //check if the carrier usage type match the product usage type
        contaminationMethod.carrierProductUsageTypeCheck(productID,ObjectIdentifier.emptyIdentifier(),cassetteID);

        /*-------------------------------*/
        /*   Check SorterJob existence   */
        /*-------------------------------*/
        // waferSorter_sorterJob_CheckForOperation
        Inputs.ObjWaferSorterJobCheckForOperation objWaferSorterJobCheckForOperation = new Inputs.ObjWaferSorterJobCheckForOperation();
        objWaferSorterJobCheckForOperation.setCassetteIDList(cassetteIDSeq);
        objWaferSorterJobCheckForOperation.setOperation(BizConstant.SP_OPERATION_FOR_CAST);
        waferMethod.waferSorterSorterJobCheckForOperation(objCommon, objWaferSorterJobCheckForOperation);
        //----------------------
        // Create Product request here
        //----------------------
        ObjectIdentifier createdProductRequest = productMethod.productRequestForControlLotRelease(objCommon, productID, waferCount, lotType, subLotType);
        //check if the recycle count meet the recycle limit
        waferMethod.recycleCountCheck(objCommon,createdProductRequest,tmpNewLotAttributes, params.getLotType());
        //check contamination
        contaminationMethod.stbCheck(cassetteID,createdProductRequest);
        //----------------------
        // Copy generated lot ID to tmpLotAttributes
        //----------------------
        int nWaferLen = CimArrayUtils.getSize(newWaferAttributesList);
        for (i = 0; i < nWaferLen; i++){
            Infos.NewWaferAttributes newWaferAttributes = newWaferAttributesList.get(i);
            if (ObjectIdentifier.isEmptyWithValue(newWaferAttributes.getNewLotID())){
                newWaferAttributes.setNewLotID(createdProductRequest);
            }
        }
        //----------------------
        // Check input parameter
        //----------------------
        ObjectIdentifier startBankOut = productMethod.productSpecificationStartBankGet(objCommon, productID);
        Outputs.ObjLotParameterForLotGenerationCheckOut checkOut = lotMethod.lotParameterForLotGenerationCheck(objCommon, startBankOut, tmpNewLotAttributes);
        if (CimBooleanUtils.isTrue(checkOut.isWaferIDAssignRequiredFlag())) {
            Outputs.ObjLotWaferIDGenerateOut generateOut = lotMethod.lotWaferIDGenerate(objCommon, tmpNewLotAttributes);
            tmpNewLotAttributes = generateOut.getNewLotAttributes();
        }
        newWaferAttributesList = tmpNewLotAttributes.getNewWaferAttributesList();
        nLen = CimArrayUtils.getSize(newWaferAttributesList);
        for (i = 0; i < nLen; i++){
            Infos.NewWaferAttributes newWaferAttributes = newWaferAttributesList.get(i);
            if (ObjectIdentifier.isEmptyWithValue(newWaferAttributes.getNewWaferID())
                    && !ObjectIdentifier.isEmptyWithValue(newWaferAttributes.getSourceWaferID())){
                newWaferAttributes.setNewWaferID(newWaferAttributes.getSourceWaferID());
            }
        }
        //----------------------
        // Prepare wafer of source lot
        //----------------------
        List<LotStbUsageRecycleLimitParams> lotStbUsageRecycleLimitParamsList = new ArrayList<>();
        newWaferAttributesList = tmpNewLotAttributes.getNewWaferAttributesList();
        for (Infos.NewWaferAttributes attributes : newWaferAttributesList) {
            LotStbUsageRecycleLimitParams limitParams = new LotStbUsageRecycleLimitParams();
            lotStbUsageRecycleLimitParamsList.add(limitParams);
            if (ObjectIdentifier.isEmptyWithValue(attributes.getSourceWaferID())) {
                limitParams.setNewLotFlag(true);
                Outputs.ObjLotWaferCreateOut waferCreateOut = lotMethod.lotWaferCreate(objCommon, attributes.getSourceLotID(), attributes.getNewWaferID().getValue());
                ObjectIdentifier newWaferID = waferCreateOut.getNewWaferID();
                attributes.setSourceWaferID(newWaferID);
                Infos.Wafer strWafer = new Infos.Wafer();
                strWafer.setWaferID(newWaferID);
                strWafer.setSlotNumber(attributes.getNewSlotNumber());
                waferMethod.waferMaterialContainerChange(objCommon, cassetteID, strWafer);
            }else {
                limitParams.setNewLotFlag(false);
                limitParams.setSourceLotID(attributes.getSourceLotID());
            }
        }
        //------------------------------------------------------
        // Check source lot property and lot - bank combination
        //------------------------------------------------------
        bankMethod.bankLotSTBCheck(objCommon, createdProductRequest, tmpNewLotAttributes);

        //------------------------------------------------------------------------
        //   lot STB
        //------------------------------------------------------------------------
        Outputs.ObjLotSTBOut objLotSTBOut = lotMethod.lotSTB(objCommon, createdProductRequest, tmpNewLotAttributes);
        ObjectIdentifier createdLotID = objLotSTBOut.getCreatedLotID();
        //update wafer's recycle count and reset usage count if this is a recycle stb
        waferMethod.lotRecycleWaferCountUpdate(objCommon, objLotSTBOut.getCreatedLotID().getReferenceKey());
        if (updateControlJobFlag) {
            //----------------------
            // Update control Job Info and
            // Machine cassette info if information exist
            //----------------------
            controlJobMethod.controlJobRelatedInfoUpdate(objCommon, Arrays.asList(cassetteID));
        }

        lotMethod.lotNpwStbUpdate(objCommon, createdLotID, lotStbUsageRecycleLimitParamsList, productID);

        //----------------------
        // Update cassette multi lot type
        //----------------------
        cassetteMethod.cassetteMultiLotTypeUpdate(objCommon, cassetteID);

        //#if 1
        //--------------------------------------------------------------------------------------------------
        // UpDate RequiredCassetteCategory
        //--------------------------------------------------------------------------------------------------
        lotMethod.lotCassetteCategoryUpdateForContaminationControl(objCommon,  createdLotID);

        //-----------------------//
        //     Process Hold      //
        //-----------------------//
        processControlService.sxProcessHoldDoActionReq(objCommon, createdLotID, params.getClaimMemo());

        Boolean bEqpMonCntResetFlag = false;
        String eqpMonCntResetPolicy = StandardProperties.OM_AMONT_USED_COUNT_RESET_ON_LOT_START.getValue();
        List<Infos.EqpMonitorWaferUsedCount> eqpMonitorWaferUsedCounts = new ArrayList<>();
        if (CimStringUtils.equals(eqpMonCntResetPolicy,"1")
                || (CimStringUtils.equals(eqpMonCntResetPolicy,"2")
                && CimStringUtils.equals(objLotSTBOut.getLotType(), BizConstant.SP_LOT_TYPE_RECYCLELOT))){
            bEqpMonCntResetFlag = true;
            Inputs.ObjEqpMonitorWaferUsedCountUpdateIn objEqpMonitorWaferUsedCountUpdateIn = new Inputs.ObjEqpMonitorWaferUsedCountUpdateIn();
            objEqpMonitorWaferUsedCountUpdateIn.setLotID(objLotSTBOut.getCreatedLotID());
            objEqpMonitorWaferUsedCountUpdateIn.setAction(BizConstant.SP_EQPMONUSEDCNT_ACTION_RESET);
            eqpMonitorWaferUsedCounts = equipmentMethod.eqpMonitorWaferUsedCountUpdate(objCommon, objEqpMonitorWaferUsedCountUpdateIn);
        }

        //------------------------------------------------------------------------
        //   Make History
        //------------------------------------------------------------------------
        lotMethod.lotWaferLotHistoryPointerUpdate(objCommon, createdLotID);
        newWaferAttributesList = tmpNewLotAttributes.getNewWaferAttributesList();
        for (Infos.NewWaferAttributes attributes : newWaferAttributesList) {
            lotMethod.lotWaferLotHistoryPointerUpdate(objCommon, attributes.getSourceLotID());
            attributes.setNewLotID(objLotSTBOut.getCreatedLotID());
        }
        eventMethod.lotWaferMoveEventMake(objCommon, tmpNewLotAttributes, TransactionIDEnum.CTRL_LOT_STB_REQ.getValue(), params.getClaimMemo());
        Inputs.LotReticleSetChangeEventMakeParams lotReticleSetChangeEventMakeParams = new Inputs.LotReticleSetChangeEventMakeParams();
        lotReticleSetChangeEventMakeParams.setLotID(objLotSTBOut.getCreatedLotID());
        lotReticleSetChangeEventMakeParams.setTransactionID(TransactionIDEnum.CTRL_LOT_STB_REQ.getValue());
        lotReticleSetChangeEventMakeParams.setClaimMemo(params.getClaimMemo());
        eventMethod.lotReticleSetChangeEventMake(objCommon, lotReticleSetChangeEventMakeParams);
        if (bEqpMonCntResetFlag){
            Inputs.ObjEqpMonitorWaferUsedCountUpdateEventMakeParams objEqpMonitorWaferUsedCountUpdateEventMakeParams = new Inputs.ObjEqpMonitorWaferUsedCountUpdateEventMakeParams();
            objEqpMonitorWaferUsedCountUpdateEventMakeParams.setLotID(objLotSTBOut.getCreatedLotID());
            objEqpMonitorWaferUsedCountUpdateEventMakeParams.setTransactionID(TransactionIDEnum.CTRL_LOT_STB_REQ.getValue());
            objEqpMonitorWaferUsedCountUpdateEventMakeParams.setStrEqpMonitorWaferUsedCountList(eqpMonitorWaferUsedCounts);
            objEqpMonitorWaferUsedCountUpdateEventMakeParams.setClaimMemo(params.getClaimMemo());
            eventMethod.eqpMonitorWaferUsedCountUpdateEventMake(objCommon, objEqpMonitorWaferUsedCountUpdateEventMakeParams);
        }
        int lotOperationMoveEventCreation = StandardProperties.OM_EVENT_MAKE_LOT_OPERATION_MOVE_FOR_LOT_START.getIntValue();
        if (lotOperationMoveEventCreation == 1){
            Inputs.LotOperationMoveEventMakeOtherParams lotOperationMoveEventMakeOtherParams = new Inputs.LotOperationMoveEventMakeOtherParams();
            lotOperationMoveEventMakeOtherParams.setClaimMemo(params.getClaimMemo());
            lotOperationMoveEventMakeOtherParams.setCreatedID(objLotSTBOut.getCreatedLotID());
            lotOperationMoveEventMakeOtherParams.setTransactionID(TransactionIDEnum.CTRL_LOT_STB_REQ.getValue());
            eventMethod.lotOperationMoveEventMakeOther(objCommon, lotOperationMoveEventMakeOtherParams);
        }
        return createdLotID;
    }

    @Override
    public void sxWaferAliasSetReq(Infos.ObjCommon objCommon, Params.WaferAliasSetReqParams params) {
        List<Infos.AliasWaferName> aliasWaferNames = params.getAliasWaferNames();
        ObjectIdentifier lotID = params.getLotID();
        Validations.check(CimObjectUtils.isEmpty(aliasWaferNames) || ObjectIdentifier.isEmpty(lotID), retCodeConfig.getInvalidInputParam());

        /*------------------------------------------------------------------------*/
        /*   Get lot / cassette connection                                        */
        /*------------------------------------------------------------------------*/
        lotMethod.lotCassetteGet(objCommon, lotID);

        /*------------------------------------------------------------------------*/
        /*   Change State                                                         */
        /*------------------------------------------------------------------------*/
        lotMethod.lotAliasWaferNameUpdate(objCommon, lotID, params.getAliasWaferNames());
    }

    @Override
    public Results.WaferLotStartCancelReqResult sxWaferLotStartCancelReq(Infos.ObjCommon objCommon, Params.WaferLotStartCancelReqParams waferLotStartCancelReqParams) {
        ObjectIdentifier stbCancelledLotID = waferLotStartCancelReqParams.getStbCancelledLotID();
        List<Infos.NewPreparedLotInfo> inputNewPreparedLotInfoList = waferLotStartCancelReqParams.getNewPreparedLotInfoList();
        Infos.NewLotAttributes inputNewLotAttributes = waferLotStartCancelReqParams.getNewLotAttributes();
        int inputNewPreparedLotInfoListSize = CimArrayUtils.isEmpty(inputNewPreparedLotInfoList) ? 0 : inputNewPreparedLotInfoList.size();


        log.info(String.format("InParam [STBCancelledLotID]%s", stbCancelledLotID));
        // check input STBCancelled lot ID
        log.info("check input STBCancelled lot ID....");
        Validations.check(ObjectIdentifier.isEmptyWithValue(stbCancelledLotID), retCodeConfig.getInvalidInputParam());

        // check OM_LOT_START_CANCEL
        log.info("check OM_LOT_START_CANCEL...");
        String tmpSTBCancelEnv = StandardProperties.OM_LOT_START_CANCEL.getValue();
        log.info(String.format("OM_LOT_START_CANCEL env value ON/OFF. tmpSTBCancelEnv = ", tmpSTBCancelEnv));
        Validations.check(!CimStringUtils.equals(BizConstant.SP_LOT_STBCANCEL_ON, tmpSTBCancelEnv), retCodeConfig.getLotStbCancelOff());

        // check input parameter
        Set<String> tmpSet = new HashSet<>();
        if (!CimArrayUtils.isEmpty(waferLotStartCancelReqParams.getNewPreparedLotInfoList())) {
            Set<String> sourceLotIDSet = new HashSet<>();
            for (Infos.NewPreparedLotInfo newPreparedLotInfo : waferLotStartCancelReqParams.getNewPreparedLotInfoList()) {
                Validations.check(sourceLotIDSet.contains(newPreparedLotInfo.getStbSourceLotID()), retCodeConfig.getDuplicateValuesInInput());
                sourceLotIDSet.add(newPreparedLotInfo.getStbSourceLotID());
            }
        }

        String lotOperationEICheck = StandardProperties.OM_CARRIER_CHK_EI_FOR_LOT_OPERATION.getValue();
        boolean updateControlJobFlag = false;
        long lockMode = 0;

        String objCassetteTransferStateGetOut = null;
        if ("0".equals(lotOperationEICheck)) {
            // 【step1】get carrier transfer status
            objCassetteTransferStateGetOut = cassetteMethod.cassetteTransferStateGet(objCommon, waferLotStartCancelReqParams.getNewLotAttributes().getCassetteID());

            // 【step2】get eqp ID in cassette
            Outputs.ObjCassetteEquipmentIDGetOut objCassetteEquipmentIDGetOut = cassetteMethod.cassetteEquipmentIDGet(objCommon,
                    waferLotStartCancelReqParams.getNewLotAttributes().getCassetteID());
            // 【step3】get required eqp lock mode
            log.info("todo - 【step3】get required eqp lock mode...");
            Inputs.ObjLockModeIn objLockModeIn = new Inputs.ObjLockModeIn(objCassetteEquipmentIDGetOut.getEquipmentID(),
                    BizConstant.SP_CLASSNAME_POSMACHINE, TransactionIDEnum.STB_CANCEL_REQ.getValue(),
                    false);
            Outputs.ObjLockModeOut objLockModeOut = objectMethod.objectLockModeGet(objCommon, objLockModeIn);
            lockMode = objLockModeOut.getLockMode();
            log.info(String.format("lockMode is %s", lockMode));
            if (CimStringUtils.equals(BizConstant.SP_TRANSSTATE_EQUIPMENTIN, objCassetteTransferStateGetOut)) {
                updateControlJobFlag = true;

                if (lockMode != BizConstant.SP_EQP_LOCK_MODE_WRITE) {
                    //【step4】get required eqp advanced lock mode
                    Inputs.ObjAdvanceLockIn objAdvanceLockIn = new Inputs.ObjAdvanceLockIn(objCassetteEquipmentIDGetOut.getEquipmentID(),
                            BizConstant.SP_CLASSNAME_POSMACHINE, BizConstant.SP_OBJECTLOCK_OBJECTTYPE_MAINOBJECT,
                            objLockModeOut.getRequiredLockForMainObject(), null);
                    objectLockMethod.advancedObjectLock(objCommon, objAdvanceLockIn);

                    //【step5】Lock eqp LoadCassette Element (Write)
                    List<String> keySeq = new ArrayList<>();
                    keySeq.add(waferLotStartCancelReqParams.getNewLotAttributes().getCassetteID().getValue());

                    Inputs.ObjAdvanceLockIn objAdvanceLockIn1 = new Inputs.ObjAdvanceLockIn(objCassetteEquipmentIDGetOut.getEquipmentID(),
                            BizConstant.SP_CLASSNAME_POSMACHINE, BizConstant.SP_OBJECTLOCK_OBJECTTYPE_LOADCASSETTE,
                            BizConstant.SP_OBJECT_LOCK_TYPE_WRITE, keySeq);
                    objectLockMethod.advancedObjectLock(objCommon, objAdvanceLockIn1);
                } else /*end if lockMode != SP_EQP_LOCK...*/ {
                    //【step6】lock pmcmg object
                    objectLockMethod.objectLock(objCommon, CimMachine.class, objCassetteEquipmentIDGetOut.getEquipmentID());
                }
            }
        }

        //【step7】object lock for cassette
        objectLockMethod.objectLock(objCommon, CimCassette.class, waferLotStartCancelReqParams.getNewLotAttributes().getCassetteID());
        if ("0".equals(lotOperationEICheck)) {
            if (!updateControlJobFlag || lockMode != BizConstant.SP_OBJECT_LOCK_TYPE_WRITE) {
                //【step8】get cassette's controlJobID - txWaferLotStartCancelReq.cpp line: 305 -337
                log.info("【step8】Done - get cassette's controlJobID...");
                ObjectIdentifier controlJobRetCode = cassetteMethod.cassetteControlJobIDGet(objCommon, waferLotStartCancelReqParams.getNewLotAttributes().getCassetteID());

                if (!ObjectIdentifier.isEmptyWithValue(controlJobRetCode)) {
                    log.info("", "cassette's controlJobID isn't blank");
                    updateControlJobFlag = true;

                    if (lockMode != BizConstant.SP_OBJECT_LOCK_TYPE_WRITE) {
                        //【step9】lock controlJob Object - txWaferLotStartCancelReq.cpp line: 321 -337
                        log.info("【step9】Done-lock controlJob Object...");
                        objectLockMethod.objectLock(objCommon, CimControlJob.class, controlJobRetCode);
                    }
                }
            }
        }

        //【step10】Object Lock for STB Cancelled lot
        objectLockMethod.objectLock(objCommon, CimLot.class, stbCancelledLotID);

        //【step11】Check InterFabXferPlan Existence
        Outputs.ObjLotCurrentOperationInfoGetDROut out = lotMethod.lotCurrentOperationInfoGetDR(objCommon, stbCancelledLotID);


        //【step12】 get interFabXfer plan list
        Inputs.ObjInterFabXferPlanListGetDRIn objInterFabXferPlanListGetDRIn = new Inputs.ObjInterFabXferPlanListGetDRIn();
        Infos.InterFabLotXferPlanInfo strInterFabLotXferPlanInfo = new Infos.InterFabLotXferPlanInfo();
        objInterFabXferPlanListGetDRIn.setStrInterFabLotXferPlanInfo(strInterFabLotXferPlanInfo);
        strInterFabLotXferPlanInfo.setLotID(stbCancelledLotID);
        strInterFabLotXferPlanInfo.setSeqNo(0);
        strInterFabLotXferPlanInfo.setOriginalRouteID(out.getMainPDID());
        strInterFabLotXferPlanInfo.setOriginalOpeNumber(out.getOpeNo());
        Outputs.ObjInterFabXferPlanListGetDROut objInterFabXferPlanListGetDROutRetCode = null;
        try {
            objInterFabXferPlanListGetDROutRetCode = interFabMethod.interFabXferPlanListGetDR(objCommon, objInterFabXferPlanListGetDRIn);
        } catch (ServiceException e) {
            objInterFabXferPlanListGetDROutRetCode = e.getData(Outputs.ObjInterFabXferPlanListGetDROut.class);
            if (!Validations.isEquals( retCodeConfig.getInterfabNotFoundXferPlan(),e.getCode())) {
                throw e;
            }
        }
        Integer xferLen = CimArrayUtils.getSize(objInterFabXferPlanListGetDROutRetCode.getStrInterFabLotXferPlanInfoSeq());
        if( xferLen != 0 ) {
            Validations.check(retCodeConfigEx.getInterfabBranchCancelError());
        }


        //【step13】check bonding group
        String bondingGroupID = lotMethod.lotBondingGroupIDGetDR(objCommon, stbCancelledLotID);
        Validations.check(!CimStringUtils.isEmpty(bondingGroupID), new OmCode(retCodeConfig.getLotHasBondingGroup(), stbCancelledLotID.getValue(), bondingGroupID));
        if ("0".equals(lotOperationEICheck)) {
            //【step14】check lot's control job ID - txWaferLotStartCancelReq.cpp line:430 - 451
            log.info("【step14】Done - check lot's control job ID...");
            ObjectIdentifier objLotControlJobIDGetOut=lotMethod.lotControlJobIDGet(objCommon, stbCancelledLotID);
            if (ObjectIdentifier.isNotEmpty(objLotControlJobIDGetOut)){
                log.info("objLotControlJobIDGetOut.controlJobID != 0");
                Validations.check(new OmCode(retCodeConfig.getLotControlJobidFilled()
                        , ObjectIdentifier.fetchValue(stbCancelledLotID), ObjectIdentifier.fetchValue(objLotControlJobIDGetOut)));
            }

            //【step15】get inPostProcessFlag of lot
            Outputs.ObjLotInPostProcessFlagOut objLotInPostProcessFlagGetOut = lotMethod.lotInPostProcessFlagGet(objCommon, stbCancelledLotID);
            Validations.check(objLotInPostProcessFlagGetOut.getInPostProcessFlagOfLot(), new OmCode(retCodeConfig.getLotInPostProcess(), ObjectIdentifier.fetchValue(stbCancelledLotID)));
            // if lot is in post process, returns error
        }

        if ("0".equals(lotOperationEICheck) && !CimStringUtils.equals(BizConstant.SP_TRANSSTATE_EQUIPMENTIN, objCassetteTransferStateGetOut)) {
            //【step16】check carrier transfer status
            String cassetteTransferStateGetOut = cassetteMethod.cassetteTransferStateGet(objCommon, inputNewLotAttributes.getCassetteID());
            Validations.check(CimStringUtils.equals(BizConstant.SP_TRANSSTATE_EQUIPMENTIN, cassetteTransferStateGetOut), retCodeConfig.getChangedToEiByOtherOperation());
        }

        //【step17】get STB Cancel information
        log.info("call lot Infos.STBCancelInfo get()...");
        Outputs.ObjLotSTBCancelInfoOut objLotSTBCancelInfoOut = lotMethod.lotSTBCancelInfoGetDR(objCommon, stbCancelledLotID);

        //set collected STB cancel information to temporary structure
        log.info("set collected STB cancel information to temporary structure....");
        Infos.STBCancelledLotInfo tmpSTBCancelledLotInfo = objLotSTBCancelInfoOut.getStbCancelledLotInfo();
        List<Infos.NewPreparedLotInfo> tmpNewPreparedLotInfoList = objLotSTBCancelInfoOut.getNewPreparedLotInfoList();
        List<Infos.STBCancelWaferInfo> tmpSTBCancelWaferInfoList = objLotSTBCancelInfoOut.getStbCancelWaferInfoList();
        //check if input cassetteID is STB cancelled lot's cassette
        log.info("check if input cassetteID is STB cancelled lot's cassette...");
        Validations.check(!ObjectIdentifier.equalsWithValue(tmpSTBCancelledLotInfo.getCassetteID(), inputNewLotAttributes.getCassetteID()), retCodeConfig.getInvalidInputCassetteId());

        //check if STB cancelled wafer's STB source lots info exist
        log.info("check if STB cancelled wafer's STB source lots info exist...");
        int cassetteWaferSize = CimArrayUtils.isEmpty(tmpSTBCancelWaferInfoList) ? 0 : tmpSTBCancelWaferInfoList.size();
        int newPreparedLotSize = CimArrayUtils.isEmpty(tmpNewPreparedLotInfoList) ? 0 : tmpNewPreparedLotInfoList.size();
        for (int i = 0; i < cassetteWaferSize; i++) {
            Infos.STBCancelWaferInfo stbCancelWaferInfo = tmpSTBCancelWaferInfoList.get(i);
            if (stbCancelledLotID.equals(stbCancelWaferInfo.getCurrentLotID())) {
                Validations.check(CimStringUtils.isEmpty(stbCancelWaferInfo.getStbSourceLotID()),
                        new OmCode(retCodeConfig.getWaferNoStbSourceLotInfo(), tmpSTBCancelWaferInfoList.get(i).getWaferID().getValue()));

                boolean stbInfoFoundFlag = false;
                for (int j = 0; j < newPreparedLotSize; j++) {
                    Infos.NewPreparedLotInfo newPreparedLotInfo = tmpNewPreparedLotInfoList.get(j);
                    if (CimStringUtils.equals(stbCancelWaferInfo.getStbSourceLotID(), newPreparedLotInfo.getStbSourceLotID())) {
                        log.info(String.format("STB Source lot info is found for wafer which waferID:%s",stbCancelWaferInfo.getWaferID()));
                        stbInfoFoundFlag = true;
                        break;
                    }
                }
                Validations.check(!stbInfoFoundFlag, retCodeConfig.getStbSourceLotInfoNotExist());
            }
        }
        //set STB Cancel Information to strSTBCancelInfoSeq to call productRequest_release_BySTBCancel
        log.info("Set STB Cancel information to strSTBCancelInfoSeq to call productRequest_release_BySTBCancel...");
        int  inputWaferSize = CimArrayUtils.isEmpty(inputNewLotAttributes.getNewWaferAttributesList())
                ? 0 : inputNewLotAttributes.getNewWaferAttributesList().size();
        List<Infos.STBCancelInfo> stbCancelInfoList = new ArrayList<>();
        for (int i = 0; i < newPreparedLotSize; i++) {
            boolean lotFoundFlag = false;
            Infos.NewPreparedLotInfo newPreparedLotInfo = tmpNewPreparedLotInfoList.get(i);
            Infos.NewLotAttributes tmpNewLotAttributes = new Infos.NewLotAttributes();

            for (int j = 0; j < inputNewPreparedLotInfoListSize; j++) {
                Infos.NewPreparedLotInfo inputNewPreparedLotInfo = inputNewPreparedLotInfoList.get(j);
                if (inputNewPreparedLotInfo.getStbSourceLotID().equals(newPreparedLotInfo.getStbSourceLotID())) {
                    log.info(String.format("STB Source lot ID is %s", newPreparedLotInfo.getStbSourceLotID()));
                    log.info(String.format("sourceLotType is %s", inputNewPreparedLotInfo.getLotType()));
                    log.info(String.format("subLotType is %s", inputNewPreparedLotInfo.getSubLotType()));

                    newPreparedLotInfo.setLotType(inputNewPreparedLotInfo.getLotType());
                    newPreparedLotInfo.setSubLotType(inputNewPreparedLotInfo.getSubLotType());
                    lotFoundFlag = true;
                    List<Infos.NewWaferAttributes> newWaferAttributesList = new ArrayList<>();
                    for (int k = 0; k < cassetteWaferSize; k++) {
                        Infos.STBCancelWaferInfo stbCancelWaferInfo = tmpSTBCancelWaferInfoList.get(k);
                        log.info(String.format("round %d - WaferID:%s", k, stbCancelWaferInfo.getWaferID()));
                        if (!ObjectIdentifier.equalsWithValue(stbCancelledLotID, stbCancelWaferInfo.getCurrentLotID())) {
                            continue;
                        }
                        if (CimStringUtils.equals(newPreparedLotInfo.getStbSourceLotID(), stbCancelWaferInfo.getStbSourceLotID())) {
                            log.info(String.format("This wafer has STB Source lot info which WaferID is %s", stbCancelWaferInfo.getWaferID()));
                            Infos.NewWaferAttributes newWaferAttributes = new Infos.NewWaferAttributes();

                            ObjectIdentifier sourceWaferID = stbCancelWaferInfo.getWaferID();
                            newWaferAttributes.setSourceWaferID(sourceWaferID);
                            newWaferAttributes.setSourceLotID(stbCancelledLotID);

                            Integer slotNumber = CimNumberUtils.intValue(CimObjectUtils.toString(stbCancelWaferInfo.getSlotNo()));
                            newWaferAttributes.setNewSlotNumber(slotNumber);
                            for (int l = 0; l < inputWaferSize; l++) {
                                Infos.NewWaferAttributes inputNewWaferAttributes = inputNewLotAttributes.getNewWaferAttributesList().get(l);
                                if (ObjectIdentifier.equalsWithValue(sourceWaferID, inputNewWaferAttributes.getSourceWaferID())
                                        && !ObjectIdentifier.isEmptyWithValue(inputNewWaferAttributes.getNewWaferID())) {
                                    log.info(String.format("newWaferID is specified for the wafer.sourceWaferID:%s", sourceWaferID));
                                    newWaferAttributes.setNewWaferID(inputNewWaferAttributes.getNewWaferID());
                                }
                            }

                            newWaferAttributesList.add(newWaferAttributes);
                            tmpNewLotAttributes.setNewWaferAttributesList(newWaferAttributesList);
                        }
                    }

                    tmpNewLotAttributes.setCassetteID(inputNewLotAttributes.getCassetteID());

                    Integer newWaferAttributesListSize = CimArrayUtils.getSize(newWaferAttributesList);
                    Long waferCount = Long.parseLong(newWaferAttributesListSize.toString());
                    newPreparedLotInfo.setWaferCount(waferCount);

                    Infos.STBCancelInfo stbCancelInfo = new Infos.STBCancelInfo();
                    stbCancelInfo.setNewPreparedLotInfo(newPreparedLotInfo);
                    stbCancelInfo.setNewLotAttributes(tmpNewLotAttributes);
                    stbCancelInfoList.add(stbCancelInfo);
                }
            }

            Validations.check(!lotFoundFlag, retCodeConfig.getStbSourceLotInfoNotInInput());
        }

        //【step18】Set STB Cancel information to strSTBCancelInfoSeq to call productRequest_Release_BySTBCancel
        int createdLotSize = CimArrayUtils.getSize(stbCancelInfoList);
        log.info(String.format("created lot size is %d", createdLotSize));
        for (int i = 0; i < createdLotSize; i++) {
            Infos.STBCancelInfo stbCancelInfo = stbCancelInfoList.get(i);
            //Prepare Product Request for New Prepared lot
            log.info("call productRequest_Release_BySTBCancel()...");
            ObjectIdentifier productID = stbCancelInfo.getNewPreparedLotInfo().getProductID();
            ObjectIdentifier bankID = tmpSTBCancelledLotInfo.getRouteStartBankID();
            String lotType = stbCancelInfo.getNewPreparedLotInfo().getLotType();
            String subLotType = stbCancelInfo.getNewPreparedLotInfo().getSubLotType();
            Long productCount = stbCancelInfo.getNewPreparedLotInfo().getWaferCount();
            Inputs.ObjProductRequestReleaseBySTBCancelIn stbCancelIn = new Inputs.ObjProductRequestReleaseBySTBCancelIn(productID,
                    bankID, lotType, subLotType, Integer.parseInt(productCount.toString()));
            Outputs.ObjProductRequestReleaseBySTBCancelOut productRequestReleaseOut = productMethod.productRequestReleaseBySTBCancel(objCommon, stbCancelIn);
            stbCancelInfo.setProductReqID(productRequestReleaseOut.getCreatedProductRequestID());

            // copy generated new lot ID to structure
            log.info("copy generated New lot ID to structure...");
            int waferSize = CimArrayUtils.getSize(stbCancelInfo.getNewLotAttributes().getNewWaferAttributesList());
            for (int j = 0; j < waferSize; j++) {
                Infos.NewWaferAttributes newWaferAttributes = stbCancelInfo.getNewLotAttributes().getNewWaferAttributesList().get(j);
                newWaferAttributes.setNewLotID(productRequestReleaseOut.getCreatedProductRequestID());
            }

            //【step19】check input parameter
            log.info("call lot_parameterForLotGeneration_Check()");

            //objLotParameterForLotGenerationCheckOut.setWaferIDAssignRequiredFlag(false);
            //checkOut.setObject(objLotParameterForLotGenerationCheckOut);
            Outputs.ObjLotParameterForLotGenerationCheckOut checkOut= lotMethod.lotParameterForLotGenerationCheck(objCommon,tmpSTBCancelledLotInfo.getRouteStartBankID(),
                    stbCancelInfo.getNewLotAttributes());
            if (checkOut.isWaferIDAssignRequiredFlag()) {
                log.info("bWaferIDAssignRequred == TRUE");
                //【step20】generate lot-wafer ID
                Outputs.ObjLotWaferIDGenerateOut generateOut = lotMethod.lotWaferIDGenerate(objCommon, stbCancelInfo.getNewLotAttributes());
                stbCancelInfo.setNewLotAttributes(generateOut.getNewLotAttributes());
            }
            waferSize = CimArrayUtils.getSize(stbCancelInfo.getNewLotAttributes().getNewWaferAttributesList());
            for (int j = 0; j < waferSize; j++) {
                Infos.NewWaferAttributes newWaferAttributes = stbCancelInfo.getNewLotAttributes().getNewWaferAttributesList().get(j);
                if (ObjectIdentifier.isEmpty(newWaferAttributes.getNewWaferID())) {
                    newWaferAttributes.setNewWaferID(newWaferAttributes.getSourceWaferID());
                }
            }
        }

        //【step21】check STB Cancel Condition dor cassette, lot, wafer, bank
        log.info("【step21】check STB Cancel Condtion dor cassette, lot, wafer, bank...");
        Inputs.ObjLotSTBCancelCheckIn checkIn = new Inputs.ObjLotSTBCancelCheckIn(stbCancelledLotID, tmpSTBCancelledLotInfo.getRouteStartBankID(), stbCancelInfoList);
        lotMethod.lotSTBCancelCheck(objCommon, checkIn);


        //【step22】STB Cancel
        Inputs.ObjLotSTBCancelIn lotSTBCancelIn = new Inputs.ObjLotSTBCancelIn(stbCancelledLotID, tmpSTBCancelledLotInfo.getRouteStartBankID(), stbCancelInfoList);
        Outputs.ObjLotSTBCancelOut lotSTBCancelOut = lotMethod.lotSTBCancel(objCommon, lotSTBCancelIn);

        List<Infos.PreparedLotInfo> preparedLotInfoList = lotSTBCancelOut.getPreparedLotInfoList();
        int preparedLotSize = CimArrayUtils.isEmpty(preparedLotInfoList) ? 0 : preparedLotInfoList.size();
        if (updateControlJobFlag) {
            // update control job info and pmcmg cassette info if information exist
            List<ObjectIdentifier> tmpCassetteIDList = new ArrayList<>();
            tmpCassetteIDList.add(inputNewLotAttributes.getCassetteID());
            controlJobMethod.controlJobRelatedInfoUpdate(objCommon, tmpCassetteIDList);
        }

        //【step24】cassette_multiLotType_Update
        cassetteMethod.cassetteMultiLotTypeUpdate(objCommon, inputNewLotAttributes.getCassetteID());
        /*----------------------------------------------------------------------------*/
        /*   Prepare structure for making history                                     */
        /*----------------------------------------------------------------------------*/
        Infos.NewLotAttributes newLotAttributesHstry = new Infos.NewLotAttributes();
        List<Infos.NewWaferAttributes> newWaferAttributesList = new ArrayList<>();
        for (Infos.STBCancelInfo stbCancelInfo : stbCancelInfoList) {
            for (Infos.NewWaferAttributes waferAttributes : stbCancelInfo.getNewLotAttributes().getNewWaferAttributesList()) {
                Infos.NewWaferAttributes newWaferAttributes = new Infos.NewWaferAttributes();
                for (Infos.PreparedLotInfo preparedLotInfo : lotSTBCancelOut.getPreparedLotInfoList()) {
                    if (ObjectIdentifier.equalsWithValue(waferAttributes.getNewLotID(), preparedLotInfo.getLotID())){
                        newWaferAttributes.setSourceLotID(preparedLotInfo.getLotID());
                    }
                }
                newWaferAttributes.setSourceWaferID(waferAttributes.getNewWaferID());
                newWaferAttributes.getSourceWaferID().setReferenceKey(waferAttributes.getSourceWaferID().getReferenceKey());
                newWaferAttributes.setNewWaferID(waferAttributes.getSourceWaferID());
                newWaferAttributes.setNewLotID(waferAttributes.getSourceLotID());
                newWaferAttributes.setNewSlotNumber(waferAttributes.getNewSlotNumber());
                newWaferAttributesList.add(newWaferAttributes);
            }
        }
        newLotAttributesHstry.setNewWaferAttributesList(newWaferAttributesList);
        //【step25】make history
        log.info("【step25】make history");
        log.info("【step25-1】call lot_waferLotHistoryPointer_Update() for STB cancelled lot");
        lotMethod.lotWaferLotHistoryPointerUpdate(objCommon, stbCancelledLotID);
        //call lot_waferLotHistoryPointer_Update() for new prepared lots
        if (!CimObjectUtils.isEmpty(lotSTBCancelOut.getPreparedLotInfoList())){
            for (Infos.PreparedLotInfo preparedLotInfo : lotSTBCancelOut.getPreparedLotInfoList()) {
                lotMethod.lotWaferLotHistoryPointerUpdate(objCommon, preparedLotInfo.getLotID());
            }
        }
        log.info("【step25-2】Make wafer move event");
        //Make wafer move event
        eventMethod.lotWaferMoveEventMake(objCommon, newLotAttributesHstry,TransactionIDEnum.STB_CANCEL_REQ.getValue(), waferLotStartCancelReqParams.getClaimMemo());
        //【step26】Set created lot information to output structure;
        log.info("【step26】Set created lot information to output structure...");
        Results.WaferLotStartCancelReqResult waferLotStartCancelReqObj = new Results.WaferLotStartCancelReqResult();
        waferLotStartCancelReqObj.setBankID(tmpSTBCancelledLotInfo.getRouteStartBankID());
        waferLotStartCancelReqObj.setPreparedLotInfoList(lotSTBCancelOut.getPreparedLotInfoList());
        return waferLotStartCancelReqObj;
    }

    @Override
    public Results.WaferLotStartReqResult sxWaferLotStartReq(Infos.ObjCommon objCommon, ObjectIdentifier productRequestID, Infos.NewLotAttributes newLotAttributes, String claimMemo) {
        Results.WaferLotStartReqResult waferLotStartReqResult = new Results.WaferLotStartReqResult();
        //----------------------------------------------------
        // Copy input parameter and use tmpNewLotAttributes
        // inside of this method
        //----------------------------------------------------
        // step1 - Copy input parameter and use tmpNewLotAttributes inside of this method
        log.info("step1 - Copy input parameter and use tmpNewLotAttributes inside of this method");
        Infos.NewLotAttributes tmpNewLotAttributes = newLotAttributes;
        ObjectIdentifier cassetteID = tmpNewLotAttributes.getCassetteID();
        List<Infos.NewWaferAttributes> newWaferAttributesList = tmpNewLotAttributes.getNewWaferAttributesList();
        //--------------------------------
        // Lock objects of product request
        //--------------------------------
        //step 2 - Lock objects of product request
        log.info("step 2 - Lock objects of product request");
        objectLockMethod.objectLock(objCommon, CimProductRequest.class, productRequestID);

        String lotOperationEIcheckStr = StandardProperties.OM_CARRIER_CHK_EI_FOR_LOT_OPERATION.getValue();
        int lotOperationEIcheck = CimStringUtils.isEmpty(lotOperationEIcheckStr) ? 0 : Integer.parseInt(lotOperationEIcheckStr);
        boolean updateControlJobFlag = false;
        int lockMode = 0;
        String cassetteTransferStateResult = null;
        if (0 == lotOperationEIcheck){
            //-------------------------------
            // Get carrier transfer status
            //-------------------------------
            //step 3 -  Get carrier transfer status
            log.info("step 3 -  Get carrier transfer status");
            cassetteTransferStateResult = cassetteMethod.cassetteTransferStateGet(objCommon, cassetteID);
            /*------------------------------------*/
            /*   Get equipment ID in Cassette     */
            /*------------------------------------*/
            //step 4 -  Get eqp ID in cassette
            log.info("step 4 -  Get eqp ID in cassette");
            Outputs.ObjCassetteEquipmentIDGetOut cassetteEquipmentIDResult = cassetteMethod.cassetteEquipmentIDGet(objCommon, cassetteID);
            //-------------------------------
            // Get required equipment lock mode
            //-------------------------------
            //step 5 - Lock Macihne object
            log.info("step 5 - Lock Macihne object");
            Inputs.ObjLockModeIn objLockModeIn = new Inputs.ObjLockModeIn();
            objLockModeIn.setObjectID(cassetteEquipmentIDResult.getEquipmentID());
            objLockModeIn.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
            objLockModeIn.setFunctionCategory(objCommon.getTransactionID());
            objLockModeIn.setUserDataUpdateFlag(false);
            Outputs.ObjLockModeOut objLockModeOut = objectMethod.objectLockModeGet(objCommon, objLockModeIn);

            lockMode = objLockModeOut.getLockMode().intValue();
            if (CimStringUtils.equals(cassetteTransferStateResult, BizConstant.SP_TRANSSTATE_EQUIPMENTIN)){
                updateControlJobFlag = true;
                if ( lockMode != BizConstant.SP_EQP_LOCK_MODE_WRITE ) {
                    Inputs.ObjAdvanceLockIn objAdvancedObjectLockIn = new Inputs.ObjAdvanceLockIn();
                    objAdvancedObjectLockIn.setObjectID(cassetteEquipmentIDResult.getEquipmentID());
                    objAdvancedObjectLockIn.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
                    objAdvancedObjectLockIn.setObjectType(BizConstant.SP_OBJECTLOCK_OBJECTTYPE_MAINOBJECT);
                    objAdvancedObjectLockIn.setLockType(objLockModeOut.getRequiredLockForMainObject());
                    objectLockMethod.advancedObjectLock(objCommon, objAdvancedObjectLockIn);

                    // Lock Equipment LoadCassette Element (Write)
                    List<String> loadCastSeq = new ArrayList<>();
                    loadCastSeq.add(ObjectIdentifier.fetchValue(cassetteID));
                    Inputs.ObjAdvanceLockIn objAdvanceLockIn = new Inputs.ObjAdvanceLockIn();
                    objAdvanceLockIn.setObjectID(cassetteEquipmentIDResult.getEquipmentID());
                    objAdvanceLockIn.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
                    objAdvanceLockIn.setObjectType(BizConstant.SP_OBJECTLOCK_OBJECTTYPE_LOADCASSETTE);
                    objAdvanceLockIn.setLockType(Long.valueOf(BizConstant.SP_OBJECTLOCK_LOCKTYPE_WRITE));
                    objAdvanceLockIn.setKeyList(loadCastSeq);
                    objectLockMethod.advancedObjectLock(objCommon, objAdvanceLockIn);

                } else {
                    /*--------------------------------*/
                    /*   Lock Macihne object          */
                    /*--------------------------------*/
                    objectLockMethod.objectLock(objCommon, CimMachine.class, cassetteEquipmentIDResult.getEquipmentID());
                }
            }
        }
        //--------------------------------
        //   Lock objects of cassette to be updated
        // for tmpNewLotAttributes.cassetteID
        //--------------------------------
        log.info("Lock objects of cassette to be updated");
        //D6000455 objObject_Lock_out strObject_Lock_out;
        objectLockMethod.objectLock(objCommon, CimCassette.class, cassetteID);

        //step 6 - Lock objects of cassette to be updated for tmpNewLotAttributes.cassetteID
        log.info("step 6 - Lock objects of cassette to be updated for tmpNewLotAttributes.cassetteID");
        if (lotOperationEIcheck == 0){
            if (!updateControlJobFlag || lockMode != BizConstant.SP_EQP_LOCK_MODE_WRITE){
                //---------------------------------
                //   Get Cassette's ControlJobID
                //---------------------------------
                //step 7 -  Get cassette's ControlJobID
                log.info("step 7 -  Get cassette's ControlJobID");
                ObjectIdentifier cassetteControlJobIDResult = cassetteMethod.cassetteControlJobIDGet(objCommon, cassetteID);
                if (!ObjectIdentifier.isEmptyWithValue(cassetteControlJobIDResult)){
                    updateControlJobFlag = true;
                    if (lockMode != BizConstant.SP_EQP_LOCK_MODE_WRITE){
                        //step 8 - Lock controljob Object
                        log.info("step 8 - Lock controljob Object");
                        /*------------------------------*/
                        /*   Lock ControlJob Object     */
                        /*------------------------------*/
                        objectLockMethod.objectLock(objCommon, CimControlJob.class, cassetteControlJobIDResult);
                    }
                }
            }
        }
        // step 9 - Lock objects of source lot
        List<Infos.NewWaferAttributes> tmpNewWaferAttributesList = newWaferAttributesList;
        int nLen = CimArrayUtils.getSize(tmpNewWaferAttributesList);
        for (int i = 0; i < nLen; i++){
            if (i > 0 && ObjectIdentifier.equalsWithValue(tmpNewWaferAttributesList.get(i).getSourceLotID(), tmpNewWaferAttributesList.get(i - 1).getSourceLotID())){
                continue;
            }
            objectLockMethod.objectLock(objCommon, CimLot.class, newWaferAttributesList.get(i).getSourceLotID());

            if (lotOperationEIcheck == 0){
                // step 9 - Check lot's Control Job ID
                log.info("step 9 - Check lot's Control Job ID");
                ObjectIdentifier lotControlJobIDResult = lotMethod.lotControlJobIDGet(objCommon, tmpNewWaferAttributesList.get(i).getSourceLotID());
                Validations.check(!ObjectIdentifier.isEmptyWithValue(lotControlJobIDResult), new OmCode(retCodeConfig.getLotControlJobidFilled(), tmpNewWaferAttributesList.get(i).getSourceLotID().getValue(),
                        ObjectIdentifier.fetchValue(lotControlJobIDResult)));
                //----------------------------------
                //  Get InPostProcessFlag of Lot
                //----------------------------------
                // step 10 - Get InPostProcessFlag of lot
                log.info("step 10 - Get InPostProcessFlag of lot");
                Outputs.ObjLotInPostProcessFlagOut lotInPostProcessFlagResult = lotMethod.lotInPostProcessFlagGet(objCommon, tmpNewWaferAttributesList.get(i).getSourceLotID());
                //----------------------------------------------
                //  If Lot is in post process, returns error
                //----------------------------------------------
                //step 11 - If lot is in post process, returns error
                log.info("step 11 - If lot is in post process, returns error");
                Validations.check(lotInPostProcessFlagResult.getInPostProcessFlagOfLot(), new OmCode(retCodeConfig.getLotInPostProcess(), tmpNewWaferAttributesList.get(0).getSourceLotID().getValue()));
            }
        }
        if (lotOperationEIcheck == 0 && !CimStringUtils.equals(cassetteTransferStateResult, BizConstant.SP_TRANSSTATE_EQUIPMENTIN)){
            //-------------------------------
            // Check carrier transfer status
            //-------------------------------
            //step 12 -  Check carrier transfer status
            log.info("step 12 -  Check carrier transfer status");
            String cassetteTransferStateResult2 = cassetteMethod.cassetteTransferStateGet(objCommon, cassetteID);
            Validations.check(CimStringUtils.equals(cassetteTransferStateResult2, BizConstant.SP_TRANSSTATE_EQUIPMENTIN),
                    new OmCode(retCodeConfig.getChangedToEiByOtherOperation(), cassetteID.getValue()));
        }
        // step13 - Check input parameter
        log.info("step13 - Check input parameter");
        List<ObjectIdentifier> cassetteIDSeq = new ArrayList<>();
        cassetteIDSeq.add(cassetteID);
        List<Infos.LotWaferMap> lotWaferMaps = cassetteMethod.cassetteScrapWaferSelectDR(objCommon, cassetteIDSeq);
        int scrapCount = CimArrayUtils.getSize(lotWaferMaps);
        Validations.check(scrapCount > 0, retCodeConfig.getFoundScrap());


        // step14 - Check SorterJob existence
        log.info("step14 - Check SorterJob existence");
        //todo sort 做waferStart跳过 waferSorterSorterJobCheckForOperation 验证
        if(!TransactionIDEnum.SORT_ACTION_RPT.getValue().equals(objCommon.getTransactionID())){
            Inputs.ObjWaferSorterJobCheckForOperation objWaferSorterJobCheckForOperation = new Inputs.ObjWaferSorterJobCheckForOperation();
            objWaferSorterJobCheckForOperation.setOperation(BizConstant.SP_OPERATION_FOR_CAST);
            objWaferSorterJobCheckForOperation.setCassetteIDList(cassetteIDSeq);
            waferMethod.waferSorterSorterJobCheckForOperation(objCommon, objWaferSorterJobCheckForOperation);
        }
        //当actioCode=WaferStart此时的TransactionID由替OSRTR005
        // 换成merge->STB_RELEASED_LOT_REQ("OLSTW001")为了增加lotStart的操作记录
        if(objCommon.getTransactionID().equals(TransactionIDEnum.SORT_ACTION_RPT.getValue())) {
            objCommon.setTransactionID(TransactionIDEnum.STB_RELEASED_LOT_REQ.getValue());
        }

        // step15 - get product request detail
        log.info("step15 - get product request detail");
        Outputs.ObjProductRequestGetDetailOut detailOut = productMethod.productRequestGetDetail(objCommon, productRequestID);
        // step16 - check lot parameter for lot generation.
        log.info("step16 - check lot parameter for lot generation.");
        Outputs.ObjLotParameterForLotGenerationCheckOut checkOut = lotMethod.lotParameterForLotGenerationCheck(objCommon,
                detailOut.getProdReqInq().getStartBankID(), tmpNewLotAttributes);
        //check first step's category req
        contaminationMethod.stbCheck(cassetteID,productRequestID);
        //check usage type
        contaminationMethod.carrierProductUsageTypeCheck(detailOut.getProdReqInq().getProductID(),ObjectIdentifier.emptyIdentifier(),cassetteID);

        if (checkOut.isWaferIDAssignRequiredFlag()) {
            // step17 - generate lot waferID
            log.info("step17 - generate lot waferID");
            Outputs.ObjLotWaferIDGenerateOut generateOut = lotMethod.lotWaferIDGenerate(objCommon, tmpNewLotAttributes);
            tmpNewLotAttributes = generateOut.getNewLotAttributes();
        }
        nLen = CimArrayUtils.getSize(newWaferAttributesList);
        for (int i = 0; i < nLen; i++) {
            if (ObjectIdentifier.isEmpty(newWaferAttributesList.get(i).getNewWaferID())
                    && !ObjectIdentifier.isEmpty(newWaferAttributesList.get(i).getSourceWaferID())) {
                newWaferAttributesList.get(i).setNewWaferID(newWaferAttributesList.get(i).getSourceWaferID());
            }
        }
        // step18 - prepare wafer of source lot
        log.info("step18 - prepare wafer of source lot");
        for (int i = 0; i < nLen; i++) {
            if (ObjectIdentifier.isEmpty(newWaferAttributesList.get(i).getSourceWaferID())) {
                Outputs.ObjLotWaferCreateOut objLotWaferCreateOut = lotMethod.lotWaferCreate(objCommon,
                        newWaferAttributesList.get(i).getSourceLotID(), newWaferAttributesList.get(i).getNewWaferID().getValue());
                newWaferAttributesList.get(i).setSourceWaferID(objLotWaferCreateOut.getNewWaferID());
                Infos.Wafer strWafer = new Infos.Wafer();
                strWafer.setWaferID(objLotWaferCreateOut.getNewWaferID());
                strWafer.setSlotNumber(newWaferAttributesList.get(i).getNewSlotNumber());
                waferMethod.waferMaterialContainerChange(objCommon, cassetteID, strWafer);
            }
        }
        // step19 - check source lot property and lot -bank combination
        log.info("step19 - check source lot property and lot -bank combination");
        bankMethod.bankLotSTBCheck(objCommon, productRequestID, tmpNewLotAttributes);
        // step 20 - stb lot
        log.info("step 20 - stb lot");
        Outputs.ObjLotSTBOut stbOut = lotMethod.lotSTB(objCommon, productRequestID, tmpNewLotAttributes);
        waferLotStartReqResult.setLotID(stbOut.getCreatedLotID());

        // check if the cast usage type match the product usage type
        ObjectIdentifier LotID = stbOut.getCreatedLotID();
        contaminationMethod.carrierProductUsageTypeCheck(ObjectIdentifier.emptyIdentifier(), LotID, cassetteID);

        //step 21 -  Copy stringified object reference of created lot
        log.info("step 21 -  Copy stringified object reference of created lot");
        nLen = CimArrayUtils.getSize(newWaferAttributesList);
        for (int i = 0; i < nLen; i++) {
            newWaferAttributesList.get(i).setNewLotID(stbOut.getCreatedLotID());
        }
        // step22 - update control job related info and pmcmg cassette info if information exist
        log.info("step22 - update control job related info and pmcmg cassette info if information exist");
        if (updateControlJobFlag) {
            List<ObjectIdentifier> cassetteIDList = new ArrayList<>();
            cassetteIDList.add(cassetteID);
            controlJobMethod.controlJobRelatedInfoUpdate(objCommon, cassetteIDList);
        }
        // step23 - update cassette multiple lot type
        log.info("step23 - update cassette multiple lot type");
        cassetteMethod.cassetteMultiLotTypeUpdate(objCommon, cassetteID);
        // step24 - update required cassette category
        log.info("step24 - update required cassette category");
        lotMethod.lotCassetteCategoryUpdateForContaminationControl(objCommon, stbOut.getCreatedLotID());
        // step25 - process hold
        log.info("step25 - process hold");
        processControlService.sxProcessHoldDoActionReq(objCommon, stbOut.getCreatedLotID(), claimMemo);
        //【add by bear】delete the product request info.
        //  productRequestCore.removeByProductRequestID(productRequestID);

        //lot start hold
        Infos.ProdReqInq prodReqInq = detailOut.getProdReqInq();

        if(!CimObjectUtils.isEmpty(prodReqInq)) {
            //department section reasonCode ！= null, hold lot
            String department = prodReqInq.getDepartment();
            String section = prodReqInq.getSection();
            String reasonCode = prodReqInq.getReasonCode();
            if (!CimStringUtils.isEmpty(department) && !CimStringUtils.isEmpty(section) && !CimStringUtils.isEmpty(reasonCode)) {
                Infos.LotInfoInqFlag lotInfoInqFlag = new Infos.LotInfoInqFlag();
                lotInfoInqFlag.setLotOperationInfoFlag(true);
                Infos.LotInfo lotInfo = lotMethod.lotDBInfoGetDR(objCommon, lotInfoInqFlag, LotID);
                if(!CimObjectUtils.isEmpty(lotInfo) && !CimObjectUtils.isEmpty(lotInfo.getLotOperationInfo())){
                    List<Infos.LotHoldReq> holdReqList = new ArrayList<>();
                    Infos.LotHoldReq lotHoldReq = new Infos.LotHoldReq();
                    lotHoldReq.setHoldType(BizConstant.SP_HOLDTYPE_LOTHOLD);
                    lotHoldReq.setHoldUserID(objCommon.getUser().getUserID());
                    lotHoldReq.setRouteID(lotInfo.getLotOperationInfo().getRouteID());
                    lotHoldReq.setOperationNumber(lotInfo.getLotOperationInfo().getOperationNumber());
                    lotHoldReq.setResponsibleOperationMark(BizConstant.SP_RESPONSIBLEOPERATION_CURRENT);
                    lotHoldReq.setDepartment(department);
                    lotHoldReq.setSection(section);
                    lotHoldReq.setHoldReasonCodeID(new ObjectIdentifier(reasonCode));
                    holdReqList.add(lotHoldReq);
                    lotService.sxHoldLotReq(objCommon, LotID, holdReqList);
                }
            }
        }

        //step 26 - Make History
        log.info("step 26 - Make History");
        if (CimStringUtils.equals(stbOut.getProductType(), BizConstant.SP_PRODTYPE_WAFER)
                || CimStringUtils.equals(stbOut.getProductType(), BizConstant.SP_PRODTYPE_DIE)){
            lotMethod.lotWaferLotHistoryPointerUpdate(objCommon, stbOut.getCreatedLotID());
            for (int i = 0; i < newWaferAttributesList.size(); i++) {
                if (i > 0 && CimStringUtils.equals(newWaferAttributesList.get(i).getSourceLotID().getValue(),
                        newWaferAttributesList.get(i - 1).getSourceLotID().getValue())){
                    continue;
                }
                lotMethod.lotWaferLotHistoryPointerUpdate(objCommon, newWaferAttributesList.get(i).getSourceLotID());
            }
        }
        eventMethod.lotWaferMoveEventMake(objCommon, tmpNewLotAttributes, TransactionIDEnum.STB_RELEASED_LOT_REQ.getValue(), claimMemo);
        Inputs.LotReticleSetChangeEventMakeParams params = new Inputs.LotReticleSetChangeEventMakeParams();
        params.setClaimMemo(claimMemo);
        params.setLotID(stbOut.getCreatedLotID());
        params.setTransactionID(TransactionIDEnum.STB_RELEASED_LOT_REQ.getValue());
        eventMethod.lotReticleSetChangeEventMake(objCommon, params);
        String lotOperationMoveEventCreation = StandardProperties.OM_EVENT_MAKE_LOT_OPERATION_MOVE_FOR_LOT_START.getValue();
        if (CimStringUtils.equals(lotOperationMoveEventCreation, "1")){
            Inputs.LotOperationMoveEventMakeOtherParams lotOperationParams = new Inputs.LotOperationMoveEventMakeOtherParams();
            lotOperationParams.setClaimMemo(claimMemo);
            lotOperationParams.setCreatedID(stbOut.getCreatedLotID());
            lotOperationParams.setTransactionID(TransactionIDEnum.STB_RELEASED_LOT_REQ.getValue());
            eventMethod.lotOperationMoveEventMakeOther(objCommon, lotOperationParams);
        }
        return waferLotStartReqResult;
    }

    @Override
    public void lotStartCheck(Infos.ObjCommon objCommon, ObjectIdentifier productRequestID,
                   Infos.NewLotAttributes newLotAttributes, String claimMemo) {
        //----------------------------------------------------
        // Copy input parameter and use tmpNewLotAttributes
        // inside of this method
        //----------------------------------------------------
        // step1 - Copy input parameter and use tmpNewLotAttributes inside of this method
        log.info("step1 - Copy input parameter and use tmpNewLotAttributes inside of this method");
        Infos.NewLotAttributes tmpNewLotAttributes = newLotAttributes;
        ObjectIdentifier cassetteID = tmpNewLotAttributes.getCassetteID();
        List<Infos.NewWaferAttributes> newWaferAttributesList = tmpNewLotAttributes.getNewWaferAttributesList();
        //--------------------------------
        // Lock objects of product request
        //--------------------------------
        //step 2 - Lock objects of product request
        log.info("step 2 - Lock objects of product request");
        objectLockMethod.objectLock(objCommon, CimProductRequest.class, productRequestID);

        String lotOperationEIcheckStr = StandardProperties.OM_CARRIER_CHK_EI_FOR_LOT_OPERATION.getValue();
        int lotOperationEIcheck = CimStringUtils.isEmpty(lotOperationEIcheckStr) ? 0 : Integer.parseInt(lotOperationEIcheckStr);
        boolean updateControlJobFlag = false;
        int lockMode = 0;
        String cassetteTransferStateResult = null;
        if (0 == lotOperationEIcheck){
            //-------------------------------
            // Get carrier transfer status
            //-------------------------------
            //step 3 -  Get carrier transfer status
            log.info("step 3 -  Get carrier transfer status");
            cassetteTransferStateResult = cassetteMethod.cassetteTransferStateGet(objCommon, cassetteID);
            /*------------------------------------*/
            /*   Get equipment ID in Cassette     */
            /*------------------------------------*/
            //step 4 -  Get eqp ID in cassette
            log.info("step 4 -  Get eqp ID in cassette");
            Outputs.ObjCassetteEquipmentIDGetOut cassetteEquipmentIDResult = cassetteMethod.cassetteEquipmentIDGet(objCommon, cassetteID);
            //-------------------------------
            // Get required equipment lock mode
            //-------------------------------
            //step 5 - Lock Macihne object
            log.info("step 5 - Lock Macihne object");
            Inputs.ObjLockModeIn objLockModeIn = new Inputs.ObjLockModeIn();
            objLockModeIn.setObjectID(cassetteEquipmentIDResult.getEquipmentID());
            objLockModeIn.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
            objLockModeIn.setFunctionCategory(objCommon.getTransactionID());
            objLockModeIn.setUserDataUpdateFlag(false);
            Outputs.ObjLockModeOut objLockModeOut = objectMethod.objectLockModeGet(objCommon, objLockModeIn);

            lockMode = objLockModeOut.getLockMode().intValue();
            if (CimStringUtils.equals(cassetteTransferStateResult, BizConstant.SP_TRANSSTATE_EQUIPMENTIN)){
                updateControlJobFlag = true;
                if ( lockMode != BizConstant.SP_EQP_LOCK_MODE_WRITE ) {
                    Inputs.ObjAdvanceLockIn objAdvancedObjectLockIn = new Inputs.ObjAdvanceLockIn();
                    objAdvancedObjectLockIn.setObjectID(cassetteEquipmentIDResult.getEquipmentID());
                    objAdvancedObjectLockIn.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
                    objAdvancedObjectLockIn.setObjectType(BizConstant.SP_OBJECTLOCK_OBJECTTYPE_MAINOBJECT);
                    objAdvancedObjectLockIn.setLockType(objLockModeOut.getRequiredLockForMainObject());
                    objectLockMethod.advancedObjectLock(objCommon, objAdvancedObjectLockIn);

                    // Lock Equipment LoadCassette Element (Write)
                    List<String> loadCastSeq = new ArrayList<>();
                    loadCastSeq.add(ObjectIdentifier.fetchValue(cassetteID));
                    Inputs.ObjAdvanceLockIn objAdvanceLockIn = new Inputs.ObjAdvanceLockIn();
                    objAdvanceLockIn.setObjectID(cassetteEquipmentIDResult.getEquipmentID());
                    objAdvanceLockIn.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
                    objAdvanceLockIn.setObjectType(BizConstant.SP_OBJECTLOCK_OBJECTTYPE_LOADCASSETTE);
                    objAdvanceLockIn.setLockType(Long.valueOf(BizConstant.SP_OBJECTLOCK_LOCKTYPE_WRITE));
                    objAdvanceLockIn.setKeyList(loadCastSeq);
                    objectLockMethod.advancedObjectLock(objCommon, objAdvanceLockIn);

                } else {
                    /*--------------------------------*/
                    /*   Lock Macihne object          */
                    /*--------------------------------*/
                    objectLockMethod.objectLock(objCommon, CimMachine.class, cassetteEquipmentIDResult.getEquipmentID());
                }
            }
        }
        //--------------------------------
        //   Lock objects of cassette to be updated
        // for tmpNewLotAttributes.cassetteID
        //--------------------------------
        log.info("Lock objects of cassette to be updated");
        //D6000455 objObject_Lock_out strObject_Lock_out;
        objectLockMethod.objectLock(objCommon, CimCassette.class, cassetteID);

        //step 6 - Lock objects of cassette to be updated for tmpNewLotAttributes.cassetteID
        log.info("step 6 - Lock objects of cassette to be updated for tmpNewLotAttributes.cassetteID");
        if (lotOperationEIcheck == 0){
            if (!updateControlJobFlag || lockMode != BizConstant.SP_EQP_LOCK_MODE_WRITE){
                //---------------------------------
                //   Get Cassette's ControlJobID
                //---------------------------------
                //step 7 -  Get cassette's ControlJobID
                log.info("step 7 -  Get cassette's ControlJobID");
                ObjectIdentifier cassetteControlJobIDResult = cassetteMethod.cassetteControlJobIDGet(objCommon, cassetteID);
                if (!ObjectIdentifier.isEmptyWithValue(cassetteControlJobIDResult)){
                    updateControlJobFlag = true;
                    if (lockMode != BizConstant.SP_EQP_LOCK_MODE_WRITE){
                        //step 8 - Lock controljob Object
                        log.info("step 8 - Lock controljob Object");
                        /*------------------------------*/
                        /*   Lock ControlJob Object     */
                        /*------------------------------*/
                        objectLockMethod.objectLock(objCommon, CimControlJob.class, cassetteControlJobIDResult);
                    }
                }
            }
        }
        // step 9 - Lock objects of source lot
        List<Infos.NewWaferAttributes> tmpNewWaferAttributesList = newWaferAttributesList;
        int nLen = CimArrayUtils.getSize(tmpNewWaferAttributesList);
        for (int i = 0; i < nLen; i++){
            if (i > 0 && ObjectIdentifier.equalsWithValue(tmpNewWaferAttributesList.get(i).getSourceLotID(), tmpNewWaferAttributesList.get(i - 1).getSourceLotID())){
                continue;
            }
            objectLockMethod.objectLock(objCommon, CimLot.class, newWaferAttributesList.get(i).getSourceLotID());

            if (lotOperationEIcheck == 0){
                // step 9 - Check lot's Control Job ID
                log.info("step 9 - Check lot's Control Job ID");
                ObjectIdentifier lotControlJobIDResult = lotMethod.lotControlJobIDGet(objCommon, tmpNewWaferAttributesList.get(i).getSourceLotID());
                Validations.check(!ObjectIdentifier.isEmptyWithValue(lotControlJobIDResult), new OmCode(retCodeConfig.getLotControlJobidFilled(), tmpNewWaferAttributesList.get(i).getSourceLotID().getValue(),
                        ObjectIdentifier.fetchValue(lotControlJobIDResult)));
                //----------------------------------
                //  Get InPostProcessFlag of Lot
                //----------------------------------
                // step 10 - Get InPostProcessFlag of lot
                log.info("step 10 - Get InPostProcessFlag of lot");
                Outputs.ObjLotInPostProcessFlagOut lotInPostProcessFlagResult = lotMethod.lotInPostProcessFlagGet(objCommon, tmpNewWaferAttributesList.get(i).getSourceLotID());
                //----------------------------------------------
                //  If Lot is in post process, returns error
                //----------------------------------------------
                //step 11 - If lot is in post process, returns error
                log.info("step 11 - If lot is in post process, returns error");
                Validations.check(lotInPostProcessFlagResult.getInPostProcessFlagOfLot(), new OmCode(retCodeConfig.getLotInPostProcess(), tmpNewWaferAttributesList.get(0).getSourceLotID().getValue()));
            }
        }
        if (lotOperationEIcheck == 0 && !CimStringUtils.equals(cassetteTransferStateResult, BizConstant.SP_TRANSSTATE_EQUIPMENTIN)){
            //-------------------------------
            // Check carrier transfer status
            //-------------------------------
            //step 12 -  Check carrier transfer status
            log.info("step 12 -  Check carrier transfer status");
            String cassetteTransferStateResult2 = cassetteMethod.cassetteTransferStateGet(objCommon, cassetteID);
            Validations.check(CimStringUtils.equals(cassetteTransferStateResult2, BizConstant.SP_TRANSSTATE_EQUIPMENTIN),
                    new OmCode(retCodeConfig.getChangedToEiByOtherOperation(), cassetteID.getValue()));
        }
        // step13 - Check input parameter
        log.info("step13 - Check input parameter");
        List<ObjectIdentifier> cassetteIDSeq = new ArrayList<>();
        cassetteIDSeq.add(cassetteID);
        List<Infos.LotWaferMap> lotWaferMaps = cassetteMethod.cassetteScrapWaferSelectDR(objCommon, cassetteIDSeq);
        int scrapCount = CimArrayUtils.getSize(lotWaferMaps);
        Validations.check(scrapCount > 0, retCodeConfig.getFoundScrap());

        // step15 - get product request detail
        log.info("step15 - get product request detail");
        Outputs.ObjProductRequestGetDetailOut detailOut = productMethod.productRequestGetDetail(objCommon, productRequestID);
        // step16 - check lot parameter for lot generation.
        log.info("step16 - check lot parameter for lot generation.");
        Outputs.ObjLotParameterForLotGenerationCheckOut checkOut = lotMethod.lotParameterForLotGenerationCheck(objCommon,
                detailOut.getProdReqInq().getStartBankID(), tmpNewLotAttributes);
        //check first step's category req
        contaminationMethod.stbCheck(cassetteID,productRequestID);
        /*if (checkOut.isWaferIDAssignRequiredFlag()) {
            // step17 - generate lot waferID
            log.info("step17 - generate lot waferID");
            Outputs.ObjLotWaferIDGenerateOut generateOut = lotMethod.lotWaferIDGenerate(objCommon, tmpNewLotAttributes);
            tmpNewLotAttributes = generateOut.getNewLotAttributes();
        }
        nLen = CimArrayUtils.getSize(newWaferAttributesList);
        for (int i = 0; i < nLen; i++) {
            if (CimObjectUtils.isEmpty(newWaferAttributesList.get(i).getNewWaferID())
                    && !CimObjectUtils.isEmpty(newWaferAttributesList.get(i).getSourceWaferID())) {
                newWaferAttributesList.get(i).setNewWaferID(newWaferAttributesList.get(i).getSourceWaferID());
            }
        }
        // step18 - prepare wafer of source lot
        log.info("step18 - prepare wafer of source lot");
        for (int i = 0; i < nLen; i++) {
            if (CimObjectUtils.isEmpty(newWaferAttributesList.get(i).getSourceWaferID())) {
                Outputs.ObjLotWaferCreateOut objLotWaferCreateOut = lotMethod.lotWaferCreate(objCommon,
                        newWaferAttributesList.get(i).getSourceLotID(), newWaferAttributesList.get(i).getNewWaferID().getValue());
                newWaferAttributesList.get(i).setSourceWaferID(objLotWaferCreateOut.getNewWaferID());
                Infos.Wafer strWafer = new Infos.Wafer();
                strWafer.setWaferID(objLotWaferCreateOut.getNewWaferID());
                strWafer.setSlotNumber(newWaferAttributesList.get(i).getNewSlotNumber());
                waferMethod.waferMaterialContainerChange(objCommon, cassetteID, strWafer);
            }
        }*/
        bankMethod.bankLotSTBCheck(objCommon, productRequestID, tmpNewLotAttributes);
        lotMethod.lotSTBCheck(objCommon,productRequestID,tmpNewLotAttributes);
    }
}