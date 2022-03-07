package com.fa.cim.service.recipe.impl;

import com.fa.cim.annotaion.OmService;
import com.fa.cim.common.constant.BizConstant;
import com.fa.cim.common.constant.TCSReqEnum;
import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.OmCode;
import com.fa.cim.common.utils.*;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.config.RetCodeConfigEx;
import com.fa.cim.dto.*;
import com.fa.cim.method.*;
import com.fa.cim.method.impl.ProcessMethod;
import com.fa.cim.newcore.bo.durable.CimCassette;
import com.fa.cim.newcore.bo.machine.CimMachine;
import com.fa.cim.newcore.bo.product.CimLot;
import com.fa.cim.newcore.impl.bo.env.StandardProperties;
import com.fa.cim.service.recipe.IRecipeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

@OmService
@Slf4j
public class RecipeServiceImpl implements IRecipeService {
    @Autowired
    private RetCodeConfig retCodeConfig;

    @Autowired
    private IEquipmentMethod equipmentMethod;

    @Autowired
    private IMachineRecipeMethod machineRecipeMethod;

    @Autowired
    private ITCSMethod tcsMethod;

    @Autowired
    private IEventMethod eventMethod;

    @Autowired
    private IObjectLockMethod objectLockMethod;

    @Autowired
    private IObjectMethod objectMethod;

    @Autowired
    private IRecipeMethod recipeMethod;

    @Autowired
    private IControlJobMethod controlJobMethod;

    @Autowired
    private RetCodeConfigEx retCodeConfigEx;

    @Autowired
    private ProcessMethod processMethod;

    @Override
    public void sxRecipeDeleteInFileReq(Infos.ObjCommon objCommon, Params.RecipeDeleteInFileReqParams params) {
        Inputs.ObjLockModeIn objLockModeIn = new Inputs.ObjLockModeIn();
        objLockModeIn.setObjectID(params.getEquipmentID());
        objLockModeIn.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
        objLockModeIn.setFunctionCategory(TransactionIDEnum.RECIPE_FILE_DELETION_REQ.getValue());
        objLockModeIn.setUserDataUpdateFlag(false);
        Outputs.ObjLockModeOut objLockModeOut = objectMethod.objectLockModeGet(objCommon, objLockModeIn);


        Long lockMode = objLockModeOut.getLockMode();
        if (lockMode.longValue() != BizConstant.SP_EQP_LOCK_MODE_WRITE.longValue()) {
            log.info("lockMode != SP_EQP_LOCK_MODE_WRITE");

            // Advanced Mode
            // Lock Equipment Main Object
            Inputs.ObjAdvanceLockIn objAdvancedObjectLockIn = new Inputs.ObjAdvanceLockIn();
            objAdvancedObjectLockIn.setObjectID(params.getEquipmentID());
            objAdvancedObjectLockIn.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
            objAdvancedObjectLockIn.setObjectType(BizConstant.SP_OBJECTLOCK_OBJECTTYPE_MAINOBJECT);
            objAdvancedObjectLockIn.setLockType(objLockModeOut.getRequiredLockForMainObject());
            objectLockMethod.advancedObjectLock(objCommon, objAdvancedObjectLockIn);

        } else {
            log.info("lockMode = SP_EQP_LOCK_MODE_WRITE");
            //--------------------------------
            //   Lock objects to be updated
            //--------------------------------
            log.info("Lock objects to be updated");
            objectLockMethod.objectLock(objCommon, CimMachine.class, params.getEquipmentID());
        }


        ObjectIdentifier equipmentID = params.getEquipmentID();
        /*---------------------------------------*/
        /*   Get Eqp's Recipe Body Manage Flag   */
        /*---------------------------------------*/
        Validations.check(!equipmentMethod.equipmentRecipeBodyManageFlagGet(objCommon, equipmentID), retCodeConfig.getEqpRcpflagOff());

        /*------------------------------*/
        /*   Get Eqp's Operation Mode   */
        /*------------------------------*/
        Infos.EqpPortInfo eqpPortInfo = equipmentMethod.equipmentPortInfoGet(objCommon, equipmentID);
        List<Infos.EqpPortStatus> eqpPortStatuses = eqpPortInfo.getEqpPortStatuses();
        Validations.check(CimObjectUtils.isEmpty(eqpPortStatuses), retCodeConfig.getNotFoundPort());
        Validations.check(CimStringUtils.equals(eqpPortStatuses.get(0).getOnlineMode(), BizConstant.SP_EQP_ONLINEMODE_OFFLINE),
                retCodeConfig.getInvalidEquipmentMode(), ObjectIdentifier.fetchValue(equipmentID), eqpPortStatuses.get(0).getOnlineMode());

        /*------------------------------------------------*/
        /*   Set Recipe Deletion Info to Machine Recipe   */
        /*------------------------------------------------*/
        machineRecipeMethod.machineRecipeDeletionInfoSet(objCommon, params.getMachineRecipeID());

        /*----------------------------------*/
        /*   Make RecipeBody Manage Event   */
        /*----------------------------------*/
        //recipeBodyManageEvent_Make
        eventMethod.recipeBodyManageEventMake(objCommon, TransactionIDEnum.RECIPE_FILE_DELETION_REQ.getValue(), equipmentID,
                BizConstant.SP_RCPMANAGEACTION_FILEDELETE, params.getMachineRecipeID(), params.getPhysicalRecipeID(), params.getFileLocation(),
                params.getFileName(), false, params.getClaimMemo());

        /*----------------------------------------------*/
        /*   Send Recipe File Deletion Request to EAP   */
        /*----------------------------------------------*/
        Inputs.SendRecipeDeleteInFileReqIn in = new Inputs.SendRecipeDeleteInFileReqIn();
        in.setClaimMemo(params.getClaimMemo());
        in.setFileLocation(params.getFileLocation());
        in.setEquipmentID(params.getEquipmentID());
        in.setFileName(params.getFileName());
        in.setMachineRecipeID(params.getMachineRecipeID());
        in.setPhysicalRecipeID(params.getPhysicalRecipeID());
        in.setRequestUserID(params.getUser());
        in.setObjCommonIn(objCommon);
        tcsMethod.sendTCSReq(TCSReqEnum.sendRecipeDeleteInFileReq, in);
    }

    @Override
    public void sxRecipeCompareReq(Infos.ObjCommon objCommon, Params.RecipeCompareReqParams params) {

        Inputs.ObjLockModeIn objLockModeIn = new Inputs.ObjLockModeIn();
        objLockModeIn.setObjectID(params.getEquipmentID());
        objLockModeIn.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
        objLockModeIn.setFunctionCategory(TransactionIDEnum.RECIPE_CONFIRMATION_REQ.getValue());
        objLockModeIn.setUserDataUpdateFlag(false);
        Outputs.ObjLockModeOut objLockModeOut = objectMethod.objectLockModeGet(objCommon, objLockModeIn);

        Long lockMode = objLockModeOut.getLockMode();
        if (lockMode.longValue() != BizConstant.SP_EQP_LOCK_MODE_WRITE.longValue()) {
            log.info("lockMode != SP_EQP_LOCK_MODE_WRITE");

            // Advanced Mode
            // Lock Equipment Main Object
            Inputs.ObjAdvanceLockIn objAdvancedObjectLockIn = new Inputs.ObjAdvanceLockIn();
            objAdvancedObjectLockIn.setObjectID(params.getEquipmentID());
            objAdvancedObjectLockIn.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
            objAdvancedObjectLockIn.setObjectType(BizConstant.SP_OBJECTLOCK_OBJECTTYPE_MAINOBJECT);
            objAdvancedObjectLockIn.setLockType(objLockModeOut.getRequiredLockForMainObject());
            objectLockMethod.advancedObjectLock(objCommon, objAdvancedObjectLockIn);

        } else {
            log.info("lockMode = SP_EQP_LOCK_MODE_WRITE");
            //--------------------------------
            //   Lock objects to be updated
            //--------------------------------
            log.info("Lock objects to be updated");
            objectLockMethod.objectLock(objCommon, CimMachine.class, params.getEquipmentID());
        }


        ObjectIdentifier equipmentID = params.getEquipmentID();
        /*---------------------------------------*/
        /*   Get Eqp's Recipe Body Manage Flag   */
        /*---------------------------------------*/
        Validations.check(!equipmentMethod.equipmentRecipeBodyManageFlagGet(objCommon, equipmentID), retCodeConfig.getEqpRcpflagOff());

        /*------------------------------*/
        /*   Get Eqp's Operation Mode   */
        /*------------------------------*/
        Infos.EqpPortInfo eqpPortInfo = equipmentMethod.equipmentPortInfoGet(objCommon, equipmentID);
        List<Infos.EqpPortStatus> eqpPortStatuses = eqpPortInfo.getEqpPortStatuses();
        Validations.check(CimObjectUtils.isEmpty(eqpPortStatuses), retCodeConfig.getNotFoundPort());
        Validations.check(CimStringUtils.equals(eqpPortStatuses.get(0).getOnlineMode(), BizConstant.SP_EQP_ONLINEMODE_OFFLINE),
                retCodeConfig.getInvalidEquipmentMode(), ObjectIdentifier.fetchValue(equipmentID), eqpPortStatuses.get(0).getOnlineMode());

        /*------------------------------*/
        /*   Get Uploaded Recipe Info   */
        /*------------------------------*/
        String physicalRecipeID = params.getPhysicalRecipeID();
        String fileLocation = params.getFileLocation();
        String fileName = params.getFileName();
        boolean formatFlag = params.isFormatFlag();
        if (CimObjectUtils.isEmpty(params.getFileName())) {
            log.info("In-parm's fileName == null...");
            List<Infos.MachineRecipeInfo> machineRecipeInfos = machineRecipeMethod.machineRecipeGetListByEquipment(objCommon, equipmentID);
            for (Infos.MachineRecipeInfo machineRecipeInfo : machineRecipeInfos) {
                if (ObjectIdentifier.equalsWithValue(params.getMachineRecipeID(), machineRecipeInfo.getMachineRecipeID())) {
                    physicalRecipeID = machineRecipeInfo.getPhysicalRecipeID();
                    fileLocation = machineRecipeInfo.getFileLocation();
                    fileName = machineRecipeInfo.getFileName();
                    formatFlag = machineRecipeInfo.getFormatFlag();
                    break;
                }
            }
        }

        /*-----------------------------------------*/
        /*   Send Recipe Confirmation Request to EAP   */
        /*-----------------------------------------*/
        Inputs.SendRecipeCompareReqIn in = new Inputs.SendRecipeCompareReqIn();
        in.setEquipmentID(params.getEquipmentID());
        in.setClaimMemo(params.getClaimMemo());
        in.setFileLocation(fileLocation);
        in.setFileName(fileName);
        in.setFormatFlag(formatFlag);
        in.setMachineRecipeID(params.getMachineRecipeID());
        in.setPhysicalRecipeID(physicalRecipeID);
        in.setRequestUserID(params.getUser());
        in.setObjCommonIn(objCommon);
        tcsMethod.sendTCSReq(TCSReqEnum.sendRecipeCompareReq, in);
    }

    @Override
    public void sxRecipeUploadReq(Infos.ObjCommon objCommon, Params.RecipeUploadReqParams params) {
        ObjectIdentifier equipmentID = params.getEquipmentID();
        ObjectIdentifier machineRecipeID = params.getMachineRecipeID();
        String physicalRecipeID = params.getPhysicalRecipeID();
        String fileName = params.getFileName();
        String fileLocation = params.getFileLocation();

        log.info("【step1】: lock mode");
        Inputs.ObjLockModeIn objLockModeIn = new Inputs.ObjLockModeIn();
        objLockModeIn.setObjectID(equipmentID);
        objLockModeIn.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
        objLockModeIn.setFunctionCategory(objCommon.getTransactionID());
        objLockModeIn.setUserDataUpdateFlag(false);
        Outputs.ObjLockModeOut objLockModeOut = objectMethod.objectLockModeGet(objCommon, objLockModeIn);

        Long lockMode = objLockModeOut.getLockMode();
        if (lockMode.longValue() != BizConstant.SP_EQP_LOCK_MODE_WRITE.longValue()) {
            log.info("lockMode != SP_EQP_LOCK_MODE_WRITE");

            // Advanced Mode
            // Lock Equipment Main Object
            Inputs.ObjAdvanceLockIn objAdvancedObjectLockIn = new Inputs.ObjAdvanceLockIn();
            objAdvancedObjectLockIn.setObjectID(params.getEquipmentID());
            objAdvancedObjectLockIn.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
            objAdvancedObjectLockIn.setObjectType(BizConstant.SP_OBJECTLOCK_OBJECTTYPE_MAINOBJECT);
            objAdvancedObjectLockIn.setLockType(objLockModeOut.getRequiredLockForMainObject());
            objectLockMethod.advancedObjectLock(objCommon, objAdvancedObjectLockIn);

        } else {
            log.info("lockMode = SP_EQP_LOCK_MODE_WRITE");
            //--------------------------------
            //   Lock objects to be updated
            //--------------------------------
            log.info("Lock objects to be updated");
            objectLockMethod.objectLock(objCommon, CimMachine.class, params.getEquipmentID());
        }


        /*---------------------------------------*/
        /*   Get Eqp's Recipe Body Manage Flag   */
        /*---------------------------------------*/
        log.info("【step2】: Get Eqp's Recipe Body Manage Flag...");
        Validations.check(!equipmentMethod.equipmentRecipeBodyManageFlagGet(objCommon, equipmentID), retCodeConfig.getEqpRcpflagOff());

        /*------------------------------*/
        /*   Get Eqp's Operation Mode   */
        /*------------------------------*/
        log.info("【step3】: Get Eqp's Operation Mode...");
        Infos.EqpPortInfo eqpPortInfo = equipmentMethod.equipmentPortInfoGet(objCommon, equipmentID);
        List<Infos.EqpPortStatus> eqpPortStatuses = eqpPortInfo.getEqpPortStatuses();
        Validations.check(CimObjectUtils.isEmpty(eqpPortStatuses), retCodeConfig.getNotFoundPort());
        String onlineMode = eqpPortStatuses.get(0).getOnlineMode();
        Validations.check(CimStringUtils.equals(onlineMode, BizConstant.SP_EQP_ONLINEMODE_OFFLINE), retCodeConfig.getInvalidEquipmentMode(),
                ObjectIdentifier.fetchValue(equipmentID), onlineMode);

        /*-----------------------------------*/
        /*   Check Recipe Body File Naming   */
        /*-----------------------------------*/
        log.info("【step4】: Check Recipe Body File Naming");
        recipeMethod.recipeBodyFileNameCheckNaming(objCommon, fileName);

        /*--------------------------------------------------------*/
        /*   Check Recipe Body File Name is Already Used or Not   */
        /*--------------------------------------------------------*/
        log.info("【step5】: Check Recipe Body File Name is Already Used or Not");
        recipeMethod.recipeBodyFileNameCheckDuplicateDR(objCommon, machineRecipeID, fileLocation, fileName);

        /*-----------------------------------------------------------------------*/
        /*   Main Process                                                        */
        /*-----------------------------------------------------------------------*/
        /*-------------------------------------------------*/
        /*   Set Recipe Uploading Info to Machine Recipe   */
        /*-------------------------------------------------*/
        log.info("【step6】: Set Recipe Uploading Info to Machine Recipe...");
        machineRecipeMethod.machineRecipeUploadingInfoSet(objCommon, equipmentID, machineRecipeID, fileName, params.isFormatFlag());

        /*----------------------------------*/
        /*   Make RecipeBody Manage Event   */
        /*----------------------------------*/
        log.info("【step7】: Make RecipeBody Manage Event...");
        // recipeBodyManageEvent_Make
        eventMethod.recipeBodyManageEventMake(objCommon, TransactionIDEnum.RECIPE_UPLOAD_REQ.getValue(), equipmentID, BizConstant.SP_RCPMANAGEACTION_UPLOAD,
                machineRecipeID, physicalRecipeID, fileLocation, fileName, params.isFormatFlag(), params.getClaimMemo());

        /*---------------------------------------*/
        /*   Send Recipe Upload Request to TCS   */
        /*---------------------------------------*/
        log.info("【step8】: Send Recipe Upload Request to TCS...");
        Inputs.SendRecipeUploadReqIn in = new Inputs.SendRecipeUploadReqIn();
        in.setEquipmentID(equipmentID);
        in.setFileLocation(fileLocation);
        in.setClaimMemo(params.getClaimMemo());
        in.setFileName(fileName);
        in.setMachineRecipeID(machineRecipeID);
        in.setPhysicalRecipeID(physicalRecipeID);
        in.setRequestUserID(objCommon.getUser());
        in.setObjCommonIn(objCommon);
        in.setFormatFlag(params.isFormatFlag());
        tcsMethod.sendTCSReq(TCSReqEnum.sendRecipeUploadReq, in);
    }

    @Override
    public void sxRecipeDeleteReq(Infos.ObjCommon objCommon, Params.RecipeDeleteReqParams params) {
        ObjectIdentifier equipmentID = params.getEquipmentID();
        ObjectIdentifier machineRecipeID = params.getMachineRecipeID();
        String physicalRecipeID = params.getPhysicalRecipeID();
        String fileName = params.getFileName();
        String fileLocation = params.getFileLocation();
        boolean recipeFileDeleteFlag = params.isRecipeFileDeleteFlag();

        log.info("【step1】: lock mode");

        Inputs.ObjLockModeIn objLockModeIn = new Inputs.ObjLockModeIn();
        objLockModeIn.setObjectID(params.getEquipmentID());
        objLockModeIn.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
        objLockModeIn.setFunctionCategory(TransactionIDEnum.RECIPE_DELETION_REQ.getValue());
        objLockModeIn.setUserDataUpdateFlag(false);
        Outputs.ObjLockModeOut objLockModeOut = objectMethod.objectLockModeGet(objCommon, objLockModeIn);


        Long lockMode = objLockModeOut.getLockMode();
        if (lockMode.longValue() != BizConstant.SP_EQP_LOCK_MODE_WRITE.longValue()) {
            log.info("lockMode != SP_EQP_LOCK_MODE_WRITE");

            // Advanced Mode
            // Lock Equipment Main Object
            Inputs.ObjAdvanceLockIn objAdvancedObjectLockIn = new Inputs.ObjAdvanceLockIn();
            objAdvancedObjectLockIn.setObjectID(params.getEquipmentID());
            objAdvancedObjectLockIn.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
            objAdvancedObjectLockIn.setObjectType(BizConstant.SP_OBJECTLOCK_OBJECTTYPE_MAINOBJECT);
            objAdvancedObjectLockIn.setLockType(objLockModeOut.getRequiredLockForMainObject());
            objectLockMethod.advancedObjectLock(objCommon, objAdvancedObjectLockIn);

        } else {
            log.info("lockMode = SP_EQP_LOCK_MODE_WRITE");
            //--------------------------------
            //   Lock objects to be updated
            //--------------------------------
            log.info("Lock objects to be updated");
            objectLockMethod.objectLock(objCommon, CimMachine.class, params.getEquipmentID());
        }

        /*---------------------------------------*/
        /*   Get Eqp's Recipe Body Manage Flag   */
        /*---------------------------------------*/
        log.info("【step2】: Get Eqp's Recipe Body Manage Flag...");
        Validations.check(!equipmentMethod.equipmentRecipeBodyManageFlagGet(objCommon, equipmentID), retCodeConfig.getEqpRcpflagOff());

        /*------------------------------*/
        /*   Get Eqp's Operation Mode   */
        /*------------------------------*/
        log.info("【step3】: Get Eqp's Operation Mode...");
        Infos.EqpPortInfo eqpPortInfo = equipmentMethod.equipmentPortInfoGet(objCommon, equipmentID);
        List<Infos.EqpPortStatus> eqpPortStatuses = eqpPortInfo.getEqpPortStatuses();
        Validations.check(CimObjectUtils.isEmpty(eqpPortStatuses), retCodeConfig.getNotFoundPort());
        String onlineMode = eqpPortStatuses.get(0).getOnlineMode();
        Validations.check(CimStringUtils.equals(onlineMode, BizConstant.SP_EQP_ONLINEMODE_OFFLINE),
                retCodeConfig.getInvalidEquipmentMode(), ObjectIdentifier.fetchValue(equipmentID), onlineMode);

        /*-----------------------------------------------------------------------*/
        /*   Main Process                                                        */
        /*-----------------------------------------------------------------------*/
        /*------------------------------------------------*/
        /*   Set Recipe Deletion Info to Machine Recipe   */
        /*------------------------------------------------*/
        log.info("【step4】: Set Recipe Deletion Info to Machine Recipe");
        if (recipeFileDeleteFlag) {
            log.info("【step4-1】: Set Recipe Deletion Info to Machine Recipe...");
            machineRecipeMethod.machineRecipeDeletionInfoSet(objCommon, machineRecipeID);
            /*----------------------------------*/
            /*   Make RecipeBody Manage Event   */
            /*----------------------------------*/
            log.info("【step4-2】: Make RecipeBody Manage Event...");
            eventMethod.recipeBodyManageEventMake(objCommon, TransactionIDEnum.RECIPE_DELETION_REQ.getValue(), equipmentID, BizConstant.SP_RCPMANAGEACTION_FILEDELETE,
                    machineRecipeID, physicalRecipeID, fileLocation, fileName, false, params.getClaimMemo());
        }

        /*----------------------------------*/
        /*   Make RecipeBody Manage Event   */
        /*----------------------------------*/
        log.info("【step5】: Make RecipeBody Manage Event...");
        // recipeBodyManageEvent_Make
        eventMethod.recipeBodyManageEventMake(objCommon, TransactionIDEnum.RECIPE_DELETION_REQ.getValue(), equipmentID, BizConstant.SP_RCPMANAGEACTION_RECIPEDELETE,
                new ObjectIdentifier(), physicalRecipeID, "", "", false, params.getClaimMemo());

        /*-----------------------------------------*/
        /*   Send Recipe Deletion Request to EAP   */
        /*-----------------------------------------*/
        log.info("【step6】: Send Recipe Deletion Request to EAP");
        Inputs.SendRecipeDeleteReqIn in = new Inputs.SendRecipeDeleteReqIn();
        in.setEquipmentID(equipmentID);
        in.setFileLocation(fileLocation);
        in.setClaimMemo(params.getClaimMemo());
        in.setFileName(fileName);
        in.setMachineRecipeID(machineRecipeID);
        in.setPhysicalRecipeID(physicalRecipeID);
        in.setRequestUserID(objCommon.getUser());
        in.setRecipeFileDeleteFlag(recipeFileDeleteFlag);
        in.setObjCommonIn(objCommon);
        tcsMethod.sendTCSReq(TCSReqEnum.sendRecipeDeleteReq, in);
    }

    @Override
    public Results.RecipeParamAdjustOnActivePJReqResult sxRecipeParamAdjustOnActivePJReq(Infos.ObjCommon objCommon, Params.RecipeParamAdjustOnActivePJReqParam params) {
        //----------------------------------------------------------------
        //  Pre Process
        //----------------------------------------------------------------
        Results.RecipeParamAdjustOnActivePJReqResult recipeParamAdjustOnActivePJReqResult = new Results.RecipeParamAdjustOnActivePJReqResult();
        // Initialize

        //----------------------------------------------------------------
        //  In-Parameter Trace
        //----------------------------------------------------------------

        //------------------------------------------------------
        // Check Function Availability
        //------------------------------------------------------
        String PJCtrlFunc = StandardProperties.OM_PJ_CONTROL_ENABLE_FLAG.getValue();
        Validations.check(!CimStringUtils.equals(PJCtrlFunc, BizConstant.SP_FUNCTION_AVAILABLE_TRUE),
                new OmCode(retCodeConfig.getFunctionNotAvailable(), "Process Job Level Control"));

        //----------------------------------------------------------------
        //  In parameters check
        //----------------------------------------------------------------
        //------------------------------------------------------
        // Check Equipment
        //------------------------------------------------------
        // The PJ Level Control Flag of the equipment should be True
        // Equipment online mode should be On-Line Remote
        // Multiple recipe capability of the equipment should be MultipleRecipe
        log.info("call equipment_processJobLevelControlCheck()");
        equipmentMethod.equipmentProcessJobLevelControlCheck(objCommon, params.getEquipmentID(), true, true, true, true);

        //------------------------------------------------------
        // Check Control Job
        //------------------------------------------------------
        // The specified control job should be existing
        log.info("call controlJob_status_Get()");
        Outputs.ObjControlJobStatusGetOut strControlJobStatusGetOut = controlJobMethod.controlJobStatusGet(objCommon, params.getControlJobID());


        // Check ControlJob Status
        Validations.check(!CimStringUtils.equals(strControlJobStatusGetOut.getControlJobStatus(), BizConstant.SP_CONTROLJOBSTATUS_EXECUTING),
                new OmCode(retCodeConfigEx.getInvalidCjstatus(), strControlJobStatusGetOut.getControlJobStatus()));

        //Check and filter changed recipe parameters
        log.info("call recipeParameter_CheckConditionForAdjust()");
        Infos.RecipeParameterCheckConditionForAdjustIn in = new Infos.RecipeParameterCheckConditionForAdjustIn();
        in.setEquipmentID(params.getEquipmentID());
        in.setControlJobID(params.getControlJobID());
        in.setStrProcessRecipeParameterSeq(params.getStrProcessRecipeParameterSeq());
        Outputs.RecipeParameterCheckConditionForAdjustOut recipeParameterCheckConditionForAdjustOut = recipeMethod.recipeParameterCheckConditionForAdjust(objCommon, in);
        if (0 < CimArrayUtils.getSize(recipeParameterCheckConditionForAdjustOut.getStrProcessRecipeParameterSeq())) {
            //------------------------------------------------------
            // Send request to TCS
            //------------------------------------------------------
            Inputs.SendRecipeParamAdjustOnActivePJReqIn sendRecipeParamAdjustOnActivePJReqIn = new Inputs.SendRecipeParamAdjustOnActivePJReqIn();
            Params.RecipeParamAdjustOnActivePJReqParam strRecipeParamAdjustOnActivePJReqInParm = new Params.RecipeParamAdjustOnActivePJReqParam();
            sendRecipeParamAdjustOnActivePJReqIn.setStrRecipeParamAdjustOnActivePJReqInParm(strRecipeParamAdjustOnActivePJReqInParm);
            strRecipeParamAdjustOnActivePJReqInParm.setControlJobID(recipeParameterCheckConditionForAdjustOut.getControlJobID());
            strRecipeParamAdjustOnActivePJReqInParm.setEquipmentID(recipeParameterCheckConditionForAdjustOut.getEquipmentID());
            strRecipeParamAdjustOnActivePJReqInParm.setStrProcessRecipeParameterSeq(recipeParameterCheckConditionForAdjustOut.getStrProcessRecipeParameterSeq());
            sendRecipeParamAdjustOnActivePJReqIn.setObjCommonIn(objCommon);
            sendRecipeParamAdjustOnActivePJReqIn.setEquipmentID(recipeParameterCheckConditionForAdjustOut.getEquipmentID());
            Outputs.SendRecipeParamAdjustOnActivePJReqOut sendRecipeParamAdjustOnActivePJReqOut = (Outputs.SendRecipeParamAdjustOnActivePJReqOut) tcsMethod.sendTCSReq(TCSReqEnum.sendRecipeParamAdjustOnActivePJReq,sendRecipeParamAdjustOnActivePJReqIn);
            recipeParamAdjustOnActivePJReqResult = sendRecipeParamAdjustOnActivePJReqOut.getRecipeParamAdjustOnActivePJReqResult();

            //------------------------------------------------------
            // Create posProcessJobChangeEventRecord
            //------------------------------------------------------
            // Prepare event data
            int iCnt1 = 0;
            int iCnt2 = 0;
            int resultLen = CimArrayUtils.getSize(recipeParamAdjustOnActivePJReqResult.getStrMultipleBaseResultSeq());

            for (iCnt1 = 0; iCnt1 < resultLen; iCnt1++) {
                log.info("iCnt1 : {}", iCnt1);
                if ( !Validations.isSuccess(recipeParamAdjustOnActivePJReqResult.getStrMultipleBaseResultSeq().get(iCnt1).getReturnCode())) {
                    // No need to create event if equipment did not accept the action
                    log.info("returncode is not 0, continue : {}", recipeParamAdjustOnActivePJReqResult.getStrMultipleBaseResultSeq().get(iCnt1).getReturnCode());
                    continue;
                }

                int lenProcRecipPara = CimArrayUtils.getSize(recipeParameterCheckConditionForAdjustOut.getStrProcessRecipeParameterSeq());
                int indexProcRecipPara = 0;
                for (indexProcRecipPara = 0; indexProcRecipPara < lenProcRecipPara; indexProcRecipPara++) {
                    log.info("indexProcRecipPara : {}", indexProcRecipPara);
                    if ( CimStringUtils.equals(recipeParamAdjustOnActivePJReqResult.getStrMultipleBaseResultSeq().get(iCnt1).getKey() ,
                            recipeParameterCheckConditionForAdjustOut.getStrProcessRecipeParameterSeq().get(indexProcRecipPara).getProcessJobID() ) ) {
                        log.info("processJobID : {}", recipeParamAdjustOnActivePJReqResult.getStrMultipleBaseResultSeq().get(iCnt1).getKey());
                        break;
                    }
                }
                if (indexProcRecipPara >= lenProcRecipPara) {
                    // Unknown processIobID
                    log.info("Unknown processIobID : {}", recipeParamAdjustOnActivePJReqResult.getStrMultipleBaseResultSeq().get(iCnt1).getKey());
                    continue;
                }

                Inputs.ProcessJobChangeEventMakeParams strProcessJobChangeEvent_Make_in=new Inputs.ProcessJobChangeEventMakeParams();
                strProcessJobChangeEvent_Make_in.setControlJobID   (params.getControlJobID());
                strProcessJobChangeEvent_Make_in.setProcessJobID   (recipeParameterCheckConditionForAdjustOut.getStrProcessRecipeParameterSeq().get(indexProcRecipPara).getProcessJobID());
                strProcessJobChangeEvent_Make_in.setOpeCategory    (BizConstant.SP_PROCESSJOBOPECATEGORY_RECIPEPARAMETERADJUST);
                strProcessJobChangeEvent_Make_in.setProcessStart   (BizConstant.SP_PROCESSJOBSTART_DEFAULT);
                strProcessJobChangeEvent_Make_in.setCurrentState   (BizConstant.SP_PROCESSJOBSTATUS_UNKNOWN);
                strProcessJobChangeEvent_Make_in.setProcessWaferList(new ArrayList<>());
                strProcessJobChangeEvent_Make_in.setClaimMemo      (params.getClaimMemo());

                int lenStartRecipPara = CimArrayUtils.getSize(recipeParameterCheckConditionForAdjustOut.getStrProcessRecipeParameterSeq().get(indexProcRecipPara).getStartRecipeParameterList());
                strProcessJobChangeEvent_Make_in.setProcessJobChangeRecipeParameterList(new ArrayList<>(lenStartRecipPara));
                for (iCnt2 = 0; iCnt2 < lenStartRecipPara; iCnt2++) {
                    strProcessJobChangeEvent_Make_in.getProcessJobChangeRecipeParameterList().add(new Infos.ProcessJobChangeRecipeParameter());
                    strProcessJobChangeEvent_Make_in.getProcessJobChangeRecipeParameterList().get(iCnt2).setParameterName      (recipeParameterCheckConditionForAdjustOut.getStrProcessRecipeParameterSeq().get(indexProcRecipPara).getStartRecipeParameterList().get(iCnt2).getParameterName());
                    strProcessJobChangeEvent_Make_in.getProcessJobChangeRecipeParameterList().get(iCnt2).setPreParameterValue  (recipeParameterCheckConditionForAdjustOut.getPreProcessRecipeParameterSeq().get(indexProcRecipPara).getStartRecipeParameterList().get(iCnt2).getParameterValue());
                    strProcessJobChangeEvent_Make_in.getProcessJobChangeRecipeParameterList().get(iCnt2).setParameterValue     (recipeParameterCheckConditionForAdjustOut.getStrProcessRecipeParameterSeq().get(indexProcRecipPara).getStartRecipeParameterList().get(iCnt2).getParameterValue());
                }

                eventMethod.processJobChangeEventMake (
                        objCommon,
                        strProcessJobChangeEvent_Make_in);
            }
        }
        //------------------------------------------------------
        // Set output result
        //------------------------------------------------------

        // Return to caller
        return recipeParamAdjustOnActivePJReqResult;
    }

    @Override
    public void sxRecipeParamAdjustReq(Infos.ObjCommon objCommon,Params.RecipeParamAdjustReqParams params) {
        ObjectIdentifier equipmentID = params.getEquipmentID();
        ObjectIdentifier controlJobID = params.getControlJobID();
        List<Infos.StartCassette> strStartCassette = params.getStrStartCassette();
        Boolean allProcessStartFlag = params.getAllProcessStartFlag();

        Inputs.ObjLockModeIn objLockModeIn = new Inputs.ObjLockModeIn();
        objLockModeIn.setObjectID(params.getEquipmentID());
        objLockModeIn.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
        objLockModeIn.setFunctionCategory(TransactionIDEnum.RECIPE_PARAMETER_ADJUST_REQ.getValue());
        objLockModeIn.setUserDataUpdateFlag(false);
        Outputs.ObjLockModeOut objLockModeOut = objectMethod.objectLockModeGet(objCommon, objLockModeIn);


        Long lockMode = objLockModeOut.getLockMode();
        if (lockMode.longValue() != BizConstant.SP_EQP_LOCK_MODE_WRITE.longValue()) {
            log.info("lockMode != SP_EQP_LOCK_MODE_WRITE");

            // Advanced Mode
            // Lock Equipment Main Object
            Inputs.ObjAdvanceLockIn objAdvancedObjectLockIn = new Inputs.ObjAdvanceLockIn();
            objAdvancedObjectLockIn.setObjectID(params.getEquipmentID());
            objAdvancedObjectLockIn.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
            objAdvancedObjectLockIn.setObjectType(BizConstant.SP_OBJECTLOCK_OBJECTTYPE_MAINOBJECT);
            objAdvancedObjectLockIn.setLockType(objLockModeOut.getRequiredLockForMainObject());
            objectLockMethod.advancedObjectLock(objCommon, objAdvancedObjectLockIn);

        } else {
            log.info("lockMode = SP_EQP_LOCK_MODE_WRITE");
            //--------------------------------
            //   Lock objects to be updated
            //--------------------------------
            log.info("Lock objects to be updated");
            objectLockMethod.objectLock(objCommon, CimMachine.class, params.getEquipmentID());
        }

        int nILen = CimArrayUtils.getSize(params.getStrStartCassette());

        List<ObjectIdentifier> lotIDs = new ArrayList<>();
        List<ObjectIdentifier> cassetteIDs = new ArrayList<>();
        for (int i=0; i < nILen; i++ ) {
            cassetteIDs.add(params.getStrStartCassette().get(i).getCassetteID());
            int nJLen = CimArrayUtils.getSize(params.getStrStartCassette().get(i).getLotInCassetteList());
            for (int j=0; j < nJLen; j++ ) {
                lotIDs.add(params.getStrStartCassette().get(i).getLotInCassetteList().get(j).getLotID());
            }
        }
        /*------------------------------*/
        /*   Lock Cassette/Lot Object   */
        /*-------------------------------*/

        log.info("calling objectSequence_Lock() : {}", BizConstant.SP_CLASSNAME_POSCASSETTE );
        objectLockMethod.objectSequenceLock(objCommon, CimCassette.class, cassetteIDs);

        log.info("calling objectSequence_Lock(){}", BizConstant.SP_CLASSNAME_POSLOT );
        objectLockMethod.objectSequenceLock(objCommon, CimLot.class, lotIDs);

        List<Infos.StartCassette> startCassettes = recipeMethod.recipeParameterAdjustConditionCheck(objCommon, equipmentID, controlJobID, strStartCassette, allProcessStartFlag);

        //step7 - TCSMgr_SendRecipeParamAdjustReq
        /*-------------------------*/
        /*   Send Request to EAP   */
        /*-------------------------*/
        Inputs.SendRecipeParamAdjustReqIn sendRecipeParamAdjustReqIn = new Inputs.SendRecipeParamAdjustReqIn();
        sendRecipeParamAdjustReqIn.setObjCommonIn(objCommon);
        sendRecipeParamAdjustReqIn.setEquipmentID(params.getEquipmentID());
        sendRecipeParamAdjustReqIn.setControlJobID(params.getControlJobID());
        sendRecipeParamAdjustReqIn.setStrStartCassette(startCassettes);
        sendRecipeParamAdjustReqIn.setAllProcessStartFlag(params.getAllProcessStartFlag());
        sendRecipeParamAdjustReqIn.setClaimMemo(params.getClaimMemo());
        tcsMethod.sendTCSReq(TCSReqEnum.sendRecipeParamAdjustReq,sendRecipeParamAdjustReqIn);
    }

    @Override
    public void sxRecipeParamAdjustRpt(Infos.ObjCommon objCommon, Params.RecipeParamAdjustRptParams params) {
        //------------------------------------------------------
        // Check Function Availability
        //------------------------------------------------------
        String pjCtrlFunc = StandardProperties.OM_PJ_CONTROL_ENABLE_FLAG.getValue();
        Validations.check(!CimStringUtils.equals(pjCtrlFunc, BizConstant.SP_FUNCTION_AVAILABLE_TRUE),
                new OmCode(retCodeConfig.getFunctionNotAvailable(), "Process Job Level Control"));

        // Get Lot List from ControlJobID
        //【step1】 - controlJob_containedLot_Get
        List<Infos.ControlJobCassette> controlJobContainedLotRetCode = controlJobMethod.controlJobContainedLotGet(objCommon, params.getControlJobID());
        log.info("Get containedLot");
        //Lock Objects
        int lenCtrlCas = CimArrayUtils.getSize(controlJobContainedLotRetCode);
        for (int i=0; i < lenCtrlCas; i++)
        {
            log.info("lenCtrlCas, i---> {}", i);
            int lenCtrlJobLot = CimArrayUtils.getSize(controlJobContainedLotRetCode.get(i).getControlJobLotList());
            for (int j=0; j < lenCtrlJobLot; j++) {
                log.info("lenCtrlJobLot, j---> {}", j);
                if (CimBooleanUtils.isTrue(controlJobContainedLotRetCode.get(i).getControlJobLotList().get(j).getOperationStartFlag())) {
                    log.info( "operationStartFlag == TRUE, lotID---> {}", controlJobContainedLotRetCode.get(i).getControlJobLotList().get(j).getLotID());
                    log.info( "object_Lock" );
                    objectLockMethod.objectLock(objCommon, CimLot.class, controlJobContainedLotRetCode.get(i).getControlJobLotList().get(j).getLotID());
                }
            }
        }
        //Check input recipe parameters and get the resulted recipe parameters
        log.info("RecipeParameter_CheckConditionForStore");
        //【step3】 - recipeParameter_CheckConditionForStore
        Outputs.ObjRecipeParameterCheckConditionForStoreOut objRecipeParameterCheckConditionForStoreOut = recipeMethod.recipeParameterCheckConditionForStore(objCommon, params.getControlJobID(), params.getStrLotWaferSeq());
        //Check input recipe parameters and get the resulted recipe parameters
        log.info("processOperation_recipeParameters_Set");
        //【step4】 - processOperation_recipeParameters_Set
        processMethod.processOperationRecipeParametersSet(objCommon,params.getControlJobID(),objRecipeParameterCheckConditionForStoreOut.getStrLotStartRecipeParameterSeq());
    }

    @Override
    public void sxRecipeDownloadReq(Infos.ObjCommon objCommon, Params.RecipeDownloadReqParams params) {

        Inputs.ObjLockModeIn objLockModeIn = new Inputs.ObjLockModeIn();
        objLockModeIn.setObjectID(params.getEquipmentID());
        objLockModeIn.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
        objLockModeIn.setFunctionCategory(TransactionIDEnum.RECIPE_DOWNLOAD_REQ.getValue());
        objLockModeIn.setUserDataUpdateFlag(false);
        Outputs.ObjLockModeOut objLockModeOut = objectMethod.objectLockModeGet(objCommon, objLockModeIn);


        Long lockMode = objLockModeOut.getLockMode();
        if (lockMode.longValue() != BizConstant.SP_EQP_LOCK_MODE_WRITE.longValue()) {
            log.info("lockMode != SP_EQP_LOCK_MODE_WRITE");

            // Advanced Mode
            // Lock Equipment Main Object
            Inputs.ObjAdvanceLockIn objAdvancedObjectLockIn = new Inputs.ObjAdvanceLockIn();
            objAdvancedObjectLockIn.setObjectID(params.getEquipmentID());
            objAdvancedObjectLockIn.setClassName(BizConstant.SP_CLASSNAME_POSMACHINE);
            objAdvancedObjectLockIn.setObjectType(BizConstant.SP_OBJECTLOCK_OBJECTTYPE_MAINOBJECT);
            objAdvancedObjectLockIn.setLockType(objLockModeOut.getRequiredLockForMainObject());
            objectLockMethod.advancedObjectLock(objCommon, objAdvancedObjectLockIn);

        } else {
            log.info("lockMode = SP_EQP_LOCK_MODE_WRITE");
            //--------------------------------
            //   Lock objects to be updated
            //--------------------------------
            log.info("Lock objects to be updated");
            objectLockMethod.objectLock(objCommon, CimMachine.class, params.getEquipmentID());
        }

        ObjectIdentifier equipmentID = params.getEquipmentID();
        /*---------------------------------------*/
        /*   Get Eqp's Recipe Body Manage Flag   */
        /*---------------------------------------*/
        Validations.check(!equipmentMethod.equipmentRecipeBodyManageFlagGet(objCommon, equipmentID), retCodeConfig.getEqpRcpflagOff());

        /*------------------------------*/
        /*   Get Eqp's Operation Mode   */
        /*------------------------------*/
        Infos.EqpPortInfo eqpPortInfo = equipmentMethod.equipmentPortInfoGet(objCommon, equipmentID);
        List<Infos.EqpPortStatus> eqpPortStatuses = eqpPortInfo.getEqpPortStatuses();
        Validations.check(CimObjectUtils.isEmpty(eqpPortStatuses), retCodeConfig.getNotFoundPort());
        Validations.check(CimStringUtils.equals(eqpPortStatuses.get(0).getOnlineMode(), BizConstant.SP_EQP_ONLINEMODE_OFFLINE),
                retCodeConfig.getInvalidEquipmentMode(), ObjectIdentifier.fetchValue(equipmentID), eqpPortStatuses.get(0).getOnlineMode());

        /*---------------------------------------------------*/
        /*   Set Recipe Downloading Info to Machine Recipe   */
        /*---------------------------------------------------*/
        machineRecipeMethod.machineRecipeDownloadingInfoSet(objCommon, equipmentID, params.getMachineRecipeID());

        /*----------------------------------*/
        /*   Make RecipeBody Manage Event   */
        /*----------------------------------*/
        eventMethod.recipeBodyManageEventMake(objCommon, TransactionIDEnum.RECIPE_DOWNLOAD_REQ.getValue(), params.getEquipmentID(),
                BizConstant.SP_RCPMANAGEACTION_DOWNLOAD, params.getMachineRecipeID(), params.getPhysicalRecipeID(), params.getFileLocation(),
                params.getFileName(), params.isFormatFlag(), params.getClaimMemo());

        /*-----------------------------------------*/
        /*   Send Recipe Download Request to TCS   */
        /*-----------------------------------------*/
        Inputs.SendRecipeDownloadReqIn in = new Inputs.SendRecipeDownloadReqIn();
        in.setClaimMemo(params.getClaimMemo());
        in.setEquipmentID(params.getEquipmentID());
        in.setFileLocation(params.getFileLocation());
        in.setFileName(params.getFileName());
        in.setFormatFlag(params.isFormatFlag());
        in.setMachineRecipeID(params.getMachineRecipeID());
        in.setPhysicalRecipeID(params.getPhysicalRecipeID());
        in.setRequestUserID(objCommon.getUser());
        in.setObjCommonIn(objCommon);
        tcsMethod.sendTCSReq(TCSReqEnum.sendRecipeDownloadReq, in);
    }

}
